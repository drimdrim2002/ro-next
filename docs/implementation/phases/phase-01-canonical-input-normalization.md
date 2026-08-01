# Phase 01 — 내부 표준 입력과 정규화

```yaml
document_status: REBASE_APPLIED_PENDING_REREVIEW
document_workflow_status: SEMANTIC_REBASE_APPLIED_PENDING_REREVIEW
phase_execution_status: BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE
implementation_status: NOT_STARTED
implementation_evidence_status: NOT_PRODUCED
phase_acceptance_status: NOT_ACCEPTED
phase: 01
canonical_slug: phase-01-canonical-input-normalization
filename_policy: KEEP_DISPLAY_SEPARATION
plan_version: 1.0
baseline_date: 2026-07-28
semantic_rebase_date: 2026-08-01
semantic_rebase_note: >
  APPROVED Master/Domain/Architecture + plan Phase 01 (D1) overlay +
  2026-08-01 interview Decisions 1–10. implementation status·win_poc·ACCEPTED 승격 없음.
  filename KEEP.
source_authority: APPROVED_MASTER_DOMAIN_ARCHITECTURE_PLUS_USER_PHASE_MAP
phase_c_note: path remap to docs/deprecated/*; semantic rebase recomputed live authority fingerprints
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_status: ALNS_FIRST_GATE_OVERLAY_APPLIED_DOCUMENTATION_ONLY
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
  - Phase 00 detailed plan and review exist
  - E-P00-BUILD accepted
  - E-P00-ARCH accepted
  - E-P00-LEGACY accepted
  - accepted module/package names and test command are recorded
planned_handoff:
  - phase-02-prepared-travel-immutable-problem.md
source_fingerprints_sha256:
  docs/deprecated/2026-07-30-design-interview-phase-a.md: cec96defaa6a3617a7504ea0aeb57e0a111c77dc44a7af9981096c70261868e5
  docs/master-design.md: 765641c215cd512ca78eb5dfc943503bc8bdb278102a3c3103cf4935ff7f38be
  docs/domain-design.md: 0d2509fd7d90cb460a6eeef1e8105fcf1154a8afae383b34bc4886088a8cd044
  docs/architecture-design.md: 8f588a11a9f0648b58f94cb3ed8cf6e20819fd50fd09fcabaec3e794b7729818
  docs/implementation/master-realization-plan.md: 0bd2336cfeac8894c6fc3f664920957185562a9f1343fa08b7eff2b7c7c1e91b
  docs/implementation/README.md: 63bc51f42e025d7aee7c81c2d28fec7cb016f781a924727b4c0fb8993e221518
historical_cross_check_sha256:
  docs/deprecated/2026-07-26-domain-design.md: e257e4e983c8a906d4aec0cd414826dad21588f43e306e06f80486917efa1745
  docs/deprecated/2026-07-26-architecture-design.md: cd603429c2da4d36b5a98307a184bd6311ca3eff5643cb4223815a39d4c1c04e
  docs/deprecated/2026-07-26-master-design.md: 5f0f6a48011942d85295eb980b66ccef99a6ee49b656feb23b1fefbcd170da90
source_sections:
  phase_a_interview:
    - "A1–A12, D1·D2, O1–O3"
  master:
    - "D1, §2.2–§2.3, §3.1, §4.1, §5.1–§5.2, §8.5"
  domain:
    - "§2.4 servicePattern, §4 fixed input, §4.3 reqDate, §4.4 vehicle, §4.6 adapter, §5 normalization"
  architecture:
    - "§4.2 adapters/input, D1 placement"
  realization_plan:
    - "Phase 01 D1 overlay, §4.1 tree, §2 drift table"
  implementation_readme:
    - "§5.1 KEEP_DISPLAY_SEPARATION; authority map"
  historical_cross_check_only:
    - "2026-07-26 Final D/A/M — conflict loses to APPROVED"
review:
  document: ../reviews/phase-01-review.md
  verdict: REBASE_PENDING_REREVIEW
  prior_verdict: ACCEPTED_WITH_APPLIED_CORRECTIONS
  implementation_authorized: false
```

## 1. 문서 지위와 사용 규칙

이 문서는 Phase 01 구현을 위한 상세 설계이며 구현 완료 보고가 아니다.
`document_status` / workflow 상태와 `implementation_status` / `phase_acceptance_status` /
`implementation_evidence_status`를 섞지 않는다.

이 문서 세트의 **live 권위**는 사용자 선언(15 Phase map · 2026-08-01 Phase 01 인터뷰 Decisions 1–10 포함),
[Phase A 인터뷰](../../deprecated/2026-07-30-design-interview-phase-a.md),
[Master](../../master-design.md) / [Domain](../../domain-design.md) / [Architecture](../../architecture-design.md) **APPROVED**,
그리고 이미 정렬된 implementation core 3([plan](../master-realization-plan.md), [README](../README.md), progress)이다.

`document_status: REBASE_APPLIED_PENDING_REREVIEW`는 **의미 rebase 반영 + 독립 re-review 대기**이며
Phase 01 구현, exit evidence, acceptance, win_poc 성공을 뜻하지 않는다.

다음 충돌 규칙을 적용한다.

1. 사용자 선언과 APPROVED Master(**D1**, A1–A12, D2/O1 등)가 우선한다.
2. 입력·정규화 의미는 [Domain](../../domain-design.md) APPROVED §4·§5·§2.4를 따른다.
3. adapter 배치는 [Architecture](../../architecture-design.md) **`adapters/input`** 및 plan Phase 01 overlay를 따른다.
4. 2026-07-26 Final Domain/Architecture/Master, deprecated open-questions·integrated design은
   **historical cross-check only**다. live 권위와 충돌하면 APPROVED가 이긴다.
5. Master에 없는 `Q-INFRA-01 RESOLVED` / “compute = Lambda only” / 구 `RESOLVED 26` 집계를
   live 질문 상태로 승격하지 않는다. Phase 01 관련 live OPEN은 wire/schema(**O3** 등),
   C-17 **GATED**, compute **O1 OPEN**(비범위) 등으로 Master/Domain/plan에서 인용한다.
6. `docs/codex/*`는 역사/참고이며 이 Phase에서 현재 authority로 사용하지 않는다.

현재 checkout에는 Phase 00~14 상세·review **15/15**가 존재한다.
[Phase 00](phase-00-build-architecture-skeleton.md) / [review](../reviews/phase-00-review.md)는
2026-08-01 semantic rebase 후 document/review **`REBASE_PENDING_REREVIEW`**(prior document-contract
`PASS_WITH_RESIDUAL_BLOCKERS`)이며 implementation은 `NOT_STARTED`, evidence `NOT_PRODUCED`,
acceptance `PLANNED/NOT_ACCEPTED`다. 따라서 Phase 01 실행은 계속
`BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE`다.

이 Phase의 [review](../reviews/phase-01-review.md)는 본문 rebase 후 **`REBASE_PENDING_REREVIEW`**다.
prior 문서 계약 verdict `ACCEPTED_WITH_APPLIED_CORRECTIONS`(2026-07-28)는 historical이며
새 본문을 자동 승인하지 않고, 구현/acceptance evidence도 아니다.
Entry evidence 전 마지막 안전 지점은 **문서 설계와 test/fixture 명세**이며 production source·target POM을 수정하지 않는다.

구체 package, type, method, canonical encoding과 error code 이름은 모두 **proposed internal design**이다.
Phase 00 accepted module tree 또는 별도 API/schema 승인이 다른 이름을 고르면 이름을 바꿀 수 있지만
이 문서의 **의미·불변조건·failure oracle·dependency direction**(D1 포함)은 보존해야 한다.

### 1.1 권위 source baseline

Fingerprint는 **2026-08-01 semantic rebase** 시 읽은 live authority bytes의 SHA-256이다.
원문이 바뀌면 구현 착수 전에 이 표를 다시 계산하고 영향 section을 review한다.

| 역할 | 입력과 SHA-256 | Phase 01에서 직접 적용하는 section |
|---|---|---|
| Phase A 인터뷰 | [design-interview-phase-a](../../deprecated/2026-07-30-design-interview-phase-a.md), `cec96defaa6a3617a7504ea0aeb57e0a111c77dc44a7af9981096c70261868e5` | D1, A1–A12, O3 |
| Canonical Master | [Master Design](../../master-design.md), `765641c215cd512ca78eb5dfc943503bc8bdb278102a3c3103cf4935ff7f38be` | D1, §5.1–§5.2, §8.5 |
| Approved Domain | [Domain Design](../../domain-design.md), `0d2509fd7d90cb460a6eeef1e8105fcf1154a8afae383b34bc4886088a8cd044` | §2.4, §4, §4.3 reqDate, §4.4, §4.6, §5 |
| Approved Architecture | [Architecture Design](../../architecture-design.md), `8f588a11a9f0648b58f94cb3ed8cf6e20819fd50fd09fcabaec3e794b7729818` | `adapters/input`, D1 placement |
| 총괄 실행 계획 | [Master Realization Plan](../master-realization-plan.md), `0bd2336cfeac8894c6fc3f664920957185562a9f1343fa08b7eff2b7c7c1e91b` | Phase 01 D1 overlay, §4.1 |
| 구현 문서 지도 | [Implementation README](../README.md), `63bc51f42e025d7aee7c81c2d28fec7cb016f781a924727b4c0fb8993e221518` | §5.1 filename KEEP |
| Historical only | [2026-07-26 Domain](../../deprecated/2026-07-26-domain-design.md), `e257e4e983c8a906d4aec0cd414826dad21588f43e306e06f80486917efa1745` | 퇴행 cross-check only |
| Historical only | [2026-07-26 Architecture](../../deprecated/2026-07-26-architecture-design.md), `cd603429c2da4d36b5a98307a184bd6311ca3eff5643cb4223815a39d4c1c04e` | 퇴행 cross-check only |
| Historical only | [2026-07-26 Master — SUPERSEDED](../../deprecated/2026-07-26-master-design.md), `5f0f6a48011942d85295eb980b66ccef99a6ee49b656feb23b1fefbcd170da90` | 퇴행 cross-check only |

`docs/codex/*` 및 deprecated open-questions·integrated design은 authority로 인용하지 않는다.

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

### 2.1 목표 (**D1**)

외부/레거시 request·fixture bytes를 anti-corruption boundary에서 해석하여
**단일 고정 canonical 계약**과 exact normalized facts를 만든다.
**multi-version 입력 스키마 병행 운영 MUST NOT.** 레거시 변환 경로는
Architecture **`adapters/input`의 adapter 하나**(공식 이름·범위 **O3 OPEN**).
wire/schema 문자열·declared adapter identity는 provenance용일 수 있으나
**multi-canonical track 운영이 아니다** (Master D1 · Domain §4.1·§4.6 · plan Phase 01 overlay).

```text
external bytes + optional declared adapter/source identity
→ adapters/input (single anti-corruption path)
→ CanonicalBusinessInput   // single fixed contract
→ strict normalization
→ NormalizedInputArtifact
```

`NormalizedInputArtifact`는 외부 표현의 alias, 문자열, 단위, 순서와 누락값을 solver가 다시 추정할 필요가 없도록
numeric, time, service(`reqDate`·`servicePattern`), identity, compatibility, ownership, zone, trip 의미를 확정한다.
동일 artifact는 Phase 02가 dense identity, complete `PreparedTravel`과
**`immutable solve snapshot`** 문제 쪽(구성 요소 proposed 이름 예: `ProblemInstance` ≠ freeze 단위 전체)을
만드는 **유일한** 입력이다.

### 2.2 범위

- 단일 canonical으로의 변환 (`adapters/input`); multi-version 스키마 **병행 운영 없음**
- 외부 DTO와 provider-neutral canonical input의 분리
- Raw bytes digest와 적용 alias/coercion/default/policy **provenance** (adapter identity 기록 허용)
- Plan identity, exact customer/profile/version과 objective preset의 명시적 선택 또는 omission
- External request/order/vehicle/location identity와 reference validation
- **`servicePattern` only**: `DELIVERY_ONLY` | `PICKUP_DELIVERY` (Domain §2.4; `kind` 없음)
- Optional mandatory declaration과 승인된 typed extension input의 보존·검증
- 무게·부피 `n=3/FLOOR`, item-first, `qty` 곱과 checked arithmetic (단위 **전역 고정**, D1)
- 비용·거리·시간의 integer-only syntax와 단위
- Plan `[start,end)`, inclusive close, planning-origin `long` seconds
- Repeating/overnight window의 deterministic expansion과 clipping
- `duration + Σ(item.taskTime × qty)` service-time 정규화
- 쪽별 **`reqDate`** 보존 (의미: 고객 요청 시각; Domain §4.3)
- Vehicle size, `vehicleFeatureList`, capability, **vehicle `zoneIds` 복수**, visit zone, ownership 정규화
- `oneway`와 single `roundtrip`, `multiRotation` 처리와 `waitInDepot` 의미 고정
- Vehicle/global `maxStopCnt`, `maxDriveTime`, `maxDriveDist`의 typed present/absent 선언
- Vehicle speed **present / absent / invalid→reject** (absent 시 01이 45를 채우지 않음; §6 handoff)
- Sparse provided travel declaration의 syntax/reference 정규화
- Canonical ordering, duplicate/ambiguity detection, deterministic error ordering
- Immutable artifact lifecycle, semantic fingerprint와 Phase 02 handoff contract

### 2.3 비범위

- Missing `D/U` 생성, Great Circle, `M²` coverage, speed→duration **실행** (Phase 02+)
- Dense `RequestId`/`VehicleId`/`SolverNodeId`/`PhysicalLocationId` 할당
- `PreparedTravel` 또는 **`immutable solve snapshot`** 조립
- Route propagation, `serviceStartTime ≤ reqDate` **판정 실행**, full-arc feasibility (Phase 03)
- `servableVehicles` dense bitset, final `PROVEN` unassignability 판정
- Profile/capability binding, preset default 해소, score, objective와 `SolvePlan`
- Pair insertion, route/bank state, portfolio, ALNS와 verifier
- HTTP/storage/cloud DTO, AWS/GCP SDK, object key 또는 public submission API 확정
- multi-version 입력 스키마 **병행 운영** 체계
- Route pool/MIP, provider 준비, solver/backend logic
- Current decimal `D/U` Win fixture를 compliant/official input으로 변경

Phase 01은 travel source를 **보존·검증하여 넘길 수는 있지만 준비하지 않는다**.
Phase 02가 소비할 provided/generated-input facts만 산출하고, search·verifier travel authority를 미리 만들지 않는다.

## 3. 적용 결정과 불변조건

### 3.1 확정 의미 (APPROVED + 2026-08-01 interview)

| 영역 | 적용 결정 |
|---|---|
| **D1 / Adapter** | 단일 고정 canonical. multi-version 병행 운영 MUST NOT. 단위 전역 고정. 레거시 → **`adapters/input` adapter 하나** (이름 O3 OPEN). wire 메타 ≠ multi-canonical 운영 |
| Numeric | 무게·부피 exact decimal `n=3/FLOOR`, item-first 후 positive integer `qty`, 비용·거리·시간 integer-only, checked arithmetic (Domain §5.1) |
| Time | exact `yyyy-MM-dd HH:mm:ss`, solver 밖 timezone, `[planStart,planEnd)`, inclusive window close, repeating/overnight (Domain §5.2) |
| **`reqDate`** | **고객 요청 시각** (완료 기한 아님). 제약 의미 = **`serviceStartTime ≤ reqDate` only** (`serviceEndTime` 제외). 쪽별: DELIVERY_ONLY=delivery; PICKUP_DELIVERY=pickup·delivery 각각 (Domain §4.3). 판정 **실행**은 Phase 03 |
| Service time | `duration + Σ(item.taskTime×qty)`; order-level `taskTime` 거부 |
| Trip/resource | oneway 우선, single roundtrip만 지원, route resource는 route 전체; multi-trip non-oneway → unsupported |
| Compatibility | vehicle size와 capability 분리, exact `["ALL"]`, case-sensitive free-form code, size ∧ capability ∧ zone |
| **`servicePattern`** | **only** `DELIVERY_ONLY` \| `PICKUP_DELIVERY`. `kind` / LOGICAL\|REAL / `REAL_PICKUP_DELIVERY` **폐기**. DELIVERY_ONLY = delivery visit + initial load (Domain §2.4) |
| **Ownership** | present → exact `DIRECT`\|`LEASE` only. **absent(미입력) = 소유 축 미사용**. missing→숨은 `DIRECT` 채움 **금지** (Domain §4.4) |
| **Zone** | vehicle **`zoneIds` 복수**; 미입력 = 전 구역. visit zone 쪽별 보존. zone 1개 강제 금지. conflict → 정규화 fact(≠ malformed reject) (Domain §5.3) |
| **Speed handoff** | present valid / absent / present-invalid→reject. **default 45 km/h는 travel prep 정책**(missing `U` 또는 추후 ruleset으로 `U` 재계산). Phase 01이 absent에 45를 **채우지 않음** |
| Travel handoff | provided integer directed `D/U` syntax/source 보존; complete preparation = Phase 02 |
| Wire/public schema | OPEN (Product·API·Data). alias/unknown policy exact 값은 승인 전 production 약속 아님 |

### 3.2 Phase 01 불변조건

1. Core canonical/normalized type은 Jackson, HTTP, cloud event, provider URI와 SDK type을 참조하지 않는다.
2. 변환 경로는 **단일**이다. multi-version 입력 **운영**·`latest`/유사 이름/field 추측/silent fallback 금지. declared adapter identity는 provenance로만 기록한다.
3. External ID는 schema가 명시한 decoding 뒤 case-sensitive opaque identity다. 승인되지 않은 trim, case-fold와 Unicode normalization을 하지 않는다.
4. Duplicate identity와 duplicate directed travel key는 canonical sorting이나 map overwrite 전에 거부한다.
5. Normalization은 exact decimal/integer string에서 시작하며 `double`을 중간 표현으로 쓰지 않는다.
6. 모든 scale, `qty` 곱, item/request/limit/service 합과 time offset 계산은 checked arithmetic이다.
7. Missing/invalid/infeasible를 numeric sentinel, `Long.MAX_VALUE`, plan end, 0 또는 999로 일반 치환하지 않는다. ownership/speed/zone restriction absence를 숨은 값으로 채우지 않는다.
8. 999 CBM은 adapter가 volume dimension 미사용을 명시한 경우에만 적용하는 유한 capacity이며 provenance에 남긴다.
9. Canonical collection order는 semantic set과 ordered sequence를 구분한다. Set-like collection만 versioned canonical comparator로 정렬한다.
10. 같은 normalized meaning과 policy는 같은 semantic fingerprint를 만든다. Raw byte digest와 alias provenance는 별도 identity로 보존한다.
11. Failure는 partial normalized artifact를 내지 않는다. Complete, deterministically ordered typed problems 또는 sealed success 중 하나만 반환한다.
12. Phase 01 output에는 dense solver ID, prepared matrix, **bound profile**, route, bank, score, objective와 solver state가 없다. Exact customer/profile/version, requested preset 또는 omission, mandatory와 승인된 typed extension 선언은 Phase 04가 raw input을 다시 읽지 않도록 보존하되 이 Phase에서 bind하거나 default를 채우지 않는다.

## 4. Entry gate와 현재 evidence 확인

### 4.1 필수 entry evidence

| 확인 항목 | 요구 evidence | 현재 checkout 판정 |
|---|---|---|
| Phase 00 상세/review | [actual Phase 00](phase-00-build-architecture-skeleton.md), [actual Phase 00 review](../reviews/phase-00-review.md) | 문서/review 있음; document/review **`REBASE_PENDING_REREVIEW`** (prior contract `PASS_WITH_RESIDUAL_BLOCKERS`); implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `PLANNED/NOT_ACCEPTED` — **`BLOCKED`** |
| Build skeleton | `E-P00-BUILD` accepted, root reactor와 exact build command | evidence 없음 — `BLOCKED` |
| Dependency guard | `E-P00-ARCH` accepted, module/package DAG와 forbidden dependency | evidence 없음 — `BLOCKED` |
| Legacy baseline | `E-P00-LEGACY` accepted | evidence 없음 — `BLOCKED` |
| Phase 01 source contract | [actual Phase 01 review](../reviews/phase-01-review.md) | **`REBASE_PENDING_REREVIEW`** (prior `ACCEPTED_WITH_APPLIED_CORRECTIONS`); 구현/acceptance evidence 아님 |

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

adapters/input/   # Architecture §4.2 · D1 — proposed module name; Phase 00 accepted tree 우선
├── src/main/java/com/ronext/rpdptw/adapter/input/
│   ├── InputAdapter.java              # proposed; single anti-corruption path (not multi-schema ops)
│   ├── ExternalInputDocument.java
│   ├── AdaptationResult.java
│   └── legacy/                        # optional one legacy mapping path (O3 OPEN name)
│       ├── LegacyExternalSolveDto.java
│       ├── LegacyExternalInputAdapter.java
│       └── LegacyAliasPolicy.java
└── src/test/java/com/ronext/rpdptw/adapter/input/
    ├── InputAdapterContractTest.java
    └── LegacyExternalInputAdapterTest.java

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
adapters/input DTO/mapper   # single path (D1)
  → rpdptw-core/input
  → rpdptw-core/normalization

build/test-fixtures
  → adapters/input + core test APIs

Phase 02 domain/travel
  → Phase 01 NormalizedInputArtifact
```

금지 edge:

```text
core → Jackson/HTTP/cloud/provider adapter
normalization → travel preparation/solver/application/verification
adapter → solver/profile/objective
Phase 01 → PreparedTravel / immutable-solve-snapshot builder implementation
multi-version input schema ops as parallel canonical tracks
```

## 6. 입력·출력 artifact와 contract

### 6.1 입력

| 입력 | 필수 내용 | 권위 |
|---|---|---|
| `ExternalInputDocument` (proposed) | exact bytes, optional declared adapter/source identity, media type | Inbound `adapters/input`가 전달; raw digest는 Phase 01이 계산/검증. identity ≠ multi-canonical 운영 |
| Adapter policy | approved alias/coercion/unknown-field policy (public wire OPEN) | Product·API·Data; O3 adapter 이름·범위 OPEN |
| `NormalizationPolicySnapshot` (proposed) | numeric/time/service/identity/order policy ID (+ travel-prep speed default policy ref) | Canonical decisions에서 생성, solve 중 변경 금지 |
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
- **`servicePattern`**: `DELIVERY_ONLY` | `PICKUP_DELIVERY` (+ DELIVERY_ONLY initial-load 의미)
- 쪽별 **`reqDate`** (요청 시각 의미; 판정 실행은 Phase 03)
- Exact customer/profile/version identity, requested objective preset 또는 명시적 omission
- Optional mandatory declaration과 승인된 typed extension input; binding/evaluation은 아직 수행하지 않음
- Location/coordinate input and sparse directed provided travel values
- Vehicle speed: **present valid** 또는 **absent** (invalid reject).
  Policy note only: travel prep **default speed = 45 km/h**; missing `U` 시 speed로 산출;
  추후 ruleset으로 present `U`도 speed 재계산 가능 — **적용 실행은 Phase 02+**. Phase 01이 45를 채우지 않음
- Vehicle **`zoneIds` 복수** 또는 전 구역(absent restriction); visit zone; ownership present/absent
- Terminal/trip/`waitInDepot`/vehicle·global route-resource declarations와 typed absence
- Policy IDs, source provenance와 fingerprints

다음은 제공하지 않는다.

- Dense IDs/array indices
- Generated distance/time 또는 speed→`U` 계산 결과
- Complete matrix or `PreparedTravel`
- **`immutable solve snapshot` 조립** (`ProblemInstance`는 snapshot 구성 요소 proposed 이름일 수 있음; Phase 01 산출 아님)
- Bound profile, routes, bank, score/objective

### 6.3 Public와 internal contract

| Contract | 상태 | 규칙 |
|---|---|---|
| External JSON/wire schema | `OPEN` | 별도 Product/API/Data 승인 전 fixture나 DTO를 public 약속으로 인용 금지 |
| Java adapter SPI | `PROPOSED INTERNAL` | **단일** `adapters/input` path + typed failure; multi-schema 운영 아님 |
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
/** Proposed SPI: single anti-corruption path (D1). Not multi-schema operations. */
public interface InputAdapter {
    boolean supports(AdapterIdentity adapter);

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

### 7.2 Canonical request와 service pattern (Domain §2.4 · §4.3)

```java
public record CanonicalRequestInput(
    ExternalRequestId id,
    ServicePattern servicePattern,
    Optional<CanonicalServiceInput> pickup,   // empty when DELIVERY_ONLY (no route visit)
    CanonicalServiceInput delivery,
    List<CanonicalItemInput> items,
    CanonicalCompatibilityInput compatibility,
    Optional<Boolean> mandatoryDeclaration,
    Optional<ApprovedTypedExtensionInput> extensionInput
) {}

/** Domain §2.4 — kind LOGICAL|REAL discarded. */
public enum ServicePattern {
    DELIVERY_ONLY,
    PICKUP_DELIVERY
}

/** Per-visit service side: windows, service time inputs, zone, reqDate (request time). */
public record CanonicalServiceInput(
    ExternalLocationId locationId,
    /* windows, duration, ... */
    Optional<NormalizedRequestTime> reqDate,  // 고객 요청 시각
    Optional<ZoneCode> zone
) {}
```

`DELIVERY_ONLY`: route 방문은 **delivery만**. pickup 쪽은 pair 소유 + **initial load**만이며
location/travel/stop/service visit을 만들지 않는다 (`kind`/logical enum 없음).
`PICKUP_DELIVERY`: pickup·delivery 둘 다 route 방문; **`reqDate`는 쪽별** 각각 보존.
Dense node와 pair object 생성은 Phase 02. `serviceStartTime ≤ reqDate` **판정 실행**은 Phase 03.

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

비용·거리·시간 parser는 decimal point와 exponent가 있는 numeric lexeme를 integer value로 조용히 바꾸지 않는다. Exact lexical policy는 adapter policy(OPEN wire)에 포함한다.

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

// reqDate constraint is NOT a completion-window policy.
// Domain §4.3: serviceStartTime <= reqDate only (executed in Phase 03).

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

### 7.5 Compatibility, zone, ownership (Domain §4.4 · §5.3)

```java
public record NormalizedCompatibilityInput(
    AllowedVehicleSizes allowedSizes,
    SortedSet<CapabilityCode> requiredCapabilities,
    Optional<ZoneCode> visitZone   // request/visit side; per pickup/delivery as applicable
) {}

public sealed interface AllowedVehicleSizes {
    record All() implements AllowedVehicleSizes {}
    record Exact(SortedSet<VehicleSizeCode> values)
        implements AllowedVehicleSizes {}
}

/** Vehicle zones: multi-zone; empty/absent = all zones (Domain §5.3). */
public sealed interface VehicleZoneSet {
    record AllZones() implements VehicleZoneSet {}
    record Restricted(SortedSet<ZoneCode> zoneIds) implements VehicleZoneSet {}
}

/** Ownership: absent = axis unused; never silent DIRECT (Domain §4.4). */
public sealed interface VehicleOwnership {
    record Absent() implements VehicleOwnership {}
    record Direct() implements VehicleOwnership {}
    record Lease() implements VehicleOwnership {}
}

/** Speed: Phase 01 does not fill default 45; travel prep owns application. */
public sealed interface VehicleSpeedInput {
    record Absent() implements VehicleSpeedInput {}
    record PresentKmH(double value) implements VehicleSpeedInput {} // valid finite only; else reject
}
```

Free-form code는 exact case-sensitive string이다. `Feature`의 숫자나 배열 순서에서 차량 크기를 추론하지 않는다.
Capability subset과 route zone feasibility **실행**은 후속 Phase가 수행하지만 Phase 01은
shape, duplicate code, pickup/delivery zone facts, vehicle multi-zone / all-zones를 엄격히 정규화한다.
zone conflict → 정규화된 static unassignability fact (malformed input 아님).

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
UNSUPPORTED_ADAPTER_OR_SOURCE
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
INVALID_SERVICE_PATTERN
INVALID_REQ_DATE
UNAPPROVED_EXTENSION_INPUT
INVALID_VEHICLE_FEATURE
INVALID_FEATURE_LIST
INVALID_CAPABILITY
INVALID_ZONE
INVALID_OWNERSHIP
INVALID_SPEED
INVALID_WAIT_POLICY
INVALID_ROUTE_RESOURCE_LIMIT
UNSUPPORTED_TRIP_POLICY
UNSUPPORTED_ROTATION
```

Unknown-field policy exact 값은 public wire **OPEN**이다. `UNKNOWN_FIELD_REJECTED`는 explicit `REJECT` adapter policy일 때만 사용하며 hidden default가 아니다. Problem은 `InputPath → code → stable evidence digest` 순으로 deterministic sort하고 raw PII/value 전체를 message나 log에 넣지 않는다.

### 7.7 End-to-end pseudo-code

```text
normalize(document, declaredAdapterIdentity, policy):
  rawDigest = digestExactBytes(document.bytes)
  adapter = singleInputPath.resolve(declaredAdapterIdentity)
      or reject UNSUPPORTED_ADAPTER_OR_SOURCE
  // D1: one path to single canonical — not multi-version schema ops

  dto = adapter.parseStrictly(document.bytes)
  canonicalDraft = adapter.mapToSingleCanonical(dto)
  problems += detectUnknownFieldsPerExplicitPolicy(dto)
  problems += detectDuplicatesBeforeMapsOrSorting(canonicalDraft)
  problems += validateAllReferences(canonicalDraft)
  problems += validateServicePatternAndReqDateShape(canonicalDraft)

  if problems not empty:
    return Rejected(sortDeterministically(problems))

  normalizedDraft = normalizeNumericTimeServiceCompatibilityTrip(canonicalDraft)
  // ownership/speed/zone: typed absence preserved; no silent DIRECT / no fill 45
  problems += checkedArithmeticAndBoundaryProblems(normalizedDraft)

  if problems not empty:
    return Rejected(sortDeterministically(problems))

  ordered = canonicalOrderOnlySetLikeCollections(normalizedDraft)
  semanticFingerprint = fingerprint(
      policy + adapter identity
      + plan/profile/preset-or-omission
      + mandatory/typed-extension declarations
      + trip/wait/resource/ownership/zone/speed declarations
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

### WP-01.1 — Single-path input adapter (D1 · `adapters/input`)

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.0 완료; adapter identity·alias policy(OPEN wire) 방향 합의 |
| 수정 대상 | `adapters/input/...`, `rpdptw-core/.../input` |
| 구체 작업 | External DTO 분리, **단일** adapter path, raw digest, unknown-field policy, plan/customer/profile/version와 preset omission, `servicePattern`/`reqDate` 쪽별 mapping, mandatory/approved-extension, provenance |
| 검증 명령 | `mvn -pl adapters/input -am -Dtest=InputAdapterContractTest,LegacyExternalInputAdapterTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `acceptsSupportedAdapterAndRecordsRawDigest`, `rejectsUnknownAdapterWithoutFallback`, `mapsPerSideReqDateOnly`, `preservesExactProfileSelectionAndPresetOmission`, `rejectsUnapprovedRawCustomerExtension`, `unknownFieldBehaviorComesFromExplicitPolicy` |
| 기대 결과 | 단일 canonical 또는 ordered typed problems; multi-version 운영 경로 없음 |
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
| 구체 작업 | Strict local datetime, planning-origin seconds, plan range, inclusive close representation, repeating/overnight expansion/clip, service-time sum, **쪽별 reqDate 보존**(요청 시각 의미; 판정 실행 없음), trip/wait policy와 vehicle/global route-resource present/absent 선언 |
| 검증 명령 | `mvn -pl rpdptw/core -am -Dtest=TimeNormalizerTest,ServiceTimeNormalizerTest,TripPolicyNormalizerTest,RouteResourceNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `convertsExactLocalDateTimeToPlanOriginSeconds`, `rejectsOffsetOrZone`, `keepsPlanEndExclusiveAndCloseInclusive`, `expandsOvernightWindowOncePerPlanDate`, `rejectsEqualOpenCloseWithoutSchemaMeaning`, `preservesFullArcRestartPolicyAndExactHandoffOracle`, `checksServiceDurationItemTimeQuantitySum`, `rejectsOrderLevelTaskTime`, `onewayIgnoresRotationButRecordsRawValue`, `normalizesExplicitDepotWaitPolicyWithoutFallback`, `rejectsUnknownDepotWaitPolicy`, `rejectsNonOnewayRotation`, `preservesMissingRouteLimitsAsTypedAbsence`, `rejectsNegativeOrFractionalRouteResourceLimit` |
| 기대 결과 | Normalized interval/policy와 exact expected seconds 또는 typed rejection 일치 |
| 실패/rollback | Partial window expansion을 폐기; plan/time sentinel 없음 |
| handoff | `E-P01-TIME` candidate report와 Phase 03 full-arc fixture |

### WP-01.5 — Size, capability, zone, ownership, speed shape

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.2 identity/reference rules |
| 수정 대상 | core compatibility/vehicle records/normalizer, compatibility oracle |
| 구체 작업 | Vehicle concrete feature, request exact `ALL`/set, capability sets, **vehicle `zoneIds` 복수 / 미입력=전 구역**, visit zone, pickup/delivery zone conflict fact, ownership present `DIRECT`\|`LEASE` / **absent 축 미사용**, speed present/absent/invalid (no fill 45), `servicePattern` shape |
| 검증 명령 | `mvn -pl rpdptw/core -am -Dtest=CompatibilityNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| 테스트 | `keepsFreeFormSizeCodeCaseSensitive`, `acceptsOnlyExactAllAlternative`, `rejectsMixedAllAndConcreteCode`, `rejectsVehicleAllOrBlank`, `preservesVehicleMultiZoneIds`, `normalizesMissingVehicleZonesToAllZones`, `preservesNoEligibleVehicleAsValidNormalizedFact`, `preservesConflictingConcretePickupDeliveryZonesAsStaticUnassignabilityFact`, `normalizesOwnershipPresentOrAbsentNeverSilentDirect`, `preservesAbsentSpeedWithoutFilling45`, `rejectsInvalidSpeed`, `acceptsServicePatternDeliveryOnlyAndPickupDeliveryOnly` |
| 기대 결과 | Static facts exact; zone conflict ≠ malformed; ownership/speed absence 보존 |
| 실패/rollback | No fallback registry/톤수 추론/숨은 DIRECT/45 채움; draft 폐기 |
| handoff | `E-P01-COMPAT` candidate report |

### WP-01.6 — Artifact sealing, typed error와 architecture guard

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-01.1~01.5의 모든 normalizer |
| 수정 대상 | artifact/fingerprint/result types, architecture rule |
| 구체 작업 | Defensive immutable copy, semantic/envelope fingerprint, deterministic error aggregation, raw input/PII-safe evidence와 log/report redaction, forbidden dependency와 forbidden output type 검사 |
| 검증 명령 | `mvn -pl rpdptw/core,adapters/input,build/architecture-rules -am test` |
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
| 검증 명령 | `mvn -pl rpdptw/core,adapters/input,build/architecture-rules -am verify`<br>`mvn verify` |
| 테스트 | §9.2의 모든 exact class/method, forbidden-dependency suite, required test skipped/failed 수 0 확인 |
| 기대 결과 | `E-P01-NUMERIC/TIME/COMPAT/ERROR` 모두 digest-protected, review verdict accepted |
| 실패/rollback | Phase 상태를 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING`에 유지; Phase 02 implementation 시작 금지 |
| handoff | [Phase 02](phase-02-prepared-travel-immutable-problem.md)가 소비할 exact `NormalizedInputArtifact` schema/version/fingerprint와 negative fixture catalog |

## 9. 테스트 작성 명세

### 9.1 Red → green 순서

현재 target module/type/test가 없으므로 아래 functional test를 먼저 추가하면 **test compilation 또는 missing class로 실패하는 것이 예상되는 미래 red**다. 이 실패는 현재 결함 evidence나 Phase completion이 아니다.

1. Adapter contract red: single-path D1, unknown policy, raw digest, no multi-version ops.
2. Identity/order red: duplicate/reference/order/fingerprint.
3. Numeric red: hand boundary, item-first, overflow.
4. Time/service/trip red: exact seconds, interval, service sum, reqDate shape, unsupported rotation.
5. Compatibility red: feature/capability/multi-zone/ownership absence/speed absence.
6. Artifact/error red: deep immutability, typed failures, fingerprint.
7. Architecture red: forbidden dependency/output type.
8. Minimal implementation으로 각 suite를 green으로 만들고 root verify를 실행한다.

Phase 03의 actual full-arc restart test와 Phase 02의 generated travel test는 Phase 01에서 green으로 만들지 않는다. Phase 01은 각각 `WorkArcPolicy.FULL_ARC_WITHIN_ONE_WORK_WINDOW`와 normalized travel-source fixture만 handoff한다.

### 9.2 Exact test class/method와 oracle

| Test class | Exact methods | Fixture/builder | Oracle | Layer |
|---|---|---|---|---|
| `InputAdapterContractTest` | `acceptsSupportedAdapterAndRecordsRawDigest`; `rejectsUnknownAdapterWithoutFallback`; `unknownFieldBehaviorComesFromExplicitPolicy` | Raw JSON bytes builder | Independent SHA-256 + expected code/path | Adapter contract |
| `LegacyExternalInputAdapterTest` | `mapsPerSideReqDateOnly`; `rejectsOrderLevelTaskTime`; `preservesExactProfileSelectionAndPresetOmission`; `rejectsUnapprovedRawCustomerExtension` | `ExternalInputFixtureBuilder` | Hand canonical DTO | Adapter unit |
| `CanonicalOrderingTest` | `rejectsDuplicateRequestBeforeCanonicalSort`; `setPermutationKeepsSemanticFingerprint`; `visitOrderChangesSemanticFingerprint`; `failureOrderIsInputPermutationIndependent` | Permutation generator with fixed seed | UTF-8 comparator reference, sorted expected tuple list | Property/oracle |
| `FixedPointNormalizerTest` | `floorsWeightAtThirdDecimal`; `normalizesEachItemBeforeQuantityMultiplication`; `rejectsDecimalDistanceEvenWhenMathematicallyIntegral`; overflow methods | Boundary table | `BigInteger`/decimal-string hand oracle, never production normalizer | Value/boundary |
| `TimeNormalizerTest` | `convertsExactLocalDateTimeToPlanOriginSeconds`; `keepsPlanEndExclusiveAndCloseInclusive`; `expandsOvernightWindowOncePerPlanDate`; `rejectsEqualOpenCloseWithoutSchemaMeaning`; `preservesFullArcRestartPolicyAndExactHandoffOracle` | Fixed 3-day plan + full-arc handoff fixture | Explicit expected second intervals and restart tuple | Value/oracle |
| `ServiceTimeNormalizerTest` | `checksServiceDurationItemTimeQuantitySum`; `detectsServiceTimeOverflow`; `rejectsOrderLevelTaskTime` | Item service builder | `Math.addExact/multiplyExact` reference | Value/boundary |
| `TripPolicyNormalizerTest` | `onewayIgnoresRotationButRecordsRawValue`; `acceptsSingleRoundtripZeroRotation`; `rejectsNonOnewayRotation`; `normalizesExplicitDepotWaitPolicyWithoutFallback`; `rejectsUnknownDepotWaitPolicy` | Trip/wait table | Expected sealed type + provenance/code | Unit |
| `RouteResourceNormalizerTest` | `preservesMissingRouteLimitsAsTypedAbsence`; `normalizesVehicleAndGlobalRouteLimitsIndependently`; `rejectsNegativeOrFractionalRouteResourceLimit`; `detectsRouteResourceLimitOverflow` | Vehicle/global resource table | Hand typed-presence and checked-integer oracle | Value/boundary |
| `CompatibilityNormalizerTest` | §8 WP-01.5 methods | Compatibility matrix builder | Independent set/subset/intersection functions | Property |
| `NormalizedInputArtifactTest` | `artifactDefensivelyCopiesAllCollections`; `sameMeaningAndPolicyHasSameSemanticFingerprint`; `profilePresetMandatoryResourceMeaningChangesSemanticFingerprint`; `aliasProvenanceChangesEnvelopeNotMeaning`; `rejectionNeverExposesPartialArtifact`; `rejectionEvidenceRedactsRawValuesAndInputBytes` | `CanonicalInputFixtureBuilder` | Canonical encoding test implementation + canary-token absence oracle | Contract/security |
| `Phase01DependencyRulesTest` | `coreDoesNotDependOnJacksonCloudSolverOrVerification`; `normalizationDoesNotCreatePreparedTravelOrSolveSnapshot`; `adapterDoesNotDependOnSolver` | Compiled class graph | Explicit forbidden package/artifact list | Architecture |

### 9.3 Exact failure fixtures

| Fixture | 입력 | Expected oracle |
|---|---|---|
| `item-first-difference` | weight `0.0009`, qty `2` | normalized weight `0`; line-first `1`을 만들면 failure |
| `scale-boundary` | `1.2340`, `1.2349` | 둘 다 `1234` milli-unit |
| `integer-looking-decimal` | distance/time `1.0` | `FRACTION_NOT_ALLOWED`, exact path |
| `quantity-overflow` | normalized item near `Long.MAX_VALUE`, qty `2` | `ARITHMETIC_OVERFLOW`, no artifact |
| `sum-overflow` | individually valid items whose sum overflows | `ARITHMETIC_OVERFLOW`, deterministic item/request path |
| `duplicate-request` | two requests same external ID (`DELIVERY_ONLY` + `PICKUP_DELIVERY`) | `DUPLICATE_IDENTITY` before sort/map |
| `duplicate-arc` | same directed `(A,B)` twice with same value | `DUPLICATE_TRAVEL_KEY`; no silent dedupe |
| `reqDate-shape` | missing/invalid per-side `reqDate` where required by servicePattern | typed reject or present shape as specified |
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
| `zone-conflict` | `PICKUP_DELIVERY` pickup zone A, delivery zone B | normalized static unassignability fact; not malformed input |
| `ownership-absent` | ownership omitted | typed absence; not silent `DIRECT` |
| `speed-absent` | speed omitted | absent preserved; no fill `45` in Phase 01 |
| `service-pattern-only` | only `DELIVERY_ONLY` / `PICKUP_DELIVERY` accepted | other kind/REAL/LOGICAL tokens reject |
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
    adapterIdentity (provenance; not multi-canonical ops)
    rawInputDigest
    semanticFingerprint
  external identity/reference graph
  normalized requests (servicePattern + per-side reqDate)/vehicles/locations
  plan identity + exact customer/profile/version
  requested objective preset or explicit omission
  optional mandatory + approved typed extension declarations
  integer units and normalized seconds
  sparse directed provided travel declarations
  coordinate + speed present/absent facts
  vehicle zoneIds multi / all-zones; ownership present/absent
  trip/terminal/wait/resource policies and typed absence
  travel-prep policy refs (default speed 45 km/h; optional U-recalc ruleset — apply in Phase 02+)
  adapter/normalization identities
  provenance + semantic/envelope fingerprints
```

여기서 service declaration은 아직 `SolverNodeId`가 아니다. Phase 02는 handoff fingerprint를 먼저 검증한 뒤 dense IDs·`PreparedTravel`·**`immutable solve snapshot`** 문제 쪽을 만든다
(`ProblemInstance`는 구성 요소 proposed 이름일 수 있음; freeze 단위 전체 아님).
Raw JSON alias, locale/timezone, decimal rounding, ownership silent DIRECT, speed 45 채움,
`reqDate` 의미(완료 기한/`serviceEndTime` 이중 상한)를 다시 왜곡하면 handoff 위반이다.

## 12. Blocker, OPEN, GATED와 deferred

| 항목 | 상태 | Owner | 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00 evidence | `BLOCKER` | Phase 00/build owner + reviewer | Phase 01 source implementation 전체 | 이 문서와 fixture 설계 | `E-P00-BUILD/ARCH/LEGACY` accepted + exact module contract (Phase 00 document rebase ≠ accepted evidence) |
| Public wire schema / adapter name (O3) | `OPEN` | Product·API·Data owner | Production external adapter compatibility | Adapter SPI/test-only fixture | wire·O3 승인 |
| exact coercion/alias/unknown policy | `OPEN PROPOSED` | Product·API·Data + Domain·Input | adapter green/production activation | Strict no-fallback abstract contract | Explicit policy + negative fixtures |
| Public customer/profile/preset/mandatory/extension field shape | `OPEN` | Product·API·Data + Profile owner | Production wire compatibility | Internal typed declaration + test-only fixture | field/type/omission/authorization 승인 |
| Canonical comparator/encoding name | `PROPOSED INTERNAL` | Domain·Architecture | Stable fingerprint compatibility | Test-only comparator | Review/ADR + replay/migration |
| Current Win fixture decimal `D/U` | `BLOCKER FOR OFFICIAL USE ONLY` | Input·Matrix + Benchmark | 해당 fixture compliant/official use | Negative rejection fixture | Integer matrix 또는 명시 migration |
| Benchmark calibration values | `OPEN` (Master/Domain; 구 Q-BENCH 표기 historical) | Benchmark·Quality | Phase 14 official manifest | Phase 01 unaffected | Calibration + approval |
| `C-17` hybrid | `GATED TARGET` | Product·Algorithm·Architecture + legal/ops/cost … | Phase 13 | Phase 01 unaffected | 06/07/08 accepted + 14A receipt + C-17 승인 묶음 |
| Optional variants | `OPEN/DEFERRED` (Master) | Product·Domain·Algorithm | Optional variants | Current pair/trip facts | 별도 승인 |
| Multi-trip/rotation | `DEFERRED FEATURE` | Product·Domain·Algorithm | Non-single-trip input | Reject unsupported | Trip contract + 승인 |
| Compute Lambda vs ECS | `O1 OPEN` | Architecture/ops | Phase 01 **비범위** | — | Master D2/O1; Q-INFRA RESOLVED Lambda **금지** |

Blocker가 발생해도 임시 default, 임의 `Q-*`, public field 또는 완료 evidence를 만들지 않는다.

## 13. Anti-pattern

- External JSON을 `immutable solve snapshot` / solver route 또는 `Map<String,Object>` core state로 직접 역직렬화
- multi-version 입력 스키마 **병행 운영** 또는 unknown field/alias를 `latest`/유사 이름/first-non-null로 해석
- `double` parse 후 fixed-point 변환
- Item line 합계를 먼저 만든 뒤 한 번만 `FLOOR`
- Decimal cost/distance/time 또는 decimal `D/U`를 round/truncate
- Overflow를 clamp, saturation, wraparound, `Long.MAX_VALUE`로 숨김
- External ID trim/case-fold/Unicode 변환을 policy 없이 적용
- Duplicate를 `Map.put`, `distinct`, sort 또는 last-write-wins로 제거
- Unordered collection iteration이나 locale comparator를 fingerprint/error order에 사용
- Plan end와 window close를 같은 포함/제외 의미로 처리
- `openTime==closeTime`을 임의로 24시간 또는 빈 창으로 해석
- `reqDate`를 완료 기한/`serviceEndTime` 이중 상한으로 해석
- `kind` LOGICAL\|REAL / `REAL_PICKUP_DELIVERY` / LogicalInitialLoad enum으로 service 구분
- `DELIVERY_ONLY`에 가짜 pickup travel/stop/service node 생성
- ownership missing → 숨은 `DIRECT`; speed absent → Phase 01에서 `45` 채움
- vehicle zone **1개 강제** 또는 multi-zone 폐기
- `Feature` 숫자에서 톤급 순서를 추론하거나 size/capability/zone을 한 문자열 축으로 합침
- Compatible vehicle 0개·zone conflict를 malformed input으로 거부하거나 final `PROVEN` outcome을 Phase 01에서 생성
- Phase 01에서 Great Circle, travel completion, dense ID, snapshot 조립, solver/verifier logic 구현
- Current HTTP/GCP placeholder 연결을 Phase 01 완료 evidence로 변경
- Future red/disabled test 또는 fixture 값을 production default로 승격
- Preset omission, optional mandatory, resource-limit/ownership/speed absence 또는 `waitInDepot`를 hidden default/sentinel으로 채움
- Raw customer extension, address/PII 또는 full input bytes를 generic map, error message, evidence/log attribute로 통과시킴
- 문서 rebase만으로 Phase `ACCEPTED` / win_poc / production 승인

## 14. Source → requirement → test traceability

| Requirement | Source | Phase 01 realization | Exact test/evidence |
|---|---|---|---|
| D1 single canonical + one adapter path | Master D1; Domain §4.1·§4.6; Arch `adapters/input`; plan Phase 01 | DTO/core 분리, single path, raw digest/provenance; multi-version ops MUST NOT | `InputAdapterContractTest`; `E-P01-ERROR` |
| Plan/profile/preset/extension declaration | Domain §4.1~§4.2 | Plan identity, customer/profile/version, preset omission, mandatory·typed extension 보존; binding/default 금지 | `LegacyExternalInputAdapterTest`, `NormalizedInputArtifactTest`; `E-P01-ERROR` |
| Fixed-point/integer-only | Domain §5.1 | Exact decimal, scale-3 floor, item-first, checked arithmetic | `FixedPointNormalizerTest`; `E-P01-NUMERIC` |
| Plan/window/time axis | Domain §5.2 | Strict local string, origin seconds, half-open plan, inclusive close | `TimeNormalizerTest`; `E-P01-TIME` |
| reqDate + service time | Domain §4.3·§5.2 | 요청 시각·쪽별 보존; service checked sum; order taskTime reject; 판정 실행은 Phase 03 | `ServiceTimeNormalizerTest`, `mapsPerSideReqDateOnly`; `E-P01-TIME` |
| Trip/wait/resource declaration | Domain §4.5; Master time facts | Oneway/single-roundtrip, depot-wait, typed vehicle/global limit absence | `TripPolicyNormalizerTest`, `RouteResourceNormalizerTest`; `E-P01-TIME` |
| Size/capability/zone | Domain §5.3·§4.4 | Exact size/ALL, capability, **vehicle multi-zone / all-zones**, visit zone | `CompatibilityNormalizerTest`; `E-P01-COMPAT` |
| servicePattern | Domain §2.4 | `DELIVERY_ONLY` \| `PICKUP_DELIVERY` only; initial load for DELIVERY_ONLY | service-pattern fixtures; `E-P01-COMPAT` |
| Ownership | Domain §4.4 | present DIRECT\|LEASE; **absent = axis unused**; no silent DIRECT | `normalizesOwnershipPresentOrAbsentNeverSilentDirect`; `E-P01-COMPAT` |
| Speed handoff | Domain §6 + user 2026-08-01; plan Phase 02 | present/absent only; default 45 policy ref for Phase 02+; no fill in 01 | `preservesAbsentSpeedWithoutFilling45`; handoff |
| Identity/order/fingerprint | Master identity; Domain fingerprint | Duplicate-before-sort, set/sequence, three identities | `CanonicalOrderingTest`, `NormalizedInputArtifactTest`; `E-P01-ERROR` |
| Typed pre-solve rejection | Master; Domain errors | Ordered sealed problems, no partial artifact | Failure fixture suite; `E-P01-ERROR` |
| Security/redaction | Plan §9/§13 | Raw PII canary 없음 | `rejectionEvidenceRedactsRawValuesAndInputBytes`; `E-P01-ERROR` |
| Phase boundary | plan Phase 01/02 | Normalized facts only; no prepared travel / snapshot assembly / solver | `Phase01DependencyRulesTest`; handoff manifest |

## 15. Review checklist

- [ ] Live fingerprints = Phase A + Master + **Domain** + **Architecture** APPROVED + core 3; Final = historical only
- [ ] **D1**: single canonical; multi-version ops MUST NOT; **`adapters/input`** one path
- [ ] **`reqDate`** = 요청 시각; **`serviceStartTime ≤ reqDate` only**
- [ ] **`servicePattern` only** `DELIVERY_ONLY` \| `PICKUP_DELIVERY`; no kind/REAL/Logical enum
- [ ] Ownership: present DIRECT\|LEASE; **absent = unused**; no silent DIRECT
- [ ] Vehicle **multi-zone** / 미입력=전 구역; zone 1개 강제 금지
- [ ] Speed: present/absent; **default 45** = travel-prep policy; Phase 01 **does not fill 45**
- [ ] Q-INFRA Lambda RESOLVED / `26/1/1` live 인용 없음; O1 OPEN · C-17 GATED 유지
- [ ] Phase 00/01 review **REBASE_PENDING_REREVIEW**; implementation/acceptance **NOT_***; no win_poc promotion
- [ ] Handoff terms: **`immutable solve snapshot`**; `ProblemInstance` ≠ full freeze
- [ ] filename **KEEP** `phase-01-canonical-input-normalization`
- [ ] Phase 00 evidence 부재를 구현 완료로 오인하지 않는다
- [ ] Public/internal/proposed/test-only/open 구분; OPEN 수치 MUST 고정 금지
- [ ] Partial artifact 금지; PII redaction; Phase 02 travel/snapshot 비범위
- [ ] 다른 Phase 본문 미수정 (이 문서 작업 범위)
