package com.ronext.rpdptw.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.ronext.rpdptw")
public class StableModuleDependencyArchitectureTest {

    @ArchTest
    static final ArchRule verification_does_not_depend_on_solver =
            noClasses()
                    .that().resideInAPackage("com.ronext.rpdptw.verification..")
                    .should().dependOnClassesThat().resideInAPackage("com.ronext.rpdptw.solver..");

    @ArchTest
    static final ArchRule profiles_do_not_depend_on_solver_app_verification_legacy =
            noClasses()
                    .that().resideInAPackage("com.ronext.rpdptw.profiles..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "com.ronext.rpdptw.solver..",
                            "com.ronext.rpdptw.application..",
                            "com.ronext.rpdptw.verification..",
                            "com.ronext.optimizer.."
                    );

    @ArchTest
    static final ArchRule core_does_not_depend_on_other_rpdptw_modules =
            noClasses()
                    .that().resideInAnyPackage(
                            "com.ronext.rpdptw.core..",
                            "com.ronext.rpdptw.domain..",
                            "com.ronext.rpdptw.evaluation..",
                            "com.ronext.rpdptw.input..",
                            "com.ronext.rpdptw.normalization..",
                            "com.ronext.rpdptw.propagation..",
                            "com.ronext.rpdptw.travel.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "com.ronext.rpdptw.solver..",
                            "com.ronext.rpdptw.verification..",
                            "com.ronext.rpdptw.application..",
                            "com.ronext.rpdptw.profiles.."
                    );
}
