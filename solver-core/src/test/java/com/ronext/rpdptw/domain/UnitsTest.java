package com.ronext.rpdptw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class UnitsTest {

    private static final Supplier<String> WEIGHT = () -> "orders[0].items[0].weight";

    @Test
    void floorsAtThirdDecimal() {
        assertEquals(26_200L, Units.toMilli(new BigDecimal("26.2"), WEIGHT));
        assertEquals(1_234L, Units.toMilli(new BigDecimal("1.23456"), WEIGHT));
        assertEquals(0L, Units.toMilli(new BigDecimal("0.0009"), WEIGHT));
    }

    @Test
    void rejectsNegative() {
        InputException weight = assertThrows(
                InputException.class, () -> Units.toMilli(new BigDecimal("-1"), WEIGHT));
        assertEquals(InputException.Kind.INVALID_INPUT, weight.kind());

        InputException volume = assertThrows(
                InputException.class,
                () -> Units.toMilli(new BigDecimal("-0.01"), () -> "orders[0].items[0].volume"));
        assertEquals(InputException.Kind.INVALID_INPUT, volume.kind());
    }

    @Test
    void rejectsFractionalDistanceAndTime() {
        InputException distance = assertThrows(
                InputException.class,
                () -> Units.toWholeMeters(new BigDecimal("310708.03"), () -> "distanceMatrix[0].D"));
        assertEquals(InputException.Kind.INVALID_INPUT, distance.kind());

        InputException time = assertThrows(
                InputException.class,
                () -> Units.toWholeSeconds(new BigDecimal("17265.5"), () -> "distanceMatrix[0].U"));
        assertEquals(InputException.Kind.INVALID_INPUT, time.kind());

        InputException integerWrittenAsDecimal = assertThrows(
                InputException.class,
                () -> Units.toWholeMeters(new BigDecimal("15.0"), () -> "distanceMatrix[1].D"));
        assertEquals(InputException.Kind.INVALID_INPUT, integerWrittenAsDecimal.kind());

        assertEquals(15, Units.toWholeMeters(new BigDecimal("15"), () -> "distanceMatrix[2].D"));
        assertEquals(15, Units.toWholeSeconds(new BigDecimal("15"), () -> "distanceMatrix[2].U"));
    }
}
