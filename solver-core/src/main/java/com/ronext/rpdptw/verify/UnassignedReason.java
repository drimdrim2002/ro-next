package com.ronext.rpdptw.verify;

/** 미배정 사유 — Domain §11.1의 4종 고정 목록. 값을 늘리지 않는다 (stage-05 §9). */
public enum UnassignedReason {
    NO_COMPATIBLE_VEHICLE,
    CAPACITY,
    TIME_WINDOW_INFEASIBLE,
    NOT_PLACED
}
