package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.WORK;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class ZoneQuotaSubsetFillConstructionTest {

    /** T41 — 용량 10,000 bin 2개·요청 5,4,4,3,2,2: 부피 best-fit은 1건을 놓치고 subsetSum은 두 bin을 정확히 채움 · 올림 양자화는 용량을 넘기지 않음. */
    @Test
    void subsetSumFillsExactly() {
        Problem problem = problem(new long[] {5_000L, 4_000L, 4_000L, 3_000L, 2_000L, 2_000L}, 10_000L, 2, OptionalInt.empty(), List.of());
        Vehicle vehicle = problem.vehicles().getFirst();
        List<RequestId> chosen = ZoneQuotaSubsetFillConstruction.subsetSum(problem, vehicle, InsertionSearch.sortedRequestIds(problem), 0, 0);
        assertEquals(10_000L, volume(problem, chosen));

        Solution solution = new ZoneQuotaSubsetFillConstruction().construct(problem, new DefaultProfile());
        assertEquals(Set.of(), solution.bank());
        assertEquals(2, solution.routes().size());
        for (Route route : solution.routes()) {
            assertEquals(10_000L, volume(problem, requestIds(problem, route)));
        }

        // 올림 양자화 — 용량 30,001(u = 2)에서 15,000 + 15,001은 양자화 합 15,001 > 15,000이라 고르지 않는다. 고른 합은 언제나 ≤ 용량
        Problem quantized = problem(new long[] {15_000L, 15_001L, 1L}, 30_001L, 1, OptionalInt.empty(), List.of());
        List<RequestId> picked = ZoneQuotaSubsetFillConstruction.subsetSum(
                quantized, quantized.vehicles().getFirst(), InsertionSearch.sortedRequestIds(quantized), 0, 0);
        assertTrue(volume(quantized, picked) <= 30_001L);
        assertEquals(15_002L, volume(quantized, picked));
    }

    /** T42 — 용량 8,000·정차 3 bin 2개·요청 4,4,3,3,1,1: 하한 없이는 첫 bin이 {4,4}를 먹어 둘째 bin이 정차 한도에 걸리고, 하한(6 − 3 = 3)이 있으면 전량 배정. */
    @Test
    void countLowerBoundSpreadsSmallRequests() {
        Problem problem = problem(new long[] {4_000L, 4_000L, 3_000L, 3_000L, 1_000L, 1_000L}, 8_000L, 2, OptionalInt.of(3), List.of());
        Vehicle vehicle = problem.vehicles().getFirst();
        List<RequestId> items = InsertionSearch.sortedRequestIds(problem);
        List<RequestId> greedy = ZoneQuotaSubsetFillConstruction.subsetSum(problem, vehicle, items, 0, 3);
        assertEquals(2, greedy.size());                                                 // {4,4} — 방문 수 최소
        List<RequestId> bounded = ZoneQuotaSubsetFillConstruction.subsetSum(problem, vehicle, items, 3, 3);
        assertEquals(3, bounded.size());
        assertEquals(8_000L, volume(problem, bounded));

        Solution solution = new ZoneQuotaSubsetFillConstruction().construct(problem, new DefaultProfile());
        assertEquals(Set.of(), solution.bank());
        for (Route route : solution.routes()) {
            assertEquals(3, route.visits().size());
        }
    }

    /** T43 — 시간창 때문에 함께 못 도는 요청이 순서화에서 실패 → 제외·nmax 축소 후 재DP가 pool 안에 끝나고 그 요청은 bank (X22). */
    @Test
    void oracleFeedbackExcludesInfeasibleAndTerminates() {
        LocationId near1 = new LocationId("N1");
        LocationId near2 = new LocationId("N2");
        LocationId far = new LocationId("F");
        Problem problem = freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        delivery("R1", near1, 4_000L, WORK, Optional.empty()),
                        delivery("R2", near2, 3_000L, WORK, Optional.empty()),
                        // 111 km 떨어진 곳에 08:00~08:01 창 — 근무 시작(08:00)에 출발해도 못 닿는다. 다음 창이 없어 어느 경로에도 불가
                        delivery("R3", far, 3_000L, new TimeWindow(28_800L, 28_860L), Optional.empty())),
                List.of(vehicle("V1", 10_000L, Optional.of(DEPOT), Optional.empty(), OptionalInt.of(3), Optional.empty())),
                locations(at(DEPOT, 37.0, 127.0), at(near1, 37.001, 127.0), at(near2, 37.002, 127.0), at(far, 38.0, 127.0)),
                List.of());

        List<RequestId> chosen = ZoneQuotaSubsetFillConstruction.subsetSum(
                problem, problem.vehicles().getFirst(), InsertionSearch.sortedRequestIds(problem), 0, 3);
        assertEquals(3, chosen.size());                                                 // DP는 셋 다 고른다 — 시간창은 오라클 몫

        Solution solution = new ZoneQuotaSubsetFillConstruction().construct(problem, new DefaultProfile());
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        assertEquals(Set.of(new RequestId("R3")), solution.bank());
        assertEquals(1, solution.routes().size());
        assertEquals(Set.of(new RequestId("R1"), new RequestId("R2")), Set.copyOf(requestIds(problem, solution.routes().getFirst())));
        assertTrue(Evaluator.evaluate(problem, new DefaultProfile(), solution) instanceof EvaluationResult.Feasible);
    }

    private static Problem problem(long[] volumes, long capacity, int vehicles, OptionalInt maxStop, List<String> unused) {
        List<Location> locations = new ArrayList<>();
        locations.add(at(DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < volumes.length; i++) {
            LocationId id = new LocationId("L" + (i + 1));
            locations.add(at(id, 37.0 + 0.001 * (i + 1), 127.0));
            requests.add(delivery("R" + (i + 1), id, volumes[i], ALL_DAY, Optional.empty()));
        }
        List<Vehicle> fleet = new ArrayList<>();
        for (int i = 1; i <= vehicles; i++) {
            fleet.add(vehicle("V" + i, capacity, Optional.of(DEPOT), Optional.empty(), maxStop, Optional.empty()));
        }
        return freeze(List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())), requests, fleet,
                locations(locations.toArray(Location[]::new)), List.of());
    }

    private static List<RequestId> requestIds(Problem problem, Route route) {
        List<RequestId> ids = new ArrayList<>();
        for (var nodeId : route.visits()) {
            ids.add(problem.nodeRef(nodeId).orElseThrow().requestId());
        }
        return ids;
    }

    private static long volume(Problem problem, List<RequestId> ids) {
        long total = 0L;
        for (RequestId id : ids) {
            total += problem.request(id).totalVolume();
        }
        return total;
    }
}
