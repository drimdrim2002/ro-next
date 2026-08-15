package com.ronext.rpdptw.domain;

public record Item(
        String itemId, long weightMilliKg, long volumeMilliCbm, int qty, long taskTimeSec) {

    public Item {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId");
        }
    }
}
