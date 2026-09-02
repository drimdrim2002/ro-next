package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

class GiantTourSplitTest {

    /** T34 — 같은 순열·같은 차량 순서에서 Split의 (bank, 차량 수, 거리) 사전식 키 ≤ next-fit 분할 (X16). */
    @Test
    void splitNeverWorseThanNextFitOnSameTour() {
        Profile profile = new DefaultProfile();
        for (Problem problem : List.of(
                ConstructionFixtures.ring(8, 3, 30_000L, 10_000L),
                ConstructionFixtures.ring(7, 2, 30_000L, 10_000L),               // 차량이 모자라 bank가 생긴다
                ConstructionFixtures.mixed())) {
            List<RequestId> sigma = InsertionSearch.sortedRequestIds(problem);
            List<Vehicle> vehicles = InsertionSearch.vehiclesByCapacityDesc(problem);

            Solution split = GiantTourSplit.split(problem, profile, sigma, vehicles);
            Solution nextFit = nextFit(problem, profile, sigma, vehicles);
            assertTrue(StructureCheck.check(problem, split).isEmpty());
            assertTrue(StructureCheck.check(problem, nextFit).isEmpty());
            assertTrue(GiantTourSplit.compare(key(problem, profile, split), key(problem, profile, nextFit)) <= 0,
                    problem.requests().size() + " requests");
        }
    }

    @Test
    void splitIsDeterministic() {
        Problem problem = ConstructionFixtures.ring(6, 2, 30_000L, 10_000L);
        List<RequestId> sigma = InsertionSearch.sortedRequestIds(problem);
        Solution a = GiantTourSplit.split(problem, new DefaultProfile(), sigma, InsertionSearch.vehiclesByCapacityDesc(problem));
        Solution b = GiantTourSplit.split(problem, new DefaultProfile(), sigma, InsertionSearch.vehiclesByCapacityDesc(problem));
        assertArrayEquals(key(problem, new DefaultProfile(), a), key(problem, new DefaultProfile(), b));
        assertTrue(a.equals(b));
    }

    /** next-fit — 순열 순으로 현재 차량에 붙이다가 불가하면 다음 차량으로, 차량이 없으면 bank. */
    private static Solution nextFit(Problem problem, Profile profile, List<RequestId> sigma, List<Vehicle> vehicles) {
        List<Route> routes = new ArrayList<>();
        Set<RequestId> bank = new LinkedHashSet<>();
        int k = 0;
        List<NodeId> visits = new ArrayList<>();
        for (RequestId requestId : sigma) {
            boolean placed = false;
            while (k < vehicles.size()) {
                List<NodeId> extended = new ArrayList<>(visits);
                GiantTourSplit.appendPair(problem, requestId, extended);
                if (InsertionSearch.validate(problem, profile, vehicles.get(k).id(), extended).isPresent()) {
                    visits = extended;
                    placed = true;
                    break;
                }
                if (!visits.isEmpty()) {
                    routes.add(new Route(vehicles.get(k).id(), visits));
                }
                visits = new ArrayList<>();
                k++;
            }
            if (!placed) {
                bank.add(requestId);
            }
        }
        if (!visits.isEmpty() && k < vehicles.size()) {
            routes.add(new Route(vehicles.get(k).id(), visits));
        }
        return new Solution(routes, bank);
    }

    private static long[] key(Problem problem, Profile profile, Solution solution) {
        EvaluationResult.Feasible feasible = assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, profile, solution));
        return new long[] {
            feasible.evaluation().unassignedCount(), feasible.evaluation().usedVehicleCount(), feasible.evaluation().totalDistanceMeter()
        };
    }
}
