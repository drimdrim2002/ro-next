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
 * 값 = (Σ부족, Σ낭비, Σ사용 대수) 사전식. 상태 수 ≤ Π(대수+1) ≤ MAX_COMBINATIONS — 구조로 종료한다 (§4.3).
 */
final class ZoneQuotaAllocation {

    /** Π(유형별 대수 + 1) 한계 — 넘으면 H23·H24 기권 (§4.4·X20). */
    static final long MAX_COMBINATIONS = 65_536L;
    static final String NO_ZONE = VehicleZoneFillConstruction.NO_ZONE;

    private ZoneQuotaAllocation() {}

    /** 차량 유형 = (호환 Request 집합, maxWeight, maxVolume, effectiveMaxStopCount)이 같은 차량들. 유형 안은 VehicleId ASC. */
    record VehicleType(Set<RequestId> compatible, long maxWeight, long maxVolume,
                       OptionalInt maxStopCount, List<VehicleId> vehicles) {}

    /** 존 = anchor side zoneId(부재 "(none)")별 Request. */
    record Zone(String zoneId, List<RequestId> members) {}

    /** 유형 목록·존 순서대로의 존 목록·존별 배정 차량. 배정 없는 존은 빈 목록. */
    record Allocation(List<VehicleType> types, List<Zone> zones, Map<String, List<VehicleId>> vehiclesByZone) {}

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

    /** Π(대수 + 1) — abstains()가 본다. 한계를 넘으면 그 이상은 세지 않는다. */
    static long combinations(List<VehicleType> types) {
        long product = 1L;
        for (VehicleType type : types) {
            product = Math.multiplyExact(product, type.vehicles().size() + 1L);
            if (product > MAX_COMBINATIONS) {
                return product;
            }
        }
        return product;
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

    static Allocation allocate(Problem problem) {
        List<VehicleType> types = vehicleTypes(problem);
        List<Zone> zones = zones(problem, types);
        int typeCount = types.size();
        int[] counts = new int[typeCount];
        long[] radix = new long[typeCount + 1];
        radix[0] = 1L;
        for (int t = 0; t < typeCount; t++) {
            counts[t] = types.get(t).vehicles().size();
            radix[t + 1] = radix[t] * (counts[t] + 1);
        }

        // 상태 키 = 혼합 진법으로 부호화한 남은 대수 벡터. 배열 인덱스 순회라 순서가 고정된다 (결정성).
        int size = (int) radix[typeCount];
        List<Layer> layers = new ArrayList<>();
        Layer states = new Layer(size);
        states.value[encodeIndex(counts, radix)] = new long[] {0L, 0L, 0L};
        layers.add(states);
        for (Zone zone : zones) {
            Demand demand = Demand.of(problem, types, zone).cached(radix);
            Layer next = new Layer(size);
            for (int from = 0; from < size; from++) {
                long[] base = states.value[from];
                if (base == null) {
                    continue;
                }
                int[] remaining = decode(from, counts, radix);
                for (int[] s : frontier(remaining, demand)) {
                    boolean covers = demand.covers(s);
                    long capacity = 0L;
                    int used = 0;
                    int to = from;
                    for (int t = 0; t < typeCount; t++) {
                        capacity = Math.addExact(capacity, s[t] * types.get(t).maxVolume());
                        used += s[t];
                        to -= (int) (s[t] * radix[t]);
                    }
                    long shortage = covers ? 0L : Math.max(1L, demand.totalVolume - capacity);
                    long waste = covers ? capacity - demand.totalVolume : 0L;
                    long[] value = new long[] {base[0] + shortage, base[1] + waste, base[2] + used};
                    if (next.value[to] == null || compare(value, next.value[to]) < 0) {   // 동률 = 먼저 계산된 값 유지
                        next.value[to] = value;
                        next.parent[to] = from;
                        next.chosen[to] = s;
                    }
                }
            }
            states = next;
            layers.add(states);
        }

        int best = -1;
        for (int key = 0; key < size; key++) {                                   // 동률 = 남은 벡터 사전식 최소 (인덱스 오름차순)
            if (states.value[key] != null && (best < 0 || compare(states.value[key], states.value[best]) < 0)) {
                best = key;
            }
        }
        Map<String, int[]> quotas = new LinkedHashMap<>();
        int key = best;
        for (int z = zones.size() - 1; z >= 0; z--) {
            Layer layer = layers.get(z + 1);
            quotas.put(zones.get(z).zoneId(), layer.chosen[key]);
            key = layer.parent[key];
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
        return new Allocation(types, zones, vehiclesByZone);
    }

    /**
     * 프론티어 — 유형 순서로 재귀 나열(covers가 되는 순간 그 유형에서 중단)한 s 중
     * (a) 최소 덮개 · (b) 한 대 모자란 최대 비덮개 · (c) 빈 집합.
     */
    static List<int[]> frontier(int[] remaining, Demand demand) {
        List<int[]> out = new ArrayList<>();
        enumerate(0, new int[remaining.length], remaining, demand, out);
        return out;
    }

    private static void enumerate(int t, int[] s, int[] remaining, Demand demand, List<int[]> out) {
        if (t == s.length) {
            classify(s, remaining, demand, out);
            return;
        }
        for (int c = 0; c <= remaining[t]; c++) {
            s[t] = c;
            enumerate(t + 1, s, remaining, demand, out);
            if (demand.covers(s)) {                                            // 이후 유형은 0인 상태 — 이미 덮으면 더 늘릴 이유가 없다
                break;
            }
        }
        s[t] = 0;
    }

    private static void classify(int[] s, int[] remaining, Demand demand, List<int[]> out) {
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
            if (remaining[t] > s[t]) {
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

    private static int encodeIndex(int[] vector, long[] radix) {
        long key = 0L;
        for (int t = 0; t < vector.length; t++) {
            key += vector[t] * radix[t];
        }
        return (int) key;
    }

    private static int[] decode(long key, int[] counts, long[] radix) {
        int[] vector = new int[counts.length];
        for (int t = 0; t < counts.length; t++) {
            vector[t] = (int) ((key / radix[t]) % (counts[t] + 1));
        }
        return vector;
    }

    /** DP 한 층 — 인덱스 = 남은 대수 벡터의 부호화 키. value null = 도달 불가. */
    private static final class Layer {
        final long[][] value;
        final int[] parent;
        final int[][] chosen;

        Layer(int size) {
            value = new long[size][];
            parent = new int[size];
            chosen = new int[size][];
        }
    }

    /**
     * 존 수요 — 호환 그룹(|호환 유형 수| ASC, 유형 비트 ASC)의 누적값과 그룹별 최대 단품.
     * covers(s) = 접두마다 부피·무게·방문 수 충족 ∧ 그룹마다 최대 단품을 실을 호환 유형 존재 (Hall 조건).
     */
    static final class Demand {
        private final List<VehicleType> types;
        final long totalVolume;
        private final int[] prefixMask;
        private final long[] prefixVolume;
        private final long[] prefixWeight;
        private final long[] prefixVisits;
        private final int[] groupMask;
        private final long[] groupMaxVolume;
        private final long[] groupMaxWeight;
        private long[] radix;                                                   // covers 결과 캐시 — 벡터 부호화 키 (0 미계산 · 1 거짓 · 2 참)
        private byte[] cache;

        private Demand(List<VehicleType> types, long totalVolume, int[] prefixMask, long[] prefixVolume,
                       long[] prefixWeight, long[] prefixVisits, int[] groupMask, long[] groupMaxVolume, long[] groupMaxWeight) {
            this.types = types;
            this.totalVolume = totalVolume;
            this.prefixMask = prefixMask;
            this.prefixVolume = prefixVolume;
            this.prefixWeight = prefixWeight;
            this.prefixVisits = prefixVisits;
            this.groupMask = groupMask;
            this.groupMaxVolume = groupMaxVolume;
            this.groupMaxWeight = groupMaxWeight;
        }

        static Demand of(Problem problem, List<VehicleType> types, Zone zone) {
            TreeMap<Integer, long[]> groups = new TreeMap<>(
                    Comparator.comparingInt(Integer::bitCount).thenComparing(Comparator.naturalOrder()));
            long total = 0L;
            for (RequestId id : zone.members()) {
                Request request = problem.request(id);
                int mask = 0;
                for (int t = 0; t < types.size(); t++) {
                    if (types.get(t).compatible().contains(id)) {
                        mask |= 1 << t;
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
            int[] prefixMask = new int[n];
            long[] prefixVolume = new long[n];
            long[] prefixWeight = new long[n];
            long[] prefixVisits = new long[n];
            int[] groupMask = new int[n];
            long[] groupMaxVolume = new long[n];
            long[] groupMaxWeight = new long[n];
            int k = 0;
            int mask = 0;
            long volume = 0L;
            long weight = 0L;
            long visits = 0L;
            for (Map.Entry<Integer, long[]> entry : groups.entrySet()) {
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
            return new Demand(types, total, prefixMask, prefixVolume, prefixWeight, prefixVisits, groupMask, groupMaxVolume, groupMaxWeight);
        }

        /** 같은 존 안에서 covers(s)는 s에만 의존한다 — 벡터 수 ≤ Π(대수+1)라 배열 하나로 전부 기억한다. */
        Demand cached(long[] radix) {
            this.radix = radix;
            this.cache = new byte[(int) radix[radix.length - 1]];
            return this;
        }

        boolean covers(int[] s) {
            if (cache == null) {
                return compute(s);
            }
            long key = 0L;
            for (int t = 0; t < s.length; t++) {
                key += s[t] * radix[t];
            }
            int index = (int) key;
            if (cache[index] == 0) {
                cache[index] = compute(s) ? (byte) 2 : (byte) 1;
            }
            return cache[index] == 2;
        }

        private boolean compute(int[] s) {
            for (int k = 0; k < prefixMask.length; k++) {
                long volume = 0L;
                long weight = 0L;
                long visits = 0L;
                boolean unlimitedVisits = false;
                for (int t = 0; t < types.size(); t++) {
                    if (s[t] == 0 || (prefixMask[k] & (1 << t)) == 0) {
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
                    fitsLargest = s[t] > 0 && (groupMask[k] & (1 << t)) != 0
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
