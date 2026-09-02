package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class WeakestFitDecreasingConstructionTest {

    /** T32 — 약한 차로 충분한 요청이 강한 차를 쓰지 않아, 나중의 어려운 요청이 강한 차에 들어감. */
    @Test
    void strongVehicleReservedForHardRequest() {
        LocationId e = new LocationId("E");
        LocationId h = new LocationId("H");
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        delivery("E", e, 5_000L, new TimeWindow(36_000L, 39_600L), Optional.empty()),   // 창이 좁아 difficulty 순 첫 번째
                        delivery("H", h, 16_000L)),                                                      // 무거워 강한 차만 가능
                List.of(
                        vehicle("V1", 10_000L, Optional.of(DEPOT), Optional.empty()),                    // 약한 차
                        vehicle("V2", 20_000L, Optional.of(DEPOT), Optional.empty())),                   // 강한 차
                locations(at(DEPOT, 37.0, 127.0), at(e, 37.01, 127.0), at(h, 37.0, 127.01)),
                List.of());
        assertEquals(List.of(new VehicleId("V1"), new VehicleId("V2")),
                WeakestFitDecreasingConstruction.strengthOrder(problem).stream().map(v -> v.id()).toList());

        Solution solution = new WeakestFitDecreasingConstruction().construct(problem, new DefaultProfile());
        assertTrue(solution.bank().isEmpty());
        assertEquals(List.of(NodeId.delivery(new RequestId("E"))), InsertionSearch.visitsOf(solution, new VehicleId("V1")));
        assertEquals(List.of(NodeId.delivery(new RequestId("H"))), InsertionSearch.visitsOf(solution, new VehicleId("V2")));
    }
}
