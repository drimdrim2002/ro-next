package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalRequestId;

import java.util.Objects;

public record StaticUnassignabilityFact(
        ExternalRequestId requestId,
        UnassignabilityReason reason,
        String detail
) {
    public StaticUnassignabilityFact {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(detail, "detail must not be null");
    }

    /** Backward-compatible view of detail text. */
    public String reasonText() {
        return detail;
    }
}
