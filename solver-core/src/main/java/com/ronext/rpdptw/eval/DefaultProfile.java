package com.ronext.rpdptw.eval;

import java.util.Collection;
import java.util.List;

import com.ronext.rpdptw.problem.Problem;

public class DefaultProfile implements Profile {

    public static final String ID = "default";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<HardConstraint> hardConstraints() {
        return List.of();
    }

    @Override
    public long[] score(Problem problem, Evaluation metrics, Collection<RouteFacts> routes) {
        return new long[] {
            metrics.unassignedCount(),
            metrics.usedVehicleCount(),
            metrics.totalDistanceMeter(),
            metrics.totalRouteOperationalTimeSec()
        };
    }
}
