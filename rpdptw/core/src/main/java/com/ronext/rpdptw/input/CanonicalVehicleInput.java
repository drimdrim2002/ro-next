package com.ronext.rpdptw.input;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record CanonicalVehicleInput(
    ExternalVehicleId id,
    String sizeFeatureCode,
    Set<String> capabilities,
    Set<String> vehicleZoneIds,
    Optional<String> ownership,
    Optional<String> speedKmH,
    Boolean oneway,
    Boolean singleRoundtrip,
    Optional<String> waitPolicy,
    Optional<String> routeResourceLimit
) {
    public CanonicalVehicleInput {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(sizeFeatureCode, "sizeFeatureCode must not be null");
        Objects.requireNonNull(capabilities, "capabilities must not be null");
        Objects.requireNonNull(vehicleZoneIds, "vehicleZoneIds must not be null");
        Objects.requireNonNull(ownership, "ownership must not be null");
        Objects.requireNonNull(speedKmH, "speedKmH must not be null");
        Objects.requireNonNull(oneway, "oneway must not be null");
        Objects.requireNonNull(singleRoundtrip, "singleRoundtrip must not be null");
        Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
        Objects.requireNonNull(routeResourceLimit, "routeResourceLimit must not be null");

        capabilities = Set.copyOf(capabilities);
        vehicleZoneIds = Set.copyOf(vehicleZoneIds);
    }
}
