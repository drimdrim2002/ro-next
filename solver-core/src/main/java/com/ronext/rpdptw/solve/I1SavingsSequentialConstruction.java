package com.ronext.rpdptw.solve;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.VehicleId;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.problem.Problem;
import com.ronext.rpdptw.solve.InsertionSearch.Candidate;

/** H11 — 단독 경로 대비 절감 최대 순차 삽입, Solomon I1 c2 (heuristics 문서 §5 H11). */
public final class I1SavingsSequentialConstruction implements ConstructionHeuristic {

    static final String ID = "i1-savings-sequential";

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
        Solution current = InsertionSearch.emptySolution(problem);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        int bound = pending.size();
        int iterations = 0;
        Comparator<RequestId> seedOrder = Comparator
                .<RequestId>comparingLong(id -> InsertionSearch.lastCloseSec(InsertionSearch.anchor(problem.request(id))))
                .thenComparing(InsertionSearch.BY_REQUEST_ID);
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
            RequestId seed = pending.stream().min(seedOrder).orElseThrow();
            Solution base = current;
            Optional<VehicleId> vehicle = InsertionSearch.firstUnusedCompatible(problem, seed, InsertionSearch.usedVehicles(base));
            Optional<Solution> opened = vehicle.flatMap(v -> InsertionSearch.openRoute(problem, profile, base, seed, v));
            pending.remove(seed);
            if (opened.isEmpty()) {
                continue;                                                       // 규칙 (b)
            }
            current = opened.get();
            VehicleId v = vehicle.get();
            while (true) {
                RequestId bestId = null;
                Candidate best = null;
                long bestSaving = 0L;
                for (RequestId requestId : pending) {
                    List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, v);
                    if (cands.isEmpty()) {
                        continue;
                    }
                    long saving = InsertionSearch.standaloneDistMeter(problem, v, requestId) - cands.getFirst().deltaDriveDistMeter();
                    if (best == null || saving > bestSaving) {                 // pending은 RequestId 순 — 동률은 앞선 것
                        best = cands.getFirst();
                        bestId = requestId;
                        bestSaving = saving;
                    }
                }
                if (best == null) {
                    break;                                                      // 경로 확정
                }
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
                current = InsertionSearch.apply(problem, current, best);
                pending.remove(bestId);
            }
        }
        return current;
    }
}
