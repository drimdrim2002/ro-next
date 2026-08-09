# Phase 08 독립 문서 리뷰 — Application interface와 local 실행

```yaml
document_status: COMPLETE
document_version: 1.1
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "08"
review_date: 2026-07-28
reviewer_role: independent Phase 08 document reviewer
target_document: docs/implementation/phases/phase-08-application-ports-local-runtime.md
target_document_version_after_safe_fixes: 1.3
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
document_verdict: CHANGES_REQUIRED
phase_acceptance_verdict: BLOCKED_NOT_IMPLEMENTED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status_observed: BLOCKED_BY_UNACCEPTED_PREDECESSORS
handoff_status_observed: NOT_READY
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_verdict: PASS_DOCUMENTATION_ONLY_STATUS_UNCHANGED
scheduler_status_change: NOT_AUTHORIZED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
source_commit_observed: 3424277c9c74f8151a83be056a07dd4659331beb
finding_counts:
  critical: 0
  high: 6
  medium: 6
  low: 1
  total: 13
finding_disposition:
  applied_safe_obvious: 7
  recorded_residual_cross_phase_blocker: 6
fake_evidence_detected: false
code_change_reviewed: false
review_revision_reason: FINAL_LIVE_ADJACENT_STATUS_ALIGNMENT
```

## 1. 결론

[Phase 08 v1.2](../phases/phase-08-application-ports-local-runtime.md)은 provider-neutral
application ports와 explicit local runtime을 Phase 07 both-gate 뒤에 배치하고, immutable
artifact → verified read → state guard → publication pointer CAS 순서, idempotency,
cancellation/deadline 분리, local security, reproducibility와 legacy rollback을 구체적인
fixture/oracle로 연결한다. 안전하게 확정할 수 있었던 7건은 target에 직접 반영했다.

문서 verdict는 `CHANGES_REQUIRED`다. 특히 수정 전 문서는 Phase 07
`GateIncomplete`에 존재하지 않는 disposition/failure를 요구해 손실 없는 mapping이
불가능했고, selected-test command가 zero-test false green을 허용했다. 두 결함은 수정했다.
그러나 다음 핵심 계약은 외부 권위 없이는 닫을 수 없다.

1. Phase 03~06 full-solution evaluation owner/API/identity가 확정되지 않아 Phase 07과 실제
   Phase 08 E2E의 authoritative result chain이 시작되지 않는다.
2. Caller가 보낸 `TenantId`와 `ArtifactRef`만으로 tenant authorization closure를 증명할 수
   없고, approved non-ambient access binding이 없다.
3. Storage operation의 missing/denied/corrupt/stale/indeterminate를 application으로 손실 없이
   운반할 approved carrier가 없다.
4. Run-state authorization fence와 published-pointer precondition identity가 분리되지 않았다.
5. Phase 09/10 worker committed-outcome authority primitive와 Phase 09/11 S3 ownership이
   cross-phase decision을 기다린다.

Phase acceptance는 별도로 `BLOCKED_NOT_IMPLEMENTED`다. Actual checkout에는 proposed
reactor/module/port/local adapter/Phase 08 test가 없고 `E-P08-*`도 없다. Root Maven test
1건 성공은 기존 GCP placeholder regression일 뿐 Phase 08 evidence가 아니다.

## 2. Scope, authority와 검증 inventory

### 2.1 완전히 읽고 대조한 권위 source

| Source | 읽은 범위 | Review 적용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | 전체 | Submission→solve→both-gate publication, identity, reproducibility, failure, migration/rollback |
| [Final Domain](../../2026-07-26-domain-design.md) | 전체 | Immutable domain/result identity, termination, verification ceiling와 safe failure |
| [Final Architecture](../../2026-07-26-architecture-design.md) | 전체 | Java 25/Maven DAG, application-owned ports, local reference, CAS, security/evidence |
| [Integrated design](../../architecture-domain-implementation-design.md) | 전체 | Phase 08 application/local runtime, Phase 09 storage, failure/corruption/observability boundary |
| [Question register](../../master-design-open-questions.md) | 전체 | `Q-INFRA-01 RESOLVED`, `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`, gated `C-17` |
| [Master Realization Plan](../master-realization-plan.md) | 전체 | Actual inventory, Phase DAG, entry/exit, work/evidence/blocker/handoff |
| [Phase 07](../phases/phase-07-independent-verification-final-result.md)와 [review](phase-07-review.md) | 전체 | Exact `Publishable`, `VerificationRejected`, `GateIncomplete`; unaccepted upstream gate |
| [Phase 09](../phases/phase-09-object-storage-no-database.md)와 [review](phase-09-review.md) | 최신 계약/review 전체 대조 | Access/failure/publication/worker/provider handoff의 해소·잔존 판정 |
| Existing Phase 00~07 reviews | 각 파일 전체 | Earlier blocker가 accepted input 또는 Phase 08 evidence로 승격되지 않았는지 확인 |

이 문서 세트의 입력 권위가 사용자 선언으로 고정됐다는 규칙을 유지했다. Canonical/Final
문서 metadata의 `REVIEW`는 provenance이며 review 중단 조건이 아니다. 반대로 detailed
Phase 문서의 Java pseudo-signature, future test/command와 planned evidence key는 실제
implementation/evidence가 아니다.

[2026-07-26 Master Design](../../2026-07-26-master-design.md)은
`SUPERSEDED_NOT_AUTHORITY` historical cross-check로만 읽었다.
`docs/codex/`(repo 미존재 가능)는 역사 초안의 drift 확인에만 사용했고 current contract,
package, status 또는 evidence로 복사하지 않았다.

`concurrent review observed`: 같은 scheduler batch에서 인접 Phase/review가 동시에
편집되는 상태를 한 번 관찰했다. 최신 Phase 09 계약과 review를 한 번 대조해 Phase 08
finding의 해소/잔존을 판정한 뒤 인접 whole-file hash/status 재추적을 중단했다. 최신
Phase 09는 adjacent document/section digest acceptance를 제거했으므로 그 문제는
resolved로 판정했다.

Final consistency pass에서는 인접 metadata만 대조해 live status drift를 닫았다. Phase 07,
09, 10 review는 모두 완료됐고 verdict는 `CHANGES_REQUIRED`이며, 각 Phase의 implementation
`NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY`는 그대로다. Target 작성 중
최초 관찰한 Phase 09/10 `READY_FOR_REVIEW` 값은 target metadata의
`historical_authoring_snapshot`에 `HISTORICAL_OBSERVATION_ONLY_NOT_CURRENT_STATUS`로만
보존했다.

### 2.2 Actual Java 25/Maven/source/test/deployment inventory

| 항목 | Actual 관찰 | Phase 08 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`; review 시작 전 `docs/implementation/` 전체가 untracked | 공유 scheduler/user 작업을 보존; 이 review는 허용된 target/review만 작성 |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Root POM의 Java 25/enforcer와 일치; Phase 08 evidence는 아님 |
| Build shape | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT` jar; Maven wrapper와 child modules 없음 | Target의 reactor/module/`./mvnw` command는 future contract |
| Dependencies | Google Workflow/Storage, Jackson, JUnit; shade plugin | Current GCP placeholder inventory; provider-neutral application DAG가 아님 |
| Source/test | Main Java 6개, test Java 1개 | Phase 08 port/local/filesystem/security/Phase 07 integration test 0 |
| Application placeholder | [`AlnsBatchEngine`](../../../../src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java)과 HTTP controllers | UUID/time seed, raw map/double objective와 direct provider behavior를 target authority로 승격 금지 |
| Hidden runtime values | `parallelRuns=8`, `iterations=5000`, workflow timeouts/retries, `System.nanoTime()` seed | Legacy characterization only; `Q-BENCH-02` official/default가 아님 |
| Storage/result | Direct GCS create/list, object-existence status/result | Phase 08 exact ref/verified read/CAS/both-gate publication을 구현하지 않음 |
| Deployment | [`Dockerfile`](../../../../Dockerfile), [`gcp/cloudbuild.yaml`](../../../../gcp/cloudbuild.yaml), [`gcp/workflows/optimization.yaml`](../../../../gcp/workflows/optimization.yaml), [`gcp/README.md`](../../../../gcp/README.md) | GCP placeholder 배포 inventory; target local/security/provider evidence가 아님 |
| Fresh root test | `mvn -B -ntp -Dstyle.color=never test` 성공, tests `1/0/0/0` | Existing `AlnsBatchEngineTest`만 green; `E-P08-*`로 사용 불가 |

## 3. Severity와 review-area 판정

| Severity | 의미 |
|---|---|
| `CRITICAL` | 즉시 잘못된 authoritative publication/corruption을 정상화하며 안전한 last point가 없음 |
| `HIGH` | Both-gate/access/failure/linearizability/evidence gap으로 implementation 또는 acceptance를 차단 |
| `MEDIUM` | Lifecycle, phase ownership, fixture/oracle/command가 실제 defect 검출을 유의미하게 약화 |
| `LOW` | Status/traceability drift가 false claim이나 반복 review cycle을 유발 |

| Review area | 판정 | 근거 |
|---|---|---|
| Source contract/Phase boundary | `BLOCKED AFTER CORRECTION` | F-P08-001, 003~006, 011~012 |
| Phase 07 exact handoff | `ADEQUATE PLAN AFTER CORRECTION` | Three variants와 exact field preservation, publication call 0 oracle |
| Artifact/state/publication lifecycle | `BLOCKED` | Ordering과 CAS plan은 구체적이나 access/failure/precondition contract 미승인 |
| Identity/idempotency | `ADEQUATE PLAN` | Same/same convergence, same/different conflict, locator/time/env exclusion과 exact tests |
| Cancellation/deadline/retry | `ADEQUATE PLAN AFTER CORRECTION` | Intent/actual termination/watchdog/algorithm budget/attempt identity 분리 |
| Security/tenant/local config | `BLOCKED` | Explicit config/no hidden value는 적절; authorization closure는 F-P08-003 |
| Failure/corruption path | `BLOCKED` | Phase 07 mapping은 교정; storage failure carrier는 F-P08-004 |
| Reproducibility/observability | `ADEQUATE PLAN` | Two-workspace oracle와 semantic/observation 분리; actual evidence 없음 |
| Rollback/migration | `ADEQUATE PLAN` | New local unit isolation, legacy immutable baseline, pointer preservation/no destructive cleanup |
| Test command/evidence | `FAIL FOR ACCEPTANCE` | F-P08-002 교정 후 future fail-closed plan; actual Phase 08 tests/evidence 0 |
| Actual repository consistency | `NON-CONFORMANT PLACEHOLDER` | Single root module/direct GCP/1 test이며 planned Phase 08 tree가 없음 |
| Hidden defaults/gated/deferred | `PASS AT DOCUMENT LEVEL` | Legacy values는 characterization, OPEN/GATED/deferred는 owner/restart condition과 함께 보존 |

Critical finding은 없다. Phase 08 implementation과 authoritative pointer가 존재하지 않아 새
오류가 실제 production authority로 commit된 것은 아니다. 이는 high blocker를 낮추거나
current placeholder를 conformant하다고 평가한다는 뜻이 아니다.

## 4. Findings

### F-P08-001 — `GateIncomplete`를 completed rejection shape로 취급했다

- **Severity/status:** `HIGH — APPLIED`
- **Finding:** 수정 전 target은 Phase 07 non-success를 한
  `FinalResultRejection(stage, disposition, failure, candidatePass)` shape로 소비했다.
  `GateIncomplete`에는 disposition과 `VerificationFailure`가 없으므로 이 계약은 field를
  합성하거나 incomplete 정보를 잃게 만든다.
- **Exact evidence/source:** [Phase 07 §7.6~§7.7, §8.4~§8.5](../phases/phase-07-independent-verification-final-result.md)는
  `VerificationRejected(stage, disposition, failure, optional candidate PASS)`와
  `GateIncomplete(stage, SafeIncompleteFailure, optional candidate PASS, LastSafeIdentity)`를
  분리한다. Canonical Master §14.1은 두 verifier PASS만 publishable하다고 고정한다.
- **Correction:** Exhaustive three-way handling을 고정하고 `GateIncomplete`를
  `VERIFICATION_INCOMPLETE` + `Interrupted(VerificationGateIncomplete)`로 map한다.
  존재하지 않는 disposition/normal payload를 만들지 않는다.
- **Applied:** Target §2~§3, §6~§7.7, §11.1/§11.5, WP-08.2, DoD, handoff와 traceability.
- **Residual risk:** Phase 07 actual implementation/evidence가 없으므로 future integration이
  exact variant/field contract test를 통과해야 한다.

### F-P08-002 — Selected-test command가 zero-test false green을 허용했다

- **Severity/status:** `HIGH — APPLIED`
- **Finding:** 수정 전 WP command는 nonexistent planned reactor에 `-am`을 사용하고
  `surefire.failIfNoSpecifiedTests=false`로 선택한 class가 없어도 성공할 수 있었다.
- **Exact evidence/source:** Actual root는 child module/`mvnw`가 없고 test class는
  `AlnsBatchEngineTest` 하나다. Master Realization Plan §9~§11은 discovered class/method,
  zero failure/error/skip와 real evidence를 요구한다.
- **Correction:** Future wrapper를 먼저 고정하고 selected test는 exact owner POM `-f`,
  `-am` 없음, `surefire.failIfNoSpecifiedTests=true`로 실행한다. Full clean reactor
  install/verify를 별도로 요구하고 skip/disable/zero-test를 evidence로 금지한다.
- **Applied:** Target WP-08.0~7 command와 §13 evidence protocol.
- **Residual risk:** Commands는 module/wrapper 생성 전 실행 불가한 future plan이다.
  실제 module에서 discovered method count를 별도 검증해야 한다.

### F-P08-003 — `TenantId`와 `ArtifactRef`만으로 authorization closure가 성립하지 않는다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Finding:** Caller-provided `TenantId`는 routing identity일 뿐 authorization proof가 아니다.
  Proposed `ArtifactRef`는 logical `ArtifactKey`/tenant를 직접 보존하지 않고
  `readVerified(ref)`/`metadata(ref)`에는 approved access carrier가 없다.
- **Exact evidence/source:** Final Architecture §5.5, Integrated §20,
  [Phase 09 §3.1~§3.2/§7.8/§8.3](../phases/phase-09-object-storage-no-database.md)는
  authorization-before-existence와 non-ambient binding을 요구한다.
- **Correction:** Phase 08 Application/Security + Phase 09 Storage가 explicit tenant-scoped
  authorized session/facade 또는 동등한 closure, lifecycle/async propagation과
  missing/mismatch safe failure를 승인해야 한다.
- **Applied:** Target §2.4, §7.1/§7.3, §8.1, §10, §11.3/§11.6~§11.7, WP-08.1/3,
  blocker/handoff/checklist에 fail-closed gate를 기록했다. 임의 field/signature는 추가하지 않았다.
- **Residual risk / last safe:** Backend call과 existence disclosure 0인 상태가 last safe다.
  승인 signature와 no-ambient/no-leak contract tests 전 port 구현을 시작하지 않는다.

### F-P08-004 — Storage operation failure를 lossless하게 운반할 carrier가 없다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Finding:** `ArtifactStore.readVerified`/`metadata`, state와 publisher operation이
  missing/denied/corrupt/stale/visibility-indeterminate를 exhaustive result로 반환하는 shape가
  없다. `ApplicationFailure.AdapterUnavailable`로 collapse하면 retry/security/integrity 의미가
  사라진다.
- **Exact evidence/source:** Final Architecture §5.3,
  Integrated §21과 [Phase 09 finding F-P09-003](phase-09-review.md#f-p09-003--internal-storage-failure와-phase-08-public-result-사이-lossless-carrier가-없다).
- **Correction:** Operation별 checked/sealed result 또는 approved application failure
  carrier, safe fields와 retry disposition의 total mapping을 cross-phase 승인한다.
- **Applied:** Target §7.3/§7.6, §11.3, WP-08.1/3, blocker/handoff/checklist에 no-collapse
  invariant와 test gate를 추가했다. Variant를 임의 확정하지 않았다.
- **Residual risk / last safe:** Failure 시 state/pointer 불변, caller success 0이 last
  safe다. Operation × failure matrix와 compile-time exhaustiveness evidence 전 구현 금지다.

### F-P08-005 — Run-state fence와 publication-pointer precondition identity가 분리되지 않았다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Finding:** `ResultPublisher.compareAndSet(solveId, StateVersion expectedState, result)`는
  run-state guard를 받지만 published pointer의 absent/current conditional identity를
  표현하지 않는다. 하나의 token 재사용이나 hidden adapter precondition은 linearizability와
  lost-response idempotency를 검증할 수 없다.
- **Exact evidence/source:** Target §7.3의 단계 4/5는 run-state re-read와 pointer CAS를
  분리한다. Integrated §13.5와 [Phase 09 finding F-P09-005](phase-09-review.md#f-p09-005--run-state-fence와-publication-pointer-cas-token이-혼재했다)는
  서로 다른 authoritative key/linearization point를 요구한다.
- **Correction:** Phase 08/09/10이 distinct typed run-state fence/publication precondition,
  create-if-absent/CAS, stale/occupied/different-result/response-loss reconciliation을 승인한다.
- **Applied:** Target §7.3, §11.6, blocker/handoff/checklist에 gate와 oracle를 기록했다.
- **Residual risk / last safe:** Verified immutable publishable closure와 published pointer
  없음이 last safe다. Cross-phase signature/consumer receipt 전 publication 구현 금지다.

### F-P08-006 — Upstream full-solution evaluation contract가 닫히지 않았다

- **Severity/status:** `HIGH — RESIDUAL INHERITED BLOCKER`
- **Finding:** Phase 08 real E2E는 Phase 07 publishable result를 소비하지만 그 authority의
  전제인 solution-level evaluation API/owner/identity/reuse/failure/comparator가 Phase 03~06에
  걸쳐 미확정이다.
- **Exact evidence/source:** [Phase 07 review F-P07-005](phase-07-review.md#f-p07-005--solution-level-evaluation-authorityownerapi가-upstream에서-비어-있다)와
  그 blocker table의 `F-P03-004/F-P04-001/F-P05-001/F-P06-004`.
- **Correction:** Upstream owners가 exact aggregate API, profile/evaluation identity,
  verifier reuse와 no-ad-hoc reconstruction tests를 승인해야 한다.
- **Applied:** Target §15 predecessor blocker와 restart condition에 보존했다. 인접 Phase는
  수정하지 않았다.
- **Residual risk / last safe:** Route-level kernel과 ordered routes/bank까지만 safe다.
  Fake aggregate 또는 raw objective로 Phase 07/08 E2E를 green 처리하면 안 된다.

### F-P08-007 — Application lifecycle이 Phase 07 내부 substage를 관찰한 것처럼 주장했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Finding:** 수정 전 lifecycle은 application state를
  `VERIFYING_CANDIDATE → FINALIZING → VERIFYING_RESULT`로 두었지만 Phase 08은 single
  Phase 07 facade output만 받으므로 내부 stage를 실제 state transition으로 관찰할 수 없다.
- **Exact evidence/source:** Phase 07 §7.6 facade와 target §7.7 service composition.
- **Correction:** Application state를 `VERIFYING_FINAL_RESULT` 하나로 두고 exact stage는
  returned rejection/incomplete variant에 보존한다. Exceptional arrows는 alternative branch로
  표현한다.
- **Applied:** Target §6.3~§6.4, WP-08.1과 state tests.
- **Residual risk:** Future Phase 07 API가 explicit progress event를 승인하지 않는 한 fake
  substates를 telemetry/state evidence로 만들지 않는다.

### F-P08-008 — 인접 section digest acceptance가 reciprocal review cycle을 만들었다

- **Severity/status:** `MEDIUM — APPLIED / ADJACENT RESOLVED`
- **Finding:** 수정 전 target은 Phase 07/09 section projection hash를 entry/handoff
  acceptance에 사용했다. 이는 인접 편집 순서가 acceptance를 바꾸는 reciprocal cycle이다.
- **Exact evidence/source:** 사용자 acceptance 제한과 Master Plan의 source/contract/evidence
  trace 원칙. 최신 Phase 09 v1.3도 digest acceptance를 제거했다.
- **Correction:** Canonical source fingerprint는 provenance로 유지하고, 인접 계약은 stable
  section/contract citation + semantic-impact review + accepted artifact/evidence identity로
  trace한다.
- **Applied:** Target metadata/§1/WP-08.0/§13/§15~§18.
- **Residual risk:** Stable citation도 의미 drift를 자동 검출하지 않으므로 entry 시 semantic
  impact와 accepted implementation contract identity를 확인해야 한다.

### F-P08-009 — Planned fixture/contract module path가 Maven 표준 source set과 충돌했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Finding:** 수정 전 tree는 별도 Maven module인데도 test fixture를
  `build/test-fixtures/src/testFixtures/java`, contract tests를
  `build/port-contract-tests/src/main/java`에 두어 별도 plugin/config 없이는 test discovery와
  Surefire 실행을 보장하지 못했다.
- **Exact evidence/source:** Actual POM은 custom source-set plugin이 없고 standard
  `src/main/java`/`src/test/java`만 사용한다. Final Architecture §2/§5.6은 build DAG와
  independent test module을 요구한다.
- **Correction:** 두 module의 executable tests를 standard `src/test/java`에 둔다.
- **Applied:** Target §5.2 proposed tree.
- **Residual risk:** Reactor 생성 시 fixture sharing 방식과 dependency scope를 architecture
  test로 검증해야 한다.

### F-P08-010 — CLI cancellation이 same-process scope와 모순됐다

- **Severity/status:** `MEDIUM — APPLIED`
- **Finding:** 수정 전 mandatory CLI `--cancel`은 local runtime을 same-process
  single-worker/in-process state로 제한한 계약과 충돌했다. Separate CLI process가 durable
  shared cancellation authority 없이 실행 중 process를 안전하게 cancel할 수 없다.
- **Exact evidence/source:** Target §2.2, §7.4, §8~§9 local scope; Final Architecture
  §3.2/§3.6 cancellation identity 원칙.
- **Correction:** Mandatory CLI는 solve/status/result와 blocking solve의 cooperative interrupt로
  제한한다. Separate-process cancel은 cross-process state/lease/CAS evidence가 있을 때만
  추가한다.
- **Applied:** Target §8.4, WP-08.4와 CLI tests.
- **Residual risk:** Future durable cancel endpoint가 intent를 actual termination으로
  오인하지 않도록 exact lifecycle/receipt test가 필요하다.

### F-P08-011 — Phase 09와 Phase 11의 S3 implementation/evidence ownership이 충돌한다

- **Severity/status:** `MEDIUM — RESIDUAL CROSS-PHASE BOUNDARY BLOCKER`
- **Finding:** Integrated/Master Plan/수정 전 Phase 08 handoff는 Phase 09
  filesystem/S3 same-suite를 요구하지만 Phase 09는 production/provider work를 Phase 11로
  분리한다. `Q-INFRA-01 RESOLVED`는 AWS target 선택이지 구현 Phase 자동 선택이 아니다.
- **Exact evidence/source:** Master Plan Phase 09, Integrated §13.12,
  [Phase 09 finding F-P09-001](phase-09-review.md#f-p09-001--phase-09와-phase-11의-s3-ownership이-canonical-realization-source와-충돌한다).
- **Correction:** Architecture + Phase 08/09/11 owners + scheduler가 one aligned decision으로
  owning module, environment/security/parity evidence를 배정한다.
- **Applied:** Target §2.3, §15, §16.2, checklist는 provider-neutral/local last safe와
  conditional ownership만 기록한다. S3 owner를 임의 확정하지 않았다.
- **Residual risk / last safe:** Phase 08 provider-neutral ports + local single-JVM
  reference가 last safe다. Decision 전 S3 implementation/acceptance claim은 금지다.

### F-P08-012 — Phase 09/10 worker committed-outcome authority primitive가 없다

- **Severity/status:** `MEDIUM — RESIDUAL DOWNSTREAM BLOCKER`
- **Finding:** Phase 10 declared-worker fan-in은 exact committed outcome을 요구하지만 current
  Phase 08 storage projection에는 worker pointer create/CAS operation이 없다.
- **Exact evidence/source:** Integrated §13.5/§14,
  [Phase 09 finding F-P09-004](phase-09-review.md#f-p09-004--worker-committed-outcome-pointer를-권위화할-application-operation이-없다).
- **Correction:** Phase 09/10 review가 immutable create-once 또는 typed CAS primitive,
  same/different digest, response-loss와 exact read semantics를 승인한다.
- **Applied:** Target §15/§16.2/checklist에 downstream blocker와 last safe를 기록했다.
  Phase 08 single-worker core signature를 임의 확장하지 않았다.
- **Residual risk / last safe:** Verified immutable worker outcome은 orphan/reference일 뿐
  committed/completeness authority가 아니다.

### F-P08-013 — Review metadata와 actual inventory 상태가 문서 사실과 달랐다

- **Severity/status:** `LOW — APPLIED`
- **Finding:** 수정 전 target은 review가 없는 `READY_FOR_REVIEW` 상태였고 actual single-root
  placeholder와 future reactor/evidence의 분리가 validation gate에 충분히 드러나지 않았다.
- **Exact evidence/source:** Target v1.0 metadata, actual root POM/source/test/deployment
  inventory, Master Plan §2/§5~§11.
- **Correction:** Target v1.1 initial safe fixes에 review link/verdict,
  implementation/evidence/entry/handoff status 분리, actual inventory와 future
  command/evidence 제한을 명시하고 final consistency pass에서 v1.2 live status로 정렬한다.
- **Applied:** Target metadata/§1/§4/§13~§18.
- **Final consistency update:** Target v1.2 metadata, §3.2와 §4 live adjacent status를
  completed review verdict에 맞췄다. Historical authoring observation은 별도 non-authoritative
  snapshot으로 격리했다.
- **Residual risk:** Scheduler만 authoritative progress/status를 갱신할 수 있다. 이 review는
  implementation start/acceptance를 선언하지 않는다.

## 5. Target에 반영한 변경 요약

| 변경 | 적용 section | 성격 |
|---|---|---|
| Review v1.2 metadata와 live/historical status 축 분리 | Metadata, §1, §3.2, §4 | Completed neighbor review verdict 반영; 구현/evidence는 `NOT_STARTED`/`NOT_PRODUCED` 유지 |
| Phase 07 three-variant lossless mapping | §2~§3, §6~§7, §11~§18 | `GateIncomplete` field synthesis와 fake publication 차단 |
| Fail-closed Maven selected-test protocol | WP-08.0~7, §13 | Zero-test/`-am` false green 차단 |
| Explicit tenant access gate | §2/§7~§11/§15~§18 | Blocker/test 추가, public signature 미발명 |
| Lossless storage failure/precondition gates | §7/§11/§15~§18 | No-collapse/linearizability oracle, exact API는 blocker |
| Honest application lifecycle | §6, WP-08.1 | Fake Phase 07 internal state 제거 |
| Standard Maven test-source paths | §5 | Future test discovery 가능 구조 |
| CLI same-process cancellation scope | §8/WP-08.4 | Separate-process hidden authority 제거 |
| Adjacent digest acceptance 제거 | Metadata/§1/§13~§18 | Stable citation/semantic review/artifact evidence로 대체 |
| Phase 09/10/11 handoff blockers | §15~§18 | Worker/S3 boundary를 hidden default 없이 보존 |

Java/POM/README/progress/인접 Phase/review/source/deployment는 수정하지 않았다.

## 6. 미해결 blocker와 restart

| Blocker | Owner | 현재 막는 범위 | 마지막 안전 지점 | Restart condition |
|---|---|---|---|---|
| Phase 00~07 accepted evidence와 scheduler task 부재 | Predecessor owners + scheduler | 모든 Phase 08 real code/E2E/acceptance | Reviewed Phase 08 document와 read-only inventory | Accepted predecessor bundles/reviews, exact task와 separated roles |
| Full-solution evaluation owner/API gap | Phase 03 Core/Evaluation + Phase 04 Profile + Phase 05/06 Algorithm | Phase 07 authority와 Phase 08 real solve/E2E | Route-level kernel, ordered routes/bank | Exact aggregate identity/API/reuse/failure/comparator + cross-phase tests |
| Actual Phase 07 unaccepted | Verification/Result owner | Both-gate integration/publication | Proposed exact three-variant adapter | `E-P07-*`, accepted review와 compatible handoff |
| Tenant authorization closure 부재 | Phase 08 Application/Security + Phase 09 Storage | Storage/state/publication implementation과 `G08-SECURITY` | Backend call/existence disclosure 0 | Approved non-ambient binding/lifecycle + missing/mismatch/no-leak tests |
| Lossless storage failure carrier 부재 | Phase 08 Application + Phase 09 Data Integrity | Port implementation/fault/corruption evidence | Failure 시 state/pointer 불변 | Exhaustive operation × failure carrier and no-collapse tests |
| Publication precondition identity 부재 | Phase 08/09/10 | Publisher linearizability/idempotency | Immutable publishable closure, pointer 없음 | Distinct typed fence/precondition + stale/occupied/lost-response tests |
| Worker committed-outcome primitive 부재 | Phase 09 Storage + Phase 10 Coordinator | Declared-worker commit/read/fan-in | Verified orphan outcome, committed authority 없음 | Approved create/CAS primitive + same/different digest consumer receipt |
| Phase 09/11 S3 ownership 충돌 | Architecture + Phase 08/09/11 + scheduler | Provider suite/evidence와 Phase 09 exit/Phase 11 entry | Provider-neutral/local single-JVM reference | Aligned source decision, owning module, executable same-suite evidence |
| Fingerprint/SolveId, CLI schema, production limits OPEN | Architecture/Data/API/Product/Security/Operations | External compatibility/production use | Versioned injected TEST_ONLY values; no defaults | Approved ADR/schema/measured policy와 migration |
| Local filesystem CAS/atomicity | Storage/Platform | Filesystem adapter acceptance | In-memory contract only | Target filesystem capability/crash/concurrency evidence |
| `Q-BENCH-02`, decimal `D/U`, `C-17`, `Q-VAR-01` | Registered Product/Benchmark/Input/Algorithm owners | Official run/cutover/later feature | TEST_ONLY integer fixture, ALNS-only/current domain | Question register의 exact experiment/restart evidence와 approval |

Rollback은 blind overwrite/delete가 아니다. New local entrypoint를 stop/disable하고 기존
placeholder source/artifact를 보존한다. 새 immutable artifact가 생겼어도 accepted pointer가
없으면 unpublished/orphan이며 normal result로 복구 추론하지 않는다. Production traffic,
GCP deployment, legacy result rewrite와 evidence cleanup은 이 review가 승인하지 않았다.

## 7. 실제 verification command와 결과

### 7.1 Build/source characterization

| Command | Actual result | 해석 |
|---|---|---|
| `java -version` | Exit 0; Corretto OpenJDK `25.0.3` | Java 25 baseline 일치 |
| `mvn -version` | Exit 0; Maven `3.9.14`, Java `25.0.3`, macOS aarch64 | Root toolchain 일치 |
| Canonical six-file `shasum -a 256` | Exit 0; target metadata의 six fingerprints와 exact 일치 | Canonical provenance 확인; neighbor acceptance hash 아님 |
| `rg --files src gcp` + POM inspection | Main Java 6, test Java 1, GCP files 3; single root jar | Target module/test/deployment inventory 확인 |
| `test ! -e mvnw`와 planned module path checks | Wrapper와 all planned Phase 08 child modules absent | Future commands는 현재 evidence가 아님 |
| `mvn -B -ntp -Dstyle.color=never test` | Exit 0; tests `1`, failures `0`, errors `0`, skipped `0`; `BUILD SUCCESS`; 0.828 s | Root placeholder regression only; `E-P08-*` 아님 |

Historical Phase 08 review-time fingerprints observed (현재 implementation-direction
revision의 master plan hash가 아님):

```text
58554334b9f27586c93a685adc0facf0fbd7e79576c18890f0ac13891b2f803b  docs/master-design.md
1870662f85a08cc9a1e48a1974b96278eccddfd1519721d71b356c56034ecaab  docs/deprecated/2026-07-26-domain-design.md
3d4dbbfc7e4cbdb2f3985378d84fd5f717db770b04131573c00ed354a9f41614  docs/deprecated/2026-07-26-architecture-design.md
ec513ac1b0bacd88149683bf48c36f7e6edcd53a9232498597e3b0d57c585875  docs/deprecated/architecture-domain-implementation-design.md
b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b  docs/deprecated/master-design-open-questions.md
5921213ae419b9398bde8c91c3d6ada5aa64bf22a9b823e5b3889e1642085c05  docs/implementation/master-realization-plan.md
```

### 7.2 Post-write document validation

| Validation | Actual result |
|---|---|
| 두 허용 파일 non-empty | PASS — `test -s` 두 건 exit 0 |
| Required metadata/section/finding count | PASS — required metadata/§1~§8 존재, finding heading 13개가 metadata total과 일치 |
| Relative links/local anchors | PASS — local Markdown target/GFM-style heading validator: `OK links+anchors files=2` |
| Fence parity/trailing whitespace | PASS — target fence 96, review fence 4로 모두 even; trailing whitespace 0 |
| Hidden default/gate bypass/reciprocal neighbor hash | PASS — exact adjacent digest field 0; legacy numeric/default hit는 characterization/TEST_ONLY/OPEN/금지문뿐; accepted/evidence bypass claim 0 |
| 허용 경로 한정 write/diff audit | PASS WITH BASELINE LIMITATION — edit operation은 target/review 두 경로뿐; scoped status는 두 파일 모두 `??`, tracked diff name 0. Review 시작 전 implementation tree 전체가 untracked여서 Git index만으로 pre-existing target 내용과 이번 delta를 분리할 수 없음 |
| `git diff --check` | PASS — scoped command exit 0; 두 untracked 파일의 `git diff --no-index --check /dev/null <file>` whitespace diagnostic 0 |

## 8. 최종 verdict와 handoff 제한

Document verdict는 `CHANGES_REQUIRED`, Phase acceptance는
`BLOCKED_NOT_IMPLEMENTED`, Phase 09 handoff는 `NOT_READY`다. Target은 안전 교정 뒤
implementation-start contract로 더 명확해졌지만 residual 6건을 구현자가 임의 Java type,
caller tenant trust, ambient context, generic exception, provider default 또는 fake test로
메우면 안 된다.

Phase 09/10은 accepted handoff 전 다음을 소비했다고 주장할 수 없다.

- Actual `ArtifactStore`/`RunStateRepository`/`ResultPublisher` implementation
- Tenant-scoped non-ambient authorization closure
- Lossless storage failure surface
- Distinct run-state/publication precondition identities
- Worker committed-outcome authority primitive
- Real Phase 07 three-variant integration
- `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`
- Approved Phase 09/11 S3 ownership/parity evidence

Restart 시 reviewer는 canonical source fingerprint와 stable cited contract의 semantic impact를
확인하고, actual owner module에서 discovered test class/method count와
failure/error/skipped를 fail-closed 대조해야 한다. Planned tree, pseudo-signature, root
placeholder build success, object existence와 reciprocal digest는 implementation,
publication 또는 Phase acceptance evidence가 아니다.

## 9. ALNS-first direction revision review

Phase 08 v1.3의 local reference가 optimizer/MIP/license/native/cloud/production
authority 없이 Phase 06→07 경로를 실행하고, declared run 전부의 fixture, seed,
hardware/runtime, work/resource, objective, verifier와 trace digest를 Phase 14A에
넘기는 계약을 `PASS`로 검토했다.

Local E2E 또는 `win_poc_case_floor.json` 단일 run은 그 자체로 benchmark acceptance나
Phase 13 open receipt가 아니다. 기존 `CHANGES_REQUIRED`, implementation
`NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY`는 유지된다.
