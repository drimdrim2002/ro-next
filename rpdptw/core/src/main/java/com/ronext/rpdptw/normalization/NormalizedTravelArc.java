package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalLocationId;

import java.util.Objects;

public record NormalizedTravelArc(
        ExternalLocationId from,
        ExternalLocationId to,
        Seconds duration,
        Meters distance
) {
    public NormalizedTravelArc {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(distance, "distance must not be null");
    }
}
