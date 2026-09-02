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
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class SweepNextFitConstructionTest {

    private static final LocationId A = new LocationId("A");
    private static final LocationId B = new LocationId("B");
    private static final LocationId C = new LocationId("C");

    /** T22 — 좌표를 바꿔 각도 순서만 바꾸고 이동표는 고정 → 방문 순서는 바뀌되 보고된 거리는 표 값과 일치. */
    @Test
    void coordinatesOrderOnly() {
        // 이동표: 모든 arc 1,000m/80s (대칭·균일) — 좌표로 재면 절대 이 값이 안 나온다.
        List<TravelEntry> travel = new ArrayList<>();
        for (LocationId from : List.of(DEPOT, A, B, C)) {
            for (LocationId to : List.of(DEPOT, A, B, C)) {
                if (!from.equals(to)) {
                    travel.add(new TravelEntry(from, to, 1_000, 80));
                }
            }
        }
        Map<LocationId, Location> east = locations(
                at(DEPOT, 37.0, 127.0), at(A, 37.0, 127.1), at(B, 37.1, 127.0), at(C, 36.9, 127.0));   // θ: A=0, B=π/2, C=−π/2
        Map<LocationId, Location> flipped = locations(
                at(DEPOT, 37.0, 127.0), at(A, 36.9, 127.0), at(B, 37.0, 127.1), at(C, 37.1, 127.0));   // θ: A=−π/2, B=0, C=π/2

        Solution first = construct(east, travel);
        Solution second = construct(flipped, travel);
        assertEquals(1, first.routes().size());
        assertEquals(1, second.routes().size());
        // 균일 표라 모든 위치가 동률 → §4.1의 (VehicleId, 위치) 순으로 위치 0이 이긴다 = 각도 순서의 역순.
        // 각도 순서 C,A,B → 방문 B,A,C · 각도 순서 A,B,C → 방문 C,B,A. 좌표가 순서만 바꿨다는 증거다.
        assertEquals(nodes("B", "A", "C"), first.routes().getFirst().visits());
        assertEquals(nodes("C", "B", "A"), second.routes().getFirst().visits());
        assertNotEquals(first.routes().getFirst().visits(), second.routes().getFirst().visits());

        for (Solution solution : List.of(first, second)) {
            Problem problem = problem(east, travel);
            EvaluationResult.Feasible evaluated = assertInstanceOf(
                    EvaluationResult.Feasible.class, Evaluator.evaluate(problem, new DefaultProfile(), solution));
            assertEquals(3 * 1_000L, evaluated.evaluation().totalDistanceMeter());        // depot→3방문, endDepot 없음
        }
    }

    private static Solution construct(Map<LocationId, Location> locations, List<TravelEntry> travel) {
        return new SweepNextFitConstruction().construct(problem(locations, travel), new DefaultProfile());
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
            nodes.add(NodeId.delivery(new com.ronext.rpdptw.domain.RequestId(id)));
        }
        return nodes;
    }
}
