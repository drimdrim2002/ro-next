package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class RouteBiddingConstructionTest {

    /** T30 — 같은 차량에 입찰이 몰려도 라운드당 1건만 수락 · 라운드 수 ≤ |requests| (X15). */
    @Test
    void eachRoundEachRouteAcceptsAtMostOne() {
        Problem problem = ConstructionFixtures.ring(5, 1, 30_000L, 1_000L);      // 차량 1대 — 매 라운드 전원이 V1에 입찰
        List<Integer> accepted = new ArrayList<>();
        Solution solution = new RouteBiddingConstruction().construct(problem, new DefaultProfile(), accepted);
        assertEquals(List.of(1, 1, 1, 1, 1), accepted);
        assertTrue(accepted.size() <= problem.requests().size());
        assertTrue(solution.bank().isEmpty());
        assertEquals(1, solution.routes().size());

        Problem two = ConstructionFixtures.ring(4, 2, 30_000L, 1_000L);          // 차량 2대 — 첫 라운드는 미사용 첫 차량(V1)에만 몰린다
        List<Integer> acceptedTwo = new ArrayList<>();
        new RouteBiddingConstruction().construct(two, new DefaultProfile(), acceptedTwo);
        assertEquals(1, acceptedTwo.getFirst());
        assertTrue(acceptedTwo.size() <= two.requests().size());
        assertTrue(acceptedTwo.stream().allMatch(count -> count <= two.vehicles().size()));
    }
}
