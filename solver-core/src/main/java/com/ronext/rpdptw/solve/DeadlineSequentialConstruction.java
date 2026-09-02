package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H4 — 마감 임박 순 순차 삽입 (baseline, heuristics 문서 §5 H4). */
public final class DeadlineSequentialConstruction implements ConstructionHeuristic {

    static final String ID = "deadline-sequential";

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
        return construct(problem, profile, deadlineOrder(problem), Candidate.byCost(), ID);
    }

    /** 주어진 순서로 순차 삽입 — 위치 비교자만 갈아 끼운다 (H13·H22가 공유). */
    static Solution construct(
            Problem problem, Profile profile, List<RequestId> order, Comparator<Candidate> position, String id) {
        Solution current = InsertionSearch.emptySolution(problem);
        int iterations = 0;
        for (RequestId requestId : order) {
            InsertionSearch.checkOuterLoop(id, ++iterations, order.size());
            List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, requestId);
            if (cands.isEmpty()) {
                continue;                                                       // 규칙 (b) — bank에 남는다
            }
            List<Candidate> ranked = new ArrayList<>(cands);
            ranked.sort(position);                                              // 안정 정렬 — 동률은 §4.1 순서 유지
            current = InsertionSearch.apply(problem, current, ranked.getFirst()); // 규칙 (a)
        }
        return current;
    }

    /** 삽입 순서 = (있는 delivery의 마지막 창 close ASC, 없으면 pickup의, RequestId ASC). */
    static List<RequestId> deadlineOrder(Problem problem) {
        List<RequestId> order = new ArrayList<>(InsertionSearch.sortedRequestIds(problem));
        order.sort(deadlineComparator(problem));
        return order;
    }

    static Comparator<RequestId> deadlineComparator(Problem problem) {
        return Comparator.<RequestId>comparingLong(id -> deadlineSec(problem.request(id)))
                .thenComparing(InsertionSearch.BY_REQUEST_ID);
    }

    static long deadlineSec(Request request) {
        return InsertionSearch.lastCloseSec(InsertionSearch.lastSide(request));
    }
}
