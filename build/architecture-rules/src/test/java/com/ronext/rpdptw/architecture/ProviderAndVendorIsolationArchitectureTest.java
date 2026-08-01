package com.ronext.rpdptw.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.ronext.rpdptw")
public class ProviderAndVendorIsolationArchitectureTest {

    @ArchTest
    static final ArchRule rpdptw_does_not_depend_on_provider_or_vendor_packages =
            noClasses()
                    .that().resideInAPackage("com.ronext.rpdptw..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "com.google.cloud..",
                            "software.amazon..",
                            "com.azure..",
                            "com.google.ortools..",
                            "com.ronext.optimizer.."
                    );
}
