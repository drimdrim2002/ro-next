package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PackageAndCustomerBoundaryArchitectureTest {
    @Test
    void modulesDoNotAccessAnotherModulesInternalPackages() throws Exception {
        Path repository = ArchitecturePolicy.repository();
        assertEquals(java.util.List.of(), ArchitecturePolicy.crossModuleInternalAccess(repository));
    }

    @Test
    void genericCoreSolverAndVerificationContainNoCustomerIdentityConditional() throws Exception {
        Path repository = ArchitecturePolicy.repository();
        assertEquals(java.util.List.of(), ArchitecturePolicy.customerConditionals(repository));
    }

    @Test
    void coreDoesNotReadAmbientEnvironmentClockRandomOrStaticMutableRegistry() throws Exception {
        Path repository = ArchitecturePolicy.repository();
        assertEquals(java.util.List.of(), ArchitecturePolicy.coreAmbientLeaks(repository));
    }
}
