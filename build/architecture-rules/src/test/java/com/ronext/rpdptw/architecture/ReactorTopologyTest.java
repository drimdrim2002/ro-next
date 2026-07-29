package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReactorTopologyTest {
    @Test
    void allExpectedStableModulesParticipateInAcyclicReviewedGraph() throws Exception {
        Map<String, ArchitecturePolicy.ModuleModel> modules = ArchitecturePolicy.targetModels();
        assertEquals(
                Set.of(
                        "rpdptw-core",
                        "rpdptw-solver",
                        "rpdptw-verification",
                        "rpdptw-application",
                        "rpdptw-capabilities",
                        "rpdptw-profile-catalog"),
                modules.keySet());

        Map<String, Set<String>> actual = new LinkedHashMap<>();
        modules.forEach((artifactId, model) ->
                actual.put(artifactId, ArchitecturePolicy.directRonextDependencies(model)));

        assertEquals(Set.of(), actual.get("rpdptw-core"));
        assertEquals(Set.of("rpdptw-core"), actual.get("rpdptw-solver"));
        assertEquals(Set.of("rpdptw-core"), actual.get("rpdptw-verification"));
        assertEquals(Set.of("rpdptw-core"), actual.get("rpdptw-capabilities"));
        assertEquals(Set.of("rpdptw-core"), actual.get("rpdptw-profile-catalog"));
        assertEquals(
                Set.of("rpdptw-core", "rpdptw-solver", "rpdptw-verification"),
                actual.get("rpdptw-application"));
        assertFalse(ArchitecturePolicy.hasCycle(actual));
    }

    @Test
    void rootIsDependencyFreePomAggregator() throws Exception {
        ArchitecturePolicy.ModuleModel root =
                ArchitecturePolicy.readModule(ArchitecturePolicy.repository().resolve("pom.xml"));
        String pom = Files.readString(root.pom());

        assertEquals("ro-next-parent", root.artifactId());
        assertEquals("pom", root.packaging());
        assertTrue(root.dependencies().isEmpty());
        assertTrue(pom.contains("<module>rpdptw</module>"));
        assertTrue(pom.contains("<module>build</module>"));
        assertTrue(pom.contains("<module>legacy</module>"));
    }

    @Test
    void phaseZeroCreatesOnlyPackageOwnershipNotFutureDomainTypes() throws Exception {
        Path repository = ArchitecturePolicy.repository();
        for (String modulePath : ArchitecturePolicy.STABLE_MODULE_PATHS) {
            for (Path source : ArchitecturePolicy.javaSources(repository.resolve(modulePath))) {
                assertEquals("package-info.java", source.getFileName().toString(), source.toString());
            }
        }
        assertFalse(Files.exists(repository.resolve("adapters/route-selection-ortools-cpsat")));
        assertFalse(Files.exists(repository.resolve("rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/pool")));
        assertFalse(Files.exists(repository.resolve("rpdptw/core/src/main/java/com/ronext/rpdptw/domain/CanonicalInput.java")));
        assertFalse(Files.exists(repository.resolve("rpdptw/core/src/main/java/com/ronext/rpdptw/domain/ProblemInstance.java")));
    }
}
