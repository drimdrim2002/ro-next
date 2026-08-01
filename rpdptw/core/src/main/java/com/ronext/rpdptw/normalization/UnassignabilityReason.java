package com.ronext.rpdptw.normalization;

public enum UnassignabilityReason {
    /** No vehicle satisfies size/capability/zone requirements. */
    NO_ELIGIBLE_VEHICLE,
    /**
     * Pickup and delivery visit zones form a union no vehicle can cover
     * (Option A: explicit zone-union unassignability).
     */
    PICKUP_DELIVERY_ZONE_UNION_UNCOVERED
}
