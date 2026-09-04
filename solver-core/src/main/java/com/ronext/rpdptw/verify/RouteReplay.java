package com.ronext.rpdptw.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.Request;
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
import com.ronext.rpdptw.verify.VerifyViolation.Kind;

/**
 * 경로 하나를 Domain §7.1 절차대로 처음부터 다시 계산하는 **두 번째 구현** (stage-05 §2.3).
 *
 * <p>solve의 전파 코드를 부르지 않고, 창을 걷는 코드도 여기서 따로 구현한다 — 같은 코드를 부르면
 * 같은 버그가 양쪽에 숨어 재검증이 아무것도 검증하지 못한다 (Domain §10.1, stage-05 §5 N1).
 * 공유하는 것은 {@link Problem}의 동결 사실(이동표·정규화된 창 목록)과 core {@code eval}의 값
 * 타입뿐이다.</p>
 */
final class RouteReplay {

    private RouteReplay() {}

    sealed interface Outcome {
        record Ok(RouteFacts facts) implements Outcome {}

        record Violated(VerifyViolation violation) implements Outcome {}
    }

    static Outcome replay(Problem problem, VehicleId vehicleId, List<NodeId> visits) {
        Vehicle vehicle = problem.vehicle(vehicleId);
        TravelMatrix travel = problem.travel();
        int speedKmH = problem.resolvedSpeedKmH(vehicleId);
        List<TimeWindow> work = vehicle.workWindows();

        List<NodeRef> refs = new ArrayList<>(visits.size());
        for (NodeId nodeId : visits) {
            refs.add(problem.nodeRef(nodeId).orElseThrow());
        }

        // 절차 0 — 출발 적재: 그 경로에 배정된 DELIVERY_ONLY 수요 (Domain §6.2 부호 규칙).
        // DELIVERY_ONLY는 방문이 하나(하차)뿐이라 방문 단위로 세도 중복이 없다.
        long initialWeight = 0L;
        long initialVolume = 0L;
        for (NodeRef ref : refs) {
            Request request = problem.request(ref.requestId());
            if (request.pattern() == ServicePattern.DELIVERY_ONLY) {
                initialWeight = Math.addExact(initialWeight, request.totalWeight());
                initialVolume = Math.addExact(initialVolume, request.totalVolume());
            }
        }
        if (initialWeight > vehicle.maxWeight()) {
            return route(vehicleId, Kind.CAPACITY_WEIGHT,
                    "initialLoadWeight=" + initialWeight + ", maxWeight=" + vehicle.maxWeight());
        }
        if (initialVolume > vehicle.maxVolume()) {
            return route(vehicleId, Kind.CAPACITY_VOLUME,
                    "initialLoadVolume=" + initialVolume + ", maxVolume=" + vehicle.maxVolume());
        }
        if (work.isEmpty()) {
            return route(vehicleId, Kind.WORK_WINDOW, "no work window in plan span");
        }
        long spanStartSec = work.get(0).openSec();

        long loadWeight = initialWeight;
        long loadVolume = initialVolume;
        long driveDistMeter = 0L;
        long driveTimeSec = 0L;
        long depotWaitingSec = 0L;
        long customerWaitingSec = 0L;
        long serviceTotalSec = 0L;
        int stopCount = 0;

        LocationId firstLocation = refs.get(0).side().locationId();
        Optional<Long> depotDepartureSec = Optional.empty();
        LocationId previousLocation = null;
        long previousDepartureSec = spanStartSec;

        if (vehicle.startDepot().isPresent()) {
            LocationId startDepot = vehicle.startDepot().get();
            List<TimeWindow> depotWindows = problem.depotAt(startDepot).windows();
            if (depotWindows.isEmpty()) {
                return route(vehicleId, Kind.DEPOT_WINDOW, "no startDepot window in plan span");
            }
            long firstLegSec = travel.timeSec(startDepot, firstLocation, speedKmH);
            // 절차 0: 근무창 안 · 차고 창 안 · 첫 이동이 그 근무창에 통째로. 동시 소진은 WORK_WINDOW.
            Fit fit = earliestJoint(work, depotWindows, spanStartSec, firstLegSec,
                    Kind.DEPOT_WINDOW, Kind.WORK_WINDOW);
            if (fit instanceof Fit.None none) {
                return route(vehicleId, none.kind(), "cannot depart startDepot " + startDepot.value());
            }
            long departureSec = ((Fit.At) fit).timeSec();

            if (problem.deliveryPolicy().waitInDepot()) {
                // Y는 늦추기만 한다 — 기준이 되는 첫 방문 serviceStart는 N 출발로 먼저 구한다 (Domain §7.1).
                long arrivalSec = Math.addExact(departureSec, firstLegSec);
                Fit service = earliestJoint(work, refs.get(0).side().windows(), arrivalSec,
                        refs.get(0).side().serviceTimeSec(), Kind.TIME_WINDOW, Kind.TIME_WINDOW);
                if (service instanceof Fit.None none) {
                    return visit(vehicleId, none.kind(), refs.get(0),
                            "no service slot at " + refs.get(0).side().nodeId().value());
                }
                departureSec = latestDeparture(work, depotWindows, spanStartSec, firstLegSec,
                        ((Fit.At) service).timeSec(), departureSec);
            }

            depotDepartureSec = Optional.of(departureSec);
            depotWaitingSec = workingSpan(spanStartSec, departureSec, work);
            previousDepartureSec = departureSec;
            previousLocation = startDepot;
            driveDistMeter = travel.distanceMeter(startDepot, firstLocation);
            driveTimeSec = firstLegSec;
        }

        List<VisitFacts> visitFacts = new ArrayList<>(refs.size());
        LocationId previousStopLocation = null;
        String routeZone = null;

        for (int index = 0; index < refs.size(); index++) {
            NodeRef ref = refs.get(index);
            RequestSide side = ref.side();
            LocationId location = side.locationId();

            // 절차 1 — 도착. startDepot이 없으면 첫 방문은 근무 시작에 이미 와 있다 (절차 0').
            long arrivalSec;
            if (previousLocation == null) {
                arrivalSec = spanStartSec;
            } else {
                long legSec = travel.timeSec(previousLocation, location, speedKmH);
                if (index > 0) {
                    driveDistMeter = Math.addExact(
                            driveDistMeter, travel.distanceMeter(previousLocation, location));
                    driveTimeSec = Math.addExact(driveTimeSec, legSec);
                }
                arrivalSec = Math.addExact(previousDepartureSec, legSec);
            }

            // 절차 2 — 서비스 시작. 시간창 안이면서 서비스 전체가 한 근무창 안. 동시 소진은 TIME_WINDOW.
            Fit fit = earliestJoint(work, side.windows(), arrivalSec, side.serviceTimeSec(),
                    Kind.TIME_WINDOW, Kind.TIME_WINDOW);
            if (fit instanceof Fit.None none) {
                return visit(vehicleId, none.kind(), ref, "no service slot at " + side.nodeId().value());
            }
            long serviceStartSec = ((Fit.At) fit).timeSec();
            long serviceEndSec = Math.addExact(serviceStartSec, side.serviceTimeSec());

            // 절차 4 — reqDate는 serviceStart로만 본다 (serviceEnd는 조건이 아니다).
            if (serviceStartSec > side.reqDateSec()) {
                return visit(vehicleId, Kind.REQ_DATE, ref,
                        "serviceStart=" + serviceStartSec + ", reqDate=" + side.reqDateSec());
            }

            // 절차 5·6 — 적재 곡선과 용량, 그리고 구역 단일성.
            Request request = problem.request(ref.requestId());
            long signedWeight = ref.pickup() ? request.totalWeight() : -request.totalWeight();
            long signedVolume = ref.pickup() ? request.totalVolume() : -request.totalVolume();
            loadWeight = Math.addExact(loadWeight, signedWeight);
            loadVolume = Math.addExact(loadVolume, signedVolume);
            if (loadWeight < 0 || loadWeight > vehicle.maxWeight()) {
                return visit(vehicleId, Kind.CAPACITY_WEIGHT, ref,
                        "loadWeight=" + loadWeight + ", maxWeight=" + vehicle.maxWeight());
            }
            if (loadVolume < 0 || loadVolume > vehicle.maxVolume()) {
                return visit(vehicleId, Kind.CAPACITY_VOLUME, ref,
                        "loadVolume=" + loadVolume + ", maxVolume=" + vehicle.maxVolume());
            }
            if (side.zoneId().isPresent()) {                  // ALL·부재는 정규화가 비웠다 (Domain §3.4)
                String zone = side.zoneId().get();
                if (routeZone != null && !routeZone.equals(zone)) {
                    return visit(vehicleId, Kind.ZONE_MIX, ref, "routeZone=" + routeZone + ", visitZone=" + zone);
                }
                routeZone = zone;
            }

            // 절차 7 — 다음 이동을 통째로 담는 가장 이른 출발. 뒤에 이동이 없으면 serviceEnd 그대로.
            LocationId nextLocation = nextLocation(vehicle, refs, index);
            long departureSec;
            if (nextLocation == null) {
                departureSec = serviceEndSec;
            } else {
                OptionalLong depart = earliestWholly(
                        work, serviceEndSec, travel.timeSec(location, nextLocation, speedKmH));
                if (depart.isEmpty()) {
                    return visit(vehicleId, Kind.WORK_WINDOW, ref,
                            "no work window holds the leg after " + side.nodeId().value());
                }
                departureSec = depart.getAsLong();
            }

            customerWaitingSec = Math.addExact(
                    customerWaitingSec,
                    Math.addExact(
                            workingSpan(arrivalSec, serviceStartSec, work),
                            workingSpan(serviceEndSec, departureSec, work)));
            serviceTotalSec = Math.addExact(serviceTotalSec, side.serviceTimeSec());
            if (!location.equals(previousStopLocation)) {
                stopCount++;
            }
            previousStopLocation = location;

            visitFacts.add(new VisitFacts(
                    side.nodeId(),
                    ref.requestId(),
                    ref.pickup(),
                    location,
                    arrivalSec,
                    serviceStartSec,
                    serviceEndSec,
                    departureSec,
                    serviceStartSec - arrivalSec,
                    loadWeight,
                    loadVolume));

            previousLocation = location;
            previousDepartureSec = departureSec;
        }

        // 절차 8 — 종료. endDepot 도착 순간이 차고 창 밖이면 미루지 않고 DEPOT_WINDOW.
        Optional<Long> endDepotArrivalSec = Optional.empty();
        if (vehicle.endDepot().isPresent()) {
            LocationId endDepot = vehicle.endDepot().get();
            long lastLegSec = travel.timeSec(previousLocation, endDepot, speedKmH);
            long arrivalSec = Math.addExact(previousDepartureSec, lastLegSec);
            driveDistMeter = Math.addExact(driveDistMeter, travel.distanceMeter(previousLocation, endDepot));
            driveTimeSec = Math.addExact(driveTimeSec, lastLegSec);
            if (!insideAny(arrivalSec, problem.depotAt(endDepot).windows())) {
                return route(vehicleId, Kind.DEPOT_WINDOW, "endDepot arrival=" + arrivalSec + " outside windows");
            }
            for (NodeRef ref : refs) {                        // PICKUP_ONLY만 도착 사건에서 하차
                Request request = problem.request(ref.requestId());
                if (request.pattern() == ServicePattern.PICKUP_ONLY) {
                    loadWeight = Math.subtractExact(loadWeight, request.totalWeight());
                    loadVolume = Math.subtractExact(loadVolume, request.totalVolume());
                }
            }
            if (loadWeight < 0 || loadWeight > vehicle.maxWeight()) {
                return route(vehicleId, Kind.CAPACITY_WEIGHT,
                        "loadWeight=" + loadWeight + ", maxWeight=" + vehicle.maxWeight());
            }
            if (loadVolume < 0 || loadVolume > vehicle.maxVolume()) {
                return route(vehicleId, Kind.CAPACITY_VOLUME,
                        "loadVolume=" + loadVolume + ", maxVolume=" + vehicle.maxVolume());
            }
            endDepotArrivalSec = Optional.of(arrivalSec);
        }
        // pair 완비 구조에서는 산술적으로 0이다 — replay 자체 버그·모델 변화 방어 (stage-05 E10).
        if (loadWeight != 0L || loadVolume != 0L) {
            return route(vehicleId, Kind.END_LOAD_NOT_ZERO,
                    "endLoadWeight=" + loadWeight + ", endLoadVolume=" + loadVolume);
        }

        if (vehicle.effectiveMaxStopCount().isPresent()
                && stopCount > vehicle.effectiveMaxStopCount().getAsInt()) {
            return route(vehicleId, Kind.MAX_STOP_COUNT,
                    "stopCount=" + stopCount + ", max=" + vehicle.effectiveMaxStopCount().getAsInt());
        }
        if (vehicle.maxDriveTimeSec().isPresent() && driveTimeSec > vehicle.maxDriveTimeSec().getAsLong()) {
            return route(vehicleId, Kind.MAX_DRIVE_TIME,
                    "driveTimeSec=" + driveTimeSec + ", max=" + vehicle.maxDriveTimeSec().getAsLong());
        }
        if (vehicle.maxDriveDistMeter().isPresent() && driveDistMeter > vehicle.maxDriveDistMeter().getAsLong()) {
            return route(vehicleId, Kind.MAX_DRIVE_DIST,
                    "driveDistMeter=" + driveDistMeter + ", max=" + vehicle.maxDriveDistMeter().getAsLong());
        }

        long routeEndSec = endDepotArrivalSec.orElseGet(() -> visitFacts.get(visitFacts.size() - 1).serviceEndSec());
        long restSec = Math.subtractExact(
                Math.subtractExact(routeEndSec, spanStartSec),
                workingSpan(spanStartSec, routeEndSec, work));
        return new Outcome.Ok(new RouteFacts(
                vehicleId,
                spanStartSec,
                depotDepartureSec,
                initialWeight,
                initialVolume,
                visitFacts,
                endDepotArrivalSec,
                driveDistMeter,
                driveTimeSec,
                customerWaitingSec,
                depotWaitingSec,
                serviceTotalSec,
                restSec,
                stopCount));
    }

    /** 이 방문 뒤에 이동이 있으면 그 도착지, 없으면 null (마지막 방문 + endDepot 부재). */
    private static LocationId nextLocation(Vehicle vehicle, List<NodeRef> refs, int index) {
        if (index + 1 < refs.size()) {
            return refs.get(index + 1).side().locationId();
        }
        return vehicle.endDepot().orElse(null);
    }

    /**
     * 두 창 축이 함께 만족되는 가장 이른 시각: {@code t}가 site 창 안이면서
     * {@code [t, t + duration]}이 한 work 창 안 (Domain §7.1 절차 0·2).
     *
     * <p>정규화가 창을 정렬·병합해 두었으므로(Domain §3.2) 창 쌍마다 후보는
     * {@code max(notBefore, work.open, site.open)} 하나뿐이다 — 그보다 늦은 시각은 두 조건을
     * 더 어렵게만 만든다. 따라서 쌍을 전부 훑어 최소 후보를 고르면 그것이 가장 이른 시각이다.</p>
     *
     * <p>귀속: 한 축만 막았으면 그 축, 두 축이 함께 소진되면 {@code tie}
     * (절차 0은 WORK_WINDOW, 절차 2는 TIME_WINDOW — Domain §7.1 2026-08-11).
     * 각 축은 단독으로 가능한데 창이 서로 엇갈려 겹치지 않는 경우도 두 축이 함께 소진된 것으로 본다.</p>
     */
    private static Fit earliestJoint(
            List<TimeWindow> work, List<TimeWindow> site, long notBefore, long durationSec,
            Kind siteBlockedKind, Kind tie) {
        long earliest = Long.MAX_VALUE;
        for (TimeWindow workWindow : work) {
            for (TimeWindow siteWindow : site) {
                long candidate = Math.max(notBefore, Math.max(workWindow.openSec(), siteWindow.openSec()));
                if (candidate <= siteWindow.closeSec() && holds(workWindow, candidate, durationSec)) {
                    earliest = Math.min(earliest, candidate);
                }
            }
        }
        if (earliest != Long.MAX_VALUE) {
            return new Fit.At(earliest);
        }
        boolean workBlocked = earliestWholly(work, notBefore, durationSec).isEmpty();
        boolean siteBlocked = true;
        for (TimeWindow siteWindow : site) {
            if (siteWindow.closeSec() >= notBefore) {
                siteBlocked = false;
                break;
            }
        }
        if (workBlocked && !siteBlocked) {
            return new Fit.None(Kind.WORK_WINDOW);
        }
        if (siteBlocked && !workBlocked) {
            return new Fit.None(siteBlockedKind);
        }
        return new Fit.None(tie);
    }

    /** 이동을 통째로 담는 가장 이른 출발 시각 (Domain §7.1 절차 7). 창을 넘으면 다음 창으로 미룬다. */
    private static OptionalLong earliestWholly(List<TimeWindow> windows, long notBefore, long durationSec) {
        for (TimeWindow window : windows) {
            long candidate = Math.max(notBefore, window.openSec());
            if (holds(window, candidate, durationSec)) {
                return OptionalLong.of(candidate);
            }
        }
        return OptionalLong.empty();
    }

    /**
     * waitInDepot=Y의 가장 늦은 출발 (Domain §7.1 절차 0): 세 조건을 지키면서
     * 첫 방문 serviceStart보다 늦게 도착하지 않는 시각 중 최댓값.
     * N 출발도 후보이므로 {@code notLaterThan}에서 시작하면 후보 집합이 비지 않는다.
     */
    private static long latestDeparture(
            List<TimeWindow> work, List<TimeWindow> depotWindows,
            long notBefore, long firstLegSec, long firstServiceStartSec, long earliestDepartureSec) {
        long latest = earliestDepartureSec;
        for (TimeWindow workWindow : work) {
            for (TimeWindow depotWindow : depotWindows) {
                long candidate = Math.min(
                        Math.subtractExact(firstServiceStartSec, firstLegSec),
                        Math.min(Math.subtractExact(workWindow.closeSec(), firstLegSec), depotWindow.closeSec()));
                long floor = Math.max(notBefore, Math.max(workWindow.openSec(), depotWindow.openSec()));
                if (candidate >= floor && holds(workWindow, candidate, firstLegSec)) {
                    latest = Math.max(latest, candidate);
                }
            }
        }
        return latest;
    }

    /** {@code [from, to]} 중 근무창에 걸친 시간. 모든 소요 시간은 두 시각의 차다 (Domain §3.2). */
    private static long workingSpan(long from, long to, List<TimeWindow> windows) {
        long total = 0L;
        for (TimeWindow window : windows) {
            long start = Math.max(from, window.openSec());
            long end = Math.min(to, window.closeSec());
            if (end > start) {
                total = Math.addExact(total, end - start);
            }
        }
        return total;
    }

    private static boolean insideAny(long timeSec, List<TimeWindow> windows) {
        for (TimeWindow window : windows) {
            if (window.openSec() <= timeSec && timeSec <= window.closeSec()) {
                return true;
            }
        }
        return false;
    }

    /** {@code [start, start + duration]}이 이 창 안에 통째로 들어가는가 (양끝 포함). */
    private static boolean holds(TimeWindow window, long startSec, long durationSec) {
        if (startSec < window.openSec() || startSec > window.closeSec()) {
            return false;
        }
        return durationSec <= window.closeSec() - startSec;
    }

    private static Outcome route(VehicleId vehicleId, Kind kind, String detail) {
        return new Outcome.Violated(new VerifyViolation(
                kind, Optional.of(vehicleId), Optional.empty(), Optional.empty(), detail));
    }

    private static Outcome visit(VehicleId vehicleId, Kind kind, NodeRef ref, String detail) {
        return new Outcome.Violated(new VerifyViolation(
                kind,
                Optional.of(vehicleId),
                Optional.of(ref.requestId()),
                Optional.of(ref.side().nodeId()),
                detail));
    }

    private sealed interface Fit {
        record At(long timeSec) implements Fit {}

        record None(Kind kind) implements Fit {}
    }
}
