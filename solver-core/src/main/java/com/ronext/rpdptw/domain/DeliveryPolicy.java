package com.ronext.rpdptw.domain;

import java.util.Objects;
import java.util.OptionalInt;

public record DeliveryPolicy(Trips trips, boolean waitInDepot, OptionalInt defaultSpeedKmH) {

    public DeliveryPolicy {
        Objects.requireNonNull(trips, "trips");
        Objects.requireNonNull(defaultSpeedKmH, "defaultSpeedKmH");
    }
}
