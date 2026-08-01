package com.ronext.rpdptw.normalization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TripPolicyNormalizerTest {

    private final TripPolicyNormalizer normalizer = new DefaultTripPolicyNormalizer();

    @Test
    void onewayIgnoresRotationButRecordsRawValue() {
        TripPolicy policy = normalizer.normalizeTripPolicy(true, false, 5);
        assertInstanceOf(TripPolicy.OneWay.class, policy);
    }

    @Test
    void acceptsSingleRoundtripZeroRotation() {
        TripPolicy policy = normalizer.normalizeTripPolicy(false, true, 0);
        assertInstanceOf(TripPolicy.SingleRoundTrip.class, policy);
    }

    @Test
    void rejectsNonOnewayRotation() {
        TripReject reject = assertThrows(TripReject.class, () ->
                normalizer.normalizeTripPolicy(false, true, 1)
        );
        assertEquals(InputProblemCode.UNSUPPORTED_ROTATION, reject.code());
    }

    @Test
    void normalizesExplicitDepotWaitPolicyWithoutFallback() {
        assertEquals(DepotWaitPolicy.NONE, normalizer.normalizeWaitPolicy("NONE"));
        assertEquals(DepotWaitPolicy.ALLOWED, normalizer.normalizeWaitPolicy("ALLOWED"));
    }

    @Test
    void rejectsUnknownDepotWaitPolicy() {
        TripReject reject = assertThrows(TripReject.class, () ->
                normalizer.normalizeWaitPolicy("UNKNOWN")
        );
        assertEquals(InputProblemCode.INVALID_WAIT_POLICY, reject.code());
    }
}
