package com.ronext.rpdptw.normalization;

import java.util.Objects;

public record SizeFeatureCode(String value) {
    public SizeFeatureCode {
        Objects.requireNonNull(value, "value must not be null");
    }
}
