package com.ronext.rpdptw.architecture;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.ronext.rpdptw")
public class PackageBoundaryArchitectureTest {

    @ArchTest
    static final ArchRule no_cross_module_internal_package_access =
            noClasses()
                    .that().resideInAPackage("com.ronext.rpdptw..")
                    .should(accessInternalPackageOfAnotherModule());

    private static ArchCondition<JavaClass> accessInternalPackageOfAnotherModule() {
        return new ArchCondition<>("access internal packages of another module") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaAccess<?> access : javaClass.getAccessesFromSelf()) {
                    JavaClass target = access.getTargetOwner();
                    String targetPkg = target.getPackageName();
                    if (targetPkg.contains(".internal") || targetPkg.endsWith(".internal")) {
                        int idx = targetPkg.indexOf(".internal");
                        String modulePrefix = targetPkg.substring(0, idx);
                        String originPkg = javaClass.getPackageName();
                        if (!originPkg.startsWith(modulePrefix)) {
                            String message = String.format(
                                    "Class %s in package %s accesses internal class %s in package %s",
                                    javaClass.getName(), originPkg, target.getName(), targetPkg
                            );
                            events.add(SimpleConditionEvent.violated(javaClass, message));
                        }
                    }
                }
            }
        };
    }
}
