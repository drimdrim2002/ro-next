package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

class TestFixtureArtifactContractTest {
    @Test
    void consumerUsesOnlyAttachedTestsClassifierAtTestScope() throws Exception {
        var architectureRules = ArchitecturePolicy.readModule(
                ArchitecturePolicy.repository().resolve("build/architecture-rules/pom.xml"));
        var fixtureEdges = architectureRules.dependencies().stream()
                .filter(dependency -> "rpdptw-test-fixtures".equals(dependency.artifactId()))
                .toList();

        assertEquals(1, fixtureEdges.size());
        var edge = fixtureEdges.getFirst();
        assertEquals("test", edge.scope());
        assertEquals("test-jar", edge.type());
        assertEquals("tests", edge.classifier());
        assertFalse(ArchitecturePolicy.detectsTestFixtureProductionLeak(edge));
    }

    @Test
    void stableProductionPomsDoNotDependOnFixtureArtifact() throws Exception {
        for (var module : ArchitecturePolicy.targetModels().values()) {
            assertTrue(module.dependencies().stream()
                    .noneMatch(dependency -> "rpdptw-test-fixtures".equals(dependency.artifactId())));
        }
    }

    @Test
    void attachedTestsJarContainsFixtureBytecodeAndMainJarDoesNot() throws Exception {
        Path target = ArchitecturePolicy.repository().resolve("build/test-fixtures/target");
        Path testsJar = target.resolve("rpdptw-test-fixtures-0.1.0-SNAPSHOT-tests.jar");
        Path mainJar = target.resolve("rpdptw-test-fixtures-0.1.0-SNAPSHOT.jar");
        assertTrue(Files.isRegularFile(testsJar), testsJar.toString());

        String entry = "com/ronext/rpdptw/fixture/package-info.class";
        try (JarFile tests = new JarFile(testsJar.toFile())) {
            assertTrue(tests.getEntry(entry) != null);
        }
        if (Files.isRegularFile(mainJar)) {
            try (JarFile main = new JarFile(mainJar.toFile())) {
                assertTrue(main.getEntry(entry) == null);
            }
        } else {
            assertTrue(ArchitecturePolicy.javaSources(
                    ArchitecturePolicy.repository().resolve("build/test-fixtures")).isEmpty());
        }
    }
}
