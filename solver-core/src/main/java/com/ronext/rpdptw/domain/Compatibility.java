package com.ronext.rpdptw.domain;

public final class Compatibility {

    private Compatibility() {}

    public static boolean size(Vehicle v, Request r) {
        if (v.vehicleFeature().isEmpty() || r.allowedVehicleFeatures().isEmpty()) {
            return true;
        }
        return r.allowedVehicleFeatures().orElseThrow().contains(v.vehicleFeature().orElseThrow());
    }

    public static boolean capability(Vehicle v, Request r) {
        return r.requiredCapabilities().isEmpty()
                || v.capabilities().containsAll(r.requiredCapabilities());
    }

    public static boolean zone(Vehicle v, RequestSide s) {
        if (v.zoneIds().isEmpty() || s.zoneId().isEmpty()) {
            return true;
        }
        return v.zoneIds().orElseThrow().contains(s.zoneId().orElseThrow());
    }

    public static boolean depotAnchors(Vehicle v, Request r) {
        return switch (r.pattern()) {
            case DELIVERY_ONLY -> v.startDepot().isPresent();
            case PICKUP_ONLY -> v.endDepot().isPresent();
            case PICKUP_DELIVERY -> true;
        };
    }

    public static boolean compatible(Vehicle v, Request r) {
        if (!size(v, r) || !capability(v, r) || !depotAnchors(v, r)) {
            return false;
        }
        if (r.pickup().isPresent() && !zone(v, r.pickup().orElseThrow())) {
            return false;
        }
        return r.delivery().isEmpty() || zone(v, r.delivery().orElseThrow());
    }
}
