package com.ronext.rpdptw.solve;

import java.util.random.RandomGenerator;

import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** repair SPI — bank의 Request를 pair 삽입 (stage-04 §3.1, Domain §9.1). */
public interface RepairOperator {

    String id();

    /**
     * bank의 Request들을 (이전부터 bank였던 것 포함) 가능한 만큼 pair 삽입한 새 Solution.
     * 0건 삽입도 정상 반환이다 (Domain §9.1). 반환 해는 구조 검사를 통과해야 한다.
     * profile을 받는다 — §4.3-②의 후보 검증(profile hard)이 InsertionSearch에 profile을 요구한다.
     */
    Solution repair(Problem problem, Profile profile, Solution destroyed, RandomGenerator rng);
}
