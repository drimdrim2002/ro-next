package com.ronext.rpdptw.verify;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.EvaluationResult;
import com.ronext.rpdptw.solve.Evaluator;
import com.ronext.rpdptw.solve.Route;
import com.ronext.rpdptw.solve.Solution;
import com.ronext.rpdptw.verify.VerifyViolation.Kind;

/** stage-05 §8 T1·T2·T3·T5·T6·T7·T12·T13. */
class SolutionVerifierTest {

    private static final Profile PROFILE = new DefaultProfile();

    // --- T5: 유효 해는 Pass이고, 두 독립 구현의 값이 합치한다 ---

    @Test
    void passesValidSolutionAndAgreesWithSolve() {
        Problem problem = VerifyFixtures.baseProblem();
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        EvaluationResult.Feasible reported = evaluate(problem, PROFILE, routes, Set.of());

        VerificationResult result = SolutionVerifier.verify(
                problem, PROFILE, routes, Set.of(), reported.evaluation(), reported.score());

        VerificationResult.Pass pass = pass(result);
        assertEquals(reported.evaluation(), pass.evaluation());
        assertArrayEquals(reported.score(), pass.score());
        assertEquals(reported.routes(), pass.routeFacts());
        assertEquals(Set.of(), pass.bank());

        // Domain §7.2 → stage-03 §3.4 목표값 (창이 하나라 미루기·휴식이 없다)
        RouteFacts facts = pass.routeFacts().get(VerifyFixtures.V1);
        assertEquals(28_800L, facts.spanStartSec());
        assertEquals(28_800L, facts.departureSec().orElseThrow());
        assertEquals(10_000L, facts.initialLoadWeight());
        assertEquals(6_000L, facts.driveTimeSec());
        assertEquals(27_000L, facts.driveDistMeter());
        assertEquals(12_300L, facts.customerWaitingTimeSec());
        assertEquals(0L, facts.depotWaitingTimeSec());
        assertEquals(1_440L, facts.serviceTimeSec());
        assertEquals(0L, facts.interWorkWindowRestTimeSec());
        assertEquals(3, facts.stopCount());
        assertIdentity(facts);
    }

    @Test
    void passesMultiDaySolutionAndAgreesWithSolve() {
        Problem problem = VerifyFixtures.multiDayProblem();
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.multiDayRoute());
        Set<RequestId> bank = Set.of(new RequestId("R5"));
        EvaluationResult.Feasible reported = evaluate(problem, PROFILE, routes, bank);

        VerificationResult.Pass pass = pass(SolutionVerifier.verify(
                problem, PROFILE, routes, bank, reported.evaluation(), reported.score()));

        // E19 — 두 구현이 창을 다르게 걸으면 여기서 드러난다 (방문별 departureSec·휴식까지 대조)
        assertEquals(reported.routes(), pass.routeFacts());
        RouteFacts facts = pass.routeFacts().get(VerifyFixtures.V1);
        assertEquals(54_000L, facts.interWorkWindowRestTimeSec());
        assertEquals(26_700L, facts.customerWaitingTimeSec());
        assertEquals(115_200L, facts.visits().get(1).serviceStartSec());
        assertEquals(31_500L, facts.visits().get(0).departureSec());
        assertIdentity(facts);
    }

    @Test
    void passesWaitInDepotAndWindowBoundaries() {
        // E9 — Y는 출발만 늦춘다. serviceStart 시각들은 N과 같고 대기의 귀속만 옮겨진다.
        Problem waiting = VerifyFixtures.waitInDepotProblem();
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        EvaluationResult.Feasible reported = evaluate(waiting, PROFILE, routes, Set.of());
        VerificationResult.Pass pass = pass(SolutionVerifier.verify(
                waiting, PROFILE, routes, Set.of(), reported.evaluation(), reported.score()));
        RouteFacts facts = pass.routeFacts().get(VerifyFixtures.V1);
        assertEquals(reported.routes(), pass.routeFacts());
        assertEquals(30_000L, facts.departureSec().orElseThrow());
        assertEquals(1_200L, facts.depotWaitingTimeSec());
        assertEquals(11_100L, facts.customerWaitingTimeSec());
        assertEquals(32_400L, facts.visits().get(0).serviceStartSec());
        assertIdentity(facts);

        // E8 — serviceStart == 그 창의 close == reqDate. 양끝 포함이라 통과한다.
        Problem boundary = VerifyFixtures.boundaryProblem();
        EvaluationResult.Feasible boundaryReported = evaluate(boundary, PROFILE, routes, Set.of());
        VerificationResult.Pass boundaryPass = pass(SolutionVerifier.verify(
                boundary, PROFILE, routes, Set.of(), boundaryReported.evaluation(), boundaryReported.score()));
        assertEquals(boundaryReported.routes(), boundaryPass.routeFacts());
        assertEquals(46_800L, boundaryPass.routeFacts().get(VerifyFixtures.V1).visits().get(1).serviceStartSec());
    }

    // --- T1: 짝 분리 ---

    @Test
    void failsOnPairSplit() {
        Problem problem = VerifyFixtures.baseProblem();
        Map<VehicleId, List<NodeId>> routes = new LinkedHashMap<>();
        routes.put(VerifyFixtures.V1, List.of(
                NodeId.delivery(VerifyFixtures.R1), NodeId.delivery(VerifyFixtures.R2)));
        routes.put(VerifyFixtures.V2, List.of(NodeId.pickup(VerifyFixtures.R2)));

        VerificationResult result = SolutionVerifier.verify(
                problem, PROFILE, routes, Set.of(), new Evaluation(0, 2, 0L, 0L), new long[] {0, 2, 0, 0});

        assertEquals(List.of(Kind.PAIR_SPLIT), kinds(result));
        assertEquals(VerifyFixtures.R2, only(result).requestId().orElseThrow());
    }

    // --- T2: 용량 초과 ---

    @Test
    void failsOnCapacityExceeded() {
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        Evaluation any = new Evaluation(0, 1, 0L, 0L);
        long[] anyScore = {0, 1, 0, 0};

        VerificationResult weight = SolutionVerifier.verify(
                VerifyFixtures.baseProblem(15_000L, 1_000_000L), PROFILE, routes, Set.of(), any, anyScore);
        assertEquals(List.of(Kind.CAPACITY_WEIGHT), kinds(weight));
        assertEquals(NodeId.pickup(VerifyFixtures.R2), only(weight).at().orElseThrow());

        VerificationResult volume = SolutionVerifier.verify(
                VerifyFixtures.baseProblem(1_000_000L, 7_000L), PROFILE, routes, Set.of(), any, anyScore);
        assertEquals(List.of(Kind.CAPACITY_VOLUME), kinds(volume));

        // E4 — 출발 적재(DELIVERY_ONLY 몰림)만으로 초과. 방문 전이라 at은 비어 있다.
        VerificationResult initial = SolutionVerifier.verify(
                VerifyFixtures.baseProblem(5_000L, 1_000_000L), PROFILE, routes, Set.of(), any, anyScore);
        assertEquals(List.of(Kind.CAPACITY_WEIGHT), kinds(initial));
        assertTrue(only(initial).at().isEmpty());
    }

    // --- T3: 점수 불일치 ---

    @Test
    void failsOnScoreMismatch() {
        Problem problem = VerifyFixtures.baseProblem();
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        EvaluationResult.Feasible reported = evaluate(problem, PROFILE, routes, Set.of());
        Evaluation good = reported.evaluation();

        List<Evaluation> tampered = List.of(
                new Evaluation(good.unassignedCount() + 1, good.usedVehicleCount(),
                        good.totalDistanceMeter(), good.totalRouteOperationalTimeSec()),
                new Evaluation(good.unassignedCount(), good.usedVehicleCount() + 1,
                        good.totalDistanceMeter(), good.totalRouteOperationalTimeSec()),
                new Evaluation(good.unassignedCount(), good.usedVehicleCount(),
                        good.totalDistanceMeter() - 1L, good.totalRouteOperationalTimeSec()),
                new Evaluation(good.unassignedCount(), good.usedVehicleCount(),
                        good.totalDistanceMeter(), good.totalRouteOperationalTimeSec() + 1L));
        for (Evaluation evaluation : tampered) {
            VerificationResult result = SolutionVerifier.verify(
                    problem, PROFILE, routes, Set.of(), evaluation, reported.score());
            assertTrue(kinds(result).contains(Kind.SCORE_MISMATCH), "expected mismatch for " + evaluation);
        }

        // Evaluation은 그대로 두고 score만 조작 — 8·9는 독립 검사다
        long[] tamperedScore = reported.score();
        tamperedScore[2] += 1L;
        assertEquals(List.of(Kind.SCORE_MISMATCH),
                kinds(SolutionVerifier.verify(problem, PROFILE, routes, Set.of(), good, tamperedScore)));

        // E17 — 길이가 다른 score도 SCORE_MISMATCH다 (예외가 아니다)
        assertEquals(List.of(Kind.SCORE_MISMATCH),
                kinds(SolutionVerifier.verify(problem, PROFILE, routes, Set.of(), good, new long[] {0, 1, 2})));
    }

    // --- T6: XOR·참조 위반 ---

    @Test
    void failsOnXorViolations() {
        Problem problem = VerifyFixtures.baseProblem();
        Set<RequestId> all = Set.of(VerifyFixtures.R1, VerifyFixtures.R2);

        // E1 — 경로 0개, 전 Request bank는 유효한 해다
        VerificationResult.Pass empty = pass(SolutionVerifier.verify(
                problem, PROFILE, Map.of(), all, new Evaluation(2, 0, 0L, 0L), new long[] {2, 0, 0, 0}));
        assertEquals(new Evaluation(2, 0, 0L, 0L), empty.evaluation());
        assertEquals(all, empty.bank());

        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        Evaluation any = new Evaluation(0, 1, 0L, 0L);
        long[] anyScore = {0, 1, 0, 0};

        assertEquals(List.of(Kind.ASSIGNED_AND_BANKED),
                kinds(SolutionVerifier.verify(problem, PROFILE, routes, Set.of(VerifyFixtures.R1), any, anyScore)));

        assertEquals(List.of(Kind.NOT_ASSIGNED_NOT_BANKED),
                kinds(SolutionVerifier.verify(
                        problem, PROFILE,
                        routes(VerifyFixtures.V1, List.of(
                                NodeId.pickup(VerifyFixtures.R2), NodeId.delivery(VerifyFixtures.R2))),
                        Set.of(), any, anyScore)));

        assertEquals(List.of(Kind.UNKNOWN_REQUEST),
                kinds(SolutionVerifier.verify(
                        problem, PROFILE, routes, Set.of(new RequestId("NOPE")), any, anyScore)));

        // E23 — 빈 경로. verify 입력은 raw 맵이라 Route가 못 만드는 상태도 들어올 수 있다
        Map<VehicleId, List<NodeId>> withEmpty = new LinkedHashMap<>(routes);
        withEmpty.put(VerifyFixtures.V2, List.of());
        assertEquals(List.of(Kind.EMPTY_ROUTE),
                kinds(SolutionVerifier.verify(problem, PROFILE, withEmpty, Set.of(), any, anyScore)));

        // E5 — DELIVERY_ONLY의 가짜 픽업 NodeId는 색인에 없다
        assertTrue(kinds(SolutionVerifier.verify(
                problem, PROFILE,
                routes(VerifyFixtures.V1, List.of(NodeId.pickup(VerifyFixtures.R1))),
                Set.of(), any, anyScore)).contains(Kind.UNKNOWN_NODE));
    }

    // --- T7: 호환성과 profile hard ---

    @Test
    void failsOnIncompatibleAndProfileHard() {
        // E14 — R1은 "T1" 차급만 허용하는데 V1은 "T2"다
        Problem problem = VerifyFixtures.baseProblem(
                30_000L, 1_000_000L, java.util.Optional.of("T1"), java.util.Optional.of("T2"));
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        VerificationResult incompatible = SolutionVerifier.verify(
                problem, PROFILE, routes, Set.of(), new Evaluation(0, 1, 0L, 0L), new long[] {0, 1, 0, 0});
        assertEquals(List.of(Kind.INCOMPATIBLE_VEHICLE), kinds(incompatible));
        assertEquals(VerifyFixtures.R1, only(incompatible).requestId().orElseThrow());
        assertEquals(NodeId.delivery(VerifyFixtures.R1), only(incompatible).at().orElseThrow());

        // E15 — 인자로 받은 profile을 그대로 적용한다 (탐색과 같은 인스턴스인지는 호출자 책임)
        Problem plain = VerifyFixtures.baseProblem();
        Profile rejecting = new RejectingProfile();
        VerificationResult hard = SolutionVerifier.verify(
                plain, rejecting, routes, Set.of(), new Evaluation(0, 1, 0L, 0L), new long[] {0, 1, 0, 0});
        assertEquals(List.of(Kind.PROFILE_HARD), kinds(hard));
        assertEquals(RejectingProfile.CONSTRAINT_ID, only(hard).detail());
    }

    // --- T12: 근무창·차고 창 ---

    @Test
    void failsOnWorkAndDepotWindow() {
        Problem problem = VerifyFixtures.multiDayProblem();
        Set<RequestId> r5 = Set.of(new RequestId("R5"));

        // ③ 대조군 — 오염이 없으면 Pass
        Map<VehicleId, List<NodeId>> clean = routes(VerifyFixtures.V1, VerifyFixtures.multiDayRoute());
        EvaluationResult.Feasible reported = evaluate(problem, PROFILE, clean, r5);
        assertInstanceOf(VerificationResult.Pass.class, SolutionVerifier.verify(
                problem, PROFILE, clean, r5, reported.evaluation(), reported.score()));

        // ① 어느 근무창(9시간)에도 통째로 들어가지 않는 12시간 이동 (E20)
        List<NodeId> straddling = new ArrayList<>(VerifyFixtures.multiDayRoute());
        straddling.add(NodeId.delivery(new RequestId("R5")));
        assertEquals(List.of(Kind.WORK_WINDOW), kinds(SolutionVerifier.verify(
                problem, PROFILE, routes(VerifyFixtures.V1, straddling), Set.of(),
                new Evaluation(0, 1, 0L, 0L), new long[] {0, 1, 0, 0})));

        // ② endDepot 도착이 차고 창 밖 — 미루지 않는다 (E21)
        Map<VehicleId, List<NodeId>> endRoute =
                routes(VerifyFixtures.V1, List.of(NodeId.delivery(VerifyFixtures.R1)));
        Problem open = VerifyFixtures.endDepotProblem(List.of(VerifyFixtures.ALL_DAY));
        EvaluationResult.Feasible openReported = evaluate(open, PROFILE, endRoute, Set.of());
        assertInstanceOf(VerificationResult.Pass.class, SolutionVerifier.verify(
                open, PROFILE, endRoute, Set.of(), openReported.evaluation(), openReported.score()));

        Problem closed = VerifyFixtures.endDepotProblem(List.of(new TimeWindow(0L, 3_600L)));
        assertEquals(List.of(Kind.DEPOT_WINDOW), kinds(SolutionVerifier.verify(
                closed, PROFILE, endRoute, Set.of(), new Evaluation(0, 1, 0L, 0L), new long[] {0, 1, 0, 0})));
    }

    // --- T13: 경로 구역 단일성 ---

    @Test
    void failsOnZoneMix() {
        Problem problem = VerifyFixtures.zoneProblem();
        NodeId za1 = NodeId.delivery(new RequestId("ZA1"));
        NodeId za2 = NodeId.delivery(new RequestId("ZA2"));
        NodeId zb = NodeId.delivery(new RequestId("ZB"));
        NodeId zn = NodeId.delivery(new RequestId("ZN"));

        // A → ALL → A 는 구체 구역이 하나뿐이라 통과한다
        Map<VehicleId, List<NodeId>> clean = routes(VerifyFixtures.V1, List.of(za1, zn, za2));
        Set<RequestId> cleanBank = Set.of(new RequestId("ZB"));
        EvaluationResult.Feasible reported = evaluate(problem, PROFILE, clean, cleanBank);
        assertInstanceOf(VerificationResult.Pass.class, SolutionVerifier.verify(
                problem, PROFILE, clean, cleanBank, reported.evaluation(), reported.score()));

        // A → ALL → B 는 구체 구역이 둘 (E25)
        VerificationResult mixed = SolutionVerifier.verify(
                problem, PROFILE, routes(VerifyFixtures.V1, List.of(za1, zn, zb)),
                Set.of(new RequestId("ZA2")), new Evaluation(1, 1, 0L, 0L), new long[] {1, 1, 0, 0});
        assertEquals(List.of(Kind.ZONE_MIX), kinds(mixed));
        assertEquals(zb, only(mixed).at().orElseThrow());
    }

    // --- 헬퍼 ---

    private static Map<VehicleId, List<NodeId>> routes(VehicleId vehicleId, List<NodeId> visits) {
        Map<VehicleId, List<NodeId>> routes = new LinkedHashMap<>();
        routes.put(vehicleId, visits);
        return routes;
    }

    private static EvaluationResult.Feasible evaluate(
            Problem problem, Profile profile, Map<VehicleId, List<NodeId>> routes, Set<RequestId> bank) {
        List<Route> routeList = new ArrayList<>();
        routes.forEach((vehicleId, visits) -> routeList.add(new Route(vehicleId, visits)));
        EvaluationResult result = Evaluator.evaluate(problem, profile, new Solution(routeList, bank));
        return assertInstanceOf(EvaluationResult.Feasible.class, result);
    }

    private static VerificationResult.Pass pass(VerificationResult result) {
        return assertInstanceOf(VerificationResult.Pass.class, result);
    }

    private static List<Kind> kinds(VerificationResult result) {
        return assertInstanceOf(VerificationResult.Fail.class, result)
                .violations().stream().map(VerifyViolation::kind).toList();
    }

    private static VerifyViolation only(VerificationResult result) {
        List<VerifyViolation> violations = assertInstanceOf(VerificationResult.Fail.class, result).violations();
        assertEquals(1, violations.size(), violations.toString());
        return violations.get(0);
    }

    /** Domain §7.3 항등식 — 두 구현이 어긋났는지 보는 가장 싼 검사. */
    private static void assertIdentity(RouteFacts facts) {
        assertEquals(facts.routeEndSec() - facts.spanStartSec(), facts.routeOperationalTimeSec());
    }

    private static final class RejectingProfile extends DefaultProfile {

        static final String CONSTRAINT_ID = "always-false";

        @Override
        public List<HardConstraint> hardConstraints() {
            return List.of(new HardConstraint() {
                @Override
                public String id() {
                    return CONSTRAINT_ID;
                }

                @Override
                public boolean satisfied(Problem problem, RouteFacts route) {
                    return false;
                }
            });
        }
    }
}
