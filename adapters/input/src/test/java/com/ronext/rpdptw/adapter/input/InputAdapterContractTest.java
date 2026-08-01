package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.adapter.input.testfixture.TestFixtureAliasPolicy;
import com.ronext.rpdptw.adapter.input.testfixture.TestFixtureInputAdapter;
import com.ronext.rpdptw.fixture.ExternalInputFixtureBuilder;
import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.normalization.InputProblemCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

class InputAdapterContractTest {

    private final SinglePathInputAdapterRegistry registry = SinglePathInputAdapterRegistry.testDefault();

    @Test
    void acceptsSupportedAdapterAndRecordsRawDigest() throws Exception {
        byte[] bytes = new ExternalInputFixtureBuilder().buildBytes();
        ExternalInputDocument doc = new ExternalInputDocument(
                bytes,
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );

        AdaptationResult result = registry.adapt(doc);

        assertInstanceOf(AdaptationResult.Accepted.class, result);
        AdaptationResult.Accepted accepted = (AdaptationResult.Accepted) result;

        byte[] expectedDigest = MessageDigest.getInstance("SHA-256").digest(bytes);
        assertArrayEquals(expectedDigest, accepted.input().rawInputDigest().sha256());
        assertNotNull(accepted.input().canonicalInput());
    }

    @Test
    void rejectsUnknownAdapterWithoutFallback() {
        byte[] bytes = new ExternalInputFixtureBuilder().buildBytes();
        ExternalInputDocument doc = new ExternalInputDocument(
                bytes,
                new AdapterIdentity("UNKNOWN_ADAPTER_V99"),
                "application/json"
        );

        AdaptationResult result = registry.adapt(doc);

        assertInstanceOf(AdaptationResult.Rejected.class, result);
        AdaptationResult.Rejected rejected = (AdaptationResult.Rejected) result;

        assertEquals(1, rejected.report().problems().size());
        assertEquals(InputProblemCode.UNSUPPORTED_ADAPTER_OR_SOURCE, rejected.report().problems().get(0).code());
        assertEquals("adapter", rejected.report().problems().get(0).path().dotted());
    }

    @Test
    void unknownFieldBehaviorComesFromExplicitPolicy() {
        String jsonWithUnknownField = """
            {
              "plan": {
                "planId": "PLAN-001",
                "customer": "CUSTOMER-A",
                "profile": "DEFAULT_PROFILE",
                "profileVersion": "1.0",
                "planStart": "2026-08-01 08:00:00",
                "planEndExclusive": "2026-08-01 18:00:00"
              },
              "unknownTopLevelField": "unexpected",
              "vehicles": [],
              "locations": [],
              "requests": [],
              "travelCosts": []
            }
            """;
        byte[] bytes = jsonWithUnknownField.getBytes(StandardCharsets.UTF_8);
        ExternalInputDocument doc = new ExternalInputDocument(
                bytes,
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );

        // Test STRICT policy -> Rejected
        SinglePathInputAdapterRegistry strictRegistry = new SinglePathInputAdapterRegistry(
                java.util.List.of(new TestFixtureInputAdapter(TestFixtureAliasPolicy.STRICT))
        );
        AdaptationResult strictResult = strictRegistry.adapt(doc);
        assertInstanceOf(AdaptationResult.Rejected.class, strictResult);
        AdaptationResult.Rejected strictRejected = (AdaptationResult.Rejected) strictResult;
        assertEquals(InputProblemCode.UNKNOWN_FIELD_REJECTED, strictRejected.report().problems().get(0).code());

        // Test IGNORE policy -> Accepted with unknown fields recorded in provenance
        SinglePathInputAdapterRegistry ignoreRegistry = new SinglePathInputAdapterRegistry(
                java.util.List.of(new TestFixtureInputAdapter(TestFixtureAliasPolicy.TEST_IGNORE))
        );
        AdaptationResult ignoreResult = ignoreRegistry.adapt(doc);
        assertInstanceOf(AdaptationResult.Accepted.class, ignoreResult);
        AdaptationResult.Accepted ignoreAccepted = (AdaptationResult.Accepted) ignoreResult;
        assertTrue(ignoreAccepted.input().canonicalInput().provenance().unknownFieldsIgnored().contains("unknownTopLevelField"));
    }
}
