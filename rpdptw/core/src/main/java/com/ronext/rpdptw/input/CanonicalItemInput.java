package com.ronext.rpdptw.input;

import java.util.Objects;

public record CanonicalItemInput(
    String weightDecimal,
    String volumeDecimal,
    int quantity,
    String itemTaskTimeSeconds
) {
    public CanonicalItemInput {
        Objects.requireNonNull(weightDecimal, "weightDecimal must not be null");
        Objects.requireNonNull(volumeDecimal, "volumeDecimal must not be null");
        Objects.requireNonNull(itemTaskTimeSeconds, "itemTaskTimeSeconds must not be null");
    }
}
