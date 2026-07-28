# Phase 04 — 재사용 기능과 고객 profile

```yaml
document_status: REVIEWED_CHANGES_REQUIRED
document_version: 1.1
document_authoring_status: DRAFT_COMPLETE
document_review_status: COMPLETE
review_verdict: CHANGES_REQUIRED
review_document: docs/implementation/reviews/phase-04-review.md
phase: "04"
phase_name: capabilities-customer-profiles
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
phase_schedule_status: PLANNED
phase_acceptance_status: NOT_ACCEPTED
evidence_status: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
public_api_status: PROPOSED_NOT_APPROVED
descriptor_format_status: OPEN_REQUIRES_ADR_003_AND_PRODUCT_API_DATA_REVIEW
facet_status: OPEN_REQUIRES_ADR_004
scheduler_task_id: TBD_NOT_SUPPLIED
inventory_checkout:
  repository: /Users/brown/workspace/ro-next
  branch: codex/domain-design
  commit: 3424277
owners:
  implementation: RPDPTW Capability/Profile implementation owner role
  core_contract: Phase 03 Core/Evaluation owner role
  descriptor_and_catalog: Product·API·Data + Profile Catalog owner roles
  authorization: Product·Tenant/Security owner role
  capability_inventory: Distribution·Build/Security owner role
  downstream_search: Phase 05 Pair/Insertion owner role
  downstream_verification: Phase 07 Verification owner role
  review: independent Phase 04 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture and explicit composition-root rule
  - Phase 01 accepted compatibility/service/trip/policy normalized facts
  - Phase 02 accepted immutable ProblemInstance and complete PreparedTravel
  - Phase 03 accepted propagation/evaluation internal contract and evidence bundle
  - ADR-003 or equivalent review for capability registry and profile descriptor/catalog
planned_evidence:
  - E-P04-BINDING
  - E-P04-ISOLATION
  - E-P04-FACET
source_fingerprints_sha256:
  docs/master-design.md: 58554334b9f27586c93a685adc0facf0fbd7e79576c18890f0ac13891b2f803b
  docs/2026-07-26-domain-design.md: 1870662f85a08cc9a1e48a1974b96278eccddfd1519721d71b356c56034ecaab
  docs/2026-07-26-architecture-design.md: 3d4dbbfc7e4cbdb2f3985378d84fd5f717db770b04131573c00ed354a9f41614
  docs/architecture-domain-implementation-design.md: ec513ac1b0bacd88149683bf48c36f7e6edcd53a9232498597e3b0d57c585875
  docs/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 5921213ae419b9398bde8c91c3d6ada5aa64bf22a9b823e5b3889e1642085c05
  docs/implementation/README.md: accf7758802c253ae47e3d0fe41e190728c507195d0b41f27f14a25804c8f23f
historical_cross_check:
  file: docs/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
adjacent_context:
  phase_03:
    file: docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md
    consumed_sections: "§2.3, §3, §7, §9, §13~§15"
  phase_05:
    file: docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
    consumed_sections: "§2.3, §4, §6.3, §7, §10.3, §13~§16"
  fingerprint_policy: NO_RECIPROCAL_WHOLE_FILE_HASH_USE_DIRECTIONAL_HANDOFF_MANIFEST_AFTER_ACCEPTANCE
```

## 1. 문서 지위와 권위

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 권위 원문의 `REVIEW` metadata는 provenance로 보존하지만 이 상세 문서 작성을 중단하는 조건이 아니다. [Phase 04 독립 review](../reviews/phase-04-review.md)는 완료됐고 verdict는 `CHANGES_REQUIRED`다. 이 파일의 작성 완료, proposed type/signature 또는 future test 목록만으로 Phase 04 구현·evidence·acceptance가 완료되지는 않는다. 현재 존재하지 않는 module, source, test, descriptor, report와 command 결과는 모두 `proposed`, `future` 또는 `test-only`다.

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 compatibility/evaluation/profile 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 Maven/module/package/DAG
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 capability/profile과 15 Phase 배치
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)
7. Actual [Phase 03](phase-03-route-propagation-evaluation-kernel.md)의 accepted될 internal handoff contract
8. Actual [Phase 05](phase-05-pair-insertion-initial-portfolio.md)의 proposed downstream consumer contract

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 현재 authority로 사용하거나 복사하지 않았고 이 작업에서 수정하지 않는다.

문서 상태와 구현 상태는 독립이다.

| 상태 축 | 현재 값 | 의미 |
|---|---|---|
| Document | `REVIEWED_CHANGES_REQUIRED` | 독립 review는 완료됐으나 residual blocker 때문에 문서 변경이 필요함 |
| Implementation | `NOT_STARTED` | Capability/profile target source가 현재 없음 |
| Entry gate | `BLOCKED_BY_UNACCEPTED_PREDECESSORS` | Phase 00~03 accepted artifact/evidence가 없어 code/test 착수 불가 |
| Evidence | `NOT_PRODUCED` | `E-P04-*` report나 digest-protected bundle이 없음 |
| Schedule | `PLANNED` | Realization plan에 배치됐지만 implementation task 전이가 없음 |
| Acceptance | `NOT_ACCEPTED` | Review verdict가 `CHANGES_REQUIRED`이고 accepted evidence bundle이 없음 |

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 04에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §2.4, §3.1~§3.3, §4.2~§4.6, §5.3, §7.5, §9, §13.2~§13.3, §15.1/§15.4, §16~§17 | 고객 정책 격리, capability 축 분리, exact binding/lifecycle/fingerprint, no fallback/default, `RM-2`, reproducibility와 gate |
| [Final Domain](../../2026-07-26-domain-design.md) | §3, §5.3, §7, §9~§10, §15~§18, §20~§21 | Compatibility/service/trip/resource 사실, profile/objective 의미, verifier closure, 새 고객의 가장 좁은 seam |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2.1~§2.7, §5.2/§5.6, §6 | Core/capability/profile module DAG, registry identity, architecture enforcement와 evidence |
| [Integrated design](../../architecture-domain-implementation-design.md) | §1.4, §3, §7~§9, §19, §22~§25, §28~§30 | Capability code 대 profile data, descriptor/catalog/binder, Phase 03/05 경계, provenance, test와 anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-TIME-03`, `Q-COMP-01~02`, `Q-REQ-01~02`, `Q-OBJ-01~03`, `Q-RES-01`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | Explicit service policy, compatibility, single-trip, preset/default/mandatory/ownership, OPEN/GATED/deferred 상태 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§4, Phase 03~05, §8~§15 | Current inventory, Phase 04 entry/exit, evidence/DoD, blockers와 traceability |
| [구현 문서 지도](../README.md) | §3~§7 | Authority, canonical filename, status/review/link 규칙 |
| [Actual Phase 03](phase-03-route-propagation-evaluation-kernel.md) | §3~§7, §9~§15 | 변경하지 않을 evaluation SPI, `PropagationDeclaration`/`EvaluationPlan`, typed facet gate, Phase 04/05 handoff |
| [Actual Phase 05](phase-05-pair-insertion-initial-portfolio.md) | §2.3, §4, §6.3, §7.1~§7.5, §10.3, §13~§16 | `BoundProfile`/`SolvePlan`, proposed `BoundInsertionAuthority`, exact identity/delegated evaluation 소비 요구와 insertion/portfolio ownership |

Phase 03/05는 read-only adjacent context이며 exact consumed section만 추적한다. 변경 가능한 인접 문서 whole-file SHA나 일시적인 review status를 authoritative source 또는 acceptance digest로 고정하지 않는다. 문서 존재도 accepted implementation handoff가 아니다. Phase 04가 accepted될 때 producer가 발행한 순환 self-reference 없는 section-scoped artifact/API/evidence manifest를 Phase 05 consumer가 단방향으로 pin해야 한다.

Final Domain/Architecture의 일부 historical 행은 `Q-INFRA-01 DEFERRED`, 질문 수 `25/1/2`를 표시하지만 현재 authority는 질문 등록부와 canonical Master의 `Q-INFRA-01 RESOLVED`, `26/1/1`이다. 이 drift는 Phase 04에 cloud dependency를 추가하는 근거가 아니며 profile/capability 경계는 provider-neutral하게 유지한다.

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 04는 exact customer/profile/version/preset과 승인된 capability registry를 solve 전에 한 번 resolve·검증하고, Phase 03 평가 kernel이 이미 소유한 계약으로 materialize한 **solve-bound immutable `BoundProfile`**을 만든다.

```text
exact authorized profile reference
+ verified immutable descriptor
+ explicit capability registry snapshot
+ Phase 02 problem/travel identities
+ Phase 03 contract versions
→ pure deterministic binding
→ BoundProfileSnapshot + executable BoundProfile
→ Phase 05/07가 customer/catalog 재조회 없이 소비
```

같은 canonical descriptor, registry, problem/travel, Phase 03 contract/build identity는 descriptor 파일 순서, registry 등록 순서, thread, locale, classpath와 catalog backend에 관계없이 같은 dependency closure, Phase 03 declaration bytes와 `BoundProfileFingerprint`를 만들어야 한다.

### 2.2 포함 범위

- `CustomerKey`, `ProfileKey`, `ProfileVersion`, `PresetKey`, schema/implementation-contract version의 분리
- Exact descriptor 조회, tenant/customer authorization 결과와 descriptor content verification
- Exact profile version에 선언된 default preset만을 사용하는 omitted-preset resolution
- Explicit capability inventory/registry와 duplicate/same-identity-different-content 거부
- Capability key/version, typed parameter, fact/metric/unit/output dependency와 implementation fingerprint
- Core-required physical rules, profile-selectable reusable behavior와 customer/catalog policy의 분리
- Problem의 compatibility/resource/service/trip/ownership/mandatory facts와 profile policy 조합 검증
- Stable dependency graph closure, cycle/duplicate/missing/type/unit/range/direction rejection
- Phase 03 `PropagationDeclaration`과 `EvaluationPlan`의 **materialization만** 수행하는 binding
- Long-lived descriptor/profile identity와 solve-bound problem/travel identity를 분리한 lifecycle/fingerprint
- Immutable portable `BoundProfileSnapshot`과 in-memory executable `BoundProfile`의 equality
- Profile drift/corruption/isolation/reproducibility/architecture와 combination rejection oracle
- Typed facet의 승인 gate, empty-facet baseline과 승인된 경우의 independent recomputation contract
- Phase 05/07이 재사용할 bound evaluation/capability handoff
- Canonical `RM-2` full-solution evaluation에 필요한 profile/declaration identity의 binding 경계와 Phase 03~05 공동 review/compile/equality gate; exact solution evaluator API는 공동 승인을 기다림
- Business objective equality와 solution/insertion-context tie를 분리하는 handoff gate; tie가 non-tied business objective를 뒤집지 않는 의미만 고정

### 2.3 명시적 비범위

- Phase 03의 interface, propagation algorithm, neutral fact, unit 또는 comparator 의미 변경
- Raw input parsing, compatibility 계산, service/trip normalization 또는 prepared travel 생성
- Phase 05의 pair position 열거, insertion delta, route/bank/COW mutation과 4×2 portfolio
- Full-solution objective aggregation, route-artifact reuse/invalidation, candidate ranking과 solution total-order 구현
- Phase 06의 ALNS operator/acceptance/adaptive state, step/round/watchdog 수치와 runtime
- Phase 07 verifier verdict, final outcome/audit/diagnostic/publication
- Application port, profile object-storage catalog, AWS/GCP adapter, dynamic code/JAR loading
- Route pool/MIP/projection/backend와 `C-17` 기능
- Production 고객 identity, 단가, threshold, profile descriptor 또는 default preset을 원문 없이 발명
- Public/wire descriptor format, canonical hash algorithm 또는 external compatibility 승인
- Multi-trip/rotation, optional variant와 승인되지 않은 typed facet

Phase 04의 test descriptor/customer/component key와 수치는 모두 `test-only`다. 이를 production catalog, official preset 또는 source authority로 승격하지 않는다.

## 3. Phase-local 결정, 소유권과 불변조건

아래 type/signature는 **proposed internal design**이다. Phase 00/03/04 review와 `ADR-003`이 승인하기 전 public API가 아니다. 새로운 `C-*` 또는 `Q-*` ID를 만들지 않는다.

### 3.1 무엇이 어디에 속하는가

| 분류 | 소유자 | 예/의미 | Profile이 할 수 없는 일 |
|---|---|---|---|
| Core physical contract | Phase 01~03 core | Pair/precedence, capacity prefix, size/capability/zone AND, prepared travel, single-trip terminal, resource limit, checked time/load | 끄기, 완화, finite penalty화, 다른 의미로 재해석 |
| Reusable executable capability | `rpdptw-capabilities` | 기존 fact를 소비하는 constraint/metric/score/objective, explicit service rule materializer, 승인된 typed facet | Customer name 읽기, raw route reflection, search mutation |
| Customer profile policy | Immutable descriptor/preset | 사용할 capability exact version, typed parameter, objective order/direction, exact default preset | Class/script/cloud locator 주입, 다른 customer preset fallback |
| Catalog/authorization policy | Profile catalog + Product/Security | 어느 customer가 어느 exact profile/preset을 쓸 수 있는지, same identity overwrite 금지 | Feasibility/objective 계산 |
| Capability provider inventory | Distribution composition root | 이 build에 승인·포함된 capability implementation exact set | Classpath first-wins, runtime plugin download, hidden implementation 교체 |
| Infrastructure provider policy | Phase 08~12 adapter/deployment | AWS/local/future provider 조립 | Profile/evaluation fingerprint 의미나 customer objective 정의 |

Vehicle의 `requiredCapabilities ⊆ vehicle.capabilities`에서 “capability”는 **vehicle qualification fact**다. Phase 04의 executable `EvaluationCapability`와 같은 registry key 공간으로 합치지 않는다. Profile은 Phase 02가 확정한 size/qualification/zone eligibility를 다시 계산하지 않는다.

Vehicle qualification key, Phase 03 evaluation capability key, Phase 05/06 algorithm operator key와 budget/guard key는 서로 다른 namespace와 승인 inventory다. Proposed `SolvePlan`이 operator reference를 보존하더라도 이를 `CapabilityRegistryView`로 resolve하거나 evaluation capability로 가장하면 안 된다. Phase 04는 승인된 cross-phase inventory의 exact typed reference만 bind하며 operator implementation/runtime은 Phase 05/06에 남긴다.

### 3.2 결정과 상태

| 결정 | 상태 | 내용 |
|---|---|---|
| Kernel preservation | FIXED | Phase 03 signature/meaning을 변경하지 않고 binder가 기존 `PropagationDeclaration`/`EvaluationPlan`을 만든다. |
| Exact identity | FIXED | Customer/profile/version은 모두 exact다. `latest`, range, name similarity와 cross-customer fallback을 금지한다. |
| Omitted preset | FIXED | 해당 exact profile version이 선언한 exact default만 허용한다. Default가 없으면 error다. |
| Descriptor format | OPEN/PROPOSED | Serialization/schema/canonical encoding은 `ADR-003`과 Product/API/Data review 대상이다. 예시 YAML/JSON은 wire authority가 아니다. |
| Registry assembly | PROPOSED | Composition root가 explicit stable inventory를 주입한다. Reflection scan/`ServiceLoader`/classpath order는 사용하지 않는다. |
| Profile runtime split | PROPOSED | Portable `BoundProfileSnapshot`과 executable in-memory `BoundProfile`을 같은 semantic fingerprint로 연결한다. |
| `SolvePlan` type/name | PROPOSED/OPEN (`P-04`) | Stage/objective/operator/budget-reference/guard 의미와 fingerprint만 고정한다. Numeric run budget, operator runtime와 search state는 소유하지 않는다. |
| Solution evaluation binding boundary | OPEN/CROSS-PHASE BLOCKER | Phase 04는 exact problem/travel/profile/declaration identity를 제공하지만 route artifacts + bank를 full-solution objective로 집계하는 API/owner/identity/failure/comparator를 발명하지 않는다. |
| Comparator/tie boundary | OPEN/CROSS-PHASE BLOCKER | Ordered business vector와 “business equality 뒤에만 stable/context tie 적용” 의미만 고정한다. Equality 관찰 API, solution tie와 insertion-context tie의 owner/order/fingerprint는 공동 승인이 필요하다. |
| Facet seam | OPEN/GATED | `ADR-004`와 Phase 03/07 recomputation contract가 승인된 facet만 bind한다. 승인된 facet이 없으면 empty set이 정상이다. |
| Production catalog | BLOCKED/OPEN | 승인된 실제 customer descriptor가 제공되지 않았다. Generic binder는 test-only descriptor로 검증할 수 있지만 production entry는 만들지 않는다. |

### 3.3 불변조건

1. **Exact resolution:** 모든 profile, preset, capability와 contract version은 exact match이며 범위/별칭/fallback이 없다.
2. **Explicit defaults:** `START_ONLY` 같은 source-approved default도 canonical descriptor/bound snapshot에 explicit 값과 provenance로 materialize한다. Binder 내부 hidden fallback이 아니다.
3. **Pure binding:** Executable binder는 verified descriptor와 immutable registry/problem/travel만 소비하고 I/O, clock, locale, environment, random, mutable global registry를 읽지 않는다.
4. **One-way layering:** Problem facts → dependency closure → Phase 03 declaration/plan 순서다. Capability가 raw DTO, search/cache 또는 final result를 읽지 않는다.
5. **Core rule preservation:** Pair, capacity, compatibility, resource, service/trip authority는 profile component로 disable할 수 없다.
6. **Typed components:** Parameter, dependency, unit, value와 output은 typed contract다. `Map<String,Object>`, arbitrary expression/script/class name을 금지한다.
7. **Unique stable keys:** 같은 key/version은 정확히 한 implementation contract/content를 가리킨다. Duplicate나 same identity/different bytes는 startup/binding failure다.
8. **Closed dependencies:** Missing/cycle/duplicate/type/unit/range/direction 불일치는 kernel 호출 전에 typed binding error다.
9. **Problem-bound lifecycle:** Dense/request-indexed binding은 한 problem/travel fingerprint에만 속하며 다른 solve/problem에 재사용하지 않는다.
10. **Immutable runtime:** Bound profile과 capability instances/config는 생성 후 불변이다. Evaluation scratch/cache/telemetry를 보유하거나 run/thread 간 공유하지 않는다.
11. **Objective truth:** Mandatory는 사용 시 최상위 lexicographic dimension이고 hard/Big-M이 아니다. `LEASE`가 존재하면 이를 지원하는 preset은 outsourced dimension을 명시해야 한다.
12. **Reproducible identity:** Descriptor, selected preset, authorization policy, registry/implementation, dependency closure, problem/travel, Phase 03 declaration/plan과 build contract가 fingerprint에 포함된다.
13. **No premature search/runtime:** Bound artifact에는 insertion option, route/bank mutation, operator state, seed, ALNS budget, cloud/runtime handle이 없다.
14. **No ad hoc solution authority:** Route-level artifact의 합이나 insertion delta를 full-solution objective authority로 승격하지 않는다. Exact solution evaluator 계약이 승인되기 전 last safe point는 Phase 03 route kernel + ordered business vector + immutable routes/bank다.
15. **Tie after business equality only:** Solution/insertion-context tie는 business comparator equality 뒤에만 적용되며 서로의 key namespace나 fingerprint를 암묵적으로 재사용하지 않는다.

## 4. Entry gate와 확인 방법

문서 작성은 완료할 수 있지만 implementation은 다음 gate가 모두 충족될 때까지 `BLOCKED`다.

| Entry 항목 | 확인 방법 | 현재 checkout 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Review verdict + `E-P00-ARCH`; target reactor/capability/profile modules와 composition root 존재 | Review `PASS_WITH_RESIDUAL_BLOCKERS`; implementation `NOT_STARTED`, acceptance `PLANNED`, evidence `NOT_PRODUCED`; root 단일 project | BLOCKED |
| Phase 01 accepted | `E-P01-COMPAT/TIME/ERROR`; normalized service/trip/ownership/mandatory/profile-reference facts | Review `ACCEPTED_WITH_APPLIED_CORRECTIONS`; phase acceptance `BLOCKED_NOT_IMPLEMENTED`, implementation/evidence 미완료 | BLOCKED |
| Phase 02 accepted | `ProblemInstance`, `PreparedTravel`, dependency declarations와 exact fingerprints | Review `PASS_AFTER_APPLIED_CORRECTIONS`; phase acceptance `BLOCKED_NOT_IMPLEMENTED`, implementation evidence 없음 | BLOCKED |
| [Phase 03](phase-03-route-propagation-evaluation-kernel.md) accepted | Review verdict, `E-P03-*`, approved SPI signature/unit/failure/facet status | Review `CHANGES_REQUIRED`, entry `BLOCKED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `NOT_ACCEPTED` | BLOCKED |
| Full-solution evaluation boundary | Exact owner/API/input/identity/invalidation/failure/comparator와 Phase 03~05 reciprocal compile/full-equality/corruption test 승인 | Phase 03/05가 cross-phase blocker로 기록; Phase 04에는 bound identity만 proposed | CROSS_PHASE_REVIEW_BLOCKED |
| Business equality/context tie boundary | Equality 관찰 API, solution stable tie와 insertion-context tie owner/order/fingerprint 승인 | “non-tied business objective 우선” 의미만 고정됨 | CROSS_PHASE_REVIEW_BLOCKED |
| `ADR-003` | Registry assembly, descriptor/catalog format, canonical encoding과 duplicate policy 승인 | ADR/evidence가 현재 없음 | REVIEW_REQUIRED |
| `ADR-004` | Typed facet contract와 verifier recomputation 승인 | 현재 OPEN/PROPOSED | FACET_ONLY_BLOCKED |
| Production profile authority | Exact customer/profile/version/preset/authorization descriptor 승인 | Source authority에 실제 production descriptor/수치 없음 | PRODUCTION_ONBOARDING_BLOCKED |
| Owner/scheduler | Exact task ID와 implementer/reviewer 역할 | `TBD_NOT_SUPPLIED` | OWNER_GATE |

Phase 03 handoff를 받을 때 다음을 exact 대조한다.

```text
accepted Phase03ApiSignatureFingerprint
== binder target contract fingerprint

ProblemInstance.preparedTravelFingerprint
== PreparedTravel.fingerprint

all descriptor component dependencies
⊆ accepted Phase 03 fact/metric/unit contract

all approved facet contracts
have independent Phase 07 recomputation contract

full-solution evaluator input identity
includes exact problem/travel/profile + ordered routes + bank

solution/insertion tie
is applied only after observable business objective equality
```

불일치가 있으면 adapter/default/shim으로 보완하지 않는다. Last safe point는 이 문서, descriptor schema 초안과 independent test oracle뿐이며 source implementation을 시작하지 않는다.

## 5. 2026-07-28 current inventory

Repository/toolchain/source inventory 기준은 branch `codex/domain-design`, commit `3424277`에서 읽은 historical baseline snapshot이다. `docs/implementation/` 전체는 이 문서 작성 전부터 Git 기준 untracked였으며 기존 사용자 변경으로 취급한다. 아래 review 행만 최종 감사 시점의 live metadata를 표시하며 historical snapshot을 다시 쓰지 않는다.

| 항목 | 실제 관찰 | Phase 04 해석 |
|---|---|---|
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Java 25 목표와 일치하지만 Phase 04 build/evidence는 아님 |
| Maven | Root `com.ronext:ro-next:0.1.0-SNAPSHOT` 단일 project, release 25, module 목록 없음 | `rpdptw-core/capabilities/profile-catalog` reactor가 없음 |
| Root dependencies | Google Workflow Executions/Storage, Jackson, JUnit가 같은 classpath | Target core/profile/provider 격리 전 placeholder |
| Main Java | `com.ronext.optimizer` 아래 HTTP/GCP adapter 5개와 `AlnsBatchEngine` 1개 | Capability registry, descriptor, binder, `BoundProfile`이 없음 |
| Placeholder behavior | `AlnsBatchEngine`은 `double` 합성 objective와 `Map<String,Object>` candidate를 생성 | Evaluation/profile/capability evidence로 재사용 금지 |
| Test | `AlnsBatchEngineTest` 1개 | Synthetic status/objective만 검사; Phase 04 test/evidence가 아님 |
| Phase documents | Phase 00~14 상세 15개 actual | Document 존재와 accepted predecessor/handoff를 구분해야 함 |
| Phase reviews — live final audit | 15/15 완료. `00 PASS_WITH_RESIDUAL_BLOCKERS`; `01 ACCEPTED_WITH_APPLIED_CORRECTIONS`; `02 PASS_AFTER_APPLIED_CORRECTIONS`; `03~12 CHANGES_REQUIRED`; `13 PASS_WITH_RESIDUAL_BLOCKERS`; `14 CHANGES_REQUIRED` | Document review verdict일 뿐 implementation/evidence/phase acceptance를 승격하지 않음 |
| Adjacent Phase 03/05 reviews — live | Phase 03 `CHANGES_REQUIRED`; Phase 05 `CHANGES_REQUIRED`; 둘 다 implementation entry `BLOCKED`, implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `NOT_ACCEPTED` | Exact cited sections와 residual blockers만 소비하며 accepted handoff로 보지 않음 |
| Customer/profile data | Integrated design의 설명·예시 외 승인된 production descriptor 없음 | 예시 고객/단가/version을 production 값으로 복사 금지 |

이 문서 작성 turn은 `src`, `pom.xml`, 공용 README/plan/progress, 다른 Phase/review와 `docs/codex/*`를 수정하지 않는다.

## 6. Proposed 변경 module/package/file tree

아래는 Phase 00/03 accepted path가 같은 의미를 승인한 뒤 Phase 04 implementation에서 허용할 target이다. 이 문서 작성 turn에서는 어느 경로도 만들지 않는다.

```text
rpdptw/core/
└── src/main/java/com/ronext/rpdptw/evaluation/
    ├── api/profile/
    │   ├── CustomerKey.java
    │   ├── ProfileKey.java
    │   ├── ProfileVersion.java
    │   ├── PresetKey.java
    │   ├── CapabilityKey.java
    │   ├── CapabilityVersion.java
    │   ├── ImplementationContractVersion.java
    │   ├── ProfileSchemaVersion.java
    │   ├── ProfileLookupCoordinate.java
    │   ├── ProfileDescriptorIdentity.java
    │   ├── ProfileDescriptorFingerprint.java
    │   ├── VerifiedProfileDefinition.java
    │   ├── ResolvedPresetDefinition.java
    │   ├── ProfileAuthorizationDecision.java
    │   ├── AuthorizedProfileSelection.java
    │   ├── CapabilityRegistryView.java
    │   ├── CapabilityRegistrySnapshot.java
    │   ├── CapabilityResolution.java
    │   ├── CapabilityProvider.java
    │   ├── CapabilityContract.java
    │   ├── CapabilityParameters.java
    │   ├── CapabilityDependency.java
    │   ├── ProblemCompatibilityRequirement.java
    │   └── BoundCapability.java
    └── runtime/profile/
        ├── ProfileBindingEngine.java
        ├── ProfileBindingRequest.java
        ├── ProfileBindingResult.java
        ├── ProfileBindingFailure.java
        ├── BoundProfile.java
        ├── BoundProfileSnapshot.java
        ├── BoundProfileIdentity.java
        ├── BoundCapabilitySet.java
        ├── BoundDependencyClosure.java
        └── internal/
            ├── DefaultProfileBindingEngine.java
            ├── DependencyClosureValidator.java
            ├── ProblemProfileCompatibilityValidator.java
            └── BoundProfileFingerprintMaterializer.java

rpdptw/capabilities/
├── pom.xml                                      # Phase 00 owner; no customer/cloud/search dependency
└── src/
    ├── main/java/com/ronext/rpdptw/capability/
    │   ├── registry/
    │   │   ├── ExplicitCapabilityRegistry.java    # implements core CapabilityRegistryView
    │   │   └── ExplicitCapabilityInventory.java
    │   ├── standard/                            # only approved reusable baseline components
    │   ├── servicelevel/                        # add only with approved reusable requirement
    │   ├── fleetcost/
    │   └── facet/                               # ADR-004 approved implementations only
    └── test/java/com/ronext/rpdptw/capability/
        ├── CapabilityRegistryContractTest.java
        └── CapabilityImplementationContractTest.java

rpdptw/profile-catalog/
├── pom.xml                                      # artifactId: rpdptw-profile-catalog
└── src/
    ├── main/java/com/ronext/rpdptw/profile/catalog/
    │   ├── CatalogProfileProvider.java
    │   ├── ProfileCatalog.java
    │   ├── ProfileResolution.java
    │   ├── ProfileAuthorization.java
    │   └── internal/
    │       ├── CatalogDescriptorDocument.java
    │       ├── CanonicalProfileDescriptorReader.java
    │       ├── DescriptorSchemaValidator.java
    │       └── CatalogIntegrityValidator.java
    ├── main/resources/profiles/
    │   └── schemas/                             # ADR-003 approved schema only
    └── test/
        ├── java/com/ronext/rpdptw/profile/
        │   ├── ProfileIdentityResolutionTest.java
        │   ├── ProfileBinderContractTest.java
        │   ├── ProfileCombinationOracleTest.java
        │   ├── ProfileObjectivePolicyTest.java
        │   ├── BoundProfileLifecycleTest.java
        │   ├── BoundProfileCorruptionTest.java
        │   ├── BoundProfileIsolationTest.java
        │   ├── BoundProfileReproducibilityTest.java
        │   ├── BoundProfilePhase03ContractTest.java
        │   ├── ProfileAuthorizationSecurityTest.java
        │   ├── ProfileBindingSecurityObservabilityTest.java
        │   ├── Phase04OracleSensitivityTest.java
        │   ├── FacetBindingGateTest.java
        │   └── testing/
        │       ├── TestOnlyProfileDescriptorBuilder.java
        │       ├── TestOnlyCapabilityProviderBuilder.java
        │       ├── ProfileCombinationFixtureBuilder.java
        │       ├── ExhaustiveProfileBindingOracle.java
        │       └── CorruptBoundProfileFixtureBuilder.java
        └── resources/profiles/test-only/        # never production catalog

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase04ProfileArchitectureTest.java
```

`standard`, `servicelevel`, `fleetcost` package 이름은 target ownership 예시다. 승인된 capability contract가 없는 구현/수치/production descriptor를 빈 skeleton이나 가짜 provider로 만들지 않는다.

Phase 04의 descriptor/provider builder와 oracle은 profile-catalog의 production type을 소비하므로 `rpdptw-profile-catalog` 자체 `src/test/java`에 둔다. Phase 00이 정의한 generic `rpdptw-test-fixtures -TEST→ rpdptw-core` artifact에 profile-catalog 의존성을 역으로 추가하거나 attached fixture artifact를 production compile path에 올리지 않는다. 다른 Phase가 fixture를 공유해야 하면 Phase 00 architecture review에서 acyclic test-only artifact를 별도로 승인한 뒤 이동한다.

금지 변경 target:

- Phase 03 `RoutePropagator`, `RouteEvaluationKernel`, facts/result/SPI signature와 의미
- `rpdptw-solver`, insertion/portfolio/COW/ALNS, verification verdict/finalization
- Adapter/application/storage/workflow/compute/deployment
- Current `com.ronext.optimizer.*` placeholder를 profile engine으로 개조
- Customer별 module/POM/JAR 또는 customer-name package
- Root에 capabilities/profile/cloud dependency를 공통 승격

## 7. I/O artifact, contract, identity와 lifecycle

### 7.1 Artifact 계약

| Artifact | Owner/생성 시점 | Identity 최소 구성 | Lifecycle/소비자 |
|---|---|---|---|
| `VerifiedProfileDefinition` | Catalog, exact read/schema/digest 검증 뒤 core-owned data contract로 materialize | Customer/profile/version, schema version, canonical content fingerprint | Long-lived immutable definition; executable instance 아님 |
| `CapabilityRegistrySnapshot` | Distribution composition root/startup | Stable ordered capability key/version/contract/implementation/build fingerprints | Process/build scoped immutable; classpath scan 결과가 아님 |
| `AuthorizedProfileSelection` | Catalog provider | Verified definition + selected exact preset + omitted/default resolution provenance + authorization decision | Binding call-local immutable core-owned data contract |
| `BoundDependencyClosure` | Binding engine | Ordered component/dependency/unit/type/parameter contract digest | Problem/profile-bound immutable |
| `BoundCapabilitySet` | Binding engine | Ordered exact provider identities + typed config + closure/facet contract | Solve-bound immutable; scratch/cache 없음 |
| `SolvePlan` | Binding engine | Exact stage order + objective/operator/budget-reference/guard keys·versions + contract fingerprint | Typed immutable declaration; operator implementation, numeric run budget와 runtime state 없음 |
| `BoundProfileSnapshot` | Binding engine | Profile/preset/catalog/authorization/registry/problem/travel/Phase03/`SolvePlan`/build fingerprints | Portable immutable data artifact; code/class name/secret/locator 없음 |
| `BoundProfile` | Binding engine | Snapshot fingerprint + executable bound capability set + exact Phase 03 declarations + typed `SolvePlan` | In-memory solve-bound object; Phase 05/07 consumer |
| `ProfileBindingFailure` | Catalog/binder | Stable kind, safe subject/path, expected/actual identity without secret/PII | Pre-solve failure; score/unassignment/runtime retry로 변환 금지 |

`BoundProfileSnapshot`은 provider locator나 arbitrary serialized Java object를 포함하지 않는다. 분산 worker에서 재구성할 때 exact registry/build와 descriptor snapshot을 다시 대조하고 같은 `BoundProfileFingerprint`가 아니면 실행을 시작하지 않는다. Object-storage persistence와 retrieval은 Phase 08/09가 소유한다.

### 7.2 Identity와 fingerprint

Identity를 한 값으로 합치지 않는다.

```text
ProfileLookupCoordinate
  CustomerKey
  ProfileKey
  ProfileVersion

ProfileDescriptorIdentity
  ProfileLookupCoordinate
  ProfileSchemaVersion

ResolvedProfileIdentity
  ProfileDescriptorIdentity
  exact PresetKey
  ImplementationContractVersion

CapabilityImplementationIdentity
  CapabilityKey
  CapabilityVersion
  capability contract version
  implementation fingerprint

BoundProfileIdentity
  ResolvedProfileIdentity
  ProfileDescriptorFingerprint
  authorization policy fingerprint
  CapabilityRegistryFingerprint
  ProblemFingerprint
  PreparedTravelFingerprint
  Phase03ApiSignatureFingerprint
  PropagationDeclarationFingerprint
  EvaluationPlanFingerprint
  SolvePlanFingerprint
  build/runtime compatibility fingerprint
```

Catalog exact lookup의 유일 coordinate는 `ProfileLookupCoordinate(customer, profile, version)`다. 같은 coordinate 아래 schema version, canonical content, default preset 또는 authorization-relevant descriptor 의미가 다른 두 definition을 병렬 등록하거나 overwrite하지 않는다. 승인된 의미/schema/default 변경은 새 `ProfileVersion` 또는 별도 승인된 migration으로만 도입한다. `ProfileSchemaVersion`은 lookup fallback 차원이 아니라 읽은 content를 검증하고 fingerprint에 포함하는 metadata다. Unsupported schema는 다른 version/profile로 fallback하지 않고 typed rejection이다.

`ProfileDescriptorFingerprint`는 long-lived descriptor content의 의미 identity이고 problem ID를 포함하지 않는다. `BoundProfileFingerprint`는 exact problem/travel에 materialize한 solve-bound identity다. Profile version 문자열, Java class 이름, `toString()`, unordered collection, file path, S3 key, object identity 또는 mutable `latest`를 fingerprint 대신 쓰지 않는다.

Fingerprint의 canonical encoding/hash algorithm은 Phase 00/`ADR-003` 소유다. Phase 04는 다음 semantic field의 포함만 고정한다.

- Descriptor identity/schema/content와 exact selected preset/default provenance
- Authorization policy decision/version/fingerprint
- 모든 capability key/version/contract/implementation/config와 stable dependency order
- Parameter type/unit/value와 default가 있으면 승인된 schema default provenance
- Problem/travel and normalized compatibility/service/trip/resource policy identities
- Materialized Phase 03 propagation/evaluation plan identities
- Typed `SolvePlan` stage/objective/operator/budget-reference/guard identities and contract version
- Facet contract/provider/verifier recomputation version
- Capability registry와 build/runtime compatibility identity

### 7.3 Lifecycle과 상태 전이

```text
ProfileReference
→ requested tenant/customer scope authorization
→ exact tenant-scoped catalog lookup
→ descriptor schema/content verification
→ exact preset resolution
→ exact descriptor/preset authorization
→ explicit registry snapshot resolution
→ dependency/type/unit/range/objective closure
→ problem compatibility/resource/service/trip/policy validation
→ Phase 03 declaration/plan materialization
→ snapshot/runtime fingerprint equality
→ BoundProfile
```

실패는 어느 단계에서도 다른 profile/preset/provider로 진행하지 않는다.

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

any transition
→ REJECTED(typed ProfileBindingFailure)
```

`BOUND` 이후 descriptor/catalog/registry를 다시 읽지 않는다. Profile/catalog drift는 다음 solve 또는 worker reconstruction에서 exact identity mismatch로 검출하며 이미 bound된 객체를 mutate하지 않는다.

### 7.4 Proposed Java 25 contract

Phase 03 API는 그대로 유지한다. Phase 04는 그 API를 구현하는 provider와 binding wrapper만 추가한다.

```java
package com.ronext.rpdptw.evaluation.api.profile;

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

public record CapabilityContract<P extends CapabilityParameters>(
    CapabilityKey key,
    CapabilityVersion version,
    ImplementationContractVersion implementationContractVersion,
    Class<P> parameterType,
    List<CapabilityDependency> dependencies,
    List<ProblemCompatibilityRequirement> compatibilityRequirements,
    CapabilityContractFingerprint fingerprint
) {}
```

`Class<P>`는 approved typed record의 type token일 뿐 descriptor가 arbitrary class name을 지정하거나 reflection으로 instantiate하는 권한이 아니다. Descriptor parser는 approved capability contract가 제공하는 typed codec/schema만 사용한다. Codec/serialization의 exact API는 `ADR-003` 전까지 open이다.

```java
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

    record FacetContract(
        FacetKey key,
        ImplementationContractVersion version
    ) implements CapabilityDependency {}
}
```

Provider interface는 별도 Maven module 구현을 허용해야 하므로 cross-module `sealed`로 만들지 않는다. 대신 key/version/contract와 bound result/failure 같은 data/result hierarchy를 sealed/record로 고정한다.

```java
package com.ronext.rpdptw.evaluation.runtime.profile;

public interface ProfileBindingEngine {
    ProfileBindingResult bind(ProfileBindingRequest request);
}

public record ProfileBindingRequest(
    ProblemInstance problem,
    PreparedTravel preparedTravel,
    AuthorizedProfileSelection profileSelection,
    CapabilityRegistryView capabilityRegistry,
    Phase03ContractIdentity phase03Contract,
    BuildRuntimeCompatibilityFingerprint buildRuntime
) {}

public sealed interface ProfileBindingResult
        permits ProfileBindingResult.Bound,
                ProfileBindingResult.Rejected {

    record Bound(BoundProfile profile) implements ProfileBindingResult {}
    record Rejected(ProfileBindingFailure failure)
        implements ProfileBindingResult {}
}
```

Failure는 normal infeasibility와 corruption을 구분한다.

```java
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

정확한 subtype field는 review 대상이지만 stable code에는 최소 unknown/latest/cross-customer, duplicate, same-identity-different-content, missing/cycle, parameter/type/unit/range, objective direction/order, LEASE/mandatory, service/trip/resource/compatibility, problem/travel/Phase03 fingerprint와 facet approval 실패를 구분할 수 있어야 한다.

```java
public record SolvePlan(
    SolvePlanContractVersion contractVersion,
    List<SolveStageDeclaration> stagesInExactOrder,
    SolvePlanFingerprint fingerprint
) {}

public record SolveStageDeclaration(
    SolveStageKey key,
    List<ObjectiveDimensionKey> objectivePriority,
    List<OperatorCapabilityReference> approvedOperatorReferences,
    List<BudgetReference> budgetReferences,
    SolveStageGuard guard
) {}

public sealed interface SolveStageGuard
        permits SolveStageGuard.Always,
                SolveStageGuard.TypedCondition {

    record Always() implements SolveStageGuard {}

    record TypedCondition(
        GuardContractKey key,
        GuardContractVersion version,
        GuardParameters parameters
    ) implements SolveStageGuard {}
}

public record BoundProfile(
    BoundProfileIdentity identity,
    BoundProfileSnapshot snapshot,
    BoundCapabilitySet capabilities,
    PropagationDeclaration propagationDeclaration,
    EvaluationPlan evaluationPlan,
    SolvePlan solvePlan,
    BoundProfileFingerprint fingerprint
) {
    // defensive immutable values; snapshot/runtime semantic fingerprints equal
}
```

`SolvePlan`, stage/guard type 이름은 Master `P-04`에 따라 **PROPOSED/OPEN**이다. `OperatorCapabilityReference`와 `BudgetReference`는 exact key/version declaration이지 구현 object, numeric run value 또는 mutable counter가 아니다. Operator reference는 Phase 03 `CapabilityRegistryView`의 evaluation capability key가 아니며 승인된 Phase 05/06 operator inventory/contract로만 검증한다. 그 inventory가 아직 승인되지 않았거나 exact reference를 검증할 수 없으면 해당 `SolvePlan` binding을 거부하며 “나중에 찾기”, key-space coercion 또는 default stage로 보완하지 않는다.

Phase 05/07은 customer/catalog/registry를 재조회하지 않고 다음처럼 Phase 03 kernel을 소비한다.

```java
RouteEvaluationResult result = kernel.evaluate(
    new RouteEvaluationRequest(
        problem,
        preparedTravel,
        route,
        boundProfile.propagationDeclaration(),
        boundProfile.evaluationPlan()
    )
);
```

`BoundProfile`은 Phase 03 request에 새 parameter를 추가하지 않는다. `EvaluationPlan` 안의 comparator가 유일한 evaluation comparison authority다. Proposed `SolvePlan`은 exact stage order, objective key, approved reusable operator-capability key/version, budget-reference key/version과 guard declaration만 보존한다. Descriptor가 구현 class/callback을 지정할 수 없고 Phase 04는 insertion/portfolio/operator implementation, numeric run budget, seed, step/round/watchdog 값과 runtime state를 포함하지 않는다.

### 7.5 Dependency direction

```text
rpdptw-core evaluation.api/runtime (Phase 03 contract + provider-neutral profile data/SPI)
          ↑                         ↑
rpdptw-capabilities       rpdptw-profile-catalog
          ↑                         ↑
          └──── distribution composition root ────┘

rpdptw-solver (Phase 05+)       → rpdptw-core BoundProfile/Phase03 API
rpdptw-verification (Phase 07) → rpdptw-core cache-free contract
```

Runtime assembly:

```text
distribution
→ explicit CapabilityRegistrySnapshot
→ exact ProfileCatalog (core-owned verified data contract output)
→ CatalogProfileProvider + pure ProfileBindingEngine
→ BoundProfile
→ solver/verifier
```

금지 방향:

- `rpdptw-core → rpdptw-capabilities` 또는 profile catalog
- Capabilities/profile catalog → solver/search/cache/application/provider SDK
- Application generic module → concrete capability/profile implementation
- Phase 05/07 → catalog/customer lookup, descriptor parsing 또는 classpath scan
- Profile/capability → adapter/storage/workflow/compute/vendor types

## 8. Binding algorithm, combination gate와 pseudocode

### 8.1 Catalog resolution

```text
function resolveProfile(accessContext, requestedIdentity, optionalPreset):
    reject identity aliases, ranges and literal "latest"
    preAuthorize accessContext for requested tenant/customer/profile/version scope
        or reject safe ACCESS_DENIED without catalog read
    descriptor = tenantScopedCatalog.findExact(requested customer/profile/version)
        or reject PROFILE_NOT_FOUND
    verify schema + canonical content fingerprint
        or reject CATALOG_INTEGRITY

    if optionalPreset present:
        selectedPreset = exact match or reject PRESET_NOT_FOUND
    else:
        selectedPreset = descriptor.exactDefaultPreset
            or reject DEFAULT_PRESET_NOT_DECLARED

    authorize accessContext for exact verified descriptor and selected preset
        or reject safe ACCESS_DENIED

    return AuthorizedProfileSelection(
        VerifiedProfileDefinition,
        ResolvedPresetDefinition,
        ProfileAuthorizationDecision
    )
```

Catalog lookup은 I/O를 포함할 수 있으므로 pure binder 밖이다. Requested tenant/customer scope authorization은 catalog read보다 먼저 일어나야 하며 denial은 profile/preset 존재, schema 지원 여부나 integrity 상태를 외부에 드러내지 않는다. 사전 scope authorization을 통과한 뒤에도 verified descriptor와 selected preset에 대한 exact authorization을 다시 확인한다. Catalog provider가 verified immutable input을 만든 뒤 binder는 외부 상태를 읽지 않는다.

### 8.2 Pure binding

```text
function bind(request):
    require request.authorization covers exact descriptor/preset
    require problem.preparedTravelFingerprint == travel.fingerprint
    require phase03 signature/units == accepted handoff identity

    validate descriptor and preset key uniqueness
    resolve every capability by exact key/version from explicit registry
    reject duplicate registry identity or same identity/different fingerprint
    decode parameters with provider-owned typed contract
    validate required/range/unit/value type without coercion

    graph = capability/fact/metric/facet dependencies
    reject missing node, duplicate output, cycle or ambiguous provider
    stableClosure = topological order by declared stable key after dependencies

    validate coreRequiredRulesCannotBeDisabled()
    validateCompatibilityResourceServiceTripPolicyCombination()
    validateMandatoryAndOwnershipObjectiveOrder()
    validateFacetApprovalAndVerifierContract()

    propagationDeclaration =
        materializeExistingPhase03PropagationDeclaration(stableClosure)
    evaluationPlan =
        materializeExistingPhase03EvaluationPlan(stableClosure)
    validateWithPhase03EvaluationContractValidator()

    snapshot = immutableCanonicalBoundProfileSnapshot(...)
    runtime = immutableBoundCapabilitySet(...)
    require snapshot.semanticFingerprint == runtime.semanticFingerprint
    return BoundProfile(snapshot, runtime, propagationDeclaration, evaluationPlan)
```

Stable dependency order는 descriptor array order나 registry insertion order로 정하지 않는다. Dependency graph가 순서를 요구하는 edge를 먼저 보존하고, 독립 node만 stable exact key/version으로 정렬한다. Duplicate output을 first-wins/last-wins로 해소하지 않는다.

### 8.3 Compatibility/resource/service/trip/policy gate

Binder는 Phase 02 facts를 재계산하지 않고 profile이 그 facts와 **모순 없이** Phase 03 contract를 materialize할 수 있는지만 검사한다.

| 조합 | Expected binding | 이유/금지 fallback |
|---|---|---|
| Delivery-only + real pair mixed, supported single-trip | ALLOW | 원문이 허용한 core service meaning; profile이 임의 분리 금지 |
| `oneway` + capability가 final depot arc/fact를 필수 요구 | REJECT `TRIP_CONTRACT_UNSUPPORTED` | Return arc 합성 금지 |
| Single `roundtrip` + exact terminal-compatible component | ALLOW | Phase 02/03 terminal authority 재사용 |
| Rotation-required component 또는 corrupt non-oneway rotation problem | REJECT | Multi-trip/rotation deferred; profile로 활성화 금지 |
| Explicit `START_ONLY` | ALLOW and materialize exact enum | Runtime default 추정 금지 |
| Explicit `COMPLETE_WITHIN_WINDOW` | ALLOW and materialize exact enum | Phase 03 algorithm 변경 없이 선택 |
| Service-window rule omitted after canonical descriptor verification | REJECT unless approved schema already materialized explicit source default | Binder hidden `START_ONLY` 금지 |
| Normalized stop/drive/resource limit present + profile attempts disable | REJECT `CORE_RULE_OVERRIDE` | Core hard rule를 policy로 해제 금지 |
| Resource limit absent + component requires measured bound/reference | REJECT missing dependency | `Long.MAX_VALUE`/0/default 금지 |
| Some request has eligible vehicle set 0 only because valid size/qualification/zone facts | ALLOW binding | Static `PROVEN` 가능성이지 profile binding defect가 아님 |
| Profile tries to reinterpret size code ordering or merge vehicle qualification with executable capability | REJECT contract violation | Phase 01/02 compatibility authority 유지 |
| `LEASE` vehicle present + preset lacks supported outsourced vehicle-volume dimension | REJECT `LEASE_OBJECTIVE_REQUIRED` | 다른 objective/customer fallback 금지 |
| `DIRECT` only + outsourced dimension absent | ALLOW | Optional dimension을 hidden 추가하지 않음 |
| Mandatory semantics used + dimension absent/not first | REJECT `MANDATORY_OBJECTIVE_ORDER` | Hard assignment/Big-M로 대체 금지 |
| Approved facet exact contract/version + verifier recomputation contract | ALLOW | Full identity에 포함 |
| Facet key/version unknown/unapproved/verifier contract missing | REJECT `FACET_NOT_APPROVED` | Nullable map/script/ignored facet 금지 |

Profile policy는 normalized business fact와 capability implementation availability의 교집합에서만 bind된다. 문제를 “맞추기” 위해 ownership, service pattern, trip, resource, compatibility 또는 prepared travel을 변경하지 않는다.

### 8.4 Bound artifact state and consumer transition

```text
AuthorizedProfileSelection
+ CapabilityRegistrySnapshot
+ ProblemInstance/PreparedTravel
+ accepted Phase03 contract
→ BOUND
→ BoundProfileSnapshot (portable evidence)
  + BoundProfile (executable immutable runtime)
→ Phase 05:
     side-effect-free insertion option마다
     same RouteEvaluationKernel + bound declarations 사용
→ Phase 07:
     bound snapshot/runtime identity를 검증하고
     route evaluation을 cache 없이 independently recompute
```

Phase 05가 consumer라고 해서 Phase 04가 `InsertionOption`, `SearchRequestBank`, candidate cache 또는 portfolio policy를 생성하지 않는다. Phase 04 handoff의 끝은 immutable binding이고, 첫 route/bank mutation은 actual-but-unaccepted Phase 05의 책임이다.

## 9. Independent rejection oracle과 exact test plan

### 9.1 Oracle 독립성

Production catalog reader, registry, dependency validator, capability provider 또는 binding engine이 expected result를 만들면 안 된다.

- `ProfileCombinationFixtureBuilder`는 service pattern, trip, resource, ownership, mandatory, compatibility와 profile policy의 작은 bounded Cartesian product를 stable order로 만든다.
- `ExhaustiveProfileBindingOracle`은 production `ProfileBindingEngine`, provider implementation과 dependency-closure helper를 호출하지 않는다. Literal rule table과 set/type/unit 관계로 `ALLOW` 또는 exact rejection code를 계산한다.
- Hand fixture는 canonical descriptor input과 expected resolved identity, ordered closure, Phase 03 declaration/plan fingerprint material을 literal로 보존한다.
- `CorruptBoundProfileFixtureBuilder`는 정상 public factory를 우회한 test-only encoding으로 descriptor/registry/problem/travel/dependency/plan/fingerprint를 한 필드씩 변조한다.
- Reproducibility expected identity는 production `toString()`/hash order가 아니라 independently canonicalized literal token table과 digest oracle에서 만든다.
- Test-only customer/capability/version/value에는 이름 또는 manifest metadata로 `test-only`를 표시하고 production catalog resource path에 넣지 않는다.

### 9.2 Combination oracle fixture

`ProfileCombinationFixtureBuilder.boundedCompatibilityMatrix()`는 다음 dimension을 작은 test-only set으로 전수 조합한다.

```text
servicePattern:
  DELIVERY_ONLY | REAL_PICKUP_DELIVERY | MIXED

trip:
  ONEWAY | SINGLE_ROUNDTRIP | CORRUPT_ROTATION_REQUIRED

resource:
  ABSENT | STOP_LIMIT | DRIVE_LIMITS | STOP_AND_DRIVE_LIMITS

fleetOwnership:
  DIRECT_ONLY | DIRECT_AND_LEASE

mandatoryFacts:
  NONE | PRESENT

staticCompatibility:
  ALL_HAVE_ELIGIBLE_VEHICLE
  | SOME_ZERO_ELIGIBLE_BUT_VALID_FACTS
  | PROFILE_ATTEMPTS_TO_REINTERPRET_SIZE_OR_QUALIFICATION

servicePolicy:
  EXPLICIT_START_ONLY
  | EXPLICIT_COMPLETE_WITHIN_WINDOW
  | OMITTED_WITHOUT_SCHEMA_MATERIALIZATION

profilePolicy:
  VALID_BASELINE
  | DISABLE_CORE_RESOURCE
  | FINAL_DEPOT_FACT_REQUIRED
  | MANDATORY_DIMENSION_MISSING
  | MANDATORY_DIMENSION_NOT_FIRST
  | LEASE_DIMENSION_MISSING
  | MISSING_FACT_OR_METRIC
  | UNAPPROVED_FACET
```

Bounded generator는 invalid 조합도 생성한다. Oracle은 앞선 authority/integrity failure를 stable precedence table로 선택한다. Production validator의 iteration order에 따라 “처음 발견한 오류”가 달라지면 안 된다.

Oracle precedence:

```text
AUTHORITY/CATALOG INTEGRITY
→ AUTHORIZATION/IDENTITY
→ REGISTRY IDENTITY
→ PARAMETER/DEPENDENCY/UNIT CONTRACT
→ CORE RULE OVERRIDE OR UNSUPPORTED TRIP/FACET
→ MANDATORY/LEASE OBJECTIVE POLICY
→ ALLOW
```

`SOME_ZERO_ELIGIBLE_BUT_VALID_FACTS`는 단독으로 reject하지 않는다. 반대로 profile이 size 문자열의 숫자 순서를 추론하거나 vehicle qualification을 executable capability registry로 재해석하려 하면 contract violation이다. 이 차이를 oracle이 별도 expected row로 고정한다.

### 9.3 Exact test class/method matrix

모든 test는 미래 test이며 현재 파일/실행 evidence가 아니다.

| Class | Exact method | Builder/oracle | Red 원인과 green 판정 |
|---|---|---|---|
| `ProfileIdentityResolutionTest` | `resolvesOmittedPresetOnlyFromExactProfileVersionDefault()` | `TestOnlyProfileDescriptorBuilder.withDeclaredDefault()` | Runtime/global default를 사용하면 실패; exact descriptor default와 provenance |
|  | `rejectsLatestVersionRangeSimilarityAndUnknownPreset()` | invalid reference table | 하나라도 다른 identity로 resolve되면 실패; exact typed code |
|  | `rejectsCrossCustomerPresetEvenWhenKeysMatch()` | two-customer same-key fixture | Cross-customer fallback/authorization 우회 0 |
|  | `rejectsSameIdentityWithDifferentCanonicalContent()` | one-field descriptor drift | Catalog startup/resolve가 `CATALOG_IDENTITY_COLLISION` |
|  | `rejectsSameLookupCoordinateWithDifferentSchemaContentOrDefault()` | same customer/profile/version, one-field schema/content/default variants | Parallel ambiguity/overwrite 0; 새 `ProfileVersion` 없이는 reject |
|  | `rejectsUnsupportedSchemaWithoutCrossVersionFallback()` | unsupported schema + supported neighbor version | Exact typed reject; older/newer profile lookup 0 |
|  | `canonicalFieldOrderDoesNotChangeDescriptorFingerprint()` | semantically equal reordered documents | Same canonical fingerprint; unknown/duplicate field 정책은 schema대로 exact |
| `ProfileAuthorizationSecurityTest` | `rejectsUnauthorizedCustomerBeforeDescriptorReadAndDoesNotRevealExistenceOrIntegrity()` | deny policy + catalog read-count spy + absent/corrupt/existing variants | Catalog read 0; outward code/body가 existence/schema/integrity에 따라 달라지지 않음 |
|  | `requiresExactDescriptorAndPresetAuthorizationAfterVerification()` | authorized scope + denied exact preset fixture | 다른 preset/customer fallback 0; safe `ACCESS_DENIED` |
| `CapabilityRegistryContractTest` | `resolvesOnlyExactCapabilityKeyVersion()` | explicit provider inventory | Version range/latest fallback 0 |
|  | `rejectsDuplicateIdentityBeforeAnyProfileBinding()` | two providers, same key/version | First/last wins 금지; registry construction failure |
|  | `rejectsSameContractIdentityWithDifferentImplementationFingerprint()` | drifted provider pair | Startup/build failure; arbitrary implementation selection 금지 |
|  | `registryInsertionOrderDoesNotChangeSnapshotFingerprint()` | all provider permutations | Stable snapshot bytes/fingerprint exact equality |
| `CapabilityImplementationContractTest` | `bindsOnlyDeclaredTypedParameterRecord()` | typed parameter provider | Raw map/coercion/class-name instantiation 실패 |
|  | `rejectsParameterRangeUnitAndValueTypeMismatch()` | boundary table | Exact typed failure; clamp/default/convert 금지 |
|  | `doesNotRetainEvaluationScratchClockRandomOrMutableRegistry()` | state-leak probe | Bound capability field graph에 mutable/runtime state 0 |
| `ProfileBinderContractTest` | `rejectsMissingDuplicateCyclicAndAmbiguousDependencies()` | dependency graph builder + independent topological oracle | 각 defect exact code; first/last wins 없음 |
|  | `materializesStableDependencyClosureIndependentOfDescriptorOrder()` | all independent-node permutations | Ordered closure and fingerprint exact equality |
|  | `rejectsMetricFactUnitOutputAndDirectionMismatchBeforeKernelCall()` | invocation-count kernel spy | Kernel/component bind invocation after failure 0 |
|  | `rejectsMissingObjectiveOperatorBudgetAndGuardReferencesInSolvePlan()` | one-field `SolvePlan` reference corruption table | Missing/latest/default/coercion 없이 pre-solve typed reject |
|  | `rejectsEvaluationCapabilityOperatorAndBudgetKeySpaceCollision()` | equal literal keys across distinct typed inventories | Cross-namespace coercion/registry lookup 0; exact typed inventory required |
|  | `rejectsProblemTravelAndPhase03ContractFingerprintMismatch()` | one-field authority corruption | Materialization 전 `AUTHORITY_MISMATCH` |
|  | `materializesApprovedSchemaDefaultAsExplicitValueWithProvenance()` | schema-default test-only fixture | Bound snapshot에 explicit `START_ONLY`와 schema/version source; binder-only hidden default 0 |
| `ProfileCombinationOracleTest` | `matchesBoundedCompatibilityResourceServiceTripAndPolicyMatrix()` | §9.2 builder + `ExhaustiveProfileBindingOracle` | 모든 generated ordinal의 variant/code exact equality, counterexample 0 |
|  | `allowsZeroEligibleVehicleFactWithoutTreatingItAsBindingDefect()` | static precheck fixture | Bind succeeds; no route/vehicle invention |
|  | `rejectsProfileAttemptToDisableCanonicalCompatibilityOrResourceRule()` | core-rule override fixture | `CORE_RULE_OVERRIDE`; finite penalty/ignored flag 금지 |
|  | `rejectsOnewayFinalDepotAndRotationRequiredCapabilities()` | trip matrix | Return arc/rotation 합성 없이 typed reject |
|  | `acceptsMixedServicePatternsWithBothExplicitWindowPolicies()` | mixed service hand fixtures | Both policies bind to exact Phase 03 enum, service meaning unchanged |
| `ProfileObjectivePolicyTest` | `requiresMandatoryUnassignedAsFirstLexicographicDimensionWhenUsed()` | mandatory order table | Missing/non-first/Big-M form reject; valid first dimension bind |
|  | `requiresOutsourcedVehicleVolumeDimensionWhenLeaseFleetIsPresent()` | DIRECT/LEASE × objective table | LEASE+missing reject; DIRECT-only valid |
|  | `keepsMandatoryAndLeaseDimensionsOutWhenNotDeclaredAndNotRequired()` | base profile fixture | Hidden objective insertion 0 |
|  | `rejectsDuplicateObjectiveAndMetricKeys()` | corrupt preset | First/last wins 금지 |
| `BoundProfileLifecycleTest` | `snapshotAndExecutableRuntimeHaveSameSemanticFingerprint()` | valid hand profile | Exact equality and defensive immutability |
|  | `boundProfileCannotBeReusedWithAnotherProblemOrTravel()` | two-problem/two-travel fixture | Evaluation 전에 identity mismatch |
|  | `doesNotReadCatalogRegistryOrAuthorizationAfterBinding()` | fail-after-bind spies | Bind 뒤 lookup/call count 0 |
| `BoundProfileCorruptionTest` | `rejectsDescriptorRegistryImplementationAndAuthorizationDrift()` | one-field corruption builder | 각 source에 distinct typed failure |
|  | `rejectsDependencyClosureAndPhase03PlanFingerprintCorruption()` | corrupt snapshot bytes | Rebind/consume 전에 integrity failure |
|  | `rejectsSameBoundIdentityWithDifferentCanonicalBytes()` | duplicate artifact fixture | Overwrite/last-wins 금지 |
| `BoundProfileIsolationTest` | `sameProblemCanBindTwoAuthorizedProfilesWithoutSharedState()` | test-only profile A/B | Each exact expected plan/fingerprint; scratch/parameters no alias |
|  | `addingUnrelatedProfileDoesNotChangeExistingBoundFingerprint()` | catalog before/after | Existing profile exact bytes/result unchanged |
|  | `customerIdentityIsNotVisibleToCapabilityEvaluation()` | capability spy/architecture probe | Capability receives bound facts/config only |
| `BoundProfileReproducibilityTest` | `sameInputsProduceSameClosureDeclarationsAndFingerprintAcrossRepeatedParallelBinding()` | fixed fixture, 100 sequential + bounded parallel | Snapshot bytes, closure, Phase 03 declarations/fingerprint exact equality |
|  | `catalogBackendAndProviderRegistrationOrderDoNotAffectBinding()` | memory/resource catalog + inventory permutations | Same canonical bound identity |
| `BoundProfilePhase03ContractTest` | `materializesOnlyExistingPhase03PropagationAndEvaluationContracts()` | accepted signature fixture | Phase 03 signature diff 0; wrapper-only integration |
|  | `boundPlanProducesSameKernelArtifactAsExplicitPhase03Declaration()` | Phase 03 hand fixture | Direct explicit plan vs bound plan route artifact exact equality |
|  | `doesNotClaimRouteArtifactAggregationAsFullSolutionAuthority()` | API/field graph + Phase 05 consumer probe | Phase 04 solution aggregator/candidate ranking implementation 0; cross-phase gate remains explicit |
|  | `carriesTypedSolvePlanWithoutInsertionPortfolioOrRuntimeState()` | field-graph + dependency rule | Typed refs/fingerprint exact; numeric run budget, seed, bank, option, candidate, mutable counter 0 |
|  | `phase05ConsumerNeedsNoCustomerCatalogRegistryOrDescriptorParser()` | compile dependency rule/test consumer | Consumer imports core bound/evaluation API only |
| `Phase04OracleSensitivityTest` | `detectsLatestFallbackFirstWinsHiddenDefaultAndWrongPrecedenceFaults()` | four deliberately faulty test doubles + literal minimal counterexamples | 각 fault를 해당 exact ordinal/code mismatch로 검출; production helper reuse 0 |
|  | `detectsAuthorizationAfterLookupAndCrossNamespaceResolutionFaults()` | read-before-auth catalog + key-coercing resolver doubles | Read count/error-shape 또는 expected typed code mismatch로 둘 다 red |
| `ProfileBindingSecurityObservabilityTest` | `emitsOnlySafeStableFieldsForSuccessAndFailure()` | capture sink + success/denial/integrity/corruption fixtures | Stable code/stage/opaque identity/fingerprint/ordinal만 허용 |
|  | `redactsDescriptorParametersCustomerPiiSecretsLocatorsClasspathAndStackInputs()` | sentinel-bearing fixture + recursive captured payload scan | Sentinel occurrence 0; telemetry/log field가 semantic fingerprint에 미포함 |
| `FacetBindingGateTest` | `rejectsFacetWithoutApprovedContractAndVerifierRecomputationVersion()` | unapproved facet fixture | `FACET_NOT_APPROVED`, ignored/null facet 금지 |
|  | `emptyFacetSetPreservesBaseProfilePlanAndFingerprintRules()` | no-facet baseline | Base profile works without synthetic provider |
|  | `approvedTestOnlyFacetRecomputesFromFactsWhenAdrIsAvailable()` | conditional ADR-gated fixture | ADR가 없으면 test not applicable로 manifest에 사유; skip을 green으로 숨기지 않음 |
| `Phase04ProfileArchitectureTest` | `coreDoesNotDependOnCapabilitiesProfilesCustomerProviderOrSolver()` | module/package/bytecode rules | Forbidden edge/reference 0 |
|  | `capabilitiesAndProfilesDoNotDependOnSearchApplicationCloudOrVendor()` | dependency graph | Forbidden edge/reference 0 |
|  | `profileTestFixturesDoNotReverseThePhase00ProductionDependencyDag()` | reactor test-scope graph/classifier inspection | profile-local test helpers만 허용; generic fixture→profile production edge 0 |
|  | `hasNoCustomerNameBranchDynamicScriptReflectionScanOrRawParameterMap()` | bytecode/source-structure rule | 금지 pattern/reference 0 |
|  | `applicationDoesNotCompileDependOnConcreteCapabilityOrProfileCatalog()` | reactor graph | Distribution assembly만 concrete dependency 보유 |

Property/exhaustive test는 stable ordinal을 기록하고 최초 최소 counterexample의 full dimension tuple, descriptor/registry/problem fingerprints와 expected/actual code를 evidence에 넣는다. 별도 property framework를 채택하면 Phase 00 dependency review와 deterministic seed/shrink record가 필요하다.

Phase 03/05 cross-phase blocker가 해제될 때에는 별도 `FullSolutionEvaluationHandoffContractTest`가 exact problem/travel/profile + ordered routes + bank 입력, route artifact reuse/invalidation, aggregation/failure/fingerprint와 full recomputation equality/corruption을 검증해야 한다. `ComparatorTieBoundaryContractTest`는 business-equality 관찰, solution stable tie와 insertion-context tie의 분리, non-tied 우선, transitivity/antisymmetry와 order permutation을 검증해야 한다. 승인된 API가 없는 현재 이 이름들은 **restart 조건**이지 생성·실행된 test나 evidence가 아니다.

### 9.4 Red → green 순서

**Future red test expectation:** Production type/logic보다 test를 먼저 추가한다. 아직 module/type이 없어서 compile red이거나 의도적으로 결함이 있는 minimal implementation에서 표의 원인으로 실제 실패해야 한다.

| 순서 | Red를 먼저 고정할 test | 예상 red | Green 조건 |
|---:|---|---|---|
| 1 | Identity/catalog/authorization | Exact types/resolver 부재 또는 latest/cross-customer가 통과 | Exact identity/default/authorization과 drift rejection |
| 2 | Explicit registry/typed parameters | Duplicate first-wins, raw map/coercion 또는 order drift | Exact provider, typed config, stable registry fingerprint |
| 3 | Dependency/unit/objective closure | Missing/cycle/unit/direction이 kernel까지 진행 | 모든 contract defect pre-kernel reject |
| 4 | §9.2 combination oracle | Core-rule override/LEASE/mandatory/trip 조합 mismatch | 모든 bounded ordinal expected result exact |
| 5 | Phase 03 materialization equivalence | Binder가 kernel contract를 복제/변경하거나 artifact 불일치 | Existing declarations만 사용하고 artifact exact equality |
| 6 | Oracle sensitivity/security/observability | Faulty fallback/first-wins/default/precedence/auth-order/key-space double을 oracle이 놓치거나 sentinel이 노출됨 | 모든 seeded fault red, pre-read denial, forbidden field/sentinel 0 |
| 7 | Isolation/drift/corruption/facet gate | Shared parameter/scratch, stale fingerprint, unknown facet 허용 | No alias, one-field corruption reject, empty/approved facet rule |
| 8 | Reproducibility/architecture/consumer | Classpath/order/parallel 차이 또는 forbidden dependency | Canonical equality, forbidden edge 0, Phase 05 core-only consumer |

Required test를 skip/disable하거나 production helper로 oracle expected를 만들어 green으로 만들면 evidence가 아니다. `ADR-004`가 없어서 approved-facet positive test가 적용 불가하면 evidence manifest에 `NOT_APPLICABLE_NO_APPROVED_FACET`와 empty/unapproved gate test 결과를 기록한다. Required identity/binding/isolation/architecture test는 skip할 수 없다.

## 10. Ordered work packages

각 package는 앞 package의 green/evidence를 선행조건으로 한다. Command는 Phase 00 reactor가 존재한 뒤 실행할 **future exact command**다. Phase 00 review에서 module selector가 바뀌면 같은 의미의 command를 상세/review와 함께 갱신한다.

### WP-04.0 — Entry, Phase 03 contract와 ADR freeze

- **Prerequisite:** Phase 00~03 accepted bundle/digest, owner/task 지정.
- **Change target:** Phase 04 detailed/review input, `ADR-003`, 필요 시 `ADR-004`; source code 없음.
- **Concrete work:** Source/Phase 03 API fingerprint 대조, descriptor/catalog/registry 선택지와 canonical encoding review, production profile authority 부재 기록, §7 semantic contract와 failure precedence 승인.
- **Verification command:**

  ```bash
  git diff --check -- \
    docs/implementation/phases/phase-04-capabilities-customer-profiles.md
  ```

- **Tests/checks:** Source SHA-256, actual Phase 03/05 link와 review/acceptance/evidence status truth, required metadata/heading/owner/prerequisite, proposed/open/test-only marker 검사.
- **Expected:** Source drift 0, Phase 03 unchanged contract와 production descriptor 0건을 명시하고 ADR/review verdict가 존재.
- **Failure/rollback:** Authority/API drift 또는 ADR 미승인이면 source 구현 금지. 마지막 reviewed document/oracle 설계로 돌아가 source fingerprint부터 재개.
- **Handoff:** Approved identity/failure/registry/descriptor contract와 red-test list를 WP-04.1에 전달.

### WP-04.1 — Exact identity, immutable descriptor와 catalog integrity

- **Prerequisite:** WP-04.0 contract/`ADR-003` approved.
- **Change target:** `rpdptw/profile-catalog` identity/descriptor/catalog/authorization types, schema와 identity tests.
- **Concrete work:** Exact coordinate types, schema/canonical reader, content fingerprint, same-identity collision, declared default preset, cross-customer authorization와 typed resolution failure 구현. Production customer descriptor는 추가하지 않는다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/profile-catalog/pom.xml \
    -Dtest=ProfileIdentityResolutionTest,ProfileAuthorizationSecurityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** §9.3 identity methods 모두 green; latest/range/similarity/cross-customer/unknown/default-missing/content-drift가 exact reject.
- **Failure/rollback:** Hidden default/fallback, provider locator, arbitrary class/script 또는 same identity overwrite가 필요하면 중단. WP source를 제거하고 WP-04.0 schema decision을 재검토.
- **Handoff:** Core-owned `VerifiedProfileDefinition`, `ResolvedPresetDefinition`, `ProfileAuthorizationDecision`을 묶은 `AuthorizedProfileSelection`을 WP-04.2에 전달.

### WP-04.2 — Explicit capability registry와 typed provider contract

- **Prerequisite:** WP-04.1 green, Phase 03 accepted SPI/type/unit list.
- **Change target:** Core profile API extension, `rpdptw-capabilities` registry/provider contracts와 tests.
- **Concrete work:** Exact capability identity, typed parameters/dependencies/compatibility requirements, explicit inventory, duplicate/implementation drift rejection과 stable registry snapshot 구현. 승인된 reusable baseline component만 추가하고 고객 수치/가짜 production provider는 만들지 않는다.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/capabilities/pom.xml \
    -Dtest=CapabilityRegistryContractTest,CapabilityImplementationContractTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/core/pom.xml \
    -Dtest=RouteEvaluationLayerTest,RouteEvaluationContractCorruptionTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Exact registry/typed parameter tests green이고 Phase 03 regression unchanged; provider order permutation fingerprint 동일.
- **Failure/rollback:** Phase 03 signature를 바꾸거나 customer/search/cloud dependency가 필요하면 중단. Capability implementation만 제거하고 accepted Phase 03 API를 last safe point로 유지.
- **Handoff:** Immutable `CapabilityRegistrySnapshot`과 typed provider contracts를 WP-04.3에 전달.

### WP-04.3 — Pure binder, dependency closure와 combination oracle

- **Prerequisite:** WP-04.1/2 green, exact Problem/Travel fixture와 accepted Phase 03 contract.
- **Change target:** Core `evaluation.runtime.profile`, catalog provider orchestration, binder/combination/objective tests.
- **Concrete work:** Pure binding engine, stable dependency closure, unit/type/range/direction validation, problem/travel/Phase03 identity, explicit service rule, core-rule non-disable, LEASE/mandatory objective와 §9.2 exhaustive oracle 구현.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/profile-catalog/pom.xml \
    -Dtest=ProfileBinderContractTest,ProfileCombinationOracleTest,ProfileObjectivePolicyTest,Phase04OracleSensitivityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** 모든 matrix ordinal exact oracle equality; pre-kernel failure 뒤 component/kernel invocation 0; valid rows는 stable `BoundProfile`.
- **Failure/rollback:** Core fact 재계산, default/fallback, finite penalty/Big-M, trip/return 합성 또는 zero-eligible request를 binding defect로 만들면 package 미완료. Binder code를 revert하고 WP-04.2 registry snapshot으로 복귀.
- **Handoff:** Validated closure, `BoundProfileSnapshot`/runtime candidate와 combination report를 WP-04.4에 전달.

### WP-04.4 — Lifecycle, Phase 03 equivalence, isolation, corruption와 facet gate

- **Prerequisite:** WP-04.3 exact binder green.
- **Change target:** Bound artifact lifecycle/fingerprint, corruption/isolation/reproducibility/Phase03/facet tests.
- **Concrete work:** Snapshot/runtime equality, defensive immutability, post-bind no lookup, one-field drift/corruption rejection, same problem multi-profile isolation, repeated/parallel binding, Phase 03 direct-vs-bound artifact equivalence와 facet gate 구현.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/profile-catalog/pom.xml \
    -Dtest=BoundProfileLifecycleTest,BoundProfileCorruptionTest,BoundProfileIsolationTest,BoundProfileReproducibilityTest,BoundProfilePhase03ContractTest,ProfileBindingSecurityObservabilityTest,FacetBindingGateTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Full canonical identity exact equality, lookup after bind 0, corruption 허용 0, Phase 03 artifact exact equality, base empty-facet green.
- **Failure/rollback:** Runtime object가 mutable scratch/catalog handle을 보유하거나 Phase 03 output이 다르면 evidence 폐기. Lifecycle/facet optimization을 제거하고 WP-04.3 pure full-binding path를 last safe point로 유지.
- **Handoff:** `E-P04-BINDING/ISOLATION/FACET` 후보 report와 immutable bound artifact를 WP-04.5에 전달.

### WP-04.5 — Architecture, evidence, independent review와 Phase 05/07 handoff

- **Prerequisite:** WP-04.1~4 required tests green, skipped required test 0.
- **Change target:** Architecture rules, evidence manifest와 Phase 04 review input. 공용 status/progress는 scheduler만 변경.
- **Concrete work:** Forbidden dependency/customer/script/reflection/raw-map 검사, module/root verify, exact command/toolchain/test count/digest/limitation, production catalog empty 상태, rollback과 downstream compile contract 기록.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/architecture-rules -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/core,rpdptw/capabilities,rpdptw/profile-catalog -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never clean verify
  ```

- **Expected:** OR-Tools-free ALNS-only root build green; required failed/error/skipped 0; forbidden edge/customer branch/dynamic execution 0; digest-protected bundle과 independent review `PASS`.
- **Failure/rollback:** Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`/production profile/handoff authority를 주장하지 않는다. Last accepted Phase 03 artifact를 유지한다.
- **Handoff:** §14의 actual-but-unaccepted Phase 05와 Phase 07 consumer에게 exact artifact/API/evidence identity를 전달한다.

## 11. Verification command, evidence와 판정

### 11.1 Layer별 command

| Layer | Future command | 통과 판정 |
|---|---|---|
| Identity/catalog | WP-04.1 selected test command | Exact method green; fallback/collision/cross-customer 허용 0 |
| Registry/typed provider | WP-04.2 selected command | Duplicate/drift/order/parameter mismatch expected reject |
| Binding/oracle/objective | WP-04.3 selected command | 모든 matrix ordinal exact result; counterexample 0; kernel call-after-reject 0 |
| Lifecycle/isolation/corruption | WP-04.4 selected command | Snapshot/runtime equality, one-field corruption reject, no shared state |
| Phase 03 compatibility | WP-04.2/4 regression/equivalence | Accepted signature unchanged, direct-vs-bound artifact bytes identical |
| Reproducibility | `BoundProfileReproducibilityTest` | Repeated/parallel/backend/order permutations의 closure/declaration/fingerprint exact equality |
| Architecture | WP-04.5 architecture command | Forbidden module/import/bytecode/customer/script/reflection/raw-map reference 0 |
| Modules | `./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core,rpdptw/capabilities,rpdptw/profile-catalog -am clean verify` | Selected modules와 required upstream green |
| Reactor | `./mvnw -B -ntp -Dstyle.color=never clean verify` | OR-Tools-free ALNS-only full reactor green, unrelated required module skip 없음 |

Selected command는 module POM을 `-f`로 직접 실행하고 `-Dsurefire.failIfNoSpecifiedTests=true`로 fail-closed한다. 각 command 직전 해당 module report directory를 `clean`하고, 실행 뒤 §9.3의 exact class/method 이름을 fresh Surefire XML에서 manifest로 대조한다. Missing report/method, duplicate result, failed/error/skipped required test는 command exit가 0이어도 evidence failure다. `-DskipTests`, `-Dmaven.test.skip=true`, required test disable, stale `target/`, console summary 한 줄, 이전 run 혼합 또는 `ADR-004` 부재를 모든 facet test skip 근거로 쓰는 것은 exit evidence가 아니다.

### 11.2 Planned evidence

| Key | 반드시 포함할 내용 | 현재 상태 |
|---|---|---|
| `E-P04-BINDING` | Source/build/Phase03/problem/travel/descriptor/authorization/registry fingerprints, exact lookup coordinate/default, typed closure, §9.2 oracle + seeded-fault sensitivity, objective/policy rejection, exact command/toolchain/fresh class·method manifest/result | NOT_PRODUCED |
| `E-P04-ISOLATION` | Same problem multi-profile 결과, pre-read cross-customer denial, non-enumerating safe error/redaction scan, unrelated-profile regression, no shared scratch/catalog lookup, corruption/drift와 repeated/parallel canonical equality | NOT_PRODUCED |
| `E-P04-FACET` | `ADR-004` status, empty-facet baseline, unapproved facet rejection; 승인 facet이 있으면 full propagation와 independent verifier recomputation parity | NOT_PRODUCED |

Bundle은 content-addressed immutable artifact 또는 digest-protected local equivalent여야 한다. 구현자가 reviewer/verdict를 자체 합성하거나 이 문서의 expected 표를 실행 report로 사용해서는 안 된다.

### 11.3 판정 우선순위와 failure evidence

Binding success/failure의 audit/telemetry는 다음 정보만 안전하게 남긴다.

```text
stable binding outcome/code/category and stage
opaque tenant/profile lookup coordinate or safe subject
descriptor/registry/problem/travel/contract fingerprints
safe dependency key or redacted location
expected and actual unit/type/version where non-sensitive
oracle fixture ordinal for test evidence
```

Raw descriptor 전체/bytes, parameter value, full customer identity/PII, secret/provider locator, executable classpath, input-derived stack detail을 error payload·log·metric label에 넣지 않는다. Denied caller의 outward error/body/timing class는 requested profile/preset의 존재, schema 지원 여부나 integrity 상태를 구별하지 못하는 safe `ACCESS_DENIED` contract를 따른다. 권한 있는 내부 audit도 위 allowlist와 승인된 opaque correlation만 사용한다. Log/metric/trace ID나 emission order는 semantic fingerprint 입력이 아니다. Multiple defect fixture에서는 §9.2 precedence와 deterministic stable subject order가 같아야 한다.

## 12. Exit gate, Definition of Done과 anti-pattern

### 12.1 Exit gate

다음 AND 조건을 모두 만족해야 독립 reviewer가 Phase 04 `ACCEPTED`를 권고할 수 있다.

- Phase 00~03 accepted artifact/evidence와 exact source/Phase03 contract fingerprint가 확인됨.
- `ADR-003`이 descriptor/catalog/registry/canonical encoding/duplicate 정책을 승인함.
- Exact lookup coordinate/profile schema/version/preset/default/authorization lifecycle이 구현되고 same-coordinate ambiguity, latest/range/similarity/cross-customer fallback이 0임.
- Unauthorized request가 catalog read 전에 종료되고 existence/integrity enumeration과 unsafe log/error field가 0임.
- Explicit registry가 exact capability version/implementation을 제공하며 classpath first-wins/dynamic executable loading이 0임.
- Typed parameter/dependency/fact/metric/unit/output/range/direction closure가 solve 전에 검증됨.
- §9.2 compatibility/resource/service/trip/policy matrix가 independent oracle와 전수 일치함.
- Seeded faulty fallback/first-wins/default/precedence/auth-order/key-space doubles를 independent oracle/security tests가 모두 검출함.
- Mandatory/LEASE objective 규칙이 lexicographic 의미를 보존하고 hard/Big-M/hidden dimension으로 바뀌지 않음.
- Phase 03 API/signature/semantics가 변경되지 않고 direct-vs-bound evaluation artifact가 exact 일치함.
- Canonical `RM-2` full-solution evaluator owner/API/identity/failure/comparator와 reciprocal compile/full-equality/corruption tests가 Phase 03~05 공동 승인됨.
- Business equality, solution stable tie와 insertion-context tie가 분리되고 non-tied priority/transitivity/antisymmetry/order-permutation tests가 통과함.
- Portfolio traversal/`CLOCK`/utilization reference를 profile/`SolvePlan`이 제공한다면 exact coordinate/edge policy와 golden handoff test가 승인됨; 아니면 관련 reference는 empty이고 hidden default가 0임.
- `BoundProfileSnapshot`과 executable runtime이 같은 semantic identity이며 다른 problem/travel에 재사용되지 않음.
- Profile drift/corruption/cross-customer/unrelated-profile 추가를 거부 또는 격리하고 repeated/parallel binding이 재현됨.
- Empty/unapproved/approved-if-applicable facet gate와 verifier recomputation contract가 evidence에 명시됨.
- Core/solver/verifier/application의 customer/capability/provider 역의존과 customer-name branch가 0임.
- Profile-local test helper가 production DAG 또는 Phase 00 generic test-fixtures dependency를 역전하지 않음.
- Root OR-Tools-free ALNS-only `./mvnw -B -ntp -Dstyle.color=never clean verify`, immutable `E-P04-*` bundle과 independent Phase 04 review가 통과함.
- Actual-but-unaccepted Phase 05와 Phase 07 handoff/rollback point가 명시되고 insertion/ALNS/runtime code가 Phase 04에 없음.
- Production 고객 descriptor/수치, OPEN/GATED/deferred/official 값을 발명하거나 hidden default로 넣지 않음.

### 12.2 Definition of Done

Phase 04의 `ACCEPTED`는 다음을 뜻한다.

1. Long-lived descriptor와 solve-bound binding lifecycle이 exact identity/fingerprint로 분리된다.
2. Customer policy는 data이고 executable behavior는 reusable typed capability이며 core physical rule은 어느 쪽도 임의 변경하지 못한다.
3. 모든 dependency와 problem-policy 조합 오류가 kernel/search 전에 typed failure로 거부된다.
4. Phase 03 contract를 복제·변경하지 않고 exact declarations로 materialize한다.
5. Bound artifact는 immutable하고 search/catalog/provider state가 없으며 Phase 05/07이 customer를 모른 채 소비할 수 있다.
6. Isolation, drift/corruption, reproducibility와 architecture evidence가 실제 test로 확인된다.
7. Unauthorized request와 telemetry가 catalog content, customer data나 provider/runtime detail을 노출하지 않는다.
8. Full-solution evaluation과 comparator/tie handoff가 route-only Phase 03/04 authority를 넘지 않는 exact cross-phase contract로 닫힌다.
9. Evidence/review/handoff가 immutable identity로 고정된다.

Generic Phase 04 acceptance는 production 고객 profile의 존재나 활성화를 뜻하지 않는다. Production profile onboarding은 별도 exact descriptor/authorization/Product·Security approval와 regression evidence를 요구한다.

### 12.3 금지 anti-pattern

- Customer별 module/POM/JAR, solver/verifier/worker 복제
- Core/solver/verifier의 `if (customer...)`, preset 문자열 포함 검사
- `latest`, version range, 비슷한 이름, 다른 customer/preset fallback
- 같은 customer/profile/version coordinate 아래 다른 schema/content/default를 병렬 등록하거나 overwrite
- Default preset/service rule/parameter/objective를 binder/runtime에서 몰래 삽입
- Same identity descriptor/capability bytes overwrite, first-wins/last-wins
- Reflection class scan, `ServiceLoader`/classpath order, arbitrary class name/script/expression/DSL 실행
- `Map<String,Object>` parameter와 무검증 string→number/unit coercion
- Profile이 raw route/input, search cache, insertion/ALNS operator 또는 final result를 읽음
- Unauthorized customer/profile request에서 catalog를 먼저 읽거나 not-found/schema/integrity 차이를 외부에 노출
- Vehicle qualification/evaluation capability/algorithm operator/budget key를 같은 registry/namespace로 coercion
- Capability가 customer ID, provider SDK, clock/random/telemetry scratch를 의미 입력으로 읽음
- Pair/capacity/size/capability/zone/resource/trip hard rule을 profile로 disable
- Hard violation, mandatory 또는 strict objective를 finite penalty/Big-M/음수 score로 평탄화
- `LEASE`를 final outcome으로 바꾸거나 fleet 밖 provider를 생성
- `oneway`에 return arc, rotation 또는 depot fact를 profile이 합성
- Zero-compatible-vehicle request를 profile bind failure로 오분류
- Unapproved facet을 nullable map/ignored field로 통과
- `BoundProfile`에 catalog handle, mutable registry, cache, scratch, seed, operator, step budget, provider locator/secret 저장
- Phase 03 interface 변경, Phase 05 insertion/portfolio, Phase 06 ALNS/runtime 또는 Phase 07 verdict를 이 Phase로 당김
- Route artifact 합이나 insertion delta를 승인된 full-solution objective authority로 가장
- Gated route pool/MIP/vendor dependency 또는 AWS 구현을 profile/capability에 선반영

## 13. Blocker, OPEN/GATED/deferred와 restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00~03 accepted evidence 부재 | BLOCKER | Architecture + Domain/Input/Travel/Core | Phase 04 source/test/evidence 착수 | 이 detailed document와 independent oracle 설계 | Accepted review/bundle/digest와 actual reactor/artifacts 전달 |
| Scheduler task/owner 미지정 | BLOCKER | 총괄 scheduler | Authoritative status/review assignment | `TBD_NOT_SUPPLIED` | Exact task ID와 implementer/reviewer 지정 |
| `ADR-003` descriptor/catalog/registry | OPEN/REVIEW REQUIRED | Architecture + Product/API/Data + Security | Identity/API/schema implementation freeze | §7 semantic fields and no-fallback contract | Canonical encoding, schema, registry assembly, duplicate/authorization approval |
| Production customer descriptor/수치 | BLOCKER FOR PRODUCTION ONBOARDING | Product/Profile/Security | 실제 customer profile 등록·기본 preset·운영 활성화 | Empty production catalog + test-only fixtures | Exact approved descriptor/preset/authorization/parameter evidence |
| `ADR-004` typed facet | OPEN/FACET-ONLY BLOCKER | Domain + Capability + Verification | Facet provider implementation | Empty facet set, rejection gate | Physical need, typed contract, Phase03/07 recomputation and regression approval |
| Phase 03 final internal API names | PROPOSED/OPEN until accepted | Core/Evaluation + Architecture | Compile contract freeze | Semantic handoff in §7 | Phase 03 review/API signature fingerprint |
| Full-solution evaluator owner/contract | CROSS-PHASE REVIEW BLOCKER | Core/Evaluation + Capability/Profile + Phase 05 State/Portfolio | Canonical `RM-2`, candidate evaluation/ranking, transition fresh evaluation과 evidence | Phase 03 route kernel + Phase 04 bound declarations + immutable routes/bank; ad hoc aggregation 금지 | Problem/travel/profile + ordered routes + bank API, reuse/invalidation, aggregation/failure/fingerprint/comparator와 reciprocal compile/full-equality/corruption test 승인 |
| Business equality와 context tie boundary | CROSS-PHASE REVIEW BLOCKER | Core/Evaluation + Capability/Profile + Phase 05/06 Algorithm | Comparator API freeze와 deterministic solution/option order | Ordered business vector와 “non-tied objective 우선” 의미 | Equality 관찰 API, solution stable tie와 insertion-context tie owner/order/fingerprint, transitivity/antisymmetry/permutation test 승인 |
| `SolvePlan` final type/name and reference vocabulary (`P-04`) | PROPOSED/OPEN | Product + Algorithm + Capability/Profile + Phase 05/06 owners | Solve-stage declaration compatibility freeze | §7.1/§7.4 typed reference semantics; evaluation/operator/budget key-space 분리; no runtime implementation | Cross-phase contract review, distinct exact key/version inventories와 fingerprint approval |
| Phase 05 portfolio traversal/`CLOCK`/utilization policy input | CROSS-PHASE AUTHORITY BLOCKER | Algorithm + Domain/Input + Capability/Profile + Phase 05 Portfolio | 관련 `SolvePlan`/profile declaration freeze와 portfolio policy activation | Canonical policy role만 보존; missing `CLOCK`은 `UNAVAILABLE`; positive rational test-only utilization fixture; hidden order/coordinate/zero default 금지 | Exact request-target traversal, `CLOCK` axis/orientation reference vectors, typed utilization missing/zero policy와 golden traversal trace 공동 승인 |
| Actual Phase 05 consumer acceptance pending | DOWNSTREAM GATE, NOT A PHASE04 DESIGN BLOCKER | Phase 05 owner | Downstream implementation/compile handoff acceptance | This document + actual Phase 05 proposed consumer contract | Phase 04 accepted artifact/digest 뒤 Phase 05 directional reference·review 갱신 |
| `Q-BENCH-02` official values | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 14 official manifest; Phase 04 generic binding은 안 막음 | No official numeric field in profile | Calibration review and explicit approval |
| Current Win fixture decimal `D/U` | BLOCKER FOR OFFICIAL USE | Input·Matrix + Benchmark | Official fixture/baseline; Phase 04 test-only integer problem은 안 막음 | Generic integer fixtures | Compliant integer matrix or explicit contract/migration approval |
| `C-17` route pool/MIP | GATED TARGET | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13/production default | Bound profile may declare no selector capability | Phase 06/07 baseline, C-17 scope, OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | Optional variant profile/capability 질문·구현 | Current fixed-terminal single-trip contract | Representative fixture, core-impact feasibility, separate approval |
| Multi-trip/rotation | DEFERRED FEATURE | Product·Domain·Algorithm | Trip/facet/profile activation | Oneway + single roundtrip | Exact trip/reset/depot/resource/pair contract and approval |
| Proposed public API/wire schema | OPEN | Product/API/Data | External compatibility promise | Internal proposed types | Versioned contract, compatibility/security review and approval |

`Q-INFRA-01`은 `RESOLVED`지만 Phase 04에 AWS 의존성을 추가하는 근거가 아니다. AWS S3/Step Functions/Lambda는 Phase 11 target/reference이고 profile/capability fingerprints와 evaluation meaning을 정의하지 않는다.

## 14. Previous/next handoff

### 14.1 Previous — actual Phase 03, unaccepted

[Phase 03 — route propagation/evaluation kernel](phase-03-route-propagation-evaluation-kernel.md)에서 다음 accepted artifact를 받아야 한다.

- `HardConstraint`, `MetricContributor`, `ScoreComponent`, `ObjectiveDimension`, `ObjectiveComparator`
- Typed key/version/parameter/unit/fact dependency declarations
- Immutable `PropagationDeclaration`, `EvaluationPlan`과 fingerprints
- Customer-neutral `RouteFacts`, `EvaluationSnapshot`, evaluation failure/contract validator
- Direct hand/oracle/comparator/cache-free evidence `E-P03-*`
- Facet SPI의 accepted 또는 explicitly empty/deferred status

Phase 03은 route-level kernel만 구체화하며 solution-level evaluation owner와 business-equality/context-tie boundary를 cross-phase blocker로 넘긴다. Phase 04는 exact profile/declaration identity를 제공하고 공동 handoff gate에 참여하되 route objective를 합산하거나 comparator/tie API를 임의로 추가하지 않는다. Baseline inspection에서 Phase 03 문서는 actual이지만 `READY_FOR_REVIEW/NOT_STARTED`, predecessor/evidence-blocked이므로 implementation handoff는 아직 성립하지 않는다.

### 14.2 Next — actual Phase 05, unaccepted

[Phase 05 — Pair insertion/initial portfolio](phase-05-pair-insertion-initial-portfolio.md)의 cited sections는 read-only proposed consumer context다. 그 문서의 proposed `BoundInsertionAuthority`는 아래 Phase 04 artifact와 existing Phase 03 kernel/comparator를 조립해 보는 customer-neutral Phase 05 view여야 하며, Phase 04가 insertion interface를 구현하거나 소유한다는 뜻이 아니다. Phase 05는 다음만 소비해야 한다.

- Exact `ProblemInstance`/`PreparedTravel` fingerprints에 결합된 immutable `BoundProfile`
- `BoundProfileSnapshot`/runtime semantic equality와 dependency closure
- Existing Phase 03 `PropagationDeclaration`, `EvaluationPlan`, kernel/comparator
- Stable capability/profile/build identity와 binding evidence
- Typed binding failure는 pre-solve에서 이미 종료됐다는 보장
- Optional typed `SolvePlan`/portfolio policy declarations; insertion option, bank, seed, screen 또는 runtime state는 제외
- Cross-phase 승인 뒤 exact full-solution evaluator가 요구하는 profile/declaration identity와 business-equality/tie separation contract
- 공동 승인 뒤 exact portfolio traversal/`CLOCK` coordinate/utilization edge policy의 typed references; 값을 Phase 04가 발명하거나 traversal을 실행하지 않음

Phase 05는 다음을 다시 수행하면 안 된다.

- Customer/profile/preset authorization, catalog lookup 또는 descriptor parsing
- Capability implementation/version 선택이나 parameter default/coercion
- Compatibility/service/trip/resource policy 재해석
- Phase 03 propagation/evaluation 복제
- Route artifact 합/delta를 full-solution objective로 승격하거나 Phase 04 `PASS`를 candidate ranking으로 재사용

Handoff verification:

```text
Phase05.problemFingerprint
== BoundProfile.problemFingerprint

Phase05.preparedTravelFingerprint
== BoundProfile.preparedTravelFingerprint

Phase05 evaluation input declarations
== BoundProfile materialized Phase03 declarations

catalog/profile/capability concrete module compile dependencies
== 0

full-solution evaluator input
== exact problem/travel/profile + ordered routes + bank

context tie application
== only after observable business objective equality
```

Full-solution/tie blocker 해제 전 Phase 05는 route-only artifacts를 candidate solution authority로 사용하면 안 된다. Phase 05 문서가 존재한다는 사실은 Phase 04 `ACCEPTED` artifact를 downstream에서 실제 소비했다는 evidence가 아니다. Phase 04 acceptance와 directional handoff manifest를 받은 뒤 Phase 05의 현재 “planned Phase 04 absent” entry를 actual handoff로 갱신하고 consumer compile/equivalence test를 통과해야 한다.

Phase 05 review가 남긴 portfolio traversal, `CLOCK` axis/orientation과 utilization missing/zero 정책도 Phase 04 profile 쪽에서 hidden default로 닫지 않는다. `PortfolioPolicyHandoffContractTest`는 승인된 typed coordinate/policy가 생긴 뒤 exact reference vectors, missing `CLOCK → UNAVAILABLE`, zero/missing capacity edge와 golden traversal trace를 검증하는 restart test다. 현재는 생성·실행 evidence가 아니다.

### 14.3 Planned Phase 07 verifier consumer

[planned: Phase 07 — Independent verification/final result](phase-07-independent-verification-final-result.md)는 같은 problem/travel/profile snapshot과 accepted core contracts에서 bound closure/identity를 검증하고 route evaluation을 cache 없이 다시 계산해야 한다. Verification module이 catalog를 재조회하거나 concrete capability/profile module을 compile-depend하지 않는다.

| Consumer | 소비할 것 | 소비하면 안 되는 것 | Handoff verification |
|---|---|---|---|
| Phase 05 Pair/Insertion | Bound profile, pure kernel, comparator와 exact identities | Catalog/customer lookup, provider object, scratch/cache, runtime budgets | Direct-vs-bound evaluation equality, compile dependency rule |
| Phase 07 Candidate verifier | Portable bound snapshot + core-owned executable bound contract, accepted Phase03 contract, problem/travel authority | Catalog 재조회, concrete profile/capability compile dependency, search cache/summary | Snapshot/runtime fingerprint equality, corruption/facet parity와 independent full evaluation |

Verifier는 capability code의 same approved implementation contract를 사용할 수 있지만 Phase 04 binder가 만든 `PASS`나 cached evaluation을 verification verdict로 승격하지 않는다.

## 15. Source → requirement → test → evidence traceability

| Requirement | Source | Phase 04 contract | Exact test | Planned evidence |
|---|---|---|---|---|
| `REQ-PROFILE-IDENTITY` exact customer/profile/version/preset/default | [Master §9.3](../../master-design.md#93-profile-binding과-lifecycle), `Q-OBJ-01` | Exact coordinates, declared default only, no latest/cross fallback | `ProfileIdentityResolutionTest.*` | `E-P04-BINDING` |
| `REQ-CAPABILITY-SEPARATION` vehicle qualification/core rule/reusable code/profile data 분리 | [Master §3.1/§5.3](../../master-design.md#53-vehicle-size와-capability), [Integrated §8.1~§8.3](../../architecture-domain-implementation-design.md#81-customer는-module이-아니라-descriptor) | §3.1 ownership and explicit provider registry | `CapabilityRegistryContractTest.*`, architecture tests | `E-P04-BINDING`, `E-P04-ISOLATION` |
| `REQ-EVAL` hard/metric/score/objective/plan separation | [Master §9](../../master-design.md#9-extensible-policy-evaluation과-profile-architecture), `C-04` | Existing Phase 03 declarations only, typed closure | `ProfileBinderContractTest.*`, Phase03 equivalence | `E-P04-BINDING` |
| `REQ-COMPAT` size/qualification/zone and no reinterpretation | [Domain §5.3](../../2026-07-26-domain-design.md#53-size-capability와-zone), `Q-COMP-01~02` | Core rule non-disable, zero-eligible allowed | Combination oracle exact rows | `E-P04-BINDING` |
| `REQ-SERVICE-TRIP` mixed service, explicit window rule, single-trip | [Master §5.2/§7.3](../../master-design.md#73-planning-period와-time), `Q-TIME-03`, `Q-REQ-01~02` | Explicit Phase 03 enum, oneway/roundtrip gate, rotation reject | Combination service/trip methods | `E-P04-BINDING` |
| `REQ-RESOURCE` normalized stop/drive limits remain hard | [Master §6.2](../../master-design.md#62-route와-feasibility), `Q-IN-02` | Profile cannot disable; missing fact has no sentinel/default | `rejectsProfileAttemptToDisableCanonicalCompatibilityOrResourceRule()` | `E-P04-BINDING` |
| `REQ-OBJECTIVE` mandatory and ownership ordering | [Master §9.3](../../master-design.md#93-profile-binding과-lifecycle), `Q-OBJ-02~03` | Lexicographic mandatory, LEASE dimension contract, no Big-M | `ProfileObjectivePolicyTest.*` | `E-P04-BINDING` |
| `REQ-FINGERPRINT` solve-bound exact closure/version identity | [Master §2.4/§9.3](../../master-design.md#93-profile-binding과-lifecycle), [Integrated §8.2/§8.4](../../architecture-domain-implementation-design.md#82-profile-identity) | Descriptor/registry/problem/travel/Phase03/build lifecycle | Lifecycle/corruption/reproducibility tests | `E-P04-ISOLATION` |
| `REQ-ISOLATION` same problem multi-profile and customer branch 0 | [Master §2.4](../../master-design.md#24-구현-완료의-의미), `C-03` | No shared state/customer visibility; unrelated profile regression | `BoundProfileIsolationTest.*`, architecture tests | `E-P04-ISOLATION` |
| `REQ-FACET` new physical state only via approved typed seam | [Integrated §8.6](../../architecture-domain-implementation-design.md#86-typed-domain-facet), `ADR-004` | Empty/unapproved gate; approved facet needs verifier parity | `FacetBindingGateTest.*` | `E-P04-FACET` |
| `REQ-REPRO` stable binding independent of order/backend/thread | [Master §13.2](../../master-design.md#132-strong-reproducibility-envelope) | Pure binder, stable graph/canonical identity | `BoundProfileReproducibilityTest.*` | `E-P04-ISOLATION` |
| `REQ-SECURITY-OBS` pre-read authorization, non-enumeration과 redaction | [Integrated §20](../../architecture-domain-implementation-design.md#20-security와-tenant-boundary), [Master §2.4](../../master-design.md#24-구현-완료의-의미) | Requested scope preauthorization + exact descriptor/preset authorization; safe allowlist telemetry | `ProfileAuthorizationSecurityTest.*`, `ProfileBindingSecurityObservabilityTest.*` | `E-P04-ISOLATION` |
| `REQ-ARCH-DAG` core/capability/profile/solver/verifier/provider isolation | [Final Architecture §2](../../2026-07-26-architecture-design.md#2-module과-package-경계), [Integrated §3.5~§3.6](../../architecture-domain-implementation-design.md#35-compile-dependency-dag) | §7.5 dependency direction | `Phase04ProfileArchitectureTest.*` | All `E-P04-*` + architecture report |
| `REQ-HANDOFF` bound artifact only; full-solution/tie boundary; no insertion/ALNS/runtime pull | [Realization Plan Phase 04~05](../master-realization-plan.md#phase-04--재사용-기능과-고객-profile), [Actual Phase 03 §14](phase-03-route-propagation-evaluation-kernel.md#14-previousnext-handoff), [Actual Phase 05 §7/§15](phase-05-pair-insertion-initial-portfolio.md#15-previousnext-handoff) | Core-only bound runtime/snapshot, exact profile/declaration identity, separate evaluation/operator key spaces; route artifacts are not full-solution authority | `BoundProfilePhase03ContractTest.*`; blocker 해제 뒤 `FullSolutionEvaluationHandoffContractTest`, `ComparatorTieBoundaryContractTest` | Phase 04 review blocker record + 승인 뒤 Phase 03~05 reciprocal compile/full-equality/corruption result |
| `REQ-PORTFOLIO-POLICY-HANDOFF` traversal/`CLOCK`/utilization policy는 explicit typed authority 뒤 bind | [Phase 05 review F-P05-011](../reviews/phase-05-review.md#f-p05-011--portfolio-traversal과-coordinateutilization-policy는-deterministic-implementation을-freeze하기에-아직-authority가-부족하다) | Hidden input order/coordinate/zero fallback 0; missing `CLOCK`은 `UNAVAILABLE`; Phase 04는 reference만 bind | 승인 뒤 `PortfolioPolicyHandoffContractTest` | Phase 04/05 joint policy manifest + golden traversal/reference-vector result |

새 capability, profile parameter/default, objective dimension, facet 또는 core-rule 조합을 발견하면 source/owner/status/test/evidence를 이 표와 관련 authority/ADR/review에 같은 변경 단위로 연결한다. 구현 편의를 위해 미확정 의미를 production default, customer branch 또는 arbitrary descriptor rule로 채우지 않는다.
