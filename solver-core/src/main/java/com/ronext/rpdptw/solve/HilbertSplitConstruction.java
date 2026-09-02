package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ronext.rpdptw.domain.Location;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** H18 — Hilbert 곡선(차수 16) 순열 + Split, Bartholdi & Platzman (heuristics 문서 §5 H18). 좌표는 순서에만. */
public final class HilbertSplitConstruction implements ConstructionHeuristic {

    static final String ID = "hilbert-split";
    static final int ORDER = 16;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        return GiantTourSplit.split(problem, profile, hilbertOrder(problem), InsertionSearch.vehiclesByCapacityDesc(problem));
    }

    /** anchor 좌표(PD는 픽업·배송의 중점)를 경계 상자로 정규화해 Hilbert 인덱스 ASC, 동률 RequestId. */
    static List<RequestId> hilbertOrder(Problem problem) {
        Map<RequestId, double[]> points = new LinkedHashMap<>();
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        for (Request request : problem.requests()) {
            Location a = problem.locations().get(InsertionSearch.anchor(request).locationId());
            Location b = problem.locations().get(InsertionSearch.lastSide(request).locationId());
            double[] point = {(a.latitude() + b.latitude()) / 2.0, (a.longitude() + b.longitude()) / 2.0};
            points.put(request.id(), point);
            minLat = Math.min(minLat, point[0]);
            maxLat = Math.max(maxLat, point[0]);
            minLon = Math.min(minLon, point[1]);
            maxLon = Math.max(maxLon, point[1]);
        }
        long side = 1L << ORDER;
        Map<RequestId, Long> index = new LinkedHashMap<>();
        for (Map.Entry<RequestId, double[]> entry : points.entrySet()) {
            long x = normalize(entry.getValue()[1], minLon, maxLon, side);
            long y = normalize(entry.getValue()[0], minLat, maxLat, side);
            index.put(entry.getKey(), hilbertIndex(side, x, y));
        }
        List<RequestId> order = new ArrayList<>(InsertionSearch.sortedRequestIds(problem));
        order.sort(Comparator.<RequestId>comparingLong(index::get).thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }

    private static long normalize(double value, double min, double max, long side) {
        if (max <= min) {
            return 0L;
        }
        long cell = (long) Math.floor((value - min) / (max - min) * side);
        return Math.min(cell, side - 1);
    }

    /** 표준 xy → d (Hilbert curve, side = 2^ORDER). */
    static long hilbertIndex(long side, long x, long y) {
        long d = 0L;
        for (long s = side / 2; s > 0; s /= 2) {
            long rx = (x & s) > 0 ? 1 : 0;
            long ry = (y & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);
            if (ry == 0) {
                if (rx == 1) {
                    x = side - 1 - x;
                    y = side - 1 - y;
                }
                long t = x;
                x = y;
                y = t;
            }
        }
        return d;
    }
}
