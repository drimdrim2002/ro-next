package com.ronext.rpdptw.normalization;

public sealed interface VehicleSpeedInput {
    record Absent() implements VehicleSpeedInput {}
    record PresentKmH(double value) implements VehicleSpeedInput {
        public PresentKmH {
            if (!(value > 0.0) || Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException("invalid speed");
            }
        }
    }
}
