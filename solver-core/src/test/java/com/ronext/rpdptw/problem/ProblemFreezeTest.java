package com.ronext.rpdptw.problem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Compatibility;
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
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.TravelMatrix;
import com.ronext.rpdptw.domain.Trips;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;

class ProblemFreezeTest {

    private static final LocationId DEPOT_ID = new LocationId("DEPOT");
    private static final LocationId C1 = new LocationId("C1");
    private static final LocationId C2 = new LocationId("C2");
    private static final TimeBase TIME_BASE = new TimeBase(LocalDateTime.of(2023, 9, 13, 0, 0));

    /**
     * 원본 Plan 컬렉션 변조 검사는 두지 않는다 — Plan의 canonical 생성자가 이미
     * List.copyOf·unmodifiableMap으로 스냅샷을 떠서, freeze가 방어 복사를 하든 안 하든
     * 항상 통과하는 단언이 되기 때문이다 (§7 T8, 2026-08-17).
     */
    @Test
    void immutableAfterFreeze() {
        Problem problem = Problem.freeze(plan(
                List.of(depot(DEPOT_ID)),
                List.of(deliveryOnly("R1", C1)),
                List.of(vehicle("V1", Optional.of("T1"), OptionalInt.of(45))),
                locations(DEPOT_ID, C1),
                List.of(),
                OptionalInt.of(45)));

        assertEquals(1, problem.requests().size());
        assertEquals(1, problem.vehicles().size());
        assertEquals(1, problem.depots().size());
        assertEquals(2, problem.locations().size());

        assertThrows(UnsupportedOperationException.class, () -> problem.requests().add(deliveryOnly("R2", C1)));
        assertThrows(UnsupportedOperationException.class, () -> problem.vehicles().clear());
        assertThrows(UnsupportedOperationException.class, () -> problem.depots().remove(0));
        assertThrows(UnsupportedOperationException.class, () -> problem.locations().put(C2, loc(C2, 38.0, 127.0)));
        assertThrows(
                UnsupportedOperationException.class,
                () -> problem.compatibleVehicles(new RequestId("R1")).add(new VehicleId("V9")));

        assertNoMutators(Problem.class);
        assertNoMutators(TravelMatrix.class);
    }

    @Test
    void validatesReferencesAndPairs() {
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1)),
                List.of(vehicle("V1", Optional.of("T1"), OptionalInt.of(0))),
                OptionalInt.of(45))));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1)),
                List.of(vehicle("V1", Optional.of("T1"), OptionalInt.of(-1))),
                OptionalInt.of(45))));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1)),
                List.of(vehicle("V1", Optional.of("T1"), OptionalInt.empty())),
                OptionalInt.of(0))));

        Request deliveryWithPickup = request(
                "R1",
                ServicePattern.DELIVERY_ONLY,
                Optional.of(pickupSide("R1", C2)),
                Optional.of(deliverySide("R1", C1)));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryWithPickup),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        Request pickupWithDelivery = request(
                "R1",
                ServicePattern.PICKUP_ONLY,
                Optional.of(pickupSide("R1", C1)),
                Optional.of(deliverySide("R1", C2)));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(pickupWithDelivery),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        Request pdMissingDelivery = request(
                "R1",
                ServicePattern.PICKUP_DELIVERY,
                Optional.of(pickupSide("R1", C1)),
                Optional.empty());
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(pdMissingDelivery),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        Request pdMissingPickup = request(
                "R1",
                ServicePattern.PICKUP_DELIVERY,
                Optional.empty(),
                Optional.of(deliverySide("R1", C1)));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(pdMissingPickup),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        Request bothMissing = request("R1", ServicePattern.PICKUP_DELIVERY, Optional.empty(), Optional.empty());
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(bothMissing),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        Vehicle unknownStart = vehicle(
                "V1",
                Optional.empty(),
                OptionalInt.of(45),
                Optional.of(C1),
                Optional.empty());
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1)),
                List.of(unknownStart),
                OptionalInt.of(45))));

        Vehicle unknownEnd = vehicle(
                "V1",
                Optional.empty(),
                OptionalInt.of(45),
                Optional.of(DEPOT_ID),
                Optional.of(C1));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1)),
                List.of(unknownEnd),
                OptionalInt.of(45))));

        Request dupA = deliveryOnly("R1", C1);
        Request dupB = request(
                "R1",
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(new RequestSide(
                        new NodeId("R1-dup:D"),
                        C1,
                        List.of(new TimeWindow(0, 86_399)),
                        0L,
                        0L,
                        86_400L,
                        Optional.empty())));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(dupA, dupB),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1)),
                List.of(
                        vehicle("V1", Optional.empty(), OptionalInt.of(45)),
                        vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        Request nodeClash = request(
                "R2",
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(new RequestSide(
                        NodeId.delivery(new RequestId("R1")),
                        C2,
                        List.of(new TimeWindow(0, 86_399)),
                        0L,
                        0L,
                        86_400L,
                        Optional.empty())));
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(basePlan(
                List.of(deliveryOnly("R1", C1), nodeClash),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                OptionalInt.of(45))));

        // E21 — 같은 locationId의 차고 둘. 창이 달라 뒤엣것이 앞엣것을 덮으면 조용한 오답이 된다.
        Depot shadowing = new Depot(DEPOT_ID, List.of(new TimeWindow(3_600, 7_200)), Optional.empty());
        assertThrows(ProblemCreationException.class, () -> Problem.freeze(plan(
                List.of(depot(DEPOT_ID), shadowing),
                List.of(deliveryOnly("R1", C1)),
                List.of(vehicle("V1", Optional.empty(), OptionalInt.of(45))),
                locations(DEPOT_ID, C1, C2),
                List.of(),
                OptionalInt.of(45))));
    }

    @Test
    void freezesCompatibilityFacts() {
        Request t1Only = request(
                "R-T1",
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(deliverySide("R-T1", C1)),
                Optional.of(Set.of("T1")),
                Set.of());
        Request noCompatible = request(
                "R-NONE",
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(deliverySide("R-NONE", C2)),
                Optional.of(Set.of("TX")),
                Set.of());
        Vehicle t1 = vehicle("V-T1", Optional.of("T1"), OptionalInt.of(45));
        Vehicle t2 = vehicle("V-T2", Optional.of("T2"), OptionalInt.of(45));
        Vehicle noStart = vehicle(
                "V-NS",
                Optional.of("T1"),
                OptionalInt.of(45),
                Optional.empty(),
                Optional.empty());

        Problem problem = Problem.freeze(basePlan(
                List.of(t1Only, noCompatible),
                List.of(t1, t2, noStart),
                OptionalInt.of(45)));

        assertEquals(Set.of(), problem.compatibleVehicles(noCompatible.id()));
        assertEquals(Set.of(t1.id()), problem.compatibleVehicles(t1Only.id()));

        for (Request request : problem.requests()) {
            Set<VehicleId> expected = new java.util.LinkedHashSet<>();
            for (Vehicle vehicle : problem.vehicles()) {
                if (Compatibility.compatible(vehicle, request)) {
                    expected.add(vehicle.id());
                }
            }
            assertEquals(expected, problem.compatibleVehicles(request.id()));
        }
    }

    @Test
    void travelCompleteAfterFreeze() {
        Vehicle withSpeed = vehicle("V45", Optional.empty(), OptionalInt.of(45));
        Vehicle usesDefault = vehicle("VDEF", Optional.empty(), OptionalInt.empty());
        Vehicle usesFallback = vehicle("VFB", Optional.empty(), OptionalInt.empty());
        Plan plan = plan(
                List.of(depot(DEPOT_ID)),
                List.of(deliveryOnly("R1", C1), deliveryOnly("R2", C2)),
                List.of(withSpeed, usesDefault, usesFallback),
                locations(DEPOT_ID, C1, C2),
                List.of(new TravelEntry(DEPOT_ID, C1, 500, 40)),
                OptionalInt.of(45));
        // fallback 체인을 보기 위해 defaultSpeed가 비어 있는 별도 Plan
        Plan fallbackPlan = plan(
                List.of(depot(DEPOT_ID)),
                List.of(deliveryOnly("R1", C1)),
                List.of(usesFallback),
                locations(DEPOT_ID, C1),
                List.of(),
                OptionalInt.empty());

        Problem problem = Problem.freeze(plan);
        assertEquals(45, problem.resolvedSpeedKmH(withSpeed.id()));
        assertEquals(45, problem.resolvedSpeedKmH(usesDefault.id()));
        assertEquals(45, Problem.freeze(fallbackPlan).resolvedSpeedKmH(usesFallback.id()));

        Set<LocationId> ids = problem.locations().keySet();
        for (LocationId from : ids) {
            for (LocationId to : ids) {
                problem.travel().distanceMeter(from, to);
                problem.travel().timeSec(from, to, problem.resolvedSpeedKmH(withSpeed.id()));
            }
        }
        assertEquals(ids.size() * ids.size(), 9);
        assertEquals(500, problem.travel().distanceMeter(DEPOT_ID, C1));
        assertEquals(40, problem.travel().timeSec(DEPOT_ID, C1, 45));
        assertTrue(problem.travel().locationIds().containsAll(ids));
    }

    private static void assertNoMutators(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            String name = method.getName();
            assertTrue(
                    !name.startsWith("set")
                            && !name.startsWith("add")
                            && !name.startsWith("remove")
                            && !name.startsWith("put"),
                    type.getSimpleName() + " mutator: " + name);
        }
    }

    private static Plan basePlan(List<Request> requests, List<Vehicle> vehicles, OptionalInt defaultSpeed) {
        return plan(
                List.of(depot(DEPOT_ID)),
                requests,
                vehicles,
                locations(DEPOT_ID, C1, C2),
                List.of(),
                defaultSpeed);
    }

    private static Plan plan(
            List<Depot> depots,
            List<Request> requests,
            List<Vehicle> vehicles,
            Map<LocationId, Location> locations,
            List<TravelEntry> travelEntries,
            OptionalInt defaultSpeed) {
        return new Plan(
                "P1",
                Optional.of("cust"),
                TIME_BASE,
                86_400L,
                depots,
                requests,
                vehicles,
                locations,
                travelEntries,
                new DeliveryPolicy(Trips.ONEWAY, false, defaultSpeed));
    }

    private static Depot depot(LocationId id) {
        return new Depot(id, List.of(new TimeWindow(0, 86_399)), Optional.empty());
    }

    private static Request deliveryOnly(String id, LocationId locationId) {
        return request(id, ServicePattern.DELIVERY_ONLY, Optional.empty(), Optional.of(deliverySide(id, locationId)));
    }

    private static Request request(
            String id, ServicePattern pattern, Optional<RequestSide> pickup, Optional<RequestSide> delivery) {
        return request(id, pattern, pickup, delivery, Optional.empty(), Set.of());
    }

    private static Request request(
            String id,
            ServicePattern pattern,
            Optional<RequestSide> pickup,
            Optional<RequestSide> delivery,
            Optional<Set<String>> features,
            Set<String> requiredCapabilities) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                pattern,
                pickup,
                delivery,
                List.of(new Item("I1", 1_000L, 1_000L, 1, 0L)),
                1_000L,
                1_000L,
                features,
                requiredCapabilities);
    }

    private static RequestSide deliverySide(String requestId, LocationId locationId) {
        return new RequestSide(
                NodeId.delivery(new RequestId(requestId)),
                locationId,
                List.of(new TimeWindow(0, 86_399)),
                0L,
                0L,
                86_400L,
                Optional.empty());
    }

    private static RequestSide pickupSide(String requestId, LocationId locationId) {
        return new RequestSide(
                NodeId.pickup(new RequestId(requestId)),
                locationId,
                List.of(new TimeWindow(0, 86_399)),
                0L,
                0L,
                86_400L,
                Optional.empty());
    }

    private static Vehicle vehicle(String id, Optional<String> feature, OptionalInt speedKmH) {
        return vehicle(id, feature, speedKmH, Optional.of(DEPOT_ID), Optional.empty());
    }

    private static Vehicle vehicle(
            String id,
            Optional<String> feature,
            OptionalInt speedKmH,
            Optional<LocationId> startDepot,
            Optional<LocationId> endDepot) {
        return new Vehicle(
                new VehicleId(id),
                feature,
                1_000_000L,
                1_000_000L,
                List.of(new TimeWindow(0, 86_399)),
                speedKmH,
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Set.of(),
                Optional.empty(),
                startDepot,
                endDepot);
    }

    private static Location loc(LocationId id, double lat, double lon) {
        return new Location(id, lat, lon);
    }

    private static Map<LocationId, Location> locations(LocationId... ids) {
        Map<LocationId, Location> map = new LinkedHashMap<>();
        double lat = 37.0;
        for (LocationId id : ids) {
            map.put(id, loc(id, lat, 127.0));
            lat += 0.1;
        }
        return map;
    }
}
