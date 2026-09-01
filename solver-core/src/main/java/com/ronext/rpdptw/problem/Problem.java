package com.ronext.rpdptw.problem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.Compatibility;
import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Plan;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.TravelMatrix;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;

public final class Problem {

    private static final int FALLBACK_SPEED_KM_H = 45;

    private final String planId;
    private final Optional<String> customerId;
    private final TimeBase timeBase;
    private final long planEndSec;
    private final DeliveryPolicy deliveryPolicy;
    private final List<Depot> depots;
    private final List<Request> requests;
    private final List<Vehicle> vehicles;
    private final Map<LocationId, Location> locations;
    private final TravelMatrix travel;
    private final Map<RequestId, Request> requestsById;
    private final Map<VehicleId, Vehicle> vehiclesById;
    private final Map<LocationId, Depot> depotsByLocation;
    private final Map<VehicleId, Integer> resolvedSpeedKmH;
    private final Map<RequestId, Set<VehicleId>> compatibleVehicles;
    private final Map<NodeId, NodeRef> nodeRefs;

    private Problem(
            String planId,
            Optional<String> customerId,
            TimeBase timeBase,
            long planEndSec,
            DeliveryPolicy deliveryPolicy,
            List<Depot> depots,
            List<Request> requests,
            List<Vehicle> vehicles,
            Map<LocationId, Location> locations,
            TravelMatrix travel,
            Map<RequestId, Request> requestsById,
            Map<VehicleId, Vehicle> vehiclesById,
            Map<LocationId, Depot> depotsByLocation,
            Map<VehicleId, Integer> resolvedSpeedKmH,
            Map<RequestId, Set<VehicleId>> compatibleVehicles,
            Map<NodeId, NodeRef> nodeRefs) {
        this.planId = planId;
        this.customerId = customerId;
        this.timeBase = timeBase;
        this.planEndSec = planEndSec;
        this.deliveryPolicy = deliveryPolicy;
        this.depots = depots;
        this.requests = requests;
        this.vehicles = vehicles;
        this.locations = locations;
        this.travel = travel;
        this.requestsById = requestsById;
        this.vehiclesById = vehiclesById;
        this.depotsByLocation = depotsByLocation;
        this.resolvedSpeedKmH = resolvedSpeedKmH;
        this.compatibleVehicles = compatibleVehicles;
        this.nodeRefs = nodeRefs;
    }

    /** 검증 + 이동표 준비 + 호환성 사전 계산 + 동결. 실패 시 ProblemCreationException. */
    public static Problem freeze(Plan plan) {
        Objects.requireNonNull(plan, "plan");

        List<Depot> depots = List.copyOf(plan.depots());
        List<Request> requests = List.copyOf(plan.requests());
        List<Vehicle> vehicles = List.copyOf(plan.vehicles());
        Map<LocationId, Location> locations = copyLocations(plan.locations());

        Map<RequestId, Request> requestsById = new LinkedHashMap<>();
        for (Request request : requests) {
            if (requestsById.put(request.id(), request) != null) {
                throw new ProblemCreationException("duplicate RequestId: " + request.id());
            }
        }
        Map<VehicleId, Vehicle> vehiclesById = new LinkedHashMap<>();
        for (Vehicle vehicle : vehicles) {
            if (vehiclesById.put(vehicle.id(), vehicle) != null) {
                throw new ProblemCreationException("duplicate VehicleId: " + vehicle.id());
            }
        }
        Set<NodeId> nodeIds = new LinkedHashSet<>();
        for (Request request : requests) {
            request.pickup().ifPresent(side -> addNodeId(nodeIds, side));
            request.delivery().ifPresent(side -> addNodeId(nodeIds, side));
        }

        for (Request request : requests) {
            if (!pairMatches(request)) {
                throw new ProblemCreationException("pair mismatch: " + request.id() + " " + request.pattern());
            }
            request.pickup().ifPresent(side -> requireLocation(locations, side.locationId(), "pickup"));
            request.delivery().ifPresent(side -> requireLocation(locations, side.locationId(), "delivery"));
        }
        Map<LocationId, Depot> depotsByLocation = new LinkedHashMap<>();
        for (Depot depot : depots) {
            requireLocation(locations, depot.locationId(), "depot");
            // 덮어쓰기를 허용하면 depots()엔 둘 다 남고 depotAt()은 뒤엣것만 돌려줘
            // 앞 차고의 시간창이 조용히 사라진다 — 예외가 아니라 오답이 된다 (§6 E21).
            if (depotsByLocation.put(depot.locationId(), depot) != null) {
                throw new ProblemCreationException("duplicate depot LocationId: " + depot.locationId());
            }
        }
        Set<LocationId> depotIds = Set.copyOf(depotsByLocation.keySet());
        for (Vehicle vehicle : vehicles) {
            vehicle.startDepot().ifPresent(id -> requireDepot(depotIds, id, "startDepot"));
            vehicle.endDepot().ifPresent(id -> requireDepot(depotIds, id, "endDepot"));
        }

        Map<VehicleId, Integer> resolvedSpeedKmH = new LinkedHashMap<>();
        Set<Integer> speeds = new LinkedHashSet<>();
        for (Vehicle vehicle : vehicles) {
            int speed = resolveSpeed(vehicle, plan.deliveryPolicy());
            if (speed <= 0) {
                throw new ProblemCreationException("resolved speed <= 0: " + vehicle.id() + " = " + speed);
            }
            resolvedSpeedKmH.put(vehicle.id(), speed);
            speeds.add(speed);
        }

        TravelMatrix travel;
        try {
            travel = TravelMatrix.prepare(locations, plan.travelEntries(), speeds);
        } catch (IllegalArgumentException ex) {
            throw new ProblemCreationException(ex.getMessage(), ex);
        }

        Map<RequestId, Set<VehicleId>> compatibleVehicles = new LinkedHashMap<>();
        for (Request request : requests) {
            Set<VehicleId> compatible = new LinkedHashSet<>();
            for (Vehicle vehicle : vehicles) {
                if (Compatibility.compatible(vehicle, request)) {
                    compatible.add(vehicle.id());
                }
            }
            compatibleVehicles.put(request.id(), Collections.unmodifiableSet(compatible));
        }

        Map<NodeId, NodeRef> nodeRefs = new LinkedHashMap<>();
        for (Request request : requests) {
            request.pickup().ifPresent(side ->
                    nodeRefs.put(side.nodeId(), new NodeRef(request.id(), true, side)));
            request.delivery().ifPresent(side ->
                    nodeRefs.put(side.nodeId(), new NodeRef(request.id(), false, side)));
        }

        return new Problem(
                plan.planId(),
                plan.customerId(),
                plan.timeBase(),
                plan.planEndSec(),
                plan.deliveryPolicy(),
                depots,
                requests,
                vehicles,
                locations,
                travel,
                Collections.unmodifiableMap(requestsById),
                Collections.unmodifiableMap(vehiclesById),
                Collections.unmodifiableMap(depotsByLocation),
                Collections.unmodifiableMap(resolvedSpeedKmH),
                Collections.unmodifiableMap(compatibleVehicles),
                Collections.unmodifiableMap(nodeRefs));
    }

    public String planId() {
        return planId;
    }

    public Optional<String> customerId() {
        return customerId;
    }

    public TimeBase timeBase() {
        return timeBase;
    }

    public long planEndSec() {
        return planEndSec;
    }

    public DeliveryPolicy deliveryPolicy() {
        return deliveryPolicy;
    }

    public List<Depot> depots() {
        return depots;
    }

    public List<Request> requests() {
        return requests;
    }

    public List<Vehicle> vehicles() {
        return vehicles;
    }

    public Map<LocationId, Location> locations() {
        return locations;
    }

    public TravelMatrix travel() {
        return travel;
    }

    public Request request(RequestId id) {
        Request request = requestsById.get(id);
        if (request == null) {
            throw new IllegalArgumentException("unknown request: " + id);
        }
        return request;
    }

    public Vehicle vehicle(VehicleId id) {
        Vehicle vehicle = vehiclesById.get(id);
        if (vehicle == null) {
            throw new IllegalArgumentException("unknown vehicle: " + id);
        }
        return vehicle;
    }

    public Depot depotAt(LocationId id) {
        Depot depot = depotsByLocation.get(id);
        if (depot == null) {
            throw new IllegalArgumentException("unknown depot: " + id);
        }
        return depot;
    }

    public int resolvedSpeedKmH(VehicleId id) {
        Integer speed = resolvedSpeedKmH.get(id);
        if (speed == null) {
            throw new IllegalArgumentException("unknown vehicle: " + id);
        }
        return speed;
    }

    public Set<VehicleId> compatibleVehicles(RequestId id) {
        Set<VehicleId> ids = compatibleVehicles.get(id);
        if (ids == null) {
            throw new IllegalArgumentException("unknown request: " + id);
        }
        return ids;
    }

    public Optional<NodeRef> nodeRef(NodeId id) {
        return Optional.ofNullable(nodeRefs.get(id));
    }

    private static Map<LocationId, Location> copyLocations(Map<LocationId, Location> source) {
        Map<LocationId, Location> ordered = new LinkedHashMap<>();
        source.forEach((id, location) -> ordered.put(
                Objects.requireNonNull(id, "locations key"),
                Objects.requireNonNull(location, "locations value")));
        return Collections.unmodifiableMap(ordered);
    }

    private static void addNodeId(Set<NodeId> nodeIds, RequestSide side) {
        if (!nodeIds.add(side.nodeId())) {
            throw new ProblemCreationException("duplicate NodeId: " + side.nodeId());
        }
    }

    private static boolean pairMatches(Request request) {
        boolean pickup = request.pickup().isPresent();
        boolean delivery = request.delivery().isPresent();
        return switch (request.pattern()) {
            case DELIVERY_ONLY -> !pickup && delivery;
            case PICKUP_ONLY -> pickup && !delivery;
            case PICKUP_DELIVERY -> pickup && delivery;
        };
    }

    private static void requireLocation(Map<LocationId, Location> locations, LocationId id, String field) {
        if (!locations.containsKey(id)) {
            throw new ProblemCreationException(field + " location not in locations: " + id);
        }
    }

    private static void requireDepot(Set<LocationId> depotIds, LocationId id, String field) {
        if (!depotIds.contains(id)) {
            throw new ProblemCreationException(field + " not in depot set: " + id);
        }
    }

    /** vehicle.speedKmH ▷ deliveryPolicy.defaultSpeedKmH ▷ 45 */
    private static int resolveSpeed(Vehicle vehicle, DeliveryPolicy policy) {
        if (vehicle.speedKmH().isPresent()) {
            return vehicle.speedKmH().getAsInt();
        }
        if (policy.defaultSpeedKmH().isPresent()) {
            return policy.defaultSpeedKmH().getAsInt();
        }
        return FALLBACK_SPEED_KM_H;
    }
}
