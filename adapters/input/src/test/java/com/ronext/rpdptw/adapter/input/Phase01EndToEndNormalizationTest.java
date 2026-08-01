package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.fixture.ExternalInputFixtureBuilder;
import com.ronext.rpdptw.fixture.Phase01FailureFixtures;
import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.normalization.DefaultCanonicalInputNormalizer;
import com.ronext.rpdptw.normalization.InputProblemCode;
import com.ronext.rpdptw.normalization.NormalizationPolicySnapshot;
import com.ronext.rpdptw.normalization.NormalizationResult;
import com.ronext.rpdptw.normalization.NormalizedInputArtifact;
import com.ronext.rpdptw.normalization.NormalizedItem;
import com.ronext.rpdptw.normalization.StaticUnassignabilityFact;
import com.ronext.rpdptw.normalization.UnassignabilityReason;
import com.ronext.rpdptw.normalization.VehicleOwnership;
import com.ronext.rpdptw.normalization.VehicleSpeedInput;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end: ExternalInputDocument → adapter → sealed NormalizedInputArtifact.
 * Covers §9.3 catalog with behavioral oracles on sealed fields.
 */
class Phase01EndToEndNormalizationTest {

    private static final AdapterIdentity TEST_ADAPTER = new AdapterIdentity("TEST_FIXTURE_V1");

    private static final NormalizationPolicySnapshot TEST_POLICY =
            new NormalizationPolicySnapshot("1.0", "FLOOR_SCALE_3", "MILLI_UNITS");

    private final SinglePathInputAdapterRegistry registry = SinglePathInputAdapterRegistry.testDefault();
    private final DefaultCanonicalInputNormalizer normalizer = new DefaultCanonicalInputNormalizer();

    private NormalizationResult adaptAndNormalize(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ExternalInputDocument doc = new ExternalInputDocument(bytes, TEST_ADAPTER, "application/json");
        AdaptationResult adapted = registry.adapt(doc);
        if (adapted instanceof AdaptationResult.Rejected rejected) {
            return new NormalizationResult.Rejected(rejected.report());
        }
        AdaptationResult.Accepted accepted = (AdaptationResult.Accepted) adapted;
        return normalizer.normalize(accepted.input(), TEST_POLICY);
    }

    private NormalizationResult.Accepted assertAccepted(NormalizationResult result) {
        assertInstanceOf(NormalizationResult.Accepted.class, result, "Expected Accepted but got: " + result);
        return (NormalizationResult.Accepted) result;
    }

    private NormalizationResult.Rejected assertRejected(NormalizationResult result) {
        assertInstanceOf(NormalizationResult.Rejected.class, result, "Expected Rejected but got: " + result);
        return (NormalizationResult.Rejected) result;
    }

    private void assertProblemContains(NormalizationResult.Rejected rejected, InputProblemCode code) {
        boolean found = rejected.report().problems().stream().anyMatch(p -> p.code() == code);
        assertTrue(found, "Expected problem code " + code + " but found: " + rejected.report().problems());
    }

    @Test
    void happyPathEndToEnd() {
        String json = new ExternalInputFixtureBuilder().buildJson();
        NormalizationResult.Accepted accepted = assertAccepted(adaptAndNormalize(json));
        NormalizedInputArtifact artifact = accepted.artifact();

        assertNotNull(artifact.plan());
        assertFalse(artifact.vehicles().isEmpty());
        assertFalse(artifact.locations().isEmpty());
        assertFalse(artifact.requests().isEmpty());
        assertFalse(artifact.travelCosts().isEmpty());
        assertEquals(32, artifact.rawInputDigest().sha256().length);
        assertFalse(artifact.fingerprint().semanticFingerprint().isEmpty());
        assertFalse(artifact.fingerprint().envelopeFingerprint().isEmpty());
        assertEquals(TEST_POLICY, artifact.policySnapshot());
        // sealed values
        assertTrue(artifact.requests().get(0).items().get(0).weightMilli().value() > 0
                || artifact.requests().get(0).items().get(0).weightMilli().value() == 0);
        assertTrue(artifact.travelCosts().get(0).duration().value() >= 0);
    }

    @Test
    void itemFirstDifference() {
        NormalizationResult.Accepted accepted = assertAccepted(adaptAndNormalize(Phase01FailureFixtures.itemFirstDifference()));
        NormalizedItem item = accepted.artifact().requests().get(0).items().get(0);
        assertEquals(0L, item.weightMilli().value());
        assertEquals(2, item.quantity());
    }

    @Test
    void scaleBoundary() {
        NormalizationResult.Accepted accepted = assertAccepted(adaptAndNormalize(Phase01FailureFixtures.scaleBoundary()));
        // fixture has two items: 1.2340 and 1.2349
        var items = accepted.artifact().requests().get(0).items();
        assertEquals(2, items.size());
        assertEquals(1234L, items.get(0).weightMilli().value());
        assertEquals(1234L, items.get(1).weightMilli().value());
    }

    @Test
    void integerLookingDecimal() {
        NormalizationResult.Rejected rejected = assertRejected(adaptAndNormalize(Phase01FailureFixtures.integerLookingDecimal()));
        assertProblemContains(rejected, InputProblemCode.FRACTION_NOT_ALLOWED);
    }

    @Test
    void quantityOverflowAndSumOverflow() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.quantityOverflow())),
                InputProblemCode.ARITHMETIC_OVERFLOW);
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.sumOverflow())),
                InputProblemCode.ARITHMETIC_OVERFLOW);
    }

    @Test
    void duplicateRequestAndDuplicateArc() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.duplicateRequest())),
                InputProblemCode.DUPLICATE_IDENTITY);
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.duplicateArc())),
                InputProblemCode.DUPLICATE_TRAVEL_KEY);
    }

    @Test
    void decimalWinTravel() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.decimalWinTravel())),
                InputProblemCode.FRACTION_NOT_ALLOWED);
    }

    @Test
    void ownershipAbsentAndSpeedAbsent() {
        NormalizationResult.Accepted ow = assertAccepted(adaptAndNormalize(Phase01FailureFixtures.ownershipAbsent()));
        assertInstanceOf(VehicleOwnership.Absent.class, ow.artifact().vehicles().get(0).ownership());

        NormalizationResult.Accepted sp = assertAccepted(adaptAndNormalize(Phase01FailureFixtures.speedAbsent()));
        assertInstanceOf(VehicleSpeedInput.Absent.class, sp.artifact().vehicles().get(0).speed());
    }

    @Test
    void servicePatternOnly() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.servicePatternOnly())),
                InputProblemCode.INVALID_SERVICE_PATTERN);
        assertAccepted(adaptAndNormalize(new ExternalInputFixtureBuilder().buildJson()));
    }

    @Test
    void piiRedaction() {
        NormalizationResult.Rejected rejected = assertRejected(adaptAndNormalize(Phase01FailureFixtures.piiRedaction()));
        String reportString = rejected.report().toString();
        assertFalse(reportString.contains("user@secret-domain.com"));
        assertFalse(reportString.contains("123 Secret Street"));
        assertFalse(reportString.contains("INVALID_SECONDS_ABC"));
    }

    @Test
    void zoneConflict() {
        NormalizationResult.Accepted accepted = assertAccepted(adaptAndNormalize(Phase01FailureFixtures.zoneConflict()));
        assertFalse(accepted.artifact().unassignabilityFacts().isEmpty());
        StaticUnassignabilityFact fact = accepted.artifact().unassignabilityFacts().get(0);
        assertEquals("REQ-1", fact.requestId().value());
        assertEquals(UnassignabilityReason.PICKUP_DELIVERY_ZONE_UNION_UNCOVERED, fact.reason());
    }

    @Test
    void reqDateShape() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.reqDateShape())),
                InputProblemCode.INVALID_REQ_DATE);
    }

    @Test
    void routeResourceInvalid() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.routeResourceInvalid())),
                InputProblemCode.INVALID_ROUTE_RESOURCE_LIMIT);
    }

    @Test
    void unapprovedExtension() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.unapprovedExtension())),
                InputProblemCode.UNKNOWN_FIELD_REJECTED);
    }

    @Test
    void featureAllMix() {
        assertProblemContains(assertRejected(adaptAndNormalize(Phase01FailureFixtures.featureAllMix())),
                InputProblemCode.INVALID_FEATURE_LIST);
    }
}
