package com.ronext.optimizer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AlnsBatchEngineCharacterizationTest {
    private final AlnsBatchEngine engine = new AlnsBatchEngine();

    @Test
    void sameSeedRunAndIterationsProduceSameSyntheticCandidate() {
        Map<String, Object> first = engine.run("request-1", "gs://bucket/input.json", 3, 42L, 5_000);
        Map<String, Object> second = engine.run("request-1", "gs://bucket/input.json", 3, 42L, 5_000);
        Map<String, Object> differentSeed = engine.run("request-1", "gs://bucket/input.json", 3, 43L, 5_000);

        assertEquals(first, second);
        assertNotEquals(first.get("objective"), differentSeed.get("objective"));
        assertEquals("CANDIDATE", first.get("status"));
        assertEquals(3, first.get("runNumber"));
        assertEquals(42L, first.get("seed"));
        assertEquals(5_000, first.get("iterations"));
    }

    @Test
    void inputUriBytesAreNotReadByCurrentPlaceholder() {
        Map<String, Object> first = engine.run("request-1", "gs://does-not-exist/a.json", 2, 7L, 100);
        Map<String, Object> second = engine.run("request-1", "gs://another/missing.json", 2, 7L, 100);

        assertEquals(first.get("objective"), second.get("objective"));
        assertEquals("gs://does-not-exist/a.json", first.get("inputUri"));
        assertEquals("gs://another/missing.json", second.get("inputUri"));
    }
}
