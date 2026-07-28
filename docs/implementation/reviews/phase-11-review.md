# Phase 11 독립 리뷰 — AWS reference distribution

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "11"
review_date: 2026-07-28
reviewer_role: independent Phase 11 documentation reviewer
target_document: docs/implementation/phases/phase-11-aws-reference-distribution.md
target_document_version_after_safe_fixes: 1.3
target_whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
document_verdict: CHANGES_REQUIRED
phase_acceptance_verdict: BLOCKED_NOT_IMPLEMENTED
implementation_status_observed: NOT_STARTED
deployment_status_observed: NOT_DEPLOYED
production_authority_observed: NOT_GRANTED
evidence_status_observed: NOT_PRODUCED
phase_acceptance_status_observed: BLOCKED_NOT_IMPLEMENTED
live_phase_documents_observed: 15_OF_15_PRESENT
live_phase_reviews_observed: 15_OF_15_COMPLETE
scheduler_status_change: NOT_AUTHORIZED
scheduler_task_id_observed: TBD_NOT_SUPPLIED
finding_counts:
  critical: 2
  high: 4
  medium: 1
  low: 1
  total: 8
finding_disposition:
  applied_safe_obvious: 6
  residual_external_or_cross_phase: 4
fake_evidence_detected: false
code_change_reviewed: false
code_change_made: false
```

## 1. 결론

[Phase 11 v1.3](../phases/phase-11-aws-reference-distribution.md)은 AWS가 선택된
target/reference라는 결정과 실제 구현, non-production integration, 배포,
production authority를 분리한다. Phase 09 exact-key/immutable/CAS/no-database,
Phase 10 action/completeness/retry/cancel, Phase 12 conformance handoff를 보존하며,
IAM/KMS/tenant, corruption, observability, cost와 rollback도 폭넓게 계획한다.

리뷰에서 source로 답이 명백한 6건은 target에 적용했다.

1. Opaque run-state token을 action/event/publication precondition으로 재사용하지
   않고 exact current pending action과 distinct publication token을 요구했다.
2. Cancellation request와 terminal fence를 분리하고 `CANCEL_REQUESTED` 대
   `PUBLISHING`을 같은 run-state version에서 경쟁시켰다.
3. Step Functions workflow type과 Lambda integration mode를 hidden provider
   default가 아닌 explicit reviewed config/gate로 만들었다.
4. S3 artifact/state/publication write에 conditional header가 없으면 bucket policy와
   actual negative test가 거부하도록 보강했다.
5. Maven selected/full/profile test와 SAM empty-change-set evidence를
   fail-closed로 만들었다.
6. Phase 문서/review `15/15` live inventory, actual neighbor 상태,
   canonical-only fingerprint와 stable neighbor section citation, review status와
   one-way evidence dependency 정책을 반영했다.

Document verdict는 `CHANGES_REQUIRED`다. 최신 Phase 10 v1.3 review가
publication-pointer precondition, same-run-state cancellation fence와
restart-safe monotonic deadline을 Phase 08/09/10/11 공동 blocker로 남겼고,
사용자 고정 canonical set 안의 Final Domain/Architecture에는 여전히 과거
`Q-INFRA-01 DEFERRED`, `25/1/2` 표기가 남아 있기 때문이다. Phase 10의
Maven zero-test false-green은 같은 scheduler batch의 초기 v1.1 correction에서 이미
해소됐으므로 Phase 11 residual finding으로 유지하지 않았다.

Phase acceptance는 별도로 `BLOCKED_NOT_IMPLEMENTED`다. Target Maven reactor,
AWS adapter/distribution/source IaC/test, approved AWS environment, 실제 S3/
Step Functions/Lambda/IAM/KMS run, `E-P11-*`와 rollback evidence가 없다.
현재 root placeholder의 `mvn clean verify` 성공이나 ignored `.serverless`
생성물은 Phase 11 evidence가 아니다.

## 2. 범위, 권위 source와 baseline

### 2.1 완전히 읽고 대조한 권위 source

| Source | 직접 대조한 범위 | Review 적용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | 전체, 특히 §1~§4, §13~§17 | AWS target/reference, C-20 provider boundary, declared completeness, both-gate publication, RM-8와 production 분리 |
| [Final Domain](../../2026-07-26-domain-design.md) | 전체, 특히 §7~§10, §15~§18 | Immutable authority, verified result, failure/evidence와 잔여 Q-INFRA drift |
| [Final Architecture](../../2026-07-26-architecture-design.md) | 전체, 특히 §2~§3, §5~§6 | Java 25/Maven DAG, port/state/identity/security/test와 잔여 provider-status drift |
| [Integrated design](../../architecture-domain-implementation-design.md) | 전체, 특히 §12~§16, §19~§28 | Phase 09~12, S3/Step Functions/Lambda, no-DB, migration/failure/test/invariants |
| [Question register](../../master-design-open-questions.md) | 전체, exact 28개 질문 | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`; Q-INFRA 선택은 배포/production 아님 |
| [Master Realization Plan](../master-realization-plan.md) | 전체, 특히 §2~§6, Phase 09~12/14, §8~§15 | Source conflict resolution, Phase 11 entry/exit/evidence/DoD |
| [Implementation map](../README.md) | 전체 | User-locked authority, canonical filename, planned/actual와 review 권한 |
| [Execution progress](../execution-progress-and-results.md) | 전체 | Scheduler-only status와 implementation claim `NONE` |

Authority는 사용자 선언으로 고정했다. Superseded
[2026-07-26 Master](../../2026-07-26-master-design.md)와 `docs/codex/*`는
historical cross-check로만 확인했고 수정하거나 현재 AWS authority로 사용하지
않았다. `Q-BENCH-02`, `C-17`, `Q-VAR-01`, provider 수치와 production
authority를 임의로 닫지 않았다.

### 2.2 인접 Phase와 current review

| 문서 | 읽은 범위 | 관찰 |
|---|---|---|
| [Phase 09](../phases/phase-09-object-storage-no-database.md) + [review](phase-09-review.md) | 최신 v1.3 contract/blocker section | Review `COMPLETE`/`CHANGES_REQUIRED`; exact read/immutable/CAS/no-DB와 distinct publication precondition blocker |
| [Phase 10](../phases/phase-10-provider-neutral-coordinator.md) + [review](phase-10-review.md) | 최신 v1.3 §6.3~§7.4, §8.6, §13~§14와 blocker section | Review `COMPLETE`/`CHANGES_REQUIRED`; Maven false-green은 해소, pending-action/publication/cancel/deadline blocker는 유지 |
| [Phase 12](../phases/phase-12-provider-substitution.md) + [review](phase-12-review.md) | 최신 v1.3 §1~§4, §15.1, §18.2와 blocker section | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS`/`COMPLETE_CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_READY`; Phase 11 accepted evidence/receipt 없이는 entry 차단 |
| [Prior review format](phase-07-review.md) | 전체 | Severity/finding/evidence 형식과 fail-closed Maven·canonical fingerprint policy를 대조 |

Neighbor는 모두 read-only로 유지했다. Whole-file neighbor digest나 reciprocal
fingerprint를 acceptance로 쓰지 않고 canonical 4문서 fingerprint와 안정된
section 인용만 target에 남겼다. Phase 09/10 동시 review는 scheduler 의도 batch로
한 번만 관찰·판정했고 이후 whole-file hash/status를 반복 추적하지 않았다.
당시 관찰값은 target metadata의 `authoring_time_neighbor_snapshot`에 historical
snapshot으로 보존했다. 현재 live inventory는 Phase 문서 `15/15 PRESENT`, review
`15/15 COMPLETE`이며 이 completion은 implementation/evidence/acceptance가 아니다.

### 2.3 Actual repository/build/source/deployment inventory

| 항목 | Actual 관찰 | Phase 11 판정 |
|---|---|---|
| Phase document inventory | Phase 00~14 `15/15 PRESENT` | 문서 존재는 구현 또는 acceptance evidence가 아님 |
| Phase review inventory | Phase 00~14 `15/15 COMPLETE` | Phase 11 verdict는 `CHANGES_REQUIRED`; acceptance는 `BLOCKED_NOT_IMPLEMENTED` |
| Git baseline | Branch `codex/domain-design`, commit `3424277c9c74`; review 전부터 `docs/implementation/` 전체 untracked | Shared 사용자 작업 보존; 허용된 target/review만 write |
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Root POM enforcer/release 25와 일치; AWS evidence는 아님 |
| Maven | Root 단일 `com.ronext:ro-next` jar; Google Workflow/Storage, Jackson, JUnit 한 classpath | Target reactor/AWS adapter 격리 없음 |
| Main/test | Main Java 6개, JUnit test 1개 | Target `com.ronext.rpdptw`, S3/Step Functions/Lambda adapter/test 없음 |
| Current algorithm | `AlnsBatchEngine`이 seeded `double` 합성 objective map 생성 | Solver/parity oracle 아님 |
| Current finalization | GCS prefix listing 뒤 보이는 raw objective 최솟값 선택 | Declared completeness, CAS, verifier, publication gate와 불일치 |
| Tracked deployment | `gcp/README.md`, Cloud Build, Google Workflow | Legacy characterization only; target authority/production evidence 아님 |
| Tracked AWS source | SAM/CloudFormation/ASL/serverless source와 AWS dependency 0 | Phase 11 implementation/deployment 0 |
| Ignored `.serverless` | Generated CFN에 S3 1, Lambda 7, Step Functions 1, IAM 2와 DynamoDB 1 포함 | Source/reproducible deploy evidence 아님; DynamoDB는 no-DB 위반 |
| Root verification | `mvn clean verify` exit 0, main 6/test 1 compile, tests 1/0/0/0 | Placeholder build characterization만; `E-P11-*` 아님 |
| AWS environment/evidence | Account/region/role/budget/cleanup approval와 actual run 없음 | `NOT_DEPLOYED`, `NOT_PRODUCED`, production authority 없음 |

Root build는 shade 단계의 overlapping resource/class warning을 남겼다. 이 warning은
Phase 00/build owner가 다룰 inventory이며, AWS distribution의 reproducible package
또는 parity 성공을 증명하지 않는다.

## 3. Severity 기준

| Severity | 의미 |
|---|---|
| `CRITICAL` | 즉시 잘못된 production/publication authority 또는 회복 곤란한 corruption을 허용 |
| `HIGH` | False-green, CAS/identity/delivery/Phase handoff gap으로 구현·acceptance를 막음 |
| `MEDIUM` | Source governance 또는 independent evidence 신뢰성을 유의미하게 약화 |
| `LOW` | Status/link/inventory/fingerprint hygiene drift로 사실성·재현성을 저하 |

## 4. Findings

### F-P11-001 — Workflow type과 Lambda invocation semantics가 service default에 숨어 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact source/evidence:** 수정 전 target §3은 DLQ 선택을 “invocation mode에
  따라” 정한다고만 했고 §6/§9/§12에 workflow type/invocation mode를 요구하는
  config field와 exact test가 없었다. AWS 공식 문서상 Step Functions는 생성 시
  Standard/Express type을 선택하고 type은 이후 변경할 수 없으며 execution
  guarantee, duration과 callback 지원이 다르다. Lambda/Step Functions integration도
  synchronous, asynchronous, callback에 따라 retry/heartbeat/delivery가 달라진다.
  [AWS workflow type](https://docs.aws.amazon.com/step-functions/latest/dg/choosing-workflow-type.html),
  [Step Functions Lambda integration](https://docs.aws.amazon.com/step-functions/latest/dg/connect-lambda.html),
  [Lambda retry behavior](https://docs.aws.amazon.com/lambda/latest/dg/invocation-retries.html).
- **Correction:** 특정 mode를 reviewer가 선택하지 않는다. `AwsWorkflowRuntimeConfig`가
  workflow type, coordinator/worker integration mode와 retry/deadline/reconciliation
  policy를 필수로 받고, reviewed ADR/IaC/actual integration이 compatible semantics를
  입증하지 못하면 fail-closed한다.
- **Applied:** Target [§3](../phases/phase-11-aws-reference-distribution.md#3-결정-상태와-수치-gate),
  [§6.2~§6.3](../phases/phase-11-aws-reference-distribution.md#62-workflow와-compute-mapping),
  WP11-1/4, §12 tests, §13~§15 evidence/gate, §17 blocker와 §18 traceability에 반영했다.
- **Residual risk:** Exact type/mode는 계속 `OPEN/ADR_REQUIRED`다. 실제 environment와
  workload evidence 없이 Standard/Express 또는 sync/async/callback을 production
  default로 승격할 수 없다.

### F-P11-002 — Adapter conditional write를 bucket policy가 강제하지 않았다

- **Severity/status:** `HIGH — APPLIED`
- **Exact source/evidence:** 수정 전 target §6.1은 S3 adapter가 conditional create/CAS를
  쓰도록 했지만 §8.2 bucket policy는 transport/encryption만 거부했다. Buggy/misconfigured
  code path가 unconditional `PutObject`를 실행하면 immutable artifact나 state/publication
  fence를 우회할 방어가 없었다. S3는 `If-None-Match`/`If-Match` conditional write와
  이를 강제하는 bucket-policy condition key를 제공한다.
  [S3 conditional writes](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes.html),
  [conditional-write enforcement](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes-enforce.html).
- **Correction:** Immutable artifact prefix는 conditional create를, state/publication
  prefix는 operation별 create/update precondition을 요구한다. Header 없는 write는
  provider-native deny여야 하고 original object가 보존되어야 한다. `CopyObject`와
  multipart의 정책 제약은 별도 ADR/integration 없이 migration shortcut으로 쓰지 않는다.
- **Applied:** Target [§6.1](../phases/phase-11-aws-reference-distribution.md#61-phase-09-storage-mapping),
  §8.2, §12.2~§12.4, §13~§16, evidence manifest와 traceability에 반영했다.
- **Residual risk:** Actual S3 concurrency/policy/KMS evidence는 `NOT_PRODUCED`다.
  S3의 strong read-after-write와 single-key atomicity도 실제 adapter suite로
  확인해야 하며 list를 completeness authority로 승격하지 않는다.

### F-P11-003 — Future test/deploy command가 0-test·stale report·empty change set을 통과시킬 수 있었다

- **Severity/status:** `HIGH — APPLIED`
- **Exact source/evidence:** 수정 전 target §12.6은 system `mvn`, no `clean`,
  selected-test no-zero guard, report-count reconciliation 없이 `verify` exit만
  요구했다. `sam deploy --no-fail-on-empty-changeset` exit 0도 새 deploy처럼
  오해할 수 있었다. Target module 자체가 없으므로 현재 이 명령들은 실행 evidence가
  아니라 future contract다.
- **Correction:** Accepted Phase 00의 pinned future `./mvnw`로 root `clean install`,
  selected module no-`-am` + `failIfNoSpecifiedTests=true clean test`, filter 없는
  slice/profile `clean verify`를 요구한다. 각 run 직후 fresh Surefire/Failsafe XML과
  expected/discovered/passed class/method를 봉인한다. Empty change set은 `NO_CHANGE`이며
  resolved deployed revision/digest reconciliation일 뿐 새 deploy/production evidence가 아니다.
- **Applied:** Target [§12.6](../phases/phase-11-aws-reference-distribution.md#126-commands와-pass-criteria),
  §14 evidence와 §15 DoD에 반영했다.
- **Residual risk:** `./mvnw`, target reactor/module/test/report가 아직 없으므로
  모든 강화 command는 future red다. 현재 root의 1-test success로 대체할 수 없다.

### F-P11-004 — Cancel request와 publication fence가 서로 다른 key의 CAS winner로 표현됐다

- **Severity/status:** `CRITICAL — APPLIED LOCALLY / RESIDUAL CROSS-PHASE BLOCKER`
- **Exact source/evidence:** 수정 전 target §6.3은 “cancel intent가 먼저 CAS”되면
  publish를 막는다고 썼다. 최신 [Phase 10 review F-P10-002](phase-10-review.md#f-p10-002--서로-다른-key의-cancellation-intent-cas와-publication-cas를-하나의-race-winner로-간주했다)는
  cancellation-intent object와 run-state `PUBLISHING`이 서로 다른 key라 두 CAS가
  모두 성공할 수 있음을 확인했다. Intent write의 선후만으로 terminal meaning을
  하나로 고를 수 없다.
- **Correction:** Intent object는 durable request/hint다. 같은 run-state expected
  version에서 `CANCEL_REQUESTED`와 `PUBLISHING` CAS가 경쟁하고 성공한 하나만
  terminal intent를 권위화한다. Step Functions abort/cancel signal도 이 fence를
  우회하지 않는다.
- **Applied:** Target [§6.3](../phases/phase-11-aws-reference-distribution.md#63-stateasl-mapping),
  WP11-4, actual cancellation/publication race tests, exit/DoD/anti-pattern,
  §17 blocker와 Phase 10 handoff에 반영했다.
- **Residual/last safe/restart:** Phase 08/10/11 accepted signature와 actual AWS
  fault evidence는 없다. Last safe point는 durable cancel request만 기록하고 새
  side effect/publication을 승인하지 않는 pure mapping이다. Same-version race,
  too-late와 crash test 공동 승인 뒤 재개한다.

### F-P11-005 — Final Domain/Architecture의 Q-INFRA 상태가 최신 canonical authority와 충돌한다

- **Severity/status:** `MEDIUM — RESIDUAL SOURCE GOVERNANCE`
- **Exact source/evidence:** Final Domain
  [§18.2~§18.3](../../2026-07-26-domain-design.md#182-deferred-boundary)는
  `Q-INFRA-01`, `Q-VAR-01`을 둘 다 deferred로 두고 `25/1/2`를 기록한다.
  Final Architecture [§6.3~§6.5](../../2026-07-26-architecture-design.md#63-phase-dependency와-deferred-decision)도
  provider 미결정과 `Q-INFRA-01 DEFERRED`를 남긴다. 반면 Canonical Master §1/§4/§16,
  질문 등록부 `Q-INFRA-01`, Master Realization Plan §2.2는 2026-07-26 사용자 승인
  AWS reference와 `26/1/1`을 명시한다.
- **Correction required:** Domain/Architecture owner가 stale status/count와
  provider-pending 문장을 최신 결정에 맞추되 provider-neutral boundary와
  implementation/production gate는 보존해야 한다.
- **Applied locally:** Target §1과 이 review는 사용자 고정 conflict order에 따라
  AWS reference 선택을 적용하고 implementation/deploy/production을 계속 차단한다.
  허용 범위 밖 canonical 파일은 수정하지 않았다.
- **Residual/last safe/restart:** Last safe authority는 Canonical Master +
  question register + realization-plan §2.2다. Source 정합화 review 전까지
  Domain/Architecture의 해당 status table을 단독 authority로 사용하지 않는다.

### F-P11-006 — Phase 12 상태, review metadata와 evidence dependency 정책이 stale했다

- **Severity/status:** `LOW — APPLIED`
- **Exact source/evidence:** 수정 전 metadata/§1.1은 Phase 12를
  `PLANNED_NOT_PRESENT_AT_BASELINE`/“actual file 없음”으로 기록했고 Phase 08~10
  section digest를 compatibility acceptance 입력처럼 유지했다. 이후 작성 중
  snapshot에는 Phase 08/12의 `READY_FOR_REVIEW`와 Phase 09/10 v1.1 상태가 남았다.
  현재는 Phase 문서 `15/15 PRESENT`, review `15/15 COMPLETE`이고
  [Actual Phase 12](../phases/phase-12-provider-substitution.md)는 v1.3
  `INDEPENDENT_REVIEWED_WITH_CORRECTIONS`/`COMPLETE_CHANGES_REQUIRED`/
  `NOT_STARTED`/`NOT_READY`다. 이전 최종 감사 전 target은 완료된 Phase 11 review와
  달리 문서 상태를 `READY_FOR_REVIEW`로 유지했고, pre-review
  `Phase11EvidenceManifest`가 `independentReviewRef`를 역참조했으며 Phase 11
  evidence와 Phase 12-owned `ProviderConformanceManifest`의 dependency 방향도
  분리하지 않았다.
- **Correction:** Phase 12 actual 상태와 §1~§4/§15.1/§18.2를 stable citation으로
  기록했다. Metadata fingerprint는 canonical 4문서만 두고 neighbor whole-file/
  section/reciprocal hash는 acceptance에서 제거했다. Target document status를
  review verdict와 정렬하고, pre-review evidence → post-review acceptance receipt
  → Phase 12-owned conformance manifest의 단방향 dependency를 고정했다. 작성 당시
  neighbor 값은 historical snapshot으로 보존하고 별도 live `15/15` inventory와
  Phase 11 미완료 상태를 기록했다.
- **Applied:** Target metadata와 §1, WP11-8, §14~§15, §18.1~§18.4, reviewer
  checklist 및 이 review의 Phase 10 GFM anchors에 반영했다.
- **Residual:** Phase 08~12 문서 존재는 acceptance가 아니다. 모두 actual-but-unaccepted
  상태이며 implementation entry에서 section 의미를 직접 다시 확인해야 한다.
  현재 review는 `CHANGES_REQUIRED`, evidence는 `NOT_PRODUCED`이므로 post-review
  acceptance receipt도 존재하지 않고 phase acceptance는
  `BLOCKED_NOT_IMPLEMENTED`다.

### F-P11-007 — Run-state token과 publication-pointer precondition이 분리되지 않았다

- **Severity/status:** `CRITICAL — APPLIED LOCALLY / RESIDUAL CROSS-PHASE BLOCKER`
- **Exact source/evidence:** 수정 전 target은 Phase 10 action을 실행하고 S3
  publication pointer를 conditional commit한다고 했지만, action/event에서 opaque
  run-state token을 배제하고 pointer의 distinct precondition을 exact read/reconcile하는
  계약이 없었다. 최신 [Phase 10 review F-P10-001](phase-10-review.md#f-p10-001--pending-action이-pre-cas-opaque-version을-semantic-authorization과-publication에-재사용했다)과
  [Phase 09 review](phase-09-review.md)는 run-state/action/publication token을
  대체하면 stale action 또는 잘못된 pointer CAS가 생긴다고 blocker로 남겼다.
- **Correction:** `CoordinatorAction`은 opaque repository version을 운반하지 않는다.
  Executor가 exact current pending `ActionId`, payload fingerprint와 authorizing
  transition ordinal을 검사한다. Publication은 Phase 09가 승인할 distinct
  `PublicationPrecondition`과 exact read/reconcile을 사용한다.
- **Applied:** Target [§6.1~§6.3](../phases/phase-11-aws-reference-distribution.md#61-phase-09-storage-mapping),
  WP11-2/4, action/token/publication AwsIT, evidence/gates/DoD/anti-pattern,
  §17과 §18.1에 반영했다.
- **Residual/last safe/restart:** Exact public port/read/CAS type은 아직 cross-phase
  proposed다. Last safe point는 pure action mapping과 immutable
  `PublishableResultRef`의 publication 불가 상태다. Phase 08/09/10/11 accepted
  signature와 local/storage/AWS conformance 뒤 재개한다.

### F-P11-008 — Crash-resume 뒤 monotonic deadline origin 복원 계약이 없었다

- **Severity/status:** `HIGH — RESIDUAL CROSS-PHASE BLOCKER`
- **Exact source/evidence:** Target은 Lambda remaining time, Step Functions timeout,
  application watchdog을 분리했지만 새 Lambda/JVM에서 process-local monotonic
  origin을 어떻게 복원할지는 정하지 않았다. 최신
  [Phase 10 review F-P10-005](phase-10-review.md#f-p10-005--durable-crash-resume를-요구하면서-monotonic-deadline-origin-복원-계약이-없었다)는
  wall clock/provider remaining-time fallback이 watchdog/replay 의미를 바꾼다고
  cross-phase blocker로 판정했다.
- **Correction required:** Phase 08/10/11 owner가 durable deadline representation,
  restart-origin reconciliation, platform reserve, indeterminate clock failure와
  virtual-clock/crash oracle를 승인해야 한다.
- **Applied locally:** Target [§9.2](../phases/phase-11-aws-reference-distribution.md#92-deadline),
  WP11-3, `AwsDeadlineRestartIT`, operations gate/DoD/anti-pattern, §17/§18.1에
  fail-closed last safe point를 기록했다.
- **Residual/last safe/restart:** Last safe point는 explicit `TEST_ONLY` virtual
  clock을 쓰는 pure/local model이고 deadline-enabled AWS path는 off다. 승인 ADR와
  actual crash/restart test가 생긴 뒤 재개한다.

## 5. 집중 검사축 판정

| Review axis | 판정 | 근거/남은 조건 |
|---|---|---|
| AWS 선택 대 implementation/deploy/production authority | `PASS` | 네 상태 축과 `productionAuthority=false`; 실제 AWS/production claim 0 |
| Phase 09 storage/no-DB handoff | `PASS AFTER FIX / EVIDENCE MISSING` | Exact read, immutable create, opaque CAS, conditional policy, no listing/DB; actual S3 미실행 |
| Phase 10 coordinator handoff | `BLOCKED` | Current v1.3에 command false-green 해소가 유지됨; publication/cancel/deadline cross-phase contract 미승인 |
| Phase 12 substitution boundary | `PASS AFTER FIX` | Phase 12-owned manifest가 Phase 11 evidence를 forward-reference하는 downstream consumer만; reverse ref/adoption/cutover authority 없음 |
| S3 consistency/CAS/corruption | `PASS DOCUMENT PLAN` | Strong exact-read premise, opaque ETag, independent digest, conditional race/deny tests |
| Step Functions/Lambda retry/idempotency | `PASS AFTER FIX / ADR OPEN` | Type/mode/default 제거, application identity/CAS fence 유지; actual semantics 미검증 |
| IAM least privilege/tenant | `PASS DOCUMENT PLAN` | API/coordinator/worker/workflow/deploy/test 역할, wildcard data action 금지, negative matrix |
| Encryption/integrity | `PASS DOCUMENT PLAN` | TLS/SSE-KMS/KMS policy와 digest 독립; key lifecycle는 Security gate |
| Failure/DLQ/cancel/deadline | `CHANGES_REQUIRED` | DLQ/event hint-only와 same-state cancel fence 보강; durable deadline restart는 blocked |
| Observability/cost | `PASS DOCUMENT PLAN` | Correlation/alarms/redaction/cardinality/cost vector; threshold는 open |
| Legacy GCP/`.serverless`/DynamoDB | `PASS` | Historical inventory only, source/evidence/authority 승격 0 |
| Test/evidence false-green | `PASS AFTER FIX` | Phase 11 command 보강; current Phase 10 v1.3도 no-`-am`/true/fresh count를 유지 |
| Rollback/reproducibility | `PASS DOCUMENT PLAN` | Immutable artifacts, previous/local last safe, exact state reconciliation |
| OPEN/GATED/deferred/hidden numeric default | `PASS` | Q-BENCH/C-17/Q-VAR와 provider 수치 미확정 유지 |

## 6. Applied fixes와 residual blockers

### 6.1 Applied safe/obvious fixes — 6

1. Exact current pending action authorization, opaque token exclusion과 distinct
   publication precondition mapping.
2. Durable cancel request 대 same-run-state cancel/publication terminal fence.
3. Explicit Step Functions workflow type/Lambda integration mode config, tests,
   evidence와 ADR gate.
4. S3 conditional-write bucket-policy enforcement와 provider-native negative tests.
5. Pinned/fresh/fail-closed Maven evidence 및 empty SAM change-set authority 분리.
6. Actual Phase 12/status/handoff, canonical fingerprint + stable neighbor citation,
   review metadata, GFM anchor와 one-way pre/post-review evidence dependency 정정.

### 6.2 Blocker ledger

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Exact current pending action + distinct publication precondition | Phase 08/09/10/11 Application/Storage/Coordinator | Pure mapping + immutable result ref; publication off | Accepted typed action/read/CAS + local/storage/AWS conformance |
| Same-run-state cancel/publication fence | Phase 08/10/11 Application/Coordinator/AWS | Durable cancel request only; new side effects off | Same-version race/too-late/crash test 공동 승인 |
| Durable monotonic deadline restart | Phase 08/10/11 Application/Coordinator/Ops | `TEST_ONLY` virtual clock; AWS deadline path off | Durable deadline ADR + restart fault tests |
| Phase 00/06~09/10 predecessor unaccepted | 각 Phase owner + scheduler | Proposed adapter/event/IaC contract only | Exact accepted review/evidence refs in Phase11EntryReceipt |
| AWS mode/resource/IAM/KMS/network/retry/retention/cost ADR 미승인 | Platform/Security/Ops/FinOps | Red tests/offline IaC only | Explicit reviewed ADR set; hidden defaults 0 |
| Isolated AWS integration environment authority 없음 | Platform/Security/SRE | Local/emulator only | Scoped account alias/region/role/budget/cleanup owner |
| Actual S3/Step Functions/Lambda/IAM/KMS evidence 없음 | Storage/Coordinator/Compute/Security | Local oracle and un-deployed source | Complete actual provider contract/fault/security/parity evidence |
| Final Domain/Architecture Q-INFRA source drift | Document governance + Domain/Architecture owners | Canonical Master/register conflict rule | Stale status/count sync + cross-document review |
| `Q-BENCH-02`/quota/cost/retention 값 open | Benchmark/Quality/Platform/FinOps/Security | Explicit test-only/non-prod config | Workload evidence와 owner approval; official hidden value 금지 |
| Production authority 없음 | Product/Platform/Security/Ops + Phase 14 | Accepted local 또는 미래 non-prod reference | Phase 14 gates + exact production deploy/cutover approval |
| `Q-VAR-01` deferred / `C-17` gated | Product/Domain/Algorithm/Architecture | Current baseline only | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, register restart evidence와 별도 C-17 scope + OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback 승인 |

## 7. 변경 요약

수정한 파일은 target
`docs/implementation/phases/phase-11-aws-reference-distribution.md`와 이 review
두 개뿐이다. Java/POM/build/deployment/canonical/neighbor/progress 문서는 수정하지
않았다. Target은 v1.3/`INDEPENDENT_REVIEWED_WITH_CORRECTIONS`가 되었지만 review
verdict는 `CHANGES_REQUIRED`이고 implementation/deployment/evidence/handoff/
production 상태는 각각 `NOT_STARTED`/`NOT_DEPLOYED`/`NOT_PRODUCED`/`NOT_READY`/
`NOT_GRANTED`로 유지했다.

## 8. 실제 검증 명령과 결과

| 검사/명령 | 결과 | 관찰 |
|---|---|---|
| `java -version && mvn -version` | `PASS INVENTORY` | Corretto 25.0.3, Maven 3.9.14 |
| `mvn clean verify` | `PASS PLACEHOLDER ONLY` | Exit 0; main 6, test source 1; tests 1, failures/errors/skipped 0; target AWS evidence 아님 |
| `git ls-files`, `rg --files`, POM/Java/GCP whole-file read | `PASS INVENTORY` | Root single module, tracked AWS source 0, GCP placeholder 확인 |
| `git check-ignore -v .serverless/* target` | `PASS INVENTORY` | `.serverless/`, `target/` ignored |
| `.serverless` CFN resource type count | `PASS HISTORICAL CHECK` | S3 1, Lambda 7, Step Functions 1, DynamoDB 1 등; current authority 아님 |
| Canonical/inventory `shasum -a 256` | `PASS` | Target metadata canonical 4와 actual inventory fingerprints 일치 |
| AWS official primary docs semantic cross-check | `PASS DOCUMENT REVIEW` | S3 conditional/consistency, Step Functions type, Lambda integration/retry, SSE-KMS 대조 |
| Phase 09/10 concurrent batch latest contract/blocker one-time read | `PASS` | Phase 10 Maven false-green 해소 확인; publication/cancel/deadline blocker만 Phase 11에 반영, neighbor 재추적 중단 |
| Live Phase/review inventory | `PASS` | Phase 문서 15/15 present, review 15/15 complete; authoring-time neighbor 값은 historical snapshot으로 격리 |
| Required non-empty/metadata/section structure | `PASS` | Target §1~§19와 review §1~§8, finding 8개 확인 |
| Relative Markdown link/actual GFM anchor | `PASS` | Local broken 0; Phase 10 F-P10-001/002/005의 em dash 제거 후 double-hyphen slug exact |
| Fence/trailing whitespace | `PASS` | Fence parity, trailing whitespace error 0 |
| AWS reference/implementation/deploy/production separation | `PASS` | `NOT_STARTED`/`NOT_DEPLOYED`/`NOT_GRANTED`/`NOT_PRODUCED`, fake authority 0 |
| no-DB/OPEN/GATED/deferred | `PASS` | DynamoDB 비권위, Q-BENCH/C-17/Q-VAR 상태 보존 |
| Pre/post-review evidence dependency cycle grep | `PASS` | Pre-review manifest의 reviewer/result/receipt ref 0; receipt만 evidence/review digest를 참조 |
| Phase 11 AWS evidence → Phase 12 cycle grep | `PASS` | Phase 11 evidence/receipt artifact의 Phase 12 manifest ref 0; Phase 12-owned forward-reference만 허용 |
| Canonical/neighbor fingerprint policy | `PASS` | Canonical 4 hash exact; adjacent/reciprocal acceptance hash 0 |
| Allowed write scope | `PASS` | 이 reviewer의 write call은 target + 새 review 두 파일뿐; scoped status에서 두 파일 untracked 확인 |
| `git diff --check` + untracked `git diff --no-index --check` | `PASS` | 두 허용 파일 whitespace error 0 |

검증 명령의 console output이나 현재 root test 1건을 `E-P11-*`, AWS deploy,
parity, security 또는 production evidence로 사용하지 않았다.

## ALNS-first direction revision addendum

Task `019fa901-8776-7f61-b467-a8c6595b970d`에서 Phase 11 AWS reference branch는
Phase 14A ALNS benchmark와 독립 병행 가능하고 14B에서만 합류함을 검토했다.
Phase 13은 ALNS-only cutover의 predecessor가 아니며 기존 review verdict,
implementation, acceptance와 evidence 상태는 변하지 않는다.
