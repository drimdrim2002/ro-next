package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.InputProvenance;
import com.ronext.rpdptw.input.RawInputDigest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

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
            CanonicalBusinessInput sortedPayload
    ) {
        String semantic = computeSemanticFingerprint(policy, sortedPayload);
        String envelope = computeEnvelopeFingerprint(rawInputDigest, semantic, provenance);
        return new CanonicalFingerprint(semantic, envelope);
    }

    public static String computeSemanticFingerprint(
            NormalizationPolicySnapshot policy,
            CanonicalBusinessInput payload
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("fp-v1\n");
        sb.append(policy.version()).append("|")
          .append(policy.scalePolicy()).append("|")
          .append(policy.fixedPointPrecision()).append("\n");

        // Plan
        var plan = payload.plan();
        sb.append("PLAN:").append(plan.planId().value()).append("|")
          .append(plan.customer()).append("|")
          .append(plan.profile()).append("|")
          .append(plan.profileVersion()).append("|")
          .append(plan.preset().orElse("NONE")).append("|")
          .append(plan.planStart()).append("|")
          .append(plan.planEndExclusive()).append("\n");

        // Vehicles
        sb.append("VEHICLES:").append(payload.vehicles().size()).append("\n");
        for (var v : payload.vehicles()) {
            sb.append("V:").append(v.id().value()).append("|")
              .append(v.sizeFeatureCode()).append("|")
              .append(v.capabilities().stream().sorted().toList()).append("|")
              .append(v.vehicleZoneIds().stream().sorted().toList()).append("|")
              .append(v.ownership().orElse("NONE")).append("|")
              .append(v.speedKmH().orElse("NONE")).append("|")
              .append(v.oneway()).append("|")
              .append(v.singleRoundtrip()).append("|")
              .append(v.waitPolicy().orElse("NONE")).append("|")
              .append(v.routeResourceLimit().orElse("NONE")).append("\n");
        }

        // Locations
        sb.append("LOCATIONS:").append(payload.locations().size()).append("\n");
        for (var loc : payload.locations()) {
            sb.append("L:").append(loc.id().value()).append("|")
              .append(loc.zone().orElse("NONE")).append("\n");
        }

        // Requests
        sb.append("REQUESTS:").append(payload.requests().size()).append("\n");
        for (var req : payload.requests()) {
            sb.append("R:").append(req.id().value()).append("|")
              .append(req.servicePattern()).append("|");

            if (req.pickup().isPresent()) {
                var p = req.pickup().get();
                sb.append("P[").append(p.locationId().value()).append("|")
                  .append(p.windowOpen()).append("|")
                  .append(p.windowCloseInclusive()).append("|")
                  .append(p.durationSeconds()).append("]|");
            } else {
                sb.append("P[NONE]|");
            }

            var d = req.delivery();
            sb.append("D[").append(d.locationId().value()).append("|")
              .append(d.windowOpen()).append("|")
              .append(d.windowCloseInclusive()).append("|")
              .append(d.durationSeconds()).append("]|");

            sb.append("ITEMS:").append(req.items().size()).append("[");
            for (var item : req.items()) {
                sb.append("I(").append(item.weightDecimal()).append(",")
                  .append(item.volumeDecimal()).append(",")
                  .append(item.quantity()).append(",")
                  .append(item.itemTaskTimeSeconds()).append(")");
            }
            sb.append("]|");

            sb.append("COMPAT:").append(req.compatibility().allowedVehicleSizes()).append(",")
              .append(req.compatibility().requiredVehicleCapabilities().stream().sorted().toList()).append("|");

            sb.append("MANDATORY:").append(req.mandatoryDeclaration().map(Object::toString).orElse("NONE")).append("\n");
        }

        // Travel costs
        sb.append("TRAVEL:").append(payload.travelCosts().size()).append("\n");
        for (var t : payload.travelCosts()) {
            sb.append("T:").append(t.from().value()).append("->").append(t.to().value()).append("|")
              .append(t.durationSeconds()).append("|")
              .append(t.distanceMeters()).append("\n");
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

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
