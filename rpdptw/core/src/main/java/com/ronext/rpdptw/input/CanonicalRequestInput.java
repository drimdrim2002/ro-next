package com.ronext.rpdptw.input;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CanonicalRequestInput(
    ExternalRequestId id,
    ServicePattern servicePattern,
    Optional<CanonicalServiceInput> pickup,
    CanonicalServiceInput delivery,
    List<CanonicalItemInput> items,
    CanonicalCompatibilityInput compatibility,
    Optional<Boolean> mandatoryDeclaration,
    Optional<ApprovedTypedExtensionInput> extensionInput
) {
    public CanonicalRequestInput {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(servicePattern, "servicePattern must not be null");
        Objects.requireNonNull(pickup, "pickup must not be null");
        Objects.requireNonNull(delivery, "delivery must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(compatibility, "compatibility must not be null");
        Objects.requireNonNull(mandatoryDeclaration, "mandatoryDeclaration must not be null");
        Objects.requireNonNull(extensionInput, "extensionInput must not be null");

        if (servicePattern == ServicePattern.DELIVERY_ONLY && pickup.isPresent()) {
            throw new IllegalArgumentException("pickup must be empty for DELIVERY_ONLY service pattern");
        }
        if (servicePattern == ServicePattern.PICKUP_DELIVERY && pickup.isEmpty()) {
            throw new IllegalArgumentException("pickup must be present for PICKUP_DELIVERY service pattern");
        }

        items = List.copyOf(items);
    }
}
