package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.InputProvenance;
import com.ronext.rpdptw.input.RawInputDigest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Fingerprints bind to the sealed normalized meaning graph (fp-v2), not raw lexemes.
 */
public record CanonicalFingerprint(
        String semanticFingerprint,
        String envelopeFingerprint
) {
    public CanonicalFingerprint {
        Objects.requireNonNull(semanticFingerprint, "semanticFingerprint must not be null");
        Objects.requireNonNull(envelopeFingerprint, "envelopeFingerprint must not be null");
    }

    public static CanonicalFingerprint compute(
            NormalizationPolicySnapshot policy,
            InputProvenance provenance,
            RawInputDigest rawInputDigest,
            NormalizedPlanEnvelope plan,
            java.util.List<NormalizedVehicle> vehicles,
            java.util.List<NormalizedLocation> locations,
            java.util.List<NormalizedRequest> requests,
            java.util.List<NormalizedTravelArc> travelCosts
    ) {
        String semantic = computeSemanticFingerprint(policy, plan, vehicles, locations, requests, travelCosts);
        String envelope = computeEnvelopeFingerprint(rawInputDigest, semantic, provenance);
        return new CanonicalFingerprint(semantic, envelope);
    }

    public static String computeSemanticFingerprint(
            NormalizationPolicySnapshot policy,
            NormalizedPlanEnvelope plan,
            java.util.List<NormalizedVehicle> vehicles,
            java.util.List<NormalizedLocation> locations,
            java.util.List<NormalizedRequest> requests,
            java.util.List<NormalizedTravelArc> travelCosts
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("fp-v2\n");
        sb.append(policy.version()).append("|")
                .append(policy.scalePolicy()).append("|")
                .append(policy.fixedPointPrecision()).append("\n");

        sb.append("PLAN:").append(plan.planId().value()).append("|")
                .append(plan.customer()).append("|")
                .append(plan.profile()).append("|")
                .append(plan.profileVersion()).append("|")
                .append(plan.preset().orElse("NONE")).append("|")
                .append(plan.planStartEpochSecond()).append("|")
                .append(plan.planDurationSeconds()).append("|")
                .append(plan.planEndExclusiveSeconds()).append("|")
                .append(plan.workArcPolicy()).append("|")
                .append(plan.globalRouteResourceLimits().maxRouteDurationSeconds().map(Object::toString).orElse("NONE")).append("|")
                .append(plan.globalRouteResourceLimits().maxRouteDistanceMeters().map(Object::toString).orElse("NONE"))
                .append("\n");

        sb.append("VEHICLES:").append(vehicles.size()).append("\n");
        for (NormalizedVehicle v : vehicles) {
            sb.append("V:").append(v.id().value()).append("|")
                    .append(v.sizeCode().value()).append("|")
                    .append(v.capabilities().stream().map(CapabilityCode::value).sorted().toList()).append("|")
                    .append(encodeZoneSet(v.zoneSet())).append("|")
                    .append(encodeOwnership(v.ownership())).append("|")
                    .append(encodeSpeed(v.speed())).append("|")
                    .append(encodeTrip(v.tripPolicy())).append("|")
                    .append(v.waitPolicy().map(Enum::name).orElse("NONE")).append("|")
                    .append(v.routeResourceLimit().map(Object::toString).orElse("NONE"))
                    .append("\n");
        }

        sb.append("LOCATIONS:").append(locations.size()).append("\n");
        for (NormalizedLocation loc : locations) {
            sb.append("L:").append(loc.id().value()).append("|")
                    .append(loc.zone().map(ZoneCode::value).orElse("NONE")).append("\n");
        }

        sb.append("REQUESTS:").append(requests.size()).append("\n");
        for (NormalizedRequest req : requests) {
            sb.append("R:").append(req.id().value()).append("|")
                    .append(req.servicePattern()).append("|");
            if (req.pickup().isPresent()) {
                sb.append("P[").append(encodeVisit(req.pickup().get())).append("]|");
            } else {
                sb.append("P[NONE]|");
            }
            sb.append("D[").append(encodeVisit(req.delivery())).append("]|");
            sb.append("ITEMS:").append(req.items().size()).append("[");
            for (NormalizedItem item : req.items()) {
                sb.append("I(")
                        .append(item.weightMilli().value()).append(",")
                        .append(item.volumeMilli().value()).append(",")
                        .append(item.quantity()).append(",")
                        .append(item.itemTaskTime().value()).append(")");
            }
            sb.append("]|");
            sb.append("TOTALS:")
                    .append(req.totalWeightMilli().value()).append(",")
                    .append(req.totalVolumeMilli().value()).append(",")
                    .append(req.totalServiceSeconds().value()).append("|");
            sb.append("COMPAT:").append(encodeAllowedSizes(req.allowedSizes())).append(",")
                    .append(req.requiredCapabilities().stream().map(CapabilityCode::value).sorted().toList()).append("|");
            sb.append("MANDATORY:").append(req.mandatoryDeclaration().map(Object::toString).orElse("NONE")).append("\n");
        }

        sb.append("TRAVEL:").append(travelCosts.size()).append("\n");
        for (NormalizedTravelArc t : travelCosts) {
            sb.append("T:").append(t.from().value()).append("->").append(t.to().value()).append("|")
                    .append(t.duration().value()).append("|")
                    .append(t.distance().value()).append("\n");
        }

        return sha256Hex(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static String computeEnvelopeFingerprint(
            RawInputDigest rawDigest,
            String semanticFingerprint,
            InputProvenance provenance
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("RAW_DIGEST:").append(HexFormat.of().formatHex(rawDigest.sha256())).append("\n");
        sb.append("SEMANTIC_FP:").append(semanticFingerprint).append("\n");
        sb.append("PROVENANCE:").append(provenance.adapterIdentity().value()).append("|")
                .append(provenance.schemaIdentity().value()).append("|")
                .append(provenance.aliasesApplied()).append("|")
                .append(provenance.unknownFieldsIgnored()).append("\n");
        return sha256Hex(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String encodeVisit(NormalizedServiceVisit v) {
        return v.locationId().value() + "|"
                + v.window().startSecond() + "|"
                + v.window().endSecondExclusive() + "|"
                + v.serviceDuration().value() + "|"
                + v.reqDateFromPlanOrigin().map(s -> Long.toString(s.value())).orElse("NONE") + "|"
                + v.zone().map(ZoneCode::value).orElse("NONE");
    }

    private static String encodeZoneSet(VehicleZoneSet zoneSet) {
        if (zoneSet instanceof VehicleZoneSet.AllZones) {
            return "ALL_ZONES";
        }
        if (zoneSet instanceof VehicleZoneSet.Restricted restricted) {
            return restricted.zoneIds().stream()
                    .map(ZoneCode::value)
                    .sorted()
                    .collect(Collectors.joining(",", "RESTRICTED[", "]"));
        }
        return zoneSet.toString();
    }

    private static String encodeOwnership(VehicleOwnership ownership) {
        if (ownership instanceof VehicleOwnership.Absent) {
            return "ABSENT";
        }
        if (ownership instanceof VehicleOwnership.Direct) {
            return "DIRECT";
        }
        if (ownership instanceof VehicleOwnership.Lease) {
            return "LEASE";
        }
        return ownership.toString();
    }

    private static String encodeSpeed(VehicleSpeedInput speed) {
        if (speed instanceof VehicleSpeedInput.Absent) {
            return "ABSENT";
        }
        if (speed instanceof VehicleSpeedInput.PresentKmH present) {
            return "PRESENT:" + Double.toString(present.value());
        }
        return speed.toString();
    }

    private static String encodeTrip(TripPolicy trip) {
        if (trip instanceof TripPolicy.OneWay) {
            return "ONEWAY";
        }
        if (trip instanceof TripPolicy.SingleRoundTrip) {
            return "SINGLE_ROUNDTRIP";
        }
        return trip.toString();
    }

    private static String encodeAllowedSizes(AllowedVehicleSizes sizes) {
        if (sizes instanceof AllowedVehicleSizes.All) {
            return "ALL";
        }
        if (sizes instanceof AllowedVehicleSizes.Concrete concrete) {
            return concrete.codes().stream()
                    .map(SizeFeatureCode::value)
                    .sorted()
                    .collect(Collectors.joining(",", "CONCRETE[", "]"));
        }
        return sizes.toString();
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
