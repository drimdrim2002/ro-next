package com.ronext.rpdptw.normalization;

public record MilliCubicMeters(long value) {
    public MilliCubicMeters {
        if (value < 0) {
            throw new IllegalArgumentException("volume milli must be non-negative: " + value);
        }
    }
}
