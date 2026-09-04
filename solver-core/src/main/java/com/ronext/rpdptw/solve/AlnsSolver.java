package com.ronext.rpdptw.solve;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.AlnsRunStats.Termination;

/**
 * ALNS 본체 — 반복 루프·acceptance·종료·best 관리 (stage-04 §4.2·§4.5, Domain §9).
 * 순서는 구조 검사 → 정식 평가 → acceptance이고, 수락·best 비교의 권위는 Evaluator뿐이다.
 * Problem·profile은 읽기만 한다. 탐색 예산은 AlnsConfig에만 있다 (Domain §2.5.1).
 */
public final class AlnsSolver {

    private final AlnsConfig config;
    private final List<DestroyOperator> destroyOperators;
    private final List<RepairOperator> repairOperators;

    public AlnsSolver(AlnsConfig config,
                      List<DestroyOperator> destroyOperators,
                      List<RepairOperator> repairOperators) {
        this.config = Objects.requireNonNull(config, "config");
        this.destroyOperators = List.copyOf(destroyOperators);
        this.repairOperators = List.copyOf(repairOperators);
        if (this.destroyOperators.isEmpty() || this.repairOperators.isEmpty()) {
            throw new IllegalArgumentException("at least one destroy and one repair operator required");
        }
    }

    /** §2의 기본 연산자 5개 — destroy 3 + repair 2 (2026-09-04). */
    public static AlnsSolver withDefaults(AlnsConfig config) {
        return new AlnsSolver(
                config,
                List.of(new RandomRemoval(), new RouteRemoval(), new StringRemoval()),
                List.of(new GreedyInsertion(), new RegretInsertion()));
    }

    /**
     * 초기해(InitialSolutionBuilder.build) → ALNS 반복 → best 반환. profile은 인자다 — Problem에 담기지
     * 않으며, 호출자가 재검증에도 같은 인스턴스를 넘긴다 (Domain §8.4 MUST).
     * 빌더가 이미 구조 검사·정식 평가를 거쳤으므로 그 evaluation·score가 initialEvaluation·initialScore가 된다.
     */
    public AlnsResult solve(Problem problem, Profile profile) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        InitialSolutionResult initial = InitialSolutionBuilder.build(problem, profile);
        return run(problem, profile, initial.best(),
                new EvaluationResult.Feasible(initial.evaluation(), initial.score(), Map.of()));
    }

    /**
     * 초기해를 이미 가진 호출자용 (§3.2 오버로드) — 손 조립 해로 포트폴리오 없이 루프 전체를 시험한다.
     * StructureCheck 위반이면 예외, Evaluator Infeasible이면 예외 (§4.2-2). 예산·acceptance·종료는 동일.
     */
    public AlnsResult solve(Problem problem, Profile profile, Solution initial) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(initial, "initial");
        List<StructureViolation> violations = StructureCheck.check(problem, initial);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("initial solution has structure violations: " + violations);
        }
        EvaluationResult evaluated = Evaluator.evaluate(problem, profile, initial);
        if (!(evaluated instanceof EvaluationResult.Feasible feasible)) {
            throw new IllegalStateException("initial solution infeasible: " + evaluated);
        }
        return run(problem, profile, initial, feasible);
    }

    private AlnsResult run(Problem problem, Profile profile, Solution initial, EvaluationResult.Feasible initialEval) {
        long startNanos = System.nanoTime();
        long limitNanos = Math.multiplyExact(config.timeLimitSec(), 1_000_000_000L);
        long deadlineNanos = startNanos + limitNanos;
        RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(config.seed());
        AdaptiveWeights destroyWeights = weights(destroyOperators.size());
        AdaptiveWeights repairWeights = weights(repairOperators.size());

        Solution current = initial;
        long[] currentScore = initialEval.score();
        Solution best = initial;
        EvaluationResult.Feasible bestEval = initialEval;
        long[] bestScore = initialEval.score();

        long iterations = 0L;
        long accepted = 0L;
        long infeasibleDiscarded = 0L;
        long bestImproved = 0L;
        long lastBestStep = 0L;
        long lastBestNanos = startNanos;
        int requestCount = problem.requests().size();

        Termination termination;
        while (true) {
            long now = System.nanoTime();
            termination = terminated(now, deadlineNanos, iterations, lastBestStep, lastBestNanos);
            if (termination != null) {
                break;
            }
            iterations++;

            // a. q — 배정 0건이면 destroy 생략 (E3)
            int assigned = requestCount - current.bank().size();
            Solution draft = current;
            int destroyIndex = destroyWeights.select(rng);
            if (assigned > 0) {
                // 비율이 아니라 절대 개수다 (2026-09-04) — 적재 여유가 작은 입력에서 비율로 뽑으면 다시 못 넣는다
                int drawn = config.minDestroyCount()
                        + rng.nextInt(config.maxDestroyCount() - config.minDestroyCount() + 1);
                int q = Math.max(1, Math.min(drawn, assigned));
                draft = destroyOperators.get(destroyIndex).destroy(problem, current, q, rng);   // b
            }
            int repairIndex = repairWeights.select(rng);
            draft = repairOperators.get(repairIndex).repair(problem, profile, draft, rng);   // c

            // d. 구조 위반은 폐기가 아니라 예외 (N2)
            List<StructureViolation> violations = StructureCheck.check(problem, draft);
            if (!violations.isEmpty()) {
                throw new IllegalStateException(destroyOperators.get(destroyIndex).id() + "/"
                        + repairOperators.get(repairIndex).id() + " produced structure violations: " + violations);
            }
            // e. 정식 평가 — Infeasible은 폐기 + 카운트 (E10)
            EvaluationResult evaluated = Evaluator.evaluate(problem, profile, draft);
            if (!(evaluated instanceof EvaluationResult.Feasible feasible)) {
                infeasibleDiscarded++;
                continue;
            }
            long[] draftScore = feasible.score();

            // f. acceptance (§4.5) — 비교 부호와 감쇠 확률만
            int cmp = Scores.compare(draftScore, currentScore);
            boolean accept = cmp <= 0
                    || (leadingAxesEqual(draftScore, currentScore)
                            && rng.nextDouble() < worseAcceptProbability(now, startNanos, limitNanos));
            AdaptiveWeights.Outcome outcome = null;
            if (accept) {
                accepted++;
                current = draft;
                currentScore = draftScore;
                if (Scores.compare(draftScore, bestScore) < 0) {
                    best = draft;
                    bestEval = feasible;
                    bestScore = draftScore;
                    bestImproved++;
                    lastBestStep = iterations;
                    lastBestNanos = System.nanoTime();        // idle 리셋은 여기서만
                    outcome = AdaptiveWeights.Outcome.NEW_BEST;
                } else if (cmp < 0) {
                    outcome = AdaptiveWeights.Outcome.IMPROVED;
                } else if (cmp > 0) {
                    outcome = AdaptiveWeights.Outcome.ACCEPTED_WORSE;
                }
            }
            // g. 보상 + segment 진행
            if (outcome != null) {
                destroyWeights.reward(destroyIndex, outcome);
                repairWeights.reward(repairIndex, outcome);
            }
            destroyWeights.endIteration();
            repairWeights.endIteration();
        }

        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        return new AlnsResult(
                best,
                bestEval.evaluation(),
                bestScore,
                initialEval.evaluation(),
                initialEval.score(),
                new AlnsRunStats(iterations, accepted, infeasibleDiscarded, bestImproved, elapsedMillis, termination));
    }

    /** 종료 조건 4개 — 검사 순서대로 첫 번째 것 (E11b: TIME_LIMIT 우선). 없으면 null. */
    private Termination terminated(long now, long deadlineNanos, long iterations, long lastBestStep, long lastBestNanos) {
        if (now >= deadlineNanos) {
            return Termination.TIME_LIMIT;
        }
        if (config.maxSteps().isPresent() && iterations >= config.maxSteps().getAsLong()) {
            return Termination.MAX_STEPS;
        }
        if (config.idleSteps().isPresent() && iterations - lastBestStep >= config.idleSteps().getAsLong()) {
            return Termination.IDLE_STEPS;
        }
        if (config.idleSec().isPresent()
                && now - lastBestNanos >= Math.multiplyExact(config.idleSec().getAsLong(), 1_000_000_000L)) {
            return Termination.IDLE_TIME;
        }
        return null;
    }

    /**
     * 축 가드 (§4.5, 2026-09-04) — 앞 두 축(미배정·차량 수)이 동률일 때만 확률 수락한다. N1이 스칼라 Δ를
     * 금지해 확률이 평평하므로, 앞 축이 나빠진 draft를 받으면 best를 이길 수 없는 영역에 갇힌다.
     * 축이 1개뿐인 profile이면 그 하나만 본다 — 배열 길이를 가정하지 않는다.
     */
    private static boolean leadingAxesEqual(long[] draftScore, long[] currentScore) {
        int axes = Math.min(2, draftScore.length);
        for (int i = 0; i < axes; i++) {
            if (draftScore[i] != currentScore[i]) {
                return false;
            }
        }
        return true;
    }

    /** p(t) = worseAcceptStartProbability × (1 − 경과/한도) — timeLimitSec 기준 선형 감쇠 (§4.5). */
    private double worseAcceptProbability(long now, long startNanos, long limitNanos) {
        if (limitNanos <= 0L) {
            return 0.0;
        }
        double progress = Math.min(1.0, (double) (now - startNanos) / limitNanos);
        return config.worseAcceptStartProbability() * (1.0 - progress);
    }

    private AdaptiveWeights weights(int operatorCount) {
        return new AdaptiveWeights(
                operatorCount,
                config.weightDecay(),
                config.weightSegmentLength(),
                config.rewardNewBest(),
                config.rewardImproved(),
                config.rewardAcceptedWorse());
    }
}
