---
phase: AR-0
rm_mapping: RM-0
title: Baseline과 Java 25 Maven reactor build architecture
status: NOT_STARTED
document_status: REVIEW
last_verified: "2026-07-24"
repository: /Users/brown/workspace/ro-next
working_directory: /Users/brown/workspace/ro-next
source_baseline:
  implementation_plan:
    path: docs/codex/implementation-plan.md
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  master_design:
    version: 3.2-review
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    version: 1.1-review
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    version: 2.2-review
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
  question_register:
    version: 2.1-review
    sha256: 3d6bc496b8df98a10534338828dd7e845b50ea967afa884642403405e613c088
repository_baseline:
  git_head: 523c23e2e13410885b16e974efe40ffe598106ee
  branch: codex/domain-design
  root_pom_sha256: f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6
  win_fixture_sha256: ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7
predecessor_phase_documents: []
predecessor_evidence: none
next_phase_documents:
  - docs/codex/phases/phase-01-input-domain-and-travel.md
parallel_handoff:
  - AR-6 logical-port/local skeleton after AR-0 DONE
---

# AR-0 / RM-0 — Baseline과 build architecture 실행 명세

## 1. 목적, 결과와 읽기 규칙

이 phase의 결과는 현재 단일 shaded JAR을 **동작 보존된 `legacy/current-app`과 Java 25 Maven parent/aggregator reactor**로 분리하고, 이후 phase가 dependency·package·문서 traceability 위반을 `mvn verify`에서 즉시 발견하게 하는 것이다. 이 문서는 구현 완료 보고가 아니며 production code를 구현하지 않는다.

구현 세션은 반드시 다음 순서를 지킨다.

1. 이 문서의 source hash와 작업 트리 기준선을 다시 확인한다.
2. 현재 legacy test와 artifact를 먼저 실행하여 green 기준선을 보존한다.
3. Production 이동이나 root POM 변경보다 먼저 characterization test와 architecture test를 작성한다.
4. 각 test가 아래에 적힌 **의도한 이유로 red**가 되는 것을 직접 확인하고 report를 보존한다.
5. 그 red를 보지 않은 상태에서는 source 이동, root `packaging=pom` 전환 또는 module POM 생성을 시작하지 않는다.
6. 최소 build 변경으로 targeted green을 만든 뒤 module verify와 전체 reactor regression을 순서대로 실행한다.

Phase의 구현 상태는 현재 `NOT_STARTED`다. 문서가 상세하다는 이유, POM이나 test 파일이 생겼다는 이유, 혹은 빈 module이 compile된다는 이유만으로 `DONE`이라 판정하지 않는다.

## 2. Authority, 결정 상태와 blocker

### 2.1 규범 근거

| Authority | 이 phase가 소비하는 절 | 적용 내용 |
|---|---|---|
| [전체 구현 계획](../implementation-plan.md) | §2~§8, §9.1, §10~§13 | 계획상 고정 module/path, legacy 보존, 테스트 우선, evidence bundle, DONE AND gate |
| [Master Design](../../master-design.md) | §1, §4.4~§4.7, §15.1~§15.2, §16.2, §17 | REVIEW 권위, dependency direction, `RM-0` traceability baseline, migration/change control |
| [Architecture Design](../../architecture-design.md) | §2, §5~§8, §18~§20, §21~§22 | current inventory, target reactor, module/package rule, test evidence, `ADR-ARCH-001` 후보 |
| [Domain Design](../../domain-design.md) | §1, §3~§3.1, §17~§18 | 의미 owner와 Maven 배치의 분리, deferred 경계, question traceability |
| [질문 등록부](../../master-design-open-questions.md) | §1~§4 | `RESOLVED 25`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 2`, 총 28개 상태 |

세 설계와 이 phase 문서는 모두 `REVIEW`다. 따라서 이 문서의 module, package, type과 method 이름은 **계획상 고정 이름 또는 계획상 제안 API**일 뿐 승인된 public Java/API/wire/storage 계약이 아니다. 채택된 외부 계약이나 승인 ADR이 충돌하면 구현을 멈추고 영향 문서와 계획을 같은 변경 단위에서 갱신한다.

### 2.2 이 phase의 active blocker 판정

작성 시점에는 AR-0 구현 자체를 막는 외부 질문은 없다. 다음 항목은 AR-0 blocker가 아니지만 범위 확대를 막는 guard다.

| ID/상태 | AR-0 해석 |
|---|---|
| `Q-BENCH-02` / `OPEN — EXPERIMENT_REQUIRED` | `screenMaxSteps`, phase-2 worker 수, `phase2MaxSteps`, `maxRounds`, watchdog 숫자를 POM, fixture, README 또는 sample config에 추가하지 않는다. |
| `Q-INFRA-01` / `DEFERRED` | 현재 GCP 자료를 legacy characterization으로만 보존한다. Provider adapter, `deployment/`, provider package와 production topology는 만들지 않는다. |
| `Q-VAR-01` / `DEFERRED` | Optional variant, multi-trip/rotation, route pool/MIP용 module·dependency·type을 만들지 않는다. |
| Win fixture decimal `D/U` | Build architecture에는 blocker가 아니다. Fixture를 이동·변환·반올림·baseline화하지 않는다. |
| `ADR-ARCH-001` | Architecture §21의 추천 ADR 후보다. 이 문서와 전체 계획의 고정 경로대로 internal scaffold를 만들 수 있으나, 다른 module 이름/방향이 필요하면 ADR과 계획 갱신 전 `BLOCKED`다. |

다음은 즉시 `BLOCKED`다.

- 구현 시작 시 source baseline hash가 이 metadata와 다르고 변경 권위를 확인할 수 없음
- `pom.xml`, `Dockerfile`, `README.md`, `src/main`, `src/test` 또는 예정 destination에 사용자/다른 세션 변경이 겹침
- 기존 source를 byte-for-byte 보존할 수 없거나 legacy artifact/main class parity가 깨짐
- 계획상 고정 module 이름이나 dependency 방향을 바꾸어야만 reactor가 성립함
- architecture test의 첫 red가 assertion이 아니라 dependency download, 권한, 잘못된 working directory 또는 unrelated test failure임

여러 phase 세션이 같은 working directory의 `target/`에서 Maven을 동시에 실행하면 shade JAR replace 같은 transient 충돌이 날 수 있다. 그런 동시 실행 실패는 repository baseline 결함이나 AR-0 blocker로 확정하지 않는다. 작성 시점 공통 baseline은 격리 실행으로 확인한 Java 25.0.3/Maven 3.9.14와 기존 `test`/`verify` 성공이다. 구현 세션의 Maven evidence도 다른 build가 끝난 격리 구간에서 얻은 결과만 사용한다.

## 3. 작성 시점 실제 저장소 baseline

### 3.1 Git과 파일 inventory

작성 시점 `git status --short --untracked-files=all`은 설계 문서와 연구 문서의 사용자 변경, `data/`와 `docs/codex/` 등의 untracked 파일을 포함한다. 이것들은 사용자/다른 세션 소유다. AR-0 구현 세션은 baseline status를 evidence에 보존하고 **예정 경로와 겹치지 않는 변경을 수정·stage·삭제하지 않는다**.

현재 구현 관련 tracked 경로인 `pom.xml`, `Dockerfile`, `README.md`, `gcp/**`, `src/main/**`, `src/test/**`에는 작성 시점 tracked diff가 없다. `data/**`는 untracked 사용자 소유다. `docs/master-design.md`, `docs/architecture-design.md`, `docs/domain-design.md`는 tracked modification이지만 위 metadata hash가 전체 구현 계획의 source baseline hash와 정확히 일치하므로 이 문서를 작성할 때 source drift는 없다.

현재 `rg --files -g '!target/**' -g '!**/target/**'`로 확인한 build/source 구조는 다음뿐이다.

```text
pom.xml
Dockerfile
README.md
src/main/java/com/ronext/optimizer/
├── application/AlnsBatchEngine.java
└── adapter/in/http/
    ├── HttpJson.java
    ├── JsonSupport.java
    ├── OptimizationApiController.java
    ├── OptimizationHttpServer.java
    └── OptimizationWorkerController.java
src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java
gcp/README.md
gcp/cloudbuild.yaml
gcp/workflows/optimization.yaml
data/ro_input_json_spec.pdf
data/win_poc_case.json
```

현재 repository root에는 `pom.xml`이 하나뿐이며 `<modules>`, 명시적 `<packaging>`, target `com.ronext.rpdptw` source, `architecture-rules`와 `test-fixtures`가 없다.

### 3.2 Toolchain과 baseline 실행

| 항목 | 작성 시점 관찰 |
|---|---|
| Java | Amazon Corretto `25.0.3`, JDK spec 25 |
| Maven | Apache Maven `3.9.14` |
| SDKMAN | `.sdkmanrc`: `java=25.0.3-amzn`, `maven=3.9.14` |
| Current packaging | 명시 생략으로 `jar`; `com.ronext:ro-next:0.1.0-SNAPSHOT` 단일 project |
| `mvn test` | 격리 실행 성공, `AlnsBatchEngineTest` 1건, failure/error/skip 0 |
| `mvn verify` | 격리 실행 성공, 같은 test 1건과 shade packaging 실행 |
| Thin JAR | `target/ro-next-0.1.0-SNAPSHOT.jar` |
| Fat JAR | `target/ro-next-0.1.0-SNAPSHOT-app.jar` |
| Main class | `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer` |
| Shade observation | module-info, service resource, license/notice 등 overlapping warning 다수; baseline 관찰이지 target 허용 규칙이 아님 |

한 번의 작성 시점 build에서 thin JAR SHA-256은 `c731b3435edfc59d4736668f74ae5ab11deac03c6390e7662995326f4ce8b98b`, fat JAR SHA-256은 `a4bd29513afb6b6827aecf2fa611424cf400678ca721287f1bc159d9a925b410`였다. 현재 POM에는 reproducible archive 정책이 없으므로 이 두 digest를 byte-reproducibility 계약으로 승격하지 않는다. Rollback artifact 식별용 단일 관찰값일 뿐이다.

### 3.3 Current application contract 관찰

| 대상 | 현재 관찰 | AR-0에서의 처리 |
|---|---|---|
| `AlnsBatchEngine.run` | `Map<String,Object>`에 insertion order로 `requestId,inputUri,runNumber,seed,iterations,objective,status`; status `CANDIDATE` | Exact fixed input/output characterization 후 byte-preserving 이동 |
| Fixed call | `("request-1","gs://bucket/instance.json",2,42L,5000)`의 objective `944806.7416068788` | JDK 25 동일 baseline assertion. RPDPTW quality evidence로 사용 금지 |
| Public submit | `gs://`만 허용; `parallelRuns` default 8/clamp 1..20, `iterationsPerRun` default 5000/clamp 100..250000, seed 누락 시 `System.nanoTime()` | Legacy-only contract로 기록. `Q-BENCH-02` 공식값으로 승격 금지 |
| Retrieval | `results/<requestId>.json` 부재 시 202 `RUNNING`, 존재 시 raw JSON 200 | AR-8 compatibility 입력으로만 handoff |
| Worker | `/internal/batches`, candidate key `candidates/<requestId>/<runNumber>.json` | Content parity와 workflow characterization |
| Finalize | prefix 아래 candidate의 `double objective` 최솟값, `results/<requestId>.json`, status `COMPLETED` | 두-verifier target 의미가 아니며 수정하지 않음 |
| Workflow | run ordinal만큼 병렬 호출, `seed + runNumber`, `iterationsPerRun`, default HTTP retry 후 finalize | 파일 hash/semantic characterization; completeness 승인 아님 |
| Runtime | `SERVICE_MODE=api|worker` 한 fat JAR | `legacy/current-app`에서 유지; target apps는 빈 skeleton만 생성 |

### 3.4 Current dependency와 fixture

현재 root direct dependency는 Google Workflow Executions `2.94.0`, Google Cloud Storage `2.70.0`, Jackson `2.19.2`, JUnit `5.13.1(test)`이다. Enforcer `3.6.1`, Compiler `3.14.1`, Surefire `3.5.4`, Shade `3.6.1`을 직접 실행한다. Cloud dependency가 root application classpath에 있는 현 상태는 legacy module에서만 보존한다.

[Win fixture](../../../data/win_poc_case.json)는 14,157,512 bytes이고 SHA-256은 metadata와 같다. 작성 시점 조사에서 `distanceMatrix`는 205,209개 entry를 가지며 다수 `D/U`가 소수 문자열이다. 이 phase는 fixture value, provider/product, optional variant 또는 변환 방법을 추측하지 않으며 파일을 읽기 전용으로 둔다.

## 4. 현 상태에서 목표 상태까지의 gap

| 영역 | 현재 | AR-0 목표 | Exit 관찰 |
|---|---|---|---|
| Root build | 단일 implicit `jar` | `com.ronext.rpdptw:rpdptw-parent:0.1.0-SNAPSHOT`, `packaging=pom` parent/aggregator | `ReactorStructureTest`와 effective POM |
| Reactor | project 1개 | root 포함 17 project, 하위 module project 16개 | 정확한 path/artifact set, cycle 0 |
| Legacy | root source/POM/shade | standalone `legacy/current-app`에 기존 POM·6 production class·기존 test를 byte-preserving 이동 | pre/post SHA manifest, exact behavior tests |
| Docker | root single-project build | root Dockerfile이 `legacy/current-app` fat JAR을 계속 만들고 original Dockerfile은 legacy 경로에 보존 | static contract test와 fat JAR manifest |
| Target namespace | 없음 | target leaf POM과 package owner만 예약; semantic production type 없음 | `com.ronext.rpdptw` 외 legacy 혼입 0 |
| Version policy | dependency/plugin version이 current root에 혼재 | target parent의 dependency/plugin management와 Java 25 convention | effective POM test |
| Dependency boundary | Cloud SDK가 root에 노출 | Cloud SDK는 `legacy/current-app`에만 존재; target core/solver/verification/application 0 | Enforcer + dependency policy + bytecode test |
| Verification independence | module 자체가 없음 | verification → core만 허용, solver dependency 금지 | synthetic violation red + real reactor green |
| Test fixtures | 전용 module 없음 | `rpdptw-test-fixtures`, core compile dependency만; consumer는 test scope만 | leakage rule |
| Package boundary | 단일 `com.ronext.optimizer` | target base `com.ronext.rpdptw`, 다른 module `.internal` import 금지 | ArchUnit 또는 동등 bytecode rule |
| Documentation | root README의 `docs/08_gcp_architecture.md` link가 실제 경로와 불일치; question 상태 수동 | local link/anchor, phase mapping, 28-question status를 verify gate로 검사 | traceability report |
| Reproducible target build | archive timestamp policy 없음 | target parent가 fixed build output timestamp와 plugin version을 관리 | clean rebuild digest test; legacy digest는 제외 |

## 5. 목표 reactor와 dependency 계약

### 5.1 정확한 reactor project

```text
ro-next/                                      # rpdptw-parent, packaging=pom
├── legacy/current-app/                       # com.ronext:ro-next, packaging=jar
├── rpdptw/                                   # aggregator
│   ├── core/                                 # rpdptw-core
│   ├── solver/                               # rpdptw-solver
│   ├── verification/                         # rpdptw-verification
│   ├── application/                          # rpdptw-application
│   └── profiles/                             # aggregator
│       └── standard/                         # rpdptw-profile-standard
├── adapters/                                 # aggregator
│   └── common/                               # rpdptw-adapter-common
├── apps/                                     # aggregator
│   ├── cli/                                  # rpdptw-cli
│   ├── api/                                  # rpdptw-api
│   └── worker/                               # rpdptw-worker
├── build/test-fixtures/                      # rpdptw-test-fixtures
└── build/architecture-rules/                 # rpdptw-architecture-rules
```

Root와 모든 target module의 계획상 제안 `groupId`는 `com.ronext.rpdptw`, version은 `0.1.0-SNAPSHOT`이다. `legacy/current-app/pom.xml`은 원본 POM을 byte-for-byte 이동하므로 예외적으로 기존 `com.ronext:ro-next:0.1.0-SNAPSHOT`와 자체 plugin/dependency version을 유지한다. Target module은 legacy module에 의존하지 않고 legacy module도 target module에 의존하지 않는다.

### 5.2 계획상 제안 compile dependency

| Consumer | 허용 direct compile dependency |
|---|---|
| `rpdptw-core` | repository 내부 module 없음 |
| `rpdptw-solver` | `rpdptw-core` |
| `rpdptw-verification` | `rpdptw-core` |
| `rpdptw-profile-standard` | `rpdptw-core` |
| `rpdptw-application` | `rpdptw-core`, `rpdptw-solver`, `rpdptw-verification` |
| `rpdptw-adapter-common` | `rpdptw-core`, `rpdptw-verification`, `rpdptw-application` |
| `rpdptw-cli` | `rpdptw-application`, `rpdptw-adapter-common`, `rpdptw-profile-standard` |
| `rpdptw-api` | `rpdptw-application`, `rpdptw-adapter-common` |
| `rpdptw-worker` | `rpdptw-application`, `rpdptw-adapter-common`, `rpdptw-profile-standard` |
| `rpdptw-test-fixtures` | `rpdptw-core`; 모든 consumer는 이 artifact를 `test` scope로만 선언 |
| `rpdptw-architecture-rules` | 모든 target leaf를 `test` scope로 관찰하여 reactor 마지막에 실행 |

Aggregator POM은 child module을 나열할 뿐 business dependency를 선언하지 않는다. App POM의 위 dependency는 composition 방향을 예약하는 internal proposal이며 public runtime API 승인이 아니다.

### 5.3 Parent convention

Target parent는 다음을 중앙 관리한다.

- Java release `25`, UTF-8, Maven `[3.9.14,)`, Java `[25,26)`
- JUnit `5.13.1`, Jackson `2.19.2`와 internal target artifact version의 `dependencyManagement`
- Enforcer `3.6.1`, Compiler `3.14.1`, Resources `3.4.0`, Surefire/Failsafe `3.5.4`, JAR `3.5.0`, Shade `3.6.1`의 `pluginManagement`
- 계획상 제안 architecture test dependency `com.tngtech.archunit:archunit-junit5:1.4.1`; 선택 변경 시 이 문서/ADR을 먼저 갱신
- Surefire unit/property/architecture naming과 Failsafe `*IT` 실행
- Target archive에 고정 `project.build.outputTimestamp`; legacy standalone POM은 byte parity를 위해 변경하지 않음
- Core/solver/verification/application의 provider SDK ban, 모든 target module의 test-fixture scope ban

Root `<dependencies>`에는 business, cloud, Jackson, JUnit dependency를 넣지 않는다. Dependency version을 관리하는 것과 모든 child classpath에 dependency를 주입하는 것을 구분한다.

## 6. 정확한 변경·신규·이동 예상 경로

### 6.1 Build, documentation과 deployment compatibility

| 작업 | Repository-relative path | 내용 |
|---|---|---|
| 변경 | `pom.xml` | 새 target parent/aggregator. 원본 bytes는 먼저 legacy POM으로 이동 |
| 신규(원본 이동) | `legacy/current-app/pom.xml` | 현재 `pom.xml` SHA-256 그대로; standalone legacy exception |
| 변경 | `Dockerfile` | reactor context에서 `-pl legacy/current-app -am` package 후 legacy fat JAR 복사 |
| 신규(원본 이동) | `legacy/current-app/Dockerfile` | 현재 Dockerfile bytes 그대로; standalone rollback build |
| 변경 | `README.md` | reactor/legacy 실행법과 실제 `docs/arranged/08_gcp_architecture.md` link로 최소 수정 |
| 변경 금지 | `gcp/README.md` | Legacy characterization 입력으로만 hash/evidence 보존 |
| 변경 금지 | `gcp/cloudbuild.yaml` | Root Dockerfile compatibility로 기존 build entry 유지 |
| 변경 금지 | `gcp/workflows/optimization.yaml` | Legacy workflow characterization만 수행 |
| 변경 금지 | `data/ro_input_json_spec.pdf` | Read-only |
| 변경 금지 | `data/win_poc_case.json` | Read-only, 변환/공식 baseline 금지 |

### 6.2 Aggregator와 leaf POM

| 작업 | Path | Artifact/역할 |
|---|---|---|
| 신규 | `rpdptw/pom.xml` | semantic/application aggregator |
| 신규 | `rpdptw/core/pom.xml` | `rpdptw-core` |
| 신규 | `rpdptw/solver/pom.xml` | `rpdptw-solver` |
| 신규 | `rpdptw/verification/pom.xml` | `rpdptw-verification` |
| 신규 | `rpdptw/application/pom.xml` | `rpdptw-application` |
| 신규 | `rpdptw/profiles/pom.xml` | profile aggregator |
| 신규 | `rpdptw/profiles/standard/pom.xml` | `rpdptw-profile-standard` |
| 신규 | `adapters/pom.xml` | adapter aggregator |
| 신규 | `adapters/common/pom.xml` | `rpdptw-adapter-common` |
| 신규 | `apps/pom.xml` | deployable app aggregator |
| 신규 | `apps/cli/pom.xml` | `rpdptw-cli` |
| 신규 | `apps/api/pom.xml` | `rpdptw-api` |
| 신규 | `apps/worker/pom.xml` | `rpdptw-worker` |
| 신규 | `build/test-fixtures/pom.xml` | `rpdptw-test-fixtures` |
| 신규 | `build/architecture-rules/pom.xml` | `rpdptw-architecture-rules`; reactor-last verify gate |

AR-0에서는 target leaf에 semantic `src/main/java` type을 추가하지 않는다. Maven이 빈 JAR 경고를 내더라도 후속 phase의 책임을 당겨 구현하지 않는다. Directory를 Git에 남기기 위한 `.gitkeep`도 production skeleton으로 오인될 수 있으므로 추가하지 않는다.

### 6.3 Byte-preserving legacy source/test 이동

| 현재 path | 최종 path | 보존 조건 |
|---|---|---|
| `src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | `legacy/current-app/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | SHA `4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/HttpJson.java` | `legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/HttpJson.java` | SHA `90bf71d9ec0e89edd76603d0b4c4548be257af23a5f4c5e387d3ce00590662a5` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/JsonSupport.java` | `legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/JsonSupport.java` | SHA `33e6d1cd1a7b523250161162762dadde04ee0d8e9611910b9810b2060db76b27` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java` | `legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java` | SHA `3dcab11fcac78bb994b1f77bedaf425601684e8b9b3eb1560c305dcf671f358c` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java` | `legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java` | SHA `2284c20ad0a0d8a957bf7bc4fe5c4a352c2f923c1f56a9023f1389910b28667c` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java` | `legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java` | SHA `846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c` |
| `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` | `legacy/current-app/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` | SHA `947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829` |

기존 7개 Java 파일은 package, import, comment와 whitespace를 포함해 수정하지 않는다. 새 characterization test는 이 manifest의 “기존 content parity” 대상과 구분한다.

### 6.4 신규 legacy characterization test/resource

| Path | 목적 |
|---|---|
| `legacy/current-app/src/test/java/com/ronext/optimizer/application/LegacyAlnsBatchEngineCharacterizationTest.java` | exact ordered map, deterministic fixed call |
| `legacy/current-app/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyHttpContractCharacterizationTest.java` | static validation/default/clamp/error mapping |
| `legacy/current-app/src/test/java/com/ronext/optimizer/characterization/LegacyWorkflowDefinitionCharacterizationTest.java` | workflow endpoint, seed, iteration, retry, finalize 순서 |
| `legacy/current-app/src/test/java/com/ronext/optimizer/characterization/LegacySourceContentParityTest.java` | 기존 7개 Java와 원본 POM/Dockerfile hash mapping |
| `legacy/current-app/src/test/resources/legacy/alns-candidate-baseline.properties` | fixed call expected value와 key order |
| `legacy/current-app/src/test/resources/legacy/current-app-source-sha256.txt` | pre-move path, post-move path, SHA-256 |
| `legacy/current-app/src/test/resources/legacy/http-workflow-contract.properties` | legacy-only endpoints/defaults/object keys; official config 아님 |

Test source는 production 이동 전에 임시로 현재 `src/test/**` 아래에 먼저 작성하고 red/green baseline을 만든 뒤 production source와 함께 위 최종 경로로 이동한다.

### 6.5 신규 architecture rule test/support

| Path | 목적 |
|---|---|
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/ReactorStructureTest.java` | packaging, module/artifact set, parent chain, cycle |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/ForbiddenDependencyArchitectureTest.java` | 허용 DAG와 provider/transport ban |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/InternalPackageBoundaryTest.java` | cross-module `.internal` bytecode dependency ban |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/TestFixturesScopeArchitectureTest.java` | production scope leakage ban |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/ToolchainAndReproducibilityTest.java` | Java/Maven/plugin/archive convention |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/DocumentationTraceabilityTest.java` | link/anchor, phase/RM, question status |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/LegacyDockerfileContractTest.java` | root Dockerfile의 legacy module artifact mapping |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/LegacyPackagingCharacterizationIT.java` | reactor package 뒤 fat JAR 이름, main class, required legacy classes |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/support/MavenReactorInspector.java` | POM graph parser |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/support/DependencyPolicy.java` | allowed edge와 scope evaluator |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/support/BytecodePolicy.java` | ArchUnit/`jdeps` 결과 normalization |
| `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/support/MarkdownTraceabilityInspector.java` | local link/anchor와 question table parser |
| `build/architecture-rules/src/test/resources/architecture/expected-reactor-modules.txt` | exact 16 module path/artifact pair |
| `build/architecture-rules/src/test/resources/architecture/allowed-module-dependencies.txt` | §5.2 DAG |
| `build/architecture-rules/src/test/resources/architecture/allowed-planned-links.txt` | 아직 생성 전인 phase link만 exact allowlist |
| `build/architecture-rules/src/test/resources/fixtures/forbidden-cloud/pom.xml` | cloud dependency 검출용 synthetic invalid POM |
| `build/architecture-rules/src/test/resources/fixtures/test-fixture-leak/pom.xml` | compile-scope test-fixture 검출용 invalid POM |
| `build/architecture-rules/src/test/resources/fixtures/cycle/a/pom.xml` | cycle fixture A |
| `build/architecture-rules/src/test/resources/fixtures/cycle/b/pom.xml` | cycle fixture B |

Invalid fixture는 `src/test/resources` 안에서만 사용한다. Red evidence를 만들기 위해 실제 target POM에 cloud dependency, cycle 또는 `.internal` import를 잠시 넣는 shortcut은 금지한다.

## 7. 계획상 제안 package, type와 API

AR-0에는 domain/search/application production API가 없다. 아래 type은 `rpdptw-architecture-rules`의 **test-only 계획상 제안 API**다. 승인된 외부 API가 아니며 다른 production module이 compile-depend하면 architecture violation이다.

```java
package com.ronext.rpdptw.build.architecture.support;

record ModuleCoordinate(
        Path relativePath,
        String groupId,
        String artifactId,
        String version,
        String packaging) {}

record DeclaredDependency(
        String groupId,
        String artifactId,
        String scope,
        boolean optional) {}

record ArchitectureViolation(
        String ruleId,
        Path subject,
        String evidence) {}

record ReactorModel(
        ModuleCoordinate root,
        List<ModuleCoordinate> modules,
        Map<ModuleCoordinate, List<DeclaredDependency>> dependencies) {}

final class MavenReactorInspector {
    static ReactorModel inspect(Path repositoryRoot);
}

final class DependencyPolicy {
    static List<ArchitectureViolation> evaluate(ReactorModel reactor);
}

final class BytecodePolicy {
    static List<ArchitectureViolation> evaluate(
            Path repositoryRoot,
            Map<ModuleCoordinate, Path> compiledClassDirectories);
}

record TraceabilityReport(
        List<ArchitectureViolation> brokenLinks,
        Map<String, Integer> questionStatusCounts,
        Map<String, String> phaseMappings) {}

final class MarkdownTraceabilityInspector {
    static TraceabilityReport inspect(Path repositoryRoot, List<Path> documents);
}

final class ArchitectureInspectionException extends RuntimeException {
    ArchitectureInspectionException(String message, Throwable cause);
}
```

API 불변조건과 error contract:

- 모든 `Path`는 repository root 아래로 normalize하며 symlink를 이용한 root 이탈은 `ArchitectureInspectionException`이다.
- POM parse 실패, duplicate module path/artifact, unresolved local parent와 cycle은 빈 결과가 아니라 typed exception 또는 `ArchitectureViolation`이다.
- `scope` 생략은 Maven의 `compile`로 정규화한다. Unknown scope를 허용 scope로 추정하지 않는다.
- Dependency policy는 exact module identity를 사용하고 artifact 이름 substring으로 owner를 추정하지 않는다.
- Bytecode rule은 test fixture의 의도적 invalid class와 실제 reactor class를 분리한다.
- Markdown 검사기는 code fence와 외부 HTTP link를 local file로 오인하지 않는다. Local file/anchor만 strict 검사한다.
- `allowed-planned-links.txt`는 전체 계획 §7의 아직 생성 전 phase path만 포함한다. 임의 broken link를 숨기는 general allowlist가 아니다.
- Violation message는 최소 `ruleId`, module/path, offending edge/reference를 포함하여 첫 red 원인을 눈으로 확인할 수 있어야 한다.

`rpdptw-test-fixtures`는 AR-0에서 public type을 만들지 않는다. `com.ronext.rpdptw.testing`의 구체 builder/API는 `ProblemInstance` 계약을 소유하는 AR-1이 test-first로 제안한다.

## 8. 세분화된 test case

### 8.1 Legacy characterization

| Test class.method | 종류/권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green 조건 |
|---|---|---|---|---|---|
| `LegacyAlnsBatchEngineCharacterizationTest.runPreservesOrderedCandidateContract` | Characterization; 구현 계획 §4.2/§9.1 | fixed call `request-1`, URI, run 2, seed 42, 5000 | key order 7개, exact scalar와 `944806.7416068788`, `CANDIDATE` | 최초 test만 작성했을 때 `BASELINE_RESOURCE_MISSING: legacy/alns-candidate-baseline.properties` | fixture 추가 뒤 현재 root와 이동 후 legacy module에서 동일 PASS |
| `LegacyAlnsBatchEngineCharacterizationTest.sameEnvelopeProducesSameMap` | Reproducibility-characterization | 같은 call 2회 | `Map.equals`, key order와 `Double.doubleToLongBits` 동일 | expectation loader가 없으면 compile error `cannot find symbol LegacyCandidateExpectation` | 최소 test helper 뒤 PASS; solver reproducibility 주장 금지 |
| `LegacyHttpContractCharacterizationTest.submitDefaultsAndClampsRemainLegacyOnly` | Unit/reflection; implementation plan §4.2 | private static helper boundary values | defaults 8/5000, clamp 1..20/100..250000 | expected table을 먼저 `0/0` sentinel로 두지 말고 missing baseline resource failure를 관찰 | exact current values PASS, properties에 `legacyOnly=true` |
| `LegacyHttpContractCharacterizationTest.requiredFieldsAndErrorsRemainStable` | Unit/reflection | blank/missing `inputUri`, numeric worker fields | exact 400-triggering messages와 `gs://` rule | `BASELINE_KEY_MISSING: inputUri.error` | source 변경 없이 exact messages PASS |
| `LegacyWorkflowDefinitionCharacterizationTest.recordsFanOutSeedRetryAndFinalizeOrder` | File characterization | `gcp/workflows/optimization.yaml` | endpoints, `seed + runNumber`, iteration mapping, two default retries, finalize after parallel | baseline semantic list 없음: `WORKFLOW_BASELINE_MISSING` | YAML bytes/required tokens and order PASS |
| `LegacySourceContentParityTest.requiresLegacyDestinationsWithExactBytes` | Migration | 7-file SHA manifest와 source/destination pair | old path 없음, new path 있음, SHA exact | production 이동 전 `MISSING_DESTINATION: legacy/current-app/src/main/.../AlnsBatchEngine.java` | 모든 move가 100% rename이며 mismatch 0 |
| `LegacyPackagingCharacterizationIT.fatJarKeepsNameAndMainClass` | Failsafe/package | packaged legacy module | `ro-next-0.1.0-SNAPSHOT-app.jar`, exact main, 6 classes | reactor 전 destination artifact 없음: `LEGACY_FAT_JAR_MISSING` | `verify`에서 manifest/class list PASS |

### 8.2 Reactor와 enforcement

| Test class.method | 종류/권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green 조건 |
|---|---|---|---|---|---|
| `ReactorStructureTest.rootIsPomParentAndContainsExactModuleSet` | Architecture; Architecture §5, §19 | current repository + expected module list | root `pom`, 16 child project path/artifact, parent linkage | `ROOT_PACKAGING expected=pom actual=jar; MISSING_MODULES=[...]` | unexpected/missing/duplicate 0 |
| `ReactorStructureTest.graphIsAcyclicAndTopologicallyBuildable` | Architecture | synthetic A↔B cycle와 actual reactor | fixture는 `REACTOR_CYCLE` 검출, actual cycle 0 | policy 구현 전 fixture violation list empty assertion | fixture red detector PASS + actual green |
| `ForbiddenDependencyArchitectureTest.rejectsSyntheticCloudDependency` | Architecture; Architecture §7.2~§7.3 | `fixtures/forbidden-cloud/pom.xml` | `FORBIDDEN_DEPENDENCY`에 coordinate와 owner 포함 | test-first compile failure `cannot find symbol DependencyPolicy`, 이후 empty violation assertion | fixture 1개 이상 검출 |
| `ForbiddenDependencyArchitectureTest.actualReactorMatchesAllowedDag` | Architecture | actual effective POM graph | §5.2 edge 외 0, verification→solver 0, target cloud/transport 0 | root 단일 app에서 `UNKNOWN_MODULE/ROOT_BUSINESS_DEPENDENCY` | real graph violation 0 |
| `InternalPackageBoundaryTest.rejectsCrossModuleInternalFixture` | Architecture/bytecode; Architecture §8.2 | test-only illegal consumer/producer classes | exact `.internal` edge 검출 | `expected INTERNAL_PACKAGE_EDGE but violations=[]` | invalid fixture detected, real target 0 |
| `TestFixturesScopeArchitectureTest.rejectsCompileScopeFixture` | Architecture | invalid POM | `TEST_FIXTURE_SCOPE` | evaluator 미구현으로 expected violation 없음 | invalid detected; all actual consumer scopes `test` |
| `ToolchainAndReproducibilityTest.inheritsJava25AndPinnedPlugins` | Build contract | effective POMs | release 25, Enforcer ranges, exact plugin versions | child POM 누락 목록과 inherited value mismatch | target leaf 모두 일치 |
| `ToolchainAndReproducibilityTest.targetArtifactsUsePinnedArchiveConvention` | Reproducibility | target effective POM과 built JAR entry timestamp | 모든 target artifact가 선언한 output timestamp/plugin convention 사용 | `REPRODUCIBLE_ARCHIVE_POLICY_MISSING` 또는 entry timestamp mismatch | target convention/entry timestamp 일치; legacy artifact는 이 assertion에서 제외 |
| `LegacyDockerfileContractTest.rootImageBuildTargetsLegacyCurrentApp` | Deployment characterization | root Dockerfile | `-pl legacy/current-app -am`, legacy fat-JAR source, same main | current file에서 `LEGACY_MODULE_BUILD_SELECTOR_MISSING` | new root wrapper와 standalone original Dockerfile 계약 PASS |

### 8.3 문서와 질문 traceability

| Test class.method | 종류/권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green 조건 |
|---|---|---|---|---|---|
| `DocumentationTraceabilityTest.normativeLocalLinksAndAnchorsResolve` | Governance; RM-0 | Master/Architecture/Domain/plan/register/root README | local file/anchor broken 0; planned phase path만 exact allowlist | `BROKEN_FILE_LINK README.md -> docs/08_gcp_architecture.md` | README 실제 path 수정, 그 밖의 broken 0 |
| `DocumentationTraceabilityTest.questionRegisterHasExactStatusPartition` | Governance | question register table | total 28, resolved 25, open-experiment 1, deferred 2; exact IDs | parser/expected status 미구현 시 count mismatch message | count와 IDs 모두 PASS |
| `DocumentationTraceabilityTest.phaseAndRmMappingsAreBijective` | Governance | implementation plan §7/§9, Architecture §19.2 | AR-0..10 각각 한 RM/phase doc; AR-11은 deferred로 active list 밖 | missing/duplicate mapping을 exact ID로 출력 | active 11 mapping과 deferred 경계 PASS |
| `DocumentationTraceabilityTest.reviewStatusIsNotReportedAsApproval` | Governance | normative documents/README | REVIEW를 APPROVED/API/topology 완료로 표현한 신규 문장 0 | forbidden phrase fixture가 검출되지 않아 assertion fail | fixture detects; actual newly changed text 0 |

Compile failure는 `MavenReactorInspector`/`DependencyPolicy` 같은 test utility skeleton을 처음 만드는 red에서만 허용한다. Structure, scope, link, cycle 같은 의미 test는 utility compile 이후 위 assertion/message가 실제로 출력되어야 한다.

## 9. Red → failure 확인 → 최소 구현 순서

### 9.1 작업 단위 A — Baseline과 characterization 먼저

1. `git status`, source hashes, Java/Maven version, current `mvn test`, current `mvn verify`, fat-JAR manifest를 evidence `baseline/`에 기록한다.
2. `LegacyAlnsBatchEngineCharacterizationTest`를 현재 root test tree에 먼저 작성한다. Expected resource는 아직 만들지 않는다.
3. Targeted test를 실행하여 `BASELINE_RESOURCE_MISSING`을 확인한다. “test가 발견되지 않음”이나 dependency download 실패는 인정하지 않는다.
4. `alns-candidate-baseline.properties`와 최소 expectation loader만 추가하여 targeted green을 확인한다.
5. HTTP/workflow characterization test를 production 수정 전에 추가하고 현재 behavior에서 green을 만든다.
6. `LegacySourceContentParityTest`를 추가하고 `build/architecture-rules` test scaffold에 `LegacyPackagingCharacterizationIT`를 먼저 작성한다.
7. Source parity test가 `MISSING_DESTINATION`, architecture-rules의 packaging IT가 package phase에서 `LEGACY_FAT_JAR_MISSING`으로 red임을 각각 보존한다.

### 9.2 작업 단위 B — Architecture tests를 production POM보다 먼저

1. `build/architecture-rules`의 test source/resource와 test 실행에 필요한 **standalone test-only bootstrap POM**을 작성한다. Target production module/POM은 아직 만들지 않는다.
2. `ReactorStructureTest`를 현재 repository에 실행하여 root `jar`와 missing module 목록을 확인한다.
3. Synthetic invalid POM/class fixture에 대한 dependency/internal/scope/cycle test를 작성한다.
4. Support API가 없어서 나는 compile failure를 한 번 확인한 뒤 최소 inspector/policy test utility를 작성한다.
5. 각 invalid fixture가 의도한 rule ID로 잡히는 green을 만든다. 실제 production POM에 위반을 넣지 않는다.
6. `DocumentationTraceabilityTest`에서 현재 root README broken link를 red로 확인한다.
7. `LegacyDockerfileContractTest`에서 현재 root Dockerfile의 missing module selector red를 확인한다.

### 9.3 작업 단위 C — 최소 reactor 구현

1. 원본 root `pom.xml`과 `Dockerfile` bytes를 각각 `legacy/current-app/` destination으로 먼저 이동한다.
2. 기존 6 production class와 기존 test를 SHA manifest대로 이동한다. Characterization test/resource도 최종 legacy 경로로 옮긴다.
3. 새 root parent/aggregator POM을 만들고 aggregator POM, leaf POM을 §6.2 순서로 추가한다.
4. `build/architecture-rules/pom.xml`의 standalone bootstrap 설정을 target parent 상속 형태로 최소 변경한다.
5. Dockerfile wrapper와 README의 reactor/legacy 설명·link만 최소 수정한다.
6. Semantic production Java type, provider adapter, placeholder domain API는 만들지 않는다.

### 9.4 Targeted green과 regression

1. Legacy source parity test를 실행하여 기존 Java SHA mismatch 0을 확인한다.
2. Legacy engine/HTTP/workflow characterization 전체를 실행한다.
3. Reactor 마지막의 architecture-rules packaging IT를 `verify`에서 실행하고 legacy fat JAR main class를 확인한다.
4. Reactor structure test를 green으로 만든다.
5. Dependency/internal/test-fixture/toolchain/document/Dockerfile test를 각각 targeted green으로 만든다.
6. `rpdptw-architecture-rules` module verify를 실행한다.
7. 전체 `mvn verify`를 실행한다.
8. Target module clean rebuild digest, forbidden `rg`, dependency graph, diff scope와 `git diff --check`를 evidence에 넣는다.

어느 단계에서도 red report를 삭제하거나 마지막 green report로 덮어쓰지 않는다.

## 10. Exact 실행 명령

모든 명령의 working directory는 `/Users/brown/workspace/ro-next`다.

### 10.1 구현 시작 전 baseline

```bash
pwd
git status --short --untracked-files=all
git rev-parse --verify HEAD
git branch --show-current
rg --files -g '!target/**' -g '!**/target/**' | sort
rg --files -g 'pom.xml' -g '!node_modules/**' -g '!target/**' | sort
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
shasum -a 256 pom.xml Dockerfile README.md gcp/README.md gcp/cloudbuild.yaml gcp/workflows/optimization.yaml data/win_poc_case.json
mvn -version
mvn test
mvn verify
unzip -p target/ro-next-0.1.0-SNAPSHOT-app.jar META-INF/MANIFEST.MF
```

Source design hash 중 하나라도 metadata와 다르면 여기서 멈춘다.

### 10.2 첫 red와 characterization green

```bash
mvn -Dtest=LegacyAlnsBatchEngineCharacterizationTest test
mvn -Dtest=LegacyAlnsBatchEngineCharacterizationTest,LegacyHttpContractCharacterizationTest,LegacyWorkflowDefinitionCharacterizationTest test
mvn -Dtest=LegacySourceContentParityTest test
mvn -f build/architecture-rules/pom.xml -Darchitecture.repositoryRoot=/Users/brown/workspace/ro-next -Dit.test=LegacyPackagingCharacterizationIT verify
```

첫 번째 명령의 최초 실행은 `BASELINE_RESOURCE_MISSING`, 세 번째는 `MISSING_DESTINATION`, 네 번째는 `LEGACY_FAT_JAR_MISSING`이어야 한다. Test 0건, class-not-found가 아닌 assertion/report를 확인한다.

Architecture bootstrap red:

```bash
mvn -f build/architecture-rules/pom.xml -Darchitecture.repositoryRoot=/Users/brown/workspace/ro-next -Dtest=ReactorStructureTest test
mvn -f build/architecture-rules/pom.xml -Darchitecture.repositoryRoot=/Users/brown/workspace/ro-next -Dtest=ForbiddenDependencyArchitectureTest,InternalPackageBoundaryTest,TestFixturesScopeArchitectureTest test
mvn -f build/architecture-rules/pom.xml -Darchitecture.repositoryRoot=/Users/brown/workspace/ro-next -Dtest=DocumentationTraceabilityTest,LegacyDockerfileContractTest test
```

### 10.3 이동 뒤 targeted green

```bash
mvn -pl legacy/current-app -am -Dtest=AlnsBatchEngineTest,LegacyAlnsBatchEngineCharacterizationTest,LegacyHttpContractCharacterizationTest,LegacyWorkflowDefinitionCharacterizationTest,LegacySourceContentParityTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl build/architecture-rules -am -Dit.test=LegacyPackagingCharacterizationIT -Dsurefire.failIfNoSpecifiedTests=false verify
mvn -pl build/architecture-rules -am -Dtest=ReactorStructureTest,ForbiddenDependencyArchitectureTest,InternalPackageBoundaryTest,TestFixturesScopeArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl build/architecture-rules -am -Dtest=ToolchainAndReproducibilityTest,DocumentationTraceabilityTest,LegacyDockerfileContractTest -Dsurefire.failIfNoSpecifiedTests=false test
```

`-Dsurefire.failIfNoSpecifiedTests=false`는 `-am`으로 같이 빌드되는 owner가 아닌 module에만 적용한다. `legacy/current-app`와 `build/architecture-rules`의 Surefire/Failsafe report에 지정 test가 실제 실행됐는지 확인한다.

### 10.4 Module verify와 reactor regression

```bash
mvn -pl legacy/current-app -am verify
mvn -pl build/architecture-rules -am verify
mvn verify
mvn -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/profiles/standard,adapters/common,apps/cli,apps/api,apps/worker,build/test-fixtures dependency:tree -Dscope=compile
```

### 10.5 금지 dependency/value와 namespace 검색

다음 `rg`는 match가 없어서 exit 1인 것이 green이다.

```bash
rg -n --glob 'pom.xml' --glob '*.java' 'com\.google\.cloud|software\.amazon\.awssdk|com\.amazonaws|com\.azure|io\.azure|org\.springframework|io\.quarkus' rpdptw adapters apps build
rg -n --glob '*.java' 'if\s*\([^)]*(customerId|customerName)|switch\s*\([^)]*(customerId|customerName)' rpdptw/core rpdptw/solver rpdptw/verification
rg -n --glob 'pom.xml' --glob '*.java' 'screenMaxSteps|phase2MaxSteps|maxRounds|parallelRuns|iterationsPerRun' rpdptw adapters apps build
rg -n --glob '*.java' '^package com\.ronext\.optimizer|^import com\.ronext\.optimizer' rpdptw adapters apps build
```

Legacy와 GCP characterization 경로에는 위 문자열이 존재하므로 검색 대상에서 의도적으로 제외한다. Provider-neutral `adapters/common`에도 provider SDK match는 0이어야 한다.

Expected positive searches:

```bash
rg -n '<packaging>pom</packaging>|<module>|rpdptw-(core|solver|verification|application|profile-standard|adapter-common|cli|api|worker|test-fixtures|architecture-rules)' pom.xml rpdptw adapters apps build
rg -n 'com\.google\.cloud|maven-shade-plugin|OptimizationHttpServer' legacy/current-app
rg -n 'RESOLVED 25|OPEN — EXPERIMENT_REQUIRED 1|DEFERRED 2|TOTAL 28' docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
```

### 10.6 Move/diff/link 범위 검증

```bash
git diff --find-renames=100% --summary -- pom.xml Dockerfile README.md src legacy rpdptw adapters apps build
git diff --find-renames=100% --numstat -- src legacy/current-app/src
git diff --check -- pom.xml Dockerfile README.md src legacy rpdptw adapters apps build
git diff --name-only -- pom.xml Dockerfile README.md src legacy rpdptw adapters apps build
git status --short --untracked-files=all -- pom.xml Dockerfile README.md src legacy rpdptw adapters apps build
shasum -a 256 legacy/current-app/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/HttpJson.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/JsonSupport.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java legacy/current-app/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java
```

전체 `git status`에는 이 phase 밖의 기존 변경이 계속 보여야 한다. 구현자는 그 상태를 “정리”하지 않는다. 위 scoped diff와 시작 전 status snapshot의 차이로 AR-0 소유 변경만 판정한다.

## 11. Step-by-step implementation checklist

- [ ] Metadata의 다섯 source hash와 현재 파일 hash가 일치한다.
- [ ] 시작 전 전체 `git status --short --untracked-files=all`을 evidence에 보존했다.
- [ ] 예정 경로 overlap이 없고 다른 세션 파일을 건드리지 않음을 확인했다.
- [ ] Java 25.0.3/Maven 3.9.14와 격리된 current `mvn test`, `mvn verify` green을 기록했다.
- [ ] Current fat-JAR name/main class와 shade warning을 baseline으로 기록했다.
- [ ] Legacy engine characterization test를 production 수정 전에 작성했다.
- [ ] `BASELINE_RESOURCE_MISSING` red report를 보존했다.
- [ ] 최소 expected resource/helper로 characterization green을 만들었다.
- [ ] HTTP/workflow contract를 legacy-only로 명시하고 test로 고정했다.
- [ ] Source destination missing red와 packaging missing red를 각각 관찰했다.
- [ ] Architecture test와 invalid fixtures를 target POM보다 먼저 작성했다.
- [ ] `ReactorStructureTest`가 current root `jar`와 missing module을 정확히 보고했다.
- [ ] Dependency/internal/scope/cycle detector가 synthetic invalid fixture를 잡았다.
- [ ] README broken link red와 Docker legacy selector red를 관찰했다.
- [ ] 원본 POM/Dockerfile을 legacy destination에 내용 보존 이동했다.
- [ ] 기존 7개 Java file을 byte-for-byte 이동하고 SHA가 모두 일치했다.
- [ ] 새 root POM은 `packaging=pom`이며 root business dependency가 0이다.
- [ ] Exact aggregator/leaf POM 15개와 legacy POM 1개가 존재한다.
- [ ] Target parent convention과 plugin/dependency version을 고정했다.
- [ ] Allowed compile DAG 외 edge가 없다.
- [ ] `rpdptw-verification` → `rpdptw-solver` edge가 없다.
- [ ] Target core/solver/verification/application cloud/transport SDK가 0이다.
- [ ] Test-fixtures production scope가 0이다.
- [ ] Cross-module `.internal` dependency가 0이다.
- [ ] Target leaf에는 semantic production Java type이 없다.
- [ ] Root Dockerfile이 legacy fat JAR을 만들고 GCP files는 변경하지 않았다.
- [ ] README link와 reactor/legacy 실행 설명만 최소 변경했다.
- [ ] Legacy targeted test와 packaging IT가 green이다.
- [ ] Architecture targeted tests가 green이다.
- [ ] Architecture-rules `verify`가 green이다.
- [ ] 전체 reactor `mvn verify`가 green이다.
- [ ] Target clean rebuild digest가 일치하고 legacy digest는 main/class parity로만 비교했다.
- [ ] `rg` 금지 검색과 actual dependency tree를 evidence에 보존했다.
- [ ] `git diff --check`가 통과하고 scoped change가 §6 목록과 정확히 일치한다.
- [ ] Evidence bundle과 `handoff.md`가 완성되기 전 `DONE`으로 표시하지 않았다.

## 12. Deliverable와 evidence bundle

### 12.1 Deliverable

| Deliverable | 다음 소비자 사용법 |
|---|---|
| Java 25 parent/aggregator와 16 child project | 모든 후속 phase가 parent convention과 `-pl/-am` 명령을 그대로 사용 |
| `legacy/current-app` | AR-8 compatibility/shadow까지 rollback·characterization source로 유지 |
| Empty target leaf POMs | AR-1~AR-7이 owner module에 test부터 추가 |
| `rpdptw-test-fixtures` scope contract | AR-1이 core contract builder를 추가하되 consumer `test` scope 유지 |
| `rpdptw-architecture-rules` | 모든 phase의 reactor verify에서 dependency/package/document regression 차단 |
| Question/phase traceability report | 후속 phase가 source drift와 open/deferred 상태를 검증 |
| Legacy artifact/main report | Docker/current-app rollback과 AR-8 compatibility matrix 입력 |

### 12.2 Evidence bundle 위치와 필수 파일

구현 evidence는 `target/codex-evidence/AR-0/<evidence-id>/`에 둔다.

```text
target/codex-evidence/AR-0/<evidence-id>/
├── evidence.json
├── commands.log
├── baseline/
│   ├── git-status.txt
│   ├── source-sha256.txt
│   ├── toolchain.txt
│   ├── current-test.txt
│   ├── current-verify.txt
│   └── current-fat-jar-manifest.txt
├── red/
│   ├── legacy-baseline-resource.txt
│   ├── legacy-destination-missing.txt
│   ├── legacy-fat-jar-missing.txt
│   ├── reactor-structure.txt
│   ├── dependency-policy.txt
│   ├── internal-package.txt
│   ├── test-fixture-scope.txt
│   ├── documentation-link.txt
│   └── dockerfile-selector.txt
├── green/
│   ├── legacy-characterization/
│   └── architecture-rules/
├── regression/
│   ├── legacy-verify/
│   ├── architecture-rules-verify/
│   └── reactor-verify/
├── fingerprints/
│   ├── legacy-source-sha256.txt
│   ├── artifacts-sha256.txt
│   └── source-design-sha256.txt
├── diff/
│   ├── changed-files.txt
│   ├── rename-summary.txt
│   ├── diff-check.txt
│   ├── dependency-tree.txt
│   ├── reactor-graph.txt
│   └── forbidden-rg.txt
└── handoff.md
```

`evidence.json`에는 최소 `phaseId=AR-0`, `rmMapping=RM-0`, status, source design version/hash, git commit 또는 tree, Java/Maven version, 명령과 exit code, test count, artifact digest, blocker, 작성시각을 기록한다. Timestamp는 metadata이며 target artifact/domain identity가 아니다.

## 13. Rollback

Rollback 단위는 **AR-0 scoped path 전체**다. Broad `git reset --hard`, `git checkout -- .`, `git clean` 또는 workspace 삭제를 사용하지 않는다.

1. 시작 전 `git status`와 원본 hash manifest를 확인하여 AR-0 밖 사용자 변경을 식별한다.
2. AR-0 변경만 담은 binary diff/rename summary와 created-file manifest를 evidence `diff/`에 보존한다.
3. Rollback 필요 시 먼저 `git diff --check`와 legacy source SHA를 기록하고 실행을 중단한다.
4. 기존 7개 Java 파일은 hash가 baseline과 일치할 때만 `legacy/current-app/src/**`에서 원래 `src/**` 경로로 되돌린다. Mismatch가 있으면 사용자 변경으로 간주하고 자동 이동하지 않는다.
5. `legacy/current-app/pom.xml`과 `legacy/current-app/Dockerfile`의 hash가 원본 baseline과 일치할 때만 각각 root path 복원 source로 사용한다.
6. 새 root/aggregator/leaf POM, architecture test와 characterization test는 created-file manifest에 있는 exact path만 제거한다.
7. `README.md`는 AR-0 reverse patch hunk만 적용한다. 다른 문장 변경을 덮어쓰지 않는다.
8. `gcp/**`, `data/**`, 설계 문서와 다른 phase 문서는 rollback 대상이 아니다.
9. 복구 뒤 current `mvn test`, `mvn verify`, original fat-JAR name/main class를 다시 확인한다.

Logical cutover, state/artifact schema rollback은 AR-0에 존재하지 않는다. 이 phase의 rollback은 build layout과 byte-preserved legacy path에 한정한다.

## 14. DONE / BLOCKED 판정

### 14.1 `DONE` AND gate

다음을 모두 만족해야 `DONE`이다.

1. Source design hash와 entry baseline이 확인되었다.
2. Characterization 이전 current test/verify green evidence가 있다.
3. 각 build 변경에 대응하는 의도한 red와 targeted green report가 있다.
4. 기존 7개 Java file과 original legacy POM/Dockerfile의 content parity가 증명되었다.
5. Legacy current-app unit/characterization/packaging test가 통과한다.
6. Root 포함 17 project가 exact coordinate/path로 build되고 cycle이 0이다.
7. Java 25/Maven Enforcer, pinned plugin, target reproducible archive convention이 모든 target leaf에 적용된다.
8. Allowed dependency DAG 외 edge, target cloud/transport dependency, verification→solver edge가 0이다.
9. Cross-module `.internal` reference와 test-fixture production leakage가 0이다.
10. Synthetic invalid fixture가 각 architecture rule을 실제로 깨뜨린다는 detector evidence가 있다.
11. Normative link/anchor, AR/RM mapping과 28-question status 검증이 통과한다.
12. `mvn -pl build/architecture-rules -am verify`와 전체 `mvn verify`가 통과한다.
13. `git diff --check`, scoped change manifest, forbidden `rg`가 통과한다.
14. Evidence bundle과 다음 phase `handoff.md`가 완전하다.
15. Blocker가 남지 않았고 AR-0 밖 파일 변경이 0이다.

### 14.2 `BLOCKED`

다음 중 하나면 마지막 green 지점에서 멈추고 `BLOCKED`로 기록한다.

- Source hash drift 또는 예정 path overlap의 소유권을 확인할 수 없음
- 기존 behavior/content를 바꾸지 않고 legacy module로 이동할 수 없음
- Toolchain 조건을 만족하고 다른 Maven build와 `target/`을 공유하지 않는 격리 환경에서도 baseline test/verify가 실패함
- Red가 의도한 assertion이 아니라 환경/다운로드/권한/unrelated failure임
- Exact module/DAG가 Maven cycle 또는 semantic owner 충돌을 만들며 계획/ADR 변경이 필요함
- Architecture rule을 통과하려면 provider, public API, Q-BENCH 값, fixture 변환 또는 optional variant를 결정해야 함
- 전체 reactor green을 위해 test skip, Enforcer skip, architecture module 제외 또는 legacy test 삭제가 필요함
- Diff가 §6 밖의 사용자/다른 phase 파일을 포함함

`Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` 자체는 AR-0 blocker가 아니다. 그것을 해결한 척하는 값·provider·variant가 필요해지는 상황이 blocker다.

## 15. 다음 phase handoff

AR-1은 다음 identity/evidence를 입력으로 받아야 한다.

- AR-0 evidence ID와 source design hashes
- Root parent coordinate/version과 exact module graph
- `rpdptw-core` owner POM과 Java 25/plugin convention
- `rpdptw-test-fixtures`의 test-only scope rule; 아직 public builder API 없음
- Architecture rule 실행 명령과 allowed dependency/package boundary
- Legacy source hash manifest와 current behavior report
- `Q-BENCH-02`/fixture/provider/variant guard 상태

AR-1 entry gate는 AR-0 `DONE`이다. AR-1은 `rpdptw-core`의 input/domain/travel test를 production type보다 먼저 작성하고 `rpdptw-test-fixtures` API를 그 계약에 맞춰 제안한다. AR-0은 `CanonicalInput`, `ProblemInstance`, `PreparedTravel` 또는 external JSON schema를 미리 정의하지 않는다.

AR-6의 provider-neutral port/local skeleton은 AR-0 `DONE` 뒤 병행할 수 있지만, 정상 publication/retrieval 완료 주장은 AR-5를 기다린다. Physical provider adapter는 이 handoff에 포함되지 않는다.

## 16. Scope exclusions와 금지 shortcut

AR-0은 다음을 구현하지 않는다.

- RPDPTW input/domain/normalization/travel/propagation/evaluation/search/verifier/result type
- `CanonicalInput`, `ProblemInstance`, `PreparedTravel`, `BoundProfile` 등의 semantic API
- Public HTTP/wire/storage schema 승인 또는 legacy endpoint cutover
- Provider adapter, cloud product 선택, `deployment/`와 production infrastructure
- `Q-BENCH-02` 숫자, README 예시값의 official default 승격
- Win fixture `D/U` 변환, 반올림, 정수 fixture 생성 또는 official baseline
- Optional variant, multi-trip/rotation, route pool/MIP, apply/undo
- Legacy finalizer를 두-verifier target 구현으로 인정하거나 수정하는 일
- Legacy module 삭제, endpoint 변경, shadow comparison 또는 logical cutover

금지 shortcut:

- Characterization red를 보지 않고 production file부터 이동
- Test 0건, compile 환경 오류 또는 dependency download 실패를 red evidence로 사용
- Architecture detector를 증명하려고 실제 target POM에 cloud dependency/cycle을 임시 추가
- Root POM에 모든 dependency를 넣고 leaf가 transitive로 받게 함
- `mvn verify -DskipTests`, Enforcer skip, architecture-rules module 제외
- Empty placeholder domain interface/class를 만들어 “skeleton complete”라 주장
- General broken-link allowlist, broad package ignore 또는 artifact substring 기반 dependency 허용
- Current GCP topology를 target provider 결정으로 승격
- 다른 세션의 dirty file을 restore, stage, delete 또는 format

## 17. 문서 작성 세션 검증 기록

작성 전 다음이 확인되었다.

- 전체 구현 계획 857행과 Master/Architecture/Domain Design의 전체 문맥을 읽었다.
- 구현 계획 metadata의 세 design SHA-256과 실제 파일 SHA-256이 일치했다.
- Root POM/source/test/README/Dockerfile/GCP/data를 직접 조사했다.
- Java/Maven version, current `mvn test`, current `mvn verify`, fat-JAR manifest를 실행 확인했다.
- 이 문서는 구현 계획 §10의 13개 항목을 §2~§16에 모두 대응시킨다.

이 절의 최종 link/heading/diff/source-drift 검증 결과는 문서 작성 완료 직후 다시 실행해 handoff 응답에 보고한다.
