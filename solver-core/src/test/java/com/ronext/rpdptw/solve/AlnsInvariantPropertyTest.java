package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/**
 * T2 — seed 20개 × 랜덤 연산자 시퀀스 200스텝. destroy 직후·repair 직후 각각 StructureCheck 위반 0이고,
 * 배정↔bank 이동이 Request 단위(부분 pair 이동 없음)임을 단언한다. PICKUP_DELIVERY 포함 문제와
 * 삼각부등식을 어기는 이동표로 수행하고, 연산자 목록에 StringRemoval을 포함한다 (2026-09-04).
 * jqwik 없이 seed 루프로 손수 만든다 (Stage 0 §4.2).
 */
class AlnsInvariantPropertyTest {

    private static final int SEEDS = 20;
    private static final int STEPS = 200;
    private static final Profile PROFILE = new DefaultProfile();

    @Test
    void structureHoldsUnderRandomSteps() {
        List<DestroyOperator> destroys = List.of(new RandomRemoval(), new RouteRemoval(), new StringRemoval());
        List<RepairOperator> repairs = List.of(new GreedyInsertion(), new RegretInsertion());
        // 셋째 문제는 삼각부등식을 어기는 이동표다 — §4.3의 "기존 경로 후보 제외" 경로를 밟는다 (N9)
        for (Problem problem : List.of(
                AlnsSolverTest.smallFixture(), ConstructionFixtures.mixed(), InsertionOperatorsTest.nonTriangleProblem())) {
            Set<RequestId> all = ConstructionFixtures.requestIds(problem);
            for (long seed = 0; seed < SEEDS; seed++) {
                RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
                Solution current = InitialSolutionBuilder.build(problem, PROFILE).best();
                for (int step = 0; step < STEPS; step++) {
                    String at = "seed " + seed + " step " + step;
                    Set<RequestId> before = assigned(problem, current);
                    int q = 1 + rng.nextInt(Math.max(1, before.size()));

                    Solution destroyed = destroys.get(rng.nextInt(destroys.size())).destroy(problem, current, q, rng);
                    assertTrue(StructureCheck.check(problem, destroyed).isEmpty(), at + " after destroy");
                    Set<RequestId> afterDestroy = assigned(problem, destroyed);
                    assertTrue(before.containsAll(afterDestroy), at + " destroy added assignments");
                    assertTrue(destroyed.bank().containsAll(current.bank()), at + " destroy dropped bank entries");
                    assertEquals(difference(before, afterDestroy), difference(destroyed.bank(), current.bank()), at + " destroy move");
                    assertXor(all, destroyed, afterDestroy, at + " after destroy");

                    Solution repaired = repairs.get(rng.nextInt(repairs.size())).repair(problem, PROFILE, destroyed, rng);
                    assertTrue(StructureCheck.check(problem, repaired).isEmpty(), at + " after repair");
                    Set<RequestId> afterRepair = assigned(problem, repaired);
                    assertTrue(afterRepair.containsAll(afterDestroy), at + " repair dropped assignments");
                    assertTrue(destroyed.bank().containsAll(repaired.bank()), at + " repair added bank entries");
                    assertEquals(difference(afterRepair, afterDestroy), difference(destroyed.bank(), repaired.bank()), at + " repair move");
                    assertXor(all, repaired, afterRepair, at + " after repair");

                    current = repaired;
                }
            }
        }
    }

    /** 경로 방문을 역참조한 배정 Request 집합 — pair가 완비됐는지는 StructureCheck가 본다. */
    private static Set<RequestId> assigned(Problem problem, Solution solution) {
        Set<RequestId> ids = new HashSet<>();
        for (Route route : solution.routes()) {
            for (NodeId nodeId : route.visits()) {
                ids.add(InsertionSearch.requestOf(problem, nodeId));
            }
        }
        return ids;
    }

    private static void assertXor(Set<RequestId> all, Solution solution, Set<RequestId> assigned, String at) {
        Set<RequestId> union = new HashSet<>(assigned);
        union.addAll(solution.bank());
        assertEquals(all, union, at + " union");
        assertTrue(difference(assigned, all).isEmpty(), at);
        Set<RequestId> both = new HashSet<>(assigned);
        both.retainAll(solution.bank());
        assertTrue(both.isEmpty(), at + " assigned and banked: " + both);
    }

    private static Set<RequestId> difference(Set<RequestId> a, Set<RequestId> b) {
        Set<RequestId> out = new HashSet<>(a);
        out.removeAll(b);
        return out;
    }
}
