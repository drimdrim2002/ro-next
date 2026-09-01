package com.ronext.rpdptw.eval;

import java.util.Objects;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;

public record VisitFacts(
        NodeId nodeId,
        RequestId requestId,
        boolean pickup,
        LocationId locationId,
        long arrivalSec,
        long serviceStartSec,
        long serviceEndSec,
        long departureSec,
        long waitingSec,
        long loadWeightAfter,
        long loadVolumeAfter) {

    public VisitFacts {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(locationId, "locationId");
    }
}
