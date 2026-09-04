package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/**
 * regret-2 — 차선과의 격차(ΔdriveDistMeter)가 큰 Request부터 최선 위치에 삽입 (stage-04 §4.4).
 * 차선이 없으면 +∞ 취급. 동률은 RequestId 문자열 순 — rng를 쓰지 않는다 (결정적).
 * 삽입마다 바뀐 경로만 InsertionSearch.Cache가 재전파한다 (N4 — 대조는 heuristics T13b).
 */
public final class RegretInsertion implements RepairOperator {

    static final String ID = "regret-insertion";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Solution repair(Problem problem, Profile profile, Solution destroyed, RandomGenerator rng) {
        List<RequestId> sorted = new ArrayList<>(destroyed.bank());
        sorted.sort(InsertionSearch.BY_REQUEST_ID);
        Set<RequestId> pending = new LinkedHashSet<>(sorted);
        InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
        Solution current = destroyed;
        while (!pending.isEmpty()) {
            RequestId bestId = null;
            long bestRegret = Long.MIN_VALUE;
            Candidate bestCandidate = null;
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {
                List<Candidate> cands = cache.candidates(current, requestId);
                if (cands.isEmpty()) {
                    excluded.add(requestId);                              // 후보 0개 — bank에 남긴다
                    continue;
                }
                long regret = cands.size() == 1
                        ? Long.MAX_VALUE
                        : cands.get(1).deltaDriveDistMeter() - cands.get(0).deltaDriveDistMeter();
                if (regret > bestRegret) {                                // 동률은 앞선(문자열 순) Request
                    bestRegret = regret;
                    bestId = requestId;
                    bestCandidate = cands.getFirst();
                }
            }
            pending.removeAll(excluded);
            if (bestId == null) {
                break;
            }
            current = InsertionSearch.apply(problem, current, bestCandidate);
            pending.remove(bestId);
            cache.forget(bestId);
        }
        return current;
    }
}
