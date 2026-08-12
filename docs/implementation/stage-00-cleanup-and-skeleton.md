---
title: Stage 0 — 정리와 뼈대 (상세 구현 설계)
stage: 0
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../architecture-design.md (§2 모듈, §2.3 계층 기준, §4 Spring 규칙, §7 마이그레이션 메모)
  - ../implementation-plan.md (Stage 0)
revisions:
  - 2026-08-09 최초 작성 (2모듈)
  - 2026-08-10 3계층 확정 반영 — 모듈 3개, core에 `eval` 패키지, `profile`은 별도 모듈
  - 2026-08-10 외부 검토 반영 — Spring Boot 4.1로 상향(§4.4·E7·Plan), POM 버전 해석 경로 명시(§4.1),
    §6 근거 절 보정, Q2에 e2e 선행 조건 추가. 검토 노트 절은 본문 흡수 후 삭제
  - 2026-08-10 Jackson 3 전 문서 기준 확정 · Dockerfile C4/Plan 동기화(Q1 해소) · C4 포인터 보정
  - 2026-08-10 구현 모호성 제거 — 버전 숫자·전체 POM·소스·테스트·README·작업 순서를 본문에 고정
  - 2026-08-10 Boot 4.1 구현 반영 — `TestRestTemplate`을 `spring-boot-resttestclient` +
    `@AutoConfigureTestRestTemplate`로 수정 (§4.4·§5.5; starter-test에서 분리됨)
  - 2026-08-10 §8 V2·V4 검증 명령 수정 — `-q`가 `dependency:list`의 INFO 출력을 통째로 죽여
    두 검사가 **항상 빈 출력 = 통과**로 보이던 문제. `-q` 제거 + 좌표 grep·V4 대조군 추가
  - 2026-08-10 §11 Q2(multiRotation)를 Plan §2.1 D1으로 이관 — 포인터만 변경, 설계 무변경
  - 2026-08-10 **D1 확정 반영 — §11 Q2 해소**: `multiRotation` = 바퀴 수이므로 fixture의 `"1"`은
    지원 범위 안(통과 = `{0,1}`). Q3 문면도 "2회전 = 값 2"로 정합. 파일 목록·DoD 무변경
  - 2026-08-12 Domain 2026-08-12 개정(self arc sentinel·startDepot 부재 규칙·수치 number
    인코딩) 정합 — §11 Q2 해소 문면의 fixture 값 인용을 number 표기(`1`)로. 파일 목록·DoD 무변경
  - 2026-08-13 감사 결함 정정 (분할 5 #6) — §11 세 질문이 전부 종결됐음을 절 서두에 명시하고
    Q1·Q3에 해소 날짜 표기 (README 공통 규칙 "표시하고 남김" 이행). 설계 무변경
---

# Stage 0 — 정리와 뼈대

구 placeholder 코드와 GCP 잔재를 걷어내고, [Architecture §2](../architecture-design.md)의
3모듈 구조(`solver-core` + `solver-profile` + `app`)를 빈 상태로 세운다. 이 문서의
이름·경로·좌표·버전·파일 내용이 이후 모든 Stage의 **이름 기준**이자 Stage 0의 **구현 계약**이다.

**구현 재량 없음.** 아래 표·코드 블록에 없는 선택(다른 artifactId, 다른 Boot 라인,
Jackson 2, shade 유지, health 컨트롤러 추가 등)은 하지 않는다. 버전 상향이 필요하면
**이 문서를 먼저 개정**한 뒤 구현한다.

**DoD** ([Plan Stage 0](../implementation-plan.md)): `mvn verify` 통과 ·
`app` 기동 후 health 응답 · GCP 의존성 0.

---

## 0. 고정 버전표 (구현 시 그대로 기입)

| property / 좌표 | 값 | 근거 |
|---|---|---|
| `project.version` | `0.1.0-SNAPSHOT` | 기존 유지 |
| `maven.compiler.release` | `25` | `.sdkmanrc` · enforcer |
| enforcer Java | `[25,26)` | 기존 유지 |
| enforcer Maven | `[3.9.14,)` | 기존 유지 |
| `spring-boot.version` | **`4.1.0`** | Boot 4.1 라인 · Java 17–26 · OSS ~2027-07-31. 패치 상향 시 이 표와 §4를 함께 개정 |
| `junit.version` | **`6.0.3`** | Boot 4.1.0 BOM의 `junit-jupiter.version`과 동일 — core/profile test와 app test 정렬 |
| `archunit.version` | **`1.5.0`** | 2026-08 시점 Central release |
| `maven-enforcer-plugin` | `3.6.1` | 기존 pom 유지 |
| `maven-compiler-plugin` | `3.14.1` | 기존 pom 유지 |
| `maven-surefire-plugin` | `3.5.4` | 기존 pom 유지 |

JSON 스택: Boot 4.1.0 기본 **Jackson 3** (`tools.jackson`, `JsonMapper`). Stage 0 코드는
JSON을 직접 쓰지 않는다. Stage 6이 쓴다 (Architecture §4 · 본 문서 §4.4).

---

## 1. 현재 상태 인벤토리 (2026-08-09 기준)

작성 시점에 실제 확인한 내용이다. 정리 대상을 추측이 아니라 목록으로 고정한다.

| 경로 | git 추적 | 내용 |
|---|---|---|
| `pom.xml` | 추적 | 단일 모듈. GCP(workflow-executions·cloud-storage)·Jackson 의존, shade 플러그인, main class `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer` |
| `src/` | 추적 (7파일) | `com.ronext.optimizer.*` placeholder — `AlnsBatchEngine`(합성 데모), 수제 HTTP 서버 |
| `gcp/` | 추적 (3파일) | cloudbuild·Workflows 잔재 |
| `Dockerfile` | 추적 | 구 `src/` 단일 모듈 shade jar 전용 빌드 |
| `rpdptw/` `adapters/` `apps/` `build/` | **미추적** | 폐기된 15-phase 빌드의 `target/` 산출물만 존재 (소스 없음 — 확인 완료) |
| `.serverless/` `node_modules/` `target/` | **미추적** | 과거 실험 잔재·빌드 산출물 (`.gitignore`에 이미 포함) |
| `scripts/` | 추적 | `floor_win_poc_matrix.py` — floor fixture 생성 도구. **유지** |
| `data/` `docs/` | 추적 | 규약 PDF·fixture·설계 문서. **유지** |
| `README.md` | 추적 | "현재 코드 상태 (주의)" 절이 placeholder를 설명 — 갱신 대상 |
| `.gitignore` `.sdkmanrc` `.dockerignore` | 추적 | **유지**. `.sdkmanrc`: java 25.0.3-amzn, maven 3.9.14. `.dockerignore`는 C4 후 Stage 7까지 쓰이지 않으나 내용(`target`·`node_modules` 등)은 유효 — Stage 7도 변경하지 않음 |

---

## 2. 정리 작업

근거: [Architecture §7 마이그레이션 메모](../architecture-design.md), [Plan Stage 0](../implementation-plan.md).

| # | 대상 | 조치 | 비고 |
|---|---|---|---|
| C1 | `pom.xml` | parent pom으로 **재작성** (§4.1 전체 XML) | 부분 편집 금지 — 구 의존·shade를 남기지 않는다 |
| C2 | `src/` | `git rm -r src` | placeholder 전체. `AlnsBatchEngine` 데모는 완료 근거 아님 (Architecture §7) |
| C3 | `gcp/` | `git rm -r gcp` | Architecture §7 명시 |
| C4 | `Dockerfile` | `git rm Dockerfile` | 구 단일 모듈·`src/` shade 전용 — 죽은 파일. Stage 7 재작성 (Plan Stage 0 정리 행) |
| C5 | `rpdptw/` `adapters/` `apps/` `build/` | `rm -rf rpdptw adapters apps build` (로컬만) | 미추적이므로 커밋 없음 |
| C6 | `.serverless/` `node_modules/` `target/` | `rm -rf .serverless node_modules target` (로컬만) | 〃 |
| C7 | `README.md` | §7 최종 본문으로 **교체** | 절 단위 패치가 아니라 §7 전체를 따른다 |

`.gitignore`는 그대로 둔다 — `target/`(전 depth)·`node_modules/` 등 항목이 계속 유효하고,
`.serverless/`·`.gcloud/` 줄이 남아 있어도 무해하다.

---

## 3. 만드는 파일 전체 목록 (커밋 대상)

Architecture §2의 트리를 그대로 따른다. **아래 경로만** 새로 만들고, 목록에 없는 파일은 만들지 않는다.

```text
ro-next/
├── pom.xml
├── solver-core/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/ronext/rpdptw/
│       │   ├── domain/package-info.java
│       │   ├── problem/package-info.java
│       │   ├── eval/package-info.java
│       │   ├── solve/package-info.java
│       │   └── verify/package-info.java
│       └── test/java/com/ronext/rpdptw/
│           └── ArchitectureRulesTest.java
├── solver-profile/
│   ├── pom.xml
│   └── src/main/java/com/ronext/rpdptw/profile/
│       └── package-info.java
│   # test/ 디렉터리는 만들지 않는다 (Stage 3이 필요 시 추가). git은 빈 폴더를 추적하지 않는다.
└── app/
    ├── pom.xml
    └── src/
        ├── main/java/com/ronext/rpdptw/app/
        │   ├── RoNextApplication.java
        │   ├── api/package-info.java
        │   ├── run/package-info.java
        │   ├── input/package-info.java
        │   └── storage/package-info.java
        ├── main/resources/application.yml
        └── test/java/com/ronext/rpdptw/app/
            └── RoNextApplicationTest.java
```

- **`solver-profile`은 Stage 0 시점에 고객 구현 0개**다. 그래도 모듈은 지금 만든다 — Stage 0이
  pom을 쓰는 유일한 시점이다.
- `package-info.java` 본문은 §5.1 고정 문자열을 그대로 쓴다 (어노테이션 없음).

### 3.1 이름 기준 (이후 Stage가 이어받는 것)

| 항목 | 값 | 근거 |
|---|---|---|
| Maven groupId | `com.ronext` | 기존 유지 |
| parent | `com.ronext:ro-next` (packaging `pom`, `0.1.0-SNAPSHOT`) | 기존 좌표 유지 |
| core 모듈 | 디렉터리 `solver-core/`, artifactId `ro-next-solver-core` | Architecture §2 |
| profile 모듈 | 디렉터리 `solver-profile/`, artifactId `ro-next-solver-profile` | Architecture §2 |
| app 모듈 | 디렉터리 `app/`, artifactId `ro-next-app` | Architecture §2 |
| core 루트 패키지 | `com.ronext.rpdptw` (하위: `domain`·`problem`·`eval`·`solve`·`verify`) | Architecture §2 |
| profile 루트 패키지 | `com.ronext.rpdptw.profile` (하위: 고객별) | Architecture §2 |
| app 루트 패키지 | `com.ronext.rpdptw.app` (하위: `api`·`run`·`input`·`storage`) | Architecture §2 |
| 모듈 의존 방향 | `app → solver-profile → solver-core` (한 방향) | Architecture §2 |
| 진입점 클래스 | `com.ronext.rpdptw.app.RoNextApplication` | 본 문서 확정 |
| 경계 테스트 클래스 | `com.ronext.rpdptw.ArchitectureRulesTest` (solver-core test) | Architecture §2.1 — Stage 5가 규칙 추가 시 이 클래스에 |
| Java / Maven | 25 (`[25,26)`) / 3.9.14+ | 기존 `.sdkmanrc`·enforcer 유지 |

구 패키지 `com.ronext.optimizer`는 폐기한다. 어떤 Stage도 재사용하지 않는다.

`eval`이라는 이름은 "평가 계약"을 뜻한다 — 사실 값(`RouteFacts` 등)·중립 지표(`Evaluation`)·
profile SPI·기본 구현이 여기 있다. **고객 정책은 여기 없다** (그건 `solver-profile`).

---

## 4. POM 상세 (전체 XML — 이 내용으로 파일을 쓴다)

`<relativePath>`는 적지 않는다 — Maven 기본값 `../pom.xml`이 이 배치에 맞다.

### 4.1 parent `pom.xml` (루트)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ronext</groupId>
    <artifactId>ro-next</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>ro-next</name>
    <description>RPDPTW solver service (ECS Fargate, S3) — parent</description>

    <modules>
        <module>solver-core</module>
        <module>solver-profile</module>
        <module>app</module>
    </modules>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>4.1.0</spring-boot.version>
        <junit.version>6.0.3</junit.version>
        <archunit.version>1.5.0</archunit.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter</artifactId>
                <version>${junit.version}</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>com.tngtech.archunit</groupId>
                <artifactId>archunit-junit5</artifactId>
                <version>${archunit.version}</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.14.1</version>
                    <configuration>
                        <release>${maven.compiler.release}</release>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.5.4</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-enforcer-plugin</artifactId>
                <version>3.6.1</version>
                <executions>
                    <execution>
                        <id>enforce-build-toolchain</id>
                        <goals><goal>enforce</goal></goals>
                        <configuration>
                            <rules>
                                <requireJavaVersion>
                                    <version>[25,26)</version>
                                </requireJavaVersion>
                                <requireMavenVersion>
                                    <version>[3.9.14,)</version>
                                </requireMavenVersion>
                                <bannedDependencies>
                                    <excludes>
                                        <exclude>com.google.cloud:*</exclude>
                                    </excludes>
                                </bannedDependencies>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 4.1.1 버전 해석 경로 (이 표와 위 XML이 계약)

| 대상 | 어디에 쓰는가 | 이유 |
|---|---|---|
| JUnit·ArchUnit | parent `dependencyManagement`에 버전 포함. child는 GAV + scope만 | 한 곳에서 관리 |
| enforcer | parent `<build><plugins>` | `pluginManagement`만 두면 **실행되지 않음** |
| compiler·surefire | parent `pluginManagement`만 | jar 기본 lifecycle에 버전·설정 적용. child에 재선언하지 않음 |
| `spring-boot.version` | parent `<properties>` | app BOM import·plugin이 `${spring-boot.version}`으로 참조 |
| `spring-boot-maven-plugin` | **app** pom에 version 명시 | BOM `import`는 dependencyManagement만 가져옴 |

구 parent에 있던 것 — GCP 2건, Jackson 2건, 루트 JUnit 직접 의존, shade 플러그인,
"Cloud Run / Google Cloud Workflows" description — **전부 제거** (위 XML에 없음).

### 4.2 `solver-core/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.ronext</groupId>
        <artifactId>ro-next</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>ro-next-solver-core</artifactId>
    <name>ro-next-solver-core</name>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit-junit5</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- compile/runtime 의존 **0**. Spring·Jackson·AWS SDK·고객 라이브러리 **넣지 않는다**.
- 고객 전용 라이브러리는 `solver-profile/pom.xml`에만 선언 (Stage 0에는 추가하지 않음).

### 4.3 `solver-profile/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.ronext</groupId>
        <artifactId>ro-next</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>ro-next-solver-profile</artifactId>
    <name>ro-next-solver-profile</name>

    <dependencies>
        <dependency>
            <groupId>com.ronext</groupId>
            <artifactId>ro-next-solver-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- Stage 0 프로덕션 파일은 `package-info.java` 하나. Spring 의존 **없음**.
- JUnit은 선언만 하고 Stage 0에는 테스트 클래스를 두지 않는다 (빈 test 소스 트리 없음 → surefire tests=0 통과).

### 4.4 `app/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.ronext</groupId>
        <artifactId>ro-next</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>ro-next-app</artifactId>
    <name>ro-next-app</name>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>com.ronext</groupId>
            <artifactId>ro-next-solver-profile</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.ronext</groupId>
            <artifactId>ro-next-solver-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-resttestclient</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-restclient</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- data·jpa·redis·cloud 스타터 **금지**. S3 SDK는 Stage 6.
- JSON은 starter-web → starter-json의 **Jackson 3**. 별도 jackson 의존·`spring-boot-jackson2` **금지**.
- app이 core를 직접 선언하는 것은 방향 위반이 아니다 (transitive 의존 금지).
- Boot 4에서 `TestRestTemplate`은 `spring-boot-starter-test`에 포함되지 않는다.
  app pom에 **test scope** `spring-boot-resttestclient` + `spring-boot-restclient`
  (`RestTemplateBuilder`)를 추가한다 (§5.5).

**왜 Boot 3이 아니라 4.1.0인가**

| 라인 | OSS 지원 | Java | 판단 |
|---|---|---|---|
| 3.5 (최종 3.5.16) | 2026-06-30 종료 | 17–25 | EOL — 신규 고정 금지 |
| 4.0 (4.0.7) | 2026-12-31 | 17–25 | 단기 |
| **4.1.0** | **2027-07-31** | **17–26** | **채택 (본 Stage 고정 값)** |

---

## 5. 소스·리소스 전문 (이 내용으로 파일을 쓴다)

### 5.1 `package-info.java` (어노테이션 없음 — E2)

각 파일 내용은 **아래 한 블록 전체**다.

`solver-core/.../domain/package-info.java`
```java
/**
 * Canonical input model, normalization, and delivery policy (Domain §1–4). Types added in Stage 1.
 */
package com.ronext.rpdptw.domain;
```

`solver-core/.../problem/package-info.java`
```java
/**
 * Problem freeze, validation, and travel tables (Domain §5). Types added in Stage 2.
 */
package com.ronext.rpdptw.problem;
```

`solver-core/.../eval/package-info.java`
```java
/**
 * Evaluation contracts, profile SPI, and default implementations (Domain §7.3·§8). Types added in Stage 3.
 */
package com.ronext.rpdptw.eval;
```

`solver-core/.../solve/package-info.java`
```java
/**
 * Solution, propagation, ALNS, and search settings (Domain §6–9). Types added in Stages 3–4.
 */
package com.ronext.rpdptw.solve;
```

`solver-core/.../verify/package-info.java`
```java
/**
 * Independent re-verification (Domain §10). Types added in Stage 5.
 */
package com.ronext.rpdptw.verify;
```

`solver-profile/.../profile/package-info.java`
```java
/**
 * ProfileRegistry and customer-specific Profile implementations (Domain §8.4). Types added in Stage 3.
 */
package com.ronext.rpdptw.profile;
```

`app/.../api/package-info.java`
```java
/**
 * REST accept and query endpoints (Architecture §3). Types added in Stage 6.
 */
package com.ronext.rpdptw.app.api;
```

`app/.../run/package-info.java`
```java
/**
 * SolveExecutor and in-process solve pipeline (Architecture §3.2). Types added in Stage 6.
 */
package com.ronext.rpdptw.app.run;
```

`app/.../input/package-info.java`
```java
/**
 * Wire JSON ↔ canonical adapter (Jackson 3). Types added in Stage 6.
 */
package com.ronext.rpdptw.app.input;
```

`app/.../storage/package-info.java`
```java
/**
 * SolveStore interface and S3/local implementations (Architecture §3.5). Types added in Stage 6.
 */
package com.ronext.rpdptw.app.storage;
```

### 5.2 `RoNextApplication.java`

경로: `app/src/main/java/com/ronext/rpdptw/app/RoNextApplication.java`

```java
package com.ronext.rpdptw.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoNextApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoNextApplication.class, args);
    }
}
```

### 5.3 `application.yml`

경로: `app/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: ro-next
```

- 운영 키(버킷·동시 실행 수 등)는 Stage 6. **이 파일에 다른 키를 추가하지 않는다.**
- health: actuator 기본 `GET /actuator/health`만 사용. 컨트롤러 없음.
  `management.endpoints.web.exposure.include` **추가 금지** (기본이 `health` 하나).

### 5.4 `ArchitectureRulesTest.java`

경로: `solver-core/src/test/java/com/ronext/rpdptw/ArchitectureRulesTest.java`

```java
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
```

- `allowEmptyShould(true)` 필수 (E1). Stage 5 이후 규칙은 **이 클래스에** `@ArchTest` 필드로 추가.
- 별도 architecture-rules 모듈 **만들지 않음**.

이 규칙이 막는 것 (Architecture §2.1):

1. 재검증이 탐색 증분 캐시·내부 상태를 참조
2. 재검증이 탐색 예산(`AlnsConfig` ∈ `solve`)을 참조  
   Domain §2.5.1 MUST NOT + Architecture §2.1·§2.3 배치가 맞물린 결과.

모듈 경계는 Maven 의존 방향이 막는다 (ArchUnit 추가 없음).

### 5.5 `RoNextApplicationTest.java`

경로: `app/src/test/java/com/ronext/rpdptw/app/RoNextApplicationTest.java`

```java
package com.ronext.rpdptw.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

- T2 = `contextLoads`, T3 = `healthEndpointRespondsUp`. 메서드 이름·단언을 바꾸지 않는다.
- AssertJ는 `spring-boot-starter-test`가 제공한다.
- Boot 4: 패키지 `org.springframework.boot.resttestclient`, 주입에 `@AutoConfigureTestRestTemplate` 필요.
  app pom test scope: `spring-boot-resttestclient` + `spring-boot-restclient` (§4.4).

---

## 6. README 갱신 (C7 — 최종 본문)

루트 `README.md`를 **아래 전문으로 교체**한다 (소개·설계 링크·기술 기준을 이 기준으로 맞춤).
(아래 펜스는 문서 표시용 4백틱 — 파일에 쓸 때는 안쪽 내용만 저장한다.)

````markdown
# ro-next

배차 최적화(RPDPTW) 서비스. 규약 JSON을 접수해 ALNS로 배차안을 만들고, 독립 재검증을 통과한
결과만 S3에 저장하는 **단일 Spring Boot 서비스** (AWS ECS Fargate)다.

## 설계 문서

- **진입점:** [`docs/README.md`](docs/README.md)
- 현행 4문서: [Master](docs/master-design.md) · [Domain](docs/domain-design.md) ·
  [Architecture](docs/architecture-design.md) · [Implementation Plan](docs/implementation-plan.md)
  (2026-08-09 확정 · 2026-08-10 3계층·Boot 4.1 개정)
- 구현 Stage 상세: [docs/implementation/README.md](docs/implementation/README.md)
- 과거 설계는 전부 [`docs/deprecated/`](docs/deprecated/) — 효력 없음

## 기술 기준 (확정 설계)

- Java 25 (`.sdkmanrc`로 고정) · Maven 3.9.14+
- 모듈 3개 (3계층): `solver-core`(순수 Java, compile 의존 0) + `solver-profile`(고객 정책) +
  `app`(**Spring Boot 4.1.0**, ECS 배포 단위) · JSON은 **Jackson 3** (`JsonMapper`)
- 저장: **Amazon S3만** (RDB·Redis 없음)
- 배포: **AWS ECS Fargate** 단일 서비스 (Lambda·Step Functions 사용하지 않음)

## 모듈 구조

| 모듈 | 책임 |
|---|---|
| `solver-core` | RPDPTW 도메인·탐색·재검증 (외부 라이브러리 compile 의존 0) |
| `solver-profile` | 고객별 `Profile` 구현 · `ProfileRegistry` |
| `app` | Spring Boot 접수/조회 API · executor · 저장 adapter |

진행 상태: [docs/implementation/README.md](docs/implementation/README.md)

## 빌드 · 기동

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn verify
mvn spring-boot:run -pl app
# 다른 터미널
curl -s localhost:8080/actuator/health
```
````

---

## 7. 구현 순서 (이 순서대로)

작업 디렉터리: 저장소 루트. JDK/Maven은 `.sdkmanrc` (`sdk env`).

1. **로컬 잔재 삭제 (커밋 없음)**  
   `rm -rf rpdptw adapters apps build .serverless node_modules target`
2. **추적 대상 삭제**  
   `git rm -r src gcp`  
   `git rm Dockerfile`
3. **parent `pom.xml`** — §4.1 XML 전체로 덮어쓰기
4. **모듈 디렉터리·pom** — §4.2·§4.3·§4.4 파일 생성
5. **소스** — §5.1–§5.5 파일 생성 (`solver-profile`에 test 디렉터리 **만들지 않음**)
6. **README** — §6 전문으로 교체
7. **검증 (DoD)** — §8 표 순서대로 실행. 전부 green이면 Stage 0 완료.

커밋 범위(권장): C2–C4 삭제 + 신규 모듈 파일 + parent pom + README.  
`docs/` 개정은 별 커밋이어도 된다 (구현 DoD와 무관).

---

## 8. 테스트 목록 — DoD 1:1 대응

| # | 테스트/확인 | 위치 | 대응 DoD 문장 | 합격 기준 |
|---|---|---|---|---|
| T1 | `ArchitectureRulesTest` (필드 `VERIFY_MUST_NOT_DEPEND_ON_SOLVE`) | solver-core | 경계 테스트 + `mvn verify` 일부 | surefire green |
| T2 | `RoNextApplicationTest.contextLoads` | app | `app` 기동 | 예외 없이 통과 |
| T3 | `RoNextApplicationTest.healthEndpointRespondsUp` | app | health 응답 | HTTP 200 · body `status` == `"UP"` |
| V1 | 루트에서 `mvn verify` | 루트 | `mvn verify` 통과 | 세 모듈 BUILD SUCCESS · T1–T3 green |
| V2 | GCP 0 | 빌드 | GCP 의존성 0 | enforcer 통과 + 아래 V2 명령에 `com.google.cloud` 좌표 0건 |
| V3 | 수동 1회 | 로컬 | 기동 후 health | `mvn spring-boot:run -pl app` 기동 후 `curl -s localhost:8080/actuator/health` 에 `"status":"UP"` |
| V4 | core 순수성 | 빌드 | Architecture §2.1 | 아래 V4 명령에 외부 compile 좌표 없음 (로컬 프로젝트 좌표만 허용; Stage 0에서는 compile 의존 자체가 없음) · 대조군은 0이 아니어야 함 |

**`-q`를 붙이지 않는다.** `dependency:list`는 결과를 INFO로 출력하므로 `-q`를 붙이면 의존이
있든 없든 **출력이 항상 비어** 두 검사가 무조건 통과한 것처럼 보인다. 아래 명령을 그대로 쓴다.

V2 확인 명령 (고정):

```bash
mvn dependency:list -DincludeGroupIds=com.google.cloud \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — com.google.cloud 좌표 0건"
# 기대: PASS 줄만 출력. 좌표가 한 줄이라도 찍히면 FAIL.
# enforcer bannedDependencies가 이미 빌드를 막지만, 이 명령은 그것과 독립으로 확인한다.
# 참고: com.vaadin.external.google:android-json (starter-test의 JSONassert 전이, test scope)은
#       groupId가 달라 여기에 잡히지 않으며 금지 대상이 아니다.
```

V4 확인 명령 (고정 — 본 검사 + 대조군 2줄을 모두 실행):

```bash
mvn dependency:list -pl solver-core -DincludeScope=compile \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — compile 의존 0건"

# 대조군: 명령·grep이 살아 있다는 증거. scope 제한을 풀면 test 의존이 보여야 한다.
mvn dependency:list -pl solver-core | grep -cE '^\[INFO\]\s+\S+:\S+:\S+:'
# 기대: 본 검사는 PASS 줄만, 대조군은 0이 아닌 수(2026-08-10 구현 시점 15).
# 대조군이 0이면 검사 자체가 고장난 것이므로 V4를 통과로 판정하지 않는다.
```

---

## 9. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | ArchUnit 룰 대상 프로덕션 클래스 0개 | `allowEmptyShould(true)` — §5.4에 포함 | Plan "빈 패키지 상태로도 룰 파일 먼저" |
| E2 | 어노테이션 없는 `package-info.java`는 `.class`를 생성하지 않을 수 있음 | 정상 — E1. 어노테이션 억지 추가 금지 | §5.1 |
| E3 | GCP 의존성 transitive 재유입 | enforcer `bannedDependencies` 빌드 실패 | DoD GCP 0 |
| E4 | JDK 25 아님 | enforcer `requireJavaVersion` 실패 — `sdk env` | `.sdkmanrc` |
| E5 | C5·C6 삭제는 git diff에 안 나타남 | 정상 | §2 |
| E6 | `.vscode/settings.json` 구 경로 | 미추적 로컬 — 저장소 범위 밖 | — |
| E7 | Boot 라인 변경 필요 | **이 문서 §0·§4.4 버전표를 개정한 뒤** 구현. 구현 중 임의 상향 금지 | §0 |
| E8 | 모듈 디렉터리에서만 `mvn verify` | DoD 판정은 **루트** V1만 | §8 |
| E9 | `solver-profile` 프로덕션 = package-info 하나 | 정상 | §3 |
| E10 | 모듈 빌드 순서 | Maven 의존 그래프 (`solver-core` → `solver-profile` → `app`) | §4.1 |
| E11 | `solver-profile`에 test 소스 0 | surefire tests run: 0 — 실패 아님 | §4.3 |
| E12 | parent packaging `pom`에서 compiler 미실행 | 정상 — child jar에 pluginManagement 적용 | §4.1.1 |

---

## 10. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 |
|---|---|
| canonical 모델·정규화·오류 분류 타입 | Stage 1 |
| 이동표·`Problem` | Stage 2 |
| `Solution`·전파·평가·`Profile`/`ProfileRegistry` 구현 | Stage 3 |
| ALNS | Stage 4 |
| `verify` 구현 (Stage 0은 빈 패키지 + 룰만) | Stage 5 |
| `SolveStore`·adapter·접수/조회 API·executor·S3 SDK | Stage 6 |
| Dockerfile 재작성·ECS·LocalStack | Stage 7 |
| `.dockerignore` 보강 | Stage 7 재량 (Stage 0·7 기본 변경 안 함) |
| `data/`·`scripts/`·`docs/`(deprecated 포함) 정리, `.gitignore` 손질 | 범위 밖 |
| solver-core 순수성 자동 검사 장치 추가 | 안 함 — 컴파일 차단 + V4 리뷰 |
| Boot/JUnit/ArchUnit 버전을 §0 표 밖으로 임의 변경 | 안 함 — 문서 개정 후 |
| health 전용 컨트롤러·security 스타터·추가 actuator endpoint | 안 함 |

---

## 11. 미해결 질문

**세 질문 전부 종결됐다** — 닫힌 질문은 [README](README.md) 공통 규칙(2026-08-13,
표시하고 남김)대로 해소 표시를 달아 남긴다. 이 절에 미결은 없다.

| # | 질문 | 현재 처리 |
|---|---|---|
| Q1 | ~~Dockerfile 삭제(C4)가 Plan에 없었음~~ | **해소 (2026-08-10).** Plan Stage 0 코드 정리에 반영. C4 그대로 |
| Q2 | fixture `multiRotation = "1"` 의미 미확정 | **해소 (2026-08-10, [Plan §2.1 D1](../implementation-plan.md) 확정).** 숫자는 **차량이 도는 바퀴 수**이고 값 `1` = 1바퀴 = **지원 범위 안**이다 — fixture는 그대로 접수된다. 판정은 `!= 0` 거부에서 **통과 = `{0, 1}`**로 반전됐다 (정본 Domain §2.5). Stage 0 파일 목록·DoD에는 여전히 무영향 |
| Q3 | 2회전 고객 확인됨 | **구현하지 않음 (2026-08-10, D1 확정과 함께 종결).** 2회전 = `multiRotation` `2` = 차고 재방문(multi-trip)이라 구조 변경이다. Domain §2.5 지원 범위 = 1바퀴 |

Q2·Q3는 Stage 0 파일 목록·DoD에 영향을 주지 않는다.
