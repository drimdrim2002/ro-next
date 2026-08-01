package com.ronext.rpdptw.normalization;

import java.util.Objects;
import java.util.Optional;

public record NormalizedRouteResourceLimits(
        Optional<Long> maxRouteDurationSeconds,
        Optional<Long> maxRouteDistanceMeters
) {
    public NormalizedRouteResourceLimits {
        Objects.requireNonNull(maxRouteDurationSeconds, "maxRouteDurationSeconds must not be null");
        Objects.requireNonNull(maxRouteDistanceMeters, "maxRouteDistanceMeters must not be null");
    }
}
