package com.ronext.rpdptw.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Plan(
        String planId,
        Optional<String> customerId,
        TimeBase timeBase,
        long planEndSec,
        List<Depot> depots,
        List<Request> requests,
        List<Vehicle> vehicles,
        Map<LocationId, Location> locations,
        List<TravelEntry> travelEntries,
        DeliveryPolicy deliveryPolicy) {

    public Plan {
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("planId");
        }
        customerId = Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(timeBase, "timeBase");
        depots = List.copyOf(depots);
        requests = List.copyOf(requests);
        vehicles = List.copyOf(vehicles);
        locations = Map.copyOf(locations);
        travelEntries = List.copyOf(travelEntries);
        Objects.requireNonNull(deliveryPolicy, "deliveryPolicy");
    }
}
