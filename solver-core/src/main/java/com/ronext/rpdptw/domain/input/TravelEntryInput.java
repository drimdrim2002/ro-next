package com.ronext.rpdptw.domain.input;

import java.math.BigDecimal;

public record TravelEntryInput(
        String fromLocId, String toLocId, BigDecimal distance, BigDecimal time) {}
