package com.ronext.optimizer.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LegacyWorkflowCharacterizationTest {
    private static final Path REPOSITORY = Path.of(System.getProperty("phase00.repoRoot"));

    @Test
    void dispatchesDeclaredParallelRangeThenFinalize() throws Exception {
        String workflow = Files.readString(REPOSITORY.resolve("gcp/workflows/optimization.yaml"));
        int parallel = workflow.indexOf("- runAlnsBatches:");
        int finalize = workflow.indexOf("- finalize:");
        int completed = workflow.indexOf("- completed:");

        assertTrue(parallel >= 0);
        assertTrue(finalize > parallel);
        assertTrue(completed > finalize);
        assertTrue(workflow.contains("range: [0, \"${args.parameters.parallelRuns - 1}\"]"));
        assertTrue(workflow.contains("/internal/batches"));
        assertTrue(workflow.contains("/internal/finalize"));
    }

    @Test
    void derivesCurrentWorkerSeedFromBaseSeedAndRunNumber() throws Exception {
        String workflow = Files.readString(REPOSITORY.resolve("gcp/workflows/optimization.yaml"));

        assertTrue(workflow.contains("seed: \"${args.parameters.seed + runNumber}\""));
        assertTrue(workflow.contains("iterations: \"${args.parameters.iterationsPerRun}\""));
    }
}
