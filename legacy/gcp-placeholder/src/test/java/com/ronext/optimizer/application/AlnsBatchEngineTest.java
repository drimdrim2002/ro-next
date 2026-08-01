package com.ronext.optimizer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AlnsBatchEngineTest {
    private final AlnsBatchEngine engine = new AlnsBatchEngine();

    @Test
    void producesCandidateForIndependentAlnsBatch() {
        Map<String, Object> candidate = engine.run("request-1", "gs://bucket/instance.json", 2, 42L, 5_000);

        assertEquals("CANDIDATE", candidate.get("status"));
        assertEquals(2, candidate.get("runNumber"));
        assertTrue(((Double) candidate.get("objective")) > 0.0);
    }
}
