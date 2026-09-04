package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

class InsertionOperatorsTest {

    private static final Profile PROFILE = new DefaultProfile();

    private static RandomGenerator rng(long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    /** T6 — PD 삽입 후보는 전부 i ≤ j (E7), greedy·regret 삽입 결과 경로에서 픽업 선행. */
    @Test
    void pairInsertionKeepsPickupFirst() {
        Problem problem = ConstructionFixtures.mixed();
        RequestId r2 = new RequestId("R2");
        RequestId r5 = new RequestId("R5");
        Solution empty = InsertionSearch.emptySolution(problem);

        // 기존 경로(R5 pair + R1)가 있는 해에 R2를 넣는 후보 전부
        Solution withRoute = new Solution(
                List.of(new Route(new VehicleId("V1"), List.of(NodeId.pickup(r5), NodeId.delivery(new RequestId("R1")), NodeId.delivery(r5)))),
                Set.of(r2, new RequestId("R3"), new RequestId("R4")));
        assertTrue(StructureCheck.check(problem, withRoute).isEmpty());
        List<Candidate> cands = InsertionSearch.candidates(problem, PROFILE, withRoute, r2);
        assertFalse(cands.isEmpty());
        for (Candidate candidate : cands) {
            assertTrue(candidate.visits().indexOf(NodeId.pickup(r2)) < candidate.visits().indexOf(NodeId.delivery(r2)), candidate.toString());
            if (!candidate.newRoute()) {
                assertTrue(candidate.visits().indexOf(NodeId.pickup(r5)) < candidate.visits().indexOf(NodeId.delivery(r5)), candidate.toString());
            }
        }

        for (RepairOperator repair : List.of(new GreedyInsertion(), new RegretInsertion())) {
            for (long seed = 0; seed < 5; seed++) {
                Solution repaired = repair.repair(problem, PROFILE, empty, rng(seed));
                assertTrue(StructureCheck.check(problem, repaired).isEmpty(), repair.id());
                for (Route route : repaired.routes()) {
                    for (RequestId pd : List.of(r2, r5)) {
                        int pickup = route.visits().indexOf(NodeId.pickup(pd));
                        int delivery = route.visits().indexOf(NodeId.delivery(pd));
                        assertEquals(pickup >= 0, delivery >= 0, repair.id() + " " + pd);
                        if (pickup >= 0) {
                            assertTrue(pickup < delivery, repair.id() + " " + route);
                        }
                    }
                }
            }
        }
    }

    /** T8 — 비호환 차량 후보 없음(E12) · 새 경로는 실제 미사용 VehicleId(§9.1) · 호환 0대는 bank 유지(E2). */
    @Test
    void usesOnlyCompatibleRealVehicles() {
        Problem problem = capabilityProblem();
        RequestId cold = new RequestId("COLD1");
        RequestId plain = new RequestId("PLAIN1");
        RequestId hot = new RequestId("HOT1");
        assertEquals(Set.of(new VehicleId("V2")), problem.compatibleVehicles(cold));
        assertEquals(Set.of(new VehicleId("V1"), new VehicleId("V2")), problem.compatibleVehicles(plain));
        assertEquals(Set.of(), problem.compatibleVehicles(hot));

        Solution empty = InsertionSearch.emptySolution(problem);
        List<Candidate> coldCands = InsertionSearch.candidates(problem, PROFILE, empty, cold);
        assertFalse(coldCands.isEmpty());
        for (Candidate candidate : coldCands) {
            assertEquals(new VehicleId("V2"), candidate.vehicleId());
        }
        assertTrue(InsertionSearch.candidates(problem, PROFILE, empty, hot).isEmpty());

        Set<VehicleId> realVehicles = new HashSet<>();
        for (Vehicle vehicle : problem.vehicles()) {
            realVehicles.add(vehicle.id());
        }
        for (RepairOperator repair : List.of(new GreedyInsertion(), new RegretInsertion())) {
            for (long seed = 0; seed < 5; seed++) {
                Solution repaired = repair.repair(problem, PROFILE, empty, rng(seed));
                assertTrue(StructureCheck.check(problem, repaired).isEmpty(), repair.id());
                assertEquals(Set.of(hot), repaired.bank(), repair.id());
                Set<VehicleId> used = new HashSet<>();
                for (Route route : repaired.routes()) {
                    assertTrue(realVehicles.contains(route.vehicleId()), repair.id());
                    assertTrue(used.add(route.vehicleId()), repair.id() + " duplicate vehicle");
                    for (NodeId nodeId : route.visits()) {
                        RequestId owner = InsertionSearch.requestOf(problem, nodeId);
                        assertTrue(problem.compatibleVehicles(owner).contains(route.vehicleId()), repair.id() + " " + owner);
                    }
                }
            }
        }
    }

    /**
     * T7b — 삼각부등식을 어기는 이동표에서 방문 하나를 빼면 남은 경로가 Infeasible이 된다. 그 경로는 예외 없이
     * 후보 0개로 빠지고(§4.3 개정), 그 해는 Evaluator Infeasible이라 AlnsSolver가 폐기한다 (E10·N9).
     */
    @Test
    void infeasibleExistingRouteYieldsNoCandidates() {
        Problem problem = nonTriangleProblem();
        RequestId ra = new RequestId("RA");
        RequestId rb = new RequestId("RB");
        RequestId rc = new RequestId("RC");
        RequestId re = new RequestId("RE");
        VehicleId v1 = new VehicleId("V1");
        Solution full = new Solution(
                List.of(new Route(v1, List.of(NodeId.delivery(ra), NodeId.delivery(rb), NodeId.delivery(rc)))),
                Set.of(re));
        assertTrue(StructureCheck.check(problem, full).isEmpty());
        assertInstanceOf(EvaluationResult.Feasible.class, Evaluator.evaluate(problem, PROFILE, full));

        // B를 빼면 A→C 직행이 5,000초라 C가 창(close 30,000)을 넘긴다 — 제거는 feasibility를 보존하지 않는다
        Solution withoutB = InsertionSearch.remove(problem, full, rb);
        assertEquals(List.of(NodeId.delivery(ra), NodeId.delivery(rc)), withoutB.routes().getFirst().visits());
        assertInstanceOf(PropagationResult.Infeasible.class,
                RoutePropagator.propagate(problem, v1, withoutB.routes().getFirst().visits()));

        // 예외 없이 후보 0개 — 차량이 하나뿐이라 새 경로 후보도 없다
        assertTrue(InsertionSearch.candidates(problem, PROFILE, withoutB, rb).isEmpty());
        assertTrue(InsertionSearch.candidates(problem, PROFILE, withoutB, re).isEmpty());
        assertTrue(InsertionSearch.candidatesFor(problem, PROFILE, withoutB, rb, v1).isEmpty());

        // 그 해는 Evaluator Infeasible이고, AlnsSolver는 예외 없이 폐기한다
        assertInstanceOf(EvaluationResult.Infeasible.class, Evaluator.evaluate(problem, PROFILE, withoutB));
        AlnsResult result = new AlnsSolver(
                AlnsSolverTest.steps(20L, 1L), List.of(alwaysRemove(rb)), List.of(new GreedyInsertion()))
                .solve(problem, PROFILE, full);
        assertEquals(20L, result.stats().iterations());
        assertEquals(20L, result.stats().infeasibleDiscarded());
        assertEquals(full, result.best());
    }

    /** 늘 같은 Request 하나만 빼는 테스트 전용 destroy — seed 운에 기대지 않고 §4.3 경로를 밟는다. */
    private static DestroyOperator alwaysRemove(RequestId requestId) {
        return new DestroyOperator() {
            @Override
            public String id() {
                return "always-remove-" + requestId.value();
            }

            @Override
            public Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng) {
                return InsertionSearch.remove(problem, current, requestId);
            }
        };
    }

    /**
     * 삼각부등식을 어기는 손 조립 이동표 — 전 arc 100 m/100 s이고 A→C만 5,000 s다 (t(A,C) > t(A,B) + t(B,C)).
     * RC의 창 close 30,000은 그 사이에 있다: A→B→C면 29,700에 닿고 A→C 직행이면 34,200이다. 차량은 1대뿐.
     */
    static Problem nonTriangleProblem() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        LocationId c = new LocationId("C");
        LocationId e = new LocationId("E");
        List<LocationId> ids = List.of(ConstructionFixtures.DEPOT, a, b, c, e);
        List<TravelEntry> travel = new ArrayList<>();
        for (LocationId from : ids) {
            for (LocationId to : ids) {
                if (!from.equals(to)) {
                    travel.add(new TravelEntry(from, to, 100, from.equals(a) && to.equals(c) ? 5_000 : 100));
                }
            }
        }
        return ConstructionFixtures.freeze(
                List.of(new Depot(ConstructionFixtures.DEPOT, List.of(ConstructionFixtures.ALL_DAY), Optional.empty())),
                List.of(
                        ConstructionFixtures.delivery("RA", a, 1_000L),
                        ConstructionFixtures.delivery("RB", b, 1_000L),
                        ConstructionFixtures.delivery("RC", c, 1_000L, new TimeWindow(28_800L, 30_000L), Optional.empty()),
                        ConstructionFixtures.delivery("RE", e, 1_000L)),
                List.of(ConstructionFixtures.vehicle("V1", 30_000L, Optional.of(ConstructionFixtures.DEPOT), Optional.empty())),
                ConstructionFixtures.locations(
                        ConstructionFixtures.at(ConstructionFixtures.DEPOT, 37.0, 127.0),
                        ConstructionFixtures.at(a, 37.01, 127.0),
                        ConstructionFixtures.at(b, 37.02, 127.0),
                        ConstructionFixtures.at(c, 37.03, 127.0),
                        ConstructionFixtures.at(e, 37.04, 127.0)),
                travel);
    }

    /** V1(능력 없음)·V2(COLD)·V3(start 없음 → DELIVERY_ONLY 비호환). COLD1은 V2만, PLAIN1은 V1·V2, HOT1은 0대. */
    static Problem capabilityProblem() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        LocationId c = new LocationId("C");
        Request cold = requiring(ConstructionFixtures.delivery("COLD1", a, 1_000L), Set.of("COLD"));
        Request plain = ConstructionFixtures.delivery("PLAIN1", b, 1_000L);
        Request hot = requiring(ConstructionFixtures.delivery("HOT1", c, 1_000L), Set.of("HOT"));
        return ConstructionFixtures.freeze(
                List.of(new Depot(ConstructionFixtures.DEPOT, List.of(ConstructionFixtures.ALL_DAY), Optional.empty())),
                List.of(cold, plain, hot),
                List.of(
                        vehicle("V1", Set.of(), Optional.of(ConstructionFixtures.DEPOT)),
                        vehicle("V2", Set.of("COLD"), Optional.of(ConstructionFixtures.DEPOT)),
                        vehicle("V3", Set.of("COLD", "HOT"), Optional.empty())),
                ConstructionFixtures.locations(
                        ConstructionFixtures.at(ConstructionFixtures.DEPOT, 37.0, 127.0),
                        ConstructionFixtures.at(a, 37.01, 127.0),
                        ConstructionFixtures.at(b, 37.0, 127.01),
                        ConstructionFixtures.at(c, 37.01, 127.01)),
                List.of());
    }

    private static Request requiring(Request request, Set<String> capabilities) {
        return new Request(
                request.id(), request.pattern(), request.pickup(), request.delivery(), request.items(),
                request.totalWeight(), request.totalVolume(), request.allowedVehicleFeatures(), capabilities);
    }

    private static Vehicle vehicle(String id, Set<String> capabilities, Optional<LocationId> start) {
        return new Vehicle(
                new VehicleId(id), Optional.empty(), 30_000L, 30_000L, List.of(ConstructionFixtures.WORK),
                OptionalInt.of(45), OptionalInt.empty(), OptionalLong.empty(), OptionalLong.empty(),
                capabilities, Optional.empty(), start, Optional.empty());
    }
}
