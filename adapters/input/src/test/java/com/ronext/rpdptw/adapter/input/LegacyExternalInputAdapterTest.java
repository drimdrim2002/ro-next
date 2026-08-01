package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.fixture.ExternalInputFixtureBuilder;
import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.ServicePattern;
import com.ronext.rpdptw.normalization.InputProblemCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LegacyExternalInputAdapterTest {

    private final SinglePathInputAdapterRegistry registry = SinglePathInputAdapterRegistry.testDefault();

    @Test
    void mapsPerSideReqDateOnly() {
        String json = """
            {
              "plan": {
                "planId": "PLAN-001",
                "customer": "CUSTOMER-A",
                "profile": "DEFAULT_PROFILE",
                "profileVersion": "1.0",
                "planStart": "2026-08-01 08:00:00",
                "planEndExclusive": "2026-08-01 18:00:00"
              },
              "vehicles": [],
              "locations": [
                {"id": "LOC-P"},
                {"id": "LOC-D"}
              ],
              "requests": [
                {
                  "id": "REQ-1",
                  "servicePattern": "PICKUP_DELIVERY",
                  "pickup": {
                    "locationId": "LOC-P",
                    "windowOpen": "2026-08-01 08:00:00",
                    "windowCloseInclusive": "2026-08-01 12:00:00",
                    "durationSeconds": "300",
                    "reqDate": "2026-08-01"
                  },
                  "delivery": {
                    "locationId": "LOC-D",
                    "windowOpen": "2026-08-01 13:00:00",
                    "windowCloseInclusive": "2026-08-01 17:00:00",
                    "durationSeconds": "300",
                    "reqDate": "2026-08-02"
                  },
                  "items": [],
                  "compatibility": {
                    "allowedVehicleSizes": ["ALL"],
                    "requiredVehicleCapabilities": []
                  }
                }
              ],
              "travelCosts": []
            }
            """;
        ExternalInputDocument doc = new ExternalInputDocument(
                json.getBytes(StandardCharsets.UTF_8),
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );

        AdaptationResult result = registry.adapt(doc);
        assertInstanceOf(AdaptationResult.Accepted.class, result);
        AdaptationResult.Accepted accepted = (AdaptationResult.Accepted) result;

        CanonicalRequestInput req = accepted.input().canonicalInput().requests().get(0);
        assertEquals(ServicePattern.PICKUP_DELIVERY, req.servicePattern());
        assertTrue(req.pickup().isPresent());
        assertEquals(Optional.of("2026-08-01"), req.pickup().get().reqDate());
        assertEquals(Optional.of("2026-08-02"), req.delivery().reqDate());
    }

    @Test
    void rejectsOrderLevelTaskTime() {
        String json = """
            {
              "plan": {
                "planId": "PLAN-001",
                "customer": "CUSTOMER-A",
                "profile": "DEFAULT_PROFILE",
                "profileVersion": "1.0",
                "planStart": "2026-08-01 08:00:00",
                "planEndExclusive": "2026-08-01 18:00:00"
              },
              "vehicles": [],
              "locations": [{"id": "LOC-D"}],
              "requests": [
                {
                  "id": "REQ-1",
                  "servicePattern": "DELIVERY_ONLY",
                  "delivery": {
                    "locationId": "LOC-D",
                    "windowOpen": "2026-08-01 09:00:00",
                    "windowCloseInclusive": "2026-08-01 17:00:00",
                    "durationSeconds": "300"
                  },
                  "items": [],
                  "compatibility": {
                    "allowedVehicleSizes": ["ALL"],
                    "requiredVehicleCapabilities": []
                  },
                  "taskTimeSeconds": "300"
                }
              ],
              "travelCosts": []
            }
            """;
        ExternalInputDocument doc = new ExternalInputDocument(
                json.getBytes(StandardCharsets.UTF_8),
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );

        AdaptationResult result = registry.adapt(doc);
        assertInstanceOf(AdaptationResult.Rejected.class, result);
        AdaptationResult.Rejected rejected = (AdaptationResult.Rejected) result;

        assertEquals(1, rejected.report().problems().size());
        assertEquals(InputProblemCode.ORDER_LEVEL_TASK_TIME_NOT_ALLOWED, rejected.report().problems().get(0).code());
        assertEquals("requests[0].taskTimeSeconds", rejected.report().problems().get(0).path().dotted());
    }

    @Test
    void preservesExactProfileSelectionAndPresetOmission() {
        // Preset omitted
        byte[] bytesOmitted = new ExternalInputFixtureBuilder()
                .withCustomer("CUST-1")
                .withProfile("PROF-A")
                .withProfileVersion("2.1")
                .withPreset(null)
                .buildBytes();
        ExternalInputDocument docOmitted = new ExternalInputDocument(
                bytesOmitted,
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );
        AdaptationResult resultOmitted = registry.adapt(docOmitted);
        assertInstanceOf(AdaptationResult.Accepted.class, resultOmitted);
        var envelopeOmitted = ((AdaptationResult.Accepted) resultOmitted).input().canonicalInput().plan();
        assertEquals("CUST-1", envelopeOmitted.customer());
        assertEquals("PROF-A", envelopeOmitted.profile());
        assertEquals("2.1", envelopeOmitted.profileVersion());
        assertEquals(Optional.empty(), envelopeOmitted.preset());

        // Preset present
        byte[] bytesPresent = new ExternalInputFixtureBuilder()
                .withCustomer("CUST-1")
                .withProfile("PROF-A")
                .withProfileVersion("2.1")
                .withPreset("PRESET-X")
                .buildBytes();
        ExternalInputDocument docPresent = new ExternalInputDocument(
                bytesPresent,
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );
        AdaptationResult resultPresent = registry.adapt(docPresent);
        assertInstanceOf(AdaptationResult.Accepted.class, resultPresent);
        var envelopePresent = ((AdaptationResult.Accepted) resultPresent).input().canonicalInput().plan();
        assertEquals(Optional.of("PRESET-X"), envelopePresent.preset());
    }

    @Test
    void rejectsUnapprovedRawCustomerExtension() {
        String json = """
            {
              "plan": {
                "planId": "PLAN-001",
                "customer": "CUSTOMER-A",
                "profile": "DEFAULT_PROFILE",
                "profileVersion": "1.0",
                "planStart": "2026-08-01 08:00:00",
                "planEndExclusive": "2026-08-01 18:00:00"
              },
              "vehicles": [],
              "locations": [{"id": "LOC-D"}],
              "requests": [
                {
                  "id": "REQ-1",
                  "servicePattern": "DELIVERY_ONLY",
                  "delivery": {
                    "locationId": "LOC-D",
                    "windowOpen": "2026-08-01 09:00:00",
                    "windowCloseInclusive": "2026-08-01 17:00:00",
                    "durationSeconds": "300"
                  },
                  "items": [],
                  "compatibility": {
                    "allowedVehicleSizes": ["ALL"],
                    "requiredVehicleCapabilities": []
                  },
                  "rawCustomerExtension": {
                    "unapprovedKey": "unapprovedValue"
                  }
                }
              ],
              "travelCosts": []
            }
            """;
        ExternalInputDocument doc = new ExternalInputDocument(
                json.getBytes(StandardCharsets.UTF_8),
                new AdapterIdentity("TEST_FIXTURE_V1"),
                "application/json"
        );

        AdaptationResult result = registry.adapt(doc);
        assertInstanceOf(AdaptationResult.Rejected.class, result);
        AdaptationResult.Rejected rejected = (AdaptationResult.Rejected) result;

        assertEquals(1, rejected.report().problems().size());
        assertEquals(InputProblemCode.UNAPPROVED_EXTENSION_INPUT, rejected.report().problems().get(0).code());
        assertEquals("requests[0].rawCustomerExtension", rejected.report().problems().get(0).path().dotted());
    }
}
