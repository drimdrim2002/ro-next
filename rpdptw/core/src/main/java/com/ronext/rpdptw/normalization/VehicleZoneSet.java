package com.ronext.rpdptw.normalization;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public sealed interface VehicleZoneSet {
    record AllZones() implements VehicleZoneSet {}
    record Restricted(SortedSet<ZoneCode> zoneIds) implements VehicleZoneSet {
        public Restricted {
            Objects.requireNonNull(zoneIds, "zoneIds must not be null");
            zoneIds = Collections.unmodifiableSortedSet(new TreeSet<>(zoneIds));
        }
    }
}
