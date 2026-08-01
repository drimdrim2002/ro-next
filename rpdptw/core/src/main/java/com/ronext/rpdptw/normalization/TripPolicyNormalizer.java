package com.ronext.rpdptw.normalization;

public interface TripPolicyNormalizer {
    TripPolicy normalizeTripPolicy(Boolean oneway, Boolean singleRoundtrip, int rotation);

    DepotWaitPolicy normalizeWaitPolicy(String waitPolicyStr);
}
