package com.ronext.rpdptw.normalization;

public record NormalizedWindow(long startSecond, long endSecondExclusive) {
    public NormalizedWindow {
        if (startSecond >= endSecondExclusive) {
            throw new IllegalArgumentException("startSecond (" + startSecond + ") must be less than endSecondExclusive (" + endSecondExclusive + ")");
        }
    }
}
