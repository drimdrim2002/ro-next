package com.ronext.rpdptw.verify;

import java.util.Objects;
import java.util.Optional;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;

/**
 * 재검증이 찾아낸 위반 한 건 (stage-05 §2.2). Stage 6이 FAILED 원인으로 기록한다 (Domain §12).
 *
 * <p>{@code Kind}는 solve의 {@code StructureViolation.Kind}·{@code Violation}과 의미가 같지만
 * 별도 enum이다 — 참조하면 {@code verify → solve} 경계를 깨고, 이중 기입도 무너진다 (§5 N1·N2).
 * 이름은 리뷰 편의를 위해 의미가 같은 항목끼리 맞춘다.</p>
 */
public record VerifyViolation(Kind kind,
                              Optional<VehicleId> vehicleId,
                              Optional<RequestId> requestId,
                              Optional<NodeId> at,
                              String detail) {

    public VerifyViolation {
        Objects.requireNonNull(kind, "kind");
        vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
        requestId = Objects.requireNonNull(requestId, "requestId");
        at = Objects.requireNonNull(at, "at");
        Objects.requireNonNull(detail, "detail");
    }

    public enum Kind {
        // 구조 (Domain §1.4·§6.3)
        UNKNOWN_VEHICLE,
        EMPTY_ROUTE,
        UNKNOWN_NODE,
        DUPLICATE_NODE,
        PAIR_SPLIT,
        PAIR_INCOMPLETE,
        PICKUP_AFTER_DELIVERY,
        ASSIGNED_AND_BANKED,
        NOT_ASSIGNED_NOT_BANKED,
        UNKNOWN_REQUEST,
        // hard 재계산 (Domain §6.2·§7)
        TIME_WINDOW,
        REQ_DATE,
        WORK_WINDOW,
        DEPOT_WINDOW,
        CAPACITY_WEIGHT,
        CAPACITY_VOLUME,
        END_LOAD_NOT_ZERO,
        MAX_STOP_COUNT,
        MAX_DRIVE_TIME,
        MAX_DRIVE_DIST,
        ZONE_MIX,
        INCOMPATIBLE_VEHICLE,
        PROFILE_HARD,
        // 점수 대조 (Domain §10.2·§6.4)
        SCORE_MISMATCH
    }
}
