package com.ronext.rpdptw.eval;

import java.util.Collection;

public record Evaluation(
        int unassignedCount,
        int usedVehicleCount,
        long totalDistanceMeter,
        long totalRouteOperationalTimeSec) {

    /**
     * 중립 집계: usedVehicleCount = routes.size() (Route는 비어 있지 않음),
     * 합산은 Math.addExact. 해석(순위)은 profile.score의 일.
     */
    public static Evaluation aggregate(Collection<RouteFacts> routes, int unassignedCount) {
        int used = routes.size();
        long distance = 0L;
        long operational = 0L;
        for (RouteFacts route : routes) {
            distance = Math.addExact(distance, route.driveDistMeter());
            operational = Math.addExact(operational, route.routeOperationalTimeSec());
        }
        return new Evaluation(unassignedCount, used, distance, operational);
    }
}
