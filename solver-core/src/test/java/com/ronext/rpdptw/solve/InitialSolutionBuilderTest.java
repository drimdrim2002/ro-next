package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

class InitialSolutionBuilderTest {

    /** 기권 가능한 기법 전부 (§4.4) — 이 집합 밖의 기법은 어떤 Problem에서도 기권하지 않는다. */
    static final Set<String> MAY_ABSTAIN = Set.of(
            "farthest-seed-sequential", "savings-merge", "sweep-next-fit",
            "nearest-neighbor-split", "gap-sweep-bidirectional");

    /** T14 — Scores.compare 최소인 해가 선택되고, 동률이면 우선순위 앞선 기법 (X7). */
    @Test
    void selectsBestByOfficialEvaluation() {
        Problem problem = ConstructionFixtures.ring(3, 2, 30_000L, 5_000L);
        Profile profile = new DefaultProfile();
        RequestId r1 = new RequestId("R1");
        RequestId r2 = new RequestId("R2");
        RequestId r3 = new RequestId("R3");
        Solution partial = new Solution(
                List.of(new Route(new VehicleId("V1"), List.of(NodeId.delivery(r1), NodeId.delivery(r2)))), Set.of(r3));
        Solution full = new Solution(
                List.of(new Route(new VehicleId("V1"), List.of(NodeId.delivery(r1), NodeId.delivery(r2), NodeId.delivery(r3)))),
                Set.of());
        Solution fullTwin = new Solution(full.routes(), full.bank());

        InitialSolutionResult result = InitialSolutionBuilder.build(problem, profile, List.of(
                ConstructionFixtures.fixed("partial", partial),
                ConstructionFixtures.fixed("full-a", full),
                ConstructionFixtures.fixed("full-b", fullTwin)));
        assertEquals("full-a", result.heuristicId());
        assertEquals(full, result.best());
        assertEquals(0, result.evaluation().unassignedCount());
        assertEquals(3, result.outcomes().size());
    }

    /** T15 — PD 포함·다중 depot 문제에서 H5·H7·H8(·H19·H20)만 ABSTAINED, 나머지는 BUILT. */
    @Test
    void abstainedHeuristicsAreSkipped() {
        Problem problem = ConstructionFixtures.mixed();
        InitialSolutionResult result = InitialSolutionBuilder.build(problem, new DefaultProfile());
        for (ConstructionOutcome outcome : result.outcomes()) {
            boolean expectAbstain = MAY_ABSTAIN.contains(outcome.heuristicId());
            assertEquals(expectAbstain ? ConstructionOutcome.Status.ABSTAINED : ConstructionOutcome.Status.BUILT,
                    outcome.status(), outcome.heuristicId());
        }
        assertTrue(StructureCheck.check(problem, result.best()).isEmpty());
        assertFalse(MAY_ABSTAIN.contains(result.heuristicId()));
    }

    /** T16 — 기권 기법만 담은 목록을 오버로드에 넘기면 빈 해, "empty", 예외 없음 (X3). */
    @Test
    void allAbstainedYieldsEmptySolution() {
        Problem problem = ConstructionFixtures.mixed();
        InitialSolutionResult result = InitialSolutionBuilder.build(problem, new DefaultProfile(), List.of(
                new SavingsMergeConstruction(), new SweepNextFitConstruction()));
        assertEquals(InitialSolutionBuilder.EMPTY_ID, result.heuristicId());
        assertTrue(result.best().routes().isEmpty());
        assertEquals(ConstructionFixtures.requestIds(problem), result.best().bank());
        assertEquals(problem.requests().size(), result.evaluation().unassignedCount());
        assertEquals(2, result.outcomes().size());
        assertTrue(result.outcomes().stream().allMatch(o -> o.status() == ConstructionOutcome.Status.ABSTAINED));
    }

    /** T17 — 22개 각각을 직접 Evaluator.evaluate → 전부 Feasible. profile hard 있는 문제에서도 (X5·X14). */
    @Test
    void everyConstructionResultIsFeasible() {
        List<Problem> problems = List.of(
                ConstructionFixtures.ring(6, 3, 30_000L, 10_000L),
                ConstructionFixtures.mixed(),
                ConstructionFixtures.ring(4, 1, 30_000L, 10_000L));
        List<Profile> profiles = List.of(new DefaultProfile(), ConstructionFixtures.maxVisitsProfile(2), ConstructionFixtures.maxVisitsProfile(0));
        for (Problem problem : problems) {
            for (Profile profile : profiles) {
                for (ConstructionHeuristic heuristic : InitialSolutionBuilder.defaults()) {
                    if (heuristic.abstains(problem)) {
                        continue;
                    }
                    Solution solution = heuristic.construct(problem, profile);
                    assertTrue(StructureCheck.check(problem, solution).isEmpty(), heuristic.id());
                    EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
                    assertInstanceOf(EvaluationResult.Feasible.class, evaluated, heuristic.id() + " " + profile.id());
                }
            }
        }
    }

    /** T18 — 같은 Problem·Profile로 두 번 → best·heuristicId 동등, score는 Arrays.equals (X8). */
    @Test
    void deterministicAcrossRuns() {
        Problem problem = ConstructionFixtures.ring(7, 3, 30_000L, 10_000L);
        Profile profile = new DefaultProfile();
        InitialSolutionResult first = InitialSolutionBuilder.build(problem, profile);
        InitialSolutionResult second = InitialSolutionBuilder.build(problem, profile);
        assertEquals(first.best(), second.best());
        assertEquals(first.heuristicId(), second.heuristicId());
        assertArrayEquals(first.score(), second.score());
        for (int i = 0; i < first.outcomes().size(); i++) {
            ConstructionOutcome a = first.outcomes().get(i);
            ConstructionOutcome b = second.outcomes().get(i);
            assertEquals(a.heuristicId(), b.heuristicId());
            assertEquals(a.status(), b.status());
            assertEquals(a.unassignedCount(), b.unassignedCount());
            if (a.score().isPresent()) {
                assertArrayEquals(a.score().get(), b.score().get(), a.heuristicId());
            }
        }
    }

    /** T24 — outcomes가 우선순위 순이고 기법마다 상태·미배정 수·score·소요를 담는다. */
    @Test
    void recordsPerHeuristicOutcome() {
        Problem problem = ConstructionFixtures.mixed();
        InitialSolutionResult result = InitialSolutionBuilder.build(problem, new DefaultProfile());
        List<String> expectedOrder = new ArrayList<>();
        for (ConstructionHeuristic heuristic : InitialSolutionBuilder.defaults()) {
            expectedOrder.add(heuristic.id());
        }
        assertEquals(expectedOrder, result.outcomes().stream().map(ConstructionOutcome::heuristicId).toList());
        for (ConstructionOutcome outcome : result.outcomes()) {
            assertTrue(outcome.elapsedMillis() >= 0L);
            if (outcome.status() == ConstructionOutcome.Status.BUILT) {
                assertTrue(outcome.unassignedCount().isPresent(), outcome.heuristicId());
                assertTrue(outcome.score().isPresent(), outcome.heuristicId());
                assertEquals(outcome.unassignedCount().getAsInt(), outcome.score().get()[0], outcome.heuristicId());
            } else {
                assertTrue(outcome.unassignedCount().isEmpty());
                assertTrue(outcome.score().isEmpty());
            }
        }
    }
}
