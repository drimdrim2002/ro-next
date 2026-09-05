package com.ronext.rpdptw.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.input.PlanNormalizer;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.AlnsConfig;
import com.ronext.rpdptw.solve.AlnsResult;
import com.ronext.rpdptw.solve.AlnsRunStats;
import com.ronext.rpdptw.solve.AlnsSolver;
import com.ronext.rpdptw.solve.EvaluationResult;
import com.ronext.rpdptw.solve.Evaluator;
import com.ronext.rpdptw.solve.Solution;
import com.ronext.rpdptw.solve.StructureCheck;
import com.ronext.rpdptw.solve.ZoneQuotaBalancedFillConstruction;

import tools.jackson.databind.ObjectMapper;

/**
 * T12b — 실물 fixture 회귀 (stage-04-alns §7, 2026-09-04). H23의 해를 §3.2 오버로드에 넣어 돌린다
 * (포트폴리오 24개는 다시 돌리지 않는다 — 그 선택은 T44가 고정했다). 단언은 예외 없는 정상 종료(§4.3 개정의
 * 실증 — 개정 전에는 첫 반복에서 죽었다) · 미배정 0 · 차량 31 · 거리 < 초기해다.
 * 품질 판정선(Win 대비 몇 %)은 두지 않는다 — 그건 Stage 8이고 시간 한도에 따라 값이 달라진다.
 * 규약 JSON → PlanInput 매핑은 T44(WinPocFixtureTest)의 테스트 전용 매핑을 재사용한다.
 */
class WinPocAlnsTest {

    /** CI용 짧은 한도 — 실물 예산(600초)이 아니다. */
    private static final long TIME_LIMIT_SEC = 20L;

    @Test
    void alnsImprovesOverInitialOnRealFixture() throws Exception {
        Problem problem = Problem.freeze(new PlanNormalizer()
                .normalize(WinPocFixtureTest.toInput(new ObjectMapper().readTree(WinPocFixtureTest.FIXTURE.toFile()))));
        DefaultProfile profile = new DefaultProfile();
        Solution initial = new ZoneQuotaBalancedFillConstruction().construct(problem, profile);
        long[] initialScore = assertInstanceOf(
                EvaluationResult.Feasible.class, Evaluator.evaluate(problem, profile, initial)).score();

        AlnsResult result = AlnsSolver.withDefaults(AlnsConfig.defaults(1L, TIME_LIMIT_SEC))
                .solve(problem, profile, initial);
        AlnsRunStats stats = result.stats();

        System.out.printf("[T12b] initial=%s best=%s%n",
                Arrays.toString(initialScore), Arrays.toString(result.bestScore()));
        System.out.printf("[T12b] iterations=%d accepted=%d infeasibleDiscarded=%d bestImproved=%d elapsedMillis=%d termination=%s%n",
                stats.iterations(), stats.accepted(), stats.infeasibleDiscarded(), stats.bestImproved(),
                stats.elapsedMillis(), stats.termination());

        assertTrue(stats.iterations() > 0L);
        assertTrue(StructureCheck.check(problem, result.best()).isEmpty());
        assertEquals(0L, result.bestScore()[0]);
        assertEquals(31L, result.bestScore()[1]);
        assertTrue(result.bestScore()[2] < initialScore[2],
                "distance " + result.bestScore()[2] + " vs initial " + initialScore[2]);
    }
}
