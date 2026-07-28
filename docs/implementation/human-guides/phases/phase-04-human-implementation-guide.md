# Phase 04 사람용 구현 가이드 — Capability와 customer profile

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 04 only
canonical_phase_count: 15
phase: "04"
phase_name: capabilities-customer-profiles
canonical_phase_document: docs/implementation/phases/phase-04-capabilities-customer-profiles.md
canonical_phase_review: docs/implementation/reviews/phase-04-review.md
canonical_phase_document_status: REVIEWED_CHANGES_REQUIRED
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: NOT_STARTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_branch: codex-implementation
inventory_observed_date: 2026-07-29
working_tree_cross_check_at: "2026-07-29T02:22:42+09:00"
working_tree_cross_check_state: UNCOMMITTED_PHASE_00_FIX_01_LIVE_DRIFT_NOT_ACCEPTED
prerequisite_phases:
  - "00"
  - "01"
  - "02"
  - "03"
source_fingerprint_scheme: expected_head_blob_plus_live_working_bytes_fail_closed
source_sections_and_fingerprints:
  docs/domain-design.md: "current top-level domain map, REVIEW provenance | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "current top-level architecture map, REVIEW provenance | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/master-design.md: "§2.4, §3~5, §7, §9, §13, §15~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/2026-07-26-domain-design.md: "§2~3, §5.3, §7, §9~10, §15~18, §20~21 | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "§1.2~1.5, §2, §5.2, §5.5~5.6, §6 | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/architecture-domain-implementation-design.md: "§1~3, §7~9, §19~26, §28~30 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-* rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§1, §3~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~4, Phase 03~05, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§2, §5~9 | 250aa90ae568a6b32ec905fa5ee456d430ff72cf"
  docs/implementation/phases/phase-04-capabilities-customer-profiles.md: "§1~15 | e0a68fd442a7db383e4a650e4a561234c323b1fe"
  docs/implementation/reviews/phase-04-review.md: "§1~7 | bd7d11d478f8afbd69c6336c3b653cd89ea69ac2"
adjacent_sources:
  docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md: "§2.3, §3~7, §9, §13~15 | 74098f7e15cf75bcc443ae009cc475a9b60d63a3"
  docs/implementation/reviews/phase-03-review.md: "§1, §4, §6~7 | e473887ffbbb165f18ea6aaa76d6fc3917fc4af2"
  docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md: "§2.3, §4, §6.3, §7, §10.3, §13~16 | 0ea8046142a9e53cc1b9cedb198a1bdae550378b"
  docs/implementation/reviews/phase-05-review.md: "§1, F-P05-001, F-P05-005, F-P05-011, §6~7 | 88ecf56c57556a0ae8d3578ec158561602222c14"
historical_cross_check:
  docs/2026-07-26-master-design.md: "SUPERSEDED_NOT_AUTHORITY | d4f7fbf058711a02451de911bc86b1cd2f426218"
  docs/codex/: "ABSENT_IN_COMMIT_AND_WORKTREE | NO_GIT_BLOB"
expected_reader:
  - Java의 interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, time window, capacity와 objective를 경험했다
  - ro-next의 RPDPTW identity, profile lifecycle과 module 경계는 처음 접한다
owner_roles:
  implementation: RPDPTW Capability/Profile implementation owner
  core_contract: Phase 03 Core/Evaluation owner
  descriptor_and_catalog: Product/API/Data + Profile Catalog owner
  authorization: Product/Tenant/Security owner
  capability_inventory: Distribution/Build/Security owner
  downstream_search: Phase 05 Pair/Insertion/Portfolio owner
  downstream_verification: Phase 07 Verification owner
  architecture: Phase 00 Architecture owner
  review: independent Phase 04 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P04-BINDING
  - E-P04-ISOLATION
  - E-P04-FACET
```

> 이 문서는 Phase 04를 처음 맡은 사람이 계약을 학습하고, entry gate가 열린 뒤 구현하고, 실제 evidence와 인계를 스스로 판정하기 위한 교육형 작업 지시서다. 현재 checkout에는 병렬 Phase 00 작업이 만든 미커밋 module/package skeleton은 있지만 Phase 04 production type, test, descriptor 또는 evidence는 없다. 아래 Java 이름, package, method signature와 file tree는 별도 표시가 없는 한 **proposed internal skeletal contract**이며, 존재하는 코드나 승인된 public API를 뜻하지 않는다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 04는 “고객마다 solver를 복제하는 일”을 막는 경계다. 여러 고객이 재사용할 수 있는 실행 코드인 **capability**와 고객이 어떤 capability를 어떤 exact version·typed parameter·objective 순서로 사용할지 선언한 **profile descriptor**를 분리하고, solve 시작 전에 모든 의존성과 권한을 검증하여 immutable `BoundProfile`로 동결한다.

성공한 뒤 Phase 05와 Phase 07은 customer 이름, catalog, descriptor parser 또는 capability 선택 로직을 다시 보지 않는다. 오직 Phase 02 문제·이동 identity와 Phase 03 평가 계약에 결합된 immutable bound artifact만 소비한다.

현재는 이 구현을 시작할 수 없다. Phase 00~03 accepted artifact/evidence가 없고, Phase 04 독립 review가 세 가지 cross-Phase 계약을 `CHANGES_REQUIRED`로 남겼기 때문이다. 지금 허용되는 마지막 안전 지점은 문서·oracle 설계와 공동 API review 준비다.

## 2. 큰 그림과 canonical 15 Phase에서의 위치

### 2.1 왜 이 Phase가 필요한가

CVRPTW 구현에서 고객별 차이를 빠르게 붙이다 보면 다음과 같은 코드가 생기기 쉽다.

```java
if (customerName.equals("...")) { /* 다른 비용과 제약 */ }
if (presetName.contains("express")) { /* 다른 목적 순서 */ }
```

이 방식은 처음에는 짧지만 다음 문제를 만든다.

- Propagation, insertion, ALNS와 verifier에 같은 고객 분기가 반복된다.
- 같은 정책을 solver와 verifier가 서로 다르게 구현할 수 있다.
- 고객 한 명을 추가할 때 core, worker, POM과 배포를 모두 바꾸게 된다.
- 어떤 code/config/version으로 결과가 나왔는지 재현하기 어렵다.
- 다른 고객의 preset이나 `latest`가 우연히 선택되는 보안·격리 결함이 생긴다.

Phase 04의 해법은 다음과 같다.

```text
customer policy data
  └─ exact profile/version/preset
       └─ approved capability key/version + typed parameters
            └─ pure deterministic binding
                 └─ immutable BoundProfile
```

물리 사실을 계산하는 Phase 03은 고객을 모른다. Phase 04는 그 사실을 어떤 hard constraint, metric, score, objective와 stage declaration으로 조합할지 결정한다. Phase 05 이후 algorithm은 이 조합 결과만 사용한다.

### 2.2 Phase 00~14, 총 15개의 위치

이 저장소의 canonical 구현 단계는 **Phase 00~14, 총 15개**다.

| Phase | 주제 | Phase 04와의 관계 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | Core/capabilities/profile-catalog module과 dependency guard를 제공해야 한다 |
| 01 | Canonical input와 normalization | Service/trip/compatibility/ownership/mandatory 관련 normalized fact를 만든다 |
| 02 | Prepared travel과 immutable problem | Bound artifact가 결합될 exact problem/travel identity를 만든다 |
| 03 | Route propagation과 evaluation kernel | Capability가 구현하고 binder가 materialize할 customer-neutral SPI를 만든다 |
| **04** | **Capability와 customer profile** | **이 가이드가 소유하는 exact binding과 lifecycle** |
| 05 | Pair insertion과 initial portfolio | `BoundProfile`과 Phase 03 kernel을 customer-neutral하게 소비한다 |
| 06 | COW ALNS와 reproducibility | Bound plan과 comparator를 사용하되 profile을 재해석하지 않는다 |
| 07 | Independent verification과 final result | 같은 bound contract로 cache 없이 독립 재계산한다 |
| 08 | Application port와 local runtime | Exact profile selection과 local composition을 조립한다 |
| 09 | No-DB object storage | Descriptor/artifact 저장과 exact-key 조회를 담당한다 |
| 10 | Provider-neutral coordinator | Bound snapshot/manifest identity로 worker를 조정한다 |
| 11 | AWS reference distribution | S3 + Step Functions + Lambda adapter를 붙인다 |
| 12 | Provider substitution | 승인된 provider 축만 교체한다 |
| 13 | Optional hybrid route selection | Phase 14A receipt 뒤에만 검토하는 `C-17` gated branch다 |
| 14 | 14A ALNS benchmark / 14B official cutover | Official 수치·production authority를 별도 gate로 관리한다 |

ALNS-first critical path에서 Phase 04의 위치는 다음과 같다.

```text
00 → 01 → 02 → 03 → [04] → 05 → 06 → 07 → 08 → 14A
```

Phase 04는 Phase 13의 MIP backend를 요구하지 않는다. Phase 13은 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 별도 `C-17` 승인이 있어야 시작할 수 있다. Phase 14A receipt도 Phase 13이나 Phase 14B production cutover를 자동 승인하지 않는다.

### 2.3 Producer와 consumer 계약

```text
Phase 01 normalized compatibility/service/trip/policy facts
                 ↓
Phase 02 ProblemInstance + PreparedTravel
                 ↓ exact identity
Phase 03 PropagationDeclaration + EvaluationPlan + SPI
                 ↓
Phase 04 exact authorization/catalog resolution
         → explicit capability registry resolution
         → dependency/type/unit/range/objective validation
         → immutable BoundProfileSnapshot + BoundProfile
                 ↓
Phase 05 insertion/portfolio     Phase 07 independent verifier
```

| 경계 | Producer가 넘길 것 | Consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 03 → 04 | Typed fact/metric/unit/key 계약, immutable declaration/plan, route-level kernel와 failure taxonomy | Profile이 propagation algorithm을 복제하거나 raw route/input을 반사적으로 읽기 |
| Phase 02 → 04 | Exact `ProblemInstance`/`PreparedTravel` identity와 compatibility/resource/service/trip facts | Profile이 size/zone/travel을 다시 계산하거나 fallback 생성하기 |
| Phase 04 → 05 | Immutable `BoundProfile`, Phase 03 declarations, dependency closure, comparator 의미, evidence identity | Catalog 재조회, parameter default/coercion, customer branch, ad hoc route-vector 합산 |
| Phase 04 → 07 | Portable snapshot, approved executable contract와 exact fingerprints | Binder의 `PASS`, cache나 concrete catalog/provider를 verification verdict로 신뢰하기 |

Phase 04는 insertion option, route/bank mutation, initial portfolio, ALNS, final outcome 또는 publication을 만들지 않는다.

## 3. RPDPTW primer와 프로젝트 전용 용어

### 3.1 CVRPTW 경험자가 먼저 분리할 것

이 프로젝트에서 `capability`라는 단어는 문맥에 따라 세 가지 다른 것을 가리킬 수 있다. 같은 key 공간이나 registry로 합치면 안 된다.

| 축 | 의미 | Owner | 예 |
|---|---|---|---|
| Vehicle qualification capability | Request가 요구하고 vehicle이 보유하는 정적 자격 fact | Phase 01~02 Domain/Input | 냉장, lift, 위험물 자격 |
| Evaluation capability | Phase 03 facts를 소비하는 재사용 실행 코드 | Phase 04 Capability | 지각 score, 차량 비용, approved facet |
| Algorithm operator capability | Phase 05~06 탐색 동작의 exact reference | Phase 05~06 Algorithm | Destroy/repair operator, guard/budget reference |

Vehicle qualification은 다음 static compatibility 식의 일부다.

```text
request.requiredCapabilities ⊆ vehicle.capabilities
```

Phase 04 evaluation capability는 이 집합을 다시 계산하지 않는다. Phase 02가 만든 eligibility fact를 소비하거나, 기존 facts에 대한 constraint/metric/score를 제공한다. Algorithm operator도 evaluation registry로 resolve하지 않는다.

### 3.2 Capability와 profile

| 용어 | 정확한 의미 | 아닌 것 |
|---|---|---|
| Capability | 승인된 typed SPI를 구현하는 재사용 Java behavior | Customer 이름 분기, arbitrary script, runtime 다운로드 JAR |
| Profile | Capability exact key/version, typed parameter, objective order를 선언한 immutable 고객 설정 | Java module, executable class 이름, cloud locator |
| Preset | 한 exact profile version 안에서 선택 가능한 승인 조합 | 다른 customer에서 가져오는 fallback |
| Catalog | Exact profile definition을 identity로 조회하고 무결성을 검증하는 경계 | Feasibility 또는 objective 계산기 |
| Registry | 이 build/distribution에 승인된 capability 구현의 explicit inventory | Classpath scan 결과, `ServiceLoader` first-wins |
| Binding | Verified descriptor를 registry와 problem/Phase 03 contract에 결합하는 pre-solve 검증 | Search 중 문자열을 반복 해석하는 과정 |
| `BoundProfile` | Binding이 끝나 더 해석할 것이 없는 solve-bound immutable executable contract | Long-lived catalog record, mutable cache, provider handle |

Profile은 “무엇을 선택할지”를 말하고 capability는 “선택된 behavior를 어떻게 실행할지”를 제공한다. Core physical rule은 어느 쪽도 끄거나 완화할 수 없다.

### 3.3 RPDPTW identity

CVRPTW의 단일 customer ID와 달리 이 프로젝트는 다음 identity를 분리한다.

```text
ProfileLookupCoordinate
  = CustomerKey + ProfileKey + ProfileVersion

ProfileDescriptorIdentity
  = ProfileLookupCoordinate + ProfileSchemaVersion

ResolvedProfileIdentity
  = ProfileDescriptorIdentity + exact PresetKey
    + ImplementationContractVersion

CapabilityImplementationIdentity
  = CapabilityKey + CapabilityVersion
    + capability contract version
    + implementation fingerprint

BoundProfileIdentity
  = ResolvedProfileIdentity
    + descriptor/authorization/registry fingerprints
    + ProblemFingerprint + PreparedTravelFingerprint
    + Phase03 declaration/plan fingerprints
    + SolvePlan/build-runtime fingerprints
```

Catalog의 유일 lookup coordinate는 `customer/profile/version`이다. 같은 coordinate 아래 다른 schema/content/default를 병렬 등록하거나 overwrite하지 않는다. Schema version은 fallback selector가 아니라 읽은 bytes의 의미와 호환성을 검증하는 metadata다.

### 3.4 Lifecycle와 상태 전이

Long-lived descriptor와 solve-bound runtime은 수명이 다르다.

```text
requested exact reference
→ requested tenant/customer scope authorization
→ exact tenant-scoped catalog lookup
→ schema/content fingerprint verification
→ exact preset resolution
→ exact descriptor+preset authorization
→ explicit registry snapshot resolution
→ dependency/type/unit/range/objective validation
→ problem/travel/Phase03 binding
→ snapshot/runtime semantic fingerprint equality
→ BOUND
```

상태를 이름으로 표현하면 다음과 같다.

```text
UNRESOLVED
→ ACCESS_SCOPE_AUTHORIZED
→ DESCRIPTOR_VERIFIED
→ PRESET_RESOLVED
→ DESCRIPTOR_PRESET_AUTHORIZED
→ CAPABILITIES_RESOLVED
→ CONTRACT_VALIDATED
→ PROBLEM_BOUND
→ BOUND

any state → REJECTED(typed failure)
```

`BOUND` 뒤에는 catalog, authorization source와 registry를 다시 읽지 않는다. Drift는 다음 solve나 worker reconstruction에서 identity mismatch로 거부하고 이미 bound된 객체를 mutate하지 않는다.

### 3.5 Project-specific invariant

Phase 04 구현은 다음 불변조건을 동시에 지킨다.

1. 모든 profile/preset/capability/contract version은 exact match다.
2. `latest`, 범위, 비슷한 이름과 cross-customer fallback은 없다.
3. Omitted preset은 해당 exact profile version이 선언한 exact default만 사용한다.
4. Default가 없다면 error다. Binder의 hidden default는 없다.
5. Binding은 verified immutable input만 소비하는 pure deterministic 계산이다.
6. Problem facts → dependency closure → Phase 03 declaration/plan의 한 방향을 지킨다.
7. Pair, capacity, compatibility, resource, service/trip hard rule을 profile로 끄지 않는다.
8. Parameter, dependency, unit, value와 output은 typed contract다.
9. 같은 key/version은 하나의 exact contract/content만 가리킨다.
10. Missing/cycle/duplicate/type/unit/range/direction defect는 kernel/search 전에 reject한다.
11. Bound object는 exact problem/travel에 속하며 다른 solve에 재사용하지 않는다.
12. Capability instance는 scratch/cache/clock/random/global registry를 보유하지 않는다.
13. Mandatory는 사용하는 preset에서 최상위 lexicographic dimension이지 hard/Big-M이 아니다.
14. `LEASE`가 있으면 이를 지원하는 preset은 outsourced dimension을 명시한다.
15. Route-level artifact 합을 승인된 full-solution objective authority로 가장하지 않는다.
16. Context tie는 business objective equality 뒤에만 적용한다.

### 3.6 용어집

| 용어 | 이 가이드에서의 의미 |
|---|---|
| Exact resolution | Range·alias·유사도 없이 identity 전체를 정확히 찾는 것 |
| Dependency closure | 선택 capability가 요구하는 fact/metric/capability/facet의 닫힌 DAG |
| Canonical fingerprint | 승인된 canonical encoding의 의미 field에서 계산한 content identity |
| Implementation fingerprint | 같은 capability key/version의 실제 승인 구현을 식별하는 값 |
| Problem-bound | Dense/request-indexed binding이 특정 problem/travel에만 유효한 성질 |
| Pure binder | I/O, clock, environment, random, mutable global registry를 읽지 않는 binding 계산 |
| Typed facet | 기존 facts로 표현할 수 없는 새 물리 상태를 위한 승인된 확장 seam |
| Full-solution evaluator | Ordered routes와 request bank 전체를 읽어 solution objective를 계산하는 미승인 cross-Phase 계약 |
| Business equality | Ordered business objective vector가 동일하다는 판단 |
| Context tie | Business equality 뒤에만 적용하는 solution/insertion 안정 정렬 |
| Last safe point | 미승인 의미를 발명하지 않고 작업을 멈출 수 있는 마지막 권위 경계 |

## 4. Source authority, section과 fingerprint

### 4.1 충돌 해소 순서

Source를 단일 선형 순서로 섞지 않는다. 먼저 각 문서의 역할과 관찰 시점을 분리한다.

```text
사용자 고정 지시
├─ current top-level map:
│    docs/domain-design.md + docs/architecture-design.md
│    현재 의미/배치 map, REVIEW provenance; implementation acceptance가 아님
├─ user-fixed design inputs:
│    canonical Master + exact Q-* register
│    + 2026-07-26 Final Domain/Architecture + integrated 15-Phase design
├─ implementation baseline:
│    README/master plan/progress + canonical Phase 04/review + adjacent handoff
│    실행·evidence·status 계약; source design을 임의 대체하지 않음
├─ live working-tree snapshot:
│    현재 관측 inventory와 bytes; accepted implementation evidence가 아님
└─ historical cross-check:
     superseded master와 history; current authority가 아님
```

같은 역할 안에서 의미가 충돌하면 사용자 고정 지시, canonical Master와 question register의 exact 상태를 우선 대조한다. 서로 다른 역할/시점의 문서가 충돌하면 한쪽을 조용히 승격하지 않는다. `source-role → heading → requirement → WP/test/evidence` 영향표를 만들고 해당 Product/Domain/Architecture/Implementation owner의 rebaseline 승인을 기다린다.

예를 들어 dated Final Domain/Architecture 일부에는 과거 상태인 `Q-INFRA-01 DEFERRED`, 질문 수 `25/1/2`가 남아 있지만 current top-level map, canonical Master와 질문 등록부에는 **`Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`**이 기록돼 있다. 이 차이는 source role/time drift로 보존한다. AWS target 선택을 Phase 04 SDK 의존성, 구현 완료 또는 production authority로 확대하지 않는다.

Live working tree에 현재 reactor/module skeleton과 scheduler-owned progress 변경이 보이는 것도 관찰 사실일 뿐이다. Accepted Phase 00 receipt와 handoff가 없으므로 implementation baseline의 `0/15`, Phase 04 `NOT_STARTED/NOT_PRODUCED/NOT_ACCEPTED`를 승격하지 않는다.

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md)은 누락·퇴행을 확인하는 역사 자료일 뿐 current API authority가 아니다. `docs/codex/`는 `inventory_observed_at_commit`과 현재 working tree 모두에 존재하지 않아 읽거나 fingerprint할 파일이 없었다. 이후 그 경로가 생겨도 사용자 지시대로 historical cross-check로만 취급한다. 과거 초안에 solution evaluator 이름이 있었다고 해서 현재 cross-Phase blocker가 자동으로 해결되지 않는다.

### 4.2 검증 가능한 source fingerprint

아래 expected 값은 `inventory_observed_at_commit`에서 `git rev-parse HEAD:<path>`로 얻은 **Git blob hash**다. Expected HEAD와 현재 working bytes는 서로 다른 증거다. Source가 변경되면 hash만 교체하지 말고 관련 heading의 요구와 Java/test/evidence 영향을 함께 review한다.

| Source | 직접 읽을 section | Git blob |
|---|---|---|
| [Current Domain map](../../../domain-design.md) | 현재 top-level domain meaning/status map | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Current Architecture map](../../../architecture-design.md) | 현재 top-level module/runtime/status map | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Canonical Master](../../../master-design.md) | §2.4, §3~5, §7, §9, §13, §15~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Final Domain](../../../2026-07-26-domain-design.md) | §2~3, §5.3, §7, §9~10, §15~18, §20~21 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Final Architecture](../../../2026-07-26-architecture-design.md) | §1.2~1.5, §2, §5.2, §5.5~5.6, §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §1~3, Phase 03~05, §19~26, §28~30 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~4와 `Q-TIME`, `Q-COMP`, `Q-REQ`, `Q-OBJ`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | §1, §3~7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §2~4, Phase 03~05, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | §2, §5~9 | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Canonical Phase 04](../../phases/phase-04-capabilities-customer-profiles.md) | 전체, 특히 §3~15 | `e0a68fd442a7db383e4a650e4a561234c323b1fe` |
| [Phase 04 review](../../reviews/phase-04-review.md) | §1, §4~7 | `bd7d11d478f8afbd69c6336c3b653cd89ea69ac2` |
| [Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md) | §2.3, §3~7, §13~15 | `74098f7e15cf75bcc443ae009cc475a9b60d63a3` |
| [Phase 03 review](../../reviews/phase-03-review.md) | F-P03-004/F-P03-006, §6 | `e473887ffbbb165f18ea6aaa76d6fc3917fc4af2` |
| [Phase 05](../../phases/phase-05-pair-insertion-initial-portfolio.md) | §2.3, §4, §6.3, §7, §13~16 | `0ea8046142a9e53cc1b9cedb198a1bdae550378b` |
| [Phase 05 review](../../reviews/phase-05-review.md) | F-P05-001/F-P05-005/F-P05-011, §6 | `88ecf56c57556a0ae8d3578ec158561602222c14` |

Historical cross-check인 [2026-07-26 Master 초안](../../../2026-07-26-master-design.md)의 Git blob은 `d4f7fbf058711a02451de911bc86b1cd2f426218`이다. 이 값은 current authority fingerprint가 아니다. `docs/codex/`는 absent inventory이므로 Git blob이 없다.

Implementation start와 pre-review evidence seal 직전에 **모든 expected source**를 다음 machine-readable manifest로 검사한다. 한 행이라도 missing/mismatch면 non-zero로 끝내며, 이 예시의 TSV를 실행 중 자동 갱신하지 않는다.

```bash
(
set -eu

while IFS="$(printf '\t')" read -r expected_blob source_file
do
  actual_blob="$(git rev-parse --verify "HEAD:${source_file}")" || exit 41
  test "${actual_blob}" = "${expected_blob}" || exit 42
done <<'EXPECTED_SOURCE_BLOBS'
ace117c380466b733994a1fbb2a95d31e41b3959	docs/domain-design.md
81495ff448d0e618ab3563e8ff80614fb1028acf	docs/architecture-design.md
b507a5e7ba0b7e76475bc2d755493e814f4d053a	docs/master-design.md
0a02ba4c77a402455e3d80b76969dca28831b1e6	docs/2026-07-26-domain-design.md
d51339e251dee1e032e711144dc63d6d07d7323b	docs/2026-07-26-architecture-design.md
1199abf2cd52c801ec412bfbcf4729e2b5b29cf0	docs/architecture-domain-implementation-design.md
3fff4c583a54f02dea667e78c8e5187d65ec0e18	docs/master-design-open-questions.md
8a9cb4a29685a2540bd605c3ac63bb459052b2a1	docs/implementation/README.md
d7f6be4fff0089204fbdb52f731b2348407f36eb	docs/implementation/master-realization-plan.md
250aa90ae568a6b32ec905fa5ee456d430ff72cf	docs/implementation/execution-progress-and-results.md
e0a68fd442a7db383e4a650e4a561234c323b1fe	docs/implementation/phases/phase-04-capabilities-customer-profiles.md
bd7d11d478f8afbd69c6336c3b653cd89ea69ac2	docs/implementation/reviews/phase-04-review.md
74098f7e15cf75bcc443ae009cc475a9b60d63a3	docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md
e473887ffbbb165f18ea6aaa76d6fc3917fc4af2	docs/implementation/reviews/phase-03-review.md
0ea8046142a9e53cc1b9cedb198a1bdae550378b	docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
88ecf56c57556a0ae8d3578ec158561602222c14	docs/implementation/reviews/phase-05-review.md
d4f7fbf058711a02451de911bc86b1cd2f426218	docs/2026-07-26-master-design.md
EXPECTED_SOURCE_BLOBS
)
```

이 block은 현재 지원하는 `bash`와 `zsh`에서 block 전체를 그대로 실행한다. `source_file`은
zsh의 `$path` 특수 배열을 건드리지 않는다. Exit `0`은 17개 expected blob 일치,
`41`은 missing/unresolvable `HEAD:<source_file>`, `42`는 expected/actual mismatch다.

Expected HEAD 검사와 별도로 같은 exact path set의 live snapshot을 기록하고 working
bytes가 `HEAD`와 다르면 exit `43`으로 fail-closed한다.

```bash
(
set -eu

live_drift=0
git rev-parse HEAD
for source_file in \
  docs/domain-design.md \
  docs/architecture-design.md \
  docs/master-design.md \
  docs/2026-07-26-domain-design.md \
  docs/2026-07-26-architecture-design.md \
  docs/architecture-domain-implementation-design.md \
  docs/master-design-open-questions.md \
  docs/implementation/README.md \
  docs/implementation/master-realization-plan.md \
  docs/implementation/execution-progress-and-results.md \
  docs/implementation/phases/phase-04-capabilities-customer-profiles.md \
  docs/implementation/reviews/phase-04-review.md \
  docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md \
  docs/implementation/reviews/phase-03-review.md \
  docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md \
  docs/implementation/reviews/phase-05-review.md \
  docs/2026-07-26-master-design.md
do
  expected_blob="$(git rev-parse --verify "HEAD:${source_file}")" || exit 41
  working_blob="$(git hash-object "${source_file}")" || exit 44
  printf 'source_file=%s\nexpected_head_blob=%s\nworking_tree_blob=%s\n' \
    "${source_file}" "${expected_blob}" "${working_blob}"
  git status --short -- "${source_file}"
  git diff --name-only -- "${source_file}"
  git diff --cached --name-only -- "${source_file}"
  shasum -a 256 "${source_file}"
  test "${working_blob}" = "${expected_blob}" || live_drift=1
done

test "${live_drift}" -eq 0 || exit 43
)
```

`git hash-object`는 현재 working bytes의 Git object identity, SHA-256은 tool-independent working-byte identity다. 둘 다 expected HEAD blob을 대신하지 않는다. Untracked human guide/review처럼 expected HEAD blob이 없는 correction input은 `NO_HEAD_BLOB`과 working-byte SHA-256을 명시하고 implementation authority로 승격하지 않는다.

Live block의 exit `0`은 모든 working blob이 expected `HEAD`와 같다는 뜻이다. Exit
`43`은 하나 이상의 live drift, `44`는 missing/unreadable live source다. 현재
관찰에서는 scheduler-owned `execution-progress-and-results.md`의 live Git object가
expected HEAD blob과 다르다. 이는 exit `43`인 fail-closed drift이며 최신 진행
관찰에는 읽되 accepted source rebaseline 또는 Phase 04 evidence로 사용하지 않는다.

Source가 바뀌면 다음 다섯 가지를 기록한다.

1. 변경된 heading과 requirement
2. 이 가이드의 영향 section과 work package
3. Java contract/test/evidence 영향
4. OPEN/GATED/deferred 상태 변화와 승인 record
5. Expected HEAD rebaseline 승인자·새 manifest digest와 기존 evidence invalidation 범위

승인된 rebaseline 전에는 source drift를 무시하거나 새 hash만 복사하지 않는다. 이 검사를 implementation 시작과 pre-review seal에서 모두 반복하고, 두 snapshot이 다르면 evidence seal을 중단한다.

## 5. 시작 전 읽기 순서와 entry gate

### 5.1 정확한 읽기 순서

1. [Canonical Master §4](../../../master-design.md#4-구현-아키텍처와-책임-경계)에서 end-to-end 책임과 lifecycle을 읽는다.
2. [Master §5.3](../../../master-design.md#53-vehicle-size와-capability)에서 vehicle qualification과 executable capability를 분리한다.
3. [Master §9](../../../master-design.md#9-extensible-policy-evaluation과-profile-architecture)와 [RM-2](../../../master-design.md#154-rm-2--propagation-evaluation과-bound-profile)에서 evaluation/profile 책임과 deliverable을 읽는다.
4. [Final Domain primer](../../../2026-07-26-domain-design.md#primer), [immutable model](../../../2026-07-26-domain-design.md#7-immutable-solver-model), [evaluation](../../../2026-07-26-domain-design.md#evaluation), [새 customer 절차](../../../2026-07-26-domain-design.md#customer-extension)를 읽는다.
5. [Final Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계), [§5.2](../../../2026-07-26-architecture-design.md#52-artifact-configuration과-provenance), [§5.5~5.6](../../../2026-07-26-architecture-design.md#55-observability와-security)를 읽는다.
6. [Integrated design Phase 03](../../../architecture-domain-implementation-design.md#7-phase-3--경로-전파-계산과-평가-kernel), [Phase 04](../../../architecture-domain-implementation-design.md#8-phase-4--capability와-data-driven-customer-profile), [Phase 05](../../../architecture-domain-implementation-design.md#9-phase-5--stable-solution-insertion과-initial-portfolio)를 이어 읽는다.
7. [Question register](../../../master-design-open-questions.md)의 exact `Q-TIME-03`, `Q-COMP-01~02`, `Q-REQ-01~02`, `Q-OBJ-01~03`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` 행을 읽는다.
8. [Implementation README §3](../../README.md#3-source-authority)과 [§6](../../README.md#6-phase-작업-순서)에서 latest status와 ALNS-first gate를 확인한다.
9. [Master Plan Phase 04](../../master-realization-plan.md#phase-04--재사용-기능과-고객-profile), [test 전략](../../master-realization-plan.md#8-공통-테스트-전략), [evidence DAG](../../master-realization-plan.md#9-evidence-bundle-규칙), [DoD](../../master-realization-plan.md#11-definition-of-done)를 읽는다.
10. [Canonical Phase 04](../../phases/phase-04-capabilities-customer-profiles.md) 전체와 [독립 review](../../reviews/phase-04-review.md)를 함께 읽는다.
11. [Phase 03 handoff](../../phases/phase-03-route-propagation-evaluation-kernel.md#142-next--actual-document-unaccepted-phase-04)와 [Phase 05 consumer](../../phases/phase-05-pair-insertion-initial-portfolio.md#151-previous--actual-but-unaccepted-phase-0304)를 대조한다.
12. [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry)와 [§8](../../execution-progress-and-results.md#8-현재-blockers-open-gates와-남은-이슈)에서 live entry 상태를 확인한다.

### 5.2 Entry gate와 evidence 확인법

다음 조건은 모두 AND다.

| Gate | 받아야 할 evidence | 현재 관찰 | 행동 |
|---|---|---|---|
| Phase 00 accepted | Reactor, module DAG, wrapper, architecture rules, `E-P00-*`, review와 receipt | 미커밋 reactor/module/package/wrapper skeleton은 생겼지만 Phase 00 review/receipt가 없음 | Source 구현 시작 금지 |
| Phase 01 accepted | Service/trip/compatibility/ownership/mandatory normalized facts와 `E-P01-*` | 문서만 있고 accepted artifact 없음 | Source 구현 시작 금지 |
| Phase 02 accepted | Immutable problem/travel, exact fingerprints와 `E-P02-*` | implementation/evidence 미완료 | Source 구현 시작 금지 |
| Phase 03 accepted | Approved SPI/declarations/kernel, `E-P03-*`, review와 receipt | Review `CHANGES_REQUIRED`, implementation `NOT_STARTED` | Source 구현 시작 금지 |
| Full-solution evaluator | Exact owner/API/input/identity/reuse/invalidation/failure/comparator | Phase 03~05 residual blocker | Ad hoc aggregator 금지 |
| Business equality/tie | Equality API, solution tie와 insertion tie owner/order/fingerprint | 의미만 고정, API 미승인 | Context tie type freeze 금지 |
| Portfolio policy authority | Traversal, `CLOCK`, utilization missing/zero typed contract | Phase 05 review F-P05-011 residual | Hidden order/default 금지 |
| `ADR-003` | Registry assembly, descriptor/catalog format, canonical encoding, duplicate/authorization policy | 승인 record 없음 | Descriptor/public API 구현 freeze 금지 |
| Scheduler/owner | Exact task ID, implementer, independent reviewer | `TBD_NOT_SUPPLIED` | 상태 전이·acceptance 주장 금지 |

Facet은 별도 gate다. `ADR-004`가 없더라도 empty facet baseline과 unapproved-facet rejection 설계는 가능하지만 approved facet implementation/positive evidence는 만들 수 없다.

### 5.3 Producer artifact를 받을 때 확인할 equality

```text
ProblemInstance.preparedTravelFingerprint
  == PreparedTravel.fingerprint

Phase03ApiSignatureFingerprint
  == binder target contract fingerprint

all descriptor dependencies
  ⊆ accepted Phase03 fact/metric/unit contract

all approved facets
  have an independent Phase07 recomputation contract

full-solution evaluator input identity
  includes exact problem/travel/profile + ordered routes + bank

context tie
  applies only after observable business objective equality
```

Mismatch가 있으면 shim, fallback, default 또는 “임시 합산”으로 보완하지 않는다.

### 5.4 마지막 안전 지점

Entry가 닫힌 현재 허용 범위:

- 이 가이드와 authority 문서 읽기
- Descriptor schema/registry 선택지와 ADR 질문 정리
- Test-only literal descriptor, independent oracle와 faulty double 설계
- Cross-Phase solution evaluator/equality/portfolio policy review 준비
- Source fingerprint와 repository inventory 재확인

Entry 전에 금지:

- `rpdptw/core`, `rpdptw/capabilities`, `rpdptw/profile-catalog` production source 생성
- Root POM/module 변경
- Production customer descriptor/default/단가/threshold 추가
- Placeholder `AlnsBatchEngine`을 profile engine으로 포장
- Scheduler registry나 Phase acceptance 직접 승격

## 6. 실제 repository inventory: 현재와 목표

### 6.1 재현 가능한 read-only inventory 명령

```bash
git rev-parse HEAD
git branch --show-current
git status --short
find . -path './node_modules' -prune -o -path './target' -prune \
  -o -name pom.xml -print | sort
find rpdptw build legacy src -type f -name '*.java' -print 2>/dev/null | sort
test -x ./mvnw
java -version
mvn -version
```

`inventory_observed_at_commit`의 committed baseline은 단일 root application POM, main Java 6개와 test 1개였다. 이 가이드를 검증하는 동안 같은 checkout의 병렬 Phase 00 작업이 root reactor, `rpdptw/*`, `build/*`, `legacy/*` skeleton을 **미커밋 상태**로 만들고 기존 placeholder를 `legacy/gcp-placeholder`로 옮겼다. Scheduler-owned progress에는 Phase 00 Fix 01 in progress가 관측되지만 acceptance receipt는 없다. 아래 표는 `working_tree_cross_check_at` 시각의 live snapshot일 뿐이며 이후 외부 drift를 계속 추적하지 않는다. 구현을 실제 시작할 때 §6.1 inventory를 새로 실행하되, 파일 존재보다 accepted Phase 00~03 receipt와 exact digest를 우선한다. 미커밋 skeleton/test의 존재는 accepted architecture 또는 Phase 04 entry receipt가 아니다.

최종 cross-check 시 `test -x ./mvnw`는 zero이고, 미커밋 `mvnw`와 `.mvn/wrapper/maven-wrapper.properties`가 있다. 이것은 병렬 Phase 00 작업의 skeleton이지 accepted wrapper evidence가 아니다. Phase 00 review/receipt와 pinned distribution 검증이 끝난 뒤에만 `./mvnw`를 Phase 04 evidence의 고정 명령으로 사용한다.

### 6.2 존재, placeholder와 부재

| 항목 | 분류 | 2026-07-29 실제 관찰 | Phase 04 목표 |
|---|---|---|---|
| Root [`pom.xml`](../../../../pom.xml) | 미커밋 Phase 00 skeleton | Committed baseline은 단일 `com.ronext:ro-next`; working tree는 `com.ronext:ro-next-parent` reactor로 변경 중 | Reviewed/accepted business-dependency-free parent/reactor |
| Java/Maven | 존재 | Corretto 25.0.3, Maven 3.9.14, macOS aarch64 | Reproducible toolchain evidence |
| Root dependency placement | 이동 중 | Google Workflow/GCS/Jackson이 미커밋 [`legacy/gcp-placeholder/pom.xml`](../../../../legacy/gcp-placeholder/pom.xml)로 격리됨 | Stable core와 provider dependency 격리의 accepted evidence |
| [`AlnsBatchEngine`](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java) | legacy placeholder | `double`, `SplittableRandom`, synthetic objective `Map<String,Object>` | Phase 04 artifact로 재사용 금지 |
| [`AlnsBatchEngineTest`](../../../../legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java) | legacy placeholder test | Candidate status와 양수 objective만 검사 | Phase 04 evidence가 아님 |
| `rpdptw/core` | 미커밋 package skeleton | [`pom.xml`](../../../../rpdptw/core/pom.xml)과 `package-info.java` 8개, production domain/evaluation type와 test 0개 | Accepted Phase 01~03 stable API owner |
| `rpdptw/capabilities` | 미커밋 package skeleton | [`pom.xml`](../../../../rpdptw/capabilities/pom.xml)과 [`package-info.java`](../../../../rpdptw/capabilities/src/main/java/com/ronext/rpdptw/capability/package-info.java)만 존재 | Reusable capability production type와 test |
| `rpdptw/profile-catalog` | 미커밋 package skeleton | [`pom.xml`](../../../../rpdptw/profile-catalog/pom.xml)과 [`package-info.java`](../../../../rpdptw/profile-catalog/src/main/java/com/ronext/rpdptw/profile/catalog/package-info.java)만 존재 | Descriptor/catalog/authorization orchestration type와 test |
| `rpdptw/solver`, `verification`, `application` | 미커밋 package skeleton | POM과 `package-info.java`만 존재, production type/test 0개 | 이후 Phase consumer; Phase 04가 선행 구현하지 않음 |
| `build/test-fixtures` | 미커밋 package skeleton | POM과 test-scope `package-info.java`만 존재 | Accepted profile-local/공통 fixture policy에 맞는 최소 helper |
| `build/architecture-rules` | 미커밋 Phase 00 Fix 01 work | [`pom.xml`](../../../../build/architecture-rules/pom.xml)과 test Java 9개가 관측됨; Phase 04-specific rule과 accepted review/receipt는 없음 | Dependency/customer/reflection/raw-map guard와 accepted evidence |
| `./mvnw` | 미커밋 Phase 00 skeleton | Executable script와 wrapper properties 존재, acceptance/review 없음 | Phase 00 accepted/pinned wrapper |
| Production profile descriptor | 부재 | 승인된 customer/profile/preset 0건 | 별도 onboarding approval 뒤에만 추가 |
| Phase 04 production Java/test | 부재 | Capability/profile binding type와 Phase 04 test 0개 | 이 가이드의 gated 구현 대상 |
| `E-P04-*` | 부재 | `NOT_PRODUCED` | Immutable evidence bundle |

현재 working tree의 module 선택 문법을 확인하는 명령은 다음과 같다.

```bash
mvn -B -ntp -Dstyle.color=never \
  -pl rpdptw/capabilities,rpdptw/profile-catalog -am clean test

mvn -B -ntp -Dstyle.color=never \
  -pl legacy/gcp-placeholder -am clean test
```

첫 명령은 현재 skeleton에 Phase 04 test가 0개이고 parent가 `failIfNoTests=false`이므로 성공해도 false-green이다. 두 번째 명령은 legacy placeholder만 특성화한다. 두 명령 모두 Phase 00 작업이 미커밋인 동안 이 가이드 검증에서 실행하지 않으며, Phase 04 acceptance 판정에 사용하지 않는다. Phase 00 acceptance 뒤에는 wrapper와 accepted module 좌표를 다시 확인하고 §10~11의 fail-closed future command를 사용한다.

### 6.3 Proposed target file tree

Phase 00/03과 `ADR-003`이 같은 의미를 승인한 뒤의 후보다.

```text
rpdptw/core/
└── src/main/java/com/ronext/rpdptw/evaluation/
    ├── api/profile/
    │   ├── CustomerKey.java
    │   ├── ProfileKey.java
    │   ├── ProfileVersion.java
    │   ├── PresetKey.java
    │   ├── ProfileLookupCoordinate.java
    │   ├── CapabilityRegistryView.java
    │   ├── CapabilityProvider.java
    │   ├── CapabilityContract.java
    │   ├── CapabilityDependency.java
    │   └── AuthorizedProfileSelection.java
    └── runtime/profile/
        ├── ProfileBindingEngine.java
        ├── ProfileBindingResult.java
        ├── ProfileBindingFailure.java
        ├── BoundProfile.java
        ├── BoundProfileSnapshot.java
        └── internal/

rpdptw/capabilities/
└── src/
    ├── main/java/com/ronext/rpdptw/capability/
    │   ├── registry/
    │   └── <approved-reusable-business-name>/
    └── test/java/com/ronext/rpdptw/capability/

rpdptw/profile-catalog/
└── src/
    ├── main/java/com/ronext/rpdptw/profile/catalog/
    ├── main/resources/profiles/schemas/
    └── test/
        ├── java/com/ronext/rpdptw/profile/
        └── resources/profiles/test-only/

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase04ProfileArchitectureTest.java

build/profile-validation/                         # PROPOSED test-only composition owner
├── pom.xml                                       # Phase 00/Architecture approval required
└── src/test/
    ├── java/com/ronext/rpdptw/profilevalidation/
    │   ├── ProfileCompositionContractTest.java
    │   └── Phase05CoreOnlyConsumerCompileProbeTest.java
    └── consumer-java/                            # restricted classpath: rpdptw-core only
        └── Phase05CoreOnlyConsumer.java
```

`standard`, `servicelevel`, `fleetcost` 같은 package 이름은 승인된 capability가 생겼을 때의 ownership 예시일 뿐, 빈 production provider나 임의 정책을 미리 만들라는 뜻이 아니다.

Profile-specific builder와 oracle은 `rpdptw-profile-catalog/src/test`에 둔다. Phase 00의 generic test-fixtures에 profile-catalog production dependency를 역으로 추가하지 않는다.

`build/profile-validation`은 [Integrated design §3.2](../../../architecture-domain-implementation-design.md#32-전체-directory-tree)의 제안된 test-only composition seam을 Phase 04에 적용한 **후보**다. 현재 live reactor에는 이 module이 없고 Phase 00 acceptance도 없으므로 아직 만들거나 evidence로 세지 않는다. Phase 00/Architecture review가 승인하면 이 module만 test scope로 `rpdptw-core`, `rpdptw-capabilities`, `rpdptw-profile-catalog`를 소비한다. Production module 어느 것도 이 module을 의존하지 않으며, `core → capabilities/profile-catalog` 역방향 edge도 생기지 않는다.

실제 registry → catalog provider → core-owned binding engine → `BoundProfile` 조립은 이 test-only owner가 검증한다. Phase 05 compile probe는 composition module의 넓은 test classpath로 단순 compile하지 않고, 별도 fixture source를 **`rpdptw-core`만 있는 제한 classpath**로 compile해야 한다. 이 owner/제한 classpath가 승인·구현되기 전에는 cross-module integration evidence는 `NOT_PRODUCED`이며 sibling module build 성공을 대체 증거로 쓰지 않는다.

## 7. Scope, non-scope와 결정 상태

### 7.1 In scope

- Exact customer/profile/version/preset identity와 authorization
- Unique lookup coordinate와 same-coordinate overwrite/collision 거부
- Exact profile version이 선언한 omitted-preset default
- Explicit stable capability registry snapshot
- Capability exact key/version/contract/implementation fingerprint
- Typed parameter와 dependency/fact/metric/unit/output closure
- Problem compatibility/resource/service/trip/policy 조합 검증
- Phase 03 declaration/plan materialization
- Immutable portable snapshot과 executable runtime의 semantic equality
- Problem/travel/Phase03/build에 결합된 bound lifecycle
- Profile drift, corruption, isolation, reproducibility와 safe observability
- Empty/unapproved facet gate와 approved-if-applicable recomputation 조건
- Phase 05/07 customer-neutral handoff

### 7.2 Non-scope

- Phase 03 propagation/evaluation 의미나 signature를 임의 변경
- Raw input parsing, normalization, travel generation
- Pair position, insertion, route/bank mutation과 initial portfolio
- Full-solution aggregation API를 단독 발명
- ALNS operator, acceptance, step/worker/round/watchdog
- Candidate/result verifier verdict, final audit, publication
- Object storage, AWS/GCP adapter, dynamic plugin download
- Phase 13 route pool/MIP/backend
- Production customer descriptor, 단가, threshold, default preset 발명
- Public wire schema, canonical hash algorithm과 migration 자동 승인
- Multi-trip/rotation과 optional variant

### 7.3 확정된 의미

| 의미 | 상태 | 구현 판단 |
|---|---|---|
| Exact identity/no fallback | FIXED | `latest`, range, similarity, cross-customer fallback 0 |
| Exact declared default | FIXED | Omitted preset만 해당 profile version의 default로 materialize |
| Kernel preservation | FIXED | Binder는 accepted Phase 03 declaration/plan만 만든다 |
| Core rule preservation | FIXED | Pair/capacity/compatibility/resource/service/trip을 profile이 disable하지 못함 |
| Typed closure | FIXED | Raw map/coercion/script/class-name 실행 금지 |
| Immutable problem-bound lifecycle | FIXED | 다른 problem/travel에 재사용 금지 |
| Mandatory/ownership meaning | FIXED | Lexicographic mandatory, LEASE dimension 계약, no Big-M |
| Explicit inventory | PROPOSED, approval required | Composition root가 stable registry를 주입 |
| Snapshot/runtime split | PROPOSED | Portable data와 executable runtime을 같은 semantic fingerprint로 연결 |

### 7.4 OPEN, GATED, deferred와 사람 승인

| 항목 | 상태 | Owner | Last safe point | Resume 조건 |
|---|---|---|---|---|
| Descriptor/catalog/registry format | `OPEN / ADR-003 REQUIRED` | Architecture + Product/API/Data + Security | Semantic field와 no-fallback contract | Canonical encoding/schema/registry/duplicate/authorization 승인 |
| Full-solution evaluator | `CROSS-PHASE BLOCKER` | Core/Evaluation + Profile + Phase 05 | Route kernel + ordered declaration + immutable routes/bank | Exact API/identity/reuse/failure/comparator와 equality/corruption evidence |
| Business equality/tie | `CROSS-PHASE BLOCKER` | Core/Evaluation + Phase 05/06 | Ordered business vector; objective-first 의미 | Equality/solution tie/insertion tie owner/order/fingerprint 승인 |
| Traversal/`CLOCK`/utilization | `CROSS-PHASE AUTHORITY BLOCKER` | Algorithm + Domain/Input + Profile + Phase 05 | Policy role only, missing `CLOCK → UNAVAILABLE` | Reference vectors, typed edge policy, golden trace |
| `SolvePlan` final type/name | `PROPOSED/OPEN P-04` | Product + Algorithm + Profile | Typed exact reference semantics | Cross-Phase vocabulary/fingerprint 승인 |
| Typed facet | `OPEN / ADR-004` | Domain + Capability + Verification | Empty set + unapproved reject | Typed state, Phase 03/07 recomputation 승인 |
| Production profile | `BLOCKED FOR ONBOARDING` | Product/Profile/Security | Empty production catalog | Exact descriptor/preset/auth/parameter approval |
| `Q-BENCH-02` 수치 | `OPEN — EXPERIMENT_REQUIRED` | Benchmark/Quality | No official numeric field | Calibration과 explicit approval |
| `C-17` MIP | `GATED` | Product/Algorithm/Architecture 외 | ALNS-only path | Phase 14A receipt + 모든 별도 backend/운영 승인 |
| `Q-VAR-01` | `DEFERRED` | Product/Domain/Algorithm | Current single-trip contract | Representative fixture와 별도 승인 |
| Multi-trip/rotation | `DEFERRED FEATURE` | Product/Domain/Algorithm | Oneway + single roundtrip | Exact trip/reset/depot/resource/pair contract |
| Phase 14B production | `AUTHORITY NOT GRANTED` | Product/Operations/Security/Release | Local/benchmark evidence only | Shadow/rollback/operations와 explicit authority |

### 7.5 사람 checkpoint와 마지막 안전 지점

다음 결정을 자동으로 내리지 않는다.

- Descriptor를 JSON/YAML 중 무엇으로 할지
- Canonical field order/hash algorithm
- Registry를 generated inventory, explicit constructor list 등 어떤 방식으로 만들지
- `SolvePlan`과 operator/budget reference의 최종 type/name
- Solution evaluator/equality/tie API
- `CLOCK` 축·방향과 utilization zero/missing 의미
- Production customer의 단가/default/threshold

이 결정이 없으면 가장 가까운 checkpoint에서 중단한다. 구현 편의를 위해 library default, enum ordinal, classpath order 또는 test fixture 값을 production 의미로 올리지 않는다.

## 8. 학습 경로: 개념에서 실제 통합까지

### 8.1 단계 A — 개념 지도

해야 할 일:

1. Vehicle qualification, evaluation capability, algorithm operator를 세 칸으로 나눈다.
2. Descriptor, registry, binder, bound snapshot/runtime의 owner와 lifecycle을 그린다.
3. Phase 03 SPI와 Phase 05/07 consumer가 보는 contract를 표시한다.
4. Core rule과 profile-selectable rule을 사례별로 분류한다.

작은 연습:

| 요구 | 가장 좁은 seam | 이유 |
|---|---|---|
| km 단가 변경 | Profile parameter | 기존 distance metric으로 충분 |
| Objective 순서 변경 | Profile preset | 물리 계산 변화 없음 |
| 새 지각 비용 함수 | Reusable score capability | 기존 arrival/lateness facts 소비 |
| 배터리 SOC | Typed facet 후보 | 다음 방문 feasibility에 영향을 주는 새 물리 상태 |
| 고객 이름별 차량 예외 | 금지 | Customer branch로 core rule을 우회 |

완료 신호:

- 같은 요구를 profile/capability/facet 중 어디에 둘지 설명할 수 있다.
- Vehicle capability와 evaluation capability를 혼동하지 않는다.
- “고객 한 명 추가 = POM 추가”가 아닌 이유를 설명할 수 있다.

자문:

- 이 요구는 새 물리 상태인가, 기존 fact의 가격화인가?
- Customer 이름 없이 업무 의미로 capability를 이름 붙일 수 있는가?

### 8.2 단계 B — 작은 탐색과 test-only 실습

Production source 전에 literal test-only 모델을 손으로 만든다.

```text
Customer A / Profile base / Version 1
  default preset = economy
  capabilities = distance-metric@1, vehicle-volume-cost@1

Customer B / Profile base / Version 1
  no default preset
  capabilities = distance-metric@1, lateness-cost@2
```

확인할 counterexample:

- Omitted preset이 A의 exact default로만 resolve된다.
- B의 omitted preset은 reject된다.
- `latest`는 reject된다.
- A의 preset key가 B에도 있어도 cross-customer 선택은 reject된다.
- Registry에 같은 key/version 두 구현이 있으면 first-wins가 아니라 construction failure다.
- Map field 순서와 unordered capability/dependency 선언의 permutation은 같은 의미와 fingerprint를 만든다.
- Objective dimension 또는 solve stage의 sequence를 바꾸면 다른 plan/fingerprint와 comparator behavior를 만든다.
- Unauthorized caller는 catalog read 전에 종료된다.

완료 신호:

- Expected result를 production binder helper 없이 literal 표로 계산한다.
- 최소 counterexample가 fallback, hidden default, wrong precedence를 실제 red로 만든다.
- 모든 key와 수치가 `TEST_ONLY`임을 fixture 이름과 evidence에 표시한다.

자문:

- Oracle이 production resolver와 같은 함수를 호출하고 있지 않은가?
- 실패 precedence가 collection iteration order에 따라 바뀌지 않는가?

### 8.3 단계 C — 실제 변경

Entry와 ADR가 열린 뒤 다음 순서로 변경한다.

1. Core-owned identity/value/failure skeletal contract
2. Catalog exact lookup와 two-stage authorization
3. Explicit registry와 typed provider contract
4. Pure binder와 stable dependency closure
5. Problem/Phase03 materialization
6. Snapshot/runtime lifecycle, isolation, corruption와 reproducibility
7. Architecture/security evidence

각 단계는 red test → minimal green → refactor 순서다. 한 work package가 green이 되기 전에 다음 package의 fallback을 만들어 덮지 않는다.

완료 신호:

- Binding failure 뒤 kernel/search 호출 수가 0이다.
- Bind 뒤 catalog/registry/authorization 호출 수가 0이다.
- Same inputs는 sequential/parallel/backend order에 무관하게 exact 같은 bytes/fingerprint를 만든다.

### 8.4 단계 D — 통합과 handoff

통합 학습의 핵심은 “내 module test green”보다 consumer가 다시 해석할 필요가 없는가다.

확인:

- Phase 05 test consumer가 core bound API만 import한다.
- Phase 07 verifier가 catalog나 concrete profile module을 compile-depend하지 않는다.
- Direct Phase 03 declaration과 bound declaration의 route artifact가 exact 같다.
- Solution evaluator/tie/portfolio blocker의 승인 여부가 handoff manifest에 명시된다.
- Evidence → review → receipt가 단방향 digest DAG다.

완료 신호:

- Catalog를 제거한 consumer test에서도 이미 bound된 solve가 실행 가능하다.
- Corrupted snapshot은 evaluation 전에 reject된다.
- Downstream이 customer ID로 분기할 이유가 없다.

## 9. Java skeletal 설계 안내

### 9.1 Package와 dependency 방향

```text
rpdptw-core evaluation.api/runtime
          ↑                  ↑
rpdptw-capabilities   rpdptw-profile-catalog
          ↑                  ↑
          └── distribution composition root ──┘

rpdptw-solver       → rpdptw-core bound/evaluation API
rpdptw-verification → rpdptw-core cache-free contract

build/profile-validation -TEST→ rpdptw-core
                         -TEST→ rpdptw-capabilities
                         -TEST→ rpdptw-profile-catalog
```

금지 방향:

- `rpdptw-core → rpdptw-capabilities/profile-catalog`
- Capability/profile → solver/search/cache/application/provider SDK
- Application generic module → concrete capability/profile implementation
- Phase 05/07 → catalog lookup, descriptor parser, classpath scan
- Profile/capability → AWS/GCP/storage/workflow/HTTP type
- Production module → `build/profile-validation`

### 9.2 Identity value 후보

아래 record는 validation과 canonical encoding을 생략한 skeletal 예시다.

```java
// PROPOSED INTERNAL — public/wire API가 아니다.
public record CustomerKey(String value) {}
public record ProfileKey(String value) {}
public record ProfileVersion(String value) {}
public record PresetKey(String value) {}
public record CapabilityKey(String value) {}
public record CapabilityVersion(String value) {}

public record ProfileLookupCoordinate(
    CustomerKey customer,
    ProfileKey profile,
    ProfileVersion version
) {}
```

실제 구현에서는 compact constructor가 null/blank/format을 검증하고 canonical identity rule을 따른다. `toString()`이나 record field iteration order를 fingerprint로 사용하지 않는다.

### 9.3 Capability SPI 후보

Provider는 다른 Maven module이 구현해야 하므로 cross-module sealed hierarchy로 만들지 않는다.

```java
// PROPOSED INTERNAL
public interface CapabilityParameters {}

public interface CapabilityRegistryView {
    CapabilityResolution resolveExact(
        CapabilityKey key,
        CapabilityVersion version
    );

    CapabilityRegistrySnapshot snapshot();
}

public interface CapabilityProvider<P extends CapabilityParameters> {
    CapabilityContract<P> contract();

    BoundCapability bind(
        CapabilityBindingContext context,
        P parameters
    );
}
```

`Class<P>` 같은 type token을 contract에 포함할 수는 있지만 descriptor가 arbitrary class name을 지정하거나 reflection으로 instantiate하는 권한으로 사용하지 않는다. Parameter codec/schema exact API는 `ADR-003` 전까지 open이다.

Dependency는 sealed data hierarchy 후보로 표현할 수 있다.

```java
// PROPOSED INTERNAL
public sealed interface CapabilityDependency
        permits CapabilityDependency.Fact,
                CapabilityDependency.Metric,
                CapabilityDependency.Capability,
                CapabilityDependency.FacetContract {

    record Fact(FactKey key, EvaluationUnit unit, ValueType type)
        implements CapabilityDependency {}

    record Metric(MetricKey key, EvaluationUnit unit, ValueType type)
        implements CapabilityDependency {}

    record Capability(CapabilityKey key, CapabilityVersion version)
        implements CapabilityDependency {}

    record FacetContract(FacetKey key, ContractVersion version)
        implements CapabilityDependency {}
}
```

Sealed hierarchy는 known result/data variants를 닫을 때 사용한다. 외부 module provider 확장을 막는 위치에 쓰지 않는다.

### 9.4 Binding result와 failure 후보

```java
// PROPOSED INTERNAL
public interface ProfileBindingEngine {
    ProfileBindingResult bind(ProfileBindingRequest request);
}

public sealed interface ProfileBindingResult
        permits ProfileBindingResult.Bound,
                ProfileBindingResult.Rejected {

    record Bound(BoundProfile profile) implements ProfileBindingResult {}
    record Rejected(ProfileBindingFailure failure)
        implements ProfileBindingResult {}
}

public sealed interface ProfileBindingFailure
        permits ProfileBindingFailure.NotFound,
                ProfileBindingFailure.Unauthorized,
                ProfileBindingFailure.CatalogIntegrityFailure,
                ProfileBindingFailure.UnknownCapability,
                ProfileBindingFailure.ContractViolation,
                ProfileBindingFailure.UnsupportedProblemCombination,
                ProfileBindingFailure.AuthorityMismatch,
                ProfileBindingFailure.FacetNotApproved {
    BindingFailureCode code();
    SafeBindingLocation location();
}
```

Subtype 이름과 field는 proposed다. Stable code는 최소 다음을 구분해야 한다.

- Not found와 unauthorized outward non-enumerating contract
- Descriptor/schema/content collision
- Duplicate registry identity와 implementation drift
- Missing/cycle/duplicate dependency
- Parameter/type/unit/range/direction mismatch
- Core-rule override와 unsupported trip/facet
- Problem/travel/Phase03 authority mismatch

### 9.5 Bound artifact 후보

```java
// PROPOSED INTERNAL
public record BoundProfile(
    BoundProfileIdentity identity,
    BoundProfileSnapshot snapshot,
    BoundCapabilitySet capabilities,
    PropagationDeclaration propagationDeclaration,
    EvaluationPlan evaluationPlan,
    SolvePlan solvePlan,
    BoundProfileFingerprint fingerprint
) {
    // 실제 구현: defensive copy, transitive immutability,
    // snapshot/runtime semantic fingerprint equality 검증.
}
```

Java record는 자동으로 deep immutable하지 않다. List/Map/array/bitset을 compact constructor에서 defensive copy하고 accessor mutation probe를 test한다.

`SolvePlan`, stage/guard/operator/budget type 이름은 **PROPOSED/OPEN (`P-04`)**다. Reference는 exact typed key/version declaration이어야 하며 구현 object, numeric run budget, seed, mutable counter가 아니다.

### 9.6 Catalog resolution과 pure binder를 분리하는 pseudocode

Catalog resolution은 I/O와 authorization을 포함할 수 있다.

```text
resolveProfile(access, requestedExactIdentity, optionalPreset):
    reject alias/range/"latest"
    preAuthorize requested tenant/customer/profile/version scope
        or return safe ACCESS_DENIED before catalog read

    descriptor = tenantScopedCatalog.findExact(coordinate)
        or return PROFILE_NOT_FOUND
    verify schema + canonical content fingerprint
        or return CATALOG_INTEGRITY

    preset =
        explicit exact preset
        or descriptor.exactDeclaredDefault
        or return DEFAULT_PRESET_NOT_DECLARED

    authorize exact verified descriptor + preset
        or return safe ACCESS_DENIED

    return AuthorizedProfileSelection
```

그 뒤 pure binder는 외부 상태를 읽지 않는다.

```text
bind(request):
    require authorization covers exact descriptor/preset
    require problem.preparedTravelFingerprint == travel.fingerprint
    require Phase03 contract identity == accepted handoff

    resolve every capability by exact key/version
    reject duplicate identity or same identity/different fingerprint
    decode only approved typed parameters
    validate required/range/unit/value without coercion

    graph = fact/metric/capability/facet dependencies
    reject missing, duplicate output, cycle, ambiguous provider
    closure = stable topological order

    reject core-rule override
    validate compatibility/resource/service/trip combination
    validate mandatory/LEASE objective order
    validate facet approval/recomputation contract

    propagation = materialize accepted Phase03 declaration
    evaluation = materialize accepted Phase03 plan

    snapshot = immutable canonical snapshot
    runtime = immutable executable capability set
    require snapshot.semanticFingerprint == runtime.semanticFingerprint

    return BoundProfile
```

모든 배열을 unordered로 취급하지 않는다. Descriptor schema는 각 field를 다음처럼 먼저 분류해야 한다.

| Semantic kind | 예 | Canonical/fingerprint rule |
|---|---|---|
| Keyed map | Typed parameter map, named config field | 승인된 canonical key order로 정규화; source field order는 의미가 아님 |
| Unordered declaration set | Capability exact key/version 집합, 독립 dependency 선언 집합 | Duplicate를 거부하고 stable exact key/version/type로 정렬; permutation은 같은 fingerprint |
| Dependency DAG | Capability/fact/metric/facet edge | Edge 의미를 보존하고 dependency-first topological order; 독립 node만 stable key로 tie |
| Ordered business sequence | Objective priority dimensions | 입력 sequence ordinal을 그대로 보존하고 fingerprint; swap은 다른 comparator/plan |
| Ordered execution sequence | `SolvePlan` stage sequence | 입력 sequence ordinal을 그대로 보존하고 fingerprint; swap은 다른 plan |
| Approved ordered tie sequence | Business equality 뒤의 solution tie order | 승인된 순서만 보존/fingerprint; 미승인 상태에서는 empty/blocker |
| Schema가 아직 분류하지 않은 collection | Operator/guard/budget reference 등 | Unordered라고 추정하지 않고 `ADR-003`/cross-Phase review까지 binding freeze |

따라서 registry insertion order, keyed map field order와 **unordered로 선언된** capability/dependency permutation은 의미를 바꾸지 않는다. 반대로 objective dimension, solve stage와 승인된 tie order는 business/algorithm 의미이므로 sequence ordinal을 canonical bytes에 포함한다. Exact wire encoding과 hash algorithm은 계속 `ADR-003`의 `OPEN` 항목이다.

### 9.7 State transition과 실패 precedence

여러 defect가 동시에 있는 fixture에서도 failure가 iteration order에 따라 달라지지 않아야 한다.

```text
REQUEST SYNTAX / EXACT-REFERENCE REJECTION
  (no catalog lookup)
→ REQUESTED-SCOPE AUTHORIZATION
  (denial: safe ACCESS_DENIED, catalog read count = 0)
→ TENANT-SCOPED LOOKUP / CATALOG INTEGRITY / EXACT PRESET RESOLUTION
→ VERIFIED DESCRIPTOR + PRESET AUTHORIZATION
→ REGISTRY IDENTITY
→ PARAMETER / DEPENDENCY / UNIT CONTRACT
→ CORE RULE OVERRIDE / UNSUPPORTED TRIP / FACET
→ MANDATORY / LEASE OBJECTIVE POLICY
→ ALLOW
```

Pre-read requested-scope denial은 catalog not-found, unsupported schema, content corruption과 preset-not-found보다 항상 먼저이며 outward shape가 모두 같다. Exact verified descriptor+preset authorization은 lookup/verification 뒤의 **별도 ordinal**이고 pre-read authorization과 합치지 않는다. Registry/binder validation은 두 authorization이 통과한 뒤에만 호출한다.

문법/exact-reference rejection도 alias/range/`latest`를 catalog lookup 없이 거부한다. 다만 outward public error shape가 authorization과 어떻게 조합되는지는 Product/API/Security review 대상이며, 이 문서가 새 public code를 확정하지 않는다. 내부 stable stage/ordinal은 catalog I/O count와 함께 evidence에 남긴다.

Exact subtype field와 public code는 review 대상이지만, 위 cross-stage order 자체는 fail-closed contract다. Production validator와 independent oracle이 서로 다른 iteration helper를 쓰면서 같은 stable ordinal을 내야 한다. Multi-defect sensitivity fixture는 unauthorized+absent, unauthorized+corrupt, authorized+corrupt, authorized+missing preset, verified-selection denial을 각각 고정한다.

## 10. 순서 있는 work packages

모든 WP는 이전 WP의 accepted output을 전제로 한다. 아래 command는 **Phase 00 reactor/wrapper와 target module이 실제 생성되고, Phase 00~03 handoff 및 관련 ADR가 승인된 뒤에만** 실행하는 future command다.

### WP-04.0 — Entry, authority와 cross-Phase contract freeze

**목적과 이유**

구현 전에 source, predecessor identity와 세 residual cross-Phase blocker를 확정한다. 이 단계를 건너뛰면 route-level 값을 solution objective로 합치거나 context tie를 business objective에 섞는 구현이 생긴다.

**사전조건**

- Scheduler task/owner가 지정됨
- Phase 00~03 acceptance receipt와 evidence digest가 있음
- Phase 03 SPI signature/unit/failure identity가 있음

**예상 변경**

- `ADR-003` 또는 동등한 approved decision
- 필요할 때 `ADR-004`
- Cross-Phase solution evaluator/equality/portfolio policy review artifact
- 이 WP에서는 production source를 만들지 않는다

**구체 행동**

1. §4의 expected HEAD blob 전부와 live working-byte snapshot을 fail-closed로 재검증한다.
2. Phase 02 problem/travel과 Phase 03 declaration fingerprint를 대조한다.
3. Descriptor/schema/registry/authorization 선택지를 review한다.
4. Full-solution evaluator 입력·owner·identity·failure·reuse/invalidation을 공동 승인한다.
5. Business equality, solution tie, insertion tie를 분리한다.
6. Traversal/`CLOCK`/utilization은 승인되지 않았으면 related reference를 empty로 유지한다.
7. Production profile authority 부재를 명시한다.

**설계 근거**

Phase 04 review의 residual 3건은 Phase 04 문서만으로 닫을 수 없다. Last safe point는 Phase 03 route kernel + ordered declarations + immutable routes/bank다.

**금지 shortcut**

- Historical API 이름 복원
- Route vector 합 또는 insertion delta를 solution authority로 사용
- Test fixture 좌표/ratio를 production policy로 승격
- `latest`, hidden default, classpath scan 채택

**검증**

```bash
# §4.2 expected-source manifest check: any missing/mismatch exits non-zero
# then record exact-source-path scoped status/diff-name/hash-object/SHA-256.
git diff --check -- \
  docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md
```

**기대 결과**

- Expected HEAD/source working-byte drift 0 또는 승인 전 implementation stop
- Required ADR/joint review record 존재
- Production descriptor 0건임을 명시
- OPEN/GATED/deferred 상태가 그대로임

**실패 해석과 rollback**

Authority, source bytes 또는 API가 불일치하면 production source를 시작하지 않는다. Changed heading → requirement → WP/test/evidence 영향과 invalidated artifact를 기록하고 approved rebaseline 전에는 hash만 갱신하지 않는다. 이 가이드, literal oracle 설계와 마지막 accepted predecessor artifact로 돌아간다.

**다음 handoff**

Approved identity/failure/registry/schema/cross-Phase contract와 red-test manifest를 WP-04.1에 넘긴다.

### WP-04.1 — Core-owned selection contract, exact catalog와 two-stage authorization

**목적과 이유**

먼저 `rpdptw-core`에 provider-neutral identity/selection/result/failure/binding API의 skeletal contract를 두고 core contract test를 고정한다. 그 다음 profile-catalog가 이 contract를 구현·생산하게 하여 exact coordinate와 immutable definition을 만들고, unauthorized caller가 profile 존재·schema·integrity를 열거하지 못하게 한다.

**사전조건**

- WP-04.0 승인
- `ADR-003` schema/canonical encoding/authorization 결정

**예상 file/package/type와 순서**

1. `rpdptw-core/evaluation/api/profile`: `ProfileLookupCoordinate`, `VerifiedProfileDefinition`, `ResolvedPresetDefinition`, `AuthorizedProfileSelection`
2. `rpdptw-core/evaluation/runtime/profile`: `ProfileBindingEngine`, `ProfileBindingResult`, `ProfileBindingFailure`
3. `rpdptw-core/src/test`: provider-neutral ownership, failure-stage와 immutability contract test
4. `rpdptw-profile-catalog`: `ProfileCatalog`, `ProfileAuthorization`, canonical reader/integrity validator와 catalog implementation tests

이 이름과 subtype은 모두 proposed internal skeletal contract이며 public wire API가 아니다. Descriptor wire schema/encoding은 `ADR-003` 승인 전 구현 freeze다.

**구체 행동**

1. Core-owned exact identity/value/result/failure API와 provider-neutral contract test를 먼저 만든다.
2. Core contract가 catalog implementation, descriptor parser와 concrete capability를 import하지 않는지 compile rule로 확인한다.
3. Profile-catalog가 core-owned output을 생산하도록 구현한다. 같은 contract를 catalog module에 복제하지 않는다.
4. Exact customer/profile/version coordinate value를 만든다.
5. Same coordinate의 다른 schema/content/default 병렬 등록과 overwrite를 거부한다.
6. Unsupported schema에서 이웃 version으로 fallback하지 않는다.
7. Omitted preset은 exact declared default만 materialize하고 provenance를 남긴다.
8. §9.7 순서대로 문법/exact reference를 lookup 없이 거부하고 requested-scope authorization을 catalog read 전에 수행한다.
9. Tenant-scoped lookup/integrity/preset resolution 뒤 verified descriptor+exact preset authorization을 별도 stage로 수행한다.
10. Pre-read denial에서 catalog read count가 0이고 outward denial이 absence/schema/integrity에 따라 달라지지 않게 한다.

**설계 근거**

Lookup coordinate와 descriptor identity를 분리해야 schema가 fallback selector로 오용되지 않는다. Pre-read authorization은 tenant enumeration을 막는다.

**금지 shortcut**

- `latest`, version range와 name similarity
- Same identity overwrite
- Global default preset
- Unauthorized 뒤 catalog read
- Error message에 raw descriptor/customer PII 노출
- Catalog-owned duplicate identity/result/failure API
- `rpdptw-core → rpdptw-profile-catalog` dependency

**Future verification**

```bash
# 먼저 §11.6의 isolated-repository reactor freshness prelude를 같은
# P04_M2/source commit/method manifest로 완료해야 한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/core/pom.xml \
  -Dtest=ProfileSelectionContractTest,ProfileFailurePrecedenceContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/profile-catalog/pom.xml \
  -Dtest=ProfileIdentityResolutionTest,ProfileAuthorizationSecurityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과**

- Exact method가 fresh Surefire report에 존재
- Core contract tests가 catalog classpath 없이 green이고 core→catalog edge 0
- Latest/range/similarity/cross-customer/content drift 허용 0
- Pre-read denial에서 catalog read count 0
- Pre-read denial이 not-found/corruption보다 우선하고 post-verification denial은 별도 ordinal
- Required failed/error/skipped 0

**실패 해석과 rollback**

Fallback이나 overwrite가 필요해 보이면 schema/identity 결정이 잘못된 것이다. WP-04.1 source를 handoff 후보에서 제외하고 WP-04.0 decision을 다시 연다.

**다음 handoff**

Core-owned verified immutable selection/failure/binding contract와 이를 생산하는 catalog implementation을 WP-04.2에 넘긴다.

### WP-04.2 — Explicit capability registry와 typed provider contract

**목적과 이유**

이 build에 포함된 capability의 exact inventory를 만들고 duplicate, implementation drift와 untyped parameter를 solve 전에 차단한다.

**사전조건**

- WP-04.1 green
- Accepted Phase 03 key/unit/fact/metric SPI
- Distribution composition root contract 승인

**예상 file/package/type**

- Core: `CapabilityRegistryView`, `CapabilityContract`, `CapabilityDependency`
- Capabilities: `ExplicitCapabilityRegistry`, `ExplicitCapabilityInventory`
- Test: `CapabilityRegistryContractTest`, `CapabilityImplementationContractTest`

**구체 행동**

1. Exact key/version/contract/implementation fingerprint를 등록한다.
2. Duplicate identity와 same identity/different implementation을 registry construction에서 거부한다.
3. Registry snapshot과 unordered declaration set만 stable key/version으로 canonicalize한다.
4. Typed parameter record와 provider-owned codec/schema만 허용한다.
5. Bound capability가 scratch/clock/random/mutable registry를 보유하지 않게 한다.
6. Vehicle/evaluation/operator/budget key namespace를 서로 분리한다.
7. Objective priority, solve stage와 승인된 tie order는 sequence ordinal을 보존하고 fingerprint한다.

**설계 근거**

Classpath scan/first-wins는 build와 registration order에 따라 결과를 바꾼다. Explicit inventory는 reproducibility와 공급망 review를 가능하게 한다.

**금지 shortcut**

- `ServiceLoader`, reflection scan, runtime JAR download
- `Map<String,Object>` parameter
- String→number/unit coercion
- Customer ID를 capability input으로 전달
- Operator reference를 evaluation registry에서 resolve

**Future verification**

```bash
# §11.6 isolated-repository reactor freshness prelude required.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/capabilities/pom.xml \
  -Dtest=CapabilityRegistryContractTest,CapabilityImplementationContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/core/pom.xml \
  -Dtest=RouteEvaluationLayerTest,RouteEvaluationContractCorruptionTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과**

- Exact registry/typed parameter methods 전부 green
- Provider insertion order와 unordered declaration permutation fingerprint 동일
- Objective dimension/stage swap은 다른 fingerprint/plan/comparator result
- Accepted Phase 03 regression unchanged

**실패 해석과 rollback**

Phase 03 signature 변경, customer/search/cloud dependency가 필요하면 ownership이 잘못됐다. 새 capability implementation을 handoff에서 제거하고 accepted Phase 03 API로 돌아간다.

**다음 handoff**

Immutable `CapabilityRegistrySnapshot`과 typed provider contract를 WP-04.3에 넘긴다.

### WP-04.3 — Pure binder, dependency closure와 policy combination oracle

**목적과 이유**

Verified descriptor와 registry/problem/Phase03 contract를 외부 상태 없이 결합하고 모든 잘못된 조합을 kernel/search 전에 typed rejection한다.

**사전조건**

- WP-04.1/2 green
- Exact problem/travel fixture
- Accepted Phase 03 declaration/plan
- Full-solution/equality blocker의 승인된 contract 또는 관련 field를 freeze하지 않는 명시적 safe path

**예상 file/package/type**

- `evaluation.runtime.profile`
- `DefaultProfileBindingEngine`
- `DependencyClosureValidator`
- `ProblemProfileCompatibilityValidator`
- `BoundProfileFingerprintMaterializer`
- Profile-local test builder와 `ExhaustiveProfileBindingOracle`

**구체 행동**

1. Exact registry resolution과 typed parameter decode를 수행한다.
2. Dependency graph의 missing/duplicate/cycle/ambiguous output을 거부한다.
3. Fact/metric unit/type와 objective direction/order를 검사한다.
4. Core compatibility/resource/service/trip rule override를 거부한다.
5. Mandatory/LEASE objective 조합을 검사한다.
6. Approved facet contract 또는 empty set을 검사한다.
7. Stable topological closure에서 Phase 03 declaration/plan만 materialize하되 objective/stage sequence ordinal을 보존한다.
8. Literal matrix oracle과 production binder 결과를 전수 비교한다.

**설계 근거**

Binder는 Phase 02 facts를 재계산하는 곳이 아니다. Profile이 accepted Phase 03 contract로 표현 가능한지, core rule과 모순되지 않는지를 확인하는 곳이다.

**금지 shortcut**

- Missing dependency를 0/`Long.MAX_VALUE`로 채우기
- Oneway return arc나 rotation 합성
- Zero-eligible request를 binding defect로 처리
- Mandatory를 hard/Big-M으로 변환
- `LEASE` fleet인데 outsourced dimension을 숨게 삽입
- 첫 defect iteration order를 failure precedence로 사용
- Objective/stage/tie sequence를 key-sort하여 semantic order를 지우기

**Future verification**

```bash
# §11.6 isolated-repository reactor freshness prelude required.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/profile-catalog/pom.xml \
  -Dtest=ProfileBinderContractTest,ProfileCombinationOracleTest,ProfileObjectivePolicyTest,Phase04OracleSensitivityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과**

- 모든 generated stable ordinal의 expected variant/code exact equality
- Invalid row의 component/kernel call count 0
- Latest/first-wins/hidden-default/wrong-precedence faulty double이 모두 assertion red
- Pre-read auth denial의 catalog read count 0과 post-verified auth 별도 ordinal
- Map/unordered declaration permutation은 same; objective/stage swap은 different fingerprint/plan/comparator
- Valid row는 stable immutable bound candidate 생성

**실패 해석과 rollback**

Production binder helper를 oracle에서 재사용했거나 seeded fault가 green이면 oracle이 독립적이지 않다. Evidence를 폐기하고 literal table과 minimal counterexample부터 다시 만든다.

**다음 handoff**

Validated closure와 bound snapshot/runtime 후보를 WP-04.4에 넘긴다.

### WP-04.4 — Lifecycle, isolation, corruption, security와 facet gate

**목적과 이유**

Bound artifact가 한 solve에만 속하고, bind 뒤 catalog/registry state와 분리되며, drift/corruption/parallel execution에도 의미가 바뀌지 않는지 검증한다.

**사전조건**

- WP-04.3 binder green
- Canonical fingerprint field 결정
- `ADR-004` status가 explicit

**예상 file/test**

- `BoundProfile`, `BoundProfileSnapshot`, identity/fingerprint materializer
- `BoundProfileLifecycleTest`
- `BoundProfileIsolationTest`
- `BoundProfileCorruptionTest`
- `BoundProfileReproducibilityTest`
- `ProfileBindingSecurityObservabilityTest`
- `FacetBindingGateTest`

**구체 행동**

1. Snapshot/runtime semantic fingerprint equality를 강제한다.
2. Collection/array/bitset을 defensive copy한다.
3. 다른 problem/travel 재사용을 evaluation 전에 거부한다.
4. Bind 뒤 catalog/registry/authorization 호출을 0으로 만든다.
5. Same problem에 profile A/B를 독립 bind하고 state alias 0을 확인한다.
6. Unrelated profile 추가가 기존 fingerprint를 바꾸지 않게 한다.
7. One-field descriptor/registry/implementation/auth/closure drift를 거부한다.
8. Safe observability allowlist와 sentinel redaction을 검사한다.
9. Empty facet baseline과 unapproved facet rejection을 항상 실행한다.
10. Approved facet positive test는 `ADR-004`와 verifier contract가 있을 때만 실행한다.

**설계 근거**

Long-lived catalog definition과 solve-bound runtime을 분리해야 live catalog drift가 실행 중 의미를 바꾸지 않는다. Telemetry는 의미 입력이 아니며 fingerprint를 바꾸지 않는다.

**금지 shortcut**

- Bound object에 catalog handle, mutable registry, provider locator 저장
- Clock/random/cache/scratch를 capability instance에 저장
- `toString()`, object identity, file path를 fingerprint로 사용
- Facet test 전체 skip
- Secret/descriptor parameter/customer PII를 log/evidence에 저장

**Future verification**

```bash
# §11.6 isolated-repository reactor freshness prelude required.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/profile-catalog/pom.xml \
  -Dtest=BoundProfileLifecycleTest,BoundProfileCorruptionTest,BoundProfileIsolationTest,BoundProfileReproducibilityTest,BoundProfilePhase03ContractTest,ProfileBindingSecurityObservabilityTest,FacetBindingGateTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과**

- Lookup after bind 0
- Snapshot/runtime exact equality
- Corruption 허용 0
- Same input repeated/parallel canonical bytes exact equality
- Empty/unapproved facet required methods green
- Approved facet 미적용이면 manifest에 `NOT_APPLICABLE_NO_APPROVED_FACET` 사유

**실패 해석과 rollback**

Mutable state나 post-bind lookup이 발견되면 lifecycle evidence를 폐기한다. Optimization을 제거하고 WP-04.3의 pure full-binding path로 돌아간다.

**다음 handoff**

`E-P04-BINDING/ISOLATION/FACET` 후보 자료와 immutable artifact를 WP-04.5에 넘긴다.

### WP-04.5 — Phase 03 equivalence와 downstream contract

**목적과 이유**

Binder가 Phase 03을 복제하거나 변경하지 않았는지, 실제 capability + catalog + core binder 조합이 하나의 `BoundProfile`을 만드는지, Phase 05/07이 customer/catalog를 모른 채 소비할 수 있는지 검증한다.

**사전조건**

- WP-04.1~4 required test green
- Full-solution evaluator/equality/portfolio policy의 승인 상태가 handoff에 명시됨
- Phase 00/Architecture review가 test-only `build/profile-validation` owner와 제한 classpath compile probe를 승인함

**예상 test/contract**

- `BoundProfilePhase03ContractTest`
- `build/profile-validation`의 `ProfileCompositionContractTest`
- `build/profile-validation`의 `Phase05CoreOnlyConsumerCompileProbeTest`
- 승인 뒤 `FullSolutionEvaluationHandoffContractTest`
- 승인 뒤 `ComparatorTieBoundaryContractTest`
- 승인 뒤 `PortfolioPolicyHandoffContractTest`

**구체 행동**

1. Direct Phase 03 declaration과 bound declaration의 route artifact를 exact 비교한다.
2. Test-only composition owner가 actual `ExplicitCapabilityInventory` → catalog provider → core-owned `ProfileBindingEngine` → `BoundProfile`을 조립한다.
3. Composition 결과의 snapshot/runtime semantic fingerprint와 direct-vs-bound Phase 03 artifact가 exact 같은지 확인한다.
4. 별도 consumer fixture를 `rpdptw-core` artifact만 있는 제한 classpath로 compile한다. Composition module의 넓은 test classpath compile은 core-only 증거가 아니다.
5. Phase 05 consumer import graph에 catalog/capabilities concrete dependency가 0인지 본다.
6. Route-only artifact가 full-solution authority가 아니라는 negative test를 둔다.
7. Approved solution evaluator는 exact problem/travel/profile + ordered routes + bank를 받는지 검증한다.
8. Business equality 뒤 solution/insertion tie가 분리되는지 comparator 법칙을 검증한다.
9. Approved portfolio policy가 있을 때만 reference vector/golden trace를 검증한다.

**설계 근거**

Sibling module의 개별 test나 reactor 성공은 composition root가 실제 concrete registry와 catalog를 core runtime에 연결했다는 증거가 아니다. 반대로 production distribution/application에 concrete dependency를 넣으면 generic DAG가 깨진다. 따라서 integrated design의 `build/profile-validation`을 승인된 test-only owner로 사용하고, production reverse edge 없이 actual 조립과 downstream core-only compile을 분리해 증명한다.

Phase 04 review의 residual blocker는 문서 표기가 아니라 reciprocal compile/full-equality/corruption evidence로 닫아야 한다.

**금지 shortcut**

- Route objective 합을 candidate objective로 사용
- Phase 04 `PASS`를 Phase 07 verdict로 사용
- Context tie를 objective dimension으로 직렬화
- `CLOCK` missing을 다른 policy로 fallback
- 미승인 restart test를 “skipped green”으로 완료 처리
- Sibling build 성공을 actual composition evidence로 기록
- `application`이나 production distribution에 test용 concrete dependency 추가
- Full test classpath에서 compile된 fixture를 core-only compile probe로 주장

**Future verification**

승인된 API가 없을 때 세 restart test 이름은 생성·실행 evidence가 아니다. API 승인 뒤 exact method manifest와 함께 실행한다. `build/profile-validation` owner가 아직 승인/생성되지 않았으면 아래 두 composition test는 `NOT_PRODUCED`; 기존 sibling build로 대체하지 않는다.

```bash
# §11.6 isolated-repository reactor freshness prelude required.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f rpdptw/profile-catalog/pom.xml \
  -Dtest=BoundProfilePhase03ContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -f build/profile-validation/pom.xml \
  -Dtest=ProfileCompositionContractTest,Phase05CoreOnlyConsumerCompileProbeTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과**

- Direct-vs-bound Phase 03 artifact exact equality
- Actual registry→catalog→core binder→`BoundProfile` composition green
- Snapshot/runtime semantic fingerprint exact equality
- Restricted `rpdptw-core`-only Phase 05 consumer compile probe green
- Downstream concrete catalog/profile dependency 0
- Cross-Phase restart test는 승인된 경우에만 required manifest에 포함

**실패 해석과 rollback**

Phase 03 output이 달라지면 binder materialization을 폐기하고 accepted Phase 03 declaration으로 돌아간다. Composition owner/limited classpath가 없거나 concrete production reverse edge가 생기면 integration evidence를 `NOT_PRODUCED`로 유지한다. Cross-Phase contract가 미승인이면 Phase 04 exit를 주장하지 않는다.

**다음 handoff**

Architecture/evidence review가 소비할 compile/equality/corruption report를 WP-04.6에 넘긴다.

### WP-04.6 — Architecture, evidence, independent review와 acceptance handoff

**목적과 이유**

Module 경계, safe evidence와 단방향 acceptance chain을 검증하고 Phase 05/07이 소비할 immutable handoff를 만든다.

**사전조건**

- WP-04.1~5 required test green
- Missing/skipped required test 0
- 모든 residual/exit blocker의 exact 상태, owner, last-safe point와 resume 조건이 snapshot에 있음

**예상 변경**

- `build/architecture-rules`의 Phase 04 rule
- Pre-review evidence manifest
- Independent review input
- Branch A의 blocked review report 또는 Branch B의 accepted handoff candidate/receipt
- Branch별 rollback/resume digest
- 공용 progress/status는 total scheduler만 수정

**구체 행동**

1. Forbidden dependency/customer/script/reflection/raw-map 검사를 실행한다.
2. Selected module과 full reactor를 `clean verify`한다.
3. Fresh Surefire exact class/method manifest를 만든다.
4. Source/toolchain/config/test/oracle/architecture/security digest를 봉인한다.
5. Production catalog empty 상태와 OPEN/GATED/deferred snapshot을 기록한다.
6. Independent reviewer가 immutable manifest digest를 review한다.
7. 아래 두 branch 중 정확히 하나로 끝낸다.

**Branch A — exit blocker unresolved**

```text
immutable implementation/test/blocked-state evidence
→ preReviewEvidenceManifest [digest M-BLOCKED]
→ independentReviewReport [references M-BLOCKED]
→ verdict CHANGES_REQUIRED or BLOCKED
→ no postReviewAcceptanceReceipt
→ no ACCEPTED status, no accepted Phase05/07 handoff
```

- Artifact: blocker snapshot, last-safe implementation/test evidence, review report, rollback point.
- Status: `CHANGES_REQUIRED` 또는 `BLOCKED`; 최대 `REVIEW_PENDING`으로도 acceptance를 뜻하지 않는다.
- Rollback: incomplete bound/handoff candidate를 배포·publish하지 않고 마지막 accepted Phase 03/build를 보존한다.
- Resume: blocker owner approval과 required reciprocal test가 생기면 기존 manifest를 수정하지 않는다. 새 source snapshot과 새 immutable evidence manifest로 WP-04.0/영향 WP부터 재개한다.

**Branch B — all exit blockers resolved**

```text
immutable complete implementation/test evidence
→ preReviewEvidenceManifest [digest M]
→ independentReviewReport PASS [references M, digest R]
→ postReviewAcceptanceReceipt [references M + R]
→ ACCEPTED handoff
```

- Artifact: complete `E-P04-*`, approved cross-Phase receipts/tests, review `PASS`, receipt와 handoff/rollback digest.
- Status: receipt 검증 전 `REVIEW_PENDING`, 검증 뒤에만 scheduler가 `ACCEPTED`.
- Rollback: receipt가 참조한 immutable rollback point와 이전 accepted build/profile identity로 복귀한다.
- Resume: 새 회귀는 same-identity overwrite가 아니라 새 evidence chain/review/receipt로 처리한다.

**Future verification**

```bash
# §11.6 isolated-repository reactor freshness prelude and exact P04_M2 required.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -pl build/architecture-rules -am clean verify

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -pl build/profile-validation -am clean verify

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" clean verify
```

**기대 결과**

- OR-Tools-free ALNS-only root build green
- Forbidden edge/customer branch/dynamic execution 0
- Required failed/error/skipped/missing 0
- Branch A이면 immutable manifest + `CHANGES_REQUIRED/BLOCKED` review만 있고 receipt/handoff 없음
- Branch B이면 digest-protected complete manifest + review `PASS` + valid receipt/accepted handoff

**실패 해석과 rollback**

Unresolved exit blocker, review `PASS` 부재 또는 receipt 부재를 “explicitly blocking but accepted”로 해석하지 않는다. Branch A로 종료하고 `ACCEPTED`/accepted handoff를 주장하지 않는다. 마지막 accepted Phase 03 artifact와 이전 accepted build를 보존한다.

**다음 handoff**

Branch B에서만 §15의 Phase 05/07 consumer manifest와 rollback point를 총괄 scheduler에 제출한다. Branch A에서는 blocker/review report와 resume 조건만 제출한다.

## 11. 테스트 구현 안내

### 11.1 Fixture, builder와 oracle

Profile-specific test helper는 다음처럼 production code와 분리한다.

```text
rpdptw/profile-catalog/src/test/java/.../testing/
  TestOnlyProfileDescriptorBuilder
  TestOnlyCapabilityProviderBuilder
  ProfileCombinationFixtureBuilder
  ExhaustiveProfileBindingOracle
  CorruptBoundProfileFixtureBuilder
```

규칙:

1. Builder default도 모두 explicit `TEST_ONLY` 값이어야 한다.
2. Oracle은 production resolver/binder/graph validator를 호출하지 않는다.
3. Expected result는 literal table, independent topological walk와 stable precedence로 계산한다.
4. Same-coordinate drift는 한 field만 바꾼 minimal fixture로 만든다.
5. Serialization이 미승인이라면 byte corruption을 발명하지 않고 field projection boundary에서 corruption을 주입한다.
6. Property test는 stable ordinal/seed와 최초 최소 counterexample를 evidence에 남긴다.

### 11.2 Bounded combination fixture

다음 축을 작은 유한 집합으로 만든다.

```text
trip:
  ONEWAY | SINGLE_ROUNDTRIP | ROTATION_REQUIRED

ownership:
  DIRECT_ONLY | DIRECT_AND_LEASE

mandatory:
  NONE | PRESENT

static compatibility:
  ALL_HAVE_ELIGIBLE
  | SOME_ZERO_ELIGIBLE_BUT_VALID
  | PROFILE_REINTERPRETS_SIZE_OR_QUALIFICATION

service:
  EXPLICIT_START_ONLY
  | EXPLICIT_COMPLETE_WITHIN_WINDOW
  | OMITTED_WITHOUT_SCHEMA_MATERIALIZATION

profile policy:
  VALID_BASELINE
  | DISABLE_CORE_RESOURCE
  | FINAL_DEPOT_FACT_REQUIRED
  | MANDATORY_DIMENSION_MISSING
  | MANDATORY_DIMENSION_NOT_FIRST
  | LEASE_DIMENSION_MISSING
  | MISSING_FACT_OR_METRIC
  | UNAPPROVED_FACET
```

중요 판정:

- `SOME_ZERO_ELIGIBLE_BUT_VALID`만으로 binding을 reject하지 않는다.
- `ONEWAY + final-depot dependency`는 reject한다.
- Rotation-required는 deferred이므로 reject한다.
- Explicit `START_ONLY`/`COMPLETE_WITHIN_WINDOW`는 accepted Phase 03 enum으로 materialize한다.
- Schema가 explicit default로 materialize하지 않은 omitted service rule은 binder hidden default로 채우지 않는다.
- Profile의 core resource disable 시도는 reject한다.

### 11.3 Exact test class와 method 후보

이 표는 future manifest 후보이며 현재 파일이나 evidence가 아니다.

| Class | Required method 후보 | 핵심 oracle/pass |
|---|---|---|
| `ProfileSelectionContractTest` | `coreOwnsProviderNeutralSelectionAndBindingContracts()` | Core contract가 catalog/capability를 import하지 않음 |
| `ProfileFailurePrecedenceContractTest` | `keepsPreReadScopeAuthorizationAndPostVerifiedSelectionAuthorizationAsDistinctStages()` | 두 authorization ordinal 분리 |
| `ProfileIdentityResolutionTest` | `resolvesOmittedPresetOnlyFromExactProfileVersionDefault()` | Exact declared default + provenance |
|  | `rejectsLatestVersionRangeSimilarityAndUnknownPreset()` | Fallback 0 |
|  | `rejectsCrossCustomerPresetEvenWhenKeysMatch()` | Cross-customer 선택 0 |
|  | `rejectsSameLookupCoordinateWithDifferentSchemaContentOrDefault()` | Same-coordinate ambiguity 0 |
|  | `rejectsUnsupportedSchemaWithoutCrossVersionFallback()` | Neighbor lookup 0 |
|  | `canonicalFieldOrderDoesNotChangeDescriptorFingerprint()` | Semantic equality |
| `ProfileAuthorizationSecurityTest` | `rejectsUnauthorizedCustomerBeforeDescriptorReadAndDoesNotRevealExistenceOrIntegrity()` | Read count 0, same outward shape |
|  | `requiresExactDescriptorAndPresetAuthorizationAfterVerification()` | Exact second-stage authorization |
|  | `preReadDenialOutranksAbsentCorruptAndMissingPresetVariants()` | Pre-read denial 우선, catalog read 0 |
| `CapabilityRegistryContractTest` | `resolvesOnlyExactCapabilityKeyVersion()` | Range/latest 0 |
|  | `rejectsDuplicateIdentityBeforeAnyProfileBinding()` | First/last wins 0 |
|  | `rejectsSameContractIdentityWithDifferentImplementationFingerprint()` | Startup failure |
|  | `registryInsertionOrderDoesNotChangeSnapshotFingerprint()` | Permutation equality |
| `CapabilityImplementationContractTest` | `bindsOnlyDeclaredTypedParameterRecord()` | Raw map/coercion 0 |
|  | `rejectsParameterRangeUnitAndValueTypeMismatch()` | Exact typed failure |
|  | `doesNotRetainEvaluationScratchClockRandomOrMutableRegistry()` | Mutable/runtime state 0 |
| `ProfileBinderContractTest` | `rejectsMissingDuplicateCyclicAndAmbiguousDependencies()` | Independent graph oracle |
|  | `materializesStableDependencyClosureIndependentOfUnorderedDeclarationOrder()` | Unordered closure/fingerprint equality |
|  | `mapFieldAndUnorderedCapabilityPermutationsKeepFingerprint()` | Non-semantic order equality |
|  | `objectiveDimensionAndStageSwapsChangeFingerprintPlanAndComparator()` | Semantic sequence 보존 |
|  | `rejectsMetricFactUnitOutputAndDirectionMismatchBeforeKernelCall()` | Kernel call 0 |
|  | `rejectsMissingObjectiveOperatorBudgetAndGuardReferencesInSolvePlan()` | Hidden/default refs 0 |
|  | `rejectsEvaluationCapabilityOperatorAndBudgetKeySpaceCollision()` | Cross-namespace coercion 0 |
|  | `rejectsProblemTravelAndPhase03ContractFingerprintMismatch()` | Pre-materialization mismatch |
| `ProfileCombinationOracleTest` | `matchesBoundedCompatibilityResourceServiceTripAndPolicyMatrix()` | All ordinals exact |
|  | `allowsZeroEligibleVehicleFactWithoutTreatingItAsBindingDefect()` | Bind success |
|  | `rejectsProfileAttemptToDisableCanonicalCompatibilityOrResourceRule()` | `CORE_RULE_OVERRIDE` |
|  | `rejectsOnewayFinalDepotAndRotationRequiredCapabilities()` | No synthesized return/rotation |
| `ProfileObjectivePolicyTest` | `requiresMandatoryUnassignedAsFirstLexicographicDimensionWhenUsed()` | No hard/Big-M |
|  | `requiresOutsourcedVehicleVolumeDimensionWhenLeaseFleetIsPresent()` | LEASE contract |
|  | `rejectsDuplicateObjectiveAndMetricKeys()` | First/last wins 0 |
| `BoundProfileLifecycleTest` | `snapshotAndExecutableRuntimeHaveSameSemanticFingerprint()` | Exact equality |
|  | `boundProfileCannotBeReusedWithAnotherProblemOrTravel()` | Pre-evaluation reject |
|  | `doesNotReadCatalogRegistryOrAuthorizationAfterBinding()` | Post-bind call 0 |
| `BoundProfileCorruptionTest` | `rejectsDescriptorRegistryImplementationAndAuthorizationDrift()` | One-field drift reject |
|  | `rejectsDependencyClosureAndPhase03PlanFingerprintCorruption()` | Integrity failure |
| `BoundProfileIsolationTest` | `sameProblemCanBindTwoAuthorizedProfilesWithoutSharedState()` | No alias |
|  | `addingUnrelatedProfileDoesNotChangeExistingBoundFingerprint()` | Existing bytes unchanged |
|  | `customerIdentityIsNotVisibleToCapabilityEvaluation()` | Customer-neutral input |
| `BoundProfileReproducibilityTest` | `sameInputsProduceSameClosureDeclarationsAndFingerprintAcrossRepeatedParallelBinding()` | Exact canonical equality |
|  | `catalogBackendAndProviderRegistrationOrderDoNotAffectBinding()` | Backend/order equality |
| `BoundProfilePhase03ContractTest` | `materializesOnlyExistingPhase03PropagationAndEvaluationContracts()` | Signature diff 0 |
|  | `boundPlanProducesSameKernelArtifactAsExplicitPhase03Declaration()` | Direct/bound artifact equality |
|  | `doesNotClaimRouteArtifactAggregationAsFullSolutionAuthority()` | Ad hoc aggregator 0 |
|  | `phase05ConsumerNeedsNoCustomerCatalogRegistryOrDescriptorParser()` | Core-only compile dependency |
| `Phase04OracleSensitivityTest` | `detectsLatestFallbackFirstWinsHiddenDefaultAndWrongPrecedenceFaults()` | 모든 seeded fault red |
|  | `detectsAuthorizationAfterLookupAndCrossNamespaceResolutionFaults()` | Read/order/coercion fault red |
| `ProfileBindingSecurityObservabilityTest` | `emitsOnlySafeStableFieldsForSuccessAndFailure()` | Allowlist only |
|  | `redactsDescriptorParametersCustomerPiiSecretsLocatorsClasspathAndStackInputs()` | Sentinel 0 |
| `FacetBindingGateTest` | `rejectsFacetWithoutApprovedContractAndVerifierRecomputationVersion()` | Unapproved reject |
|  | `emptyFacetSetPreservesBaseProfilePlanAndFingerprintRules()` | Required baseline green |
| `Phase04ProfileArchitectureTest` | `coreDoesNotDependOnCapabilitiesProfilesCustomerProviderOrSolver()` | Forbidden edge 0 |
|  | `capabilitiesAndProfilesDoNotDependOnSearchApplicationCloudOrVendor()` | Forbidden edge 0 |
|  | `profileTestFixturesDoNotReverseThePhase00ProductionDependencyDag()` | Test DAG 유지 |
|  | `hasNoCustomerNameBranchDynamicScriptReflectionScanOrRawParameterMap()` | Forbidden pattern 0 |
| `ProfileCompositionContractTest` | `bindsActualRegistryCatalogAndCoreRuntimeWithEqualSemanticFingerprint()` | Actual cross-module composition과 fingerprint equality |
| `Phase05CoreOnlyConsumerCompileProbeTest` | `compilesConsumerAgainstRestrictedCoreOnlyClasspath()` | Catalog/capabilities 없는 downstream compile |

Restart-only tests:

- `FullSolutionEvaluationHandoffContractTest`
- `ComparatorTieBoundaryContractTest`
- `PortfolioPolicyHandoffContractTest`

승인된 API/policy가 없을 때 이들은 “현재 missing test”가 아니라 blocker 해제 후 required가 되는 restart condition이다.

### 11.4 Red → green 순서

| 순서 | 먼저 실패시킬 것 | Red가 증명해야 할 결함 | Green 조건 |
|---:|---|---|---|
| 1 | Identity/catalog/auth | Latest/cross-customer/read-before-auth 통과 | Exact identity, pre-read denial |
| 2 | Registry/typed params | First-wins/raw map/order drift | Exact inventory, typed config |
| 3 | Dependency/unit/objective | Missing/cycle/unit/direction이 kernel까지 감 | Pre-kernel typed reject |
| 4 | Combination oracle | Core override/LEASE/mandatory/trip mismatch | All ordinal exact equality |
| 5 | Phase 03 equivalence | Binder가 kernel을 복제·변경 | Existing declaration artifact equality |
| 6 | Sensitivity/security | Faulty double을 oracle이 놓침, sentinel 노출 | All seeded faults red, sentinel 0 |
| 7 | Lifecycle/corruption/facet | Shared state, post-bind lookup, stale identity | No alias, no lookup, drift reject |
| 8 | Repro/architecture/consumer | Order/thread/backend/import 차이 | Canonical equality, forbidden edge 0 |

Compile failure만 red evidence로 충분하지 않다. Minimal faulty implementation이 assertion을 실제로 실패시키고, 수정 후 같은 test가 green이어야 oracle sensitivity evidence가 된다.

### 11.5 Test category별 적용성

| 종류 | Phase 04 적용 | Pass 판정 또는 미적용 이유 |
|---|---|---|
| Unit/boundary | 필수 | Identity, parameter range/unit, objective order boundary |
| Contract | 필수 | Catalog, registry, Phase 03 materialization, downstream consumer |
| Module integration | 필수 | Approved `build/profile-validation` owner가 actual core + capability + profile-catalog를 test scope로 조립; 없으면 `NOT_PRODUCED` |
| Full application E2E | Phase 04 exit에는 직접 미적용 | Phase 08 소유; Phase 04는 bound handoff 제공 |
| Architecture | 필수 | Forbidden dependency/customer/reflection/raw-map 0 |
| Fault injection | 필수 | Faulty resolver/registry/component/oracle sensitivity |
| Corruption | 필수 | Descriptor/registry/auth/closure/plan drift rejection |
| Reproducibility | 필수 | Repeat/parallel/backend/order canonical equality |
| Security | 필수 범위 | Pre-read auth, non-enumeration, allowlist/redaction |
| Tenant storage integration | 직접 미적용 | Phase 09 이후 provider evidence; generic contract만 검증 |
| Performance | 구조 evidence만 | Stable closure/pure bind의 bounded work 기록; 공식 threshold는 OPEN |
| E2E provider/deployment | 미적용 | Phase 11/14 소유 |
| Approved facet positive | 조건부 | `ADR-004`가 없으면 `NOT_APPLICABLE_NO_APPROVED_FACET`; empty/reject test는 필수 |
| Phase 13/MIP | 미적용/GATED | 14A receipt와 `C-17` 승인 전 로드·구현 금지 |

### 11.6 False-green 방지와 Maven pass 판정

Leaf `-f rpdptw/.../pom.xml` command는 sibling `rpdptw-core`의 현재 source를 reactor에서 만들지 않는다. 기존 `~/.m2` snapshot이나 stale `target/`이 있으면 clean checkout에서 실패할 build가 green처럼 보일 수 있다. 따라서 leaf selected test만 단독 실행하지 않는다.

Phase 00가 wrapper/reactor/plugin policy를 accepted한 뒤, 각 evidence attempt는 다음 freshness procedure를 하나의 method manifest로 실행한다.

```text
source snapshot S
→ 새 isolated Maven local repository R
→ S에서 parent + selected module graph를 -am clean install
→ 같은 R/S에서 leaf exact selected tests를 clean test
→ approved test-only composition module verify
→ root clean verify
→ fresh report/method manifest와 R inventory digest seal
```

시작 예:

```bash
P04_SOURCE_COMMIT="$(git rev-parse HEAD)" || exit 51
P04_M2="$(mktemp -d /tmp/ro-next-p04-m2.XXXXXX)" || exit 52

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${P04_M2}" \
  -pl rpdptw/core,rpdptw/capabilities,rpdptw/profile-catalog \
  -am clean install
```

이 reactor freshness command는 selected leaf method의 대체가 아니다. 그 뒤 각 WP의 `-f ... -Dtest=... -Dsurefire.failIfNoSpecifiedTests=true clean test`를 **같은 `P04_M2`와 source commit**으로 실행한다. 반대로 leaf command만 실행한 결과도 evidence가 아니다.

`-am` reactor command에 leaf-only `-Dtest`를 전역 전달해 upstream의 “specified test 없음”을 false failure/skip으로 만들지 않는다. Reactor freshness는 graph 전체의 정상 test를 실행하고, exact leaf method selection은 isolated repository에 current upstream artifact가 설치된 뒤 별도 실행한다. `-DskipTests`, `-Dmaven.test.skip=true`, `-Dsurefire.failIfNoSpecifiedTests=false`로 이 문제를 숨기지 않는다.

Evidence에는 최소 다음을 기록한다.

- `P04_SOURCE_COMMIT`, §4 expected-source manifest digest와 live working-byte digest
- `P04_M2`의 absolute path와 relative-path/size/file-hash로 만든 sealed inventory digest
- Selected reactor graph (`help/effective-pom` 또는 동등한 approved graph record)
- Wrapper, Java, Maven, OS/architecture와 plugin version
- 모든 command/exit code/start-end time
- Command별 fresh Surefire XML digest와 exact class/method manifest
- Clean 전 report directory 존재 여부와 clean 뒤 생성 time/source attempt identity

Isolated repository path가 재사용됐거나 source/expected manifest가 중간에 바뀌면 전체 attempt를 폐기한다. Phase 00 acceptance 전에는 이 procedure와 아래 command가 모두 future template이며 현재 live reactor green을 Phase 04 evidence로 기록하지 않는다.

Pass는 exit code 0만 뜻하지 않는다.

1. `clean` 뒤 생성된 fresh Surefire XML이어야 한다.
2. §11.3 required class/method가 실제 report에 모두 있어야 한다.
3. Required failed/error/skipped가 모두 0이어야 한다.
4. Duplicate report와 stale previous-run report가 없어야 한다.
5. Property ordinal/seed, fixture/oracle digest와 toolchain이 기록돼야 한다.
6. `NOT_APPLICABLE`은 승인된 조건부 test에만 쓰고 이유를 manifest에 기록한다.
7. Expected required test가 0개 실행됐거나 selected method가 없으면 exit code와 무관하게 실패다.
8. Approved `build/profile-validation` composition과 restricted core-only compile probe가 없으면 integration evidence는 `NOT_PRODUCED`다.
9. Root/graph/leaf 결과가 다른 source commit 또는 Maven local repository에서 섞이면 실패다.

Future report 확인 예:

```bash
find rpdptw build -path '*/target/surefire-reports/TEST-*.xml' \
  -type f -print

rg -n 'failures="[1-9]|errors="[1-9]|skipped="[1-9]' \
  rpdptw/*/target/surefire-reports \
  build/*/target/surefire-reports

rg -n 'name="rejectsUnauthorizedCustomerBeforeDescriptorReadAndDoesNotRevealExistenceOrIntegrity"|name="snapshotAndExecutableRuntimeHaveSameSemanticFingerprint"' \
  rpdptw/*/target/surefire-reports \
  build/*/target/surefire-reports
```

두 번째 명령에 match가 있으면 required suite는 실패다. 세 번째는 두 method 존재 확인 예일 뿐 전체 exact manifest를 대체하지 않는다.

## 12. 사람 review checkpoint, evidence와 stop/resume

### 12.1 Checkpoint

| Checkpoint | 사람이 확인할 질문 | Stop 조건 | Resume 조건 |
|---|---|---|---|
| C0 Entry | Predecessor receipt/digest와 owner/task가 실제 있는가? | 하나라도 없음/mismatch | Correct accepted artifact와 assignment |
| C1 Identity/Auth | §9.7의 no-lookup syntax → pre-read scope auth → tenant lookup/integrity/preset → post-verified auth 순서인가? | Fallback, overwrite, enumeration, pre-read catalog read | ADR/schema/security correction |
| C2 Registry | Explicit exact inventory와 typed contract인가? | First-wins, raw map, namespace coercion | Registry/API review |
| C3 Binding | Closure와 precedence가 oracle과 같은가? | Kernel-after-failure, hidden default | Binder/oracle correction |
| C4 Objective | Mandatory/LEASE와 core rule이 보존되는가? | Hard/Big-M/disable/synthesized trip | Product/Domain review |
| C5 Lifecycle | Snapshot/runtime equality와 no post-bind lookup인가? | Mutable alias/drift/shared state | Pure path로 rollback |
| C6 Cross-Phase | Solution/equality/portfolio contract가 승인됐는가? | Residual blocker 남음 | Joint review와 reciprocal evidence |
| C7 Integration | Approved test-only composition, restricted core-only compile, architecture/fresh report/security가 완전한가? | Owner 미승인, missing/skipped/stale/unsafe field | Owner 승인, clean isolated rerun과 correction |
| C8 Acceptance | Exit blocker 0, manifest M, review PASS R, receipt(M,R)가 있는가? | Blocker/review fail/receipt 없음 | Branch A blocked report 또는 새 Branch B evidence chain |

### 12.2 Evidence bundle 최소 내용

| Key | 반드시 포함할 것 |
|---|---|
| `E-P04-BINDING` | Source/build/Phase03/problem/travel/descriptor/auth/registry fingerprints, exact lookup/default, typed closure, combination oracle와 seeded-fault sensitivity, exact command/toolchain/fresh method manifest |
| `E-P04-ISOLATION` | Same problem multi-profile 결과, pre-read denial, safe error/redaction, unrelated-profile regression, post-bind lookup 0, drift/corruption와 repeated/parallel equality |
| `E-P04-FACET` | `ADR-004` status, empty baseline, unapproved rejection, 승인 facet이 있으면 Phase 03 propagation과 Phase 07 independent recomputation parity |

공통:

```text
source commit + all expected HEAD blobs + live git-object/SHA-256 fingerprints
source status/diff-name snapshot and approved rebaseline record if any
input/config/profile/build/runtime digests
exact commands + environment + exit codes
fixture/oracle/Surefire report digests
isolated Maven repository path/inventory digest + selected reactor graph
test-only composition and restricted core-only compile-probe report digests
architecture/security report digests
OPEN/GATED/deferred snapshot
handoff candidate artifact digest
rollback point digest
```

### 12.3 단방향 evidence DAG

```text
immutable implementation/test evidence
  → preReviewEvidenceManifest [digest M]
  → independentReviewReport [references M, digest R]
  → postReviewAcceptanceReceipt [references M + R]
  → ACCEPTED handoff
```

Pre-review manifest에 reviewer identity, verdict, review digest나 acceptance receipt를 넣지 않는다. Review 뒤 manifest를 backfill하지 않는다. Review `PASS`만 있고 receipt가 없으면 `ACCEPTED`가 아니다.

Exit blocker가 하나라도 남은 manifest는 immutable하게 seal할 수 있지만, 그 결과는 Branch A의 `CHANGES_REQUIRED/BLOCKED` review까지만 진행한다. Receipt와 accepted handoff를 만들지 않는다. 모든 blocker가 해결된 Branch B만 review `PASS → receipt(M,R) → ACCEPTED`로 진행한다.

### 12.4 Safe observability

허용 후보:

```text
stable binding outcome/code/stage
opaque profile lookup coordinate or safe subject
descriptor/registry/problem/travel/contract fingerprints
safe dependency key/location
expected/actual non-sensitive unit/type/version
test oracle ordinal
```

금지:

- Raw descriptor bytes와 parameter value
- Full customer identity/PII
- Secret, provider locator, classpath
- Arbitrary exception/stack input
- Log/metric/trace emission order를 semantic fingerprint에 사용

Denied outward result는 profile 존재, schema 지원 여부와 integrity defect를 구별하지 않는 safe `ACCESS_DENIED` contract를 따른다.

## 13. 흔한 오해와 anti-pattern

| 오해/shortcut | 왜 틀렸는가 | 올바른 경계 |
|---|---|---|
| “Customer마다 module 하나가 깔끔하다” | Onboarding마다 reactor/release가 바뀐다 | Descriptor + reusable capability |
| “Vehicle capability와 evaluation capability는 같다” | Static qualification과 executable policy가 섞인다 | 별도 typed namespace |
| “`latest`가 운영에 편하다” | 재현과 exact authorization이 깨진다 | Exact version only |
| “Default가 없으면 economy로 하자” | Production 의미를 binder가 발명한다 | Typed missing-default rejection |
| “같은 key면 첫 provider를 쓰자” | Classpath/order에 따라 의미가 바뀐다 | Duplicate startup failure |
| “Record면 immutable이다” | Nested collection alias가 남는다 | Defensive copy + mutation probe |
| “Map parameter가 확장하기 쉽다” | Type/unit/range/verifier closure가 사라진다 | Approved typed record/schema |
| “Profile에서 class name을 지정하자” | Arbitrary code 실행과 공급망 위험 | Explicit approved registry |
| “Unauthorized라도 not-found 확인 뒤 거부하자” | Catalog enumeration이 가능하다 | Pre-read scope authorization |
| “Descriptor list는 모두 key-sort하면 된다” | Objective/stage/tie semantic sequence가 지워진다 | Schema field별 unordered/ordered 분류 |
| “Profile이 compatibility를 다시 계산하자” | Phase 02 authority와 drift한다 | Accepted fact를 소비 |
| “Hard 위반도 큰 penalty면 된다” | Infeasible가 objective로 통과한다 | Typed rejection |
| “Mandatory를 hard로 만들자” | 0 불가능한 best partial의 의미가 사라진다 | Top lexicographic dimension |
| “Route objective를 합치면 solution objective다” | Bank/used vehicle/global dimension owner가 없다 | Approved full-solution evaluator |
| “Tie key를 마지막 objective로 넣자” | Context가 business result를 뒤집을 수 있다 | Equality 뒤 별도 tie |
| “CLOCK은 일반 좌표 convention이면 된다” | Axis/orientation authority가 미승인이다 | Reference vector 승인 대기 |
| “Facet field를 nullable map으로 미리 두자” | Core 오염과 verifier gap이 생긴다 | Empty set, ADR-004 뒤 typed seam |
| “Bind 뒤 catalog를 다시 보면 최신이다” | Solve 중 의미가 drift한다 | Bound snapshot only |
| “Telemetry field도 fingerprint에 넣자” | 실행 환경이 business identity를 바꾼다 | Semantic과 operational metadata 분리 |
| “현재 reactor/module skeleton이 있거나 test가 green이면 시작 가능하다” | Skeleton에는 Phase 04 type/test가 0개이고 legacy synthetic test만 있을 수 있다 | Phase 00~03 receipt/evidence |
| “Leaf POM clean test면 fresh하다” | Sibling core는 stale local snapshot에서 올 수 있다 | Isolated repo + current `-am` graph + leaf exact test |
| “Sibling module verify면 composition이다” | Actual registry/catalog/binder 조립과 restricted consumer compile을 실행하지 않는다 | Approved test-only composition owner |
| “Blocker를 적어 두었으니 receipt를 내도 된다” | Exit blocker와 accepted handoff가 모순된다 | Blocked review branch와 accepted branch 분리 |
| “Phase 13 backend를 미리 넣자” | ALNS-first와 C-17 gate를 우회한다 | OR-Tools-free Phase 04 |

추가 금지:

- Customer별 solver/verifier/worker 복제
- Same coordinate 다른 content overwrite
- Cross-customer preset fallback
- Reflection scan/`ServiceLoader`/dynamic script/DSL
- Profile이 raw route, search cache, final result를 읽는 것
- Oneway return arc와 rotation 합성
- Unapproved facet을 무시하고 계속하는 것
- `BoundProfile`에 seed, numeric budget, insertion option, bank, operator state를 저장
- Concrete profile/catalog를 Phase 05/07이 compile-depend

## 14. 실제 구현 exit checklist와 Definition of Done

### 14.1 Entry와 authority

- [ ] Phase 00~03 accepted receipt와 exact handoff digest가 있다.
- [ ] Scheduler task, implementation owner와 independent reviewer가 지정됐다.
- [ ] `ADR-003`이 schema/encoding/registry/duplicate/authorization을 승인했다.
- [ ] Full-solution evaluator, business equality/tie, portfolio policy blocker가 exit에 필요한 범위에서 승인됐다.
- [ ] 모든 expected HEAD blob과 live working-byte/status/diff snapshot을 implementation start와 pre-review seal에서 fail-closed 재검사했다.
- [ ] Source blob/heading drift가 있었다면 requirement→WP/test/evidence 영향과 approved rebaseline이 있다.

### 14.2 Identity, catalog와 security

- [ ] Exact customer/profile/version/preset만 resolve한다.
- [ ] Omitted preset은 exact declared default만 사용한다.
- [ ] Same lookup coordinate의 다른 schema/content/default를 거부한다.
- [ ] Unsupported schema에서 이웃 version으로 fallback하지 않는다.
- [ ] Unauthorized request는 catalog read 전에 끝난다.
- [ ] Request syntax/exact-reference rejection도 catalog lookup 없이 끝난다.
- [ ] Pre-read denial이 not-found/corruption/preset defect보다 우선하고 catalog read count가 0이다.
- [ ] Verified descriptor+preset authorization은 별도 post-verification ordinal이다.
- [ ] Denial이 profile 존재/integrity를 노출하지 않는다.
- [ ] Raw descriptor, parameter, PII, secret, locator가 log/evidence에 없다.

### 14.3 Registry와 binding

- [ ] Explicit registry가 exact capability/implementation을 제공한다.
- [ ] Duplicate/drift/registration order defect를 fail-closed한다.
- [ ] Typed parameter/dependency/unit/range/direction closure를 pre-solve 검증한다.
- [ ] Core physical rule override를 거부한다.
- [ ] Mandatory/LEASE objective 의미를 보존한다.
- [ ] Stable dependency order가 descriptor/registry order와 무관하다.
- [ ] Map/unordered declaration permutation은 같은 fingerprint를 만든다.
- [ ] Objective dimension/stage/approved tie sequence swap은 다른 fingerprint/plan/comparator behavior를 만든다.
- [ ] Invalid binding 뒤 component/kernel/search 호출이 0이다.

### 14.4 Lifecycle, reproducibility와 architecture

- [ ] Snapshot/runtime semantic fingerprint가 exact 같다.
- [ ] 다른 problem/travel에 bound artifact를 재사용할 수 없다.
- [ ] Bind 뒤 catalog/registry/authorization lookup이 0이다.
- [ ] Same problem의 두 profile 사이 mutable alias가 없다.
- [ ] Unrelated profile 추가가 기존 bytes/fingerprint를 바꾸지 않는다.
- [ ] One-field drift/corruption을 모두 거부한다.
- [ ] Repeat/parallel/backend/registration order에서 canonical equality가 성립한다.
- [ ] Core/customer/provider/search/cloud/vendor 역의존과 customer-name branch가 0이다.
- [ ] Core-owned selection/result/failure/binding contract가 profile-catalog보다 먼저 존재하고 core→catalog edge가 0이다.
- [ ] Profile test helper가 production DAG를 역전하지 않는다.

### 14.5 Facet, downstream과 test

- [ ] Empty facet baseline과 unapproved facet rejection이 green이다.
- [ ] Approved facet이 있으면 Phase 03/07 independent recomputation parity가 있다.
- [ ] Direct-vs-bound Phase 03 artifact가 exact 같다.
- [ ] Approved test-only owner가 actual registry→catalog→core binder→`BoundProfile`을 조립한다.
- [ ] Restricted `rpdptw-core`-only classpath에서 Phase 05 consumer가 compile된다.
- [ ] Phase 05 consumer가 catalog/descriptor parser를 import하지 않는다.
- [ ] Route-only artifact를 full-solution authority로 사용하지 않는다.
- [ ] Required exact method가 fresh report에 모두 존재한다.
- [ ] Required failed/error/skipped/missing이 0이다.
- [ ] Fresh isolated Maven local repository, selected `-am` graph, leaf exact test와 root verify가 같은 source snapshot이다.
- [ ] Local repository path/inventory digest와 fresh report manifest가 evidence에 있다.
- [ ] Independent oracle가 모든 seeded faulty double을 실제 red로 잡는다.
- [ ] Root OR-Tools-free ALNS-only `clean verify`가 green이다.

### 14.6 Evidence와 acceptance

- [ ] `E-P04-BINDING`, `E-P04-ISOLATION`, `E-P04-FACET`이 immutable digest를 가진다.
- [ ] Pre-review manifest가 reviewer/receipt를 포함하지 않는다.
- [ ] Independent review가 manifest digest를 참조한다.
- [ ] Exit blocker가 남으면 review verdict가 `CHANGES_REQUIRED/BLOCKED`이고 receipt/accepted handoff가 없다.
- [ ] Exit blocker가 모두 해소된 경우에만 review `PASS` 뒤 acceptance receipt가 manifest와 review digest를 모두 참조한다.
- [ ] Branch B에서만 Phase 05/07 accepted handoff와 rollback point가 명시됐다.
- [ ] Production customer 값과 OPEN/GATED/deferred 의미를 발명하지 않았다.

### 14.7 Definition of Done

Phase 04 `ACCEPTED`는 다음을 모두 뜻한다.

1. Long-lived descriptor와 solve-bound runtime이 exact identity/fingerprint로 분리된다.
2. Customer policy는 data이고 executable behavior는 reusable typed capability다.
3. Core physical rule은 profile/capability가 임의로 약화할 수 없다.
4. 모든 identity/dependency/problem-policy defect가 kernel/search 전에 typed failure다.
5. Binder는 Phase 03을 복제하지 않고 accepted declaration/plan만 materialize한다.
6. Bound artifact는 immutable, problem-bound, customer-neutral consumer contract다.
7. Isolation, corruption, security와 reproducibility가 실제 test/evidence로 증명된다.
8. Solution evaluation/tie/portfolio handoff가 route-only authority를 넘지 않는 approved contract로 닫힌다.
9. Evidence, review, receipt와 handoff가 immutable digest로 연결된다.

다음은 DoD가 아니다.

- Descriptor/source/test file이 존재함
- Current root placeholder test 성공
- Future command를 문서에 적음
- Phase 04 문서 review가 완료됨
- Generic binder만 green이고 cross-Phase blocker가 남음
- Independent review만 있고 acceptance receipt가 없음

Generic Phase 04 acceptance는 production customer profile의 존재나 활성화를 뜻하지 않는다. Production onboarding은 별도 exact descriptor/authorization/Product·Security approval와 regression evidence가 필요하다.

## 15. 다음 Phase와 verifier 인계

### 15.1 Phase 03에서 받아야 할 producer artifact

```text
Phase03EvaluationContractHandoff
  Phase03ApiSignatureFingerprint
  Problem/PreparedTravel authority contract versions
  PropagationDeclaration schema/fingerprint
  EvaluationPlan schema/fingerprint
  fact/metric/unit/key declarations
  result/failure taxonomy
  E-P03-* references
  independent review + acceptance receipt references
```

Broken producer 증상:

- Phase 03 signature와 binder target fingerprint mismatch
- Missing unit/fact dependency
- Route result가 mutable하거나 rejection identity가 call-local authority에 결합되지 않음
- Facet status가 accepted/empty/deferred 중 무엇인지 불명확

### 15.2 Phase 05에 넘길 artifact

```text
Phase04BoundProfileHandoff
  source commit + Phase 04 detailed/review/evidence digests
  exact ProfileLookupCoordinate + resolved preset identity
  descriptor/authorization/registry/build fingerprints
  ProblemInstance + PreparedTravel fingerprints
  Phase03 API/declaration/plan fingerprints
  BoundDependencyClosure fingerprint
  BoundProfileSnapshot artifact ref/digest
  executable BoundProfile contract/build compatibility
  SolvePlan typed-reference contract/status
  full-solution evaluator/equality/tie/portfolio-policy approval refs
  test-only actual composition report digest
  restricted rpdptw-core-only consumer compile-probe digest
  E-P04-BINDING / E-P04-ISOLATION / E-P04-FACET refs
  independent review PASS + post-review acceptance receipt refs
  rollback point
```

이 handoff envelope은 WP-04.6 Branch B에서만 발행한다. Branch A는 같은 이름의 “blocked handoff”를 만들지 않고 blocker snapshot, independent review와 resume record만 전달한다.

Phase 05 consumer 확인:

```text
Phase05.problem == BoundProfile.problem
Phase05.travel == BoundProfile.travel
Phase05 declarations == BoundProfile Phase03 declarations
concrete catalog/profile module compile dependencies == 0
full-solution input == exact problem/travel/profile + ordered routes + bank
context tie == only after business equality
```

Broken handoff 증상:

- Phase 05가 catalog/profile/preset을 다시 resolve한다.
- Capability parameter default/coercion을 insertion code가 수행한다.
- Route artifact 합이나 delta를 full-solution objective로 사용한다.
- Customer ID나 preset label로 portfolio 동작을 분기한다.
- `CLOCK`/utilization의 hidden default를 Phase 04가 넣는다.
- Different problem/travel에 같은 `BoundProfile`을 사용한다.

### 15.3 Phase 07 verifier consumer

Phase 07은 portable bound snapshot과 accepted core execution contract를 사용해 cache 없이 다시 계산한다.

| 소비할 것 | 소비하면 안 되는 것 |
|---|---|
| Exact problem/travel/profile snapshot | Catalog 재조회 |
| Accepted Phase 03 contract | Concrete profile-catalog compile dependency |
| Approved capability implementation contract | Binder `PASS`나 search cache |
| Facet recomputation version | Solver summary나 final verdict |
| Corruption/fingerprint validation | Provider locator/secret |

Broken 증상:

- Verifier가 concrete profile module이나 solver를 compile-depend한다.
- Catalog drift를 다시 조회해 기존 solve 의미를 바꾼다.
- Bound snapshot fingerprint mismatch를 무시한다.
- Capability의 cached metric을 authoritative result로 신뢰한다.

### 15.4 Rollback

Phase 05 handoff 전에 실패하면 last accepted Phase 03 artifact와 Phase 04의 마지막 green WP output을 보존하고 incomplete bound artifact/evidence를 폐기한다. 같은 identity의 bytes를 overwrite하지 않는다.

Phase 04 acceptance 뒤 회귀가 발견되면 이전 accepted `Phase04BoundProfileHandoff` digest로 돌아간다. Catalog definition, registry entry 또는 published artifact를 같은 identity로 수정하지 않고 새 version/approved migration을 사용한다.

## 16. Source → requirement → WP → test/evidence traceability

| Requirement | Source | WP | Test/oracle | Evidence |
|---|---|---|---|---|
| `REQ-P04-AUTH-PIPELINE` syntax/no-lookup → pre-read scope auth → tenant lookup/integrity/preset → post-verified auth → registry/binder | [Human review HG-P04-R01](../reviews/phase-04-review.md#hg-p04-r01--high--authorization-pipeline과-failure-precedence가-충돌한다) | WP-04.1, 3 | Multi-defect read-count/ordinal tests | `E-P04-ISOLATION` |
| `REQ-P04-ORDER-SEMANTICS` unordered declaration과 ordered objective/stage/tie 분리 | [Human review HG-P04-R02](../reviews/phase-04-review.md#hg-p04-r02--high--descriptor-order-canonicalization이-semantic-order를-지울-수-있다) | WP-04.2~4 | Map/set permutation same; objective/stage swap different | `E-P04-BINDING`, `E-P04-ISOLATION` |
| `REQ-P04-CORE-CONTRACT-FIRST` core-owned identity/selection/failure/API 뒤 catalog 구현 | [Human review HG-P04-R03](../reviews/phase-04-review.md#hg-p04-r03--high--core-owned-contract와-wp-module-순서가-닫히지-않는다) | WP-04.1 | Core ownership/compile architecture tests | Architecture report |
| `REQ-P04-MAVEN-FRESHNESS` isolated repo + current `-am` graph + leaf exact test + root verify | [Human review HG-P04-R04](../reviews/phase-04-review.md#hg-p04-r04--high--leaf-maven-command가-current-core-freshness를-증명하지-않는다) | WP-04.1~6 | Fresh report/method/zero-test/stale-snapshot guards | Command/toolchain/repository manifest |
| `REQ-P04-COMPOSITION` test-only actual composition + restricted core-only downstream compile | [Human review HG-P04-R05](../reviews/phase-04-review.md#hg-p04-r05--medium--cross-module-composition과-downstream-compile-probe의-test-owner가-없다) | WP-04.5~6 | `ProfileCompositionContractTest`, restricted compile probe | Integration report or `NOT_PRODUCED` |
| `REQ-P04-SOURCE-DRIFT` all expected HEAD blobs + live bytes fail-closed at start/seal | [Human review HG-P04-R06](../reviews/phase-04-review.md#hg-p04-r06--medium--fingerprint-절차가-live-source-drift를-fail-closed하지-않는다) | WP-04.0, 6 | Machine-readable mismatch/missing negative check | Source snapshot/rebaseline digest |
| `REQ-P04-ACCEPTANCE-BRANCH` unresolved blocker는 blocked review only; resolved 뒤에만 receipt/handoff | [Human review HG-P04-R07](../reviews/phase-04-review.md#hg-p04-r07--medium--exit-blocker가-남은-wp-046-acceptance-경로가-모순된다) | WP-04.6 | Branch A no-receipt / Branch B receipt validity | Review/receipt/handoff DAG |
| `REQ-P04-EXACT-IDENTITY` exact customer/profile/version/preset/default | [Master §9.3](../../../master-design.md#93-profile-binding과-lifecycle), `Q-OBJ-01` | WP-04.0~1 | `ProfileIdentityResolutionTest` | `E-P04-BINDING` |
| `REQ-P04-AUTH` pre-read authorization/non-enumeration | [Integrated §20](../../../architecture-domain-implementation-design.md#20-security와-tenant-boundary), [Review F-P04-004](../../reviews/phase-04-review.md#4-findings) | WP-04.1, 4 | Authorization/read-count/redaction tests | `E-P04-ISOLATION` |
| `REQ-P04-CAP-SEPARATION` qualification/evaluation/operator key 분리 | [Master §5.3](../../../master-design.md#53-vehicle-size와-capability), [Review F-P04-007](../../reviews/phase-04-review.md#4-findings) | WP-04.2~3 | Registry/key-space collision tests | `E-P04-BINDING` |
| `REQ-P04-TYPED-CLOSURE` dependency/type/unit/range/direction | [Master §9](../../../master-design.md#9-extensible-policy-evaluation과-profile-architecture), `C-04` | WP-04.2~3 | Binder/combination oracle | `E-P04-BINDING` |
| `REQ-P04-COMPAT` core compatibility/resource rule preservation | [Final Domain §5.3](../../../2026-07-26-domain-design.md#compatibility-normalization), `Q-COMP-01~02` | WP-04.3 | Combination matrix | `E-P04-BINDING` |
| `REQ-P04-SERVICE-TRIP` explicit service policy/single trip | `Q-TIME-03`, `Q-REQ-01~02` | WP-04.3 | Service/trip oracle rows | `E-P04-BINDING` |
| `REQ-P04-OBJECTIVE` mandatory/LEASE lexicographic meaning | `Q-OBJ-02~03`, [Final Domain §10](../../../2026-07-26-domain-design.md#evaluation) | WP-04.3 | `ProfileObjectivePolicyTest` | `E-P04-BINDING` |
| `REQ-P04-LIFECYCLE` long-lived descriptor vs problem-bound runtime | [Canonical Phase 04 §7](../../phases/phase-04-capabilities-customer-profiles.md#7-io-artifact-contract-identity와-lifecycle) | WP-04.1, 4 | Lifecycle/corruption tests | `E-P04-ISOLATION` |
| `REQ-P04-ISOLATION` same problem multi-profile/no customer branch | [Master §2.4](../../../master-design.md#24-구현-완료의-의미), `C-03` | WP-04.4, 6 | Isolation + architecture | `E-P04-ISOLATION` |
| `REQ-P04-REPRO` order/backend/thread independent binding | [Master §13.2](../../../master-design.md#132-strong-reproducibility-envelope) | WP-04.2, 4 | Repeat/parallel/permutation | `E-P04-ISOLATION` |
| `REQ-P04-FACET` approved typed physical seam only | [Integrated §8.6](../../../architecture-domain-implementation-design.md#86-typed-domain-facet), `ADR-004` | WP-04.0, 4 | `FacetBindingGateTest` | `E-P04-FACET` |
| `REQ-P04-P03-EQUIV` existing Phase 03 declarations only | [Phase 03 handoff](../../phases/phase-03-route-propagation-evaluation-kernel.md#142-next--actual-document-unaccepted-phase-04) | WP-04.3~5 | Direct-vs-bound artifact | `E-P04-BINDING` |
| `REQ-P04-SOLUTION-EVAL` full solution authority | [Master RM-2](../../../master-design.md#154-rm-2--propagation-evaluation과-bound-profile), [Review residual](../../reviews/phase-04-review.md#62-residual-blockers--3) | WP-04.0, 5 | Restart compile/full-equality/corruption | Joint handoff evidence |
| `REQ-P04-TIE` business equality/context tie separation | [Phase 04 review §6.2](../../reviews/phase-04-review.md#62-residual-blockers--3) | WP-04.0, 5 | Comparator law/non-tied/permutation | `E-P04-BINDING` + joint review |
| `REQ-P04-PORTFOLIO-POLICY` traversal/`CLOCK`/utilization typed authority | [Phase 05 review F-P05-011](../../reviews/phase-05-review.md#4-findings) | WP-04.0, 5 | Restart reference-vector/golden trace | Joint Phase 04/05 manifest |
| `REQ-P04-ORACLE` faulty-double sensitivity | [Review F-P04-005](../../reviews/phase-04-review.md#4-findings) | WP-04.3~4 | `Phase04OracleSensitivityTest` | `E-P04-BINDING`, `E-P04-ISOLATION` |
| `REQ-P04-ARCH` module/customer/provider isolation | [Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계) | WP-04.2, 6 | `Phase04ProfileArchitectureTest` | Architecture report + all keys |
| `REQ-P04-HANDOFF` customer-neutral Phase 05/07 consumer | [Plan Phase 04](../../master-realization-plan.md#phase-04--재사용-기능과-고객-profile) | WP-04.5~6 | Consumer compile/equality/corruption | Receipt + handoff manifest |

## 17. 구현자가 마지막으로 스스로 묻는 질문

```text
Predecessor receipt나 scheduler owner가 없는가?
→ 멈춘다. 문서·oracle·joint review까지만 한다.

고객 차이를 표현하려는가?
→ 기존 fact의 parameter/score/objective인지 먼저 본다.
  새 물리 상태일 때만 ADR-004 facet을 검토한다.

Capability를 찾을 때 classpath나 latest가 필요한가?
→ 설계가 잘못됐다. Exact explicit inventory로 돌아간다.

Profile bind가 실패했는가?
→ 다른 profile/default로 재시도하지 않는다.
  Stable typed rejection으로 solve 전에 끝낸다.

Route artifact를 더하면 solution objective가 되는가?
→ 아니다. Approved full-solution evaluator가 없으면 last safe point에서 멈춘다.

Tie가 필요한가?
→ Observable business equality 뒤인지 확인한다.
  Context key가 non-tied 결과를 뒤집으면 결함이다.

Test가 exit 0인가?
→ Fresh exact method manifest, skipped 0, oracle sensitivity까지 확인한다.

Code와 tests가 모두 있는가?
→ 아직 완료가 아니다. Immutable evidence, review와 receipt를 확인한다.

Phase 13 backend나 Phase 14 official 값이 필요해 보이는가?
→ 이 Phase scope가 아니다. OPEN/GATED/authority를 우회하지 않는다.
```

## 18. 남아 있는 OPEN/GATED/deferred 요약

- `BLOCKED`: Phase 00~03 actual acceptance/evidence와 scheduler owner가 없다.
- `CHANGES_REQUIRED`: Full-solution evaluator, business equality/tie, portfolio traversal/`CLOCK`/utilization 세 cross-Phase 계약이 남아 있다.
- `OPEN / ADR-003`: Descriptor/schema/canonical encoding/registry/authorization의 최종 구현 형식.
- `OPEN / P-04`: `SolvePlan`과 reference vocabulary의 최종 type/name.
- `OPEN / ADR-004`: Typed facet. Empty set은 정상 baseline이다.
- `BLOCKED FOR PRODUCTION`: 승인된 customer descriptor/default/parameter가 없다.
- `OPEN — EXPERIMENT_REQUIRED`: `Q-BENCH-02` official execution 수치와 Phase 14A corpus/criteria.
- `GATED`: Phase 13 `C-17` route pool/MIP. 14A receipt와 별도 승인을 우회하지 않는다.
- `DEFERRED`: `Q-VAR-01`, multi-trip/rotation.
- `NOT GRANTED`: Phase 14B production authority와 public wire/API compatibility.

이 항목 중 하나라도 구현 편의를 위해 숫자, provider, default, enum ordinal 또는 library behavior로 대체하면 Phase 04는 완료가 아니다.
