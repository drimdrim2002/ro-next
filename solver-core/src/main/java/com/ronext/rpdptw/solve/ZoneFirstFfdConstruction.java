package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H6 — zone 분할 → FFD 용량 bin → bin 내부 regret-2 (heuristics 문서 §5 H6). */
public final class ZoneFirstFfdConstruction implements ConstructionHeuristic {

    static final String ID = "zone-first-ffd";

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
        // 1단계 — 분할 (용량만 본다. 시간창은 2단계가 판정한다)
        Set<RequestId> all = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        Map<String, List<RequestId>> zones = VehicleZoneFillConstruction.zoneGroups(problem, all);
        List<String> zoneOrder = new ArrayList<>(zones.keySet());
        zoneOrder.sort(Comparator.<String>comparingInt(z -> VehicleZoneFillConstruction.commonVehicles(problem, zones.get(z)).size())
                .thenComparing(Comparator.<String>comparingLong(z -> totalWeight(problem, zones.get(z))).reversed())
                .thenComparing(Comparator.naturalOrder()));

        long largestWeight = 0L;
        long largestVolume = 0L;
        for (Vehicle vehicle : problem.vehicles()) {
            largestWeight = Math.max(largestWeight, vehicle.maxWeight());
            largestVolume = Math.max(largestVolume, vehicle.maxVolume());
        }
        List<Vehicle> vehicleOrder = InsertionSearch.vehiclesByCapacityDesc(problem);
        List<Bin> bins = new ArrayList<>();
        Set<VehicleId> used = new LinkedHashSet<>();
        for (String zoneId : zoneOrder) {
            List<RequestId> members = new ArrayList<>(zones.get(zoneId));
            long lw = largestWeight;
            long lv = largestVolume;
            members.sort(Comparator.<RequestId>comparingDouble(id -> sizeRatio(problem.request(id), lw, lv)).reversed()
                    .thenComparing(InsertionSearch.BY_REQUEST_ID));
            for (RequestId requestId : members) {
                Request request = problem.request(requestId);
                Bin fit = null;
                for (Bin bin : bins) {
                    if (bin.accepts(problem, request)) {
                        fit = bin;
                        break;
                    }
                }
                if (fit == null) {
                    for (Vehicle vehicle : vehicleOrder) {
                        if (!used.contains(vehicle.id()) && problem.compatibleVehicles(requestId).contains(vehicle.id())
                                && request.totalWeight() <= vehicle.maxWeight()
                                && request.totalVolume() <= vehicle.maxVolume()) {
                            fit = new Bin(vehicle);
                            bins.add(fit);
                            used.add(vehicle.id());
                            break;
                        }
                    }
                }
                if (fit != null) {
                    fit.add(request);
                }
                // 어느 bin에도 못 들면 bank
            }
        }

        // 2단계 — 순서: bin 안에서 그 차량 하나만 대상으로 regret-2
        Solution current = InsertionSearch.emptySolution(problem);
        int iterations = 0;
        for (Bin bin : bins) {
            current = regret2Fill(problem, profile, current, bin.vehicle.id(), bin.members, ID, iterations, all.size());
            iterations += bin.members.size();
        }
        return current;
    }

    /**
     * H1의 regret-2를 차량 하나만 대상으로 — 선택 키 (희소도 ASC, onlyCandidate DESC, regret DESC, RequestId ASC).
     * 용량은 맞지만 시간창 때문에 못 들어가는 Request는 bank에 남긴다 (규칙 b). H21이 공유한다.
     */
    static Solution regret2Fill(
            Problem problem, Profile profile, Solution current, VehicleId vehicleId,
            List<RequestId> members, String id, int iterations, int bound) {
        Set<RequestId> pending = new LinkedHashSet<>();
        List<RequestId> sorted = new ArrayList<>(members);
        sorted.sort(InsertionSearch.BY_REQUEST_ID);
        pending.addAll(sorted);
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(id, ++iterations, bound);
            Pick best = null;
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {
                List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicleId);
                if (cands.isEmpty()) {
                    excluded.add(requestId);
                    continue;
                }
                boolean only = cands.size() == 1;
                long regret = only ? 0L : cands.get(1).deltaDriveDistMeter() - cands.get(0).deltaDriveDistMeter();
                Pick pick = new Pick(requestId, problem.compatibleVehicles(requestId).size(), only, regret, cands.getFirst());
                if (best == null || PICK_ORDER.compare(pick, best) < 0) {
                    best = pick;
                }
            }
            pending.removeAll(excluded);
            if (best != null) {
                current = InsertionSearch.apply(problem, current, best.candidate());
                pending.remove(best.requestId());
            }
        }
        return current;
    }

    private static final Comparator<Pick> PICK_ORDER = Comparator.comparingInt(Pick::scarcity)
            .thenComparing(Pick::onlyCandidate, Comparator.reverseOrder())
            .thenComparing(Pick::regret, Comparator.reverseOrder())
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, int scarcity, boolean onlyCandidate, long regret, Candidate candidate) {}

    private static long totalWeight(Problem problem, List<RequestId> ids) {
        long total = 0L;
        for (RequestId id : ids) {
            total = Math.addExact(total, problem.request(id).totalWeight());
        }
        return total;
    }

    /** FFD 정렬 키 = max(weight / 최대차 maxWeight, volume / 최대차 maxVolume) — 정렬 키로만 쓴다. */
    private static double sizeRatio(Request request, long largestWeight, long largestVolume) {
        double w = largestWeight == 0L ? 0.0 : (double) request.totalWeight() / (double) largestWeight;
        double v = largestVolume == 0L ? 0.0 : (double) request.totalVolume() / (double) largestVolume;
        return Math.max(w, v);
    }

    private static final class Bin {
        final Vehicle vehicle;
        final List<RequestId> members = new ArrayList<>();
        long weight;
        long volume;

        Bin(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        boolean accepts(Problem problem, Request request) {
            return problem.compatibleVehicles(request.id()).contains(vehicle.id())
                    && weight + request.totalWeight() <= vehicle.maxWeight()
                    && volume + request.totalVolume() <= vehicle.maxVolume();
        }

        void add(Request request) {
            members.add(request.id());
            weight = Math.addExact(weight, request.totalWeight());
            volume = Math.addExact(volume, request.totalVolume());
        }
    }
}
