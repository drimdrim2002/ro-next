package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.SolveFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.SolveFixtures.BUNDANG;
import static com.ronext.rpdptw.solve.SolveFixtures.DEPOT;
import static com.ronext.rpdptw.solve.SolveFixtures.END_DEPOT;
import static com.ronext.rpdptw.solve.SolveFixtures.GANGNAM100;
import static com.ronext.rpdptw.solve.SolveFixtures.GANGNAM200;
import static com.ronext.rpdptw.solve.SolveFixtures.THREE_DAY_WORK;
import static com.ronext.rpdptw.solve.SolveFixtures.WORK_08_18;
import static com.ronext.rpdptw.solve.SolveFixtures.deliveryOnly;
import static com.ronext.rpdptw.solve.SolveFixtures.freeze;
import static com.ronext.rpdptw.solve.SolveFixtures.locations;
import static com.ronext.rpdptw.solve.SolveFixtures.vehicle;
import static com.ronext.rpdptw.solve.TimeFixtures.sec;
import static com.ronext.rpdptw.solve.TimeFixtures.window;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.problem.Problem;

/**
 * {@link RoutePropagatorTest} 전용 테스트 픽스처 및 문제 설정(setup) 헬퍼.
 */
final class RoutePropagatorFixtures {

    private RoutePropagatorFixtures() {}

    static Problem threeDayProblem(Request first, Request second, int travelSec, boolean waitInDepot) {
        return freeze(
                waitInDepot,
                List.of(new Depot(DEPOT, List.of(new TimeWindow(0, 300_000)), Optional.empty())),
                List.of(first, second),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        THREE_DAY_WORK,
                        Optional.of(DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.empty())),
                locations(DEPOT, BUNDANG, GANGNAM100),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 1_000, 100),
                        new TravelEntry(BUNDANG, GANGNAM100, 9_000, travelSec)));
    }

    static Problem threeDayProblem(Request first, Request second, Duration travelDuration, boolean waitInDepot) {
        return threeDayProblem(first, second, (int) travelDuration.toSeconds(), waitInDepot);
    }

    static Problem problemWithR1(Request r1) {
        return problemWithR1(r1, List.of(new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400)), 3_000);
    }

    static Problem problemWithR1(Request r1, List<TravelEntry> extra, Duration bundangToGangnam) {
        return problemWithR1(r1, extra, (int) bundangToGangnam.toSeconds());
    }

    static Problem problemWithR1(Request r1, List<TravelEntry> extra, int bundangToGangnam) {
        List<TravelEntry> travel = new ArrayList<>(extra);
        travel.add(new TravelEntry(BUNDANG, GANGNAM100, 15_000, bundangToGangnam));
        return freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        r1,
                        SolveFixtures.pickupDelivery(
                                "R2",
                                BUNDANG,
                                GANGNAM200,
                                List.of(window("09:00", "12:00")),
                                Duration.ofMinutes(5),
                                LocalTime.of(10, 0),
                                10_000L)),
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
                travel);
    }

    static Problem problemWithTravel(List<TravelEntry> travel) {
        return freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        deliveryOnly("R1", GANGNAM100, window("13:00", "13:00"), Duration.ofMinutes(14), TimeFixtures.dateTime(2, "00:00"), 10_000L),
                        SolveFixtures.pickupDelivery(
                                "R2",
                                BUNDANG,
                                GANGNAM200,
                                List.of(window("09:00", "12:00")),
                                Duration.ofMinutes(5),
                                LocalTime.of(10, 0),
                                10_000L)),
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
                travel);
    }

    static Problem problemWithR2(Request r2, long maxWeight) {
        return freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        deliveryOnly("R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(15, 0), 10_000L),
                        r2),
                List.of(vehicle(
                        "V1",
                        maxWeight,
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
    }

    static Problem problemWithVehicle(Vehicle vehicle) {
        return freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        deliveryOnly("R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(15, 0), 10_000L),
                        SolveFixtures.pickupDelivery(
                                "R2",
                                BUNDANG,
                                GANGNAM200,
                                List.of(window("09:00", "12:00")),
                                Duration.ofMinutes(5),
                                LocalTime.of(10, 0),
                                10_000L)),
                List.of(vehicle),
                locations(DEPOT, BUNDANG, GANGNAM100, GANGNAM200),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000)));
    }

    /** BUNDANG 단독 방문 — 서비스 배치의 두 창 축만 남긴 최소 구성 (수요 0·reqDate 종일). */
    static Problem singleVisitAtBundang(List<TimeWindow> visitWindows, List<TimeWindow> workWindows) {
        return freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(deliveryOnly("R1", BUNDANG, visitWindows, Duration.ofSeconds(300), TimeFixtures.dateTime(2, "00:00"), 0L)),
                List.of(vehicle(
                        "V1",
                        30_000L,
                        1_000_000L,
                        workWindows,
                        Optional.of(DEPOT),
                        Optional.empty(),
                        OptionalInt.empty(),
                        OptionalLong.empty(),
                        OptionalLong.empty(),
                        Optional.empty())),
                locations(DEPOT, BUNDANG),
                List.of(new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400)));
    }

    static Problem problemWithEndDepotAndTravel(List<TravelEntry> travel) {
        Request r1 = deliveryOnly("R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(15, 0), 10_000L);
        Request r2 = SolveFixtures.pickupDelivery(
                "R2", BUNDANG, GANGNAM200, window("09:00", "12:00"), Duration.ofMinutes(5), LocalTime.of(10, 0), 10_000L);
        Vehicle v = vehicle(
                "V1",
                30_000L,
                1_000_000L,
                List.of(WORK_08_18),
                Optional.of(DEPOT),
                Optional.of(END_DEPOT),
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Optional.empty());
        return freeze(
                false,
                List.of(
                        new Depot(DEPOT, List.of(ALL_DAY), Optional.empty()),
                        new Depot(END_DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(r1, r2),
                List.of(v),
                locations(DEPOT, END_DEPOT, BUNDANG, GANGNAM100, GANGNAM200),
                travel);
    }

    static Problem problemWithEndDepot(Depot end) {
        return freeze(
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty()), end),
                List.of(
                        deliveryOnly("R1", GANGNAM100, window("13:00", "18:00"), Duration.ofMinutes(14), LocalTime.of(15, 0), 10_000L),
                        SolveFixtures.pickupDelivery(
                                "R2", BUNDANG, GANGNAM200, window("09:00", "12:00"), Duration.ofMinutes(5), LocalTime.of(10, 0), 10_000L)),
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
                locations(DEPOT, END_DEPOT, BUNDANG, GANGNAM100, GANGNAM200),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000),
                        new TravelEntry(GANGNAM100, END_DEPOT, 8_000, 1_200)));
    }
}
