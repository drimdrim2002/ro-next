package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.SolveFixtures.R1;
import static com.ronext.rpdptw.solve.SolveFixtures.R2;
import static com.ronext.rpdptw.solve.SolveFixtures.V1;
import static com.ronext.rpdptw.solve.SolveFixtures.section72Problem;
import static com.ronext.rpdptw.solve.SolveFixtures.freeze;
import static com.ronext.rpdptw.solve.SolveFixtures.section72Travel;
import static com.ronext.rpdptw.solve.SolveFixtures.section72Visits;
import static com.ronext.rpdptw.solve.SolveFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.Problem;

class EvaluatorTest {

    @Test
    void incompatibleVehicleAndProfileHardAreHard() {
        Problem problem = section72Problem(false);
        Vehicle restricted = vehicle(
                "V1",
                30_000L,
                1_000_000L,
                List.of(new TimeWindow(28_800, 64_800)),
                Optional.of(SolveFixtures.DEPOT),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Optional.of("T2"));
        Problem incompatible = freeze(
                false,
                problem.depots(),
                List.of(
                        withFeatures(problem.request(R1), Set.of("T1")),
                        problem.request(R2)),
                List.of(restricted),
                problem.locations(),
                section72Travel());
        Solution assigned = new Solution(
                List.of(new Route(V1, List.of(NodeId.pickup(R2), NodeId.delivery(R2), NodeId.delivery(R1)))),
                Set.of());
        EvaluationResult.Infeasible incompat =
                assertInstanceOf(EvaluationResult.Infeasible.class, Evaluator.evaluate(incompatible, new DefaultProfile(), assigned));
        assertEquals(Violation.INCOMPATIBLE_VEHICLE, incompat.violation());
        assertEquals(Optional.of(NodeId.delivery(R1)), incompat.at());

        Profile alwaysFalse = new Profile() {
            @Override
            public String id() {
                return "deny";
            }

            @Override
            public List<HardConstraint> hardConstraints() {
                return List.of(new HardConstraint() {
                    @Override
                    public String id() {
                        return "ALWAYS_FALSE";
                    }

                    @Override
                    public boolean satisfied(Problem p, RouteFacts route) {
                        return false;
                    }
                });
            }

            @Override
            public long[] score(Problem p, Evaluation metrics, Collection<RouteFacts> routes) {
                return new long[] {0};
            }
        };
        EvaluationResult.Infeasible profileHard = assertInstanceOf(
                EvaluationResult.Infeasible.class, Evaluator.evaluate(problem, alwaysFalse, assigned));
        assertEquals(Violation.PROFILE_HARD, profileHard.violation());
        assertEquals(Optional.of("ALWAYS_FALSE"), profileHard.hardConstraintId());
    }

    @Test
    void usesGivenProfileInstance() {
        Problem problem = section72Problem(false);
        for (Method method : Problem.class.getMethods()) {
            assertFalse(method.getName().equals("profile") && method.getParameterCount() == 0);
        }
        AtomicInteger hardCalls = new AtomicInteger();
        AtomicInteger scoreCalls = new AtomicInteger();
        Profile spy = new Profile() {
            @Override
            public String id() {
                return "spy";
            }

            @Override
            public List<HardConstraint> hardConstraints() {
                hardCalls.incrementAndGet();
                return List.of();
            }

            @Override
            public long[] score(Problem p, Evaluation metrics, Collection<RouteFacts> routes) {
                scoreCalls.incrementAndGet();
                return new long[] {metrics.unassignedCount()};
            }
        };
        Solution allBanked = new Solution(List.of(), Set.of(R1, R2));
        EvaluationResult.Feasible feasible =
                assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, spy, allBanked));
        assertEquals(0, hardCalls.get());
        assertEquals(1, scoreCalls.get());
        assertArrayEquals(new long[] {2}, feasible.score());

        Solution routed = new Solution(List.of(new Route(V1, section72Visits())), Set.of(R2));
        Evaluator.evaluate(problem, spy, routed);
        assertTrue(hardCalls.get() >= 1);
        assertEquals(2, scoreCalls.get());
    }

    @Test
    void aggregatesMetricsAndScoresLexicographically() {
        Problem problem = section72Problem(false);
        DefaultProfile defaults = new DefaultProfile();

        EvaluationResult.Feasible empty = assertInstanceOf(
                EvaluationResult.Feasible.class,
                Evaluator.evaluate(problem, defaults, new Solution(List.of(), Set.of(R1, R2))));
        assertEquals(new Evaluation(2, 0, 0L, 0L), empty.evaluation());
        assertArrayEquals(new long[] {2, 0, 0, 0}, empty.score());

        assertEquals(0, Scores.compare(new long[] {1, 2}, new long[] {1, 2}));
        assertThrows(IllegalArgumentException.class, () -> Scores.compare(new long[] {1}, new long[] {1, 2}));

        VisitFacts dummy = new VisitFacts(
                NodeId.delivery(R1), R1, false, SolveFixtures.GANGNAM100, 0, 0, 0, 0, 0, 0, 0);
        RouteFacts far = factsWithDistance(100_000L, dummy);
        RouteFacts near = factsWithDistance(80_000L, dummy);
        Evaluation a = Evaluation.aggregate(List.of(far), 0);
        Evaluation b = Evaluation.aggregate(List.of(near), 1);
        long[] scoreA = defaults.score(problem, a, List.of(far));
        long[] scoreB = defaults.score(problem, b, List.of(near));
        assertTrue(Scores.compare(scoreA, scoreB) < 0);

        Profile distanceOnly = new Profile() {
            @Override
            public String id() {
                return "distance";
            }

            @Override
            public List<HardConstraint> hardConstraints() {
                return List.of();
            }

            @Override
            public long[] score(Problem p, Evaluation metrics, Collection<RouteFacts> routes) {
                return new long[] {metrics.totalDistanceMeter()};
            }
        };
        assertTrue(Scores.compare(distanceOnly.score(problem, a, List.of(far)), distanceOnly.score(problem, b, List.of(near)))
                > 0);

        RouteFacts overflow = factsWithDistance(Long.MAX_VALUE, dummy);
        RouteFacts one = factsWithDistance(1L, dummy);
        assertThrows(ArithmeticException.class, () -> Evaluation.aggregate(List.of(overflow, one), 0));
    }

    @Test
    void hardConstraintReadsProblemFacts() {
        Problem problem = freeze(
                false,
                section72Problem(false).depots(),
                section72Problem(false).requests(),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        List.of(new TimeWindow(28_800, 64_800)),
                        Optional.of(SolveFixtures.DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.of("BOX"))),
                section72Problem(false).locations(),
                section72Travel());
        HardConstraint feature = new HardConstraint() {
            @Override
            public String id() {
                return "FEATURE";
            }

            @Override
            public boolean satisfied(Problem p, RouteFacts route) {
                return p.vehicle(route.vehicleId()).vehicleFeature().orElse("").equals("BOX");
            }
        };
        Profile profile = new Profile() {
            @Override
            public String id() {
                return "feat";
            }

            @Override
            public List<HardConstraint> hardConstraints() {
                return List.of(feature);
            }

            @Override
            public long[] score(Problem p, Evaluation metrics, Collection<RouteFacts> routes) {
                return new long[] {0};
            }
        };
        Solution routed = new Solution(List.of(new Route(V1, section72Visits())), Set.of(R2));
        assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, profile, routed));

        Problem otherFeature = freeze(
                false,
                problem.depots(),
                problem.requests(),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        List.of(new TimeWindow(28_800, 64_800)),
                        Optional.of(SolveFixtures.DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.of("VAN"))),
                problem.locations(),
                section72Travel());
        EvaluationResult.Infeasible denied =
                assertInstanceOf(EvaluationResult.Infeasible.class, Evaluator.evaluate(otherFeature, profile, routed));
        assertEquals(Violation.PROFILE_HARD, denied.violation());
        assertEquals(Optional.of("FEATURE"), denied.hardConstraintId());
    }

    private static com.ronext.rpdptw.domain.Request withFeatures(
            com.ronext.rpdptw.domain.Request request, Set<String> features) {
        return new com.ronext.rpdptw.domain.Request(
                request.id(),
                request.pattern(),
                request.pickup(),
                request.delivery(),
                request.items(),
                request.totalWeight(),
                request.totalVolume(),
                Optional.of(features),
                request.requiredCapabilities());
    }

    private static RouteFacts factsWithDistance(long distance, VisitFacts visit) {
        return new RouteFacts(
                V1,
                0L,
                Optional.empty(),
                0L,
                0L,
                List.of(visit),
                Optional.empty(),
                distance,
                0L,
                0L,
                0L,
                0L,
                0L,
                1);
    }
}
