package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.AlnsRunStats.Termination;

class AlnsSolverTest {

    private static final Profile PROFILE = new DefaultProfile();

    /** §3.3 기본 튜닝값에 예산만 바꿔 끼운 설정. */
    static AlnsConfig config(long timeLimitSec, OptionalLong maxSteps, OptionalLong idleSteps, OptionalLong idleSec,
                             long seed, double worseAcceptStartProbability) {
        return new AlnsConfig(timeLimitSec, maxSteps, idleSteps, idleSec, seed,
                5, 20, worseAcceptStartProbability, 100, 0.5, 5, 2, 1);
    }

    static AlnsConfig steps(long maxSteps, long seed) {
        return config(60L, OptionalLong.of(maxSteps), OptionalLong.empty(), OptionalLong.empty(), seed, 0.05);
    }

    /** T1 — 소형 fixture에서 포트폴리오 24개 중 최선 대비 개선 (2026-09-02 실측: 거리 44151 → 38764). */
    @Test
    void improvesOverInitialOnSmallFixture() {
        Problem problem = smallFixture();
        AlnsResult result = AlnsSolver.withDefaults(steps(1000L, 7L)).solve(problem, PROFILE);

        assertTrue(Scores.compare(result.bestScore(), result.initialScore()) < 0,
                "best " + java.util.Arrays.toString(result.bestScore()) + " vs initial " + java.util.Arrays.toString(result.initialScore()));
        assertEquals(Termination.MAX_STEPS, result.stats().termination());
        assertEquals(1000L, result.stats().iterations());
        assertTrue(result.stats().bestImproved() >= 1L);
        assertTrue(StructureCheck.check(problem, result.best()).isEmpty());
        assertEquals(0, result.bestEvaluation().unassignedCount());
    }

    /** T3 — 짧은 한도로 정상 반환·TIME_LIMIT. 한도 0이면 반복 0회, initial이 곧 best (E9). */
    @Test
    void stopsAtTimeLimitAndReturnsBest() {
        Problem problem = smallFixture();
        InitialSolutionResult initial = InitialSolutionBuilder.build(problem, PROFILE);

        AlnsResult zero = AlnsSolver.withDefaults(config(0L, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), 1L, 0.05))
                .solve(problem, PROFILE);
        assertEquals(Termination.TIME_LIMIT, zero.stats().termination());
        assertEquals(0L, zero.stats().iterations());
        assertEquals(initial.best(), zero.best());
        assertEquals(initial.evaluation(), zero.bestEvaluation());
        assertArrayEquals(initial.score(), zero.bestScore());
        assertArrayEquals(initial.score(), zero.initialScore());
        assertTrue(zero.stats().elapsedMillis() >= 0L);

        AlnsResult one = AlnsSolver.withDefaults(config(1L, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), 1L, 0.05))
                .solve(problem, PROFILE);
        assertEquals(Termination.TIME_LIMIT, one.stats().termination());
        assertTrue(one.stats().iterations() > 0L);
        assertTrue(one.stats().elapsedMillis() >= 1_000L, "elapsed " + one.stats().elapsedMillis());
        assertTrue(StructureCheck.check(problem, one.best()).isEmpty());
        assertTrue(Scores.compare(one.bestScore(), one.initialScore()) <= 0);
    }

    /** T4 — 보고된 bestEvaluation·bestScore = best를 새로 정식 평가한 값 (§6.4 캐시 = 재계산). */
    @Test
    void reportedEvaluationMatchesFreshEvaluation() {
        Problem problem = smallFixture();
        AlnsResult result = AlnsSolver.withDefaults(steps(300L, 3L)).solve(problem, PROFILE);

        EvaluationResult fresh = Evaluator.evaluate(problem, PROFILE, result.best());
        EvaluationResult.Feasible feasible = assertInstanceOf(EvaluationResult.Feasible.class, fresh);
        assertEquals(feasible.evaluation(), result.bestEvaluation());
        assertArrayEquals(feasible.score(), result.bestScore());
    }

    /** T5 — 같은 seed 두 번 → best·bestEvaluation 동등, bestScore Arrays.equals, 카운터 동일 (E14 조건: maxSteps 종료 + worse 확률 0). */
    @Test
    void sameSeedSameResult() {
        Problem problem = smallFixture();
        AlnsConfig config = config(60L, OptionalLong.of(400L), OptionalLong.empty(), OptionalLong.empty(), 11L, 0.0);
        AlnsResult first = AlnsSolver.withDefaults(config).solve(problem, PROFILE);
        AlnsResult second = AlnsSolver.withDefaults(config).solve(problem, PROFILE);

        assertEquals(first.best(), second.best());
        assertEquals(first.bestEvaluation(), second.bestEvaluation());
        assertArrayEquals(first.bestScore(), second.bestScore());
        assertEquals(first.stats().iterations(), second.stats().iterations());
        assertEquals(first.stats().accepted(), second.stats().accepted());
        assertEquals(first.stats().bestImproved(), second.stats().bestImproved());
        assertEquals(first.stats().infeasibleDiscarded(), second.stats().infeasibleDiscarded());
    }

    /** T9 — E1(주문 0)·E2(차량 0 / 전 Request 호환 0대) → 예외 없이 유효한 AlnsResult. */
    @Test
    void degenerateProblemsReturnValidResult() {
        Problem noRequests = ConstructionFixtures.ring(0, 1, 30_000L, 1_000L);
        AlnsResult empty = AlnsSolver.withDefaults(steps(20L, 1L)).solve(noRequests, PROFILE);
        assertTrue(empty.best().routes().isEmpty());
        assertTrue(empty.best().bank().isEmpty());
        assertEquals(20L, empty.stats().iterations());
        assertEquals(Termination.MAX_STEPS, empty.stats().termination());
        assertTrue(StructureCheck.check(noRequests, empty.best()).isEmpty());
        assertEquals(0, empty.bestEvaluation().unassignedCount());

        Problem noVehicles = ConstructionFixtures.ring(3, 0, 30_000L, 1_000L);
        AlnsResult banked = AlnsSolver.withDefaults(steps(20L, 1L)).solve(noVehicles, PROFILE);
        assertTrue(banked.best().routes().isEmpty());
        assertEquals(ConstructionFixtures.requestIds(noVehicles), banked.best().bank());
        assertEquals(3, banked.bestEvaluation().unassignedCount());
        assertTrue(StructureCheck.check(noVehicles, banked.best()).isEmpty());

        // 차량은 있으나 startDepot이 없어 DELIVERY_ONLY 전부 호환 0대 (Domain §3.4 depotAnchors)
        Problem incompatible = ConstructionFixtures.freeze(
                List.of(new Depot(ConstructionFixtures.DEPOT, List.of(ConstructionFixtures.ALL_DAY), Optional.empty())),
                List.of(ConstructionFixtures.delivery("R1", new LocationId("A"), 1_000L)),
                List.of(ConstructionFixtures.vehicle("V1", 30_000L, Optional.empty(), Optional.of(ConstructionFixtures.DEPOT))),
                ConstructionFixtures.locations(
                        ConstructionFixtures.at(ConstructionFixtures.DEPOT, 37.0, 127.0),
                        ConstructionFixtures.at(new LocationId("A"), 37.01, 127.0)),
                List.of());
        AlnsResult unassignable = AlnsSolver.withDefaults(steps(20L, 1L)).solve(incompatible, PROFILE);
        assertEquals(Set.of(new RequestId("R1")), unassignable.best().bank());
        assertTrue(unassignable.best().routes().isEmpty());
    }

    /** T10 — 작은 idleSteps / idleSec으로 한도 훨씬 전에 종료. worse 수락(확률 1)이 idle 카운터를 리셋하지 않는다. */
    @Test
    void stopsOnIdleLimits() {
        Problem problem = smallFixture();
        long idleSteps = 30L;
        AlnsResult bySteps = AlnsSolver.withDefaults(
                config(60L, OptionalLong.empty(), OptionalLong.of(idleSteps), OptionalLong.empty(), 5L, 1.0))
                .solve(problem, PROFILE);
        assertEquals(Termination.IDLE_STEPS, bySteps.stats().termination());
        assertTrue(bySteps.stats().elapsedMillis() < 30_000L);
        // worse 수락이 리셋했다면 반복 수가 (best 갱신 수 + 1) × idleSteps를 넘을 수 있다 — 넘지 않아야 한다
        assertTrue(bySteps.stats().iterations() <= (bySteps.stats().bestImproved() + 1L) * idleSteps,
                "iterations " + bySteps.stats().iterations() + " bestImproved " + bySteps.stats().bestImproved());
        assertTrue(bySteps.stats().accepted() > bySteps.stats().bestImproved(), "worse/tie acceptances must have happened");
        assertTrue(StructureCheck.check(problem, bySteps.best()).isEmpty());

        AlnsResult byTime = AlnsSolver.withDefaults(
                config(60L, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.of(1L), 5L, 1.0))
                .solve(problem, PROFILE);
        assertEquals(Termination.IDLE_TIME, byTime.stats().termination());
        assertTrue(byTime.stats().elapsedMillis() >= 1_000L);
        assertTrue(byTime.stats().elapsedMillis() < 30_000L, "elapsed " + byTime.stats().elapsedMillis());
        assertTrue(byTime.stats().accepted() > byTime.stats().bestImproved());
    }

    /** T12 — 테스트 전용 hard("경로당 방문 1개 초과 금지")로 solve: 초기해 Feasible·넣지 못한 Request는 bank·루프 예외 없음.
     *  별도 케이스: 오버로드에 Infeasible / 구조 위반 손 조립 초기해 → IllegalStateException (§4.2-2). */
    @Test
    void profileHardIsHonoredFromConstruction() {
        Problem problem = ConstructionFixtures.ring(6, 3, 30_000L, 5_000L);
        Profile maxOne = ConstructionFixtures.maxVisitsProfile(1);
        AlnsResult result = AlnsSolver.withDefaults(steps(200L, 2L)).solve(problem, maxOne);
        assertEquals(3, result.initialEvaluation().unassignedCount());
        assertEquals(3, result.bestEvaluation().unassignedCount());
        assertEquals(3, result.best().routes().size());
        for (Route route : result.best().routes()) {
            assertEquals(1, route.visits().size());
        }
        assertEquals(0L, result.stats().infeasibleDiscarded());          // 후보 검증이 profile hard를 봐서 Infeasible draft가 없다
        assertEquals(200L, result.stats().iterations());
        assertTrue(StructureCheck.check(problem, result.best()).isEmpty());

        RequestId r1 = new RequestId("R1");
        RequestId r2 = new RequestId("R2");
        Solution twoVisits = new Solution(
                List.of(new Route(new VehicleId("V1"), List.of(NodeId.delivery(r1), NodeId.delivery(r2)))),
                Set.of(new RequestId("R3"), new RequestId("R4"), new RequestId("R5"), new RequestId("R6")));
        assertInstanceOf(EvaluationResult.Infeasible.class, Evaluator.evaluate(problem, maxOne, twoVisits));
        assertThrows(IllegalStateException.class,
                () -> AlnsSolver.withDefaults(steps(10L, 1L)).solve(problem, maxOne, twoVisits));

        Solution broken = new Solution(twoVisits.routes(), ConstructionFixtures.requestIds(problem));   // R1·R2가 경로와 bank 양쪽
        assertThrows(IllegalStateException.class,
                () -> AlnsSolver.withDefaults(steps(10L, 1L)).solve(problem, PROFILE, broken));
    }

    /**
     * T10b — worseAcceptStartProbability = 1.0에서 축 가드(§4.5): 미배정이 늘어난 draft는 절대 수락되지 않고,
     * 앞 두 축이 같고 거리만 나쁜 draft는 수락된다. 둘 다 Feasible이라 폐기(E10)로 통과하는 일은 없다.
     */
    @Test
    void worseAcceptanceRequiresEqualLeadingAxes() {
        Problem problem = axisGuardFixture();
        VehicleId v1 = new VehicleId("V1");
        NodeId a = NodeId.delivery(new RequestId("RA"));
        NodeId b = NodeId.delivery(new RequestId("RB"));
        NodeId c = NodeId.delivery(new RequestId("RC"));
        Solution initial = new Solution(List.of(new Route(v1, List.of(a, b, c))), Set.of());
        Solution worseDistance = new Solution(List.of(new Route(v1, List.of(b, a, c))), Set.of());
        Solution moreUnassigned = new Solution(List.of(new Route(v1, List.of(a, b))), Set.of(new RequestId("RC")));

        long[] initialScore = feasibleScore(problem, initial);
        long[] worseScore = feasibleScore(problem, worseDistance);
        long[] unassignedScore = feasibleScore(problem, moreUnassigned);
        assertEquals(300L, initialScore[2]);
        assertEquals(1_500L, worseScore[2]);                                  // 앞 두 축은 같고 거리만 나쁘다
        assertEquals(initialScore[0], worseScore[0]);
        assertEquals(initialScore[1], worseScore[1]);
        assertEquals(initialScore[0] + 1L, unassignedScore[0]);               // 미배정이 1건 는다

        AlnsConfig always = config(60L, OptionalLong.of(30L), OptionalLong.empty(), OptionalLong.empty(), 4L, 1.0);

        AlnsResult blocked = new AlnsSolver(always, List.of(noopDestroy()), List.of(fixedRepair(moreUnassigned)))
                .solve(problem, PROFILE, initial);
        assertEquals(30L, blocked.stats().iterations());
        assertEquals(0L, blocked.stats().infeasibleDiscarded());
        assertEquals(0L, blocked.stats().accepted(), "미배정이 는 draft는 확률 1.0에서도 수락되면 안 된다");
        assertEquals(initial, blocked.best());

        AlnsResult drifted = new AlnsSolver(always, List.of(noopDestroy()), List.of(fixedRepair(worseDistance)))
                .solve(problem, PROFILE, initial);
        assertEquals(30L, drifted.stats().iterations());
        assertEquals(0L, drifted.stats().infeasibleDiscarded());
        assertTrue(drifted.stats().accepted() >= 1L, "앞 축이 같고 거리만 나쁜 draft는 수락돼야 한다");
        assertEquals(0L, drifted.stats().bestImproved());
        assertEquals(initial, drifted.best());                                // best 갱신은 strict <
    }

    private static long[] feasibleScore(Problem problem, Solution solution) {
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        return assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, PROFILE, solution)).score();
    }

    /** 아무것도 빼지 않는 테스트 전용 destroy — draft를 repair가 통째로 정한다. */
    private static DestroyOperator noopDestroy() {
        return new DestroyOperator() {
            @Override
            public String id() {
                return "noop-destroy";
            }

            @Override
            public Solution destroy(Problem problem, Solution current, int removeCount, java.util.random.RandomGenerator rng) {
                return current;
            }
        };
    }

    /** 늘 같은 해를 내는 테스트 전용 repair — acceptance만 시험한다. */
    private static RepairOperator fixedRepair(Solution fixed) {
        return new RepairOperator() {
            @Override
            public String id() {
                return "fixed-repair";
            }

            @Override
            public Solution repair(Problem problem, Profile profile, Solution destroyed, java.util.random.RandomGenerator rng) {
                return fixed;
            }
        };
    }

    /**
     * T10b fixture — 전 arc 100 m/100 s이되 D0→B·B→A·A→C만 500 m다. 방문 [A, B, C]는 300 m,
     * [B, A, C]는 1,500 m이고 둘 다 Feasible이라 앞 두 축(미배정 0·차량 1)이 같고 거리만 갈린다.
     */
    static Problem axisGuardFixture() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        LocationId c = new LocationId("C");
        List<LocationId> ids = List.of(ConstructionFixtures.DEPOT, a, b, c);
        List<TravelEntry> travel = new ArrayList<>();
        for (LocationId from : ids) {
            for (LocationId to : ids) {
                if (from.equals(to)) {
                    continue;
                }
                boolean far = (from.equals(ConstructionFixtures.DEPOT) && to.equals(b))
                        || (from.equals(b) && to.equals(a))
                        || (from.equals(a) && to.equals(c));
                travel.add(new TravelEntry(from, to, far ? 500 : 100, 100));
            }
        }
        return ConstructionFixtures.freeze(
                List.of(new Depot(ConstructionFixtures.DEPOT, List.of(ConstructionFixtures.ALL_DAY), Optional.empty())),
                List.of(
                        ConstructionFixtures.delivery("RA", a, 1_000L),
                        ConstructionFixtures.delivery("RB", b, 1_000L),
                        ConstructionFixtures.delivery("RC", c, 1_000L)),
                List.of(ConstructionFixtures.vehicle("V1", 30_000L, Optional.of(ConstructionFixtures.DEPOT), Optional.empty())),
                ConstructionFixtures.locations(
                        ConstructionFixtures.at(ConstructionFixtures.DEPOT, 37.0, 127.0),
                        ConstructionFixtures.at(a, 37.01, 127.0),
                        ConstructionFixtures.at(b, 37.02, 127.0),
                        ConstructionFixtures.at(c, 37.03, 127.0)),
                travel);
    }

    /**
     * T1 fixture (2026-09-02 실측으로 고정) — depot 왕복 차량 2대(무게 20,000), DELIVERY_ONLY 10건(4시간 창) +
     * PICKUP_DELIVERY 2건, 좌표 유래 이동표. 포트폴리오 최선은 spatiotemporal-cluster [0, 2, 44151, 37307]이고
     * ALNS가 [0, 2, 38764, ·]에 닿는다. 순수 배송 소형 문제는 포트폴리오가 이미 최적에 닿는 경우가 많아 PD를 섞었다.
     */
    static Problem smallFixture() {
        double[][] deliveries = {
            {37.018492, 127.032116, 1_000, 39_600}, {37.038870, 127.028570, 2_000, 36_000},
            {36.978253, 126.965984, 5_000, 32_400}, {37.035967, 126.962441, 3_000, 46_800},
            {37.028135, 126.964540, 3_000, 39_600}, {36.988191, 127.039495, 5_000, 43_200},
            {36.998079, 126.963953, 1_000, 36_000}, {37.028099, 126.978294, 4_000, 43_200},
            {37.037114, 126.978610, 2_000, 43_200}, {37.008712, 126.979810, 4_000, 36_000},
        };
        double[][] pairs = {
            {37.015120, 127.023813, 36.969898, 126.968606},
            {36.962621, 127.034613, 36.981346, 127.004353},
        };
        List<Location> locations = new ArrayList<>();
        locations.add(ConstructionFixtures.at(ConstructionFixtures.DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < deliveries.length; i++) {
            LocationId id = new LocationId("L" + (i + 1));
            locations.add(ConstructionFixtures.at(id, deliveries[i][0], deliveries[i][1]));
            long open = (long) deliveries[i][3];
            requests.add(ConstructionFixtures.delivery(
                    "R" + (i + 1), id, (long) deliveries[i][2], new TimeWindow(open, open + 4L * 3_600L), Optional.empty()));
        }
        for (int i = 0; i < pairs.length; i++) {
            LocationId from = new LocationId("P" + (i + 1));
            LocationId to = new LocationId("Q" + (i + 1));
            locations.add(ConstructionFixtures.at(from, pairs[i][0], pairs[i][1]));
            locations.add(ConstructionFixtures.at(to, pairs[i][2], pairs[i][3]));
            requests.add(ConstructionFixtures.pickupDelivery("PD" + (i + 1), from, to, 2_000L));
        }
        List<Vehicle> vehicles = List.of(
                ConstructionFixtures.vehicle("V1", 20_000L, Optional.of(ConstructionFixtures.DEPOT), Optional.of(ConstructionFixtures.DEPOT)),
                ConstructionFixtures.vehicle("V2", 20_000L, Optional.of(ConstructionFixtures.DEPOT), Optional.of(ConstructionFixtures.DEPOT)));
        return ConstructionFixtures.freeze(
                List.of(new Depot(ConstructionFixtures.DEPOT, List.of(ConstructionFixtures.ALL_DAY), Optional.empty())),
                requests,
                vehicles,
                ConstructionFixtures.locations(locations.toArray(Location[]::new)),
                List.of());
    }
}
