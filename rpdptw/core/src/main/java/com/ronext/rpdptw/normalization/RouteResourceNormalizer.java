package com.ronext.rpdptw.normalization;

import java.util.Optional;

public interface RouteResourceNormalizer {
    Optional<Long> normalizeLimit(Optional<String> limitStr);

    NormalizedRouteResourceLimits normalizeRouteResourceLimits(Optional<String> maxDurationStr, Optional<String> maxDistanceStr);
}
