package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class RemovalOperatorsTest {

    private static final RequestId R1 = new RequestId("R1");
    private static final RequestId R2 = new RequestId("R2");
    private static final RequestId R3 = new RequestId("R3");
    private static final RequestId R4 = new RequestId("R4");
    private static final RequestId R5 = new RequestId("R5");

    private static RandomGenerator rng(long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    /** V1: R2픽 → R1배 → R2배 → R4배, V2: R5픽 → R5배, bank {R3}. */
    private static Solution twoRoutes() {
        return new Solution(
                List.of(
                        new Route(new VehicleId("V1"), List.of(NodeId.pickup(R2), NodeId.delivery(R1), NodeId.delivery(R2), NodeId.delivery(R4))),
                        new Route(new VehicleId("V2"), List.of(NodeId.pickup(R5), NodeId.delivery(R5)))),
                Set.of(R3));
    }

    /** T7 — PD 제거 시 두 방문이 함께 사라짐 · E3 clamp · E4 빈 경로 제거. */
    @Test
    void pairRemovalAndEmptiedRouteDrop() {
        Problem problem = ConstructionFixtures.mixed();
        Solution current = twoRoutes();
        assertTrue(StructureCheck.check(problem, current).isEmpty());
        assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, new DefaultProfile(), current));

        RandomRemoval random = new RandomRemoval();
        boolean removedR5 = false;
        for (long seed = 0; seed < 20; seed++) {
            Solution destroyed = random.destroy(problem, current, 1, rng(seed));
            assertTrue(StructureCheck.check(problem, destroyed).isEmpty());
            Set<RequestId> moved = new HashSet<>(destroyed.bank());
            moved.removeAll(current.bank());
            assertEquals(1, moved.size());
            RequestId gone = moved.iterator().next();
            assertEquals(Set.of(R1, R2, R4, R5).contains(gone), true);
            for (Route route : destroyed.routes()) {
                assertFalse(route.visits().contains(NodeId.pickup(gone)));
                assertFalse(route.visits().contains(NodeId.delivery(gone)));
            }
            if (gone.equals(R5)) {
                removedR5 = true;
                assertEquals(1, destroyed.routes().size());               // E4 — V2 경로째 제거
                assertEquals(new VehicleId("V1"), destroyed.routes().getFirst().vehicleId());
            } else {
                assertEquals(2, destroyed.routes().size());
            }
        }
        assertTrue(removedR5, "20 seeds never removed R5");

        // E3 — removeCount > 배정 수 → 배정 수로 clamp: 전부 bank, 경로 0개
        Solution all = random.destroy(problem, current, 100, rng(1));
        assertTrue(all.routes().isEmpty());
        assertEquals(ConstructionFixtures.requestIds(problem), all.bank());
        assertTrue(StructureCheck.check(problem, all).isEmpty());

        // 배정 0건인 해에서 destroy → 그대로
        assertEquals(all, random.destroy(problem, all, 3, rng(2)));
    }

    /** RouteRemoval — 경로 하나가 통째로 사라지고 소유 Request 전부 bank로. 경로 없으면 그대로 (removeCount 무시, E15). */
    @Test
    void routeRemovalMovesWholeRouteToBank() {
        Problem problem = ConstructionFixtures.mixed();
        Solution current = twoRoutes();
        RouteRemoval route = new RouteRemoval();
        boolean sawV1 = false;
        boolean sawV2 = false;
        for (long seed = 0; seed < 10; seed++) {
            Solution destroyed = route.destroy(problem, current, 0, rng(seed));
            assertTrue(StructureCheck.check(problem, destroyed).isEmpty());
            assertEquals(1, destroyed.routes().size());
            if (destroyed.routes().getFirst().vehicleId().equals(new VehicleId("V2"))) {
                sawV1 = true;
                assertEquals(Set.of(R1, R2, R3, R4), destroyed.bank());
            } else {
                sawV2 = true;
                assertEquals(Set.of(R3, R5), destroyed.bank());
            }
        }
        assertTrue(sawV1 && sawV2, "10 seeds did not cover both routes");

        Solution empty = InsertionSearch.emptySolution(problem);
        assertEquals(empty, route.destroy(problem, empty, 1, rng(0)));
    }

    /**
     * T7c — StringRemoval이 seed 근처 경로에서 연속 구간을 pair 단위로 q개까지 빼고, 빈 경로는 사라지며(E4),
     * 같은 seed면 같은 결과다(N3). DELIVERY_ONLY뿐인 고리 문제라 제거 구간이 방문 구간과 1:1이다.
     */
    @Test
    void stringRemovalTakesContiguousSegments() {
        Problem problem = ConstructionFixtures.ring(8, 2, 30_000L, 1_000L);
        Solution current = ringRoutes();
        assertTrue(StructureCheck.check(problem, current).isEmpty());
        assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, new DefaultProfile(), current));

        StringRemoval string = new StringRemoval();
        boolean sawDroppedRoute = false;
        for (long seed = 0; seed < 20; seed++) {
            Solution destroyed = string.destroy(problem, current, 3, rng(seed));
            assertTrue(StructureCheck.check(problem, destroyed).isEmpty(), "seed " + seed);
            assertEquals(destroyed.bank(), removed(problem, current, destroyed), "seed " + seed);
            int count = destroyed.bank().size();
            assertTrue(count >= 1 && count <= 3, "seed " + seed + " removed " + count);
            for (Route route : current.routes()) {
                assertContiguous(problem, route, destroyed.bank(), "seed " + seed + " " + route.vehicleId().value());
            }
            sawDroppedRoute |= destroyed.routes().size() < current.routes().size();

            assertEquals(destroyed, string.destroy(problem, current, 3, rng(seed)), "seed " + seed + " determinism");
        }

        // 한 경로(4방문)를 통째로 삼키는 seed가 있어야 E4를 밟는다
        boolean sawDroppedRouteBig = false;
        for (long seed = 0; seed < 20; seed++) {
            Solution destroyed = string.destroy(problem, current, 8, rng(seed));
            assertTrue(StructureCheck.check(problem, destroyed).isEmpty(), "seed " + seed);
            sawDroppedRouteBig |= destroyed.routes().size() < current.routes().size();
        }
        assertTrue(sawDroppedRoute || sawDroppedRouteBig, "20 seeds never emptied a route");

        // q > 배정 수 → 전부 제거 (E3)
        Solution all = string.destroy(problem, current, 100, rng(1));
        assertTrue(all.routes().isEmpty());
        assertEquals(ConstructionFixtures.requestIds(problem), all.bank());

        // 배정 0건이면 그대로
        Solution empty = InsertionSearch.emptySolution(problem);
        assertEquals(empty, string.destroy(problem, empty, 3, rng(0)));
    }

    /** ring(8, 2) 위의 손 조립 해 — V1: R1..R4, V2: R5..R8. */
    private static Solution ringRoutes() {
        List<NodeId> first = new ArrayList<>();
        List<NodeId> second = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            (i <= 4 ? first : second).add(NodeId.delivery(new RequestId("R" + i)));
        }
        return new Solution(
                List.of(new Route(new VehicleId("V1"), first), new Route(new VehicleId("V2"), second)),
                Set.of());
    }

    /** 경로에서 빠진 방문의 위치가 연속 구간인지 — 한 경로에서 자르는 것은 문자열 하나뿐이다. */
    private static void assertContiguous(Problem problem, Route route, Set<RequestId> gone, String at) {
        TreeSet<Integer> indexes = new TreeSet<>();
        for (int i = 0; i < route.visits().size(); i++) {
            if (gone.contains(InsertionSearch.requestOf(problem, route.visits().get(i)))) {
                indexes.add(i);
            }
        }
        if (indexes.isEmpty()) {
            return;
        }
        int min = indexes.first();
        int max = indexes.last();
        assertEquals(max - min + 1, indexes.size(), at + " not contiguous: " + indexes);
    }

    private static Set<RequestId> removed(Problem problem, Solution before, Solution after) {
        Set<RequestId> gone = new HashSet<>(RandomRemoval.assignedRequestIds(problem, before));
        gone.removeAll(RandomRemoval.assignedRequestIds(problem, after));
        return gone;
    }
}
