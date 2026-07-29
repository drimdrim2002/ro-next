package com.ronext.rpdptw.fixture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Keeps targeted fixture-module test runs from succeeding with zero executed tests. */
class FixtureBoundarySelfTest {
    @Test
    void fixtureBytecodeLivesUnderTheTestSourceSet() {
        assertEquals("com.ronext.rpdptw.fixture", getClass().getPackageName());
    }
}
