package com.ronext.rpdptw.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class GreatCircle {

    /** haversine 반지름. Stage 2 Q2 잠정 확정값 (Domain §4). */
    private static final int EARTH_RADIUS_M = 6_371_000;

    private GreatCircle() {}

    /** haversine, 지구 반지름 6_371_000 m. meter 값을 HALF_UP으로 정수화 (Domain §4). */
    public static int distanceMeter(Location a, Location b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        double lat1 = Math.toRadians(a.latitude());
        double lat2 = Math.toRadians(b.latitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.longitude() - a.longitude());
        double hav = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double central = 2 * Math.atan2(Math.sqrt(hav), Math.sqrt(1 - hav));
        return BigDecimal.valueOf(EARTH_RADIUS_M * central).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
