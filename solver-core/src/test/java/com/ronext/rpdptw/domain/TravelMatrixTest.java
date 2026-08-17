package com.ronext.rpdptw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TravelMatrixTest {

    private static final LocationId A = new LocationId("A");
    private static final LocationId B = new LocationId("B");
    private static final LocationId C = new LocationId("C");
    private static final LocationId FOREIGN = new LocationId("FOREIGN");

    @Test
    void correctsMissingDistanceByGreatCircle() {
        Location locA = loc(A, 37.0, 127.0);
        Location locB = loc(B, 38.0, 127.0);
        int gc = GreatCircle.distanceMeter(locA, locB);
        int given = gc == 1 ? 2 : 1;

        TravelMatrix matrix = TravelMatrix.prepare(
                locations(locA, locB),
                List.of(new TravelEntry(A, B, given, 10)),
                Set.of(45));

        assertEquals(given, matrix.distanceMeter(A, B));
        assertEquals(gc, matrix.distanceMeter(B, A));
        assertNotEquals(gc, given);
    }

    @Test
    void correctsMissingTimeByCeilFormula() {
        Location origin = loc(A, 0.0, 0.0);
        Location at1000 = loc(B, latitudeForMeters(1000), 0.0);
        Location at1001 = loc(C, latitudeForMeters(1001), 0.0);
        assertEquals(1000, GreatCircle.distanceMeter(origin, at1000));
        assertEquals(1001, GreatCircle.distanceMeter(origin, at1001));

        TravelMatrix matrix = TravelMatrix.prepare(locations(origin, at1000, at1001), List.of(), Set.of(45));

        assertEquals(1000, matrix.distanceMeter(A, B));
        assertEquals(80, matrix.timeSec(A, B, 45));
        assertEquals(1001, matrix.distanceMeter(A, C));
        assertEquals(81, matrix.timeSec(A, C, 45));
        assertEquals(TravelMatrix.correctedTimeSec(matrix.distanceMeter(B, C), 45), matrix.timeSec(B, C, 45));
    }

    @Test
    void selfArcAlwaysZero() {
        Location locA = loc(A, 37.0, 127.0);
        Location locB = loc(B, 38.0, 127.0);
        Map<LocationId, Location> locs = locations(locA, locB);

        TravelMatrix withSelf = TravelMatrix.prepare(
                locs,
                List.of(new TravelEntry(A, A, 9999, 0), new TravelEntry(B, B, 9999, 0)),
                Set.of(45));
        TravelMatrix withoutSelf = TravelMatrix.prepare(locs, List.of(), Set.of(45));

        // 입력이 준 legacy sentinel(9999)이든 부재든, 표의 self는 언제나 0이다.
        for (TravelMatrix matrix : List.of(withSelf, withoutSelf)) {
            assertEquals(0, matrix.distanceMeter(A, A));
            assertEquals(0, matrix.timeSec(A, A, 45));
            assertEquals(0, matrix.distanceMeter(B, B));
            assertEquals(0, matrix.timeSec(B, B, 45));
        }
    }

    @Test
    void keepsGivenArcsAsymmetric() {
        Location locA = loc(A, 37.0, 127.0);
        Location locB = loc(B, 38.0, 127.0);
        int gcBack = GreatCircle.distanceMeter(locB, locA);

        TravelMatrix matrix = TravelMatrix.prepare(
                locations(locA, locB),
                List.of(new TravelEntry(A, B, 100, 20)),
                Set.of(45));

        assertEquals(100, matrix.distanceMeter(A, B));
        assertEquals(20, matrix.timeSec(A, B, 45));
        assertEquals(gcBack, matrix.distanceMeter(B, A));
        assertNotEquals(100, gcBack);
        assertEquals(TravelMatrix.correctedTimeSec(gcBack, 45), matrix.timeSec(B, A, 45));
    }

    @Test
    void rejectsDuplicateAndDropsForeignEntries() {
        Location locA = loc(A, 37.0, 127.0);
        Location locB = loc(B, 38.0, 127.0);
        Map<LocationId, Location> locs = locations(locA, locB);

        assertThrows(
                IllegalArgumentException.class,
                () -> TravelMatrix.prepare(
                        locs,
                        List.of(new TravelEntry(A, B, 1, 1), new TravelEntry(A, B, 1, 1)),
                        Set.of(45)));
        assertThrows(
                IllegalArgumentException.class,
                () -> TravelMatrix.prepare(
                        locs,
                        List.of(new TravelEntry(A, B, 1, 1), new TravelEntry(A, B, 2, 2)),
                        Set.of(45)));

        TravelMatrix matrix = TravelMatrix.prepare(
                locs,
                List.of(
                        new TravelEntry(FOREIGN, A, 1, 1),
                        new TravelEntry(A, FOREIGN, 2, 2),
                        new TravelEntry(FOREIGN, FOREIGN, 3, 3)),
                Set.of(45));
        int gc = GreatCircle.distanceMeter(locA, locB);
        assertEquals(gc, matrix.distanceMeter(A, B));
        assertEquals(gc, matrix.distanceMeter(B, A));
        assertEquals(TravelMatrix.correctedTimeSec(gc, 45), matrix.timeSec(A, B, 45));
    }

    @Test
    void perSpeedCorrectedTimes() {
        Location locA = loc(A, 37.0, 127.0);
        Location locB = loc(B, 38.0, 127.0);
        int gcBack = GreatCircle.distanceMeter(locB, locA);

        TravelMatrix matrix = TravelMatrix.prepare(
                locations(locA, locB),
                List.of(new TravelEntry(A, B, 100, 20)),
                Set.of(45, 60));

        assertEquals(20, matrix.timeSec(A, B, 45));
        assertEquals(20, matrix.timeSec(A, B, 60));
        assertEquals(TravelMatrix.correctedTimeSec(gcBack, 45), matrix.timeSec(B, A, 45));
        assertEquals(TravelMatrix.correctedTimeSec(gcBack, 60), matrix.timeSec(B, A, 60));
        assertNotEquals(matrix.timeSec(B, A, 45), matrix.timeSec(B, A, 60));

        assertThrows(IllegalArgumentException.class, () -> matrix.timeSec(A, B, 30));
        assertThrows(IllegalArgumentException.class, () -> matrix.distanceMeter(FOREIGN, A));
        assertThrows(IllegalArgumentException.class, () -> matrix.timeSec(FOREIGN, A, 45));
    }

    private static Location loc(LocationId id, double lat, double lon) {
        return new Location(id, lat, lon);
    }

    private static Map<LocationId, Location> locations(Location... locs) {
        Map<LocationId, Location> map = new LinkedHashMap<>();
        for (Location loc : locs) {
            map.put(loc.id(), loc);
        }
        return map;
    }

    private static double latitudeForMeters(int targetMeter) {
        Location origin = loc(new LocationId("O"), 0.0, 0.0);
        double lo = 0.0;
        double hi = 2.0;
        for (int i = 0; i < 80; i++) {
            double mid = (lo + hi) / 2;
            int d = GreatCircle.distanceMeter(origin, loc(new LocationId("X"), mid, 0.0));
            if (d == targetMeter) {
                return mid;
            }
            if (d < targetMeter) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        throw new AssertionError("no latitude for " + targetMeter + " m");
    }
}
