package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;
import com.ronext.rpdptw.input.ExternalLocationId;
import com.ronext.rpdptw.input.ExternalRequestId;
import com.ronext.rpdptw.input.ExternalVehicleId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class IdentityAndReferenceNormalizer {

    private record TravelPairKey(ExternalLocationId from, ExternalLocationId to) {}

    public List<InputProblem> validate(CanonicalBusinessInput input) {
        Objects.requireNonNull(input, "input must not be null");
        List<InputProblem> problems = new ArrayList<>();

        // 1. Validate duplicate Request IDs
        Set<ExternalRequestId> seenRequestIds = new HashSet<>();
        for (int i = 0; i < input.requests().size(); i++) {
            CanonicalRequestInput req = input.requests().get(i);
            if (!seenRequestIds.add(req.id())) {
                problems.add(new InputProblem.Identity(
                        InputProblemCode.DUPLICATE_IDENTITY,
                        new InputPath("requests[" + i + "].id")
                ));
            }
        }

        // 2. Validate duplicate Vehicle IDs
        Set<ExternalVehicleId> seenVehicleIds = new HashSet<>();
        for (int i = 0; i < input.vehicles().size(); i++) {
            CanonicalVehicleInput v = input.vehicles().get(i);
            if (!seenVehicleIds.add(v.id())) {
                problems.add(new InputProblem.Identity(
                        InputProblemCode.DUPLICATE_IDENTITY,
                        new InputPath("vehicles[" + i + "].id")
                ));
            }
        }

        // 3. Validate duplicate Location IDs
        Set<ExternalLocationId> seenLocationIds = new HashSet<>();
        for (int i = 0; i < input.locations().size(); i++) {
            CanonicalLocationInput loc = input.locations().get(i);
            if (!seenLocationIds.add(loc.id())) {
                problems.add(new InputProblem.Identity(
                        InputProblemCode.DUPLICATE_IDENTITY,
                        new InputPath("locations[" + i + "].id")
                ));
            }
        }

        // 4. Validate duplicate Travel Keys
        Set<TravelPairKey> seenTravelKeys = new HashSet<>();
        for (int i = 0; i < input.travelCosts().size(); i++) {
            CanonicalTravelInput t = input.travelCosts().get(i);
            TravelPairKey key = new TravelPairKey(t.from(), t.to());
            if (!seenTravelKeys.add(key)) {
                problems.add(new InputProblem.Identity(
                        InputProblemCode.DUPLICATE_TRAVEL_KEY,
                        new InputPath("travelCosts[" + i + "]")
                ));
            }
        }

        // 5. Reference integrity validation
        Set<ExternalLocationId> validLocations = input.locations().stream()
                .map(CanonicalLocationInput::id)
                .collect(Collectors.toSet());

        for (int i = 0; i < input.requests().size(); i++) {
            CanonicalRequestInput req = input.requests().get(i);
            if (req.pickup().isPresent()) {
                ExternalLocationId pickupLoc = req.pickup().get().locationId();
                if (!validLocations.contains(pickupLoc)) {
                    problems.add(new InputProblem.Reference(
                            InputProblemCode.DANGLING_REFERENCE,
                            new InputPath("requests[" + i + "].pickup.locationId")
                    ));
                }
            }
            if (req.delivery() != null) {
                ExternalLocationId deliveryLoc = req.delivery().locationId();
                if (!validLocations.contains(deliveryLoc)) {
                    problems.add(new InputProblem.Reference(
                            InputProblemCode.DANGLING_REFERENCE,
                            new InputPath("requests[" + i + "].delivery.locationId")
                    ));
                }
            }
        }

        for (int i = 0; i < input.travelCosts().size(); i++) {
            CanonicalTravelInput t = input.travelCosts().get(i);
            if (!validLocations.contains(t.from())) {
                problems.add(new InputProblem.Reference(
                        InputProblemCode.DANGLING_REFERENCE,
                        new InputPath("travelCosts[" + i + "].from")
                ));
            }
            if (!validLocations.contains(t.to())) {
                problems.add(new InputProblem.Reference(
                        InputProblemCode.DANGLING_REFERENCE,
                        new InputPath("travelCosts[" + i + "].to")
                ));
            }
        }

        problems.sort(CanonicalOrdering.PROBLEM_COMPARATOR);
        return List.copyOf(problems);
    }
}
