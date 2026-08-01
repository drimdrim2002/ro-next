package com.ronext.rpdptw.input;

import java.util.Objects;

public record ApprovedTypedExtensionInput(
    String typeUri,
    String payloadJson
) {
    public ApprovedTypedExtensionInput {
        Objects.requireNonNull(typeUri, "typeUri must not be null");
        Objects.requireNonNull(payloadJson, "payloadJson must not be null");
    }
}
