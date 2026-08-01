package com.ronext.rpdptw.normalization;

import java.util.List;
import java.util.Objects;

public record InputRejectionReport(List<InputProblem> problems) {
    public InputRejectionReport {
        Objects.requireNonNull(problems, "problems must not be null");
        problems = List.copyOf(problems);
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("rejection report requires at least one problem");
        }
    }
}
