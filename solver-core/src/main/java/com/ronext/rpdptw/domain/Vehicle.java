package com.ronext.rpdptw.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

public record Vehicle(
        VehicleId id,
        Optional<String> vehicleFeature,
        long maxWeightMilliKg,
        long maxVolumeMilliCbm,
        List<TimeWindow> workWindows,
        OptionalInt speedKmH,
        OptionalInt effectiveMaxStopCount,
        OptionalLong maxDriveTimeSec,
        OptionalLong maxDriveDistMeter,
        Set<String> capabilities,
        Optional<Set<String>> zoneIds,
        Optional<LocationId> startDepot,
        Optional<LocationId> endDepot) {

    public Vehicle {
        Objects.requireNonNull(id, "id");
        vehicleFeature = Objects.requireNonNull(vehicleFeature, "vehicleFeature");
        workWindows = List.copyOf(workWindows);
        Objects.requireNonNull(speedKmH, "speedKmH");
        Objects.requireNonNull(effectiveMaxStopCount, "effectiveMaxStopCount");
        Objects.requireNonNull(maxDriveTimeSec, "maxDriveTimeSec");
        Objects.requireNonNull(maxDriveDistMeter, "maxDriveDistMeter");
        capabilities = Set.copyOf(capabilities);
        zoneIds = Objects.requireNonNull(zoneIds, "zoneIds").map(Set::copyOf);
        startDepot = Objects.requireNonNull(startDepot, "startDepot");
        endDepot = Objects.requireNonNull(endDepot, "endDepot");
    }
}
