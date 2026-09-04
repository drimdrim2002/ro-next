package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.problem.Problem;

/**
 * 무작위 경로 하나를 통째로 제거 (stage-04 §4.4) — usedVehicleCount 축을 직접 흔든다.
 * removeCount는 무시한다 (힌트, E15).
 */
public final class RouteRemoval implements DestroyOperator {

    static final String ID = "route-removal";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng) {
        if (current.routes().isEmpty()) {
            return current;
        }
        int index = rng.nextInt(current.routes().size());
        List<Route> routes = new ArrayList<>(current.routes());
        Route removed = routes.remove(index);
        Set<RequestId> bank = new LinkedHashSet<>(current.bank());
        for (NodeId nodeId : removed.visits()) {
            bank.add(InsertionSearch.requestOf(problem, nodeId));
        }
        return new Solution(routes, bank);
    }
}
