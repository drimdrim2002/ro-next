package com.ronext.rpdptw.solve;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.ronext.rpdptw.eval.Evaluation;

/** 포트폴리오의 결과 — best 하나와 기법별 요약. long[] 필드 때문에 equals로 비교하지 않는다 (§7 X8). */
public record InitialSolutionResult(
        Solution best,
        String heuristicId,
        Evaluation evaluation,
        long[] score,
        List<ConstructionOutcome> outcomes) {

    public InitialSolutionResult {
        Objects.requireNonNull(best, "best");
        Objects.requireNonNull(heuristicId, "heuristicId");
        Objects.requireNonNull(evaluation, "evaluation");
        score = Arrays.copyOf(Objects.requireNonNull(score, "score"), score.length);
        outcomes = List.copyOf(outcomes);
    }

    @Override
    public long[] score() {
        return Arrays.copyOf(score, score.length);
    }
}
