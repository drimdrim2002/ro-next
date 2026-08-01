package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalRequestId;

import java.util.Objects;
import java.util.Set;

public record NormalizedRequestSpec(
    ExternalRequestId requestId,
    AllowedVehicleSizes allowedSizes,
    Set<CapabilityCode> requiredCapabilities,
    Set<ZoneCode> requestZones
) {
    public NormalizedRequestSpec {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(allowedSizes, "allowedSizes must not be null");
        Objects.requireNonNull(requiredCapabilities, "requiredCapabilities must not be null");
        Objects.requireNonNull(requestZones, "requestZones must not be null");
        requiredCapabilities = Set.copyOf(requiredCapabilities);
        requestZones = Set.copyOf(requestZones);
    }
}
