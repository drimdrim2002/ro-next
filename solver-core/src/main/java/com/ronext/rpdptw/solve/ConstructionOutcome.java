package com.ronext.rpdptw.solve;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/** 기법 1개의 실행 결과 — 로그·실험용(22 → 4 축소 판단의 입력, heuristics 문서 §9). equals로 비교하지 않는다. */
public record ConstructionOutcome(
        String heuristicId,
        Status status,
        OptionalInt unassignedCount,
        Optional<long[]> score,
        long elapsedMillis) {

    public ConstructionOutcome {
        Objects.requireNonNull(heuristicId, "heuristicId");
        Objects.requireNonNull(status, "status");
        unassignedCount = Objects.requireNonNull(unassignedCount, "unassignedCount");
        score = Objects.requireNonNull(score, "score").map(s -> Arrays.copyOf(s, s.length));
    }

    public enum Status { BUILT, ABSTAINED }
}
