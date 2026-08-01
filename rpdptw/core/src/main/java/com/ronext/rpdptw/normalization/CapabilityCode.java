package com.ronext.rpdptw.normalization;

import java.util.Objects;

public record CapabilityCode(String value) {
    public CapabilityCode {
        Objects.requireNonNull(value, "value must not be null");
    }
}
