package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.problem.NodeRef;
import com.ronext.rpdptw.problem.Problem;

public final class StructureCheck {

    private StructureCheck() {}

    /** pair·XOR·참조 규칙 전수 검사. 비어 있으면 구조 정상. Problem은 읽기만 한다. */
    public static List<StructureViolation> check(Problem problem, Solution solution) {
        List<StructureViolation> violations = new ArrayList<>();
        Set<VehicleId> seenVehicles = new HashSet<>();
        for (Route route : solution.routes()) {
            VehicleId vehicleId = route.vehicleId();
            try {
                problem.vehicle(vehicleId);
            } catch (IllegalArgumentException ex) {
                violations.add(violation(StructureViolation.Kind.UNKNOWN_VEHICLE, Optional.empty(), Optional.of(vehicleId)));
                continue;
            }
            if (!seenVehicles.add(vehicleId)) {
                violations.add(violation(
                        StructureViolation.Kind.DUPLICATE_VEHICLE_ROUTE, Optional.empty(), Optional.of(vehicleId)));
            }
        }

        Set<NodeId> seenNodes = new HashSet<>();
        Map<RequestId, List<Appeared>> appeared = new LinkedHashMap<>();
        for (Route route : solution.routes()) {
            for (int i = 0; i < route.visits().size(); i++) {
                NodeId nodeId = route.visits().get(i);
                Optional<NodeRef> ref = problem.nodeRef(nodeId);
                if (ref.isEmpty()) {
                    violations.add(violation(StructureViolation.Kind.UNKNOWN_NODE, Optional.empty(), Optional.empty()));
                    continue;
                }
                if (!seenNodes.add(nodeId)) {
                    violations.add(violation(
                            StructureViolation.Kind.DUPLICATE_NODE,
                            Optional.of(ref.get().requestId()),
                            Optional.empty()));
                }
                appeared.computeIfAbsent(ref.get().requestId(), id -> new ArrayList<>())
                        .add(new Appeared(ref.get(), route.vehicleId(), i));
            }
        }

        Set<RequestId> owned = new LinkedHashSet<>();
        for (Map.Entry<RequestId, List<Appeared>> entry : appeared.entrySet()) {
            RequestId requestId = entry.getKey();
            Request request;
            try {
                request = problem.request(requestId);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            List<Appeared> visits = entry.getValue();
            switch (request.pattern()) {
                case DELIVERY_ONLY, PICKUP_ONLY -> owned.add(requestId);
                case PICKUP_DELIVERY -> checkPickupDelivery(requestId, visits, owned, violations);
            }
        }

        Set<RequestId> knownRequests = new HashSet<>();
        for (Request request : problem.requests()) {
            knownRequests.add(request.id());
            boolean assigned = owned.contains(request.id());
            boolean banked = solution.bank().contains(request.id());
            if (assigned && banked) {
                violations.add(violation(
                        StructureViolation.Kind.ASSIGNED_AND_BANKED, Optional.of(request.id()), Optional.empty()));
            } else if (!assigned && !banked) {
                violations.add(violation(
                        StructureViolation.Kind.NOT_ASSIGNED_NOT_BANKED, Optional.of(request.id()), Optional.empty()));
            }
        }
        for (RequestId banked : solution.bank()) {
            if (!knownRequests.contains(banked)) {
                violations.add(violation(StructureViolation.Kind.UNKNOWN_REQUEST, Optional.of(banked), Optional.empty()));
            }
        }
        return List.copyOf(violations);
    }

    private static void checkPickupDelivery(
            RequestId requestId,
            List<Appeared> visits,
            Set<RequestId> owned,
            List<StructureViolation> violations) {
        Appeared pickup = null;
        Appeared delivery = null;
        for (Appeared visit : visits) {
            if (visit.ref.pickup()) {
                pickup = visit;
            } else {
                delivery = visit;
            }
        }
        if (pickup == null || delivery == null) {
            violations.add(violation(StructureViolation.Kind.PAIR_INCOMPLETE, Optional.of(requestId), Optional.empty()));
            if (pickup != null || delivery != null) {
                owned.add(requestId);
            }
            return;
        }
        if (!pickup.vehicleId.equals(delivery.vehicleId)) {
            violations.add(violation(StructureViolation.Kind.PAIR_SPLIT, Optional.of(requestId), Optional.empty()));
            owned.add(requestId);
            return;
        }
        if (pickup.index > delivery.index) {
            violations.add(
                    violation(StructureViolation.Kind.PICKUP_AFTER_DELIVERY, Optional.of(requestId), Optional.empty()));
        }
        owned.add(requestId);
    }

    private static StructureViolation violation(
            StructureViolation.Kind kind, Optional<RequestId> requestId, Optional<VehicleId> vehicleId) {
        return new StructureViolation(kind, requestId, vehicleId);
    }

    private record Appeared(NodeRef ref, VehicleId vehicleId, int index) {}
}
