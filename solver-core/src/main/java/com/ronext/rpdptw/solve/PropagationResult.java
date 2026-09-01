package com.ronext.rpdptw.solve;

import java.util.Objects;
import java.util.Optional;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.eval.RouteFacts;

public sealed interface PropagationResult {

    record Feasible(RouteFacts facts) implements PropagationResult {

        public Feasible {
            Objects.requireNonNull(facts, "facts");
        }
    }

    record Infeasible(Violation violation, Optional<NodeId> at) implements PropagationResult {

        public Infeasible {
            Objects.requireNonNull(violation, "violation");
            at = Objects.requireNonNull(at, "at");
        }
    }
}
