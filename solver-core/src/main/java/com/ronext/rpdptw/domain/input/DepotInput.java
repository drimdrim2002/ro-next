package com.ronext.rpdptw.domain.input;

import java.time.LocalTime;

public record DepotInput(
        String locId,
        String latText,
        String lonText,
        LocalTime openTime,
        LocalTime closeTime,
        String zoneId) {}
