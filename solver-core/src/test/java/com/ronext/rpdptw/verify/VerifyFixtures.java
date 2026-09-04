package com.ronext.rpdptw.verify;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Item;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Plan;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Trips;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.TimeFixtures;

/**
 * verify 테스트 전용 Problem 손 조립 (stage-05 §8 — fixture JSON 파싱은 Stage 6).
 * solve 테스트의 fixture는 package-private이라 쓸 수 없어 같은 모양을 여기서 다시 세운다.
 */
final class VerifyFixtures {

    static final LocationId DEPOT = new LocationId("DEPOT");
    static final LocationId END_DEPOT = new LocationId("END_DEPOT");
    static final LocationId BUNDANG = new LocationId("BUNDANG");
    static final LocationId GANGNAM100 = new LocationId("GANGNAM100");
    static final LocationId GANGNAM200 = new LocationId("GANGNAM200");
    static final LocationId FAR = new LocationId("FAR");

    static final RequestId R1 = new RequestId("R1");
    static final RequestId R2 = new RequestId("R2");

    static final VehicleId V1 = new VehicleId("V1");
    static final VehicleId V2 = new VehicleId("V2");

    static final TimeWindow WORK_08_18 = new TimeWindow(28_800L, 64_800L);
    static final TimeWindow ALL_DAY = new TimeWindow(0L, 86_399L);
    static final List<TimeWindow> THREE_DAY_WORK = List.of(
            new TimeWindow(28_800L, 61_200L),
            new TimeWindow(115_200L, 147_600L),
            new TimeWindow(201_600L, 234_000L));

    private static final long DAY = 86_400L;

    private VerifyFixtures() {}

    // --- Domain §7.2 예제를 pair 완비로 확장한 하루짜리 문제 (stage-03 §3.4 + R2 배송) ---

    static Problem baseProblem() {
        return baseProblem(30_000L, 1_000_000L);
    }

    static Problem baseProblem(long maxWeight, long maxVolume) {
        return baseProblem(maxWeight, maxVolume, Optional.empty(), Optional.empty());
    }

    /** waitInDepot=Y — 출발만 늦춰지고 serviceStart 시각들은 그대로다 (E9). */
    static Problem waitInDepotProblem() {
        return base(30_000L, 1_000_000L, Optional.empty(), Optional.empty(), true,
                List.of(new TimeWindow(46_800L, 64_800L)), 54_000L);
    }

    /** serviceStart == 창 close == reqDate 경계 (E8) — 양끝 포함이라 통과해야 한다. */
    static Problem boundaryProblem() {
        return base(30_000L, 1_000_000L, Optional.empty(), Optional.empty(), false,
                List.of(new TimeWindow(46_800L, 46_800L)), 46_800L);
    }

    /** feature를 주면 R1이 그 차급만 허용한다 — 비호환 배정 테스트용. */
    static Problem baseProblem(
            long maxWeight, long maxVolume, Optional<String> allowedFeature, Optional<String> v1Feature) {
        return base(maxWeight, maxVolume, allowedFeature, v1Feature, false,
                List.of(new TimeWindow(46_800L, 64_800L)), 54_000L);
    }

    private static Problem base(
            long maxWeight,
            long maxVolume,
            Optional<String> allowedFeature,
            Optional<String> v1Feature,
            boolean waitInDepot,
            List<TimeWindow> r1Windows,
            long r1ReqDateSec) {
        Request r1 = deliveryOnly("R1", GANGNAM100, r1Windows,
                840L, r1ReqDateSec, 10_000L, 5_000L, Optional.empty(), allowedFeature.map(Set::of));
        Request r2 = pickupDelivery("R2", BUNDANG, GANGNAM200,
                List.of(new TimeWindow(32_400L, 43_200L)), 300L, 36_000L,
                List.of(new TimeWindow(46_800L, 64_800L)), 300L, 54_000L,
                10_000L, 5_000L);
        List<Vehicle> vehicles = List.of(
                vehicle("V1", maxWeight, maxVolume, List.of(WORK_08_18),
                        Optional.of(DEPOT), Optional.empty(), v1Feature),
                vehicle("V2", maxWeight, maxVolume, List.of(WORK_08_18),
                        Optional.of(DEPOT), Optional.empty(), allowedFeature));
        return freeze(
                86_400L,
                waitInDepot,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(r1, r2),
                vehicles,
                locations(DEPOT, BUNDANG, GANGNAM100, GANGNAM200),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000),
                        new TravelEntry(GANGNAM100, GANGNAM200, 2_000, 600)));
    }

    /** [R2픽, R1배, R2배] — Domain §7.2 순서. */
    static List<NodeId> baseRoute() {
        return List.of(NodeId.pickup(R2), NodeId.delivery(R1), NodeId.delivery(R2));
    }

    // --- 다일 문제 (stage-03 §3.5) — R4 서비스가 다음 근무창으로 넘어간다 ---

    static Problem multiDayProblem() {
        Request r3 = deliveryOnly("R3", BUNDANG, List.of(new TimeWindow(28_800L, 61_200L)),
                300L, 3L * DAY - 1L, 10_000L, 0L, Optional.empty(), Optional.empty());
        Request r4 = deliveryOnly("R4", GANGNAM100, List.of(new TimeWindow(115_200L, 147_600L)),
                600L, 3L * DAY - 1L, 10_000L, 0L, Optional.empty(), Optional.empty());
        Request r5 = deliveryOnly("R5", FAR, List.of(new TimeWindow(28_800L, 234_000L)),
                300L, 3L * DAY - 1L, 10_000L, 0L, Optional.empty(), Optional.empty());
        return freeze(
                3L * DAY,
                false,
                List.of(new Depot(DEPOT, List.of(new TimeWindow(0L, 3L * DAY - 1L)), Optional.empty())),
                List.of(r3, r4, r5),
                List.of(vehicle("V1", 100_000L, 1_000_000L, THREE_DAY_WORK,
                        Optional.of(DEPOT), Optional.empty(), Optional.empty())),
                locations(DEPOT, BUNDANG, GANGNAM100, FAR),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000),
                        // 12시간 — 9시간짜리 근무창 어디에도 통째로 들어가지 않는다 (E20)
                        new TravelEntry(GANGNAM100, FAR, 500_000, 43_200)));
    }

    static List<NodeId> multiDayRoute() {
        return List.of(NodeId.delivery(new RequestId("R3")), NodeId.delivery(new RequestId("R4")));
    }

    // --- endDepot 문제 — 도착 차고 창을 좁히면 DEPOT_WINDOW (E21) ---

    static Problem endDepotProblem(List<TimeWindow> endDepotWindows) {
        Request r1 = deliveryOnly("R1", BUNDANG, List.of(new TimeWindow(28_800L, 64_800L)),
                300L, 64_800L, 10_000L, 0L, Optional.empty(), Optional.empty());
        return freeze(
                86_400L,
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty()),
                        new Depot(END_DEPOT, endDepotWindows, Optional.empty())),
                List.of(r1),
                List.of(vehicle("V1", 100_000L, 1_000_000L, List.of(WORK_08_18),
                        Optional.of(DEPOT), Optional.of(END_DEPOT), Optional.empty())),
                locations(DEPOT, END_DEPOT, BUNDANG),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(BUNDANG, END_DEPOT, 10_000, 2_400)));
    }

    // --- 구역 문제 (E25) — ZA1·ZA2는 ZONE_A, ZB는 ZONE_B, ZN은 구역 없음 ---

    static Problem zoneProblem() {
        Request za1 = zoned("ZA1", BUNDANG, "ZONE_A");
        Request za2 = zoned("ZA2", GANGNAM100, "ZONE_A");
        Request zb = zoned("ZB", GANGNAM200, "ZONE_B");
        Request zn = zoned("ZN", FAR, null);
        return freeze(
                86_400L,
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(za1, za2, zb, zn),
                List.of(vehicle("V1", 100_000L, 1_000_000L, List.of(WORK_08_18),
                        Optional.of(DEPOT), Optional.empty(), Optional.empty())),
                locations(DEPOT, BUNDANG, GANGNAM100, GANGNAM200, FAR),
                List.of());
    }

    private static Request zoned(String id, LocationId location, String zoneId) {
        return deliveryOnly(id, location, List.of(new TimeWindow(28_800L, 64_800L)),
                300L, 64_800L, 10_000L, 0L, Optional.ofNullable(zoneId), Optional.empty());
    }

    // --- 미배정 사유 4종을 각각 만드는 문제 (T10) ---

    static Problem reasonProblem() {
        Request u1 = deliveryOnly("U1", BUNDANG, List.of(WORK_08_18), 300L, 64_800L, 1_000L, 0L,
                Optional.empty(), Optional.of(Set.of("T9")));         // 호환 차량 0대
        Request u2 = deliveryOnly("U2", BUNDANG, List.of(WORK_08_18), 300L, 64_800L, 50_000L, 0L,
                Optional.empty(), Optional.empty());                  // 혼자서도 못 싣는다
        Request u3 = deliveryOnly("U3", BUNDANG, List.of(new TimeWindow(28_800L, 29_000L)),
                300L, 64_800L, 1_000L, 0L, Optional.empty(), Optional.empty());   // 창이 닫힌 뒤 도착
        Request u4 = deliveryOnly("U4", BUNDANG, List.of(WORK_08_18), 300L, 64_800L, 1_000L, 0L,
                Optional.empty(), Optional.empty());                  // 단독으로는 가능
        Request u5 = deliveryOnly("U5", FAR, List.of(WORK_08_18), 300L, 64_800L, 1_000L, 0L,
                Optional.empty(), Optional.empty());                  // 주행 거리 한도로만 불가 (E12)
        Vehicle v1 = new Vehicle(
                V1, Optional.of("T1"), 20_000L, 1_000_000L, List.of(WORK_08_18),
                OptionalInt.of(45), OptionalInt.empty(), OptionalLong.empty(), OptionalLong.of(25_000L),
                Set.of(), Optional.empty(), Optional.of(DEPOT), Optional.empty());
        return freeze(
                86_400L,
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(u1, u2, u3, u4, u5),
                List.of(v1),
                locations(DEPOT, BUNDANG, FAR),
                List.of(
                        new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                        new TravelEntry(DEPOT, FAR, 500_000, 3_600)));
    }

    // --- 조립 헬퍼 ---

    static Problem freeze(
            long planEndSec,
            boolean waitInDepot,
            List<Depot> depots,
            List<Request> requests,
            List<Vehicle> vehicles,
            Map<LocationId, Location> locations,
            List<TravelEntry> travel) {
        return Problem.freeze(new Plan(
                "P1",
                Optional.of("cust"),
                TimeFixtures.DEFAULT_TIME_BASE,
                planEndSec,
                depots,
                requests,
                vehicles,
                locations,
                travel,
                new DeliveryPolicy(Trips.ONEWAY, waitInDepot, OptionalInt.of(45))));
    }

    static Request deliveryOnly(
            String id,
            LocationId locationId,
            List<TimeWindow> windows,
            long serviceSec,
            long reqDateSec,
            long weight,
            long volume,
            Optional<String> zoneId,
            Optional<Set<String>> allowedFeatures) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(new RequestSide(
                        NodeId.delivery(requestId), locationId, windows, 0L, serviceSec, reqDateSec, zoneId)),
                List.of(new Item("I-" + id, weight, volume, 1, 0L)),
                weight,
                volume,
                allowedFeatures,
                Set.of());
    }

    static Request pickupDelivery(
            String id,
            LocationId pickupLocation,
            LocationId deliveryLocation,
            List<TimeWindow> pickupWindows,
            long pickupServiceSec,
            long pickupReqDateSec,
            List<TimeWindow> deliveryWindows,
            long deliveryServiceSec,
            long deliveryReqDateSec,
            long weight,
            long volume) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.PICKUP_DELIVERY,
                Optional.of(new RequestSide(NodeId.pickup(requestId), pickupLocation, pickupWindows,
                        0L, pickupServiceSec, pickupReqDateSec, Optional.empty())),
                Optional.of(new RequestSide(NodeId.delivery(requestId), deliveryLocation, deliveryWindows,
                        0L, deliveryServiceSec, deliveryReqDateSec, Optional.empty())),
                List.of(new Item("I-" + id, weight, volume, 1, 0L)),
                weight,
                volume,
                Optional.empty(),
                Set.of());
    }

    static Vehicle vehicle(
            String id,
            long maxWeight,
            long maxVolume,
            List<TimeWindow> workWindows,
            Optional<LocationId> startDepot,
            Optional<LocationId> endDepot,
            Optional<String> feature) {
        return new Vehicle(
                new VehicleId(id),
                feature,
                maxWeight,
                maxVolume,
                workWindows,
                OptionalInt.of(45),
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Set.of(),
                Optional.empty(),
                startDepot,
                endDepot);
    }

    static Map<LocationId, Location> locations(LocationId... ids) {
        Map<LocationId, Location> map = new LinkedHashMap<>();
        double lat = 37.0;
        for (LocationId id : ids) {
            map.put(id, new Location(id, lat, 127.0));
            lat += 0.1;
        }
        return map;
    }

    /** T8용 소형 fixture — 배송 8건 + PD 2건, 차량 2대. 이동표는 좌표 보정에 맡긴다. */
    static Problem alnsFixture() {
        double[][] deliveries = {
            {37.018492, 127.032116}, {37.038870, 127.028570}, {36.978253, 126.965984},
            {37.035967, 126.962441}, {37.028135, 126.964540}, {36.988191, 127.039495},
            {36.998079, 126.963953}, {37.028099, 126.978294},
        };
        double[][] pairs = {
            {37.015120, 127.023813, 36.969898, 126.968606},
            {36.962621, 127.034613, 36.981346, 127.004353},
        };
        Map<LocationId, Location> locations = new LinkedHashMap<>();
        locations.put(DEPOT, new Location(DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < deliveries.length; i++) {
            LocationId id = new LocationId("L" + (i + 1));
            locations.put(id, new Location(id, deliveries[i][0], deliveries[i][1]));
            requests.add(deliveryOnly("R" + (i + 1), id, List.of(ALL_DAY), 300L, 86_399L,
                    2_000L, 0L, Optional.empty(), Optional.empty()));
        }
        for (int i = 0; i < pairs.length; i++) {
            LocationId from = new LocationId("P" + (i + 1));
            LocationId to = new LocationId("Q" + (i + 1));
            locations.put(from, new Location(from, pairs[i][0], pairs[i][1]));
            locations.put(to, new Location(to, pairs[i][2], pairs[i][3]));
            requests.add(pickupDelivery("PD" + (i + 1), from, to,
                    List.of(ALL_DAY), 300L, 86_399L, List.of(ALL_DAY), 300L, 86_399L, 2_000L, 0L));
        }
        return freeze(
                86_400L,
                false,
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                requests,
                List.of(
                        vehicle("V1", 20_000L, 1_000_000L, List.of(ALL_DAY),
                                Optional.of(DEPOT), Optional.of(DEPOT), Optional.empty()),
                        vehicle("V2", 20_000L, 1_000_000L, List.of(ALL_DAY),
                                Optional.of(DEPOT), Optional.of(DEPOT), Optional.empty())),
                locations,
                List.of());
    }
}
