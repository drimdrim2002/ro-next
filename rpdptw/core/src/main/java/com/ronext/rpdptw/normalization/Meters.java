package com.ronext.rpdptw.normalization;

public record Meters(long value) {
    public Meters {
        if (value < 0) {
            throw new IllegalArgumentException("meters must be non-negative: " + value);
        }
    }
}
