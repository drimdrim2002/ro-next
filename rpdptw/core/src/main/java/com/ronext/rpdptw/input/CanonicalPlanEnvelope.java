package com.ronext.rpdptw.input;

import java.util.Objects;
import java.util.Optional;

public record CanonicalPlanEnvelope(
    ExternalPlanId planId,
    String customer,
    String profile,
    String profileVersion,
    Optional<String> preset,
    String planStart,
    String planEndExclusive
) {
    public CanonicalPlanEnvelope {
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(profileVersion, "profileVersion must not be null");
        Objects.requireNonNull(preset, "preset must not be null");
        Objects.requireNonNull(planStart, "planStart must not be null");
        Objects.requireNonNull(planEndExclusive, "planEndExclusive must not be null");
    }
}
