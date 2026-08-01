package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.adapter.input.AdaptedCanonicalInput;

public interface CanonicalInputNormalizer {
    NormalizationResult normalize(AdaptedCanonicalInput adapted, NormalizationPolicySnapshot policy);
}
