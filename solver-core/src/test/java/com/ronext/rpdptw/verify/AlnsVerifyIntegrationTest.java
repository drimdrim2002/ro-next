package com.ronext.rpdptw.verify;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.AlnsConfig;
import com.ronext.rpdptw.solve.AlnsResult;
import com.ronext.rpdptw.solve.AlnsSolver;
import com.ronext.rpdptw.solve.Route;

/**
 * stage-05 §8 T8 — 탐색이 내놓은 best는 언제나 캐시 없는 재검증을 통과하고,
 * 탐색이 보고한 평가·score가 재계산본과 같다 (Domain §10 전체 흐름의 실증).
 */
class AlnsVerifyIntegrationTest {

    @Test
    void alnsBestAlwaysVerifies() {
        Problem problem = VerifyFixtures.alnsFixture();
        for (long seed : new long[] {1L, 7L, 42L, 99L}) {
            Profile profile = new DefaultProfile();      // 탐색·재검증에 같은 인스턴스 (Domain §8.4)
            AlnsResult result = AlnsSolver.withDefaults(steps(300L, seed)).solve(problem, profile);

            Map<VehicleId, List<NodeId>> routes = new LinkedHashMap<>();
            for (Route route : result.best().routes()) {
                routes.put(route.vehicleId(), route.visits());
            }
            VerificationResult verification = SolutionVerifier.verify(
                    problem, profile, routes, result.best().bank(),
                    result.bestEvaluation(), result.bestScore());

            VerificationResult.Pass pass = assertInstanceOf(
                    VerificationResult.Pass.class, verification, "seed " + seed + ": " + verification);
            assertEquals(result.bestEvaluation(), pass.evaluation());
            assertArrayEquals(result.bestScore(), pass.score());
            assertEquals(result.best().bank(), pass.bank());
            assertEquals(routes.keySet(), pass.routeFacts().keySet());
            // 이 fixture는 전건 배정이 가능하다 — 빈 해를 통과시키는 공허한 검사가 아님을 못박는다
            assertEquals(0, pass.evaluation().unassignedCount());
            assertFalse(pass.routeFacts().isEmpty());
        }
    }

    private static AlnsConfig steps(long maxSteps, long seed) {
        return new AlnsConfig(60L, OptionalLong.of(maxSteps), OptionalLong.empty(), OptionalLong.empty(),
                seed, 5, 20, 0.05, 100, 0.5, 5, 2, 1);
    }
}
