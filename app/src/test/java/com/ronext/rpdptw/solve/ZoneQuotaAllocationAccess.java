package com.ronext.rpdptw.solve;

import com.ronext.rpdptw.problem.Problem;

/**
 * T52 전용 접근자 — `ZoneQuotaAllocation`은 `solve` 패키지 안에만 보이므로 app 모듈 테스트가
 * `Allocation.truncated`를 관측하려면 같은 패키지의 테스트 소스를 하나 거쳐야 한다.
 * 운영 코드의 가시성은 그대로다 (heuristics 문서 §8 T52).
 */
public final class ZoneQuotaAllocationAccess {

    private ZoneQuotaAllocationAccess() {}

    /** 존 배정 DP가 폭 제한으로 상태를 잘라 근사했는가. */
    public static boolean truncated(Problem problem) {
        return ZoneQuotaAllocation.allocate(problem).truncated();
    }
}
