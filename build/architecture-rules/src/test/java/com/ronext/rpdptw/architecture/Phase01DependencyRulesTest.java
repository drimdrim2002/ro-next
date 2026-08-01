package com.ronext.rpdptw.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.ronext.rpdptw")
public class Phase01DependencyRulesTest {

    @ArchTest
    static final ArchRule coreDoesNotDependOnJacksonCloudSolverOrVerification =
            noClasses().that().resideInAPackage("com.ronext.rpdptw.input..")
                    .or().resideInAPackage("com.ronext.rpdptw.normalization..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.fasterxml.jackson..",
                            "com.ronext.rpdptw.solver..",
                            "com.ronext.rpdptw.verification..",
                            "com.google.cloud..",
                            "software.amazon.awssdk.."
                    );

    @ArchTest
    static final ArchRule normalizationDoesNotCreatePreparedTravelOrSolveSnapshot =
            noClasses().that().resideInAPackage("com.ronext.rpdptw.normalization..")
                    .should().dependOnClassesThat()
                    .haveSimpleNameContaining("PreparedTravel");

    @ArchTest
    static final ArchRule adapterDoesNotDependOnSolver =
            noClasses().that().resideInAPackage("com.ronext.rpdptw.adapter.input..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.ronext.rpdptw.solver..");
}
