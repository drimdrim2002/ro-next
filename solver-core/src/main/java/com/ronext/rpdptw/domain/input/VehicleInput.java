package com.ronext.rpdptw.domain.input;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

public record VehicleInput(
        String vehicleId,
        String vehicleFeature,
        BigDecimal maxWeightKg,
        BigDecimal maxVolumeCbm,
        LocalTime workStart,
        LocalTime workEnd,
        Integer speedKmH,
        Integer maxStopCnt,
        Long maxDriveTimeSec,
        Long maxDriveDistMeter,
        Set<String> capabilities,
        Set<String> zoneIds,
        String startDepotLocId,
        String endDepotLocId) {}
