package com.ronext.rpdptw.input;

import java.util.List;
import java.util.Objects;

public record CanonicalBusinessInput(
    CanonicalPlanEnvelope plan,
    List<CanonicalVehicleInput> vehicles,
    List<CanonicalLocationInput> locations,
    List<CanonicalRequestInput> requests,
    List<CanonicalTravelInput> travelCosts,
    InputProvenance provenance
) {
    public CanonicalBusinessInput {
        Objects.requireNonNull(plan, "plan must not be null");
        Objects.requireNonNull(vehicles, "vehicles must not be null");
        Objects.requireNonNull(locations, "locations must not be null");
        Objects.requireNonNull(requests, "requests must not be null");
        Objects.requireNonNull(travelCosts, "travelCosts must not be null");
        Objects.requireNonNull(provenance, "provenance must not be null");
        vehicles = List.copyOf(vehicles);
        locations = List.copyOf(locations);
        requests = List.copyOf(requests);
        travelCosts = List.copyOf(travelCosts);
    }
}
