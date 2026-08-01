package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.adapter.input.AdaptedCanonicalInput;
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
import com.ronext.rpdptw.input.RawInputDigest;
import com.ronext.rpdptw.input.SchemaIdentity;
import com.ronext.rpdptw.input.ServicePattern;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Phase01NormalizedSealTest {

    private final CanonicalInputNormalizer normalizer = new DefaultCanonicalInputNormalizer();
    private final NormalizationPolicySnapshot policy =
            new NormalizationPolicySnapshot("1.0", "FLOOR_SCALE_3", "MILLI_UNITS");

    private CanonicalBusinessInput baseInput(String weightDecimal, int qty, Optional<String> ownership, Optional<String> speed) {
        CanonicalPlanEnvelope plan = new CanonicalPlanEnvelope(
                new ExternalPlanId("PLAN-1"),
                "CUST",
                "PROF",
                "1.0",
                Optional.empty(),
                "2026-08-01 08:00:00",
                "2026-08-01 18:00:00"
        );
        CanonicalLocationInput loc = new CanonicalLocationInput(new ExternalLocationId("LOC-1"), Optional.of("ZONE-A"));
        CanonicalVehicleInput vehicle = new CanonicalVehicleInput(
                new ExternalVehicleId("VEH-1"),
                "MEDIUM",
                Set.of(),
                Set.of("ZONE-A"),
                ownership,
                speed,
                true,
                false,
                Optional.empty(),
                Optional.empty()
        );
        CanonicalRequestInput request = new CanonicalRequestInput(
                new ExternalRequestId("REQ-1"),
                ServicePattern.DELIVERY_ONLY,
                Optional.empty(),
                new CanonicalServiceInput(
                        new ExternalLocationId("LOC-1"),
                        "2026-08-01 09:00:00",
                        "2026-08-01 17:00:00",
                        "300",
                        Optional.empty(),
                        Optional.of("ZONE-A")
                ),
                List.of(new CanonicalItemInput(weightDecimal, "0.500", qty, "0")),
                new CanonicalCompatibilityInput(List.of("ALL"), Set.of()),
                Optional.empty(),
                Optional.empty()
        );
        CanonicalTravelInput travel = new CanonicalTravelInput(
                new ExternalLocationId("LOC-1"),
                new ExternalLocationId("LOC-1"),
                "0",
                "0"
        );
        InputProvenance provenance = new InputProvenance(
                new AdapterIdentity("TEST"),
                new SchemaIdentity("v1"),
                List.of(),
                List.of()
        );
        return new CanonicalBusinessInput(plan, List.of(vehicle), List.of(loc), List.of(request), List.of(travel), provenance);
    }

    private NormalizedInputArtifact accept(CanonicalBusinessInput input) {
        AdaptedCanonicalInput adapted = new AdaptedCanonicalInput(
                input, new RawInputDigest("digest".getBytes(StandardCharsets.UTF_8))
        );
        NormalizationResult result = normalizer.normalize(adapted, policy);
        assertInstanceOf(NormalizationResult.Accepted.class, result, "expected Accepted but got " + result);
        return ((NormalizationResult.Accepted) result).artifact();
    }

    @Test
    void artifactExposesItemFirstMilliWeightNotRawString() {
        NormalizedInputArtifact artifact = accept(baseInput("0.0009", 2, Optional.of("DIRECT"), Optional.of("60.0")));
        NormalizedItem item = artifact.requests().get(0).items().get(0);
        assertEquals(0L, item.weightMilli().value());
        assertEquals(2, item.quantity());
        assertNotNull(item.weightMilli());
    }

    @Test
    void artifactPreservesAbsentOwnershipAndSpeedWithoutDefaults() {
        NormalizedInputArtifact artifact = accept(baseInput("10.000", 1, Optional.empty(), Optional.empty()));
        NormalizedVehicle vehicle = artifact.vehicles().get(0);
        assertInstanceOf(VehicleOwnership.Absent.class, vehicle.ownership());
        assertInstanceOf(VehicleSpeedInput.Absent.class, vehicle.speed());
    }

    @Test
    void semanticFingerprintIgnoresRawLexemeSynonymsAfterFloor() {
        CanonicalBusinessInput a = baseInput("1.2340", 1, Optional.of("DIRECT"), Optional.of("60.0"));
        CanonicalBusinessInput b = baseInput("1.2349", 1, Optional.of("DIRECT"), Optional.of("60.0"));
        NormalizedInputArtifact artA = accept(a);
        NormalizedInputArtifact artB = accept(b);
        assertEquals(1234L, artA.requests().get(0).items().get(0).weightMilli().value());
        assertEquals(1234L, artB.requests().get(0).items().get(0).weightMilli().value());
        assertEquals(
                artA.fingerprint().semanticFingerprint(),
                artB.fingerprint().semanticFingerprint()
        );
    }
}
