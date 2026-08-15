package com.ronext.rpdptw.domain.input;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.InputException;
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
import com.ronext.rpdptw.domain.Units;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;

public final class PlanNormalizer {

    private static final LocalTime DEFAULT_OPEN = LocalTime.of(0, 0, 0);
    private static final LocalTime DEFAULT_CLOSE = LocalTime.of(23, 59, 59);
    private static final String ALL = "ALL";

    public Plan normalize(PlanInput input) {
        Objects.requireNonNull(input, "input");
        if (input.planId() == null || input.planId().isBlank()) {
            throw invalid("planId", "blank");
        }
        if (input.planStart() == null || input.planEnd() == null || !input.planStart().isBefore(input.planEnd())) {
            throw invalid("dateRange", "planStart < planEnd required");
        }
        TimeBase timeBase = new TimeBase(input.planStart());
        long planEndSec = timeBase.toSeconds(input.planEnd());

        DeliveryPolicy deliveryPolicy = normalizePolicy(input.options());

        List<DepotInput> depotInputs = orEmpty(input.depots());
        if (depotInputs.isEmpty()) {
            throw invalid("depots", "at least one depot required");
        }
        Map<LocationId, Location> locations = new LinkedHashMap<>();
        Map<LocationId, Depot> depotsById = new LinkedHashMap<>();
        List<Depot> depots = new ArrayList<>();
        for (int i = 0; i < depotInputs.size(); i++) {
            String field = "depots[" + i + "]";
            DepotInput raw = requireElement(depotInputs.get(i), field);
            Location location = locationOf(raw.locId(), raw.latText(), raw.lonText(), field);
            if (depotsById.containsKey(location.id())) {
                throw invalid(field + ".locId", "duplicate LocationId");
            }
            putLocation(locations, location, field);
            List<TimeWindow> windows = timeBase.dailyWindows(
                    orDefault(raw.openTime(), DEFAULT_OPEN),
                    orDefault(raw.closeTime(), DEFAULT_CLOSE),
                    planEndSec,
                    field + ".windows");
            Depot depot = new Depot(location.id(), windows, foldZone(raw.zoneId()));
            depotsById.put(location.id(), depot);
            depots.add(depot);
        }

        List<Vehicle> vehicles = normalizeVehicles(
                orEmpty(input.vehicles()),
                input.options(),
                deliveryPolicy.trips(),
                depotsById,
                timeBase,
                planEndSec);

        List<Request> requests = normalizeRequests(
                orEmpty(input.requests()), locations, timeBase, planEndSec, input.planEnd());

        List<TravelEntry> travelEntries = normalizeTravel(orEmpty(input.travelEntries()));

        return new Plan(
                input.planId(),
                optionalText(input.customerId()),
                timeBase,
                planEndSec,
                depots,
                requests,
                vehicles,
                locations,
                travelEntries,
                deliveryPolicy);
    }

    private static DeliveryPolicy normalizePolicy(OptionsInput options) {
        OptionsInput raw = options == null ? new OptionsInput(null, null, null, null, null) : options;
        String tripsValue = raw.trips() == null ? "oneway" : raw.trips();
        Trips trips =
                switch (tripsValue) {
                    case "oneway" -> Trips.ONEWAY;
                    case "roundtrip" -> Trips.ROUNDTRIP;
                    default -> throw invalid("options.trips", "must be oneway or roundtrip");
                };
        int multiRotation = raw.multiRotation() == null ? 0 : raw.multiRotation();
        if (multiRotation != 0 && multiRotation != 1) {
            if (multiRotation == -1 || multiRotation >= 2) {
                throw unsupported("options.multiRotation", "supported values are {0, 1}");
            }
            throw invalid("options.multiRotation", "must be greater than -1");
        }
        String waitValue = raw.waitInDepot() == null ? "N" : raw.waitInDepot();
        boolean waitInDepot =
                switch (waitValue) {
                    case "Y" -> true;
                    case "N" -> false;
                    default -> throw invalid("options.waitInDepot", "must be Y or N");
                };
        OptionalInt defaultSpeed =
                raw.defaultSpeedKmH() == null ? OptionalInt.empty() : OptionalInt.of(raw.defaultSpeedKmH());
        return new DeliveryPolicy(trips, waitInDepot, defaultSpeed);
    }

    private static List<Vehicle> normalizeVehicles(
            List<VehicleInput> inputs,
            OptionsInput options,
            Trips trips,
            Map<LocationId, Depot> depotsById,
            TimeBase timeBase,
            long planEndSec) {
        Set<VehicleId> seen = new HashSet<>();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            String field = "vehicles[" + i + "]";
            VehicleInput raw = requireElement(inputs.get(i), field);
            if (raw.vehicleId() == null || raw.vehicleId().isBlank()) {
                throw invalid(field + ".vehicleId", "blank");
            }
            VehicleId id = new VehicleId(raw.vehicleId());
            if (!seen.add(id)) {
                throw invalid(field + ".vehicleId", "duplicate VehicleId");
            }
            if (raw.maxWeightKg() == null) {
                throw invalid(field + ".maxWeight", "missing");
            }
            if (raw.maxVolumeCbm() == null) {
                throw invalid(field + ".maxVolume", "missing");
            }
            long maxWeight = Units.toMilli(raw.maxWeightKg(), () -> field + ".maxWeight");
            long maxVolume = Units.toMilli(raw.maxVolumeCbm(), () -> field + ".maxVolume");
            List<TimeWindow> workWindows = timeBase.dailyWindows(
                    orDefault(raw.workStart(), DEFAULT_OPEN),
                    orDefault(raw.workEnd(), DEFAULT_CLOSE),
                    planEndSec,
                    field + ".workWindows");
            OptionalInt speed = raw.speedKmH() == null ? OptionalInt.empty() : OptionalInt.of(raw.speedKmH());
            OptionalInt effectiveMaxStop = foldMaxStop(raw.maxStopCnt(), options);
            OptionalLong maxDriveTime =
                    raw.maxDriveTimeSec() == null ? OptionalLong.empty() : OptionalLong.of(raw.maxDriveTimeSec());
            OptionalLong maxDriveDist =
                    raw.maxDriveDistMeter() == null ? OptionalLong.empty() : OptionalLong.of(raw.maxDriveDistMeter());
            Set<String> capabilities = copyStrings(raw.capabilities(), field + ".capabilities");
            Optional<Set<String>> zoneIds = foldZoneIds(raw.zoneIds(), field + ".zoneIds");
            Optional<LocationId> startDepot = resolveStartDepot(raw.startDepotLocId(), depotsById, field + ".startDepot");
            Optional<LocationId> endDepot =
                    resolveEndDepot(raw.endDepotLocId(), startDepot, trips, depotsById, field + ".endDepot");
            Optional<String> feature = foldVehicleFeature(raw.vehicleFeature());
            vehicles.add(new Vehicle(
                    id,
                    feature,
                    maxWeight,
                    maxVolume,
                    workWindows,
                    speed,
                    effectiveMaxStop,
                    maxDriveTime,
                    maxDriveDist,
                    capabilities,
                    zoneIds,
                    startDepot,
                    endDepot));
        }
        return vehicles;
    }

    private static OptionalInt foldMaxStop(Integer vehicleMax, OptionsInput options) {
        Integer global = options == null ? null : options.globalVehicleMaxStopCount();
        if (vehicleMax == null && global == null) {
            return OptionalInt.empty();
        }
        if (vehicleMax == null) {
            return OptionalInt.of(global);
        }
        if (global == null) {
            return OptionalInt.of(vehicleMax);
        }
        return OptionalInt.of(Math.min(vehicleMax, global));
    }

    private static Optional<LocationId> resolveStartDepot(
            String startDepotLocId, Map<LocationId, Depot> depotsById, String field) {
        if (startDepotLocId == null || startDepotLocId.isBlank()) {
            return Optional.empty();
        }
        LocationId id = new LocationId(startDepotLocId);
        if (!depotsById.containsKey(id)) {
            throw invalid(field, "unknown depot");
        }
        return Optional.of(id);
    }

    private static Optional<LocationId> resolveEndDepot(
            String endDepotLocId,
            Optional<LocationId> startDepot,
            Trips trips,
            Map<LocationId, Depot> depotsById,
            String field) {
        if (endDepotLocId != null && !endDepotLocId.isBlank()) {
            LocationId id = new LocationId(endDepotLocId);
            if (!depotsById.containsKey(id)) {
                throw invalid(field, "unknown depot");
            }
            return Optional.of(id);
        }
        if (trips == Trips.ROUNDTRIP) {
            if (startDepot.isEmpty()) {
                throw invalid(field, "roundtrip requires a depot to return to");
            }
            return startDepot;
        }
        return Optional.empty();
    }

    /** 부재·blank·"ALL"은 전부 전 차급 와일드카드다 (Domain §3.4). */
    private static Optional<String> foldVehicleFeature(String vehicleFeature) {
        return optionalText(vehicleFeature).filter(feature -> !ALL.equals(feature));
    }

    private static List<Request> normalizeRequests(
            List<RequestInput> inputs,
            Map<LocationId, Location> locations,
            TimeBase timeBase,
            long planEndSec,
            LocalDateTime planEnd) {
        Set<RequestId> seen = new HashSet<>();
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            String field = "orders[" + i + "]";
            RequestInput raw = requireElement(inputs.get(i), field);
            if (raw.orderId() == null || raw.orderId().isBlank()) {
                throw invalid(field + ".orderId", "blank");
            }
            RequestId id = new RequestId(raw.orderId());
            if (!seen.add(id)) {
                throw invalid(field + ".orderId", "duplicate RequestId");
            }
            if (raw.pickup() == null && raw.delivery() == null) {
                throw invalid(field, "pickup or delivery required");
            }
            if (raw.items() == null || raw.items().isEmpty()) {
                throw invalid(field + ".items", "required");
            }
            List<Item> items = new ArrayList<>();
            long totalWeight = 0L;
            long totalVolume = 0L;
            long itemTaskSum = 0L;
            for (int j = 0; j < raw.items().size(); j++) {
                String itemField = field + ".items[" + j + "]";
                ItemInput itemRaw = requireElement(raw.items().get(j), itemField);
                if (itemRaw.itemId() == null || itemRaw.itemId().isBlank()) {
                    throw invalid(itemField + ".itemId", "blank");
                }
                int qty = itemRaw.qty() == null ? 1 : itemRaw.qty();
                if (qty < 1) {
                    throw invalid(itemField + ".qty", "must be a positive integer");
                }
                if (itemRaw.weightKg() == null) {
                    throw invalid(itemField + ".weight", "missing");
                }
                if (itemRaw.volumeCbm() == null) {
                    throw invalid(itemField + ".volume", "missing");
                }
                long weight = Units.toMilli(itemRaw.weightKg(), () -> itemField + ".weight");
                long volume = Units.toMilli(itemRaw.volumeCbm(), () -> itemField + ".volume");
                long taskTime = itemRaw.taskTimeSec() == null ? 0L : itemRaw.taskTimeSec();
                if (taskTime < 0) {
                    throw invalid(itemField + ".taskTime", "negative");
                }
                try {
                    totalWeight = Math.addExact(totalWeight, Math.multiplyExact(weight, qty));
                    totalVolume = Math.addExact(totalVolume, Math.multiplyExact(volume, qty));
                    itemTaskSum = Math.addExact(itemTaskSum, Math.multiplyExact(taskTime, qty));
                } catch (ArithmeticException ex) {
                    throw invalid(itemField, "overflow");
                }
                items.add(new Item(itemRaw.itemId(), weight, volume, qty, taskTime));
            }
            Optional<RequestSide> pickup = raw.pickup() == null
                    ? Optional.empty()
                    : Optional.of(normalizeSide(
                            raw.pickup(),
                            NodeId.pickup(id),
                            locations,
                            timeBase,
                            planEndSec,
                            planEnd,
                            itemTaskSum,
                            field + ".pickup"));
            Optional<RequestSide> delivery = raw.delivery() == null
                    ? Optional.empty()
                    : Optional.of(normalizeSide(
                            raw.delivery(),
                            NodeId.delivery(id),
                            locations,
                            timeBase,
                            planEndSec,
                            planEnd,
                            itemTaskSum,
                            field + ".delivery"));
            ServicePattern pattern;
            if (pickup.isPresent() && delivery.isPresent()) {
                pattern = ServicePattern.PICKUP_DELIVERY;
            } else if (pickup.isPresent()) {
                pattern = ServicePattern.PICKUP_ONLY;
            } else {
                pattern = ServicePattern.DELIVERY_ONLY;
            }
            requests.add(new Request(
                    id,
                    pattern,
                    pickup,
                    delivery,
                    items,
                    totalWeight,
                    totalVolume,
                    foldAllowedFeatures(raw.vehicleFeatures(), field + ".vehicleFeature"),
                    copyStrings(raw.requiredCapabilities(), field + ".requiredCapabilities")));
        }
        return requests;
    }

    private static RequestSide normalizeSide(
            SideInput raw,
            NodeId nodeId,
            Map<LocationId, Location> locations,
            TimeBase timeBase,
            long planEndSec,
            LocalDateTime planEnd,
            long itemTaskSum,
            String field) {
        Location location = locationOf(raw.locId(), raw.latText(), raw.lonText(), field);
        putLocation(locations, location, field);
        List<TimeWindow> windows = timeBase.dailyWindows(
                orDefault(raw.openTime(), DEFAULT_OPEN),
                orDefault(raw.closeTime(), DEFAULT_CLOSE),
                planEndSec,
                field + ".windows");
        long duration = raw.durationSec() == null ? 0L : raw.durationSec();
        if (duration < 0) {
            throw invalid(field + ".duration", "negative");
        }
        long serviceTime;
        try {
            serviceTime = Math.addExact(duration, itemTaskSum);
        } catch (ArithmeticException ex) {
            throw invalid(field + ".serviceTime", "overflow");
        }
        long reqDateSec = timeBase.toSeconds(raw.reqDate() == null ? planEnd : raw.reqDate());
        return new RequestSide(
                nodeId, location.id(), windows, duration, serviceTime, reqDateSec, foldZone(raw.zoneId()));
    }

    private static Optional<Set<String>> foldAllowedFeatures(List<String> features, String field) {
        if (features == null) {
            return Optional.empty();
        }
        rejectNullElements(features, field);
        if (features.size() == 1 && ALL.equals(features.getFirst())) {
            return Optional.empty();
        }
        if (features.isEmpty() || features.contains(ALL)) {
            throw invalid(field, "ALL cannot be mixed or empty");
        }
        return Optional.of(Set.copyOf(features));
    }

    private static List<TravelEntry> normalizeTravel(List<TravelEntryInput> inputs) {
        List<TravelEntry> entries = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            // 오류 경로 문자열은 실패했을 때만 조립한다 — 이동표는 20만 줄이 넘는다 (§3 field Supplier 근거).
            final int index = i;
            TravelEntryInput raw = inputs.get(i);
            if (raw == null) {
                throw invalid(travelField(index), "null element");
            }
            if (raw.fromLocId() == null || raw.fromLocId().isBlank()) {
                throw invalid(travelField(index) + ".from", "blank");
            }
            if (raw.toLocId() == null || raw.toLocId().isBlank()) {
                throw invalid(travelField(index) + ".to", "blank");
            }
            if (raw.fromLocId().equals(raw.toLocId())) {
                continue;
            }
            if (raw.distance() == null) {
                throw invalid(travelField(index) + ".D", "missing");
            }
            if (raw.time() == null) {
                throw invalid(travelField(index) + ".U", "missing");
            }
            int distance = Units.toWholeMeters(raw.distance(), () -> travelField(index) + ".D");
            int time = Units.toWholeSeconds(raw.time(), () -> travelField(index) + ".U");
            entries.add(new TravelEntry(new LocationId(raw.fromLocId()), new LocationId(raw.toLocId()), distance, time));
        }
        return entries;
    }

    private static String travelField(int index) {
        return "distanceMatrix[" + index + "]";
    }

    private static Location locationOf(String locId, String latText, String lonText, String field) {
        double lat = parseCoordinate(latText, field + ".lat", -90.0, 90.0);
        double lon = parseCoordinate(lonText, field + ".lon", -180.0, 180.0);
        LocationId id = locId == null || locId.isBlank() ? LocationId.generated(latText, lonText) : new LocationId(locId);
        return new Location(id, lat, lon);
    }

    private static void putLocation(Map<LocationId, Location> locations, Location location, String field) {
        Location existing = locations.get(location.id());
        if (existing == null) {
            locations.put(location.id(), location);
            return;
        }
        if (Double.compare(existing.latitude(), location.latitude()) != 0
                || Double.compare(existing.longitude(), location.longitude()) != 0) {
            throw invalid(field, "same LocationId has different coordinates");
        }
    }

    private static double parseCoordinate(String text, String field, double min, double max) {
        if (text == null || text.isBlank()) {
            throw invalid(field, "missing");
        }
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw invalid(field, "not a number");
        }
        if (value < min || value > max) {
            throw invalid(field, "out of range");
        }
        return value;
    }

    private static Optional<String> optionalText(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    /** 부재·blank·"ALL"은 전부 구역 제약 없음이다 (Domain §3.4). */
    private static Optional<String> foldZone(String zoneId) {
        return optionalText(zoneId).filter(zone -> !ALL.equals(zone));
    }

    /**
     * 부재, 또는 정확히 ["ALL"]이면 전 구역이다 — "ALL"은 구역 이름이 될 수 없어 목록에
     * 단독으로만 온다. 다른 구역과 섞이거나 빈 집합이면 애매해서 오류다 (Domain §3.4).
     */
    private static Optional<Set<String>> foldZoneIds(Set<String> zoneIds, String field) {
        if (zoneIds == null) {
            return Optional.empty();
        }
        if (zoneIds.isEmpty()) {
            throw invalid(field, "empty set is ambiguous");
        }
        rejectNullElements(zoneIds, field);
        if (zoneIds.contains(ALL)) {
            if (zoneIds.size() > 1) {
                throw invalid(field, "ALL cannot be mixed");
            }
            return Optional.empty();
        }
        return Optional.of(Set.copyOf(zoneIds));
    }

    private static Set<String> copyStrings(Set<String> values, String field) {
        if (values == null) {
            return Set.of();
        }
        rejectNullElements(values, field);
        return Set.copyOf(values);
    }

    /** 관문에서 null 원소를 막는다 — 이후 코드는 null을 가정하지 않는다 (Domain §3 서두). */
    private static void rejectNullElements(Collection<?> values, String field) {
        for (Object value : values) {
            if (value == null) {
                throw invalid(field, "null element");
            }
        }
    }

    private static <T> T requireElement(T value, String field) {
        if (value == null) {
            throw invalid(field, "null element");
        }
        return value;
    }

    private static LocalTime orDefault(LocalTime value, LocalTime fallback) {
        return value == null ? fallback : value;
    }

    private static <T> List<T> orEmpty(List<T> value) {
        return value == null ? List.of() : value;
    }

    private static InputException invalid(String field, String message) {
        return new InputException(InputException.Kind.INVALID_INPUT, field, message);
    }

    private static InputException unsupported(String field, String message) {
        return new InputException(InputException.Kind.UNSUPPORTED_INPUT, field, message);
    }
}
