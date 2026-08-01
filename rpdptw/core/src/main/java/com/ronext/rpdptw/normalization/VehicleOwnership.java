package com.ronext.rpdptw.normalization;

public sealed interface VehicleOwnership {
    record Absent() implements VehicleOwnership {}
    record Direct() implements VehicleOwnership {}
    record Lease() implements VehicleOwnership {}
}
