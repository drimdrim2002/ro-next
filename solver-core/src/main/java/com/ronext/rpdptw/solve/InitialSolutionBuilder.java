package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.problem.Problem;

/**
 * 포트폴리오 실행기 — 24개를 우선순위 순으로 실행해 정식 평가로 최선 하나를 고른다 (heuristics 문서 §6).
 * 선택 권위는 Evaluator뿐이다. 동률이면 우선순위가 앞선 기법이 이긴다 (§7 X7).
 */
public final class InitialSolutionBuilder {

    static final String EMPTY_ID = "empty";

    private InitialSolutionBuilder() {}

    /** 우선순위 순으로 고정된 24개 — 기본 8 + 확장 14 + 실물 맞춤 2 (§5). */
    public static List<ConstructionHeuristic> defaults() {
        return List.of(
                new ScarcityRegret2Construction(),
                new UrgencyRegret3Construction(),
                new VehicleZoneFillConstruction(),
                new DeadlineSequentialConstruction(),
                new FarthestSeedSequentialConstruction(),
                new ZoneFirstFfdConstruction(),
                new SavingsMergeConstruction(),
                new SweepNextFitConstruction(),
                new FeasibleRoutesRegretMConstruction(),
                new ChainSeedRegretMConstruction(),
                new I1SavingsSequentialConstruction(),
                new RouteBiddingConstruction(),
                new SlackPreservingSequentialConstruction(),
                new VehicleFillRemainingRegretConstruction(),
                new WeakestFitDecreasingConstruction(),
                new ConstrainedPathExtensionConstruction(),
                new FarthestSeedGlobalCheapestConstruction(),
                new HilbertSplitConstruction(),
                new NearestNeighborSplitConstruction(),
                new GapSweepBidirectionalConstruction(),
                new SpatiotemporalClusterConstruction(),
                new SqueakyWheelSequentialConstruction(),
                new ZoneQuotaBalancedFillConstruction(),
                new ZoneQuotaSubsetFillConstruction());

    }

    /**
     * 24개를 순서대로 실행해 정식 평가로 최선 하나를 고른다. 기권한 기법은 건너뛴다.
     * 전원 기권이면 빈 해(전 Request bank)를 반환한다. profile은 인자다 — Problem에 담기지 않으며,
     * 호출자가 탐색·재검증에 같은 인스턴스를 넘긴다 (Domain §8.4 MUST).
     */
    public static InitialSolutionResult build(Problem problem, Profile profile) {
        return build(problem, profile, defaults());
    }

    public static InitialSolutionResult build(
            Problem problem, Profile profile, List<ConstructionHeuristic> heuristics) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(heuristics, "heuristics");

        List<ConstructionOutcome> outcomes = new ArrayList<>(heuristics.size());
        Solution best = null;
        String bestId = null;
        EvaluationResult.Feasible bestResult = null;
        for (ConstructionHeuristic heuristic : heuristics) {
            if (heuristic.abstains(problem)) {
                outcomes.add(new ConstructionOutcome(
                        heuristic.id(), ConstructionOutcome.Status.ABSTAINED, OptionalInt.empty(), Optional.empty(), 0L));
                continue;
            }
            long started = System.nanoTime();
            Solution solution = heuristic.construct(problem, profile);
            long elapsedMillis = (System.nanoTime() - started) / 1_000_000L;

            List<StructureViolation> violations = StructureCheck.check(problem, solution);
            if (!violations.isEmpty()) {
                throw new IllegalStateException(heuristic.id() + " produced structure violations: " + violations);
            }
            EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
            if (!(evaluated instanceof EvaluationResult.Feasible feasible)) {
                throw new IllegalStateException(heuristic.id() + " produced infeasible solution: " + evaluated);
            }
            outcomes.add(new ConstructionOutcome(
                    heuristic.id(),
                    ConstructionOutcome.Status.BUILT,
                    OptionalInt.of(solution.bank().size()),
                    Optional.of(feasible.score()),
                    elapsedMillis));
            // 엄격히 작을 때만 교체 — 동률은 앞선 기법이 이긴다 (§6-4).
            if (bestResult == null || Scores.compare(feasible.score(), bestResult.score()) < 0) {
                best = solution;
                bestId = heuristic.id();
                bestResult = feasible;
            }
        }

        if (best == null) {
            Solution empty = InsertionSearch.emptySolution(problem);
            EvaluationResult evaluated = Evaluator.evaluate(problem, profile, empty);
            if (!(evaluated instanceof EvaluationResult.Feasible feasible)) {
                throw new IllegalStateException("empty solution infeasible — profile hard must not reject the empty solution: " + evaluated);
            }
            return new InitialSolutionResult(empty, EMPTY_ID, feasible.evaluation(), feasible.score(), outcomes);
        }
        return new InitialSolutionResult(best, bestId, bestResult.evaluation(), bestResult.score(), outcomes);
    }
}
