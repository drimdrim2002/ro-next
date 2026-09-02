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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class SavingsMergeConstructionTest {

    /** T21 — d(i,j) ≠ d(j,i)인 이동표에서 saving이 방향을 구분함 · 좌표로 거리를 재계산하지 않음. */
    @Test
    void usesDirectedMatrixOnly() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        // A→B는 싸고(1,000) B→A는 비싸다(30,000): saving(A,B) = 10,000 + 10,000 − 1,000 > 0,
        // saving(B,A) = 10,000 + 10,000 − 30,000 < 0. 좌표는 전부 같아 좌표로 재면 0이 된다.
        List<TravelEntry> travel = new ArrayList<>();
        travel.add(new TravelEntry(DEPOT, a, 10_000, 800));
        travel.add(new TravelEntry(a, DEPOT, 10_000, 800));
        travel.add(new TravelEntry(DEPOT, b, 10_000, 800));
        travel.add(new TravelEntry(b, DEPOT, 10_000, 800));
        travel.add(new TravelEntry(a, b, 1_000, 80));
        travel.add(new TravelEntry(b, a, 30_000, 2_400));
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(delivery("A", a, 1_000L), delivery("B", b, 1_000L)),
                List.of(vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.of(DEPOT)),
                        vehicle("V2", 30_000L, Optional.of(DEPOT), Optional.of(DEPOT))),
                locations(at(DEPOT, 37.0, 127.0), at(a, 37.0, 127.0), at(b, 37.0, 127.0)),
                travel);

        Solution solution = new SavingsMergeConstruction().construct(problem, new DefaultProfile());
        assertEquals(1, solution.routes().size());
        assertEquals(List.of(NodeId.delivery(new RequestId("A")), NodeId.delivery(new RequestId("B"))),
                solution.routes().getFirst().visits());
        EvaluationResult.Feasible evaluated = assertInstanceOf(
                EvaluationResult.Feasible.class, Evaluator.evaluate(problem, new DefaultProfile(), solution));
        assertEquals(10_000L + 1_000L + 10_000L, evaluated.evaluation().totalDistanceMeter());
    }
}
