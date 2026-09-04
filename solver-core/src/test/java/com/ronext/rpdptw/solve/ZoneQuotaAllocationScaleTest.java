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
import java.util.LinkedHashSet;
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
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

/**
 * T51 — 규모 측정. 종전 기권선(Π > 65,536)을 훌쩍 넘는 두 함대에서 존 배정 DP가 완주하는지,
 * 예산을 조이면 근사로 내려가되 여전히 유효한지를 본다. 소요는 출력만 하고 판정하지 않는다 (T25와 같은 취급).
 */
class ZoneQuotaAllocationScaleTest {

    @Test
    void largeFleetCompletesWithinBudget() {
        List<Vehicle> threeTypes = new ArrayList<>();                                  // Π = 68³ = 314,432
        threeTypes.addAll(fleet("VA", 67, 10_000L));
        threeTypes.addAll(fleet("VB", 67, 20_000L));
        threeTypes.addAll(fleet("VC", 67, 30_000L));
        run("3종×67대·존 5 (Π 314,432)", zoned(threeTypes, 5, 40, 3_000L), false);
        run("3종×67대·존 20 (Π 314,432)", zoned(threeTypes, 20, 40, 3_000L), true);

        List<Vehicle> twentyTypes = new ArrayList<>();                                 // Π = 2²⁰ = 1,048,576
        for (int i = 1; i <= 20; i++) {
            twentyTypes.add(vehicle(String.format("V%03d", i), 10_000L + i * 100L,
                    Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.empty()));
        }
        run("20종×1대·존 7 (Π 2²⁰)", zoned(twentyTypes, 7, 2, 3_000L), false);
        run("20종×1대·존 20 (Π 2²⁰)", zoned(twentyTypes, 20, 2, 3_000L), true);
    }

    /**
     * 기본 예산에서 완주하는지(그리고 잘랐는지)와, 예산 1,000으로 조이면 근사로 내려가되
     * 여전히 유효·결정적인지를 본다. 소요는 출력만 하고 판정하지 않는다.
     */
    private static void run(String label, Problem problem, boolean expectTruncated) {
        long start = System.nanoTime();
        Allocation full = ZoneQuotaAllocation.allocate(problem);
        long fullMillis = (System.nanoTime() - start) / 1_000_000L;
        assertValid(problem, full);
        assertEquals(expectTruncated, full.truncated(), label + " — 기본 예산");
        assertTrue(sameAssignment(full, ZoneQuotaAllocation.allocate(problem)), label + " — 결정적이다");

        start = System.nanoTime();
        Allocation tight = ZoneQuotaAllocation.allocate(problem, 1_000);
        long tightMillis = (System.nanoTime() - start) / 1_000_000L;
        assertValid(problem, tight);
        assertTrue(sameAssignment(tight, ZoneQuotaAllocation.allocate(problem, 1_000)), label + " — 근사도 결정적이다");
        if (expectTruncated) {
            assertTrue(tight.truncated(), label + " — 예산 1,000이면 잘려야 한다");
        }

        System.out.println("T51 " + label + " — 기본 예산 " + fullMillis + " ms truncated=" + full.truncated()
                + " · 예산 1,000 " + tightMillis + " ms truncated=" + tight.truncated());
    }

    /** 차량 중복 배정 없음 · 존마다 호환 유형만 · 배정 대수 ≤ maxNeed. */
    private static void assertValid(Problem problem, Allocation allocation) {
        Set<VehicleId> seen = new LinkedHashSet<>();
        for (Zone zone : allocation.zones()) {
            ZoneQuotaAllocation.Demand demand = ZoneQuotaAllocation.Demand.of(problem, allocation.types(), zone);
            int[] quota = new int[allocation.types().size()];
            for (VehicleId id : allocation.vehiclesByZone().get(zone.zoneId())) {
                assertTrue(seen.add(id), id.value());
                for (int t = 0; t < allocation.types().size(); t++) {
                    if (allocation.types().get(t).vehicles().contains(id)) {
                        quota[t]++;
                        break;
                    }
                }
            }
            for (int t = 0; t < allocation.types().size(); t++) {
                assertTrue(quota[t] <= ZoneQuotaAllocation.maxNeed(demand, allocation.types().get(t)),
                        zone.zoneId() + " t" + t);
            }
        }
    }

    private static boolean sameAssignment(Allocation a, Allocation b) {
        return a.vehiclesByZone().equals(b.vehiclesByZone());
    }

    private static List<Vehicle> fleet(String prefix, int count, long capacity) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            vehicles.add(vehicle(String.format("%s%03d", prefix, i), capacity,
                    Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), Optional.empty()));
        }
        return vehicles;
    }

    /** 존 zoneCount개 × 각 perZone건의 DELIVERY_ONLY(각 weight = volume). */
    private static Problem zoned(List<Vehicle> vehicles, int zoneCount, int perZone, long weight) {
        List<Location> places = new ArrayList<>();
        places.add(at(DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        int index = 0;
        for (int z = 1; z <= zoneCount; z++) {
            for (int i = 0; i < perZone; i++) {
                index++;
                LocationId at = new LocationId(String.format("L%04d", index));
                places.add(at(at, 37.0 + 0.0005 * index, 127.0 + 0.0005 * index));
                requests.add(delivery(String.format("R%04d", index), at, weight, ALL_DAY,
                        Optional.of(String.format("Z%02d", z))));
            }
        }
        return freeze(List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                requests, vehicles, locations(places.toArray(Location[]::new)), List.of());
    }
}
