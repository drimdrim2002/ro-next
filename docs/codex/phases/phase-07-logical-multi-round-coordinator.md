# AR-7 / RM-6-logical — Logical multi-round coordinator 구현 계획

```yaml
phase: AR-7
rm_mapping: RM-6-logical
status: BLOCKED
document_status: REVIEW_READY
document_role: provider-neutral logical multi-round coordinator의 테스트 우선 실행 명세
authored_on: 2026-07-24
source_baseline:
  repository_head: 523c23e2e13410885b16e974efe40ffe598106ee
  working_tree: dirty; 사용자와 병렬 문서 세션의 기존 변경을 보존
  isolated_phase_00_observation:
    java: Amazon Corretto 25.0.3
    maven: 3.9.14
    legacy_test: PASS
    legacy_verify: PASS
    authority_note: 관리 세션이 전달한 격리 실행 결과; shared target의 동시 Maven 산출물은 baseline 판정에서 제외
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
predecessor_phase_documents:
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    required_phase: AR-0
    observed_at_authoring: absent
    observed_during_final_validation: parallel document present; status NOT_STARTED, isolated legacy test/verify baseline recorded
  - path: docs/codex/phases/phase-05-verification-finalization-and-publication.md
    required_phase: AR-5
    observed_at_authoring: absent
    observed_during_final_validation: parallel document present; status NOT_STARTED/entry BLOCKED, implementation evidence absent
  - path: docs/codex/phases/phase-06-local-application-and-logical-ports.md
    required_phase: AR-6
    observed_at_authoring: absent
    observed_during_final_validation: parallel document present; status NOT_STARTED, implementation evidence absent
blockers:
  - AR-0 DONE evidence와 target Maven reactor가 현재 없음
  - AR-5 DONE의 VerifiedSolution/candidate PASS/PublishableResult authority가 현재 없음
  - AR-6 DONE의 ArtifactStore/RunStateRepository/WorkerDispatcher/CancellationPort와 local CAS evidence가 현재 없음
official_scope_blockers:
  - Q-BENCH-02는 OPEN — EXPERIMENT_REQUIRED이며 공식 수치가 없음
  - data/win_poc_case.json의 decimal D/U는 official integer 계약에 비준수
  - Q-INFRA-01과 Q-VAR-01은 DEFERRED
```

## 1. 목적, 사용법과 현재 판정

이 문서는 [전체 구현 계획 §9.8](../implementation-plan.md#98-ar-7--rm-6-logical--logical-multi-round-coordinator)의 `AR-7 / RM-6-logical`만 실행하기 위한 명세다. 후속 구현 세션은 이 문서의 경로, 계획상 제안 API, red 관찰, 명령과 gate를 그대로 사용한다. 이 문서의 타입과 method 이름은 내부 구현의 모호성을 없애기 위한 **계획상 제안 API**이며 승인된 public Java API, HTTP API, wire schema 또는 저장 schema가 아니다.

현재 저장소에는 target reactor와 선행 phase 산출물이 없다. 따라서 문서 작성은 완료했지만 phase 구현 상태는 `BLOCKED`다. `AR-0` 뒤 identity/state-machine/fake test scaffold만 시작할 수 있고, `AR-5`와 `AR-6`가 `DONE`이 되기 전에는 AR-7 exit, finalization 연결 또는 정상 publication을 주장할 수 없다.

`Q-BENCH-02`의 공식 수치 부재는 provider-neutral coordinator와 명시적 test-only manifest의 구현을 막지 않는다. 반대로 이 phase가 공식 수치, official Win baseline 또는 provider topology를 만들 수 있다는 뜻도 아니다. 테스트 fixture의 작은 양의 수는 `TEST_ONLY` authority와 fixture ID를 manifest에 기록하며 production/official default로 승격하지 않는다.

## 2. Authority, 결정 상태와 blocker

### 2.1 규범 source

| Source | 이 phase가 소비하는 계약 | 결정 상태 |
|---|---|---|
| [Master §13](../../master-design.md#13-termination-reproducibility와-execution-provenance) | 정상/예외 termination 분리, 고정 seed/order/step/warm-start lineage, completion order 비의존성 | `REVIEW`; `C-08`, `C-22` 의미를 상속 |
| [Master §14.1](../../master-design.md#141-publication-gate) | Candidate/result 두 verifier 중 하나라도 fail/incomplete이면 정상 payload·benchmark vector 금지 | `REVIEW`; `C-21` 의미를 상속 |
| [Master §14.2~§14.4](../../master-design.md#142-primary-fixture와-manifest) | immutable manifest, verified complete-batch fan-in, retry identity, strict-improvement warm start | `REVIEW`; 공식 수치는 열려 있음 |
| [Master §15.8](../../master-design.md#158-rm-6--win-poc-official-workflow와-baseline) | logical coordinator/test double은 calibration 전 가능, official workflow는 별도 gate | `REVIEW` |
| [Architecture §11](../../architecture-design.md#11-distributed-multi-round-runtime) | provider-neutral state machine, run/round/worker/attempt identity와 idempotency | `[PORTABLE]` 추천 이름, 의미 계약 |
| [Architecture §12](../../architecture-design.md#12-provider-neutral-logical-ports) | `ArtifactStore`, `RunStateRepository`, `WorkerDispatcher`, cancellation, CAS 경계 | `[PORTABLE]` |
| [Architecture §13](../../architecture-design.md#13-physical-topology와-provider-boundary) | 물리 topology와 provider가 logical 의미를 정의하지 못함 | `Q-INFRA-01 DEFERRED` |
| [Architecture §14](../../architecture-design.md#14-logical-multi-round-execution) | phase-1 completeness, phase-2 declared completeness, retry와 incomplete rejection | `[PORTABLE]` |
| [Architecture §17~§19](../../architecture-design.md#17-observability-retry-failure와-security-boundary) | correlation, retry/failure matrix, fake/port evidence, reactor order와 `AR-7` gate | `REVIEW` |
| [Domain §14.3](../../domain-design.md#143-multi-round-execution-contract) | round/worker/seed/warm-start/steps/termination/verification/champion lineage | `REVIEW` |
| [Domain §15](../../domain-design.md#15-error-model) | defect, abnormal termination과 normal completion을 합치지 않음 | `REVIEW` |
| [Domain §16](../../domain-design.md#16-acceptance-evidence) | fixed envelope, stable reduction/tie-break와 normal termination 재현성 | `REVIEW` |
| [질문 등록부 `Q-BENCH-02`](../../master-design-open-questions.md#q-bench-02) | protocol은 확정, 공식 `screenMaxSteps`·worker 수·`phase2MaxSteps`·`maxRounds`·watchdog 수치는 없음 | `OPEN — EXPERIMENT_REQUIRED` |
| [질문 등록부 `Q-INFRA-01`](../../master-design-open-questions.md#q-infra-01) | logical port만 유지; provider/product/topology를 질문·활성화하지 않음 | `DEFERRED` |
| [질문 등록부 `Q-VAR-01`](../../master-design-open-questions.md#q-var-01) | optional variant를 현재 pair/terminal/bank 계약에 선반영하지 않음 | `DEFERRED` |

세 설계와 전체 구현 계획은 모두 승인된 외부 계약이 아니다. 충돌 시 채택된 외부 계약/승인 ADR → `APPROVED` Master → `APPROVED` 상세 설계 → 현재 `REVIEW` 문서 → 역사·연구 자료 순으로 처리한다. 현재 발견한 의미 충돌을 구현자가 임의 해소해서는 안 된다. 영향을 받는 질문/ADR/설계/phase 문서를 같은 변경 단위에서 갱신할 authority가 없으면 `BLOCKED`로 남긴다.

### 2.2 Entry gate와 blocker 판정

| Gate | 필요한 evidence | 작성 시점 관찰 | 판정 |
|---|---|---|---|
| `AR-0 DONE` | Java 25 reactor, `rpdptw-application`, architecture rules, test fixture convention | Root 단일 JAR만 존재; target module/POM 없음 | `BLOCKED` |
| `AR-5 DONE` | Candidate verifier `PASS`, immutable `VerifiedSolution`, finalization/result gate와 canonical digests | 해당 module/type/test/evidence 없음 | `BLOCKED` |
| `AR-6 DONE` | Immutable artifact ref/digest, CAS state repository, provider-neutral dispatcher/cancellation, local both-gate path | 해당 module/type/test/evidence 없음 | `BLOCKED` |
| AR-7 logical test authority | 모든 budget/worker/round 값이 explicit `TEST_ONLY` manifest에 있고 official default가 아님 | 계획으로 정의 가능 | 선행 gate 뒤 진행 가능 |
| AR-7 official authority | 승인된 `Q-BENCH-02` record와 compliant integer fixture | 둘 다 없음 | AR-7 범위 밖; `AR-9 BLOCKED` 유지 |
| Physical dispatcher | `Q-INFRA-01` resume evidence와 별도 승인 | `DEFERRED` | 구현 금지, AR-7 blocker로 오인 금지 |

현재 마지막 안전 지점은 “문서와 red test 목록만 존재하고 production AR-7 code는 없음”이다. 선행 artifact가 나타나면 그 실제 package/type/signature/hash를 이 문서의 계획상 제안 API와 대조한다. 이름만 다른 동등 계약은 phase 문서와 trace를 먼저 갱신하고 진행할 수 있지만, digest-before-deserialization, CAS, both-gate, completeness 또는 retry identity 의미가 빠져 있으면 즉시 `BLOCKED`다.

## 3. 실제 저장소 조사와 현 상태 → 목표 gap

### 3.1 작성 시점 inventory

Working directory는 `/Users/brown/workspace/ro-next`다.

- `git status --short`는 설계 문서의 사용자 변경, `data/`, `docs/codex/`와 여러 문서의 미추적/수정 상태를 보였다. 이 phase는 그 변경을 되돌리거나 정리하지 않는다.
- `find . -name pom.xml` 결과는 `./pom.xml` 하나뿐이다. Root는 `packaging=pom` aggregator가 아니라 단일 `jar`이고 Google Workflow Executions, Cloud Storage, Jackson, JUnit을 직접 의존한다.
- Production source는 `com.ronext.optimizer` 아래 6개, test는 `AlnsBatchEngineTest` 1개뿐이다.
- 관리 세션이 전달한 격리 phase-00 관찰은 Amazon Corretto `25.0.3`, Maven `3.9.14`, 기존 `mvn test`/`mvn verify` 성공이다. 이를 공통 build baseline으로 사용한다.
- Shared working directory의 ignored `target/`은 여러 phase 세션이 Maven을 동시에 실행한 transient 산출물을 포함할 수 있다. 그 안의 shade JAR replace 실패나 중간 report를 repository baseline 결함 또는 AR-7 blocker로 판정하지 않는다.
- `docs/codex/phases/`와 대상 파일은 작성 직전 존재하지 않았다. 선행 phase 문서와 `target/codex-evidence/AR-*`도 확인되지 않았다.
- `data/win_poc_case.json` SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이고 decimal `D/U`를 포함한다. 이 phase는 읽거나 변환하지 않으며 official fixture로 사용하지 않는다.

재현 명령:

```bash
cd /Users/brown/workspace/ro-next
git status --short
rg --files
find . -name pom.xml -print | sort
rg --files src/main/java src/test/java
java -version
mvn -version
find target/codex-evidence -maxdepth 4 -type f -print 2>/dev/null
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
```

### 3.2 Legacy 관찰과 목표 gap

| 영역 | 현재 관찰 | AR-7 목표 | Gap 처리 |
|---|---|---|---|
| Execution identity | `requestId`, `runNumber`, `seed`, `iterations`가 raw `Map<String,Object>`에 있음 | `RunId → RoundId → WorkerRunId → AttemptId`의 provider-neutral immutable identity | Target identity record와 canonical fingerprint test 신설 |
| Retry | Workflow의 `http.default_retry`; attempt identity/audit 없음 | Retry는 같은 logical assignment를 유지하고 `AttemptId`만 변경 | Retry event와 assignment fingerprint 비교를 state machine에 고정 |
| Worker assignment | `parallelRuns` range와 `seed + runNumber`; declared assignment artifact 없음 | Snapshot/warm start/seed/config/steps가 immutable `WorkerAssignment`로 선언됨 | Manifest에서 assignment set을 먼저 동결 |
| Phase 1 | Initial portfolio, available-candidate declaration, screen completeness가 없음 | AR-3이 선언한 모든 `AVAILABLE` candidate의 normal completion+verification 뒤 champion | 8-slot portfolio declaration과 available-set equality 검사 |
| Phase 2 | 단일 fan-out 후 즉시 finalize; round/warm-start/plateau 없음 | Declared batch complete fan-in, strict improvement, next-round common warm start | Round state, champion, lineage와 termination state 신설 |
| Completion | Candidate object key가 `requestId/runNumber`; retry가 같은 object를 덮어쓸 수 있음 | Same worker+same digest 수렴, same worker+different digest integrity failure | Immutable completion digest와 duplicate reconciliation |
| Completeness | Storage prefix 아래 발견한 일부 candidate만 읽음 | Expected worker identity 집합과 accepted verified completion 집합이 정확히 같아야 함 | Missing/extra/failed/unverified 모두 champion 차단 |
| Comparator | `double objective` 최솟값, 동률 order가 storage iteration에 의존 가능 | Bound quality comparator + deterministic structural tie-break | Quality relation과 total selection order를 분리 |
| Verification | Solver placeholder output을 검증 없이 final result로 사용 | Candidate verifier `PASS` artifact만 aggregation 입력 | AR-5 type/ref를 compile-time 입력으로 요구 |
| Publication | 한 verifier도 없이 `COMPLETED` JSON 저장 | Final champion만 finalization으로 넘기고 두 gate 뒤 CAS publication | AR-6 `ResultPublisher` 경계 소비, 직접 publish 금지 |
| Provider boundary | Controller와 workflow가 GCP SDK/YAML 의미를 직접 소유 | Application-owned logical port와 local/deterministic fake | GCP/GCS/URI/event type의 application 침투를 architecture test로 차단 |
| Hidden values | API/README의 `parallelRuns=8`, `iterationsPerRun=5000`, clock-derived seed | Explicit test manifest only; official omission은 rejection | Legacy 숫자 재사용 금지와 official guard test |

Legacy `OptimizationWorkerController.finalizeResult`와 `gcp/workflows/optimization.yaml`은 characterization 대상이다. AR-7 구현에서 수정·이동·삭제하지 않는다. 그 동작을 목표 fan-in으로 감싸거나 성공 worker 일부를 허용하는 compatibility shortcut도 금지한다.

## 4. 범위와 정확한 예상 경로

아래 경로는 AR-0/AR-5/AR-6가 전체 계획의 고정 module/package를 그대로 만들었다는 전제의 **계획상 고정 제안**이다. 선행 phase가 동등 책임을 다른 내부 파일로 구현했다면 production code를 중복 생성하지 말고 먼저 이 문서의 경로/API mapping을 갱신한다.

### 4.1 테스트 파일 — 반드시 production 파일보다 먼저 생성

| 순서 | 예상 경로 | 신규/변경 | 책임 |
|---:|---|---|---|
| T01 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/support/Ar7ExecutionFixtures.java` | 신규 | 명시적 `TEST_ONLY` manifest, portfolio/worker/completion/candidate digest builder |
| T02 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/support/ScriptedWorkerDispatcher.java` | 신규 | dispatch log와 지정 completion permutation을 제공하는 deterministic fake |
| T03 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/ExecutionIdentityTest.java` | 신규 | run/round/worker/attempt equality, validation, canonical identity |
| T04 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/PhaseOneCompletenessTest.java` | 신규 | 8-slot availability declaration과 모든 available screen의 normal completion/cache-free validation fan-in |
| T05 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/MultiRoundCoordinatorTest.java` | 신규 | declared phase-2 dispatch/fan-in, state transition, finalization handoff |
| T06 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/CompletionOrderIndependenceTest.java` | 신규 | 모든 completion permutation의 champion/state/audit digest 동일성 |
| T07 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/WorkerRetryIdentityTest.java` | 신규 | attempt만 변경되고 seed/warm start/config/run/steps가 보존됨 |
| T08 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/DuplicateWorkerCompletionTest.java` | 신규 | same digest convergence와 different digest integrity rejection |
| T09 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/IncompleteRoundRejectionTest.java` | 신규 | missing/failed/abnormal/unverified worker가 champion·next round·benchmark vector를 차단 |
| T10 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/StableChampionSelectorTest.java` | 신규 | quality comparator와 deterministic tie-break 분리 |
| T11 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/WarmStartLineageTest.java` | 신규 | strictly-better/equal/worse/max-round 분기와 공통 warm start |
| T12 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/OfficialManifestGuardTest.java` | 신규 | approval 없는 official numeric contract를 hidden default 없이 거부 |
| T13 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/MultiRoundCoordinatorFaultTest.java` | 신규 | cancel/watchdog/resource/platform/failure와 미완료 COW discard 관찰 |
| T14 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/MultiRoundCoordinatorIT.java` | 신규 | AR-6 artifact/state/CAS/dispatcher fake를 연결한 replay·conflict·final handoff |
| T15 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/SameProcessWorkerDispatcherTest.java` | AR-6 test 확장 | Same-process/local dispatcher가 round-aware logical assignment/attempt를 재해석하지 않음 |
| T16 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/LogicalCoordinatorBoundaryTest.java` | 신규 또는 기존 rule 확장 | application execution에 provider SDK/URI/event/customer branch가 0임 |
| T17 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/SolverCoordinatorParityTest.java` | 신규 | AR-4 in-process batch runner와 AR-7 logical coordinator의 completeness/champion/termination/lineage parity |

`Ar7ExecutionFixtures`는 다음 두 manifest를 제공한다.

- `allAvailableTestManifest()`: 8개 portfolio slot이 모두 `AVAILABLE`; phase-2 worker 세 개; test-only `screenMaxSteps=1`, `phase2MaxSteps=1`, `maxRounds=2`.
- `clockUnavailableTestManifest()`: 두 `CLOCK` slot은 AR-3이 만든 typed `UNAVAILABLE` evidence를 갖고 나머지 여섯 slot만 screen assignment를 선언한다.

이 값은 fake가 여러 identity와 round 분기를 통과시키기 위한 fixture-local 양의 수다. 각 manifest는 `TestOnlyAuthority("AR7-LOGICAL-*")`를 갖고 artifact/evidence에 그대로 기록한다. 이름이나 값 어느 것도 `Q-BENCH-02`의 후보, 권장값, default 또는 official contract가 아니다. Production source에는 이 값이 없어야 한다.

### 4.2 Production 파일 — 각 대응 red 확인 뒤에만 생성/변경

| 대응 test | 예상 경로 | 신규/변경 | 계획상 제안 책임 |
|---|---|---|---|
| T03, T07 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ExecutionIdentity.java` | AR-6 파일 변경 | AR-6 `SolveId/WorkerRunId/AttemptId`에 manifest-bound `RunId`, phase/ordinal `RoundId`와 declared slot hierarchy를 고정 |
| T04, T05, T12 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ExecutionManifest.java` | AR-6 파일 변경 | AR-6 exact refs/config/seed manifest에 `LogicalExecutionPlan`과 authority를 결합; default 없음 |
| T12 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ExecutionAuthority.java` | 신규 | `TestOnlyAuthority`와 approval artifact를 요구하는 `OfficialAuthority` 분리 |
| T04, T12 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/LogicalExecutionPlan.java` | 신규 | phase-1 inventory와 explicit phase-2 worker/steps/round plan; default 없음 |
| T04 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/PortfolioDeclaration.java` | 신규 | 4×2 slot 각각의 `AVAILABLE(candidateRef)`/`UNAVAILABLE(evidence)` authority |
| T05, T07 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/WorkerAssignment.java` | AR-6 파일 변경 | 기존 retry-stable assignment에 round/slot과 assignment fingerprint를 명시; attempt는 fingerprint에서 제외 |
| T05, T08, T09 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/WorkerCompletion.java` | 신규 | attempt, assignment fingerprint, exact termination, phase별 validation evidence와 candidate ref/digest |
| T04~T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/RunState.java` | AR-6 파일 변경 | AR-6 lifecycle/version/cancellation/result state에 immutable `MultiRoundProgress`를 결합 |
| T04~T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/RunStateTransition.java` | AR-6 파일 변경 | 기존 legal lifecycle transition이 multi-round reducer 결과만 받도록 확장 |
| T04~T13 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/MultiRoundProgress.java` | 신규 | declared set, accepted completions, champion, lineage와 audit를 가진 immutable progress |
| T04~T13 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/MultiRoundEvent.java` | 신규 | prepare/dispatch/completion/retry/cancel/reconcile event의 sealed hierarchy |
| T04~T13 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/MultiRoundTransition.java` | 신규 | next progress/state, port command, audit record 또는 typed rejection |
| T04~T13 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/MultiRoundStateMachine.java` | 신규 | side-effect-free transition과 completeness/digest/lineage invariant |
| T08, T09, T12, T13 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/CoordinatorFailure.java` | 신규 | stable error code와 identity-scoped evidence; raw provider exception 금지 |
| T06, T10, T11 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/StableChampionSelector.java` | 신규 | verified quality 비교, deterministic tie-break와 strict relation 분리 |
| T05~T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/service/MultiRoundCoordinator.java` | 신규 | artifact/state/dispatcher/cancellation port를 조립하고 CAS transition 실행 |
| T05, T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/CoordinateMultiRoundRun.java` | 신규 | start/resume/completion/retry/cancel-observation inbound use case |
| T05, T07, T14, T15 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/WorkerDispatcher.java` | AR-6 계약 소비; signature 변경 금지 | `dispatch/inspect/requestStop`의 opaque-handle 계약으로 round-aware `WorkerAssignment` 전달 |
| T05, T08, T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/RunStateRepository.java` | AR-6 계약 소비; signature 변경 금지 | SolveId-keyed create/find/expected-version CAS로 multi-round `RunState` 저장 |
| T05, T09, T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/ArtifactStore.java` | AR-6 계약 소비; 무변경 | digest-before-deserialization immutable put/read |
| T13, T14 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/CancellationPort.java` | AR-6 계약 소비; 무변경 | intent와 actual termination 분리 |
| T15 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/SameProcessWorkerDispatcher.java` | AR-6 계약 소비; test가 red일 때만 최소 수정 | assignment을 바꾸지 않는 local reference dispatch |
| T16 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/LogicalCoordinatorBoundaryTest.java` | test가 먼저인 동일 파일 | provider/customer/default 침투 차단 |

POM은 다음처럼 다룬다.

| 경로 | 계획 |
|---|---|
| `rpdptw/application/pom.xml` | AR-0이 core/solver/verification dependency와 Surefire/Failsafe를 이미 제공해야 한다. 새 runtime dependency를 추가하지 않는다. `*IT`가 reactor `verify`에서 실행되지 않으면 AR-0 build 계약 문제로 `BLOCKED`하고 AR-7에서 임의 plugin 설정을 덧붙이지 않는다. |
| `adapters/common/pom.xml` | AR-6의 application/local adapter dependency를 그대로 소비한다. Provider SDK 또는 새로운 test library를 추가하지 않는다. |
| `build/architecture-rules/pom.xml` | 기존 architecture test convention만 사용한다. AR-7을 위해 새 cloud/customer dependency를 추가하지 않는다. |

파일 이동·삭제는 없다. 특히 root `src/`, `gcp/`, `README.md`, `Dockerfile`, `data/`와 legacy module은 변경하지 않는다.

## 5. 계획상 제안 type/API와 불변조건

### 5.0 선행 phase type mapping

병렬 문서 세션이 작성한 AR-4/AR-5 문서는 최종 검증 중 읽기 전용으로 대조했다. 두 문서 역시 계획이며 production authority나 `DONE` evidence가 아니다.

| Upstream 계획상 type | AR-7에서의 소비 | 중복 구현 금지 |
|---|---|---|
| AR-4 `WorkerRunRequest` | Application `WorkerAssignment`에서 lossless하게 만드는 solver 실행 요청 | Seed derivation, ALNS config, steps 또는 warm-start 의미를 coordinator에서 다시 계산하지 않음 |
| AR-4 `WorkerRunResult` | Exact termination, completed steps, last committed candidate와 cache-free validation evidence | Solver execution/step accounting을 application에서 재구현하지 않음 |
| AR-4 `PhaseOneChampion` | In-process 경로의 phase-1 output 또는 application fan-in 결과와 equality를 검증할 authority | Phase-1 quality/comparator를 별도 `double`로 축약하지 않음 |
| AR-4 `PhaseTwoBatchPlan`/`PhaseTwoRoundResult` | Local in-process runner와 distributed logical assignment/reduction의 semantic parity oracle | Solver-side runner와 application coordinator가 다른 completeness/plateau 규칙을 갖지 않음 |
| AR-5 `CandidateVerificationResult.Passed` | Phase-2 `CandidatePassEvidence`를 만들 수 있는 유일한 정상 capability | Boolean `verified=true`, solver feasible flag 또는 summary로 대체하지 않음 |
| AR-5 `CandidateVerificationReport`/`VerifiedSolution` | Candidate PASS report/verified solution refs와 digest 결합 | Verification 계산을 application에 복사하지 않음 |
| AR-5 `PublishableResult` | Final champion이 AR-5 finalization/result gate와 AR-6 publication을 모두 통과한 뒤의 결과 | Coordinator가 직접 publishable result를 생성하지 않음 |
| AR-6 `ExecutionIdentity`/`ExecutionManifest`/`WorkerAssignment`/`RunState` | 기존 solve/worker/attempt, exact manifest, retry-stable assignment와 versioned lifecycle을 확장 | 같은 의미의 top-level ID/state/assignment type을 중복 생성하지 않음 |
| AR-6 logical ports | Artifact digest, SolveId-keyed CAS state, dispatch/inspect/stop, cancellation과 publication wiring | Port signature를 round 전용으로 갈아엎거나 provider SDK/URI/event를 새 application type으로 만들지 않음 |

AR-4의 application-independent batch runner와 AR-7의 logical coordinator는 같은 의미를 서로 다른 runtime 경계에서 실행한다. Fixed manifest local parity test는 두 경로의 declared set, champion, quality relation, termination과 lineage digest가 같음을 입증해야 한다. 실제 AR-4/AR-5 API가 위 계획 이름과 달라지면 §2.2의 mapping 절차를 먼저 적용한다.

### 5.1 Identity와 assignment

아래 signature는 모두 internal **계획상 제안 API**다.

```java
public final class ExecutionIdentity {
    public record ManifestFingerprint(String sha256Hex) {}

    public record RunId(
            SolveId solveId,
            ManifestFingerprint manifestFingerprint) {}

    public record RoundId(
            RunId runId,
            RoundKind kind,
            int ordinal) {
        public enum RoundKind { PHASE_ONE, PHASE_TWO }
    }

    public record WorkerRunId(
            RoundId roundId,
            String declaredSlot) {}

    public record AttemptId(
            WorkerRunId workerRunId,
            int attemptOrdinal) {}
}
```

Constructor invariant:

- 모든 ID 구성 요소는 non-null이고 문자열은 non-blank, canonical case/encoding을 사용한다.
- `PHASE_ONE` ordinal은 정확히 `0`, `PHASE_TWO` ordinal은 `>= 1`이다.
- `AttemptId.attemptOrdinal >= 1`이다.
- `declaredSlot`은 manifest/portfolio에 먼저 선언된 stable key다. Thread index, provider job ID, completion ordinal 또는 storage path에서 추정하지 않는다.
- `RunId`와 descendant의 equality/hash/canonical encoding은 clock, process, provider locator와 무관하다.

```java
public record WorkerAssignment(
        ExecutionIdentity.AttemptId attemptId,
        ArtifactRef solveSnapshotRef,
        ArtifactRef warmStartCandidateRef,
        long derivedSeed,
        String seedDerivationVersion,
        AlgorithmConfigRef algorithmConfigRef,
        long requestedMaxSteps,
        AssignmentFingerprint fingerprint) {
    public record AssignmentFingerprint(String sha256Hex) {}
}
```

`WorkerAssignment.fingerprint`는 `attemptId.attemptOrdinal`과 provider metadata를 제외하고 `attemptId.workerRunId`, snapshot digest, warm-start digest, derived seed/version, exact algorithm config digest와 requested steps를 포함한다. Retry는 이전 assignment에서 새 `AttemptId`만 바꾼 새 immutable value를 만들며 fingerprint가 같아야 한다. 그 밖의 field 하나라도 달라지면 retry가 아니라 `ASSIGNMENT_IDENTITY_CONFLICT`다.

### 5.2 Manifest authority와 portfolio declaration

```java
public sealed interface ExecutionAuthority
        permits TestOnlyAuthority, OfficialAuthority {}

public record TestOnlyAuthority(String fixtureId) implements ExecutionAuthority {}

public record OfficialAuthority(
        ArtifactRef approvedNumericContractRef) implements ExecutionAuthority {}

public record LogicalExecutionPlan(
        ExecutionAuthority authority,
        PortfolioDeclaration phaseOnePortfolio,
        long screenMaxSteps,
        List<PhaseTwoWorkerTemplate> phaseTwoWorkers,
        long phaseTwoMaxSteps,
        int maxRounds) {
}
```

- 모든 numeric field와 worker list는 manifest에 명시한다. `null`, omission, zero/negative 또는 `getOrDefault`로 채우지 않는다.
- `TestOnlyAuthority`는 result metadata와 evidence에 남고 official publication/benchmark path에서 거부된다.
- `OfficialAuthority`는 approval artifact ref가 없거나 digest/schema/approval 상태를 AR-9 authority가 검증할 수 없으면 `OFFICIAL_NUMERIC_CONTRACT_MISSING_OR_UNAPPROVED`다.
- AR-7은 `approvedNumericContractRef`의 실제 값을 만들거나 official 수치를 승인하지 않는다.
- `PortfolioDeclaration`은 4×2의 여덟 stable slot을 exactly once 선언한다. `AVAILABLE`은 candidate artifact digest를, `UNAVAILABLE`은 AR-3 authority evidence를 가진다.
- Phase-1 expected completion set은 `AVAILABLE` slot set과 정확히 같고, coordinator가 availability를 재판정하지 않는다.

### 5.3 Completion, duplicate와 error

```java
public sealed interface WorkerCompletion
        permits SuccessfulWorkerCompletion, FailedWorkerCompletion {}

public sealed interface CompletionEvidence
        permits PhaseOneScreenValidation, CandidatePassEvidence {}

public record PhaseOneScreenValidation(
        ArtifactRef cacheFreeValidationReportRef) implements CompletionEvidence {}

public record CandidatePassEvidence(
        ArtifactRef candidatePassReportRef,
        ArtifactRef verifiedSolutionRef) implements CompletionEvidence {}

public record SuccessfulWorkerCompletion(
        ExecutionIdentity.AttemptId attemptId,
        WorkerAssignment.AssignmentFingerprint assignmentFingerprint,
        TerminationRecord termination,
        CompletionEvidence evidence,
        ArtifactRef candidateRef) implements WorkerCompletion {
}

public record FailedWorkerCompletion(
        ExecutionIdentity.AttemptId attemptId,
        WorkerAssignment.AssignmentFingerprint assignmentFingerprint,
        TerminationRecord termination,
        SearchFailure failure) implements WorkerCompletion {
}
```

Successful completion의 공통 acceptance 조건은 모두 AND다.

1. Worker가 현재 manifest에 선언되어 있다.
2. Assignment fingerprint가 persisted declaration과 exact match다.
3. Termination은 worker 정상 완료인 `MAX_STEPS_REACHED`다.
4. Artifact digest는 deserialization 전에 검증됐다.
5. Cancellation intent 뒤 도착한 completion은 cancellation reconciliation 규칙을 통과하며 자동 정상화되지 않는다.

Phase별 evidence는 섞지 않는다.

- `PHASE_ONE`: AR-4가 만든 cache-free full validation evidence와 screen candidate digest를 요구한다. Candidate verifier `PASS`로 의미를 바꾸거나 solver summary만 받지 않는다.
- `PHASE_TWO`: AR-5 candidate verifier `PASS`, verified solution ref와 candidate digest의 결합을 요구한다.

동일 `WorkerRunId`에서:

- 같은 assignment fingerprint와 같은 phase-appropriate candidate digest의 duplicate success는 idempotent no-op로 수렴한다. 새 attempt/audit은 남길 수 있으나 champion input은 하나다.
- 다른 assignment fingerprint는 `ASSIGNMENT_IDENTITY_CONFLICT`다.
- 같은 assignment지만 다른 verified candidate digest는 `DUPLICATE_VERIFIED_DIGEST_CONFLICT` integrity failure다. 어느 하나도 선택하지 않고 run을 `FAILED`로 전이하며 publication을 차단한다.

계획상 error code:

```text
UNDECLARED_WORKER
IDENTITY_SHAPE_INVALID
ASSIGNMENT_IDENTITY_CONFLICT
ARTIFACT_DIGEST_MISMATCH
NON_NORMAL_WORKER_TERMINATION
CANDIDATE_VERIFICATION_NOT_PASS
DUPLICATE_VERIFIED_DIGEST_CONFLICT
PHASE_ONE_INCOMPLETE
ROUND_INCOMPLETE
WARM_START_LINEAGE_MISMATCH
STATE_VERSION_CONFLICT
CANCELLATION_OBSERVED
OFFICIAL_NUMERIC_CONTRACT_MISSING_OR_UNAPPROVED
```

Expected incomplete/fault는 raw exception이나 provider error string만으로 표현하지 않고 `CoordinatorFailure(code, runId, roundId, workerRunId, expected, actual, evidenceRefs)`에 남긴다. Programmer invariant defect는 fail-fast할 수 있지만 외부 publication status로 자동 매핑하지 않는다.

### 5.4 Stable champion selection

```java
public final class StableChampionSelector {
    public record ComparableCandidate(
            ExecutionIdentity.WorkerRunId workerRunId,
            ArtifactRef candidateRef,
            ObjectiveVector objectiveVector,
            String canonicalSolutionFingerprint) {}

    public record RoundChampion(
            ExecutionIdentity.RoundId roundId,
            ComparableCandidate selected) {}

    public RoundChampion select(
            ExecutionIdentity.RoundId roundId,
            List<ComparableCandidate> completeValidatedCandidates,
            ObjectiveComparator qualityComparator);

    public QualityRelation compareQuality(
            RoundChampion candidate,
            RoundChampion previous,
            ObjectiveComparator qualityComparator);
}

public enum QualityRelation { STRICTLY_BETTER, EQUAL, WORSE }
```

Selection total order:

1. AR-2/AR-5 authority에서 얻은 verified quality comparator
2. Quality-equal이면 canonical verified solution fingerprint
3. 그래도 같으면 `WorkerRunId` canonical order

두 번째와 세 번째 항목은 completion-order-independent winner를 고르기 위한 structural tie-break일 뿐 품질 성분이 아니다. `compareQuality`는 첫 번째 quality comparator만 사용한다. 따라서 structural tie-break로 선택된 다른 digest가 이전 champion과 품질상 같으면 반드시 `EQUAL`이며 다음 round를 열지 않는다.

Coordinator는 evidence artifact의 digest/schema를 검증해 읽은 뒤에만 `ComparableCandidate`를 만든다. Comparator 입력은 phase 1에서는 AR-4 cache-free full validation이 산출한 immutable quality/objective 값, phase 2에서는 candidate verifier가 재계산한 immutable quality/objective 값이다. Worker response가 중복 기재한 vector, solver summary, storage listing order, completion time, attempt ordinal, elapsed, provider ID 또는 `double` tolerance를 사용하지 않는다.

### 5.5 Pure state machine과 coordinator port API

```java
public final class MultiRoundStateMachine {
    public MultiRoundTransition apply(
            RunState current,
            MultiRoundEvent event);
}

public interface CoordinateMultiRoundRun {
    CoordinatorResult start(ArtifactRef executionManifestRef);
    CoordinatorResult resume(ExecutionIdentity.RunId runId);
    CoordinatorResult recordCompletion(WorkerCompletion completion);
    CoordinatorResult requestRetry(
            ExecutionIdentity.WorkerRunId workerRunId,
            RetryReason reason);
    CoordinatorResult observeCancellation(ExecutionIdentity.RunId runId);
}
```

`requestRetry`는 hidden retry count/backoff를 결정하지 않는다. 명시적 event가 non-terminal worker에 들어오면 다음 attempt ordinal을 CAS state에서 한 번만 배정하고 같은 assignment로 dispatch한다. Provider SDK의 내부 retry가 application attempt audit을 대체해서는 안 된다.

AR-6 port가 다음 의미를 제공해야 한다. 실제 signature가 다르면 의미 mapping을 phase 시작 전에 기록한다.

```java
public interface WorkerDispatcher {
    DispatchReceipt dispatch(WorkerAssignment assignment);
    DispatchStatus inspect(DispatchHandle handle);
    StopReceipt requestStop(DispatchHandle handle);
}

public interface RunStateRepository {
    Optional<RunState> find(ExecutionIdentity.SolveId solveId);
    RunState createIfAbsent(RunState initial);
    RunState compareAndSet(
            ExecutionIdentity.SolveId solveId,
            RunState.StateVersion expectedVersion,
            RunState next);
}
```

위 port signature는 AR-6 계획을 그대로 소비한다. Coordinator는 “state transition CAS 성공 → dispatch side effect”의 replay gap을 audit command/outbox identity로 해결해야 한다. 같은 `AttemptId`를 가진 assignment dispatch가 반복돼도 fake/local dispatcher는 같은 logical dispatch로 수렴한다. CAS conflict 뒤 최신 `RunState`를 다시 읽고 reducer를 replay하며 duplicate next round나 duplicate finalization을 만들지 않는다.

### 5.6 State transition

AR-6 `RunState` 안의 계획상 `MultiRoundProgress` state:

```text
PREPARED
→ PHASE_ONE_DISPATCHING
→ PHASE_ONE_RUNNING
→ PHASE_ONE_AGGREGATING
→ ROUND_DISPATCHING
→ ROUND_RUNNING
→ ROUND_VERIFYING
→ ROUND_AGGREGATING
→ ROUND_DISPATCHING | FINALIZING
→ PUBLISHING
→ SUCCEEDED

exceptional:
REJECTED_INPUT | BINDING_FAILED | CANCEL_REQUESTED | CANCELLED
WATCHDOG_REACHED | RESOURCE_LIMIT_REACHED | PLATFORM_TIMEOUT
FAILED | INCOMPLETE | PUBLICATION_REJECTED
```

AR-7은 `PREPARED`부터 final champion의 `FINALIZING` handoff까지를 주로 소유한다. AR-5/AR-6가 소유하는 finalization/result verification/publication의 판정을 재구현하지 않는다.

Transition invariant:

- Declared expected set은 dispatch 전에 persisted되고 이후 바뀌지 않는다.
- `acceptedCompletions.keySet == expectedWorkerRunIds`이고 각 completion이 해당 phase의 validation evidence를 가질 때만 aggregation 가능하다.
- Extra/undeclared completion은 무시하지 않고 integrity failure다.
- Missing, failed, non-`MAX_STEPS_REACHED`, unverified worker가 하나라도 있으면 champion/next round/finalization/benchmark vector가 없다.
- Phase-1 champion은 모든 declared `AVAILABLE` screen completion 뒤에만 생긴다.
- Phase-2 round의 모든 assignment는 정확히 같은 previous champion warm-start digest를 가진다.
- `STRICTLY_BETTER`이면 그 champion digest가 다음 round 모든 worker의 warm start다.
- `EQUAL`/`WORSE`이면 `NO_STRICT_IMPROVEMENT`, explicit `maxRounds`를 완결했으면 `MAX_ROUNDS_REACHED`다.
- Worker-level normal termination은 `MAX_STEPS_REACHED`만 허용한다. Coordinator-level `NO_STRICT_IMPROVEMENT`/`MAX_ROUNDS_REACHED`와 혼합하지 않는다.
- Cancel/watchdog/resource/platform/failure 때 incomplete COW candidate는 discard되며 last committed candidate도 자동 normal result가 아니다.
- Audit record는 input event, old/new state version, exact identities, artifact digests, decision과 failure code를 canonical order로 보존한다. Timestamp/elapsed는 metadata이며 semantic digest에서 제외한다.

## 6. 세분화된 테스트 계약

모든 test는 production change 전에 작성한다. 첫 API slice에서 `cannot find symbol` compile failure는 허용하지만, 그 뒤 의미 test는 compile 가능한 fixture/fake와 최소 skeleton 위에서 아래 지정 assertion으로 실패해야 한다. “테스트가 발견되지 않음”, dependency download, unrelated legacy failure 또는 환경 오류는 유효한 red가 아니다.

| Test class.method | 종류/권위 | Fixture | Expected | 첫 정상 red 관찰 | Green 조건 |
|---|---|---|---|---|---|
| `ExecutionIdentityTest.roundAndWorkerIdentityAreValueStable` | Unit; Architecture §11.3 | 동일 manifest/run/round/slot을 두 번 생성 | equality/hash/canonical bytes 동일 | 최초에는 `cannot find symbol: class RunId` | 네 identity type compile, invalid shape rejection, stable equality 통과 |
| `ExecutionIdentityTest.attemptCannotChangeLogicalWorker` | Unit/fault; retry identity | 같은 worker의 attempt 1/2와 다른-worker attempt | 앞 둘의 parent assignment 동일, 다른 worker 결합 거부 | `"attempt parent must equal declared logical worker"` | attempt ordinal만 바뀌고 worker identity가 보존 |
| `PhaseOneCompletenessTest.waitsForEveryDeclaredAvailableScreen` | Integration; Master §11.3/Architecture §11.2 | 8 available 중 마지막 screen completion만 누락 | champion/phase-2 dispatch 없음, missing set 1개 | `"phase-1 champion must be absent until every declared AVAILABLE screen is validated"` | 마지막 `MAX_STEPS_REACHED`+cache-free validation 뒤 한 번만 champion과 phase-2 assignment 생성 |
| `PhaseOneCompletenessTest.doesNotWaitForTypedUnavailableClockSlots` | Unit; Master §11.2 | 두 CLOCK slot unavailable, 여섯 available 완료 | 정확히 여섯 completion으로 phase-1 complete | `"UNAVAILABLE portfolio slots are not worker failures"` | unavailable evidence 보존, available set equality로 완료 |
| `MultiRoundCoordinatorTest.waitsForEveryDeclaredVerifiedWorker` | Integration/fault; Architecture §14 | phase-2 세 worker 중 두 PASS, 하나 missing | `ROUND_RUNNING/VERIFYING`, champion/next round 없음 | `"round champion must be absent while declared worker is missing"` | 세 번째 normal+PASS 뒤에만 aggregation |
| `MultiRoundCoordinatorTest.dispatchesEveryDeclaredWorkerExactlyOncePerAttempt` | Integration/idempotency | CAS replay와 같은 start/resume 두 번 | 같은 AttemptId dispatch log 한 건으로 수렴 | `"duplicate resume must not create a new attempt"` | dispatch command identity로 중복 제거 |
| `CompletionOrderIndependenceTest.selectsSameChampionForEveryPermutation` | Property/reproducibility; Master §13.2 | 세 verified completion의 6 permutations | champion digest, state digest, audit decision 동일 | `"completion order changed champion or state digest"` | permutation 전체 exact equality |
| `StableChampionSelectorTest.usesVerifiedQualityThenStableStructuralTieBreak` | Unit/property | quality A<B, quality-equal 서로 다른 fingerprints/worker IDs | quality 우선; equality 안에서 fingerprint/worker order | `"stable total order must not depend on list order"` | antisymmetry/transitivity와 모든 permutation 통과 |
| `StableChampionSelectorTest.tieBreakNeverCountsAsStrictImprovement` | Unit; Master §14.3~14.4 | 이전/새 champion quality equal, structural digest 다름 | `QualityRelation.EQUAL`, `NO_STRICT_IMPROVEMENT` | `"structural tie-break is not a fifth quality dimension"` | 다음 round dispatch 0, 정상 plateau termination |
| `WorkerRetryIdentityTest.preservesSeedWarmStartConfigStepsAndRunId` | Unit/fault; Architecture §11.3 | attempt 1 platform failure 뒤 explicit retry | assignment fingerprint exact same, attempt만 +1 | `"retry changed logical assignment fingerprint"` | snapshot/warm-start/seed/version/config/steps/run 전부 동일 |
| `WorkerRetryIdentityTest.rejectsRetryWithMutatedAssignment` | Negative | retry에서 seed 또는 warm-start만 변경 | `ASSIGNMENT_IDENTITY_CONFLICT` | `"mutated retry must be rejected"` | dispatch 없음, failure audit에 expected/actual digest |
| `DuplicateWorkerCompletionTest.convergesSameIdentitySameDigest` | Idempotency/fault | attempt 1/2가 같은 verified candidate digest 반환 | champion input 하나, audit에는 duplicate convergence | `"same digest duplicate must be idempotent"` | state version/CAS replay가 추가 candidate를 만들지 않음 |
| `DuplicateWorkerCompletionTest.rejectsSameIdentityDifferentDigest` | Corruption/fault; Architecture §11.3 | same worker, same assignment, candidate digest A/B | `FAILED`, champion/publication 없음 | `"conflicting verified digests must never be arbitrarily selected"` | typed integrity failure와 두 refs 보존 |
| `IncompleteRoundRejectionTest.neverPublishesBenchmarkVector` | Integration; Master §14.1/§14.4 | missing, failed, watchdog, cancelled, unverified를 parameterize | 각각 `INCOMPLETE` 또는 해당 exceptional state; vector/result ref 없음 | `"incomplete round exposed champion, next round, or benchmark vector"` | 모든 case에서 finalization/publisher fake 호출 0 |
| `WarmStartLineageTest.strictlyBetterChampionFeedsEveryNextRoundWorker` | Integration | round 1 quality가 previous보다 strict better | round 2 모든 assignment warm-start digest가 round 1 champion | `"next round workers must share the exact strict-better champion"` | seed/config은 worker별 선언 유지, warm-start만 공통 |
| `WarmStartLineageTest.equalOrWorseStopsWithoutDispatch` | Unit/integration | equal과 worse 각각 | `NO_STRICT_IMPROVEMENT`, dispatch 0 | `"equal/worse round must not open another round"` | termination/audit/last champion exact |
| `WarmStartLineageTest.explicitMaxRoundsStopsAfterCompleteBatch` | Integration | 모든 round strict better, explicit test maxRounds 도달 | 마지막 complete batch 뒤 `MAX_ROUNDS_REACHED` | `"max-round termination occurred before complete fan-in"` | declared round 수와 lineage 일치 |
| `OfficialManifestGuardTest.rejectsMissingApprovedNumericContract` | Unit/negative; `Q-BENCH-02` | official intent, approval ref 없음/unknown digest | pre-dispatch rejection | `"official execution requires an approved numeric-contract artifact"` | dispatcher/state round 생성 0, error code exact |
| `OfficialManifestGuardTest.testOnlyValuesCannotPublishOfficialResult` | Governance/integration | `TestOnlyAuthority` manifest의 complete logical run | logical result/evidence 가능, official benchmark publication 불가 | `"TEST_ONLY authority cannot become official by successful execution"` | authority가 handoff/result metadata에 보존 |
| `MultiRoundCoordinatorFaultTest.cancellationDiscardsIncompleteAndPreservesActualTermination` | Fault; Master §13.1 | running worker 중 cancel intent, worker actual cancelled/late success | incomplete candidate discard, intent/actual 분리 | `"cancellation intent must not be renamed MAX_STEPS_REACHED"` | normal champion/publication 0, last boundary/audit 보존 |
| `MultiRoundCoordinatorIT.replaysCasConflictWithoutDuplicateRoundOrFinalization` | Failsafe integration; Architecture §12/§18 | in-memory CAS가 첫 update 충돌, scripted dispatcher | reload/reduce 후 round/finalization command 각각 한 번 | `"CAS replay duplicated dispatch or finalization"` | final state/audit digest가 conflict 없는 run과 동일 |
| `SameProcessWorkerDispatcherTest.preservesRoundAndAttemptIdentityAcrossRetry` | Port contract | same-process dispatcher와 scripted worker | 전달 assignment bytes/digest exact, opaque receipt만 추가 | `"local dispatcher reinterpreted round/seed/config/warm-start"` | logical fields exact round-trip |
| `LogicalCoordinatorBoundaryTest.forbidsProviderAndHiddenDefaultDependencies` | Architecture | application execution bytecode/source scan | provider SDK/URI/event, clock seed, customer branch, hidden budget default 0 | forbidden class/package/default 목록 | reactor verify에서 위반 0 |
| `SolverCoordinatorParityTest.matchesInProcessBatchSemantics` | Integration/reproducibility; AR-4 handoff | 같은 snapshot/config/seed/warm-start와 scripted worker outcomes를 두 경로에 입력 | declared set, champion, quality relation, termination, lineage digest 동일 | `"in-process solver batch and logical coordinator diverged"` | 두 경로의 canonical parity projection byte equality |

## 7. 테스트 우선 구현 순서와 checklist

각 slice는 아래 공통 gate를 지킨다.

```text
failing test 작성
→ 지정 targeted command 실행
→ 표의 정확한 compile/assertion failure 확인·red report 보존
→ 그 test만 통과시키는 최소 production 구현
→ 같은 targeted command green
→ 해당 slice 관련 test group green
→ module verify
→ application+adapter verify
→ reactor regression
```

실행 checklist:

- [ ] 시작 전 `git status --short`, HEAD, Java/Maven, 설계 hash와 선행 evidence digest를 기록한다.
- [ ] `AR-0`, `AR-5`, `AR-6`의 phase 문서와 evidence bundle을 읽고 `DONE` AND gate를 확인한다.
- [ ] AR-6 port와 AR-5 verified artifact를 §5 API에 mapping한다. 의미 mismatch면 code 전에 `BLOCKED`한다.
- [ ] T01~T02 fixture/fake를 먼저 작성하고 fixture numeric authority가 모두 `TEST_ONLY`인지 assertion한다.
- [ ] T03 identity test를 작성하고 `cannot find symbol` red를 보존한 뒤 identity record만 최소 구현한다.
- [ ] T04 phase-1 completeness test를 red로 만든 뒤 portfolio declaration/phase-one transition만 구현한다.
- [ ] T05 declared phase-2 test를 red로 만든 뒤 assignment/fan-out/fan-in 최소 경로를 구현한다.
- [ ] T06/T10 comparator·permutation test를 red로 만든 뒤 quality/tie-break 분리 selector를 구현한다.
- [ ] T07 retry tests를 red로 만든 뒤 attempt-only retry transition을 구현한다.
- [ ] T08 duplicate tests를 red로 만든 뒤 same-digest convergence/different-digest failure를 구현한다.
- [ ] T09 incomplete tests를 red로 만든 뒤 missing/failed/unverified rejection을 구현한다.
- [ ] T11 warm-start tests를 red로 만든 뒤 strict/equal/worse/max-round transition을 구현한다.
- [ ] T12 official guard tests를 red로 만든 뒤 authority validation을 구현한다. 공식값은 추가하지 않는다.
- [ ] T13 cancellation/failure tests를 red로 만든 뒤 intent/actual/discard audit을 구현한다.
- [ ] T14 CAS integration test를 red로 만든 뒤 state/command replay idempotency를 구현한다.
- [ ] T15 local dispatcher contract를 red로 만든 뒤 AR-6 same-process adapter의 필요한 최소 변경만 한다.
- [ ] T16 architecture rule을 red로 증명할 때는 test-only forbidden fixture를 사용하고, 실제 provider dependency를 production POM에 추가하지 않는다.
- [ ] T17 parity test를 red로 만든 뒤 AR-4 semantic projection mapping만 최소 수정한다. Solver runner나 coordinator 규칙을 복사해 두 구현으로 만들지 않는다.
- [ ] Targeted green 뒤 `rpdptw/application`, `adapters/common`, architecture rules verify를 순서대로 실행한다.
- [ ] 전체 reactor `mvn verify`와 fixed test manifest 반복을 실행해 trace/state/champion digest equality를 확인한다.
- [ ] Evidence bundle, rollback 정보와 AR-8/AR-9 handoff를 작성한 뒤에만 `DONE`을 평가한다.

어떤 semantic slice도 표에 지정한 first failure를 보지 못한 상태에서 대응 production 파일을 생성·수정하지 않는다. Test가 처음부터 green이면 기존 동작이 우연히 요구를 만족한다고 결론내리지 말고 assertion이 실제 결함을 감지하도록 fixture를 mutation하여 red sensitivity를 입증한다.

## 8. Exact 명령

### 8.1 Baseline과 entry gate

```bash
cd /Users/brown/workspace/ro-next
git status --short
git rev-parse HEAD
java -version
mvn -version
find . -name pom.xml -print | sort
find docs/codex/phases -maxdepth 1 -type f -print | sort
find target/codex-evidence -maxdepth 4 -type f -print 2>/dev/null
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
```

선행 phase가 `DONE`인지 source file 존재로 추정하지 않는다. 각 phase evidence의 `evidence.json`, commands, red/green/regression과 handoff digest를 확인한다.

### 8.2 Red/green targeted tests

첫 compile red:

```bash
mvn -pl rpdptw/application -am -Dtest=ExecutionIdentityTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Phase-1과 declared phase-2:

```bash
mvn -pl rpdptw/application -am -Dtest=PhaseOneCompletenessTest,MultiRoundCoordinatorTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Comparator와 reproducibility:

```bash
mvn -pl rpdptw/application -am -Dtest=CompletionOrderIndependenceTest,StableChampionSelectorTest,WarmStartLineageTest,SolverCoordinatorParityTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Retry, duplicate, incomplete, authority와 fault:

```bash
mvn -pl rpdptw/application -am -Dtest=WorkerRetryIdentityTest,DuplicateWorkerCompletionTest,IncompleteRoundRejectionTest,OfficialManifestGuardTest,MultiRoundCoordinatorFaultTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Failsafe/local contract:

```bash
mvn -pl rpdptw/application,adapters/common -am -Dit.test=MultiRoundCoordinatorIT -DskipITs=false verify
mvn -pl adapters/common -am -Dtest=SameProcessWorkerDispatcherTest -Dsurefire.failIfNoSpecifiedTests=false test
```

각 command 뒤 target owner module report가 실제 생성됐는지 확인한다.

```bash
find rpdptw/application/target/surefire-reports -type f -maxdepth 1 -print | sort
find rpdptw/application/target/failsafe-reports -type f -maxdepth 1 -print | sort
rg -n 'tests="[1-9]|failures="[1-9]|errors="[1-9]' rpdptw/application/target/surefire-reports rpdptw/application/target/failsafe-reports
```

### 8.3 Target green → module verify → reactor regression

```bash
mvn -pl rpdptw/application -am test
mvn -pl rpdptw/application -am verify
mvn -pl rpdptw/application,adapters/common -am verify
mvn -pl build/architecture-rules -am verify
mvn verify
```

Fixed `TEST_ONLY` manifest reproducibility suite는 최소 두 번 실행하고 생성된 canonical trace/state/champion digest를 byte-for-byte 비교한다. Timestamp, elapsed와 fake completion schedule metadata는 semantic digest에서 제외한다.

### 8.4 금지 dependency/value와 identity 검색

```bash
rg -n 'com\.google|google\.cloud|amazon|aws|azure|gcp|gs://|Cloud Run|Workflows|http\.default_retry' rpdptw/application/src adapters/common/src/main
rg -n 'parallelRuns|iterationsPerRun|System\.nanoTime|Math\.random|ThreadLocalRandom|parallelStream|findAny' rpdptw/application/src/main adapters/common/src/main
rg -n 'getOrDefault|orElse|default' rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution
rg -n 'screenMaxSteps|phase2MaxSteps|maxRounds|watchdog' rpdptw/application/src/main rpdptw/application/src/test
rg -n 'latest|customerId|customerName|presetName.*contains' rpdptw/application/src/main adapters/common/src/main
rg -n 'WorkerRunId|AttemptId|warmStart|derivedSeed|AssignmentFingerprint|CompletionEvidence|candidateRef' rpdptw/application/src/main rpdptw/application/src/test
```

첫 두 검색은 match 0이 green이다. `getOrDefault/default`와 budget 이름 검색은 수동 review 대상이며, platform metadata 또는 required field 이름 외에 hidden semantic/default 값이 있으면 fail이다. Test 숫자는 `TestOnlyAuthority` fixture 안에만 있어야 한다.

### 8.5 문서, link와 diff

```bash
test -f docs/codex/phases/phase-07-logical-multi-round-coordinator.md
test -f docs/codex/implementation-plan.md
test -f docs/master-design.md
test -f docs/architecture-design.md
test -f docs/domain-design.md
test -f docs/master-design-open-questions.md
rg -n '^## ' docs/codex/phases/phase-07-logical-multi-round-coordinator.md
rg -n 'Master §13|Master §14|Master §15\.8|Architecture §11|Architecture §12|Architecture §13|Architecture §14|Architecture §17~§19|Domain §14\.3|Domain §15|Domain §16|Q-BENCH-02|Q-INFRA-01|Q-VAR-01' docs/codex/phases/phase-07-logical-multi-round-coordinator.md
git diff --check
git diff --no-index --check /dev/null docs/codex/phases/phase-07-logical-multi-round-coordinator.md
git status --short -- docs/codex/phases/phase-07-logical-multi-round-coordinator.md
git diff --name-only
git status --short
```

Untracked 문서는 일반 `git diff`에 나타나지 않으므로 `--no-index --check`를 반드시 함께 사용한다. 구현 세션에서는 `git diff --name-only`와 untracked file 목록을 시작 snapshot과 비교해 승인된 AR-7 경로 밖 변경이 0인지 확인한다. 사용자/다른 세션의 기존 변경은 rollback·restore·stage하지 않는다.

## 9. Deliverables와 evidence bundle

### 9.1 Deliverable

| Deliverable | 최소 내용 | Consumer |
|---|---|---|
| Logical identity contract | Run/round/worker/attempt canonical identity와 assignment fingerprint | AR-8 compatibility/idempotency, AR-9 official manifest |
| Versioned execution state | Declared sets, accepted completions, state version, failure/audit, termination | AR-8 status/cancellation mapping |
| Phase-1 completeness/champion | 8-slot availability authority, every-available fan-in, stable champion | AR-9 official phase-1 |
| Phase-2 assignments | Explicit worker list, same warm start, derived seed/config/steps | Local/future provider dispatcher |
| Duplicate/retry reconciliation | Same-digest convergence, different-digest failure, attempt-only retry | AR-8 retry compatibility, future adapter contract |
| Stable champion selector | Verified quality comparator + non-quality structural tie-break | AR-9 exact complete fan-in |
| Round lineage | Previous champion, round champion, quality relation, next warm start, normal termination | Reproducibility/official rerun |
| Deterministic fake evidence | Dispatch/attempt/completion permutation log와 CAS replay | AR-8 shadow/rollback tests |
| Finalization handoff | Complete final champion ref와 candidate PASS ref만 AR-5/6 path로 전달 | Both-gate finalization/publication |

### 9.2 Evidence bundle

실행 artifact는 다음 위치에 둔다.

```text
target/codex-evidence/AR-7/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── identity-compile-red.txt
│   ├── phase-one-completeness-red.txt
│   ├── round-completeness-red.txt
│   ├── retry-digest-red.txt
│   ├── comparator-lineage-red.txt
│   └── authority-fault-red.txt
├── green/
│   └── targeted-surefire-failsafe/
├── regression/
│   ├── application-verify/
│   ├── adapter-verify/
│   ├── architecture-verify/
│   └── reactor-verify/
├── fingerprints/
│   ├── test-manifest.sha256
│   ├── state.sha256
│   ├── champion.sha256
│   └── lineage.sha256
├── faults/
│   ├── duplicate-retry-matrix.json
│   ├── incomplete-worker-matrix.json
│   ├── cancellation-termination-matrix.json
│   └── cas-replay.json
├── reproducibility/
│   ├── completion-permutations.json
│   └── fixed-manifest-rerun.json
├── diff/
│   ├── changed-files.txt
│   ├── diff-check.txt
│   ├── forbidden-search.txt
│   └── dependency-graph.txt
└── handoff.md
```

`evidence.json`은 전체 계획 §11의 공통 field 외에 `manifestAuthority=TEST_ONLY`, declared/accepted phase-one count, round별 declared/accepted worker count, duplicate convergence count, digest conflict count, CAS replay count, completion permutation count, champion/lineage digest와 final coordinator termination을 포함한다. Test-only evidence에 official baseline/challenger/Win vector를 기록하지 않는다.

Red evidence마다 다음을 기록한다.

- 먼저 추가한 test file과 method
- 정확한 Maven command/exit code
- 표에 지정한 first compile/assertion failure
- unrelated failure가 아님을 보여 주는 owner module report
- failure를 통과시킨 최소 production diff와 대응 green report

## 10. Rollback

AR-7에는 physical deployment와 public cutover가 없으므로 rollback 단위는 logical code/state schema와 composition이다.

1. AR-7 전용 신규 test/source file을 하나의 phase commit 단위로 식별한다. 사용자 변경과 다른 phase commit을 함께 revert하지 않는다.
2. AR-6 port 파일을 변경했다면 method별 compatibility adapter를 먼저 제거하고 AR-6 원 signature로 되돌린다. `git reset --hard`, broad `git restore`, directory 삭제를 사용하지 않는다.
3. Persisted `RunState`/`MultiRoundProgress`/manifest의 계획상 internal schema version을 올렸다면 이전 reader/writer와 새 schema를 섞지 않는다. 새 state/artifact는 immutable하게 보존하고 publication pointer만 이전 logical version으로 CAS 복귀한다.
4. 이미 생성된 candidate/report/state artifact를 덮어쓰거나 삭제하지 않는다. Rollback 후 retrieval은 그것을 정상 published result가 아니라 versioned/incomplete/recovery artifact로만 볼 수 있어야 한다.
5. Same-process dispatcher composition을 AR-6 단일-run path로 복귀시켜도 AR-5 두 verifier gate는 유지한다. Raw candidate 직접 publication은 rollback이 아니다.
6. Evidence bundle에 rollback 전후 module verify, state/pointer version, 잔존 artifact digest와 사용자 변경 보존 여부를 기록한다.

Rollback이 public API, provider resource, external schema 또는 production deployment 변경을 요구하면 AR-7 범위를 벗어난 것이다. AR-8/별도 승인 없이 실행하지 않는다.

## 11. DONE/BLOCKED 판정

### 11.1 `DONE` — 모두 만족하는 AND gate

- [ ] `AR-0`, `AR-5`, `AR-6`가 각 phase의 실제 evidence bundle로 `DONE`이다.
- [ ] §4의 AR-7 범위 test/source/port integration이 target reactor에 존재한다.
- [ ] 모든 요구에 표의 정확한 red → 최소 구현 → targeted green evidence가 있다.
- [ ] 모든 phase-1 `AVAILABLE` screen과 모든 declared phase-2 worker의 completeness가 set equality로 검증된다.
- [ ] Worker retry는 attempt만 바꾸고 assignment fingerprint를 exact 보존한다.
- [ ] Same digest duplicate는 수렴하고 different digest는 integrity failure로 champion/publication을 차단한다.
- [ ] 모든 completion permutation에서 champion/state/audit/lineage digest가 같다.
- [ ] Structural tie-break가 `STRICTLY_BETTER` 판정에 들어가지 않는다.
- [ ] Missing/failed/abnormal/unverified/cancelled worker에서 next round, finalization, publication과 benchmark vector가 모두 없다.
- [ ] Strict-better common warm start, equal/worse plateau와 explicit max-round lineage가 정확하다.
- [ ] `TEST_ONLY` manifest는 logical evidence를 만들 수 있지만 official result로 승격되지 않는다.
- [ ] Application, adapter, architecture module verify와 reactor `mvn verify`가 통과한다.
- [ ] Provider/customer/default forbidden search가 0이고 AR-7 evidence bundle digest가 있다.
- [ ] AR-8/AR-9 consumer가 identity/state/lineage artifact를 실제로 검증해 읽는 handoff test 또는 acceptance record가 있다.

단순 class/test/fake/demo 존재, happy path 한 번, 일부 worker best, current GCP workflow 성공 또는 source compilation만으로 `DONE`이 아니다.

### 11.2 `BLOCKED` 또는 즉시 중단

다음 중 하나면 안전한 마지막 commit/evidence를 남기고 `BLOCKED`다.

- AR-5 candidate PASS/verified solution 또는 AR-6 digest/CAS/dispatcher contract가 없거나 의미가 충돌한다.
- Worker completion을 verifier 전 solver summary/objective로 비교해야만 진행할 수 있다.
- 일부 성공 worker fallback, first-completion winner 또는 retry 때 새 seed/warm start/config가 필요하다.
- Stable selection과 strict quality relation을 하나의 comparator로 합쳐 tie-break를 품질 개선으로 취급해야 한다.
- Official mode가 approval artifact 없이 budget/worker/round/watchdog 값을 채운다.
- Provider resource name/URI/event/SDK type을 application API에 넣어야 한다.
- `Q-INFRA-01`, `Q-VAR-01`, multi-trip/rotation, route pool/MIP 또는 apply/undo를 활성화해야 한다.
- Current decimal Win fixture를 변환·반올림·절삭해 official fixture로 써야 한다.
- 사용자/다른 세션 변경과 AR-7 diff가 겹쳐 안전하게 분리할 수 없다.

`Q-BENCH-02` 공식 수치와 compliant Win fixture의 부재 자체는 generic AR-7 logical `DONE`을 막지 않는다. AR-7은 그 부재를 hidden value 없이 guard하는 것이 완료 조건이며, official `AR-9`는 계속 `BLOCKED`다.

## 12. 다음 phase handoff

### 12.1 AR-8 / RM-8-cutover

다음을 digest와 함께 넘긴다.

- `RunId/RoundId/WorkerRunId/AttemptId` canonical identity/version
- State transition table, CAS/outbox/replay rule와 terminal status/error code
- Retry same-assignment, duplicate convergence/conflict와 cancellation intent/actual matrix
- `TEST_ONLY` manifest, fake dispatcher completion permutations와 fixed rerun evidence
- Complete final champion에서 AR-5/AR-6 finalization/publication으로 가는 both-gate handoff

AR-8은 legacy `requestId/runNumber`를 이 identity에 조용히 alias하지 않는다. Compatibility matrix에서 mapping, 충돌, intentional break와 rollback을 명시해야 한다. Legacy GCS object prefix/list-min behavior는 target completeness evidence가 아니다.

### 12.2 AR-9 / RM-6-official

AR-9가 소비할 seam:

- Explicit `LogicalExecutionPlan`과 `OfficialAuthority` approval artifact reference
- 모든 available phase-1 screen/declared phase-2 worker complete fan-in
- Verified quality comparator와 structural tie-break 분리
- Same-worker retry identity/digest conflict rejection
- Strict-better warm-start와 plateau/max-round lineage
- Official publication을 막는 `TEST_ONLY` guard

AR-9 entry에는 추가로 `Q-BENCH-02` calibration/explicit approval, compliant integer fixture digest와 AR-8 entry evidence가 필요하다. AR-7 test 숫자, README의 legacy 숫자, GCP timeout/retry 또는 decimal fixture를 official manifest로 복사하지 않는다.

## 13. Scope exclusions와 금지 shortcut

이 phase는 다음을 구현하지 않는다.

- `Q-BENCH-02` 공식 숫자 결정, calibration, Win baseline/challenger verdict
- Win comparator 수식 자체, Win fixture 변환 또는 official manifest 실행
- GCP/AWS/Azure 등 provider adapter, orchestration product, queue/job/storage 선택과 IaC
- HTTP/public API, canonical wire schema, database schema와 external status 승인
- Candidate verifier, result-integrity verifier, finalization/audit 또는 publication CAS의 재구현
- ALNS, initial portfolio 생성 정책, COW state, operator/acceptance와 seed derivation 수식의 재구현
- Legacy controller/workflow/README/GCP/Docker/data migration 또는 cutover
- Optional variant, multi-trip/rotation, route pool/MIP, apply/undo

금지 shortcut:

- Storage prefix에서 발견한 candidate 수를 declared completeness로 간주
- 일부 성공 worker의 minimum/first result를 champion으로 선택
- `double` objective 하나, floating tolerance 또는 completion order로 tie 처리
- Retry를 새 worker/run으로 만들거나 seed/warm start/config/steps 변경
- Same identity의 digest conflict에서 “최근 것” 또는 “더 좋은 것” 선택
- Worker `WATCHDOG_REACHED`, `CANCELLED`, `RESOURCE_LIMIT_REACHED`, `PLATFORM_TIMEOUT`, `FAILED`를 `MAX_STEPS_REACHED`로 변경
- Search cache/solver summary를 verifier PASS나 quality authority로 사용
- `TEST_ONLY` 값을 production config/default/official approval로 승격
- Provider default retry를 logical `AttemptId` audit 대신 사용
- Clock, unordered map/set, thread winner 또는 provider ID를 semantic fingerprint/comparator에 사용

## 14. 문서 작성 세션 검증

이 문서 작성 세션은 source/POM/test/README/GCP/data/progress/다른 phase 문서를 수정하지 않는다. 완료 시 다음을 다시 확인한다.

- 대상 파일과 상위 계획의 phase link path가 일치한다.
- §1~§14 heading이 존재하고 YAML에 phase/RM/status/source baseline/선행 문서가 있다.
- Master §13~§14.4/§15.8, Architecture §11~§14/§17~§19, Domain §14.3~§16과 질문 ID link가 존재한다.
- 작성 전후 네 source design/계획/question-register SHA-256가 같다.
- `git diff --check`와 untracked file용 `git diff --no-index --check`에 whitespace error가 없다.
- 이 세션의 유일한 workspace write가 `docs/codex/phases/phase-07-logical-multi-round-coordinator.md`다.

### 14.1 작성 완료 검증 결과

| 검증 | 결과 |
|---|---|
| Target path | `docs/codex/phases/phase-07-logical-multi-round-coordinator.md` 존재, path-specific status `??` |
| Heading/metadata | `## 1`~`## 14`, `phase=AR-7`, `rm_mapping=RM-6-logical`, `status=BLOCKED`, source baseline과 predecessor metadata 확인 |
| Source heading/link | 필수 Master/Architecture/Domain heading과 상대 link target file 존재 확인 |
| Source drift | Implementation plan, Master, Architecture, Domain, question register SHA-256가 YAML baseline과 모두 동일 |
| Whitespace | `git diff --check` exit 0; `git diff --no-index --check`는 whitespace 진단 0건이며 untracked file 차이 때문에 exit 1 |
| File scope | 이 세션의 `apply_patch` write는 이 파일 하나뿐이다. 검증 중 생성된 다른 phase 파일은 병렬 세션 소유이며 읽기만 수행 |
| Maven baseline | 관리 세션의 격리 phase-00 `mvn test`/`mvn verify` PASS를 사용; shared `target/`에서 Maven 재실행하지 않음 |

최종 source hash와 line count는 최종 응답에서 보고한다. 남은 blocker는 `AR-0`, `AR-5`, `AR-6`의 계획 문서 존재가 아니라 실제 구현 `DONE` evidence 부재다.
