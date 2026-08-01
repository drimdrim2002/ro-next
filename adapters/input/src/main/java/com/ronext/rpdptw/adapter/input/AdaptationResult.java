package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.normalization.InputRejectionReport;

public sealed interface AdaptationResult {
    record Accepted(AdaptedCanonicalInput input) implements AdaptationResult {}
    record Rejected(InputRejectionReport report) implements AdaptationResult {}
}
