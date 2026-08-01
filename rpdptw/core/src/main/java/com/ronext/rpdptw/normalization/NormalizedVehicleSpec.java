package com.ronext.rpdptw.normalization;

import java.util.Objects;
import java.util.Set;

public record NormalizedVehicleSpec(
    SizeFeatureCode sizeCode,
    Set<CapabilityCode> capabilities,
    VehicleZoneSet zoneSet
) {
    public NormalizedVehicleSpec {
        Objects.requireNonNull(sizeCode, "sizeCode must not be null");
        Objects.requireNonNull(capabilities, "capabilities must not be null");
        Objects.requireNonNull(zoneSet, "zoneSet must not be null");
        capabilities = Set.copyOf(capabilities);
    }
}
