package com.ronext.rpdptw.domain.input;

import java.util.List;
import java.util.Set;

public record RequestInput(
        String orderId,
        SideInput pickup,
        SideInput delivery,
        List<ItemInput> items,
        List<String> vehicleFeatures,
        Set<String> requiredCapabilities) {}
