package com.ronext.rpdptw;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.ronext.rpdptw",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule VERIFY_MUST_NOT_DEPEND_ON_SOLVE =
            noClasses()
                    .that()
                    .resideInAPackage("com.ronext.rpdptw.verify..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.ronext.rpdptw.solve..")
                    .allowEmptyShould(true);
}
