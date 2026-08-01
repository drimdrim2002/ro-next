package com.ronext.rpdptw.input;

import java.util.Objects;

public record ExternalLocationId(String value) {
    public ExternalLocationId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
