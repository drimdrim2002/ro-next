package com.ronext.rpdptw.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.EvaluationResult;
import com.ronext.rpdptw.solve.Evaluator;
import com.ronext.rpdptw.solve.Route;
import com.ronext.rpdptw.solve.Solution;

/** stage-05 §8 T9·T10·T11. */
class ResultAssemblerTest {

    private static final Profile PROFILE = new DefaultProfile();

    private static final RunStamp STAMP = new RunStamp(
            "solves/cust/P1/run-1/input.json",
            LocalDateTime.of(2023, 9, 13, 8, 0, 0),
            LocalDateTime.of(2023, 9, 13, 8, 0, 5),
            LocalDateTime.of(2023, 9, 13, 8, 10, 0),
            new SolveResult.SearchBudget(
                    60L, OptionalLong.of(1_000L), OptionalLong.empty(), OptionalLong.empty(), 7L));

    // --- T9 ---

    @Test
    void resultMatchesVerifiedSolution() {
        Problem problem = VerifyFixtures.multiDayProblem();
        Map<VehicleId, List<NodeId>> routes = routes(VerifyFixtures.V1, VerifyFixtures.multiDayRoute());
        Set<RequestId> bank = Set.of(new RequestId("R5"));
        VerificationResult.Pass pass = verify(problem, routes, bank);

        SolveResult result = ResultAssembler.assemble(problem, PROFILE, pass, STAMP);

        assertEquals("P1", result.planId());
        assertEquals(SolveResult.Status.DONE, result.status());
        assertEquals(pass.evaluation(), result.metrics());

        // 모든 Request가 routes ∪ unassigned에 정확히 1회 (PD는 방문 2건이라 Request 단위로 센다)
        List<RequestId> seen = new ArrayList<>();
        result.routes().forEach(route -> route.visits().stream()
                .map(SolveResult.Visit::orderId)
                .distinct()
                .forEach(seen::add));
        result.unassigned().forEach(unassigned -> seen.add(unassigned.orderId()));
        assertEquals(problem.requests().size(), seen.size());
        assertEquals(
                problem.requests().stream().map(Request::id).collect(java.util.stream.Collectors.toSet()),
                new LinkedHashSet<>(seen));

        TimeBase timeBase = problem.timeBase();
        SolveResult.RouteResult route = result.routes().get(0);
        RouteFacts facts = pass.routeFacts().get(VerifyFixtures.V1);
        assertEquals(facts.vehicleId(), route.vehicleId());
        assertEquals(facts.driveDistMeter(), route.driveDistMeter());
        assertEquals(facts.driveTimeSec(), route.driveTimeSec());
        assertEquals(facts.stopCount(), route.stopCount());
        assertEquals(facts.routeOperationalTimeSec(), route.routeOperationalTimeSec());
        assertEquals(Optional.of(timeBase.toWallClock(facts.departureSec().orElseThrow())), route.depotDeparture());
        assertEquals(Optional.empty(), route.depotReturn());          // endDepot 없는 경로
        assertEquals(facts.visits().size(), route.visits().size());
        for (int i = 0; i < route.visits().size(); i++) {
            VisitFacts expected = facts.visits().get(i);
            SolveResult.Visit visit = route.visits().get(i);
            assertEquals(expected.requestId(), visit.orderId());
            assertEquals(expected.pickup(), visit.pickup());
            assertEquals(expected.locationId(), visit.locationId());
            assertEquals(timeBase.toWallClock(expected.arrivalSec()), visit.arrival());
            assertEquals(timeBase.toWallClock(expected.serviceStartSec()), visit.serviceStart());
            assertEquals(timeBase.toWallClock(expected.serviceEndSec()), visit.serviceEnd());
            assertEquals(expected.loadWeightAfter(), visit.loadWeight());
            assertEquals(expected.loadVolumeAfter(), visit.loadVolume());
        }

        SolveResult.Run run = result.run();
        assertEquals(STAMP.inputKey(), run.inputKey());
        assertEquals(STAMP.receivedAt(), run.receivedAt());
        assertEquals(STAMP.startedAt(), run.startedAt());
        assertEquals(STAMP.finishedAt(), run.finishedAt());
        assertEquals(DefaultProfile.ID, run.profileId());
        assertTrue(run.verified());
        // 배송정책과 탐색 예산은 각각 별도 필드다 (Domain §11.1)
        assertEquals(problem.deliveryPolicy(), run.deliveryPolicy());
        assertEquals(STAMP.searchBudget(), run.searchBudget());

        // E24 — PD의 같은 orderId 방문 2건은 Visit.pickup으로 구분된다 (§9 Q4)
        Problem pd = VerifyFixtures.baseProblem();
        Map<VehicleId, List<NodeId>> pdRoutes = routes(VerifyFixtures.V1, VerifyFixtures.baseRoute());
        SolveResult pdResult = ResultAssembler.assemble(
                pd, PROFILE, verify(pd, pdRoutes, Set.of()), STAMP);
        List<SolveResult.Visit> r2Visits = pdResult.routes().get(0).visits().stream()
                .filter(visit -> visit.orderId().equals(VerifyFixtures.R2))
                .toList();
        assertEquals(2, r2Visits.size());
        assertTrue(r2Visits.get(0).pickup());
        assertFalse(r2Visits.get(1).pickup());
        // Request 단위로는 정확히 1회씩이다 — 방문 수로 세면 PD에서 어긋난다
        assertEquals(pd.requests().size(), pdResult.routes().get(0).visits().stream()
                .map(SolveResult.Visit::orderId).distinct().count() + pdResult.unassigned().size());

        // endDepot이 있는 경로에서는 depotReturn이 채워진다
        Problem withEnd = VerifyFixtures.endDepotProblem(List.of(VerifyFixtures.ALL_DAY));
        Map<VehicleId, List<NodeId>> endRoutes =
                routes(VerifyFixtures.V1, List.of(NodeId.delivery(VerifyFixtures.R1)));
        VerificationResult.Pass endPass = verify(withEnd, endRoutes, Set.of());
        SolveResult endResult = ResultAssembler.assemble(withEnd, PROFILE, endPass, STAMP);
        RouteFacts endFacts = endPass.routeFacts().get(VerifyFixtures.V1);
        assertEquals(
                Optional.of(withEnd.timeBase().toWallClock(endFacts.endDepotArrivalSec().orElseThrow())),
                endResult.routes().get(0).depotReturn());
    }

    // --- T10 ---

    @Test
    void derivesUnassignedReasons() {
        Problem problem = VerifyFixtures.reasonProblem();
        Set<RequestId> bank = new LinkedHashSet<>();
        problem.requests().forEach(request -> bank.add(request.id()));
        VerificationResult.Pass pass = verify(problem, Map.of(), bank);

        SolveResult result = ResultAssembler.assemble(problem, PROFILE, pass, STAMP);

        Map<String, UnassignedReason> reasons = new LinkedHashMap<>();
        result.unassigned().forEach(u -> reasons.put(u.orderId().value(), u.reason()));
        assertEquals(UnassignedReason.NO_COMPATIBLE_VEHICLE, reasons.get("U1"));
        assertEquals(UnassignedReason.CAPACITY, reasons.get("U2"));
        assertEquals(UnassignedReason.TIME_WINDOW_INFEASIBLE, reasons.get("U3"));
        assertEquals(UnassignedReason.NOT_PLACED, reasons.get("U4"));
        // E12 — 시간은 되는데 maxDriveDist만 불가해도 같은 bucket이다
        assertEquals(UnassignedReason.TIME_WINDOW_INFEASIBLE, reasons.get("U5"));
    }

    // --- T11 ---

    @Test
    void deterministicOrderingAndStatus() {
        Problem problem = VerifyFixtures.baseProblem();
        Map<VehicleId, List<NodeId>> routes = new LinkedHashMap<>();
        routes.put(VerifyFixtures.V2, List.of(NodeId.delivery(VerifyFixtures.R1)));
        routes.put(VerifyFixtures.V1, List.of(
                NodeId.pickup(VerifyFixtures.R2), NodeId.delivery(VerifyFixtures.R2)));
        // 같은 입력을 재검증부터 두 번 통과시켜 비교한다 (조립만 두 번 부르면 동어반복이다)
        SolveResult first = ResultAssembler.assemble(
                problem, PROFILE, verify(problem, routes, Set.of()), STAMP);
        SolveResult second = ResultAssembler.assemble(
                problem, PROFILE, verify(problem, routes, Set.of()), STAMP);

        assertEquals(List.of(VerifyFixtures.V1, VerifyFixtures.V2),
                first.routes().stream().map(SolveResult.RouteResult::vehicleId).toList());
        assertEquals(SolveResult.Status.DONE, first.status());
        assertEquals(first, second);

        Problem banked = VerifyFixtures.reasonProblem();
        Set<RequestId> bank = new LinkedHashSet<>();
        banked.requests().forEach(request -> bank.add(request.id()));
        SolveResult unassignedResult = ResultAssembler.assemble(
                banked, PROFILE, verify(banked, Map.of(), bank), STAMP);
        assertEquals(List.of("U1", "U2", "U3", "U4", "U5"),
                unassignedResult.unassigned().stream().map(u -> u.orderId().value()).toList());
    }

    // --- 헬퍼 ---

    private static Map<VehicleId, List<NodeId>> routes(VehicleId vehicleId, List<NodeId> visits) {
        Map<VehicleId, List<NodeId>> routes = new LinkedHashMap<>();
        routes.put(vehicleId, visits);
        return routes;
    }

    /** 결과는 재검증을 통과한 값에서만 만들어지므로 조립 테스트도 verify를 거쳐 들어간다. */
    private static VerificationResult.Pass verify(
            Problem problem, Map<VehicleId, List<NodeId>> routes, Set<RequestId> bank) {
        List<Route> routeList = new ArrayList<>();
        routes.forEach((vehicleId, visits) -> routeList.add(new Route(vehicleId, visits)));
        EvaluationResult.Feasible reported = assertInstanceOf(
                EvaluationResult.Feasible.class,
                Evaluator.evaluate(problem, PROFILE, new Solution(routeList, bank)));
        Evaluation evaluation = reported.evaluation();
        return assertInstanceOf(
                VerificationResult.Pass.class,
                SolutionVerifier.verify(problem, PROFILE, routes, bank, evaluation, reported.score()));
    }
}
