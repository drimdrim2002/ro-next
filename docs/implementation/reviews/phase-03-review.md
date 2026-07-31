# Phase 03 독립 리뷰 — 경로 전파 계산과 평가 kernel

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "03"
review_date: 2026-07-28
reviewer_role: independent Phase 03 reviewer
target_document: docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md
target_document_version_after_safe_fixes: 1.2
target_document_status_after_review: REVIEWED_CHANGES_REQUIRED
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
document_verdict: CHANGES_REQUIRED
implementation_entry_verdict: BLOCKED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
finding_counts:
  critical: 0
  high: 4
  medium: 6
  low: 1
  total: 11
finding_disposition:
  applied_safe_obvious: 9
  residual_cross_phase_blocker: 2
```

## 1. 결론

Phase 03 상세 문서는 route propagation, physical hard feasibility, neutral evaluation layering, Phase 02 authority와 Phase 04 binding seam을 상당히 구체적으로 추적한다. 리뷰 중 상위 권위로 답이 명백한 9건은 [Phase 03 v1.2](../phases/phase-03-route-propagation-evaluation-kernel.md)에 직접 정정했다.

문서 verdict는 `CHANGES_REQUIRED`다. 이유는 다음 두 cross-phase 계약이 아직 닫히지 않았기 때문이다.

1. Canonical `RM-2`의 full route/solution evaluation과 Phase 05의 proposed `CandidateEvaluationArtifact` 사이 exact owner/API/identity/failure/comparator contract.
2. Phase 03 business objective equality/solution total order와 Phase 05 insertion-context stable tie 사이 API와 fingerprint 경계.

이 둘은 Phase 03 문서만 수정해 임의 확정할 수 없다. Implementation entry도 별개로 `BLOCKED`다. Phase 00~02 phase-accepted artifact/evidence, scheduler task/owner와 target reactor가 없고 Phase 03 source/test/evidence도 존재하지 않는다.

## 2. 검토 source와 관찰 baseline

### 2.1 권위 source

| Source | 검토 범위 | 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §4.1~§4.6, §6~§9, §12~§17 | 최상위 system meaning, propagation/evaluation 순서, cache/reproducibility, `RM-2`와 gate |
| [Final Domain](../../deprecated/2026-07-26-domain-design.md) | §5~§7, §9~§10, §17.5~§18 | Time/load/full-arc/resource/evaluation 상세. §18의 오래된 `Q-INFRA-01` 상태는 사용하지 않음 |
| [Final Architecture](../../deprecated/2026-07-26-architecture-design.md) | §2, §5.6, §6 | Java 25/Maven module/package DAG, core/verifier/provider 격리와 test evidence |
| [Integrated implementation design](../../deprecated/architecture-domain-implementation-design.md) | §3, §6~§8, §19~§25, §28 | 15 Phase placement, Phase 02/03/04 contract, typed facet, security/observability/test/anti-pattern |
| [Open-question register](../../deprecated/master-design-open-questions.md) | Exact 28개 `Q-*` 행과 상태 요약 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1` 보존 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§15, 특히 Phase 02~05 | Current inventory, DAG, entry/exit/evidence/rollback/restart와 traceability |

`REVIEW` metadata는 사용자 고정 source authority 아래 provenance로 보존했으며 리뷰를 중단하지 않았다. `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip/rotation, public API/schema와 official production 값은 임의 해소하지 않았다.

### 2.2 인접 Phase와 actual inventory

Read-only로 [Phase 02](../phases/phase-02-prepared-travel-immutable-problem.md), [Phase 04](../phases/phase-04-capabilities-customer-profiles.md), downstream gap 확인을 위한 [Phase 05](../phases/phase-05-pair-insertion-initial-portfolio.md)를 대조했다. 인접 상세는 actual/`NOT_STARTED`다. Final validation에서 Phase 02 document review가 actual/`PASS_AFTER_APPLIED_CORRECTIONS`로 나타났지만 phase verdict는 `BLOCKED_NOT_IMPLEMENTED`; Phase 04/05 phase-accepting review/evidence도 없다. 이 리뷰는 인접 파일을 수정하거나 whole-file reciprocal hash를 추가하지 않았다.

Actual checkout 관찰은 다음과 같다.

| 항목 | 관찰 | 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277`; `docs/implementation/` 전체가 Git 기준 untracked | 사용자 기존 문서 작업으로 보존; 허용된 두 파일 외 수정 금지 |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Root POM Java/Maven enforcer와 일치하지만 Phase evidence 아님 |
| Maven | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT`; release 25; module 목록 없음 | Phase 00~03 target reactor/artifact 부재 |
| Dependency | Google Workflow/Storage, Jackson, JUnit가 root classpath에 함께 있음 | Target core/provider 격리 전 legacy placeholder |
| Main/test source | Main Java 6개, test 1개 | `AlnsBatchEngine`의 `double` 합성 objective와 `Map<String,Object>`는 Phase 03 구현/evidence가 아님 |
| Phase/review files | Phase 상세 15개 actual. Initial inspection의 review 0개에서 공유 workspace final validation 중 Phase 00~02 review가 추가되어, 현재 Phase 00~03 review 4개 actual | Concurrent 문서를 read-only 재확인; document review 존재를 phase acceptance로 승격하지 않음 |

`SUPERSEDED` master와 `docs/codex/*`는 current authority로 사용하지 않았다.

## 3. Severity 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 안전/권위/상태를 즉시 잘못 확정하거나 구현 시 회복 곤란한 corruption을 허용 |
| `HIGH` | Canonical 의미 위반, false-green gate, 핵심 dependency/contract gap으로 Phase 구현·acceptance를 막음 |
| `MEDIUM` | Failure/identity/test/security 또는 cross-phase API가 불완전해 결함 탐지·재현·handoff를 약화 |
| `LOW` | 상태/이름/link 같은 명백한 문서 drift로 구현 의미를 직접 바꾸지는 않음 |

## 4. Findings

### F-P03-001 — Neutral metric과 composed hard constraint 순서가 canonical source와 반대였다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0의 `HardConstraint.check(problem, route, facts)`와 pseudocode는 composed hard를 실행한 뒤 `contributeNeutralMetrics(facts)`를 호출했다. 그러나 [Master §9.1](../../master-design.md#91-단방향-평가-구조)과 [Final Domain §10](../../deprecated/2026-07-26-domain-design.md#10-evaluation-profile과-objective)은 propagation/static gate → neutral facts/metrics → composed hard → score → objective 순서를 요구한다.
- **Correction:** Physical/structural rejection 뒤에는 evaluation call 0을 유지하되, completed facts에서는 neutral metric을 먼저 만들고 composed hard가 facts와 `MetricSnapshot`을 읽게 해야 한다. Composed hard rejection 뒤 score/objective만 0이어야 한다.
- **Applied:** [Phase 03 §2.1, §7.2, §8.2, §9.3, WP-03.3, §12/§15](../phases/phase-03-route-propagation-evaluation-kernel.md)에 순서, signature, `Invalid` branch와 분리된 call-count test를 반영했다.
- **Residual:** Exact Java 이름/visibility는 여전히 proposed이며 Phase 03/04 contract review가 필요하다. 의미 순서는 더 이상 open이 아니다.

### F-P03-002 — Full-arc pseudocode가 stale/undefined work-window end를 사용할 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0 state에는 `currentWorkEnd`가 없지만 arc loop는 이를 읽었고, 다음 창을 선택한 뒤에도 갱신하지 않았다. 같은 문서의 hand fixture는 A→B에서 `[200,400]`으로 이동한 뒤 B→B와 B→D를 그 창에서 계속 처리해야 하므로 pseudocode와 expected table이 충돌했다.
- **Correction:** 매 arc마다 departure를 포함하는 normalized window를 찾고 그 창에 full arc가 들어가면 즉시 출발한다. 아니면 `start >= departure`이면서 `start + U <= end`인 첫 미래 창을 선택하고 rest를 checked 누적한다.
- **Applied:** [Phase 03 §8.2](../phases/phase-03-route-propagation-evaluation-kernel.md#82-canonical-forward-propagation)의 resolver와 `usesAdvancedWorkWindowForFollowingArcs()` sensitivity를 추가했다.
- **Residual:** 없음. Expanded/repeating/overnight window 생성은 계속 Phase 01/02 authority이며 Phase 03은 raw time을 재해석하지 않는다.

### F-P03-003 — Selected Maven command가 없는 test를 성공으로 통과시킬 수 있고 test-fixtures edge가 역전돼 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0의 모든 selected command는 `-am`과 `-Dsurefire.failIfNoSpecifiedTests=false`를 함께 사용했다. 오타/누락 test도 exit 0이 될 수 있다. 또한 core test가 `build/test-fixtures/src/main` builder를 직접 소비하는 tree는 [Phase 00 §3.1](../phases/phase-00-build-architecture-skeleton.md#31-compile-dependency-dag)의 `rpdptw-test-fixtures → rpdptw-core`와 반대 test dependency를 요구해 reactor cycle을 만들 수 있다.
- **Correction:** Core selected test는 `-am` 없이 `failIfNoSpecifiedTests=true`; independent oracle/property는 `build/test-fixtures → core` 외부 module test로 실행; architecture/test-fixtures는 full `clean verify`; exact class/method를 fresh Surefire XML에서 fail-closed 대조한다.
- **Applied:** [Phase 03 §6, §7.3, §9.3~§9.4, WP-03.1~5, §11](../phases/phase-03-route-propagation-evaluation-kernel.md)에 tree, commands, selected command와 expected test 범위의 일치, architecture tests와 report manifest rule을 반영했다. Future command는 Phase 00이 만드는 pinned `./mvnw`를 사용한다.
- **Residual:** Phase 00 accepted wrapper/reactor/Surefire configuration이 실제로 제공되기 전에는 모든 command가 future이며 실행 evidence가 아니다.

### F-P03-004 — Full solution evaluation authority가 Phase 03~05 사이에서 비어 있다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** [Master `RM-2`](../../master-design.md#154-rm-2--propagation-evaluation과-bound-profile)는 full route/solution evaluation과 exact fingerprint를 deliverable로 요구한다. Target §7은 `RoutePlan` 하나의 `RouteEvaluationKernel/Artifact`만 구체화한다. [Phase 05 §6.3/§7](../phases/phase-05-pair-insertion-initial-portfolio.md#63-evaluation-위임과-result-triage)은 Phase 04 “solution projection”과 `CandidateEvaluationArtifact`를 소비하지만 Phase 04에는 그 exact owner/API가 없다.
- **Correction required:** Core/Evaluation, Capability/Profile, Phase 05 state owner가 problem/travel/profile + ordered routes + bank를 받는 solution evaluation contract, route artifact reuse 조건, exact invalidation/fingerprint, hard/metric/objective aggregation, failure triage와 comparator entry point를 공동 승인해야 한다.
- **Applied:** Target [§2.3, §3, §13~§15](../phases/phase-03-route-propagation-evaluation-kernel.md)에 gap과 금지 fallback을 명시했다.
- **Residual/owner/last safe/restart:** Owner는 Core/Evaluation + Capability/Profile + Phase 05다. Last safe point는 route-only kernel과 ad hoc aggregation 금지다. Phase 03~05 reciprocal compile/full-equality/corruption test와 handoff review 승인 뒤 재개한다.

### F-P03-005 — Extension arithmetic/corruption을 `Invalid`로 올리는 execution boundary가 불명확했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0은 propagation overflow만 exact test했고, `HardConstraint`는 `Satisfied/Rejected`만 반환했으며 metric/score/objective component failure와 overflow의 mapping이 없었다. 이는 “overflow/component corruption은 `Invalid`”이라는 target 자체의 invariant와 불일치했다.
- **Correction:** `ConstraintCheck.Invalid`를 추가하고 kernel이 extension call을 fail-closed로 감싼다. Checked overflow, typed component failure, wrong key/unit/type/fingerprint와 unexpected component exception은 정상 rejection/default가 아닌 `Invalid`이며 evidence를 실패시킨다.
- **Applied:** [Phase 03 §7.1~§7.2, §8.2, §9.3, §11~§12](../phases/phase-03-route-propagation-evaluation-kernel.md)에 taxonomy, failure precedence, exact tests와 evidence를 반영했다.
- **Residual:** Exact exception/type hierarchy는 API review 대상이지만 `Invalid` 대 `Infeasible` 의미는 고정됐다.

### F-P03-006 — Business objective equality와 context stable tie의 API 경계가 닫히지 않았다

- **Severity/status:** `MEDIUM — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target은 `ObjectiveComparator.compare(ObjectiveVector,ObjectiveVector)`와 별도 `StableTiePolicy`로 total order를 주장하지만 comparator signature에는 tie context가 없다. [Phase 05 §8.3](../phases/phase-05-pair-insertion-initial-portfolio.md#83-stable-option-tie-key)는 business comparator가 equality를 반환한 뒤 request/vehicle/route/position tie key를 적용한다고 규정한다.
- **Correction required:** Business-vector comparison/equality API, solution stable tie와 insertion-context tie의 owner/order/fingerprint를 분리해야 한다. Non-tied business objective를 어떤 context key도 뒤집을 수 없어야 한다.
- **Applied:** Target §3/§13/§14에 cross-phase blocker와 last-safe semantics를 기록했다.
- **Residual/owner/last safe/restart:** Owner는 Core/Evaluation + Phase 05/06 Algorithm이다. Last safe point는 ordered vector 비교와 “objective first” 의미다. Cross-phase API review와 comparator/insertion exhaustive test 승인 뒤 freeze한다.

### F-P03-007 — Rejection/failure identity와 Java immutable collection 검증이 부족했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0의 `ConstraintRejection` identity는 code/position/actual/bound만 명시해 다른 problem/route/plan의 stale reason과 구분할 수 없었다. `RouteFacts`, `PropagationDeclaration`, `EvaluationPlan` 등 record의 list defensive-copy test도 없었다.
- **Correction:** Rejection/failure 또는 enclosing result가 exact problem/travel/route/propagation/evaluation identity에 결합되고 call-local/non-cacheable임을 명시한다. 모든 collection/element는 defensive immutable value이며 constructor/accessor mutation probe가 필요하다.
- **Applied:** [Phase 03 §7.1~§7.2, §9.3](../phases/phase-03-route-propagation-evaluation-kernel.md)에 identity/lifecycle과 `RouteEvaluationImmutabilityTest`를 추가했다.
- **Residual:** Canonical encoding/hash algorithm은 계속 Phase 00 ADR 소유다.

### F-P03-008 — Security/observability/redaction boundary가 Phase-local contract에 없었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Source는 [Integrated §19~§20](../../deprecated/architecture-domain-implementation-design.md#19-configuration-provenance와-observability)과 [Plan §13](../master-realization-plan.md#13-위험-보안-운영-관측과-재현성)에서 fingerprints/correlation, PII/secret redaction과 elapsed 비의미성을 요구하지만 target v1.0에는 Phase-local safe field/redaction/telemetry ownership이 없었다.
- **Correction:** Core는 logger/tracer/clock/env/raw input을 의미 입력으로 읽지 않는다. Typed result/evidence만 외부 telemetry 입력이 되며 raw address/input/external IDs/secret/locator/exception payload를 노출하지 않는다. Elapsed/thread/cache order는 identity/objective에 들어가지 않는다.
- **Applied:** [Phase 03 §11.3, §12, §15](../phases/phase-03-route-propagation-evaluation-kernel.md)에 safe fields, 금지 fields, evidence와 traceability를 추가했다.
- **Residual:** 실제 adapter redaction/authorization은 Phase 07/08 이후 evidence 대상이다.

### F-P03-009 — Boundary table과 independent oracle가 실제 결함을 잡는다는 sensitivity evidence가 약했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0의 “`rejectsOneUnitBeyondEveryHardBound`”는 exact row 목록이 없었고 red 단계 대부분이 type 부재 compile failure였다. Compile red는 hand/exhaustive/BigInteger/comparator oracle이 partial arc, reverse lookup, wrap 또는 wrong objective order를 검출한다는 증거가 아니다.
- **Correction:** Weight/volume lower·upper, window/due/plan/work arc/stop/drive rows를 명시하고 repeated/overnight/final-load boundary를 추가한다. Faulty test doubles로 partial arc, stale work window, reverse lookup, wrap와 comparator-order defect를 각각 assertion red로 검출해야 한다.
- **Applied:** [Phase 03 §9.3~§9.4, §11](../phases/phase-03-route-propagation-evaluation-kernel.md)에 exact rows, `Phase03OracleSensitivityTest`, red report와 minimal counterexample 요구를 반영했다.
- **Residual:** 아직 test/source가 없으므로 sensitivity evidence는 `NOT_PRODUCED`다.

### F-P03-010 — OPEN facet type 이름/placement가 package cycle을 암시했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 tree에는 `evaluation.api/DomainFacetProvider.java`가 있었지만 `PropagationDeclaration`은 정의되지 않은 `BoundFacetProvider<?>`를 참조했다. `evaluation.api`도 `RouteFacts`를 소비하므로 그대로 구현하면 `propagation ↔ evaluation.api` package cycle 위험이 있었다.
- **Correction:** OPEN seam 이름을 `DomainFacetProvider`로 일치시키고 propagation-owned stable SPI로 두며 `evaluation.api`가 소비하는 단방향을 명시한다. `ADR-004` 전 base acceptance는 empty set만 허용한다.
- **Applied:** [Phase 03 §6~§7.3, §9.3, §13](../phases/phase-03-route-propagation-evaluation-kernel.md)에 tree/signature/dependency rule과 architecture test를 반영했다.
- **Residual:** Facet shape/version/positive implementation은 계속 `OPEN/PROPOSED`; 임의 승인하지 않았다.

### F-P03-011 — Actual Phase 문서 inventory와 Phase 04 handoff label이 stale했다

- **Severity/status:** `LOW — APPLIED`
- **Exact evidence:** Target v1.0은 Phase 04 상세가 없고 `planned`라고 했지만 checkout에는 Phase 00~14 상세 15개가 모두 존재한다.
- **Correction:** 존재와 acceptance를 분리해 actual-but-unaccepted로 표시하고 review/evidence 부재를 보존한다.
- **Applied:** [Phase 03 §5, WP-03.0, §14.2](../phases/phase-03-route-propagation-evaluation-kernel.md)에 inventory/link/status를 정정했다.
- **Residual:** 모든 인접 Phase acceptance는 여전히 별도 review/evidence gate다.

## 5. 축별 판정

| Review axis | 판정 | 근거/남은 조건 |
|---|---|---|
| 사용자 고정 authority, `REVIEW` 비중단 | `PASS` | 권위 순서와 provenance 보존 |
| OPEN/GATED/deferred/official 값 비확정 | `PASS` | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, rotation/public schema gate 유지 |
| Propagation source invariant/boundary | `PASS AFTER FIX` | Phase 02 exact identity only, raw/reverse/lazy fallback 금지 |
| Full-arc/time/load/resource boundary | `PASS AFTER FIX` | Stale work-window 상태 제거, exact boundary rows 추가 |
| Phase 02 immutable authority handoff | `PASS DOCUMENT CONTRACT` | Actual accepted artifact/evidence 전 implementation은 blocked |
| Phase 04 binding handoff | `PASS WITH RESIDUAL` | Existing declaration materialization은 명확; solution evaluator/tie gap은 미해소 |
| Dependency/package/module direction | `PASS AFTER FIX` | Facet/test-fixtures cycle 방지; Phase 00 actual reactor 필요 |
| State/identity/lifecycle/immutability | `PASS AFTER FIX` | Result identity와 defensive-copy tests 추가 |
| Hard infeasible 대 invalid/corruption/overflow | `PASS AFTER FIX` | Stable taxonomy/precedence와 extension invalid boundary 추가 |
| Test command false-green 방지 | `PASS DOCUMENT CONTRACT` | Wrapper/reactor/test report가 아직 없어 evidence는 없음 |
| Fixture/oracle independence와 defect detection | `PASS DOCUMENT PLAN` | Seeded sensitivity가 required; actual result `NOT_PRODUCED` |
| Official numeric/default leakage | `PASS` | §9.2 숫자는 explicit test-only; Win decimal fixture official 사용 차단 |
| Java 25/Maven consistency | `PASS INVENTORY / BLOCKED IMPLEMENTATION` | Java 25.0.3/Maven 3.9.14 확인; target module/type는 아직 없음 |
| Rollback/failure/corruption | `PASS` | WP별 last safe point와 invalid fail-closed 보존 |
| Security/observability/reproducibility | `PASS AFTER FIX` | Safe fields/redaction/elapsed 비의미성 추가; actual evidence 없음 |
| Overlap/gap | `CHANGES_REQUIRED` | F-P03-004, F-P03-006 cross-phase blocker |
| Link/traceability/status/evidence truth | `PASS AFTER FIX` | Actual/planned drift 수정, 존재를 acceptance로 오인하지 않음 |

## 6. Applied fixes와 residual handoff

### 6.1 Applied safe/obvious fixes — 9

1. Canonical metric → composed hard → score/objective 순서.
2. Full-arc work-window selection과 stale-state 제거.
3. Fail-closed Maven/Surefire command, fresh exact method manifest.
4. `Invalid` extension arithmetic/component boundary와 failure precedence.
5. Rejection/failure exact identity와 collection immutability tests.
6. Safe security/observability/redaction/reproducibility fields.
7. Exact boundary rows와 seeded oracle sensitivity.
8. Facet naming/placement와 package/test-fixtures acyclic direction.
9. Actual Phase inventory와 Phase 04 handoff status.

### 6.2 Residual blockers — 2

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Solution-level evaluation owner/API/identity gap | Core/Evaluation + Capability/Profile + Phase 05 | Route-only kernel; no route-vector summing/ad hoc candidate evaluator | Exact solution contract + Phase 03~05 compile/full-equality/corruption review |
| Business equality 대 solution/insertion tie boundary | Core/Evaluation + Phase 05/06 Algorithm | Ordered objective comparison; context tie는 non-tied result를 못 뒤집음 | Equality/total-order APIs, owner/order/fingerprint + exhaustive comparator/insertion review |

별도 implementation blockers는 Phase 00~02 acceptance/evidence, scheduler task/owners, target reactor와 Phase 03 evidence 부재다. `Q-BENCH-02`, Win decimal fixture, `C-17`, `Q-VAR-01`, rotation과 public schema는 기존 범위의 gate로 유지하며 위 두 contract gap과 섞어 닫지 않는다.

## 7. 검증 결과

| 검사 | 결과 | 관찰 |
|---|---|---|
| Non-empty/required structure | `PASS` | 두 파일 non-empty; Phase metadata/§1~§15, review metadata/§1~§7 존재 |
| Finding count/disposition | `PASS` | Exact finding 11; `APPLIED` 9, `RESIDUAL` 2; metadata count 일치 |
| Source fingerprint | `PASS` | Target metadata의 canonical/source 7개 SHA-256을 fresh `shasum -a 256`과 대조해 mismatch 0 |
| Local Markdown file/anchor | `PASS` | 두 파일의 local `.md` link 64개를 target file + heading/explicit ID로 검사해 broken 0 |
| Fence/whitespace | `PASS` | Fence parity 두 파일 모두 0; trailing whitespace 0 |
| False-green/reciprocal hash guard | `PASS` | `failIfNoSpecifiedTests=false` 0; Phase 02/04/05/07 또는 Phase 03↔review whole-file reciprocal hash 0 |
| Status/evidence truth | `PASS` | Phase detail 15개와 final review inventory 재확인; implementation/evidence `NOT_STARTED/NOT_PRODUCED` 유지 |
| Git whitespace check | `PASS` | Standard `git diff --check -- <two files>`와 untracked-aware `git diff --no-index --check /dev/null <file>` 모두 whitespace error 0 |
| Allowed edit scope | `PASS` | Reviewer action log의 write target은 Phase 03 상세와 이 review 두 파일뿐. 다른 Phase/canonical/Java/POM/status 문서는 read-only |

`docs/implementation/`은 review 시작 전부터 Git 기준 untracked였고 final validation 중 다른 Phase 00~02 review 파일이 공유 workspace에 추가됐다. 그래서 `git status`만으로 author별 diff를 분리할 수 없으며, concurrent 파일은 read-only로 상태만 다시 확인했다.

Actual Java/Maven inventory에는 read-only `java -version`, `mvn -version`, POM/source listing을 사용했다. Target `./mvnw`, `rpdptw/core`, `build/test-fixtures`, Phase 03 source/test가 아직 없으므로 Maven build/test를 실행하거나 구현/evidence `PASS`로 기록하지 않았다.

## ALNS-first direction revision addendum

Task `019fa901-8776-7f61-b467-a8c6595b970d`에서 vendor-neutral evaluation kernel이
ALNS-only 경로에 남고 C-17 branch는 Phase 06/07/08 acceptance와 Phase 14A
benchmark acceptance 뒤에만 열리는지 검토했다. 기존 review verdict,
implementation, acceptance와 evidence 상태는 변하지 않는다.
