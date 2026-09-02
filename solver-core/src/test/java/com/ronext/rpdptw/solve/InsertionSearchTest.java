package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.pickupDelivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

class InsertionSearchTest {

    private static final VehicleId V1 = new VehicleId("V1");

    /** T13 — profile hard("경로당 방문 1개 초과 금지")에서 2개째 삽입 후보가 후보 단계에서 탈락 (§4.1 ②). */
    @Test
    void candidateValidationIncludesProfileHard() {
        Problem problem = ConstructionFixtures.ring(2, 1, 30_000L, 10_000L);
        RequestId r1 = new RequestId("R1");
        RequestId r2 = new RequestId("R2");
        Solution empty = InsertionSearch.emptySolution(problem);

        List<Candidate> first = InsertionSearch.candidates(problem, ConstructionFixtures.maxVisitsProfile(1), empty, r1);
        assertEquals(1, first.size());
        assertTrue(first.getFirst().newRoute());
        Solution one = InsertionSearch.apply(problem, empty, first.getFirst());
        assertEquals(Set.of(r2), one.bank());

        assertFalse(InsertionSearch.candidates(problem, new DefaultProfile(), one, r2).isEmpty());
        assertTrue(InsertionSearch.candidates(problem, ConstructionFixtures.maxVisitsProfile(1), one, r2).isEmpty());
    }

    /** T26 — d(i,j) ≠ d(j,i)인 이동표에서 standaloneDistMeter가 방향별 값을 그대로 합산 · 좌표 미사용. */
    @Test
    void standaloneDistUsesDirectedMatrix() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(pickupDelivery("R1", a, b, 1_000L), delivery("R2", b, 1_000L)),
                List.of(vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.of(DEPOT))),
                locations(at(DEPOT, 37.0, 127.0), at(a, 37.0, 127.0), at(b, 37.0, 127.0)),   // 좌표가 전부 같다
                List.of(
                        new TravelEntry(DEPOT, a, 1_000, 100), new TravelEntry(a, DEPOT, 5_000, 500),
                        new TravelEntry(a, b, 2_000, 200), new TravelEntry(b, a, 9_000, 900),
                        new TravelEntry(b, DEPOT, 3_000, 300), new TravelEntry(DEPOT, b, 7_000, 700)));
        assertEquals(1_000L + 2_000L + 3_000L, InsertionSearch.standaloneDistMeter(problem, V1, new RequestId("R1")));
        assertEquals(7_000L + 3_000L, InsertionSearch.standaloneDistMeter(problem, V1, new RequestId("R2")));
    }

    /** T27 — deltaForwardSlackSec = 삽입 전후를 각각 전파해 기존 방문 slack 합을 재계산한 값 · 새 방문은 불포함. */
    @Test
    void candidateReportsForwardSlackDelta() {
        Problem problem = ConstructionFixtures.ring(3, 1, 30_000L, 5_000L);
        List<NodeId> before = List.of(NodeId.delivery(new RequestId("R1")), NodeId.delivery(new RequestId("R2")));
        Solution current = new Solution(List.of(new Route(V1, before)), Set.of(new RequestId("R3")));

        List<Candidate> cands = InsertionSearch.candidates(problem, new DefaultProfile(), current, new RequestId("R3"));
        assertFalse(cands.isEmpty());
        RouteFacts factsBefore = facts(problem, before);
        for (Candidate candidate : cands) {
            RouteFacts factsAfter = facts(problem, candidate.visits());
            Map<NodeId, Long> slackBefore = InsertionSearch.forwardSlackByNode(problem, factsBefore);
            Map<NodeId, Long> slackAfter = InsertionSearch.forwardSlackByNode(problem, factsAfter);
            long expected = 0L;
            for (NodeId node : before) {                                   // 기존 방문만 — R3의 방문은 합에 없다
                expected += slackBefore.get(node) - slackAfter.get(node);
            }
            assertEquals(expected, candidate.deltaForwardSlackSec());
            assertEquals(factsAfter.driveDistMeter() - factsBefore.driveDistMeter(), candidate.deltaDriveDistMeter());
            assertEquals(
                    factsAfter.routeOperationalTimeSec() - factsBefore.routeOperationalTimeSec(),
                    candidate.deltaRouteOperationalTimeSec());
        }
        assertTrue(cands.stream().anyMatch(c -> c.deltaForwardSlackSec() > 0L));
    }


    /** T13b — Cache의 상위 3개·차량별 최선이 삽입을 거듭한 여러 상태에서 전체 재계산과 일치 (§4.1 대조). */
    @Test
    void cachedCandidatesMatchFullRecomputation() {
        for (Problem problem : List.of(ConstructionFixtures.ring(9, 3, 30_000L, 10_000L), ConstructionFixtures.mixed())) {
            for (var profile : List.of(new DefaultProfile(), ConstructionFixtures.maxVisitsProfile(2))) {
                InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
                Solution current = InsertionSearch.emptySolution(problem);
                List<RequestId> pending = new java.util.ArrayList<>(InsertionSearch.sortedRequestIds(problem));
                while (!pending.isEmpty()) {
                    for (RequestId requestId : pending) {
                        List<Candidate> full = InsertionSearch.candidates(problem, profile, current, requestId);
                        List<Candidate> cached = cache.candidates(current, requestId);
                        assertEquals(full.subList(0, Math.min(3, full.size())), cached.subList(0, Math.min(3, cached.size())));
                        Map<VehicleId, Long> perVehicle = new java.util.HashMap<>();
                        for (Candidate c : full) {
                            perVehicle.merge(c.vehicleId(), 1L, Long::sum);
                        }
                        long expectedSize = perVehicle.values().stream().mapToLong(n -> Math.min(n, InsertionSearch.Cache.TOP)).sum();
                        assertEquals(expectedSize, cached.size());
                        Map<VehicleId, Candidate> bestFull = new java.util.HashMap<>();
                        Map<VehicleId, Candidate> bestCached = new java.util.HashMap<>();
                        full.forEach(c -> bestFull.putIfAbsent(c.vehicleId(), c));
                        cached.forEach(c -> bestCached.putIfAbsent(c.vehicleId(), c));
                        assertEquals(bestFull, bestCached);
                    }
                    // 다음 상태 — 첫 요청을 순서대로 넣는다 (경로 하나만 바뀌므로 나머지 항목은 캐시에서 온다)
                    RequestId next = pending.removeFirst();
                    List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, next);
                    if (!cands.isEmpty()) {
                        current = InsertionSearch.apply(problem, current, cands.get(cands.size() / 2));
                        cache.forget(next);
                    }
                }
            }
        }
    }

    private static RouteFacts facts(Problem problem, List<NodeId> visits) {
        return ((PropagationResult.Feasible) RoutePropagator.propagate(problem, V1, visits)).facts();
    }
}
