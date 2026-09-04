package com.ronext.rpdptw.verify;

import java.time.LocalDateTime;
import java.util.Objects;

/** app이 아는 실행 사실 — 값 채움은 Stage 6의 일이다 (stage-05 §4.1·§9). */
public record RunStamp(String inputKey,
                       LocalDateTime receivedAt,
                       LocalDateTime startedAt,
                       LocalDateTime finishedAt,
                       SolveResult.SearchBudget searchBudget) {

    public RunStamp {
        Objects.requireNonNull(inputKey, "inputKey");
        Objects.requireNonNull(receivedAt, "receivedAt");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(finishedAt, "finishedAt");
        Objects.requireNonNull(searchBudget, "searchBudget");
    }
}
