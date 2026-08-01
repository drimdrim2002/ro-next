package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.input.AdapterIdentity;

import java.util.Arrays;
import java.util.Objects;

public record ExternalInputDocument(
    byte[] rawBytes,
    AdapterIdentity declaredAdapterIdentity,
    String mediaType
) {
    public ExternalInputDocument {
        Objects.requireNonNull(rawBytes, "rawBytes must not be null");
        Objects.requireNonNull(declaredAdapterIdentity, "declaredAdapterIdentity must not be null");
        Objects.requireNonNull(mediaType, "mediaType must not be null");
        rawBytes = rawBytes.clone();
    }

    @Override
    public byte[] rawBytes() {
        return rawBytes.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalInputDocument that = (ExternalInputDocument) o;
        return Arrays.equals(rawBytes, that.rawBytes)
                && Objects.equals(declaredAdapterIdentity, that.declaredAdapterIdentity)
                && Objects.equals(mediaType, that.mediaType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(declaredAdapterIdentity, mediaType);
        result = 31 * result + Arrays.hashCode(rawBytes);
        return result;
    }
}
