package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class SqueakyWheelSequentialConstructionTest {

    /** T37 — 같은 입력 두 번 → 같은 라운드 전개·같은 best · priority 불변 시 조기 종료 (X18). */
    @Test
    void fixedRoundsDeterministicBlame() {
        // 전원 한 경로에 들어간다 → bank 없음·중앙값 미만 경로 없음 → priority 불변 → 1라운드 만에 종료
        Problem settled = ConstructionFixtures.ring(3, 1, 30_000L, 10_000L);
        List<Map<RequestId, Integer>> trace = new ArrayList<>();
        Solution solution = new SqueakyWheelSequentialConstruction().construct(settled, new DefaultProfile(), trace);
        assertEquals(1, trace.size());
        assertTrue(solution.bank().isEmpty());

        // 차량 1대에 5건(용량 3건) → 매 라운드 bank가 남아 priority가 계속 오른다 → R=5 라운드 전부
        Problem crowded = ConstructionFixtures.ring(5, 1, 30_000L, 10_000L);
        List<Map<RequestId, Integer>> first = new ArrayList<>();
        List<Map<RequestId, Integer>> second = new ArrayList<>();
        Solution a = new SqueakyWheelSequentialConstruction().construct(crowded, new DefaultProfile(), first);
        Solution b = new SqueakyWheelSequentialConstruction().construct(crowded, new DefaultProfile(), second);
        assertEquals(SqueakyWheelSequentialConstruction.ROUNDS, first.size());
        assertEquals(first, second);
        assertEquals(a, b);
        assertEquals(2, a.bank().size());
        for (int round = 1; round < first.size(); round++) {                    // blame은 단조 — 줄어드는 priority가 없다
            for (RequestId requestId : first.get(round).keySet()) {
                assertTrue(first.get(round).get(requestId) >= first.get(round - 1).get(requestId));
            }
        }
    }
}
