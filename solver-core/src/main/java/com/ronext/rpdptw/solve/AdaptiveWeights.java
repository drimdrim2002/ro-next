package com.ronext.rpdptw.solve;

import java.util.Arrays;
import java.util.random.RandomGenerator;

/**
 * 연산자 룰렛 선택 + segment 가중치 갱신 (stage-04 §3.4). 초기 가중치 균등.
 * segment 경계에서 w ← (1−ρ)w + ρ·(score ÷ 사용 횟수), 그 segment에 미사용이면 w 유지.
 */
public final class AdaptiveWeights {

    public enum Outcome { NEW_BEST, IMPROVED, ACCEPTED_WORSE }

    private final double decay;
    private final int segmentLength;
    private final int rewardNewBest;
    private final int rewardImproved;
    private final int rewardAcceptedWorse;
    private final double[] weights;
    private final double[] scores;
    private final int[] uses;
    private int iterationsInSegment;

    public AdaptiveWeights(int operatorCount, double decay, int segmentLength,
                           int rewardNewBest, int rewardImproved, int rewardAcceptedWorse) {
        if (operatorCount < 1) {
            throw new IllegalArgumentException("operatorCount must be >= 1: " + operatorCount);
        }
        this.decay = decay;
        this.segmentLength = segmentLength;
        this.rewardNewBest = rewardNewBest;
        this.rewardImproved = rewardImproved;
        this.rewardAcceptedWorse = rewardAcceptedWorse;
        this.weights = new double[operatorCount];
        Arrays.fill(weights, 1.0);
        this.scores = new double[operatorCount];
        this.uses = new int[operatorCount];
    }

    /** 가중치 비례 룰렛. rng 호출은 정확히 한 번 — 같은 seed면 같은 선택 (N3). */
    public int select(RandomGenerator rng) {
        double total = 0.0;
        for (double weight : weights) {
            total += weight;
        }
        double pick = rng.nextDouble() * total;
        int chosen = weights.length - 1;
        double cumulative = 0.0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (pick < cumulative) {
                chosen = i;
                break;
            }
        }
        uses[chosen]++;
        return chosen;
    }

    public void reward(int operatorIndex, Outcome outcome) {
        scores[operatorIndex] += switch (outcome) {
            case NEW_BEST -> rewardNewBest;
            case IMPROVED -> rewardImproved;
            case ACCEPTED_WORSE -> rewardAcceptedWorse;
        };
    }

    public void endIteration() {
        if (++iterationsInSegment < segmentLength) {
            return;
        }
        iterationsInSegment = 0;
        for (int i = 0; i < weights.length; i++) {
            if (uses[i] > 0) {
                weights[i] = (1.0 - decay) * weights[i] + decay * (scores[i] / uses[i]);
            }
            scores[i] = 0.0;
            uses[i] = 0;
        }
    }
}
