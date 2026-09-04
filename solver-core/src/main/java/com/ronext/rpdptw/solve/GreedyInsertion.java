package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** bank를 rng로 섞은 순서대로 각각 최소 비용 후보에 순차 삽입 (stage-04 §4.4). 후보 0개면 bank에 남긴다. */
public final class GreedyInsertion implements RepairOperator {

    static final String ID = "greedy-insertion";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Solution repair(Problem problem, Profile profile, Solution destroyed, RandomGenerator rng) {
        List<RequestId> order = new ArrayList<>(destroyed.bank());
        order.sort(InsertionSearch.BY_REQUEST_ID);                    // N3 — 정렬 후 rng
        for (int i = order.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            RequestId swap = order.get(i);
            order.set(i, order.get(j));
            order.set(j, swap);
        }
        Solution current = destroyed;
        for (RequestId requestId : order) {
            List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, requestId);
            if (!cands.isEmpty()) {
                current = InsertionSearch.apply(problem, current, cands.getFirst());
            }
        }
        return current;
    }
}
