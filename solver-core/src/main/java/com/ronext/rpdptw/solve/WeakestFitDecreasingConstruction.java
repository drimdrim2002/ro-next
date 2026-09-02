package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H15 — difficulty 순 요청 × strength 순 차량, Timefold WFD (heuristics 문서 §5 H15). */
public final class WeakestFitDecreasingConstruction implements ConstructionHeuristic {

    static final String ID = "weakest-fit-decreasing";

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
        List<RequestId> order = new ArrayList<>(InsertionSearch.sortedRequestIds(problem));
        order.sort(Comparator.<RequestId>comparingInt(id -> problem.compatibleVehicles(id).size())
                .thenComparingLong(id -> InsertionSearch.windowSpanSec(InsertionSearch.anchor(problem.request(id))))
                .thenComparing(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalWeight()).reversed())
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        List<Vehicle> strength = strengthOrder(problem);

        Solution current = InsertionSearch.emptySolution(problem);
        int iterations = 0;
        for (RequestId requestId : order) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, order.size());
            Set<VehicleId> used = InsertionSearch.usedVehicles(current);
            boolean inserted = false;
            for (Vehicle vehicle : strength) {                                  // 1) 열린 경로를 약한 순으로
                if (!used.contains(vehicle.id())) {
                    continue;
                }
                List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicle.id());
                if (!cands.isEmpty()) {
                    current = InsertionSearch.apply(problem, current, cands.getFirst());
                    inserted = true;
                    break;
                }
            }
            if (inserted) {
                continue;
            }
            for (Vehicle vehicle : strength) {                                  // 2) 미사용 호환 차량을 약한 순으로
                if (used.contains(vehicle.id()) || !problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                    continue;
                }
                Optional<Solution> opened = InsertionSearch.openRoute(problem, profile, current, requestId, vehicle.id());
                if (opened.isPresent()) {
                    current = opened.get();
                    break;
                }
            }
            // 둘 다 없으면 bank (규칙 b)
        }
        return current;
    }

    /** strength 순 = (maxWeight ASC, maxVolume ASC, Σ근무창 길이 ASC, VehicleId ASC) — 약한 차 먼저. */
    static List<Vehicle> strengthOrder(Problem problem) {
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.comparingLong(Vehicle::maxWeight)
                .thenComparingLong(Vehicle::maxVolume)
                .thenComparingLong(InsertionSearch::workSpanSec)
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return vehicles;
    }
}
