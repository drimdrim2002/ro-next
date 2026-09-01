package com.ronext.rpdptw.solve;

import static com.ronext.rpdptw.solve.SolveFixtures.R1;
import static com.ronext.rpdptw.solve.SolveFixtures.R2;
import static com.ronext.rpdptw.solve.SolveFixtures.V1;
import static com.ronext.rpdptw.solve.SolveFixtures.V2;
import static com.ronext.rpdptw.solve.SolveFixtures.section72Problem;
import static com.ronext.rpdptw.solve.SolveFixtures.freeze;
import static com.ronext.rpdptw.solve.SolveFixtures.vehicle;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.ASSIGNED_AND_BANKED;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.DUPLICATE_NODE;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.NOT_ASSIGNED_NOT_BANKED;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.PAIR_INCOMPLETE;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.PAIR_SPLIT;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.PICKUP_AFTER_DELIVERY;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.UNKNOWN_NODE;
import static com.ronext.rpdptw.solve.StructureViolation.Kind.UNKNOWN_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.domain.NodeId;
import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.TimeWindow;
import com.ronext.rpdptw.problem.Problem;

class StructureCheckTest {

    @Test
    void detectsXorViolations() {
        Problem problem = section72Problem(false);

        List<StructureViolation> assignedAndBanked = StructureCheck.check(
                problem,
                new Solution(
                        List.of(new Route(V1, List.of(NodeId.pickup(R2), NodeId.delivery(R2), NodeId.delivery(R1)))),
                        Set.of(R1)));
        assertTrue(hasKind(assignedAndBanked, ASSIGNED_AND_BANKED));

        List<StructureViolation> neither = StructureCheck.check(
                problem, new Solution(List.of(new Route(V1, List.of(NodeId.pickup(R2), NodeId.delivery(R2)))), Set.of()));
        assertTrue(hasKind(neither, NOT_ASSIGNED_NOT_BANKED));

        List<StructureViolation> allBanked = StructureCheck.check(problem, new Solution(List.of(), Set.of(R1, R2)));
        assertEquals(List.of(), allBanked);

        List<StructureViolation> duplicate = StructureCheck.check(
                problem,
                new Solution(
                        List.of(new Route(
                                V1,
                                List.of(
                                        NodeId.pickup(R2),
                                        NodeId.delivery(R2),
                                        NodeId.delivery(R1),
                                        NodeId.delivery(R1)))),
                        Set.of()));
        assertTrue(hasKind(duplicate, DUPLICATE_NODE));

        List<StructureViolation> unknownBank = StructureCheck.check(
                problem, new Solution(List.of(), Set.of(R1, R2, new RequestId("RX"))));
        assertTrue(hasKind(unknownBank, UNKNOWN_REQUEST));
    }

    @Test
    void detectsPairViolations() {
        Problem problem = twoVehicleProblem();

        List<StructureViolation> fakePickup = StructureCheck.check(
                problem,
                new Solution(
                        List.of(new Route(V1, List.of(NodeId.pickup(R1), NodeId.delivery(R1)))),
                        Set.of(R2)));
        assertTrue(hasKind(fakePickup, UNKNOWN_NODE));

        List<StructureViolation> split = StructureCheck.check(
                problem,
                new Solution(
                        List.of(
                                new Route(V1, List.of(NodeId.pickup(R2))),
                                new Route(V2, List.of(NodeId.delivery(R2), NodeId.delivery(R1)))),
                        Set.of()));
        assertTrue(hasKind(split, PAIR_SPLIT));

        List<StructureViolation> incomplete = StructureCheck.check(
                problem,
                new Solution(List.of(new Route(V1, List.of(NodeId.pickup(R2), NodeId.delivery(R1)))), Set.of()));
        assertTrue(hasKind(incomplete, PAIR_INCOMPLETE));

        List<StructureViolation> after = StructureCheck.check(
                problem,
                new Solution(
                        List.of(new Route(V1, List.of(NodeId.delivery(R2), NodeId.pickup(R2), NodeId.delivery(R1)))),
                        Set.of()));
        assertTrue(hasKind(after, PICKUP_AFTER_DELIVERY));
    }

    private static boolean hasKind(List<StructureViolation> violations, StructureViolation.Kind kind) {
        return violations.stream().anyMatch(v -> v.kind() == kind);
    }

    private static Problem twoVehicleProblem() {
        Problem base = section72Problem(false);
        return freeze(
                false,
                base.depots(),
                base.requests(),
                List.of(
                        vehicle(
                                "V1",
                                30_000L,
                                1_000_000L,
                                List.of(new TimeWindow(28_800, 64_800)),
                                Optional.of(SolveFixtures.DEPOT),
                                Optional.empty(),
                                OptionalInt.empty(),
                                OptionalLong.empty(),
                                OptionalLong.empty(),
                                Optional.empty()),
                        vehicle(
                                "V2",
                                30_000L,
                                1_000_000L,
                                List.of(new TimeWindow(28_800, 64_800)),
                                Optional.of(SolveFixtures.DEPOT),
                                Optional.empty(),
                                OptionalInt.empty(),
                                OptionalLong.empty(),
                                OptionalLong.empty(),
                                Optional.empty())),
                base.locations(),
                List.of());
    }
}
