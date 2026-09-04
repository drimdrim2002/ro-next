package com.ronext.rpdptw.solve;

import java.util.Objects;

/** 로그·실험용 카운터 — 결과 JSON run 메타에는 넣지 않는다 (stage-04 §3.2, Domain §11.1). */
public record AlnsRunStats(
        long iterations,
        long accepted,
        long infeasibleDiscarded,
        long bestImproved,
        long elapsedMillis,
        Termination termination) {

    public AlnsRunStats {
        Objects.requireNonNull(termination, "termination");
    }

    /** 어느 종료 조건에 걸렸는지 (§3.3). 별도 파일이 아니라 여기 중첩 — 선례 ConstructionOutcome.Status. */
    public enum Termination { TIME_LIMIT, MAX_STEPS, IDLE_STEPS, IDLE_TIME }
}
