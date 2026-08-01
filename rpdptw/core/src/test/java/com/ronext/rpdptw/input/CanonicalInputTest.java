package com.ronext.rpdptw.input;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalInputTest {

    private CanonicalBusinessInput createSampleBusinessInput() {
        CanonicalPlanEnvelope plan = new CanonicalPlanEnvelope(
            new ExternalPlanId("PLAN-001"),
            "CUSTOMER-A",
            "DEFAULT_PROFILE",
            "1.0",
            Optional.empty(),
            "2026-08-01 08:00:00",
            "2026-08-01 18:00:00"
        );
        List<CanonicalLocationInput> locations = List.of(
            new CanonicalLocationInput(new ExternalLocationId("LOC-DEPOT"), Optional.of("ZONE-A")),
            new CanonicalLocationInput(new ExternalLocationId("LOC-DELIVERY-1"), Optional.of("ZONE-A"))
        );
        List<CanonicalVehicleInput> vehicles = List.of(
            new CanonicalVehicleInput(
                new ExternalVehicleId("VEH-1"),
                "MEDIUM",
                Set.of("LIFT_GATE"),
                Set.of("ZONE-A"),
                Optional.of("DIRECT"),
                Optional.of("60.0"),
                true,
                false,
                Optional.of("MOVE_EARLY_WAIT_TO_DEPOT"),
                Optional.empty()
            )
        );
        List<CanonicalRequestInput> requests = List.of(
            new CanonicalRequestInput(
                new ExternalRequestId("REQ-1"),
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                new CanonicalServiceInput(
                    new ExternalLocationId("LOC-DELIVERY-1"),
                    "2026-08-01 09:00:00",
                    "2026-08-01 17:00:00",
                    "300",
                    Optional.empty(),
                    Optional.of("ZONE-A")
                ),
                List.of(new CanonicalItemInput("10.000", "0.500", 1, "0")),
                new CanonicalCompatibilityInput(List.of("ALL"), Set.of("LIFT_GATE")),
                Optional.empty(),
                Optional.empty()
            )
        );
        List<CanonicalTravelInput> travelCosts = List.of(
            new CanonicalTravelInput(new ExternalLocationId("LOC-DEPOT"), new ExternalLocationId("LOC-DELIVERY-1"), "600", "5000"),
            new CanonicalTravelInput(new ExternalLocationId("LOC-DELIVERY-1"), new ExternalLocationId("LOC-DEPOT"), "600", "5000")
        );
        InputProvenance provenance = new InputProvenance(
            new AdapterIdentity("TEST_ADAPTER"),
            new SchemaIdentity("v1.0"),
            List.of(),
            List.of()
        );

        return new CanonicalBusinessInput(plan, vehicles, locations, requests, travelCosts, provenance);
    }

    @Test
    void testValidCanonicalBusinessInputCreation() {
        CanonicalBusinessInput input = createSampleBusinessInput();
        assertNotNull(input);
        assertEquals(new ExternalPlanId("PLAN-001"), input.plan().planId());
        assertEquals(1, input.vehicles().size());
        assertEquals(2, input.locations().size());
        assertEquals(1, input.requests().size());
        assertEquals(2, input.travelCosts().size());
        assertEquals(new AdapterIdentity("TEST_ADAPTER"), input.provenance().adapterIdentity());
    }

    @Test
    void testRequestInputServicePatternValidation() {
        ExternalRequestId reqId = new ExternalRequestId("REQ-1");
        ExternalLocationId locId = new ExternalLocationId("LOC-1");

        CanonicalServiceInput delivery = new CanonicalServiceInput(
            locId, "2026-08-01 09:00:00", "2026-08-01 17:00:00", "300", Optional.empty(), Optional.empty()
        );
        CanonicalServiceInput pickup = new CanonicalServiceInput(
            locId, "2026-08-01 08:00:00", "2026-08-01 12:00:00", "300", Optional.empty(), Optional.empty()
        );

        CanonicalCompatibilityInput compat = new CanonicalCompatibilityInput(List.of("ALL"), Set.of());
        List<CanonicalItemInput> items = List.of(new CanonicalItemInput("10.000", "0.500", 1, "0"));

        // DELIVERY_ONLY with pickup present -> failure
        assertThrows(IllegalArgumentException.class, () -> new CanonicalRequestInput(
            reqId, ServicePattern.DELIVERY_ONLY, Optional.of(pickup), delivery, items, compat, Optional.empty(), Optional.empty()
        ));

        // DELIVERY_ONLY with pickup empty -> valid
        CanonicalRequestInput validDeliveryOnly = new CanonicalRequestInput(
            reqId, ServicePattern.DELIVERY_ONLY, Optional.empty(), delivery, items, compat, Optional.empty(), Optional.empty()
        );
        assertEquals(ServicePattern.DELIVERY_ONLY, validDeliveryOnly.servicePattern());
        assertTrue(validDeliveryOnly.pickup().isEmpty());

        // PICKUP_DELIVERY with pickup empty -> failure
        assertThrows(IllegalArgumentException.class, () -> new CanonicalRequestInput(
            reqId, ServicePattern.PICKUP_DELIVERY, Optional.empty(), delivery, items, compat, Optional.empty(), Optional.empty()
        ));

        // PICKUP_DELIVERY with pickup present -> valid
        CanonicalRequestInput validPickupDelivery = new CanonicalRequestInput(
            reqId, ServicePattern.PICKUP_DELIVERY, Optional.of(pickup), delivery, items, compat, Optional.empty(), Optional.empty()
        );
        assertEquals(ServicePattern.PICKUP_DELIVERY, validPickupDelivery.servicePattern());
        assertTrue(validPickupDelivery.pickup().isPresent());
    }

    @Test
    void testCollectionDefensiveCopies() {
        // CanonicalBusinessInput defensive copy
        List<CanonicalVehicleInput> vehicles = new ArrayList<>(List.of(
            new CanonicalVehicleInput(new ExternalVehicleId("V1"), "S", Set.of(), Set.of(), Optional.empty(), Optional.empty(), true, false, Optional.empty(), Optional.empty())
        ));
        List<CanonicalLocationInput> locations = new ArrayList<>(List.of(
            new CanonicalLocationInput(new ExternalLocationId("L1"), Optional.empty())
        ));
        List<CanonicalRequestInput> requests = new ArrayList<>(List.of(
            new CanonicalRequestInput(
                new ExternalRequestId("R1"), ServicePattern.DELIVERY_ONLY, Optional.empty(),
                new CanonicalServiceInput(new ExternalLocationId("L1"), "O", "C", "D", Optional.empty(), Optional.empty()),
                List.of(new CanonicalItemInput("1.0", "1.0", 1, "0")),
                new CanonicalCompatibilityInput(List.of("ALL"), Set.of()), Optional.empty(), Optional.empty()
            )
        ));
        List<CanonicalTravelInput> travelCosts = new ArrayList<>(List.of(
            new CanonicalTravelInput(new ExternalLocationId("L1"), new ExternalLocationId("L1"), "0", "0")
        ));
        InputProvenance prov = new InputProvenance(
            new AdapterIdentity("A1"), new SchemaIdentity("S1"), List.of(), List.of()
        );

        CanonicalPlanEnvelope plan = new CanonicalPlanEnvelope(
            new ExternalPlanId("P1"), "C", "P", "1.0", Optional.empty(), "S", "E"
        );

        CanonicalBusinessInput businessInput = new CanonicalBusinessInput(
            plan, vehicles, locations, requests, travelCosts, prov
        );

        vehicles.clear();
        assertEquals(1, businessInput.vehicles().size());
        assertThrows(UnsupportedOperationException.class, () -> businessInput.vehicles().add(null));

        // CanonicalVehicleInput defensive copy
        Set<String> caps = new HashSet<>(Set.of("CAP1"));
        Set<String> zones = new HashSet<>(Set.of("ZONE1"));
        CanonicalVehicleInput vehicleInput = new CanonicalVehicleInput(
            new ExternalVehicleId("V1"), "S", caps, zones, Optional.empty(), Optional.empty(), true, false, Optional.empty(), Optional.empty()
        );
        caps.clear();
        zones.clear();
        assertEquals(1, vehicleInput.capabilities().size());
        assertEquals(1, vehicleInput.vehicleZoneIds().size());
        assertThrows(UnsupportedOperationException.class, () -> vehicleInput.capabilities().add("X"));

        // CanonicalCompatibilityInput defensive copy
        List<String> sizes = new ArrayList<>(List.of("S1"));
        Set<String> reqCaps = new HashSet<>(Set.of("C1"));
        CanonicalCompatibilityInput compatInput = new CanonicalCompatibilityInput(sizes, reqCaps);
        sizes.clear();
        reqCaps.clear();
        assertEquals(1, compatInput.allowedVehicleSizes().size());
        assertEquals(1, compatInput.requiredVehicleCapabilities().size());

        // InputProvenance defensive copy
        List<String> aliases = new ArrayList<>(List.of("a1"));
        List<String> unknown = new ArrayList<>(List.of("u1"));
        InputProvenance provenanceInput = new InputProvenance(
            new AdapterIdentity("A1"), new SchemaIdentity("S1"), aliases, unknown
        );
        aliases.clear();
        unknown.clear();
        assertEquals(1, provenanceInput.aliasesApplied().size());
        assertEquals(1, provenanceInput.unknownFieldsIgnored().size());
    }

    @Test
    void testConstructorNullChecks() {
        assertThrows(NullPointerException.class, () -> new CanonicalBusinessInput(
            null, List.of(), List.of(), List.of(), List.of(),
            new InputProvenance(new AdapterIdentity("A"), new SchemaIdentity("S"), List.of(), List.of())
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalPlanEnvelope(
            null, "C", "P", "1.0", Optional.empty(), "S", "E"
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalServiceInput(
            null, "O", "C", "D", Optional.empty(), Optional.empty()
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalItemInput(
            null, "1.0", 1, "0"
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalVehicleInput(
            null, "S", Set.of(), Set.of(), Optional.empty(), Optional.empty(), true, false, Optional.empty(), Optional.empty()
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalLocationInput(
            null, Optional.empty()
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalTravelInput(
            null, new ExternalLocationId("L1"), "0", "0"
        ));

        assertThrows(NullPointerException.class, () -> new CanonicalCompatibilityInput(
            null, Set.of()
        ));

        assertThrows(NullPointerException.class, () -> new ApprovedTypedExtensionInput(
            null, "{}"
        ));

        assertThrows(NullPointerException.class, () -> new InputProvenance(
            null, new SchemaIdentity("S"), List.of(), List.of()
        ));
    }
}
