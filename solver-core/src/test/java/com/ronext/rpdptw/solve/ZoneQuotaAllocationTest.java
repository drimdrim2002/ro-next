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
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

class ZoneQuotaAllocationTest {

    /** T38 — 호환 그룹 누적 검사(Hall 조건): 낭비만 보면 큰 차가 큰 차 금지 존 A로 가는 입력에서 A에 작은 차 2대·B에 큰 차. */
    @Test
    void hallConditionKeepsBigVehiclesOffRestrictedZones() {
        Problem problem = hallConditionProblem();
        List<ZoneQuotaAllocation.VehicleType> types = ZoneQuotaAllocation.vehicleTypes(problem);
        assertEquals(2, types.size());                                                  // (SMALL×2), (BIG×1) — maxVolume ASC
        assertEquals(List.of(new VehicleId("V2"), new VehicleId("V3")), types.get(0).vehicles());

        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        assertEquals(List.of("A", "B"), allocation.zones().stream().map(ZoneQuotaAllocation.Zone::zoneId).toList());  // 호환 유형 수 ASC
        assertEquals(List.of(new VehicleId("V2"), new VehicleId("V3")), allocation.vehiclesByZone().get("A"));
        assertEquals(List.of(new VehicleId("V1")), allocation.vehiclesByZone().get("B"));
    }

    /** T45 — 유형 조합 수가 커도 기권하지 않는다 (X20 대체): 17종 · 20종 · 3종×67대(Π 314,432). */
    @Test
    void manyTypesNoLongerAbstain() {
        List<Vehicle> distinct = new ArrayList<>();
        for (int i = 1; i <= 17; i++) {                                             // 유형이 서로 다른 17대 (종전 2¹⁷ 기권)
            distinct.add(vehicle(vehicleId(i), 10_000L + i * 100L, Optional.of(DEPOT), Optional.empty()));
        }
        assertRunsWithoutAbstaining(zoned(distinct, List.of(
                new ZoneSpec("Z1", 3, 3_000L, Set.of()), new ZoneSpec("Z2", 2, 2_000L, Set.of()))));

        List<Vehicle> twenty = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            twenty.add(vehicle(vehicleId(i), 10_000L + i * 100L, Optional.of(DEPOT), Optional.empty()));
        }
        assertRunsWithoutAbstaining(zoned(twenty, List.of(
                new ZoneSpec("Z1", 3, 3_000L, Set.of()), new ZoneSpec("Z2", 2, 2_000L, Set.of()))));

        List<Vehicle> threeTypes = new ArrayList<>();                               // 3종 × 각 67대 → Π = 68³ = 314,432
        threeTypes.addAll(fleet("VA", 67, 10_000L, Optional.empty()));
        threeTypes.addAll(fleet("VB", 67, 20_000L, Optional.empty()));
        threeTypes.addAll(fleet("VC", 67, 30_000L, Optional.empty()));
        assertRunsWithoutAbstaining(zoned(threeTypes, List.of(
                new ZoneSpec("Z1", 4, 4_000L, Set.of()), new ZoneSpec("Z2", 4, 3_000L, Set.of()),
                new ZoneSpec("Z3", 4, 2_000L, Set.of()))));
    }

    /** T46 — 풍부 유형(대수 ≥ Σ maxNeed)은 상태 벡터에서 빠진다: 희소 유형만으로 잡은 예산에서도 자르지 않는다. */
    @Test
    void abundantTypeLeavesStateVector() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.addAll(fleet("VA", 10, 4_000L, Optional.empty()));                  // 풍부 — Σ maxNeed = 3
        vehicles.addAll(fleet("VB", 2, 1_000L, Optional.empty()));                   // 희소 — Σ maxNeed = 6
        vehicles.addAll(fleet("VC", 1, 500L, Optional.empty()));                     // 희소 — Σ maxNeed = 12
        Problem problem = zoned(vehicles, List.of(
                new ZoneSpec("Z1", 2, 1_000L, Set.of()),
                new ZoneSpec("Z2", 2, 1_000L, Set.of()),
                new ZoneSpec("Z3", 2, 1_000L, Set.of())));

        int budget = (2 + 1) * (1 + 1) * (3 + 1);                                    // Π(희소 대수+1) × (존 수+1) = 24
        Allocation allocation = ZoneQuotaAllocation.allocate(problem, budget);
        assertFalse(allocation.truncated());                                         // 풍부 차원이 상태에 없다는 증거
        assertEquals(allocation.vehiclesByZone(), ZoneQuotaAllocation.allocate(problem).vehiclesByZone());

        long[] total = {0L, 0L, 0L};
        for (Zone zone : allocation.zones()) {
            long[] value = ZoneQuotaAllocation.value(
                    ZoneQuotaAllocation.Demand.of(problem, allocation.types(), zone), allocation.types(), quota(allocation, zone));
            for (int i = 0; i < 3; i++) {
                total[i] += value[i];
            }
        }
        assertArrayEquals(new long[] {0L, 4_000L, 4L}, total);                        // 손 계산 최적: 한 존만 VB 2대, 나머지 둘은 VA 1대씩
        assertTrue(countOfType(allocation, 4_000L) <= 10);                            // 풍부 유형 소비 ≤ 대수
        assertNoDuplicateVehicles(allocation);
    }

    /** T47 — 호환이 없는 두 묶음은 성분마다 따로 푼다: 합친 문제의 배정이 각각 단독으로 돌린 결과와 같다. */
    @Test
    void componentsSolvedIndependently() {
        List<Vehicle> alpha = new ArrayList<>();
        alpha.addAll(fleet("VA", 2, 1_000L, Optional.of("FA1")));
        alpha.addAll(fleet("VB", 1, 2_000L, Optional.of("FA2")));
        List<Vehicle> beta = new ArrayList<>();
        beta.addAll(fleet("VC", 2, 1_200L, Optional.of("FB1")));
        beta.addAll(fleet("VD", 1, 2_400L, Optional.of("FB2")));
        List<ZoneSpec> alphaZones = List.of(
                new ZoneSpec("A1", 3, 400L, Set.of("FA1", "FA2")), new ZoneSpec("A2", 3, 400L, Set.of("FA1", "FA2")));
        List<ZoneSpec> betaZones = List.of(
                new ZoneSpec("B1", 3, 480L, Set.of("FB1", "FB2")), new ZoneSpec("B2", 3, 480L, Set.of("FB1", "FB2")));

        List<Vehicle> both = new ArrayList<>(alpha);
        both.addAll(beta);
        List<ZoneSpec> bothZones = new ArrayList<>(alphaZones);
        bothZones.addAll(betaZones);
        Problem joint = zoned(both, bothZones);

        List<ZoneQuotaAllocation.VehicleType> types = ZoneQuotaAllocation.vehicleTypes(joint);
        assertEquals(2, ZoneQuotaAllocation.components(types, ZoneQuotaAllocation.zones(joint, types), joint).size());

        Allocation allocation = ZoneQuotaAllocation.allocate(joint, 16);              // 큰 묶음 하나 크기 — 합친 상태 공간에는 턱없이 모자란다
        assertFalse(allocation.truncated());
        Allocation onlyAlpha = ZoneQuotaAllocation.allocate(zoned(alpha, alphaZones));
        Allocation onlyBeta = ZoneQuotaAllocation.allocate(zoned(beta, betaZones));
        for (String zoneId : List.of("A1", "A2")) {
            assertEquals(onlyAlpha.vehiclesByZone().get(zoneId), allocation.vehiclesByZone().get(zoneId), zoneId);
        }
        for (String zoneId : List.of("B1", "B2")) {
            assertEquals(onlyBeta.vehiclesByZone().get(zoneId), allocation.vehiclesByZone().get(zoneId), zoneId);
        }
    }

    /** T48 — 폭 제한이 실제로 잘라도 결정적이고 유효하다 (X25·X27). */
    @Test
    void truncationIsDeterministicAndValid() {
        Problem problem = hallConditionProblem();
        Allocation allocation = ZoneQuotaAllocation.allocate(problem, 2);
        assertTrue(allocation.truncated());
        assertEquals(allocation.vehiclesByZone(), ZoneQuotaAllocation.allocate(problem, 2).vehiclesByZone());   // X8
        assertNoDuplicateVehicles(allocation);
        assertCompatibleOnly(problem, allocation);

        // 근사가 나와도 H23은 정상 실행된다 — 진입점에 예산 인자가 없으므로 기본 예산으로 확인한다
        DefaultProfile profile = new DefaultProfile();
        Solution solution = new ZoneQuotaBalancedFillConstruction().construct(problem, profile);
        assertTrue(Evaluator.evaluate(problem, profile, solution) instanceof EvaluationResult.Feasible);
    }

    /** T49 — 프론티어 열거는 공급이 아니라 수요에 묶인다: 호환 유형 100대라도 필요 3대까지만 연다 (§4.3). */
    @Test
    void frontierBoundedByMaxNeed() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.addAll(fleet("VA", 100, 1_000L, Optional.of("OK")));
        vehicles.addAll(fleet("VZ", 1, 5_000L, Optional.of("NO")));
        Problem problem = zoned(vehicles, List.of(new ZoneSpec("Z1", 3, 1_000L, Set.of("OK"))));

        List<ZoneQuotaAllocation.VehicleType> types = ZoneQuotaAllocation.vehicleTypes(problem);
        List<Zone> zones = ZoneQuotaAllocation.zones(problem, types);
        ZoneQuotaAllocation.Demand demand = ZoneQuotaAllocation.Demand.of(problem, types, zones.getFirst());
        int[] range = new int[types.size()];
        for (int t = 0; t < types.size(); t++) {
            range[t] = ZoneQuotaAllocation.maxNeed(demand, types.get(t));
        }
        assertEquals(3, range[0]);                                                   // 호환 유형 (maxVolume 1,000) — 100대가 아니라 수요 3
        assertEquals(0, range[1]);                                                   // 호환 안 되는 유형 (maxVolume 5,000)

        List<int[]> frontier = ZoneQuotaAllocation.frontier(range, demand);
        assertTrue(frontier.size() <= 4, "frontier " + frontier.size());
        for (int[] s : frontier) {
            for (int t = 0; t < types.size(); t++) {
                assertTrue(s[t] <= range[t], "s[" + t + "]=" + s[t]);
            }
        }
    }

    /** T50 — 공급 < 수요인 존에서도 완주하고, maxNeed를 넘겨 태우거나 호환 안 되는 차를 넣지 않는다 (X21). */
    @Test
    void uncoverableZoneTakesAtMostMaxNeed() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.addAll(fleet("VA", 2, 1_000L, Optional.of("OK")));
        vehicles.addAll(fleet("VZ", 3, 5_000L, Optional.of("NO")));
        Problem problem = zoned(vehicles, List.of(
                new ZoneSpec("HARD", 5, 1_000L, Set.of("OK")),                       // 수요 5,000 · 공급 2,000
                new ZoneSpec("EASY", 2, 1_000L, Set.of("NO"))));

        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        assertFalse(allocation.truncated());
        assertNoDuplicateVehicles(allocation);
        assertCompatibleOnly(problem, allocation);
        for (Zone zone : allocation.zones()) {
            ZoneQuotaAllocation.Demand demand = ZoneQuotaAllocation.Demand.of(problem, allocation.types(), zone);
            int[] quota = quota(allocation, zone);
            for (int t = 0; t < allocation.types().size(); t++) {
                assertTrue(quota[t] <= ZoneQuotaAllocation.maxNeed(demand, allocation.types().get(t)),
                        zone.zoneId() + " t" + t);
            }
        }
    }

    // ---- 손조립 도우미 ----

    /** T38·T48 공통 입력 — 큰 차 금지 존 A(작은 차 2대 몫)와 자유 존 B. */
    private static Problem hallConditionProblem() {
        LocationId a1 = new LocationId("A1");
        LocationId a2 = new LocationId("A2");
        LocationId b1 = new LocationId("B1");
        // 존 A 19,600 = 9,800 × 2 (SMALL만 허용) · 존 B 15,000 (BIG·SMALL 허용이지만 SMALL 10,500에는 안 든다)
        // 차량 BIG 20,000 × 1 · SMALL 10,500 × 2. 낭비만 보면 A ← BIG(400) 이 SMALL×2(1,400)보다 싸다
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

    private static List<Vehicle> fleet(String prefix, int count, long capacity, Optional<String> feature) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            vehicles.add(vehicle(String.format("%s%03d", prefix, i), capacity,
                    Optional.of(DEPOT), Optional.empty(), OptionalInt.empty(), feature));
        }
        return vehicles;
    }

    private static String vehicleId(int i) {
        return String.format("V%03d", i);
    }

    private static void assertRunsWithoutAbstaining(Problem problem) {
        assertFalse(new ZoneQuotaBalancedFillConstruction().abstains(problem));
        assertFalse(new ZoneQuotaSubsetFillConstruction().abstains(problem));
        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        assertFalse(allocation.truncated());
        assertNoDuplicateVehicles(allocation);
        assertCompatibleOnly(problem, allocation);
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
                for (RequestId id : zone.members()) {
                    compatible |= allocation.types().get(t).compatible().contains(id);
                }
                assertTrue(compatible, zone.zoneId() + " t" + t);
            }
        }
    }

    private static void assertNoDuplicateVehicles(Allocation allocation) {
        Set<VehicleId> seen = new java.util.LinkedHashSet<>();
        for (List<VehicleId> assigned : allocation.vehiclesByZone().values()) {
            for (VehicleId id : assigned) {
                assertTrue(seen.add(id), id.value());
            }
        }
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

    private static int countOfType(Allocation allocation, long maxVolume) {
        int count = 0;
        for (Zone zone : allocation.zones()) {
            int[] quota = quota(allocation, zone);
            for (int t = 0; t < allocation.types().size(); t++) {
                if (allocation.types().get(t).maxVolume() == maxVolume) {
                    count += quota[t];
                }
            }
        }
        return count;
    }
}
