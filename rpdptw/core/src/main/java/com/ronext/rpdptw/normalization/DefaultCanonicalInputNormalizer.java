package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.adapter.input.AdaptedCanonicalInput;
import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class DefaultCanonicalInputNormalizer implements CanonicalInputNormalizer {

    private final IdentityAndReferenceNormalizer identityNormalizer;
    private final FixedPointNormalizer fixedPointNormalizer;
    private final TimeNormalizer timeNormalizer;
    private final ServiceTimeNormalizer serviceTimeNormalizer;
    private final TripPolicyNormalizer tripPolicyNormalizer;
    private final RouteResourceNormalizer routeResourceNormalizer;
    private final CompatibilityNormalizer compatibilityNormalizer;

    public DefaultCanonicalInputNormalizer() {
        this.identityNormalizer = new IdentityAndReferenceNormalizer();
        this.fixedPointNormalizer = new DefaultFixedPointNormalizer();
        this.timeNormalizer = new DefaultTimeNormalizer();
        this.serviceTimeNormalizer = new DefaultServiceTimeNormalizer();
        this.tripPolicyNormalizer = new DefaultTripPolicyNormalizer();
        this.routeResourceNormalizer = new DefaultRouteResourceNormalizer();
        this.compatibilityNormalizer = new DefaultCompatibilityNormalizer();
    }

    public DefaultCanonicalInputNormalizer(
            IdentityAndReferenceNormalizer identityNormalizer,
            FixedPointNormalizer fixedPointNormalizer,
            TimeNormalizer timeNormalizer,
            ServiceTimeNormalizer serviceTimeNormalizer,
            TripPolicyNormalizer tripPolicyNormalizer,
            RouteResourceNormalizer routeResourceNormalizer,
            CompatibilityNormalizer compatibilityNormalizer
    ) {
        this.identityNormalizer = Objects.requireNonNull(identityNormalizer);
        this.fixedPointNormalizer = Objects.requireNonNull(fixedPointNormalizer);
        this.timeNormalizer = Objects.requireNonNull(timeNormalizer);
        this.serviceTimeNormalizer = Objects.requireNonNull(serviceTimeNormalizer);
        this.tripPolicyNormalizer = Objects.requireNonNull(tripPolicyNormalizer);
        this.routeResourceNormalizer = Objects.requireNonNull(routeResourceNormalizer);
        this.compatibilityNormalizer = Objects.requireNonNull(compatibilityNormalizer);
    }

    @Override
    public NormalizationResult normalize(AdaptedCanonicalInput adapted, NormalizationPolicySnapshot policy) {
        Objects.requireNonNull(adapted, "adapted input must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        CanonicalBusinessInput input = adapted.canonicalInput();
        List<InputProblem> problems = new ArrayList<>();

        // 1. Validate identity duplicates & references
        problems.addAll(identityNormalizer.validate(input));

        // 2. Validate plan envelope times
        try {
            timeNormalizer.normalizePlanEnvelope(input.plan());
        } catch (TemporalReject e) {
            problems.add(new InputProblem.Temporal(e.code(), new InputPath("plan")));
        } catch (RuntimeException e) {
            problems.add(new InputProblem.Temporal(InputProblemCode.INVALID_PLAN_RANGE, new InputPath("plan")));
        }

        // 3. Validate & normalize vehicles
        List<NormalizedVehicleSpec> normalizedVehicles = new ArrayList<>();
        for (int i = 0; i < input.vehicles().size(); i++) {
            CanonicalVehicleInput v = input.vehicles().get(i);
            InputPath vPath = new InputPath("vehicles[" + i + "]");
            try {
                SizeFeatureCode sizeCode = compatibilityNormalizer.normalizeVehicleSizeCode(v.sizeFeatureCode());
                Set<CapabilityCode> caps = compatibilityNormalizer.normalizeCapabilities(v.capabilities());
                VehicleZoneSet zoneSet = compatibilityNormalizer.normalizeVehicleZones(v.vehicleZoneIds());
                compatibilityNormalizer.normalizeOwnership(v.ownership());
                compatibilityNormalizer.normalizeSpeed(v.speedKmH());
                tripPolicyNormalizer.normalizeTripPolicy(v.oneway(), v.singleRoundtrip(), 0);
                if (v.waitPolicy().isPresent()) {
                    tripPolicyNormalizer.normalizeWaitPolicy(v.waitPolicy().get());
                }
                routeResourceNormalizer.normalizeLimit(v.routeResourceLimit());

                normalizedVehicles.add(new NormalizedVehicleSpec(sizeCode, caps, zoneSet));
            } catch (CompatibilityReject e) {
                problems.add(new InputProblem.Compatibility(e.code(), vPath));
            } catch (TripReject e) {
                problems.add(new InputProblem.Trip(e.code(), vPath));
            } catch (ResourceReject e) {
                problems.add(new InputProblem.Numeric(e.code(), vPath));
            } catch (RuntimeException e) {
                problems.add(new InputProblem.Compatibility(InputProblemCode.INVALID_VEHICLE_FEATURE, vPath));
            }
        }

        // 4. Validate & normalize requests & items
        List<NormalizedRequestSpec> normalizedRequests = new ArrayList<>();
        for (int i = 0; i < input.requests().size(); i++) {
            CanonicalRequestInput req = input.requests().get(i);
            InputPath reqPath = new InputPath("requests[" + i + "]");

            try {
                compatibilityNormalizer.normalizeServicePattern(req.servicePattern());
            } catch (CompatibilityReject e) {
                problems.add(new InputProblem.Compatibility(e.code(), reqPath));
            }

            // Validate service windows
            if (req.pickup().isPresent()) {
                CanonicalServiceInput pickup = req.pickup().get();
                try {
                    timeNormalizer.normalizeWindow(pickup.windowOpen(), pickup.windowCloseInclusive(), input.plan().planStart());
                } catch (TemporalReject e) {
                    problems.add(new InputProblem.Temporal(e.code(), new InputPath("requests[" + i + "].pickup")));
                }
            }

            if (req.delivery() != null) {
                CanonicalServiceInput delivery = req.delivery();
                try {
                    timeNormalizer.normalizeWindow(delivery.windowOpen(), delivery.windowCloseInclusive(), input.plan().planStart());
                } catch (TemporalReject e) {
                    problems.add(new InputProblem.Temporal(e.code(), new InputPath("requests[" + i + "].delivery")));
                }
            }

            // Validate items & service time
            try {
                long totalWeight = 0;
                long totalVolume = 0;
                for (CanonicalItemInput item : req.items()) {
                    long w = fixedPointNormalizer.floorNonNegativeToScale3(item.weightDecimal());
                    long v = fixedPointNormalizer.floorNonNegativeToScale3(item.volumeDecimal());
                    long itemWeight = fixedPointNormalizer.multiplyChecked(w, item.quantity());
                    long itemVolume = fixedPointNormalizer.multiplyChecked(v, item.quantity());
                    totalWeight = fixedPointNormalizer.addChecked(totalWeight, itemWeight);
                    totalVolume = fixedPointNormalizer.addChecked(totalVolume, itemVolume);
                }
                serviceTimeNormalizer.calculateServiceSeconds(req.delivery() != null ? req.delivery().durationSeconds() : "0", Optional.empty(), req.items());
            } catch (NumericReject e) {
                problems.add(new InputProblem.Numeric(e.code(), reqPath));
            } catch (TemporalReject e) {
                problems.add(new InputProblem.Temporal(e.code(), reqPath));
            }

            // Compatibility specs
            AllowedVehicleSizes allowedSizes = new AllowedVehicleSizes.All();
            Set<CapabilityCode> requiredCaps = Set.of();
            Set<ZoneCode> reqZones = new java.util.HashSet<>();

            // Extract zones from service visits
            if (req.pickup().isPresent() && req.pickup().get().zone().isPresent()) {
                reqZones.add(new ZoneCode(req.pickup().get().zone().get()));
            }
            if (req.delivery() != null && req.delivery().zone().isPresent()) {
                reqZones.add(new ZoneCode(req.delivery().zone().get()));
            }

            if (req.compatibility() != null) {
                try {
                    allowedSizes = compatibilityNormalizer.normalizeAllowedVehicleSizes(req.compatibility().allowedVehicleSizes());
                    requiredCaps = compatibilityNormalizer.normalizeCapabilities(req.compatibility().requiredVehicleCapabilities());
                } catch (CompatibilityReject e) {
                    problems.add(new InputProblem.Compatibility(e.code(), reqPath));
                }
            }

            normalizedRequests.add(new NormalizedRequestSpec(req.id(), allowedSizes, requiredCaps, Set.copyOf(reqZones)));
        }

        // 5. Validate travel costs
        for (int i = 0; i < input.travelCosts().size(); i++) {
            CanonicalTravelInput t = input.travelCosts().get(i);
            InputPath tPath = new InputPath("travelCosts[" + i + "]");
            try {
                fixedPointNormalizer.requireIntegerLexeme(t.durationSeconds());
                fixedPointNormalizer.requireIntegerLexeme(t.distanceMeters());
            } catch (NumericReject e) {
                problems.add(new InputProblem.Numeric(e.code(), tPath));
            }
        }

        // If any problems exist, sort and return Rejected
        if (!problems.isEmpty()) {
            problems.sort(CanonicalOrdering.PROBLEM_COMPARATOR);
            return new NormalizationResult.Rejected(new InputRejectionReport(problems));
        }

        // 6. Sort entity collections lexicographically
        List<CanonicalVehicleInput> sortedVehicles = new ArrayList<>(input.vehicles());
        sortedVehicles.sort(CanonicalOrdering.VEHICLE_COMPARATOR);

        List<CanonicalLocationInput> sortedLocations = new ArrayList<>(input.locations());
        sortedLocations.sort(CanonicalOrdering.LOCATION_COMPARATOR);

        List<CanonicalRequestInput> sortedRequests = new ArrayList<>(input.requests());
        sortedRequests.sort(CanonicalOrdering.REQUEST_COMPARATOR);

        List<CanonicalTravelInput> sortedTravelCosts = new ArrayList<>(input.travelCosts());
        sortedTravelCosts.sort(CanonicalOrdering.TRAVEL_COMPARATOR);

        CanonicalBusinessInput sortedPayload = new CanonicalBusinessInput(
                input.plan(),
                sortedVehicles,
                sortedLocations,
                sortedRequests,
                sortedTravelCosts,
                input.provenance()
        );

        // 7. Evaluate static unassignability facts
        List<StaticUnassignabilityFact> unassignabilityFacts =
                compatibilityNormalizer.evaluateStaticUnassignability(normalizedRequests, normalizedVehicles);

        // 8. Compute normalized plan envelope & fingerprints
        NormalizedPlanEnvelope planEnvelope = timeNormalizer.normalizePlanEnvelope(input.plan());
        CanonicalFingerprint fingerprint = CanonicalFingerprint.compute(
                policy,
                input.provenance(),
                adapted.rawInputDigest(),
                sortedPayload
        );

        NormalizedInputArtifact artifact = new NormalizedInputArtifact(
                planEnvelope,
                sortedVehicles,
                sortedLocations,
                sortedRequests,
                sortedTravelCosts,
                adapted.rawInputDigest(),
                fingerprint,
                unassignabilityFacts,
                input.provenance(),
                policy
        );

        return new NormalizationResult.Accepted(artifact);
    }
}
