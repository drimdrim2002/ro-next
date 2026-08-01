package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalPlanId;

import java.util.Objects;
import java.util.Optional;

public record NormalizedPlanEnvelope(
        ExternalPlanId planId,
        String customer,
        String profile,
        String profileVersion,
        Optional<String> preset,
        long planStartEpochSecond,
        long planDurationSeconds,
        long planEndExclusiveSeconds
) {
    public NormalizedPlanEnvelope {
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(profileVersion, "profileVersion must not be null");
        Objects.requireNonNull(preset, "preset must not be null");
    }
}
