package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalRequestId;

import java.util.Objects;

public record StaticUnassignabilityFact(ExternalRequestId requestId, String reason) {
    public StaticUnassignabilityFact {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}
