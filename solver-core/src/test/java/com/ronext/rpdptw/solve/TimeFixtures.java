package com.ronext.rpdptw.solve;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.ronext.rpdptw.domain.TimeBase;
import com.ronext.rpdptw.domain.TimeWindow;

/**
 * 단위 테스트에서 {@link LocalDateTime}, {@link LocalTime}, {@link Duration}을
 * 손쉽게 초 단위 {@code long} 및 {@link TimeWindow}로 변환하기 위한 테스트 유틸리티.
 */
public final class TimeFixtures {

    public static final LocalDateTime BASE_DATE_TIME = LocalDateTime.of(2023, 9, 13, 0, 0, 0);
    public static final LocalDate BASE_DATE = BASE_DATE_TIME.toLocalDate();
    public static final TimeBase DEFAULT_TIME_BASE = new TimeBase(BASE_DATE_TIME);

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TimeFixtures() {}

    /** 당일 LocalTime -> 기준일(Day 1) 초 */
    public static long sec(LocalTime time) {
        return time.toSecondOfDay();
    }

    /** 시, 분 -> 당일(Day 1) 초 */
    public static long sec(int hour, int minute) {
        return sec(LocalTime.of(hour, minute));
    }

    /** "HH:mm" 또는 "HH:mm:ss" -> 당일(Day 1) 초 */
    public static long sec(String timeStr) {
        return sec(parseTime(timeStr));
    }

    /** 기준일 기준 LocalDateTime -> 초 */
    public static long sec(LocalDateTime dateTime) {
        return DEFAULT_TIME_BASE.toSeconds(dateTime);
    }

    /** n일차(1-based) LocalTime -> 초 */
    public static long sec(int dayIndex, LocalTime time) {
        return (dayIndex - 1) * 86_400L + time.toSecondOfDay();
    }

    /** n일차(1-based) "HH:mm" -> 초 */
    public static long sec(int dayIndex, String timeStr) {
        return sec(dayIndex, parseTime(timeStr));
    }

    /** n일차(1-based) 시, 분 -> 초 */
    public static long sec(int dayIndex, int hour, int minute) {
        return sec(dayIndex, LocalTime.of(hour, minute));
    }

    /** Duration -> 초 */
    public static long sec(Duration duration) {
        return duration.toSeconds();
    }

    /** 분 -> 초 */
    public static long minutes(long minutes) {
        return minutes * 60L;
    }

    /** 시간 -> 초 */
    public static long hours(long hours) {
        return hours * 3600L;
    }

    /** 초 -> 초 */
    public static long seconds(long seconds) {
        return seconds;
    }

    /** openSec, closeSec으로 TimeWindow 생성 */
    public static TimeWindow window(long openSec, long closeSec) {
        return new TimeWindow(openSec, closeSec);
    }

    /** LocalTime 범위로 당일 TimeWindow 생성 */
    public static TimeWindow window(LocalTime open, LocalTime close) {
        return new TimeWindow(sec(open), sec(close));
    }

    /** "HH:mm", "HH:mm" 문자열로 당일 TimeWindow 생성 */
    public static TimeWindow window(String openStr, String closeStr) {
        return window(parseTime(openStr), parseTime(closeStr));
    }

    /** 시/분으로 당일 TimeWindow 생성 */
    public static TimeWindow window(int openHour, int openMin, int closeHour, int closeMin) {
        return window(LocalTime.of(openHour, openMin), LocalTime.of(closeHour, closeMin));
    }

    /** LocalDateTime 범위로 TimeWindow 생성 */
    public static TimeWindow window(LocalDateTime open, LocalDateTime close) {
        return new TimeWindow(sec(open), sec(close));
    }

    /** n일차 LocalTime 범위로 TimeWindow 생성 */
    public static TimeWindow window(int dayIndex, LocalTime open, LocalTime close) {
        return new TimeWindow(sec(dayIndex, open), sec(dayIndex, close));
    }

    /** n일차 문자열 범위로 TimeWindow 생성 */
    public static TimeWindow window(int dayIndex, String openStr, String closeStr) {
        return window(dayIndex, parseTime(openStr), parseTime(closeStr));
    }

    /** n일차1 시각 ~ n일차2 시각 TimeWindow 생성 */
    public static TimeWindow window(int day1, String openStr, int day2, String closeStr) {
        return new TimeWindow(sec(day1, openStr), sec(day2, closeStr));
    }

    /** 당일 종일창 (00:00:00 ~ 23:59:59) */
    public static TimeWindow allDay() {
        return new TimeWindow(0L, 86_399L);
    }

    /** n일차 종일창 (00:00:00 ~ 23:59:59) */
    public static TimeWindow allDay(int dayIndex) {
        long base = (dayIndex - 1) * 86_400L;
        return new TimeWindow(base, base + 86_399L);
    }

    /** n일차 LocalTime으로 LocalDateTime 생성 */
    public static LocalDateTime dateTime(int dayIndex, LocalTime time) {
        return BASE_DATE_TIME.plusDays(dayIndex - 1).with(time);
    }

    /** n일차 시, 분으로 LocalDateTime 생성 */
    public static LocalDateTime dateTime(int dayIndex, int hour, int minute) {
        return dateTime(dayIndex, LocalTime.of(hour, minute));
    }

    /** n일차 "HH:mm"으로 LocalDateTime 생성 */
    public static LocalDateTime dateTime(int dayIndex, String timeStr) {
        return dateTime(dayIndex, parseTime(timeStr));
    }

    /** LocalTime 생성 헬퍼 */
    public static LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    /** LocalTime 문자열 파싱 헬퍼 */
    public static LocalTime time(String timeStr) {
        return parseTime(timeStr);
    }

    private static LocalTime parseTime(String str) {
        if (str.length() == 5) {
            return LocalTime.parse(str, HH_MM);
        }
        if (str.length() == 8) {
            return LocalTime.parse(str, HH_MM_SS);
        }
        return LocalTime.parse(str);
    }
}
