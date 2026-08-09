# Phase 05 독립 리뷰 — Pickup-delivery pair, 삽입과 초기 후보군

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "05"
phase_name: pair-insertion-initial-portfolio
review_date: 2026-07-28
reviewer_role: independent Phase 05 reviewer
target_document: docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
target_document_version_after_safe_fixes: 1.3
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
source_drift_policy: SOURCE_COMMIT_PLUS_EXACT_CITED_SECTION_STATUS_AND_REQUIREMENT_IMPACT_REVIEW
whole_file_reciprocal_hashes: NOT_USED
document_verdict: CHANGES_REQUIRED
implementation_entry_verdict: BLOCKED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
phase_acceptance_status_observed: NOT_ACCEPTED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_verdict: PASS_DOCUMENTATION_ONLY_STATUS_UNCHANGED
finding_counts:
  critical: 0
  high: 3
  medium: 8
  low: 1
  total: 12
finding_disposition:
  applied_safe_obvious: 9
  residual_cross_phase_or_authority_blocker: 3
```

## 1. 결론

[Phase 05 v1.2](../phases/phase-05-pair-insertion-initial-portfolio.md)은 pair atomicity, real pair와 delivery-only service meaning, exhaustive insertion, immutable construction, 최대 8개 portfolio와 Phase 06 이전 handoff를 상세히 추적한다. 리뷰 중 canonical authority와 accepted Phase 00 test DAG로 답이 명백한 8건, final audit에서 확인된 live status drift 1건은 target 문서에 직접 정정했다.

문서 verdict는 `CHANGES_REQUIRED`다. 다음 세 계약은 Phase 05 문서만 수정해 임의 확정할 수 없다.

1. Route artifact를 넘어 ordered routes + bank 전체 objective를 계산하는 full-solution evaluator/ranking API, identity, failure와 reuse/invalidation contract.
2. Business comparator equality, solution-level stable tie와 insertion-context tie의 owner/order/fingerprint contract.
3. Portfolio의 exact request-target traversal, `CLOCK` coordinate convention과 utilization missing/zero edge policy approval.

Implementation entry는 별개로 `BLOCKED`다. Phase 00~04 accepted artifact/evidence, scheduler task/owner, target reactor와 Phase 05 source/test/evidence가 없다. 문서 review와 safe correction은 구현, evidence 또는 Phase acceptance가 아니다.

## 2. 검토 source와 관찰 baseline

### 2.1 권위 source

| Source | 검토 범위 | 적용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | §4.1~§4.7, §6, §9~§13, §15.4~§15.6, §16~§17 | Pair/portfolio/evaluation owner, stable state, COW 경계, RM-3 gate와 hidden-default 금지 |
| [Final Domain Design](../../2026-07-26-domain-design.md) | §2~§3, §6~§11, §17.5~§17.6, §18 | Real/delivery-only pair, route-bank XOR, prepared travel, full evaluation, insertion/portfolio와 acceptance evidence |
| [Final Architecture Design](../../2026-07-26-architecture-design.md) | §2, §5.5~§5.6, §6 | Java 25/Maven module/package DAG, test-fixture leakage, security/observability/test evidence |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | §3, §6~§10, §19~§25 | 15 Phase placement, Phase 05 owner, configuration/provenance/security/failure/test/corruption/anti-pattern |
| [Open-question register](../../master-design-open-questions.md) | Exact 28개 `Q-*` 행과 §3~§4 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1` 보존 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§15, 특히 Phase 03~06과 §8~§13 | Current inventory, entry/exit, test/evidence/DoD, rollback/security/observability/restart |
| [Implementation README](../README.md) | §1~§7 | 사용자 고정 authority, `REVIEW` 비중단, historical/current/status 규칙 |

Final Domain §18의 오래된 `Q-INFRA-01 DEFERRED`, `RESOLVED 25 / DEFERRED 2` 문구는 최신 질문 등록부보다 낮고 오래된 상태이므로 사용하지 않았다. `REVIEW` metadata는 provenance로 보존했으며 사용자 고정 authority 아래 리뷰를 중단하지 않았다.

`Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip/rotation, public schema, official numeric budget과 production default는 임의 해소하거나 활성화하지 않았다.

### 2.2 인접 Phase와 historical-only source

[Phase 04](../phases/phase-04-capabilities-customer-profiles.md)와 [Phase 06](../phases/phase-06-cow-alns-reproducibility.md)은 read-only로 대조했다.

- Phase 04의 accepted handoff 끝은 immutable `BoundProfile`/snapshot, existing Phase 03 declarations와 comparator다. Insertion option, bank, seed와 full-solution evaluator owner/API는 제공하지 않는다.
- Phase 06은 Phase 05의 immutable `SearchSnapshot`, `SeedPortfolio`, pure pair evaluator를 소비하고 destroy/repair/COW/acceptance/screen/RNG를 소유한다.
- Phase 04 target은 `REVIEWED_CHANGES_REQUIRED`, Phase 06 target은 `CHANGES_REQUIRED`이고 두 independent review는 `COMPLETE`/`CHANGES_REQUIRED`다.
- 두 Phase 모두 implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `NOT_ACCEPTED`이며 accepted handoff가 아니다.
- 인접 문서를 수정하거나 reciprocal whole-file hash를 추가하지 않았다.

[2026-07-26 Master — SUPERSEDED](../../2026-07-26-master-design.md)는 regression cross-check에만 사용했다. `docs/codex/*`, `master-design-sessions/*`, `arranged/*`, `orgin/*`와 legacy GCP source는 historical/reference/current-placeholder evidence일 뿐 current target authority로 사용하지 않았다.

### 2.3 Actual Java 25/Maven inventory

| 항목 | 실제 관찰 | Phase 05 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`; `docs/implementation/` 전체가 Git 기준 untracked | 기존 사용자 문서 작업으로 보존; 허용된 두 파일 외 수정 금지 |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Java 25 target과 일치하지만 Phase evidence가 아님 |
| Maven | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT`, release 25, module 목록/wrapper 없음 | Target reactor, core/solver/test module 부재 |
| Dependency | Google Workflow/Storage, Jackson, JUnit가 root classpath에 함께 있음 | Stable core/provider 격리 전 legacy placeholder |
| Source/test | Main Java 6개, test Java 1개 | Target `com.ronext.rpdptw` pair/insertion/portfolio source/test 0개 |
| Placeholder | `AlnsBatchEngine`은 `double`, `SplittableRandom`, 합성 objective와 `Map<String,Object>`를 사용 | Bound comparator, initial candidate 또는 reproducibility evidence로 재사용 금지 |
| Documents | Phase 00~14 detailed 15개와 review 15개 actual; Phase 04/06 review 모두 `COMPLETE`/`CHANGES_REQUIRED`; accepted Phase evidence 없음 | 파일 존재·document review·implementation/evidence/Phase acceptance를 분리 |

Target `./mvnw`, `rpdptw/core`, `rpdptw/solver`, `build/test-fixtures`, `build/architecture-rules`와 Phase 05 test가 없으므로 future Maven command를 실행해 구현 green으로 기록하지 않았다.

## 3. Severity 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 안전/권위/상태를 즉시 잘못 확정하거나 회복 곤란한 corruption을 허용 |
| `HIGH` | Canonical 의미 위반, false-green gate 또는 dependency/authority 공백으로 구현·acceptance를 막음 |
| `MEDIUM` | Determinism, identity, failure, oracle, security, trace 또는 cross-Phase API를 불완전하게 함 |
| `LOW` | 구현 의미를 직접 바꾸지 않는 link/status/naming drift |

## 4. Findings

### F-P05-001 — Full-solution evaluator와 ranking authority가 Phase 03~05 사이에서 비어 있다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target v1.0 §6.3은 route materialization 뒤 “Phase 04 bound solution projection/comparator value”를 얻는다고 했고 §7.4의 `BoundInsertionAuthority`는 route 하나만 받아 `CandidateRankingArtifact`를 반환했다. §8.2는 route list와 bank를 바꾼 뒤 “bound authority”로 candidate evaluation을 만든다고 했다. 그러나 [Master §4.2](../../2026-07-31-phase-b-master-design.md#42-단계별-데이터-계약)와 `RM-2`는 full route/solution evaluation을 요구하고 mandatory unassigned count, used vehicle/ownership objective는 solution 전체를 읽는다. Phase 03은 route kernel, Phase 04는 bound declaration만 정의한다.
- **Correction:** Route-level feasibility와 solution-level objective/ranking을 분리하고, 승인되지 않은 solution artifact를 route delta, mutable aggregate 또는 placeholder scalar로 합성하지 않아야 한다.
- **Applied:** Target [§4](../phases/phase-05-pair-insertion-initial-portfolio.md#4-entry-gate와-evidence-확인), [§6.3](../phases/phase-05-pair-insertion-initial-portfolio.md#63-evaluation-위임과-result-triage), §7.2/§7.4, §8.2, §11~§16에 blocker, placeholder 금지, WP/test/evidence/handoff 조건을 반영했다.
- **Residual/owner/last safe/restart:** Owner는 Core/Evaluation + Capability/Profile + Phase 05 State/Portfolio다. Last safe point는 Phase 03/04 route-level artifact와 immutable ordered routes/bank source of truth다. Problem/travel/profile + ordered routes + bank API, route-artifact reuse/invalidation, exact aggregation/failure/fingerprint/comparator와 reciprocal compile/full-equality/corruption test가 승인되면 재개한다.

### F-P05-002 — Selected Maven command가 test 0건을 성공으로 통과시킬 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0 WP-05.1~05.6과 §12.1의 selected command는 모두 `-Dsurefire.failIfNoSpecifiedTests=false`를 사용했다. Test 이름 오타, module drift 또는 부재여도 exit 0이 가능했다. 동시에 `-am`은 test가 없는 upstream module까지 같은 `-Dtest`를 전달해 fail-closed로 바꿀 때 false failure/skip을 만들 수 있었다.
- **Correction:** Pinned future wrapper를 사용하고 fresh full-reactor `clean verify`를 먼저 실행한다. Selected test는 target module만 실행하며 `failIfNoSpecifiedTests=true`, non-zero exact class/method와 fresh Surefire XML의 failed/error/skipped 0을 대조한다.
- **Applied:** Target WP-05.0~05.6과 [§12.1](../phases/phase-05-pair-insertion-initial-portfolio.md#121-future-exact-command-set)의 모든 command를 정정했다.
- **Residual:** Phase 00 accepted wrapper/reactor/plugin/report contract가 실제로 없으므로 command는 future이고 evidence는 `NOT_PRODUCED`다.

### F-P05-003 — Independent fixture와 architecture test 배치가 Phase 00 DAG를 역전하거나 test를 찾지 못하게 했다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0 §5.2는 `build/test-fixtures/src/main`의 oracle/builder를 core/solver test가 소비하는 tree를 제시했다. Target [Phase 00 §3.1](../phases/phase-00-build-architecture-skeleton.md#31-compile-dependency-dag)은 `rpdptw-test-fixtures -TEST→ rpdptw-core` 단방향이며 production/main fixture와 reverse `core test → fixture`를 허용하지 않는다. `Phase05ArchitectureTest`는 tree에 경로가 없었고 root selected command로만 호출됐다.
- **Correction:** Independent core oracle은 `build/test-fixtures/src/test`에서 test-scope로 core를 소비하고, solver oracle helper는 solver test-local로 둔다. Architecture test는 `build/architecture-rules/src/test`에 두고 exact module command로 실행한다.
- **Applied:** Target §5.2, §10.3, WP-05.2/05.5/05.6과 §12.1에 tree, class ownership과 command를 정정했다.
- **Residual:** Phase 00 accepted classifier/scope/module naming이 달라지면 Phase 05는 architecture owner review 뒤 갱신해야 한다.

### F-P05-004 — Bounded enumeration의 `maxEvaluations`가 runtime truncation인지 declaration validation인지 모호했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §6.2/§7.4는 ordered unique positions와 `maxEvaluations`를 함께 두었지만 list가 max보다 클 때의 의미와 `evaluatedPositionCount` equality를 정하지 않았다. 구현자가 loop를 max에서 중단하고도 bounded receipt를 성공으로 만들 수 있었다.
- **Correction:** Exhaustive canonical position order를 명시하고, bounded declaration은 `orderedUniquePositions.size() <= maxEvaluations`를 bind 시 검증한다. 초과/duplicate/out-of-range는 `Invalid`; runtime truncation 없이 declared set 전체를 정확히 한 번 평가한다.
- **Applied:** Target [§6.2](../phases/phase-05-pair-insertion-initial-portfolio.md#62-exhaustive와-bounded-enumeration), §7.4, exit/anti-pattern에 반영했다.
- **Residual:** Bounded policy 생성 algorithm/version은 Phase 06 repair config의 별도 proposed contract다. Phase 05 correctness/portfolio는 계속 `EXHAUSTIVE`만 사용한다.

### F-P05-005 — Business equality와 insertion stable tie의 API/encoding 경계가 닫히지 않았다

- **Severity/status:** `MEDIUM — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target v1.0 §8.3은 business comparator 뒤 request→ownership/vehicle→target→position→route fingerprint tie를 적용했지만 Phase 03 comparator signature에는 business equality와 tie context가 없고 existing route 대 `NEW_ROUTE`의 exact target-key order/version도 없었다. Non-tied objective를 뒤집지 않는 의미만 canonical이다.
- **Correction:** Business vector comparison/equality, solution tie와 insertion-context tie를 분리하고 target-key order/version을 explicit config/identity로 승인해야 한다.
- **Applied:** Target §8.3과 §14에 unresolved boundary, owner, last safe point와 restart test를 명시했다.
- **Residual/owner/last safe/restart:** Owner는 Core/Evaluation + Phase 05/06 Algorithm이다. Last safe point는 objective-first와 stable semantic field set이다. Comparator law, non-tied reversal 0, exhaustive option-order와 encoding/version review 뒤 freeze한다.

### F-P05-006 — `SeedPortfolio`가 member와 available-candidate 목록을 두 개의 authority로 저장했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §7.4 `SeedPortfolio`는 `membersInCanonicalMatrixOrder` 안의 `Available(candidate)`와 별도 `availableCandidates`를 모두 constructor input으로 받았다. §9.5도 두 list를 별도로 누적했다. Count/order/candidate identity가 서로 달라도 한 object가 만들어질 수 있었다.
- **Correction:** Member list만 source of truth로 저장하고 available candidates는 canonical member order의 immutable projection으로 계산한다. Projection equality/count/order/fingerprint를 constructor/corruption test로 검증한다.
- **Applied:** Target §7.2/§7.4, §9.5, §12.2/§13.2에 단일 authority와 anti-pattern을 반영했다.
- **Residual:** Java exact accessor/encoding은 Phase 00/Phase 05 API review 대상이지만 이중 authority 금지는 고정됐다.

### F-P05-007 — Oracle가 실제 결함을 잡는 sensitivity와 serialization-independent corruption 경계가 부족했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §10.1은 independent oracle을 설명했지만 deliberately faulty implementation을 검출하는 test가 없었다. `CorruptSeedPortfolioBuilder`는 public factory를 우회한 “serialized fixture”를 요구했지만 serialization은 §2.3/§3.1에서 OPEN/non-scope였다.
- **Correction:** 누락 position, reverse pair, fake logical pickup, reverse distance lookup, duplicate evaluation, `Invalid→Rejected`, comparator inversion과 alias를 심은 faulty doubles가 실제 red가 되는 sensitivity test를 추가한다. Corruption은 승인되지 않은 bytes가 아니라 validation boundary의 test-only field projection으로 시작한다.
- **Applied:** Target §5.2, §10.1/§10.3/§10.4, WP-05.2/05.5와 evidence gate에 `Phase05OracleSensitivityTest`와 corruption boundary를 반영했다.
- **Residual:** Canonical encoding 승인 뒤에만 별도 byte-corruption fixture를 추가할 수 있다. 현재 test/evidence는 존재하지 않는다.

### F-P05-008 — Phase-local security/observability/redaction contract가 없었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0은 fingerprint에서 elapsed/provider locator를 제외했지만 safe telemetry field, raw input/PII/secret 금지, failure evidence와 elapsed/completion-order 비의미성을 Phase-local로 정하지 않았다. [Integrated §19~§21](../../architecture-domain-implementation-design.md#19-configuration-provenance와-observability)과 [Plan §13](../master-realization-plan.md#13-위험-보안-운영-관측과-재현성)은 이를 요구한다.
- **Correction:** Core/solver는 logger/env/clock/provider를 의미 입력으로 읽지 않는다. Typed safe aggregates만 evidence adapter에 넘기고 raw input/address/coordinate/secret/provider locator/exception payload를 log/trace에서 제외한다.
- **Applied:** Target §12.2~§12.4와 exit gate에 safe fields, redaction, failure record와 test-only repeat config를 추가했다.
- **Residual:** Adapter authorization, tenant storage와 telemetry backend는 Phase 08 이후 evidence다. Phase 05는 provider를 호출하지 않는다.

### F-P05-009 — Whole-file source hash metadata가 reciprocal churn과 거짓 안정성을 만들었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 metadata는 canonical/local/historical 문서 전체 SHA-256을 소유했고 WP-05.0도 source 문서 전체 `shasum`을 gate로 삼았다. Adjacent Phase가 같은 target hash를 서로 복제하면 의미와 무관한 편집에도 순환 drift가 생긴다.
- **Correction:** Source commit + exact cited section/version/status + requirement/test impact review를 사용한다. Accepted implementation artifact/evidence digest는 handoff에 pin하되 Phase 문서 전체 hash를 reciprocal metadata로 복제하지 않는다.
- **Applied:** Target metadata, §1/§1.1, WP-05.0과 §12.2에서 whole-file hash map/command를 제거했다.
- **Residual:** Read-only 인접 문서의 기존 hash metadata는 각 소유 review에서 처리해야 한다. 이 review는 수정하지 않았다.

### F-P05-010 — Proposed Java record가 comment만으로 validation/defensive immutability를 가정했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §7.4의 collection-bearing records는 canonical constructor 없이 `List`를 그대로 받았고 `PairPositionEnumeration.Exhaustive`에는 bounded와 달리 enumeration fingerprint field가 없었다. Java record는 자동 defensive copy/deep immutability를 제공하지 않는다.
- **Correction:** 모든 collection-bearing record는 canonical constructor에서 null/range/duplicate/count/identity를 검증하고 `List.copyOf`를 사용한다. Enumeration interface가 fingerprint를 공통 제공하도록 shape를 맞춘다.
- **Applied:** Target §7.4의 `PairPositionEnumeration` contract와 공통 defensive immutable constructor rule에 반영했다.
- **Residual:** Nested element의 transitive immutability와 accessor mutation probe는 actual test가 필요하며 현재 evidence는 없다.

### F-P05-011 — Portfolio traversal과 coordinate/utilization policy는 deterministic implementation을 freeze하기에 아직 authority가 부족하다

- **Severity/status:** `MEDIUM — RESIDUAL AUTHORITY BLOCKER`
- **Exact evidence:** Target §3.1/§9.2는 exact multi-route `(request,target)` traversal을 `PROPOSED_REVIEW_REQUIRED`, `CLOCK` axis/orientation을 `OPEN/PROPOSED`, utilization missing/zero rule을 Phase 04 policy owner의 `OPEN/PROPOSED`로 이미 표시한다. Canonical Master는 policy 역할을 고정하지만 이 API 수준 세부를 확정하지 않는다.
- **Correction:** Hidden input/collection order, coordinate fallback, `double`/epsilon 또는 invented zero-capacity default를 사용하지 않고 unresolved 상태를 blocker ledger에 보존해야 한다.
- **Applied:** Target은 이미 §3.1/§9.2~§9.4/§14에서 대체로 올바르게 gate했다. 리뷰 중 §8.3 cross-tie와 WP-05.0 closure 조건을 보강했다.
- **Residual/owner/last safe/restart:** Owner는 Algorithm + Domain/Input + Phase 04 Policy다. Last safe point는 policy role, missing CLOCK → `UNAVAILABLE`, positive-capacity test-only rational fixture와 deterministic no-hidden-order rule이다. Explicit coordinate convention/reference vectors, typed utilization edge policy와 approved golden traversal trace 뒤 재개한다.

### F-P05-012 — Phase 02/03 live document-review 상태가 review 완료 전 값으로 남아 있었다

- **Severity/status:** `LOW — APPLIED STATUS-ONLY`
- **Exact evidence:** Target v1.1 §4의 `2026-07-28 current checkout 관찰`에서 Phase 02와 Phase 03 row가 actual target/review metadata와 달랐다. Actual [Phase 02 target](../phases/phase-02-prepared-travel-immutable-problem.md)은 `REVIEWED_WITH_CORRECTIONS`, [review](phase-02-review.md)는 `FINAL`/`PASS_AFTER_APPLIED_CORRECTIONS`다. Actual [Phase 03 target](../phases/phase-03-route-propagation-evaluation-kernel.md)은 `REVIEWED_CHANGES_REQUIRED`, [review](phase-03-review.md)는 `COMPLETE`/`CHANGES_REQUIRED`다.
- **Correction:** Current checkout 표는 document/review workflow만 actual live metadata로 갱신하고 implementation, evidence, acceptance와 entry gate는 별도 상태로 그대로 보존해야 한다.
- **Applied:** [Phase 05 v1.2 §4](../phases/phase-05-pair-insertion-initial-portfolio.md#4-entry-gate와-evidence-확인)에 Phase 02/03 live target/review verdict를 반영했다.
- **Residual:** 없음. Phase 02는 `NOT_STARTED`/evidence `NOT_PRODUCED`(`NOT_AVAILABLE`)/`BLOCKED_NOT_IMPLEMENTED`, Phase 03은 `NOT_STARTED`/`NOT_PRODUCED`/`NOT_ACCEPTED`, 두 Phase 05 entry row는 계속 `BLOCKED`다. 기존 full-solution evaluator/comparator/portfolio blocker도 변하지 않았다.

## 5. 축별 판정

| Review axis | 판정 | 근거/남은 조건 |
|---|---|---|
| 사용자 고정 authority, `REVIEW` 비중단 | `PASS` | Authority 순서와 provenance 보존 |
| Historical/current 분리 | `PASS AFTER FIX` | Final Domain stale infra status를 최신 register로 override; superseded/codex/GCP는 historical only |
| OPEN/GATED/deferred/hidden numeric | `PASS` | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip/public API 미확정 유지 |
| Pair atomicity/same route/exactly once/precedence | `PASS DOCUMENT CONTRACT` | Stable validator/property/corruption plan이 complete pair와 route-bank XOR를 요구 |
| Delivery-only logical pickup | `PASS DOCUMENT CONTRACT` | Initial-load ownership만, fake node/arc/stop/service 0 |
| Exhaustive/bounded honesty | `PASS AFTER FIX` | Exact order/count와 bound declaration fail-closed; Phase 05 portfolio exhaustive |
| Route feasibility evaluator reuse | `PASS WITH RESIDUAL` | Phase 03/04 route authority 재사용; full-solution owner/API는 blocker |
| Candidate/portfolio immutability identity | `PASS AFTER FIX` | Defensive constructor rule, derived available projection, no-alias/corruption tests |
| Deterministic tie-break | `CHANGES REQUIRED` | Objective-first 고정; equality/target-key/API/encoding 승인 필요 |
| Phase 04 handoff | `CHANGES REQUIRED` | Bound artifact 경계는 맞지만 solution evaluator 공백 존재 |
| Phase 06 handoff | `PASS WITH RESIDUAL` | Seed-only/COW ownership 정합; exact signature와 predecessor acceptance 필요 |
| Test command false-green 방지 | `PASS DOCUMENT PLAN` | Wrapper/full reactor/selected fail-closed/report count; actual reactor 없음 |
| Fixture/oracle/failure detection | `PASS AFTER FIX` | Test DAG 수정, seeded sensitivity와 serialization-independent corruption 추가 |
| Rollback/failure/corruption | `PASS DOCUMENT CONTRACT` | Invalid fail-closed, partial publish 금지, base bytes/fingerprint 보존 |
| Security/observability | `PASS AFTER FIX` | Safe aggregate/redaction/elapsed 비의미성 추가 |
| Reproducibility | `PASS DOCUMENT PLAN` | Repeat/parallel/input permutation exact; actual evidence 없음 |
| Java 25/Maven consistency | `PASS AFTER FIX` | Current inventory truthful, record immutability/selected module command 보강 |
| Status/evidence/traceability | `PASS AFTER FIX` | v1.1 reviewed/changes-required, implementation/evidence not-started 보존 |

## 6. Applied fixes와 residual blocker

### 6.1 Applied safe/obvious fixes — 9

1. Whole-file reciprocal hash 제거와 source commit/section impact review.
2. Current Phase/review inventory 정정.
3. Exhaustive order와 bounded fail-closed semantics.
4. Test-fixtures/architecture-rules DAG와 exact test placement.
5. Maven 0-test false-green 방지와 fresh report rule.
6. `SeedPortfolio` 단일 authority와 Java defensive immutability.
7. Oracle sensitivity와 serialization-independent corruption fixture.
8. Security/observability/redaction/failure evidence.
9. Phase 02/03 live document-review status-only correction.

### 6.2 Residual blockers — 3

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Full-solution evaluator/ranking | Core/Evaluation + Capability/Profile + Phase 05 State/Portfolio | Route-level kernel/artifact + immutable routes/bank; ad hoc aggregate 금지 | Joint API/identity/reuse/failure/comparator approval + compile/full-equality/corruption evidence |
| Comparator equality/insertion tie | Core/Evaluation + Phase 05/06 Algorithm | Objective-first, stable semantic fields, non-tied reversal 금지 | Equality/tie owner, target-key order/version + comparator-law/exhaustive tests |
| Traversal/CLOCK/utilization detail | Algorithm + Domain/Input + Phase 04 Policy | Canonical policy role, CLOCK unavailable, exact positive rational fixture | Coordinate reference vectors, typed edge policy, golden traversal trace approval |

Predecessor acceptance, scheduler/owner와 target reactor 부재는 위 문서 blocker와 별개의 implementation-entry blocker다.

## 7. 검증 결과

Final validation은 다음 두 허용 파일만 대상으로 수행했다.

```text
docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
docs/implementation/reviews/phase-05-review.md
```

| 검사 | 결과/evidence |
|---|---|
| 두 파일 non-empty | `PASS`; `test -s` 2개 경로 |
| Required metadata/sections/finding field | `PASS`; required metadata와 review/target heading 누락 0 |
| Relative Markdown target/heading/explicit anchor | `PASS`; 두 파일 parser에서 missing target/anchor 0 |
| Trailing whitespace와 fence parity | `PASS`; trailing match 0, 두 파일 fence count 짝수 |
| Whole-file reciprocal hash/64-hex document digest | `PASS`; metadata/source map에 reciprocal document digest 0 |
| False-green command | `PASS`; target active command의 `failIfNoSpecifiedTests=false`와 `shasum` 0. Review의 finding evidence에 보존한 과거 문자열은 command가 아님 |
| Phase 02/03 live status drift | `PASS`; target §4가 Phase 02 `REVIEWED_WITH_CORRECTIONS` + review `FINAL/PASS_AFTER_APPLIED_CORRECTIONS`, Phase 03 `REVIEWED_CHANGES_REQUIRED` + review `COMPLETE/CHANGES_REQUIRED`와 일치 |
| `git diff --check`/untracked whitespace check | `PASS`; 허용 경로 `git diff --check`와 각 untracked file의 `--no-index --check` whitespace error 0 |
| Actual Java/Maven inventory reconfirm | `PASS`; Java `25.0.3`, Maven `3.9.14`, main Java 6, test Java 1 |
| Adjacent Phase/Java/POM modification | `PASS`; 이 reviewer의 edit operation은 target Phase 05와 새 review에만 적용 |

Root build는 target Phase 05 module/test가 없는 placeholder build를 Phase evidence로 오인하지 않기 위해 실행하지 않았다.

## 8. ALNS-first direction revision review

후속 task `019fa901-8776-7f61-b467-a8c6595b970d`에서 Phase 05 v1.3의
ALNS-first 경계를 검토했다. Pair insertion의 `exact`가 MIP 호출을 뜻하지 않고,
Phase 05 exit/handoff가 optimizer vendor/license/native/production authority 없이
Phase 06으로 이어지며 Phase 13 요구를 선반영하지 않는 계약은 `PASS`다.

기존 `CHANGES_REQUIRED`, implementation `NOT_STARTED`, evidence `NOT_PRODUCED`,
acceptance `NOT_ACCEPTED`는 바꾸지 않는다. Full-solution evaluator/comparator-tie와
portfolio policy blocker도 그대로다.
