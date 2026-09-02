package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

/** H23 — 존 배정 DP + 부호 있는 정차 예산 best-fit + 1-1 교환 (heuristics 문서 §5 H23). */
public final class ZoneQuotaBalancedFillConstruction implements ConstructionHeuristic {

    static final String ID = "zone-quota-balanced-fill";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return ZoneQuotaAllocation.combinations(ZoneQuotaAllocation.vehicleTypes(problem))
                > ZoneQuotaAllocation.MAX_COMBINATIONS;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        Solution current = InsertionSearch.emptySolution(problem);
        int bound = 3 * problem.requests().size();
        int iterations = 0;
        List<RequestId> leftovers = new ArrayList<>();
        for (Zone zone : allocation.zones()) {
            List<VehicleId> bins = allocation.vehiclesByZone().get(zone.zoneId());
            List<RequestId> order = requestOrder(problem, allocation, zone.members());
            Map<VehicleId, Load> loads = new LinkedHashMap<>();
            for (VehicleId vehicleId : bins) {
                loads.put(vehicleId, new Load());
            }
            List<RequestId> zoneLeftovers = new ArrayList<>();
            for (int i = 0; i < order.size(); i++) {
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                RequestId requestId = order.get(i);
                Request request = problem.request(requestId);
                double mean = meanVolume(problem, order, i + 1);
                VehicleId chosen = null;
                Candidate chosenCandidate = null;
                double[] chosenKey = null;
                for (VehicleId vehicleId : bins) {
                    List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicleId);
                    if (cands.isEmpty()) {
                        continue;
                    }
                    double[] key = key(problem.vehicle(vehicleId), loads.get(vehicleId), request, mean);
                    if (chosen == null || compare(key, chosenKey) < 0) {        // 동률은 bins 순서(VehicleId ASC)의 앞
                        chosen = vehicleId;
                        chosenCandidate = cands.getFirst();
                        chosenKey = key;
                    }
                }
                if (chosen == null) {
                    zoneLeftovers.add(requestId);                                   // 규칙 b
                    continue;
                }
                current = InsertionSearch.apply(problem, current, chosenCandidate);   // 규칙 a
                loads.get(chosen).add(request);
            }
            for (RequestId requestId : sortedByVolumeDesc(problem, zoneLeftovers)) {
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                Solution exchanged = exchange(problem, profile, current, bins, requestId);
                if (exchanged != null) {
                    current = exchanged;                                            // 성공 = bank −1
                } else {
                    leftovers.add(requestId);
                }
            }
        }
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {   // 존에 안 들어간 요청(호환 0대)도 bank 그대로
            if (current.bank().contains(requestId) && !leftovers.contains(requestId)) {
                leftovers.add(requestId);
            }
        }
        for (RequestId requestId : requestOrder(problem, allocation, leftovers)) {     // leftover pass
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
            List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, requestId);
            if (!cands.isEmpty()) {
                current = InsertionSearch.apply(problem, current, cands.getFirst());
            }
        }
        return current;
    }

    /** 요청 순서 = (호환 유형 수 ASC, totalVolume DESC, RequestId ASC) — H23·H24·leftover pass 공통. */
    static List<RequestId> requestOrder(Problem problem, Allocation allocation, List<RequestId> members) {
        List<RequestId> order = new ArrayList<>(members);
        order.sort(Comparator.<RequestId>comparingInt(id -> ZoneQuotaAllocation.compatibleTypeCount(allocation.types(), id))
                .thenComparing(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume()).reversed())
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }

    private static List<RequestId> sortedByVolumeDesc(Problem problem, List<RequestId> ids) {
        List<RequestId> order = new ArrayList<>(ids);
        order.sort(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume()).reversed()
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }

    private static double meanVolume(Problem problem, List<RequestId> order, int from) {
        if (from >= order.size()) {
            return 0.0;
        }
        long total = 0L;
        for (int i = from; i < order.size(); i++) {
            total = Math.addExact(total, problem.request(order.get(i)).totalVolume());
        }
        return (double) total / (double) (order.size() - from);
    }

    /**
     * 선택 키 = (남을 부피 < 0, |남을 부피|, 남은 부피). 남을 부피 = (남은 부피 − volume(r)) − 평균 × (남은 정차 − 방문 수).
     * 정차 한도 부재면 남을 부피 = 남은 부피 − volume(r) (부피 best-fit으로 퇴화, X23). 부동소수는 정렬 키로만 (§4.2).
     */
    private static double[] key(Vehicle vehicle, Load load, Request request, double mean) {
        long remainingVolume = vehicle.maxVolume() - load.volume - request.totalVolume();
        double predicted = remainingVolume;
        if (vehicle.effectiveMaxStopCount().isPresent()) {
            int stopsLeft = vehicle.effectiveMaxStopCount().getAsInt() - load.visits - ZoneQuotaAllocation.visitCount(request);
            predicted = remainingVolume - mean * stopsLeft;
        }
        return new double[] {predicted < 0 ? 1 : 0, Math.abs(predicted), remainingVolume};
    }

    private static int compare(double[] a, double[] b) {
        for (int i = 0; i < a.length; i++) {
            int c = Double.compare(a[i], b[i]);
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /**
     * 1-1 교환: r의 존 경로 route(bins 순서)마다 volume(q) < volume(r)인 방문 q(totalVolume ASC, RequestId ASC)를 빼고
     * r을 그 경로에, q를 같은 존 다른 경로에 넣을 수 있으면 둘 다 적용. 첫 성공에서 멈춘다. 없으면 null.
     */
    private static Solution exchange(Problem problem, Profile profile, Solution current, List<VehicleId> bins, RequestId r) {
        long volume = problem.request(r).totalVolume();
        for (VehicleId vehicleId : bins) {
            List<RequestId> visitors = new ArrayList<>();
            for (var nodeId : InsertionSearch.visitsOf(current, vehicleId)) {
                RequestId q = InsertionSearch.requestOf(problem, nodeId);
                if (!visitors.contains(q) && problem.request(q).totalVolume() < volume) {
                    visitors.add(q);
                }
            }
            visitors.sort(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume())
                    .thenComparing(InsertionSearch.BY_REQUEST_ID));
            for (RequestId q : visitors) {
                Solution without = InsertionSearch.remove(problem, current, q);
                List<Candidate> forR = InsertionSearch.candidatesFor(problem, profile, without, r, vehicleId);
                if (forR.isEmpty()) {
                    continue;
                }
                Solution withR = InsertionSearch.apply(problem, without, forR.getFirst());
                for (VehicleId other : bins) {
                    if (other.equals(vehicleId)) {
                        continue;
                    }
                    List<Candidate> forQ = InsertionSearch.candidatesFor(problem, profile, withR, q, other);
                    if (!forQ.isEmpty()) {
                        return InsertionSearch.apply(problem, withR, forQ.getFirst());
                    }
                }
            }
        }
        return null;
    }

    private static final class Load {
        long volume;
        int visits;

        void add(Request request) {
            volume = Math.addExact(volume, request.totalVolume());
            visits += ZoneQuotaAllocation.visitCount(request);
        }
    }
}
