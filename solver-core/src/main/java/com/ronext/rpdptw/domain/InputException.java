package com.ronext.rpdptw.domain;

import java.util.Objects;

public final class InputException extends RuntimeException {

    public enum Kind {
        INVALID_INPUT,
        UNSUPPORTED_INPUT
    }

    private final Kind kind;
    private final String field;

    public InputException(Kind kind, String field, String message) {
        super(message);
        this.kind = Objects.requireNonNull(kind, "kind");
        this.field = Objects.requireNonNull(field, "field");
    }

    public Kind kind() {
        return kind;
    }

    public String field() {
        return field;
    }
}
