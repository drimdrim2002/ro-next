# Phase 01 사람용 구현 가이드 — Canonical input와 normalization

```yaml
guide_status: CORRECTION_02_APPLIED_PENDING_INDEPENDENT_RE_REVIEW
guide_kind: HUMAN_EDUCATIONAL_IMPLEMENTATION_GUIDE
phase: "01"
correction_round: "02"
canonical_phase_count: 15
canonical_phase_range: "00..14"
canonical_source_plan: docs/implementation/phases/phase-01-canonical-input-normalization.md
canonical_source_review: docs/implementation/reviews/phase-01-review.md
implementation_status: NOT_STARTED
implementation_evidence_status: NOT_PRODUCED
phase_acceptance_status: NOT_ACCEPTED
entry_gate_status: BLOCKED_BY_PHASE_00_ACCEPTED_EVIDENCE
public_api_status: OPEN_NOT_APPROVED
wire_schema_status: OPEN_REQUIRES_PRODUCT_API_DATA_OWNER_APPROVAL
type_and_method_names: PROPOSED_INTERNAL_UNLESS_MARKED_CONTRACT
inventory_head_baseline_at: 2026-07-29
inventory_branch: codex-implementation
inventory_head_baseline_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_live_snapshot_at: 2026-07-29T02:04:52+09:00
inventory_live_snapshot_status: UNCOMMITTED_UNAPPROVED_OBSERVATION_ONLY
inventory_live_root_pom_sha256: ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c
inventory_live_registry_sha256: 24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d
inventory_live_pom_set_sha256: c3bd8614cc821f2e1c17144321b320dfa547747dcc3abb1cdb111ca3d9ed9cb6
inventory_live_source_set_sha256: 795a5c514ef5511f109ee2acf9886f276c90dd58e30f027e5e03224a992e6a92
expected_reader:
  - Java의 class, interface, record, enum, sealed type와 Maven 기본을 안다.
  - CVRPTW의 demand, capacity, time window와 route feasibility를 안다.
  - 이 저장소의 RPDPTW 의미, module 경계와 evidence 절차는 처음 접한다.
roles:
  implementation_owner: Domain·Input implementation owner role
  schema_owner: Product·API·Data contract owner role
  numeric_time_owner: Domain·Input owner role
  architecture_owner: Build·Architecture owner role
  independent_reviewer: independent Phase 01 reviewer role
  handoff_consumer: Phase 02 Domain·Travel owner role
  scheduler: total scheduler role
predecessor:
  phase: "00"
  required_status: ACCEPTED
  required_evidence:
    - E-P00-BUILD
    - E-P00-ARCH
    - E-P00-LEGACY
successor:
  phase: "02"
  handoff: immutable NormalizedInputArtifact and digest-protected Phase 01 evidence
implementation_claim: NONE
```

> 이 문서는 구현자가 직접 판단하고 작업하기 위한 안내서다. 완성 코드를 복사해
> 붙이는 문서가 아니며, 아래 Java 이름과 signature는 별도 표기가 없으면
> **proposed internal candidate**다. 의미 계약은 보존하되 Phase 00에서 승인된
> module/package 이름과 public schema 승인 결과에 맞춰 이름은 바꿀 수 있다.

## 1. 이 가이드의 지위와 가장 먼저 알아야 할 결론

Phase 01은 외부 JSON이나 fixture를 solver가 추측 없이 소비할 수 있는 **내부 표준
의미**로 바꾸는 Phase다. 그러나 현재 checkout에서 Phase 01 구현을 시작할 entry
gate는 닫혀 있다.

- Phase 00 상세 문서와 문서 review는 존재한다. 이것은 구현 acceptance가 아니다.
- HEAD baseline `7cc890e…`에는 Phase 00 구현이 없지만, correction live snapshot에는
  별도 작업의 미커밋 reactor/legacy 이동/architecture test가 있다.
- 현재 canonical execution registry는 Phase 00을
  `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`, evidence를
  `REJECTED_PENDING_FIX_01_REGENERATION`, acceptance receipt를 `NOT_PRODUCED`로
  기록한다. 이 working-tree snapshot은 미승인·미커밋 관찰값이며 accepted baseline이
  아니다.
- 따라서 `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`와 acceptance receipt가
  생기기 전 Phase 01 production source/POM 변경은 시작하지 않는다.
- 지금 가능한 마지막 안전 지점은 이 가이드, contract 질문, test case와 fixture/oracle
  설계다.

Phase 01 원본 설계의 문서 review는
`ACCEPTED_WITH_APPLIED_CORRECTIONS`지만, 이것은 구현 acceptance가 아니다. 현재
실제 Phase 01 구현 상태는 `NOT_STARTED / NOT_ACCEPTED`이고 `E-P01-*` evidence는
없다.

이 문서에서 상태 표기는 다음처럼 읽는다.

| 표기 | 뜻 |
|---|---|
| **CONTRACT** | 권위 source에서 고정한 의미와 불변조건 |
| **PROPOSED INTERNAL** | 내부 Java/module 이름 후보. review에서 이름은 바꿀 수 있음 |
| **OPEN** | owner 승인 전 public schema, API, 정책 또는 안정 호환성 약속으로 확정 금지 |
| **TEST-ONLY** | fixture나 실험에만 쓰며 production default로 승격 금지 |
| **FUTURE RED** | target module/type이 생긴 뒤 먼저 실패해야 하는 test |
| **GATED** | 선행 evidence와 별도 승인 전 구현 또는 활성화 금지 |
| **DEFERRED** | restart condition 전 질문·구현·활성화하지 않음 |
| **LIVE SNAPSHOT** | 특정 시각의 미커밋·미승인 working-tree 관찰. HEAD나 acceptance authority가 아님 |

## 2. Source authority, 사용 section과 fingerprint

### 2.1 충돌 해소 순서

현재 구현 문서 세트는 다음 순서를 적용한다.

1. 사용자 선언과 15개 canonical Phase 범위
2. [Canonical Master](../../../master-design.md)
3. [질문 등록부](../../../master-design-open-questions.md)의 exact `Q-*` 상태
4. 현재 상위 [Domain Design](../../../domain-design.md)의 domain 의미·책임 지도
5. 현재 상위 [Architecture Design](../../../architecture-design.md)의
   `RECOMMENDED` Maven/package 지도
6. 사용자 고정 입력인 [2026-07-26 Domain Design](../../../2026-07-26-domain-design.md)의
   dated domain baseline
7. 사용자 고정 입력인
   [2026-07-26 Architecture Design](../../../2026-07-26-architecture-design.md)의
   dated architecture baseline
8. [통합 구현 설계](../../../architecture-domain-implementation-design.md)의 15 Phase 구조
9. [Master Realization Plan](../../master-realization-plan.md)의 실행·evidence·DoD

현재 상위 Domain/Architecture는 각각 의미와 권장 배치를 설명하는 `REVIEW` 지도이며,
구현 acceptance나 public Java/API 이름 승인이 아니다. Dated 두 문서는 사용자 고정
source baseline이지만, 질문 상태와 현재 상위 지도에 충돌하는 오래된 상태를 되살리는
권위로 사용하지 않는다. Implementation 문서는 이 의미와 배치를 Phase별 entry,
evidence, handoff로 실행하는 baseline이다.

Dated Domain과 Architecture 일부에 남은 `Q-INFRA-01 DEFERRED`,
`RESOLVED 25 / OPEN 1 / DEFERRED 2`는 dated drift다. 현재 적용 값은 Canonical
Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `26 / 1 / 1`이다. AWS S3 + Step
Functions + Lambda는 target/reference 선택일 뿐 구현·배포·production cutover
승인이 아니다.

[2026-07-26 Master Design — SUPERSEDED](../../../2026-07-26-master-design.md)는
누락·퇴행 cross-check에만 사용한다. `docs/codex/`는 현재 authority가 아니며 이
가이드의 요구 근거로 사용하지 않는다.

### 2.2 검증 가능한 source fingerprint

아래 Git blob ID와 SHA-256은 **HEAD baseline**
`codex-implementation@7cc890ee1d0805df5ae14b633127fade4f978639`에서 직접
계산한 historical source fingerprint다. 이 표는 live working-tree bytes를 뜻하지
않는다. 구현 착수 시 accepted source bytes가 다르면 변경 section과
requirement/test 영향부터 다시 review한다.

| Source | 직접 적용하는 heading/section | Git blob ID | SHA-256 |
|---|---|---|---|
| [`docs/master-design.md`](../../../master-design.md) | §1.5, §2~§8, §13~§17, 특히 입력·time·travel·RM-1·gate | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| [`docs/domain-design.md`](../../../domain-design.md) | §1~§8, §11, §16~§18, 현재 domain 의미·책임 지도 | `ace117c380466b733994a1fbb2a95d31e41b3959` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| [`docs/architecture-design.md`](../../../architecture-design.md) | §1~§7, §18~§20, `RECOMMENDED` module/package/DAG | `81495ff448d0e618ab3563e8ff80614fb1028acf` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| [`docs/2026-07-26-domain-design.md`](../../../2026-07-26-domain-design.md) | §1~§7, §16~§18, §20~§21 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| [`docs/2026-07-26-architecture-design.md`](../../../2026-07-26-architecture-design.md) | §1~§2, §5.6, §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| [`docs/architecture-domain-implementation-design.md`](../../../architecture-domain-implementation-design.md) | §1~§5, §19~§25, §27~§30 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| [`docs/master-design-open-questions.md`](../../../master-design-open-questions.md) | §1~§4, `Q-NUM-*`, `Q-TIME-*`, `Q-IN-*`, `Q-COMP-*`, `Q-REQ-*`, `Q-RES-01`, `Q-BENCH-02~03`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| [`docs/implementation/README.md`](../../README.md) | §1, §3~§7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| [`docs/implementation/master-realization-plan.md`](../../master-realization-plan.md) | §1~§7의 Phase 00~02, §8~§15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| [`docs/implementation/execution-progress-and-results.md`](../../execution-progress-and-results.md) | §1~§9, 특히 registry와 blocker | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` | `37f1a8a0ffad1e9614bd54d2b2444739fb83d0951bff73f2a2465ab54a3e8895` |
| [Phase 00 원본](../../phases/phase-00-build-architecture-skeleton.md) | §12~§17, 특히 blocker와 Phase 01 handoff | `410696302eb1dac289316eb74e52af9ad9e2dad3` | `0ad01e21a94ac543486a53c0ed0a256b4137e673bbae1be4d959ebc46dcefd27` |
| [Phase 00 review](../../reviews/phase-00-review.md) | §1~§5, inventory와 residual blocker | `107993c830df4d4daf7d8c2abedb423711174556` | `db4f1f8c4d178c99d82597085155a1fbfac223659944f3b092e66ae5b7f6fed8` |
| [Phase 01 원본](../../phases/phase-01-canonical-input-normalization.md) | 전체, 특히 §3~§15 | `ca8bff7cf3a0b971dc39a726ec16b323e6673133` | `67e078a058753335ae823bbec815b3628f212d4593dfce9ee0db39cc02000324` |
| [Phase 01 독립 review](../../reviews/phase-01-review.md) | 전체 finding 5개와 residual blocker | `1a926bdac08333f27b73816fcf0db52fc197a521` | `4644e7a7ef30f82368eea167e4f70197f4a1380a808c8066f37036608561f27f` |
| [Phase 02 원본](../../phases/phase-02-prepared-travel-immutable-problem.md) | §1~§7, §10~§14, 특히 Phase 01 handoff | `8b5f115369ca2189c80de12868fb3a63104d9228` | `51d8491a701f88b218609ea32e9ec714710ff88291760c1dc63bfc27bba9f686` |
| [`pom.xml`](../../../../pom.xml) | 좌표, Java/Maven enforcement, dependency/plugin과 single-project 사실 | `f8a411eadd4a5c01d8dd09fdea462738ca63d65f` | `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6` |

Phase 13/14 gate를 우회하지 않았는지 별도로 cross-check한 원본은 다음과 같다.

| Cross-check | 확인 section | Git blob ID | SHA-256 |
|---|---|---|---|
| [Phase 13 optional hybrid](../../phases/phase-13-optional-hybrid-route-selection.md) | §1.3, §4 activation gate, gate-closed no-load/skip | `cb3cd961c87b034625ad138046b5745822df16bd` | `ca31cfa532d179a6e278e2a3124eb5579b8775c39ae3cd13c0ca17ae2b4ac504` |
| [Phase 14 calibration/cutover](../../phases/phase-14-official-calibration-cutover.md) | §0.1, §2~§6, §8의 authority/rollback | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` | `c8f3b4fd2e48d35547d7e2fe31169a5e0a882730859ac97d9c5f60a165802eae` |
| [SUPERSEDED historical master](../../../2026-07-26-master-design.md) | historical regression cross-check only | `d4f7fbf058711a02451de911bc86b1cd2f426218` | `5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a` |

HEAD source fingerprints와 live inventory를 섞지 않는다. Live inventory는 §6.2의
timestamp와 working-tree hash 묶음으로만 해석하며, 그 어떤 live file도 independent
review와 post-review receipt 전에는 accepted source 또는 evidence가 아니다.

## 3. 큰 그림: 제품 흐름과 15개 Phase 중 Phase 01의 위치

### 3.1 제품이 해결하려는 문제

제품은 고객별 외부 입력을 받아 RPDPTW 운행 계획을 만들고, 모든 request가
`ASSIGNED` 또는 `UNASSIGNED` 중 정확히 하나인지 독립 검증한 뒤 결과를 발행한다.
이때 solver가 JSON field 이름, 날짜 해석, 단위, 누락값, vehicle size 의미를
탐색 도중 다시 추측하면 solver와 verifier가 서로 다른 문제를 풀 수 있다.

Phase 01은 이 semantic drift를 앞에서 차단한다.

```text
external bytes + declared schema/adapter identity
  → versioned anti-corruption adapter
  → CanonicalBusinessInput
  → strict normalization
  → immutable NormalizedInputArtifact
  → Phase 02 travel preparation와 immutable ProblemInstance
```

여기서 “canonical”은 여러 외부 표현을 하나의 업무 의미로 모은 상태이고,
“normalized”는 수치·시간·서비스·정렬·compatibility·trip 의미까지 checked value로
고정한 상태다. 둘 다 아직 dense solver ID, complete travel matrix, route, score,
objective 또는 ALNS state가 아니다.

### 3.2 Canonical Phase 00~14

Phase는 `00`부터 `14`까지 총 **15개**다. Phase 00을 빼서 14개로 세거나 Phase
13을 기본 경로에 끼워 넣지 않는다.

| Phase | 역할 | Phase 01과의 관계 |
|---:|---|---|
| 00 | Build와 architecture skeleton | **선행 producer**: accepted reactor, module DAG, dependency guard를 제공 |
| 01 | Canonical input와 normalization | 이 가이드의 소유 범위 |
| 02 | Prepared travel과 immutable problem | **직접 consumer**: normalized artifact만 읽고 dense ID/travel/problem 생성 |
| 03 | Propagation/evaluation kernel | Phase 01의 time/full-arc 정책을 실제 route 계산으로 실행 |
| 04 | Capability/customer profile | Phase 01이 보존한 profile/preset/extension 선언을 bind |
| 05 | Pair insertion과 initial portfolio | Immutable problem과 bound profile만 소비 |
| 06 | COW ALNS/reproducibility | ALNS-only search 구현 |
| 07 | Independent verification/final result | Candidate/result 두 gate 수행 |
| 08 | Application ports/local runtime | 검증된 ALNS-only local 실행 |
| 09 | No-DB object storage | Immutable artifact와 CAS publication |
| 10 | Provider-neutral coordinator | Declared worker/round 조정 |
| 11 | AWS reference distribution | 선택된 AWS target에 port mapping |
| 12 | Provider substitution | 승인된 대체 provider가 있을 때의 조건부 branch |
| 13 | Optional hybrid route selection | `C-17` **GATED optional branch** |
| 14 | ALNS benchmark qualification와 official cutover | 14A evidence gate, 14B official/production authority gate |

ALNS correctness/quality의 critical path는 다음과 같다.

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A
```

Phase 13은 `Phase 06/07/08 accepted + Phase 14A의 유효한
ALNS_BENCHMARK_ACCEPTANCE_RECEIPT + C-17의 나머지 scope/backend/법무/보안/운영/비용
승인`을 모두 요구한다. Phase 01 구현 편의를 위해 OR-Tools, MIP field, backend
정책을 input core에 넣지 않는다.

Phase 14B의 official manifest/cutover는 승인된 실행값, provider evidence, shadow,
rollback, signing trust와 명시적 production authority를 요구한다. Phase 01
fixture가 parse되거나 `win_poc_case_floor.json`이 존재하는 것만으로 official 또는
production label을 붙이지 않는다.

### 3.3 바로 앞과 바로 뒤의 producer-consumer 계약

```text
Phase 00 accepted outputs
  parent/reactor + accepted module names
  rpdptw-core as the sole semantic production-code start
  dependency/architecture guard
  test-fixture consumption rule
  exact build command + rollback point
        ↓
Phase 01 architecture-owned module-introduction gate
  approve adapters/common placement or another reviewed placement
  add only the approved adapter→core edge and test/architecture wiring
        ↓
Phase 01 outputs
  AdaptedCanonicalInput
  NormalizedInputArtifact
  typed InputProblem report
  raw/semantic/envelope identity와 provenance
        ↓
Phase 02 inputs
  validated external identity/reference graph
  normalized requests/services/vehicles/locations
  sparse provided travel declarations와 source facts
  typed absence/policy declarations
        ↓
Phase 02 creates, Phase 01 must not create
  dense IDs + PreparedTravel + ProblemInstance
```

Phase 02가 raw JSON의 alias, timezone, decimal rounding, ownership default를 다시
해석하면 handoff 위반이다. 반대로 Phase 01이 Great Circle, vehicle-resolved travel,
dense ID 또는 `ProblemInstance`를 만들면 scope 침범이다.

Phase 00의 explicit handoff에는 `adapters/common`이 없다. 현재 상위 Architecture가
이를 `RECOMMENDED` module로 제안하지만 승인된 reactor에 존재한다고 가정할 수는 없다.
또한 current Architecture §19의 build/phase 지도는 `adapters/common` local integration을
later `AR-6/RM-8-local`에 둔 반면 canonical Phase 01은 versioned input adapter를
요구한다. 이 시점 충돌을 숨기지 않고, Phase 01은 §9.2의 architecture-owner gate에서
input-only slice를 지금 도입해도 later local-runtime ownership을 선취하지 않는지
승인받는다.
승인되지 않으면 같은 dependency invariant를 지키는 다른 placement를 승인받거나
core contract/test-only fixture에서 멈춘다.

## 4. RPDPTW domain primer와 프로젝트 용어집

### 4.1 CVRPTW와 가장 다른 원자성

CVRPTW에서는 customer 한 방문을 route에 넣거나 빼는 경우가 흔하다. RPDPTW의
원자 단위는 `Request`다. Real pickup-delivery request는 두 service를 가지며 다음을
함께 지켜야 한다.

1. Pickup과 delivery는 같은 concrete vehicle route에 있다.
2. 필요한 service는 각각 정확히 한 번 있다.
3. Pickup position은 delivery position보다 앞선다.
4. Stable state에서는 request 전체가 route 하나 또는 request bank 한 곳에만 있다.

Phase 01은 아직 route를 만들지 않지만 이 identity를 잃지 않는 canonical pair
declaration을 만들어야 한다. Pickup DTO와 delivery DTO를 서로 무관한 order로
정규화하면 Phase 02 이후의 pair invariant를 복구할 수 없다.

### 4.2 Delivery-only와 real pickup-delivery

두 service pattern을 구분한다.

- **Delivery-only**: depot 출발 전에 이미 적재됐다. Logical pickup은 pair
  ownership에는 참여하지만 physical node, travel, stop, window와 service를 만들지 않는다.
- **Real pickup-delivery**: physical pickup과 delivery가 모두 location, window,
  service 의미를 가지며 route prefix load가 `+demand`, `-demand`로 변한다.

Phase 01의 canonical 표현은 이 차이를 typed하게 보존해야 한다. Delivery-only
pickup에 가짜 depot visit를 만들면 stop count, depot service와 travel이 오염된다.

### 4.3 `Order`, `Request`, `Node`, `Visit`, `Location`

| 용어 | 이 프로젝트의 의미 | Phase 01에서의 상태 |
|---|---|---|
| External `Order` | 고객/legacy 입력 표현 | Adapter가 읽는 source 표현 |
| Canonical `Request` | 하나의 원자적 운송 업무 | Phase 01이 identity와 service pattern을 고정 |
| Solver `Node` | terminal/pickup/delivery의 immutable solver 정의 | Phase 02가 생성 |
| `Visit` | route 순서 안의 node occurrence | Phase 05 이후 |
| Physical location | travel key가 되는 물리 위치 | Phase 01은 external identity를 보존, Phase 02가 dense mapping |

같은 건물의 pickup과 delivery도 solver node는 두 개일 수 있고 location만 같을 수
있다. Phase 01에서 location equality를 service identity equality로 바꾸지 않는다.

### 4.4 Canonical, normalized, prepared, bound

| Artifact/상태 | 해결한 것 | 아직 해결하지 않은 것 |
|---|---|---|
| `ExternalInputDocument` | bytes와 선언된 schema/adapter identity | field 의미 |
| `AdaptedCanonicalInput` | schema/alias를 내부 업무 field로 mapping | checked unit/time/order |
| `NormalizedInputArtifact` | 수치·시간·서비스·compatibility·trip·정렬·provenance | dense ID, complete travel, profile binding |
| `PreparedTravel` | 모든 필요한 directed distance/time | route/evaluation |
| `ProblemInstance` | dense ID, pair/node/vehicle/location와 prepared authority 결합 | customer profile의 실행 binding |
| `BoundProfile` | exact profile/preset/capability closure | search 결과 |

`requestedPreset` omission은 “default를 이미 골랐다”가 아니다. Phase 01은 omission을
보존하고, Phase 04가 exact customer default를 승인된 profile contract로 bind한다.

### 4.5 이 Phase의 세 가지 identity

1. **Raw input digest**: exact bytes의 digest다. 공백이나 field order가 달라져 bytes가
   다르면 달라진다.
2. **Semantic fingerprint**: normalized meaning, schema/adapter contract와
   normalization policy를 versioned canonical encoding으로 fingerprint한 값이다.
   Set-like 입력 순서만 달라 의미가 같으면 같아야 한다.
3. **Envelope fingerprint**: raw digest, semantic fingerprint, alias/coercion/default
   provenance를 결합한다.

이 구분이 없으면 “동일 의미의 다른 외부 표현”과 “exact same bytes”를 구별하지
못한다. Fingerprint algorithm/encoding 이름 자체는 아직 `PROPOSED INTERNAL`이며
ADR과 replay/migration review가 필요하다.

### 4.6 Lifecycle와 failure closure

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
→ REJECTED(ordered typed problems)
```

외부에 보이는 결과는 immutable `SEALED` artifact 또는 deterministic하게 정렬된
`REJECTED` report 둘 중 하나다. Partial normalized list나 mutable builder는 Phase
02에 전달하지 않는다.

### 4.7 핵심 용어집

| 용어 | 뜻 |
|---|---|
| Anti-corruption adapter | 외부 schema의 field/alias/문자열 형식을 core 의미와 분리하는 versioned mapper |
| Opaque external ID | 승인된 decoding 뒤 trim/case-fold/Unicode normalization을 추측하지 않는 case-sensitive identity |
| Checked arithmetic | overflow 시 wrap/clamp하지 않고 typed failure를 만드는 계산 |
| Item-first | item 한 단위를 먼저 `n=3/FLOOR`한 뒤 `qty`를 곱하고 합하는 순서 |
| Typed absence | 누락을 `0`, `Long.MAX_VALUE`, `999` 같은 sentinel이 아닌 별도 absent 값으로 표현 |
| Canonical order | set-like collection만 versioned comparator로 정렬하고 의미 있는 sequence는 유지 |
| Provenance | raw digest, schema/adapter/policy, alias/coercion/default와 source decision의 계보 |
| Full-arc policy | 한 work window에 arc 전체가 들어가야 출발하며 중간 pause/resume하지 않는 규칙 |
| Static unassignability fact | eligible vehicle이 없다는 입력 사실. Malformed input이나 최종 `PROVEN` outcome과 다름 |
| Evidence bundle | command/test/report/handoff를 immutable digest graph로 봉인한 Phase 검증 산출물 |

## 5. 시작 전 읽기 순서와 entry gate 확인법

### 5.1 읽기 순서

다음 순서를 지키면 상위 의미와 local 작업 지시를 섞지 않는다.

1. [Canonical Master §1.5](../../../master-design.md#15-규범어-결정-상태와-충돌-처리),
   [§4](../../../master-design.md#4-구현-아키텍처와-책임-경계),
   [§5~§8](../../../master-design.md#5-canonical-rpdptw-도메인-모델),
   [§15](../../../master-design.md#15-implementation-roadmap와-phase-gates)를 읽는다.
2. [Final Domain §2](../../../2026-07-26-domain-design.md#2-cvrptw-개발자를-위한-rpdptw-입문),
   [§4 입력](../../../2026-07-26-domain-design.md#4-입력-계약),
   [§5 정규화](../../../2026-07-26-domain-design.md#5-정규화),
   [§6 travel](../../../2026-07-26-domain-design.md#6-travel-preparation),
   [§16~§18](../../../2026-07-26-domain-design.md#16-오류와-종료-모델)을 읽는다.
3. [Final Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계)와
   [§5.6](../../../2026-07-26-architecture-design.md#56-test와-evidence),
   [§6](../../../2026-07-26-architecture-design.md#6-구현-순서와-gate)를 읽는다.
4. [통합 구현 설계 §2](../../../architecture-domain-implementation-design.md#2-전체-구현-순서),
   [§3](../../../architecture-domain-implementation-design.md#3-목표-project-architecture),
   [§5](../../../architecture-domain-implementation-design.md#5-phase-1--canonical-input와-normalization)을 읽는다.
5. [질문 등록부](../../../master-design-open-questions.md#2-질문결정-등록부)에서 이
   Phase에 해당하는 exact `Q-*` 행과 [남은 gate](../../../master-design-open-questions.md#4-남은-gate)를 확인한다.
6. [Master Realization Plan의 Phase 01](../../master-realization-plan.md#phase-01--내부-표준-입력과-정규화),
   [test 전략](../../master-realization-plan.md#8-공통-테스트-전략),
   [evidence 규칙](../../master-realization-plan.md#9-evidence-bundle-규칙),
   [DoD](../../master-realization-plan.md#11-definition-of-done)를 읽는다.
7. [Execution Progress](../../execution-progress-and-results.md#5-구현-task-registry)와
   [blocker](../../execution-progress-and-results.md#8-현재-blockers-open-gates와-남은-이슈)를
   읽어 문서 상태와 구현 상태를 분리한다.
8. [Phase 00 원본](../../phases/phase-00-build-architecture-skeleton.md#15-phase-01-handoff)과
   [Phase 00 review](../../reviews/phase-00-review.md)를 읽는다.
9. [Phase 01 원본](../../phases/phase-01-canonical-input-normalization.md)과
   [Phase 01 review](../../reviews/phase-01-review.md)를 나란히 읽는다.
10. [Phase 02 entry/handoff](../../phases/phase-02-prepared-travel-immutable-problem.md#13-이전다음-phase-handoff)를
    읽고 consumer가 실제로 필요한 field만 확인한다.

### 5.2 구현 시작 전 read-only 확인

다음 명령은 repository 내용을 바꾸지 않는다.

```bash
git status --short --untracked-files=all
git branch --show-current
git rev-parse HEAD
find . -type f -name pom.xml \
  -not -path './target/*' \
  -not -path './node_modules/*' \
  -print | sort
find . -type f -path '*/src/main/java/*.java' \
  -not -path './target/*' -print | sort
find . -type f -path '*/src/test/java/*.java' \
  -not -path './target/*' -print | sort
find adapters/common rpdptw/core build/test-fixtures build/architecture-rules \
  -type f -print 2>/dev/null | sort
test -x ./mvnw && shasum -a 256 ./mvnw .mvn/wrapper/maven-wrapper.properties
java -version
mvn -version
```

HEAD baseline의 관찰 결과는 root POM 1개, main Java 6개, test Java 1개다.
Correction live snapshot은 POM 13개, main Java 33개, test Java 16개로 다르다.
숫자나 §6.2의 hash가 달라지면 어느 쪽도 “현재”로 재사용하지 말고 새 timestamp/hash와
accepted/unaccepted 분류를 만든다. `./mvnw --version`도 dependency download나 local
cache 변화를 일으킬 수 있으므로 accepted wrapper/cache 절차가 정해지기 전에는 단순
존재·checksum 확인과 system `mvn -version`을 분리한다.

### 5.3 Phase 00 entry evidence 확인

문서 파일 존재나 root `mvn verify` 성공만 보지 말고 다음 AND gate를 확인한다.

```text
accepted Phase 00 source/build identity
AND E-P00-BUILD digest
AND E-P00-ARCH digest
AND E-P00-LEGACY digest
AND independent Phase 00 review report digest
AND post-review acceptance receipt
AND accepted module/package/test command
AND rollback point digest
```

하나라도 없으면 Phase 01 상태는 `BLOCKED`다. 이때 허용되는 작업은:

- public/wire contract 질문 정리
- fixture와 independent oracle 설계
- proposed type/package review
- future red test inventory 작성

허용되지 않는 작업은:

- target POM/module 생성
- production adapter/core source 추가
- 기존 controller를 새 path에 연결
- evidence나 Phase 상태를 `READY/ACCEPTED`로 표시

### 5.4 Entry evidence가 생긴 뒤 재개 조건

재개할 때는 accepted Phase 00 commit/digest checkout과 현재 working tree가 같은지
확인한다. 다른 작업자의 unrelated 변경이 있으면 보존하고, Phase 01 소유 경로와
겹칠 때만 충돌을 조정한다. 승인된 module 이름이 원본의 proposed tree와 다르면
이 가이드의 의미/의존 방향은 유지하되 경로와 Maven 명령을 accepted 이름으로
치환한다.

다음 overlap stop rule은 예외가 아니다.

1. Accepted Phase 00 source manifest와 current `HEAD`/working-tree manifest를 비교한다.
2. `rpdptw/core`, 승인 후보 adapter path, parent/aggregator POM,
   `build/test-fixtures`, `build/architecture-rules` 중 하나에 미커밋 변경이 있으면
   소유자·source digest·의도를 확인한다.
3. Phase 01 변경과 같은 file/section을 건드리는 미승인 작업이면 merge하거나
   덮어쓰지 않고 `BLOCKED_OVERLAP`에서 멈춘다.
4. 겹치지 않는 unrelated 변경은 보존한다. Clean working tree를 만들기 위해
   reset/checkout/delete하지 않는다.

## 6. 실제 repository inventory: HEAD baseline, live drift와 목표 상태

### 6.1 HEAD baseline — historical accepted-source 후보가 아닌 비교 기준

다음 표는 `codex-implementation@7cc890ee1d0805df5ae14b633127fade4f978639`의
tracked bytes만 설명한다. `2026-07-29 현재 checkout`이라고 읽지 않는다.

| 영역 | HEAD baseline 사실 | Phase 01 판정 |
|---|---|---|
| Build | `HEAD:pom.xml` 하나, `com.ronext:ro-next:0.1.0-SNAPSHOT`, implicit JAR, SHA-256 `f61cab65…` | Target reactor가 아님 |
| Root dependencies | Google Workflow, GCS, Jackson, JUnit가 root classpath | Core/adapter/provider 격리 없음 |
| Main/test Java | `HEAD:src/**`에 main 6개, test 1개 | Phase 01 구현/evidence 아님 |
| Legacy placeholder | `HEAD:src/main/java/com/ronext/optimizer/**`에 synthetic engine와 HTTP/GCP path | RPDPTW normalization/ALNS 증거 아님 |
| Target modules | `rpdptw/core`, `adapters/common`, `build/test-fixtures`, `build/architecture-rules` 부재 | 미래 명령 실행 불가 |
| Phase 01 types/evidence | `CanonicalBusinessInput`, `NormalizedInputArtifact`, `InputProblem`, `E-P01-*` 부재 | `NOT_STARTED / NOT_ACCEPTED` |

위 `HEAD:src/**` path는 correction live tree에서 삭제·이동 상태이므로 local file link로
제공하지 않는다. Historical bytes가 필요하면 `git show
7cc890ee1d0805df5ae14b633127fade4f978639:<path>`로 읽고 working tree에 복원하지
않는다.

<a id="61-2026-07-29-실제-상태"></a>

### 6.2 Correction live snapshot — 미커밋·미승인 관찰

아래는 correction을 시작하기 직전
`2026-07-29T02:04:52+09:00`의 **LIVE SNAPSHOT**이다. 다른 Phase 00 작업의
working-tree 상태이며 acceptance authority가 아니다.

| Snapshot key | 값 |
|---|---|
| Branch / HEAD | `codex-implementation` / `7cc890ee1d0805df5ae14b633127fade4f978639` |
| Root POM SHA-256 | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` |
| Canonical execution registry SHA-256 | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` |
| Sorted POM-set SHA-256 | `c3bd8614cc821f2e1c17144321b320dfa547747dcc3abb1cdb111ca3d9ed9cb6` |
| Sorted `rpdptw/build/legacy` Java-set SHA-256 | `795a5c514ef5511f109ee2acf9886f276c90dd58e30f027e5e03224a992e6a92` |
| Pre-correction porcelain status SHA-256 | `1edb3760e8813cc906c621ee27c3cfca04894370ce7aab35ab28789cce2bb376` |

| 영역 | Live 관찰 | Phase 01 판정 |
|---|---|---|
| Reactor | Root 포함 POM 13개, root `packaging=pom`, `rpdptw/build/legacy` aggregator와 wrapper 존재 | 미커밋 Phase 00 candidate; accepted build 아님 |
| Target skeleton | [`rpdptw/core`](../../../../rpdptw/core/pom.xml), [`build/test-fixtures`](../../../../build/test-fixtures/pom.xml), [`build/architecture-rules`](../../../../build/architecture-rules/pom.xml) 존재 | Phase 00 review/receipt 전 권위 없음 |
| Source/test | `rpdptw/build/legacy` 아래 main Java 33개, test Java 16개 | 대부분 package skeleton/Phase 00 test/legacy; Phase 01 completion 아님 |
| Phase 01 production type | `rpdptw/core/.../input`과 `normalization`에는 `package-info.java`만 있고 production type 0개 | Phase 01은 여전히 `NOT_STARTED` |
| Adapter module | `adapters/common`과 `adapters/pom.xml` 부재 | §9.2 architecture-owner gate 전 WP-01.1 실행 불가 |
| Legacy source | Root `src/**`의 6 main/1 test는 삭제 상태이고 [`legacy/gcp-placeholder`](../../../../legacy/gcp-placeholder/)로 이동·확장 | HEAD path를 현재 file로 오인 금지 |
| Phase 00 registry | `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`, rejected evidence regeneration 대기, acceptance receipt `NOT_PRODUCED` | Phase 01 entry `BLOCKED` |
| Phase 00 candidate evidence dir | `target/phase-00-evidence/` 부재 | review 당시 artifact도 mutable했으며 acceptance로 사용 금지 |
| Phase 01 evidence | `E-P01-NUMERIC/TIME/COMPAT/ERROR`, pre-review manifest, receipt 부재 | Phase acceptance 불가 |
| Data | [`win_poc_case.json`](../../../../data/win_poc_case.json), [`win_poc_case_floor.json`](../../../../data/win_poc_case_floor.json) 존재 | Raw는 negative/provenance; FLOOR는 plan-final 범위, Phase 01 acceptance 단독 증거 아님 |

Review snapshot `2026-07-29T01:27:35+09:00`에는 ignored Phase 00 candidate
evidence와 test Java 14개가 관찰됐지만, correction snapshot에는 evidence directory가
없고 test Java가 16개다. 이 차이는 live artifact가 mutable하다는 직접 증거다.
어느 snapshot도 HEAD source fingerprint, accepted Phase 00 manifest 또는 receipt와
합치지 않는다.

Live root `mvn verify`가 성공하더라도 현재는 rejected/pending Phase 00 candidate를
검증할 뿐 `E-P00-*` 또는 `E-P01-*`가 아니다. 이 correction은 Maven을 실행하지 않아
다른 작업의 `target/`을 생성·변경하지 않는다.

<a id="62-목표-상태"></a>

### 6.3 목표 상태

Phase 00의 explicit handoff 목표는 accepted `rpdptw-core`와 test/architecture seam이다.
`adapters/common`은 Phase 00 산출물이라고 가정하지 않는다. Architecture owner가
Phase 01의 module-introduction을 승인하는 branch의 proposed target은 다음과 같다.

```text
rpdptw/core/
  src/main/java/com/ronext/rpdptw/input/
  src/main/java/com/ronext/rpdptw/normalization/
  src/test/java/com/ronext/rpdptw/normalization/

adapters/common/
  src/main/java/com/ronext/rpdptw/adapter/json/input/
  src/test/java/com/ronext/rpdptw/adapter/json/input/

build/test-fixtures/
  Phase 00에서 승인한 test-jar/classifier/scope 규칙

build/architecture-rules/
  Phase01DependencyRulesTest
```

승인된 다른 adapter placement를 선택하면 path와 artifactId만 바꾸고
`adapter → core`, `core ↛ adapter/Jackson`, test-fixture production 비누출과
architecture-rule coverage는 그대로 유지한다. `adapters/common`이라는 이름은
current Architecture의 `RECOMMENDED`/`PROPOSED INTERNAL`이지 public API나 accepted
reactor 사실이 아니다.

목표는 file tree 자체가 아니라 다음 책임 경계다.

```text
versioned external DTO/mapper
  → provider-neutral canonical input
  → strict core normalization
  → immutable normalized artifact

core MUST NOT → Jackson/HTTP/cloud/provider/solver/verifier
normalization MUST NOT → travel preparation/ProblemInstance/search
adapter MUST NOT → solver/profile binding/objective
```

## 7. Scope, non-scope와 결정 상태

### 7.1 이 Phase가 반드시 하는 일

- Declared schema/adapter version exact dispatch와 allowlist
- External DTO와 canonical/normalized core type 분리
- Raw bytes digest와 alias/coercion/default/policy provenance
- Plan identity, exact customer/profile/version, requested preset 또는 omission 보존
- Optional mandatory declaration과 승인된 typed extension input 보존
- External request/order/vehicle/location identity와 reference validation
- Delivery-only와 real pickup-delivery의 canonical pair 의미
- 무게·부피 `n=3/FLOOR`, item-first, `qty` 곱과 checked sum
- 비용·거리·시간 integer-only syntax
- `[planStart, planEnd)`, inclusive close, planning-origin `long` seconds
- Repeating/overnight window expansion과 clipping
- `duration + Σ(item.taskTime × qty)` service time
- Vehicle size, capability, zone, ownership 정규화
- Oneway와 single roundtrip, `waitInDepot`, route-resource present/absent
- Sparse provided travel의 typed directed key, integer-meter `D`/integer-second `U`
  present/absent와 source identity
- Legacy `C`의 raw provenance와 non-authority, coordinate/speed의
  present-valid/missing/invalid 구분
- Duplicate/ambiguity를 sort/map 전에 거부
- Set-like collection의 deterministic canonical order
- Typed total rejection 또는 sealed immutable success
- Raw/semantic/envelope fingerprint와 Phase 02 handoff

### 7.2 이 Phase가 하지 않는 일

- Missing `D/U` 생성, Great Circle, `M²` coverage와 vehicle-resolved time
- Dense `RequestId`, `VehicleId`, `SolverNodeId`, `PhysicalLocationId` 할당
- `PreparedTravel`, `ProblemInstance`, route, bank, score, objective, solver state 생성
- Profile/capability binding과 preset default 선택
- Actual full-arc propagation, wait location 결정과 route feasibility
- `servableVehicles` dense bitset 또는 final `PROVEN` outcome
- HTTP/storage/cloud public DTO, object key, AWS/GCP SDK와 public API 확정
- Phase 13 route pool/MIP/backend field 또는 OR-Tools dependency
- Official calibration 값, production provider 설정 또는 cutover authority

### 7.3 확정된 결정

| 영역 | CONTRACT |
|---|---|
| Numeric | Weight/volume exact decimal `n=3/FLOOR`; item-first; cost/distance/time integer-only; checked arithmetic |
| Time | Exact local `yyyy-MM-dd HH:mm:ss`; timezone은 solver 밖; plan `[start,end)`; close inclusive |
| Service | `reqDate/dueDate` 완료기한 alias; `duration + Σ(item.taskTime×qty)`; order-level `taskTime` reject |
| Window | Daily repeat, `open>close` overnight, `open==close` schema 의미 없으면 reject |
| Work arc | Arc 전체가 한 work window에 들어가야 하며 다음 창에서 처음부터 재시작 |
| Trip | Oneway 우선; single roundtrip만 지원; non-oneway rotation은 unsupported |
| Window policy | `START_ONLY`가 default 의미이고 `COMPLETE_WITHIN_WINDOW` profile 선택 가능; Phase 04가 bind, Phase 03이 적용 |
| Depot service | Depot `taskTime`은 미적용; `depot.duration`은 future rotation의 trip 사이에만 적용하고 최초 출발/최종 복귀에는 미적용 |
| Depot wait | Exact `N`은 earliest departure 뒤 고객 대기, exact `Y`는 같은 조기 대기를 depot으로 이동; feasibility 완화 없음 |
| Route resource | Customer service location transition stop, actual traversed arc의 `D/U`, route-total/no-reset; vehicle/global declaration은 독립 보존 |
| Compatibility | Vehicle concrete size; request exact list 또는 `["ALL"]`; case-sensitive free-form; size∧capability∧zone |
| Ownership | Missing/null/empty=`DIRECT`, exact `DIRECT/LEASE`, 그 외 reject |
| Pair | Delivery-only logical initial load와 real physical pair를 분리하되 request identity 유지 |
| Travel input | Provided directed integer `D/U`의 typed presence/absence와 source를 보존하고 `C`는 non-authoritative; complete preparation은 Phase 02 |

### 7.4 OPEN, GATED, DEFERRED와 사람 승인

| 항목 | 상태 | Owner/승인자 | 마지막 안전 지점 | 해제 조건 |
|---|---|---|---|---|
| Phase 00 accepted baseline | `BLOCKER` | Build·Architecture + independent reviewer + scheduler | 문서/test 설계 | `E-P00-BUILD/ARCH/LEGACY`, review report와 acceptance receipt |
| Adapter module placement | `OPEN — ARCHITECTURE APPROVAL REQUIRED` | Build·Architecture + Phase 01 owner | Accepted core-only baseline, adapter contract/test 설계 | `adapters/common` 도입 또는 다른 placement, aggregator/POM/DAG/rule/test wiring 승인 |
| Public wire schema/version | `OPEN` | Product·API·Data | Adapter SPI와 TEST-ONLY fixture | Versioned field/unknown/alias/coercion policy 승인 |
| Window/wait/resource wire shape | `OPEN` | Product·API·Data + Domain | 이미 resolved된 semantic contract와 typed internal declaration | Field requiredness/omission/alias/unknown/version shape 승인 |
| Great Circle/coordinate/source policy | `PHASE 02 BLOCKER` | Domain·Travel + Architecture | Phase 01 typed source/absence handoff | Function/version, coordinate precision, source policy 승인 |
| Public profile/preset/mandatory/extension shape | `OPEN` | Product·API·Data + Profile owner | Internal typed declaration/omission | Versioned type, authorization와 compatibility 승인 |
| Canonical comparator/encoding | `PROPOSED INTERNAL` | Domain·Architecture | Test-only versioned encoding | ADR, collision/framing/replay/migration review |
| Raw decimal Win fixture | raw 직접 canonical 실행은 차단 | Input·Matrix + Benchmark | Negative fixture/provenance | 승인된 FLOOR artifact는 plan-final 범위에서 별도 사용; raw를 성공 input으로 바꾸지 않음 |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Benchmark·Quality | Phase 01 unaffected | Calibration, measured review, explicit approval |
| Phase 13 `C-17` | `GATED TARGET` | Product·Algorithm·Architecture와 backend/법무/보안/운영/비용 owner | ALNS-only path | Phase 06/07/08 accepted + Phase 14A receipt + 나머지 승인 |
| `Q-VAR-01` | `DEFERRED` | Product·Domain·Algorithm | 현재 pair/single-trip 계약 | Variant/fixture/core-impact evidence와 별도 승인 |
| Multi-trip/rotation | `DEFERRED FEATURE` | Product·Domain·Algorithm | Unsupported input reject | Trip/reset/depot/resource 계약과 승인 |
| Phase 14B production | `NOT_GRANTED / GATED` | Product·Release·Security·Operations | ALNS-only local/evidence path | Phase 11, 14A, official values, provider/shadow/rollback와 scoped authority |

사람 승인이 필요한 지점에서는 test fixture 값을 production default로 쓰지 않는다.
승인 전 구현 가능한 최대 범위가 adapter SPI/test-only fixture라면 그 지점에서
멈추고 blocker를 evidence에 기록한다.

`Q-TIME-03`과 `Q-IN-02`의 semantic meaning은 이미 `RESOLVED`다. 위 표의 OPEN은
그 의미를 다시 승인받으라는 뜻이 아니라 versioned public field shape,
requiredness/omission, alias와 unknown-field 처리만 가리킨다. Exact `Y/N` mapping,
depot/task/resource 의미를 임의 default로 바꾸거나 approval 대기 항목으로 되돌리지
않는다.

## 8. 학습 경로: 개념에서 실제 통합까지

### 8.1 단계 A — 개념 지도 만들기

해야 할 일:

1. External `Order`와 canonical `Request`를 한 문장씩 정의한다.
2. Delivery-only logical pickup과 real pickup node의 차이를 손으로 그린다.
3. Raw digest, semantic fingerprint, envelope fingerprint의 차이를 예시 bytes 두 개로
   설명한다.
4. Canonical → normalized → prepared → bound의 책임을 표로 적는다.
5. Sparse `D/U` declaration과 Phase 02의 complete `PreparedTravel`을 별도 상자로
   그린다.
6. Window policy declaration(Phase 01), profile binding(Phase 04), 실제
   propagation(Phase 03)의 owner를 나눠 적는다.

완료 신호:

- “왜 Phase 01에서 `ProblemInstance`를 만들면 안 되는가?”를 답할 수 있다.
- “왜 preset omission을 Phase 01이 default로 바꾸면 안 되는가?”를 답할 수 있다.
- “같은 location과 같은 node가 왜 다른가?”를 답할 수 있다.

자문 질문:

- 지금 설계한 type에 solver node, route, score가 섞이지 않았는가?
- JSON/Jackson annotation이 core record에 들어가지는 않았는가?
- Missing과 invalid를 같은 `null`/숫자 sentinel로 처리하지 않았는가?

### 8.2 단계 B — 작은 탐색과 hand oracle

Production code 전에 손으로 계산한다.

1. `0.0009 kg × qty 2`를 item-first와 line-first로 각각 계산한다.
2. `planEnd` event와 inclusive window close의 차이를 second 축에 표시한다.
3. `22:00→02:00` overnight window를 3일 plan에 펼치고 clip한다.
4. `reqDate`와 `dueDate`가 같은 경우/다른 경우를 구분한다.
5. `["ALL"]`과 `["ALL","T1"]`, pickup/delivery size 교집합을 표로 만든다.
6. 동일 request/travel key가 두 번 나왔을 때 map에 넣기 전 어떻게 잡는지 적는다.
7. `(A,B)`의 `D`만 present, `U`만 present, 둘 다 absent인 세 경우와 legacy `C`가
   함께 있는 경우의 Phase 01 typed handoff를 적는다.
8. Coordinate/speed를 present-valid, missing, present-invalid로 나누고, invalid만
   rejection이며 missing generation/default 적용은 Phase 02임을 적는다.
9. Exact `waitInDepot=N/Y`, depot `taskTime`/`duration`, route-total stop/drive
   resource를 표로 만들고 Phase 01이 계산하지 않는 값을 표시한다.

완료 신호:

- Expected integer, interval, error code와 path를 production normalizer 없이 계산했다.
- Fixture expected를 “현재 구현 출력 복사”가 아닌 independent oracle로 기록했다.
- PII canary가 error/report/log에 없어야 한다는 검사 문자열을 정했다.

자문 질문:

- Oracle이 production helper를 재사용해 같은 버그를 공유하지 않는가?
- Hash-map iteration이나 locale이 expected order에 숨어 있지 않은가?
- False-green이 될 수 있는 “test 0건 성공”을 어떻게 검출할 것인가?

### 8.3 단계 C — 실제 변경

Phase 00 acceptance 뒤 WP-01.1부터 red→green 순서로 구현한다. 한꺼번에 parser,
normalizer, travel, solver를 만들지 않는다. 각 WP는:

```text
future red
→ 최소 의미 구현
→ focused green
→ owner module report 존재 확인
→ full module/root regression
→ immutable evidence candidate
```

완료 신호:

- Adapter/core 의존 방향이 build에서 강제된다.
- 각 failure가 partial artifact 없이 typed report를 반환한다.
- Fixture permutation에도 semantic fingerprint와 error order가 안정적이다.
- OPEN public schema를 내부 test-only schema로 위장하지 않는다.

### 8.4 단계 D — 통합과 handoff

Adapter → canonical → normalizer → sealed artifact의 integration을 실행하고 Phase 02
consumer contract test를 붙인다. Phase 02 test는 raw bytes를 다시 읽지 않고
artifact field/fingerprint만 검사해야 한다.

완료 신호:

- `E-P01-NUMERIC/TIME/COMPAT/ERROR`가 모두 digest-protected다.
- Pre-review manifest, independent review report, acceptance receipt가 단방향 graph다.
- Phase 02 handoff artifact와 rollback point가 명시된다.
- Phase 01 상태를 올리는 주체가 구현자 개인이 아니라 scheduler/acceptance authority임을
  지킨다.

## 9. Ordered work packages

모든 package/type 이름은 Phase 00 accepted baseline과 schema owner approval에
맞춰 조정한다. 현재 checkout에서는 WP-01.0의 read-only 확인과 fixture/oracle
설계, adapter placement approval 요청까지만 수행할 수 있다. `rpdptw-core`만
Phase 00의 semantic production handoff이며 adapter module은 자동 선행조건이 아니다.

### 9.1 WP-01.0 — Entry evidence와 contract freeze

**목적과 이유**

Phase 00이 승인한 reactor/module/test-fixture 규칙 없이 Phase 01 코드를 만들면 곧바로
경로 이동이나 dependency 누출이 발생한다. 먼저 실제 baseline을 동결한다.

**사전조건**

- 이 가이드와 canonical Phase 01/review를 읽었다.
- Phase 00 acceptance evidence를 조회할 권한이 있다.

**예상 target**

- Source 변경 없음
- Phase 실행 record의 baseline section
- Read-only evidence verifier

**구체 행동**

1. `E-P00-BUILD/ARCH/LEGACY` artifact digest를 확인한다.
2. Phase 00 independent review report와 post-review acceptance receipt가 같은
   pre-review manifest digest를 참조하는지 확인한다.
3. Accepted source commit, Java/Maven, module names, test-fixture classifier/scope,
   root/focused command와 rollback point를 기록한다.
4. Current source fingerprint와 §2.2를 비교한다.
5. Public schema/alias/unknown-field approval가 있는지 별도 확인한다.
6. §6.2 명령을 다시 실행해 timestamp, POM/source/status hash를 새로 만들고 accepted
   manifest와 live drift를 별도 행으로 기록한다.
7. §5.4 overlap stop rule로 `core`, adapter 후보, POM, fixture/rule path의 소유권을
   확인한다.

**선택 근거**

문서 review와 implementation acceptance를 분리하고, future command가 실제 reactor를
가리키도록 하기 위함이다.

**금지 shortcut**

- Phase 00 문서/review 파일 존재를 acceptance로 간주
- Current root `mvn verify` 성공을 `E-P00-BUILD`로 간주
- Proposed module 이름을 승인된 이름으로 간주
- Live candidate evidence나 ignored `target/`을 accepted receipt와 혼합

**검증**

```bash
git status --short --untracked-files=all
git rev-parse HEAD
find . -type f -name pom.xml \
  -not -path './target/*' \
  -not -path './node_modules/*' \
  -print | sort
```

Accepted evidence verifier의 exact 명령은 Phase 00 handoff가 제공한 것을 사용한다.

**기대 결과**

동일한 source/build identity에 묶인 accepted evidence와 정확한 module/test command가
확인된다.

**실패 해석과 rollback**

하나라도 없거나 digest가 다르면 `BLOCKED`를 유지한다. Source 변경을 시작하지
않았으므로 rollback은 필요 없고 read-only baseline으로 돌아간다.

**다음 handoff**

WP-01.1~01.7이 소비할 accepted build/module baseline과 unresolved approval 목록.

### 9.2 WP-01.1 — Versioned anti-corruption adapter

**목적과 이유**

External JSON의 alias, field naming과 schema version이 core로 새지 않게 한다.

**사전조건**

- WP-01.0 완료
- Exact supported schema/adapter version 승인
- Alias/coercion/unknown-field allowlist 승인
- Profile/preset/mandatory/extension wire shape는 승인된 범위만 사용
- 아래 WP-01.1A의 placement/module-introduction branch 승인

#### WP-01.1A — Adapter placement와 module-introduction gate

Phase 00 accepted handoff는 `rpdptw-core`와 test/architecture seam뿐이다. Architecture
owner는 production adapter source를 추가하기 전에 다음 중 하나를 명시적으로
승인한다.

- **Branch A — current recommended placement:** `adapters` aggregator와
  `adapters/common` leaf를 Phase 01이 도입한다.
- **Branch B — approved alternate placement:** 다른 existing/새 module을 사용하되
  adapter DTO/Jackson 경계와 `adapter → core` inward dependency를 동일하게 지킨다.

승인 record는 current Architecture §19의 later `AR-6/RM-8-local` ownership과
canonical Phase 01 adapter 요구 사이의 시점 차이를 명시해야 한다. Branch A는
versioned input mapping slice만 소유하며 local artifact/state/dispatch, app assembly,
HTTP 또는 Phase 08 E2E 완료를 주장하지 않는다.

Branch A의 **PROPOSED INTERNAL** 변경 계약은 다음과 같다.

```text
pom.xml
  add adapters aggregator entry
adapters/pom.xml
  packaging=pom; add common leaf
adapters/common/pom.xml
  artifactId=rpdptw-adapter-common
  compile → com.ronext:rpdptw-core
  test-only → com.ronext:rpdptw-test-fixtures:
              type=test-jar,classifier=tests,scope=test
build/architecture-rules/pom.xml
  test-only inspect → rpdptw-adapter-common
build/architecture-rules/src/test/...
  update approved ReactorTopologyTest module allowlist
  add Phase01DependencyRulesTest adapter/core/Jackson/fixture rules
```

Exact coordinates/version/Jackson dependency는 accepted parent
`dependencyManagement`와 schema owner approval를 사용한다. Phase 01 slice에서는
필요한 compile edge를 `adapter → core` 하나로 제한한다. Current Architecture가
later consumer를 위해 허용한 adapter→application/verification edge를 선점하지
않는다. Jackson/transport dependency는 adapter leaf에만 있고 core에는 0개여야 한다.

`build/test-fixtures`는 core-only reusable oracle을 소유하고 adapter production
module을 역의존하지 않는다. Adapter-specific raw-byte builder는 adapter의
`src/test/java`에 둔다. Adapter test가 accepted `tests` classifier를 test scope로
소비하고 architecture-rules가 양쪽 compiled graph를 검사하게 하여
`adapter ↔ test-fixtures` reactor cycle과 production leakage를 동시에 막는다.

승인 뒤의 red→green/evidence 순서는 다음과 같다.

1. 승인 record, parent/aggregator/module POM과 rollback patch digest를 봉인한다.
2. Reactor가 leaf를 찾고 `adapter → core`만 resolve하는지 확인한다.
3. Jackson이 core dependency tree/bytecode에 0개인지 positive rule을 실행한다.
4. Forbidden `core → adapter/Jackson`, production fixture leakage와 cycle negative
   control이 실제 non-zero로 실패하는지 확인한다.
5. Adapter owner FUTURE RED를 실행한 뒤 최소 contract 구현으로 green을 만든다.

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl adapters/common,build/architecture-rules -am clean test
./mvnw -B -ntp -Dstyle.color=never \
  -pl adapters/common -am dependency:tree
```

이 명령은 Phase 00 receipt와 Branch A 승인 뒤에만 유효하다. Branch B면 exact approved
module selector로 바꾼다. Reactor discovery, dependency tree, architecture
positive/negative report와 POM/source digest를 `E-P01-ERROR`의 module-introduction
leaf로 기록한다. 실패하면 새 aggregator/leaf/rule 변경만 승인된 rollback patch로
되돌리고 accepted core-only handoff에서 멈춘다.

**예상 file/package/type**

```text
adapters/common/.../adapter/json/input/
  VersionedInputAdapter
  ExternalInputDocument
  AdaptationResult
  <approved-version>/ExternalSolveDto
  <approved-version>/ExternalInputAdapter

rpdptw/core/.../input/
  SchemaIdentity
  AdapterIdentity
  CanonicalBusinessInput
  InputProvenance
```

모두 **PROPOSED INTERNAL**이다. DTO만 Jackson을 알아도 되며 core type은 알면 안 된다.

**구체 행동**

1. Exact schema+adapter key로 registry를 조회하고 `latest/nearest` fallback을 금지한다.
2. Exact bytes digest를 adapter parsing 전 계산한다.
3. Approved alias가 함께 있으면 normalized equality를 검사하고 둘 다 provenance에 남긴다.
4. Plan identity, exact customer/profile/version, preset present/omitted, optional
   mandatory와 approved typed extension을 손실 없이 mapping한다.
5. Unknown-field behavior를 schema version 정책으로 주입한다.
6. Unsupported schema, alias conflict, unapproved extension은 typed rejection으로 닫는다.

**선택 근거**

External compatibility와 내부 의미의 변경 수명을 분리하고 Phase 04가 raw input을 다시
읽지 않게 한다.

**금지 shortcut**

- `Map<String,Object>`를 canonical core로 통과
- First-non-null alias 선택
- Unknown field 무시를 hidden default로 사용
- Raw customer extension map 저장
- Error message에 raw bytes/address/email 출력

**FUTURE RED와 명령**

WP-01.1A에서 placement와 module이 승인·생성된 뒤 사용한다.

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl adapters/common -am \
  -Dtest=VersionedInputAdapterContractTest,V1ExternalInputAdapterTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  clean test
```

`V1` 이름은 실제 승인 version으로 바꾼다. `failIfNoSpecifiedTests=false`는 `-am`으로
함께 빌드되는 비소유 module의 no-match만 허용한다. Owner module report 누락이나
0 tests는 실패다. `clean`은 stale report를 제거하기 위한 evidence 전용 실행이며,
현재 미승인 live tree에서는 실행하지 않는다. §11.3의 승인된 유일 catalog와
§11.6 manifest verifier가 모든 owner class/method의 양방향 exact set과 fresh report를
확인하기 전에는 green으로 판정하지 않는다.

**필수 test 후보**

- `acceptsSupportedSchemaAndRecordsRawDigest`
- `rejectsUnknownSchemaVersionWithoutFallback`
- `unknownFieldBehaviorComesFromExplicitVersionedPolicy`
- `acceptsEqualReqDateDueDateAliasAndRecordsBoth`
- `rejectsConflictingReqDateDueDate`
- `preservesExactProfileSelectionAndPresetOmission`
- `rejectsUnapprovedRawCustomerExtension`

**기대 결과**

지원된 version은 deterministic canonical input, 나머지는 ordered typed problems를
반환한다.

**실패 해석과 rollback**

Parser exception은 정상 input rejection과 구분한다. Adapter 등록과 해당 version
implementation만 마지막 green point로 되돌린다. Legacy controller 연결은 이 WP에서
변경하지 않는다.

**다음 handoff**

`AdaptedCanonicalInput` positive/negative fixture와 raw digest/provenance.

### 9.3 WP-01.2 — Identity, reference graph와 canonical order

**목적과 이유**

Map overwrite와 정렬이 duplicate를 숨기지 않게 하고, set과 sequence 의미를 분리한다.

**사전조건**

- WP-01.1의 accepted canonical DTO
- External identity decoding policy 승인

**예상 file/package/type**

```text
rpdptw/core/.../input/
  ExternalPlanId
  ExternalRequestId
  ExternalVehicleId
  ExternalLocationId

rpdptw/core/.../normalization/
  CanonicalOrdering
  InputPath
  InputProblem
```

**구체 행동**

1. ID를 case-sensitive opaque value로 받는다.
2. Request/order 통합 namespace, vehicle, location, directed travel key의 duplicate를
   map/sort 전에 검출한다.
3. 모든 request→service→location, vehicle→terminal, travel→location reference를 검증한다.
4. Requests, vehicles, physical locations, capability/size set과 sparse arc 같은
   set-like collection만 canonical sort한다.
5. Pickup→delivery와 ordered service sequence는 sort하지 않는다.
6. Error list를 `InputPath → code → stable evidence digest`로 total-order한다.

**선택 근거**

Equivalent meaning의 입력 permutation은 같은 semantic fingerprint를 가져야 하지만,
의미 있는 visit order 변화는 달라야 한다.

**금지 shortcut**

- `Map.put` last-write-wins
- `distinct()`로 duplicate 제거
- Trim/case-fold/Unicode normalization 추측
- Locale comparator 또는 hash-map iteration 사용

**FUTURE RED와 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am \
  -Dtest=CanonicalOrderingTest,NormalizedInputArtifactTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  clean test
```

**필수 test 후보**

- `rejectsDuplicateRequestBeforeCanonicalSort`
- `rejectsDuplicateTravelKeyEvenWhenValuesMatch`
- `rejectsDanglingLocationReference`
- `setPermutationKeepsSemanticFingerprint`
- `visitOrderChangesSemanticFingerprint`
- `failureOrderIsInputPermutationIndependent`

**기대 결과**

Input 순서와 map iteration이 달라도 같은 meaning은 같은 order/fingerprint를 만들고,
duplicate/dangling reference는 partial artifact 없이 거부된다.

**실패 해석과 rollback**

중복이 sort 뒤 사라지면 algorithm defect다. Canonical draft를 발행하지 말고 해당
WP 변경을 폐기한다.

**다음 handoff**

Identity/reference-valid canonical draft와 canonical ordering policy candidate.

### 9.4 WP-01.3 — Numeric와 unit normalization

**목적과 이유**

Binary floating point, line-first rounding, overflow가 demand/capacity 의미를 바꾸지
않게 한다.

**사전조건**

- WP-01.2 valid identity/reference graph
- Numeric policy ID/version 고정

**예상 file/package/type**

```text
rpdptw/core/.../normalization/
  DecimalLexeme
  FixedPointNormalizer
  MilliKilograms
  MilliCubicMeters
  Meters
  Seconds
```

**구체 행동**

1. Exact decimal lexeme를 binary `double` 없이 검증한다.
2. Weight/volume은 nonnegative, scale 3 `FLOOR`한다.
3. 각 item을 먼저 normalize한 뒤 positive integer `qty`를 `multiplyExact` 의미로 곱한다.
4. Request 합을 checked addition으로 누적한다.
5. Cost/distance/time의 decimal point 또는 exponent syntax는 approved schema가
   integer syntax로 허용하지 않으면 거부한다.
6. Scale, multiply, sum overflow를 `ARITHMETIC_OVERFLOW`로 닫는다.
7. 999 CBM은 “volume 차원 미사용”이 explicit일 때만 유한 capacity로 적용하고
   provenance를 남긴다.

**선택 근거**

Demand와 capacity가 동일 단위/rounding을 쓰고, solver/verifier가 long 값 하나를
공유하게 한다.

**금지 shortcut**

- `double` parse 후 곱셈
- Line decimal 합계 후 한 번 `FLOOR`
- Decimal distance/time `1.0`을 integer 1로 수용
- Overflow wrap, clamp, saturation, `Long.MAX_VALUE`
- Missing constraint를 큰 수로 치환

**FUTURE RED와 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am \
  -Dtest=FixedPointNormalizerTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  clean test
```

**필수 test 후보**

- `floorsWeightAtThirdDecimal`
- `normalizesEachItemBeforeQuantityMultiplication`
- `rejectsDecimalDistanceEvenWhenMathematicallyIntegral`
- `detectsScaleOverflow`
- `detectsQuantityMultiplicationOverflow`
- `detectsRequestSumOverflow`
- `appliesFinite999CbmOnlyWhenVolumeUnusedIsExplicit`

**기대 결과**

Hand-written `BigInteger`/decimal-string oracle와 exact long/code/path가 일치한다.

**실패 해석과 rollback**

한 field overflow도 전체 normalization rejection이다. 이전 accepted artifact는
바꾸지 않고 draft를 폐기한다.

**다음 handoff**

`E-P01-NUMERIC` candidate report와 boundary fixture table.

#### WP-01.3A — Sparse travel input와 source fact seal

**목적과 Phase 경계**

Phase 01은 travel을 준비하지 않고 Phase 02가 준비할 입력 선언을 완전하게 봉인한다.
Provided `D/U`, coordinate와 speed를 raw DTO에 남겨 두면 Phase 02가 JSON을 다시
해석하게 되므로 typed presence/absence, source identity와 deterministic order까지는
Phase 01 소유다.

**사전조건**

- WP-01.2의 external location/reference graph와 duplicate-before-sort rule
- WP-01.3의 integer meter/second syntax와 checked range
- Coordinate syntax/range/precision과 source identity가 승인되지 않았다면
  production type 확정 대신 TEST-ONLY fixture/contract에서 멈춘다.

**PROPOSED INTERNAL type 책임**

다음은 Java 25 문법에 맞는 skeletal candidate다. Field/API 이름과 coordinate
representation은 승인 전 확정하지 않는다.

```java
record DirectedTravelKey(
    ExternalLocationId from,
    ExternalLocationId to
) {}

sealed interface ProvidedMetric<T>
    permits ProvidedMetric.Present, ProvidedMetric.Absent {
    record Present<T>(T value, TravelSourceIdentity source)
        implements ProvidedMetric<T> {}
    record Absent<T>(TravelSourceIdentity source)
        implements ProvidedMetric<T> {}
}

record NormalizedSparseTravelDeclaration(
    DirectedTravelKey key,
    ProvidedMetric<Meters> distance,
    ProvidedMetric<Seconds> commonTravelTime,
    LegacyCProvenance legacyC
) {}

sealed interface VehicleSpeedDeclaration
    permits VehicleSpeedDeclaration.PresentPositive,
            VehicleSpeedDeclaration.Missing {
    record PresentPositive(
        ApprovedSpeedValue value,
        TravelSourceIdentity source
    ) implements VehicleSpeedDeclaration {}
    record Missing(TravelSourceIdentity source)
        implements VehicleSpeedDeclaration {}
}

sealed interface CoordinateDeclaration
    permits CoordinateDeclaration.PresentValid,
            CoordinateDeclaration.Missing {
    record PresentValid(
        ApprovedCoordinateValue value,
        TravelSourceIdentity source
    ) implements CoordinateDeclaration {}
    record Missing(TravelSourceIdentity source)
        implements CoordinateDeclaration {}
}
```

`PresentInvalid`는 sealed success variant가 아니라 ordered typed rejection이다.
`ApprovedCoordinateValue`/`ApprovedSpeedValue`, precision과 source-policy 이름은
**OPEN/PROPOSED INTERNAL**이다. `LegacyCProvenance`는 raw/envelope provenance
only이며 distance/time authority method를 노출하면 안 된다.

**구체 행동**

1. Directed key는 physical external location ID pair이며 request/node ID를 받지 않는다.
2. 같은 key는 값이 같아도 map/sort 전에 거부한다.
3. Provided `D`와 `U`를 각각 nonnegative integer meter/second
   `Present` 또는 typed `Absent`로 봉인한다. Decimal/exponent/overflow는 거부한다.
4. Legacy `C`는 raw provenance에만 남기고 travel meaning, generation branch와
   semantic fingerprint에 넣지 않는다. Raw/envelope identity는 exact bytes 차이를
   계속 반영한다.
5. Coordinate/speed는 approved validation policy 아래 `PresentValid` 또는 `Missing`으로
   봉인한다. Present-but-invalid/non-positive speed와 invalid coordinate는 missing으로
   바꾸지 않고 거부한다.
6. Sparse arc는 versioned external-location comparator로 정렬하고 source identity,
   adapter/policy identity와 typed absence를 artifact에 포함한다.
7. Phase 02가 raw bytes/DTO 없이 아래 준비 작업을 수행할 수 있는지 consumer fixture로
   확인한다.

**Phase 02만 소유하는 일**

- Self pair를 authoritative `0m/0s`로 override
- Missing `D`의 approved Great Circle 생성과 meter `HALF_UP`
- Missing `U`의 vehicle-resolved 생성과 missing speed `45 km/h` 적용
- 모든 directed physical-location `M²`와 사용 vehicle time coverage
- Provided/generated priority, complete `PreparedTravel`, dense IDs와
  `ProblemInstance`

Phase 01은 self override, `45 km/h`, Great Circle 함수/version, coordinate precision,
reverse/symmetric fallback 또는 vehicle-resolved time을 계산하거나 default로
채우지 않는다.

**FUTURE RED와 exact oracle**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am \
  -Dtest=TravelInputDeclarationNormalizerTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  clean test
```

- `preservesDistancePresentAndTimeAbsent`
- `preservesTimePresentAndDistanceAbsent`
- `preservesBothAbsentWithoutNumericSentinel`
- `legacyCChangesRawEnvelopeProvenanceButNotTravelMeaning`
- `rejectsDecimalOrOverflowingProvidedDistanceAndTime`
- `rejectsDuplicateDirectedKeyBeforeCanonicalOrdering`
- `ordersSparseKeysByVersionedExternalLocationComparator`
- `distinguishesCoordinatePresentValidMissingAndInvalid`
- `distinguishesSpeedPresentPositiveMissingAndInvalid`
- `doesNotApplySelfOverrideGreatCircleOr45KmhDefault`

Oracle은 `(from,to,D presence/value,U presence/value,source)` tuple을 hand-written
expected로 비교한다. Invalid case는 exact code/path와 artifact 없음, `C` variation은
same semantic fingerprint + different raw/envelope identity를 요구한다.
`E-P01-NUMERIC`에는 integer/overflow report, `E-P01-ERROR`에는
duplicate/reference/source/typed-absence report, handoff manifest에는
`REQ-P01-TRAVEL-HANDOFF` consumer fixture digest를 둔다.

**실패와 rollback/handoff**

Invalid 값을 `Missing`으로 바꾸거나 generation 값을 미리 만들면 Phase boundary
defect다. Travel declaration change만 마지막 green point로 되돌리고 raw DTO fallback을
허용하지 않는다. 성공 handoff는 typed sparse declaration, coordinate/speed source
facts, policy identity와 known Phase 02 blockers다.

### 9.5 WP-01.4 — Time, service, trip, wait와 route-resource declaration

**목적과 이유**

Plan/window 경계, service sum과 single-trip 의미를 route propagation 전에 하나로
고정한다.

**사전조건**

- WP-01.3 checked seconds와 service arithmetic
- Time/window schema의 반복/overnight 의미 승인
- `waitInDepot`/route-resource의 versioned field shape, requiredness, alias와
  unknown-field policy 승인

Exact `waitInDepot=N/Y`, depot task/duration과 route-resource semantics 자체는
`Q-IN-02`로 이미 resolved됐으며 재승인 사전조건이 아니다.

**예상 file/package/type**

```text
rpdptw/core/.../normalization/
  NormalizedPlanEnvelope
  NormalizedWindow
  CustomerWindowPolicyDeclaration
  WorkArcPolicy
  DepotWaitPolicy
  TripPolicy
  NormalizedDepotDeclaration
  RouteResourceSemanticPolicy
  NormalizedRouteResourceLimits
```

**구체 행동**

1. Exact `yyyy-MM-dd HH:mm:ss`만 읽고 offset/zone을 거부한다.
2. `planStart`를 origin 0으로 하여 checked `long` seconds로 변환한다.
3. Plan은 `[0, planDuration)`, raw window close inclusive를 보존한다.
4. 내부 half-open 표현을 선택하면 checked `close+1` 후 plan end로 clip한다.
5. Date 없는 window를 plan 날짜마다 펼치고 overnight를 하나의 연속 창으로 만든다.
6. `open==close`는 승인된 schema 의미가 없으면 거부한다.
7. Service time을 `duration + Σ(item.taskTime×qty)`로 checked 계산한다.
8. Order-level `taskTime`을 거부하고 `reqDate/dueDate` equality를 검사한다.
9. Customer window policy의 explicit declaration 또는 typed omission을 보존한다.
   `START_ONLY`/`COMPLETE_WITHIN_WINDOW`를 Phase 01 hidden profile default로 bind하지
   않는다. Phase 04가 exact profile을 bind하고 Phase 03이 serviceStart/serviceEnd
   constraint를 적용한다.
10. Depot `taskTime`은 non-applied raw provenance로, `depot.duration`은 future
    rotation-between-trips declaration으로 보존한다. 현재 oneway/single-roundtrip의
    최초 출발·최종 복귀 service에 더하지 않는다.
11. Exact `waitInDepot=N`을 customer-wait policy, exact `Y`를 depot-shift policy로
    mapping한다. Missing/unknown/alias 동작은 승인된 wire policy만 따르고 hidden
    default를 만들지 않는다.
12. Oneway/single roundtrip과 raw rotation provenance를 typed value로 만든다.
13. Vehicle/global route limit을 서로 독립인 present/absent declaration으로 보존하고,
    다음 semantic policy identity를 함께 봉인한다.
    - Stop은 직전 customer service location과 달라지는 transition만 세고 depot,
      logical pickup은 세지 않는다.
    - Drive distance/time은 actual traversed arc의 `D`/vehicle-resolved `U`만 센다.
    - Stop/drive resource는 route 전체 누적이며 날짜·근무창·rest에서 reset하지 않는다.
14. Full-arc next-window rule은 policy와 handoff tuple만 보존한다. 실제 departure,
    wait, stop/drive accumulation, vehicle/global `min`, reset/feasibility 계산은 Phase
    03으로 넘긴다.

**선택 근거**

Phase 03이 문자열/date/default를 다시 해석하지 않고 normalized seconds와 policy만
받게 한다.

**금지 shortcut**

- Plan end와 window close를 같은 boundary로 취급
- Close 1초 감소
- `open==close`를 임의 24시간/빈 창으로 해석
- Timezone/DST/locale 추정
- Full arc를 중간 pause/resume
- Missing limit를 0/무한/sentinel로 치환
- `waitInDepot`의 hidden default
- Exact `N/Y` mapping을 OPEN으로 되돌리거나 반대로 mapping
- Depot `taskTime`을 service에 더하거나 single-trip에 depot `duration` 적용
- Phase 01에서 window profile bind, actual departure/stop/drive resource 계산

**FUTURE RED와 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am \
  -Dtest=TimeNormalizerTest,ServiceTimeNormalizerTest,TripPolicyNormalizerTest,RouteResourceNormalizerTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  clean test
```

**필수 test 후보**

- `convertsExactLocalDateTimeToPlanOriginSeconds`
- `rejectsOffsetOrZone`
- `keepsPlanEndExclusiveAndCloseInclusive`
- `expandsOvernightWindowOncePerPlanDate`
- `rejectsEqualOpenCloseWithoutSchemaMeaning`
- `preservesFullArcRestartPolicyAndExactHandoffOracle`
- `checksServiceDurationItemTimeQuantitySum`
- `rejectsOrderLevelTaskTime`
- `preservesWindowPolicyDeclarationWithoutBindingProfileDefault`
- `preservesDepotTaskTimeAsNonAppliedProvenance`
- `preservesDepotDurationForFutureRotationOnly`
- `onewayIgnoresRotationButRecordsRawValue`
- `mapsExactNToCustomerWaitAndYToDepotShift`
- `doesNotTreatMissingWaitFieldAsResolvedDefault`
- `rejectsUnknownDepotWaitPolicy`
- `rejectsNonOnewayRotation`
- `preservesLocationTransitionStopSemanticIdentity`
- `preservesActualArcDriveResourceSemanticIdentity`
- `preservesRouteTotalNoResetSemanticIdentity`
- `keepsVehicleAndGlobalLimitsIndependentUntilPhase03`
- `preservesMissingRouteLimitsAsTypedAbsence`
- `rejectsNegativeOrFractionalRouteResourceLimit`

**기대 결과**

Expected interval/seconds/policy 또는 typed rejection이 hand oracle과 exact match한다.

**실패 해석과 rollback**

Partial window expansion은 발행하지 않는다. Time sentinel로 계속 진행하지 않고
전체 draft를 폐기한다.

**다음 handoff**

`E-P01-TIME` candidate와 Phase 03/04가 소비할 window/depot/wait/full-arc/resource
semantic handoff fixture.

### 9.6 WP-01.5 — Size, capability, zone와 ownership

**목적과 이유**

Vehicle size, 업무 capability, zone과 ownership을 한 문자열 축이나 outcome으로
혼동하지 않게 한다.

**사전조건**

- WP-01.2 identity/reference rule
- Approved legacy/new feature alias shape

**예상 file/package/type**

```text
rpdptw/core/.../normalization/
  NormalizedCompatibilityInput
  AllowedVehicleSizes
  VehicleSizeCode
  CapabilityCode
  ZoneCode
  VehicleOwnership
```

**구체 행동**

1. Vehicle size는 concrete non-empty code 하나만 허용한다.
2. Request size는 concrete code 1개 이상 또는 exact `["ALL"]`만 허용한다.
3. Code는 case-sensitive free-form이며 톤급/순서를 추론하지 않는다.
4. New/legacy list alias가 함께 있으면 exact list equality를 요구한다.
5. Capability는 request set이 vehicle set의 subset인지 판단할 raw fact를 보존한다.
6. Missing zone은 `ALL`, concrete route zone은 최대 하나라는 fact를 보존한다.
7. Real pickup/delivery size는 교집합, 서로 다른 concrete zone은 static
   unassignability fact로 보존한다.
8. Ownership은 missing/null/empty=`DIRECT`, exact `DIRECT/LEASE`, 나머지 reject다.

**선택 근거**

Static compatibility를 deterministic하게 만들되, eligible vehicle 0개를 malformed
input이나 최종 outcome으로 오인하지 않게 한다.

**금지 shortcut**

- `Feature` 숫자에서 vehicle class 순서 추론
- Size/capability/zone을 generic feature list 하나로 합침
- Vehicle `ALL` 허용
- Compatible vehicle 0개를 input reject 또는 final `PROVEN`으로 승격
- `LEASE`를 `OUTSOURCED` outcome으로 사용

**FUTURE RED와 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am \
  -Dtest=CompatibilityNormalizerTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  clean test
```

**필수 test 후보**

- `keepsFreeFormSizeCodeCaseSensitive`
- `acceptsOnlyExactAllAlternative`
- `rejectsMixedAllAndConcreteCode`
- `rejectsVehicleAllOrBlank`
- `requiresLegacyAndNewFeatureListsToMatchExactly`
- `computesRealPairSizeIntersectionWithoutGuessing`
- `preservesNoEligibleVehicleAsValidNormalizedFact`
- `normalizesMissingZoneToAll`
- `preservesConflictingConcretePickupDeliveryZonesAsStaticUnassignabilityFact`
- `normalizesOwnershipStrictly`

**기대 결과**

Independent set/intersection oracle와 exact fact/error가 일치한다.

**실패 해석과 rollback**

Fallback registry나 fuzzy matching을 추가하지 않는다. Draft를 폐기하고 fixture/contract
불일치인지 implementation defect인지 먼저 분리한다.

**다음 handoff**

`E-P01-COMPAT` candidate report.

### 9.7 WP-01.6 — Artifact sealing, typed error, fingerprint, security와 architecture guard

**목적과 이유**

정상·실패 결과, immutable snapshot, identity와 dependency violation을 한 exit
boundary에서 닫는다.

**사전조건**

- WP-01.1A와 WP-01.1~01.5(01.3A 포함) focused suite green
- Canonical encoding은 versioned proposed policy로 명시
- PII classification/canary strategy review

**예상 file/package/type**

```text
rpdptw/core/.../normalization/
  CanonicalInputNormalizer
  NormalizationResult
  NormalizedInputArtifact
  InputProblem
  InputRejectionReport
  CanonicalFingerprint

build/architecture-rules/.../
  Phase01DependencyRulesTest
```

**구체 행동**

1. 모든 collection을 deep defensive copy하고 mutable view를 노출하지 않는다.
2. Raw, semantic, envelope identity를 분리하고 algorithm/version을 기록한다.
3. Problems를 deterministic total order로 모은다.
4. Failure는 partial artifact를 절대 반환하지 않는다.
5. Raw input bytes, address/email canary와 secret-like value가 report/log/evidence에
   없는지 검사한다.
6. Core가 Jackson/cloud/HTTP/solver/verifier를 참조하지 않는지 architecture rule로 막는다.
7. Normalization output에 `PreparedTravel`, `ProblemInstance`, route/score가 없는지
   package/bytecode rule로 막는다.

**선택 근거**

Phase 02와 replay가 신뢰할 수 있는 immutable boundary를 만들고, 설명만 있는 보안
요구를 실행 가능한 failure detector로 바꾼다.

**금지 shortcut**

- `Collections.unmodifiableList`만 씌우고 내부 element/array alias 유지
- Object `hashCode()` 또는 JVM iteration order를 fingerprint로 사용
- Error에 raw value/full bytes 포함
- Failure 뒤 partially normalized object 노출
- Core에 Jackson annotation 편의 추가

**FUTURE RED와 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core,adapters/common,build/test-fixtures,build/architecture-rules \
  -am clean test
```

**필수 test 후보**

- `artifactDefensivelyCopiesAllCollections`
- `sameMeaningAndPolicyHasSameSemanticFingerprint`
- `profilePresetMandatoryResourceMeaningChangesSemanticFingerprint`
- `aliasProvenanceChangesEnvelopeNotMeaning`
- `rejectionNeverExposesPartialArtifact`
- `rejectionEvidenceRedactsRawValuesAndInputBytes`
- `coreDoesNotDependOnJacksonCloudSolverOrVerification`
- `normalizationDoesNotCreatePreparedTravelOrProblemInstance`
- `adapterDoesNotDependOnSolver`
- `coreDoesNotDependOnAdapterOrJackson`
- `adapterTestFixtureWiringIsTestScopeOnlyAndAcyclic`

**기대 결과**

Sealed success 또는 ordered rejection만 존재하고 forbidden dependency/output edge가 0이다.

**실패 해석과 rollback**

Fingerprint encoding을 바꾸면 같은 identity에 다른 bytes를 덮어쓰지 않는다. Encoding
version을 올리거나 변경을 되돌리고 migration/replay review를 요청한다.

**다음 handoff**

`E-P01-ERROR` candidate와 complete `NormalizedInputArtifact` candidate.

### 9.8 WP-01.7 — Integration, evidence seal, independent review와 Phase 02 handoff

**목적과 이유**

Focused green을 실제 Phase acceptance로 연결하고 false-green/가짜 evidence를 막는다.

**사전조건**

- WP-01.1A와 WP-01.1~01.6(01.3A 포함) green
- Required test skipped/failed 0
- Public/open limitations 문서화
- Phase 02 consumer contract 준비

**예상 target**

- Phase 01 evidence bundle
- Pre-review evidence manifest
- Independent review report
- Post-review acceptance receipt
- Handoff manifest

**구체 행동**

1. Focused suite와 architecture suite의 command, environment, exit code와 test count를
   기록한다.
2. 승인된 exit-required catalog와 manifest가 양방향 exact set인지 먼저 확인하고,
   모든 owner class/method가 fresh Surefire/Failsafe XML에 존재하며 test count가
   0이 아니고 failure/error/skipped가 0인지 확인한다.
3. Full target reactor verify와 root verify를 실행한다.
4. `E-P01-NUMERIC/TIME/COMPAT/ERROR`를 content-addressed artifact로 봉인한다.
5. Pre-review manifest를 allowlist field만으로 봉인한다.
6. Independent reviewer는 그 manifest digest만 입력으로 review report를 봉인한다.
7. Exit gate와 review가 통과한 뒤 acceptance authority가 별도 receipt를 발행한다.
8. Phase 02에 artifact schema/version/fingerprint, negative fixture, limitation과 rollback
   point를 넘긴다.

**선택 근거**

Focused test, full regression, immutable evidence, 독립 review와 acceptance authority를
분리해야 code green을 Phase acceptance로 잘못 승격하는 false-green을 막을 수 있다.

**조건부 미래 명령**

Core/test seam은 Phase 00 receipt, adapter selector는 WP-01.1A 승인 뒤에만 사용한다.

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core,adapters/common,build/test-fixtures,build/architecture-rules \
  -am clean verify
./mvnw -B -ntp -Dstyle.color=never clean verify
```

현재 live parent는 Surefire만 구성하고 Failsafe는 구성하지 않는다. 따라서 `*Test`는
Surefire report로 확인한다. Required class를 `*IT`로 이름 붙이면 accepted parent에
Failsafe `integration-test`+`verify` execution과 report directory가 먼저 추가·승인돼야
하며, `test` goal이나 Surefire XML로 그 IT 실행을 주장해서는 안 된다.

Exit-required test의 유일한 catalog는 §11.3의
`phase01-exit-required-tests-v1` marker 사이 row set이다. §9와 §11의 다른 class,
method와 fixture 이름은 `CANDIDATE` 또는 설명용 trace이며 exit catalog를 늘리거나
줄이지 않는다. Catalog의 한 method를 같은 class의 대표 method로 대체할 수 없다.

현재 adapter module, 모든 Phase 01 test FQCN/method와 Failsafe execution은 승인되지
않았다. 따라서 catalog block 전체는 `PROPOSED — ENTRY/ARCHITECTURE/TEST IDENTITY
APPROVAL REQUIRED`다. WP-01.0/01.1A에서 architecture owner와 implementation owner가
engine/module/FQCN/method identity를 승인하기 전에는 manifest를 봉인하거나 Phase exit
verifier를 green으로 주장하지 않는다. 승인 결과가 block과 다르면 같은 review change에서
block을 먼저 version-up한다. 승인 없이 proposed path/name을 production default나 stable
compatibility 약속으로 승격하지 않는다.

승인 뒤 실행별 `phase-01-required-tests.manifest`는 catalog의
`engine|module|fqcn|method` row를 생성 순서와 무관한 set으로 그대로 복사한다.
Verifier는 `catalog − manifest = ∅`와 `manifest − catalog = ∅`를 모두 확인한다.
누락뿐 아니라 extra row, wrong engine과 이름 치환도 실패다. Catalog/manifest 둘 다
UTF-8/LF, comment/blank row 없음, field 4개 exact PSV이며 다음 grammar를 만족한다.

- `engine`: exact `SUREFIRE` 또는 승인된 Failsafe execution이 있을 때만 `FAILSAFE`
- `module`: slash로 나뉜 각 segment가 `[A-Za-z0-9][A-Za-z0-9._-]*`
- `fqcn`: 점으로 나뉜 두 개 이상의 Java identifier
  `[A-Za-z_][A-Za-z0-9_]*`
- `method`: parameter display suffix를 제외한 base Java identifier
  `[A-Za-z_][A-Za-z0-9_]*`
- Catalog와 manifest 각각 exact row duplicate `0`; 한 catalog의
  `(module,fqcn)` class에 engine 둘 이상 `0`

`clean verify` 직전에 reactor 밖의 immutable run directory에 start marker를 만들고,
직후 versioned verifier를 `catalog manifest marker` 세 argument로 실행한다. Verifier는
XML filename뿐 아니라 testcase의 `classname`과 base method를 확인한다. Parameterized
display는 exact base method 뒤의 `(` 또는 `[` suffix만 허용한다. XML mtime은 start
marker보다 새로워야 하며 서로 다른 run의 XML을 합치지 않는다.

Verifier와 negative self-test의 고정 exit contract는 다음과 같다.

| Exit | 의미 | 반드시 고립 fixture로 증명할 negative control |
|---:|---|---|
| `0` | exact catalog=manifest, fresh class/method report와 모든 pass 조건 충족 | 최소 positive control 1개 |
| `64` | unknown engine | 한 row의 engine을 `JUNIT`으로 변경 |
| `65` | malformed/empty row, field 또는 XML | field 3/5개, 빈 field, 불법 module/FQCN/method 각각 |
| `66` | duplicate catalog/manifest row 또는 한 test identity의 engine 중복 | 같은 row 2회 |
| `67` | 양방향 set mismatch | required class 전체 누락, required method 한 row 누락, extra row 각각 |
| `68` | wrong engine | 같은 module/FQCN/method를 반대 engine으로 치환 |
| `69` | missing class/report | required class XML 삭제 또는 XML의 `classname` 불일치 |
| `70` | zero tests | suite `tests="0"` |
| `71` | failed test | suite/testcase failure 1 |
| `72` | error test | suite/testcase error 1 |
| `73` | skipped test | suite/testcase skipped 1 |
| `74` | missing required method | fresh class XML에서 required base method만 삭제/오명 |
| `75` | stale report | start marker보다 오래된 otherwise-green XML |

아래 `phase-01-required-test-verifier-v1`은 exit contract의 실행 기준이다. 실제 승인
change는 이 block과 byte-equivalent한 versioned script 또는 그 digest가 같은
executable artifact를 봉인한다.

```python
from collections import Counter, defaultdict
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

UNKNOWN_ENGINE = 64
MALFORMED = 65
DUPLICATE = 66
SET_MISMATCH = 67
WRONG_ENGINE = 68
MISSING_CLASS = 69
ZERO_TESTS = 70
FAILED = 71
ERROR = 72
SKIPPED = 73
MISSING_METHOD = 74
STALE = 75

ENGINES = {"SUREFIRE", "FAILSAFE"}
MODULE = re.compile(r"[A-Za-z0-9][A-Za-z0-9._-]*(/[A-Za-z0-9][A-Za-z0-9._-]*)*")
IDENT = re.compile(r"[A-Za-z_][A-Za-z0-9_]*")
FQCN = re.compile(r"[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)+")


def stop(code, message):
    print(message, file=sys.stderr)
    raise SystemExit(code)


def rows(path):
    try:
        data = Path(path).read_bytes()
        text = data.decode("utf-8")
    except (OSError, UnicodeError) as error:
        stop(MALFORMED, f"cannot read UTF-8 PSV {path}: {error}")
    if not text or not text.endswith("\n"):
        stop(MALFORMED, f"empty PSV or missing final LF: {path}")

    parsed = []
    for number, line in enumerate(text.splitlines(), 1):
        if not line or line != line.strip():
            stop(MALFORMED, f"blank/padded row {number}: {path}")
        fields = line.split("|")
        if len(fields) != 4 or any(not field for field in fields):
            stop(MALFORMED, f"field count/value at row {number}: {path}")
        engine, module, fqcn, method = fields
        if engine not in ENGINES:
            stop(UNKNOWN_ENGINE, f"unknown engine at row {number}: {engine}")
        if not MODULE.fullmatch(module):
            stop(MALFORMED, f"bad module at row {number}: {module}")
        if not FQCN.fullmatch(fqcn):
            stop(MALFORMED, f"bad FQCN at row {number}: {fqcn}")
        if not IDENT.fullmatch(method):
            stop(MALFORMED, f"bad method at row {number}: {method}")
        parsed.append(tuple(fields))

    if any(count != 1 for count in Counter(parsed).values()):
        stop(DUPLICATE, f"duplicate row: {path}")

    return parsed


def require_one_engine_per_class(parsed, path):
    class_engines = defaultdict(set)
    for engine, module, fqcn, _method in parsed:
        class_engines[(module, fqcn)].add(engine)
    if any(len(engines) != 1 for engines in class_engines.values()):
        stop(DUPLICATE, f"class assigned to multiple engines: {path}")


if len(sys.argv) != 4:
    stop(MALFORMED, "usage: verifier CATALOG MANIFEST RUN_START_MARKER")

catalog = rows(sys.argv[1])
manifest = rows(sys.argv[2])
catalog_by_test = {(module, fqcn, method): engine
                   for engine, module, fqcn, method in catalog}
manifest_by_test = {(module, fqcn, method): engine
                    for engine, module, fqcn, method in manifest}

require_one_engine_per_class(catalog, sys.argv[1])
for identity in catalog_by_test.keys() & manifest_by_test.keys():
    if catalog_by_test[identity] != manifest_by_test[identity]:
        stop(WRONG_ENGINE, f"wrong engine: {identity}")
require_one_engine_per_class(manifest, sys.argv[2])
if set(catalog) != set(manifest):
    stop(SET_MISMATCH, "catalog/manifest bidirectional set mismatch")

marker = Path(sys.argv[3])
try:
    marker_mtime = marker.stat().st_mtime_ns
except OSError as error:
    stop(MALFORMED, f"missing run marker: {error}")

required_by_class = defaultdict(set)
for engine, module, fqcn, method in catalog:
    required_by_class[(engine, module, fqcn)].add(method)

for (engine, module, fqcn), methods in sorted(required_by_class.items()):
    report_kind = "surefire-reports" if engine == "SUREFIRE" else "failsafe-reports"
    report = Path(module) / "target" / report_kind / f"TEST-{fqcn}.xml"
    try:
        if report.stat().st_mtime_ns <= marker_mtime:
            stop(STALE, f"stale report: {report}")
        root = ET.parse(report).getroot()
    except FileNotFoundError:
        stop(MISSING_CLASS, f"missing class report: {report}")
    except (OSError, ET.ParseError) as error:
        stop(MALFORMED, f"malformed class report {report}: {error}")

    def count(name):
        try:
            return int(root.attrib[name])
        except (KeyError, ValueError):
            stop(MALFORMED, f"missing/non-integer {name}: {report}")

    if count("tests") <= 0:
        stop(ZERO_TESTS, f"zero tests: {report}")
    if count("failures") != 0:
        stop(FAILED, f"failed tests: {report}")
    if count("errors") != 0:
        stop(ERROR, f"error tests: {report}")
    if count("skipped") != 0:
        stop(SKIPPED, f"skipped tests: {report}")

    testcases = [
        element for element in root.iter()
        if element.tag.rsplit("}", 1)[-1] == "testcase"
    ]
    class_cases = [
        element for element in testcases
        if element.attrib.get("classname") == fqcn
    ]
    if not class_cases:
        stop(MISSING_CLASS, f"classname absent from fresh XML: {fqcn}")

    for element in class_cases:
        child_tags = {child.tag.rsplit("}", 1)[-1] for child in element}
        if "failure" in child_tags:
            stop(FAILED, f"failed testcase in {report}")
        if "error" in child_tags:
            stop(ERROR, f"error testcase in {report}")
        if "skipped" in child_tags:
            stop(SKIPPED, f"skipped testcase in {report}")

    names = {element.attrib.get("name", "") for element in class_cases}
    for method in methods:
        if not any(
            name == method or name.startswith(method + "(") or name.startswith(method + "[")
            for name in names
        ):
            stop(MISSING_METHOD, f"missing method {fqcn}#{method}")
```

Self-test driver는 각 mutation마다 verifier process를 새로 실행해 **표의 exact exit
code**를 assert하고, positive/negative fixture digest, command/start marker,
catalog/manifest/verifier/self-test bytes와 실제 exit vector
`0,64,65,66,67,68,69,70,71,72,73,74,75`를 `E-P01-ERROR`에 봉인한다. Malformed와
omitted row는 서로 다른 control이며, omitted class/method row가 단지 report 검사로
넘어가서는 안 된다. Console 마지막 명령 exit, 한 XML 또는 class당 대표 method
하나만으로 통과 판정하지 않는다.

**금지 shortcut**

- `target/` mutable file이나 console 한 줄만 evidence로 사용
- `failIfNoSpecifiedTests=false`로 owner test 0건을 성공 처리
- Surefire로 `*IT`가 실행됐다고 주장하거나 Failsafe summary/XML을 누락
- `clean` 없이 이전 run XML을 재사용
- Pre-review manifest에 reviewer/verdict/acceptance 정보 backfill
- Source/test file 존재를 `DONE`으로 표시
- Phase 02를 acceptance receipt 전에 시작

**기대 결과**

네 evidence key와 단방향 manifest→review→receipt graph가 유효하고 Phase 02가 raw
input을 재해석하지 않아도 된다.

**실패 해석과 rollback**

- Code green/evidence incomplete: `IMPLEMENTED_PENDING_EVIDENCE`
- Manifest sealed/review incomplete: `REVIEW_PENDING`
- Review failure: `FAILED` 또는 사유에 맞는 `BLOCKED`
- Acceptance receipt 불일치: `NOT_ACCEPTED`

마지막 accepted WP build/artifact로 돌아가며 evidence를 합성하거나 수정하지 않는다.

**다음 handoff**

Phase 02가 소비할 exact `NormalizedInputArtifact`와 evidence/rollback refs.

## 10. Java 설계 안내

### 10.1 Package와 dependency 방향

아래는 skeletal owner map이다.

| Package candidate | 소유할 것 | 소유하면 안 되는 것 |
|---|---|---|
| `com.ronext.rpdptw.input` | Canonical input, external identity, schema/source identity | Jackson DTO, solver node |
| `com.ronext.rpdptw.normalization` | Checked value, time/service/compatibility normalization, result/error/fingerprint | Travel completion, route, objective |
| `com.ronext.rpdptw.adapter.json.input` | External DTO, schema registry, alias mapping | Solver/search/profile binding |
| `com.ronext.rpdptw.fixture` | Independent test builder/oracle | Production source |
| `com.ronext.rpdptw.architecture` | Forbidden dependency/output rules | Business behavior |

Compile 방향은 다음 하나를 보존한다.

```text
rpdptw-core
adapters/common -COMPILE→ rpdptw-core
build/test-fixtures -TEST→ rpdptw-core test contract only
adapters/common tests -TEST→ rpdptw-test-fixtures tests classifier
build/architecture-rules -TEST→ core + adapter + fixture compiled graph
Phase 02 core domain/travel → Phase 01 NormalizedInputArtifact
```

`build/test-fixtures → adapters/common → build/test-fixtures` cycle을 만들지 않는다.
Adapter-specific builder는 adapter test source가 소유하고 shared fixture는 core-only
oracle로 유지한다. Current Architecture의 더 넓은 adapter→application/verification
allowed edge는 later consumer가 실제 필요할 때만 추가하며 Phase 01 POM에 미리 넣지
않는다.

### 10.2 Adapter와 normalizer interface 후보

다음은 method responsibility를 보여주는 뼈대이며 public API가 아니다.

```java
// PROPOSED INTERNAL
interface VersionedInputAdapter {
    boolean supports(SchemaIdentity schema, AdapterIdentity adapter);
    AdaptationResult adapt(ExternalInputDocument document);
}

sealed interface AdaptationResult {
    record Accepted(AdaptedCanonicalInput input) implements AdaptationResult {}
    record Rejected(InputRejectionReport report) implements AdaptationResult {}
}

interface CanonicalInputNormalizer {
    NormalizationResult normalize(
        AdaptedCanonicalInput input,
        NormalizationPolicySnapshot policy
    );
}

sealed interface NormalizationResult {
    record Accepted(NormalizedInputArtifact artifact)
        implements NormalizationResult {}
    record Rejected(InputRejectionReport report)
        implements NormalizationResult {}
}
```

설계 질문:

- Adapter lookup key가 schema와 adapter version 모두를 포함하는가?
- `supports()` 순회가 classpath order/first-wins fallback을 만들지 않는가?
- Exception과 expected rejection을 서로 다른 telemetry/failure type으로 구분하는가?
- Accepted record가 mutable builder나 raw DTO를 잡고 있지 않은가?

### 10.3 Canonical request와 pickup 의미 후보

```java
// PROPOSED INTERNAL skeletal contract
record CanonicalRequestInput(
    ExternalRequestId id,
    PickupInput pickup,
    CanonicalServiceInput delivery,
    List<CanonicalItemInput> items,
    CanonicalCompatibilityInput compatibility,
    Optional<CanonicalDeadlineInput> completionDeadline,
    Optional<Boolean> mandatoryDeclaration,
    Optional<ApprovedTypedExtensionInput> extensionInput
) {}

sealed interface PickupInput {
    record LogicalInitialLoad() implements PickupInput {}
    record PhysicalService(CanonicalServiceInput service) implements PickupInput {}
}
```

여기에는 `SolverNodeId`, route position과 dense array index가 없다. `LogicalInitialLoad`
는 service location/window를 가지지 않는다.

### 10.4 Value object 후보

```java
// PROPOSED INTERNAL
record MilliKilograms(long value) {}
record MilliCubicMeters(long value) {}
record Meters(long value) {}
record Seconds(long value) {}
record RawInputDigest(String algorithm, String hex) {}
record SemanticFingerprint(String encodingVersion, String algorithm, String hex) {}
```

Value constructor에서 무엇을 검증할지와 normalizer에서 무엇을 검증할지 분리한다.
예를 들어 `Seconds`가 nonnegative만 표현한다면 negative constructor는 막을 수 있지만,
“이 field가 plan duration보다 작아야 한다”는 aggregate normalizer가 판단해야 한다.

### 10.5 Time/trip/resource hierarchy 후보

```java
// PROPOSED INTERNAL
record NormalizedWindow(
    long startSecondInclusive,
    long endSecondExclusive,
    WindowSource source
) {}

enum WorkArcPolicy {
    FULL_ARC_WITHIN_ONE_WORK_WINDOW
}

sealed interface CustomerWindowPolicyDeclaration {
    record Explicit(CustomerWindowPolicy value)
        implements CustomerWindowPolicyDeclaration {}
    record Omitted() implements CustomerWindowPolicyDeclaration {}
}

enum CustomerWindowPolicy {
    START_ONLY,
    COMPLETE_WITHIN_WINDOW
}

enum DepotWaitPolicy {
    DEPART_EARLIEST_WAIT_AT_CUSTOMER,
    MOVE_EARLY_WAIT_TO_DEPOT
}

sealed interface TripPolicy {
    record OneWay() implements TripPolicy {}
    record SingleRoundTrip() implements TripPolicy {}
}

sealed interface LimitDeclaration<T>
    permits LimitDeclaration.Present, LimitDeclaration.Absent {
    record Present<T>(T value) implements LimitDeclaration<T> {}
    record Absent<T>() implements LimitDeclaration<T> {}
}

record NormalizedRouteResourceLimits(
    LimitDeclaration<StopCountLimit> maxStopCount,
    LimitDeclaration<DriveTimeLimit> maxDriveTime,
    LimitDeclaration<DriveDistanceLimit> maxDriveDistance
) {}

record NormalizedDepotDeclaration(
    NonAppliedDepotTaskTimeProvenance taskTime,
    Optional<Seconds> futureRotationDuration
) {}

record RouteResourceSemanticPolicy(
    StopCountingPolicy stopCounting,
    DriveResourcePolicy driveResource,
    ResourceResetPolicy reset
) {}
```

모든 이름은 proposed다. 그러나 mapping 의미는 CONTRACT다: exact `N`은
`DEPART_EARLIEST_WAIT_AT_CUSTOMER`, exact `Y`는 `MOVE_EARLY_WAIT_TO_DEPOT`다.
`CustomerWindowPolicyDeclaration.Omitted`는 Phase 01이 `START_ONLY`를 몰래 채우지
않게 하며, Phase 04가 default/explicit profile을 bind한다. Phase 03만 실제
first-customer departure, window feasibility, location-transition stop, actual-arc
drive resource와 route-total/no-reset를 계산한다. `futureRotationDuration`은 현재
single-trip service에 적용하지 않는다.

### 10.6 Compatibility hierarchy 후보

```java
// PROPOSED INTERNAL
sealed interface AllowedVehicleSizes {
    record All() implements AllowedVehicleSizes {}
    record Exact(SortedSet<VehicleSizeCode> values)
        implements AllowedVehicleSizes {}
}

record NormalizedCompatibilityInput(
    AllowedVehicleSizes allowedSizes,
    SortedSet<CapabilityCode> requiredCapabilities,
    ZoneCode zone
) {}

enum VehicleOwnership {
    DIRECT,
    LEASE
}
```

`SortedSet`을 그대로 받더라도 constructor가 defensive copy를 해야 한다. Comparator는
locale이나 natural `String` assumption이 아니라 versioned canonical policy를 사용한다.

### 10.7 Error hierarchy 후보

```java
// PROPOSED INTERNAL
sealed interface InputProblem {
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
    record Travel(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Compatibility(InputProblemCode code, InputPath path)
        implements InputProblem {}
    record Trip(InputProblemCode code, InputPath path)
        implements InputProblem {}
}
```

Internal code 이름도 proposed지만 다음 failure 의미는 필요하다.

- Unsupported schema/version
- Alias ambiguity
- Duplicate identity/travel key
- Dangling reference
- Invalid/fractional/negative numeric와 overflow
- Invalid date/range/window
- Order-level task time
- Invalid feature/capability/zone/ownership
- Invalid wait/resource/trip/rotation
- Unapproved extension

Wire HTTP status/error payload는 별도 OPEN contract다.

### 10.8 State transition과 pseudocode

```text
normalize(document, declaredSchema, policy):
  rawDigest = digestExactBytes(document.bytes)

  adapter = registry.resolveExact(
      declaredSchema,
      declaredAdapterVersion
  ) or reject UNSUPPORTED_SCHEMA_VERSION

  dto = adapter.parseStrictly(document.bytes)
  draft = adapter.mapApprovedFields(dto)

  problems += unknownFieldsPerExplicitPolicy(dto)
  problems += aliasConflicts(draft)
  problems += duplicatesBeforeMapOrSort(draft)
  problems += referenceFailures(draft)
  if problems not empty:
      return orderedRejected(problems)

  normalizedDraft =
      normalizeNumericTravelTimeServiceCompatibilityTripDeclarations(draft)
  # seals sparse/source/window/depot/wait/resource declarations only;
  # does not prepare travel, bind a profile, or propagate a route
  problems += checkedBoundaryFailures(normalizedDraft)
  if problems not empty:
      return orderedRejected(problems)

  ordered = orderOnlySetLikeCollections(normalizedDraft)
  semantic = fingerprint(policy + schema + adapter + ordered)
  envelope = fingerprint(rawDigest + semantic + provenance)

  return Accepted(deepSeal(
      rawDigest,
      ordered,
      policy,
      provenance,
      semantic,
      envelope
  ))
```

이 pseudocode가 말하지 않는 선택은 구현자가 임의로 채우지 않는다. 예를 들어
canonical encoding framing, digest algorithm, unknown-field default와 public error
code는 owner review가 필요하다.

## 11. 테스트 작성 안내

### 11.1 Red → green 순서

Target module/type이 아직 없으므로 다음 test는 지금 실행할 수 있는 현재 test가 아니라
Phase 00 acceptance 뒤의 FUTURE RED다.

1. Adapter contract red
2. Identity/reference/order red
3. Numeric boundary/overflow red
4. Sparse travel/source declaration red
5. Time/service/window/depot/trip/wait/resource red
6. Compatibility/ownership red
7. Artifact immutability/fingerprint/error/redaction red
8. Architecture dependency/output red
9. Adapter→normalizer integration red
10. Focused green 뒤 clean full reactor/root regression

Future test를 `@Disabled`로 추가해 green처럼 보이게 하지 않는다. Missing target type으로
compile failure가 나는 것은 구현 전 예상 red이지 현재 Phase defect evidence가 아니다.

### 11.2 Fixture, builder와 oracle

| 도구 후보 | 역할 | false-positive 방지 |
|---|---|---|
| `ExternalInputFixtureBuilder` | Exact raw bytes와 declared schema/adapter 생성 | Production DTO builder 재사용 금지 |
| `CanonicalInputFixtureBuilder` | 이미 canonical인 value 조합 | JSON parser를 우회하는 단위 test에만 사용 |
| `NormalizationOracle` | Hand decimal/BigInteger/time interval expected | Production normalizer/helper 호출 금지 |
| `TravelDeclarationOracle` | Hand `(from,to,D/U presence,source)` tuple과 `C` non-authority | Phase 02 preparer/Great Circle/default 호출 금지 |
| `Phase01FailureFixtures` | Duplicate/overflow/alias/PII corruption | Code/path/no-artifact를 모두 assert |
| Permutation generator | Set-order invariance | Fixed seed와 failing seed/shrunk case 기록 |
| Canary scanner | Raw bytes/address/email 미노출 | Error object, rendered report, captured log 전부 scan |

Oracle이 production canonical encoder와 같은 구현을 쓰면 두 코드가 함께 틀려도 green이
된다. Small hand tuple, `BigInteger`, explicit expected interval과 별도 reference
comparator를 사용한다.

### 11.3 Test class와 method 후보

다음 표는 학습·구현 배치를 위한 **CANDIDATE** 목록이다. “핵심 method 후보”의
쉼표 구분 문구, §11.4 fixture 이름과 이 표에만 있는 class/method는 exit-required
catalog가 아니다. Phase exit에 필수인지 여부는 바로 아래
`phase01-exit-required-tests-v1` row에 포함됐는지만으로 판정한다.

| Test class | 핵심 method 후보 | 주요 oracle |
|---|---|---|
| `VersionedInputAdapterContractTest` | supported/raw digest, unknown version no fallback, explicit unknown policy | Independent SHA-256 + exact code/path |
| `V1ExternalInputAdapterTest` | alias equality/conflict, order taskTime reject, profile/preset omission, extension reject | Hand canonical DTO |
| `CanonicalOrderingTest` | duplicate-before-sort, set permutation, sequence change, error order | UTF-8/versioned comparator reference |
| `FixedPointNormalizerTest` | scale floor, item-first, decimal integer-only reject, overflow | Decimal-string/`BigInteger` hand oracle |
| `TravelInputDeclarationNormalizerTest` | `D/U` presence/absence, `C` non-authority, coordinate/speed state, no preparation | Hand travel declaration tuple |
| `TimeNormalizerTest` | origin seconds, plan/window boundary, overnight, equal-open-close, window policy declaration, full-arc handoff | Explicit interval/policy tuple |
| `ServiceTimeNormalizerTest` | duration+item sum, overflow, order taskTime reject | Checked hand arithmetic |
| `TripPolicyNormalizerTest` | depot task/duration provenance, oneway, rotation reject, exact N/Y wait | Expected sealed type/code |
| `RouteResourceNormalizerTest` | typed absence, vehicle/global independence, location-transition/actual-arc/route-total identity | Hand present/absent + semantic-policy table |
| `CompatibilityNormalizerTest` | size/ALL/alias/intersection/zone/ownership | Independent set functions |
| `NormalizedInputArtifactTest` | deep immutability, identity layers, no partial, redaction | Mutation attempts + canary absence |
| `Phase01DependencyRulesTest` | core/adapter/output forbidden edges | Compiled class/dependency graph |
| `Phase01AdapterNormalizationContractTest` | accepted adapter output seals; rejected input never reaches Phase 02 seam | End-to-end hand fixture |

`V1`은 승인된 schema 이름으로 바꾼다. Public schema가 OPEN인 동안 이 이름을 외부
호환성 약속으로 문서화하지 않는다.

`phase01-exit-required-tests-v1`의 **유일한 catalog specification**은 아래 text
fence의 content다. HTML marker와 fence delimiter는 catalog bytes가 아니다. 현재
block의 engine/module/FQCN/method는 모두 `PROPOSED`이며 approved runtime identity가
아니다. Phase 00 receipt, adapter placement와 test identity approval이 닫힌 같은
review change에서 이 block을 byte-exact 승인하거나 version-up해야 한다. 승인된 source
catalog의 저장 path도 그 change에서 정하며, 현재 가이드가 임의 path/default를 만들지
않는다. 실행 manifest는 승인된 이 block의 exact row set에서만 생성한다.

<!-- phase01-exit-required-tests-v1:begin -->
```text
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.VersionedInputAdapterContractTest|preservesSupportedVersionAndRawDigest
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.VersionedInputAdapterContractTest|rejectsUnknownSchemaVersionWithoutFallback
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.VersionedInputAdapterContractTest|preservesExplicitUnknownFieldPolicy
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.V1ExternalInputAdapterTest|preservesEquivalentAliasAndRejectsConflict
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.V1ExternalInputAdapterTest|rejectsOrderTaskTime
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.V1ExternalInputAdapterTest|preservesProfileAndPresetOmission
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.V1ExternalInputAdapterTest|rejectsUnapprovedExtension
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CanonicalOrderingTest|rejectsDuplicateBeforeSorting
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CanonicalOrderingTest|canonicalizesSetPermutation
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CanonicalOrderingTest|preservesMeaningfulSequenceChange
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CanonicalOrderingTest|ordersErrorsDeterministically
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.FixedPointNormalizerTest|floorsWeightAtThirdDecimal
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.FixedPointNormalizerTest|floorsEachItemBeforeQuantityMultiplication
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.FixedPointNormalizerTest|rejectsIntegerLookingDecimalTravelValue
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.FixedPointNormalizerTest|rejectsQuantityAndSumOverflow
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|preservesDistancePresentAndTimeAbsent
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|preservesTimePresentAndDistanceAbsent
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|keepsLegacyCOutsideTravelAuthority
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|preservesCoordinateAndSpeedStateWithoutDefaults
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|rejectsDuplicateTravelKey
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|rejectsInvalidCoordinateOrSpeed
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TravelInputDeclarationNormalizerTest|doesNotPrepareTravel
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TimeNormalizerTest|normalizesPlanAndWindowBoundary
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TimeNormalizerTest|expandsOvernightWindows
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TimeNormalizerTest|preservesWindowPolicyDeclarationOrOmission
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TimeNormalizerTest|preservesFullArcHandoffWithoutPropagation
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.ServiceTimeNormalizerTest|sumsDurationAndItemServiceExactly
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.ServiceTimeNormalizerTest|rejectsServiceOverflowAndOrderTaskTime
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TripPolicyNormalizerTest|preservesDepotTaskAndDurationProvenanceWithoutApplication
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TripPolicyNormalizerTest|mapsExactNAndYWaitSemantics
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TripPolicyNormalizerTest|rejectsRotationWhileDeferred
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.TripPolicyNormalizerTest|preservesOnewayDeclaration
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.RouteResourceNormalizerTest|preservesTypedAbsenceAndIndependentLimits
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.RouteResourceNormalizerTest|rejectsNegativeOrFractionalLimits
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.RouteResourceNormalizerTest|preservesLocationTransitionActualArcAndRouteTotalSemantics
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CompatibilityNormalizerTest|normalizesSizeCapabilityZoneAndOwnership
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CompatibilityNormalizerTest|rejectsAllFeatureMix
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.CompatibilityNormalizerTest|recordsZeroEligibleAsStaticFact
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.NormalizedInputArtifactTest|isDeeplyImmutable
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.NormalizedInputArtifactTest|separatesRawSemanticAndEnvelopeIdentity
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.NormalizedInputArtifactTest|rejectsPartialArtifactOnFailure
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.NormalizedInputArtifactTest|redactsCanariesFromErrorReportAndLog
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.NormalizedInputArtifactTest|changesFingerprintOnMeaningfulPolicyOrSequence
SUREFIRE|rpdptw/core|com.ronext.rpdptw.normalization.NormalizedInputArtifactTest|keepsFingerprintStableAcrossSetPermutationAndLocale
SUREFIRE|build/architecture-rules|com.ronext.rpdptw.architecture.Phase01DependencyRulesTest|coreDoesNotDependOnAdapterOrJackson
SUREFIRE|build/architecture-rules|com.ronext.rpdptw.architecture.Phase01DependencyRulesTest|coreDoesNotDependOnCloudHttpSolverOrVerifier
SUREFIRE|build/architecture-rules|com.ronext.rpdptw.architecture.Phase01DependencyRulesTest|phase01DoesNotProducePreparedTravelProblemRouteOrScore
SUREFIRE|build/architecture-rules|com.ronext.rpdptw.architecture.Phase01DependencyRulesTest|testFixturesDoNotLeakOrCycle
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.Phase01AdapterNormalizationContractTest|sealsAcceptedAdapterOutput
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.Phase01AdapterNormalizationContractTest|rejectedInputNeverReachesPhase02
SUREFIRE|adapters/common|com.ronext.rpdptw.adapter.json.input.Phase01AdapterNormalizationContractTest|preservesRequiredPhase02HandoffFields
```
<!-- phase01-exit-required-tests-v1:end -->

Fence content는 51 unique row / 13 class이고 UTF-8 bytes SHA-256은
`471183deffe508e429ffddf8c4c825bb5c25d4d7b103f4f8935f5e48122859b2`다. 이 digest도
approval과 pre-review evidence에 포함한다.

한 required behavior를 다른 row가 우연히 덮는다고 간주하지 않는다. Catalog row의
삭제·병합·rename, engine/module/class 이동은 test-only refactor가 아니라 catalog
version change이며 owner approval, 양방향 set oracle과 negative self-test 재봉인이
필요하다.

### 11.4 필수 negative/boundary fixture

| Fixture | 입력 | Exact pass 판정 |
|---|---|---|
| `item-first-difference` | weight `0.0009`, qty `2` | normalized `0`; line-first `1`이면 fail |
| `scale-boundary` | `1.2340`, `1.2349` | 둘 다 `1234` milli-unit |
| `integer-looking-decimal` | distance/time `1.0` | `FRACTION_NOT_ALLOWED`, exact path |
| `quantity-overflow` | near `Long.MAX_VALUE` × 2 | `ARITHMETIC_OVERFLOW`, artifact 없음 |
| `sum-overflow` | valid item들의 sum overflow | deterministic request/item path |
| `duplicate-request` | delivery-only와 real request same ID | sort/map 전 `DUPLICATE_IDENTITY` |
| `duplicate-arc` | same `(A,B)` twice, same value | `DUPLICATE_TRAVEL_KEY`, silent dedupe 없음 |
| `sparse-distance-only` | `(A,B)`의 integer `D` present, `U` absent | meter value/source + typed time absence |
| `sparse-time-only` | `(A,B)`의 `D` absent, integer `U` present | typed distance absence + common-second value/source |
| `legacy-c-only` | `(A,B)`의 `D/U` absent, `C` present | travel meaning 없음; raw/envelope만 다르고 semantic 동일 |
| `coordinate-speed-state` | present-valid, missing, present-invalid 각 1건 | valid/missing typed; invalid exact reject; 45/default 생성 없음 |
| `alias-conflict` | normalized `reqDate != dueDate` | `AMBIGUOUS_ALIAS` |
| `ordering-permutation` | set-like raw order만 다름 | same semantic, raw digest may differ |
| `meaningful-order-change` | pickup/delivery sequence swap | 다른 meaning 또는 structural reject |
| `plan-boundary` | event at planEnd, service start at close | planEnd 제외, close start 허용 표현 |
| `overnight` | `22:00→02:00`, 3-day plan | exact clipped intervals |
| `full-arc-handoff` | remainder 5s, travel 6s, next start 100s | policy + restart tuple 100; propagation은 하지 않음 |
| `window-policy-omission` | profile window policy omitted | typed omission; Phase 01 `START_ONLY` 값 주입 없음 |
| `depot-task-duration` | depot `taskTime=7`, `duration=11`, single roundtrip | taskTime non-applied provenance, duration future-rotation only, service 합산 0 |
| `wait-n-y` | 동일 first customer에 exact `N`/`Y` | N=earliest/customer wait, Y=depot-shift semantic type; departure 계산 없음 |
| `route-resource-absence` | limit omitted | typed absence, sentinel 없음 |
| `route-resource-invalid` | negative/fractional limit | typed reject, artifact 없음 |
| `route-resource-semantics` | same-location, `A→B→A`, wait/service/rest 포함 | location-transition stop, actual-arc drive, route-total/no-reset policy identity만 handoff |
| `profile-preset-omission` | exact profile, preset omitted | omission 보존, Phase 01 default 없음 |
| `unapproved-extension` | unknown type/version extension | typed reject |
| `pii-redaction` | invalid input에 address/email canary | report/log/evidence에 canary/raw bytes 없음 |
| `zone-conflict` | pickup zone A, delivery zone B | valid static unassignability fact |
| `feature-all-mix` | `["ALL","T1"]` | invalid feature list |
| `decimal-win-travel` | raw fixture style decimal `D/U` | negative fixture, official success 아님 |

### 11.5 Test category별 적용 여부

| Category | Phase 01 적용 | 이유와 pass 판정 |
|---|---|---|
| Unit/boundary | **필수** | Numeric/travel/time/service/compatibility의 exact value/code/path |
| Property | **필수** | Permutation invariance, deterministic error order, set/intersection |
| Adapter contract | **필수** | Exact version, alias, unknown policy, raw digest |
| Module integration | **필수** | External bytes→canonical→normalized sealed/rejected |
| Architecture | **필수** | Core SDK/Jackson/solver 의존 0, Phase 02 output 0 |
| Fault injection | **적용** | Parser/normalizer exception, overflow, cancellation-like interruption 뒤 partial artifact 0 |
| Corruption | **적용** | Duplicate, dangling ref, mutated collection, digest/provenance mismatch reject |
| Reproducibility | **적용** | 같은 meaning/policy의 semantic fingerprint/error order 반복 일치 |
| Security | **필수** | PII/raw bytes/secret canary가 report/log/evidence에 없음 |
| Performance | **구조적 검사만** | 공식 threshold는 OPEN. 대표 large collection에서 accidental quadratic/overflow를 계측할 수 있으나 production SLA 주장 금지 |
| Application E2E | **이 Phase exit에는 비적용** | HTTP/storage/local runner는 Phase 08 소유. 대신 adapter-core integration까지만 |
| Provider integration | **비적용** | Cloud SDK/provider는 Phase 01 core 범위 밖 |
| Solver quality/benchmark | **비적용** | Phase 06/14A 소유. Parse 성공을 quality evidence로 사용 금지 |
| Phase 03 propagation | **handoff fixture만** | Actual full-arc departure/rest/feasibility는 Phase 03 test에서 green |
| Phase 02 travel preparation | **handoff fixture만** | Self `0/0`, missing generation, `M²`, vehicle-resolved time은 Phase 02에서 green |
| Phase 04 profile binding | **handoff fixture만** | Window policy omission/default와 profile selection binding은 Phase 04에서 green |

### 11.6 명령과 pass 판정

현재 실제 project를 확인하는 명령:

```bash
mvn -version
```

Correction live candidate의 root build health를 별도 owner가 확인할 때의 command:

```bash
./mvnw -B -ntp -Dstyle.color=never verify
```

이 명령은 미승인 Phase 00 candidate를 검증할 뿐 Phase 00 acceptance나 Phase 01
evidence가 아니다. 이 correction에서는 실행하지 않았다.

Target reactor가 승인·생성된 뒤 focused 명령은 각 WP의 `-pl ... -am`을 사용한다.
최종 pass는 다음을 모두 요구한다.

1. Maven exit code 0.
2. Owner POM의 test-fixture dependency가 exact `test-jar`/`tests`/`test` scope이고
   dependency tree에 cycle/production leakage가 없음.
3. `*Test`는 Surefire, `*IT`는 approved Failsafe `integration-test`+`verify`에서
   발견되며 다른 engine report로 대체하지 않음.
4. 승인된 `phase01-exit-required-tests-v1` catalog와 실행 manifest의 양방향
   set difference가 `0/0`이고 duplicate/unknown/wrong engine, malformed/omitted
   row가 0.
5. `clean verify` start marker 뒤 required owner XML report와 exact `classname`이
   모두 새로 존재.
6. 각 required class의 suite tests 수가 1 이상이고 failed/error/skipped가 모두 0.
7. Catalog의 **모든** required base method testcase record가 존재하며 같은 class의
   대표 method로 대체되지 않음.
8. §9.8 verifier self-test가 positive `0`, unknown engine `64`, malformed `65`,
   duplicate `66`, omitted/extra set `67`, wrong engine `68`, missing class `69`,
   zero `70`, failed `71`, error `72`, skipped `73`, missing method `74`, stale
   report `75`의 exact vector를 재현.
9. Architecture violation 0.
10. Focused clean verify와 root clean verify가 모두 green.
11. Property failure seed/shrunk case가 생기면 evidence에 기록.
12. Test-only value가 production default/config에 없음.

## 12. 사람 checkpoint, evidence bundle과 stop/resume

### 12.1 Checkpoint A — Source/entry review

시점: WP-01.0 종료.

사람이 묻는 질문:

- Phase 00가 정말 `ACCEPTED`인가, 문서 review만 통과했는가?
- Module/test-fixture classifier/scope와 exact command가 승인됐는가?
- Phase 00 core-only handoff 뒤 adapter placement/aggregator/DAG 변경 owner가
  승인했는가?
- Public schema/alias/unknown-field 정책은 production adapter를 구현할 만큼 승인됐는가?
- HEAD/accepted/live source fingerprint를 분리했고 overlap이 없는가?

Stop 조건:

- `E-P00-*`/receipt 누락
- Accepted source commit 불일치
- Adapter placement/POM/DAG/architecture wiring 승인 누락
- Phase 01 소유 path와 미커밋 다른 작업 overlap
- Schema owner 승인 누락

Resume 조건:

- 누락 evidence/approval가 immutable ref로 제공되고 impact review 완료

### 12.2 Checkpoint B — Meaning review

시점: WP-01.2, WP-01.4, WP-01.5의 red fixture가 준비됐을 때.

사람이 묻는 질문:

- Set-like와 ordered sequence를 잘 나눴는가?
- Window close/plan end/service deadline을 구분했는가?
- Sparse travel의 `D/U` typed absence/source와 `C` non-authority를 보존했는가?
- Coordinate/speed invalid를 missing/default로 바꾸지 않았는가?
- Exact N/Y, depot task/duration, location-transition/actual-arc/route-total semantics를
  보존했는가?
- Window declaration은 Phase 01, binding은 Phase 04, 계산은 Phase 03으로 나뉘는가?
- Profile omission, mandatory/extension과 resource absence를 보존했는가?
- Eligible vehicle 0개와 malformed input을 구분했는가?
- Phase 02/03 의미를 앞당기지 않았는가?

Stop 조건:

- 두 권위 문서가 같은 field에 다른 의미를 주며 conflict rule로 해소되지 않음
- Public field shape 없이는 production behavior를 결정할 수 없음

Resume 조건:

- Owner decision/ADR와 updated negative fixture가 같은 변경 단위로 제공됨

### 12.3 Checkpoint C — Security/identity review

시점: WP-01.6 전후.

사람이 묻는 질문:

- Canonical encoding이 collision/framing ambiguity를 막는가?
- Raw/semantic/envelope identity가 혼합되지 않았는가?
- Error/report/log에 PII/raw bytes가 없는가?
- Unknown field와 extension authorization이 explicit인가?
- Immutable artifact의 nested alias가 없는가?

Stop 조건:

- Fingerprint migration/replay rule 미승인
- Canary leakage
- Core SDK/Jackson dependency

Resume 조건:

- ADR/보안 review, negative test와 architecture report green

### 12.4 Checkpoint D — Evidence seal과 acceptance

시점: WP-01.7.

Evidence bundle candidate:

```text
phase-01-evidence/
  manifest/
    source/phase/review/commit fingerprints
    toolchain and exact command records
    schema/adapter/policy identities
    open-gated-deferred snapshot
  E-P01-NUMERIC/
    boundary table
    item-first oracle
    overflow report
    provided D/U integer and typed-absence report
  E-P01-TIME/
    plan/window interval oracle
    service/window-policy/depot/trip/wait/resource semantic report
    full-arc handoff oracle
  E-P01-COMPAT/
    size/capability/zone property report
    ownership/trip cases
  E-P01-ERROR/
    adapter/alias/reference report
    adapter module-introduction/DAG/fixture wiring report
    sparse travel duplicate/source/C-non-authority report
    profile/preset/mandatory/extension report
    order/error/fingerprint/immutability/redaction report
    architecture/dependency report
    approved exit-required catalog + exact-set manifest
    versioned verifier + positive/negative self-test exit-vector report
  handoff/
    NormalizedInputArtifact contract fingerprint
    typed sparse travel/source contract fixture
    negative fixture catalog
    known limitations
    rollback point
```

Pre-review manifest allowlist는 다음 의미만 가진다.

```text
phase
canonicalPhasePlanDigest
reviewCriteriaDigest
sourceCommitDigest
inputArtifactDigests
configProfileBuildRuntimeDigests
commandEnvironmentToolchainExitCodeRecordDigest
testResultAndFixtureDigests
requiredEvidenceKeyArtifactDigests
architectureDependencySecurityReportDigests
openGatedDeferredSnapshotDigest
handoffCandidateArtifactDigest
rollbackPointDigest
```

Pre-review manifest에 reviewer identity, verdict, review ref/digest, acceptance status/receipt를
넣거나 review 뒤 backfill하지 않는다.

Stop 조건:

- Test report 누락/0건/skipped
- Approved exit-required catalog와 manifest의 missing/extra/wrong-engine row
- Verifier grammar 또는 negative self-test exit vector 불일치
- Mutable `target/`만 evidence로 있음
- 네 evidence key 중 하나 누락
- Review가 unsealed evidence를 사용
- Acceptance receipt가 manifest+review digest를 함께 참조하지 않음

Resume 조건:

- 새 immutable evidence graph를 다시 봉인하고 독립 review를 재실행

## 13. 흔한 오해와 anti-pattern

1. **“JSON parsing이 됐으니 Phase 01 완료다.”**
   Parsing은 syntax 일부일 뿐 numeric/time/reference/order/fingerprint/evidence가 남았다.

2. **“Canonical input이 곧 `ProblemInstance`다.”**
   Dense ID와 prepared travel은 Phase 02 소유다.

3. **“Delivery-only pickup은 depot node로 만들면 된다.”**
   가짜 node는 depot service, stop, travel과 time window를 오염시킨다.

4. **“`1.0`은 정수와 같으니 distance로 받아도 된다.”**
   Contract는 integer-only syntax다. Decimal을 round/truncate하지 않는다.

5. **“합계가 같으니 item-first와 line-first가 같다.”**
   `FLOOR`에서는 순서가 결과를 바꾼다.

6. **“ID를 trim/lowercase하면 친절하다.”**
   승인 없는 canonicalization은 opaque identity를 합쳐 duplicate/reference를 오염시킨다.

7. **“중복 값이 같으면 dedupe해도 된다.”**
   Duplicate 자체가 input integrity error다.

8. **“Missing limit는 큰 수를 넣으면 편하다.”**
   Typed absence를 잃으면 fingerprint와 later policy가 달라진다.

9. **“Preset이 없으면 customer default를 여기서 넣자.”**
   Phase 04 binding이 exact default를 선택한다. Phase 01은 omission을 보존한다.

10. **“Eligible vehicle 0개면 malformed input이다.”**
    Valid normalized fact일 수 있다. Final `PROVEN`은 후속 authority가 만든다.

11. **“Unknown field는 무시가 안전하다.”**
    Unknown policy는 schema-version별 explicit contract다.

12. **“Unmodifiable list면 immutable하다.”**
    Nested element/array/source builder alias까지 방어해야 한다.

13. **“Object hash나 JSON field order로 fingerprint하면 된다.”**
    Versioned canonical encoding과 stable ordering이 필요하다.

14. **“Error에 raw value를 넣어야 debugging이 쉽다.”**
    PII/raw bytes 유출을 막고 code/path/digest로 진단한다.

15. **“Phase 13 field를 미리 넣으면 미래 대비가 된다.”**
    `C-17` gate를 core input에 선반영하는 architecture violation이다.

16. **“FLOOR fixture가 있으니 official calibration도 승인됐다.”**
    Plan-final 실행 fixture와 Phase 14 official/production authority는 별개다.

17. **“`mvn verify`가 green이면 Phase가 accepted다.”**
    Evidence manifest, independent review와 post-review acceptance receipt가 모두 필요하다.

18. **“실패한 normalizer가 가능한 field까지만 반환해도 유용하다.”**
    Partial artifact는 Phase 02가 불완전 authority를 소비하게 만든다.

19. **“Live reactor가 보이니 Phase 00이 accepted다.”**
    미커밋 snapshot과 mutable `target/`은 HEAD baseline이나 acceptance receipt가 아니다.

20. **“Phase 00이 `adapters/common`도 만들어 줄 것이다.”**
    Explicit handoff는 core-only다. Phase 01 architecture-owner gate가 placement/POM/DAG를
    승인하기 전 adapter production source를 만들지 않는다.

21. **“`C`나 missing `D/U`를 Phase 01에서 보완하면 친절하다.”**
    `C`는 non-authoritative이고 generation/self override/`45 km/h`는 Phase 02 소유다.

22. **“`waitInDepot`과 resource 의미는 wire schema 승인 때 정하면 된다.”**
    Exact N/Y, depot task/duration과 route-total resource semantics는 이미 resolved다.
    OPEN은 field shape/alias/requiredness뿐이다.

23. **“마지막 report 검사 명령이 0이면 모든 owner test가 실행됐다.”**
    유일한 exit catalog와 manifest의 양방향 exact set, fail-closed verifier와
    `0,64..75` self-test가 grammar/class/method/engine/staleness를 모두 확인해야 한다.

## 14. 실제 Phase exit checklist와 Definition of Done

다음은 점수표가 아니라 모두 필요한 AND gate다.

### 14.1 Entry와 scope

- [ ] Phase 00 `ACCEPTED`와 `E-P00-BUILD/ARCH/LEGACY`가 동일 source identity에 묶였다.
- [ ] Accepted module/package/test-fixture 규칙과 exact command가 기록됐다.
- [ ] HEAD/accepted/live inventory가 timestamp/hash와 authority별로 분리됐다.
- [ ] Adapter placement, parent/aggregator POM, DAG, fixture/rule wiring이 Architecture owner에게 승인됐다.
- [ ] Phase 01 owner, independent reviewer와 scheduler authority가 정해졌다.
- [ ] Public/open contract 범위와 test-only 범위가 구분됐다.
- [ ] Phase 02/03/04/13/14 scope를 앞당긴 code/dependency/default가 없다.

### 14.2 Adapter/canonical contract

- [ ] Exact schema+adapter version dispatch이며 `latest/nearest` fallback이 없다.
- [ ] External DTO와 core canonical type이 분리됐다.
- [ ] Alias equality/conflict와 unknown-field policy가 versioned explicit contract다.
- [ ] Plan/profile/version/preset omission, mandatory/extension declaration이 보존된다.
- [ ] Raw digest와 mapping provenance가 있다.
- [ ] Duplicate/reference failure가 sort/map 전에 검출된다.
- [ ] Adapter→core compile edge만 필요 범위에 있고 core→adapter/Jackson edge와
  adapter↔fixture cycle이 없다.

### 14.3 Normalization contract

- [ ] Weight/volume `n=3/FLOOR`, item-first, checked multiply/sum이 정확하다.
- [ ] Cost/distance/time decimal과 overflow를 typed reject한다.
- [ ] Plan `[start,end)`, inclusive close, repeating/overnight가 정확하다.
- [ ] Service/deadline/alias/order-level taskTime rule이 정확하다.
- [ ] Sparse `D/U` presence/absence/source, `C` non-authority,
  coordinate/speed present-valid/missing/invalid가 정확하다.
- [ ] Self `0/0`, Great Circle, `45 km/h`, `M²`와 vehicle-resolved time을 만들지 않았다.
- [ ] Window policy declaration/omission을 보존하고 Phase 04 binding을 앞당기지 않았다.
- [ ] Depot task/duration, exact N/Y, oneway/single-roundtrip와 wait 의미가 정확하다.
- [ ] Location-transition stop, actual-arc drive, route-total/no-reset semantic identity와
  vehicle/global typed absence를 보존하고 Phase 03 계산을 앞당기지 않았다.
- [ ] Size/ALL/capability/zone/ownership이 exact하다.
- [ ] Set-like order만 canonicalize하고 meaningful sequence는 보존한다.

### 14.4 Artifact, failure와 architecture

- [ ] Success artifact가 deep immutable하다.
- [ ] Failure가 partial artifact를 노출하지 않는다.
- [ ] Error order와 semantic fingerprint가 input iteration/locale에 독립적이다.
- [ ] Raw/semantic/envelope identity가 분리됐다.
- [ ] PII/raw bytes/secret canary leakage가 0이다.
- [ ] Core의 Jackson/cloud/HTTP/solver/verifier dependency가 0이다.
- [ ] Phase 01 output에 prepared travel/problem/route/score가 없다.

### 14.5 Test/evidence/review

- [ ] §11의 positive/negative/boundary/property/fault/corruption/security test가 통과했다.
- [ ] Approved `phase01-exit-required-tests-v1`이 유일한 exit catalog이고
  manifest와 양방향 exact set-equality이며 missing/extra/duplicate/unknown/wrong
  engine/malformed/omitted row가 0이다.
- [ ] Owner module의 fresh exact Surefire/Failsafe reports가 모두 존재하고
  test count > 0, failed/error/skipped=0이며 catalog의 모든 class/method record가 있다.
- [ ] Report verifier self-test가 §9.8의 positive/negative control과 exact exit
  vector `0,64..75`를 모두 재현하고 digest가 `E-P01-ERROR`에 봉인됐다.
- [ ] Focused module clean verify와 root clean verify가 모두 green이다.
- [ ] `E-P01-NUMERIC/TIME/COMPAT/ERROR`가 immutable digest를 가진다.
- [ ] Pre-review manifest가 allowlist를 지키고 review/acceptance 정보를 포함하지 않는다.
- [ ] Independent review report가 sealed manifest digest를 참조한다.
- [ ] Post-review receipt가 manifest+review digest, handoff와 rollback point를 참조한다.
- [ ] OPEN/GATED/DEFERRED 항목을 임의 값으로 닫지 않았다.

### 14.6 Phase 01 DoD

위 checklist가 모두 참이고 scheduler가 유효한 acceptance receipt를 확인했을 때만
Phase 01을 `ACCEPTED`로 전이할 수 있다. 다음은 DoD가 아니다.

- Source/test 파일이 존재함
- Parser happy path가 통과함
- Current 미승인 Phase 00 candidate root build가 green임
- Review 문서만 accepted verdict를 가짐
- FLOOR fixture를 읽을 수 있음
- Console에 normalized object 한 건이 출력됨

## 15. Phase 02 인계

### 15.1 Producer artifact

Phase 01은 Phase 02에 다음을 넘긴다.

```text
NormalizedInputArtifact
  canonical input identity projection
    schema identity
    adapter identity
    raw input digest
    semantic fingerprint
    envelope fingerprint
  plan identity
  exact customer/profile/version
  requested preset or explicit omission
  optional mandatory declarations
  approved typed extension inputs
  validated external request/service/vehicle/location graph
  normalized integer numeric/time/service facts
  delivery-only/real-pair service pattern
  normalized sparse directed provided travel declarations
    physical external-location key
    integer meter D present/absent + source identity
    integer common-second U present/absent + source identity
    legacy C raw provenance and explicit non-authority
    deterministic canonical order
  coordinate/speed source facts
    present-valid or typed missing
    present-invalid rejected before seal
  customer-window declaration or typed omission
  depot taskTime non-application and future-rotation duration provenance
  trip/terminal/exact-N-Y wait declarations
  vehicle/global route-resource typed absence
  location-transition/actual-arc/route-total-no-reset semantic policy identity
  alias/coercion/default/source provenance
  schema/adapter/normalization policy identity
```

Phase 01은 dense solver ID, complete travel, `ProblemInstance`, bound profile, route/bank,
score/objective를 넘기지 않는다.

### 15.2 Consumer 확인법

Phase 02 entry에서 다음을 확인한다.

1. Artifact content digest와 semantic/envelope fingerprint가 manifest와 일치한다.
2. Schema/adapter/normalization policy identity가 supported exact version이다.
3. Collection이 immutable하고 alias가 없다.
4. External identity/reference graph가 complete하다.
5. Sparse travel의 key, `D/U` presence/value/source와 `C` non-authority가 typed하다.
6. Coordinate/speed source fact가 present-valid/missing이고 invalid가 success에 없다.
7. Phase 01 artifact에 self override/generated value/complete `M²`가 없다.
8. Profile/preset/window-policy/extension declaration이 손실되거나 미리 bind되지 않았다.
9. Depot/wait/resource semantic policy와 typed absence가 손실되지 않았다.
10. Phase 01 error closure가 success artifact와 모순되지 않는다.
11. Raw bytes/DTO를 다시 parsing하지 않는다.

### 15.3 Broken handoff 증상

| 증상 | probable producer/consumer defect |
|---|---|
| Phase 02가 JSON alias를 다시 확인 | Phase 01 artifact field/provenance 누락 또는 consumer boundary 위반 |
| Phase 02가 timezone/rounding을 다시 선택 | Normalized value/policy identity 누락 |
| Phase 02가 duplicate ID를 발견 | Phase 01 duplicate-before-map gate 실패 |
| Preset omission이 default key로 바뀜 | Phase 01이 Phase 04 binding을 앞당김 |
| Missing resource가 `Long.MAX_VALUE` | Typed absence 손실 |
| Delivery-only에 physical pickup node/location이 요구됨 | Service pattern handoff 오류 |
| Sparse arc가 complete matrix로 보임 | Phase 01이 travel preparation을 앞당기거나 source kind 손실 |
| `C` 값이 generation/fingerprint meaning을 바꿈 | Legacy non-authority 위반 |
| Missing speed가 이미 `45 km/h`로 채워짐 | Phase 01이 Phase 02 default 적용을 앞당김 |
| Depot task/duration이 single-trip service에 포함 | Resolved depot contract 손실 |
| Phase 02/03가 N/Y 또는 stop/drive reset 의미를 다시 추측 | Phase 01 semantic policy handoff 누락 |
| Fingerprint가 raw field order에 따라 달라짐 | Canonical order/encoding defect |
| 같은 artifact ID에 다른 bytes | Immutable identity/overwrite defect |
| Phase 02가 mutable list 변경 가능 | Defensive copy/seal defect |

Handoff가 깨지면 Phase 02에서 보정하거나 fallback하지 않고 corrected Phase 01 artifact를
요구한다.

## 16. Source → requirement → work package → test/evidence traceability

| Requirement ID | Source | Requirement | Work package | Exact test/evidence |
|---|---|---|---|---|
| `REQ-P01-AUTH` | Master §1.5, current Domain/Architecture §1, Implementation README §3, Plan §2 | Authority/drift/status를 분리하고 15 Phase/gate 보존 | WP-01.0, WP-01.7 | HEAD/accepted/live timestamp+hash, open/gated snapshot |
| `REQ-P01-ADAPTER` | Master §4.1~§4.3/§7.1, current Architecture §5~§7, Integrated §5.1 | Versioned anti-corruption boundary, DTO/core 분리, no fallback | WP-01.1/01.1A | Module-introduction/DAG report, `VersionedInputAdapterContractTest`, `E-P01-ERROR` |
| `REQ-P01-ENVELOPE` | Domain §4.1~§4.2, Phase 01 review F-P01-001 | Plan/profile/preset omission/mandatory/extension 보존 | WP-01.1, WP-01.6 | Adapter/artifact tests, `E-P01-ERROR` |
| `REQ-P01-IDENTITY` | Master §4.5~§4.6, Phase 01 §6.4 | Opaque ID, duplicate-before-sort, set/sequence와 three identities | WP-01.2, WP-01.6 | `CanonicalOrderingTest`, artifact fingerprint report |
| `REQ-P01-NUMERIC` | `Q-NUM-01~03`, Master §7.2, Domain §5.1 | Scale-3 FLOOR, item-first, integer-only, checked arithmetic | WP-01.3 | `FixedPointNormalizerTest`, `E-P01-NUMERIC` |
| `REQ-P01-TRAVEL-HANDOFF` | Master §8, current Domain §8, dated Domain §6, canonical Phase 01 §2.2/§3.1/§6.2, Phase 02 §3~§4 | Typed sparse key, integer D/U presence/source, C non-authority, coordinate/speed states; no preparation | WP-01.2, WP-01.3/01.3A, WP-01.7 | `TravelInputDeclarationNormalizerTest`, Phase 02 consumer fixture, `E-P01-NUMERIC/ERROR` |
| `REQ-P01-TIME` | `Q-TIME-01~04`, Master §7.3, current Domain §6 | Exact local string, origin seconds, plan/window/repeat/overnight/full-arc와 window-policy declaration | WP-01.4 | `TimeNormalizerTest`, Phase 03/04 handoff, `E-P01-TIME` |
| `REQ-P01-SERVICE` | `Q-IN-01`, Domain §4.2 | Deadline alias, service sum, order taskTime reject | WP-01.1, WP-01.4 | `ServiceTimeNormalizerTest`, adapter alias tests |
| `REQ-P01-TRIP-RESOURCE` | `Q-IN-02`, `Q-BENCH-03`, Master §6.2/§7.3, current Domain §4.4/§6.6/§11 | Oneway/single-roundtrip, depot task/duration, exact N/Y, location-transition/actual-arc/route-total policy, vehicle/global absence | WP-01.4 | Trip/resource tests, semantic handoff, `E-P01-TIME` |
| `REQ-P01-COMPAT` | `Q-COMP-01~02`, Master §7.5, Domain §5.3 | Exact size/ALL, capability subset, one concrete zone | WP-01.5 | `CompatibilityNormalizerTest`, `E-P01-COMPAT` |
| `REQ-P01-PAIR` | `Q-REQ-01~02`, Domain §2.3~§2.4 | Delivery-only logical initial load와 real pair identity | WP-01.1, WP-01.2 | Canonical request fixtures, `E-P01-COMPAT` |
| `REQ-P01-OWNERSHIP` | `Q-OBJ-03`, `Q-RES-01` | Strict `DIRECT/LEASE`, outcome 아님 | WP-01.5 | `normalizesOwnershipStrictly` |
| `REQ-P01-ERROR` | Master §4.2, Domain §16, Phase 01 §7.6 | Ordered typed reject, no partial artifact | WP-01.2~01.6 | Failure fixture suite, `E-P01-ERROR` |
| `REQ-P01-SECURITY` | Integrated §20/§22.4, Plan §9/§13, review F-P01-004 | Raw/PII/secret redaction과 corruption detector | WP-01.6 | `rejectionEvidenceRedactsRawValuesAndInputBytes` |
| `REQ-P01-ARCH` | Current Architecture §5~§7, dated Architecture §2.2~§2.7, Integrated §3/§5 | Core provider/solver 독립, approved adapter→core DAG, no prepared output | WP-01.1A, WP-01.6 | `Phase01DependencyRulesTest`, DAG/architecture report |
| `REQ-P01-MAVEN` | Canonical review F-P01-003, human-guide review HG-P01-005, current Architecture §5.2/§18~§19 | Reactor-safe command, fixture wiring, 유일 exit catalog↔manifest 양방향 exact set, Surefire/Failsafe discovery와 owner zero/stale-report false-green 방지 | 모든 focused WP, WP-01.7 | Approved catalog + exact-set manifest + verifier/`0,64..75` self-test |
| `REQ-P01-EVIDENCE` | Plan §9~§11 | Manifest→review→receipt 단방향 acceptance | WP-01.7 | Four evidence keys, manifest/review/receipt digests |
| `REQ-P01-HANDOFF` | Master §15.3, Phase 02 §4/§13.1 | Normalized facts만 전달, raw reparse/preparation 금지 | WP-01.7 | Adapter-normalization contract, Phase 02 entry manifest |
| `REQ-P01-GATES` | Plan §1.2/§14, Phase 13 §4, Phase 14 §0.1/§3 | C-17 optional과 official/production authority 우회 금지 | WP-01.0, WP-01.6, WP-01.7 | Dependency scan, open/gated snapshot, review checklist |

## 17. 구현자 최종 자문

작업 종료 전 다음 질문에 모두 “예”라고 답할 수 있어야 한다.

1. External bytes가 어느 schema/adapter/policy로 해석됐는지 재현할 수 있는가?
2. 동일 meaning과 다른 bytes, 동일 bytes와 다른 policy를 identity로 구분하는가?
3. 모든 duplicate/alias/reference failure가 sort/map 전에 잡히는가?
4. Numeric/travel/time/service expected를 production 코드와 독립적으로 손 계산했는가?
5. Delivery-only와 real pair의 identity가 후속 Phase에 충분한가?
6. Sparse D/U/source/C와 coordinate/speed 상태가 Phase 02 준비 없이도 완전한가?
7. Preset/window-policy omission, extension, wait/resource absence를 hidden default로 닫지 않았는가?
8. Depot task/duration, exact N/Y와 route-resource semantic identity를 보존했는가?
9. Success와 rejection 외의 partial observable state가 없는가?
10. Core가 Jackson/cloud/solver/travel preparation을 모르는가?
11. Adapter module/DAG/fixture wiring이 승인되고 acyclic인가?
12. PII/raw bytes canary가 모든 report/log/evidence에서 사라지는가?
13. 유일 exit catalog와 manifest가 양방향 exact set이고, focused command의 malformed/
    omitted/duplicate/unknown·wrong engine, missing class/method, zero/failed/error/skipped,
    stale report가 각각 정해진 non-zero exit로 닫히는가?
14. Evidence, independent review와 acceptance receipt가 단방향인가?
15. Phase 02가 raw input을 다시 보지 않고 일을 시작할 수 있는가?
16. Phase 13 optional gate와 Phase 14 official/production authority를 조금도 앞당기지 않았는가?

하나라도 아니면 Phase 01은 아직 exit하지 않는다.
