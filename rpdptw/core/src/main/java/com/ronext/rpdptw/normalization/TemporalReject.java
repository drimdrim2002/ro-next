package com.ronext.rpdptw.normalization;

public class TemporalReject extends RuntimeException {
    private final InputProblemCode code;

    public TemporalReject(InputProblemCode code) {
        super(code != null ? code.name() : null);
        this.code = code;
    }

    public TemporalReject(InputProblemCode code, String message) {
        super(message);
        this.code = code;
    }

    public InputProblemCode code() {
        return code;
    }

    public InputProblemCode getCode() {
        return code;
    }
}
