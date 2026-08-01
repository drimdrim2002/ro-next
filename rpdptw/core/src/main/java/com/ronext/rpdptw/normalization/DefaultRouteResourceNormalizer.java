package com.ronext.rpdptw.normalization;

import java.math.BigInteger;
import java.util.Optional;

public class DefaultRouteResourceNormalizer implements RouteResourceNormalizer {

    @Override
    public Optional<Long> normalizeLimit(Optional<String> limitStr) {
        if (limitStr == null || limitStr.isEmpty()) {
            return Optional.empty();
        }

        String lexeme = limitStr.get().trim();
        if (lexeme.isEmpty()) {
            return Optional.empty();
        }

        if (lexeme.contains(".") || lexeme.contains("e") || lexeme.contains("E")) {
            throw new ResourceReject(InputProblemCode.INVALID_ROUTE_RESOURCE_LIMIT, "Fractional or exponential limit lexeme is not allowed: " + lexeme);
        }

        BigInteger bigInt;
        try {
            bigInt = new BigInteger(lexeme);
        } catch (NumberFormatException e) {
            throw new ResourceReject(InputProblemCode.INVALID_ROUTE_RESOURCE_LIMIT, "Invalid integer format for route limit: " + lexeme);
        }

        if (bigInt.compareTo(BigInteger.ZERO) < 0) {
            throw new ResourceReject(InputProblemCode.INVALID_ROUTE_RESOURCE_LIMIT, "Route resource limit cannot be negative: " + lexeme);
        }

        if (bigInt.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ResourceReject(InputProblemCode.ARITHMETIC_OVERFLOW, "Route resource limit exceeds maximum long value: " + lexeme);
        }

        return Optional.of(bigInt.longValue());
    }

    @Override
    public NormalizedRouteResourceLimits normalizeRouteResourceLimits(Optional<String> maxDurationStr, Optional<String> maxDistanceStr) {
        Optional<Long> maxDuration = normalizeLimit(maxDurationStr);
        Optional<Long> maxDistance = normalizeLimit(maxDistanceStr);
        return new NormalizedRouteResourceLimits(maxDuration, maxDistance);
    }
}
