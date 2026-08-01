package com.ronext.rpdptw.input;

import java.util.Objects;
import java.util.Optional;

public record CanonicalLocationInput(
    ExternalLocationId id,
    Optional<String> zone
) {
    public CanonicalLocationInput {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
    }
}
