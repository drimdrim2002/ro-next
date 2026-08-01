package com.ronext.rpdptw.input;

import java.util.Objects;

public record ExternalPlanId(String value) {
    public ExternalPlanId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
