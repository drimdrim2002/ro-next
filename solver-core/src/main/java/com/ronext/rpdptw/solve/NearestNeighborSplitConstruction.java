package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** H19 — 방향성 표 위 nearest-neighbor 순열 + Split (heuristics 문서 §5 H19). 좌표가 아예 필요 없다. */
public final class NearestNeighborSplitConstruction implements ConstructionHeuristic {

    static final String ID = "nearest-neighbor-split";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return problem.depots().size() != 1;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        return GiantTourSplit.split(problem, profile, nearestNeighborOrder(problem), InsertionSearch.vehiclesByCapacityDesc(problem));
    }

    /** 단일 depot에서 시작해 d(현재, r의 첫 방문 지점) 최소(동률 RequestId)를 잇고 현재 = r의 마지막 방문 지점. */
    static List<RequestId> nearestNeighborOrder(Problem problem) {
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        List<RequestId> order = new ArrayList<>(pending.size());
        LocationId current = problem.depots().getFirst().locationId();
        while (!pending.isEmpty()) {
            RequestId next = null;
            int best = 0;
            for (RequestId requestId : pending) {
                int d = problem.travel().distanceMeter(current, InsertionSearch.anchor(problem.request(requestId)).locationId());
                if (next == null || d < best) {
                    next = requestId;
                    best = d;
                }
            }
            order.add(next);
            pending.remove(next);
            current = InsertionSearch.lastSide(problem.request(next)).locationId();
        }
        return order;
    }
}
