package com.ronext.rpdptw.normalization;

import java.util.Objects;

public record InputPath(String dotted) {
    public InputPath {
        Objects.requireNonNull(dotted, "dotted must not be null");
    }
}
