package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H22 — 구성 → blame → 재정렬 반복, SWO (heuristics 문서 §5 H22). 라운드 고정 R=5, 난수 없음. */
public final class SqueakyWheelSequentialConstruction implements ConstructionHeuristic {

    static final String ID = "squeaky-wheel-sequential";
    /** 재량 상수 — 라운드 수. */
    static final int ROUNDS = 5;

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
        return construct(problem, profile, new ArrayList<>());
    }

    /** rounds 기록용 오버로드 — 라운드마다 (해, priority 스냅샷)을 남긴다 (T37). */
    Solution construct(Problem problem, Profile profile, List<Map<RequestId, Integer>> priorityTrace) {
        Map<RequestId, Integer> priority = new LinkedHashMap<>();
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
            priority.put(requestId, 0);
        }
        Comparator<RequestId> deadline = DeadlineSequentialConstruction.deadlineComparator(problem);
        Solution best = null;
        long[] bestScore = null;
        for (int round = 0; round < ROUNDS; round++) {
            priorityTrace.add(new LinkedHashMap<>(priority));
            List<RequestId> order = new ArrayList<>(priority.keySet());
            Map<RequestId, Integer> snapshot = priority;
            order.sort(Comparator.<RequestId>comparingInt(snapshot::get).reversed().thenComparing(deadline));
            Solution solution = DeadlineSequentialConstruction.construct(problem, profile, order, Candidate.byCost(), ID);
            EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
            if (!(evaluated instanceof EvaluationResult.Feasible feasible)) {
                throw new IllegalStateException(ID + " produced infeasible round: " + evaluated);
            }
            if (best == null || Scores.compare(feasible.score(), bestScore) < 0) {  // 동률이면 앞 라운드 유지
                best = solution;
                bestScore = feasible.score();
            }
            Map<RequestId, Integer> next = blame(problem, solution, priority);
            if (next.equals(priority)) {
                break;                                                          // 불변점 — 이후 라운드도 같은 해 (X18)
            }
            priority = next;
        }
        return best;
    }

    /** blame: bank → +2 · 방문 수가 경로 방문 수 중앙값 미만인 경로의 요청 → +1. */
    static Map<RequestId, Integer> blame(Problem problem, Solution solution, Map<RequestId, Integer> priority) {
        Map<RequestId, Integer> next = new LinkedHashMap<>(priority);
        for (RequestId requestId : solution.bank()) {
            next.merge(requestId, 2, Integer::sum);
        }
        List<Integer> sizes = new ArrayList<>();
        for (Route route : solution.routes()) {
            sizes.add(route.visits().size());
        }
        if (sizes.isEmpty()) {
            return next;
        }
        sizes.sort(Comparator.naturalOrder());
        int median = sizes.get(sizes.size() / 2);
        for (Route route : solution.routes()) {
            if (route.visits().size() < median) {
                for (NodeId nodeId : route.visits()) {
                    next.merge(InsertionSearch.requestOf(problem, nodeId), 1, Integer::sum);
                }
            }
        }
        return next;
    }
}
