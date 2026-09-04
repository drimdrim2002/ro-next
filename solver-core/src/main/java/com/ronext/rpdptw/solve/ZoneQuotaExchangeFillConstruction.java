package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Demand;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.VehicleType;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

/**
 * H25 — greedy 존 배정 + 구역 간 교환(MOVE·SWAP) → H23 2단계 재사용 (heuristics 문서 §5 H25).
 * 배정의 값은 존 배정 DP와 **같은 함수**({@link ZoneQuotaAllocation#value})라 두 기법의 배정을 수치로 비교할 수 있다.
 * {@link ZoneQuotaAllocation#frontier}를 부르지 않으므로 유형 조합 수 Π와 무관하게 돌고 기권하지 않는다 (X31).
 */
public final class ZoneQuotaExchangeFillConstruction implements ConstructionHeuristic {

    static final String ID = "zone-quota-exchange-fill";
    /** 재량 상수 — 교환 스캔 라운드 상한. 개선 없는 스캔 1회면 그 전에 끝난다 (§4.3). */
    static final int MAX_SCANS = 50;

    private static final long[] NO_VALUE = {0L, 0L, 0L};

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
        return ZoneQuotaBalancedFillConstruction.fill(problem, profile, allocate(problem), ID);
    }

    /** greedy 덮개 + 구역 간 교환으로 만든 배정 — 반환형은 DP와 같은 Allocation, truncated = false. */
    static Allocation allocate(Problem problem) {
        return allocate(problem, new ArrayList<>());
    }

    /** 스캔 기록용 오버로드 — 이웃을 적용할 때마다 그때의 (Σ부족, Σ낭비, Σ사용 대수)를 valueTrace에 남긴다 (T54). */
    static Allocation allocate(Problem problem, List<long[]> valueTrace) {
        List<VehicleType> types = ZoneQuotaAllocation.vehicleTypes(problem);
        List<Zone> zones = ZoneQuotaAllocation.zones(problem, types);
        int typeCount = types.size();
        int zoneCount = zones.size();

        Demand[] demands = new Demand[zoneCount];
        for (int z = 0; z < zoneCount; z++) {
            demands[z] = Demand.of(problem, types, zones.get(z));
        }

        int[][] quotas = new int[zoneCount + 1][typeCount];             // 마지막 행 = pool (미배정 차량)
        for (int t = 0; t < typeCount; t++) {
            quotas[zoneCount][t] = types.get(t).vehicles().size();
        }
        greedy(types, demands, quotas, zoneCount, typeCount, problem.vehicles().size());
        exchange(types, demands, quotas, zoneCount, typeCount, valueTrace);

        int[] cursor = new int[typeCount];
        Map<String, List<VehicleId>> vehiclesByZone = new LinkedHashMap<>();
        for (int z = 0; z < zoneCount; z++) {
            List<VehicleId> vehicles = new ArrayList<>();
            for (int t = 0; t < typeCount; t++) {
                for (int i = 0; i < quotas[z][t]; i++) {
                    vehicles.add(types.get(t).vehicles().get(cursor[t]++));
                }
            }
            vehiclesByZone.put(zones.get(z).zoneId(), List.copyOf(vehicles));
        }
        return new Allocation(types, zones, vehiclesByZone, false);
    }

    /**
     * 존 순서대로 1회 — 호환 유형을 큰 차부터 라운드 로빈으로 더해 덮고(Hall 조건), 못 덮으면 비우고,
     * 덮었으면 작은 차부터 빼며 최소 덮개로 깎는다 (§5 H25 greedy 덮개). pool(마지막 행)에서 꺼내 쓴다.
     */
    private static void greedy(
            List<VehicleType> types, Demand[] demands, int[][] quotas, int zoneCount, int typeCount, int vehicleCount) {
        int bound = Math.max(1, 2 * vehicleCount * zoneCount);
        int iterations = 0;
        int[] pool = quotas[zoneCount];
        int[] s = new int[typeCount];
        for (int z = 0; z < zoneCount; z++) {
            Demand demand = demands[z];
            Arrays.fill(s, 0);
            while (!demand.covers(s)) {
                boolean added = false;
                for (int t = typeCount - 1; t >= 0; t--) {                  // 유형 순서 DESC — 큰 차부터
                    if ((demand.compat & (1L << t)) == 0 || pool[t] == 0) {
                        continue;
                    }
                    InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                    s[t]++;
                    pool[t]--;
                    added = true;
                    if (demand.covers(s)) {
                        break;
                    }
                }
                if (!added) {
                    break;                                                   // 공급 부족 (X30)
                }
            }
            if (!demand.covers(s)) {
                for (int t = 0; t < typeCount; t++) {                        // 못 덮는 존에 차를 쏟아붓지 않는다 (DP (c))
                    pool[t] += s[t];
                    s[t] = 0;
                }
            } else {
                for (int t = 0; t < typeCount; t++) {                        // trim — 작은 차부터 빼며 최소 덮개로
                    while (s[t] > 0) {
                        InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                        s[t]--;
                        if (demand.covers(s)) {
                            pool[t]++;
                        } else {
                            s[t]++;
                            break;
                        }
                    }
                }
            }
            System.arraycopy(s, 0, quotas[z], 0, typeCount);
        }
    }

    /**
     * 구역 간 교환 (best-improvement · 순회 순서 고정) — 스캔마다 이웃 전체를 보고 값이 사전식으로 가장 작아지는
     * 하나만 적용한다. 동률은 먼저 만난 이웃을 유지하므로 순회 순서가 곧 동률 규칙이다 (§5 H25).
     */
    private static void exchange(
            List<VehicleType> types, Demand[] demands, int[][] quotas, int zoneCount, int typeCount,
            List<long[]> valueTrace) {
        long[][] nodeValue = new long[zoneCount + 1][];
        long[] total = {0L, 0L, 0L};
        for (int z = 0; z < zoneCount; z++) {
            nodeValue[z] = ZoneQuotaAllocation.value(demands[z], types, quotas[z]);
            add(total, nodeValue[z]);
        }
        nodeValue[zoneCount] = NO_VALUE;                                     // pool의 값 기여는 0

        for (int scan = 1; scan <= MAX_SCANS; scan++) {
            int bestA = -1;
            int bestB = -1;
            int bestT = -1;
            int bestU = -1;
            long[] bestValue = null;
            for (int a = 0; a <= zoneCount; a++) {
                for (int b = 0; b <= zoneCount; b++) {
                    if (a == b) {
                        continue;
                    }
                    long[] base = {total[0] - nodeValue[a][0] - nodeValue[b][0],
                                   total[1] - nodeValue[a][1] - nodeValue[b][1],
                                   total[2] - nodeValue[a][2] - nodeValue[b][2]};
                    long compatB = b == zoneCount ? -1L : demands[b].compat;
                    long compatA = a == zoneCount ? -1L : demands[a].compat;
                    for (int t = 0; t < typeCount; t++) {                    // MOVE(A → B, t)
                        if (quotas[a][t] == 0 || (compatB & (1L << t)) == 0) {
                            continue;
                        }
                        quotas[a][t]--;
                        quotas[b][t]++;
                        long[] candidate = pairValue(base, types, demands, quotas, zoneCount, a, b);
                        quotas[a][t]++;
                        quotas[b][t]--;
                        if (improves(candidate, total, bestValue)) {
                            bestValue = candidate;
                            bestA = a;
                            bestB = b;
                            bestT = t;
                            bestU = -1;
                        }
                    }
                    for (int t = 0; t < typeCount; t++) {                    // SWAP(A ↔ B, t, u)
                        if (quotas[a][t] == 0 || (compatB & (1L << t)) == 0) {
                            continue;
                        }
                        for (int u = 0; u < typeCount; u++) {
                            if (u == t || quotas[b][u] == 0 || (compatA & (1L << u)) == 0) {
                                continue;
                            }
                            quotas[a][t]--;
                            quotas[a][u]++;
                            quotas[b][u]--;
                            quotas[b][t]++;
                            long[] candidate = pairValue(base, types, demands, quotas, zoneCount, a, b);
                            quotas[a][t]++;
                            quotas[a][u]--;
                            quotas[b][u]++;
                            quotas[b][t]--;
                            if (improves(candidate, total, bestValue)) {
                                bestValue = candidate;
                                bestA = a;
                                bestB = b;
                                bestT = t;
                                bestU = u;
                            }
                        }
                    }
                }
            }
            if (bestValue == null) {
                return;                                                      // 국소 최적 (X34)
            }
            quotas[bestA][bestT]--;
            quotas[bestB][bestT]++;
            if (bestU >= 0) {
                quotas[bestA][bestU]++;
                quotas[bestB][bestU]--;
            }
            nodeValue[bestA] = nodeValue(types, demands, quotas, zoneCount, bestA);
            nodeValue[bestB] = nodeValue(types, demands, quotas, zoneCount, bestB);
            total = bestValue;
            valueTrace.add(bestValue.clone());
        }
        // MAX_SCANS 도달 — 그때까지의 최선 배정으로 2단계로 간다. 예외가 아니다 (X29).
    }

    /** 이웃 적용 뒤의 값 = 두 노드를 뺀 나머지(base) + 두 노드의 새 값. */
    private static long[] pairValue(
            long[] base, List<VehicleType> types, Demand[] demands, int[][] quotas, int zoneCount, int a, int b) {
        long[] candidate = base.clone();
        add(candidate, nodeValue(types, demands, quotas, zoneCount, a));
        add(candidate, nodeValue(types, demands, quotas, zoneCount, b));
        return candidate;
    }

    private static long[] nodeValue(
            List<VehicleType> types, Demand[] demands, int[][] quotas, int zoneCount, int node) {
        return node == zoneCount ? NO_VALUE : ZoneQuotaAllocation.value(demands[node], types, quotas[node]);
    }

    /** 현재 값보다 사전식으로 엄격히 작고, 이번 스캔의 최선보다도 엄격히 작은가 (동률은 먼저 만난 이웃 유지). */
    private static boolean improves(long[] candidate, long[] total, long[] best) {
        return ZoneQuotaAllocation.compare(candidate, total) < 0
                && (best == null || ZoneQuotaAllocation.compare(candidate, best) < 0);
    }

    private static void add(long[] target, long[] value) {
        for (int i = 0; i < target.length; i++) {
            target[i] += value[i];
        }
    }
}
