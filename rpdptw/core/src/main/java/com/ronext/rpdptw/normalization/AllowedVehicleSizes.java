package com.ronext.rpdptw.normalization;

import java.util.List;
import java.util.Objects;

public sealed interface AllowedVehicleSizes {
    record All() implements AllowedVehicleSizes {}
    record Concrete(List<SizeFeatureCode> codes) implements AllowedVehicleSizes {
        public Concrete {
            Objects.requireNonNull(codes, "codes must not be null");
            codes = List.copyOf(codes);
        }
    }
}
