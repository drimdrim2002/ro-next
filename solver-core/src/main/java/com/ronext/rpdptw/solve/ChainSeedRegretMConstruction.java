package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;

/** H10 — 시간 연쇄 배제 seed + regret-m (heuristics 문서 §5 H10). */
public final class ChainSeedRegretMConstruction implements ConstructionHeuristic {

    static final String ID = "chain-seed-regret-m";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean abstains(Problem problem) {
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        Solution current = InsertionSearch.emptySolution(problem);
        int bound = pending.size();
        int iterations = 0;

        // 1) EPT ASC 순회 — 직전 seed 뒤에 시간상 이어 붙일 수 있으면 seed에서 제외
        List<RequestId> seeds = seeds(problem);

        // 2) 각 seed를 호환 미사용 차량(VehicleId ASC 첫)의 새 경로로 연다. 못 열면 그 seed 취소
        for (RequestId seed : seeds) {
            Optional<VehicleId> vehicle = InsertionSearch.firstUnusedCompatible(problem, seed, InsertionSearch.usedVehicles(current));
            if (vehicle.isEmpty()) {
                continue;
            }
            Optional<Solution> opened = InsertionSearch.openRoute(problem, profile, current, seed, vehicle.get());
            if (opened.isPresent()) {
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                current = opened.get();
                pending.remove(seed);
            }
        }

        // 3) 나머지는 H9의 루프 — 새 경로 후보는 기존 경로 후보가 0개인 요청에만
        return FeasibleRoutesRegretMConstruction.regretMLoop(problem, profile, current, pending, true, ID, iterations, bound);
    }

    /** seed 선정 — EPT ASC(동률 RequestId). seed 수 상한 = 차량 수. */
    static List<RequestId> seeds(Problem problem) {
        List<RequestId> order = new ArrayList<>(InsertionSearch.sortedRequestIds(problem));
        order.sort(Comparator.<RequestId>comparingLong(id -> ept(problem.request(id))).thenComparing(InsertionSearch.BY_REQUEST_ID));
        int slowest = Integer.MAX_VALUE;
        for (Vehicle vehicle : problem.vehicles()) {
            slowest = Math.min(slowest, problem.resolvedSpeedKmH(vehicle.id()));
        }
        List<RequestId> seeds = new ArrayList<>();
        Request last = null;
        for (RequestId requestId : order) {
            if (seeds.size() >= problem.vehicles().size()) {
                break;
            }
            Request request = problem.request(requestId);
            if (last != null) {
                long link = problem.travel().timeSec(
                        InsertionSearch.lastSide(last).locationId(), InsertionSearch.anchor(request).locationId(), slowest);
                if (ldt(last) + link <= ept(request)) {
                    continue;                                                   // k 뒤에 이어 붙일 수 있다 → seed 아님
                }
            }
            seeds.add(requestId);
            last = request;
        }
        return seeds;
    }

    static long ept(Request request) {
        return InsertionSearch.firstOpenSec(InsertionSearch.anchor(request));
    }

    static long ldt(Request request) {
        return InsertionSearch.lastCloseSec(InsertionSearch.lastSide(request));
    }
}
