# Phase 13 독립 문서 리뷰 — Optional hybrid route selection

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "13"
review_date: 2026-07-28
review_timezone: Asia/Seoul
reviewer_role: independent Phase 13 documentation reviewer
target_document: docs/implementation/phases/phase-13-optional-hybrid-route-selection.md
target_document_version_after_safe_fixes: 1.5
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
document_verdict: PASS_WITH_RESIDUAL_BLOCKERS
phase_acceptance_verdict: GATED_NOT_STARTED_NOT_ACCEPTED
activation_status_observed: C17_GATE_CLOSED
production_authority_observed: NOT_GRANTED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
alns_benchmark_acceptance_status_observed: NOT_PRODUCED
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_verdict: PASS_DOCUMENTATION_ONLY_GATE_REMAINS_CLOSED
scheduler_skip_receipt_status_observed: NOT_PRODUCED
signed_applicability_receipt_status_observed: NOT_PRODUCED
signing_trust_policy_status_observed: OPEN_GATED_NOT_APPROVED
scheduler_status_change: NOT_AUTHORIZED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
neighbor_validation_policy: LATEST_NAMED_CONTRACT_AND_BLOCKER_SECTIONS_READ_ONCE
whole_file_or_section_digest_acceptance: NOT_USED
finding_counts:
  critical: 0
  high: 4
  medium: 2
  low: 1
  total_non_resolved: 7
  resolved: 1
  total_recorded: 8
finding_status_counts:
  resolved_or_applied_without_active_finding_residual: 6
  active_residual: 2
finding_disposition:
  applied_safe_obvious: 5
  resolved_cross_phase_alignment: 1
  applied_with_residual_authority: 1
  residual_only_cross_phase_or_external: 1
fake_evidence_detected: false
code_change_reviewed: false
code_change_made: false
backend_policy_change_review: OR_TOOLS_DIRECT_CP_SAT_DOCUMENT_CONTRACT_ONLY
backend_policy_cross_validation_verdict: PASS_NO_ACTIONABLE_FINDINGS
```

## 1. Scope와 결론

이 review는 [Phase 13 대상 문서](../phases/phase-13-optional-hybrid-route-selection.md)의
원문 계약, C-17 gate, Phase 06/07/08/12/14 경계, test/evidence 검출력과 실제 checkout을
독립 대조했다. Java/POM/build/deployment, canonical source, Phase 12/14, progress/status와
다른 review는 수정하지 않았다. Worktree를 만들거나 코드 구현을 하지 않았다.

문서 verdict는 **`PASS_WITH_RESIDUAL_BLOCKERS`**다. Target 안에서 답이 안전하고
명백한 6건은 v1.2에 직접 정정했고, 최종 live-status/reciprocal 정합성 교정은
v1.3에 반영했다. v1.4는 사용자가 확정한 backend 정책을 Google OR-Tools direct
CP-SAT로 전환했으며 구현 상태를 올리지 않았다. v1.5는 ALNS benchmark acceptance를
Phase 13의 선행 gate로 추가했으며 구현 상태를 올리지 않았다. 특히 다음 의미를 고정했다.

- Target H1은 canonical `Phase 13 — Optional hybrid`이며 route selection은 부제다.
- Phase 12 provider substitution은 Phase 13의 보편 predecessor가 아니다. Approved
  hybrid scope가 substituted provider/runtime을 실제 선택할 때만 해당 evidence를
  조건부로 요구한다.
- C-17 gate가 닫힌 canonical path는 scheduler가 Phase 13 assembly, solver, pool,
  provider를 resolve/load/invoke하지 않는 absence/no-op다.
- 닫힌 gate의 명시적 hybrid 요청은 Phase 13 allocation 전에
  `UNAUTHORIZED_HYBRID_ACTIVATION`으로 fail closed한다.
- Gate-closed handoff는 scheduler-owned skip receipt이며 Phase 13 artifact,
  `E-P13-*` 또는 Phase acceptance가 아니다.
- Skip/Activated applicability는 Phase 14 consumption 전에 separate signed envelope와
  action-time trust/validity/revocation/freshness verification을 요구한다.
- 인접 whole-file/section digest와 reciprocal fingerprint는 acceptance가 아니다.
- Phase 06/07/08 accepted evidence와 Phase 14A의 immutable ALNS benchmark acceptance
  receipt가 없으면 C-17 상태와 무관하게 Phase 13은 열리지 않는다.
- Phase 13이 나중에 활성화되면 exact backend는 direct Java CP-SAT다. Boolean
  route/unassigned와 integer/fixed-point model이므로 `MPSolver`를 쓰지 않는다.
- OR-Tools result는 selected IDs만 제공하며 fresh materialization/full
  evaluation/Phase 07/strictly-better adoption을 우회하지 않는다.

이 verdict는 Phase 13 구현, activation 또는 evidence acceptance가 아니다. `C-17`은
계속 닫혀 있고 target source/module/test/backend와 scheduler skip receipt도 없다.
따라서 Phase verdict는 `GATED_NOT_STARTED_NOT_ACCEPTED`, production authority는
`NOT_GRANTED`다.

Active residual finding은 2건이다. 첫째, Phase 03~07의 full-solution
evaluation/comparator 계약이 아직 닫히지 않았다. 둘째, applicability signature
algorithm/trust roots/revocation/time/freshness policy가 승인되지 않았고 실제 signed
envelope/verification receipt도 없다. Phase 12 v1.3 conditional bounded handoff,
Phase 13 signed applicability envelope/action-time verification과 Phase 14 consumer
seam 정렬로 과거 reciprocal dependency/hash drift는 해소됐다.

이 해소는 `C-17`, OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost, scheduler task,
accepted predecessor/evidence, signing/trust 승인 또는 실제 signed receipt를 닫지
않는다. 이를 target의 hidden default나 가짜 handoff로 덮지 않았다.

## 2. 완독·대조 source와 actual inventory

### 2.1 Current authority와 인접 계약

| Source | 직접 대조한 범위 | Phase 13 판정 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | 전체, 특히 §1~§4, §11.7~§11.10, §13~§17과 `C-17`/`RM-9A~C` | Optional/gated branch, exact pool/projection/materialization/full evaluation/strict adoption, 별도 scope 승인 |
| [Final Domain](../../2026-07-26-domain-design.md) | 전체, 특히 §7~§10, §12~§18 | Immutable route artifact, exact selection, typed fallback, independent acceptance |
| [Final Architecture](../../2026-07-26-architecture-design.md) | 전체, 특히 §2~§6 | OR-Tools-free ALNS-only dependency, direct CP-SAT/native lifecycle, verifier isolation |
| [Integrated design](../../architecture-domain-implementation-design.md) | 전체, 특히 §3, §10~§25, §27~§28 | Phase DAG, independent Phase 12 branch, Phase 13 optional branch와 Phase 14 conditional handoff |
| [Question register](../../master-design-open-questions.md) | 전체 28개 항목 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`; `C-17` 별도 gated |
| [Master Realization Plan](../master-realization-plan.md) | 전체, 특히 §2~§7 Phase 06/07/08/12/13/14와 §8~§15 | Phase 06→07→08→14A acceptance 뒤 optional Phase 13, Phase 10→12 독립 branch, Phase 14B의 conditional hybrid |
| [Phase 12](../phases/phase-12-provider-substitution.md) | 전체, 특히 §3, §14~§18.3 | `INFRASTRUCTURE_DECISION_INPUT_ONLY`; C-17/OR-Tools activation/hybrid/production authority를 부여하지 않음 |
| [Phase 14](../phases/phase-14-official-calibration-cutover.md) | 전체, 특히 §3.1, §5.3, §9.1~§9.2와 §14~§18 | Gate-closed ALNS-only skip, signed applicability/trust/revocation/action-time verification, gate-open accepted Phase 13와 자체 official/production gates |
| [Phase 00~14 reviews](./) | 15개 current review metadata와 relevant contract/blocker | `15/15` review 완료. Phase 12는 `CHANGES_REQUIRED/BLOCKED_NOT_IMPLEMENTED`; Phase 14는 `CHANGES_REQUIRED`, 14A `NOT_RUN/RECEIPT_NOT_PRODUCED`, 14B `NOT_STARTED/BLOCKED_NOT_READY`; review 완료와 implementation/phase acceptance 분리 |

Final Domain §18과 Final Architecture §6의 과거 `Q-INFRA-01 DEFERRED`,
`25/1/2`는 최신 Canonical Master/register의 `RESOLVED`, `26/1/1`로 해소했다.
AWS target 선택은 Phase 13 OR-Tools activation 또는 production authority가 아니다.

동시 batch 인접 변경은 최신 Phase 12/14 contract/blocker section을 한 번 읽고 semantic
compatibility를 판정한 뒤 반복 hash/status 추적을 중단했다. 인접 document/section digest를
entry, acceptance 또는 handoff evidence로 사용하지 않았다.

[SUPERSEDED Master](../../2026-07-26-master-design.md)와
`docs/codex`(repo 미존재 가능)는 historical regression cross-check에만 사용했다.
Historical master도 C-17을 절대 gate로 두며 verified ALNS/both-verifier baseline,
RM-9A~C와 scope/OR-Tools config/native/OSS-license/SBOM/security/operations/compute-cost/admission/fallback/rollback 승인 전 default path/backend dependency
활성화를 금지한다. Historical codex plan은 route pool/MIP를 당시 deferred 후속으로
두므로 current Phase numbering이나 authority로 사용하지 않았다.

### 2.2 Historical authoring-time Java 25/Maven/build/source/test/deployment snapshot

아래 Java/build 관찰은 2026-07-28 작성 당시 saved checkout snapshot이다. 현재 live
document/review status는 §2.1의 `15/15` 완료와 Phase 12/14 verdict로 별도 확인했으며,
이 historical code snapshot에서 review 또는 phase acceptance를 추론하지 않는다.

| 항목 | Actual 관찰 | Phase 13 해석 |
|---|---|---|
| Git | Branch `codex/domain-design`, HEAD `3424277c9c74f8151a83be056a07dd4659331beb`; `docs/implementation/` 전체가 기존 untracked | Shared user/scheduler 문서를 보존하고 허용된 두 파일만 write |
| Toolchain | Amazon Corretto `25.0.3`, Maven `3.9.14`, macOS aarch64 | Root enforcer/release 25와 일치; Phase 13 evidence는 아님 |
| Build | Root `pom.xml` 하나, shaded `com.ronext:ro-next` JAR, wrapper/child module 없음 | Proposed solver/hybrid/backend/architecture module 0 |
| Dependency | Google Workflow/Storage, Jackson, JUnit가 root classpath에 존재 | GCP placeholder; `com.google.ortools:ortools-java`와 CP-SAT adapter는 관찰되지 않음 |
| Main/test | Main Java 6개, test Java 1개 | Phase 13 gate/pool/projection/selector/hybrid source/test 0 |
| Placeholder algorithm | `AlnsBatchEngine`이 `SplittableRandom`, `double`, `Map<String,Object>`로 합성 objective 생성 | Accepted ALNS, route evaluation, pool 또는 hybrid baseline이 아님 |
| Current finalization | GCS prefix listing 뒤 보이는 raw objective 최솟값을 unconditional result object로 기록 | Declared completeness, both-gate, exact route selection 또는 official result authority가 아님 |
| Tracked deployment | Dockerfile, GCP Cloud Build/Workflow/guide | Legacy/current migration inventory; Phase 13 runtime evidence가 아님 |
| Tracked AWS/optimizer backend | AWS IaC source 0, route-selection vendor/backend source 0 | `Q-INFRA-01` 또는 C-17 implementation evidence가 아님 |
| Ignored artifacts | `target/`와 `.serverless/`가 ignored; `.serverless` JSON 3개 존재 | Reproducible source/deploy/backend evidence로 사용 금지 |
| Fresh root verify | Exit 0, main 6/test 1 compile, tests `1/0/0/0`; Shade collision warnings | Placeholder regression only; `E-P13-*` 또는 scheduler skip evidence 아님 |

Exact token scan에서 `pom.xml`, `src`, `gcp`, Dockerfile과 root README에 route pool,
route selection, hybrid, MIP, Gurobi/CPLEX/OR-Tools, Phase13 implementation match는 0이었다. 이 문자열 inventory는 legacy/absence 확인이며 Gurobi 사용 승인이 아니다.
이는 현재 implementation absence inventory일 뿐 semantic synonym 전체를 증명하는
architecture evidence가 아니다.

## 3. Severity findings

### F-P13-001 — Phase 12를 보편 predecessor로 승격했다

- **Severity/status:** `HIGH — APPLIED`
- **Finding:** Target v1.0 metadata, §2.4, §4, WP-13.0/13.8, §12~§14는 Phase 12의
  일곱 evidence와 accepted review를 모든 Phase 13 activation의 필수 선행조건으로 뒀다.
- **Historical source evidence:** Canonical Master의 `RM-9A~C`/`C-17`, Integrated
  design의 Phase DAG와 당시 Master Realization Plan §4/Phase 12~13은 Phase 06+07→13
  optional branch와 Phase 10→12 provider-substitution branch를 분리했다. Current
  plan은 이를 Phase 06→07→08→14A acceptance 뒤 optional Phase 13으로 강화했다.
  Phase 12 §18.3도 스스로
  `INFRASTRUCTURE_DECISION_INPUT_ONLY`, `hybridImplementationAuthority=false`라고 제한한다.
- **Risk:** Baseline runtime의 합법적 gate-open review를 무관한 infrastructure branch가
  영구 차단하거나, 반대로 Phase 12 parity가 C-17/OR-Tools activation authority처럼 오인될 수 있다.
- **Correction:** Phase 12 evidence를 selected substituted provider/runtime에만 요구하는
  conditional implication으로 바꾸고 receipt, entry ledger, tests, work package, exit,
  blocker, handoff와 traceability를 함께 정렬했다.
- **Applied:** Target v1.1 §2.4, §4, §9.2, WP-13.0/13.8, §12~§14.
- **Residual risk:** 당시 반대 방향 문구는 F-P13-004에서 추적했으며 현재 owner-scoped
  alignment로 해소됐다. `C-17`과 실제 conditional Phase 12 evidence 필요성은 별도다.

### F-P13-002 — Gate-closed path가 Phase 13 activation code를 호출하는 모순

- **Severity/status:** `HIGH — APPLIED`
- **Finding:** Target v1.0은 absence semantics를 선언하면서도
  `Phase13ActivationGate.decide(...)`를 closed/absent/rejected 경로의 첫 호출로 뒀고
  proposed default application tree에 Phase 13 gate/orchestrator와 solver edge를 포함했다.
- **Exact source evidence:** Canonical `C-17`은 승인 전 implementation start/default
  activation을 금지한다. 사용자 고정 acceptance는 closed path가 hybrid code/solver/provider
  absence 또는 non-execution이어야 하고 unauthorized request는 fail closed여야 한다.
- **Risk:** “disabled implementation”이 canonical path에 들어가 classpath/service discovery,
  provider resolution, allocation 또는 hidden activation side effect를 만들 수 있다.
- **Correction:** Scheduler/control plane이 Phase 13 module 밖에서 skip/reject/open을 결정한다.
  Closed/no-request는 ALNS-only path + scheduler skip receipt, closed/explicit-request는
  pre-solve rejection이다. `Open`만 optional `hybrid-application` assembly와
  authorized-scope verifier를 호출한다. Default application→solver edge를 제거했다.
- **Applied:** Target v1.1 §3.3, §5.2, §6.5, §7.1, §8.3, §9.2, WP-13.0.
- **Residual risk:** Actual scheduler receipt/module-boundary test는 아직
  `NOT_PRODUCED`; implementation 전에는 current absence만 관찰된다.

### F-P13-003 — 인접 digest acceptance가 semantic review를 대체했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Finding:** Target v1.0 metadata/§1.2는 canonical, inventory, Phase 06/07/12와
  planned Phase 14 section SHA-256을 복제하고 hash equality를 compatibility receipt로 썼다.
- **Exact source evidence:** Current Phase 02~11 reviews는 adjacent whole-file/section
  digest가 unrelated edit와 reciprocal churn을 만들며 accepted artifact/evidence를
  증명하지 못한다고 일관되게 제거했다. 사용자 acceptance도 반복 hash 추적과 adjacent
  digest acceptance를 금지한다.
- **Risk:** Hash match가 false-green 의미 acceptance처럼 보이거나, 의미와 무관한 편집이
  entry/handoff를 막는다.
- **Correction:** 모든 source/adjacent/inventory digest map과 재현 command를 제거했다.
  최신 named section one-time read + semantic impact review + accepted artifact/evidence
  identity만 사용한다.
- **Applied:** Target v1.1 metadata와 §1.1~§1.2, §14.5.
- **Residual risk:** 당시 인접 문서 drift는 F-P13-004에서 추적했으며 현재 해소됐다.
  향후 semantic 변경은 named-section impact review로만 다시 연다.

### F-P13-004 — Phase 12/14 read-only 문서의 reciprocal dependency/hash drift

- **Severity/status:** `HIGH — RESOLVED`
- **Historical finding:** Phase 12 §18.3과 Phase 14의 entry/handoff는 target v1.0의 보편
  Phase12→Phase13 premise를 역인용하고 adjacent/reciprocal section fingerprint를
  acceptance에 사용한다.
- **Exact resolution evidence:** Phase 12 v1.2 §18.3은 selected substituted runtime에
  적용되는 `INFRASTRUCTURE_DECISION_INPUT_ONLY` bounded evidence만 넘기고
  `c17Approval`, OR-Tools activation, hybrid implementation과 production authority를
  명시적으로 부여하지 않는다. Phase 13 v1.3 §6.5/§14.3은 scheduler-owned signed
  applicability envelope와 action-time verification을 고정한다. Phase 14 v1.2
  §3.1/§5.3은 이를 downstream consumer seam으로 소비하되 unsigned/stale/non-`PASS`를
  fail-closed하고 Phase 13 reverse entry dependency와 adjacent/reciprocal digest
  acceptance를 금지한다.
- **Applied/resolution:** Phase 12/13/14의 owner-scoped corrections와 reviews로
  conditional premise, signed applicability semantics와 consumer seam이 정렬됐다.
  Target §13의 이 항목을 `RESOLVED — NOT A BLOCKER`로 갱신했다.
- **Residual risk:** 이 finding의 active residual은 `NONE`이다. Semantic regression이
  생길 때만 named-section impact review로 다시 연다. 이 종료는 `C-17`,
  signing/trust policy, 실제 signed envelope/verification receipt 또는 Phase 14 자체
  official/production gate를 승인하지 않는다.

### F-P13-005 — Full-solution evaluation/comparator authority가 upstream에서 미해결

- **Severity/status:** `HIGH — RESIDUAL INHERITED BLOCKER`
- **Finding:** Phase 13은 selected routes를 fresh materialize한 뒤 full solution
  evaluation과 comparator로 strict adoption해야 하지만 그 exact API/identity/reuse/
  invalidation/failure/comparator contract가 Phase 03~07 review에서 unresolved다.
- **Exact source evidence:** [Phase 03 review](phase-03-review.md),
  [Phase 04 review](phase-04-review.md), [Phase 05 review](phase-05-review.md),
  [Phase 06 review](phase-06-review.md), [Phase 07 review](phase-07-review.md)는
  route-level artifact와 ordered routes+bank full evaluation 사이 공백을 공통 blocker로 둔다.
- **Correction required:** Core/Evaluation, Capability/Profile, Algorithm/Verification
  owners가 exact solution input, route reuse/invalidation, hard/metric/objective aggregation,
  failure, business equality와 context tie 경계를 승인해야 한다.
- **Applied locally:** Target §13 blocker와 Phase 06/07 entry gate에 상속했다.
- **Residual/owner/last safe/restart:** Last safe point는 accepted route-level kernel과
  immutable ordered routes/bank이며 ad hoc scalar/Big-M/route-vector aggregation은 금지다.
  Cross-phase compile/full-recompute-equality/corruption/comparator tests 승인 뒤 재개한다.

### F-P13-006 — Test/module plan이 zero-test와 closed-path dependency를 충분히 차단하지 못했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Finding:** Target v1.0 selected commands는 root `mvn -pl ... -am -Dtest=...`
  형태였고 fresh exact report/discovered method gate가 없었다. Closed-path scheduler
  test와 optional hybrid implementation도 같은 application module에 섞였다.
- **Exact source evidence:** Actual repository에는 `./mvnw`, target module/test가 0이다.
  Current Phase reviews는 owner POM, no-`-am`, `failIfNoSpecifiedTests=true`, `clean`,
  fresh Surefire class/method manifest를 required detector로 사용한다.
- **Correction:** Scheduler applicability test는 generic application module에 두고
  optional implementation은 `hybrid-application` 후보 module로 분리했다. Selected
  command는 pinned future wrapper, owner POM, clean, no-`-am`, fail-closed zero-test와
  exact fresh report reconciliation을 요구한다.
- **Applied:** Target v1.1 §5.2, §9.2/§9.8, WP-13.0~13.8.
- **Residual risk:** Module 이름과 wrapper는 Phase 00 accepted architecture 뒤 freeze해야
  하며 현재 command는 future red다.

### F-P13-007 — Canonical H1과 review/inventory metadata가 actual 상태와 달랐다

- **Severity/status:** `LOW — APPLIED`
- **Finding:** Target v1.1 H1은 canonical Phase label에 route-selection 상세를 합쳐
  `Phase 13 — Optional hybrid route selection`로 썼다. Target v1.0은 Phase 12/14가
  authoring 중 나타났고 Phase 13 review가 없다는 narrative와 stale Phase 06/07
  status도 metadata에 보존했다.
- **Exact source evidence:** Canonical phase label은 `Phase 13 — Optional hybrid`다.
  Actual checkout에는 Phase 00~14 detail과 review `15/15`가 모두 존재한다. Phase 12
  v1.3은 `CHANGES_REQUIRED/BLOCKED_NOT_IMPLEMENTED`, Phase 14 v1.4는
  `CHANGES_REQUIRED`, 14A `NOT_RUN`, 14B `NOT_STARTED/BLOCKED_NOT_READY`다. 존재와 review 완료는
  implementation/evidence/acceptance가 아니다.
- **Correction:** H1을 exact canonical label로 고치고 `Route selection`은 부제로만
  유지했다. Target v1.3 metadata/§1/§5.1에는 live review `15/15`, Phase 12/14의 exact
  verdict/acceptance/handoff를 기록하고 작성 당시 branch/commit/code/build inventory는
  historical snapshot으로 분리했다.
- **Applied:** Target H1/subtitle, metadata, §1과 §5.1.
- **Residual risk:** Scheduler만 progress/status registry를 바꿀 수 있다. Target metadata는
  Phase acceptance나 activation status를 올리지 않는다.

### F-P13-008 — Skip receipt가 Phase 14의 signed applicability gate를 충족하지 못했다

- **Severity/status:** `HIGH — APPLIED CONTRACT / RESIDUAL AUTHORITY BLOCKER`
- **Finding:** Target v1.1 §6.5의 `Skip`은 reason, scheduler decision, ALNS-only
  identity, observation time와 authority version만 가졌다. Signed applicability
  envelope, signature/trust-policy reference, validity window, revocation/freshness와
  action-time verification 결과가 없어 Phase 14가 unsigned/stale receipt를 소비할
  여지가 있었다.
- **Exact source evidence:** Phase 14 §3.1의 `G14-P13-APPLICABILITY`와
  `G14-SIGNING-TRUST`는 exact applicability receipt에 signature profile, trust roots,
  revocation/time policy, validity와 verifier evidence를 요구한다. Phase 14 §3.1은
  gate 소비 때마다 current trust/revocation/time snapshot과 checked action을 담은
  immutable verification receipt를 만들고 production action 직전에 다시 검증하도록
  고정한다.
- **Risk:** 과거 signature `PASS`, expired/revoked key, wrong scope/action 또는 stale
  trust snapshot이 ALNS-only official manifest/cutover authority처럼 재사용될 수 있다.
- **Correction:** Target v1.2 §6.5/§14.3에 exact receipt version/identity/fingerprint,
  separate signed envelope, signature-profile/trust/revocation/validity refs와 immutable
  action-time verification receipt를 proposed contract로 추가했다. Unsigned,
  unknown-profile, wrong-scope/action, expired, revoked, stale 또는 non-`PASS`는
  `UNTRUSTED_OR_INVALID_PHASE13_APPLICABILITY`로 fail closed하며 Phase 14
  manifest/action과 Phase 13 load를 모두 0으로 유지한다. Activated applicability에도
  동등한 signing/action-time gate를 요구한다. 이 gate는 downstream Phase 14
  consumption gate이며 Phase 13 entry prerequisite/evidence로 역전하지 않아
  `Phase 13 → Phase 14 → Phase 13` dependency cycle을 추가하지 않는다.
- **Applied:** Target v1.2 metadata/status, §6.5, §9.1~§9.2/§9.8, WP-13.0,
  §11~§14와 anti-pattern/blocker/traceability.
- **Residual/owner/last safe/restart:** Exact signature algorithm, key/certificate
  format, trust roots/store, revocation/time/freshness authority와 모든 숫자는
  Security/Release + Phase 14 `G14-SIGNING-TRUST` 소유의 `OPEN/GATED_NOT_APPROVED`다.
  Last safe point는 unsigned proposed schema/negative tests와 no Phase 14 action이다.
  Approved policy/verifier evidence, actual signed envelope와 action-time verification
  receipt가 생긴 뒤 restart한다. 현재 상태는 모두 `NOT_PRODUCED`다.

## 4. 검사축별 판정

| Review axis | 판정 | 근거/남은 조건 |
|---|---|---|
| User-locked authority와 `REVIEW` 비중단 | `PASS` | Current canonical/register 우선, historical-only 분리 |
| `C-17` optional/gated 보존 | `PASS AFTER FIX` | Closed path absence, explicit unauthorized fail-closed, gate-open only assembly |
| Signed Phase 13 applicability | `PASS PROPOSED CONTRACT / AUTHORITY BLOCKED` | Exact signed envelope와 action-time trust/validity/revocation/freshness fail-closed; policy/receipt NOT_PRODUCED |
| Phase 12 substitution boundary | `PASS ALIGNED` | Phase 12 v1.3 bounded conditional input과 Phase 13 v1.5 contract 정렬; C-17/hybrid authority는 부여하지 않음 |
| Phase 14A ALNS benchmark prerequisite | `PASS DOCUMENT CONTRACT / EVIDENCE MISSING` | Phase 06/07/08 acceptance + immutable benchmark receipt가 Phase 13보다 먼저이며 actual receipt `NOT_PRODUCED` |
| Phase 14B ALNS-only skip | `PASS DOCUMENT CONTRACT / EVIDENCE MISSING` | Scheduler-owned skip, Phase 13 refs absent, Phase 14B 자체 gate 보존 |
| Backend decision/hidden defaults | `PASS` | Direct CP-SAT policy만 fixed; version/checksum/workers/seed/time/gap/platform/cap/cadence/traffic/performance는 OPEN/GATED; `MPSolver` fallback 없음 |
| Route pool/projection/materialization/adoption | `PASS DOCUMENT PLAN` | Exact/no-surrogate/full evaluation/strict adoption; actual source/evidence 0 |
| Full-solution evaluator/comparator | `BLOCKED` | F-P13-005 cross-phase contract 미해결 |
| Dependency/default assembly | `PASS AFTER FIX / NOT IMPLEMENTED` | Generic application→solver edge 제거; architecture evidence 0 |
| Unauthorized/security/OSS-license/SBOM/native/cost | `PASS DOCUMENT PLAN` | Pre-allocation reject, isolated approved OR-Tools adapter only; owner approval 없음 |
| Failure/rollback/reproducibility | `PASS DOCUMENT PLAN` | Optional incumbent exact retention, required incomplete, replay class explicit |
| Test/fixture/oracle false-green | `PASS AFTER FIX / FUTURE RED` | Exact oracle와 report gate; target wrapper/module/test 없음 |
| Evidence truth | `PASS` | Root verify, planned type/test, solver log와 scheduler skip을 `E-P13-*`로 승격하지 않음 |
| Adjacent/reciprocal hash | `PASS RESOLVED` | Phase 12/13/14는 stable named-section semantic seam만 사용하며 reciprocal acceptance blocker 종료 |
| Actual Java/Maven/deployment | `PASS INVENTORY / NON-CONFORMANT PLACEHOLDER` | Java 25/Maven 3.9, single GCP placeholder, hybrid source 0 |
| Production/official authority | `PASS` | Phase 13 review/acceptance가 Phase 14 official/cutover/production을 우회하지 않음 |

## 5. 적용 변경 요약

Target v1.2에 앞선 6개 safe correction을 적용했고 v1.3에서 live-status와 reciprocal
정합성을 보완했다. v1.4에서는 사용자 확정 backend 정책을 문서 계약에 반영했고,
v1.5에서는 ALNS benchmark acceptance 선행 gate와 optional MIP branch를 명시했다.

1. Universal Phase 12 predecessor를 selected substituted runtime의 conditional evidence로 교정.
2. Scheduler-owned closed-path skip/rejection과 gate-open optional assembly를 분리.
3. Source/inventory/adjacent section digest와 reciprocal fingerprint acceptance 제거.
4. Generic application과 optional hybrid/solver dependency 및 test ownership 분리.
5. Fail-closed selected Maven/report protocol, canonical H1/subtitle와 actual review/inventory status 정정.
6. Signed applicability envelope와 action-time trust/validity/revocation/freshness fail-closed contract 추가.
7. Phase review `15/15`, Phase 12/14 current verdict를 live status로 갱신하고 authoring
   code/build snapshot을 historical로 분리했으며 resolved reciprocal blocker를 종료.
8. Exact backend를 Google OR-Tools direct CP-SAT로 고정하고 status × incumbent,
   cancellation, explicit reproducibility params, native Loader/cleanup,
   Apache-2.0/applicable notice/SBOM와 unchanged-ALNS fallback 계약으로 교체.
9. Phase 06/07/08 accepted evidence와 Phase 14A ALNS benchmark acceptance receipt 뒤에만
   Phase 13을 여는 ALNS-first gate, bounded candidate modes와 timeout/failure fallback을 추가.

다른 Phase/review, canonical source, Java, POM, build/deployment, progress/status는 수정하지 않았다.

## 6. Blocker, owner, last safe point와 restart

| Blocker | Owner | 현재 막는 범위 | Last safe point | Restart condition |
|---|---|---|---|---|
| `C-17` scope/meaning/authority 미승인 | Product + Algorithm + Architecture | 모든 Phase 13 source/module/evidence | Canonical ALNS-only path | Separate immutable approval and exact scope receipt |
| Phase 06/07/08 accepted artifact/evidence 없음 | Algorithm + Verification + Application + independent reviewers | ALNS implementation, independent verification, application-boundary handoff | Reviewed documents only | Accepted `E-P06-*`/`E-P07-*`/`E-P08-*` and exact handoff identities |
| Phase 14A ALNS benchmark acceptance receipt 없음 | Benchmark + Quality + independent reviewer/acceptance owner | Phase 13 entry 전체 | ALNS-only document path; no Phase 13 source/load | Complete immutable evidence bundle and independently reviewed/accepted `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` |
| Full-solution evaluation/comparator gap | Phase 03~07 Core/Profile/Algorithm/Verification | Materialization, strict adoption, publication | Route kernel + immutable routes/bank | Exact API/identity/failure/tie + equality/corruption tests |
| Scheduler task/role/signed skip receipt 없음 | Total scheduler + Phase 14 Application | Closed skip evidence와 any implementation start | No Phase 13 path; ALNS-only, no Phase 14 action | Exact task/roles, signed scheduler receipt, action-time verifier, no-load/no-edge/unauthorized tests |
| Applicability signing/trust policy 미승인 | Security + Release evidence-trust owner | Phase 14 consumption of Skip/Activated | Unsigned proposed schema/negative tests only | Signature profile/trust roots/revocation/time/freshness/canonicalization/verifier approval and evidence |
| OR-Tools version/config/native/OSS-license/SBOM/security/ops/cost 미승인 | Registered specialist owners | Gate-open backend/selection work | OR-Tools-free ALNS-only build, no backend | Exact pin/checksum/params/platform plus all owner approvals and isolated evidence |
| Pool/budget/cadence/traffic/performance 수치 | Performance/Product/FinOps | Experiment/official rollout | No value/default | Explicit experiment manifest, measured evidence and approval |
| Phase 14 official/production gates | Benchmark/Quality/Product/Platform/Security/Ops | Official hybrid/ALNS cutover | Experiment-only, no traffic | `Q-BENCH-02`, compliant travel, accepted predecessors and production authority |

Phase 12/13/14 reciprocal contract drift는 Phase 12 v1.3 conditional bounded handoff
(introduced in v1.2),
Phase 13 v1.5 signed applicability/action-time verification과 Phase 14 v1.4 14B consumer
seam 정렬로 `RESOLVED`됐으며 active blocker 표에서 제거했다. 이 정리는 위 blocker나
`C-17`, signing/trust approval, 실제 signed receipt 부재를 닫지 않는다.

Blocker를 legacy GCP success, root test 1건, Phase 12 parity, provider SDK 설치,
backend log, test-only 숫자, environment boolean 또는 neighbor hash로 닫지 않는다.

## 7. 실제 검증 명령과 결과

### 7.1 Historical authoring-time build/source characterization

| Command/check | Actual result | 해석 |
|---|---|---|
| `git rev-parse --abbrev-ref HEAD` / `git rev-parse HEAD` | `codex/domain-design`; `3424277c9c74f8151a83be056a07dd4659331beb` | Authoring-time saved checkout snapshot; live review status와 분리 |
| `java -version` | Exit 0; Corretto OpenJDK `25.0.3` | Java 25 inventory |
| `mvn -version` | Exit 0; Maven `3.9.14`, Java `25.0.3`, macOS aarch64 | Root toolchain inventory |
| `mvn -B -ntp -Dstyle.color=never clean verify` | Exit 0; main 6/test 1; tests `1`, failures/errors/skipped `0`; 4.447 s | Placeholder regression only; Phase 13 evidence 아님 |
| Source/test/deployment listing | Main Java 6, test Java 1, GCP tracked files 3, Dockerfile 1 | Target hybrid module/test/provider 0 |
| Tracked AWS/IaC scan | Terraform/SAM/serverless/ASL source 0 | AWS/Phase 13 implementation evidence 0 |
| Hybrid/backend exact-token scan | `pom.xml`, `src`, `gcp`, Dockerfile, README match 0 | Bounded literal absence inventory only |
| Ignore/inventory check | `target/`와 `.serverless/` ignored; `.serverless` JSON 3개 | Generated/historical artifact, source/evidence 아님 |

Root verify는 Shade module-info/service/license/manifest collision warnings를 남겼다.
이는 Phase 00 build blocker inventory이며 Phase 13 backend, reproducibility 또는
evidence success를 뜻하지 않는다.

### 7.2 Post-write document validation

아래 표는 v1.3 문서 review 당시의 historical validation 기록이다. v1.4 OR-Tools
정책 전환의 current multi-document 검증은 이번 change set의 최종 validation과
별도 독립 교차검증으로 다시 수행하며, 이 과거 write-scope 주장을 현재 작업에
재사용하지 않는다.

| Validation | Result |
|---|---|
| 두 파일 non-empty | `PASS`; 두 경로 `test -s` exit 0 |
| Live review/status inventory | `PASS`; Phase review 15개 모두 완료. Phase 12 `CHANGES_REQUIRED/BLOCKED_NOT_IMPLEMENTED`; Phase 14 `CHANGES_REQUIRED`, 14A `NOT_RUN/RECEIPT_NOT_PRODUCED`, 14B `NOT_STARTED/BLOCKED_NOT_READY` |
| Required metadata/scope/inventory/verdict/findings/change/blocker/commands | `PASS`; review §1~§8, recorded 8개 = non-resolved 7개(`HIGH 4/MEDIUM 2/LOW 1`) + resolved 1개; active residual 2개 |
| Relative Markdown link/anchor | `PASS`; local target/GFM-style heading validator `PASS_LINKS_AND_ANCHORS files=2` |
| Fence parity/trailing whitespace/EOF newline | `PASS`; target fence 48, review fence 4로 even, trailing match 0, EOF newline 정상 |
| C-17 closed-path absence/no-op/fail-closed/signed scheduler skip receipt | `PASS DOCUMENT`; no-load/no-resolution, unauthorized rejection, exact signed envelope와 action-time verification contract 존재; actual receipt NOT_PRODUCED |
| Hidden solver/provider/threshold/crypto/trust/default, Phase 14 bypass와 dependency cycle | `PASS DOCUMENT`; 값은 OPEN/GATED/TEST_ONLY/금지문뿐이고 Phase 14 자체 gate를 우회하지 않음. Signing trust는 downstream consumption gate라 Phase 13 reverse entry edge가 없음 |
| Adjacent/reciprocal alignment와 64-hex document digest | `PASS RESOLVED`; F-P13-004 closed, acceptance용 digest/64-hex value 0. Policy keys는 explicit `FORBIDDEN`/`NOT_USED` |
| 당시 allowed write scope | `PASS BY PATCH TARGET LOG`; v1.3 review 당시 target과 review만 수정. 현재 v1.4 change set의 범위를 뜻하지 않음 |
| 당시 `git diff --check` + untracked-aware whitespace check | `PASS`; v1.3 기록이며 v1.4에서 재실행 필요 |

### 7.3 v1.4 OR-Tools 정책 독립 교차검증

별도 read-only reviewer가 v1.4 Phase 13 change set의 canonical authority/status,
backend 결정, status × incumbent, selected-ID boundary, native/OSS-license,
fallback과 relative link를 재검증했다. 중간에 발견한 unassigned side-channel,
gate disposition 명칭, Security/Operations/Cost owner 목록 drift는 current
authoritative 문서에 교정한 뒤 다시 검사했다.

최종 verdict는 `PASS_NO_ACTIONABLE_FINDINGS`다. 다음을 확인했다.

- Phase 13은 `C17_GATE_CLOSED / GATED_NOT_STARTED / NOT_ACCEPTED`,
  implementation/evidence/receipt는 `NOT_STARTED / NOT_PRODUCED`다.
- 0-1 integer/fixed-point model의 canonical backend는 direct Java CP-SAT이고
  `MPSolver` fallback이 없다.
- `OPTIMAL`/`FEASIBLE`에서만 selected route IDs를 읽으며 objective/bound/gap은
  adoption authority가 아니다.
- Version, workers, seed, time/gap은 `OPEN`; single-worker/fixed-seed는
  `PROPOSED TEST_ONLY`다.
- Process-wide native load, `stopSearch()`, callback/reference/temp cleanup,
  Apache-2.0/applicable notice/SBOM와 security/operations/cost gate가 정렬됐다.
- Gurobi 문자열은 명시적 금지, legacy absence inventory 또는 negative guard뿐이며
  commercial license/server/token/capacity lease 전제는 없다.
- ALNS-only default, fresh materialization/full evaluation, Phase 07 independent
  verification, strictly-better adoption과 unchanged-incumbent fallback이 유지됐다.
- `git diff --check`, 12개 대상 문서의 whitespace/fence/EOF, relative
  link/anchor와 canonical Phase map 검사가 통과했다.

이 독립 verdict도 **문서 계약 검증**이다. Phase 13 implementation/evidence
acceptance나 production authority를 만들지 않는다.

## 8. 최종 handoff 제한

Phase 13 document contract는 safe correction 뒤 review됐지만 gate는 닫힌 채다.
Phase 14는 현재 다음 둘 중 gate-closed branch만 사용할 수 있다.

```text
Scheduler Phase13ApplicabilityReceipt.Skip
  reason = C17_GATE_CLOSED
  exact receipt schema/version and scheduler-decision fingerprint
  exact ALNS-only plan identity
  signed applicability envelope = exact subject/scope/digest + signature/trust-policy refs
  validity window + revocation-policy ref
  Phase 14 action-time verification =
    verifier/trust-root-set digests + revocation/freshness snapshots
    + checked action/scope/time + PASS verdict
  Phase 13 assembly/provider/pool/model/outcome refs = ABSENT
  E-P13-* refs = ABSENT
```

unsigned, wrong-subject/scope/action, not-yet-valid, expired, revoked, stale,
unverifiable 또는 non-`PASS` applicability는 Phase 14 action과 Phase 13 load를 모두
fail-closed한다. exact signing algorithm, key/certificate format, trust root/store,
revocation/time/freshness policy와 모든 수치는 외부 승인 전 `OPEN/GATED_NOT_APPROVED`다.

이 signed envelope와 action-time verification receipt를 포함한 actual
implementation/evidence는 `NOT_PRODUCED`다. 따라서 현재 사실은
“Phase 13이 실행되지 않는다”이며 “hybrid-disabled implementation이 accepted됐다”가 아니다.
Gate-open handoff, official hybrid, Phase 14 calibration/cutover와 production authority는
모두 별도 blocker가 해제되고 accepted evidence/review가 생긴 뒤에만 재검토한다.

## 10. ALNS-first direction revision review

Phase 13 v1.5의 새 entry gate를 검토했다. Phase 06/07/08 accepted evidence와 Phase
14A의 immutable `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`가 모두 선행하고, 그 뒤에도
`C-17`, OR-Tools version/config/native/OSS/SBOM, security/operations/cost/admission/
fallback/rollback 승인을 별도로 요구한다. 이 gate는 `PASS`이며 현재 모두
미충족이므로 Phase 13은 계속 `GATED_NOT_STARTED_NOT_ACCEPTED`다.

MIP 복잡도는 worst-case 및 instance/constraint/formulation/backend/hardware 민감성으로
제한해 서술했고, solver-neutral route selection, bounded subproblem, warm-start,
repair와 intensification을 proposed mode로만 둔다. Timeout/no-incumbent/model/native/
resource failure와 invalid/equal/worse candidate의 ALNS incumbent 보존도 명시됐다.
Evidence와 acceptance receipt는 `NOT_PRODUCED`다.
