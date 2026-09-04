package com.ronext.rpdptw.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.ronext.rpdptw.solve.EvaluationResult;
import com.ronext.rpdptw.solve.Evaluator;
import com.ronext.rpdptw.solve.PropagationResult;
import com.ronext.rpdptw.solve.Route;
import com.ronext.rpdptw.solve.RoutePropagator;
import com.ronext.rpdptw.solve.Solution;
import com.ronext.rpdptw.solve.StructureCheck;
import com.ronext.rpdptw.solve.VehicleZoneFillConstruction;
import com.ronext.rpdptw.solve.ZoneQuotaBalancedFillConstruction;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * T44 — 실물 fixture(data/win_poc_case_floor.json, 주문 452·차량 31·정차 28)에서 H23이 전량 배정하고
 * 경로마다 구역 1종·차급·부피·무게·정차·시간창·reqDate를 지키며 H3보다 사전식으로 좋다 (heuristics 문서 §8 T44).
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
        assertEquals(0L, score[0]);
        assertEquals(31L, score[1]);

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
