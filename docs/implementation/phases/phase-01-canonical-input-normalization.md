# Phase 01 — 내부 표준 입력과 정규화

```yaml
document_status: REVIEWED_WITH_CORRECTIONS
document_workflow_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
phase_execution_status: BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE
implementation_status: NOT_STARTED
implementation_evidence_status: NOT_PRODUCED
phase_acceptance_status: NOT_ACCEPTED
phase: 01
canonical_slug: phase-01-canonical-input-normalization
plan_version: 1.0
baseline_date: 2026-07-28
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
implementation_claim: NONE
public_api_status: PROPOSED_NOT_APPROVED
wire_schema_status: OPEN_REQUIRES_PRODUCT_API_DATA_OWNER_APPROVAL
scheduler_task_id: TBD
owners:
  phase: Domain·Input implementation owner role
  adapter_schema: Product·API·Data contract owner role
  numeric_time: Domain·Input owner role
  review: independent Phase 01 reviewer role, assignee TBD
  handoff_consumer: Phase 02 Domain·Travel owner role
prerequisites:
  - Phase 00 detailed plan and review exist and are approved
  - E-P00-BUILD accepted
  - E-P00-ARCH accepted
  - E-P00-LEGACY accepted
  - accepted module/package names and test command are recorded
planned_handoff:
  - phase-02-prepared-travel-immutable-problem.md
source_fingerprints_sha256:
  docs/master-design.md: 58554334b9f27586c93a685adc0facf0fbd7e79576c18890f0ac13891b2f803b
  docs/2026-07-26-domain-design.md: 1870662f85a08cc9a1e48a1974b96278eccddfd1519721d71b356c56034ecaab
  docs/2026-07-26-architecture-design.md: 3d4dbbfc7e4cbdb2f3985378d84fd5f717db770b04131573c00ed354a9f41614
  docs/architecture-domain-implementation-design.md: ec513ac1b0bacd88149683bf48c36f7e6edcd53a9232498597e3b0d57c585875
  docs/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 5921213ae419b9398bde8c91c3d6ada5aa64bf22a9b823e5b3889e1642085c05
  docs/implementation/README.md: accf7758802c253ae47e3d0fe41e190728c507195d0b41f27f14a25804c8f23f
historical_cross_check_sha256:
  docs/2026-07-26-master-design.md: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
source_sections:
  master:
    - "§1.5, §3, §4.1~§4.7, §5~§8, §15.1~§15.3, §16~§17"
  domain:
    - "§1~§7, §16~§18, §20~§21"
  architecture:
    - "§1, §2, §5.6, §6"
  integrated:
    - "§1~§5, §22~§25, §27~§30"
  open_questions:
    - "Q-NUM-01~03, Q-TIME-01~04, Q-IN-01~02, Q-COMP-01~02, Q-REQ-01~02, Q-RES-01, Q-BENCH-03, Q-INFRA-01, Q-VAR-01"
  realization_plan:
    - "§1~§7 Phase 00~02, §8~§15"
  implementation_readme:
    - "§1, §3~§7"
```

## 1. 문서 지위와 사용 규칙

이 구현 문서 세트의 입력 권위는 **사용자 선언으로 고정**되어 있다. 원문 metadata의 `REVIEW`는 source provenance로 기록하지만 이 문서 작성을 중단시키지 않는다. 반대로 이 문서가 작성되었다는 사실은 Phase 00 또는 Phase 01의 구현·review·evidence gate를 통과했다는 뜻이 아니다.

현재 checkout에는 Phase 00~14 상세 문서 **15/15**와 독립 review **15/15**가 모두 실제 존재한다. [Phase 00 상세 문서](phase-00-build-architecture-skeleton.md)의 review verdict는 `PASS_WITH_RESIDUAL_BLOCKERS`지만 Phase 00 implementation은 `NOT_STARTED`, evidence는 `NOT_PRODUCED`, phase acceptance는 `PLANNED/NOT_ACCEPTED`다. 이 Phase의 [Phase 01 review](../reviews/phase-01-review.md) verdict는 `ACCEPTED_WITH_APPLIED_CORRECTIONS`이며 문서 계약 review는 완료됐다. 그러나 문서/review의 존재나 verdict만으로 predecessor 구현이 accepted되는 것은 아니므로 Phase 01 실행 상태는 계속 `BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE`다. Entry evidence가 생기기 전 마지막 안전 지점은 **문서 review와 test/fixture 설계**이며 production source나 target POM을 수정하지 않는다.

질문 상태는 [Canonical Master](../../master-design.md)와 [질문 등록부](../../master-design-open-questions.md)의 최신 값인 `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`을 적용한다. Final Domain/Architecture에 남은 과거 `25/1/2`, `Q-INFRA-01 DEFERRED` 표기는 이 Phase의 결정을 되돌리지 않는다. `Q-BENCH-02`, `C-17`, `Q-VAR-01`과 public API/schema 미확정 항목은 임의의 수치·default·완료 상태로 바꾸지 않는다.

구체 package, type, method, canonical encoding과 error code 이름은 모두 **proposed internal design**이다. Phase 00의 accepted module tree 또는 별도 API/schema 승인이 다른 이름을 고르면 이름을 바꿀 수 있지만 이 문서의 의미, 불변조건, failure oracle과 dependency direction은 보존해야 한다.

### 1.1 권위 source baseline

| 입력 | 역할 |
|---|---|
| [Canonical Master](../../master-design.md) | 전체 requirement, 결정, 불변조건과 `RM-1` gate |
| [Final Domain Design](../../2026-07-26-domain-design.md) | Input, normalization, time, compatibility, travel handoff의 상세 의미 |
| [Final Architecture Design](../../2026-07-26-architecture-design.md) | Java 25/Maven module/package와 dependency placement |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | 15 Phase numbering과 Phase 01/02 경계 |
| [Master Design open questions](../../master-design-open-questions.md) | Exact `Q-*` 상태, owner와 restart condition |
| [Master Realization Plan](../master-realization-plan.md) | Current inventory, Phase contract, evidence와 DoD |
| [Implementation document map](../README.md) | Canonical filename, authority와 review workflow |
| [SUPERSEDED historical Master](../../2026-07-26-master-design.md) | 누락·퇴행 cross-check 전용; 현재 decision authority가 아님 |

위 파일들은 metadata의 SHA-256과 절 범위까지 전체 대조했다. Source fingerprint가 달라지면 구현 전에 영향 절을 다시 읽고 이 문서의 requirement/test trace를 review한다. `docs/codex/*`는 역사/참고 자료이므로 이 문서의 authority로 인용하거나 복사·수정하지 않는다.

### 1.2 2026-07-28 사용자 승인 Win fixture migration

사용자는 raw [win_poc_case.json](../../../data/win_poc_case.json)의
`distanceMatrix.D`를 meter, `distanceMatrix.U`를 second로 해석하고 각각 exact
decimal `FLOOR`해 별도 파일로 만드는 migration을 승인했다.
[floor_win_poc_matrix.py](../../../scripts/floor_win_poc_matrix.py)는 원본을 변경하지
않고 [win_poc_case_floor.json](../../../data/win_poc_case_floor.json)을 생성한다.

이 결정은 generic canonical parser가 임의의 decimal distance/time을 조용히
절삭하도록 바꾸지 않는다. Raw fixture는 decimal rejection/provenance 입력으로
유지하고, FLOOR fixture는 이미 integer로 migration된 별도 schema input으로
받는다. Migration identity와 source/output digest는
[Master Realization Plan §1.1](../master-realization-plan.md#11-사용자-고정-최종-성공-기준)을
따른다.

## 2. 목표, 범위와 비범위

### 2.1 목표

외부 request/fixture의 bytes와 명시된 schema/adapter version을 anti-corruption boundary에서 해석하여 다음 두 단계의 immutable artifact를 만든다.

```text
external bytes + declared schema/adapter identity
→ versioned anti-corruption adapter
→ CanonicalBusinessInput
→ strict normalization
→ NormalizedInputArtifact
```

`NormalizedInputArtifact`는 외부 표현의 alias, 문자열, 단위, 순서와 누락값을 solver가 다시 추정할 필요가 없도록 numeric, time, service, identity, compatibility, ownership와 trip 의미를 확정한다. 동일 artifact는 Phase 02가 dense identity, complete `PreparedTravel`과 immutable `ProblemInstance`를 만드는 유일한 입력이다.

### 2.2 범위

- 명시적 schema/adapter version 선택과 지원 version allowlist
- 외부 DTO와 provider-neutral canonical input의 분리
- Raw bytes digest와 적용 alias/coercion/default/policy provenance
- Plan identity, exact customer/profile/version과 objective preset의 명시적 선택 또는 omission
- External request/order/vehicle/location identity와 reference validation
- Delivery-only와 real pickup-delivery를 하나의 canonical `Request` 의미로 통합
- Optional mandatory declaration과 승인된 typed extension input의 보존·검증
- 무게·부피 `n=3/FLOOR`, item-first, `qty` 곱과 checked arithmetic
- 비용·거리·시간의 integer-only syntax와 단위
- Plan `[start,end)`, inclusive close, planning-origin `long` seconds
- Repeating/overnight window의 deterministic expansion과 clipping
- `duration + Σ(item.taskTime × qty)` service-time 정규화
- Vehicle size, request `vehicleFeatureList`, capability, zone, ownership 정규화
- `oneway`와 single `roundtrip`, `multiRotation` 처리와 `waitInDepot` 의미 고정
- Vehicle/global `maxStopCnt`, `maxDriveTime`, `maxDriveDist`의 typed present/absent 선언
- Sparse provided travel declaration의 syntax/reference 정규화
- Canonical ordering, duplicate/ambiguity detection, deterministic error ordering
- Immutable artifact lifecycle, semantic fingerprint와 Phase 02 handoff contract

### 2.3 비범위

- Missing `D/U` 생성, Great Circle 계산, `M²` coverage와 vehicle-resolved travel time
- Dense `RequestId`/`VehicleId`/`SolverNodeId`/`PhysicalLocationId` 할당
- `PreparedTravel` 또는 `ProblemInstance` 생성
- Route propagation, actual full-arc next-work-window 실행과 feasibility 판정
- `servableVehicles` dense bitset, final `PROVEN` unassignability 판정
- Profile/capability binding, preset default 해소, score, objective와 `SolvePlan`
- Pair insertion, route/bank state, portfolio, ALNS와 verifier
- HTTP/storage/cloud DTO, AWS/GCP SDK, object key 또는 public submission API 확정
- Route pool/MIP, provider 준비, solver/backend logic
- Current decimal `D/U` Win fixture를 compliant/official input으로 변경

Phase 01은 travel source를 **보존·검증하여 넘길 수는 있지만 준비하지 않는다**. Phase 02가 소비할 raw provided/generated-input facts만 산출하고, search 또는 verifier가 사용할 travel authority를 미리 만들지 않는다.

## 3. 적용 결정과 불변조건

### 3.1 확정 의미

| 영역 | 적용 결정 |
|---|---|
| Numeric | `Q-NUM-01~03`, `C-10~C-12`: 무게·부피 `n=3/FLOOR`, item-first 후 positive integer `qty`, 비용·거리·시간 integer-only, checked arithmetic |
| Time | `Q-TIME-01~04`: exact `yyyy-MM-dd HH:mm:ss`, solver 밖 timezone 처리, `[planStart,planEnd)`, inclusive window close, repeating/overnight window |
| Service | `Q-IN-01`: `reqDate`/`dueDate`는 완료기한 alias, `duration + Σ(item.taskTime×qty)`, order-level `taskTime` 거부 |
| Trip/resource declaration | `Q-IN-02`, `Q-BENCH-03`: oneway 우선, single roundtrip만 지원, route resource는 route 전체 의미 |
| Compatibility | `Q-COMP-01~02`, `C-05`: vehicle size와 capability 분리, exact `["ALL"]`, case-sensitive free-form code, size∧zone |
| Pair/service pattern | `Q-REQ-01~02`, `C-06~C-07`: delivery-only initial-load 의미와 real pair를 구분하고 single-trip pair 의미 유지 |
| Ownership | `Q-OBJ-03`, `Q-RES-01`, `C-15`: missing/null/empty=`DIRECT`, exact `DIRECT/LEASE`; outcome status가 아님 |
| Travel handoff | `Q-MTX-01~03`, `C-13`: provided integer directed `D/U`의 syntax/source를 보존하되 complete preparation은 Phase 02 |
| Adapter | `P-12`: versioned adapter와 제한된 alias/coercion 방향은 유지하되 exact allowlist, unknown-field policy와 public schema는 아직 미확정 |

### 3.2 Phase 01 불변조건

1. Core canonical/normalized type은 Jackson, HTTP, cloud event, provider URI와 SDK type을 참조하지 않는다.
2. Adapter는 명시된 schema/version만 사용하며 `latest`, 유사 이름, field 추측과 silent fallback을 하지 않는다.
3. External ID는 schema가 명시한 decoding 뒤 case-sensitive opaque identity다. 승인되지 않은 trim, case-fold와 Unicode normalization을 하지 않는다.
4. Duplicate identity와 duplicate directed travel key는 canonical sorting이나 map overwrite 전에 거부한다.
5. Approved alias가 함께 존재하면 canonical value의 exact equality를 요구하고 provenance에 둘 다 기록한다.
6. Normalization은 exact decimal/integer string에서 시작하며 `double`을 중간 표현으로 쓰지 않는다.
7. 모든 scale, `qty` 곱, item/request/limit/service 합과 time offset 계산은 checked arithmetic이다.
8. Missing/invalid/infeasible를 numeric sentinel, `Long.MAX_VALUE`, plan end, 0 또는 999로 일반 치환하지 않는다.
9. 999 CBM은 adapter가 volume dimension 미사용을 명시한 경우에만 적용하는 유한 capacity이며 provenance에 남긴다.
10. Canonical collection order는 semantic set과 ordered sequence를 구분한다. Set-like collection만 versioned canonical comparator로 정렬한다.
11. 같은 normalized meaning과 policy는 같은 semantic fingerprint를 만든다. Raw byte digest와 alias provenance는 별도 identity로 보존한다.
12. Failure는 partial normalized artifact를 내지 않는다. Complete, deterministically ordered typed problems 또는 sealed success 중 하나만 반환한다.
13. Phase 01 output에는 dense solver ID, prepared matrix, **bound profile**, route, bank, score, objective와 solver state가 없다. Exact customer/profile/version, requested preset 또는 omission, mandatory와 승인된 typed extension 선언은 Phase 04가 raw input을 다시 읽지 않도록 보존하되 이 Phase에서 bind하거나 default를 채우지 않는다.

## 4. Entry gate와 현재 evidence 확인

### 4.1 필수 entry evidence

| 확인 항목 | 요구 evidence | 현재 checkout 판정 |
|---|---|---|
| Phase 00 상세/review | [actual Phase 00](phase-00-build-architecture-skeleton.md), [actual Phase 00 review](../reviews/phase-00-review.md) | 문서/review 있음; review `PASS_WITH_RESIDUAL_BLOCKERS`, implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `PLANNED/NOT_ACCEPTED` — `BLOCKED` |
| Build skeleton | `E-P00-BUILD` accepted, root reactor와 exact build command | evidence 없음 — `BLOCKED` |
| Dependency guard | `E-P00-ARCH` accepted, module/package DAG와 forbidden dependency | evidence 없음 — `BLOCKED` |
| Legacy baseline | `E-P00-LEGACY` accepted | evidence 없음 — `BLOCKED` |
| Phase 01 source contract | [actual Phase 01 review](../reviews/phase-01-review.md) verdict | `ACCEPTED_WITH_APPLIED_CORRECTIONS` — 문서 gate 충족; 구현/acceptance evidence 아님 |

Entry gate 확인 명령 후보는 다음과 같다. Phase 00가 Maven wrapper나 다른 module path를 승인하면 그 명령이 우선한다.

```bash
git status --short --untracked-files=all
find docs/implementation -maxdepth 3 -type f -print | sort
find . -type f -name pom.xml -not -path './target/*' -not -path './node_modules/*' -print | sort
mvn -version
mvn verify
```

`mvn verify` 성공만으로 Phase 00 accepted가 되지 않는다. Evidence key, source commit/digest와 independent review verdict를 함께 확인해야 한다.

### 4.2 2026-07-28 read-only current inventory

| 항목 | 확인된 실제 상태 | Phase 01 해석 |
|---|---|---|
| Git | branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb` | 문서 baseline identity일 뿐 Phase evidence가 아님 |
| Documentation inventory | Phase 00~14 상세 문서 15/15, Phase 00~14 review 15/15가 실제 존재 | 문서 세트 작성/review 완료와 implementation/evidence/phase acceptance를 분리 |
| Working tree | `docs/implementation/*`가 미추적이며 Phase 00 implementation/evidence/acceptance는 `NOT_STARTED`/`NOT_PRODUCED`/`PLANNED·NOT_ACCEPTED` | 기존 미추적 문서는 사용자 작업으로 보존; 파일/review verdict를 accepted implementation evidence로 사용하지 않음 |
| Maven | Root [pom.xml](../../../pom.xml) 하나인 `com.ronext:ro-next:0.1.0-SNAPSHOT` 단일 project | Target reactor/core/adapter/test-fixture module 없음 |
| Toolchain | Java `25.0.3` Corretto, Maven `3.9.14`; POM release 25/Enforcer 설정 | Tool version은 맞지만 Phase 00 accepted evidence는 없음 |
| Dependencies | Google Workflow Executions, GCS, Jackson, JUnit가 root classpath에 직접 존재 | Anti-corruption/core/provider 격리가 아직 없음 |
| Source | Main 6개, test 1개, 모두 `com.ronext.optimizer` | Canonical input/normalization type과 test가 없음 |
| Placeholder | [AlnsBatchEngine](../../../src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java)이 synthetic objective `Map` 생성 | Input/domain/solver evidence가 아님 |
| Controller input | HTTP controller가 raw `Map<String,Object>`를 사용 | Characterization 대상이며 canonical/public API가 아님 |
| Phase artifacts | `CanonicalBusinessInput`, `NormalizedInputArtifact`, typed input errors 없음 | 모든 Phase 01 functional test는 미래 red에서 시작 |

현재 source에 존재하지 않는 module, type, test, build success와 evidence를 아래 계획이 이미 구현된 사실처럼 읽어서는 안 된다.

## 5. 변경 대상 module, package와 file tree

Phase 00가 아래 이름을 그대로 승인한다는 가정의 **proposed tree**다. 이름이 바뀌어도 dependency와 책임은 유지한다.

```text
rpdptw/core/
├── src/main/java/com/ronext/rpdptw/input/
│   ├── SchemaIdentity.java
│   ├── AdapterIdentity.java
│   ├── CanonicalBusinessInput.java
│   ├── CanonicalPlanEnvelope.java
│   ├── CanonicalRequestInput.java
│   ├── CanonicalVehicleInput.java
│   ├── CanonicalLocationInput.java
│   ├── CanonicalTravelInput.java
│   └── InputProvenance.java
├── src/main/java/com/ronext/rpdptw/normalization/
│   ├── CanonicalInputNormalizer.java
│   ├── NormalizationPolicySnapshot.java
│   ├── NormalizedInputArtifact.java
│   ├── NormalizedPlanEnvelope.java
│   ├── NormalizedRequestInput.java
│   ├── NormalizedVehicleInput.java
│   ├── NormalizedWindow.java
│   ├── NormalizationResult.java
│   ├── InputProblem.java
│   └── CanonicalFingerprint.java
└── src/test/java/com/ronext/rpdptw/
    ├── normalization/FixedPointNormalizerTest.java
    ├── normalization/TimeNormalizerTest.java
    ├── normalization/ServiceTimeNormalizerTest.java
    ├── normalization/RouteResourceNormalizerTest.java
    ├── normalization/CompatibilityNormalizerTest.java
    ├── normalization/TripPolicyNormalizerTest.java
    ├── normalization/CanonicalOrderingTest.java
    └── normalization/NormalizedInputArtifactTest.java

adapters/common/
├── src/main/java/com/ronext/rpdptw/adapter/input/
│   ├── VersionedInputAdapter.java
│   ├── ExternalInputDocument.java
│   ├── AdaptationResult.java
│   └── v1/
│       ├── V1ExternalSolveDto.java
│       ├── V1ExternalInputAdapter.java
│       └── V1AliasPolicy.java
└── src/test/java/com/ronext/rpdptw/adapter/input/
    ├── VersionedInputAdapterContractTest.java
    └── V1ExternalInputAdapterTest.java

build/test-fixtures/
└── src/test/java/com/ronext/rpdptw/fixture/
    ├── ExternalInputFixtureBuilder.java
    ├── CanonicalInputFixtureBuilder.java
    ├── NormalizationOracle.java
    └── Phase01FailureFixtures.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase01DependencyRulesTest.java
```

Phase 01은 기존 `com.ronext.optimizer` controller/engine을 새 canonical path로 연결하거나 삭제하지 않는다. Legacy endpoint migration은 Phase 08/14 범위다.

### 5.1 Dependency direction

```text
adapters/common input DTO/mapper
  → rpdptw-core/input
  → rpdptw-core/normalization

build/test-fixtures
  → adapter/common + core test APIs

Phase 02 domain/travel
  → Phase 01 NormalizedInputArtifact
```

금지 edge:

```text
core → Jackson/HTTP/cloud/provider adapter
normalization → travel preparation/solver/application/verification
adapter → solver/profile/objective
Phase 01 → PreparedTravel/ProblemInstance builder implementation
```

## 6. 입력·출력 artifact와 contract

### 6.1 입력

| 입력 | 필수 내용 | 권위 |
|---|---|---|
| `ExternalInputDocument` (proposed) | exact bytes, declared schema ID/version, declared adapter version, media type | Inbound adapter가 전달; raw digest는 Phase 01이 계산/검증 |
| Adapter policy | approved alias/coercion/unknown-field allowlist와 version | Product·API·Data owner 승인 필요 |
| `NormalizationPolicySnapshot` (proposed) | numeric/time/service/identity/order policy ID/version | Canonical decisions에서 생성, solve 중 변경 금지 |
| Test fixture | raw bytes + expected canonical meaning 또는 expected typed failures | Test-only; public schema authority가 아님 |

Object reference resolve, tenant authorization과 bytes fetch는 Phase 08/09 adapter 책임이다. Phase 01 경계는 이미 획득한 bytes와 declared source identity를 받는다. Fixture path나 S3/GCS URI를 core identity로 사용하지 않는다.

### 6.2 산출물

```text
AdaptedCanonicalInput
  rawInputDigest
  schemaIdentity
  adapterIdentity
  CanonicalBusinessInput
  alias/coercion/unknown-field provenance

NormalizedInputArtifact
  normalized plan identity/depot/requests/vehicles/locations
  exact customer/profile/version + requested objective preset or omission
  optional mandatory declarations + approved typed extension inputs
  normalized sparse provided travel declarations
  normalized numeric/time/service/compatibility/trip/wait/resource policies
  rawInputDigest + adapter/schema identity
  provenance
  semanticFingerprint
  envelopeFingerprint
```

`NormalizedInputArtifact`는 Phase 02에 다음을 제공한다.

- Exact external identity와 validated reference graph
- Normalized integer quantities and seconds
- Delivery-only/real-pair service pattern
- Exact customer/profile/version identity, requested objective preset 또는 명시적 omission
- Optional mandatory declaration과 승인된 typed extension input; binding/evaluation은 아직 수행하지 않음
- Location/coordinate input and sparse directed provided travel values
- Vehicle speed source, including “missing and eligible for 45 km/h default”와 “present valid”
- Terminal/trip/`waitInDepot`/vehicle·global route-resource declarations와 typed absence
- Policy IDs/versions, source provenance와 fingerprints

다음은 제공하지 않는다.

- Dense IDs/array indices
- Generated distance/time
- Complete matrix or `PreparedTravel`
- `ProblemInstance`
- Bound profile, routes, bank, score/objective

### 6.3 Public와 internal contract

| Contract | 상태 | 규칙 |
|---|---|---|
| External JSON/wire schema | `OPEN` | 별도 Product/API/Data 승인 전 fixture나 DTO를 public 약속으로 인용 금지 |
| Java adapter SPI | `PROPOSED INTERNAL` | schema/version dispatch와 typed failure 의미만 고정 |
| Canonical input records | `PROPOSED MODULE CONTRACT` | Jackson/cloud annotation 금지, Phase 02가 소비; plan/profile/preset/mandatory/typed extension 선언을 누락하지 않음 |
| Error code/wire mapping | Java code는 `PROPOSED`, wire status는 `OPEN` | Internal typed error와 external HTTP code를 분리 |
| Fingerprint encoding/algorithm | `PROPOSED INTERNAL` | algorithm/version을 identity에 포함; 변경은 migration/replay review 필요 |

### 6.4 Identity와 canonical ordering

Identity는 세 층으로 분리한다.

1. `RawInputDigest`: exact bytes의 digest. 공백·field order가 달라도 bytes가 다르면 다르다.
2. `SemanticFingerprint`: normalized meaning, schema/adapter contract와 normalization policy의 canonical encoding. Plan identity, exact customer/profile/version, requested preset 또는 omission, mandatory/typed extension, wait/resource declaration을 포함한다. Set-like input 순서만 다른 경우 같다.
3. `EnvelopeFingerprint`: raw digest, semantic fingerprint, 적용 alias/coercion/default provenance를 결합한다.

Proposed canonical comparator는 normalized UTF-8 byte sequence의 unsigned lexicographic order다. Phase 01 review에서 승인하거나 versioned 대안을 선택해야 한다.

| Collection | Canonical treatment |
|---|---|
| Requests/orders 통합 집합 | exact external request identity로 sort; 중복은 sort 전 거부 |
| Vehicles | exact external vehicle identity로 sort |
| Physical locations | exact external location identity로 sort |
| Capabilities와 allowed size set | exact case-sensitive code로 unique+sort; input duplicate는 거부 |
| Windows | owner identity, normalized start, normalized end, source ordinal 순으로 stable sort; raw exact duplicate는 거부 |
| Sparse directed travel | `(fromLocationId,toLocationId)`로 sort; 동일 key duplicate는 값이 같아도 거부 |
| Items | 서비스·수치 계산은 각 item을 먼저 normalize한 뒤 checked sum; item identity가 있으면 identity sort, 없으면 source ordinal을 provenance에 보존 |
| Pickup/delivery visits | 순서가 의미이므로 sort하지 않음 |

정렬로 duplicate나 alias 충돌을 숨기지 않는다. Approved alias 두 개가 같은 canonical value를 낼 때만 성공하며 semantic fingerprint는 같을 수 있지만 raw/envelope fingerprint와 provenance는 다를 수 있다.

### 6.5 Lifecycle와 state transition

```text
RECEIVED
→ DIGESTED
→ SCHEMA_SELECTED
→ ADAPTED
→ REFERENCES_VALIDATED
→ VALUES_NORMALIZED
→ CANONICAL_ORDERED
→ SEALED

any pre-seal state
→ REJECTED(ordered InputProblem list)
```

`SEALED`와 `REJECTED`만 외부 관찰 결과다. Partial canonical object, partially normalized list와 mutable builder를 Phase 02에 전달하지 않는다. Failure/exception 시 input bytes와 이미 존재하는 immutable artifact는 바꾸지 않고 draft를 폐기한다.

## 7. Java 수준 proposed design

### 7.1 Adapter와 normalization boundary

```java
public interface VersionedInputAdapter {
    boolean supports(SchemaIdentity schema, AdapterIdentity adapter);

    AdaptationResult adapt(ExternalInputDocument document);
}

public sealed interface AdaptationResult {
    record Accepted(AdaptedCanonicalInput input) implements AdaptationResult {}
    record Rejected(InputRejectionReport report) implements AdaptationResult {}
}

public interface CanonicalInputNormalizer {
    NormalizationResult normalize(
        AdaptedCanonicalInput input,
        NormalizationPolicySnapshot policy
    );
}

public sealed interface NormalizationResult {
    record Accepted(NormalizedInputArtifact artifact)
        implements NormalizationResult {}
    record Rejected(InputRejectionReport report)
        implements NormalizationResult {}
}
```

이 interface는 Java module 내부 계약 후보이지 public submission API가 아니다.

### 7.2 Canonical request와 service pattern

```java
public record CanonicalRequestInput(
    ExternalRequestId id,
    PickupInput pickup,
    CanonicalServiceInput delivery,
    List<CanonicalItemInput> items,
    CanonicalCompatibilityInput compatibility,
    Optional<CanonicalDeadlineInput> completionDeadline,
    Optional<Boolean> mandatoryDeclaration,
    Optional<ApprovedTypedExtensionInput> extensionInput
) {}

public sealed interface PickupInput {
    record LogicalInitialLoad() implements PickupInput {}
    record PhysicalService(CanonicalServiceInput service) implements PickupInput {}
}

public enum ServicePattern {
    DELIVERY_ONLY,
    REAL_PICKUP_DELIVERY
}
```

Delivery-only의 logical pickup은 location, travel, stop 또는 depot service node를 만들지 않는다. Real request는 pickup과 delivery 둘을 같은 `ExternalRequestId` 아래 묶는다. Dense node와 pair object 생성은 Phase 02다.

### 7.3 Numeric value

```java
public record MilliKilograms(long value) {}
public record MilliCubicMeters(long value) {}
public record Seconds(long value) {}
public record Meters(long value) {}

public interface FixedPointNormalizer {
    long floorNonNegativeToScale3(DecimalLexeme value);

    long multiplyChecked(long normalizedItemValue, int quantity);

    long addChecked(long left, long right);
}
```

Method 이름은 proposed다. 구현 의미는 다음 pseudo-code를 따라야 한다.

```text
normalizeItem(rawDecimal, qty):
  require rawDecimal is finite exact decimal and rawDecimal >= 0
  require qty is integer and qty > 0
  scaled = floor(rawDecimal * 1000) without binary floating conversion
  require scaled fits long
  return multiplyExact(scaled, qty)

normalizeRequest(items):
  total = 0
  for each item in canonical item order:
    total = addExact(total, normalizeItem(item.rawValue, item.qty))
  return total
```

비용·거리·시간 parser는 decimal point와 exponent가 있는 numeric lexeme를 integer value로 조용히 바꾸지 않는다. Exact lexical policy는 adapter schema version에 포함한다.

### 7.4 Time, window와 trip

```java
public record NormalizedPlanEnvelope(
    ExternalPlanId planIdentity,
    long planDurationSeconds,
    List<NormalizedWindow> depotWindows,
    NormalizedProfileSelectionInput profileSelection,
    DepotWaitPolicy depotWaitPolicy,
    NormalizedRouteResourceLimits globalRouteResourceLimits
) {}

public record NormalizedProfileSelectionInput(
    CustomerKey customerKey,
    ProfileKey profileKey,
    ProfileVersion profileVersion,
    Optional<ObjectivePresetKey> requestedPreset
) {}

public record NormalizedWindow(
    long startSecondInclusive,
    long endSecondExclusive,
    WindowSource source
) {
    // Raw inclusive close is represented by checked close+1,
    // then clipped to planEndExclusive.
}

public enum CustomerWindowPolicy {
    START_ONLY,
    COMPLETE_WITHIN_WINDOW
}

public enum WorkArcPolicy {
    FULL_ARC_WITHIN_ONE_WORK_WINDOW
}

public enum DepotWaitPolicy {
    WAIT_AT_CUSTOMER,
    MOVE_EARLY_WAIT_TO_DEPOT
}

public sealed interface TripPolicy {
    record OneWay() implements TripPolicy {}
    record SingleRoundTrip() implements TripPolicy {}
}

public record NormalizedRouteResourceLimits(
    Optional<StopCountLimit> maxStopCount,
    Optional<DriveTimeLimit> maxDriveTime,
    Optional<DriveDistanceLimit> maxDriveDistance
) {}
```

`NormalizedWindow`의 half-open 내부 표현은 raw close-inclusive 의미를 잃지 않도록 checked `close + 1`을 사용한다. `endSecondExclusive`는 plan duration을 넘지 않는다. Feasible service/event 계산은 Phase 03이 수행한다.

`requestedPreset`의 empty는 exact omission이며 Phase 01이 customer default로 대체하지 않는다. Optional mandatory와 typed extension도 입력의 present/absent를 보존한다. Extension은 승인된 versioned type만 허용하며 raw customer field map을 통과시키지 않는다. Vehicle별 route-resource limit도 같은 typed present/absent 값으로 정규화한다. Vehicle/global 한도를 실제 `min`으로 적용하고 full-arc restart, `waitInDepot` departure를 계산하는 책임은 Phase 03이 소유한다.

### 7.5 Compatibility와 ownership

```java
public record NormalizedCompatibilityInput(
    AllowedVehicleSizes allowedSizes,
    SortedSet<CapabilityCode> requiredCapabilities,
    ZoneCode zone
) {}

public sealed interface AllowedVehicleSizes {
    record All() implements AllowedVehicleSizes {}
    record Exact(SortedSet<VehicleSizeCode> values)
        implements AllowedVehicleSizes {}
}

public enum VehicleOwnership {
    DIRECT,
    LEASE
}
```

Free-form code는 exact case-sensitive string이다. `Feature`의 숫자나 배열 순서에서 차량 크기를 추론하지 않는다. Capability subset과 route zone feasibility의 실행은 후속 Phase가 수행하지만 Phase 01은 모호한 shape, duplicate code와 pickup/delivery compatibility fact를 엄격히 정규화한다.

### 7.6 Typed failure

```java
public sealed interface InputProblem {
    InputProblemCode code();
    InputPath path();

    record Schema(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Identity(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Reference(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Numeric(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Temporal(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Compatibility(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Trip(InputProblemCode code, InputPath path)
        implements InputProblem {}
}
```

Proposed internal codes:

```text
UNSUPPORTED_SCHEMA_VERSION
AMBIGUOUS_ALIAS
UNKNOWN_FIELD_REJECTED
MISSING_REQUIRED_FIELD
DUPLICATE_IDENTITY
DUPLICATE_TRAVEL_KEY
DANGLING_REFERENCE
CANONICAL_ORDER_COLLISION
INVALID_NUMERIC_SYNTAX
FRACTION_NOT_ALLOWED
NEGATIVE_VALUE
NON_POSITIVE_QUANTITY
ARITHMETIC_OVERFLOW
INVALID_DATETIME
TIMEZONE_OR_OFFSET_NOT_ALLOWED
INVALID_PLAN_RANGE
AMBIGUOUS_WINDOW
ORDER_LEVEL_TASK_TIME_NOT_ALLOWED
UNAPPROVED_EXTENSION_INPUT
INVALID_VEHICLE_FEATURE
INVALID_FEATURE_LIST
INVALID_CAPABILITY
INVALID_ZONE
INVALID_OWNERSHIP
INVALID_WAIT_POLICY
INVALID_ROUTE_RESOURCE_LIMIT
UNSUPPORTED_TRIP_POLICY
UNSUPPORTED_ROTATION
```

Unknown-field policy 자체는 `P-12` 아래 `OPEN`이다. `UNKNOWN_FIELD_REJECTED`는 explicit `REJECT` adapter policy일 때만 사용하며 hidden default가 아니다. Problem은 `InputPath → code → stable evidence digest` 순으로 deterministic sort하고 raw PII/value 전체를 message나 log에 넣지 않는다.

### 7.7 End-to-end pseudo-code

```text
normalize(document, declaredSchema, policy):
  rawDigest = digestExactBytes(document.bytes)
  adapter = registry.resolveExact(declaredSchema, declaredAdapterVersion)
      or reject UNSUPPORTED_SCHEMA_VERSION

  dto = adapter.parseStrictly(document.bytes)
  canonicalDraft = adapter.mapApprovedFields(dto)
  problems += detectUnknownFieldsPerExplicitPolicy(dto)
  problems += detectAliasConflicts(canonicalDraft)
  problems += detectDuplicatesBeforeMapsOrSorting(canonicalDraft)
  problems += validateAllReferences(canonicalDraft)

  if problems not empty:
    return Rejected(sortDeterministically(problems))

  normalizedDraft = normalizeNumericTimeServiceCompatibilityTrip(canonicalDraft)
  problems += checkedArithmeticAndBoundaryProblems(normalizedDraft)

  if problems not empty:
    return Rejected(sortDeterministically(problems))

  ordered = canonicalOrderOnlySetLikeCollections(normalizedDraft)
  semanticFingerprint = fingerprint(
      policy + schema + adapter
      + plan/profile/preset-or-omission
      + mandatory/typed-extension declarations
      + trip/wait/resource declarations
      + ordered)
  envelopeFingerprint =
      fingerprint(rawDigest + semanticFingerprint + provenance)

  return Accepted(sealImmutable(
      rawDigest, ordered, policy, provenance,
      semanticFingerprint, envelopeFingerprint))
```

## 8. 순서 있는 work packages

### WP-01.0 — Entry evidence와 contract freeze

| 항목 | 내용 |
|---|---|
| 사전조건 | 이 문서가 review 가능 상태 |
| 수정 대상 | 없음. Read-only evidence 확인 |
| 구체 작업 | Phase 00 detail/review와 `E-P00-BUILD/ARCH/LEGACY` digest·verdict 확인; accepted module/package/build command를 이 Phase 실행 record에 고정 |
| 검증 명령 | §4.1 명령과 Phase 00 evidence verifier |
| 테스트 | Phase 00 evidence manifest의 source commit/digest, required key와 accepted review verdict 무결성 검사 |
| 기대 결과 | Exact Phase 00 source commit, toolchain, module DAG와 rollback point가 확인됨 |
| 실패/rollback | 하나라도 없거나 다른 digest면 Phase 01 source edit 금지, 상태 `BLOCKED` 유지 |
| handoff | WP-01.1~01.7이 사용할 accepted build/module baseline |

### WP-01.1 — Versioned anti-corruption adapter

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.0 완료; exact supported schema/version과 alias allowlist 승인 |
| 수정 대상 | `adapters/common/.../adapter/input`, `rpdptw-core/.../input` |
| 구체 작업 | External DTO 분리, exact adapter registry, raw digest, alias equality/conflict, unknown-field policy 주입, plan/customer/profile/version와 preset omission, canonical request/order/mandatory/approved-extension mapping, provenance 작성 |
| 검증 명령 | `mvn -pl adapters/common -am -Dtest=VersionedInputAdapterContractTest,V1ExternalInputAdapterTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `acceptsSupportedSchemaAndRecordsRawDigest`, `rejectsUnknownSchemaVersionWithoutFallback`, `acceptsEqualReqDateDueDateAliasAndRecordsBoth`, `rejectsConflictingReqDateDueDate`, `preservesExactProfileSelectionAndPresetOmission`, `rejectsUnapprovedRawCustomerExtension`, `unknownFieldBehaviorComesFromExplicitVersionedPolicy` |
| 기대 결과 | 지원 version만 deterministic canonical input 또는 ordered typed problems 반환 |
| 실패/rollback | 새 adapter registration과 code를 revert 가능한 WP commit으로 폐기; 기존 placeholder 연결 변경 없음 |
| handoff | `AdaptedCanonicalInput` fixture set |

### WP-01.2 — Identity, reference와 canonical order

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.1 accepted canonical DTO |
| 수정 대상 | core `input/normalization`, fixture builder |
| 구체 작업 | Opaque case-sensitive IDs, duplicate-before-map, order+request namespace collision, location/depot/vehicle/travel reference graph, set/sequence order 분리, error total order 구현 |
| 검증 명령 | `mvn -pl rpdptw/core -am -Dtest=CanonicalOrderingTest,NormalizedInputArtifactTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `rejectsDuplicateRequestBeforeCanonicalSort`, `rejectsDuplicateTravelKeyEvenWhenValuesMatch`, `rejectsDanglingLocationReference`, `setPermutationKeepsSemanticFingerprint`, `visitOrderChangesSemanticFingerprint`, `failureOrderIsInputPermutationIndependent` |
| 기대 결과 | Map overwrite나 input order에 의존하지 않는 동일 artifact/error |
| 실패/rollback | Draft artifact를 반환하지 않고 WP 변경만 되돌림 |
| handoff | Identity/reference-valid canonical draft |

### WP-01.3 — Numeric와 unit normalization

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.2 valid identity graph |
| 수정 대상 | core numeric value/normalizer, numeric fixtures/oracle |
| 구체 작업 | Exact decimal parser, scale-3 `FLOOR`, item-first × qty, integer-only cost/distance/time, finite/nonnegative/positive checks, scale/multiply/sum overflow, 999 CBM provenance |
| 검증 명령 | `mvn -pl rpdptw/core -am -Dtest=FixedPointNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `floorsWeightAtThirdDecimal`, `normalizesEachItemBeforeQuantityMultiplication`, `rejectsDecimalDistanceEvenWhenMathematicallyIntegral`, `rejectsExponentForIntegerOnlyFieldPerAdapterPolicy`, `detectsScaleOverflow`, `detectsQuantityMultiplicationOverflow`, `detectsRequestSumOverflow`, `appliesFinite999CbmOnlyWhenVolumeUnusedIsExplicit` |
| 기대 결과 | Hand oracle와 exact `long` 값/code/path가 일치 |
| 실패/rollback | Saturation/sentinel 없이 전체 normalization reject; accepted prior artifact 불변 |
| handoff | `E-P01-NUMERIC` candidate report |

### WP-01.4 — Time, service와 trip normalization

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.3 checked seconds/service primitives |
| 수정 대상 | core time/service/trip normalizer와 tests |
| 구체 작업 | Strict local datetime, planning-origin seconds, plan range, inclusive close representation, repeating/overnight expansion/clip, service-time sum, deadline alias, trip/wait policy와 vehicle/global route-resource present/absent 선언 |
| 검증 명령 | `mvn -pl rpdptw/core -am -Dtest=TimeNormalizerTest,ServiceTimeNormalizerTest,TripPolicyNormalizerTest,RouteResourceNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `convertsExactLocalDateTimeToPlanOriginSeconds`, `rejectsOffsetOrZone`, `keepsPlanEndExclusiveAndCloseInclusive`, `expandsOvernightWindowOncePerPlanDate`, `rejectsEqualOpenCloseWithoutSchemaMeaning`, `preservesFullArcRestartPolicyAndExactHandoffOracle`, `checksServiceDurationItemTimeQuantitySum`, `rejectsOrderLevelTaskTime`, `onewayIgnoresRotationButRecordsRawValue`, `normalizesExplicitDepotWaitPolicyWithoutFallback`, `rejectsUnknownDepotWaitPolicy`, `rejectsNonOnewayRotation`, `preservesMissingRouteLimitsAsTypedAbsence`, `rejectsNegativeOrFractionalRouteResourceLimit` |
| 기대 결과 | Normalized interval/policy와 exact expected seconds 또는 typed rejection 일치 |
| 실패/rollback | Partial window expansion을 폐기; plan/time sentinel 없음 |
| handoff | `E-P01-TIME` candidate report와 Phase 03 full-arc fixture |

### WP-01.5 — Size, capability, zone와 ownership

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.2 identity/reference rules |
| 수정 대상 | core compatibility records/normalizer, compatibility oracle |
| 구체 작업 | Vehicle concrete feature, request exact `ALL`/set, legacy list alias, capability sets, zone `ALL`, pickup/delivery intersections/conflicts, `DIRECT/LEASE` |
| 검증 명령 | `mvn -pl rpdptw/core -am -Dtest=CompatibilityNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `keepsFreeFormSizeCodeCaseSensitive`, `acceptsOnlyExactAllAlternative`, `rejectsMixedAllAndConcreteCode`, `rejectsVehicleAllOrBlank`, `requiresLegacyAndNewFeatureListsToMatchExactly`, `computesRealPairSizeIntersectionWithoutGuessing`, `preservesNoEligibleVehicleAsValidNormalizedFact`, `normalizesMissingZoneToAll`, `preservesConflictingConcretePickupDeliveryZonesAsStaticUnassignabilityFact`, `normalizesOwnershipStrictly` |
| 기대 결과 | Static facts가 exact하고 compatible vehicle 0개를 input corruption으로 오인하지 않음 |
| 실패/rollback | No fallback registry/톤수 추론; draft 폐기 |
| handoff | `E-P01-COMPAT` candidate report |

### WP-01.6 — Artifact sealing, typed error와 architecture guard

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.1~01.5의 모든 normalizer |
| 수정 대상 | artifact/fingerprint/result types, architecture rule |
| 구체 작업 | Defensive immutable copy, semantic/envelope fingerprint, deterministic error aggregation, raw input/PII-safe evidence와 log/report redaction, forbidden dependency와 forbidden output type 검사 |
| 검증 명령 | `mvn -pl rpdptw/core,adapters/common,build/architecture-rules -am test` |
| 테스트 | `artifactDefensivelyCopiesAllCollections`, `sameMeaningAndPolicyHasSameSemanticFingerprint`, `profilePresetMandatoryResourceMeaningChangesSemanticFingerprint`, `aliasProvenanceChangesEnvelopeNotMeaning`, `rejectionNeverExposesPartialArtifact`, `rejectionEvidenceRedactsRawValuesAndInputBytes`, `phase01HasNoSolverTravelPreparationOrProviderDependency` |
| 기대 결과 | Sealed success 또는 ordered rejection만 존재하고 forbidden edge 0 |
| 실패/rollback | Fingerprint version 변화는 같은 ID overwrite 금지; last accepted WP artifact로 rollback |
| handoff | `E-P01-ERROR` candidate report, complete Phase 01 artifact candidate |

### WP-01.7 — Full evidence, review와 Phase 02 handoff

| 항목 | 내용 |
|---|---|
| 사전조건 | 모든 WP green, no skipped required test |
| 수정 대상 | Evidence bundle only; scheduler/progress는 총괄 owner가 별도 갱신 |
| 구체 작업 | Exact commands/toolchain/test counts/failures/fixtures/fingerprints/limitations/rollback을 묶고 independent review 요청 |
| 검증 명령 | `mvn -pl rpdptw/core,adapters/common,build/architecture-rules -am verify`<br>`mvn verify` |
| 테스트 | §9.2의 모든 exact class/method, forbidden-dependency suite, required test skipped/failed 수 0 확인 |
| 기대 결과 | `E-P01-NUMERIC/TIME/COMPAT/ERROR` 모두 digest-protected, review verdict accepted |
| 실패/rollback | Phase 상태를 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING`에 유지; Phase 02 implementation 시작 금지 |
| handoff | [Phase 02](phase-02-prepared-travel-immutable-problem.md)가 소비할 exact `NormalizedInputArtifact` schema/version/fingerprint와 negative fixture catalog |

## 9. 테스트 작성 명세

### 9.1 Red → green 순서

현재 target module/type/test가 없으므로 아래 functional test를 먼저 추가하면 **test compilation 또는 missing class로 실패하는 것이 예상되는 미래 red**다. 이 실패는 현재 결함 evidence나 Phase completion이 아니다.

1. Adapter contract red: version dispatch, alias, unknown policy와 raw digest.
2. Identity/order red: duplicate/reference/order/fingerprint.
3. Numeric red: hand boundary, item-first, overflow.
4. Time/service/trip red: exact seconds, interval, service sum와 unsupported rotation.
5. Compatibility red: feature/capability/zone/ownership.
6. Artifact/error red: deep immutability, typed failures, fingerprint.
7. Architecture red: forbidden dependency/output type.
8. Minimal implementation으로 각 suite를 green으로 만들고 root verify를 실행한다.

Phase 03의 actual full-arc restart test와 Phase 02의 generated travel test는 Phase 01에서 green으로 만들지 않는다. Phase 01은 각각 `WorkArcPolicy.FULL_ARC_WITHIN_ONE_WORK_WINDOW`와 normalized travel-source fixture만 handoff한다.

### 9.2 Exact test class/method와 oracle

| Test class | Exact methods | Fixture/builder | Oracle | Layer |
|---|---|---|---|---|
| `VersionedInputAdapterContractTest` | `acceptsSupportedSchemaAndRecordsRawDigest`; `rejectsUnknownSchemaVersionWithoutFallback`; `unknownFieldBehaviorComesFromExplicitVersionedPolicy` | Raw JSON bytes builder | Independent SHA-256 + expected code/path | Adapter contract |
| `V1ExternalInputAdapterTest` | `acceptsEqualReqDateDueDateAliasAndRecordsBoth`; `rejectsConflictingReqDateDueDate`; `rejectsOrderLevelTaskTime`; `preservesExactProfileSelectionAndPresetOmission`; `rejectsUnapprovedRawCustomerExtension` | `ExternalInputFixtureBuilder` | Hand canonical DTO | Adapter unit |
| `CanonicalOrderingTest` | `rejectsDuplicateRequestBeforeCanonicalSort`; `setPermutationKeepsSemanticFingerprint`; `visitOrderChangesSemanticFingerprint`; `failureOrderIsInputPermutationIndependent` | Permutation generator with fixed seed | UTF-8 comparator reference, sorted expected tuple list | Property/oracle |
| `FixedPointNormalizerTest` | `floorsWeightAtThirdDecimal`; `normalizesEachItemBeforeQuantityMultiplication`; `rejectsDecimalDistanceEvenWhenMathematicallyIntegral`; overflow methods | Boundary table | `BigInteger`/decimal-string hand oracle, never production normalizer | Value/boundary |
| `TimeNormalizerTest` | `convertsExactLocalDateTimeToPlanOriginSeconds`; `keepsPlanEndExclusiveAndCloseInclusive`; `expandsOvernightWindowOncePerPlanDate`; `rejectsEqualOpenCloseWithoutSchemaMeaning`; `preservesFullArcRestartPolicyAndExactHandoffOracle` | Fixed 3-day plan + full-arc handoff fixture | Explicit expected second intervals and restart tuple | Value/oracle |
| `ServiceTimeNormalizerTest` | `checksServiceDurationItemTimeQuantitySum`; `detectsServiceTimeOverflow`; `rejectsOrderLevelTaskTime` | Item service builder | `Math.addExact/multiplyExact` reference | Value/boundary |
| `TripPolicyNormalizerTest` | `onewayIgnoresRotationButRecordsRawValue`; `acceptsSingleRoundtripZeroRotation`; `rejectsNonOnewayRotation`; `normalizesExplicitDepotWaitPolicyWithoutFallback`; `rejectsUnknownDepotWaitPolicy` | Trip/wait table | Expected sealed type + provenance/code | Unit |
| `RouteResourceNormalizerTest` | `preservesMissingRouteLimitsAsTypedAbsence`; `normalizesVehicleAndGlobalRouteLimitsIndependently`; `rejectsNegativeOrFractionalRouteResourceLimit`; `detectsRouteResourceLimitOverflow` | Vehicle/global resource table | Hand typed-presence and checked-integer oracle | Value/boundary |
| `CompatibilityNormalizerTest` | §8 WP-01.5 methods | Compatibility matrix builder | Independent set/subset/intersection functions | Property |
| `NormalizedInputArtifactTest` | `artifactDefensivelyCopiesAllCollections`; `sameMeaningAndPolicyHasSameSemanticFingerprint`; `profilePresetMandatoryResourceMeaningChangesSemanticFingerprint`; `aliasProvenanceChangesEnvelopeNotMeaning`; `rejectionNeverExposesPartialArtifact`; `rejectionEvidenceRedactsRawValuesAndInputBytes` | `CanonicalInputFixtureBuilder` | Canonical encoding test implementation + canary-token absence oracle | Contract/security |
| `Phase01DependencyRulesTest` | `coreDoesNotDependOnJacksonCloudSolverOrVerification`; `normalizationDoesNotCreatePreparedTravelOrProblemInstance`; `adapterDoesNotDependOnSolver` | Compiled class graph | Explicit forbidden package/artifact list | Architecture |

### 9.3 Exact failure fixtures

| Fixture | 입력 | Expected oracle |
|---|---|---|
| `item-first-difference` | weight `0.0009`, qty `2` | normalized weight `0`; line-first `1`을 만들면 failure |
| `scale-boundary` | `1.2340`, `1.2349` | 둘 다 `1234` milli-unit |
| `integer-looking-decimal` | distance/time `1.0` | `FRACTION_NOT_ALLOWED`, exact path |
| `quantity-overflow` | normalized item near `Long.MAX_VALUE`, qty `2` | `ARITHMETIC_OVERFLOW`, no artifact |
| `sum-overflow` | individually valid items whose sum overflows | `ARITHMETIC_OVERFLOW`, deterministic item/request path |
| `duplicate-request` | delivery-only order와 real request가 같은 ID | `DUPLICATE_IDENTITY` before sort/map |
| `duplicate-arc` | same directed `(A,B)` twice with same value | `DUPLICATE_TRAVEL_KEY`; no silent dedupe |
| `alias-conflict` | normalized `reqDate != dueDate` | `AMBIGUOUS_ALIAS` |
| `ordering-permutation` | same set-like entities in different raw orders | same semantic fingerprint, different raw digest allowed |
| `meaningful-order-change` | pickup/delivery or ordered service sequence swapped | different meaning or structural rejection |
| `plan-boundary` | event at `planEnd` and service start at window close | plan-end event excluded; close start representable |
| `overnight` | `22:00→02:00` across 3-day plan | exact clipped intervals |
| `full-arc-handoff` | current work window remainder 5s, travel 6s, next work start 100s | typed policy와 expected restart departure `100`; Phase 01은 propagation하지 않음 |
| `route-resource-absence` | vehicle/global route limit omitted | typed absence; numeric sentinel/default 없음 |
| `route-resource-invalid` | negative/fractional `maxStopCnt`, `maxDriveTime` 또는 `maxDriveDist` | `INVALID_ROUTE_RESOURCE_LIMIT`, no artifact |
| `profile-preset-omission` | exact customer/profile/version + preset omitted | omission 보존; Phase 01 default 선택 금지 |
| `unapproved-extension` | 승인 type/version 없는 customer raw field | `UNAPPROVED_EXTENSION_INPUT` |
| `pii-redaction` | address/email canary가 포함된 invalid input | code/path/digest만 남고 raw canary/input bytes는 report/log에 없음 |
| `zone-conflict` | real pickup zone A, delivery zone B | normalized static unassignability fact; not malformed input |
| `feature-all-mix` | `["ALL","T1"]` | `INVALID_FEATURE_LIST` |
| `decimal-win-travel` | current-style decimal `D/U` | negative fixture; never official/compliant success |

### 9.4 합격 판정

- Required test의 failed/skipped 수가 0이다.
- `-Dsurefire.failIfNoSpecifiedTests=false`는 `-am`으로 함께 빌드되는 비소유 module의 false failure만 막는다. Target owner module에는 §9.2의 각 exact test class XML report와 선언 method 실행 record가 반드시 하나 이상 있어야 하며, report 누락·0 tests·이름 오타는 gate failure다.
- Oracle expected integer, interval, code, path와 actual이 exact match한다.
- Property test는 failing seed와 shrunk case를 evidence에 남긴다.
- Error list order와 semantic fingerprint가 input permutation, hash-map iteration과 locale에 의존하지 않는다.
- Test-only policy/value가 production default로 노출되지 않는다.
- Phase 02/03 future tests의 예상 red를 Phase 01 failure로 집계하지 않되, required Phase 01 suite에서 `@Disabled`로 숨기지도 않는다.
- Root architecture rule과 module verify가 함께 green이어야 한다.

## 10. Entry/exit gate, evidence bundle과 DoD

### 10.1 Exit gate

Phase 01은 다음을 모두 만족할 때만 `ACCEPTED` 후보가 된다.

1. Phase 00 accepted evidence와 exact source commit이 연결된다.
2. External DTO와 canonical/normalized core type이 분리된다.
3. Schema/version/alias/unknown-field behavior가 explicit하며 fallback이 없다.
4. Plan identity, customer/profile/version, requested preset 또는 omission, mandatory/approved-extension, wait와 vehicle/global route-resource 선언이 artifact와 semantic fingerprint에 보존되고 Phase 01이 profile/preset default를 bind하지 않는다.
5. Invalid, duplicate, ambiguous, overflow, rounding, reference, ordering, unapproved extension과 redaction failures가 typed oracle로 검출된다.
6. Numeric/time/service/compatibility/trip/wait/resource positive·negative·boundary test와 full-arc handoff oracle가 통과한다.
7. Immutable artifact와 three-level identity가 deterministic하다.
8. Core/normalization forbidden dependency와 Phase 02/solver scope 침범이 0이다.
9. `E-P01-NUMERIC`, `E-P01-TIME`, `E-P01-COMPAT`, `E-P01-ERROR`가 immutable digest를 가진다.
10. Independent Phase 01 review가 source→requirement→test→evidence를 확인한다.
11. Phase 02 handoff artifact, known limitations와 rollback point가 명시된다.

### 10.2 Evidence bundle

```text
phase-01-evidence/
├── manifest
│   ├── phase/document/review/source commit
│   ├── source fingerprints
│   ├── Java/Maven/toolchain
│   └── adapter/schema/policy identities
├── E-P01-NUMERIC/
│   ├── boundary-table
│   ├── item-first-oracle
│   └── overflow-report
├── E-P01-TIME/
│   ├── plan/window interval oracle
│   ├── service/trip/wait/resource report
│   ├── full-arc restart handoff oracle
│   └── future Phase 03 handoff fixture
├── E-P01-COMPAT/
│   ├── size/capability/zone property report
│   └── ownership/trip cases
├── E-P01-ERROR/
│   ├── adapter/alias/reference report
│   ├── profile/preset/mandatory/typed-extension declaration report
│   ├── duplicate/order/error-total-order report
│   ├── fingerprint/immutability/redaction report
│   └── architecture/dependency report
└── handoff/
    ├── NormalizedInputArtifact contract fingerprint
    ├── negative fixture catalog
    ├── known limitations
    └── rollback/source point
```

Console 한 줄, mutable `target/`, source/test file 존재 또는 current placeholder test pass만으로 bundle을 대체하지 않는다.

### 10.3 Definition of Done

- Entry와 exit gate가 모두 충족됨
- 이 상세 문서와 [actual Phase 01 review](../reviews/phase-01-review.md)가 approved 상태
- 모든 required command, test count와 exit code가 bundle에 기록됨
- Canonical/normalized artifact가 immutable하고 Phase 02가 raw input을 재해석할 필요가 없음
- Public API/schema/unknown-field policy의 open 상태를 숨기지 않음
- Plan/profile/preset/mandatory/typed-extension과 wait/resource omission을 hidden default로 해소하지 않음
- Win decimal fixture와 future propagation/travel logic을 성공 evidence로 사용하지 않음
- Rollback 후 Phase 00 accepted baseline과 legacy behavior가 손상되지 않음

## 11. Failure, rollback과 handoff

### 11.1 Failure 처리

| Failure | 판정 | 허용 행동 | 금지 |
|---|---|---|---|
| Unsupported schema/version | Pre-solve reject | Correct version의 새 input | Latest/nearest fallback |
| Alias conflict | Pre-solve reject | Source correction | 한쪽 우선 선택 |
| Duplicate ID/arc | Integrity reject | Source correction | Last-write-wins/dedupe |
| Dangling reference | Pre-solve reject | Source correction | Placeholder location/vehicle 생성 |
| Decimal integer-only value | Numeric reject | Compliant integer input | Round/truncate |
| Overflow | Numeric reject | Smaller compliant input/contract review | Saturate/wrap/sentinel |
| Time/zone ambiguity | Temporal reject | Backend-normalized exact local string | Timezone/DST 추정 |
| Missing/invalid profile selection 또는 unapproved extension | Contract reject/blocker | Versioned approved declaration | Customer/profile/preset 추정, raw field passthrough |
| Invalid wait/resource declaration | Pre-solve reject | Explicit supported wait policy와 typed valid limit/absence | Sentinel, clamp, implicit wait default |
| Unsupported rotation | `UNSUPPORTED_INPUT` | Single-trip compliant input | Silent multi-trip |
| Unknown-field policy 미승인 | Contract blocker | Test-only explicit policy로 adapter contract만 검증 | Production default 발명 |
| Unexpected implementation exception | Phase defect | Draft discard, evidence/fault investigation | Normal input error나 `UNASSIGNED`로 변환 |

### 11.2 Rollback

Phase 01 transformation은 side-effect-free여야 한다. Rejection/exception은 draft를 버리고 raw input과 기존 immutable artifact를 바꾸지 않는다. Source rollback은 WP별 accepted commit/digest로 수행하고, same artifact identity에 다른 bytes를 덮어쓰지 않는다. Legacy `com.ronext.optimizer` endpoint 연결은 이 Phase에서 바꾸지 않으므로 Phase 00 characterization point로 즉시 되돌릴 수 있어야 한다.

### 11.3 Phase 02 handoff

[Phase 02 — prepared travel/immutable problem](phase-02-prepared-travel-immutable-problem.md)은 다음만 소비한다. 해당 상세 문서는 실제 존재하지만 metadata상 `BLOCKED_BY_ENTRY_GATES`/`NOT_STARTED`이므로 파일 존재를 handoff acceptance로 간주하지 않는다.

```text
NormalizedInputArtifact
  CanonicalInputIdentity projection
    schemaVersion
    adapterVersion
    rawInputDigest
    semanticFingerprint
  external identity/reference graph
  normalized requests/service declarations/vehicles/locations
  plan identity + exact customer/profile/version
  requested objective preset or explicit omission
  optional mandatory + approved typed extension declarations
  integer units and normalized seconds
  sparse directed provided travel declarations
  coordinate/speed source facts
  trip/terminal/wait/resource policies and typed absence
  schema/adapter/normalization identities
  provenance + semantic/envelope fingerprints
```

여기서 service declaration은 아직 `SolverNodeId`가 아니다. Phase 02는 handoff fingerprint를 먼저 검증한 뒤 solver nodes와 dense IDs를 만들고 travel을 준비한다. Raw JSON alias, locale/timezone, decimal rounding과 ownership default를 다시 해석하면 handoff 위반이다.

## 12. Blocker, OPEN, GATED와 deferred

| 항목 | 상태 | Owner | 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00 evidence | `BLOCKER` | Phase 00/build owner + reviewer | Phase 01 source implementation 전체 | 이 문서와 fixture 설계 | `E-P00-BUILD/ARCH/LEGACY` accepted + exact module contract |
| Public wire schema/version inventory | `OPEN` | Product·API·Data owner | Production external adapter와 compatibility promise | Adapter SPI/test-only fixture | Versioned schema, field/unknown/alias policy 승인 |
| `P-12` exact coercion/alias/unknown policy | `OPEN PROPOSED` | Product·API·Data + Domain·Input | 해당 adapter green/production activation | Strict no-fallback abstract contract | Explicit allowlist/policy/version과 negative fixtures 승인 |
| Public customer/profile/preset/mandatory/extension field shape | `OPEN` | Product·API·Data + Profile owner | Production wire compatibility promise | Internal typed declaration contract와 test-only fixture | Versioned field/type/omission/authorization 계약 승인 |
| Canonical comparator/encoding name | `PROPOSED INTERNAL` | Domain·Architecture | Stable fingerprint compatibility | Test-only versioned comparator | Review/ADR와 replay/migration rule 승인 |
| Current Win fixture decimal `D/U` | `BLOCKER FOR OFFICIAL USE ONLY` | Input·Matrix + Benchmark | 해당 fixture의 compliant/official use | Negative rejection fixture | Integer matrix 제공 또는 명시적 contract/migration 승인 |
| `Q-BENCH-02` values | `OPEN — EXPERIMENT_REQUIRED` | Benchmark·Quality | Phase 14 official manifest | Phase 01 unaffected | Calibration + measured review + explicit approval |
| `C-17` route pool/MIP | `GATED TARGET` | Product·Algorithm·Architecture | Phase 13 | Phase 01 unaffected | Phase 06/07 baseline + scope/solver/license/fallback 승인 |
| `Q-VAR-01` | `DEFERRED` | Product·Domain·Algorithm | Optional variants | Current pair/trip facts only | Variant/fixture/core-impact approval |
| Multi-trip/rotation | `DEFERRED FEATURE` | Product·Domain·Algorithm | Non-single-trip input | Reject unsupported input | Trip/reset/depot/resource contract와 승인 |

Blocker가 발생해도 임시 default, 임의 `Q-*`, public field 또는 완료 evidence를 만들지 않는다.

## 13. Anti-pattern

- External JSON을 `ProblemInstance`, solver route 또는 `Map<String,Object>` core state로 직접 역직렬화
- Unknown schema/field/alias를 `latest`, 이름 유사도, first-non-null로 해석
- `double` parse 후 fixed-point 변환
- Item line 합계를 먼저 만든 뒤 한 번만 `FLOOR`
- Decimal cost/distance/time 또는 decimal `D/U`를 round/truncate
- Overflow를 clamp, saturation, wraparound, `Long.MAX_VALUE`로 숨김
- External ID trim/case-fold/Unicode 변환을 policy 없이 적용
- Duplicate를 `Map.put`, `distinct`, sort 또는 last-write-wins로 제거
- Unordered collection iteration이나 locale comparator를 fingerprint/error order에 사용
- Plan end와 window close를 같은 포함/제외 의미로 처리
- `openTime==closeTime`을 임의로 24시간 또는 빈 창으로 해석
- Delivery-only logical pickup에 가짜 travel/stop/service node 생성
- `Feature` 숫자에서 톤급 순서를 추론하거나 size/capability/zone을 한 문자열 축으로 합침
- Compatible vehicle 0개를 malformed input으로 거부하거나 final `PROVEN` outcome을 Phase 01에서 생성
- Phase 01에서 Great Circle, travel completion, dense ID, `ProblemInstance`, solver/verifier logic 구현
- Current HTTP/GCP placeholder 연결을 Phase 01 완료 evidence로 변경
- Future red/disabled test, test-only schema 또는 fixture 값을 production default로 승격
- Preset omission, optional mandatory, resource-limit absence 또는 `waitInDepot`를 hidden customer/default/sentinel 값으로 채움
- Raw customer extension, address/PII 또는 full input bytes를 generic map, error message, evidence/log attribute로 통과시킴

## 14. Source → requirement → test traceability

| Requirement | Source | Phase 01 realization | Exact test/evidence |
|---|---|---|---|
| Versioned anti-corruption boundary | Master §4.1~§4.3, §7.1; Integrated §5.1 | DTO/core 분리, exact registry, raw digest/provenance | `VersionedInputAdapterContractTest`; `E-P01-ERROR` |
| Plan/profile/preset/extension declaration | Domain §4.1~§4.2, Integrated §5.2~§5.3 | Plan identity, exact customer/profile/version, preset omission, mandatory와 approved typed extension 보존; binding/default 금지 | `V1ExternalInputAdapterTest`, `NormalizedInputArtifactTest`; `E-P01-ERROR` |
| Fixed-point/integer-only | `C-10~12`, `Q-NUM-01~03`, Master §7.2 | Exact decimal, scale-3 floor, item-first, checked arithmetic | `FixedPointNormalizerTest`; `E-P01-NUMERIC` |
| Plan/window/time axis | `Q-TIME-01~04`, Master §7.3, Domain §5.2 | Strict local string, origin seconds, half-open plan, inclusive close | `TimeNormalizerTest`; `E-P01-TIME` |
| Service/deadline | `Q-IN-01`, Domain §4.2 | Deadline alias equality, service checked sum, order taskTime reject | `ServiceTimeNormalizerTest`; `E-P01-TIME` |
| Trip/wait/resource declaration | `Q-IN-02`, `Q-BENCH-03`, Master §7.3 | Oneway/single-roundtrip sealed policy, raw rotation provenance, explicit depot-wait policy와 typed vehicle/global limit/absence | `TripPolicyNormalizerTest`, `RouteResourceNormalizerTest`; `E-P01-TIME` |
| Size/capability/zone | `C-05`, `Q-COMP-01~02`, Master §7.5 | Exact size set/ALL, capability subset facts, zone normalization | `CompatibilityNormalizerTest`; `E-P01-COMPAT` |
| Service pattern/pair input | `C-06~07`, `Q-REQ-01~02` | Logical initial load vs physical pickup, one request identity | Adapter/compatibility fixture; `E-P01-COMPAT` |
| Ownership | `Q-OBJ-03`, `Q-RES-01` | Strict `DIRECT/LEASE` normalization | `normalizesOwnershipStrictly`; `E-P01-COMPAT` |
| Identity/order/fingerprint | Master §4.5~§4.6, §13; Integrated §5.9 | Duplicate-before-sort, set/sequence distinction, three identities | `CanonicalOrderingTest`, `NormalizedInputArtifactTest`; `E-P01-ERROR` |
| Typed pre-solve rejection | Master §4.2, Domain §16 | Ordered sealed problems, no partial artifact | Failure fixture suite; `E-P01-ERROR` |
| Security/redaction | Integrated §20, Plan §9/§13 | Raw input/PII canary가 error/evidence/log에 없음 | `rejectionEvidenceRedactsRawValuesAndInputBytes`; `E-P01-ERROR` |
| Phase boundary | Master §15.3; Realization Plan Phase 01/02 | Normalized facts only; no prepared travel/problem/solver | `Phase01DependencyRulesTest`; handoff manifest |

## 15. Review checklist

- [ ] Source fingerprints와 cited sections가 현재 source와 일치한다.
- [ ] 사용자 고정 authority와 `26/1/1` 질문 상태를 적용한다.
- [ ] Phase 00 evidence 부재를 구현 완료로 오인하지 않는다.
- [ ] Public/internal/proposed/test-only/open 상태가 구분된다.
- [ ] Numeric, time, service, compatibility, trip 의미가 원문 명칭과 일치한다.
- [ ] Plan/profile/preset/mandatory/typed-extension과 wait/resource present/absent가 보존되고 hidden default/binding이 없다.
- [ ] Duplicate/ambiguous/overflow/rounding/ordering failure oracle가 exact하다.
- [ ] Reactor targeted command가 prerequisite module의 no-match를 허용하되 owner test report 누락/0 tests를 실패시킨다.
- [ ] Raw PII/input bytes가 error/evidence/log에 노출되지 않는 canary test가 있다.
- [ ] Canonical sorting이 의미 있는 sequence를 바꾸지 않는다.
- [ ] Phase 02가 소비할 artifact만 만들고 travel preparation/solver를 당기지 않는다.
- [ ] 모든 work package에 prerequisite, target, task, command/test, expected, failure/rollback, handoff가 있다.
- [ ] Future red가 current missing implementation 때문에 실패한다는 점이 명시된다.
- [ ] `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip과 public schema를 임의 해소하지 않는다.
- [ ] [Phase 00](phase-00-build-architecture-skeleton.md), [Phase 02](phase-02-prepared-travel-immutable-problem.md), [Master Plan](../master-realization-plan.md) 상대 링크가 canonical path를 가리킨다.
