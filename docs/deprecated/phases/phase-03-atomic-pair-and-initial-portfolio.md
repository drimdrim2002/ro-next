---
phase: AR-3
rm_mapping: RM-3
title: Atomic pair insertion evaluator와 initial portfolio
status: BLOCKED
document_status: REVIEW
document_authoring: COMPLETE
implementation_started: false
implementation_entry: BLOCKED_UNTIL_AR_2_DONE
document_language: ko
document_role: 후속 구현 세션을 위한 테스트 우선 실행 명세
source_baseline_captured_on: 2026-07-24
source_baseline:
  implementation_plan:
    path: docs/codex/implementation-plan.md
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  master_design:
    path: docs/master-design.md
    version: 3.2-review
    status: REVIEW
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    path: docs/architecture-design.md
    version: 1.1-review
    status: REVIEW
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    path: docs/domain-design.md
    version: 2.2-review
    status: REVIEW
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
  question_register:
    path: docs/master-design-open-questions.md
    version: 2.1-review
    status: REVIEW
    sha256: 3d6bc496b8df98a10534338828dd7e845b50ea967afa884642403405e613c088
prerequisite_documents:
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    phase: AR-0
    required_status: DONE
    authoring_time_observation: NOT_PRESENT
    completion_time_observation: PRESENT_WITH_STATUS_NOT_STARTED
    observed_sha256: 12722696b430f693f2c4db9df760ed6acb6a463eb49cfa398927936426d01e52
  - path: docs/codex/phases/phase-01-input-domain-and-travel.md
    phase: AR-1
    required_status: DONE
    completion_time_observation: PRESENT_WITH_STATUS_BLOCKED
    observed_sha256: a2f57d1bbfd519a97feec4f7811e1600e19efe201200cbfd14929c19a3c453b3
  - path: docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
    phase: AR-2
    required_status: DONE
    authoring_time_observation: NOT_PRESENT
    completion_time_observation: PRESENT_WITH_STATUS_BLOCKED
    observed_sha256: 8ec2cb5b8178d4afe4d82fc7a7a35e274729aeca1fd6687f5f974b942df26ae1
shared_build_baseline:
  source: phase-00 isolated execution observation supplied by the management session
  java: 25.0.3-amzn
  maven: 3.9.14
  legacy_test: PASS
  legacy_verify: PASS
  concurrent_target_failures_are_authoritative: false
output_file: docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
---

# AR-3 / RM-3 — Atomic pair insertion evaluator와 initial portfolio

이 문서는 [전체 구현 계획](../implementation-plan.md) §9.4와 §10을 AR-3 구현 작업으로 구체화한다. 구현자는 이 문서를 승인된 외부 API나 wire schema로 읽지 않는다. 이 문서의 Java 이름과 signature는 상위 `REVIEW` 설계를 모호하지 않게 실행하기 위한 **계획상 고정 제안 API**다.

AR-3의 한 문장 목표는 다음과 같다.

> `ProblemInstance`·complete `PreparedTravel`·immutable `BoundProfile`만 소비하는 side-effect-free atomic pair evaluator를 만들고, 그 evaluator 하나로 4개 request-route 성장 정책과 2개 `DIRECT`-first vehicle 순서의 최대 8개 독립 `CommittedCandidate`를 생성하며, 모든 성공 move가 cache-free full recomputation과 같고 모든 실패가 호출 전 상태와 관찰상 동등함을 증명한다.

이 phase는 ALNS screen, phase-1 champion, phase-2 worker, 독립 candidate verifier 또는 publication을 구현하지 않는다.

## 1. Authority, 결정 상태와 충돌 처리

### 1.1 규범 근거

다음 절을 함께 적용한다.

| 문서 | 관련 절 | AR-3에 주는 권위 |
|---|---|---|
| [Master Design](../../master-design.md) | §6, §11.1~§11.3, §12.1~§12.2, §15.5 | Stable pair partition, atomic mutation, 공통 evaluator, 4×2 portfolio, `CLOCK UNAVAILABLE`, COW/discard와 RM-3 gate |
| [Domain Design](../../domain-design.md) | §3.1, §10, §12.4, §15.2, §16 | Package owner, `SearchRequestBank`, stable candidate, cache-free equality, search defect와 acceptance evidence |
| [Architecture Design](../../architecture-design.md) | §5~§8, §18~§19 | `rpdptw-core`/`rpdptw-solver` 배치, dependency DAG, test evidence와 AR-3 gate |
| [질문 등록부](../../master-design-open-questions.md) | `Q-ALG-01`, `Q-ALG-02`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | 4×2 조합은 resolved, COW 유지, 공식 수치는 open, infrastructure/variant는 deferred |
| [전체 구현 계획](../implementation-plan.md) | §5, §8, §9.4, §10~§11 | 관통 불변조건, red→green 순서, phase 문서 13항목, evidence bundle과 DONE AND gate |

Authority 순서는 채택된 외부 계약/승인 ADR → `APPROVED` Master → `APPROVED` 상세 설계 → 현재 `REVIEW` 문서 → 역사·연구 자료다. 현재 확인된 상위 문서는 모두 `REVIEW`다. 따라서 이 문서의 이름과 signature는 내부 구현 계획을 고정할 뿐 승인된 public API, JSON, 저장 schema 또는 provider 계약이 아니다.

다음 충돌 처리를 강제한다.

1. AR-0~2 실제 산출물의 이름만 이 문서의 제안 이름과 다르고 의미가 같다면, 구현 전에 이 문서의 경로/signature 표를 실제 이름으로 갱신하고 source hash를 다시 고정한다.
2. AR-0~2 산출물의 의미가 이 문서의 pair/bank/travel/profile 계약과 다르면 adapter나 overload로 숨기지 말고 `BLOCKED`로 판정한다.
3. `ProblemInstance`, `PreparedTravel`, `BoundProfile`의 identity/fingerprint가 서로 맞지 않으면 build를 시작하지 않는다.
4. 상위 계약에 없는 utilization missing/zero, CLOCK angle/bearing 또는 deadline 해석을 solver가 숫자·문자열·좌표 공식으로 임의 확정하지 않는다.

### 1.2 질문 상태와 AR-3 영향

| 질문 | 상태 | 이 phase의 처리 |
|---|---|---|
| `Q-ALG-01` | `RESOLVED` | 정확히 `CLOCK`, `SEQ_FARTHEST`, `SEQ_LARGE_DEMAND`, `SEQ_EARLIEST_DEADLINE` × `DIRECT_FIRST_LARGE`, `DIRECT_FIRST_SMALL`의 8개 조합 identity를 구현한다. |
| `Q-ALG-02` | `RESOLVED — KEEP_COW` | 실패/거절/예외 시 전체 draft를 폐기한다. Apply/undo skeleton도 추가하지 않는다. |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | `screenMaxSteps`, phase-2 worker/step/round/watchdog은 AR-3 입력도 산출물도 아니다. 수치 부재는 AR-3 generic construction을 막지 않는다. |
| `Q-INFRA-01` | `DEFERRED` | Provider/product/URI/SDK/workflow/deployment type을 추가하지 않는다. |
| `Q-VAR-01` | `DEFERRED` | Optional variant, multi-trip/rotation에 대비해 pair/terminal/bank invariant를 완화하지 않는다. |

### 1.3 현재 blocker와 non-blocker

완료 검증 시점에는 AR-0~2 phase 문서가 모두 생겼지만 AR-0만 `NOT_STARTED`이고, AR-1과 AR-2는 `BLOCKED`이며 AR-0~2 DONE evidence는 없다. 전체 구현 계획 §11.2에 따라 **AR-3의 현재 구현 상태도 `BLOCKED`**다. 이는 production/test 구현이 시작됐다는 뜻이 아니며 `implementation_started: false`다. Phase-03 문서 작성은 완료됐지만 문서 자체는 `REVIEW` 상태로 유지한다. AR-1이 제안한 `SemanticFingerprint`, dense ID, delivery-only `LOGICAL_START_PICKUP`, `PreparedTravel.requireCompatibleProblem` 의미와 AR-2의 단일-position `PairInsertionEvaluator`는 이 문서의 계획상 API에 반영했다. 반면 AR-2 phase 문서에는 §6.2 construction-ordering seam이 아직 없으므로 같은 의미의 versioned bound handoff가 추가로 필요하다.

구현 세션은 다음을 모두 확인해야 entry gate를 연다.

- AR-0 `DONE`: Java 25 reactor, `rpdptw-core`, `rpdptw-solver`, test/architecture convention이 존재한다.
- AR-1 `DONE`: immutable `ProblemInstance`, complete `PreparedTravel`, dense stable ID, service-pattern/terminal 표현과 fingerprints가 존재한다.
- AR-2 `DONE`: cache-free propagation/evaluation, immutable `BoundProfile`, exact comparator와 아래 §6.2의 bound construction-ordering seam이 존재한다.
- AR-2 evidence가 utilization의 missing/zero 처리와 CLOCK order key/version을 명시한다.
- 선행 artifact의 digest와 phase evidence bundle을 재현할 수 있다.

다음은 AR-3 blocker가 아니다.

- `Q-BENCH-02` 공식 수치 부재
- 현재 Win fixture의 decimal `D/U` 비준수
- Provider/product가 정해지지 않음

AR-3 test는 provider-neutral Java fixture만 사용하며 `data/win_poc_case.json`을 변환하거나 official fixture로 사용하지 않는다.

## 2. 작성 시점 실제 저장소 조사

### 2.1 Working tree와 module graph

조사 working directory는 `/Users/brown/workspace/ro-next`다.

작성 시점 `git status --short`는 clean이 아니었다. 관련 관찰은 다음과 같다.

- `docs/master-design.md`, `docs/architecture-design.md`, `docs/domain-design.md`, `docs/master-design-open-questions.md`는 수정 상태다.
- `docs/codex/`와 `data/`는 untracked 상태다.
- 그 밖의 문서 수정·삭제·untracked 파일도 존재하며 모두 사용자 또는 다른 세션 소유다.
- AR-3 대상 파일은 작성 전 존재하지 않았다.

현재 root `pom.xml`은 `packaging=pom` aggregator가 아니라 단일 `jar` project다.

| 관찰 | 현재 값 |
|---|---|
| Root artifact | `com.ronext:ro-next:0.1.0-SNAPSHOT` |
| Java | `25.0.3-amzn` |
| Maven | `3.9.14` |
| Compiler release | `25` |
| Current modules | 없음 |
| Current runtime dependencies | Google Workflow Executions, Google Cloud Storage, Jackson |
| Current tests | JUnit 5 test 1개 |
| Shared isolated baseline | 관리 세션이 전달한 phase-00 격리 관찰: legacy `mvn test`/`mvn verify` PASS |

여러 phase 문서 세션이 같은 working directory의 `target/`에서 Maven을 동시에 실행하면 shade JAR replace 같은 transient 충돌이 날 수 있다. 동시 실행 중의 Maven failure는 repository baseline 결함, AR-3 blocker 또는 contract failure로 확정하지 않는다. 관리 세션이 전달한 위 격리 baseline을 공통 기준으로 사용하고, 구현 세션의 Maven 판정이 필요하면 다른 세션이 끝난 뒤 격리된 target/worktree에서 재실행한다.

Target reactor와 `rpdptw/core`, `rpdptw/solver`, `build/test-fixtures`, `build/architecture-rules`는 아직 존재하지 않는다. 이 phase가 root POM을 aggregator로 바꾸거나 legacy source를 이동해서는 안 된다. 그 작업은 AR-0 소유다.

### 2.2 현재 production/test/runtime 자료

현재 production source는 다음 6개뿐이다.

```text
src/main/java/com/ronext/optimizer/
├── application/AlnsBatchEngine.java
└── adapter/in/http/
    ├── HttpJson.java
    ├── JsonSupport.java
    ├── OptimizationApiController.java
    ├── OptimizationHttpServer.java
    └── OptimizationWorkerController.java
```

현재 test는 다음 하나뿐이다.

```text
src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java
```

`AlnsBatchEngine`은 `Map<String,Object>`와 임의 `double objective`를 반환하는 deterministic placeholder다. Pair, bank, prepared travel, propagation, profile, portfolio, route artifact와 rollback을 구현하지 않는다. 이 legacy class를 AR-3 interface, fixture, base class 또는 expected oracle로 재사용하지 않는다.

`README.md`, `gcp/README.md`, `gcp/workflows/optimization.yaml`, `gcp/cloudbuild.yaml`, `Dockerfile`은 `parallelRuns=8`, `iterationsPerRun=5000`, Cloud Run, Workflows, GCS를 설명한다. 이 값과 topology는 legacy characterization 자료일 뿐 AR-3 config/default 또는 `Q-BENCH-02` 답이 아니다.

`data/win_poc_case.json`의 SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이고 decimal `D/U` 때문에 official integer contract에 비준수다. AR-3 test는 이 파일을 parse, copy, round, truncate 또는 rewrite하지 않는다.

### 2.3 조사 재현 명령

구현 세션은 파일을 만들기 전에 다음을 그대로 실행하고 `target/codex-evidence/AR-3/<evidence-id>/commands.log`에 exit code와 함께 보존한다.

```bash
cd /Users/brown/workspace/ro-next
git status --short
rg --files -g 'pom.xml' -g '*.java' -g '*.xml' -g '*.yaml' -g '*.yml' -g '*.json' -g 'README.md' -g 'Dockerfile' -g '!target/**' -g '!node_modules/**' | sort
find . -name pom.xml -not -path './target/*' -not -path './node_modules/*' -print | sort
mvn -version
mvn -q test
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
```

AR-0 완료 뒤에는 다음으로 실제 reactor와 dependency를 추가 확인한다.

```bash
mvn -q help:evaluate -Dexpression=project.packaging -DforceStdout
mvn -pl rpdptw/core,rpdptw/solver -am dependency:tree
mvn -pl rpdptw/core,rpdptw/solver -am help:effective-pom
```

## 3. 현 상태 → 목표 상태 gap

| 영역 | 현 상태 | AR-3 목표 | 이 phase가 하지 않는 것 |
|---|---|---|---|
| Build | 단일 root JAR, target module 없음 | AR-0이 만든 reactor에서 core+solver만 변경 | Root/module migration |
| Pair | Pair/domain type 없음 | AR-2 query convention의 모든 legal pickup/delivery index pair를 atomic 단위로 평가 | Raw input pair 해석 |
| Feasibility | `double objective` placeholder | AR-2 propagation/evaluation을 단일 경로로 호출 | Policy별 hard feasibility 복제 |
| Bank | 없음 | request ID membership만 가진 immutable `SearchRequestBank` | Final status/diagnostic 저장 |
| Candidate | 없음 | `solver.state`가 소유하는 route/bank source-of-truth immutable `CommittedCandidate` | AR-4 current/stageBest/solveBest와 ALNS |
| Portfolio | 없음 | 고정 8 combination attempt, 최대 8 available candidate | `screenMaxSteps`와 champion selection |
| `CLOCK` | 없음 | 좌표/order key 부재를 typed `UNAVAILABLE`로 기록 | 다른 순서 fallback 또는 좌표 생성 |
| State isolation | 없음 | 조합별 route/bank/workspace 독립, 현 정책 RNG 없음 | Thread 병렬화와 global random |
| Equality | 없음 | option prediction = applied route cache-free recomputation | Incremental/cache 최적화 |
| Artifact | 없음 | source policy/config/ordered route/fingerprint lineage | Public JSON/storage schema |
| Verification | 없음 | AR-3 cache-free construction validation | AR-5 독립 verifier/PASS/publication |

## 4. AR-3 범위와 불변조건

### 4.1 In-scope

1. `rpdptw-core`의 legal pickup/delivery placement enumeration과 side-effect-free evaluation
2. Feasible option과 rejected option의 typed 분리
3. `rpdptw-solver`의 immutable bank/route/candidate와 atomic move apply boundary
4. 4개 request-route policy와 2개 vehicle-order policy의 고정 Cartesian product
5. 조합별 독립 construction workspace
6. `CLOCK`의 typed availability와 다른 조합 계속 실행
7. Stable total order와 deterministic trace
8. Ordered route/bank/artifact lineage와 fingerprints
9. 선택 option 적용 결과와 cache-free full recomputation의 exact equality
10. 실패/거절/예외 시 base candidate와 다른 조합이 변하지 않는 fault evidence

### 4.2 Stable-state 불변조건

모든 외부 관찰 지점, comparator 호출, artifact 생성과 method return 시 다음 XOR가 성립해야 한다.

```text
ASSIGNED_IN_SEARCH
= pickup과 delivery 의미가 한 vehicle route에 정확히 한 번 존재
  AND pickup이 delivery보다 앞섬
  AND request가 SearchRequestBank에 없음

UNASSIGNED_IN_SEARCH
= pickup과 delivery 의미가 어느 route에도 없음
  AND request가 SearchRequestBank에 정확히 한 번 존재
```

다음은 `INFEASIBLE`이 아니라 구현 defect다.

- partial/duplicate/split/reversed pair
- route+bank 동시 membership
- route와 bank 양쪽 누락
- 다른 vehicle/terminal의 node 삽입
- incomplete/unresolved prepared travel 사용
- stale option을 다른 base route에 적용
- option prediction과 cache-free applied evaluation 불일치

### 4.3 State authority와 derived state

Source of truth:

```text
ordered route sequence
request ownership
vehicle/terminal binding
SearchRequestBank membership
```

Derived and discardable:

```text
arrival/load/resource propagation
metrics/score/objective
insertion option table
construction workspace cache
candidate/artifact fingerprint
```

AR-3의 committed object는 모두 immutable이다. Construction 중 임시 workspace가 필요하면 combination attempt마다 새 instance를 만들고 artifact/candidate에 넣지 않는다. 현재 4개 policy는 무작위성을 사용하지 않으므로 `RandomGenerator`, global seed, clock을 호출하지 않는다. “독립 random state”는 현 범위에서 `NONE`으로 기록하며 RNG를 추가해 후보 수만 늘리는 shortcut을 금지한다.

## 5. 정확한 예상 경로

아래는 AR-0~2가 이 문서의 계획상 이름을 채택했다는 전제의 **계획상 고정 경로**다. 선행 phase가 같은 의미에 다른 이름을 승인했다면 중복 type을 만들지 말고 §1.1 절차로 이 문서를 먼저 갱신한다.

### 5.1 Test paths — production보다 먼저 생성

| 작업 | Repository-relative path |
|---|---|
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionFixtures.java` |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionEvaluatorTest.java` |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionEvaluatorPropertiesTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/state/SearchRequestBankTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/state/AtomicPairMoveApplierTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolioFixtures.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolioBuilderTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolioPolicyTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/portfolio/PortfolioCandidateIsolationTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolioArtifactTest.java` |
| 신규 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/build/architecture/PortfolioDependencyArchitectureTest.java` |

### 5.2 Production paths — 위 test의 red 확인 뒤 생성

| 작업 | Repository-relative path | 책임 |
|---|---|---|
| 선행 확인·AR-3 변경 금지 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/BoundProfile.java` | AR-2 `DONE` 산출물에 `ConstructionOrderingPolicy` accessor와 exact dependency/fingerprint가 포함되어야 함 |
| 선행 확인·AR-3 변경 금지 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ConstructionOrderingPolicy.java` | AR-2가 넘길 CLOCK/utilization/deadline의 versioned exact ordering seam |
| 선행 확인·AR-3 변경 금지 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ClockAvailability.java` | AR-2가 넘길 CLOCK origin/entry key의 available/unavailable result |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionEvaluator.java` | 모든 legal pair placement의 side-effect-free evaluation |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/FullEnumerationAtomicPairInsertionEvaluator.java` | AR-2 `PairInsertionEvaluator`를 모든 legal index pair에 적용 |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionEvaluation.java` | feasible/rejected query partition와 enumeration evidence |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/FeasiblePairInsertionOption.java` | base-bound query, upstream full evaluation과 position-stable rank; AR-4 repair의 동일 handoff type |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/RejectedPairInsertion.java` | query별 upstream typed hard rejection |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/AtomicInsertionDefectCode.java` | upstream structural/identity defect와 enumeration contract vocabulary |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/AtomicInsertionContractException.java` | identity/travel/profile/partition defect의 fail-fast error |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/SearchRequestBank.java` | immutable request membership |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/AtomicPairMoveApplier.java` | stale check, candidate copy, cache-free validation 후 commit 또는 전체 discard |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/FullRecomputationAtomicPairMoveApplier.java` | `FullEvaluationEngine`을 사용하는 reference atomic apply 구현 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/PairMoveApplyResult.java` | committed/rejected typed result와 before/after identity |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/RouteId.java` | candidate-local stable route identity; core에 노출하지 않음 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/CommittedRoute.java` | vehicle-bound ordered stable route |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CommittedCandidate.java` | downstream search 공통 canonical state; immutable ordered routes + independent bank + cache-free solution evaluation + semantic fingerprint |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/RequestRoutePolicyId.java` | 4개 고정 request-route policy enum |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/VehicleOrderPolicyId.java` | 2개 고정 vehicle order enum |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/PortfolioCombination.java` | 8개 stable identity |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolioRequest.java` | problem/travel/profile/config 입력 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolioBuilder.java` | fixed Cartesian product 실행과 결과 조립 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/DefaultInitialPortfolioBuilder.java` | 고정 4×2 combination을 실행하는 reference 구현 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolio.java` | 정확히 8 attempt와 최대 8 candidate |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/PortfolioAttempt.java` | `Available`/`Unavailable` sealed 결과 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/PortfolioUnavailableReason.java` | `CLOCK_COORDINATE_UNAVAILABLE`만 AR-3 normal unavailable로 허용 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/InitialCandidateArtifact.java` | combination, policy/config, cache-free evaluation, fingerprints, trace와 routes |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/RouteArtifact.java` | vehicle/route, ordered pair/node 의미, metrics, route fingerprint |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/RequestRouteGrowthPolicy.java` | attempt-local 다음 request stable ordering SPI |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/VehicleOrderPolicy.java` | request별 servable vehicle stable ordering SPI |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/DeterministicInitialCandidateConstructor.java` | 하나의 combination을 독립 workspace에서 실행 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/ClockRequestRoutePolicy.java` | bound CLOCK key만 소비 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/SequentialFarthestPolicy.java` | farthest seed + nearest growth |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/SequentialLargeDemandPolicy.java` | utilization seed + nearest growth |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/SequentialEarliestDeadlinePolicy.java` | deadline seed + nearest growth |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/DirectFirstLargeVehicleOrder.java` | DIRECT first, bound low-utilization order |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/internal/DirectFirstSmallVehicleOrder.java` | DIRECT first, bound high-utilization order |

위 세 ordering/profile 경로는 AR-2 owner의 선행 산출물이며 AR-3 구현 diff에 들어가면 안 된다. 현재 AR-2 phase 문서에 이 exact seam이 없으므로 AR-2가 같은 의미의 approved handoff를 제공하기 전 AR-3은 `BLOCKED`다. 이름만 다르면 §1.1 mapping 절차를 적용하고, 의미가 없으면 AR-3에서 임시 API를 만들지 않는다. `pom.xml` 변경, source 이동, resource/schema 추가도 AR-3 예상 범위가 아니다. AR-0이 JUnit/architecture convention, solver→core 또는 test-scope fixture dependency를 제대로 만들지 않았다면 POM을 임시 수정하지 말고 AR-0 handoff를 `BLOCKED`로 돌려보낸다.

### 5.3 이동·삭제하지 않는 경로

다음을 이동, 삭제, rewrite 또는 포맷하지 않는다.

- `src/main/java/com/ronext/optimizer/**`
- `src/test/java/com/ronext/optimizer/**`
- Root와 aggregator POM
- `README.md`, `Dockerfile`, `gcp/**`
- `data/**`
- 설계 원문, 다른 phase 문서와 progress 문서

## 6. 계획상 제안 package/type/API

### 6.1 API 표기 규칙

아래 public은 Maven module 내부에서 solver가 소비할 Java visibility를 뜻한다. 외부 HTTP/JSON/public SDK 승인을 뜻하지 않는다. Collection은 생성 시 defensive copy하고 iteration order를 고정한다. `null`, raw `Map<String,Object>`, floating tolerance와 unordered `HashSet` iteration을 계약에 노출하지 않는다.

AR-1/2 identity와 evaluation type은 여기서 새로 만들지 않고 실제 handoff type을 사용한다. 아래 signature의 `RequestId`, `VehicleId`, `ProblemInstance`, `PreparedTravel`, `BoundProfile`, `RouteSequence`, `SolveEvaluationContext`, `PairInsertionQuery`, `InsertionEvaluation`, `PairInsertionEvaluator`, `FullEvaluationEngine`, `ObjectiveComparator`, `StableSolutionKey`, `RouteEvaluation`, `SolutionEvaluation`, `EvaluatedSolution`, `SemanticFingerprint`는 선행 phase owner type을 가리킨다. 특히 AR-2의 단일-position `PairInsertionEvaluator`를 대체하거나 같은 full propagation을 복제하지 않는다.

### 6.2 Bound construction ordering seam

AR-3 solver는 coordinate, capacity missing/zero 또는 deadline raw field를 직접 해석하지 않는다. AR-2의 `BoundProfile`은 다음 계획상 API를 exact dependency closure와 fingerprint에 포함해야 한다.

```java
package com.ronext.rpdptw.evaluation.api;

public interface ConstructionOrderingPolicy {
    ClockAvailability clockAvailability(
            VehicleId vehicleId,
            List<RequestId> requestIds);

    int compareClockwise(
            VehicleId vehicleId,
            RequestId left,
            RequestId right);

    int compareVehicleUtilization(
            RequestId requestId,
            VehicleId left,
            VehicleId right);

    int compareRequestUtilization(
            VehicleId vehicleId,
            RequestId left,
            RequestId right);

    long normalizedDeadlineSeconds(RequestId requestId);

    SemanticFingerprint fingerprint();
    String version();
}
```

AR-2의 계획상 `BoundProfile` API에는 다음 accessor 하나를 추가하고 `dependencyClosure`, `definitionFingerprint`, `boundProfileFingerprint`에 이 policy의 version/fingerprint를 포함한다.

```java
ConstructionOrderingPolicy constructionOrderingPolicy();
```

```java
public sealed interface ClockAvailability
        permits ClockAvailability.Available, ClockAvailability.Unavailable {

    record Available() implements ClockAvailability {}

    record Unavailable(
            List<PhysicalLocationId> missingOriginsOrEntries)
            implements ClockAvailability {}
}
```

계약:

- `compare*`의 음수/0/양수는 left-before/tie/right-before다.
- Comparator는 transitive, antisymmetric이며 동일 bound snapshot에서 deterministic해야 한다.
- Utilization missing/zero의 정확한 의미는 bound policy 구현과 fingerprint가 소유한다.
- `Feature` 문자열 숫자나 등록 순서로 크기를 추론하지 않는다.
- `clockAvailability`가 `Unavailable`이면 `compareClockwise`를 호출하지 않는다.
- Solver가 raw coordinate로 angle/bearing을 다시 계산하면 architecture violation이다.
- `normalizedDeadlineSeconds`는 adapter raw string이 아니라 AR-1 normalized time축 값이다.
- 위 dependency가 없거나 unresolved면 profile binding/AR-3 entry가 `BLOCKED`이며 solver fallback을 만들지 않는다.

### 6.3 Atomic insertion evaluation

```java
package com.ronext.rpdptw.evaluation.insertion;

public interface AtomicPairInsertionEvaluator {
    AtomicPairInsertionEvaluation enumerate(
            RouteSequence baseRoute,
            RequestId requestId,
            SolveEvaluationContext context);
}
```

`FullEnumerationAtomicPairInsertionEvaluator`는 public constructor
`FullEnumerationAtomicPairInsertionEvaluator(PairInsertionEvaluator singleQueryEvaluator)`를 가지며 위 `enumerate` signature를 구현한다.

```java
public record AtomicPairInsertionEvaluation(
        RequestId requestId,
        VehicleId vehicleId,
        SemanticFingerprint baseRouteFingerprint,
        List<FeasiblePairInsertionOption> feasibleOptionsInPositionOrder,
        List<RejectedPairInsertion> rejectedOptions,
        int enumeratedQueryCount,
        String enumerationContractVersion,
        SemanticFingerprint evaluationFingerprint) {
}
```

```java
public record FeasiblePairInsertionOption(
        PairInsertionQuery query,
        InsertionEvaluation upstreamEvaluation,
        SemanticFingerprint baseRouteFingerprint,
        SemanticFingerprint optionFingerprint) {
}

public record RejectedPairInsertion(
        PairInsertionQuery query,
        InsertionEvaluation upstreamEvaluation) {
}
```

`FullEnumerationAtomicPairInsertionEvaluator`는 constructor로 AR-2의 `PairInsertionEvaluator`를 주입받고 각 query를 정확히 한 번 호출한다. `InsertionEvaluation`의 실제 sealed feasible/rejected accessor는 AR-2 handoff를 그대로 사용하며 AR-3가 별도 hard-feasibility result type을 만들지 않는다.

Legal query index는 AR-2 `PairInsertionQuery` 계약에 맞춘다.

- `RouteSequence.orderedNodeIds`의 start terminal과 roundtrip end terminal은 고정이다.
- Delivery-only `LOGICAL_START_PICKUP`은 start terminal 직후의 연속 prefix에 stable `RequestId` 순서로 삽입하며 travel/stop/service를 만들지 않는다.
- Real pickup과 모든 physical delivery는 logical prefix 뒤, fixed end terminal 앞의 customer segment에만 삽입한다.
- Query는 `0 <= pickupInsertionIndex < deliveryInsertionIndex`를 만족하며, delivery index는 pickup을 materialize한 뒤의 list index다.
- Terminal/prefix legality가 불확실하면 임의로 보정하지 않고 AR-2 `RouteStructureGate`와 insertion reference test의 동일 index convention을 사용한다.

불변조건:

- `feasibleOptionsInPositionOrder`에는 hard-feasible option만 있고 core 내부 순서는 pickup index → delivery index다.
- 모든 legal `PairInsertionQuery`는 feasible 또는 rejected 중 정확히 하나다.
- 구조/static/servableVehicles gate → full propagation → bound hard constraint → neutral metrics/score/objective 순서는 AR-2 `PairInsertionEvaluator`가 소유하며 AR-3가 재구현하지 않는다.
- Normal hard infeasibility는 upstream rejected `InsertionEvaluation`이고 exception이 아니다.
- Partial base state, prepared-travel/profile identity mismatch와 upstream structural defect는 `AtomicInsertionContractException`이다.
- Evaluator는 base route, candidate, bank, cache, profile을 mutate하지 않는다.
- Option은 base route fingerprint에 bind되어 stale apply를 막는다.
- Core evaluator 내부 position tie는 `pickupInsertionIndex → deliveryInsertionIndex`다. Request/vehicle/route의 최종 total order는 solver portfolio가 붙인다.

`AtomicInsertionDefectCode`는 최소 다음을 구분한다.

```java
public enum AtomicInsertionDefectCode {
    BASE_ROUTE_FINGERPRINT_MISMATCH,
    REQUEST_ALREADY_PRESENT_IN_ROUTE,
    REQUEST_NOT_IN_BANK,
    UPSTREAM_STRUCTURAL_PAIR_DEFECT,
    PREPARED_TRAVEL_IDENTITY_MISMATCH,
    BOUND_PROFILE_IDENTITY_MISMATCH,
    ENUMERATION_PARTITION_MISMATCH,
    CACHE_FREE_RECOMPUTATION_MISMATCH
}

public final class AtomicInsertionContractException extends RuntimeException {
    public AtomicInsertionDefectCode code();
    public RequestId requestId();
    public Optional<VehicleId> vehicleId();
    public Optional<SemanticFingerprint> baseRouteFingerprint();
}
```

Capacity, time window, resource와 bound hard constraint failure는 이 enum에 복제하지 않고 upstream rejection evidence를 보존한다. 위 defect는 정상 option ranking 또는 `UNAVAILABLE`로 숨기지 않고 contract exception으로 올린다.

### 6.4 Bank, committed candidate와 atomic apply

```java
package com.ronext.rpdptw.solver.state;

public final class SearchRequestBank {
    public static SearchRequestBank allRequests(ProblemInstance problem);
    public boolean contains(RequestId requestId);
    public int size();
    public List<RequestId> requestIdsInStableOrder();
    public SearchRequestBank add(RequestId requestId);
    public SearchRequestBank remove(RequestId requestId);
    public SemanticFingerprint fingerprint();
}
```

Bank에는 request ID membership만 저장한다. Node, cost, last failure, final status, diagnostic, outsourced/deferred 의미를 넣지 않는다. Duplicate add는 `IllegalStateException("REQUEST_ALREADY_IN_BANK:" + requestId)`, absent remove는 `IllegalStateException("REQUEST_NOT_IN_BANK:" + requestId)`로 실패하고 원본 bank를 바꾸지 않는다. 이 문자열은 phase 내부 test/diagnostic 계약이며 외부 wire error가 아니다.

```java
package com.ronext.rpdptw.solver.portfolio;

public record RouteId(int stableOrdinal) {
}

public record CommittedRoute(
        RouteId routeId,
        RouteSequence routeSequence,
        RouteEvaluation cacheFreeEvaluation,
        SemanticFingerprint routeFingerprint) {
}
```

```java
package com.ronext.rpdptw.solver.state;

import com.ronext.rpdptw.solver.portfolio.CommittedRoute;

public record CommittedCandidate(
        SemanticFingerprint problemFingerprint,
        SemanticFingerprint preparedTravelFingerprint,
        SemanticFingerprint boundProfileFingerprint,
        List<CommittedRoute> routesInStableOrder,
        SearchRequestBank requestBank,
        SolutionEvaluation cacheFreeEvaluation,
        EvaluatedSolution comparatorView,
        SemanticFingerprint candidateFingerprint) {
}
```

`CommittedCandidate`의 canonical owner는 `com.ronext.rpdptw.solver.state`다. Portfolio는 초기 state를 생산하는 consumer/producer workflow일 뿐 candidate type을 소유하지 않는다. AR-4 search/COW와 이후 downstream도 이 state type 하나를 공통 사용하며 `solver.portfolio.CommittedCandidate` alias, wrapper 또는 compatibility duplicate를 만들지 않는다.

```java
package com.ronext.rpdptw.solver.state;

import com.ronext.rpdptw.solver.portfolio.RouteId;

public interface AtomicPairMoveApplier {
    PairMoveApplyResult apply(
            CommittedCandidate base,
            RouteId targetRouteId,
            FeasiblePairInsertionOption selected,
            SolveEvaluationContext context);
}
```

```java
public sealed interface PairMoveApplyResult
        permits PairMoveApplyResult.Committed, PairMoveApplyResult.Rejected {

    record Committed(
            CommittedCandidate candidate)
            implements PairMoveApplyResult {}

    record Rejected(
            AtomicInsertionDefectCode code,
            SemanticFingerprint unchangedBaseFingerprint)
            implements PairMoveApplyResult {}
}
```

Apply 순서:

1. problem/travel/profile/base/option identity와 base route fingerprint 비교
2. base route와 bank에서 pair XOR 확인
3. route sequence와 bank를 새 value로 계산
4. 영향 route를 cache 없이 full recompute
5. option의 upstream feasible `InsertionEvaluation`과 materialized route의 cache-free `RouteEvaluation` exact equality 비교
6. 새 route 목록과 새 bank로 `FullEvaluationEngine.evaluateSolution`을 호출해 cache-free `SolutionEvaluation`과 canonical `StableSolutionKey`를 만든다.
7. candidate 전체 pair/terminal/bank invariant를 검증한다.
8. 새 fingerprint와 `EvaluatedSolution` comparator view를 계산한 뒤에만 `Committed`를 반환한다.

1~7 중 하나라도 실패하면 새 candidate를 반환하지 않고 base fingerprint가 같은 `Rejected` 또는 contract exception을 반환한다. Base object와 이미 committed된 다른 candidate는 관찰상 동일해야 한다. `comparatorView`는 저장된 cache를 신뢰해 재평가한 값이 아니라 위 cache-free `SolutionEvaluation`의 objective vector와 canonical route/bank `StableSolutionKey`를 조립한 immutable `EvaluatedSolution`이며 둘의 불일치는 contract defect다.

Reference implementation의 public constructor signature는
`FullRecomputationAtomicPairMoveApplier(FullEvaluationEngine fullEvaluationEngine)`다.

### 6.5 Portfolio API

```java
package com.ronext.rpdptw.solver.portfolio;

import com.ronext.rpdptw.solver.state.CommittedCandidate;

public enum RequestRoutePolicyId {
    CLOCK,
    SEQ_FARTHEST,
    SEQ_LARGE_DEMAND,
    SEQ_EARLIEST_DEADLINE
}

public enum VehicleOrderPolicyId {
    DIRECT_FIRST_LARGE,
    DIRECT_FIRST_SMALL
}
```

```java
public record PortfolioCombination(
        RequestRoutePolicyId requestRoutePolicy,
        VehicleOrderPolicyId vehicleOrderPolicy) {

    public String stableId();
}
```

`stableId()`는 enum 이름을 그대로 결합한 `REQUEST_POLICY__VEHICLE_POLICY`다. 외부 wire ID 승인이 아니라 trace/evidence 내부 identity다.

```java
public record InitialPortfolioRequest(
        SolveEvaluationContext evaluationContext,
        String portfolioAlgorithmVersion) {
}

public interface InitialPortfolioBuilder {
    InitialPortfolio build(InitialPortfolioRequest request);
}
```

```java
public record InitialPortfolio(
        List<PortfolioAttempt> attemptsInStableOrder,
        List<CommittedCandidate> availableCandidates,
        SemanticFingerprint portfolioFingerprint) {
}
```

```java
public sealed interface PortfolioAttempt
        permits PortfolioAttempt.Available, PortfolioAttempt.Unavailable {

    PortfolioCombination combination();

    record Available(
            PortfolioCombination combination,
            CommittedCandidate candidate,
            InitialCandidateArtifact artifact)
            implements PortfolioAttempt {}

    record Unavailable(
            PortfolioCombination combination,
            PortfolioUnavailableReason reason,
            List<PhysicalLocationId> missingLocations)
            implements PortfolioAttempt {}
}

public enum PortfolioUnavailableReason {
    CLOCK_COORDINATE_UNAVAILABLE
}
```

계약:

- `attemptsInStableOrder`는 항상 정확히 8개다.
- 순서는 request policy enum 선언 순서가 outer loop, vehicle order enum 선언 순서가 inner loop다.
- `availableCandidates`는 `Available` attempt와 1:1이고 0~8개다.
- 서로 다른 combination이 같은 semantic route/bank를 만들면 candidate fingerprint가 같을 수 있다. 그래도 candidate/route/bank/workspace object는 공유하지 않으며 combination을 포함한 artifact fingerprint와 attempt identity는 서로 다르다.
- AR-3 정상 `Unavailable` reason은 `CLOCK_COORDINATE_UNAVAILABLE` 하나뿐이다.
- Non-CLOCK 조합을 좌표 문제로 unavailable 처리하지 않는다.
- Internal defect, evaluator mismatch, unresolved ordering은 `Unavailable`로 낮추지 않고 전체 `build`를 실패시킨다.
- 한 조합의 정상 infeasible request는 candidate를 실패시키지 않고 bank에 남는다.
- Empty/all-bank candidate도 stable feasible state라면 available candidate다.

### 6.6 Route artifact와 lineage

```java
public record InitialCandidateArtifact(
        PortfolioCombination combination,
        String portfolioAlgorithmVersion,
        String constructionOrderingVersion,
        SemanticFingerprint problemFingerprint,
        SemanticFingerprint preparedTravelFingerprint,
        SemanticFingerprint boundProfileFingerprint,
        SemanticFingerprint constructionOrderingFingerprint,
        List<RouteArtifact> routesInStableOrder,
        List<RequestId> bankInStableOrder,
        List<InitialCandidateArtifact.InsertionTraceEntry> insertionTrace,
        SolutionEvaluation cacheFreeEvaluation,
        SemanticFingerprint candidateFingerprint,
        SemanticFingerprint artifactFingerprint) {
}
```

```java
public record RouteArtifact(
        RouteId routeId,
        VehicleId vehicleId,
        List<RequestId> requestsInServiceOrder,
        List<SolverNodeId> nodesInServiceOrder,
        RouteEvaluation cacheFreeEvaluation,
        SemanticFingerprint routeFingerprint) {
}
```

`InsertionTraceEntry`는 `InitialCandidateArtifact.java`의 public nested record로 둔다. 별도 top-level type이나 외부 event schema로 확장하지 않는다.

```java
public static record InsertionTraceEntry(
        int committedStepOrdinal,
        RequestId requestId,
        VehicleId vehicleId,
        RouteId routeId,
        int pickupInsertionIndex,
        int deliveryInsertionIndex,
        SemanticFingerprint optionFingerprint,
        SemanticFingerprint beforeCandidateFingerprint,
        SemanticFingerprint afterCandidateFingerprint) {
}
```

`committedStepOrdinal`은 0부터 연속 증가하며 commit되지 않은 option/rejected draft는 trace entry를 만들지 않는다. `PortfolioAttempt.Unavailable`도 partial trace/artifact를 보존하지 않는다. `DefaultInitialPortfolioBuilder`의 public constructor signature는 `DefaultInitialPortfolioBuilder(AtomicPairInsertionEvaluator insertionEvaluator, AtomicPairMoveApplier moveApplier)`다.

`InitialPortfolioBuilder`, `DefaultInitialPortfolioBuilder`, `InitialPortfolio`, `PortfolioAttempt`와 `InitialCandidateArtifact`는 모두 `com.ronext.rpdptw.solver.state.CommittedCandidate`를 import해 소비한다. `InitialCandidateArtifact`의 계획상 package-private projection factory는 `InitialCandidateArtifact from(PortfolioCombination combination, String portfolioAlgorithmVersion, String constructionOrderingVersion, CommittedCandidate candidate, List<InsertionTraceEntry> insertionTrace)` signature로 state candidate의 immutable route/bank/evaluation/fingerprint를 artifact 필드에 복사한다. Artifact가 candidate owner가 되거나 별도 candidate representation을 만들지는 않는다.

Portfolio 내부 SPI와 constructor는 module 외부에 노출하지 않는다.

```java
interface RequestRouteGrowthPolicy {
    RequestRoutePolicyId id();

    List<RequestId> rankNextRequests(
            CommittedCandidate current,
            RouteId activeRouteId,
            SolveEvaluationContext context);
}

interface VehicleOrderPolicy {
    VehicleOrderPolicyId id();

    List<VehicleId> rankVehicles(
            RequestId requestId,
            CommittedCandidate current,
            SolveEvaluationContext context);
}
```

`DeterministicInitialCandidateConstructor`의 package-private method signature는
`PortfolioAttempt construct(PortfolioCombination combination, InitialPortfolioRequest request)`다. 두 `rank*` method는 bank/servable membership을 빠뜨리거나 새 request/vehicle을 만들 수 없고, 입력 collection의 immutable stable permutation만 반환한다. `CLOCK` availability preflight와 attempt-local workspace 생성은 `construct`가 policy method 호출 전에 수행한다.

Artifact는 elapsed time, object identity, thread order, map iteration order, filesystem/provider locator를 fingerprint에 넣지 않는다. Cache contents도 넣지 않는다. External ID mapping은 AR-1 lineage를 참조하되 fingerprint는 canonical dense/stable representation으로 계산한다. Serialization/storage schema는 AR-6/ADR 영역이므로 이 phase에서 JSON annotation을 붙이지 않는다.

AR-3 artifact의 “validation”은 pair/route/bank invariant와 cache-free recomputation을 뜻한다. AR-5의 independent candidate verifier `PASS` 또는 `VerifiedSolution`을 뜻하지 않으며 그렇게 이름 붙이지 않는다.

## 7. 실행 알고리즘

### 7.1 Atomic evaluator

각 request-route에 대해 다음 순서를 고정한다.

```text
validate immutable identities
→ enumerate every legal AR-2 PairInsertionQuery index pair
→ call AR-2 PairInsertionEvaluator exactly once per query
→ propagate upstream structural/identity defect
→ partition upstream feasible and normal hard-rejected evaluations
→ stable position-order feasible options
→ immutable evaluation return
```

Policy는 evaluator를 우회해 capacity, time, zone, capability, terminal 또는 customer hard constraint를 검사하지 않는다. Policy가 하는 일은 어떤 request/vehicle/route를 먼저 evaluator에 보낼지와 feasible options 중 bound rank+stable tie로 하나를 선택하는 것뿐이다.

### 7.2 하나의 combination construction

모든 combination은 다음 동일 lifecycle을 사용한다.

1. 모든 input vehicle에 대해 terminal policy를 만족하는 empty `CommittedRoute` value를 stable vehicle ID 순서로 만든다.
2. 모든 request를 가진 새 `SearchRequestBank`를 만든다.
3. 새 attempt-local workspace를 만든다. 다른 combination과 route, bank, option list, cache/scratch를 공유하지 않는다.
4. Request-route policy가 현재 route의 seed 또는 다음 request를 제안한다.
5. Vehicle-order policy가 그 request의 servable input vehicle만 정렬한다.
6. 각 target route의 legal pair positions를 공통 evaluator로 전량 평가한다.
7. 각 feasible option을 현재 immutable candidate에 `AtomicPairMoveApplier`로 적용해 cache-free full-solution hypothetical candidate를 만든다. Rejected/exception draft는 폐기한다.
8. Hypothetical candidate를 `BoundProfile.comparator().compare(candidate.comparatorView(), ...)`로 비교하고, quality가 같은 경우 §7.5 stable tuple로 하나를 선택한다. 첫 feasible option shortcut과 route-only 임시 score는 금지한다.
9. 선택된 `CommittedCandidate`만 current construction state로 교체하고 선택되지 않은 immutable hypothetical value는 폐기한다.
10. 선택할 feasible option이 없는 request는 bank에 그대로 둔다.
11. Active route에 한 full stable bank sweep 동안 commit이 하나도 없으면 그 route를 닫고 다음 route/vehicle로 이동한다.
12. 어떤 unused vehicle에도 feasible seed가 없으면 종료한다.
13. 전체 candidate를 cache-free recompute하고 artifact/fingerprint를 생성한다.

Vehicle route를 “활성”으로 고르는 계획상 내부 규칙은 다음 stable tuple이다.

```text
ownership rank (DIRECT before LEASE)
→ bound utilization order for the current request
→ stable vehicle ID
→ stable route ID
```

`DIRECT_FIRST_LARGE`는 bound policy가 정의한 낮은 utilization vehicle을 먼저, `DIRECT_FIRST_SMALL`은 높은 utilization vehicle을 먼저 둔다. Missing/zero 처리는 solver가 정하지 않는다.

### 7.3 Request-route policy

| Policy | Seed/성장 규칙 | 금지 |
|---|---|---|
| `CLOCK` | Active vehicle start terminal을 origin으로 하는 bound `compareClockwise` order를 사용해 0도부터 clockwise 순회한다. Stable ID가 마지막 tie다. | Raw coordinate/bearing 계산, missing coordinate를 distance/ID order로 fallback |
| `SEQ_FARTHEST` | Empty route seed는 prepared directed `D[startTerminal][entryLocation]`가 큰 request. 그 뒤에는 current last confirmed service location에서 entry location까지 `D`가 작은 request. | Reverse arc, 대칭화, 좌표 거리 |
| `SEQ_LARGE_DEMAND` | Empty route seed는 bound `compareRequestUtilization`상 큰 request, tie는 이른 normalized deadline, stable request ID. 그 뒤에는 current last confirmed service location에서 가까운 request. | Weight/volume `double` 나눗셈, missing/zero 추정 |
| `SEQ_EARLIEST_DEADLINE` | Empty route seed는 `normalizedDeadlineSeconds`가 작은 request, tie는 utilization 큰 request, stable request ID. 그 뒤에는 current last confirmed service location에서 가까운 request. | Raw `reqDate` 문자열 parse, due/req alias 재해석 |

Entry location은 AR-1 service pattern 의미를 그대로 사용한다.

- Real pickup-delivery: pickup physical location
- Delivery-only: delivery physical location

“가까움”은 complete `PreparedTravel`의 authoritative directed distance `D[currentServiceLocation][entryLocation]`다. `U`, coordinate, reverse lookup, speed 또는 customer name을 사용하지 않는다.

### 7.4 CLOCK unavailable

`CLOCK` combination은 다음처럼 처리한다.

1. Stable traversal 중 active vehicle origin과 비교 대상 request entry location에 대해 `clockAvailability`를 먼저 요청한다.
2. `Unavailable`이면 해당 combination draft 전체를 폐기한다.
3. `PortfolioAttempt.Unavailable(CLOCK_COORDINATE_UNAVAILABLE, missingLocations)`를 기록한다.
4. 같은 vehicle-order의 다른 request policy와 나머지 combination을 계속한다.
5. Partial route/candidate/artifact를 available 목록에 넣지 않는다.

Missing coordinate를 Great Circle로 생성하거나 prepared distance order로 대체하지 않는다. `CLOCK` unavailable은 input pre-solve error나 전체 portfolio failure가 아니다. 반대로 unresolved/malformed bound CLOCK policy는 normal unavailable이 아니라 AR-2/profile contract defect다.

### 7.5 Stable tie-break

같은 semantic rank의 최종 total order는 다음이다.

```text
request-policy semantic rank
→ vehicle-order semantic rank
→ exact full-solution objective quality
→ stable request ID
→ stable vehicle ID
→ stable route ID
→ pickup insertion index
→ delivery insertion index
```

동률을 collection iteration, completion order, object hash, wall clock 또는 random으로 깨지 않는다. Comparator law test가 transitivity/antisymmetry와 permutation independence를 검사한다.

### 7.6 Rollback 동등성

실패 전후 동등성은 단순 object reference 비교가 아니라 다음 전체 관찰을 비교한다.

```text
route count/order
ordered node/request sequence
vehicle/terminal binding
bank stable membership/order
candidate fingerprint
all pre-existing route fingerprints
cache-free route/solution evaluation
already emitted sibling candidate/artifact fingerprints
```

Normal rejected placement, stale option, injected propagation exception, injected fingerprint mismatch에서 모두 base 관찰값이 byte-for-byte/canonical-equality로 같아야 한다. Candidate draft가 exception을 던진 뒤 재사용되는 경로는 만들지 않고 draft 전체를 폐기한다.

## 8. Test fixture 계약

Fixture는 raw JSON이 아니라 AR-1/2 test builder가 만드는 immutable Java artifact다. 현재 Win fixture를 쓰지 않는다.

| Fixture | 구성 | 목적 |
|---|---|---|
| `PAIR_SMALL` | Real pair 1개, DIRECT vehicle 1대, complete asymmetric travel, hand-calculated AR-2 query indices | position enumeration와 cache-free equality |
| `PAIR_CAPACITY_REJECTED` | Demand가 vehicle capacity를 정확히 초과 | infeasible option이 ranking에 없음 |
| `PAIR_MIXED_SERVICE` | Delivery-only `LOGICAL_START_PICKUP` 1개 + real pair 1개 | canonical prefix query와 pair partition |
| `PAIR_STALE_OPTION` | Option 평가 뒤 같은 route의 다른 committed fingerprint | stale apply 전체 거절 |
| `PORTFOLIO_ALL_AVAILABLE` | 좌표/order key complete, DIRECT+LEASE, stable tie, 모든 request에 feasible vehicle | 8개 combination와 policy trace |
| `PORTFOLIO_CLOCK_MISSING` | 한 required entry coordinate의 bound CLOCK key unavailable, prepared travel은 complete | CLOCK 2개 unavailable, non-CLOCK 6개 계속 |
| `PORTFOLIO_FEATURE_TRAP` | `"10t"`/`"2t"` lexical/numeric 직관과 bound utilization order가 반대 | Feature 문자열 parsing 금지 |
| `PORTFOLIO_EQUAL_KEYS` | 모든 semantic policy key가 tie | stable ID/position total order |
| `PORTFOLIO_UNASSIGNABLE` | Static compatible vehicle 없음 | request가 bank에 남은 valid candidate |
| `PORTFOLIO_FAULT_AT_K` | K번째 full propagation/commit에 injected exception | draft discard와 sibling/base isolation |

Hand expected 값은 fixture builder 옆에 literal route sequence, bank membership, integer metrics와 fingerprint input tuple로 적는다. Search implementation으로 expected를 생성하지 않는다.

## 9. 세분화된 test case 표

### 9.1 Core evaluator

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `AtomicPairInsertionEvaluatorTest.enumeratesEveryLegalRealPairQuery` | Unit; Master §11.1, AR-2 §6.6 | `PAIR_SMALL`, base service length 2 | legal `PairInsertionQuery(pickupIndex,deliveryIndex)` 수와 exact 목록 | 최초 scaffold 전 `cannot find symbol: class AtomicPairInsertionEvaluator`; skeleton 후 `expected: <N> but was: <0>` | 모든 legal query가 feasible/rejected 중 정확히 하나 |
| `AtomicPairInsertionEvaluatorTest.neverRanksPartialOrInfeasiblePair` | Unit/negative; Master §6, §11.1 | `PAIR_CAPACITY_REJECTED` + deliberately partial base | Capacity case feasible list empty/upstream rejection 존재; partial base는 contract exception | `hard-infeasible query must not enter feasibleOptionsInPositionOrder` 또는 partial base가 normal rejected로 내려감 | Infeasible option 0, defect fail-fast |
| `AtomicPairInsertionEvaluatorTest.matchesCacheFreeAppliedRoute` | Property; implementation plan §9.4, Master §11.1/§15.5 | `PAIR_SMALL`의 각 feasible query | option upstream route evaluation = query materialization의 독립 cache-free route evaluation | `expected cache-free RouteEvaluation(...) but was ...` | 모든 feasible query의 facts/metrics/score/objective exact equality |
| `AtomicPairInsertionEvaluatorTest.supportsDeliveryOnlyLogicalPrefixWithoutSyntheticTravel` | Unit; Domain §10.2, AR-1 §6.3 | `PAIR_MIXED_SERVICE` | logical pickup은 canonical prefix index, depot logical pickup travel/stop/service 0 | prefix index mismatch 또는 extra depot arc/stop | Hand load prefix/route metrics exact |
| `AtomicPairInsertionEvaluatorTest.usesPreparedDirectedArcOnly` | Unit; Master §8/§11.2 | asymmetric `PAIR_SMALL` | Forward `D/U` hand result, reverse 값 미사용 | `expected distance <forward> but was <reverse/generated>` | Prepared forward arc exact |
| `AtomicPairInsertionEvaluatorTest.ordersPositionTiesByInsertionIndices` | Unit; Master §11.1 | equal upstream route evaluations | core option order가 pickup index→delivery index exact list | Permutation마다 option position order가 달라짐 | 모든 input permutation same position order |
| `AtomicPairInsertionEvaluatorPropertiesTest.partitionsQueriesWithoutLossOrDuplication` | Property; Domain §10, AR-2 §6.6 | generated small legal route lengths/service patterns | `feasible ∪ rejected = legal queries`, intersection empty | `expected enumerated count ...` assertion | 100+ deterministic cases pass |
| `AtomicPairInsertionEvaluatorPropertiesTest.leavesEveryInputFingerprintUnchanged` | Property; atomicity | generated stable base | problem/travel/profile/route fingerprints unchanged | 하나 이상의 before/after mismatch | 모든 deterministic cases exact equality |

### 9.2 Bank와 atomic apply

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `SearchRequestBankTest.containsOnlyStableRequestMembership` | Unit; Domain §10.3 | request IDs 0..3 | stable order, immutable add/remove | `expected original size 4 but was 3` | 원본 불변, duplicate/absent operation fail |
| `SearchRequestBankTest.rejectsDuplicateAddAndAbsentRemove` | Negative | small bank | typed invariant error, unchanged fingerprint | operation silently succeeds | error code와 fingerprint equality |
| `AtomicPairMoveApplierTest.matchesCacheFreeAppliedRoute` | Property; Master §11.1/§15.5 | `PAIR_SMALL` selected feasible option | predicted evaluation = full recompute = committed route | `expected RouteEvaluation(...) but was ...` | 전체 facts/metrics/score/objective exact |
| `AtomicPairMoveApplierTest.movesRequestFromBankToRouteAtomically` | Unit/property | `PAIR_SMALL` | pair complete on one route, bank absent | route+bank duplicate 또는 partial pair assertion | XOR와 candidate fingerprint pass |
| `AtomicPairMoveApplierTest.rejectsStaleOptionWithoutMutation` | Fault | `PAIR_STALE_OPTION` | `Rejected(BASE_ROUTE_FINGERPRINT_MISMATCH, ...)`, base exact unchanged | stale option commits 또는 fingerprint 변함 | §7.6 모든 관찰 동일 |
| `AtomicPairMoveApplierTest.discardsOnRecomputationMismatch` | Corruption/fault | poisoned predicted evaluation | `CACHE_FREE_RECOMPUTATION_MISMATCH`, no candidate | corrupted option accepted | base/previous candidates unchanged |
| `AtomicPairMoveApplierTest.discardsOnPropagationException` | Fault | injected exception | exception propagated as defect, no partial state | bank/route/fingerprint 하나 변경 | §7.6 rollback equality |

### 9.3 Portfolio combinations와 policy

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `InitialPortfolioBuilderTest.emitsAllAvailableFourByTwoCombinations` | Unit/integration; `Q-ALG-01` | `PORTFOLIO_ALL_AVAILABLE` | attempts 8, available 8, exact stable IDs | skeleton 전 `cannot find symbol: class InitialPortfolioBuilder`; skeleton 후 `expected: <8> but was: <0>` | exact Cartesian set/order, candidate object/state 독립, 8개 unique artifact fingerprints; 같은 semantic candidate fingerprint는 허용 |
| `InitialPortfolioBuilderTest.marksClockUnavailableWithoutFallback` | Unit; Master §11.2 | `PORTFOLIO_CLOCK_MISSING` | CLOCK/LARGE와 CLOCK/SMALL unavailable, reason+missing ID | `expected CLOCK_COORDINATE_UNAVAILABLE` 또는 CLOCK candidate 생성 | CLOCK 0 available, other 6 available |
| `InitialPortfolioBuilderTest.continuesEveryNonClockCombinationAfterClockUnavailable` | Fault/integration | same | Non-CLOCK exact 6 combinations emitted | build가 전체 abort 또는 6보다 적음 | exact six identities |
| `InitialPortfolioBuilderTest.keepsUnassignableRequestInBank` | Unit | `PORTFOLIO_UNASSIGNABLE` | every available candidate has request in bank, route 없음 | request 누락 또는 fake route/vehicle 생성 | pair XOR와 candidate validation pass |
| `InitialPortfolioPolicyTest.clockConsumesBoundOrderAndNeverRawCoordinates` | Unit/architecture | fake bound clock order deliberately unlike coordinate intuition | fake bound order의 route trace | coordinate-derived expected와 일치하거나 raw getter 호출 spy > 0 | bound comparator calls only, raw calls 0 |
| `InitialPortfolioPolicyTest.seqFarthestUsesDirectedDepotThenLastLocationDistance` | Unit | asymmetric distance hand case | farthest seed, 이후 nearest exact request sequence | reverse/symmetric sequence | hand route sequence exact |
| `InitialPortfolioPolicyTest.seqLargeDemandUsesBoundUtilizationThenDeadlineThenId` | Unit | utilization ties + deadline | exact seed tie chain | weight/volume double 또는 ID-first 결과 | fake bound compare call trace+route exact |
| `InitialPortfolioPolicyTest.seqEarliestDeadlineUsesDeadlineThenUtilizationThenId` | Unit | equal deadline cases | exact seed tie chain | utilization/deadline 우선순위 역전 | exact route trace |
| `InitialPortfolioPolicyTest.directFirstLargeUsesDirectThenLowUtilization` | Unit | DIRECT/LEASE mixed | all feasible DIRECT before LEASE, low utilization first | LEASE 또는 high utilization first | vehicle attempt trace exact |
| `InitialPortfolioPolicyTest.directFirstSmallUsesDirectThenHighUtilization` | Unit | DIRECT/LEASE mixed | all feasible DIRECT before LEASE, high utilization first | LEASE 또는 low utilization first | vehicle attempt trace exact |
| `InitialPortfolioPolicyTest.breaksFinalTiesByRequestVehicleRouteAndPositionIdentity` | Unit/property; Master §11.1/§15.5 | `PORTFOLIO_EQUAL_KEYS`, equal exact objective | request→vehicle→route→pickup index→delivery index의 exact tuple | collection permutation별 선택 move가 달라짐 | 모든 permutation에서 같은 selected tuple/trace |
| `InitialPortfolioPolicyTest.neverInfersCapacityFromFeatureText` | Negative/architecture | `PORTFOLIO_FEATURE_TRAP` | bound utilization order wins | `"10t"`가 parsed/lexically selected | fake bound order exact, forbidden rg 0 |
| `InitialPortfolioPolicyTest.isIndependentOfInputCollectionPermutation` | Property/reproducibility | `PORTFOLIO_EQUAL_KEYS` permutations | same 8 traces/fingerprints | permutation별 difference | all permutations exact equality |

### 9.4 Isolation, artifact와 architecture

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `PortfolioCandidateIsolationTest.candidatesShareNoMutableRouteBankOrWorkspace` | Property | `PORTFOLIO_ALL_AVAILABLE` | immutable values; one derived operation cannot affect sibling | sibling route/bank/fingerprint changes | all sibling before/after equal |
| `PortfolioCandidateIsolationTest.failedInsertionLeavesNoObservableMutation` | Fault/property; implementation plan §9.4 | `PORTFOLIO_FAULT_AT_K` | failed draft absent; base/sibling §7.6 equality | `route/bank/cache/fingerprint changed after failed insertion` | every observed component equal |
| `PortfolioCandidateIsolationTest.hasNoRandomOrClockDependentState` | Architecture/reproducibility | full portfolio repeated | no RNG/Clock call, same trace/fingerprint | forbidden call spy/rg 또는 rerun diff | call 0, exact rerun |
| `InitialPortfolioArtifactTest.recordsSourcePolicyConfigAndOrderedRoutes` | Unit | all available | required lineage non-null/exact | missing policy/config/route/fingerprint field | hand expected artifact tuple |
| `InitialPortfolioArtifactTest.fingerprintExcludesCacheElapsedAndObjectIdentity` | Property | same semantics, different cache/scratch/object allocation | same artifact fingerprint | fingerprints differ | exact equality |
| `InitialPortfolioArtifactTest.changesFingerprintForRouteBankOrLineageChange` | Property | one-field mutations | each semantic mutation changes digest | collision/equal fingerprint | all mutations unequal |
| `PortfolioDependencyArchitectureTest.keepsCoreAndSolverProviderFree` | Architecture; Architecture §7/§20 | bytecode/source scan | solver→core only; cloud/HTTP/customer/profile impl 0 | forbidden dependency 목록에 class 등장 | rule pass |
| `PortfolioDependencyArchitectureTest.keepsInsertionFreeOfSolverMutation` | Architecture | package dependency scan | `evaluation.insertion`→`solver.*` reference 0 | offending class/edge printed | rule pass |

## 10. 테스트 우선 구현 순서

아래 순서를 바꾸지 않는다. Production 구현 파일을 먼저 만들고 test를 나중에 맞추는 것은 AR-3 evidence로 인정하지 않는다.

### 10.1 Red 0 — 선행 gate

1. Source hash와 `git status`를 기록한다.
2. AR-0~2 phase 문서/status/evidence bundle/digest를 확인한다.
3. `mvn -pl rpdptw/core,rpdptw/solver -am verify`가 baseline green인지 확인한다.
4. §6.2 ordering seam과 service-pattern placement 표현을 실제 handoff와 대조한다.
5. 하나라도 없으면 production/test를 만들지 않고 `BLOCKED` evidence를 작성한다.

### 10.2 Red 1 — core API compile failure

1. `AtomicPairInsertionFixtures.java`, `AtomicPairInsertionEvaluatorTest.java`를 production보다 먼저 만든다.
2. Targeted Maven command를 실행한다.
3. 예상한 `cannot find symbol: class AtomicPairInsertionEvaluator` 또는 첫 제안 type의 동일 compile failure를 `red/`에 보존한다.
4. Dependency/environment/unrelated failure면 올바른 red가 아니므로 원인을 먼저 고친다.
5. 이 compile red를 본 뒤에만 최소 signature/record skeleton을 추가한다.
6. Skeleton은 empty list/`UnsupportedOperationException("not implemented")`까지만 허용한다.
7. 다시 실행해 `expected: <N> but was: <0>` 또는 `not implemented`의 semantic red를 확인한다.
8. Semantic red를 보지 않은 채 evaluator behavior를 구현하지 않는다.

### 10.3 Green 1 — placement와 evaluation

1. Legal `PairInsertionQuery` index enumeration 최소 구현
2. Structural/servable/static gate
3. Full propagation/bound hard evaluation delegation
4. feasible/rejected partition
5. stable total order
6. core unit targeted green
7. property test를 test-first로 추가하고 각각 의도한 assertion failure를 확인
8. core module `test`, 그 뒤 `verify`

### 10.4 Red/Green 2 — bank와 atomic apply

1. `SearchRequestBankTest`, `AtomicPairMoveApplierTest`를 production보다 먼저 만든다.
2. Missing type compile failure를 확인한다.
3. Minimal immutable skeleton 뒤 semantic red를 확인한다.
4. Bank value semantics를 최소 구현한다.
5. Apply stale check → copy → cache-free recompute → equality → freeze 순으로 한 behavior씩 green한다.
6. Fault injection test를 먼저 추가하고 rollback mismatch red를 확인한 뒤 discard path를 구현한다.
7. Solver state targeted green을 실행한다.

### 10.5 Red/Green 3 — 8개 portfolio

1. `InitialPortfolioBuilderTest`와 exact 8 ID assertion을 먼저 만든다.
2. Missing builder compile red를 확인한다.
3. Minimal builder skeleton 후 `expected: <8> but was: <0>` red를 확인한다.
4. Enum/product/attempt container만 구현해 8 identity test를 green한다.
5. Non-CLOCK deterministic constructor를 하나씩 test-first로 추가한다.
6. 두 vehicle order를 test-first로 추가한다.
7. `CLOCK` available behavior를 구현하기 전 bound-order-only test red를 확인한다.
8. `CLOCK UNAVAILABLE` test red를 확인한 뒤 typed unavailable/continue를 구현한다.
9. Combination 전체 test를 green한다.

### 10.6 Red/Green 4 — isolation, artifact, architecture

1. Failure injection과 sibling isolation test를 먼저 작성한다.
2. 의도한 route/bank/fingerprint mismatch 또는 shared workspace 관찰을 red로 저장한다.
3. Combination-local workspace와 whole-draft discard를 최소 구현한다.
4. Artifact required-field/fingerprint tests를 먼저 red로 만든다.
5. Canonical lineage/fingerprint를 최소 구현한다.
6. Architecture rules를 먼저 red로 추가하고 offending dependency/package가 정확히 출력되는지 확인한다.
7. 금지 edge를 제거해 green한다.

### 10.7 Green gates

각 behavior는 다음 순서를 모두 거친다.

```text
failing test 작성
→ 의도한 compile/assertion/message failure 확인
→ 최소 production 구현
→ targeted green
→ core/solver module test
→ core/solver module verify
→ reactor verify
→ rg/architecture/diff/evidence check
```

AR-3 production 구현이 끝났더라도 reactor regression과 evidence bundle이 없으면 `DONE`이 아니다.

## 11. Exact Maven/rg/git 명령

모든 명령의 working directory는 `/Users/brown/workspace/ro-next`다.

### 11.1 Red commands

```bash
cd /Users/brown/workspace/ro-next
mvn -pl rpdptw/core -am -Dtest=AtomicPairInsertionEvaluatorTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=SearchRequestBankTest,AtomicPairMoveApplierTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=InitialPortfolioBuilderTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=PortfolioCandidateIsolationTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl build/architecture-rules -am -Dtest=PortfolioDependencyArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
```

각 command는 대상 owner module의 Surefire report가 실제로 생겼는지 확인한다.

```bash
find rpdptw/core/target/surefire-reports rpdptw/solver/target/surefire-reports build/architecture-rules/target/surefire-reports -type f -maxdepth 1 -print | sort
rg -n 'tests=\"[1-9]|failures=\"[1-9]|errors=\"[1-9]' rpdptw/core/target/surefire-reports rpdptw/solver/target/surefire-reports build/architecture-rules/target/surefire-reports
```

### 11.2 Targeted green

```bash
mvn -pl rpdptw/core -am -Dtest=AtomicPairInsertionEvaluatorTest,AtomicPairInsertionEvaluatorPropertiesTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=SearchRequestBankTest,AtomicPairMoveApplierTest,InitialPortfolioBuilderTest,InitialPortfolioPolicyTest,PortfolioCandidateIsolationTest,InitialPortfolioArtifactTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl build/architecture-rules -am -Dtest=PortfolioDependencyArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
```

### 11.3 Module verify와 reactor regression

```bash
mvn -pl rpdptw/core -am verify
mvn -pl rpdptw/solver -am verify
mvn -pl rpdptw/core,rpdptw/solver,build/architecture-rules -am verify
mvn verify
```

위 Maven 결과는 다른 세션이 같은 `target/`을 쓰지 않는 격리 실행에서만 authoritative하다. 동시 실행 중 shade JAR replace, report overwrite 또는 artifact rename failure는 `CONCURRENT_TARGET_COLLISION_SUSPECTED`로 기록하고 baseline/blocker에 넣지 않으며, 격리 재실행 결과로만 판정한다.

`-DskipTests`, `-Dmaven.test.skip`, test name typo, fail-if-no-test 완화로 green을 만들지 않는다. `-Dsurefire.failIfNoSpecifiedTests=false`는 `-am` 선행 module에 같은 이름 test가 없는 경우만 위한 것이며 target owner test 실행 여부를 report로 확인한다.

### 11.4 Forbidden dependency/value/search checks

```bash
rg -n 'com\.google\.cloud|software\.amazon|aws\.sdk|azure|HttpClient|HttpServer|StorageOptions|ExecutionsClient' rpdptw/core/src rpdptw/solver/src
rg -n 'customer(Id|Name)|switch\s*\([^)]*customer|latest|parallelRuns|iterationsPerRun|screenMaxSteps|phase2MaxSteps|maxRounds|watchdog' rpdptw/core/src rpdptw/solver/src
rg -n 'RandomGenerator|SplittableRandom|Math\.random|System\.nanoTime|currentTimeMillis|Instant\.now|Clock\.system' rpdptw/core/src/main rpdptw/solver/src/main
rg -n 'Feature.*(parse|substring)|parse(Int|Long|Double).*Feature|Double|float|double' rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio
rg -n 'coordinate|latitude|longitude|atan2|bearing|great.?circle' rpdptw/solver/src/main/java/com/ronext/rpdptw/solver
rg -n 'OUTSOURCED|DEFERRED|diagnostic|last.*failure|cost' rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/SearchRequestBank.java
rg -n 'com\.ronext\.rpdptw\.solver' rpdptw/core/src/main
```

각 명령의 허용 결과를 evidence에 설명한다.

- Cloud/provider/core→solver 검색은 0건이어야 한다.
- `screenMaxSteps` 등은 test 설명이나 금지 assertion 외 production 0건이어야 한다.
- RNG/clock production 0건이어야 한다.
- Coordinate math는 solver production 0건이어야 한다.
- `Double/float/double`은 portfolio ordering production 0건이어야 한다.
- Bank 금지 의미 production 0건이어야 한다.

### 11.5 Diff, scope, link와 source drift

```bash
git status --short
git diff --check
git diff --name-status
git diff -- rpdptw/core rpdptw/solver build/architecture-rules
git status --short -- rpdptw/core rpdptw/solver build/architecture-rules docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
rg -n '^#{1,6} ' docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
rg -n '\[[^]]+\]\([^)]+' docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
```

Implementation diff의 허용 production/test 범위는 §5 표뿐이다. 다른 파일이 이미 dirty이면 그것을 고치거나 restore하지 않는다. 자기 변경만 별도 commit 또는 patch로 식별한다.

## 12. Step-by-step implementation checklist

### 12.1 Entry와 baseline

- [ ] `git status --short` 전체를 evidence에 보존했다.
- [ ] Source design 5개 hash가 YAML baseline과 같다.
- [ ] AR-0, AR-1, AR-2가 실제 `DONE`이고 evidence digest가 있다.
- [ ] AR-1의 pair/service-pattern/terminal placement 표현을 확인했다.
- [ ] AR-2의 propagation/evaluation/comparator와 `ConstructionOrderingPolicy`가 immutable/fingerprinted다.
- [ ] Utilization missing/zero와 CLOCK key 의미가 bound version에 있고 solver fallback이 필요 없다.
- [ ] Baseline reactor `mvn verify`가 green이다.

### 12.2 Test-first evaluator

- [ ] Core test/fixture 파일을 production보다 먼저 만들었다.
- [ ] Missing API compile failure를 보존했다.
- [ ] Minimal skeleton 뒤 semantic assertion failure를 보존했다.
- [ ] Real pair legal query index 전량 enumeration을 구현했다.
- [ ] Delivery-only logical prefix query가 travel/stop/service를 만들지 않는다.
- [ ] Feasible/rejected partition이 loss/duplicate 없이 legal query를 덮는다.
- [ ] Infeasible는 ranking에서 제외되고 structural defect는 fail-fast다.
- [ ] Prepared directed travel과 bound profile만 사용한다.
- [ ] Stable tie와 input non-mutation property가 green이다.

### 12.3 Test-first state/apply

- [ ] Bank/apply test를 production보다 먼저 만들고 red를 보존했다.
- [ ] Bank는 request membership만 보유하고 immutable하다.
- [ ] Option은 base route fingerprint에 bind된다.
- [ ] Apply는 새 route/bank value를 만든다.
- [ ] Full recomputation equality 뒤에만 commit한다.
- [ ] Stale/mismatch/exception은 base를 바꾸지 않는다.
- [ ] Pair/bank XOR와 terminal/route invariant를 commit 전에 검증한다.

### 12.4 Test-first portfolio

- [ ] Exact 8 combination test를 production보다 먼저 만들고 red를 보존했다.
- [ ] Attempts의 fixed Cartesian order가 green이다.
- [ ] 각 combination이 독립 workspace를 갖는다.
- [ ] 4 request-route policy가 evaluator만 사용한다.
- [ ] 두 vehicle order 모두 DIRECT를 LEASE보다 먼저 둔다.
- [ ] Large/Small은 bound utilization comparator만 사용한다.
- [ ] CLOCK은 bound order만 사용하고 missing 시 해당 attempt를 폐기한다.
- [ ] CLOCK unavailable 뒤 non-CLOCK 6개가 계속 생성된다.
- [ ] Unassignable request는 bank에 남고 fake vehicle/resource를 만들지 않는다.
- [ ] Collection permutation과 rerun이 동일 trace/fingerprint다.

### 12.5 Artifact, regression와 evidence

- [ ] Ordered route/bank/source policy/config/identity가 artifact에 있다.
- [ ] Artifact fingerprint가 cache/elapsed/object identity를 제외한다.
- [ ] Semantic route/bank/lineage 변경은 fingerprint를 바꾼다.
- [ ] Core targeted test와 module verify가 green이다.
- [ ] Solver targeted test와 module verify가 green이다.
- [ ] Architecture test와 full reactor verify가 green이다.
- [ ] Forbidden dependency/value searches가 허용 결과만 낸다.
- [ ] `git diff --check`가 green이고 change scope가 §5뿐이다.
- [ ] Evidence bundle과 `handoff.md`가 완결됐다.

## 13. Deliverables와 evidence bundle

### 13.1 Deliverables

| Deliverable | 형태 | 다음 consumer |
|---|---|---|
| Atomic pair option contract | Core API와 full-recompute result | AR-4 repair/move, AR-5 insertion audit |
| Immutable search baseline | `solver.state.CommittedCandidate`, `CommittedRoute`, `SearchRequestBank` | AR-4 COW current/stageBest/solveBest |
| Initial portfolio | 8 attempts, 최대 8 available candidates | AR-4 phase-1 screen |
| Unavailability record | typed CLOCK reason + missing location IDs | AR-4 available-candidate completeness |
| Ordered route artifact | policy/config/order/metrics/fingerprint | AR-4 warm-start lineage, AR-5 candidate input |
| Atomic apply/discard boundary | full equality와 rollback result | AR-4 mutation fault handling |
| Test builders/oracles | small immutable Java fixtures | AR-4/AR-5 regression |

AR-4가 소비할 것은 available `com.ronext.rpdptw.solver.state.CommittedCandidate`와 artifact다. `PortfolioAttempt.Unavailable`은 candidate가 아니며 screen assignment를 만들면 안 된다. AR-3 output에 phase-1 champion은 없다.

### 13.2 Evidence bundle

Implementation run은 다음을 생성한다.

```text
target/codex-evidence/AR-3/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── core-api-compile-failure.txt
│   ├── core-semantic-failure.txt
│   ├── state-apply-failure.txt
│   ├── portfolio-eight-failure.txt
│   ├── clock-unavailable-failure.txt
│   └── isolation-architecture-failure.txt
├── green/
│   ├── core-targeted.txt
│   ├── state-targeted.txt
│   └── portfolio-targeted.txt
├── regression/
│   ├── core-verify.txt
│   ├── solver-verify.txt
│   ├── architecture-verify.txt
│   └── reactor-verify.txt
├── fingerprints/
│   ├── source-baseline.sha256
│   ├── candidate-fingerprints.txt
│   ├── route-artifact-fingerprints.txt
│   └── portfolio-fingerprint.txt
├── faults/
│   ├── stale-option.txt
│   ├── recomputation-mismatch.txt
│   ├── propagation-exception.txt
│   └── failed-insertion-rollback.txt
├── reproducibility/
│   ├── permutation-equality.txt
│   └── portfolio-rerun.diff
├── diff/
│   ├── git-status.txt
│   ├── changed-files.txt
│   ├── git-diff-check.txt
│   ├── dependency-tree.txt
│   └── forbidden-searches.txt
└── handoff.md
```

`evidence.json`은 전체 계획 §11.1 필드에 더해 다음을 가진다.

```text
availableCombinationIds
unavailableCombinationIdsAndReasons
problemFingerprint
preparedTravelFingerprint
boundProfileFingerprint
constructionOrderingFingerprint
portfolioAlgorithmVersion
portfolioFingerprint
candidateAndRouteArtifactFingerprints
rollbackFaultCaseCount
permutationCaseCount
```

Evidence bundle은 `target/` 아래 CI artifact로 보존하되 source control에 넣지 않는다.

## 14. Rollback 계획

AR-3에는 schema/public endpoint/logical cutover/state migration이 없다. Rollback 단위는 AR-3 전용 commit 또는 exact patch 하나다.

1. 구현 전에 dirty status와 AR-0~2 tree/commit/evidence digest를 기록한다.
2. AR-3 파일만 stage하고 `git diff --cached --name-status`로 §5와 대조한다.
3. 기존 사용자/다른 세션 변경은 stage, restore, stash, reformat하지 않는다.
4. Rollback은 가능하면 AR-3 commit을 새 revert commit으로 되돌린다.
5. Commit 전 rollback이 필요하면 자기 세션이 만든 exact §5 파일과 `BoundProfile.java`의 자기 hunk만 역패치한다.
6. `git reset --hard`, broad `git clean`, module 전체 checkout/restore를 사용하지 않는다.
7. `BoundProfile` ordering dependency를 되돌릴 때 profile fingerprint/dependency closure test도 같은 단위로 되돌린다.
8. Evidence bundle은 삭제하지 말고 rollback reason, original digest와 rollback commit/patch identity를 기록한다.

Rollback 뒤 AR-0~2 reactor verify가 원래 baseline과 같아야 한다. Legacy root/gcp/data에는 rollback 동작이 없어야 한다.

## 15. DONE/BLOCKED 판정

### 15.1 `DONE` AND gate

다음을 모두 만족해야 AR-3를 `DONE`으로 판정한다.

1. AR-0~2가 `DONE`이고 handoff artifact/digest가 실제로 소비됐다.
2. §5 production/test deliverable이 repository에 존재한다.
3. 모든 요구 behavior에 의도한 red와 targeted green evidence가 있다.
4. Feasible option에 partial/hard-infeasible pair가 0건이다.
5. 모든 selected move가 cache-free applied route evaluation과 exact equality다.
6. 모든 stable candidate가 pair/route-bank XOR와 route invariant를 만족한다.
7. Complete ordering fixture에서 8개 stable combination candidate가 생성된다.
8. CLOCK coordinate unavailable fixture에서 CLOCK 2개만 unavailable이고 non-CLOCK 6개가 생성된다.
9. Failed/rejected/stale/exception move에서 §7.6 관찰값 변화가 0건이다.
10. Candidate 간 mutable route/bank/workspace 공유가 0건이다.
11. Artifact가 source policy/config/ordered route/bank/evaluation/fingerprint lineage를 가진다.
12. Targeted test, core verify, solver verify, architecture verify, reactor verify가 모두 통과한다.
13. Cloud/provider/customer-name/raw coordinate/hidden number/RNG/clock/Feature parsing 금지 검색 위반이 0건이다.
14. Evidence bundle digest와 재현 가능한 handoff가 있다.
15. AR-4 consumer test가 available candidate와 unavailable record를 실제로 읽을 수 있다.

단순히 class/test 1개가 존재하거나 8개 enum을 반환하는 것은 `DONE`이 아니다.

### 15.2 `BLOCKED` 조건과 재개 조건

| Blocker | 판정 | 마지막 안전 지점 | 재개 조건 |
|---|---|---|---|
| AR-0/1/2 미완료 또는 evidence 없음 | `BLOCKED` | 문서/fixture 설계만 | 선행 phase `DONE`+digest |
| Pair/service-pattern placement 표현 충돌 | `BLOCKED` | Production test 작성 전 | AR-1 계약/ADR와 이 문서 동시 갱신 |
| Utilization missing/zero unresolved | `BLOCKED` | Vehicle-order 구현 전 | Bound policy version+test+fingerprint |
| CLOCK order key/bearing 의미 unresolved | `BLOCKED` | CLOCK 구현 전 | Bound ordering seam+availability evidence |
| Evaluator와 full recomputation이 다름 | `BLOCKED`, defect | 마지막 green commit | AR-2 propagation/evaluation 원인 수정과 regression |
| Module dependency가 Architecture DAG를 요구대로 지킬 수 없음 | `BLOCKED` | POM 임시 변경 금지 | Architecture ADR/상위 계획 갱신 |
| User change와 같은 hunk overlap | `BLOCKED` | 해당 파일 미수정 | owner 조정 또는 새 approved patch boundary |

다음 상황은 `BLOCKED`로 숨기지 말고 명시적으로 실패시킨다.

- Partial pair를 bank에 되돌렸다고 주장
- Infeasible option을 low score로 ranking
- CLOCK을 다른 policy로 fallback
- Missing utilization을 0/∞/임의 capacity로 처리
- Stale option/recomputation mismatch를 skip하고 계속

## 16. 다음 phase handoff

### 16.1 AR-4에 전달

`handoff.md`는 다음 exact identity를 기록한다.

- AR-0~2 evidence ID/digest
- problem/prepared travel/bound profile/construction ordering fingerprints
- portfolio algorithm version
- 8 combination attempt status와 stable IDs
- available `com.ronext.rpdptw.solver.state.CommittedCandidate` IDs/fingerprints
- unavailable CLOCK missing location IDs
- 각 candidate의 ordered route/bank artifact fingerprint
- evaluator/atomic applier version
- deterministic tie order
- rollback/fault/property test counts
- known risk와 open blocker 0건 확인

AR-4 entry는 available candidate 각각을 독립 phase-1 screen에 배정할 수 있어야 한다. AR-4는 AR-3 candidate를 mutable warm start로 공유하지 않고 각 run의 COW current로 복제/freeze해야 한다.

### 16.2 AR-5에 전달

AR-5가 나중에 소비할 수 있도록 다음 의미를 보존한다.

- Problem/profile/prepared travel identity
- Candidate ordered route/node sequence
- Candidate `SearchRequestBank`
- Cache-free evaluation lineage

AR-5 verifier는 AR-3 cache, feasibility flag, artifact metric을 권위로 신뢰하지 않는다. AR-3가 `validated`라고 기록한 candidate도 AR-5 `PASS` 전에는 `VerifiedSolution`이 아니다.

### 16.3 Known risks

- 선행 AR-1/2 type 이름과 이 문서 제안 이름이 다를 가능성
- Delivery-only logical pickup의 실제 내부 표현
- Bound construction ordering이 AR-2 scope에 아직 포함되지 않았을 가능성
- Artifact canonical encoding이 AR-6/ADR 전까지 Java value/fingerprint 수준이라는 점

이 위험은 duplicate API, raw fallback 또는 JSON 임시 schema로 해결하지 않는다.

## 17. Scope exclusions와 금지 shortcut

### 17.1 명시적 비범위

- Phase-1 per-candidate ALNS screen과 champion selection
- `screenMaxSteps`, `phase2MaxSteps`, worker 수, `maxRounds`, watchdog 값
- Destroy/repair/local search/acceptance/adaptive weight
- AR-4 current/stageBest/solveBest와 termination
- AR-5 candidate/result verifier, finalization, audit, publication
- AR-6 logical ports, JSON/local adapter, artifact storage
- AR-7 multi-round coordinator
- AR-8 legacy cutover
- AR-9 official Win run
- AR-10 profiling/apply-undo decision
- Physical infrastructure/provider/product/deployment
- Optional variants, multi-trip/rotation, route pool/MIP

### 17.2 금지 shortcut

- Test red를 보지 않고 production behavior 구현
- Policy마다 capacity/time/zone/capability feasibility 복제
- Partial pickup 또는 delivery를 임시 stable state로 노출
- Infeasible option을 score/penalty로 ranking
- `SearchRequestBank`에 node/cost/failure/final status/diagnostic 저장
- `CLOCK` 좌표 부재 시 farthest/nearest/ID fallback
- Solver에서 raw coordinate, speed, reverse arc 또는 symmetric distance 사용
- `Feature` 문자열 숫자를 vehicle size/capacity로 parse
- Utilization missing/zero를 숨은 0, ∞, sentinel 또는 `double` tolerance로 처리
- README의 8/5000, current Workflow timeout/retry를 official/default config로 사용
- Global random, time seed, unordered collection first-winner
- Candidate/artifact fingerprint에 elapsed/thread/provider locator 포함
- Apply/undo skeleton 또는 mutation journal 선반영
- Cache를 source of truth로 사용하거나 equality check 생략
- AR-3 validation을 AR-5 verifier `PASS`로 표현
- Current Win fixture의 decimal `D/U`를 round/truncate해 test baseline으로 사용
- Cloud/HTTP/provider/customer-name dependency를 core/solver에 추가
- 다른 phase 문서, 설계 원문, progress, legacy source, POM/gcp/data 변경

## 18. 문서 작성 완료 검증 기준

이 phase 문서 자체는 다음을 만족해야 한다.

- YAML에 phase/RM/status/source hashes/선행 문서가 있다.
- §1~§17에 authority, blocker, gap, exact paths, proposed API/signature/error/invariant, detailed tests, strict TDD order, exact commands, checklist, deliverables/evidence, rollback, DONE/BLOCKED, handoff, exclusions가 있다.
- Test path와 production path가 분리되어 있고 test-first 순서가 명시되어 있다.
- `Q-BENCH-02`, provider/product, optional variant, Win fixture 변환을 추측하지 않는다.
- 물리 infrastructure를 logical/core 범위에 넣지 않는다.
- Source design hash drift가 없고 Markdown link/heading을 검사한다.
- 이 문서 작성 세션이 변경한 파일은 이 파일 하나뿐이다.

## 19. 전체 구현 계획 §10의 13개 계약 대응표

| §10 항목 | 이 문서의 충족 위치 | 완료 판단 |
|---:|---|---|
| 1. 실제 저장소 조사 | §2 | POM/source/test/README/GCP/data와 dirty tree를 관찰하고 legacy와 target을 구분 |
| 2. Authority와 blocker | §1 | 문서 우선순위, 질문 상태, 선행 phase gate와 non-blocker 명시 |
| 3. 현 상태→목표 gap | §3 | 영역별 current/target/non-goal 표 |
| 4. 정확한 경로 | §5 | test 신규를 먼저, production 변경/신규를 뒤에, 이동·삭제 금지 경로까지 명시 |
| 5. Package/type/API | §6~§7 | 계획상 제안임을 표시하고 signature, error, 불변조건, 알고리즘 순서를 고정 |
| 6. 세분화된 test | §8~§9 | `class.method`, fixture, expected, 첫 compile/assertion/message failure와 green 기준 |
| 7. 테스트 우선 순서 | §10 | red→실패 확인→최소 구현→target green→module verify→reactor regression 강제 |
| 8. Exact 명령 | §11 | Maven/rg/git/link/hash 명령과 허용 결과 |
| 9. Deliverables/evidence | §13 | 다음 consumer별 산출물과 evidence tree/필드 |
| 10. Rollback | §14 | AR-3 exact patch/commit 단위와 사용자 변경 보존 |
| 11. DONE/BLOCKED | §15 | AND gate, blocker, 마지막 안전 지점, 재개 조건 |
| 12. 다음 phase handoff | §16 | AR-4/AR-5 consumer identity와 lineage |
| 13. Scope exclusion/shortcut | §17 | deferred 범위와 금지 구현 방식 |
