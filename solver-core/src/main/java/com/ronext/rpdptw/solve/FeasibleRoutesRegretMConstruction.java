package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H9 — 동적 삽입 가능 경로 수 우선 + regret-m (heuristics 문서 §5 H9). */
public final class FeasibleRoutesRegretMConstruction implements ConstructionHeuristic {

    static final String ID = "feasible-routes-regret-m";

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
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        return regretMLoop(problem, profile, InsertionSearch.emptySolution(problem), pending, false, ID, 0, pending.size());
    }

    /**
     * regret-m 루프 — H10이 공유한다. newRouteOnlyIfNoExisting이면 새 경로 후보는 기존 경로 후보가
     * 0개인 요청에만 허용한다 (H10 3단계).
     */
    static Solution regretMLoop(
            Problem problem, Profile profile, Solution current, Set<RequestId> pending,
            boolean newRouteOnlyIfNoExisting, String id, int iterations, int bound) {
        InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(id, ++iterations, bound);
            Pick best = null;
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {
                List<Candidate> cands = cache.candidates(current, requestId);
                if (newRouteOnlyIfNoExisting && cands.stream().anyMatch(c -> !c.newRoute())) {
                    cands = cands.stream().filter(c -> !c.newRoute()).toList();
                }
                if (cands.isEmpty()) {
                    excluded.add(requestId);                                   // 규칙 (b)
                    continue;
                }
                Map<VehicleId, Candidate> bestPerRoute = new LinkedHashMap<>();
                for (Candidate candidate : cands) {                            // 비용 오름차순이라 첫 등장이 경로별 최선
                    bestPerRoute.putIfAbsent(candidate.vehicleId(), candidate);
                }
                long overall = cands.getFirst().deltaDriveDistMeter();
                long regretM = 0L;
                for (Candidate candidate : bestPerRoute.values()) {
                    regretM = Math.addExact(regretM, candidate.deltaDriveDistMeter() - overall);
                }
                Pick pick = new Pick(requestId, bestPerRoute.size(), regretM, cands.getFirst());
                if (best == null || PICK_ORDER.compare(pick, best) < 0) {
                    best = pick;
                }
            }
            pending.removeAll(excluded);
            if (best != null) {
                current = InsertionSearch.apply(problem, current, best.candidate());   // 규칙 (a)
                pending.remove(best.requestId());
                cache.forget(best.requestId());
            }
        }
        return current;
    }

    /** 선택 키 = (feasibleRoutes ASC, regretM DESC, RequestId ASC). */
    private static final Comparator<Pick> PICK_ORDER = Comparator.comparingInt(Pick::feasibleRoutes)
            .thenComparing(Pick::regretM, Comparator.reverseOrder())
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, int feasibleRoutes, long regretM, Candidate candidate) {}
}
