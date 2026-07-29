package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProviderAndVendorIsolationArchitectureTest {
    @Test
    void stableModulesDoNotReferenceCloudTransportLegacyOrVendorApis() throws Exception {
        Path repository = ArchitecturePolicy.repository();
        assertEquals(
                java.util.List.of(),
                ArchitecturePolicy.forbiddenStableSourceReferences(repository));
        assertEquals(
                java.util.List.of(),
                ArchitecturePolicy.forbiddenStableBytecodeReferences(repository));
    }

    @Test
    void defaultReactorAdvertisesNoRouteSelectionCapability() throws Exception {
        Path repository = ArchitecturePolicy.repository();
        assertFalse(Files.exists(repository.resolve("adapters/route-selection-ortools-cpsat")));
        assertTrue(ArchitecturePolicy.findFiles(
                        repository.resolve("rpdptw"),
                        path -> path.toString().contains("META-INF/services")
                                || path.getFileName().toString().contains("RouteSelection"))
                .isEmpty());
    }
}
