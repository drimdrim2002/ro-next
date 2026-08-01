package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;
import com.ronext.rpdptw.input.InputProvenance;
import com.ronext.rpdptw.input.RawInputDigest;

import java.util.List;
import java.util.Objects;

public record NormalizedInputArtifact(
        NormalizedPlanEnvelope plan,
        List<CanonicalVehicleInput> vehicles,
        List<CanonicalLocationInput> locations,
        List<CanonicalRequestInput> requests,
        List<CanonicalTravelInput> travelCosts,
        RawInputDigest rawInputDigest,
        CanonicalFingerprint fingerprint,
        List<StaticUnassignabilityFact> unassignabilityFacts,
        InputProvenance provenance,
        NormalizationPolicySnapshot policySnapshot
) {
    public NormalizedInputArtifact {
        Objects.requireNonNull(plan, "plan must not be null");
        Objects.requireNonNull(vehicles, "vehicles must not be null");
        Objects.requireNonNull(locations, "locations must not be null");
        Objects.requireNonNull(requests, "requests must not be null");
        Objects.requireNonNull(travelCosts, "travelCosts must not be null");
        Objects.requireNonNull(rawInputDigest, "rawInputDigest must not be null");
        Objects.requireNonNull(fingerprint, "fingerprint must not be null");
        Objects.requireNonNull(unassignabilityFacts, "unassignabilityFacts must not be null");
        Objects.requireNonNull(provenance, "provenance must not be null");
        Objects.requireNonNull(policySnapshot, "policySnapshot must not be null");

        vehicles = List.copyOf(vehicles);
        locations = List.copyOf(locations);
        requests = List.copyOf(requests);
        travelCosts = List.copyOf(travelCosts);
        unassignabilityFacts = List.copyOf(unassignabilityFacts);
    }
}
