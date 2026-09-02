package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.WORK;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class FeasibleRoutesRegretMConstructionTest {

    /** T28 — 정적 호환 수는 같지만 경로가 차서 동적 가능 경로 수가 다른 두 요청에서 적은 쪽이 먼저 삽입됨. */
    @Test
    void dynamicFeasibleRouteCountLeads() {
        LocationId north = new LocationId("N");
        LocationId south = new LocationId("S");
        LocationId southEast = new LocationId("SE");
        LocationId southNear = new LocationId("SN");
        TimeWindow tight = new TimeWindow(32_400L, 32_700L);                     // 09:00–09:05
        RequestId r1 = new RequestId("R1");
        RequestId r2 = new RequestId("R2");
        RequestId a = new RequestId("A");
        RequestId b = new RequestId("B");
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        delivery("R1", north, 1_000L, tight, Optional.empty()),
                        delivery("R2", south, 1_000L),
                        delivery("A", southNear, 1_000L),                        // V1·V2 어디든 들어간다 (동적 2)
                        delivery("B", southEast, 1_000L, tight, Optional.empty())), // R1과 창이 겹쳐 V1엔 못 든다 (동적 1)
                List.of(
                        vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.empty()),
                        vehicle("V2", 30_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.of(2), Optional.empty())),
                locations(
                        at(DEPOT, 37.0, 127.0), at(north, 37.05, 127.0), at(south, 36.95, 127.0),
                        at(southEast, 36.95, 127.05), at(southNear, 36.951, 127.0)),
                List.of());
        assertEquals(problem.compatibleVehicles(a), problem.compatibleVehicles(b));     // 정적 호환 수는 같다

        Solution current = new Solution(
                List.of(new Route(new VehicleId("V1"), List.of(NodeId.delivery(r1))),
                        new Route(new VehicleId("V2"), List.of(NodeId.delivery(r2)))),
                Set.of(a, b));
        assertEquals(2, InsertionSearch.candidates(problem, new DefaultProfile(), current, a).stream().map(c -> c.vehicleId()).distinct().count());
        assertEquals(1, InsertionSearch.candidates(problem, new DefaultProfile(), current, b).stream().map(c -> c.vehicleId()).distinct().count());

        // A가 먼저 가면 V2(더 싼 쪽)를 채워(maxStop 2) B가 bank가 된다. 동적 가능 경로 수가 적은 B가 먼저다.
        Solution result = FeasibleRoutesRegretMConstruction.regretMLoop(
                problem, new DefaultProfile(), current, new LinkedHashSet<>(List.of(a, b)), false, "test", 0, 2);
        assertTrue(result.bank().isEmpty());
        assertTrue(InsertionSearch.visitsOf(result, new VehicleId("V2")).contains(NodeId.delivery(b)));
        assertTrue(InsertionSearch.visitsOf(result, new VehicleId("V1")).contains(NodeId.delivery(a)));
    }
}
