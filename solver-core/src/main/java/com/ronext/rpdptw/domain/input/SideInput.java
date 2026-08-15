package com.ronext.rpdptw.domain.input;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record SideInput(
        String locId,
        String latText,
        String lonText,
        LocalTime openTime,
        LocalTime closeTime,
        Long durationSec,
        LocalDateTime reqDate,
        String zoneId) {}
