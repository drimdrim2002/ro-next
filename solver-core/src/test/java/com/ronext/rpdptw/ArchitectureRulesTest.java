package com.ronext.rpdptw;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * [아키텍처 자동 검증 테스트]
 * 
 * 개발자가 실수로 모듈 간의 레이어 규칙이나 의존성 경계를 어기지 않도록
 * ArchUnit 도구를 사용해 컴파일된 바이트코드를 자동으로 검사하는 프로젝트 가드레일입니다.
 * 평소 비즈니스 개발 시 직접 수정할 필요 없이 안전장치로 작동합니다.
 */
@AnalyzeClasses(
        packages = "com.ronext.rpdptw",
        // 테스트 코드(.Test)는 의존성 검사 대상에서 제외하고, 프로덕션 코드만 분석합니다.
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    /**
     * [규칙 1] 'verify'(독립 재검증) 패키지는 'solve'(탐색/ALNS 엔진) 패키지를 절대로 참조할 수 없습니다.
     * 
     * - 이유: 재검증 로직이 탐색 엔진의 내부 상태나 캐시, 탐색 가중치에 오염되지 않고
     *        순수하게 독립적으로 결과의 정당성을 검증해야 하기 때문입니다.
     */
    @ArchTest
    static final ArchRule VERIFY_MUST_NOT_DEPEND_ON_SOLVE =
            noClasses()
                    // 대상: com.ronext.rpdptw.verify 하위의 모든 클래스는
                    .that()
                    .resideInAPackage("com.ronext.rpdptw.verify..")
                    // 금지 사항: com.ronext.rpdptw.solve 하위의 클래스에 의존해서는 안 됨
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.ronext.rpdptw.solve..")
                    // 설정: 아직 패키지가 비어 있어도 테스트가 실패하지 않고 통과하도록 허용
                    .allowEmptyShould(true);
}

