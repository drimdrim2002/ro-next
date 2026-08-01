package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.input.CanonicalBusinessInput;
import com.ronext.rpdptw.input.CanonicalCompatibilityInput;
import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalLocationInput;
import com.ronext.rpdptw.input.CanonicalPlanEnvelope;
import com.ronext.rpdptw.input.CanonicalRequestInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;
import com.ronext.rpdptw.input.CanonicalTravelInput;
import com.ronext.rpdptw.input.CanonicalVehicleInput;
import com.ronext.rpdptw.input.ExternalLocationId;
import com.ronext.rpdptw.input.ExternalPlanId;
import com.ronext.rpdptw.input.ExternalRequestId;
import com.ronext.rpdptw.input.ExternalVehicleId;
import com.ronext.rpdptw.input.InputProvenance;
import com.ronext.rpdptw.input.SchemaIdentity;
import com.ronext.rpdptw.input.ServicePattern;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalOrderingTest {

    private CanonicalPlanEnvelope createSamplePlan() {
        return new CanonicalPlanEnvelope(
                new ExternalPlanId("PLAN-1"),
                "CUST-1",
                "PROF-1",
                "1.0",
                Optional.empty(),
                "2026-08-01 08:00:00",
                "2026-08-01 18:00:00"
        );
    }

    private InputProvenance createSampleProvenance() {
        return new InputProvenance(
                new AdapterIdentity("TEST"),
                new SchemaIdentity("v1"),
                List.of(),
                List.of()
        );
    }

    private CanonicalServiceInput createService(String locId) {
        return new CanonicalServiceInput(
                new ExternalLocationId(locId),
                "2026-08-01 09:00:00",
                "2026-08-01 17:00:00",
                "300",
                Optional.empty(),
                Optional.empty()
        );
    }

    private CanonicalRequestInput createRequest(String reqId, String deliveryLocId) {
        return new CanonicalRequestInput(
                new ExternalRequestId(reqId),
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                createService(deliveryLocId),
                List.of(new CanonicalItemInput("10.000", "0.500", 1, "0")),
                new CanonicalCompatibilityInput(List.of("ALL"), Set.of()),
                Optional.empty(),
                Optional.empty()
        );
    }

    @Test
    void rejectsDuplicateRequestBeforeCanonicalSort() {
        CanonicalLocationInput loc1 = new CanonicalLocationInput(new ExternalLocationId("LOC-1"), Optional.empty());
        List<CanonicalLocationInput> locations = List.of(loc1);

        CanonicalRequestInput req1 = createRequest("REQ-1", "LOC-1");
        CanonicalRequestInput reqDuplicate = createRequest("REQ-1", "LOC-1");
        List<CanonicalRequestInput> requests = List.of(req1, reqDuplicate);

        CanonicalBusinessInput input = new CanonicalBusinessInput(
                createSamplePlan(),
                List.of(),
                locations,
                requests,
                List.of(),
                createSampleProvenance()
        );

        IdentityAndReferenceNormalizer normalizer = new IdentityAndReferenceNormalizer();
        List<InputProblem> problems = normalizer.validate(input);

        assertEquals(1, problems.size());
        InputProblem problem = problems.get(0);
        assertEquals(InputProblemCode.DUPLICATE_IDENTITY, problem.code());
        assertEquals("requests[1].id", problem.path().dotted());
    }

    @Test
    void rejectsDuplicateTravelKeyEvenWhenValuesMatch() {
        CanonicalLocationInput loc1 = new CanonicalLocationInput(new ExternalLocationId("LOC-1"), Optional.empty());
        CanonicalLocationInput loc2 = new CanonicalLocationInput(new ExternalLocationId("LOC-2"), Optional.empty());
        List<CanonicalLocationInput> locations = List.of(loc1, loc2);

        CanonicalTravelInput t1 = new CanonicalTravelInput(new ExternalLocationId("LOC-1"), new ExternalLocationId("LOC-2"), "600", "5000");
        CanonicalTravelInput tDuplicate = new CanonicalTravelInput(new ExternalLocationId("LOC-1"), new ExternalLocationId("LOC-2"), "600", "5000");
        List<CanonicalTravelInput> travelCosts = List.of(t1, tDuplicate);

        CanonicalBusinessInput input = new CanonicalBusinessInput(
                createSamplePlan(),
                List.of(),
                locations,
                List.of(),
                travelCosts,
                createSampleProvenance()
        );

        IdentityAndReferenceNormalizer normalizer = new IdentityAndReferenceNormalizer();
        List<InputProblem> problems = normalizer.validate(input);

        assertEquals(1, problems.size());
        InputProblem problem = problems.get(0);
        assertEquals(InputProblemCode.DUPLICATE_TRAVEL_KEY, problem.code());
        assertEquals("travelCosts[1]", problem.path().dotted());
    }

    @Test
    void rejectsDanglingLocationReference() {
        CanonicalLocationInput loc1 = new CanonicalLocationInput(new ExternalLocationId("LOC-1"), Optional.empty());
        List<CanonicalLocationInput> locations = List.of(loc1);

        CanonicalRequestInput reqDangling = createRequest("REQ-1", "LOC-MISSING");
        List<CanonicalRequestInput> requests = List.of(reqDangling);

        CanonicalTravelInput tDangling = new CanonicalTravelInput(new ExternalLocationId("LOC-1"), new ExternalLocationId("LOC-MISSING-2"), "600", "5000");
        List<CanonicalTravelInput> travelCosts = List.of(tDangling);

        CanonicalBusinessInput input = new CanonicalBusinessInput(
                createSamplePlan(),
                List.of(),
                locations,
                requests,
                travelCosts,
                createSampleProvenance()
        );

        IdentityAndReferenceNormalizer normalizer = new IdentityAndReferenceNormalizer();
        List<InputProblem> problems = normalizer.validate(input);

        assertEquals(2, problems.size());
        assertTrue(problems.stream().anyMatch(p -> p.code() == InputProblemCode.DANGLING_REFERENCE && p.path().dotted().equals("requests[0].delivery.locationId")));
        assertTrue(problems.stream().anyMatch(p -> p.code() == InputProblemCode.DANGLING_REFERENCE && p.path().dotted().equals("travelCosts[0].to")));
    }

    @Test
    void failureOrderIsInputPermutationIndependent() {
        CanonicalLocationInput loc1 = new CanonicalLocationInput(new ExternalLocationId("LOC-1"), Optional.empty());

        CanonicalRequestInput req1 = createRequest("REQ-1", "LOC-MISSING-A");
        CanonicalRequestInput req2 = createRequest("REQ-2", "LOC-MISSING-B");

        // Permutation 1: [REQ-1, REQ-2]
        CanonicalBusinessInput input1 = new CanonicalBusinessInput(
                createSamplePlan(),
                List.of(),
                List.of(loc1),
                List.of(req1, req2),
                List.of(),
                createSampleProvenance()
        );

        // Permutation 2: [REQ-2, REQ-1]
        CanonicalBusinessInput input2 = new CanonicalBusinessInput(
                createSamplePlan(),
                List.of(),
                List.of(loc1),
                List.of(req2, req1),
                List.of(),
                createSampleProvenance()
        );

        IdentityAndReferenceNormalizer normalizer = new IdentityAndReferenceNormalizer();
        List<InputProblem> problems1 = normalizer.validate(input1);
        List<InputProblem> problems2 = normalizer.validate(input2);

        assertEquals(2, problems1.size());
        assertEquals(2, problems2.size());

        // Check that problem list is sorted by path then code in both cases
        for (int i = 0; i < problems1.size() - 1; i++) {
            assertTrue(CanonicalOrdering.PROBLEM_COMPARATOR.compare(problems1.get(i), problems1.get(i + 1)) <= 0);
        }
        for (int i = 0; i < problems2.size() - 1; i++) {
            assertTrue(CanonicalOrdering.PROBLEM_COMPARATOR.compare(problems2.get(i), problems2.get(i + 1)) <= 0);
        }
    }

    @Test
    void testCanonicalOrderingComparators() {
        CanonicalRequestInput r1 = createRequest("REQ-A", "LOC-1");
        CanonicalRequestInput r2 = createRequest("REQ-B", "LOC-1");
        assertTrue(CanonicalOrdering.REQUEST_COMPARATOR.compare(r1, r2) < 0);

        CanonicalVehicleInput v1 = new CanonicalVehicleInput(new ExternalVehicleId("V-A"), "S", Set.of(), Set.of(), Optional.empty(), Optional.empty(), true, false, Optional.empty(), Optional.empty());
        CanonicalVehicleInput v2 = new CanonicalVehicleInput(new ExternalVehicleId("V-B"), "S", Set.of(), Set.of(), Optional.empty(), Optional.empty(), true, false, Optional.empty(), Optional.empty());
        assertTrue(CanonicalOrdering.VEHICLE_COMPARATOR.compare(v1, v2) < 0);

        CanonicalLocationInput l1 = new CanonicalLocationInput(new ExternalLocationId("LOC-A"), Optional.empty());
        CanonicalLocationInput l2 = new CanonicalLocationInput(new ExternalLocationId("LOC-B"), Optional.empty());
        assertTrue(CanonicalOrdering.LOCATION_COMPARATOR.compare(l1, l2) < 0);

        CanonicalTravelInput t1 = new CanonicalTravelInput(new ExternalLocationId("LOC-A"), new ExternalLocationId("LOC-B"), "10", "10");
        CanonicalTravelInput t2 = new CanonicalTravelInput(new ExternalLocationId("LOC-A"), new ExternalLocationId("LOC-C"), "10", "10");
        assertTrue(CanonicalOrdering.TRAVEL_COMPARATOR.compare(t1, t2) < 0);

        InputProblem p1 = new InputProblem.Reference(InputProblemCode.DANGLING_REFERENCE, new InputPath("requests[0].delivery.locationId"));
        InputProblem p2 = new InputProblem.Reference(InputProblemCode.DANGLING_REFERENCE, new InputPath("requests[1].delivery.locationId"));
        assertTrue(CanonicalOrdering.PROBLEM_COMPARATOR.compare(p1, p2) < 0);
    }
}
