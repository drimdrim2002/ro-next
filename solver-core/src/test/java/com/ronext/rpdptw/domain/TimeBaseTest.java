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

    record DateTimeRange(LocalDateTime start, LocalDateTime end) {
        static DateTimeRange of(LocalDateTime start, LocalDateTime end) {
            return new DateTimeRange(start, end);
        }
    }

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
        LocalDateTime threeDaysEnd = ORIGIN.plusDays(3);

        List<DateTimeRange> mergedDefault = expandToDateTimeRanges(
                midnight,
                LocalTime.of(0, 0, 0),
                LocalTime.of(23, 59, 59),
                threeDaysEnd,
                "depot.windows");
        assertEquals(
                List.of(DateTimeRange.of(
                        LocalDateTime.of(2023, 9, 13, 0, 0, 0),
                        LocalDateTime.of(2023, 9, 15, 23, 59, 59))),
                mergedDefault);

        List<DateTimeRange> workThreeDays = expandToDateTimeRanges(
                midnight,
                LocalTime.of(8, 0, 0),
                LocalTime.of(17, 0, 0),
                threeDaysEnd,
                "vehicle.workWindows");
        assertEquals(
                List.of(
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 13, 8, 0, 0),
                                LocalDateTime.of(2023, 9, 13, 17, 0, 0)),
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 14, 8, 0, 0),
                                LocalDateTime.of(2023, 9, 14, 17, 0, 0)),
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 15, 8, 0, 0),
                                LocalDateTime.of(2023, 9, 15, 17, 0, 0))),
                workThreeDays);

        TimeBase startsAtTen = new TimeBase(ORIGIN.withHour(10));
        LocalDateTime fourteenHoursEnd = ORIGIN.withHour(10).plusHours(14);
        List<DateTimeRange> clippedOpen = expandToDateTimeRanges(
                startsAtTen,
                LocalTime.of(8, 0, 0),
                LocalTime.of(17, 0, 0),
                fourteenHoursEnd,
                "vehicle.workWindows");
        assertEquals(
                List.of(DateTimeRange.of(
                        LocalDateTime.of(2023, 9, 13, 10, 0, 0),
                        LocalDateTime.of(2023, 9, 13, 17, 0, 0))),
                clippedOpen);

        LocalDateTime eighteenHoursEnd = ORIGIN.plusHours(18);
        List<DateTimeRange> clippedClose = expandToDateTimeRanges(
                midnight,
                LocalTime.of(0, 0, 0),
                LocalTime.of(23, 59, 59),
                eighteenHoursEnd,
                "vehicle.workWindows");
        assertEquals(
                List.of(DateTimeRange.of(
                        LocalDateTime.of(2023, 9, 13, 0, 0, 0),
                        LocalDateTime.of(2023, 9, 13, 17, 59, 59))),
                clippedClose);

        LocalDateTime twoHoursEnd = startsAtTen.origin().plusHours(2);
        List<DateTimeRange> empty = expandToDateTimeRanges(
                startsAtTen,
                LocalTime.of(8, 0, 0),
                LocalTime.of(9, 0, 0),
                twoHoursEnd,
                "vehicle.workWindows");
        assertEquals(List.of(), empty);
    }

    @Test
    void acceptsOvernightWindowAndRejectsZeroLength() {
        TimeBase midnight = new TimeBase(ORIGIN);
        LocalDateTime threeDaysEnd = ORIGIN.plusDays(3);

        List<DateTimeRange> overnight = expandToDateTimeRanges(
                midnight,
                LocalTime.of(22, 0, 0),
                LocalTime.of(6, 0, 0),
                threeDaysEnd,
                "vehicle.workWindows");
        assertEquals(
                List.of(
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 13, 0, 0, 0),
                                LocalDateTime.of(2023, 9, 13, 6, 0, 0)),
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 13, 22, 0, 0),
                                LocalDateTime.of(2023, 9, 14, 6, 0, 0)),
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 14, 22, 0, 0),
                                LocalDateTime.of(2023, 9, 15, 6, 0, 0)),
                        DateTimeRange.of(
                                LocalDateTime.of(2023, 9, 15, 22, 0, 0),
                                LocalDateTime.of(2023, 9, 15, 23, 59, 59))),
                overnight);

        InputException zeroLength = assertThrows(
                InputException.class,
                () -> expandToDateTimeRanges(
                        midnight,
                        LocalTime.of(9, 0, 0),
                        LocalTime.of(9, 0, 0),
                        threeDaysEnd,
                        "orders[0].windows"));
        assertEquals(InputException.Kind.INVALID_INPUT, zeroLength.kind());
        assertEquals("orders[0].windows", zeroLength.field());
    }

    private List<DateTimeRange> expandToDateTimeRanges(
            TimeBase timeBase,
            LocalTime open,
            LocalTime close,
            LocalDateTime planEnd,
            String field) {
        long planEndSec = timeBase.toSeconds(planEnd);
        List<TimeWindow> windows = timeBase.dailyWindows(open, close, planEndSec, field);
        assertWindowInvariants(windows);
        return windows.stream()
                .map(w -> DateTimeRange.of(timeBase.toWallClock(w.openSec()), timeBase.toWallClock(w.closeSec())))
                .toList();
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
