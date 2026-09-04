package com.ronext.rpdptw.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Compatibility;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.input.DepotInput;
import com.ronext.rpdptw.domain.input.ItemInput;
import com.ronext.rpdptw.domain.input.OptionsInput;
import com.ronext.rpdptw.domain.input.PlanInput;
import com.ronext.rpdptw.domain.input.PlanNormalizer;
import com.ronext.rpdptw.domain.input.RequestInput;
import com.ronext.rpdptw.domain.input.SideInput;
import com.ronext.rpdptw.domain.input.TravelEntryInput;
import com.ronext.rpdptw.domain.input.VehicleInput;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.Scores;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.ConstructionHeuristic;
import com.ronext.rpdptw.solve.EvaluationResult;
import com.ronext.rpdptw.solve.Evaluator;
import com.ronext.rpdptw.solve.PropagationResult;
import com.ronext.rpdptw.solve.Route;
import com.ronext.rpdptw.solve.RoutePropagator;
import com.ronext.rpdptw.solve.Solution;
import com.ronext.rpdptw.solve.StructureCheck;
import com.ronext.rpdptw.solve.VehicleZoneFillConstruction;
import com.ronext.rpdptw.solve.ZoneQuotaAllocationAccess;
import com.ronext.rpdptw.solve.ZoneQuotaBalancedFillConstruction;
import com.ronext.rpdptw.solve.ZoneQuotaExchangeFillConstruction;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * T44 — 실물 fixture(data/win_poc_case_floor.json, 주문 452·차량 31·정차 28)에서 H23이 전량 배정하고
 * 경로마다 구역 1종·차급·부피·무게·정차·시간창·reqDate를 지키며 H3보다 사전식으로 좋다 (heuristics 문서 §8 T44).
 * score 전체 일치와 `Allocation.truncated == false`는 T52(존 배정 DP 개정의 회귀 고정)다.
 * 규약 JSON → PlanInput 매핑은 테스트 전용이다 — 정식 adapter는 Stage 6이 만들고 이 매핑을 대체한다.
 */
class WinPocFixtureTest {

    /** T15(WinPocAlnsTest)도 이 경로와 아래 매핑을 그대로 쓴다 — 정식 adapter는 Stage 6이다. */
    static final Path FIXTURE = Path.of("..", "data", "win_poc_case_floor.json");
    private static final int MAX_STOPS = 28;

    @Test
    void zoneQuotaBalancedFillAssignsEveryRequestOnRealFixture() throws Exception {
        assertTrue(Files.exists(FIXTURE), "fixture missing: " + FIXTURE.toAbsolutePath());
        Problem problem = Problem.freeze(new PlanNormalizer().normalize(toInput(new ObjectMapper().readTree(FIXTURE.toFile()))));
        assertEquals(452, problem.requests().size());
        assertEquals(31, problem.vehicles().size());
        assertEquals(1, problem.depots().size());

        DefaultProfile profile = new DefaultProfile();
        Solution solution = new ZoneQuotaBalancedFillConstruction().construct(problem, profile);
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        assertEquals(Set.of(), solution.bank());
        assertEquals(31, solution.routes().size());

        EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
        assertTrue(evaluated instanceof EvaluationResult.Feasible, "H23 must be Feasible");
        long[] score = ((EvaluationResult.Feasible) evaluated).score();
        // T52 — 존 배정 DP 개정(성분별 희소 DP + 폭 제한) 전후로 실물 답이 그대로임을 고정한다
        assertArrayEquals(new long[] {0L, 31L, 4_198_408L, 1_002_069L}, score);
        assertFalse(ZoneQuotaAllocationAccess.truncated(problem), "실물 fixture는 폭 제한에 닿지 않는다");

        // 같은 입력이면 같은 해 (결정성, X8)
        long[] again = ((EvaluationResult.Feasible) Evaluator.evaluate(
                problem, profile, new ZoneQuotaBalancedFillConstruction().construct(problem, profile))).score();
        assertTrue(Arrays.equals(score, again));

        // 판정 기준: 종전 1위 H3(vehicle-zone-fill)보다 사전식으로 좋다
        EvaluationResult h3 = Evaluator.evaluate(problem, profile, new VehicleZoneFillConstruction().construct(problem, profile));
        assertTrue(h3 instanceof EvaluationResult.Feasible);
        assertTrue(Scores.compare(score, ((EvaluationResult.Feasible) h3).score()) < 0);

        for (Route route : solution.routes()) {
            audit(problem, route);
        }
    }

    /**
     * T56 — 같은 실행 안에서 H3·H25·H23을 돌려 (미배정, 차량, 거리, 운행시간, 소요)와 H25·H23의 배정 값 V를
     * 표로 출력한다. 단언은 H25의 구조·Feasible·재실행 동일 score·경로 감사뿐이다 —
     * `H25 ≤ H3` 판정은 실측을 보고 사용자가 정한다 (stage-04-h25 §8).
     */
    @Test
    void zoneQuotaExchangeFillComparedOnRealFixture() throws Exception {
        Problem problem = Problem.freeze(new PlanNormalizer().normalize(toInput(new ObjectMapper().readTree(FIXTURE.toFile()))));
        DefaultProfile profile = new DefaultProfile();

        System.out.println("T56 실물 fixture — 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요(ms)");
        long[] h3 = report("H3  vehicle-zone-fill       ", problem, profile, new VehicleZoneFillConstruction());
        long[] h25 = report("H25 zone-quota-exchange-fill", problem, profile, new ZoneQuotaExchangeFillConstruction());
        long[] h23 = report("H23 zone-quota-balanced-fill", problem, profile, new ZoneQuotaBalancedFillConstruction());
        System.out.println("T56 배정 값 V (Σ부족, Σ낭비, Σ대수) — H25 " + Arrays.toString(
                ZoneQuotaAllocationAccess.exchangeAllocationValue(problem))
                + " · H23(DP) " + Arrays.toString(ZoneQuotaAllocationAccess.dpAllocationValue(problem))
                + " · H25 교환 적용 수 " + ZoneQuotaAllocationAccess.exchangeScanCount(problem));
        System.out.println("T56 사전식 비교 — H25 vs H3 " + Scores.compare(h25, h3)
                + " · H25 vs H23 " + Scores.compare(h25, h23));

        Solution solution = new ZoneQuotaExchangeFillConstruction().construct(problem, profile);
        assertTrue(StructureCheck.check(problem, solution).isEmpty());
        EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
        assertTrue(evaluated instanceof EvaluationResult.Feasible, "H25 must be Feasible");
        assertArrayEquals(h25, ((EvaluationResult.Feasible) evaluated).score());          // 재실행 동일 score (X8)
        for (Route route : solution.routes()) {
            audit(problem, route);
        }
    }

    /** 기법 하나를 돌려 표 한 줄을 찍고 score를 돌려준다. */
    private static long[] report(String label, Problem problem, DefaultProfile profile, ConstructionHeuristic heuristic) {
        long started = System.nanoTime();
        Solution solution = heuristic.construct(problem, profile);
        long elapsedMillis = (System.nanoTime() - started) / 1_000_000L;
        EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
        assertTrue(evaluated instanceof EvaluationResult.Feasible, heuristic.id());
        long[] score = ((EvaluationResult.Feasible) evaluated).score();
        System.out.println("T56 " + label + " | " + score[0] + " | " + score[1] + " | " + score[2] + " | "
                + score[3] + " | " + elapsedMillis);
        return score;
    }

    /** 경로 감사 — 구역 1종 · 차급 · 부피 · 무게 · 정차 28 · 시간창 · reqDate. 전파는 정식 코드 경로다. */
    private static void audit(Problem problem, Route route) {
        Vehicle vehicle = problem.vehicle(route.vehicleId());
        PropagationResult propagated = RoutePropagator.propagate(problem, vehicle.id(), route.visits());
        assertTrue(propagated instanceof PropagationResult.Feasible, vehicle.id().value());
        RouteFacts facts = ((PropagationResult.Feasible) propagated).facts();
        Set<String> zones = new TreeSet<>();
        long weight = 0L;
        long volume = 0L;
        for (VisitFacts visit : facts.visits()) {
            Request request = problem.request(visit.requestId());
            RequestSide side = request.delivery().orElseThrow();
            side.zoneId().ifPresent(zones::add);
            weight += request.totalWeight();
            volume += request.totalVolume();
            assertTrue(Compatibility.size(vehicle, request), vehicle.id().value() + " feature " + request.id().value());
            boolean inWindow = false;
            for (TimeWindow window : side.windows()) {
                inWindow |= window.openSec() <= visit.serviceStartSec() && visit.serviceStartSec() <= window.closeSec();
            }
            assertTrue(inWindow, vehicle.id().value() + " window " + request.id().value());
            assertTrue(visit.serviceStartSec() <= side.reqDateSec(), vehicle.id().value() + " reqDate " + request.id().value());
        }
        assertTrue(zones.size() <= 1, vehicle.id().value() + " zones " + zones);
        assertTrue(weight <= vehicle.maxWeight(), vehicle.id().value() + " weight");
        assertTrue(volume <= vehicle.maxVolume(), vehicle.id().value() + " volume");
        assertTrue(facts.stopCount() <= MAX_STOPS, vehicle.id().value() + " stops " + facts.stopCount());
    }

    // ---- 규약 JSON → PlanInput (테스트 전용 매핑 · Stage 6 adapter가 대체한다) ----

    static PlanInput toInput(JsonNode root) {
        JsonNode range = root.get("dateRange");
        List<DepotInput> depots = new ArrayList<>();
        for (JsonNode d : root.get("depot")) {
            depots.add(new DepotInput(text(d, "locId"), text(d, "latitude"), text(d, "longitude"),
                    time(d, "openTime"), time(d, "closeTime"), text(d, "zoneId")));
        }
        String singleDepot = depots.size() == 1 ? depots.getFirst().locId() : null;
        List<RequestInput> requests = new ArrayList<>();
        for (JsonNode o : root.get("orders")) {
            List<ItemInput> items = new ArrayList<>();
            for (JsonNode it : o.get("items")) {
                items.add(new ItemInput(text(it, "itemId"), decimal(it, "weight"), decimal(it, "volume"),
                        has(it, "qty") ? it.get("qty").asInt() : null, has(it, "taskTime") ? it.get("taskTime").asLong() : null));
            }
            List<String> features = null;
            if (has(o, "vehicleFeature")) {
                features = new ArrayList<>();
                for (JsonNode f : o.get("vehicleFeature")) {
                    features.add(f.asText());
                }
            }
            SideInput delivery = new SideInput(text(o, "locId"), text(o, "latitude"), text(o, "longitude"),
                    time(o, "openTime"), time(o, "closeTime"), has(o, "duration") ? o.get("duration").asLong() : null,
                    dateTime(o, "reqDate"), text(o, "zoneId"));
            requests.add(new RequestInput(text(o, "orderId"), null, delivery, items, features, null));
        }
        List<VehicleInput> vehicles = new ArrayList<>();
        for (JsonNode v : root.get("vehicles")) {
            vehicles.add(new VehicleInput(text(v, "vehicleId"), text(v, "vehicleFeature"), decimal(v, "maxWeight"), decimal(v, "maxVolume"),
                    time(v, "workStartTime"), time(v, "workEndTime"), has(v, "speed") ? v.get("speed").asInt() : null,
                    has(v, "maxStopCnt") ? v.get("maxStopCnt").asInt() : null, null, null, null, null,
                    has(v, "startDepot") ? text(v, "startDepot") : singleDepot, text(v, "endDepot")));
        }
        List<TravelEntryInput> travel = new ArrayList<>();
        for (JsonNode e : root.get("distanceMatrix")) {
            travel.add(new TravelEntryInput(e.get("F").asText(), e.get("T").asText(), decimal(e, "D"), decimal(e, "U")));
        }
        JsonNode op = root.get("options");
        OptionsInput options = new OptionsInput(text(op, "trips"), has(op, "multiRotation") ? op.get("multiRotation").asInt() : null,
                text(op, "waitInDepot"), has(op, "Optimizer.DefaultSpeed") ? op.get("Optimizer.DefaultSpeed").asInt() : null,
                has(op, "Optimizer.VehicleMaxStopCount") ? op.get("Optimizer.VehicleMaxStopCount").asInt() : null);
        return new PlanInput(text(root, "planId"), text(root, "shprId"), dateTime(range, "from"), dateTime(range, "to"),
                depots, requests, vehicles, travel, options);
    }

    private static boolean has(JsonNode node, String key) {
        return node != null && node.has(key) && !node.get(key).isNull();
    }

    private static String text(JsonNode node, String key) {
        return has(node, key) ? node.get(key).asText() : null;
    }

    private static BigDecimal decimal(JsonNode node, String key) {
        return has(node, key) ? new BigDecimal(node.get(key).asText()) : null;
    }

    private static LocalTime time(JsonNode node, String key) {
        return has(node, key) ? LocalTime.parse(node.get(key).asText()) : null;
    }

    private static LocalDateTime dateTime(JsonNode node, String key) {
        return has(node, key) ? LocalDateTime.parse(node.get(key).asText().replace(' ', 'T').replace("Z", "")) : null;
    }

}
