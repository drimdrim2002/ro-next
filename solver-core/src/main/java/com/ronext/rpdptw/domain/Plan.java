package com.ronext.rpdptw.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Plan(
        String planId,
        Optional<String> customerId,
        TimeBase timeBase,
        long planEndSec,
        List<Depot> depots,
        List<Request> requests,
        List<Vehicle> vehicles,
        Map<LocationId, Location> locations,
        List<TravelEntry> travelEntries,
        DeliveryPolicy deliveryPolicy) {

    public Plan {
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("planId");
        }
        customerId = Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(timeBase, "timeBase");
        depots = List.copyOf(depots);
        requests = List.copyOf(requests);
        vehicles = List.copyOf(vehicles);
        // Map.copyOf는 순회 순서를 JVM 실행마다 흔든다 — Stage 2가 이 맵으로 색인을 매기므로
        // 같은 입력이 다른 색인을 받는다. 정규화의 삽입 순서(차고 → 주문 side)를 보존한다.
        locations = Collections.unmodifiableMap(new LinkedHashMap<>(locations));
        travelEntries = List.copyOf(travelEntries);
        Objects.requireNonNull(deliveryPolicy, "deliveryPolicy");
    }
}
