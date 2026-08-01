package com.ronext.rpdptw.input;

import java.util.Objects;

public record AdapterIdentity(String value) {
    public AdapterIdentity {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("adapter identity must not be empty");
        }
    }
}
