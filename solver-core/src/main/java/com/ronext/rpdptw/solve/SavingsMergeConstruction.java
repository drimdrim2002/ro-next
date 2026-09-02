package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TravelMatrix;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** H7 — Clarke-Wright 병합, 방향성 표 그대로 (heuristics 문서 §5 H7). */
public final class SavingsMergeConstruction implements ConstructionHeuristic {

    static final String ID = "savings-merge";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        if (problem.depots().size() != 1) {
            return true;
        }
        for (Request request : problem.requests()) {
            if (request.pattern() == ServicePattern.PICKUP_DELIVERY) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        LocationId depot = problem.depots().getFirst().locationId();
        TravelMatrix travel = problem.travel();

        // 초기: 각 Request → 단독 경로 (호환 미사용 차량 VehicleId ASC 첫). 열 수 없으면 bank.
        Solution current = InsertionSearch.emptySolution(problem);
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
            Optional<VehicleId> vehicle = InsertionSearch.firstUnusedCompatible(
                    problem, requestId, InsertionSearch.usedVehicles(current));
            if (vehicle.isEmpty()) {
                continue;
            }
            Solution base = current;
            current = InsertionSearch.openRoute(problem, profile, base, requestId, vehicle.get()).orElse(base);
        }

        // 절감: saving(i, j) = d(i, depot) + d(depot, j) − d(i, j) — 대칭화하지 않는다 (Domain §4)
        List<RequestId> assigned = new ArrayList<>();
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
            if (!current.bank().contains(requestId)) {
                assigned.add(requestId);
            }
        }
        List<Saving> savings = new ArrayList<>();
        for (RequestId i : assigned) {
            LocationId li = InsertionSearch.anchor(problem.request(i)).locationId();
            for (RequestId j : assigned) {
                if (i.equals(j)) {
                    continue;
                }
                LocationId lj = InsertionSearch.anchor(problem.request(j)).locationId();
                long saving = (long) travel.distanceMeter(li, depot) + travel.distanceMeter(depot, lj) - travel.distanceMeter(li, lj);
                if (saving > 0L) {
                    savings.add(new Saving(i, j, saving));
                }
            }
        }
        savings.sort(Comparator.comparingLong(Saving::value).reversed()
                .thenComparing(Saving::i, InsertionSearch.BY_REQUEST_ID)
                .thenComparing(Saving::j, InsertionSearch.BY_REQUEST_ID));

        // 병합: i가 자기 경로의 꼬리, j가 다른 경로의 머리일 때만. 경로 수가 매 병합마다 1 줄어든다
        int merges = 0;
        for (Saving saving : savings) {
            Route tail = routeOf(problem, current, saving.i());
            Route head = routeOf(problem, current, saving.j());
            if (tail == null || head == null || tail.vehicleId().equals(head.vehicleId())) {
                continue;
            }
            if (!InsertionSearch.requestOf(problem, tail.visits().getLast()).equals(saving.i())
                    || !InsertionSearch.requestOf(problem, head.visits().getFirst()).equals(saving.j())) {
                continue;
            }
            List<NodeId> merged = new ArrayList<>(tail.visits());
            merged.addAll(head.visits());
            Optional<VehicleId> vehicle = smallestFreeVehicle(problem, current, tail, head, merged);
            if (vehicle.isEmpty() || InsertionSearch.validate(problem, profile, vehicle.get(), merged).isEmpty()) {
                continue;                                                       // 이 쌍은 버린다
            }
            List<Route> routes = new ArrayList<>();
            for (Route route : current.routes()) {
                if (!route.vehicleId().equals(tail.vehicleId()) && !route.vehicleId().equals(head.vehicleId())) {
                    routes.add(route);
                }
            }
            routes.add(new Route(vehicle.get(), merged));
            current = new Solution(routes, current.bank());
            InsertionSearch.checkOuterLoop(ID, ++merges, problem.requests().size());
        }
        return current;
    }

    private static Route routeOf(Problem problem, Solution current, RequestId requestId) {
        for (Route route : current.routes()) {
            for (NodeId nodeId : route.visits()) {
                if (InsertionSearch.requestOf(problem, nodeId).equals(requestId)) {
                    return route;
                }
            }
        }
        return null;
    }

    /** 합친 Request 전체와 호환인 미사용 차량 중 (maxWeight ASC, VehicleId ASC) 첫 번째 — 두 경로의 차량도 후보다. */
    private static Optional<VehicleId> smallestFreeVehicle(
            Problem problem, Solution current, Route tail, Route head, List<NodeId> merged) {
        Set<VehicleId> used = InsertionSearch.usedVehicles(current);
        used.remove(tail.vehicleId());
        used.remove(head.vehicleId());
        Set<RequestId> requests = new LinkedHashSet<>();
        for (NodeId nodeId : merged) {
            requests.add(InsertionSearch.requestOf(problem, nodeId));
        }
        Map<VehicleId, Vehicle> candidates = new LinkedHashMap<>();
        for (Vehicle vehicle : problem.vehicles()) {
            if (used.contains(vehicle.id())) {
                continue;
            }
            boolean compatibleWithAll = true;
            for (RequestId requestId : requests) {
                if (!problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                    compatibleWithAll = false;
                    break;
                }
            }
            if (compatibleWithAll) {
                candidates.put(vehicle.id(), vehicle);
            }
        }
        return candidates.values().stream()
                .min(Comparator.comparingLong(Vehicle::maxWeight).thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID))
                .map(Vehicle::id);
    }

    private record Saving(RequestId i, RequestId j, long value) {}
}
