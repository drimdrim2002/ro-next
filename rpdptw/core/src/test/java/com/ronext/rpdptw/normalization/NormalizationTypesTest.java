package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.RawInputDigest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalizationTypesTest {

    @Test
    void testInputPathValidation() {
        assertThrows(NullPointerException.class, () -> new InputPath(null));
        InputPath path = new InputPath("requests[0].id");
        assertEquals("requests[0].id", path.dotted());
    }

    @Test
    void testInputProblemCodeCount() {
        assertEquals(31, InputProblemCode.values().length);
    }

    @Test
    void testInputProblemSubRecords() {
        InputPath path = new InputPath("test.path");
        InputProblemCode code = InputProblemCode.MISSING_REQUIRED_FIELD;

        InputProblem schema = new InputProblem.Schema(code, path);
        InputProblem identity = new InputProblem.Identity(code, path);
        InputProblem ref = new InputProblem.Reference(code, path);
        InputProblem num = new InputProblem.Numeric(code, path);
        InputProblem temp = new InputProblem.Temporal(code, path);
        InputProblem comp = new InputProblem.Compatibility(code, path);
        InputProblem trip = new InputProblem.Trip(code, path);

        assertEquals(code, schema.code());
        assertEquals(path, schema.path());
        assertEquals(code, identity.code());
        assertEquals(code, ref.code());
        assertEquals(code, num.code());
        assertEquals(code, temp.code());
        assertEquals(code, comp.code());
        assertEquals(code, trip.code());
    }

    @Test
    void testInputRejectionReportValidation() {
        assertThrows(NullPointerException.class, () -> new InputRejectionReport(null));
        assertThrows(IllegalArgumentException.class, () -> new InputRejectionReport(Collections.emptyList()));

        InputProblem problem = new InputProblem.Schema(InputProblemCode.MISSING_REQUIRED_FIELD, new InputPath("field"));
        InputRejectionReport report = new InputRejectionReport(List.of(problem));
        assertEquals(1, report.problems().size());
        assertEquals(problem, report.problems().get(0));

        // Ensure immutability of returned problems list
        assertThrows(UnsupportedOperationException.class, () -> report.problems().add(problem));
    }

    @Test
    void testNormalizationResultTypes() {
        NormalizedPlanEnvelope plan = new NormalizedPlanEnvelope(
                new com.ronext.rpdptw.input.ExternalPlanId("P"),
                new NormalizedProfileSelectionInput("C", "P", "1.0", java.util.Optional.empty()),
                0, 100, 100,
                new NormalizedRouteResourceLimits(java.util.Optional.empty(), java.util.Optional.empty()),
                WorkArcPolicy.FULL_ARC_WITHIN_ONE_WORK_WINDOW
        );
        RawInputDigest digest = new RawInputDigest(new byte[]{1});
        CanonicalFingerprint fp = new CanonicalFingerprint("sem", "env");
        com.ronext.rpdptw.input.InputProvenance prov = new com.ronext.rpdptw.input.InputProvenance(
                new com.ronext.rpdptw.input.AdapterIdentity("A"),
                new com.ronext.rpdptw.input.SchemaIdentity("S"),
                List.of(), List.of()
        );
        NormalizationPolicySnapshot policy = new NormalizationPolicySnapshot("v1", "FLOOR", "3");

        NormalizedInputArtifact artifact = new NormalizedInputArtifact(
                plan, List.of(), List.of(), List.of(), List.of(), digest, fp, List.of(), prov, policy
        );
        NormalizationResult accepted = new NormalizationResult.Accepted(artifact);
        assertInstanceOf(NormalizationResult.Accepted.class, accepted);
        assertEquals(artifact, ((NormalizationResult.Accepted) accepted).artifact());

        InputProblem problem = new InputProblem.Schema(InputProblemCode.AMBIGUOUS_ALIAS, new InputPath("alias"));
        InputRejectionReport report = new InputRejectionReport(List.of(problem));
        NormalizationResult rejected = new NormalizationResult.Rejected(report);
        assertInstanceOf(NormalizationResult.Rejected.class, rejected);
        assertEquals(report, ((NormalizationResult.Rejected) rejected).report());

        assertThrows(NullPointerException.class, () -> new NormalizationResult.Accepted(null));
        assertThrows(NullPointerException.class, () -> new NormalizationResult.Rejected(null));
    }
}
