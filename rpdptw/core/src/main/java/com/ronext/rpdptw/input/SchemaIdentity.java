package com.ronext.rpdptw.input;

import java.util.Objects;

public record SchemaIdentity(String value) {
    public SchemaIdentity {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("schema identity must not be empty");
        }
    }
}
