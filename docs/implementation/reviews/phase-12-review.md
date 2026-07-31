# Phase 12 독립 리뷰 — Provider substitution

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "12"
review_date: 2026-07-28
final_audit_cycle_status: RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2
manifest_reference_dag: STANDALONE_EVIDENCE_TO_FORWARD_MANIFEST_TO_REVIEW_TO_RECEIPT
live_phase_document_inventory: "15/15"
live_review_document_inventory: "15/15"
live_review_completion: "15/15"
authoring_snapshot_status: HISTORICAL_NOT_CURRENT
reviewer_role: independent Phase 12 documentation reviewer
target_document: docs/implementation/phases/phase-12-provider-substitution.md
target_document_version_after_safe_fixes: 1.3
historical_safe_fix_version: 1.2
current_version_reason: ALNS_FIRST_C17_RESTART_GATE_AND_VERSION_REFERENCE_CORRECTION
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
document_verdict: CHANGES_REQUIRED
phase_acceptance_verdict: BLOCKED_NOT_IMPLEMENTED
implementation_status_observed: NOT_STARTED
candidate_provider_status_observed: NOT_SELECTED
provider_adoption_status_observed: NOT_APPROVED
deployment_status_observed: NOT_DEPLOYED
production_authority_observed: NOT_GRANTED
evidence_status_observed: NOT_PRODUCED
phase_13_c17_authority_observed: NOT_GRANTED
scheduler_status_change: NOT_AUTHORIZED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
finding_counts_basis: HISTORICAL_FINDINGS_AS_RAISED
finding_counts:
  critical: 1
  high: 5
  medium: 2
  low: 0
  total: 8
finding_disposition:
  applied_safe_obvious: 7
  resolved_by_phase11_v1_2_and_phase12_v1_2: 1
  residual_external_or_cross_phase: 5
fake_evidence_detected: false
code_change_reviewed: false
code_change_made: false
```

## 1. 결론

[Phase 12 v1.3](../phases/phase-12-provider-substitution.md)은 provider-neutral core와
adapter 경계, 승인된 최소 substitution axis, actual provider conformance,
digest-preserving migration, security/observability/cost/rollback과 production
authority 분리를 충분히 상세하게 계획한다. AWS는 canonical 결정에 따른 reference
execution/evidence subject지만 semantic truth의 유일한 oracle도, 자동 production
default도 아니다. Candidate provider와 axis도 owner approval 전 선택되지 않는다.

독립 리뷰에서 source로 답이 명백한 7개 교정 묶음을 target에 적용했다.

1. Run-state, pending-action authorization과 publication-pointer precondition을 서로
   다른 typed authority로 분리하고 same-run-state cancel/publication fence를 요구했다.
2. `*IT`를 Surefire `-Dtest`로 선택하던 false-green 명령을 Failsafe
   `-Dit.test`와 no-zero-test/report reconciliation로 바꿨다.
3. Non-ambient authorization, lossless failure carrier와 worker committed-outcome
   authority가 없으면 conformance implementation을 시작하지 못하게 했다.
4. Crash/re-entry parity에 restart-safe durable monotonic deadline을 필수화했다.
5. AWS와 candidate가 각각 같은 독립 provider-neutral oracle에 pass한 뒤에만
   semantic parity를 비교하고, applicable case set을 실행 전에 seal하게 했다.
6. Stale adjacent section digest를 acceptance에서 제거하고 canonical 네 문서
   fingerprint와 안정 section 인용만 사용했다.
7. AWS/candidate evidence를 manifest 독립으로 먼저 seal하고, pre-review conformance
   manifest가 두 evidence digest와 applicability/config를 forward-reference한 뒤
   review와 acceptance receipt가 이어지는 단방향 DAG로 교정했다.

Document verdict는 `CHANGES_REQUIRED`다. 최신 Phase 08~11 review에는 non-ambient
authorization, lossless failure carrier, worker committed-outcome authority,
distinct publication precondition, exact current-pending action, same-run-state
cancellation fence와 restart-safe deadline이 아직 공동 blocker로 남아 있다.
`RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`: Phase 11 v1.2 §14.1/§18.2와
Phase 12 v1.2 §15.1/§18.2가 standalone AWS evidence/receipt → Phase 12-owned
conformance manifest 방향을 고정했다. Reciprocal conformance-evidence cycle은
더 이상 residual finding이나 verdict 사유가 아니다.

Document verdict가 계속 `CHANGES_REQUIRED`인 이유는 cycle이 아니라 위의 나머지
cross-phase contract blockers, 실제 implementation/evidence 부재와 source-governance
drift다. Historical finding은 8개를 보존하고, F-P12-007을 resolved로 제외한 current
external/cross-phase residual은 5개다.

Phase acceptance는 별도로 `BLOCKED_NOT_IMPLEMENTED`다. Candidate/axis/environment
approval, target Maven reactor, provider adapter/distribution, conformance tests,
actual AWS/candidate runs, migration/rollback rehearsal와 `E-P12-*` evidence가 없다.
현재 root `mvn clean verify`의 한 개 합성 unit test 성공은 Phase 12 parity evidence가
아니다. 이 review는 production provider 선택, cutover, traffic/default 변경 또는
Phase 13 `C-17` 권한을 만들지 않는다.

## 2. 범위, 권위 source와 baseline

### 2.1 완전히 읽고 대조한 권위 source

| Source | 직접 대조한 범위 | Review 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | 전체, 특히 §1~§4와 §13~§17 | C-20 provider boundary, lifecycle/identity/retry, both-gate publication, RM-8 substitution, RM-9 gate |
| [Final Domain](../../deprecated/2026-07-26-domain-design.md) | 전체, 특히 §3, §7~§10, §15~§18 | Provider-neutral problem/result authority, immutable provenance, failure/evidence와 source drift |
| [Final Architecture](../../deprecated/2026-07-26-architecture-design.md) | 전체, 특히 §2~§3, §5~§6 | Java 25/Maven module boundary, ports, state/identity/lifecycle/security/test |
| [Integrated design](../../deprecated/architecture-domain-implementation-design.md) | 전체, 특히 §16과 §19~§28 | Provider substitution, migration, failure/retry, conformance, invariant와 deferred boundary |
| [Question register](../../deprecated/master-design-open-questions.md) | 전체, exact 질문 상태 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`; Q-INFRA 선택과 production authority 분리 |
| [Master Realization Plan](../master-realization-plan.md) | 전체, 특히 §2~§6, Phase 11~13과 §8~§15 | Entry/exit, artifact ownership, evidence, rollback, independent review |
| [Implementation map](../README.md) | 전체 | 사용자 고정 authority와 planned/actual/review 경계 |
| [Target Phase 12](../phases/phase-12-provider-substitution.md) | 전체 | 계약, WP, test/evidence/rollback/handoff의 실행 가능성 |

Authority는 사용자 선언으로 고정했다. Superseded
[2026-07-26 Master](../../deprecated/2026-07-26-master-design.md)와 `docs/codex/*`는 historical
cross-check로만 확인했고 현재 contract 또는 provider 선택 근거로 쓰지 않았다.
`Q-BENCH-02`, `Q-VAR-01`, `C-17`과 production authority를 임의로 닫지 않았다.

### 2.2 인접 Phase와 current reviews

| 문서 | 읽은 범위 | 관찰 |
|---|---|---|
| [Phase 11 v1.3](../phases/phase-11-aws-reference-distribution.md) + [review](phase-11-review.md) | 최신 §6.1~§6.3, §14.1~§14.2, §17, §18.2와 review 전체 | v1.2에서 reciprocal cycle `RESOLVED`; current v1.3에서도 유지. `CHANGES_REQUIRED`, `NOT_STARTED/NOT_PRODUCED`, handoff `NOT_READY`는 불변 |
| [Phase 13 v1.5](../phases/phase-13-optional-hybrid-route-selection.md) + [review](phase-13-review.md) | §1.3, §4.1, §6.5, §14.2~§14.4와 blocker/handoff | `PASS_WITH_RESIDUAL_BLOCKERS`, `GATED_NOT_STARTED_NOT_ACCEPTED`; Phase 14A ALNS benchmark acceptance가 선행하며 Phase 12 receipt는 selected substituted runtime일 때만 조건부 |
| [Phase 14 v1.4](../phases/phase-14-official-calibration-cutover.md) + [review](phase-14-review.md) | Current metadata/verdict, 14A/14B entry blocker | `CHANGES_REQUIRED`; 14A `NOT_RUN/RECEIPT_NOT_PRODUCED`, 14B `NOT_STARTED/BLOCKED_NOT_READY`; ALNS benchmark/calibration/cutover authority 없음 |
| [Phase 08 review](phase-08-review.md) | 최신 finding/blocker와 handoff | Non-ambient access, lossless failure, worker commit, publication identity blocker |
| [Phase 09 review](phase-09-review.md) | 최신 finding/blocker와 handoff | Storage access/failure, worker outcome, distinct publication precondition blocker |
| [Phase 10 review](phase-10-review.md) | 전체, 특히 F-P10-001/002/004/005/006과 blocker | Exact pending action, same-state cancellation fence, test false-green, durable deadline와 adjacent digest |
| [현재 review set](.) | Phase 00~14 review 15/15 metadata와 verdict | Review 15/15 완료; P00/P13 residual-pass, P01/P02 applied-correction pass, P03~P12/P14 changes-required 계열 |

동시 배치의 인접 문서는 최신 contract/blocker section을 한 번 읽어 판정했다.
그 뒤 whole-file hash/status를 반복 추적하지 않았다. Neighbor digest나 reciprocal
fingerprint는 acceptance에 쓰지 않고 canonical fingerprint와 위 stable section
인용만 남겼다. 작성 당시 Phase 문서/review 일부가 없거나 `READY_FOR_REVIEW`였다는
관찰은 `HISTORICAL_NOT_CURRENT`다. Live inventory는 Phase 문서 15/15, review
15/15 존재 및 완료다.

### 2.3 Actual repository/build/source/test/deployment inventory

| 항목 | Actual 관찰 | Phase 12 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74`; review 시작 전부터 `docs/implementation/` 전체 untracked | Shared 사용자 작업 보존; 허용된 target/review 두 파일만 write |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Root POM release 25와 일치; provider evidence는 아님 |
| Maven | Root 단일 `com.ronext:ro-next` jar; Surefire 3.5.4, Failsafe/wrapper/target reactor 없음 | Planned selected/integration command는 현재 실행 불가 |
| Dependencies | Google Workflow/Storage SDK가 root compile dependency | Provider-neutral core/adapter isolation이 아직 없음 |
| Main/test | Main Java 6개, JUnit test 1개 | Phase 12 subject/capability/CAS/fault/security/parity test 0 |
| Current algorithm | [`AlnsBatchEngine`](../../../src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java)이 seed/iterations로 합성 `double` objective map 생성 | RPDPTW semantic oracle 또는 reference result가 아님 |
| API | [`OptimizationApiController`](../../../src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java)가 `Map`, `gs://`, random UUID와 fallback 8/5000 사용 | Canonical identity/config/provider-neutral port evidence가 아님 |
| Worker/finalize | [`OptimizationWorkerController`](../../../src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java)가 GCS prefix list와 raw objective 최소값 사용 | Completeness, verifier, CAS와 publication authority 불일치 |
| Tracked deployment | [`gcp/`](../../../gcp/) Workflow/Cloud Build/GCS 자료 | Historical/legacy characterization only; candidate 또는 parity evidence 아님 |
| Tracked AWS/candidate source | AWS/provider adapter/IaC/test source 0; ignored `.serverless` generated output만 존재 | Reproducible source/deployment/evidence 0 |
| Root verification | `mvn -B -ntp -Dstyle.color=never clean verify` exit 0; main 6/test 1 compile, tests 1/0/0/0 | Placeholder characterization만; `E-P12-*` 아님 |
| Provider authority/evidence | Candidate/axis/account/project/cluster approval와 actual AWS/candidate run 없음 | `NOT_SELECTED`, `NOT_APPROVED`, `NOT_DEPLOYED`, `NOT_PRODUCED` |

Root shade 단계는 module encapsulation과 overlapping class/resource warning을 남겼다.
이는 build inventory risk이며 provider substitution의 reproducibility나 semantic
parity 성공을 증명하지 않는다.

## 3. Severity 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 잘못된 state/publication authority 또는 회복 곤란한 corruption을 정상화 |
| `HIGH` | False-green, identity/CAS/security/retry/evidence gap으로 구현·acceptance를 막음 |
| `MEDIUM` | Source governance, oracle 독립성 또는 evidence 재현성을 유의미하게 약화 |
| `LOW` | 링크/status/whitespace 같은 제한적 hygiene 문제 |

## 4. Findings

### F-P12-001 — Run-state version이 action authorization과 publication precondition을 겸했다

- **Severity/status:** `CRITICAL — APPLIED LOCALLY / RESIDUAL CROSS-PHASE BLOCKER`
- **Exact source/evidence:** Review baseline의 target §6.3은
  `ResultPublisher.compareAndSet(... StateVersion expectedState, ...)`와
  `CoordinatorAction.expectedStateVersion`을 제안했다. 그러나
  [Phase 10 review F-P10-001](phase-10-review.md#f-p10-001--pending-action이-pre-cas-opaque-version을-semantic-authorization과-publication에-재사용했다)는
  pre-CAS token이 current pending action이나 별도 publication pointer의
  precondition이 될 수 없다고 판정한다. F-P10-002와 Phase 11 §6.3은 cancel intent
  object가 terminal race winner가 아니며 `CANCEL_REQUESTED`와 `PUBLISHING`이 같은
  run-state version에서 경쟁해야 한다고 요구한다.
- **Correction:** Pending action은 current authoritative state가 보유한 exact
  `actionId`/authorizing transition으로 검증하고, publication은 별도
  `PublicationPrecondition`과 current-pointer read/CAS를 쓴다. Cancel intent는
  durable request/hint만 담당하며 terminal fence를 대체하지 않는다.
- **Applied:** Target §2.4, §4, §6.2~§6.3, §8, WP12-3/4, §11~§12, §15~§18과
  traceability/checklist에 typed token separation과 same-state race를 반영했다.
- **Residual risk/last safe/restart:** Phase 08/09/10/11의 accepted public signature와
  actual local/storage/AWS conformance가 없다. Last safe point는 immutable result와
  pure state/action mapping까지만이며 publication은 off다. 네 owner가 exact
  read/CAS signature, response-loss/stale-token/same-version race를 공동 승인한 뒤
  adapter 구현을 재개한다.

### F-P12-002 — Integration test를 Surefire로 선택해 0-test green이 가능했다

- **Severity/status:** `HIGH — APPLIED`
- **Exact source/evidence:** Review baseline의 WP/§12 명령은 `*IT` class를
  `-Dtest=...IT`로 선택했고 Failsafe 설정이나 selected-test zero guard가 없었다.
  Actual [`pom.xml`](../../../pom.xml)은 Surefire 3.5.4만 사용하며 Failsafe plugin,
  `./mvnw`와 planned modules가 없다. 따라서 미래 파일명이 생겨도 integration
  lifecycle/profile/report가 실행됐다는 보장이 없었다.
- **Correction:** Future accepted wrapper와 exact module POM을 사용한다. Unit은
  `-Dtest` + `surefire.failIfNoSpecifiedTests=true`, integration은 `-Dit.test` +
  `failsafe.failIfNoSpecifiedTests=true`로 분리한다. Root `clean install` 뒤 no-`-am`
  selected module과 filter 없는 profile `clean verify`를 모두 실행하고 fresh
  Surefire/Failsafe XML의 discovered/passed/failed/error/skipped 수를 reconcile한다.
- **Applied:** Target WP12-0~9와 §12.1~§12.3, pass criteria, anti-pattern에 반영했다.
- **Residual risk:** Wrapper/reactor/Failsafe/tests가 아직 없어 이 명령은 future
  contract다. 현재 root one-test success나 stale XML은 대체 evidence가 아니다.

### F-P12-003 — Access/failure/worker authority gap을 adapter가 임의로 메울 수 있었다

- **Severity/status:** `HIGH — APPLIED LOCALLY / RESIDUAL CROSS-PHASE BLOCKER`
- **Exact source/evidence:** [Phase 08 F-P08-003/004/012](phase-08-review.md),
  [Phase 09 F-P09-002/003/004](phase-09-review.md)은 public port에 tenant-scoped
  non-ambient authorization binding, storage failure의 lossless carrier와 worker
  committed-outcome authority가 없다고 판정한다. Review baseline target은 provider
  subject 생성, storage test와 failure comparison이 이 미확정 계약을 실행할 수
  있다고 가정했다.
- **Correction:** Ambient credential/thread-local/global default provider는 subject를
  구성할 수 없다. Authorization은 existence/backend call 전에 검사하고
  denied/missing/corrupt/stale/indeterminate를 서로 또는 generic error로 축소하지
  않는다. Worker committed pointer의 exact create/CAS authority도 accepted
  cross-phase type 없이는 구현하지 않는다.
- **Applied:** Target §2.4, §4, §5.1, §6.1/6.4, §8, WP12-1/3/5/6/8,
  §11~§16과 blocker ledger에 gate/test/evidence를 추가했다.
- **Residual risk/last safe/restart:** Last safe는 backend call·existence disclosure와
  committed/publication pointer가 모두 0인 provider-neutral fixture/schema review다.
  Phase 08/09/10 owner가 binding lifecycle, exhaustive operation×failure carrier와
  worker pointer concurrency/response-loss test를 승인한 뒤 재개한다.

### F-P12-004 — Crash/re-entry parity에 durable monotonic deadline 복원 계약이 없었다

- **Severity/status:** `HIGH — APPLIED LOCALLY / RESIDUAL CROSS-PHASE BLOCKER`
- **Exact source/evidence:** Baseline target은 retry/deadline/crash parity를 요구했지만
  restart 뒤 monotonic origin, remaining-budget reconciliation, reserve와 corrupt
  persisted deadline 처리를 고정하지 않았다.
  [Phase 10 F-P10-005](phase-10-review.md#f-p10-005--durable-crash-resume를-요구하면서-monotonic-deadline-origin-복원-계약이-없었다)는
  Phase 08/10/11 공동 결정 전 provider deadline path를 활성화하지 말라고 판정한다.
- **Correction:** Accepted restart-safe deadline ref와 crash/re-entry fault suite를
  entry/capability/evidence gate에 추가했다. Wall clock, provider remaining time,
  hidden timeout/default로 복원하는 path는 negative oracle이 거부한다.
- **Applied:** Target §2.4, §4, §6.1/6.4, §8, WP12-4/5/8, §11~§16과 §18 blocker에 반영했다.
- **Residual risk/last safe/restart:** Last safe는 explicit `TEST_ONLY` virtual clock과
  deadline-enabled provider path off다. Durable encoding/origin reconciliation/
  reserve/failure ADR와 restart fault evidence 공동 승인 후 재개한다.

### F-P12-005 — AWS equality가 semantic oracle을 대신하고 applicability가 사후 변경될 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact source/evidence:** Baseline target은 AWS를 반복해서 “reference oracle”로
  불렀고 AWS↔candidate equality를 parity 중심으로 두었다. 두 adapter 또는 shared
  projection이 같은 결함을 가지면 동일한 오답도 pass한다. 또한 approved axis에서
  applicable case set을 provider run 전에 seal한다는 조건이 없어 candidate 실패 뒤
  `NOT_APPLICABLE`로 재분류할 여지가 있었다. Canonical Master §16.3과 Integrated
  §16.9~§16.10은 별도 parity/approval을 요구하지만 AWS 결과 자체를 domain truth로
  만들지 않는다.
- **Correction:** AWS를 `AWS_REFERENCE` execution/evidence subject로, candidate를
  별도 `CANDIDATE` subject로 둔다. 두 subject가 같은 provider-neutral independent
  oracle/version/case ID에 각각 pass한 뒤 semantic projection을 비교한다. Applicable
  set은 approved axis와 accepted contract version으로 실행 전 seal하며 required
  skip은 0이다. 공용 identity는 neutral `ProviderId`를 쓴다.
- **Applied:** Target metadata, §1~§3, §6.1/6.5, §8, WP12-1/8,
  §11~§12, §15~§16과 traceability/checklist를 교정했다.
- **Residual risk:** Independent oracle implementation과 defect-sensitivity mutants,
  actual AWS/candidate evidence는 없다. Candidate/axis도 계속 `NOT_SELECTED`다.

### F-P12-006 — Stale adjacent fingerprint가 predecessor acceptance에 들어 있었다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact source/evidence:** Baseline metadata와 §1.1은 Phase 11 §18.2 section
  fingerprint를 고정했다. 같은 batch의 Phase 11 v1.1 correction 뒤 one-time
  recomputation에서 값이 달라졌고, neighbor가 다시 target을 참조하면 reciprocal
  churn이 된다. 문서 hash 일치는 accepted handoff/evidence도 증명하지 않는다.
- **Correction:** 사용자 지정 canonical 네 문서만 whole-file provenance fingerprint로
  유지한다. Phase 11 §6.1~§6.3/§14.1~§14.2/§17/§18.2와 Phase 13
  §4.1/§6.5/§14.2는 stable semantic citation으로만 읽고 acceptance에는 actual
  immutable artifact/evidence receipt를 요구한다.
- **Applied:** Target metadata, §1.1, §4, WP12-0, §12, §17~§18과 checklist에서
  adjacent/reciprocal fingerprint를 제거했다.
- **Residual risk:** Canonical source가 바뀌면 fingerprint drift로 review를 재개해야
  하지만 현재 fingerprint 자체가 acceptance verdict는 아니다.

### F-P12-007 — Conformance/evidence/review manifests가 circular하게 봉인될 수 있었다

- **Severity/status:** `HIGH — RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`
- **Exact source/evidence:** Baseline `Phase12EvidenceManifest`는
  `independentReviewRef`를 포함해 reviewer가 검토할 manifest digest와 review
  content가 서로를 요구했다. Phase 11 §18.2 `ProviderConformanceManifest`도
  `awsReferenceEvidenceRef`를 포함하면서 provider evidence가 conformance manifest
  ref로 역참조할 수 있어 cycle이 생겼다. 1차 교정안처럼 evidence가 conformance
  manifest를 한 방향으로 참조해도, 나중에 manifest가 evidence digest를 포함하는
  순간 다시 순환하므로 최종 audit에서 방향을 재교정했다.
- **Correction:** Provider run 전에는 standalone
  `ProviderConformanceApplicabilityConfig`만 seal한다. AWS와 candidate
  `ProviderEvidenceManifest`는 그 config digest를 실행 provenance로 가질 수 있지만
  conformance manifest/review/receipt reference 없이 각각 immutable seal한다.
  그 다음 pre-review `ProviderConformanceManifest`가 applicability/config와 distinct
  AWS/candidate evidence content digest를 forward-reference한다. Independent review는
  manifest digest만 참조하고, 별도 `Phase12AcceptanceReceipt`만 exact manifest
  digest와 review digest/verdict/blockers를 결합한다.
- **Applied:** Target objective, entry receipt, comparison record/pseudocode,
  WP12-1/8/9, red→green, pass criteria, §15~§18, Phase 11/13 handoff와 checklist에
  exact DAG, forbidden edges와 mismatch/cycle negative tests를 반영했다.
- **Resolution evidence:** Phase 11 v1.2 §14.1/§18.2는 pre-review
  `Phase11EvidenceManifest`와 post-review receipt에 Phase 12 manifest reference가
  없고 Phase 12-owned manifest만 그 digests를 forward-reference하도록 고정한다.
  Phase 12 v1.2 §15.1/§18.2와 exact cycle tests도 같은 DAG를 요구한다.
- **Residual risk:** Regression 가능성만 남으며 current blocker로 세지 않는다.
  Anti-cycle/digest test 실패 시 이 finding을 다시 연다. 실제 evidence가
  `NOT_PRODUCED`이고 handoff가 `NOT_READY`인 상태는 별도 implementation gate다.

### F-P12-008 — Final Domain/Architecture의 Q-INFRA 상태가 최신 authority와 충돌한다

- **Severity/status:** `MEDIUM — RESIDUAL SOURCE-GOVERNANCE BLOCKER`
- **Exact source/evidence:** Question register와 Canonical Master는
  `Q-INFRA-01 RESOLVED`, 전체 질문 `RESOLVED 26 / OPEN 1 / DEFERRED 1`로 고정한다.
  사용자 고정 canonical set의 Final Domain/Architecture에는 과거
  `Q-INFRA-01 DEFERRED`, `25/1/2` 표기가 남아 있다. Phase 12가 이 두 source를
  수정하거나 새로운 authority 순서를 만들 권한은 없다.
- **Correction required:** Source owners가 provenance를 보존한 채 question register와
  정합화하거나 명시적 supersession/erratum을 승인해야 한다. 그 전 Phase 12는 사용자
  고정 authority order와 exact question register를 적용하고 충돌을 숨기지 않는다.
- **Applied:** Target §1과 inventory/blocker/checklist에 drift와 적용 순서를 명시했다.
- **Residual risk/last safe/restart:** Last safe는 provider-neutral schema/red
  conformance spec과 AWS reference-role 문서화뿐이다. Source-owner accepted correction
  또는 erratum 뒤 canonical fingerprint를 갱신하고 영향을 받는 section을 재검토한다.

## 5. Provider substitution 집중 판정

| 검사 축 | Verdict | 근거/잔여 조건 |
|---|---|---|
| Provider-neutral core/adapter boundary | `ADEQUATE PLAN / NOT IMPLEMENTED` | SDK는 adapter/distribution에만; root actual은 GCP SDK가 단일 module에 있어 target 미달 |
| AWS reference 의미 | `PASS AFTER CORRECTION` | Reference subject/baseline이며 sole oracle/default/production completion 아님 |
| Candidate/axis 선택 | `PASS DOCUMENT` | `NOT_SELECTED`/`NOT_APPROVED`; hidden fallback/provider default 0 |
| Semantic parity/oracle | `ADEQUATE PLAN / EVIDENCE MISSING` | 각 subject independent oracle pass + equality; defect-sensitivity/actual evidence 0 |
| CAS/consistency/idempotency | `CHANGES_REQUIRED` | Typed separation plan은 교정; Phase 08~11 accepted signatures와 actual fault evidence 0 |
| Corruption/retry/deadline | `CHANGES_REQUIRED` | Exact cases 있음; durable restart deadline cross-phase blocker |
| Security/failure carrier | `CHANGES_REQUIRED` | Non-ambient/no-collapse gate 추가; public binding/carrier 미승인 |
| Observability/operations | `ADEQUATE PLAN / EVIDENCE MISSING` | Neutral semantic fields와 provider metadata 분리; actual alarm/recovery evidence 0 |
| Migration/rollback | `ADEQUATE PLAN / EVIDENCE MISSING` | Exact closure/digest/shadow/no-authority plan; actual rehearsal 0 |
| Evidence sealing | `PASS DOCUMENT CONTRACT / EVIDENCE NOT_PRODUCED` | `RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`; DAG/anti-cycle contract 정합, actual digest/receipt는 없음 |
| Phase 11 handoff | `BLOCKED` | Cycle 때문이 아니라 review changes required, implementation/evidence not started와 accepted receipt 부재로 handoff not ready |
| Phase 13/C-17 boundary | `PASS DOCUMENT / GATED` | Infra evidence only; hybrid/OR-Tools activation/native distribution/production authority false |
| Production authority | `PASS DOCUMENT / NOT GRANTED` | Recommendation/receipt 모두 `productionAuthority=false`; cutover command 없음 |

## 6. 적용한 변경 요약

원래 독립 review의 쓰기 범위는 [Phase 12 target](../phases/phase-12-provider-substitution.md)과
이 review였고, 그 historical safe-fix cycle에서 target을 v1.2로 올려 다음을
반영했다. 후속 ALNS-first direction revision과 새 세션 corrective review는
C-17 restart gate와 current version reference를 정렬해 current target을 v1.3으로
올렸으며 아래 historical finding 수와 disposition은 바꾸지 않는다.

- Review/status/inventory metadata와 canonical-only fingerprint policy
- Independent oracle, pre-sealed applicability와 neutral provider role/identity
- Exact pending action, distinct publication precondition와 same-state cancel fence
- Non-ambient access, lossless failure, worker authority와 restart-safe deadline gates
- Surefire/Failsafe no-zero-test, fresh report reconciliation와 exact future module POM
- Manifest-independent provider evidence, forward-reference conformance manifest,
  manifest-only review와 manifest+review acceptance receipt DAG
- Phase 11/12 v1.2 reciprocal cycle resolution과 Phase/review 15/15 live inventory
- Phase 11/13 bounded handoff, blocker owner/last-safe/restart와 C-17/production denial

코드, POM, test, deployment, scheduler/progress, 인접 Phase/review와 canonical source는
수정하지 않았다.

## 7. Blocker, owner, last safe point와 restart

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Phase 08~11 review/evidence unaccepted | 각 Phase owner + scheduler | Provider-neutral catalog/red specs | Accepted review/evidence refs와 residual blocker 해소 |
| Non-ambient authorization/lossless failure | Phase 08/09 Application/Security/Data Integrity | Backend call/existence disclosure 0 | Approved binding/lifecycle + exhaustive operation×failure tests |
| Worker committed outcome authority | Phase 08/09/10 Storage/Coordinator | Verified orphan outcome only; committed pointer 없음 | Exact create/CAS type + concurrency/response-loss evidence |
| Action/state/publication identities | Phase 08/09/10/11 owners | Pure reducer + immutable result; publication off | Accepted typed current-action/read/CAS contract와 conformance |
| Cancel/publication race | Phase 08/10/11 owners | Durable cancel request only; terminal side effect off | Same-version/too-late/crash race evidence |
| Durable deadline restart | Phase 08/10/11 + Operations | Explicit test-only virtual clock; provider deadline path off | Durable ADR + restart/corruption fault evidence |
| Candidate/axis/environment 미승인 | Product/Platform/Ops/Security | Offline suite/schema only | Exact adoption decision와 isolated non-prod approval |
| Required provider semantics unsupported | Storage/Coordinator/Platform | AWS/local accepted path 유지 | Candidate/approved mechanism 변경; port weakening 금지 |
| Performance/cost/security/operations evidence 없음 | Security/SRE/FinOps/Performance | Measurement schema and test-only run | Approved policies + actual provider evidence |
| Final source Q-INFRA drift | Domain/Architecture source owners | User-locked authority + question register 적용 | Accepted source correction/erratum와 affected-section review |
| Phase 13 ALNS benchmark/`C-17`/production authority 없음 | Product/Algorithm/Platform/Security/Ops + Benchmark/Quality | Phase 12 infra evidence only | Phase 06/07/08 accepted + Phase 14A immutable benchmark acceptance receipt, separate C-17 and production adoption/cutover approvals |

Resolved audit ledger:

| Item | Status | Residual treatment |
|---|---|---|
| Phase 11/12 reciprocal conformance-evidence cycle | `RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2` | Current blocker/residual count에서 제외; regression test 실패 때만 reopen |

## 8. 실제 검증 명령과 결과

| 명령/검사 | Actual 결과 | 의미 |
|---|---|---|
| `java -version` | exit 0; Corretto OpenJDK `25.0.3` | Java 25 inventory 확인 |
| `mvn -version` | exit 0; Maven `3.9.14`, Java `25.0.3`, aarch64 | Current toolchain 확인 |
| `mvn -B -ntp -Dstyle.color=never clean verify` | exit 0; main 6/test 1 compile; tests 1, failures/errors/skipped 0 | Root placeholder build만 green; Phase 12 evidence 아님 |
| `rg --files` inventory | Main Java 6, test Java 1, tracked GCP files; wrapper/target reactor/adapters/distributions 없음 | Planned conformance implementation 부재 확인 |
| Live Phase/review metadata inventory | Phase 15/15, review 15/15, review complete 15/15 | Historical authoring snapshot과 current status 분리 |
| Canonical `shasum -a 256` | target metadata의 네 fingerprint와 모두 일치 | Canonical provenance drift 0 |
| `test -s` 상당의 metadata/heading assertion | exit 0; required field/section 11/11, missing 0, 두 파일 non-empty | 필수 review 구조 확인 |
| Local relative link/anchor validator + fence count | 45 links checked, missing file/anchor 0; fences target 84/review 2로 모두 even | Relative link와 Markdown fence closure 확인 |
| Whitespace/CRLF scan | trailing blank/tab match 0; `git diff --no-index --check` whitespace diagnostic 0 | Untracked 두 파일까지 whitespace 확인 |
| Residual/cycle semantic assertion | errors 0; current residual 5; cycle current blocker 0; reciprocal edge 0; positive authority grant 0 | `RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`와 남은 gate 분리 |
| `git diff --check` + status/write audit | exit 0; tracked change 0; explicit status는 target/review 두 파일 `??` | 허용 write scope 확인; 전체 implementation tree가 baseline부터 untracked라 original Git diff 대신 apply-patch operation audit 병행 |

Build는 shade overlapping resources/classes와 module-info warning, API unchecked
operation warning을 남겼다. 이 warning을 숨기지 않지만 Phase 12 문서 교정 범위 밖이며
provider parity evidence로도 해석하지 않는다.

## 9. 최종 verdict와 handoff

- **Document:** `CHANGES_REQUIRED`
- **Phase acceptance:** `BLOCKED_NOT_IMPLEMENTED`
- **Implementation/deployment/evidence:** `NOT_STARTED / NOT_DEPLOYED / NOT_PRODUCED`
- **Candidate/adoption/production:** `NOT_SELECTED / NOT_APPROVED / NOT_GRANTED`
- **Phase 11 handoff:** `NOT_READY`
- **Phase 13 handoff:** `NOT_READY`; Phase 14A ALNS benchmark acceptance receipt,
  `C-17`, OR-Tools activation/native distribution/hybrid/production authority 0
- **Safe next action:** 남은 cross-phase public contract를 승인하고 Phase 11 actual
  evidence/review acceptance를 생산한 뒤 candidate/axis/environment adoption record를
  별도로 만든다. 그 다음 target reactor의 red applicability/config와 anti-cycle
  regression tests부터 시작한다.

이 review 또는 교정된 Phase 12 문서만으로 provider resource 생성, production
traffic/default 변경, AWS/candidate cutover, Phase 13 구현 또는 scheduler 상태 변경을
승인하지 않는다.

## ALNS-first direction revision addendum

Task `019fa901-8776-7f61-b467-a8c6595b970d`에서 provider substitution은 계속 독립
optional branch이며 Phase 14A ALNS benchmark나 Phase 13 activation authority를
부여하지 않음을 검토했다. Phase 13은 Phase 14A acceptance 뒤에도 selected
substituted runtime일 때만 Phase 12 evidence를 조건부 소비한다. 기존 review verdict,
implementation, acceptance와 evidence 상태는 변하지 않는다.

## 새 세션 corrective review addendum

후속 새 세션 review는 ALNS-first/C-17 restart 의미가 추가된 target을 기존 v1.2로
계속 식별하면 변경 전후 계약을 구분할 수 없음을 확인했다. Target과 이 review의
current version reference를 v1.3으로 정렬하고, current Phase 11/13/14 label을 각
문서 metadata와 대조했다. `RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`와 해당
finding 본문의 v1.2 표기는 reciprocal evidence-cycle이 실제로 해소된 historical
version을 가리키므로 유지한다.

이 correction은 새 implementation/evidence/review receipt를 만들지 않으며
`CHANGES_REQUIRED`, `BLOCKED_NOT_IMPLEMENTED`, `NOT_PRODUCED`와 모든 provider/C-17/
production gate를 그대로 보존한다.
