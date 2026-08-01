package com.ronext.rpdptw.input;

import java.util.Objects;

public record ExternalRequestId(String value) {
    public ExternalRequestId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
