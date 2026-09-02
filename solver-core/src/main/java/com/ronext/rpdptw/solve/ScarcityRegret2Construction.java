package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H1 — 희소성 우선 regret-2 (heuristics 문서 §5 H1). */
public final class ScarcityRegret2Construction implements ConstructionHeuristic {

    static final String ID = "scarcity-regret2";

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
        InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        int bound = pending.size();
        int iterations = 0;
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
            Pick best = null;
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {
                List<Candidate> cands = cache.candidates(current, requestId);
                if (cands.isEmpty()) {
                    excluded.add(requestId);                                   // 규칙 (b)
                    continue;
                }
                boolean onlyCandidate = cands.size() == 1;
                long regret = onlyCandidate ? 0L : cands.get(1).deltaDriveDistMeter() - cands.get(0).deltaDriveDistMeter();
                Pick pick = new Pick(requestId, problem.compatibleVehicles(requestId).size(), onlyCandidate, regret, cands.getFirst());
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

    /** 선택 키 = (희소도 ASC, onlyCandidate DESC, regret DESC, RequestId ASC) — big-M 없이 축 분리. */
    private static final Comparator<Pick> PICK_ORDER = Comparator.comparingInt(Pick::scarcity)
            .thenComparing(Pick::onlyCandidate, Comparator.reverseOrder())
            .thenComparing(Pick::regret, Comparator.reverseOrder())
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, int scarcity, boolean onlyCandidate, long regret, Candidate candidate) {}
}
