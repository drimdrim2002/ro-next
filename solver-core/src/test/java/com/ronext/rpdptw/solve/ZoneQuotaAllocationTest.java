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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;

class ZoneQuotaAllocationTest {

    /** T38 — 호환 그룹 누적 검사(Hall 조건): 낭비만 보면 큰 차가 큰 차 금지 존 A로 가는 입력에서 A에 작은 차 2대·B에 큰 차. */
    @Test
    void hallConditionKeepsBigVehiclesOffRestrictedZones() {
        LocationId a1 = new LocationId("A1");
        LocationId a2 = new LocationId("A2");
        LocationId b1 = new LocationId("B1");
        // 존 A 19,600 = 9,800 × 2 (SMALL만 허용) · 존 B 15,000 (BIG·SMALL 허용이지만 SMALL 10,500에는 안 든다)
        // 차량 BIG 20,000 × 1 · SMALL 10,500 × 2. 낭비만 보면 A ← BIG(400) 이 SMALL×2(1,400)보다 싸다
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        withFeatures(delivery("A1", a1, 9_800L, ALL_DAY, Optional.of("A")), Set.of("SMALL")),
                        withFeatures(delivery("A2", a2, 9_800L, ALL_DAY, Optional.of("A")), Set.of("SMALL")),
                        withFeatures(delivery("B1", b1, 15_000L, ALL_DAY, Optional.of("B")), Set.of("BIG", "SMALL"))),
                List.of(
                        vehicle("V1", 20_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.of("BIG")),
                        vehicle("V2", 10_500L, Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.of("SMALL")),
                        vehicle("V3", 10_500L, Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.of("SMALL"))),
                locations(at(DEPOT, 37.0, 127.0), at(a1, 37.01, 127.0), at(a2, 37.02, 127.0), at(b1, 37.0, 127.01)),
                List.of());

        List<ZoneQuotaAllocation.VehicleType> types = ZoneQuotaAllocation.vehicleTypes(problem);
        assertEquals(2, types.size());                                                  // (SMALL×2), (BIG×1) — maxVolume ASC
        assertEquals(List.of(new VehicleId("V2"), new VehicleId("V3")), types.get(0).vehicles());
        assertEquals(6L, ZoneQuotaAllocation.combinations(types));                      // (2+1)(1+1)

        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        assertEquals(List.of("A", "B"), allocation.zones().stream().map(ZoneQuotaAllocation.Zone::zoneId).toList());  // 호환 유형 수 ASC
        assertEquals(List.of(new VehicleId("V2"), new VehicleId("V3")), allocation.vehiclesByZone().get("A"));
        assertEquals(List.of(new VehicleId("V1")), allocation.vehiclesByZone().get("B"));
    }

    /** T39 — 유형이 서로 다른 17대(2¹⁷ > 65,536)면 H23·H24 기권 · 같은 유형 31대(32)면 실행 (X20). */
    @Test
    void combinationsBoundAbstains() {
        List<Vehicle> distinct = new ArrayList<>();
        for (int i = 1; i <= 17; i++) {
            distinct.add(vehicle("V" + i, 10_000L + i, Optional.of(DEPOT), Optional.empty()));
        }
        LocationId l1 = new LocationId("L1");
        List<Request> requests = List.of(delivery("R1", l1, 1_000L));
        java.util.Map<LocationId, Location> locations = locations(at(DEPOT, 37.0, 127.0), at(l1, 37.01, 127.0));
        Problem many = freeze(List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())), requests, distinct, locations, List.of());
        assertTrue(ZoneQuotaAllocation.combinations(ZoneQuotaAllocation.vehicleTypes(many)) > ZoneQuotaAllocation.MAX_COMBINATIONS);
        assertTrue(new ZoneQuotaBalancedFillConstruction().abstains(many));
        assertTrue(new ZoneQuotaSubsetFillConstruction().abstains(many));

        Problem same = ConstructionFixtures.ring(3, 31, 30_000L, 10_000L);
        assertEquals(32L, ZoneQuotaAllocation.combinations(ZoneQuotaAllocation.vehicleTypes(same)));
        assertFalse(new ZoneQuotaBalancedFillConstruction().abstains(same));
        assertFalse(new ZoneQuotaSubsetFillConstruction().abstains(same));
    }
}
