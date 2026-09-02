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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class VehicleZoneFillConstructionTest {

    /** T23 — 차량 1대·존 2개에서 적재율 규칙대로 한 존만 커밋 · what-if가 다른 존을 오염시키지 않음. */
    @Test
    void commitsOnlyBestZonePerVehicle() {
        LocationId a1 = new LocationId("A1");
        LocationId a2 = new LocationId("A2");
        LocationId b1 = new LocationId("B1");
        LocationId b2 = new LocationId("B2");
        LocationId b3 = new LocationId("B3");
        // 존 A: 2건 × 10,000 = 적재율 0.67 (전원 성공) · 존 B: 3건 × 9,000 = 0.90 (전원 성공, > 0.85) → B 커밋
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        delivery("A1", a1, 10_000L, WORK, Optional.of("A")),
                        delivery("A2", a2, 10_000L, WORK, Optional.of("A")),
                        delivery("B1", b1, 9_000L, WORK, Optional.of("B")),
                        delivery("B2", b2, 9_000L, WORK, Optional.of("B")),
                        delivery("B3", b3, 9_000L, WORK, Optional.of("B"))),
                List.of(vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.empty())),
                locations(
                        at(DEPOT, 37.0, 127.0),
                        at(a1, 37.01, 127.0), at(a2, 37.02, 127.0),
                        at(b1, 37.0, 127.01), at(b2, 37.0, 127.02), at(b3, 37.0, 127.03)),
                List.of());

        Solution solution = new VehicleZoneFillConstruction().construct(problem, new DefaultProfile());
        assertEquals(1, solution.routes().size());
        assertEquals(
                Set.of(NodeId.delivery(new RequestId("B1")), NodeId.delivery(new RequestId("B2")), NodeId.delivery(new RequestId("B3"))),
                Set.copyOf(solution.routes().getFirst().visits()));
        assertEquals(Set.of(new RequestId("A1"), new RequestId("A2")), solution.bank());   // A의 what-if는 버려졌다
    }
}
