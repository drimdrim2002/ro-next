package com.ronext.rpdptw.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record TimeBase(LocalDateTime origin) {

    public TimeBase {
        Objects.requireNonNull(origin, "origin");
    }

    public long toSeconds(LocalDateTime t) {
        return ChronoUnit.SECONDS.between(origin, t);
    }

    /**
     * 날마다 반복되는 창(open~close)을 계획 기간에 맞춰 절대 창 목록으로 펼친다.
     * close == open이면 InputException(INVALID_INPUT). 결과가 빈 목록일 수 있다.
     */
    public List<TimeWindow> dailyWindows(LocalTime open, LocalTime close, long planEndSec, String field) {
        Objects.requireNonNull(open, "open");
        Objects.requireNonNull(close, "close");
        if (close.equals(open)) {
            throw new InputException(InputException.Kind.INVALID_INPUT, field, "time window close == open");
        }
        boolean overnight = close.isBefore(open);
        LocalDate startDate = origin.toLocalDate();
        LocalDate endDate = origin.plusSeconds(planEndSec).toLocalDate();
        long lastSec = planEndSec - 1;
        List<TimeWindow> windows = new ArrayList<>();
        for (LocalDate date = startDate.minusDays(1); !date.isAfter(endDate); date = date.plusDays(1)) {
            long openSec = toSeconds(LocalDateTime.of(date, open));
            LocalDate closeDate = overnight ? date.plusDays(1) : date;
            long closeSec = toSeconds(LocalDateTime.of(closeDate, close));
            long clippedOpen = Math.max(openSec, 0L);
            long clippedClose = Math.min(closeSec, lastSec);
            if (clippedOpen <= clippedClose) {
                windows.add(new TimeWindow(clippedOpen, clippedClose));
            }
        }
        return mergeTouching(windows);
    }

    private static List<TimeWindow> mergeTouching(List<TimeWindow> windows) {
        if (windows.isEmpty()) {
            return List.of();
        }
        List<TimeWindow> merged = new ArrayList<>();
        TimeWindow current = windows.get(0);
        for (int i = 1; i < windows.size(); i++) {
            TimeWindow next = windows.get(i);
            if (current.closeSec() + 1 >= next.openSec()) {
                current = new TimeWindow(current.openSec(), Math.max(current.closeSec(), next.closeSec()));
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return List.copyOf(merged);
    }

    public LocalDateTime toWallClock(long sec) {
        return origin.plusSeconds(sec);
    }
}
