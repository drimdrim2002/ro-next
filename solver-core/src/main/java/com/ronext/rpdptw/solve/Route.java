package com.ronext.rpdptw.solve;

import java.util.List;
import java.util.Objects;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.VehicleId;

public record Route(VehicleId vehicleId, List<NodeId> visits) {

    public Route {
        Objects.requireNonNull(vehicleId, "vehicleId");
        visits = List.copyOf(visits);
        if (visits.isEmpty()) {
            throw new IllegalArgumentException("visits must not be empty");
        }
    }
}
