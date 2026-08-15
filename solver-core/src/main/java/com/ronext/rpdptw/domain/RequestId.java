package com.ronext.rpdptw.domain;

public record RequestId(String value) {

    public RequestId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value");
        }
    }
}
