package com.ronext.rpdptw.input;

import java.util.Objects;
import java.util.Optional;

public record CanonicalServiceInput(
    ExternalLocationId locationId,
    String windowOpen,
    String windowCloseInclusive,
    String durationSeconds,
    Optional<String> reqDate,
    Optional<String> zone
) {
    public CanonicalServiceInput {
        Objects.requireNonNull(locationId, "locationId must not be null");
        Objects.requireNonNull(windowOpen, "windowOpen must not be null");
        Objects.requireNonNull(windowCloseInclusive, "windowCloseInclusive must not be null");
        Objects.requireNonNull(durationSeconds, "durationSeconds must not be null");
        Objects.requireNonNull(reqDate, "reqDate must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
    }
}
