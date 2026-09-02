package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H2 — 시간창 긴급 등급(4분위) + regret-3 (heuristics 문서 §5 H2). */
public final class UrgencyRegret3Construction implements ConstructionHeuristic {

    static final String ID = "urgency-regret3";

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
        Map<RequestId, Integer> grade = urgencyGrades(problem);           // Problem에서 한 번 계산하고 동결
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
                long c1 = cands.get(0).deltaDriveDistMeter();
                long regret3 = 0L;
                for (int k = 1; k < Math.min(3, cands.size()); k++) {          // 없는 항은 채우지 않는다
                    regret3 = Math.addExact(regret3, cands.get(k).deltaDriveDistMeter() - c1);
                }
                // 후보 수는 regret3의 빠진 항을 대신하는 축이라 3에서 자른다 — 그 이상은 항이 다 있다
                Pick pick = new Pick(requestId, grade.get(requestId), Math.min(cands.size(), 3), regret3, cands.getFirst());
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

    /**
     * 긴급 등급 0..3 — 창 폭 합(delivery가 있으면 delivery, 없으면 pickup)을 오름차순 정렬해 4등분.
     * 같은 폭은 같은 등급이다 (경계값은 정렬된 폭의 n/4·n/2·3n/4 번째 값).
     */
    static Map<RequestId, Integer> urgencyGrades(Problem problem) {
        List<Long> widths = new ArrayList<>();
        Map<RequestId, Long> widthById = new LinkedHashMap<>();
        for (Request request : problem.requests()) {
            long width = InsertionSearch.windowSpanSec(InsertionSearch.lastSide(request));
            widthById.put(request.id(), width);
            widths.add(width);
        }
        widths.sort(Comparator.naturalOrder());
        int n = widths.size();
        Map<RequestId, Integer> grades = new LinkedHashMap<>();
        for (Map.Entry<RequestId, Long> entry : widthById.entrySet()) {
            int grade = 0;
            for (int k = 1; k <= 3; k++) {
                if (entry.getValue() >= widths.get((int) ((long) k * n / 4))) {
                    grade++;
                }
            }
            grades.put(entry.getKey(), grade);
        }
        return grades;
    }

    /** 선택 키 = (긴급 등급 ASC, 후보 수 ASC, regret3 DESC, RequestId ASC). */
    private static final Comparator<Pick> PICK_ORDER = Comparator.comparingInt(Pick::grade)
            .thenComparingInt(Pick::candidateCount)
            .thenComparing(Pick::regret3, Comparator.reverseOrder())
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, int grade, int candidateCount, long regret3, Candidate candidate) {}
}
