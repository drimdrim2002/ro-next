package com.ronext.rpdptw.domain.input;

public record OptionsInput(
        String trips,
        Integer multiRotation,
        String waitInDepot,
        Integer defaultSpeedKmH,
        Integer globalVehicleMaxStopCount) {}
