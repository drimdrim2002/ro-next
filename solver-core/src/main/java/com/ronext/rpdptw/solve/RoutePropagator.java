package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;
import com.ronext.rpdptw.domain.ServicePattern;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelMatrix;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.eval.VisitFacts;
import com.ronext.rpdptw.problem.NodeRef;
import com.ronext.rpdptw.problem.Problem;

/**
 * 단일 차량 경로(Route)의 물리적 운행 과정을 순차 시뮬레이션(전파, Forward Propagation)하는 엔진.
 *
 * <p>주요 역할:</p>
 * <ul>
 *   <li>시간, 이동 거리, 적재량, 근무창(Work Window), 방문지/차고 시간창(Time Window) 등의 물리적 제약을 순차 검증</li>
 *   <li>Hard 제약 위반 발생 시 점수/패널티 없이 즉시 탈락({@link PropagationResult.Infeasible}) 반환</li>
 *   <li>모든 제약을 만족하면 세부 운행 사실({@link RouteFacts}, {@link VisitFacts}) 생성</li>
 * </ul>
 *
 * <p>전제 조건: {@code visits}는 미등록 노드가 없는 구조적으로 유효한 목록이어야 합니다.</p>
 */
public final class RoutePropagator {

    private RoutePropagator() {}

    /**
     * 경로의 방문 순서대로 물리 상태를 전파하고 타당성을 판정합니다.
     *
     * @param problem   불변 문제 정의
     * @param vehicleId 배정 차량 ID
     * @param visits    고객 방문 노드 순서 (비어 있으면 예외)
     * @return 전파 성공 시 {@link PropagationResult.Feasible}, 위반 시 {@link PropagationResult.Infeasible}
     */
    public static PropagationResult propagate(Problem problem, VehicleId vehicleId, List<NodeId> visits) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(vehicleId, "vehicleId");
        Objects.requireNonNull(visits, "visits");
        if (visits.isEmpty()) {
            throw new IllegalArgumentException("visits must not be empty");
        }
        return new Propagation(problem, vehicleId, visits).run();
    }

    /**
     * 단일 경로의 전파 시뮬레이션 상태를 유지하는 일회용 실행 객체.
     *
     * <p>실행 흐름 (Hard 제약 검사 순서 고정):</p>
     * <ol>
     *   <li>{@link #prepare()}: [절차 0] 출발 적재량(DELIVERY_ONLY 사전 적재) 계산 및 첫 근무창 기준 시각 설정</li>
     *   <li>{@link #departFromStartDepot()}: [절차 1] 출발 차고 출발 시각 확정(차고창/근무창/waitInDepot 적용) 및 첫 이동</li>
     *   <li>{@link #visitAll()}: [절차 2] 전체 방문지 순차 전파 (도착 → 서비스 시작/종료 → 희망마감일 → 적재량 갱신 → 다음 이동 출발)</li>
     *   <li>{@link #arriveAtEndDepot()}: [절차 3] 도착 차고 복귀, PICKUP_ONLY 일괄 하차 및 최종 적재량 검증</li>
     *   <li>{@link #checkRouteLimits()}: [절차 5] 경로 전체 누적 한도(최대 정차 수, 총 운전 시간, 총 운전 거리) 검증</li>
     *   <li>{@link #assembleFacts()}: [절차 4·6] 근무창 사이 휴식 시간 산출 및 최종 {@link RouteFacts} 조립</li>
     * </ol>
     *
     * <p>※ 순서가 바뀌면 동일 경로에 대해 보고되는 첫 번째 위반({@link Violation}) 종류가 달라지므로
     * 반드시 정의된 순서대로 검사를 수행합니다.</p>
     */
    private static final class Propagation {

        private final Problem problem;
        private final VehicleId vehicleId;
        private final Vehicle vehicle;
        private final TravelMatrix travel;
        private final int speedKmH;
        private final List<PlannedVisit> plannedVisits;
        private final List<VisitFacts> visitFacts;
        private final List<TimeWindow> workWindows;

        // [절차 0] 근무창 시작 시각 및 출발 적재량 (spanStartSec은 근무창 검사를 통과한 뒤 유효)
        private long spanStartSec;
        private long initialLoadWeight;
        private long initialLoadVolume;

        // [절차 1~3] 시뮬레이션 진행에 따라 앞으로 갱신되는 위치·시각 및 누적 통계 사실
        private Optional<Long> depotDepartureSec = Optional.empty();
        private Optional<Long> endDepotArrivalSec = Optional.empty();
        private long previousDepartureSec;
        private LocationId previousLocation;
        private LocationId previousStopLocation;
        private long loadWeight;
        private long loadVolume;
        private long driveDistMeter;
        private long driveTimeSec;
        private long customerWaitingSec;
        private long depotWaitingSec;
        private long serviceTimeSec;
        private int stopCount;
        /** 경로의 구체 구역 — 첫 구체 zoneId 방문에서 정해지고 이후 다른 구체 zoneId는 ZONE_MIX (Domain §3.4). */
        private String routeZone;

        Propagation(Problem problem, VehicleId vehicleId, List<NodeId> visits) {
            this.problem = problem;
            this.vehicleId = vehicleId;
            this.vehicle = problem.vehicle(vehicleId);
            this.travel = problem.travel();
            this.speedKmH = problem.resolvedSpeedKmH(vehicleId);
            this.workWindows = vehicle.workWindows();
            this.plannedVisits = new ArrayList<>(visits.size());
            for (NodeId nodeId : visits) {
                NodeRef ref = problem.nodeRef(nodeId)
                        .orElseThrow(() -> new IllegalArgumentException("unknown node: " + nodeId));
                this.plannedVisits.add(new PlannedVisit(nodeId, ref));
            }
            this.visitFacts = new ArrayList<>(visits.size());
        }

        PropagationResult run() {
            Optional<PropagationResult.Infeasible> stopped = prepare()  // 절차 0: 출발 적재·근무창 확인
                    .or(this::departFromStartDepot)                      // 절차 1: 출발 차고 출발 및 첫 구간 이동
                    .or(this::visitAll)                                  // 절차 2: 전체 방문지 순차 전파 루프
                    .or(this::arriveAtEndDepot)                          // 절차 3: 도착 차고 복귀 및 PICKUP_ONLY 하차
                    .or(this::checkRouteLimits);                         // 절차 5: 경로 누적 한도(MAX_*) 검사
            return stopped.isPresent()
                    ? stopped.get()
                    : new PropagationResult.Feasible(assembleFacts());   // 절차 4·6: 휴식 시간 계산 및 사실 조립
        }

        /**
         * [절차 0] 출발 전 사전 준비: 초기 적재량 계산 및 첫 근무창 확인.
         *
         * <ul>
         *   <li>배송 전용 주문(DELIVERY_ONLY): 중간 상차가 없으므로 출발 차고에서 미리 싣고 출발해야 합니다.
         *       따라서 경로에 포함된 모든 DELIVERY_ONLY 주문의 총 무게/부피를 초기 적재량으로 합산합니다.
         *       (DELIVERY_ONLY는 배송 방문 노드만 1개 존재하므로 방문 단위로 순회해도 중복 합산이 없음)</li>
         *   <li>초기 적재량이 차량 최대 용량을 초과하면 즉시 CAPACITY_WEIGHT / CAPACITY_VOLUME 위반을 반환합니다.</li>
         *   <li>차량의 근무창(workWindows)이 없으면 운행 자체가 불가하므로 WORK_WINDOW 위반을 반환합니다.</li>
         *   <li>이 단계의 위반은 방문지 도착 전 발생하므로 위반 노드 위치(at)는 빈 값(Optional.empty)입니다.</li>
         * </ul>
         */
        private Optional<PropagationResult.Infeasible> prepare() {
            // DELIVERY_ONLY는 방문을 하나(하차)만 만들므로 방문 단위로 세도 중복 가산이 없다.
            for (PlannedVisit visit : plannedVisits) {
                Request request = problem.request(visit.requestId());
                if (request.pattern() == ServicePattern.DELIVERY_ONLY) {
                    initialLoadWeight = Math.addExact(initialLoadWeight, request.totalWeight());
                    initialLoadVolume = Math.addExact(initialLoadVolume, request.totalVolume());
                }
            }
            if (initialLoadWeight > vehicle.maxWeight()) {
                return infeasible(Violation.CAPACITY_WEIGHT);
            }
            if (initialLoadVolume > vehicle.maxVolume()) {
                return infeasible(Violation.CAPACITY_VOLUME);
            }
            loadWeight = initialLoadWeight;
            loadVolume = initialLoadVolume;

            if (workWindows.isEmpty()) {
                return infeasible(Violation.WORK_WINDOW);
            }
            spanStartSec = workWindows.getFirst().openSec();
            return Optional.empty();
        }

        /**
         * [절차 1] 출발 차고에서의 출발 시각 결정 및 첫 이동 구간(startDepot -> 첫 방문지) 반영.
         *
         * <ul>
         *   <li><b>출발 차고가 없는 경우 (절차 0'):</b>
         *       차고 이동/대기가 없으므로 첫 방문지 도착 시각을 근무 시작 시각(spanStartSec)으로 간주합니다.</li>
         *   <li><b>출발 차고가 있는 경우:</b>
         *     <ul>
         *       <li>차고 영업 시간창과 차량 근무창이 모두 열려 있고, 첫 이동 시간이 해당 근무창 안에 온전히 들어가는 가장 이른 시각을 계산합니다.</li>
         *       <li><b>차고 대기 정책 waitInDepot = N (즉시 출발):</b>
         *           개장/근무 시작 즉시 출발합니다. 첫 방문지에 일찍 도착하면 고객지에서 대기합니다.</li>
         *       <li><b>차고 대기 정책 waitInDepot = Y (지연 출발):</b>
         *           첫 방문지의 서비스 시작 가능 시각에 딱 맞춰 차고에서 출발을 최대한 늦춥니다.
         *           (고객지 대기 시간을 차고 대기 시간으로 전환하여 고객 불편을 줄임).</li>
         *     </ul>
         *   </li>
         *   <li>차고 대기 시간(depotWaitingSec): 근무 시작 시각부터 실제 차고 출발 시각까지 중 근무창에 포함된 시간을 누적합니다.</li>
         * </ul>
         */
        private Optional<PropagationResult.Infeasible> departFromStartDepot() {
            if (vehicle.startDepot().isEmpty()) {
                previousDepartureSec = spanStartSec;
                previousLocation = null;
                return Optional.empty();
            }

            LocationId startDepot = vehicle.startDepot().get();
            List<TimeWindow> depotWindows = problem.depotAt(startDepot).windows();
            if (depotWindows.isEmpty()) {
                return infeasible(Violation.DEPOT_WINDOW);
            }
            PlannedVisit firstVisit = plannedVisits.getFirst();
            long firstLegTravelSec = travel.timeSec(startDepot, firstVisit.location(), speedKmH);

            TimeWindowFit earliest = fitDepotDeparture(workWindows, depotWindows, spanStartSec, firstLegTravelSec);
            if (earliest instanceof TimeWindowFit.Miss miss) {
                return infeasible(miss.violation());
            }
            long departureSec = ((TimeWindowFit.Hit) earliest).timeSec();

            if (problem.deliveryPolicy().waitInDepot()) {
                // waitInDepot=Y는 출발 시각을 늦추기만 한다.
                // 기준이 되는 첫 방문지의 서비스 시작 시각(firstServiceStartSec)은 N 정책(즉시 출발) 가정으로 먼저 구한다.
                // (방문 루프에서 동일한 값을 다시 계산하여 "Y 정책이 서비스 시작 시각을 바꾸지 않는다"는 불변식을 검증함)
                long firstArrivalSec = Math.addExact(departureSec, firstLegTravelSec);
                TimeWindowFit firstService = fitService(workWindows, firstVisit.side(), firstArrivalSec);
                if (firstService instanceof TimeWindowFit.Miss miss) {
                    return infeasible(miss.violation(), firstVisit.nodeId());
                }
                long firstServiceStartSec = ((TimeWindowFit.Hit) firstService).timeSec();
                departureSec = latestDepotDeparture(
                        workWindows, depotWindows, spanStartSec, firstLegTravelSec, firstServiceStartSec);
            }

            depotDepartureSec = Optional.of(departureSec);
            depotWaitingSec = overlapWithWindows(spanStartSec, departureSec, workWindows);
            previousDepartureSec = departureSec;
            previousLocation = startDepot;
            driveDistMeter = travel.distanceMeter(startDepot, firstVisit.location());
            driveTimeSec = firstLegTravelSec;
            return Optional.empty();
        }

        /** [절차 2] 전체 방문지를 순서대로 순회하며 시뮬레이션 수행. 첫 번째 Hard 위반 발생 시 즉시 중단. */
        private Optional<PropagationResult.Infeasible> visitAll() {
            for (int index = 0; index < plannedVisits.size(); index++) {
                Optional<PropagationResult.Infeasible> stopped = visit(index);
                if (stopped.isPresent()) {
                    return stopped;
                }
            }
            return Optional.empty();
        }

        /**
         * [절차 2] 단일 방문지 처리 시뮬레이션 (1개 노드 방문 시의 7단계 물리 전파).
         *
         * <ol>
         *   <li><b>1. 도착 (arriveAt):</b> 직전 출발 시각 + 구간 이동 시간.</li>
         *   <li><b>2. 서비스 시작 (fitService):</b> 도착 시각 이후이면서, 고객 시간창과 차량 근무창을 모두 만족하는 가장 이른 시각 탐색
         *       (서비스 소요 시간이 당일 근무창을 넘어가면 다음 근무창으로 미룸).</li>
         *   <li><b>3. 서비스 종료:</b> 서비스 시작 시각 + 서비스 소요 시간.</li>
         *   <li><b>4. 희망 마감 시각 검증:</b> 서비스 시작 시각이 고객 희망 마감 시각(reqDateSec)을 초과하면 REQ_DATE 위반.</li>
         *   <li><b>5~6. 적재량 갱신 및 용량 검증 (updateLoad):</b> 픽업(+수요) / 배송(-수요) 반영 후 차량 용량 초과 검증.
         *       이어서 구역 단일성: 이 방문의 구체 zoneId가 경로의 앞선 구체 zoneId와 다르면 ZONE_MIX (Domain §3.4·§7.1-6).</li>
         *   <li><b>7. 다음 이동 출발 시각 결정 (departFrom):</b> 다음 목적지까지의 이동 시간이 온전히 들어가는 가장 이른 근무창 탐색
         *       (현재 근무창에서 다음 이동을 완료할 수 없으면 다음 근무창 시작 시각으로 출발을 미룸).</li>
         * </ol>
         *
         * <p>이후 고객 대기 시간(도착~서비스시작 + 서비스종료~다음출발 중 근무창 시간), 총 정차 수(stopCount) 등을 누적합니다.</p>
         */
        private Optional<PropagationResult.Infeasible> visit(int index) {
            PlannedVisit visit = plannedVisits.get(index);
            LocationId location = visit.location();

            long arrivalSec = arriveAt(index, location);

            TimeWindowFit service = fitService(workWindows, visit.side(), arrivalSec);
            if (service instanceof TimeWindowFit.Miss miss) {
                return infeasible(miss.violation(), visit.nodeId());
            }
            long serviceStartSec = ((TimeWindowFit.Hit) service).timeSec();
            long serviceEndSec = Math.addExact(serviceStartSec, visit.serviceTimeSec());
            if (serviceStartSec > visit.side().reqDateSec()) {
                return infeasible(Violation.REQ_DATE, visit.nodeId());
            }

            Optional<PropagationResult.Infeasible> overCapacity = updateLoad(visit);
            if (overCapacity.isPresent()) {
                return overCapacity;
            }
            Optional<String> zone = visit.side().zoneId();          // ALL·부재는 정규화가 비웠다 (Domain §3.4)
            if (zone.isPresent()) {
                if (routeZone != null && !routeZone.equals(zone.get())) {
                    return infeasible(Violation.ZONE_MIX, visit.nodeId());
                }
                routeZone = zone.get();
            }

            OptionalLong departure = departFrom(index, location, serviceEndSec);
            if (departure.isEmpty()) {
                return infeasible(Violation.WORK_WINDOW, visit.nodeId());
            }
            long departureSec = departure.getAsLong();

            customerWaitingSec = Math.addExact(
                    customerWaitingSec,
                    Math.addExact(
                            overlapWithWindows(arrivalSec, serviceStartSec, workWindows),
                            overlapWithWindows(serviceEndSec, departureSec, workWindows)));
            serviceTimeSec = Math.addExact(serviceTimeSec, visit.serviceTimeSec());
            if (previousStopLocation == null || !location.equals(previousStopLocation)) {
                stopCount++;
            }
            previousStopLocation = location;

            visitFacts.add(new VisitFacts(
                    visit.nodeId(),
                    visit.requestId(),
                    visit.pickup(),
                    location,
                    arrivalSec,
                    serviceStartSec,
                    serviceEndSec,
                    departureSec,
                    serviceStartSec - arrivalSec,
                    loadWeight,
                    loadVolume));

            previousDepartureSec = departureSec;
            previousLocation = location;
            return Optional.empty();
        }

        /**
         * [절차 2 - 1단계] 방문지 도착 시각 계산 및 이동 거리·운전 시간 누적.
         * (동일 장소 연속 방문의 경우 self arc로 거리 0, 이동시간 0 적용)
         */
        private long arriveAt(int index, LocationId location) {
            if (index == 0 && vehicle.startDepot().isEmpty()) {
                return spanStartSec;                    // 출발 차고가 없으면 근무 시작 시각에 바로 첫 방문지에 도착한 것으로 처리
            }
            long legTravelSec = travel.timeSec(previousLocation, location, speedKmH);
            if (index > 0) {
                // 첫 방문지의 앞 구간(startDepot -> 첫 방문지)은 departFromStartDepot()에서 이미 가산됨
                driveDistMeter = Math.addExact(driveDistMeter, travel.distanceMeter(previousLocation, location));
                driveTimeSec = Math.addExact(driveTimeSec, legTravelSec);
            }
            return Math.addExact(previousDepartureSec, legTravelSec);
        }

        /**
         * [절차 2 - 5~6단계] 방문지 처리 후 적재량 갱신 및 차량 용량(무게·부피) 제약 검증.
         * (픽업 노드는 적재량 증가, 배송 노드는 적재량 감소. 초과 시 해당 방문 노드를 위반 위치로 보고)
         */
        private Optional<PropagationResult.Infeasible> updateLoad(PlannedVisit visit) {
            Request request = problem.request(visit.requestId());
            if (visit.pickup()) {
                loadWeight = Math.addExact(loadWeight, request.totalWeight());
                loadVolume = Math.addExact(loadVolume, request.totalVolume());
            } else {
                loadWeight = Math.subtractExact(loadWeight, request.totalWeight());
                loadVolume = Math.subtractExact(loadVolume, request.totalVolume());
            }
            if (loadWeight < 0 || loadWeight > vehicle.maxWeight()) {
                return infeasible(Violation.CAPACITY_WEIGHT, visit.nodeId());
            }
            if (loadVolume < 0 || loadVolume > vehicle.maxVolume()) {
                return infeasible(Violation.CAPACITY_VOLUME, visit.nodeId());
            }
            return Optional.empty();
        }

        /**
         * [절차 2 - 7단계] 다음 목적지(다음 방문지 또는 도착 차고)로 이동 가능한 가장 이른 출발 시각 산출.
         *
         * <p>다음 구간 이동 소요 시간 전체가 단일 근무창 내에 온전히 포함될 수 있는 시각으로 출발 시각을 정하며,
         * 현재 근무창 내에 이동을 마칠 수 없다면 다음 근무창 시작 시각으로 출발을 미룹니다.</p>
         * (마지막 방문지이고 도착 차고가 없는 경우, 서비스 종료 시각이 곧 경로 종료 시각이 됨)
         */
        private OptionalLong departFrom(int index, LocationId location, long serviceEndSec) {
            boolean isLastVisit = index == plannedVisits.size() - 1;
            if (isLastVisit && vehicle.endDepot().isEmpty()) {
                return OptionalLong.of(serviceEndSec);
            }
            LocationId nextLocation = isLastVisit
                    ? vehicle.endDepot().orElseThrow()
                    : plannedVisits.get(index + 1).location();
            long nextLegTravelSec = travel.timeSec(location, nextLocation, speedKmH);
            return fitArc(workWindows, serviceEndSec, nextLegTravelSec);
        }

        /**
         * [절차 3] 도착 차고(endDepot) 복귀 처리: 도착 시각, 차고 시간창 검증, PICKUP_ONLY 일괄 하차.
         *
         * <ul>
         *   <li>도착 차고 도착 시각이 차고 운영 시간창에 포함되지 않으면 DEPOT_WINDOW 위반 (차고 밖 대기 후 진입 불가).</li>
         *   <li>수거 전용 주문(PICKUP_ONLY): 중간 하차가 없었으므로 도착 차고에서 일괄 하차(-수요) 처리합니다.</li>
         *   <li>최종 적재량이 0 이상 용량 이하인지 최종 검증합니다.</li>
         * </ul>
         */
        private Optional<PropagationResult.Infeasible> arriveAtEndDepot() {
            if (vehicle.endDepot().isEmpty()) {
                return Optional.empty();
            }
            LocationId endDepot = vehicle.endDepot().get();
            long lastLegTravelSec = travel.timeSec(previousLocation, endDepot, speedKmH);
            long arrivalSec = Math.addExact(previousDepartureSec, lastLegTravelSec);
            driveDistMeter = Math.addExact(driveDistMeter, travel.distanceMeter(previousLocation, endDepot));
            driveTimeSec = Math.addExact(driveTimeSec, lastLegTravelSec);

            // 기다렸다 들어가는 것으로 미루지 않는다 (Domain §7.1 절차 8).
            if (!isInsideAnyWindow(arrivalSec, problem.depotAt(endDepot).windows())) {
                return infeasible(Violation.DEPOT_WINDOW);
            }

            for (PlannedVisit visit : plannedVisits) {
                Request request = problem.request(visit.requestId());
                if (request.pattern() == ServicePattern.PICKUP_ONLY) {
                    loadWeight = Math.subtractExact(loadWeight, request.totalWeight());
                    loadVolume = Math.subtractExact(loadVolume, request.totalVolume());
                }
            }
            if (loadWeight < 0 || loadWeight > vehicle.maxWeight()) {
                return infeasible(Violation.CAPACITY_WEIGHT);
            }
            if (loadVolume < 0 || loadVolume > vehicle.maxVolume()) {
                return infeasible(Violation.CAPACITY_VOLUME);
            }
            endDepotArrivalSec = Optional.of(arrivalSec);
            return Optional.empty();
        }

        /**
         * [절차 5] 경로 수준 누적 한도(최대 정차 수, 최대 운전 시간, 최대 운전 거리) 검증.
         * (제약 조건이 설정된 축만 검사하며, 위반 시 경로 전체 위반이므로 at 위치는 비어 있음)
         */
        private Optional<PropagationResult.Infeasible> checkRouteLimits() {
            if (vehicle.effectiveMaxStopCount().isPresent()
                    && stopCount > vehicle.effectiveMaxStopCount().getAsInt()) {
                return infeasible(Violation.MAX_STOP_COUNT);
            }
            if (vehicle.maxDriveTimeSec().isPresent() && driveTimeSec > vehicle.maxDriveTimeSec().getAsLong()) {
                return infeasible(Violation.MAX_DRIVE_TIME);
            }
            if (vehicle.maxDriveDistMeter().isPresent() && driveDistMeter > vehicle.maxDriveDistMeter().getAsLong()) {
                return infeasible(Violation.MAX_DRIVE_DIST);
            }
            return Optional.empty();
        }

        /**
         * [절차 4·6] 근무창 사이의 휴식 시간(interWorkWindowRestSec)을 계산하고 최종 운행 사실(RouteFacts) 생성.
         *
         * <p>휴식 시간은 [spanStartSec, routeEndSec] 전체 구간 중 근무창에 포함되지 않는 야간/공백 시간의 총합입니다.</p>
         */
        private RouteFacts assembleFacts() {
            long routeEndSec = endDepotArrivalSec.orElseGet(() -> visitFacts.getLast().serviceEndSec());
            long interWorkWindowRestSec = Math.subtractExact(
                    Math.subtractExact(routeEndSec, spanStartSec),
                    overlapWithWindows(spanStartSec, routeEndSec, workWindows));
            return new RouteFacts(
                    vehicleId,
                    spanStartSec,
                    depotDepartureSec,
                    initialLoadWeight,
                    initialLoadVolume,
                    visitFacts,
                    endDepotArrivalSec,
                    driveDistMeter,
                    driveTimeSec,
                    customerWaitingSec,
                    depotWaitingSec,
                    serviceTimeSec,
                    interWorkWindowRestSec,
                    stopCount);
        }

        private Optional<PropagationResult.Infeasible> infeasible(Violation violation) {
            return Optional.of(new PropagationResult.Infeasible(violation, Optional.empty()));
        }

        private Optional<PropagationResult.Infeasible> infeasible(Violation violation, NodeId at) {
            return Optional.of(new PropagationResult.Infeasible(violation, Optional.of(at)));
        }
    }

    /**
     * 서비스 작업이 차량 근무창과 고객 시간창을 모두 만족하며 실행될 수 있는 가장 이른 시각을 탐색.
     *
     * <ul>
     *   <li>서비스 소요 시간 전체([t, t + serviceTime])가 하나의 근무창 안에 온전히 들어가야 합니다.</li>
     *   <li>서비스 시작 시각(t)은 고객 방문지 시간창의 [open, close] 범위 내에 있어야 합니다.</li>
     *   <li>당일 근무창 내에 서비스 완료가 불가능하면 다음 근무창으로 미룹니다.</li>
     *   <li>창이 소진될 경우: 고객 창이 먼저 소진되면 TIME_WINDOW, 근무창이 먼저 소진되면 WORK_WINDOW 위반.
     *       (두 창 축이 동시에 소진되면 정의상 고객 시간창 위반인 TIME_WINDOW로 귀속)</li>
     * </ul>
     */
    private static TimeWindowFit fitService(List<TimeWindow> workWindows, RequestSide side, long arrivalSec) {
        return earliestFitInBothWindows(
                workWindows,
                side.windows(),
                arrivalSec,
                side.serviceTimeSec(),
                Violation.TIME_WINDOW,
                Violation.TIME_WINDOW);
    }

    /**
     * 출발 차고에서의 첫 이동이 차량 근무창과 차고 영업창을 모두 만족하며 출발할 수 있는 가장 이른 시각을 탐색.
     *
     * <ul>
     *   <li>첫 이동 소요 시간 전체([t, t + firstLegTravel])가 하나의 근무창 안에 온전히 들어가야 합니다.</li>
     *   <li>출발 시각(t)은 차고 영업 시간창의 [open, close] 범위 내에 있어야 합니다.</li>
     *   <li>창이 소진될 경우: 차고 창이 먼저 소진되면 DEPOT_WINDOW, 근무창이 먼저 소진되면 WORK_WINDOW 위반.
     *       (두 창 축이 동시에 소진되면 정의상 근무창 위반인 WORK_WINDOW로 귀속)</li>
     * </ul>
     */
    private static TimeWindowFit fitDepotDeparture(
            List<TimeWindow> workWindows,
            List<TimeWindow> depotWindows,
            long notBeforeSec,
            long firstLegTravelSec) {
        return earliestFitInBothWindows(
                workWindows,
                depotWindows,
                notBeforeSec,
                firstLegTravelSec,
                Violation.DEPOT_WINDOW,
                Violation.WORK_WINDOW);
    }

    /**
     * 두 시간창 목록(근무창 목록 vs 상대 축 창 목록)을 순차 탐색하여,
     * 구간 작업 [t, t + duration]이 단일 근무창에 온전히 포함되고 t가 상대 축 시간창 내에 위치하는
     * 가장 이른 t (≥ notBeforeSec)를 찾습니다.
     *
     * @param workWindows 차량 근무창 목록
     * @param siteWindows 상대 축(방문지 고객 창 또는 차고 영업창) 시간창 목록
     * @param notBeforeSec 탐색 시작 최소 시각
     * @param duration 구간 소요 시간(서비스 시간 또는 이동 시간)
     * @param siteExhausted 상대 축 창이 먼저 소진되었을 때 반환할 위반 종류
     * @param bothExhausted 두 창 축이 동시에 소진되었을 때 반환할 위반 종류
     */
    private static TimeWindowFit earliestFitInBothWindows(
            List<TimeWindow> workWindows,
            List<TimeWindow> siteWindows,
            long notBeforeSec,
            long durationSec,
            Violation siteExhausted,
            Violation bothExhausted) {
        int workIndex = 0;
        int siteIndex = 0;
        while (workIndex < workWindows.size() && siteIndex < siteWindows.size()) {
            TimeWindow workWindow = workWindows.get(workIndex);
            TimeWindow siteWindow = siteWindows.get(siteIndex);
            long candidateSec = Math.max(notBeforeSec, Math.max(workWindow.openSec(), siteWindow.openSec()));
            boolean workMiss = !fitsWholly(workWindow, candidateSec, durationSec);
            boolean siteMiss = candidateSec > siteWindow.closeSec();
            if (!workMiss && !siteMiss) {
                return new TimeWindowFit.Hit(candidateSec);
            }
            if (workMiss) {
                workIndex++;
            }
            if (siteMiss) {
                siteIndex++;
            }
        }
        boolean workAxisExhausted = workIndex >= workWindows.size();
        boolean siteAxisExhausted = siteIndex >= siteWindows.size();
        if (workAxisExhausted && siteAxisExhausted) {
            return new TimeWindowFit.Miss(bothExhausted);
        }
        return new TimeWindowFit.Miss(workAxisExhausted ? Violation.WORK_WINDOW : siteExhausted);
    }

    /**
     * 차고 대기 지연 정책(waitInDepot = Y):
     * 근무창, 차고 영업창, 첫 이동 가능 조건을 모두 만족하면서 첫 방문지 서비스 시작 가능 시각(firstServiceStartSec)에
     * 딱 맞춰 도착할 수 있는 "가장 늦은 차고 출발 시각"을 계산합니다.
     */
    private static long latestDepotDeparture(
            List<TimeWindow> workWindows,
            List<TimeWindow> depotWindows,
            long notBeforeSec,
            long firstLegTravelSec,
            long firstServiceStartSec) {
        long latestFound = Long.MIN_VALUE;
        boolean found = false;
        for (TimeWindow workWindow : workWindows) {
            for (TimeWindow depotWindow : depotWindows) {
                long serviceCapSec = subtractBounded(firstServiceStartSec, firstLegTravelSec);
                long workCapSec = subtractBounded(workWindow.closeSec(), firstLegTravelSec);
                long latestSec = Math.min(Math.min(serviceCapSec, workCapSec), depotWindow.closeSec());
                long earliestSec = Math.max(notBeforeSec, Math.max(workWindow.openSec(), depotWindow.openSec()));
                if (latestSec >= earliestSec
                        && fitsWholly(workWindow, latestSec, firstLegTravelSec)
                        && latestSec <= depotWindow.closeSec()) {
                    found = true;
                    latestFound = Math.max(latestFound, latestSec);
                }
            }
        }
        if (!found) {
            throw new IllegalStateException("waitInDepot latest departure empty");
        }
        return latestFound;
    }

    /**
     * 이동 구간 전체([t, t + duration])가 단일 근무창 내에 온전히 포함될 수 있는 가장 이른 시각 t (≥ from)을 탐색.
     *
     * <p>이동 도중 근무창이 종료되면 운전을 도중에 끊지 않고, 다음 근무창 시작 시각으로 이동 전체를 통째로 미룹니다.</p>
     *
     * @param windows 차량 근무창 목록
     * @param from 출발 가능 기준 시각
     * @param duration 이동 소요 시간(초)
     * @return 유효한 출발 시각(OptionalLong), 만족하는 근무창이 없으면 OptionalLong.empty()
     */
    static OptionalLong fitArc(List<TimeWindow> windows, long from, long duration) {
        for (TimeWindow window : windows) {
            long startSec = Math.max(from, window.openSec());
            if (fitsWholly(window, startSec, duration)) {
                return OptionalLong.of(startSec);
            }
        }
        return OptionalLong.empty();
    }

    /**
     * 지정된 시간 구간 [from, to] 중 주어진 시간창 목록과 겹치는 실제 유효 시간(초)의 합계를 계산.
     * (시간 측정 규약: 모든 소요 시간은 양 끝 시각의 차이(to - from)로 계산하여 1초 오차 방지)
     */
    static long overlapWithWindows(long from, long to, List<TimeWindow> windows) {
        if (from >= to) {
            return 0L;
        }
        long total = 0L;
        for (TimeWindow window : windows) {
            long overlapStartSec = Math.max(from, window.openSec());
            long overlapEndSec = Math.min(to, window.closeSec());
            if (overlapEndSec > overlapStartSec) {
                total = Math.addExact(total, overlapEndSec - overlapStartSec);
            }
        }
        return total;
    }

    /** 주어진 시각이 시간창 목록 중 어느 하나라도 안에 포함되는지 검사. */
    private static boolean isInsideAnyWindow(long timeSec, List<TimeWindow> windows) {
        for (TimeWindow window : windows) {
            if (timeSec >= window.openSec() && timeSec <= window.closeSec()) {
                return true;
            }
        }
        return false;
    }

    /** [startSec, startSec + durationSec] 구간 전체가 단일 시간창 내에 온전히 들어가는지 검사 (양 끝점 포함). */
    private static boolean fitsWholly(TimeWindow window, long startSec, long durationSec) {
        if (startSec > window.closeSec()) {
            return false;
        }
        try {
            return Math.addExact(startSec, durationSec) <= window.closeSec();
        } catch (ArithmeticException ex) {
            return false;
        }
    }

    /** 두 값의 차이를 계산하되, 오버플로 발생 시 최솟값(Long.MIN_VALUE)을 반환하여 해당 후보를 탈락시킴. */
    private static long subtractBounded(long minuend, long subtrahend) {
        try {
            return Math.subtractExact(minuend, subtrahend);
        } catch (ArithmeticException ex) {
            return Long.MIN_VALUE;
        }
    }

    /** 방문 노드(NodeId)와 해당 노드가 참조하는 문제 데이터(NodeRef)를 결합한 불변 레코드. */
    private record PlannedVisit(NodeId nodeId, NodeRef ref) {
        RequestId requestId() {
            return ref.requestId();
        }

        boolean pickup() {
            return ref.pickup();
        }

        RequestSide side() {
            return ref.side();
        }

        LocationId location() {
            return ref.side().locationId();
        }

        long serviceTimeSec() {
            return ref.side().serviceTimeSec();
        }
    }

    /** 시간창 매칭 시도 결과: 성공 시 확정 시각({@link Hit}), 실패 시 소진된 축에 따른 위반 사유({@link Miss}). */
    private sealed interface TimeWindowFit {
        record Hit(long timeSec) implements TimeWindowFit {}
        record Miss(Violation violation) implements TimeWindowFit {}
    }
}
