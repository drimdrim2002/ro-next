package com.ronext.rpdptw.domain;

public record LocationId(String value) {

    public LocationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value");
        }
    }

    /** locId 부재 시 좌표 원문 문자열로 생성 — "@{lat},{lon}" */
    public static LocationId generated(String latText, String lonText) {
        return new LocationId("@" + latText + "," + lonText);
    }
}
