package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H8 — depot 기준 각도 sweep → next-fit 분할 (heuristics 문서 §5 H8). 좌표는 정렬에만 쓴다. */
public final class SweepNextFitConstruction implements ConstructionHeuristic {

    static final String ID = "sweep-next-fit";

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
            if (request.pattern() != ServicePattern.DELIVERY_ONLY) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        LocationId depot = problem.depots().getFirst().locationId();
        List<RequestId> order = angleOrder(problem, depot);

        // 분할: next-fit — 넘치면 다음 차량으로, 앞 차로 되돌아가지 않는다 (각도 연속성)
        List<Vehicle> vehicles = InsertionSearch.vehiclesByCapacityDesc(problem);
        List<List<RequestId>> groups = new ArrayList<>();
        for (Vehicle ignored : vehicles) {
            groups.add(new ArrayList<>());
        }
        int index = 0;
        long weight = 0L;
        long volume = 0L;
        for (RequestId requestId : order) {
            if (index >= vehicles.size()) {
                break;                                                          // 차량 소진 → 나머지 bank
            }
            Request request = problem.request(requestId);
            Vehicle vehicle = vehicles.get(index);
            boolean overflow = weight + request.totalWeight() > vehicle.maxWeight()
                    || volume + request.totalVolume() > vehicle.maxVolume();
            if (overflow && groups.get(index).isEmpty()) {
                continue;                                                       // 빈 차에도 안 들면 bank — 차량을 넘기지 않는다
            }
            if (overflow) {
                index++;
                weight = 0L;
                volume = 0L;
                if (index >= vehicles.size()) {
                    break;
                }
                vehicle = vehicles.get(index);
                if (request.totalWeight() > vehicle.maxWeight() || request.totalVolume() > vehicle.maxVolume()) {
                    continue;
                }
            }
            groups.get(index).add(requestId);
            weight = Math.addExact(weight, request.totalWeight());
            volume = Math.addExact(volume, request.totalVolume());
        }

        // 순서: 각 차량 묶음 안에서 §4.1 최소 비용 순차 삽입 (각도 순). 시간창 때문에 못 들어가면 bank
        Solution current = InsertionSearch.emptySolution(problem);
        int iterations = 0;
        for (int v = 0; v < vehicles.size(); v++) {
            for (RequestId requestId : groups.get(v)) {
                InsertionSearch.checkOuterLoop(ID, ++iterations, order.size());
                List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicles.get(v).id());
                if (!cands.isEmpty()) {
                    current = InsertionSearch.apply(problem, current, cands.getFirst());
                }
            }
        }
        return current;
    }

    /** θ(r) = atan2(lat − lat(depot), lon − lon(depot)) ASC(−π부터), 동률 RequestId ASC. 좌표는 여기서만. */
    static List<RequestId> angleOrder(Problem problem, LocationId depot) {
        Location center = problem.locations().get(depot);
        List<RequestId> order = new ArrayList<>(InsertionSearch.sortedRequestIds(problem));
        order.sort(Comparator.<RequestId>comparingDouble(id -> angle(problem, center, InsertionSearch.anchor(problem.request(id)).locationId()))
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }

    static double angle(Problem problem, Location center, LocationId locationId) {
        Location location = problem.locations().get(locationId);
        return Math.atan2(location.latitude() - center.latitude(), location.longitude() - center.longitude());
    }
}
