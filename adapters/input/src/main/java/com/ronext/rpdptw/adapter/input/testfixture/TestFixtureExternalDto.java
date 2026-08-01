package com.ronext.rpdptw.adapter.input.testfixture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TestFixtureExternalDto(
    @JsonProperty("plan") PlanDto plan,
    @JsonProperty("vehicles") List<VehicleDto> vehicles,
    @JsonProperty("locations") List<LocationDto> locations,
    @JsonProperty("requests") List<RequestDto> requests,
    @JsonProperty("travelCosts") List<TravelCostDto> travelCosts
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlanDto(
        @JsonProperty("planId") String planId,
        @JsonProperty("customer") String customer,
        @JsonProperty("profile") String profile,
        @JsonProperty("profileVersion") String profileVersion,
        @JsonProperty("preset") String preset,
        @JsonProperty("planStart") String planStart,
        @JsonProperty("planEndExclusive") String planEndExclusive
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VehicleDto(
        @JsonProperty("id") String id,
        @JsonProperty("sizeFeatureCode") String sizeFeatureCode,
        @JsonProperty("capabilities") Set<String> capabilities,
        @JsonProperty("vehicleZoneIds") Set<String> vehicleZoneIds,
        @JsonProperty("ownership") String ownership,
        @JsonProperty("speedKmH") String speedKmH,
        @JsonProperty("oneway") Boolean oneway,
        @JsonProperty("singleRoundtrip") Boolean singleRoundtrip,
        @JsonProperty("waitPolicy") String waitPolicy,
        @JsonProperty("routeResourceLimit") String routeResourceLimit
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocationDto(
        @JsonProperty("id") String id,
        @JsonProperty("zone") String zone
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServiceVisitDto(
        @JsonProperty("locationId") String locationId,
        @JsonProperty("windowOpen") String windowOpen,
        @JsonProperty("windowCloseInclusive") String windowCloseInclusive,
        @JsonProperty("durationSeconds") String durationSeconds,
        @JsonProperty("reqDate") String reqDate,
        @JsonProperty("zone") String zone
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemDto(
        @JsonProperty("weightDecimal") String weightDecimal,
        @JsonProperty("volumeDecimal") String volumeDecimal,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("itemTaskTimeSeconds") String itemTaskTimeSeconds
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompatibilityDto(
        @JsonProperty("allowedVehicleSizes") List<String> allowedVehicleSizes,
        @JsonProperty("requiredVehicleCapabilities") Set<String> requiredVehicleCapabilities
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExtensionInputDto(
        @JsonProperty("typeUri") String typeUri,
        @JsonProperty("payloadJson") String payloadJson
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RequestDto(
        @JsonProperty("id") String id,
        @JsonProperty("servicePattern") String servicePattern,
        @JsonProperty("pickup") ServiceVisitDto pickup,
        @JsonProperty("delivery") ServiceVisitDto delivery,
        @JsonProperty("items") List<ItemDto> items,
        @JsonProperty("compatibility") CompatibilityDto compatibility,
        @JsonProperty("mandatoryDeclaration") Boolean mandatoryDeclaration,
        @JsonProperty("extensionInput") ExtensionInputDto extensionInput,
        @JsonProperty("taskTimeSeconds") String taskTimeSeconds,
        @JsonProperty("taskTime") String taskTime,
        @JsonProperty("rawCustomerExtension") JsonNode rawCustomerExtension
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TravelCostDto(
        @JsonProperty("from") String from,
        @JsonProperty("to") String to,
        @JsonProperty("durationSeconds") String durationSeconds,
        @JsonProperty("distanceMeters") String distanceMeters
    ) {}
}
