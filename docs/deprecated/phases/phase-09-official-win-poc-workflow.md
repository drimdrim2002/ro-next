---
phase: AR-9
rm_mapping: RM-6-official
title: Official Win PoC workflow
status: BLOCKED
implementation_allowed: false
language: ko
document_role: 승인된 immutable official manifest, exact four-component Win comparator, complete verified multi-round workflow, baseline/challenger record와 deterministic rerun의 테스트 우선 구현 명세
source_baseline:
  captured_at: 2026-07-24T18:38:46+09:00
  git_commit: 523c23e2e13410885b16e974efe40ffe598106ee
  git_branch: codex/domain-design
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
  open_questions:
    path: docs/master-design-open-questions.md
    sha256: 3d6bc496b8df98a10534338828dd7e845b50ea967afa884642403405e613c088
predecessor_documents:
  direct:
    - docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
  transitive:
    - docs/codex/phases/phase-00-baseline-and-build-architecture.md
    - docs/codex/phases/phase-01-input-domain-and-travel.md
    - docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
    - docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
    - docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
    - docs/codex/phases/phase-05-verification-finalization-and-publication.md
    - docs/codex/phases/phase-06-local-application-and-logical-ports.md
    - docs/codex/phases/phase-07-logical-multi-round-coordinator.md
entry_blockers:
  - Q-BENCH-02
  - WIN-OFFICIAL-INTEGER-DU-FIXTURE
---

# AR-9 / RM-6-official — Official Win PoC workflow 구현 명세

## 1. 목적, 현재 판정과 사용 규칙

이 phase의 목적은 승인된 공식 수치와 integer meter/second 계약을 만족하는 fixture만으로 다음 폐쇄된 경로를 구현하는 것이다.

```text
approved calibration + approved compliant fixture
→ immutable official manifest
→ 모든 available phase-1 candidate screen 완료·검증
→ 모든 declared phase-2 worker 완료·검증
→ stable round champion과 warm-start lineage
→ final champion 두 verifier 통과
→ immutable official run record
→ baseline/challenger comparison record
→ identical-manifest deterministic rerun
```

작성 시점 판정은 **`BLOCKED`**다. 구현자는 아래 두 blocker와 `AR-8 DONE` evidence가 모두 해소되기 전에는 AR-9 test file, production file, manifest, baseline 또는 challenger record를 만들기 시작해서는 안 된다.

1. `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`이고 `screenMaxSteps`, phase-2 worker 수, `phase2MaxSteps`, `maxRounds`, watchdog의 공식값이 없다.
2. 현재 `data/win_poc_case.json`은 SHA-256 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이지만 소수 `D/U`를 포함하여 official integer meter/second 계약에 맞지 않는다.
3. 직접 선행 문서 `docs/codex/phases/phase-08-compatibility-migration-and-cutover.md`와 그 구현 evidence가 현재 baseline에는 존재하지 않는다.

이 문서의 Java type, method, JSON field와 repository path는 후속 구현 세션이 임의의 이름을 만들지 않도록 정한 **계획상 제안 API**다. 현재 `REVIEW` 설계가 승인된 public API, wire schema, 저장 schema 또는 provider contract를 뜻하지 않는다. 선행 phase가 같은 책임을 다른 internal 이름으로 승인·구현했다면, 구현자는 코드를 이중화하지 말고 영향 ADR과 이 phase 문서를 같은 변경 단위에서 갱신한 뒤 재개한다.

## 2. Authority, 결정 상태와 충돌 처리

### 2.1 규범 근거

| 근거 | 이 phase가 소비하는 계약 |
|---|---|
| [전체 구현 계획 §9.10](../implementation-plan.md#910-ar-9--rm-6-official--official-win-poc-workflow) | AR-9 목적, 두 entry blocker, first-failing test, deliverable과 종료 조건 |
| [전체 구현 계획 §10](../implementation-plan.md#10-phase-문서-작성-계약) | 이 문서의 13개 작성 항목 |
| [전체 구현 계획 §11](../implementation-plan.md#11-evidence-bundle과-progress-상태) | Evidence bundle 구조와 `BLOCKED`/`DONE` AND gate |
| [전체 구현 계획 §13](../implementation-plan.md#13-현재-blocker와-재개-조건) | `Q-BENCH-02`, 비준수 Win fixture, deferred infrastructure/variant 상태 |
| [Master §14.2](../../master-design.md#142-primary-fixture와-manifest) | Fixture read-only, 현 digest, decimal `D/U` 비준수, manifest/card와 fingerprint equality |
| [Master §14.3](../../master-design.md#143-win-poc-comparator) | 정확한 네 성분, 사전식 비교, route 운영시간 공식, 구조 tie-break의 비품질성 |
| [Master §14.4](../../master-design.md#144-multi-round-official-execution) | Complete worker batch, retry identity, warm-start, `INCOMPLETE`, 정상 종료 |
| [Master §15.8](../../master-design.md#158-rm-6--win-poc-official-workflow와-baseline) | RM-6 entry/deliverable/금지/exit evidence |
| [Domain §14](../../domain-design.md#14-result-metrics와-win-poc) | Operational-time breakdown, exact comparator, lineage field |
| [Domain §16](../../domain-design.md#16-acceptance-evidence) | Numeric/travel/state/result/reproducibility acceptance evidence |
| [Architecture §11~§19](../../architecture-design.md#11-distributed-multi-round-runtime) | Logical state/identity/ports, provider 중립성, artifact/provenance, test/evidence와 build phase |
| [질문 등록부 `Q-BENCH-02`](../../master-design-open-questions.md#q-bench-02) | Protocol만 확정되고 공식 수치는 미승인 |
| [AR-7 phase 문서](phase-07-logical-multi-round-coordinator.md) | 계획상 `LogicalExecutionPlan`, `OfficialAuthority`, `WorkerAssignment`, `MultiRoundCoordinator` handoff 이름; `BLOCKED` draft이므로 승인 authority는 아님 |
| [AR-8 phase 문서](phase-08-compatibility-migration-and-cutover.md) | Direct predecessor의 compatibility matrix, active logical target version, cutover/rollback receipt handoff; `BLOCKED` draft이므로 `DONE` evidence는 아님 |

### 2.2 Authority 순서

충돌은 다음 순서로 처리한다.

1. 채택된 외부 입력·출력 계약과 승인된 Decision Record
2. `APPROVED` Master Design
3. `APPROVED` 상세 설계
4. 현재 `REVIEW` 상태의 Master/Architecture/Domain Design과 전체 구현 계획
5. `master-design-sessions`, 연구 자료와 legacy 구현

현재 1~3의 승인 evidence는 확인되지 않았다. 의미 충돌, 승인 record digest 불일치, 선행 phase API 불일치 또는 manifest 비교 범위 충돌은 코드에서 임의로 해결하지 않는다. `BLOCKED` evidence에 질문, 영향 문서, 마지막 안전 지점과 필요한 승인을 기록한다.

### 2.3 이 phase가 바꾸지 않는 결정

- Win comparator는 모든 고객의 solve objective가 아니라 Win PoC 전용이다.
- `Q-BENCH-02`의 공식값을 README, legacy controller, GCP workflow, 인터뷰 예시 또는 test-local 값에서 가져오지 않는다.
- `Q-INFRA-01`은 `DEFERRED`다. 이 phase는 logical port와 local/fake workflow만 사용하고 provider/product/topology를 선택하지 않는다.
- `Q-VAR-01`, multi-trip/rotation, route pool/MIP는 `DEFERRED`다.
- 두 verifier 중 하나라도 `FAIL` 또는 미완료이면 official result와 benchmark vector를 만들지 않는다.
- Fingerprint가 다른 run 사이에는 quality 승패를 판정하지 않는다.

## 3. 작성 시점 실제 저장소 조사

### 3.1 Git과 toolchain baseline

작성 시점:

```text
git commit: 523c23e2e13410885b16e974efe40ffe598106ee
branch: codex/domain-design
Maven: 3.9.14
Java: 25.0.3 Amazon Corretto
OS: macOS aarch64
```

`git status --short`에는 사용자/다른 세션 소유 변경이 이미 다수 있었다. 설계 문서 수정, `data/`와 `docs/codex/` 등 untracked 파일, 역사 문서와 editor 파일이 포함된다. 이 phase 파일은 작성 전에는 존재하지 않았다. 구현 세션은 clean worktree를 가정하거나 `git clean`, `git reset`, broad restore를 실행해서는 안 된다.

관리 세션 정정에 따라 공통 build baseline은 `AR-0`의 격리 실행 관찰을 사용한다.

```text
Java 25.0.3 / Maven 3.9.14
기존 mvn test: 1 test, 0 failure/error, BUILD SUCCESS
기존 mvn verify: BUILD SUCCESS
```

여러 문서 세션이 같은 working directory의 `target/`에서 Maven을 동시에 실행할 수 있으므로 shade JAR replace 등 transient 실패를 repository baseline 결함, AR-9 blocker 또는 red evidence로 확정하지 않는다. 이 문서 작성 중 같은 legacy test 성공도 확인했지만 공통 판정은 위 격리 baseline을 따른다. 추가 build 실행 없이 링크, source drift, diff와 scope 검증으로 문서 세션을 마무리한다. 기존 1개 test는 `AlnsBatchEngineTest.producesCandidateForIndependentAlnsBatch`뿐이며 RPDPTW official 의미의 evidence가 아니다.

병렬 문서 세션이 작성 중 추가한 phase 파일은 읽기 전용이다. 최종 검토 시 `phase-01`·`phase-02`·`phase-07`·`phase-08`은 `BLOCKED`, `phase-00`·`phase-03`·`phase-04`·`phase-05`·`phase-06`은 `NOT_STARTED` 문서였다. 파일 존재는 선행 phase `DONE` evidence가 아니다.

### 3.2 POM/module graph

현재 repository에는 root `pom.xml` 하나만 있다.

```text
com.ronext:ro-next:0.1.0-SNAPSHOT (jar)
├── google-cloud-workflow-executions
├── google-cloud-storage
├── jackson-databind / jackson-datatype-jsr310
└── junit-jupiter (test)
```

- Java release 25, Maven `[3.9.14,)`, Java `[25,26)` Enforcer가 있다.
- Root는 `packaging=pom` aggregator가 아니라 단일 shaded JAR이다.
- `rpdptw/application`, `adapters/common`, `apps/worker`, `build/test-fixtures` module은 아직 없다.
- Shade main class는 `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer`다.

따라서 이 문서의 target path는 `AR-0`~`AR-8`이 계획대로 완료된 뒤 존재해야 하는 예상 경로다. 선행 module이 없다는 이유로 AR-9가 module 생성 책임을 대신하지 않는다.

### 3.3 Production/test/runtime 현황

현재 production source는 6개 class뿐이다.

| 현재 경로 | 관찰 | AR-9 해석 |
|---|---|---|
| `src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | Seed와 iterations로 하나의 `double objective`를 만드는 placeholder | Exact 네 성분, RPDPTW route, verifier 또는 official record가 아님 |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java` | GCS candidate prefix를 읽어 `double objective` 최솟값을 즉시 선택 | Declared-worker completeness, verification, retry identity, round lineage가 없음 |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java` | `parallelRuns=8`, `iterationsPerRun=5000`, seed fallback을 hidden runtime default로 사용 | Official `Q-BENCH-02` 값으로 승격 금지 |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java` | `SERVICE_MODE=api|worker` 단일 JAR | 목표 app composition root가 아님 |
| `gcp/workflows/optimization.yaml` | `parallelRuns` worker 뒤 `/internal/finalize` 호출 | 일부 object만 있어도 finalize 가능; official workflow 아님 |
| `README.md`, `gcp/README.md`, `gcp/cloudbuild.yaml`, `Dockerfile` | 현 GCP 배포와 예시 수치 | Characterization/rollback 자료일 뿐 provider 승인 아님 |

현재 test는 `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` 하나다. Official manifest, comparator, worker completeness, verifier, record immutability, rerun test는 0개다.

### 3.4 Data 현황

| Artifact | 실제 상태 | 허용 용도 |
|---|---|---|
| `data/win_poc_case.json` | 14,157,512 bytes, 1 line, SHA-256 `ea003bac...b7d7`; 452 orders, 31 vehicles, 205,209 matrix entries | Read-only legacy fixture, decimal rejection test |
| `data/win_poc_case.json`의 `D/U` | 조사 시 decimal lexeme를 가진 `D` 204,756개, `U` 184,119개 | Official 변환·반올림·절삭·baseline 금지 |
| `data/ro_input_json_spec.pdf` | SHA-256 `07b4c3ae...ae2a`; legacy CVRPTW 사례 | Canonical RPDPTW schema 승인 근거가 아님 |

위 개수는 현재 파일의 비준수 상태를 설명하는 inventory일 뿐 새 fixture의 값, 변환법 또는 정답을 제안하지 않는다. 새 integer fixture는 승인된 외부 artifact로 제공되어야 하며 현재 파일에서 생성해서는 안 된다.

## 4. Entry gate, blocker와 재개 절차

### 4.1 AND entry gate

아래가 모두 참이어야 `status`를 `IN_PROGRESS`로 바꾸고 첫 test를 작성할 수 있다.

| Gate | 필요한 evidence | 실패 시 판정 |
|---|---|---|
| `AR-8 DONE` | Phase-08 문서/evidence digest, unresolved compatibility row 0, compatibility matrix·shadow report·cutover/rollback receipt digest, active logical `TARGET_V1` entry와 both-pass artifact identity | `BLOCKED_PREDECESSOR` |
| `Q-BENCH-02 APPROVED` | Calibration corpus와 측정 report, 수치별 명시적 승인, approval artifact digest | `BLOCKED_Q_BENCH_02` |
| Compliant fixture approved | Integer `D/U` fixture, source/provenance, 승인 digest, canonical parse 성공 | `BLOCKED_FIXTURE` |
| AR-1~AR-7 handoff | `SolveSnapshot`, bound profile, solver, both verifier, logical coordinator와 local ports의 exact contract/version | `BLOCKED_HANDOFF` |
| Source drift review | 이 문서 metadata의 네 source hash가 현재 hash와 일치하거나 drift impact가 승인됨 | `BLOCKED_SOURCE_DRIFT` |

### 4.2 `Q-BENCH-02` approval의 최소 내용

Approval은 다음 이름의 값을 **모두** 명시해야 한다. 이 문서는 값을 채우지 않는다.

```text
screenMaxSteps
phase2WorkerCount
phase2MaxSteps
maxRounds
workerWatchdog
calibrationCorpusFingerprint
calibrationReportFingerprint
approvalId
approvalContentDigest
approvedAt
approvedBy
```

`workerWatchdog`은 algorithm quality 종료가 아니라 병리적 실행을 중단하는 safety 설정이어야 한다. 승인 artifact에 값이 없거나 manifest 값과 exact equality가 아니면 official mode는 pre-execution validation에서 실패해야 한다. Test-local 양의 값은 `official=false`, `source=TEST_FIXTURE`로만 허용하고 official approval을 대신할 수 없다.

### 4.3 Compliant fixture approval의 최소 내용

Approval은 새 fixture bytes에 대해 다음을 제공해야 한다.

```text
fixtureArtifactPath
fixtureContentDigest
fixtureSchemaVersion
integerDistanceUnit = METER
integerTimeUnit = SECOND
travelContractVersion
adapterVersion
conversionProvenance
approvalId
approvalContentDigest
```

`conversionProvenance`는 승인자가 제공하는 출처 정보다. 구현자가 기존 decimal `D/U`를 반올림, 절삭, 문자열 parse 또는 generated travel로 조용히 변환해 채우면 gate 실패다. Approved fixture가 현재 read-only fixture와 어떤 관계인지도 approval이 명시해야 하며 이 문서가 추측하지 않는다.

### 4.4 재개 시 고정 제안 repository path

아래는 승인 artifact가 들어올 계획상 고정 경로다. 다른 경로가 승인되면 구현 전에 이 문서와 manifest reference를 같은 변경 단위에서 갱신한다.

```text
data/win_poc_case.integer-v1.json
benchmarks/win-poc/official/v1/approvals/q-bench-02-calibration.json
benchmarks/win-poc/official/v1/approvals/fixture-approval.json
benchmarks/win-poc/official/v1/manifest.json
```

현재 경로가 없다는 사실은 placeholder 값으로 파일을 미리 만들라는 뜻이 아니다. Approval과 fixture bytes가 실제로 제공된 뒤 exact digest를 기록한다.

## 5. 현 상태에서 목표 상태로의 gap

| 영역 | 현 상태 | 목표 상태 | 먼저 실패해야 하는 test |
|---|---|---|---|
| Build/module | 단일 shaded JAR | AR-0~8 module 위 AR-9 owner code/test/profile | Entry gate/source drift check |
| Manifest | 없음, legacy request Map과 hidden default | Approval과 모든 fingerprint를 exact bind한 immutable manifest | `OfficialManifestValidationTest` |
| Fixture | Decimal `D/U` legacy fixture | 승인된 integer meter/second fixture와 digest | `OfficialManifestValidationTest.rejectsCurrentDecimalFixture` |
| Quality | 하나의 `double objective` | 네 non-negative exact integer 성분의 사전식 비교 | `WinComparatorTest` |
| Operational time | 없음 | drive+customer wait+depot wait+service+inter-work rest | `WinQualityVectorFactoryTest` |
| Phase 1 | 없음 | 모든 available initial candidate exact screen 정상 완료·검증 뒤 champion | `OfficialWorkflowIT.requiresEveryAvailableScreenCandidate` |
| Phase 2 | Flat parallel legacy runs | Declared worker completeness, stable fan-in, strict warm-start rounds | `OfficialWorkflowIT.requiresAllWorkersNormalAndVerified` |
| Retry | GCP default retry | 같은 logical worker/seed/warm start/config, attempt만 변경 | `OfficialWorkflowIT.preservesRetryIdentityAndWarmStartLineage` |
| Verification | 없음 | Candidate verifier 후 finalization, result verifier 후 publish/compare | `OfficialWorkflowIT.blocksSingleGateResult` |
| Completion order | GCS list/min order에 간접 의존 | 모든 permutation에서 같은 champion/digest | `OfficialWorkflowPermutationIT` |
| Baseline | 없음 | Explicit approval을 가진 create-once immutable run record | `OfficialRecordStoreContractTest` |
| Challenger | 없음 | 동일 manifest fingerprint의 별도 run record | `OfficialComparisonServiceTest` |
| Rerun | Legacy placeholder seed test 1개 | Same manifest의 trace/champion/result fingerprint exact equality | `OfficialRerunReproducibilityIT` |
| Provider | GCP controller/workflow에 결합 | Application-owned logical port와 local/fake harness | Architecture test와 `rg` gate |

이 phase는 legacy files를 이동·삭제하지 않는다. AR-8이 legacy 처분을 이미 결정했더라도 AR-9는 그 결정을 재개정하지 않는다.

## 6. 정확한 예상 변경·신규·이동 경로

아래 경로는 **향후 AR-9 구현 예상 범위**다. Test path가 production path보다 먼저 나열되고, checklist에서도 먼저 생성한다.

### 6.1 Test와 test fixture — 먼저 생성

| 작업 | 정확한 repository-relative path | 책임 |
|---|---|---|
| 변경 | `build/test-fixtures/pom.xml` | AR-9 test fixture의 test-scope 노출; production leakage 금지 |
| 신규 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testfixture/official/OfficialManifestFixtures.java` | 승인/누락/불일치 manifest builder |
| 신규 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testfixture/official/OfficialRunFixtures.java` | Verified worker, round, result와 corruption builder |
| 변경 | `rpdptw/application/pom.xml` | JUnit/test-fixtures test dependency만 추가 |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/official/WinComparatorTest.java` | 네 성분 exact order와 equality |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/official/WinQualityVectorFactoryTest.java` | Verified metric projection과 운영시간 공식 |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/official/OfficialManifestValidationTest.java` | Approval, fixture, fingerprint, no-default gate |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/official/OfficialComparisonServiceTest.java` | Same-manifest comparison과 quality tie |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/official/OfficialRecordStoreContractTest.java` | Create-once baseline/challenger record |
| 변경 | `adapters/common/pom.xml` | Canonical JSON codec test dependency |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/json/official/OfficialManifestJsonCodecTest.java` | Unknown/missing field, canonical digest, round-trip |
| 변경 | `apps/worker/pom.xml` | Failsafe, `official-win-poc` profile, manifest property validation |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/official/OfficialWorkflowIT.java` | Complete phase-1/phase-2/final gate |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/official/OfficialWorkflowPermutationIT.java` | Completion permutation independence |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/official/OfficialRerunReproducibilityIT.java` | Identical manifest rerun |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/official/WinPocBenchmarkIT.java` | 승인 manifest evidence bundle 생성 |

### 6.2 Production — 해당 test의 red 확인 뒤 생성

| 작업 | 정확한 repository-relative path | 책임 |
|---|---|---|
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/WinQualityVector.java` | 네 non-negative exact 품질 성분 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/WinComparison.java` | `LEFT_BETTER`, `QUALITY_TIE`, `RIGHT_BETTER` |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/WinComparator.java` | Exact 사전식 품질 비교 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/WinQualityVectorFactory.java` | `PublishableResult`의 verifier 재계산 metric만 투영 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/WinQualityVectorException.java` | Raw/single-gate/breakdown/overflow typed rejection |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialExecutionManifest.java` | Raw immutable manifest value |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialFingerprintSet.java` | Required semantic/build/verifier/compatibility fingerprint set |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/CompatibilityHandoffRef.java` | AR-8 matrix/shadow/cutover/rollback/active-version digest refs |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialVerifierContractSet.java` | Candidate/result verifier exact contract refs |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/ValidatedOfficialManifest.java` | Validator만 만들 수 있는 execution capability |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialApprovalBundle.java` | Calibration/fixture approval content와 digest |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialFixtureEvidence.java` | Parser/preparation이 만든 integer compliance와 fingerprint evidence |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialManifestViolation.java` | Typed validation code와 field path |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialManifestValidationException.java` | 정렬된 violation list |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialManifestValidator.java` | Approval equality, compliance와 fingerprint gate |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialWorkflowOutcome.java` | `Completed`/`Incomplete` sealed outcome |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialIncompleteReport.java` | Missing/abnormal/unverified/digest-conflict worker와 last safe boundary |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialWinWorkflow.java` | AR-7 coordinator를 strict official policy로 실행 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialRunRecord.java` | Manifest/result/trace/lineage의 immutable record |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialComparisonRecord.java` | Baseline/challenger와 exact verdict |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/BaselineComparisonApproval.java` | 외부 approval가 참조하는 record digest set |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/ApprovedOfficialComparison.java` | Approval exact validation 뒤만 생성되는 capability |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/official/OfficialComparisonService.java` | 비교 가능성 gate와 comparison record 생성 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/OfficialRecordStore.java` | Create-once content-addressed record port |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/official/OfficialManifestJsonCodec.java` | Internal canonical JSON decode/encode/digest |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/official/OfficialManifestCodecException.java` | Duplicate/unknown/missing/coercion/canonicalization typed error |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/local/official/LocalOfficialRecordStore.java` | Atomic create, same-digest converge, different-digest conflict |
| 신규 | `apps/worker/src/main/java/com/ronext/rpdptw/app/worker/official/OfficialWinPocRunner.java` | Composition root; manifest path를 명시적으로 받아 workflow 실행 |

### 6.3 승인 artifact와 immutable record

| 작업 | 정확한 repository-relative path | 생성/변경 규칙 |
|---|---|---|
| 신규, 외부 제공 | `data/win_poc_case.integer-v1.json` | 기존 fixture에서 구현자가 변환 금지 |
| 신규, 승인 제공 | `benchmarks/win-poc/official/v1/approvals/q-bench-02-calibration.json` | 승인값과 report digest; code가 생성하지 않음 |
| 신규, 승인 제공 | `benchmarks/win-poc/official/v1/approvals/fixture-approval.json` | Fixture bytes/digest와 integer contract approval |
| 신규 | `benchmarks/win-poc/official/v1/manifest.json` | 두 approval과 모든 fingerprint exact bind |
| 신규, 실행 산출 후 승인 | `benchmarks/win-poc/official/v1/records/baseline.json` | Create-once; 자동 overwrite 금지 |
| 신규, rerun 산출 후 승인 | `benchmarks/win-poc/official/v1/records/challenger-rerun.json` | Baseline과 같은 manifest fingerprint |
| 신규 | `benchmarks/win-poc/official/v1/records/comparison.json` | Exact verdict와 두 record digest |
| 신규, 승인 제공 | `benchmarks/win-poc/official/v1/approvals/baseline-and-comparison.json` | Baseline/comparison의 explicit approval |

이 표에 이동 또는 삭제 작업은 없다. `pom.xml`, legacy `src/`, `README.md`, `gcp/`, `Dockerfile`, 기존 `data/win_poc_case.json`, 원본 설계와 다른 phase 문서는 AR-9에서 수정하지 않는다.

## 7. 계획상 제안 package, type와 API

### 7.1 Manifest와 approval

다음은 internal Java 계약의 **계획상 제안 API**다.

```java
package com.ronext.rpdptw.application.execution.official;

public record OfficialExecutionManifest(
        String schemaVersion,
        String manifestId,
        ArtifactRef fixtureRef,
        String fixtureContentDigest,
        String calibrationApprovalDigest,
        String fixtureApprovalDigest,
        SolveSnapshotRef solveSnapshotRef,
        CompatibilityHandoffRef compatibilityHandoffRef,
        OfficialFingerprintSet fingerprints,
        LogicalExecutionPlan logicalExecutionPlan,
        OfficialVerifierContractSet verifierContracts,
        String metricContractVersion,
        String comparatorContractVersion) {}

public final class OfficialManifestValidator {
    public ValidatedOfficialManifest validate(
            OfficialExecutionManifest manifest,
            OfficialApprovalBundle approvals,
            OfficialFixtureEvidence fixtureEvidence)
            throws OfficialManifestValidationException;
}
```

`LogicalExecutionPlan`과 그 안의 `OfficialAuthority`, `PortfolioDeclaration`, phase-2 worker template, seed derivation, step/round/watchdog 값은 AR-7 계획상 handoff를 재사용한다. AR-9는 이를 중복 type으로 다시 만들지 않고, `OfficialAuthority.approvedNumericContractRef`가 이 manifest의 calibration approval과 같은 content digest를 가리키는지 검증한다. AR-7 실제 구현 이름이 다르면 §1의 API mapping 절차를 먼저 수행한다.

`CompatibilityHandoffRef`는 AR-8이 만든 compatibility matrix, shadow report, cutover receipt, rollback rehearsal와 active logical version의 content digest를 참조한다. AR-9는 AR-8의 compatibility/cutover state machine을 복제하지 않고, unresolved blocking row가 0이며 active logical entry가 승인된 target workflow인지 확인한다.

`ValidatedOfficialManifest`의 public constructor/factory는 두지 않는다. Validator 성공만 official execution capability를 만든다. Raw `OfficialExecutionManifest`를 `OfficialWinWorkflow`에 직접 전달할 overload도 두지 않는다.

`OfficialFingerprintSet`은 최소 다음 exact non-blank digest를 가진다.

```text
rawFixture
canonicalInput
problem
preparedTravel
numericPolicy
timePolicy
travelPolicy
adapter
compatibilityMatrix
shadowReport
logicalExecutionVersion
cutoverReceipt
rollbackRehearsal
profile
preset
evaluation
objective
solvePlan
algorithm
operatorRegistry
stateStrategy
seedDerivation
buildRuntime
candidateVerifierContract
resultVerifierContract
metricContract
comparatorContract
```

Approval에서 소유하는 다섯 수치는 manifest와 exact equality여야 한다. 누락값을 default로 채우거나 범위 안이면 승인된 것으로 간주하지 않는다.

### 7.2 Manifest error model

`OfficialManifestViolation.Code`는 최소 다음을 고정 제안한다.

```text
CALIBRATION_APPROVAL_MISSING
CALIBRATION_APPROVAL_DIGEST_MISMATCH
CALIBRATION_VALUE_MISMATCH
FIXTURE_APPROVAL_MISSING
FIXTURE_APPROVAL_DIGEST_MISMATCH
FIXTURE_CONTENT_DIGEST_MISMATCH
FIXTURE_NON_INTEGER_DISTANCE_OR_TIME
FIXTURE_SCHEMA_MISMATCH
REQUIRED_FINGERPRINT_MISSING
FINGERPRINT_MISMATCH
UNAPPROVED_OFFICIAL_VALUE
NON_POSITIVE_STEP_BUDGET
NON_POSITIVE_WORKER_OR_ROUND_COUNT
WATCHDOG_NOT_SAFETY_ONLY
METRIC_CONTRACT_MISMATCH
COMPARATOR_CONTRACT_MISMATCH
VERIFIER_CONTRACT_MISMATCH
DECLARED_CANDIDATE_SET_MISMATCH
COMPATIBILITY_HANDOFF_MISSING
UNRESOLVED_COMPATIBILITY_ROW
LOGICAL_TARGET_VERSION_NOT_ACTIVE
CUTOVER_RECEIPT_MISMATCH
```

Violation은 `code`, canonical `fieldPath`, expected approval digest/value reference, observed digest/value를 가진다. Secret, raw customer bytes 또는 전체 input을 error에 넣지 않는다. Violation list는 code와 field path로 stable sort한다.

### 7.3 Exact 네 성분 Win comparator

```java
package com.ronext.rpdptw.application.execution.official;

public record WinQualityVector(
        long unassignedOrderCount,
        long dispatchedVehicleCount,
        long totalDirectedDistanceMeters,
        long totalRouteOperationalTimeSeconds) {}

public enum WinComparison {
    LEFT_BETTER,
    QUALITY_TIE,
    RIGHT_BETTER
}

public final class WinComparator {
    public WinComparison compare(WinQualityVector left, WinQualityVector right);
}

public final class WinQualityVectorFactory {
    public WinQualityVector from(PublishableResult result);
}
```

불변조건:

1. 네 값은 모두 non-negative checked integer다.
2. 비교는 `Long.compare`를 순서대로 사용하고 subtraction/weighted sum/Big-M/`double`을 사용하지 않는다.
3. 순서는 `unassignedOrderCount → dispatchedVehicleCount → totalDirectedDistanceMeters → totalRouteOperationalTimeSeconds`다.
4. 첫 번째 다른 성분만 승패를 결정한다.
5. 네 값이 모두 같으면 `QUALITY_TIE`다.
6. Canonical solution fingerprint 등의 structural tie-break는 champion을 하나 선택하는 deterministic reduction에만 쓸 수 있고 다섯 번째 품질 성분이나 baseline 승패로 기록하지 않는다.
7. `WinQualityVectorFactory`는 두 verifier `PASS`가 포함된 `PublishableResult`만 받는다. Raw candidate, solver summary, search cache 또는 legacy `double objective`를 받는 overload를 두지 않는다.

`WinQualityVectorException.Code`는 `RESULT_NOT_PUBLISHABLE`, `CANDIDATE_REPORT_NOT_PASS`, `RESULT_REPORT_NOT_PASS`, `DISTANCE_BREAKDOWN_MISMATCH`, `OPERATIONAL_TIME_BREAKDOWN_MISMATCH`, `NEGATIVE_COMPONENT`, `CHECKED_SUM_OVERFLOW`를 최소 집합으로 고정 제안한다.

운영시간은 verifier가 재계산한 breakdown을 checked sum한다.

```text
Σ used routes (
  driveTime
  + customerWaitingTime
  + depotWaitingTime
  + serviceTime
  + interWorkWindowRestTime
)
```

미사용 vehicle idle, route 전 업무 무관 시간, solver/verifier/serialization elapsed를 포함하면 `OPERATIONAL_TIME_BREAKDOWN_MISMATCH`로 official vector 생성을 거부한다.

### 7.4 Complete verified official workflow

```java
package com.ronext.rpdptw.application.execution.official;

public final class OfficialWinWorkflow {
    public OfficialWorkflowOutcome execute(
            ValidatedOfficialManifest manifest,
            RunId runId);
}

public sealed interface OfficialWorkflowOutcome {
    record Completed(OfficialRunRecord runRecord) implements OfficialWorkflowOutcome {}
    record Incomplete(OfficialIncompleteReport report) implements OfficialWorkflowOutcome {}
}
```

`OfficialWinWorkflow`는 AR-7의 `MultiRoundCoordinator`, AR-6 logical ports와 AR-5 verification/publication service를 조립한다. Round/fan-in 알고리즘을 복제하지 않는다.

Official execution 불변조건:

1. Manifest의 declared initial candidate set과 실제 available set을 비교한다. 모든 실제 available candidate는 exact `screenMaxSteps`로 `MAX_STEPS_REACHED`하고 cache-free validation을 통과해야 한다.
2. Fixture 특성상 unavailable candidate가 있을 수 있는지는 manifest가 exact set으로 선언한다. 구현자가 availability를 추측하거나 누락을 성공으로 처리하지 않는다.
3. Phase-1 champion은 Win comparator와 별도의 stable structural tie-break로 선택한다.
4. 각 phase-2 round는 manifest가 선언한 `workerCount`개의 distinct logical `WorkerRunId`를 가진다.
5. 각 worker는 common warm start, declared derived seed, exact `phase2MaxSteps`, 동일 config/fingerprint를 사용하고 `MAX_STEPS_REACHED`와 candidate verifier `PASS`를 모두 만족한다.
6. Retry는 같은 `WorkerRunId`, round, worker ordinal, seed, warm start, requested steps, config를 보존하고 `AttemptId`만 바꾼다.
7. 같은 logical worker의 same digest duplicate는 수렴할 수 있다. Different digest duplicate는 integrity violation으로 전체 run을 `INCOMPLETE` 처리한다.
8. Completion order, thread 수와 physical parallel order는 comparator input order가 아니다.
9. Missing, failed, cancelled, watchdog, resource, platform timeout, non-normal termination 또는 unverified worker가 하나라도 있으면 champion/next round/final official record를 만들지 않는다.
10. Round champion이 previous champion보다 품질상 `LEFT_BETTER`일 때만 다음 round common warm start가 된다. `QUALITY_TIE` 또는 worse면 complete batch 집계 뒤 `NO_STRICT_IMPROVEMENT`로 끝낸다.
11. Declared `maxRounds`를 모두 완료하면 `MAX_ROUNDS_REACHED`다.
12. Final champion은 candidate verifier `PASS` 뒤 finalization/audit, result verifier `PASS`를 거쳐야 한다. Both-pass 전 baseline/challenger/vector publication이 없다.

`OfficialIncompleteReport`는 최소 `MISSING_DECLARED_SCREEN`, `ABNORMAL_SCREEN_TERMINATION`, `UNVERIFIED_SCREEN`, `MISSING_DECLARED_WORKER`, `ABNORMAL_WORKER_TERMINATION`, `UNVERIFIED_WORKER`, `ASSIGNMENT_IDENTITY_CONFLICT`, `DUPLICATE_DIGEST_CONFLICT`, `FINAL_CANDIDATE_VERIFICATION_FAILED`, `FINAL_RESULT_VERIFICATION_FAILED` code와 affected identity, last completed boundary, evidence refs를 stable order로 기록한다. 이 report는 recovery candidate나 official result가 아니다.

### 7.5 Baseline, challenger와 comparison record

```java
public record OfficialRunRecord(
        String recordSchemaVersion,
        String manifestFingerprint,
        RunId runId,
        ArtifactRef finalPublishableResult,
        WinQualityVector quality,
        String canonicalTraceFingerprint,
        String championFingerprint,
        String resultFingerprint,
        CompleteRoundLineage lineage,
        VerifierReportRefs verifierReports) {}

public record OfficialComparisonRecord(
        String recordSchemaVersion,
        String manifestFingerprint,
        ArtifactRef baselineRecordRef,
        String baselineRecordDigest,
        ArtifactRef challengerRecordRef,
        String challengerRecordDigest,
        WinQualityVector baselineQuality,
        WinQualityVector challengerQuality,
        WinComparison verdict,
        StructuralTieMetadata nonQualityTieMetadata) {}

public final class OfficialComparisonService {
    public OfficialComparisonRecord compare(
            OfficialRunRecord baseline,
            OfficialRunRecord challenger);

    public ApprovedOfficialComparison validateApproval(
            OfficialComparisonRecord comparison,
            BaselineComparisonApproval approval);
}

public interface OfficialRecordStore {
    PutIfAbsentResult putIfAbsent(
            OfficialRecordKey key,
            byte[] canonicalBytes,
            String expectedContentDigest);
}
```

비교 불변조건:

- Baseline과 challenger의 `manifestFingerprint`가 exact equality가 아니면 `COMPARISON_NOT_ALLOWED_MANIFEST_MISMATCH`다.
- 현재 authority는 build/runtime fingerprint까지 manifest에 포함한다. 그러므로 이 phase의 최초 challenger는 **같은 approved manifest의 deterministic rerun**이다. 다른 build/algorithm manifest를 품질 비교하려면 상위 authority가 comparison compatibility를 별도로 승인하고 이 문서를 갱신해야 한다.
- Comparison record는 두 record digest, manifest digest, 네 성분 vector, `LEFT_BETTER/QUALITY_TIE/RIGHT_BETTER`와 structural tie-break metadata를 가진다.
- Run/comparison canonical bytes는 자기 content digest를 내부 field로 포함하지 않는다. `ArtifactStore`/`OfficialRecordStore`가 bytes 밖에서 digest를 계산·검증하여 self-hash cycle을 피한다.
- `baseline-and-comparison.json` approval은 baseline/comparison digest를 참조한다. `validateApproval`은 그 외부 approval과 exact digest equality를 검증한 뒤에만 `ApprovedOfficialComparison` capability를 만든다. Raw comparison과 approval이 서로의 digest를 포함하는 순환 구조를 만들지 않는다.
- Record는 create-once다. Same key/same digest는 idempotent convergence, same key/different digest는 conflict다.
- Baseline, challenger 또는 comparison을 자동 덮어쓰거나 mutable `latest`로 선택하지 않는다.
- `approvedAt`, elapsed와 storage locator는 metadata이며 quality vector나 deterministic semantic fingerprint에 넣지 않는다.

### 7.6 JSON codec와 internal schema 지위

`OfficialManifestJsonCodec`는 internal artifact codec의 **계획상 제안 API**다.

```java
public final class OfficialManifestJsonCodec {
    public OfficialExecutionManifest decode(VerifiedArtifactBytes bytes);
    public byte[] encodeCanonical(OfficialExecutionManifest manifest);
    public String contentDigest(byte[] canonicalBytes);
}
```

- Digest를 검증하기 전에 deserialization하지 않는 AR-6 `ArtifactStore` contract를 그대로 사용한다.
- Unknown field, duplicate key, missing required field, numeric coercion, scientific/floating representation을 거부한다.
- Map iteration/classpath discovery order가 bytes를 바꾸지 않도록 canonical field/order encoding을 고정한다.
- 이 JSON은 승인된 public HTTP schema가 아니다. Public API에 노출하거나 provider event type으로 재사용하지 않는다.

`VerifiedArtifactBytes`는 AR-6 `ArtifactStore`가 expected digest 검증 뒤만 발급하는 계획상 capability type이며 동등한 실제 AR-6 type이 있으면 mapping한다. Codec에는 raw `byte[]` decode overload를 두지 않는다. `OfficialManifestCodecException.Code`는 `DUPLICATE_KEY`, `UNKNOWN_FIELD`, `MISSING_REQUIRED_FIELD`, `NUMERIC_COERCION_FORBIDDEN`, `FLOATING_OR_SCIENTIFIC_NUMBER_FORBIDDEN`, `INVALID_UTF8`, `NON_CANONICAL_VALUE`를 최소 집합으로 고정 제안한다.

## 8. 세분화된 test case 명세

각 test는 production change의 선행 조건이다. 첫 red가 의도한 compile/assertion failure가 아니면 production 구현으로 넘어가지 않는다.

| Test class.method | 종류·권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `WinComparatorTest.usesExactFourComponentLexicographicOrder` | Unit; Master §14.3 | 네 성분을 한 번씩만 다르게 한 hand vectors | 앞 성분 차이는 뒤의 극단값으로 뒤집히지 않음 | 최초 API 전 `cannot find symbol WinComparator`; skeleton 후 각 case `expected LEFT_BETTER but was ...` | 4차원 양방향 8 case 통과 |
| `WinComparatorTest.treatsAllFourEqualAsQualityTie` | Unit | 동일 vector, 다른 structural fingerprint | `QUALITY_TIE` | `expected QUALITY_TIE` assertion failure | Fingerprint와 무관하게 tie |
| `WinComparatorTest.doesNotOverflowAtLongBoundaries` | Unit/property | `0`, `Long.MAX_VALUE` 조합 | 올바른 순서, exception 없음 | subtraction 구현 시 잘못된 verdict | `Long.compare` law 통과 |
| `WinQualityVectorFactoryTest.recomputesExactOperationalTimeBreakdown` | Unit; Master §14.3/Domain §14.1 | Used route 2개의 hand breakdown | 다섯 allowed term의 checked sum | expected hand total과 actual 불일치 | Exact total/distance/count |
| `WinQualityVectorFactoryTest.rejectsRawOrSingleGateResult` | Negative; publication gate | Raw candidate, candidate-pass-only result | Typed rejection | Vector가 생성되어 `assertThrows` 실패 | Both-pass만 허용 |
| `WinQualityVectorFactoryTest.excludesElapsedAndUnusedVehicleIdle` | Unit | 큰 elapsed/idle metadata | Quality vector 불변 | elapsed/idle 반영으로 equality 실패 | Metadata mutation 전후 vector 동일 |
| `OfficialManifestValidationTest.requiresApprovedCalibrationAndExactValues` | Unit/governance | Missing approval, digest mismatch, 5개 값 중 하나 mismatch | 해당 violation code | Validator가 허용하거나 hidden default를 채워 `assertThrows` 실패 | 각 corruption이 exact code로 거부 |
| `OfficialManifestValidationTest.rejectsCurrentDecimalFixture` | Integration/negative; Master §14.2 | 현재 `data/win_poc_case.json` | `FIXTURE_NON_INTEGER_DISTANCE_OR_TIME` | 현재 fixture가 validated manifest를 생성 | 현 fixture는 반드시 거부 |
| `OfficialManifestValidationTest.requiresCompliantFixtureDigestAndAllFingerprints` | Unit/negative | Approved integer fixture evidence, one-at-a-time digest/fingerprint corruption | Stable violation list | 누락/mismatch를 허용 | 모든 필드 exact match일 때만 validated |
| `OfficialManifestValidationTest.neverSuppliesOfficialDefaults` | Unit/governance | 수치 field 각각 누락 | `UNAPPROVED_OFFICIAL_VALUE` 또는 missing-field decode failure | README/legacy/test value로 채워짐 | 모든 누락이 pre-execution reject |
| `OfficialManifestValidationTest.requiresActiveApprovedCompatibilityHandoff` | Unit/governance; AR-8 handoff | Missing handoff, unresolved row, stale cutover receipt, active `LEGACY_V1`을 one-at-a-time | 해당 compatibility violation, dispatch 0 | Official manifest validation 또는 dispatch가 성공 | Unresolved 0 + receipt digest match + approved target logical entry만 validated |
| `OfficialManifestJsonCodecTest.canonicalRoundTripHasStableDigest` | Unit/reproducibility | Field order가 다른 같은 manifest bytes | 같은 canonical bytes/digest | Map order로 digest가 다름 | 반복 encode exact bytes equality |
| `OfficialManifestJsonCodecTest.rejectsUnknownDuplicateAndNumericCoercion` | Unit/negative | Unknown field, duplicate key, decimal/string step value | Decode failure with field path | Jackson coercion으로 허용 | 모든 malformed input 거부 |
| `OfficialWorkflowIT.requiresEveryAvailableScreenCandidate` | Integration; Master §14.4 | Test-local explicit 8-candidate manifest와 하나 missing/unverified screen | `INCOMPLETE`, phase-2 dispatch 0 | Champion 또는 phase 2가 생성 | 전 available screen normal+verified 뒤만 진행 |
| `OfficialWorkflowIT.requiresAllWorkersNormalAndVerified` | Integration/fault | 한 round에서 missing/failed/watchdog/cancelled/unverified worker를 one-at-a-time | `INCOMPLETE`, champion/next round/publication 없음 | 일부 success로 champion 생성 | 모든 declared worker만 complete |
| `OfficialWorkflowIT.preservesRetryIdentityAndWarmStartLineage` | Integration/fault | 첫 attempt platform failure, 둘째 success | Attempt만 변경, seed/warm start/config 동일 | Retry identity 중 하나 변경 | Full lineage exact equality |
| `OfficialWorkflowIT.rejectsSameWorkerDifferentDigest` | Corruption/fault | Same `WorkerRunId`, two verified digests | Integrity violation, `INCOMPLETE` | 임의 one-winner 선택 | Same digest만 converge |
| `OfficialWorkflowIT.advancesOnlyOnStrictlyBetterCompleteChampion` | Integration | Better/tie/worse round champion hand vectors | Better만 next warm start; tie/worse normal plateau | Tie가 fingerprint tie-break로 next round | `NO_STRICT_IMPROVEMENT` 정확 |
| `OfficialWorkflowIT.publishesOnlyAfterBothVerifierPass` | Integration/corruption | Final result verifier fail/incomplete | No `OfficialRunRecord`/publication | Candidate pass만으로 record 생성 | Both-pass만 completed |
| `OfficialWorkflowPermutationIT.selectsSameChampionForEveryCompletionPermutation` | Property/reproducibility | 같은 declared workers의 모든 또는 pairwise-complete permutation | Champion/round digest/lineage 동일 | Completion order별 digest 차이 | Permutation equality |
| `OfficialComparisonServiceTest.rejectsDifferentManifestFingerprint` | Unit/governance | 같은 vector, 다른 manifest/build fingerprint | `COMPARISON_NOT_ALLOWED_MANIFEST_MISMATCH` | Quality verdict 생성 | Compare-not-allowed |
| `OfficialComparisonServiceTest.recordsExactFourComponentVerdict` | Unit | Same manifest baseline/challenger hand vectors | Exact verdict, two digests, no fifth dimension | Structural fingerprint가 승패를 바꿈 | Quality tie와 selection tie 분리 |
| `OfficialRecordStoreContractTest.isCreateOnceAndDigestConvergent` | Contract/fault | Same key/same bytes, same key/different bytes, interrupted write | Converge/conflict/atomic absence | Overwrite 또는 partial record | Atomic immutable record |
| `OfficialRerunReproducibilityIT.repeatsTraceChampionAndResultFingerprint` | Reproducibility; Master §13/§14.4 | 승인된 동일 manifest를 격리된 local store에서 2회 | Trace/champion/result/vector exact equality | 어느 fingerprint든 mismatch | ExecutionId/elapsed 제외 semantic equality |
| `WinPocBenchmarkIT.emitsCompleteApprovedEvidenceBundle` | Benchmark/governance | 실제 approved manifest path | Complete evidence, baseline/challenger/comparison candidate bytes | Missing evidence 또는 incomplete run | Approval 전 source record overwrite 없이 bundle 생성 |

Test fixture에 쓰는 수치는 반드시 `OfficialManifestFixtures.testOnly(...)`로 생성하고 `official=false`, `valueSource=TEST_FIXTURE`를 포함한다. 이 수치는 문서, production constant, official manifest와 approval artifact로 복사하지 않는다.

## 9. 강제 테스트 우선 구현 순서

### 9.1 Red evidence 원칙

각 API cluster는 다음 두 단계 red를 보존한다.

1. **API compile red:** Test file을 production file보다 먼저 만들고 지정 명령을 실행한다. `cannot find symbol` 또는 `package ... does not exist`가 예상 class/type을 가리키는지 확인한다.
2. **Semantic assertion red:** 최소 signature skeleton만 추가하고 method body는 명시적 `UnsupportedOperationException("AR-9 not implemented")` 또는 neutral wrong result로 둔다. 다시 실행해 표의 assertion/message가 실패하는지 확인한다.

Dependency download 실패, Java/Maven mismatch, 대상 test 0개, unrelated 선행 test 실패는 유효한 red가 아니다. 그런 경우 production 구현을 시작하지 말고 환경/선행 phase를 복구한다.

### 9.2 구현 순서

1. Entry gate와 source hash를 확인하고 evidence ID를 만든다.
2. `build/test-fixtures`의 두 AR-9 builder를 먼저 작성한다.
3. `WinComparatorTest`, `WinQualityVectorFactoryTest`를 먼저 작성하고 compile red를 보존한다.
4. Comparator/vector signature skeleton만 추가하고 semantic red를 보존한다.
5. Exact comparator와 both-pass metric projection의 최소 구현 후 targeted green을 만든다.
6. `OfficialManifestValidationTest`, `OfficialManifestJsonCodecTest`를 먼저 작성하고 compile red를 보존한다.
7. Manifest/approval/codec skeleton 후 missing approval, 현재 decimal fixture, missing fingerprint의 semantic red를 각각 본다.
8. No-default validator와 canonical codec의 최소 구현 후 targeted green을 만든다.
9. `OfficialWorkflowIT`, `OfficialWorkflowPermutationIT`를 먼저 작성하고 compile red를 보존한다.
10. Workflow outcome/signature skeleton 후 partial-success가 잘못 완료되거나 `AR-9 not implemented`로 실패하는 것을 확인한다.
11. AR-7 coordinator를 재사용하여 phase-1 completeness, phase-2 completeness, retry identity, warm-start와 both-gate 최소 구현 후 targeted green을 만든다.
12. `OfficialComparisonServiceTest`, `OfficialRecordStoreContractTest`를 먼저 작성해 red를 본 뒤 immutable record/store 최소 구현으로 green을 만든다.
13. `OfficialRerunReproducibilityIT`를 먼저 작성해 trace/champion/result mismatch red를 확인하고 stable ordering/digest 원인을 하나씩 최소 수정한다.
14. Application unit/property 전체, adapters/common contract 전체, apps/worker Failsafe 전체를 순서대로 실행한다.
15. Architecture rule과 module `verify`, reactor `mvn verify`를 실행한다.
16. 실제 승인 manifest로 `WinPocBenchmarkIT`를 실행해 source-controlled record가 아니라 `target/codex-evidence/AR-9/...` candidate bundle을 먼저 만든다.
17. Human/authority explicit approval 뒤에만 approved baseline/challenger/comparison bytes를 계획 경로에 create-once로 추가하고 동일 manifest rerun으로 재검증한다.

어떤 단계에서도 “곧 실패할 것이 명백하다”는 이유로 red 실행을 생략하지 않는다. `commands.log`에 red command, exit code, 핵심 compiler/assertion message와 report path를 남기기 전 다음 production file을 만들지 않는다.

## 10. Exact 명령

모든 명령의 working directory는 다음이다.

```bash
cd /Users/brown/workspace/ro-next
```

### 10.1 Preflight와 blocker

```bash
git status --short --untracked-files=all
git rev-parse --verify HEAD
mvn -version
find . -maxdepth 4 -name pom.xml -print | sort
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
shasum -a 256 data/win_poc_case.json
test -f docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
test -f benchmarks/win-poc/official/v1/approvals/q-bench-02-calibration.json
test -f benchmarks/win-poc/official/v1/approvals/fixture-approval.json
test -f data/win_poc_case.integer-v1.json
```

현재 baseline에서는 마지막 네 `test -f` 중 선행 phase/approval/fixture 검사가 실패하는 것이 정상이며, 그 상태에서 아래 AR-9 test/implementation 명령으로 넘어가지 않는다.

### 10.2 Targeted red/green

```bash
mvn -pl rpdptw/application -am \
  -Dtest=WinComparatorTest,WinQualityVectorFactoryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl rpdptw/application,adapters/common -am \
  -Dtest=OfficialManifestValidationTest,OfficialManifestJsonCodecTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl apps/worker -am \
  -Dit.test=OfficialWorkflowIT,OfficialWorkflowPermutationIT \
  -Dsurefire.failIfNoSpecifiedTests=false verify

mvn -pl rpdptw/application,adapters/common -am \
  -Dtest=OfficialComparisonServiceTest,OfficialRecordStoreContractTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -pl apps/worker -am \
  -Dit.test=OfficialRerunReproducibilityIT \
  -Drpdptw.official.manifest=/Users/brown/workspace/ro-next/benchmarks/win-poc/official/v1/manifest.json \
  verify
```

Failsafe report에서 대상 `*IT` 실행 수가 0이면 green이 아니다.

### 10.3 Module, reactor와 official benchmark

```bash
mvn -pl rpdptw/application -am test
mvn -pl rpdptw/application,adapters/common -am verify
mvn -pl apps/worker -am verify
mvn verify

mvn -pl apps/worker -am -Pofficial-win-poc \
  -Dit.test=WinPocBenchmarkIT \
  -Drpdptw.official.manifest=/Users/brown/workspace/ro-next/benchmarks/win-poc/official/v1/manifest.json \
  verify
```

`official-win-poc` profile은 manifest property가 없거나 validation이 실패하면 build를 fail해야 한다. 일반 `mvn verify`는 correctness test를 실행하되 장시간 official benchmark를 자동 실행하지 않아도 된다. Official profile은 correctness/module/reactor verify를 대체하지 않는다.

### 10.4 금지 dependency/value/shortcut 검색

```bash
rg -n 'com\\.google|google\\.cloud|amazon|aws|azure|StepFunctions|Lambda|CloudRun|WorkflowExecutions' \
  rpdptw/core rpdptw/solver rpdptw/verification rpdptw/application

rg -n 'parallelRuns|iterationsPerRun|screenMaxSteps\\s*=|phase2MaxSteps\\s*=|workerCount\\s*=|maxRounds\\s*=|watchdog\\s*=' \
  rpdptw adapters apps benchmarks

rg -n 'double objective|comparingDouble|Map<String,\\s*Object>|latest|System\\.nanoTime|Math\\.random|new Random\\(' \
  rpdptw/application apps/worker

rg -n 'data/win_poc_case\\.json' \
  rpdptw adapters apps benchmarks

rg -n 'WinComparator|OfficialWinWorkflow|OfficialRecordStore' \
  rpdptw adapters apps build
```

첫 세 명령의 match는 test corruption fixture, explicit rejection, legacy characterization 또는 adapter edge가 아니면 gate 실패다. 기존 decimal fixture path는 rejection test 외 official production/resource에서 0건이어야 한다.

### 10.5 Link, heading, diff와 changed-scope 검사

```bash
test -f docs/codex/phases/phase-09-official-win-poc-workflow.md
test -f docs/codex/implementation-plan.md
test -f docs/master-design.md
test -f docs/architecture-design.md
test -f docs/domain-design.md
test -f docs/master-design-open-questions.md

rg -n '^## ' docs/codex/phases/phase-09-official-win-poc-workflow.md
rg -n 'AR-9|RM-6-official|Q-BENCH-02|WIN-OFFICIAL-INTEGER-DU-FIXTURE|BLOCKED|DONE' \
  docs/codex/phases/phase-09-official-win-poc-workflow.md

git diff --check -- docs/codex/phases/phase-09-official-win-poc-workflow.md
git diff --no-index --check /dev/null docs/codex/phases/phase-09-official-win-poc-workflow.md
git diff --name-only
git status --short --untracked-files=all
```

Implementation 세션은 preflight `git status --porcelain=v1 --untracked-files=all`을 evidence에 저장하고 종료 status와 비교한다. AR-9 예상 경로 밖의 새 변화는 원복하지 말고 사용자/다른 세션 변경인지 판정하여 자기 변경만 분리한다.

## 11. Step-by-step implementation checklist

### 11.1 Gate와 red

- [ ] Metadata의 source hash를 현재 파일과 대조했다.
- [ ] `AR-8 DONE` evidence ID/digest와 exact handoff API를 기록했다.
- [ ] `Q-BENCH-02` approval ID/content digest와 다섯 승인값을 기록했다.
- [ ] Compliant integer fixture bytes/digest/approval을 기록했다.
- [ ] 현재 decimal fixture를 변환·수정·official input으로 사용하지 않았다.
- [ ] Pre-existing working-tree status를 evidence에 저장했다.
- [ ] Test fixture production leakage가 없는지 확인했다.
- [ ] Comparator/vector test를 production file보다 먼저 만들고 compile red를 보존했다.
- [ ] Manifest/codec test를 production file보다 먼저 만들고 compile red를 보존했다.
- [ ] Workflow/fault/permutation test를 production file보다 먼저 만들고 compile red를 보존했다.
- [ ] Record/rerun test를 production file보다 먼저 만들고 compile red를 보존했다.
- [ ] 각 cluster에서 skeleton 뒤 semantic assertion red도 보존했다.

### 11.2 최소 구현

- [ ] 네 성분은 non-negative checked integer이며 exact 순서로만 비교한다.
- [ ] Structural tie-break를 품질 차원과 분리했다.
- [ ] Operational time은 다섯 allowed term만 합산한다.
- [ ] Raw/single-gate result에서 vector 생성을 막았다.
- [ ] Manifest는 approval과 exact equality이며 hidden default가 없다.
- [ ] AR-8 compatibility matrix/receipt/active target entry digest를 exact 검증한다.
- [ ] Validator만 `ValidatedOfficialManifest`를 만들 수 있다.
- [ ] Codec은 digest-before-deserialization, unknown/duplicate/coercion rejection과 canonical bytes를 보장한다.
- [ ] Phase-1의 모든 available candidate 정상 완료·검증을 요구한다.
- [ ] 각 round의 모든 declared worker 정상 완료·검증을 요구한다.
- [ ] Retry identity와 duplicate digest convergence/conflict를 구현했다.
- [ ] Completion order와 concurrency가 champion에 영향을 주지 않는다.
- [ ] Strictly better일 때만 next-round warm start를 바꾼다.
- [ ] Incomplete run은 finalization/publication/benchmark record를 만들지 않는다.
- [ ] Final champion의 candidate/result 두 verifier `PASS`를 확인한다.
- [ ] Baseline/challenger/comparison은 create-once다.
- [ ] Different manifest fingerprint 비교를 거부한다.

### 11.3 Green, regression과 approval

- [ ] 모든 targeted test가 green이고 Surefire/Failsafe report에 대상 test 수가 0이 아니다.
- [ ] `rpdptw/application` unit/property suite가 green이다.
- [ ] `adapters/common` codec/record contract가 green이다.
- [ ] `apps/worker` integration/fault/reproducibility suite가 green이다.
- [ ] Module verify와 reactor `mvn verify`가 green이다.
- [ ] Architecture/provider/customer/hidden-value search 위반이 0이다.
- [ ] Official benchmark evidence bundle이 complete하다.
- [ ] Identical-manifest two-run trace/champion/result/vector가 exact equality다.
- [ ] Baseline/comparison explicit approval artifact가 있다.
- [ ] Source-controlled official record를 자동 overwrite하지 않았다.
- [ ] `git diff --check`, source drift, changed-scope 검사를 통과했다.

## 12. Deliverables와 evidence bundle

### 12.1 Deliverables

| Deliverable | Identity/evidence | Downstream consumer |
|---|---|---|
| `ValidatedOfficialManifest` | Manifest content digest + calibration/fixture approval digest | `OfficialWinWorkflow` |
| Exact `WinComparator` | Comparator contract version + hand/property report | Coordinator와 comparison service |
| `OfficialRunRecord` | Same manifest, complete lineage, both verifier refs, trace/champion/result digest | Baseline/challenger record |
| `OfficialComparisonRecord` | Baseline/challenger digest + exact quality verdict | Approval/regression review |
| Deterministic rerun record | Run A/B semantic fingerprint equality | AR-10 representative workload 선택 |
| Complete official evidence | Evidence bundle digest와 explicit approval | Progress/DONE 판정 |

### 12.2 Evidence bundle

```text
target/codex-evidence/AR-9/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── comparator-compile-red.txt
│   ├── comparator-semantic-red.txt
│   ├── manifest-red.txt
│   ├── workflow-red.txt
│   └── record-rerun-red.txt
├── green/
│   ├── targeted-surefire/
│   └── targeted-failsafe/
├── regression/
│   ├── application-verify/
│   ├── adapters-common-verify/
│   ├── worker-verify/
│   └── reactor-verify/
├── fingerprints/
│   ├── calibration-approval.sha256
│   ├── fixture-approval.sha256
│   ├── fixture.sha256
│   ├── manifest.sha256
│   ├── solve-snapshot.json
│   ├── baseline-record.sha256
│   ├── challenger-rerun-record.sha256
│   └── comparison-record.sha256
├── faults/
│   ├── worker-completeness.json
│   ├── retry-identity.json
│   ├── duplicate-digest-conflict.json
│   └── both-gate-publication.json
├── reproducibility/
│   ├── run-a.json
│   ├── run-b.json
│   └── exact-diff.txt
├── benchmark/
│   ├── phase-one-lineage.json
│   ├── round-lineage.json
│   ├── quality-vector.json
│   ├── performance-metadata.json
│   └── approval-reference.json
├── diff/
│   ├── git-diff-check.txt
│   ├── changed-files.txt
│   ├── forbidden-search.txt
│   └── dependency-graph.txt
└── handoff.md
```

`evidence.json`은 전체 구현 계획 §11의 공통 필드와 함께 다음을 반드시 가진다.

```text
phaseId = AR-9
rmMapping = RM-6-official
status
sourceDesignVersions
sourceDesignHashes
predecessorEvidenceIds
gitCommitOrTree
javaVersion
mavenVersion
calibrationApprovalId/digest
fixtureApprovalId/digest
manifestFingerprint
testCounts
allDeclaredWorkersComplete
candidateVerifierPass
resultVerifierPass
baselineRecordDigest
challengerRecordDigest
comparisonRecordDigest
rerunExactEquality
blockers
createdAt
```

Elapsed와 timestamp는 metadata이며 quality vector나 semantic fingerprint에 넣지 않는다.

## 13. Rollback과 사용자 변경 보존

1. AR-9는 logical official mode를 추가할 뿐 provider deployment나 production traffic을 바꾸지 않는다.
2. 구현 rollback 단위는 AR-9 test/production/POM/profile/resource 변경만 포함한 독립 commit 또는 patch다. `AR-0`~`AR-8`, legacy source, README/GCP/data 원본과 사용자 변경을 함께 되돌리지 않는다.
3. `OfficialWinPocRunner` wiring을 제거해도 generic local solve, AR-7 logical coordinator와 AR-8 compatibility path는 그대로 동작해야 한다.
4. Source-controlled baseline/challenger/comparison record는 overwrite하거나 삭제해 rollback하지 않는다. 잘못 승인된 artifact는 새 revocation/decision record와 새 version directory로 무효화하고 원 bytes/digest를 audit용으로 보존한다.
5. Publication pointer가 생기는 경우 AR-6의 CAS와 AR-8 rollback pointer를 사용한다. 다른 digest를 같은 key에 쓰지 않는다.
6. Partial benchmark 실행의 `target/codex-evidence`는 publishable record가 아니다. 실패 evidence로 보존하거나 명시적으로 폐기하되 정상 baseline 경로에 복사하지 않는다.
7. Working tree가 preflight와 다를 때 broad restore/reset/clean을 하지 않는다. AR-9가 직접 만든 파일만 path 단위로 분리하고 overlap은 사용자에게 보고한다.

## 14. DONE/BLOCKED 판정

### 14.1 현재 `BLOCKED`

현재 판정은 다음 세 이유로 `BLOCKED`다.

```text
BLOCKED_Q_BENCH_02:
  공식 screenMaxSteps/workerCount/phase2MaxSteps/maxRounds/watchdog 승인 없음

BLOCKED_FIXTURE:
  승인된 compliant integer D/U Win fixture와 새 digest 없음

BLOCKED_PREDECESSOR:
  AR-8 및 AR-0~AR-7 phase 구현/evidence가 현재 baseline에 없음
```

이 상태에서 허용되는 것은 이 계획 문서, blocker evidence 검토와 approval/fixture 수령뿐이다. Official test/manifest/baseline의 test-local 또는 guessed 구현은 허용되지 않는다.

### 14.2 `DONE` AND gate

다음을 모두 만족해야 `DONE`이다.

1. `AR-8`과 모든 transitive predecessor가 실제 `DONE` evidence를 가진다.
2. `Q-BENCH-02` calibration approval과 compliant integer fixture approval이 존재하고 manifest와 digest/value가 exact equality다.
3. 이 문서의 test/production/resource deliverable이 실제 path에 존재한다.
4. 각 production behavior에 compile red와 semantic assertion red, targeted green evidence가 있다.
5. Exact four-component comparator hand/property test가 통과한다.
6. Phase-1 모든 available candidate와 모든 phase-2 declared worker가 정상 완료·검증된다.
7. Retry/duplicate/completion permutation/incomplete/both-gate fault matrix가 통과한다.
8. Final champion이 candidate verifier와 result-integrity verifier 모두 `PASS`다.
9. Baseline, challenger-rerun, comparison record와 explicit approval evidence가 create-once로 존재한다.
10. Identical approved manifest 재실행의 canonical trace, champion, result fingerprint와 quality vector가 exact equality다.
11. Targeted test, module verify, reactor verify와 architecture/forbidden search가 통과한다.
12. Evidence bundle과 digest가 완결되고 blocker가 0이다.
13. 다음 phase가 official workload를 실제 검증해 소비할 수 있다.

Source/test/mock/demo 또는 일부 worker 결과가 존재하는 것만으로 `DONE`이 아니다.

### 14.3 즉시 중단 조건

- README의 `8/5000`, legacy controller clamp/default, GCP timeout/retry 또는 인터뷰 예시를 official 수치로 사용하려는 경우
- 현재 decimal fixture를 반올림·절삭·문자열 변환하여 official fixture로 만들려는 경우
- Missing approval/fingerprint를 default나 `latest`로 채우려는 경우
- 일부 successful worker로 champion/next round/baseline을 만들려는 경우
- Watchdog/cancel/resource/platform timeout을 `MAX_STEPS_REACHED`로 바꾸려는 경우
- Candidate/result verifier 중 하나를 생략하거나 solver cache/summary를 신뢰하려는 경우
- 다른 manifest/build fingerprint 사이에 quality verdict를 만들려는 경우
- Provider SDK/type/URI를 application/core official API에 넣어야 진행할 수 있다고 판단한 경우
- Optional variant, multi-trip/rotation, route pool/MIP 또는 apply/undo를 편의상 함께 구현하려는 경우

## 15. 다음 phase handoff

### 15.1 AR-10 / RM-7에 넘길 것

AR-9가 `DONE`이면 AR-10은 다음 artifact를 선택적으로 대표 workload로 소비할 수 있다.

```text
approved manifest ref/digest
compliant fixture ref/digest
solve snapshot/profile/config/build fingerprint
verified final champion ref
canonical trace/result fingerprint
phase/round/worker lineage
performance metadata
AR-9 evidence bundle digest
```

AR-10은 quality vector나 official 수치를 COW 전환 threshold로 재해석하지 않는다. Profiling on/off가 AR-9 trace/result fingerprint를 바꾸지 않는지 검증하고 `KEEP_COW` 또는 별도 제안만 만든다.

### 15.2 AR-11 / RM-9에 넘기지 않는 것

Official completion은 physical provider/product/deployment topology를 자동 승인하지 않는다. AR-11은 `Q-INFRA-01` resume evidence와 별도 scope approval가 있어야만 시작한다. AR-9 manifest의 logical worker/round contract는 보존 대상이지 특정 provider mapping 지시가 아니다.

### 15.3 Handoff 문서 최소 내용

`target/codex-evidence/AR-9/<evidence-id>/handoff.md`에는 다음을 기록한다.

- Exact predecessor evidence ID/digest
- Approval/fixture/manifest/baseline/challenger/comparison digest
- Public이 아닌 계획상 internal API의 실제 구현 이름과 차이
- Test builder 사용법과 test-local value 격리
- Reproducibility run A/B의 제외 metadata와 exact equality 필드
- Known risk, rollback 단위와 unresolved item
- AR-10 entry에 사용할 artifact path와 검증 명령

## 16. Scope exclusions와 금지 shortcut

이 phase는 다음을 구현하지 않는다.

- `Q-BENCH-02` calibration experiment 자체 또는 공식값 결정
- Decimal Win fixture의 integer 변환, 보정, rounding 또는 정답 route 생성
- Canonical public HTTP/wire/database schema 승인
- Physical provider adapter, orchestration product, queue, storage, IAM, deployment와 IaC
- GCP workflow/controller의 production 교체 또는 배포
- Optional variant, MDVRP/OVRP/SDVRP, multi-trip/rotation
- Route pool, MIP, column generation 또는 academic benchmark expansion
- COW→apply/undo 구현/전환
- Customer solve objective를 Win comparator로 교체
- Seed ranking, “좋은 seed” 선정 또는 seed별 no-worse hard gate
- Wall-clock quality deadline, worker 중간 plateau 종료
- Recovery candidate를 정상 official completion으로 표시
- One-verifier publication, partial-success champion, mutable baseline와 `latest`

금지 shortcut:

1. Legacy `double objective`를 네 성분 comparator adapter로 감싸기
2. Worker count만 확인하고 identity/termination/verifier/digest completeness를 생략하기
3. Structural fingerprint를 다섯 번째 quality dimension으로 쓰기
4. Elapsed/CPU/memory를 quality vector에 넣기
5. Manifest validation 뒤 mutable config/profile을 다시 읽기
6. Digest 검증 전에 JSON deserialize하기
7. Same worker different digest에서 먼저 완료된 것을 선택하기
8. Test-local 값이나 fake approval을 official source tree에 복사하기
9. `target` evidence를 explicit approval 없이 baseline record로 승격하기
10. Blocker를 `SKIPPED` test나 placeholder manifest로 숨기기
