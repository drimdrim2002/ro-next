package com.ronext.rpdptw.normalization;

import java.util.Objects;

public record NormalizationPolicySnapshot(
        String version,
        String scalePolicy,
        String fixedPointPrecision
) {
    public NormalizationPolicySnapshot {
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(scalePolicy, "scalePolicy must not be null");
        Objects.requireNonNull(fixedPointPrecision, "fixedPointPrecision must not be null");
    }
}
