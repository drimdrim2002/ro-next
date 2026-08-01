package com.ronext.rpdptw.normalization;

class NumericReject extends RuntimeException {
    private final InputProblemCode code;

    NumericReject(InputProblemCode code) {
        super(code != null ? code.name() : null);
        this.code = code;
    }

    NumericReject(InputProblemCode code, String message) {
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
