package com.ronext.rpdptw.domain;

import java.util.Objects;

public record NodeId(String value) {

    public NodeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value");
        }
    }

    public static NodeId pickup(RequestId id) {
        return new NodeId(Objects.requireNonNull(id, "id").value() + ":P");
    }

    public static NodeId delivery(RequestId id) {
        return new NodeId(Objects.requireNonNull(id, "id").value() + ":D");
    }
}
