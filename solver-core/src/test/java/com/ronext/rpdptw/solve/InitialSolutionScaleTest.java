package com.ronext.rpdptw.solve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Item;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Plan;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Trips;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/**
 * T25 — 규모 측정. Stage 2 T12와 같은 합성 문제(장소 453·주문 452·차량 31·이동표 453² 전 쌍)에
 * 실물 fixture의 정차 한도 28(VehicleMaxStopCount)을 더해 포트폴리오 1회 — 한도가 없으면 경로 하나가
 * 전 주문을 삼켜(2026-09-02 실측 H1 724초·routes 1) 실물과 무관한 값이 된다. 기법별 소요·미배정·score와 총 소요 / timeLimitSec 비를 출력한다 — 한도가 아니다.
 */
class InitialSolutionScaleTest {

    private static final int LOCATION_COUNT = 453;
    private static final int REQUEST_COUNT = 452;
    private static final int VEHICLE_COUNT = 31;
    /** 실물 fixture의 Optimizer.VehicleMaxStopCount (Plan fixture 실측). */
    private static final int MAX_STOP_COUNT = 28;
    /** 실물 fixture의 Termination.secondsSpentLimit (Plan §fixture 실측, 2026-08-10). ALNS 예산의 기준값. */
    private static final long TIME_LIMIT_SEC = 600L;

    @Test
    void runsPortfolioOnFullScaleSyntheticProblem() {
        Problem problem = syntheticProblem();
        Profile profile = new DefaultProfile();

        List<ConstructionHeuristic> traced = new ArrayList<>();
        for (ConstructionHeuristic heuristic : InitialSolutionBuilder.defaults()) {
            traced.add(printing(heuristic));
        }
        long started = System.nanoTime();
        InitialSolutionResult result = InitialSolutionBuilder.build(problem, profile, traced);
        long totalMillis = (System.nanoTime() - started) / 1_000_000L;

        System.out.printf("[T25] %-32s %-9s %6s %10s %s%n", "heuristic", "status", "bank", "elapsedMs", "score");
        for (ConstructionOutcome outcome : result.outcomes()) {
            System.out.printf("[T25] %-32s %-9s %6s %10d %s%n",
                    outcome.heuristicId(),
                    outcome.status(),
                    outcome.unassignedCount().isPresent() ? String.valueOf(outcome.unassignedCount().getAsInt()) : "-",
                    outcome.elapsedMillis(),
                    outcome.score().map(java.util.Arrays::toString).orElse("-"));
        }
        System.out.printf("[T25] best=%s score=%s 총 소요=%d ms · 총 소요 / timeLimitSec(%d s) = %.3f%n",
                result.heuristicId(), java.util.Arrays.toString(result.score()), totalMillis, TIME_LIMIT_SEC,
                totalMillis / 1000.0 / TIME_LIMIT_SEC);

        assertEquals(InitialSolutionBuilder.defaults().size(), result.outcomes().size());
        assertTrue(result.outcomes().stream().allMatch(o -> o.status() == ConstructionOutcome.Status.BUILT));
        assertTrue(StructureCheck.check(problem, result.best()).isEmpty());
    }

    private static ConstructionHeuristic printing(ConstructionHeuristic delegate) {
        return new ConstructionHeuristic() {
            @Override
            public String id() {
                return delegate.id();
            }

            @Override
            public boolean abstains(Problem problem) {
                return delegate.abstains(problem);
            }

            @Override
            public Solution construct(Problem problem, Profile profile) {
                long started = System.nanoTime();
                Solution solution = delegate.construct(problem, profile);
                System.out.printf("[T25] %-32s done in %d ms (bank %d, routes %d)%n",
                        delegate.id(), (System.nanoTime() - started) / 1_000_000L, solution.bank().size(), solution.routes().size());
                return solution;
            }
        };
    }

    /** Stage 2 T12(ProblemScaleTest)와 같은 조립 + 정차 한도 28 — 실물 JSON은 읽지 않는다. */
    static Problem syntheticProblem() {
        LocationId[] ids = new LocationId[LOCATION_COUNT];
        Map<LocationId, Location> locations = new LinkedHashMap<>();
        for (int i = 0; i < LOCATION_COUNT; i++) {
            LocationId id = new LocationId("WIN_" + i);
            ids[i] = id;
            locations.put(id, new Location(id, 37.0 + (i * 0.001), 127.0));
        }
        List<Depot> depots = List.of(new Depot(ids[0], List.of(new TimeWindow(0, 86_399)), Optional.empty()));
        List<Request> requests = new ArrayList<>(REQUEST_COUNT);
        for (int i = 1; i <= REQUEST_COUNT; i++) {
            RequestId requestId = new RequestId("O" + i);
            requests.add(new Request(
                    requestId,
                    ServicePattern.DELIVERY_ONLY,
                    Optional.empty(),
                    Optional.of(new RequestSide(
                            NodeId.delivery(requestId), ids[i], List.of(new TimeWindow(0, 86_399)), 0L, 0L, 86_400L, Optional.empty())),
                    List.of(new Item("I" + i, 1_000L, 1_000L, 1, 0L)),
                    1_000L,
                    1_000L,
                    Optional.empty(),
                    Set.of()));
        }
        List<Vehicle> vehicles = new ArrayList<>(VEHICLE_COUNT);
        for (int i = 1; i <= VEHICLE_COUNT; i++) {
            vehicles.add(new Vehicle(
                    new VehicleId("V" + i), Optional.empty(), 1_000_000L, 1_000_000L,
                    List.of(new TimeWindow(0, 86_399)), OptionalInt.of(45), OptionalInt.of(MAX_STOP_COUNT),
                    OptionalLong.empty(), OptionalLong.empty(), Set.of(), Optional.empty(), Optional.of(ids[0]), Optional.empty()));
        }
        List<TravelEntry> entries = new ArrayList<>(LOCATION_COUNT * LOCATION_COUNT);
        for (int i = 0; i < LOCATION_COUNT; i++) {
            for (int j = 0; j < LOCATION_COUNT; j++) {
                entries.add(new TravelEntry(ids[i], ids[j], i == j ? 9999 : 1_000, i == j ? 0 : 80));
            }
        }
        return Problem.freeze(new Plan(
                "SCALE", Optional.empty(), new TimeBase(LocalDateTime.of(2023, 9, 13, 0, 0)), 86_400L,
                depots, requests, vehicles, locations, entries, new DeliveryPolicy(Trips.ONEWAY, false, OptionalInt.of(45))));
    }
}
