package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.problem.Problem;

/** 배정된 Request 중 무작위 q개 제거 (stage-04 §4.4). 후보는 RequestId 문자열 순으로 정렬한 뒤 rng를 적용한다 (N3). */
public final class RandomRemoval implements DestroyOperator {

    static final String ID = "random-removal";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng) {
        List<RequestId> assigned = assignedRequestIds(problem, current);
        int count = Math.min(removeCount, assigned.size());          // E3 clamp
        Solution result = current;
        // 부분 Fisher–Yates — 앞 count칸에 서로 다른 Request가 모인다.
        for (int i = 0; i < count; i++) {
            int j = i + rng.nextInt(assigned.size() - i);
            RequestId picked = assigned.get(j);
            assigned.set(j, assigned.get(i));
            assigned.set(i, picked);
            result = InsertionSearch.remove(problem, result, picked);   // pair 전체 + 빈 경로 제거 (E4)
        }
        return result;
    }

    /** 경로 방문을 problem.nodeRef로 역참조해 모은 배정 RequestId — 문자열 순. */
    static List<RequestId> assignedRequestIds(Problem problem, Solution current) {
        Set<RequestId> seen = new LinkedHashSet<>();
        for (Route route : current.routes()) {
            for (NodeId nodeId : route.visits()) {
                seen.add(InsertionSearch.requestOf(problem, nodeId));
            }
        }
        List<RequestId> ids = new ArrayList<>(seen);
        ids.sort(InsertionSearch.BY_REQUEST_ID);
        return ids;
    }
}
