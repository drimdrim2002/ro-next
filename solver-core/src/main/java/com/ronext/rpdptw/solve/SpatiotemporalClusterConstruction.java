package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/**
 * H21 — 시공간 거리 응집 클러스터 → 차량 배정, Kerscher & Minner (heuristics 문서 §5 H21).
 * S_ij = D_ij × (2 − (f_ij − h_ij)/planEndSec + (w_i + w_j)/W), 두 방향 중 유효한(f ≥ 0) 쪽의 최소.
 * complete-linkage 응집 — 초기화가 없어 그 자체로 결정적이다. 부동소수는 정렬 키로만 쓴다 (§4.2).
 */
public final class SpatiotemporalClusterConstruction implements ConstructionHeuristic {

    static final String ID = "spatiotemporal-cluster";

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
        List<RequestId> ids = InsertionSearch.sortedRequestIds(problem);
        List<Vehicle> vehicles = InsertionSearch.vehiclesByCapacityDesc(problem);
        if (vehicles.isEmpty() || ids.isEmpty()) {
            return InsertionSearch.emptySolution(problem);
        }
        int n = ids.size();
        double[][] distance = pairwiseDistance(problem, ids, vehicles.getFirst());

        // 클러스터링 — 요청마다 클러스터 1개에서 시작, 병합 가능한 쌍 중 linkage 최소를 병합 (≤ n−1 회)
        List<Cluster> clusters = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            clusters.add(Cluster.single(problem, ids.get(i)));
        }
        double[][] linkage = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                linkage[i][j] = distance[i][j];
            }
        }
        boolean[] alive = new boolean[n];
        java.util.Arrays.fill(alive, true);
        int merges = 0;
        while (true) {
            int bestA = -1;
            int bestB = -1;
            for (int a = 0; a < n; a++) {
                if (!alive[a]) {
                    continue;
                }
                for (int b = 0; b < n; b++) {
                    if (a == b || !alive[b] || Double.isNaN(linkage[a][b])) {
                        continue;
                    }
                    Cluster first = clusters.get(a);
                    Cluster second = clusters.get(b);
                    if (first.min.value().compareTo(second.min.value()) >= 0) {
                        continue;                                               // (min, 상대 min) 순서 고정 — 한 번만 본다
                    }
                    if (!first.canMergeWith(problem, second)) {
                        continue;
                    }
                    if (bestA < 0 || better(linkage[a][b], first, second, linkage[bestA][bestB], clusters.get(bestA), clusters.get(bestB))) {
                        bestA = a;
                        bestB = b;
                    }
                }
            }
            if (bestA < 0) {
                break;
            }
            InsertionSearch.checkOuterLoop(ID, ++merges, Math.max(n - 1, 0));
            clusters.set(bestA, clusters.get(bestA).mergedWith(problem, clusters.get(bestB)));
            alive[bestB] = false;
            for (int c = 0; c < n; c++) {                                      // complete linkage = 두 값의 max, 무효는 전파
                double merged = Double.isNaN(linkage[bestA][c]) || Double.isNaN(linkage[bestB][c])
                        ? Double.NaN : Math.max(linkage[bestA][c], linkage[bestB][c]);
                linkage[bestA][c] = merged;
                linkage[c][bestA] = merged;
            }
        }

        // 배정: 클러스터 순서 (교집합 크기 ASC, Σweight DESC, min RequestId ASC)
        List<Cluster> order = new ArrayList<>();
        for (int c = 0; c < n; c++) {
            if (alive[c]) {
                order.add(clusters.get(c));
            }
        }
        order.sort(Comparator.<Cluster>comparingInt(c -> c.common.size())
                .thenComparing(Comparator.<Cluster>comparingLong(c -> c.weight).reversed())
                .thenComparing(c -> c.min, InsertionSearch.BY_REQUEST_ID));
        Solution current = InsertionSearch.emptySolution(problem);
        int iterations = 0;
        for (Cluster cluster : order) {
            Set<VehicleId> used = InsertionSearch.usedVehicles(current);
            Optional<Vehicle> vehicle = problem.vehicles().stream()
                    .filter(v -> cluster.common.contains(v.id()) && !used.contains(v.id()))
                    .min(Comparator.comparingLong(Vehicle::maxWeight).thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
            if (vehicle.isEmpty()) {
                continue;                                                       // 차량을 못 받은 클러스터의 요청은 bank
            }
            current = ZoneFirstFfdConstruction.regret2Fill(
                    problem, profile, current, vehicle.get().id(), cluster.members, ID, iterations, n);
            iterations += cluster.members.size();
        }
        return current;
    }

    private static boolean better(double link, Cluster a, Cluster b, double bestLink, Cluster bestA, Cluster bestB) {
        if (link != bestLink) {
            return link < bestLink;
        }
        int byMin = a.min.value().compareTo(bestA.min.value());
        return byMin != 0 ? byMin < 0 : b.min.value().compareTo(bestB.min.value()) < 0;
    }

    /** S(i,j) 행렬 — 무효(양방향 f < 0)는 NaN. 거리·시간은 이동표에서만 (Domain §4). */
    static double[][] pairwiseDistance(Problem problem, List<RequestId> ids, Vehicle v0) {
        int n = ids.size();
        int speed = problem.resolvedSpeedKmH(v0.id());
        long capacity = 0L;
        for (Vehicle vehicle : problem.vehicles()) {
            capacity = Math.max(capacity, vehicle.maxWeight());
        }
        double planEnd = problem.planEndSec();
        double[][] s = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    s[i][j] = 0.0;
                    continue;
                }
                double forward = directed(problem, problem.request(ids.get(i)), problem.request(ids.get(j)), speed, capacity, planEnd);
                double backward = directed(problem, problem.request(ids.get(j)), problem.request(ids.get(i)), speed, capacity, planEnd);
                if (Double.isNaN(forward)) {
                    s[i][j] = backward;
                } else if (Double.isNaN(backward)) {
                    s[i][j] = forward;
                } else {
                    s[i][j] = Math.min(forward, backward);
                }
            }
        }
        return s;
    }

    /** i 다음 j: f_ij ≥ 0이어야 유효. 아니면 NaN. */
    static double directed(Problem problem, Request i, Request j, int speed, long capacity, double planEnd) {
        RequestSide ai = InsertionSearch.anchor(i);
        RequestSide aj = InsertionSearch.anchor(j);
        LocationId from = ai.locationId();
        LocationId to = aj.locationId();
        long t = problem.travel().timeSec(from, to, speed);
        long ei = InsertionSearch.firstOpenSec(ai);
        long li = InsertionSearch.lastCloseSec(ai);
        long ej = InsertionSearch.firstOpenSec(aj);
        long lj = InsertionSearch.lastCloseSec(aj);
        long si = ai.serviceTimeSec();
        long f = lj - (ei + si + t);
        if (f < 0L) {
            return Double.NaN;
        }
        long h = Math.max(ej - (li + si + t), 0L);
        double load = capacity == 0L ? 0.0 : (double) (i.totalWeight() + j.totalWeight()) / (double) capacity;
        double d = problem.travel().distanceMeter(from, to);
        return d * (2.0 - (double) (f - h) / planEnd + load);
    }

    private static final class Cluster {
        final List<RequestId> members;
        final RequestId min;
        final Set<VehicleId> common;
        final long weight;
        final long volume;

        private Cluster(List<RequestId> members, RequestId min, Set<VehicleId> common, long weight, long volume) {
            this.members = members;
            this.min = min;
            this.common = common;
            this.weight = weight;
            this.volume = volume;
        }

        static Cluster single(Problem problem, RequestId id) {
            Request request = problem.request(id);
            return new Cluster(List.of(id), id, new LinkedHashSet<>(problem.compatibleVehicles(id)),
                    request.totalWeight(), request.totalVolume());
        }

        /** 병합 가능 = 호환 교집합 ≠ ∅ · Σweight·Σvolume이 교집합 내 최대 차량 이하 · 요청 수 ≤ 그 차량 B. */
        boolean canMergeWith(Problem problem, Cluster other) {
            Set<VehicleId> merged = new LinkedHashSet<>(common);
            merged.retainAll(other.common);
            if (merged.isEmpty()) {
                return false;
            }
            long maxWeight = 0L;
            long maxVolume = 0L;
            int stopLimit = 0;
            for (VehicleId id : merged) {
                Vehicle vehicle = problem.vehicle(id);
                maxWeight = Math.max(maxWeight, vehicle.maxWeight());
                maxVolume = Math.max(maxVolume, vehicle.maxVolume());
                stopLimit = Math.max(stopLimit, vehicle.effectiveMaxStopCount().orElse(problem.requests().size()));
            }
            return weight + other.weight <= maxWeight
                    && volume + other.volume <= maxVolume
                    && members.size() + other.members.size() <= stopLimit;
        }

        Cluster mergedWith(Problem problem, Cluster other) {
            List<RequestId> all = new ArrayList<>(members);
            all.addAll(other.members);
            all.sort(InsertionSearch.BY_REQUEST_ID);
            Set<VehicleId> merged = new LinkedHashSet<>(common);
            merged.retainAll(other.common);
            return new Cluster(all, all.getFirst(), merged, weight + other.weight, volume + other.volume);
        }
    }
}
