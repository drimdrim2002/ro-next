package com.ronext.optimizer.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LegacyBuildContractCharacterizationTest {
    private static final Path REPOSITORY = Path.of(System.getProperty("phase00.repoRoot"));

    @Test
    void preservesMainClassDependenciesAndLegacyOnlyCoordinate() throws Exception {
        String pom = Files.readString(REPOSITORY.resolve("legacy/gcp-placeholder/pom.xml"));

        assertTrue(pom.contains("<artifactId>legacy-gcp-placeholder</artifactId>"));
        assertTrue(pom.contains("<mainClass>com.ronext.optimizer.adapter.in.http.OptimizationHttpServer</mainClass>"));
        assertTrue(pom.contains("<artifactId>google-cloud-workflow-executions</artifactId>"));
        assertTrue(pom.contains("<artifactId>google-cloud-storage</artifactId>"));
        assertTrue(pom.contains("<artifactId>jackson-databind</artifactId>"));
        assertTrue(pom.contains("<artifactId>jackson-datatype-jsr310</artifactId>"));
        assertFalse(Files.readString(REPOSITORY.resolve("pom.xml")).contains("<artifactId>google-cloud-storage</artifactId>"));
    }

    @Test
    void preservesCurrentEndpointObjectKeyAndRawObjectiveMechanics() throws Exception {
        String api = Files.readString(REPOSITORY.resolve(
                "legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java"));
        String worker = Files.readString(REPOSITORY.resolve(
                "legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java"));

        assertTrue(api.contains("\"/optimizations\""));
        assertTrue(api.contains("\"results/\" + requestId + \".json\""));
        assertTrue(worker.contains("\"/internal/batches\""));
        assertTrue(worker.contains("\"/internal/finalize\""));
        assertTrue(worker.contains("\"candidates/\" + requestId + \"/\""));
        assertTrue(worker.contains("Comparator.comparingDouble"));
        assertEquals(1, count(worker, "listCandidateObjects("));
        assertEquals(1, count(worker, "storage.readCandidate(bucket, reference)"));
    }

    private static int count(String value, String token) {
        int count = 0;
        for (int index = value.indexOf(token); index >= 0; index = value.indexOf(token, index + token.length())) {
            count++;
        }
        return count;
    }
}
