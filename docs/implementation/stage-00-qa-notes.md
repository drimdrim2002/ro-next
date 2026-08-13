# Stage 0 (정리와 뼈대) 구현 및 테스트 해설 노트

> **참조 문서:** [`docs/implementation/stage-00-cleanup-and-skeleton.md`](file:///Users/brown/workspace/ro-next/docs/implementation/stage-00-cleanup-and-skeleton.md)

---

## 1. Stage 0 작업의 목적 (Why)

1. **레거시 및 프로토타입 잔재 청소**
   - 기존의 단일 모듈 구조에 있던 임시 placeholder 코드(`com.ronext.optimizer.*`), GCP(Cloud Build, Workflows 등) 연동 잔재, 구 단일 모듈 전용 `Dockerfile`을 완전히 제거하여 프로젝트를 깨끗한 초기 상태로 전환했습니다.

2. **3계층 Multi-Module 아키텍처 수립**
   - 전체 시스템 아키텍처 설계([`docs/architecture-design.md`](file:///Users/brown/workspace/ro-next/docs/architecture-design.md))에 명시된 **3개의 모듈 (`solver-core`, `solver-profile`, `app`)** 구조를 확정하고, 의존성 방향(`app → solver-profile → solver-core`)을 고정했습니다.

3. **엄격한 기술 표준 및 모듈 순수성 보장**
   - **`solver-core`**: Pure Java 25 기반 (외부 compile 라이브러리 의존성 0개). 알고리즘과 도메인 로직이 외부 기술에 오염되지 않도록 격리했습니다.
   - **`solver-profile`**: 고객별 커스텀 배차 제약/정책 SPI.
   - **`app`**: Spring Boot 4.1.0 기반 단일 실행/배포(AWS ECS Fargate) 단위. REST API, S3 저장 어댑터, 실행기(Executor) 전용 모듈.

4. **자동화된 검증 기틀 (DoD) 마련**
   - 모듈 간 경계 규칙 위반을 감지하는 ArchUnit 테스트 및 GCP 의존성을 차단하는 Maven Enforcer 규칙을 최초 도입했습니다.

---

## 2. 생성된 패키지 및 `package-info.java`의 역할

각 모듈 하위에 생성된 `package-info.java` 파일들은 다음과 같은 3가지 목적을 가집니다:

1. **빈 패키지 구조 유지 및 Git 추적 (Git Directory Tracking)**
   - Git은 빈 디렉터리를 추적하지 않으므로, Stage 0 시점에 실제 프로덕션 클래스가 없더라도 프로젝트 패키지 뼈대를 미리 세우고 커밋하기 위한 목적입니다.
2. **패키지별 역할 및 구현 시점 명시 (Architecture Contract)**
   - 각 `package-info.java`의 Javadoc 주석에 해당 패키지의 책임과 향후 어느 Stage에서 클래스가 추가되는지 명시되어 있습니다.
     - `com.ronext.rpdptw.domain`: 도메인 표준 모델 (Stage 1)
     - `com.ronext.rpdptw.problem`: 문제 동결 및 이동 매트릭스 (Stage 2)
     - `com.ronext.rpdptw.eval`: 평가 계약 및 프로필 SPI (Stage 3)
     - `com.ronext.rpdptw.solve`: 탐색/ALNS 엔진 (Stage 3–4)
     - `com.ronext.rpdptw.verify`: 독립 재검증 (Stage 5)
     - `com.ronext.rpdptw.app.api/run/input/storage`: Spring Boot 접수/실행/JSON어댑터/S3저장소 (Stage 6)
3. **향후 패키지 수준 어노테이션 적용 대비**
   - 자바 표준에 따라 패키지 단위 어노테이션(예: JSpecify, Nullability 선언 등)을 작성할 수 있는 자리를 마련했습니다.

---

## 3. 테스트 코드 상세 해설 (Test Suite)

Stage 0에서는 **총 2개 클래스 (3개 테스트 메서드)**의 테스트가 작성되었습니다.

### ① [`ArchitectureRulesTest.java`](file:///Users/brown/workspace/ro-next/solver-core/src/test/java/com/ronext/rpdptw/ArchitectureRulesTest.java) (`solver-core` 모듈)
* **사용 도구**: ArchUnit (자바 바이트코드 검증 도구)
* **목적**: `verify`(독립 재검증) 패키지가 `solve`(탐색/ALNS 엔진) 패키지를 절대로 참조하지 못하도록 컴파일/테스트 단계에서 강제하는 **아키텍처 자동 가드레일**입니다.
* **도메인 배경**:
  - `solve`: 최적화 탐색 알고리즘, 이동 연산자, 탐색 예산(`AlnsConfig`), 캐시 상태가 존재하는 곳입니다.
  - `verify`: 만들어진 최종 배차안이 비즈니스 제약을 위반하지 않았는지 독립적으로 검증하는 곳입니다.
  - 재검증 로직이 탐색 알고리즘 내부의 임시 상태나 가중치에 오염되지 않도록 아키텍처 수준에서 물리적으로 오염을 방지합니다.

```java
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
                    .allowEmptyShould(true); // 빈 패키지 상태에서도 빌드 성공 허용
}
```

---

### ② [`RoNextApplicationTest.java`](file:///Users/brown/workspace/ro-next/app/src/test/java/com/ronext/rpdptw/app/RoNextApplicationTest.java) (`app` 모듈)
* **사용 도구**: `@SpringBootTest`, `TestRestTemplate`, AssertJ
* **목적**: Spring Boot 애플리케이션의 정상 구동 및 Actuator 헬스체크 응답을 검증하는 **E2E 통합 테스트**입니다.
* **구현 내용**:
  1. `contextLoads()`: 스프링 컨텍스트(설정, 빈 등록)가 에러 없이 정상 로딩되는지 확인 (T2)
  2. `healthEndpointRespondsUp()`: 무작위 내장 포트로 웹 서버를 기동 후 `GET /actuator/health` 요청을 보내 HTTP status `200 OK` 및 body `status == "UP"` 확인 (T3)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class RoNextApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // Spring context starts (T2)
    }

    @Test
    void healthEndpointRespondsUp() {
        ResponseEntity<Map> response =
                restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }
}
```

---

## 4. DoD (Definition of Done) 검증 명령어

프로젝트 루트에서 아래 명령을 통해 전체 모듈 빌드 및 테스트 합격 여부를 언제든지 검증할 수 있습니다:

```bash
# 전체 모듈 빌드 및 테스트 검증
mvn verify

# GCP 의존성 0건 검증
mvn dependency:list -DincludeGroupIds=com.google.cloud \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — com.google.cloud 좌표 0건"

# solver-core 순수성(compile 의존 0건) 검증
mvn dependency:list -pl solver-core -DincludeScope=compile \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — compile 의존 0건"
```
