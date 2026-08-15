package com.ronext.rpdptw.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CompatibilityTest {

    @Test
    void axisTruthTable() {
        Vehicle wildcardVehicle = vehicle(Optional.empty(), Set.of(), Optional.empty());
        Vehicle t1 = vehicle(Optional.of("T1"), Set.of(), Optional.empty());
        Vehicle t2 = vehicle(Optional.of("T2"), Set.of(), Optional.empty());

        Request anySize = request(Optional.empty(), Set.of());
        Request t1Only = request(Optional.of(Set.of("T1")), Set.of());
        Request t1AndT2 = request(Optional.of(Set.of("T1", "T2")), Set.of());
        Request t2Only = request(Optional.of(Set.of("T2")), Set.of());

        assertTrue(Compatibility.size(wildcardVehicle, t1Only));
        assertTrue(Compatibility.size(t1, anySize));
        assertTrue(Compatibility.size(t1, t1Only));
        assertTrue(Compatibility.size(t2, t1AndT2));
        assertFalse(Compatibility.size(t1, t2Only));

        assertTrue(Compatibility.capability(t1, anySize));
        assertTrue(Compatibility.capability(vehicle(Optional.of("T1"), Set.of(), Optional.empty()), anySize));

        RequestSide zoned = side(Optional.of("ZONE-A"));
        assertTrue(Compatibility.zone(t1, zoned));
        assertTrue(Compatibility.zone(t1, side(Optional.empty())));

        assertTrue(Compatibility.compatible(t1, t1Only));
        assertFalse(Compatibility.compatible(t1, t2Only));

        Vehicle noDepot = vehicle(Optional.of("T1"), Set.of(), Optional.empty(), Optional.empty(), Optional.empty());
        Vehicle startOnly = vehicle(
                Optional.of("T1"), Set.of(), Optional.empty(), Optional.of(new LocationId("DEPOT")), Optional.empty());
        Vehicle endOnly = vehicle(
                Optional.of("T1"), Set.of(), Optional.empty(), Optional.empty(), Optional.of(new LocationId("DEPOT")));
        Request deliveryOnly = request(ServicePattern.DELIVERY_ONLY, Optional.empty(), Optional.of(side(Optional.empty())));
        Request pickupOnly = request(ServicePattern.PICKUP_ONLY, Optional.of(side(Optional.empty())), Optional.empty());
        Request bothSides = request(
                ServicePattern.PICKUP_DELIVERY,
                Optional.of(side(Optional.empty())),
                Optional.of(side(Optional.empty())));

        assertTrue(Compatibility.depotAnchors(startOnly, deliveryOnly));
        assertTrue(Compatibility.compatible(startOnly, deliveryOnly));
        assertFalse(Compatibility.depotAnchors(noDepot, deliveryOnly));
        assertTrue(Compatibility.depotAnchors(endOnly, pickupOnly));
        assertTrue(Compatibility.compatible(endOnly, pickupOnly));
        assertFalse(Compatibility.depotAnchors(noDepot, pickupOnly));
        assertTrue(Compatibility.depotAnchors(noDepot, bothSides));
        assertFalse(Compatibility.compatible(noDepot, deliveryOnly));
        assertFalse(Compatibility.compatible(noDepot, pickupOnly));
        assertTrue(Compatibility.compatible(noDepot, bothSides));

        assertFalse(Compatibility.depotAnchors(startOnly, pickupOnly));
        assertFalse(Compatibility.compatible(startOnly, pickupOnly));
        assertFalse(Compatibility.depotAnchors(endOnly, deliveryOnly));
        assertFalse(Compatibility.compatible(endOnly, deliveryOnly));
    }

    private static Vehicle vehicle(
            Optional<String> feature, Set<String> capabilities, Optional<Set<String>> zoneIds) {
        return vehicle(feature, capabilities, zoneIds, Optional.of(new LocationId("DEPOT")), Optional.empty());
    }

    private static Vehicle vehicle(
            Optional<String> feature,
            Set<String> capabilities,
            Optional<Set<String>> zoneIds,
            Optional<LocationId> startDepot,
            Optional<LocationId> endDepot) {
        return new Vehicle(
                new VehicleId("V1"),
                feature,
                1_000_000L,
                1_000_000L,
                List.of(new TimeWindow(0, 86_399)),
                OptionalInt.empty(),
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                capabilities,
                zoneIds,
                startDepot,
                endDepot);
    }

    private static Request request(Optional<Set<String>> features, Set<String> requiredCapabilities) {
        return request(
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(side(Optional.empty())),
                features,
                requiredCapabilities);
    }

    private static Request request(
            ServicePattern pattern, Optional<RequestSide> pickup, Optional<RequestSide> delivery) {
        return request(pattern, pickup, delivery, Optional.empty(), Set.of());
    }

    private static Request request(
            ServicePattern pattern,
            Optional<RequestSide> pickup,
            Optional<RequestSide> delivery,
            Optional<Set<String>> features,
            Set<String> requiredCapabilities) {
        RequestId id = new RequestId("R1");
        return new Request(
                id,
                pattern,
                pickup,
                delivery,
                List.of(new Item("I1", 1_000L, 1_000L, 1, 0L)),
                1_000L,
                1_000L,
                features,
                requiredCapabilities);
    }

    private static RequestSide side(Optional<String> zoneId) {
        return new RequestSide(
                NodeId.delivery(new RequestId("R1")),
                new LocationId("C1"),
                List.of(new TimeWindow(0, 86_399)),
                0L,
                0L,
                86_400L,
                zoneId);
    }
}
