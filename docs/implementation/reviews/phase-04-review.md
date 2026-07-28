# Phase 04 독립 리뷰 — 재사용 기능과 고객 profile

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "04"
phase_name: capabilities-customer-profiles
review_date: 2026-07-28
reviewer_role: independent Phase 04 reviewer
target_document: docs/implementation/phases/phase-04-capabilities-customer-profiles.md
target_document_version_after_safe_fixes: 1.1
target_document_status_after_review: REVIEWED_CHANGES_REQUIRED
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
document_verdict: CHANGES_REQUIRED
implementation_entry_verdict: BLOCKED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
phase_acceptance_status_observed: NOT_ACCEPTED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
finding_counts:
  critical: 0
  high: 2
  medium: 6
  low: 1
  total: 9
finding_disposition:
  applied_safe_obvious: 6
  residual_cross_phase_or_authority_blocker: 3
```

## 1. 결론

[Phase 04 v1.1](../phases/phase-04-capabilities-customer-profiles.md)은 capability code와 customer profile policy, catalog/authorization과 provider inventory, long-lived descriptor와 solve-bound artifact를 대체로 올바르게 분리한다. Canonical physical rule을 profile이 약화하지 못하게 하고 exact version/default/typed dependency/fingerprint를 요구하는 방향도 권위 원문과 정합한다. 리뷰 중 상위 권위와 accepted될 Phase 00 DAG로 답이 명백한 6건은 target 문서에 직접 정정했다.

문서 verdict는 `CHANGES_REQUIRED`다. 다음 세 계약은 Phase 04 문서만 수정해 임의 확정할 수 없다.

1. Canonical `RM-2`의 full-solution evaluation과 Phase 05 candidate ranking 사이 owner/API/input/identity/invalidation/failure/comparator 계약.
2. Business objective equality, solution stable tie와 insertion-context tie의 owner/order/fingerprint 경계.
3. Phase 05 portfolio의 exact request-target traversal, `CLOCK` coordinate convention과 utilization missing/zero policy authority.

Implementation entry도 별개로 `BLOCKED`다. Phase 00~03 accepted artifact/evidence, scheduler task/owner, target reactor/wrapper와 `ADR-003`이 없으며 Phase 04 source/test/evidence도 존재하지 않는다. Document review와 future test 명세는 implementation, evidence 또는 Phase acceptance가 아니다.

## 2. 검토 source와 관찰 baseline

### 2.1 권위 source

| Source | 검토 범위 | 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §2.4, §3~§5, §7, §9, §13, §15~§17 | Customer 격리, capability/profile, exact binding/lifecycle, `RM-2`, reproducibility와 gate |
| [Final Domain Design](../../2026-07-26-domain-design.md) | §3, §5, §7, §9~§10, §15~§21 | Compatibility/service/trip/resource 사실, objective/profile 의미, verifier와 acceptance |
| [Final Architecture Design](../../2026-07-26-architecture-design.md) | §2, §5~§6 | Java 25/Maven module/package DAG, provider/profile 격리와 test evidence |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | §1, §3, §7~§9, §19~§25, §28~§30 | Capability/profile split, descriptor/catalog/binder, security/observability, test와 anti-pattern |
| [Open-question register](../../master-design-open-questions.md) | Exact 28개 `Q-*` 행과 상태 요약 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1` 보존 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§15, 특히 Phase 03~05 | Actual inventory, entry/exit, evidence/DoD, blocker, rollback/restart와 traceability |
| [Implementation README](../README.md) | §1~§7 | 사용자 고정 authority, `REVIEW` 비중단, current/historical/status/link 규칙 |

`REVIEW` metadata는 사용자 고정 source authority 아래 provenance로 보존했으며 리뷰를 중단하지 않았다. `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip/rotation, public descriptor/API schema, official numeric value와 production customer default는 임의로 해소하거나 숨은 기본값으로 넣지 않았다.

### 2.2 인접 Phase와 historical-only source

Read-only로 [Phase 03](../phases/phase-03-route-propagation-evaluation-kernel.md), [Phase 05](../phases/phase-05-pair-insertion-initial-portfolio.md)와 각각의 independent review를 대조했다.

- Phase 03은 route-level propagation/evaluation kernel만 구체화하며 full-solution evaluator와 comparator/tie 경계를 cross-phase blocker로 넘긴다.
- Phase 05는 immutable `BoundProfile`과 Phase 03 route contract를 소비해야 하지만 ordered routes + bank 전체 평가와 portfolio policy 세부가 아직 승인되지 않았다.
- [Phase 05 review](phase-05-review.md)는 같은 full-solution/tie gap과 별도로 traversal/`CLOCK`/utilization authority blocker를 확인한다.
- Phase 03 review와 Phase 05 review의 current verdict는 모두 `CHANGES_REQUIRED`다. 둘 다 implementation entry `BLOCKED`, implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, phase `NOT_ACCEPTED`이며 이 리뷰는 인접 파일을 수정하거나 whole-file reciprocal hash를 추가하지 않았다.

`concurrent review observed`: 공유 checkout에서 인접 review가 동시 변경되는 것을 한 번 관찰했다. 인접 파일을 재해시·갱신하지 않았고, 위 결론은 exact cited section만 사용하며 인접 review 상태나 whole-file digest를 authority/acceptance 조건으로 삼지 않는다.

[2026-07-26 Master — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. Historical [codex Phase 02](../../codex/phases/phase-02-propagation-evaluation-and-profiles.md)는 과거 초안에 `FullEvaluationEngine.evaluateSolution`과 business-quality/tie 분리 개념이 존재했음을 보여 주지만 current API authority나 gap의 자동 해답으로 사용하지 않았다.

### 2.3 Actual Java 25/Maven inventory

| 항목 | 실제 관찰 | Phase 04 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`; `docs/implementation/` 전체가 Git 기준 untracked | 기존 사용자 문서 작업으로 보존; 허용된 두 파일 외 수정 금지 |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Java 25 target과 일치하지만 Phase evidence가 아님 |
| Maven | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT`, release 25, module 목록/wrapper 없음 | Target core/capabilities/profile-catalog reactor 부재 |
| Dependency | Google Workflow/Storage, Jackson, JUnit가 root classpath에 함께 있음 | Stable core/provider/profile 격리 전 legacy placeholder |
| Source/test | Main Java 6개, test Java 1개 | Target `com.ronext.rpdptw` capability/profile/binder source/test 0개 |
| Placeholder | `AlnsBatchEngine`은 `double`, `SplittableRandom`, 합성 objective와 `Map<String,Object>`를 사용 | Typed capability/profile/evaluation evidence로 재사용 금지 |
| Documents | Phase 00~14 상세 15개 actual | Document 존재를 predecessor acceptance나 handoff evidence로 승격하지 않음 |
| Reviews — live final audit | 15/15 완료. `00 PASS_WITH_RESIDUAL_BLOCKERS`; `01 ACCEPTED_WITH_APPLIED_CORRECTIONS`; `02 PASS_AFTER_APPLIED_CORRECTIONS`; `03~12 CHANGES_REQUIRED`; `13 PASS_WITH_RESIDUAL_BLOCKERS`; `14 CHANGES_REQUIRED` | Review verdict를 implementation/evidence/phase acceptance로 승격하지 않으며 adjacent review는 exact cited section만 read-only 소비 |

Target `./mvnw`, `rpdptw/core`, `rpdptw/capabilities`, `rpdptw/profile-catalog`, `build/architecture-rules`와 Phase 04 tests가 없으므로 future Maven command를 실행해 구현 green으로 기록하지 않았다.

## 3. Severity 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 안전/권위/상태를 즉시 잘못 확정하거나 회복 곤란한 corruption을 허용 |
| `HIGH` | Canonical 의미 위반, false-green gate 또는 핵심 dependency/authority 공백으로 구현·acceptance를 막음 |
| `MEDIUM` | Identity/lifecycle/security/oracle/handoff API가 불완전해 격리·재현·결함 탐지를 약화 |
| `LOW` | 구현 의미를 직접 바꾸지 않는 link/status/hash/naming drift |

## 4. Findings

### F-P04-001 — Full-solution evaluation authority가 Phase 03~05 사이에서 비어 있었다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** [Master `RM-2`](../../master-design.md#154-rm-2--propagation-evaluation과-bound-profile)는 full route/solution evaluation을 요구한다. Phase 03 §2.3/§13은 route-level kernel만 승인 가능한 last-safe contract로 제한한다. Target v1.0은 `BoundProfile`이 Phase 03 declarations를 제공한다고 했지만 Phase 05의 `CandidateEvaluationArtifact`가 요구하는 ordered routes + bank aggregation owner/API/identity를 entry/blocker/handoff에 상속하지 않았다. Mandatory unassigned count와 used vehicle/ownership objective는 route artifact 하나로 권위 있게 계산할 수 없다.
- **Correction required:** Core/Evaluation, Capability/Profile, Phase 05 State/Portfolio가 exact problem/travel/profile + ordered routes + bank 입력, route artifact reuse/invalidation, aggregation/failure/fingerprint와 comparator entry point를 공동 승인해야 한다.
- **Applied:** Target [§2~§4, §9.3, §12~§15](../phases/phase-04-capabilities-customer-profiles.md)에 no-ad-hoc-aggregation invariant, entry/exit/blocker, exact restart tests와 Phase 05 handoff를 반영했다.
- **Residual/owner/last safe/restart:** Owner는 Core/Evaluation + Capability/Profile + Phase 05 State/Portfolio다. Last safe point는 Phase 03 route kernel + Phase 04 ordered declarations + immutable routes/bank이며 route-vector 합산/delta ranking은 금지한다. Joint API review와 reciprocal compile/full-recomputation-equality/corruption evidence 뒤 재개한다.

### F-P04-002 — Selected Maven command와 test-fixture 배치가 false green 또는 dependency cycle을 만들 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0 WP-04.1~5의 selected command는 `mvn ... -am -Dsurefire.failIfNoSpecifiedTests=false`를 사용해 test 이름 오타/부재도 성공할 수 있었다. 또한 `build/test-fixtures/src/main`의 profile builder가 profile-catalog production type을 소비하려면 [Phase 00 §3.1](../phases/phase-00-build-architecture-skeleton.md#31-compile-dependency-dag)의 generic `rpdptw-test-fixtures -TEST→ rpdptw-core`에 profile dependency를 추가하거나 profile test가 fixture를 역참조해야 해 target DAG를 깨뜨릴 수 있었다.
- **Correction:** Pinned future `./mvnw`와 target module `-f`, `failIfNoSpecifiedTests=true`, `clean`, fresh exact Surefire class/method manifest를 사용한다. Profile-specific builder/oracle은 profile-catalog `src/test/java`에 test-local로 두고 generic fixture artifact/production path로 올리지 않는다.
- **Applied:** Target [§6, §9.3~§9.4, WP-04.1~5, §11~§12](../phases/phase-04-capabilities-customer-profiles.md)에 module path, fixture ownership, commands, exact report rule과 architecture test를 반영했다.
- **Residual:** Phase 00 accepted wrapper/reactor/plugin configuration과 predecessor artifacts가 실제로 제공되기 전에는 command가 future이며 evidence는 `NOT_PRODUCED`다.

### F-P04-003 — Lookup coordinate와 descriptor/schema identity가 병렬 version ambiguity를 허용했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §7.2의 `ProfileDescriptorIdentity`는 customer/profile/version/schema version을 포함했지만 §8.1 catalog lookup은 customer/profile/version만 받았다. 따라서 같은 lookup coordinate에 서로 다른 schema/content/default가 있으면 둘은 “다른 identity”이면서 exact lookup 결과는 모호해질 수 있었다. Same-full-identity collision test만으로 이 경우를 막지 못했다.
- **Correction:** `ProfileLookupCoordinate(customer, profile, version)`를 유일 catalog coordinate로 고정한다. Schema/content/default 의미 변경은 새 `ProfileVersion` 또는 승인된 migration만 허용하고 same-coordinate 병렬 등록/overwrite를 거부한다. Schema version은 fallback selector가 아니라 verified metadata/fingerprint 차원이다.
- **Applied:** Target [§6, §7.2~§7.3, §8.1, §9.3, §12](../phases/phase-04-capabilities-customer-profiles.md)에 coordinate, lifecycle과 exact collision/unsupported-schema tests를 추가했다.
- **Residual:** Canonical wire encoding과 migration compatibility는 `ADR-003`/Product·API·Data review가 필요하다. Generic same-coordinate ambiguity 금지는 더 이상 open이 아니다.

### F-P04-004 — Authorization이 catalog read 뒤에 있어 tenant enumeration과 unsafe observability를 허용했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §8.1은 `catalog.findExact`와 schema/fingerprint 검증 뒤 customer authorization을 수행했다. Unauthorized caller가 not-found, unsupported schema, integrity failure를 구별하거나 catalog read side effect를 유발할 수 있었다. §11.3은 failure safe fields만 나열하고 success audit, exact pre-read denial과 sentinel redaction test가 없었다.
- **Correction:** Requested tenant/customer scope를 catalog read 전에 authorize하고, tenant-scoped exact lookup/verification 뒤 exact descriptor+preset authorization을 다시 수행한다. Denial outward contract는 존재/schema/integrity를 노출하지 않는다. Success/failure audit allowlist, forbidden fields와 semantic fingerprint 비영향을 exact tests로 고정한다.
- **Applied:** Target [§7.3, §8.1, §9.3, §11.2~§11.3, §12/§15](../phases/phase-04-capabilities-customer-profiles.md)에 two-stage authorization, read-count/error-shape test와 capture-sink sentinel redaction test를 반영했다.
- **Residual:** 실제 tenant storage, API error mapping과 telemetry backend authorization은 Phase 08 이후 evidence 대상이다. Phase 04 generic contract는 provider-neutral하다.

### F-P04-005 — Independent oracle가 fallback/default/precedence 결함을 실제로 검출하는 sensitivity 계약이 없었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §9는 exhaustive literal oracle을 계획했지만 deliberately faulty implementation을 assertion red로 만드는 test가 없었다. Type 부재 compile red나 production binder와 oracle의 일치만으로는 latest fallback, duplicate first-wins, hidden default, wrong failure precedence 또는 authorization-after-read를 oracle이 놓치는지 알 수 없다.
- **Correction:** Minimal literal counterexample마다 latest fallback, first-wins, hidden default, wrong precedence, read-before-auth와 cross-namespace coercion을 심은 faulty doubles를 두고 oracle/security assertion이 실제 red가 되는 sensitivity report를 요구한다.
- **Applied:** Target [§6, §9.1~§9.4, WP-04.3, §11~§12](../phases/phase-04-capabilities-customer-profiles.md)에 `Phase04OracleSensitivityTest`, stable ordinal/counterexample와 evidence 요구를 추가했다.
- **Residual:** Phase 04 test/source가 없으므로 sensitivity evidence는 현재 `NOT_PRODUCED`다.

### F-P04-006 — Business equality와 solution/insertion-context tie 경계가 닫히지 않았다

- **Severity/status:** `MEDIUM — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Phase 03 §3/§13은 ordered business vector 뒤 tie라는 의미만 고정하고 equality 관찰 API를 open으로 남긴다. Phase 05 §8.3은 business equality 뒤 request/vehicle/target/position tie를 적용한다. Target v1.0은 `EvaluationPlan` comparator를 유일 authority로 말했지만 solution stable tie와 insertion-context tie의 exact owner/order/fingerprint 또는 equality entry point를 entry/blocker에 상속하지 않았다.
- **Correction required:** Business-vector compare/equality, solution total-order tie와 insertion-context tie를 분리하고 어떤 context key도 non-tied objective를 뒤집지 못하게 해야 한다.
- **Applied:** Target §2~§4, §9.3, §12~§15에 blocker, last-safe meaning과 `ComparatorTieBoundaryContractTest` restart 조건을 추가했다.
- **Residual/owner/last safe/restart:** Owner는 Core/Evaluation + Capability/Profile + Phase 05/06 Algorithm이다. Last safe point는 ordered business vector와 objective-first 의미다. Equality/tie API, owner/order/fingerprint 및 transitivity/antisymmetry/non-tied/permutation tests의 공동 승인 뒤 freeze한다.

### F-P04-007 — Vehicle/evaluation/operator/budget “capability” key 공간이 섞여 있었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §3.1은 vehicle qualification과 evaluation capability는 분리했지만 §7.4 `SolvePlan`은 `List<CapabilityReference> approvedOperatorReferences`를 두고 “current registry”로 검증한다고 했다. 그대로 구현하면 Phase 05/06 operator key를 Phase 03 evaluation `CapabilityRegistryView`로 resolve하거나 같은 literal key를 coercion할 수 있다.
- **Correction:** Vehicle qualification, evaluation capability, algorithm operator와 budget/guard를 별도 typed namespace/inventory로 둔다. Phase 04는 승인된 operator inventory의 exact reference만 bind하고 operator implementation/runtime을 소유하지 않는다.
- **Applied:** Target [§3.1~§3.3, §7.4, §9.3, §12~§13](../phases/phase-04-capabilities-customer-profiles.md)에 `OperatorCapabilityReference`, key-space collision test, distinct inventory와 last-safe gate를 반영했다.
- **Residual:** `SolvePlan` exact name/type와 operator/budget inventory는 `P-04` cross-phase review 전까지 `PROPOSED/OPEN`이다. 미승인 reference는 hidden default가 아니라 typed rejection이다.

### F-P04-008 — Phase 05 traversal/`CLOCK`/utilization authority gap이 Phase 04 policy handoff에 누락됐다

- **Severity/status:** `MEDIUM — RESIDUAL CROSS-PHASE AUTHORITY BLOCKER`
- **Exact evidence:** [Phase 05 review F-P05-011](phase-05-review.md#f-p05-011--portfolio-traversal과-coordinateutilization-policy는-deterministic-implementation을-freeze하기에-아직-authority가-부족하다)은 exact request-target traversal, `CLOCK` axis/orientation과 utilization missing/zero edge policy를 미승인으로 판정하고 Capability/Profile을 공동 owner로 둔다. Target v1.0의 `SolvePlan`/handoff에는 이 blocker와 last-safe policy가 없었다.
- **Correction required:** Phase 04는 related typed reference만 bind하며 hidden collection order, coordinate convention, `double`/epsilon, zero-capacity default를 만들지 않는다.
- **Applied:** Target [§12~§15](../phases/phase-04-capabilities-customer-profiles.md)에 blocker, conditional exit gate, Phase 05 handoff와 `PortfolioPolicyHandoffContractTest` restart 조건을 추가했다.
- **Residual/owner/last safe/restart:** Owner는 Algorithm + Domain/Input + Capability/Profile + Phase 05 Portfolio다. Last safe point는 policy role만 보존, missing `CLOCK → UNAVAILABLE`, positive rational test-only fixture와 hidden order/default 금지다. Exact traversal, coordinate reference vectors, typed utilization edge policy와 golden trace 승인 뒤 관련 profile reference를 freeze한다.

### F-P04-009 — 인접 Phase whole-file hash가 stale했고 reciprocal churn을 만들었다

- **Severity/status:** `LOW — APPLIED`
- **Exact evidence:** Target v1.0 metadata는 Phase 03의 오래된 whole-file SHA와 Phase 05의 “observed” whole-file SHA를 기록했다. 인접 문서가 safe correction을 받을 때 의미와 무관하게 stale해지고, 서로 상대 문서 hash를 기록하면 순환 편집이 발생한다.
- **Correction:** Canonical source provenance와 accepted implementation artifact/evidence digest는 유지하되 변경 중인 인접 문서는 exact consumed section/status로 추적한다. Acceptance 뒤 producer가 순환 self-reference 없는 section-scoped API/artifact/evidence manifest를 발행하고 consumer가 단방향으로 pin한다.
- **Applied:** Target metadata와 §1.1에서 Phase 03/05 whole-file hash를 제거하고 consumed section/status + directional handoff policy로 바꿨다.
- **Residual:** Canonical source hash map은 reciprocal adjacent reference가 아니며 fresh 검증 대상으로 남겼다. 인접 파일의 자체 metadata는 각 owner review 소관이다.

## 5. 축별 판정

| Review axis | 판정 | 근거/남은 조건 |
|---|---|---|
| 사용자 고정 authority, `REVIEW` 비중단 | `PASS` | Authority 순서와 provenance 보존 |
| Historical/current 분리 | `PASS` | Superseded/codex는 regression cross-check only; current 결정을 대신하지 않음 |
| OPEN/GATED/deferred/official/default 비확정 | `PASS` | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, rotation/public schema/production 값 gate 유지 |
| Phase 03 binding boundary | `PASS WITH RESIDUAL` | Existing declarations만 materialize; full-solution/tie blockers 미해소 |
| Capability 대 customer/provider policy ownership | `PASS AFTER FIX` | Physical/evaluation/profile/catalog/provider/operator namespace와 owner 분리 |
| Profile identity/version/fingerprint lifecycle | `PASS AFTER FIX` | Unique lookup coordinate, schema/fingerprint, solve-bound lifecycle과 collision test |
| Phase 05 handoff | `CHANGES_REQUIRED` | Immutable bound handoff는 명확; solution/tie/portfolio policy 세 blocker |
| Hidden production defaults/fallback | `PASS` | Exact version/default provenance, unsupported/missing typed rejection, production catalog empty |
| Dependency direction/test placement | `PASS AFTER FIX` | Core/provider/profile DAG와 profile-local test helper; actual Phase 00 reactor 필요 |
| Executable command/report truth | `PASS DOCUMENT PLAN` | Pinned wrapper, fail-closed selected tests, fresh exact method manifest; actual wrapper/module 없음 |
| Fixture/oracle independence | `PASS DOCUMENT PLAN` | Literal exhaustive oracle + seeded faulty doubles; result `NOT_PRODUCED` |
| Compatibility/rejection/corruption | `PASS DOCUMENT CONTRACT` | Core-rule non-disable, typed precedence, one-field drift/collision/fingerprint rejection |
| Reproducibility/immutability/isolation | `PASS DOCUMENT PLAN` | Stable closure/backend/order/parallel equality, defensive lifecycle와 no post-bind lookup |
| Security/authorization/observability | `PASS AFTER FIX` | Pre-read denial, non-enumeration, allowlist/redaction과 telemetry 비의미성 |
| Rollback/last-safe/restart | `PASS AFTER FIX` | WP rollback과 세 external blocker owner/last-safe/exact restart 분리 |
| Adjacent overlap/gap | `CHANGES_REQUIRED` | F-P04-001, F-P04-006, F-P04-008 |
| Link/traceability/status/fake evidence | `PASS AFTER FIX` | Actual/proposed/future/NOT_PRODUCED 구분, source→test→evidence와 directional handoff |
| Whole-file reciprocal hash | `PASS AFTER FIX` | Phase 03/05와 review reciprocal document hash 0 |

## 6. Applied fixes와 residual blocker

### 6.1 Applied safe/obvious fixes — 6

1. Fail-closed pinned Maven commands, fresh exact class/method report manifest와 profile-local test helpers.
2. Unique profile lookup coordinate, schema/version/content/default lifecycle와 collision tests.
3. Pre-read authorization, non-enumerating error와 success/failure observability redaction.
4. Seeded faulty-double oracle sensitivity와 stable minimal counterexample evidence.
5. Vehicle/evaluation/operator/budget key-space와 inventory 분리.
6. 인접 Phase reciprocal whole-file hash 제거와 directional section-scoped handoff policy.

### 6.2 Residual blockers — 3

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Full-solution evaluator/ranking | Core/Evaluation + Capability/Profile + Phase 05 State/Portfolio | Route kernel + ordered bound declarations + immutable routes/bank; ad hoc aggregate 금지 | Exact solution API/identity/reuse/failure/comparator + reciprocal compile/full-equality/corruption evidence |
| Business equality 대 solution/insertion tie | Core/Evaluation + Capability/Profile + Phase 05/06 Algorithm | Ordered business vector; context tie는 non-tied 결과를 못 뒤집음 | Equality/tie owner/order/fingerprint + comparator-law/non-tied/permutation tests |
| Traversal/`CLOCK`/utilization policy authority | Algorithm + Domain/Input + Capability/Profile + Phase 05 Portfolio | Policy role only; missing `CLOCK` unavailable; no hidden order/coordinate/zero default | Reference vectors, typed missing/zero policy와 golden traversal trace approval |

Phase 00~03 acceptance/evidence, scheduler task/owners, target reactor/wrapper, `ADR-003`, production onboarding과 `ADR-004` facet은 위 세 cross-phase gap과 별개의 implementation/feature gates다. 서로를 임의로 대신해 닫지 않는다.

## 7. 검증 결과

Final validation은 다음 두 허용 파일만 대상으로 수행했다.

```text
docs/implementation/phases/phase-04-capabilities-customer-profiles.md
docs/implementation/reviews/phase-04-review.md
```

| 검사 | 결과/evidence |
|---|---|
| 두 파일 non-empty | `PASS`; 두 경로 모두 non-empty |
| Required metadata/sections/finding field | `PASS`; target metadata/§1~§15와 review metadata/§1~§7, 9개 finding의 severity/evidence/correction/applied/residual field 확인 |
| Source fingerprint | `PASS`; target metadata의 canonical/source 7개 SHA-256을 fresh 계산해 mismatch 0 |
| Relative Markdown target/anchor | `PASS`; 두 파일의 local `.md` target과 heading/explicit anchor missing 0 |
| Trailing whitespace/fence parity | `PASS`; trailing match 0, 두 파일 fence count 짝수 |
| False-green/reciprocal hash guard | `PASS`; target active command의 `failIfNoSpecifiedTests=false` 0, Phase 03/05 또는 Phase 04↔review reciprocal whole-file hash 0 |
| Status/evidence truth | `PASS`; review 15/15 current verdict와 adjacent Phase 03/05 `CHANGES_REQUIRED`를 live 표기하고 historical inventory, `NOT_STARTED/NOT_PRODUCED/NOT_ACCEPTED`와 future/restart marker 유지 |
| Git whitespace check | `PASS`; allowed paths의 standard/untracked-aware diff check에서 whitespace error 0 |
| Allowed edit scope | `PASS`; reviewer write action은 target Phase 04와 이 review 두 파일뿐. Canonical/인접 Phase/Java/POM/status 문서는 read-only |

Actual Java/Maven inventory에는 read-only toolchain/POM/source listing만 사용했다. Target wrapper/module/test가 없으므로 root placeholder build나 future Phase command를 실행해 Phase 04 evidence `PASS`로 기록하지 않았다.
