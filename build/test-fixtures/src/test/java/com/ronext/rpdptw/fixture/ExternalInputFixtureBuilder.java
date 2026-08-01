package com.ronext.rpdptw.fixture;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExternalInputFixtureBuilder {

    private String planId = "PLAN-001";
    private String customer = "CUSTOMER-A";
    private String profile = "DEFAULT_PROFILE";
    private String profileVersion = "1.0";
    private String preset = null;
    private String planStart = "2026-08-01 08:00:00";
    private String planEndExclusive = "2026-08-01 18:00:00";

    private final List<String> vehiclesJson = new ArrayList<>();
    private final List<String> locationsJson = new ArrayList<>();
    private final List<String> requestsJson = new ArrayList<>();
    private final List<String> travelJson = new ArrayList<>();

    public ExternalInputFixtureBuilder() {
        locationsJson.add("{\"id\":\"LOC-DEPOT\",\"zone\":\"ZONE-A\"}");
        locationsJson.add("{\"id\":\"LOC-DELIVERY-1\",\"zone\":\"ZONE-A\"}");

        vehiclesJson.add("{\"id\":\"VEH-1\",\"sizeFeatureCode\":\"MEDIUM\",\"capabilities\":[\"LIFT_GATE\"],\"vehicleZoneIds\":[\"ZONE-A\"],\"ownership\":\"DIRECT\",\"speedKmH\":\"60.0\",\"oneway\":true,\"singleRoundtrip\":false}");

        requestsJson.add("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[\"LIFT_GATE\"]}}");

        travelJson.add("{\"from\":\"LOC-DEPOT\",\"to\":\"LOC-DELIVERY-1\",\"durationSeconds\":\"600\",\"distanceMeters\":\"5000\"}");
        travelJson.add("{\"from\":\"LOC-DELIVERY-1\",\"to\":\"LOC-DEPOT\",\"durationSeconds\":\"600\",\"distanceMeters\":\"5000\"}");
    }

    public ExternalInputFixtureBuilder withPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public ExternalInputFixtureBuilder withCustomer(String customer) {
        this.customer = customer;
        return this;
    }

    public ExternalInputFixtureBuilder withProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public ExternalInputFixtureBuilder withProfileVersion(String profileVersion) {
        this.profileVersion = profileVersion;
        return this;
    }

    public ExternalInputFixtureBuilder withPreset(String preset) {
        this.preset = preset;
        return this;
    }

    public ExternalInputFixtureBuilder withPlanStart(String planStart) {
        this.planStart = planStart;
        return this;
    }

    public ExternalInputFixtureBuilder withPlanEndExclusive(String planEndExclusive) {
        this.planEndExclusive = planEndExclusive;
        return this;
    }

    public ExternalInputFixtureBuilder clearVehicles() {
        this.vehiclesJson.clear();
        return this;
    }

    public ExternalInputFixtureBuilder addVehicleJson(String json) {
        this.vehiclesJson.add(json);
        return this;
    }

    public ExternalInputFixtureBuilder clearLocations() {
        this.locationsJson.clear();
        return this;
    }

    public ExternalInputFixtureBuilder addLocationJson(String json) {
        this.locationsJson.add(json);
        return this;
    }

    public ExternalInputFixtureBuilder clearRequests() {
        this.requestsJson.clear();
        return this;
    }

    public ExternalInputFixtureBuilder addRequestJson(String json) {
        this.requestsJson.add(json);
        return this;
    }

    public ExternalInputFixtureBuilder clearTravel() {
        this.travelJson.clear();
        return this;
    }

    public ExternalInputFixtureBuilder addTravelJson(String json) {
        this.travelJson.add(json);
        return this;
    }

    public String buildJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"plan\": {\n");
        sb.append("    \"planId\": \"").append(planId).append("\",\n");
        sb.append("    \"customer\": \"").append(customer).append("\",\n");
        sb.append("    \"profile\": \"").append(profile).append("\",\n");
        sb.append("    \"profileVersion\": \"").append(profileVersion).append("\"");
        if (preset != null) {
            sb.append(",\n    \"preset\": \"").append(preset).append("\"");
        }
        sb.append(",\n    \"planStart\": \"").append(planStart).append("\",\n");
        sb.append("    \"planEndExclusive\": \"").append(planEndExclusive).append("\"\n");
        sb.append("  },\n");

        sb.append("  \"vehicles\": [\n    ").append(String.join(",\n    ", vehiclesJson)).append("\n  ],\n");
        sb.append("  \"locations\": [\n    ").append(String.join(",\n    ", locationsJson)).append("\n  ],\n");
        sb.append("  \"requests\": [\n    ").append(String.join(",\n    ", requestsJson)).append("\n  ],\n");
        sb.append("  \"travelCosts\": [\n    ").append(String.join(",\n    ", travelJson)).append("\n  ]\n");
        sb.append("}");
        return sb.toString();
    }

    public byte[] buildBytes() {
        return buildJson().getBytes(StandardCharsets.UTF_8);
    }
}
