package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H14 — 차량 순서 채움 + 남은 차량 대비 regret, VROOM (heuristics 문서 §5 H14). */
public final class VehicleFillRemainingRegretConstruction implements ConstructionHeuristic {

    static final String ID = "vehicle-fill-remaining-regret";

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
        Solution current = InsertionSearch.emptySolution(problem);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        List<Vehicle> vehicles = vehicleOrder(problem);
        int bound = pending.size() + vehicles.size();
        int iterations = 0;
        for (int index = 0; index < vehicles.size() && !pending.isEmpty(); index++) {
            VehicleId v = vehicles.get(index).id();
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);           // 규칙 (c)
            Optional<Solution> seeded = seed(problem, profile, current, pending, v);
            if (seeded.isEmpty()) {
                continue;                                                       // 전부 실패면 v 사용 안 함
            }
            current = seeded.get();
            List<Vehicle> remaining = vehicles.subList(index + 1, vehicles.size());
            while (true) {
                Pick best = null;
                for (RequestId requestId : pending) {
                    List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, v);
                    if (cands.isEmpty()) {
                        continue;
                    }
                    Candidate first = cands.getFirst();
                    Optional<Long> regret = remainingStandalone(problem, remaining, requestId);
                    Pick pick = new Pick(requestId, regret.isEmpty(),
                            first.deltaDriveDistMeter() - regret.orElse(0L), first.deltaDriveDistMeter(), first);
                    if (best == null || PICK_ORDER.compare(pick, best) < 0) {
                        best = pick;
                    }
                }
                if (best == null) {
                    break;                                                      // v 확정, 다음 차량
                }
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);       // 규칙 (a)
                current = InsertionSearch.apply(problem, current, best.candidate());
                pending.remove(best.requestId());
            }
        }
        return current;
    }

    /** seed = v와 호환·미처리 중 (totalWeight DESC, totalVolume DESC, RequestId ASC) 순 첫 새 경로 성공. */
    private static Optional<Solution> seed(
            Problem problem, Profile profile, Solution current, Set<RequestId> pending, VehicleId v) {
        List<RequestId> order = new ArrayList<>();
        for (RequestId requestId : pending) {
            if (problem.compatibleVehicles(requestId).contains(v)) {
                order.add(requestId);
            }
        }
        order.sort(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalWeight()).reversed()
                .thenComparing(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalVolume()).reversed())
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        for (RequestId requestId : order) {
            Optional<Solution> opened = InsertionSearch.openRoute(problem, profile, current, requestId, v);
            if (opened.isPresent()) {
                pending.remove(requestId);
                return opened;
            }
        }
        return Optional.empty();
    }

    /** regret(r) = min over 차량 순서상 v 이후·r과 호환인 v′ 의 standaloneDistMeter. 없으면 empty (noAlt). */
    static Optional<Long> remainingStandalone(Problem problem, List<Vehicle> remaining, RequestId requestId) {
        Long min = null;
        for (Vehicle vehicle : remaining) {
            if (!problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                continue;
            }
            long standalone = InsertionSearch.standaloneDistMeter(problem, vehicle.id(), requestId);
            if (min == null || standalone < min) {
                min = standalone;
            }
        }
        return Optional.ofNullable(min);
    }

    /** 차량 순서 = (effectiveMaxStopCount DESC — 부재 = 최우선, maxWeight DESC, Σ근무창 길이 DESC, VehicleId ASC). H16 공유. */
    static List<Vehicle> vehicleOrder(Problem problem) {
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.<Vehicle>comparingLong(v -> v.effectiveMaxStopCount().isPresent()
                        ? v.effectiveMaxStopCount().getAsInt() : Long.MAX_VALUE).reversed()
                .thenComparing(Comparator.comparingLong(Vehicle::maxWeight).reversed())
                .thenComparing(Comparator.comparingLong(InsertionSearch::workSpanSec).reversed())
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return vehicles;
    }

    /** 선택 키 = (noAlt DESC, Δ거리(v) − regret ASC, Δ거리 ASC, RequestId ASC) — big-M 없이 축 분리 (X19). */
    private static final Comparator<Pick> PICK_ORDER = Comparator.comparing(Pick::noAlt, Comparator.reverseOrder())
            .thenComparingLong(Pick::netCost)
            .thenComparingLong(Pick::deltaDist)
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, boolean noAlt, long netCost, long deltaDist, Candidate candidate) {}
}
