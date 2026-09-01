package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.TimeFixtures.allDay;
import static com.ronext.rpdptw.solve.TimeFixtures.dateTime;
import static com.ronext.rpdptw.solve.TimeFixtures.hours;
import static com.ronext.rpdptw.solve.TimeFixtures.minutes;
import static com.ronext.rpdptw.solve.TimeFixtures.sec;
import static com.ronext.rpdptw.solve.TimeFixtures.seconds;
import static com.ronext.rpdptw.solve.TimeFixtures.time;
import static com.ronext.rpdptw.solve.TimeFixtures.window;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.TimeWindow;

class TimeFixturesTest {

    @Test
    void testSecondsConversion() {
        assertEquals(0L, sec(LocalTime.of(0, 0)));
        assertEquals(28_800L, sec(LocalTime.of(8, 0)));
        assertEquals(28_800L, sec("08:00"));
        assertEquals(28_800L, sec("08:00:00"));
        assertEquals(28_800L, sec(8, 0));

        // n일차
        assertEquals(28_800L, sec(1, LocalTime.of(8, 0)));
        assertEquals(115_200L, sec(2, LocalTime.of(8, 0))); // 86400 + 28800
        assertEquals(201_600L, sec(3, "08:00")); // 86400*2 + 28800
        assertEquals(300_000L, sec(4, "11:20")); // 86400*3 + 11*3600 + 20*60

        // LocalDateTime
        assertEquals(0L, sec(LocalDateTime.of(2023, 9, 13, 0, 0)));
        assertEquals(86_400L, sec(LocalDateTime.of(2023, 9, 14, 0, 0)));

        // Duration / Units
        assertEquals(60L, sec(Duration.ofMinutes(1)));
        assertEquals(120L, minutes(2));
        assertEquals(7_200L, hours(2));
        assertEquals(30L, seconds(30));
    }

    @Test
    void testTimeWindowCreation() {
        assertEquals(new TimeWindow(28_800, 64_800), window("08:00", "18:00"));
        assertEquals(new TimeWindow(28_800, 64_800), window(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        assertEquals(new TimeWindow(28_800, 64_800), window(8, 0, 18, 0));

        assertEquals(new TimeWindow(115_200, 147_600), window(2, "08:00", "17:00"));
        assertEquals(new TimeWindow(145_800, 234_000), window(2, "16:30", 3, "17:00"));

        assertEquals(new TimeWindow(0L, 86_399L), allDay());
        assertEquals(new TimeWindow(86_400L, 172_799L), allDay(2));
    }

    @Test
    void testDateTimeAndLocalTimeHelpers() {
        assertEquals(LocalTime.of(8, 30), time("08:30"));
        assertEquals(LocalTime.of(8, 30), time(8, 30));

        assertEquals(LocalDateTime.of(2023, 9, 13, 8, 30), dateTime(1, "08:30"));
        assertEquals(LocalDateTime.of(2023, 9, 14, 16, 0), dateTime(2, "16:00"));
        assertEquals(LocalDateTime.of(2023, 9, 15, 9, 0), dateTime(3, 9, 0));
    }
}
