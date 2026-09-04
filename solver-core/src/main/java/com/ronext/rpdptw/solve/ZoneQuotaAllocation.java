package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.problem.Problem;

/**
 * H23·H24 공통 — 존 → 차량 유형 대수 배정 DP (heuristics 문서 §3.4·§5 H23 공통).
 * 상태 = 남은 유형별 대수 벡터, 전이 = 프론티어(최소 덮개 · 한 대 모자란 최대 비덮개 · 빈 집합),
 * 값 = (Σ부족, Σ낭비, Σ사용 대수) 사전식. 호환 성분마다 독립 실행하고 도달 상태만 보관하며,
 * 성분당 보관 상태 총량이 MAX_TOTAL_STATES를 넘으면 값 순으로 잘라 근사한다 — 기권하지 않는다 (§4.3).
 */
final class ZoneQuotaAllocation {

    /** 성분 하나의 DP가 층 전체에 보관하는 상태 수 총량 (재량 상수). 넘치면 값 순으로 잘라 근사한다. */
    static final int MAX_TOTAL_STATES = 262_144;
    static final String NO_ZONE = VehicleZoneFillConstruction.NO_ZONE;

    /** 상태 키 순서 — 마지막 유형부터 첫 유형 순으로 비교 (종전 혼합 진법 키 오름차순과 같은 순서). */
    static final Comparator<int[]> KEY_ORDER = (a, b) -> {
        for (int i = a.length - 1; i >= 0; i--) {
            if (a[i] != b[i]) {
                return a[i] < b[i] ? -1 : 1;
            }
        }
        return 0;
    };

    private ZoneQuotaAllocation() {}

    /** 차량 유형 = (호환 Request 집합, maxWeight, maxVolume, effectiveMaxStopCount)이 같은 차량들. 유형 안은 VehicleId ASC. */
    record VehicleType(Set<RequestId> compatible, long maxWeight, long maxVolume,
                       OptionalInt maxStopCount, List<VehicleId> vehicles) {}

    /** 존 = anchor side zoneId(부재 "(none)")별 Request. */
    record Zone(String zoneId, List<RequestId> members) {}

    /** 유형 목록·존 순서대로의 존 목록·존별 배정 차량. 배정 없는 존은 빈 목록. truncated = 폭 제한이 잘라냈으면 true. */
    record Allocation(List<VehicleType> types, List<Zone> zones,
                      Map<String, List<VehicleId>> vehiclesByZone, boolean truncated) {}

    /** 호환 성분 — 유형 index 목록(유형 순서)·존 index 목록(존 순서). 존이 없는 성분은 만들지 않는다 (X24). */
    record Component(List<Integer> typeIndexes, List<Integer> zoneIndexes) {}

    /** 유형 순서 = (maxVolume ASC, maxWeight ASC, 첫 VehicleId ASC). 근무창·depot·속도는 유형에 넣지 않는다. */
    static List<VehicleType> vehicleTypes(Problem problem) {
        Map<VehicleId, Set<RequestId>> compatibleByVehicle = new LinkedHashMap<>();
        for (Vehicle vehicle : problem.vehicles()) {
            compatibleByVehicle.put(vehicle.id(), new LinkedHashSet<>());
        }
        for (Request request : problem.requests()) {
            for (VehicleId vehicleId : problem.compatibleVehicles(request.id())) {
                compatibleByVehicle.get(vehicleId).add(request.id());
            }
        }
        Map<String, List<Vehicle>> groups = new TreeMap<>();
        for (Vehicle vehicle : problem.vehicles()) {
            List<String> ids = new ArrayList<>();
            for (RequestId id : compatibleByVehicle.get(vehicle.id())) {
                ids.add(id.value());
            }
            ids.sort(Comparator.naturalOrder());
            String key = vehicle.maxWeight() + "|" + vehicle.maxVolume() + "|" + vehicle.effectiveMaxStopCount() + "|" + ids;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(vehicle);
        }
        List<VehicleType> types = new ArrayList<>();
        for (List<Vehicle> group : groups.values()) {
            group.sort(Comparator.comparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
            Vehicle first = group.getFirst();
            List<VehicleId> ids = new ArrayList<>();
            for (Vehicle vehicle : group) {
                ids.add(vehicle.id());
            }
            types.add(new VehicleType(
                    Set.copyOf(compatibleByVehicle.get(first.id())), first.maxWeight(), first.maxVolume(),
                    first.effectiveMaxStopCount(), List.copyOf(ids)));
        }
        types.sort(Comparator.comparingLong(VehicleType::maxVolume)
                .thenComparingLong(VehicleType::maxWeight)
                .thenComparing(t -> t.vehicles().getFirst(), InsertionSearch.BY_VEHICLE_ID));
        return types;
    }

    /** 존 순서 = (호환 유형 수 ASC, Σvolume DESC, zoneId ASC). 호환 차량 0대인 Request는 존에 넣지 않는다. */
    static List<Zone> zones(Problem problem, List<VehicleType> types) {
        Map<String, List<RequestId>> grouped = new TreeMap<>();
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
            if (problem.compatibleVehicles(requestId).isEmpty()) {
                continue;
            }
            String zoneId = InsertionSearch.anchor(problem.request(requestId)).zoneId().orElse(NO_ZONE);
            grouped.computeIfAbsent(zoneId, z -> new ArrayList<>()).add(requestId);
        }
        List<Zone> zones = new ArrayList<>();
        for (Map.Entry<String, List<RequestId>> entry : grouped.entrySet()) {
            zones.add(new Zone(entry.getKey(), List.copyOf(entry.getValue())));
        }
        zones.sort(Comparator.<Zone>comparingInt(z -> compatibleTypeCount(types, z.members()))
                .thenComparing(Comparator.comparingLong((Zone z) -> totalVolume(problem, z.members())).reversed())
                .thenComparing(Zone::zoneId));
        return zones;
    }

    /**
     * 호환 성분 — 정점 = 유형 ∪ 존, 간선 z–t ⇔ t ∈ compat(z). union-find를 유형 순서 → 존 순서로 돌아 결정적이다.
     * 성분 순서 = 성분에 든 첫 존의 존 순서. 존이 없는 성분(호환 존이 하나도 없는 유형)은 돌려주지 않는다 (X24).
     */
    static List<Component> components(List<VehicleType> types, List<Zone> zones, Problem problem) {
        int typeCount = types.size();
        int[] parent = new int[typeCount + zones.size()];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }
        for (int z = 0; z < zones.size(); z++) {
            for (int t = 0; t < typeCount; t++) {
                if (compatible(types.get(t), zones.get(z))) {
                    union(parent, t, typeCount + z);
                }
            }
        }
        Map<Integer, List<Integer>> typesByRoot = new LinkedHashMap<>();
        for (int t = 0; t < typeCount; t++) {
            typesByRoot.computeIfAbsent(find(parent, t), r -> new ArrayList<>()).add(t);
        }
        Map<Integer, List<Integer>> zonesByRoot = new LinkedHashMap<>();
        List<Component> components = new ArrayList<>();
        for (int z = 0; z < zones.size(); z++) {
            int root = find(parent, typeCount + z);
            List<Integer> members = zonesByRoot.get(root);
            if (members == null) {
                members = new ArrayList<>();
                zonesByRoot.put(root, members);
                components.add(new Component(typesByRoot.getOrDefault(root, List.of()), members));
            }
            members.add(z);
        }
        return components;
    }

    private static boolean compatible(VehicleType type, Zone zone) {
        for (RequestId id : zone.members()) {
            if (type.compatible().contains(id)) {
                return true;
            }
        }
        return false;
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra != rb) {
            parent[Math.max(ra, rb)] = Math.min(ra, rb);
        }
    }

    static int compatibleTypeCount(List<VehicleType> types, RequestId requestId) {
        int count = 0;
        for (VehicleType type : types) {
            if (type.compatible().contains(requestId)) {
                count++;
            }
        }
        return count;
    }

    private static int compatibleTypeCount(List<VehicleType> types, List<RequestId> members) {
        int count = 0;
        for (VehicleType type : types) {
            for (RequestId id : members) {
                if (type.compatible().contains(id)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private static long totalVolume(Problem problem, List<RequestId> members) {
        long total = 0L;
        for (RequestId id : members) {
            total = Math.addExact(total, problem.request(id).totalVolume());
        }
        return total;
    }

    /** 방문 수 — 단일 패턴 1, PICKUP_DELIVERY 2 (정차 한도 근사). */
    static int visitCount(Request request) {
        return (request.pickup().isPresent() ? 1 : 0) + (request.delivery().isPresent() ? 1 : 0);
    }

    /** 존이 유형 t를 최대로 쓸 수 있는 대수 — 호환 아니면 0, 아니면 자원별 올림의 최대 (최소 1). 용량·한도 부재는 그 항 0. */
    static int maxNeed(Demand demand, VehicleType type) {
        int index = demand.types.indexOf(type);
        if (index < 0 || (demand.compat & (1L << index)) == 0) {
            return 0;
        }
        long need = 1L;
        if (type.maxVolume() > 0L) {
            need = Math.max(need, ceilDiv(demand.totalVolume, type.maxVolume()));
        }
        if (type.maxWeight() > 0L) {
            need = Math.max(need, ceilDiv(demand.totalWeight, type.maxWeight()));
        }
        if (type.maxStopCount().isPresent() && type.maxStopCount().getAsInt() > 0) {
            need = Math.max(need, ceilDiv(demand.totalVisits, type.maxStopCount().getAsInt()));
        }
        return (int) Math.min(need, Integer.MAX_VALUE);
    }

    private static long ceilDiv(long a, long b) {
        return (a + b - 1L) / b;
    }

    /** 존 전이 하나의 값 (부족, 낭비, Σs) — s에 든 유형만 합한다. */
    static long[] value(Demand demand, List<VehicleType> types, int[] s) {
        long capacity = 0L;
        long used = 0L;
        for (int t = 0; t < s.length; t++) {
            if (s[t] == 0) {
                continue;
            }
            capacity = Math.addExact(capacity, s[t] * types.get(t).maxVolume());
            used += s[t];
        }
        boolean covers = demand.covers(s);
        long shortage = covers ? 0L : Math.max(1L, demand.totalVolume - capacity);
        long waste = covers ? capacity - demand.totalVolume : 0L;
        return new long[] {shortage, waste, used};
    }

    static Allocation allocate(Problem problem) {
        return allocate(problem, MAX_TOTAL_STATES);
    }

    static Allocation allocate(Problem problem, int maxTotalStates) {
        List<VehicleType> types = vehicleTypes(problem);
        List<Zone> zones = zones(problem, types);
        int typeCount = types.size();
        Demand[] demands = new Demand[zones.size()];
        int[][] maxNeeds = new int[zones.size()][];
        for (int z = 0; z < zones.size(); z++) {
            demands[z] = Demand.of(problem, types, zones.get(z));
            maxNeeds[z] = new int[typeCount];
            for (int t = 0; t < typeCount; t++) {
                maxNeeds[z][t] = maxNeed(demands[z], types.get(t));
            }
        }

        Map<String, int[]> quotas = new LinkedHashMap<>();
        boolean truncated = false;
        for (Component component : components(types, zones, problem)) {
            truncated |= solveComponent(types, zones, demands, maxNeeds, component, maxTotalStates, quotas);
        }

        int[] cursor = new int[typeCount];
        Map<String, List<VehicleId>> vehiclesByZone = new LinkedHashMap<>();
        for (Zone zone : zones) {
            int[] quota = quotas.get(zone.zoneId());
            List<VehicleId> vehicles = new ArrayList<>();
            for (int t = 0; t < typeCount; t++) {
                for (int i = 0; i < quota[t]; i++) {
                    vehicles.add(types.get(t).vehicles().get(cursor[t]++));
                }
            }
            vehiclesByZone.put(zone.zoneId(), List.copyOf(vehicles));
        }
        return new Allocation(types, zones, vehiclesByZone, truncated);
    }

    /** 성분 하나의 희소 DP — 존별 (유형 → 대수)를 quotas에 넣고, 폭 제한이 잘라냈으면 true. */
    private static boolean solveComponent(
            List<VehicleType> types, List<Zone> zones, Demand[] demands, int[][] maxNeeds,
            Component component, int maxTotalStates, Map<String, int[]> quotas) {
        int typeCount = types.size();
        List<Integer> zoneIndexes = component.zoneIndexes();
        int zoneCount = zoneIndexes.size();

        // 풍부 유형 — 대수가 Σ maxNeed 이상이면 남은 대수가 열거 범위를 제한하지 못한다 → 상태 벡터에서 뺀다.
        List<Integer> stateDims = new ArrayList<>();
        List<Integer> abundantDims = new ArrayList<>();
        for (int t : component.typeIndexes()) {
            long need = 0L;
            for (int z : zoneIndexes) {
                need += maxNeeds[z][t];
            }
            if (types.get(t).vehicles().size() >= need) {
                abundantDims.add(t);
            } else {
                stateDims.add(t);
            }
        }
        int dims = stateDims.size();
        int abundant = abundantDims.size();

        int[] start = new int[dims];
        for (int i = 0; i < dims; i++) {
            start[i] = types.get(stateDims.get(i)).vehicles().size();
        }
        List<Layer> layers = new ArrayList<>();
        layers.add(Layer.initial(start, abundant));

        boolean truncated = false;
        long stored = 1L;
        int[] range = new int[typeCount];
        for (int zi = 0; zi < zoneCount; zi++) {
            int zoneIndex = zoneIndexes.get(zi);
            Demand demand = demands[zoneIndex];
            int[] maxNeedZ = maxNeeds[zoneIndex];
            Layer from = layers.get(zi);
            TreeMap<int[], Entry> next = new TreeMap<>(KEY_ORDER);
            TreeMap<int[], List<int[]>> frontiers = new TreeMap<>(KEY_ORDER);   // 같은 R이면 프론티어도 같다 (존마다 새로)
            for (int i = 0; i < from.size; i++) {                                  // 층은 KEY_ORDER 오름차순
                int[] key = from.key(i);
                System.arraycopy(maxNeedZ, 0, range, 0, typeCount);
                for (int d = 0; d < dims; d++) {
                    range[stateDims.get(d)] = Math.min(key[d], maxNeedZ[stateDims.get(d)]);
                }
                long[] base = from.value(i);
                List<int[]> frontier = frontiers.get(range);
                if (frontier == null) {
                    frontier = frontier(range, demand);
                    frontiers.put(range.clone(), frontier);
                }
                for (int[] s : frontier) {
                    long[] delta = value(demand, types, s);
                    long[] candidate = {base[0] + delta[0], base[1] + delta[1], base[2] + delta[2]};
                    int[] to = new int[dims];
                    for (int d = 0; d < dims; d++) {
                        to[d] = key[d] - s[stateDims.get(d)];
                    }
                    Entry existing = next.get(to);
                    if (existing != null && compare(candidate, existing.value) >= 0) {   // 동률 = 먼저 계산된 값 유지
                        continue;
                    }
                    int[] taken = new int[abundant];
                    for (int a = 0; a < abundant; a++) {
                        taken[a] = s[abundantDims.get(a)];
                    }
                    next.put(to, new Entry(candidate, i, taken));
                }
            }
            long cap = Math.max(1L, (maxTotalStates - stored) / (zoneCount - zi));
            List<Map.Entry<int[], Entry>> entries = new ArrayList<>(next.entrySet());
            if (entries.size() > cap) {
                entries.sort((a, b) -> compare(a.getValue().value, b.getValue().value));   // 안정 정렬 → 2차 키 KEY_ORDER
                entries = new ArrayList<>(entries.subList(0, (int) cap));
                entries.sort((a, b) -> KEY_ORDER.compare(a.getKey(), b.getKey()));
                truncated = true;
            }
            Layer layer = Layer.of(entries, dims, abundant);
            layers.add(layer);
            stored += layer.size;
        }

        int best = 0;
        Layer last = layers.get(zoneCount);
        for (int i = 1; i < last.size; i++) {                                      // 동률 = 남은 벡터 KEY_ORDER 최소
            if (compare(last.value(i), last.value(best)) < 0) {
                best = i;
            }
        }
        int index = best;
        for (int zi = zoneCount - 1; zi >= 0; zi--) {
            Layer layer = layers.get(zi + 1);
            Layer previous = layers.get(zi);
            int parent = layer.parent[index];
            int[] quota = new int[typeCount];
            for (int d = 0; d < dims; d++) {
                quota[stateDims.get(d)] = previous.key(parent)[d] - layer.key(index)[d];
            }
            for (int a = 0; a < abundant; a++) {
                quota[abundantDims.get(a)] = layer.abundant[index * abundant + a];
            }
            quotas.put(zones.get(zoneIndexes.get(zi)).zoneId(), quota);
            index = parent;
        }
        return truncated;
    }

    /**
     * 프론티어 — 유형 순서로 열거 범위 range 안에서 재귀 나열(covers가 되는 순간 그 유형에서 중단)한 s 중
     * (a) 최소 덮개 · (b) 한 대 모자란 최대 비덮개 · (c) 빈 집합.
     */
    static List<int[]> frontier(int[] range, Demand demand) {
        List<int[]> out = new ArrayList<>();
        enumerate(0, new int[range.length], range, demand, out);
        return out;
    }

    private static void enumerate(int t, int[] s, int[] range, Demand demand, List<int[]> out) {
        if (t == s.length) {
            classify(s, range, demand, out);
            return;
        }
        for (int c = 0; c <= range[t]; c++) {
            s[t] = c;
            enumerate(t + 1, s, range, demand, out);
            if (demand.covers(s)) {                                            // 이후 유형은 0인 상태 — 이미 덮으면 더 늘릴 이유가 없다
                break;
            }
        }
        s[t] = 0;
    }

    private static void classify(int[] s, int[] range, Demand demand, List<int[]> out) {
        int typeCount = s.length;
        boolean empty = true;
        for (int c : s) {
            empty &= c == 0;
        }
        if (empty) {
            out.add(s.clone());                                                 // (c)
            return;
        }
        int[] probe = s.clone();
        if (demand.covers(s)) {
            int smallest = 0;
            while (s[smallest] == 0) {
                smallest++;
            }
            probe[smallest]--;
            if (!demand.covers(probe)) {
                out.add(s.clone());                                             // (a)
            }
            return;
        }
        boolean extendable = false;
        for (int t = 0; t < typeCount; t++) {
            if (range[t] > s[t]) {
                extendable = true;
                probe[t]++;
                boolean covers = demand.covers(probe);
                probe[t]--;
                if (!covers) {
                    return;
                }
            }
        }
        if (extendable) {
            out.add(s.clone());                                                 // (b)
        }
    }

    static int compare(long[] a, long[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return a[i] < b[i] ? -1 : 1;
            }
        }
        return 0;
    }

    /** 다음 층을 만드는 동안의 상태 하나 — 값·직전 층 index·풍부 유형의 대수(키에 없어 따로 들고 간다). */
    private record Entry(long[] value, int parent, int[] abundant) {}

    /** DP 한 층 — 도달 상태만 평면 배열로 (키 dims개 · 값 3개 · parent · 풍부 유형 대수). KEY_ORDER 오름차순. */
    private static final class Layer {
        final int size;
        final int dims;
        final int[] keys;
        final long[] values;
        final int[] parent;
        final int[] abundant;

        private Layer(int size, int dims, int abundantCount) {
            this.size = size;
            this.dims = dims;
            this.keys = new int[size * dims];
            this.values = new long[size * 3];
            this.parent = new int[size];
            this.abundant = new int[size * abundantCount];
        }

        static Layer initial(int[] start, int abundantCount) {
            Layer layer = new Layer(1, start.length, abundantCount);
            System.arraycopy(start, 0, layer.keys, 0, start.length);
            layer.parent[0] = -1;
            return layer;
        }

        static Layer of(List<Map.Entry<int[], Entry>> entries, int dims, int abundantCount) {
            Layer layer = new Layer(entries.size(), dims, abundantCount);
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<int[], Entry> entry = entries.get(i);
                System.arraycopy(entry.getKey(), 0, layer.keys, i * dims, dims);
                System.arraycopy(entry.getValue().value(), 0, layer.values, i * 3, 3);
                layer.parent[i] = entry.getValue().parent();
                System.arraycopy(entry.getValue().abundant(), 0, layer.abundant, i * abundantCount, abundantCount);
            }
            return layer;
        }

        int[] key(int index) {
            int[] key = new int[dims];
            System.arraycopy(keys, index * dims, key, 0, dims);
            return key;
        }

        long[] value(int index) {
            return new long[] {values[index * 3], values[index * 3 + 1], values[index * 3 + 2]};
        }
    }

    /**
     * 존 수요 — 호환 그룹(|호환 유형 수| ASC, 유형 비트 ASC)의 누적값과 그룹별 최대 단품.
     * covers(s) = 접두마다 부피·무게·방문 수 충족 ∧ 그룹마다 최대 단품을 실을 호환 유형 존재 (Hall 조건).
     */
    static final class Demand {
        private final List<VehicleType> types;
        final long totalVolume;
        final long totalWeight;
        final long totalVisits;
        final long compat;                                                  // 유형 t의 비트 = 1L << t (차종 ≤ 64)
        private final long[] prefixMask;
        private final long[] prefixVolume;
        private final long[] prefixWeight;
        private final long[] prefixVisits;
        private final long[] groupMask;
        private final long[] groupMaxVolume;
        private final long[] groupMaxWeight;

        private Demand(List<VehicleType> types, long totalVolume, long totalWeight, long totalVisits, long compat,
                       long[] prefixMask, long[] prefixVolume, long[] prefixWeight, long[] prefixVisits,
                       long[] groupMask, long[] groupMaxVolume, long[] groupMaxWeight) {
            this.types = types;
            this.totalVolume = totalVolume;
            this.totalWeight = totalWeight;
            this.totalVisits = totalVisits;
            this.compat = compat;
            this.prefixMask = prefixMask;
            this.prefixVolume = prefixVolume;
            this.prefixWeight = prefixWeight;
            this.prefixVisits = prefixVisits;
            this.groupMask = groupMask;
            this.groupMaxVolume = groupMaxVolume;
            this.groupMaxWeight = groupMaxWeight;
        }

        static Demand of(Problem problem, List<VehicleType> types, Zone zone) {
            TreeMap<Long, long[]> groups = new TreeMap<>(
                    Comparator.comparingInt(Long::bitCount).thenComparing(Comparator.naturalOrder()));
            long total = 0L;
            for (RequestId id : zone.members()) {
                Request request = problem.request(id);
                long mask = 0L;
                for (int t = 0; t < types.size(); t++) {
                    if (types.get(t).compatible().contains(id)) {
                        mask |= 1L << t;
                    }
                }
                long[] g = groups.computeIfAbsent(mask, m -> new long[5]);       // Σvolume, Σweight, Σvisits, max volume, max weight
                g[0] = Math.addExact(g[0], request.totalVolume());
                g[1] = Math.addExact(g[1], request.totalWeight());
                g[2] += visitCount(request);
                g[3] = Math.max(g[3], request.totalVolume());
                g[4] = Math.max(g[4], request.totalWeight());
                total = Math.addExact(total, request.totalVolume());
            }
            int n = groups.size();
            long[] prefixMask = new long[n];
            long[] prefixVolume = new long[n];
            long[] prefixWeight = new long[n];
            long[] prefixVisits = new long[n];
            long[] groupMask = new long[n];
            long[] groupMaxVolume = new long[n];
            long[] groupMaxWeight = new long[n];
            int k = 0;
            long mask = 0L;
            long volume = 0L;
            long weight = 0L;
            long visits = 0L;
            for (Map.Entry<Long, long[]> entry : groups.entrySet()) {
                mask |= entry.getKey();
                volume = Math.addExact(volume, entry.getValue()[0]);
                weight = Math.addExact(weight, entry.getValue()[1]);
                visits += entry.getValue()[2];
                prefixMask[k] = mask;
                prefixVolume[k] = volume;
                prefixWeight[k] = weight;
                prefixVisits[k] = visits;
                groupMask[k] = entry.getKey();
                groupMaxVolume[k] = entry.getValue()[3];
                groupMaxWeight[k] = entry.getValue()[4];
                k++;
            }
            return new Demand(types, total, weight, visits, mask, prefixMask, prefixVolume, prefixWeight, prefixVisits,
                    groupMask, groupMaxVolume, groupMaxWeight);
        }

        boolean covers(int[] s) {
            for (int k = 0; k < prefixMask.length; k++) {
                long volume = 0L;
                long weight = 0L;
                long visits = 0L;
                boolean unlimitedVisits = false;
                for (int t = 0; t < types.size(); t++) {
                    if (s[t] == 0 || (prefixMask[k] & (1L << t)) == 0) {
                        continue;
                    }
                    VehicleType type = types.get(t);
                    volume = Math.addExact(volume, s[t] * type.maxVolume());
                    weight = Math.addExact(weight, s[t] * type.maxWeight());
                    if (type.maxStopCount().isPresent()) {
                        visits += (long) s[t] * type.maxStopCount().getAsInt();
                    } else {
                        unlimitedVisits = true;
                    }
                }
                if (volume < prefixVolume[k] || weight < prefixWeight[k] || (!unlimitedVisits && visits < prefixVisits[k])) {
                    return false;
                }
                boolean fitsLargest = false;
                for (int t = 0; t < types.size() && !fitsLargest; t++) {
                    fitsLargest = s[t] > 0 && (groupMask[k] & (1L << t)) != 0
                            && types.get(t).maxVolume() >= groupMaxVolume[k] && types.get(t).maxWeight() >= groupMaxWeight[k];
                }
                if (!fitsLargest) {
                    return false;
                }
            }
            return true;
        }
    }
}
