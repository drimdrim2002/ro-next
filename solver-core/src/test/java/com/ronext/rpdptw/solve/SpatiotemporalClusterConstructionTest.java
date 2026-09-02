package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.ConstructionFixtures.ALL_DAY;
import static com.ronext.rpdptw.solve.ConstructionFixtures.DEPOT;
import static com.ronext.rpdptw.solve.ConstructionFixtures.at;
import static com.ronext.rpdptw.solve.ConstructionFixtures.delivery;
import static com.ronext.rpdptw.solve.ConstructionFixtures.freeze;
import static com.ronext.rpdptw.solve.ConstructionFixtures.locations;
import static com.ronext.rpdptw.solve.ConstructionFixtures.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.Depot;
import com.ronext.rpdptw.domain.LocationId;
import com.ronext.rpdptw.domain.Request;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.domain.TravelEntry;
import com.ronext.rpdptw.domain.Vehicle;
import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.problem.Problem;

class SpatiotemporalClusterConstructionTest {

    private static final LocationId I = new LocationId("I");
    private static final LocationId J = new LocationId("J");

    /** T36 — S(i,j)가 두 방향을 각각 계산해 최소를 취함 · f < 0인 방향 배제 · 좌표로 거리를 재계산하지 않음 (X17). */
    @Test
    void distanceUsesDirectedTravelOnly() {
        List<TravelEntry> travel = List.of(
                new TravelEntry(DEPOT, I, 500, 40), new TravelEntry(I, DEPOT, 500, 40),
                new TravelEntry(DEPOT, J, 500, 40), new TravelEntry(J, DEPOT, 500, 40),
                new TravelEntry(I, J, 1_000, 80), new TravelEntry(J, I, 9_000, 720));   // 비대칭. 좌표는 전부 같다

        // 양방향 유효 (창 종일) → 두 방향 중 최소 = i→j 쪽 (d=1,000)
        Problem both = problem(ALL_DAY, ALL_DAY, travel);
        double[][] s = matrix(both);
        double forward = directed(both, "I", "J");
        double backward = directed(both, "J", "I");
        assertTrue(forward < backward);
        assertEquals(forward, s[0][1]);
        assertEquals(s[0][1], s[1][0]);
        assertEquals(1_000.0 * (2.0 - (double) fMinusH(both, "I", "J") / both.planEndSec() + 2_000.0 / 30_000.0), forward, 1e-9);

        // j→i가 시간상 불가(f < 0)면 i→j만 남는다 — 그 값이 더 비싼 방향이어도 배제된 쪽은 안 쓴다
        Problem oneWay = problem(new TimeWindow(28_800L, 32_400L), new TimeWindow(36_000L, 39_600L), travel);
        assertTrue(Double.isNaN(directed(oneWay, "J", "I")));
        assertEquals(directed(oneWay, "I", "J"), matrix(oneWay)[0][1]);

        // 반대로 i→j가 불가한 창이면 j→i(9,000 기준)만 남는다
        Problem reverse = problem(new TimeWindow(36_000L, 39_600L), new TimeWindow(28_800L, 32_400L), travel);
        assertTrue(Double.isNaN(directed(reverse, "I", "J")));
        assertEquals(directed(reverse, "J", "I"), matrix(reverse)[0][1]);
        assertTrue(matrix(reverse)[0][1] > matrix(oneWay)[0][1]);

        // 양방향 다 무효면 병합 금지 쌍 → 요청당 클러스터 1개로 퇴화해도 유효한 해 (X17)
        Problem none = problem(new TimeWindow(28_800L, 28_900L), new TimeWindow(28_800L, 28_900L), travel);
        assertTrue(Double.isNaN(matrix(none)[0][1]));
        Solution solution = new SpatiotemporalClusterConstruction().construct(none, new DefaultProfile());
        assertTrue(StructureCheck.check(none, solution).isEmpty());
    }

    private static Problem problem(TimeWindow windowI, TimeWindow windowJ, List<TravelEntry> travel) {
        return freeze(
                List.of(new Depot(DEPOT, List.of(ALL_DAY), Optional.empty())),
                List.of(delivery("I", I, 1_000L, windowI, Optional.empty()), delivery("J", J, 1_000L, windowJ, Optional.empty())),
                List.of(vehicle("V1", 30_000L, Optional.of(DEPOT), Optional.empty()),
                        vehicle("V2", 30_000L, Optional.of(DEPOT), Optional.empty())),
                locations(at(DEPOT, 37.0, 127.0), at(I, 37.0, 127.0), at(J, 37.0, 127.0)),
                travel);
    }

    private static double[][] matrix(Problem problem) {
        List<Vehicle> vehicles = InsertionSearch.vehiclesByCapacityDesc(problem);
        return SpatiotemporalClusterConstruction.pairwiseDistance(problem, InsertionSearch.sortedRequestIds(problem), vehicles.getFirst());
    }

    private static double directed(Problem problem, String from, String to) {
        Request i = problem.request(new RequestId(from));
        Request j = problem.request(new RequestId(to));
        return SpatiotemporalClusterConstruction.directed(problem, i, j, 45, 30_000L, problem.planEndSec());
    }

    private static long fMinusH(Problem problem, String from, String to) {
        Request i = problem.request(new RequestId(from));
        Request j = problem.request(new RequestId(to));
        long t = problem.travel().timeSec(I, J, 45);
        long ei = InsertionSearch.firstOpenSec(InsertionSearch.anchor(i));
        long li = InsertionSearch.lastCloseSec(InsertionSearch.anchor(i));
        long ej = InsertionSearch.firstOpenSec(InsertionSearch.anchor(j));
        long lj = InsertionSearch.lastCloseSec(InsertionSearch.anchor(j));
        long si = InsertionSearch.anchor(i).serviceTimeSec();
        long f = lj - (ei + si + t);
        long h = Math.max(ej - (li + si + t), 0L);
        return f - h;
    }
}
