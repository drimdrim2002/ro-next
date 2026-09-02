package com.ronext.rpdptw.solve;

import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** construction SPI — 난수 없이 rule로 초기해 하나를 만든다 (heuristics 문서 §3.2). */
public interface ConstructionHeuristic {

    String id();

    /**
     * 이 Problem에서 이 기법이 성립하지 않으면 true — 실행하지 않고 건너뛴다.
     * 기권은 실패가 아니다 (§4.4). Problem만 보고 판정하며 부작용이 없다.
     */
    boolean abstains(Problem problem);

    /**
     * 빈 해에서 시작해 Request를 pair 단위로 삽입한 Solution 하나를 만든다.
     * 넣지 못한 Request는 bank에 남는다 — 유효한 해다 (Domain §9.1).
     * 반환 해는 StructureCheck를 통과하고 Evaluator Feasible이어야 한다 (§4.1).
     */
    Solution construct(Problem problem, Profile profile);
}
