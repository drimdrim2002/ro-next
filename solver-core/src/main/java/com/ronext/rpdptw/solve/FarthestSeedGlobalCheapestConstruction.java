package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H17 — farthest seed + 전역 최저 삽입, OR-Tools PCI (heuristics 문서 §5 H17). */
public final class FarthestSeedGlobalCheapestConstruction implements ConstructionHeuristic {

    static final String ID = "farthest-seed-global-cheapest";
    /** 재량 상수 — seed를 심는 차량 비율 (OR-Tools cheapest_insertion_farthest_seeds_ratio 대응, Stage 8 조정). */
    static final double SEED_RATIO = 0.25;

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
        Solution current = InsertionSearch.emptySolution(problem);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        int bound = pending.size();
        int iterations = 0;

        // seed 단계: 차량 (maxWeight DESC, VehicleId ASC) 순 ⌈0.25·m⌉대에 가장 먼 요청을 심는다
        List<Vehicle> vehicles = InsertionSearch.vehiclesByCapacityDesc(problem);
        int seedVehicles = (int) Math.ceil(SEED_RATIO * vehicles.size());
        for (int v = 0; v < seedVehicles && v < vehicles.size(); v++) {
            Vehicle vehicle = vehicles.get(v);
            Optional<LocationId> origin = vehicle.startDepot().or(vehicle::endDepot);
            if (origin.isEmpty()) {
                continue;                                                       // 둘 다 없으면 그 차량 seed 생략
            }
            List<RequestId> order = new ArrayList<>();
            for (RequestId requestId : pending) {
                if (problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                    order.add(requestId);
                }
            }
            LocationId from = origin.get();
            order.sort(Comparator.<RequestId>comparingInt(id -> problem.travel().distanceMeter(
                            from, InsertionSearch.anchor(problem.request(id)).locationId())).reversed()
                    .thenComparing(InsertionSearch.BY_REQUEST_ID));
            for (RequestId requestId : order) {
                Optional<Solution> opened = InsertionSearch.openRoute(problem, profile, current, requestId, vehicle.id());
                if (opened.isPresent()) {
                    InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                    current = opened.get();
                    pending.remove(requestId);
                    break;
                }
            }
        }

        // 본 단계: 매번 모든 (요청, 위치) 중 전역 최저 (byCost, RequestId ASC)
        InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
            RequestId bestId = null;
            Candidate best = null;
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {                              // RequestId 순 — 동률은 앞선 것
                List<Candidate> cands = cache.candidates(current, requestId);
                if (cands.isEmpty()) {
                    excluded.add(requestId);                                   // 규칙 (b)
                    continue;
                }
                if (best == null || Candidate.byCost().compare(cands.getFirst(), best) < 0) {
                    best = cands.getFirst();
                    bestId = requestId;
                }
            }
            pending.removeAll(excluded);
            if (best != null) {
                current = InsertionSearch.apply(problem, current, best);        // 규칙 (a)
                pending.remove(bestId);
                cache.forget(bestId);
            }
        }
        return current;
    }
}
