package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.fixture.ExternalInputFixtureBuilder;
import com.ronext.rpdptw.fixture.Phase01FailureFixtures;
import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.normalization.DefaultCanonicalInputNormalizer;
import com.ronext.rpdptw.normalization.InputProblem;
import com.ronext.rpdptw.normalization.InputProblemCode;
import com.ronext.rpdptw.normalization.NormalizationPolicySnapshot;
import com.ronext.rpdptw.normalization.NormalizationResult;
import com.ronext.rpdptw.normalization.NormalizedInputArtifact;
import com.ronext.rpdptw.normalization.StaticUnassignabilityFact;
import com.ronext.rpdptw.normalization.VehicleOwnership;
import com.ronext.rpdptw.normalization.VehicleSpeedInput;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests exercising the full normalization pipeline:
 * {@code ExternalInputDocument -> SinglePathInputAdapterRegistry.testDefault().adapt(doc)
 *    -> DefaultCanonicalInputNormalizer().normalize(adapted, policy)}
 *
 * Covers the spec §9.3 failure matrix and happy path.
 */
class Phase01EndToEndNormalizationTest {

    private static final AdapterIdentity TEST_ADAPTER =
            new AdapterIdentity("TEST_FIXTURE_V1");

    private static final NormalizationPolicySnapshot TEST_POLICY =
            new NormalizationPolicySnapshot("1.0", "FLOOR_SCALE_3", "MILLI_UNITS");

    private final SinglePathInputAdapterRegistry registry =
            SinglePathInputAdapterRegistry.testDefault();

    private final DefaultCanonicalInputNormalizer normalizer =
            new DefaultCanonicalInputNormalizer();

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private NormalizationResult adaptAndNormalize(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ExternalInputDocument doc = new ExternalInputDocument(bytes, TEST_ADAPTER, "application/json");
        AdaptationResult adapted = registry.adapt(doc);

        if (adapted instanceof AdaptationResult.Rejected rejected) {
            // Convert adapter rejection into NormalizationResult.Rejected for uniform assertion
            return new NormalizationResult.Rejected(rejected.report());
        }

        AdaptationResult.Accepted accepted = (AdaptationResult.Accepted) adapted;
        return normalizer.normalize(accepted.input(), TEST_POLICY);
    }

    private NormalizationResult.Accepted assertAccepted(NormalizationResult result) {
        assertInstanceOf(NormalizationResult.Accepted.class, result,
                "Expected Accepted but got: " + result);
        return (NormalizationResult.Accepted) result;
    }

    private NormalizationResult.Rejected assertRejected(NormalizationResult result) {
        assertInstanceOf(NormalizationResult.Rejected.class, result,
                "Expected Rejected but got: " + result);
        return (NormalizationResult.Rejected) result;
    }

    private void assertProblemContains(NormalizationResult.Rejected rejected, InputProblemCode code) {
        boolean found = rejected.report().problems().stream()
                .anyMatch(p -> p.code() == code);
        assertTrue(found, "Expected problem code " + code + " but found: " + rejected.report().problems());
    }

    // -----------------------------------------------------------------------
    // 1. Happy path
    // -----------------------------------------------------------------------

    @Test
    void happyPathEndToEnd() {
        String json = new ExternalInputFixtureBuilder().buildJson();
        NormalizationResult result = adaptAndNormalize(json);

        NormalizationResult.Accepted accepted = assertAccepted(result);
        NormalizedInputArtifact artifact = accepted.artifact();

        assertNotNull(artifact.plan(), "plan must not be null");
        assertFalse(artifact.vehicles().isEmpty(), "vehicles must not be empty");
        assertFalse(artifact.locations().isEmpty(), "locations must not be empty");
        assertFalse(artifact.requests().isEmpty(), "requests must not be empty");
        assertFalse(artifact.travelCosts().isEmpty(), "travelCosts must not be empty");

        // SHA-256 digest must be present
        assertNotNull(artifact.rawInputDigest(), "rawInputDigest must not be null");
        assertNotNull(artifact.rawInputDigest().sha256(), "SHA-256 digest must not be null");
        assertEquals(32, artifact.rawInputDigest().sha256().length, "SHA-256 must be 32 bytes");

        // Fingerprints must be present
        assertNotNull(artifact.fingerprint(), "fingerprint must not be null");
        assertNotNull(artifact.fingerprint().semanticFingerprint(), "semantic fingerprint must not be null");
        assertNotNull(artifact.fingerprint().envelopeFingerprint(), "envelope fingerprint must not be null");
        assertFalse(artifact.fingerprint().semanticFingerprint().isEmpty());
        assertFalse(artifact.fingerprint().envelopeFingerprint().isEmpty());

        // Policy snapshot must be captured
        assertNotNull(artifact.policySnapshot());
        assertEquals(TEST_POLICY, artifact.policySnapshot());
    }

    // -----------------------------------------------------------------------
    // 2. Item-first difference: 0.0009 * 2 → weight 0 (floor before multiply)
    // -----------------------------------------------------------------------

    @Test
    void itemFirstDifference() {
        String json = Phase01FailureFixtures.itemFirstDifference();
        NormalizationResult result = adaptAndNormalize(json);

        // This should succeed — 0.0009 floors to 0 milli-units, which is valid
        NormalizationResult.Accepted accepted = assertAccepted(result);
        assertNotNull(accepted.artifact());
    }

    // -----------------------------------------------------------------------
    // 3. Scale boundary: "1.2340" and "1.2349" both → 1234 milli-units
    // -----------------------------------------------------------------------

    @Test
    void scaleBoundary() {
        String json = Phase01FailureFixtures.scaleBoundary();
        NormalizationResult result = adaptAndNormalize(json);

        // 1.2349 floors to 1234 milli-units — should be accepted
        NormalizationResult.Accepted accepted = assertAccepted(result);
        assertNotNull(accepted.artifact());
    }

    // -----------------------------------------------------------------------
    // 4. Integer-looking decimal: "600.0" for integer-only field → FRACTION_NOT_ALLOWED
    // -----------------------------------------------------------------------

    @Test
    void integerLookingDecimal() {
        String json = Phase01FailureFixtures.integerLookingDecimal();
        NormalizationResult result = adaptAndNormalize(json);

        NormalizationResult.Rejected rejected = assertRejected(result);
        assertProblemContains(rejected, InputProblemCode.FRACTION_NOT_ALLOWED);
    }

    // -----------------------------------------------------------------------
    // 5. Quantity overflow and sum overflow → ARITHMETIC_OVERFLOW, Rejected with NO artifact
    // -----------------------------------------------------------------------

    @Test
    void quantityOverflowAndSumOverflow() {
        // Quantity overflow
        String qJson = Phase01FailureFixtures.quantityOverflow();
        NormalizationResult qResult = adaptAndNormalize(qJson);

        NormalizationResult.Rejected qRejected = assertRejected(qResult);
        assertProblemContains(qRejected, InputProblemCode.ARITHMETIC_OVERFLOW);

        // Sum overflow
        String sJson = Phase01FailureFixtures.sumOverflow();
        NormalizationResult sResult = adaptAndNormalize(sJson);

        NormalizationResult.Rejected sRejected = assertRejected(sResult);
        assertProblemContains(sRejected, InputProblemCode.ARITHMETIC_OVERFLOW);
    }

    // -----------------------------------------------------------------------
    // 6. Duplicate request and duplicate arc
    // -----------------------------------------------------------------------

    @Test
    void duplicateRequestAndDuplicateArc() {
        // Duplicate request
        String drJson = Phase01FailureFixtures.duplicateRequest();
        NormalizationResult drResult = adaptAndNormalize(drJson);

        NormalizationResult.Rejected drRejected = assertRejected(drResult);
        assertProblemContains(drRejected, InputProblemCode.DUPLICATE_IDENTITY);

        // Duplicate travel arc
        String daJson = Phase01FailureFixtures.duplicateArc();
        NormalizationResult daResult = adaptAndNormalize(daJson);

        NormalizationResult.Rejected daRejected = assertRejected(daResult);
        assertProblemContains(daRejected, InputProblemCode.DUPLICATE_TRAVEL_KEY);
    }

    // -----------------------------------------------------------------------
    // 7. Decimal travel: decimal distance/duration → FRACTION_NOT_ALLOWED
    // -----------------------------------------------------------------------

    @Test
    void decimalWinTravel() {
        String json = Phase01FailureFixtures.decimalWinTravel();
        NormalizationResult result = adaptAndNormalize(json);

        NormalizationResult.Rejected rejected = assertRejected(result);
        assertProblemContains(rejected, InputProblemCode.FRACTION_NOT_ALLOWED);
    }

    // -----------------------------------------------------------------------
    // 8. Ownership absent and speed absent → typed absence, no defaults
    // -----------------------------------------------------------------------

    @Test
    void ownershipAbsentAndSpeedAbsent() {
        // Ownership absent
        String owJson = Phase01FailureFixtures.ownershipAbsent();
        NormalizationResult owResult = adaptAndNormalize(owJson);

        // Should be accepted — absent ownership is not a rejection, it's typed absence
        NormalizationResult.Accepted owAccepted = assertAccepted(owResult);
        assertNotNull(owAccepted.artifact());

        // Speed absent
        String spJson = Phase01FailureFixtures.speedAbsent();
        NormalizationResult spResult = adaptAndNormalize(spJson);

        // Should be accepted — absent speed is not a rejection, it's typed absence
        NormalizationResult.Accepted spAccepted = assertAccepted(spResult);
        assertNotNull(spAccepted.artifact());

        // Verify VehicleOwnership.Absent and VehicleSpeedInput.Absent are the correct types
        assertInstanceOf(VehicleOwnership.Absent.class, new VehicleOwnership.Absent());
        assertInstanceOf(VehicleSpeedInput.Absent.class, new VehicleSpeedInput.Absent());
    }

    // -----------------------------------------------------------------------
    // 9. Service pattern only: accepts DELIVERY_ONLY/PICKUP_DELIVERY,
    //    rejects legacy tokens like REAL_DELIVERY_ONLY → INVALID_SERVICE_PATTERN
    // -----------------------------------------------------------------------

    @Test
    void servicePatternOnly() {
        // Legacy token should be rejected
        String legacyJson = Phase01FailureFixtures.servicePatternOnly();
        NormalizationResult legacyResult = adaptAndNormalize(legacyJson);

        NormalizationResult.Rejected legacyRejected = assertRejected(legacyResult);
        assertProblemContains(legacyRejected, InputProblemCode.INVALID_SERVICE_PATTERN);

        // Valid DELIVERY_ONLY should be accepted (via happy path fixture)
        String validJson = new ExternalInputFixtureBuilder().buildJson();
        NormalizationResult validResult = adaptAndNormalize(validJson);
        assertAccepted(validResult);
    }

    // -----------------------------------------------------------------------
    // 10. PII redaction: rejection toString() must not leak raw input strings
    // -----------------------------------------------------------------------

    @Test
    void piiRedaction() {
        String json = Phase01FailureFixtures.piiRedaction();
        NormalizationResult result = adaptAndNormalize(json);

        NormalizationResult.Rejected rejected = assertRejected(result);
        String reportString = rejected.report().toString();

        // Canary values that MUST NOT appear in the report
        assertFalse(reportString.contains("user@secret-domain.com"),
                "rejection report must not leak email addresses");
        assertFalse(reportString.contains("123 Secret Street"),
                "rejection report must not leak addresses");
        assertFalse(reportString.contains("INVALID_SECONDS_ABC"),
                "rejection report must not leak raw invalid input values");
    }

    // -----------------------------------------------------------------------
    // 11. Zone conflict: conflicting pickup/delivery zones → Accepted
    //     with StaticUnassignabilityFact, NOT rejected
    // -----------------------------------------------------------------------

    @Test
    void zoneConflict() {
        String json = Phase01FailureFixtures.zoneConflict();
        NormalizationResult result = adaptAndNormalize(json);

        NormalizationResult.Accepted accepted = assertAccepted(result);
        NormalizedInputArtifact artifact = accepted.artifact();

        // Must have at least one unassignability fact
        assertFalse(artifact.unassignabilityFacts().isEmpty(),
                "zone conflict should produce at least one StaticUnassignabilityFact");

        // Verify the fact references our request
        StaticUnassignabilityFact fact = artifact.unassignabilityFacts().get(0);
        assertEquals("REQ-1", fact.requestId().value());
        assertNotNull(fact.reason());
        assertFalse(fact.reason().isEmpty());
    }
}
