package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;

import java.util.List;
import java.util.Optional;

public interface ServiceTimeNormalizer {
    long calculateServiceSeconds(CanonicalServiceInput service, Optional<String> orderTaskTime, List<CanonicalItemInput> items);

    long calculateServiceSeconds(String durationSecondsStr, Optional<String> orderTaskTime, List<CanonicalItemInput> items);
}
