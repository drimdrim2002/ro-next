package com.ronext.rpdptw.solve;

import java.util.Arrays;
import java.util.Objects;

import com.ronext.rpdptw.eval.Evaluation;

/**
 * 탐색 산출 — best + best 승격 시점의 정식 평가(③)·score(④) + 초기해의 같은 값 + 통계 (stage-04 §3.2).
 * bestEvaluation·bestScore는 마지막에 새로 계산한 값이 아니라 승격 시점의 평가를 그대로 보관한 것이다 —
 * 재검증(Stage 5)이 이 둘을 대조한다. long[] 필드 때문에 equals로 비교하지 않는다.
 */
public record AlnsResult(
        Solution best,
        Evaluation bestEvaluation,
        long[] bestScore,
        Evaluation initialEvaluation,
        long[] initialScore,
        AlnsRunStats stats) {

    public AlnsResult {
        Objects.requireNonNull(best, "best");
        Objects.requireNonNull(bestEvaluation, "bestEvaluation");
        bestScore = Arrays.copyOf(Objects.requireNonNull(bestScore, "bestScore"), bestScore.length);
        Objects.requireNonNull(initialEvaluation, "initialEvaluation");
        initialScore = Arrays.copyOf(Objects.requireNonNull(initialScore, "initialScore"), initialScore.length);
        Objects.requireNonNull(stats, "stats");
    }

    @Override
    public long[] bestScore() {
        return Arrays.copyOf(bestScore, bestScore.length);
    }

    @Override
    public long[] initialScore() {
        return Arrays.copyOf(initialScore, initialScore.length);
    }
}
