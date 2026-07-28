# AR-8 — Compatibility migration과 logical cutover 구현 계획

```yaml
phase: AR-8
rm_mapping: RM-8-cutover
status: BLOCKED
document_status: READY_FOR_REVIEW
document_path: docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
last_updated: 2026-07-24
working_directory: /Users/brown/workspace/ro-next
scope:
  includes:
    - legacy endpoint/worker/finalize characterization
    - versioned common adapter와 alias/coercion provenance
    - semantic compatibility matrix와 side-effect-free shadow comparison
    - idempotency/cancellation/status/retrieval 의미 보존
    - logical version cutover와 recoverable rollback
  excludes:
    - provider/product 선택
    - GCP 또는 다른 production topology/deployment 변경
    - official Win PoC 수치·baseline·challenger 판정
source_baseline:
  git:
    branch: codex/domain-design
    head: 523c23e2e13410885b16e974efe40ffe598106ee
    working_tree: dirty
    note: 기존 수정·미추적 파일은 사용자 또는 다른 문서 세션 소유이며 이 phase 문서 작성에서 변경하지 않음
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
  current_build:
    shape: single Maven jar
    root_pom_sha256: f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6
    java: 25.0.3-amzn
    maven: 3.9.14
    baseline_test: "PASS — 1 test, 0 failures, 0 errors, 0 skipped"
    baseline_verify: "PASS — shade overlap warnings 존재"
    baseline_authority: 관리 세션이 전달한 격리 phase-00 실행 관찰
    shared_target_maven_runs: 동시 실행 transient 충돌 가능성이 있어 baseline/blocker 판정에 사용 금지
  fixtures:
    win_poc_case:
      path: data/win_poc_case.json
      sha256: ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7
      official_use: prohibited
    legacy_input_spec:
      path: data/ro_input_json_spec.pdf
      sha256: 07b4c3ae910b802914d5e45fb3f0699097b73c8bcb3946dabeaaff94e68aae2a
      authority: characterization_only
predecessor_documents:
  - phase: AR-5
    path: docs/codex/phases/phase-05-verification-finalization-and-publication.md
    required_status: DONE
    observed_at_authoring: NOT_PRESENT
    observed_at_final_validation: NOT_STARTED
  - phase: AR-6
    path: docs/codex/phases/phase-06-local-application-and-logical-ports.md
    required_status: DONE
    observed_at_authoring: NOT_PRESENT
    observed_at_final_validation: NOT_STARTED
  - phase: AR-7
    path: docs/codex/phases/phase-07-logical-multi-round-coordinator.md
    required_status: DONE
    observed_at_authoring: NOT_PRESENT
    observed_at_final_validation: BLOCKED
entry_gate:
  required: [AR-5_DONE, AR-6_DONE, AR-7_DONE]
  observed: unmet
  implementation_start: prohibited
```

## 1. 문서 목적과 사용 규칙

이 문서는 현재 `com.ronext.optimizer` legacy 동작을 read-only로 고정하고, 검증된 target application을 versioned adapter와 logical port 뒤에서 비교한 뒤, artifact를 잃지 않고 되돌릴 수 있는 논리 cutover를 구현하기 위한 실행 명세다. 이 문서 자체는 code, public API, wire schema, provider topology 또는 cutover 승인을 만들지 않는다.

후속 구현 세션은 다음 규칙을 지킨다.

1. `AR-5`, `AR-6`, `AR-7`의 실제 phase 문서와 evidence bundle이 모두 `DONE`인지 먼저 확인한다. 하나라도 없거나 digest를 재검증할 수 없으면 production/test 작업을 시작하지 않고 `BLOCKED`로 끝낸다.
2. Legacy characterization suite는 먼저 green이어야 한다. 그 뒤 새 target behavior의 test를 production file보다 먼저 추가하고, 이 문서가 지정한 이유로 red가 된 것을 보지 못하면 production 구현을 시작하지 않는다.
3. 이 문서의 Java 이름과 signature는 모두 **계획상 제안 API**다. 승인된 public HTTP field, route, status, error body, 저장 schema 또는 외부 version label로 인용하지 않는다.
4. Shadow는 business publication pointer, 정상 status, idempotency record와 cancellation record를 변경하지 않는다. 격리된 test/evidence artifact를 만드는 것만 허용한다.
5. Cutover와 rollback은 logical version pointer의 CAS 변경이다. 기존 immutable artifact, verifier report, idempotency record와 legacy module을 삭제하거나 덮어쓰지 않는다.
6. Physical provider cutover, GCP resource/IAM/workflow/image 변경은 이 phase 밖이다.

## 2. Authority, 결정 상태와 blocker

### 2.1 Authority 순서

현재 Master, Architecture, Domain과 상위 구현 계획은 모두 `REVIEW`다. 충돌은 다음 순서로 처리한다.

1. 채택된 외부 input/output 계약과 승인된 Decision Record
2. `APPROVED` Master Design
3. `APPROVED` 상세 설계
4. 현재 `REVIEW` Master/Architecture/Domain Design과 `implementation-plan.md`
5. Legacy code, README, GCP 자료, PDF, fixture와 역사 문서

현재 1~3의 승인 상태가 확인되지 않았다. 따라서 legacy 관찰을 목표 public API로 승격하거나, 이 문서의 제안 type을 승인된 API로 표현해서는 안 된다. 의미 충돌은 compatibility matrix에서 분류하고 승인 reference가 생길 때까지 cutover를 차단한다.

### 2.2 이 phase의 규범 근거

| 근거 | 이 phase가 소비하는 계약 |
|---|---|
| [Master §15.10](../../master-design.md#1510-rm-8--logical-port-integration과-compatibility-migration) | Logical port integration, versioned legacy adapter, shadow/cutover/rollback, idempotency/cancellation/status/retrieval |
| [Master §16.2](../../master-design.md#162-migration) | Characterization → matrix → logical port 격리 → provenance → shadow → both-gate → versioned cutover/rollback 순서 |
| [Architecture §2](../../architecture-design.md#2-current-state-inventory와-migration-기준) | 현재 code/GCP는 목표 증거가 아니라 characterization 대상 |
| [Architecture §6.6](../../architecture-design.md#66-adapter와-app-modules) | JSON/local은 `adapters/common`, API/worker는 composition root, provider adapter는 deferred |
| [Architecture §10~§12](../../architecture-design.md#10-localsingle-run-runtime) | Local reference runtime, logical state/idempotency, provider-neutral port |
| [Architecture §13~§17](../../architecture-design.md#13-physical-topology와-provider-boundary) | Physical topology 보류, artifact/CAS/provenance, retry/cancellation/status 의미 |
| [Architecture §19](../../architecture-design.md#19-maven-build-order와-implementation-phases) | `AR-8 / RM-8-cutover` module·gate와 reactor 순서 |
| [Domain §3, §4, §13, §15~§17](../../domain-design.md#3-계층과-단방향-책임) | Adapter 의미, alias 충돌, 두 verifier, typed error, deferred physical boundary |
| [구현 계획 §4](../implementation-plan.md#4-보존-characterization와-migration-전략) | Legacy 보존·이동 전후 characterization, AR-8 전 정상 경로 교체 금지 |
| [구현 계획 §9.9](../implementation-plan.md#99-ar-8--rm-8-cutover--compatibility-migration과-logical-cutover) | AR-8 범위, first failing tests, deliverable와 exit evidence |
| [구현 계획 §10](../implementation-plan.md#10-phase-문서-작성-계약) | 이 phase 문서의 13개 작성 계약 |

### 2.3 현재 blocker와 영향

| Blocker | 현재 관찰 | 차단 범위 | 재개 조건 |
|---|---|---|---|
| `B-AR8-ENTRY-05` | 최종 검증 시 phase 문서는 `NOT_STARTED`, `AR-5 DONE` evidence는 없음 | `PublishableResult`, both-pass gate를 쓰는 모든 target result mapping/publication | AR-5 문서 `DONE`, candidate/result verifier report와 publishable artifact digest 검증 |
| `B-AR8-ENTRY-06` | 최종 검증 시 phase 문서는 `NOT_STARTED`, `AR-6 DONE` evidence는 없음 | Submission/status/artifact/CAS/cancellation/retrieval port integration | AR-6 문서 `DONE`, local port contract와 end-to-end evidence 검증 |
| `B-AR8-ENTRY-07` | 최종 검증 시 phase 문서는 `BLOCKED`, `AR-7 DONE` evidence는 없음 | Retry identity, declared-worker completeness, logical workflow shadow/cutover | AR-7 문서 `DONE`, coordinator identity/fault evidence 검증 |
| `B-AR8-REACTOR` | 현재 root는 단일 `jar`; target module/POM이 없음 | 이 문서의 `-pl` Maven 명령과 제안 경로 구현 | AR-0 이후 target reactor가 계획상 경로로 존재하고 `mvn verify` 통과 |
| `B-AR8-PUBLIC-CONTRACT` | 승인된 public HTTP/wire/error/version-selection 계약이 확인되지 않음 | Public route 교체, status/error body 호환 주장, intentional break 활성화 | 각 matrix row에 승인된 external contract/ADR digest 연결 |

`Q-BENCH-02`의 공식 수치와 Win fixture의 decimal `D/U`는 AR-9 official 실행 blocker이며 AR-8의 provider-neutral compatibility code 자체를 막지 않는다. 단, 그 값이나 fixture를 AR-8 default, official shadow baseline 또는 cutover 승인 근거로 사용할 수 없다.

`Q-INFRA-01`은 `DEFERRED`다. 이는 logical cutover의 blocker가 아니라 physical provider 작업을 이 phase에서 하지 못하게 하는 scope fence다. `Q-VAR-01`도 이 phase에서 질문하거나 활성화하지 않는다.

## 3. 작성 시점 실제 저장소 조사

### 3.1 조사 명령과 baseline

작성 시점에 다음 inventory 명령을 실행했다. Maven baseline 판정은 shared `target/`을 사용하는 병렬 문서 세션의 실행 결과가 아니라, 관리 세션이 전달한 격리 phase-00 관찰을 authority로 사용한다.

```bash
cd /Users/brown/workspace/ro-next
git status --short
git branch --show-current
git rev-parse HEAD
rg --files -g '!target/**' -g '!docs/.obsidian/**' | sort
find . -name pom.xml -not -path './target/*' -not -path './node_modules/*' -print | sort
sha256sum docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md
sha256sum pom.xml README.md Dockerfile gcp/README.md gcp/cloudbuild.yaml gcp/workflows/optimization.yaml
sha256sum data/ro_input_json_spec.pdf data/win_poc_case.json
mvn -version
mvn test
mvn verify
```

관찰 결과:

- Branch/HEAD는 `codex/domain-design` / `523c23e2e13410885b16e974efe40ffe598106ee`다.
- Working tree에는 이 문서 작업 전부터 tracked 수정·삭제와 여러 untracked file이 있다. 전부 사용자/다른 세션 소유로 취급한다.
- Repository-owned POM은 root `pom.xml` 하나뿐이며 `packaging` 생략으로 `jar`다.
- 격리 phase-00 관찰은 Java 25.0.3-amzn, Maven 3.9.14, 기존 `mvn test`/`mvn verify` 성공이다. 실제 legacy test는 `AlnsBatchEngineTest` 1개뿐이다.
- Shared working directory에서 다른 phase 문서 세션이 Maven을 동시에 실행할 수 있으므로 shade JAR replace 등 transient 실패를 repository baseline 결함이나 AR-8 blocker로 기록하지 않는다.
- 최초 inventory 때 `docs/codex/phases/`의 AR-5/6/7 문서는 없었다. 병렬 문서 세션 완료 뒤 최종 read-only 점검에서는 AR-5 `NOT_STARTED`, AR-6 `NOT_STARTED`, AR-7 `BLOCKED` 문서가 존재하지만 `DONE` implementation evidence는 없다.

### 3.2 현재 build/source/test/runtime

| 영역 | 현재 존재 | AR-8 해석 |
|---|---|---|
| Build | Root `pom.xml` 한 개, Google Workflows/Storage SDK와 Jackson을 root compile classpath에 포함, shade main 지정 | Target reactor와 port가 아직 없음. Current POM은 characterization 대상 |
| Production | `src/main/java/com/ronext/optimizer/**` 6개 class | `legacy/current-app`으로 내용 보존 이동될 대상. Target base namespace가 아님 |
| Test | `AlnsBatchEngineTest` 1개 | Endpoint/worker/finalize, retry/idempotency/cancel/status/retrieval coverage가 없음 |
| API | `OptimizationApiController`가 Storage/Executions SDK client를 직접 생성 | Provider와 application 의미 결합. 목표 architecture 증거가 아님 |
| Worker | `OptimizationWorkerController`가 engine 결과를 GCS key에 직접 저장 | Immutable artifact/digest/CAS/verification이 없음 |
| Finalize | Prefix 아래 발견한 후보 중 `double objective` 최솟값 | Declared completeness, stable logical identity와 두 verifier가 없음 |
| Runtime | `SERVICE_MODE=api|worker`로 한 shaded JAR을 재사용 | Target `apps/api`, `apps/worker` composition과 다름 |
| Workflow | `parallelRuns` HTTP 호출 뒤 `/internal/finalize` | 일부-success, retry identity, complete round semantics를 보장하지 않음 |
| Data | Win JSON 14,157,512 bytes, 452 orders, 31 vehicles, decimal `D/U` 다수 | Read-only characterization fixture. Official input/baseline 금지 |
| Legacy PDF | 10-page CVRPTW JSON spec | 표·sample·상위 계약이 충돌. Canonical/public schema 권위가 아님 |

현재 source hash 중 AR-8 drift 확인에 사용할 값은 다음과 같다.

| Path | SHA-256 |
|---|---|
| `src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | `4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java` | `3dcab11fcac78bb994b1f77bedaf425601684e8b9b3eb1560c305dcf671f358c` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java` | `846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c` |
| `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java` | `2284c20ad0a0d8a957bf7bc4fe5c4a352c2f923c1f56a9023f1389910b28667c` |
| `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` | `947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829` |

AR-0가 이 source를 `legacy/current-app`으로 이동했다면 content hash를 다시 계산해 위 값과 이동 manifest를 대조한다. 경로 변경만으로 drift라 판단하지 않고 content, POM dependency, main class와 observed behavior를 함께 비교한다.

### 3.3 Legacy endpoint/worker/finalize characterization 대상

아래 표의 “관찰”은 승인된 목표 동작이 아니다.

| Case ID | 현재 관찰 | Characterization assertion |
|---|---|---|
| `LEG-API-POST-001` | `POST /optimizations`는 nonblank `inputUri`를 요구하고 `gs://`만 허용 | 누락은 `400 {"message":"inputUri is required"}`, 다른 scheme은 `400 {"message":"inputUri must be a gs:// URI"}` |
| `LEG-API-POST-002` | `parameters`가 map이 아니면 빈 map으로 바꿈 | Error가 아니라 default 경로로 감 |
| `LEG-API-POST-003` | `parallelRuns` default 8, clamp 1..20 | Workflow argument에 변환값이 들어감 |
| `LEG-API-POST-004` | `iterationsPerRun` default 5000, clamp 100..250000 | Workflow argument에 변환값이 들어감 |
| `LEG-API-POST-005` | `seed`가 Number가 아니면 `System.nanoTime()` | 값은 비결정적이므로 exact value 대신 “생성됨 + caller value 아님”을 관찰 |
| `LEG-API-POST-006` | UUID request ID와 `Instant.now()` submittedAt 생성 | Format/field presence만 고정하고 값 자체를 semantic baseline으로 쓰지 않음 |
| `LEG-API-POST-007` | Workflow start 후 `202`, `ACCEPTED`, status path 반환 | Workflow payload와 response field를 함께 capture |
| `LEG-API-GET-001` | `results/<id>.json` 부재 시 `202 RUNNING` | requestId와 status exact |
| `LEG-API-GET-002` | Object 존재 시 검증 없이 raw bytes를 `200`으로 반환 | Byte-for-byte 반환을 고정하되 target 허용 behavior로 승격 금지 |
| `LEG-API-ERR-001` | `IllegalArgumentException`은 400, 그 밖은 generic 500 | Malformed JSON/SDK failure의 message를 고정 |
| `LEG-WORKER-001` | `/internal/batches`는 POST만 허용 | 다른 method는 `405 Method not allowed` |
| `LEG-WORKER-002` | requestId/inputUri/runNumber/seed/iterations 요구 | Missing/wrong type별 exact 400 message |
| `LEG-WORKER-003` | Number를 `intValue`/`longValue`로 변환 | Decimal truncation도 current observation에 포함하되 target coercion 승인으로 해석 금지 |
| `LEG-WORKER-004` | Candidate key는 `candidates/<requestId>/<runNumber>.json` | 같은 key create가 immutable/CAS가 아님을 기록 |
| `LEG-WORKER-005` | 성공 시 `200 CANDIDATE_STORED` | requestId/runNumber/status exact |
| `LEG-FINAL-001` | Candidate prefix 전부를 읽고 `double objective` 최솟값 선택 | 선언 worker 수/identity/verification을 확인하지 않음 |
| `LEG-FINAL-002` | Candidate가 없거나 손상되면 generic 500 | `Unable to finalize optimization` exact |
| `LEG-FINAL-003` | `results/<requestId>.json`에 `COMPLETED`, bestCandidate, completedAt 저장 | Both-pass 없이 정상 result를 만들며 target에서는 차단 대상 |
| `LEG-HTTP-001` | API root의 알려지지 않은 route는 404 | `{"message":"Not found"}` |
| `LEG-RUNTIME-001` | `SERVICE_MODE` default는 `api`; 다른 값은 startup error | `api|worker` single-JAR behavior와 main class 보존 |

Characterization harness가 동적 UUID/clock/seed를 deterministic supplier로 주입할 수 있도록 AR-0에서 seam을 제공하지 않았다면 AR-8이 임의로 legacy production을 refactor하지 않는다. 먼저 AR-0 baseline owner에게 돌아가 content-preserving seam과 이동 전/후 parity evidence를 완성해야 한다.

## 4. 현 상태 → 목표 gap

| 책임 | 현재 상태 | AR-8 목표 | 이 phase의 first evidence |
|---|---|---|---|
| Schema/version | Raw `Map<String,Object>`, version 없음 | Exact legacy adapter version과 target contract version | Missing/unknown version typed rejection |
| Alias/coercion | Map/Jackson/Number conversion에 암묵적 | 모든 alias/coercion rule ID와 source→target provenance | Alias conflict, unapproved coercion red/green |
| Legacy behavior | Endpoint/worker/finalize automated coverage 없음 | 이동 전 baseline과 current behavior parity | Characterization suite green |
| Compatibility | “비슷함”을 판단할 matrix 없음 | 모든 semantic row가 4개 classification 중 하나 | Matrix coverage test |
| Shadow | Production workflow 자체가 side effect | Isolated legacy observation + target shadow artifact 비교, publication/status 무변경 | Shadow no-side-effect IT |
| Idempotency | POST마다 새 UUID/execution | Same key+same semantic digest 수렴, same key+different digest conflict | Idempotency fault test |
| Cancellation | API/worker cancellation contract 없음 | Intent, actual termination, last completed boundary 분리 | Cancellation compatibility test |
| Status | `ACCEPTED/RUNNING/COMPLETED` 중심 | 정상/예외/verification/publication 상태를 잃지 않는 projection | Status non-collapse test |
| Retrieval | GCS raw result 존재 여부 | Both-pass `PublishableResult`만 정상 retrieval | Unverified retrieval rejection |
| Artifact | Mutable key/create, digest 검증 없음 | Immutable digest ref와 CAS publication pointer | Artifact identity/rollback IT |
| Cutover | `SERVICE_MODE`와 GCP deployment가 path 선택 | Provider-neutral logical version pointer/CAS | Cutover CAS test |
| Rollback | 이전 version pointer/record 없음 | 이전 logical version 복원, artifacts/idempotency 보존 | Rollback rehearsal |
| Legacy disposition | 현재 정상 경로와 target 분리 없음 | Legacy 유지/격리/제거 decision record | Evidence handoff; AR-8에서 삭제 금지 |

## 5. Semantic compatibility matrix 계약

### 5.1 허용 classification

Compatibility row는 다음 네 값 중 정확히 하나를 가져야 한다.

| 값 | 의미 |
|---|---|
| `EQUIVALENT` | 권위 있는 의미와 관찰 결과가 같다 |
| `LEGACY_ONLY` | Legacy에만 존재하며 target 정상 계약으로 승격하지 않는다 |
| `TARGET_ONLY` | Target correctness/integrity를 위해 새로 존재한다 |
| `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | 그대로 보존할 수 없거나 외부 계약 결정이 필요하며 승인 전 cutover 차단 |

별도의 `UNRESOLVED` classification을 만들지 않는다. 미승인 row는 위 네 값 중 의미에 맞게 분류하고 `decisionState=UNRESOLVED`, `blocksCutover=true`로 기록한다.

각 row의 필수 field는 다음이다.

```text
caseId
surface
legacyObservation
targetRequirement
classification
decisionState
blocksCutover
adapterRuleId
authorityRefs[]
characterizationTest
targetTest
approvalRef
evidenceRef
```

### 5.2 작성 시점 seed matrix

| Case ID | Legacy 관찰 | Target 요구 | 분류 | 초기 결정 상태 |
|---|---|---|---|---|
| `CMP-SUBMIT-IDEMPOTENCY` | Retry마다 UUID와 workflow execution 새로 생성 | Same idempotency key+digest 수렴, 다른 digest conflict | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | External key/wire 미승인; cutover 차단 |
| `CMP-SUBMIT-GS-URI` | `gs://`만 허용 | Application은 provider locator를 opaque하게 격리 | `LEGACY_ONLY` | Public input reference 계약 미승인 |
| `CMP-SUBMIT-HIDDEN-RUNS` | `parallelRuns=8` default/clamp | Official/semantic execution 값을 hidden default로 만들지 않음 | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Legacy characterization만 허용 |
| `CMP-SUBMIT-HIDDEN-STEPS` | `iterationsPerRun=5000` default/clamp | Explicit config/manifest만 사용 | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | `Q-BENCH-02` 값으로 승격 금지 |
| `CMP-SUBMIT-HIDDEN-SEED` | 누락 seed에 `System.nanoTime()` | Exact seed lineage | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Silent mapping 금지 |
| `CMP-SUBMIT-UNKNOWN-FIELDS` | Map의 unknown parameter를 workflow payload에 보존 | Unknown-field policy는 승인 필요 | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Policy 승인 전 reject |
| `CMP-GET-MISSING-RESULT` | Object 부재면 `202 RUNNING` | Prepared/running/verifying/rejected 등 actual state 보존 | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Legacy status projection 미승인 |
| `CMP-GET-RAW-RESULT` | Object가 있으면 검증 없이 `200` raw JSON | Both-pass `PublishableResult`만 정상 retrieval | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Target에서는 반드시 차단 |
| `CMP-WORKER-NUMBER-TRUNCATION` | Number `intValue/longValue`로 decimal도 축소 가능 | 승인된 coercion이 아니면 reject+provenance | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Coercion 승인 없음 |
| `CMP-WORKER-MUTABLE-KEY` | 같은 candidate key create 가능 | Immutable create-once digest artifact | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Target integrity 우선 |
| `CMP-FINAL-PARTIAL-PREFIX` | 발견한 일부 candidate만으로 min 선택 | Declared worker completeness와 verified fan-in | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Target에서는 반드시 차단 |
| `CMP-FINAL-DOUBLE-OBJECTIVE` | 하나의 `double objective`로 min | Bound comparator와 verified objective | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Legacy target equivalence 주장 금지 |
| `CMP-FINAL-COMPLETED` | Both-pass 없이 `COMPLETED` | 두 verifier `PASS` 뒤 publication | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Target에서는 반드시 차단 |
| `CMP-CANCELLATION` | 지원 없음 | Intent/actual termination/last completed boundary | `TARGET_ONLY` | Target contract |
| `CMP-REQDATE-DUEDATE` | PDF/sample와 raw 입력에 두 이름 존재 가능 | 둘이 함께 있으면 normalized exact equality, 아니면 conflict | `EQUIVALENT` | Domain decision을 adapter rule로 기록 |
| `CMP-VEHICLE-FEATURE-ALIAS` | Legacy order `vehicleFeature` array | `vehicleFeatureList` alias, 둘 다 있으면 exact list equality | `EQUIVALENT` | Domain decision을 adapter rule로 기록 |
| `CMP-ORDER-TASKTIME` | PDF sample에는 order-level `taskTime` | Target은 거부 | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | PDF는 authority가 아님; cutover row 승인 필요 |
| `CMP-ITEM-TASKTIME-QTY` | PDF 설명은 qty 비적용 예시 | Target은 `item.taskTime × qty` | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | 상위 Domain 의미가 우선, external 승인 필요 |
| `CMP-DATETIME-FORMAT` | PDF 표 RFC3339와 sample local string이 충돌 | Exact timezone/offset 없는 `yyyy-MM-dd HH:mm:ss` | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Ambiguous raw input reject |
| `CMP-ONEWAY-ROTATION` | Win fixture `oneway + multiRotation=1` | Oneway 우선, raw value는 non-authority provenance | `EQUIVALENT` | `Q-BENCH-03` 결정 |
| `CMP-DECIMAL-DU` | Win fixture에 decimal `D/U` 다수 | Integer-only, decimal reject | `INTENTIONAL_BREAK_REQUIRES_APPROVAL` | Official 사용 금지; AR-8에서 변환 금지 |

Matrix implementation은 row를 삭제해 green으로 만들 수 없다. 새 legacy observation 또는 target status/error가 생기면 coverage test가 누락 case ID를 열거하며 실패해야 한다.

## 6. 정확한 예상 경로와 변경 분류

아래 경로는 target reactor가 선행 phase에서 계획대로 만들어졌다는 조건의 **계획상 고정 제안**이다. 실제 선행 phase가 승인된 ADR로 이름을 바꾸었다면 임의로 두 벌을 만들지 말고 AR-8 문서/계획을 같은 변경 단위에서 갱신한다.

### 6.1 Entry에서 존재해야 하며 AR-8이 소유하지 않는 경로

| 경로 | 기대 상태 | AR-8 규칙 |
|---|---|---|
| `legacy/current-app/pom.xml` | AR-0가 current app을 내용 보존 이동 | Characterization baseline owner; 임의 dependency 제거 금지 |
| `legacy/current-app/src/main/java/com/ronext/optimizer/**` | 현재 6개 class의 이동본 | 먼저 hash/behavior parity 확인 |
| `legacy/current-app/src/test/java/com/ronext/optimizer/**` | Legacy baseline characterization | 누락 시 AR-0 blocker로 반환 |
| `rpdptw/verification/src/main/java/**` | AR-5 verified result/both-pass API | 직접 우회하거나 duplicate verifier 생성 금지 |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/**` | AR-6 logical port | Provider DTO를 추가하지 않음 |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/**` | AR-7 identity/state machine | Retry/worker identity를 재정의하지 않음 |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/local/**` | AR-6 local artifact/state/publisher | Contract test를 재사용 |
| `apps/api/pom.xml`, `apps/worker/pom.xml` | AR-0/6 composition modules | AR-8 dependency는 필요한 최소 범위만 추가 |

### 6.2 AR-8에서 먼저 만들 test/resource

| 순서 | 경로 | 분류 | 책임 |
|---:|---|---|---|
| 1 | `legacy/current-app/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyEndpointCharacterizationTest.java` | 선행 존재 확인 또는 보강 | POST/GET/404/error와 workflow payload |
| 2 | `legacy/current-app/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyWorkerCharacterizationTest.java` | 선행 존재 확인 또는 보강 | `/internal/batches`, numeric conversion, candidate key |
| 3 | `legacy/current-app/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyFinalizeCharacterizationTest.java` | 선행 존재 확인 또는 보강 | Prefix/min objective/result status/error |
| 4 | `legacy/current-app/src/test/resources/characterization/legacy-http-v1/*.json` | 신규/보강 | 동적 field를 명시적으로 표시한 golden observation |
| 5 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/json/legacy/v1/LegacyInputAdapterCompatibilityTest.java` | 신규 | Alias/coercion/provenance/error |
| 6 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/json/legacy/v1/LegacyResultAdapterCompatibilityTest.java` | 신규 | Both-pass 외 result projection 차단 |
| 7 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/json/legacy/v1/CompatibilityMatrixCoverageTest.java` | 신규 | 모든 row/test/approval 상태 coverage |
| 8 | `adapters/common/src/test/resources/legacy-v1/*.json` | 신규 | 최소 hand fixture; Win JSON 복제/변환 금지 |
| 9 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/compatibility/IdempotencyCancellationCompatibilityTest.java` | 신규 | Same-key convergence/conflict와 cancel 의미 |
| 10 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/compatibility/StatusRetrievalCompatibilityTest.java` | 신규 | Status non-collapse, verified retrieval |
| 11 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/cutover/LogicalVersionCutoverServiceTest.java` | 신규 | Evidence gate와 pointer CAS |
| 12 | `apps/api/src/test/java/com/ronext/rpdptw/api/ShadowComparisonIT.java` | 신규 | 모든 semantic diff 분류와 no-side-effect |
| 13 | `apps/api/src/test/java/com/ronext/rpdptw/api/CutoverRollbackIT.java` | 신규 | Cutover/rollback, artifact/idempotency 보존 |
| 14 | `apps/api/src/test/java/com/ronext/rpdptw/api/CutoverPublicationGateIT.java` | 신규 | Legacy/target candidate의 both-gate 우회 차단 |
| 15 | `apps/api/src/test/java/com/ronext/rpdptw/api/VersionedRouteCompositionIT.java` | 신규 | Internal logical version 선택; public wire 승인 주장 금지 |
| 16 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/CompatibilityCutoverArchitectureTest.java` | 신규 | Provider SDK, legacy package, unverified result 침투 차단 |

### 6.3 Test red 확인 뒤 만들 production/resource

| 경로 | 분류 | 책임 |
|---|---|---|
| `adapters/common/pom.xml` | 변경 | 필요한 test dependency/resource만 추가; provider SDK 금지 |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/legacy/v1/LegacyV1InputAdapter.java` | 신규 | Exact version input mapping |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/legacy/v1/LegacyV1AdapterPolicy.java` | 신규 | 승인 rule/unknown-field policy snapshot |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/legacy/v1/AdapterProvenance.java` | 신규 | Alias/coercion/ignored-non-authority lineage |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/legacy/v1/LegacyV1MappingException.java` | 신규 | Typed pre-solve mapping error |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/json/legacy/v1/LegacyV1ResultAdapter.java` | 신규 | `PublishableResult`만 legacy projection 후보로 변환 |
| `adapters/common/src/main/resources/META-INF/rpdptw/compatibility/legacy-v1-target-v1.yaml` | 신규 | Versioned semantic compatibility matrix |
| `adapters/common/src/main/resources/META-INF/rpdptw/compatibility/legacy-v1-policy.yaml` | 신규 | 승인 rule ID와 decision reference; raw public schema 승인 아님 |
| `rpdptw/application/pom.xml` | 변경 | Compatibility/cutover package test 구성 |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/compatibility/CompatibilityClassification.java` | 신규 | 네 classification enum |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/compatibility/CompatibilityMatrix.java` | 신규 | Matrix identity/coverage |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/compatibility/ShadowComparisonReport.java` | 신규 | Classified difference와 artifact fingerprint |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/compatibility/ShadowComparisonService.java` | 신규 | Isolated observation 비교 |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/compatibility/VersionedSolveFacade.java` | 신규 | Idempotency/cancel/status/retrieval을 version 선택 뒤 보존 |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/cutover/LogicalExecutionVersion.java` | 신규 | Internal `LEGACY_V1`/`TARGET_V1` identity |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/cutover/CutoverPointer.java` | 신규 | Active/previous version, generation, evidence digest |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/cutover/LogicalVersionCutoverService.java` | 신규 | Cutover/rollback evidence gate와 CAS |
| `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/LogicalVersionRepository.java` | 신규 | Provider-neutral get/CAS port |
| `adapters/common/src/main/java/com/ronext/rpdptw/adapter/local/LocalLogicalVersionRepository.java` | 신규 | Deterministic local CAS implementation |
| `apps/api/pom.xml` | 변경 | Application/common adapter composition |
| `apps/api/src/main/java/com/ronext/rpdptw/api/internal/VersionedSolveHandler.java` | 신규 | Internal logical route composition; public wire 미확정 |
| `apps/api/src/main/java/com/ronext/rpdptw/api/ApiCompositionRoot.java` | 변경 | Approved profiles/adapters/backends 주입; SDK client 생성 금지 |

### 6.4 이동·삭제·폐기

AR-8 안에서 계획된 file move와 delete는 **없다**.

- `legacy/current-app` 삭제 또는 source rewrite는 금지한다.
- `gcp/**`, root `Dockerfile`, current deployment/README는 변경하지 않는다.
- `src/main/java/com/ronext/optimizer/**`가 아직 root에 있으면 AR-8이 이동하지 않는다. AR-0 미완료 blocker로 처리한다.
- Legacy module 처분은 rollback 기간, 승인 record와 physical cutover scope가 별도로 충족된 뒤 결정한다.

## 7. 계획상 제안 package/type/API

이 절의 이름은 구현 세션 간 모호성을 줄이기 위한 **계획상 제안 API**이며 public HTTP/wire/storage 계약이 아니다.

### 7.1 Legacy input mapping

```java
package com.ronext.rpdptw.adapter.json.legacy.v1;

public final class LegacyV1InputAdapter {
    public AdaptedCanonicalInput adapt(
            byte[] rawJson,
            LegacyV1AdapterPolicy policy
    ) throws LegacyV1MappingException;
}

public record AdaptedCanonicalInput(
        CanonicalInput canonicalInput,
        AdapterProvenance provenance,
        String rawSha256,
        String adapterVersion
) {}

public record LegacyV1AdapterPolicy(
        String sourceSchemaVersion,
        String targetContractVersion,
        UnknownFieldPolicy unknownFieldPolicy,
        List<ApprovedMappingRule> approvedRules,
        String policySha256
) {}

public enum UnknownFieldPolicy {
    REJECT,
    PRESERVE_NON_AUTHORITY
}
```

`UnknownFieldPolicy` 값을 runtime default로 고르지 않는다. Policy artifact에 명시되지 않았거나 approval reference가 없으면 adapter construction이 실패한다.

```java
public record AdapterProvenance(
        String rawSha256,
        String canonicalFingerprint,
        String adapterVersion,
        String policySha256,
        List<MappingDecision> decisions
) {}

public record MappingDecision(
        String rawJsonPointer,
        String canonicalPath,
        MappingRuleId ruleId,
        MappingOperation operation,
        String rawValueSha256,
        String canonicalValueSha256,
        String authorityReference
) {}

public enum MappingOperation {
    DIRECT,
    ALIAS,
    EXPLICIT_COERCION,
    IGNORED_NON_AUTHORITY
}
```

Raw PII/value 전체를 provenance/log에 복제하지 않는다. JSON pointer, rule, operation과 value digest만 보존한다.

### 7.2 Adapter error

```java
public final class LegacyV1MappingException extends Exception {
    public LegacyV1MappingError error();
    public String jsonPointer();
    public String ruleId();
}

public enum LegacyV1MappingError {
    UNSUPPORTED_SCHEMA_VERSION,
    AMBIGUOUS_ALIAS,
    UNAPPROVED_COERCION,
    UNKNOWN_FIELD_POLICY_UNRESOLVED,
    INVALID_LEGACY_VALUE,
    TARGET_CONTRACT_REJECTION
}
```

예외를 임의의 HTTP 400/500과 message body로 바꾸지 않는다. Public error mapping은 승인된 external contract가 matrix row에 연결된 뒤 `apps/api`가 담당한다.

### 7.3 Result projection

```java
public final class LegacyV1ResultAdapter {
    public LegacyV1ResultView adapt(PublishableResult result);
}

public record LegacyV1ResultView(
        String logicalSolveId,
        String legacyStatus,
        byte[] canonicalPayload,
        String payloadSha256,
        String candidateVerifierReportSha256,
        String resultVerifierReportSha256
) {}
```

Method input을 `CommittedCandidate`, `VerifiedSolution`, raw `FinalResult` 또는 `Map<String,Object>`로 overload하지 않는다. `PublishableResult`보다 앞 단계 artifact는 compile-time에 정상 result projection으로 들어갈 수 없어야 한다.

### 7.4 Compatibility와 shadow

```java
package com.ronext.rpdptw.application.compatibility;

public enum CompatibilityClassification {
    EQUIVALENT,
    LEGACY_ONLY,
    TARGET_ONLY,
    INTENTIONAL_BREAK_REQUIRES_APPROVAL
}

public record CompatibilityMatrix(
        String sourceVersion,
        String targetVersion,
        String matrixSha256,
        List<CompatibilityCase> cases
) {}

public final class ShadowComparisonService {
    public ShadowComparisonReport compare(ShadowComparisonCommand command);
}

public record ShadowComparisonCommand(
        String caseId,
        ArtifactRef capturedLegacyObservation,
        ArtifactRef isolatedTargetObservation,
        String compatibilityMatrixSha256
) {}
```

`compare`는 입력 artifact를 digest 검증 뒤 읽고 report를 계산할 뿐 다음을 호출하지 않는다.

- `ResultPublisher`
- 정상 `RunStateRepository` transition
- `SubmissionPort`
- `CancellationPort`
- `WorkflowExecutionPort`

Target observation을 만들기 위한 shadow execution은 별도 격리 namespace와 non-publishing application path를 사용한다. 정상 solve ID/idempotency key를 재사용하지 않는다.

```java
public record ShadowComparisonReport(
        String caseId,
        String legacyObservationSha256,
        String targetObservationSha256,
        String matrixSha256,
        List<ClassifiedDifference> differences,
        boolean hasUnclassifiedDifference,
        boolean blocksCutover,
        String reportSha256
) {}
```

### 7.5 Logical cutover와 rollback

```java
package com.ronext.rpdptw.application.cutover;

public enum LogicalExecutionVersion {
    LEGACY_V1,
    TARGET_V1
}

public record CutoverPointer(
        String routeKey,
        LogicalExecutionVersion activeVersion,
        LogicalExecutionVersion previousVersion,
        long generation,
        String compatibilityMatrixSha256,
        String shadowReportSha256,
        String approvalBundleSha256
) {}

public interface LogicalVersionRepository {
    Optional<CutoverPointer> find(String routeKey);
    CutoverPointer compareAndSet(
            String routeKey,
            long expectedGeneration,
            CutoverPointer next
    ) throws CutoverCasConflictException;
}

public final class LogicalVersionCutoverService {
    public CutoverReceipt cutover(CutoverCommand command);
    public RollbackReceipt rollback(RollbackCommand command);
}
```

`CutoverCommand`는 expected generation, from/to version, complete matrix digest, shadow report digest, 모든 intentional-break approval digest와 evidence ID를 요구한다. `RollbackCommand`는 cutover receipt digest, expected generation과 정확한 previous version을 요구한다. Rollback은 artifact를 복사·삭제·재직렬화하지 않고 pointer만 CAS로 되돌린다.

### 7.6 Versioned lifecycle facade

```java
package com.ronext.rpdptw.application.compatibility;

public final class VersionedSolveFacade {
    public SubmissionReceipt submit(VersionedSubmitCommand command);
    public CancellationReceipt requestCancellation(VersionedCancelCommand command);
    public SolveStatusView getStatus(VersionedStatusQuery query);
    public Optional<VerifiedResultView> getVerifiedResult(VersionedResultQuery query);
}
```

Facade는 AR-6/7의 기존 use case와 port type을 delegate한다. 별도의 parallel state machine을 만들지 않는다.

- Submit identity는 `idempotencyKey + canonical semantic digest + selected logical version`을 보존한다.
- Same key/same digest retry는 동일 solve identity와 artifact refs로 수렴한다.
- Same key/different digest 또는 다른 logical version은 typed conflict다.
- Cancellation receipt는 `intentRecorded`, `actualTermination`, `lastCompletedBoundary`를 분리한다.
- Status view는 cancel requested, cancelled, incomplete, verifier rejected, publication rejected를 `RUNNING`/`COMPLETED`로 뭉개지 않는다.
- Verified result retrieval은 both-pass artifact reference가 없으면 empty/typed non-publishable state를 반환하고 raw candidate를 노출하지 않는다.

### 7.7 Cutover error

| Error | 발생 조건 | 외부 mapping |
|---|---|---|
| `IDEMPOTENCY_CONFLICT` | Same key, 다른 semantic digest/version | Public status/body 미승인 |
| `UNCLASSIFIED_SHADOW_DIFFERENCE` | Matrix에 없는 field/status/metric 차이 | Cutover 차단 |
| `CUTOVER_EVIDENCE_INCOMPLETE` | Matrix/shadow/approval/both-pass digest 누락 | Cutover 차단 |
| `CUTOVER_CAS_CONFLICT` | Stale generation | 현재 pointer 재조회 후 명시적 재시도 |
| `ROLLBACK_TARGET_UNAVAILABLE` | Previous logical backend/adapter가 보존되지 않음 | Rollback 실패, pointer 불변 |
| `RESULT_NOT_VERIFIED` | Both-pass 없는 retrieval/projection | 정상 result 노출 금지 |
| `CANCELLATION_STATE_LOSS` | Intent와 actual termination 중 하나가 사라짐 | Integrity failure |

### 7.8 불변조건

1. 모든 alias/coercion/ignored legacy field에는 stable rule ID, authority reference와 provenance entry가 있다.
2. Ambiguous alias는 “우선순위”로 고르지 않고 reject한다.
3. Legacy hidden default, README 수치와 PDF 예시를 target official/default config로 옮기지 않는다.
4. Shadow는 정상 publication pointer, state와 idempotency/cancellation record를 바꾸지 않는다.
5. `PublishableResult`와 두 verifier report digest 없이는 target/legacy 어느 projection도 정상 retrieval이 아니다.
6. Retry는 logical identity, seed, warm start, config와 semantic digest를 바꾸지 않는다.
7. Cancellation intent, actual termination과 last completed boundary를 별도 보존한다.
8. Cutover/rollback은 generation CAS이며 immutable artifact를 삭제·덮어쓰기하지 않는다.
9. Rollback 후 같은 idempotency key와 solve ID가 이전 artifact identity로 조회된다.
10. Provider locator/SDK/resource type은 application compatibility/cutover type에 없다.
11. Completion order, wall clock과 provider execution ID는 semantic comparison/cutover approval input이 아니다.
12. Legacy module은 rollback window와 별도 approval 전 삭제하지 않는다.

## 8. 세분화된 test case

### 8.1 Characterization와 adapter

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `LegacyEndpointCharacterizationTest.postDefaultsClampAndWorkflowPayloadMatchBaseline` | Characterization; 구현 계획 §4.2 | Deterministic UUID/clock/seed seam + min/max request | Exact 202 body와 workflow fields; default/clamp observation | AR-0 baseline golden과 current response/payload diff가 field path로 출력 | 모든 stable field 일치, dynamic field는 declared matcher로만 허용 |
| `LegacyEndpointCharacterizationTest.getReturnsRunningOrRawStoredBytes` | Characterization | Missing blob / arbitrary stored JSON bytes | 202 RUNNING / byte-exact 200 | status, key 또는 returned bytes diff | 둘 다 baseline과 일치 |
| `LegacyEndpointCharacterizationTest.preservesErrorMapping` | Characterization | Missing inputUri, wrong scheme, malformed JSON, SDK failure, unknown route | Exact 400/500/404 message | HTTP code/message mismatch | Case별 exact 관찰 일치 |
| `LegacyWorkerCharacterizationTest.recordsRequiredFieldsNumericConversionAndObjectKey` | Characterization | Integer/decimal/missing worker request | Current conversion/error와 key | run/seed/iterations conversion 또는 key diff | Golden observation 일치 |
| `LegacyFinalizeCharacterizationTest.selectsMinimumObservedDoubleWithoutCompletenessGate` | Characterization | 3 prefix candidates, declared count 불일치 | Current minimum chosen, COMPLETED output | Candidate choice/status/key diff | Legacy current behavior와 일치; target 승인 의미로 사용하지 않음 |
| `LegacyInputAdapterCompatibilityTest.recordsEveryAliasAndRejectsAmbiguity` | Unit/integration; Domain §4/§15 | `reqDate`, `dueDate`, both equal/different; `vehicleFeature`/`vehicleFeatureList` | Equal alias maps with rule entries; conflict typed reject | 최초에는 `LegacyV1InputAdapter` symbol compile failure; skeleton 뒤에는 `expected AMBIGUOUS_ALIAS at /orders/0/dueDate` assertion failure | Canonical value와 provenance exact, conflict error exact |
| `LegacyInputAdapterCompatibilityTest.rejectsEveryUnapprovedCoercion` | Unit/negative | Decimal worker numbers, decimal `D/U`, order-level taskTime, ambiguous datetime | No truncation/rounding/guess | `expected UNAPPROVED_COERCION but mapping succeeded` | Pointer/rule/error exact |
| `LegacyInputAdapterCompatibilityTest.doesNotInventUnknownFieldPolicy` | Unit/governance | Unknown top-level/parameter field, policy missing | Adapter construction or mapping reject | `expected UNKNOWN_FIELD_POLICY_UNRESOLVED` | Explicit approved policy에서만 deterministic result |
| `LegacyInputAdapterCompatibilityTest.fingerprintsRawCanonicalPolicyAndDecisions` | Unit/reproducibility | Same raw bytes/policy twice, one-byte/policy change | Same input same fingerprint; change propagates | Same input digest differs or changed input digest unchanged | Fingerprint law 통과 |
| `LegacyResultAdapterCompatibilityTest.acceptsOnlyBothPassPublishableResult` | Unit/publication | Both-pass result, candidate-only, candidate PASS/result FAIL | Only first maps | Compile API가 raw candidate overload를 허용하거나 `RESULT_NOT_VERIFIED` 없이 map | `PublishableResult` only + report digests preserved |
| `CompatibilityMatrixCoverageTest.classifiesEveryObservedSemanticCase` | Unit/governance | Characterization case index + matrix YAML | Every case exactly once, 4 enum 중 하나 | `unclassified cases: [...]` 또는 duplicate case IDs | 누락/중복/unknown classification 0 |
| `CompatibilityMatrixCoverageTest.blocksEveryUnapprovedIntentionalBreak` | Unit/governance | Matrix YAML | Approval ref 없는 intentional break는 `blocksCutover=true` | `case CMP-... lacks approval but does not block cutover` | 모든 unresolved row 차단 |

Characterization test는 이미 존재하는 behavior를 고정하는 baseline guard이므로 새 production behavior의 red 증거를 대신하지 않는다. Characterization suite green 후 `LegacyInputAdapterCompatibilityTest...`의 compile failure 또는 지정 assertion failure를 반드시 관찰하고 보존한 뒤 adapter production file을 만든다.

### 8.2 Application semantics, shadow와 cutover

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `IdempotencyCancellationCompatibilityTest.sameKeySameDigestConvergesAndDifferentDigestConflicts` | Fault/contract; Arch §11.3 | Fake submission/state/artifact, two retries | Same solve/artifacts; typed conflict on changed digest/version | `expected same solveId` 또는 conflict 없이 second execution 생성 | Stable identity와 zero duplicate dispatch |
| `IdempotencyCancellationCompatibilityTest.preservesIntentActualTerminationAndLastCompletedBoundary` | Fault; Arch §14/§17 | Cancel before dispatch, mid-step, after publish | Intent와 actual termination 별도, incomplete candidate discard | `CANCEL_REQUESTED collapsed to CANCELLED/COMPLETED` | Case별 exact state/last boundary |
| `StatusRetrievalCompatibilityTest.neverCollapsesExceptionalStatesIntoLegacyRunningOrCompleted` | Unit/integration | REJECTED_INPUT, INCOMPLETE, verifier fail, publication fail, cancel | Distinct internal state | Expected state differs or legacy projection silently chosen | Internal view exact; external projection blocked unless approved |
| `StatusRetrievalCompatibilityTest.exposesOnlyPublishableResult` | Integration | Candidate, VerifiedSolution only, result FAIL, PublishableResult | First 3 normal retrieval absent/rejected; last available | `unverified artifact was returned` | Both report digest와 payload digest match |
| `LogicalVersionCutoverServiceTest.requiresCompleteMatrixShadowAndApprovalBundle` | Unit/governance | Missing digest combinations | No pointer update | `expected CUTOVER_EVIDENCE_INCOMPLETE` | Complete bundle only CAS success |
| `LogicalVersionCutoverServiceTest.rejectsStaleGeneration` | Unit/fault | Repository generation N, command N-1 | Pointer unchanged, CAS conflict | Stale cutover succeeds | Exact current pointer preserved |
| `ShadowComparisonIT.classifiesEverySemanticDifference` | Integration | Captured legacy observation + isolated target result with known differences | Every diff maps to case ID/classification | `unclassified differences: [/status,...]` | Unclassified 0; report digest stable |
| `ShadowComparisonIT.doesNotPublishOrMutateNormalLifecycleState` | Integration/fault | Recording publisher/state/idempotency/cancel fakes | Invocation count 0; normal records byte/digest same | `expected no calls to ResultPublisher` 또는 state digest changed | Only isolated shadow/evidence artifact exists |
| `ShadowComparisonIT.isCompletionOrderIndependent` | Reproducibility | Same observations permuted | Same canonical report/digest | Different order yields different digest | Stable sort/tie rules로 exact equality |
| `CutoverPublicationGateIT.blocksUnverifiedTargetAndLegacyCandidate` | Integration/fault; two verifier invariant | Target result FAIL, raw legacy COMPLETED candidate | No normal publication/retrieval | Either path returns 200/published pointer | Both blocked with `RESULT_NOT_VERIFIED`/approval block |
| `VersionedRouteCompositionIT.selectsOnlyCasActiveLogicalVersion` | Integration | Pointer LEGACY_V1/TARGET_V1/stale read | Exactly active backend; stale write cannot switch | Both backend calls, fallthrough 또는 stale selection | One backend call and recorded pointer generation |
| `CutoverRollbackIT.restoresPreviousLogicalVersionWithoutArtifactLoss` | Fault/integration | Legacy artifacts + target artifacts + cutover receipt | Pointer returns to previous; all digests still readable | `missing artifact after rollback` 또는 version mismatch | Artifact set/digests identical before/after |
| `CutoverRollbackIT.preservesRetrievalAndIdempotencyAcrossRollback` | Fault/integration | Same key submit before cutover, retry after cutover/rollback | No duplicate solve; version conflict explicit; previous result retrievable | New solve silently created or old result lost | Stable solve/artifact identity |
| `CutoverRollbackIT.isIdempotentForSameReceiptAndRejectsDifferentReceipt` | Fault | Rollback twice, forged receipt | Same receipt converges; forged receipt no mutation | Generation increments twice or forged rollback succeeds | Pointer/audit exact |
| `CompatibilityCutoverArchitectureTest.forbidsProviderAndLegacyTypesInCoreApplicationContracts` | Architecture | Compiled classes/dependency graph | Zero SDK/resource URI/`com.ronext.optimizer` reference in core/application | Violation list includes class/member | Violation 0 |
| `CompatibilityCutoverArchitectureTest.forbidsOfficialNumericDefaultsInCutoverCode` | Architecture/governance | Production bytecode/source scan | No Q-BENCH numeric default | Field/default assignment path reported | Test fixture-local explicit values만 허용 |

## 9. 강제 test-first 구현 순서

한 묶음의 test가 red인 동안 다음 묶음 production을 미리 만들지 않는다.

### 9.1 Gate 0 — Entry와 baseline

1. `git status --short`, HEAD, design/plan/source hash와 toolchain을 evidence에 기록한다.
2. AR-5/6/7 phase 문서 status와 evidence digest를 검증한다.
3. Target reactor/module/POM과 legacy 이동 manifest가 없으면 `BLOCKED`로 종료한다.
4. Legacy characterization suite를 실행한다.
5. Characterization이 drift로 실패하면 AR-8 target 구현을 시작하지 않는다. Drift source, expected/actual, owner와 마지막 known-good hash를 기록한다.

### 9.2 Gate 1 — Adapter red → minimal green

1. `LegacyInputAdapterCompatibilityTest`, `LegacyResultAdapterCompatibilityTest`, `CompatibilityMatrixCoverageTest`와 최소 fixture/resource를 production보다 먼저 작성한다.
2. Targeted Maven 명령을 실행한다.
3. 최초 red는 다음 중 하나여야 한다.
   - 새 type의 `cannot find symbol` compile failure
   - `expected AMBIGUOUS_ALIAS ...`
   - `expected UNAPPROVED_COERCION ...`
   - `unclassified cases: [...]`
4. Dependency download, Java version, missing predecessor test 또는 unrelated failure만 보았다면 올바른 red가 아니며 진행하지 않는다.
5. Error enum/record skeleton만 만들어 compile시킨 뒤 의미 assertion red를 다시 확인한다.
6. 한 assertion을 통과시키는 최소 mapping/rule/provenance 구현을 작성한다.
7. Targeted adapter suite green, `adapters/common` 전체 test, module verify 순으로 실행한다.

### 9.3 Gate 2 — Application semantics red → minimal green

1. Idempotency/cancellation/status/retrieval/cutover unit test를 production보다 먼저 작성한다.
2. `cannot find symbol` 뒤 skeleton을 만들고, 반드시 semantic assertion failure를 다시 관찰한다.
3. AR-6/7 use case/state machine을 delegate하는 최소 facade와 repository port를 구현한다. Parallel state machine을 만들지 않는다.
4. Same-key/different-digest, cancel mid-step, verifier fail, stale CAS 순으로 하나씩 green으로 만든다.
5. `rpdptw/application` 전체 test와 verify를 실행한다.

### 9.4 Gate 3 — Shadow와 route composition red → minimal green

1. `ShadowComparisonIT`, `CutoverPublicationGateIT`, `VersionedRouteCompositionIT`를 먼저 작성한다.
2. 첫 red에서 unclassified field/status/metric path 또는 unexpected publisher/state call을 확인한다.
3. Pure comparison/canonical ordering부터 구현한다.
4. Isolated target observation path를 연결하되 정상 `ResultPublisher`/state/idempotency/cancel port가 호출되면 즉시 red로 유지한다.
5. Internal logical version handler를 연결한다. Public header/path/body version 선택은 external contract 승인 전 구현하지 않는다.
6. Failsafe targeted green 뒤 `apps/api` module verify를 실행한다.

### 9.5 Gate 4 — Cutover/rollback rehearsal

1. `CutoverRollbackIT`를 먼저 작성하고 artifact loss, idempotency drift 또는 pointer generation failure를 관찰한다.
2. Evidence bundle validation과 CAS pointer update만 최소 구현한다.
3. Target cutover → status/retrieval → rollback → same-key retry → artifact digest 재검증 순으로 rehearsal한다.
4. Cutover 중 오류, stale generation, duplicate command, forged receipt를 fault-inject한다.
5. `apps/api,apps/worker` verify와 reactor `mvn verify`를 실행한다.

### 9.6 최종 순서 요약

```text
characterization green
→ new test file 작성
→ intended compile/assertion red 확인·보존
→ API skeleton
→ intended semantic assertion red 재확인
→ 최소 production 구현
→ targeted green
→ owner module 전체 test
→ owner module verify
→ apps/api+apps/worker verify
→ reactor verify
→ architecture/rg/diff/evidence 검증
```

Production 구현 전에 red report가 `target/codex-evidence/AR-8/<evidence-id>/red/`에 없으면 phase는 `DONE`이 될 수 없다.

## 10. Exact 실행 명령

모든 명령의 working directory:

```bash
cd /Users/brown/workspace/ro-next
```

### 10.1 Baseline와 entry

```bash
git status --short
git branch --show-current
git rev-parse HEAD
sha256sum docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md
test -f docs/codex/phases/phase-05-verification-finalization-and-publication.md
test -f docs/codex/phases/phase-06-local-application-and-logical-ports.md
test -f docs/codex/phases/phase-07-logical-multi-round-coordinator.md
rg -n '^status: DONE$|^  status: DONE$' docs/codex/phases/phase-05-verification-finalization-and-publication.md docs/codex/phases/phase-06-local-application-and-logical-ports.md docs/codex/phases/phase-07-logical-multi-round-coordinator.md
mvn -version
```

Phase document의 YAML shape가 위 grep과 다르면 status를 눈으로 추정하지 말고 해당 문서 계약의 exact metadata path를 사용한다.

### 10.2 Characterization

```bash
mvn -pl legacy/current-app -am -Dtest=LegacyEndpointCharacterizationTest,LegacyWorkerCharacterizationTest,LegacyFinalizeCharacterizationTest -Dsurefire.failIfNoSpecifiedTests=false test
sha256sum legacy/current-app/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java legacy/current-app/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java
```

### 10.3 Adapter targeted red/green

```bash
mvn -pl adapters/common -am -Dtest=LegacyInputAdapterCompatibilityTest,LegacyResultAdapterCompatibilityTest,CompatibilityMatrixCoverageTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl adapters/common -am test
mvn -pl adapters/common -am verify
```

### 10.4 Application targeted red/green

```bash
mvn -pl rpdptw/application -am -Dtest=IdempotencyCancellationCompatibilityTest,StatusRetrievalCompatibilityTest,LogicalVersionCutoverServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/application -am test
mvn -pl rpdptw/application -am verify
```

### 10.5 Failsafe integration와 regression

```bash
mvn -pl apps/api -am -Dit.test=ShadowComparisonIT,CutoverPublicationGateIT,VersionedRouteCompositionIT,CutoverRollbackIT verify
mvn -pl apps/api,apps/worker -am verify
mvn verify
```

### 10.6 Architecture와 금지 shortcut 검색

```bash
mvn -pl build/architecture-rules -am -Dtest=CompatibilityCutoverArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
rg -n 'com\.google\.cloud|software\.amazon|com\.amazonaws|com\.azure|io\.kubernetes' rpdptw/application adapters/common
rg -n 'com\.ronext\.optimizer' rpdptw adapters/common apps/api apps/worker
rg -n 'StorageOptions|getDefaultInstance|gs://|WORKFLOW_NAME|RESULTS_BUCKET' rpdptw/application adapters/common
rg -n 'screenMaxSteps|phase2MaxSteps|maxRounds|watchdog|parallelRuns|iterationsPerRun' rpdptw/application adapters/common apps/api apps/worker
rg -n 'CommittedCandidate|VerifiedSolution|FinalResult|PublishableResult' adapters/common/src/main/java rpdptw/application/src/main/java apps/api/src/main/java
rg -n 'ResultPublisher|RunStateRepository|SubmissionPort|CancellationPort|WorkflowExecutionPort' rpdptw/application/src/main/java/com/ronext/rpdptw/application/compatibility
```

첫 세 검색은 target application/common에서 provider/legacy 침투가 0이어야 한다. 수치 검색은 이름 자체를 금지하지 않지만 production numeric assignment/default가 0이어야 하며 test fixture-local explicit config만 허용한다. Artifact type 검색은 `LegacyV1ResultAdapter`가 `PublishableResult`보다 앞선 type을 입력으로 받지 않는지 review한다. Port 검색은 shadow compare path에서 business mutation port 호출이 0인지 확인한다.

### 10.7 Link, heading, source drift와 diff

```bash
test -f docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
rg -n '^## (1|2|3|4|5|6|7|8|9|10|11|12|13)\.' docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
rg -n 'Master §15\.10|Master §16\.2|Architecture §2|Architecture §6\.6|Architecture §10~§17|Architecture §19|구현 계획 §4|구현 계획 §9\.9' docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
sha256sum docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md
git diff --check -- docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
git diff --no-index --check /dev/null docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
git status --short -- docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
git diff --name-only -- docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
```

새 untracked file에 대한 `git diff --no-index`는 내용 차이 때문에 exit code 1이 정상일 수 있다. Whitespace error text가 출력되면 실패로 처리한다. 전체 working tree의 다른 변경은 건드리지 않으며, AR-8 implementation evidence에서는 시작 시 저장한 status와 종료 status를 비교해 이 phase가 만든 path만 별도로 열거한다.

## 11. Step-by-step implementation checklist

### Entry와 보존

- [ ] 시작 `git status --short`, HEAD, Java/Maven과 source/design hash를 evidence에 저장했다.
- [ ] AR-5/6/7 문서와 evidence digest가 모두 `DONE`임을 확인했다.
- [ ] Target reactor module과 `legacy/current-app` 이동 manifest가 존재한다.
- [ ] Legacy content hash와 AR-0 이동 전 hash의 관계를 설명했다.
- [ ] Legacy characterization suite가 green이다.
- [ ] Characterization drift 또는 user-owned overlap이 있으면 구현을 중단했다.

### Adapter와 provenance

- [ ] Adapter test/resource를 production보다 먼저 작성했다.
- [ ] Intended compile failure를 보존했다.
- [ ] Skeleton 뒤 intended semantic assertion failure를 다시 보존했다.
- [ ] 모든 direct/alias/coercion/ignored mapping에 rule ID와 authority ref가 있다.
- [ ] `reqDate/dueDate`, order vehicle feature alias conflict test가 있다.
- [ ] Decimal `D/U`, numeric truncation, order-level taskTime과 ambiguous datetime을 추측 없이 거부한다.
- [ ] Unknown-field policy를 hidden default로 만들지 않는다.
- [ ] Raw/canonical/policy/decision fingerprint가 재현 가능하다.
- [ ] Result adapter input은 `PublishableResult`뿐이다.

### Compatibility와 shadow

- [ ] Characterization case마다 matrix row가 정확히 하나 있다.
- [ ] 모든 row가 허용된 네 classification 중 하나다.
- [ ] Approval 없는 intentional break가 cutover를 차단한다.
- [ ] Shadow test를 production보다 먼저 작성하고 unclassified diff red를 보았다.
- [ ] Shadow가 normal publisher/state/submission/cancel/workflow port를 호출하지 않는다.
- [ ] Completion permutation이 report/digest를 바꾸지 않는다.
- [ ] Shadow report가 raw→canonical→target result lineage를 연결한다.

### Idempotency, cancellation, status와 retrieval

- [ ] Same key/same digest가 같은 solve/artifact identity로 수렴한다.
- [ ] Same key/different digest/version은 typed conflict다.
- [ ] Retry가 seed/warm start/config/worker identity를 바꾸지 않는다.
- [ ] Cancellation intent, actual termination과 last completed boundary가 분리된다.
- [ ] Exceptional status를 legacy `RUNNING/COMPLETED`로 뭉개지 않는다.
- [ ] Candidate/one-pass/final FAIL artifact는 정상 retrieval되지 않는다.
- [ ] Both-pass report와 payload digest가 retrieval에 포함된다.

### Cutover와 rollback

- [ ] Cutover test를 production보다 먼저 작성하고 evidence/CAS red를 보았다.
- [ ] Matrix/shadow/approval/evidence digest가 모두 있어야 cutover한다.
- [ ] Logical pointer는 expected generation CAS로만 바뀐다.
- [ ] Stale/duplicate/concurrent cutover fault test가 있다.
- [ ] Rollback은 exact previous version으로 pointer만 되돌린다.
- [ ] Cutover/rollback 전후 artifact set/digest가 같다.
- [ ] Rollback 뒤 status/retrieval/idempotency가 같은 identity를 보존한다.
- [ ] `legacy/current-app`를 삭제하지 않았다.

### Verification과 evidence

- [ ] Adapter/application/API targeted green report가 있다.
- [ ] Owner module 전체 test와 verify가 통과했다.
- [ ] `apps/api,apps/worker -am verify`가 통과했다.
- [ ] Reactor `mvn verify`와 architecture rule이 통과했다.
- [ ] Provider/customer/hidden-value/legacy package scan violation이 0이다.
- [ ] `git diff --check`와 changed-path scope가 clean이다.
- [ ] Evidence bundle과 handoff digest가 있다.

## 12. Deliverables와 evidence bundle

### 12.1 Deliverable

| Deliverable | 최소 내용 | Consumer |
|---|---|---|
| Legacy characterization report | Endpoint/worker/finalize case ID, stable observation, dynamic field matcher, source hash | Matrix/shadow, rollback |
| Versioned adapter | Exact legacy version, typed mapping error, rule policy | Target preparation path |
| Adapter provenance | Raw/canonical/policy/decision fingerprints | Verifier/result lineage, audit |
| Semantic compatibility matrix | 모든 row/classification/decision/approval/test/evidence | Cutover gate, product review |
| Shadow report | Classified diff, no-side-effect proof, artifact digests | Cutover approval |
| Versioned lifecycle facade | Idempotency/cancel/status/retrieval preservation | Internal API composition |
| Logical cutover pointer | Active/previous version, generation, matrix/shadow/approval digest | Local API harness, rollback |
| Rollback receipt | From/to generation, preserved artifact set digest, replay outcome | Operations handoff |
| Legacy disposition decision | `KEEP_ISOLATED`, rollback window, removal blockers | 후속 physical cutover roadmap |

### 12.2 Evidence bundle

구현 세션은 다음 구조를 만든다.

```text
target/codex-evidence/AR-8/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── adapters-common-red.txt
│   ├── application-red.txt
│   ├── shadow-red.txt
│   └── rollback-red.txt
├── green/
│   ├── adapters-common-surefire/
│   ├── application-surefire/
│   └── apps-api-failsafe/
├── regression/
│   ├── module-verify/
│   ├── reactor-verify/
│   └── architecture/
├── fingerprints/
│   ├── source-design-sha256.txt
│   ├── legacy-source-sha256.txt
│   ├── adapter-policy-sha256.txt
│   ├── compatibility-matrix-sha256.txt
│   └── result-artifact-sha256.txt
├── faults/
│   ├── idempotency-matrix.json
│   ├── cancellation-matrix.json
│   ├── cas-conflicts.json
│   └── publication-gate.json
├── reproducibility/
│   ├── shadow-permutations.json
│   └── repeated-report-digests.txt
├── compatibility/
│   ├── legacy-characterization.json
│   ├── semantic-matrix.yaml
│   ├── shadow-report.json
│   ├── cutover-receipt.json
│   └── rollback-receipt.json
├── diff/
│   ├── git-status-before.txt
│   ├── git-status-after.txt
│   ├── changed-files.txt
│   ├── git-diff-check.txt
│   └── forbidden-scan.txt
└── handoff.md
```

`evidence.json`은 상위 계획 §11.1 field에 더해 `predecessorEvidenceDigests`, `legacyCharacterizationDigest`, `adapterPolicyDigest`, `compatibilityMatrixDigest`, `shadowReportDigest`, `cutoverReceiptDigest`, `rollbackReceiptDigest`, `approvedBreakDecisionRefs`를 포함한다.

Timestamp/elapsed/provider execution ID는 metadata이며 semantic comparison이나 result fingerprint에 넣지 않는다.

## 13. Rollback, DONE/BLOCKED 판정, handoff와 scope exclusion

### 13.1 Rollback 단위

| 변경 종류 | Rollback |
|---|---|
| Adapter policy/schema | 기존 version을 삭제/수정하지 않고 새 version 활성화를 중지한다. Pointer를 이전 adapter/contract version으로 CAS 복원한다 |
| Application logical version | Cutover receipt의 exact previous version/generation으로 pointer CAS rollback |
| State/idempotency | Record를 삭제하지 않는다. 이전 version의 기존 solve/artifact identity를 그대로 조회한다 |
| Artifact/result | Immutable artifact를 삭제·덮어쓰기·재직렬화하지 않는다. Publication index/pointer만 복원 |
| POM/source | AR-8 change commit/path만 역방향 patch로 되돌린다. `git reset --hard`, broad checkout/restore와 사용자 변경 덮어쓰기 금지 |
| Legacy module | Rollback window 동안 `legacy/current-app`과 characterization fixture 유지 |
| Physical infrastructure | AR-8이 변경하지 않으므로 rollback 대상 아님 |

Rollback rehearsal가 실패하면 target pointer를 활성화하지 않는다. Partial cutover가 발생했으면 새로운 pointer write 전에 current generation과 artifact set을 read-only로 재조사하고, 승인된 receipt와 일치하는 CAS만 허용한다.

### 13.2 `DONE` AND gate

AR-8 구현은 다음을 모두 만족해야 `DONE`이다.

1. AR-5/6/7이 실제 evidence digest와 함께 `DONE`이다.
2. Legacy endpoint/worker/finalize characterization이 source hash와 함께 green이다.
3. 모든 새 production behavior에 intended red→semantic red→minimal green evidence가 있다.
4. 모든 semantic difference가 네 classification 중 하나이며 cutover-blocking unresolved row가 0이다.
5. Alias/coercion/ignored field의 provenance coverage가 100%다.
6. Same-key idempotency, cancellation intent/actual termination, status와 verified retrieval fault matrix가 green이다.
7. Shadow에 unclassified diff와 normal lifecycle side effect가 0이다.
8. Target/legacy 어느 경로도 both-pass 없이 정상 publication/retrieval되지 않는다.
9. Cutover와 rollback이 동일 artifact/idempotency identity로 재현된다.
10. Targeted test, module verify, apps verify, reactor verify와 architecture scan이 통과한다.
11. Evidence bundle digest와 다음 phase handoff가 존재한다.
12. Provider/public wire/official value/deferred variant를 임의 확정하지 않았다.

### 13.3 `BLOCKED` 판정

다음 중 하나면 `BLOCKED`다.

- AR-5/6/7 predecessor 문서/evidence가 없거나 `DONE`이 아님
- Legacy source/behavior가 baseline과 drift했는데 owner/approval 없이 원인을 확정할 수 없음
- Public route/status/error/version-selection의 unresolved row가 cutover에 필요함
- Intentional break approval ref가 없음
- Shadow에 unclassified difference가 남음
- Both-pass 전 raw result를 노출해야만 legacy parity를 만들 수 있음
- Rollback이 artifact, idempotency, cancellation 또는 retrieval identity를 잃음
- Provider resource migration, public wire break 또는 production deployment 변경이 필요함
- 사용자/다른 세션 변경과 overlap하여 안전하게 patch를 분리할 수 없음

작성 시점 판정은 `BLOCKED`다. `B-AR8-ENTRY-05/06/07`, `B-AR8-REACTOR`가 실제로 존재한다. 문서 작성 완료를 phase implementation 완료로 바꾸지 않는다.

### 13.4 다음 phase handoff

AR-9에는 다음을 넘긴다.

- 승인된 compatibility matrix digest와 unresolved row 0 증거
- Raw input → adapter provenance → canonical input → solve/result의 lineage
- Active logical workflow entry/version과 cutover/rollback receipt
- Idempotency/cancellation/status/retrieval fault matrix
- Both-pass target publication과 rollback 뒤 같은 artifact identity
- Legacy-only behavior와 target-only behavior의 명시적 분류

AR-9는 이 handoff만으로 시작할 수 없다. `Q-BENCH-02` 승인 수치와 compliant integer `D/U` fixture/digest가 별도로 있어야 official phase entry를 통과한다.

Physical provider cutover, GCP topology와 production deployment는 deferred `AR-11/RM-9`의 별도 scope다.

### 13.5 Scope exclusions와 금지 shortcut

이 phase에서 하지 않는다.

- GCP Workflow, Cloud Run, Cloud Storage, IAM, Cloud Build, Docker image 또는 production environment 변경
- Provider adapter/deployment module 생성
- `Q-BENCH-02` 값 추측, README `8/5000`, workflow timeout/retry의 official 승격
- Win fixture decimal `D/U`의 반올림·절삭·official normalization
- Optional variant, multi-trip/rotation, route pool/MIP 또는 apply/undo
- Public route/header/version/status/error body를 승인 없이 확정
- Legacy `double objective`, 일부 worker minimum 또는 raw GCS result를 target correctness evidence로 사용
- Candidate verifier/result verifier 중 하나를 생략
- Shadow에서 정상 publication/state/idempotency/cancellation record를 변경
- Same idempotency key에 다른 digest/version을 조용히 새 실행으로 처리
- Cancellation intent를 실제 `CANCELLED`, watchdog/resource/platform failure 또는 정상 완료로 이름 변경
- Status를 `RUNNING/COMPLETED` 두 값으로 축약해 의미 손실
- Rollback 시 artifact/state/legacy module 삭제
- Legacy source를 target base class/interface/fixture implementation으로 재사용
- Provider SDK/resource URI를 core/application port signature에 넣기
- Characterization green만으로 target compatibility/cutover `DONE` 주장
