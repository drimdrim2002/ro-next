package com.ronext.rpdptw.input;

import java.util.Objects;

public record ExternalVehicleId(String value) {
    public ExternalVehicleId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
