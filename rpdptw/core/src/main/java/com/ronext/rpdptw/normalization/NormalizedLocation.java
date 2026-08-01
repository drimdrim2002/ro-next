package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalLocationId;

import java.util.Objects;
import java.util.Optional;

public record NormalizedLocation(
        ExternalLocationId id,
        Optional<ZoneCode> zone
) {
    public NormalizedLocation {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
    }
}
