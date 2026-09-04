package com.ronext.rpdptw.solve;

import java.util.Objects;
import java.util.OptionalLong;

/**
 * 탐색 예산(Domain §2.5.1 — Problem에 담기지 않는 값들) + 알고리즘 튜닝(전부 재량, stage-04 §3.3).
 * timeLimitSec은 이미 결정된 값으로 들어온다 — 출처(wire·application.yml)는 core가 모른다.
 */
public record AlnsConfig(
        long timeLimitSec,
        OptionalLong maxSteps,
        OptionalLong idleSteps,
        OptionalLong idleSec,
        long seed,
        int minDestroyCount,
        int maxDestroyCount,
        double worseAcceptStartProbability,
        int weightSegmentLength,
        double weightDecay,
        int rewardNewBest,
        int rewardImproved,
        int rewardAcceptedWorse) {

    public AlnsConfig {
        Objects.requireNonNull(maxSteps, "maxSteps");
        Objects.requireNonNull(idleSteps, "idleSteps");
        Objects.requireNonNull(idleSec, "idleSec");
        if (timeLimitSec < 0L) {
            throw new IllegalArgumentException("timeLimitSec must be >= 0: " + timeLimitSec);
        }
        if (minDestroyCount < 1) {
            throw new IllegalArgumentException("minDestroyCount must be >= 1: " + minDestroyCount);
        }
        if (maxDestroyCount < minDestroyCount) {
            throw new IllegalArgumentException(
                    "maxDestroyCount must be >= minDestroyCount: " + minDestroyCount + ", " + maxDestroyCount);
        }
        if (!(0.0 <= worseAcceptStartProbability && worseAcceptStartProbability <= 1.0)) {
            throw new IllegalArgumentException("worseAcceptStartProbability must be in [0, 1]: " + worseAcceptStartProbability);
        }
        if (weightSegmentLength < 1) {
            throw new IllegalArgumentException("weightSegmentLength must be >= 1: " + weightSegmentLength);
        }
        if (!(0.0 <= weightDecay && weightDecay <= 1.0)) {
            throw new IllegalArgumentException("weightDecay must be in [0, 1]: " + weightDecay);
        }
    }

    /** 재량 기본값 (§3.3) — 시간 한도만 걸리고 step·idle 한도는 미적용. */
    public static AlnsConfig defaults(long seed, long timeLimitSec) {
        return new AlnsConfig(
                timeLimitSec,
                OptionalLong.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                seed,
                5,
                20,
                0.05,
                100,
                0.5,
                5,
                2,
                1);
    }
}
