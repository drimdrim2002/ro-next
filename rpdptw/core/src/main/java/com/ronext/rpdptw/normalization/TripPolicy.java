package com.ronext.rpdptw.normalization;

public sealed interface TripPolicy {
    record OneWay() implements TripPolicy {}
    record SingleRoundTrip() implements TripPolicy {}
}
