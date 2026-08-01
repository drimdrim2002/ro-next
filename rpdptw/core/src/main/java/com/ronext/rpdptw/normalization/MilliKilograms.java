package com.ronext.rpdptw.normalization;

public record MilliKilograms(long value) {
    public MilliKilograms {
        if (value < 0) {
            throw new IllegalArgumentException("weight milli must be non-negative: " + value);
        }
    }
}
