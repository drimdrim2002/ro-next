package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

/** H24 — 존 배정 DP + seed pool subset-sum DP + 오라클 피드백 (heuristics 문서 §5 H24). */
public final class ZoneQuotaSubsetFillConstruction implements ConstructionHeuristic {

    static final String ID = "zone-quota-subset-fill";
    /** 재량 상수 — 부피 양자화 칸 수 (u = ⌈maxVolume / 20,000⌉). */
    static final long VOLUME_BUCKETS = 20_000L;
    /** 재량 상수 — pool 부피 목표 = ⌈1.5 × maxVolume⌉. */
    static final long POOL_NUMERATOR = 3L;
    static final long POOL_DENOMINATOR = 2L;
    /** 재량 상수 — 정차 하한 비례 0.8 = 8/10. */
    static final long LOWER_BOUND_NUMERATOR = 8L;
    static final long LOWER_BOUND_DENOMINATOR = 10L;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        Solution current = InsertionSearch.emptySolution(problem);
        List<RequestId> leftovers = new ArrayList<>();
        for (Zone zone : allocation.zones()) {
            List<RequestId> unassigned = new ArrayList<>(zone.members());
            List<Vehicle> bins = binOrder(problem, allocation.vehiclesByZone().get(zone.zoneId()), zone.members());
            for (int i = 0; i < bins.size(); i++) {
                Vehicle vehicle = bins.get(i);
                List<RequestId> candidates = new ArrayList<>();
                for (RequestId id : unassigned) {
                    if (problem.compatibleVehicles(id).contains(vehicle.id())) {
                        candidates.add(id);
                    }
                }
                if (candidates.isEmpty()) {
                    continue;
                }
                List<RequestId> pool = pool(problem, vehicle, candidates);
                int nmax = vehicle.effectiveMaxStopCount().isPresent()
                        ? Math.min(vehicle.effectiveMaxStopCount().getAsInt(), pool.size()) : pool.size();
                int nmin = Math.min(nmax, lowerBound(bins, i, unassigned.size()));
                Set<RequestId> excluded = new LinkedHashSet<>();
                int iterations = 0;
                while (true) {
                    InsertionSearch.checkOuterLoop(ID, ++iterations, pool.size() + 1);     // 반복마다 제외 +1 또는 nmax −1
                    List<RequestId> items = new ArrayList<>(pool);
                    items.removeAll(excluded);
                    if (items.isEmpty() || nmax == 0) {
                        break;
                    }
                    List<RequestId> chosen = subsetSum(problem, vehicle, items, nmin, nmax);
                    if (chosen.isEmpty()) {
                        break;
                    }
                    Solution trial = current;
                    List<RequestId> failed = new ArrayList<>();
                    for (RequestId id : sortedByVolumeDesc(problem, chosen)) {
                        List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, trial, id, vehicle.id());
                        if (cands.isEmpty()) {
                            failed.add(id);
                        } else {
                            trial = InsertionSearch.apply(problem, trial, cands.getFirst());
                        }
                    }
                    if (failed.isEmpty()) {
                        current = trial;                                                // bin 확정
                        unassigned.removeAll(chosen);
                        break;
                    }
                    excluded.addAll(failed);                                            // 이 반복의 삽입은 버린다 (해는 불변)
                    nmax = chosen.size() - failed.size();
                    if (nmax < Math.max(1, nmin)) {
                        nmin = 0;
                    }
                }
            }
            for (RequestId id : new ArrayList<>(unassigned)) {                          // 남은 요청 → 존 경로에 첫 성공
                for (Vehicle vehicle : bins) {
                    List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, id, vehicle.id());
                    if (!cands.isEmpty()) {
                        current = InsertionSearch.apply(problem, current, cands.getFirst());
                        unassigned.remove(id);
                        break;
                    }
                }
            }
            leftovers.addAll(unassigned);
        }
        int iterations = 0;
        for (RequestId id : ZoneQuotaBalancedFillConstruction.requestOrder(problem, allocation, leftovers)) {   // leftover pass
            InsertionSearch.checkOuterLoop(ID, ++iterations, problem.requests().size());
            List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, id);
            if (!cands.isEmpty()) {
                current = InsertionSearch.apply(problem, current, cands.getFirst());
            }
        }
        return current;
    }

    /** bins 순서 = (존 요청 중 호환 수 ASC, maxVolume DESC, VehicleId ASC) — 까다로운 차부터. */
    private static List<Vehicle> binOrder(Problem problem, List<VehicleId> assigned, List<RequestId> members) {
        List<Vehicle> bins = new ArrayList<>();
        for (VehicleId id : assigned) {
            bins.add(problem.vehicle(id));
        }
        bins.sort(Comparator.<Vehicle>comparingInt(v -> {
                    int count = 0;
                    for (RequestId id : members) {
                        if (problem.compatibleVehicles(id).contains(v.id())) {
                            count++;
                        }
                    }
                    return count;
                })
                .thenComparing(Comparator.comparingLong(Vehicle::maxVolume).reversed())
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return bins;
    }

    /**
     * pool = seed(기준 depot에서 이동시간 최대)로부터 이동시간 ASC로 Σvolume ≥ ⌈1.5 × maxVolume⌉이 되는 최소 접두,
     * 단 ≥ 정차 한도 개 (부재면 후보 전부). 좌표가 아니라 TravelMatrix 시간이다 (Domain §4).
     */
    private static List<RequestId> pool(Problem problem, Vehicle vehicle, List<RequestId> candidates) {
        int speed = problem.resolvedSpeedKmH(vehicle.id());
        Optional<LocationId> reference = vehicle.startDepot().or(vehicle::endDepot);
        RequestId seed = candidates.getFirst();
        if (reference.isPresent()) {
            seed = candidates.stream()
                    .max(Comparator.<RequestId>comparingInt(id -> problem.travel().timeSec(reference.get(), anchorLocation(problem, id), speed))
                            .thenComparing(InsertionSearch.BY_REQUEST_ID.reversed()))
                    .orElseThrow();
        }
        LocationId seedLocation = anchorLocation(problem, seed);
        List<RequestId> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.<RequestId>comparingInt(id -> problem.travel().timeSec(seedLocation, anchorLocation(problem, id), speed))
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        if (vehicle.effectiveMaxStopCount().isEmpty()) {
            return ordered;
        }
        long target = (POOL_NUMERATOR * vehicle.maxVolume() + POOL_DENOMINATOR - 1) / POOL_DENOMINATOR;
        int minimum = vehicle.effectiveMaxStopCount().getAsInt();
        List<RequestId> pool = new ArrayList<>();
        long volume = 0L;
        for (RequestId id : ordered) {
            pool.add(id);
            volume = Math.addExact(volume, problem.request(id).totalVolume());
            if (volume >= target && pool.size() >= minimum) {
                break;
            }
        }
        return pool;
    }

    private static LocationId anchorLocation(Problem problem, RequestId id) {
        return InsertionSearch.anchor(problem.request(id)).locationId();
    }

    /** nmin = max(|미적재| − Σ_{뒤 bin} B, ⌊0.8 × |미적재| × maxVolume_b / Σ_{i 이후 bin} maxVolume⌋). 뒤 bin에 한도 부재가 있으면 첫 항은 0. */
    private static int lowerBound(List<Vehicle> bins, int index, int remaining) {
        long laterStops = 0L;
        boolean unlimited = false;
        long capacitySum = 0L;
        for (int j = index; j < bins.size(); j++) {
            capacitySum = Math.addExact(capacitySum, bins.get(j).maxVolume());
            if (j > index) {
                if (bins.get(j).effectiveMaxStopCount().isPresent()) {
                    laterStops += bins.get(j).effectiveMaxStopCount().getAsInt();
                } else {
                    unlimited = true;
                }
            }
        }
        long byStops = unlimited ? 0L : Math.max(0L, remaining - laterStops);
        long proportional = capacitySum == 0L ? 0L
                : (LOWER_BOUND_NUMERATOR * remaining * bins.get(index).maxVolume()) / (LOWER_BOUND_DENOMINATOR * capacitySum);
        return (int) Math.max(byStops, proportional);
    }

    private static List<RequestId> sortedByVolumeDesc(Problem problem, List<RequestId> ids) {
        List<RequestId> order = new ArrayList<>(ids);
        order.sort(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume()).reversed()
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }

    /**
     * subsetSum — 상태 f[v][n] = 양자화 부피 합 v·방문 수 합 n인 부분집합의 최소 weight (maxWeight 초과는 버린다).
     * 부피는 올림 양자화라 절대 넘치지 않는다. 선택 = (v DESC, n ASC) 첫 상태, nmin ≤ n ≤ nmax — 없으면 nmin = 0으로 다시.
     * 정차 한도 부재면 n 축이 없다 (X23). items는 RequestId ASC로 받는다.
     */
    static List<RequestId> subsetSum(Problem problem, Vehicle vehicle, List<RequestId> items, int nmin, int nmax) {
        long unit = Math.max(1L, (vehicle.maxVolume() + VOLUME_BUCKETS - 1L) / VOLUME_BUCKETS);
        int capacity = (int) (vehicle.maxVolume() / unit);
        boolean counted = vehicle.effectiveMaxStopCount().isPresent();
        int n = counted ? nmax : 0;
        int width = n + 1;
        long[] f = new long[(capacity + 1) * width];
        Arrays.fill(f, Long.MAX_VALUE);
        f[0] = 0L;
        int[] volumes = new int[items.size()];
        int[] visits = new int[items.size()];
        BitSet[] taken = new BitSet[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Request request = problem.request(items.get(i));
            long quantized = Math.max(1L, (request.totalVolume() + unit - 1L) / unit);
            volumes[i] = quantized > capacity ? capacity + 1 : (int) quantized;        // 혼자서도 못 드는 것은 선택 불가
            visits[i] = counted ? ZoneQuotaAllocation.visitCount(request) : 0;
            taken[i] = new BitSet(f.length);
            long weight = request.totalWeight();
            for (int v = capacity; v >= volumes[i]; v--) {
                for (int c = n; c >= visits[i]; c--) {
                    long source = f[(v - volumes[i]) * width + (c - visits[i])];
                    if (source == Long.MAX_VALUE) {
                        continue;
                    }
                    long candidate = source + weight;
                    int index = v * width + c;
                    if (candidate <= vehicle.maxWeight() && candidate < f[index]) {
                        f[index] = candidate;
                        taken[i].set(index);
                    }
                }
            }
        }
        int[] selected = select(f, capacity, width, counted ? nmin : 0, n);
        if (selected == null && nmin > 0) {
            selected = select(f, capacity, width, 0, n);
        }
        List<RequestId> chosen = new ArrayList<>();
        if (selected == null) {
            return chosen;
        }
        int v = selected[0];
        int c = selected[1];
        for (int i = items.size() - 1; i >= 0; i--) {
            if (taken[i].get(v * width + c)) {
                chosen.add(items.get(i));
                v -= volumes[i];
                c -= visits[i];
            }
        }
        chosen.sort(InsertionSearch.BY_REQUEST_ID);
        return chosen;
    }

    private static int[] select(long[] f, int capacity, int width, int nmin, int nmax) {
        for (int v = capacity; v >= 1; v--) {
            for (int c = nmin; c <= nmax; c++) {
                if (f[v * width + c] != Long.MAX_VALUE) {
                    return new int[] {v, c};
                }
            }
        }
        return null;
    }
}
