package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.List;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H20 — 최대 간극 제로각 양방향 sweep, Hertrich 2019 (heuristics 문서 §5 H20). 분할 기준은 오라클 실현성. */
public final class GapSweepBidirectionalConstruction implements ConstructionHeuristic {

    static final String ID = "gap-sweep-bidirectional";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return problem.depots().size() != 1;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        LocationId depot = problem.depots().getFirst().locationId();
        List<RequestId> byAngle = SweepNextFitConstruction.angleOrder(problem, depot);
        List<RequestId> clockwise = rotateToLargestGap(problem, depot, byAngle);
        List<RequestId> counter = new ArrayList<>(clockwise);
        if (counter.size() > 1) {
            List<RequestId> tail = new ArrayList<>(counter.subList(1, counter.size()));
            counter = new ArrayList<>();
            counter.add(clockwise.getFirst());                                  // 같은 제로각에서 반대 방향
            counter.addAll(tail.reversed());
        }

        Solution first = sweep(problem, profile, clockwise);
        Solution second = sweep(problem, profile, counter);
        EvaluationResult a = Evaluator.evaluate(problem, profile, first);
        EvaluationResult b = Evaluator.evaluate(problem, profile, second);
        if (!(a instanceof EvaluationResult.Feasible fa) || !(b instanceof EvaluationResult.Feasible fb)) {
            throw new IllegalStateException(ID + " produced infeasible direction: " + a + " / " + b);
        }
        return Scores.compare(fb.score(), fa.score()) < 0 ? second : first;      // 동률 = 시계 방향
    }

    /** 제로각 = 인접 θ 간극이 최대인 곳 (동률은 앞 요청 RequestId ASC 쪽). 그 다음 요청부터 시작하도록 회전. */
    static List<RequestId> rotateToLargestGap(Problem problem, LocationId depot, List<RequestId> byAngle) {
        int n = byAngle.size();
        if (n < 2) {
            return byAngle;
        }
        double[] theta = new double[n];
        for (int i = 0; i < n; i++) {
            theta[i] = SweepNextFitConstruction.angle(
                    problem, problem.locations().get(depot), InsertionSearch.anchor(problem.request(byAngle.get(i))).locationId());
        }
        int gapAt = 0;
        double largest = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            double next = i + 1 < n ? theta[i + 1] : theta[0] + 2 * Math.PI;
            double gap = next - theta[i];
            if (gap > largest) {                                                // 동률은 앞선(각도 순 먼저 = RequestId 순) 쪽 유지
                largest = gap;
                gapAt = i;
            }
        }
        List<RequestId> rotated = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            rotated.add(byAngle.get((gapAt + i) % n));
        }
        return rotated;
    }

    /**
     * 각도 순으로 차량을 채운다 — 비호환은 건너뛰고(뒤 차량 몫으로 남긴다), 열린 경로에 후보 0개면
     * 경로 확정·r부터 다음 차량 (규칙 c). 빈 경로에서 후보 0개인 요청은 차량을 소비하지 않고 건너뛴다 —
     * 어느 차로도 못 가는 요청이 차량을 전부 태우는 것을 막는다.
     */
    private static Solution sweep(Problem problem, Profile profile, List<RequestId> order) {
        Solution current = InsertionSearch.emptySolution(problem);
        List<RequestId> pending = new ArrayList<>(order);
        List<Vehicle> vehicles = InsertionSearch.vehiclesByCapacityDesc(problem);
        int bound = 2 * (order.size() + vehicles.size());
        int iterations = 0;
        for (Vehicle vehicle : vehicles) {
            if (pending.isEmpty()) {
                break;
            }
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);           // 규칙 (c)
            boolean opened = false;
            int i = 0;
            while (i < pending.size()) {
                RequestId requestId = pending.get(i);
                if (!problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                    i++;
                    continue;
                }
                List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicle.id());
                if (cands.isEmpty()) {
                    if (!opened) {
                        i++;
                        continue;
                    }
                    break;
                }
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);       // 규칙 (a)
                current = InsertionSearch.apply(problem, current, cands.getFirst());
                pending.remove(i);
                opened = true;
            }
        }
        return current;
    }
}
