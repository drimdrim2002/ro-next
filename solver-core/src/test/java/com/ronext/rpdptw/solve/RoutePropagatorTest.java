package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.problemWithEndDepot;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.problemWithEndDepotAndTravel;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.problemWithR1;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.problemWithR2;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.problemWithTravel;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.problemWithVehicle;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.singleVisitAtBundang;
import static com.ronext.rpdptw.solve.RoutePropagatorFixtures.threeDayProblem;
import static com.ronext.rpdptw.solve.SolveFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.SolveFixtures.BUNDANG;
import static com.ronext.rpdptw.solve.SolveFixtures.DEPOT;
import static com.ronext.rpdptw.solve.SolveFixtures.END_DEPOT;
import static com.ronext.rpdptw.solve.SolveFixtures.GANGNAM100;
import static com.ronext.rpdptw.solve.SolveFixtures.GANGNAM200;
import static com.ronext.rpdptw.solve.SolveFixtures.R1;
import static com.ronext.rpdptw.solve.SolveFixtures.R2;
import static com.ronext.rpdptw.solve.SolveFixtures.SAME;
import static com.ronext.rpdptw.solve.SolveFixtures.THREE_DAY_WORK;
import static com.ronext.rpdptw.solve.SolveFixtures.V1;
import static com.ronext.rpdptw.solve.SolveFixtures.WORK_08_18;
import static com.ronext.rpdptw.solve.SolveFixtures.deliveryOnly;
import static com.ronext.rpdptw.solve.SolveFixtures.feasible;
import static com.ronext.rpdptw.solve.SolveFixtures.freeze;
import static com.ronext.rpdptw.solve.SolveFixtures.infeasible;
import static com.ronext.rpdptw.solve.SolveFixtures.locations;
import static com.ronext.rpdptw.solve.SolveFixtures.pickupDelivery;
import static com.ronext.rpdptw.solve.SolveFixtures.pickupOnly;
import static com.ronext.rpdptw.solve.SolveFixtures.section72Problem;
import static com.ronext.rpdptw.solve.SolveFixtures.section72Visits;
import static com.ronext.rpdptw.solve.SolveFixtures.side;
import static com.ronext.rpdptw.solve.SolveFixtures.vehicle;
import static com.ronext.rpdptw.solve.TimeFixtures.DEFAULT_TIME_BASE;
import static com.ronext.rpdptw.solve.TimeFixtures.allDay;
import static com.ronext.rpdptw.solve.TimeFixtures.dateTime;
import static com.ronext.rpdptw.solve.TimeFixtures.hours;
import static com.ronext.rpdptw.solve.TimeFixtures.minutes;
import static com.ronext.rpdptw.solve.TimeFixtures.sec;
import static com.ronext.rpdptw.solve.TimeFixtures.window;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.LocationId;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Item;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.Problem;

class RoutePropagatorTest {

    @Test
    void reproducesDomainSection72() {
        Problem problem = section72Problem(false);
        RouteFacts facts = feasible(RoutePropagator.propagate(problem, V1, section72Visits()));

        assertEquals(sec("08:00"), facts.spanStartSec());
        assertEquals(Optional.of(sec("08:00")), facts.departureSec());
        assertEquals(10_000L, facts.initialLoadWeight());

        VisitFacts pickup = facts.visits().get(0);
        assertEquals(NodeId.pickup(R2), pickup.nodeId());
        assertEquals(sec("08:40"), pickup.arrivalSec());
        assertEquals(minutes(20), pickup.waitingSec());
        assertEquals(sec("09:00"), pickup.serviceStartSec());
        assertEquals(sec("09:05"), pickup.serviceEndSec());
        assertEquals(sec("09:05"), pickup.departureSec());
        assertEquals(20_000L, pickup.loadWeightAfter());
        assertTrue(pickup.serviceStartSec() <= sec("10:00"));

        VisitFacts delivery = facts.visits().get(1);
        assertEquals(NodeId.delivery(R1), delivery.nodeId());
        assertEquals(sec("09:55"), delivery.arrivalSec());
        assertEquals(minutes(185), delivery.waitingSec());
        assertEquals(sec("13:00"), delivery.serviceStartSec());
        assertEquals(sec("13:14"), delivery.serviceEndSec());
        assertEquals(sec("13:14"), delivery.departureSec());
        assertEquals(10_000L, delivery.loadWeightAfter());
        assertTrue(delivery.serviceStartSec() <= sec("15:00"));

        assertEquals(minutes(90), facts.driveTimeSec());
        assertEquals(minutes(205), facts.customerWaitingTimeSec());
        assertEquals(minutes(19), facts.serviceTimeSec());
        assertEquals(2, facts.stopCount());
        assertEquals(0L, facts.depotWaitingTimeSec());
        assertEquals(0L, facts.interWorkWindowRestTimeSec());
        assertEquals(18_840L, facts.routeOperationalTimeSec());
        assertEquals(18_840L, facts.routeEndSec() - facts.spanStartSec());
    }

    @Test
    void reqDateFailsOnLateServiceStart() {
        Request late = deliveryOnly(
                "R1", GANGNAM100, window("16:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(15, 0), 10_000L);
        Problem lateProblem = problemWithR1(late);
        PropagationResult.Infeasible failed =
                infeasible(RoutePropagator.propagate(lateProblem, V1, section72Visits()));
        assertEquals(Violation.REQ_DATE, failed.violation());
        assertEquals(Optional.of(NodeId.delivery(R1)), failed.at());

        Request equalReqDate = deliveryOnly(
                "R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(13, 0), 10_000L);
        RouteFacts equal = feasible(RoutePropagator.propagate(problemWithR1(equalReqDate), V1, section72Visits()));
        assertEquals(sec("13:00"), equal.visits().get(1).serviceStartSec());

        Request serviceEndAfterReqDate = deliveryOnly(
                "R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(13, 0), 10_000L);
        RouteFacts endAfter =
                feasible(RoutePropagator.propagate(problemWithR1(serviceEndAfterReqDate), V1, section72Visits()));
        assertTrue(endAfter.visits().get(1).serviceEndSec() > sec("13:00"));
        assertTrue(endAfter.visits().get(1).serviceStartSec() <= sec("13:00"));
    }

    @Test
    void detectsHardViolations() {
        Request closeOk = deliveryOnly(
                "R1", GANGNAM100, window("13:00", "13:00"), Duration.ofMinutes(14), dateTime(2, "00:00"), 10_000L);
        RouteFacts onClose = feasible(RoutePropagator.propagate(problemWithR1(closeOk), V1, section72Visits()));
        assertEquals(sec("13:00"), onClose.visits().get(1).serviceStartSec());

        Problem tooLate = problemWithTravel(List.of(
                new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                new TravelEntry(BUNDANG, GANGNAM100, 15_000, 15_000)));
        PropagationResult.Infeasible timeWindow =
                infeasible(RoutePropagator.propagate(tooLate, V1, section72Visits()));
        assertEquals(Violation.TIME_WINDOW, timeWindow.violation());

        Problem overCapacity =
                section72Problem(false, WORK_08_18, ALL_DAY, Optional.of(DEPOT), Optional.empty(), 9_000L);
        PropagationResult.Infeasible initial = infeasible(RoutePropagator.propagate(overCapacity, V1, section72Visits()));
        assertEquals(Violation.CAPACITY_WEIGHT, initial.violation());
        assertEquals(Optional.empty(), initial.at());

        Request noPickupLoad = pickupDelivery(
                "R2", BUNDANG, GANGNAM200, window("09:00", "12:00"), Duration.ofMinutes(5), LocalTime.of(10, 0), 0L);
        Problem atCap = problemWithR2(noPickupLoad, 10_000L);
        // 경계는 적재 == maxWeight일 때만 살아 있다 (30t로 흘러가면 `>`/`>=` 구분이 죽는다)
        assertEquals(10_000L, atCap.vehicle(V1).maxWeight());
        // 부피는 무게와 별개 분기다 — 출발 적재 부피만 넘긴 구성 (무게 10,000 ≤ 30,000은 통과).
        Request bulky = new Request(
                R1,
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(side(
                        NodeId.delivery(R1),
                        GANGNAM100,
                        List.of(window("13:00", "18:00")),
                        minutes(14),
                        sec("15:00"))),
                List.of(new Item("I-R1", 10_000L, 2_000L, 1, 0L)),
                10_000L,
                2_000L,
                Optional.empty(),
                Set.of());
        Problem overVolume = freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(bulky, noPickupLoad),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000L,
                        List.of(WORK_08_18),
                        Optional.of(DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.empty())),
                locations(DEPOT, BUNDANG, GANGNAM100, GANGNAM200),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000)));
        PropagationResult.Infeasible initialVolume =
                infeasible(RoutePropagator.propagate(overVolume, V1, section72Visits()));
        assertEquals(Violation.CAPACITY_VOLUME, initialVolume.violation());
        assertEquals(Optional.empty(), initialVolume.at());

        RouteFacts full = feasible(RoutePropagator.propagate(atCap, V1, section72Visits()));
        assertEquals(10_000L, full.visits().get(0).loadWeightAfter());

        Vehicle twoStops = vehicle(
                "V1",
                30_000L,
                1_000_000L,
                List.of(WORK_08_18),
                Optional.of(DEPOT),
                Optional.empty(),
                OptionalInt.of(2),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Optional.empty());
        Problem maxTwo = problemWithVehicle(twoStops);
        feasible(RoutePropagator.propagate(maxTwo, V1, section72Visits()));

        Vehicle oneStop = vehicle(
                "V1",
                30_000L,
                1_000_000L,
                List.of(WORK_08_18),
                Optional.of(DEPOT),
                Optional.empty(),
                OptionalInt.of(1),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Optional.empty());
        PropagationResult.Infeasible tooMany =
                infeasible(RoutePropagator.propagate(problemWithVehicle(oneStop), V1, section72Visits()));
        assertEquals(Violation.MAX_STOP_COUNT, tooMany.violation());

        // 운전 두 축 — 이 경로는 DEPOT→BUNDANG(2,400초·10,000m) + BUNDANG→GANGNAM100(3,000초·15,000m)
        // = 5,400초·25,000m다. 한도를 실제 합계에 맞춰야 `>`/`>=` 구분이 살아 있다.
        RouteFacts atDriveTime = feasible(RoutePropagator.propagate(
                problemWithVehicle(withDriveLimits(OptionalLong.of(5_400L), OptionalLong.empty())),
                V1,
                section72Visits()));
        assertEquals(5_400L, atDriveTime.driveTimeSec());

        PropagationResult.Infeasible overDriveTime = infeasible(RoutePropagator.propagate(
                problemWithVehicle(withDriveLimits(OptionalLong.of(5_399L), OptionalLong.empty())),
                V1,
                section72Visits()));
        assertEquals(Violation.MAX_DRIVE_TIME, overDriveTime.violation());
        assertEquals(Optional.empty(), overDriveTime.at());

        RouteFacts atDriveDist = feasible(RoutePropagator.propagate(
                problemWithVehicle(withDriveLimits(OptionalLong.empty(), OptionalLong.of(25_000L))),
                V1,
                section72Visits()));
        assertEquals(25_000L, atDriveDist.driveDistMeter());

        PropagationResult.Infeasible overDriveDist = infeasible(RoutePropagator.propagate(
                problemWithVehicle(withDriveLimits(OptionalLong.empty(), OptionalLong.of(24_999L))),
                V1,
                section72Visits()));
        assertEquals(Violation.MAX_DRIVE_DIST, overDriveDist.violation());
        assertEquals(Optional.empty(), overDriveDist.at());

        Problem longReturn = problemWithEndDepotAndTravel(
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000),
                        new TravelEntry(GANGNAM100, END_DEPOT, 1, 40_000)));
        PropagationResult.Infeasible work = infeasible(RoutePropagator.propagate(longReturn, V1, section72Visits()));
        assertEquals(Violation.WORK_WINDOW, work.violation());

        Vehicle emptyWork = vehicle(
                "V1",
                30_000L,
                1_000_000L,
                List.of(),
                Optional.of(DEPOT),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Optional.empty());
        PropagationResult.Infeasible noWork =
                infeasible(RoutePropagator.propagate(problemWithVehicle(emptyWork), V1, section72Visits()));
        assertEquals(Violation.WORK_WINDOW, noWork.violation());
        assertEquals(Optional.empty(), noWork.at());

        // 서비스 배치의 두 창 축이 같은 시각에 함께 소진되는 구성 — 도착 31,200에서 근무창은
        // 서비스가 안 들어가고(31,500 > 31,300) 방문지 창도 이미 닫혔다(31,200 > 30,000).
        // 절차 2에 먼저 적힌 방문지 축을 기록한다 (§3.3 위반 귀속).
        Problem bothAxesAtOnce = singleVisitAtBundang(
                List.of(window(LocalTime.of(0, 0), LocalTime.of(8, 20))),
                List.of(window(LocalTime.of(8, 0), LocalTime.of(8, 41, 40))));
        PropagationResult.Infeasible tie =
                infeasible(RoutePropagator.propagate(bothAxesAtOnce, V1, List.of(NodeId.delivery(R1))));
        assertEquals(Violation.TIME_WINDOW, tie.violation());
        assertEquals(Optional.of(NodeId.delivery(R1)), tie.at());

        // 방문지 창 목록이 비면 그 축이 즉시 소진된다 — 위 emptyWork(근무창 쪽 빈 목록)와 다른 축이다.
        Problem noVisitWindow = singleVisitAtBundang(List.of(), List.of(WORK_08_18));
        PropagationResult.Infeasible emptyVisitWindows =
                infeasible(RoutePropagator.propagate(noVisitWindow, V1, List.of(NodeId.delivery(R1))));
        assertEquals(Violation.TIME_WINDOW, emptyVisitWindows.violation());
        assertEquals(Optional.of(NodeId.delivery(R1)), emptyVisitWindows.at());
    }

    @Test
    void waitInDepotRelocatesFirstWait() {
        Problem n = section72Problem(false);
        Problem y = section72Problem(true);
        RouteFacts factsN = feasible(RoutePropagator.propagate(n, V1, section72Visits()));
        RouteFacts factsY = feasible(RoutePropagator.propagate(y, V1, section72Visits()));

        assertEquals(factsN.visits().get(0).serviceStartSec(), factsY.visits().get(0).serviceStartSec());
        assertEquals(factsN.routeOperationalTimeSec(), factsY.routeOperationalTimeSec());
        assertEquals(minutes(20), factsN.visits().get(0).waitingSec());
        assertEquals(0L, factsY.visits().get(0).waitingSec());
        assertEquals(0L, factsN.depotWaitingTimeSec());
        assertEquals(minutes(20), factsY.depotWaitingTimeSec());
        assertEquals(Optional.of(sec("08:20")), factsY.departureSec());

        Request earlyClose = deliveryOnly(
                "R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(15, 0), 10_000L);
        Request earlyPickup = pickupDelivery(
                "R2", BUNDANG, GANGNAM200, window("08:00", "08:20"), Duration.ofMinutes(5), dateTime(2, "00:00"), 10_000L);
        Problem tooEarlyWindow = freeze(
                true,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(earlyClose, earlyPickup),
                List.of(vehicle(
                    "V1",
                    30_000L,
                    1_000_000L,
                    List.of(WORK_08_18),
                    Optional.of(DEPOT),
                    Optional.empty(),
                    OptionalInt.empty(),
                    OptionalLong.empty(),
                    OptionalLong.empty(),
                    Optional.empty())),
                locations(DEPOT, BUNDANG, GANGNAM100, GANGNAM200),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000)));
        PropagationResult.Infeasible lateEvenForY =
                infeasible(RoutePropagator.propagate(tooEarlyWindow, V1, section72Visits()));
        assertEquals(Violation.TIME_WINDOW, lateEvenForY.violation());

        TimeWindow workFromSix = window("06:00", "18:00");
        TimeWindow depotFromEight = window("08:00", "18:00");
        Problem lateDepot =
                section72Problem(false, workFromSix, depotFromEight, Optional.of(DEPOT), Optional.empty(), 30_000L);
        RouteFacts delayed = feasible(RoutePropagator.propagate(lateDepot, V1, section72Visits()));
        assertEquals(Optional.of(sec("08:00")), delayed.departureSec());
        assertEquals(hours(2), delayed.depotWaitingTimeSec());
    }

    @Test
    void stopCountAndDriveAggregation() {
        Request a = deliveryOnly("R1", SAME, allDay(), Duration.ZERO, dateTime(2, "00:00"), 0L);
        Request b = deliveryOnly("R2", SAME, allDay(), Duration.ZERO, dateTime(2, "00:00"), 0L);
        Problem samePlace = freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(a, b),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        List.of(WORK_08_18),
                        Optional.of(DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.empty())),
                locations(DEPOT, SAME),
                List.of(new TravelEntry(DEPOT, SAME, 500, 60)));
        RouteFacts consecutive = feasible(RoutePropagator.propagate(
                samePlace, V1, List.of(NodeId.delivery(R1), NodeId.delivery(R2))));
        assertEquals(1, consecutive.stopCount());
        assertEquals(60L, consecutive.driveTimeSec());
        assertEquals(500L, consecutive.driveDistMeter());
        assertEquals(0L, consecutive.visits().get(1).arrivalSec() - consecutive.visits().get(0).departureSec());

        Request atDepot = deliveryOnly("R1", DEPOT, allDay(), Duration.ZERO, dateTime(2, "00:00"), 0L);
        Problem firstAtDepot = freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(atDepot),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        List.of(WORK_08_18),
                        Optional.of(DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.empty())),
                locations(DEPOT),
                List.of());
        RouteFacts selfFirst = feasible(RoutePropagator.propagate(firstAtDepot, V1, List.of(NodeId.delivery(R1))));
        assertEquals(1, selfFirst.stopCount());
        assertEquals(0L, selfFirst.driveDistMeter());
        assertEquals(0L, selfFirst.driveTimeSec());
        assertEquals(selfFirst.departureSec().orElseThrow(), selfFirst.visits().get(0).arrivalSec());

        Problem withEnd =
                section72Problem(false, WORK_08_18, ALL_DAY, Optional.of(DEPOT), Optional.of(END_DEPOT), 30_000L);
        RouteFacts returning = feasible(RoutePropagator.propagate(withEnd, V1, section72Visits()));
        assertEquals(5_400L + 1_200L, returning.driveTimeSec());
        assertEquals(10_000L + 15_000L + 8_000L, returning.driveDistMeter());
        assertEquals(Optional.of(47_640L + 1_200L), returning.endDepotArrivalSec());
    }

    @Test
    void defersToNextWorkWindow() {
        long day2Leave = sec(2, "16:30");
        long day2WorkEnd = sec(2, "17:00");
        long travelSec = minutes(90);
        long day3WorkStart = sec(3, "08:00");
        long horizonSec = 300_000L;
        LocalDateTime horizon = DEFAULT_TIME_BASE.toWallClock(horizonSec);

        assertTrue(day2Leave + travelSec > day2WorkEnd);
        assertEquals(Optional.of(day3WorkStart), optional(RoutePropagator.fitArc(THREE_DAY_WORK, day2Leave, travelSec)));

        Request first = deliveryOnly(
                "R1",
                BUNDANG,
                window(day2Leave, day2WorkEnd),
                Duration.ZERO,
                horizon,
                0L);
        Request second = deliveryOnly("R2", GANGNAM100, window(0L, horizonSec), Duration.ZERO, horizon, 0L);
        Problem multi = threeDayProblem(first, second, (int) travelSec, false);
        RouteFacts deferred = feasible(RoutePropagator.propagate(
                multi, V1, List.of(NodeId.delivery(R1), NodeId.delivery(R2))));
        assertEquals(day3WorkStart, deferred.visits().get(0).departureSec());
        assertEquals(sec(3, "09:30"), deferred.visits().get(1).arrivalSec());
        assertEquals(minutes(30), RoutePropagator.overlapWithWindows(day2Leave, day3WorkStart, THREE_DAY_WORK));
        assertEquals(hours(15), day3WorkStart - day2WorkEnd);
        assertEquals(
                RoutePropagator.overlapWithWindows(deferred.visits().get(0).arrivalSec(), day2Leave, THREE_DAY_WORK) + minutes(30),
                deferred.customerWaitingTimeSec());
        assertTrue(deferred.interWorkWindowRestTimeSec() >= hours(15));
        assertEquals(0L, deferred.interWorkWindowRestTimeSec() % hours(15));

        Request endsAtFour = deliveryOnly(
                "R1", BUNDANG, window(2, "16:00", "17:00"), Duration.ZERO, horizon, 0L);
        RouteFacts fromFour = feasible(RoutePropagator.propagate(
                threeDayProblem(endsAtFour, second, (int) travelSec, false),
                V1,
                List.of(NodeId.delivery(R1), NodeId.delivery(R2))));
        assertEquals(sec(2, "16:00"), fromFour.visits().get(0).serviceEndSec());
        assertEquals(day3WorkStart, fromFour.visits().get(0).departureSec());
        assertEquals(hours(1), RoutePropagator.overlapWithWindows(sec(2, "16:00"), day3WorkStart, THREE_DAY_WORK));
        assertTrue(fromFour.customerWaitingTimeSec() >= hours(1));
        assertTrue(fromFour.interWorkWindowRestTimeSec() >= hours(15));

        Request longService = deliveryOnly(
                "R1", BUNDANG, window(dateTime(2, "16:30"), dateTime(3, "17:00")), Duration.ofHours(1), horizon, 0L);
        RouteFacts serviceDeferred = feasible(RoutePropagator.propagate(
                threeDayProblem(longService, second, (int) travelSec, false),
                V1,
                List.of(NodeId.delivery(R1), NodeId.delivery(R2))));
        assertEquals(day3WorkStart, serviceDeferred.visits().get(0).serviceStartSec());

        PropagationResult.Infeasible tooLong = infeasible(RoutePropagator.propagate(
                threeDayProblem(first, second, (int) hours(12), false),
                V1,
                List.of(NodeId.delivery(R1), NodeId.delivery(R2))));
        assertEquals(Violation.WORK_WINDOW, tooLong.violation());
        assertEquals(Optional.of(NodeId.delivery(R1)), tooLong.at());
    }

    @Test
    void depotWindowAppliesToDepartureAndReturn() {
        TimeWindow work = WORK_08_18;
        TimeWindow depotEndsFirst = window(0L, 20_000L);
        Problem dsFirst = section72Problem(false, work, depotEndsFirst, Optional.of(DEPOT), Optional.empty(), 30_000L);
        PropagationResult.Infeasible depart =
                infeasible(RoutePropagator.propagate(dsFirst, V1, section72Visits()));
        assertEquals(Violation.DEPOT_WINDOW, depart.violation());
        assertEquals(Optional.empty(), depart.at());

        Depot gapped = new Depot(
                END_DEPOT,
                List.of(window(0L, 10_000L), window(80_000L, 86_399L)),
                Optional.empty());
        Problem gapReturn = problemWithEndDepot(gapped);
        PropagationResult.Infeasible ret =
                infeasible(RoutePropagator.propagate(gapReturn, V1, section72Visits()));
        assertEquals(Violation.DEPOT_WINDOW, ret.violation());

        // 두 축이 같은 시각에 함께 소진되는 구성 — 후보 28,800에서 근무창은 첫 이동(2,400)이
        // 안 들어가고(31,200 > 30,000) 차고 창도 이미 닫혔다(28,800 > 20,000).
        // Ds가 먼저 소진되는 위 depart와 달리, 절차 1에 먼저 적힌 근무 축을 기록한다.
        Problem bothAxesAtOnce = section72Problem(
                false, window(LocalTime.of(8, 0), LocalTime.of(8, 20)), depotEndsFirst, Optional.of(DEPOT), Optional.empty(), 30_000L);
        PropagationResult.Infeasible tie =
                infeasible(RoutePropagator.propagate(bothAxesAtOnce, V1, section72Visits()));
        assertEquals(Violation.WORK_WINDOW, tie.violation());
        assertEquals(Optional.empty(), tie.at());

        RouteFacts allDay = feasible(RoutePropagator.propagate(section72Problem(false), V1, section72Visits()));
        assertEquals(Optional.of(sec("08:00")), allDay.departureSec());
        assertEquals(0L, allDay.depotWaitingTimeSec());
    }

    @Test
    void routeOperationalTimeIdentityHolds() {
        assertIdentity(section72Problem(false), section72Visits());
        assertIdentity(section72Problem(true), section72Visits());
        assertIdentity(
                section72Problem(false, WORK_08_18, ALL_DAY, Optional.of(DEPOT), Optional.of(END_DEPOT), 30_000L),
                section72Visits());

        LocalDateTime horizon = DEFAULT_TIME_BASE.toWallClock(300_000L);
        Request day2 = deliveryOnly("R1", BUNDANG, window(2, "16:30", "17:00"), Duration.ZERO, horizon, 0L);
        Request day3 = deliveryOnly("R2", GANGNAM100, window(0L, 300_000L), Duration.ZERO, horizon, 0L);
        assertIdentity(threeDayProblem(day2, day3, 5_400, false), List.of(NodeId.delivery(R1), NodeId.delivery(R2)));
        assertIdentity(threeDayProblem(day2, day3, 5_400, true), List.of(NodeId.delivery(R1), NodeId.delivery(R2)));

        Request pickup = pickupOnly("R1", BUNDANG, allDay(), Duration.ZERO, dateTime(2, "00:00"), 4_000L);
        Problem pickupOnly = freeze(
                false,
                List.of(
                        new Depot(DEPOT, List.of(ALL_DAY), Optional.empty()),
                        new Depot(END_DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(pickup),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        List.of(WORK_08_18),
                        Optional.of(DEPOT),
                        Optional.of(END_DEPOT),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.empty())),
                locations(DEPOT, END_DEPOT, BUNDANG),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 1_000, 100),
                        new TravelEntry(BUNDANG, END_DEPOT, 1_000, 100)));
        RouteFacts po = feasible(RoutePropagator.propagate(pickupOnly, V1, List.of(NodeId.pickup(R1))));
        assertEquals(4_000L, po.visits().get(0).loadWeightAfter());
        assertEquals(po.routeOperationalTimeSec(), po.routeEndSec() - po.spanStartSec());

        Problem noStart = section72Problem(false, WORK_08_18, ALL_DAY, Optional.empty(), Optional.empty(), 30_000L);
        RouteFacts fromFirst = feasible(RoutePropagator.propagate(noStart, V1, section72Visits()));
        assertEquals(Optional.empty(), fromFirst.departureSec());
        assertEquals(sec("08:00"), fromFirst.visits().get(0).arrivalSec());
        assertEquals(0L, fromFirst.depotWaitingTimeSec());
        assertEquals(fromFirst.routeOperationalTimeSec(), fromFirst.routeEndSec() - fromFirst.spanStartSec());

        Problem noStartY = section72Problem(true, WORK_08_18, ALL_DAY, Optional.empty(), Optional.empty(), 30_000L);
        RouteFacts ignoredY = feasible(RoutePropagator.propagate(noStartY, V1, section72Visits()));
        assertEquals(fromFirst.visits().get(0).serviceStartSec(), ignoredY.visits().get(0).serviceStartSec());
        assertEquals(fromFirst.routeOperationalTimeSec(), ignoredY.routeOperationalTimeSec());
        assertEquals(ignoredY.routeOperationalTimeSec(), ignoredY.routeEndSec() - ignoredY.spanStartSec());
    }

    @Test
    void singleWindowMatchesPreD4Values() {
        RouteFacts facts = feasible(RoutePropagator.propagate(section72Problem(false), V1, section72Visits()));
        for (VisitFacts visit : facts.visits()) {
            assertEquals(visit.serviceEndSec(), visit.departureSec());
        }
        assertEquals(0L, facts.interWorkWindowRestTimeSec());
        assertEquals(0L, facts.depotWaitingTimeSec());
    }

    /** 운전 축 한도만 갈아 끼운 V1 — 나머지 필드는 problemWithVehicle의 기본 구성과 같다. */
    private static Vehicle withDriveLimits(OptionalLong maxDriveTime, OptionalLong maxDriveDist) {
        return vehicle(
                "V1",
                30_000L,
                1_000_000L,
                List.of(WORK_08_18),
                Optional.of(DEPOT),
                Optional.empty(),
                OptionalInt.empty(),
                maxDriveTime,
                maxDriveDist,
                Optional.empty());
    }

    private static void assertIdentity(Problem problem, List<NodeId> visits) {
        RouteFacts facts = feasible(RoutePropagator.propagate(problem, V1, visits));
        assertEquals(facts.routeEndSec() - facts.spanStartSec(), facts.routeOperationalTimeSec());
    }

    /** T17 — E44 경로 구역 단일성: A→ALL→A→A→ALL 통과 · A→ALL→B → ZONE_MIX(at = B 방문) · PD 양단 상이 → ZONE_MIX · ALL→ALL 통과. */
    @Test
    void zoneMixIsHard() {
        LocationId a1 = new LocationId("A1");
        LocationId a2 = new LocationId("A2");
        LocationId a3 = new LocationId("A3");
        LocationId n1 = new LocationId("N1");
        LocationId n2 = new LocationId("N2");
        LocationId b1 = new LocationId("B1");
        TimeWindow work = ConstructionFixtures.WORK;
        Problem problem = ConstructionFixtures.freeze(
                List.of(new Depot(ConstructionFixtures.DEPOT, List.of(ConstructionFixtures.ALL_DAY), Optional.of("ALL"))),
                List.of(
                        ConstructionFixtures.delivery("A1", a1, 100L, work, Optional.of("A")),
                        ConstructionFixtures.delivery("A2", a2, 100L, work, Optional.of("A")),
                        ConstructionFixtures.delivery("A3", a3, 100L, work, Optional.of("A")),
                        ConstructionFixtures.delivery("N1", n1, 100L, work, Optional.empty()),    // ALL·부재 = 중립
                        ConstructionFixtures.delivery("N2", n2, 100L, work, Optional.empty()),
                        ConstructionFixtures.delivery("B1", b1, 100L, work, Optional.of("B")),
                        pdAcrossZones("PD", a1, b1)),
                List.of(ConstructionFixtures.vehicle("V1", 30_000L, Optional.of(ConstructionFixtures.DEPOT), Optional.empty())),
                ConstructionFixtures.locations(
                        ConstructionFixtures.at(ConstructionFixtures.DEPOT, 37.0, 127.0),
                        ConstructionFixtures.at(a1, 37.01, 127.0), ConstructionFixtures.at(a2, 37.02, 127.0),
                        ConstructionFixtures.at(a3, 37.03, 127.0), ConstructionFixtures.at(n1, 37.0, 127.01),
                        ConstructionFixtures.at(n2, 37.0, 127.02), ConstructionFixtures.at(b1, 37.0, 127.03)),
                List.of());
        VehicleId v1 = new VehicleId("V1");

        // A → ALL → A → A → ALL : 구체 구역 {A} 하나 → 통과
        feasible(RoutePropagator.propagate(problem, v1, List.of(d("A1"), d("N1"), d("A2"), d("A3"), d("N2"))));
        // ALL → ALL : 구체 구역 ∅ → 통과
        feasible(RoutePropagator.propagate(problem, v1, List.of(d("N1"), d("N2"))));
        // A → ALL → B : {A, B} → ZONE_MIX, at = B 방문
        PropagationResult.Infeasible mixed = infeasible(RoutePropagator.propagate(problem, v1, List.of(d("A1"), d("N1"), d("B1"))));
        assertEquals(Violation.ZONE_MIX, mixed.violation());
        assertEquals(Optional.of(d("B1")), mixed.at());
        // B → A 순서를 바꿔도 두 번째 구체 구역의 첫 방문에서 걸린다
        assertEquals(Optional.of(d("A1")), infeasible(RoutePropagator.propagate(problem, v1, List.of(d("B1"), d("A1")))).at());
        // PD 양단이 다른 구체 구역 → 단독 경로도 ZONE_MIX
        RequestId pd = new RequestId("PD");
        PropagationResult.Infeasible pair = infeasible(RoutePropagator.propagate(problem, v1, List.of(NodeId.pickup(pd), NodeId.delivery(pd))));
        assertEquals(Violation.ZONE_MIX, pair.violation());
        assertEquals(Optional.of(NodeId.delivery(pd)), pair.at());
        // 차고의 zoneId("ALL")는 방문이 아니라 세지 않는다 — 위 통과 케이스가 그 증거다
    }

    private static NodeId d(String id) {
        return NodeId.delivery(new RequestId(id));
    }

    /** pickup은 구역 A, delivery는 구역 B인 PD. */
    private static Request pdAcrossZones(String id, LocationId from, LocationId to) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.PICKUP_DELIVERY,
                Optional.of(ConstructionFixtures.side(NodeId.pickup(requestId), from, ConstructionFixtures.WORK, 60L, Optional.of("A"))),
                Optional.of(ConstructionFixtures.side(NodeId.delivery(requestId), to, ConstructionFixtures.WORK, 60L, Optional.of("B"))),
                List.of(new Item("I-" + id, 100L, 100L, 1, 0L)),
                100L,
                100L,
                Optional.empty(),
                Set.of());
    }

    private static Optional<Long> optional(OptionalLong value) {
        return value.isPresent() ? Optional.of(value.getAsLong()) : Optional.empty();
    }
}
