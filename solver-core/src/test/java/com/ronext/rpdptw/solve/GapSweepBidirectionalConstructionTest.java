package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class GapSweepBidirectionalConstructionTest {

    private static final LocationId A = new LocationId("A");
    private static final LocationId B = new LocationId("B");
    private static final LocationId C = new LocationId("C");

    /** T35 — 좌표 변경이 순서만 바꾸고 거리는 표 값과 일치(T22와 동형) · 양방향 동률이면 시계 방향. */
    @Test
    void coordinatesOrderOnlyAndDeterministicDirection() {
        List<TravelEntry> travel = new ArrayList<>();
        for (LocationId from : List.of(DEPOT, A, B, C)) {
            for (LocationId to : List.of(DEPOT, A, B, C)) {
                if (!from.equals(to)) {
                    travel.add(new TravelEntry(from, to, 1_000, 80));            // 균일 표 — 두 방향이 동률이 된다
                }
            }
        }
        // θ: A=0, B=π/2, C=−π/2 → 각도 순 C,A,B. 최대 간극은 B→C(π) → 제로각 뒤 = C부터. 시계 [C,A,B]·반시계 [C,B,A]
        Map<LocationId, Location> east = locations(
                at(DEPOT, 37.0, 127.0), at(A, 37.0, 127.1), at(B, 37.1, 127.0), at(C, 36.9, 127.0));
        Problem problem = problem(east, travel);
        assertEquals(List.of(new RequestId("C"), new RequestId("A"), new RequestId("B")),
                GapSweepBidirectionalConstruction.rotateToLargestGap(problem, DEPOT, SweepNextFitConstruction.angleOrder(problem, DEPOT)));

        Solution first = new GapSweepBidirectionalConstruction().construct(problem, new DefaultProfile());
        // 균일 표라 모든 위치가 동률 → 위치 0이 이긴다 = 삽입 순서의 역순. 시계 [C,A,B] → 방문 [B,A,C] (동률이면 시계)
        assertEquals(nodes("B", "A", "C"), first.routes().getFirst().visits());

        Map<LocationId, Location> flipped = locations(
                at(DEPOT, 37.0, 127.0), at(A, 36.9, 127.0), at(B, 37.0, 127.1), at(C, 37.1, 127.0));
        Solution second = new GapSweepBidirectionalConstruction().construct(problem(flipped, travel), new DefaultProfile());
        assertEquals(nodes("C", "B", "A"), second.routes().getFirst().visits());
        assertNotEquals(first.routes().getFirst().visits(), second.routes().getFirst().visits());

        for (Solution solution : List.of(first, second)) {
            EvaluationResult.Feasible evaluated = assertInstanceOf(
                    EvaluationResult.Feasible.class, Evaluator.evaluate(problem, new DefaultProfile(), solution));
            assertEquals(3 * 1_000L, evaluated.evaluation().totalDistanceMeter());
        }
    }

    private static Problem problem(Map<LocationId, Location> locations, List<TravelEntry> travel) {
        return freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(delivery("A", A, 1_000L), delivery("B", B, 1_000L), delivery("C", C, 1_000L)),
                List.of(vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.empty())),
                locations,
                travel);
    }

    private static List<NodeId> nodes(String... ids) {
        List<NodeId> nodes = new ArrayList<>();
        for (String id : ids) {
            nodes.add(NodeId.delivery(new RequestId(id)));
        }
        return nodes;
    }
}
