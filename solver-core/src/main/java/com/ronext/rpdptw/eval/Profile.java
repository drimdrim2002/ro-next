package com.ronext.rpdptw.eval;

import java.util.Collection;
import java.util.List;

import com.ronext.rpdptw.problem.Problem;

public interface Profile {

    String id();

    List<HardConstraint> hardConstraints();

    /**
     * 이 profile의 목적식 축(층 ④). 전부 "작을수록 좋다".
     * 같은 profile은 호출마다 항상 같은 길이를 반환한다.
     */
    long[] score(Problem problem, Evaluation metrics, Collection<RouteFacts> routes);
}
