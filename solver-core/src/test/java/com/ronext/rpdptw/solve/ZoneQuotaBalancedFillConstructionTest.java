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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class ZoneQuotaBalancedFillConstructionTest {

    /**
     * T40 — 차량 (부피 20,000·정차 4)·(4,000·정차 4), 요청 4,000 × 4건 + 1,000 × 4건.
     * 부피 best-fit은 첫 4,000을 작은 차에 넣어 큰 차의 정차를 다 쓰고 3건을 놓치지만,
     * 부호 예산 규칙(정차가 먼저 바닥나는 bin은 뒤로)은 전량 배정한다.
     */
    @Test
    void signedBudgetAvoidsWastingStops() {
        List<Location> locations = new ArrayList<>();
        locations.add(at(DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            LocationId id = new LocationId("L" + i);
            locations.add(at(id, 37.0 + 0.001 * i, 127.0));
            requests.add(delivery("R" + i, id, i <= 4 ? 4_000L : 1_000L, ALL_DAY, Optional.empty()));
        }
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                requests,
                List.of(
                        vehicle("V1", 20_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.of(4), Optional.empty()),
                        vehicle("V2", 4_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.of(4), Optional.empty())),
                locations(locations.toArray(Location[]::new)),
                List.of());

        Solution solution = new ZoneQuotaBalancedFillConstruction().construct(problem, new DefaultProfile());
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        assertEquals(Set.of(), solution.bank());
        assertEquals(2, solution.routes().size());
        for (Route route : solution.routes()) {
            Set<RequestId> ids = new java.util.HashSet<>();
            for (var nodeId : route.visits()) {
                ids.add(problem.nodeRef(nodeId).orElseThrow().requestId());
            }
            if (route.vehicleId().equals(new VehicleId("V1"))) {
                assertEquals(Set.of(new RequestId("R1"), new RequestId("R2"), new RequestId("R3"), new RequestId("R4")), ids);
            } else {
                assertEquals(Set.of(new RequestId("R5"), new RequestId("R6"), new RequestId("R7"), new RequestId("R8")), ids);
            }
        }
    }
}
