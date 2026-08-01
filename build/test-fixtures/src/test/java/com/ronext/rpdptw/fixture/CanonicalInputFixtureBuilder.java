package com.ronext.rpdptw.fixture;

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
import com.ronext.rpdptw.input.SchemaIdentity;
import com.ronext.rpdptw.input.ServicePattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CanonicalInputFixtureBuilder {

    private CanonicalPlanEnvelope plan;
    private final List<CanonicalVehicleInput> vehicles = new ArrayList<>();
    private final List<CanonicalLocationInput> locations = new ArrayList<>();
    private final List<CanonicalRequestInput> requests = new ArrayList<>();
    private final List<CanonicalTravelInput> travelCosts = new ArrayList<>();
    private InputProvenance provenance;

    public CanonicalInputFixtureBuilder() {
        this.plan = new CanonicalPlanEnvelope(
            new ExternalPlanId("PLAN-001"),
            "CUSTOMER-A",
            "DEFAULT_PROFILE",
            "1.0",
            Optional.empty(),
            "2026-08-01 08:00:00",
            "2026-08-01 18:00:00"
        );

        this.locations.add(new CanonicalLocationInput(new ExternalLocationId("LOC-DEPOT"), Optional.of("ZONE-A")));
        this.locations.add(new CanonicalLocationInput(new ExternalLocationId("LOC-DELIVERY-1"), Optional.of("ZONE-A")));

        this.vehicles.add(new CanonicalVehicleInput(
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
        ));

        this.requests.add(new CanonicalRequestInput(
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
        ));

        this.travelCosts.add(new CanonicalTravelInput(
            new ExternalLocationId("LOC-DEPOT"),
            new ExternalLocationId("LOC-DELIVERY-1"),
            "600",
            "5000"
        ));
        this.travelCosts.add(new CanonicalTravelInput(
            new ExternalLocationId("LOC-DELIVERY-1"),
            new ExternalLocationId("LOC-DEPOT"),
            "600",
            "5000"
        ));

        this.provenance = new InputProvenance(
            new AdapterIdentity("TEST_ADAPTER"),
            new SchemaIdentity("v1.0"),
            List.of(),
            List.of()
        );
    }

    public CanonicalInputFixtureBuilder plan(CanonicalPlanEnvelope plan) {
        this.plan = plan;
        return this;
    }

    public CanonicalInputFixtureBuilder vehicles(List<CanonicalVehicleInput> vehicles) {
        this.vehicles.clear();
        this.vehicles.addAll(vehicles);
        return this;
    }

    public CanonicalInputFixtureBuilder addVehicle(CanonicalVehicleInput vehicle) {
        this.vehicles.add(vehicle);
        return this;
    }

    public CanonicalInputFixtureBuilder locations(List<CanonicalLocationInput> locations) {
        this.locations.clear();
        this.locations.addAll(locations);
        return this;
    }

    public CanonicalInputFixtureBuilder addLocation(CanonicalLocationInput location) {
        this.locations.add(location);
        return this;
    }

    public CanonicalInputFixtureBuilder requests(List<CanonicalRequestInput> requests) {
        this.requests.clear();
        this.requests.addAll(requests);
        return this;
    }

    public CanonicalInputFixtureBuilder addRequest(CanonicalRequestInput request) {
        this.requests.add(request);
        return this;
    }

    public CanonicalInputFixtureBuilder travelCosts(List<CanonicalTravelInput> travelCosts) {
        this.travelCosts.clear();
        this.travelCosts.addAll(travelCosts);
        return this;
    }

    public CanonicalInputFixtureBuilder addTravelCost(CanonicalTravelInput travelCost) {
        this.travelCosts.add(travelCost);
        return this;
    }

    public CanonicalInputFixtureBuilder provenance(InputProvenance provenance) {
        this.provenance = provenance;
        return this;
    }

    public CanonicalBusinessInput build() {
        return new CanonicalBusinessInput(
            plan,
            vehicles,
            locations,
            requests,
            travelCosts,
            provenance
        );
    }
}
