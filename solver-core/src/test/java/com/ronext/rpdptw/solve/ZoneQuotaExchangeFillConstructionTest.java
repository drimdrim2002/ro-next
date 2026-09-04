package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static com.ronext.rpdptw.solve.ConstructionFixtures.withFeatures;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

/** H25 `zone-quota-exchange-fill` — greedy 존 배정 + 구역 간 교환 (heuristics 문서 §8 T53~T55). */
class ZoneQuotaExchangeFillConstructionTest {

    /** T53 — T38의 입력에서 greedy + 교환의 최종 값이 존 배정 DP의 값과 같다. */
    @Test
    void exchangeReachesDpValueOnHallFixture() {
        Problem problem = hallConditionProblem();

        Allocation exchanged = ZoneQuotaExchangeFillConstruction.allocate(problem);
        assertFalse(exchanged.truncated());                                          // greedy+교환은 근사 플래그를 쓰지 않는다
        assertNoDuplicateVehicles(exchanged);
        assertCompatibleOnly(problem, exchanged);

        Allocation dp = ZoneQuotaAllocation.allocate(problem);
        assertArrayEquals(totalValue(problem, dp), totalValue(problem, exchanged));
        assertEquals(dp.vehiclesByZone(), exchanged.vehiclesByZone());                // 이 입력에서는 배정 자체가 같다
    }

    /**
     * T54 — greedy가 부족을 남기고 교환이 그것을 줄이는 입력: trace가 사전식 엄격 감소하고 길이 ≤ MAX_SCANS,
     * 두 번 실행해 같은 trace(X8). 개선 이웃이 없는 입력에서는 길이 0 (X34).
     */
    @Test
    void scansStrictlyImproveAndTerminate() {
        Problem problem = greedyLeavesShortageProblem();

        List<long[]> trace = new ArrayList<>();
        Allocation allocation = ZoneQuotaExchangeFillConstruction.allocate(problem, trace);
        assertFalse(trace.isEmpty(), "교환이 한 번도 개선하지 못했다");
        assertTrue(trace.size() <= ZoneQuotaExchangeFillConstruction.MAX_SCANS, "trace " + trace.size());
        for (int i = 1; i < trace.size(); i++) {
            assertTrue(ZoneQuotaAllocation.compare(trace.get(i), trace.get(i - 1)) < 0, "scan " + i);
        }
        assertArrayEquals(trace.getLast(), totalValue(problem, allocation));           // 마지막 trace = 최종 배정 값
        assertNoDuplicateVehicles(allocation);
        assertCompatibleOnly(problem, allocation);

        List<long[]> again = new ArrayList<>();
        ZoneQuotaExchangeFillConstruction.allocate(problem, again);
        assertEquals(trace.size(), again.size());
        for (int i = 0; i < trace.size(); i++) {
            assertArrayEquals(trace.get(i), again.get(i), "scan " + i);
        }

        // 차량 1대 — MOVE도 SWAP도 개선하지 못한다 (X34)
        Problem single = zoned(List.of(vehicle("V001", 10_000L, Optional.of(DEPOT), Optional.empty())),
                List.of(new ZoneSpec("Z1", 1, 1_000L, Set.of())));
        List<long[]> none = new ArrayList<>();
        ZoneQuotaExchangeFillConstruction.allocate(single, none);
        assertEquals(0, none.size());
    }

    /** T55 — 유형이 서로 다른 17대(Π = 2¹⁷)에서도 기권하지 않고 완주한다 (X31). */
    @Test
    void neverAbstainsBeyondCombinationLimit() {
        List<Vehicle> distinct = new ArrayList<>();
        for (int i = 1; i <= 17; i++) {
            distinct.add(vehicle(String.format("V%03d", i), 10_000L + i * 100L, Optional.of(DEPOT), Optional.empty()));
        }
        Problem problem = zoned(distinct, List.of(
                new ZoneSpec("Z1", 3, 3_000L, Set.of()), new ZoneSpec("Z2", 2, 2_000L, Set.of())));

        ZoneQuotaExchangeFillConstruction heuristic = new ZoneQuotaExchangeFillConstruction();
        assertFalse(heuristic.abstains(problem));
        assertEquals(17, ZoneQuotaAllocation.vehicleTypes(problem).size());
        assertNoDuplicateVehicles(ZoneQuotaExchangeFillConstruction.allocate(problem));

        DefaultProfile profile = new DefaultProfile();
        Solution solution = heuristic.construct(problem, profile);
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        assertTrue(Evaluator.evaluate(problem, profile, solution) instanceof EvaluationResult.Feasible);
    }

    // ---- 손조립 도우미 ----

    /** T38과 같은 입력 — 큰 차 금지 존 A(작은 차 2대 몫)와 자유 존 B. */
    private static Problem hallConditionProblem() {
        LocationId a1 = new LocationId("A1");
        LocationId a2 = new LocationId("A2");
        LocationId b1 = new LocationId("B1");
        return freeze(
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
    }

    /**
     * greedy가 큰 차를 먼저 쓰는 존(Σ 28,000, 4건 × 7,000)을 처리하다 큰 차를 소진해,
     * 큰 차 한 대가 있어야만 덮이는 존(단품 24,000)을 못 덮게 되는 입력. 교환이 그 부족을 줄인다.
     */
    private static Problem greedyLeavesShortageProblem() {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            vehicles.add(vehicle(String.format("VS%02d", i), 10_000L, Optional.of(DEPOT), Optional.empty()));
        }
        vehicles.add(vehicle("VB01", 25_000L, Optional.of(DEPOT), Optional.empty()));
        return zoned(vehicles, List.of(
                new ZoneSpec("ZA", 4, 7_000L, Set.of()),                              // Σ 28,000 — 존 순서상 먼저
                new ZoneSpec("ZB", 1, 24_000L, Set.of())));                           // 단품 24,000 — 큰 차만 덮는다
    }

    /** 존 하나 — DELIVERY_ONLY count건, 각 weight(=volume), 허용 차급 features(빈 집합이면 제한 없음). */
    private record ZoneSpec(String zoneId, int count, long weight, Set<String> features) {}

    private static Problem zoned(List<Vehicle> vehicles, List<ZoneSpec> specs) {
        List<Location> places = new ArrayList<>();
        places.add(at(DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        int index = 0;
        for (ZoneSpec spec : specs) {
            for (int i = 0; i < spec.count(); i++) {
                index++;
                LocationId at = new LocationId(String.format("L%03d", index));
                places.add(at(at, 37.0 + 0.001 * index, 127.0 + 0.001 * index));
                Request request = delivery(String.format("R%03d", index), at, spec.weight(), ALL_DAY, Optional.of(spec.zoneId()));
                requests.add(spec.features().isEmpty() ? request : withFeatures(request, spec.features()));
            }
        }
        return freeze(List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                requests, vehicles, locations(places.toArray(Location[]::new)), List.of());
    }

    /** 배정 전체의 값 (Σ부족, Σ낭비, Σ사용 대수) — DP·H25가 같은 함수를 쓴다. */
    private static long[] totalValue(Problem problem, Allocation allocation) {
        long[] total = {0L, 0L, 0L};
        for (Zone zone : allocation.zones()) {
            long[] value = ZoneQuotaAllocation.value(
                    ZoneQuotaAllocation.Demand.of(problem, allocation.types(), zone), allocation.types(), quota(allocation, zone));
            for (int i = 0; i < 3; i++) {
                total[i] += value[i];
            }
        }
        return total;
    }

    private static int[] quota(Allocation allocation, Zone zone) {
        int[] quota = new int[allocation.types().size()];
        for (VehicleId id : allocation.vehiclesByZone().get(zone.zoneId())) {
            for (int t = 0; t < allocation.types().size(); t++) {
                if (allocation.types().get(t).vehicles().contains(id)) {
                    quota[t]++;
                    break;
                }
            }
        }
        return quota;
    }

    private static void assertNoDuplicateVehicles(Allocation allocation) {
        Set<VehicleId> seen = new LinkedHashSet<>();
        for (List<VehicleId> assigned : allocation.vehiclesByZone().values()) {
            for (VehicleId id : assigned) {
                assertTrue(seen.add(id), id.value());
            }
        }
    }

    /** 존마다 배정 차량이 compat(z) 유형뿐인가. */
    private static void assertCompatibleOnly(Problem problem, Allocation allocation) {
        for (Zone zone : allocation.zones()) {
            int[] quota = quota(allocation, zone);
            for (int t = 0; t < allocation.types().size(); t++) {
                if (quota[t] == 0) {
                    continue;
                }
                boolean compatible = false;
                for (var id : zone.members()) {
                    compatible |= allocation.types().get(t).compatible().contains(id);
                }
                assertTrue(compatible, zone.zoneId() + " t" + t);
            }
        }
    }
}
