# Phase 11 — AWS reference distribution

```yaml
document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
document_version: 1.3
document_workflow_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
phase: "11"
phase_name: aws-reference-distribution
baseline_date: "2026-07-28"
implementation_status: NOT_STARTED
deployment_status: NOT_DEPLOYED
production_authority: NOT_GRANTED
evidence_status: NOT_PRODUCED
phase_acceptance_status: BLOCKED_NOT_IMPLEMENTED
review_status: COMPLETE_CHANGES_REQUIRED
review_document: ../reviews/phase-11-review.md
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS_AND_AWS_INTEGRATION_GATE
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_status: ALNS_FIRST_GATE_OVERLAY_APPLIED_DOCUMENTATION_ONLY
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: AWS adapter/distribution owner role
  application_contract: Phase 08 Application/Local Runtime owner role
  storage_contract: Phase 09 Object Storage owner role
  coordinator_contract: Phase 10 Provider-neutral Coordinator owner role
  security: Platform Security/IAM/KMS owner role
  operations: AWS Platform/SRE owner role
  cost: FinOps/Platform owner role
  independent_integration_oracle: Phase 11 provider-integration test owner role
  downstream_conformance: Phase 12 Provider Substitution owner role
  downstream_cutover: Phase 14 Calibration/Cutover owner role
  review: independent Phase 11 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture and provider-leakage rules
  - Phase 06 accepted deterministic WorkerRun semantics
  - Phase 07 accepted both-gate PublishableResult contract
  - Phase 08 accepted provider-neutral application ports and local reference runtime
  - Phase 09 accepted object-storage, exact-key, digest and CAS contracts
  - Phase 10 accepted state/action, declared-completeness, retry and cancellation contracts
  - reviewed AWS resource/IAM/encryption/retention/quota/cost ADRs
  - approved isolated non-production AWS integration environment
planned_evidence:
  - E-P11-AWS-CONTRACT
  - E-P11-PARITY
  - E-P11-SECURITY
  - E-P11-OPERATIONS
  - E-P11-ROLLBACK
source_sections:
  canonical_master: "§1~4, §13~17; especially §15.10 RM-8 and §16.2~16.3"
  final_domain: "§7~10, §15~18; immutable authority and verified-result meaning only"
  final_architecture: "§2, §3, §5, §6; provider-neutral ports, identity, state, security and evidence"
  integrated_design: "§3, §12~16, §19~25, §26.3~26.4, §27~28"
  open_questions: "Q-BENCH-02, Q-INFRA-01, Q-VAR-01 and §3~4 status/gates"
  implementation_readme: "§1~7"
  master_realization_plan: "§2~4, §6, Phase 08~12 and 14, §8~15"
  phase_08_actual: "§7.3~7.5, §9.1~9.3 and §16.2~16.4"
  phase_09_actual: "§7.2, §7.5~7.8, §8.1~8.7, §9.1~9.6 and §15.2~15.3"
  phase_10_actual: "v1.3 §6.3~7.4, §8.6, §13 and §14.2; reviewed corrections and residual handoff blockers"
  phase_12_actual: "§1~4, §15.1 and §18.2; downstream conformance consumer only"
  root_readme_and_inventory: "README technology/deployment/placeholder; tracked GCP/Docker/Java files; ignored generated serverless inventory"
canonical_source_fingerprints_sha256:
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/deprecated/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/deprecated/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/deprecated/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
inventory_fingerprints_sha256:
  README.md: 22eff4f63607db29bd4049344986109c680aa970d0865a3b859598e6b3b96c06
  pom.xml: f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6
  Dockerfile: 2aae6615e6c3dba5184064593e29407f7cded4e63dd416719a0b2fb30846d985
  gcp/README.md: 8294e4c3bbb7b92b0d7f51136b18aaa342fe3c9f06ec822dc23ce5a75c915913
  gcp/cloudbuild.yaml: 284663c833bb3550497f412e507e25a4d12b64f4bcae0ad993c9bd576f76b209
  gcp/workflows/optimization.yaml: 65eeef9344a63b743af1684b915c9bdfa976454e560ebb8624d75ed00fd874c1
  AlnsBatchEngine.java: 4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481
  OptimizationApiController.java: 3dcab11fcac78bb994b1f77bedaf425601684e8b9b3eb1560c305dcf671f358c
  OptimizationWorkerController.java: 846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c
  AlnsBatchEngineTest.java: 947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829
  ignored_.serverless/serverless-state.json: 571994fe5967ab483318dd8dfc9fcaef67676769890f1b49c51339658df0400e
  ignored_.serverless/cloudformation-template-update-stack.json: c48e056dcf45b3c7e0cabfc7cec7d491c24ec190c94f567709ee55bbf749811a
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
authoring_time_neighbor_snapshot:
  snapshot_status: HISTORICAL_SNAPSHOT_NOT_LIVE_STATUS
  phase_08: ACTUAL_READY_FOR_REVIEW_NOT_STARTED_APPEARED_DURING_AUTHORING
  phase_09: ACTUAL_V1_1_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_HANDOFF_NOT_READY
  phase_10: ACTUAL_V1_1_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_HANDOFF_NOT_READY
  phase_12: ACTUAL_READY_FOR_REVIEW_NOT_STARTED_HANDOFF_NOT_READY
  phase_11_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
live_document_inventory:
  phase_documents_present: 15_OF_15
  phase_reviews_present: 15_OF_15
  phase_reviews_complete: 15_OF_15
  phase_11_document_verdict: CHANGES_REQUIRED
  phase_11_implementation_status: NOT_STARTED
  phase_11_evidence_status: NOT_PRODUCED
  phase_11_acceptance_status: BLOCKED_NOT_IMPLEMENTED
neighbor_fingerprint_policy: CANONICAL_FINGERPRINTS_AND_STABLE_SECTION_CITATIONS_ONLY
```

## 1. 문서 지위, 권위와 source 해석

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. Source 문서의
`REVIEW` metadata는 provenance로 보존하지만 Phase 11 상세 문서 작성을 멈추는
조건이 아니다. 반대로 이 문서의 type, test, IAM matrix 또는 IaC tree가
구체적이라는 사실은 구현, AWS 배포, integration evidence, production cutover
authority가 생겼다는 뜻이 아니다.

상태 축을 분리한다.

| 축 | 현재 값 | 의미 |
|---|---|---|
| Source decision | `Q-INFRA-01 RESOLVED` | AWS S3 + Step Functions + Lambda가 선택된 target/reference다. |
| 문서 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS` | 독립 문서 리뷰가 완료됐고 안전 교정이 반영됐지만 verdict는 `CHANGES_REQUIRED`다. |
| Phase document inventory | `15/15 PRESENT` | Phase 00~14 상세 문서가 모두 존재한다. |
| Phase review inventory | `15/15 COMPLETE` | Phase 00~14 review가 모두 완료됐지만 completion은 acceptance가 아니다. |
| 구현 | `NOT_STARTED` | Target module/type/test가 존재한다고 주장하지 않는다. |
| AWS integration | `NOT_RUN` | 실제 AWS account/region에서 생성·호출·삭제 evidence가 없다. |
| 배포 | `NOT_DEPLOYED` | `.serverless` 생성물이나 GCP guide는 AWS 배포 증거가 아니다. |
| Production authority | `NOT_GRANTED` | AWS 선택은 production 배포·트래픽·cutover 승인이 아니다. |
| Phase review | `COMPLETE — CHANGES_REQUIRED` | [독립 리뷰](../reviews/phase-11-review.md)는 완료됐지만 residual cross-phase blocker와 implementation/evidence gate가 남는다. |
| Phase acceptance | `BLOCKED_NOT_IMPLEMENTED` | 구현/evidence와 accepted post-review receipt가 없어 Phase 11 acceptance는 미완료다. |
| Handoff | `NOT_READY` | Phase 12/14가 소비할 accepted evidence manifest가 없다. |

권위 적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../2026-07-31-phase-b-master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 의미와 불변조건
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/DAG/port 배치
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 15 Phase, no-DB와 AWS mapping
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는
누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 역사 자료일 뿐 현재
authority, AWS topology 또는 evidence로 사용하지 않는다. Final Architecture
Final Domain §18과 Final Architecture §6 말미에 남은 `Q-INFRA-01 DEFERRED`,
`25/1/2` 표기는 최신 Canonical Master와 질문 등록부의
`Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. Source drift 자체는 해당 문서
owner의 잔여 정합화 항목이며 Phase 11이 그 두 파일을 수정하거나 다른 결정을
만들지 않는다.

### 1.1 직접 소비한 section

| Source | 직접 소비한 section | Phase 11에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | §1~4, §13~17 | AWS 선택과 구현/cutover 분리, logical port, artifact/identity, replay, RM-8, migration/rollback |
| [Final Domain](../../2026-07-26-domain-design.md) | §7~10, §15~18 | Problem/travel/profile/result의 provider-neutral semantic identity와 verifier gate |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2~3, §5~6 | Java 25/Maven DAG, port owner, distributed state, retry/cancel, security/telemetry/test |
| [Integrated design](../../architecture-domain-implementation-design.md) | §12~16, §19~25 | Phase 08~12 contract, S3/Step Functions/Lambda mapping, no-DB, failure/test/anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01`, §3~4 | AWS target resolved, 공식 실행 수치 open, optional variant deferred, cutover 별도 gate |
| [Master Realization Plan](../master-realization-plan.md) | §2~4, Phase 08~12/14, §8~15 | Current inventory, Phase 11 entry/exit/evidence/DoD, production blocker와 handoff |
| [구현 문서 지도](../README.md) | §1~7 | Canonical filename, planned/actual, scheduler/review 권한 |
| [Actual Phase 08](phase-08-application-ports-local-runtime.md) | v1.3 §7.3~§7.5, §9.1~§9.3, §16.2~§16.4 | `REVIEWED_WITH_CORRECTIONS`/`CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_READY`; application port, idempotency/deadline와 local rollback handoff |
| [Actual Phase 09](phase-09-object-storage-no-database.md) | v1.3 §7.2, §7.5~§7.8, §8.1~§8.7, §9.1~§9.6, §15.2~§15.3 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS`/`CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_READY`; exact storage/CAS contract와 Phase 11 backend handoff |
| [Actual Phase 10](phase-10-provider-neutral-coordinator.md) | v1.3 §6.3~§7.4, §8.6, §13, §14.2 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS`/`CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_READY`; pending action/publication/cancel/deadline blocker와 Phase 11 handoff |
| [Actual Phase 12](phase-12-provider-substitution.md) | v1.3 §1~§4, §15.1, §18.2 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS`/`CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_READY`; Phase 11 evidence/receipt의 downstream consumer이고 adoption/cutover는 별도 gate |
| [Root README](../../../../README.md) | 기술 기준·배포·placeholder | 실제 Java/GCP/Docker current state와 목표 AWS를 분리 |

현재 live inventory는 Phase 상세 문서 `15/15 PRESENT`, 독립 review
`15/15 COMPLETE`다. Phase 08~12도 모두 review correction이 반영됐지만 review
completion이나 문서 존재는 implementation/evidence/acceptance authority가 아니다.
특히 Phase 11 verdict는 `CHANGES_REQUIRED`, 구현은 `NOT_STARTED`, evidence는
`NOT_PRODUCED`, acceptance는 `BLOCKED_NOT_IMPLEMENTED`다.

Metadata의 `authoring_time_neighbor_snapshot`은 작성 도중 관찰한 과거 상태로
보존하며 live status로 사용하지 않는다. 당시 Phase 09/10 동시 review를 관찰한
사실도 historical snapshot에 한 번만 남기고 neighbor status/hash를 acceptance
조건으로 반복 추적하지 않는다. 인접 문서는 위 표의 안정된 section을 구현
entry에서 직접 다시 읽고 semantic compatibility receipt를 만든다.
인접 whole-file/section digest나 reciprocal fingerprint는 acceptance 조건으로
사용하지 않는다. Metadata fingerprint는 사용자 지정 canonical 4문서에만 두고,
보조 등록부·계획·인접 Phase는 안정된 section/anchor 인용으로 대조한다.

## 2. 목표, 범위, 비범위와 불변조건

### 2.1 목표

Phase 11은 Phase 09의 object-storage port를 S3에, Phase 10의 durable
coordination을 Step Functions에, worker dispatch/compute를 Lambda에 매핑한다.
매핑 뒤에도 local reference와 동일한 logical manifest, exact artifact identity,
state transition, declared-worker completeness, retry/cancel classification, 두
verifier와 publication eligibility가 보존되어야 한다.

```text
Phase 09 ArtifactStore/RunStateRepository/ResultPublisher
    → S3 conditional object adapter

Phase 10 WorkflowScheduler/WorkerDispatcher + provider-neutral action
    → Step Functions thin workflow + Lambda adapter

Phase 10 ExecuteWorkerRun
    → Worker Lambda handler

same logical input + same immutable manifest
    → same canonical artifact/result fingerprints or same typed termination
```

AWS가 이 system의 semantic owner가 되어서는 안 된다. Step Functions execution
history, Lambda completion order, S3 listing, ARN, region, retry count와 clock은
objective, seed, worker completeness, result fingerprint 또는 publication
eligibility의 hidden input이 아니다.

### 2.2 포함 범위

- S3 `ObjectStorageBackend`와 Phase 09 port mapping
- Immutable put-if-absent, verified read, versioned CAS와 publication pointer
- Tenant-scoped logical key namespace와 adapter-only bucket/key/ARN locator
- API/coordinator/worker Lambda inbound adapter와 event/version contract
- Step Functions의 command/wait/wakeup/retry/cancel forwarding mapping
- `WorkerRunId`/`AttemptId` 보존, duplicate event와 same/different digest 수렴
- IAM least-privilege role 분리와 explicit deny/negative integration tests
- At-rest/in-transit encryption, KMS/key rotation/access evidence contract
- Ingress/egress/service-access topology와 public exposure를 분리한 network contract
- Concurrency, deadline, retry, quota와 payload-size preflight contract
- DLQ 또는 동등한 durable failure reconciliation과 poison-event 격리
- 구조화 logs, bounded-cardinality metrics, distributed trace와 redaction
- 제안 AWS SAM/CloudFormation IaC tree, immutable build/deployment provenance
- Isolated integration stage 배포, parity/shadow/fault/rollback rehearsal
- Cost/resource/retention evidence와 production authority를 분리한 gate
- Phase 12-owned conformance manifest가 필요 시 forward-reference할 immutable AWS
  reference evidence와 post-review acceptance receipt

### 2.3 명시적 비범위

- Core/domain/solver/verifier/application 의미, port 또는 state machine 재설계
- Phase 09 object-storage 의미나 Phase 10 champion/completeness 로직 재구현
- S3 prefix list, Step Functions branch 또는 Lambda handler에서 champion 선택
- Database, DynamoDB table, cache table, queue consumer state를 authority로 추가
- Public API/wire schema의 최종 승인과 legacy `gs://` API의 묵시적 호환
- Official `screenMaxSteps`, worker 수, `phase2MaxSteps`, `maxRounds`, watchdog 수치
- Lambda memory/timeout/reserved concurrency, Step Functions concurrency, retention,
  cost cap 등의 미측정 production 기본값
- Phase 12 provider abstraction 재설계, GCP/ECS/Kubernetes 구현 또는 provider cutover
- `C-17 GATED TARGET`인 Phase 13 route pool/MIP과 optional OR-Tools/native
  backend 구현·활성화
- Phase 14 official benchmark, production deployment, traffic shift 또는 cutover 승인
- Multi-trip/rotation, `Q-VAR-01` 또는 다른 deferred variant
- Ignored `.serverless` 생성물의 source IaC 승격 또는 DynamoDB 재사용

### 2.4 고정 불변조건

1. Core/solver/verification/application에서 AWS SDK, Lambda event, ARN, region reference는 0이다.
2. Application business state의 authoritative store는 Phase 09 immutable artifact +
   단일 CAS state/pointer이며 database는 사용하지 않는다.
   CloudFormation stack state, Step Functions execution history와 CloudWatch
   telemetry는 provider control-plane/observation일 뿐 business database나
   recovery authority로 읽지 않는다.
3. S3 bucket/key/version/ETag는 adapter-private locator/token이며 domain/result identity가 아니다.
4. Artifact는 create-once이고 same key/same digest만 idempotent하게 수렴한다.
5. Same key/different digest와 same logical worker/different verified digest는 integrity failure다.
6. S3 listing과 event arrival은 wake-up hint일 뿐 declared completeness의 authority가 아니다.
7. Step Functions는 command, wait, wakeup, platform retry/timeout/cancel 전달만 소유한다.
8. Step Functions ASL에는 customer, constraint, score, comparator, verifier,
   publication eligibility branch가 없다.
9. Provider side effect는 Phase 10 state에 stable `ActionId`와 pending action이
   먼저 CAS된 뒤에만 실행하며 retry는 같은 action identity로 수렴한다.
10. Lambda handler는 event를 typed application command로 mapping하고 core 의미를 해석하지 않는다.
11. Retry는 `AttemptId`와 provider observation만 바꾸며 seed, warm start, requested
    steps, `WorkerRunId`, manifest를 바꾸지 않는다.
12. Lambda remaining time/platform timeout은 normal `MAX_STEPS_REACHED`가 아니다.
13. 한 declared worker라도 정상 완료와 candidate verification을 충족하지 못하면
    round는 `INCOMPLETE`다.
14. Candidate/result verifier 두 gate를 통과하지 않은 object/pointer는 publish하지 않는다.
15. IAM role은 API, coordinator, worker, deployment/test로 분리하고 wildcard data access를 금지한다.
16. Encryption은 digest 검증을 대체하지 않고 digest는 access control/encryption을 대체하지 않는다.
17. Secret, raw address/PII, input/result body는 log, metric dimension, trace attribute에 넣지 않는다.
18. Platform quota에 맞지 않는 manifest는 worker 수를 조용히 줄이지 않고 dispatch 전 typed failure로 차단한다.
19. DLQ/failure destination/message는 재처리 신호이며 application state나 result authority가 아니다.
20. Deployment revision, Lambda alias, state-machine execution ID는 run metadata일 수
    있지만 semantic fingerprint의 hidden input이 아니다.
21. Production apply/cutover는 Phase 11 문서나 non-production integration pass만으로 허용되지 않는다.

## 3. 결정 상태와 수치 gate

| 항목 | 상태 | Phase 11 처리 |
|---|---|---|
| S3 + Step Functions + Lambda 선택 | `RESOLVED` | Target/reference mapping으로 구현한다. |
| No-database | `USER-CONSTRAINT/FIXED` | DynamoDB, RDS 또는 provider KV를 business authority로 쓰지 않는다. |
| Provider-neutral dependency direction | `FIXED` | SDK/IaC는 adapter/distribution/deployment에만 둔다. |
| S3 canonical key layout/encoding | `PROPOSED/ADR_REQUIRED` | §7 baseline을 review하고 Phase 09 contract와 함께 고정한다. |
| S3 CAS token/conditional API mapping | `PROPOSED/PROVIDER_INTEGRATION_REQUIRED` | Emulator가 아니라 실제 S3 concurrency evidence가 필요하다. |
| IaC tool | `PROPOSED` | AWS SAM/CloudFormation baseline. ADR가 다른 tool을 승인해도 file ownership/test semantics는 유지한다. |
| Step Functions workflow type/Lambda integration mode | `OPEN/ADR_REQUIRED` | `STANDARD`/`EXPRESS`와 synchronous/asynchronous/callback mode는 duration, delivery, retry, callback 가능 여부를 바꾸므로 명시한다. AWS default를 쓰지 않고 선택 조합의 semantics를 actual integration evidence로 검증한다. |
| Encryption mode/key lifecycle | `PROPOSED SECURITY BASELINE` | Stage별 customer-managed symmetric KMS key + TLS를 제안한다. Exact key/rotation/retention은 Security review가 필요하다. |
| Network topology/VPC endpoint | `OPEN/ADR_REQUIRED` | API ingress auth/TLS, coordinator/worker public ingress 금지와 approved egress allowlist는 고정한다. VPC 배치/endpoint/NAT 선택은 workload/security evidence 뒤 정한다. |
| Tenant IAM partition mode | `OPEN/SECURITY_ADR_REQUIRED` | Tenant-scoped temporary session/access point 또는 dedicated tenant resource 중 하나를 승인한다. Shared role의 unrestricted `tenants/*` access를 application check만으로 보완하는 방식은 금지한다. |
| DLQ/failure handling product | `PROPOSED/REVIEW_REQUIRED` | Authoritative failure artifact + exact state reconciliation은 고정. Invocation mode에 따라 SQS DLQ/failure destination 사용 여부를 ADR에서 고정한다. |
| Lambda/Step Functions/S3/KMS quota 수치 | `OPEN/ENVIRONMENT_MEASURED` | 배포 preflight와 evidence에 explicit value가 필수이며 hidden default 금지다. |
| Lambda memory/timeout/ephemeral storage/concurrency | `OPEN/PROPOSED PER STAGE` | Test/integration 값은 manifest에 `test-only`로 표시한다. Production 수치로 승격하지 않는다. |
| Log/trace retention과 cost cap | `OPEN/OWNER_APPROVAL_REQUIRED` | Security/Operations/FinOps가 stage별 승인하기 전 production gate를 열지 않는다. |
| `Q-BENCH-02` official 수치 | `OPEN — EXPERIMENT_REQUIRED` | Test fixture 값만 explicit하게 사용하고 official run/cutover를 주장하지 않는다. |
| Phase 12 target provider | `GATED/UNSELECTED` | Phase 11 AWS evidence/receipt만 handoff하고 Phase 12-owned conformance manifest, adapter 또는 cutover를 당기지 않는다. |
| `C-17` optional hybrid | `GATED TARGET` | Phase 13 route pool/MIP/OR-Tools dependency/native distribution을 만들거나 AWS distribution에서 활성화하지 않는다. |
| `Q-VAR-01` | `DEFERRED` | 질문·활성화·AWS config 반영 금지다. |
| Production deployment/cutover | `AUTHORITY GATED` | Phase 14 조건과 explicit production approval 전 금지다. |

## 4. Entry gate와 확인 evidence

문서 review, test fixture 설계와 offline IaC validation은 진행할 수 있다. 실제 Phase
11 구현/통합 환경 배포/evidence 착수와 `ACCEPTED` 주장은 아래 AND gate가
충족될 때까지 차단한다.

| Entry 항목 | 필요한 확인/evidence | 2026-07-28 local 관찰 | 판정 |
|---|---|---|---|
| Phase 00 architecture | Accepted review, AWS SDK leakage 0, target Maven reactor | Root 단일 POM과 GCP SDK 혼합 classpath | BLOCKED |
| Phase 06 worker | `E-P06-*`, exact WorkerRun/termination/replay | 상세만 존재; accepted evidence 없음 | BLOCKED |
| Phase 07 publication | `E-P07-*`, immutable `PublishableResult`, two-gate rejection | 상세만 존재; accepted evidence 없음 | BLOCKED |
| Phase 08 ports/local | Accepted `ArtifactStore`, workflow/dispatcher, cancel/telemetry와 local E2E oracle | Actual v1.3 review는 `COMPLETE`/`CHANGES_REQUIRED`; implementation/evidence/handoff 없음 | BLOCKED |
| Phase 09 storage | Accepted exact-key/digest/CAS/tenant suite와 encoding/key-layout ADR | Actual v1.3 review는 `COMPLETE_CHANGES_REQUIRED`; implementation/evidence/handoff 없음 | BLOCKED |
| Phase 10 coordinator | Accepted state/action/completeness/retry/cancel contract와 fake oracle | Actual v1.3 review는 `COMPLETE_CHANGES_REQUIRED`; implementation/evidence/handoff 없음. Publication precondition, same-run-state cancel fence와 durable deadline은 cross-phase blocker | BLOCKED |
| AWS ADR | Resource boundary, IAM, KMS, network, key layout, retry/DLQ, quota, retention, cost | 승인 기록 없음 | REVIEW_REQUIRED |
| Integration environment | Isolated account/role/region, budget guard, deploy/cleanup owner | 권한·environment evidence 없음 | AUTHORITY_GATE |
| Production authority | Phase 14 gate + Security/Ops/Product approval | 없음 | BLOCKED |
| Scheduler/owners | Task ID와 named accountable owners | `TBD_NOT_SUPPLIED` | OWNER_GATE |

Entry receipt는 최소 다음 manifest를 가져야 한다.

```text
Phase11EntryReceipt
  phase00ArchitectureEvidenceRef
  phase06WorkerContractRef
  phase07PublishableResultContractRef
  phase08PortAndLocalOracleRef
  phase09StorageContractRef
  phase10CoordinatorContractRef
  reviewedAwsAdrRefs[]
  integrationAccountAlias          # account number를 문서/log에 노출하지 않음
  integrationRegion
  deployRoleArnRef                  # secret/credential value가 아닌 approved handle
  budgetGuardRef
  approvedBy[]
```

누락 entry를 legacy DTO, mock 성공, generated CloudFormation, AWS Console 화면 또는
임시 default로 보완하지 않는다.

## 5. Current inventory와 migration 분류

Read-only inventory 기준 commit은
`3424277c9c74f8151a83be056a07dd4659331beb`이다. 이 문서 작성 전
`docs/implementation/` 전체는 Git 기준 untracked였고 기존 사용자 작업으로
보존한다.

| 실제 항목 | 관찰 | Phase 11 분류 |
|---|---|---|
| Root Maven | 단일 `pom.xml`, Google Workflow Executions/Storage SDK와 Jackson가 같은 project | Legacy placeholder characterization; AWS/core target DAG evidence 아님 |
| Java source | `com.ronext.optimizer` 6개 main file | Target `com.ronext.rpdptw`/adapter module 없음 |
| `AlnsBatchEngine` | 입력을 풀지 않고 seeded `double` 합성 objective 반환 | Solver/worker/parity oracle로 사용 금지 |
| GCP API/worker | `gs://` URI, Workflow execution, Cloud Storage list/finalize | Migration/rollback inventory. Listing/partial-success 의미는 target authority가 아님 |
| `gcp/workflows/optimization.yaml` | 병렬 HTTP worker와 finalize 호출 | Legacy orchestration characterization only |
| `gcp/cloudbuild.yaml`, `gcp/README.md` | Docker build/Cloud Run/Workflows/GCS 배포 guide | 실제 배포·production evidence가 아니며 AWS IaC source가 아님 |
| `Dockerfile` | Maven build + Corretto 25 runtime | Runtime/build input inventory. Lambda packaging evidence 아님 |
| Ignored `target/` | 과거 build output | Reproducible distribution/evidence로 계산하지 않음 |
| Ignored `node_modules/` | Serverless/AWS 관련 package 포함 | Dependency/source manifest가 없으므로 target dependency authority 아님 |
| Ignored `.serverless/` | 생성된 state/CloudFormation에 API Gateway, 7 Lambda, Step Functions, S3, IAM/Logs와 **DynamoDB table** 포함 | Historical generated artifact only; no-DB 위반이며 source IaC/배포 증거로 복사 금지 |
| `serverless.yml`/package manifest | 추적 파일로 존재하지 않음 | `.serverless`를 재현하거나 검증할 source가 없음 |
| Accepted AWS evidence | 없음 | Phase 11 implementation/deployment completion 0 |

Ignored `.serverless` artifact는 rollback inventory에도 content snapshot으로만 남긴다.
그 안의 state machine, Lambda split, IAM 또는 DynamoDB를 새 reference 설계로
옮기지 않는다. 실제 stack 존재 여부, account, region, deployment success를
추론하지 않는다.

### 5.1 Proposed change tree

아래는 미래 구현 target이며 현재 파일 존재를 주장하지 않는다.

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
│   ├── api-role-policy.json
│   ├── coordinator-role-policy.json
│   ├── worker-role-policy.json
│   └── integration-test-role-policy.json
├── parameters/
│   ├── integration.json
│   └── production.example.json        # values only; no production authority/secret
├── alarms/
│   └── required-alarms.yaml
└── local/
    └── docker-compose.yml

build/port-contract-tests/
└── src/test/java/com/ronext/rpdptw/contract/aws/

build/test-fixtures/src/main/resources/aws/
├── events/v1/
├── manifests/
├── state/
├── failures/
├── iam/
└── observability/
```

### 5.2 Compile dependency direction

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
  ↑
deployment/aws  # compile dependency가 아니라 packaging/config only
```

금지 방향:

```text
core/solver/verification/application -X-> software.amazon.awssdk.*
core/application                    -X-> Lambda event/Context
application                         -X-> S3 bucket/key/ETag/ARN
Step Functions ASL                 -X-> objective/comparator/verifier meaning
deployment/aws                     -X-> semantic defaults or customer branches
```

## 6. Java와 infrastructure contract

모든 이름은 **PROPOSED INTERNAL**이다. Phase 08~10 handoff review에서 의미를
보존하는 범위로 조정할 수 있지만 provider type 누출과 owner 방향은 바꿀 수 없다.

### 6.1 Phase 09 storage mapping

```java
// adapters/object-s3
public final class S3ObjectStorageBackend implements ObjectStorageBackend {
    public BackendCapabilities capabilities();
    public BackendGetResult getExact(ObjectKey key);
    public BackendPutResult putIfAbsent(
            ObjectKey key,
            ObjectWriteSource source,
            ExpectedObjectIntegrity expected);
    public BackendCasResult compareAndSet(
            ObjectKey key,
            ObjectVersionToken expectedVersion,
            ObjectWriteSource replacement,
            ExpectedObjectIntegrity expectedReplacement);
    public BackendMetadataResult metadataExact(ObjectKey key);
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
        ChecksumPolicy checksumPolicy,
        RetryPolicyId sdkRetryPolicy,
        Duration operationDeadline) {}

final class S3ObjectKeyLayout {
    ObjectKey toObjectKey(ArtifactKey key);
    ObjectKey stateKey(TenantId tenantId, SolveId solveId);
    ObjectKey publicationKey(TenantId tenantId, SolveId solveId);
}
```

`S3ObjectStorageBackend`만 AWS SDK의 request/response/exception을 본다. Phase 09의
`ObjectArtifactStore`, `ObjectRunArtifactRepository`, `ObjectRunStateRepository`,
`ObjectResultPublisher`는 provider-neutral contract를 유지한다.

`capabilities()`는 최소 `atomicCreateIfAbsent=true`,
`atomicCompareAndSet=true`, `exactReadAfterCommittedResult=true`와
`executionScope=DISTRIBUTED_PROVIDER`를 actual AWS evidence로 증명해야 한다.
Required capability를 check-then-put, list-then-decide, process-local lock 또는
last-write-wins wrapper로 흉내 내지 않는다.

Conditional mapping baseline:

| Logical operation | AWS adapter behavior | Success | Conflict/failure |
|---|---|---|---|
| Immutable create | Exact key + conditional create + expected checksum/digest metadata | New object ref | Existing same digest는 converge, different digest는 integrity conflict |
| Verified read | Exact key, size/schema/digest 확인 후 bytes 노출 | `ReadableArtifact` | Missing, checksum/digest/schema mismatch는 typed failure |
| State CAS | Read에서 받은 opaque version token으로 conditional replacement | New opaque token | Stale token은 `StateConflict`; reload/revalidate |
| Publication CAS | Publishable result ref만 conditional pointer commit | Same digest converge | Different result digest는 `PublicationConflict` |

Provider version/ETag/checksum 값 자체를 application fingerprint에 넣지 않는다.
실제 conditional header/API와 versioning 조합은 Phase 09 ADR와 실제 S3
integration test가 확정해야 하며 emulator pass만으로 완료하지 않는다.

Source IaC의 bucket policy는 immutable artifact prefix의 create에
`If-None-Match`, state/publication prefix의 create/update에 해당 operation의
`If-None-Match` 또는 `If-Match`가 없으면 write를 거부해야 한다. Adapter가
conditional header를 빠뜨린 경로를 application test만으로 막지 않는다.
`CopyObject`/multipart처럼 conditional-policy 조합에 별도 제약이 있는 API는
명시적 integration test와 ADR 없이 migration shortcut으로 쓰지 않는다.

### 6.2 Workflow와 compute mapping

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

// Phase 08 owns this port; AWS module implements the accepted signature.
final class StepFunctionsWorkflowExecutionAdapter
        implements WorkflowExecutionPort {
    public WorkflowStartReceipt start(LogicalWorkflowRequest request);
    public WorkflowExecutionStatus getStatus(LogicalWorkflowRunId runId);
    public WorkflowCancelReceipt requestCancel(
            LogicalWorkflowRunId runId,
            CancellationId cancellationId);
    // State machine ARN, execution name and SDK DTO remain private.
}

// Phase 10 action executor; not a second semantic coordinator.
final class StepFunctionsCoordinatorActionExecutor {
    ActionExecutionReceipt execute(CoordinatorAction action);
}

// adapters/compute-aws-lambda
final class LambdaWorkerDispatcher implements WorkerDispatcher {
    public DispatchReceipt dispatch(WorkerAssignmentRef assignment);
    public WorkerDispatchStatus status(WorkerRunId workerRunId);
    public CancellationReceipt requestStop(WorkerRunId workerRunId);
}
```

`AwsWorkflowRuntimeConfig`의 mode 필드는 nullable/defaultable하지 않다. 선택한
workflow type과 Lambda integration mode가 callback, execution duration,
delivery/retry와 execution-history 요구를 만족하는지는 reviewed ADR와 actual
AWS test로 증명한다. Incompatible combination은 IaC validation에서 fail-closed하고
다른 mode로 조용히 fallback하지 않는다.

Phase 08/09/10 actual 문서의 proposed signatures에는 `WorkerAssignment` 대
`WorkerAssignmentRef`, `StateVersion` 대 `RepositoryVersion`, cancellation ID
argument 등 이름/shape drift가 있다. 세 문서는 아직 accepted가 아니다. Phase
11이 임의 adapter overload나 `Object` bridge로 이를 봉합하지 않는다. WP11-0/1의
cross-phase receipt가 exact accepted signature와 compatibility projection을
고정한 뒤 하나만 구현한다. 위 code는 Phase 10의 명시적 Phase 11 handoff 이름을
AWS mapping 관점에서 보여주는 proposed projection이다.

`LambdaWorkerDispatcher.status`가 필요하면 exact action receipt와 Phase 09의
committed worker state를 읽어 logical status를 반환한다. Lambda invocation list,
CloudWatch log 존재, function completion order 또는 DLQ count로 worker success를
추론하지 않는다.

`CoordinatorAction`은 opaque S3/run-state version token을 semantic identity나
payload로 운반하지 않는다. Executor는 side effect 직전에 exact current state의
pending `ActionId`, payload fingerprint와 authorizing transition ordinal을 모두
대조한다. Run-state CAS token은 repository equality/CAS result에만 남고 Lambda
event, Step Functions input 또는 action fingerprint로 직렬화되지 않는다.

```java
// adapter-only versioned envelopes
record AwsCommandEnvelopeV1(
        String eventVersion,
        String tenantHandle,
        String solveId,
        String commandRef,
        String manifestFingerprint,
        String correlationId) {}

sealed interface AwsComputeEventV1
        permits AwsPhaseOneScreenEventV1, AwsWorkerEventV1 {}

record AwsPhaseOneScreenEventV1(
        String eventVersion,
        String screenAssignmentRef,
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

Handler boundary:

```java
ApiLambdaHandler
  AWS request envelope
  → validate version/size/auth context
  → SubmitSolve
  → map typed response

CoordinatorLambdaHandler
  AwsCommandEnvelopeV1
  → AdvanceSolve
  → one provider-neutral action
  → AwsActionMapper

WorkerLambdaHandler
  AwsComputeEventV1
  → PhaseOne event: readVerified(PhaseOneScreenAssignmentRef)
                    → ExecutePhaseOneScreen
  → Worker event: readVerified(WorkerAssignmentRef)
                  → ExecuteWorkerRun
  → immutable candidate/report put
  → CompletePhaseOneScreen or CompleteWorkerRun exact CAS
```

Handler는 `Lambda Context`를 solver에 넘기지 않는다. Deadline은 application의
platform observation/cancellation input으로만 전달하고 정상 algorithm budget과
분리한다.

### 6.3 State/ASL mapping

Phase 10 state가 authority다.

```text
SUBMITTED
→ PREPARING(
     snapshot
     → PhaseOneDispatching
     → PhaseOneRunning
     → PhaseOneAggregating
     → PhaseOneComplete)
→ PREPARED
→ ROUND_DISPATCHING → ROUND_RUNNING
→ ROUND_VERIFYING → ROUND_AGGREGATING
→ ROUND_DISPATCHING | FINALIZING
→ PUBLISHING → SUCCEEDED
```

`PUBLISHING` state CAS는 Phase 10의 publication authorization fence다.
Cancellation intent object는 durable request/hint이지 terminal fence가 아니다.
같은 run-state expected version에서 `CANCEL_REQUESTED`와 `PUBLISHING` CAS가
경쟁하고 성공한 하나만 terminal intent를 권위화한다. `PUBLISHING`이 먼저
성공하면 뒤늦은 cancel은 too-late/no-op이고, `CANCEL_REQUESTED`가 먼저 성공하면
publish action은 실행하지 않는다. Step Functions cancel/abort signal이나 별도
intent object가 이 same-state fence를 우회해 result pointer와 run state에 서로
다른 terminal meaning을 만들 수 없다.

Publication pointer CAS는 run-state version을 재사용하지 않고 Phase 09가 승인한
별도 `PublicationPrecondition`과 exact read/reconcile contract를 사용한다.
Run-state `PUBLISHING`은 publication을 허가하지만 S3 pointer의 conditional token을
만들거나 예측하지 않는다. Distinct precondition API가 accepted되기 전에는
`PublishableResultRef`를 보존하되 AWS publication을 시작하지 않는다.

Step Functions state는 thin mapping이다.

ASL/template은 reviewed `workflowType`, coordinator/worker
`LambdaIntegrationMode`와 explicit retry/catch/deadline policy를 materialize한다.
Service default로 type 또는 invocation mode를 결정하지 않는다. Mode 변경은
delivery/retry semantics와 state-machine replacement/rollback 영향을 다시
검증해야 하며 같은 logical retry identity와 CAS fence를 바꾸지 않는다.

```text
InvokeCoordinator
→ Choice(action.type)
   ├─ DISPATCH_PHASE_ONE_SCREENS → invoke/map screen refs → InvokeCoordinator
   ├─ WAIT_FOR_PHASE_ONE_SCREENS → Wait/Wakeup → InvokeCoordinator
   ├─ DISPATCH_WORKERS → invoke/map assignment refs → InvokeCoordinator
   ├─ WAIT_FOR_WORKERS → Wait/Wakeup → InvokeCoordinator
   ├─ REQUEST_WORKER_STOPS → forward exact stop refs → InvokeCoordinator
   ├─ FINALIZE_RESULT → InvokeCoordinator
   ├─ PUBLISH_RESULT → InvokeCoordinator
   ├─ COMPLETE → Succeed
   └─ FAIL → Fail
```

Choice는 action type만 본다. Worker count, success count, objective, verifier report
내용이나 champion score를 ASL에서 계산하지 않는다. `WAIT_FOR_WORKERS` wakeup이
lost/duplicate/out-of-order여도 coordinator가 exact state와 declared outcome key를
다시 읽어 수렴해야 한다.

### 6.4 Pseudocode

```text
onApiSubmit(request):
  envelope = validateAwsEnvelopeVersionAndSize(request)
  command = mapToProviderNeutralSubmit(envelope)
  result = submitSolve(command)
  if result == CREATED:
      scheduler.start(result.advanceSolveRef)
  else if result == SAME_SUBMISSION_SAME_DIGEST:
      converge
  else:
      reject typed conflict
```

```text
onCoordinatorCommand(commandRef):
  command = artifactStore.readVerified(commandRef)
  action = advanceSolve(command)
  require action.actionId and payloadRef are committed in exact current state
  switch action.type only:
      DISPATCH_PHASE_ONE_SCREENS:
          dispatch exact Phase-1 screen refs in declared stable order
      WAIT_FOR_PHASE_ONE_SCREENS:
          schedule wait/wakeup hint
      DISPATCH_WORKERS:
          assert deploymentQuota.canRepresent(action.assignments)
          dispatch exact assignment refs in stable ordinal
      WAIT_FOR_WORKERS:
          schedule wait/wakeup hint
      REQUEST_WORKER_STOPS:
          forward exact in-flight WorkerRunIds and cancellation identity
      FINALIZE_RESULT or PUBLISH_RESULT:
          invoke matching application command
      COMPLETE:
          return terminal provider metadata
      FAIL:
          record typed failure and stop
```

```text
onComputeEvent(event):
  validate eventVersion and identity
  assignment = readVerified(event.assignmentRef)
  if event is PhaseOne:
      assert assignment.phaseOneScreenRunId == event.phaseOneScreenRunId
      outcome = executePhaseOneScreen(assignment)
      refs = putIfAbsent exact screen outcome/report keys
      completePhaseOneScreen(screenRunId, attemptId, refs)
      same/different digest duplicate rules apply
      return
  assert assignment.workerRunId == event.workerRunId
  observe platform deadline without changing algorithm budget
  outcome = executeWorkerRun(assignment)
  candidateRef = putIfAbsent(exact candidate key, outcome.candidate)
  reportRef = putIfAbsent(exact verifier key, outcome.report)
  completion = completeWorkerRun(workerRunId, attemptId, candidateRef, reportRef)
  same digest duplicate → converge
  different digest duplicate → integrity failure
```

```text
onPlatformFailure(observation):
  write immutable provider failure observation
  reconcile exact application state
  if retry policy permits:
      same PhaseOneScreenRunId or WorkerRunId
      + new AttemptId + same assignment/seed/warmStart/steps
  else:
      transition to PLATFORM_TIMEOUT | RESOURCE_LIMIT | FAILED | INCOMPLETE
  never publish partial success
```

## 7. Identity, namespace와 lifecycle

### 7.1 Logical identity와 physical key

Stable identity:

```text
TenantId / SubmissionId / SolveId / ManifestFingerprint
PhaseOneScreenRunId / PortfolioOrdinal
RoundOrdinal / WorkerOrdinal / WorkerRunId / AttemptId
ArtifactKind / ArtifactId / ContentDigest
```

Phase 09의 proposed logical layout에 S3 adapter의 opaque `rootPrefix`만 앞에 붙인다.

```text
{rootPrefix}/tenants/{tenantToken}/
├── artifacts/{artifactKind}/{schemaVersion}/{algorithm}-{checksum}
├── profiles/{profileKey}/{profileVersion}/{profileChecksum}
├── submissions/{submissionToken}/pointer
└── solves/{solveToken}/
    ├── manifests/{manifestChecksum}
    ├── state/current
    ├── cancellation/intent
    ├── phase-one/
    │   ├── assignments/{portfolioOrdinal}/{assignmentChecksum}
    │   ├── screens/{portfolioOrdinal}/runs/{screenRunToken}/
    │   │   └── attempts/{attemptToken}/{outcomeChecksum}
    │   └── champion
    ├── rounds/{roundOrdinal}/
    │   ├── state/current
    │   ├── assignments/{workerOrdinal}/{assignmentChecksum}
    │   ├── workers/{workerOrdinal}/
    │   │   ├── runs/{workerRunToken}/attempts/{attemptToken}/{outcomeChecksum}
    │   │   └── committed
    │   └── champion
    └── results/
        ├── manifests/{publishableChecksum}
        └── published
```

규칙:

- Segment는 typed canonical encoder로 만들고 raw external key/path를 붙이지 않는다.
- `/`, `..`, percent escape, control character, Unicode normalization ambiguity,
  empty/reserved segment와 overlength를 거부한다.
- Decode 후 re-encode가 byte-exact canonical key와 다르면 거부한다.
- Content address는 algorithm/version/kind/schema를 포함하고 truncate/hash
  fallback을 숨기지 않는다.
- Tenant root 밖 접근은 key-layout unit test와 IAM negative integration test가 모두 막는다.
- Bucket/account/region/KMS key/endpoint는 locator 또는 deployment config이며 logical key가 아니다.
- Prefix listing으로 worker/result completeness를 판정하지 않는다.

### 7.2 Idempotency와 attempt lifecycle

```text
same SubmissionId + same input/profile/manifest digest
    → same SolveId/status로 converge

same SubmissionId + different digest
    → IDEMPOTENCY_CONFLICT

same PhaseOneScreenRunId or WorkerRunId + retry
    → new AttemptId only

same logical screen/worker ID + same verified success digest
    → duplicate converge

same logical screen/worker ID + different verified success digest
    → REPRODUCIBILITY_INTEGRITY_FAILURE
```

SDK retry, Lambda platform retry, Step Functions task retry와 application retry를
구분한다. SDK retry는 한 attempt 안의 transport observation이고, application
retry만 새 `AttemptId`를 만든다. 어느 layer도 무한 retry하지 않으며 exact
max-attempt/backoff/jitter 수치는 stage config와 evidence에 명시한다. 미확정 값을
hidden default로 넣지 않는다.

### 7.3 Artifact/state lifecycle와 retention

```text
immutable content put
→ read-back checksum/digest/schema verify
→ immutable verifier/failure observation put
→ one authoritative state/pointer CAS
→ exact-key readers observe committed ref
```

중간 실패로 생긴 unreferenced object는 result가 아니다. Retention/GC는
authoritative pointer reachability와 minimum incident/audit retention을 보존하며,
prefix age만 보고 실행 중 object를 삭제하지 않는다. Exact retention 일수,
version expiration, incomplete multipart cleanup과 KMS rotation은
Security/Operations ADR에서 explicit 값으로 승인한다.

## 8. IAM, encryption, secret와 tenant contract

### 8.1 Role matrix

| Role | 허용 data-plane action | 금지 |
|---|---|---|
| API Lambda | submission/input/profile exact read, initial state create, approved state-machine start, published pointer와 verified result closure exact read | Worker outcome/unpublished result write or read, broad tenant list, KMS admin |
| Coordinator Lambda | exact solve state read/CAS, assignment/action artifact write, worker dispatch/status/stop, workflow wakeup | Raw input body log, result verifier bypass, other tenant prefix |
| Worker Lambda | Assigned snapshot/profile/travel exact read, own worker attempt/candidate/report exact write, own completion CAS request | State-machine start, champion/publication write, other worker/tenant write |
| Step Functions execution | Named API/coordinator/worker Lambda invoke, approved wait/callback/log/trace operation | S3 artifact/state read/write, KMS decrypt, comparator/verifier data access |
| Integration test | Named integration stack invoke/read plus explicit negative-test assume role | Production resources, IAM/KMS policy mutation |
| Deploy/rollback | Reviewed stack change set, Lambda/version/alias and state-machine/IAM/KMS provisioning | Application artifact/result content read unless separately audited |

Resource patterns are generated from stage, tenant partition strategy와 exact key namespace.
`s3:*`, `kms:*`, `states:*`, `lambda:*` on `*`는 data-plane role에서 금지한다.
Provider control-plane service-linked operations이 필요하면 deploy role에만 좁혀
CloudFormation change-set review evidence로 남긴다.

Multi-tenant data role은 다음 중 Security가 승인한 한 mode를 명시해야 한다.

```text
TENANT_SCOPED_SESSION
  → short-lived tenant-scoped session/session policy
  → exact tenant root와 required KMS context만 허용

DEDICATED_TENANT_RESOURCE
  → tenant-specific bucket/access point/role boundary
  → logical ArtifactKey와 result identity는 동일
```

Shared Lambda execution role이 모든 `tenants/*`를 읽고 application의 string check만
믿는 mode는 금지한다. Temporary credential/handle은 adapter 내부에서만 사용하고
artifact/event/log에 넣지 않는다. Integration stage의 두 test tenant로 positive
own-tenant와 negative cross-tenant operation을 실제 검증한다.

### 8.2 Encryption baseline

제안 safe baseline:

- TLS endpoint와 hostname verification 없이 S3/Step Functions/Lambda/KMS를 호출하지 않는다.
- S3 object는 stage별 customer-managed symmetric KMS key의 SSE-KMS로 암호화한다.
- API/coordinator/worker role별 KMS encrypt/decrypt/data-key 권한을 key policy와 IAM 양쪽에서 제한한다.
- KMS key ARN은 opaque config handle이며 artifact/result fingerprint에 포함하지 않는다.
- Bucket policy는 unencrypted transport와 승인되지 않은 encryption header를 거부한다.
- Bucket policy는 §6.1의 artifact/state/publication prefix에 필요한 conditional
  header가 없는 write도 거부하며 unconditional overwrite 권한을 만들지 않는다.
- Object encryption metadata와 content digest를 독립 검증한다.
- Key rotation/deletion window/multi-region 여부는 Security ADR가 explicit하게 정하며 미확정 상태에서는 production deploy하지 않는다.

Integration stage에서 AWS-managed key를 사용하는 예외는 Security가 목적·기간·범위를
승인하고 evidence manifest에 `NON_PRODUCTION_EXCEPTION`으로 기록할 때만 허용한다.
이를 production baseline으로 승격하지 않는다.

### 8.3 Secrets와 logs

- Long-lived AWS credential을 artifact, environment file, SAM parameter file 또는 log에 넣지 않는다.
- Runtime은 workload role/temporary credential을 사용한다.
- Secret reference만 bootstrap config에 두고 secret value는 `SecretResolver` 경계에서만 해석한다.
- Input, address, PII, full result, signed URL, bearer token, raw event body를 log/trace에 남기지 않는다.
- Correlation은 opaque tenant handle, solve/manifest/worker/attempt ID와 digest만 사용한다.

### 8.4 Network boundary

- API ingress는 approved gateway/auth/TLS contract 뒤에만 열고 Lambda function URL
  또는 public endpoint를 무인증 default로 만들지 않는다.
- Coordinator/worker Lambda에는 public ingress가 없다.
- S3, Step Functions, Lambda, KMS, logs/traces 접근 경로와 egress destination을
  IaC allowlist와 network ADR에 명시한다.
- VPC 연결, interface/gateway endpoint, NAT 또는 public AWS endpoint 중 어느
  topology를 쓰는지는 latency/cold-start/egress/security evidence 전까지 open이다.
- Network retry/timeout은 SDK/platform observation이며 logical attempt/algorithm
  termination을 바꾸지 않는다.
- DNS/endpoint/region failure, unapproved egress와 cross-region access를 actual
  integration fault/negative test로 검증한다.

## 9. Concurrency, deadline, quota와 DLQ-equivalent

### 9.1 Concurrency

다음 세 수치를 분리한다.

```text
logicalDeclaredPhaseOneCount     # 최대 8개 explicit screen declaration
logicalDeclaredWorkerCount       # ExecutionManifest의 Phase-2 의미
dispatchConcurrencyLimit         # Step Functions/Lambda platform config
lambdaReservedConcurrency        # deployment isolation/resource guard
```

Platform limit가 logical worker 수보다 작으면 queue/wave로 **모든 declared
assignment**을 실행할 수는 있지만 worker를 생략하거나 일부 성공만 aggregate할 수
없다. Deadline 안에 complete schedule을 보장할 evidence가 없으면 dispatch 전에
`PLATFORM_CAPACITY_INSUFFICIENT` 또는 실행 중 `INCOMPLETE`로 끝낸다. Completion
order와 cold start는 comparator input이 아니다.

### 9.2 Deadline

```text
algorithmStepBudget          # Q-BENCH-02/manifest
applicationWatchdog          # logical exceptional termination
lambdaRemainingTime          # platform observation
stepFunctionsTaskTimeout     # provider timeout
sdkOperationDeadline         # transport bound
```

각 값은 서로 변환하지 않는다. Worker는 configured safety margin 전에 cooperative
cancel/checkpoint를 요청할 수 있지만 미완료 step을 completed로 세지 않는다. Exact
safety margin과 timeouts는 workload measurement 전 proposed/open이며 stage
manifest에 빠지면 startup/preflight가 실패해야 한다.

Durable workflow/crash-resume는 process-local monotonic tick을 새 Lambda/JVM의
origin과 직접 비교하지 않는다. Phase 08/10/11 owner가 durable deadline
representation, restart-origin reconciliation, platform reserve와 indeterminate
clock failure를 승인하기 전 deadline-enabled production path는 닫혀 있다.
Wall clock, Lambda remaining time, Step Functions elapsed time 또는 새 process의
monotonic origin을 hidden fallback으로 사용하지 않는다. Last safe point는 explicit
`TEST_ONLY` virtual clock을 쓰는 pure/local model이며 actual AWS restart fault
test가 승인 ADR을 검증해야 한다.

### 9.3 Quota preflight

배포와 run preflight는 최소 다음을 읽고 evidence에 고정한다.

- Lambda timeout, memory, ephemeral storage, reserved/account concurrency
- Step Functions execution/task concurrency, payload/history/time limits
- S3 request/multipart/object-size/conditional-operation assumptions
- KMS request quota와 throttling behavior
- Log ingestion/retention, trace sampling과 metric cardinality budget
- API ingress payload/timeout과 artifact-reference offload threshold

큰 input/problem/candidate/result는 workflow/Lambda payload에 넣지 않고 digest가
있는 `ArtifactRef`로 전달한다. Quota 초과 시 payload truncate, worker 축소,
verification skip 또는 normal success 변환을 금지한다.

### 9.4 DLQ 또는 동등한 failure contract

Authoritative contract:

```text
failed provider delivery/execution
→ immutable ProviderFailureObservation
→ exact SolveId/WorkerRunId/AttemptId state reconciliation
→ permitted retry or typed terminal/incomplete state
```

선택한 Lambda invocation mode가 asynchronous delivery라면 SQS DLQ 또는 Lambda
failure destination을 두고 payload에는 event ref와 opaque correlation만 넣는다.
Step Functions synchronous task failure/redrive를 쓰는 mapping이면 execution
failure + alarm + reconciliation worker가 DLQ-equivalent다. 어느 방식이든:

- DLQ/message 수와 payload는 completeness authority가 아니다.
- Poison event는 bounded redrive 뒤 quarantine하고 success로 drop하지 않는다.
- Duplicate redrive는 same identity 규칙으로 수렴한다.
- DLQ 접근 role, retention, encryption, alarm과 replay audit가 필요하다.
- Product 선택과 exact redrive 수치는 ADR 승인 전 proposed/open이다.

## 10. Observability와 cost contract

### 10.1 Logs, metrics, traces

Required structured log fields:

```text
eventSchemaVersion, stage, component, deploymentRevision
tenantHandle, solveId, manifestFingerprint
roundOrdinal, workerOrdinal, workerRunId, attemptId
actionType, stateBefore, stateAfter, termination
artifactKind, artifactDigest, verifierDisposition
awsRequestId, stateMachineExecutionHandle, traceId
```

Metrics:

```text
submissions_total
state_transition_total{component,transition,outcome}
worker_dispatch_total{outcome}
worker_attempt_total{termination}
artifact_io_total{operation,outcome}
artifact_bytes{operation,kind}
cas_conflict_total{object_kind}
duplicate_event_total{classification}
declared_worker_missing_total
verification_total{gate,disposition}
publication_total{outcome}
platform_throttle_total{service,operation}
dlq_or_reconciliation_backlog
```

Tenant/Solve/Worker/Digest를 metric dimension으로 넣지 않는다. 이들은 log/trace
correlation field다. Trace는 API → coordinator → dispatcher → worker →
completion → publication을 연결하지만 raw artifact body를 attribute/event로
기록하지 않는다. Sampling은 failure/integrity/security event를 잃지 않는 explicit
policy를 갖는다.

Required alarms:

- Failed/throttled coordinator 또는 worker invocation
- Step Functions failed/timed-out/aborted execution
- DLQ/reconciliation backlog non-zero
- Missing declared worker/round incomplete
- Candidate/result verifier failure와 publication rejection
- S3/KMS access denied, digest mismatch와 CAS conflict surge
- Lambda concurrency/throttle, timeout, out-of-memory/resource failure
- Log delivery/trace exporter failure

### 10.2 Cost evidence

Unit/contract/local-emulation pass는 cost evidence가 아니다. Isolated AWS run은
manifest별 다음 usage를 수집한다.

```text
Lambda invocations, GB-seconds, duration and provisioned/reserved settings
Step Functions transitions/executions
S3 request counts, bytes stored/read/written and retention class
KMS request counts
log ingestion/storage, metric and trace volume
network transfer by region/path
failed/retried/duplicate work ratio
```

FinOps가 workload class와 비용 cap을 승인하기 전에는 “저비용”, “scale-to-zero라
무료” 또는 production 적합을 주장하지 않는다. Cap이 없으면 tests가 성공해도
`PRODUCTION_COST_GATE_OPEN=false`로 evidence manifest를 만든다.

## 11. Ordered work packages

각 WP는 앞 WP의 output을 입력으로 사용한다. Red test와 offline validation은
entry gate 전 준비할 수 있지만 실제 adapter 구현·AWS deploy evidence는 §4 gate
뒤에만 수행한다.

### WP11-0 — Entry receipt와 drift freeze

- **Prerequisite:** 이 문서 review 가능 상태.
- **Targets:** `Phase11EntryReceipt`, source/adjacent section projection, ADR register.
- **Tasks:** Phase 00/06~10 accepted evidence를 exact ref로 확인하고, actual이 된
  Phase 09/10의 handoff section만 대조한다. AWS integration account/region/role와
  budget guard authority를 확인한다.
- **Verification:** `test -s`로 source/evidence ref 존재 확인, metadata checker로
  `Q-INFRA-01=RESOLVED`, `Q-BENCH-02=OPEN — EXPERIMENT_REQUIRED`,
  `Q-VAR-01=DEFERRED`를 검사한다.
- **Expected:** 모든 entry가 signed receipt에 있거나 `BLOCKED`가 유지된다.
- **Failure/rollback:** 누락 시 문서/test fixture 단계로 돌아가며 mock artifact,
  empty implementation 또는 temporary production default를 만들지 않는다.
- **Handoff:** `Phase11EntryReceipt` → WP11-1.

### WP11-1 — Module/DAG, event/config와 IaC contract

- **Prerequisite:** WP11-0 receipt, Phase 00 architecture owner review.
- **Targets:** Three AWS adapter modules, distribution, versioned event DTO,
  `AwsServerlessDeploymentConfig`, proposed SAM/CloudFormation/ASL tree.
- **Tasks:** POM dependency를 adapter에만 추가하고 event/config validation을
  fail-closed로 만든다. Workflow type과 Lambda integration mode를 explicit
  reviewed config로 요구하고 IaC parameter에서 provider/semantic/algorithm
  default를 제거한다.
- **Verification:** `AwsSdkLeakageArchitectureTest`,
  `AwsEventEnvelopeContractTest`, `AwsDeploymentConfigTest`,
  `AwsServerlessTemplateContractTest`; `mvn -pl ... -am verify`,
  `sam validate --lint`, `cfn-lint`, ASL schema validation.
- **Expected:** Stable module AWS reference 0, unsupported event/config 누락 거부,
  ASL semantic branch 0.
- **Failure/rollback:** Module/IaC draft를 merge하지 않고 last accepted reactor와
  local distribution을 유지한다.
- **Handoff:** Versioned event/config/IaC contract → WP11-2/3/4.

### WP11-2 — S3 backend와 Phase 09 conformance

- **Prerequisite:** Accepted Phase 09 suite/encoding/key layout, WP11-1.
- **Targets:** `S3ObjectStorageBackend`, key layout, checksum/digest,
  put-if-absent/CAS/publication mapping.
- **Tasks:** Phase 09 abstract suite를 상속하고 tenant/exact-key, conditional write,
  streaming, KMS metadata와 SDK error mapping을 구현한다. Run-state token과
  distinct한 publication-pointer precondition/read/reconcile contract만 mapping한다.
- **Verification:** Unit → local-emulation → actual S3 integration 순서. 실제
  concurrency, stale token, same/different digest와 KMS deny를 포함한다.
- **Expected:** Filesystem/local oracle와 같은 logical result, no listing authority,
  last-write-wins 0.
- **Failure/rollback:** S3 adapter/distribution을 비활성화하고 immutable integration
  objects를 evidence quarantine에 보존한다. State/publication pointer를 임의
  overwrite하지 않는다.
- **Handoff:** `E-P11-AWS-CONTRACT.storage` → WP11-5/7.

### WP11-3 — Lambda handlers와 worker dispatch

- **Prerequisite:** Phase 08/10 accepted ports, WP11-1, S3 verified-read available.
- **Targets:** API/coordinator/worker handler, `LambdaWorkerDispatcher`, deadline and
  error mapper.
- **Tasks:** Versioned event → command mapping, exact assignment read, same logical
  retry, duplicate completion, cooperative cancel과 platform termination을 구현한다.
  Durable deadline ADR와 restart-origin reconciliation이 없으면 deadline-enabled
  AWS path를 fail-closed한다.
- **Verification:** Handler contract, duplicate/retry/deadline/cancel/fault tests와
  Lambda integration invocation.
- **Expected:** Provider context 누출 0, retry identity 보존, partial/timeout success 0.
- **Failure/rollback:** Lambda alias/distribution을 previous accepted version으로
  되돌리고 logical state를 exact CAS reconciliation한다.
- **Handoff:** Compute/event mapping evidence → WP11-4/7.

### WP11-4 — Thin Step Functions mapping

- **Prerequisite:** Accepted Phase 10 action oracle, WP11-1/3.
- **Targets:** `solve.asl.json`, `StepFunctionsWorkflowScheduler`, action mapper,
  failure/redrive reconciliation.
- **Tasks:** Action-only Choice, wait/wakeup, bounded retry/cancel forwarding을
  구현한다. Reviewed workflow type/invocation mode의 delivery, retry, callback,
  duration과 rollback semantics를 materialize한다. Exact current pending
  action/ordinal만 실행하고 same-run-state cancel/publish fence를 보존한다.
  All-declared completeness와 champion은 coordinator에 남긴다.
- **Verification:** ASL structural contract, action trace oracle, duplicate/lost
  wakeup, start conflict, timeout, abort와 redrive integration tests.
- **Expected:** 동일 Phase 10 action log/terminal state, ASL comparator/verifier logic 0.
- **Failure/rollback:** New state-machine revision으로 신규 start를 중단하고 previous
  revision에 route한다. In-flight execution은 exact state reconciliation 정책에
  따라 drain/cancel하며 object state를 rollback하지 않는다.
- **Handoff:** Workflow evidence → WP11-7.

### WP11-5 — IAM/KMS/tenant/security

- **Prerequisite:** WP11-1~4 resource/action inventory, Security owner.
- **Targets:** Role policies, bucket/key policies, KMS policy, network boundary,
  log redaction, negative-access matrix.
- **Tasks:** API/coordinator/worker/deploy/test 역할을 분리하고 allowed/denied
  action을 실제 AWS에서 검증한다. Encryption metadata와 digest를 독립 검사한다.
- **Verification:** IAM policy lint/static analysis, actual assume-role negative tests,
  cross-tenant/key-prefix/KMS deny, secret/PII log scan.
- **Expected:** Required positive operation만 성공하고 negative matrix는 모두
  AccessDenied/typed rejection이다.
- **Failure/rollback:** Integration stack exposure를 차단하고 role policy/alias를
  previous accepted revision으로 되돌린다. Key/object 삭제는 rollback 수단이 아니다.
- **Handoff:** `E-P11-SECURITY` → WP11-6/7.

### WP11-6 — Quota, observability, cost와 operational readiness

- **Prerequisite:** WP11-3~5 integration stack, Operations/FinOps owner.
- **Targets:** Quota preflight, alarms/dashboard-as-code, usage/cost collector,
  retention/backup/incident runbook.
- **Tasks:** Workload manifest마다 explicit limits와 platform observations를
  수집하고 throttle/timeout/DLQ/reconciliation alarms를 fault injection으로
  발화시킨다.
- **Verification:** Metrics/log/trace schema tests, alarm probes, quota rejection,
  cost evidence completeness와 redaction/cardinality tests.
- **Expected:** 모든 required signal이 correlation 가능하고 semantic fingerprint를
  바꾸지 않는다. Cost cap 미승인 시 gate는 닫혀 있다.
- **Failure/rollback:** 신규 workload/deploy를 중단하고 previous alarm/config
  revision으로 복귀한다. 관측 불능 run은 parity/cutover evidence로 사용하지 않는다.
- **Handoff:** `E-P11-OPERATIONS` → WP11-7/8.

### WP11-7 — Actual AWS parity, fault와 shadow rehearsal

- **Prerequisite:** WP11-2~6, approved isolated AWS integration environment,
  accepted local oracle.
- **Targets:** Same-manifest local↔AWS parity bundle, fault matrix, shadow record.
- **Tasks:** Canonical fixtures를 local과 AWS에서 실행하고 artifact/result
  fingerprint, action/state trace, termination을 비교한다. S3 conflict, Lambda
  duplicate/start failure/timeout, Step Functions lost wakeup/abort, KMS deny와
  incomplete worker를 주입한다.
- **Verification:** `AwsReferenceParityIT`, `AwsReferenceFaultIT`,
  `AwsReproducibilityIT`, provider evidence collector.
- **Expected:** Normal run은 semantic parity, fault run은 exact typed terminal과
  publication block. Provider metadata만 별도다.
- **Failure/rollback:** AWS distribution을 candidate 상태로 유지하고 local
  reference를 authority로 보존한다. 실패 evidence를 숨기거나 success로 재분류하지 않는다.
- **Handoff:** `E-P11-PARITY` candidate → WP11-8.

### WP11-8 — Deployment/rollback rehearsal와 evidence seal

- **Prerequisite:** WP11-7 pass와 rollback owner.
- **Targets:** Immutable deployment manifest, change set, previous revision, rollback
  rehearsal, immutable pre-review `Phase11EvidenceManifest`, 별도
  `Phase11PostReviewAcceptanceReceipt`.
- **Tasks:** Integration stage에서 deploy → smoke/parity → alias/state-machine
  revision rollback → exact state/result reconciliation을 수행한다. Evidence
  content digest와 log/query references를 먼저 seal한다. 그 digest를 대상으로
  독립 리뷰가 accepted된 뒤에만 evidence manifest digest와 review digest를
  참조하는 별도 acceptance receipt를 만든다.
- **Verification:** `AwsDeploymentRollbackIT`, manifest schema/content verification,
  one-way dependency/cycle 검사, sealed evidence에 대한 independent Phase 11 review.
- **Expected:** Non-production reference distribution이 accepted evidence를 갖고
  Phase 12/14 handoff가 가능하다. Production authority는 계속 false다.
- **Failure/rollback:** Previous accepted revision 유지, new starts 차단, in-flight
  reconciliation, immutable artifact 보존. Failed rehearsal 또는 accepted가 아닌
  review이면 acceptance receipt를 만들지 않고 Phase 11을 미완료로 둔다.
- **Handoff:** Immutable AWS reference evidence digest + post-review acceptance
  receipt → Phase 12; evidence/rollback bundle + receipt → Phase 14 production
  entry review.

## 12. Exact test specification

### 12.1 Fixture와 oracle

| Fixture | 위치 | Oracle |
|---|---|---|
| Small accepted manifest | `build/test-fixtures/src/main/resources/aws/manifests/small-rpdptw-manifest-v1.json` | Phase 10 fake/local action trace + Phase 07 publishable result digest |
| Duplicate worker events | `aws/events/v1/worker-duplicate-*.json` | Same digest converge, different digest integrity failure |
| State CAS race | `aws/state/cas-race-v1.json` | Exactly one expected-version winner, loser reload |
| Missing worker | `aws/failures/missing-worker-v1.json` | `INCOMPLETE`, publication pointer absent |
| Deadline/timeout | `aws/failures/platform-timeout-v1.json` | `PLATFORM_TIMEOUT`, never `MAX_STEPS_REACHED` |
| Cancellation | `aws/failures/cancel-race-v1.json` | Intent/stop/actual terminal separated |
| IAM matrix | `aws/iam/role-action-matrix-v1.json` | Explicit allow/deny table from §8.1 |
| Redaction corpus | `aws/observability/sensitive-fields-v1.json` | Forbidden token/address/body absent |
| Metric schema | `aws/observability/required-metrics-v1.json` | Required names, bounded dimensions |

Oracle 우선순위는 hand-computed/provider-neutral fake → local reference → actual AWS
observation이다. AWS output을 자기 자신의 expected file로 golden-update하지 않는다.

### 12.2 Unit/architecture/contract tests

| Test class | Exact method | Pass criteria |
|---|---|---|
| `AwsSdkLeakageArchitectureTest` | `stableModulesHaveZeroAwsSdkReferences()` | Core/solver/verification/application AWS SDK bytecode reference 0 |
| same | `applicationContractsExposeNoArnBucketKeyOrLambdaTypes()` | Public signature provider token 0 |
| `S3ObjectKeyLayoutTest` | `encodesTenantAndArtifactSegmentsCanonically()` | Stable exact expected key |
| same | `rejectsTraversalSlashControlUnicodeAmbiguityAndOverlength()` | 모든 악성 segment typed rejection |
| `S3ErrorMapperTest` | `mapsPreconditionFailureToStateConflict()` | Retryable transport error와 conflict 분리 |
| same | `neverMapsAccessDeniedOrChecksumMismatchToNotFound()` | Security/integrity failure 보존 |
| `AwsEventEnvelopeContractTest` | `mapsV1ApiCoordinatorAndWorkerEventsWithoutSemanticDefaults()` | Exact fields, omitted semantic value fail |
| same | `rejectsUnknownVersionOversizeAndIdentityMismatch()` | Handler 호출 전 rejection |
| `LambdaDeadlineMapperTest` | `platformRemainingTimeDoesNotBecomeAlgorithmTermination()` | Typed platform observation only |
| `StepFunctionsActionMappingContractTest` | `mapsOnlyProviderNeutralActionTypes()` | Action type 외 semantic inspection 0 |
| same | `mapsDeclaredPhaseOneAndPhaseTwoActionsWithoutEmbeddingSelection()` | Screen/worker set은 refs로만 전달 |
| same | `definitionContainsNoCustomerObjectiveComparatorVerifierBranch()` | Forbidden token/JSONPath 0 |
| same | `executesOnlyPendingActionCommittedUnderSameActionId()` | Uncommitted/stale action side effect 0 |
| same | `actionCarriesNoOpaqueRunStateOrPublicationVersionToken()` | Semantic/event/action token leakage 0 |
| same | `authorizesExactCurrentPendingActionIdPayloadAndTransitionOrdinal()` | Stale/current ambiguity 0 |
| `AwsDeploymentConfigTest` | `requiresExplicitRegionEncryptionConcurrencyDeadlineQuotaAndRetention()` | 누락 config startup fail |
| same | `requiresExplicitWorkflowTypeAndLambdaIntegrationModesWithoutDefaults()` | Omitted/defaulted mode fail |
| same | `rejectsWorkflowAndInvocationCombinationWithoutReviewedDeliveryRetryContract()` | Unsupported/ungoverned combination fail |
| same | `rejectsSharedUnscopedTenantDataRole()` | Tenant scope를 application check만으로 대체하는 config 거부 |
| `AwsServerlessTemplateContractTest` | `createsNoDatabaseResources()` | DynamoDB/RDS/Aurora/ElastiCache resource 0 |
| same | `separatesApiCoordinatorWorkerRoles()` | Shared broad execution role 0 |
| same | `requiresEncryptionVersioningLoggingAlarmsAndTags()` | Required controls present |
| same | `enforcesConditionalHeadersOnArtifactStateAndPublicationWrites()` | Unconditional create/overwrite path 0 |

### 12.3 Storage/IaC/local-emulation tests

| Test class | Exact method |
|---|---|
| `S3ObjectStorageBackendContractIT` | `putIfAbsentCreatesExactKeyAndVerifiedDigest()` |
| same | `putIfAbsentSameKeySameDigestConverges()` |
| same | `putIfAbsentSameKeyDifferentDigestRejects()` |
| same | `compareAndSetAllowsExactlyOneConcurrentWriter()` |
| same | `compareAndSetRejectsStaleOpaqueVersion()` |
| same | `readVerifiedRejectsChecksumDigestSchemaAndLengthMismatch()` |
| same | `tenantPrefixCannotEscapeOrCrossRead()` |
| same | `publicationPointerAcceptsOnlyPublishableResultRef()` |
| `AwsLocalEmulationIT` | `runsSubmitDispatchCompletePublishWithoutListingAuthority()` |
| same | `duplicateWorkerEventConvergesAndDifferentDigestFails()` |
| `AwsServerlessTemplateContractTest` | `aslAndTemplateReferencesResolve()` |
| same | `allFunctionsHaveExplicitMemoryTimeoutConcurrencyAndLogRetentionParameters()` |
| same | `stateMachineAndLambdaIntegrationModesAreExplicitAndCompatible()` |

Local emulation은 SDK/event/IaC mapping의 빠른 feedback일 뿐 S3 conditional write,
IAM/KMS, Lambda timeout, Step Functions redrive 또는 AWS quota evidence가 아니다.

### 12.4 Actual AWS integration/fault/security tests

| Test class | Exact method | Required AWS oracle |
|---|---|---|
| `S3ObjectStorageBackendAwsIT` | `concurrentCasHasOneWinnerAndNoLostUpdate()` | Exact final token/content + one conflict |
| same | `sseKmsAndDigestAreBothVerified()` | Head/read metadata + independent digest |
| same | `bucketPolicyRejectsUnconditionalArtifactStateAndPublicationWrites()` | Provider-native deny + original object unchanged |
| `StepFunctionsLambdaAwsIT` | `sameManifestCompletesAllDeclaredWorkersAndPublishesBothGateResult()` | Exact state/result refs |
| same | `selectedWorkflowAndInvocationModesMatchDeclaredDeliveryRetryAndReentrySemantics()` | Config/ASL/actual trace exact |
| same | `missingDeclaredPhaseOneScreenCannotSelectWarmStartOrDispatchPhaseTwo()` | Phase-1 incomplete, Phase-2 start 0 |
| same | `duplicateAndOutOfOrderWakeupsConverge()` | Same terminal digest |
| same | `missingDeclaredWorkerEndsIncompleteWithoutPublication()` | `INCOMPLETE`, pointer absent |
| `AwsActionReplayIT` | `crashAfterActionCasReplaysSameActionIdWithoutDuplicateLogicalWork()` | Same receipt/state lineage |
| `AwsRetryIdentityIT` | `workerStartFailureKeepsWorkerRunSeedWarmStartAndStepsAndChangesAttemptOnly()` | Identity projection equality |
| `AwsCancellationIT` | `cancelIntentStopRequestAndActualTerminationRemainSeparate()` | Three observations and no false success |
| same | `publicationAuthorizationFenceAllowsExactlyOneTerminalIntent()` | Cancel 또는 publish 하나만 authoritative |
| same | `cancelRequestedAndPublishingCompeteOnSameRunStateVersion()` | Separate intent object는 winner가 아님 |
| `AwsPublicationFenceAwsIT` | `runStateVersionCannotSubstituteForPublicationPrecondition()` | Distinct exact-read/CAS token and convergence |
| `AwsDeadlineRestartIT` | `crashRestartDoesNotRebaseWatchdogFromWallClockOrProviderRemainingTime()` | Approved durable deadline or typed blocked/indeterminate |
| `AwsPlatformFailureIT` | `lambdaTimeoutThrottleAndOutOfMemoryAreNotNormalTermination()` | Typed exceptional state |
| `AwsDlqReconciliationIT` | `poisonEventIsQuarantinedAndCannotCreateSuccess()` | Bounded redrive + terminal block |
| `AwsIamBoundaryAwsIT` | `workerCannotReadOtherTenantOrWriteChampionOrStartWorkflow()` | All operations denied |
| same | `apiCannotReadUnpublishedResultOrWriteWorkerOutcome()` | All operations denied |
| same | `coordinatorCannotDecryptUnassignedTenantArtifact()` | KMS/S3 deny |
| same | `stateMachineRoleCannotReadS3OrDecryptArtifacts()` | Workflow role has no business-data authority |
| `AwsNetworkBoundaryAwsIT` | `coordinatorAndWorkerHaveNoPublicIngressAndRejectUnapprovedEgress()` | Public ingress 0, deny probe succeeds |
| `AwsLogRedactionIT` | `logsAndTracesContainCorrelationButNoSecretPiiOrPayload()` | Forbidden corpus has zero match |
| `AwsObservabilityAwsIT` | `faultsEmitRequiredMetricsTraceLinksAndAlarms()` | Required metric/alarm/trace evidence |
| `AwsCostEvidenceIT` | `recordsCompleteUsageVectorForOneManifest()` | §10.2 field completeness |
| `AwsReferenceParityIT` | `localAndAwsProduceSameCanonicalArtifactsResultAndTermination()` | All semantic fingerprints equal |
| `AwsReproducibilityIT` | `repeatedAwsRunWithSameEnvelopeHasSameSemanticTraceAndResult()` | Provider metadata excluded, semantics equal |
| `AwsDeploymentRollbackIT` | `previousRevisionCanResumeNewStartsAfterCandidateRollback()` | Alias/revision rollback + state reconciliation |

### 12.5 Red → green sequence

1. Architecture/event/IaC tests를 missing module/template에서 red로 고정한다.
2. S3 abstract contract를 in-memory/file oracle green, S3 adapter red로 실행한다.
3. Key/error/config unit tests를 green으로 만들고 workflow type/Lambda integration
   mode 누락·불일치와 unconditional S3 write policy를 red로 고정한다.
4. Local emulation에서 event/SDK/IaC mapping을 green으로 만든다.
5. Actual S3 concurrency/IAM/KMS/conditional-policy tests가 red인 상태에서
   integration resource를 최소 권한으로 조정해 green으로 만든다.
6. Phase 10 fake action trace를 explicit workflow/invocation mode의
   Step Functions/Lambda integration과 비교한다.
7. Duplicate/lost/timeout/cancel/DLQ fault tests를 red→green으로 만든다.
8. Local↔AWS parity와 repeated-run reproducibility를 green으로 만든다.
9. Observability/cost/rollback rehearsal과 independent review를 통과한다.

Cloud integration이 먼저 우연히 green이어도 unit/contract/local oracle red를
무시하지 않는다. Emulator green을 actual AWS green으로 표기하지 않는다.

### 12.6 Commands와 pass criteria

Future target tree가 구현된 뒤 실행할 exact command baseline:

```bash
./mvnw -B -ntp -Dstyle.color=never clean install
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
  -Dtest=AwsSdkLeakageArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
./mvnw -B -ntp -Dstyle.color=never \
  -pl adapters/object-s3,adapters/workflow-aws-stepfunctions,adapters/compute-aws-lambda,distributions/aws-serverless \
  -am clean verify
./mvnw -B -ntp -Dstyle.color=never \
  -pl distributions/aws-serverless -Paws-local-it clean verify
sam validate --template-file deployment/aws/template.yaml --lint
cfn-lint deployment/aws/template.yaml deployment/aws/alarms/required-alarms.yaml
```

Approved isolated environment only:

```bash
sam build --template-file deployment/aws/template.yaml
sam deploy --config-file deployment/aws/samconfig.toml --config-env integration --no-confirm-changeset --no-fail-on-empty-changeset
./mvnw -B -ntp -Dstyle.color=never \
  -pl distributions/aws-serverless -Paws-integration clean verify \
  -Daws.stage=integration -Daws.evidenceDir=target/phase11-evidence
```

각 Maven command 직후 다음 `clean` 전에 fresh Surefire/Failsafe XML과 exact
class/method manifest를 봉인한다. Manifest의 expected/discovered/passed 수를
대조하고 missing/duplicate/failed/error/skipped가 하나라도 있으면 실패다.
Selected test command는 `-am` 없이 typo/누락 0-test를 fail-closed하고, 같은
immutable source의 root `clean install`과 filter 없는 module/profile `clean verify`를
함께 요구한다. Console summary나 이전 `target/` report만으로 통과시키지 않는다.

`sam deploy --no-fail-on-empty-changeset`의 exit 0은 새 deployment evidence가 아니다.
Empty change set은 `NO_CHANGE`로 기록하고, resolved template, function code/alias,
state-machine definition/revision과 manifest digest가 정확히 일치할 때만 기존
integration stack reconciliation evidence로 쓸 수 있다. 어떤 경우에도 production
deployment 또는 production authority evidence가 아니다.

Production `sam deploy` command는 이 문서의 실행 baseline에 포함하지 않는다.
Production deployment는 Phase 14 authority가 exact account/region/change set과
승인자를 제공한 뒤 별도 controlled procedure에서만 실행한다.

전체 pass criteria:

- Maven/IaC/schema validation exit 0
- Expected/discovered/passed class/method count exact; failed/error/skipped/missing/duplicate 0
- 실제 AWS suite를 emulator/profile로 대체한 skip 0
- Core/solver/verification/application AWS leakage 0
- Database IaC resource 0
- S3 contract, state CAS, IAM/KMS negative matrix 모두 pass
- Unconditional S3 artifact/state/publication write 허용 0
- Workflow type/Lambda integration mode의 omitted/default/fallback 0
- Missing worker/timeout/cancel/fault에서 publication false success 0
- Local↔AWS semantic fingerprint mismatch 0
- Required logs/metrics/traces/alarms 누락과 forbidden sensitive match 0
- Cost/retention/quota evidence field 누락 0
- Rollback rehearsal unresolved in-flight state 0
- Independent review verdict accepted

## 13. IaC, deployment와 rollback contract

### 13.1 IaC contract

AWS SAM/CloudFormation은 proposed baseline이고 다음을 code review 가능한 source로
관리한다.

- S3 bucket/versioning/encryption/public-access block/lifecycle/logging과
  artifact/state/publication prefix별 conditional-write enforcement
- KMS key/policy/alias/rotation setting
- API/coordinator/worker Lambda role/function/version/alias/log group
- Explicit workflow type와 Lambda integration mode를 가진 Step Functions state
  machine/role/logging/tracing/alarm
- Optional DLQ/failure destination and reconciliation alarm
- Explicit parameters for region-independent names, quotas, concurrency, deadline, retention
- Required tags: application, stage, owner, data-classification, cost-center,
  deployment-revision

Generated `.serverless/cloudformation-template-*.json`은 source IaC가 아니다. Source
template에서 재현되지 않는 resource는 배포하지 않는다.

### 13.2 Deployment manifest

```text
AwsDeploymentManifest
  phase11EvidenceSchemaVersion
  sourceCommit
  sourceSectionFingerprints
  buildToolchainFingerprint
  distributionArtifactDigest
  containerOrZipDigest
  samTemplateDigest
  aslDefinitionDigest
  changeSetDigest
  stage
  region
  accountAlias
  lambdaVersionAndAliasRefs
  stateMachineRevisionRef
  bucketAndKmsOpaqueRefs
  explicitWorkflowAndInvocationModeConfigDigest
  conditionalWritePolicyDigest
  explicitQuotaDeadlineRetentionConfigDigest
  iamPolicyDigests
  alarmsAndTelemetryConfigDigest
  deployedAt
  deployedByApprovedRole
  productionAuthority: false
```

Actual stack outputs/ARN은 restricted evidence locator에 두고 문서/log에 raw account
정보를 복사하지 않는다.

### 13.3 Rollout/rollback

Non-production reference rollout:

```text
build immutable distribution
→ validate change set and policies
→ deploy candidate Lambda versions/state-machine revision
→ keep previous revision addressable
→ smoke + contract + parity + fault
→ route new integration submissions to candidate
→ rollback rehearsal
→ seal evidence
```

Rollback:

```text
stop new candidate starts
→ route new starts to previous accepted revision
→ read exact application state for in-flight solves
→ drain or typed-cancel according to approved policy
→ reconcile exact worker/state/result refs
→ preserve immutable S3 artifacts and evidence
→ verify publication pointer and tenant isolation
```

S3 artifact bytes, state history 또는 KMS key를 삭제/overwrite하는 방식은 rollback이
아니다. Schema migration이 필요한 경우 versioned reader/writer compatibility와
forward-only artifact copy를 별도 승인한다.

첫 AWS reference deployment에 previous accepted AWS revision이 없다면 rollback
target은 **AWS 신규 start 차단 + accepted local reference 유지**다. Ignored
`.serverless` stack/template나 legacy GCP path를 AWS semantic fallback으로
승격하지 않는다. Legacy GCP는 별도 compatibility/rollback inventory로만 남고
그쪽 deploy/traffic 변경도 이 Phase 11 권한에 포함되지 않는다.

## 14. Evidence bundle과 gates

### 14.1 Evidence manifest

```text
Phase11EvidenceManifest
  schemaVersion
  phase = 11
  provider = AWS_REFERENCE
  storage = S3
  workflow = STEP_FUNCTIONS
  compute = LAMBDA
  entryReceiptRef
  sourceCommit
  sourceSectionFingerprints
  phase09StorageContractRef
  phase10CoordinatorContractRef
  buildAndArchitectureEvidenceRef
  s3ContractAndActualAwsEvidenceRef
  s3ConditionalWritePolicyEvidenceRef
  distinctPublicationPreconditionEvidenceRef
  lambdaEventComputeEvidenceRef
  stepFunctionsActionEvidenceRef
  workflowAndInvocationModeEvidenceRef
  sameRunStateCancellationPublicationFenceEvidenceRef
  durableDeadlineRestartEvidenceRef
  iamEncryptionTenantEvidenceRef
  quotaDeadlineDlqEvidenceRef
  observabilityAndCostEvidenceRef
  localAwsParityEvidenceRef
  faultAndReproducibilityEvidenceRef
  deploymentManifestRef
  rollbackRehearsalRef
  limitationsAndOpenValues[]
  productionAuthority = false
  contentDigest
```

Evidence reference는 immutable content digest와 restricted locator를 가진다.
Console screenshot, command transcript 한 줄, generated template, mock success 또는
“deployed” 서술만으로 evidence key를 채우지 않는다.

`Phase11EvidenceManifest`는 독립 리뷰 **전**에 봉인되는 AWS reference evidence
source다. Reviewer identity, review document/digest/result 또는 acceptance receipt
reference를 포함하지 않으며 review 이후 다시 써서도 안 된다. 독립 리뷰가
`ACCEPTED`인 경우에만 다음 별도 receipt를 만든다.

```text
Phase11PostReviewAcceptanceReceipt
  schemaVersion
  phase = 11
  phase11EvidenceManifestDigest
  independentReviewDigest
  independentReviewVerdict = ACCEPTED
  reviewedTargetDocumentVersion
  productionAuthority = false
  contentDigest
```

의존 방향은 receipt → pre-review evidence/review뿐이다. Evidence manifest는
receipt나 review를 역참조하지 않는다. 현재 review verdict는 `CHANGES_REQUIRED`,
evidence status는 `NOT_PRODUCED`이므로 이 receipt도 현재 존재하지 않는다.

### 14.2 Exit gates

| Gate | Required evidence |
|---|---|
| G11-A Architecture | AWS SDK/provider type leakage 0, module/IaC contract |
| G11-B Storage | Phase 09 suite + distinct publication precondition + actual S3 CAS/digest/tenant/KMS + unconditional-write deny |
| G11-C Workflow/compute | Exact pending action, same-state cancel fence, explicit workflow/invocation mode + Phase 10 action/state/retry/cancel parity |
| G11-D Completeness/publication | Missing worker blocks; two-gate publication only |
| G11-E Security | IAM negative matrix, encryption, secret/PII redaction |
| G11-F Operations | Restart-safe durable deadline, quota/DLQ, logs/metrics/traces/alarms, cost/retention evidence |
| G11-G Parity/replay | Local↔AWS semantic parity and same-envelope reproducibility |
| G11-H Deployment/rollback | Immutable non-prod deployment + rollback rehearsal |
| G11-I Review | Evidence manifest digest와 independent review digest를 단방향 참조하는 `Phase11PostReviewAcceptanceReceipt` |

모든 gate는 AND다. G11-H가 통과해도 production authority는 생기지 않는다.

## 15. Definition of Done

Phase 11은 다음이 모두 참일 때만 `ACCEPTED` 후보가 된다.

1. Phase 00/06~10 predecessor가 accepted이고 exact handoff receipt가 있다.
2. S3/Step Functions/Lambda mapping이 provider-neutral contract를 바꾸지 않는다.
3. Actual AWS에서 S3 conditional semantics, unconditional-write deny, IAM/KMS와
   failure mapping을 검증했다.
4. Local과 AWS가 같은 logical manifest에 semantic parity를 보인다.
5. Retry/duplicate/cancel/timeout/throttle/missing worker가 exact terminal로 수렴한다.
6. 두 verifier 이전 publication과 partial worker success가 0이다.
7. Database resource와 object-listing authority가 0이다.
8. IAM least privilege, tenant isolation, network boundary, encryption과 redaction
   negative tests가 통과한다.
9. Quota/deadline/DLQ, logs/metrics/traces/alarms/cost/retention evidence가 완전하다.
10. Immutable deploy provenance와 recoverable non-production rollback rehearsal이 있다.
11. Pre-review evidence manifest digest와 independent review digest를 단방향
    참조하는 post-review acceptance receipt가 있다.
12. Open/gated/deferred 값과 production authority absence가 그대로 기록되어 있다.
13. Workflow type과 coordinator/worker Lambda integration mode가 reviewed config에
    명시되고 default/fallback 없이 actual delivery/retry/reentry semantics가 검증됐다.
14. Opaque run-state token은 action/event identity에 없고 publication pointer는
    distinct precondition으로 exact reconcile된다.
15. Cancel request가 terminal fence로 오인되지 않고 `CANCEL_REQUESTED`와
    `PUBLISHING`이 같은 run-state version에서 경쟁한다.
16. Durable deadline restart ADR와 crash test가 없으면 deadline-enabled AWS path는
    disabled/blocked다.

Source/test/IaC file 존재, SAM validation, emulator green, AWS stack create 또는 happy
path 한 번은 DoD가 아니다.

## 16. 금지 anti-pattern

| Anti-pattern | 왜 금지하는가 |
|---|---|
| AWS SDK를 root/core/application POM에 추가 | Provider-neutral dependency 붕괴 |
| S3 key/ARN/ETag를 `ArtifactKey`/result fingerprint로 사용 | Provider substitution과 identity 붕괴 |
| DynamoDB로 state/CAS를 쉽게 구현 | No-database와 Phase 09 authority 위반 |
| Prefix list로 성공 worker를 모음 | Missing/visibility/order에 따른 false success |
| Conditional header 없는 S3 write를 adapter IAM에 허용 | Immutable create/CAS/publication fence 우회 |
| Step Functions ASL에서 점수 비교/worker completeness 판단 | Phase 10 semantic ownership 복제 |
| Step Functions type/Lambda invocation mode를 service default로 둠 | Delivery/retry/duration/callback semantics가 hidden input이 됨 |
| Opaque run-state version을 action/event 또는 publication token으로 재사용 | Crash replay와 distinct pointer CAS 붕괴 |
| 별도 cancel-intent object를 publication과의 race winner로 사용 | 서로 다른 key CAS 둘 다 성공해 terminal meaning 분기 |
| Restart 뒤 wall clock/provider remaining time으로 monotonic deadline 복원 | Watchdog/replay 의미가 provider/process마다 달라짐 |
| Lambda handler에서 raw DTO를 solver에 직접 전달 | Input/application/provider 경계 붕괴 |
| Platform timeout을 정상 max-step으로 변환 | 품질 종료와 infrastructure failure 혼합 |
| Lambda retry 때 seed/warm start/steps 변경 | Logical reproducibility 붕괴 |
| DLQ drain을 성공 처리로 계산 | Poison/lost event를 숨김 |
| Shared broad IAM role와 wildcard data action | Tenant/least-privilege evidence 상실 |
| SSE-KMS만 확인하고 digest를 생략 | Encryption과 integrity 혼동 |
| Digest만 확인하고 IAM/encryption을 생략 | Integrity와 confidentiality 혼동 |
| Full input/result를 log/trace에 기록 | PII/secret/tenant boundary 위반 |
| Emulator 결과를 actual AWS integration evidence로 표기 | Provider semantics를 거짓 증명 |
| `.serverless` generated template를 source IaC로 복사 | 재현 불가·DynamoDB/no-DB 위반 legacy 승격 |
| Production config에 test-only 수치 사용 | `Q-BENCH-02`와 운영 gate 임의 해소 |
| Phase 12 adapter/cutover를 함께 구현 | Orthogonal substitution 검증 범위 당김 |

## 17. Blockers, last safe state와 restart

| Blocker | Owner | Last safe state | Restart condition |
|---|---|---|---|
| Phase 00/06~10 unaccepted | 해당 Phase owner + scheduler | 문서/fixture/offline contract only | Accepted evidence/review refs가 entry receipt에 들어감 |
| Exact current pending-action authorization + distinct publication precondition | Phase 08/09/10/11 Application/Storage/Coordinator owners | Pure action mapping과 immutable `PublishableResultRef`; AWS side effect/publication 없음 | Accepted typed action/read/CAS signature와 local/storage/AWS conformance |
| Same-run-state cancellation/publication fence | Phase 08/10/11 Application/Coordinator/AWS owners | Durable cancel request만 저장; 새 side effect/publication authority 없음 | `CANCEL_REQUESTED` 대 `PUBLISHING` same-version race, too-late/crash AWS fault test 공동 승인 |
| Durable monotonic deadline restart contract | Phase 08/10/11 Application/Coordinator/Operations owners | Explicit `TEST_ONLY` virtual clock; deadline-enabled AWS path off | Durable encoding/origin reconciliation/reserve/failure ADR + crash/restart tests |
| AWS ADR 미승인 | Platform/Security/Ops/FinOps | Proposed tree와 red tests; deploy 없음 | IAM/KMS/key/retry/DLQ/quota/retention/cost ADR 승인 |
| Workflow type/Lambda integration mode 미승인 | Platform/Operations/Reliability | Mode-neutral contract와 red config tests; deploy 없음 | Explicit type/mode ADR, compatible IaC validation, actual delivery/retry/reentry evidence |
| Integration environment authority 없음 | Platform/Security | Local/emulator only | Isolated stage, scoped role, region, budget guard와 cleanup owner 승인 |
| Actual S3 CAS/IAM/KMS evidence 없음 | Storage/Security | Filesystem/local oracle | Actual AWS suite pass와 immutable evidence seal |
| `Q-BENCH-02` official 수치 없음 | Benchmark/Quality | Explicit test-only manifests | Calibration result와 사용자 승인 |
| Quota/cost/retention 수치 미확정 | Platform/FinOps/Security/Ops | Non-production candidate config | Workload measurement와 owner-approved thresholds |
| Phase 12 provider 미선택 | Product/Platform | Immutable Phase 11 AWS evidence/receipt만 준비; Phase 12 manifest 미소유 | Provider별 adoption scope 별도 승인 |
| `C-17` optional hybrid gated | Product/Algorithm/Architecture | ALNS-only AWS reference와 infra evidence only | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, Phase 13 C-17 scope + OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| Production authority 없음 | Product/Platform/Security/Ops + Phase 14 owner | Accepted non-prod reference 또는 local runtime | Phase 14 gates + explicit production deploy/cutover approval |
| `Q-VAR-01` deferred | Product/Domain/Algorithm | Current pair/terminal/bank contract | Register의 restart evidence와 별도 승인 |

같은 blocker가 있어도 이 문서는 `REVIEW`에서 멈추지 않는다. 구현자는 last safe
state까지 작업하고 blocker/evidence를 제출한다. Gate를 숨은 default, mock, legacy
artifact 또는 부분 AWS 성공으로 닫지 않는다.

## 18. Handoff와 traceability

### 18.1 Phase 10에서 받는 계약

[Actual Phase 10](phase-10-provider-neutral-coordinator.md)의 authoring task 완료를
확인한 뒤 §14.2 final Phase 11 handoff를 안정된 section 인용으로 대조했다.
Phase 10/11 whole-file 또는 section fingerprint는 이 receipt의 acceptance
입력이 아니다. 구현 entry에서는 §14.2를 다시 읽고 아래 contract의 accepted
review/evidence와 semantic compatibility를 확인한다.

```text
Phase10HandoffManifest
ExecutionRunId/RoundId/WorkerRunId/AttemptId/ActionId contract fingerprint
SolveState/legal-transition/failure taxonomy fingerprint
CoordinatorAction schema fingerprint:
  no opaque repository version in semantic identity/payload
  exact current pending ActionId/payload/transition authorization
  distinct publication-pointer precondition
Artifact/RunState/WorkerDispatcher/Cancellation/Clock/ResultPublisher port fingerprints
declared-completeness/comparator-tie/retry/cancel/deadline semantics
  cancellation intent is request; CANCEL_REQUESTED run-state CAS is fence
  durable monotonic restart contract approval ref
CoordinatorAction:
  DispatchPhaseOneScreens
  WaitForPhaseOneScreens
  DispatchWorkers
  WaitForWorkers
  RequestWorkerStops
  FinalizeResult
  PublishResult
  CompleteSolve
  FailSolve
Phase10EvidenceManifest
E-P10-STATE / E-P10-COMPLETENESS / E-P10-RETRY refs
accepted Phase10 review ref
rollback point
```

AWS adapter는 이 목록을 확장할 수는 있어도 provider-specific field를 원래 contract에
추가하거나 의미를 바꾸지 않는다.

### 18.2 Phase 12 handoff

[Actual Phase 12](phase-12-provider-substitution.md) v1.3 §1~§4, §15.1과 §18.2를
대조했다. 그 문서는 `INDEPENDENT_REVIEWED_WITH_CORRECTIONS`/
`COMPLETE_CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_READY`이며 아래
accepted evidence와 receipt가 없으면 entry를 열지 않는다. Phase 11은 §14.1의
immutable `Phase11EvidenceManifest.contentDigest`와, accepted review 뒤에만 생기는
`Phase11PostReviewAcceptanceReceipt.contentDigest`를 넘긴다.

`ProviderConformanceManifest`의 schema와 lifecycle은 Phase 12가 소유한다. Phase 12가
AWS reference를 oracle로 소비한다면 그 manifest가 Phase 11 evidence/receipt
digest를 **forward-reference**할 수 있다. 반대 방향은 금지한다.

```text
Phase12-owned ProviderConformanceManifest
  -> phase11EvidenceManifestDigest
  -> phase11PostReviewAcceptanceReceiptDigest

Phase11EvidenceManifest
  -X-> ProviderConformanceManifest

Phase11PostReviewAcceptanceReceipt
  -X-> ProviderConformanceManifest
```

Phase 11은 GCS/Cloud Run/Kubernetes/ECS adapter를 만들거나 port를 재설계하지
않는다. AWS evidence는 Phase 12 document/status/adoption/review와 독립된 immutable
source이고 Phase 12가 provider substitution을 승인받았을 때만 downstream oracle로
사용한다. Provider cutover authority도 넘기지 않는다.

### 18.3 Phase 14 handoff

Phase 14는 `Phase11EvidenceManifest`, `Phase11PostReviewAcceptanceReceipt`,
deployment/rollback bundle, open quota/cost/retention limitations를 받는다.
이것은 production entry input일 뿐 production deploy 승인 자체가 아니다.
`Q-BENCH-02`, compliant integer fixture, official manifest와 explicit production
authority가 별도로 필요하다.

### 18.4 Requirement traceability

| Requirement | Design | Work/Test | Evidence |
|---|---|---|---|
| Phase 09 port → S3 | §6.1, §7 | WP11-2, S3 contract/AwsIT | `E-P11-AWS-CONTRACT.storage` |
| Phase 10 coordinator → Step Functions | §6.2~6.4 | WP11-4, action mapping/AwsIT | `E-P11-AWS-CONTRACT.workflow` |
| Exact current action + distinct publication precondition | §6.1~§6.4 | Action/token architecture + publication fence AwsIT | `E-P11-AWS-CONTRACT` |
| Same-run-state cancel/publication fence | §6.3, §9.4 | Cancellation/publication race AwsIT | `E-P11-AWS-CONTRACT.workflow`, `E-P11-OPERATIONS` |
| Durable deadline restart | §9.2 | Restart clock fault IT | `E-P11-OPERATIONS` |
| Explicit AWS workflow/delivery semantics | §3, §6.2~§6.3, §9.4 | Config/template/AwsIT mode tests | `E-P11-AWS-CONTRACT.workflow`, `E-P11-OPERATIONS` |
| Worker → Lambda | §6.2, §9 | WP11-3, handler/retry/deadline tests | `E-P11-AWS-CONTRACT.compute` |
| IAM/encryption/network/tenant | §8 | WP11-5, IAM/KMS/network/redaction tests | `E-P11-SECURITY` |
| Namespace/idempotency/CAS | §7 | WP11-2/3, key/CAS/duplicate tests | `E-P11-AWS-CONTRACT` |
| Concurrency/deadline/quota/DLQ | §9 | WP11-3/4/6, fault/reconciliation tests | `E-P11-OPERATIONS` |
| Logs/metrics/traces/cost | §10 | WP11-6, observability/cost tests | `E-P11-OPERATIONS` |
| Deployment/rollback | §13 | WP11-8, rollback integration test | `E-P11-ROLLBACK` |
| Provider-neutral/no-DB | §2.4, §5.2 | Architecture/IaC tests | `E-P11-AWS-CONTRACT` |
| Local vs actual AWS separation | §12 | Local emulator + separate AwsIT profiles | `E-P11-PARITY` |
| Phase 12 conformance input only | §14.1, §18.2 | WP11-8 evidence seal, accepted-review receipt와 cycle 검사 | `Phase11EvidenceManifest` + `Phase11PostReviewAcceptanceReceipt` digests |
| Production authority gate | §1, §3, §14~18 | Manifest assertion/review | `productionAuthority=false` |

## 19. Reviewer checklist

- [ ] Source authority와 최신 `Q-INFRA-01 RESOLVED`를 보존했다.
- [ ] AWS 선택을 implementation/deploy/production 완료로 쓰지 않았다.
- [ ] Phase 09/10/12 actual 상태와 handoff를 정확히 분리했다.
- [ ] Legacy GCP/Docker/ignored serverless를 migration inventory로만 다뤘다.
- [ ] DynamoDB/no-DB 충돌과 generated artifact 비권위를 명시했다.
- [ ] S3/Step Functions/Lambda mapping에 IAM, encryption, network, key, idempotency,
      concurrency, deadline, quota, DLQ, telemetry, cost, deploy/rollback이 있다.
- [ ] S3 unconditional write를 policy와 actual negative test로 차단했다.
- [ ] Workflow type/Lambda integration mode를 hidden provider default로 두지 않았다.
- [ ] Opaque run-state token과 publication-pointer precondition을 구분했다.
- [ ] Cancel request와 same-run-state terminal fence를 구분했다.
- [ ] Restart-safe monotonic deadline 계약이 없으면 AWS deadline path를 열지 않았다.
- [ ] AWS SDK/IaC가 core/application contract로 누출되지 않는다.
- [ ] Unit/local-emulation/actual AWS evidence와 pass criteria가 분리됐다.
- [ ] 모든 WP에 prerequisite/target/task/verification/expected/failure rollback/handoff가 있다.
- [ ] Exact test class/method, fixture/oracle, red→green, command가 있다.
- [ ] OPEN/GATED/deferred 수치와 production authority absence를 임의 확정하지 않았다.
- [ ] `C-17 GATED TARGET`을 Phase 13 hybrid 구현·활성화 권한으로 승격하지 않았다.
- [ ] Pre-review evidence는 reviewer/result/receipt를 역참조하지 않고 post-review
      receipt만 evidence digest와 review digest를 단방향 참조한다.
- [ ] Phase 12에는 immutable AWS evidence/acceptance receipt만 넘기고
      Phase 12-owned conformance manifest, redesign 또는 cutover를 당기지 않았다.
- [ ] Whole-file reciprocal neighbor hash가 없다.
- [ ] Fake deployment/evidence나 console-only completion claim이 없다.
