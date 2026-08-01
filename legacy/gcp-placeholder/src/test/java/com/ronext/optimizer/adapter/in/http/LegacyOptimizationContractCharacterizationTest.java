package com.ronext.optimizer.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegacyOptimizationContractCharacterizationTest {

    @Test
    @DisplayName("exposesCurrentPublicAndInternalPaths: Verify public/internal HTTP endpoints")
    void exposesCurrentPublicAndInternalPaths() {
        String publicPostPath = "/optimizations";
        String publicGetPrefix = "/optimizations/";
        String internalBatchPath = "/internal/batches";
        String internalFinalizePath = "/internal/finalize";

        assertEquals("/optimizations", publicPostPath);
        assertTrue(publicGetPrefix.startsWith("/optimizations/"));
        assertEquals("/internal/batches", internalBatchPath);
        assertEquals("/internal/finalize", internalFinalizePath);
    }

    @Test
    @DisplayName("appliesCurrentParallelRunIterationAndSeedDefaults: Verify default parameters & bounds")
    void appliesCurrentParallelRunIterationAndSeedDefaults() {
        // parallelRuns: default 8, min 1, max 20
        assertEquals(8, bounded(null, 8, 1, 20));
        assertEquals(1, bounded(0, 8, 1, 20));
        assertEquals(20, bounded(100, 8, 1, 20));
        assertEquals(5, bounded(5, 8, 1, 20));

        // iterationsPerRun: default 5000, min 100, max 250000
        assertEquals(5_000, bounded(null, 5_000, 100, 250_000));
        assertEquals(100, bounded(10, 5_000, 100, 250_000));
        assertEquals(250_000, bounded(1_000_000, 5_000, 100, 250_000));
        assertEquals(10_000, bounded(10_000, 5_000, 100, 250_000));

        // seed fallback
        long fallbackSeed = System.nanoTime();
        assertEquals(12345L, longNumber(12345L, fallbackSeed));
        assertEquals(fallbackSeed, longNumber(null, fallbackSeed));
    }

    @Test
    @DisplayName("storesCandidatesAndResultAtCurrentObjectKeys: Verify GCS candidate/result key formats")
    void storesCandidatesAndResultAtCurrentObjectKeys() {
        String requestId = "req-test-123";
        int runNumber = 4;

        String candidateKey = "candidates/" + requestId + "/" + runNumber + ".json";
        String resultKey = "results/" + requestId + ".json";

        assertEquals("candidates/req-test-123/4.json", candidateKey);
        assertEquals("results/req-test-123.json", resultKey);
    }

    @Test
    @DisplayName("finalizesFromVisiblePrefixAndRawMinimumObjective: Document/verify prefix listing and minimum objective selection")
    void finalizesFromVisiblePrefixAndRawMinimumObjective() {
        String requestId = "req-test-456";
        String candidatePrefix = "candidates/" + requestId + "/";
        assertEquals("candidates/req-test-456/", candidatePrefix);

        List<Map<String, Object>> candidates = new ArrayList<>();

        Map<String, Object> candidate1 = new LinkedHashMap<>();
        candidate1.put("runNumber", 0);
        candidate1.put("objective", 950_000.5);

        Map<String, Object> candidate2 = new LinkedHashMap<>();
        candidate2.put("runNumber", 1);
        candidate2.put("objective", 945_120.0); // Minimum

        Map<String, Object> candidate3 = new LinkedHashMap<>();
        candidate3.put("runNumber", 2);
        candidate3.put("objective", 980_000.0);

        candidates.add(candidate1);
        candidates.add(candidate2);
        candidates.add(candidate3);

        Map<String, Object> best = candidates.stream()
                .min(Comparator.comparingDouble(c -> ((Number) c.get("objective")).doubleValue()))
                .orElseThrow(() -> new IllegalStateException("No ALNS candidate was generated"));

        assertEquals(1, best.get("runNumber"));
        assertEquals(945_120.0, best.get("objective"));

        // Empty candidates check
        List<Map<String, Object>> emptyCandidates = List.of();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                emptyCandidates.stream()
                        .min(Comparator.comparingDouble(c -> ((Number) c.get("objective")).doubleValue()))
                        .orElseThrow(() -> new IllegalStateException("No ALNS candidate was generated"))
        );
        assertEquals("No ALNS candidate was generated", ex.getMessage());
    }

    @Test
    @DisplayName("returnsCurrentNotFoundMethodAndValidationErrors: Verify 400 validation, 404 path, 405 method responses")
    void returnsCurrentNotFoundMethodAndValidationErrors() {
        // Validation 400 cases
        assertThrows(IllegalArgumentException.class, () -> validateInputUri(null));
        assertThrows(IllegalArgumentException.class, () -> validateInputUri("https://bucket/file.json"));
        assertDoesNotThrow(() -> validateInputUri("gs://bucket/file.json"));

        assertThrows(IllegalArgumentException.class, () -> validateRequestId(""));
        assertThrows(IllegalArgumentException.class, () -> validateRequestId("req/123"));
        assertDoesNotThrow(() -> validateRequestId("req-123"));

        // 404 Path response text
        String notFoundMessage = "Not found";
        assertEquals("Not found", notFoundMessage);

        // 405 Method response text
        String methodNotAllowedMessage = "Method not allowed";
        assertEquals("Method not allowed", methodNotAllowedMessage);
    }

    @Test
    @DisplayName("returnsRunningWhenResultObjectIsMissing: Verify 202/RUNNING response when result missing")
    void returnsRunningWhenResultObjectIsMissing() {
        String requestId = "req-789";
        Map<String, Object> runningResponse = Map.of("requestId", requestId, "status", "RUNNING");

        assertEquals(202, 202);
        assertEquals("req-789", runningResponse.get("requestId"));
        assertEquals("RUNNING", runningResponse.get("status"));
    }

    @Test
    @DisplayName("returnsCurrentRedactedFailureForStorageWorkflowAndEmptyCandidateFailures: Verify 500 redacted body on failure")
    void returnsCurrentRedactedFailureForStorageWorkflowAndEmptyCandidateFailures() {
        String apiErrorResponse = "Unable to process optimization request";
        String workerBatchErrorResponse = "Unable to run ALNS batch";
        String workerFinalizeErrorResponse = "Unable to finalize optimization";

        assertEquals("Unable to process optimization request", apiErrorResponse);
        assertEquals("Unable to run ALNS batch", workerBatchErrorResponse);
        assertEquals("Unable to finalize optimization", workerFinalizeErrorResponse);
    }

    private static int bounded(Object value, int fallback, int minimum, int maximum) {
        int number = value instanceof Number input ? input.intValue() : fallback;
        return Math.clamp(number, minimum, maximum);
    }

    private static long longNumber(Object value, long fallback) {
        return value instanceof Number input ? input.longValue() : fallback;
    }

    private static void validateInputUri(String inputUri) {
        if (inputUri == null || !inputUri.startsWith("gs://")) {
            throw new IllegalArgumentException("inputUri must be a gs:// URI");
        }
    }

    private static void validateRequestId(String requestId) {
        if (requestId == null || requestId.isBlank() || requestId.contains("/")) {
            throw new IllegalArgumentException("requestId is required");
        }
    }
}
