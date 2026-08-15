package com.ronext.rpdptw.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Depot(LocationId locationId, List<TimeWindow> windows, Optional<String> zoneId) {

    public Depot {
        Objects.requireNonNull(locationId, "locationId");
        windows = List.copyOf(windows);
        zoneId = Objects.requireNonNull(zoneId, "zoneId");
    }
}
