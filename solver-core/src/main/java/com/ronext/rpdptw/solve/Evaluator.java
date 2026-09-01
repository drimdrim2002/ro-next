package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;

public final class Evaluator {

    private Evaluator() {}

    /**
     * 전제: StructureCheck 위반 0. 구조를 재검사하지 않는다. Problem·profile은 읽기만 한다.
     * profile은 인자다 — Problem에 담기지 않는다.
     */
    public static EvaluationResult evaluate(Problem problem, Profile profile, Solution solution) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(solution, "solution");

        Map<VehicleId, RouteFacts> factsByVehicle = new LinkedHashMap<>();
        List<RouteFacts> facts = new ArrayList<>();
        for (Route route : solution.routes()) {
            PropagationResult propagated = RoutePropagator.propagate(problem, route.vehicleId(), route.visits());
            if (propagated instanceof PropagationResult.Infeasible infeasible) {
                return new EvaluationResult.Infeasible(
                        route.vehicleId(), infeasible.violation(), infeasible.at(), Optional.empty());
            }
            RouteFacts routeFacts = ((PropagationResult.Feasible) propagated).facts();

            Optional<EvaluationResult.Infeasible> compatibility = checkCompatibility(problem, route, routeFacts);
            if (compatibility.isPresent()) {
                return compatibility.get();
            }
            for (HardConstraint constraint : profile.hardConstraints()) {
                if (!constraint.satisfied(problem, routeFacts)) {
                    return new EvaluationResult.Infeasible(
                            route.vehicleId(),
                            Violation.PROFILE_HARD,
                            Optional.empty(),
                            Optional.of(constraint.id()));
                }
            }
            factsByVehicle.put(route.vehicleId(), routeFacts);
            facts.add(routeFacts);
        }

        Evaluation evaluation = Evaluation.aggregate(facts, solution.bank().size());
        long[] score = profile.score(problem, evaluation, facts);
        return new EvaluationResult.Feasible(evaluation, score, factsByVehicle);
    }

    private static Optional<EvaluationResult.Infeasible> checkCompatibility(
            Problem problem, Route route, RouteFacts facts) {
        Set<RequestId> seen = new LinkedHashSet<>();
        for (var visit : facts.visits()) {
            RequestId requestId = visit.requestId();
            if (!seen.add(requestId)) {
                continue;
            }
            if (!problem.compatibleVehicles(requestId).contains(route.vehicleId())) {
                return Optional.of(new EvaluationResult.Infeasible(
                        route.vehicleId(),
                        Violation.INCOMPATIBLE_VEHICLE,
                        Optional.of(existingVisitNode(problem, requestId)),
                        Optional.empty()));
            }
        }
        return Optional.empty();
    }

    private static NodeId existingVisitNode(Problem problem, RequestId requestId) {
        Request request = problem.request(requestId);
        return switch (request.pattern()) {
            case DELIVERY_ONLY -> request.delivery().orElseThrow().nodeId();
            case PICKUP_ONLY -> request.pickup().orElseThrow().nodeId();
            case PICKUP_DELIVERY -> request.delivery().orElseThrow().nodeId();
        };
    }
}
