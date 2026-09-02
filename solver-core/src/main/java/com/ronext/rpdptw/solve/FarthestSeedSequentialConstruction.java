package com.ronext.rpdptw.solve;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H5 — 최원거리 seed 경로별 순차 삽입 (heuristics 문서 §5 H5). 경로를 하나씩 완성한다. */
public final class FarthestSeedSequentialConstruction implements ConstructionHeuristic {

    static final String ID = "farthest-seed-sequential";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return problem.depots().size() != 1;                                   // "그 depot"이 정의되지 않는다
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        LocationId depot = problem.depots().getFirst().locationId();
        Solution current = InsertionSearch.emptySolution(problem);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        int bound = pending.size();
        int iterations = 0;
        Comparator<RequestId> seedOrder = Comparator
                .<RequestId>comparingInt(id -> problem.travel().distanceMeter(
                        depot, InsertionSearch.anchor(problem.request(id)).locationId()))
                .reversed()
                .thenComparing(InsertionSearch.BY_REQUEST_ID);
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
            RequestId seed = pending.stream().min(seedOrder).orElseThrow();
            Optional<VehicleId> vehicle = InsertionSearch.firstUnusedCompatible(
                    problem, seed, InsertionSearch.usedVehicles(current));
            Solution base = current;
            Optional<Solution> opened = vehicle.flatMap(v -> InsertionSearch.openRoute(problem, profile, base, seed, v));
            pending.remove(seed);
            if (opened.isEmpty()) {
                continue;                                                       // 규칙 (b)
            }
            current = opened.get();                                             // 규칙 (a)
            current = fillRoute(problem, profile, current, vehicle.get(), pending, ID, iterations, bound);
        }
        return current;
    }

    /** 이 경로에 넣을 수 있는 것 가운데 (byCost, RequestId) 최소를 더 못 넣을 때까지 삽입 — H5·H8 공유. */
    static Solution fillRoute(
            Problem problem, Profile profile, Solution current, VehicleId vehicleId,
            Set<RequestId> pending, String id, int iterations, int bound) {
        while (true) {
            RequestId bestId = null;
            Candidate best = null;
            for (RequestId requestId : pending) {
                List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicleId);
                if (cands.isEmpty()) {
                    continue;
                }
                Candidate first = cands.getFirst();
                if (best == null || Candidate.byCost().compare(first, best) < 0) {
                    best = first;
                    bestId = requestId;
                }
            }
            if (best == null) {
                return current;                                                 // 경로 확정
            }
            InsertionSearch.checkOuterLoop(id, ++iterations, bound);
            current = InsertionSearch.apply(problem, current, best);            // 규칙 (a)
            pending.remove(bestId);
        }
    }
}
