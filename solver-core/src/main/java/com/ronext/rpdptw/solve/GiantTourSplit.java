package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;

/**
 * H18·H19 공통 — 요청 순열의 DP 최적 분할, Beasley/Prins Split (heuristics 문서 §5 H18).
 * 상태 f(i, k) = "σ[1..i] 처리·V[1..k] 소비"의 최소 키 (bank 수, 사용 차량 수, Σ거리) 사전식.
 * 상태 수 (n+1)(m+1) 고정 — 루프 상한이 아니라 구조로 종료한다 (§4.3).
 */
final class GiantTourSplit {

    private GiantTourSplit() {}

    static Solution split(Problem problem, Profile profile, List<RequestId> sigma, List<Vehicle> vehicles) {
        int n = sigma.size();
        int m = vehicles.size();
        long[][][] best = new long[n + 1][m + 1][];
        int[][][] parent = new int[n + 1][m + 1][];                            // {pi, pk, kind}: 1 skip · 2 bank · 3 route
        best[0][0] = new long[] {0L, 0L, 0L};

        for (int i = 0; i <= n; i++) {
            for (int k = 0; k <= m; k++) {
                long[] from = best[i][k];
                if (from == null) {
                    continue;
                }
                if (k < m) {                                                    // ① 차량 V[k+1]을 건너뜀
                    relax(best, parent, i, k + 1, from, i, k, 1);
                }
                if (i < n) {                                                    // ② σ[i+1]을 bank
                    relax(best, parent, i + 1, k, new long[] {from[0] + 1, from[1], from[2]}, i, k, 2);
                }
                if (i < n && k < m) {                                           // ③ σ[i+1..j]를 V[k+1]의 경로로
                    Vehicle vehicle = vehicles.get(k);
                    int limit = vehicle.effectiveMaxStopCount().isPresent()
                            ? Math.min(n, i + vehicle.effectiveMaxStopCount().getAsInt()) : n;
                    List<NodeId> visits = new ArrayList<>();
                    for (int j = i + 1; j <= limit; j++) {
                        appendPair(problem, sigma.get(j - 1), visits);
                        Optional<RouteFacts> facts = InsertionSearch.validate(problem, profile, vehicle.id(), visits);
                        if (facts.isEmpty()) {
                            continue;                                           // 불가하면 전이 없음
                        }
                        relax(best, parent, j, k + 1,
                                new long[] {from[0], from[1] + 1, from[2] + facts.get().driveDistMeter()}, i, k, 3);
                    }
                }
            }
        }

        // 최종 상태 = f(n, m) — ①이 남은 차량을 전부 건너뛰므로 항상 도달한다 (X16)
        List<Route> routes = new ArrayList<>();
        Set<RequestId> bank = new LinkedHashSet<>();
        int i = n;
        int k = m;
        while (i != 0 || k != 0) {
            int[] p = parent[i][k];
            if (p[2] == 2) {
                bank.add(sigma.get(i - 1));
            } else if (p[2] == 3) {
                List<NodeId> visits = new ArrayList<>();
                for (int t = p[0] + 1; t <= i; t++) {
                    appendPair(problem, sigma.get(t - 1), visits);
                }
                routes.add(new Route(vehicles.get(p[1]).id(), visits));
            }
            i = p[0];
            k = p[1];
        }
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
            if (!sigma.contains(requestId)) {
                bank.add(requestId);
            }
        }
        return new Solution(routes.reversed(), bank);
    }

    /** 동률 = 먼저 계산된 값 유지 — 엄격히 작을 때만 갱신 (결정적). */
    private static void relax(long[][][] best, int[][][] parent, int i, int k, long[] key, int pi, int pk, int kind) {
        if (best[i][k] == null || compare(key, best[i][k]) < 0) {
            best[i][k] = key;
            parent[i][k] = new int[] {pi, pk, kind};
        }
    }

    static int compare(long[] a, long[] b) {
        for (int t = 0; t < 3; t++) {
            if (a[t] != b[t]) {
                return a[t] < b[t] ? -1 : 1;
            }
        }
        return 0;
    }

    /** 순열 순으로, PICKUP_DELIVERY는 p 바로 뒤에 d. */
    static void appendPair(Problem problem, RequestId requestId, List<NodeId> visits) {
        Request request = problem.request(requestId);
        request.pickup().ifPresent(side -> visits.add(side.nodeId()));
        request.delivery().ifPresent(side -> visits.add(side.nodeId()));
    }
}
