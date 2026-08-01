package com.ronext.optimizer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlnsBatchEngineCharacterizationTest {

    private final AlnsBatchEngine engine = new AlnsBatchEngine();

    @Test
    @DisplayName("sameSeedRunAndIterationsProduceSameSyntheticCandidate: Assert equal objective value for identical seed/run/iterations")
    void sameSeedRunAndIterationsProduceSameSyntheticCandidate() {
        Map<String, Object> candidate1 = engine.run("req-100", "gs://bucket-a/input-1.json", 2, 42L, 5_000);
        Map<String, Object> candidate2 = engine.run("req-200", "gs://bucket-b/input-2.json", 2, 42L, 5_000);

        assertNotNull(candidate1);
        assertNotNull(candidate2);

        assertEquals("CANDIDATE", candidate1.get("status"));
        assertEquals("CANDIDATE", candidate2.get("status"));
        assertEquals(2, candidate1.get("runNumber"));
        assertEquals(42L, candidate1.get("seed"));
        assertEquals(5_000, candidate1.get("iterations"));

        assertEquals(candidate1.get("objective"), candidate2.get("objective"),
                "Identical seed, runNumber, and iterations must produce identical objective value");
    }

    @Test
    @DisplayName("inputUriBytesAreNotReadByCurrentPlaceholder: Prove engine ignores input URI/bytes")
    void inputUriBytesAreNotReadByCurrentPlaceholder() {
        String nonExistentUri = "gs://non-existent-bucket-9999/non-existent-file-9999.json";

        Map<String, Object> candidate = assertDoesNotThrow(
                () -> engine.run("req-300", nonExistentUri, 0, 100L, 1_000),
                "Engine placeholder must not attempt to read or fetch bytes from inputUri"
        );

        assertNotNull(candidate);
        assertEquals(nonExistentUri, candidate.get("inputUri"));
        assertEquals("CANDIDATE", candidate.get("status"));
        assertNotNull(candidate.get("objective"));
    }
}
