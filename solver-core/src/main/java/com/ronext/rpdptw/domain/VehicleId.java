package com.ronext.rpdptw.domain;

public record VehicleId(String value) {

    public VehicleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value");
        }
    }
}
