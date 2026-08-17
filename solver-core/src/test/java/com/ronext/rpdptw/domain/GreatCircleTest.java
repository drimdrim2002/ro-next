package com.ronext.rpdptw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreatCircleTest {

    @Test
    void knownDistances() {
        Location origin = new Location(new LocationId("A"), 37.0, 127.0);
        Location same = new Location(new LocationId("B"), 37.0, 127.0);
        Location oneDegreeNorth = new Location(new LocationId("C"), 38.0, 127.0);

        assertEquals(0, GreatCircle.distanceMeter(origin, same));
        assertEquals(111_195, GreatCircle.distanceMeter(origin, oneDegreeNorth));
    }
}
