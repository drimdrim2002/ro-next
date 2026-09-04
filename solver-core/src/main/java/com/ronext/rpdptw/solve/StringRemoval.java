package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.problem.Problem;

/**
 * seed 근처 경로들에서 연속 구간(한 경로당 방문 10개 이하)을 잘라 q개까지 제거 (stage-04 §4.4, 2026-09-04).
 * 도로를 따라 이어진 토막을 통째로 빼고 다시 잇는다 — 비삼각 이동표에서 실제 개선으로 이어지는 형태다.
 */
public final class StringRemoval implements DestroyOperator {

    static final String ID = "string-removal";

    /** 한 경로에서 자르는 연속 구간의 최대 길이 — 방문 수로 센다 (§4.4). */
    static final int MAX_STRING = 10;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng) {
        List<RequestId> assigned = RandomRemoval.assignedRequestIds(problem, current);
        if (assigned.isEmpty()) {
            return current;
        }
        RequestId seed = assigned.get(rng.nextInt(assigned.size()));        // 정렬된 목록에 rng (N3)
        LocationId seedAt = InsertionSearch.anchor(problem.request(seed)).locationId();

        // 경로를 "seed와 최근접 방문의 이동표 거리" 오름차순으로 — 동률은 VehicleId 문자열 순 (N3)
        List<Nearest> byDistance = new ArrayList<>(current.routes().size());
        for (Route route : current.routes()) {
            byDistance.add(nearest(problem, seedAt, route));
        }
        byDistance.sort(Comparator.comparingLong(Nearest::distanceMeter)
                .thenComparing(near -> near.route().vehicleId().value()));

        Set<RequestId> picked = new LinkedHashSet<>();
        for (Nearest near : byDistance) {
            if (picked.size() >= removeCount) {
                break;
            }
            List<NodeId> visits = near.route().visits();
            int len = 1 + rng.nextInt(Math.min(MAX_STRING, removeCount - picked.size()));
            int start = Math.max(0, Math.min(near.index() - len / 2, visits.size() - len));   // 최근접 방문을 중심으로
            int end = Math.min(visits.size(), start + len);
            for (int i = start; i < end && picked.size() < removeCount; i++) {
                picked.add(InsertionSearch.requestOf(problem, visits.get(i)));
            }
        }
        Solution result = current;
        for (RequestId requestId : picked) {
            result = InsertionSearch.remove(problem, result, requestId);   // pair 전체 + 빈 경로 제거 (E4)
        }
        return result;
    }

    /** 그 경로에서 seed와 가장 가까운 방문 — 동률은 앞선 방문. */
    private static Nearest nearest(Problem problem, LocationId seedAt, Route route) {
        int index = 0;
        long best = Long.MAX_VALUE;
        for (int i = 0; i < route.visits().size(); i++) {
            LocationId at = problem.nodeRef(route.visits().get(i)).orElseThrow().side().locationId();
            long distance = problem.travel().distanceMeter(seedAt, at);
            if (distance < best) {
                best = distance;
                index = i;
            }
        }
        return new Nearest(route, index, best);
    }

    private record Nearest(Route route, int index, long distanceMeter) {}
}
