package com.ronext.rpdptw.domain.input;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Compatibility;
import com.ronext.rpdptw.domain.InputException;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Plan;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.Vehicle;

class PlanNormalizerTest {

    static final LocalDateTime PLAN_START = LocalDateTime.of(2023, 9, 13, 0, 0, 0);
    static final LocalDateTime PLAN_END = LocalDateTime.of(2023, 9, 14, 0, 0, 0);

    private final PlanNormalizer normalizer = new PlanNormalizer();

    @Test
    void absentOptionalMeansNoConstraint() {
        Vehicle vehicle = normalizer.normalize(minimalPlan()).vehicles().getFirst();

        assertTrue(vehicle.effectiveMaxStopCount().isEmpty());
        assertTrue(vehicle.maxDriveTimeSec().isEmpty());
        assertTrue(vehicle.maxDriveDistMeter().isEmpty());
        assertTrue(vehicle.zoneIds().isEmpty());
        assertTrue(vehicle.speedKmH().isEmpty());
        assertTrue(vehicle.capabilities().isEmpty());
    }

    @Test
    void foldsWildcardFeatureToNoConstraint() {
        // 차급 칸이 "ALL"·blank·부재인 차량은 전 차급이다. 문자 그대로 비교하면 어느 주문도
        // 싣지 못하는 유령 차량이 된다 — E26·E14가 막으려는 결함이다 (Domain §3.4).
        Plan plan = normalizer.normalize(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1",
                        null,
                        side("C1", null, null, null, null),
                        List.of(item("I1", "1", "0.1", 1, 0L)),
                        List.of("T1"),
                        null)),
                List.of(
                        vehicle("V-ALL", "ALL", null, null, null, null, null),
                        vehicle("V-BLANK", "", null, null, null, null, null),
                        vehicle("V-T1", "T1", null, null, null, null, null)),
                defaults()));

        Vehicle wildcardByAll = plan.vehicles().get(0);
        Vehicle wildcardByBlank = plan.vehicles().get(1);
        Vehicle t1 = plan.vehicles().get(2);
        assertTrue(wildcardByAll.vehicleFeature().isEmpty());
        assertTrue(wildcardByBlank.vehicleFeature().isEmpty());
        assertEquals("T1", t1.vehicleFeature().orElseThrow());

        // 접은 결과가 실제 호환 판정까지 통과하는지 확인한다 (정규화 → Compatibility 연결).
        Request t1Only = plan.requests().getFirst();
        assertTrue(Compatibility.size(wildcardByAll, t1Only));
        assertTrue(Compatibility.size(wildcardByBlank, t1Only));
        assertTrue(Compatibility.size(t1, t1Only));

        // 주문 쪽 ["ALL"]도 같은 뜻 — 전 차급 허용이라 빈 Optional로 접힌다 (E14 통과 케이스).
        Request allowsAll = normalizer
                .normalize(plan(
                        List.of(depot("WIN_0")),
                        List.of(new RequestInput(
                                "O1",
                                null,
                                side("C1", null, null, null, null),
                                List.of(item("I1", "1", "0.1", 1, 0L)),
                                List.of("ALL"),
                                null)),
                        List.of(vehicle("V-T1", "T1", null, null, null, null, null)),
                        defaults()))
                .requests()
                .getFirst();
        assertTrue(allowsAll.allowedVehicleFeatures().isEmpty());
        assertTrue(Compatibility.size(t1, allowsAll));

        // 구역도 같은 규칙이다 — "ALL"은 구역 이름이 될 수 없는 예약어라 전 구역으로 접힌다 (E35).
        Plan zones = normalizer.normalize(plan(
                List.of(depot("WIN_0")),
                List.of(
                        deliveryOnly("O-ZONE16", zonedSide("C1", "37.1", "127.1", "ZONE_16")),
                        deliveryOnly("O-ZONE99", zonedSide("C2", "37.2", "127.2", "ZONE_99")),
                        deliveryOnly("O-ALL", zonedSide("C3", "37.3", "127.3", "ALL")),
                        deliveryOnly("O-BLANK", zonedSide("C4", "37.4", "127.4", ""))),
                List.of(
                        vehicleWithZones("V-ALL", Set.of("ALL")),
                        vehicleWithZones("V-ZONE16", Set.of("ZONE_16"))),
                defaults()));

        Vehicle everywhereByAll = zones.vehicles().get(0);
        Vehicle onlyZone16 = zones.vehicles().get(1);
        assertTrue(everywhereByAll.zoneIds().isEmpty());
        assertEquals(Set.of("ZONE_16"), onlyZone16.zoneIds().orElseThrow());

        // "ALL"은 구역 이름이 될 수 없어 단독으로만 온다 — 섞이면 전 구역인지 그 구역만인지
        // 애매하므로 거부한다 (E35b, 주문 차급 ["ALL","T1"]과 같은 규칙).
        InputException mixed = failure(plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicleWithZones("V1", Set.of("ALL", "ZONE_1"))),
                defaults()));
        assertEquals(InputException.Kind.INVALID_INPUT, mixed.kind());
        assertEquals("vehicles[0].zoneIds", mixed.field());

        RequestSide zone16 = zones.requests().get(0).delivery().orElseThrow();
        RequestSide zone99 = zones.requests().get(1).delivery().orElseThrow();
        RequestSide allZones = zones.requests().get(2).delivery().orElseThrow();
        RequestSide blankZone = zones.requests().get(3).delivery().orElseThrow();
        assertEquals("ZONE_16", zone16.zoneId().orElseThrow());
        assertTrue(allZones.zoneId().isEmpty());
        assertTrue(blankZone.zoneId().isEmpty());

        assertTrue(Compatibility.zone(everywhereByAll, zone99));   // ["ALL"] 차량은 어디든 간다
        assertTrue(Compatibility.zone(onlyZone16, zone16));        // 구체 구역이 목록에 있으면 통과
        assertTrue(Compatibility.zone(onlyZone16, allZones));      // 구역 제약 없는 주문
        assertTrue(Compatibility.zone(onlyZone16, blankZone));
        assertFalse(Compatibility.zone(onlyZone16, zone99));       // 구체 구역인데 목록에 없으면 불가
    }

    @Test
    void rejectsNullElements() {
        // 목록 안의 빈 값은 관문에서 막는다 — 통과시키면 Set.copyOf에서 NPE(5xx)가 되어
        // 입력 오류(4xx)로 돌려줄 수 없다 (E36, Domain §3 서두).
        InputException features = failure(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1",
                        null,
                        side("C1", null, null, null, null),
                        List.of(item("I1", "1", "0.1", 1, 0L)),
                        Arrays.asList("T1", null),
                        null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults()));
        assertEquals(InputException.Kind.INVALID_INPUT, features.kind());
        assertEquals("orders[0].vehicleFeature", features.field());

        InputException capabilities = failure(plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicleWithCapabilities("V1", setWithNull("CRANE"))),
                defaults()));
        assertEquals("vehicles[0].capabilities", capabilities.field());

        InputException zoneIds = failure(plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicleWithZones("V1", setWithNull("ZONE_1"))),
                defaults()));
        assertEquals("vehicles[0].zoneIds", zoneIds.field());

        InputException items = failure(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1", null, side("C1", null, null, null, null), Arrays.asList((ItemInput) null), null, null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults()));
        assertEquals("orders[0].items[0]", items.field());

        InputException vehicles = failure(plan(
                List.of(depot("WIN_0")),
                List.of(),
                Arrays.asList((VehicleInput) null),
                defaults()));
        assertEquals("vehicles[0]", vehicles.field());
    }

    @Test
    void acceptsOneRotationRejectsMultiTrip() {
        assertDoesNotThrow(() -> normalizer.normalize(planWithMultiRotation(0)));
        assertDoesNotThrow(() -> normalizer.normalize(planWithMultiRotation(1)));

        assertEquals(InputException.Kind.UNSUPPORTED_INPUT, kindOf(planWithMultiRotation(2)));
        assertEquals(InputException.Kind.UNSUPPORTED_INPUT, kindOf(planWithMultiRotation(5)));
        assertEquals(InputException.Kind.UNSUPPORTED_INPUT, kindOf(planWithMultiRotation(-1)));
        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(planWithMultiRotation(-2)));
    }

    @Test
    void serviceTimeFormula() {
        PlanInput input = plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1",
                        null,
                        side("C1", null, null, 10L, null),
                        List.of(
                                item("I1", "1", "0.1", 3, 5L),
                                item("I2", "1", "0.1", 4, 2L)),
                        null,
                        null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults());

        Request request = normalizer.normalize(input).requests().getFirst();
        assertEquals(10L + (5L * 3L) + (2L * 4L), request.delivery().orElseThrow().serviceTimeSec());
        assertEquals(33L, request.delivery().orElseThrow().serviceTimeSec());
    }

    @Test
    void reqDateDefaultsToPlanEnd() {
        LocalDateTime pickupReq = PLAN_START.minusHours(1);
        LocalDateTime deliveryReq = PLAN_START.plusHours(6);
        PlanInput input = plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1",
                        side("P1", "37.2", "127.2", null, null, null, pickupReq),
                        side("C1", "37.1", "127.1", null, null, null, deliveryReq),
                        List.of(item("I1", "1", "0.1", 1, 0L)),
                        null,
                        null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults());

        Plan result = normalizer.normalize(input);
        Request request = result.requests().getFirst();
        assertEquals(result.timeBase().toSeconds(pickupReq), request.pickup().orElseThrow().reqDateSec());
        assertEquals(result.timeBase().toSeconds(deliveryReq), request.delivery().orElseThrow().reqDateSec());

        PlanInput missingReqDate = plan(
                List.of(depot("WIN_0")),
                List.of(deliveryOnly("O1", side("C1", null, null, null, null))),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults());
        Plan defaulted = normalizer.normalize(missingReqDate);
        assertEquals(defaulted.planEndSec(), defaulted.requests().getFirst().delivery().orElseThrow().reqDateSec());
    }

    @Test
    void foldsGlobalStopCount() {
        PlanInput globalOnly = plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                new OptionsInput(null, null, null, null, 28));
        assertEquals(28, normalizer.normalize(globalOnly).vehicles().getFirst().effectiveMaxStopCount().orElseThrow());

        PlanInput both = plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicle("V1", null, 30, null, null, null, null)),
                new OptionsInput(null, null, null, null, 28));
        assertEquals(28, normalizer.normalize(both).vehicles().getFirst().effectiveMaxStopCount().orElseThrow());
    }

    @Test
    void rejectsAmbiguousInput() {
        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput("O1", null, side("C1", null, null, null, null), List.of(), null, null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults())));
        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput("O1", null, side("C1", null, null, null, null), null, null, null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults())));

        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1",
                        null,
                        side("C1", null, null, null, null),
                        List.of(item("I1", "1", "0.1", 1, 0L)),
                        List.of("ALL", "T1"),
                        null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults())));
        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0")),
                List.of(new RequestInput(
                        "O1",
                        null,
                        side("C1", null, null, null, null),
                        List.of(item("I1", "1", "0.1", 1, 0L)),
                        List.of(),
                        null)),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults())));

        assertEquals(
                InputException.Kind.INVALID_INPUT,
                kindOf(new PlanInput(
                        "P1",
                        null,
                        PLAN_START,
                        PLAN_START,
                        List.of(depot("WIN_0")),
                        List.of(),
                        List.of(vehicle("V1", null, null, null, null, null, null)),
                        List.of(),
                        defaults())));

        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0", "37.0", "127.0")),
                List.of(deliveryOnly("O1", new SideInput("WIN_0", "37.1", "127.1", null, null, null, null, null))),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults())));

        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0")),
                List.of(
                        deliveryOnly("O1", side("C1", null, null, null, null)),
                        deliveryOnly("O1", side("C2", "37.3", "127.3", null, null, null, null))),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults())));
        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(
                        vehicle("V1", null, null, null, null, null, null),
                        vehicle("V1", null, null, null, null, null, null)),
                defaults())));
    }

    @Test
    void patternFollowsPresentSides() {
        Request deliveryOnly = normalizer
                .normalize(plan(
                        List.of(depot("WIN_0")),
                        List.of(deliveryOnly("O1", side("C1", null, null, null, null))),
                        List.of(vehicle("V1", null, null, null, null, null, null)),
                        defaults()))
                .requests()
                .getFirst();
        assertEquals(ServicePattern.DELIVERY_ONLY, deliveryOnly.pattern());
        assertTrue(deliveryOnly.pickup().isEmpty());
        assertTrue(deliveryOnly.delivery().isPresent());

        Request pickupOnly = normalizer
                .normalize(plan(
                        List.of(depot("WIN_0")),
                        List.of(pickupOnly("O1", side("P1", "37.2", "127.2", null, null, null, null))),
                        List.of(vehicle("V1", null, null, null, null, null, null)),
                        defaults()))
                .requests()
                .getFirst();
        assertEquals(ServicePattern.PICKUP_ONLY, pickupOnly.pattern());
        assertTrue(pickupOnly.pickup().isPresent());
        assertTrue(pickupOnly.delivery().isEmpty());

        Request both = normalizer
                .normalize(plan(
                        List.of(depot("WIN_0")),
                        List.of(new RequestInput(
                                "O1",
                                side("P1", "37.2", "127.2", null, null, null, null),
                                side("C1", null, null, null, null),
                                List.of(item("I1", "1", "0.1", 1, 0L)),
                                null,
                                null)),
                        List.of(vehicle("V1", null, null, null, null, null, null)),
                        defaults()))
                .requests()
                .getFirst();
        assertEquals(ServicePattern.PICKUP_DELIVERY, both.pattern());
        assertTrue(both.pickup().isPresent());
        assertTrue(both.delivery().isPresent());

        assertEquals(
                InputException.Kind.INVALID_INPUT,
                kindOf(plan(
                        List.of(depot("WIN_0")),
                        List.of(new RequestInput(
                                "O1",
                                null,
                                null,
                                List.of(item("I1", "1", "0.1", 1, 0L)),
                                null,
                                null)),
                        List.of(vehicle("V1", null, null, null, null, null, null)),
                        defaults())));
    }

    @Test
    void depotResolution() {
        PlanInput roundtrip = plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicle("V1", null, null, null, "WIN_0", null, null)),
                new OptionsInput("roundtrip", null, null, null, null));
        Vehicle folded = normalizer.normalize(roundtrip).vehicles().getFirst();
        assertEquals(new LocationId("WIN_0"), folded.startDepot().orElseThrow());
        assertEquals(new LocationId("WIN_0"), folded.endDepot().orElseThrow());

        PlanInput onewayExplicitEnd = plan(
                List.of(depot("WIN_0"), depot("WIN_1", "37.5", "127.5")),
                List.of(),
                List.of(vehicle("V1", null, null, null, "WIN_0", "WIN_1", null)),
                new OptionsInput("oneway", null, null, null, null));
        assertEquals(
                new LocationId("WIN_1"),
                normalizer.normalize(onewayExplicitEnd).vehicles().getFirst().endDepot().orElseThrow());

        assertTrue(normalizer.normalize(minimalPlan()).vehicles().getFirst().startDepot().isEmpty());
        assertTrue(normalizer
                .normalize(plan(
                        List.of(depot("WIN_0"), depot("WIN_1", "37.5", "127.5")),
                        List.of(),
                        List.of(vehicle("V1", null, null, null, null, null, null)),
                        defaults()))
                .vehicles()
                .getFirst()
                .startDepot()
                .isEmpty());

        PlanInput roundtripNoAnchors = plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                new OptionsInput("roundtrip", null, null, null, null));
        assertEquals(InputException.Kind.INVALID_INPUT, kindOf(roundtripNoAnchors));

        assertEquals(
                InputException.Kind.INVALID_INPUT,
                kindOf(plan(
                        List.of(depot("WIN_0")),
                        List.of(),
                        List.of(vehicle("V1", null, null, null, "MISSING", null, null)),
                        defaults())));
    }

    @Test
    void floorFixtureWindowsStayOneEach() {
        PlanInput input = plan(
                List.of(new DepotInput("WIN_0", "37.0", "127.0", LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59), null)),
                List.of(deliveryOnly(
                        "O1",
                        new SideInput(
                                "C1",
                                "37.1",
                                "127.1",
                                LocalTime.of(5, 45, 0),
                                LocalTime.of(10, 30, 0),
                                null,
                                null,
                                null))),
                List.of(new VehicleInput(
                        "V1",
                        null,
                        new BigDecimal("1000"),
                        new BigDecimal("5.95"),
                        LocalTime.of(0, 0, 0),
                        LocalTime.of(23, 30, 0),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)),
                defaults());

        Plan plan = normalizer.normalize(input);
        assertEquals(List.of(new TimeWindow(0, 84_600)), plan.vehicles().getFirst().workWindows());
        assertEquals(List.of(new TimeWindow(0, 86_399)), plan.depots().getFirst().windows());
        assertEquals(1, plan.requests().getFirst().delivery().orElseThrow().windows().size());
        assertEquals(
                List.of(new TimeWindow(20_700, 37_800)),
                plan.requests().getFirst().delivery().orElseThrow().windows());
    }

    private InputException.Kind kindOf(PlanInput input) {
        return failure(input).kind();
    }

    private InputException failure(PlanInput input) {
        return assertThrows(InputException.class, () -> normalizer.normalize(input));
    }

    static SideInput zonedSide(String locId, String lat, String lon, String zoneId) {
        return new SideInput(locId, lat, lon, null, null, null, null, zoneId);
    }

    static VehicleInput vehicleWithZones(String vehicleId, Set<String> zoneIds) {
        return vehicleWith(vehicleId, null, zoneIds);
    }

    static VehicleInput vehicleWithCapabilities(String vehicleId, Set<String> capabilities) {
        return vehicleWith(vehicleId, capabilities, null);
    }

    static VehicleInput vehicleWith(String vehicleId, Set<String> capabilities, Set<String> zoneIds) {
        return new VehicleInput(
                vehicleId,
                null,
                new BigDecimal("1000"),
                new BigDecimal("5.95"),
                null,
                null,
                null,
                null,
                null,
                null,
                capabilities,
                zoneIds,
                null,
                null);
    }

    /** null 원소는 `Set.of`로 만들 수 없다 — 실제 wire가 보낼 수 있는 형태를 재현한다. */
    static Set<String> setWithNull(String present) {
        return new HashSet<>(Arrays.asList(present, null));
    }

    static PlanInput minimalPlan() {
        return plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                defaults());
    }

    static PlanInput planWithMultiRotation(int multiRotation) {
        return plan(
                List.of(depot("WIN_0")),
                List.of(),
                List.of(vehicle("V1", null, null, null, null, null, null)),
                new OptionsInput(null, multiRotation, null, null, null));
    }

    static PlanInput plan(
            List<DepotInput> depots,
            List<RequestInput> requests,
            List<VehicleInput> vehicles,
            OptionsInput options) {
        return new PlanInput("P1", null, PLAN_START, PLAN_END, depots, requests, vehicles, List.of(), options);
    }

    static OptionsInput defaults() {
        return new OptionsInput(null, null, null, null, null);
    }

    static DepotInput depot(String locId) {
        return depot(locId, "37.0", "127.0");
    }

    static DepotInput depot(String locId, String lat, String lon) {
        return new DepotInput(locId, lat, lon, null, null, null);
    }

    static RequestInput deliveryOnly(String orderId, SideInput delivery) {
        return new RequestInput(orderId, null, delivery, List.of(item("I1", "1", "0.1", 1, 0L)), null, null);
    }

    static RequestInput pickupOnly(String orderId, SideInput pickup) {
        return new RequestInput(orderId, pickup, null, List.of(item("I1", "1", "0.1", 1, 0L)), null, null);
    }

    static SideInput side(String locId, LocalTime open, LocalTime close, Long duration, LocalDateTime reqDate) {
        return side(locId, "37.1", "127.1", open, close, duration, reqDate);
    }

    static SideInput side(
            String locId,
            String lat,
            String lon,
            LocalTime open,
            LocalTime close,
            Long duration,
            LocalDateTime reqDate) {
        return new SideInput(locId, lat, lon, open, close, duration, reqDate, null);
    }

    static ItemInput item(String id, String weight, String volume, Integer qty, Long taskTime) {
        return new ItemInput(id, new BigDecimal(weight), new BigDecimal(volume), qty, taskTime);
    }

    static VehicleInput vehicle(
            String vehicleId,
            String feature,
            Integer maxStopCnt,
            Long maxDriveTimeSec,
            String startDepotLocId,
            String endDepotLocId,
            Integer speedKmH) {
        return new VehicleInput(
                vehicleId,
                feature,
                new BigDecimal("1000"),
                new BigDecimal("5.95"),
                null,
                null,
                speedKmH,
                maxStopCnt,
                maxDriveTimeSec,
                null,
                null,
                null,
                startDepotLocId,
                endDepotLocId);
    }
}
