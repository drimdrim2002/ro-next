package com.ronext.rpdptw.domain;

public record Item(
        String itemId, long weight, long volume, int qty, long taskTimeSec) {

    public Item {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId");
        }
    }
}
