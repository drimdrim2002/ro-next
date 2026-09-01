package com.ronext.rpdptw.solve;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.RouteFacts;

public sealed interface EvaluationResult {

    record Feasible(Evaluation evaluation, long[] score, Map<VehicleId, RouteFacts> routes)
            implements EvaluationResult {

        public Feasible {
            Objects.requireNonNull(evaluation, "evaluation");
            Objects.requireNonNull(score, "score");
            score = Arrays.copyOf(score, score.length);
            routes = Map.copyOf(routes);
        }

        @Override
        public long[] score() {
            return Arrays.copyOf(score, score.length);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Feasible that)) {
                return false;
            }
            return evaluation.equals(that.evaluation)
                    && Arrays.equals(score, that.score)
                    && routes.equals(that.routes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(evaluation, Arrays.hashCode(score), routes);
        }
    }

    record Infeasible(
            VehicleId vehicleId,
            Violation violation,
            Optional<NodeId> at,
            Optional<String> hardConstraintId)
            implements EvaluationResult {

        public Infeasible {
            Objects.requireNonNull(vehicleId, "vehicleId");
            Objects.requireNonNull(violation, "violation");
            at = Objects.requireNonNull(at, "at");
            hardConstraintId = Objects.requireNonNull(hardConstraintId, "hardConstraintId");
        }
    }
}
