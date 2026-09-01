package com.ronext.rpdptw.solve;

import java.util.Objects;
import java.util.Optional;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;

public record StructureViolation(Kind kind, Optional<RequestId> requestId, Optional<VehicleId> vehicleId) {

    public StructureViolation {
        Objects.requireNonNull(kind, "kind");
        requestId = Objects.requireNonNull(requestId, "requestId");
        vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
    }

    public enum Kind {
        UNKNOWN_VEHICLE,
        DUPLICATE_VEHICLE_ROUTE,
        UNKNOWN_NODE,
        DUPLICATE_NODE,
        PAIR_SPLIT,
        PAIR_INCOMPLETE,
        PICKUP_AFTER_DELIVERY,
        ASSIGNED_AND_BANKED,
        NOT_ASSIGNED_NOT_BANKED,
        UNKNOWN_REQUEST
    }
}
