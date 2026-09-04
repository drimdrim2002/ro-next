package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.AlnsRunStats.Termination;

/**
 * T11 — 규모 측정. Stage 2 T12·4-초기해 T25와 같은 합성 문제(장소 453·주문 452·차량 31·정차 28)로
 * AlnsSolver.solve 1회(포트폴리오 초기해 → ALNS) — 시간 한도 안에 정상 종료하고 반복·수락 수·elapsed를 출력한다.
 * 해의 품질·개선폭은 판정하지 않는다 (Stage 8).
 */
class AlnsScaleTest {

    /** 실물 예산(600초)이 아니라 측정용 짧은 한도 — 반복 수를 기록하는 것이 목적이다. */
    private static final long TIME_LIMIT_SEC = 20L;

    @Test
    void runsOnFullScaleSyntheticProblem() {
        Problem problem = InitialSolutionScaleTest.syntheticProblem();
        Profile profile = new DefaultProfile();

        long started = System.nanoTime();
        AlnsResult result = AlnsSolver.withDefaults(AlnsConfig.defaults(1L, TIME_LIMIT_SEC)).solve(problem, profile);
        long totalMillis = (System.nanoTime() - started) / 1_000_000L;
        AlnsRunStats stats = result.stats();

        System.out.printf("[T11] initial=%s best=%s%n", Arrays.toString(result.initialScore()), Arrays.toString(result.bestScore()));
        System.out.printf("[T11] iterations=%d accepted=%d infeasibleDiscarded=%d bestImproved=%d elapsedMillis=%d (loop) / %d ms 총(초기해 포함) termination=%s%n",
                stats.iterations(), stats.accepted(), stats.infeasibleDiscarded(), stats.bestImproved(),
                stats.elapsedMillis(), totalMillis, stats.termination());

        assertEquals(Termination.TIME_LIMIT, stats.termination());
        assertTrue(stats.iterations() > 0L);
        assertTrue(StructureCheck.check(problem, result.best()).isEmpty());
        assertTrue(Scores.compare(result.bestScore(), result.initialScore()) <= 0);
    }
}
