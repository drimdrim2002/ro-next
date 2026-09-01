package com.ronext.rpdptw.solve;

import java.util.List;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;

public record Solution(List<Route> routes, Set<RequestId> bank) {

    public Solution {
        routes = List.copyOf(routes);
        bank = Set.copyOf(bank);
    }
}
