package com.ronext.rpdptw.domain;

import java.util.Objects;

public record TravelEntry(LocationId from, LocationId to, int distanceMeter, int timeSec) {

    public TravelEntry {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
    }
}
