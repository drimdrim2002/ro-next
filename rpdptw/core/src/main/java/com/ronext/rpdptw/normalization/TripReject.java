package com.ronext.rpdptw.normalization;

public class TripReject extends RuntimeException {
    private final InputProblemCode code;

    public TripReject(InputProblemCode code) {
        super(code != null ? code.name() : null);
        this.code = code;
    }

    public TripReject(InputProblemCode code, String message) {
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
