# Phase 11 사람용 구현 가이드 — AWS reference distribution

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_correction_status: CORRECTED_ROUND_02_AWAITING_INDEPENDENT_REVIEW
guide_correction_round: "02"
guide_scope: Phase 11 only
canonical_phase_count: 15
phase: "11"
phase_name: aws-reference-distribution
canonical_phase_document: docs/implementation/phases/phase-11-aws-reference-distribution.md
canonical_phase_review: docs/implementation/reviews/phase-11-review.md
canonical_phase_document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
deployment_status_observed: NOT_DEPLOYED
phase_acceptance_status_observed: BLOCKED_NOT_IMPLEMENTED
evidence_status_observed: NOT_PRODUCED
production_authority_observed: NOT_GRANTED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS_AND_AWS_INTEGRATION_GATE
handoff_status_observed: NOT_READY
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_snapshot_at: "2026-07-29T02:36:43+0900"
inventory_snapshot_policy: CORRECTION_ROUND_01_ONE_TIME_LIVE_SNAPSHOT_UNCOMMITTED_UNAPPROVED
inventory_drift_status: PHASE00_REVIEW02_CHANGES_REQUIRED_FIX02_IN_PROGRESS
source_fingerprint_scheme: head_git_blob_plus_live_blob_for_untracked_or_drifting_sources
prerequisite_phases:
  - "00"
  - "06"
  - "07"
  - "08"
  - "09"
  - "10"
source_sections_and_fingerprints:
  docs/README.md: "current top-level map §권장 읽기 순서/§문서 계층 | 13f1b3b2dea038b8e0b466c138f5f59299413125"
  docs/master-design.md: "§1~4, §13~17; especially §15.10 RM-8 and §16.2~16.3 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "§1~3, §9~16, §18~21 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "§1~7, §10~20, §22 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/architecture-domain-implementation-design.md: "§1~3, §12~16, §19~28 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-BENCH-02/Q-INFRA-01/Q-VAR-01 rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~4, Phase 08~14, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§1~10 | HEAD 250aa90ae568a6b32ec905fa5ee456d430ff72cf; correction live 0419f69199b3140dd44020f78278b1352e6517b8"
  docs/implementation/phases/phase-08-application-ports-local-runtime.md: "§7.3~7.5, §9.1~9.3, §16.2~16.3 | 2aff093a6f2728470a7ccbb22b7e1a1a71f5b963"
  docs/implementation/phases/phase-09-object-storage-no-database.md: "§7.2, §7.5~9.6, §14~15.3 | 99a5b0df5531a65964272f423cc0ccca4d4f1430"
  docs/implementation/phases/phase-10-provider-neutral-coordinator.md: "§6.3~8.6, §13, §14.2 | 2b909924008df6c6f34d7d4d4399c8fdf6b6830a"
  docs/implementation/phases/phase-11-aws-reference-distribution.md: "§1~19 | 14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8"
  docs/implementation/reviews/phase-11-review.md: "§1~8 and F-P11-001~008 | 74377517a4018da73bc0e0bc8bf033ff7a3b0832"
  docs/implementation/phases/phase-12-provider-substitution.md: "§1~4, §15.1, §18.1~18.4 | 63b9defb25d7771bce590d593db9485367724918"
  docs/implementation/phases/phase-14-official-calibration-cutover.md: "§0.1, §3, §5.3~5.4, §8, §13~16 | c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26"
  docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md: "§9.4~9.6 and next handoff | live untracked blob 2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223"
  docs/implementation/human-guides/phases/phase-09-human-implementation-guide.md: "§9.5~9.10 and §17 | live untracked blob 1c31c4747362317c7f4ec8fb63c5f57627019769"
  docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md: "§8~10, §14.2 | live untracked blob 09f6f24e53167a4e320fc158640c13d1aa0cd29f"
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md: "§3.4, §9~13, §16.1 | live untracked blob d0557e019ec71fbeb1e931d589945949f7ac0074"
expected_reader:
  - Java의 interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, capacity, time-window propagation을 구현해 보았다
  - RPDPTW의 pair identity, CAS, provider-neutral coordinator와 AWS 운영 경계는 처음 접한다
owner_roles:
  implementation: AWS adapter/distribution owner
  application_contract: Phase 08 Application/Local Runtime owner
  storage_contract: Phase 09 Object Storage owner
  coordinator_contract: Phase 10 Provider-neutral Coordinator owner
  security: Platform Security/IAM/KMS owner
  operations: AWS Platform/SRE owner
  cost: FinOps/Platform owner
  independent_oracle: Phase 11 provider-integration test owner
  review: independent Phase 11 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P11-AWS-CONTRACT
  - E-P11-PARITY
  - E-P11-SECURITY
  - E-P11-OPERATIONS
  - E-P11-ROLLBACK
```

> 이 문서는 Java와 CVRPTW 경험이 있는 사람이 Phase 11의 경계를 배우고, gate가 열린 뒤 실제 AWS reference distribution을 구현하며, 독립 evidence로 완료 여부를 판정하기 위한 교육형 작업 지시서다. 현재 checkout에는 동시 작업 중인 **미커밋·미승인 Phase 00 review 02 `CHANGES_REQUIRED`와 fix 02 진행 상태**가 보이지만 accepted receipt는 없고 Phase 11의 S3, Step Functions, Lambda adapter, AWS distribution, source IaC, provider test 또는 evidence도 없다. 아래 Java 이름과 signature는 별도 표시가 없는 한 **제안 후보(`PROPOSED INTERNAL`)**이며 존재하는 API, 승인된 public/wire contract 또는 구현 완료 증거가 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 11은 Phase 09의 object-storage 의미를 S3에, Phase 10의 provider-neutral action을 Step Functions와 Lambda에 매핑하되, **AWS가 route·score·worker completeness·verification·publication의 의미를 새로 만들지 못하게 하는 단계**다.

AWS를 선택했다는 결정과 다음 네 가지는 서로 다르다.

```text
AWS target/reference 선택        = RESOLVED
Phase 11 Java/IaC 구현           = NOT_STARTED
isolated AWS integration evidence = NOT_PRODUCED
production 배포/cutover 권한      = NOT_GRANTED
```

따라서 “S3 bucket이 생겼다”, “Lambda가 응답했다”, “SAM deploy가 exit 0이었다”는 Phase 11 완료 신호가 아니다. 같은 immutable manifest를 local과 AWS에 넣었을 때 canonical artifact, state/action 의미, termination, 두 verifier와 publication 결과가 같고, 장애·보안·rollback evidence까지 봉인되어야 한다.

## 2. 큰 그림과 필요한 이유

### 2.1 CVRPTW solver를 Lambda에 올리는 일과 다른 이유

CVRPTW 프로그램을 cloud function으로 옮길 때는 handler가 JSON을 읽고 solver를 호출한 뒤 결과를 S3에 쓰는 것으로 충분해 보일 수 있다. RPDPTW에서는 이 shortcut이 다음 결함을 만든다.

- Handler가 raw input을 다시 해석하면 Phase 01의 canonicalization authority가 사라진다.
- Pickup과 delivery를 별도 event로 처리하면 pair의 same-vehicle·precedence·exactly-once가 깨질 수 있다.
- 완료된 Lambda 목록이나 S3 prefix list로 fan-in하면 누락 worker가 있어도 부분 성공을 publish할 수 있다.
- Step Functions `Choice`에서 score를 비교하면 Phase 10 comparator가 provider마다 복제된다.
- Platform retry 때 seed나 warm start를 바꾸면 같은 `WorkerRunId`가 다른 논리 작업이 된다.
- Lambda timeout을 `MAX_STEPS_REACHED`로 바꾸면 infrastructure failure가 정상 품질 종료처럼 보인다.
- S3 ETag나 key를 result identity로 쓰면 provider를 교체하는 순간 같은 결과가 다른 결과가 된다.
- KMS 암호화만 믿고 content digest를 생략하면 암호화된 corruption을 정상 artifact로 읽을 수 있다.
- Cancel request object와 publication pointer가 서로 다른 key인데 “먼저 쓴 쪽이 승자”라고 가정하면 둘 다 성공할 수 있다.

Phase 11의 기준 흐름은 다음과 같다.

```text
versioned AWS event
→ provider-neutral application command
→ exact immutable ArtifactRef read + digest/schema verification
→ Phase 10 AdvanceSolve
→ exact current pending ActionId 확인
→ thin Step Functions/Lambda side effect
→ immutable worker outcome/report put-if-absent
→ one authoritative run-state CAS
→ all-declared completeness
→ Phase 07 candidate verifier
→ finalization + result verifier
→ distinct publication precondition으로 pointer CAS
→ verified result exact retrieval
```

AWS execution ID, ARN, region, bucket, object version, request ID와 completion order는 관측 정보다. `SolveId`, `WorkerRunId`, manifest fingerprint, candidate/result digest와 publication eligibility의 입력이 아니다.

### 2.2 AWS reference가 기준인 것과 기준이 아닌 것

AWS reference는 provider adapter의 **실제 조건부 쓰기, retry, IAM, deadline, failure delivery와 운영 envelope를 검증하는 기준 구현**이다.

| 같아야 하는 semantic 영역 | AWS에서만 달라도 되는 observation 영역 |
|---|---|
| Problem/travel/profile/manifest fingerprint | Region, account alias, ARN |
| `SolveId`, round/worker/run identity | Step Functions execution handle |
| Seed, warm start, requested/completed ALNS work | Lambda request ID, cold start |
| State/action transition과 failure taxonomy | SDK retry count, provider latency |
| Candidate/result verifier disposition | S3 object version/ETag |
| Publishable result와 canonical result fingerprint | Deployment revision, log stream |

Reference라는 말은 “AWS output을 expected golden으로 복사한다”는 뜻이 아니다. Expected meaning은 hand oracle, Phase 10 fake/local oracle와 Phase 07 verifier가 먼저 정한다. AWS observation은 그 의미를 보존했는지 증명하는 피시험 대상이다.

### 2.3 Canonical 15 Phase 안의 위치

| Phase | Producer output 또는 책임 | Phase 11과의 관계 |
|---:|---|---|
| 00 | Reactor, module/package와 provider leakage rule | AWS SDK를 stable module 밖에 가두는 선행 gate |
| 01 | Canonical input와 normalization | AWS handler가 재해석하면 안 되는 authority |
| 02 | Complete `PreparedTravel`, immutable problem | S3에 저장·전달하되 의미를 바꾸지 않음 |
| 03 | Propagation, evaluation, comparator | ASL/Lambda가 복제하면 안 되는 계산 |
| 04 | Exact bound profile/capability | Runtime에서 `latest`나 customer fallback 금지 |
| 05 | Pair insertion과 최대 8개 portfolio | Worker가 실행할 immutable assignment의 기원 |
| 06 | COW ALNS, `WorkerRun`, replay·termination | Retry에서 보존할 seed/work/identity 생산자 |
| 07 | Candidate/result 두 verifier와 `PublishableResult` | Publication eligibility의 유일한 생산자 |
| 08 | Application use case와 provider-neutral ports/local oracle | AWS adapter가 구현할 port owner |
| 09 | Exact-key, immutable put, verified read, CAS, no-DB | S3 adapter의 storage contract owner |
| 10 | State/action, declared completeness, retry/cancel | Step Functions/Lambda가 매핑할 semantic owner |
| **11** | **S3 + Step Functions + Lambda reference** | **이 가이드의 유일한 구현 범위** |
| 12 | Storage/workflow/compute 축별 provider substitution | Phase 11 evidence/receipt의 downstream consumer; 자동 착수 아님 |
| 13 | Optional route pool/MIP | `C-17 GATED`; Phase 11이 구현·활성화하지 않음 |
| 14 | 14A benchmark와 14B official/production cutover | Phase 11 evidence는 14B 입력일 뿐 authority 자체가 아님 |

Critical path를 오해하지 않는다.

```text
00 → … → 08 → 09 → 10 → 11

06 → 07 → 08 → 14A ALNS benchmark
14A receipt → 13 optional
11 + 14A receipt (+ accepted 13 only for official hybrid) → 14B
```

Phase 13은 Phase 11의 후속 필수 단계가 아니다. Phase 14A는 AWS 없이 수행할 수 있다. Phase 11 완료가 Phase 14B production authority를 자동으로 열지도 않는다.

### 2.4 Producer와 consumer 계약

| 경계 | Producer가 보장할 것 | Phase 11/consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 06 → 11 | Stable worker identity, seed, warm start, requested/completed work, typed termination | Retry 때 work/seed를 재설정하거나 timeout을 정상 종료로 바꿈 |
| Phase 07 → 11 | `PublishableResult`와 two-gate disposition | S3 object 존재나 Lambda 성공으로 publishable을 합성 |
| Phase 08 → 11 | Inbound use case, outbound ports, local oracle, cancel/deadline seam | AWS DTO/ARN/Context를 application signature에 강요 |
| Phase 09 → 11 | Logical key/ref, exact verified read, immutable create, opaque state token, distinct publication precondition | Listing, check-then-put, last-write-wins 또는 run-state token 재사용 |
| Phase 10 → 11 | Exact pending action, state transitions, declared completeness, retry/cancel/deadline contract | ASL에서 champion/completeness/verifier를 다시 구현 |
| Phase 11 → 12 | Standalone AWS evidence digest와 post-review acceptance receipt | Phase 12-owned conformance manifest를 역참조하거나 provider adoption 승인 |
| Phase 11 → 14B | Deployment/rollback, security/operations/parity bundle, limitations | Production deploy/traffic/pointer authority를 스스로 발행 |

## 3. Source authority, fingerprint와 정확한 읽기 순서

### 3.1 충돌 해소 순서

```text
사용자 고정 지시
→ current docs/README.md top-level map
→ canonical docs/master-design.md
→ canonical question register의 exact Q-* 상태
→ canonical docs/domain-design.md의 domain 의미
→ canonical docs/architecture-design.md의 module/port 배치
→ canonical integrated design의 15 Phase/no-DB/AWS mapping
→ implementation README/plan/Phase 11/review
→ direct Phase 08/09와 인접 Phase 10/12의 stable handoff section
→ historical cross-check
```

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md), deprecated 문서와 `docs/codex/*`는 역사 cross-check일 뿐 현재 authority가 아니다. 날짜가 붙은 [Domain](../../../2026-07-26-domain-design.md)과 [Architecture](../../../2026-07-26-architecture-design.md)는 각각 plain 문서를 `supersedes`한다고 적지만, 현재 [top-level map](../../../README.md#문서-계층과-규범-지위)과 Canonical Master §1.4는 plain [Domain](../../../domain-design.md)과 [Architecture](../../../architecture-design.md)를 current 상세 설계로 가리킨다. 이 correction은 사용자 고정 canonical 5문서와 current map을 따라 plain 문서를 사용한다. 이 source-governance 충돌은 repository 전체에서 해소되지 않았으므로 상위 owner가 반대 결정을 내리면 구현을 중지하고 source index, section semantic diff, requirement/WP/test/evidence fingerprint를 같은 변경 단위에서 다시 승인받는다.

| Source-set 차이 | 이 가이드의 correction 적용 | 영향받는 구현/판정 |
|---|---|---|
| Current Domain §13~§16은 finalization/outcome/error/acceptance evidence를 상세화하고 특정 cloud를 domain 비범위로 둠 | Pair/result/two-gate/failure authority는 current Domain을 사용 | WP11-2/3/4/7/8, publication·fault·parity oracle |
| Current Architecture §12~§18은 logical port, AWS implementation gate, observability/security와 provider compatibility suite를 분리함 | Module/port/test/operations 배치는 current Architecture를 사용 | WP11-1~7, Failsafe/report gate, S3/IAM/observability suite |
| 날짜 문서의 stale `Q-INFRA-01 DEFERRED`, `25/1/2`와 self-declared supersession | Master/register의 `Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`로만 해소 | AWS target 선택만 resolved; 구현·evidence·production은 계속 gated |

이 authority 선택은 AWS 구현·배포·production 완료를 뜻하지 않는다.

### 3.2 검증 가능한 source fingerprint

아래 Git blob은 metadata의 `inventory_observed_at_commit`에서 `git rev-parse HEAD:<path>`로 얻었다. Source가 바뀌면 hash만 고치지 말고 변경된 heading의 의미가 requirement, WP, test, evidence와 gate에 미치는 영향을 review한다.

| Source | 직접 읽을 heading/section | HEAD Git blob |
|---|---|---|
| [Current top-level map](../../../README.md#문서-계층과-규범-지위) | 권장 읽기 순서, canonical current entry와 문서 계층 | `13f1b3b2dea038b8e0b466c138f5f59299413125` |
| [Canonical Master §4](../../../master-design.md#4-구현-아키텍처와-책임-경계) | §1~4, §13~14.1, §15.10, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Canonical Domain §13](../../../domain-design.md#13-finalization-outcomes와-diagnostics) | §1~3, §9~16, §18~21 | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Canonical Architecture §12](../../../architecture-design.md#12-provider-neutral-logical-ports) | §1~7, §10~20, §22 | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Integrated design §15](../../../architecture-domain-implementation-design.md#15-phase-11--selected-aws-targetreference-distribution) | §1~3, §12~16, §19~28 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register `Q-INFRA-01`](../../../master-design-open-questions.md#q-infra-01) | §1~4, exact `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README §6](../../README.md#6-phase-작업-순서) | §0~7, 15 Phase, ALNS-first DAG, status authority | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan Phase 11](../../master-realization-plan.md#phase-11--aws-reference-distribution) | §2~4, Phase 08~14, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry) | §1~10, implementation registry와 scheduler authority | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Actual Phase 08 §7.3](../../phases/phase-08-application-ports-local-runtime.md#73-artifactstorage-contracts-produced-for-phase-09) | §7.3~§7.5, §9.1~§9.3, §16.2~§16.3 | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| [Actual Phase 09 §7.2](../../phases/phase-09-object-storage-no-database.md#72-logical-namespace와-key-grammar) | §7.2, §7.5~§9.6, §14~§15.3 | `99a5b0df5531a65964272f423cc0ccca4d4f1430` |
| [Actual Phase 10 §14.2](../../phases/phase-10-provider-neutral-coordinator.md#142-next--actual-but-unaccepted-phase-11-aws-reference) | §6.3~8.6, §13, §14.2 | `2b909924008df6c6f34d7d4d4399c8fdf6b6830a` |
| [Canonical Phase 11 §4](../../phases/phase-11-aws-reference-distribution.md#4-entry-gate와-확인-evidence) | 전체, 특히 §3~18 | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` |
| [Phase 11 review §4](../../reviews/phase-11-review.md#4-findings) | §1~8, `F-P11-001`~`F-P11-008` | `74377517a4018da73bc0e0bc8bf033ff7a3b0832` |
| [Actual Phase 12 §18.2](../../phases/phase-12-provider-substitution.md#182-phase-11에서-받는-handoff) | §1~4, §15.1, §18.1~18.4 | `63b9defb25d7771bce590d593db9485367724918` |
| [Actual Phase 14 §3.1](../../phases/phase-14-official-calibration-cutover.md#31-gate-matrix) | §0.1, §3, §5.3~5.4, §8, §13~16 | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` |
| [Phase 08 사람용 guide §9.4](phase-08-human-implementation-guide.md#94-storage-port-skeleton과-open-failure-carrier) | §9.4~§9.6와 다음 handoff | Live untracked blob `2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223` |
| [Phase 09 사람용 guide §9.5](phase-09-human-implementation-guide.md#95-application-owned-baseline-port) | §9.5~§9.10와 §17 | Live untracked blob `1c31c4747362317c7f4ec8fb63c5f57627019769` |
| [Phase 10 사람용 guide §14.2](phase-10-human-implementation-guide.md#142-phase-11에-넘길-것) | §8~§10와 §14.2 | Live untracked blob `09f6f24e53167a4e320fc158640c13d1aa0cd29f` |
| [Phase 12 사람용 guide §16.1](phase-12-human-implementation-guide.md#161-phase-11에서-받아야-할-것) | §3.4, §9~§13과 §16.1 | Live untracked blob `d0557e019ec71fbeb1e931d589945949f7ac0074` |

`execution-progress-and-results.md`는 concurrent scheduler 작업으로 live bytes가 바뀌었다. HEAD blob은 위 값이고 correction snapshot의 live `git hash-object`는 `0419f69199b3140dd44020f78278b1352e6517b8`이다. Live 문서는 Phase 00 review 02를 `CHANGES_REQUIRED`, fix 02를 `IN_PROGRESS`, evidence를 `REJECTED_PENDING_FIX_02_REGENERATION`으로 기록하며 accepted receipt는 없다. Phase 11 acceptance는 열리지 않는다. 이 가이드는 이후 외부 drift를 승인하지 않으며 구현 재개 때 새 inventory receipt를 만든다.

재검증 예:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/README.md
git rev-parse HEAD:docs/master-design.md
git rev-parse HEAD:docs/domain-design.md
git rev-parse HEAD:docs/architecture-design.md
git rev-parse HEAD:docs/implementation/phases/phase-08-application-ports-local-runtime.md
git rev-parse HEAD:docs/implementation/phases/phase-09-object-storage-no-database.md
git rev-parse HEAD:docs/implementation/phases/phase-11-aws-reference-distribution.md
git rev-parse HEAD:docs/implementation/reviews/phase-11-review.md
git hash-object docs/implementation/execution-progress-and-results.md
git hash-object docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md
git hash-object docs/implementation/human-guides/phases/phase-09-human-implementation-guide.md
```

인접 Phase의 whole-file digest를 서로의 acceptance 조건으로 만들지 않는다. Entry에서는 인용 section의 semantic diff와 accepted implementation evidence identity를 확인한다.

### 3.3 구현 전 정확한 읽기 순서

| 순서 | 읽을 곳 | 답해야 할 질문 | 확인할 module/file/evidence |
|---:|---|---|---|
| 1 | [Current top-level map](../../../README.md)와 [Implementation README §0~7](../../README.md) | Canonical 5문서, 15 Phase, ALNS-first DAG와 status owner는 누구인가? | Source selection conflict, Phase 11 filename/review, scheduler 규칙 |
| 2 | Master §4, §13~14.1, §15.10, §16 | Provider가 보존해야 할 identity, replay, publication와 migration 의미는 무엇인가? | Logical port, both-gate, `RM-8` |
| 3 | Canonical Domain §1~§3/§9~§16 | Pair/route-bank/result/termination/incomplete의 정확한 의미는 무엇인가? | `Request`, stable partition, verified result |
| 4 | Canonical Architecture §1~§7/§10~§20 | Module DAG, logical port, AWS gate, retry/cancel, security/test 경계는 어디인가? | Stable modules, adapter/distribution/test placement |
| 5 | Integrated design §3, §12~16, §19~25 | No-DB, AWS mapping, failure, test, anti-pattern은 무엇인가? | `object-s3`, Step Functions, Lambda target tree |
| 6 | Question register exact rows | 무엇이 확정이고 무엇이 open/deferred인가? | `Q-INFRA-01`, `Q-BENCH-02`, `Q-VAR-01` |
| 7 | Master Plan Phase 08~14, §8~15 | Entry/exit/evidence/DoD/rollback과 14A/14B 분리는 무엇인가? | `E-P11-*`, production gate |
| 8 | Actual/Human Phase 08 §7.3~§7.5/§9.1~§9.3/direct handoff | AWS adapter가 구현할 port, access/failure/cancel/deadline seam과 미승인 blocker는 무엇인가? | Accepted `Phase08ApplicationStorageContractManifest`, local oracle, receipt |
| 9 | Actual/Human Phase 09 §7.2/§7.5~§9.6/§14~§15 | Exact key/verified read/CAS/publication, tenant/failure와 S3 owner 경계는 무엇인가? | Accepted `Phase09StorageHandoff`, boundary decision, receipt |
| 10 | Actual/Human Phase 10 §6.3~§8.6/§14.2 | AWS가 받을 exact state/action/identity와 residual blocker는 무엇인가? | `Phase10HandoffManifest`, accepted receipt |
| 11 | Phase 11 전체 | Mapping, WP, test, IAM/KMS, IaC, evidence의 canonical 상세는 무엇인가? | §4 entry, §6 contracts, §11~15, §17 |
| 12 | Phase 11 original review 전체 | 어떤 교정이 적용됐고 무엇이 아직 blocker인가? | `F-P11-001`~`008`, verdict `CHANGES_REQUIRED` |
| 13 | Actual/Human Phase 12 handoff | 어떤 evidence dependency 방향만 허용되는가? | Phase 11 standalone evidence → Phase 12-owned manifest |
| 14 | Phase 14 §3/§5/§8 | Phase 11 evidence와 production authority가 왜 다른가? | 14B gate, signed authority, rollback |
| 15 | Execution Progress §5~10 | 현재 구현 상태와 status 변경 권한은 무엇인가? | Phase 00 review 02 pending, Phase 11 `BLOCKED_NOT_IMPLEMENTED`, scheduler only |
| 16 | 실제 repository inventory | Module/type/test/IaC/evidence가 실제로 존재하는가? | Root/live POM, target AWS path, Failsafe/profile/report, ignored artifacts |

### 3.4 Entry gate를 읽고 서명하는 순서

실제 adapter 구현이나 AWS 배포 전 `Phase11EntryReceipt`가 다음 AND 조건을 가리켜야 한다.

1. Accepted Phase 00 architecture evidence와 AWS SDK leakage rule.
2. Accepted Phase 06 worker/replay contract.
3. Accepted Phase 07 `PublishableResult`와 two-gate contract.
4. Accepted Phase 08 ports/local oracle의 exact source section, implementation artifact/evidence identity와 handoff receipt. §9의 proposed signature와 다르면 proposed signature를 폐기하고 owner-approved 하나만 채택한다.
5. Accepted Phase 09 exact-key/digest/CAS/tenant/failure contract, distinct publication precondition, S3 ownership decision과 exact source section semantic diff. Whole-file reciprocal hash는 acceptance 대용이 아니다.
6. Accepted Phase 10 exact pending action, same-state cancel fence와 restart-safe deadline contract.
7. Reviewed AWS workflow type/invocation mode, IAM/KMS/network/key-layout/retry/DLQ/quota/retention/cost ADR.
8. Isolated non-production account alias, region, scoped role, budget guard, cleanup owner.
9. Named implementation/security/operations/cost/review owners와 scheduler task.

하나라도 없으면 허용되는 일은 source review, fixture, red test와 offline IaC draft까지다. Mock receipt, Console screenshot, generated template 또는 temporary production default로 entry를 열지 않는다.

## 4. RPDPTW primer, 용어집, identity와 lifecycle

### 4.1 CVRPTW customer와 RPDPTW request

CVRPTW에서는 customer 하나가 한 visit인 경우가 많다. RPDPTW에서는 한 `Request`가 pickup과 delivery 의미를 함께 소유한다.

| 용어 | 짧은 의미 | AWS에서 잘못 번역한 예 |
|---|---|---|
| `Order` | 외부 business 입력 | Raw order ID를 S3 key에 그대로 붙임 |
| `Request` | 원자 운송 업무와 pair ownership | Pickup/delivery를 독립 message로 성공 처리 |
| `Node` | Immutable service/terminal 정의 | Physical address와 node identity를 동일시 |
| `Visit` | Route 안에서 node를 수행하는 occurrence | Lambda invocation을 visit로 간주 |
| `Route` | Concrete vehicle + ordered visits + terminal policy | Worker output의 request set만 보고 route라 부름 |
| `SearchRequestBank` | Stable state에서 route 밖의 request 집합 | DLQ를 unassigned bank로 사용 |

모든 stable solution에서 request는 다음을 만족해야 한다.

```text
routeOwnerCount(request) + bankMembershipCount(request) = 1

route에 있으면:
  pickup과 delivery가 같은 concrete vehicle
  필요한 visit이 각각 exactly once
  pickup position < delivery position
```

이 불변조건은 S3 object 수, Lambda 성공 수 또는 Step Functions branch 성공과 무관하다. AWS는 이미 검증된 domain artifact를 운반하고 실행할 뿐이다.

### 4.2 Phase 11에서 자주 쓰는 용어

| 용어 | 이 가이드의 의미 |
|---|---|
| `ArtifactRef` | Kind/schema/digest/length와 opaque locator를 가진 immutable content reference |
| Exact read | 선언된 단일 key를 읽고 bytes 노출 전 length/schema/digest를 검증 |
| Put-if-absent | Same key를 새로 만들거나 same digest에만 idempotent converge하는 create-once |
| CAS | Read에서 얻은 opaque version/precondition이 아직 current일 때만 교체 |
| Run-state token | Run state repository의 equality/CAS에만 쓰는 opaque token |
| Publication precondition | Published pointer의 exact read/CAS에만 쓰는 distinct token |
| `ActionId` | Phase 10이 commit한 provider side effect의 stable logical identity |
| `WorkerRunId` | Seed/warm start/config/requested work가 고정된 logical worker identity |
| `AttemptId` | 같은 logical run을 다시 실행하는 application attempt identity |
| Wake-up hint | State를 다시 읽게 하는 신호; completeness나 success authority가 아님 |
| Provider observation | ARN, request ID, ETag, latency, SDK retry처럼 semantic identity 밖의 정보 |
| Declared completeness | Manifest가 선언한 모든 worker의 exact outcome/ref가 정상 완료·검증됨 |
| Two-gate publication | Candidate verifier와 result verifier가 모두 PASS한 결과만 publish |
| Last safe point | 승인되지 않은 side effect/publication 없이 되돌아갈 수 있는 경계 |

### 4.3 Identity를 하나의 문자열로 생각하지 않기

```text
SubmissionId
  └─ SolveId + ManifestFingerprint
      └─ RoundOrdinal
          └─ WorkerOrdinal + WorkerRunId
              └─ AttemptId

Artifact identity
  = kind + schema + canonical content digest

Provider observation
  = bucket/key/version/ETag/ARN/region/execution/request ID
```

Retry 규칙:

```text
same WorkerRunId
+ same assignment/seed/warm start/requested steps
+ new AttemptId
```

Same logical worker와 same verified success digest는 duplicate converge다. Same logical worker와 different verified digest는 “더 좋은 것을 선택”하지 않고 reproducibility integrity failure다.

### 4.4 Artifact, state와 observation lifecycle

```text
immutable input/manifest/action artifact create
→ exact read-back + digest/schema verify
→ provider side effect for exact current pending ActionId
→ immutable outcome/verifier/failure observation create
→ one authoritative run-state CAS
→ all-declared exact refs 확인
→ two verifier
→ PUBLISHING authorization CAS
→ distinct publication precondition exact read/CAS
→ published pointer exact retrieval
```

부분 실패로 만들어진 unreferenced S3 object는 result가 아니다. Step Functions history와 DLQ message도 application state가 아니다. Recovery는 exact run state와 declared keys를 다시 읽고 reconcile한다.

### 4.5 Cancel과 publication lifecycle

다음 세 가지를 분리한다.

```text
cancel intent object       = durable request/hint
CANCEL_REQUESTED state CAS = terminal-intent fence
actual worker stop         = provider/application observation
```

`CANCEL_REQUESTED`와 `PUBLISHING`은 **같은 run-state expected version**에서 경쟁한다. Intent object와 publication pointer처럼 서로 다른 key의 쓰기 순서를 승자 판정에 쓰지 않는다.

```text
CANCEL_REQUESTED wins → new publish action 금지
PUBLISHING wins       → 늦은 cancel은 too-late/no-op
둘 다 authoritative  → 결함
```

### 4.6 Deadline을 하나의 timeout으로 합치지 않기

| 값 | 의미 | 서로 바꾸면 안 되는 값 |
|---|---|---|
| Algorithm step budget | 정상 품질 작업량 | Wall-clock/Lambda timeout |
| Application watchdog | 논리 exceptional deadline | `MAX_STEPS_REACHED` |
| Lambda remaining time | Platform observation | Algorithm completed steps |
| Step Functions task timeout | Provider delivery/execution bound | Normal termination |
| SDK operation deadline | Transport call bound | Application attempt identity |

Crash-resume 뒤 새 JVM의 monotonic origin을 이전 process tick과 직접 비교할 수 없다. Durable representation과 restart-origin reconciliation ADR가 없으면 deadline-enabled AWS path는 off이고, explicit `TEST_ONLY` virtual clock이 마지막 안전 지점이다.

### 4.7 Phase 11 고정 불변조건

1. Core/solver/verification/application의 AWS SDK, ARN, Lambda event reference는 0이다.
2. Business authority는 immutable artifact + single CAS state/pointer이며 database가 아니다.
3. S3 key/version/ETag는 domain/result identity가 아니다.
4. Same key/different digest overwrite는 0이다.
5. Listing/event/history/DLQ는 wake-up 또는 observation일 뿐 completeness authority가 아니다.
6. Step Functions는 action type만 매핑하고 objective/comparator/verifier/customer 의미를 보지 않는다.
7. Provider side effect 전 exact current pending `ActionId`, payload fingerprint, transition ordinal을 확인한다.
8. Run-state token을 event/action/publication token으로 재사용하지 않는다.
9. Retry는 `AttemptId`만 바꾸고 seed/warm start/work/manifest를 바꾸지 않는다.
10. Lambda timeout/resource failure를 normal algorithm termination으로 바꾸지 않는다.
11. Declared worker 하나라도 없거나 미검증이면 round는 `INCOMPLETE`이고 publication은 없다.
12. Candidate/result 두 verifier PASS 이전 publication은 0이다.
13. API/coordinator/worker/workflow/deploy/test role을 분리한다.
14. Encryption, integrity, authorization은 서로 대체하지 않는다.
15. Secret, raw address/PII, full payload를 log/metric/trace에 넣지 않는다.
16. Quota 부족을 worker 축소, payload truncate 또는 verification skip으로 숨기지 않는다.
17. Open/gated/deferred 값을 service default나 legacy 값으로 닫지 않는다.
18. Non-production Phase 11 pass는 production authority가 아니다.

## 5. 실제 repository inventory — HEAD baseline, live drift와 목표

### 5.1 조사 방법과 고정 정책

HEAD baseline은 commit `7cc890ee1d0805df5ae14b633127fade4f978639`의 tracked tree다. Correction live inventory는 `2026-07-29T02:36:43+0900`에 같은 checkout의 concurrent uncommitted 작업을 read-only로 한 번 관찰한 것이다. 이 시점은 review target의 authoring snapshot이나 독립 review snapshot과 다르며 서로 합치지 않는다.

```text
HEAD:
  single root JAR
  src/main Java 6 / src/test Java 1
  direct GCP Workflow/Storage + Jackson dependency
  synthetic AlnsBatchEngine와 GCP orchestration placeholder

correction live drift:
  root pom → unaccepted reactor parent
  .mvn/, build/, legacy/, rpdptw/와 wrapper 추가
  root legacy source 삭제 + legacy/gcp-placeholder로 이동
  rpdptw stable package ownership용 package-info scaffold
  root 포함 POM 13개, rpdptw main Java 23개는 모두 package-info
  non-node_modules test Java 15개 + package-info 1개
  Surefire 3.5.4만 configured; Failsafe/profile 0
  progress tracker에서 Phase 00 review 02 CHANGES_REQUIRED, fix 02 IN_PROGRESS

Phase 11 target:
  object-s3/workflow-stepfunctions/compute-lambda adapters
  api/coordinator/worker apps + aws-serverless distribution
  deployment/aws source IaC
  contract/actual-AWS/fault/security/parity/rollback tests
  immutable E-P11-* evidence와 accepted post-review receipt
```

Live root `pom.xml`의 HEAD blob은 `f8a411eadd4a5c01d8dd09fdea462738ca63d65f`, correction live `git hash-object`는 `1dc675ba17b7f2202f34a22131f152cc2868b075`이고 SHA-256은 `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`다. Live progress blob은 `0419f69199b3140dd44020f78278b1352e6517b8`, SHA-256은 `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c`다. 이 외부 변경은 미커밋·미승인 Phase 00 candidate이며 Phase 11이 수정하거나 acceptance를 주장하지 않는다. 이후 다른 작업자가 live tree를 바꾸더라도 이 가이드는 snapshot을 다시 정의하지 않고 구현 재개 checkpoint에서 새 inventory receipt를 만든다.

### 5.2 현재 vs 목표 inventory

| 영역 | HEAD baseline | Correction live inventory | Phase 11 목표/판정 |
|---|---|---|---|
| Root build | Single `ro-next` JAR | Parent reactor POM으로 수정 | Phase 00 acceptance 전 미승인 |
| Toolchain | Maven 3.9.14, Java 25.0.3 관찰 | `mvnw`, wrapper properties, toolchain example 추가 | 존재는 pinned/reproducible build acceptance가 아님 |
| Maven modules | Target stable module 없음 | `node_modules` 제외 POM 13개, `rpdptw` 6 child + build/legacy scaffold | AWS adapter/distribution/port-contract module 없음 |
| RPDPTW main source | 없음 | Java 23개, 모두 `package-info.java` | Concrete interface/record/service 0 |
| RPDPTW tests | 없음 | `rpdptw/**/src/test/java` Java 0 | Phase 06~11 evidence 0 |
| Maven test lifecycle | Root Surefire 없음 | Surefire `3.5.4`, `failIfNoTests=false`; Failsafe/profile 없음 | Pinned Failsafe binding, JUnit/fixture dependency, profile와 report audit 필요 |
| Non-node test inventory | Root test 1 | Concrete test Java 15: build 10, legacy 5; build test `package-info` 1 별도 | Phase 11 required 64 case는 0 |
| `adapters/` | Tracked target module 없음 | `legacy-win-json`, `object-filesystem` directory에 file 0 | `object-s3`, workflow, compute target 모두 부재 |
| `apps/` | 없음 | `apps/cli` directory에 file 0 | API/coordinator/worker app 부재 |
| `distributions/aws-serverless` | 없음 | Directory 자체 없음 | POM/source/resource/test 부재 |
| `deployment/aws` | 없음 | Directory 자체 없음 | SAM/CFN/ASL/policy/alarm source 부재 |
| `build/port-contract-tests` | 없음 | Directory 자체 없음 | Shared provider contract suite 부재 |
| Legacy GCP | Root `src/**`, Docker/GCP | `legacy/gcp-placeholder`로 이동 중, characterization tests 추가 | Migration inventory only |
| Ignored `.serverless` | Generated artifact | S3, Lambda 7, Step Functions와 DynamoDB resource 포함 | Source/reproducible evidence 아님; no-DB 위반 |
| Phase 11 evidence | 없음 | `E-P11-*`, deploy/rollback/receipt 없음 | `NOT_PRODUCED` |

Correction live target-scope checks:

```text
ABSENT adapters/object-s3
ABSENT adapters/workflow-aws-stepfunctions
ABSENT adapters/compute-aws-lambda
ABSENT distributions/aws-serverless
ABSENT deployment/aws
ABSENT build/port-contract-tests
ABSENT serverless.yml
ABSENT package.json
```

### 5.3 Placeholder와 source/evidence를 구분하기

- `package-info.java`는 package ownership 방향을 보여주지만 executable contract가 아니다.
- `pom.xml`과 directory는 module implementation이 아니다.
- `.serverless/cloudformation-template-update-stack.json`은 ignored generated output이며 source IaC가 아니다.
- 그 generated template의 DynamoDB table은 no-DB target과 충돌하므로 복사하지 않는다.
- `node_modules/@aws*`와 `aws-sdk`는 추적된 Java dependency 또는 approved toolchain이 아니다.
- Root/legacy placeholder test pass는 Phase 11 contract/parity/security test가 아니다.
- CloudFormation/SAM empty change set exit 0은 새 deployment evidence가 아니다.

### 5.4 Read-only inventory 명령

```bash
git status --short
git rev-parse HEAD
git rev-parse HEAD:pom.xml
git hash-object pom.xml
find rpdptw build legacy adapters apps -type f -not -path '*/target/*' | sort
find rpdptw -type f -name '*.java' ! -name package-info.java
find rpdptw -type f -path '*/src/test/java/*' -name '*.java'
test ! -d adapters/object-s3
test ! -d adapters/workflow-aws-stepfunctions
test ! -d adapters/compute-aws-lambda
test ! -d distributions/aws-serverless
test ! -d deployment/aws
git check-ignore -v .serverless node_modules target
```

“없음” 검사가 성공하는 것은 Phase 11 green이 아니라 target이 아직 없다는 inventory evidence다.

## 6. Scope, non-scope, 결정 상태와 사람 승인

### 6.1 포함 범위

- Phase 09 storage port의 S3 conditional-operation adapter
- Logical key와 adapter-private bucket/key/version/ETag mapping
- API/coordinator/worker Lambda inbound adapter와 versioned event
- Phase 10 action을 매핑하는 thin Step Functions definition/adapter
- Exact pending action authorization과 duplicate/retry reconciliation
- IAM least privilege, tenant isolation, KMS/TLS와 conditional-write bucket policy
- Deadline/quota/preflight, failure destination/DLQ-equivalent reconciliation
- Structured logging, bounded metrics, trace, alarm, redaction과 cost evidence
- Versioned SAM/CloudFormation/ASL source와 immutable distribution provenance
- Isolated non-production AWS contract/parity/fault/security/rollback rehearsal
- Standalone pre-review evidence와 별도 post-review acceptance receipt

### 6.2 명시적 비범위

- Domain, solver, verifier, application state machine 또는 Phase 09/10 port 의미 재설계
- S3 listing/Step Functions/Lambda에서 champion, completeness, score, verifier 구현
- DynamoDB/RDS/cache/queue state를 business authority로 추가
- Public API/wire schema와 legacy `gs://` 호환 최종 승인
- Official worker/round/step/watchdog와 production resource 수치 확정
- Phase 12 provider 선택, adapter/cutover 또는 conformance manifest 소유
- Phase 13 route pool/MIP/OR-Tools dependency/native package/activation
- Phase 14 official benchmark, production deploy, traffic/pointer cutover
- `Q-VAR-01`, multi-trip/rotation 또는 다른 optional variant
- Ignored `.serverless`를 source IaC로 승격

### 6.3 확정, proposed/open, gated, deferred

| 항목 | 상태 | 구현자가 할 수 있는 것 | 임의로 하면 안 되는 것 |
|---|---|---|---|
| S3 + Step Functions + Lambda target | `RESOLVED` | Reference adapter/IaC contract 준비 | 구현·배포·production 완료 주장 |
| No-database | `FIXED` | Object + exact key + CAS 사용 | DynamoDB로 state/CAS 대체 |
| Provider-neutral dependency | `FIXED` | SDK를 adapter/distribution에 격리 | Root/core/application POM에 AWS SDK 추가 |
| Exact Java type/method 이름 | `PROPOSED/OPEN` | Semantic skeleton과 red test 제안 | Public compatibility 확정 |
| S3 key encoding/CAS API | `PROPOSED/ADR + ACTUAL AWS REQUIRED` | 후보와 actual concurrency test | Check-then-put/list/last-write-wins |
| S3 versioning/delete-marker interpretation | `OPEN/STORAGE ADR + ACTUAL AWS REQUIRED` | Current-version/read/CAS/delete-marker matrix 검증 | Provider version을 logical identity로 승격하거나 absent를 추측 |
| Multipart/copy/body replay | `OPEN/STORAGE+SECURITY ADR REQUIRED` | Operation별 지원·deny·cleanup/replayability를 fail-closed 검증 | Single-part retry 의미를 그대로 재사용 |
| Tenant credential source | `OPEN/SECURITY ADR REQUIRED` | Explicit `TenantCredentialProviderRef`와 scoped temporary authority 검증 | Shared broad role 또는 ambient credential fallback |
| Workflow type/invocation mode | `OPEN/ADR_REQUIRED` | Explicit config와 fail-closed test | AWS default/fallback 사용 |
| IAM tenant partition | `OPEN/SECURITY_ADR_REQUIRED` | Scoped session 또는 dedicated resource 후보 검증 | Shared broad role + application string check |
| Network/VPC/endpoint | `OPEN/ADR_REQUIRED` | Ingress/egress 불변조건과 후보 측정 | Unauthenticated function URL |
| Memory/timeout/concurrency/quota | `OPEN/ENVIRONMENT_MEASURED` | Explicit `TEST_ONLY` integration 값 | Production default 승격 |
| Retention/cost cap | `OPEN/OWNER_APPROVAL_REQUIRED` | Usage vector 수집 | “저비용/scale-to-zero” 주장 |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Explicit experiment-only manifest | Official 숫자 생성 |
| Phase 12 target provider | `GATED/UNSELECTED` | Phase 11 evidence만 전달 | Future provider module/cutover 당김 |
| Phase 13 | `OPTIONAL / C-17 GATED TARGET` | 아무 구현도 하지 않음 | OR-Tools/default 활성화 |
| `Q-VAR-01` | `DEFERRED` | 현재 pair/terminal/bank 보존 | 질문·option·구현 |
| Phase 14A | `NOT_RUN` | Phase 11과 독립임을 보존 | AWS pass를 ALNS benchmark receipt로 사용 |
| Phase 14B | `AUTHORITY GATED` | Handoff bundle 준비 | Production deploy/traffic/cutover |

### 6.4 사람 승인이 필요한 결정과 마지막 안전 지점

| 결정/gate | 승인 owner | 승인 전 마지막 안전 지점 | 승인 없을 때 stop 범위 |
|---|---|---|---|
| Phase 00/06~10 acceptance | Predecessor owners + scheduler | 문서, fixture, red test, offline IaC | Real adapter/AWS evidence/acceptance |
| Exact pending action + publication precondition | Phase 08/09/10/11 | Pure action mapping + immutable publishable ref; side effect/publication off | WP11-2/4 exit, publication |
| Same-state cancel/publish fence | Phase 08/10/11 | Durable cancel request only; new side effect off | Cancel/publish integration |
| Durable deadline restart | Phase 08/10/11 + Ops | `TEST_ONLY` virtual clock; deadline AWS path off | Deadline-enabled AWS execution |
| Workflow type/invocation mode | Platform/Ops/Reliability | Explicit red config/template tests | State machine deploy |
| S3 key/CAS/conditional policy | Storage/Security | Local contract oracle | Actual artifact/state/pointer write |
| S3 versioning/delete-marker/multipart/copy/body replay | Storage/Security/Ops | Unsupported combination startup fail; single-part local oracle | Actual S3 operation enablement |
| Tenant credential provider와 scope | Security/Platform | Explicit two-tenant negative fixture; provider lookup off | Runtime tenant data access |
| IAM/KMS/tenant/network | Security/Platform | Offline least-privilege model | Integration exposure/deploy |
| Quota/retention/cost | Ops/FinOps/Security | Bounded non-prod candidate | Production-readiness claim |
| Isolated AWS environment | Platform/Security/FinOps | Local/emulator only | Actual AWS suite |
| Production authority | Product/Release/Security/Ops + Phase 14 | Accepted non-prod reference 또는 local | Production apply/cutover |

Stop은 실패가 아니다. 미승인 경계를 편의상 default로 채우지 않는 것이 Phase 11의 핵심 안전성이다.

## 7. 사람을 위한 학습 경로

### 7.1 1단계 — 개념을 말로 설명하기

완료 신호:

- S3 object와 application artifact의 차이를 설명한다.
- Run-state token, `ActionId`, publication precondition을 서로 바꿀 수 없는 이유를 설명한다.
- Prefix list와 Step Functions history가 completeness authority가 아닌 이유를 설명한다.
- Cancel intent, `CANCEL_REQUESTED`, actual stop을 구분한다.
- Algorithm step, watchdog, Lambda timeout을 구분한다.
- AWS selection, Phase 11 acceptance, production authority를 구분한다.

자문 질문:

1. 이 필드는 semantic identity인가 provider observation인가?
2. AWS를 제거해도 이 interface의 의미가 남는가?
3. Duplicate/lost/out-of-order event 뒤 exact state를 다시 읽으면 수렴하는가?
4. Missing worker가 있을 때 어느 branch가 publication을 막는가?
5. 이 값은 승인된 contract인가 service default인가?

### 7.2 2단계 — 작은 탐색으로 현재 구조 확인하기

다음 순서로 읽기만 한다.

```bash
rg -n 'software\\.amazon|com\\.amazonaws|S3|StepFunctions|Lambda' \
  pom.xml rpdptw build adapters apps 2>/dev/null
find rpdptw -type f -name '*.java' ! -name package-info.java
find adapters apps distributions deployment -type f 2>/dev/null | sort
rg -n 'DynamoDB|prefix|listing|compareAndSet|putIfAbsent' \
  docs/implementation/phases/phase-09-object-storage-no-database.md \
  docs/implementation/phases/phase-10-provider-neutral-coordinator.md \
  docs/implementation/phases/phase-11-aws-reference-distribution.md
```

완료 신호는 “구현이 있다”가 아니라 HEAD/live/target과 owner 경계를 정확히 분류한 inventory note다.

### 7.3 3단계 — 실제 변경을 가장 작은 순서로 만들기

Gate가 열린 뒤에도 cloud부터 시작하지 않는다.

```text
accepted port/action signatures freeze
→ architecture/event/config red tests
→ S3 key/error/config unit tests
→ shared storage contract
→ Lambda handler contract
→ Step Functions action-only structure
→ IAM/KMS/tenant negative model
→ local/emulator mapping
→ isolated actual AWS contract/fault/security
→ local↔AWS parity
→ deploy/rollback/evidence seal
```

완료 신호는 각 단계가 다음 단계 없이도 독립적으로 실패 이유를 설명하고 rollback point를 가진다는 것이다.

### 7.4 4단계 — 통합하며 의미가 보존되는지 확인하기

통합 학습 질문:

- Local과 AWS의 canonical digest가 다르면 serialization 문제인가 domain 문제인가?
- Duplicate Lambda event가 다른 `AttemptId`를 만들었는가, 같은 SDK call retry인가?
- State CAS 성공 뒤 side effect response가 유실되면 exact pending action을 어떻게 reconcile하는가?
- Step Functions abort와 application cancellation이 동시에 오면 누가 terminal meaning을 정하는가?
- KMS deny가 `NotFound`로 collapse되어 tenant existence를 숨기거나 새지 않는가?
- Rollback 뒤 in-flight solve는 어느 exact state와 revision을 읽는가?

완료 신호는 normal뿐 아니라 missing worker, stale CAS, corruption, timeout, cancel, access deny, rollback에서 expected typed outcome과 publication absence를 독립 oracle로 설명할 수 있는 것이다.

## 8. 목표 module/package/file과 dependency

### 8.1 예상 change tree

아래는 gate가 열린 뒤의 목표이며 현재 존재를 주장하지 않는다.

```text
adapters/
├── object-s3/
│   ├── pom.xml
│   └── src/{main,test}/java/com/ronext/rpdptw/adapter/object/s3/
├── workflow-aws-stepfunctions/
│   ├── pom.xml
│   └── src/{main,test}/java/com/ronext/rpdptw/adapter/workflow/aws/
└── compute-aws-lambda/
    ├── pom.xml
    └── src/{main,test}/java/com/ronext/rpdptw/adapter/compute/aws/lambda/

apps/
├── api/
├── coordinator/
└── worker/

distributions/aws-serverless/
├── pom.xml
├── src/main/resources/logging/
└── src/test/java/com/ronext/rpdptw/distribution/aws/

deployment/aws/
├── README.md
├── samconfig.toml
├── template.yaml
├── state-machines/solve.asl.json
├── policies/
├── parameters/integration.json
├── parameters/production.example.json
└── alarms/required-alarms.yaml

build/port-contract-tests/
├── pom.xml
├── src/test/java/com/ronext/rpdptw/contract/aws/
│   ├── Phase11RequiredTestManifestContractTest.java
│   ├── Phase11S3ActualCaseCatalogContractTest.java
│   ├── Phase11EffectivePomContractTest.java
│   └── Phase11FreshReportContractTest.java
└── src/test/resources/phase11/
    ├── required-tests-v1.txt
    ├── required-test-cases-v1.json
    └── phase11-s3-actual-cases-v1.json

build/test-fixtures/src/main/resources/aws/
├── events/v1/
├── manifests/
├── state/
├── failures/
├── iam/
└── observability/
    ├── required-log-fields-v1.json
    ├── required-metrics-v1.json
    ├── required-alarms-v1.json
    ├── required-cost-fields-v1.json
    └── forbidden-fields-v1.json
```

### 8.2 Compile dependency

```text
rpdptw-core
  ↑
rpdptw-solver        rpdptw-verification
  ↑                         ↑
rpdptw-application ─────────┘
  ↑
adapters/object-common
  ↑
adapters/object-s3

rpdptw-application
  ↑
adapters/workflow-aws-stepfunctions
adapters/compute-aws-lambda
  ↑
apps/api | apps/coordinator | apps/worker
  ↑
distributions/aws-serverless

deployment/aws = packaging/config source; semantic compile owner가 아님
```

금지:

```text
core/solver/verification/application -X-> AWS SDK/event/ARN
application                         -X-> bucket/key/ETag
Step Functions ASL                 -X-> objective/comparator/verifier/customer
deployment config                  -X-> hidden semantic/algorithm default
```

#### 8.2.1 Maven test lifecycle contract

Phase 11 `*IT`는 file naming만으로 실행되지 않는다. Gate가 열린 뒤 Phase 00/build owner가 다음 POM contract를 승인하고 effective POM으로 검증해야 한다.

| 위치 | Required future wiring | Fail-closed 판정 |
|---|---|---|
| Root parent `pluginManagement` | `maven-failsafe-plugin` version `3.5.4`를 explicit pin; accepted Phase 00이 다른 version을 승인하면 source/effective-POM/test-manifest를 한 번에 재봉인 | Version/property/plugin 부재 또는 implicit version이면 fail |
| 각 Phase 11 IT owner module | Failsafe `integration-test`와 `verify` goal binding, explicit `**/*IT.java` include, JUnit Platform provider, `failIfNoTests=true` | Goal/include/provider/summary XML 하나라도 없으면 fail |
| Adapter/distribution test dependency | `org.junit.jupiter:junit-jupiter` test scope와 approved `rpdptw-test-fixtures` test-jar/classifier 또는 owner-approved fixture artifact | Local repository에 우연히 설치된 미선언 fixture로 compile되면 fail |
| `aws-local-it` profile | Explicit opt-in, emulator/local mapping만; actual AWS test exclude가 evidence에 표시됨 | Profile 미존재/미활성/실제 AWS evidence 주장 시 fail |
| `aws-integration` profile | Explicit opt-in, approved account/stage marker와 actual `*AwsIT`/provider cases include, `skipITs=false`, `skipTests=false` | Marker/credential authority/profile activation/test discovery 중 하나라도 없으면 fail |
| `build/port-contract-tests` catalog/report audit | `Phase11S3ActualCaseCatalogContractTest`가 §11.3.1 supplementary S3 catalog의 29 unique case↔fault↔report mapping과 sensitivity receipt schema의 literal `expectedSensitivityDisposition=RED`를 검사하고, fresh auditor가 §11.3 canonical manifest, supplementary catalog, same-run Surefire/Failsafe XML/P11CASE/activation receipt를 입력으로 두 expected set과 sensitivity negative disposition을 독립 계산 | Missing/extra/duplicate/failure/error/skipped/unmapped/stale/sensitivity-missing/sensitivity-survived/wrong-sensitivity-mapping 중 하나라도 non-empty, `|activatedSensitivity|` 또는 `|observedRed|`가 29가 아님, normal `PASS`/sensitivity `RED`와 non-zero exit 불일치, source/run/profile/deployment 또는 두 manifest digest 불일치면 non-zero |

`help:effective-pom`/`help:active-profiles` 출력은 그 자체로 acceptance evidence가 아니다. `Phase11EffectivePomContractTest`가 selected module마다 plugin version, execution id, `integration-test`+`verify`, include, provider, JUnit/fixture dependency, profile property와 skip 금지를 machine-check하고 결과를 source/effective-POM digest에 묶는다. Phase 11 module이 아직 없는 현재 live POM에는 Failsafe와 두 profile이 0개이므로 이 gate는 의도적으로 red다.

### 8.3 책임 배치 자문표

| 질문 | 배치 |
|---|---|
| “S3 412를 어떤 logical conflict로 매핑하는가?” | `adapter.object.s3` |
| “Same digest duplicate는 성공인가?” | Phase 09 application/storage contract |
| “Worker가 모두 끝났는가?” | Phase 10 coordinator |
| “어느 candidate가 더 좋은가?” | Core/profile comparator |
| “Result가 publish 가능한가?” | Phase 07 verifier/result |
| “Lambda event version을 어떻게 검증하는가?” | Compute inbound adapter |
| “ARN/role/memory/timeout은 어디서 고르는가?” | Distribution/deployment reviewed config |
| “Production traffic을 전환하는가?” | Phase 14 authority |

## 9. Proposed/Open Java 계약과 상태 전이

이 절은 구현 방향을 가르치는 **compile-shape skeletal contract**다. Pure port는 Phase 08/09/10 owner module의 `interface`이고 AWS module은 그 accepted interface를 구현하는 concrete `final class`다. 아래 concrete method에는 Java 25 문법상 필요한 body를 두지만 의도적으로 `UnsupportedOperationException`만 던지므로 완성 구현이나 runtime wiring evidence가 아니다. `Contract skeleton` throw가 남은 distribution은 assembly test가 시작을 거부해야 한다. Exact name, import와 signature는 predecessor owner가 승인할 때까지 `PROPOSED/OPEN`이며, 승인된 signature가 다르면 이 후보를 폐기하고 owner contract 하나만 compile dependency로 사용한다.

### 9.1 Storage backend 후보

```java
package com.ronext.rpdptw.adapter.object.s3;

import java.time.Duration;
import java.util.Objects;
import software.amazon.awssdk.services.s3.S3Client;
// ObjectStorageBackend와 logical value/result type은 accepted Phase 09 module에서 import.

// PROPOSED INTERNAL compile-shape — adapters/object-s3 only.
final class S3ObjectStorageBackend implements ObjectStorageBackend {
    private final S3Client client;
    private final S3BackendConfig config;

    S3ObjectStorageBackend(S3Client client, S3BackendConfig config) {
        this.client = Objects.requireNonNull(client);
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public BackendCapabilities capabilities() {
        throw contractSkeleton();
    }

    @Override
    public BackendGetResult getExact(ObjectKey key) {
        throw contractSkeleton();
    }

    @Override
    public BackendPutResult putIfAbsent(
            ObjectKey key,
            ObjectWriteSource source,
            ExpectedObjectIntegrity expected) {
        throw contractSkeleton();
    }

    @Override
    public BackendCasResult compareAndSet(
            ObjectKey key,
            ObjectVersionToken expectedVersion,
            ObjectWriteSource replacement,
            ExpectedObjectIntegrity expectedReplacement) {
        throw contractSkeleton();
    }

    @Override
    public BackendMetadataResult metadataExact(ObjectKey key) {
        throw contractSkeleton();
    }

    private static UnsupportedOperationException contractSkeleton() {
        return new UnsupportedOperationException("PROPOSED contract skeleton only");
    }
}

record S3BackendConfig(
        Region region,
        String bucketName,
        String rootPrefix,
        EncryptionMode encryptionMode,
        KmsKeyArnRef kmsKey,
        TenantIsolationMode tenantIsolationMode,
        TenantCredentialProviderRef tenantCredentialProvider,
        boolean versioningRequired,
        MultipartMode multipartMode,
        CopyMode copyMode,
        ReplayableBodyPolicy replayableBodyPolicy,
        ChecksumPolicy checksumPolicy,
        RetryPolicyId sdkRetryPolicy,
        Duration operationDeadline) {}
```

`S3Client`는 `adapters/object-s3`의 compile dependency이고 나머지 logical port/value/result는 accepted Phase 09 provider-neutral dependency다. `S3BackendConfig`의 versioning/multipart/copy/body-replay/credential 값은 숨은 SDK default가 아니라 reviewed ADR identity여야 한다. Unsupported 조합은 constructor/config validation에서 fail-closed하고 다른 operation으로 fallback하지 않는다. 핵심은 signature보다 projection이다.

```text
putIfAbsent:
  new key                      → CREATED
  same key + same digest       → CONVERGED
  same key + different digest  → INTEGRITY_CONFLICT

compareAndSet:
  exact current token          → new opaque token
  stale token                  → STATE_CONFLICT; reload/revalidate
  access denied                → DENIED; never NOT_FOUND
```

Run-state token과 publication precondition의 exact type은 Phase 09/10 공동 승인 전 `OPEN BLOCKER`다. Adapter overload나 `Object` bridge로 봉합하지 않는다.

#### 9.1.1 S3 conditional operation, versioning과 multipart 판정

S3 response를 application meaning으로 직접 승격하지 않는다. Phase 09가 logical owner, Phase 11 adapter가 provider observation mapper, Storage/Security/Ops ADR가 versioning·multipart·credential policy owner다.

| Logical operation / owner state | Provider observation | Required mapper와 retry | Failure/rollback | Independent semantic oracle |
|---|---|---|---|---|
| Immutable create; owner state `ABSENT → IMMUTABLE_VERIFIED` | Conditional create success | Exact key를 read-back하고 protected metadata/length/checksum/digest/schema가 모두 같을 때만 `CREATED` | Read-back indeterminate/corrupt면 pointer commit 금지, bytes overwrite 금지, quarantine/incident | Original expected bytes + exact verified read |
| Immutable create; current object exists | `412 Precondition Failed` | Exact read 후 same bytes/protected metadata면 `CONVERGED`, 다르면 `INTEGRITY_CONFLICT`; 412 자체는 same/different를 결정하지 않음 | Original current version/bytes 보존 | Same-key same/different fixture |
| Immutable create; concurrent operation | `409 ConditionalRequestConflict` 또는 response unknown | Exact current read/revalidate가 우선. Approved retry가 있고 source가 처음부터 replayable할 때만 same logical operation/idempotency로 새 transport attempt | Blind SDK retry, non-replayable stream 재사용, 다른 key fallback 금지 | Two-writer race + response-loss oracle |
| State CAS; owner state exact current token | Conditional replacement success | Exact current pointer/body 재독해 후 desired ref와 protected metadata equality, 그 뒤 새 opaque token 반환 | Mismatch/unknown이면 success 추론 금지; prior accepted pointer가 last safe | Exactly-one winner + final token/content |
| State CAS; stale/concurrent | `412`, `409` 또는 response unknown | Exact current read. Desired ref면 `ALREADY_AT_DESIRED`, 다른 ref/version이면 `STALE_VERSION`, 판단 불가면 `VISIBILITY_INDETERMINATE`; retry 전 business precondition 재승인 | Last-write-wins/expected token 재사용 금지 | Same/different desired CAS race |
| Publication CAS; distinct pointer precondition | Conditional create/update success/conflict | Run-state authorization fence와 pointer precondition을 별도 exact read. Publishable closure와 desired digest가 모두 같을 때만 converge | Publication object 존재만으로 `SUCCEEDED` 금지; pointer blind rollback/삭제 금지 | Both-gate closure + cancel/publish same-state race |
| Any operation | Access/KMS/policy deny, timeout, transport failure | `DENIED`, `TRANSIENT_UNAVAILABLE` 또는 `VISIBILITY_INDETERMINATE`로 lossless mapping; `NOT_FOUND`로 collapse 금지 | New state/pointer side effect 중단, evidence 보존 | Role-action matrix + injected timeout/deny |

Versioning과 object lifecycle 규칙:

- `versioningRequired`는 explicit reviewed config다. Enabled/suspended/disabled를 SDK나 account default에서 추측하지 않는다.
- Versioned bucket의 current version과 delete marker는 provider observation이다. Conditional create가 current delete marker 때문에 가능하더라도 logical `ABSENT`로 인정할지는 Phase 09 lifecycle ADR와 exact read oracle이 결정한다. Version ID/ETag/delete marker를 domain/result fingerprint에 넣지 않는다.
- Rollback은 이전 accepted immutable ref로 authorized pointer CAS를 수행하는 것이며 object version delete, delete-marker 삽입, “latest version 선택” 또는 bucket rollback이 아니다.
- Multipart upload는 threshold, checksum, part retry, completion precondition, response-loss reconciliation, orphan abort/retention owner가 모두 승인된 경우만 사용한다. `409` 뒤 `CompleteMultipartUpload`나 stream을 그대로 재사용하지 않는다. Exact upload/read identity를 다시 만들 수 없으면 fail-closed한다.
- `CopyObject`는 source/destination precondition과 bucket-policy condition 의미가 일반 `PutObject`와 같다고 가정하지 않는다. Explicit ADR, policy test와 original-byte oracle 전에는 migration/rollback shortcut으로 금지한다.
- Incomplete multipart cleanup은 staging/orphan만 대상으로 하며 current immutable artifact/state/publication version을 지우지 않는다. Cleanup evidence에는 upload identity, age/hold/quarantine와 owner가 필요하다.
- `tenantCredentialProvider`는 요청 tenant와 approved short-lived scoped authority를 결합한다. Missing/mismatch이면 S3 lookup 전 fail하고 shared broad role, ambient default credential chain 또는 application string check로 fallback하지 않는다.

이 표는 S3를 provider-neutral 의미의 공식 기본값으로 만드는 표가 아니다. S3는 actual observation을 내는 subject이며 expected state/result는 Phase 08/09/10 local/model oracle이 소유한다. Exact versioning mode, multipart threshold, retry 횟수와 key encoding은 계속 `OPEN/ADR_REQUIRED`다.

### 9.2 Workflow/compute config 후보

```java
enum StepFunctionsWorkflowType {
    STANDARD,
    EXPRESS
}

enum LambdaIntegrationMode {
    SYNCHRONOUS_REQUEST_RESPONSE,
    ASYNCHRONOUS_EVENT,
    CALLBACK_TASK_TOKEN
}

record AwsWorkflowRuntimeConfig(
        StepFunctionsWorkflowType workflowType,
        LambdaIntegrationMode coordinatorInvocationMode,
        LambdaIntegrationMode workerInvocationMode,
        RetryPolicyId taskRetryPolicy,
        DeadlinePolicyId taskDeadlinePolicy,
        FailureReconciliationPolicyId failureReconciliationPolicy) {}
```

모든 필드는 non-null/non-default다. 어떤 조합을 채택할지는 `OPEN/ADR_REQUIRED`다. Unsupported 조합을 다른 mode로 fallback하지 않는다.

### 9.3 Workflow와 worker port 후보

```java
package com.ronext.rpdptw.adapter.workflow.aws;

import java.util.Objects;
import software.amazon.awssdk.services.sfn.SfnClient;
// WorkflowExecutionPort와 action/value/result type은 accepted Phase 08/10 module에서 import.

// Phase 08 owns the accepted port; AWS module implements it.
final class StepFunctionsWorkflowExecutionAdapter
        implements WorkflowExecutionPort {
    private final SfnClient client;

    StepFunctionsWorkflowExecutionAdapter(SfnClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public WorkflowStartReceipt start(LogicalWorkflowRequest request) {
        throw contractSkeleton();
    }

    @Override
    public WorkflowExecutionStatus getStatus(LogicalWorkflowRunId runId) {
        throw contractSkeleton();
    }

    @Override
    public WorkflowCancelReceipt requestCancel(
            LogicalWorkflowRunId runId,
            CancellationId cancellationId) {
        throw contractSkeleton();
    }

    private static UnsupportedOperationException contractSkeleton() {
        return new UnsupportedOperationException("PROPOSED contract skeleton only");
    }
}

// Phase 10 action executor, not a second coordinator.
final class StepFunctionsCoordinatorActionExecutor {
    private final SfnClient client;

    StepFunctionsCoordinatorActionExecutor(SfnClient client) {
        this.client = Objects.requireNonNull(client);
    }

    ActionExecutionReceipt execute(CoordinatorAction action) {
        throw new UnsupportedOperationException("PROPOSED contract skeleton only");
    }
}
```

```java
package com.ronext.rpdptw.adapter.compute.aws.lambda;

import java.util.Objects;
import software.amazon.awssdk.services.lambda.LambdaClient;
// WorkerDispatcher와 logical value/result type은 accepted Phase 08/10 module에서 import.

final class LambdaWorkerDispatcher implements WorkerDispatcher {
    private final LambdaClient client;

    LambdaWorkerDispatcher(LambdaClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public DispatchReceipt dispatch(WorkerAssignment assignment) {
        throw contractSkeleton();
    }

    @Override
    public WorkerExecutionStatus getStatus(WorkerRunId workerRunId) {
        throw contractSkeleton();
    }

    @Override
    public StopReceipt requestStop(
            WorkerRunId workerRunId,
            CancellationId cancellationId) {
        throw contractSkeleton();
    }

    private static UnsupportedOperationException contractSkeleton() {
        return new UnsupportedOperationException("PROPOSED contract skeleton only");
    }
}
```

`SfnClient`는 workflow adapter에만, `LambdaClient`는 compute adapter에만 존재한다. `rpdptw-application` 또는 port contract가 두 SDK type을 import하면 architecture test가 실패해야 한다. Accepted Phase 08~10 signature가 다르면 하나를 공동 승인한다. 임시 overload, raw Map, reflection adapter로 여러 draft를 동시에 지원하지 않는다.

### 9.4 Versioned event 후보

```java
sealed interface AwsComputeEventV1
        permits AwsPhaseOneScreenEventV1, AwsWorkerEventV1 {}

record AwsCommandEnvelopeV1(
        String eventVersion,
        String tenantHandle,
        String solveId,
        String commandRef,
        String manifestFingerprint,
        String correlationId) {}

record AwsPhaseOneScreenEventV1(
        String eventVersion,
        String assignmentRef,
        String phaseOneScreenRunId,
        String attemptId,
        String correlationId) implements AwsComputeEventV1 {}

record AwsWorkerEventV1(
        String eventVersion,
        String assignmentRef,
        String workerRunId,
        String attemptId,
        String correlationId) implements AwsComputeEventV1 {}

record PlatformDeadlineObservation(
        long remainingMillis,
        long configuredSafetyMarginMillis,
        String runtimeInvocationId) {}
```

Event에는 raw problem/result, credential, ARN, opaque run-state/publication token을 넣지 않는다. 큰 payload는 verified `ArtifactRef`로 전달한다.

### 9.5 Action authorization과 publication 후보

```text
PROPOSED/OPEN contract:

authorizeSideEffect(action):
  current = runStateRepository.readExact(action.solveId)
  require current.pendingAction.id == action.actionId
  require current.pendingAction.payloadFingerprint == action.payloadFingerprint
  require current.authorizingTransitionOrdinal == action.transitionOrdinal
  # opaque repository version은 semantic action payload가 아님

publish(resultRef):
  require resultRef is PublishableResultRef
  require run state has authoritative PUBLISHING fence
  publication = resultPublisher.readExactPublication(solveId)
  precondition = publication.distinctPrecondition
  compareAndSetPublication(precondition, resultRef)
```

Exact public port/type은 cross-Phase approval 전 확정하지 않는다. 승인 전 마지막 안전 지점은 immutable `PublishableResultRef`를 보존하되 AWS publication을 시작하지 않는 상태다.

### 9.6 State transition

```text
SUBMITTED
→ PREPARING
→ PREPARED
→ ROUND_DISPATCHING
→ ROUND_RUNNING
→ ROUND_VERIFYING
→ ROUND_AGGREGATING
→ next ROUND_DISPATCHING | FINALIZING
→ PUBLISHING
→ SUCCEEDED
```

Exceptional:

```text
REJECTED_INPUT | BINDING_FAILED | PLATFORM_TIMEOUT | RESOURCE_LIMIT
| CANCEL_REQUESTED → CANCELLED
| INCOMPLETE | PUBLICATION_REJECTED | FAILED
```

Step Functions state는 이 state machine의 authority가 아니다. ASL은 `CoordinatorAction.type`만 보고 invoke/wait/wakeup/fail/succeed를 매핑한다.

### 9.7 Handler pseudocode

```text
onWorkerEvent(event):
  validate version, size, identity, auth context
  assignment = artifactStore.readVerified(event.assignmentRef)
  require assignment.workerRunId == event.workerRunId
  observe platform deadline without changing algorithm budget
  outcome = executeWorkerRun(assignment)
  candidateRef = putIfAbsent(exact candidate key, outcome.candidate)
  reportRef = putIfAbsent(exact verifier key, outcome.report)
  completeWorkerRun(workerRunId, attemptId, candidateRef, reportRef)
  same digest duplicate → converge
  different digest duplicate → integrity failure
```

```text
onProviderFailure(observation):
  write immutable ProviderFailureObservation
  state = read exact application state
  if approved retry policy permits:
    same WorkerRunId + same assignment/seed/warm start/work
    new AttemptId only
  else:
    typed exceptional/incomplete state
  never publish partial success
```

## 10. Ordered work packages

각 WP는 앞 WP의 output을 입력으로 사용한다. Entry gate 전에는 red test와 offline draft까지만 허용된다.

### WP11-0 — Entry receipt와 drift freeze

| 항목 | 지시 |
|---|---|
| 목적/이유 | 구현 전에 accepted predecessor와 한 시점 repository/ADR/environment authority를 고정해 moving target과 fake entry를 막는다. |
| 사전조건 | Canonical Phase 11/review를 읽고 status가 blocked임을 이해함 |
| 예상 file/type | `Phase11EntryReceipt` schema, ADR register, inventory receipt; 구현 위치는 owner 승인 전 proposed |
| 구체 행동 | Phase 00/06~10 evidence/review/rollback ref 확인, stable section semantic diff, account alias/region/role/budget/cleanup owner 확인 |
| 근거 | Phase 11 §4, review blocker ledger, Master Plan evidence DAG |
| 금지 shortcut | Mock evidence, empty ref, neighbor whole-file digest monitoring, Console screenshot |
| 검증 | Receipt의 모든 ref가 non-empty·digest-bound·accepted인지 검사; `Q-INFRA-01`, `Q-BENCH-02`, `Q-VAR-01` exact 상태 검사 |
| 기대 결과 | 모두 있으면 named owner가 entry 승인; 하나라도 없으면 `BLOCKED` |
| 실패 해석 | 구현 난이도가 아니라 authority/evidence 부재 |
| Rollback | 문서/fixture/red test only 상태로 복귀; side effect 0 |
| Handoff | Signed/immutable entry receipt → WP11-1 |

자문 checkpoint: “내가 확인한 것은 문서 존재인가, accepted implementation receipt인가?”

### WP11-1 — Module/DAG, event/config와 IaC contract

| 항목 | 지시 |
|---|---|
| 목적/이유 | AWS SDK/event/resource가 stable 의미에 침투하기 전에 물리 경계를 red test로 고정한다. |
| 사전조건 | WP11-0, accepted Phase 00 architecture |
| 예상 file/package/type | Three adapter POM, apps, `distributions/aws-serverless`, `build/port-contract-tests`, versioned event records, `AwsWorkflowRuntimeConfig`, `deployment/aws` source |
| 구체 행동 | Provider dependency를 adapter에만 추가, explicit workflow/invocation mode와 config validation, ASL action-only rule, no-database template rule 작성. Parent/module POM에 pinned Failsafe `integration-test`+`verify`, `*IT` include, JUnit/approved fixture dependency, `aws-local-it`/`aws-integration` profile와 report auditor를 §8.2.1대로 구성 |
| 근거 | Architecture §2/§5, Integrated §3/§15, Phase 11 §5~6 |
| 금지 shortcut | Root POM AWS SDK, 하나의 `adapters/aws`, service default, generated `.serverless` 복사 |
| 검증 명령 | Future reactor에서 architecture/event/config/template selected tests; effective-POM/active-profile machine check; `sam validate --lint`, `cfn-lint`, ASL schema validation |
| 기대 결과 | Stable module AWS reference 0, omitted config fail, database resource 0, semantic ASL branch 0; missing Failsafe/profile/JUnit/fixture/0-test가 모두 non-zero |
| 실패 해석 | Dependency/ownership/config contract가 아직 불완전 |
| Rollback | AWS module/IaC draft를 merge/deploy하지 않고 accepted reactor/local 유지 |
| Handoff | Versioned event/config/IaC contract → WP11-2/3/4 |

### WP11-2 — S3 backend와 Phase 09 conformance

| 항목 | 지시 |
|---|---|
| 목적/이유 | S3의 conditional API를 Phase 09의 immutable/exact/CAS 의미에 손실 없이 매핑한다. |
| 사전조건 | Accepted Phase 09 contract/encoding/key-layout와 WP11-1 |
| 예상 file/package/type | `adapter.object.s3`, `S3ObjectStorageBackend`, key layout, error mapper, S3 contract/AwsIT |
| 구체 행동 | Exact key, canonical segment, streaming digest, put-if-absent, state CAS, distinct publication CAS, KMS/checksum/error mapping을 §9.1.1 outcome table로 구현. `tenantCredentialProviderRef`, versioning/delete-marker, multipart/copy/body replay policy를 explicit ADR input으로 검증하고 unsupported 조합은 startup fail. §11.3.1의 supplementary actual-S3 29-case catalog를 stable report identity로 전부 실행하고, 각 unique fault activation이 같은 mapped case에서 `RED`/non-zero가 된 receipt를 normal `PASS`와 함께 봉인 |
| 근거 | Phase 09 handoff, Phase 11 §6.1/§7/§8.2 |
| 금지 shortcut | List-then-decide, check-then-put, process lock, last-write-wins, ETag identity, unconditional policy |
| 검증 | Unit → shared contract → emulator mapping → actual S3 412/409/concurrency/version/delete-marker/multipart-policy/credential/KMS 순서. Same-run auditor가 canonical 64와 supplementary 29를 별도 expected set으로 판정하고 supplementary activation↔red 29개 bijection, survived/wrong mapping 0을 검사 |
| 기대 결과 | Exactly one CAS winner, same digest converge, different digest/corruption/denied typed failure, original bytes/current accepted pointer unchanged, orphan multipart만 bounded cleanup |
| 실패 해석 | Emulator와 actual S3 semantics, body replay/versioning 또는 provider-native policy/credential enforcement gap |
| Rollback | S3 adapter/distribution off; immutable objects quarantine; previous accepted pointer 유지; object version/delete-marker/state/pointer overwrite·삭제 금지 |
| Handoff | `E-P11-AWS-CONTRACT.storage` candidate → WP11-5/7 |

### WP11-3 — Lambda handlers와 worker dispatch

| 항목 | 지시 |
|---|---|
| 목적/이유 | AWS event/lifecycle을 provider-neutral command와 exact retry identity로 축소한다. |
| 사전조건 | Accepted Phase 08/10 ports, WP11-1, verified S3 read |
| 예상 file/package/type | `adapter.compute.aws.lambda`, API/coordinator/worker handlers, dispatcher, event/deadline/error mapper |
| 구체 행동 | Version/size/auth validation, assignment exact read, Context 격리, duplicate/retry/cancel, platform failure mapping |
| 근거 | Phase 11 §6.2/§6.4/§9 |
| 금지 shortcut | Raw DTO → solver, Lambda Context 전달, timeout→normal termination, retry 때 seed/work 변경 |
| 검증 | Handler contract, unknown version/oversize/identity mismatch, duplicate/different digest, start failure, timeout/OOM/throttle, cancel |
| 기대 결과 | Provider type 누출 0, same logical retry, partial/timeout false success 0 |
| 실패 해석 | Event mapping 또는 logical/platform lifecycle 혼합 |
| Rollback | Candidate Lambda alias off/previous accepted revision; exact state reconciliation |
| Handoff | Compute/event evidence → WP11-4/7 |

Deadline ADR가 없으면 deadline-enabled handler path는 계속 disabled다.

### WP11-4 — Thin Step Functions mapping

| 항목 | 지시 |
|---|---|
| 목적/이유 | Durable orchestration을 얻되 Phase 10 coordinator 의미를 ASL에 복제하지 않는다. |
| 사전조건 | Accepted Phase 10 action oracle, approved type/mode ADR, WP11-1/3 |
| 예상 file/package/type | `solve.asl.json`, workflow adapter, action executor, failure reconciliation |
| 구체 행동 | Action-only `Choice`, wait/wakeup, bounded retry, exact current pending action authorization, same-state cancel/publish fence |
| 근거 | Phase 10 §7~8/§14.2, Phase 11 §6.3 |
| 금지 shortcut | Score/worker count/verifier/customer JSONPath, history-based success, cancel-intent key를 fence로 사용 |
| 검증 | ASL structural contract, Phase 10 action trace parity, duplicate/lost/out-of-order wakeup, crash replay, cancel/publish race |
| 기대 결과 | Same logical action/state/terminal trace; ASL semantic branch 0 |
| 실패 해석 | Provider workflow가 second coordinator가 되었음 |
| Rollback | New starts를 previous state-machine revision으로 route; in-flight exact reconcile/drain/cancel |
| Handoff | Workflow evidence → WP11-7 |

### WP11-5 — IAM, KMS, tenant와 network security

| 항목 | 지시 |
|---|---|
| 목적/이유 | Application string check가 아니라 provider-native least privilege와 deny로 tenant/data 경계를 방어한다. |
| 사전조건 | WP11-1~4 resource/action inventory, Security owner |
| 예상 file/type | Role/bucket/KMS policies, network ADR, negative matrix, redaction corpus |
| 구체 행동 | API/coordinator/worker/workflow/deploy/test role 분리, conditional-header deny, cross-tenant/KMS deny, public ingress/egress 제한 |
| 근거 | Phase 11 §8, review `F-P11-002` |
| 금지 shortcut | Shared `tenants/*` role, `s3:*`/`kms:*` on `*`, encryption=digest, app-only conditional write |
| 검증 | Static policy lint + actual assume-role positive/negative + log/trace sensitive scan |
| 기대 결과 | Required positive operation만 성공하고 모든 forbidden matrix가 provider-native deny |
| 실패 해석 | Policy resource/action/context 또는 runtime credential scope가 과도함 |
| Rollback | Exposure/new deploy 차단, previous policy/alias; key/object 삭제 금지 |
| Handoff | `E-P11-SECURITY` candidate → WP11-6/7 |

### WP11-6 — Quota, observability, cost와 operational readiness

| 항목 | 지시 |
|---|---|
| 목적/이유 | 의미는 같아도 실제 workload가 quota/deadline/비용/관측 실패로 운영 불가능한 상태를 분리한다. |
| 사전조건 | WP11-3~5 integration stack, Ops/FinOps/Security owner |
| 예상 file/type | Quota preflight, versioned required log/metric/alarm/cost/forbidden schemas, alarm/dashboard-as-code, usage collector, retention/incident runbook |
| 구체 행동 | §11.6.1 exact schema에서 explicit stage limits, logs/metrics/alarms, trace correlation, forbidden dimensions/fields, units/freshness와 failure 포함 cost vector를 구현 |
| 근거 | Phase 11 §9~10 |
| 금지 shortcut | Hidden default, worker 축소, payload truncate, sampled integrity alarm, “저비용” 주장 |
| 검증 | Quota rejection, throttle/timeout/DLQ/exporter/alarm fault injection, expected↔discovered field 차집합, forbidden/cardinality/unit/freshness/redaction, manifest별 usage completeness |
| 기대 결과 | Required log/metric/alarm/cost 집합 missing/duplicate/forbidden/unit/freshness violation 0; cap 미승인 시 production gate false |
| 실패 해석 | 관측/운영 evidence 부재이며 semantic success로 보완 불가 |
| Rollback | New workload/deploy 중단, previous config/alarm; 관측 불능 run은 parity evidence 제외 |
| Handoff | `E-P11-OPERATIONS` candidate → WP11-7/8 |

### WP11-7 — Actual AWS parity, fault와 reproducibility

| 항목 | 지시 |
|---|---|
| 목적/이유 | Emulator가 증명하지 못하는 S3/Lambda/Step Functions/IAM/KMS 의미와 local semantic parity를 실제 provider에서 검증한다. |
| 사전조건 | WP11-2~6, approved isolated environment, accepted local oracle |
| 예상 test | `AwsReferenceParityIT`, `AwsReferenceFaultIT`, `AwsReproducibilityIT`, evidence collector |
| 구체 행동 | Same manifest local/AWS, S3 race/corruption, duplicate/lost wakeup, timeout/cancel, KMS deny, missing worker 주입 |
| 근거 | Phase 11 §12.4, exit G11-B~G |
| 금지 shortcut | AWS output golden-update, emulator를 AwsIT로 이름 변경, failed run 누락, provider metadata 비교 |
| 검증 | Canonical artifact/result/termination/state-action projection exact 비교. §11.3.1 actual-S3 report/sensitivity receipt와 canonical 64 report를 같은 run identity 및 서로 다른 manifest digest로 audit하고, 29 unique fault activation과 mapped `RED`/non-zero disposition의 bijection 및 survived/wrong mapping 0을 확인 |
| 기대 결과 | Normal semantic parity; fault typed terminal + publication absent; repeated semantic trace/digest equal |
| 실패 해석 | Mapping/serialization/provider semantic gap; AWS를 새로운 oracle로 승격하지 않음 |
| Rollback | AWS candidate off, accepted local reference 유지, failure evidence 보존 |
| Handoff | `E-P11-PARITY` candidate → WP11-8 |

### WP11-8 — Deployment/rollback rehearsal와 evidence seal

| 항목 | 지시 |
|---|---|
| 목적/이유 | Deploy 성공과 recoverability/evidence/acceptance를 분리하고 immutable provenance DAG를 완성한다. |
| 사전조건 | WP11-7 pass, rollback owner, independent reviewer |
| 예상 artifact | `AwsDeploymentManifest`, `Phase11EvidenceManifest`, review report, `Phase11PostReviewAcceptanceReceipt` |
| 구체 행동 | Integration deploy → smoke/parity/fault → previous/local rollback → exact reconcile → evidence seal → independent review |
| 근거 | Phase 11 §13~15, Master Plan §9 |
| 금지 shortcut | Evidence에 review/receipt 역참조, review 뒤 manifest backfill, empty change set=new deploy, production apply |
| 검증 | Digest/schema/content, §11.3 canonical 64 digest와 §11.3.1 supplementary S3 29 digest, 각 fresh report set oracle와 29 activation↔red receipt bijection, sensitivity survived/wrong mapping 0, dependency cycle, rollback unresolved state, accepted verdict |
| 기대 결과 | Standalone pre-review evidence M → review R → receipt(M,R); `productionAuthority=false` |
| 실패 해석 | Review `CHANGES_REQUIRED` 또는 rehearsal gap이면 acceptance receipt 없음 |
| Rollback | Previous accepted AWS revision 또는 AWS new starts off + accepted local 유지 |
| Handoff | Evidence + post-review receipt → Phase 12/14B entry review |

### 10.1 WP별 future 검증 command map

아래 selector와 profile은 **target POM/test가 생긴 뒤 Phase 00/build owner가 승인해야 할 proposed command**다. Correction inventory에서는 module/test/profile/Failsafe가 없으므로 지금 실행해 green을 주장하지 않는다. 표의 selected run은 같은 source를 isolated local repository에 먼저 full `clean install`한 뒤 **`-am` 없이** 실행한다. 별도로 모든 target module과 report-audit module을 포함한 filter 없는 `-am clean verify`가 필수다.

| WP | Proposed future command/test | 추가 pass 판정 |
|---|---|---|
| 11-0 | `./mvnw -pl build/port-contract-tests -Dtest=Phase11EntryReceiptContractTest,Phase11RequiredTestManifestContractTest,Phase11S3ActualCaseCatalogContractTest -Dsurefire.failIfNoSpecifiedTests=true clean test` | Missing/unaccepted ref, canonical/supplementary manifest hash/count drift, 29 unique case↔fault↔report mapping 위반 또는 receipt schema의 literal `expectedSensitivityDisposition=RED` 부재/변경이면 red |
| 11-1 | `./mvnw -pl build/architecture-rules -Dtest=AwsSdkLeakageArchitectureTest -Dsurefire.failIfNoSpecifiedTests=true clean test` + effective-POM/profile checker + `sam validate --template-file deployment/aws/template.yaml --lint` | Leakage/default/database/semantic-ASL 0; Failsafe/profile/JUnit/fixture wiring exact |
| 11-2 | `./mvnw -pl adapters/object-s3 -Paws-integration -Dit.test=S3ObjectStorageBackendAwsIT -Dfailsafe.failIfNoSpecifiedTests=true -DskipITs=false -DskipTests=false clean verify` | §11.3.1 actual-S3 expected/discovered/passed `29/29/29`, `|activatedSensitivity|=|observedRed|=29`; missing/duplicate/failure/error/skipped/stale/sensitivity-missing/sensitivity-survived/wrong-sensitivity-mapping 0 |
| 11-3 | `./mvnw -pl adapters/compute-aws-lambda -Dtest=AwsEventEnvelopeContractTest,LambdaDeadlineMapperTest -Dsurefire.failIfNoSpecifiedTests=true clean test` | Unknown/oversize/timeout/retry false success 0 |
| 11-4 | `./mvnw -pl adapters/workflow-aws-stepfunctions -Paws-integration -Dit.test=StepFunctionsLambdaAwsIT,AwsActionReplayIT,AwsCancellationIT -Dfailsafe.failIfNoSpecifiedTests=true -DskipITs=false -DskipTests=false clean verify` | Phase 10 trace parity와 same-state fence pass |
| 11-5 | `./mvnw -pl distributions/aws-serverless -Paws-integration -Dit.test=AwsIamBoundaryAwsIT,AwsNetworkBoundaryAwsIT,AwsLogRedactionIT -Dfailsafe.failIfNoSpecifiedTests=true -DskipITs=false -DskipTests=false clean verify` | Positive allow와 모든 negative deny exact |
| 11-6 | `./mvnw -pl distributions/aws-serverless -Paws-integration -Dit.test=AwsObservabilityAwsIT,AwsCostEvidenceIT -Dfailsafe.failIfNoSpecifiedTests=true -DskipITs=false -DskipTests=false clean verify` | Schema missing/duplicate/forbidden/unit/freshness 0; 미승인 cap 명시 |
| 11-7 | `./mvnw -pl distributions/aws-serverless -Paws-integration -Dit.test=AwsReferenceParityIT,AwsReproducibilityIT -Dfailsafe.failIfNoSpecifiedTests=true -DskipITs=false -DskipTests=false clean verify` | Semantic mismatch/누락 run/skip 0; fault cases는 §11.3 manifest의 개별 classes로 집계 |
| 11-8 | `./mvnw -pl distributions/aws-serverless -Paws-integration -Dit.test=AwsDeploymentRollbackIT -Dfailsafe.failIfNoSpecifiedTests=true -DskipITs=false -DskipTests=false clean verify` | In-flight unresolved 0, required manifest/report audit와 M→R→receipt DAG valid |

`-Dit.test`/Failsafe naming과 exact module selector는 target POM에서 이 계약을 구현할 때 확정한다. 다른 이름을 승인하더라도 §11.3 canonical mapping, expected/discovered/passed reconciliation, missing/duplicate/hash/sensitivity와 0-test/stale-report fail-closed 의미는 바꾸지 않는다.

## 11. Test-first 전략, fixture, oracle와 false-green 방지

### 11.1 Fixture와 builder

| Fixture/builder | 최소 내용 | Independent oracle |
|---|---|---|
| `small-rpdptw-manifest-v1.json` | Small pair/time-window, declared workers, exact fingerprints | Phase 10 fake/local action trace + Phase 07 publishable digest |
| `worker-duplicate-same.json` | Same worker/run/assignment/digest | Converge once |
| `worker-duplicate-different.json` | Same logical ID, different verified digest | Integrity failure |
| `cas-race-v1.json` | Same expected state version, two replacements | Exactly one winner |
| `cancel-publish-race-v1.json` | Same run-state version, two terminal intents | Exactly one authoritative intent |
| `missing-worker-v1.json` | Declared worker 한 개 outcome 없음 | `INCOMPLETE`, pointer absent |
| `platform-timeout-v1.json` | Incomplete algorithm step + Lambda timeout | `PLATFORM_TIMEOUT`, not max-step |
| `corrupt-artifact-*.bin/json` | Length/schema/digest/checksum 조합 손상 | Bytes 노출/deserialize 전 reject |
| `role-action-matrix-v1.json` | Role × action × tenant × prefix × KMS context | Explicit allow/deny table |
| `sensitive-fields-v1.json` | Secret/address/PII/token/full body marker | Log/trace match 0 |
| `required-metrics-v1.json` | Required name/tag/cardinality | Exact bounded schema |

Builder는 semantic identity와 provider observation을 별도 필드로 만든다. Random UUID, current time, unordered map, environment default를 expected fixture 생성에 쓰지 않는다.

### 11.2 Oracle 우선순위

```text
hand-computed/model oracle
→ accepted provider-neutral fake
→ accepted local reference
→ actual AWS observation
```

AWS output을 expected file로 golden-update하지 않는다. Local과 AWS가 함께 틀릴 수 있으므로 corruption/fault expected result는 hand/model oracle이 소유한다.

<a id="113-exact-test-classmethod-후보"></a>
> **Legacy fragment compatibility only:** `113-exact-test-classmethod-후보`의 “후보”는 현재 normative status가 아니다. 아래 §11.3의 canonical required manifest와 report oracle이 현재 규범이다.

### 11.3 Canonical required test manifest와 report oracle

Acceptance required set은 [Canonical Phase 11 §12.2~§12.4](../../phases/phase-11-aws-reference-distribution.md#122-unitarchitecturecontract-tests)의 source blob `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8`에서 추출한 **64 unique case / 27 class**다. 이전 36개 후보 표는 교육용 subset이었고 acceptance manifest가 아니다. 아래 block은 UTF-8/LF, source order, `Class#method()` 한 줄, final newline을 포함한 `phase11-required-tests-v1` exact bytes다. Future `build/port-contract-tests/src/test/resources/phase11/required-tests-v1.txt`는 이 block과 byte-exact해야 한다.

```text
manifestVersion=phase11-required-tests-v1
sourcePath=docs/implementation/phases/phase-11-aws-reference-distribution.md
sourceBlob=14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8
sourceSections=12.2-12.4
caseCount=64
classCount=27
contentSha256=98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1
owner=Phase 11 independent provider-integration test owner
```

<!-- phase11-required-tests-v1:begin -->
```text
AwsSdkLeakageArchitectureTest#stableModulesHaveZeroAwsSdkReferences()
AwsSdkLeakageArchitectureTest#applicationContractsExposeNoArnBucketKeyOrLambdaTypes()
S3ObjectKeyLayoutTest#encodesTenantAndArtifactSegmentsCanonically()
S3ObjectKeyLayoutTest#rejectsTraversalSlashControlUnicodeAmbiguityAndOverlength()
S3ErrorMapperTest#mapsPreconditionFailureToStateConflict()
S3ErrorMapperTest#neverMapsAccessDeniedOrChecksumMismatchToNotFound()
AwsEventEnvelopeContractTest#mapsV1ApiCoordinatorAndWorkerEventsWithoutSemanticDefaults()
AwsEventEnvelopeContractTest#rejectsUnknownVersionOversizeAndIdentityMismatch()
LambdaDeadlineMapperTest#platformRemainingTimeDoesNotBecomeAlgorithmTermination()
StepFunctionsActionMappingContractTest#mapsOnlyProviderNeutralActionTypes()
StepFunctionsActionMappingContractTest#mapsDeclaredPhaseOneAndPhaseTwoActionsWithoutEmbeddingSelection()
StepFunctionsActionMappingContractTest#definitionContainsNoCustomerObjectiveComparatorVerifierBranch()
StepFunctionsActionMappingContractTest#executesOnlyPendingActionCommittedUnderSameActionId()
StepFunctionsActionMappingContractTest#actionCarriesNoOpaqueRunStateOrPublicationVersionToken()
StepFunctionsActionMappingContractTest#authorizesExactCurrentPendingActionIdPayloadAndTransitionOrdinal()
AwsDeploymentConfigTest#requiresExplicitRegionEncryptionConcurrencyDeadlineQuotaAndRetention()
AwsDeploymentConfigTest#requiresExplicitWorkflowTypeAndLambdaIntegrationModesWithoutDefaults()
AwsDeploymentConfigTest#rejectsWorkflowAndInvocationCombinationWithoutReviewedDeliveryRetryContract()
AwsDeploymentConfigTest#rejectsSharedUnscopedTenantDataRole()
AwsServerlessTemplateContractTest#createsNoDatabaseResources()
AwsServerlessTemplateContractTest#separatesApiCoordinatorWorkerRoles()
AwsServerlessTemplateContractTest#requiresEncryptionVersioningLoggingAlarmsAndTags()
AwsServerlessTemplateContractTest#enforcesConditionalHeadersOnArtifactStateAndPublicationWrites()
S3ObjectStorageBackendContractIT#putIfAbsentCreatesExactKeyAndVerifiedDigest()
S3ObjectStorageBackendContractIT#putIfAbsentSameKeySameDigestConverges()
S3ObjectStorageBackendContractIT#putIfAbsentSameKeyDifferentDigestRejects()
S3ObjectStorageBackendContractIT#compareAndSetAllowsExactlyOneConcurrentWriter()
S3ObjectStorageBackendContractIT#compareAndSetRejectsStaleOpaqueVersion()
S3ObjectStorageBackendContractIT#readVerifiedRejectsChecksumDigestSchemaAndLengthMismatch()
S3ObjectStorageBackendContractIT#tenantPrefixCannotEscapeOrCrossRead()
S3ObjectStorageBackendContractIT#publicationPointerAcceptsOnlyPublishableResultRef()
AwsLocalEmulationIT#runsSubmitDispatchCompletePublishWithoutListingAuthority()
AwsLocalEmulationIT#duplicateWorkerEventConvergesAndDifferentDigestFails()
AwsServerlessTemplateContractTest#aslAndTemplateReferencesResolve()
AwsServerlessTemplateContractTest#allFunctionsHaveExplicitMemoryTimeoutConcurrencyAndLogRetentionParameters()
AwsServerlessTemplateContractTest#stateMachineAndLambdaIntegrationModesAreExplicitAndCompatible()
S3ObjectStorageBackendAwsIT#concurrentCasHasOneWinnerAndNoLostUpdate()
S3ObjectStorageBackendAwsIT#sseKmsAndDigestAreBothVerified()
S3ObjectStorageBackendAwsIT#bucketPolicyRejectsUnconditionalArtifactStateAndPublicationWrites()
StepFunctionsLambdaAwsIT#sameManifestCompletesAllDeclaredWorkersAndPublishesBothGateResult()
StepFunctionsLambdaAwsIT#selectedWorkflowAndInvocationModesMatchDeclaredDeliveryRetryAndReentrySemantics()
StepFunctionsLambdaAwsIT#missingDeclaredPhaseOneScreenCannotSelectWarmStartOrDispatchPhaseTwo()
StepFunctionsLambdaAwsIT#duplicateAndOutOfOrderWakeupsConverge()
StepFunctionsLambdaAwsIT#missingDeclaredWorkerEndsIncompleteWithoutPublication()
AwsActionReplayIT#crashAfterActionCasReplaysSameActionIdWithoutDuplicateLogicalWork()
AwsRetryIdentityIT#workerStartFailureKeepsWorkerRunSeedWarmStartAndStepsAndChangesAttemptOnly()
AwsCancellationIT#cancelIntentStopRequestAndActualTerminationRemainSeparate()
AwsCancellationIT#publicationAuthorizationFenceAllowsExactlyOneTerminalIntent()
AwsCancellationIT#cancelRequestedAndPublishingCompeteOnSameRunStateVersion()
AwsPublicationFenceAwsIT#runStateVersionCannotSubstituteForPublicationPrecondition()
AwsDeadlineRestartIT#crashRestartDoesNotRebaseWatchdogFromWallClockOrProviderRemainingTime()
AwsPlatformFailureIT#lambdaTimeoutThrottleAndOutOfMemoryAreNotNormalTermination()
AwsDlqReconciliationIT#poisonEventIsQuarantinedAndCannotCreateSuccess()
AwsIamBoundaryAwsIT#workerCannotReadOtherTenantOrWriteChampionOrStartWorkflow()
AwsIamBoundaryAwsIT#apiCannotReadUnpublishedResultOrWriteWorkerOutcome()
AwsIamBoundaryAwsIT#coordinatorCannotDecryptUnassignedTenantArtifact()
AwsIamBoundaryAwsIT#stateMachineRoleCannotReadS3OrDecryptArtifacts()
AwsNetworkBoundaryAwsIT#coordinatorAndWorkerHaveNoPublicIngressAndRejectUnapprovedEgress()
AwsLogRedactionIT#logsAndTracesContainCorrelationButNoSecretPiiOrPayload()
AwsObservabilityAwsIT#faultsEmitRequiredMetricsTraceLinksAndAlarms()
AwsCostEvidenceIT#recordsCompleteUsageVectorForOneManifest()
AwsReferenceParityIT#localAndAwsProduceSameCanonicalArtifactsResultAndTermination()
AwsReproducibilityIT#repeatedAwsRunWithSameEnvelopeHasSameSemanticTraceAndResult()
AwsDeploymentRollbackIT#previousRevisionCanResumeNewStartsAfterCandidateRollback()
```
<!-- phase11-required-tests-v1:end -->

Block 내부 64줄만 순서대로 연결한 SHA-256은 `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1`이다. Metadata block, fence와 comment marker는 hash 입력이 아니다. Case 이름을 바꾸거나 합치면 원본을 조용히 수정하지 말고 `required-test-cases-v1.json`에 `canonicalCaseId → implementationClass/method/reportCaseId` mapping을 둔다. Parameterized execution은 각 canonical case마다 stable `P11CASE:<canonicalCaseId>` report identity를 하나 생성해야 하며 하나의 invocation으로 여러 canonical case를 암묵 통과시키지 않는다.

`required-test-cases-v1.json`은 64 case 각각에 다음을 one-to-one으로 제공하고 content digest를 evidence에 봉인한다.

```text
canonicalCaseId
sourceSection/sourceBlob
category and owning module/profile
fixtureRef + fixtureDigest
independentOracleOwner + expectedSemanticOutcome
passCriteria
sensitivityFaultId + faultyDoubleOrProviderProbe
implementationClass/method/reportCaseId
```

Report oracle는 multiset으로 판정한다.

```text
E = required-tests-v1의 canonicalCaseId 64개
D = same-run Surefire/Failsafe XML + P11CASE receipt에서 발견한 canonicalCaseId
P = D 중 passed

missing          = E - D
duplicate        = canonical ID frequency in D != 1
renamedUnmapped  = report case는 있으나 approved mapping이 없음
unexpectedMapped = mapping/source manifest에 없는 canonical ID
failed/error/skipped = report disposition별 집합

PASS iff:
  manifestHash == 98fa3535...e0a1
  |E| == |D| == |P| == 64
  missing = duplicate = renamedUnmapped = unexpectedMapped = ∅
  failed = error = skipped = ∅
```

같은 run이라는 사실도 oracle 일부다. Reactor `clean` 시작 전에 random이 아닌 source/profile-bound `phase11RunId`와 start receipt를 만들고, 각 XML/P11CASE receipt가 그 run ID, source digest, manifest digest, effective-POM digest와 profile identity를 기록해야 한다. Report path가 expected module 밖이거나 mtime/embedded run ID가 start~end window에 없거나 이전 `target/`에서 왔으면 `STALE_REPORT`로 fail한다.

Sensitivity는 선택 사항이 아니다. 64개 case 각각의 `sensitivityFaultId`는 seeded faulty double, corrupt fixture, denied policy, isolated provider fault 또는 equivalent mutation을 켰을 때 해당 case를 red로 만들어야 한다. Production code와 oracle가 같은 mapping helper를 공유하거나 actual AWS output을 golden-update하지 않는다. Sensitivity run은 normal green bundle과 별도 receipt로 봉인하며 missing sensitivity case가 있으면 64/64 green이어도 acceptance fail이다.

#### 11.3.1 Supplementary actual-S3 required case catalog

Canonical 64-case manifest는 위 exact bytes와 digest를 그대로 보존한다. 그 manifest의
세 `S3ObjectStorageBackendAwsIT` method만으로 §9.1.1의 새 operation matrix를
통과했다고 추론하지 않는다. 다음은 별도의 **required**
`phase11-s3-actual-cases-v1` catalog다.

AWS 공식 [conditional-write 동작](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes.html)은
current version/delete marker, 409/412와 `PutObject` retry 대
`CompleteMultipartUpload` 전체 재시작의 차이를 설명하고,
[bucket-policy enforcement](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes-enforce.html)은
multipart creation operation 예외와 conditional-write-enforced prefix의
`CopyObject` 403/501을 설명한다. AWS SDK v2
[`PutObjectRequest`](https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/model/PutObjectRequest.html)는
`If-Match`/`If-None-Match`와 409 후 reread/retry surface를 제공한다. 이들은
**provider observation/probe source**일 뿐 expected semantic oracle, exact
production choice, 숫자 또는 provider default가 아니다.

```text
catalogVersion=phase11-s3-actual-cases-v1
futurePath=build/port-contract-tests/src/test/resources/phase11/phase11-s3-actual-cases-v1.json
sourcePath=docs/implementation/human-guides/phases/phase-11-human-implementation-guide.md
sourceSections=9.1.1,11.3.1
canonicalSourceBlob=14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8
caseCount=29
contentSha256=279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81
owner=Phase 11 independent provider-integration test owner
```

`contentSha256` 입력은 아래 marker 사이 JSON fence 내부의 첫 `{`부터 마지막 `}` 뒤
final LF까지인 UTF-8/LF exact bytes다. Future JSON file은 그 bytes와 byte-exact해야
한다. `fixtureRef`는 versioned fixture/probe recipe의 stable ID이며 실제 fixture
bytes digest는 실행 전 case receipt에 추가한다. `independentOracle`는
hand/model/accepted Phase 09·10 oracle이고 AWS response/output 자체가 아니다.

<!-- phase11-s3-actual-cases-v1:begin -->
```json
{
  "catalogVersion": "phase11-s3-actual-cases-v1",
  "caseCount": 29,
  "reportIdPrefix": "P11CASE:",
  "cases": [
    {
      "caseId": "P11S3-IMM-409",
      "operation": "IMMUTABLE_CREATE",
      "observation": "409_CONDITIONAL_REQUEST_CONFLICT",
      "fixtureRef": "s3-actual/immutable-create.json#concurrent-delete-409",
      "providerProbe": "conditional PutObject while a competing delete wins",
      "independentOracle": "model:immutable-create/current-key-and-expected-bytes",
      "passCriterion": "exact reread classifies same bytes as converge, different bytes as integrity conflict, and blind retry or overwrite is zero",
      "sensitivityFaultId": "P11S3FAULT-IMM-409-BLIND-RETRY",
      "reportId": "P11CASE:P11S3-IMM-409"
    },
    {
      "caseId": "P11S3-IMM-412",
      "operation": "IMMUTABLE_CREATE",
      "observation": "412_PRECONDITION_FAILED",
      "fixtureRef": "s3-actual/immutable-create.json#existing-current-412",
      "providerProbe": "If-None-Match PutObject against an existing current object",
      "independentOracle": "model:immutable-create/same-versus-different-content",
      "passCriterion": "same protected bytes converge, different bytes are integrity conflict, and original current bytes remain unchanged",
      "sensitivityFaultId": "P11S3FAULT-IMM-412-AS-CONVERGED",
      "reportId": "P11CASE:P11S3-IMM-412"
    },
    {
      "caseId": "P11S3-IMM-DENY",
      "operation": "IMMUTABLE_CREATE",
      "observation": "DENY",
      "fixtureRef": "s3-actual/immutable-create.json#conditional-policy-deny",
      "providerProbe": "PutObject with the required conditional authority removed",
      "independentOracle": "role-action-model:immutable-create-denied",
      "passCriterion": "typed DENIED is preserved, NOT_FOUND is never emitted, and no object or pointer changes",
      "sensitivityFaultId": "P11S3FAULT-IMM-DENY-AS-NOT-FOUND",
      "reportId": "P11CASE:P11S3-IMM-DENY"
    },
    {
      "caseId": "P11S3-IMM-TIMEOUT",
      "operation": "IMMUTABLE_CREATE",
      "observation": "TIMEOUT_BEFORE_COMMIT",
      "fixtureRef": "s3-actual/immutable-create.json#deadline-before-send",
      "providerProbe": "expire the approved operation deadline before request commit",
      "independentOracle": "model:immutable-create/no-side-effect",
      "passCriterion": "typed transient unavailable is returned and exact read proves the key remains absent",
      "sensitivityFaultId": "P11S3FAULT-IMM-TIMEOUT-AS-SUCCESS",
      "reportId": "P11CASE:P11S3-IMM-TIMEOUT"
    },
    {
      "caseId": "P11S3-IMM-UNKNOWN",
      "operation": "IMMUTABLE_CREATE",
      "observation": "RESPONSE_UNKNOWN",
      "fixtureRef": "s3-actual/immutable-create.json#commit-then-drop-response",
      "providerProbe": "drop the response after the conditional PutObject may have committed",
      "independentOracle": "model:immutable-create/expected-protected-bytes",
      "passCriterion": "exact reread alone proves converge or integrity conflict; absence or indeterminate visibility never becomes success",
      "sensitivityFaultId": "P11S3FAULT-IMM-UNKNOWN-ASSUME-SUCCESS",
      "reportId": "P11CASE:P11S3-IMM-UNKNOWN"
    },
    {
      "caseId": "P11S3-STATE-409",
      "operation": "STATE_CAS",
      "observation": "409_CONDITIONAL_REQUEST_CONFLICT",
      "fixtureRef": "s3-actual/state-cas.json#concurrent-delete-409",
      "providerProbe": "conditional state replacement concurrent with a competing mutation",
      "independentOracle": "model:state-cas/expected-token-and-desired-ref",
      "passCriterion": "exact current read returns already-at-desired, stale-version, or visibility-indeterminate without token reuse or last-write-wins",
      "sensitivityFaultId": "P11S3FAULT-STATE-409-REUSE-TOKEN",
      "reportId": "P11CASE:P11S3-STATE-409"
    },
    {
      "caseId": "P11S3-STATE-412",
      "operation": "STATE_CAS",
      "observation": "412_PRECONDITION_FAILED",
      "fixtureRef": "s3-actual/state-cas.json#stale-current-412",
      "providerProbe": "If-Match state replacement with a stale opaque token",
      "independentOracle": "model:state-cas/exactly-one-winner",
      "passCriterion": "loser is typed stale-version after reread, winner content/token are exact, and the loser never overwrites",
      "sensitivityFaultId": "P11S3FAULT-STATE-412-LAST-WRITE-WINS",
      "reportId": "P11CASE:P11S3-STATE-412"
    },
    {
      "caseId": "P11S3-STATE-DENY",
      "operation": "STATE_CAS",
      "observation": "DENY",
      "fixtureRef": "s3-actual/state-cas.json#role-or-kms-deny",
      "providerProbe": "state replacement under a role or KMS context without authority",
      "independentOracle": "role-action-model:state-cas-denied",
      "passCriterion": "typed DENIED is preserved and the prior accepted state bytes/token remain current",
      "sensitivityFaultId": "P11S3FAULT-STATE-DENY-AS-CONFLICT",
      "reportId": "P11CASE:P11S3-STATE-DENY"
    },
    {
      "caseId": "P11S3-STATE-TIMEOUT",
      "operation": "STATE_CAS",
      "observation": "TIMEOUT_BEFORE_COMMIT",
      "fixtureRef": "s3-actual/state-cas.json#deadline-before-send",
      "providerProbe": "expire the operation deadline before the conditional state request commits",
      "independentOracle": "model:state-cas/prior-state-unchanged",
      "passCriterion": "typed transient unavailable is returned and exact current read proves prior state/token unchanged",
      "sensitivityFaultId": "P11S3FAULT-STATE-TIMEOUT-ADVANCE",
      "reportId": "P11CASE:P11S3-STATE-TIMEOUT"
    },
    {
      "caseId": "P11S3-STATE-UNKNOWN",
      "operation": "STATE_CAS",
      "observation": "RESPONSE_UNKNOWN",
      "fixtureRef": "s3-actual/state-cas.json#commit-then-drop-response",
      "providerProbe": "drop the response after conditional state replacement may have committed",
      "independentOracle": "model:state-cas/desired-ref-and-prior-token",
      "passCriterion": "exact reread proves already-at-desired or stale-version; ambiguous visibility is fail-closed and no second blind CAS occurs",
      "sensitivityFaultId": "P11S3FAULT-STATE-UNKNOWN-SECOND-CAS",
      "reportId": "P11CASE:P11S3-STATE-UNKNOWN"
    },
    {
      "caseId": "P11S3-PUB-409",
      "operation": "PUBLICATION_CAS",
      "observation": "409_CONDITIONAL_REQUEST_CONFLICT",
      "fixtureRef": "s3-actual/publication-cas.json#concurrent-delete-409",
      "providerProbe": "conditional publication update concurrent with a competing pointer mutation",
      "independentOracle": "model:publication-cas/publishable-closure-and-distinct-precondition",
      "passCriterion": "reread revalidates both the run-state authorization and distinct pointer precondition; object existence alone never succeeds",
      "sensitivityFaultId": "P11S3FAULT-PUB-409-SKIP-AUTHORIZATION",
      "reportId": "P11CASE:P11S3-PUB-409"
    },
    {
      "caseId": "P11S3-PUB-412",
      "operation": "PUBLICATION_CAS",
      "observation": "412_PRECONDITION_FAILED",
      "fixtureRef": "s3-actual/publication-cas.json#stale-pointer-412",
      "providerProbe": "publication update with a stale distinct pointer precondition",
      "independentOracle": "model:publication-cas/desired-digest-and-both-gate-closure",
      "passCriterion": "same authorized publishable digest converges, different digest is publication conflict, and the accepted pointer is unchanged",
      "sensitivityFaultId": "P11S3FAULT-PUB-412-CONVERGE-DIFFERENT",
      "reportId": "P11CASE:P11S3-PUB-412"
    },
    {
      "caseId": "P11S3-PUB-DENY",
      "operation": "PUBLICATION_CAS",
      "observation": "DENY",
      "fixtureRef": "s3-actual/publication-cas.json#role-or-policy-deny",
      "providerProbe": "publication write under an unauthorized role or missing conditional header",
      "independentOracle": "role-action-model:publication-denied",
      "passCriterion": "typed DENIED is preserved, no SUCCEEDED state is synthesized, and the accepted pointer remains exact",
      "sensitivityFaultId": "P11S3FAULT-PUB-DENY-AS-NOT-FOUND",
      "reportId": "P11CASE:P11S3-PUB-DENY"
    },
    {
      "caseId": "P11S3-PUB-TIMEOUT",
      "operation": "PUBLICATION_CAS",
      "observation": "TIMEOUT_BEFORE_COMMIT",
      "fixtureRef": "s3-actual/publication-cas.json#deadline-before-send",
      "providerProbe": "expire the operation deadline before pointer commit",
      "independentOracle": "model:publication-cas/prior-pointer-unchanged",
      "passCriterion": "typed transient unavailable is returned, no terminal success is emitted, and exact read proves the prior pointer unchanged",
      "sensitivityFaultId": "P11S3FAULT-PUB-TIMEOUT-AS-SUCCEEDED",
      "reportId": "P11CASE:P11S3-PUB-TIMEOUT"
    },
    {
      "caseId": "P11S3-PUB-UNKNOWN",
      "operation": "PUBLICATION_CAS",
      "observation": "RESPONSE_UNKNOWN",
      "fixtureRef": "s3-actual/publication-cas.json#commit-then-drop-response",
      "providerProbe": "drop the response after the pointer write may have committed",
      "independentOracle": "model:publication-cas/authorized-desired-digest",
      "passCriterion": "exact reread plus both-gate authorization proves converge or conflict; ambiguous visibility never becomes SUCCEEDED",
      "sensitivityFaultId": "P11S3FAULT-PUB-UNKNOWN-ASSUME-SUCCESS",
      "reportId": "P11CASE:P11S3-PUB-UNKNOWN"
    },
    {
      "caseId": "P11S3-VERSION-ENABLED-CURRENT",
      "operation": "VERSIONED_CURRENT_STATE",
      "observation": "VERSIONING_ENABLED_CURRENT_OBJECT",
      "fixtureRef": "s3-actual/versioning.json#enabled-current-object",
      "providerProbe": "conditional write and exact read with a current version in an enabled bucket",
      "independentOracle": "model:lifecycle/current-logical-object",
      "passCriterion": "only current protected bytes determine logical state; version ID and ETag remain provider observations",
      "sensitivityFaultId": "P11S3FAULT-VERSION-ENABLED-LATEST-AS-IDENTITY",
      "reportId": "P11CASE:P11S3-VERSION-ENABLED-CURRENT"
    },
    {
      "caseId": "P11S3-VERSION-SUSPENDED-CURRENT",
      "operation": "VERSIONED_CURRENT_STATE",
      "observation": "VERSIONING_SUSPENDED_CURRENT_OBJECT",
      "fixtureRef": "s3-actual/versioning.json#suspended-current-null-version",
      "providerProbe": "conditional write and exact read with the current null version in a suspended bucket",
      "independentOracle": "model:lifecycle/current-logical-object",
      "passCriterion": "suspended/null-version behavior is explicitly classified and never inferred from the enabled-mode fixture",
      "sensitivityFaultId": "P11S3FAULT-VERSION-SUSPENDED-AS-ENABLED",
      "reportId": "P11CASE:P11S3-VERSION-SUSPENDED-CURRENT"
    },
    {
      "caseId": "P11S3-VERSION-DELETE-MARKER-CURRENT",
      "operation": "VERSIONED_CURRENT_STATE",
      "observation": "CURRENT_DELETE_MARKER",
      "fixtureRef": "s3-actual/versioning.json#enabled-current-delete-marker",
      "providerProbe": "If-None-Match write when the current version is a delete marker",
      "independentOracle": "model:lifecycle/approved-delete-marker-policy",
      "passCriterion": "provider success is not logical ABSENT until the approved lifecycle oracle says so; prior immutable versions remain untouched",
      "sensitivityFaultId": "P11S3FAULT-DELETE-MARKER-AUTO-ABSENT",
      "reportId": "P11CASE:P11S3-VERSION-DELETE-MARKER-CURRENT"
    },
    {
      "caseId": "P11S3-RETRY-PUT-409",
      "operation": "PUT_OBJECT_RETRY",
      "observation": "409_RETRYABLE_ONLY_AFTER_REVALIDATION",
      "fixtureRef": "s3-actual/retry.json#put-object-409-replayable",
      "providerProbe": "conditional PutObject returns 409 with a replayable source",
      "independentOracle": "model:immutable-create/current-state-plus-source-digest",
      "passCriterion": "a new transport attempt is allowed only after exact reread/revalidation and uses identical logical identity and bytes",
      "sensitivityFaultId": "P11S3FAULT-PUT-409-RETRY-WITHOUT-REREAD",
      "reportId": "P11CASE:P11S3-RETRY-PUT-409"
    },
    {
      "caseId": "P11S3-REINIT-MPU-409",
      "operation": "COMPLETE_MULTIPART_UPLOAD_REINIT",
      "observation": "409_REQUIRES_NEW_MULTIPART_UPLOAD",
      "fixtureRef": "s3-actual/retry.json#complete-multipart-409",
      "providerProbe": "CompleteMultipartUpload returns 409 after a competing delete",
      "independentOracle": "model:multipart/full-object-digest-and-part-plan",
      "passCriterion": "the old upload ID and completion request are never retried; a new CreateMultipartUpload and full approved part plan are required",
      "sensitivityFaultId": "P11S3FAULT-MPU-409-REUSE-UPLOAD-ID",
      "reportId": "P11CASE:P11S3-REINIT-MPU-409"
    },
    {
      "caseId": "P11S3-MPU-COMPLETE-412",
      "operation": "MULTIPART_COMPLETION",
      "observation": "412_AFTER_INTERVENING_CURRENT_WRITE",
      "fixtureRef": "s3-actual/multipart.json#complete-after-competing-put",
      "providerProbe": "complete an in-progress multipart upload after another current object wins",
      "independentOracle": "model:immutable-create/competing-current-object",
      "passCriterion": "completion is rejected, the competing current object stays byte-exact, and uploaded parts are not treated as a committed artifact",
      "sensitivityFaultId": "P11S3FAULT-MPU-412-PUBLISH-PARTS",
      "reportId": "P11CASE:P11S3-MPU-COMPLETE-412"
    },
    {
      "caseId": "P11S3-MPU-COMPLETE-UNKNOWN",
      "operation": "MULTIPART_COMPLETION",
      "observation": "RESPONSE_UNKNOWN",
      "fixtureRef": "s3-actual/multipart.json#complete-then-drop-response",
      "providerProbe": "drop the response after CompleteMultipartUpload may have committed",
      "independentOracle": "model:multipart/full-object-protected-bytes",
      "passCriterion": "exact current read verifies full length/checksum/digest/schema before converge; upload history or ETag alone is insufficient",
      "sensitivityFaultId": "P11S3FAULT-MPU-UNKNOWN-ETAG-AS-ORACLE",
      "reportId": "P11CASE:P11S3-MPU-COMPLETE-UNKNOWN"
    },
    {
      "caseId": "P11S3-MPU-CLEANUP-ORPHAN",
      "operation": "MULTIPART_CLEANUP",
      "observation": "BOUNDED_ORPHAN_ABORT",
      "fixtureRef": "s3-actual/multipart.json#orphan-held-current-matrix",
      "providerProbe": "run cleanup over orphan, held/quarantined, in-progress, and current accepted objects",
      "independentOracle": "model:multipart/approved-orphan-set",
      "passCriterion": "only the approved aged orphan upload IDs are aborted; held, in-progress, immutable current, state, and publication objects remain exact",
      "sensitivityFaultId": "P11S3FAULT-MPU-CLEANUP-DELETE-CURRENT",
      "reportId": "P11CASE:P11S3-MPU-CLEANUP-ORPHAN"
    },
    {
      "caseId": "P11S3-COPY-POLICY-403",
      "operation": "COPY_OBJECT_POLICY",
      "observation": "403_WITHOUT_CONDITIONAL_HEADER",
      "fixtureRef": "s3-actual/copy-policy.json#copy-without-condition",
      "providerProbe": "CopyObject into a conditional-write-enforced prefix without If-Match or If-None-Match",
      "independentOracle": "policy-model:copy-forbidden-under-conditional-enforcement",
      "passCriterion": "provider-native 403 is preserved as typed DENIED, no fallback PutObject occurs, and destination bytes remain unchanged",
      "sensitivityFaultId": "P11S3FAULT-COPY-403-FALLBACK-PUT",
      "reportId": "P11CASE:P11S3-COPY-POLICY-403"
    },
    {
      "caseId": "P11S3-COPY-POLICY-501",
      "operation": "COPY_OBJECT_POLICY",
      "observation": "501_WITH_CONDITIONAL_HEADER",
      "fixtureRef": "s3-actual/copy-policy.json#copy-with-condition",
      "providerProbe": "CopyObject into a conditional-write-enforced prefix with If-Match or If-None-Match",
      "independentOracle": "policy-model:copy-forbidden-under-conditional-enforcement",
      "passCriterion": "provider-native 501 is typed unsupported, no unconditional fallback occurs, and destination bytes remain unchanged",
      "sensitivityFaultId": "P11S3FAULT-COPY-501-DROP-HEADER",
      "reportId": "P11CASE:P11S3-COPY-POLICY-501"
    },
    {
      "caseId": "P11S3-BODY-REPLAYABLE-RETRY",
      "operation": "BODY_REPLAYABILITY",
      "observation": "REPLAYABLE_SOURCE",
      "fixtureRef": "s3-actual/body-replay.json#reopenable-byte-source",
      "providerProbe": "inject one approved retry into a source that can be reopened from byte zero",
      "independentOracle": "hand-oracle:full-source-digest-and-length",
      "passCriterion": "each attempt reads a fresh source from byte zero and the committed protected bytes equal the independent digest and length",
      "sensitivityFaultId": "P11S3FAULT-REPLAYABLE-RESUME-PARTIAL",
      "reportId": "P11CASE:P11S3-BODY-REPLAYABLE-RETRY"
    },
    {
      "caseId": "P11S3-BODY-NONREPLAYABLE-FAIL",
      "operation": "BODY_REPLAYABILITY",
      "observation": "NON_REPLAYABLE_SOURCE",
      "fixtureRef": "s3-actual/body-replay.json#one-shot-stream",
      "providerProbe": "inject 409 or response loss after consuming a one-shot stream",
      "independentOracle": "hand-oracle:no-second-read-and-no-partial-success",
      "passCriterion": "the adapter fails closed without a second transport attempt, fallback key, overwrite, or success inference",
      "sensitivityFaultId": "P11S3FAULT-NONREPLAYABLE-SECOND-ATTEMPT",
      "reportId": "P11CASE:P11S3-BODY-NONREPLAYABLE-FAIL"
    },
    {
      "caseId": "P11S3-CREDENTIAL-CROSS-TENANT-DENY",
      "operation": "SCOPED_CREDENTIAL",
      "observation": "CROSS_TENANT_DENY",
      "fixtureRef": "s3-actual/credential-scope.json#tenant-a-role-tenant-b-key",
      "providerProbe": "use tenant A temporary authority against tenant B exact key and KMS context",
      "independentOracle": "role-action-model:cross-tenant-deny",
      "passCriterion": "provider-native S3 or KMS deny is preserved, no existence signal leaks as NOT_FOUND, and target bytes remain unchanged",
      "sensitivityFaultId": "P11S3FAULT-CREDENTIAL-CROSS-TENANT-ALLOW",
      "reportId": "P11CASE:P11S3-CREDENTIAL-CROSS-TENANT-DENY"
    },
    {
      "caseId": "P11S3-CREDENTIAL-MISSING-SCOPE-DENY",
      "operation": "SCOPED_CREDENTIAL",
      "observation": "MISSING_OR_MISMATCHED_SCOPE",
      "fixtureRef": "s3-actual/credential-scope.json#missing-mismatched-provider-ref",
      "providerProbe": "resolve a missing or tenant-mismatched TenantCredentialProviderRef",
      "independentOracle": "role-action-model:credential-resolution-fail-closed",
      "passCriterion": "resolution fails before S3 lookup and ambient/default/shared broad credentials are never consulted",
      "sensitivityFaultId": "P11S3FAULT-CREDENTIAL-AMBIENT-FALLBACK",
      "reportId": "P11CASE:P11S3-CREDENTIAL-MISSING-SCOPE-DENY"
    }
  ]
}
```
<!-- phase11-s3-actual-cases-v1:end -->

Supplementary catalog의 JSON bytes와 digest는 바꾸지 않고, 그 catalog를 실행하는
sensitivity receipt schema를 다음과 같이 별도로 고정한다.
`expectedSensitivityDisposition`은 환경 변수나 provider 결과로 정하는 값이 아니라
schema의 literal `RED`다. `Phase11S3ActualCaseCatalogContractTest`는 29개
`caseId ↔ sensitivityFaultId ↔ reportId`가 각각 unique한 bijection인지와 이
literal/schema가 누락·완화되지 않았는지를 실행 전에 검사한다.

```text
Phase11S3SensitivityReceipt (exactly one per supplementary case)
  schemaVersion = phase11-s3-sensitivity-receipt-v1
  caseId = catalog.caseId
  reportId = "P11CASE:" + caseId
  sensitivityFaultId = catalog.sensitivityFaultId
  faultActivationEvidence = non-empty activation receipt + its content digest
  expectedSensitivityDisposition = RED
  normalDisposition = PASS
  sensitivityDisposition = RED
  sensitivityExitCode = non-zero
  phase11RunId
  sourceDigest
  effectivePomDigest
  profileIdentity
  deploymentIdentity
  canonicalManifestSha256 = 98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1
  s3ActualCatalogSha256 = 279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81
```

Activation evidence, normal `PASS`, sensitivity `RED`와 non-zero exit는 위 한
receipt에서 같은 case/fault pair와 동일한 source/run/profile/deployment 및 두
manifest digest에 결합한다. 모든 supplementary receipt도 서로 같은
`phase11RunId`, source/profile/deployment identity와 두 digest를 사용한다.
Activation만 별도 run ID로 바꾸거나 normal result, sensitivity result 또는 다른
case의 fault receipt를 이어 붙이면 `wrong-sensitivity-mapping`이다.

Catalog coverage는 다음 exact partition으로 audit한다.

```text
operation × observation matrix = 3 × 5 = 15
versioning/current-state         = 3
PutObject vs MPU retry/re-init   = 2
multipart completion/cleanup     = 3
copy-policy 403/501              = 2
body replayability               = 2
scoped credential deny           = 2
total                            = 29
```

Same-run audit는 canonical과 supplementary를 섞어 count를 맞추지 않는다.

```text
C = canonical phase11-required-tests-v1 expected set (64)
S = supplementary phase11-s3-actual-cases-v1 expected set (29)
DC/PC = same-run canonical discovered/passed multiset
DS/PS = same-run supplementary P11CASE discovered/passed multiset
RS = same-run supplementary sensitivity receipt multiset

activatedSensitivity =
  {caseId | exactly one RS receipt binds the catalog caseId, unique sensitivityFaultId,
            non-empty faultActivationEvidence and the same source/run/profile/deployment
            plus both manifest digests}
observedRed =
  {caseId | that mapped receipt has expectedSensitivityDisposition=RED,
            normalDisposition=PASS, sensitivityDisposition=RED
            and sensitivityExitCode != 0}
sensitivity-missing = S - activatedSensitivity
sensitivitySurvived =
  {caseId | fault activation receipt는 있으나 mapped case가 expected RED/non-zero가 아님}
sensitivity-survived = sensitivitySurvived
wrong-sensitivity-mapping =
  {caseId | caseId/reportId/sensitivityFaultId is absent, duplicated, cross-mapped,
            not the catalog mapping, or its source/run/profile/deployment/two-digest
            identity differs from the normal and sensitivity observations}

PASS iff:
  canonicalManifestSha256 == 98fa3535...e0a1
  s3ActualCatalogSha256 == 279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81
  |C| == |DC| == |PC| == 64
  |S| == |DS| == |PS| == 29
  |activatedSensitivity| == |observedRed| == 29
  for each set independently:
    missing = duplicate = unexpected = unmapped = failure = error = skipped = stale = ∅
  canonical-sensitivity-missing = ∅
  sensitivity-missing = sensitivity-survived = wrong-sensitivity-mapping = ∅
```

각 supplementary P11CASE receipt는 위 schema 외에도 case/fixture bytes digest,
provider probe receipt와 independent oracle receipt를 기록한다. Fresh auditor는
normal `29/29/29`가 green이어도 activated/observed-red count가 29가 아니거나
`sensitivity-missing`, `sensitivity-survived`, `wrong-sensitivity-mapping` 중 하나가
non-empty면 non-zero로 끝난다. 두 set 중 하나라도 digest/count/run identity가
다르거나 case가 다른 set의 report/fault ID로 대체되어도 non-zero다. Exact key
encoding, versioning mode, multipart threshold, retry 횟수,
SDK/service/provider default는 이 catalog의 expected 값이 아니며 계속
`OPEN/ADR_REQUIRED`다.

### 11.4 Red → green 순서

1. Missing module/template 때문에 architecture/event/IaC tests가 red임을 확인한다.
2. Phase 09 abstract suite를 in-memory/filesystem oracle에서는 green, S3 subject에서는 red로 고정한다.
3. Key/error/config unit을 green으로 만들고 omitted mode/unconditional write는 red로 유지한다.
4. Local emulator에서 serialization/event/SDK mapping만 green으로 만든다.
5. Actual S3 CAS/conditional policy/IAM/KMS tests를 red→green으로 만든다.
6. Phase 10 fake action trace와 actual Step Functions/Lambda trace를 비교한다.
7. Duplicate/lost/timeout/cancel/DLQ/corruption fault를 red→green으로 만든다.
8. Local↔AWS parity와 repeated-run reproducibility를 green으로 만든다.
9. Observability/cost/rollback과 independent review를 통과한다.

Cloud happy path가 먼저 green이어도 앞 layer의 red를 무시하지 않는다.

### 11.5 False-green 방지

- Selected unit test는 `surefire.failIfNoSpecifiedTests=true`, selected IT는 `failsafe.failIfNoSpecifiedTests=true`를 사용하고 plugin/effective-POM이 실제 property를 소비하는지 검사한다.
- `*IT`는 pinned Failsafe의 `integration-test`+`verify` binding, active profile, JUnit/fixture dependency와 XML summary가 모두 있어야 한다. File이 compile됐거나 `verify`가 exit 0인 것만으로 discovery를 추론하지 않는다.
- Filter 없는 target slice는 같은 source에서 `-pl <all Phase 11 modules>,build/port-contract-tests -am clean verify`로 upstream과 함께 실행한다. Selected subject는 먼저 같은 immutable source를 clean isolated local repository에 full install한 뒤 `-am` 없이 실행한다.
- 각 run 전 `clean`, 직후 다음 clean 전에 same-run XML/P11CASE receipt를 봉인하고 §11.3 canonical manifest와 §11.3.1 supplementary S3 catalog의 hash, source/effective-POM/profile/run ID를 대조한다.
- `Phase11S3ActualCaseCatalogContractTest`는 supplementary 29개 case↔unique fault↔report bijection과 receipt schema의 literal `expectedSensitivityDisposition=RED`를 검사하며, 누락·중복·완화가 있으면 provider run 전에 fail한다.
- Fresh auditor는 각 supplementary receipt의 fault activation evidence, normal `PASS`, sensitivity `RED`/non-zero disposition이 동일한 case ID, unique fault ID, source/run/profile/deployment와 두 manifest digest에 묶였는지 검사한다. `|activatedSensitivity|=|observedRed|=29`가 아니거나 sensitivity fault가 다른 case/report에 연결되면 fail이다.
- Failure/error/skipped/missing/duplicate/renamed-unmapped/unexpected/stale/sensitivity-missing/sensitivity-survived/wrong-sensitivity-mapping가 하나라도 있으면 fail이다. Receipt가 모두 존재해도 활성 fault의 mapped case가 expected `RED`/non-zero가 아니면 `sensitivity-survived`이며 normal `29/29/29`로 상쇄할 수 없다.
- `-am`이 다른 module의 같은 test 이름을 실행해 selected subject 0을 숨기지 않게 selected test에는 `-am`을 쓰지 않는다. 반대로 release/slice gate에서는 `-am clean`을 생략해 stale upstream artifact를 쓰지 않는다.
- Emulator profile이 actual AWS suite를 skip하면 actual AWS gate는 fail이다.
- Console summary, 이전 `target/`, generated template와 screenshot은 evidence가 아니다.
- `sam deploy --no-fail-on-empty-changeset`의 exit 0은 `NO_CHANGE`다.
- Failure run, timeout run과 비용이 큰 run을 evidence에서 선택적으로 빼지 않는다.
- Pre-review evidence manifest에 reviewer/review result/receipt를 넣지 않는다.

### 11.6 Test 종류별 적용성과 pass 판정

| 종류 | 적용 | Phase 11 pass 판정 | 미적용/경계 이유 |
|---|---|---|---|
| Unit | 필수 | Key/error/config/event pure mapping 전수 pass | AWS 실제 semantics를 대신하지 않음 |
| Contract | 필수 | Same abstract storage/workflow/compute cases pass | Provider별 별도 의미 허용 안 함 |
| Integration | 필수 | Isolated actual S3/Lambda/Step Functions/IAM/KMS pass | Emulator-only는 미적용 |
| E2E | 필수 | Submit→all workers→two gate→publish/retrieve exact | Production traffic은 범위 밖 |
| Architecture | 필수 | SDK/provider/customer/vendor leakage 0 | Source grep만으로 bytecode/DAG를 대신하지 않음 |
| Fault | 필수 | Retry/timeout/cancel/lost/duplicate/OOM/DLQ typed terminal | Happy path로 대체 불가 |
| Corruption | 필수 | Length/schema/checksum/digest/state/pointer 손상 reject | 암호화 pass로 대체 불가 |
| Reproducibility | 필수 | Same envelope semantic trace/result equal | Latency/request ID는 비교 제외 |
| Security | 필수 | IAM/KMS/tenant/network/redaction negative matrix all pass | Static lint-only로 actual deny 대체 불가 |
| Performance | 조건부 필수 | 측정 vector와 explicit non-prod envelope 완전 | 공식 threshold는 OPEN; pass/fail 숫자 발명 금지 |
| Cost | 조건부 필수 | Usage vector 완전, cap 미승인 표시 | Production affordability 판정은 owner gate |
| Chaos/large-scale load | 별도 승인 | 승인된 workload에 bounded fault/load evidence | 무제한 비용·production 계정 실행은 범위 밖 |
| UI/browser | 미적용 | 해당 없음 | Phase 11은 UI를 소유하지 않음 |
| Database migration | 미적용/금지 | DB resource 0 | No-DB fixed contract |
| Hybrid/MIP | 미적용/금지 | OR-Tools dependency/type 0 | Phase 13 optional gated |

#### 11.6.1 Observability와 cost required schema

다음 catalog는 [Canonical Phase 11 §10](../../phases/phase-11-aws-reference-distribution.md#10-observability와-cost-contract) blob `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8`에서 파생한 normative minimum이다. Future JSON schema는 §8.1의 exact path에 두고 `schemaVersion`, canonical source blob/section, owner, content digest를 포함한다. Owner는 Logs/Metrics/Traces/Alarms는 Phase 11 Operations+Security, usage/cost는 FinOps+Operations, independent completeness oracle는 Phase 11 provider-integration test owner다. 구현자가 수집하기 쉬운 field만 남겨 schema를 golden-update할 수 없다.

Structured log completeness:

| Event scope | Required fields |
|---|---|
| 모든 Phase 11 event | `eventSchemaVersion`, `stage`, `component`, `deploymentRevision`, `tenantHandle`, `solveId`, `manifestFingerprint`, `traceId` |
| Worker lifecycle | `roundOrdinal`, `workerOrdinal`, `workerRunId`, `attemptId` |
| State/action transition | `actionType`, `stateBefore`, `stateAfter` |
| Terminal transition | `termination` |
| Artifact operation | `artifactKind`, `artifactDigest` |
| Verification/publication | `verifierDisposition`; 관련 artifact가 있으면 `artifactKind`, `artifactDigest` |
| AWS invocation/workflow observation | 적용 가능한 component에 `awsRequestId`, `stateMachineExecutionHandle` |

빈 문자열, sentinel `"unknown"` 또는 이전 event의 field 복사는 present가 아니다. Event type별 required set과 discovered non-null typed field의 차집합이 0이어야 한다. `tenantHandle`은 opaque handle이며 raw tenant/customer name이 아니다.

Required metrics와 unit/dimension:

| Metric | Unit | Allowed dimensions |
|---|---|---|
| `submissions_total` | `Count` | `component,outcome` |
| `state_transition_total` | `Count` | `component,transition,outcome` |
| `worker_dispatch_total` | `Count` | `outcome` |
| `worker_attempt_total` | `Count` | `termination` |
| `artifact_io_total` | `Count` | `operation,outcome` |
| `artifact_bytes` | `Bytes` | `operation,kind` |
| `cas_conflict_total` | `Count` | `object_kind` |
| `duplicate_event_total` | `Count` | `classification` |
| `declared_worker_missing_total` | `Count` | `component` |
| `verification_total` | `Count` | `gate,disposition` |
| `publication_total` | `Count` | `outcome` |
| `platform_throttle_total` | `Count` | `service,operation` |
| `dlq_or_reconciliation_backlog` | `Count` | `component` |

Metric dimension에는 `tenant`, `tenantHandle`, `customer`, `solveId`, `manifestFingerprint`, `roundOrdinal`, `workerOrdinal`, `workerRunId`, `attemptId`, artifact/result digest, address, credential 또는 raw provider request ID를 넣지 않는다. Catalog 밖 dimension, wrong unit, unbounded value 또는 required metric missing은 fail이다.

Required alarms:

1. Failed/throttled coordinator 또는 worker invocation.
2. Step Functions failed/timed-out/aborted execution.
3. DLQ/reconciliation backlog non-zero.
4. Missing declared worker 또는 round incomplete.
5. Candidate/result verifier failure와 publication rejection.
6. S3/KMS access denied, digest mismatch와 CAS conflict surge.
7. Lambda concurrency/throttle, timeout, out-of-memory/resource failure.
8. Log delivery 또는 trace exporter failure.

각 alarm은 IaC logical ID, source metric/query, unit, evaluation policy identity, injected fault ID, expected transition과 owner/runbook ref를 가져야 한다. Integrity/security/failure event와 alarm은 sampling으로 버릴 수 없다. Trace는 API → coordinator → dispatcher → worker → completion → publication chain을 parent/link와 `traceId`로 연결하며 raw artifact body, secret/PII를 attribute/event에 넣지 않는다.

Cost/usage completeness:

| Required usage field | Unit/required qualifier |
|---|---|
| Lambda invocations | `Count`; successful/failed/retried 포함 |
| Lambda duration/compute | `Milliseconds`와 `GB-seconds`; memory와 provisioned/reserved setting identity |
| Step Functions executions/transitions | `Count`; workflow type/mode별 |
| S3 usage | Operation별 request `Count`, stored/read/written `Bytes`, retention/storage class |
| KMS usage | Operation별 request `Count` |
| Observability usage | Log ingestion/storage `Bytes`, metric datum `Count`, trace span/volume `Count` 또는 `Bytes`를 schema에 고정 |
| Network transfer | `Bytes`; source/destination region/path classification |
| Failed/retried/duplicate work | 각각 numerator/denominator `Count`와 dimensionless ratio; failure run을 제외하지 않음 |

Completeness/freshness/forbidden-field oracle:

```text
expectedFields - discoveredTypedFields = ∅
discoveredFields - allowedFields = ∅
duplicateSchemaKey = ∅
wrongUnit = ∅
forbiddenFieldOrDimension = ∅
missingFailureRun = ∅

for every log/metric/trace/alarm datum:
  phase11RunId/source/deployment/profile identity matches
  observedAt is inside the declared run/collection window
  exporter/query receipt exists before evidence seal

for cost/usage:
  usageWindow covers every normal + failed + retried + duplicate run
  providerSource.completeThrough >= usageWindow.end
  collectedAt >= completeThrough and before evidence seal
```

Exact collection delay, alarm evaluation period, retention, price, workload class와 cost cap은 owner가 manifest/ADR에 explicit하게 승인할 때까지 `OPEN/OWNER_APPROVAL_REQUIRED`다. 값이 미승인이면 field completeness evidence는 만들 수 있지만 affordability/production gate는 false다. `completeThrough`가 run end보다 이르면 기다리거나 `PENDING_FRESHNESS`로 실패하며 이전 billing window나 추정 단가로 green을 합성하지 않는다.

### 11.7 Maven/IaC 명령 — 현재와 미래를 구분하기

현재 inventory에서 사실 확인에만 쓸 수 있는 명령:

```bash
mvn -version
./mvnw -version
git status --short
find rpdptw build legacy adapters apps -type f -not -path '*/target/*' | sort
```

현재 `./mvnw`, POM scaffold 또는 root build 성공은 Phase 00/11 acceptance evidence가 아니다. Concurrent owner가 scaffold를 freeze하기 전 `clean verify` 결과를 Phase 11 기준선으로 봉인하지 않는다.

Target tree가 구현되고 accepted Phase 00 wrapper/reactor가 존재할 때의 future baseline이다. `<isolated-m2>`, `<phase11-run-id>`와 AWS stage 값은 entry receipt가 정한 task-specific 값으로 치환한다.

```bash
# 1. Same immutable source를 clean isolated local repository에 full install한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> clean install

# 2. Selected unit test는 -am 없이 0-test를 fail-closed한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -pl build/architecture-rules \
  -Dtest=AwsSdkLeakageArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

# 3. Target 전체는 upstream과 함께 반드시 clean부터 다시 빌드한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -Dphase11.runId=<phase11-run-id> \
  -DskipTests=false -DskipITs=false \
  -pl adapters/object-s3,adapters/workflow-aws-stepfunctions,adapters/compute-aws-lambda,distributions/aws-serverless,build/port-contract-tests \
  -am clean verify

# 4. Local integration profile도 filter 없이 별도 clean reactor run이다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -Dphase11.runId=<phase11-run-id>-local \
  -DskipTests=false -DskipITs=false \
  -pl adapters/object-s3,adapters/workflow-aws-stepfunctions,adapters/compute-aws-lambda,distributions/aws-serverless,build/port-contract-tests \
  -am -Paws-local-it clean verify

# 5. Effective POM/profile output을 각 owner module에서 만들고 contract test로 검사한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -pl distributions/aws-serverless -Paws-local-it \
  help:active-profiles help:effective-pom \
  -Doutput=target/phase11-local-effective-pom.xml
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -pl build/port-contract-tests \
  -Dtest=Phase11EffectivePomContractTest,Phase11RequiredTestManifestContractTest,Phase11S3ActualCaseCatalogContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true test

sam validate --template-file deployment/aws/template.yaml --lint
cfn-lint deployment/aws/template.yaml deployment/aws/alarms/required-alarms.yaml
```

Approved isolated environment에서만:

```bash
sam build --template-file deployment/aws/template.yaml
sam deploy --config-file deployment/aws/samconfig.toml \
  --config-env integration --no-confirm-changeset --no-fail-on-empty-changeset

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -Dphase11.runId=<phase11-run-id>-aws \
  -DskipTests=false -DskipITs=false \
  -pl adapters/object-s3,adapters/workflow-aws-stepfunctions,adapters/compute-aws-lambda,distributions/aws-serverless,build/port-contract-tests \
  -am -Paws-integration clean verify \
  -Daws.stage=integration -Daws.evidenceDir=target/phase11-evidence

# 위 run의 XML/P11CASE receipt를 다음 clean 전에 audit한다. Selected auditor에는 -am을 쓰지 않는다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-m2> \
  -pl build/port-contract-tests \
  -Dtest=Phase11FreshReportContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dphase11.reportRunId=<phase11-run-id>-aws \
  -Dphase11.requiredManifestSha256=98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1 \
  -Dphase11.s3ActualCatalogSha256=279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81 \
  test
```

각 `-am clean verify` run 직후 `Phase11FreshReportContractTest` auditor가 canonical
`64/64/64`와 supplementary actual-S3 `29/29/29`를 독립 확인하고, 다음 clean 전에
supplementary receipt 29개를 다시 읽어 `|activatedSensitivity|=|observedRed|=29`와
`sensitivity-missing=sensitivity-survived=wrong-sensitivity-mapping=∅`를
검사해야 한다. 각 activation receipt는 case ID, catalog의 unique fault ID와
activation evidence, normal `PASS`, sensitivity `RED`/non-zero disposition을
동일 source/run/profile/deployment, canonical manifest digest와 supplementary
catalog digest에 결합해야 한다. 두 set 모두
failure/error/skipped/missing/duplicate/unmapped/stale 0이어야 하며, survived/wrong
disposition은 normal `29/29/29`와 무관하게 auditor non-zero다. Reactor exit `0`,
Failsafe summary 부재, profile 미활성, stale local repository artifact 또는 이전
report도 fail이다. `sam`, `cfn-lint`, target profiles와 modules는 correction
inventory에서 존재·설치·구현되었다고 확인되지 않았다. 따라서 위 명령은 지금
실행할 수 있는 완료 증거가 아니라 미래 조건부 계약이다. Production deploy
command는 이 가이드에 포함하지 않는다.

## 12. 사람 checkpoint, evidence, stop과 resume

### 12.1 Checkpoint 표

| Checkpoint | 사람이 확인할 evidence | Stop 조건 | Resume 조건 |
|---|---|---|---|
| C0 Entry | Accepted predecessor refs, ADR, environment, owner | Any missing/unaccepted | `Phase11EntryReceipt` 완전 |
| C1 Boundary | Module DAG, leakage/event/config red-green | Stable module leakage/default | Phase 00 owner 승인 |
| C2 Storage | Shared suite + actual S3 CAS/policy/KMS | Last-write-wins/unconditional | Actual negative/concurrency pass |
| C3 Compute | Handler/retry/deadline mapping | Context/timeout/seed drift | Contract + fault pass |
| C4 Workflow | Action-only ASL + trace parity | Semantic Choice, stale action | Phase 10 oracle parity |
| C5 Security | Positive/negative role matrix | Cross-tenant/wildcard/public ingress | Security approval |
| C6 Operations | Quota/alarms/cost/retention | Hidden limit, missing signal | Ops/FinOps approval |
| C7 Parity | Local↔AWS + fault/repro | Semantic mismatch/partial publish | Full immutable bundle |
| C8 Rollback/review | Deploy manifest, rollback, M→R→receipt | Unresolved in-flight/CHANGES_REQUIRED | Accepted independent review |

### 12.2 Evidence DAG

```text
implementation/test/provider evidence
→ Phase11EvidenceManifest [digest M]
→ independentReviewReport [references M, digest R]
→ Phase11PostReviewAcceptanceReceipt [references M + R]
→ accepted handoff
```

금지 방향:

```text
Phase11EvidenceManifest -X-> review/receipt/Phase12 manifest
review report           -X-> acceptance receipt
Phase 11 receipt        -X-> Phase12 ProviderConformanceManifest
```

현재 review verdict는 `CHANGES_REQUIRED`, evidence는 `NOT_PRODUCED`이므로 M, accepted R, receipt 모두 현재 존재하지 않는다.

### 12.3 Evidence manifest 최소 항목

```text
Phase11EvidenceManifest
  phase/provider/storage/workflow/compute
  entryReceiptRef
  sourceCommit/canonicalFive/sectionFingerprints/sourceGovernanceDecision
  Phase08/09/10 accepted contract + handoff refs
  build/architecture/effectivePom/profile evidence
  requiredTestManifestSha256 + canonicalFreshReportSet + canonicalSensitivityReceipt
  s3ActualCaseCatalogSha256 + s3ActualFreshReportSet
  s3ActualSensitivityReceipts[29]
    schemaVersion = phase11-s3-sensitivity-receipt-v1
    caseId + reportId + unique sensitivityFaultId
    faultActivationEvidence + faultActivationEvidenceDigest
    expectedSensitivityDisposition = RED
    normalDisposition = PASS
    sensitivityDisposition = RED + sensitivityExitCode(non-zero)
    same sourceDigest/phase11RunId/effectivePomDigest/profileIdentity/deploymentIdentity
    same requiredTestManifestSha256/s3ActualCaseCatalogSha256
  activatedSensitivityCount = 29
  observedRedCount = 29
  sensitivityMissing[] + sensitivitySurvived[] + wrongSensitivityMapping[] = empty
  S3 actual/conditional-policy/409-412/versioning/multipart/credential evidence
  distinct publication precondition evidence
  Lambda/Step Functions type-mode/action evidence
  same-state cancel/publish evidence
  durable deadline restart evidence
  IAM/KMS/tenant/network evidence
  quota/DLQ/observabilitySchemaDigests/costSchemaDigest/freshness evidence
  local/AWS parity + fault/repro evidence
  deployment/rollback refs
  limitationsAndOpenValues[]
  productionAuthority = false
  contentDigest
```

Evidence schema validator는 `activatedSensitivityCount` 또는 `observedRedCount`가
29가 아니거나 `sensitivityMissing[]`, `sensitivitySurvived[]`,
`wrongSensitivityMapping[]` 중 하나가 non-empty면 non-zero다. Normal
supplementary report `29/29/29`, receipt 파일 존재 또는 manifest serialization
성공은 이 negative-disposition failure를 상쇄하지 않는다.

### 12.4 멈춘 뒤 넘길 blocker note

```text
blocked_contract:
owner:
last_safe_state:
forbidden_next_action:
evidence_already_available:
missing_approval_or_test:
exact_restart_condition:
rollback_or_cleanup_owner:
production_authority: false
```

“AWS가 잘 안 됨”처럼 쓰지 않는다. 어느 contract, owner, evidence와 restart condition이 빠졌는지 적는다.

## 13. 금지 anti-pattern

| Anti-pattern | 왜 금지하는가 |
|---|---|
| Root/core/application POM에 AWS SDK 추가 | Provider-neutral DAG 붕괴 |
| S3 key/ETag/ARN을 domain ID로 사용 | Migration/replay identity 붕괴 |
| DynamoDB로 state/CAS 구현 | No-DB authority 위반 |
| Prefix list로 worker fan-in | Missing worker false success |
| Conditional header 없는 write | Immutable/CAS/publication fence 우회 |
| Adapter-only check 후 bucket policy 생략 | Buggy path의 overwrite 방어 없음 |
| S3 412만 conflict로 처리하고 409/response unknown을 blind retry | Stale precondition·non-replayable body와 lost response 의미 붕괴 |
| Version delete/delete marker 또는 multipart abort를 rollback으로 사용 | Accepted current object/pointer와 immutable lineage 삭제 위험 |
| Shared/ambient credential fallback | Tenant authority와 existence confidentiality 붕괴 |
| Step Functions에서 objective/champion 판단 | Second semantic coordinator 생성 |
| Workflow/invocation mode default | Delivery/retry/duration가 hidden input |
| Opaque run-state token을 action/event/publish에 재사용 | Crash replay와 distinct CAS 붕괴 |
| Cancel intent key를 publish와의 승자로 사용 | 서로 다른 key CAS가 모두 성공 가능 |
| Wall clock/remaining time으로 deadline 복원 | Restart마다 watchdog 의미 변경 |
| Raw event/Context를 solver에 전달 | Provider/domain 경계 붕괴 |
| Platform timeout을 max-step으로 변환 | Quality와 infra failure 혼합 |
| Retry 때 seed/warm start/work 변경 | Logical reproducibility 붕괴 |
| DLQ drain을 success로 계산 | Poison/lost event 은폐 |
| Shared broad IAM role | Tenant/least privilege 상실 |
| SSE-KMS로 digest 검증 대체 | Encryption과 integrity 혼동 |
| Digest로 IAM/encryption 대체 | Integrity와 confidentiality 혼동 |
| Full payload/PII/secret log | Security/tenant boundary 위반 |
| Emulator를 actual AWS evidence로 표기 | Provider semantics 거짓 증명 |
| `*IT`만 만들고 Failsafe/profile/JUnit wiring을 생략 | `verify` 0-test false-green |
| `-am clean` 없는 slice/release verify | Local repository의 stale upstream artifact 사용 |
| Required schema를 emitted output에서 역생성 | Observability/cost completeness self-fulfilling green |
| Generated `.serverless`를 source로 복사 | 재현 불가 + DynamoDB legacy 승격 |
| Test-only 수치를 production default로 사용 | OPEN gate 임의 해소 |
| Phase 12/13/14 작업을 함께 구현 | Scope와 authority gate 위반 |
| Happy path/SAM exit 0만으로 DoD 선언 | Contract/fault/security/rollback 미증명 |

## 14. 실제 구현 exit checklist와 Definition of Done

다음은 모두 AND다.

### 14.1 Entry와 scope

- [ ] Phase 00/06~10 accepted evidence/review와 exact handoff receipt가 있다.
- [ ] Phase 08/09 direct source section semantic diff와 accepted implementation artifact identity가 receipt에 있고, proposed signature와 다르면 owner-approved 하나로 교체됐다.
- [ ] Scheduler task와 implementation/security/operations/cost/review owner가 지정됐다.
- [ ] Approved isolated account alias/region/role/budget/cleanup authority가 있다.
- [ ] Phase 12/13/14 scope를 당기지 않았다.

### 14.2 Architecture와 contract

- [ ] Stable module AWS SDK/event/ARN reference가 0이다.
- [ ] Exact current pending action authorization이 accepted contract와 일치한다.
- [ ] Run-state token과 publication precondition이 distinct하다.
- [ ] `CANCEL_REQUESTED`와 `PUBLISHING`이 같은 run-state version에서 경쟁한다.
- [ ] Durable deadline ADR가 없으면 deadline-enabled AWS path가 disabled다.
- [ ] Workflow type과 Lambda integration mode가 explicit reviewed config다.
- [ ] Database resource와 listing-based authority가 0이다.
- [ ] Pinned Failsafe `integration-test`+`verify`, `*IT` include, JUnit/fixture dependency와 active profile이 effective POM에서 exact하고 missing/profile-off/0-test가 non-zero다.

### 14.3 Storage, compute와 workflow

- [ ] Actual S3에서 put-if-absent/CAS/verified-read/conditional-policy/KMS가 통과했다.
- [ ] Immutable create/state/publication 각각의 412/409/deny/timeout/response-unknown이 exact reread/revalidate와 typed outcome으로 판정된다.
- [ ] Versioning/delete-marker/multipart/copy/body replay/tenant credential mode가 explicit ADR와 config에 있고 unsupported 조합은 fail-closed다.
- [ ] Same key/different digest와 corruption이 original bytes를 바꾸지 않는다.
- [ ] Concurrent writer는 exactly-one winner이고 rollback/cleanup이 current accepted version/pointer를 삭제하지 않는다.
- [ ] Lambda retry가 same logical identity와 new `AttemptId`만 사용한다.
- [ ] ASL에 objective/comparator/verifier/customer/completeness 계산이 없다.
- [ ] Missing worker, timeout, cancel, throttle, OOM, poison event가 false success를 만들지 않는다.
- [ ] Candidate/result verifier 모두 PASS한 result만 pointer에 연결된다.

### 14.4 Security, operations와 parity

- [ ] Role/tenant/KMS/network negative matrix가 actual AWS에서 모두 pass한다.
- [ ] Secret/PII/full payload forbidden match가 0이다.
- [ ] §11.6.1 required logs/metrics/alarms/traces/cost schema의 missing/duplicate/forbidden-field·dimension/wrong-unit/stale datum이 0이고 failure/retry/duplicate run도 포함한다.
- [ ] Local↔AWS canonical semantic fingerprint와 termination이 일치한다.
- [ ] Same envelope repeated run의 semantic trace/result가 일치한다.
- [ ] Immutable deploy provenance와 recoverable non-production rollback rehearsal이 있다.

### 14.5 Evidence와 authority

- [ ] Canonical required manifest SHA-256가 `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1`이고 canonical case expected/discovered/passed가 `64/64/64`다.
- [ ] Supplementary actual-S3 catalog SHA-256가 `279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81`이고 supplementary case expected/discovered/passed가 `29/29/29`다.
- [ ] `Phase11S3ActualCaseCatalogContractTest`가 29 unique case↔fault↔report mapping과 receipt schema의 literal `expectedSensitivityDisposition=RED`를 통과했다.
- [ ] Fresh auditor가 `|activatedSensitivity|=|observedRed|=29`를 확인했고 각 receipt의 case ID, unique fault ID, activation evidence, normal `PASS`, sensitivity `RED`/non-zero disposition이 동일 source/run/profile/deployment와 두 manifest digest에 결합됐다.
- [ ] 두 set을 독립 판정한 missing/duplicate/failure/error/skipped/renamed-unmapped/unexpected/stale가 모두 0이고 `sensitivity-missing=sensitivity-survived=wrong-sensitivity-mapping=∅`이며, evidence와 post-review exit 입력이 두 digest를 함께 봉인한다. Survived 또는 wrong disposition이 하나라도 있으면 exit는 non-zero다.
- [ ] Filter 없는 Phase 11 slice/profile이 `-am clean verify`로 실행됐고 selected test는 isolated full install 뒤 `-am` 없이 실행됐다.
- [ ] Pre-review evidence manifest가 reviewer/review/receipt를 역참조하지 않는다.
- [ ] Independent review verdict가 exit gate를 accepted했다.
- [ ] Post-review receipt가 evidence digest와 review digest를 단방향 참조한다.
- [ ] OPEN/GATED/deferred와 limitation이 그대로 기록됐다.
- [ ] `productionAuthority=false`다.

Source/test/IaC 존재, emulator green, stack create, happy path 한 번 또는 non-prod deployment만으로는 DoD가 아니다.

## 15. 다음 Phase handoff와 broken-contract 증상

### 15.1 Phase 12로 넘기는 것

```text
Phase11EvidenceManifest.contentDigest
Phase11PostReviewAcceptanceReceipt.contentDigest
logical port/artifact/state schema versions
identity/retry projection
storage/workflow/compute contract case catalog
local oracle + actual AWS evidence refs
security/operations/cost envelope
known AWS limitations/open gates
productionAuthority = false
```

Phase 12가 소유하는 `ProviderConformanceManifest`는 Phase 11 evidence/receipt를 forward-reference할 수 있다. Phase 11 evidence/receipt는 Phase 12 manifest를 역참조하지 않는다.

### 15.2 Phase 14B로 넘기는 것

- Phase 11 evidence/acceptance receipt
- Immutable deployment manifest와 source/artifact/IaC/ASL/policy digest
- Security/operations/cost/retention/quota limitation
- Rollback rehearsal와 last safe point

이것은 production entry input이다. Signed production authority, exact account/region/action/traffic/pointer scope와 승인자를 대신하지 않는다.

### 15.3 Broken-contract 증상

| 증상 | 의심할 계약 | 즉시 안전 행동 |
|---|---|---|
| Local/AWS result digest 불일치 | Serialization, hidden provider input, completion order | Candidate deploy/new start 중단, artifact 보존 |
| Missing worker인데 SUCCEEDED | Listing/history fan-in 또는 ASL completeness | Publication off, exact state audit |
| Same worker different digest 중 하나 선택 | Retry/reproducibility | Integrity incident, 둘 다 publish 금지 |
| Cancel과 publish가 모두 성공 | Same-state fence 누락 | New publication/start 중단, pointer/state reconcile |
| Stale action side effect 반복 | Pending action authorization/token leakage | Executor off, exact state/action audit |
| AccessDenied가 NotFound | Error taxonomy/tenant existence leakage | Adapter release 중단, negative matrix 재실행 |
| Timeout이 MAX_STEPS_REACHED | Deadline/termination 혼합 | Result invalidation, normal benchmark 제외 |
| Rollback 후 in-flight state 분실 | Revision/state reconciliation | New starts off, accepted local/previous 유지 |
| Cost/metrics가 일부 run만 존재 | Evidence cherry-pick/observability gap | Parity/production claim 중단 |
| Phase 12가 없는데 Phase 11 evidence가 그 manifest를 참조 | Evidence DAG cycle | Seal 폐기 후 standalone evidence 재생성 |

## 16. Source → requirement → WP → test/evidence traceability

아래 evidence key는 계획된 requirement이며 현재 완료 증거가 아니다.

| Source | Requirement | WP | Exact test/evidence |
|---|---|---|---|
| Master §4/§15.10 | Provider-neutral logical port 보존 | 11-0/1/4 | Leakage/action mapping; `E-P11-AWS-CONTRACT` |
| Canonical Domain §9~§16 | Pair/result/failure/two-gate 의미 보존 | 11-2/3/4/7/8 | Storage/fault/E2E + parity; `E-P11-PARITY` |
| Canonical Architecture §12~§18 | SDK/event/locator 격리, provider suite와 operations evidence | 11-1/5/6/7 | Leakage + effective POM + provider compatibility |
| Integrated §13 | Exact-key/immutable/CAS/no-DB | 11-2 | S3 contract/AwsIT |
| Integrated §14 | Declared completeness/retry/cancel | 11-3/4/7 | Missing worker/retry/cancel AwsIT |
| Integrated §15 | S3/Step Functions/Lambda mapping | 11-1~7 | Contract + actual AWS bundle |
| Question `Q-INFRA-01` | AWS는 selected target/reference | 11-0~8 | Manifest provider assertion |
| Question `Q-BENCH-02` | Official 수치 open 유지 | 11-1/6/8 | Config/manifest hidden-default test |
| Question `Q-VAR-01` | Variant deferred 유지 | 11-1/8 | Dependency/config absence test |
| Phase 08 direct handoff §7.3~§9.3/§16 | Port/access/failure/cancel/deadline owner contract | 11-0/2/3/4 | Accepted handoff receipt + signature compile test |
| Phase 09 direct handoff §7.2/§7.5~§9.6/§15 | Verified read/put-if-absent/CAS/publication/S3 boundary | 11-0/2 | `S3ObjectStorageBackendContractIT`, boundary receipt |
| Phase 09/10 reviews | Distinct publication precondition | 11-0/2/4/7 | `AwsPublicationFenceAwsIT` |
| Phase 10 §7~8 | Exact current pending action | 11-4/7 | `AwsActionReplayIT` |
| Phase 10/review | Same-state cancel/publish fence | 11-4/7 | `AwsCancellationIT` |
| Phase 10/review | Restart-safe durable deadline | 11-3/6/7 | `AwsDeadlineRestartIT` |
| Phase 11 §3/§6.2 | Explicit workflow/invocation mode | 11-1/4/7 | Config/template/mode AwsIT |
| Phase 11 §6.1/§8.2 | Provider-native conditional deny | 11-2/5 | S3 policy negative AwsIT |
| Phase 11 §6.1/§7.3 | 409/412, versioning/delete-marker, multipart/copy/body replay | 11-2/5/7 | `phase11-s3-actual-cases-v1` 29/29/29 + 29 activation↔red bijection + cleanup/original-byte oracle |
| Phase 11 §8 | IAM/KMS/tenant/network | 11-5 | `AwsIamBoundaryAwsIT`, network/redaction |
| Phase 11 §9~10 | Quota/DLQ/observability/cost | 11-6/7 | Exact schema completeness/forbidden/unit/freshness + fault IT |
| Phase 11 §12 | Required 64 + supplementary actual-S3 29 + local vs actual AWS 분리 | 11-1~8 | 두 manifest hash/독립 report set + survived/wrong mapping 0인 sensitivity receipts + parity/reproducibility |
| Phase 11 §13 | Deploy/rollback | 11-8 | `AwsDeploymentRollbackIT`, manifest |
| Master Plan §9 | Evidence 단방향 DAG | 11-8 | Manifest/review/receipt cycle 검사 |
| Phase 12 §15/§18 | Downstream-only conformance | 11-8 | Phase 11→Phase 12 back-reference 0 |
| Phase 13 gate | Optional hybrid 미적용 | 11-1/8 | OR-Tools/module/config absence |
| Phase 14 §3/§8 | Production authority 별도 | 11-8 | `productionAuthority=false`, no production command |

## 17. 구현자가 마지막으로 답할 자문 질문

1. AWS가 없어도 이 action/state/result의 의미를 설명할 수 있는가?
2. Exact key를 모르면 listing으로 찾지 않고 typed failure로 멈추는가?
3. Same key/different digest가 overwrite되지 않는가?
4. Side effect 직전에 exact current pending action을 다시 확인하는가?
5. Run-state token과 publication precondition이 code/type/test에서 분리되는가?
6. Cancel과 publish가 같은 authoritative state version에서 경쟁하는가?
7. Crash 뒤 deadline origin을 추측하지 않는가?
8. Missing worker와 provider timeout이 normal success가 될 경로가 0인가?
9. S3 409/412, current version/delete marker, multipart/copy와 body replay를 operation별로 exact reread/revalidate하는가?
10. IAM/KMS/network/tenant credential deny를 실제 AWS에서 검증했는가?
11. Canonical/supplementary digest와 report가 각각 64/64/64, 29/29/29이고 `|activatedSensitivity|=|observedRed|=29`, sensitivity missing/survived/wrong mapping 0인가?
12. Log/metric/alarm/cost field, forbidden dimension, unit와 freshness를 schema set으로 판정하는가?
13. Emulator, generated template, empty change set 또는 stale report를 evidence로 쓰지 않았는가?
14. Pre-review evidence가 downstream/review/receipt를 역참조하지 않는가?
15. Phase 13 optional과 Phase 14 production gate를 닫힌 채 보존했는가?
16. Rollback은 artifact/version 삭제가 아니라 new starts/pointer/alias와 exact reconciliation인가?
17. 총괄 scheduler 외 주체가 Phase status를 승격하지 않았는가?

하나라도 “모르겠다”면 해당 checkpoint에서 멈추고 blocker note를 작성한다. Phase 11의 목적은 AWS를 사용하는 것이 아니라 **AWS에서도 같은 RPDPTW 의미만 존재하도록 증명하는 것**이다.
