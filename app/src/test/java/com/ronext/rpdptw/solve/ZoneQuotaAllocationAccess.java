package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.List;

import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Allocation;
import com.ronext.rpdptw.solve.ZoneQuotaAllocation.Zone;

/**
 * T52·T56 전용 접근자 — `ZoneQuotaAllocation`은 `solve` 패키지 안에만 보이므로 app 모듈 테스트가
 * `Allocation.truncated`나 배정 값을 관측하려면 같은 패키지의 테스트 소스를 하나 거쳐야 한다.
 * 운영 코드의 가시성은 그대로다 (heuristics 문서 §8 T52·T56).
 */
public final class ZoneQuotaAllocationAccess {

    private ZoneQuotaAllocationAccess() {}

    /** 존 배정 DP가 폭 제한으로 상태를 잘라 근사했는가. */
    public static boolean truncated(Problem problem) {
        return ZoneQuotaAllocation.allocate(problem).truncated();
    }

    /** 존 배정 DP(H23·H24)의 배정 값 (Σ부족, Σ낭비, Σ사용 대수). */
    public static long[] dpAllocationValue(Problem problem) {
        return totalValue(problem, ZoneQuotaAllocation.allocate(problem));
    }

    /** H25 greedy + 구역 간 교환의 배정 값 (같은 값 함수라 DP 값과 직접 비교된다). */
    public static long[] exchangeAllocationValue(Problem problem) {
        return totalValue(problem, ZoneQuotaExchangeFillConstruction.allocate(problem));
    }

    /** H25 교환이 이웃을 적용한 횟수 (= valueTrace 길이). */
    public static int exchangeScanCount(Problem problem) {
        List<long[]> trace = new ArrayList<>();
        ZoneQuotaExchangeFillConstruction.allocate(problem, trace);
        return trace.size();
    }

    private static long[] totalValue(Problem problem, Allocation allocation) {
        long[] total = {0L, 0L, 0L};
        for (Zone zone : allocation.zones()) {
            int[] quota = new int[allocation.types().size()];
            for (VehicleId id : allocation.vehiclesByZone().get(zone.zoneId())) {
                for (int t = 0; t < allocation.types().size(); t++) {
                    if (allocation.types().get(t).vehicles().contains(id)) {
                        quota[t]++;
                        break;
                    }
                }
            }
            long[] value = ZoneQuotaAllocation.value(
                    ZoneQuotaAllocation.Demand.of(problem, allocation.types(), zone), allocation.types(), quota);
            for (int i = 0; i < 3; i++) {
                total[i] += value[i];
            }
        }
        return total;
    }
}
