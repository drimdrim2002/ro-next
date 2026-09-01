package com.ronext.rpdptw.eval;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ronext.rpdptw.domain.VehicleId;

public record RouteFacts(
        VehicleId vehicleId,
        long spanStartSec,
        Optional<Long> departureSec,
        long initialLoadWeight,
        long initialLoadVolume,
        List<VisitFacts> visits,
        Optional<Long> endDepotArrivalSec,
        long driveDistMeter,
        long driveTimeSec,
        long customerWaitingTimeSec,
        long depotWaitingTimeSec,
        long serviceTimeSec,
        long interWorkWindowRestTimeSec,
        int stopCount) {

    public RouteFacts {
        Objects.requireNonNull(vehicleId, "vehicleId");
        departureSec = Objects.requireNonNull(departureSec, "departureSec");
        visits = List.copyOf(visits);
        endDepotArrivalSec = Objects.requireNonNull(endDepotArrivalSec, "endDepotArrivalSec");
    }

    /** §7.3 공식: driveTime + customerWaiting + depotWaiting + serviceTime + interWorkWindowRest */
    public long routeOperationalTimeSec() {
        return driveTimeSec
                + customerWaitingTimeSec
                + depotWaitingTimeSec
                + serviceTimeSec
                + interWorkWindowRestTimeSec;
    }

    /** §7.3 항등식 대조용. endDepotArrivalSec 또는 마지막 visit의 serviceEnd. */
    public long routeEndSec() {
        if (endDepotArrivalSec.isPresent()) {
            return endDepotArrivalSec.get();
        }
        if (visits.isEmpty()) {
            throw new IllegalStateException("no route end");
        }
        return visits.get(visits.size() - 1).serviceEndSec();
    }
}
