package com.ronext.rpdptw.eval;

import com.ronext.rpdptw.problem.Problem;

public interface HardConstraint {

    String id();

    /** 전파를 통과한 경로 사실에 대한 추가 hard 판정. 위반이면 false. */
    boolean satisfied(Problem problem, RouteFacts route);
}
