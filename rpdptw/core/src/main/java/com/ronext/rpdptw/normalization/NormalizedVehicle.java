package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalVehicleId;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record NormalizedVehicle(
        ExternalVehicleId id,
        SizeFeatureCode sizeCode,
        Set<CapabilityCode> capabilities,
        VehicleZoneSet zoneSet,
        VehicleOwnership ownership,
        VehicleSpeedInput speed,
        TripPolicy tripPolicy,
        Optional<DepotWaitPolicy> waitPolicy,
        Optional<Long> routeResourceLimit
) {
    public NormalizedVehicle {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(sizeCode, "sizeCode must not be null");
        Objects.requireNonNull(capabilities, "capabilities must not be null");
        Objects.requireNonNull(zoneSet, "zoneSet must not be null");
        Objects.requireNonNull(ownership, "ownership must not be null");
        Objects.requireNonNull(speed, "speed must not be null");
        Objects.requireNonNull(tripPolicy, "tripPolicy must not be null");
        Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
        Objects.requireNonNull(routeResourceLimit, "routeResourceLimit must not be null");
        capabilities = Set.copyOf(capabilities);
    }
}
