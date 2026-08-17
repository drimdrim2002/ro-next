package com.ronext.rpdptw.problem;

public final class ProblemCreationException extends RuntimeException {

    public ProblemCreationException(String message) {
        super(message);
    }

    public ProblemCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
