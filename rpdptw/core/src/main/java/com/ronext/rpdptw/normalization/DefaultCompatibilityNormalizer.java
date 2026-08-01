package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.ServicePattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class DefaultCompatibilityNormalizer implements CompatibilityNormalizer {

    @Override
    public AllowedVehicleSizes normalizeAllowedVehicleSizes(List<String> rawCodes) {
        if (rawCodes == null || rawCodes.isEmpty()) {
            return new AllowedVehicleSizes.All();
        }

        if (rawCodes.contains("ALL")) {
            if (rawCodes.size() == 1) {
                return new AllowedVehicleSizes.All();
            } else {
                throw new CompatibilityReject(InputProblemCode.INVALID_FEATURE_LIST, "ALL keyword cannot be combined with specific size feature codes");
            }
        }

        List<SizeFeatureCode> codes = new ArrayList<>();
        for (String rawCode : rawCodes) {
            if (rawCode == null || rawCode.trim().isEmpty()) {
                throw new CompatibilityReject(InputProblemCode.INVALID_FEATURE_LIST, "Blank size feature code in feature list");
            }
            codes.add(new SizeFeatureCode(rawCode));
        }

        return new AllowedVehicleSizes.Concrete(codes);
    }

    @Override
    public SizeFeatureCode normalizeVehicleSizeCode(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new CompatibilityReject(InputProblemCode.INVALID_VEHICLE_FEATURE, "Vehicle size feature code must not be null or blank");
        }
        if ("ALL".equals(rawCode)) {
            throw new CompatibilityReject(InputProblemCode.INVALID_VEHICLE_FEATURE, "Vehicle size feature code cannot be ALL");
        }
        return new SizeFeatureCode(rawCode);
    }

    @Override
    public Set<CapabilityCode> normalizeCapabilities(Collection<String> rawCapabilities) {
        if (rawCapabilities == null || rawCapabilities.isEmpty()) {
            return Set.of();
        }

        Set<CapabilityCode> capabilities = new HashSet<>();
        for (String raw : rawCapabilities) {
            if (raw == null || raw.trim().isEmpty()) {
                throw new CompatibilityReject(InputProblemCode.INVALID_CAPABILITY, "Capability code must not be null or blank");
            }
            capabilities.add(new CapabilityCode(raw));
        }

        return Set.copyOf(capabilities);
    }

    @Override
    public VehicleZoneSet normalizeVehicleZones(Collection<String> rawZones) {
        if (rawZones == null || rawZones.isEmpty()) {
            return new VehicleZoneSet.AllZones();
        }

        SortedSet<ZoneCode> sortedZones = new TreeSet<>();
        for (String rawZone : rawZones) {
            if (rawZone == null || rawZone.trim().isEmpty()) {
                throw new CompatibilityReject(InputProblemCode.INVALID_ZONE, "Vehicle zone code must not be null or blank");
            }
            sortedZones.add(new ZoneCode(rawZone));
        }

        return new VehicleZoneSet.Restricted(sortedZones);
    }

    @Override
    public VehicleOwnership normalizeOwnership(Optional<String> rawOwnership) {
        if (rawOwnership == null || rawOwnership.isEmpty()) {
            return normalizeOwnership((String) null);
        }
        return normalizeOwnership(rawOwnership.get());
    }

    @Override
    public VehicleOwnership normalizeOwnership(String rawOwnership) {
        if (rawOwnership == null || rawOwnership.trim().isEmpty()) {
            return new VehicleOwnership.Absent();
        }

        String trimmed = rawOwnership.trim();
        if ("DIRECT".equals(trimmed)) {
            return new VehicleOwnership.Direct();
        } else if ("LEASE".equals(trimmed)) {
            return new VehicleOwnership.Lease();
        }

        throw new CompatibilityReject(InputProblemCode.INVALID_OWNERSHIP, "Invalid vehicle ownership: " + rawOwnership);
    }

    @Override
    public VehicleSpeedInput normalizeSpeed(Optional<String> rawSpeed) {
        if (rawSpeed == null || rawSpeed.isEmpty()) {
            return normalizeSpeed((String) null);
        }
        return normalizeSpeed(rawSpeed.get());
    }

    @Override
    public VehicleSpeedInput normalizeSpeed(String rawSpeed) {
        if (rawSpeed == null || rawSpeed.trim().isEmpty()) {
            return new VehicleSpeedInput.Absent();
        }

        double val;
        try {
            val = Double.parseDouble(rawSpeed.trim());
        } catch (NumberFormatException e) {
            throw new CompatibilityReject(InputProblemCode.INVALID_SPEED, "Invalid numeric format for speed: " + rawSpeed);
        }

        if (!(val > 0.0) || Double.isNaN(val) || Double.isInfinite(val)) {
            throw new CompatibilityReject(InputProblemCode.INVALID_SPEED, "Speed must be positive finite number: " + rawSpeed);
        }

        return new VehicleSpeedInput.PresentKmH(val);
    }

    @Override
    public ServicePattern normalizeServicePattern(String rawPattern) {
        if (rawPattern == null || rawPattern.trim().isEmpty()) {
            throw new CompatibilityReject(InputProblemCode.INVALID_SERVICE_PATTERN, "Service pattern must not be null or blank");
        }

        String trimmed = rawPattern.trim();
        if ("DELIVERY_ONLY".equals(trimmed)) {
            return ServicePattern.DELIVERY_ONLY;
        } else if ("PICKUP_DELIVERY".equals(trimmed)) {
            return ServicePattern.PICKUP_DELIVERY;
        }

        throw new CompatibilityReject(InputProblemCode.INVALID_SERVICE_PATTERN, "Invalid service pattern: " + rawPattern);
    }

    @Override
    public ServicePattern normalizeServicePattern(ServicePattern rawPattern) {
        if (rawPattern == ServicePattern.DELIVERY_ONLY || rawPattern == ServicePattern.PICKUP_DELIVERY) {
            return rawPattern;
        }
        throw new CompatibilityReject(InputProblemCode.INVALID_SERVICE_PATTERN, "Invalid service pattern: " + rawPattern);
    }

    @Override
    public boolean isVehicleCompatible(NormalizedVehicleSpec vehicle, NormalizedRequestSpec request) {
        if (vehicle == null || request == null) {
            return false;
        }

        if (request.allowedSizes() instanceof AllowedVehicleSizes.Concrete concrete) {
            if (!concrete.codes().contains(vehicle.sizeCode())) {
                return false;
            }
        }

        if (!vehicle.capabilities().containsAll(request.requiredCapabilities())) {
            return false;
        }

        if (vehicle.zoneSet() instanceof VehicleZoneSet.Restricted restricted) {
            if (!restricted.zoneIds().containsAll(request.requestZones())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Optional<StaticUnassignabilityFact> checkStaticUnassignability(NormalizedRequestSpec request, List<NormalizedVehicleSpec> vehicles) {
        if (request == null) {
            return Optional.empty();
        }

        if (vehicles != null) {
            for (NormalizedVehicleSpec vehicle : vehicles) {
                if (isVehicleCompatible(vehicle, request)) {
                    return Optional.empty();
                }
            }
        }

        UnassignabilityReason reason = request.requestZones().size() > 1
                ? UnassignabilityReason.PICKUP_DELIVERY_ZONE_UNION_UNCOVERED
                : UnassignabilityReason.NO_ELIGIBLE_VEHICLE;
        String detail = reason == UnassignabilityReason.PICKUP_DELIVERY_ZONE_UNION_UNCOVERED
                ? "No eligible vehicle covers pickup/delivery zone union"
                : "No eligible vehicle satisfies request requirements (size/capabilities/zones)";
        return Optional.of(new StaticUnassignabilityFact(request.requestId(), reason, detail));
    }

    @Override
    public List<StaticUnassignabilityFact> evaluateStaticUnassignability(List<NormalizedRequestSpec> requests, List<NormalizedVehicleSpec> vehicles) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<StaticUnassignabilityFact> facts = new ArrayList<>();
        for (NormalizedRequestSpec request : requests) {
            checkStaticUnassignability(request, vehicles).ifPresent(facts::add);
        }

        return List.copyOf(facts);
    }
}
