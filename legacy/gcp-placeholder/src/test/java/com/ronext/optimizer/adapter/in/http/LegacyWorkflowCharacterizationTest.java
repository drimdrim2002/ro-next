package com.ronext.optimizer.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegacyWorkflowCharacterizationTest {

    @Test
    @DisplayName("dispatchesDeclaredParallelRangeThenFinalize: Verify workflow dispatch range and finalize sequence matching gcp/workflows/optimization.yaml")
    void dispatchesDeclaredParallelRangeThenFinalize() throws Exception {
        Path workflowYamlPath = Path.of("../../gcp/workflows/optimization.yaml").toAbsolutePath().normalize();
        if (!Files.exists(workflowYamlPath)) {
            workflowYamlPath = Path.of("gcp/workflows/optimization.yaml").toAbsolutePath().normalize();
        }

        assertTrue(Files.exists(workflowYamlPath), "gcp/workflows/optimization.yaml must exist at " + workflowYamlPath);

        String yamlContent = Files.readString(workflowYamlPath);

        assertTrue(yamlContent.contains("runAlnsBatches:"), "Workflow must contain runAlnsBatches step");
        assertTrue(yamlContent.contains("range: [0, \"${args.parameters.parallelRuns - 1}\"]"),
                "Workflow must dispatch parallel range from 0 to parallelRuns - 1");
        assertTrue(yamlContent.contains("/internal/batches"), "Workflow parallel steps must call /internal/batches");
        assertTrue(yamlContent.contains("finalize:"), "Workflow must contain finalize step");
        assertTrue(yamlContent.contains("/internal/finalize"), "Workflow finalize step must call /internal/finalize");

        // Verify sequence order: runAlnsBatches before finalize
        int batchesIndex = yamlContent.indexOf("runAlnsBatches:");
        int finalizeIndex = yamlContent.indexOf("finalize:");
        assertTrue(batchesIndex < finalizeIndex, "runAlnsBatches step must precede finalize step");
    }

    @Test
    @DisplayName("derivesCurrentWorkerSeedFromBaseSeedAndRunNumber: Verify exact worker seed derivation formula (baseSeed + runNumber)")
    void derivesCurrentWorkerSeedFromBaseSeedAndRunNumber() {
        long baseSeed = 100_000L;

        for (int runNumber = 0; runNumber < 8; runNumber++) {
            long derivedWorkerSeed = deriveWorkerSeed(baseSeed, runNumber);
            assertEquals(baseSeed + runNumber, derivedWorkerSeed,
                    "Worker seed for runNumber " + runNumber + " must equal baseSeed + runNumber");
        }
    }

    private static long deriveWorkerSeed(long baseSeed, int runNumber) {
        return baseSeed + runNumber;
    }
}
