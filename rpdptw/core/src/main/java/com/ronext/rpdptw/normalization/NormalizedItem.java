package com.ronext.rpdptw.normalization;

import java.util.Objects;

/**
 * Sealed item facts after item-first FLOOR scale-3.
 * {@code weightMilli}/{@code volumeMilli} store the product after floor-then-×qty.
 */
public record NormalizedItem(
        MilliKilograms weightMilli,
        MilliCubicMeters volumeMilli,
        int quantity,
        Seconds itemTaskTime
) {
    public NormalizedItem {
        Objects.requireNonNull(weightMilli, "weightMilli must not be null");
        Objects.requireNonNull(volumeMilli, "volumeMilli must not be null");
        Objects.requireNonNull(itemTaskTime, "itemTaskTime must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
    }
}
