package com.ronext.rpdptw.fixture;

import com.ronext.rpdptw.input.CanonicalBusinessInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixtureBuilderTest {

    @Test
    void testCanonicalInputFixtureBuilder() {
        CanonicalInputFixtureBuilder canonicalBuilder = new CanonicalInputFixtureBuilder();
        CanonicalBusinessInput businessInput = canonicalBuilder.build();
        assertNotNull(businessInput);
        assertNotNull(businessInput.plan());
        assertNotNull(businessInput.vehicles());
        assertNotNull(businessInput.locations());
        assertNotNull(businessInput.requests());
        assertNotNull(businessInput.travelCosts());
        assertNotNull(businessInput.provenance());
    }

    @Test
    void testExternalInputFixtureBuilder() {
        ExternalInputFixtureBuilder externalBuilder = new ExternalInputFixtureBuilder();
        String json = externalBuilder.buildJson();
        byte[] bytes = externalBuilder.buildBytes();
        assertNotNull(json);
        assertNotNull(bytes);
        assertTrue(json.contains("PLAN-001"));
        assertTrue(json.contains("CUSTOMER-A"));
    }

    @Test
    void testPhase01FailureFixtures() {
        assertNotNull(Phase01FailureFixtures.itemFirstDifference());
        assertNotNull(Phase01FailureFixtures.scaleBoundary());
        assertNotNull(Phase01FailureFixtures.integerLookingDecimal());
        assertNotNull(Phase01FailureFixtures.quantityOverflow());
        assertNotNull(Phase01FailureFixtures.sumOverflow());
        assertNotNull(Phase01FailureFixtures.duplicateRequest());
        assertNotNull(Phase01FailureFixtures.duplicateArc());
        assertNotNull(Phase01FailureFixtures.reqDateShape());
        assertNotNull(Phase01FailureFixtures.routeResourceInvalid());
        assertNotNull(Phase01FailureFixtures.unapprovedExtension());
        assertNotNull(Phase01FailureFixtures.piiRedaction());
        assertNotNull(Phase01FailureFixtures.zoneConflict());
        assertNotNull(Phase01FailureFixtures.ownershipAbsent());
        assertNotNull(Phase01FailureFixtures.speedAbsent());
        assertNotNull(Phase01FailureFixtures.servicePatternOnly());
        assertNotNull(Phase01FailureFixtures.featureAllMix());
        assertNotNull(Phase01FailureFixtures.decimalWinTravel());
    }
}
