package com.ronext.rpdptw.domain.input;

import java.time.LocalDateTime;
import java.util.List;

public record PlanInput(
        String planId,
        String customerId,
        LocalDateTime planStart,
        LocalDateTime planEnd,
        List<DepotInput> depots,
        List<RequestInput> requests,
        List<VehicleInput> vehicles,
        List<TravelEntryInput> travelEntries,
        OptionsInput options) {}
