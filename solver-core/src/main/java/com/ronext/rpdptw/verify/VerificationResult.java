package com.ronext.rpdptw.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.RouteFacts;

public sealed interface VerificationResult {

    /**
     * 검증 통과. 담긴 값은 전부 재계산본 — 결과 조립의 유일한 원천 (stage-05 §5 N4).
     * long[] 필드 때문에 equals로 비교하지 않는다 — score 대조는 Arrays.equals다
     * (stage-05 §2.1의 전 Stage 공통 규칙, AlnsResult·Feasible과 같은 취급).
     */
    record Pass(Evaluation evaluation,
                long[] score,
                Map<VehicleId, RouteFacts> routeFacts,
                Set<RequestId> bank)
            implements VerificationResult {

        public Pass {
            Objects.requireNonNull(evaluation, "evaluation");
            score = Arrays.copyOf(Objects.requireNonNull(score, "score"), score.length);
            routeFacts = Map.copyOf(routeFacts);
            bank = Set.copyOf(bank);
        }

        @Override
        public long[] score() {
            return Arrays.copyOf(score, score.length);
        }
    }

    record Fail(List<VerifyViolation> violations) implements VerificationResult {

        public Fail {
            violations = List.copyOf(violations);
        }
    }
}
