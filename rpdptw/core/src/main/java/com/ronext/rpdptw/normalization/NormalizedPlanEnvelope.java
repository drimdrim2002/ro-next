package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalPlanId;

import java.util.Objects;
import java.util.Optional;

public record NormalizedPlanEnvelope(
        ExternalPlanId planId,
        NormalizedProfileSelectionInput profileSelection,
        long planStartEpochSecond,
        long planDurationSeconds,
        long planEndExclusiveSeconds,
        NormalizedRouteResourceLimits globalRouteResourceLimits,
        WorkArcPolicy workArcPolicy
) {
    public NormalizedPlanEnvelope {
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(profileSelection, "profileSelection must not be null");
        Objects.requireNonNull(globalRouteResourceLimits, "globalRouteResourceLimits must not be null");
        Objects.requireNonNull(workArcPolicy, "workArcPolicy must not be null");
    }

    /** Convenience: customer from profile selection. */
    public String customer() {
        return profileSelection.customer();
    }

    public String profile() {
        return profileSelection.profile();
    }

    public String profileVersion() {
        return profileSelection.profileVersion();
    }

    public Optional<String> preset() {
        return profileSelection.requestedPreset();
    }
}
