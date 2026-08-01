package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalRequestId;
import com.ronext.rpdptw.input.ServicePattern;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CompatibilityNormalizer {

    AllowedVehicleSizes normalizeAllowedVehicleSizes(List<String> rawCodes);

    SizeFeatureCode normalizeVehicleSizeCode(String rawCode);

    Set<CapabilityCode> normalizeCapabilities(Collection<String> rawCapabilities);

    VehicleZoneSet normalizeVehicleZones(Collection<String> rawZones);

    VehicleOwnership normalizeOwnership(Optional<String> rawOwnership);

    VehicleOwnership normalizeOwnership(String rawOwnership);

    VehicleSpeedInput normalizeSpeed(Optional<String> rawSpeed);

    VehicleSpeedInput normalizeSpeed(String rawSpeed);

    ServicePattern normalizeServicePattern(String rawPattern);

    ServicePattern normalizeServicePattern(ServicePattern rawPattern);

    boolean isVehicleCompatible(
            NormalizedVehicleSpec vehicle,
            NormalizedRequestSpec request
    );

    Optional<StaticUnassignabilityFact> checkStaticUnassignability(
            NormalizedRequestSpec request,
            List<NormalizedVehicleSpec> vehicles
    );

    List<StaticUnassignabilityFact> evaluateStaticUnassignability(
            List<NormalizedRequestSpec> requests,
            List<NormalizedVehicleSpec> vehicles
    );
}
