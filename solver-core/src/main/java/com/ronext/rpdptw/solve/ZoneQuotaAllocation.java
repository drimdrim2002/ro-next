package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Arrays;
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
 * 값 = (Σ부족, Σ대수, Σ낭비, Σ결손) 사전식 — 앞 두 축이 정식 score (미배정, 차량 수)와 같은 순서다.
 * 호환 성분마다 독립 실행하고 도달 상태만 보관하며,
 * 성분당 보관 상태 총량이 MAX_TOTAL_STATES를 넘으면 값 순으로 잘라 근사한다 — 기권하지 않는다 (§4.3).
 */
final class ZoneQuotaAllocation {

    /** 성분 하나의 DP가 층 전체에 보관하는 상태 수 총량 (재량 상수). 넘치면 값 순으로 잘라 근사한다. */
    static final int MAX_TOTAL_STATES = 262_144;
    /**
     * 존 하나의 프론티어 열거(존당 1회, ZoneFrontier)가 도달할 수 있는 잎 수 총량 — 시간·메모리 상한 (재량 상수).
     * 소진되면 열거 순서 접두만 남기고 근사한다 (X29) — 모든 상태가 같은 접두를 본다 (X40). 벡터 ≤ 잎이라 존의 공유 목록도
     * 이 상수에 묶인다 (X43). 실물 fixture는 존당 최대 1,933잎을 쓴다 (frontier-budget §2.7).
     */
    static final int MAX_FRONTIER_LEAVES = 4_194_304;
    /**
     * 상태 하나가 받는 후보 수 상한 (재량 상수) — 정렬 접두에서 멈추고 truncated (X42). 전이 수를 상태 × 후보로 묶는다.
     * 실물 상태당 최대 526 위의 2의 거듭제곱 (frontier-budget §2.6).
     */
    static final int MAX_FRONTIER_CANDIDATES = 1_024;
    /** 값 축 수 — (Σ부족, Σ대수, Σ낭비, Σ결손). Layer.values의 stride이자 value()가 돌려주는 배열 길이. */
    static final int VALUE_AXES = 4;
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

    /** 프론티어 열거 결과 — 벡터 목록 · 소비한 잎 수 · 잎 예산에 걸려 잘렸는가 (X29). 래퍼 frontier()의 결과 (T49·T63). */
    record Frontier(List<int[]> vectors, int leaves, boolean truncated) {}

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

    /**
     * 존 전이 하나의 값 (부족, 대수, 낭비, 결손) — s에 든 유형만 합한다 (zone-value-function §2.6).
     * 결손 = max(0, Σs × ⌊Σvolume_z / n_z⌋ − 낭비), Σs = 0이면 0 — "존의 차 한 대마다 평균 단품 하나가 더 들어갈 여유"의 부족분.
     * Demand·types·s 외에는 읽지 않는다 (§2.4 경계 — 이동표·시간창·요청 순서는 2단계 몫).
     */
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
        long deficit = used == 0L ? 0L : Math.max(0L, Math.multiplyExact(used, demand.totalVolume / demand.count) - waste);
        return new long[] {shortage, used, waste, deficit};
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
            int[] box = maxNeedZ.clone();                                          // 층의 모든 R을 덮는 상자 — 존마다 한 번 열거한다 (X40)
            for (int d = 0; d < dims; d++) {
                box[stateDims.get(d)] = Math.min(start[d], maxNeedZ[stateDims.get(d)]);
            }
            ZoneFrontier shared = ZoneFrontier.enumerate(box, demand, MAX_FRONTIER_LEAVES);
            truncated |= shared.truncated();

            for (int i = 0; i < from.size; i++) {                                  // 층은 KEY_ORDER 오름차순
                int[] key = from.key(i);
                System.arraycopy(maxNeedZ, 0, range, 0, typeCount);
                for (int d = 0; d < dims; d++) {
                    range[stateDims.get(d)] = Math.min(key[d], maxNeedZ[stateDims.get(d)]);
                }
                long[] base = from.value(i);
                List<int[]> frontier = shared.forRange(range, MAX_FRONTIER_CANDIDATES);
                truncated |= frontier.size() >= MAX_FRONTIER_CANDIDATES;           // 후보 상한에 닿음 (X42)
                for (int[] s : frontier) {
                    long[] delta = value(demand, types, s);
                    long[] candidate = new long[VALUE_AXES];
                    for (int axis = 0; axis < VALUE_AXES; axis++) {
                        candidate[axis] = base[axis] + delta[axis];
                    }
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
     * 프론티어 — 열거 범위 range 안의 (a) 최소 덮개 · (b) 한 대 모자란 최대 비덮개 · (c) 빈 집합 (래퍼, T49·T63).
     * ZoneFrontier.enumerate(range, demand, cap).forRange(range)와 같다. cap ≥ 1이라 (c)는 반드시 나온다 (X29).
     * DP 본체는 이 함수가 아니라 ZoneFrontier를 존마다 한 번 부른다.
     */
    static Frontier frontier(int[] range, Demand demand, int cap) {
        ZoneFrontier shared = ZoneFrontier.enumerate(range, demand, cap);
        return new Frontier(List.copyOf(shared.forRange(range, Integer.MAX_VALUE)), shared.leaves(), shared.truncated());
    }

    /**
     * 존 하나의 공유 프론티어 (frontier-budget §2.5) — 상자 box로 한 번 열거한 (c)·(a)·"(b) 후보"(확장 마스크 포함)를
     * 열거 순서 = 사전식(유형 index 0부터, 대수 ASC) 배열로 보관한다. forRange(R)는 s ≤ R을 접두 범위 탐색으로 뽑고
     * (b)는 R 기준으로 재판정한다 — R마다 따로 열거한 것과 같은 집합이다: 가지치기가 s에만 의존해 방문 집합이 상자에
     * 단조이고, (a)·(c)는 covers(s)만 보며, (b)_R(s) ⇔ ¬covers(s) ∧ ∃t R_t > s_t ∧ ∀t (R_t = s_t ∨ t ∈ mask(s)) 이기
     * 때문이다 (X41). 잎 예산 cap에 닿으면 열거 순서 접두만 남고 truncated — 모든 상태가 같은 접두를 본다 (X40).
     */
    static final class ZoneFrontier {
        private final int[][] vectors;                                          // 사전식 = 열거 순서 (정렬 불필요)
        private final long[] masks;                                             // (b) 후보: 덮게 하는 확장 t의 비트 (1L << t). (a)·(c)는 -1
        private final int leaves;
        private final boolean truncated;

        private ZoneFrontier(int[][] vectors, long[] masks, int leaves, boolean truncated) {
            this.vectors = vectors;
            this.masks = masks;
            this.leaves = leaves;
            this.truncated = truncated;
        }

        /** cap은 이 열거가 도달해도 되는 잎 수(≥ 1) — 열거 순서상 첫 잎이 빈 집합이라 cap = 1이어도 (c)는 나온다 (X29). */
        static ZoneFrontier enumerate(int[] box, Demand demand, int cap) {
            Enumeration enumeration = new Enumeration(Math.max(1, cap));
            walk(0, new int[box.length], box, demand, enumeration);
            return new ZoneFrontier(enumeration.out.toArray(int[][]::new), Arrays.copyOf(enumeration.masks, enumeration.out.size()),
                    enumeration.leaves, enumeration.truncated);
        }

        int leaves() {
            return leaves;
        }

        boolean truncated() {
            return truncated;
        }

        /**
         * 상태 R의 프론티어 — 사전식 배열 위 접두 범위 탐색 (유형 t에서 s_t ≤ R_t인 구간만 내려간다).
         * candidateCap개에 이르면 멈춘다 (정렬 접두, X42). (c)는 첫 원소라 항상 든다.
         */
        List<int[]> forRange(int[] range, int candidateCap) {
            List<int[]> out = new ArrayList<>();
            collect(0, vectors.length, 0, range, candidateCap, out);
            return out;
        }

        private void collect(int lo, int hi, int t, int[] range, int cap, List<int[]> out) {
            if (t == range.length) {
                for (int i = lo; i < hi && out.size() < cap; i++) {
                    if (masks[i] < 0L || acceptsAsB(vectors[i], masks[i], range)) {
                        out.add(vectors[i]);
                    }
                }
                return;
            }
            int pos = lo;
            while (pos < hi && out.size() < cap) {
                int c = vectors[pos][t];
                if (c > range[t]) {
                    return;                                                     // 같은 접두 안에서 s_t는 오름차순
                }
                int end = upperBound(pos, hi, t, c);
                collect(pos, end, t + 1, range, cap, out);
                pos = end;
            }
        }

        /** [lo, hi) 안에서 vectors[i][t] ≤ value인 마지막 다음 index (같은 접두 안에서 s_t는 오름차순). */
        private int upperBound(int lo, int hi, int t, int value) {
            int left = lo;
            int right = hi;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (vectors[mid][t] <= value) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            return left;
        }

        /** (b)_R — ∃t R_t > s_t ∧ ∀t (R_t = s_t ∨ t ∈ mask). R_t = s_t < B_t인 t는 조건에서 빠진다 (X41). */
        private static boolean acceptsAsB(int[] s, long mask, int[] range) {
            boolean extendable = false;
            for (int t = 0; t < s.length; t++) {
                if (range[t] > s[t]) {
                    extendable = true;
                    if ((mask & (1L << t)) == 0L) {
                        return false;
                    }
                }
            }
            return extendable;
        }

        /** 열거 중 상태 — 남은 잎 예산·모은 벡터와 마스크·절단 여부. 잎 예산이 시간과 메모리를 동시에 묶는다 (X29). */
        private static final class Enumeration {
            private final int cap;
            private final List<int[]> out = new ArrayList<>();
            private long[] masks = new long[64];
            private int leaves;
            private boolean truncated;

            private Enumeration(int cap) {
                this.cap = cap;
            }

            /** 잎 하나를 소비한다. 예산이 남아 있으면 true. */
            boolean visitLeaf() {
                if (leaves == cap) {
                    truncated = true;
                    return false;
                }
                leaves++;
                return true;
            }

            boolean exhausted() {
                return leaves == cap;
            }

            void keep(int[] s, long mask) {
                if (out.size() == masks.length) {
                    masks = Arrays.copyOf(masks, masks.length * 2);
                }
                masks[out.size()] = mask;
                out.add(s.clone());
            }
        }

        /** 유형 순서로 대수를 0..box_t 늘리며 재귀 나열 — 뒤 유형이 0인 채로 이미 덮이면 그 유형은 더 늘리지 않는다 (가지치기는 s에만 의존). */
        private static void walk(int t, int[] s, int[] box, Demand demand, Enumeration enumeration) {
            if (t == s.length) {
                if (enumeration.visitLeaf()) {
                    classify(s, box, demand, enumeration);
                }
                return;
            }
            for (int c = 0; c <= box[t]; c++) {
                s[t] = c;
                walk(t + 1, s, box, demand, enumeration);
                if (enumeration.exhausted()) {
                    enumeration.truncated |= c < box[t];                        // 남은 대안을 버렸으면 근사다 (X29)
                    break;
                }
                if (demand.covers(s)) {                                         // 이후 유형은 0인 상태 — 이미 덮으면 더 늘릴 이유가 없다
                    break;
                }
            }
            s[t] = 0;
        }

        /** (c) s = 0 · (a) covers ∧ ¬covers(s − e_min) · (b) 후보: ¬covers이며 어떤 확장 t(box_t > s_t)가 덮는다 — 그 t들의 마스크와 함께. */
        private static void classify(int[] s, int[] box, Demand demand, Enumeration enumeration) {
            int typeCount = s.length;
            boolean empty = true;
            for (int c : s) {
                empty &= c == 0;
            }
            if (empty) {
                enumeration.keep(s, -1L);                                       // (c)
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
                    enumeration.keep(s, -1L);                                   // (a)
                }
                return;
            }
            long mask = 0L;
            for (int t = 0; t < typeCount; t++) {
                if (box[t] > s[t]) {
                    probe[t]++;
                    if (demand.covers(probe)) {
                        mask |= 1L << t;
                    }
                    probe[t]--;
                }
            }
            if (mask != 0L) {
                enumeration.keep(s, mask);                                      // (b) 후보 — R 기준 판정은 forRange에서
            }
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

    /** DP 한 층 — 도달 상태만 평면 배열로 (키 dims개 · 값 VALUE_AXES개 · parent · 풍부 유형 대수). KEY_ORDER 오름차순. */
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
            this.values = new long[size * VALUE_AXES];
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
                System.arraycopy(entry.getValue().value(), 0, layer.values, i * VALUE_AXES, VALUE_AXES);
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
            long[] value = new long[VALUE_AXES];
            System.arraycopy(values, index * VALUE_AXES, value, 0, VALUE_AXES);
            return value;
        }
    }

    /**
     * 존 수요 — 호환 그룹(|호환 유형 수| ASC, 유형 비트 ASC)의 누적값과 그룹별 최대 단품.
     * covers(s) = 접두마다 부피·무게·방문 수 충족 ∧ 그룹마다 최대 단품을 실을 호환 유형 존재 (Hall 조건).
     */
    static final class Demand {
        private final List<VehicleType> types;
        final int count;                                                    // 존의 요청 수 — 평균 단품 부피 = totalVolume / count
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

        private Demand(List<VehicleType> types, int count, long totalVolume, long totalWeight, long totalVisits, long compat,
                       long[] prefixMask, long[] prefixVolume, long[] prefixWeight, long[] prefixVisits,
                       long[] groupMask, long[] groupMaxVolume, long[] groupMaxWeight) {
            this.types = types;
            this.count = count;
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
            return new Demand(types, zone.members().size(), total, weight, visits, mask,
                    prefixMask, prefixVolume, prefixWeight, prefixVisits, groupMask, groupMaxVolume, groupMaxWeight);
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
