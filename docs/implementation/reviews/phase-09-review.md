# Phase 09 독립 리뷰 — DB 없는 object storage

> 검토 대상 부제: Database 없는 object storage

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "09"
review_date: 2026-07-28
reviewer_role: independent Phase 09 reviewer
target_document: docs/implementation/phases/phase-09-object-storage-no-database.md
target_document_version_after_safe_fixes: 1.3
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
document_verdict: CHANGES_REQUIRED
phase_acceptance_verdict: BLOCKED_NOT_IMPLEMENTED
implementation_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status_observed: BLOCKED_BY_UNACCEPTED_PHASE_08
handoff_status_observed: NOT_READY
review_batch_status_observed: 15_OF_15_PHASE_REVIEWS_COMPLETE
scheduler_status_change: NOT_AUTHORIZED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
source_commit_observed: 3424277c9c74f8151a83be056a07dd4659331beb
finding_counts:
  critical: 0
  high: 5
  medium: 2
  low: 1
  total: 8
finding_disposition:
  applied_safe_obvious: 3
  residual_cross_phase_blocker: 5
fake_evidence_detected: false
code_change_reviewed: false
```

## 1. 결론

[Phase 09 v1.3](../phases/phase-09-object-storage-no-database.md)은 database나
database-like hidden index 없이 immutable content와 authoritative pointer를 분리하고,
exact-key verified read, one-key conditional create/CAS, declared reference, listing/event
비권위, duplicate convergence, corruption/quarantine, crash recovery, tenant/security와
retention을 구체적인 fixture와 oracle로 연결한다. 현재 GCS placeholder의 prefix fan-in,
unconditional result write와 object-existence status를 target contract로 승격하지 않은 점도
타당하다.

그러나 문서 verdict는 `CHANGES_REQUIRED`다. 다음 5건은 Phase 09만 수정해 닫을 수 없다.

1. Master realization/Integrated/Phase 08은 Phase 09에 filesystem/S3 same-suite 결과를 요구하지만
   Phase 09 v1.0은 S3를 Phase 11로 미뤘다.
2. Phase 08 public storage port에 tenant/access parameter가 없고 Phase 09의 internal
   `StorageAccessContext`를 non-ambient하게 전달하는 승인 계약이 없다.
3. Phase 09의 typed storage failure를 Phase 08 result/`ApplicationFailure`로 손실 없이 전달할
   carrier가 없다.
4. Worker committed-outcome pointer를 exact하게 권위화할 operation이 Phase 08/09 port에 없다.
5. `ResultPublisher.expectedState`의 run-state authorization fence와 published-pointer 자체의
   conditional token이 한 `StateVersion` 설명 안에서 혼재했다.

Source로 답이 안전하고 명백한 3건은 target에 직접 정정했다. `OpaqueLocator`를 protected
semantic projection에서 제외했고, `ObjectProfileCatalog`와 exact profile contract suite를
복원했으며, 인접 Phase document/section digest를 acceptance 조건으로 쓰는 구조를 제거했다.
Cross-phase 5건은 owner, last safe point와 restart condition을 target §3/§4/§8/§10~§16에
기록했지만 residual blocker로 남는다.

Phase acceptance는 별도로 `BLOCKED_NOT_IMPLEMENTED`다. Target reactor/module/type/test,
`E-P09-*`, accepted Phase 00/07/08 input과 scheduler task가 없다. Root `mvn verify`의
placeholder test 1건 성공은 Phase 09 contract evidence가 아니며, 존재하지 않는 planned
module command는 실제로 exit 1이었다.

## 2. Review scope와 authority

### 2.1 권위 source

| Source | 완전히 읽고 대조한 범위 | Review 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §1~§4.6, §10.3, §13~§17 | Immutable provenance, provider isolation, two-gate publication, logical state/pointer와 gate 보존 |
| [Final Domain](../../2026-07-26-domain-design.md) | §1, §3, §7~§8, §15~§18 | Semantic identity, final result authority, failure/evidence ceiling |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §1~§3.6, §5~§6.5 | Java 25/Maven DAG, application-owned ports, CAS, security, test/ADR |
| [Integrated design](../../architecture-domain-implementation-design.md) | §1~§3, §12~§14, §19~§28 | No-DB exact key/CAS, object profile, filesystem/S3 suite, Phase 08/10 boundary, failure/corruption |
| [Question register](../../master-design-open-questions.md) | 전체 28개 항목과 §3~§5 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`; `Q-INFRA-01`, `Q-BENCH-02`, `Q-VAR-01` |
| [Master Realization Plan](../master-realization-plan.md) | §2~§15, 특히 Phase 08~10 | Actual inventory, Phase DAG, Phase 09 filesystem/S3 output, evidence/DoD/rollback |
| [Implementation map](../README.md) | §3~§7 | User-locked authority, canonical filename, review/status 규칙 |
| [Execution tracker](../execution-progress-and-results.md) | §2, §5~§9 | Phase 09 `PLANNED`, task `TBD`, scheduler-only status |
| [Phase 08](../phases/phase-08-application-ports-local-runtime.md) | §6.3~§7.6, §8.3, §9.1~§9.3, §16.2 | Storage port signature, result sequence, exact profile, local handoff와 filesystem/S3 expectation |
| [Phase 10](../phases/phase-10-provider-neutral-coordinator.md) | §6.1~§7.4, §14.1 | Declared worker identities, committed outcomes, exact read와 CAS consumer |
| [Phase 07](../phases/phase-07-independent-verification-final-result.md)와 [review](phase-07-review.md) | Target §7.6/§8.4~§8.5/§13~§16, review 전체 | Both-gate publishable result와 unresolved upstream gate를 accepted input으로 오인하지 않음 |

이 문서 세트의 입력 권위가 사용자 선언으로 고정됐다는 규칙을 적용했다. Canonical/Final
source의 `REVIEW` metadata는 provenance로 보존하지만 review 중단 조건으로 사용하지 않았다.
반대로 detailed phase 문서의 pseudo-signature, future command와 planned evidence key를
implementation/evidence로 승격하지 않았다.

`Q-INFRA-01`은 AWS S3 + Step Functions + Lambda target 선택으로 `RESOLVED`지만 실제
implementation/cutover approval은 여전히 gated다. `Q-BENCH-02` official 실행 수치는 OPEN,
`Q-VAR-01`과 multi-trip/rotation은 deferred, `C-17` route pool/MIP는 gated로 보존했다.

### 2.2 Historical cross-check

[2026-07-26 Master Design](../../2026-07-26-master-design.md)은
`SUPERSEDED_NOT_AUTHORITY`로만 대조했다. 관찰 SHA-256은
`5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a`다.
`docs/codex/`는 역사 implementation/coordinator 초안의 존재와 drift만 확인했고 current
contract, package, status 또는 evidence로 인용하지 않았다.

인접 Phase whole-file digest나 reciprocal section projection digest를 acceptance로 사용하지
않았다. Canonical source fingerprint는 provenance/drift 확인용으로 검증하고, Phase handoff는
stable named section citation과 이후 accepted artifact/evidence identity로만 표현했다.

### 2.3 Actual repository/build/source/test/deployment inventory

`concurrent review observed`: 같은 scheduler batch에서 인접 Phase/review가 동시에 편집되는
상태를 한 번 관찰했다. Authoring-time status는 historical snapshot으로만 보존한다. Final
consistency audit에서 Phase 00~14 review `15/15 COMPLETE`와 Phase 07/08/10의 live verdict를
한 번 확인했으며, 인접 whole-file hash 재추적 없이 위 finding의 잔존 여부를 유지했다.

| 항목 | Actual 관찰 | Phase 09 판정 |
|---|---|---|
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`; review 시작 전 `docs/implementation/` 전체가 untracked | 공유/사용자 작업을 보존하고 허용된 target/review 두 파일만 수정 |
| Review batch | Phase 00~14 review `15/15 COMPLETE` | Document review 완료일 뿐 implementation/evidence/acceptance 완료가 아님 |
| Adjacent verdict | Phase 07 `CHANGES_REQUIRED/BLOCKED_NOT_IMPLEMENTED`; Phase 08 `CHANGES_REQUIRED/BLOCKED_NOT_IMPLEMENTED`; Phase 10 `CHANGES_REQUIRED/NOT_RECOMMENDED` | Phase 07/08/10 target은 모두 `NOT_STARTED`/`NOT_PRODUCED`/`NOT_READY`; Phase 09 entry/handoff blocker 유지 |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | [POM](../../../pom.xml)의 Java 25/Maven enforcer와 일치; P09 evidence는 아님 |
| Reactor | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT` | `rpdptw/application`, `adapters/object-*`, `build/port-contract-tests`가 없음 |
| Dependency | Direct Google Workflow/Storage, Jackson, JUnit | Provider SDK가 placeholder application adapter에 직접 노출; DB/JDBC/JPA dependency는 관찰되지 않음 |
| Main/test | Main Java 6개, test 1개 | Target storage port/backend/profile/CAS/fault test가 없음 |
| Placeholder payload | [`AlnsBatchEngine`](../../../src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java)이 synthetic `double` objective와 `Map<String,Object>`를 생성 | Canonical immutable artifact/result가 아님 |
| Candidate write | [`OptimizationWorkerController`](../../../src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java)가 raw request/run key에 GCS `storage.create` | Conditional create, digest, tenant identity와 conflict oracle 없음 |
| Fan-in | 같은 controller가 `candidates/{requestId}/`를 prefix-list하고 보이는 최소 raw objective를 선택 | Declared worker set/exact-key authority와 정면 충돌 |
| Result authority | Unconditional `results/{requestId}.json` write, [`OptimizationApiController`](../../../src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java)는 object 존재로 `RUNNING`/result 판정 | Immutable payload와 authoritative publication pointer/CAS가 결합·부재 |
| Hidden defaults | `parallelRuns=8`, `iterations=5000`, seed=`System.nanoTime()` | Current placeholder characterization이며 canonical official/default로 승격 금지 |
| Deployment | [`gcp/workflows/optimization.yaml`](../../../gcp/workflows/optimization.yaml), [`gcp/cloudbuild.yaml`](../../../gcp/cloudbuild.yaml), [`gcp/README.md`](../../../gcp/README.md) | Workflow parallel HTTP/finalize와 GCS `objectAdmin`; P09 no-DB contract/security evidence가 아님 |
| DB/state store scan | POM/source/deployment에 JDBC/JPA/SQL/Redis/Firestore/DynamoDB 등 DB 구현 미관찰 | Hidden DB는 현재 없지만 raw GCS objects/list/existence가 hidden authority 역할을 하는 placeholder |
| Existing build artifact | Ignored `target/` 존재, review 중 root build 재실행 | Fresh command 결과는 아래 기록; stale artifact를 P09 evidence로 사용하지 않음 |

Current source는 database를 추가하지 않았지만 storage contract를 구현한 것도 아니다.
`storage.list` fan-in과 result object existence가 사실상의 discovery/status authority이고,
provider SDK default create/visibility/overwrite semantics에 의존한다. Phase 09 target은 이를
compatibility requirement로 보존하면 안 된다.

## 3. Severity와 판정 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 현재 문서만으로 즉시 잘못된 authoritative publication/corruption을 정상화하며 안전한 last point가 없음 |
| `HIGH` | 핵심 access/failure/CAS/phase ownership/handoff gap으로 구현·acceptance를 차단 |
| `MEDIUM` | Identity/integrity/필수 port 책임 또는 defect-detection suite를 유의미하게 약화 |
| `LOW` | Status/trace/fingerprint 방식이 false acceptance나 지속적 reciprocal drift를 유발할 수 있음 |

### 3.1 Coverage disposition

| Review area | Document-level 판정 | Evidence/잔존 |
|---|---|---|
| Source contract/invariant/Phase boundary | BLOCKED | F-P09-001~005; Phase 08/10 handoff의 access/failure/worker/publish contract 미확정 |
| Immutable content vs authoritative pointer/CAS | ADEQUATE PLAN | Target §7.1/§7.4~§7.5/§8.6/§9; object existence는 authority가 아니지만 implementation/evidence 없음 |
| Exact-key read vs listing/event hint | ADEQUATE PLAN | Target §7.6/§10.6; normal path list API 금지와 noise fixture가 있으나 current source는 실제 prefix-list |
| Consistency/idempotency/concurrent writers | ADEQUATE PLAN WITH BLOCKER | Same/same, different, stale/lost response oracle는 구체적; worker/publish precondition은 F-P09-004/005 |
| Partial write/crash recovery/corruption | ADEQUATE PLAN | Staging/orphan/pointer-before-after/tamper/quarantine oracle가 exact; evidence `NOT_PRODUCED` |
| Artifact identity/lifecycle/retention | ADEQUATE AFTER SAFE FIX | Locator-free projection F-P09-006 반영; hold/root/grace/conditional delete와 preserve rollback 유지 |
| Security/tenant isolation | BLOCKED | Tenant-first/redaction/classification은 구체적이지만 authorization transport는 F-P09-002 |
| Failure taxonomy | BLOCKED | Taxonomy는 구체적이지만 public lossless carrier는 F-P09-003 |
| Observability | ADEQUATE PLAN | Safe disposition, pseudonymous identity, operation, redacted audit와 fault trace 계획; actual telemetry evidence 없음 |
| Reproducibility | ADEQUATE PLAN | TEST_ONLY fixture, manual clock/barrier, toolchain/filesystem/capability fingerprint와 exact counts 요구; hidden official default 없음 |
| Rollback/restart | ADEQUATE PLAN | Immutable graph/pointer preservation, no blind overwrite/delete, owner/last-safe/restart 표 존재 |
| False-green/evidence | FAIL FOR ACCEPTANCE | Root test 1건 외 target test/module 0; future/TBD command와 planned evidence를 사실로 주장하지 않음 |
| Actual repository/deployment | NON-CONFORMANT PLACEHOLDER | Direct GCS, prefix fan-in, unconditional result, existence status, broad object role; migration characterization만 허용 |

Critical finding은 없다. Phase 09 implementation이 존재하지 않고 entry gate가 닫혀 있어
잘못된 새 pointer가 실제로 commit된 것은 아니다. 이 사실은 high blocker를 낮추거나
placeholder를 안전하다고 평가한다는 뜻이 아니다.

## 4. Findings

### F-P09-001 — Phase 09와 Phase 11의 S3 ownership이 canonical realization source와 충돌한다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** 수정 전 target §1.1/§2.3/§3.1/§13은 Phase 09를 memory/local로 제한하고
  S3 adapter를 Phase 11로 미뤘다. 그러나 [Master Realization Plan — Phase 09](../master-realization-plan.md#phase-09--db-없는-object-storage)는
  “Filesystem/S3 backend contract”와 같은 abstract suite를 Phase 09 output/step으로 두고,
  [Integrated §13.12](../../architecture-domain-implementation-design.md#1312-phase-9-gate)는
  filesystem reference와 S3 backend가 같은 suite를 통과해야 한다고 명시한다.
  [Phase 08 §16.2](../phases/phase-08-application-ports-local-runtime.md#162-next--actual-but-unaccepted-phase-09-storage-contract)도
  filesystem/S3 conditional operations를 Phase 09에 넘긴다.
- **Correction required:** Architecture + Phase 08/09/11 owners + scheduler가 S3 adapter,
  environment/security/parity evidence를 어느 Phase가 소유하는지 하나의 decision으로 승인하고
  Plan/Integrated/Phase 08/09/11을 정렬해야 한다. 선택은 `Q-INFRA-01 RESOLVED`와 Phase 11
  production/distribution gate를 훼손하지 않아야 한다.
- **Applied locally:** Target §1.1, §2.3, §3.1, §4, §10~§16에서 local-only 완료 claim을
  boundary blocker로 바꾸고 `TBD_BY_APPROVED_PHASE_09_11_BOUNDARY`를 실행 명령이 아닌
  blocker 표기로 추가했다.
- **Residual risk / last safe / restart:** Last safe point는 provider-neutral contract와
  memory/local planned suite이며 Phase 09 acceptance는 닫혀 있다. Decision ref, owning module,
  executable command, environment fingerprint, same-suite evidence가 승인된 뒤 WP-09.0부터
  restart한다.

### F-P09-002 — Tenant authorization을 public port에 non-ambient하게 전달할 계약이 없다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target §3.2 invariant 1/20과 §7.8은 authorization-before-existence를
  요구한다. 반면 [Phase 08 §7.3](../phases/phase-08-application-ports-local-runtime.md#73-artifactstorage-contracts-produced-for-phase-09)의
  `ArtifactStore.readVerified(ArtifactRef)`, `metadata(ArtifactRef)`와 publisher/state signature에는
  caller/access parameter가 없다. Target §8.3 v1.0은 internal `StorageAccessContext`를
  “binding”한다고만 했고 누가 어떤 lifetime으로 전달하는지 설명하지 않았다.
- **Correction required:** Phase 08 Application + Phase 09 Security/Data Integrity가 explicit
  tenant-scoped authorized session/facade 또는 동등한 non-ambient binding, missing/mismatch
  failure, async/retry propagation과 lifecycle을 승인해야 한다. Global/static/`ThreadLocal`/
  implicit request context는 금지한다.
- **Applied locally:** Target §3.1~§3.2, §4, §6.1, §7.8, §8.3, §10.8/§10.11,
  WP-09.0/09.5, structural/exit/blocker/handoff/traceability에 fail-closed requirement와
  exact oracle를 추가했다. Phase 08 signature는 scope대로 수정하지 않았다.
- **Residual risk / last safe / restart:** Last safe point는 backend call 0과 unpublished
  state다. Cross-phase signature/binding approval, missing/mismatch/backend-call-count tests와
  redaction evidence 전에는 WP-09.1 이후를 시작하지 않는다.

### F-P09-003 — Internal storage failure와 Phase 08 public result 사이 lossless carrier가 없다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target §3.3은 `ACCESS_DENIED`, `CORRUPT`,
  `VISIBILITY_INDETERMINATE`, `PARTIAL_WRITE_ABORTED`, `UNSUPPORTED_CAPABILITY` 등을 분리한다.
  Target §8.4의 public `ArtifactPutResult`는 `Created/AlreadyPresent/Conflict`뿐이고
  `StorageFailure`는 internal hierarchy다. Phase 08 `ApplicationFailure` review 없이 variant를
  추가하지 않겠다는 제한은 맞지만 public caller에 failure가 어떻게 도달하는지 total mapping이
  없었다.
- **Correction required:** Phase 08/09 owner가 각 public operation별 checked/sealed
  result 또는 approved application-failure carrier와 exhaustive mapping을 승인한다.
  Denied/corrupt/indeterminate/partial/unsupported를 generic unchecked exception,
  `Conflict`, `NOT_FOUND` 또는 empty result로 축소하지 않는다.
- **Applied locally:** Target §3.1~§3.3, §4, §8.4, §10.3, WP-09.0/09.3/09.5,
  command/evidence/exit/anti-pattern/blocker/handoff/traceability에 total-mapping gate와
  `StorageFailureSurfaceContract` oracle를 추가했다.
- **Residual risk / last safe / restart:** Last safe point는 operation 실패 시 pointer/state
  불변과 caller success 0이다. Operation × failure matrix, compile-time exhaustiveness,
  provider exception mapping과 no-collapse tests가 승인돼야 WP-09.2를 시작한다.

### F-P09-004 — Worker committed-outcome pointer를 권위화할 application operation이 없다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** Target §7.4~§7.6은 worker outcome payload/ref 뒤 `committed` pointer
  CAS와 Phase 10 exact read를 요구한다. [Integrated §13.5](../../architecture-domain-implementation-design.md#135-immutable-artifact와-mutable-pointer-분리)도
  worker committed outcome pointer를 제한된 mutable/CAS state로 분류한다.
  [Phase 10 §6.3](../phases/phase-10-provider-neutral-coordinator.md#63-lifecycle과-stateaction-commit)은
  `CommittedWorkerOutcomeRef`를 fan-in input으로 소비한다. 그러나 target §8.5의
  `ArtifactStore`, read-only `RunArtifactRepository`, solve-level `RunStateRepository`,
  `ResultPublisher`에는 worker commit create/CAS operation이 없다.
- **Correction required:** Phase 08/09/10 review가 logical-key immutable create-once,
  typed worker-record CAS 또는 승인된 repository projection 중 하나를 선택하고 exact identity,
  same/different digest, response loss, conflict/failure와 read contract를 고정해야 한다.
- **Applied locally:** Target §3.1~§3.2, §4, §8.5, §10.4~§10.5, WP-09.0/09.4,
  exit/blocker/handoff/traceability에 unresolved operation과 `WorkerCommitContract`를
  기록했다. 임의 pseudo-signature는 확정하지 않았다.
- **Residual risk / last safe / restart:** Last safe point는 verified immutable worker outcome이
  orphan/referenceable일 뿐 committed/completeness authority가 아닌 상태다. Approved primitive,
  same/different digest concurrency/lost-response oracle와 Phase 10 consumer receipt 후 재시작한다.

### F-P09-005 — Run-state fence와 publication pointer CAS token이 혼재했다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact evidence:** `ResultPublisher.compareAndSet(SolveId, StateVersion expectedState,
  PublishableResultRef)`의 parameter 이름과 [Phase 08 §7.3](../phases/phase-08-application-ports-local-runtime.md#73-artifactstorage-contracts-produced-for-phase-09)은
  run-state guard re-read 뒤 published pointer CAS를 별도 단계로 둔다. 그러나 target §8.6
  v1.0은 “current publication version이 `expectedVersion`과 일치”한다고 서술해 같은 token을
  publication pointer version처럼 사용했다. Run-state object와 published pointer는 서로 다른
  authoritative key/linearization point다.
- **Correction required:** Phase 08/09/10 owner가 `expectedState`를 run-state authorization
  fence로 고정할지, publication precondition을 별도 typed value로 public contract에 노출할지
  결정해야 한다. Published pointer create-if-absent/CAS, `AlreadyPublished`, stale fence와
  response-loss reconciliation의 exact linearization을 승인한다.
- **Applied locally:** Target §3.1~§3.2, §4, §8.5~§8.6, §9.4, §10.4~§10.5,
  WP-09.0/09.4, structural/exit/blocker/handoff/traceability에서 distinct identity와 provisional
  Phase 08 sequence 해석을 명시했다.
- **Residual risk / last safe / restart:** Last safe point는 immutable publishable closure와
  published pointer 없음이다. Distinct typed preconditions, stale fence, different desired,
  response-loss와 corrupt pointer tests가 cross-phase 승인될 때까지 publication 구현을 금지한다.

### F-P09-006 — `OpaqueLocator`를 포함한 envelope의 protected identity projection이 모호했다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** Target §7.3 v1.0은 provider locator를 semantic identity에서 제외한다고
  했지만 §8.2 `ArtifactRef`에는 `OpaqueLocator`가 포함되고 §8.3 `ArtifactEnvelope`은 full
  `ArtifactRef`와 `protectedMetadataDigest`를 함께 가졌다. Full-record canonical serialization을
  hash하면 physical copy가 semantic identity를 바꾸고, §10.3 locator-copy parity oracle과
  충돌한다.
- **Correction:** Canonical `ArtifactIdentityProjection`의 included field를 열거하고
  `opaqueLocator`, provider version/generation, created/observation metadata를 projection에서
  제외한다. Full `ArtifactRef` serialization을 protected digest로 쓰지 않는다.
- **Applied:** Target §7.3, §8.3, §10.2, exit/traceability에 locator-free projection과
  `opaqueLocatorIsExcludedFromProtectedMetadataProjection` test를 추가했다.
- **Residual:** Exact production encoding/hash algorithm은 ADR까지 OPEN이다. Approved vector가
  생기면 relocation 전/후 protected digest와 authority refs를 독립 oracle로 재검증해야 한다.

### F-P09-007 — Exact immutable profile catalog의 구현·contract suite 책임이 빠졌다

- **Severity/status:** `MEDIUM — APPLIED`
- **Exact evidence:** [Integrated §12.3/§13.4](../../architecture-domain-implementation-design.md#123-provider-neutral-outbound-ports)는
  `ObjectProfileCatalog implements ProfileCatalogPort`를 common semantic implementation으로
  둔다. [Phase 08 §7.4](../phases/phase-08-application-ports-local-runtime.md#74-profile-dispatch-workflow-and-cancellation-ports)는
  tenant/profile/version/preset exact lookup signature를 정의한다. Target v1.0 tree에는
  `ProfileCatalogPort` 이름만 있었고 `ObjectProfileCatalog`, work package, contract test와
  evidence ownership이 없었다.
- **Correction:** Object-common에 exact immutable `ObjectProfileCatalog` 책임을 두고
  latest/default/list/event/hidden registry/index fallback을 금지한다. Same identity/different
  protected bytes, missing exact version과 cross-tenant oracle를 reusable suite에 넣는다.
- **Applied:** Target §6, §8.5, §10.9/§10.11, WP-09.3, command/structural/exit/handoff/
  traceability에 구현 target, `ProfileCatalogContract`와 evidence receipt를 추가했다.
- **Residual:** Phase 08 port와 profile descriptor schema가 unaccepted다. Exact fixture는
  accepted Phase 04/08 identity가 나온 뒤 고정해야 하며 current text는 implementation
  evidence가 아니다.

### F-P09-008 — 인접 section digest가 acceptance 구조에 남아 reciprocal drift를 유발했다

- **Severity/status:** `LOW — APPLIED`
- **Exact evidence:** Target metadata v1.0은 Phase 08/10 named-section ordered projection
  SHA-256 두 개를 기록하고 §1.1/WP-09.0/§14/§15.2가 이를 handoff acceptance에 사용했다.
  인접 문서 수정마다 상호 digest를 다시 계산하면 내용 의미가 아닌 review 순서가 acceptance를
  바꾸고 reciprocal cycle을 만든다.
- **Correction:** Canonical source whole-file fingerprint는 provenance/drift 확인에 유지하되,
  neighbor document/section digest를 acceptance로 사용하지 않는다. Stable named source
  section과 accepted immutable artifact/evidence identity로 handoff를 trace한다.
- **Applied:** Target metadata/§1.1/WP-09.0/§14/§15.2에서 adjacent section fingerprints와
  projection acceptance를 제거하고 review status/link를 actual로 갱신했다.
- **Residual:** Canonical source가 바뀌면 그 fingerprint와 직접 소비 section을 같이
  re-review해야 한다. Stable section citation도 의미 변경을 자동 탐지하지 않으므로 accepted
  artifact contract identity가 궁극 handoff authority다.

## 5. Target에 반영한 변경 요약

| 변경 | 적용 section | 성격 |
|---|---|---|
| Document v1.3, canonical H1/Database 부제, 현존 GFM anchor와 live 15/15 review inventory 정렬 | H1, metadata, §1.1, §4~§5, §16, review source links/inventory | 최종 감사 정정; implementation/evidence/acceptance와 blocker 의미 불변 |
| Independent review `COMPLETE_CHANGES_REQUIRED`, review link | Metadata, §1 | 상태 사실성; implementation/evidence는 그대로 NOT_STARTED/NOT_PRODUCED |
| Neighbor digest acceptance 제거 | Metadata, §1.1, WP-09.0, §14~§15 | Reciprocal hash cycle 제거 |
| Phase 09/11 S3 boundary blocker | §1.1, §2.3, §3~§4, §10~§16 | External authority 결정 전 scope/exit 차단 |
| Non-ambient authorization requirement | §3/§4/§6~§8/§10~§16 | Local guard/test 추가, Phase 08 변경 없음 |
| Exhaustive failure carrier gate | §3~§4/§8.4/§10~§16 | No-collapse oracle 추가, public variant 미확정 |
| Worker commit authority gap | §3~§4/§8.5/§10~§16 | Cross-phase blocker/test 추가, signature 미조작 |
| Distinct run-state/publication preconditions | §3~§4/§8.5~§9.4/§10~§16 | Phase 08 sequence와 정렬, exact API는 blocker |
| Locator-free identity projection | §7.3/§8.3/§10.2/§13/§16 | 안전·명백한 semantic correction |
| `ObjectProfileCatalog` + exact suite | §6/§8.5/§10.9/§11~§16 | 누락 책임/false-green 보완 |

Java/POM/README/progress/인접 Phase·review/source/deployment는 수정하지 않았다.

## 6. Blocker, last safe point와 restart

| Blocker | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|
| Phase 00/07/08 unaccepted + scheduler task 없음 | Architecture, Verification, Application, scheduler | 모든 Phase 09 implementation/evidence/acceptance | P09 v1.3 pure document, pointer 없음 | Accepted `E-P00-*`/`E-P07-*`/`E-P08-*`, exact task/role separation |
| Phase 09/11 S3 boundary 충돌 | Architecture + P08/P09/P11 + scheduler | Provider module/suite/evidence와 exit | Provider-neutral + memory/local planned contract | Aligned decision/source, owning module, executable same-suite command/evidence |
| Authorization binding 부재 | P08 Application + P09 Security/Data Integrity | 모든 storage operation | Backend lookup/call 0 | Non-ambient binding signature/lifecycle + missing/mismatch/no-leak tests |
| Failure carrier 부재 | P08 Application + P09 Data Integrity | Port implementation/fault evidence | Failure 시 state/pointer 불변 | Exhaustive operation × failure mapping + compile/no-collapse tests |
| Worker committed pointer operation 부재 | P08/P09 Storage + P10 Coordinator | Worker commit, exact fan-in | Verified orphan outcome, committed authority 없음 | Approved typed create/CAS primitive + concurrency/response-loss consumer receipt |
| Publication precondition identity 모호 | P08/P09/P10 | Published pointer linearizability | Publishable immutable closure, pointer 없음 | Distinct run-state/publication preconditions + stale/conflict/lost-response tests |
| Encoding/key/checksum/security/retention policy OPEN | Architecture, Integrity, Security, Records, Operations | External bytes/production/storage delete | Algorithm-tagged/proposed contract, preserve/no-delete | Approved versioned ADR/policy and migration/rotation/hold evidence |
| Local filesystem capability ENVIRONMENT-GATED | Local Runtime/Platform | Local adapter acceptance | In-memory reference only | Target FS capability/recovery evidence; unsupported fail-closed |
| `Q-BENCH-02`, decimal `D/U`, `C-17`, `Q-VAR-01` | 등록된 Product/Benchmark/Input/Algorithm owners | Official run/cutover 또는 later feature | TEST_ONLY fixture, generic artifact kind | Question register의 exact restart evidence/approval |

Rollback은 blind overwrite/delete가 아니다. Last accepted immutable artifact graph와 current
pointer를 보존하고, 실패한 새 bytes는 orphan/quarantine/hold로 남긴다. 실제 rollback은
승인된 이전 immutable ref로 single conditional pointer update를 수행하고 audit evidence를
남기는 별도 authorized operation이어야 한다.

## 7. 실제 verification command와 결과

### 7.1 Review 중 실행한 build/inventory

| Command | Exit/result | 해석 |
|---|---|---|
| `git rev-parse --abbrev-ref HEAD` / `git rev-parse HEAD` | `0`; `codex/domain-design`; `3424277c9c74f8151a83be056a07dd4659331beb` | Review baseline |
| `java -version` | `0`; Corretto OpenJDK `25.0.3` | POM Java range와 일치 |
| `mvn -version` | `0`; Apache Maven `3.9.14`, Java `25.0.3`, macOS aarch64 | POM Maven range와 일치 |
| `mvn verify` | `0`; `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`; 3.897 s | Root placeholder regression만 green; P09 evidence 아님 |
| `mvn -pl rpdptw/application -am verify` | `1`; selected project가 reactor에 없다는 Maven error | Target module/command가 현재 실행 불가함을 확인; skip/zero-test로 우회하지 않음 |
| `rg --files src/main/java src/test/java gcp` | `0`; main Java 6, test Java 1, deployment files 3 | Inventory count 확인 |
| POM/source/deployment DB token scan | `1` (no match) for JDBC/JPA/SQL database implementations | Actual hidden DB dependency 미관찰; raw GCS authority 문제는 별도 |
| GCS/list/default scan | `0`; direct Storage imports/create/list/result existence, `8/5000/System.nanoTime` 발견 | Target anti-pattern과 current placeholder drift 확인 |
| Canonical `shasum -a 256` set | `0`; target metadata의 canonical source fingerprint 9개와 exact 일치 | Source provenance 확인; neighbor acceptance digest로 사용하지 않음 |

`mvn verify` 완료 시각은 `2026-07-28T11:03:45+09:00`이고 reported total time은
3.897초였다. 이 값, root Surefire test 1건과 ignored `target/`를 `E-P09-*`로 복사하지 않는다.
Planned module/test가 생기기 전 `TBD_BY_APPROVED_PHASE_09_11_BOUNDARY`, pseudo test 이름과
future command는 evidence가 아니다.

### 7.2 Post-write document validation

아래 표는 두 허용 파일을 쓴 뒤 같은 checkout에서 실행한 결과만 기록한다.

| Validation | Result |
|---|---|
| 두 파일 non-empty | PASS — `test -s` 두 건 exit 0 |
| Live review inventory/status | PASS — Phase 00~14 review file 15개 모두 terminal `COMPLETE`/`FINAL`; Phase 07/08/10 verdict와 target `NOT_STARTED`/`NOT_PRODUCED`/`NOT_READY` exact 대조 |
| Metadata/scope/inventory/verdict/findings/change/blocker/command 필수 구조 | PASS — required heading 전부 존재, finding heading 8개와 metadata count 일치 |
| Relative link target와 local heading anchor | PASS — Node local Markdown target/GFM-style heading validator: `OK links+anchors files=2` |
| Markdown fence parity와 trailing whitespace | PASS — target fence 76, review fence 2로 모두 even; trailing whitespace 0 |
| Hidden DB/default/gate bypass/reciprocal neighbor hash scan | PASS — actual DB/state-store token 0, default match는 current characterization/TEST_ONLY/OPEN/금지문뿐, acceptance status bypass 0, neighbor hash 0 |
| 허용 경로 write audit | PASS WITH BASELINE LIMITATION — edit operation은 target/review 두 경로뿐; `git diff --name-only` tracked change 0, 두 scoped path는 `??`; review 전부터 전체 implementation tree가 untracked여서 Git index 단독으로 pre-existing file content delta를 분리할 수 없음 |
| `git diff --check`와 untracked-file 보완 검사 | PASS — scoped `git diff --check` exit 0; 두 파일 각각 `git diff --no-index --check /dev/null <file>`은 new-file difference 때문에 exit 1이지만 whitespace diagnostic 0 |

## 8. 최종 verdict와 handoff 제한

Document verdict는 `CHANGES_REQUIRED`, Phase acceptance는
`BLOCKED_NOT_IMPLEMENTED`, Phase 10 handoff는 `NOT_READY`다. 안전 수정으로 문서의
failure-awareness와 traceability는 개선됐지만 residual 5건을 구현자가 임의 Java type,
ambient context, generic exception, backend key 또는 hidden DB/index/lock으로 메우면 안 된다.

Phase 10은 accepted handoff 전 다음을 소비했다고 주장할 수 없다.

- Exact declared worker committed-outcome authority primitive
- Lossless missing/denied/corrupt/stale/indeterminate failure surface
- Distinct run-state/worker/publication precondition identities
- Tenant-scoped non-ambient access binding
- Exact immutable profile catalog
- Approved Phase 09/11 provider-boundary evidence
- `E-P09-STORAGE-CONTRACT`, `E-P09-CAS`, `E-P09-TENANT`

Restart 시 reviewer는 stable source section과 accepted artifact/evidence identity를 확인하고,
실제 reactor module에서 discovered test count/skip/failure를 fail-closed 대조해야 한다.
Object/list/event 존재, root placeholder build success, 계획 문서나 reciprocal digest는
implementation, publication 또는 Phase acceptance evidence가 아니다.

## ALNS-first direction revision addendum

Task `019fa901-8776-7f61-b467-a8c6595b970d`에서 Phase 09 storage branch는 Phase 14A
ALNS benchmark의 선행조건이 아니며, AWS ALNS-only 14B 경로의 별도 branch임을
재확인했다. C-17/Phase 13은 Phase 14A benchmark acceptance 뒤에만 열리고 기존
review verdict, implementation, acceptance와 evidence 상태는 변하지 않는다.
