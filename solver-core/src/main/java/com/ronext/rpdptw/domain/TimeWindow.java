package com.ronext.rpdptw.domain;

public record TimeWindow(long openSec, long closeSec) {

    public TimeWindow {
        if (openSec > closeSec) {
            throw new IllegalArgumentException("openSec > closeSec");
        }
    }
}
