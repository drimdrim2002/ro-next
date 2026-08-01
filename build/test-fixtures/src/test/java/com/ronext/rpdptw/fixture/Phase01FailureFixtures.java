package com.ronext.rpdptw.fixture;

public final class Phase01FailureFixtures {

    private Phase01FailureFixtures() {
    }

    public static String itemFirstDifference() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"0.0009\",\"volumeDecimal\":\"0.500\",\"quantity\":2,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String scaleBoundary() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"1.2349\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String integerLookingDecimal() {
        return new ExternalInputFixtureBuilder()
            .clearTravel()
            .addTravelJson("{\"from\":\"LOC-DEPOT\",\"to\":\"LOC-DELIVERY-1\",\"durationSeconds\":\"600.0\",\"distanceMeters\":\"5000\"}")
            .buildJson();
    }

    public static String quantityOverflow() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"9223372036854775.000\",\"volumeDecimal\":\"0.500\",\"quantity\":2147483647,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String sumOverflow() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"9223372036854775.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"},{\"weightDecimal\":\"9223372036854775.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String duplicateRequest() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"5.000\",\"volumeDecimal\":\"0.200\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String duplicateArc() {
        return new ExternalInputFixtureBuilder()
            .clearTravel()
            .addTravelJson("{\"from\":\"LOC-DEPOT\",\"to\":\"LOC-DELIVERY-1\",\"durationSeconds\":\"600\",\"distanceMeters\":\"5000\"}")
            .addTravelJson("{\"from\":\"LOC-DEPOT\",\"to\":\"LOC-DELIVERY-1\",\"durationSeconds\":\"600\",\"distanceMeters\":\"5000\"}")
            .buildJson();
    }

    public static String reqDateShape() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"PICKUP_DELIVERY\",\"pickup\":{\"locationId\":\"LOC-DEPOT\",\"windowOpen\":\"2026-08-01 08:00:00\",\"windowCloseInclusive\":\"2026-08-01 12:00:00\",\"durationSeconds\":\"300\",\"reqDate\":\"INVALID_DATE_FORMAT\"},\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String routeResourceInvalid() {
        return new ExternalInputFixtureBuilder()
            .clearVehicles()
            .addVehicleJson("{\"id\":\"VEH-1\",\"sizeFeatureCode\":\"MEDIUM\",\"capabilities\":[],\"vehicleZoneIds\":[],\"ownership\":\"DIRECT\",\"speedKmH\":\"60.0\",\"oneway\":true,\"singleRoundtrip\":false,\"routeResourceLimit\":\"-1\"}")
            .buildJson();
    }

    public static String unapprovedExtension() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]},\"unapprovedCustomField\":\"secretData\"}")
            .buildJson();
    }

    public static String piiRedaction() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"INVALID_SECONDS_ABC\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]},\"customerEmail\":\"user@secret-domain.com\",\"customerAddress\":\"123 Secret Street\"}")
            .buildJson();
    }

    public static String zoneConflict() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"PICKUP_DELIVERY\",\"pickup\":{\"locationId\":\"LOC-DEPOT\",\"windowOpen\":\"2026-08-01 08:00:00\",\"windowCloseInclusive\":\"2026-08-01 12:00:00\",\"durationSeconds\":\"300\",\"zone\":\"ZONE-A\"},\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\",\"zone\":\"ZONE-B\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String ownershipAbsent() {
        return new ExternalInputFixtureBuilder()
            .clearVehicles()
            .addVehicleJson("{\"id\":\"VEH-1\",\"sizeFeatureCode\":\"MEDIUM\",\"capabilities\":[],\"vehicleZoneIds\":[],\"speedKmH\":\"60.0\",\"oneway\":true,\"singleRoundtrip\":false}")
            .buildJson();
    }

    public static String speedAbsent() {
        return new ExternalInputFixtureBuilder()
            .clearVehicles()
            .addVehicleJson("{\"id\":\"VEH-1\",\"sizeFeatureCode\":\"MEDIUM\",\"capabilities\":[],\"vehicleZoneIds\":[],\"ownership\":\"DIRECT\",\"oneway\":true,\"singleRoundtrip\":false}")
            .buildJson();
    }

    public static String servicePatternOnly() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"LOGICAL_DELIVERY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String featureAllMix() {
        return new ExternalInputFixtureBuilder()
            .clearRequests()
            .addRequestJson("{\"id\":\"REQ-1\",\"servicePattern\":\"DELIVERY_ONLY\",\"delivery\":{\"locationId\":\"LOC-DELIVERY-1\",\"windowOpen\":\"2026-08-01 09:00:00\",\"windowCloseInclusive\":\"2026-08-01 17:00:00\",\"durationSeconds\":\"300\"},\"items\":[{\"weightDecimal\":\"10.000\",\"volumeDecimal\":\"0.500\",\"quantity\":1,\"itemTaskTimeSeconds\":\"0\"}],\"compatibility\":{\"allowedVehicleSizes\":[\"ALL\",\"MEDIUM\"],\"requiredVehicleCapabilities\":[]}}")
            .buildJson();
    }

    public static String decimalWinTravel() {
        return new ExternalInputFixtureBuilder()
            .clearTravel()
            .addTravelJson("{\"from\":\"LOC-DEPOT\",\"to\":\"LOC-DELIVERY-1\",\"durationSeconds\":\"600.5\",\"distanceMeters\":\"5000.75\"}")
            .buildJson();
    }
}
