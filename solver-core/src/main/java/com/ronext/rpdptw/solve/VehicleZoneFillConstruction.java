package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H3 — 차량 외곽 루프 + 최선 존 커밋 (heuristics 문서 §5 H3). */
public final class VehicleZoneFillConstruction implements ConstructionHeuristic {

    static final String ID = "vehicle-zone-fill";
    static final String NO_ZONE = "(none)";
    /** 재량 상수 — 이 적재율을 넘기는 "전원 성공" 존이 있으면 그중에서 고른다 (Stage 8 조정). */
    static final double LOAD_TARGET = 0.85;

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
        int iterations = 0;
        for (Vehicle vehicle : vehicles) {
            if (pending.isEmpty()) {
                break;
            }
            InsertionSearch.checkOuterLoop(ID, ++iterations, vehicles.size());
            Map<String, List<RequestId>> zones = zoneGroups(problem, pending);
            List<String> zoneOrder = zoneOrder(problem, zones);
            WhatIf chosen = null;
            for (String zoneId : zoneOrder) {
                WhatIf whatIf = tryZone(problem, profile, current, vehicle, zoneId, zones.get(zoneId));
                if (chosen == null || COMMIT_ORDER.compare(whatIf, chosen) < 0) {
                    chosen = whatIf;
                }
            }
            if (chosen != null && !chosen.inserted().isEmpty()) {
                current = chosen.solution();                                    // 그 존의 what-if만 채택
                pending.removeAll(chosen.inserted());
            }
            // 아무것도 못 받은 차량은 사용하지 않는다 — 빈 Route를 만들지 않는다 (X9)
        }
        return current;
    }

    /** 차량 순서 = (담당 가능한 Request 수 ASC, maxWeight DESC, VehicleId ASC) — 까다로운 차부터. */
    static List<Vehicle> vehicleOrder(Problem problem) {
        Map<VehicleId, Integer> coverage = new LinkedHashMap<>();
        for (Vehicle vehicle : problem.vehicles()) {
            coverage.put(vehicle.id(), 0);
        }
        for (Request request : problem.requests()) {
            for (VehicleId vehicleId : problem.compatibleVehicles(request.id())) {
                coverage.merge(vehicleId, 1, Integer::sum);
            }
        }
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.<Vehicle>comparingInt(v -> coverage.get(v.id()))
                .thenComparing(Comparator.comparingLong(Vehicle::maxWeight).reversed())
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return vehicles;
    }

    /** zoneId(anchor side 기준, 부재 = "(none)")로 그룹핑. 그룹 안은 RequestId 순. */
    static Map<String, List<RequestId>> zoneGroups(Problem problem, Set<RequestId> pending) {
        Map<String, List<RequestId>> zones = new TreeMap<>();
        for (RequestId requestId : pending) {
            String zoneId = InsertionSearch.anchor(problem.request(requestId)).zoneId().orElse(NO_ZONE);
            zones.computeIfAbsent(zoneId, z -> new ArrayList<>()).add(requestId);
        }
        return zones;
    }

    /** 존 순서 = (호환 차량 집합 교집합 크기 ASC, 존 Request 수 DESC, zoneId ASC). */
    private static List<String> zoneOrder(Problem problem, Map<String, List<RequestId>> zones) {
        List<String> order = new ArrayList<>(zones.keySet());
        order.sort(Comparator.<String>comparingInt(z -> commonVehicles(problem, zones.get(z)).size())
                .thenComparing(Comparator.<String>comparingInt(z -> zones.get(z).size()).reversed())
                .thenComparing(Comparator.naturalOrder()));
        return order;
    }

    static Set<VehicleId> commonVehicles(Problem problem, List<RequestId> requestIds) {
        Set<VehicleId> common = null;
        for (RequestId requestId : requestIds) {
            if (common == null) {
                common = new LinkedHashSet<>(problem.compatibleVehicles(requestId));
            } else {
                common.retainAll(problem.compatibleVehicles(requestId));
            }
        }
        return common == null ? Set.of() : common;
    }

    /** what-if — 커밋하지 않는다. (요구 weight DESC, RequestId ASC)로 v의 빈 경로에 최소 비용 삽입. */
    private static WhatIf tryZone(
            Problem problem, Profile profile, Solution base, Vehicle vehicle, String zoneId, List<RequestId> members) {
        List<RequestId> order = new ArrayList<>(members);
        order.sort(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalWeight()).reversed()
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        Solution solution = base;
        List<RequestId> inserted = new ArrayList<>();
        long weight = 0L;
        long volume = 0L;
        for (RequestId requestId : order) {
            List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, solution, requestId, vehicle.id());
            if (cands.isEmpty()) {
                continue;
            }
            solution = InsertionSearch.apply(problem, solution, cands.getFirst());
            inserted.add(requestId);
            Request request = problem.request(requestId);
            weight = Math.addExact(weight, request.totalWeight());
            volume = Math.addExact(volume, request.totalVolume());
        }
        double loadRatio = Math.max(ratio(weight, vehicle.maxWeight()), ratio(volume, vehicle.maxVolume()));
        return new WhatIf(zoneId, solution, inserted, inserted.size() == members.size(), loadRatio);
    }

    private static double ratio(long used, long capacity) {
        return capacity == 0L ? 0.0 : (double) used / (double) capacity;
    }

    /**
     * 커밋 규칙: "전원 성공 ∧ 적재율 > 0.85"인 존이 있으면 그중 적재율 최대, 없으면 적재율 최대 존.
     * 부동소수는 정렬 키로만 — 동률은 zoneId ASC (§4.2).
     */
    private static final Comparator<WhatIf> COMMIT_ORDER = Comparator
            .comparing((WhatIf w) -> w.allInserted() && w.loadRatio() > LOAD_TARGET, Comparator.reverseOrder())
            .thenComparing(WhatIf::loadRatio, Comparator.reverseOrder())
            .thenComparing(WhatIf::zoneId);

    private record WhatIf(String zoneId, Solution solution, List<RequestId> inserted, boolean allInserted, double loadRatio) {}
}
