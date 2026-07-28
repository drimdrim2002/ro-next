# Phase 14 — Official calibration/cutover

```yaml
document_status: REVIEWED_WITH_CORRECTIONS
document_version: 1.3
phase: "14"
phase_name: official-calibration-cutover
baseline_date: "2026-07-28"
implementation_status: GATED
entry_gate_status: CLOSED
calibration_status: NOT_RUN
official_manifest_status: NOT_CREATED
official_run_status: NOT_RUN
deployment_status: NOT_DEPLOYED
production_authority: NOT_GRANTED
cutover_status: NOT_STARTED
evidence_status: NOT_PRODUCED
review_status: COMPLETE_CHANGES_REQUIRED
review_document: ../reviews/phase-14-review.md
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  phase: Phase 14 Calibration/Cutover owner role
  scheduler: 총괄 스케줄러 role
  benchmark_quality: Benchmark/Quality owner role
  algorithm: Algorithm/ALNS owner role
  input_matrix: Input/Matrix owner role
  great_circle_policy: Input/Matrix + Domain/Architecture owner roles
  independent_verification: Independent Verification/Result owner role
  provider_deployment: Selected-provider Platform/Release owner role
  security: Platform Security/IAM/KMS owner role
  operations: Production SRE/Operations owner role
  cost: FinOps owner role
  product: Product authority role
  signing_trust: Security/Release evidence-trust owner role
  rollback: Production SRE/Incident owner role
  review: Independent Phase 14 reviewer role
prerequisites:
  - Phase 00~11 applicable accepted evidence and review receipts
  - Phase 07 accepted candidate/result both-gate publication path
  - Phase 11 accepted AWS reference parity/security/operations/rollback evidence
  - Phase 12 signed applicability decision; accepted evidence when a substituted provider is selected or actual Phase 13 Activated requires it
  - Phase 13 signed applicability decision; accepted evidence only for an official hybrid manifest
  - approved compliant integer travel fixture or official integer snapshot
  - approved Great Circle policy/function/version and reference vectors
  - approved official ALNS parameter set
  - Q-BENCH-02 calibration result and explicit official execution-value approval
  - approved evidence-signing and trust-root policy
  - actual selected-provider deployment evidence
  - approved calibration acceptance policy and result
  - explicit scoped production authority for each traffic-changing action
planned_evidence:
  - E-P14-CALIBRATION
  - E-P14-OFFICIAL-RUN
  - E-P14-CUTOVER
  - E-P14-ROLLBACK
source_sections:
  canonical_master: "§1~4, §7~8, §13~17; especially §14 and §15.8~15.10"
  final_domain: "§6~17, §18.1~18.2; travel, reproducibility, verification and gated hybrid meaning"
  final_architecture: "§2~6; especially §3.3~3.6 and §5"
  integrated_design: "§2~3, §12~26; especially §17~25 and §26.4"
  open_questions: "Q-MTX-01~03, Q-BENCH-01~03, Q-INFRA-01, Q-VAR-01 and §3~4"
  implementation_readme: "§1~7"
  master_realization_plan: "§2~4, §6~15; Phase 00~14 contracts"
  execution_progress: "§1~9; scheduler authority, phase status and blocker inventory"
  phase_02_actual: "§13.3 downstream consumer only"
  phase_07_actual: "§15.3 downstream consumer only"
  phase_11_actual: "§18.3 Phase 14 handoff only"
  phase_12_actual: "§18.4 future adoption/cutover boundary only"
  phase_13_actual: "v1.2 §6.5 and §14.3~14.4 Phase 14 signed skip/activated applicability handoff only"
  root_readme_and_inventory: "README, current POM/Java/GCP/Docker/data inventory"
source_fingerprints_sha256:
  README.md: 22eff4f63607db29bd4049344986109c680aa970d0865a3b859598e6b3b96c06
  docs/README.md: 5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569
  docs/master-design.md: 58554334b9f27586c93a685adc0facf0fbd7e79576c18890f0ac13891b2f803b
  docs/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/2026-07-26-domain-design.md: 1870662f85a08cc9a1e48a1974b96278eccddfd1519721d71b356c56034ecaab
  docs/2026-07-26-architecture-design.md: 3d4dbbfc7e4cbdb2f3985378d84fd5f717db770b04131573c00ed354a9f41614
  docs/architecture-domain-implementation-design.md: ec513ac1b0bacd88149683bf48c36f7e6edcd53a9232498597e3b0d57c585875
  docs/implementation/README.md: accf7758802c253ae47e3d0fe41e190728c507195d0b41f27f14a25804c8f23f
  docs/implementation/master-realization-plan.md: 5921213ae419b9398bde8c91c3d6ada5aa64bf22a9b823e5b3889e1642085c05
  docs/implementation/execution-progress-and-results.md: 75eac895fd3a3c930e5135a4ef57badb0c540bfa692d928188af9eeb2f73b803
inventory_fingerprints_sha256:
  data/win_poc_case.json: ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7
  pom.xml: f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6
  Dockerfile: 2aae6615e6c3dba5184064593e29407f7cded4e63dd416719a0b2fb30846d985
  gcp/README.md: 8294e4c3bbb7b92b0d7f51136b18aaa342fe3c9f06ec822dc23ce5a75c915913
  gcp/cloudbuild.yaml: 284663c833bb3550497f412e507e25a4d12b64f4bcae0ad993c9bd576f76b209
  gcp/workflows/optimization.yaml: 65eeef9344a63b743af1684b915c9bdfa976454e560ebb8624d75ed00fd874c1
  AlnsBatchEngine.java: 4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481
  OptimizationApiController.java: 3dcab11fcac78bb994b1f77bedaf425601684e8b9b3eb1560c305dcf671f358c
  OptimizationWorkerController.java: 846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c
  AlnsBatchEngineTest.java: 947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829
historical_cross_check:
  file: docs/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
  sha256: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
live_review_inventory:
  review_files_present: 15
  reviews_complete_or_final: 15
  phase_00: COMPLETE_PASS_WITH_RESIDUAL_BLOCKERS
  phase_01: FINAL_ACCEPTED_WITH_APPLIED_CORRECTIONS
  phase_02: FINAL_PASS_AFTER_APPLIED_CORRECTIONS
  phase_03: COMPLETE_CHANGES_REQUIRED
  phase_04: COMPLETE_CHANGES_REQUIRED
  phase_05: COMPLETE_CHANGES_REQUIRED
  phase_06: COMPLETE_CHANGES_REQUIRED
  phase_07: COMPLETE_CHANGES_REQUIRED
  phase_08: COMPLETE_CHANGES_REQUIRED
  phase_09: COMPLETE_CHANGES_REQUIRED
  phase_10: COMPLETE_CHANGES_REQUIRED
  phase_11: COMPLETE_CHANGES_REQUIRED
  phase_12: COMPLETE_CHANGES_REQUIRED
  phase_13: COMPLETE_V1_2_PASS_WITH_RESIDUAL_BLOCKERS
  phase_14: COMPLETE_CHANGES_REQUIRED_BLOCKED_NOT_READY
  semantics: DOCUMENT_REVIEW_ONLY_NOT_IMPLEMENTATION_OR_EVIDENCE_ACCEPTANCE
historical_authoring_snapshot:
  status: HISTORICAL_PROVENANCE_ONLY_NOT_LIVE_STATUS
  phase_00_through_11: ACTUAL_READY_FOR_REVIEW_NOT_ACCEPTED
  phase_12: ACTUAL_READY_FOR_REVIEW_NOT_STARTED_APPEARED_DURING_AUTHORING
  phase_13: ACTUAL_READY_FOR_REVIEW_GATED_NOT_STARTED_APPEARED_DURING_AUTHORING
  phase_14_review: PLANNED_NOT_PRESENT_AT_BASELINE
neighbor_validation_policy: STABLE_CITED_SECTION_SEMANTIC_REVIEW_NO_ADJACENT_OR_RECIPROCAL_DIGEST_ACCEPTANCE
```

## 1. 문서 지위, 권위와 source 해석

이 문서는 Phase 14를 실행하기 위한 상세 계약이다. 문서는 검토할 수 있는 상태지만
구현과 gate는 닫혀 있다. Source 문서의 `REVIEW` metadata는 provenance로 보존하되
문서 작성을 멈추는 조건으로 쓰지 않는다. 반대로 이 문서가 type, test, gate와
cutover 절차를 구체적으로 제안한다는 사실은 calibration 실행, official 수치 승인,
AWS 배포, production authority 또는 traffic cutover가 이루어졌다는 뜻이 아니다.

| 축 | 현재 값 | 의미 |
|---|---|---|
| Source authority | `USER_LOCKED_FOR_THIS_DOCUMENT_SET` | 사용자 선언과 정본 순서를 적용한다. |
| 문서 | `REVIEWED_WITH_CORRECTIONS` | 독립 리뷰의 안전·명백한 교정을 반영했다. Phase acceptance가 아니다. |
| 구현 Phase | `GATED` | 필수 predecessor와 authority receipt가 없다. |
| Calibration | `NOT_RUN` | 측정값, 통계 결과 또는 승인값을 주장하지 않는다. |
| Official manifest/run | `NOT_CREATED` / `NOT_RUN` | test manifest나 legacy 기본값을 공식값으로 승격하지 않는다. |
| Provider deployment | `NOT_DEPLOYED` | GCP 자료와 ignored/generated artifact는 AWS 배포 증거가 아니다. |
| Production authority | `NOT_GRANTED` | `Q-INFRA-01 RESOLVED`는 production 승인과 다르다. |
| Cutover | `NOT_STARTED` | Pointer, alias, endpoint 또는 traffic을 바꾸지 않았다. |
| Evidence/review | `NOT_PRODUCED` / `COMPLETE_CHANGES_REQUIRED` | Review 완료는 planned evidence나 Phase acceptance가 아니다. |

권위 적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 의미와 불변조건
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/port 배치
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 15 Phase 구조
6. [Master Realization Plan](../master-realization-plan.md), [진행 기록](../execution-progress-and-results.md), [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는
역사 cross-check에만 사용했다. `docs/codex/*`의 역사적 11-phase 계획, progress와
evidence 표현은 Phase 14 entry, calibration baseline, provider parity, official run
또는 production approval로 재사용하지 않는다. 현재 canonical Phase 00~11도 실제
accepted review와 immutable evidence receipt가 있어야만 소비한다.

### 1.1 직접 소비한 section과 semantic impact review

| Source | 직접 소비한 section | Phase 14에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §1~4, §7~8, §13~17 | Official reproducibility, two-gate publication, exact comparator, `Q-BENCH-02`, RM-6/RM-8, migration/rollback |
| [Final Domain](../../2026-07-26-domain-design.md) | §6~17, §18.1~18.2 | Travel authority, official worker completeness, candidate/result verification, hybrid gate |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2~6 | Module/port isolation, identity/retry, artifact/provenance, security/evidence |
| [Integrated design](../../architecture-domain-implementation-design.md) | §2~3, §12~26 | Phase 08~14, no-DB, AWS mapping, Phase 13 optionality, calibration/cutover, invariants |
| [질문 등록부](../../master-design-open-questions.md) | `Q-MTX-01~03`, `Q-BENCH-01~03`, `Q-INFRA-01`, `Q-VAR-01`, §3~4 | Integer travel, open official execution values, AWS selection/cutover separation, deferred boundary |
| [Master Realization Plan](../master-realization-plan.md) | §2~4, §6~15, Phase 00~14 | Current inventory, predecessor evidence, Phase 14 entry/exit/DoD/rollback |
| [Execution Progress](../execution-progress-and-results.md) | §1~9 | Scheduler-only status authority, current blockers, no implementation completion claim |
| [Actual Phase 02](phase-02-prepared-travel-immutable-problem.md) | §13.3 only | Test-only travel artifact의 official 자동 승격 금지, separate approved fixture/fingerprint |
| [Actual Phase 07](phase-07-independent-verification-final-result.md) | §15.3 only | Publishable result + official manifest, test-only/different fingerprint 비교 금지 |
| [Actual Phase 11](phase-11-aws-reference-distribution.md) | §18.3 only | Phase 11 evidence/deployment/rollback은 entry input이지 production authority가 아님 |
| [Actual Phase 12](phase-12-provider-substitution.md) | §18.4 only | Provider evidence는 separate adoption/cutover input일 뿐 production default/authority가 아님 |
| [Actual Phase 13 v1.2](phase-13-optional-hybrid-route-selection.md) | §6.5, §14.3~§14.4 only | Scheduler-owned gate-closed skip와 accepted/approved hybrid handoff 두 branch; signed applicability envelope + action-time verification schema, Phase 14→13 reverse entry edge 금지 |
| [Root README](../../../README.md)와 inventory | 기술·배포·placeholder | 현재 Java/GCP placeholder와 future target/evidence 분리 |

인접 Phase는 위 stable section을 직접 읽어 의미와 accepted artifact/evidence identity를
대조한다. Line number, section bytes digest, 인접 whole-file hash 또는 reciprocal
fingerprint는 compatibility나 gate acceptance가 아니다. 인접 section이 바뀌면 변경된
계약의 semantic impact와 producer의 accepted implementation/evidence를 다시 review한다.
Hash 일치만으로 compatibility receipt를 만들거나 gate를 열지 않는다.

### 1.2 Live review inventory와 historical authoring snapshot

현재 독립 review 파일은 15/15가 `COMPLETE` 또는 `FINAL`이다. 아래 verdict는
**document review 결과만** 나타낸다.

| Phase | Current review lifecycle/verdict | Implementation/evidence/Phase 의미 |
|---:|---|---|
| [00](../reviews/phase-00-review.md) | `COMPLETE — PASS_WITH_RESIDUAL_BLOCKERS` | `NOT_STARTED` / `NOT_PRODUCED`; implementation authorization/acceptance 아님 |
| [01](../reviews/phase-01-review.md) | `FINAL — ACCEPTED_WITH_APPLIED_CORRECTIONS` | Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [02](../reviews/phase-02-review.md) | `FINAL — PASS_AFTER_APPLIED_CORRECTIONS` | Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [03](../reviews/phase-03-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; not accepted |
| [04](../reviews/phase-04-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; not accepted |
| [05](../reviews/phase-05-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; not accepted |
| [06](../reviews/phase-06-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; not accepted |
| [07](../reviews/phase-07-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [08](../reviews/phase-08-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [09](../reviews/phase-09-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [10](../reviews/phase-10-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; acceptance `NOT_RECOMMENDED` |
| [11](../reviews/phase-11-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [12](../reviews/phase-12-review.md) | `COMPLETE — CHANGES_REQUIRED` | `NOT_STARTED` / `NOT_PRODUCED`; Phase acceptance `BLOCKED_NOT_IMPLEMENTED` |
| [13](../reviews/phase-13-review.md) | `COMPLETE — PASS_WITH_RESIDUAL_BLOCKERS` | `GATED_NOT_STARTED_NOT_ACCEPTED`; evidence `NOT_PRODUCED` |
| [14](../reviews/phase-14-review.md) | `COMPLETE — CHANGES_REQUIRED` | `BLOCKED_NOT_READY`; implementation `GATED_NOT_STARTED`, evidence `NOT_PRODUCED` |

따라서 review verdict 분포는 `CHANGES_REQUIRED` 11개(03~12, 14),
`PASS_WITH_RESIDUAL_BLOCKERS` 2개(00, 13),
`ACCEPTED_WITH_APPLIED_CORRECTIONS` 1개(01),
`PASS_AFTER_APPLIED_CORRECTIONS` 1개(02)다. Review 15/15 완료는 accepted
implementation/evidence graph, official calibration, provider deployment, cutover 또는
production authority를 만들지 않는다.

Metadata의 `historical_authoring_snapshot`은 작성 당시 provenance만 보존한다. 그 안의
Phase 00~11 `READY_FOR_REVIEW`, Phase 12/13 `APPEARED_DURING_AUTHORING`, Phase 14 review
`PLANNED_NOT_PRESENT_AT_BASELINE`은 **현재 live status가 아니며** entry, handoff,
acceptance 또는 scheduler status에 사용하지 않는다.

## 2. 목표, 범위, 비범위와 불변조건

### 2.1 목표

1. Calibration 입력·실행·분석을 사전 등록하고 승인 전 산출물을
   `EXPERIMENT_ONLY`로 격리한다.
2. `Q-BENCH-02` 공식 실행 수치와 공식 ALNS parameter를 서로 다른 signed approval로
   고정한다.
3. Approved integer travel fixture와 Great Circle policy를 서로 다른 immutable
   authority artifact로 고정한다.
4. 같은 semantic/build/config/seed/provider envelope에서 all-declared-worker,
   both-verifier, exact comparator와 replay gate를 통과한 official result만 만든다.
5. 실제 provider/deployment evidence와 production authority를 분리하고 shadow,
   proposed canary, hold, activation, rollback을 auditable state machine으로 제어한다.
6. 완료 후 `ImplementationEvidenceIndex`와 `ProductionActivationRecord`를 운영에
   handoff하되 원시 credential, secret, PII와 mutable “latest”를 권위로 넘기지 않는다.

### 2.2 포함 범위

- Phase 00~12 applicable accepted evidence receipt와 Phase 13 applicability 판정
- Calibration corpus/plan/candidate matrix/seed/environment freeze
- Independent verification, statistical analysis와 approval record
- `Q-BENCH-02` 수치, ALNS parameter, travel fixture, Great Circle policy의 separate gate
- Official manifest/card, official run, exact Win comparator와 replay
- AWS reference 또는 별도 승인된 substituted provider의 deployment evidence verification
- Legacy/current/target compatibility matrix, shadow와 versioned endpoint/adapter
- Artifact/state copy digest, publication/traffic pointer CAS, rollback rehearsal
- Security/access/audit/retry/recovery/observability/performance/cost evidence
- Scoped production approval, proposed canary/activation과 rollback record
- 최종 evidence index, activation record, 운영 runbook handoff

### 2.3 명시적 비범위

- `Q-BENCH-02`, ALNS parameter, 통계 threshold, SLO, cost limit, canary 비율/시간을 추정해 채우기
- Current decimal `data/win_poc_case.json`을 자동 반올림해 official fixture로 만들기
- Great Circle earth model/constant/library/version을 구현자 편의로 고르기
- Phase 13을 calibration 또는 cutover를 위해 강제 실행하거나 default 활성화하기
- `C-17` gate, solver/license/native/fallback approval를 Phase 14가 대신 닫기
- Phase 12 future provider를 빈 module로 만들거나 채택하지 않은 provider를 배포하기
- Legacy GCP/Cloud Run/Workflows/Cloud Storage 자료를 AWS parity나 rollback authority로 승격하기
- `docs/codex` 역사 11-phase result/evidence, ignored `target/` 또는 `.serverless/` 생성물을 재사용하기
- Production traffic을 바꾸는 보편적 shell command, credential, account/region 또는 secret을 문서에 고정하기
- Legacy artifact/state/KMS key/versioned endpoint 삭제와 그 밖의 irreversible decommission
- Optional variant, multi-trip/rotation, dynamic routing 또는 academic benchmark 확장

### 2.4 고정 불변조건

1. Official 값은 signed approval artifact에 explicit하게 존재하며 hidden default가 0개다.
2. `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`는 measured review와 explicit approval 전까지
   닫히지 않는다.
3. Official ALNS parameter는 execution budget과 분리된 identity와 approval을 갖는다.
4. Integer fixture는 모든 provided `D/U`가 integer meter/second이며 source/version/digest가
   승인돼야 한다.
5. Great Circle policy는 함수 ID/version, earth model/constants, coordinate contract,
   precision, meter `HALF_UP`과 approved reference vector를 가진다.
6. Generated `U`는 vehicle별 `CEILING(D_meter × 3.6 ÷ speed_km_h)` integer second이고,
   speed가 missing일 때만 `45 km/h`를 사용하며 present-invalid speed는 거부한다.
   Provided/generated와 speed source provenance를 cell/vehicle authority에 보존한다.
7. Complete provided matrix라도 production system의 missing-`D` 경로를 위해 Great Circle
   gate를 생략하지 않는다.
8. Same official manifest 비교에서만 quality regression을 판정한다.
9. Exact Win comparator는 미배정 수 → 배차 차량 수 → 전체 거리 → 전체 운영시간이다.
10. 모든 declared worker가 정상 완료·검증되지 않으면 official round/run은 `INCOMPLETE`다.
11. Candidate verifier와 result verifier 둘 다 `PASS`가 아니면 publication/benchmark하지 않는다.
12. Seed를 실행 뒤 선별하거나 실패 seed를 제외하지 않는다.
13. Elapsed/completion order는 quality, seed 또는 fingerprint의 hidden input이 아니다.
14. Phase 13 gate가 닫히면 `ALNS_ONLY`가 정상 branch이며 hybrid code를 호출하지 않는다.
15. Official hybrid이면 Phase 13 accepted evidence와 explicit activation approval을 모두 요구한다.
16. Provider deployment evidence는 production authority가 아니며 그 역도 성립하지 않는다.
17. 모든 traffic/pointer transition은 exact expected version과 approval scope를 CAS로 확인한다.
18. Artifact는 create-once immutable이고 cutover/rollback은 pointer/alias/traffic만 바꾼다.
19. Stop condition의 evidence가 없거나 telemetry/audit가 불완전하면 fail closed/hold한다.
20. Rollback target과 권한이 검증되기 전 canary/activation을 시작하지 않는다.
21. Irreversible delete/revoke/destructive transform은 이 Phase의 activation authority에 포함되지 않는다.

## 3. Entry gate와 exact evidence verification

Phase 14는 하나의 큰 승인으로 모든 단계를 여는 phase가 아니다. 각 work package는
필요한 gate만 소비하며, 뒤 gate가 닫혔다는 이유로 앞의 문서 review와 test 설계를
중단하지 않는다. Official label과 production action은 아래 AND gate를 우회할 수 없다.

### 3.1 Gate matrix

| Gate | 현재 | 별도 authority | 필요한 signed/immutable evidence | 닫는 범위 |
|---|---|---|---|---|
| `G14-PREDECESSOR` | `CLOSED` | Scheduler + independent reviewers | Phase 00~11 accepted review/evidence index, build/source identities, rollback points | Target implementation, official run, cutover |
| `G14-P13-APPLICABILITY` | `CLOSED` | Scheduler + Product + Algorithm + Architecture | Actual Phase 13 v1.2의 `Phase13ApplicabilityReceipt.Skip` 또는 `Phase13ApplicabilityReceipt.Activated`, exact receipt/subject/scope digest, signed applicability envelope, action-time trust/validity/revocation/freshness verification receipt | Manifest algorithm branch |
| `G14-INTEGER-FIXTURE` | `CLOSED` | Input/Matrix + Benchmark | Integer fixture/snapshot bytes digest, schema/version, all-cell validation, provided/generated provenance, canonical generated-`U`/speed-source validation, source/migration approval, expected coverage | Official calibration/result |
| `G14-GREAT-CIRCLE` | `CLOSED` | Input/Matrix + Domain/Architecture | Function/policy ID/version, earth model/constants, coordinate rules, precision, reference vectors, signature | Missing-`D` implementation/production readiness |
| `G14-ALNS-PARAMETERS` | `CLOSED` | Algorithm + Quality | Operator/repair/acceptance/adaptive/state-strategy config, units/ranges/version, measured rationale, signature | Official algorithm config |
| `G14-QBENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Benchmark/Quality | Approved `screenMaxSteps`, worker count, `phase2MaxSteps`, `maxRounds`, watchdog/resource policy, seed derivation; calibration result refs | Official execution manifest/baseline |
| `G14-SIGNING-TRUST` | `CLOSED` | Security + Release | Signature profile/trust roots/revocation/time/freshness policy, canonical encoding, verifier build digest, negative-test evidence | 모든 signed gate consumption |
| `G14-PROVIDER-DEPLOYMENT` | `CLOSED` | Platform + Security + Ops + FinOps | Actual environment deployment manifest, IaC/change-set digest, artifact/image revision, IAM/KMS/network/alarms/quota/retention/cost/rollback evidence | Shadow/canary/cutover |
| `G14-CALIBRATION-ACCEPTANCE` | `CLOSED` | Benchmark/Quality + independent verifier | Frozen plan/result, declared analysis method, all run refs, limitations, approved thresholds and decision | Official values/manifest seal |
| `G14-PRODUCTION-AUTHORITY` | `NOT_GRANTED` | Product + Release + Security + Ops | Environment/action/manifest/deployment/pointer scope, traffic bound, validity window, approvers, rollback authority, signatures | Canary/activation only |

Phase 13 applicability의 schema/signature field 부재는
`RESOLVED_BY_PHASE13_V1_2`다. Phase 13 v1.2 §6.5/§14.3~§14.4가 `Skip`과
`Activated` 모두에 signed envelope와 action-time verification reference를 제공한다.
이 상태는 **contract field gap만** 해소한다. Signature algorithm, trust root/store,
revocation/time/freshness policy 값은 계속 `OPEN/GATED_NOT_APPROVED`이고 actual signed
envelope/verification receipt는 `NOT_PRODUCED`이므로 `G14-P13-APPLICABILITY`와
`G14-SIGNING-TRUST`는 계속 닫혀 있다.

각 evidence envelope는 최소한 다음을 가진다.

```text
SignedEvidenceEnvelope
  schemaVersion
  evidenceKind
  subjectDigest
  canonicalPayloadDigest
  issuerRole
  signerIdentityRef
  signatureProfileId
  signatureValue
  signingKeyId
  signedAt
  validFrom / expiresAt
  approvalScope
  sourceCommit
  predecessorEvidenceDigests[]
  immutableLocator
  revocationPolicyRef
```

정확한 signature algorithm, certificate format와 trust store 배치는 현재 source에서
확정되지 않았다. 이는 `PROPOSED` contract이며 Security/Release가
`G14-SIGNING-TRUST`에서 승인한다. Shared-secret HMAC, repository에 들어간 private key,
test signer, self-signed local receipt는 official approval로 허용하지 않는다.

Envelope bytes, payload와 locator binding은 immutable이고 overwrite하지 않는다. Signature가
한 번 valid했다는 사실은 이후 revocation/expiry를 동결하지 않는다. Gate를 소비할 때마다
verifier build, trust-root set digest, revocation/time/freshness authority snapshot, checked
scope/action, checked-at와 verdict를 가진 별도 immutable
`SignedEvidenceVerificationReceipt`를 만든다.
Mutable OCSP/CRL endpoint, console status 또는 과거 `PASS` 한 줄을 evidence bundle에 직접
pin하지 않으며, production action 직전 current policy에 따라 다시 검증한다.

### 3.2 Phase 00~12 evidence receipt

아래 key는 future accepted evidence의 exact 최소 index이며 현재 evidence가 아니다.
모든 receipt는 evidence digest, accepted review ref, source/build fingerprint, known
limitation과 rollback point를 함께 검증한다.

| Phase | 필수 accepted evidence | Phase 14가 확인하는 핵심 |
|---:|---|---|
| 00 | `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY` | Reproducible reactor, dependency guard, legacy characterization |
| 01 | `E-P01-NUMERIC`, `E-P01-TIME`, `E-P01-COMPAT`, `E-P01-ERROR` | Canonical integer/time/service/profile input authority |
| 02 | `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM` | Complete directed travel/problem identity; test-only는 official 아님 |
| 03 | `E-P03-PROPAGATION`, `E-P03-EVALUATION`, `E-P03-COMPARATOR` | Cache-free physical facts와 exact comparator |
| 04 | `E-P04-BINDING`, `E-P04-ISOLATION`, `E-P04-FACET` | Exact approved profile/preset/capability identity |
| 05 | `E-P05-PAIR`, `E-P05-INSERTION`, `E-P05-PORTFOLIO` | Pair/bank invariant와 4×2 portfolio |
| 06 | `E-P06-COW`, `E-P06-ALNS`, `E-P06-REPLAY` | Explicit config, completed-step, fixed-envelope replay |
| 07 | `E-P07-CANDIDATE-VERIFY`, `E-P07-AUDIT`, `E-P07-RESULT-VERIFY` | Both-gate publishable result |
| 08 | `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY` | Provider-neutral local oracle와 identity |
| 09 | `E-P09-STORAGE-CONTRACT`, `E-P09-CAS`, `E-P09-TENANT` | Immutable artifact, exact-key, pointer CAS, tenant isolation |
| 10 | `E-P10-STATE`, `E-P10-COMPLETENESS`, `E-P10-RETRY` | Declared worker completeness, stable fan-in, retry/cancel |
| 11 | `E-P11-AWS-CONTRACT`, `E-P11-PARITY`, `E-P11-SECURITY`, `E-P11-OPERATIONS`, `E-P11-ROLLBACK` | Actual AWS reference semantics and operational readiness |
| 12 | Applicable이면 `E-P12-PROVIDER-CONTRACT`, `E-P12-PARITY`, `E-P12-MIGRATION`, `E-P12-SECURITY`, `E-P12-OPERATIONS`, `E-P12-PERFORMANCE`, `E-P12-ROLLBACK` | Selected/evaluated provider의 digest-preserving parity와 bounded adoption input |

Phase 12가 적용되지 않으면 scheduler가 서명한 `NOT_APPLICABLE` receipt에
`selectedProvider=AWS_REFERENCE`, 판단 source와 scope를 기록한다. `NOT_APPLICABLE`은
Phase 12를 `ACCEPTED`로 위장하지 않는다. AWS 이외 provider를 선택한 상태에서 Phase 12
evidence를 생략할 수 없다. 또한 actual Phase 13의 `Activated` handoff는 그 문서가
요구하는 accepted Phase 12 evidence를 전제로 하므로, official hybrid branch에서
Phase 12를 `NOT_APPLICABLE`로 처리해 그 gate를 우회하지 않는다.

### 3.3 Exact verification procedure

1. Evidence locator에서 bytes를 읽고 digest를 확인한 뒤에만 deserialize한다.
2. Approved signature profile, trust chain, role, validity와 current
   revocation/time/freshness authority를 확인하고 immutable verification receipt를 만든다.
3. `subjectDigest`가 현재 fixture/config/build/deployment bytes와 같은지 재계산한다.
4. Predecessor evidence graph에 missing node, cycle, mixed source commit 또는 stale review가
   없는지 확인한다.
5. Environment/action scope가 현재 요청과 exact match인지 확인한다.
6. Unknown field, hidden default, absent numeric value, test signer와 expired approval을 거부한다.
7. Gate별 verifier report를 별도 immutable artifact로 저장한다.
8. 모든 required report `PASS` 뒤에만 다음 state transition command를 만든다.

검증 실패는 숫자를 다시 주입하거나 legacy/default/mock evidence로 보완하지 않는다.
Gate owner에게 typed failure와 last safe state를 handoff한다.

## 4. Current inventory와 proposed change tree

### 4.1 2026-07-28 actual inventory

| 영역 | 실제 상태 | Phase 14 판정 |
|---|---|---|
| Build | 단일 root `pom.xml`, Java 25/Maven enforcer, Google Cloud SDK dependency | Target reactor/Phase acceptance가 아님 |
| Application | `com.ronext.optimizer.*` 아래 HTTP/controller와 `AlnsBatchEngine` | Canonical RPDPTW application/calibration/cutover 없음 |
| Solver | `AlnsBatchEngine`이 `double` synthetic objective를 계산 | Official ALNS 또는 benchmark evidence로 사용 금지 |
| API config | `parallelRuns=8`, `iterationsPerRun=5000`, clock seed fallback | Legacy hidden/default characterization only; official 값 금지 |
| Fan-in | GCS prefix listing과 scalar `double objective` 최소 선택 | Declared completeness/exact comparator/both-gate를 만족하지 않음 |
| Deployment | Docker, GCP Cloud Build/Run/Workflows/Cloud Storage 자료 | Historical migration inventory; AWS production evidence 아님 |
| AWS | Target adapter/IaC/deployment evidence 없음 | `G14-PROVIDER-DEPLOYMENT CLOSED` |
| Data | `data/win_poc_case.json`, SHA-256 `ea003...b7d7`, 14,157,512 bytes | Read-only primary source candidate지만 decimal `D/U`로 official 사용 불가 |
| Phase docs | Phase 00~13 actual detail; 12 `NOT_STARTED`, 13 `GATED_NOT_STARTED` | Detail 존재는 implementation/evidence acceptance가 아님 |
| Review/evidence | Review 15/15 완료: 00/13 `PASS_WITH_RESIDUAL_BLOCKERS`, 01 `ACCEPTED_WITH_APPLIED_CORRECTIONS`, 02 `PASS_AFTER_APPLIED_CORRECTIONS`, 03~12/14 `CHANGES_REQUIRED`; accepted implementation/evidence bundle 없음 | Document review와 implementation acceptance 분리; Phase 14 entry closed |

현재 fixture의 `distanceMatrix`는 205,209 cell이고 다수 `D/U`가 decimal text다. 이
문서는 이를 integer로 변환하지 않는다. Conversion이 제안되면 source digest, exact
conversion rule, information-loss analysis, expected integer bytes와 Input/Matrix 및
Benchmark 승인을 새 artifact로 받아야 하며, 단순 반올림은 승인으로 간주하지 않는다.

### 4.2 Proposed change tree

아래는 Phase 00~13 target이 accepted된 뒤의 **candidate internal tree**다. 현재 파일
존재나 public API를 주장하지 않으며 accepted module/package ADR이 우선한다.

```text
rpdptw/application/
└── src/main/java/com/ronext/rpdptw/application/
    ├── calibration/
    │   ├── CalibrationPlanService.java
    │   ├── CalibrationRunner.java
    │   ├── CalibrationAcceptanceEvaluator.java
    │   └── OfficialManifestFactory.java
    ├── official/
    │   ├── OfficialBenchmarkService.java
    │   ├── OfficialRunCompletenessVerifier.java
    │   └── ImplementationEvidenceIndexer.java
    └── cutover/
        ├── CutoverCoordinator.java
        ├── CutoverStateMachine.java
        ├── ProductionAuthorityVerifier.java
        └── RollbackCoordinator.java

adapters/common/
└── src/main/java/com/ronext/rpdptw/adapter/common/
    ├── evidence/
    └── calibration/

apps/coordinator/
└── src/main/java/com/ronext/rpdptw/app/coordinator/
    ├── OfficialRunCommandHandler.java
    └── CutoverCommandHandler.java

distributions/aws-serverless/
├── src/main/resources/calibration/
└── src/test/java/com/ronext/rpdptw/distribution/aws/phase14/

deployment/aws/
├── parameters/
│   ├── calibration.example.json
│   └── cutover.example.json
├── state-machines/
│   └── cutover-actions.asl.json
├── alarms/
│   └── phase14-stop-conditions.yaml
└── runbooks/
    ├── shadow.md
    ├── activation.md
    └── rollback.md

build/test-fixtures/
└── src/test/java/com/ronext/rpdptw/testfixture/phase14/

build/port-contract-tests/
└── src/test/java/com/ronext/rpdptw/contract/phase14/
```

Production value, approval signature와 secret은 source/resource example에 넣지 않는다.
`*.example.json`은 field/schema 설명만 소유하고 official value가 누락된 채 실행되면
validation failure여야 한다. Phase 13 gate가 닫힌 ALNS-only branch에서는
`solver/pool`, `solver/selection`, `application/hybrid` 또는 vendor adapter를 Phase 14
때문에 생성하지 않는다.

`build/test-fixtures`의 Phase 14 builder/oracle은 test source와 Phase 00이 승인할 attached
tests classifier/test-scope contract로만 소비한다. Production main/runtime artifact,
official authority factory, signing key 또는 cutover action implementation을 포함하지 않는다.

### 4.3 Dependency direction

```text
core / solver / verification
            ↑
      rpdptw-application
            ↑
 adapters/common evidence verifier
            ↑
 apps/coordinator + selected distribution
            ↑
 deployment/runbook
```

- Application은 `SignedEvidenceVerifier`, `ArtifactStore`, `RunStateRepository`,
  `WorkflowExecutionPort`, `ResultPublisher`, `TelemetryPort` 같은 provider-neutral
  port만 소비한다.
- AWS signature/KMS/IAM/change-set/event DTO는 adapter/distribution 경계에만 둔다.
- `CutoverStateMachine`은 comparator, solver, candidate/result verifier를 재구현하지 않는다.
- Verification은 solver/search/cache/provider/cutover package를 compile-depend하지 않는다.
- Deployment config는 semantic/algorithm 숫자의 source가 아니다.

## 5. Input/output artifact, contract, identity와 lifecycle

### 5.1 Authority artifact

| Artifact | Authority owner | 핵심 identity | Lifecycle |
|---|---|---|---|
| `IntegerTravelFixtureAuthority` | Input/Matrix + Benchmark | Fixture bytes/schema/source/migration digest | Draft → independently validated → signed approved → immutable |
| `GreatCirclePolicyAuthority` | Input/Matrix + Domain/Architecture | Function/version/earth model/constants/precision/vector digest | Proposed → reference-vector verified → signed approved |
| `OfficialAlnsParameterAuthority` | Algorithm + Quality | Full parameter canonical digest | Experiment candidate → measured → signed approved |
| `OfficialExecutionValueAuthority` | Benchmark/Quality | `Q-BENCH-02` values + calibration result digest | Experiment-only → signed approved |
| `CalibrationPlan` | Benchmark/Quality | Corpus/build/config/seed/environment/analysis digest | Draft → preregistered/frozen → executable |
| `CalibrationResult` | Calibration runner + independent verifier | Plan + declared run set + raw/result/analysis digests | Running → complete/incomplete → independently reviewed |
| `OfficialExecutionManifest` | Official manifest authority | All semantic/algorithm/execution/build/provider authority digests | Draft → sealed immutable template → controlled run/replay receipts |
| `OfficialBenchmarkCard` | Benchmark + verifier | Manifest/result/comparator/champion/replay digest | Candidate → verified → approved baseline/challenger |
| `ProviderDeploymentEvidence` | Platform/Release/Security/Ops | Environment/revision/IaC/resource/security/rollback digest | Candidate deployment → verified immutable receipt |
| `ProductionAuthorityEnvelope` | Product/Release/Security/Ops | Exact action/environment/manifest/deployment/pointer/version scope | Proposed → signed active → used/expired/revoked |
| `CutoverRecord` | Cutover coordinator + audit | Plan/state/event/pointer/observation digest | Append-only transitions |
| `RollbackRecord` | Rollback owner | Trigger/last-safe/restore/reconciliation digest | Requested → executed → independently verified |
| `ProductionActivationRecord` | Release/Ops + independent reviewer | Final active manifest/deployment/pointer/evidence index digest | Created only after active verification |
| `ImplementationEvidenceIndex` | Phase 14 + scheduler | Complete evidence graph root digest | Draft → closure checked → sealed handoff |

### 5.2 Identity separation

```text
semantic identity:
  canonical input/problem/prepared travel/profile/policy fingerprints

algorithm identity:
  portfolio/operator/repair/acceptance/adaptive/state-strategy versions
  official ALNS parameter authority digest
  optional Phase 13 pool/projection/backend identities

execution identity:
  screen/worker/step/round/watchdog/resource values
  seed derivation and declared logical-run/worker lineage
  invocation-specific run/replay role and attempt lineage in append-only execution receipts

build/runtime identity:
  source commit, dependency lock, artifact/image digest, JDK/runtime/architecture

provider deployment identity:
  provider/stage/region-class, IaC/change set, function/workflow/storage revision

production activation identity:
  authority envelope, previous/desired pointer versions, traffic scope, audit record
```

Provider account/ARN/bucket/key, execution ID, attempt ID와 credential은 semantic result
identity가 아니다. Restricted opaque reference는 deployment/audit metadata에 둘 수 있다.
Secret/license/private key value는 어떤 fingerprint에도 넣지 않는다.

### 5.3 Phase 13 two-branch contract

```text
Phase13ApplicabilityReceipt
  Skip
    reason = C17_GATE_CLOSED
    receipt schema/version
    schedulerDecision
    exact ALNS-only plan identity
    signedApplicabilityEnvelope: SignedApplicabilityEnvelopeRef
      envelope ref/digest
      exact subject/scope + signature-profile/trust-policy refs
      validity window + revocation-policy ref
    actionTimeVerification: ApplicabilityActionTimeVerificationRef
      verification receipt
      verifier build/trust-root-set digests
      revocation/freshness snapshots + checked Phase 14 action/scope
      verdict = PASS
    pool/model/outcome/hybrid refs = ABSENT
    E-P13 implementation refs = ABSENT
  or
  Activated
    Phase13ActivatedHandoff
    signedApplicabilityEnvelope: SignedApplicabilityEnvelopeRef
    actionTimeVerification: ApplicabilityActionTimeVerificationRef
      trust/validity/revocation/freshness verification receipt
    E-P13-GATE/POOL/SELECTION/HYBRID
    E-P13-SECURITY-LICENSE-OPS-COST
    E-P13-SHADOW-ROLLBACK
    accepted Phase13 review
    last accepted ALNS-only rollback point
```

- `Skip`: Phase 13 actual §6.5/§14.3의 scheduler-owned applicability control record다.
  Phase 13을 실행하거나 `ACCEPTED`로 만들지 않고 ALNS-only manifest만 허용한다.
- `Activated`: Phase 13 actual §6.5/§14.4의 exact activated handoff다. Accepted Phase 13
  evidence, exact hybrid plan, backend/license/native/fallback approval, reproducibility
  class와 Phase 13이 요구한 Phase 12 accepted receipt를 모두 포함한다.

두 branch의 schema/signature field 부재는 `RESOLVED_BY_PHASE13_V1_2`지만 actual
signed envelope와 current action-time `PASS` receipt 없이는 어느 branch도 소비하지
않는다. `G14-SIGNING-TRUST`는 Phase 14의 downstream consumption gate이며 Phase 13
implementation entry나 Phase 13 evidence prerequisite로 역전하지 않는다.
`Phase 13 → Phase 14 → Phase 13` dependency cycle은 금지한다.

Phase 14는 Phase 13을 강제 실행하지 않는다. Hybrid가 더 좋아 보인다는 calibration
결과만으로 `C-17`을 닫거나 production default를 바꾸지 않는다.

### 5.4 Lifecycle and commit boundary

```text
explicit experiment values
→ frozen CalibrationPlan
→ declared CalibrationRun set
→ complete raw measurements
→ independent recomputation/analysis
→ CalibrationResult
→ signed ALNS parameter approval
→ signed Q-BENCH-02 execution-value approval
→ sealed OfficialExecutionManifest
→ all-declared-worker official run
→ both-gate verified OfficialBenchmarkCard
→ identical-manifest replay
→ provider shadow evidence
→ scoped production authority
→ proposed canary/hold/activation
→ ProductionActivationRecord
```

각 화살표는 새 immutable artifact를 만들고 이전 artifact를 덮어쓰지 않는다.
`CalibrationResult`가 승인값을 스스로 만들지 않으며, 승인 record가 raw measurement를
바꾸지 않는다. Cutover는 verified result artifact를 복사/삭제하지 않고 authoritative
pointer와 traffic scope만 CAS로 전환한다.

## 6. Calibration experiment protocol

### 6.1 Preregistration과 freeze

`CalibrationPlan`은 첫 measured run 전에 승인·동결한다.

```text
CalibrationPlan
  planId / schemaVersion
  purpose and experimentOnly = true
  corpusManifest
  integerTravelAuthorityRef
  greatCirclePolicyAuthorityRef
  problem/profile/preset fingerprints
  candidateAlnsParameterSets[]
  candidateExecutionValueSets[]
  sourceCommit / clean-tree receipt
  build/dependency/artifact/image/runtime fingerprints
  provider/environment/resource/concurrency fingerprints
  seedDerivationVersion
  exact declared baseSeeds[]
  exact case × candidate × seed × repeat run matrix
  warm-start and round lineage
  analysisMethod
  acceptancePolicyRef
  independentVerifierBuildRef
  security/cost/observability collection plan
  exclusion/abort rules
  owners/signatures
```

Corpus 구성, candidate set 수, seed 수, repeat 수, confidence level, statistical threshold,
performance/cost/SLO threshold는 현재 미확정이다. 각 값은 plan에서 explicit하며
`OPEN_PENDING_CALIBRATION_PROTOCOL_APPROVAL`로 남긴다. 누락값을 library default로
채우지 않는다.

Freeze 대상:

1. Fixture/corpus bytes와 schema, integer travel와 Great Circle authority
2. Source commit, dirty-tree 상태, dependency lock와 build artifact/image digest
3. JDK/runtime/CPU architecture와 determinism-relevant environment
4. Profile/preset, semantic policy와 exact algorithm config
5. Candidate parameter/config set와 execution values
6. Base/derived seed, derivation function/version와 stable ordering
7. Provider stage/region/resource/concurrency/retry/watchdog configuration
8. Verifier build, comparator, canonical serializer와 analysis implementation
9. Security/telemetry/cost collection schema와 redaction policy
10. Exclusion, abort, missing-run 처리와 acceptance method

### 6.2 Official execution values와 ALNS parameter 분리

`Q-BENCH-02` authority는 다음만 소유한다.

```text
screenMaxSteps
phase2WorkerCount
phase2MaxSteps
maxRounds
watchdogPolicy
resourcePolicy
seedDerivationPolicy
```

`OfficialAlnsParameterAuthority`는 다음 candidate field를 소유한다. Exact field set은
Phase 06 accepted config contract와 호환성 review 뒤 확정하므로 현재 `PROPOSED`다.

```text
portfolioVersion
destroyOperatorSet / repairOperatorSet
operatorSelectionPolicyVersion
initialOperatorWeights
rewardVector
adaptiveUpdateParameters
acceptancePolicyVersion
temperatureScheduleParameters
boundedImprovementPolicy
stateStrategy = COW
stableOrderingAndTieBreakVersion
```

두 authority는 서로의 값을 포함하지 않고 digest로 참조한다. Step/worker/round를
바꾸면서 같은 ALNS parameter approval을 유지할 수 있는지는 compatibility policy가
명시해야 한다. 하나의 calibration 결과에서 값 일부를 cherry-pick해 새 조합을 만들면
새 candidate이며 재측정·재승인이 필요하다.

### 6.3 Run execution and independent verification

1. Preflight가 모든 frozen digest와 signature를 재검증한다.
2. Declared run matrix를 exact-key artifact로 materialize하고 listing으로 completeness를
   판단하지 않는다.
3. 각 run은 same logical run identity, candidate set, case, seed, warm start와 requested
   work를 기록한다.
4. Retry는 same logical identity/seed/config/work를 유지하고 새 `AttemptId`만 갖는다.
5. Watchdog/resource/platform/cancel/failure는 정상 max-step으로 다시 이름 붙이지 않는다.
6. 모든 declared run의 raw trace/result/telemetry/cost가 있어야 candidate set이 분석 가능하다.
7. Independent verifier는 solver cache/summary를 사용하지 않고 candidate와 result를
   각각 재계산한다.
8. 독립 분석기는 run manifest와 raw evidence에서 result table을 다시 만들고 producer
   summary digest와 비교한다.
9. Missing/failed run을 버리거나 좋은 seed만 선택하지 않는다.
10. Incomplete candidate set은 `INCOMPLETE`, 손상 evidence는 `INVALID_EVIDENCE`로 남긴다.

### 6.4 Statistical and acceptance method

다음 method shape는 `PROPOSED`; 숫자 threshold와 표본 크기는 open이다.

- 동일 corpus case/seed/repeat를 block으로 한 paired comparison을 사용한다.
- Quality는 exact four-component lexicographic vector로 case별 win/tie/loss와 각
  component delta를 보고하며 임의 scalar score로 평탄화하지 않는다.
- Reliability는 all-declared-run completion, both-verifier pass와 typed termination
  distribution을 별도 hard gate로 보고한다.
- Strong-reproducibility 대상은 identical manifest replay의 trace/result/payload
  fingerprint exact equality를 요구한다.
- Performance와 cost는 parsing/search/verification/finalization/provider 단계별
  분포와 paired delta를 보고한다.
- 승인된 경우 paired bootstrap 또는 동등한 사전 등록 method를 사용할 수 있으나
  confidence level, resample count, interval type와 multiple-comparison correction은
  `AcceptancePolicy`에 explicit해야 한다.
- Acceptance predicate는 first run 전에 versioned expression으로 고정한다. Threshold가
  하나라도 absent면 자동 선택하지 않고 `CALIBRATION_GATE_BLOCKED`다.
- Raw distribution, outlier policy, censored/failed run과 limitation을 모두 공개한다.
- Winner는 하나의 완결된 parameter/config envelope다. 서로 다른 후보의 최선 성분을
  합쳐 fabricated candidate를 만들지 않는다.

Independent reviewer는 plan preregistration timestamp가 첫 run보다 이른지, 분석 code
digest가 고정됐는지, 모든 declared run이 포함됐는지와 producer/independent result가
같은지 확인한다.

### 6.5 Reproducibility and replay

Strong replay envelope:

```text
problem + integer travel + Great Circle policy
+ profile/preset/semantic policy
+ ALNS parameters + Q-BENCH-02 execution values
+ build/dependency/runtime
+ base/derived seeds and stable order
+ round/worker/warm-start lineage
+ provider determinism-relevant environment
+ optional hybrid pool/projection/backend/work identity
```

정상 `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT`,
`MAX_ROUNDS_REACHED` 실행은 canonical trace, verified solution, outcome, metric/objective와
payload fingerprint가 같아야 한다. Timeboxed or multi-thread hybrid는 source가 허용한
`TIMEBOXED_HYBRID` class로 별도 distribution evidence를 만들며 strong equality를
거짓 주장하지 않는다. ALNS-only와 hybrid envelope는 같은 comparison card에서
quality regression으로 직접 비교하지 않는다.

### 6.6 Security, cost and observability evidence

| 영역 | 최소 evidence | 실패 |
|---|---|---|
| Security | Tenant isolation, least privilege, IAM/KMS/network, secret/PII redaction, signed evidence access, audit integrity | `SECURITY_GATE_FAILED` |
| Cost | Declared resource usage, per-stage/provider metering, retry/amplification, storage/retention estimate, approved bound | `COST_GATE_BLOCKED_OR_FAILED` |
| Observability | Required correlation, requested/completed work, verifier outcome, stop/rollback signals, alarm delivery, missing-telemetry test | `OBSERVABILITY_GATE_FAILED` |
| Operations | Cancel/retry/recovery, duplicate/out-of-order, quota/deadline, reconciliation, on-call/runbook | `OPERATIONS_GATE_FAILED` |

Cost/security/observability evidence가 성능·quality result와 분리되어야 한다. Raw address,
full input, credential, secret, license value, private signing material은 evidence/log에
넣지 않는다. 승인된 numeric bound가 없으면 통과로 간주하지 않고 blocker로 남긴다.

## 7. Official manifest, run and benchmark contract

### 7.1 Official manifest factory

`OfficialExecutionManifest`는 모든 gate reference를 검증한 factory만 만든다.

```text
OfficialExecutionManifest
  schemaVersion / manifestId / manifestFingerprint
  sourceCommit / build / runtime / distribution fingerprints
  integerTravelFixtureAuthorityRef
  greatCirclePolicyAuthorityRef
  problem / preparedTravel / profile / preset fingerprints
  officialAlnsParameterAuthorityRef
  officialExecutionValueAuthorityRef
  calibrationPlan / result / acceptance refs
  phase13ApplicabilityRef
  optionalHybridAuthorityRefs
  exact phase-one candidate set
  exact round/worker/run identities
  base/derived seeds and derivation version
  retry/watchdog/resource policy
  comparator/version
  candidate/result verifier versions
  selected provider/deployment evidence ref
  artifact/state/publication schema versions
  provenance and telemetry schema versions
  signed seal
```

Field 누락, unknown enum, defaulted number, untrusted/expired signature, different subject
digest와 provider deployment mismatch는 manifest creation을 거부한다. Official factory는
experiment/test manifest를 relabel하지 않고 새 immutable manifest를 만든다.

Sealed manifest는 소비 시 mutation하거나 `used=true`로 덮어쓰지 않는다. Manifest는 exact
logical execution template과 허용된 official-run/replay 역할을 선언하고, 각 invocation은
별도 immutable `OfficialExecutionReceipt`에 role, logical run identity, `AttemptId`,
시작/종료와 outcome을 기록한다. Official result lineage와 baseline/challenger promotion은
승인된 한 lineage에만 귀속하며, 임의 추가 실행이나 좋은 run 선택은 금지한다. Replay는
같은 manifest bytes와 semantic execution envelope를 사용하되 별도 receipt/attempt lineage를
가지며 그 operational identity를 quality input으로 사용하지 않는다.

### 7.2 Official run

```text
sealed manifest
→ verify all gate signatures and subject digests
→ account for the complete 4×2 construction portfolio
→ execute every manifest-declared available phase-one candidate
→ stable phase-one champion
→ execute every declared phase-two worker
→ verify every worker candidate
→ stable all-worker fan-in
→ strict-improvement next-round or normal stop
→ candidate verifier PASS
→ finalization/audit
→ result verifier PASS
→ publishable official result
→ identical-manifest replay
→ official benchmark card
```

- 모든 worker는 exact requested completed-step을 수행하고 정상 종료해야 한다.
- 4개 construction policy × 2개 vehicle-order policy는 최대 후보 공간이다. Manifest는
  각 조합을 `AVAILABLE` 또는 authority-backed `UNAVAILABLE`로 exact하게 선언한다.
  `AVAILABLE` 후보는 모두 screen하며, 좌표/정책 부재 등을 hidden skip이나 fallback으로
  바꾸지 않는다. 실행 가능한 후보가 0이면 official run은 `INCOMPLETE`다.
- Worker failure는 same logical identity/seed/warm start/config로 retry할 수 있다.
- 끝내 완료되지 않은 worker가 하나라도 있으면 run 전체가 `INCOMPLETE`다.
- Prefix listing, completion order, fastest worker, partial success로 champion을 만들지 않는다.
- Recovery candidate, watchdog/resource/platform termination은 official completion이 아니다.
- Hybrid optional fallback은 manifest가 explicit하게 허용한 경우만 typed
  `DEGRADED_ALNS_ONLY`가 될 수 있고, required hybrid fallback은 `INCOMPLETE`다.

### 7.3 Exact comparator and card

```text
(
  verified unassigned request count,
  verified dispatched vehicle count,
  verified total directed distance meters,
  verified total route operational time seconds
)
```

모든 성분은 작을수록 좋고 첫 번째 다른 성분만 승패를 결정한다. 네 성분이 같으면
quality tie이며 structural tie-break는 품질의 다섯 번째 성분이 아니다. Operational
time은 used route의 drive + customer wait + depot wait + service +
inter-work-window rest 합이다.

`OfficialBenchmarkCard`는 fixture/manifest/result/replay/evidence digests, comparator
vector, champion lineage, completeness, both-verifier reports와 limitation을 가진다.
Baseline 승격은 Benchmark/Quality와 independent reviewer의 별도 signed approval이다.
Mutable `latest`, console summary와 최솟값만 남긴 table은 baseline이 아니다.

## 8. Cutover state machine, authority and rollback

Source는 shadow, versioned cutover와 rollback 의미를 요구하지만 canary/hold/approve의
exact state 이름은 확정하지 않았다. 아래 enum 이름은 모두
**`PROPOSED_INTERNAL`**이며 public API/wire schema가 아니다. 의미, authority와
fail-closed 규칙이 우선한다.

### 8.1 Proposed states and commands

```text
states:
  PREPARED
  SHADOW_RUNNING
  SHADOW_HOLD
  CANARY_APPROVED
  CANARY_RUNNING
  CANARY_HOLD
  ACTIVATION_APPROVED
  ACTIVE
  ROLLBACK_REQUESTED
  ROLLED_BACK
  ABORTED

commands:
  START_SHADOW
  HOLD_SHADOW
  APPROVE_CANARY
  START_CANARY
  HOLD_CANARY
  APPROVE_ACTIVATION
  ACTIVATE
  REQUEST_ROLLBACK
  EXECUTE_ROLLBACK
  ABORT
```

### 8.2 Legal transition and authority

| From | Command | Required authority/evidence | To |
|---|---|---|---|
| `PREPARED` | `START_SHADOW` | Official card, provider deployment, compatibility/migration/rollback ready | `SHADOW_RUNNING` |
| `SHADOW_RUNNING` | `HOLD_SHADOW` | Complete shadow observations or stop signal | `SHADOW_HOLD` |
| `SHADOW_HOLD` | `APPROVE_CANARY` | Scoped canary production authority, passed shadow/security/ops/cost evidence | `CANARY_APPROVED` |
| `CANARY_APPROVED` | `START_CANARY` | Exact previous/desired pointer versions, rollback target/role healthy | `CANARY_RUNNING` |
| `CANARY_RUNNING` | `HOLD_CANARY` | Approved observation window complete or manual hold | `CANARY_HOLD` |
| `CANARY_HOLD` | `APPROVE_ACTIVATION` | Separate activation authority and canary acceptance | `ACTIVATION_APPROVED` |
| `ACTIVATION_APPROVED` | `ACTIVATE` | Authority still valid, exact deployment/manifest/pointer scope match | `ACTIVE` |
| Any running/hold/active | `REQUEST_ROLLBACK` | Structural stop condition or authorized operator | `ROLLBACK_REQUESTED` |
| `ROLLBACK_REQUESTED` | `EXECUTE_ROLLBACK` | Rollback role, last-safe pointer, reconciliation plan | `ROLLED_BACK` |
| Before `ACTIVE` | `ABORT` | Phase owner; no production forward transition | `ABORTED` |

`APPROVE_CANARY`와 `APPROVE_ACTIVATION`은 동일 approval을 재사용하지 않는 것을
기본 proposed rule로 한다. 실제 authority composition은 Product/Release/Security/Ops가
승인한다. Approval scope에는 environment, manifest/deployment digest, action, maximum
traffic scope, validity window, expected/desired pointer version과 rollback role이 들어간다.

Traffic/pointer를 바꾸는 transition에는 control-state CAS와 provider pointer CAS를 서로
다른 typed precondition으로 둔다. `CANARY_RUNNING`, `ACTIVE`, `ROLLED_BACK`은 provider
action receipt와 exact pointer/traffic read-back이 desired state와 일치하고 in-flight
reconciliation이 끝난 뒤에만 final control-state CAS로 기록한다. Provider action 전에는
현재 semantic state를 유지한 채 immutable transition intent와 pending transition identity만
권위화한다. Provider action 성공 뒤 process가 죽으면 같은 transition identity로 exact
read-back/reconciliation 후 final state를 수렴시키거나 hold/rollback/incident로 남긴다.
Control state를 먼저 terminal state로 바꾸거나 두 CAS를 하나의 opaque version으로
대체하지 않는다.

Observation/hold는 signed `CutoverObservationPolicy`가 window identity, start authority,
absolute deadline, required telemetry/alarm set, acceptance/stop predicate와 missing-data
처리를 explicit하게 가진 경우에만 시작한다. 한 process 안에서는 monotonic elapsed를
사용한다. Crash/restart에서는 persisted absolute deadline과 승인된 clock authority로
remaining window를 재계산하며, clock provenance/jump/expiry가 불명확하면 window를
완료로 추정하지 않고 `HOLD_CLOCK_INDETERMINATE`로 fail-closed한다. Provider remaining
time, 새 JVM의 process-local monotonic origin 또는 wall-clock library default는 관찰 완료나
authority 유효성의 hidden fallback이 아니다.

### 8.3 Shadow and parity

Shadow는 same logical input/profile/manifest를 local accepted oracle과 selected deployment에
side-effect 없이 실행한다. Target result를 production response로 내보내거나 publication
pointer를 바꾸지 않는다.

검사:

- Canonical input/problem/travel/profile/manifest fingerprint equality
- Declared worker set, seed/warm-start/retry/termination equality
- Candidate/result verifier outcome와 payload digest equality
- Exact comparator/result lineage equality
- Provider metadata가 semantic identity에 영향을 주지 않음
- Cancellation, duplicate, timeout, CAS conflict와 recovery parity
- Security/tenant/audit/telemetry/cost evidence complete

Fingerprint가 다르면 quality를 비교하지 않고 `NOT_COMPARABLE`로 hold한다.

### 8.4 Stop conditions

구조적 stop condition은 숫자 threshold 없이도 즉시 적용한다.

- Gate signature/digest/scope/expiry/revocation mismatch
- Different manifest/build/deployment/pointer version
- Missing/incomplete declared worker 또는 abnormal termination
- Candidate/result verifier fail/incomplete
- Artifact digest, canonical encoding, state version 또는 publication CAS conflict
- Tenant crossing, unauthorized access, secret/PII exposure 또는 audit integrity failure
- Required telemetry/alarm/control-plane visibility 누락
- Rollback target/role/runbook가 unavailable
- Unknown state/event, duplicate event의 divergent digest, retry identity drift
- Phase 13 branch/solver/license/fallback scope mismatch

Performance, reliability, SLO, error, latency, cost와 canary traffic stop threshold는
현재 `OPEN`; approved `CutoverAcceptancePolicy`에 explicit해야 한다. 누락되면 canary를
시작하지 않는다. Shadow에서는 hold/abort하고, canary/active에서는 approved automatic
또는 operator rollback policy를 실행한다. Rollback 자체 실패는 정상 active로 유지하는
것이 아니라 production incident다.

### 8.5 Last safe point and rollback

Last safe point:

```text
previous accepted endpoint/adapter/distribution revision
+ previous authoritative traffic/publication pointer version
+ all immutable input/result/state/audit artifacts
+ exact in-flight solve reconciliation snapshot
+ verified rollback role and runbook
```

Rollback 순서:

1. 새 candidate start를 중단한다.
2. Expected pointer/version을 다시 읽고 scope mismatch면 incident/hold한다.
3. 새 traffic을 previous accepted revision으로 CAS 전환한다.
4. In-flight solve를 approved drain 또는 typed cancel policy로 처리한다.
5. Exact worker/state/result refs를 reconcile한다.
6. Immutable artifact와 audit/evidence를 보존한다.
7. Publication pointer, tenant isolation, cancel/retry와 alarm을 검증한다.
8. Independent reviewer가 restore evidence를 확인한 뒤 `ROLLED_BACK`을 seal한다.

S3 artifact, state history, legacy version, KMS key를 삭제하거나 overwrite하는 것은
rollback이 아니다. 첫 AWS production revision에 previous AWS revision이 없으면 last
safe point는 new AWS start/traffic 차단과 기존 accepted production path 유지다. Legacy
GCP가 accepted target semantic contract를 만족하지 않으면 semantic fallback이라고
부르지 않고 별도 compatibility risk로 기록한다.

### 8.6 Irreversible authority

Phase 14의 정상 cutover는 reversible pointer/alias/traffic transition만 허용한다.
Legacy delete, KMS/key destruction, irreversible schema rewrite, artifact purge, rollback
role revoke와 versioned endpoint 제거는 비범위다.

예외적으로 irreversible action이 요구되면:

1. Phase 14 transition을 hold한다.
2. Exact target/resource/data class와 복구 불가능성을 명시한 별도
   `IrreversibleChangeApproval`을 받는다.
3. Retention/legal/security/product/operations authority와 independent review를 받는다.
4. 이 Phase와 분리된 decommission plan/evidence로 실행한다.

Production activation approval만으로 irreversible operation을 수행할 수 없다.

## 9. Proposed Java/config/IaC contracts

아래 이름과 signature는 **proposed internal candidates**다. Phase 00~13 accepted
contract와 public API/schema review가 우선한다.

### 9.1 Records and enums

```java
public record ApprovedEvidenceRef(
    String evidenceKind,
    String subjectDigest,
    String envelopeDigest,
    String immutableLocator
) {}

public record IntegerTravelFixtureAuthority(
    String fixtureId,
    String schemaVersion,
    String contentDigest,
    long directedCellCount,
    String sourceAuthorityId,
    String validationReportDigest,
    ApprovedEvidenceRef approval
) {}

public record GreatCirclePolicyAuthority(
    String policyId,
    String functionVersion,
    String earthModelId,
    String constantsDigest,
    String coordinateContractVersion,
    String precisionPolicyVersion,
    String referenceVectorDigest,
    ApprovedEvidenceRef approval
) {}

public record OfficialExecutionValues(
    long screenMaxSteps,
    int phase2WorkerCount,
    long phase2MaxSteps,
    int maxRounds,
    String watchdogPolicyId,
    String resourcePolicyId,
    String seedDerivationPolicyId
) {}

public record OfficialAlnsParameters(
    String parameterSchemaVersion,
    String portfolioVersion,
    String operatorSetVersion,
    String selectionPolicyVersion,
    Map<String, String> explicitTypedParameters,
    String canonicalDigest
) {}

public sealed interface Phase13ApplicabilityReceipt
    permits Phase13ApplicabilityReceipt.Skip,
            Phase13ApplicabilityReceipt.Activated {
    record Skip(
        SkipReason reason,
        ApplicabilityReceiptVersion receiptVersion,
        Fingerprint schedulerDecision,
        Fingerprint alnsOnlyPlanIdentity,
        SignedApplicabilityEnvelopeRef signedEnvelope,
        ApplicabilityActionTimeVerificationRef actionTimeVerification,
        Instant decisionTime,
        String authorityVersion
    ) implements Phase13ApplicabilityReceipt {}

    record Activated(
        Phase13ActivatedHandoff handoff,
        SignedApplicabilityEnvelopeRef signedEnvelope,
        ApplicabilityActionTimeVerificationRef actionTimeVerification
    ) implements Phase13ApplicabilityReceipt {}
}
```

위 `SignedApplicabilityEnvelopeRef`와 `ApplicabilityActionTimeVerificationRef`는
Phase 13 v1.2 scheduler/control-plane contract를 Phase 14가 소비하는 reference다.
Phase 14가 이 type을 Phase 13 implementation module에 제공하거나 Phase 13 entry가
`G14-SIGNING-TRUST`에 의존하게 만들지 않는다.

`Map<String,String>`은 설명을 줄이기 위한 candidate이며 arbitrary untyped production
bag 승인이 아니다. 실제 구현은 Phase 06 contract의 typed value/units/ranges를
사용해야 한다. Unknown/missing key와 unsupported version을 거부한다.

```java
public record CalibrationPlan(
    String planId,
    String corpusManifestDigest,
    List<String> candidateParameterDigests,
    List<String> candidateExecutionValueDigests,
    List<String> declaredRunIds,
    String frozenEnvironmentDigest,
    String analysisMethodDigest,
    String acceptancePolicyDigest,
    ApprovedEvidenceRef preregistrationApproval
) {}

public sealed interface CalibrationOutcome
    permits CalibrationOutcome.Complete,
            CalibrationOutcome.Incomplete,
            CalibrationOutcome.InvalidEvidence {
    record Complete(String resultDigest, String independentAnalysisDigest)
        implements CalibrationOutcome {}
    record Incomplete(Set<String> missingRunIds, String reportDigest)
        implements CalibrationOutcome {}
    record InvalidEvidence(List<String> failures, String reportDigest)
        implements CalibrationOutcome {}
}

public record OfficialExecutionManifest(
    String manifestId,
    String manifestFingerprint,
    String authorityGraphRootDigest,
    String sourceBuildRuntimeDigest,
    String problemTravelProfileDigest,
    String alnsParameterAuthorityDigest,
    String executionValueAuthorityDigest,
    String phase13ApplicabilityReceiptDigest,
    String providerDeploymentEvidenceDigest,
    String signedSealDigest
) {}
```

### 9.2 Interfaces and signatures

```java
public interface SignedEvidenceVerifier {
    EvidenceVerificationReport verify(
        ApprovedEvidenceRef evidence,
        EvidenceVerificationContext context
    );
}

public interface CalibrationRunner {
    CalibrationOutcome execute(CalibrationPlan frozenPlan);
}

public interface CalibrationAcceptanceEvaluator {
    CalibrationDecision evaluate(
        CalibrationPlan plan,
        CalibrationOutcome.Complete result,
        AcceptancePolicy policy
    );
}

public interface OfficialManifestFactory {
    OfficialManifestResult create(
        OfficialManifestDraft draft,
        VerifiedGateBundle gates
    );
}

public interface OfficialBenchmarkRunner {
    OfficialRunOutcome execute(OfficialExecutionManifest manifest);
}

public interface ProductionAuthorityVerifier {
    AuthorityVerificationReport verify(
        ProductionAuthorityEnvelope authority,
        CutoverCommand command,
        CutoverSnapshot current
    );
}

public interface CutoverControlPort {
    CutoverTransitionResult apply(
        CutoverCommand command,
        CutoverSnapshot expected
    );
}

public interface RollbackControlPort {
    RollbackOutcome restore(
        LastSafePoint lastSafe,
        RollbackAuthority authority
    );
}
```

Application이 정의한 port를 AWS adapter가 구현한다. `OfficialManifestFactory`는
signature 검증 실패, absent official number와 Phase 13 scope mismatch를 typed failure로
반환하며 partial manifest를 외부에 노출하지 않는다.

### 9.3 Proposed cutover types

```java
public enum ProposedCutoverState {
    PREPARED,
    SHADOW_RUNNING,
    SHADOW_HOLD,
    CANARY_APPROVED,
    CANARY_RUNNING,
    CANARY_HOLD,
    ACTIVATION_APPROVED,
    ACTIVE,
    ROLLBACK_REQUESTED,
    ROLLED_BACK,
    ABORTED
}

public sealed interface CutoverCommand
    permits StartShadow, HoldShadow, ApproveCanary, StartCanary,
            HoldCanary, ApproveActivation, Activate,
            RequestRollback, ExecuteRollback, AbortCutover {}

public record CutoverSnapshot(
    String cutoverId,
    ProposedCutoverState state,
    String controlStateVersion,
    String providerPointerVersion,
    String activePointerDigest,
    String desiredPointerDigest,
    String manifestDigest,
    String deploymentDigest,
    String lastSafePointDigest,
    String pendingTransitionId,
    String providerActionReceiptDigest,
    String observationWindowRef
) {}
```

State 이름은 proposed다. Serialization될 가능성이 생기면 versioned schema와 compatibility
review를 별도로 받는다.

### 9.4 Pseudocode

```text
createOfficialManifest(draft):
  gateReports = verifyEachGateIndependently(draft.gateRefs)
  require every report == PASS
  require no defaulted or absent numeric field
  require fixture.isInteger && fixture.approval.scope == OFFICIAL
  require GreatCircle policy/reference vectors approved
  require ALNS parameters and Q-BENCH-02 values have separate approvals
  require phase13 branch is exact and authorized
  require provider deployment subject digest matches selected deployment
  return immutable, canonically encoded, signed manifest
```

```text
applyCutoverCommand(command, expected):
  current = stateRepository.readExact(expected.cutoverId)
  require current.controlStateVersion == expected.controlStateVersion
  require legalTransition(current.state, command)
  verify command evidence digests

  if command changes production traffic or pointer:
    verify scoped production authority
    require authority not expired/revoked/used outside scope
    require rollback target/role/telemetry healthy
    observedPointer = providerControl.readExact()
    require observedPointer.version == expected.providerPointerVersion
    require observedPointer.digest == current.activePointerDigest

  if structuralStopCondition():
    return requestRollbackOrHold(current)

  transitionId = deterministicIdentity(command, current, authority)
  append immutable transition-intent event by transitionId
  pending = cas control state from current.controlStateVersion
            to same semantic state + pending transitionId

  if command changes production traffic or pointer:
    providerReceipt = providerControl.compareAndSet(
      expected.providerPointerVersion,
      current.activePointerDigest,
      desiredPointerDigest(command),
      idempotencyKey = transitionId
    )
    readBack = providerControl.readExact()
    require receipt/readBack converge to exact desired pointer and traffic scope
  else:
    perform non-production action with transitionId and exact read-back

  reconcile in-flight work and required observation-window state
  finalState = pureFinalTransition(pending, command, providerReceipt, readBack)
  require traffic-semantic final state is impossible without verified receipt/readBack
  converged = cas control state from pending.controlStateVersion to finalState
  append immutable transition-result event by transitionId
  return converged
```

```text
officialRun(manifest):
  reverify manifest seal and complete authority graph
  declared = exactDeclaredWorkers(manifest)
  execute all declared workers with stable identities
  completed = read exact refs for declared
  require completed.keySet == declared
  require each normalTermination && candidateVerifierPASS
  champion = stableExactComparator(completed)
  final = finalizeAndResultVerify(champion)
  require final.resultVerifierPASS
  replay = executeIdenticalManifest(manifest)
  require replay fingerprint contract
  return official card or typed INCOMPLETE/FAILED
```

### 9.5 Config and IaC boundary

Candidate config documents:

```text
CalibrationPlanDocument
OfficialAlnsParameterDocument
OfficialExecutionValueDocument
AcceptancePolicyDocument
OfficialExecutionManifestDocument
CutoverPlanDocument
ProductionAuthorityEnvelope
ImplementationEvidenceIndex
```

모두 schema/version/canonical encoding/digest/signature를 가진다. Environment variable은
opaque locator와 credential handle만 제공할 수 있고 core/application이 algorithm
숫자나 semantic policy를 환경에서 직접 읽지 않는다.

Phase 11의 proposed AWS SAM/CloudFormation baseline을 유지한다.

- Candidate Lambda version/alias, Step Functions revision, S3/KMS/IAM/alarm 변경은 source
  template과 reviewed change set에서 재현돼야 한다.
- Production parameter file은 source control의 example을 복사해 hidden default로 채우지
  않는다.
- Change set digest와 deployed resource revision을 `ProviderDeploymentEvidence`에 묶는다.
- Traffic alias/pointer update는 expected previous version과 desired immutable revision을
  명시한다.
- IaC apply 성공만으로 canary/activation authority가 생기지 않는다.
- Generated `.serverless/cloudformation-template-*`는 source/approval evidence가 아니다.

## 10. Ordered gated work packages

모든 work package는 prerequisite/authority → target → task → verification →
expected → failure/rollback → handoff 순서다. Future command 이름은 Phase 00 target
reactor/CLI가 확정된 뒤 exact path로 review한다.

### WP14-0 — Entry receipt and source/config semantic-impact freeze

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | 문서 review 가능; target 구현은 `G14-PREDECESSOR`와 scheduler task 필요 |
| Targets | Phase 00~12 receipt, cited source/contract impact, owner matrix, last safe baseline |
| Tasks | Evidence graph 수집, signature/digest/review 검증, Phase 12 applicability, legacy evidence exclusion, semantic-impact report |
| Verification | §12 fail-closed protocol의 `Phase14PredecessorReceiptTest`; stable cited section semantic review; accepted evidence graph closure checker. 인접 digest는 acceptance가 아님 |
| Expected | Missing/stale/mixed evidence 0, legacy 11-phase reuse 0, gate별 상태/owner/restart 고정 |
| Failure/rollback | 구현 시작 금지; 현재 accepted predecessor와 문서/fixture 설계가 last safe |
| Handoff | `Phase14EntryReceipt`와 blocker register |

### WP14-1 — Signed evidence and authority contract

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-0; Security/Release signing policy review |
| Targets | `SignedEvidenceEnvelope`, canonical encoding, trust/revocation verifier, test-only negative signer |
| Tasks | Signature profile/trust-root proposal, role/scope/expiry/revocation, digest-before-deserialize, replay/forgery tests |
| Verification | §12 fail-closed protocol의 `SignedEvidenceVerifierContractTest`; untrusted/expired/revoked/test signer/corrupt payload cases |
| Expected | `G14-SIGNING-TRUST` approved; official path accepts only trusted scoped immutable envelope |
| Failure/rollback | Signature를 bypass하지 않고 unsigned schema/test 설계까지만 유지 |
| Handoff | Approved evidence trust policy와 verifier build digest |

### WP14-2 — Travel and calibration preregistration

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-1; Input/Matrix, Great Circle, Benchmark/Quality owners |
| Targets | Integer fixture authority, Great Circle authority, corpus, candidate configs, seed/run matrix, acceptance policy |
| Tasks | Decimal rejection, complete cell audit, function/reference-vector verification, corpus/config/environment freeze, plan preregistration |
| Verification | §12 fail-closed protocol의 `IntegerTravelFixtureAuthorityTest`, `GreatCirclePolicyAuthorityTest`, `CalibrationPlanFreezeTest` |
| Expected | `G14-INTEGER-FIXTURE`와 `G14-GREAT-CIRCLE` 각각의 `SATISFIED/PASS` evidence; plan has no absent/defaulted field |
| Failure/rollback | Test-only hand fixture와 explicit exploratory plan만 허용; official/calibration approval label 금지 |
| Handoff | Signed authorities와 frozen `CalibrationPlan` |

### WP14-3 — Calibration execution and independent analysis

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-2 frozen plan, approved isolated experiment environment와 cost guard |
| Targets | Exact declared run set, raw trace/result/telemetry/cost, independent result table |
| Tasks | Candidate×case×seed×repeat 실행, retry identity 보존, both-verifier, complete-run reconciliation, preregistered statistics |
| Verification | §12 fail-closed protocol의 local experiment `CalibrationDeclaredRunCompletenessIT`, `CalibrationIndependentAnalysisIT`; replay/corruption/security/cost/telemetry suites |
| Expected | 모든 declared run accounted, producer/independent analysis digest equality, no post-hoc exclusion |
| Failure/rollback | Candidate set을 `INCOMPLETE`/`INVALID_EVIDENCE`로 seal; 일부 성공값 승인 금지 |
| Handoff | `CalibrationResult`, raw evidence index, limitation와 approval packet |

### WP14-4 — Separate parameter/value approvals and official manifest seal

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-3 complete result; Algorithm/Quality/Benchmark independent approvals |
| Targets | `OfficialAlnsParameterAuthority`, `OfficialExecutionValueAuthority`, actual Phase 13 v1.2-compatible `Phase13ApplicabilityReceipt`, official manifest |
| Tasks | Acceptance predicate 적용, coherent candidate 선택, separate signatures, Phase 13 branch/envelope/action-time verification, manifest canonical seal |
| Verification | §12 fail-closed protocol의 `OfficialManifestFactoryTest`, `OfficialParameterSeparationTest`, `Phase13ApplicabilityTest`, `Phase13ApplicabilityDependencyTest` |
| Expected | `G14-ALNS-PARAMETERS`, `G14-QBENCH-02`, `G14-CALIBRATION-ACCEPTANCE` 각각 approved; one immutable manifest |
| Failure/rollback | `EXPERIMENT_ONLY` result 유지; 값을 복사하거나 official default 생성 금지 |
| Handoff | Sealed `OfficialExecutionManifest`와 authority graph |

### WP14-5 — Official run, exact benchmark and replay

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-4, accepted local/provider execution path, official-run environment authority |
| Targets | All-worker official execution, both-gate result, exact card, identical-manifest replay |
| Tasks | 4×2 조합 availability 전수 accounting, 모든 manifest-declared available candidate screen, complete phase-two batches, stable champion, finalization/verifiers, replay, baseline/challenger review |
| Verification | §12 fail-closed protocol의 local `OfficialRunCompletenessIT`, `OfficialReplayIT`; `OfficialBenchmarkComparatorTest`, unavailable/silent-skip와 payload corruption suite |
| Expected | Incomplete worker 0, abnormal official termination 0, both verifier PASS, replay contract PASS |
| Failure/rollback | No publication/baseline; immutable failed/incomplete record와 last verified predecessor 유지 |
| Handoff | `E-P14-OFFICIAL-RUN` candidate, `OfficialBenchmarkCard` |

### WP14-6 — Provider deployment, migration and shadow

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-5; Phase 11 accepted; Phase 12 if applicable; approved non-production deployment scope |
| Targets | Exact provider deployment, compatibility matrix, artifact copy mapping, shadow, rollback target |
| Tasks | IaC/change-set verification, deploy candidate immutable revision, source→destination digest copy, versioned adapter, local↔provider shadow, security/ops/cost |
| Verification | Selected provider의 approved distribution/profile에서 §12 fail-closed protocol을 적용한다. `AWS_REFERENCE`일 때만 `ProviderDeploymentEvidenceIT`, `ArtifactMigrationDigestIT`, `AwsShadowParityIT`; substituted provider면 accepted Phase 12가 정한 동등 suite; stop-condition fault suite |
| Expected | `G14-PROVIDER-DEPLOYMENT` satisfied, parity mismatch 0, rollback rehearsal ready |
| Failure/rollback | Candidate start 중단, previous accepted path 유지, copied immutable orphan은 quarantine/retention policy로 보존 |
| Handoff | Shadow report, compatibility/migration record, verified last safe point |

### WP14-7 — Proposed canary and hold

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-6, scoped canary `G14-PRODUCTION-AUTHORITY`, approved cutover thresholds/runbook |
| Targets | `CANARY_APPROVED/RUNNING/HOLD`, bounded traffic/pointer, audit/telemetry |
| Tasks | Authority scope reverify, rollback health check, pending transition intent, provider pointer CAS/read-back, final control-state CAS, persisted observation window, hold |
| Verification | §12의 non-mutating preflight/post-readback suite와 exact report protocol. Traffic 변경은 Maven test가 아니라 approved release pipeline action으로만 수행하고 `ProductionAuthorityScopeIT`, selected-provider canary contract, stop/clock/restart/reconciliation과 audit completeness를 검증 |
| Expected | Traffic never exceeds approved scope; acceptance or rollback decision fully evidenced |
| Failure/rollback | Immediate approved rollback; rollback failure는 incident, authority expansion 금지 |
| Handoff | Canary record와 separate activation approval packet 또는 rollback record |

### WP14-8 — Activation or rollback

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-7 hold accepted, separate exact activation authority |
| Targets | `ACTIVATION_APPROVED→ACTIVE` 또는 `ROLLBACK_REQUESTED→ROLLED_BACK` |
| Tasks | Reverify all gate/authority/deployment/pointer digests, pending transition intent, provider traffic/pointer CAS/read-back, reconcile in-flight, final control-state CAS, verify publication/access/alarms |
| Verification | Approved release pipeline의 controlled action 뒤 §12 non-mutating post-transition `ProductionActivationIT` 또는 rollback reconciliation suite; pointer/control-state response-loss, smoke/security/cancel/retry. Test exit가 authority나 mutation receipt를 대체하지 않음 |
| Expected | One authoritative active pointer/version, no unresolved state, or independently verified rollback |
| Failure/rollback | Forward transition 중단하고 last safe point로 restore; artifact deletion 금지 |
| Handoff | `E-P14-CUTOVER`, `E-P14-ROLLBACK`, transition record |

### WP14-9 — Evidence closure and operations handoff

| 항목 | 계약 |
|---|---|
| Prerequisite/authority | WP14-8 terminal result, independent Phase 14 review |
| Targets | `ImplementationEvidenceIndex`, `ProductionActivationRecord` 또는 terminal rollback record, runbook/owner handoff |
| Tasks | Evidence graph closure, exact active identities, known limitations/open gates, expiry/rollback/on-call ownership, scheduler submission |
| Verification | §12 fail-closed protocol의 `ImplementationEvidenceIndexTest`, `ProductionActivationRecordTest`; link/digest/signature/closure audit |
| Expected | All required keys immutable, review accepted, operations can resolve exact artifact/state without listing/latest |
| Failure/rollback | Phase 14 `REVIEW_PENDING`/`GATED` 유지; activation record를 fabricate하지 않음 |
| Handoff | Production Operations와 scheduler가 소비할 final index/record |

## 11. Exact test specification

### 11.1 Fixtures, builders and independent oracles

Test-only candidates:

```text
Phase14FixtureBuilder
  integerTravelFixture()
  decimalTravelFixture()
  completeProvidedTravelFixture()
  missingDistanceFixture()
  officialManifestDraftWithNoApprovals()
  declaredWorkerSet()
  localAndAwsEquivalentResults()

TestSignedEvidenceBuilder
  TEST_ONLY signer only
  trusted/untrusted/expired/revoked/corrupt variants
  must be rejected by official production trust policy

CutoverScenarioBuilder
  distinct control-state and provider-pointer versions
  shadow/canary/activation/rollback snapshots
  action-success-before-final-state and response-loss snapshots
  persisted observation-window and clock-anomaly snapshots
  structural and threshold stop signals
```

Independent oracle:

- Integer matrix scanner: JSON number/text를 exact decimal로 읽고 fractional `D/U`를 거부한다.
- Great Circle vector oracle: owner-approved fixed reference vectors만 사용하며 production
  implementation을 호출하지 않는다.
- Generated-duration oracle: approved integer `D`, exact vehicle speed/source에서
  `CEILING(D_meter × 3.6 ÷ speed_km_h)`를 독립 계산하고 missing speed만 `45 km/h`,
  present-invalid speed rejection과 provided/generated provenance를 대조한다.
- Comparator hand oracle: 작은 route/outcome에서 네 component를 직접 계산한다.
- Declared completeness oracle: manifest의 exact run/worker ID set과 result set equality를
  비교한다.
- Statistical oracle: frozen raw table에서 preregistered paired method를 독립 재실행한다.
- Signature oracle: approved crypto library/trust policy로 canonical bytes를 독립 검증한다.
- Cutover transition oracle: legal `(state, command, authority) → result` truth table이다.
- Migration oracle: source/destination bytes digest와 pointer version을 독립 read-back한다.
- Pointer/control-state oracle: provider CAS receipt/read-back 전 traffic-semantic final state를
  허용하지 않고, action-success 뒤 crash/retry가 같은 transition identity로 수렴하는지 본다.
- Observation oracle: persisted absolute deadline, approved clock authority와 attempt-local
  monotonic elapsed를 독립 계산하고 clock anomaly/missing telemetry를 completion으로 바꾸지 않는다.

Fixture/builder/oracle가 official authority artifact나 production signature를 생성할 수
없어야 한다.

### 11.2 Exact class/method matrix

| Layer/class | Exact future test method | Fixture/oracle | Pass criterion |
|---|---|---|---|
| Unit `IntegerTravelFixtureAuthorityTest` | `rejectsAnyFractionalDistanceOrDuration()` | Decimal fixture/scanner | Fractional cell 1개도 typed rejection |
| Unit `IntegerTravelFixtureAuthorityTest` | `doesNotRelabelRoundedLegacyFixtureAsApproved()` | Legacy digest + fake conversion | Approval 없는 conversion 거부 |
| Unit `IntegerTravelFixtureAuthorityTest` | `requiresCanonicalGeneratedDurationAndSpeedSourceProvenance()` | Missing/invalid speed + duration oracle | Formula/CEILING/default/provenance drift 모두 fail |
| Unit `GreatCirclePolicyAuthorityTest` | `requiresFunctionEarthModelPrecisionAndReferenceVectors()` | Missing fields/vector oracle | Missing field마다 fail |
| Unit `GreatCirclePolicyAuthorityTest` | `matchesEveryApprovedReferenceVectorWithMeterHalfUp()` | Approved vectors | Expected integer meter exact match |
| Unit `OfficialParameterSeparationTest` | `keepsExecutionValuesSeparateFromAlnsParameters()` | Two authority drafts | Cross-owned/hidden field 0 |
| Unit `CalibrationPlanFreezeTest` | `fingerprintsCorpusBuildConfigSeedsAndEnvironment()` | Plan builder | 모든 freeze field가 digest에 영향 |
| Unit `CalibrationPlanFreezeTest` | `rejectsAbsentThresholdInsteadOfDefaulting()` | Open policy | Typed blocked result |
| Unit `CalibrationAcceptanceEvaluatorTest` | `usesPreregisteredPairedMethodOnly()` | Frozen raw table/stat oracle | Producer/independent digest equality |
| Unit `OfficialManifestFactoryTest` | `rejectsUnapprovedQBenchValues()` | Unsigned value ref | Manifest 미생성 |
| Unit `OfficialManifestFactoryTest` | `rejectsTestSignerExpiredOrRevokedApproval()` | Evidence variants | 모두 typed rejection |
| Unit `Phase13ApplicabilityTest` | `doesNotRequireOrExecutePhase13WhenGateIsClosed()` | ALNS-only record | Hybrid ref/call 0 |
| Unit `Phase13ApplicabilityTest` | `requiresAcceptedPhase13EvidenceForHybridManifest()` | Hybrid draft | Missing evidence fail |
| Unit `Phase13ApplicabilityTest` | `requiresSignedEnvelopeAndFreshActionVerificationForSkipAndActivated()` | Unsigned/expired/revoked/stale/wrong-action variants | Actual current `PASS` receipt 외 모두 fail |
| Architecture `Phase13ApplicabilityDependencyTest` | `doesNotCreatePhase14ToPhase13EntryDependency()` | Module/receipt dependency graph | Phase 14 consumption edge만 존재; reverse entry edge 0 |
| Unit `OfficialManifestFactoryTest` | `accountsForEveryConstructionCombinationAndRejectsSilentUnavailableFallback()` | 4×2 availability table | Available all executed; unavailable authority missing이면 fail |
| Unit `OfficialBenchmarkComparatorTest` | `comparesFourComponentsLexicographically()` | Hand cases | Exact expected winner/tie |
| Unit `OfficialBenchmarkComparatorTest` | `doesNotUseStructuralTieBreakAsQualityDimension()` | Equal vector | Quality tie |
| Unit `OfficialRunCompletenessVerifierTest` | `rejectsAnyMissingOrAbnormalDeclaredWorker()` | Set oracle | Partial success never complete |
| Unit `CutoverStateMachineTest` | `acceptsOnlyLegalProposedTransitions()` | Truth table | Illegal transition 100% reject |
| Unit `CutoverStateMachineTest` | `doesNotEnterTrafficSemanticStateBeforeProviderReceiptAndReadBack()` | Distinct state/pointer CAS oracle | Premature `RUNNING/ACTIVE/ROLLED_BACK` 0 |
| Unit `CutoverObservationWindowTest` | `restoresPersistedDeadlineAndFailsClosedOnClockOrTelemetryGap()` | Clock/window oracle | Restart-shortening/false completion 0 |
| Unit `ProductionAuthorityVerifierTest` | `rejectsWrongEnvironmentActionDigestScopeOrTime()` | Authority variants | Scope expansion 0 |
| Unit `RollbackCoordinatorTest` | `restoresExpectedPointerAndPreservesArtifacts()` | Cutover scenario | Pointer restored, artifact delete 0 |
| Contract `SignedEvidenceVerifierContractTest` | `verifiesDigestBeforeDeserializeAndSignatureBeforeUse()` | Corrupt bytes | No unverified object exposure |
| Contract `CutoverControlPortContractTest` | `convergesDuplicateSameCommandAndRejectsDivergentReplay()` | State/CAS oracle | Same/same converge; same/different fail |
| Contract `CutoverControlPortContractTest` | `reconcilesProviderSuccessBeforeControlStateCommit()` | Response-loss snapshot | Same transition converges; no second divergent action |
| Integration `CalibrationDeclaredRunCompletenessIT` | `accountsForEveryDeclaredRunIncludingFailures()` | Frozen run matrix | Missing/excluded run 0 |
| Integration `CalibrationIndependentAnalysisIT` | `recomputesProducerDecisionFromImmutableRawEvidence()` | Stat oracle | Digest/decision equality |
| Integration `OfficialRunCompletenessIT` | `publishesOnlyAfterAllWorkersAndBothVerifiersPass()` | Worker fault matrix | False publication 0 |
| Integration `OfficialReplayIT` | `replaysIdenticalNormalManifestToRequiredFingerprints()` | Official manifest | Strong envelope exact equality |
| Integration `ArtifactMigrationDigestIT` | `copiesReadBackVerifiesThenCasSwitches()` | Source/destination backend | Digest equality before pointer |
| Provider `AwsShadowParityIT` | `preservesLogicalArtifactsResultAndTermination()` | Local/AWS same manifest | Semantic mismatch 0 |
| Provider `AwsCanaryCutoverIT` | `neverExceedsAuthorityAndCanRollback()` | Scoped authority | Scope breach 0, rollback PASS |
| Provider `ProductionActivationIT` | `activatesExactApprovedRevisionAndWritesAudit()` | Activation authority | Exact pointer/audit/health |
| Provider `RollbackRehearsalIT` | `reconcilesInflightStateWithoutDeletingImmutableArtifacts()` | Fault scenarios | Unresolved state/delete 0 |
| Architecture `Phase14DependencyRulesTest` | `keepsCloudCryptoCutoverAndVendorTypesOutOfStableModules()` | Module graph/bytecode | Forbidden reference 0 |

### 11.3 Red → green sequence

1. Authority model red: unsigned/test/expired/revoked/wrong-scope evidence가 거부되지 않음.
2. Travel red: decimal fixture 또는 unspecified Great Circle가 manifest에 들어감.
3. Freeze red: input/build/config/seed/environment 변경이 같은 plan digest를 만듦.
4. Calibration red: missing run/post-hoc exclusion이 complete decision을 만듦.
5. Parameter red: Q-BENCH values와 ALNS parameters가 한 hidden default bag에 섞임.
6. Phase 13 red: closed gate에서도 hybrid package/backend를 호출함.
7. Official red: incomplete worker/one verifier만 PASS인데 publication함.
8. Replay red: identical normal manifest 결과/trace digest가 drift함.
9. Shadow red: provider metadata/completion order가 semantic result를 바꿈.
10. Cutover red: authority 없이 canary/activation, provider receipt 전 terminal state 또는
    illegal transition이 가능함.
11. Observation red: restart/clock anomaly/missing telemetry가 hold window 완료로 처리됨.
12. Rollback red: artifact delete, stale pointer overwrite, unresolved in-flight가 성공 처리됨.
13. Architecture/full evidence green: 모든 layer, corruption/fault/security/ops/cost와
    independent review가 통과함.

앞 단계가 green이 아니면 뒤 단계를 fake fixture로 green 처리하지 않는다.

### 11.4 Layer application

| Layer | 적용 | 대체 불가 evidence |
|---|---|---|
| Unit/property | 항상 | Exact values, digests, state/comparator laws |
| Architecture | 항상 | SDK/vendor/verifier dependency and hidden-default scan |
| Local contract/E2E | 항상 | Provider-neutral manifest/completeness/cutover semantics |
| Experiment environment | Calibration | Frozen run matrix/raw/statistical/security/cost evidence |
| Actual AWS integration | AWS reference 선택 시 | IAM/KMS/CAS/retry/timeout/alarms/deployment semantics |
| Selected substituted provider | Phase 12 applicable 시 | Same contract/parity/migration evidence |
| Shadow | Cutover 전 필수 | Same logical manifest, no production publication |
| Canary/activation | Explicit production authority 뒤 | Actual traffic/pointer/audit/rollback evidence |

Emulator/local 성공은 actual provider evidence를 대체하지 않고, cloud deploy 성공은
domain/verifier/calibration correctness를 대체하지 않는다.

## 12. Verification commands and pass criteria

현재 checkout에는 target reactor/type/test/`./mvnw`가 없으므로 아래는 **future
fail-closed command baseline candidate**다. Phase 00 accepted wrapper/module 이름과
일치하도록 review한 뒤 실행한다. Command 성공을 official 수치/approval/deploy/cutover
증거로 과장하지 않는다.

```bash
./mvnw -B -ntp clean install

./mvnw -B -ntp -f rpdptw/application/pom.xml \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest='*Calibration*Test,*Official*Test,*Cutover*Test,*Rollback*Test' \
  clean test

./mvnw -B -ntp -f build/port-contract-tests/pom.xml \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest='SignedEvidenceVerifierContractTest,CutoverControlPortContractTest' \
  clean test

./mvnw -B -ntp -f build/architecture-rules/pom.xml clean verify

./mvnw -B -ntp -f distributions/local/pom.xml \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dit.test='CalibrationDeclaredRunCompletenessIT,CalibrationIndependentAnalysisIT,OfficialRunCompletenessIT,OfficialReplayIT' \
  -Pphase14-local-it clean verify
```

각 `clean test/verify` 직후 해당 command가 만든 fresh Surefire/Failsafe XML을 읽어
expected/discovered/executed class와 method set이 exact하게 같은지, required count가
non-zero인지, failure/error/skipped가 모두 0인지 봉인한다. Report가 없거나 stale하거나
expected method 하나가 실행되지 않으면 exit 0이어도 fail이다. Wildcard selector는
reviewed expected-method manifest와 exact reconciliation할 때만 허용한다.

`AWS_REFERENCE`가 selected provider인 경우에만 다음 IaC와 isolated environment candidate를
사용한다. Substituted provider는 accepted Phase 12가 정한 distribution/profile/tooling을
사용하며 AWS command를 fallback으로 실행하지 않는다.

```bash
sam validate --template-file deployment/aws/template.yaml --lint
cfn-lint deployment/aws/template.yaml \
  deployment/aws/alarms/required-alarms.yaml \
  deployment/aws/alarms/phase14-stop-conditions.yaml

./mvnw -B -ntp -f distributions/aws-serverless/pom.xml \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dit.test='CalibrationDeclaredRunCompletenessIT,CalibrationIndependentAnalysisIT' \
  -Pphase14-calibration-experiment clean verify \
  -Dphase14.experimentOnly=true \
  -Dphase14.calibrationPlanRef='<immutable-approved-plan-ref>'

./mvnw -B -ntp -f distributions/aws-serverless/pom.xml \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dit.test='ProviderDeploymentEvidenceIT,ArtifactMigrationDigestIT,AwsShadowParityIT' \
  -Pphase14-aws-shadow clean verify \
  -Dphase14.manifestRef='<sealed-official-manifest-ref>' \
  -Dphase14.deploymentEvidenceRef='<verified-deployment-evidence-ref>'
```

Production `sam deploy`, traffic/alias/pointer mutation과 rollback command는 universal
command baseline에 포함하지 않는다. 이는 security 회피가 아니라 exact
account/environment/change-set/action scope와 authority를 runtime에서 다시 검증해야
하기 때문이다. Approved release pipeline은 실제 command, caller role, input evidence
digests, exit, before/after pointer와 audit ref를 `CutoverRecord`에 남긴다.
Maven/Surefire/Failsafe test는 release authorization이나 traffic mutation mechanism이
아니며 production action을 내부에서 수행하지 않는다. Production suite는 approved release
action 전의 read-only preflight와 action 뒤 exact read-back/reconciliation/evidence 검증으로
분리한다.

Pass criteria:

- Required test failure/error/skip 0
- Forbidden stable-module cloud/vendor/cutover/crypto leakage 0
- Unsigned/test/expired/revoked/wrong-scope approval acceptance 0
- Decimal official travel cell과 unspecified Great Circle field 0
- Hidden/defaulted official numeric parameter 0
- Declared calibration/official worker 누락 0
- Candidate/result verifier false publication 0
- Producer/independent analysis mismatch 0
- Strong replay fingerprint mismatch 0
- Local/provider semantic parity mismatch 0
- Cross-tenant access, secret/PII log match와 missing audit event 0
- Approved cost/operations/observability threshold violation 0
- Authority scope/traffic bound breach와 illegal cutover transition 0
- Provider receipt/read-back 전 traffic-semantic final state 0
- Observation-window clock/restart/missing-telemetry false completion 0
- Rollback unresolved in-flight state와 immutable artifact deletion 0
- Independent review verdict `ACCEPTED`

미확정 threshold가 남아 있으면 해당 criteria는 pass가 아니라 `BLOCKED_OPEN_VALUE`다.

## 13. Evidence bundle and Definition of Done

### 13.1 Evidence bundle

```text
E-P14-CALIBRATION/
  source-and-predecessor-receipt
  signing-trust-policy-and-verifier
  integer-travel-authority
  great-circle-policy-authority
  frozen-calibration-plan
  declared-run-index
  raw-results-traces-telemetry-cost
  independent-analysis
  acceptance-decision
  official-alns-parameter-approval
  q-bench-02-execution-value-approval

E-P14-OFFICIAL-RUN/
  sealed-official-manifest
  phase13-applicability
  declared-screen-worker-round-index
  completion-and-retry-lineage
  candidate-verifier-reports
  finalization-audit
  result-verifier-report
  exact-benchmark-card
  identical-manifest-replay
  baseline-or-challenger-approval

E-P14-CUTOVER/
  provider-deployment-evidence
  compatibility-matrix
  artifact-state-migration-digests
  shadow-parity
  production-authority-envelopes
  proposed-canary-hold-activation-events
  before-after-pointer-and-traffic
  security-operations-cost-observability
  production-activation-record

E-P14-ROLLBACK/
  last-safe-point
  rehearsal-and-stop-condition-cases
  rollback-authority
  trigger-and-transition-events
  inflight-reconciliation
  post-restore-security-publication-health
  independent-rollback-verdict

handoff/
  implementation-evidence-index
  production-activation-or-terminal-rollback-record
  runbooks-owners-expiry-known-limitations
```

각 bundle은 source commit, exact command/toolchain/environment, exit/test counts, artifact
digests, signature verification, reviewer/verdict/timestamp, known limitation, rollback point를
가진다. Restricted provider locator는 opaque ref로 두고 raw credential/account data를
복사하지 않는다.

### 13.2 Phase 14 Definition of Done

Phase 14는 다음 AND 조건을 모두 만족할 때만 `ACCEPTED`다.

1. Canonical detailed/review 문서가 approved되고 cited source/contract semantic impact가
   review됐으며 인접 digest를 acceptance로 쓰지 않는다.
2. Applicable Phase 00~12 accepted evidence graph가 complete하다.
3. Phase 13 applicability가 signed되고 current action-time trust/validity/revocation/
   freshness verification을 통과하며 forced execution이나 reverse entry dependency가 없다.
4. Integer fixture와 Great Circle policy가 separate signed authority를 가진다.
5. ALNS parameters와 `Q-BENCH-02` values가 separate measured approval을 가진다.
6. Calibration plan/result/independent analysis/acceptance가 immutable complete하다.
7. Official manifest에 hidden/default/open value가 없다.
8. All-declared-worker normal completion과 both-verifier publication path가 통과한다.
9. Exact comparator card와 identical-manifest replay가 통과한다.
10. Actual selected-provider deployment/parity/security/ops/cost evidence가 approved된다.
11. Shadow와 migration digest, rollback target/rehearsal가 통과한다.
12. Canary와 activation 각각에 exact production authority, provider action receipt,
    pointer/traffic read-back, observation-window와 audit가 있다.
13. `ACTIVE`와 `ProductionActivationRecord`가 independently verified되고 unresolved
    in-flight/pending transition/pointer가 없다.
14. `E-P14-*` bundle과 independent review가 sealed된다.
15. `ImplementationEvidenceIndex`와 `ProductionActivationRecord`가
    scheduler/operations에 handoff된다.
16. OPEN/GATED/deferred와 limitation을 값/완료 claim으로 숨기지 않는다.

Production activation이 일어나지 않았거나 activation 뒤 rollback/reject된 결과는 운영상
안전한 terminal 결과일 수 있지만 Phase 14 `ACCEPTED`와 production cutover DoD는 아니다.
그 경우에도 terminal `RollbackRecord`/`CutoverRejectedRecord`와 evidence index를 handoff하고,
Scheduler는 evidence에 따라 `ROLLED_BACK`, `FAILED`, `GATED` 또는 `ACCEPTED`를 구분한다.

## 14. Blockers, owner, last safe state and restart

| Blocker/gate | Owner | Last safe state | Restart condition |
|---|---|---|---|
| Phase 00~11 implementation/evidence unaccepted; document review는 live 15/15 완료 | Phase owners + scheduler/reviewers | Current document verdict와 offline test 설계; official action 0 | Residual contract 해소와 모든 applicable implementation/evidence/review receipt accepted |
| Phase 12 applicability unknown | Product/Platform | AWS reference assumption도 production에는 사용 안 함 | Signed provider/applicability decision; substituted provider면 accepted Phase 12 |
| Phase 13 applicability actual authority/receipt 없음; schema/signature field 부재는 `RESOLVED_BY_PHASE13_V1_2` | Scheduler + Product/Algorithm/Architecture + Security/Release | Phase 13 v1.2 proposed envelope/action-time verification schema; `ALNS_ONLY_GATE_CLOSED`, pool/model/outcome/hybrid refs와 Phase 13 실행 0 | Closed면 approved-policy actual signed `Skip` + current action-time `PASS`; hybrid면 동등한 signed `Activated` + accepted Phase 13 handoff. Phase 14→13 entry 역의존 금지 |
| Integer travel fixture 없음 | Input/Matrix + Benchmark | Current decimal fixture read-only, test-only oracle | Versioned integer bytes/digest/validation/source approval |
| Great Circle 상세 미승인 | Input/Matrix + Domain/Architecture | Missing-`D` official/production path blocked | Function/version/earth model/constants/precision/vectors approval |
| Official ALNS parameters 없음 | Algorithm + Quality | Explicit experiment config only | Complete measured parameter envelope approval |
| `Q-BENCH-02` 공식 수치 없음 | Benchmark/Quality | Logical coordinator/test-only manifest | Frozen calibration, measured result, separate explicit approval |
| Evidence signing/trust 미승인 | Security/Release | Phase 13 v1.2/Phase 14 proposed schema와 negative tests only; algorithm/trust-root/policy 값과 actual receipt 0 | Signature algorithm/profile, trust roots/store, revocation/time/freshness/canonicalization/verifier policy 승인 + actual evidence |
| Calibration threshold/corpus/sample 미승인 | Benchmark/Quality | Exploratory experiment, no official selection | Preregistered plan/acceptance policy and independent review |
| Actual provider deployment evidence 없음 | Platform/Security/Ops/FinOps | Accepted local or non-prod reference | Actual deployment/IaC/parity/security/ops/cost/rollback evidence |
| Production authority 없음 | Product/Release/Security/Ops | Shadow hold, production pointer unchanged | Exact canary/activation scope, validity, rollback authority signatures |
| Stop threshold 미확정 | Product/Ops/SRE/FinOps | Shadow or pre-canary hold | Approved explicit thresholds and alarm/runbook mapping |
| Distinct control-state/provider-pointer transition contract 미승인 | Phase 08~11 Application/Storage/Coordinator + Phase 14 Release | Pending traffic action 0, current pointer/state 유지 | Typed preconditions, provider receipt/read-back, crash-between-CAS/reconciliation conformance 승인 |
| Durable observation/deadline clock authority 미승인 | Application/Coordinator/Ops/SRE | Shadow/pre-canary hold; 완료 추정 0 | Persisted window/deadline, clock authority/anomaly policy와 crash/restart/missing-telemetry tests 승인 |
| Rollback target/role unhealthy | SRE/Incident/Platform | No new traffic | Verified last safe point, role, reconciliation/rehearsal |
| `Q-VAR-01` deferred | Product/Domain/Algorithm | Current pair/terminal/bank/travel contract | Register restart evidence와 separate approval |

같은 blocker가 있어도 문서 review, negative test, fixture schema와 last-safe rehearsal은
진행할 수 있다. 그러나 owner/restart evidence 없이 gate status를 바꾸거나 mock/legacy/
부분 성공으로 닫을 수 없다.

## 15. Failure handling and anti-patterns

### 15.1 Typed failures

```text
PREDECESSOR_EVIDENCE_MISSING
SOURCE_CONTRACT_IMPACT_UNREVIEWED
UNTRUSTED_OR_INVALID_SIGNATURE
AUTHORITY_SCOPE_MISMATCH
INTEGER_TRAVEL_AUTHORITY_MISSING
GREAT_CIRCLE_POLICY_MISSING
CALIBRATION_PLAN_NOT_FROZEN
CALIBRATION_RUN_SET_INCOMPLETE
INDEPENDENT_ANALYSIS_MISMATCH
ACCEPTANCE_VALUE_OPEN
OFFICIAL_PARAMETER_UNAPPROVED
PHASE13_SCOPE_MISMATCH
OFFICIAL_MANIFEST_INVALID
OFFICIAL_RUN_INCOMPLETE
CANDIDATE_OR_RESULT_VERIFICATION_FAILED
PROVIDER_DEPLOYMENT_EVIDENCE_MISSING
SHADOW_NOT_COMPARABLE
PRODUCTION_AUTHORITY_MISSING
CUTOVER_STOP_CONDITION
ROLLBACK_FAILED
IRREVERSIBLE_ACTION_BLOCKED
```

이 실패를 normal completion, unassignment, no-strict-improvement 또는 production success로
변환하지 않는다.

### 15.2 금지 anti-pattern

- README/GCP controller의 `8`, `5000`, clock seed를 official 수치로 복사
- 인터뷰 예시, test fixture, 과거 draft의 numeric value를 production default로 사용
- Current decimal `D/U`를 parser에서 반올림하고 원본 digest만 official로 표시
- Great Circle library 기본 earth radius/precision을 승인된 정책으로 위장
- ALNS parameter와 execution budget을 한 untyped environment variable bag에 혼합
- 좋은 seed/성공 run만 남기고 failed/outlier run을 삭제
- 서로 다른 candidate/manifest의 최선 metric을 합성
- 일부 worker, listing 결과, fastest completion으로 champion 확정
- Candidate `PASS`만으로 result publication 또는 benchmark
- Phase 13 gate를 calibration/cutover 일정 때문에 자동 열기
- Hybrid accepted evidence가 없는데 raw backend incumbent/ObjVal 사용
- GCP/ignored `.serverless`/historical `docs/codex` evidence를 canonical Phase 00~11로 재사용
- AWS stack exists 또는 `sam deploy` 성공을 production authority로 간주
- 한 approval로 provider deployment, canary, full activation, irreversible deletion까지 수행
- Missing telemetry/threshold를 “문제 없음”으로 처리
- Mutable `latest`, prefix listing, console screenshot, log 한 줄을 evidence authority로 사용
- Rollback에서 artifact/state/key/version을 삭제 또는 overwrite
- Review/approval signature를 test builder나 구현자가 합성

## 16. Final handoff and traceability

### 16.1 Implementation evidence index

```text
ImplementationEvidenceIndex
  schemaVersion
  phase = 14
  sourceCommit
  canonicalSourceAndRequirementImpactRefs
  predecessorEvidenceGraphRoot
  phase12ApplicabilityRef
  phase13ApplicabilityRef
  integerTravelAuthorityRef
  greatCirclePolicyAuthorityRef
  calibrationPlanResultAcceptanceRefs
  alnsParameterAuthorityRef
  qBench02ExecutionValueAuthorityRef
  officialManifestAndRunRefs
  officialBenchmarkCardAndReplayRefs
  providerDeploymentAndShadowRefs
  productionAuthorityRefs
  cutoverTransitionRefs
  rollbackEvidenceRef
  securityOperationsCostObservabilityRefs
  reviewVerdictRef
  knownLimitationsAndOpenGates
  rootDigest
  signedSeal
```

Index는 exact reference로 closure를 증명하며 object listing으로 evidence를 발견하지
않는다. Index에 들어간다고 raw artifact가 production authority로 승격되는 것은
아니며 각 evidence kind의 scope를 보존한다.

### 16.2 Production activation record

실제 activation이 성공한 경우에만 생성한다.

```text
ProductionActivationRecord
  schemaVersion
  environmentRef
  activeManifestFingerprint
  activeBuildRuntimeFingerprint
  activeProviderDeploymentDigest
  activeProfileTravelPolicyDigests
  previousPointerVersionAndDigest
  activePointerVersionAndDigest
  canaryAndActivationAuthorityRefs
  cutoverEventRangeDigest
  verificationAndHealthRefs
  rollbackLastSafePointRef
  implementationEvidenceIndexRef
  activatedAt
  activatedByApprovedRole
  operationsOwnerAndOnCallRef
  authorityExpiryOrReviewAt
  signedSeal
```

Activation하지 않았거나 rollback된 경우 이 record를 빈 값으로 만들지 않는다. 대신
terminal `RollbackRecord` 또는 `CutoverRejectedRecord`를 handoff한다.

### 16.3 Operations handoff

Production Operations가 받는 것:

- Exact active manifest/build/deployment/pointer identity
- Official card, verifier/replay, security/ops/cost/observability evidence refs
- State/worker/result exact lookup and publication rules
- Stop condition, alarm, cancel/retry/recovery와 rollback runbook
- Authority validity/expiry/review schedule와 authorized roles
- Known limitations, open/deferred gates와 incident owner
- Immutable evidence index and activation/rollback record

받지 않는 것:

- Raw PII/full input/credential/private key/license value
- Prefix listing이나 mutable latest만 있는 locator
- Test signer/fake approval/test-only fixture
- Legacy GCP semantics를 target fallback이라고 부른 record
- Phase 13 raw MIP incumbent 또는 provider SDK DTO

### 16.4 Requirement traceability

| Requirement | Source | Work/test | Planned evidence |
|---|---|---|---|
| Official integer travel | `Q-MTX-01~03`, Master §8, Phase 02 §13.3 | WP14-2, integer/provenance/generated-`U` authority tests | `E-P14-CALIBRATION.travel` |
| Approved Great Circle | Master §8, Domain §6, Phase 02 blocker | WP14-2, reference-vector oracle | `E-P14-CALIBRATION.great-circle` |
| `Q-BENCH-02` official values | Register `Q-BENCH-02`, Master §14.4/§15.8 | WP14-3~4, separate approval tests | `E-P14-CALIBRATION.execution-values` |
| Official ALNS parameters | Master §13~14, Phase 06 explicit config | WP14-2~4, separation/acceptance tests | `E-P14-CALIBRATION.alns-parameters` |
| Frozen experiment/reproducibility | Master §13, Domain §17.9 | WP14-2~5, freeze/analysis/replay | `E-P14-CALIBRATION`, `E-P14-OFFICIAL-RUN` |
| All-worker/both-gate official result | Master §14, Phase 07 §15.3, Phase 10 | WP14-5, completeness/corruption | `E-P14-OFFICIAL-RUN` |
| Phase 13 optional branch | `C-17`, Integrated §17, actual Phase 13 v1.2 §6.5/§14.3~14.4 | WP14-4, signed applicability/action-time/dependency-cycle tests | Signed applicability envelope + action-time verification receipt + optional P13 refs; field gap `RESOLVED_BY_PHASE13_V1_2`, actual receipts `NOT_PRODUCED` |
| Provider/deployment evidence | `Q-INFRA-01`, Integrated §15~16, Phase 11 §18.3, actual Phase 12 §18.4 | WP14-6, actual provider/parity | `E-P14-CUTOVER.provider` |
| Production authority separation | Master §16.3, Plan Phase 14 | WP14-7~8, authority scope tests | `E-P14-CUTOVER.authority` |
| Shadow/versioned cutover | Master §15.10/§16.2, Integrated §18 | WP14-6~8, shadow/canary/activation | `E-P14-CUTOVER` |
| Rollback last safe point | Master §16.2, Architecture §5.3, Plan §12 | WP14-6~8, fault/rehearsal | `E-P14-ROLLBACK` |
| Security/cost/observability | Architecture §5.5~5.6, Integrated §19~22 | WP14-3/6~9 | All P14 bundles |
| Final production handoff | Plan Phase 14/system DoD | WP14-9, index/record tests | Evidence index + activation/rollback record |

## 17. Reviewer checklist

- [ ] Document status와 implementation `GATED`를 분리했다.
- [ ] 사용자 고정 source authority와 exact `Q-*` 상태를 보존했다.
- [ ] `SUPERSEDED` master와 `docs/codex`를 historical only로 사용했다.
- [ ] Phase 00~11 detail 존재를 accepted evidence로 과장하지 않았다.
- [ ] Phase 12/13 actual detail의 `NOT_STARTED`/`GATED_NOT_STARTED`와 applicability
      branch를 정확히 분리했다.
- [ ] Phase 13 gate-closed ALNS-only branch에서 forced hybrid 실행이 없다.
- [ ] Phase 13 v1.2 signed applicability field gap 해소와 actual trust/receipt 부재를
      분리하고 Phase 14→13 reverse entry dependency를 만들지 않는다.
- [ ] Integer fixture, Great Circle, official ALNS parameter, `Q-BENCH-02`, provider/deployment,
      production authority가 각각 독립 gate와 signed evidence를 가진다.
- [ ] Generated `U`의 canonical `CEILING` formula, missing-only `45 km/h`, invalid-present
      rejection과 provided/generated provenance를 보존했다.
- [ ] Current decimal fixture나 legacy/default 숫자를 official로 만들지 않았다.
- [ ] Calibration이 input/build/source/config/seed/environment와 analysis를 freeze한다.
- [ ] Statistical threshold/sample/confidence/cost/SLO의 미확정 값을 open으로 남겼다.
- [ ] All-declared-worker, both-verifier, exact comparator와 replay가 official gate다.
- [ ] Shadow/canary/hold/approve/activate/rollback state 이름을 proposed로 표시했다.
- [ ] Production action의 scope/expiry/audit/rollback과 irreversible 비범위를 명시했다.
- [ ] Provider deploy 성공과 production authority를 서로 대체하지 않았다.
- [ ] Stop condition과 rollback last safe point가 fail-closed다.
- [ ] Java/config/IaC type/signature/dependency/pseudocode가 candidate임을 표시했다.
- [ ] Exact test class/method/fixture/builder/oracle/red→green/layer/command/pass criteria가 있다.
- [ ] Evidence bundle/DoD/anti-pattern/blocker owner/restart/handoff가 완결됐다.
- [ ] 인접 계약은 stable section 의미와 accepted artifact/evidence로 검증하고,
      인접/reciprocal digest를 compatibility 또는 acceptance로 사용하지 않는다.
