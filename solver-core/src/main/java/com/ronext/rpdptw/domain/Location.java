package com.ronext.rpdptw.domain;

import java.util.Objects;

public record Location(LocationId id, double latitude, double longitude) {

    public Location {
        Objects.requireNonNull(id, "id");
    }
}
