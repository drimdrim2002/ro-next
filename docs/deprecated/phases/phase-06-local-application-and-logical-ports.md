# AR-6 / RM-8-local — Local application과 provider-neutral logical ports

```yaml
phase: AR-6
rm_mapping: RM-8-local
status: BLOCKED
document_status: COMPLETE
implementation_entry_gate: BLOCKED
implementation_entry_gate_reason: "AR-0 DONE reactor/evidence가 없고, AR-6 exit에 필요한 AR-5 DONE both-gate evidence도 없음"
document_role: 후속 LLM 구현 세션을 위한 테스트 우선 실행 명세
language: ko
last_updated: 2026-07-24
source_baseline:
  implementation_plan:
    path: docs/codex/implementation-plan.md
    last_updated: 2026-07-24
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
repository_baseline:
  git_head: 523c23e2e13410885b16e974efe40ffe598106ee
  git_branch_observed: codex/domain-design
  build_shape: single Maven jar; target multi-module reactor absent
  java: 25.0.3-amzn
  maven: 3.9.14
  baseline_test: "phase-00 isolated observation: mvn test PASS, existing test PASS"
  baseline_verify: "phase-00 isolated observation: mvn verify PASS"
  baseline_authority: "관리 세션 정정에 따라 동시 target 사용 중 개별 세션 Maven 결과는 baseline 판정에서 제외"
predecessor_documents:
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    gate: AR-0 DONE required before skeleton implementation
    observed_at_authoring: "관리 세션 기준 phase-00 status NOT_STARTED; isolated Java/Maven/test/verify baseline만 전달되었고 AR-0 DONE 구현 evidence는 없음"
  - path: docs/codex/phases/phase-05-verification-finalization-and-publication.md
    gate: AR-5 DONE required before both-gate publication/retrieval and AR-6 exit
    observed_at_authoring: "present; AR-5 DONE both-gate evidence 없음"
successor_documents:
  - path: docs/codex/phases/phase-07-logical-multi-round-coordinator.md
  - path: docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
evidence_root: target/codex-evidence/AR-6/<evidence-id>/
```

## 1. 목적, 사용법과 문서 계약

이 문서는 [전체 구현 계획 §9.7](../implementation-plan.md#97-ar-6--rm-8-local--local-application과-logical-ports)을 실행 가능한 file/API/test 순서로 구체화한다. 목표는 `rpdptw-application`이 소유하는 provider-neutral use case와 logical port를 local reference runtime에 조립하고, 입력 artifact에서 verified result retrieval까지의 digest·CAS·idempotency·cancellation 의미를 보존하는 것이다.

이 문서의 작성·자체 검증 상태는 `COMPLETE`지만 phase 구현 상태는 `BLOCKED`다. Phase-00만 현재 `NOT_STARTED`이고, AR-6은 `AR-0 DONE` reactor/evidence가 없어 Track A 구현에도 안전하게 진입할 수 없다. 코드 구현은 시작되지 않았으며, AR-5의 actual both-gate `DONE` evidence가 추가로 갖춰지기 전에는 Track B와 phase exit도 닫을 수 없다.

이 phase는 두 개의 gate를 의도적으로 분리한다.

1. `AR-0 DONE` 뒤에는 port/interface, deterministic fake, in-memory/filesystem adapter와 composition skeleton을 다른 core phase와 병행할 수 있다.
2. `AR-5 DONE` 뒤에만 실제 `SolveSnapshot`·`CommittedCandidate`·두 verifier report·`PublishableResult`를 연결하여 정상 publication/retrieval evidence를 만들고 `AR-6 DONE`을 판정할 수 있다.

Skeleton이나 mock의 존재는 두 번째 gate를 대체하지 않는다. 구현 세션은 이 문서를 작업 목록으로만 읽지 말고, 각 production 변경 앞에 지정된 test red와 첫 실패 관찰을 실제로 남겨야 한다.

### 1.1 상위 계획 §10의 13개 항목 대응

| 계약 항목 | 이 문서의 규정 위치 |
|---|---|
| 1. 실제 저장소 조사 | §3 |
| 2. Authority와 결정 상태 | §2 |
| 3. 현 상태→목표 gap | §3.3 |
| 4. 정확한 예상 경로 | §6 |
| 5. 예상 타입/API | §7 |
| 6. Test case 표 | §9 |
| 7. 테스트 우선 구현 순서 | §10 |
| 8. Exact 명령 | §11 |
| 9. Deliverable와 evidence | §12 |
| 10. Rollback | §13 |
| 11. 완료·중단 조건 | §14 |
| 12. Handoff | §15 |
| 13. Scope exclusions | §16 |

## 2. Authority, 결정 상태와 blocker

### 2.1 규범 근거와 충돌 처리

이 phase의 의미 authority는 다음 절에서 온다.

- [Master Design §4](../../master-design.md#4-구현-아키텍처와-책임-경계): immutable handoff, application logical responsibility, 두 gate publication과 provider isolation
- [Master Design §14.4](../../master-design.md#144-multi-round-official-execution): retry identity와 complete verified execution의 상위 계약. 이 phase는 공식 수치나 multi-round coordinator를 구현하지 않고 AR-7 handoff seam만 만든다.
- [Master Design §15.10](../../master-design.md#1510-rm-8--logical-port-integration과-compatibility-migration): logical port의 조기 병행과 verified end-to-end 뒤 cutover gate
- [Architecture Design §6.4~§6.6](../../architecture-design.md#64-rpdptw-application): application/profile/adapter/app module 책임
- [Architecture Design §10~§17](../../architecture-design.md#10-localsingle-run-runtime): local flow, identity/idempotency, port catalog, artifact/CAS, cancellation, observability, retry와 security
- [Architecture Design §19](../../architecture-design.md#19-maven-build-order와-implementation-phases): reactor order, `AR-6 / RM-8-local` gate와 선행 관계
- [Domain Design §3](../../domain-design.md#3-계층과-단방향-책임): logical execution은 immutable domain/result 의미를 소비하며 재해석하지 않는다는 경계
- [Domain Design §14.3](../../domain-design.md#143-multi-round-execution-contract): run lineage가 보존해야 할 identity/termination/verification 정보

세 설계와 상위 계획은 모두 `REVIEW`다. 따라서 §7의 package/type/method는 승인된 public Java API, HTTP wire API, JSON schema 또는 저장 schema가 아니라 **계획상 제안 API**다. 구현 중 승인된 외부 계약·ADR 또는 선행 phase의 owner API와 충돌하면 이 문서의 이름을 억지로 구현하지 않는다. 충돌한 exact path/type, 영향받는 test와 필요한 ADR을 evidence에 기록하고, authority 문서와 이 phase 문서를 같은 변경 단위에서 갱신하기 전 해당 slice를 `BLOCKED`로 둔다.

### 2.2 질문과 ADR 상태

| ID/authority | 현재 상태 | AR-6 판단 |
|---|---|---|
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | AR-6 local/port test를 막지 않는다. 모든 step/worker/watchdog 값은 fixture-local explicit 값이어야 하고 official/production default로 승격하지 않는다. |
| `Q-INFRA-01` | `DEFERRED` | Provider/product/physical topology를 질문·선택·구현하지 않는다. 이 phase는 logical port와 local adapter만 만든다. |
| `Q-VAR-01` | `DEFERRED` | Optional variant를 활성화하지 않고 현재 pair/terminal/bank/result 의미를 그대로 소비한다. |
| `ADR-ARCH-002` | 미승인 candidate | Canonical public JSON/wire schema를 확정하지 않는다. Local artifact codec은 internal/versioned 계획상 제안 encoding만 소유한다. |
| `ADR-ARCH-005` | 미승인 candidate | Artifact canonical encoding/digest/CAS의 public 또는 cross-provider 승인을 주장하지 않는다. Local contract가 향후 ADR 검증 기준이 된다. |
| `ADR-ARCH-006` | 미승인 candidate | Run state/lease/duplicate/publication consistency의 provider mapping을 확정하지 않는다. Local CAS fault evidence를 제공한다. |
| `ADR-ARCH-009` | 미승인 candidate | Cancellation/recovery payload의 외부 노출을 확정하지 않는다. Local status는 intent와 actual termination을 분리한다. |

### 2.3 Entry blocker와 non-blocker

| Blocker ID | 차단 범위 | 작성 시점 관찰 | 재개 조건 |
|---|---|---|---|
| `AR6-B01-AR0` | 모든 production/test skeleton 구현 | 목표 module/POM과 AR-0 phase 문서/evidence가 없음 | `AR-0 DONE`, target reactor build, architecture rule와 test convention 확인 |
| `AR6-B02-AR5` | `LocalSolveEndToEndIT`, 정상 publication/retrieval와 AR-6 exit | AR-5 문서/evidence 및 실제 verification types가 없음 | `AR-5 DONE`; actual `PublishableResult`와 두 `PASS` report를 application이 owner API로 소비 가능 |
| `AR6-B03-AUTHORITY-DRIFT` | 충돌한 slice | 작성 시점 source hash는 상위 계획 baseline과 일치 | 구현 시작 직전 hash drift review와 승인된 변경 반영 |
| `AR6-B04-ATOMIC-FS` | Filesystem publication을 정상으로 주장하는 slice | 구현 전 미측정 | 대상 filesystem에서 same-directory atomic move와 lock/CAS fault test 통과, 또는 승인된 대체 전략 |
| `AR6-B05-OWNER-API` | 선행 artifact를 복제하려는 slice | 선행 modules 없음 | AR-1~AR-5 실제 exported type/API를 import하고 application-side duplicate type 0건 |

`Q-BENCH-02`, decimal Win fixture, `Q-INFRA-01`, `Q-VAR-01`은 generic AR-6 local 구현의 blocker가 아니다. 대신 official benchmark, fixture 변환, provider adapter와 optional variant가 이 phase 범위 밖임을 강제한다.

## 3. 실제 저장소 조사와 현 상태→목표 gap

### 3.1 작성 시점 명령과 결과

Working directory는 `/Users/brown/workspace/ro-next`다. 작성 세션은 다음 read-only inventory/hash 명령을 사용했다.

```bash
git status --short
rg --files
rg -n '<module>|<packaging>|<artifactId>' --glob 'pom.xml' .
rg --files src | sort
sha256sum docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/codex/implementation-plan.md
shasum -a 256 data/win_poc_case.json data/ro_input_json_spec.pdf
```

Toolchain/build 공통 baseline은 관리 세션이 전달한 phase-00 격리 실행의 `mvn -version`, `mvn test`, `mvn verify` 결과를 사용한다. 이 문서 세션은 최종 검증을 위해 Maven을 다시 실행하지 않는다.

관찰 결과:

- Root [pom.xml](../../../pom.xml)은 `packaging`이 생략된 단일 `jar`이고 `<modules>`가 없다. Google Workflow Executions, Cloud Storage, Jackson과 JUnit이 root classpath에 직접 있다.
- Production은 legacy `com.ronext.optimizer` 아래 6개 class뿐이다. `OptimizationApiController`와 `OptimizationWorkerController`가 SDK client를 직접 만들며, `AlnsBatchEngine`은 `Map<String,Object>`와 `double objective`를 반환하는 placeholder다.
- Test는 [AlnsBatchEngineTest](../../../src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java) 1개뿐이다. Phase-00의 격리 실행에서 기존 `mvn test`와 `mvn verify`가 성공했으므로 이것을 공통 build baseline으로 사용한다. RPDPTW domain, artifact digest/CAS, idempotency, cancellation, verifier gate 또는 retrieval test는 없다.
- 여러 phase 문서 세션이 같은 `target/`을 동시에 사용할 수 있으므로 개별 세션의 비격리 Maven 결과는 repository 결함, phase red evidence 또는 blocker로 판정하지 않는다. 이 문서 작성 세션의 완료 검증은 source hash, link, heading, diff와 파일 scope에 한정한다.
- [README](../../../README.md)의 `parallelRuns=8`, `iterationsPerRun=5000`, [GCP workflow](../../../gcp/workflows/optimization.yaml)의 default retry/timeout과 [GCP README](../../../gcp/README.md)의 Cloud Run/Workflows/Storage topology는 legacy characterization 값이다.
- [Win PoC fixture](../../../data/win_poc_case.json)의 SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이고 소수 `D/U`가 있다. 이 phase는 fixture를 읽거나 변환하지 않는다.
- `rpdptw/application`, `adapters/common`, `apps/cli`, `apps/worker`, phase evidence와 지정 phase 파일은 작성 전 존재하지 않았다.
- Working tree에는 사용자/다른 세션 소유의 수정·삭제·untracked 파일이 다수 있다. 구현 세션도 이 변경을 reset, restore, stash, 이동 또는 정리해서는 안 된다.

### 3.2 Legacy는 characterization일 뿐 target authority가 아니다

| Legacy 동작 | 현재 관찰 | AR-6 목표와 차이 |
|---|---|---|
| Submit | UUID, `System.nanoTime`, `gs://`, implicit numeric fallback/clamp | Explicit identity/config/ref, same-key idempotency와 digest conflict; provider URI/default 금지 |
| Worker | SDK와 engine 직접 생성, raw map, candidate object overwrite | Composition root가 port/adapter/service 주입; immutable artifact create-once |
| Finalize | 존재하는 candidate 중 `double objective` 최솟값 | `PublishableResult`와 두 verifier `PASS`만 publication CAS 허용 |
| Status/retrieval | result blob이 없으면 `RUNNING`, 있으면 raw JSON 200 | Running/rejected/published를 구분하고 미검증 payload는 반환하지 않음 |
| Cancellation | 없음 | Idempotent intent, cooperative signal, actual termination과 마지막 completed boundary 분리 |
| Artifact identity | GCS key가 사실상 identity, digest check 없음 | Content digest/schema identity가 semantic reference; locator는 opaque adapter metadata |
| Retry/state | Workflow default retry, durable logical state 없음 | Expected-version CAS, logical operation identity, duplicate convergence/conflict |
| Telemetry | 요청/완료 timestamp 중심 | Provider-neutral correlation과 redaction; elapsed/completion order는 quality identity에서 제외 |

### 3.3 목표 gap

| 영역 | 현재 | AR-6 목표 |
|---|---|---|
| Maven | 단일 shaded JAR | AR-0이 만든 reactor 안에서 application/common adapter/CLI/worker leaf를 실제 연결 |
| Namespace | `com.ronext.optimizer` | 새 `com.ronext.rpdptw.application`과 `com.ronext.rpdptw.adapter` |
| Use case | Controller가 orchestration·SDK·serialization 혼합 | Application-owned submit/prepare/execute/publish/cancel/status/retrieve |
| Port | 없음 | §7.3의 11개 provider-neutral logical port |
| Artifact | mutable provider object | Digest-before-deserialization, create-once, schema/media identity, atomic local write |
| State | blob existence로 추정 | Versioned state, legal transition, expected-version CAS |
| Idempotency | 매번 새 UUID/run | Same key+same fingerprint 수렴, same key+different fingerprint conflict |
| Cancellation | 없음 | Intent/actual termination 분리, incomplete candidate discard |
| Publication | raw/single-pass candidate 가능 | AR-5의 actual both-pass `PublishableResult`만 CAS pointer 생성 |
| Retrieval | raw bytes 반환 | `Running`, `Rejected`, `Published` sealed result; published artifact digest 재검증 |
| Composition | 하나의 HTTP main | CLI/worker local composition root; app/core에 global SDK/default 없음 |
| Provider | GCP type과 URI 침투 | SDK/resource/URI 0인 application signatures; physical infrastructure deferred |

## 4. Phase 경계: 조기 skeleton과 both-gate 완료

### 4.1 Track A — `AR-0 DONE` 뒤 조기 병행 가능

Track A는 다음만 구현한다.

1. `rpdptw-application`의 계획상 제안 identity/value, inbound use case와 outbound port signature
2. legal state transition, expected-version CAS, idempotency key/digest conflict와 cancellation intent의 pure application test
3. `adapters/common`의 deterministic test fake, in-memory state/cancellation, filesystem artifact/state/publisher skeleton
4. digest 검증 뒤에만 codec을 호출하는 versioned internal artifact codec
5. CLI/worker composition root의 dependency wiring과 human-readable typed failure skeleton
6. Provider-neutral architecture test와 no-hidden-default scan

Track A test fixture가 가짜 `PASS` report 또는 test-only `PublishableResult` 모양을 사용하더라도 그것은 interface shape만 검증한다. `AR-5` evidence나 정상 publication evidence로 기록하지 않는다. Track A 상태는 최대 `IN_PROGRESS`다.

### 4.2 Track B — `AR-5 DONE` 뒤에만 수행

Track B는 선행 owner types를 복제하지 않고 직접 소비한다.

```text
actual SolveSnapshot
→ actual solver execution / CommittedCandidate
→ actual candidate verifier PASS
→ actual finalization/audit
→ actual result-integrity verifier PASS / PublishableResult
→ immutable local result artifact
→ expected-version publication CAS
→ verified retrieval
```

Track B에서 다음을 확인하기 전 정상 result를 만들지 않는다.

- Candidate verifier report가 정확히 `PASS`
- Result-integrity verifier report가 정확히 `PASS`
- `PublishableResult`의 problem/travel/profile/candidate/result lineage와 artifact refs가 일치
- Result artifact bytes의 claimed digest와 실제 SHA-256이 일치
- Expected run state version과 publication pointer의 이전 상태가 일치
- State를 `SUCCEEDED`로 바꾸는 CAS와 result pointer가 동일 logical publication operation으로 수렴

한 gate가 `FAIL`/미완료이거나 artifact/CAS가 충돌하면 state는 `PUBLICATION_REJECTED` 또는 typed integrity/failure 상태이고 normal payload는 retrieval되지 않는다.

## 5. Scope, module 책임과 dependency

| Module | 이 phase 책임 | Compile dependency | 금지 |
|---|---|---|---|
| `rpdptw-application` | Use case, logical port, execution identity/state/idempotency, service 조립 | `core`, `solver`, `verification` | Jackson, HTTP, provider SDK/URI/resource type, local filesystem |
| `adapters/common` | Internal JSON codec, filesystem/in-memory/local implementations | `core`, `verification`, `application` | Provider SDK, customer branch, solver 의미 재구현 |
| `apps/cli` | Local command entrypoint와 composition | application, common adapter, selected profile | Raw candidate 정상 출력, hidden config, SDK |
| `apps/worker` | Headless local worker entrypoint와 composition | application, common adapter, selected profile | HTTP/GCP event 의미, verifier 우회 |
| `apps/api` | 이 phase에서 변경하지 않음 | — | Public route/schema를 AR-6에서 확정 |

`rpdptw-application`은 port interface의 owner다. Adapter가 interface를 소유하고 application이 구현하는 역전은 금지한다. `adapters/common`의 filesystem locator/path, file lock과 JSON DTO는 application/domain identity가 아니다. App module은 composition root이므로 concrete adapter/profile/service를 생성할 수 있지만 semantic default를 만들 수 없다.

## 6. 정확한 예상 경로

아래는 구현 순서대로 **test 파일을 production 파일보다 먼저** 나열한다. 모든 신규 파일명은 계획상 고정 제안이다. 선행 phase가 동일 책임의 다른 승인 이름을 이미 만들었다면 중복 생성하지 말고 §2.1의 authority 절차를 따른다.

### 6.1 먼저 생성할 test와 test support

| 조치 | Repository-relative path | 연결 behavior |
|---|---|---|
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/architecture/ProviderNeutralPortArchitectureTest.java` | Port signature/provider dependency 금지 |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/service/SubmissionIdempotencyTest.java` | Same-key convergence/conflict |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/execution/RunStateTransitionTest.java` | Legal state와 intent/termination 분리 |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/service/CancellationStateMachineTest.java` | Idempotent cancellation과 discard |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/service/PublicationGateApplicationTest.java` | Both-pass와 expected-version gate |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/service/VerifiedResultRetrievalTest.java` | Running/rejected/published 분리 |
| 신규 | `rpdptw/application/src/test/java/com/ronext/rpdptw/application/support/ApplicationFakes.java` | Deterministic fake clock/ports/recorder; production runtime scope 금지 |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/ArtifactStoreContractTest.java` | Digest-before-deserialization, create-once |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/FileSystemArtifactStoreFaultTest.java` | Atomic move 실패 격리와 path boundary |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/RunStateRepositoryContractTest.java` | In-memory/filesystem expected-version CAS |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/FileSystemResultPublisherTest.java` | Publication pointer CAS/duplicate |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/SameProcessWorkerDispatcherTest.java` | Assignment identity와 deterministic dispatch |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/in/json/DeterministicArtifactJsonCodecTest.java` | Version/schema/ordered encoding과 verified-bytes precondition |
| 신규 | `adapters/common/src/test/java/com/ronext/rpdptw/adapter/out/local/TelemetryRedactionTest.java` | Correlation field와 secret/PII 금지 |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/LocalSolveEndToEndIT.java` | Actual AR-5 both-gate local flow |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/LocalCancellationIT.java` | Incomplete candidate discard와 actual termination |
| 신규 | `apps/worker/src/test/java/com/ronext/rpdptw/app/worker/WorkerLocalCompositionIT.java` | Headless assignment→outcome ref |
| 신규 | `apps/cli/src/test/java/com/ronext/rpdptw/app/cli/VerifiedResultRetrievalIT.java` | CLI가 published result만 정상 노출 |
| 신규 | `apps/cli/src/test/java/com/ronext/rpdptw/app/cli/RpdptwCliIT.java` | Exit code, required explicit config와 output |

### 6.2 Test red 뒤 생성할 production file

| 조치 | Repository-relative path | 책임 |
|---|---|---|
| 변경 | `rpdptw/application/pom.xml` | Core/solver/verification dependency와 test convention; provider/Jackson dependency 금지 |
| 변경 | `adapters/common/pom.xml` | Application/core/verification와 Jackson dependency; provider SDK 금지 |
| 변경 | `apps/cli/pom.xml` | Application/common adapter/selected profile와 CLI main packaging |
| 변경 | `apps/worker/pom.xml` | Application/common adapter/selected profile와 worker main packaging |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/SubmissionPort.java` | Idempotent submission inbound use case |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/PrepareSolveSnapshotUseCase.java` | Canonical owner artifacts로 snapshot 준비 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/ExecuteWorkerRunUseCase.java` | Explicit worker assignment 실행 |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/PublishVerifiedResultUseCase.java` | Both-gate result publication |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/RequestCancellationUseCase.java` | Cancellation intent |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/GetSolveStatusUseCase.java` | Versioned status |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/GetVerifiedResultUseCase.java` | Verified-only retrieval |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/ArtifactStore.java` | Immutable bytes/ref put/read |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/RunStateRepository.java` | Create/find/expected-version CAS |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/WorkerDispatcher.java` | Logical assignment dispatch/inspect/stop |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/WorkflowExecutionPort.java` | Top-level manifest start/inspect/stop seam |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/CancellationPort.java` | Intent record/signal |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/ProfileCatalogPort.java` | Exact approved profile snapshot lookup |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/SecretResolver.java` | Opaque bootstrap-only secret seam |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/ResultPublisher.java` | Verified result pointer CAS |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/TelemetryPort.java` | Structured portable event |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/Clock.java` | Application/monotonic observation only |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ArtifactRef.java` | Kind/schema/media/digest/length/opaque locator |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ExecutionIdentity.java` | Submission/Solve/WorkerRun/Attempt IDs |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/RunState.java` | Lifecycle/version/cancellation/termination/result ref |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/RunStateTransition.java` | Legal transition pure function |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/SubmissionCommand.java` | Explicit idempotency + artifact/profile/config refs |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/SubmissionReceipt.java` | Stable receipt |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/CancellationRequest.java` | Intent reason/subject metadata |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/VerifiedResultRetrieval.java` | Running/Rejected/Published sealed outcome |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/WorkerAssignment.java` | Retry-stable logical assignment |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ExecutionManifest.java` | Exact refs/config/seed lineage; no official defaults |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/execution/ApplicationFailure.java` | Typed stable failure category |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/service/SolveApplicationService.java` | Submit/prepare/publish/cancel/status/retrieve orchestration |
| 신규 | `rpdptw/application/src/main/java/com/ronext/rpdptw/application/service/WorkerRunApplicationService.java` | Solver→candidate verifier→finalizer/result verifier 조립 |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/in/json/DeterministicArtifactJsonCodec.java` | Verified bytes만 typed artifact로 decode |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/FileSystemArtifactStore.java` | Atomic create-once artifact |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/InMemoryRunStateRepository.java` | Deterministic in-process CAS |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/FileSystemRunStateRepository.java` | Lock + atomic file CAS/persistence |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/SameProcessWorkerDispatcher.java` | Same-process logical dispatch |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/InMemoryCancellationPort.java` | Cooperative token/intent |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/FileSystemResultPublisher.java` | Atomic result pointer CAS |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/SameProcessWorkflowExecutionAdapter.java` | Local top-level execution seam |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/StaticProfileCatalogAdapter.java` | Exact profile snapshot; fallback 없음 |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/RejectingSecretResolver.java` | Local execution에 secret 요구가 생기면 명시적 실패 |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/StructuredLogTelemetryAdapter.java` | Redacted structured event |
| 신규 | `adapters/common/src/main/java/com/ronext/rpdptw/adapter/out/local/SystemApplicationClock.java` | App metadata/elapsed clock adapter |
| 신규 | `apps/cli/src/main/java/com/ronext/rpdptw/app/cli/RpdptwCli.java` | `submit/run/cancel/status/result` local command |
| 신규 | `apps/cli/src/main/java/com/ronext/rpdptw/app/cli/CliCompositionRoot.java` | Explicit workspace와 dependency wiring |
| 신규 | `apps/worker/src/main/java/com/ronext/rpdptw/app/worker/RpdptwWorker.java` | Headless local worker main |
| 신규 | `apps/worker/src/main/java/com/ronext/rpdptw/app/worker/WorkerCompositionRoot.java` | Assignment/ref 기반 dependency wiring |

이 phase에는 source/resource/POM의 이동이나 삭제가 없다. `src/main/java/com/ronext/optimizer/**`, root POM, `gcp/**`, `data/**`, Dockerfile과 README를 수정·이동하지 않는다. AR-0이 이미 legacy를 `legacy/current-app`으로 이동했다면 그 결과도 건드리지 않는다.

## 7. 계획상 제안 package/type/API

### 7.1 공통 규칙

아래 signature는 후속 구현 세션의 ambiguity를 없애기 위한 **계획상 제안 API**다.

- External/public HTTP, CLI JSON, database 또는 provider API 승인이 아니다.
- `byte[]`는 입·출력에서 defensive copy하고 `VerifiedArtifactBytes`만 codec에 전달한다.
- `ArtifactRef.opaqueLocator`는 adapter가 해석하며 semantic/content/result fingerprint 계산에서 제외한다.
- Port failure는 provider SDK exception/type을 밖으로 노출하지 않고 `ApplicationFailure`의 stable category와 safe metadata로 변환한다.
- `Clock`, path, thread count, completion order, elapsed와 secret은 algorithm/result fingerprint에 들어가지 않는다.
- AR-1~AR-5 owner type을 application package에서 재정의하지 않는다.

### 7.2 Inbound use case

```java
package com.ronext.rpdptw.application.port.in;

public interface SubmissionPort {
    SubmissionReceipt submit(SubmissionCommand command);
}

public interface PrepareSolveSnapshotUseCase {
    ArtifactRef prepare(ExecutionIdentity.SolveId solveId);
}

public interface ExecuteWorkerRunUseCase {
    WorkerRunOutcome execute(WorkerAssignment assignment);
}

public interface PublishVerifiedResultUseCase {
    PublicationReceipt publish(
            ExecutionIdentity.SolveId solveId,
            PublishableResult publishableResult,
            RunState.StateVersion expectedVersion);
}

public interface RequestCancellationUseCase {
    CancellationReceipt request(CancellationRequest request);
}

public interface GetSolveStatusUseCase {
    RunState get(ExecutionIdentity.SolveId solveId);
}

public interface GetVerifiedResultUseCase {
    VerifiedResultRetrieval get(ExecutionIdentity.SolveId solveId);
}
```

`PublishableResult`, `WorkerRunOutcome`와 verifier report type은 AR-4/AR-5 owner module의 실제 exported type을 import한다. 이름이나 package가 다르면 중복 wrapper를 semantic owner처럼 만들지 않고 얇은 application command/reference만 둔다.

### 7.3 Outbound port

```java
public interface ArtifactStore {
    ArtifactRef putIfAbsent(ArtifactWrite write);
    VerifiedArtifactBytes readVerified(ArtifactRef reference);
}

public interface RunStateRepository {
    Optional<RunState> find(ExecutionIdentity.SolveId solveId);
    RunState createIfAbsent(RunState initial);
    RunState compareAndSet(
            ExecutionIdentity.SolveId solveId,
            RunState.StateVersion expectedVersion,
            RunState next);
}

public interface WorkerDispatcher {
    DispatchReceipt dispatch(WorkerAssignment assignment);
    DispatchStatus inspect(DispatchHandle handle);
    StopReceipt requestStop(DispatchHandle handle);
}

public interface WorkflowExecutionPort {
    WorkflowHandle start(ArtifactRef executionManifestRef, IdempotencyKey key);
    WorkflowStatus inspect(WorkflowHandle handle);
    StopReceipt requestStop(WorkflowHandle handle);
}

public interface CancellationPort {
    CancellationReceipt request(CancellationRequest request);
    CancellationSnapshot find(CancellationSubject subject);
    CancellationSignal signalFor(CancellationSubject subject);
}

public interface ProfileCatalogPort {
    ProfileSnapshot resolveExact(ProfileSelection exactSelection);
}

public interface SecretResolver {
    OpaqueSecret resolve(SecretReference reference);
}

public interface ResultPublisher {
    PublicationReceipt compareAndSet(PublicationCommand command);
    Optional<PublishedResultPointer> find(ExecutionIdentity.SolveId solveId);
}

public interface TelemetryPort {
    void emit(TelemetryEvent event);
}

public interface Clock {
    Instant wallTime();
    long monotonicNanos();
}
```

`SubmissionPort`는 inbound application port다. 나머지 catalog는 application-owned outbound port다. `SecretResolver`는 local runtime에서 `RejectingSecretResolver`로 조립하며 core/solver/verifier에 secret을 전달하지 않는다.

### 7.4 주요 value와 불변조건

| 계획상 제안 type | 주요 field/의미 | 불변조건 |
|---|---|---|
| `ArtifactRef` | `kind`, `schemaVersion`, SHA-256 digest, length, media type, opaque locator | Digest/schema/content identity는 non-null/exact; locator는 equality/fingerprint에서 제외 |
| `VerifiedArtifactBytes` | Defensive-copy bytes + verified ref | Store가 length/digest를 검증한 뒤에만 생성; codec 외부에서 위조 생성 불가 |
| `SubmissionCommand` | Explicit submission/solve ID, idempotency key, input/profile/config refs | 누락/fallback 없음; key와 semantic refs로 submission fingerprint 계산 |
| `ExecutionManifest` | Snapshot/build/runtime/algorithm/config/seed/run identity refs | 모든 값 explicit; official flag/default 없음; timestamp/locator/secret 제외 |
| `WorkerAssignment` | Solve/worker/run/attempt, snapshot/warm-start/config refs, seed/requested steps | Retry는 attempt만 변경; logical worker ID, seed, warm start, steps는 고정 |
| `RunState` | Lifecycle, monotonically increasing version, cancellation intent, actual termination, result ref/failure | Terminal state 역행 금지; `SUCCEEDED`만 verified result ref 필수; intent와 termination 별도 |
| `VerifiedResultRetrieval` | sealed `Running`, `Rejected`, `Published` | Running/Rejected에 normal payload 없음; Published는 both-pass pointer와 verified bytes만 |
| `ApplicationFailure` | Stable code, scope, retryability, safe evidence refs | Provider exception/resource/secret/PII를 public field/message에 넣지 않음 |
| `PublicationCommand` | Publishable result ref, candidate/result PASS refs, expected state version | 두 PASS와 lineage/digest 일치; same digest duplicate만 수렴 |

### 7.5 Error contract

| Stable failure code | 발생 조건 | 상태/side effect |
|---|---|---|
| `IDEMPOTENCY_CONFLICT` | Same key에 다른 submission fingerprint | 기존 receipt/state/artifact/dispatch 보존 |
| `ARTIFACT_NOT_FOUND` | 참조 artifact 없음 | Decode 호출 0, state 변경 0 |
| `ARTIFACT_DIGEST_MISMATCH` | Claimed ref와 actual bytes SHA-256 불일치 | Deserialize 0, corruption/integrity telemetry |
| `ARTIFACT_SCHEMA_MISMATCH` | Kind/media/schema contract 불일치 | Deserialize 또는 owner mapping 전 거부 |
| `ARTIFACT_IMMUTABILITY_CONFLICT` | 같은 logical locator에 다른 digest overwrite 시도 | 기존 bytes 보존 |
| `STATE_NOT_FOUND` | Unknown solve/run | 새 state 추정 생성 금지 |
| `STATE_VERSION_CONFLICT` | Stale expected version | Retry는 caller가 최신 state를 다시 읽고 같은 logical operation으로 판단 |
| `ILLEGAL_STATE_TRANSITION` | Terminal 역행/검증 전 success 등 | CAS 수행 전 거부 |
| `CANCELLATION_CONFLICT` | 다른 subject/reason identity를 같은 key로 오염 | 기존 intent 보존 |
| `PUBLICATION_NOT_VERIFIED` | Candidate/result gate 중 하나가 PASS 아님 | `PUBLICATION_REJECTED`; normal pointer 없음 |
| `PUBLICATION_CONFLICT` | Same solve에 다른 result digest 또는 stale state | 어느 result도 임의 선택하지 않음 |
| `RESULT_NOT_AVAILABLE` | Running/rejected/unpublished | 상태 variant 반환; payload 없음 |
| `PORT_FAILURE` | Local I/O/lock/atomic operation 실패 | Provider-neutral safe category; partial mutation 0 |

Application은 input/binding/search/verification의 owner error를 `UNASSIGNED`나 정상 `SUCCEEDED`로 바꾸지 않는다. Cancellation을 `MAX_STEPS_REACHED`로, watchdog/resource/platform failure를 `CANCELLED`로 다시 이름 붙이지 않는다.

## 8. Artifact, state, idempotency, cancellation과 retrieval invariant

### 8.1 Artifact write/read

```text
write:
  bytes defensive copy
  → actual SHA-256 + length 계산
  → claimed ref와 비교
  → 같은 directory temp create
  → bytes write + flush
  → target create-once atomic move
  → existing same digest: idempotent success
  → existing different digest: conflict, no overwrite

read:
  ref로 opaque locator resolve
  → bytes read
  → length + SHA-256 검증
  → VerifiedArtifactBytes 생성
  → 그 뒤에만 schema-aware deserialization
```

Path normalization 뒤 artifact root 밖으로 나가는 locator, symlink escape와 absolute caller path를 거부한다. Temp filename, filesystem mtime와 workspace absolute path는 artifact/result fingerprint가 아니다.

### 8.2 State와 CAS

State version은 create 시 고정된 initial version에서 한 번의 successful CAS마다 정확히 1 증가한다. `compareAndSet(expected, next)`는 다음을 atomic하게 보장한다.

- 현재 version이 expected와 다르면 `STATE_VERSION_CONFLICT`, write 0
- `RunStateTransition`이 허용하지 않으면 `ILLEGAL_STATE_TRANSITION`, write 0
- Same logical operation 재호출이 이미 같은 target digest/state를 만들었으면 idempotent receipt
- Terminal state에 다른 result/termination을 덮어쓰지 않음
- Filesystem adapter는 state lock과 same-directory atomic replacement를 사용하고 중간 temp 파일을 정상 state로 읽지 않음

### 8.3 Submission idempotency

Submission fingerprint는 idempotency key 자체가 아니라 exact input/profile/config/manifest content identity의 canonical combination이다. Raw secret, opaque locator, timestamp와 workspace path는 제외한다.

| 재호출 | 결과 |
|---|---|
| Same key + same fingerprint | 최초 `SolveId`/receipt 반환, artifact/state/dispatch 중복 0 |
| Same key + different fingerprint | `IDEMPOTENCY_CONFLICT`, 기존 실행 보존 |
| Different key + same fingerprint | 별도 explicit submission으로 허용; result dedup 정책을 임의 도입하지 않음 |

### 8.4 Cancellation

Cancellation은 idempotent intent이며 cooperative safe point에서 소비한다.

- Intent timestamp/reason과 worker actual termination을 분리한다.
- Cancellation을 본 미완료 COW candidate는 전체 discard한다.
- Completed-step, adaptive state와 candidate artifact를 미완료 step에서 전진시키지 않는다.
- 마지막 committed candidate가 있어도 두 gate 없이는 normal result가 아니다.
- Late cancellation과 실제 `MAX_STEPS_REACHED`, watchdog/resource/platform failure를 있는 그대로 기록한다.
- Stop request success는 worker가 `CANCELLED`했다는 증거가 아니다. Actual termination record가 필요하다.

### 8.5 Publication과 retrieval

정상 publication은 다음 AND gate다.

```text
candidate verifier PASS
AND result-integrity verifier PASS
AND PublishableResult lineage/digest match
AND immutable result artifact digest verified
AND expected RunState version matches
AND no conflicting published digest
```

Retrieval은 state나 blob existence를 추정하지 않는다.

- `Running`: non-terminal 또는 cancellation requested; normal payload 없음
- `Rejected`: input/binding/failure/cancel/publication rejection; normal payload 없음
- `Published`: `SUCCEEDED`, publication pointer와 both-pass report refs가 일치하고 result bytes digest를 다시 검증한 경우

Published pointer가 있는데 state가 `SUCCEEDED`가 아니거나 그 반대이면 integrity failure이며 한쪽을 진실로 추정하지 않는다.

## 9. 세분화된 test case

표의 test는 순서대로 작성한다. `첫 실패 관찰`이 의도대로 나타나지 않으면 production logic으로 넘어가지 않는다.

| ID | Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|---|
| T01 | `ProviderNeutralPortArchitectureTest.declaresApplicationOwnedPortCatalog` | Architecture; Arch §6.4/§12 | 필수 7 inbound + 10 outbound type 목록 | application package에 모두 존재 | 최초에는 `package ...port... does not exist` 또는 missing type exact list | 모든 계획상 API type load, owner module 일치 |
| T02 | `ProviderNeutralPortArchitectureTest.exposesNoProviderOrUriTypes` | Architecture; Master §4.4 | Port public signature bytecode | Google/AWS/Azure/HTTP/`URI`/filesystem type 0 | 위반 signature FQCN을 assertion에 열거 | logical identity/value만 노출 |
| T03 | `SubmissionIdempotencyTest.sameKeyAndFingerprintReturnsOriginalReceiptOnce` | Unit/fault; Arch §11.3 | Fixed IDs, fake clock, same refs, counting repository/dispatcher | 같은 receipt; create/dispatch 각 1 | `expected dispatchCount <1> but was <2>` 또는 SolveId 불일치 | duplicate side effect 0 |
| T04 | `SubmissionIdempotencyTest.sameKeyDifferentFingerprintConflictsWithoutMutation` | Unit/fault | Same key, input digest B로 재호출 | `IDEMPOTENCY_CONFLICT`; 최초 state/ref 보존 | `Expected ApplicationFailure[IDEMPOTENCY_CONFLICT] but nothing was thrown` | state version/digest/dispatch count 불변 |
| T05 | `RunStateTransitionTest.rejectsStaleVersionAndIllegalTerminalRegression` | Unit/contract | Version 3 RUNNING, stale 2; SUCCEEDED→RUNNING | `STATE_VERSION_CONFLICT`, `ILLEGAL_STATE_TRANSITION` | stale update가 성공하거나 version 4가 됨 | write 0, exact code |
| T06 | `CancellationStateMachineTest.recordsIntentSeparatelyFromActualTermination` | Unit/fault; Master §13 | CANCEL_REQUESTED 뒤 worker actual watchdog/cancel variants | Intent와 actual termination 둘 다 보존 | expected actual `WATCHDOG_REACHED` but was `CANCELLED` 같은 rename 관찰 | 입력 termination exact 보존 |
| T07 | `ArtifactStoreContractTest.rejectsDigestMismatchBeforeDeserialization` | Corruption/contract; Arch §16.2 | Claimed A, corrupt B, decode-spy | `ARTIFACT_DIGEST_MISMATCH`, decodeCalls=0 | `expected <0> decode calls but was <1>` 또는 exception 없음 | Digest failure precedes decode |
| T08 | `ArtifactStoreContractTest.createOnceConvergesOnlyForSameDigest` | Contract/fault | 같은 bytes 두 put, 같은 locator 다른 bytes | Same ref idempotent; different digest conflict | 두 번째 bytes가 기존 target을 덮어씀 | 최초 bytes/digest 유지 |
| T09 | `FileSystemArtifactStoreFaultTest.atomicMoveFailureLeavesNoReadableArtifact` | Fault | Injected atomic mover failure | Target 없음, temp 정리/격리, `PORT_FAILURE` | Partial target을 `readVerified`가 읽음 | 관찰 가능한 artifact 0 |
| T10 | `FileSystemArtifactStoreFaultTest.rejectsPathAndSymlinkEscape` | Security | `../`, absolute, symlink-outside locators | Root 밖 read/write 모두 거부 | 외부 sentinel 파일 생성/읽기 | 외부 파일 불변, safe failure |
| T11 | `RunStateRepositoryContractTest.expectedVersionCasIsAtomicForInMemoryAndFilesystem` | Contract/fault | 두 writer가 same expected version 사용 | 정확히 하나 성공, 하나 conflict | 둘 다 성공 또는 last-writer-wins | version 1회 증가, winner state만 |
| T12 | `FileSystemResultPublisherTest.sameDigestDuplicateConvergesAndDifferentDigestConflicts` | Contract/fault | Same solve/result A twice, then B | A duplicate same receipt; B conflict | pointer가 B로 교체됨 | pointer A/digest/state version 유지 |
| T13 | `DeterministicArtifactJsonCodecTest.requiresVerifiedBytesAndExactSchema` | Unit/architecture | Verified wrapper, wrong schema, unordered input attempt | Raw bytes decode API 없음; wrong schema 거부; stable encoding | raw `byte[]` overload 존재 또는 digest가 iteration order로 변함 | exact schema/version, stable digest |
| T14 | `SameProcessWorkerDispatcherTest.preservesLogicalIdentityAcrossAttemptRetry` | Unit/fault | Same worker run, attempt 1/2, fixed seed/warm start/config | Attempt만 다르고 logical assignment invariant 동일 | seed/warm start/requested steps 중 하나 변경 | retry identity exact |
| T15 | `PublicationGateApplicationTest.rejectsCandidatePassWithoutResultPass` | Integration/fault; Master §14.1 | Actual candidate PASS + result FAIL/incomplete | `PUBLICATION_NOT_VERIFIED`, pointer 없음 | `expected no published pointer` but found result | state `PUBLICATION_REJECTED`, payload 없음 |
| T16 | `PublicationGateApplicationTest.rejectsResultPassWithoutCandidatePass` | Integration/fault | Candidate FAIL/incomplete + fabricated result PASS | Publication 차단 | candidate report 없이 publish 성공 | pointer/state unchanged |
| T17 | `PublicationGateApplicationTest.rejectsLineageOrExpectedVersionMismatch` | Corruption/fault | Result digest/report lineage mismatch, stale state | Typed integrity/CAS failure | stale/mismatched result가 SUCCEEDED | 기존 state/artifacts만 유지 |
| T18 | `VerifiedResultRetrievalTest.distinguishesRunningRejectedAndPublished` | Unit/integration | 세 state와 pointer matrix | Sealed variant 정확, first two payload 없음 | Running/rejected가 raw candidate/result bytes 반환 | Published만 verified bytes |
| T19 | `VerifiedResultRetrievalTest.rejectsTamperedPublishedBytes` | Corruption | Publish 뒤 result file 1 byte 변경 | Digest mismatch, 정상 payload 없음 | tampered JSON을 deserialize/return | integrity failure, decodeCalls=0 |
| T20 | `TelemetryRedactionTest.emitsPortableCorrelationWithoutSecretOrLocator` | Security/observability | Secret/path/PII 포함 port failure | Allowed identity/digest/category만 기록 | log에 secret, raw path, address 또는 full input 포함 | forbidden field/value 0 |
| T21 | `LocalSolveEndToEndIT.publishesOnlyAfterBothVerifierPass` | Failsafe integration; AR-5 actual authority | AR-1~AR-5 hand-built compliant fixture, fixed fake clock/config/seed | input/snapshot/search→두 PASS→CAS→Published, exact lineage | candidate 또는 single-pass 단계에서 retrieval이 `Published` | only post-result-PASS publication |
| T22 | `LocalSolveEndToEndIT.repeatedEnvelopeHasSameSemanticArtifactDigests` | Reproducibility | Same snapshot/profile/config/build/seed/order/steps, 다른 workspace/clock | Semantic artifact/result digests 동일 | path/timestamp/thread가 digest를 바꿈 | exact digest equality; metadata만 다름 |
| T23 | `LocalCancellationIT.discardsIncompleteCandidateAndPreservesActualTermination` | Fault integration | Safe-point cancel injected mid-step | Candidate artifact 없음; completed count 불변; actual `CANCELLED` | partial candidate 존재 또는 `SUCCEEDED`/`MAX_STEPS_REACHED` | cancel intent + actual termination + discard evidence |
| T24 | `WorkerLocalCompositionIT.executesOnlyReferencedVerifiedAssignment` | Failsafe integration | Assignment artifact, corrupt/sound refs | Corrupt ref는 worker 호출 전 거부; sound는 outcome ref | worker가 raw path/provider URI를 직접 읽음 | verified refs only, headless exit mapping |
| T25 | `VerifiedResultRetrievalIT.cliReturnsOnlyBothGatePublishedResult` | Failsafe integration | Running/rejected/published workspaces | 상태별 exit/output; success는 result ref/digest | rejected raw payload가 stdout에 노출 | normal output only for Published |
| T26 | `RpdptwCliIT.requiresExplicitWorkspaceIdentityAndRunConfig` | Failsafe/negative | 각 required option 누락 | Usage + nonzero exit; numeric/default 주입 없음 | README legacy `8/5000` 또는 cwd/default path 사용 | 모든 semantic input explicit |

T01의 compile failure는 API skeleton을 처음 만드는 경우에만 유효하다. Signature skeleton을 최소로 만든 뒤 T03~T20은 compile 가능한 상태에서 assertion/typed failure red를 다시 관찰해야 한다. `UnsupportedOperationException`, dependency download 실패, unrelated legacy failure와 test 미발견은 의미 red가 아니다.

## 10. 테스트 우선 step-by-step implementation checklist

각 checkbox는 후속 구현 세션이 evidence에 순서와 exit code를 남겨야 하는 실행 단위다.

### 10.1 Gate와 baseline

- [ ] `git status --short --untracked-files=all`과 대상 경로 hash를 기록하고 사용자/다른 세션 변경을 분리한다.
- [ ] §11.1의 source hash를 재계산한다. Drift가 있으면 관련 절 diff를 읽고 `AR6-B03`을 판정한다.
- [ ] `AR-0 DONE` evidence, reactor module/POM, Surefire/Failsafe/architecture rule convention을 확인한다.
- [ ] Track A 시작 시 `AR-5` 부재를 명시하고 production publication success를 비활성 상태로 둔다.
- [ ] Build baseline은 phase-00의 격리 실행 `mvn test`/`mvn verify` 성공을 사용한다. Shared `target/`에서 동시에 얻은 개별 세션 실패를 repository 결함이나 blocker로 기록하지 않는다.

### 10.2 Slice A — Port/identity compile red

- [ ] T01/T02 test를 production type보다 먼저 생성한다.
- [ ] Targeted Maven으로 missing package/type compile failure를 확인하고 report를 `red/api-skeleton/`에 보존한다.
- [ ] Test가 요구하는 최소 interface/value signature만 만든다. Method body/business logic/local adapter를 추가하지 않는다.
- [ ] T01을 green으로 만들고 T02의 provider/URI violation 0을 확인한다.

### 10.3 Slice B — State/idempotency/cancellation

- [ ] T03~T06과 `ApplicationFakes`를 먼저 작성한다.
- [ ] Same-key duplicate dispatch, same-key conflict 부재, stale CAS success 또는 termination rename 중 의도한 assertion failure를 관찰한다.
- [ ] `RunStateTransition`, `InMemoryRunStateRepository`, `InMemoryCancellationPort`의 최소 구현만 추가한다.
- [ ] T03~T06 targeted green 뒤 application module test를 실행한다.

### 10.4 Slice C — Artifact/codec/filesystem

- [ ] T07~T13을 먼저 작성한다.
- [ ] Digest mismatch에서 decoder가 호출되는 red, overwrite red, stale CAS red와 atomic-move partial artifact red를 각각 관찰한다.
- [ ] `FileSystemArtifactStore`, verified-bytes wrapper, internal codec, filesystem repository/publisher를 순서대로 최소 구현한다.
- [ ] Digest-before-deserialization, create-once, path confinement, expected-version CAS와 same-digest convergence가 모두 green인지 확인한다.
- [ ] Test temp directory는 JUnit이 제공한 explicit directory만 사용하고 repository/data/root를 target으로 쓰지 않는다.

### 10.5 Slice D — Dispatch/workflow/profile/secret/telemetry

- [ ] T14/T20과 필요한 port contract test를 먼저 작성한다.
- [ ] Retry 때 identity가 바뀌거나 telemetry에 locator/secret이 노출되는 red를 관찰한다.
- [ ] Same-process dispatcher/workflow, static exact catalog, rejecting secret resolver와 structured logger 최소 구현을 추가한다.
- [ ] Completion order/thread count가 semantic digest에 들어가지 않음을 확인한다.

### 10.6 Track B gate

- [ ] `AR-5 DONE` evidence와 actual owner package/type/report contract를 확인한다.
- [ ] 계획상 import와 actual type mapping 표를 evidence `handoff.md`에 기록한다.
- [ ] Test-only fake PASS/result를 normal publication integration에서 제거한다.
- [ ] T15~T19를 actual AR-5 type으로 먼저 작성하고 single-pass/lineage/CAS/tamper red를 각각 관찰한다.
- [ ] `SolveApplicationService`의 publication/retrieval 최소 logic만 구현하여 T15~T19를 green으로 만든다.

### 10.7 CLI/worker local composition과 end-to-end

- [ ] T21~T26 Failsafe test를 `RpdptwCli`, `RpdptwWorker` production main보다 먼저 작성한다.
- [ ] First red가 “미검증 결과가 노출됨”, “corrupt ref가 worker까지 도달함”, “cancel partial candidate 남음” 또는 missing composition type compile failure 중 지정 이유인지 확인한다.
- [ ] `WorkerRunApplicationService`, local composition root와 main을 최소 구현한다.
- [ ] CLI는 workspace/solve/idempotency/config/schema/profile identity를 explicit하게 요구하고 legacy 수치를 default로 채우지 않는다.
- [ ] T21~T26 targeted green, module verify, reactor regression을 순서대로 실행한다.

### 10.8 Evidence와 scope close

- [ ] Surefire/Failsafe report에서 지정 test가 실제 target owner module에서 실행됐는지 확인한다.
- [ ] §11의 provider/default/customer/SDK/URI/secret scan을 실행한다.
- [ ] Artifact/state/result digest, CAS/fault/cancellation matrix와 deterministic rerun diff를 evidence bundle에 보존한다.
- [ ] `git diff --check`와 changed-file scope를 확인한다.
- [ ] §14의 DONE AND gate를 평가하고 blocker가 하나라도 남으면 `DONE`으로 표시하지 않는다.

## 11. Exact 명령

### 11.1 Working directory와 preflight

```bash
cd /Users/brown/workspace/ro-next
git status --short --untracked-files=all
git rev-parse HEAD
sha256sum docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/codex/implementation-plan.md
rg --files rpdptw/application adapters/common apps/cli apps/worker
rg -n '<module>|<packaging>|<artifactId>' --glob 'pom.xml' .
mvn -version
```

Hash가 YAML baseline과 다르면 단순히 새 hash를 evidence에 복사하지 말고 관련 design/plan diff와 authority 영향을 먼저 판정한다.

### 11.2 Targeted red/green

API/architecture:

```bash
mvn -pl rpdptw/application -am \
  -Dtest=ProviderNeutralPortArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Application state/use case:

```bash
mvn -pl rpdptw/application -am \
  -Dtest=SubmissionIdempotencyTest,RunStateTransitionTest,CancellationStateMachineTest,PublicationGateApplicationTest,VerifiedResultRetrievalTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Local adapter:

```bash
mvn -pl adapters/common -am \
  -Dtest=ArtifactStoreContractTest,FileSystemArtifactStoreFaultTest,RunStateRepositoryContractTest,FileSystemResultPublisherTest,SameProcessWorkerDispatcherTest,DeterministicArtifactJsonCodecTest,TelemetryRedactionTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Worker Failsafe:

```bash
mvn -pl apps/worker -am \
  -Dit.test=LocalSolveEndToEndIT,LocalCancellationIT,WorkerLocalCompositionIT \
  -DskipITs=false verify
```

CLI Failsafe:

```bash
mvn -pl apps/cli -am \
  -Dit.test=VerifiedResultRetrievalIT,RpdptwCliIT \
  -DskipITs=false verify
```

`-Dsurefire.failIfNoSpecifiedTests=false`는 `-am` 선행 module에 같은 이름의 test가 없을 때만 허용한다. 대상 module의 Surefire XML에 test가 없으면 red/green evidence가 아니다. `*IT`는 Failsafe `verify`에서 실행하고 `-DskipITs=false`를 보존한다.

### 11.3 Module와 reactor regression

```bash
mvn -pl rpdptw/application -am test
mvn -pl adapters/common -am test
mvn -pl rpdptw/application,adapters/common,apps/cli,apps/worker -am verify
mvn verify
```

마지막 reactor 실패가 unrelated/pre-existing이면 AR-6을 자동 green으로 처리하지 않는다. Failure module/goal을 evidence에 분리하고, AR-6 changed graph와 관계가 있으면 고친 뒤 다시 전체 명령을 통과해야 한다.

### 11.4 Architecture/forbidden-value scan

```bash
rg -n 'com\.google|google\.cloud|StorageOptions|ExecutionsClient|gs://|s3://|amazonaws|azure|java\.net\.URI|HttpExchange|WorkflowEvent|FunctionContext' \
  rpdptw/application adapters/common apps/cli apps/worker

rg -n 'parallelRuns|iterationsPerRun|screenMaxSteps|phase2MaxSteps|maxRounds|watchdog' \
  rpdptw/application/src/main adapters/common/src/main apps/cli/src/main apps/worker/src/main

rg -n 'customerId\s*(==|\.equals|switch)|customerName\s*(==|\.equals|switch)|presetName\.contains' \
  rpdptw/application adapters/common apps/cli apps/worker

rg -n 'System\.nanoTime|System\.currentTimeMillis|Instant\.now|UUID\.randomUUID|Math\.random|new Random|parallelStream' \
  rpdptw/application/src/main

rg -n 'System\.getenv|RESULTS_BUCKET|WORKFLOW_NAME|SERVICE_MODE' \
  rpdptw/application adapters/common
```

첫 scan은 모두 0건이어야 한다. 두 번째 scan은 explicit field/name 자체는 있을 수 있으나 production numeric fallback/default가 0건이어야 한다. 세 번째는 0건이다. 네 번째는 injected `Clock`/explicit identity를 우회한 application semantic code가 0건이어야 한다. 다섯 번째는 app composition root 밖의 application/common adapter에서 0건이어야 한다.

### 11.5 Diff, evidence와 link check

```bash
git diff --check
git status --short --untracked-files=all
git diff --name-only
git diff -- rpdptw/application adapters/common apps/cli apps/worker
find target/codex-evidence/AR-6 -maxdepth 3 -type f -print | sort
rg -n '^#{1,4} ' docs/codex/phases/phase-06-local-application-and-logical-ports.md
rg -n 'phase-06-local-application-and-logical-ports\.md' docs/codex/implementation-plan.md
```

## 12. Deliverables와 evidence bundle

### 12.1 Deliverables

| Deliverable | 완료 artifact/evidence | 다음 소비자 |
|---|---|---|
| Application-owned port catalog | Public signature report + provider-neutral architecture test | AR-7 coordinator, AR-8 adapter/cutover |
| Execution identity/state | `ExecutionManifest`, `WorkerAssignment`, `RunState`, transition/CAS tests | AR-7 round/worker state |
| Immutable artifact contract | Artifact ref/schema/digest, filesystem/in-memory contract report | AR-7/AR-8 artifact adapter |
| Idempotent submission | Same-key convergence/conflict trace | AR-8 compatibility/API |
| Cooperative cancellation | Intent/actual termination/discard fault record | AR-7 worker retry, AR-8 external status |
| Both-gate publication | Actual candidate/result PASS refs, publication CAS record | Normal retrieval, AR-8 cutover, AR-9 official seam |
| Verified retrieval | Running/rejected/published matrix + tamper test | CLI/API compatibility |
| Local reference runtime | CLI/worker artifact, composition dependency graph | Development, deterministic rerun, provider parity |
| Structured telemetry | Redaction/correlation report | AR-7 logical orchestration, future provider adapter |

### 12.2 Evidence bundle layout

Implementation은 다음 경로를 만들되 `target/`을 source control에 추가하지 않는다.

```text
target/codex-evidence/AR-6/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── api-skeleton/
│   ├── state-idempotency/
│   ├── artifact-cas/
│   ├── publication-retrieval/
│   └── local-e2e/
├── green/
│   ├── targeted/
│   └── integration/
├── regression/
│   ├── application/
│   ├── adapters-common/
│   ├── apps/
│   ├── architecture/
│   └── reactor/
├── fingerprints/
│   ├── artifact-refs.json
│   ├── execution-manifest.json
│   ├── publication-lineage.json
│   └── deterministic-rerun.diff
├── faults/
│   ├── artifact-corruption.json
│   ├── state-cas.json
│   ├── idempotency.json
│   ├── cancellation.json
│   └── publication-retrieval.json
├── diff/
│   ├── changed-files.txt
│   ├── git-diff-check.txt
│   ├── forbidden-scan.txt
│   └── dependency-graph.txt
└── handoff.md
```

`evidence.json`은 상위 계획 §11의 공통 field 외에 `trackAStatus`, `trackBStatus`, `predecessorEvidence`, `portContractVersion`, `artifactSchemaVersions`, `stateTransitionVersion`, `idempotencyContractVersion`, `publicationContractVersion`, `candidateVerifierReportDigest`, `resultVerifierReportDigest`, `localFilesystemCapabilities`, `testOnlyValues`를 가진다. Timestamp/elapsed/workspace locator는 metadata이며 semantic fingerprint가 아니다.

## 13. Rollback

### 13.1 Source/POM rollback

- AR-6 변경은 다른 phase/user 변경과 섞지 않은 독립 commit 또는 exact patch로 유지한다.
- Rollback은 `git revert <AR-6-commit>` 또는 검토된 AR-6 patch의 역적용을 사용한다. `git reset --hard`, broad `git checkout --`, broad `git restore`, stash drop와 untracked directory 삭제를 사용하지 않는다.
- Root/aggregator POM과 legacy 이동은 AR-0 소유이므로 AR-6 rollback에서 되돌리지 않는다.
- AR-6이 바꾼 네 leaf POM의 dependency/main packaging hunk와 §6의 AR-6 file만 되돌린다.
- 사용자/다른 세션 변경이 같은 file에 겹치면 자동 역적용하지 않고 overlap을 `BLOCKED`로 보고한다.

### 13.2 Artifact/state rollback

- Immutable artifact는 overwrite/delete하지 않는다. 실패 version의 refs를 inactive로 남기고 retention 정책에 따른다.
- Publication pointer rollback은 “현재 pointer/version이 rollback 대상 version과 일치”하는 expected-version CAS로만 이전 verified result ref를 가리킨다.
- 이전 result도 두 verifier `PASS`와 digest 검증이 있어야 한다. Raw legacy candidate를 rollback target으로 사용하지 않는다.
- Run state schema를 바꿨다면 old reader와 new reader의 version compatibility evidence 없이 기존 state file을 in-place rewrite하지 않는다.
- Partial temp file은 정상 artifact/state로 읽지 않고 fault evidence와 함께 격리한다.
- CLI/worker logical version switch는 local workspace별 explicit config로 되돌리고 semantic artifact identity를 path나 runtime version에 맞춰 다시 쓰지 않는다.

## 14. DONE/BLOCKED 판정

### 14.1 `DONE` AND gate

다음을 모두 만족해야 `AR-6 / RM-8-local DONE`이다.

1. `AR-0 DONE`과 `AR-5 DONE`의 evidence ID/digest가 bundle에 있다.
2. §6의 범위 내 production/test/POM이 실제로 존재하고 예상 밖 이동/삭제가 없다.
3. T01~T26 각각에 의도한 red, targeted green과 owner-module Surefire/Failsafe report가 있다.
4. `rpdptw/application`, `adapters/common`, CLI/worker verify와 전체 reactor `mvn verify`가 통과한다.
5. Application port signature에 provider SDK/resource/URI/filesystem/HTTP type이 0건이다.
6. Same-key/same-fingerprint는 단일 receipt/state/dispatch로 수렴하고 different fingerprint는 mutation 없이 conflict다.
7. Artifact read는 digest 검증 전 deserialization 0회이고 write는 create-once/atomic이다.
8. In-memory/filesystem CAS가 stale writer를 거부하고 terminal state/result를 덮어쓰지 않는다.
9. Cancellation fault에서 incomplete candidate와 step side effect가 0이고 actual termination이 보존된다.
10. Actual AR-5 candidate/result verifier가 모두 `PASS`인 result만 publication/retrieval된다.
11. Running/rejected/corrupt/CAS-conflict 상태가 normal payload를 노출하지 않는다.
12. Fixed local envelope 반복에서 semantic artifact/result digest가 같고 workspace/clock/thread는 metadata에만 남는다.
13. Evidence bundle, rollback과 AR-7/AR-8 handoff를 downstream이 실제로 재사용할 수 있다.
14. Blocker, hidden official value, physical provider, fixture 변환, optional variant와 금지 shortcut이 0건이다.

### 14.2 `BLOCKED` 판정

다음 중 하나면 완료를 주장하지 않고 blocker ID, 마지막 안전 state와 재개 조건을 기록한다.

- AR-0 reactor/architecture evidence 또는 AR-5 actual both-gate owner API/evidence가 없음
- 승인된 owner API와 §7 계획상 API의 의미 충돌
- Local filesystem이 required atomic move/lock/CAS 의미를 제공하지 못함
- Artifact canonical bytes/schema를 안정적으로 만들 수 없는데 digest identity를 임의 encoding으로 고정하려 함
- Public HTTP/JSON/storage contract 승인이 필요해야만 local reference를 진행할 수 있는 설계가 됨
- Provider URI/SDK/event/resource type을 port signature에 넣어야 한다고 판단함
- Verifier 전 candidate 또는 one-pass result를 정상 retrieval해야 한다고 판단함
- User/다른 세션 변경과 대상 file이 겹쳐 안전한 분리가 불가능함
- Targeted test가 의도한 red가 아니라 환경/dependency/unrelated failure만 보임

`Q-BENCH-02` 미승인, current decimal Win fixture와 `Q-INFRA-01`/`Q-VAR-01 DEFERRED` 자체는 AR-6 blocker가 아니다. 그것을 구현에 끌어들이려는 시도가 중단 사유다.

## 15. 다음 phase handoff

### 15.1 AR-7에 넘길 것

- `ExecutionManifest`, `WorkerAssignment`, logical worker/attempt identity와 retry-stable field 목록
- `ArtifactStore`, `RunStateRepository`, `WorkerDispatcher`, `WorkflowExecutionPort`, `CancellationPort`, `ResultPublisher`, `TelemetryPort`
- Deterministic fake/in-memory/filesystem adapter와 abstract contract/fault test
- Expected-version CAS, duplicate convergence/different-digest conflict와 cancellation intent/actual termination record
- Actual verified-candidate/result artifact refs와 both-gate publication lineage
- Thread/completion-order/path/clock이 semantic identity에 들어가지 않는 rerun evidence

AR-7은 round/worker completeness와 champion aggregation을 추가하지만 AR-6의 artifact/idempotency/CAS/cancellation/publication 의미를 바꾸지 않는다. `WorkerAssignment` retry에서 attempt 외 seed/warm start/config/requested steps를 바꾸지 않는다.

### 15.2 AR-8에 넘길 것

- Versioned application use case seam과 semantic compatibility가 비교할 status/retrieval variants
- Same-key/different-digest behavior와 local logical version rollback primitive
- Raw legacy result와 target verified result를 구분하는 publication/retrieval gate
- CLI/worker composition dependency graph와 provider-neutral port compatibility suite

AR-8이 legacy public endpoint를 mapping하더라도 AR-6 local artifact encoding을 승인된 public schema라고 재사용해서는 안 된다.

### 15.3 Known risks

- AR-5 actual exported type/package가 현재 계획상 이름과 다를 수 있다. 중복 type이 아니라 import/mapping review로 해소한다.
- Filesystem lock/atomic move semantics는 플랫폼별 차이가 있다. 정상 publication evidence는 실제 대상 local filesystem fault test와 함께 해석한다.
- Same-process dispatcher가 AR-7의 completion-order fault를 충분히 재현하지 못할 수 있다. AR-7 deterministic fake가 permutation을 추가한다.
- Internal JSON canonicalization을 public wire contract로 오해할 위험이 있다. Schema 이름과 문서에 `internal`/version을 유지한다.

## 16. Scope exclusions와 금지 shortcut

### 16.1 이 phase가 구현하지 않는 것

- `Q-BENCH-02` 공식 `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`, watchdog
- Official Win PoC manifest, baseline/challenger와 `data/win_poc_case.json` 변환/정규화
- Multi-round champion/fan-in/completeness state machine의 전체 구현(AR-7)
- Public API compatibility, shadow/cutover와 legacy 제거(AR-8)
- Physical provider/product adapter, GCP/AWS/Azure topology, IaC와 deployment(AR-11/RM-9)
- Optional variant, multi-trip/rotation, route pool/MIP
- Core normalization/travel/profile/search/verifier 의미 재구현
- Customer-specific profile/preset, public HTTP field와 database schema 승인

### 16.2 금지 shortcut

- Legacy `parallelRuns=8`, `iterationsPerRun=5000`, timeout/retry를 official/local hidden default로 복사
- `gs://`, bucket/object key, provider execution ID 또는 filesystem path를 semantic `ArtifactRef` identity로 사용
- Digest를 확인하기 전에 Jackson/object deserialization 수행
- Existing artifact/result/state를 overwrite하거나 last-writer-wins로 CAS를 흉내 냄
- Same idempotency key의 different digest 중 하나를 임의 선택
- Retry 때 새 seed, warm start, requested steps, config 또는 logical worker ID 선택
- Blob/file 존재만으로 `RUNNING`/`SUCCEEDED`를 추정
- Cancellation request 성공을 actual `CANCELLED` termination으로 기록
- Watchdog/resource/platform failure를 cancellation 또는 정상 step 종료로 rename
- Candidate verifier 하나만 통과한 result, fake PASS 또는 raw candidate를 normal publish/retrieve
- Search cache/solver summary를 result verifier evidence로 사용
- Secret, provider locator, raw address/full input을 artifact fingerprint나 telemetry에 기록
- App/core에서 environment, global clock/random, classpath order와 thread first-winner를 semantic default로 사용
- Test red를 보지 않고 production logic 작성
- Mock/demo/source 존재만으로 Track B 또는 AR-6을 `DONE` 처리

## 17. 문서 자체 검증 기준

이 phase 문서 작성 세션은 코드/POM/원본 설계/다른 phase/progress를 변경하지 않는다. 완료 전 다음을 확인한다.

```bash
cd /Users/brown/workspace/ro-next
test -f docs/codex/phases/phase-06-local-application-and-logical-ports.md
rg -n '^## (1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17)\.' \
  docs/codex/phases/phase-06-local-application-and-logical-ports.md
rg -n 'phase-06-local-application-and-logical-ports\.md' docs/codex/implementation-plan.md
sha256sum docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/codex/implementation-plan.md
git diff --no-index --check /dev/null docs/codex/phases/phase-06-local-application-and-logical-ports.md
git status --short --untracked-files=all
```

`git diff --no-index --check`의 exit `1`은 새 파일 diff가 존재한다는 뜻이며 whitespace error 출력이 없어야 한다. 최종 changed scope에서 이 세션이 생성한 경로는 `docs/codex/phases/phase-06-local-application-and-logical-ports.md` 하나뿐이어야 한다.
