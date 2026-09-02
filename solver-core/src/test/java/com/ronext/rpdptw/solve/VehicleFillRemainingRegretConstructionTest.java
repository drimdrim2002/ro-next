package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static com.ronext.rpdptw.solve.ConstructionFixtures.withFeatures;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class VehicleFillRemainingRegretConstructionTest {

    /** T31 — 차량 순서상 지나간 차량은 regret 계산에서 제외 · noAlt 요청이 최우선 (X19). */
    @Test
    void regretUsesOnlyRemainingVehicles() {
        LocationId p = new LocationId("P");
        LocationId q = new LocationId("Q");
        LocationId s = new LocationId("S");
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        withFeatures(delivery("S", s, 6_000L), Set.of("X", "Y")),   // 가장 무거워 V1의 seed
                        withFeatures(delivery("A", p, 4_000L), Set.of("X", "Y")),   // V2로 미룰 수 있다
                        withFeatures(delivery("B", q, 4_000L), Set.of("X"))),       // V1뿐 — noAlt
                List.of(
                        vehicle("V1", 10_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.of("X")),
                        vehicle("V2", 10_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.of("Y"))),
                locations(at(DEPOT, 37.0, 127.0), at(p, 37.01, 127.0), at(q, 37.0, 127.01), at(s, 37.01, 127.01)),
                List.of());
        List<Vehicle> order = VehicleFillRemainingRegretConstruction.vehicleOrder(problem);
        assertEquals(List.of(new VehicleId("V1"), new VehicleId("V2")), order.stream().map(Vehicle::id).toList());

        // 지나간 차량 제외: V1을 채우는 동안 남은 차량은 V2뿐 — B는 V2와 비호환이라 noAlt, A는 V2 단독 거리
        List<Vehicle> remaining = order.subList(1, 2);
        assertTrue(VehicleFillRemainingRegretConstruction.remainingStandalone(problem, remaining, new RequestId("B")).isEmpty());
        assertEquals(Optional.of(InsertionSearch.standaloneDistMeter(problem, new VehicleId("V2"), new RequestId("A"))),
                VehicleFillRemainingRegretConstruction.remainingStandalone(problem, remaining, new RequestId("A")));
        assertTrue(VehicleFillRemainingRegretConstruction.remainingStandalone(problem, List.of(), new RequestId("A")).isEmpty());

        // V1 = S(6k) + 하나(4k). noAlt인 B가 A보다 먼저 들어가고, A는 V2로 간다 — 아무것도 bank가 아니다
        Solution solution = new VehicleFillRemainingRegretConstruction().construct(problem, new DefaultProfile());
        assertTrue(solution.bank().isEmpty());
        assertTrue(InsertionSearch.visitsOf(solution, new VehicleId("V1")).contains(NodeId.delivery(new RequestId("B"))));
        assertEquals(List.of(NodeId.delivery(new RequestId("A"))), InsertionSearch.visitsOf(solution, new VehicleId("V2")));
    }
}
