package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalLocationId;

import java.util.Objects;
import java.util.Optional;

public record NormalizedServiceVisit(
        ExternalLocationId locationId,
        NormalizedWindow window,
        Seconds serviceDuration,
        Optional<Seconds> reqDateFromPlanOrigin,
        Optional<ZoneCode> zone
) {
    public NormalizedServiceVisit {
        Objects.requireNonNull(locationId, "locationId must not be null");
        Objects.requireNonNull(window, "window must not be null");
        Objects.requireNonNull(serviceDuration, "serviceDuration must not be null");
        Objects.requireNonNull(reqDateFromPlanOrigin, "reqDateFromPlanOrigin must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
    }
}
