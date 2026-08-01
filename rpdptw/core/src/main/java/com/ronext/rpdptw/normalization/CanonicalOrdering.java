package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;

import java.util.Comparator;

public final class CanonicalOrdering {

    private CanonicalOrdering() {}

    public static final Comparator<CanonicalRequestInput> REQUEST_COMPARATOR =
            Comparator.comparing(r -> r.id().value());

    public static final Comparator<CanonicalVehicleInput> VEHICLE_COMPARATOR =
            Comparator.comparing(v -> v.id().value());

    public static final Comparator<CanonicalLocationInput> LOCATION_COMPARATOR =
            Comparator.comparing(l -> l.id().value());

    public static final Comparator<CanonicalTravelInput> TRAVEL_COMPARATOR =
            Comparator.comparing((CanonicalTravelInput t) -> t.from().value())
                    .thenComparing(t -> t.to().value());

    public static final Comparator<InputProblem> PROBLEM_COMPARATOR =
            Comparator.comparing((InputProblem p) -> p.path().dotted())
                    .thenComparing(p -> p.code().name());

    public static Comparator<CanonicalRequestInput> requestComparator() {
        return REQUEST_COMPARATOR;
    }

    public static Comparator<CanonicalVehicleInput> vehicleComparator() {
        return VEHICLE_COMPARATOR;
    }

    public static Comparator<CanonicalLocationInput> locationComparator() {
        return LOCATION_COMPARATOR;
    }

    public static Comparator<CanonicalTravelInput> travelComparator() {
        return TRAVEL_COMPARATOR;
    }

    public static Comparator<InputProblem> problemComparator() {
        return PROBLEM_COMPARATOR;
    }
}
