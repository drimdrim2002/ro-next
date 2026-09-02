package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class ConstrainedPathExtensionConstructionTest {

    /** T33 — PD 요청이 […, p, d]로 통째로 붙음 — pickup만 붙는 상태가 존재하지 않음. */
    @Test
    void pairAppendedAtomically() {
        Problem problem = ConstructionFixtures.mixed();
        Solution solution = new ConstrainedPathExtensionConstruction().construct(problem, new DefaultProfile());
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        int pairsSeen = 0;
        for (Route route : solution.routes()) {
            for (Request request : problem.requests()) {
                if (request.pattern() != ServicePattern.PICKUP_DELIVERY) {
                    continue;
                }
                int p = route.visits().indexOf(request.pickup().orElseThrow().nodeId());
                int d = route.visits().indexOf(request.delivery().orElseThrow().nodeId());
                if (p >= 0 || d >= 0) {
                    assertEquals(p + 1, d, request.id() + " delivery must directly follow pickup");
                    pairsSeen++;
                }
            }
        }
        assertFalse(pairsSeen == 0, "at least one PD pair must be routed");
    }
}
