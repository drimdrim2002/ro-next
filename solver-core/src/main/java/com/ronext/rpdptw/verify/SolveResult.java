package com.ronext.rpdptw.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import com.ronext.rpdptw.domain.DeliveryPolicy;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Evaluation;

/** 결과 모델 — Domain §11.1 의미 목록과 1:1. wire 필드명·직렬화는 Stage 6이 확정한다 (§11.2). */
public record SolveResult(
        String planId,
        Status status,
        Run run,
        List<RouteResult> routes,
        List<Unassigned> unassigned,
        Evaluation metrics) {

    public SolveResult {
        Objects.requireNonNull(planId, "planId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(run, "run");
        routes = List.copyOf(routes);
        unassigned = List.copyOf(unassigned);
        Objects.requireNonNull(metrics, "metrics");
    }

    /** FAILED는 열거 호환용이다 — 재검증에 실패한 배차안은 결과를 만들지 않는다 (§5 N6). */
    public enum Status { DONE, FAILED }

    public record Run(String inputKey,
                      LocalDateTime receivedAt,
                      LocalDateTime startedAt,
                      LocalDateTime finishedAt,
                      String profileId,
                      boolean verified,
                      DeliveryPolicy deliveryPolicy,
                      SearchBudget searchBudget) {

        public Run {
            Objects.requireNonNull(inputKey, "inputKey");
            Objects.requireNonNull(receivedAt, "receivedAt");
            Objects.requireNonNull(startedAt, "startedAt");
            Objects.requireNonNull(finishedAt, "finishedAt");
            Objects.requireNonNull(profileId, "profileId");
            Objects.requireNonNull(deliveryPolicy, "deliveryPolicy");
            Objects.requireNonNull(searchBudget, "searchBudget");
        }
    }

    /**
     * 탐색 예산(Domain §2.5.1)을 결과에 기록하기 위한 값. 알고리즘 튜닝 파라미터도,
     * 종료 사유 같은 실행 통계도 담지 않는다 (그건 탐색 쪽 통계의 몫이다 — §4.1).
     */
    public record SearchBudget(long timeLimitSec,
                               OptionalLong maxSteps,
                               OptionalLong idleSteps,
                               OptionalLong idleSec,
                               long seed) {

        public SearchBudget {
            Objects.requireNonNull(maxSteps, "maxSteps");
            Objects.requireNonNull(idleSteps, "idleSteps");
            Objects.requireNonNull(idleSec, "idleSec");
        }
    }

    public record RouteResult(VehicleId vehicleId,
                              List<Visit> visits,
                              Optional<LocalDateTime> depotDeparture,
                              Optional<LocalDateTime> depotReturn,
                              long driveDistMeter,
                              long driveTimeSec,
                              int stopCount,
                              long routeOperationalTimeSec) {

        public RouteResult {
            Objects.requireNonNull(vehicleId, "vehicleId");
            visits = List.copyOf(visits);
            depotDeparture = Objects.requireNonNull(depotDeparture, "depotDeparture");
            depotReturn = Objects.requireNonNull(depotReturn, "depotReturn");
        }
    }

    public record Visit(RequestId orderId,
                        boolean pickup,
                        LocationId locationId,
                        LocalDateTime arrival,
                        LocalDateTime serviceStart,
                        LocalDateTime serviceEnd,
                        long loadWeight,
                        long loadVolume) {

        public Visit {
            Objects.requireNonNull(orderId, "orderId");
            Objects.requireNonNull(locationId, "locationId");
            Objects.requireNonNull(arrival, "arrival");
            Objects.requireNonNull(serviceStart, "serviceStart");
            Objects.requireNonNull(serviceEnd, "serviceEnd");
        }
    }

    public record Unassigned(RequestId orderId, UnassignedReason reason) {

        public Unassigned {
            Objects.requireNonNull(orderId, "orderId");
            Objects.requireNonNull(reason, "reason");
        }
    }
}
