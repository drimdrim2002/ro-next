package com.ronext.rpdptw.normalization;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteResourceNormalizerTest {

    private final RouteResourceNormalizer normalizer = new DefaultRouteResourceNormalizer();

    @Test
    void preservesMissingRouteLimitsAsTypedAbsence() {
        NormalizedRouteResourceLimits limits = normalizer.normalizeRouteResourceLimits(Optional.empty(), Optional.empty());
        assertTrue(limits.maxRouteDurationSeconds().isEmpty());
        assertTrue(limits.maxRouteDistanceMeters().isEmpty());

        Optional<Long> singleLimit = normalizer.normalizeLimit(Optional.empty());
        assertTrue(singleLimit.isEmpty());
    }

    @Test
    void normalizesVehicleAndGlobalRouteLimitsIndependently() {
        NormalizedRouteResourceLimits limits = normalizer.normalizeRouteResourceLimits(
                Optional.of("7200"),
                Optional.of("3600")
        );
        assertEquals(Optional.of(7200L), limits.maxRouteDurationSeconds());
        assertEquals(Optional.of(3600L), limits.maxRouteDistanceMeters());
    }

    @Test
    void rejectsNegativeOrFractionalRouteResourceLimit() {
        ResourceReject rejectNegative = assertThrows(ResourceReject.class, () ->
                normalizer.normalizeLimit(Optional.of("-100"))
        );
        assertEquals(InputProblemCode.INVALID_ROUTE_RESOURCE_LIMIT, rejectNegative.code());

        ResourceReject rejectFractional = assertThrows(ResourceReject.class, () ->
                normalizer.normalizeLimit(Optional.of("3600.5"))
        );
        assertEquals(InputProblemCode.INVALID_ROUTE_RESOURCE_LIMIT, rejectFractional.code());
    }

    @Test
    void detectsRouteResourceLimitOverflow() {
        ResourceReject rejectOverflow = assertThrows(ResourceReject.class, () ->
                normalizer.normalizeLimit(Optional.of("999999999999999999999999999999"))
        );
        assertEquals(InputProblemCode.ARITHMETIC_OVERFLOW, rejectOverflow.code());
    }
}
