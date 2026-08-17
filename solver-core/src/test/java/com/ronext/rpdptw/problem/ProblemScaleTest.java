package com.ronext.rpdptw.problem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Item;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Plan;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.TravelMatrix;
import com.ronext.rpdptw.domain.Trips;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;

class ProblemScaleTest {

    private static final int LOCATION_COUNT = 453;
    private static final int REQUEST_COUNT = 452;
    private static final int VEHICLE_COUNT = 31;
    /** long 곱 — LOCATION_COUNT를 46,341 이상으로 올려도 조용히 넘치지 않게. */
    private static final long PAIR_COUNT = (long) LOCATION_COUNT * LOCATION_COUNT;

    @Test
    void freezesFullScaleSyntheticProblem() {
        LocationId[] ids = new LocationId[LOCATION_COUNT];
        Map<LocationId, Location> locations = new LinkedHashMap<>();
        for (int i = 0; i < LOCATION_COUNT; i++) {
            LocationId id = new LocationId("WIN_" + i);
            ids[i] = id;
            locations.put(id, new Location(id, 37.0 + (i * 0.001), 127.0));
        }

        List<Depot> depots = List.of(new Depot(ids[0], List.of(new TimeWindow(0, 86_399)), Optional.empty()));
        List<Request> requests = new ArrayList<>(REQUEST_COUNT);
        for (int i = 1; i <= REQUEST_COUNT; i++) {
            RequestId requestId = new RequestId("O" + i);
            requests.add(new Request(
                    requestId,
                    ServicePattern.DELIVERY_ONLY,
                    Optional.empty(),
                    Optional.of(new RequestSide(
                            NodeId.delivery(requestId),
                            ids[i],
                            List.of(new TimeWindow(0, 86_399)),
                            0L,
                            0L,
                            86_400L,
                            Optional.empty())),
                    List.of(new Item("I" + i, 1_000L, 1_000L, 1, 0L)),
                    1_000L,
                    1_000L,
                    Optional.empty(),
                    Set.of()));
        }
        List<Vehicle> vehicles = new ArrayList<>(VEHICLE_COUNT);
        for (int i = 1; i <= VEHICLE_COUNT; i++) {
            vehicles.add(new Vehicle(
                    new VehicleId("V" + i),
                    Optional.empty(),
                    1_000_000L,
                    1_000_000L,
                    List.of(new TimeWindow(0, 86_399)),
                    OptionalInt.of(45),
                    OptionalInt.empty(),
                    OptionalLong.empty(),
                    OptionalLong.empty(),
                    Set.of(),
                    Optional.empty(),
                    Optional.of(ids[0]),
                    Optional.empty()));
        }
        List<TravelEntry> entries = new ArrayList<>((int) PAIR_COUNT);
        for (int i = 0; i < LOCATION_COUNT; i++) {
            for (int j = 0; j < LOCATION_COUNT; j++) {
                entries.add(new TravelEntry(ids[i], ids[j], i == j ? 9999 : 1_000, i == j ? 0 : 80));
            }
        }

        Plan plan = new Plan(
                "SCALE",
                Optional.empty(),
                new TimeBase(LocalDateTime.of(2023, 9, 13, 0, 0)),
                86_400L,
                depots,
                requests,
                vehicles,
                locations,
                entries,
                new DeliveryPolicy(Trips.ONEWAY, false, OptionalInt.of(45)));

        // 계측은 freeze 하나만 감싼다 — TravelMatrix.prepare는 freeze 안에서 정확히 한 번
        // 돌기 때문이다. 계측용으로 따로 또 부르면 시간이 이중 계상되고 이동표 두 벌이
        // 힙에 남아 숫자가 실물의 2배가 된다 (§7 T12, 2026-08-17).
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long heapBefore = runtime.totalMemory() - runtime.freeMemory();

        long freezeStarted = System.nanoTime();
        Problem problem = Problem.freeze(plan);
        long freezeMs = (System.nanoTime() - freezeStarted) / 1_000_000L;

        System.gc();
        long heapAfter = runtime.totalMemory() - runtime.freeMemory();

        // 실물 규모의 완전성 — 전 쌍 D·U가 예외 없이 조회되고, 합계로 값까지 맞는지 본다.
        // 비대각 204,756쌍은 입력값(D=1,000·U=80), 대각 453쌍은 self arc라 0·0 (Domain §4).
        long scanStarted = System.nanoTime();
        long distanceSum = 0L;
        long timeSum = 0L;
        TravelMatrix travel = problem.travel();
        for (LocationId from : ids) {
            for (LocationId to : ids) {
                distanceSum += travel.distanceMeter(from, to);
                timeSum += travel.timeSec(from, to, 45);
            }
        }
        long scanMs = (System.nanoTime() - scanStarted) / 1_000_000L;

        System.out.printf(
                "[T12] freeze(prepare 포함)=%d ms, 전 쌍 조회(%d회)=%d ms, "
                        + "heapBefore=%d MB, heapAfter=%d MB, delta=%d MB, locations=%d, entries=%d%n",
                freezeMs,
                PAIR_COUNT,
                scanMs,
                heapBefore / (1024 * 1024),
                heapAfter / (1024 * 1024),
                (heapAfter - heapBefore) / (1024 * 1024),
                problem.locations().size(),
                PAIR_COUNT);

        assertEquals(LOCATION_COUNT, problem.locations().size());
        assertEquals(REQUEST_COUNT, problem.requests().size());
        assertEquals(VEHICLE_COUNT, problem.vehicles().size());
        assertEquals(LOCATION_COUNT, travel.locationIds().size());
        assertEquals(problem.locations().keySet(), travel.locationIds());

        // 대각 453쌍은 0이라 합계에 기여하지 않는다. D·U가 음수일 수 없으므로 합계가 정확히
        // 맞으면 대각이 전부 0이고 입력 9999가 한 건도 새지 않았다는 뜻이다.
        long offDiagonal = PAIR_COUNT - LOCATION_COUNT;
        assertEquals(offDiagonal * 1_000L, distanceSum);
        assertEquals(offDiagonal * 80L, timeSum);
        assertEquals(0, travel.distanceMeter(ids[0], ids[0]));
        assertEquals(0, travel.timeSec(ids[0], ids[0], 45));
        assertEquals(1_000, travel.distanceMeter(ids[0], ids[1]));

        // 차량 31대 전부 speed 45로 확정되고, 주문 452건 전부가 그 31대와 호환이어야 한다.
        for (Vehicle vehicle : problem.vehicles()) {
            assertEquals(45, problem.resolvedSpeedKmH(vehicle.id()));
        }
        for (Request request : problem.requests()) {
            assertEquals(VEHICLE_COUNT, problem.compatibleVehicles(request.id()).size());
        }
        assertFalse(problem.compatibleVehicles(new RequestId("O1")).isEmpty());
    }
}
