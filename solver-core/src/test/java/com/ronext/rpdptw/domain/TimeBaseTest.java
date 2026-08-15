package com.ronext.rpdptw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class TimeBaseTest {

    private static final LocalDateTime ORIGIN = LocalDateTime.of(2023, 9, 13, 0, 0, 0);

    @Test
    void secondsFromPlanStart() {
        TimeBase timeBase = new TimeBase(ORIGIN);

        assertEquals(0L, timeBase.toSeconds(ORIGIN));
        assertEquals(3_600L, timeBase.toSeconds(ORIGIN.plusHours(1)));
        assertEquals(86_400L, timeBase.toSeconds(ORIGIN.plusDays(1)));

        assertEquals(ORIGIN.plusHours(2), timeBase.toWallClock(7_200L));
        assertEquals(ORIGIN, timeBase.toWallClock(timeBase.toSeconds(ORIGIN)));

        long beforeOrigin = timeBase.toSeconds(ORIGIN.minusHours(1));
        assertEquals(-3_600L, beforeOrigin);
        assertEquals(ORIGIN.minusHours(1), timeBase.toWallClock(beforeOrigin));
    }

    @Test
    void expandsDailyWindows() {
        TimeBase midnight = new TimeBase(ORIGIN);
        long threeDays = 3 * 86_400L;

        List<TimeWindow> mergedDefault = midnight.dailyWindows(
                LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59), threeDays, "depot.windows");
        assertEquals(List.of(new TimeWindow(0, 259_199)), mergedDefault);

        List<TimeWindow> workThreeDays = midnight.dailyWindows(
                LocalTime.of(8, 0, 0), LocalTime.of(17, 0, 0), threeDays, "vehicle.workWindows");
        assertEquals(
                List.of(
                        new TimeWindow(28_800, 61_200),
                        new TimeWindow(115_200, 147_600),
                        new TimeWindow(201_600, 234_000)),
                workThreeDays);

        TimeBase startsAtTen = new TimeBase(ORIGIN.withHour(10));
        List<TimeWindow> clippedOpen = startsAtTen.dailyWindows(
                LocalTime.of(8, 0, 0),
                LocalTime.of(17, 0, 0),
                14 * 3_600L,
                "vehicle.workWindows");
        assertEquals(List.of(new TimeWindow(0, 25_200)), clippedOpen);

        List<TimeWindow> clippedClose = midnight.dailyWindows(
                LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59), 18 * 3_600L, "vehicle.workWindows");
        assertEquals(List.of(new TimeWindow(0, 64_799)), clippedClose);

        List<TimeWindow> empty = startsAtTen.dailyWindows(
                LocalTime.of(8, 0, 0), LocalTime.of(9, 0, 0), 2 * 3_600L, "vehicle.workWindows");
        assertEquals(List.of(), empty);

        assertWindowInvariants(mergedDefault);
        assertWindowInvariants(workThreeDays);
        assertWindowInvariants(clippedOpen);
        assertWindowInvariants(clippedClose);
        assertWindowInvariants(empty);
    }

    @Test
    void acceptsOvernightWindowAndRejectsZeroLength() {
        TimeBase midnight = new TimeBase(ORIGIN);
        long threeDays = 3 * 86_400L;

        List<TimeWindow> overnight = midnight.dailyWindows(
                LocalTime.of(22, 0, 0), LocalTime.of(6, 0, 0), threeDays, "vehicle.workWindows");
        assertEquals(
                List.of(
                        new TimeWindow(0, 21_600),
                        new TimeWindow(79_200, 108_000),
                        new TimeWindow(165_600, 194_400),
                        new TimeWindow(252_000, 259_199)),
                overnight);
        assertWindowInvariants(overnight);

        InputException zeroLength = assertThrows(
                InputException.class,
                () -> midnight.dailyWindows(
                        LocalTime.of(9, 0, 0), LocalTime.of(9, 0, 0), threeDays, "orders[0].windows"));
        assertEquals(InputException.Kind.INVALID_INPUT, zeroLength.kind());
        assertEquals("orders[0].windows", zeroLength.field());
    }

    private static void assertWindowInvariants(List<TimeWindow> windows) {
        for (int i = 0; i < windows.size(); i++) {
            TimeWindow window = windows.get(i);
            assertTrue(window.openSec() <= window.closeSec());
            if (i > 0) {
                assertTrue(windows.get(i - 1).closeSec() + 1 < window.openSec());
            }
        }
    }
}
