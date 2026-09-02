package com.ronext.rpdptw.solve;

import java.util.Comparator;

import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H13 — 시간창 여유 손실 최소 순차 삽입, Lu & Dessouky (heuristics 문서 §5 H13). H4의 순서, 위치 비교자만 다르다. */
public final class SlackPreservingSequentialConstruction implements ConstructionHeuristic {

    static final String ID = "slack-preserving-sequential";

    /** (newRoute ASC, deltaForwardSlackSec ASC, Δ거리 ASC, Δ운행시간 ASC, VehicleId) — 위치 순은 안정 정렬이 지킨다. */
    static final Comparator<Candidate> POSITION_ORDER = Comparator.comparing(Candidate::newRoute)
            .thenComparingLong(Candidate::deltaForwardSlackSec)
            .thenComparingLong(Candidate::deltaDriveDistMeter)
            .thenComparingLong(Candidate::deltaRouteOperationalTimeSec)
            .thenComparing(Candidate::vehicleId, InsertionSearch.BY_VEHICLE_ID);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        return DeadlineSequentialConstruction.construct(
                problem, profile, DeadlineSequentialConstruction.deadlineOrder(problem), POSITION_ORDER, ID);
    }
}
