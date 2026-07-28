# Phase 06 독립 리뷰 — COW ALNS와 재현성

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "06"
review_date: 2026-07-28
reviewer_role: independent Phase 06 reviewer
target_document: docs/implementation/phases/phase-06-cow-alns-reproducibility.md
target_document_version_after_safe_fixes: 1.1
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
document_verdict: CHANGES_REQUIRED
implementation_entry_verdict: BLOCKED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
finding_counts:
  critical: 0
  high: 5
  medium: 5
  low: 1
  total: 11
finding_disposition:
  applied_safe_obvious: 9
  residual_cross_phase_blocker: 2
```

## 1. 결론

Phase 06 상세 문서는 changed-route first-write COW, immutable `current`/`stageBest`/`solveBest`, proposal-only destroy, exact repair delegation, completed-step, adaptive/acceptance/temperature, namespaced RNG, replay oracle와 Phase 07 handoff를 폭넓게 추적한다. 리뷰 중 상위 권위와 인접 계약으로 답이 명백한 9건은 [Phase 06 v1.1](../phases/phase-06-cow-alns-reproducibility.md)에 직접 정정했다.

문서 verdict는 `CHANGES_REQUIRED`다. 다음 두 cross-phase 계약을 Phase 06 문서만 고쳐 임의 확정할 수 없기 때문이다.

1. 중앙 pair-removal editor의 구현/module/API/evidence owner가 Master Realization/Integrated Phase 05와 actual Phase 05/06 사이에서 다르다.
2. Actual [Phase 03 review](phase-03-review.md)의 residual인 solution-level full evaluation owner/API와 business objective equality 대 context tie 경계가 Phase 06 guard/current/best/replay 입력까지 닫히지 않았다.

Implementation entry도 별개로 `BLOCKED`다. Phase 00~05 accepted artifact/evidence, 위 두 contract, explicit algorithm/acceptance/termination/seed config, scheduler task/owners와 target reactor가 없다. 문서 review, future test 이름 또는 기존 placeholder의 실행 가능성은 Phase 06 구현·evidence·acceptance가 아니다.

## 2. 검토 source와 관찰 baseline

### 2.1 권위 source

| Source | 검토 범위 | 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §4.1~§4.6, §9~§13, §15.5~§15.7, §16~§17 | Stable state, ALNS step, guard/best, COW, termination, replay와 `RM-4` |
| [Final Domain](../../2026-07-26-domain-design.md) | §8, §10~§11, §16, §17.5~§17.6, §17.9, §18 | Route/bank partition, no-alias state, operator/result, fault와 replay |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2.1~§2.7, §3.1~§3.6, §5.2~§5.6, §6 | Java 25/Maven module DAG, completed-step, retry identity, verifier 분리와 evidence |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | §2~§3, §9~§10, §19~§25 | 15 Phase 배치, Phase 05/06 ownership, provenance/security/failure/test/anti-pattern |
| [Open-question register](../../master-design-open-questions.md) | Exact 28개 `Q-*` 행과 상태 요약 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1` 보존 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§15, 특히 Phase 03~07 | Current inventory, Phase gate, test/evidence, rollback/restart와 traceability |

`REVIEW` metadata는 사용자 고정 source authority 아래 provenance로 보존했으며 리뷰를 중단하지 않았다. `Q-BENCH-02` 공식 수치, current Win decimal `D/U`, `C-17`, `Q-VAR-01`, multi-trip/rotation과 public schema를 임의 해소하거나 hidden default로 채우지 않았다. Final Domain/Architecture의 오래된 `Q-INFRA-01 DEFERRED`, `25/1/2`는 current authority로 사용하지 않았다.

### 2.2 인접 Phase와 actual Java 25/Maven inventory

[Phase 05](../phases/phase-05-pair-insertion-initial-portfolio.md)와 [Phase 07](../phases/phase-07-independent-verification-final-result.md)은 handoff·overlap·gap 확인에만 read-only로 사용했다. 인접 파일을 수정하거나 whole-file reciprocal hash를 만들지 않았다. Final validation 중 두 인접 상세와 Phase 05 review가 다른 owner에 의해 갱신되어 최신 section을 다시 대조했다. Phase 06 v1.1은 인접 whole-file hash를 제거하고 직접 읽은 section/status만 기록한다. 인접 문서의 Phase 06 named-section receipt는 이 리뷰에서 수정하지 않으며 accepted handoff 전에 해당 owner가 current reviewed contract와 semantic diff를 재검증해야 한다.

Actual checkout 관찰은 다음과 같다.

| 항목 | 관찰 | 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`; `docs/implementation/` 전체가 Git 기준 untracked | 기존 사용자 문서 작업으로 보존; 허용된 두 파일 외 수정 금지 |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Target Java 25와 일치하지만 Phase 06 build/replay evidence는 아님 |
| Maven | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT`; release 25; module 목록과 `./mvnw` 없음 | `rpdptw-solver`/architecture boundary와 future exact command는 아직 구현 전 |
| Dependency | Google Workflow/Storage, Jackson, JUnit가 root classpath에 함께 있음 | Target solver/provider 격리 전 legacy placeholder |
| Main/test source | Main Java 6개, test 1개 | `AlnsBatchEngine`의 `SplittableRandom`, `double` 합성 objective와 `Map<String,Object>`는 COW ALNS/replay evidence가 아님 |
| Phase/review inventory | **Live:** Phase 상세 15개와 독립 review 15/15 actual; review lifecycle은 모두 `COMPLETE` 또는 `FINAL`. Verdict는 `CHANGES_REQUIRED` 11개(Phase 03~12, 14), `PASS_WITH_RESIDUAL_BLOCKERS` 2개(Phase 00, 13), `ACCEPTED_WITH_APPLIED_CORRECTIONS` 1개(Phase 01), `PASS_AFTER_APPLIED_CORRECTIONS` 1개(Phase 02). **Historical authoring snapshot:** review 00~03은 시작 전 존재했고 `APPEARED_DURING_AUTHORING`은 review 04~07이었다. | Historical snapshot은 provenance로만 보존; document verdict와 implementation/evidence/Phase acceptance를 분리하며 Phase 06 entry는 계속 `BLOCKED` |
| Accepted evidence | Phase 00~14 accepted bundle 없음 | Target phase accepted completion 0; Phase 06 entry `BLOCKED` |

`SUPERSEDED` master와 `docs/codex/*`는 current authority나 구현 evidence로 사용하지 않았다.

## 3. Severity 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 안전/권위/상태를 즉시 잘못 확정하거나 회복 곤란한 corruption을 허용 |
| `HIGH` | Canonical 의미 위반, false-green gate 또는 핵심 cross-phase owner/API gap으로 구현·acceptance를 막음 |
| `MEDIUM` | Lifecycle/replay/handoff/security/evidence가 불완전해 결함 탐지·재현·독립 검증을 약화 |
| `LOW` | 상태/metadata/fingerprint drift로 구현 의미를 직접 바꾸지는 않음 |

## 4. Findings

### F-P06-001 — Stage guard 실패 후보가 `stageBest`/`solveBest`로 승격될 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0 §8.2는 stage guard 실패 시 `decision := REJECT`로 두었지만 곧바로 모든 `completedTrial`에 `strictlyBest(before.stageBest, completedTrial)`와 `strictlyBest(before.solveBest, completedTrial)`를 적용했다. [Master §11.4](../../master-design.md#114-alns-step)는 선행 objective guard를 통과하지 못한 후보가 하위 목표 개선만으로 current/best가 될 수 없다고 고정한다.
- **Correction:** Guard 결과를 best eligibility로도 사용하고, 실패 후보는 정상 `REJECTED` completed step일 수 있어도 세 solution slot을 모두 보존해야 한다.
- **Applied:** [Phase 06 §3, §8.1~§8.2, §11.1, §14.1](../phases/phase-06-cow-alns-reproducibility.md)에 `guardPassed`, guarded best update와 `stageGuardFailureCannotUpdateCurrentStageBestOrSolveBest()`를 추가했다.
- **Residual:** Exact comparator API는 F-P06-004가 닫힐 때까지 cross-phase blocker다. Guard 실패의 비승격 의미는 더 이상 open이 아니다.

### F-P06-002 — Selected Maven command가 test 부재를 성공으로 통과시킬 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0의 WP-06.1~7 selected command는 `-am`과 `surefire/failsafe.failIfNoSpecifiedTests=false`를 사용했다. Target class 오타·누락이어도 Maven exit code 0이 가능하며, root에는 현재 `rpdptw/solver`와 wrapper가 없어 명령을 실제 evidence로 실행할 수도 없다.
- **Correction:** Phase 00 accepted wrapper 뒤 selected solver command는 `-am` 없이 target module에 `failIfNoSpecifiedTests=true clean test/verify`를 적용한다. Architecture는 module 전체 `clean verify`를 실행하고, 모든 command 뒤 fresh Surefire/Failsafe XML에서 exact class/method를 fail-closed 대조한다.
- **Applied:** [Phase 06 WP-06.1~7와 §13](../phases/phase-06-cow-alns-reproducibility.md#12-ordered-work-packages)에 pinned wrapper command, fresh report manifest와 false-green 금지를 반영했다.
- **Residual:** `./mvnw`, target reactor/test/report가 아직 없으므로 command는 future red이며 evidence는 `NOT_PRODUCED`다.

### F-P06-003 — 중앙 pair-removal editor의 Phase owner가 권위 문서 사이에서 충돌한다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** [Master Realization Phase 05](../master-realization-plan.md#phase-05--pickup-delivery-pair-삽입과-초기-후보군)는 중앙 atomic editor를 Phase 05 산출물로 요구하고 [Integrated §9.7](../../architecture-domain-implementation-design.md#97-destroy-contract)도 Phase 5 gate에 둔다. 반면 actual [Phase 05 §2.3/§15.2](../phases/phase-05-pair-insertion-initial-portfolio.md#152-next--actual-but-unaccepted-phase-06)는 construction-only transition만 소유하고 central removal/COW를 Phase 06에 넘긴다. Target v1.0은 이를 `FIXED` Phase 06 owner로 단정했다.
- **Correction required:** Phase 05/06/Architecture reviewers가 central editor의 module/API, construction transition과의 차이, compile direction, contract test와 `E-P05-PAIR`/`E-P06-*` evidence owner를 하나로 승인해야 한다. 중복 editor와 owner 공백을 모두 금지해야 한다.
- **Applied:** [Phase 06 §2~§6, WP-06.2, §14~§17](../phases/phase-06-cow-alns-reproducibility.md)에 의미는 고정하되 implementation owner를 `CROSS-PHASE OWNER CONFLICT — BLOCKED`로 바꾸고 conditional tree/task를 기록했다.
- **Residual/owner/last safe/restart:** Owner는 Phase 05/06 Pair/Algorithm + Architecture다. Last safe point는 ordered proposal + central pair edit semantics와 no duplicate implementation이다. 공동 owner/API/evidence review 승인 뒤 WP-06.2를 재개한다.

### F-P06-004 — Full solution evaluation과 comparator/tie seam이 upstream에서 닫히지 않았다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target은 Phase 03/04 `fullEvaluate`, stage guard, `strictlyBest`와 `BoundComparator`를 Phase 06 권위로 소비한다. [Phase 03 review F-P03-004](phase-03-review.md#f-p03-004--full-solution-evaluation-authority가-phase-0305-사이에서-비어-있다)와 [F-P03-006](phase-03-review.md#f-p03-006--business-objective-equality와-context-stable-tie의-api-경계가-닫히지-않았다), final validation 중 actual이 된 [Phase 05 review F-P05-001](phase-05-review.md#f-p05-001--full-solution-evaluator와-ranking-authority가-phase-0305-사이에서-비어-있다)과 [F-P05-005](phase-05-review.md#f-p05-005--business-equality와-insertion-stable-tie의-apiencoding-경계가-닫히지-않았다)는 solution-level evaluation owner/API/identity와 business equality 대 context tie를 residual blocker로 남겼다. Phase 05/06 candidate aggregation과 best/replay fingerprint는 이 두 계약 없이 구현할 수 없다.
- **Correction required:** Phase 03 Core/Evaluation, Phase 04 Profile과 Phase 05/06 Algorithm이 ordered routes+bank full evaluation, invalidation/fingerprint, comparator equality와 insertion/solution tie owner/order를 승인해야 한다.
- **Applied:** [Phase 06 §4와 §15](../phases/phase-06-cow-alns-reproducibility.md)에 explicit contract gate, last safe point와 restart condition을 추가했다.
- **Residual/owner/last safe/restart:** Owner는 Phase 03~06이다. Last safe point는 route-only kernel, ordered business objective first, ad hoc aggregation/Big-M/context tie reversal 금지다. Phase 03 `F-P03-004/006`의 cross-phase API/test 승인 뒤 구현 contract를 freeze한다.

### F-P06-005 — Phase 07 handoff에 authority content digest와 accepted-review receipt가 빠져 있었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §16.2는 authority fingerprint와 `E-P06-*`만 열거했다. Actual [Phase 07 §7.3/§15.1](../phases/phase-07-independent-verification-final-result.md#73-actual-phase-06-handoff-최소-필드)은 fingerprint와 authority content digest, accepted predecessor bundle/review ref를 요구한다. Fingerprint 선언만으로 same identity/different bytes 또는 미승인 handoff를 차단할 수 없다.
- **Correction:** Candidate/replay에 authority content digest를 추가하고, scheduler가 연결한 accepted independent Phase 06 review receipt를 evidence handoff에 포함한다. Review와 evidence/detail whole-file hash의 reciprocal digest는 만들지 않는다.
- **Applied:** [Phase 06 §7.1과 §16.2](../phases/phase-06-cow-alns-reproducibility.md#162-next--actual-but-unaccepted-phase-07)에 content digest, non-self-referential review receipt와 reciprocal-hash 금지를 반영했다.
- **Residual:** Phase 07의 named-section receipt는 이 리뷰 scope에서 수정하지 않았다. Phase 07 owner가 accepted handoff 전에 current v1.1 semantic projection을 재검토해야 한다.

### F-P06-006 — State publication 뒤 completed-step 기록처럼 읽히는 이중 전이가 있었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §8.1 transition은 `NEXT_STATE_PUBLISHED → COMPLETED_STEP_RECORDED`였지만 §3의 single-publication invariant와 pseudocode는 completed count, trace, adaptive/acceptance를 하나의 immutable next state로 만들겠다고 했다. Publication 뒤 counter write가 실패하면 candidate와 work accounting이 갈라질 수 있다.
- **Correction:** Completed-step/trace를 immutable `nextState`에 먼저 포함하고 그 aggregate를 한 번만 publish한다. `commit(completedTrial)`도 외부 publication처럼 읽히지 않도록 local immutable value freeze로 명확히 한다.
- **Applied:** [Phase 06 §8.1~§8.2](../phases/phase-06-cow-alns-reproducibility.md#8-state-transition-completed-step-pseudocode와-termination)의 state sequence와 pseudocode를 정정했다.
- **Residual:** 실제 atomic reference/state-store 구현과 fault injection evidence는 아직 없다.

### F-P06-007 — Platform `AttemptId`가 canonical trace에 들어갈 여지가 있었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target은 seed derivation에서 `AttemptId`를 금지했지만 `DecisionTrace` identity를 “attempt/completed-step 경계”, oracle row를 “attempt/completedStep ordinal”로 표현했다. [Final Architecture §3.6](../../2026-07-26-architecture-design.md#36-identity-idempotency-retry와-cancellation)은 retry가 `AttemptId`만 바꾸고 logical worker seed/config/warm start를 보존한다고 규정한다.
- **Correction:** Canonical trace/candidate는 logical run/step/completed-step만 포함한다. Platform attempt/thread/elapsed/provider execution은 별도 observation에 두고 retry 전후 trace/candidate exact equality를 검사한다.
- **Applied:** [Phase 06 §7.1, §7.5, §10.3, §11.1과 §17](../phases/phase-06-cow-alns-reproducibility.md)에 separation rule과 `retryAttemptIdDoesNotChangeCanonicalTraceOrCandidate()`를 추가했다.
- **Residual:** Seed mixing/canonical encoding version은 계속 proposed review gate다.

### F-P06-008 — Trace의 `IDs`가 raw external identifier를 허용할 수 있었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0은 trace에 “IDs/digests/version”을 허용하면서 raw address/input/secret만 금지했다. [Integrated §20](../../architecture-domain-implementation-design.md#20-security와-tenant-boundary)은 raw PII/full input 배제를 요구하고, raw external request/vehicle/customer ID가 tenant/PII일 수 있는 경우를 구분하지 않았다. 또한 semantic trace와 운영 correlation/attempt metadata의 경계가 한 곳에 정리돼 있지 않았다.
- **Correction:** Canonical trace identifier를 dense/internal safe ID, logical ordinal, version/digest로 제한한다. Raw external identifiers와 arbitrary exception payload를 배제하고 typed observation metadata를 semantic fingerprint 밖에 둔다.
- **Applied:** [Phase 06 §11.1~§11.2와 §17](../phases/phase-06-cow-alns-reproducibility.md#11-exact-test-plan)에 trace allowlist/observation rule과 redaction test를 추가했다.
- **Residual:** Tenant authorization, encryption과 provider IAM은 Phase 08/11/14 책임이며 solver가 구현하지 않는다.

### F-P06-009 — Source fingerprint, actual inventory와 인접 whole-file hash가 stale했다

- **Severity/status:** `LOW — APPLIED`
- **Exact evidence:** Target v1.0 metadata의 Phase 03/05 hash는 fresh SHA-256과 달랐고 Phase 05/07 whole-file hash는 인접 문서 authoring drift를 만든다. 당시 §5는 Phase 상세가 00~07 일부만 actual이라고 했지만 checkout에는 00~14 상세 15개와 review 00~03이 존재했고, 이후 historical `APPEARED_DURING_AUTHORING` snapshot으로 review 04~07을 관찰했다. 현재 live inventory는 독립 review 15/15 완료와 각 document verdict를 별도로 기록한다.
- **Correction:** Canonical source hash는 fresh 대조하고 current Phase 03 hash를 갱신한다. Phase 05/07은 직접 읽은 section과 read-only 상태만 기록하며 whole-file hash를 제거한다. Actual inventory는 존재와 acceptance를 분리해 갱신한다.
- **Applied:** [Phase 06 metadata와 §5](../phases/phase-06-cow-alns-reproducibility.md)에 v1.1, `OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH`와 actual inventory를 반영했다.
- **Residual:** 인접 문서의 own metadata는 read-only scope로 수정하지 않았다.

### F-P06-010 — Global-best candidate가 세 solution slot의 같은 top-level handle로 alias될 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact evidence:** Target v1.0과 첫 정정 pseudocode는 한 `candidateSnapshot`을 `nextCurrent`에 넣고 `strictlyBest`가 같은 object를 `nextStageBest`/`nextSolveBest`로 반환할 수 있었다. 문서 §3과 [Final Domain §11](../../2026-07-26-domain-design.md#11-alns-state와-operator)은 세 top-level snapshot이 서로 alias되지 않아야 한다. Constructor의 “distinct handles” comment만으로 이미 같은 reference를 받은 세 변수를 분리할 수 없다.
- **Correction:** 비교는 immutable content를 선택하고, 같은 candidate content가 여러 slot을 이겨도 `CURRENT`/`STAGE_BEST`/`SOLVE_BEST`별 pairwise-distinct immutable handle을 만든 뒤 next state를 구성한다.
- **Applied:** [Phase 06 §8.2와 §11.1](../phases/phase-06-cow-alns-reproducibility.md#82-canonical-alns-step-pseudocode)에 `strictlyBestContent`, slot별 `distinctSlotRef`, pairwise assertion과 `globalBestImprovementPublishesDistinctSlotHandlesForSameContent()`를 추가했다.
- **Residual:** Actual Java record constructor/handle factory와 identity probe evidence는 아직 없다.

### F-P06-011 — Cooperative interruption probe와 publication race의 linearization point가 pseudocode에 없었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target v1.0 §8.2는 step 시작과 bounded improvement 뒤에서만 interruption을 확인했지만 §11.1은 “모든 lifecycle boundary” cancel/watchdog discard를 요구했다. Operator/repair/full evaluation 뒤 또는 next state 파생과 publication 사이 signal의 처리가 정의되지 않으면 incomplete step이 publish되거나 이미 완료된 step을 소급 취소할 수 있다.
- **Correction:** Named semantic boundary마다 cooperative probe하고, final pre-publication probe와 atomic single publication을 linearization boundary로 정의한다. Publish 전 signal은 entire state unchanged, publish 후 signal은 완료 step을 보존하고 다음 boundary의 exceptional termination이 된다.
- **Applied:** [Phase 06 §8.1~§8.2, §11.1과 §14.1](../phases/phase-06-cow-alns-reproducibility.md#8-state-transition-completed-step-pseudocode와-termination)에 boundary probes, bounded-work probe, pre-publication rule과 `interruptionRaceUsesPublicationAsSingleLinearizationPoint()`를 추가했다.
- **Residual:** Actual interruption primitive와 publication recorder/fault-injection evidence는 아직 없다.

## 5. 축별 판정

| Review axis | 판정 | 근거/남은 조건 |
|---|---|---|
| 사용자 고정 authority, `REVIEW` 비중단 | `PASS` | Authority 순서와 provenance 보존 |
| OPEN/GATED/deferred/official 값 비확정 | `PASS` | `Q-BENCH-02`, current Win decimal `D/U`, `C-17`, `Q-VAR-01`, rotation/public schema gate 유지 |
| COW/no-alias/first-write copy | `PASS DOCUMENT CONTRACT` | Changed route unique first-write copy, independent bank, owner graph/copy counter와 discard tests 명시 |
| Immutable current/stageBest/solveBest | `PASS AFTER FIX` | Same-content global best도 pairwise-distinct handles, single aggregate publication, stage-guard failure 비승격 |
| Destroy/repair ownership | `CHANGES_REQUIRED` | Atomic semantics는 명확하나 F-P06-003 owner/API/evidence 미해소 |
| Acceptance/adaptive/temperature/termination | `PASS AFTER FIX` | Explicit config, guard-before-acceptance, rejected completed, invalid/interrupted non-advance, exceptional 분리 |
| Incomplete-step semantics | `PASS AFTER FIX` | Named probes, pre-publication linearization, publish 전에 completed-step/trace/update를 next state에 원자 구성 |
| Namespaced RNG/replay/reproducibility | `PASS AFTER FIX` | Purpose streams, stable order, independent full-copy/seed/canonicalizer oracle, AttemptId separation |
| Candidate/evidence handoff to verification | `PASS AFTER FIX WITH OWNER REFRESH` | Authority content digest와 accepted review receipt 보강; Phase 07 projection refresh 필요 |
| Official numeric default leakage | `PASS` | Legacy/test/experiment/official 상태 분리, omission bind failure |
| Java 25/Maven/dependency | `PASS INVENTORY / BLOCKED IMPLEMENTATION` | Java 25.0.3/Maven 3.9.14 확인; target module/wrapper 없음 |
| Executable test/oracle/command/evidence plan | `PASS AFTER FIX / NOT_PRODUCED` | Fail-closed selected command, fresh report manifest, independent oracle와 exact method matrix; 실제 source/report 없음 |
| Failure/timeout/corruption/rollback | `PASS DOCUMENT PLAN` | Typed fault/termination, every-boundary discard, replay corruption와 WP rollback point |
| Security/observability | `PASS AFTER FIX` | Safe internal trace IDs, redaction, semantic/observation separation; provider IAM은 downstream |
| Overlap/gap | `CHANGES_REQUIRED` | F-P06-003/004 residual cross-phase blockers |
| Link/traceability/status/evidence truth | `PASS AFTER FIX` | Actual inventory, source/requirement/test/evidence와 no-overclaim 유지 |

## 6. Applied fixes와 residual handoff

### 6.1 Applied safe/obvious fixes — 9

1. Stage guard failure의 current/stageBest/solveBest 비승격과 exact test.
2. Fail-closed wrapper commands, `clean`, fresh Surefire/Failsafe required-method manifest.
3. Phase 07 authority content digest와 non-reciprocal accepted-review receipt.
4. Completed-step/trace/adaptive/acceptance를 한 immutable state로 단일 publication.
5. Platform `AttemptId`와 canonical trace/candidate 분리 및 retry equality test.
6. Safe internal trace identifier와 observation/redaction boundary.
7. Source fingerprint, actual inventory와 인접 whole-file reciprocal hash 제거.
8. Global-best same-content에서도 current/stageBest/solveBest top-level handle 분리.
9. 모든 named lifecycle boundary의 cooperative probe와 publication race linearization.

### 6.2 Residual blockers — 2

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Central pair-removal editor Phase owner/API/evidence | Phase 05/06 Pair/Algorithm + Architecture | Ordered proposal + central pair edit semantics; no duplicate implementation | Master Realization/Integrated Phase 05와 actual Phase 05/06의 공동 owner/API/evidence review |
| Solution-level evaluation과 business equality/context tie | Phase 03 Core/Evaluation + Phase 04 Profile + Phase 05/06 Algorithm | Route-only kernel, objective-first ordering; no ad hoc aggregation/Big-M/tie reversal | Phase 03 `F-P03-004/006` exact solution API/fingerprint/tie contract와 cross-phase tests 승인 |

별도 implementation blockers는 Phase 00~05 acceptance/evidence, scheduler task/owners, target reactor, explicit algorithm/operator/acceptance/termination/seed config와 actual Phase 06 source/test/evidence 부재다. `Q-BENCH-02`, Win decimal fixture, `C-17`, `Q-VAR-01`, rotation과 public schema gate는 위 두 contract gap과 섞어 닫지 않는다.

## 7. 검증 결과

| 검사 | 결과 | 관찰 |
|---|---|---|
| Non-empty/required structure | `PASS` | 두 파일 non-empty; Phase metadata/§1~§17, review metadata/§1~§7 존재 |
| Finding count/disposition | `PASS` | Exact finding 11; `APPLIED` 9, `RESIDUAL` 2; metadata count 일치 |
| Source fingerprint | `PASS` | Phase metadata의 canonical/source SHA-256과 fresh `shasum -a 256` mismatch 0; 인접 whole-file hash 0 |
| Local Markdown file/anchor | `PASS` | 두 파일 local `.md` link target/heading 검사에서 broken 0 |
| Fence/whitespace | `PASS` | Fence parity 정상; trailing whitespace 0 |
| False-green/reciprocal hash guard | `PASS` | Executable command의 `failIfNoSpecifiedTests=false` 0; Phase 05/07 whole-file hash와 Phase 06↔review whole-file reciprocal hash 0 |
| Status/evidence truth | `PASS` | Implementation/evidence `NOT_STARTED/NOT_PRODUCED`, entry `BLOCKED` 유지 |
| Git whitespace check | `PASS` | Untracked-aware `git diff --no-index --check /dev/null <file>` 두 파일 모두 whitespace error 0 |
| Allowed edit scope | `PASS` | Reviewer write target은 Phase 06 상세와 이 review 두 파일뿐. Phase 05/07/canonical/Java/POM/status 문서는 read-only |

Actual target `./mvnw`, `rpdptw/solver`, Phase 06 source/test와 evidence bundle이 아직 없으므로 Maven build/test를 실행하거나 future method/oracle을 `PASS` evidence로 기록하지 않았다. Read-only `java -version`, `mvn -version`, POM/source listing과 hash/link/Markdown 검사를 실제 checkout inventory로 사용했다.
