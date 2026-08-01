package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ExternalRequestId;
import com.ronext.rpdptw.input.ServicePattern;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityNormalizerTest {

    private final CompatibilityNormalizer normalizer = new DefaultCompatibilityNormalizer();

    @Test
    void keepsFreeFormSizeCodeCaseSensitive() {
        AllowedVehicleSizes allowed = normalizer.normalizeAllowedVehicleSizes(List.of("t1", "T1"));
        assertInstanceOf(AllowedVehicleSizes.Concrete.class, allowed);
        AllowedVehicleSizes.Concrete concrete = (AllowedVehicleSizes.Concrete) allowed;
        assertEquals(2, concrete.codes().size());
        assertEquals(new SizeFeatureCode("t1"), concrete.codes().get(0));
        assertEquals(new SizeFeatureCode("T1"), concrete.codes().get(1));

        SizeFeatureCode vehicleSizeLower = normalizer.normalizeVehicleSizeCode("t1");
        SizeFeatureCode vehicleSizeUpper = normalizer.normalizeVehicleSizeCode("T1");
        assertFalse(vehicleSizeLower.equals(vehicleSizeUpper));
    }

    @Test
    void acceptsOnlyExactAllAlternative() {
        AllowedVehicleSizes allowedExactAll = normalizer.normalizeAllowedVehicleSizes(List.of("ALL"));
        assertInstanceOf(AllowedVehicleSizes.All.class, allowedExactAll);

        AllowedVehicleSizes allowedEmpty = normalizer.normalizeAllowedVehicleSizes(List.of());
        assertInstanceOf(AllowedVehicleSizes.All.class, allowedEmpty);

        AllowedVehicleSizes allowedNull = normalizer.normalizeAllowedVehicleSizes(null);
        assertInstanceOf(AllowedVehicleSizes.All.class, allowedNull);

        AllowedVehicleSizes allowedLowerAll = normalizer.normalizeAllowedVehicleSizes(List.of("all"));
        assertInstanceOf(AllowedVehicleSizes.Concrete.class, allowedLowerAll);
        AllowedVehicleSizes.Concrete concrete = (AllowedVehicleSizes.Concrete) allowedLowerAll;
        assertEquals(List.of(new SizeFeatureCode("all")), concrete.codes());
    }

    @Test
    void rejectsMixedAllAndConcreteCode() {
        CompatibilityReject reject = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeAllowedVehicleSizes(List.of("ALL", "T1"))
        );
        assertEquals(InputProblemCode.INVALID_FEATURE_LIST, reject.code());

        CompatibilityReject reject2 = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeAllowedVehicleSizes(List.of("T1", "ALL"))
        );
        assertEquals(InputProblemCode.INVALID_FEATURE_LIST, reject2.code());
    }

    @Test
    void rejectsVehicleAllOrBlank() {
        CompatibilityReject rejectAll = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeVehicleSizeCode("ALL")
        );
        assertEquals(InputProblemCode.INVALID_VEHICLE_FEATURE, rejectAll.code());

        CompatibilityReject rejectBlank = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeVehicleSizeCode("   ")
        );
        assertEquals(InputProblemCode.INVALID_VEHICLE_FEATURE, rejectBlank.code());

        CompatibilityReject rejectNull = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeVehicleSizeCode(null)
        );
        assertEquals(InputProblemCode.INVALID_VEHICLE_FEATURE, rejectNull.code());
    }

    @Test
    void preservesVehicleMultiZoneIds() {
        VehicleZoneSet zones = normalizer.normalizeVehicleZones(List.of("ZONE_B", "ZONE_A"));
        assertInstanceOf(VehicleZoneSet.Restricted.class, zones);
        VehicleZoneSet.Restricted restricted = (VehicleZoneSet.Restricted) zones;
        SortedSet<ZoneCode> expected = new TreeSet<>(Set.of(new ZoneCode("ZONE_A"), new ZoneCode("ZONE_B")));
        assertEquals(expected, restricted.zoneIds());
    }

    @Test
    void normalizesMissingVehicleZonesToAllZones() {
        VehicleZoneSet zonesNull = normalizer.normalizeVehicleZones(null);
        assertInstanceOf(VehicleZoneSet.AllZones.class, zonesNull);

        VehicleZoneSet zonesEmpty = normalizer.normalizeVehicleZones(Set.of());
        assertInstanceOf(VehicleZoneSet.AllZones.class, zonesEmpty);
    }

    @Test
    void preservesNoEligibleVehicleAsValidNormalizedFact() {
        ExternalRequestId reqId = new ExternalRequestId("REQ_001");
        NormalizedRequestSpec requestSpec = new NormalizedRequestSpec(
                reqId,
                new AllowedVehicleSizes.Concrete(List.of(new SizeFeatureCode("LARGE"))),
                Set.of(),
                Set.of()
        );
        NormalizedVehicleSpec vehicleSpec = new NormalizedVehicleSpec(
                new SizeFeatureCode("SMALL"),
                Set.of(),
                new VehicleZoneSet.AllZones()
        );

        Optional<StaticUnassignabilityFact> fact = normalizer.checkStaticUnassignability(
                requestSpec,
                List.of(vehicleSpec)
        );

        assertTrue(fact.isPresent());
        assertEquals(reqId, fact.get().requestId());
    }

    @Test
    void preservesConflictingConcretePickupDeliveryZonesAsStaticUnassignabilityFact() {
        ExternalRequestId reqId = new ExternalRequestId("REQ_002");
        NormalizedRequestSpec requestSpec = new NormalizedRequestSpec(
                reqId,
                new AllowedVehicleSizes.All(),
                Set.of(),
                Set.of(new ZoneCode("ZONE_A"), new ZoneCode("ZONE_B"))
        );

        SortedSet<ZoneCode> zonesV1 = new TreeSet<>(Set.of(new ZoneCode("ZONE_A")));
        NormalizedVehicleSpec vehicle1 = new NormalizedVehicleSpec(
                new SizeFeatureCode("MEDIUM"),
                Set.of(),
                new VehicleZoneSet.Restricted(zonesV1)
        );

        SortedSet<ZoneCode> zonesV2 = new TreeSet<>(Set.of(new ZoneCode("ZONE_B")));
        NormalizedVehicleSpec vehicle2 = new NormalizedVehicleSpec(
                new SizeFeatureCode("MEDIUM"),
                Set.of(),
                new VehicleZoneSet.Restricted(zonesV2)
        );

        Optional<StaticUnassignabilityFact> fact = normalizer.checkStaticUnassignability(
                requestSpec,
                List.of(vehicle1, vehicle2)
        );

        assertTrue(fact.isPresent());
        assertEquals(reqId, fact.get().requestId());
    }

    @Test
    void normalizesOwnershipPresentOrAbsentNeverSilentDirect() {
        assertEquals(new VehicleOwnership.Absent(), normalizer.normalizeOwnership(Optional.empty()));
        assertEquals(new VehicleOwnership.Absent(), normalizer.normalizeOwnership((String) null));
        assertEquals(new VehicleOwnership.Absent(), normalizer.normalizeOwnership(""));

        assertEquals(new VehicleOwnership.Direct(), normalizer.normalizeOwnership(Optional.of("DIRECT")));
        assertEquals(new VehicleOwnership.Direct(), normalizer.normalizeOwnership("DIRECT"));

        assertEquals(new VehicleOwnership.Lease(), normalizer.normalizeOwnership(Optional.of("LEASE")));
        assertEquals(new VehicleOwnership.Lease(), normalizer.normalizeOwnership("LEASE"));

        CompatibilityReject reject = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeOwnership("OTHER")
        );
        assertEquals(InputProblemCode.INVALID_OWNERSHIP, reject.code());
    }

    @Test
    void preservesAbsentSpeedWithoutFilling45() {
        assertEquals(new VehicleSpeedInput.Absent(), normalizer.normalizeSpeed(Optional.empty()));
        assertEquals(new VehicleSpeedInput.Absent(), normalizer.normalizeSpeed((String) null));
        assertEquals(new VehicleSpeedInput.Absent(), normalizer.normalizeSpeed(""));

        VehicleSpeedInput present = normalizer.normalizeSpeed(Optional.of("45.0"));
        assertInstanceOf(VehicleSpeedInput.PresentKmH.class, present);
        assertEquals(45.0, ((VehicleSpeedInput.PresentKmH) present).value());
    }

    @Test
    void rejectsInvalidSpeed() {
        CompatibilityReject rejectZero = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeSpeed("0")
        );
        assertEquals(InputProblemCode.INVALID_SPEED, rejectZero.code());

        CompatibilityReject rejectNegative = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeSpeed("-10")
        );
        assertEquals(InputProblemCode.INVALID_SPEED, rejectNegative.code());

        CompatibilityReject rejectNaN = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeSpeed("NaN")
        );
        assertEquals(InputProblemCode.INVALID_SPEED, rejectNaN.code());

        CompatibilityReject rejectInf = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeSpeed("Infinity")
        );
        assertEquals(InputProblemCode.INVALID_SPEED, rejectInf.code());

        CompatibilityReject rejectString = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeSpeed("abc")
        );
        assertEquals(InputProblemCode.INVALID_SPEED, rejectString.code());
    }

    @Test
    void acceptsServicePatternDeliveryOnlyAndPickupDeliveryOnly() {
        assertEquals(ServicePattern.DELIVERY_ONLY, normalizer.normalizeServicePattern("DELIVERY_ONLY"));
        assertEquals(ServicePattern.PICKUP_DELIVERY, normalizer.normalizeServicePattern("PICKUP_DELIVERY"));

        CompatibilityReject reject = assertThrows(CompatibilityReject.class, () ->
                normalizer.normalizeServicePattern("INVALID_PATTERN")
        );
        assertEquals(InputProblemCode.INVALID_SERVICE_PATTERN, reject.code());
    }
}
