package com.ronext.rpdptw.normalization;

import java.util.Objects;

public record ZoneCode(String value) implements Comparable<ZoneCode> {
    public ZoneCode {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public int compareTo(ZoneCode o) {
        return this.value.compareTo(o.value);
    }
}
