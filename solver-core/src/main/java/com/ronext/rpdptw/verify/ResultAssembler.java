package com.ronext.rpdptw.verify;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.Problem;

/** 재검증을 통과한 값에서만 결과를 만든다 — Fail 경로에는 결과가 없다 (Domain §10.2 MUST). */
public final class ResultAssembler {

    private ResultAssembler() {}

    /** profile은 run.profileId 기록용이다 — Problem에 없으므로 인자로 받는다 (stage-05 §4.1). */
    public static SolveResult assemble(Problem problem,
                                       Profile profile,
                                       VerificationResult.Pass pass,
                                       RunStamp stamp) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(pass, "pass");
        Objects.requireNonNull(stamp, "stamp");

        TimeBase timeBase = problem.timeBase();
        List<SolveResult.RouteResult> routes = new ArrayList<>();
        pass.routeFacts().keySet().stream()
                .sorted(Comparator.comparing(VehicleId::value))
                .forEach(vehicleId -> routes.add(routeResult(timeBase, pass.routeFacts().get(vehicleId))));

        List<SolveResult.Unassigned> unassigned = new ArrayList<>();
        pass.bank().stream()
                .sorted(Comparator.comparing(RequestId::value))
                .forEach(requestId -> unassigned.add(
                        new SolveResult.Unassigned(requestId, reason(problem, requestId))));

        SolveResult.Run run = new SolveResult.Run(
                stamp.inputKey(),
                stamp.receivedAt(),
                stamp.startedAt(),
                stamp.finishedAt(),
                profile.id(),
                true,
                problem.deliveryPolicy(),
                stamp.searchBudget());
        return new SolveResult(
                problem.planId(), SolveResult.Status.DONE, run, routes, unassigned, pass.evaluation());
    }

    private static SolveResult.RouteResult routeResult(TimeBase timeBase, RouteFacts facts) {
        List<SolveResult.Visit> visits = new ArrayList<>(facts.visits().size());
        for (VisitFacts visit : facts.visits()) {
            visits.add(new SolveResult.Visit(
                    visit.requestId(),
                    visit.pickup(),
                    visit.locationId(),
                    timeBase.toWallClock(visit.arrivalSec()),
                    timeBase.toWallClock(visit.serviceStartSec()),
                    timeBase.toWallClock(visit.serviceEndSec()),
                    visit.loadWeightAfter(),
                    visit.loadVolumeAfter()));
        }
        return new SolveResult.RouteResult(
                facts.vehicleId(),
                visits,
                facts.departureSec().map(timeBase::toWallClock),
                facts.endDepotArrivalSec().map(timeBase::toWallClock),
                facts.driveDistMeter(),
                facts.driveTimeSec(),
                facts.stopCount(),
                facts.routeOperationalTimeSec());
    }

    /**
     * 미배정 사유 (Domain §11.1 판정 규칙 정본, stage-05 §4.3) — 축을 차례로 좁힌다.
     * 순서가 고정이라 혼합 원인의 모호함이 생기지 않는다.
     */
    private static UnassignedReason reason(Problem problem, RequestId requestId) {
        Set<VehicleId> compatible = problem.compatibleVehicles(requestId);
        if (compatible.isEmpty()) {
            return UnassignedReason.NO_COMPATIBLE_VEHICLE;
        }
        Request request = problem.request(requestId);
        List<VehicleId> fitting = new ArrayList<>();
        for (VehicleId vehicleId : compatible) {
            Vehicle vehicle = problem.vehicle(vehicleId);
            if (request.totalWeight() <= vehicle.maxWeight() && request.totalVolume() <= vehicle.maxVolume()) {
                fitting.add(vehicleId);
            }
        }
        if (fitting.isEmpty()) {
            return UnassignedReason.CAPACITY;
        }
        List<NodeId> soloRoute = soloRoute(request);
        for (VehicleId vehicleId : fitting) {
            if (RouteReplay.replay(problem, vehicleId, soloRoute) instanceof RouteReplay.Outcome.Ok) {
                return UnassignedReason.NOT_PLACED;
            }
        }
        return UnassignedReason.TIME_WINDOW_INFEASIBLE;
    }

    private static List<NodeId> soloRoute(Request request) {
        return switch (request.pattern()) {
            case DELIVERY_ONLY -> List.of(request.delivery().orElseThrow().nodeId());
            case PICKUP_ONLY -> List.of(request.pickup().orElseThrow().nodeId());
            case PICKUP_DELIVERY -> List.of(
                    request.pickup().orElseThrow().nodeId(), request.delivery().orElseThrow().nodeId());
        };
    }
}
