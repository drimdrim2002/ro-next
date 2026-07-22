package com.ronext.optimizer.application;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.SplittableRandom;

/**
 * Stateless optimization boundary. The Cloud Run adapter and a future GKE worker both call this class.
 * Replace the deterministic placeholder objective with the real ALNS destroy/repair loop.
 */
public final class AlnsBatchEngine {
    public Map<String, Object> run(String requestId, String inputUri, int runNumber, long seed, int iterations) {
        RandomGenerator random = new SplittableRandom(seed);
        double diversification = random.nextDouble(0.0, 1.0) + (seed % 1_000) / 1_000_000.0;
        double objective = Math.max(1.0, 1_000_000.0 - (iterations * 11.0) - (runNumber * 97.0) + diversification);

        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("requestId", requestId);
        candidate.put("inputUri", inputUri);
        candidate.put("runNumber", runNumber);
        candidate.put("seed", seed);
        candidate.put("iterations", iterations);
        candidate.put("objective", objective);
        candidate.put("status", "CANDIDATE");
        return candidate;
    }
}
