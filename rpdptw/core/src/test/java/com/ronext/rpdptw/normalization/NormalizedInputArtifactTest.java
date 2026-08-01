package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.adapter.input.AdaptedCanonicalInput;
import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.CanonicalCompatibilityInput;
import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalPlanEnvelope;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;
import com.ronext.rpdptw.input.ExternalLocationId;
import com.ronext.rpdptw.input.ExternalPlanId;
import com.ronext.rpdptw.input.ExternalRequestId;
import com.ronext.rpdptw.input.ExternalVehicleId;
import com.ronext.rpdptw.input.InputProvenance;
import com.ronext.rpdptw.input.RawInputDigest;
import com.ronext.rpdptw.input.SchemaIdentity;
import com.ronext.rpdptw.input.ServicePattern;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NormalizedInputArtifactTest {

    private final CanonicalInputNormalizer normalizer = new DefaultCanonicalInputNormalizer();
    private final NormalizationPolicySnapshot policy = new NormalizationPolicySnapshot("v1", "FLOOR", "3");

    private CanonicalBusinessInput createSampleInput() {
        CanonicalPlanEnvelope plan = new CanonicalPlanEnvelope(
                new ExternalPlanId("plan-1"),
                "customer-A",
                "profile-X",
                "1.0",
                Optional.of("preset-1"),
                "2026-08-01 00:00:00",
                "2026-08-02 00:00:00"
        );
        CanonicalLocationInput loc1 = new CanonicalLocationInput(new ExternalLocationId("loc-1"), Optional.empty());
        CanonicalLocationInput loc2 = new CanonicalLocationInput(new ExternalLocationId("loc-2"), Optional.empty());

        CanonicalVehicleInput v1 = new CanonicalVehicleInput(
                new ExternalVehicleId("v-1"),
                "SIZE_M",
                Set.of("CAP_1"),
                Set.of("ZONE_1"),
                Optional.of("DIRECT"),
                Optional.of("50.0"),
                true,
                false,
                Optional.of("NONE"),
                Optional.of("10")
        );

        CanonicalItemInput item1 = new CanonicalItemInput("1.000", "0.500", 10, "300");
        CanonicalServiceInput del = new CanonicalServiceInput(
                new ExternalLocationId("loc-2"),
                "2026-08-01 08:00:00",
                "2026-08-01 12:00:00",
                "300",
                Optional.empty(),
                Optional.empty()
        );
        CanonicalRequestInput req1 = new CanonicalRequestInput(
                new ExternalRequestId("req-1"),
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                del,
                List.of(item1),
                new CanonicalCompatibilityInput(List.of("SIZE_M"), Set.of("CAP_1")),
                Optional.of(true),
                Optional.empty()
        );

        CanonicalTravelInput travel = new CanonicalTravelInput(
                new ExternalLocationId("loc-1"),
                new ExternalLocationId("loc-2"),
                "600",
                "10000"
        );

        InputProvenance provenance = new InputProvenance(
                new AdapterIdentity("adapter-1"),
                new SchemaIdentity("schema-1"),
                List.of("alias1"),
                List.of()
        );

        return new CanonicalBusinessInput(
                plan,
                List.of(v1),
                List.of(loc1, loc2),
                List.of(req1),
                List.of(travel),
                provenance
        );
    }

    @Test
    void artifactDefensivelyCopiesAllCollections() {
        CanonicalBusinessInput input = createSampleInput();
        AdaptedCanonicalInput adapted = new AdaptedCanonicalInput(input, new RawInputDigest("abc".getBytes(StandardCharsets.UTF_8)));
        NormalizationResult result = normalizer.normalize(adapted, policy);

        assertInstanceOf(NormalizationResult.Accepted.class, result);
        NormalizedInputArtifact artifact = ((NormalizationResult.Accepted) result).artifact();

        // Mutate inputs / attempt to mutate artifact getters
        assertThrows(UnsupportedOperationException.class, () -> artifact.vehicles().add(null));
        assertThrows(UnsupportedOperationException.class, () -> artifact.locations().add(null));
        assertThrows(UnsupportedOperationException.class, () -> artifact.requests().add(null));
        assertThrows(UnsupportedOperationException.class, () -> artifact.travelCosts().add(null));
        assertThrows(UnsupportedOperationException.class, () -> artifact.unassignabilityFacts().add(null));
    }

    @Test
    void sameMeaningAndPolicyHasSameSemanticFingerprint() {
        CanonicalBusinessInput input1 = createSampleInput();
        CanonicalBusinessInput input2 = createSampleInput();

        AdaptedCanonicalInput adapted1 = new AdaptedCanonicalInput(input1, new RawInputDigest("digest1".getBytes(StandardCharsets.UTF_8)));
        AdaptedCanonicalInput adapted2 = new AdaptedCanonicalInput(input2, new RawInputDigest("digest2".getBytes(StandardCharsets.UTF_8)));

        NormalizationResult r1 = normalizer.normalize(adapted1, policy);
        NormalizationResult r2 = normalizer.normalize(adapted2, policy);

        NormalizedInputArtifact artifact1 = ((NormalizationResult.Accepted) r1).artifact();
        NormalizedInputArtifact artifact2 = ((NormalizationResult.Accepted) r2).artifact();

        assertEquals(artifact1.fingerprint().semanticFingerprint(), artifact2.fingerprint().semanticFingerprint());
    }

    @Test
    void profilePresetMandatoryResourceMeaningChangesSemanticFingerprint() {
        CanonicalBusinessInput input1 = createSampleInput();

        // Modify preset in plan
        CanonicalPlanEnvelope plan2 = new CanonicalPlanEnvelope(
                input1.plan().planId(),
                input1.plan().customer(),
                input1.plan().profile(),
                input1.plan().profileVersion(),
                Optional.of("preset-DIFFERENT"),
                input1.plan().planStart(),
                input1.plan().planEndExclusive()
        );
        CanonicalBusinessInput input2 = new CanonicalBusinessInput(
                plan2,
                input1.vehicles(),
                input1.locations(),
                input1.requests(),
                input1.travelCosts(),
                input1.provenance()
        );

        AdaptedCanonicalInput adapted1 = new AdaptedCanonicalInput(input1, new RawInputDigest("digest1".getBytes(StandardCharsets.UTF_8)));
        AdaptedCanonicalInput adapted2 = new AdaptedCanonicalInput(input2, new RawInputDigest("digest1".getBytes(StandardCharsets.UTF_8)));

        NormalizedInputArtifact artifact1 = ((NormalizationResult.Accepted) normalizer.normalize(adapted1, policy)).artifact();
        NormalizedInputArtifact artifact2 = ((NormalizationResult.Accepted) normalizer.normalize(adapted2, policy)).artifact();

        assertNotEquals(artifact1.fingerprint().semanticFingerprint(), artifact2.fingerprint().semanticFingerprint());
    }

    @Test
    void aliasProvenanceChangesEnvelopeNotMeaning() {
        CanonicalBusinessInput input1 = createSampleInput();

        InputProvenance provenance2 = new InputProvenance(
                input1.provenance().adapterIdentity(),
                input1.provenance().schemaIdentity(),
                List.of("alias1", "alias2_DIFFERENT"),
                input1.provenance().unknownFieldsIgnored()
        );
        CanonicalBusinessInput input2 = new CanonicalBusinessInput(
                input1.plan(),
                input1.vehicles(),
                input1.locations(),
                input1.requests(),
                input1.travelCosts(),
                provenance2
        );

        AdaptedCanonicalInput adapted1 = new AdaptedCanonicalInput(input1, new RawInputDigest("digest1".getBytes(StandardCharsets.UTF_8)));
        AdaptedCanonicalInput adapted2 = new AdaptedCanonicalInput(input2, new RawInputDigest("digest1".getBytes(StandardCharsets.UTF_8)));

        NormalizedInputArtifact artifact1 = ((NormalizationResult.Accepted) normalizer.normalize(adapted1, policy)).artifact();
        NormalizedInputArtifact artifact2 = ((NormalizationResult.Accepted) normalizer.normalize(adapted2, policy)).artifact();

        assertEquals(artifact1.fingerprint().semanticFingerprint(), artifact2.fingerprint().semanticFingerprint());
        assertNotEquals(artifact1.fingerprint().envelopeFingerprint(), artifact2.fingerprint().envelopeFingerprint());
    }

    @Test
    void rejectionNeverExposesPartialArtifact() {
        // Create an invalid input (e.g. duplicate vehicle ID)
        CanonicalBusinessInput input = createSampleInput();
        List<CanonicalVehicleInput> invalidVehicles = List.of(input.vehicles().get(0), input.vehicles().get(0));
        CanonicalBusinessInput invalidInput = new CanonicalBusinessInput(
                input.plan(),
                invalidVehicles,
                input.locations(),
                input.requests(),
                input.travelCosts(),
                input.provenance()
        );

        AdaptedCanonicalInput adapted = new AdaptedCanonicalInput(invalidInput, new RawInputDigest("digest1".getBytes(StandardCharsets.UTF_8)));
        NormalizationResult result = normalizer.normalize(adapted, policy);

        assertInstanceOf(NormalizationResult.Rejected.class, result);
        NormalizationResult.Rejected rejected = (NormalizationResult.Rejected) result;
        assertNotNull(rejected.report());
        assertFalse(rejected.report().problems().isEmpty());
    }

    @Test
    void rejectionEvidenceRedactsRawValuesAndInputBytes() {
        String canaryEmail = "secret_canary_user@example.com";
        // Create an invalid input with canary string in field path or location ID that causes rejection
        CanonicalBusinessInput input = createSampleInput();

        // Cause rejection via negative/invalid duration
        CanonicalTravelInput badTravel = new CanonicalTravelInput(
                new ExternalLocationId("loc-1"),
                new ExternalLocationId("loc-2"),
                "-100", // invalid negative duration
                "1000"
        );
        CanonicalBusinessInput invalidInput = new CanonicalBusinessInput(
                input.plan(),
                input.vehicles(),
                input.locations(),
                input.requests(),
                List.of(badTravel),
                input.provenance()
        );

        AdaptedCanonicalInput adapted = new AdaptedCanonicalInput(invalidInput, new RawInputDigest(("canary_bytes_" + canaryEmail).getBytes(StandardCharsets.UTF_8)));
        NormalizationResult result = normalizer.normalize(adapted, policy);

        assertInstanceOf(NormalizationResult.Rejected.class, result);
        InputRejectionReport report = ((NormalizationResult.Rejected) result).report();

        String reportStr = report.toString();
        assertFalse(reportStr.contains(canaryEmail), "Rejection report must not contain raw canary PII: " + reportStr);
        assertFalse(reportStr.contains("canary_bytes_"), "Rejection report must not contain raw input digest bytes: " + reportStr);
    }
}
