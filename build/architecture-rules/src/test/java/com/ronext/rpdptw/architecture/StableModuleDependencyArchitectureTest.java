package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class StableModuleDependencyArchitectureTest {
    @Test
    void coreHasNoOutboundProjectDependency() throws Exception {
        var core = ArchitecturePolicy.targetModels().get("rpdptw-core");
        assertEquals(Set.of(), ArchitecturePolicy.directRonextDependencies(core));
    }

    @Test
    void verificationDependsOnlyOnCoreAndNotSolverSearchOrLegacy() throws Exception {
        var verification = ArchitecturePolicy.targetModels().get("rpdptw-verification");
        assertEquals(
                Set.of("rpdptw-core"),
                ArchitecturePolicy.directRonextDependencies(verification));
        assertTrue(verification.dependencies().stream()
                .noneMatch(dependency -> Set.of(
                                "rpdptw-solver",
                                "legacy-gcp-placeholder",
                                "rpdptw-test-fixtures")
                        .contains(dependency.artifactId())));
    }

    @Test
    void solverAndApplicationUseOnlyReviewedEdges() throws Exception {
        var modules = ArchitecturePolicy.targetModels();
        assertEquals(
                Set.of("rpdptw-core"),
                ArchitecturePolicy.directRonextDependencies(modules.get("rpdptw-solver")));
        assertEquals(
                Set.of("rpdptw-core", "rpdptw-solver", "rpdptw-verification"),
                ArchitecturePolicy.directRonextDependencies(modules.get("rpdptw-application")));
    }
}
