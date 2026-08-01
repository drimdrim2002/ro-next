package com.ronext.rpdptw.input;

import java.util.Objects;

public record CanonicalTravelInput(
    ExternalLocationId from,
    ExternalLocationId to,
    String durationSeconds,
    String distanceMeters
) {
    public CanonicalTravelInput {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(durationSeconds, "durationSeconds must not be null");
        Objects.requireNonNull(distanceMeters, "distanceMeters must not be null");
    }
}
