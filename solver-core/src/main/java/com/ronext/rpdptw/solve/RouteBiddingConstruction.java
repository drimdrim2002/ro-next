package com.ronext.rpdptw.solve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H12 — 경로 측이 요청을 고르는 입찰 라운드, Antes & Derigs (heuristics 문서 §5 H12). */
public final class RouteBiddingConstruction implements ConstructionHeuristic {

    static final String ID = "route-bidding";

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
        return construct(problem, profile, new ArrayList<>());
    }

    /** 라운드 기록용 오버로드 — 라운드마다 수락 건수를 남긴다 (T30). */
    Solution construct(Problem problem, Profile profile, List<Integer> acceptedPerRound) {
        Solution current = InsertionSearch.emptySolution(problem);
        InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        int bound = pending.size();
        int rounds = 0;
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(ID, ++rounds, bound);
            Map<VehicleId, List<Bid>> bids = new TreeMap<>(InsertionSearch.BY_VEHICLE_ID);
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {
                List<Candidate> cands = cache.candidates(current, requestId);
                if (cands.isEmpty()) {
                    excluded.add(requestId);                                   // 규칙 (b)
                    continue;
                }
                Candidate best = cands.getFirst();
                bids.computeIfAbsent(best.vehicleId(), v -> new ArrayList<>()).add(new Bid(requestId, best));
            }
            pending.removeAll(excluded);
            int accepted = 0;
            for (List<Bid> box : bids.values()) {                              // VehicleId ASC
                Bid winner = box.stream().min(BID_ORDER).orElseThrow();
                current = InsertionSearch.apply(problem, current, winner.candidate());   // 규칙 (a) — 경로당 1건
                pending.remove(winner.requestId());
                cache.forget(winner.requestId());
                accepted++;
            }
            acceptedPerRound.add(accepted);
            if (accepted == 0) {
                break;                                                          // 전원 제외였음 (X15)
            }
        }
        return current;
    }

    private static final Comparator<Bid> BID_ORDER = Comparator.comparing(Bid::candidate, Candidate.byCost())
            .thenComparing(Bid::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Bid(RequestId requestId, Candidate candidate) {}
}
