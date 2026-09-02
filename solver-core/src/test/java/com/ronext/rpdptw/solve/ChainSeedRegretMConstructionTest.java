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
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class ChainSeedRegretMConstructionTest {

    /** T29 — 시간상 이어 붙일 수 있는 요청이 seed에서 빠지고, 이어 붙일 수 없는 요청 수만큼 경로가 열림. */
    @Test
    void chainExclusionDropsLinkableSeeds() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        LocationId far = new LocationId("FAR");
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        delivery("R1", a, 1_000L, new TimeWindow(28_800L, 32_400L), Optional.empty()),   // 08:00–09:00
                        delivery("R2", b, 1_000L, new TimeWindow(36_000L, 39_600L), Optional.empty()),   // 10:00–11:00 — R1·R3 뒤에 이어진다
                        delivery("R3", far, 1_000L, new TimeWindow(30_600L, 32_400L), Optional.empty())), // 08:30–09:00 — R1 뒤에 못 잇는다
                List.of(
                        vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.empty()),
                        vehicle("V2", 30_000L, Optional.of(DEPOT), Optional.empty()),
                        vehicle("V3", 30_000L, Optional.of(DEPOT), Optional.empty())),
                locations(at(DEPOT, 37.0, 127.0), at(a, 37.01, 127.0), at(b, 37.02, 127.0), at(far, 37.2, 127.2)),
                List.of());

        assertEquals(List.of(new RequestId("R1"), new RequestId("R3")), ChainSeedRegretMConstruction.seeds(problem));

        Solution solution = new ChainSeedRegretMConstruction().construct(problem, new DefaultProfile());
        assertEquals(2, solution.routes().size());                                // seed 수만큼 열리고 R2는 기존 경로로
        assertTrue(solution.bank().isEmpty());
    }
}
