package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class ConstructionHeuristicsTest {

    /** T19 — 22개 각각이 바깥 루프 상한 안에서 끝나고(예외 없음), 방어 카운터 초과는 예외 (X13). */
    @Test
    void outerLoopBoundedByRequestCount() {
        List<Problem> problems = List.of(
                ConstructionFixtures.ring(9, 4, 30_000L, 10_000L),
                ConstructionFixtures.mixed(),
                ConstructionFixtures.ring(0, 2, 30_000L, 10_000L),          // X1 — requests 빈 목록
                ConstructionFixtures.ring(3, 0, 30_000L, 10_000L));         // X2 — vehicles 빈 목록
        for (Problem problem : problems) {
            for (ConstructionHeuristic heuristic : InitialSolutionBuilder.defaults()) {
                if (heuristic.abstains(problem)) {
                    continue;
                }
                Solution solution = assertDoesNotThrow(() -> heuristic.construct(problem, new DefaultProfile()), heuristic.id());
                assertTrue(StructureCheck.check(problem, solution).isEmpty(), heuristic.id());
                if (problem.vehicles().isEmpty()) {
                    assertEquals(ConstructionFixtures.requestIds(problem), solution.bank(), heuristic.id());
                }
            }
        }
        assertDoesNotThrow(() -> InsertionSearch.checkOuterLoop("x", 5, 5));
        assertThrows(IllegalStateException.class, () -> InsertionSearch.checkOuterLoop("x", 6, 5));
    }

    /** T20 — 22개 결과 전부 StructureCheck 위반 0 · pair 원자성 · XOR. PD 포함 문제로도. */
    @Test
    void structureHoldsForEveryHeuristic() {
        for (Problem problem : List.of(ConstructionFixtures.ring(8, 3, 30_000L, 10_000L), ConstructionFixtures.mixed())) {
            for (ConstructionHeuristic heuristic : InitialSolutionBuilder.defaults()) {
                if (heuristic.abstains(problem)) {
                    continue;
                }
                Solution solution = heuristic.construct(problem, new DefaultProfile());
                assertTrue(StructureCheck.check(problem, solution).isEmpty(), heuristic.id());
                Set<RequestId> assigned = new HashSet<>();
                for (Route route : solution.routes()) {
                    assertFalse(route.visits().isEmpty(), heuristic.id());
                    for (NodeId nodeId : route.visits()) {
                        assigned.add(problem.nodeRef(nodeId).orElseThrow().requestId());
                    }
                    for (Request request : problem.requests()) {
                        if (request.pattern() == ServicePattern.PICKUP_DELIVERY) {
                            int p = route.visits().indexOf(request.pickup().orElseThrow().nodeId());
                            int d = route.visits().indexOf(request.delivery().orElseThrow().nodeId());
                            assertEquals(p < 0, d < 0, heuristic.id() + " pair split " + request.id());
                            assertTrue(p <= d, heuristic.id());
                        }
                    }
                }
                for (Request request : problem.requests()) {
                    assertTrue(assigned.contains(request.id()) ^ solution.bank().contains(request.id()),
                            heuristic.id() + " XOR " + request.id());
                }
            }
        }
    }

    @Test
    void defaultsAreTwentyTwoInPriorityOrder() {
        List<ConstructionHeuristic> defaults = InitialSolutionBuilder.defaults();
        assertEquals(List.of(
                "scarcity-regret2", "urgency-regret3", "vehicle-zone-fill", "deadline-sequential",
                "farthest-seed-sequential", "zone-first-ffd", "savings-merge", "sweep-next-fit",
                "feasible-routes-regret-m", "chain-seed-regret-m", "i1-savings-sequential", "route-bidding",
                "slack-preserving-sequential", "vehicle-fill-remaining-regret", "weakest-fit-decreasing",
                "constrained-path-extension", "farthest-seed-global-cheapest", "hilbert-split",
                "nearest-neighbor-split", "gap-sweep-bidirectional", "spatiotemporal-cluster",
                "squeaky-wheel-sequential"),
                defaults.stream().map(ConstructionHeuristic::id).toList());
        assertEquals(Optional.empty(), Optional.empty());
    }
}
