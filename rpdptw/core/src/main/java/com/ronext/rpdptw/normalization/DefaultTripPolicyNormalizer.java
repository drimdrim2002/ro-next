package com.ronext.rpdptw.normalization;

public class DefaultTripPolicyNormalizer implements TripPolicyNormalizer {

    @Override
    public TripPolicy normalizeTripPolicy(Boolean oneway, Boolean singleRoundtrip, int rotation) {
        if (Boolean.TRUE.equals(oneway)) {
            return new TripPolicy.OneWay();
        }

        if (rotation != 0) {
            throw new TripReject(InputProblemCode.UNSUPPORTED_ROTATION, "Non-zero rotation is unsupported for non-oneway trips: " + rotation);
        }

        if (Boolean.TRUE.equals(singleRoundtrip)) {
            return new TripPolicy.SingleRoundTrip();
        }

        throw new TripReject(InputProblemCode.UNSUPPORTED_TRIP_POLICY, "Unsupported trip policy combination");
    }

    @Override
    public DepotWaitPolicy normalizeWaitPolicy(String waitPolicyStr) {
        if (waitPolicyStr == null) {
            throw new TripReject(InputProblemCode.INVALID_WAIT_POLICY, "Depot wait policy must not be null");
        }

        String trimmed = waitPolicyStr.trim();
        if ("NONE".equals(trimmed)) {
            return DepotWaitPolicy.NONE;
        } else if ("ALLOWED".equals(trimmed)) {
            return DepotWaitPolicy.ALLOWED;
        }

        throw new TripReject(InputProblemCode.INVALID_WAIT_POLICY, "Invalid depot wait policy: " + waitPolicyStr);
    }
}
