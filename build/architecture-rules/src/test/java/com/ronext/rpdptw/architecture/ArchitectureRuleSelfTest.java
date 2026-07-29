package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArchitectureRuleSelfTest {
    @Test
    void forbiddenProviderFixtureIsRejectedWithoutMatchingCommentsOrStrings() {
        assertTrue(ArchitecturePolicy.detectsProviderReference(
                "package bad; import software.amazon.awssdk.services.s3.S3Client; class Leak {}"));
        assertTrue(ArchitecturePolicy.detectsProviderReference(
                "package bad; class Leak { com.google.ortools.Loader value; }"));
        assertFalse(ArchitecturePolicy.detectsProviderReference(
                "/** software.amazon.awssdk is forbidden. */ class Clean { String note = \"com.google.ortools\"; }"));
    }

    @Test
    void coreAmbientAndStaticMutableFixturesAreRejected() {
        assertTrue(ArchitecturePolicy.detectsCoreAmbientLeak(
                "class Bad { String read() { return System.getenv(\"SECRET\"); } }"));
        assertTrue(ArchitecturePolicy.detectsCoreAmbientLeak(
                "class Bad { long now() { return System.currentTimeMillis(); } }"));
        assertTrue(ArchitecturePolicy.detectsCoreAmbientLeak(
                "class Bad { static java.util.Map registry = new java.util.HashMap(); }"));
    }

    @Test
    void crossModuleInternalFixtureIsRejected() {
        assertTrue(ArchitecturePolicy.detectsCrossModuleInternal(
                "import com.ronext.rpdptw.solver.internal.MutableSearchState; class Bad {}",
                "com.ronext.rpdptw.solver.internal"));
        assertFalse(ArchitecturePolicy.detectsCrossModuleInternal(
                "/** com.ronext.rpdptw.solver.internal is forbidden. */ class Clean {}",
                "com.ronext.rpdptw.solver.internal"));
    }

    @Test
    void testFixtureProductionLeakFixtureIsRejected() {
        assertTrue(ArchitecturePolicy.detectsTestFixtureProductionLeak(
                new ArchitecturePolicy.Dependency(
                        "com.ronext",
                        "rpdptw-test-fixtures",
                        "compile",
                        "jar",
                        null)));
        assertFalse(ArchitecturePolicy.detectsTestFixtureProductionLeak(
                new ArchitecturePolicy.Dependency(
                        "com.ronext",
                        "rpdptw-test-fixtures",
                        "test",
                        "test-jar",
                        "tests")));
    }

    @Test
    void customerConditionalAndSwitchFixturesAreRejected() {
        assertTrue(ArchitecturePolicy.detectsCustomerConditional(
                "class Bad { void f(String customerId) { if (customerId.equals(\"fresh-chain\")) {} } }"));
        assertTrue(ArchitecturePolicy.detectsCustomerConditional(
                "class Bad { void f(String customerName) { switch (customerName) { default -> {} } } }"));
        assertTrue(ArchitecturePolicy.detectsCustomerConditional(
                "class Bad { void f(String presetName) { if (presetName.contains(\"outsourcing\")) {} } }"));
    }

    @Test
    void capabilityAdvertisementFixturesAreRejected() {
        assertTrue(ArchitecturePolicy.detectsCapabilityAdvertisement(
                "src/main/resources/META-INF/services/com.example.RouteSelectionProvider",
                "com.example.UnapprovedProvider"));
        assertTrue(ArchitecturePolicy.detectsCapabilityAdvertisement(
                "src/main/java/example/Bad.java",
                "class Bad { RouteSelectionCapability capability; }"));
        assertFalse(ArchitecturePolicy.detectsCapabilityAdvertisement(
                "src/main/java/example/Clean.java",
                "/** RouteSelectionCapability is not present. */ class Clean {}"));
    }
}
