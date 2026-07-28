---
phase: AR-10
rm_mapping: RM-7
title: COW profiling과 state-strategy decision gate
status: BLOCKED
document_status: REVIEW
language: ko
authoring_baseline:
  captured_at: 2026-07-24
  git_head: 523c23e2e13410885b16e974efe40ffe598106ee
  git_branch: codex/domain-design
  working_tree: dirty
  note: source 설계와 docs/codex/implementation-plan.md는 작성 시점 working tree의 사용자 소유 변경이며 HEAD 내용으로 대체하지 않는다.
source_baseline:
  implementation_plan:
    path: docs/codex/implementation-plan.md
    status: REVIEW
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
predecessor_documents:
  required:
    - path: docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
      phase: AR-4
      rm_mapping: RM-4
      state_at_authoring: NOT_PRESENT
    - path: docs/codex/phases/phase-05-verification-finalization-and-publication.md
      phase: AR-5
      rm_mapping: RM-5
      state_at_authoring: NOT_PRESENT
  optional:
    - path: docs/codex/phases/phase-09-official-win-poc-workflow.md
      phase: AR-9
      rm_mapping: RM-6-official
      state_at_authoring: NOT_PRESENT
entry_gate_at_authoring: BLOCKED_ON_PREDECESSOR_EVIDENCE
default_decision: KEEP_COW
shared_build_baseline:
  source: AR-0 isolated execution observation supplied by the coordinating session
  java: 25.0.3 Amazon Corretto
  maven: 3.9.14
  existing_test: PASS
  existing_verify: PASS
  concurrency_rule: shared target directory에서 동시 Maven 실행 중 발생한 transient artifact/shade replace 실패는 격리 재실행 전 baseline defect나 phase blocker로 판정하지 않는다.
---

# AR-10 / RM-7 — COW profiling과 state-strategy decision gate

## 1. 문서 목적과 phase 판정

이 문서는 정확한 changed-route copy-on-write(COW) baseline의 비용을 **전체 completed ALNS step 비용 안에서** 재현 가능하게 측정하고, 결과를 다음 두 값 중 하나로 끝내기 위한 실행 명세다.

1. `KEEP_COW`: 현재 기본 state strategy를 유지한다.
2. `PROPOSE_SEPARATE_EXPERIMENT`: 측정된 COW 병목과 독립 검토 근거를 첨부해 별도 apply/undo 실험·ADR·roadmap 승인을 요청한다.

두 번째 값도 apply/undo 구현 또는 전환 승인이 아니다. 이 phase가 반환할 수 있는 값에 `SWITCH_TO_APPLY_UNDO`는 존재하지 않는다. 성능 이득이 불충분하거나 판단 기준이 승인되지 않았거나 정확성·재현성·관측 가능성 동등성 중 하나라도 실패하면 `KEEP_COW`가 정상적인 `DONE` 결과다.

작성 시점 저장소에는 `AR-4`, `AR-5`의 실제 구현/DONE evidence와 target multi-module source가 없다. 따라서 문서 작성·검토 상태는 별도 metadata `document_status: REVIEW`로 유지하고, `AR-10` phase 구현 상태는 `BLOCKED`, 구현 entry gate는 `BLOCKED_ON_PREDECESSOR_EVIDENCE`로 판정한다. 이는 코드 구현을 시작했다는 뜻이 아니다. 현재 `NOT_STARTED`인 phase는 `AR-0`뿐이며, `AR-10`은 필수 선행 evidence가 해소될 때까지 `BLOCKED`다. 이 판정은 `AR-10` 설계가 불가능하다는 뜻이 아니라, 후속 구현 세션이 legacy placeholder 위에 profiler를 붙여 완료를 가장하지 못하게 하는 안전 gate다.

## 2. Authority, 결정 상태와 충돌 처리

### 2.1 규범 authority

이 phase가 직접 소비하는 근거는 다음과 같다.

| Authority | 적용 계약 |
|---|---|
| [Master Design §12](../../master-design.md#12-candidate-state-cache와-rollback) | Changed-route COW, independent bank, immutable current/best, reject/fail/interrupt 전체 discard, cache는 derived state |
| [Master Design §15.9](../../master-design.md#159-rm-7--cow-profiling과-선택적-state-strategy-재검토) | Route copy/allocation/GC/전체 search cost profiling, COW 유지 또는 별도 제안, 자동 threshold 금지 |
| [Master Design §13](../../master-design.md#13-termination-reproducibility와-execution-provenance) | Completed step, 정상 종료, strong reproducibility envelope, elapsed는 semantic input이 아님 |
| [Master Design §14.1](../../master-design.md#141-publication-gate) | Candidate/result 두 verifier의 독립 `PASS`가 publishable result의 전제 |
| [Domain Design §10.4~§10.5](../../domain-design.md#104-candidate-state) | COW candidate와 cache source-of-truth/derived-state 경계 |
| [Domain Design §16~§17](../../domain-design.md#16-acceptance-evidence) | COW isolation/cache-free equality/reproducibility evidence와 deferred physical/variant 경계 |
| [Architecture Design §17~§21](../../architecture-design.md#17-observability-retry-failure와-security-boundary) | Resource/GC/cache 관측, test evidence, `AR-10` gate, anti-pattern과 ADR backlog |
| [전체 구현 계획 §9.11](../implementation-plan.md#911-ar-10--rm-7--cow-profiling-decision) | AR-10의 entry, exact scope, first failing tests, deliverable와 종료 조건 |
| [전체 구현 계획 §10](../implementation-plan.md#10-phase-문서-작성-계약) | 이 phase 문서의 13개 필수 작성 항목 |
| [질문 등록부 `Q-ALG-02`](../../master-design-open-questions.md#q-alg-02) | `RESOLVED — KEEP_COW`; 별도 evidence/승인 전 apply/undo가 기본 경로가 아님 |
| [질문 등록부 `Q-BENCH-02`](../../master-design-open-questions.md#q-bench-02) | Official solver step/worker/round/watchdog 수치는 `OPEN — EXPERIMENT_REQUIRED` |

### 2.2 현재 authority 해석

- Master, Architecture, Domain과 전체 구현 계획은 모두 `REVIEW`다. 이 문서의 package, type, method, Maven profile, report schema는 승인된 public API나 wire/storage schema가 아니다.
- 이 문서의 구체 이름은 후속 구현 세션이 임의로 다시 설계하지 않게 하는 **계획상 고정 제안**이다. 선행 `AR-4` 실제 이름과 충돌하면 구현자가 한쪽을 조용히 바꾸지 않는다. 같은 변경 단위에서 승인된 ADR, 이 문서와 영향 test/path 표를 갱신한 뒤 재개한다.
- `Q-ALG-02`의 현재 판정은 COW 유지다. Profiling은 그 결정을 자동으로 뒤집는 절차가 아니다.
- `Q-BENCH-02`는 official 품질 실행 수치만 차단한다. `AR-5`에서 검증된 explicit non-official execution manifest를 그대로 재사용하는 profiling은 가능하다. 그 manifest의 solver step 수를 새 값으로 바꾸거나 README의 `5000`, `parallelRuns=8`을 가져오면 안 된다.
- `Q-INFRA-01`과 `Q-VAR-01`은 `DEFERRED`다. 이 phase는 provider/product, deployment topology, optional variant, multi-trip/rotation을 선택하거나 활성화하지 않는다.

### 2.3 충돌과 blocker 처리

다음 상황은 추측으로 해소하지 않고 `BLOCKED` evidence를 남긴다.

| 상황 | 판정 | 재개 조건 |
|---|---|---|
| `AR-4`가 `DONE`이 아니거나 COW trace/reproducibility evidence가 없음 | `BLOCKED: AR4_COW_BASELINE_MISSING` | `AR-4` evidence ID/digest, exact COW state strategy와 normal rerun evidence |
| `AR-5` both-pass verified execution이 없음 | `BLOCKED: AR5_VERIFIED_PATH_MISSING` | Candidate/result verifier `PASS`, publishable result fingerprint와 immutable handoff |
| Representative case가 mutable, digest 불일치 또는 verifier 미통과 | `BLOCKED: REPRESENTATIVE_CASE_NOT_VERIFIED` | Content-addressed case와 both-pass report |
| Profiling on/off trace 또는 result fingerprint가 다름 | `BLOCKED: PROFILING_SEMANTIC_DRIFT` | 원인 수정 후 새 red→green/equality evidence |
| 공식 case라고 표시했지만 `Q-BENCH-02` approval/compliant fixture가 없음 | `BLOCKED: OFFICIAL_AUTHORITY_MISSING` | 승인된 calibration record와 compliant fixture digest |
| COW 병목 주장을 위해 임의 수치 threshold가 필요함 | `KEEP_COW`, 필요 시 decision authority 요청 | 별도 승인된 판단 기준; 이 phase에서 threshold 생성 금지 |
| Apply/undo code/skeleton이 이미 이 phase diff에 포함됨 | `BLOCKED: SCOPE_VIOLATION` | 해당 변경 제거 또는 별도 승인 roadmap으로 이동 |

## 3. 작성 시점 실제 저장소 inventory

### 3.1 Build와 module graph

작성 시점에는 repository-owned Maven POM이 root `pom.xml` 하나뿐이다.

```text
ro-next (jar; 단일 module)
├── Google Workflow Executions
├── Google Cloud Storage
├── Jackson
└── JUnit 5
```

- Java release는 25, Maven Enforcer 범위는 Java `[25,26)`, Maven `[3.9.14,)`다.
- 실제 확인 toolchain은 Maven `3.9.14`, Amazon Corretto Java `25.0.3`이다.
- Root POM은 `packaging=pom` aggregator가 아니고 `rpdptw/solver` module, `profiling` profile, JMH/JFR harness가 없다.
- Shade plugin은 `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer`를 main으로 하는 단일 fat JAR을 만든다.
- 공통 build baseline은 관리 세션이 전달한 `AR-0` 격리 실행 관찰을 사용한다. Java `25.0.3`/Maven `3.9.14`에서 기존 `mvn test`와 `mvn verify`가 성공했다.
- 여러 phase 문서 세션이 같은 working directory의 `target/`을 공유하며 Maven을 동시에 실행할 수 있다. 그때 관찰한 shade JAR replace, transient artifact 또는 report 충돌은 격리 재실행 전 repository baseline 결함이나 이 phase blocker로 기록하지 않는다.

작성 시작 시 `git status --short`의 축약 snapshot은 다음과 같았다. 모든 항목은 사용자/다른 세션 소유로 간주하며 이 문서는 어느 것도 복원·수정하지 않는다.

```text
 D HGS_CVRP_QA_정리.md
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
?? docs/master-design-sessions/...
?? docs/orgin/HGS_CVRP_QA_정리.md
```

### 3.2 Production/test source

현재 production Java는 6개뿐이다.

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

현재 test는 `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` 하나다. `AlnsBatchEngine`은 `SplittableRandom`과 `double objective`를 쓰는 deterministic placeholder이며 route, bank, changed-route copy, cache, propagation/evaluation, completed ALNS step, verifier 또는 canonical fingerprint를 구현하지 않는다. 이 placeholder의 allocation/GC를 재는 것은 `AR-10` evidence가 아니다.

### 3.3 README, GCP와 data

- Root `README.md`의 `parallelRuns=8`, `iterationsPerRun=5000`은 legacy 실행 예시이며 `Q-BENCH-02` 값이 아니다.
- `gcp/workflows/optimization.yaml`은 run 수만큼 HTTP worker를 호출한 뒤 일부 candidate prefix를 finalizer가 읽는 legacy flow다. Complete verified worker fan-in이나 COW cost attribution을 제공하지 않는다.
- `gcp/README.md`, `gcp/cloudbuild.yaml`, `Dockerfile`은 Cloud Run/Workflows/Cloud Storage를 전제한 legacy characterization 자료다. Physical profiling topology를 정하는 authority가 아니다.
- `data/win_poc_case.json`은 14,157,512 bytes, SHA-256 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이다. `D/U`에 소수 문자열이 있으므로 official integer meter/second 계약에 비준수다. 이 phase는 값을 반올림·절삭·재생성해 profiling fixture로 만들지 않는다.

### 3.4 Source hash drift 판정

작성 시점 Master/Architecture/Domain SHA-256은 전체 구현 계획 YAML의 baseline과 정확히 일치한다. 구현 계획 자체는 untracked working-tree 문서이므로 그 digest `d4450f...14a`를 이 문서 metadata에 별도 고정했다. 후속 구현 착수 시 이 digest 중 하나라도 다르면 §12의 preflight를 중단하고 semantic diff를 검토해야 한다.

## 4. 현 상태에서 목표 상태까지의 gap

| 영역 | 현 상태 | AR-10 목표 | 이 phase의 action |
|---|---|---|---|
| Search state | Placeholder map/objective; COW 없음 | `AR-4`의 correct changed-route COW | 새 solver를 만들지 않고 `AR-4 DONE`을 entry로 요구 |
| Profiling hook | 없음 | Run-scoped, primitive-only, read-only counter/timer | Solver main source에 계획상 제안 API와 최소 hook 추가 |
| Cost attribution | 없음 | Route copy, bank copy, cache invalidation/rebuild, propagation, evaluation, total step/other | 겹치지 않는 cost center와 raw snapshot/report |
| Allocation/GC | 측정 없음 | Full-step/route-copy allocation과 GC, stack 기반 attribution | JMH `gc` + JFR raw recording; 수동 byte 추정 금지 |
| Semantic equality | Placeholder unit test 1개 | Profiling OFF/ON의 trace, committed solution, both-pass final result 동일 | Solver unit/repro test + application Failsafe IT |
| Benchmark source | 없음 | 일반 build에서 분리된 `src/profiling/java`와 explicit `profiling` profile | Profile 비활성 시 JMH runtime leakage 0 |
| Representative case | 비준수 Win fixture만 존재 | `AR-5` handoff의 immutable both-pass execution | 변환하지 않고 exact artifact ref/digest 소비 |
| Evidence report | 없음 | Environment/scenario/raw metrics/JFR/decision digest bundle | `target/codex-evidence/AR-10/<evidence-id>/` 생성 |
| Decision | `Q-ALG-02 KEEP_COW` | `KEEP_COW` 또는 별도 experiment proposal | 자동 switch/threshold 없이 governance gate |

## 5. Entry gate와 representative case 선택

### 5.1 필수 entry

Production/test 변경을 시작하기 전에 다음을 모두 만족해야 한다.

1. `AR-4 / RM-4`가 `DONE`이고 exact COW state strategy, completed-step trace, fixed reproducibility envelope와 cache-free equality evidence가 있다.
2. `AR-5 / RM-5`가 `DONE`이고 candidate verifier와 result-integrity verifier가 모두 `PASS`한 publishable result path가 있다.
3. `AR-4`와 `AR-5` handoff가 동일 `ProblemInstance`, `PreparedTravel`, `BoundProfile`, build/runtime compatibility와 committed candidate lineage를 연결한다.
4. Solver의 normal termination이 `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED` 중 하나이며 interrupted/failure recovery를 대표 성능 case로 쓰지 않는다.
5. Target reactor에서 `mvn -pl rpdptw/solver -am verify`와 `mvn -pl rpdptw/application -am verify`가 변경 전 green이다.

공유 `target/`에서 다른 세션의 Maven 실행과 겹쳤을 가능성이 있는 실패는 이 조건을 곧바로 깨지 않는다. 같은 commit/tree에서 다른 Maven process가 없는 격리 상태로 한 번 재실행한 뒤에도 같은 원인으로 실패할 때만 baseline failure 또는 blocker 후보로 분류한다.

### 5.2 Case 선택 절차

Case 값과 변환을 추측하지 않기 위해 다음 절차를 고정한다.

1. `AR-5` `handoff.md`가 `representativeProfilingCandidate`로 지목한 evidence만 우선 사용한다.
2. 지목이 없으면 `AR-5` evidence 중 both-pass, normal termination, immutable artifact digest, fixed seed/config/step을 모두 가진 case를 고른다. 둘 이상이면 임의 quality winner를 만들지 말고 stable evidence ID lexical order의 첫 case를 사용하고 선택 이유를 report에 기록한다.
3. 원본 artifact를 수정·정규화·축소·샘플링하지 않는다. `AR-10` input manifest에는 source artifact ref와 digest만 복사한다.
4. `AR-9` approval가 없으면 `classification=REPRESENTATIVE_NON_OFFICIAL`로 고정한다. `Q-BENCH-02` 공식 수치나 official baseline을 주장하지 않는다.
5. `classification=OFFICIAL_APPROVED`는 `AR-9 DONE`, calibration approval ref, compliant fixture digest가 모두 있는 경우에만 허용한다.
6. 위 조건을 만족하는 case가 없으면 synthetic microbenchmark만 실행해 `DONE`을 주장하지 않고 `BLOCKED: REPRESENTATIVE_CASE_NOT_VERIFIED`로 끝낸다.

### 5.3 계획상 제안 profiling-case manifest

아래는 외부 public schema가 아니라 evidence bundle 내부의 계획상 제안 schema다.

```text
schemaVersion = "ar10-cow-profile-case/v1"
classification = REPRESENTATIVE_NON_OFFICIAL | OFFICIAL_APPROVED
sourceEvidenceId
sourceHandoffDigest
problemFingerprint
travelFingerprint
profileFingerprint
algorithmConfigFingerprint
buildRuntimeFingerprint
seedDerivationVersion
baseSeed
requestedAndCompletedSteps
normalTermination
warmStartCandidateFingerprint
expectedCanonicalTraceDigest
expectedCommittedSolutionFingerprint
candidateVerifierReportDigest
resultVerifierReportDigest
expectedPublishableResultFingerprint
officialApprovalRef?        # OFFICIAL_APPROVED에서만 필수
```

Manifest에 problem bytes, provider locator, secret, wall-clock timestamp를 semantic value로 복사하지 않는다. `sourceEvidenceId`가 가리키는 immutable artifact를 digest 검증 전에 deserialize하지 않는다.

## 6. Scope와 정확한 예상 경로

### 6.1 변경/신규 경로 표

아래 경로는 작성 시점에 존재하지 않는 target reactor를 기준으로 한 **계획상 고정 제안**이다.

| 상태 | Repository-relative path | 책임 | 먼저 실패해야 하는 test |
|---|---|---|---|
| 변경 | `pom.xml` | `jmh.version=1.37`, `build-helper-maven-plugin=3.6.1`, `exec-maven-plugin=3.6.3`을 parent dependency/plugin management에 pin. 일반 dependencies에는 JMH를 넣지 않음 | `ProfilingDependencyArchitectureTest.jmhExistsOnlyInProfilingProfile` |
| 변경 | `rpdptw/solver/pom.xml` | `profiling` profile, `src/profiling/java` source set, JMH annotation processor와 profile-only execution | `CowBenchmarkProfileContractTest.compilesAndDiscoversBenchmarkOnlyWithProfile` |
| 변경 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CowCandidateState.java` | First-write route/bank copy와 cache invalidation 지점에 primitive profiling hook 호출 | `CowProfilingScenarioTest.attributesCopyCacheAndCompletedStepCost` |
| 변경 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/AlnsStepExecutor.java` | Whole-step total, propagation/evaluation/cache rebuild exclusive timing; recorder 값은 search decision에 사용 금지 | `CowProfilingInstrumentationTest.doesNotChangeTraceOrSolutionFingerprint` |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowCostCenter.java` | Cost center stable order | API compile red |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowCounter.java` | Copy/cache/candidate/completed-step counters | API compile red |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowProfilingRecorder.java` | Read-only recorder interface | API compile red |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowProfilingRecorders.java` | Disabled singleton과 run-scoped collecting recorder factory | API compile red |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowProfilingSnapshot.java` | Immutable ordered counter/timing snapshot | `CowProfilingRecorderTest.snapshotsAreImmutableAndOrdered` |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowProfilingException.java` | Profiling-only typed configuration/overflow/overlap failure | `CowProfilingRecorderTest.rejectsNegativeOverflowAndOverlappingCost` |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowDecision.java` | `KEEP_COW`/`PROPOSE_SEPARATE_EXPERIMENT`만 허용 | `CowDecisionGateTest.hasNoSwitchDecision` |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling/CowDecisionGate.java` | Evidence completeness와 별도 review ref를 검사하는 governance gate | `CowDecisionGateTest.keepsCowWithoutReviewedBottleneckEvidence` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/profiling/CowProfilingInstrumentationTest.java` | OFF/ON semantic equivalence와 첫 compile/assertion red | 해당 test 자체 |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/profiling/CowProfilingRecorderTest.java` | Recorder value/error/invariant unit test | 해당 test 자체 |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/profiling/CowProfilingScenarioTest.java` | Whole-step attribution과 fail/cancel discard integration | 해당 test 자체 |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/profiling/CowDecisionGateTest.java` | Default KEEP_COW와 proposal-only gate | 해당 test 자체 |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/profiling/CowProfilingCaseManifestTest.java` | Both-pass/digest/classification/official guard | 해당 test 자체 |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/profiling/CowBenchmarkProfileContractTest.java` | Profile/source-set/discovery/output contract | 해당 test 자체 |
| 신규 | `rpdptw/solver/src/profiling/java/com/ronext/rpdptw/solver/profiling/CowStateStrategyBenchmark.java` | Route-copy micro와 representative full-step JMH benchmark | `CowBenchmarkProfileContractTest` |
| 신규 | `rpdptw/solver/src/profiling/java/com/ronext/rpdptw/solver/profiling/CowProfilingMain.java` | Required case/output args 검증, JMH `gc`/JFR 실행, raw report 조립 | `CowProfilingCaseManifestTest`와 profile contract test |
| 신규 | `rpdptw/solver/src/profiling/java/com/ronext/rpdptw/solver/profiling/CowProfilingReportWriter.java` | Stable JSON/Markdown report와 digest 생성 | `CowBenchmarkProfileContractTest.writesCompleteEvidenceIndex` |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/profiling/CowProfilingVerifiedPathIT.java` | OFF/ON 모두 two verifier `PASS`와 동일 publishable result fingerprint | 해당 IT |
| 신규 또는 변경 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/ProfilingDependencyArchitectureTest.java` | Normal runtime의 JMH/JFR harness leakage, provider/apply-undo 침투 차단 | 해당 architecture test |
| 이동/삭제 없음 | 해당 없음 | AR-10은 source/module/file을 이동·삭제하지 않는다. 선행 AR-4 owner file에는 observation hook만 최소 변경한다. | Scope diff 검사 |

`CowCandidateState.java`와 `AlnsStepExecutor.java`는 선행 `AR-4`가 같은 책임에 다른 계획상 이름을 승인했을 때만 실제 owner file로 일괄 치환할 수 있다. 단, 이 표·API 연결·test import·evidence report stack-frame mapping을 같은 change set에서 바꾸고 승인된 ADR을 남겨야 한다. 이름 충돌을 이유로 병렬 wrapper state나 두 번째 step executor를 만들지 않는다.

### 6.2 생성하지만 source control에 넣지 않는 경로

```text
target/codex-evidence/AR-10/cow-profile-v1/
├── evidence.json
├── commands.log
├── inputs/
│   ├── profile-case.json
│   └── source-evidence-digests.json
├── red/
├── green/
├── regression/
├── reproducibility/
│   ├── profiling-off.json
│   ├── profiling-on.json
│   └── semantic-diff.json
├── benchmark/
│   ├── jmh-result.json
│   ├── jmh-human.txt
│   ├── jfr/
│   ├── gc-summary.json
│   ├── attribution.json
│   ├── cow-profiling-report.json
│   └── cow-profiling-report.md
├── decision/
│   └── cow-decision.json
├── diff/
└── handoff.md
```

`target/` evidence를 git에 추가하지 않는다. CI artifact store가 bundle digest와 보존 위치를 progress record에 연결한다.

## 7. 계획상 제안 package/type/API

이 절의 Java 이름과 signature는 내부 구현 계획이며 승인된 외부 API가 아니다.

### 7.1 Cost center와 counter

```java
package com.ronext.rpdptw.solver.profiling;

public enum CowCostCenter {
    ROUTE_COPY,
    BANK_COPY,
    CACHE_INVALIDATION,
    CACHE_REBUILD,
    PROPAGATION,
    EVALUATION,
    STEP_TOTAL
}

public enum CowCounter {
    ROUTES_COPIED,
    ROUTE_ELEMENTS_COPIED,
    BANKS_COPIED,
    BANK_REQUESTS_COPIED,
    CACHE_ENTRIES_INVALIDATED,
    CACHE_ENTRIES_REBUILT,
    CANDIDATES_ACCEPTED,
    CANDIDATES_DISCARDED,
    COMPLETED_STEPS
}
```

`STEP_TOTAL`만 inclusive다. 나머지 elapsed center는 서로 겹치지 않게 계측한다. `OTHER_STEP_NANOS`는 report 작성 시 `STEP_TOTAL - Σ(exclusive center)`로 계산하고 음수면 report를 실패시킨다. Copy된 element 수는 물리 allocation bytes의 대리값이 아니다.

### 7.2 Recorder

```java
package com.ronext.rpdptw.solver.profiling;

public interface CowProfilingRecorder {
    boolean enabled();
    long start(CowCostCenter center);
    void stop(CowCostCenter center, long startedAtNanos);
    void add(CowCounter counter, long nonNegativeDelta);
    CowProfilingSnapshot snapshot();
}

public final class CowProfilingRecorders {
    public static CowProfilingRecorder disabled();
    public static CowProfilingRecorder collecting(java.util.function.LongSupplier monotonicNanoTime);
}

public record CowCostMeasurement(
        CowCostCenter center,
        long invocationCount,
        long elapsedNanos) {}

public record CowCounterMeasurement(
        CowCounter counter,
        long value) {}

public record CowProfilingSnapshot(
        String schemaVersion,
        java.util.List<CowCostMeasurement> costs,
        java.util.List<CowCounterMeasurement> counters) {}
```

불변조건:

- `disabled()`는 immutable singleton이고 모든 mutation method가 no-op이며 `snapshot()`은 모든 center/counter가 stable enum order의 zero인 값이다.
- `collecting(...)`은 worker run마다 새로 만들고 static mutable/global registry를 사용하지 않는다.
- Snapshot list는 enum declaration order이며 immutable copy다. `Map` iteration order를 canonical encoding에 사용하지 않는다.
- `nonNegativeDelta < 0`은 `NEGATIVE_DELTA`, checked-add overflow는 `COUNTER_OVERFLOW`, timer 역행은 `NON_MONOTONIC_CLOCK`, exclusive 합이 total보다 크면 report 단계 `OVERLAPPING_COST_CENTERS`다.
- Profiling recorder는 route, bank, cache, random, comparator, acceptance, termination 객체 참조를 받지 않는다. Primitive count와 monotonic elapsed만 받는다.
- Recorder snapshot과 elapsed 값은 canonical trace, solution/result fingerprint, comparator, seed, operator 선택, step/round 종료에 절대 포함하지 않는다.
- Profiling failure는 `PROFILING_RUN_FAILED`로 보고하고 normal solver result로 바꾸지 않는다. Recorder 호출 전에 committed state를 변경하거나 recorder failure를 잡아 candidate acceptance를 계속하지 않는다.

### 7.3 Hook 위치와 signature

`CowCandidateState`와 `AlnsStepExecutor`의 계획상 변경 signature:

```java
public final class CowCandidateState {
    public static CowCandidateState begin(
            CommittedSearchState base,
            CowProfilingRecorder profiling);

    RouteState copyRouteForWrite(RouteId routeId);
    SearchRequestBank copyBankForWrite();
    void invalidateDerivedState(RouteId routeId);
}

public final class AlnsStepExecutor {
    public AlnsStepResult executeCompletedStep(
            CommittedSearchState current,
            AlnsStepContext context,
            CowProfilingRecorder profiling);
}
```

Hook 규칙:

1. `copyRouteForWrite`가 실제 첫 copy를 만들 때만 `ROUTES_COPIED += 1`, 실제 backing element 수만 `ROUTE_ELEMENTS_COPIED`에 더한다. 같은 route의 두 번째 write는 copy count를 늘리지 않는다.
2. Bank도 candidate별 실제 copy 한 번만 기록한다.
3. Cache invalidation은 제거/invalid 처리한 entry count, rebuild는 실제 다시 계산한 entry count와 exclusive elapsed를 기록한다.
4. Propagation/evaluation timer는 같은 work를 두 center에 중복 기록하지 않는다. Evaluation이 propagation을 내부 호출한다면 outer integration layer에서 두 작업을 분리하거나 report를 `BLOCKED: COST_CENTER_OVERLAP`으로 실패시킨다.
5. Whole-step timer는 committed current를 읽기 직전 시작하고 commit/discard와 adaptive state 갱신이 끝난 뒤 멈춘다.
6. `COMPLETED_STEPS`는 solver의 기존 completed-step source of truth가 증가한 뒤 동일 ordinal을 관측해 한 번만 더한다. Destroy/repair 중 exception, cancel, watchdog, resource failure에서는 증가하지 않는다.
7. Instrumentation ON/OFF는 동일 algorithm control flow를 사용한다. `if (profiling.enabled())`로 solver branch의 semantic work를 생략하거나 추가하지 않는다.

### 7.4 Error model

```java
public enum CowProfilingErrorCode {
    NEGATIVE_DELTA,
    COUNTER_OVERFLOW,
    ELAPSED_OVERFLOW,
    NON_MONOTONIC_CLOCK,
    OVERLAPPING_COST_CENTERS,
    MISSING_PROFILE_CASE,
    UNSUPPORTED_PROFILE_CASE_SCHEMA,
    SOURCE_DIGEST_MISMATCH,
    REPRESENTATIVE_CASE_NOT_VERIFIED,
    OFFICIAL_AUTHORITY_MISSING,
    SEMANTIC_EQUIVALENCE_FAILED,
    INCOMPLETE_BENCHMARK_OUTPUT
}

public final class CowProfilingException extends IllegalStateException {
    public CowProfilingErrorCode code();
}
```

고정 message prefix는 `AR10_COW_PROFILE_<CODE>:`다. Test는 전체 platform path가 포함된 문자열을 exact match하지 않고 code와 prefix를 검증한다.

### 7.5 Decision gate

```java
public enum CowDecision {
    KEEP_COW,
    PROPOSE_SEPARATE_EXPERIMENT
}

public record CowDecisionRequest(
        boolean semanticEquivalencePassed,
        boolean representativeBundleComplete,
        boolean rawAllocationAndGcEvidencePresent,
        boolean reviewedCowBottleneckClaimPresent,
        boolean separateProposalRequested,
        java.util.List<String> evidenceDigests,
        String reviewReference) {}

public record CowDecisionResult(
        CowDecision decision,
        boolean switchAuthorized,
        java.util.List<String> reasons,
        java.util.List<String> evidenceDigests) {}

public final class CowDecisionGate {
    public CowDecisionResult decide(CowDecisionRequest request);
}
```

Gate 규칙:

- 어떤 입력에서도 `switchAuthorized`는 `false`다.
- 모든 boolean/evidence가 충족되지 않으면 `KEEP_COW`다.
- 모두 충족되어도 반환값은 `PROPOSE_SEPARATE_EXPERIMENT`뿐이며 apply/undo code 작성 권한이 아니다.
- `reviewedCowBottleneckClaimPresent`는 자동 비율 threshold가 아니라 raw report를 검토한 별도 authority reference다.
- `reviewReference`가 blank이거나 evidence digest가 비어 있으면 proposal을 거부한다.
- Semantic equivalence 실패는 성능이 좋아도 항상 `KEEP_COW` + `PROPOSAL_BLOCKED_SEMANTIC_REGRESSION`이다.

## 8. 측정 계약과 evidence report

### 8.1 측정 대상

| 대상 | Authority 측정 | 보조 측정 | 금지 해석 |
|---|---|---|---|
| Changed-route copy | Hook의 copy 횟수/element, JMH route-copy time/op | JFR allocation stack | element 수를 byte로 환산해 실제 allocation이라고 주장 |
| Bank copy | Hook의 copy 횟수/request 수/time | JFR allocation stack | Route copy와 합쳐 원인 숨김 |
| Cache | Invalidation/rebuild count와 exclusive time | Cache hit/miss metadata | Cache hit가 correctness source라고 주장 |
| Propagation/evaluation | Exclusive elapsed와 invocation | Full-step 대비 비율 | 가격/quality metric과 혼합 |
| Full completed step | `STEP_TOTAL`, normal completed step count | JMH single-shot/average time | 미완료 step을 denominator에 포함 |
| Allocation | JMH `gc.alloc.rate.norm`과 JFR allocation events | Heap/GC configuration | 수동 객체-size 추정만으로 결론 |
| GC | JMH `gc.count`, `gc.time`; JFR GC pause/events | JVM GC log | GC가 한 번 없었다고 allocation 0 주장 |
| Semantic result | Trace/solution/result digest exact equality | Human-readable diff | elapsed equality를 요구하거나 elapsed를 fingerprint에 포함 |

### 8.2 Benchmark source set/profile

- `src/profiling/java`는 `-Pprofiling`에서만 compile된다.
- `org.openjdk.jmh:jmh-core:1.37`과 `jmh-generator-annprocess:1.37`은 profile-only이며 normal `runtime` dependency tree에 없어야 한다.
- `CowStateStrategyBenchmark`는 최소 다음 benchmark method를 가진다.

```java
@Benchmark
public Object copyChangedRoute(RepresentativeCowState state);

@Benchmark
public Object executeCompletedStepProfilingOff(RepresentativeCowState state);

@Benchmark
public Object executeCompletedStepProfilingOn(RepresentativeCowState state);
```

- Route-copy microbenchmark와 representative full-step 결과를 섞어 단일 “COW 비율”로 만들지 않는다. Report가 두 결과와 측정 단위를 별도 표시한다.
- Full-step state는 invocation마다 immutable warm start에서 새 run state를 만든다. 이전 invocation의 mutated state를 재사용하지 않는다.
- JMH warm-up/measurement/fork 수는 **측정 harness 값**이며 solver `screenMaxSteps`, `phase2MaxSteps`, worker/round/watchdog 공식값이 아니다. 최초 계획상 제안은 warm-up 5 iterations, measurement 10 iterations, 3 forks다. 실제 변경은 evidence comparability note와 함께 report schema version을 올린다.
- JMH `gc` profiler와 JFR profiler를 함께 사용한다. 환경이 JFR를 지원하지 않으면 allocation stack attribution이 불완전하므로 `DONE`이 아니라 `BLOCKED: JFR_EVIDENCE_MISSING`이다.
- JVM, GC algorithm, heap flags, CPU/OS, process affinity 여부, fork/warm-up/measurement, JMH version, Java build, git tree/diff digest를 report에 기록한다.

### 8.3 Profiling on/off semantic equivalence

동일성 비교는 다음 고정 envelope를 사용한다.

```text
same immutable problem/travel/profile/config/build
+ same initial/warm-start candidate
+ same base/derived seed and derivation version
+ same operator registry/order
+ same requested step/round lineage
+ same stable iteration/reduction/tie-break
+ same normal termination
```

반드시 exact equality를 요구하는 값:

- Canonical completed-step trace bytes와 digest
- Completed step/round count와 exact termination
- Accepted/discarded candidate outcome sequence
- `current`, `stageBest`, `solveBest` solution fingerprint
- Candidate verifier decision과 verified solution fingerprint
- Final request outcome partition, metric/objective와 publishable result fingerprint

같을 필요가 없고 semantic fingerprint에서 제외할 값:

- elapsed nanos, CPU, memory/allocation/GC, process/thread ID
- evidence capture timestamp와 output path
- JMH fork/iteration order

### 8.4 Report schema와 attribution

`cow-profiling-report.json`은 최소 다음을 가진다.

```text
schemaVersion
phaseId/rmMapping/evidenceId
sourceDesignVersionsAndDigests
gitCommitOrTreeAndDirtyDiffDigest
toolchainAndEnvironment
representativeCaseIdentityAndClassification
normalTerminationAndCompletedSteps
profilingOffSemanticDigests
profilingOnSemanticDigests
semanticEquality
counterAndExclusiveTimingSnapshot
jmhPrimaryAndSecondaryMetrics
jfrRecordingDigests
allocationStackAttribution
gcEventsAndPause
wholeStepCostAndDerivedOther
limitations
decisionInput
```

Attribution 규칙:

1. `STEP_TOTAL`은 inclusive, 나머지는 exclusive다.
2. `OTHER_STEP = STEP_TOTAL - exclusiveSum`; 음수면 report fail.
3. 비율 denominator와 단위를 각 표에 명시한다. `nanos`, `bytes/op`, `events/fork`, `count/completed-step`을 혼합하지 않는다.
4. JFR sampled allocation을 total allocated bytes로 오인하지 않는다. Total은 JMH GC normalized allocation, stack attribution은 JFR event/sample로 별도 표시한다.
5. Fork별 raw 값을 보존하고 평균만 남기지 않는다.
6. Outlier 제거를 자동 적용하지 않는다. 제외가 필요하면 원본과 제외 사유를 함께 남기고 decision은 별도 review를 요구한다.
7. 성능 수치만으로 correctness/publication gate를 대체하지 않는다.

## 9. 세분화된 test case

### 9.1 Unit/reproducibility tests

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `CowProfilingInstrumentationTest.doesNotChangeTraceOrSolutionFingerprint` | Repro; Master §13, 계획 §9.11 | `AR-4` fixed envelope의 작은 normal COW run, recorder OFF/ON | trace bytes, termination, completed steps, accepted/discarded outcomes, solution fingerprint exact equal | 최초 test compile에서 `cannot find symbol: CowProfilingRecorders`; API skeleton 뒤 `expected <traceDigestOff> but was <traceDigestOn>` | 모든 semantic field exact equal, elapsed는 비교 제외 |
| `CowProfilingInstrumentationTest.doesNotAdvanceOnInterruptedStep` | Fault; Master §12/§13 | Destroy 후 cancel, repair 후 exception, evaluation 중 watchdog injection | candidate discard; `COMPLETED_STEPS`, current/best fingerprint 불변 | `expected completedSteps=0 but was 1` 또는 current fingerprint diff | 세 fault 모두 zero advance와 same committed fingerprint |
| `CowProfilingRecorderTest.disabledRecorderIsStableNoOp` | Unit/architecture | Disabled singleton에 모든 enum/delta 호출 | zero ordered immutable snapshot | snapshot counter가 누락되거나 값이 증가 | 모든 enum이 stable order zero, mutation 불가 |
| `CowProfilingRecorderTest.recordsOnePhysicalCopyOnlyOnce` | Unit | 한 candidate에서 같은 route 두 번 write, 다른 route 한 번 write | `ROUTES_COPIED=2`; element count는 실제 두 copy 합 | expected 2, actual write-call 수 3 | First-write copy만 count |
| `CowProfilingRecorderTest.rejectsNegativeOverflowAndClockRegression` | Negative | negative delta, `Long.MAX_VALUE+1`, 역행 fake clock | 각 typed error code/prefix | 무시/랩어라운드/음수 elapsed | exact code와 no partial snapshot mutation |
| `CowProfilingRecorderTest.snapshotsAreImmutableAndOrdered` | Unit/repro | enum 순서와 임의 record order | declaration-order list, immutable defensive copy | `UnsupportedOperationException`이 안 나거나 order 변동 | 반복 snapshot canonical bytes 동일 |
| `CowDecisionGateTest.keepsCowWithoutReviewedBottleneckEvidence` | Governance; Q-ALG-02 | incomplete evidence 조합 전체 | 모두 `KEEP_COW`, `switchAuthorized=false` | proposal 또는 switch true | incomplete 조합별 stable reason |
| `CowDecisionGateTest.proposesOnlySeparateExperimentWithCompleteReviewedEvidence` | Governance | all-pass + raw digest + nonblank review ref + explicit request | `PROPOSE_SEPARATE_EXPERIMENT`, switch false | KEEP_COW 이외에 switch/implementation action 생성 | proposal만 생성, evidence digest 보존 |
| `CowDecisionGateTest.hasNoSwitchDecision` | Architecture/governance | enum reflection | 허용 enum 정확히 두 개 | `SWITCH_*`, `APPLY_UNDO` constant 발견 | exact set `KEEP_COW`, `PROPOSE_SEPARATE_EXPERIMENT` |

### 9.2 Scenario/integration tests

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `CowProfilingScenarioTest.attributesCopyCacheAndCompletedStepCost` | Integration/performance contract | Changed route 2개, unchanged route 1개, independent bank, known cache rebuild의 deterministic completed step | copy/bank/cache/propagation/evaluation/total center 존재; exact counts; total ≥ exclusive sum | center 누락, count mismatch 또는 `OTHER_STEP<0` | 모든 category/단위/step denominator valid |
| `CowProfilingScenarioTest.distinguishesRejectedAndAcceptedCandidates` | Integration | 같은 base에서 reject/accept 두 step | Copy cost는 둘 다 관측, committed state는 accept에서만 교체, counters separate | reject가 current를 바꾸거나 copy event 누락 | state semantics와 관측 모두 정확 |
| `CowProfilingScenarioTest.cacheHitMissDoesNotChangeSemanticResult` | Property/corruption | cold cache, warm cache, poisoned derived cache | full recomputation과 same result; hit/miss/cost만 다를 수 있음 | objective/feasibility/fingerprint diff | semantic equality, poisoned cache discard |
| `CowProfilingCaseManifestTest.rejectsUnverifiedMutableOrDigestMismatchedCase` | Negative/integrity | verifier FAIL, missing report, content digest mismatch, abnormal termination | respective typed error; benchmark 시작 전 실패 | JMH가 시작되거나 input deserialize 선행 | digest-before-deserialize와 both-pass guard |
| `CowProfilingCaseManifestTest.neverConvertsDecimalWinFixture` | Negative/governance | current `data/win_poc_case.json` ref를 official/converted case로 표기 | `OFFICIAL_AUTHORITY_MISSING` 또는 `REPRESENTATIVE_CASE_NOT_VERIFIED` | 반올림/절삭된 derived digest를 허용 | 원본 비준수 case를 official/implicit conversion으로 사용 불가 |
| `CowProfilingCaseManifestTest.allowsExplicitNonOfficialVerifiedCaseWithoutOfficialDefaults` | Integration/governance | AR-5 both-pass explicit config, `REPRESENTATIVE_NON_OFFICIAL` | case load 성공, official claim false | `Q-BENCH-02` default 주입 또는 case 거부 | explicit config 그대로 소비, official fields absent |
| `CowProfilingVerifiedPathIT.profilingOnOffProducesSamePublishableResultFingerprint` | Failsafe/repro/publication | 동일 AR-5 application fixture를 OFF/ON 두 번 실행 | 두 candidate/result verifier `PASS`; verified solution/outcomes/result fingerprint exact equal | single-pass/trace only 비교 또는 result digest diff | both-pass와 final exact equality |
| `CowProfilingVerifiedPathIT.profilingFailureNeverPublishesNormalResult` | Fault/publication | recorder overflow/clock regression 주입 | profiling run failure, publication 0, prior immutable result 보존 | `COMPLETED/PUBLISHED` 상태 노출 | structured failure와 no publication |

### 9.3 Benchmark/profile/architecture tests

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `CowBenchmarkProfileContractTest.compilesAndDiscoversBenchmarkOnlyWithProfile` | Build/architecture | Normal build와 `-Pprofiling` build | Normal runtime에 JMH 없음; profile에서 `CowStateStrategyBenchmark` discovered | profile 미존재, benchmark list empty, normal dependency leak | 두 mode dependency/discovery assertion 통과 |
| `CowBenchmarkProfileContractTest.requiresCaseAndOutputDirectory` | Negative | 각 property 누락 | `MISSING_PROFILE_CASE` 또는 fixed argument error; benchmark 미실행 | implicit fixture/default output 사용 | 둘 다 explicit일 때만 start |
| `CowBenchmarkProfileContractTest.writesCompleteEvidenceIndex` | Integration | Tiny verified test-only case | JMH JSON, human text, JFR digest, GC summary, attribution, report/decision input index | 파일 하나라도 누락 또는 digest 없음 | schema-required artifact/digest 모두 존재 |
| `CowStateStrategyBenchmark.copyChangedRoute/executeCompletedStepProfilingOff/executeCompletedStepProfilingOn` | Benchmark | §5의 representative case | warm-up/measurement/fork/environment와 raw secondary metrics 생성 | JMH raw JSON/JFR/GC secondary metric 누락 | all raw artifacts와 normal case lineage |
| `ProfilingDependencyArchitectureTest.jmhExistsOnlyInProfilingProfile` | Architecture | Effective POM/dependency tree | Normal solver runtime에서 `org.openjdk.jmh` 0, profile에서만 존재 | normal runtime dependency 발견 | zero leak |
| `ProfilingDependencyArchitectureTest.forbidsProviderCustomerAndApplyUndoTypes` | Architecture/scope | Solver profiling main/profile source | Provider SDK/name, customer branch, apply/undo implementation 0 | 금지 import/class/name 탐지 | 허용된 decision report 문자열 외 implementation 0 |
| `ProfilingDependencyArchitectureTest.metricsCannotReachComparatorFingerprintOrTermination` | Architecture | Bytecode/package dependency graph | Profiling snapshot에서 search decision/fingerprint/termination 방향 dependency 없음 | comparator/fingerprint가 profiling type 참조 | recorder는 outbound observation leaf |

## 10. 강제하는 red → green 구현 순서

각 단계는 앞 단계 evidence 없이는 다음 production 변경을 시작하지 않는다.

### 10.1 Step 0 — preflight와 baseline green

1. §12.1 명령으로 source hash, working tree와 toolchain을 기록한다.
2. `AR-4`, `AR-5` phase status/evidence/handoff를 확인한다.
3. Targeted solver/application baseline verify를 실행해 green report를 `regression/before/`에 보존한다.
4. Representative case를 §5 절차로 선택하고 input digest를 확인한다.
5. 어느 entry가 실패하면 test/production file을 만들지 않고 `BLOCKED` handoff만 작성한다.

### 10.2 Step 1 — API compile red를 먼저 관찰

1. Production file보다 먼저 `CowProfilingInstrumentationTest`와 `CowProfilingRecorderTest`를 만든다.
2. 제안 API를 import한 targeted Maven test를 실행한다.
3. `testCompile`의 `cannot find symbol: CowProfilingRecorders`/`CowProfilingRecorder`를 `red/api-compile.log`에 보존한다.
4. Dependency download, unrelated failure, test 미발견은 올바른 red가 아니다.
5. 이 compile failure를 실제로 보지 못하면 production API skeleton을 만들지 않는다.

### 10.3 Step 2 — 최소 API skeleton과 semantic assertion red

1. §7 signature만 가진 최소 compiling skeleton을 추가한다.
2. Disabled zero snapshot과 collecting recorder가 아직 값을 기록하지 않게 둔다.
3. 같은 test를 실행해 `expected routesCopied=1 but was 0`과 immutable/order assertion failure를 관찰한다.
4. Assertion failure report를 `red/recorder-semantics/`에 보존한다.
5. 그 다음에만 primitive counter/timer와 typed error를 최소 구현한다.

### 10.4 Step 3 — state hook red/green

1. `CowProfilingScenarioTest`를 production hook 변경 전에 작성한다.
2. Existing COW state에서 semantic result는 green이지만 expected route/bank/cache count가 0인 red를 관찰한다.
3. `CowCandidateState` first-write copy와 invalidation 지점에 최소 hook을 추가한다.
4. Exact counts와 reject/fail discard가 green인지 확인한다.
5. Hook을 넣기 위해 state ownership이나 COW algorithm을 재작성하지 않는다.

### 10.5 Step 4 — whole-step attribution red/green

1. Total/exclusive/other assertion을 먼저 추가한다.
2. `PROPAGATION`, `EVALUATION` 또는 `STEP_TOTAL` 누락 red와 `OTHER_STEP` 계산 failure를 관찰한다.
3. `AlnsStepExecutor`에 exclusive timer를 최소 추가한다.
4. Completed-step semantics, cancel/watchdog/failure counter가 green인지 확인한다.
5. Timer nesting이 겹치면 값을 보정하지 말고 hook boundary를 고친다.

### 10.6 Step 5 — OFF/ON solver semantic equivalence

1. OFF/ON exact trace/solution assertion을 실행한다.
2. 차이가 있으면 production benchmark/POM 작업을 시작하지 않는다.
3. Difference의 첫 step/operator/state field를 `semantic-diff.json`에 남긴다.
4. 동일성이 green이 된 뒤 module test를 실행한다.

### 10.7 Step 6 — profiling-case guard와 benchmark source set

1. `CowProfilingCaseManifestTest`, `CowBenchmarkProfileContractTest`, architecture test를 먼저 작성한다.
2. Missing profile/source/class/discovery, unverified case, JMH normal dependency leakage의 의도한 red를 각각 확인한다.
3. Root/solver POM과 `src/profiling/java`를 최소 구현한다.
4. Normal profile OFF dependency tree와 profiling profile ON discovery를 모두 green으로 만든다.
5. Current Win fixture의 변환 code/resource를 만들지 않는다.

### 10.8 Step 7 — application both-pass equivalence

1. `CowProfilingVerifiedPathIT`를 production application 변경 없이 추가한다.
2. Recorder injection seam이나 final result equality가 아직 없어 발생하는 compile/assertion red를 확인한다.
3. 이미 존재하는 application composition seam으로 recorder를 주입하는 최소 test-only wiring만 추가한다. Provider adapter나 public API를 바꾸지 않는다.
4. OFF/ON 두 실행의 candidate verifier/result verifier `PASS`와 result fingerprint exact equality를 green으로 만든다.

### 10.9 Step 8 — representative profiling과 decision

1. Profile case를 digest 검증해 evidence input directory에 둔다.
2. 고정 JMH/JFR command를 실행하고 raw output을 보존한다.
3. Report completeness test를 실행한다.
4. `CowDecisionGate`에 raw evidence digest와 review reference를 입력한다.
5. Review ref가 없거나 병목 evidence가 불충분하면 `KEEP_COW`로 정상 종료한다.
6. `PROPOSE_SEPARATE_EXPERIMENT`가 나와도 apply/undo source를 만들지 않는다.

### 10.10 Step 9 — target green, module verify, reactor regression

순서는 다음을 바꾸지 않는다.

```text
targeted unit red/green
→ targeted scenario green
→ application Failsafe both-pass green
→ profiling profile benchmark/report green
→ solver module verify
→ application module verify
→ architecture rules verify
→ full reactor verify
→ diff/scope/forbidden-value/source-hash validation
```

## 11. Implementation checklist

### 11.1 Entry와 preservation

- [ ] `git status --short --untracked-files=all`을 evidence에 저장했다.
- [ ] Source baseline SHA-256가 metadata와 일치한다.
- [ ] `AR-4 DONE` evidence ID/digest와 `AR-5 DONE` evidence ID/digest를 기록했다.
- [ ] Baseline solver/application verify가 green이다.
- [ ] Representative case는 both-pass normal execution이고 source bytes를 수정하지 않았다.
- [ ] Case classification이 `REPRESENTATIVE_NON_OFFICIAL` 또는 승인된 `OFFICIAL_APPROVED`로 명시돼 있다.

### 11.2 Test-first

- [ ] API compile red를 production skeleton 전에 관찰했다.
- [ ] Recorder assertion red를 recorder implementation 전에 관찰했다.
- [ ] Hook count red를 state/search 변경 전에 관찰했다.
- [ ] OFF/ON semantic diff red 또는 기존 동등성 baseline을 보존했다.
- [ ] Benchmark profile/discovery/dependency red를 POM 변경 전에 관찰했다.
- [ ] Application both-pass result equality red를 wiring 변경 전에 관찰했다.
- [ ] Red report마다 command, exit code, expected failure, actual failure가 있다.

### 11.3 최소 implementation

- [ ] Recorder는 primitive-only/run-scoped이고 static mutable state가 없다.
- [ ] Disabled recorder는 stable no-op이다.
- [ ] Copy counter는 write 호출이 아니라 물리 copy를 센다.
- [ ] Invalidation과 rebuild, propagation과 evaluation이 겹치지 않는다.
- [ ] 미완료 step은 `COMPLETED_STEPS`를 증가시키지 않는다.
- [ ] Recorder 값이 comparator/fingerprint/seed/operator/termination에 들어가지 않는다.
- [ ] Normal runtime dependency tree에 JMH가 없다.
- [ ] JFR/JMH output은 `target/codex-evidence`에만 생성된다.

### 11.4 Evidence와 decision

- [ ] OFF/ON trace, solution, both-pass result fingerprint가 exact equal이다.
- [ ] Fork별 JMH primary/secondary raw 결과가 있다.
- [ ] JFR recording과 digest가 있다.
- [ ] Route/bank copy, allocation/GC, cache, propagation, evaluation와 total step attribution이 있다.
- [ ] Report가 measurement limitation과 non-official 여부를 표시한다.
- [ ] Decision result의 `switchAuthorized=false`다.
- [ ] Default/불충분 evidence 결과는 `KEEP_COW`다.
- [ ] Proposal이면 별도 review reference가 있고 이 diff에는 apply/undo code가 없다.

### 11.5 Regression와 handoff

- [ ] Targeted tests, solver/application module verify, architecture verify, reactor verify가 모두 green이다.
- [ ] Forbidden provider/customer/apply-undo/default 검색 결과가 0이다.
- [ ] `git diff --check`와 target file scope 검사가 통과했다.
- [ ] Evidence bundle digest와 `handoff.md`가 생성됐다.
- [ ] 다음 phase에 default COW baseline, limitations와 proposal 여부가 전달됐다.

## 12. 정확한 실행 명령

모든 명령의 working directory는 다음으로 고정한다.

```bash
cd /Users/brown/workspace/ro-next
```

### 12.1 작성/구현 preflight

```bash
git status --short --untracked-files=all
git rev-parse HEAD
git branch --show-current
mvn -version
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md data/win_poc_case.json
rg -n '^(phase|rm_mapping|status|evidence_id|evidence_bundle):' docs/codex/phases/phase-04-cow-alns-and-reproducibility.md docs/codex/phases/phase-05-verification-finalization-and-publication.md
rg -n 'Q-ALG-02|Q-BENCH-02|Q-INFRA-01|Q-VAR-01' docs/master-design-open-questions.md
mvn -pl rpdptw/solver -am verify
mvn -pl rpdptw/application -am verify
```

첫 두 phase 문서가 없거나 `DONE` evidence를 가리키지 않으면 이후 Maven 명령을 실행하지 않는다.

### 12.2 First compile/assertion red

```bash
mvn -pl rpdptw/solver -am -Dtest=CowProfilingInstrumentationTest,CowProfilingRecorderTest -Dsurefire.failIfNoSpecifiedTests=false test
```

첫 실행의 정상 red는 `testCompile`의 계획상 제안 API `cannot find symbol`이다. API skeleton 뒤 같은 명령의 정상 red는 recorder count/equality assertion failure다. `No tests matching pattern`, dependency download failure, unrelated test failure는 정상 red가 아니다.

### 12.3 Targeted green

```bash
mvn -pl rpdptw/solver -am -Dtest=CowProfilingInstrumentationTest,CowProfilingRecorderTest,CowProfilingScenarioTest,CowProfilingCaseManifestTest,CowDecisionGateTest,CowBenchmarkProfileContractTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/application -am -Dit.test=CowProfilingVerifiedPathIT verify
mvn -pl build/architecture-rules -am -Dtest=ProfilingDependencyArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
```

### 12.4 Representative profiling

Evidence path와 case path는 고정하고 source artifact를 수정하지 않는다.

```bash
test -f "$PWD/target/codex-evidence/AR-10/cow-profile-v1/inputs/profile-case.json"
mkdir -p "$PWD/target/codex-evidence/AR-10/cow-profile-v1/benchmark"
mvn -pl rpdptw/solver -am -Pprofiling -Dcow.profile.case="$PWD/target/codex-evidence/AR-10/cow-profile-v1/inputs/profile-case.json" -Dcow.profile.outputDir="$PWD/target/codex-evidence/AR-10/cow-profile-v1/benchmark" verify
```

`profile-case.json`은 §5 절차가 생성한 evidence input이며 repository source fixture가 아니다. `mkdir`는 evidence output directory만 만들며 source tree를 바꾸지 않는다.

### 12.5 Module/reactor regression

```bash
mvn -pl rpdptw/solver -am verify
mvn -pl rpdptw/application -am verify
mvn -pl build/architecture-rules -am verify
mvn verify
```

### 12.6 Dependency와 forbidden-scope 검사

```bash
mvn -pl rpdptw/solver -DskipTests dependency:tree -Dscope=runtime -Dincludes=org.openjdk.jmh
mvn -pl rpdptw/solver -Pprofiling -DskipTests dependency:tree -Dincludes=org.openjdk.jmh
rg -n 'com\.google|software\.amazon|azure|CloudRun|Workflows|S3|GCS|gs://' rpdptw/solver/src/main rpdptw/solver/src/profiling
rg -n 'customer(Id|Name)|shprId|lssId' rpdptw/solver/src/main rpdptw/solver/src/profiling
rg -n 'ApplyUndo|applyUndo|SWITCH_TO_APPLY_UNDO|route pool|set.partition|MIP' rpdptw/solver/src/main rpdptw/solver/src/profiling
rg -n 'parallelRuns|iterationsPerRun|screenMaxSteps|phase2MaxSteps|maxRounds|5000' rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/profiling rpdptw/solver/src/profiling
rg -n 'CowProfiling|CowProfilingSnapshot' rpdptw/core rpdptw/verification
```

판정:

- Normal runtime JMH dependency 명령은 artifact line이 없어야 한다.
- Profiling profile dependency 명령에는 pinned `1.37`만 있어야 한다.
- Provider/customer/apply-undo 검색은 허용된 governance test string 외 production hit가 0이어야 한다.
- `screenMaxSteps` 등 이름은 manifest field로 나타날 수 있지만 numeric default assignment가 있으면 실패다.
- Core/verification에서 solver profiling type 참조는 0이어야 한다.

### 12.7 Evidence, link, heading와 diff 검사

```bash
rg -n '^#{1,4} ' docs/codex/phases/phase-10-cow-profiling-decision.md
rg -n 'Master Design §12|Master Design §15\.9|Domain Design §10\.4|Architecture Design §19|전체 구현 계획 §9\.11' docs/codex/phases/phase-10-cow-profiling-decision.md
test -f docs/codex/phases/phase-10-cow-profiling-decision.md
test -f docs/codex/implementation-plan.md
test -f docs/master-design.md
test -f docs/architecture-design.md
test -f docs/domain-design.md
git diff --check
git diff --name-only
git status --short -- docs/codex/phases/phase-10-cow-profiling-decision.md
git diff --no-index --check /dev/null docs/codex/phases/phase-10-cow-profiling-decision.md
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
```

신규 untracked file에 대한 `git diff --no-index --check`는 whitespace error가 없어도 “파일 차이 있음” 때문에 exit code 1일 수 있다. 판정은 stderr/stdout에 whitespace error가 없는지와 `git diff --no-index /dev/null <file>`의 대상이 정확히 이 파일 하나인지 함께 확인한다. 기존 사용자 변경 때문에 전체 `git diff --name-only`가 여러 파일을 보여도 이 phase 세션은 그 파일을 수정·복원·stage하지 않는다.

## 13. Deliverables와 evidence bundle

### 13.1 Source deliverables

1. Run-scoped read-only `CowProfilingRecorder`와 immutable snapshot
2. COW copy/cache/step 지점의 최소 hook
3. 일반 build와 분리된 `profiling` Maven profile/source set
4. `CowStateStrategyBenchmark`와 report writer
5. Solver semantic equivalence, application both-pass equivalence, benchmark/profile/architecture tests
6. `CowDecisionGate`와 `KEEP_COW` 또는 proposal-only decision

### 13.2 Evidence bundle 필수 파일

`evidence.json`은 전체 구현 계획 §11.1 공통 field 외 다음을 포함한다.

```text
representativeCaseClassification
sourceEvidenceId/sourceEvidenceDigest
cowStateStrategyVersion
profilingSchemaVersion
jmhVersion
jvmGcAndHeapFlags
warmup/measurement/forks
completedSteps/termination
semanticEquality
trace/solution/candidateVerifier/resultVerifier/result digests
jmhRawDigest/jfrDigests/reportDigest
decision/decisionReason/reviewReference
switchAuthorized=false
```

Evidence 소비 순서:

- Reviewer는 `evidence.json` → `semantic-diff.json` → raw JMH/JFR → attribution report → decision 순으로 읽는다.
- 다음 solver optimization phase는 `KEEP_COW` decision과 baseline report를 회귀 기준으로 소비한다.
- 별도 experiment proposal은 raw evidence digest와 승인 reference를 entry gate로 사용하지만, 이 bundle 자체가 apply/undo 구현 승인은 아니다.

## 14. Rollback

이 phase는 logical cutover, schema migration, provider state 또는 published result를 바꾸지 않는다. Rollback 단위는 다음과 같다.

1. `rpdptw/solver/pom.xml`의 `profiling` profile과 root POM의 profile-only version management
2. `src/profiling/java` 전체
3. `solver.profiling` recorder/decision types
4. `CowCandidateState`/`AlnsStepExecutor`의 hook parameter와 호출
5. AR-10 전용 test/architecture rule

Rollback 절차:

- 사용자/다른 세션 변경과 겹치지 않는 AR-10 commit 단위만 revert한다.
- Hook 제거 전 `git diff`로 AR-4 logic line과 AR-10 observation line을 구분한다. AR-4 state/search 구현을 되돌리지 않는다.
- Profile output은 immutable evidence로 보존하거나 CI artifact retention에 따라 만료한다. Published result/artifact pointer는 건드리지 않는다.
- Recorder injection이 application test seam에만 있다면 production application state/schema에는 rollback이 없다.
- Profiling failure 중 생성된 partial report는 `INCOMPLETE`로 표시하고 decision input으로 사용하지 않는다.
- `PROPOSE_SEPARATE_EXPERIMENT`를 철회해도 baseline decision은 `KEEP_COW`이며 state artifact/version을 apply/undo로 바꾸지 않는다.

Rollback 뒤 다음을 다시 실행한다.

```bash
mvn -pl rpdptw/solver -am verify
mvn -pl rpdptw/application -am verify
mvn verify
```

## 15. DONE/BLOCKED 판정

### 15.1 `DONE` AND gate

다음을 모두 만족해야 `DONE`이다.

1. `AR-4`, `AR-5`가 실제 `DONE`이고 immutable evidence digest가 연결된다.
2. Representative both-pass normal execution을 원본 변환 없이 사용했다.
3. 모든 production change에 first red → intended failure → minimal implementation → targeted green evidence가 있다.
4. Profiling OFF/ON의 canonical trace, solution과 publishable result fingerprint가 exact equal하다.
5. Route/bank copy, cache invalidation/rebuild, propagation, evaluation, total/other step의 count/time attribution이 완결됐다.
6. JMH normalized allocation/GC와 JFR allocation/GC raw evidence가 있다.
7. Toolchain, GC/heap, warm-up/measurement/forks, source/case fingerprints와 limitations가 report에 있다.
8. Targeted tests, solver/application/architecture verify와 full reactor `mvn verify`가 green이다.
9. Normal runtime의 JMH/provider/customer/apply-undo leakage와 hidden official numeric default가 0이다.
10. Evidence bundle digest와 handoff가 있다.
11. Decision이 `KEEP_COW` 또는 `PROPOSE_SEPARATE_EXPERIMENT`이고 `switchAuthorized=false`다.
12. Blocker와 scope violation이 남아 있지 않다.

측정 결과 COW가 병목이 아니거나 판단 기준이 없어서 `KEEP_COW`가 되더라도 위 evidence가 완결되면 `DONE`이다.

### 15.2 `BLOCKED`

다음 중 하나면 `DONE`을 주장하지 않는다.

- AR-4/AR-5 authority/evidence 부재
- Representative both-pass normal case 부재 또는 digest mismatch
- Profiling OFF/ON semantic drift
- JFR/JMH raw output 또는 environment record 부재
- Cost center overlap/음수 other/단위 혼합으로 attribution 불가능
- Profiling hook이 completed-step/state semantics를 바꿈
- Official claim에 필요한 Q-BENCH approval/compliant fixture 부재
- Provider/product 또는 optional variant 선택이 필요하다는 주장
- Apply/undo skeleton/production code가 diff에 포함됨
- Full reactor 또는 architecture regression 실패

`BLOCKED` record는 blocker ID, 마지막 안전 commit/tree, 실패 command/report, 필요한 authority와 재개 조건을 포함한다.

## 16. 다음 phase handoff

### 16.1 기본 handoff

정상 기본 handoff는 다음이다.

```text
decision = KEEP_COW
stateStrategyVersion = <AR-4 COW version>
semanticEquivalence = PASS
representativeCase = <artifact refs + digests>
profilingReport = <digest + CI artifact locator>
knownBottlenecks = <measured attribution, overclaim 없음>
switchAuthorized = false
```

후속 구현은 이 baseline보다 빠르다는 이유만으로 COW contract를 바꾸지 않는다. Solver 변경은 같은 semantic equality와 relevant regression을 다시 실행한다.

### 16.2 별도 experiment proposal handoff

`PROPOSE_SEPARATE_EXPERIMENT`일 때 handoff에는 추가로 다음이 있어야 한다.

- Reviewed COW bottleneck claim과 authority reference
- Raw allocation/GC/stack attribution digest
- 제안할 apply/undo experiment의 별도 scope 초안
- Master §12.3의 round-trip/fault/trace/verifier/final-solution 동등성 gate
- 별도 ADR/roadmap 승인 필요 문구
- 이 phase에 apply/undo code가 0이라는 diff evidence

새 승인 roadmap이 생기기 전에는 후속 agent가 proposal을 구현 task로 해석하지 않는다.

## 17. Scope exclusions와 금지 shortcut

### 17.1 명시적 제외

- Apply/undo production, prototype 또는 skeleton
- COW와 apply/undo dual strategy interface를 미리 core에 추가하는 일
- Route pool/MIP, multi-trip/rotation, MDVRP/OVRP/SDVRP
- Provider adapter, cloud profiler service, deployment/IaC와 production topology
- Public HTTP/API/wire/result schema 변경
- `Q-BENCH-02` 공식 수치, production default 또는 threshold 결정
- Win fixture의 `D/U` 반올림·절삭·재생성·변환
- New customer profile/objective/constraint
- Search quality 변경, operator 튜닝, seed 선별과 objective comparator 변경

### 17.2 금지 shortcut

- Legacy `AlnsBatchEngine` allocation을 COW baseline으로 보고
- Profiling ON/OFF에서 다른 seed/config/step/build를 사용
- Trace만 같고 candidate/result verifier equality를 생략
- Average 한 값만 남기고 fork raw data/JFR를 폐기
- Route element count를 allocation bytes로 환산해 실제 값으로 주장
- JFR sample byte를 total allocation으로 주장
- GC가 관찰되지 않은 한 run을 “GC 비용 0”으로 결론
- 미완료/cancel/watchdog step을 total step denominator에 포함
- Stopwatch/JMH elapsed를 quality objective, comparator, fingerprint 또는 termination에 사용
- README/GCP의 `8`, `5000`, timeout을 official/profile solver config로 복사
- 비준수 Win fixture를 “profiling 전용”이라는 이유로 묵시 변환
- 측정 비율 하나로 자동 apply/undo switch
- `KEEP_COW`를 profiling 실패/증거 부재를 숨기는 값으로 사용
- Test source 존재, benchmark 실행 1회 또는 report 파일 존재만으로 `DONE` 주장

## 18. 문서 작성 완료 검증 기준

이 문서 자체의 작성 세션은 다음을 확인하고 끝낸다.

1. 대상 경로는 `docs/codex/phases/phase-10-cow-profiling-decision.md` 하나다.
2. 전체 구현 계획 §10의 13개 항목이 각각 본문의 authority, gap, path, API, test, TDD, command, deliverable, rollback, DONE/BLOCKED, handoff, exclusion 절에 대응한다.
3. Test file 작성과 red 관찰이 production file/POM 변경보다 앞선다.
4. Source design hash는 작성 전 baseline과 동일하다.
5. Self heading, upstream link와 구현 계획의 phase link가 유효하다.
6. `git diff --check`와 신규-file whitespace check에 오류가 없다.
7. 이 세션은 code/POM/source design/다른 phase/progress 파일을 수정하지 않는다.

## 19. 작성 세션 검증 결과

| 검증 | 결과 |
|---|---|
| 대상 file/path | `docs/codex/phases/phase-10-cow-profiling-decision.md` 존재, YAML metadata와 H1 및 §1~§19 heading 확인 |
| Upstream heading/link | Master §12/§13/§14.1/§15.9, Domain §10.4/§10.5/§16/§17, Architecture §17/§19/§20/§21, 구현 계획 §9.11/§10, 질문 anchor 두 개의 실제 heading/anchor 확인 |
| Source hash drift | Implementation Plan `d4450f...14a`, Master `5e6a79...5f9c`, Architecture `161b08...212`, Domain `3a98d3...f98e`, question register `3d6bc4...c088`; 작성 전 metadata와 작성 후 값 동일 |
| Markdown hygiene | Code fence 50개로 짝수, trailing whitespace 0, tab 0 |
| Tracked diff check | `git diff --check` exit `0`, output 없음 |
| New-file whitespace check | `git diff --no-index --check /dev/null <target>` output 없음. Exit `1`은 whitespace 오류가 아니라 새 파일 차이 존재를 뜻함 |
| 변경 scope | New-file diff stat은 대상 Markdown 한 파일만 표시. 동시에 나타난 다른 phase 문서는 다른 세션 소유이며 읽기/수정/stage하지 않음 |
| Maven baseline | 이 문서 세션은 공유 `target/` 충돌을 피하려고 test/verify를 재실행하지 않음. 관리 세션의 AR-0 격리 관찰인 Java `25.0.3`, Maven `3.9.14`, 기존 test/verify `PASS`를 공통 baseline으로 사용 |
| 구현 blocker | AR-10 phase 구현 상태는 `AR-4 DONE` COW baseline과 `AR-5 DONE` both-pass representative evidence 전까지 `BLOCKED/BLOCKED_ON_PREDECESSOR_EVIDENCE`; 코드는 시작되지 않았으며 문서 작성·검토 상태는 별도 `document_status: REVIEW` |
