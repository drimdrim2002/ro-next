package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.TimeFixtures.allDay;
import static com.ronext.rpdptw.solve.TimeFixtures.sec;
import static com.ronext.rpdptw.solve.TimeFixtures.window;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;

class SolveFixtures {

    static final LocationId DEPOT = new LocationId("DEPOT");
    static final LocationId END_DEPOT = new LocationId("END_DEPOT");
    static final LocationId BUNDANG = new LocationId("BUNDANG");
    static final LocationId GANGNAM100 = new LocationId("GANGNAM100");
    static final LocationId GANGNAM200 = new LocationId("GANGNAM200");
    static final LocationId SAME = new LocationId("SAME");

    static final RequestId R1 = new RequestId("R1");
    static final RequestId R2 = new RequestId("R2");
    static final RequestId R3 = new RequestId("R3");

    static final VehicleId V1 = new VehicleId("V1");
    static final VehicleId V2 = new VehicleId("V2");

    static final TimeBase TIME_BASE = TimeFixtures.DEFAULT_TIME_BASE;
    static final TimeWindow ALL_DAY = allDay();
    static final TimeWindow WORK_08_18 = window("08:00", "18:00");

    static final List<TimeWindow> THREE_DAY_WORK = List.of(
            window(1, "08:00", "17:00"),
            window(2, "08:00", "17:00"),
            window(3, "08:00", "17:00"));

    SolveFixtures() {}

    static Problem section72Problem(boolean waitInDepot) {
        return section72Problem(waitInDepot, WORK_08_18, ALL_DAY, Optional.of(DEPOT), Optional.empty(), 30_000L);
    }

    static Problem section72Problem(
            boolean waitInDepot,
            TimeWindow work,
            TimeWindow depotWindow,
            Optional<LocationId> startDepot,
            Optional<LocationId> endDepot,
            long maxWeight) {
        Request r2 = pickupDelivery(
                "R2",
                BUNDANG,
                GANGNAM200,
                List.of(window("09:00", "12:00")),
                Duration.ofMinutes(5),
                LocalTime.of(10, 0),
                10_000L);
        Request r1 = deliveryOnly(
                "R1",
                GANGNAM100,
                List.of(window("13:00", "18:00")),
                Duration.ofMinutes(14),
                LocalTime.of(15, 0),
                10_000L);
        Vehicle v1 = vehicle(
                "V1",
                maxWeight,
                1_000_000L,
                List.of(work),
                startDepot,
                endDepot,
                OptionalInt.empty(),
                OptionalLong.empty(),
                OptionalLong.empty(),
                Optional.empty());
        List<TravelEntry> travel = new ArrayList<>();
        travel.add(new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400));
        travel.add(new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000));
        travel.add(new TravelEntry(BUNDANG, GANGNAM200, 5_000, 600));
        travel.add(new TravelEntry(GANGNAM100, GANGNAM200, 2_000, 600));
        travel.add(new TravelEntry(GANGNAM100, END_DEPOT, 8_000, 1_200));
        travel.add(new TravelEntry(BUNDANG, END_DEPOT, 8_000, 1_200));
        travel.add(new TravelEntry(DEPOT, GANGNAM100, 20_000, 4_000));
        travel.add(new TravelEntry(DEPOT, END_DEPOT, 1_000, 100));
        List<Depot> depots = new ArrayList<>();
        depots.add(new Depot(DEPOT, List.of(depotWindow), Optional.empty()));
        if (endDepot.isPresent() && endDepot.get().equals(END_DEPOT)) {
            depots.add(new Depot(END_DEPOT, List.of(ALL_DAY), Optional.empty()));
        }
        return freeze(waitInDepot, depots, List.of(r1, r2), List.of(v1), locations(DEPOT, END_DEPOT, BUNDANG, GANGNAM100, GANGNAM200), travel);
    }

    static Problem freeze(
            boolean waitInDepot,
            List<Depot> depots,
            List<Request> requests,
            List<Vehicle> vehicles,
            Map<LocationId, Location> locations,
            List<TravelEntry> travel) {
        return Problem.freeze(new Plan(
                "P1",
                Optional.of("cust"),
                TIME_BASE,
                345_600L,
                depots,
                requests,
                vehicles,
                locations,
                travel,
                new DeliveryPolicy(Trips.ONEWAY, waitInDepot, OptionalInt.of(45))));
    }

    // --- deliveryOnly ---

    static Request deliveryOnly(
            String id, LocationId locationId, List<TimeWindow> windows, long service, long reqDate, long weight) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                Optional.of(side(NodeId.delivery(requestId), locationId, windows, service, reqDate)),
                List.of(new Item("I-" + id, weight, 0L, 1, 0L)),
                weight,
                0L,
                Optional.empty(),
                Set.of());
    }

    static Request deliveryOnly(
            String id, LocationId locationId, List<TimeWindow> windows, Duration service, LocalTime reqDate, long weight) {
        return deliveryOnly(id, locationId, windows, sec(service), sec(reqDate), weight);
    }

    static Request deliveryOnly(
            String id, LocationId locationId, List<TimeWindow> windows, Duration service, LocalDateTime reqDate, long weight) {
        return deliveryOnly(id, locationId, windows, sec(service), sec(reqDate), weight);
    }

    static Request deliveryOnly(
            String id, LocationId locationId, List<TimeWindow> windows, Duration service, long reqDate, long weight) {
        return deliveryOnly(id, locationId, windows, sec(service), reqDate, weight);
    }

    static Request deliveryOnly(
            String id, LocationId locationId, TimeWindow window, Duration service, LocalTime reqDate, long weight) {
        return deliveryOnly(id, locationId, List.of(window), sec(service), sec(reqDate), weight);
    }

    static Request deliveryOnly(
            String id, LocationId locationId, TimeWindow window, Duration service, LocalDateTime reqDate, long weight) {
        return deliveryOnly(id, locationId, List.of(window), sec(service), sec(reqDate), weight);
    }

    static Request deliveryOnly(
            String id, LocationId locationId, TimeWindow window, Duration service, long reqDate, long weight) {
        return deliveryOnly(id, locationId, List.of(window), sec(service), reqDate, weight);
    }

    static Request deliveryOnly(
            String id, LocationId locationId, TimeWindow window, long service, long reqDate, long weight) {
        return deliveryOnly(id, locationId, List.of(window), service, reqDate, weight);
    }

    // --- pickupOnly ---

    static Request pickupOnly(
            String id, LocationId locationId, List<TimeWindow> windows, long service, long reqDate, long weight) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.PICKUP_ONLY,
                Optional.of(side(NodeId.pickup(requestId), locationId, windows, service, reqDate)),
                Optional.empty(),
                List.of(new Item("I-" + id, weight, 0L, 1, 0L)),
                weight,
                0L,
                Optional.empty(),
                Set.of());
    }

    static Request pickupOnly(
            String id, LocationId locationId, List<TimeWindow> windows, Duration service, LocalTime reqDate, long weight) {
        return pickupOnly(id, locationId, windows, sec(service), sec(reqDate), weight);
    }

    static Request pickupOnly(
            String id, LocationId locationId, List<TimeWindow> windows, Duration service, LocalDateTime reqDate, long weight) {
        return pickupOnly(id, locationId, windows, sec(service), sec(reqDate), weight);
    }

    static Request pickupOnly(
            String id, LocationId locationId, List<TimeWindow> windows, Duration service, long reqDate, long weight) {
        return pickupOnly(id, locationId, windows, sec(service), reqDate, weight);
    }

    static Request pickupOnly(
            String id, LocationId locationId, TimeWindow window, Duration service, LocalTime reqDate, long weight) {
        return pickupOnly(id, locationId, List.of(window), sec(service), sec(reqDate), weight);
    }

    static Request pickupOnly(
            String id, LocationId locationId, TimeWindow window, Duration service, LocalDateTime reqDate, long weight) {
        return pickupOnly(id, locationId, List.of(window), sec(service), sec(reqDate), weight);
    }

    static Request pickupOnly(
            String id, LocationId locationId, TimeWindow window, Duration service, long reqDate, long weight) {
        return pickupOnly(id, locationId, List.of(window), sec(service), reqDate, weight);
    }

    static Request pickupOnly(
            String id, LocationId locationId, TimeWindow window, long service, long reqDate, long weight) {
        return pickupOnly(id, locationId, List.of(window), service, reqDate, weight);
    }

    // --- pickupDelivery ---

    static Request pickupDelivery(
            String id,
            LocationId pickupLoc,
            LocationId deliveryLoc,
            List<TimeWindow> pickupWindows,
            long pickupService,
            long pickupReqDate,
            long weight) {
        return pickupDelivery(
                id,
                pickupLoc,
                deliveryLoc,
                pickupWindows,
                pickupService,
                pickupReqDate,
                List.of(ALL_DAY),
                0L,
                86_400L,
                weight);
    }

    static Request pickupDelivery(
            String id,
            LocationId pickupLoc,
            LocationId deliveryLoc,
            List<TimeWindow> pickupWindows,
            Duration pickupService,
            LocalTime pickupReqDate,
            long weight) {
        return pickupDelivery(
                id,
                pickupLoc,
                deliveryLoc,
                pickupWindows,
                sec(pickupService),
                sec(pickupReqDate),
                weight);
    }

    static Request pickupDelivery(
            String id,
            LocationId pickupLoc,
            LocationId deliveryLoc,
            List<TimeWindow> pickupWindows,
            Duration pickupService,
            LocalDateTime pickupReqDate,
            long weight) {
        return pickupDelivery(
                id,
                pickupLoc,
                deliveryLoc,
                pickupWindows,
                sec(pickupService),
                sec(pickupReqDate),
                weight);
    }

    static Request pickupDelivery(
            String id,
            LocationId pickupLoc,
            LocationId deliveryLoc,
            TimeWindow pickupWindow,
            Duration pickupService,
            LocalTime pickupReqDate,
            long weight) {
        return pickupDelivery(
                id,
                pickupLoc,
                deliveryLoc,
                List.of(pickupWindow),
                sec(pickupService),
                sec(pickupReqDate),
                weight);
    }

    static Request pickupDelivery(
            String id,
            LocationId pickupLoc,
            LocationId deliveryLoc,
            TimeWindow pickupWindow,
            Duration pickupService,
            LocalDateTime pickupReqDate,
            long weight) {
        return pickupDelivery(
                id,
                pickupLoc,
                deliveryLoc,
                List.of(pickupWindow),
                sec(pickupService),
                sec(pickupReqDate),
                weight);
    }

    static Request pickupDelivery(
            String id,
            LocationId pickupLoc,
            LocationId deliveryLoc,
            List<TimeWindow> pickupWindows,
            long pickupService,
            long pickupReqDate,
            List<TimeWindow> deliveryWindows,
            long deliveryService,
            long deliveryReqDate,
            long weight) {
        RequestId requestId = new RequestId(id);
        return new Request(
                requestId,
                ServicePattern.PICKUP_DELIVERY,
                Optional.of(side(NodeId.pickup(requestId), pickupLoc, pickupWindows, pickupService, pickupReqDate)),
                Optional.of(side(NodeId.delivery(requestId), deliveryLoc, deliveryWindows, deliveryService, deliveryReqDate)),
                List.of(new Item("I-" + id, weight, 0L, 1, 0L)),
                weight,
                0L,
                Optional.empty(),
                Set.of());
    }

    static RequestSide side(NodeId nodeId, LocationId locationId, List<TimeWindow> windows, long service, long reqDate) {
        return new RequestSide(nodeId, locationId, windows, 0L, service, reqDate, Optional.empty());
    }

    static Vehicle vehicle(
            String id,
            long maxWeight,
            long maxVolume,
            List<TimeWindow> workWindows,
            Optional<LocationId> startDepot,
            Optional<LocationId> endDepot,
            OptionalInt maxStop,
            OptionalLong maxDriveTime,
            OptionalLong maxDriveDist,
            Optional<String> feature) {
        return new Vehicle(
                new VehicleId(id),
                feature,
                maxWeight,
                maxVolume,
                workWindows,
                OptionalInt.of(45),
                maxStop,
                maxDriveTime,
                maxDriveDist,
                Set.of(),
                Optional.empty(),
                startDepot,
                endDepot);
    }

    static Map<LocationId, Location> locations(LocationId... ids) {
        Map<LocationId, Location> map = new LinkedHashMap<>();
        double lat = 37.0;
        for (LocationId id : ids) {
            map.put(id, new Location(id, lat, 127.0));
            lat += 0.1;
        }
        return map;
    }

    static List<NodeId> section72Visits() {
        return List.of(NodeId.pickup(R2), NodeId.delivery(R1));
    }

    static List<TravelEntry> section72Travel() {
        return List.of(
                new TravelEntry(DEPOT, BUNDANG, 10_000, 2_400),
                new TravelEntry(BUNDANG, GANGNAM100, 15_000, 3_000),
                new TravelEntry(BUNDANG, GANGNAM200, 5_000, 600),
                new TravelEntry(GANGNAM100, GANGNAM200, 2_000, 600),
                new TravelEntry(GANGNAM100, END_DEPOT, 8_000, 1_200),
                new TravelEntry(BUNDANG, END_DEPOT, 8_000, 1_200),
                new TravelEntry(DEPOT, GANGNAM100, 20_000, 4_000),
                new TravelEntry(DEPOT, END_DEPOT, 1_000, 100));
    }

    static RouteFacts feasible(PropagationResult result) {
        if (result instanceof PropagationResult.Feasible feasible) {
            return feasible.facts();
        }
        throw new AssertionError("expected feasible, got " + result);
    }

    static PropagationResult.Infeasible infeasible(PropagationResult result) {
        if (result instanceof PropagationResult.Infeasible infeasible) {
            return infeasible;
        }
        throw new AssertionError("expected infeasible, got " + result);
    }
}
