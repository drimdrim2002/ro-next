# Phase 02 — 이동 자료 준비와 immutable problem

```yaml
document_status: REVIEWED_WITH_CORRECTIONS
document_workflow_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
phase: "02"
phase_name: prepared-travel-immutable-problem
phase_registry_status_observed: PLANNED
phase_readiness_assessment: BLOCKED_BY_ENTRY_GATES
implementation_status: NOT_STARTED
implementation_completion_claim: NONE
canonical_path: docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md
baseline_date: 2026-07-28
baseline_commit: 3424277c9c74f8151a83be056a07dd4659331beb
scheduler_task_id: TBD
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
prerequisites:
  - Phase 00 ACCEPTED build/module/architecture skeleton
  - Phase 01 ACCEPTED canonical input and normalization artifacts
  - approved Great Circle function and exact version
  - reviewed typed travel-source and generation policy
owners:
  accountable: RPDPTW Domain·Input·Matrix owner
  implementation: Phase 02 core/travel implementer
  consulted:
    - Phase 01 canonical-input owner
    - Phase 03 propagation owner
    - Verification owner
    - Architecture owner
    - Official travel-data provider owner
  acceptance: independent Phase 02 reviewer and total scheduler
required_evidence:
  - E-P02-TRAVEL
  - E-P02-DENSE-ID
  - E-P02-PROBLEM
historical_cross_check_only:
  - docs/2026-07-26-master-design.md
```

이 문서는 **Phase 02 구현을 실행하기 위한 상세 계약**이지 구현 완료 보고가 아니다. 위 `document_status`와 `phase_registry_status_observed` 및 `implementation_status`는 서로 다른 상태다. `REVIEWED_WITH_CORRECTIONS`는 상세 문서 review 결과일 뿐 Phase 02가 `READY`, `IN_PROGRESS` 또는 `ACCEPTED`라는 뜻이 아니다.

이 구현 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 원문 metadata의 `REVIEW`는 provenance로 보존하지만 문서 작성을 중단시키지 않는다. 반대로 Phase 02의 실제 entry/exit evidence와 독립 review도 생략하지 않는다. `OPEN — EXPERIMENT_REQUIRED`, `GATED`, `DEFERRED`와 아직 승인되지 않은 수치/API는 본문에서 명시적으로 보존한다.

## 1. 권위 입력과 해석 기준

### 1.1 직접 대조한 source와 적용 section

| Source | 이 Phase가 소비한 section | 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §1.5, §2.3~§2.4, §3.2~§3.3, §4.1~§4.6, §5~§8, §14.1, §15.3, §16~§17 | 권위·비범위·`C-06`, `C-10~C-14`, `P-03`, travel/immutable problem/verification gate |
| [Final Domain Design](../../2026-07-26-domain-design.md) | §1, §3~§7, §16~§18 | 정확한 입력 단위, travel 준비, immutable model, 오류·acceptance와 drift 확인 |
| [Final Architecture Design](../../2026-07-26-architecture-design.md) | §1~§2, §5.2~§5.6, §6 | Java 25/Maven module·package·artifact·검증 경계 |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | §1~§3, §5~§7, §19~§25, §27 | 15 Phase 배치, Phase 01/02/03 경계, provider SDK 격리, test/evidence |
| [Question register](../../master-design-open-questions.md) | §1~§4, 특히 `Q-MTX-01~03`, `Q-NUM-01~03`, `Q-INFRA-01`, `Q-BENCH-02`, `Q-VAR-01` | exact decision/status/owner/gate |
| [Master Realization Plan](../master-realization-plan.md) | §1~§8, §9~§15, 특히 Phase 01~03 | current inventory, canonical Phase contract, evidence/DoD/traceability |
| [Implementation README](../README.md) | §1~§7 | 사용자 고정 authority, filename, status와 planned-link 규칙 |
| [SUPERSEDED Master](../../2026-07-26-master-design.md) | metadata, §1, §6.1~§6.2, §10 | 누락·퇴행 여부만 historical cross-check |

`docs/codex/*`는 역사 자료이므로 읽기 권위, 문장 복사 원본 또는 수정 대상으로 사용하지 않는다.

### 1.2 충돌과 drift 해소

| Drift | Phase 02 판단 |
|---|---|
| Final Domain/Architecture 일부가 `Q-INFRA-01 DEFERRED`, 질문 상태 `25/1/2`로 남아 있음 | 최신 Canonical Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `26/1/1`을 적용한다. Phase 02 core에는 여전히 provider SDK를 넣지 않는다. |
| `PreparedTravel` 내부 표현과 구체 Java API는 `P-03`으로 미확정 | 본 문서의 type/file/signature는 **proposed internal candidate**다. 의미·검증 기준만 고정하며 public API로 승인하지 않는다. |
| Great Circle 사용은 확정됐으나 Earth model, 함수 ID, library와 version은 미기재 | 값을 만들지 않는다. 승인된 `GreatCirclePolicyId/functionVersion`이 entry evidence로 제출될 때까지 missing-`D` 경로와 Phase acceptance를 막는다. |
| 현재 Win fixture에는 decimal `D/U`가 존재 | 해당 fixture는 non-compliant이며 Phase 02 generic 구현이나 test-only hand oracle을 official travel/benchmark evidence로 승격할 수 없다. |
| Registry는 Phase 02를 `PLANNED`로 기록 | 본 문서가 entry blocker를 상세화해도 authoritative registry를 직접 변경하지 않는다. 총괄 스케줄러만 상태를 전이한다. |

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 01의 immutable `NormalizedInputArtifact`를 받아 다음 두 authority를 한 번의 실패 원자적 preparation으로 만든다.

1. 모든 physical location directed pair와 모든 사용 vehicle의 travel time이 solve 전에 해소된 immutable `PreparedTravel`.
2. Dense ID bijection, request/node/location/vehicle reference, Phase 01 normalized facts와 `PreparedTravel` identity를 결합한 immutable `ProblemInstance`.

Phase 03의 route propagation은 raw `D/U`, coordinate, speed, provider response 또는 “비슷한 arc”를 보지 않고 이 두 artifact만으로 순수 계산을 시작할 수 있어야 한다.

### 2.2 포함 범위

- Phase 01 artifact의 schema/fingerprint/reference 검증
- `RequestId`, `VehicleId`, `SolverNodeId`, `PhysicalLocationId`의 분리와 dense bijection
- provided directed integer `D/U`의 typed 수용과 source provenance
- self arc `0 meter/0 second` normalization
- explicit policy에 따른 missing `D`/`U` 생성
- 모든 `M²` directed distance와 모든 사용 vehicle의 resolved time coverage
- deterministic canonical ordering, checked index/range/arithmetic
- immutable `PreparedTravel` 및 `ProblemInstance` 생성
- Phase 01의 단일 `NormalizedInputArtifact` source와 pure core preparation의 경계
- whole-artifact cache의 허용 범위와 corruption 처리
- semantic fingerprint, artifact digest, provenance와 lineage
- unit/property/contract/fault/corruption/reproducibility/security/observability/structural-performance test

### 2.3 명시적 비범위

- Route sequence의 load/time/window/rest/stop/resource propagation: [Phase 03](phase-03-route-propagation-evaluation-kernel.md)
- Hard constraint, metric, score, objective, comparator 또는 customer profile binding
- Insertion, initial portfolio, ALNS, cache/incremental route evaluation
- Candidate/result verifier 구현. 다만 두 verifier가 소비할 authority contract와 corruption fixture 요구는 정의한다.
- Application port/adapter, network/provider SDK client, credential, retry, rate limit, cloud distribution과 production provider 채택
- Dynamic traffic, time-dependent matrix, geocoding, address 정제와 live refresh
- Reverse arc 복사, 대칭 평균, 임의 provider fallback 또는 search 중 lazy generation
- Official Win benchmark, `Q-BENCH-02` 수치와 production cutover
- Multi-trip/rotation, optional variants와 `C-17` route pool/MIP
- Public wire schema와 public Java API 확정

## 3. Phase 결정과 불변조건

### 3.1 이 문서가 고정하는 의미

1. Travel key는 solver node가 아니라 physical location identity다.
2. `A→B`와 `B→A`는 독립된 directed cell이다.
3. Provided `D`는 directed integer meter, provided `U`는 vehicle-independent directed integer second다.
4. Decimal `D/U`를 rounding/절삭해 수용하지 않는다.
5. `C`는 non-authoritative이며 feasibility, generation, score와 fingerprint의 semantic value로 사용하지 않는다. Raw provenance에 보존할 수는 있다.
6. 모든 self cell은 raw value와 무관하게 `0m/0s`다. 원래 값이 달랐다면 override 사실을 provenance에 남긴다.
7. Missing `D`만 approved Great Circle 함수로 만들고 fractional meter에 `HALF_UP`을 한 번 적용한다.
8. Missing `U`만 vehicle별 `CEILING(D_meter × 3.6 ÷ speed_km_h)`로 만든다.
9. Speed가 **missing**일 때만 정확히 `45 km/h`를 사용한다. Present-invalid speed는 validation error다.
10. Provided common `U`와 vehicle-resolved generated `U`는 source와 표현에서 구분한다.
11. Solver, Phase 03, Phase 05와 Phase 07 verifier는 동일 prepared fingerprint를 소비한다.

### 3.2 불변조건

| ID | 불변조건 |
|---|---|
| `INV-P02-01` | Physical location 수가 `M`이면 directed distance cell은 정확히 `M²`개다. |
| `INV-P02-02` | 사용 vehicle 수가 `V`이면 모든 `(vehicle, from, to)`가 하나의 resolved time을 갖는다. |
| `INV-P02-03` | 모든 dense ID는 `0..N-1`의 hole 없는 범위이고 external↔dense round-trip이 bijection이다. |
| `INV-P02-04` | Pickup, delivery와 terminal node가 같은 location을 가리켜도 `SolverNodeId` identity를 합치지 않는다. |
| `INV-P02-05` | 모든 request는 정확히 한 pickup node와 한 delivery node를 참조하고 dangling/cross-kind reference가 없다. |
| `INV-P02-06` | Construction input, returned view와 artifact 사이에 mutable array/list/map alias가 없다. |
| `INV-P02-07` | Invalid/incomplete Phase 01 source handoff, duplicate-conflict, unknown reference, overflow 또는 corruption이면 stable artifact를 한 개도 발행하지 않는다. |
| `INV-P02-08` | Canonical input collection 순서가 달라도 canonical meaning이 같으면 canonical bytes와 fingerprints가 같다. |
| `INV-P02-09` | Source kind/policy/version 또는 resolved value가 다르면 relevant fingerprint가 달라진다. |
| `INV-P02-10` | Runtime lookup은 total이다. Missing, reverse, symmetric, provider 또는 coordinate fallback branch가 없다. |
| `INV-P02-11` | Artifact cache는 complete artifact의 성능 최적화일 뿐 authority가 아니며 cache hit/miss가 bytes/fingerprint를 바꾸지 않는다. |
| `INV-P02-12` | `ProblemInstance`는 `PreparedTravelFingerprint`를 값으로 bind하고 mismatched object/reference를 거부한다. |

## 4. Entry gate와 사전 evidence 확인

### 4.1 필수 entry gate

| Gate | 필요한 evidence | 현재 확인 | 판정 |
|---|---|---|---|
| Phase 00 accepted | `E-P00-BUILD`, `E-P00-ARCH`, accepted review | 현재 checkout은 root 단일 POM이며 accepted evidence 없음 | `BLOCKED` |
| Phase 01 accepted | `E-P01-NUMERIC`, `E-P01-TIME`, `E-P01-COMPAT`, `E-P01-ERROR`, accepted review | [Phase 01 상세](phase-01-canonical-input-normalization.md)은 actual document지만 registry `PLANNED`/implementation `NOT_STARTED`이고 artifact/evidence 없음 | `BLOCKED` |
| Great Circle approval | 함수 ID/version, Earth model/constant, coordinate validation, deterministic precision, reference vectors, approval record | Authority 문서는 “approved function/version”만 요구하고 구체값은 제공하지 않음 | `BLOCKED` |
| Typed source policy | allowed source kinds, provided/generated priority, declared sparse-input meaning, source identity와 no-fallback rule review | 본 문서가 proposed contract를 제공하지만 승인 record 없음 | `BLOCKED` |
| Integer travel evidence scope | Generic Phase 02는 test-only integer hand oracle로 검증 가능; official fixture/snapshot은 별도 authority 필요 | repository에는 승인된 integer fixture가 없고 current Win fixture는 decimal | Generic entry blocker 아님; official/benchmark 사용만 `BLOCKED` |
| Owner/task | implementation/reviewer와 scheduler task identity | Scheduler task ID `TBD`; registry `PLANNED` | 실행 전 확인 필요 |

Phase 02 구현자는 `BLOCKED`로 판정된 entry gate가 충족되기 전 production code를 시작하지 않는다. Test plan과 pure core source contract를 review하는 것은 가능하지만 fake/test value를 production default나 승인된 source로 승격하지 않는다. Approved integer fixture나 official snapshot의 부재만으로 generic Phase 02를 막지 않으며, test-only oracle을 official/benchmark evidence로 승격하는 것만 금지한다.

### 4.2 Entry artifact acceptance checklist

Phase 01 handoff는 [실제 Phase 01 상세](phase-01-canonical-input-normalization.md)의 계약과 일치하는 최소 다음 내용을 가져야 한다.

```text
NormalizedInputArtifact
  rawInputDigest
  schemaIdentity
  adapterIdentity
  normalized plan/depot/requests/vehicles/locations
  normalized sparse provided travel declarations
  normalized numeric/time/service/compatibility/trip policies
  alias/coercion/unknown-field provenance
  semanticFingerprint
  envelopeFingerprint
```

다음은 entry rejection이다.

- Phase 01 fingerprint가 bytes/declared identity와 맞지 않음
- Mutable collection/view가 전달됨
- External ID duplicate 또는 unresolved reference
- Decimal/negative `D/U`, non-finite coordinate, present-non-positive speed
- `null`, empty string과 missing을 schema 근거 없이 같은 의미로 처리
- Raw provider DTO/SDK class가 canonical artifact에 남음
- `latest`, file modification time 또는 iteration order가 semantic input에 포함됨

## 5. 2026-07-28 current inventory와 변경 범위

### 5.1 실제 checkout inventory

Read-only 확인 기준은 commit `3424277c9c74f8151a83be056a07dd4659331beb`이다.

| 항목 | 확인된 사실 | Phase 02 해석 |
|---|---|---|
| Toolchain | Corretto/OpenJDK `25.0.3`, Maven `3.9.14`, `.sdkmanrc`와 root Enforcer가 Java `[25,26)`, Maven `3.9.14+` 요구 | Target Java 25와 일치하나 Phase 02 build evidence는 아님 |
| Maven | `com.ronext:ro-next:0.1.0-SNAPSHOT` root 단일 project | Proposed `rpdptw-core`, fixtures, architecture-rules module은 아직 없음 |
| Main Java | `com.ronext.optimizer` 아래 6개 파일 | Canonical input, travel, immutable problem 구현 없음 |
| Solver placeholder | `AlnsBatchEngine`이 seed/iterations로 synthetic `objective` map 생성 | RPDPTW/travel/feasibility evidence가 아님 |
| Test | `AlnsBatchEngineTest` 1개 | Phase 02 test/evidence 없음 |
| Dependencies | GCP Workflow/Storage, Jackson, JUnit가 root에 직접 존재 | Core/provider 격리 미구현 |
| Data | `ro_input_json_spec.pdf`, `win_poc_case.json` | 전자는 legacy 참고, 후자는 decimal `D/U`로 official 사용 불가 |
| Phase docs | Phase 00/01/02/03 상세는 실제 존재하지만 모두 implementation `NOT_STARTED`이고 accepted review/evidence가 없음 | Link target 존재와 accepted handoff를 구분해야 함 |

현재 존재하는 코드나 ignored `target/` artifact를 `PreparedTravel`, `ProblemInstance` 또는 accepted evidence로 이름 바꾸지 않는다.

### 5.2 Proposed 변경 module/package/file tree

아래는 Phase 00에서 target reactor 이름이 승인된다는 전제의 **proposed internal change set**이다. Phase 00 ADR에서 이름이 바뀌면 의미와 test owner를 유지한 채 경로만 갱신한다.

```text
rpdptw/core/
├── src/main/java/com/ronext/rpdptw/domain/
│   ├── RequestId.java
│   ├── VehicleId.java
│   ├── SolverNodeId.java
│   ├── PhysicalLocationId.java
│   ├── DenseIdBijection.java
│   ├── ProblemInstance.java
│   └── ProblemFingerprint.java
├── src/main/java/com/ronext/rpdptw/travel/
│   ├── PreparedTravel.java
│   ├── PreparedTravelFingerprint.java
│   ├── TravelPreparer.java
│   ├── TravelPreparationInput.java
│   ├── TravelGenerationPolicy.java
│   ├── GreatCircleDistanceFunction.java
│   ├── TravelSourceKind.java
│   ├── TravelCellProvenance.java
│   └── TravelPreparationFailure.java
├── src/test/java/com/ronext/rpdptw/domain/
│   ├── DenseIdBijectionTest.java
│   ├── DenseIdBijectionPropertyTest.java
│   ├── ProblemInstanceTest.java
│   └── ProblemInstanceCorruptionTest.java
└── src/test/java/com/ronext/rpdptw/travel/
    ├── TravelSourceHandoffTest.java
    ├── TravelPreparerTest.java
    ├── TravelPreparerPropertyTest.java
    ├── PreparedTravelImmutabilityTest.java
    ├── PreparedTravelCorruptionTest.java
    ├── TravelFingerprintReproducibilityTest.java
    ├── TravelPreparationFaultInjectionTest.java
    ├── TravelProvenanceSecurityTest.java
    ├── TravelPreparationReportTest.java
    └── TravelPreparationComplexityTest.java

build/test-fixtures/
└── src/testFixtures/java/com/ronext/rpdptw/testing/
    ├── CanonicalInputFixtureBuilder.java
    ├── IntegerTravelFixtureBuilder.java
    ├── TravelHandOracle.java
    ├── MutatingTravelSourceFake.java
    └── CorruptedPreparedTravelBuilder.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase02ArchitectureTest.java
```

Phase 02가 직접 수정할 production owner는 `rpdptw-core`의 domain/travel뿐이다. Phase 01의 `NormalizedInputArtifact` 안에 sealed된 sparse travel declaration만 소비하며 detached source batch를 추가 입력으로 받지 않는다. Application port와 provider adapter는 Phase 08 이후 scope owner가 별도로 다루고 Phase 02는 provider module/SDK를 미리 만들지 않는다.

## 6. I/O artifact, contract, identity와 lifecycle

### 6.1 입력/출력 계약

| 경계 | 입력 | 출력 | 실패 |
|---|---|---|---|
| Phase 01 → Phase 02 | Immutable `NormalizedInputArtifact` 안의 sparse typed travel와 policy provenance | `TravelPreparationInput` | Fingerprint/reference/source rejection |
| Travel preparation | Locations, vehicles, provided cells, approved generation policy | Complete immutable `PreparedTravel` | No stable partial artifact |
| Problem freeze | Normalized facts, dense mappings, exact travel fingerprint/reference | Immutable `ProblemInstance` | No partially frozen problem |
| Phase 02 → Phase 03 | `ProblemInstance`, `PreparedTravel` and equality proof | Read-only lookup and IDs | Fingerprint mismatch blocks propagation |

### 6.2 Source class를 혼동하지 않는 규칙

| Source class | 의미 | 사용 가능 범위 | 절대 금지 |
|---|---|---|---|
| `TEST_ONLY_HAND_ORACLE` | Test method가 명시한 작은 정수와 독립 손계산 | Unit/property/fault test | Official, production, benchmark 또는 approved fixture라는 표현 |
| `APPROVED_INTEGER_CONTRACT_FIXTURE` | Input·Matrix owner가 schema/version/digest/reference result를 승인한 정수 fixture | Contract/integration regression | Provider의 최신 official data라고 표현 |
| `OFFICIAL_TRAVEL_SNAPSHOT` | 승인된 provider/source policy로 획득해 snapshot identity와 content digest가 고정된 travel data | 승인 범위의 solve/integration | Test fixture와 자동 상호대체, provider unavailable 시 fixture fallback |
| `PROVIDED_CANONICAL_INPUT` | 승인된 input adapter가 canonical sparse `D/U`로 전달한 authoritative 값 | 해당 submission | Decimal coercion, `C` 사용 |
| `GENERATED_GREAT_CIRCLE` | Explicit missing `D`를 승인 함수로 생성 | Policy가 허용한 missing cell | Provider 실패/partial response를 “missing”으로 재해석 |
| `GENERATED_VEHICLE_TIME` | Explicit missing `U`를 resolved `D`와 speed로 생성 | 해당 vehicle/cell | Provided common `U` 덮어쓰기 |
| `SELF_NORMALIZED` | Diagonal을 강제 `0/0` 처리 | 모든 self cell | Raw diagonal을 authoritative로 유지 |

Repository에 현재 `APPROVED_INTEGER_CONTRACT_FIXTURE`나 Phase 02용 `OFFICIAL_TRAVEL_SNAPSHOT`은 없다. 구현자가 임의 숫자를 넣고 위 source class를 붙여서는 안 된다.

### 6.3 Proposed identity model

```text
TravelPreparationInputIdentity
  canonicalInputFingerprint
  normalizedFactsFingerprint
  locationMappingFingerprint
  vehicleMappingFingerprint
  travelSourcePolicyId/version
  GreatCirclePolicyId/version
  unit/rounding/diagonal/coverage policy IDs

PreparedTravelFingerprint
  canonical dense location/vehicle mapping
  every resolved distance/time value
  source kind for every value
  source-policy and generation-policy identities
  unit/rounding/coverage/diagonal identities

PreparedTravelArtifactDigest
  canonical schema version
  PreparedTravelFingerprint
  canonical artifact bytes

ProblemFingerprint
  Phase 01 normalized problem fingerprint
  all dense bijections and typed references
  PreparedTravelFingerprint
  problem schema/policy versions
```

Raw provider locator, request completion order, retrieval timestamp, retry/attempt ID, credential와 log correlation은 provenance metadata일 수 있지만 semantic result를 바꾸지 않는 한 `PreparedTravelFingerprint`에서 제외한다. 반대로 source kind, source policy/version과 official snapshot digest는 같은 값이라도 authority가 다르면 fingerprint가 달라지도록 포함한다.

Fingerprint encoding은 다음을 만족해야 한다.

- Canonical versioned encoding과 length-prefix를 사용해 delimiter collision을 막는다.
- External map/set iteration order를 사용하지 않는다.
- Entity는 approved stable dense mapping 순서, cell은 `(fromDenseId, toDenseId)`, vehicle-resolved time은 `(vehicleDenseId, fromDenseId, toDenseId)` 순서다.
- Hash algorithm 자체도 `FingerprintAlgorithmId/version`으로 선언한다. `SHA-256`은 **proposed candidate**이며 ADR 승인 전 public contract가 아니다.
- Same identity/different canonical bytes는 integrity failure다.

### 6.4 Lifecycle과 state transition

```text
Phase01AcceptedArtifact
  → INPUT_VALIDATED
  → SOURCE_BATCH_VALIDATED
  → DENSE_IDS_VALIDATED
  → INTERNAL_TRAVEL_DRAFT          # method-local mutable, 외부 비노출
  → COMPLETE_TRAVEL_CANDIDATE
  → CANONICALIZED_AND_FINGERPRINTED
  → PREPARED_TRAVEL
  → PROBLEM_DRAFT                  # method-local mutable, 외부 비노출
  → REFERENCES_AND_RANGES_VALIDATED
  → PROBLEM_INSTANCE
  → HANDOFF_READY

any validation/fault/cancel
  → REJECTED(error, safe evidence)
  → draft discarded
```

`INTERNAL_TRAVEL_DRAFT`, `COMPLETE_TRAVEL_CANDIDATE`, `PROBLEM_DRAFT`는 artifact/status/API가 아니다. 외부 observer, solver, cache와 handoff가 볼 수 없다.

### 6.5 Immutability와 alias 방어

Java `record`라는 이유만으로 immutable이라고 간주하지 않는다.

- Constructor에서 array/list/map/bitset을 defensive copy한다.
- `List.copyOf`만으로 element가 mutable한 경우 element도 immutable value로 변환한다.
- Array accessor, mutable iterator, raw `ByteBuffer`, mutable bitset과 backing map을 노출하지 않는다.
- Lookup은 primitive value 또는 immutable record를 반환한다.
- `ProblemInstance`는 mutable `PreparedTravel` object reference가 아니라 immutable instance와 exact fingerprint equality를 확인한다.
- Serialization round-trip 뒤에도 mutation surface가 없어야 한다.
- Test fixture builder의 mutable input을 artifact 생성 후 변경해도 artifact bytes/fingerprint가 바뀌지 않아야 한다.

## 7. Source handoff, absence, error와 cache semantics

### 7.1 Phase 01 source boundary

Pure core preparer는 network, file, object storage, SDK와 clock을 호출하지 않는다.

```text
external/provider representation
  → Phase 01 versioned adapter + normalization
  → exact schema/unit/reference/source validation
  → immutable NormalizedInputArtifact
  → pure TravelPreparer
  → PreparedTravel
```

Phase 02에 도달한 source authority는 `NormalizedInputArtifact` 하나다.

- Detached `TravelSourceBatch`, provider response, SDK DTO, provider URI, credential와 mutable response를 추가 입력으로 받지 않는다.
- Phase 01 artifact가 선언한 sparse cell과 source identity를 exact하게 검증한다.
- Sparse canonical input의 의도적 `Absent`와 upstream acquisition의 unavailable/partial/corrupt를 혼동하지 않는다.
- Upstream unavailable/partial/corrupt를 Phase 02에서 Great Circle, reverse, cache, test fixture 또는 다른 provider fallback으로 성공 변환하지 않는다.
- Provider 선택·재시도·port/adapter 구현은 Phase 08 이후 owner의 별도 logical operation이며 Phase 02 scope가 아니다.

### 7.2 Absence semantics

```text
Absent
  = approved schema/policy가 해당 D 또는 U를 제공하지 않았다고 명시

Present(value)
  = exact integer token과 source identity가 존재

Invalid
  = null/empty/decimal/negative/overflow/unknown reference/conflict
```

- `Absent`만 generation 대상으로 삼는다.
- `Invalid`를 `Absent`로 낮추지 않는다.
- `D`와 `U`의 absence는 cell마다 독립이다.
- Provided `D` + missing `U`는 그 `D`로 vehicle time을 생성한다.
- Missing `D` + provided common `U`는 `D`만 생성하고 `U`는 그대로 쓴다.
- 둘 다 missing이면 먼저 `D`, 다음 vehicle별 `U`를 생성한다.
- Non-self zero는 co-located meaning일 수 있으므로 상위 계약에 없는 “반드시 양수” 규칙을 만들지 않는다. 음수와 overflow는 거부한다.

### 7.3 Unit와 rounding

| Value | Unit/precision | Rule |
|---|---|---|
| Provided `D` | exact integer meter | 그대로 사용 |
| Provided `U` | exact integer second | 모든 vehicle에 common authority |
| Generated `D` | approved Great Circle output in meter | 최종 meter boundary에서 정확히 한 번 `HALF_UP` |
| Vehicle speed | exact positive decimal km/h 또는 missing | missing만 exact `45 km/h` |
| Generated `U` | integer second | exact decimal/rational 계산 뒤 한 번 `CEILING` |

Generated time의 의미식:

```text
ceil(D_meter × 3.6 / speed_km_h)
```

Binary floating-point literal로 중간 반올림하지 않는다. Candidate 구현은 exact `BigDecimal("3.6")` 또는 동등한 checked rational을 사용하고 결과를 `longValueExact`와 checked range로 바꾼다. 구체 speed scale은 Phase 01 contract를 소비하며 Phase 02가 임의 자릿수를 자르지 않는다.

Great Circle 구현은 다음 approval 전 선택하지 않는다.

- Function/policy ID와 version
- Earth model/radius 또는 ellipsoid
- Latitude/longitude 허용 범위와 coordinate order
- Intermediate precision/determinism
- Anti-meridian/pole/same-coordinate behavior
- `HALF_UP` 전 reference decimal meter vectors

### 7.4 Typed error candidate

```java
sealed interface TravelPreparationFailure
        permits SourceFailure, ValueFailure, CoverageFailure,
                IdentityFailure, ArithmeticFailure, IntegrityFailure {}
```

| Code | 의미 | Retry/fallback |
|---|---|---|
| `TRAVEL_SOURCE_UNAPPROVED` | Source/policy/version이 allowlist에 없음 | Corrected/approved new input만 |
| `TRAVEL_SOURCE_HANDOFF_INVALID` | Phase 01 artifact의 source identity/declaration이 불완전하거나 서로 맞지 않음 | Corrected Phase 01 artifact만 |
| `TRAVEL_DUPLICATE_CONFLICT` | Same key에 다른 value/source | 실패, 자동 winner 금지 |
| `TRAVEL_VALUE_NOT_INTEGER` | Provided `D/U` decimal/string coercion 필요 | 실패 |
| `TRAVEL_VALUE_NEGATIVE` | Negative distance/time | 실패 |
| `TRAVEL_REFERENCE_UNKNOWN` | Unknown location/vehicle | 실패 |
| `TRAVEL_COORDINATE_REQUIRED` | Missing `D`인데 좌표 없음 | 실패 |
| `TRAVEL_SPEED_PRESENT_INVALID` | Present speed가 non-positive/invalid | 실패; 45로 대체 금지 |
| `TRAVEL_ARITHMETIC_OVERFLOW` | Index, formula, size 또는 canonical encoding overflow | 실패 |
| `TRAVEL_COVERAGE_INCOMPLETE` | `M²`/vehicle time totality 실패 | 실패 |
| `DENSE_ID_BIJECTION_INVALID` | Duplicate/hole/round-trip mismatch | 실패 |
| `PROBLEM_REFERENCE_INVALID` | Pair/node/location/vehicle reference 불일치 | 실패 |
| `TRAVEL_FINGERPRINT_MISMATCH` | Object/ref/problem fingerprint 불일치 | Integrity incident |
| `TRAVEL_ARTIFACT_CORRUPT` | Bytes/digest/source coverage 손상 | 재사용 금지 |
| `TRAVEL_NONDETERMINISTIC_OUTPUT` | Same authority input의 canonical bytes 불일치 | Defect, publication 금지 |

Error evidence는 raw address, coordinate 전체, credential, provider response body와 full input을 포함하지 않는다. Safe dense/external identity도 tenant classification과 redaction policy를 따른다.

### 7.5 Cache semantics

Phase 02의 기본 correctness path는 cache 없이도 완전해야 한다.

허용 가능한 cache는 **complete `PreparedTravel` artifact 단위**의 content-addressed reuse뿐이다.

```text
PreparationInputFingerprint
  → exact cache lookup
  → bytes read
  → schema + artifact digest + PreparedTravelFingerprint verify
  → exact mapping/policy/source identity verify
  → hit

miss
  → same authoritative input으로 pure preparation
  → canonical bytes/digest verify
  → put-if-absent
```

규칙:

- `latest`, TTL winner, external ID 일부, unordered map hash 또는 provider locator만으로 key를 만들지 않는다.
- Partial arc cache를 authority로 쓰지 않는다.
- Cache hit/miss는 canonical bytes와 fingerprint를 바꾸지 않는다.
- Same key/same digest는 수렴하고 same key/different digest는 integrity failure다.
- Corrupt hit를 조용히 evict하고 다른 provider/임의 generation으로 성공 처리하지 않는다. 먼저 typed integrity failure/evidence를 남긴다.
- Per-invocation memoization은 외부 비노출이고 call count만 줄일 수 있으며 output/provenance/source 선택을 바꿀 수 없다.
- Search, Phase 03과 verifier에는 preparation cache port가 없다. 완성된 artifact만 전달한다.

### 7.6 Observability와 safe report

Pure core는 telemetry backend를 호출하지 않고 성공/실패 결과에 immutable `TravelPreparationReport`/`SafeFailureEvidence`를 반환한다. Application은 이후 이 값을 provider-neutral telemetry로 전달할 수 있지만 report가 artifact 의미나 fingerprint를 바꾸지는 않는다.

최소 safe aggregate:

- canonical input, prepared travel과 problem fingerprint/reference
- Great Circle/source/generation policy ID/version
- physical location 수 `M`, used vehicle 수 `V`
- expected/actual distance 및 vehicle-time coverage count
- provided/generated/self-normalized value count와 missing-speed-default count
- success 또는 typed failure code, integrity/corruption category

금지:

- Raw address, full coordinate, credential, provider locator/response body와 full input
- 모든 arc/value를 그대로 펼친 high-cardinality event
- Clock duration, thread, attempt와 log correlation을 semantic fingerprint에 포함
- Report count와 artifact coverage가 다른데 success로 표시

`TravelPreparationReportTest`는 source count 합계, coverage count와 artifact를 독립 대조하고, `TravelProvenanceSecurityTest`는 secret/PII marker와 high-cardinality raw data가 report/evidence에 없음을 검증한다.

## 8. Proposed Java type와 method signature

아래 API는 public/wire 확정안이 아니다. 이름보다 불변조건, direction과 failure가 우선이다.

```java
record DistanceMeters(long value) {}
record TravelTimeSeconds(long value) {}
record VehicleSpeedKilometersPerHour(BigDecimal exactValue) {}

record DenseId(int value) {}

final class DenseIdBijection<E> {
    int size();
    DenseId denseIdOf(E externalId);
    E externalIdOf(DenseId denseId);
}

enum TravelSourceKind {
    PROVIDED_CANONICAL_INPUT,
    OFFICIAL_TRAVEL_SNAPSHOT,
    GENERATED_GREAT_CIRCLE,
    GENERATED_VEHICLE_TIME,
    SELF_NORMALIZED
}

record TravelCellProvenance(
    TravelSourceKind sourceKind,
    SourceAuthorityId sourceAuthorityId,
    SourcePolicyVersion sourcePolicyVersion,
    Optional<SnapshotDigest> snapshotDigest,
    boolean missingSpeedDefaultUsed
) {}

interface GreatCircleDistanceFunction {
    GreatCircleFunctionId id();
    GreatCircleFunctionVersion version();
    BigDecimal unroundedMeters(Coordinate from, Coordinate to);
}

record TravelPreparationInput(
    NormalizedInputArtifact normalizedInput,
    TravelGenerationPolicy generationPolicy,
    GreatCircleDistanceFunction approvedGreatCircle
) {}

sealed interface TravelPreparationResult
        permits TravelPreparationSucceeded, TravelPreparationRejected {}

record TravelPreparationSucceeded(
    PreparedTravel preparedTravel,
    TravelPreparationReport report
) implements TravelPreparationResult {}

record TravelPreparationRejected(
    TravelPreparationFailure failure,
    SafeFailureEvidence evidence
) implements TravelPreparationResult {}

interface TravelPreparer {
    TravelPreparationResult prepare(TravelPreparationInput input);
}

interface PreparedTravel {
    int physicalLocationCount();
    int vehicleCount();
    DistanceMeters distance(PhysicalLocationId from, PhysicalLocationId to);
    TravelTimeSeconds travelTime(
        VehicleId vehicle,
        PhysicalLocationId from,
        PhysicalLocationId to
    );
    TravelCellProvenance distanceProvenance(
        PhysicalLocationId from,
        PhysicalLocationId to
    );
    TravelCellProvenance timeProvenance(
        VehicleId vehicle,
        PhysicalLocationId from,
        PhysicalLocationId to
    );
    PreparedTravelFingerprint fingerprint();
}

sealed interface ProblemFreezeResult
        permits ProblemFreezeSucceeded, ProblemFreezeRejected {}

interface ProblemInstanceFactory {
    ProblemFreezeResult freeze(
        NormalizedInputArtifact normalizedInput,
        PreparedTravel preparedTravel
    );
}
```

`PreparedTravel`이 common `U`와 vehicle-resolved `U`를 내부에서 어떤 array/table로 압축할지는 `P-03 OPEN`이다. 어떤 표현도 lookup totality, source 구분, no-alias와 fingerprint equality를 약화할 수 없다.

`APPROVED_INTEGER_CONTRACT_FIXTURE`와 `TEST_ONLY_HAND_ORACLE`은 evidence/test envelope의 source class이지 production enum이나 core branch가 아니다. Test-scope builder는 canonical provided cells와 distinct `SourceAuthorityId`를 만든다. 별도 승인된 official data가 있다면 Phase 01 artifact가 `OFFICIAL_TRAVEL_SNAPSHOT` identity와 snapshot digest를 보존해 넘긴다. 따라서 fixture와 official data의 identity/fingerprint는 구분되지만 production source에는 test-only class가 새지 않는다.

### 8.1 Dependency direction

```text
domain value/ID
  ← travel source value + policy
  ← pure TravelPreparer
  ← ProblemInstanceFactory

Phase 03 propagation
  → domain + PreparedTravel read-only API

Phase 07 verification
  → domain + PreparedTravel read-only API
  -X→ solver/search/cache/provider adapter
```

`rpdptw-core`는 application, adapter, provider SDK, Jackson/cloud annotation과 storage locator를 compile-depend하지 않는다.

### 8.2 Preparation pseudo-code

```text
prepare(input):
  verify Phase01 identities, source policy and approved function identity
  create/validate stable dense bijections
  read only the normalized artifact's sealed sparse travel declarations
  canonicalize provided cells by directed dense key
  reject unknown key, invalid value, duplicate conflict or invalid source handoff

  checked allocate method-local draft for M² distances
  checked allocate/represent vehicle-resolved time coverage

  for each directed (from, to) in stable dense order:
    if from == to:
      distance = 0, time = 0, source = SELF_NORMALIZED
      record any raw override provenance
      continue

    distance =
      if provided D is PresentInteger:
        exact provided D
      else:
        require coordinates and approved Great Circle policy
        HALF_UP(approvedFunction.unroundedMeters(from, to))

    for each used vehicle in stable dense order:
      time =
        if provided common U is PresentInteger:
          exact provided U
        else:
          speed = PresentValid speed OR exact missing default 45
          CEILING(distance × 3.6 / speed)
      checked store value and provenance

  independently assert exact M² and V×M² lookup coverage
  canonical encode values + mapping + source/policy identities
  compute/verify PreparedTravelFingerprint
  defensive-copy freeze PreparedTravel

  validate all problem pair/node/location/vehicle references and ranges
  bind exact PreparedTravelFingerprint
  canonical encode/fingerprint
  defensive-copy freeze ProblemInstance
  return success

on any failure:
  discard all drafts
  return one typed rejection
```

Phase 01 canonical collection의 원래 순서, external collection 순서와 hash table iteration은 위 loop 순서에 영향을 주지 않는다.

## 9. Ordered work packages

모든 work package는 red → green → refactor 순서로 진행한다. 현재 target module과 test가 없으므로 아래 test command는 **future-red expected**다. 현재 root placeholder test의 통과를 green으로 계산하지 않는다.

### WP-02-0 — Entry baseline과 contract freeze

**사전조건**

- 총괄 스케줄러가 task/owner를 배정
- Phase 00/01 accepted evidence 또는 명시적 blocker review
- Great Circle/source policy approval owner 지정

**수정 대상**

- 이 Phase 상세에 대응하는 future ADR/test trace
- Production Java 수정 없음

**구체 작업**

1. Source commit, exact cited section/version/status와 Phase 01 artifact fingerprints를 evidence manifest에 기록한다. Whole-file reciprocal document hash는 만들지 않는다.
2. Great Circle approval record와 integer fixture/official snapshot authority를 확인한다.
3. `P-03` internal API, fingerprint algorithm/encoding과 source DTO를 review한다.
4. Entry 미충족이면 implementation branch를 시작하지 않고 blocker handoff를 제출한다.

**검증**

```bash
test -s docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md
git diff --check -- docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md
```

**Expected**

- 문서 non-empty/whitespace clean
- 승인되지 않은 함수/수치/provider/fixture를 발견하면 `BLOCKED`, silent default 0건

**Failure·rollback·handoff**

- Failure 시 코드 변경 없이 마지막 safe point인 Phase 01 accepted artifact로 돌아간다.
- Owner/approval/evidence가 준비되면 같은 source commit과 cited section drift를 재확인하고 WP-02-1부터 재시작한다.

### WP-02-1 — Identity, value와 typed failure

**사전조건**

- WP-02-0 통과
- Phase 00 target module 경로 accepted

**수정 대상**

- `rpdptw/core/.../domain/*Id`, `DenseIdBijection`, travel value/source/failure type
- `DenseIdBijection*Test`, foundational travel value tests

**구체 작업**

1. Request/vehicle/node/location identity를 별도 final value로 만든다.
2. External↔dense bijection과 canonical dense ordering을 구현한다.
3. Meter/second/speed/source/provenance를 typed value로 만들고 negative/overflow를 거부한다.
4. Error sealed hierarchy와 safe evidence를 만든다.
5. Constructor/accessor alias test를 먼저 red로 만든다.

**검증 command/test**

```bash
mvn -pl rpdptw/core -am \
  -Dtest=DenseIdBijectionTest,DenseIdBijectionPropertyTest,PreparedTravelImmutabilityTest \
  test
```

**Expected**

- Duplicate/hole/out-of-range/alias test 포함 전부 pass, failed/skipped 0
- Same canonical identities의 shuffled input이 같은 mapping/fingerprint

**Failure·rollback·handoff**

- Bijection/alias 실패면 artifact type을 downstream에 공개하지 않는다.
- Mutable draft와 generated test output을 폐기하고 WP-02-1 pre-state로 돌아간다.

### WP-02-2 — Source policy와 Phase 01 handoff contract

**사전조건**

- Approved source kinds/priority/declared sparse-input policy
- Phase 01 `NormalizedInputArtifact` handoff schema/identity acceptance

**수정 대상**

- Core source policy/provenance와 `NormalizedInputArtifact` consumer validation
- `TravelSourceHandoffTest`
- Application port와 provider-specific production adapter는 **현재 scope 아님**

**구체 작업**

1. `Absent`, `PresentInteger`, `Invalid`를 구분한다.
2. Phase 01 artifact 안의 declared sparse key set, duplicate/conflict, unknown reference와 canonical order를 검증한다.
3. Official snapshot, approved integer fixture, test-only oracle를 type/identity로 분리한다.
4. Invalid upstream handoff가 Great Circle/cache/fixture/다른 provider fallback으로 바뀌지 않게 한다.
5. Detached source batch/provider response를 두 번째 authority로 받지 않는지 architecture/API 검사로 확인한다.

**검증 command/test**

```bash
mvn -pl rpdptw/core -am \
  -Dtest=TravelSourceHandoffTest,TravelProvenanceSecurityTest test
```

**Expected**

- Accepted Phase 01 artifact의 declared sparse travel만 preparation input이 됨
- Invalid handoff/conflict/decimal/unauthorized source identity는 typed rejection
- Equivalent canonical declaration은 raw input order와 무관하게 동일
- Error/evidence/log에 credential, raw payload와 full coordinates 없음

**Failure·rollback·handoff**

- Invalid handoff에서는 draft를 만들지 않고 corrected Phase 01 artifact를 요구한다.
- Upstream acquisition failure를 test fixture나 generator로 이어가지 않는다.

### WP-02-3 — Complete directed travel preparation

**사전조건**

- WP-02-1/2 green
- Great Circle function/version과 reference vectors 승인

**수정 대상**

- `TravelPreparer`, `TravelGenerationPolicy`, `GreatCircleDistanceFunction`
- `TravelPreparerTest`, property/oracle/fault tests

**구체 작업**

1. Provided `D/U` priority와 self `0/0`를 구현한다.
2. Missing `D`의 approved Great Circle + `HALF_UP`을 구현한다.
3. Missing `U`의 exact vehicle-specific `CEILING`을 구현한다.
4. Missing speed만 45를 사용하고 present-invalid를 거부한다.
5. `M²` 및 `V×M²` totality, asymmetric arc와 checked index/size를 검증한다.
6. Partial construction/cancel/injected exception 시 draft 전체를 폐기한다.

**검증 command/test**

```bash
mvn -pl rpdptw/core -am \
  -Dtest=TravelPreparerTest,TravelPreparerPropertyTest,TravelPreparationFaultInjectionTest,TravelPreparationComplexityTest \
  test
```

**Expected**

- Unit/property/fault test 전부 pass, failed/skipped 0
- Great Circle call 수는 missing non-self `D` cell 수와 같고 reverse용 추가 call 없음
- Common provided `U`는 vehicle 수와 무관하게 그대로 resolve
- 실패 뒤 complete artifact/fingerprint/cache write 0개

**Failure·rollback·handoff**

- Arithmetic/coverage/reference/fault면 typed rejection 하나와 safe evidence만 남긴다.
- Partial table을 cache나 fixture로 저장하지 않는다.

### WP-02-4 — `PreparedTravel` canonicalization, fingerprint와 cache integrity

**사전조건**

- Complete preparation green
- Canonical encoding/fingerprint ADR reviewed

**수정 대상**

- `PreparedTravel`, canonical encoder, fingerprint/report
- Optional whole-artifact cache contract fake
- Immutability/corruption/reproducibility tests

**구체 작업**

1. Stable dense order로 canonical encoding한다.
2. Value, source kind, policy/version, mapping을 fingerprint에 포함한다.
3. Volatile provider metadata/ordering을 semantic fingerprint에서 제외한다.
4. Defensive copy와 total lookup API를 제공한다.
5. Same-key/different-bytes, digest mismatch, altered cell/provenance/mapping을 거부한다.
6. Cache hit/miss/disabled가 동일 bytes를 만드는지 검증한다.

**검증 command/test**

```bash
mvn -pl rpdptw/core -am \
  -Dtest=PreparedTravelImmutabilityTest,PreparedTravelCorruptionTest,TravelFingerprintReproducibilityTest,TravelPreparationReportTest \
  test
```

**Expected**

- Mutation/corruption/ordering/cache poison을 실제 검출
- Same authority input 반복에서 canonical bytes와 fingerprint 동일
- Source/policy/value 변경에서 fingerprint 변경

**Failure·rollback·handoff**

- Corrupt cache/object는 integrity incident로 격리하고 재사용하지 않는다.
- Encoding 변경이 필요하면 schema/version을 올리고 old identity에 overwrite하지 않는다.

### WP-02-5 — Immutable `ProblemInstance` freeze

**사전조건**

- Phase 01 normalized facts identity green
- WP-02-4 immutable travel green

**수정 대상**

- `ProblemInstance`, factory/fingerprint
- `ProblemInstanceTest`, corruption/reference tests

**구체 작업**

1. Dense ID array length, bijection과 typed reference를 재검증한다.
2. Request pickup/delivery, solver node→location, vehicle→terminal/travel view를 검증한다.
3. Pair-level static compatibility facts와 explicit absence를 보존한다.
4. Exact prepared fingerprint를 problem에 bind한다.
5. No-compatible-vehicle request를 structural error로 바꾸지 않는다.
6. Constructor input과 returned view alias를 차단한다.

**검증 command/test**

```bash
mvn -pl rpdptw/core -am \
  -Dtest=ProblemInstanceTest,ProblemInstanceCorruptionTest,DenseIdBijectionPropertyTest \
  test
```

**Expected**

- Valid fixture freeze와 모든 invalid reference/corruption rejection
- Problem과 PreparedTravel fingerprint equality
- Original builder/map/array mutation 뒤 problem bytes 불변

**Failure·rollback·handoff**

- Problem draft 전체 폐기, `PreparedTravel` 자체는 immutable input으로 보존 가능
- Problem artifact가 발행된 뒤 travel identity를 바꾸지 않는다. 새 identity로 다시 prepare/freeze한다.

### WP-02-6 — Architecture, integration, evidence와 handoff

**사전조건**

- WP-02-1~5 green
- Independent reviewer 지정

**수정 대상**

- Architecture rules/test
- Evidence manifests/reports
- Code 외 authoritative registry는 총괄 스케줄러만 갱신

**구체 작업**

1. Core cloud/provider/HTTP/Jackson SDK leakage와 runtime fallback symbol을 검사한다.
2. Generic gate는 test-only integer hand oracle로 실행한다. Approved fixture/official snapshot이 별도 제공된 경우에만 scope와 identity를 명시한 추가 integration을 실행하고 source class label을 섞지 않는다.
3. Solver/verifier consumer contract가 exact fingerprint mismatch를 거부하는 future-red test를 준비한다.
4. `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`을 immutable bundle로 묶는다.
5. Phase 02 review verdict와 Phase 03 handoff acceptance를 받는다.

**검증 command/test**

```bash
mvn -pl build/architecture-rules,rpdptw/core -am verify
mvn verify
```

**Expected**

- License-free root build pass
- Phase 02 required test failed/skipped 0
- Core provider SDK reference, runtime lazy/reverse/symmetric fallback 0
- Evidence key별 report/digest와 handoff identity 존재

**Failure·rollback·handoff**

- Review fail이면 phase는 `IMPLEMENTED_PENDING_EVIDENCE`/`FAILED` 중 reviewer 판단 상태이며 `ACCEPTED`가 아니다.
- Phase 03에는 artifact를 넘기지 않고 마지막 accepted Phase 01 handoff를 safe point로 유지한다.

## 10. Exact test inventory, fixture/builder와 oracle

### 10.1 Test class와 method

| Test class | Exact method | Fixture/oracle | 잡아야 하는 결함 |
|---|---|---|---|
| `DenseIdBijectionTest` | `roundTripsEveryExternalAndDenseId()` | `CanonicalInputFixtureBuilder` | Wrong reverse mapping |
|  | `rejectsDuplicateExternalIdsAndDenseHoles()` | Duplicate/hole builder | Silent overwrite/hole |
| `DenseIdBijectionPropertyTest` | `shuffledEquivalentEntitiesProduceCanonicalMapping()` | Seeded permutations, shrinkable list | Nondeterministic ordering |
|  | `checkedCellIndexNeverWraps()` | Boundary `M` generator | `M²` overflow |
| `TravelPreparerTest` | `usesProvidedDirectedIntegerDistanceAndCommonTimeFirst()` | `TEST_ONLY_HAND_ORACLE` | Generation overwrites provided |
|  | `rejectsDecimalProvidedDistanceOrTimeWithoutRounding()` | Decimal token fixture | Decimal coercion |
|  | `normalizesEverySelfArcToZeroAndRecordsOverride()` | Non-zero raw diagonal | Raw diagonal authority |
|  | `preservesAsymmetricDirectedArcs()` | A→B/B→A distinct test-only values | Reverse/symmetric fallback |
|  | `generatesOnlyMissingDistanceWithApprovedFunctionAndHalfUp()` | Stub approved function returns boundary decimals | Wrong priority/rounding |
|  | `rejectsMissingDistanceWhenCoordinateIsAbsent()` | Missing coordinate | Hidden distance fallback |
|  | `generatesVehicleSpecificMissingTimeWithCeiling()` | Exact rational hand oracle | FLOOR/double error |
|  | `usesFortyFiveOnlyWhenSpeedIsMissing()` | Missing-speed variant | Wrong default trigger |
|  | `rejectsPresentInvalidSpeedInsteadOfDefaulting()` | Zero/negative/invalid speed | Invalid→missing coercion |
|  | `keepsProvidedCommonTimeWhenDistanceWasGenerated()` | Missing D/present U | U overwritten |
|  | `rejectsDuplicateConflictingDirectedCell()` | Conflicting batch | Last-write-wins |
|  | `rejectsUnknownLocationOrVehicleReference()` | Unknown IDs | Dangling reference |
| `TravelPreparerPropertyTest` | `resolvesExactlyAllMByMDirectedDistanceCells()` | Generated `M` locations | Incomplete coverage |
|  | `resolvesEveryUsedVehicleTimeCell()` | Generated `M,V` | Partial vehicle time |
|  | `permutedSourceOrderHasSameBytesAndFingerprint()` | Seeded permutations | Map/provider ordering leak |
|  | `differentSourcePolicyOrValueChangesFingerprint()` | One-field mutation | Fingerprint under-binding |
| `PreparedTravelImmutabilityTest` | `mutatingConstructorInputsCannotChangeArtifact()` | Mutable arrays/maps | Constructor alias |
|  | `returnedViewsCannotMutateBackingState()` | Mutation probes | Accessor alias |
|  | `problemAndTravelDoNotShareMutableBuilderState()` | Shared builder | Cross-artifact alias |
| `TravelPreparationFaultInjectionTest` | `invalidSourceHandoffFailsBeforeGeneration()` | Invalid/incomplete Phase 01 artifact | Invalid handoff→generation fallback |
|  | `upstreamFailureMarkerNeverFallsBackToFixtureCacheReverseOrGenerator()` | Call-count spies | Unauthorized fallback |
|  | `injectedFailurePublishesNoPartialArtifactOrCacheEntry()` | Fail-after-N fake | Partial commit |
|  | `cancelledPreparationDiscardsDraft()` | Cooperative cancellation fake | Partial artifact exposure |
|  | `checkedDistanceTimeAndIndexOverflowAreTypedFailures()` | Boundary oracle | Wrap/saturation |
| `PreparedTravelCorruptionTest` | `rejectsAlteredCellWithUnchangedDeclaredFingerprint()` | `CorruptedPreparedTravelBuilder` | Value corruption |
|  | `rejectsAlteredProvenanceOrMissingCoverage()` | Source/coverage corruption | Provenance/partial corruption |
|  | `rejectsSameIdentityDifferentCanonicalBytes()` | Cache fake | Identity collision/overwrite |
| `TravelFingerprintReproducibilityTest` | `repeatPreparationProducesIdenticalCanonicalBytes()` | Fixed test-only manifest | Hidden nondeterminism |
|  | `cacheHitMissAndDisabledProduceSameArtifact()` | Whole-artifact cache fake | Cache authority |
|  | `volatileProviderMetadataDoesNotChangeSemanticFingerprint()` | Timestamp/order/attempt variants | Runtime metadata leakage |
| `ProblemInstanceTest` | `freezesValidPairNodeLocationVehicleReferences()` | Canonical builder + prepared travel | Invalid freeze |
|  | `bindsExactPreparedTravelFingerprint()` | Exact fingerprint oracle | Detached travel |
|  | `keepsNoCompatibleVehicleAsStaticFactNotStructuralError()` | No-eligible request | Wrong input rejection |
| `ProblemInstanceCorruptionTest` | `rejectsSplitDanglingOrWrongKindPairReferences()` | Corruption builder | Pair/reference corruption |
|  | `rejectsTravelMappingOrFingerprintMismatch()` | Swapped mapping | Wrong travel authority |
|  | `rejectsArrayLengthAndDenseRangeCorruption()` | Truncated arrays | Partial snapshot |
| `TravelSourceHandoffTest` | `acceptsOnlyTravelSealedInThePhase01Artifact()` | Detached-batch negative fixture | Multiple authority paths |
|  | `rejectsInvalidDeclaredSourceIdentityBeforeGeneration()` | One-field identity corruption | Invalid handoff→generation fallback |
|  | `preservesApprovedFixtureAndOfficialSnapshotDistinction()` | Two typed identities | Authority label confusion |
| `TravelProvenanceSecurityTest` | `redactsCredentialRawPayloadAddressAndCoordinatesFromFailureEvidence()` | Secret/PII markers | Data leakage |
|  | `providerLocatorAndAttemptDoNotBecomeDomainIdentity()` | Locator/attempt variants | Provider coupling |
| `TravelPreparationReportTest` | `aggregateCountsMatchPreparedCoverageAndSourceKinds()` | Independent count oracle | Misleading coverage/source telemetry |
|  | `safeReportDoesNotChangeSemanticFingerprint()` | Report metadata variants | Observability→domain identity coupling |
| `TravelPreparationComplexityTest` | `callsGreatCircleExactlyOncePerMissingNonSelfDistance()` | Counting function | Reverse/repeated generation |
|  | `doesNotPerformRuntimeOrPostFreezeSourceCalls()` | Throw-after-freeze fake | Lazy fallback |
| `Phase02ArchitectureTest` | `coreHasNoProviderCloudHttpSdkDependency()` | Bytecode/dependency rule | Provider SDK leakage |
|  | `solverAndVerifierCannotReprepareOrFallbackTravel()` | Package/symbol rule | Multiple authority paths |
|  | `productionScopeContainsNoTestFixtureClass()` | Scope inspection | Fixture leakage |

### 10.2 Red → green 기준

1. Test 이름과 independent expected value를 먼저 작성한다.
2. 현재 module/type 부재로 compile 또는 assertion failure가 나는 상태를 future-red evidence에 기록한다.
3. Test-only Great Circle stub은 rounding boundary만 검증한다. 실제 함수 정확성 green은 승인 reference vectors로만 얻는다.
4. 최소 구현 후 targeted class command를 green으로 만든다.
5. Fault/corruption/property/architecture suite를 green으로 만든다.
6. Root `mvn verify`와 동일 manifest 반복을 green으로 만든다.
7. Test를 삭제, 약화, `@Disabled`, broad exception catch 또는 fixture source label 변경으로 green 처리하지 않는다.

### 10.3 Oracle 규칙

- `TravelHandOracle`은 production preparer를 호출하지 않는다.
- Provided integer oracle은 표에 선언된 test-only 정수를 그대로 비교한다.
- Generated `U` oracle은 exact decimal/rational 식과 `CEILING`을 독립 계산한다.
- Great Circle numeric oracle는 승인된 reference vector/함수 version이 없으면 green을 주장하지 않는다.
- Coverage oracle는 expected key set을 Cartesian product로 독립 생성한다.
- Fingerprint oracle는 production collection iteration을 재사용하지 않고 canonical tuple list를 별도로 만든다.
- Corruption builder는 정상 production encoder를 통해 “손상”을 다시 정상화하지 않고 bytes/value/source/mapping을 직접 한 지점씩 변경한다.

### 10.4 Test category와 합격 판정

| Category | Command/대표 suite | 합격 |
|---|---|---|
| Unit/boundary | Core targeted `-Dtest=TravelPreparerTest,...` | Positive/negative/boundary/overflow 전부 pass |
| Property | `DenseIdBijectionPropertyTest`, `TravelPreparerPropertyTest` | Seed 기록, shrink 가능한 failure, 반복 pass |
| Handoff contract | `TravelSourceHandoffTest` | Phase 01 artifact만 source authority이며 detached/invalid handoff rejection |
| Generic integration | Test-only integer hand oracle | Exact expected values, complete preparation, no skipped; official claim 금지 |
| Optional authority integration | Approved integer fixture 또는 official snapshot profile | Authority가 별도 제공된 경우 exact source identity/digest와 scope를 기록 |
| Fault | `TravelPreparationFaultInjectionTest` | Pre-state 보존, partial artifact/cache write 0 |
| Corruption | `PreparedTravelCorruptionTest`, `ProblemInstanceCorruptionTest` | 모든 one-field corruption typed rejection |
| Reproducibility | `TravelFingerprintReproducibilityTest` | Repeated canonical bytes/fingerprint identical |
| Security/observability | `TravelProvenanceSecurityTest`, `TravelPreparationReportTest`, architecture rule | Secret/PII/provider SDK leakage 0; safe aggregate count와 artifact coverage 일치 |
| Performance | `TravelPreparationComplexityTest` + approved-envelope future integration | Structural call-count bound pass; wall-time/memory threshold는 승인 envelope 없이는 만들지 않음 |

공식 wall-time, 최대 `M/V`, memory threshold는 authority source에 없다. 임의 숫자를 넣지 않는다. Performance owner가 workload/environment/repetition/threshold를 승인하면 exact test와 command를 evidence manifest에 추가한다. 그 전에는 structural complexity evidence만 Phase 02에 적용하고 production sizing을 주장하지 않는다.

## 11. Gate, evidence bundle과 Definition of Done

### 11.1 Exit gate

다음 AND gate를 모두 만족해야 한다.

- Phase 00/01가 `ACCEPTED`이고 exact handoff fingerprint가 있다.
- Approved Great Circle function/version과 typed source policy가 있다.
- `M²` distance 및 모든 used vehicle time coverage가 total이다.
- Provided/generated priority, self `0/0`, asymmetry, `HALF_UP`, `CEILING`, missing-only 45가 검증됐다.
- Dense bijection과 all request/node/location/vehicle reference가 검증됐다.
- Mutation/alias/invalid handoff/ordering/overflow/fault/corruption test가 실제 결함을 검출한다.
- PreparedTravel/ProblemInstance의 canonical bytes, fingerprints와 provenance가 repeatable하다.
- Runtime lazy/reverse/symmetric/provider fallback과 core SDK dependency가 0이다.
- Phase 03 consumer와 Phase 07 verifier용 fingerprint equality contract가 준비됐다.
- Evidence bundle과 independent review가 통과했다.

### 11.2 Evidence bundle

| Key | 최소 내용 |
|---|---|
| `E-P02-TRAVEL` | Source/policy/function approval, test-only oracle identity와 별도 제공 시 approved fixture/snapshot scope/digest, exact commands, test report, `M²`/vehicle coverage, safe aggregate report, formula/oracle, asymmetry, no-fallback, corruption/reproducibility/security result |
| `E-P02-DENSE-ID` | External↔dense canonical mapping digest, bijection/property/overflow report, node→location and vehicle mapping evidence |
| `E-P02-PROBLEM` | Problem schema/fingerprint, PreparedTravel equality, pair/reference/range/no-alias/corruption report, Phase 03 handoff manifest |

Bundle 공통 metadata:

```text
phase/document/review version
source commit and exact cited section/version/status
toolchain/build/runtime fingerprint
Phase01 input artifact identities
Great Circle/source policy approval references
exact command + exit code
test passed/failed/skipped counts
known limitation and OPEN/GATED/DEFERRED items
reviewer/verdict/timestamp
handoff artifact identities
rollback/last-safe-point reference
```

Console 한 줄, mutable `target/`, source/test file 존재, current placeholder test와 unapproved fixture는 evidence가 아니다.

### 11.3 Phase 02 Definition of Done

Phase 02는 다음을 모두 만족하고 총괄 스케줄러가 registry를 전이한 뒤에만 `ACCEPTED`다.

1. Entry/exit gate와 owner가 확인됐다.
2. 이 상세 문서와 [actual Phase 02 review](../reviews/phase-02-review.md)가 존재하고 승인됐다.
3. Scope 외 propagation/evaluation/ALNS/provider SDK를 당기지 않았다.
4. 모든 applicable positive/negative/boundary/property/fault/corruption/security/observability/reproducibility test가 failed/skipped 0이다.
5. Architecture dependency와 test-fixture leakage가 0이다.
6. Evidence three-key bundle이 digest-protected immutable identity를 가진다.
7. Phase 03가 exact handoff를 소비할 수 있고 rollback point가 있다.
8. OPEN/GATED/deferred와 proposed API/수치를 production default로 채우지 않았다.

### 11.4 Anti-pattern

- `Map<String,Object>` 또는 raw JSON/provider DTO를 `ProblemInstance`에 보관
- Node ID를 travel matrix key로 사용
- Missing arc에서 reverse, symmetric average, nearest ID 또는 0 사용
- Provider unavailable/partial을 intentional missing으로 바꾸고 Great Circle 실행
- Test fixture를 provider failure fallback으로 사용
- Decimal `D/U`를 `longValue`, cast, `Math.round`로 수용
- 모든 speed 오류에 45 적용
- `double` 누적 후 우연한 epsilon/tolerance로 rounding
- Diagonal raw `9999/0`을 보존
- Common `U`와 generated vehicle time을 같은 source label로 저장
- Mutable array/bitset/map accessor
- `M*M`, `V*M*M`, distance/time formula의 unchecked arithmetic
- HashMap iteration, provider response arrival order, clock 또는 random UUID를 fingerprint input으로 사용
- Partial table/cache entry를 `PreparedTravel`로 publish
- `latest` cache/profile/provider snapshot 사용
- Search/propagator/verifier에서 좌표·speed/provider를 다시 조회
- 동일 artifact identity에 다른 bytes overwrite
- Current Win decimal fixture를 approved integer/official evidence로 명명

## 12. Blocker, deferred/open와 restart

| Item | 상태 | Owner | 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00 accepted module baseline | `BLOCKER` | Architecture + scheduler | 모든 Phase 02 production code | Current root single-project checkout | `E-P00-BUILD/ARCH`와 accepted review |
| Phase 01 accepted artifact | `BLOCKER` | Phase 01 + scheduler | Phase 02 implementation/acceptance | Read-only authority/inventory | `E-P01-*`, immutable handoff identity와 accepted review |
| Great Circle function/version | `BLOCKER` | Input·Matrix + Architecture | Missing `D` green과 Phase exit | Provided-complete travel contract만 review 가능 | Function/Earth model/precision/reference vectors/approval |
| Typed travel source policy | `BLOCKER` | Input·Matrix + Phase 01 owner | Provided/generated authority와 no-fallback exit | Test-only hand oracle | Source allowlist/priority/declared absence/source identity approval |
| Approved integer fixture/official snapshot | External authority gate; generic Phase 02 blocker 아님 | Input·Matrix + Benchmark/provider owner | 해당 artifact의 official/integration/benchmark 사용만 | Generic unit/property/integration의 test-only hand oracle | Versioned integer artifact, digest, expected coverage, explicit scope approval |
| Current Win decimal `D/U` | Non-compliant fixture blocker | Input·Matrix + Benchmark | 해당 fixture의 canonical/official use | Read-only raw fixture | Compliant integer matrix 또는 explicit contract migration approval |
| `Q-BENCH-02` official execution 수치 | `OPEN — EXPERIMENT_REQUIRED` | Benchmark·Quality | Phase 14, not Phase 02 semantics | Phase 02 immutable artifacts | Calibration protocol/results/explicit approval |
| `C-17` route pool/MIP | `GATED TARGET` | Product·Algorithm·Architecture + solver/license | Phase 13 only | ALNS-only critical path | Phase 06/07 baseline + scope/solver/license/native/fallback approval |
| `Q-VAR-01` | `DEFERRED` | Product·Domain·Algorithm | Optional variants | Current atomic pair/travel contract | Variant/fixture/core-impact feasibility + separate approval |
| Multi-trip/rotation | Deferred feature | Product·Domain·Algorithm | Trip-specific problem meaning | Oneway/single roundtrip | Trip/reset/depot/resource contract + approval |
| Proposed public API/schema/fingerprint algorithm | `OPEN` | Product/API/Data + Architecture | External compatibility promise | Internal proposed types | Versioned contract, migration/security review and approval |
| Wall-time/memory performance threshold | `OPEN` | Performance·Operations | Production sizing claim | Structural complexity tests | Approved workload envelope/environment/repetitions/threshold |

동일 blocker가 해제되면 source commit, cited section drift와 current inventory를 다시 확인한다. Authority 문서가 바뀌었으면 단순 재실행하지 않고 requirement/test/evidence impact review부터 다시 시작한다.

## 13. 이전/다음 Phase handoff

### 13.1 Phase 01 → Phase 02

Source: [Phase 01 — canonical input normalization](phase-01-canonical-input-normalization.md) (**actual document; implementation `NOT_STARTED`, predecessor-blocked**).

Phase 02가 소비:

- `NormalizedInputArtifact` schema/identity, raw/semantic/envelope fingerprints
- Immutable request/service declarations, physical locations, vehicles
- Exact normalized units/time/service/compatibility facts
- Typed coordinates and speed absence/validity
- Typed sparse directed `D/U` absence/presence
- Alias/coercion/default/source policy provenance
- Terminal/trip/resource declarations and missing-limit typed absence
- Phase 01 error closure

Phase 02는 raw external bytes를 다시 parsing하거나 Phase 01 numeric/time/alias 정책을 재해석하지 않는다. Entry mismatch는 corrected Phase 01 artifact를 요구한다.

### 13.2 Phase 02 → Phase 03

Consumer: [Phase 03 — route propagation/evaluation kernel](phase-03-route-propagation-evaluation-kernel.md) (**actual document; entry is blocked until Phase 02 acceptance**).

Handoff bundle:

```text
ProblemInstanceRef
  schemaVersion
  problemFingerprint
  canonicalInputFingerprint
  dense mapping fingerprints
  preparedTravelFingerprint
  contentDigest

PreparedTravelRef
  schemaVersion
  preparedTravelFingerprint
  location/vehicle mapping fingerprints
  source/generation policy fingerprints
  exact M² / used-vehicle coverage proof
  contentDigest

Phase02HandoffManifest
  Phase01 input identities
  Great Circle/source policy identities
  E-P02-TRAVEL / DENSE-ID / PROBLEM refs
  accepted review ref
```

Phase 03가 받을 수 있는 것은 immutable lookup, identity와 provenance뿐이다. 다음은 handoff하지 않는다.

- Raw provider response/SDK/client/credential
- Coordinates/speed를 이용한 fallback function
- Partial table/draft/cache handle
- Mutable array/map/bitset
- Route evaluation, cache, objective, profile 또는 ALNS config
- Test-only/approved fixture를 official이라고 바꾸는 label

Phase 03 entry test는 `ProblemInstance.preparedTravelFingerprint == PreparedTravel.fingerprint`를 확인하고 다르면 propagation을 시작하지 않는다.

### 13.3 이후 consumer

- Phase 05는 Phase 03/04를 거쳐 immutable problem/travel만 소비한다.
- Phase 07 candidate/result verifier는 solver cache가 아니라 같은 `PreparedTravel` authority와 problem declaration을 소비한다.
- Phase 14 official manifest는 별도로 승인된 compliant integer fixture와 모든 fingerprint를 요구한다. Phase 02 test-only artifact는 자동 승격되지 않는다.

## 14. Source → requirement → test traceability

| Requirement ID | Requirement | Source | Exact tests | Evidence |
|---|---|---|---|---|
| `REQ-P02-AUTH` | User-locked authority; REVIEW는 중단 아님, open/gated/deferred 보존 | [README §3](../README.md#3-source-authority), [Plan §2](../master-realization-plan.md#2-입력-권위와-충돌-규칙) | Document citation/link/version/status self-check | Bundle metadata |
| `REQ-P02-TRAVEL-KEY` | Physical location directed key, node/location identity 분리 | [Master §5.1](../../master-design.md#51-핵심-개념), [Master §8](../../master-design.md#8-directed-distancetime-matrix-계약) | `roundTripsEveryExternalAndDenseId`, `preservesAsymmetricDirectedArcs` | `E-P02-DENSE-ID`, `E-P02-TRAVEL` |
| `REQ-P02-INTEGER` | Provided `D/U` integer meter/second, decimal reject, `C` non-authoritative | `Q-MTX-01~02`, [Domain §6](../../2026-07-26-domain-design.md#6-travel-preparation) | `rejectsDecimalProvidedDistanceOrTimeWithoutRounding`, `usesProvidedDirectedIntegerDistanceAndCommonTimeFirst` | `E-P02-TRAVEL` |
| `REQ-P02-SELF` | Self `0/0` regardless raw | `Q-MTX-02`, [Master §8](../../master-design.md#8-directed-distancetime-matrix-계약) | `normalizesEverySelfArcToZeroAndRecordsOverride` | `E-P02-TRAVEL` |
| `REQ-P02-D-GEN` | Missing D approved Great Circle + meter HALF_UP, coordinate required | `Q-MTX-03`, [Domain §6](../../2026-07-26-domain-design.md#6-travel-preparation) | `generatesOnlyMissingDistanceWithApprovedFunctionAndHalfUp`, `rejectsMissingDistanceWhenCoordinateIsAbsent` | `E-P02-TRAVEL` |
| `REQ-P02-U-GEN` | Missing U vehicle-specific CEILING, missing speed 45 only | `Q-MTX-02~03`, [Integrated §6.2](../../architecture-domain-implementation-design.md#62-complete-preparation) | `generatesVehicleSpecificMissingTimeWithCeiling`, `usesFortyFiveOnlyWhenSpeedIsMissing`, `rejectsPresentInvalidSpeedInsteadOfDefaulting` | `E-P02-TRAVEL` |
| `REQ-P02-COVERAGE` | Complete `M²` and every used-vehicle time before solve | `C-13`, [Plan Phase 02](../master-realization-plan.md#phase-02--이동-자료-준비와-immutable-problem) | `resolvesExactlyAllMByMDirectedDistanceCells`, `resolvesEveryUsedVehicleTimeCell` | `E-P02-TRAVEL` |
| `REQ-P02-NO-FALLBACK` | No lazy/reverse/symmetric/provider fallback | [Master §8](../../master-design.md#8-directed-distancetime-matrix-계약), [Integrated §6.3](../../architecture-domain-implementation-design.md#63-runtime-prohibition와-provenance) | `upstreamFailureMarkerNeverFallsBackToFixtureCacheReverseOrGenerator`, `doesNotPerformRuntimeOrPostFreezeSourceCalls`, architecture tests | `E-P02-TRAVEL` |
| `REQ-P02-SOURCE` | Approved fixture/official/provided/generated source 구분과 provenance | [Domain §6](../../2026-07-26-domain-design.md#6-travel-preparation), [Integrated §19](../../architecture-domain-implementation-design.md#19-configuration-provenance와-observability) | `preservesApprovedFixtureAndOfficialSnapshotDistinction`, `differentSourcePolicyOrValueChangesFingerprint` | `E-P02-TRAVEL` |
| `REQ-P02-BOUNDARY` | Phase 01 artifact is the only source input; application/provider scope is not pulled forward; core SDK 0 | [Phase 01 §6.2](phase-01-canonical-input-normalization.md#62-산출물), [Architecture §2.2~2.5](../../2026-07-26-architecture-design.md#2-module과-package-경계) | `acceptsOnlyTravelSealedInThePhase01Artifact`, `coreHasNoProviderCloudHttpSdkDependency` | `E-P02-TRAVEL`, architecture report |
| `REQ-P02-ABSENCE` | Only typed absent generates; invalid/incomplete handoff never absent | [Master §4.2](../../master-design.md#42-단계별-데이터-계약), [Plan Phase 02](../master-realization-plan.md#phase-02--이동-자료-준비와-immutable-problem) | `invalidSourceHandoffFailsBeforeGeneration`, invalid/duplicate/reference tests | `E-P02-TRAVEL` |
| `REQ-P02-DENSE` | External↔dense bijection, checked range, separate identity kinds | [Domain §7](../../2026-07-26-domain-design.md#7-immutable-solver-model), [Integrated §6.4](../../architecture-domain-implementation-design.md#64-dense-identity와-immutable-problem) | `shuffledEquivalentEntitiesProduceCanonicalMapping`, `checkedCellIndexNeverWraps` | `E-P02-DENSE-ID` |
| `REQ-P02-PROBLEM` | Immutable ProblemInstance, pair/reference checks, exact travel bind | [Domain §7](../../2026-07-26-domain-design.md#7-immutable-solver-model), `C-06` | `freezesValidPairNodeLocationVehicleReferences`, all `ProblemInstanceCorruptionTest` | `E-P02-PROBLEM` |
| `REQ-P02-IMMUTABLE` | No constructor/accessor/cross-artifact alias | [Master §4.5~4.6](../../master-design.md#45-상태와-산출물의-생명주기), [Plan §4.2](../master-realization-plan.md#42-compileruntime-invariants) | All `PreparedTravelImmutabilityTest` methods | `E-P02-PROBLEM` |
| `REQ-P02-FINGERPRINT` | Stable canonical fingerprint/provenance; solver/verifier equality | `C-13`, [Domain §17.4](../../2026-07-26-domain-design.md#174-travel) | `repeatPreparationProducesIdenticalCanonicalBytes`, mismatch/corruption tests | `E-P02-TRAVEL`, `E-P02-PROBLEM` |
| `REQ-P02-FAULT` | Partial/fault/cancel/overflow publishes no partial artifact | [Plan §8](../master-realization-plan.md#8-공통-테스트-전략), [Integrated §21~22](../../architecture-domain-implementation-design.md#21-failure와-retry-matrix) | All `TravelPreparationFaultInjectionTest` methods | `E-P02-TRAVEL` |
| `REQ-P02-CORRUPTION` | Value/source/coverage/mapping/digest corruption rejection | [Integrated §22.4](../../architecture-domain-implementation-design.md#224-independent-corruption-fixtures) | `PreparedTravelCorruptionTest`, `ProblemInstanceCorruptionTest` | `E-P02-TRAVEL`, `E-P02-PROBLEM` |
| `REQ-P02-REPRO` | Ordering/cache/provider metadata cannot change semantic result | [Master §13.2](../../master-design.md#132-strong-reproducibility-envelope), [Plan §13](../master-realization-plan.md#13-위험-보안-운영-관측과-재현성) | Property permutation and all `TravelFingerprintReproducibilityTest` | All three keys |
| `REQ-P02-SECURITY` | No credential/PII/raw input leak; provider locator not domain identity | [Integrated §20](../../architecture-domain-implementation-design.md#20-security와-tenant-boundary) | All `TravelProvenanceSecurityTest` methods | Security report |
| `REQ-P02-OBS` | Safe aggregate report is coverage-consistent and never semantic authority | [Integrated §19.3](../../architecture-domain-implementation-design.md#193-correlation-fields), [Plan §13](../master-realization-plan.md#13-위험-보안-운영-관측과-재현성) | All `TravelPreparationReportTest` methods | `E-P02-TRAVEL` safe report |
| `REQ-P02-HANDOFF` | Phase 03 gets immutable problem/travel only; no propagation/ALNS pull-forward | [Plan Phase 02~03](../master-realization-plan.md#phase-02--이동-자료-준비와-immutable-problem) | Fingerprint equality consumer contract future-red | `E-P02-PROBLEM`, Phase 03 receipt |

새 requirement가 발견되면 source, owner, test와 evidence key를 함께 추가한다. Source 문장 없이 production default/API/수치를 추가하지 않는다.
