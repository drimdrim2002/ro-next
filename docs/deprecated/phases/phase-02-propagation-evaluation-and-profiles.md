# AR-2 / RM-2 — 전파, 평가와 불변 프로파일 구현 계획

```yaml
phase: AR-2
rm_mapping: RM-2
status: BLOCKED
document_role: 후속 구현 세션이 테스트를 먼저 작성해 route full propagation, 평가 계층, exact comparator, SolvePlan과 immutable BoundProfile을 구현하기 위한 실행 명세
language: ko
repository_root: /Users/brown/workspace/ro-next
source_baseline:
  implementation_plan:
    status: REVIEW
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  master_design:
    version: 3.2-review
    status: REVIEW
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    version: 1.1-review
    status: REVIEW
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    version: 2.2-review
    status: REVIEW
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
  question_register:
    version: 2.1-review
    status: REVIEW
    sha256: 3d6bc496b8df98a10534338828dd7e845b50ea967afa884642403405e613c088
required_predecessors:
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    required_status: DONE
    observed_at_authoring: ABSENT
    observed_at_final_verification: NOT_STARTED
    sha256_at_final_verification: 12722696b430f693f2c4db9df760ed6acb6a463eb49cfa398927936426d01e52
  - path: docs/codex/phases/phase-01-input-domain-and-travel.md
    required_status: DONE
    observed_at_authoring: ABSENT
    observed_at_final_verification: BLOCKED
    sha256_at_final_verification: a2f57d1bbfd519a97feec4f7811e1600e19efe201200cbfd14929c19a3c453b3
shared_build_baseline:
  source: phase-00 isolated execution observation supplied by the management session
  java: 25.0.3-amzn
  maven: 3.9.14
  legacy_test: PASS
  legacy_verify: PASS
  concurrent_target_failures_are_authoritative: false
entry_artifacts:
  - ProblemInstance
  - PreparedTravel
  - dense external-to-core identity mapping
  - AR-1 hand-calculated problem/travel fixtures
  - AR-0 reactor and architecture-rule harness
implementation_blocker:
  id: AR2-BLOCK-ENTRY-01
  reason: AR-0 문서는 NOT_STARTED이고 AR-1 문서는 BLOCKED이며 DONE evidence와 target Maven modules가 아직 존재하지 않음
```

## 1. 문서 목적과 현재 판정

이 문서는 [전체 구현 계획 §9.3](../implementation-plan.md#93-ar-2--rm-2--propagation-evaluation과-profiles)의 `AR-2 / RM-2`만 다룬다. 목표는 다음 후속 단계가 raw 입력이나 고객 문자열을 재해석하지 않고 사용할 수 있는 네 가지 권위 산출물을 만드는 것이다.

1. `ProblemInstance`, `PreparedTravel`과 불변 route sequence만 소비하는 one-pass full propagation
2. hard feasibility, neutral metric, score, objective/comparator와 `SolvePlan`의 단방향 평가 계층
3. exact customer/profile/version/preset/dependency closure를 한 problem에 bind한 immutable `BoundProfile`
4. cache나 공유 scratch 없이 같은 입력을 언제든 전체 재계산할 수 있는 insertion/evaluation reference seam

문서 작성 시점의 구현 착수 판정은 `BLOCKED`다. 최초 조사 때 [AR-0 문서](phase-00-baseline-and-build-architecture.md)와 [AR-1 문서](phase-01-input-domain-and-travel.md)가 모두 없었고, 최종 문서 검증 시점에는 다른 세션이 두 문서를 작성했지만 각각 `NOT_STARTED`, `BLOCKED`다. AR-0/AR-1 `DONE` evidence, target reactor, 구현된 `ProblemInstance`와 `PreparedTravel`은 여전히 없다. 이 상태에서는 아래 테스트 파일도 올바른 owner module에 추가할 수 없다. Blocker가 해소되면 구현자는 별도 API 질문 없이 이 문서의 계획상 고정 제안을 사용하되, 실제 AR-1 handoff 타입과 충돌하는 경우 §2.4의 change-control을 먼저 수행한다.

`Q-BENCH-02`의 공식 step/worker/round/watchdog 값 부재는 이 phase의 blocker가 아니다. `SolvePlan`은 숫자를 발명하지 않고 명시적 `BudgetRef`만 보존한다. `Q-INFRA-01`과 `Q-VAR-01`은 이 phase에서 활성화하지 않는 deferred 범위다.

공통 build baseline은 관리 세션이 전달한 phase-00 격리 실행 관찰인 Java `25.0.3-amzn`, Maven `3.9.14`, 기존 `mvn test`/`mvn verify` 성공이다. 여러 phase 문서 세션이 같은 working directory의 `target/`을 동시에 사용해 발생한 shade JAR replace, report overwrite, rename 같은 transient failure는 repository baseline 결함이나 phase blocker evidence가 아니다. 그런 실패는 동시 실행이 끝난 격리 환경에서 같은 명령을 재실행하기 전까지 `CONCURRENT_TARGET_COLLISION_SUSPECTED`로만 기록한다.

## 2. Authority, 결정 상태와 blocker

### 2.1 규범 근거

| Authority | 이 phase가 소비하는 계약 |
|---|---|
| [Master Design §9](../../master-design.md#9-extensible-policy-evaluation과-profile-architecture) | normalization → propagation/hard gate → neutral metric → hard constraint → score → objective/comparator → `SolvePlan`의 단방향 책임, exact binding과 customer isolation |
| [Master Design §15.4](../../master-design.md#154-rm-2--propagation-evaluation과-bound-profile) | RM-2 entry/deliverable/금지/exit evidence |
| [Domain Design §11](../../domain-design.md#11-route-propagation과-resources) | one-pass state, stop의 location-transition 의미, actual-arc drive resource, multi-trip 금지 경계 |
| [Domain Design §12](../../domain-design.md#12-evaluation-profile과-objective) | 평가 계층, exact `BoundProfile`, mandatory·ownership objective, portfolio config 경계 |
| [Domain Design §16](../../domain-design.md#16-acceptance-evidence) | numeric/time/compatibility/travel/state/result/reproducibility acceptance 사례 |
| [Architecture Design §6~§9](../../architecture-design.md#6-module-responsibility와-package-boundary) | `rpdptw-core` package owner, profile JAR, dependency DAG, namespace, customer extension seam |
| [Architecture Design §18~§19](../../architecture-design.md#18-test-structure와-module-evidence) | module evidence, reactor order와 `AR-2` gate |
| [질문 등록부](../../master-design-open-questions.md) | `Q-TIME-03~04`, `Q-IN-02`, `Q-COMP-01~02`, `Q-REQ-01~02`, `Q-OBJ-01~03`의 resolved 의미와 open/deferred 상태 |

세 설계와 이 계획은 모두 `REVIEW`다. 따라서 아래 Java 이름과 signature는 **승인된 외부 public API가 아니라 이 phase 문서가 후속 구현 세션의 모호성을 없애기 위해 고정하는 계획상 제안 API**다. Public HTTP field, JSON, 저장 schema, profile 배포 형식 또는 provider topology를 승인하지 않는다.

### 2.2 적용 결정

| 결정 | 적용 |
|---|---|
| `Q-TIME-03` | `START_ONLY`와 `COMPLETE_WITHIN_WINDOW`는 typed propagation policy로 bind하며 close는 포함 경계다. |
| `Q-TIME-04` | arc 전체가 한 work window에 들어가야 한다. 그렇지 않으면 다음 work start에서 arc 전체를 다시 시작하고 partial drive는 누적하지 않는다. |
| `Q-IN-02` | stop/drive limit는 route 전체 누적이며 rest/day에서 reset하지 않는다. `waitInDepot`은 wait 위치만 바꾼다. |
| `Q-COMP-01~02` | size/capability/zone은 structural/static 또는 bound hard gate이며 score로 완화하지 않는다. |
| `Q-REQ-01` | delivery-only initial load와 real pickup/delivery load 증감을 한 route에서 정확히 전파한다. |
| `Q-REQ-02` | oneway와 single roundtrip만 처리한다. multi-trip/rotation은 구현하지 않는다. |
| `Q-OBJ-01` | exact customer/profile/version과 해당 customer가 승인한 exact preset만 resolve한다. 생략은 descriptor의 exact default preset을 사용한다. |
| `Q-OBJ-02` | `mandatoryUnassignedCount`는 지원 preset에서 최상위 사전식 dimension이며 hard rule/finite penalty가 아니다. |
| `Q-OBJ-03` | 입력 `DIRECT/LEASE` vehicle만 resource다. `LEASE` volume objective는 지원 preset에서만 허용하며 미배정을 외주/이월로 바꾸지 않는다. |

### 2.3 현재 blocker와 비-blocker

| ID | 상태 | 영향 | 재개 조건 |
|---|---|---|---|
| `AR2-BLOCK-ENTRY-01` | `BLOCKED` | 이 phase의 모든 production/test 구현 | AR-0과 AR-1 문서가 `DONE`, evidence bundle digest가 존재하고 target module과 `ProblemInstance`/`PreparedTravel` API가 실제로 build됨 |
| `AR2-AUTH-API-01` | 제한 | 계획상 내부 API 구현은 가능하나 외부 public API로 선언 불가 | External contract 또는 ADR 승인 시 이 문서와 상위 계획을 같은 변경 단위에서 갱신 |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | 공식 numeric `SolvePlan`/benchmark manifest만 차단 | 이 phase는 숫자 없는 `BudgetRef`와 fixture-local test ref로 진행 가능 |
| `Q-INFRA-01` | `DEFERRED` | Provider/product/physical registry 배포 | 이 phase blocker가 아니며 구현 범위에서 제외 |
| `Q-VAR-01` | `DEFERRED` | Optional variant/multi-trip 의미 | 이 phase blocker가 아니며 현재 pair/terminal 계약을 그대로 유지 |
| Win fixture decimal `D/U` | 비준수 | Official Win baseline과 fixture 변환 | Hand-calculated integer fixture를 새로 test 안에서 만들 수 있으므로 AR-2 generic 구현은 진행 가능 |

### 2.4 충돌과 change-control

구현 시작 직전에 AR-1이 이 문서의 `RouteSequence`, delivery-only logical pickup 표현 또는 ID accessor와 다른 승인된 계획상 제안 API를 제공하면 임의 adapter, reflection 또는 `Map<String,Object>`를 추가하지 않는다.

1. 실제 AR-1 타입과 이 문서 §6 signature의 차이를 표로 기록한다.
2. 의미가 같은 이름/shape 차이면 이 문서의 모든 경로·signature·test fixture를 한 변경 단위에서 수정한다.
3. pair, terminal, service pattern, prepared travel 또는 unit 의미 차이면 `AR2-BLOCK-CONTRACT-02`로 중단하고 Master/Domain/Architecture와 AR-1 문서 영향 승인을 받는다.
4. 변경 전후 source hash와 ADR/approval reference를 evidence에 남긴다.

후속 구현자가 임의로 이름을 선택하거나 양쪽 API를 동시에 유지하는 compatibility layer를 만드는 것은 허용하지 않는다.

## 3. 작성 시점 저장소 inventory

### 3.1 Working tree snapshot

문서 작성 전 `/Users/brown/workspace/ro-next`에서 실행한 `git status --short`는 다음과 같았다. 이 변경은 사용자 또는 다른 세션 소유이며 이 phase 문서 작성 세션은 수정·복원하지 않는다.

```text
 D "HGS_CVRP_QA_정리.md"
 M docs/architecture-design.md
 M docs/arranged/02_initial_solution_heuristics.md
 M docs/domain-design.md
 M docs/master-design-open-questions.md
 M docs/master-design-sessions/README.md
 M docs/master-design.md
?? data/
?? docs/.master-design.md.swp
?? docs/.obsidian/
?? docs/codex/
?? docs/master-design-beginner-guide.md
?? docs/master-design-revised.md
?? docs/master-design-sessions/01-gcp-architecture.md
?? docs/master-design-sessions/02-rpdptw-terminology.md
?? docs/master-design-sessions/03-rpdptw-model-recheck.md
?? docs/master-design-sessions/04-objective-policy-flexibility.md
?? docs/master-design-sessions/05-vehicle-size-and-constraints.md
?? docs/master-design-sessions/06-request-pair-invariants.md
?? docs/master-design-sessions/07-termination-and-reproducibility.md
?? docs/master-design-sessions/08-candidate-copy-and-rollback.md
?? docs/master-design-sessions/10-domain-limits-and-constants.md
?? docs/master-design-sessions/17-optional-variant-feasibility.md
?? docs/master-design-sessions/18-document-governance.md
?? docs/master-design-sessions/19-26-scheduler-log.md
?? docs/master-design-sessions/19-integration-plan.md
?? docs/master-design-sessions/26-master-review.md
?? docs/master-design-sessions/27-28-correction-scheduler-log.md
?? docs/master-design-sessions/27-review-corrections.md
?? docs/master-design-sessions/28-master-re-review.md
?? docs/master-design-sessions/29-open-question-interview.md
?? docs/master-design-sessions/31-domain-design-integration.md
?? docs/orgin/HGS_CVRP_QA_정리.md
```

### 3.2 POM과 module graph

현재 root `pom.xml`은 `com.ronext:ro-next:0.1.0-SNAPSHOT` 단일 `jar`이고 `<modules>`가 없다. Java release 25, Maven `[3.9.14,)`, Java `[25,26)` Enforcer는 있으나 Google Workflow Executions, Cloud Storage, Jackson, JUnit과 shade packaging이 한 module에 직접 있다. 실제 toolchain 관찰은 Maven `3.9.14`, Amazon Corretto `25.0.3`이다.

기존 repository의 격리 baseline은 `mvn test`와 `mvn verify` 모두 성공이다. 이 문서 세션은 동시 `target/` 충돌을 피하고 source artifact를 만들지 않기 위해 Maven test/verify를 다시 실행하지 않는다.

현재 compile graph:

```text
ro-next:jar
├── google-cloud-workflow-executions
├── google-cloud-storage
├── jackson-databind
└── jackson-datatype-jsr310
```

AR-2가 소비해야 할 선행 graph:

```text
rpdptw-core
├── rpdptw-profile-standard → rpdptw-core
├── build-test-fixtures → rpdptw-core        # test-only consumer
└── build-architecture-rules → core + profile bytecode/test surface
```

`rpdptw-core`가 `rpdptw-profile-standard`를 역으로 참조하거나 profile module이 solver/search/provider를 참조하면 중단한다. AR-2는 root aggregator/module 구조를 만들지 않는다. 위 module이 없으면 AR-0 blocker이지 AR-2가 대신 고칠 POM 작업이 아니다.

### 3.3 Source, test, README, GCP와 data

| 영역 | 실제 상태 | AR-2 해석 |
|---|---|---|
| Production | `com.ronext.optimizer` 아래 6개 class뿐. `AlnsBatchEngine`은 `Map<String,Object>`와 `double objective` placeholder를 반환 | 전파/평가/profile 구현이 0개. Legacy type을 상속·이동·재사용하지 않음 |
| Test | `AlnsBatchEngineTest` 1개. status/run number/양의 `double objective`만 확인 | RPDPTW propagation, comparator, profile evidence가 0개 |
| README | `parallelRuns=8`, `iterationsPerRun=5000`, GCP/Cloud Run/Storage를 실행 기준으로 기술 | Legacy characterization 값이며 `Q-BENCH-02` 공식값이나 target profile default가 아님 |
| GCP workflow | run number별 worker 호출 후 prefix의 후보 중 `double objective` 최솟값 finalization | Exact comparator, complete batch, profile binding evidence가 아님 |
| Root POM | Cloud SDK/Jackson/shade main이 root에 결합 | AR-0 migration 전 target core에 어떤 dependency도 복사하지 않음 |
| `data/win_poc_case.json` | SHA-256 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`, 소수 문자열 `D/U` 포함 | Read-only negative inventory. 변환·반올림·official fixture 사용 금지 |

현재 목표 경로 `rpdptw/core`, `rpdptw/profiles/standard`, `build/test-fixtures`, `build/architecture-rules`는 존재하지 않는다. 동시 세션이 작성한 phase-00/01 문서는 각각 `NOT_STARTED`/`BLOCKED`이고 production artifact/evidence handoff는 아직 없다.

## 4. 현 상태 → 목표 gap과 dependency closure

| Capability | 현재 | AR-2 목표 | 다음 소비자 |
|---|---|---|---|
| Route propagation | 없음 | start부터 end까지 한 번 순회해 load/time/window/wait/rest/stop/drive/resource를 exact integer로 계산 | AR-3 insertion, AR-5 candidate verifier |
| Hard feasibility | Legacy placeholder objective뿐 | structural/static/physical/composed constraint를 typed rejection으로 반환하고 score 경로에 진입시키지 않음 | AR-3, AR-4, AR-5 |
| Neutral metric | 없음 | 가격·선호 없는 unit-aware route/solution metric snapshot | Profile score, verifier |
| Score | 하나의 `double` | feasible facts만 소비하는 checked integer component breakdown | Objective vector |
| Objective/comparator | `Comparator.comparingDouble` | exact ordered dimensions + canonical structural tie key의 stable total order | Portfolio/ALNS/champion |
| `SolvePlan` | 없음 | stage order, exact objective refs, no-worse guard, 숫자 없는 budget refs | AR-4 stage execution |
| Profile registry | 없음 | explicit provider list를 stable identity로 정렬하고 duplicate/unknown/latest/cross-customer를 거부 | App assembly, AR-3~5 |
| `BoundProfile` | 없음 | problem/profile/preset/config/dependency closure에 bind된 immutable snapshot/fingerprint | AR-3~5 |
| Full recomputation | 없음 | cache-free evaluator와 insertion reference seam | AR-3 selection equality, AR-4 cache equality, AR-5 verifier |
| Isolation | 없음 | 호출/problem/run/thread 간 mutable scratch 0, core customer/provider branch 0 | 전체 후속 phase |

Dependency closure는 다음 AND 조건이다.

```text
exact ProfileIdentity(customer, profile, version)
+ exact PresetKey (explicit or descriptor-declared exact default)
+ ProfileSchemaVersion
+ ImplementationContractVersion
+ every hard-constraint descriptor/version/config
+ every neutral metric descriptor/unit/value schema/version
+ every score descriptor/input metric dependency/config/version
+ every objective dimension/direction/source/version
+ SolvePlan stage/objective/guard/operator-capability/budget references
+ optional typed facet + candidate-verifier support versions
+ normalized problem ID/unit/fact-contract fingerprint
= immutable BoundProfile fingerprint
```

Missing reference, duplicate key, unit/value-schema mismatch, unknown facet, `LEASE`/outsourced objective mismatch 또는 unresolved stage reference 중 하나라도 있으면 solve 전에 typed binding error다. Classpath discovery 순서, reflection scan, “먼저 발견된 provider”, `latest`, 비슷한 이름 또는 다른 customer fallback은 closure에 포함되지 않으며 금지한다.

## 5. 정확한 예상 경로

모든 경로는 repository root 기준이다. `종류=선행 확인`은 AR-0/AR-1 owner이며 AR-2가 새로 만들지 않는다. 해당 파일이 없거나 역할이 다르면 entry blocker로 중단한다.

### 5.1 POM과 선행 contract

| 종류 | 경로 | AR-2 작업 |
|---|---|---|
| 선행 확인 | `pom.xml` | `packaging=pom`, module 등록, Java/Maven/plugin convention 확인만 함 |
| 선행 확인 | `rpdptw/pom.xml` | core/profile aggregator와 dependency 방향 확인만 함 |
| 변경 예상 | `rpdptw/core/pom.xml` | AR-0이 관리한 test/property dependency만 사용. Cloud/Jackson/profile/solver dependency 추가 금지 |
| 변경 예상 | `rpdptw/profiles/standard/pom.xml` | compile dependency를 `rpdptw-core` 하나로 제한; test fixture는 test scope |
| 변경 예상 | `build/test-fixtures/pom.xml` | test-only artifact가 `rpdptw-core`를 compile-consume하도록 하되 production runtime leakage 금지 |
| 변경 예상 | `build/architecture-rules/pom.xml` | AR-0 architecture harness로 core/profile bytecode와 synthetic negative fixture 검사 |
| 선행 확인 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/**` | AR-1 `ProblemInstance`, IDs, node/service/vehicle/terminal contract |
| 선행 확인 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/**` | AR-1 complete `PreparedTravel`과 fingerprint |

### 5.2 테스트와 fixture — production보다 먼저 생성

| 순서 | 정확한 경로 | 책임 |
|---:|---|---|
| 1 | `rpdptw/core/src/test/java/com/ronext/rpdptw/propagation/RoutePropagatorApiCompilationTest.java` | 첫 API red; 허용되는 유일한 초기 compile failure |
| 2 | `rpdptw/core/src/test/java/com/ronext/rpdptw/propagation/RoutePropagatorTest.java` | full-arc restart, wait/window/service, mixed load, stop, resource hand cases |
| 3 | `rpdptw/core/src/test/java/com/ronext/rpdptw/propagation/RoutePropagatorPropertiesTest.java` | full traversal, prefix capacity, exact aggregation property |
| 4 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/FullEvaluationEngineTest.java` | hard/metric/score/objective 단방향과 infeasible score 차단 |
| 5 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/ProfileRegistryTest.java` | provider order 독립, duplicate identity/key 거부, immutable snapshot |
| 6 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/ProfileBinderTest.java` | missing/duplicate/unit/schema/reference/LEASE mismatch |
| 7 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/CustomerProfileIsolationTest.java` | exact customer/profile/version/preset, `latest`와 cross-customer 거부 |
| 8 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/LexicographicComparatorPropertiesTest.java` | antisymmetry/transitivity/totality/dimension priority/stable tie |
| 9 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/SolvePlanStageGuardTest.java` | protected objective no-worse guard와 hard rule 불변 |
| 10 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/runtime/EvaluationIsolationTest.java` | problem/call/thread 간 shared scratch와 bound array 누출 방지 |
| 11 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/insertion/PairInsertionEvaluationContractTest.java` | base route 무변경과 materialized full recomputation equality |
| 12 | `rpdptw/profiles/standard/src/test/java/com/ronext/rpdptw/profile/standard/StandardEvaluationCatalogTest.java` | 공통 neutral catalog의 unit/formula와 가격 부재 |
| 13 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testing/evaluation/HandCalculatedEvaluationFixtures.java` | AR-1 builder를 소비한 작은 integer problem/travel/route oracle |
| 14 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testing/profile/TestProfileProvider.java` | production bundle에 들어가지 않는 exact two-customer test descriptors |
| 15 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/EvaluationLayerArchitectureTest.java` | package 방향, propagation의 price/score/customer 참조 금지 |
| 16 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/ProfileModuleDependencyArchitectureTest.java` | core→profile 및 profile→solver/provider/internal 참조 금지 |

### 5.3 Production — 각 대응 test의 의도한 red 확인 뒤 생성

| Package | 정확한 경로 | 계획상 제안 type |
|---|---|---|
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/RouteSequence.java` | Explicit terminal/service node order와 vehicle binding |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/SolutionEvaluationInput.java` | Immutable routes + bank request partition view |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/SolveEvaluationContext.java` | `ProblemInstance` + `PreparedTravel` + `BoundProfile` |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/PropagationPolicy.java` | Window/departure 같은 price-free bound physical policy |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/RoutePropagationFacts.java` | Node facts와 exact route aggregate |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/PropagationResult.java` | Feasible facts 또는 typed physical/structural violation |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/PropagationFailure.java` | Stable failure code, node/arc/request scope와 defect category |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/PropagationFailureCode.java` | §6.1의 exact failure enum |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/EvaluationFacts.java` | Route/solution policy SPI가 소비하는 sealed fact view |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/RouteEvaluation.java` | Route feasibility/facts/metric/score result |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/SolutionEvaluation.java` | Partition, route result와 objective를 가진 full result |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/HardConstraint.java` | Composed hard constraint SPI |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ConstraintResult.java` | Pass 또는 typed rejection evidence |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ConstraintDescriptor.java` | Key/version/fact dependency와 typed parameter schema |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/MetricContributor.java` | Facts → unit-aware neutral value |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/MetricSnapshot.java` | Exact key/unit/value immutable snapshot |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/MetricDescriptor.java` | Metric key/version/unit/value schema/dependency |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/MetricValue.java` | Exact unit-aware checked integer value |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ScoreComponent.java` | Feasible metric snapshot → checked score value |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ScoreBreakdown.java` | Exact score component values |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ScoreDescriptor.java` | Score key/version/metric dependencies/config schema |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ScoreValue.java` | Exact checked score component |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ObjectiveKey.java` | Exact objective identity |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ObjectiveValue.java` | Key/schema/direction/exact integer value |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ObjectiveVector.java` | Ordered exact integer dimensions |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ObjectiveSchema.java` | Ordered key/direction/source/version contract |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/EvaluatedSolution.java` | Objective vector와 collision-safe stable structural key |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/StableSolutionKey.java` | Canonical vehicle/route/node/bank lexicographic tie key |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ObjectiveComparator.java` | Quality vector + stable structural tie total order |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/SolvePlan.java` | Immutable stages and exact references |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/SolveStage.java` | Active/protected objective, operator capability와 budget ref |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/StageKey.java` | Exact stage identity |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/BudgetRef.java` | 숫자를 소유하지 않는 exact config reference |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/OperatorCapabilityKey.java` | AR-4가 구현할 capability reference |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/StageGuard.java` | Protected objective prefix contract |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/StageGuardResult.java` | Pass 또는 offending key/value rejection |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/CustomerKey.java` | Typed exact customer identity |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ProfileKey.java` | Typed exact profile identity |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ProfileVersion.java` | Blank/`latest`를 거부하는 exact version |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/PresetKey.java` | Customer-scoped exact preset identity |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ProfileIdentity.java` | Customer/profile/version composite identity |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ProfileSelection.java` | Request customer + exact requested identity/preset |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ProfileDescriptor.java` | Components, preset, default, schemas and versions |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ProfileProvider.java` | Explicit registry input contract |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/DependencyClosure.java` | Stable sorted component/fact/metric/score/objective/plan closure |
| `evaluation.api` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/BoundProfile.java` | Solve-bound immutable closure and fingerprint |
| `propagation` | `rpdptw/core/src/main/java/com/ronext/rpdptw/propagation/RoutePropagator.java` | Stateless full-propagation interface |
| `propagation` | `rpdptw/core/src/main/java/com/ronext/rpdptw/propagation/OnePassRoutePropagator.java` | Forward-only exact implementation |
| `propagation.internal` | `rpdptw/core/src/main/java/com/ronext/rpdptw/propagation/internal/WorkWindowTravelResolver.java` | Full-arc fit/restart 계산 |
| `propagation.internal` | `rpdptw/core/src/main/java/com/ronext/rpdptw/propagation/internal/RouteStructureGate.java` | Terminal/pair/service/static pre-propagation gate |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/ImmutableProfileRegistry.java` | Explicit providers의 deterministic registry |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/ProfileBinder.java` | Exact selection + dependency closure binding |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/ProfileBindingException.java` | Typed pre-solve bind error |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/ProfileBindingErrorCode.java` | §6.5의 exact bind error enum |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/ProfileRegistryException.java` | Duplicate/unstable registry error |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/FullEvaluationEngine.java` | Propagation → constraint/metric/score/objective full recomputation |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/ExactLexicographicComparator.java` | Exact dimension order와 canonical tie |
| `evaluation.runtime` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/NoWorseObjectivePrefixGuard.java` | Stage protected-prefix guard |
| `evaluation.insertion` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/PairInsertionQuery.java` | Request + target route + pickup/delivery positions |
| `evaluation.insertion` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/InsertionEvaluation.java` | Side-effect-free feasible/rejected option result |
| `evaluation.insertion` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/PairInsertionEvaluator.java` | AR-3이 소비할 interface |
| `evaluation.insertion` | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/FullRecomputationPairInsertionEvaluator.java` | Materialize immutable route copy → full evaluator reference |
| `profile.standard` | `rpdptw/profiles/standard/src/main/java/com/ronext/rpdptw/profile/standard/StandardEvaluationCatalog.java` | Customer-neutral constraint/metric/objective component catalog |
| `profile.standard` | `rpdptw/profiles/standard/src/main/java/com/ronext/rpdptw/profile/standard/StandardMetricKeys.java` | Unit-aware common physical metric keys |
| `profile.standard` | `rpdptw/profiles/standard/src/main/java/com/ronext/rpdptw/profile/standard/StandardObjectiveKeys.java` | Mandatory/unassigned/ownership-volume 등 조립 가능 key |

이 phase에서 source/resource를 이동하거나 삭제하지 않는다. `src/main/java/com/ronext/optimizer/**`, root README, `gcp/**`, `data/**`는 변경하지 않는다. 위 exact 파일 목록을 바꿔야 하면 §2.4를 먼저 적용한다.

## 6. 계획상 제안 API와 error model

이 절의 모든 Java type/signature는 **계획상 제안 API**다. 외부 public API 승인이 아니다. Java `record`, sealed interface와 immutable collection의 최종 세부는 구현 중 바꿀 수 없으며, 바꿔야 하면 이 문서를 먼저 갱신한다.

### 6.1 Propagation과 full evaluation

```java
public record RouteSequence(
        VehicleId vehicleId,
        List<SolverNodeId> orderedNodeIds) {
}

public record SolutionEvaluationInput(
        List<RouteSequence> routes,
        Set<RequestId> bankRequestIds) {
}

public record SolveEvaluationContext(
        ProblemInstance problem,
        PreparedTravel travel,
        BoundProfile profile) {
}

public interface RoutePropagator {
    PropagationResult propagate(
            RouteSequence route,
            ProblemInstance problem,
            PreparedTravel travel,
            PropagationPolicy policy);
}

public final class FullEvaluationEngine {
    public RouteEvaluation evaluateRoute(
            RouteSequence route,
            SolveEvaluationContext context);

    public SolutionEvaluation evaluateSolution(
            SolutionEvaluationInput solution,
            SolveEvaluationContext context);
}
```

`RouteSequence`는 start terminal을 첫 node로 명시한다. Single roundtrip은 같은 depot/end terminal을 마지막 node로 명시하고 oneway는 마지막 physical service node로 끝난다. AR-1이 delivery-only logical pickup을 explicit prefix node로 표현하면 그 node는 start 직후의 연속 prefix에만 있고 travel/stop/service를 만들지 않는다. AR-1이 equivalent initial-load representation을 승인했다면 §2.4로 이 signature와 test fixture를 먼저 갱신한다.

`PropagationResult`와 `RouteEvaluation`은 hard-infeasible을 exception이나 큰 숫자로 표현하지 않는다.

```java
public sealed interface PropagationResult {
    record Feasible(RoutePropagationFacts facts) implements PropagationResult {}
    record Infeasible(PropagationFailure failure) implements PropagationResult {}
}

public enum PropagationFailureCode {
    STRUCTURAL_PAIR_PARTIAL,
    STRUCTURAL_PAIR_DUPLICATE,
    STRUCTURAL_PRECEDENCE,
    WRONG_TERMINAL,
    INTERNAL_DEPOT_REVISIT,
    STATIC_VEHICLE_INCOMPATIBLE,
    NO_WORK_WINDOW_FOR_FULL_ARC,
    PLAN_END_EXCLUDED,
    SERVICE_WINDOW_VIOLATION,
    COMPLETION_DEADLINE_VIOLATION,
    LOAD_BELOW_ZERO,
    CAPACITY_EXCEEDED,
    STOP_LIMIT_EXCEEDED,
    DRIVE_TIME_LIMIT_EXCEEDED,
    DRIVE_DISTANCE_LIMIT_EXCEEDED,
    PREPARED_TRAVEL_IDENTITY_MISMATCH,
    CHECKED_ARITHMETIC_OVERFLOW
}
```

구조 결함과 unresolved travel identity는 정상 insertion infeasibility로 숨길 수 없는 defect category를 함께 가진다. AR-3 option 평가에서는 normal hard rejection과 implementation defect를 구분해 후자는 search를 중단한다.

### 6.2 Evaluation SPI

```java
public interface HardConstraint {
    ConstraintDescriptor descriptor();
    ConstraintResult evaluate(EvaluationFacts facts);
}

public interface MetricContributor {
    MetricDescriptor descriptor();
    MetricValue compute(EvaluationFacts facts);
}

public interface ScoreComponent {
    ScoreDescriptor descriptor();
    ScoreValue compute(MetricSnapshot metrics);
}

public record ObjectiveVector(List<ObjectiveValue> orderedValues) {
}

public interface ObjectiveComparator {
    int compare(EvaluatedSolution left, EvaluatedSolution right);
    int compareQuality(ObjectiveVector left, ObjectiveVector right);
}
```

모든 current numeric value는 dimension별 checked `long` 또는 더 좁은 exact integer다. `double`, `float`, epsilon/tolerance, NaN/Infinity, Big-M와 numeric sentinel을 사용하지 않는다. `MetricValue`는 key, unit, value schema와 exact value를 함께 보존한다. `ScoreComponent`는 route/raw input을 받을 수 없고 hard failure 뒤 호출되지 않는다.

`compareQuality`는 첫 번째로 다른 dimension만 비교한다. `compare`는 quality가 같을 때 `StableSolutionKey`의 canonical ordered vehicle ID → ordered node ID → sorted bank request ID를 lexicographic하게 비교한다. Digest 하나만 tie key로 사용해 collision을 total order로 오인하지 않는다. Stable tie key는 품질 dimension이 아니며 result objective vector에 추가하지 않는다.

### 6.3 `SolvePlan`과 stage guard

```java
public record SolvePlan(
        List<SolveStage> stages,
        String contractVersion) {
}

public record SolveStage(
        StageKey key,
        List<ObjectiveKey> activeObjectiveOrder,
        List<ObjectiveKey> protectedObjectivePrefix,
        StageGuard guard,
        Set<OperatorCapabilityKey> requiredOperatorCapabilities,
        BudgetRef budgetRef) {
}

public interface StageGuard {
    StageGuardResult evaluate(
            ObjectiveVector candidate,
            ObjectiveVector verifiedStageBest);
}
```

`BudgetRef`는 이름과 version만 가진다. 이 phase는 `screenMaxSteps`, `phase2MaxSteps`, worker 수, `maxRounds`, watchdog 숫자를 넣지 않는다. Test에서는 `BudgetRef("fixture-stage-a", "test-only-v1")`처럼 숫자 없는 fixture-local ref를 쓴다.

`NoWorseObjectivePrefixGuard`는 protected dimension 각각이 verified stage best보다 나빠지지 않았을 때만 통과한다. Lower objective 개선이 상위 dimension 악화를 상쇄하지 못한다. Guard는 hard constraint를 해제하거나 infeasible candidate를 받지 않는다. Tolerance는 0이라는 숨은 숫자가 아니라 exact integer comparison 자체로 표현한다.

### 6.4 Registry, binder와 `BoundProfile`

```java
public record ProfileIdentity(
        CustomerKey customerKey,
        ProfileKey profileKey,
        ProfileVersion profileVersion) {
}

public record ProfileSelection(
        CustomerKey requestCustomerKey,
        ProfileIdentity requestedProfile,
        Optional<PresetKey> requestedPreset) {
}

public interface ProfileProvider {
    Collection<ProfileDescriptor> descriptors();
}

public final class ImmutableProfileRegistry {
    public static ImmutableProfileRegistry create(
            Collection<? extends ProfileProvider> providers);

    public ProfileDescriptor requireExact(ProfileIdentity identity);
}

public final class ProfileBinder {
    public BoundProfile bind(
            ProfileSelection selection,
            ProblemInstance problem,
            PreparedTravel travel,
            ImmutableProfileRegistry registry);
}
```

Registry는 app/composition root가 명시적으로 전달한 provider 목록만 사용한다. `ServiceLoader`, reflection/classpath scanning 또는 static mutable global registry를 이 phase에서 선택하지 않는다. 이는 `ADR-ARCH-003`을 임의로 닫지 않으면서 discovery order를 의미에서 제거한다. 향후 등록 mechanics가 승인되더라도 `ImmutableProfileRegistry.create`의 duplicate/order/identity semantics는 유지한다.

`ProfileVersion` constructor/factory는 blank와 case-insensitive literal `latest`를 거부한다. Preset 생략은 해당 exact descriptor가 선언한 exact default key로만 치환한다. Registry는 provider 입력 순서를 stable identity로 정렬한 뒤 동일 identity가 한 번만 존재하는지 검증한다. 동일 identity/동일 content라 해도 중복 provider는 silent merge하지 않고 startup error다.

`BoundProfile`은 최소 다음 accessor를 제공한다.

```java
public interface BoundProfile {
    ProfileIdentity identity();
    PresetKey presetKey();
    PropagationPolicy propagationPolicy();
    List<HardConstraint> hardConstraints();
    List<MetricContributor> metricContributors();
    List<ScoreComponent> scoreComponents();
    ObjectiveSchema objectiveSchema();
    ObjectiveComparator comparator();
    SolvePlan solvePlan();
    DependencyClosure dependencyClosure();
    SemanticFingerprint definitionFingerprint();
    SemanticFingerprint boundProfileFingerprint();
}
```

`definitionFingerprint`는 descriptor/component/config/version의 deterministic length-prefixed internal encoding digest다. Type은 AR-1 계획상 제안 `com.ronext.rpdptw.fingerprint.SemanticFingerprint`를 재사용한다. `boundProfileFingerprint`는 definition/preset/dependency closure에 AR-1 problem ID/unit/fact-contract fingerprint를 결합한다. `PreparedTravel`의 content fingerprint는 별도 authority로 유지하고 `SolveSnapshot`에서 함께 묶는다. 다만 binder는 travel unit/schema/coverage contract compatibility를 검증한다. 이 internal encoding은 public artifact serialization 승인이 아니다.

`BoundProfile`은 defensive immutable copy만 노출한다. Problem-indexed array/bitset은 해당 problem fingerprint에만 속하며 다른 bind에 재사용하지 않는다. Route evaluation scratch, mutable accumulator, cache, random, telemetry는 field에 두지 않고 호출별 local state로 만든다.

### 6.5 Binding error

```java
public enum ProfileBindingErrorCode {
    UNKNOWN_PROFILE,
    UNAUTHORIZED_CUSTOMER,
    LATEST_NOT_ALLOWED,
    UNKNOWN_PRESET,
    DUPLICATE_COMPONENT_KEY,
    MISSING_DEPENDENCY,
    UNIT_MISMATCH,
    VALUE_SCHEMA_MISMATCH,
    UNKNOWN_REFERENCE,
    INVALID_PARAMETER_RANGE,
    MISSING_OBJECTIVE_DIMENSION,
    INVALID_OBJECTIVE_ORDER,
    UNKNOWN_STAGE_REFERENCE,
    LEASE_OBJECTIVE_REQUIRED,
    UNSUPPORTED_FACET_VERSION,
    VERIFIER_SUPPORT_MISMATCH,
    FINGERPRINT_MISMATCH
}

public final class ProfileBindingException extends RuntimeException {
    public ProfileBindingErrorCode code();
    public ProfileIdentity requestedIdentity();
    public Optional<String> dependencyKey();
}
```

Message는 stable code와 exact offending key/unit/schema를 포함하되 raw customer data를 포함하지 않는다. Binding failure는 search termination이나 `UNASSIGNED` reason이 아니라 pre-solve error다.

### 6.6 Insertion reference seam

```java
public record PairInsertionQuery(
        RouteSequence baseRoute,
        RequestId requestId,
        int pickupInsertionIndex,
        int deliveryInsertionIndex) {
}

public interface PairInsertionEvaluator {
    InsertionEvaluation evaluate(
            PairInsertionQuery query,
            SolveEvaluationContext context);
}
```

`0 <= pickupInsertionIndex < deliveryInsertionIndex`이고 terminal/prefix policy가 허용하는 position이어야 한다. 구현은 base route를 수정하지 않고 새 immutable node list를 materialize한 뒤 `FullEvaluationEngine.evaluateRoute`를 호출한다. AR-2는 단일 query reference evaluation까지만 구현한다. 모든 position enumeration, ranking, selected move apply와 bank mutation은 AR-3 범위다.

## 7. One-pass propagation 실행 규칙

`OnePassRoutePropagator`는 route를 앞에서 뒤로 한 번만 순회하며 다음 순서를 고정한다.

1. Route/vehicle/terminal, internal depot, pair completeness/precedence, service pattern과 static `servableVehicles`를 검사한다.
2. Start terminal, vehicle work window와 depot window에서 earliest departure를 결정한다. `waitInDepot=Y`면 첫 physical customer open과 첫 arc time으로 조기 wait을 depot으로 옮기며 feasibility는 바꾸지 않는다.
3. Delivery-only logical prefix를 처리한다. Initial load를 증가시키되 travel, stop, depot/customer wait 또는 service time을 만들지 않는다.
4. 다음 physical node까지의 exact directed `D`와 vehicle-resolved `U`를 `PreparedTravel`에서 조회한다.
5. `departure + fullTravelTime <= currentWorkEnd`면 arc 전체를 운전한다. 아니면 partial drive를 버리는 방식이 아니라 애초에 시작하지 않고, plan 안의 다음 window 중 arc 전체가 들어가는 첫 `workStart`에서 동일 arc 전체를 시작한다.
6. Arrival 후 customer/depot wait, service start, window policy, service/deadline, service end/departure를 계산한다.
7. Service completion 뒤 load delta를 적용한다. 모든 prefix에서 `0 <= load <= capacity`를 검사한다.
8. 직전 physical customer service location과 현재 location이 다를 때만 stop을 증가시킨다. Terminal/depot과 logical pickup은 stop이 아니다.
9. 실제 traversed arc만 distance/drive time에, customer/depot wait·service·inter-work-window rest는 각각 별도 exact accumulator에 더한다.
10. Route 전체 included upper bound로 stop/drive time/drive distance를 검사한다. 날짜/work window/rest에서 reset하지 않는다.
11. Oneway는 마지막 service에서, single roundtrip은 실제 final customer→same depot arc를 포함해 끝낸다.

최소 `RoutePropagationFacts`:

```text
per node:
  arrival
  serviceStart
  serviceEnd/departure
  loadWeight
  loadVolume

route:
  directedDistanceMeters
  driveTimeSeconds
  customerWaitingSeconds
  depotWaitingSeconds
  serviceSeconds
  interWorkWindowRestSeconds
  routeOperationalTimeSeconds
  stopCount
```

`routeOperationalTimeSeconds`는 위 다섯 time component의 checked sum이다. 계산 실패를 plan end, `Long.MAX_VALUE`, 음수 또는 empty metric으로 바꾸지 않는다.

## 8. 평가 계층, profile과 recomputation 불변조건

### 8.1 Full evaluation 순서

```text
RouteSequence / SolutionEvaluationInput
→ structural/static gate
→ one-pass physical propagation
→ policy-neutral metric contributors
→ composed hard constraints
→ hard feasible인 경우에만 score components
→ objective vector
→ exact comparator / SolvePlan stage guard
```

각 단계는 앞 단계의 immutable output만 받는다. Metric contributor와 score component가 route/raw input을 다시 순회하지 않는다. Comparator는 propagation을 호출하지 않는다. Hard-infeasible result에는 score breakdown과 objective vector를 만들지 않는다.

### 8.2 Standard customer-neutral catalog

`StandardEvaluationCatalog`는 다음 공통 key/component를 조립 가능한 catalog로 제공한다. 고객 profile/preset 자체를 hard-code하지 않는다.

| Key | Scope/unit | 의미 |
|---|---|---|
| `driveDistance` | route/solution, meter | actual directed arcs 합 |
| `driveTime` | route/solution, second | vehicle-resolved actual arcs 합 |
| `customerWaitingTime` | route/solution, second | customer early wait |
| `depotWaitingTime` | route/solution, second | `waitInDepot`에 의해 depot으로 옮겨진 wait |
| `serviceTime` | route/solution, second | request service 합 |
| `interWorkWindowRestTime` | route/solution, second | work window 사이 rest |
| `routeOperationalTime` | route/solution, second | 위 규범 formula |
| `stopCount` | route/solution, count | physical location transition |
| `totalUnassignedCount` | solution, count | bank membership count |
| `mandatoryUnassignedCount` | solution, count | profile이 지원할 때 mandatory subset |
| `regularVehicleVolumeCost` | solution, volume-unit | 사용한 `DIRECT` vehicle별 `maxVolume` 한 번 |
| `outsourcedVehicleVolumeCost` | solution, volume-unit | 사용한 `LEASE` vehicle별 `maxVolume` 한 번 |

이 catalog는 단가, 고객 이름, Win PoC 전용 4성분 comparator 또는 미배정의 후속 외주 의미를 포함하지 않는다. `*VolumeCost`는 설계의 규범 이름을 따르지만 값은 입력 fleet capacity volume의 exact 합이며 가격이 아니다.

`TestProfileProvider`는 `build/test-fixtures`에만 두고 production app dependency에서 차단한다. 최소 두 customer의 같은-named preset과 하나의 exact default를 제공해 customer isolation을 검증한다.

### 8.3 Comparator law

모든 `a`, `b`, `c`에 대해 다음을 만족한다.

```text
compare(a, a) == 0
sign(compare(a, b)) == -sign(compare(b, a))
compare(a, b) <= 0 && compare(b, c) <= 0 => compare(a, c) <= 0
compare(a, b) is defined for every same-schema pair
first differing objective dimension decides compareQuality
equal quality => canonical StableSolutionKey decides compare
```

Objective schema/fingerprint가 다른 vector 비교는 arbitrary order가 아니라 typed `OBJECTIVE_SCHEMA_MISMATCH` defect다. Checked integer overflow가 발생하면 비교 가능한 sentinel을 만들지 않는다.

### 8.4 Full recomputation과 scratch isolation

- `FullEvaluationEngine`은 cache parameter를 받지 않는다.
- `PairInsertionEvaluator`는 base route를 바꾸지 않고 materialized route를 full evaluate한다.
- 같은 route/problem/profile 반복 호출은 exact facts/metrics/score/objective/fingerprint를 만든다.
- 다른 problem에 bind한 array를 identity가 같아 보인다는 이유로 재사용하지 않는다.
- 두 thread가 같은 immutable `BoundProfile`을 읽을 수는 있지만 accumulator/scratch는 공유하지 않는다.
- Cache는 AR-4가 도입할 파생 최적화다. AR-2 full evaluator가 후속 cache equality의 oracle이다.

## 9. 세분화된 테스트 케이스

아래 표의 테스트 source와 fixture를 먼저 작성한다. “첫 실패”를 관찰·보존하지 않은 production 구현은 시작할 수 없다.

| Test class.method | 종류/권위 | Fixture | Expected | 정상 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `RoutePropagatorApiCompilationTest.exposesOnlyImmutableFullPropagationContract` | API compile / Arch §6.1 | AR-1 tiny problem + planned signature imports | Exact signature와 immutable return type compile | 최초 1회만 `cannot find symbol: class RoutePropagator` 또는 `PropagationResult` | API shell 생성 뒤 compile; 의미 구현은 아직 fail 유지 |
| `RoutePropagatorTest.restartsWholeArcAtNextWorkWindow` | Unit / Domain §6.5, §11.1 | Work windows `[0,100]`, `[200,400]`, travel `120s`, `1200m` | departure `200`, arrival `320`, drive `120`, rest `200`; partial `100+20` 없음 | `expected: <320> but was: <220>` 또는 `NO_WORK_WINDOW...` 오분류 | 모든 exact fact/assertion 일치 |
| `RoutePropagatorTest.appliesStartOnlyAndCompleteWithinWindowExactly` | Unit / `Q-TIME-03` | arrival/serviceStart `90`, service `20`, close `90` | `START_ONLY` feasible; `COMPLETE_WITHIN_WINDOW` code `SERVICE_WINDOW_VIOLATION` | 두 policy가 같은 결과 또는 close equality 거부 | Policy별 expected 결과 |
| `RoutePropagatorTest.propagatesMixedDeliveryOnlyAndRealPairs` | Unit/property / `Q-REQ-01` | Delivery-only `4`, real pair `3`, sequence loads `4→7→3→0`, capacity `7` | Prefix `[4,7,3,0]`, final `0`, logical pickup travel/stop `0` | initial load 누락으로 음수 또는 expected prefix 불일치 | 모든 prefix와 pair/terminal facts 일치 |
| `RoutePropagatorTest.countsPhysicalLocationTransitionsOnly` | Unit / Domain §11.2 | customer locations `A,A,B,A`, logical pickup/depot 포함 | stop count `3`; depot/logical 제외 | request count `4` 또는 unique count `2` 반환 | exact `3` |
| `RoutePropagatorTest.accumulatesActualDriveResourcesAcrossRest` | Unit / Domain §11.3 | actual arcs `100+200m`, `10+20s`, 중간 rest, inclusive max equal | dist `300`, drive `30`; equal limit pass, `299/29` typed fail | rest 후 counter reset 또는 wait/service 포함 | exact aggregate와 failure code |
| `RoutePropagatorTest.separatesWaitServiceRestAndOperationalTime` | Unit / Domain §14.1 | drive `300`, customer wait `50`, depot wait `20`, service `80`, rest `100` | breakdown과 operational total `550` | elapsed/idle 포함 또는 component 중복 | exact breakdown/checked sum |
| `RoutePropagatorPropertiesTest.neverReturnsFeasibleWithInvalidPrefixOrPartialPair` | Property / Master §6 | Generated small integer route/pair permutations | Feasible이면 pair/precedence/load/terminal invariant 모두 참 | 최소 counterexample route 출력 | seed/counterexample 보존하며 property pass |
| `FullEvaluationEngineTest.neverScoresHardInfeasibleRoute` | Unit / Master §9.1 | Capacity 초과 route + recording score component | `Infeasible`, score invocation `0`, objective absent | `expected score calls <0> but was <1>` | Hard fail 뒤 score/objective 없음 |
| `FullEvaluationEngineTest.keepsMetricsNeutralAndUsesBoundScoreOnly` | Unit / Master §9.2 | 같은 facts + 서로 다른 test score config | Metric snapshot 동일, score/objective만 다름 | metric에 config 가격이 반영되어 snapshot 차이 | Metrics exact equal, score expected differ |
| `ProfileRegistryTest.isIndependentOfProviderIterationOrder` | Unit/reproducibility / Arch §9.2 | 같은 providers를 모든 순열로 전달 | Registry identities/fingerprint 동일 | 순열별 first-wins 또는 fingerprint 차이 | 모든 순열 exact equal |
| `ProfileRegistryTest.rejectsDuplicateIdentityAndComponentKeys` | Unit / Arch §9.3 | duplicate descriptor/metric/score/objective keys | `ProfileRegistryException` 또는 bind error exact code/key | exception 미발생 또는 silent overwrite | 모든 duplicate variant 거부 |
| `ProfileBinderTest.rejectsMissingDuplicateAndUnitMismatchedDependencies` | Unit / Master §9.3 | `waitSeconds` 필요, meter 제공; missing metric; schema mismatch | 각각 `MISSING_DEPENDENCY`, `UNIT_MISMATCH`, `VALUE_SCHEMA_MISMATCH` | `assertThrows` 실패 또는 fallback bind | exact code/key/expected/actual metadata |
| `ProfileBinderTest.rejectsLeaseFleetWithoutSupportedOutsourcedObjective` | Unit / `Q-OBJ-03` | LEASE vehicle 포함 + outsourced dimension 없는 preset | `LEASE_OBJECTIVE_REQUIRED` | bind 성공 또는 LEASE 무시 | pre-solve exact rejection |
| `CustomerProfileIsolationTest.deniesCrossCustomerPresetAndLatest` | Unit/integration / `Q-OBJ-01` | `cust-a/basic/1`, `cust-b/basic/1`, same preset name, literal `latest` | cross customer=`UNAUTHORIZED_CUSTOMER`; latest=`LATEST_NOT_ALLOWED`; unknown no fallback | 다른 customer/default가 resolve되거나 exception 없음 | exact composite lookup만 성공 |
| `CustomerProfileIsolationTest.usesOnlyDescriptorDeclaredExactDefault` | Unit / `Q-OBJ-01` | preset omitted, descriptor default `balanced-v1` | `balanced-v1` exact; registry-wide default 없음 | first preset 또는 `latest` 선택 | preset/fingerprint exact |
| `LexicographicComparatorPropertiesTest.obeysAntisymmetryTransitivityAndTotality` | Property / RM-2 exit | Generated exact long vectors, same schema | Comparator law 전부 참 | counterexample `a,b,c`와 seed 출력 | 모든 generated cases pass |
| `LexicographicComparatorPropertiesTest.usesFirstDimensionThenCanonicalStableTie` | Unit / Master §9.1 | `[0,2,100]` vs `[1,0,0]`; equal vector/different route key | 첫 vector 우수; equal quality는 stable key만 결정 | 뒤 dimension이 앞 차이를 뒤집거나 compare `0` | quality와 tie API 모두 expected |
| `SolvePlanStageGuardTest.rejectsLowerObjectiveGainWhenProtectedPrefixWorsens` | Unit / Master §11.4 | best `[0,2,100]`, candidate `[0,3,0]`, protected first two | `PROTECTED_OBJECTIVE_WORSENED` at second key | candidate accepted due distance improvement | exact reject key/value |
| `SolvePlanStageGuardTest.neverReceivesHardInfeasibleCandidate` | Architecture/unit / Master §9.1 | Hard fail + recording guard | Guard invocation `0` | guard invocation `1` | Full engine call ordering pass |
| `EvaluationIsolationTest.doesNotShareScratchAcrossCallsProblemsOrThreads` | Fault/property / Master §9.3 | Alternating two problems/routes on one descriptor; concurrent reads | Each result equals standalone oracle, fingerprints stay problem-specific | 이전 route accumulator/array가 다음 result에 잔존 | repeated/interleaved results exact |
| `PairInsertionEvaluationContractTest.matchesMaterializedFullRecomputationWithoutMutation` | Property / AR-2 handoff | Base route + request + all legal position pairs | option result equals direct full route evaluation; base fingerprint unchanged | base list changed 또는 objective mismatch | 모든 pair exact and side-effect-free |
| `StandardEvaluationCatalogTest.exposesUnitCorrectNeutralComponents` | Unit / Domain §12 | Hand facts and used DIRECT/LEASE vehicle set | Units/formulas exact; each used vehicle volume once; no price field | duplicate vehicle volume 또는 wrong unit | descriptor/value/fingerprint exact |
| `EvaluationLayerArchitectureTest.detectsSyntheticPriceLeakAndKeepsProductionClean` | Architecture / Arch §7~§9 | Synthetic forbidden propagation→score/customer class + production packages | Synthetic violation detected; production violation `0` | rule가 synthetic leak를 못 찾거나 production leak 열거 | 양쪽 assertion pass |
| `ProfileModuleDependencyArchitectureTest.enforcesOneWayProfileDependency` | Architecture / Arch §7 | Synthetic profile→solver/provider/internal refs + actual bytecode | Synthetic 전부 탐지; actual forbidden ref `0`; core→profile `0` | 금지 참조 누락 또는 actual violation | rule/bytecode scan pass |

Property test framework는 AR-0 dependency management가 이미 승인한 것을 사용한다. 승인된 framework가 없으면 JUnit 5의 deterministic generated loop와 seed/counterexample logging으로 구현하며 AR-2가 새 라이브러리 version을 root POM에 임의 추가하지 않는다.

## 10. 테스트 우선 구현 순서

### 10.1 공통 red gate

각 slice마다 다음 순서를 강제한다.

1. 표의 test/fixture 파일만 먼저 만든다.
2. Targeted Maven command를 실행한다.
3. Owner module의 Surefire report가 test를 실제 발견했는지 확인한다.
4. 표의 “정상 첫 실패 관찰”과 같은 compile error/assertion/counterexample인지 확인한다.
5. `target/codex-evidence/AR-2/<evidence-id>/red/`에 command, exit code, report와 핵심 failure를 보존한다.
6. 환경/다운로드/선행 unrelated failure이면 red로 인정하지 않고 환경을 고친 뒤 다시 실행한다.
7. 의도한 red를 보지 못했으면 production 파일을 만들거나 수정하지 않는다.

Compile error는 `RoutePropagatorApiCompilationTest`가 API shell을 처음 요구하는 한 번만 정상 red다. 그 뒤 semantic test는 compile 가능한 test double/fixture를 사용해 구체 assertion failure를 보여야 한다.

### 10.2 Vertical slice 순서

| Slice | Test-first | 의도한 failure 확인 뒤 최소 production | Target green |
|---|---|---|---|
| A. API shell | `RoutePropagatorApiCompilationTest` | `RouteSequence`, propagation/evaluation result의 immutable shell만 생성 | API compile test만 green |
| B. Structural + propagation | `RoutePropagatorTest`, properties | `RouteStructureGate`, `WorkWindowTravelResolver`, `OnePassRoutePropagator` 최소 구현 | 모든 hand/property propagation green |
| C. Evaluation layering | `FullEvaluationEngineTest` | SPI value types와 `FullEvaluationEngine`; hard fail에서 즉시 종료 | layer tests green |
| D. Comparator/plan | Comparator property와 stage guard tests | Exact comparator, stable structural key, no-worse prefix guard | law/guard green |
| E. Registry/binding | Registry/binder/customer isolation tests | Explicit immutable registry, exact binder, typed errors/fingerprints | binding/isolation green |
| F. Standard catalog | `StandardEvaluationCatalogTest` | Customer-neutral standard component catalog | catalog green |
| G. Insertion seam | `PairInsertionEvaluationContractTest` | Immutable materialization + full recomputation evaluator | all legal pair positions green |
| H. Architecture/isolation | Architecture + isolation tests | Package visibility/dependency 수정과 per-call scratch 확정 | architecture/isolation green |

각 slice의 targeted green 후 다음 slice로 간다. 모든 slice 뒤 `rpdptw-core` module test, core+profile+fixture+architecture `verify`, 마지막 reactor `mvn verify` 순서를 지킨다.

## 11. Exact 명령

모든 명령의 working directory:

```bash
cd /Users/brown/workspace/ro-next
```

### 11.1 Entry와 baseline

```bash
git status --short
mvn -version
test -f docs/codex/phases/phase-00-baseline-and-build-architecture.md
test -f docs/codex/phases/phase-01-input-domain-and-travel.md
rg -n '^status: DONE$|^phase:|^rm_mapping:|evidence' \
  docs/codex/phases/phase-00-baseline-and-build-architecture.md \
  docs/codex/phases/phase-01-input-domain-and-travel.md
find . -name pom.xml -not -path './.git/*' -not -path './node_modules/*' -print | sort
rg -n '<module>|<artifactId>|<dependency>' \
  pom.xml rpdptw/pom.xml rpdptw/core/pom.xml \
  rpdptw/profiles/pom.xml rpdptw/profiles/standard/pom.xml \
  build/test-fixtures/pom.xml build/architecture-rules/pom.xml
```

선행 `DONE`와 evidence digest를 찾지 못하면 여기서 `BLOCKED`로 끝낸다.

### 11.2 Red와 targeted green

```bash
mvn -pl rpdptw/core -am \
  -Dtest=RoutePropagatorApiCompilationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl rpdptw/core -am \
  -Dtest=RoutePropagatorTest,RoutePropagatorPropertiesTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl rpdptw/core -am \
  -Dtest=FullEvaluationEngineTest,LexicographicComparatorPropertiesTest,SolvePlanStageGuardTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl rpdptw/core -am \
  -Dtest=ProfileRegistryTest,ProfileBinderTest,CustomerProfileIsolationTest,EvaluationIsolationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl rpdptw/core -am \
  -Dtest=PairInsertionEvaluationContractTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl rpdptw/profiles/standard -am \
  -Dtest=StandardEvaluationCatalogTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl build/architecture-rules -am \
  -Dtest=EvaluationLayerArchitectureTest,ProfileModuleDependencyArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

각 command 뒤 owner report를 exact 확인한다.

```bash
find rpdptw/core/target/surefire-reports \
  rpdptw/profiles/standard/target/surefire-reports \
  build/architecture-rules/target/surefire-reports \
  -maxdepth 1 -type f -print | sort
rg -n 'tests="[1-9]|failures="[1-9]|errors="[1-9]' \
  rpdptw/core/target/surefire-reports/TEST-*.xml \
  rpdptw/profiles/standard/target/surefire-reports/TEST-*.xml \
  build/architecture-rules/target/surefire-reports/TEST-*.xml
```

### 11.3 Module verify와 reactor regression

```bash
mvn -pl rpdptw/core -am test
mvn -pl rpdptw/core,rpdptw/profiles/standard,build/test-fixtures,build/architecture-rules -am verify
mvn verify
```

Failsafe `*IT`를 추가했다면 `test`가 아니라 다음 `verify`에서 실제 실행을 확인한다.

```bash
find . -path '*/target/failsafe-reports/TEST-*.xml' -print | sort
```

### 11.4 Dependency, 금지 값과 source scan

```bash
mvn -pl rpdptw/core,rpdptw/profiles/standard -am dependency:tree

rg -n 'com\.google\.|com\.amazonaws\.|software\.amazon\.|io\.grpc\.|jakarta\.ws\.rs|org\.springframework' \
  rpdptw/core/src rpdptw/profiles/standard/src

rg -n '\b(double|float)\b|EPSILON|TOLERANCE|Big-M|Long\.MAX_VALUE|Integer\.MAX_VALUE' \
  rpdptw/core/src/main/java/com/ronext/rpdptw/propagation \
  rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation \
  rpdptw/profiles/standard/src/main/java

rg -n '"latest"|LATEST|ServiceLoader|ClassGraph|Reflections|Map<String,\s*Object>' \
  rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation \
  rpdptw/profiles/standard/src/main/java

rg -n 'if\s*\([^)]*(customer|profile|preset)|switch\s*\([^)]*(customer|profile|preset)' \
  rpdptw/core/src/main/java rpdptw/solver/src/main/java

rg -n 'price|tariff|customerName|costPer' \
  rpdptw/core/src/main/java/com/ronext/rpdptw/propagation

rg -n 'parallelRuns|iterationsPerRun|screenMaxSteps\s*=\s*[0-9]|phase2MaxSteps\s*=\s*[0-9]|maxRounds\s*=\s*[0-9]|5_000|5000' \
  rpdptw/core/src/main rpdptw/profiles/standard/src/main
```

첫 다섯 scan은 기대 0건이다. 마지막 official-value scan도 main source에서 0건이어야 한다. `ProfileIdentity.customerKey` 같은 typed identity는 허용되지만 그것을 조건 분기하는 source는 허용하지 않는다.

### 11.5 Diff, link와 scope

```bash
git diff --check
git status --short
git diff --name-status -- \
  rpdptw/core \
  rpdptw/profiles/standard \
  build/test-fixtures \
  build/architecture-rules \
  docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md

rg -n '^#{1,4} ' \
  docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md

test -f docs/codex/implementation-plan.md
test -f docs/master-design.md
test -f docs/architecture-design.md
test -f docs/domain-design.md
test -f docs/master-design-open-questions.md
```

Dirty worktree의 다른 변경은 baseline manifest와 비교해 제외한다. `git reset`, `git checkout --`, broad `git restore`, 사용자 변경 stash/drop을 사용하지 않는다.

동시 Maven 실행 중 shade JAR replace, report truncation/overwrite 또는 `target/` rename failure가 발생하면 위 regression 명령의 authoritative failure로 분류하지 않는다. 다른 실행이 끝난 격리 환경에서 동일 명령을 다시 실행하고 그 결과만 red/green/module/reactor 판정에 사용한다.

## 12. Step-by-step implementation checklist

- [ ] `git status --short`, toolchain, source design hashes와 phase-00/01 evidence digest를 기록한다.
- [ ] AR-0/AR-1이 `DONE`인지, target modules와 required artifacts가 실제 build되는지 확인한다.
- [ ] AR-1 `ProblemInstance`/`PreparedTravel`/service-pattern API와 §6 계획상 제안 API의 compatibility 표를 작성한다.
- [ ] 테스트/fixture 경로를 production보다 먼저 만든다.
- [ ] `RoutePropagatorApiCompilationTest`의 정확한 `cannot find symbol` red를 한 번 보존한다.
- [ ] 최소 immutable API shell만 만들어 compile green으로 전환한다.
- [ ] Full-arc restart/window/load/stop/resource hand tests를 작성하고 구체 assertion red를 확인한다.
- [ ] `RouteStructureGate`, `WorkWindowTravelResolver`, one-pass propagator의 최소 구현으로 propagation target을 green으로 만든다.
- [ ] Hard/metric/score/objective layer test red를 확인한 뒤 full evaluator를 최소 구현한다.
- [ ] Comparator property와 stage guard counterexample red를 확인한 뒤 exact comparator/guard를 구현한다.
- [ ] Registry/binder/customer isolation test red를 확인한 뒤 explicit immutable registry와 typed binder error를 구현한다.
- [ ] Provider 순열, duplicate, unknown/latest/cross-customer, unit/schema/LEASE mismatch를 모두 green으로 만든다.
- [ ] Standard neutral catalog test red 뒤 customer-neutral component만 구현한다.
- [ ] Insertion reference test red 뒤 immutable route materialization/full recomputation seam을 구현한다.
- [ ] Isolation/architecture synthetic fixture가 실제 위반을 탐지함을 먼저 확인하고 production violation 0을 만든다.
- [ ] Targeted green report에 test count가 0이 아님을 확인한다.
- [ ] `rpdptw-core` 전체 test를 실행한다.
- [ ] Core/profile/test-fixture/architecture module `verify`를 실행한다.
- [ ] Reactor `mvn verify`를 실행한다.
- [ ] 동시 `target/` 충돌 의심 실패는 blocker로 승격하지 않고 격리 재실행 결과로 판정한다.
- [ ] Dependency tree와 모든 `rg` 금지 scan 결과를 evidence에 저장한다.
- [ ] Problem/travel/profile definition/bound profile/evaluation result fingerprint를 evidence에 저장한다.
- [ ] `git diff --check`, scoped changed-file list, source hash drift와 rollback 명령을 기록한다.
- [ ] §15의 모든 AND gate를 확인한 뒤에만 status를 `DONE`으로 바꾼다.

## 13. Deliverables와 evidence bundle

### 13.1 Deliverables

| Deliverable | 내용 | 소비 방법 |
|---|---|---|
| Full propagation contract | `RouteSequence` → exact `PropagationResult` | AR-3 insertion과 AR-5 verifier가 같은 stateless library 호출 |
| Full evaluation contract | Route/solution → feasibility, neutral metrics, score, objective | AR-3/4는 comparison에 사용, AR-5는 cache-free recompute |
| Exact comparator | Objective schema와 stable total order | AR-3 portfolio tie, AR-4 current/best/champion |
| `SolvePlan` | Stages, protected prefix, operator capability refs, budget refs | AR-4가 explicit run config와 결합; hidden 숫자 없음 |
| `ImmutableProfileRegistry` | Exact composite identities와 duplicate/order validation | 향후 app composition root가 explicit providers 전달 |
| `BoundProfile` | Problem-bound dependency closure와 fingerprints | `SolveSnapshot`의 profile authority |
| Standard catalog | Customer-neutral reusable components | Customer/test profile composition; core 역의존 금지 |
| Insertion reference seam | Side-effect-free full recomputation option | AR-3 enumeration/ranking의 correctness oracle |
| Test fixtures | Small integer hand oracle와 isolated test profiles | AR-3와 AR-5가 동일 사실/프로파일을 재사용 |

### 13.2 Evidence bundle

위치는 다음으로 고정한다.

```text
target/codex-evidence/AR-2/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── api-compilation/
│   ├── propagation/
│   ├── evaluation/
│   ├── profile-binding/
│   ├── comparator-plan/
│   ├── insertion/
│   └── architecture/
├── green/
├── regression/
│   ├── core/
│   ├── profile-standard/
│   ├── architecture/
│   └── reactor/
├── fingerprints/
│   ├── problem.sha256
│   ├── prepared-travel.sha256
│   ├── profile-definition.sha256
│   ├── bound-profile.sha256
│   ├── dependency-closure.txt
│   └── evaluation-result.sha256
├── faults/
│   ├── binding-rejection-matrix.txt
│   ├── customer-isolation.txt
│   └── scratch-isolation.txt
├── reproducibility/
│   ├── provider-order-permutations.txt
│   └── comparator-properties.txt
├── diff/
│   ├── changed-files.txt
│   ├── diff-check.txt
│   ├── dependency-tree.txt
│   └── forbidden-scan.txt
└── handoff.md
```

`evidence.json`은 전체 계획 §11.1 공통 field에 더해 `ar0EvidenceDigest`, `ar1EvidenceDigest`, `problemFingerprint`, `preparedTravelFingerprint`, `profileDefinitionFingerprint`, `boundProfileFingerprint`, `comparatorContractVersion`, `solvePlanContractVersion`, `redFailureCount`, `targetedTestCount`, `moduleTestCount`, `reactorTestCount`를 가진다.

Red report는 첫 실패가 의도한 이유였음을 사람이 읽을 수 있는 한 줄로 요약한다. Green report는 같은 test와 command의 exit `0`, Surefire test count와 report path를 연결한다.

## 14. Rollback

이 phase에는 schema cutover, runtime state migration, publication pointer 또는 provider artifact가 없다. Rollback 단위는 AR-2가 추가한 test/production file과 네 POM의 AR-2 dependency hunk뿐이다.

1. 시작 전에 AR-0/AR-1 file hash와 `git status --short`를 evidence에 보존한다.
2. Rollback은 AR-2 changed-file manifest의 exact path에만 역 patch를 적용한다. Broad `git reset`, `git checkout --`, worktree 전체 restore를 하지 않는다.
3. AR-0/AR-1이 만든 module skeleton, parent convention, domain/travel source와 다른 세션 파일은 삭제·수정하지 않는다.
4. Profile identity/version은 content가 한 번 소비된 뒤 in-place 변경하지 않는다. Semantics를 바꿔야 하면 새 exact version을 추가하고 old version을 보존한다.
5. 실패한 AR-2 evidence fingerprint는 재사용하지 않고 bundle status를 `ROLLED_BACK`으로 표시한다. `target/` evidence 삭제가 필요하면 changed-file manifest 확인 뒤 별도 recoverable cleanup으로 한다.
6. Test-first commit/log가 분리되어 있으면 마지막 fully green slice까지만 유지하고 이후 slice만 되돌린다.
7. Rollback 뒤 AR-0/AR-1 targeted verify와 reactor verify를 다시 실행해 predecessor parity를 확인한다.

## 15. DONE/BLOCKED 판정

### 15.1 `DONE` AND gate

다음이 모두 참일 때만 이 phase를 `DONE`으로 바꾼다.

1. AR-0과 AR-1이 실제 `DONE`이고 evidence digest가 bundle에 연결된다.
2. 표의 production/test path와 API가 실제 repository에 존재하고 build된다.
3. 각 semantic behavior에 의도한 red와 같은 command의 targeted green evidence가 있다.
4. Full-arc restart, mixed load, window/wait/rest/stop/drive resource hand oracle가 exact integer로 통과한다.
5. Hard-infeasible route는 score/objective/stage guard에 진입하지 않는다.
6. Neutral metric snapshot에 가격/고객 선호가 없고 같은 facts에서 profile score만 달라진다.
7. Missing/duplicate/unit/schema/reference/LEASE mismatch, unknown/latest/cross-customer가 모두 pre-solve typed error다.
8. Registry provider 순서가 identity/fingerprint에 영향을 주지 않고 duplicate first-wins가 없다.
9. Comparator law와 stable structural tie property가 통과한다.
10. Stage guard가 protected objective 악화를 lower-objective 개선으로 상쇄하지 않는다.
11. `BoundProfile`과 problem-indexed data가 immutable이고 shared mutable scratch가 없다.
12. Insertion reference가 base route를 바꾸지 않고 direct full recomputation과 정확히 같다.
13. Core/profile dependency와 customer/provider/package architecture violation이 0이다.
14. Targeted tests, module `verify`, reactor `mvn verify`, `git diff --check`가 모두 통과한다.
15. Evidence bundle과 fingerprint/handoff/rollback이 완결되고 blocker가 0이다.

### 15.2 `BLOCKED` 조건

다음 중 하나면 안전한 마지막 green slice에서 중단하고 `BLOCKED` evidence를 남긴다.

- AR-0/AR-1 `DONE` 문서, evidence 또는 required module/artifact가 없다.
- AR-1 service pattern/terminal/travel semantics가 상위 설계와 충돌한다.
- 새 physical fact가 필요하지만 기존 typed seam으로 표현할 수 없고 facet/verifier ADR 승인이 없다.
- External public API, wire schema 또는 profile discovery mechanics를 승인해야만 진행할 수 있다.
- Exact unit/value schema 또는 customer preset authority가 모호하다.
- Comparator total order를 floating tolerance/Big-M/digest-only collision 가정 없이는 만들 수 없다고 판단된다.
- Profile component가 solver/search/provider SDK 또는 다른 customer fallback을 요구한다.
- Full recomputation이 `PreparedTravel` 대신 raw coordinates/speed fallback을 요구한다.
- Unrelated user/다른 세션 변경과 target path가 겹쳐 보존할 수 없다.

`Q-BENCH-02` 공식 숫자, `Q-INFRA-01`, `Q-VAR-01`과 compliant Win fixture 부재는 이 phase 자체를 `BLOCKED`로 만들지 않는다. 해당 값을 구현하려는 시도가 중단 조건이다.

동시 phase 세션의 shared `target/` 때문에 발생한 shade JAR replace, report overwrite/truncation 또는 rename failure도 blocker가 아니다. 동시 실행이 끝난 격리 재실행에서 같은 실패가 재현될 때만 실제 repository/contract failure 후보로 분류한다.

## 16. 다음 phase handoff

### 16.1 AR-3에 넘길 것

- `ProblemInstance`, `PreparedTravel`, `BoundProfile` exact fingerprints
- `SolveEvaluationContext`
- Stateless `FullEvaluationEngine`
- Side-effect-free `PairInsertionEvaluator`
- Exact `ObjectiveComparator`와 `StableSolutionKey` 규칙
- `SolvePlan`/stage guard와 test-only budget refs
- `HandCalculatedEvaluationFixtures`, `TestProfileProvider`
- Base route 무변경 + materialized full recomputation equality report

AR-3은 모든 pickup/delivery position enumeration과 feasible option ranking을 위 insertion seam 위에 구현한다. Partial pair나 hard-infeasible option을 ranking에 넣지 않고, 선택 move apply 뒤 full recomputation equality를 다시 검증해야 한다.

### 16.2 AR-5에 넘길 것

- Cache를 받지 않는 propagation/evaluation entrypoint
- Profile declaration/dependency closure와 exact comparator version
- Problem/travel/profile identity mismatch typed errors
- Hand-calculated route facts/metrics/objective oracle
- Architecture evidence: verification은 core를 사용할 수 있으나 solver/search/cache를 참조하지 않음

AR-5 verifier는 이 library를 공유할 수 있지만 candidate route를 독립 traversal하고 search cache/solver summary를 authority로 받지 않는다.

### 16.3 Known risks

- Delivery-only logical pickup의 exact AR-1 표현은 아직 handoff가 없어 가장 먼저 compatibility 확인이 필요하다.
- Profile discovery mechanics는 ADR 전이므로 explicit provider collection만 사용한다.
- Stable structural tie encoding은 external result encoding이 아니며 AR-5 canonical payload와 혼동하면 안 된다.
- Profile-bound problem arrays는 성능상 유용하지만 cross-problem reuse와 shared scratch 위험이 크므로 isolation tests가 mandatory다.
- Standard catalog의 ownership volume 이름에 `Cost`가 있어도 price가 아니므로 unit/value tests와 문서가 필요하다.

## 17. Scope exclusions와 금지 shortcut

이 phase는 다음을 구현하지 않는다.

- AR-3의 모든 position enumeration, option ranking, initial 4×2 portfolio와 candidate mutation
- AR-4의 COW state, cache/incremental evaluation, ALNS, acceptance/adaptive update, numeric step budget
- AR-5의 candidate/result verifier, finalization, outcome, audit와 publication
- AR-6~AR-8의 application port, registry delivery, local/provider adapter, coordinator와 cutover
- AR-9의 exact Win PoC 4성분 comparator, official manifest/baseline
- AR-10의 profiling 또는 apply/undo
- Physical infrastructure, provider/product SDK와 deployment
- Multi-trip/rotation, optional variant, route pool/MIP
- Canonical public JSON/API/schema, persistence schema와 artifact wire encoding
- Decimal Win fixture를 integer fixture로 변환하거나 official evidence로 사용하는 일

금지 shortcut:

- Hard violation을 finite penalty/temperature/acceptance로 통과
- Metric에 price/선호/customer label 포함
- Score/comparator가 route/raw input/coordinates를 다시 해석
- Comparator의 floating tolerance, Big-M, unordered collection, clock tie-break
- `latest`, unknown, nearest-name, cross-customer 또는 registry-wide default fallback
- `Map<String,Object>`, raw JSON, reflection expression으로 profile dependency 처리
- First discovered provider 승리, mutable static registry, shared evaluation scratch
- Runtime travel fallback, reverse arc, speed/coordinate 재계산
- Cache/incremental result를 full recomputation authority로 사용
- Customer name/preset label branch를 propagator/evaluator/solver에 추가
- README/GCP의 `8`, `5000`, timeout을 official `Q-BENCH-02` 값으로 복사
- Legacy `double objective`와 GCP finalizer를 comparator baseline으로 사용
- Test red를 관찰하지 않고 production 구현 시작
- AR-0/AR-1 blocker를 AR-2에서 POM/domain 재작성으로 우회

## 18. 문서 작성 세션 검증 기준

이 문서 작성 세션은 code/POM/source/test/README/GCP/data를 수정하지 않으며 Maven test/verify를 다시 실행하지 않는다. Phase-00 격리 baseline의 legacy test/verify `PASS`를 사용하고, 완료 시 문서 링크/hash drift/diff/scope만 다음처럼 확인한다.

### 18.1 전체 구현 계획 §10 대응

| §10 계약 | 이 문서 |
|---|---|
| 1. 실제 저장소 조사 | §3, §11.1 |
| 2. Authority와 결정 상태 | §2 |
| 3. 현 상태→목표 gap | §4 |
| 4. 정확한 예상 경로 | §5 |
| 5. 예상 type/API/error | §6~§8 |
| 6. Test case 표 | §9 |
| 7. 테스트 우선 순서 | §10 |
| 8. Exact 명령 | §11 |
| 9. Deliverable/evidence | §13 |
| 10. Rollback | §14 |
| 11. 완료·중단 조건 | §15 |
| 12. Handoff | §16 |
| 13. Scope exclusions | §17 |

### 18.2 검증 명령

```bash
cd /Users/brown/workspace/ro-next

test -f docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
rg -n '^# AR-2 / RM-2|^## (1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18)\.' \
  docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md

shasum -a 256 \
  docs/master-design.md \
  docs/architecture-design.md \
  docs/domain-design.md \
  docs/codex/implementation-plan.md \
  docs/master-design-open-questions.md

git diff --no-index --check /dev/null \
  docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md

git status --short -- \
  docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
```

Expected source hashes는 YAML metadata와 같아야 한다. Target status는 `?? docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md` 하나여야 한다. 전체 working tree의 다른 변경은 §3.1 baseline과 동시 세션 소유이며 건드리지 않는다.

### 18.3 작성 완료 관찰

| 검증 | 결과 |
|---|---|
| Target path | `PASS`; 지정 파일 존재 |
| Heading | `PASS`; H1 1개와 numbered H2 `1`~`18` 전부 확인 |
| Code fence | `PASS`; fence marker 짝수 |
| Local Markdown link path | `PASS`; 11개 unique local target 존재, unexpected missing 0 |
| Original source hash drift | `PASS`; implementation plan/Master/Architecture/Domain/question register가 YAML hash와 모두 일치 |
| Predecessor snapshot | Phase-00 `NOT_STARTED`, phase-01 `BLOCKED`; metadata의 관찰 hash와 일치 |
| Whitespace diff check | `PASS`; `git diff --no-index --check` diagnostic 0. Exit `1`은 `/dev/null`과 새 파일이 다르다는 no-index 정상 상태 |
| Scoped status | `PASS`; exact target만 `?? docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md` |
| Maven | 이 문서 세션에서는 미실행; 관리 세션의 격리 legacy `test`/`verify PASS` 사용 |
