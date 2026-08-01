package com.ronext.rpdptw.input;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record CanonicalCompatibilityInput(
    List<String> allowedVehicleSizes,
    Set<String> requiredVehicleCapabilities
) {
    public CanonicalCompatibilityInput {
        Objects.requireNonNull(allowedVehicleSizes, "allowedVehicleSizes must not be null");
        Objects.requireNonNull(requiredVehicleCapabilities, "requiredVehicleCapabilities must not be null");
        allowedVehicleSizes = List.copyOf(allowedVehicleSizes);
        requiredVehicleCapabilities = Set.copyOf(requiredVehicleCapabilities);
    }
}
