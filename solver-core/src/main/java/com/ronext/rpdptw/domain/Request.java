package com.ronext.rpdptw.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record Request(
        RequestId id,
        ServicePattern pattern,
        Optional<RequestSide> pickup,
        Optional<RequestSide> delivery,
        List<Item> items,
        long totalWeightMilliKg,
        long totalVolumeMilliCbm,
        Optional<Set<String>> allowedVehicleFeatures,
        Set<String> requiredCapabilities) {

    public Request {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(pattern, "pattern");
        pickup = Objects.requireNonNull(pickup, "pickup");
        delivery = Objects.requireNonNull(delivery, "delivery");
        items = List.copyOf(items);
        allowedVehicleFeatures = Objects.requireNonNull(allowedVehicleFeatures, "allowedVehicleFeatures")
                .map(Set::copyOf);
        requiredCapabilities = Set.copyOf(requiredCapabilities);
    }
}
