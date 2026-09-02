package com.ronext.rpdptw.solve;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.Item;
import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Plan;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Trips;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;

/** 초기해 construction 테스트용 Problem 손조립 (Stage 1·2 경로). 이동표를 안 주면 GreatCircle이 채운다. */
final class ConstructionFixtures {

    static final LocationId DEPOT = new LocationId("D0");
    static final LocationId DEPOT2 = new LocationId("D1");
    static final TimeWindow WORK = new TimeWindow(28_800L, 64_800L);       // 08:00–18:00
    static final TimeWindow ALL_DAY = new TimeWindow(0L, 86_399L);

    private ConstructionFixtures() {}

    static Location at(LocationId id, double lat, double lon) {
        return new Location(id, lat, lon);
    }

    static Map<LocationId, Location> locations(Location... locations) {
        Map<LocationId, Location> map = new LinkedHashMap<>();
        for (Location location : locations) {
            map.put(location.id(), location);
        }
        return map;
    }

    static RequestSide side(NodeId nodeId, LocationId at, TimeWindow window, long serviceSec, Optional<String> zone) {
        return new RequestSide(nodeId, at, List.of(window), 0L, serviceSec, 86_400L * 3, zone);
    }

    static Request delivery(String id, LocationId at, long weight) {
        return delivery(id, at, weight, WORK, Optional.empty());
    }

    static Request delivery(String id, LocationId at, long weight, TimeWindow window, Optional<String> zone) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(side(NodeId.delivery(requestId), at, window, 300L, zone)),
                List.of(new Item("I-" + id, weight, weight, 1, 0L)),
                weight,
                weight,
                Optional.empty(),
                Set.of());
    }

    static Request pickup(String id, LocationId at, long weight) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.PICKUP_ONLY,
                Optional.of(side(NodeId.pickup(requestId), at, WORK, 300L, Optional.empty())),
                Optional.empty(),
                List.of(new Item("I-" + id, weight, weight, 1, 0L)),
                weight,
                weight,
                Optional.empty(),
                Set.of());
    }

    static Request pickupDelivery(String id, LocationId from, LocationId to, long weight) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.PICKUP_DELIVERY,
                Optional.of(side(NodeId.pickup(requestId), from, WORK, 300L, Optional.empty())),
                Optional.of(side(NodeId.delivery(requestId), to, WORK, 300L, Optional.empty())),
                List.of(new Item("I-" + id, weight, weight, 1, 0L)),
                weight,
                weight,
                Optional.empty(),
                Set.of());
    }

    static Request withFeatures(Request request, Set<String> features) {
        return new Request(
                request.id(), request.pattern(), request.pickup(), request.delivery(), request.items(),
                request.totalWeight(), request.totalVolume(), Optional.of(features), request.requiredCapabilities());
    }

    static Vehicle vehicle(String id, long maxWeight, Optional<LocationId> start, Optional<LocationId> end) {
        return vehicle(id, maxWeight, start, end, OptionalInt.empty(), Optional.empty());
    }

    static Vehicle vehicle(
            String id, long maxWeight, Optional<LocationId> start, Optional<LocationId> end,
            OptionalInt maxStop, Optional<String> feature) {
        return new Vehicle(
                new VehicleId(id),
                feature,
                maxWeight,
                maxWeight,
                List.of(WORK),
                OptionalInt.of(45),
                maxStop,
                OptionalLong.empty(),
                OptionalLong.empty(),
                Set.of(),
                Optional.empty(),
                start,
                end);
    }

    static Problem freeze(
            List<Depot> depots,
            List<Request> requests,
            List<Vehicle> vehicles,
            Map<LocationId, Location> locations,
            List<TravelEntry> travel) {
        return Problem.freeze(new Plan(
                "CONSTRUCT",
                Optional.empty(),
                new TimeBase(LocalDateTime.of(2023, 9, 13, 0, 0)),
                86_400L * 3,
                depots,
                requests,
                vehicles,
                locations,
                travel,
                new DeliveryPolicy(Trips.ONEWAY, false, OptionalInt.of(45))));
    }

    /** depot 둘레 반지름 0.03°의 고리 위에 DELIVERY_ONLY n건, depot 출발 차량 m대. 좌표 유래 이동표(대칭). */
    static Problem ring(int requestCount, int vehicleCount, long maxWeight, long weight) {
        List<Location> locations = new ArrayList<>();
        locations.add(at(DEPOT, 37.0, 127.0));
        List<Request> requests = new ArrayList<>();
        for (int i = 1; i <= requestCount; i++) {
            LocationId id = new LocationId("L" + i);
            double theta = 2 * Math.PI * (i - 1) / requestCount;
            locations.add(at(id, 37.0 + 0.03 * Math.sin(theta), 127.0 + 0.03 * Math.cos(theta)));
            requests.add(delivery("R" + i, id, weight));
        }
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= vehicleCount; i++) {
            vehicles.add(vehicle("V" + i, maxWeight, Optional.of(DEPOT), Optional.empty()));
        }
        return freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                requests,
                vehicles,
                locations(locations.toArray(Location[]::new)),
                List.of());
    }

    /** 전 패턴 혼합 + depot 2개 — H5·H7·H8·H19·H20이 기권하는 문제. */
    static Problem mixed() {
        LocationId a = new LocationId("A");
        LocationId b = new LocationId("B");
        LocationId c = new LocationId("C");
        LocationId d = new LocationId("D");
        return freeze(
                List.of(
                        new Depot(DEPOT, List.of(ALL_DAY), Optional.empty()),
                        new Depot(DEPOT2, List.of(ALL_DAY), Optional.empty())),
                List.of(
                        delivery("R1", a, 5_000L),
                        pickupDelivery("R2", b, c, 5_000L),
                        pickup("R3", d, 5_000L),
                        delivery("R4", c, 5_000L),
                        pickupDelivery("R5", a, d, 5_000L)),
                List.of(
                        vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.of(DEPOT2)),
                        vehicle("V2", 30_000L, Optional.of(DEPOT), Optional.of(DEPOT2))),
                locations(
                        at(DEPOT, 37.0, 127.0), at(DEPOT2, 37.05, 127.05),
                        at(a, 37.01, 127.0), at(b, 37.0, 127.02), at(c, 37.02, 127.02), at(d, 37.03, 127.0)),
                List.of());
    }

    /** 테스트 전용 hard 제약 — 경로당 방문 maxVisits 초과 금지. */
    static Profile maxVisitsProfile(int maxVisits) {
        return new Profile() {
            @Override
            public String id() {
                return "max-visits";
            }

            @Override
            public List<HardConstraint> hardConstraints() {
                return List.of(new HardConstraint() {
                    @Override
                    public String id() {
                        return "max-visits-" + maxVisits;
                    }

                    @Override
                    public boolean satisfied(Problem problem, RouteFacts route) {
                        return route.visits().size() <= maxVisits;
                    }
                });
            }

            @Override
            public long[] score(Problem problem, com.ronext.rpdptw.eval.Evaluation metrics, java.util.Collection<RouteFacts> routes) {
                return new long[] {
                    metrics.unassignedCount(), metrics.usedVehicleCount(),
                    metrics.totalDistanceMeter(), metrics.totalRouteOperationalTimeSec()
                };
            }
        };
    }

    static Set<RequestId> requestIds(Problem problem) {
        Set<RequestId> ids = new java.util.LinkedHashSet<>();
        for (Request request : problem.requests()) {
            ids.add(request.id());
        }
        return ids;
    }

    static ConstructionHeuristic fixed(String id, Solution solution) {
        return new ConstructionHeuristic() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public boolean abstains(Problem problem) {
                return false;
            }

            @Override
            public Solution construct(Problem problem, Profile profile) {
                return solution;
            }
        };
    }
}
