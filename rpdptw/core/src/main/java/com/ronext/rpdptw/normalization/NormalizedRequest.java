package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ApprovedTypedExtensionInput;
import com.ronext.rpdptw.input.ExternalRequestId;
import com.ronext.rpdptw.input.ServicePattern;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record NormalizedRequest(
        ExternalRequestId id,
        ServicePattern servicePattern,
        Optional<NormalizedServiceVisit> pickup,
        NormalizedServiceVisit delivery,
        List<NormalizedItem> items,
        MilliKilograms totalWeightMilli,
        MilliCubicMeters totalVolumeMilli,
        Seconds totalServiceSeconds,
        AllowedVehicleSizes allowedSizes,
        Set<CapabilityCode> requiredCapabilities,
        Optional<Boolean> mandatoryDeclaration,
        Optional<ApprovedTypedExtensionInput> extensionInput
) {
    public NormalizedRequest {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(servicePattern, "servicePattern must not be null");
        Objects.requireNonNull(pickup, "pickup must not be null");
        Objects.requireNonNull(delivery, "delivery must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(totalWeightMilli, "totalWeightMilli must not be null");
        Objects.requireNonNull(totalVolumeMilli, "totalVolumeMilli must not be null");
        Objects.requireNonNull(totalServiceSeconds, "totalServiceSeconds must not be null");
        Objects.requireNonNull(allowedSizes, "allowedSizes must not be null");
        Objects.requireNonNull(requiredCapabilities, "requiredCapabilities must not be null");
        Objects.requireNonNull(mandatoryDeclaration, "mandatoryDeclaration must not be null");
        Objects.requireNonNull(extensionInput, "extensionInput must not be null");
        items = List.copyOf(items);
        requiredCapabilities = Set.copyOf(requiredCapabilities);
    }
}
