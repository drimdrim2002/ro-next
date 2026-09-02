package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** H16 — 제약 우선 arc 확장, OR-Tools PATH_MOST_CONSTRAINED_ARC (heuristics 문서 §5 H16). 경로 끝에 pair를 통째로 잇는다. */
public final class ConstrainedPathExtensionConstruction implements ConstructionHeuristic {

    static final String ID = "constrained-path-extension";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        List<Vehicle> vehicles = VehicleFillRemainingRegretConstruction.vehicleOrder(problem);
        int bound = 2 * pending.size() + vehicles.size();
        int iterations = 0;
        List<Route> routes = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (pending.isEmpty()) {
                break;
            }
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);           // 규칙 (c)
            List<NodeId> visits = new ArrayList<>();
            while (true) {
                List<RequestId> order = new ArrayList<>();
                for (RequestId requestId : pending) {
                    if (problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                        order.add(requestId);
                    }
                }
                LocationId tail = visits.isEmpty()
                        ? vehicle.startDepot().orElse(null)
                        : problem.nodeRef(visits.getLast()).orElseThrow().side().locationId();
                order.sort(Comparator.<RequestId>comparingInt(id -> problem.compatibleVehicles(id).size())
                        .thenComparingLong(id -> tail == null ? 0L
                                : problem.travel().distanceMeter(tail, InsertionSearch.anchor(problem.request(id)).locationId()))
                        .thenComparing(InsertionSearch.BY_REQUEST_ID));
                RequestId appended = null;
                for (RequestId requestId : order) {
                    List<NodeId> extended = new ArrayList<>(visits);
                    GiantTourSplit.appendPair(problem, requestId, extended);   // [ …, p, d ] — 원자성이 구조로 보장
                    if (InsertionSearch.validate(problem, profile, vehicle.id(), extended).isPresent()) {
                        visits = extended;
                        appended = requestId;
                        break;                                                  // 첫 통과를 확정 (규칙 a)
                    }
                }
                if (appended == null) {
                    break;                                                      // 전부 실패 → v 확정
                }
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                pending.remove(appended);
            }
            if (!visits.isEmpty()) {
                routes.add(new Route(vehicle.id(), visits));
            }
        }
        return new Solution(routes, pending);
    }
}
