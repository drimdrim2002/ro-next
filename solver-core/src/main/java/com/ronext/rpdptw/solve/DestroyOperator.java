package com.ronext.rpdptw.solve;

import java.util.random.RandomGenerator;

import com.ronext.rpdptw.problem.Problem;

/** destroy SPI — pair 단위로 빼서 bank로 (stage-04 §3.1, Domain §9.1). */
public interface DestroyOperator {

    String id();

    /**
     * current에서 Request들을 pair 단위로 빼 bank에 넣은 새 Solution을 반환한다.
     * removeCount는 목표치(힌트)다 — 연산자는 pair 단위를 지키는 한 덜 뺄 수 있다.
     * 반환 해는 구조 검사를 통과해야 한다 (빈 visits 경로 금지 → 경로째 제거, §6 E4).
     */
    Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng);
}
