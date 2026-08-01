package com.ronext.rpdptw.input;

import java.util.Arrays;
import java.util.Objects;

public record RawInputDigest(byte[] sha256) {
    public RawInputDigest {
        Objects.requireNonNull(sha256, "sha256 must not be null");
        sha256 = sha256.clone();
    }

    @Override
    public byte[] sha256() {
        return sha256.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawInputDigest digest = (RawInputDigest) o;
        return Arrays.equals(sha256, digest.sha256);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(sha256);
    }

    @Override
    public String toString() {
        return "RawInputDigest[sha256=" + Arrays.toString(sha256) + "]";
    }
}
