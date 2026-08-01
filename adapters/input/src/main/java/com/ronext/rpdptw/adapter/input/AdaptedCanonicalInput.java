package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.RawInputDigest;

import java.util.Objects;

public record AdaptedCanonicalInput(
    CanonicalBusinessInput canonicalInput,
    RawInputDigest rawInputDigest
) {
    public AdaptedCanonicalInput {
        Objects.requireNonNull(canonicalInput, "canonicalInput must not be null");
        Objects.requireNonNull(rawInputDigest, "rawInputDigest must not be null");
    }
}
