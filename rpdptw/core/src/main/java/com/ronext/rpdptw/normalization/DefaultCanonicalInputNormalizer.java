package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.adapter.input.AdaptedCanonicalInput;
import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;
import com.ronext.rpdptw.input.ServicePattern;

import java.util.ArrayList;
import java.util.HashSet;
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

        problems.addAll(identityNormalizer.validate(input));

        NormalizedPlanEnvelope planEnvelope = null;
        try {
            planEnvelope = timeNormalizer.normalizePlanEnvelope(input.plan());
        } catch (TemporalReject e) {
            problems.add(new InputProblem.Temporal(e.code(), new InputPath("plan")));
        } catch (RuntimeException e) {
            problems.add(new InputProblem.Temporal(InputProblemCode.INVALID_PLAN_RANGE, new InputPath("plan")));
        }

        List<NormalizedVehicle> vehicles = new ArrayList<>();
        List<NormalizedVehicleSpec> vehicleSpecs = new ArrayList<>();
        for (int i = 0; i < input.vehicles().size(); i++) {
            CanonicalVehicleInput v = input.vehicles().get(i);
            InputPath vPath = new InputPath("vehicles[" + i + "]");
            try {
                SizeFeatureCode sizeCode = compatibilityNormalizer.normalizeVehicleSizeCode(v.sizeFeatureCode());
                Set<CapabilityCode> caps = compatibilityNormalizer.normalizeCapabilities(v.capabilities());
                VehicleZoneSet zoneSet = compatibilityNormalizer.normalizeVehicleZones(v.vehicleZoneIds());
                VehicleOwnership ownership = compatibilityNormalizer.normalizeOwnership(v.ownership());
                VehicleSpeedInput speed = compatibilityNormalizer.normalizeSpeed(v.speedKmH());
                TripPolicy tripPolicy = tripPolicyNormalizer.normalizeTripPolicy(v.oneway(), v.singleRoundtrip(), 0);
                Optional<DepotWaitPolicy> waitPolicy = Optional.empty();
                if (v.waitPolicy().isPresent()) {
                    waitPolicy = Optional.of(tripPolicyNormalizer.normalizeWaitPolicy(v.waitPolicy().get()));
                }
                Optional<Long> routeLimit = routeResourceNormalizer.normalizeLimit(v.routeResourceLimit());

                NormalizedVehicle nv = new NormalizedVehicle(
                        v.id(), sizeCode, caps, zoneSet, ownership, speed, tripPolicy, waitPolicy, routeLimit
                );
                vehicles.add(nv);
                vehicleSpecs.add(new NormalizedVehicleSpec(sizeCode, caps, zoneSet));
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

        List<NormalizedRequest> requests = new ArrayList<>();
        List<NormalizedRequestSpec> requestSpecs = new ArrayList<>();
        for (int i = 0; i < input.requests().size(); i++) {
            CanonicalRequestInput req = input.requests().get(i);
            InputPath reqPath = new InputPath("requests[" + i + "]");
            try {
                ServicePattern pattern = compatibilityNormalizer.normalizeServicePattern(req.servicePattern());

                Optional<NormalizedServiceVisit> pickupVisit = Optional.empty();
                if (req.pickup().isPresent()) {
                    pickupVisit = Optional.of(normalizeVisit(
                            req.pickup().get(),
                            input.plan().planStart(),
                            "requests[" + i + "].pickup"
                    ));
                }

                if (req.delivery() == null) {
                    problems.add(new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("requests[" + i + "].delivery")));
                    continue;
                }
                NormalizedServiceVisit deliveryVisit = normalizeVisit(
                        req.delivery(),
                        input.plan().planStart(),
                        "requests[" + i + "].delivery"
                );

                List<NormalizedItem> items = new ArrayList<>();
                long totalWeight = 0L;
                long totalVolume = 0L;
                for (CanonicalItemInput item : req.items()) {
                    long unitW = fixedPointNormalizer.floorNonNegativeToScale3(item.weightDecimal());
                    long unitV = fixedPointNormalizer.floorNonNegativeToScale3(item.volumeDecimal());
                    long productW = fixedPointNormalizer.multiplyChecked(unitW, item.quantity());
                    long productV = fixedPointNormalizer.multiplyChecked(unitV, item.quantity());
                    totalWeight = fixedPointNormalizer.addChecked(totalWeight, productW);
                    totalVolume = fixedPointNormalizer.addChecked(totalVolume, productV);
                    long task = fixedPointNormalizer.requireIntegerLexeme(item.itemTaskTimeSeconds());
                    items.add(new NormalizedItem(
                            new MilliKilograms(productW),
                            new MilliCubicMeters(productV),
                            item.quantity(),
                            new Seconds(task)
                    ));
                }

                long totalService = serviceTimeNormalizer.calculateServiceSeconds(
                        req.delivery().durationSeconds(), Optional.empty(), req.items()
                );

                AllowedVehicleSizes allowedSizes = new AllowedVehicleSizes.All();
                Set<CapabilityCode> requiredCaps = Set.of();
                if (req.compatibility() != null) {
                    allowedSizes = compatibilityNormalizer.normalizeAllowedVehicleSizes(req.compatibility().allowedVehicleSizes());
                    requiredCaps = compatibilityNormalizer.normalizeCapabilities(req.compatibility().requiredVehicleCapabilities());
                }

                Set<ZoneCode> reqZones = new HashSet<>();
                if (pickupVisit.isPresent() && pickupVisit.get().zone().isPresent()) {
                    reqZones.add(pickupVisit.get().zone().get());
                }
                if (deliveryVisit.zone().isPresent()) {
                    reqZones.add(deliveryVisit.zone().get());
                }

                NormalizedRequest nreq = new NormalizedRequest(
                        req.id(),
                        pattern,
                        pickupVisit,
                        deliveryVisit,
                        items,
                        new MilliKilograms(totalWeight),
                        new MilliCubicMeters(totalVolume),
                        new Seconds(totalService),
                        allowedSizes,
                        requiredCaps,
                        req.mandatoryDeclaration(),
                        req.extensionInput()
                );
                requests.add(nreq);
                requestSpecs.add(new NormalizedRequestSpec(req.id(), allowedSizes, requiredCaps, Set.copyOf(reqZones)));
            } catch (CompatibilityReject e) {
                problems.add(new InputProblem.Compatibility(e.code(), reqPath));
            } catch (NumericReject e) {
                problems.add(new InputProblem.Numeric(e.code(), reqPath));
            } catch (TemporalReject e) {
                problems.add(new InputProblem.Temporal(e.code(), reqPath));
            } catch (RuntimeException e) {
                problems.add(new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, reqPath));
            }
        }

        List<NormalizedTravelArc> travelArcs = new ArrayList<>();
        for (int i = 0; i < input.travelCosts().size(); i++) {
            CanonicalTravelInput t = input.travelCosts().get(i);
            InputPath tPath = new InputPath("travelCosts[" + i + "]");
            try {
                long duration = fixedPointNormalizer.requireIntegerLexeme(t.durationSeconds());
                long distance = fixedPointNormalizer.requireIntegerLexeme(t.distanceMeters());
                travelArcs.add(new NormalizedTravelArc(
                        t.from(),
                        t.to(),
                        new Seconds(duration),
                        new Meters(distance)
                ));
            } catch (NumericReject e) {
                problems.add(new InputProblem.Numeric(e.code(), tPath));
            }
        }

        List<NormalizedLocation> locations = new ArrayList<>();
        for (CanonicalLocationInput loc : input.locations()) {
            Optional<ZoneCode> zone = loc.zone().map(ZoneCode::new);
            locations.add(new NormalizedLocation(loc.id(), zone));
        }

        if (!problems.isEmpty()) {
            problems.sort(CanonicalOrdering.PROBLEM_COMPARATOR);
            return new NormalizationResult.Rejected(new InputRejectionReport(problems));
        }

        // Success path only when plan envelope succeeded
        if (planEnvelope == null) {
            return new NormalizationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Temporal(InputProblemCode.INVALID_PLAN_RANGE, new InputPath("plan"))
            )));
        }

        vehicles.sort(CanonicalOrdering.NORMALIZED_VEHICLE_COMPARATOR);
        locations.sort(CanonicalOrdering.NORMALIZED_LOCATION_COMPARATOR);
        requests.sort(CanonicalOrdering.NORMALIZED_REQUEST_COMPARATOR);
        travelArcs.sort(CanonicalOrdering.NORMALIZED_TRAVEL_COMPARATOR);

        // Rebuild specs in same order for unassignability
        vehicleSpecs.clear();
        for (NormalizedVehicle v : vehicles) {
            vehicleSpecs.add(new NormalizedVehicleSpec(v.sizeCode(), v.capabilities(), v.zoneSet()));
        }
        requestSpecs.clear();
        for (NormalizedRequest r : requests) {
            Set<ZoneCode> zones = new HashSet<>();
            r.pickup().flatMap(NormalizedServiceVisit::zone).ifPresent(zones::add);
            r.delivery().zone().ifPresent(zones::add);
            requestSpecs.add(new NormalizedRequestSpec(r.id(), r.allowedSizes(), r.requiredCapabilities(), zones));
        }

        List<StaticUnassignabilityFact> facts =
                compatibilityNormalizer.evaluateStaticUnassignability(requestSpecs, vehicleSpecs);

        CanonicalFingerprint fingerprint = CanonicalFingerprint.compute(
                policy,
                input.provenance(),
                adapted.rawInputDigest(),
                planEnvelope,
                vehicles,
                locations,
                requests,
                travelArcs
        );

        NormalizedInputArtifact artifact = new NormalizedInputArtifact(
                planEnvelope,
                vehicles,
                locations,
                requests,
                travelArcs,
                adapted.rawInputDigest(),
                fingerprint,
                facts,
                input.provenance(),
                policy
        );

        return new NormalizationResult.Accepted(artifact);
    }

    private NormalizedServiceVisit normalizeVisit(
            CanonicalServiceInput service,
            String planStart,
            String pathPrefix
    ) {
        NormalizedWindow window = timeNormalizer.normalizeWindow(
                service.windowOpen(), service.windowCloseInclusive(), planStart
        );
        long duration = fixedPointNormalizer.requireIntegerLexeme(service.durationSeconds());

        Optional<Seconds> reqDate = Optional.empty();
        if (service.reqDate().isPresent()) {
            String raw = service.reqDate().get();
            try {
                long seconds = timeNormalizer.toPlanOriginSeconds(raw, planStart);
                reqDate = Optional.of(new Seconds(seconds));
            } catch (TemporalReject e) {
                throw new TemporalReject(InputProblemCode.INVALID_REQ_DATE, "Invalid reqDate at " + pathPrefix);
            }
        }

        Optional<ZoneCode> zone = service.zone().map(ZoneCode::new);
        return new NormalizedServiceVisit(
                service.locationId(),
                window,
                new Seconds(duration),
                reqDate,
                zone
        );
    }
}
