package com.ronext.rpdptw.normalization;

import java.util.Objects;
import java.util.Optional;

public record NormalizedProfileSelectionInput(
        String customer,
        String profile,
        String profileVersion,
        Optional<String> requestedPreset
) {
    public NormalizedProfileSelectionInput {
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(profileVersion, "profileVersion must not be null");
        Objects.requireNonNull(requestedPreset, "requestedPreset must not be null");
    }
}
