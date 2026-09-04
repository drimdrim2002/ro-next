package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.NodeRef;
import com.ronext.rpdptw.problem.Problem;

/**
 * 공통 삽입 후보 탐색·검증·비용 (heuristics 문서 §3.3·§4.1).
 * 검증 = 호환 필터 + RoutePropagator Feasible + profile hard 전부 satisfied — 이 한 곳뿐이다.
 */
public final class InsertionSearch {

    private InsertionSearch() {}

    /** Request 하나를 넣을 수 있는 모든 (경로, 위치) 후보를 비용 오름차순으로. 없으면 빈 목록. */
    public static List<Candidate> candidates(Problem problem, Profile profile, Solution current, RequestId requestId) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(current, "current");
        Set<VehicleId> compatible = problem.compatibleVehicles(requestId);
        List<Candidate> out = new ArrayList<>();
        Set<VehicleId> used = new HashSet<>();
        for (Route route : current.routes()) {
            used.add(route.vehicleId());
            if (compatible.contains(route.vehicleId())) {
                collect(problem, profile, requestId, route.vehicleId(), route.visits(), out);
            }
        }
        firstUnusedCompatible(problem, requestId, used)
                .ifPresent(vehicleId -> collect(problem, profile, requestId, vehicleId, List.of(), out));
        return sorted(out);
    }

    /**
     * 차량 하나만 대상으로 한 후보 (H3·H5·H6·H8·H11·H14·H20·H21 — "이 경로에 넣을 수 있는 것").
     * 그 차량이 비호환이면 빈 목록. 경로가 없으면 새 경로 후보다.
     */
    static List<Candidate> candidatesFor(
            Problem problem, Profile profile, Solution current, RequestId requestId, VehicleId vehicleId) {
        if (!problem.compatibleVehicles(requestId).contains(vehicleId)) {
            return List.of();
        }
        List<Candidate> out = new ArrayList<>();
        collect(problem, profile, requestId, vehicleId, visitsOf(current, vehicleId), out);
        return sorted(out);
    }

    /** 후보를 적용한 새 Solution (불변 — 원본은 그대로). 삽입된 Request는 bank에서 빠진다 (XOR). */
    public static Solution apply(Problem problem, Solution current, Candidate candidate) {
        List<Route> routes = new ArrayList<>(current.routes().size() + 1);
        boolean replaced = false;
        for (Route route : current.routes()) {
            if (route.vehicleId().equals(candidate.vehicleId())) {
                routes.add(new Route(candidate.vehicleId(), candidate.visits()));
                replaced = true;
            } else {
                routes.add(route);
            }
        }
        if (!replaced) {
            routes.add(new Route(candidate.vehicleId(), candidate.visits()));
        }
        Set<RequestId> bank = new LinkedHashSet<>(current.bank());
        for (NodeId nodeId : candidate.visits()) {
            bank.remove(requestOf(problem, nodeId));
        }
        return new Solution(routes, bank);
    }

    /**
     * 요청 하나를 차량 v의 단독 경로로 돌 때의 이동표 거리 합 —
     * start→(pickup→)delivery→end 중 그 요청·차량에 있는 구간만 더한다. 오라클이 아니라
     * 표 조회다 (feasibility를 보장하지 않는다). H11 절감·H14 regret 전용 (Solomon c2의 λ·d_0u 항).
     */
    public static long standaloneDistMeter(Problem problem, VehicleId vehicleId, RequestId requestId) {
        Vehicle vehicle = problem.vehicle(vehicleId);
        Request request = problem.request(requestId);
        List<LocationId> stops = new ArrayList<>(4);
        vehicle.startDepot().ifPresent(stops::add);
        request.pickup().ifPresent(side -> stops.add(side.locationId()));
        request.delivery().ifPresent(side -> stops.add(side.locationId()));
        vehicle.endDepot().ifPresent(stops::add);
        long total = 0L;
        for (int i = 0; i + 1 < stops.size(); i++) {
            total = Math.addExact(total, problem.travel().distanceMeter(stops.get(i), stops.get(i + 1)));
        }
        return total;
    }

    /**
     * Request 하나를 경로에서 빼 bank로 돌린 새 Solution (불변). 경로가 비면 그 Route는 사라진다 —
     * 빈 Route를 두지 않는다 (Stage 3 E30). H23 1-1 교환 전용 (heuristics 문서 §5 H23).
     */
    static Solution remove(Problem problem, Solution current, RequestId requestId) {
        Request request = problem.request(requestId);
        Set<NodeId> nodes = new HashSet<>();
        request.pickup().ifPresent(side -> nodes.add(side.nodeId()));
        request.delivery().ifPresent(side -> nodes.add(side.nodeId()));
        List<Route> routes = new ArrayList<>(current.routes().size());
        for (Route route : current.routes()) {
            List<NodeId> kept = new ArrayList<>(route.visits().size());
            for (NodeId nodeId : route.visits()) {
                if (!nodes.contains(nodeId)) {
                    kept.add(nodeId);
                }
            }
            if (!kept.isEmpty()) {
                routes.add(kept.size() == route.visits().size() ? route : new Route(route.vehicleId(), kept));
            }
        }
        Set<RequestId> bank = new LinkedHashSet<>(current.bank());
        bank.add(requestId);
        return new Solution(routes, bank);
    }

    /** §4.3 방어 카운터 — 바깥 루프가 기법별 상한을 넘으면 버그다. 조용히 자르지 않는다. */
    static void checkOuterLoop(String heuristicId, int iterations, int bound) {
        if (iterations > bound) {
            throw new IllegalStateException(heuristicId + ": outer loop " + iterations + " exceeded bound " + bound);
        }
    }

    static RequestId requestOf(Problem problem, NodeId nodeId) {
        return problem.nodeRef(nodeId)
                .map(NodeRef::requestId)
                .orElseThrow(() -> new IllegalArgumentException("unknown node: " + nodeId));
    }

    static List<NodeId> visitsOf(Solution current, VehicleId vehicleId) {
        for (Route route : current.routes()) {
            if (route.vehicleId().equals(vehicleId)) {
                return route.visits();
            }
        }
        return List.of();
    }

    static boolean isUsed(Solution current, VehicleId vehicleId) {
        for (Route route : current.routes()) {
            if (route.vehicleId().equals(vehicleId)) {
                return true;
            }
        }
        return false;
    }

    /** 미사용 호환 차량 중 VehicleId 문자열 순 첫 번째 (§4.1 결정성). */
    static Optional<VehicleId> firstUnusedCompatible(Problem problem, RequestId requestId, Set<VehicleId> used) {
        VehicleId first = null;
        for (VehicleId vehicleId : problem.compatibleVehicles(requestId)) {
            if (used.contains(vehicleId)) {
                continue;
            }
            if (first == null || vehicleId.value().compareTo(first.value()) < 0) {
                first = vehicleId;
            }
        }
        return Optional.ofNullable(first);
    }

    static Set<VehicleId> usedVehicles(Solution current) {
        Set<VehicleId> used = new HashSet<>();
        for (Route route : current.routes()) {
            used.add(route.vehicleId());
        }
        return used;
    }

    /** 호환 필터 + 전파 + profile hard — 통과하면 facts, 아니면 empty. 후보 검증의 단일 지점 (§4.1). */
    static Optional<RouteFacts> validate(Problem problem, Profile profile, VehicleId vehicleId, List<NodeId> visits) {
        for (NodeId nodeId : visits) {
            if (!problem.compatibleVehicles(requestOf(problem, nodeId)).contains(vehicleId)) {
                return Optional.empty();
            }
        }
        PropagationResult propagated = RoutePropagator.propagate(problem, vehicleId, visits);
        if (!(propagated instanceof PropagationResult.Feasible feasible)) {
            return Optional.empty();
        }
        RouteFacts facts = feasible.facts();
        for (HardConstraint constraint : profile.hardConstraints()) {
            if (!constraint.satisfied(problem, facts)) {
                return Optional.empty();
            }
        }
        return Optional.of(facts);
    }

    private static List<Candidate> sorted(List<Candidate> out) {
        // 생성 순서가 (VehicleId, 삽입 위치) 순이라 안정 정렬로 §4.1의 동률 규칙이 유지된다.
        out.sort(Candidate.byCost().thenComparing(candidate -> candidate.vehicleId().value()));
        return List.copyOf(out);
    }

    private static void collect(
            Problem problem,
            Profile profile,
            RequestId requestId,
            VehicleId vehicleId,
            List<NodeId> visits,
            List<Candidate> out) {
        boolean newRoute = visits.isEmpty();
        RouteFacts before = null;
        Map<NodeId, Long> slackBefore = Map.of();
        if (!newRoute) {
            // 이미 Infeasible인 기존 경로는 후보 0개로 뺀다 — destroy가 만들 수 있는 상태이지 버그가 아니다
            // (§4.3·N9: 이동표가 삼각부등식을 지키지 않으면 방문 하나를 빼는 것만으로 뒤 방문이 창을 넘긴다).
            Optional<RouteFacts> validated = validate(problem, profile, vehicleId, visits);
            if (validated.isEmpty()) {
                return;
            }
            before = validated.get();
            slackBefore = forwardSlackByNode(problem, before);
        }
        Request request = problem.request(requestId);
        int n = visits.size();
        switch (request.pattern()) {
            case DELIVERY_ONLY, PICKUP_ONLY -> {
                NodeId node = request.pattern() == ServicePattern.DELIVERY_ONLY
                        ? request.delivery().orElseThrow().nodeId()
                        : request.pickup().orElseThrow().nodeId();
                for (int i = 0; i <= n; i++) {
                    List<NodeId> inserted = new ArrayList<>(n + 1);
                    inserted.addAll(visits.subList(0, i));
                    inserted.add(node);
                    inserted.addAll(visits.subList(i, n));
                    evaluate(problem, profile, vehicleId, newRoute, before, slackBefore, inserted, out);
                }
            }
            case PICKUP_DELIVERY -> {
                NodeId pickup = request.pickup().orElseThrow().nodeId();
                NodeId delivery = request.delivery().orElseThrow().nodeId();
                for (int i = 0; i <= n; i++) {
                    for (int j = i; j <= n; j++) {
                        List<NodeId> inserted = new ArrayList<>(n + 2);
                        inserted.addAll(visits.subList(0, i));
                        inserted.add(pickup);
                        inserted.addAll(visits.subList(i, j));
                        inserted.add(delivery);
                        inserted.addAll(visits.subList(j, n));
                        evaluate(problem, profile, vehicleId, newRoute, before, slackBefore, inserted, out);
                    }
                }
            }
        }
    }

    private static void evaluate(
            Problem problem,
            Profile profile,
            VehicleId vehicleId,
            boolean newRoute,
            RouteFacts before,
            Map<NodeId, Long> slackBefore,
            List<NodeId> inserted,
            List<Candidate> out) {
        Optional<RouteFacts> after = validate(problem, profile, vehicleId, inserted);
        if (after.isEmpty()) {
            return;
        }
        RouteFacts facts = after.get();
        long distBefore = before == null ? 0L : before.driveDistMeter();
        long opBefore = before == null ? 0L : before.routeOperationalTimeSec();
        long slackDelta = 0L;
        if (before != null) {
            Map<NodeId, Long> slackAfter = forwardSlackByNode(problem, facts);
            long sumBefore = 0L;
            long sumAfter = 0L;
            for (Map.Entry<NodeId, Long> entry : slackBefore.entrySet()) {
                sumBefore = Math.addExact(sumBefore, entry.getValue());
                sumAfter = Math.addExact(sumAfter, slackAfter.get(entry.getKey()));
            }
            slackDelta = sumBefore - sumAfter;
        }
        out.add(new Candidate(
                vehicleId,
                newRoute,
                inserted,
                facts.driveDistMeter() - distBefore,
                facts.routeOperationalTimeSec() - opBefore,
                slackDelta));
    }


    // ---- 기법 공통 helper (§4.2 결정성 — 전부 문자열 순으로 끝맺는다) ----

    static final Comparator<RequestId> BY_REQUEST_ID = Comparator.comparing(RequestId::value);
    static final Comparator<VehicleId> BY_VEHICLE_ID = Comparator.comparing(VehicleId::value);

    /** 모든 RequestId를 문자열 순으로. */
    static List<RequestId> sortedRequestIds(Problem problem) {
        List<RequestId> ids = new ArrayList<>(problem.requests().size());
        for (Request request : problem.requests()) {
            ids.add(request.id());
        }
        ids.sort(BY_REQUEST_ID);
        return ids;
    }

    /** 전 Request가 bank인 빈 해. */
    static Solution emptySolution(Problem problem) {
        return new Solution(List.of(), new LinkedHashSet<>(sortedRequestIds(problem)));
    }

    /** anchor(r) = 첫 방문 side (픽업이 있으면 픽업, 없으면 delivery). */
    static RequestSide anchor(Request request) {
        return request.pickup().orElseGet(() -> request.delivery().orElseThrow());
    }

    /** 마지막 방문 side (delivery가 있으면 delivery, 없으면 pickup). */
    static RequestSide lastSide(Request request) {
        return request.delivery().orElseGet(() -> request.pickup().orElseThrow());
    }

    static long firstOpenSec(RequestSide side) {
        return side.windows().getFirst().openSec();
    }

    static long lastCloseSec(RequestSide side) {
        return side.windows().getLast().closeSec();
    }

    static long windowSpanSec(RequestSide side) {
        long span = 0L;
        for (TimeWindow window : side.windows()) {
            span = Math.addExact(span, window.closeSec() - window.openSec());
        }
        return span;
    }

    static long workSpanSec(Vehicle vehicle) {
        long span = 0L;
        for (TimeWindow window : vehicle.workWindows()) {
            span = Math.addExact(span, window.closeSec() - window.openSec());
        }
        return span;
    }

    /** 차량 순서 (maxWeight DESC, VehicleId ASC) — H6·H8·H17·H18·H19·H20·H21 공통. */
    static List<Vehicle> vehiclesByCapacityDesc(Problem problem) {
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.comparingLong(Vehicle::maxWeight).reversed()
                .thenComparing(Vehicle::id, BY_VEHICLE_ID));
        return vehicles;
    }

    /** 새 경로 후보를 적용한 해 — 그 차량으로 열 수 없으면 empty (§4.1 검증 그대로). */
    static Optional<Solution> openRoute(
            Problem problem, Profile profile, Solution current, RequestId requestId, VehicleId vehicleId) {
        List<Candidate> cands = candidatesFor(problem, profile, current, requestId, vehicleId);
        return cands.isEmpty() ? Optional.empty() : Optional.of(apply(problem, current, cands.getFirst()));
    }


    /**
     * 경로 버전 캐시 — 요청별·차량별로 "그 경로의 방문 목록 → 상위 3개 후보"를 기억하고, 경로가 바뀐
     * 차량만 다시 전파한다 (§4.1 2026-09-02 도입분). 상위 3개면 regret-2·regret-3·regret-m(경로별
     * 최선)·최선이 전부 candidates()의 전체 재계산과 같다 — T13b가 대조한다.
     * 사용처는 H1·H2·H9·H10·H12·H17뿐이다. 후보 1개의 검증은 여전히 경로 전체 재전파다.
     */
    static final class Cache {

        static final int TOP = 3;

        private final Problem problem;
        private final Profile profile;
        private final Map<RequestId, Map<VehicleId, Entry>> entries = new HashMap<>();

        Cache(Problem problem, Profile profile) {
            this.problem = problem;
            this.profile = profile;
        }

        /** candidates()와 같은 순서 — 차량별 상위 3개의 합집합을 (byCost, VehicleId, 위치) 순으로. */
        List<Candidate> candidates(Solution current, RequestId requestId) {
            Set<VehicleId> compatible = problem.compatibleVehicles(requestId);
            List<Candidate> out = new ArrayList<>();
            Set<VehicleId> used = new HashSet<>();
            for (Route route : current.routes()) {
                used.add(route.vehicleId());
                if (compatible.contains(route.vehicleId())) {
                    out.addAll(top(requestId, route.vehicleId(), route.visits()));
                }
            }
            firstUnusedCompatible(problem, requestId, used)
                    .ifPresent(vehicleId -> out.addAll(top(requestId, vehicleId, List.of())));
            return sorted(out);
        }

        /** 삽입된 요청의 항목을 버린다 — 다시 조회되지 않는다. */
        void forget(RequestId requestId) {
            entries.remove(requestId);
        }

        private List<Candidate> top(RequestId requestId, VehicleId vehicleId, List<NodeId> visits) {
            Map<VehicleId, Entry> byVehicle = entries.computeIfAbsent(requestId, id -> new HashMap<>());
            Entry entry = byVehicle.get(vehicleId);
            if (entry != null && (entry.visits() == visits || entry.visits().equals(visits))) {
                return entry.top();
            }
            List<Candidate> all = new ArrayList<>();
            collect(problem, profile, requestId, vehicleId, visits, all);
            List<Candidate> ranked = sorted(all);
            List<Candidate> top = ranked.subList(0, Math.min(TOP, ranked.size()));
            byVehicle.put(vehicleId, new Entry(visits, top));
            return top;
        }

        private record Entry(List<NodeId> visits, List<Candidate> top) {}
    }

    /** 방문 하나의 forward slack = 서비스가 시작된 시간창의 close − serviceStartSec (§4.1). */
    static Map<NodeId, Long> forwardSlackByNode(Problem problem, RouteFacts facts) {
        Map<NodeId, Long> slack = new TreeMap<>(Comparator.comparing(NodeId::value));
        for (VisitFacts visit : facts.visits()) {
            RequestSide side = problem.nodeRef(visit.nodeId()).orElseThrow().side();
            long start = visit.serviceStartSec();
            Long close = null;
            for (TimeWindow window : side.windows()) {
                if (window.openSec() <= start && start <= window.closeSec()) {
                    close = window.closeSec();
                    break;
                }
            }
            if (close == null) {
                throw new IllegalStateException("service start outside every window: " + visit.nodeId());
            }
            slack.put(visit.nodeId(), close - start);
        }
        return slack;
    }

    public record Candidate(
            VehicleId vehicleId,
            boolean newRoute,
            List<NodeId> visits,
            long deltaDriveDistMeter,
            long deltaRouteOperationalTimeSec,
            long deltaForwardSlackSec) {

        public Candidate {
            Objects.requireNonNull(vehicleId, "vehicleId");
            visits = List.copyOf(visits);
        }

        /**
         * 사전식 비용: (새 경로 여부, Δ거리, Δ운행시간). 후보 '고르기' 전용 (§4.2).
         * deltaForwardSlackSec은 여기 들지 않는다 — H13만 자기 비교자로 쓴다 (§5 H13).
         */
        public static Comparator<Candidate> byCost() {
            return Comparator.comparing(Candidate::newRoute)
                    .thenComparingLong(Candidate::deltaDriveDistMeter)
                    .thenComparingLong(Candidate::deltaRouteOperationalTimeSec);
        }
    }
}
