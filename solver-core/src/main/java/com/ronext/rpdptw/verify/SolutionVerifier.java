package com.ronext.rpdptw.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.NodeRef;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.verify.VerifyViolation.Kind;

/**
 * 결과 저장 직전 1회, 해 전체를 캐시 없이 재검증한다 (Domain §10.2, stage-05 §3).
 */
public final class SolutionVerifier {

    private SolutionVerifier() {}

    /**
     * 결과 저장 직전 1회, 해 전체(경로 + bank)를 캐시 없이 재검증한다 (Domain §10.2).
     * Problem·이동표·profile은 읽기만 한다. routes의 List&lt;NodeId&gt;는 방문 순서 그대로다.
     * profile은 탐색에 넘긴 것과 같은 인스턴스여야 한다 (Domain §8.4 MUST — 호출자 책임).
     * 탐색 예산은 인자에 없다 — 재검증은 예산과 무관하게 해 전체를 본다 (§2.5.1).
     */
    public static VerificationResult verify(Problem problem,
                                            Profile profile,
                                            Map<VehicleId, List<NodeId>> routes,
                                            Set<RequestId> bank,
                                            Evaluation reportedEvaluation,
                                            long[] reportedScore) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(routes, "routes");
        Objects.requireNonNull(bank, "bank");
        Objects.requireNonNull(reportedEvaluation, "reportedEvaluation");
        Objects.requireNonNull(reportedScore, "reportedScore");

        List<VehicleId> vehicleOrder = routes.keySet().stream()
                .sorted(Comparator.comparing(VehicleId::value))
                .toList();
        List<RequestId> bankOrder = bank.stream()
                .sorted(Comparator.comparing(RequestId::value))
                .toList();

        List<VerifyViolation> violations = new ArrayList<>();
        Map<RequestId, List<Appearance>> appearances = new LinkedHashMap<>();
        checkReferences(problem, routes, bankOrder, vehicleOrder, appearances, violations);
        Set<RequestId> owned = checkPairs(problem, appearances, violations);
        checkExclusivity(problem, bank, owned, violations);
        if (!violations.isEmpty()) {
            return new VerificationResult.Fail(violations);   // 절차 4 — 구조가 깨진 경로의 물리 재계산은 무의미
        }

        Map<VehicleId, RouteFacts> factsByVehicle = new LinkedHashMap<>();
        for (VehicleId vehicleId : vehicleOrder) {
            RouteReplay.Outcome outcome = RouteReplay.replay(problem, vehicleId, routes.get(vehicleId));
            if (outcome instanceof RouteReplay.Outcome.Violated violated) {
                violations.add(violated.violation());         // 나머지 경로도 계속 본다 (E7)
                continue;
            }
            RouteFacts facts = ((RouteReplay.Outcome.Ok) outcome).facts();
            checkWindowContainment(problem, vehicleId, facts, violations);
            factsByVehicle.put(vehicleId, facts);
        }
        for (VehicleId vehicleId : vehicleOrder) {
            checkCompatibility(problem, vehicleId, routes.get(vehicleId), violations);
        }
        for (VehicleId vehicleId : vehicleOrder) {
            RouteFacts facts = factsByVehicle.get(vehicleId);
            if (facts == null) {
                continue;
            }
            for (HardConstraint constraint : profile.hardConstraints()) {
                if (!constraint.satisfied(problem, facts)) {
                    violations.add(new VerifyViolation(
                            Kind.PROFILE_HARD, Optional.of(vehicleId), Optional.empty(), Optional.empty(),
                            constraint.id()));
                }
            }
        }
        if (!violations.isEmpty()) {
            // 절차 7-a — 위반 해의 부분 facts로 하는 점수 대조는 가짜 SCORE_MISMATCH만 만든다.
            return new VerificationResult.Fail(violations);
        }

        Evaluation recomputed = Evaluation.aggregate(factsByVehicle.values(), bank.size());
        if (!recomputed.equals(reportedEvaluation)) {
            violations.add(mismatch("expected=" + reportedEvaluation + ", recomputed=" + recomputed));
        }
        long[] recomputedScore = profile.score(problem, recomputed, factsByVehicle.values());
        if (!Arrays.equals(recomputedScore, reportedScore)) {
            violations.add(mismatch("expected=" + Arrays.toString(reportedScore)
                    + ", recomputed=" + Arrays.toString(recomputedScore)));
        }
        if (!violations.isEmpty()) {
            return new VerificationResult.Fail(violations);
        }
        return new VerificationResult.Pass(recomputed, recomputedScore, factsByVehicle, Set.copyOf(bank));
    }

    /** 절차 1 — 차량·방문 노드·bank ID의 참조 정합. */
    private static void checkReferences(
            Problem problem,
            Map<VehicleId, List<NodeId>> routes,
            List<RequestId> bankOrder,
            List<VehicleId> vehicleOrder,
            Map<RequestId, List<Appearance>> appearances,
            List<VerifyViolation> violations) {
        Set<NodeId> seenNodes = new HashSet<>();
        for (VehicleId vehicleId : vehicleOrder) {
            try {
                problem.vehicle(vehicleId);
            } catch (IllegalArgumentException ex) {
                violations.add(new VerifyViolation(
                        Kind.UNKNOWN_VEHICLE, Optional.of(vehicleId), Optional.empty(), Optional.empty(),
                        "vehicle not in problem"));
                continue;
            }
            List<NodeId> visits = routes.get(vehicleId);
            if (visits.isEmpty()) {
                violations.add(new VerifyViolation(
                        Kind.EMPTY_ROUTE, Optional.of(vehicleId), Optional.empty(), Optional.empty(),
                        "route has no visit"));
                continue;
            }
            for (int index = 0; index < visits.size(); index++) {
                NodeId nodeId = visits.get(index);
                Optional<NodeRef> ref = problem.nodeRef(nodeId);
                if (ref.isEmpty()) {
                    violations.add(new VerifyViolation(
                            Kind.UNKNOWN_NODE, Optional.of(vehicleId), Optional.empty(), Optional.of(nodeId),
                            "node not in problem"));
                    continue;
                }
                if (!seenNodes.add(nodeId)) {
                    violations.add(new VerifyViolation(
                            Kind.DUPLICATE_NODE,
                            Optional.of(vehicleId),
                            Optional.of(ref.get().requestId()),
                            Optional.of(nodeId),
                            "node appears more than once in the solution"));
                }
                appearances.computeIfAbsent(ref.get().requestId(), id -> new ArrayList<>())
                        .add(new Appearance(ref.get(), vehicleId, index));
            }
        }
        for (RequestId requestId : bankOrder) {
            try {
                problem.request(requestId);
            } catch (IllegalArgumentException ex) {
                violations.add(new VerifyViolation(
                        Kind.UNKNOWN_REQUEST, Optional.empty(), Optional.of(requestId), Optional.empty(),
                        "banked request not in problem"));
            }
        }
    }

    /** 절차 2 — pair 규칙 (Domain §1.4). 반환값은 경로가 소유한 RequestId 집합. */
    private static Set<RequestId> checkPairs(
            Problem problem,
            Map<RequestId, List<Appearance>> appearances,
            List<VerifyViolation> violations) {
        Set<RequestId> owned = new LinkedHashSet<>();
        for (Map.Entry<RequestId, List<Appearance>> entry : appearances.entrySet()) {
            RequestId requestId = entry.getKey();
            List<Appearance> visits = entry.getValue();
            owned.add(requestId);
            if (problem.request(requestId).pattern() != ServicePattern.PICKUP_DELIVERY) {
                continue;
            }
            Appearance pickup = null;
            Appearance delivery = null;
            for (Appearance visit : visits) {
                if (visit.ref().pickup()) {
                    pickup = visit;
                } else {
                    delivery = visit;
                }
            }
            if (pickup == null || delivery == null) {
                violations.add(pairViolation(Kind.PAIR_INCOMPLETE, requestId, "only one side is assigned"));
            } else if (!pickup.vehicleId().equals(delivery.vehicleId())) {
                violations.add(pairViolation(Kind.PAIR_SPLIT, requestId,
                        "pickup=" + pickup.vehicleId().value() + ", delivery=" + delivery.vehicleId().value()));
            } else if (pickup.index() > delivery.index()) {
                violations.add(pairViolation(Kind.PICKUP_AFTER_DELIVERY, requestId,
                        "pickupIndex=" + pickup.index() + ", deliveryIndex=" + delivery.index()));
            }
        }
        return owned;
    }

    /** 절차 3 — 배정 XOR (Domain §6.3). */
    private static void checkExclusivity(
            Problem problem, Set<RequestId> bank, Set<RequestId> owned, List<VerifyViolation> violations) {
        for (Request request : problem.requests()) {
            boolean assigned = owned.contains(request.id());
            boolean banked = bank.contains(request.id());
            if (assigned && banked) {
                violations.add(pairViolation(Kind.ASSIGNED_AND_BANKED, request.id(), "in a route and in bank"));
            } else if (!assigned && !banked) {
                violations.add(pairViolation(Kind.NOT_ASSIGNED_NOT_BANKED, request.id(), "in no route and not in bank"));
            }
        }
    }

    /**
     * 절차 5-a — 재계산이 끝난 사실을 한 번 더 훑어 배치 자체를 확인한다 (Domain §10.2).
     * 모든 이동·서비스가 한 근무창 안, 있는 차고 출·도착 순간이 그 차고 창 안.
     */
    private static void checkWindowContainment(
            Problem problem, VehicleId vehicleId, RouteFacts facts, List<VerifyViolation> violations) {
        Vehicle vehicle = problem.vehicle(vehicleId);
        List<TimeWindow> work = vehicle.workWindows();
        List<VisitFacts> visits = facts.visits();

        if (facts.departureSec().isPresent()) {
            checkArc(vehicleId, facts.departureSec().get(), visits.get(0).arrivalSec(), work, violations,
                    "startDepot leg");
        }
        for (int index = 1; index < visits.size(); index++) {
            checkArc(vehicleId, visits.get(index - 1).departureSec(), visits.get(index).arrivalSec(), work,
                    violations, "leg into " + visits.get(index).nodeId().value());
        }
        if (facts.endDepotArrivalSec().isPresent()) {
            checkArc(vehicleId, visits.get(visits.size() - 1).departureSec(), facts.endDepotArrivalSec().get(),
                    work, violations, "endDepot leg");
        }
        for (VisitFacts visit : visits) {
            if (!containedInOne(visit.serviceStartSec(), visit.serviceEndSec(), work)) {
                violations.add(new VerifyViolation(
                        Kind.WORK_WINDOW, Optional.of(vehicleId), Optional.of(visit.requestId()),
                        Optional.of(visit.nodeId()),
                        "service [" + visit.serviceStartSec() + ", " + visit.serviceEndSec()
                                + "] not inside one work window"));
            }
        }
        if (vehicle.startDepot().isPresent() && facts.departureSec().isPresent()
                && !containedInOne(facts.departureSec().get(), facts.departureSec().get(),
                        problem.depotAt(vehicle.startDepot().get()).windows())) {
            violations.add(new VerifyViolation(
                    Kind.DEPOT_WINDOW, Optional.of(vehicleId), Optional.empty(), Optional.empty(),
                    "startDepot departure=" + facts.departureSec().get() + " outside depot windows"));
        }
        if (vehicle.endDepot().isPresent() && facts.endDepotArrivalSec().isPresent()
                && !containedInOne(facts.endDepotArrivalSec().get(), facts.endDepotArrivalSec().get(),
                        problem.depotAt(vehicle.endDepot().get()).windows())) {
            violations.add(new VerifyViolation(
                    Kind.DEPOT_WINDOW, Optional.of(vehicleId), Optional.empty(), Optional.empty(),
                    "endDepot arrival=" + facts.endDepotArrivalSec().get() + " outside depot windows"));
        }
    }

    private static void checkArc(
            VehicleId vehicleId, long fromSec, long toSec, List<TimeWindow> work,
            List<VerifyViolation> violations, String what) {
        if (!containedInOne(fromSec, toSec, work)) {
            violations.add(new VerifyViolation(
                    Kind.WORK_WINDOW, Optional.of(vehicleId), Optional.empty(), Optional.empty(),
                    what + " [" + fromSec + ", " + toSec + "] not inside one work window"));
        }
    }

    private static boolean containedInOne(long fromSec, long toSec, List<TimeWindow> windows) {
        for (TimeWindow window : windows) {
            if (window.openSec() <= fromSec && toSec <= window.closeSec()) {
                return true;
            }
        }
        return false;
    }

    /** 절차 6 — 호환성 (Domain §3.4 동결 사실). at은 그 request의 있는 방문 NodeId. */
    private static void checkCompatibility(
            Problem problem, VehicleId vehicleId, List<NodeId> visits, List<VerifyViolation> violations) {
        Set<RequestId> seen = new LinkedHashSet<>();
        for (NodeId nodeId : visits) {
            RequestId requestId = problem.nodeRef(nodeId).orElseThrow().requestId();
            if (!seen.add(requestId)) {
                continue;
            }
            if (!problem.compatibleVehicles(requestId).contains(vehicleId)) {
                violations.add(new VerifyViolation(
                        Kind.INCOMPATIBLE_VEHICLE,
                        Optional.of(vehicleId),
                        Optional.of(requestId),
                        Optional.of(existingVisitNode(problem, requestId)),
                        "vehicle not in compatibleVehicles"));
            }
        }
    }

    private static NodeId existingVisitNode(Problem problem, RequestId requestId) {
        Request request = problem.request(requestId);
        return switch (request.pattern()) {
            case DELIVERY_ONLY -> request.delivery().orElseThrow().nodeId();
            case PICKUP_ONLY -> request.pickup().orElseThrow().nodeId();
            case PICKUP_DELIVERY -> request.delivery().orElseThrow().nodeId();
        };
    }

    private static VerifyViolation pairViolation(Kind kind, RequestId requestId, String detail) {
        return new VerifyViolation(kind, Optional.empty(), Optional.of(requestId), Optional.empty(), detail);
    }

    private static VerifyViolation mismatch(String detail) {
        return new VerifyViolation(
                Kind.SCORE_MISMATCH, Optional.empty(), Optional.empty(), Optional.empty(), detail);
    }

    private record Appearance(NodeRef ref, VehicleId vehicleId, int index) {}
}
