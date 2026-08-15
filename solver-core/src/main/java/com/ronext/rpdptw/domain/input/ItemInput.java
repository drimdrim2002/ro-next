package com.ronext.rpdptw.domain.input;

import java.math.BigDecimal;

public record ItemInput(
        String itemId,
        BigDecimal weightKg,
        BigDecimal volumeCbm,
        Integer qty,
        Long taskTimeSec) {}
