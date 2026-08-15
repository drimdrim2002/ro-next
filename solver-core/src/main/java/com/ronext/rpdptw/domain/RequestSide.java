package com.ronext.rpdptw.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RequestSide(
        NodeId nodeId,
        LocationId locationId,
        List<TimeWindow> windows,
        long durationSec,
        long serviceTimeSec,
        long reqDateSec,
        Optional<String> zoneId) {

    public RequestSide {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(locationId, "locationId");
        windows = List.copyOf(windows);
        zoneId = Objects.requireNonNull(zoneId, "zoneId");
    }
}
