# Phase 12 사람용 구현 가이드 — Provider substitution

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 12 only
guide_revision: "correction-01"
correction_status: F_HG_P12_001_THROUGH_004_ADDRESSED_PENDING_INDEPENDENT_RECHECK
correction_review_source: docs/implementation/human-guides/reviews/phase-12-review.md
correction_target_sha256_before: a0e29edfacc21d8a9f23e68b665db0eb62e862fec1f25d7a8031d0a96c2c6fdf
canonical_phase_count: 15
phase: "12"
phase_name: provider-substitution
canonical_phase_document: docs/implementation/phases/phase-12-provider-substitution.md
canonical_phase_review: docs/implementation/reviews/phase-12-review.md
canonical_phase_document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
canonical_phase_document_version: "1.3 + ALNS_FIRST_1.0 overlay"
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
candidate_provider_status_observed: NOT_SELECTED
provider_adoption_status_observed: NOT_APPROVED
deployment_status_observed: NOT_DEPLOYED
evidence_status_observed: NOT_PRODUCED
production_authority_observed: NOT_GRANTED
entry_gate_status: GATED_BY_UNACCEPTED_PREDECESSORS_AND_PROVIDER_ADOPTION
handoff_status_observed: NOT_READY
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_snapshot_at: "2026-07-29T01:10:23+0900"
inventory_snapshot_policy: ONE_TIME_LIVE_SNAPSHOT_UNCOMMITTED_UNAPPROVED
inventory_drift_status: CONCURRENT_PHASE00_SCAFFOLD_AND_PROGRESS_UPDATE_OBSERVED
post_snapshot_drift_policy: FROZEN_DO_NOT_RESNAPSHOT_OR_TREAT_AS_ACCEPTED
correction_inventory_observed_at: "2026-07-29T02:24:51+0900"
correction_inventory_policy: READ_ONLY_LIVE_DRIFT_NOT_AUTHORITY_OR_ACCEPTANCE
source_fingerprint_scheme: git_blob_at_inventory_observed_commit
prerequisite_phases:
  - "00"
  - "07"
  - "08"
  - "09"
  - "10"
  - "11"
conditional_downstream_phases:
  - "13"
  - "14B"
source_sections_and_fingerprints:
  docs/master-design.md: "§1~4, §13~17; especially §15.10 RM-8 and §16.2~16.3 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/2026-07-26-domain-design.md: "§2~3, §7~10, §15~18, §21 | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "§1~3, §5~6 | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/architecture-domain-implementation-design.md: "§16, §19~28 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-BENCH-02/Q-INFRA-01/Q-VAR-01 rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~6, Phase 11~14, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§1~10 | HEAD 250aa90ae568a6b32ec905fa5ee456d430ff72cf; frozen live 3e9dcd16621e459e4572a094cb71ab62a3978360"
  docs/implementation/phases/phase-11-aws-reference-distribution.md: "§6.1~6.3, §14.1~14.2, §17, §18.2 | 14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8"
  docs/implementation/phases/phase-12-provider-substitution.md: "§1~20 | 63b9defb25d7771bce590d593db9485367724918"
  docs/implementation/reviews/phase-12-review.md: "§1~9 and F-P12-001~008 | d55633a518d3099cdc42ff00d6eb08961f973369"
  docs/implementation/phases/phase-13-optional-hybrid-route-selection.md: "§4.1, §6.5, §14.2~14.4 | cb3cd961c87b034625ad138046b5745822df16bd"
  docs/implementation/phases/phase-14-official-calibration-cutover.md: "§0.1, §3.1~3.3, §5.3~5.4, §13.2~13.3 | c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26"
expected_reader:
  - Java interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW route, capacity, time-window propagation을 구현해 보았다
  - RPDPTW pair identity, immutable artifact, CAS, provider-neutral runtime은 처음 접한다
owner_roles:
  implementation: approved candidate-provider adapter owner
  application_contract: Phase 08 Application/Local Runtime owner
  storage_contract: Phase 09 Object Storage owner
  coordinator_contract: Phase 10 Provider-neutral Coordinator owner
  aws_reference_subject: Phase 11 AWS Reference Distribution owner
  provider_adoption: Product + Platform + Operations
  security: Platform Security + Data Governance
  reliability: SRE + Disaster Recovery
  performance_cost: Performance + FinOps
  independent_oracle: provider-neutral conformance test owner
  review: independent Phase 12 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P12-PROVIDER-CONTRACT
  - E-P12-PARITY
  - E-P12-MIGRATION
  - E-P12-SECURITY
  - E-P12-OPERATIONS
  - E-P12-PERFORMANCE
  - E-P12-ROLLBACK
```

> 이 문서는 Java와 CVRPTW 경험이 있는 신규 구현자가 Phase 12의 경계를 이해하고,
> gate가 열린 뒤 승인된 한 provider 교체 축을 안전하게 구현·검증하기 위한 교육형
> 지시서다. 현재 checkout의 multi-module 구조는 동시 작업 중인 **미커밋·미승인
> Phase 00 scaffold**다. Phase 12 candidate adapter, provider conformance module,
> distribution, deployment, 실제 AWS/candidate evidence는 없다. 이 문서의 Java
> 이름·경로·명령은 별도 표시가 없는 한 **`PROPOSED INTERNAL` 또는 `FUTURE`**이며
> 존재하는 API나 승인된 public contract로 읽지 않는다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 12는 “GCP나 Kubernetes에 한 번 띄워 보기”가 아니다. **승인된 한
substitution axis에서 AWS reference와 candidate provider가 같은 provider-neutral
계약을 각각 독립 oracle에 통과하고, 그 뒤 semantic parity·migration·security·
operations·rollback evidence를 단방향 DAG로 봉인하는 단계**다.

현재 상태를 네 축으로 나누면 혼동이 줄어든다.

```text
AWS target/reference 선택                 = RESOLVED
candidate provider와 교체 축 선택         = NOT_SELECTED
Phase 12 implementation/evidence          = NOT_STARTED / NOT_PRODUCED
candidate adoption/production cutover 권한 = NOT_APPROVED / NOT_GRANTED
```

따라서 candidate가 더 빠르거나 싸 보이는 것, resource가 생성된 것, emulator가
green인 것, AWS와 candidate 출력 문자열이 같은 것은 완료 신호가 아니다.

## 2. 큰 그림, 배경과 필요한 이유

### 2.1 “클라우드만 바꾸면 된다”가 위험한 이유

CVRPTW batch solver를 다른 runtime으로 옮길 때는 input을 읽어 solver를 호출하고
결과 파일을 쓰는 정도로 보일 수 있다. RPDPTW에서는 provider adapter가 다음 의미를
실수로 다시 만들 수 있다.

- Pickup과 delivery를 서로 다른 message나 retry identity로 처리하면 same-vehicle,
  precedence, exactly-once가 깨진다.
- Prefix listing이나 event 도착 순서로 완료를 판단하면 누락 worker가 있는데도
  champion을 만들 수 있다.
- Storage generation/ETag를 logical identity에 넣으면 provider를 바꿀 때 같은
  artifact가 다른 의미가 된다.
- Check-then-write를 CAS처럼 쓰면 두 writer가 모두 성공해 state나 publication이
  갈라진다.
- Candidate와 AWS가 같은 공용 bug를 공유하면 “둘이 같다”는 비교만으로 잘못된
  결과도 통과한다.
- Runtime timeout을 `MAX_STEPS_REACHED`로 번역하면 infrastructure failure가 정상
  품질 종료가 된다.
- Ambient credential이나 thread-local tenant에 의존하면 async retry/re-entry에서
  다른 tenant 권한이 적용될 수 있다.
- Candidate 성능 수치를 본 뒤 어려운 test를 `NOT_APPLICABLE`로 바꾸면 parity가
  사후 조작된다.

그러므로 실행 전에 교체 축, applicable case, oracle, config를 먼저 봉인하고 각
provider를 독립 검증해야 한다.

```text
approved provider + approved axes + non-prod scope
→ pre-sealed applicability/config
→ AWS reference subject ───────┐
                               ├─ same cases + independent oracle
→ candidate provider subject ──┘
→ each subject PASS
→ provider-neutral projection comparison
→ security/operations/performance/migration/rollback evidence
→ standalone provider evidence
→ forward-reference conformance manifest
→ independent review
→ separate acceptance receipt
```

### 2.2 교체 축은 직교한다

| Axis | 바꾸는 것 | 바꾸지 않는 것 | 예시일 뿐 확정 아님 |
|---|---|---|---|
| `STORAGE` | Object backend와 provider locator mapping | Artifact kind/schema/digest, logical key, CAS 의미 | S3 → GCS/Azure Blob |
| `DURABLE_WORKFLOW` | Start/wakeup/re-entry/cancel mapping | Phase 10 state/action/completeness | Step Functions → approved workflow/controller |
| `WORKER_COMPUTE` | Dispatch/status/stop와 runtime envelope | `WorkerRunId`, seed, warm start, requested work | Lambda → ECS/Cloud Run/Kubernetes Job |
| `DISTRIBUTION_MAPPING` | Assembly, config, reviewed deployment mapping | Core/application semantics | AWS serverless → approved distribution |

한 축의 제약 때문에 모든 축을 한꺼번에 바꾸지 않는다. Lambda memory가 부족하다는
evidence가 있다면 storage와 workflow를 유지한 채 compute만 평가할 수 있다.
Storage residency가 문제라면 compute를 바꾸지 않을 수 있다. 이 최소 변화 원칙은
failure 원인과 rollback point를 명확하게 한다.

### 2.3 Canonical 15 Phase에서의 위치

| Phase | Producer output 또는 책임 | Phase 12와의 관계 |
|---:|---|---|
| 00 | Reactor, module/package/DAG, provider leakage rule | Conformance와 adapter를 격리할 선행 gate |
| 01 | Canonical input와 normalization | Provider가 재해석하면 안 되는 business meaning |
| 02 | Complete `PreparedTravel`, immutable problem | Provider 간 동일하게 이동할 artifact |
| 03 | Propagation, evaluation, comparator | Workflow/adapter가 복제하면 안 되는 계산 |
| 04 | Exact bound profile/capability | `latest`/customer fallback 없이 보존할 identity |
| 05 | Pair insertion과 초기 portfolio | Assignment의 pair-atomic 기원 |
| 06 | COW ALNS, replay, typed termination | Retry에서 보존할 seed/work/identity |
| 07 | 두 verifier와 `PublishableResult` | Publication eligibility의 유일한 생산자 |
| 08 | Application use case, port, local oracle | Candidate adapter가 구현할 port owner |
| 09 | Exact-key artifact, verified read, CAS, no-DB | Storage conformance owner |
| 10 | State/action, completeness, retry/cancel/deadline | Workflow/compute conformance owner |
| 11 | S3 + Step Functions + Lambda reference evidence | Accepted standalone reference evidence producer |
| **12** | **승인된 provider substitution conformance** | **이 가이드의 유일한 구현 범위** |
| 13 | Optional route pool/MIP | 조건부 infra evidence consumer; `C-17`은 여전히 닫힘 |
| 14 | 14A benchmark / 14B official·production gate | Applicable Phase 12 evidence를 14B가 소비할 수 있음 |

전체 DAG의 핵심은 다음과 같다.

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10 → 11
                                                       └────→ 12  # 승인된 provider가 있을 때만

06 → 07 → 08 → 14A ALNS benchmark acceptance
14A receipt + 별도 C-17 승인 → 13 optional
11 + 14A receipt → 14B AWS ALNS-only candidate
accepted 12 → 14B  # selected substituted provider일 때만
accepted 13 → 14B  # official hybrid를 명시 선택할 때만
```

Phase 12와 13은 서로의 보편 predecessor가 아니다. Phase 13이 substituted runtime을
실제로 선택한 경우에만 accepted Phase 12 evidence를 조건부로 받는다. Phase 12가
Phase 13을 시작하거나 OR-Tools를 활성화하지 않는다.

### 2.4 Producer와 consumer 계약

| 경계 | Producer가 보장할 것 | Phase 12가 하면 안 되는 것 |
|---|---|---|
| Phase 07 → 12 | Cache-free candidate/result verifier와 `PublishableResult` | Provider 성공을 verifier `PASS`로 합성 |
| Phase 08 → 12 | Non-ambient access를 포함한 accepted use case/port/local oracle | Provider DTO/credential/default로 signature를 봉합 |
| Phase 09 → 12 | Immutable put, exact verified read, state CAS, distinct publication precondition | List/check-then-write/run-state token 재사용 |
| Phase 10 → 12 | Exact current pending action, same-state cancel fence, declared completeness, durable retry/deadline | Provider workflow에서 comparator/completeness를 재구현 |
| Phase 11 → 12 | Manifest-independent AWS evidence digest와 post-review receipt | Phase 12 manifest를 Phase 11이 역참조하게 만듦 |
| Phase 12 → 13 | Bounded infrastructure decision evidence | `C-17`, backend, hybrid implementation 권한을 부여 |
| Phase 12 → 14B | Applicable provider parity/migration/security/ops/rollback input | Production traffic/pointer authority를 스스로 발행 |

## 3. Source authority, fingerprint와 정확한 읽기 순서

### 3.1 충돌 해소 순서

```text
사용자 고정 지시
→ canonical docs/master-design.md
→ question register의 exact Q-* 상태
→ Final Domain의 domain/result 의미
→ Final Architecture의 module/port 배치
→ Integrated design의 Phase 12 substitution 계약
→ implementation README/plan/progress
→ canonical Phase 12 + review
→ Phase 11/13/14 stable handoff section
→ historical cross-check
```

[2026-07-26 이전 Master 초안](../../../2026-07-26-master-design.md), deprecated
문서와 과거 session 문서는 현재 authority가 아니다. Final Domain §18과 Final
Architecture §6에 남은 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 최신
[Canonical Master](../../../master-design.md)와
[question register](../../../master-design-open-questions.md)의
`Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`로 해소한다.
이 source drift를 숨기지 말고 source-owner erratum gate로 남긴다.

### 3.2 검증 가능한 source fingerprint

아래 Git blob은 `inventory_observed_at_commit`에서
`git rev-parse HEAD:<path>`로 얻었다. Source가 바뀌면 hash만 갱신하지 말고 바뀐
heading이 requirement, WP, test, evidence, gate에 미치는 영향을 다시 review한다.

| Source path/link | 직접 읽을 heading/section | HEAD Git blob |
|---|---|---|
| [Canonical Master §4](../../../master-design.md#4-구현-아키텍처와-책임-경계) | §1~4, §13~14, §15.10, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Final Domain §2](../../../2026-07-26-domain-design.md#2-cvrptw-개발자를-위한-rpdptw-입문) | §2~3, §7~10, §15~18, §21 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Final Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계) | §1~3, §5~6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated design Phase 12](../../../architecture-domain-implementation-design.md#16-phase-12--ecs-gcp와-kubernetes-future-substitution) | §16, §19~28 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register `Q-INFRA-01`](../../../master-design-open-questions.md#q-infra-01) | §1~4, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README §6](../../README.md#6-phase-작업-순서) | §0~7, 15 Phase, ALNS-first DAG | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan Phase 12](../../master-realization-plan.md#phase-12--provider-substitution) | §2~6, Phase 11~14, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry) | §1~10, scheduler/implementation registry | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Canonical Phase 12 §4](../../phases/phase-12-provider-substitution.md#4-entry-gate와-evidence-receipt) | 전체, 특히 §2~§19 | `63b9defb25d7771bce590d593db9485367724918` |
| [Phase 12 review §4](../../reviews/phase-12-review.md#4-findings) | §1~9, `F-P12-001`~`F-P12-008` | `d55633a518d3099cdc42ff00d6eb08961f973369` |
| [Phase 11 §18.2](../../phases/phase-11-aws-reference-distribution.md#182-phase-12-handoff) | §6.1~6.3, §14.1~14.2, §17, §18.2 | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` |
| [Phase 13 §14.2](../../phases/phase-13-optional-hybrid-route-selection.md#142-conditional-input--actual-but-unaccepted-phase-12) | §4.1, §6.5, §14.2~14.4 | `cb3cd961c87b034625ad138046b5745822df16bd` |
| [Phase 14 §3.1](../../phases/phase-14-official-calibration-cutover.md#31-gate-matrix) | §0.1, §3.1~3.3, §5.3~5.4, §13.2~13.3 | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` |

`execution-progress-and-results.md`는 concurrent scheduler 작업으로 live bytes가
바뀌었다. HEAD blob은 위 값이고 이 guide가 한 번 고정한 live blob은
`3e9dcd16621e459e4572a094cb71ab62a3978360`이다. 이후
`build/generate-phase-00-evidence.sh` 같은 외부 Phase 00 파일이 더 나타난 것을
관찰했지만 재스냅샷하거나 승인된 state로 흡수하지 않았다.

### 3.3 구현 전 정확한 읽기 순서

신규 구현자는 다음 순서를 지킨다. 링크를 “참고”로만 열지 말고 각 단계의 질문에
답을 적은 뒤 다음으로 이동한다.

1. [Canonical Master §4.4~4.6](../../../master-design.md#44-의존-방향)에서
   authority, lifecycle, dependency 불변조건을 읽는다.
2. [Final Domain §2](../../../2026-07-26-domain-design.md#2-cvrptw-개발자를-위한-rpdptw-입문),
   [§8](../../../2026-07-26-domain-design.md#8-stable-solution-request-bank와-cow),
   [§15](../../../2026-07-26-domain-design.md#15-verification-finalization과-result)에서
   request pair, stable state, two-gate result 의미를 익힌다.
3. [Final Architecture §2.2](../../../2026-07-26-architecture-design.md#22-module-dag와-금지-dependency),
   [§5.1](../../../2026-07-26-architecture-design.md#51-provider-neutral-운영-port)에서
   module DAG와 port owner를 읽는다.
4. [Integrated design §16](../../../architecture-domain-implementation-design.md#16-phase-12--ecs-gcp와-kubernetes-future-substitution)에서
   axis별 교체와 migration 순서를 읽는다.
5. [Question register](../../../master-design-open-questions.md)의 exact
   `Q-INFRA-01`, `Q-BENCH-02`, `Q-VAR-01` 상태를 확인한다.
6. [Implementation README §4](../../README.md#4-canonical-phase와-review-index)와
   [Master Plan Phase 12](../../master-realization-plan.md#phase-12--provider-substitution)에서
   15 Phase 위치와 target evidence를 확인한다.
7. [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry)와
   [§10](../../execution-progress-and-results.md#10-실제-구현-execution-registry)에서
   scheduler status와 live implementation authority를 확인한다.
8. [Canonical Phase 12](../../phases/phase-12-provider-substitution.md) 전체와
   [review findings](../../reviews/phase-12-review.md#4-findings)을 읽는다.
9. [Phase 11 §18.2](../../phases/phase-11-aws-reference-distribution.md#182-phase-12-handoff)에서
   실제 predecessor receipt를, [Phase 13 §14.2](../../phases/phase-13-optional-hybrid-route-selection.md#142-conditional-input--actual-but-unaccepted-phase-12)와
   [Phase 14 §3.2](../../phases/phase-14-official-calibration-cutover.md#32-phase-0012-evidence-receipt)에서
   consumer가 요구하는 bounded evidence를 읽는다.
10. 마지막으로 HEAD/live inventory를 다시 읽되, 이미 고정한 snapshot과 다른 외부
    변화는 별도 drift note로만 남긴다.

### 3.4 Entry gate를 읽고 서명하는 순서

Phase 12 implementation은 아래 AND gate다.

| Gate | 필요한 evidence | 현재 판정 | 닫혀 있을 때 마지막 안전 지점 |
|---|---|---|---|
| `G12-E0 Source` | Canonical blobs/section semantic review | Guide snapshot만 있음 | 문서·fixture review |
| `G12-E1 Phase 08~10` | Accepted port/state/failure/evidence refs | 없음 | Contract catalog와 red specs |
| `G12-E2 Phase 11` | Accepted review + standalone AWS evidence/receipt | 없음 | AWS evidence schema review |
| `G12-E3 Adoption` | Exact candidate/axis/scope/capability approval | 없음 | Candidate module 생성 금지 |
| `G12-E4 Environment` | Isolated non-prod, scoped credential, budget/cleanup owner | 없음 | Offline fake/design |
| `G12-E5 Security/Ops` | Non-ambient access, lossless failure, IAM-equivalent, retention/retry/quota/telemetry policy | 없음 | Negative test 설계 |
| `G12-E6 Linearization/deadline` | Distinct publication precondition, same-state cancel fence, restart-safe deadline | 없음 | Pure state/virtual-clock red tests |

서명 전에 다음 질문에 모두 “예”라고 답해야 한다.

- Provider ID뿐 아니라 `approvedAxes`, environment class, expiry/review boundary가 있는가?
- Phase 11 evidence는 Phase 12 manifest/review/receipt를 역참조하지 않는가?
- Applicable case set이 provider 실행 전에 고정되는가?
- Missing capability를 `SUPPORTED`로 흉내 내지 않는가?
- Candidate가 actual provider environment에서 검증되는가?
- Production traffic/default 권한이 receipt에 명시적으로 `false`인가?

하나라도 아니면 WP12-2 이후로 가지 않는다.

## 4. RPDPTW primer, 용어집, identity, lifecycle과 invariant

### 4.1 CVRPTW customer와 RPDPTW request

CVRPTW에서는 customer visit 하나를 한 route에서 제거·삽입하는 경우가 많다.
RPDPTW의 원자 단위는 `Request`다. Real request는 pickup과 delivery 두 visit을
가지며 다음 네 조건을 동시에 만족해야 한다.

```text
same concrete vehicle
+ pickup exactly once
+ delivery exactly once
+ pickup position < delivery position
+ request는 route 또는 bank 중 정확히 한 곳
```

Delivery-only request도 logical pickup ownership을 유지하지만 physical pickup visit은
만들지 않는다. Route 초기 적재량에 참여하고 delivery에서 감소한다. Provider가
message를 둘로 나누거나 일부 completion만 commit해도 stable state에는 partial pair가
나오면 안 된다.

### 4.2 용어집

| 용어 | 이 Phase에서의 의미 | 흔한 오해 |
|---|---|---|
| Reference subject | AWS 실제 실행/evidence 피시험 대상 | 유일한 semantic oracle |
| Candidate provider | 별도 승인된 평가 대상 | 문서에 예시로 나온 GCP/ECS/Kubernetes |
| Substitution axis | 독립 교체하는 storage/workflow/compute/distribution 축 | Provider 전체 |
| Semantic identity | Provider가 달라도 같아야 하는 logical identity | URI/ARN/ETag/execution ID |
| Provider observation | Region, task ID, retry count 같은 비교 metadata | Result fingerprint 입력 |
| Immutable artifact | Kind/schema/canonical digest/length/authority를 가진 create-once bytes | “파일이 존재함” |
| Run state CAS | Solve state의 한 legal transition을 선형화 | Publication pointer CAS |
| Publication precondition | Verified result pointer의 별도 expected state | Run-state version |
| Declared completeness | Manifest가 미리 선언한 모든 worker outcome 집합 | List/event로 보이는 성공 집합 |
| Conformance case | 두 subject에 같은 assertion/oracle로 실행할 test | Provider별로 복사한 integration test |
| Capability decision | `SUPPORTED/UNSUPPORTED/GATED/INDETERMINATE` 판정 | boolean feature flag |
| Parity | 각 subject가 oracle에 통과한 뒤 semantic projection이 같음 | Raw JSON/metric 문자열 equality |
| Shadow | Publication/state/traffic authority 0인 비교 실행 | 작은 production canary |
| Adoption recommendation | 별도 review로 갈 수 있다는 evidence | Production default 승인 |

### 4.3 Identity를 층으로 나누기

Provider 교체에서 가장 중요한 연습은 “ID 하나”를 만들지 않는 것이다.

```text
Business/semantic identity
  Tenant / Submission / Solve / ManifestFingerprint
  RoundOrdinal / WorkerOrdinal / WorkerRunId / AttemptId
  Artifact kind/schema/content digest
  Candidate/result/publication digest

Provider observation
  Account/project/subscription/cluster
  Bucket/container/object locator
  Function/task/job/pod/execution ID
  ETag/generation/version token
  Provider trace/log/metric locator
```

`AttemptId`는 retry마다 바뀔 수 있지만 `WorkerRunId`, assignment, seed, warm start,
requested ALNS steps는 바뀌면 안 된다. Opaque provider version token은 CAS 비교에만
쓰고 정렬·파싱·fingerprint하지 않는다.

### 4.4 Artifact, state, publication과 observation lifecycle

```text
canonical bytes
→ kind/schema/length/digest verify
→ immutable put-if-absent
→ exact read-back verify
→ ArtifactRef
```

```text
VersionedRunState
→ exact expected state version
→ legal next state CAS
→ new VersionedRunState
```

```text
PublishableResultRef
→ distinct PublicationPrecondition
→ publication pointer CAS
→ same digest converges / different digest conflicts
```

세 lifecycle은 합치지 않는다. Artifact가 존재한다고 state가 완료된 것이 아니며,
`PUBLISHING` state가 됐다고 pointer CAS가 자동 성공한 것도 아니다. Provider event와
log는 reconciliation evidence이지 authority가 아니다.

### 4.5 Cancellation, retry와 deadline lifecycle

Cancellation request는 durable intent/hint다. Authority는 같은 run-state version에서
경쟁하는 `CANCEL_REQUESTED`와 `PUBLISHING` 중 성공한 CAS다.

```text
cancel request 저장
→ current state reload
→ CANCEL_REQUESTED CAS ─┐
                        ├─ exactly one state intent wins
→ PUBLISHING CAS ───────┘
```

Retry는 세 층을 구분한다.

```text
SDK retry              = 같은 provider operation, 같은 AttemptId
delivery retry         = 같은 logical event receipt reconciliation
application work retry = 같은 WorkerRunId, 새 AttemptId
```

Crash/re-entry 뒤 deadline을 process-local monotonic tick, wall clock 또는 provider
remaining time으로 조용히 복원하지 않는다. Durable deadline encoding/origin
reconciliation이 승인되지 않으면 deadline-enabled provider path는 `GATED`다.

### 4.6 Phase 12 고정 불변조건

1. Core, solver, verification, application의 provider SDK/type reference는 0이다.
2. Pair, route-bank XOR, travel, evaluation, comparator와 two-gate publication은
   provider 교체로 바뀌지 않는다.
3. Provider locator/execution/version token은 semantic fingerprint에서 제외한다.
4. Artifact kind/schema/canonical digest/authority lineage는 provider 간 같다.
5. Put-if-absent, exact verified read, state CAS, publication CAS 의미를 완화하지 않는다.
6. Listing, event arrival, completion order는 completeness/champion authority가 아니다.
7. Retry는 logical work/seed/warm start를 바꾸지 않는다.
8. Platform timeout/resource failure는 정상 ALNS termination이 아니다.
9. 두 verifier `PASS` 전 publication은 0이다.
10. Unsupported는 `UNSUPPORTED`, 미승인은 `GATED`, 증거 부족은 `INDETERMINATE`다.
11. Provider exception은 lossless neutral taxonomy로 mapping한다.
12. Secret, credential, resource name은 domain artifact가 아니다.
13. Codec은 versioned allowlist이며 Java serialization/SDK DTO를 저장하지 않는다.
14. Security, integrity, observability, performance는 서로 대체하지 않는다.
15. Phase 12 evidence는 Phase 13/14 production authority가 아니다.

## 5. 실제 repository inventory — HEAD, live drift와 목표

### 5.1 조사 시점과 고정 정책

이 guide는 HEAD와 live worktree를 한 상태처럼 말하지 않는다.

| Snapshot | 무엇을 뜻하는가 | 승인 의미 |
|---|---|---|
| HEAD `7cc890ee...` | Git에 기록된 baseline | 현재 작업의 안정 비교점 |
| 2026-07-29 01:10:23 KST live | 동시 Phase 00 작업이 만든 미커밋 reactor/legacy 이동 | 구현·evidence·acceptance 아님 |
| Snapshot 뒤 관찰된 추가 drift | 외부 작업이 계속 진행 중이라는 신호 | 재스냅샷하지 않고 제외 |

HEAD tree에는 root 단일 `pom.xml`, `src/main/java` 6개, `src/test/java` 1개와
GCP 자료가 있다. Live snapshot에서는 root source가 삭제 표시되고 다음
미추적 tree가 보였다.

```text
root pom parent/aggregator
├── rpdptw/
│   ├── core
│   ├── solver
│   ├── verification
│   ├── application
│   ├── capabilities
│   └── profile-catalog
├── build/
│   ├── architecture-rules
│   └── test-fixtures
└── legacy/
    └── gcp-placeholder
```

Live root POM blob은 `8aeca338c772527c28a38c89270177cd5182caf1`,
snapshot 당시 `rpdptw build legacy` file-list blob은
`93e35e76be7da36a23c3a4cec137ab934d8d8f19`다. 이 hash는 acceptance가 아니라
“어느 미승인 inventory를 보고 이 guide를 썼는가”만 재현한다.

### 5.2 현재 vs 목표 inventory

| 영역 | HEAD baseline | Frozen live snapshot | Phase 12 목표/판정 |
|---|---|---|---|
| Build | 단일 root JAR, wrapper 없음 | Parent/aggregator와 wrapper가 미추적 | Phase 00 acceptance 전 target build로 간주 금지 |
| Stable modules | 없음 | `rpdptw-*` POM과 `package-info.java` placeholder | 실제 Phase 08~10 port/type는 없음 |
| Stable Java | Legacy 6 main + 1 test | `rpdptw` Java 23개가 모두 `package-info.java` | Domain/application 구현으로 세지 않음 |
| Architecture tests | 없음 | 7개 test class + fixture self-test가 미추적 | Provider isolation 기반 후보, Phase 12 acceptance 아님 |
| Legacy | Root에 GCP SDK/controller | `legacy/gcp-placeholder`로 이동 중 | Characterization 대상, candidate baseline 아님 |
| Phase 08~10 ports | 없음 | Package placeholder만 있음 | Accepted signature/evidence 부재 |
| Phase 11 AWS | Tracked source 0 | S3/Step Functions/Lambda adapter/IaC 0 | Reference evidence 부재 |
| Phase 12 conformance | 없음 | `build/provider-conformance-tests` 0 | 부재 |
| Candidate adapters | 없음 | `adapters/` tree 0 | 승인 전 정상 부재 |
| Distribution/deployment | GCP historical 자료 | `distributions/`, `deployment/` 0 | 승인된 candidate tree 부재 |
| Failsafe | Root 설정 없음 | Live root에도 plugin/config 없음 | `*IT` 실행 계약 미구현 |
| Evidence | Ignored/generated 파일 가능 | `E-P12-*` 0 | `NOT_PRODUCED` |
| Progress | HEAD는 구현 0/15 | Live는 Phase 00 prerequisite remediation `IN_PROGRESS` | Phase 12는 계속 gated |

`adapters apps distributions deployment` file-list는 snapshot에서 empty blob
`e69de29bb2d1d6434b8b29ae775ad8c2e48c5391`이었다. “비어 있다”는 결함이 아니라
provider adoption 전 안전한 상태다.

Correction 01은 original one-time snapshot을 다시 쓰지 않고, 다음 **별도 live drift
observation**을 read-only로 기록했다. 이 표는 authoring provenance나 acceptance
authority가 아니며 correction 이후 변경을 자동 흡수하지 않는다.

| Correction 관찰 항목 | 2026-07-29 02:24:51 KST live bytes | 판정 |
|---|---|---|
| Branch/HEAD | `codex-implementation` / `7cc890ee1d0805df5ae14b633127fade4f978639` | Original authority commit과 같지만 worktree bytes는 별도다 |
| Root reactor | `pom.xml` SHA-256 `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`; root가 `rpdptw`, `build`, `legacy`를 aggregate | Phase 00 replacement evidence review 02 진행 중; 아직 accepted build contract 아님 |
| Build reactor | `build/pom.xml` SHA-256 `7b09571dbdaef2a25d33508f6154a6237e8ce07fc03f6b2d8a25d7abf70b3c49`; `test-fixtures`, `architecture-rules` 두 module | `provider-conformance-tests`는 여전히 없음 |
| Fixture scaffold | `build/test-fixtures`가 `src/test/java`를 `tests` classifier test-jar로 attach | Phase 00 승인 뒤에만 Phase 12가 소비할 수 있는 후보; `src/testFixtures/java`의 자동 인식 근거가 아님 |
| Toolchain | `./mvnw -version`: Maven 3.9.14, Java 25.0.3 | Toolchain inventory일 뿐 Phase 12 evidence 아님 |
| Stable RPDPTW source/test | main Java 23개 모두 `package-info.java`; test Java 0 | Phase 08~12 semantic implementation 없음 |
| Build test source | Java 11개(그중 `package-info.java` 1개), generated Surefire XML 존재 | Phase 00 scaffold evidence 후보일 뿐 Phase 12 report가 아니며 stale mixing 금지 |
| Provider tree | `adapters/` regular file 0, `distributions/` 부재 | Candidate/axis/module 미선정·미구현 |
| Integration lifecycle | Root/build POM에 `maven-failsafe-plugin`, `provider-integration`, `provider-parity` 없음 | `*IT` 실행·0-test failure가 lifecycle에 연결되지 않음 |
| Phase 12 named production type | `ProviderConformanceSubject`, `RunStateRepository`, `Phase12AcceptanceReceipt` 등 0 | `NOT_IMPLEMENTED` |
| Current maps | implementation progress SHA-256 `5696065eb923c50c00e6fa7d5ed2e13c30894e77fcc7a8009fcd36b5d2d8e7cd`; human progress SHA-256 `ea79cc6bf101c5dfeafe9b67455f0f2c7bb21620e6624afa7ee6c51987848e11` | Phase 00 review 02와 Phase 12 correction 진행을 기록하지만 acceptance를 부여하지 않음 |

Original snapshot, authority commit와 correction-time live observation 중 어느 것도
다른 것을 덮어쓰지 않는다. Live drift가 accepted Phase 00 receipt를 얻기 전에는
아래 Maven tree 전체가 `PROPOSED/BLOCKED_PENDING_BUILD_CONTRACT`다.

### 5.3 존재, placeholder, 부재를 정확히 표현하기

| 표현 | 사용 조건 | 예 |
|---|---|---|
| `존재` | File/type가 실제 snapshot에 있음 | `rpdptw/application/pom.xml` |
| `placeholder` | Package/POM 경계만 있고 semantic 구현은 없음 | `application/port/out/package-info.java` |
| `legacy characterization` | 과거 동작을 보존·검사할 뿐 target authority가 아님 | `legacy/gcp-placeholder` |
| `proposed` | 문서가 후보 이름/경로를 제시 | `ProviderConformanceSubject` |
| `future conditional` | 승인·predecessor 뒤 생성 가능 | `adapters/object-<approved-provider>` |
| `부재` | Actual file/type/test/evidence가 없음 | Phase 12 adapter, conformance suite |

다음 문장을 쓰지 않는다.

```text
“application module이 있으므로 port가 구현됐다”
“GCP legacy test가 있으므로 candidate parity가 있다”
“architecture rule이 있으므로 provider leakage가 0으로 승인됐다”
“wrapper가 생겼으므로 future Phase 12 명령이 실행 가능하다”
```

### 5.4 Read-only inventory 명령

```bash
git rev-parse HEAD
git status --short --branch

git ls-tree -r --name-only HEAD |
  rg '(^pom.xml$|^src/|^gcp/|^build/|^rpdptw/|^adapters/|^distributions/)'

rg --files \
  -g 'pom.xml' -g '*.java' -g 'mvnw*' \
  -g 'rpdptw/**' -g 'build/**' -g 'legacy/**' \
  -g 'adapters/**' -g 'apps/**' -g 'distributions/**' -g 'deployment/**' |
  sort

rg -n 'software\.amazon|com\.google\.cloud|com\.azure|io\.kubernetes' \
  pom.xml rpdptw build adapters apps distributions deployment
```

결과가 guide snapshot과 달라도 concurrent file을 삭제·이동·수정하지 않는다. 변경된
경로와 blob을 blocker note에 기록하고 source semantics가 달라졌을 때만 guide review를
재개한다.

## 6. Scope, non-scope, 결정 상태와 사람 승인

### 6.1 포함 범위

- 승인된 provider와 최소 substitution axis를 immutable decision으로 고정한다.
- Provider-neutral storage/workflow/worker/application contract catalog를 만든다.
- AWS와 candidate가 공유하는 단일 conformance suite와 독립 oracle을 만든다.
- Candidate adapter/distribution을 승인된 축에 한해 격리한다.
- Capability, CAS/consistency, failure, retry, deadline, codec/replay 차이를 판정한다.
- IAM-equivalent least privilege, encryption, tenant, redaction과 audit을 비교한다.
- Provider observation과 semantic identity를 분리한다.
- Exact declared closure의 digest-preserving copy, shadow와 rollback을 rehearsal한다.
- 동일 workload의 performance/cost vector를 승인된 policy 아래 기록한다.
- Standalone evidence → conformance manifest → review → receipt DAG를 봉인한다.

### 6.2 명시적 비범위

- 승인 없는 GCP/ECS/Kubernetes/Azure/provider 선택이나 account/project/cluster 생성
- Production traffic, pointer, endpoint, default provider 또는 AWS reference role 변경
- Core/domain/solver/verifier/application contract를 candidate 제약에 맞춰 완화
- Database, queue, prefix listing 또는 event log를 새 authority로 도입
- Public API/wire schema를 Phase 12 후보 type에서 확정
- Phase 13 route pool/MIP/OR-Tools dependency·native packaging·hybrid 구현
- Phase 14A benchmark 수치, 14B official manifest/calibration/cutover 권한
- `Q-BENCH-02`, `C-17`, `Q-VAR-01`을 test default로 해소
- Legacy GCP를 target candidate evidence로 승격

### 6.3 확정, proposed, open, gated, deferred

| 항목 | 상태 | 구현자 행동 |
|---|---|---|
| AWS S3 + Step Functions + Lambda target/reference | `RESOLVED` | Reference subject로만 사용; sole oracle/default로 승격 금지 |
| Provider-neutral semantic boundary | 확정 불변조건 | Candidate 때문에 변경 금지 |
| Candidate provider | `NOT_SELECTED` | 승인 record 전 module도 만들지 않음 |
| Substitution axis | `NOT_SELECTED` | 최소 축이 승인될 때까지 후보 비교만 문서화 |
| Java type/package 이름 | `PROPOSED INTERNAL` | Accepted Phase 08~11 signature에 맞춰 조정 |
| Retry/deadline/performance/cost 수치 | `OPEN/GATED` | `TEST_ONLY` explicit 값만 사용 |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Official 값 발명 금지 |
| Phase 13 | `C-17 GATED`, `GATED_NOT_STARTED` | Phase 14A receipt와 별도 승인 전 구현·load 금지 |
| `Q-VAR-01` | `DEFERRED` | 질문·구현·invariant 완화 금지 |
| Phase 14A | `NOT_RUN` | Phase 12가 acceptance를 만들지 않음 |
| Phase 14B/production | `NOT_STARTED/NOT_GRANTED` | Cutover 명령·authority 생성 금지 |

### 6.4 사람 승인이 필요한 결정과 마지막 안전 지점

| 사람 checkpoint | 승인자 | 승인 전 마지막 안전 지점 | Resume evidence |
|---|---|---|---|
| Phase 08~11 cross-phase signatures | Application/Storage/Coordinator/AWS owners | Contract catalog/red tests | Accepted exact port/state/failure/evidence refs |
| Candidate/axis/scope | Product/Platform/Ops | Provider-neutral suite만 | `ProviderAdoptionDecision` equivalent |
| Non-prod environment | Platform/Security/FinOps | Offline fake | Scoped credential, budget, cleanup owner |
| Required capability policy | Contract owners | Capability probe spec | Required set와 unsupported disposition |
| Authorization/failure carrier | Security/Application/Storage | Backend call 0 | Non-ambient binding + exhaustive negative evidence |
| Deadline/restart | Reliability/Ops/Coordinator | Virtual clock test-only | Durable ADR + crash/re-entry tests |
| Security/retention/network | Security/Data Governance | No deploy | Approved control profile |
| Performance/cost envelope | Performance/FinOps/Ops | Measurement schema | Workload/threshold/repetition approval |
| Migration/shadow | Storage/Ops/DR | Source refs/pointers unchanged | Scope, copy plan, cleanup/rollback approval |
| Adoption/production | Product/Release/Security/Ops | Recommendation only | 별도 Phase 14 authority |

승인을 기다리는 동안 adapter skeleton을 만들어 “진행”으로 보이게 하지 않는다.
Required capability가 실제 provider에서 unsupported이면 `RejectedUnsupported`가 정상
종료다. Port를 약화해서 green으로 만들지 않는다.

## 7. 학습 경로 — 개념 → 작은 탐색 → 실제 변경 → 통합

### 7.1 1단계 — 개념을 말로 설명하기

다음 문장을 코드 없이 설명할 수 있어야 한다.

1. AWS와 candidate가 같다는 사실이 왜 correctness oracle이 아닌가?
2. Run-state CAS와 publication-pointer CAS가 왜 다른가?
3. Provider locator가 바뀌어도 artifact identity가 같은 이유는 무엇인가?
4. Retry에서 바뀌어도 되는 ID와 바뀌면 안 되는 ID는 무엇인가?
5. `UNSUPPORTED`, `GATED`, `INDETERMINATE`, `FAIL`은 어떻게 다른가?
6. Shadow run이 production canary가 아닌 이유는 무엇인가?
7. Phase 12 evidence가 Phase 13/14 authority가 아닌 이유는 무엇인가?

**완료 신호:** 동료가 provider 제품명을 하나도 사용하지 않고 위 답을 다시 설명할
수 있다.

### 7.2 2단계 — 작은 탐색

종이 또는 test fixture로 다음을 손으로 계산한다.

- 두 writer가 같은 state version으로 다른 next state를 CAS할 때 승자는 하나다.
- Same artifact key/same digest 재시도는 수렴하고 different digest는 conflict다.
- Cancel intent와 publish intent가 같은 run-state version에서 경쟁한다.
- Worker completion 순서를 뒤집어도 semantic replay digest는 같다.
- Provider URI만 바꿔도 semantic artifact fingerprint는 같다.
- Candidate가 한 required case를 skip하면 parity는 pass가 아니다.
- AWS와 candidate가 같은 잘못된 result를 만들면 independent oracle이 둘 다 거부한다.

**완료 신호:** Expected result를 AWS log나 candidate SDK 응답 없이 hand oracle로
도출한다.

### 7.3 3단계 — 실제 변경을 최소 순서로 만들기

Gate가 열린 뒤에도 첫 변경은 candidate SDK가 아니다.

```text
accepted contract snapshot
→ provider-neutral case catalog
→ independent oracle + deterministic fake
→ pre-sealed applicability config
→ candidate construction denial test
→ approved minimal adapter module
→ capability probe
→ applicable contract implementation
```

**완료 신호:** SDK dependency를 삭제해도 catalog, fake, oracle, config와 negative
construction test가 통과한다.

### 7.4 4단계 — 통합하며 의미를 보존하기

통합은 happy path부터 끝까지 한 번 잇는 것이 아니다. 각 vertical slice는 아래를
함께 가진다.

```text
positive case
+ duplicate/lost-ack case
+ authorization negative
+ corruption/failure case
+ immutable evidence
+ rollback/last safe point
```

Storage, workflow, compute axis를 한 번에 합치지 않는다. 한 axis의 evidence가 완전한
뒤 다음 applicable axis로 이동한다.

**완료 신호:** Provider-specific setup만 교체해 같은 case ID와 oracle을 AWS와
candidate에 parameterized 실행할 수 있다.

### 7.5 단계별 자문 질문

| 단계 | 자문 질문 |
|---|---|
| 개념 | 내가 provider product 기능을 semantic requirement와 혼동했는가? |
| 탐색 | Expected value가 subject output을 복사한 것은 아닌가? |
| 변경 | 승인되지 않은 provider/axis/module을 먼저 만들었는가? |
| 통합 | 한 provider에서만 skip/expected-failure가 있는가? |
| Evidence | Review 결과를 evidence manifest에 backfill했는가? |
| Handoff | Recommendation을 production authority처럼 표현했는가? |

## 8. 목표 module, package, file과 dependency

### 8.1 승인 뒤에만 가능한 change tree

아래 이름은 `PROPOSED`. `<approved-*>`는 literal directory가 아니다.

```text
build/
├── test-fixtures/                         # Phase 00 accepted test-jar일 때만 재사용
│   └── src/test/java/com/ronext/rpdptw/fixture/
└── provider-conformance-tests/
    ├── pom.xml
    ├── src/main/java/com/ronext/rpdptw/conformance/
    ├── src/test/java/com/ronext/rpdptw/conformance/fixture/
    ├── src/test/java/com/ronext/rpdptw/conformance/oracle/
    ├── src/test/java/com/ronext/rpdptw/conformance/comparison/
    └── src/test/resources/phase12-canonical-oracle-manifest-v1.txt

adapters/
├── pom.xml                                # 승인된 module만 aggregate
├── object-<approved-provider>/            # STORAGE 승인 때만
├── workflow-<approved-provider>/          # DURABLE_WORKFLOW 승인 때만
└── compute-<approved-provider>/           # WORKER_COMPUTE 승인 때만

distributions/
├── pom.xml                                # 승인된 distribution만 aggregate
└── <approved-distribution>/               # DISTRIBUTION_MAPPING 승인 때만

deployment/
└── <approved-provider>/                   # reviewed source IaC only
```

예상 owner package:

| 책임 | Proposed module/package | 금지 |
|---|---|---|
| Conformance case/catalog | `build/provider-conformance-tests/...conformance` | Provider SDK import |
| Independent oracle | same module의 `...oracle` | Concrete exception/DTO 의존 |
| Cross-phase common fixture | Accepted `build/test-fixtures`의 `tests` classifier test-jar | `src/testFixtures/java`를 Maven 기본 source로 가정 |
| Phase 12 fixture/builder | Conformance module의 `src/test/java/...fixture` | Production credential/default |
| Storage adapter | `adapters/object-*` | Core schema/identity 변경 |
| Workflow adapter | `adapters/workflow-*` | Comparator/completeness 계산 |
| Compute adapter | `adapters/compute-*` | Seed/work 재작성 |
| Assembly/config | `distributions/*` | Hidden provider/default axis |
| Source IaC | `deployment/*` | Generated console output을 source로 복사 |

### 8.2 Compile dependency

```text
rpdptw-core / solver / verification
              ↓
        rpdptw-application
              ↓
     provider-neutral ports
          ↙          ↘
 object-common   provider-conformance-tests
          ↓             ↑
 approved adapters ─────┘
          ↓
 distribution/deployment
```

금지 edge:

```text
core|solver|verification|application → provider SDK
core|solver|verification             → conformance module
AWS adapter                          → candidate adapter
candidate adapter                    → AWS adapter
independent oracle                   → provider exception/DTO
Phase 12                             → Phase 13 hybrid/backend/vendor module
```

Conformance test module이 두 adapter를 test runtime에 조립하는 것은 허용될 수 있지만,
두 adapter가 서로 compile-depend하면 안 된다.

#### Maven reactor, fixture, profile과 lifecycle 계약

이 wiring은 **`PROPOSED/BLOCKED_PENDING_PHASE00_BUILD_CONTRACT`**다. Phase 00
Build owner가 plugin/version과 fixture publication을 승인한 receipt 없이 POM을
만들거나 current scaffold를 accepted로 간주하지 않는다. 승인 뒤에는 다음 연결이
모두 한 변경 단위에 있어야 한다.

| 연결 | Exact future contract | 실패 판정 |
|---|---|---|
| Root aggregation | Root `<modules>`가 `build`, 승인된 경우에만 `adapters`와 `distributions` aggregator를 포함 | Reactor manifest에 expected module 하나라도 없으면 build fail |
| Build aggregation | `build/pom.xml`이 `test-fixtures` 뒤에 `provider-conformance-tests`를 module로 등록 | Direct `-f` 성공만 있고 full reactor에 없으면 fail |
| Fixture source | Accepted `build/test-fixtures/src/test/java`를 `maven-jar-plugin:test-jar`로 attach하고 consumer가 `type=test-jar`, `classifier=tests`, `scope=test`로 의존 | `src/testFixtures/java` 자동 compile 가정, source 복사 또는 provider별 fixture fork는 fail |
| Neutral dependencies | Conformance module은 application/verification/accepted port artifact와 test-fixture test-jar에만 compile/test 의존 | Core/solver/provider SDK의 역방향 edge나 Phase 13/vendor dependency는 fail |
| Provider assembly | `provider-integration`/`provider-parity` profile이 exact approved AWS/candidate adapter·distribution test-runtime dependency와 explicit decision/environment refs를 추가 | Default profile, ambient provider 또는 미승인 artifact가 resolve되면 fail |
| Unit lifecycle | Surefire는 `*Test`만 포함하고 `*IT`를 제외하며 module-local `failIfNoTests=true`; selected run은 `surefire.failIfNoSpecifiedTests=true` | Unit discovered 0 또는 selected method 누락은 fail |
| Integration lifecycle | Pinned `maven-failsafe-plugin`을 conformance module `<build><plugins>`에 선언하고 `integration-test`와 `verify` goal을 두 profile에 bind; `*IT` include와 `failIfNoTests=true` | PluginManagement에만 있거나 goal/profile이 bind되지 않으면 fail |
| Report location | Unit은 해당 module `target/surefire-reports`, integration은 `target/failsafe-reports`; 두 directory는 run 시작 전 `clean`으로 제거 | 다른 module/이전 run XML을 읽으면 fail |
| Verify-phase closure | Owner-approved verify-phase report verifier가 canonical oracle manifest와 fresh XML을 reconcile하고 execution manifest를 seal | Expected/discovered/executed/pass/fail/error/skip/missing/unexpected/duplicate 불일치면 Maven `verify` fail |

`build-helper-maven-plugin:add-test-source` 방식은 이 guide의 선택이 아니다. Phase 00
owner가 test-jar 방식을 거부하고 pinned additional-source 방식을 별도 승인하면 그
plugin/version/source root/consumer와 동등한 zero-test oracle을 새 compatibility
receipt로 교체해야 한다. 두 방식을 동시에 활성화해 test를 중복 발견해서도 안 된다.

### 8.3 File 배치 자문

새 type을 만들기 전에 묻는다.

1. Provider가 없어도 의미가 성립하는가? 그러면 accepted application/common contract
   owner를 찾는다.
2. 특정 SDK request/response/exception을 아는가? 그러면 candidate adapter 내부다.
3. 두 provider의 결과를 비교하는가? Conformance module이 소유한다.
4. Route/score/verifier 결과를 계산하는가? Phase 12에 두지 않는다.
5. Credential, resource name, region을 담는가? Adapter/distribution config이며 semantic
   fingerprint에서 제외한다.
6. Production traffic을 바꾸는가? Phase 12 scope 밖이다.

## 9. PROPOSED Java 계약, 상태 전이와 pseudocode

> 이 절은 완성 코드를 복사하라는 뜻이 아니다. 이름과 signature는 accepted
> Phase 08~11 contract에 맞춰 바뀔 수 있다. 구현자는 각 type이 어떤 잘못된 상태를
> 표현 불가능하게 만드는지 먼저 설명한 뒤 최소 형태로 작성한다.

### 9.1 Adoption과 axis 후보

```java
// PROPOSED INTERNAL
public enum SubstitutionAxis {
    STORAGE,
    DURABLE_WORKFLOW,
    WORKER_COMPUTE,
    DISTRIBUTION_MAPPING
}

public record ProviderAdoptionDecision(
        DecisionId decisionId,
        CandidateProviderId providerId,
        Set<SubstitutionAxis> approvedAxes,
        DeploymentScope nonProductionScope,
        Set<RequiredCapabilityId> requiredCapabilities,
        SecurityControlProfileId securityProfile,
        PerformanceEnvelopePolicyRef performancePolicy,
        CostEvidencePolicyRef costPolicy,
        ReviewBoundary reviewBoundary,
        ApprovalEvidenceRef approvalEvidence) {
}
```

Provider ID, 실제 axis, region/account, threshold와 review 날짜는 모두 `OPEN`이다.
Empty axes, production scope, expired approval은 subject construction 전에 거부한다.

### 9.2 Capability를 boolean으로 축약하지 않기

```java
// PROPOSED INTERNAL
public sealed interface ProviderCapabilityDecision
        permits Supported, Unsupported, Gated, Indeterminate {

    RequiredCapabilityId capability();
}

public record Supported(
        RequiredCapabilityId capability,
        CapabilityEvidenceRef evidence)
        implements ProviderCapabilityDecision {
}

public record Unsupported(
        RequiredCapabilityId capability,
        UnsupportedReasonCode reason)
        implements ProviderCapabilityDecision {
}

public record Gated(
        RequiredCapabilityId capability,
        ApprovalRequirementRef requirement)
        implements ProviderCapabilityDecision {
}

public record Indeterminate(
        RequiredCapabilityId capability,
        MissingEvidenceRef missingEvidence)
        implements ProviderCapabilityDecision {
}
```

Required capability 후보:

```text
IMMUTABLE_PUT_IF_ABSENT
EXACT_VERIFIED_READ
ATOMIC_COMPARE_AND_SET
ATOMIC_PUBLICATION_FENCE
EXACT_READ_AFTER_COMMITTED_WRITE
DURABLE_WORKFLOW_REENTRY
IDEMPOTENT_WORKFLOW_START
IDEMPOTENT_WORKER_DISPATCH
ATTEMPT_PRESERVING_RETRY
COOPERATIVE_STOP_AND_CANCEL
DECLARED_COMPLETENESS
TENANT_SCOPED_AUTHORIZATION
NON_AMBIENT_TENANT_AUTHORIZATION
LOSSLESS_OPERATION_FAILURE_MAPPING
DISTINCT_RUN_STATE_AND_PUBLICATION_PRECONDITIONS
SAME_RUN_STATE_CANCEL_PUBLICATION_FENCE
RESTART_SAFE_DURABLE_DEADLINE
ENCRYPTION_IN_TRANSIT
ENCRYPTION_AT_REST
STRUCTURED_CORRELATED_TELEMETRY
BOUNDED_RETRY_AND_DEADLINE
CANONICAL_SERIALIZATION_REPLAY
```

이 목록은 예시 15개가 아니라 canonical Phase 12 §6.1의 **최소 22개 exact
catalog**다. Accepted cross-phase compatibility receipt 없이 이름을 합치거나 빼지
않는다.

Capability는 두 단계로 판정한다.

1. Provider 실행 전에 `ProviderCapabilityApplicabilityManifest`가 canonical 22개 각각을
   `REQUIRED`, `NOT_APPLICABLE_PRESEALED_WITH_APPROVAL` 또는
   `BLOCKED_PENDING_ACCEPTED_CONTRACT` 중 하나로 seal한다.
2. `REQUIRED`인 행만 actual provider probe가 `SUPPORTED`, `UNSUPPORTED`, `GATED`,
   `INDETERMINATE` 중 하나로 판정한다. `NOT_APPLICABLE`은 provider 결과를 본 뒤
   선택할 수 없다.

각 applicability 행에는 다음 값이 모두 있어야 한다.

```text
capabilityId
approvedAxes[]
applicabilityDisposition
decisionOwner
acceptedSourceContractRef
requiredCaseIds[]
negativeControlIds[]
actualProviderEvidenceSchemaRef
approvalOrBlockerRef
```

`ProviderAdoptionDecision.requiredCapabilities`는 위 manifest에서 `REQUIRED`로 seal된
행의 exact 집합과 같아야 한다. Canonical ID 22개 기준으로 missing/duplicate/unknown
행, owner/source/test/evidence가 비어 있는 행, unapproved default가 각각 0이어야
subject construction이 가능하다.

Review에서 빠졌던 7개는 다음 owner/evidence/negative control을 최소 연결로 가진다.
`Evidence`는 계획된 schema이며 현재 evidence가 있다는 뜻이 아니다.

| 복원 capability | Applicability와 decision owner | Actual evidence | Required negative control |
|---|---|---|---|
| `EXACT_READ_AFTER_COMMITTED_WRITE` | `STORAGE`이면 `REQUIRED`; Phase 09 Storage owner + Phase 12 adoption owner | Committed write receipt 뒤 exact key read의 kind/schema/length/digest와 visibility trace | False missing, stale bytes 또는 list-based discovery를 success로 반환하면 fail |
| `COOPERATIVE_STOP_AND_CANCEL` | `DURABLE_WORKFLOW` 또는 `WORKER_COMPUTE`이면 `REQUIRED`; Phase 10 Coordinator + Phase 11/runtime owner | Stop intent, provider delivery, actual termination, same-state cancel/publication race trace | Stop intent를 terminal로 간주하거나 cancel과 publish가 둘 다 authoritative success면 fail |
| `TENANT_SCOPED_AUTHORIZATION` | 모든 data/control-plane axis에서 `REQUIRED`; Phase 08/09 Application/Security/Data Governance | Role×operation×tenant provider-native allow/deny matrix와 no-disclosure trace | Application tenant string check만 있거나 cross-tenant existence/backend call이 하나라도 있으면 fail |
| `DISTINCT_RUN_STATE_AND_PUBLICATION_PRECONDITIONS` | `STORAGE`/`DURABLE_WORKFLOW`이면 `REQUIRED`; Phase 09/10/11 Storage/Coordinator owners | Compile/type separation, independent state/pointer CAS, stale/lost-ack reconciliation | `StateVersion` cast/reuse로 publication을 시도하거나 한 token이 두 pointer를 전진시키면 fail |
| `ENCRYPTION_IN_TRANSIT` | Actual provider axis 전부 `REQUIRED`; Platform Security + Data Governance | Approved endpoint/TLS policy, handshake/config evidence와 content-digest 독립 assertion | Plaintext/downgrade/default endpoint가 허용되거나 TLS가 digest를 대신하면 fail |
| `ENCRYPTION_AT_REST` | Artifact/state를 보유하는 axis에서 `REQUIRED`; Platform Security + Data Governance | Approved key/classification/policy/rotation-revocation evidence와 exact stored-object assertion | Unencrypted/default unmanaged key가 허용되거나 encryption metadata가 integrity를 대신하면 fail |
| `BOUNDED_RETRY_AND_DEADLINE` | Provider operation을 수행하는 모든 axis에서 `REQUIRED`; Phase 08/10/11 + SRE | Attempt/call bound, typed exhaustion, durable restart reconciliation, final verification/publication reserve | Unbounded retry, wall-clock/provider-time hidden fallback, reserve 소진 뒤 false success면 fail |

`UNSUPPORTED`는 안전한 adoption rejection일 수 있지만 eligible/pass는 아니다.
`GATED`/`INDETERMINATE`/`BLOCKED_PENDING_ACCEPTED_CONTRACT`가 한 행이라도 있으면
full-suite와 adoption eligibility는 false다. N/A는 approved axis와 source contract가
실행 전에 증명한 경우만 허용하며, owner 승인 ref 없는 N/A는 missing으로 센다.

### 9.3 Conformance subject와 port 후보

```java
// PROPOSED INTERNAL; exact port types are cross-phase gated.
public interface ProviderConformanceSubject extends AutoCloseable {
    ProviderIdentity identity();
    ProviderCapabilityReport probeCapabilities();
    ArtifactStore artifactStore();
    RunStateRepository runStateRepository();
    ResultPublisher resultPublisher();
    WorkerDispatcher workerDispatcher();
    WorkflowExecutionPort workflowExecutionPort();
    CancellationPort cancellationPort();
    TelemetryPort telemetryPort();
    ProviderObservationReader observations();
}

public record ProviderIdentity(
        ProviderId providerId,
        ProviderRole role,
        ProviderAdapterVersion adapterVersion,
        DistributionArtifactDigest distributionDigest,
        ProviderEnvironmentClass environmentClass) {
}

public enum ProviderRole {
    AWS_REFERENCE,
    CANDIDATE
}
```

Construction은 explicit non-ambient authorization binding과 environment scope를
받아야 한다. Process default credential, global provider, thread-local tenant를
읽지 않는다.

Storage/application signature의 의미 후보:

```java
public interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
            ArtifactKey key,
            ArtifactContent content,
            ContentDigest expectedDigest);

    ReadableArtifact readVerified(ArtifactRef reference);
    ArtifactMetadata metadata(ArtifactRef reference);
}

public interface RunStateRepository {
    CreateStateResult createIfAbsent(SolveId solveId, RunState initialState);
    VersionedRunState get(SolveId solveId);
    StateUpdateResult compareAndSet(
            SolveId solveId,
            StateVersion expectedVersion,
            RunState nextState);
}

public interface ResultPublisher {
    VersionedPublishedResult get(SolveId solveId);
    PublicationResult compareAndSet(
            SolveId solveId,
            PublicationPrecondition expectedPublication,
            PublishableResultRef result);
}
```

`StateVersion`과 `PublicationPrecondition`은 서로 대입 가능하게 만들지 않는다.
Exact failure carrier와 access-binding parameter는 Phase 08~10 owner 승인 전
overload나 ambient context로 대신하지 않는다.

`createIfAbsent` 이름과 위 signature도 **canonical projection을 복원한
`PROPOSED/CROSS_PHASE_BLOCKED`**다. 정확한 public signature는 Phase 08~10 owner
승인 전 확정하지 않지만, 다음 semantic contract는 adapter가 임의로 생략할 수 없다.

```text
SubmissionIdentity
  tenantId
  submissionId
  canonicalInputDigest
  preparedTravelDigest
  boundProfileDigest
  algorithmConfigDigest
  executionManifestDigest

InitialRunStateBinding
  submissionIdentity
  solveId
  initialState = SUBMITTED
  initialStateVersion
```

- 같은 tenant의 `SubmissionId`와 같은 canonical digest tuple은 같은
  `SolveId`/`SUBMITTED` state/version으로 수렴한다.
- 같은 tenant의 `SubmissionId`와 다른 digest tuple은
  `IDEMPOTENCY_CONFLICT`이며 기존 binding/state를 바꾸지 않는다.
- 최초 생성의 authority key는 tenant-scoped submission identity다. 서로 다른
  `SolveId` 두 개를 먼저 만든 뒤 list/index로 고르는 구현은 금지한다.
- Submission binding과 visible initial state는 하나의 atomic semantic transition이다.
  Backend가 두 객체 transaction을 제공하지 않으면 accepted deterministic identity
  derivation 또는 single authoritative record 설계가 필요하다. Check-then-put,
  hidden mutable index와 orphan state는 대안이 아니다.
- Concurrent same/same create는 exactly one create winner와 동일한 existing result로
  수렴한다. Concurrent same/different는 한 binding만 남기고 다른 요청을 conflict로
  거부한다.
- Commit acknowledgement를 잃으면 tenant-scoped submission exact read로 identity와
  state를 reconcile한다. Blind create, unconditional put 또는 state reset을 하지 않는다.
- Crash가 authoritative commit 전이면 binding/state 둘 다 보이지 않고, commit 뒤면
  둘 다 exact read 가능해야 한다. Partial visibility는
  `VisibilityIndeterminate`이며 새 workflow dispatch/publication을 막는다.
- Authorization은 존재 확인보다 먼저 수행한다. Denied/missing/conflict/indeterminate를
  같은 not-found나 generic retry로 축약하지 않는다.
- `createIfAbsent`는 `ABSENT → SUBMITTED`만 소유한다. 이후 transition은 state CAS,
  result publication은 distinct publication CAS가 소유한다.

### 9.4 Failure 후보

```java
public sealed interface ProviderOperationFailure
        permits TransientUnavailable,
                Throttled,
                AccessDenied,
                AuthenticationFailed,
                ConditionalConflict,
                VisibilityIndeterminate,
                IntegrityViolation,
                UnsupportedCapability,
                QuotaOrCapacityExceeded,
                PlatformTimeout,
                CancellationObserved,
                ProviderProtocolViolation {

    ProviderFailureCode code();
    RetryDisposition retryDisposition();
    SafeFailureDetail safeDetail();
    ProviderObservationRef observation();
}
```

Denied, missing, corrupt, stale, conflict, indeterminate를 `AdapterUnavailable`,
empty result 또는 generic exception 하나로 축소하지 않는다. Unknown SDK status는
`ProviderProtocolViolation`으로 fail closed한다.

### 9.5 Case, result와 comparison 후보

```java
public interface ProviderConformanceCase {
    ConformanceCaseId id();
    Set<RequiredCapabilityId> requiredCapabilities();
    ConformanceCaseResult execute(
            ProviderConformanceSubject subject,
            ConformanceFixture fixture,
            ConformanceOracle oracle);
}

public enum ConformanceDisposition {
    PASS,
    FAIL,
    UNSUPPORTED,
    GATED,
    NOT_RUN
}

public record ProviderEvidenceComparison(
        ProviderConformanceApplicabilityConfig applicabilityConfig,
        EvidenceContentDigest awsReferenceEvidenceDigest,
        EvidenceContentDigest candidateProviderEvidenceDigest,
        List<ContractComparison> contractComparisons,
        List<ProviderSemanticDifference> differences,
        SecurityControlComparison security,
        ObservabilityComparison observability,
        PerformanceEnvelopeComparison performance,
        MigrationComparison migration,
        AdoptionRecommendation recommendation,
        boolean productionAuthority) {
}
```

`productionAuthority`는 Phase 12에서 항상 `false`다. Constructor/factory invariant로
강제하고 serialization test에서 `true`를 거부한다.

### 9.6 State transition

Provider event state가 아니라 application state를 비교한다.

```text
ABSENT
→ SUBMITTED  # tenant-scoped submission create-if-absent, exactly once

SUBMITTED
→ PREPARING
→ PREPARED
→ ROUND_DISPATCHING
→ ROUND_RUNNING
→ ROUND_VERIFYING
→ ROUND_AGGREGATING
→ ROUND_DISPATCHING | FINALIZING
→ PUBLISHING
→ SUCCEEDED
```

Exceptional terminal은 구분한다.

```text
CANCEL_REQUESTED
WATCHDOG_REACHED
RESOURCE_LIMIT_REACHED
PLATFORM_TIMEOUT
FAILED
INCOMPLETE
PUBLICATION_REJECTED
```

Late provider event는 terminal을 되돌리지 않고 no-op/reconciliation evidence가 된다.
Create retry도 `SUBMITTED` 이후 state를 초기값으로 되돌리지 않는다.

### 9.7 Execution pseudocode

```text
createOrReconcileInitialState(access, submissionIdentity, proposedSolveId):
  authorize tenant-scoped CREATE_STATE before lookup
  outcome = atomic create-if-absent(submissionIdentity, proposedSolveId, SUBMITTED)
  if CREATED:
    exact-read submission binding and state; require full identity equality
  if EXISTING_SAME:
    exact-read and return the existing SolveId/state/version
  if EXISTING_DIFFERENT:
    return IDEMPOTENCY_CONFLICT without mutation or workflow start
  if ACK_LOST or VISIBILITY_INDETERMINATE:
    exact-read the submission authority key within bounded policy
    converge only on full identity equality; otherwise fail closed
  never list, blind-write, reset state, or dispatch before reconciliation
```

```text
prepareApplicability(decision, acceptedContracts):
  require provider, axes, non-prod scope and approvals
  enumerate all 22 canonical capabilities
  require exactly one pre-sealed applicability row, owner, source, cases,
          negative controls and evidence schema for every capability
  derive decision.requiredCapabilities from REQUIRED rows; no manual subset
  resolve exact case catalog from accepted contract versions
  bind oracle version, runtime config and metadata-exclusion projection
  seal config before either provider run
  return immutable config digest
```

```text
executeCase(case, subject, config):
  require case.id is pre-sealed applicable
  fixture = exact verified read
  before = independent oracle captures authoritative state
  actual = case executes one declared operation/fault
  after = independent oracle captures authoritative state
  semantic = oracle recomputes neutral projection
  observation = subject reads safe provider metadata projection
  return immutable case result
```

```text
compareProviders(config, awsEvidence, candidateEvidence):
  require both evidence bind the same config digest
  require applicable skipped case count == 0
  require each provider independently passes the same oracle
  compare semantic identity/artifact/state/failure/termination projections
  keep provider metadata outside semantic fingerprint
  evaluate security/operations and approved performance policy
  return recommendation with productionAuthority = false
```

### 9.8 Migration pseudocode

```text
migrateDeclaredClosure(sourceRootRef, destination):
  closure = traverse exact declared refs in stable order
  for each ref:
    verify source kind/schema/length/digest/authority
    destination put-if-absent exact logical key
    exact read-back and verify
    seal ArtifactCopyReceipt
  require receipt for every declared ref
  run shadow with publication/state/traffic authority = false
  on failure:
    stop
    preserve source refs and pointers
    quarantine conflicting destination object
```

Prefix list를 closure authority로 쓰지 않는다. Same logical key/different digest는
overwrite가 아니라 integrity conflict다.

## 10. 순서 있는 work package

모든 WP는 canonical Phase 12의 WP12-0~9와 같은 순서를 사용한다. Path/type은
`PROPOSED`, Maven command는 §12의 조건을 충족한 뒤 실행하는 `FUTURE`다. Gate가
닫힌 현재 허용되는 범위는 WP12-0, WP12-1의 read-only review와 red-test 명세까지다.

### WP12-0 — Entry receipt와 source drift freeze

**목적과 필요한 이유**

구현자가 서로 다른 source version, 미승인 predecessor, 임의 provider를 섞어
시작하지 못하게 한다. “어떤 계약과 authority로 시작했는가”가 없으면 이후 parity가
무엇을 비교한 것인지 재현할 수 없다.

**사전조건**

- 문서 읽기 권한만 필요하다.
- Candidate implementation/adoption 권한은 필요하지 않다.
- HEAD와 live drift를 구분할 수 있어야 한다.

**예상 file/package/type**

- `ProviderConformanceApplicabilityConfig`가 아니라 먼저 `Phase12EntryReceipt`
- Source fingerprint verifier 또는 build evidence helper
- `Phase12EntryReceiptTest`
- 실제 위치는 accepted build/evidence owner가 결정하며 현재 생성하지 않는다.

**구체 행동**

1. §3.2의 HEAD blob을 재현한다.
2. Phase 08~11 exact accepted review/evidence/receipt를 확인한다.
3. Candidate provider, axes, non-prod scope, owner, expiry/review boundary를 확인한다.
4. Security/Ops/performance policy ref와 cross-phase blocker resolution ref를 확인한다.
5. Actual Phase 13/14 상태를 기록하되 Phase 12 entry로 역전하지 않는다.
6. 누락 항목을 `GATED` receipt로 기록한다.
7. Source drift가 있으면 affected heading→requirement→test 영향만 review한다.

**근거**

- [Canonical Phase 12 §4](../../phases/phase-12-provider-substitution.md#4-entry-gate와-evidence-receipt)
- [Review F-P12-006](../../reviews/phase-12-review.md#f-p12-006--stale-adjacent-fingerprint가-predecessor-acceptance에-들어-있었다)
- [Master Plan §9](../../master-realization-plan.md#9-evidence-bundle-규칙)

**금지 shortcut**

- 인접 Phase whole-file hash 일치만으로 accepted handoff라고 판단
- Console screenshot이나 generated deployment output을 evidence로 사용
- Candidate 이름만 있고 axes/scope가 없는 decision을 승인으로 처리
- Scheduler task/status를 구현자가 직접 승격

**검증 명령과 test — FUTURE**

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/implementation/phases/phase-12-provider-substitution.md

./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=Phase12EntryReceiptTest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

Test 후보:

```text
Phase12EntryReceiptTest.rejectsMissingPhase11EvidenceOrProviderApproval()
Phase12EntryReceiptTest.rejectsUnacceptedCrossPhaseContracts()
Phase12EntryReceiptTest.keepsProductionAuthorityFalse()
Phase12EntryReceiptTest.recordsStableSectionsWithoutReciprocalNeighborHash()
```

**기대 결과와 실패 해석**

- 현재 expected는 implementation-ready `PASS`가 아니라 typed `GATED`다.
- Missing Phase 11 evidence로 test가 fail하면 test 결함이 아니라 정확한 entry 차단일
  수 있다.
- Provider module 파일 수는 0이어야 한다.

**Rollback/마지막 안전 지점**

Entry receipt draft와 red specification만 보존한다. Candidate source/config/resource를
만들지 않는다.

**Handoff**

WP12-1에는 immutable source/contract review와 blocker list만 넘긴다. Entry AND gate가
닫혀 있으면 WP12-2 이후 handoff는 만들지 않는다.

### WP12-1 — Accepted contract snapshot와 conformance catalog

**목적과 필요한 이유**

AWS와 candidate가 “같은 테스트”를 받도록 실행 전에 port/state/failure/capability/
case catalog를 봉인한다. Provider 결과를 본 뒤 assertion이나 applicability를 바꾸는
것을 막는다.

**사전조건**

- WP12-0 source review pass
- Phase 08~11 accepted contract refs가 모두 존재
- Exact publication precondition, current-pending action, same-state cancel fence,
  non-ambient authorization, lossless failure, durable deadline 계약이 승인됨

**예상 file/package/type**

```text
build/provider-conformance-tests/
  .../conformance/ProviderConformanceCatalog.java
  .../conformance/ProviderConformanceApplicabilityConfig.java
  .../conformance/ProviderCanonicalCodec.java
  .../oracle/ProviderConformanceOracle.java
  .../fixture/ProviderFixtureCatalog.java
```

**구체 행동**

1. Accepted port method, state, action, failure code, artifact schema와 identity를
   versioned catalog로 투영한다.
2. Canonical capability ID 22개를 exact seed로 두고 각 행의 axis/applicability,
   owner/source/case/negative-control/evidence schema를 완성한다.
3. Approved axes에서 `REQUIRED`와 승인된 pre-sealed N/A를 결정하고
   `ProviderAdoptionDecision.requiredCapabilities`를 REQUIRED 행에서만 파생한다.
4. Canonical Phase 12 §11 exact method 95개의 정규화 seed manifest와 SHA-256을
   고정하고 모든 method에 실행 전 disposition을 배정한다.
5. Independent oracle version과 metadata-exclusion projection을 고정한다.
6. Runtime test config와 `TEST_ONLY` 값도 digest에 포함한다.
7. Config를 두 provider 실행 전에 immutable seal한다.
8. §8의 reactor/test-jar/Surefire/Failsafe/report-verifier wiring이 accepted Phase 00
   build receipt에 정확히 연결됐는지 확인한다.
9. Provider-specific setup은 profile fixture로 분리하되 assertion은 하나만 둔다.

**근거**

- [Review F-P12-005](../../reviews/phase-12-review.md#f-p12-005--aws-equality가-semantic-oracle을-대신하고-applicability가-사후-변경될-수-있었다)
- [Final Architecture §5.6](../../../2026-07-26-architecture-design.md#56-test와-evidence)
- [Integrated design §22](../../../architecture-domain-implementation-design.md#22-test와-evidence-matrix)

**금지 shortcut**

- `Map<String,Object>`로 signature drift를 흡수
- Provider별 case/test class 복사 후 assertion 삭제
- Candidate failure 뒤 case를 `NOT_APPLICABLE`로 변경
- AWS output을 expected golden으로 복사
- Current live `package-info.java`를 accepted port contract로 간주
- Canonical 22개/95개 seed 중 일부만 local manifest로 다시 정의
- Direct module `-f` 성공으로 root reactor aggregation을 생략

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderConformanceCatalogTest,ProviderConformanceApplicabilityConfigTest,ProviderCanonicalCodecTest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderConformanceCatalogTest.coversEveryAcceptedPortMethodStateActionFailureAndCapability()
ProviderConformanceCatalogTest.coversExactlyCanonicalTwentyTwoCapabilitiesWithOwnerEvidenceAndNegativeControl()
ProviderConformanceCatalogTest.usesOneCaseIdAndOracleForAwsAndCandidateSubjects()
ProviderConformanceApplicabilityConfigTest.sealsAxesCasesOracleAndRuntimeConfigBeforeProviderRuns()
ProviderCanonicalOracleManifestTest.matchesCanonicalMethodSeedCountAndSha256()
ProviderCanonicalCodecTest.decodeValidateReencodeProducesCanonicalBytesAndDigest()
```

**기대 결과와 실패 해석**

- Provider SDK import/reference 0
- Accepted method/state/failure coverage 100%
- Canonical capability expected/discovered `22/22`, missing/duplicate/unknown/unowned 0
- Canonical oracle seed expected/discovered `95/95`, seed SHA-256 exact
- Duplicate provider-specific assertion 0
- Missing cross-phase signature가 있으면 bridge를 만드는 대신 WP12-0으로 되돌아간다.

**Rollback/마지막 안전 지점**

Catalog와 test fixture만 유지한다. Config가 한 provider라도 실행된 뒤 바뀌면 기존
run을 폐기하고 새 digest/version으로 처음부터 다시 만든다.

**Handoff**

WP12-2~9에 sealed applicability/config digest, common case catalog, oracle version을
넘긴다. 이것은 provider evidence나 pre-review conformance manifest가 아니다.

### WP12-2 — Approved candidate capability probe와 module boundary

**목적과 필요한 이유**

승인된 최소 axis만 module로 만들고 provider가 required semantics를 실제로 제공하는지
먼저 판정한다. Unsupported capability를 복잡한 emulation으로 숨기기 전에 adoption을
중단할 수 있다.

**사전조건**

- WP12-1 green
- Exact candidate/axis/non-prod scope adoption decision
- Scoped non-ambient credential, budget, cleanup owner
- Approved required capability set

**예상 file/package/type**

```text
adapters/<approved-axis-module>/pom.xml
.../CandidateProviderConformanceSubject.java
.../CandidateProviderCapabilityProbe.java
.../CandidateProviderConfigValidator.java
```

**구체 행동**

1. Approved axis module만 scaffold한다.
2. SDK dependency와 concrete exception/DTO를 adapter 내부에 가둔다.
3. Subject constructor가 exact decision, explicit environment와 access binding을
   요구하게 한다.
4. Canonical 22개 applicability 행의 owner/source/case/negative-control/evidence
   closure를 먼저 검증한다.
5. 각 required capability를 provider documentation 주장만이 아니라 API probe와
   isolated integration evidence로 판정한다.
6. `SUPPORTED/UNSUPPORTED/GATED/INDETERMINATE`를 모두 표현한다.
7. Unsupported required capability는 recommendation을 `RejectedUnsupported`로 끝낸다.
8. Pre-sealed N/A를 actual result에 따라 바꾸지 않고, owner approval ref 없는 N/A를
   missing으로 판정한다.

**근거**

- [Integrated design §16.1~16.6](../../../architecture-domain-implementation-design.md#161-orthogonal-substitution)
- [Canonical Phase 12 §6.1](../../phases/phase-12-provider-substitution.md#61-conformance-subject와-capability)
- [Master Plan §14](../../master-realization-plan.md#14-open-gated-deferred와-restart-condition)

**금지 shortcut**

- 승인되지 않은 provider module을 비교 목적으로 미리 생성
- Atomic CAS를 local lock/check-then-write/list로 emulation
- Ambient credential이 있으니 access binding을 생략
- Missing/unknown capability를 `false`가 아닌 default `true`로 처리
- SDK를 root/stable module dependencyManagement의 공통 dependency로 추가

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/architecture-rules/pom.xml clean test \
  -Dtest=ProviderSdkLeakageArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true

./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dit.test=ProviderCapabilityProbeIT \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderConfigValidationTest.unapprovedProviderOrAxisCannotConstructSubject()
ProviderConfigValidationTest.ambientCredentialOrDefaultProviderCannotConstructSubject()
ProviderConformanceApplicabilityConfigTest.everyCanonicalCapabilityHasExactlyOnePresealedDecisionOwnerEvidenceAndNegativeControl()
ProviderSdkLeakageArchitectureTest.providerTypesAppearOnlyInApprovedAdapterDistributionAndDeploymentModules()
ProviderCapabilityProbeIT.reportsUnsupportedInsteadOfEmulatingMissingAtomicCas()
```

**기대 결과와 실패 해석**

- Canonical 22개 각각이 pre-run applicability 하나를 가지며 uncovered/duplicate 0
- 모든 required capability가 provider disposition 네 가지 중 정확히 하나
- Architecture leakage 0
- Failsafe discovered test count가 expected manifest와 일치
- Required capability가 `UNSUPPORTED`이면 WP 실패가 아니라 adoption reject evidence다.

**Rollback/마지막 안전 지점**

Candidate assembly를 비활성화하고 credential/policy를 revoke한다. Provider resource를
production과 연결하지 않는다. Source branch가 있다면 승인 범위 밖 파일은 만들지
않고 rejection evidence만 보존한다.

**Handoff**

Approved axes와 capability matrix를 applicable WP12-3/4로 넘긴다. Unsupported axis는
WP12-3/4로 넘기지 않는다.

### WP12-3 — Storage, CAS와 consistency conformance

**목적과 필요한 이유**

Object storage를 단순 byte 저장소가 아니라 immutable artifact, exact verified read,
state CAS와 publication CAS를 가진 권위 경계로 검증한다.

**사전조건**

- WP12-2에서 `STORAGE` axis 승인 및 required capabilities supported
- Phase 09 accepted contract suite
- Distinct publication precondition과 lossless storage failure carrier 승인

**예상 file/package/type**

```text
adapters/object-<approved-provider>/
  .../CandidateObjectStorageBackend.java
  .../CandidateObjectKeyLayout.java
  .../CandidateStorageFailureMapper.java
  .../CandidateStorageConfig.java
```

**구체 행동**

1. Logical key를 provider key/locator로 한 방향 mapping한다.
2. Put-if-absent, same-digest convergence, different-digest conflict를 구현한다.
3. Tenant-scoped `SubmissionIdentity`에 대해 initial state create-if-absent를 하나의
   atomic semantic transition으로 구현한다.
4. Same/same convergence, same/different conflict와 concurrent one-winner를 검증한다.
5. Kind/schema/length/digest 확인 뒤에만 bytes를 deserialize한다.
6. State CAS와 publication CAS를 서로 다른 typed precondition으로 구현한다.
7. Create/CAS lost acknowledgement를 exact read로 reconcile한다.
8. Committed write의 visibility/read semantics를 actual provider에서 검증한다.
9. Tenant A가 tenant B submission/exact key 존재 여부도 알 수 없게 negative test한다.

**근거**

- [Integrated design §16.4](../../../architecture-domain-implementation-design.md#164-gcs-또는-azure-blob-추가)
- [Integrated design §16.7](../../../architecture-domain-implementation-design.md#167-artifact-migration)
- [Review F-P12-001](../../reviews/phase-12-review.md#f-p12-001--run-state-version이-action-authorization과-publication-precondition을-겸했다)

**금지 shortcut**

- Prefix listing으로 read/completeness 판단
- Check-then-put, last-write-wins, unconditional overwrite
- Provider ETag/generation을 parse/order/fingerprint
- Run-state version을 publication precondition으로 캐스팅
- Same key/different digest를 retry 가능한 success로 처리
- Emulator만으로 consistency/CAS/IAM을 증명
- Submission index와 initial state를 두 번 check-then-write
- Lost create ack 뒤 새 `SolveId`를 발급하거나 `SUBMITTED`로 reset
- Prefix list/hidden mutable index로 “winner” state를 사후 선택

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dtest=ProviderStorageConformanceTest \
  -Dit.test=CandidateObjectStorageProviderIT \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderStorageConformanceTest.putIfAbsentCreatesOnceAndSameDigestConverges()
ProviderStorageConformanceTest.sameKeyDifferentDigestIsIntegrityConflictWithoutOverwrite()
ProviderStorageConformanceTest.readVerifiesKindSchemaLengthDigestBeforeDeserialization()
ProviderStorageConformanceTest.initialStateCreateIfAbsentHasExactlyOneSubmissionWinner()
ProviderStorageConformanceTest.sameSubmissionSameCanonicalDigestsReturnsSameSolveIdAndState()
ProviderStorageConformanceTest.sameSubmissionDifferentCanonicalDigestsIsIdempotencyConflict()
ProviderStorageConformanceTest.lostCreateAcknowledgementConvergesByTenantScopedExactRead()
ProviderStorageConformanceTest.crashCannotExposePartialSubmissionBindingOrOrphanInitialState()
ProviderStorageConformanceTest.concurrentStateCasHasExactlyOneWinner()
ProviderStorageConformanceTest.runStateVersionCannotServeAsPublicationPrecondition()
ProviderStorageConformanceTest.lostPublicationResponseConvergesByExactPointerRead()
CandidateObjectStorageProviderIT.actualBackendPassesCompleteStorageConformanceSuite()
CandidateObjectStorageProviderIT.indeterminateWriteIsReconciledWithoutBlindOverwrite()
```

**기대 결과와 실패 해석**

- Concurrent initial create/state writer winner 정확히 1
- Same/same returns exact same `SolveId`/state/version; same/different mutation 0
- Crash/lost ack 뒤 duplicate workflow start, orphan state와 reset 0
- Original bytes/pointer overwrite 0
- List call count 0
- Semantic digest mismatch 0
- Visibility indeterminate는 success가 아니라 typed failure/reconcile path다.

**Rollback/마지막 안전 지점**

Partial/indeterminate object를 quarantine하고 state/publication pointer를 전진시키지
않는다. Accepted source refs와 AWS/local path를 유지한다.

**Handoff**

`StorageConformanceEvidence`, exact limitations와 failure matrix를 WP12-5, WP12-7,
WP12-9로 넘긴다.

### WP12-4 — Workflow, coordinator와 worker conformance

**목적과 필요한 이유**

Candidate workflow/compute가 Phase 10의 action을 운반할 뿐 score, completeness,
verification과 publication 의미를 새로 소유하지 않음을 증명한다.

**사전조건**

- WP12-2에서 workflow/compute axis 승인
- Phase 10 accepted state/action/retry/cancel/deadline contract
- Exact current-pending action authorization과 committed worker outcome authority

**예상 file/package/type**

```text
adapters/workflow-<approved-provider>/
adapters/compute-<approved-provider>/
distributions/<approved-distribution>/
```

후보 type:

```text
CandidateWorkflowExecutionAdapter
CandidateCoordinatorActionExecutor
CandidateWorkerDispatcher
CandidateCommandEnvelopeV1
CandidateComputeEventV1
```

**구체 행동**

1. Submission retry가 WP12-3의 exact binding/state를 읽고 같은 logical workflow
   identity로 수렴한 뒤에만 start하도록 mapping한다.
2. Same/same은 workflow start를 중복하지 않고 same/different는 start call 0으로
   `IDEMPOTENCY_CONFLICT`를 반환한다.
3. Start/status/cancel과 durable re-entry를 provider-neutral port에 mapping한다.
4. Side effect 직전에 exact current pending `ActionId`, payload fingerprint,
   authorizing transition ordinal을 확인한다.
5. Same `WorkerRunId` retry에서 새 `AttemptId`만 허용한다.
6. Duplicate/lost/out-of-order event를 exact state read로 수렴시킨다.
7. Declared worker set 전부가 없으면 aggregation/publication을 막는다.
8. Cancel/publish same-state race와 late provider event를 검증한다.
9. Platform timeout/throttle/capacity를 정상 termination과 분리한다.

**근거**

- [Integrated design §16.9](../../../architecture-domain-implementation-design.md#169-workflowcompute-adapter-contract-suite)
- [Phase 11 §6.2~6.3](../../phases/phase-11-aws-reference-distribution.md#62-workflow와-compute-mapping)
- [Review F-P12-004](../../reviews/phase-12-review.md#f-p12-004--crashre-entry-parity에-durable-monotonic-deadline-복원-계약이-없었다)

**금지 shortcut**

- Workflow Choice/controller에서 objective/comparator/verifier 판단
- Provider completion list/count를 declared completeness로 사용
- Retry 때 seed/warm start/assignment/work 변경
- Cancel intent object를 terminal fence로 사용
- Platform timeout을 `MAX_STEPS_REACHED`로 변환
- Restart 때 wall clock/provider remaining time으로 deadline 복원
- Initial binding/state 확인 전 workflow를 먼저 시작
- Submission conflict를 existing success로 축소하거나 create retry에서 새 workflow key 생성

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dtest=ProviderWorkflowConformanceTest,ProviderWorkerConformanceTest \
  -Dit.test=ProviderApplicationParityIT \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderWorkflowConformanceTest.sameWorkflowKeySameManifestConverges()
ProviderWorkflowConformanceTest.sameSubmissionSameDigestsStartsExactlyOneLogicalWorkflow()
ProviderWorkflowConformanceTest.sameSubmissionDifferentDigestsStartsNothingAndConflicts()
ProviderWorkflowConformanceTest.crashAfterInitialStateCommitReconcilesBeforeWorkflowStart()
ProviderWorkflowConformanceTest.crashAfterActionCasReplaysSameActionId()
ProviderWorkflowConformanceTest.executorRejectsActionNotMatchingExactCurrentPendingPayloadAndOrdinal()
ProviderWorkflowConformanceTest.missingDeclaredWorkerBlocksAggregationAndPublication()
ProviderWorkerConformanceTest.retryChangesAttemptOnlyAndPreservesAssignmentSeedWarmStartAndWork()
ProviderWorkerConformanceTest.platformTimeoutResourceLimitAndThrottleAreNotNormalTermination()
ProviderApplicationParityIT.sameManifestProducesSameCanonicalArtifactsResultAndTermination()
```

**기대 결과와 실패 해석**

- Same logical trace 또는 같은 typed exceptional terminal
- Missing declared worker에서 dispatch/aggregation/publication count 0
- Completion permutation이 champion/result digest를 바꾸지 않음
- Durable deadline contract가 없으면 deadline cases는 `GATED`, pass로 세지 않음

**Rollback/마지막 안전 지점**

New candidate starts를 중단하고 in-flight exact state를 reconcile한다. Candidate
terminal을 success로 바꾸지 않고 accepted AWS/local path를 유지한다.

**Handoff**

Workflow/compute evidence, semantic replay trace와 open mappings를 WP12-5, WP12-8,
WP12-9로 넘긴다.

### WP12-5 — Failure, retry, deadline, serialization과 replay parity

**목적과 필요한 이유**

Happy path가 같은지보다 장애가 같은 semantic boundary에서 멈추는지를 검증한다.
Provider exception 이름은 달라도 application은 retry 가능성, integrity, security,
last safe point를 잃지 않아야 한다.

**사전조건**

- WP12-3/4 중 approved axis가 green
- Accepted exhaustive failure carrier
- Canonical codec/version allowlist
- Retry/deadline policy ref; 숫자는 승인 전 `TEST_ONLY`

**예상 file/package/type**

```text
.../failure/CandidateProviderFailureMapper.java
.../codec/ProviderCanonicalEnvelopeCodec.java
.../replay/SemanticReplayProjection.java
.../replay/ProviderObservationProjection.java
```

**구체 행동**

1. Transport, throttle, authn/authz, conflict, indeterminate visibility, corruption,
   quota, timeout, cancel, unknown protocol fault를 하나씩 주입한다.
2. 각 fault를 neutral code와 retry disposition 하나에 lossless mapping한다.
3. SDK/delivery/application retry trace를 분리한다.
4. Unknown schema/version을 `UNSUPPORTED_SCHEMA`로 거부한다.
5. Decode → semantic validate → canonical re-encode → digest equality를 검증한다.
6. Provider metadata를 바꿔도 semantic replay projection이 같은지 확인한다.
7. Crash/re-entry는 approved durable deadline or typed gated/indeterminate로 끝낸다.

**근거**

- [Canonical Master §13](../../../master-design.md#13-termination-reproducibility와-execution-provenance)
- [Final Architecture §5.3](../../../2026-07-26-architecture-design.md#53-상태와-failure-계약)
- [Review F-P12-003](../../reviews/phase-12-review.md#f-p12-003--accessfailureworker-authority-gap을-adapter가-임의로-메울-수-있었다)

**금지 shortcut**

- Unknown SDK status를 success/empty/not-found로 mapping
- Denied/missing/corrupt/stale/indeterminate를 generic error로 축소
- Java serialization 또는 SDK DTO를 artifact로 저장
- Map iteration order, provider event order를 canonical order로 사용
- Retry exhausted를 normal termination으로 바꿈
- Wall clock fallback으로 restart deadline test를 green

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderFailureTaxonomyContractTest,ProviderCanonicalCodecTest \
  -Dsurefire.failIfNoSpecifiedTests=true

./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dit.test=ProviderRetryIdentityIT,ProviderDeadlineRestartIT,ProviderSerializationReplayIT,ProviderCorruptionIT \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderFailureTaxonomyContractTest.mapsEveryProviderFaultToOneNeutralCodeAndRetryDisposition()
ProviderFailureTaxonomyContractTest.unknownProviderStatusFailsClosedAsProtocolViolation()
ProviderCanonicalCodecTest.unknownSchemaVersionIsUnsupportedNotSilentlyIgnored()
ProviderRetryIdentityIT.transportDeliveryAndApplicationRetriesRemainDistinct()
ProviderDeadlineRestartIT.restartUsesApprovedDurableOriginReconciliationOrFailsClosed()
ProviderSerializationReplayIT.awsAndCandidateRoundTripSameArtifactsWithoutSemanticDrift()
ProviderCorruptionIT.mutatedBytesLengthSchemaOrAuthorityRefAreRejectedBeforeUse()
```

**기대 결과와 실패 해석**

- Fault matrix uncovered row 0
- Forbidden serialization format acceptance 0
- Same logical retry의 assignment/seed/work drift 0
- Corrupt bytes는 deserialize 전에 거부
- Missing durable deadline approval은 `GATED`; hidden fallback pass는 defect다.

**Rollback/마지막 안전 지점**

Bad attempt/artifact를 quarantine하고 자동 retry를 중단한다. Last committed state와
verified result ref를 유지한다.

**Handoff**

Failure/replay evidence와 open mapping list를 WP12-8/9로 넘긴다.

### WP12-6 — Security, observability와 operations equivalence

**목적과 필요한 이유**

Provider 이름이 아니라 같은 control objective가 충족되는지 검증한다. Semantic parity가
있어도 broad credential, tenant crossing, secret leakage, alarm 부재가 있으면 adoption
후보가 될 수 없다.

**사전조건**

- Applicable adapter integration environment
- Approved security control profile, data classification, retention/network policy
- Explicit non-ambient access binding과 lossless deny mapping

**예상 file/package/type**

- Candidate provider identity/policy/IaC source
- `ProviderSecurityControlIT`
- `ProviderFailureCarrierIT`
- `ProviderObservabilityIT`
- `ProviderOperationalRecoveryIT`

**구체 행동**

1. API/coordinator/worker/workflow/deploy 역할별 최소 operation matrix를 만든다.
2. Cross-tenant exact read/write/CAS/start/stop/publish negative test를 실행한다.
3. Missing/mismatched explicit access binding이 ambient credential로 fallback하지 않게 한다.
4. TLS/endpoint, at-rest encryption, key reference, rotation/revocation owner를 검증한다.
5. Secret/PII/raw payload/signed locator forbidden corpus를 log/trace에서 검색한다.
6. Neutral correlation event와 provider metric namespace mapping을 비교한다.
7. Integrity, auth deny, retry exhaustion, timeout, incomplete work alarm/recovery를
   실제 environment에서 rehearsal한다.

**근거**

- [Integrated design §20](../../../architecture-domain-implementation-design.md#20-security와-tenant-boundary)
- [Final Architecture §5.5](../../../2026-07-26-architecture-design.md#55-observability와-security)
- [Canonical Phase 12 §13~14](../../phases/phase-12-provider-substitution.md#13-security-iam-equivalence와-encryption-contract)

**금지 shortcut**

- 같은 role 이름이면 equivalent라고 판단
- Broad wildcard data role + application tenant string check
- Encryption metadata로 digest 검증을 대체하거나 그 반대
- Raw payload, address, credential, unrestricted locator를 telemetry에 기록
- Console policy screenshot만 evidence로 사용
- Missing required alarm을 “운영에서 나중에”로 두고 parity pass

**검증 명령과 test — FUTURE**

```bash
./mvnw -f distributions/<approved-distribution>/pom.xml \
  -Pprovider-integration clean verify \
  -Dit.test=ProviderSecurityControlIT,ProviderFailureCarrierIT,ProviderObservabilityIT,ProviderOperationalRecoveryIT \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderSecurityControlIT.workerCannotReadOtherTenantWriteChampionOrStartWorkflow()
ProviderSecurityControlIT.missingOrMismatchedExplicitAccessBindingCannotUseAmbientCredential()
ProviderSecurityControlIT.encryptionInTransitAtRestAndContentDigestAreIndependentlyVerified()
ProviderFailureCarrierIT.deniedMissingCorruptStaleAndIndeterminateRemainDistinctEndToEnd()
ProviderObservabilityIT.requiredCorrelationStateFailureRetryAndVerificationEventsAreEmitted()
ProviderOperationalRecoveryIT.reentryAfterProcessLossResumesFromExactCommittedState()
ProviderOperationalRecoveryIT.lateSuccessAfterExceptionalTerminalCannotPublish()
```

**기대 결과와 실패 해석**

- Forbidden operation allow 0
- Backend call/existence disclosure 0 for missing binding
- Forbidden log match 0
- Required semantic event/alarm 누락 0
- Provider-native deny를 application exception으로만 흉내 낸 결과는 fail이다.

**Rollback/마지막 안전 지점**

Credentials/policy/revision을 revoke하고 new starts를 중단한다. Immutable audit
evidence를 보존하고 incident/owner에게 handoff한다.

**Handoff**

Security/operations comparison과 residual risk를 WP12-8/9로 넘긴다.

### WP12-7 — Artifact copy, shadow와 migration rehearsal

**목적과 필요한 이유**

Provider 교체가 content와 authority를 바꾸지 않으며 실패 시 source가 계속 안전한
기준점임을 증명한다.

**사전조건**

- `STORAGE` axis applicable, WP12-3 green
- Source/destination non-prod scope와 copy/cleanup owner 승인
- Declared artifact closure schema와 exact ref traversal

**예상 file/package/type**

```text
.../migration/ArtifactCopyPlanner.java
.../migration/ArtifactCopyExecutor.java
.../migration/ArtifactCopyReceipt.java
.../migration/DeclaredClosureVerifier.java
.../shadow/ShadowDistributionSelector.java
```

**구체 행동**

1. Source ref의 kind/schema/length/digest/authority를 확인한다.
2. Declared exact refs를 stable order로 순회한다.
3. Destination exact logical key에 put-if-absent한다.
4. Destination exact read-back 후 canonical digest/authority를 검증한다.
5. Ref마다 immutable receipt를 봉인한다.
6. Closure completeness를 확인한 뒤 publication/state/traffic authority 0인 shadow를
   실행한다.
7. Destination conflict, partial copy, process loss와 rollback을 rehearsal한다.

**근거**

- [Integrated design §16.7](../../../architecture-domain-implementation-design.md#167-artifact-migration)
- [Canonical Master §16.2](../../../master-design.md#162-migration)
- [Master Plan §12.2](../../master-realization-plan.md#122-rollback)

**금지 shortcut**

- Prefix list 결과를 closure로 사용
- Source를 verify하기 전에 copy
- Destination read-back 생략
- Same key/different digest overwrite
- Shadow에서 publication/state/source pointer/production traffic 변경
- 실패 cleanup을 위해 source artifact 삭제

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dit.test=ProviderArtifactMigrationIT,ProviderShadowIsolationIT,ProviderRollbackIT \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderArtifactMigrationIT.copyVerifiesSourcePutIfAbsentReadBackAndCompleteClosure()
ProviderArtifactMigrationIT.sameLogicalKeyDifferentDigestStopsBeforePointerProposal()
ProviderArtifactMigrationIT.providerLocatorChangeDoesNotChangeSemanticFingerprint()
ProviderShadowIsolationIT.shadowRunCannotPublishMutateSourceStateOrReceiveProductionTraffic()
ProviderRollbackIT.rollbackPreservesImmutableArtifactsAndReconcilesInflightState()
```

**기대 결과와 실패 해석**

- Declared ref receipt coverage 100%
- Canonical digest/authority mismatch 0
- Source pointer/state/traffic mutation count 0
- Destination conflict는 integrity failure이며 overwrite retry 대상이 아니다.

**Rollback/마지막 안전 지점**

Source refs/pointers를 그대로 유지한다. Destination conflict를 quarantine한다. Copied but
unreferenced bytes는 승인된 cleanup policy 전 삭제하지 않는다.

**Handoff**

Migration/rollback receipts와 shadow isolation proof를 WP12-8/9로 넘긴다.

### WP12-8 — Full parity와 performance/cost envelope comparison

**목적과 필요한 이유**

모든 applicable contract를 같은 sealed config와 corpus로 실행하고, 각 provider가
독립 oracle에 pass한 뒤 semantic parity와 운영 envelope를 비교한다.

**사전조건**

- WP12-3~7 중 applicable gate 전부 green
- Standalone AWS reference evidence가 accepted Phase 11에서 제공됨
- Approved workload/corpus와 performance/cost policy 또는 명시적 `GATED` 처리

**예상 file/package/type**

```text
ProviderEvidenceManifest              # AWS와 candidate 각각 독립
ProviderEvidenceComparison
ProviderEndToEndParityIT
ProviderReproducibilityIT
ProviderPerformanceEnvelopeIT
ProviderCostEvidenceIT
```

**구체 행동**

1. Same sealed config/case catalog/corpus를 AWS와 candidate에 실행한다.
2. 각 evidence가 config digest, actual case results와 oracle assertions를 기록한다.
3. 두 evidence를 conformance/review/receipt back-reference 없이 각각 먼저 seal한다.
4. Artifact/state/identity/failure/termination/security/observability/migration projection을
   비교한다.
5. Performance/cost는 semantic/security hard gate 뒤에 평가한다.
6. Threshold가 승인되지 않았으면 `GATED_MISSING_POLICY`, pass가 아니다.
7. Recommendation과 limitation을 기록하되 `productionAuthority=false`로 봉인한다.

**근거**

- [Canonical Phase 12 §9](../../phases/phase-12-provider-substitution.md#9-conformance와-evidence-comparison-model)
- [Review F-P12-005](../../reviews/phase-12-review.md#f-p12-005--aws-equality가-semantic-oracle을-대신하고-applicability가-사후-변경될-수-있었다)
- [Master Plan §13](../../master-realization-plan.md#13-위험-보안-운영-관측과-재현성)

**금지 shortcut**

- AWS와 candidate equality만으로 pass
- 한 provider case skip 또는 다른 expected failure
- Candidate가 빠르다는 이유로 security/semantic fail 허용
- Test-only threshold를 production policy로 사용
- Failed/timeout run을 corpus에서 제외
- 서로 다른 config/manifest 결과의 좋은 부분만 조합

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-parity clean verify \
  -Dit.test=ProviderEndToEndParityIT,ProviderReproducibilityIT,ProviderPerformanceEnvelopeIT,ProviderCostEvidenceIT \
  -Dfailsafe.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderEndToEndParityIT.localAwsAndCandidateAgreeOnProviderNeutralExecutionProjection()
ProviderReproducibilityIT.sameStrongEnvelopeProducesSameSemanticTraceAndResult()
ProviderReproducibilityIT.differentVerifiedDigestForSameStrongReplayIdentityIsIntegrityFailure()
ProviderPerformanceEnvelopeIT.semanticParityAndSecurityRemainHardGatesEvenWhenCandidateIsFaster()
ProviderPerformanceEnvelopeIT.missingApprovedThresholdReturnsGatedNotPass()
ProviderCostEvidenceIT.doesNotConvertExperimentEstimateIntoProductionDefault()
ProviderEvidenceManifestTest.awsAndCandidateEvidenceHaveNoConformanceReviewOrReceiptBackReference()
```

**기대 결과와 실패 해석**

- Each-subject independent oracle pass 100%
- Applicable skip 0
- Semantic mismatch 0
- Missing approved envelope는 `GATED`, false pass 0
- Candidate mismatch는 activation 없음이며 AWS production 권한도 자동 변경하지 않는다.

**Rollback/마지막 안전 지점**

Candidate activation을 하지 않고 exact mismatch와 last safe point를 evidence에 남긴다.
AWS reference role과 current production/default도 Phase 12가 변경하지 않는다.

**Handoff**

Standalone AWS/candidate evidence digest, comparison, recommendation과 limitations를
WP12-9로 넘긴다.

### WP12-9 — Conformance manifest, independent review와 bounded handoff

**목적과 필요한 이유**

Review 대상과 review 결과가 서로를 참조하는 순환을 막고, 어떤 bytes가 검토됐는지
exact digest로 고정한다.

**사전조건**

- WP12-0~8 applicable evidence complete
- Open/unsupported/gated limitation 포함
- Independent reviewer와 acceptance authority 분리

**예상 artifact/type**

```text
ProviderConformanceManifest
IndependentPhase12Review
Phase12AcceptanceReceipt
Phase12ToPhase13SubstitutionEvidence
```

**구체 행동**

1. AWS와 candidate evidence를 서로 독립 seal한다.
2. Applicability/config, 두 provider evidence content digest, comparison/limitations를
   forward-reference하는 `ProviderConformanceManifest`를 review 전에 seal한다.
3. Reviewer는 exact conformance manifest digest만 권위 입력으로 검토한다.
4. Review 뒤 manifest/evidence를 backfill하거나 overwrite하지 않는다.
5. Acceptance receipt만 manifest digest와 review digest/verdict를 결합한다.
6. Receipt와 handoff에 `productionAuthority=false`, `c17Approval=false`를 확인한다.
7. Phase 13/14 consumer에는 bounded projection과 limitations만 넘긴다.

**근거**

- [Canonical Phase 12 §15.1](../../phases/phase-12-provider-substitution.md#151-evidence-manifests)
- [Review F-P12-007](../../reviews/phase-12-review.md#f-p12-007--conformanceevidencereview-manifests가-circular하게-봉인될-수-있었다)
- [Phase 11 §18.2](../../phases/phase-11-aws-reference-distribution.md#182-phase-12-handoff)

**금지 shortcut**

- Evidence가 conformance manifest/review/receipt를 역참조
- Conformance manifest가 review/receipt를 선참조
- Review가 provider evidence를 직접 content-reference
- Acceptance receipt가 provider evidence를 직접 참조
- Review 교정 때문에 기존 evidence/manifest bytes overwrite
- Document review `CHANGES_REQUIRED` 상태에서 accepted receipt 위조

**검증 명령과 test — FUTURE**

```bash
./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderEvidenceManifestTest,ProviderConformanceManifestTest,IndependentPhase12ReviewTest,Phase12AcceptanceReceiptTest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

Exact method 후보:

```text
ProviderConformanceManifestTest.forwardReferencesApplicabilityAndExactlyTwoProviderEvidenceDigests()
ProviderConformanceManifestTest.containsNoReviewOrAcceptanceReference()
IndependentPhase12ReviewTest.referencesReviewedConformanceManifestDigestButNoProviderEvidence()
Phase12AcceptanceReceiptTest.referencesExactConformanceManifestAndIndependentReviewDigests()
Phase12AcceptanceReceiptTest.rejectsEvidenceManifestReviewOrReceiptCycle()
Phase12AcceptanceReceiptTest.rejectsMismatchedManifestOrReviewDigest()
Phase12AcceptanceReceiptTest.rejectsProductionAuthorityTrue()
```

**기대 결과와 실패 해석**

- Evidence→conformance/review/receipt edge 0
- Conformance→review/receipt edge 0
- Review→provider evidence/receipt edge 0
- Receipt의 manifest/review digest 각각 정확히 1, exact match
- `productionAuthority=true` 0
- Independent verdict가 acceptance 조건을 만족하지 않으면 handoff `NOT_READY`

**Rollback/마지막 안전 지점**

불완전 chain을 수정 overwrite하지 않는다. 영향을 받은 가장 이른 evidence부터 새
content digest로 재생성해 manifest → review → receipt를 새 version chain으로 만든다.

**Handoff**

- Phase 13: selected substituted runtime일 때만 infrastructure decision input
- Phase 14B: applicable provider evidence/review/receipt와 rollback point
- Status authority: scheduler가 exact receipt를 검증한 뒤에만 상태 변경

## 11. Test-first 구현 안내

### 11.1 Fixture, builder와 independent oracle

Fixture는 provider resource를 직접 가리키는 credential bundle이 아니다. Semantic
input과 expected authority를 고정하고 provider setup은 별도 binding으로 주입한다.

| Fixture ID | 핵심 내용 | Independent oracle |
|---|---|---|
| `provider/basic-success-v1` | Explicit manifest, declared workers, both verifier PASS refs | Phase 10 state/action + Phase 07 result hand oracle |
| `provider/submission-same-same-v1` | Same tenant/submission/canonical digest tuple, concurrent create와 lost ack | Same `SolveId`/`SUBMITTED` version, exactly one create |
| `provider/submission-same-different-v1` | Same tenant/submission, one canonical digest differs | `IDEMPOTENCY_CONFLICT`, mutation/start 0 |
| `provider/submission-crash-boundary-v1` | Commit 직전/직후 process loss | Binding/state both absent or both exact-readable |
| `provider/missing-screen-v1` | Declared Phase-1 screen 하나 누락 | Warm start/Phase-2 dispatch absent |
| `provider/missing-worker-v1` | Declared outcome 하나 누락 | `INCOMPLETE`/wait, pointer absent |
| `provider/duplicate-out-of-order-v1` | Completion/wakeup permutation | Same semantic replay digest |
| `provider/cas-race-v1` | 같은 expected version의 다른 next states | Exactly one winner |
| `provider/publication-precondition-v1` | Run-state token과 pointer precondition 교차 | Type/domain separation |
| `provider/lost-ack-v1` | Create/CAS success response loss | Exact read convergence |
| `provider/retry-identity-v1` | Start failure 뒤 retry | `AttemptId`만 변경 |
| `provider/platform-timeout-v1` | Completion record 전 runtime 종료 | `PLATFORM_TIMEOUT`, normal 아님 |
| `provider/cancel-publish-race-v1` | Cancel request와 publish 경쟁 | Same-run-state CAS one winner |
| `provider/deadline-restart-v1` | Process loss와 monotonic origin 변경 | Approved reconciliation or gated |
| `provider/corrupt-artifact-v1` | Bytes/schema/length/authority 변조 | Deserialize 전 integrity rejection |
| `provider/serialization-v1` | Noncanonical collection order | Canonical bytes/digest exact |
| `provider/cross-tenant-v1` | Tenant A가 B ref/action probe | Denied, metadata disclosure 0 |
| `provider/ambient-auth-v1` | Explicit binding 없음, ambient credential 있음 | Provider call 0 |
| `provider/role-operation-v1` | API/workflow/worker role별 forbidden artifact/state/crypto operation | Provider-native deny, application-only check 불허 |
| `provider/alarm-catalog-v1` | Integrity/access/retry exhaustion/timeout/incomplete work signal을 하나씩 제거 | Missing alarm exact ID 검출 |
| `provider/deadline-reserve-v1` | Final verification/publication 직전 bounded budget | Approved reserve 안 complete 또는 typed non-pass |
| `provider/candidate-rollback-v1` | Candidate new-start failure와 in-flight reconciliation | New non-prod starts는 accepted reference로만 복귀 |
| `provider/shadow-copy-v1` | Complete declared artifact closure | Destination digest exact, source unchanged |
| `provider/performance-corpus-v1` | Approved workload class/repeat | External policy or gated |

Builder 후보:

```text
ProviderConformanceFixtureBuilder
  withManifest(...)
  withDeclaredWorkers(...)
  withCanonicalArtifacts(...)
  withFault(...)
  withAuthorizationBinding(...)
  withTestOnlyRetryPolicy(...)
  withTestOnlyVirtualClock(...)
  buildAndSeal()
```

Builder는 default provider, default tenant, hidden retry/deadline/threshold를 갖지 않는다.
필수 값이 없으면 build가 실패해야 한다.

Oracle 우선순위:

1. Hand-calculated state/CAS/identity result
2. Phase 07 cache-free verifier와 Phase 10 pure state/action reference
3. Small exhaustive/permutation oracle
4. Deterministic in-memory/file subject
5. AWS reference와 candidate actual observations는 **피시험 값**

AWS 결과를 candidate expected로 복사하지 않는다. 같은 공용 projection bug를 잡기 위해
provider field/ordering/digest mutation을 넣은 defect-sensitivity test를 둔다.

### 11.2 Contract와 architecture test 후보

#### Canonical exact-oracle manifest와 hash

Canonical Phase 12 §11.2~§11.9의 exact method name은 현재 authoritative source
snapshot에서 **95개이며 모두 unique**하다. Correction 01이 고정한 method-name seed는
다음과 같다.

```text
canonicalSourcePath = docs/implementation/phases/phase-12-provider-substitution.md
canonicalSourceSha256 = 5f243b2900afe31801ab1c47b9c2467a6344c42b7b27f49ee5c5b51d1c21de1a
normalization = extract backticked Java no-arg method IDs from §11,
                strip backticks, LC_ALL=C sort -u, LF after every entry
expectedCanonicalMethodCount = 95
canonicalMethodNameSetSha256 = 9efdc487e7f059c0362fa5e3cfdfeeb65d265f8b2193f8cb02161653547efb40
```

재현 명령:

```bash
sed -n '/^## 11\. Exact test specification$/,/^## 12\. /p' \
  docs/implementation/phases/phase-12-provider-substitution.md |
  rg -o '`[A-Za-z][A-Za-z0-9]*\(\)`' |
  tr -d '`' |
  LC_ALL=C sort -u |
  shasum -a 256
```

Source SHA 또는 seed count/hash가 다르면 hash만 갱신하지 않고 canonical 변경 영향을
다시 review한다. Future
`src/test/resources/phase12-canonical-oracle-manifest-v1.txt`는 method name만 복사한
목록이 아니라 다음 tuple을 95개 모두 가져야 한다.

```text
TestClass#exactMethod()
canonicalMethodName
disposition =
  REQUIRED |
  BLOCKED_PENDING_ACCEPTED_CONTRACT |
  NOT_APPLICABLE_PRESEALED_WITH_APPROVAL
fixtureOrFaultId
independentOracleIdAndVersion
forbiddenCallOrStateMutation
expectedDisposition
actualProviderEvidenceRequired
plannedEvidenceKey
ownerAndApprovalOrBlockerRef
```

Manifest는 provider 실행 전 canonical method 95개와 correction-required 추가 case를
별도 namespace로 seal하고 전체 canonical bytes SHA-256을 기록한다. Provider evidence
각각은 다음 count와 digest를 기록한다.

```text
canonicalMethodNameSetSha256
sealedExactManifestContentSha256
expected / discovered / executed
passed / failed / errors / skipped
blocked / approvedNotApplicable
missing / unexpected / duplicate
surefireReportDigests[] / failsafeReportDigests[]
sourceCommit / reactorPomDigests[] / profile / runId / startedAt / finishedAt
```

Class filter가 exit 0인 것만으로는 pass가 아니다. `REQUIRED`는 discovered=executed=passed,
failed/error/skipped/missing 0이어야 한다. `BLOCKED`가 하나라도 있으면 현재 환경의
full-suite/adoption은 `GATED`, pass가 아니다. Approved pre-sealed N/A는 exact owner
approval ref가 있을 때만 missing에서 제외한다. Canonical 95개 바깥 method는 sealed
correction-required namespace에 없으면 unexpected다.

Review에서 누락됐던 canonical 7개 exact oracle은 다음과 같이 복원한다.

| Test class와 exact method | Fixture/fault | Independent oracle | Forbidden call/state mutation | Expected disposition | Actual provider | Planned evidence |
|---|---|---|---|---|---|---|
| `ProviderWorkflowConformanceTest.missingDeclaredScreenBlocksWarmStartAndPhaseTwoDispatch()` | `provider/missing-screen-v1` | Declared screen manifest 대 exact outcome ref closure | Warm-start read, Phase-2 dispatch, state advance | `INCOMPLETE`/wait; three call counts 0 | AWS + candidate workflow/compute axis | `E-P12-PROVIDER-CONTRACT`, `E-P12-PARITY` |
| `ProviderSecurityControlIT.apiCannotReadUnpublishedResultOrWriteWorkerOutcome()` | API role in `provider/role-operation-v1` | Provider-native role×operation deny matrix | Unpublished result read, worker outcome put/CAS | Denied before existence disclosure; mutation 0 | Required | `E-P12-SECURITY` |
| `ProviderSecurityControlIT.workflowIdentityCannotReadBusinessArtifactsOrDecryptPayload()` | Workflow role in `provider/role-operation-v1` | Provider-native deny + key policy oracle | Artifact read, decrypt, raw payload access | Denied; backend plaintext/read count 0 | Required | `E-P12-SECURITY` |
| `ProviderSecurityControlIT.wildcardDataPlanePrivilegeAndApplicationOnlyTenantCheckAreRejected()` | Wildcard/broad policy mutation | Approved least-privilege policy analyzer + cross-tenant probe | Wildcard allow와 application-only tenant filter | Subject/config rejected; provider call 0 | Required | `E-P12-SECURITY` |
| `ProviderObservabilityIT.alarmsCoverIntegrityAccessRetryExhaustionTimeoutAndIncompleteWork()` | `provider/alarm-catalog-v1`에서 alarm 하나씩 제거 | Required semantic event/alarm ID set equality | Missing alarm을 generic alarm/로그로 대체 | Every mutation fail; missing alarm ID exact | Required | `E-P12-OPERATIONS` |
| `ProviderPerformanceEnvelopeIT.deadlineReserveCanCompleteFinalVerificationAndPublication()` | `provider/deadline-reserve-v1` | Approved durable budget/reserve + virtual-clock and actual trace | Reserve 침범 후 verifier/publish 실행 또는 false success | Complete within approved reserve or typed non-pass | Actual environment + deterministic fault oracle | `E-P12-PERFORMANCE`, `E-P12-OPERATIONS` |
| `ProviderRollbackIT.candidateFailureRoutesNewNonProductionStartsBackToAcceptedReference()` | `provider/candidate-rollback-v1` | Accepted reference revision/addressability + start-routing ledger | Candidate new start, production traffic/pointer mutation | New non-prod starts use accepted reference; in-flight reconciled | Required | `E-P12-ROLLBACK` |

| Test class | Exact method 후보 | Pass oracle |
|---|---|---|
| `ProviderConformanceCatalogTest` | `coversEveryAcceptedPortMethodStateActionFailureAndCapability()` | Accepted catalog uncovered item 0 |
| same | `usesOneCaseIdAndOracleForAwsAndCandidateSubjects()` | Duplicate provider assertion 0 |
| same | `sealsApplicableCaseSetBeforeEitherProviderRun()` | Post-result reclassification 0 |
| `ProviderConformanceApplicabilityConfigTest` | `sealsAxesCasesOracleAndRuntimeConfigBeforeProviderRuns()` | Immutable config digest |
| `ProviderSdkLeakageArchitectureTest` | `providerTypesAppearOnlyInApprovedAdapterDistributionAndDeploymentModules()` | Forbidden bytecode/import 0 |
| same | `coreDomainSolverVerificationApplicationDoNotDependOnConformanceOrProviderModules()` | Forbidden DAG edge 0 |
| same | `phase12DoesNotDependOnPhase13HybridOrVendorModules()` | Phase 13/vendor edge 0 |
| `ProviderNeutralSchemaStabilityTest` | `candidateAdapterDoesNotChangeArtifactIdentityStateOrResultSchema()` | Accepted schema fingerprints exact |
| same | `providerLocatorAndExecutionIdAreExcludedFromSemanticFingerprint()` | Metadata mutation invariant |
| `ProviderConfigValidationTest` | `missingProviderAxisRetryDeadlineSecurityOrPerformancePolicyFailsClosed()` | Construction rejected |
| same | `unapprovedProviderOrAxisCannotConstructSubject()` | Subject count 0 |
| same | `ambientCredentialOrDefaultProviderCannotConstructSubject()` | Provider call count 0 |

### 11.3 Storage/CAS/consistency test 후보

`ProviderStorageConformanceTest`는 AWS와 candidate subject에 같은 parameterized
method를 실행한다.

| Exact method 후보 | Pass oracle |
|---|---|
| `putIfAbsentCreatesOnceAndSameDigestConverges()` | One canonical ref |
| `sameKeyDifferentDigestIsIntegrityConflictWithoutOverwrite()` | Original unchanged |
| `readVerifiesKindSchemaLengthDigestBeforeDeserialization()` | Corruption rejected |
| `metadataAndReadDoNotDependOnPrefixListing()` | List call 0 |
| `initialStateCreateIfAbsentHasExactlyOneSubmissionWinner()` | One binding/`SolveId`/initial version |
| `sameSubmissionSameCanonicalDigestsReturnsSameSolveIdAndState()` | Exact same state, second create/start 0 |
| `sameSubmissionDifferentCanonicalDigestsIsIdempotencyConflict()` | Existing state unchanged |
| `lostCreateAcknowledgementConvergesByTenantScopedExactRead()` | Blind write/new solve 0 |
| `crashCannotExposePartialSubmissionBindingOrOrphanInitialState()` | Binding/state visibility atomic |
| `concurrentStateCasHasExactlyOneWinner()` | One new state/token |
| `staleStateVersionCannotOverwrite()` | Current unchanged |
| `lostCasAcknowledgementConvergesByExactRead()` | Second transition 0 |
| `runStateVersionCannotServeAsPublicationPrecondition()` | Compile/type separation |
| `publicationRequiresBothVerifierPassClosure()` | Missing/FAIL report rejected |
| `samePublicationDigestConvergesAndDifferentDigestConflicts()` | One pointer |
| `lostPublicationResponseConvergesByExactPointerRead()` | Second pointer transition 0 |
| `committedExactReadMeetsDeclaredConsistencyCapability()` | False missing/stale success 0 |
| `providerVersionTokenRemainsOpaqueAndUnordered()` | Parse/order attempt 0 |
| `crossTenantExactReadWriteAndCasAreDenied()` | All denied |

Actual integration:

```text
CandidateObjectStorageProviderIT.actualBackendPassesCompleteStorageConformanceSuite()
CandidateObjectStorageProviderIT.providerNativeConditionalRaceMatchesAbstractCasOracle()
CandidateObjectStorageProviderIT.indeterminateWriteIsReconciledWithoutBlindOverwrite()
```

### 11.4 Workflow, worker와 application integration test 후보

| Test class | Exact method 후보 | Pass oracle |
|---|---|---|
| `ProviderWorkflowConformanceTest` | `sameWorkflowKeySameManifestConverges()` | Same logical run |
| same | `sameWorkflowKeyDifferentManifestConflicts()` | Typed conflict |
| same | `sameSubmissionSameDigestsStartsExactlyOneLogicalWorkflow()` | Same solve/workflow, start count 1 |
| same | `sameSubmissionDifferentDigestsStartsNothingAndConflicts()` | Start count 0, state unchanged |
| same | `crashAfterInitialStateCommitReconcilesBeforeWorkflowStart()` | Exact read before one start |
| same | `crashAfterActionCasReplaysSameActionId()` | Same action receipt |
| same | `duplicateLostAndOutOfOrderWakeupsConverge()` | Same semantic trace |
| same | `executorRejectsActionNotMatchingExactCurrentPendingPayloadAndOrdinal()` | Side effect 0 |
| same | `missingDeclaredScreenBlocksWarmStartAndPhaseTwoDispatch()` | Warm-start/Phase-2 dispatch 0 |
| same | `missingDeclaredWorkerBlocksAggregationAndPublication()` | Pointer absent |
| same | `completionOrderNeverChangesChampionOrResult()` | Permutation equality |
| `ProviderWorkerConformanceTest` | `retryChangesAttemptOnlyAndPreservesAssignmentSeedWarmStartAndWork()` | Identity projection |
| same | `duplicateSameDigestCompletionConverges()` | One committed outcome |
| same | `duplicateDifferentDigestCompletionIsIntegrityFailure()` | Arbitrary winner 0 |
| same | `platformTimeoutResourceLimitAndThrottleAreNotNormalTermination()` | Typed exceptional |
| same | `stopIntentAndActualTerminationRemainSeparate()` | Distinct observations |
| `ProviderApplicationParityIT` | `sameManifestProducesSameCanonicalArtifactsResultAndTermination()` | AWS/candidate projection |
| same | `bothGatePublicationIsTheOnlySuccessPath()` | Fault matrix |
| `ProviderEndToEndParityIT` | `localAwsAndCandidateAgreeOnProviderNeutralExecutionProjection()` | Three-way oracle |

### 11.5 Fault, corruption, recovery와 reproducibility test 후보

| Test class | Exact method 후보 | Pass oracle |
|---|---|---|
| `ProviderFailureTaxonomyContractTest` | `mapsEveryProviderFaultToOneNeutralCodeAndRetryDisposition()` | Exhaustive table |
| same | `unknownProviderStatusFailsClosedAsProtocolViolation()` | Success false |
| `ProviderRetryIdentityIT` | `transportDeliveryAndApplicationRetriesRemainDistinct()` | Layered trace |
| same | `boundedRetryExhaustionEndsTypedWithoutPublication()` | Call bound + pointer absent |
| `ProviderCancellationDeadlineIT` | `cancelPublishRaceHasExactlyOneAuthoritativeTerminalIntent()` | Same-state CAS |
| same | `deadlineCannotChangeSeedComparatorStepsOrResultFingerprint()` | Semantic equality |
| `ProviderDeadlineRestartIT` | `restartUsesApprovedDurableOriginReconciliationOrFailsClosed()` | No hidden fallback |
| `ProviderCorruptionIT` | `mutatedBytesLengthSchemaOrAuthorityRefAreRejectedBeforeUse()` | Independent codec/digest |
| same | `poisonedProviderMetadataCannotChangeSemanticArtifactIdentity()` | Projection unchanged |
| `ProviderOperationalRecoveryIT` | `reentryAfterProcessLossResumesFromExactCommittedState()` | Duplicate logical work 0 |
| same | `lateSuccessAfterExceptionalTerminalCannotPublish()` | Terminal/pointer unchanged |
| `ProviderCanonicalCodecTest` | `sdkDtoAndJavaSerializationAreRejectedArtifactFormats()` | Codec allowlist |
| `ProviderReproducibilityIT` | `sameStrongEnvelopeProducesSameSemanticTraceAndResult()` | Exact replay |
| same | `differentProviderCompletionOrderDoesNotChangeReplayProjection()` | Permutation equality |
| same | `differentVerifiedDigestForSameStrongReplayIdentityIsIntegrityFailure()` | Winner 0 |

### 11.6 Migration, security, observability와 performance test 후보

| Test class | Exact method 후보 | Pass oracle |
|---|---|---|
| `ProviderArtifactMigrationIT` | `copyVerifiesSourcePutIfAbsentReadBackAndCompleteClosure()` | Receipt per declared ref |
| same | `sameLogicalKeyDifferentDigestStopsBeforePointerProposal()` | Both pointers unchanged |
| `ProviderShadowIsolationIT` | `shadowRunCannotPublishMutateSourceStateOrReceiveProductionTraffic()` | Authority calls 0 |
| `ProviderRollbackIT` | `candidateFailureRoutesNewNonProductionStartsBackToAcceptedReference()` | Accepted reference selected; candidate starts 0 |
| `ProviderRollbackIT` | `rollbackPreservesImmutableArtifactsAndReconcilesInflightState()` | Delete/overwrite 0 |
| `ProviderSecurityControlIT` | `workerCannotReadOtherTenantWriteChampionOrStartWorkflow()` | Provider-native deny |
| same | `missingOrMismatchedExplicitAccessBindingCannotUseAmbientCredential()` | Backend call 0 |
| same | `apiCannotReadUnpublishedResultOrWriteWorkerOutcome()` | Provider-native deny |
| same | `workflowIdentityCannotReadBusinessArtifactsOrDecryptPayload()` | Provider-native deny |
| same | `wildcardDataPlanePrivilegeAndApplicationOnlyTenantCheckAreRejected()` | Subject/config rejected |
| same | `secretPiiPayloadAndProviderCredentialNeverAppearInEvidenceLogsOrTraces()` | Forbidden match 0 |
| `ProviderFailureCarrierIT` | `deniedMissingCorruptStaleAndIndeterminateRemainDistinctEndToEnd()` | No-collapse mapping |
| `ProviderObservabilityIT` | `requiredCorrelationStateFailureRetryAndVerificationEventsAreEmitted()` | Event catalog complete |
| same | `providerMetricNamespaceMapsWithoutChangingSemanticEventCode()` | Neutral code exact |
| same | `alarmsCoverIntegrityAccessRetryExhaustionTimeoutAndIncompleteWork()` | Alarm catalog exact |
| `ProviderPerformanceEnvelopeIT` | `recordsColdWarmLatencyThroughputMemoryConcurrencyAndErrorVector()` | Complete schema |
| same | `comparesSameManifestWorkAndArtifactSizesAcrossProviders()` | Workload identity exact |
| same | `missingApprovedThresholdReturnsGatedNotPass()` | `GATED_MISSING_POLICY` |
| same | `deadlineReserveCanCompleteFinalVerificationAndPublication()` | Approved reserve or typed non-pass |
| `ProviderCostEvidenceIT` | `recordsRequestComputeStorageTransferObservabilityAndOperationalCostVector()` | Complete vector |
| same | `doesNotConvertExperimentEstimateIntoProductionDefault()` | Authority false |

### 11.7 Evidence graph test 후보

| Test class | Exact method 후보 | Pass oracle |
|---|---|---|
| `ProviderEvidenceManifestTest` | `awsAndCandidateEvidenceHaveNoConformanceReviewOrReceiptBackReference()` | Forbidden edge 0 |
| same | `eachEvidenceBindsExecutedApplicabilityConfigAndHasDistinctContentDigest()` | Config exact, digests distinct |
| `ProviderConformanceManifestTest` | `forwardReferencesApplicabilityAndExactlyTwoProviderEvidenceDigests()` | Config + two evidence |
| same | `containsNoReviewOrAcceptanceReference()` | Forbidden edge 0 |
| `IndependentPhase12ReviewTest` | `referencesReviewedConformanceManifestDigestButNoProviderEvidence()` | One manifest ref |
| `Phase12AcceptanceReceiptTest` | `referencesExactConformanceManifestAndIndependentReviewDigests()` | Two exact refs |
| same | `rejectsEvidenceManifestReviewOrReceiptCycle()` | Graph acyclic |
| same | `rejectsMismatchedManifestOrReviewDigest()` | Mismatch rejected |
| same | `rejectsProductionAuthorityTrue()` | Authority false |

### 11.8 Red → green 순서

1. Canonical capability 22개와 exact oracle 95개 seed count/hash를 먼저 검증한다.
2. Missing conformance module에서 catalog/architecture test를 red로 고정한다.
3. Initial submission create/idempotency/crash oracle를 fake subject에서 red로 고정한다.
4. Hand oracle와 deterministic fake subject로 assertions를 green한다.
5. AWS reference subject가 같은 assertions에 각각 통과하는지 확인한다.
6. Adoption record 없이는 candidate construction이 red인지 확인한다.
7. Approved minimal axis module을 추가하고 capability probe를 actual environment에서
   실행한다.
8. Required unsupported이면 typed rejection을 green으로 판정하되 adoption은 reject하고
   이후 eligible/full-suite 단계로 진행하지 않는다.
9. Applicable contract를 unit → deterministic fake → actual provider 순서로 green한다.
10. Fault/corruption/retry/deadline/cancel cases를 green한다.
11. IAM/encryption/tenant/redaction negative cases를 actual provider에서 green한다.
12. Copy/shadow/rollback을 source authority 불변으로 green한다.
13. 각 subject independent oracle pass 뒤 parity/performance를 실행한다.
14. Fresh Surefire/Failsafe XML을 sealed exact manifest와 reconcile한다.
15. Provider evidence를 독립 seal한 뒤 manifest → review → receipt test를 green한다.

“Green”은 항상 성공 추천을 뜻하지 않는다. `Unsupported`를 정확히 판정하고 안전하게
중단하는 test도 green이다.

### 11.9 False-green 방지

- Unit `*Test`는 Surefire `-Dtest`와
  `-Dsurefire.failIfNoSpecifiedTests=true`로 선택한다.
- Integration `*IT`는 Failsafe `-Dit.test`와
  `-Dfailsafe.failIfNoSpecifiedTests=true`로 선택한다.
- 먼저 root에서 `-pl build/provider-conformance-tests -am clean install`로
  conformance module과 upstream dependency가 실제 reactor에 포함되는지 검증한다.
- 그 다음 exact owner POM에서 no-`-am` standalone `clean verify`, filter 없는
  `provider-integration`/`provider-parity` full profile, 마지막으로 selected filter
  run을 각각 실행한다.
- Selected module은 exact owner POM에서 실행하고 stale `target` report를 섞지 않는다.
- Run 시작 전 report directory가 없음을 확인하고, fresh XML의 path/timestamp/run ID,
  source/POM/profile digest와 discovered/executed/passed/failed/error/skipped count를
  sealed exact manifest와 대조한다.
- Canonical method count `95`, method-name seed SHA-256
  `9efdc487e7f059c0362fa5e3cfdfeeb65d265f8b2193f8cb02161653547efb40`,
  missing/unexpected/duplicate 0을 검증한다.
- Required case skip, disabled profile, 한 provider report 부재, emulator-only result,
  stale XML은 pass가 아니다.
- Subject output을 expected로 복사하지 않고 mutation/defect-sensitivity test를 둔다.
- Performance fail을 semantic pass와 합산하지 않는다.

### 11.10 Test 종류별 적용성과 pass 판정

| Test 종류 | Phase 12 적용 | Pass 판정 | 미적용/조건부 이유 |
|---|---|---|---|
| Unit | 필수 | Codec/config/failure mapping 모든 case pass | 없음 |
| Contract | 필수 | AWS/candidate가 같은 suite 개별 pass | 없음 |
| Integration | 필수 | Actual approved provider environment에서 Failsafe pass | Provider 미승인 현재는 실행 불가 |
| E2E | 필수 | Local oracle↔AWS↔candidate semantic mismatch 0 | Entry gate 뒤 |
| Architecture | 필수 | SDK/vendor/Phase 13 leakage 0 | 없음 |
| Fault | 필수 | Retry/cancel/timeout/indeterminate가 typed last-safe state | 없음 |
| Corruption | 필수 | Bytes/schema/authority/token mutation 모두 fail closed | 없음 |
| Reproducibility | 필수 | Same strong envelope semantic trace/result exact | Timeboxed는 별도 통계 class |
| Security | 필수 | Role×operation deny, tenant disclosure, secret leak 0 | Actual environment 필요 |
| Performance | 조건부 hard gate | Approved envelope pass 또는 typed gated/reject | 승인 threshold 없으면 pass 불가 |
| Cost | 조건부 evidence | Complete vector + approved policy or gated | 가격 timestamp/workload policy 필요 |
| Migration | `STORAGE` axis일 때 필수 | Declared closure/digest exact, source mutation 0 | Storage 미교체면 N/A를 pre-sealed config가 결정 |
| Workflow | Workflow axis일 때 필수 | Durable re-entry/action/completeness pass | Axis 미승인이면 실행 전 N/A |
| Compute | Compute axis일 때 필수 | Retry identity/status/stop/timeout pass | Axis 미승인이면 실행 전 N/A |

N/A는 실행 뒤 편의를 위해 고르지 않는다. Approved axes와 accepted contract version이
applicability를 실행 전에 결정한다.

### 11.11 전체 pass criteria

- Build/test exit code 0, discovered required test count exact, required skip 0
- Canonical capability catalog `22/22`, uncovered/duplicate/unknown/unowned 0
- Canonical exact method seed `95/95`와 SHA-256 exact; required
  discovered/executed/passed 일치, missing/unexpected/duplicate 0
- Root `-pl ... -am clean` reactor manifest, standalone owner-POM run과 filter 없는
  integration/parity profile가 모두 동일 source/POM/profile digest에 bind
- Fresh Surefire/Failsafe report만 사용하고 stale/foreign run report 0
- Same pre-sealed case/config/oracle을 두 subject에 적용
- 각 subject independent oracle pass 100%, 그 뒤 semantic mismatch 0
- Provider SDK/type leakage와 Phase 13/vendor dependency 0
- Artifact/schema/identity/result mismatch 0
- CAS lost update, overwrite, list authority, partial publication 0
- Initial submission same/same은 같은 `SolveId`/state, same/different는 conflict;
  concurrent create one winner, partial binding/orphan/reset/blind retry 0
- Run-state/action/publication token-domain 혼용 0
- Ambient credential fallback, tenant disclosure, failure-code collapse 0
- Restart deadline hidden wall-clock/provider-time fallback 0
- Retry의 logical identity/seed/warm start/work drift 0
- Platform/cancel/resource failure의 normal termination 오분류 0
- Corrupt/unknown schema/protocol false success 0
- Required security operation allow와 forbidden log match 0
- Migration source pointer/state/traffic mutation 0
- Missing performance/security policy의 false pass 0
- Evidence DAG forbidden edge/cycle/digest mismatch 0
- `productionAuthority=true`, provider default/traffic mutation 0
- Independent review와 valid separate receipt

## 12. Maven과 검증 명령 — 현재와 FUTURE를 구분하기

### 12.1 현재 frozen inventory에서 사실인 것

HEAD에는 wrapper/reactor가 없고 live snapshot에는 미커밋 wrapper와 Phase 00 reactor
scaffold가 있다. 그러나 다음은 여전히 부재한다.

```text
build/provider-conformance-tests/pom.xml
adapters/<approved-provider>/
distributions/<approved-distribution>/
deployment/<approved-provider>/
Failsafe integration lifecycle
Phase 08~11 accepted implementation/evidence
```

따라서 아래 read-only inventory 명령은 지금 실행 가능하지만 Phase 12 test 명령은
아직 completion evidence가 될 수 없다.

```bash
test -f pom.xml
test -f mvnw
test -f build/architecture-rules/pom.xml
test ! -e build/provider-conformance-tests/pom.xml

rg --files rpdptw build legacy | sort
git status --short --branch
```

Live `./mvnw clean verify`가 성공하더라도 Phase 00 concurrent scaffold의 결과이며
Phase 12 provider parity가 아니다.

Correction-time live drift에는 wrapper/reactor/test-jar가 보이지만 Phase 00 review 02
acceptance receipt가 없고 Failsafe/profile/conformance module도 없다. 따라서
“현재 실행 가능”과 “Phase 12 authority가 있음”을 분리하며, 아래 command를 지금
실행해 completion으로 기록하지 않는다.

### 12.2 Entry gate 뒤의 FUTURE command sequence

Accepted Phase 00 wrapper/plugin policy와 Phase 12 modules가 생긴 뒤에만 실행한다.

```bash
# 1. Root reactor가 module과 upstream dependency를 실제 포함하는지 clean install
./mvnw -pl build/provider-conformance-tests -am clean install

# 2. Architecture와 unit full suite; filter 없이 먼저 실행
./mvnw -pl build/architecture-rules -am clean test

# Selected architecture diagnostic은 exact owner POM에서 no-am
./mvnw -f build/architecture-rules/pom.xml clean test \
  -Dtest=ProviderSdkLeakageArchitectureTest,ProviderNeutralSchemaStabilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true

./mvnw -f build/provider-conformance-tests/pom.xml clean test

# 3. Selected unit diagnostics도 zero-test fail-closed
./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderCanonicalOracleManifestTest,ProviderConformanceCatalogTest,ProviderConformanceApplicabilityConfigTest,ProviderStorageConformanceTest,ProviderWorkflowConformanceTest,ProviderWorkerConformanceTest \
  -Dsurefire.failIfNoSpecifiedTests=true

./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderFailureTaxonomyContractTest,ProviderCanonicalCodecTest,ProviderConformanceManifestTest,Phase12AcceptanceReceiptTest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

각 `clean test`의 verify-phase report closure가 필요하면 accepted Phase 00 build
contract가 지정한 `verify` command를 사용한다. Test report를 별도 수동 합산해
Maven exit code와 분리하지 않는다.

Approved isolated candidate environment에서는 먼저 filter 없는 profile full suite를
root reactor `-am clean`과 exact owner POM no-`-am`으로 각각 실행한다.

```bash
./mvnw -pl build/provider-conformance-tests -am \
  -Pprovider-integration clean verify \
  -Dprovider.adoptionDecisionRef=<approved-ref> \
  -Dprovider.environmentRef=<approved-isolated-environment-ref> \
  -Dprovider.evidenceDir=build/provider-conformance-tests/target/phase12-evidence

./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dprovider.adoptionDecisionRef=<approved-ref> \
  -Dprovider.environmentRef=<approved-isolated-environment-ref> \
  -Dprovider.evidenceDir=target/phase12-evidence

# Selected diagnostic after full-suite success
./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-integration clean verify \
  -Dit.test=ProviderCapabilityProbeIT,ProviderSecurityControlIT,ProviderObservabilityIT,ProviderOperationalRecoveryIT \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dprovider.adoptionDecisionRef=<approved-ref> \
  -Dprovider.environmentRef=<approved-isolated-environment-ref> \
  -Dprovider.evidenceDir=target/phase12-evidence-selected
```

Approved AWS/candidate evidence refs가 모두 있을 때 parity도 같은 순서를 지킨다.

```bash
./mvnw -pl build/provider-conformance-tests -am \
  -Pprovider-parity clean verify \
  -Dprovider.awsEvidenceRef=<accepted-phase11-ref> \
  -Dprovider.candidateEvidenceRef=<candidate-ref> \
  -Dprovider.evidenceDir=build/provider-conformance-tests/target/phase12-comparison

./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-parity clean verify \
  -Dprovider.awsEvidenceRef=<accepted-phase11-ref> \
  -Dprovider.candidateEvidenceRef=<candidate-ref> \
  -Dprovider.evidenceDir=target/phase12-comparison

# Selected diagnostic after filter-free parity success
./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-parity clean verify \
  -Dit.test=ProviderApplicationParityIT,ProviderEndToEndParityIT,ProviderReproducibilityIT,ProviderDeadlineRestartIT,ProviderCorruptionIT,ProviderArtifactMigrationIT,ProviderShadowIsolationIT,ProviderRollbackIT,ProviderPerformanceEnvelopeIT,ProviderCostEvidenceIT \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dprovider.awsEvidenceRef=<accepted-phase11-ref> \
  -Dprovider.candidateEvidenceRef=<candidate-ref> \
  -Dprovider.evidenceDir=target/phase12-comparison-selected
```

Direct child-POM selected diagnostic은 root `-am clean` full-slice 성공을 대신하지
못하므로 단독 acceptance command가 아니다. Angle bracket 값은 승인 record에서
resolve한다. Literal로 실행하거나 environment default로 보완하지 않는다. 이
guide에는 production deploy/cutover command가 없다.

각 full/selected run의 `verify`가 다음을 모두 확인해야 exit 0이다.

1. Expected reactor module/artifact와 fixture test-jar가 exact POM digest로 resolve됐다.
2. Surefire/Failsafe provider와 lifecycle execution ID가 expected manifest와 같다.
3. Report XML의 run ID/timestamp가 이번 `clean` 이후이고 다른 module report가 0이다.
4. Canonical 95-method seed hash와 sealed exact execution manifest hash가 일치한다.
5. Expected/discovered/executed/pass/fail/error/skip/blocked/N/A/missing/unexpected/
   duplicate가 reconciliation rule을 만족한다.
6. AWS/candidate 두 subject report와 actual-provider-required evidence가 모두 있다.

### 12.3 명령 실패를 해석하는 법

| 실패 | 먼저 의심할 것 | 하면 안 되는 대응 |
|---|---|---|
| POM 없음 | Entry/implementation이 아직 안 됨 | 임시 POM을 무승인 생성 |
| Selected tests 0 | Surefire/Failsafe 선택/config 오류 | `failIfNoSpecifiedTests=false` |
| Profile disabled | Environment/authority 누락 | Emulator 결과로 대체 |
| Capability unsupported | Provider 부적합 | Port 완화/emulation |
| Integration deny | IAM/tenant control 또는 binding 문제 | Broad role 부여 |
| Timeout | Platform/retry/deadline contract | Normal ALNS termination으로 변환 |
| Digest mismatch | Corruption/identity defect | Overwrite/re-copy 무한 retry |
| Performance policy 없음 | Owner 승인 부재 | Test constant로 pass |

## 13. 사람 checkpoint, evidence, stop과 resume

### 13.1 WP별 checkpoint

| Checkpoint | 사람이 확인할 evidence | Stop 조건 | Resume 조건 |
|---|---|---|---|
| CP12-0 Entry | Exact source/predecessor/adoption receipt | 하나라도 missing/unaccepted | 모든 entry ref accepted |
| CP12-1 Catalog | Case/capability/contract coverage와 sealed config | Signature drift, oracle coupling | Cross-phase owner compatibility 승인 |
| CP12-2 Capability | Actual provider probe와 architecture report | Required unsupported/indeterminate | Provider/approved mechanism 변경 또는 evidence 보완 |
| CP12-3 Storage | Actual CAS/visibility/digest/tenant suite | Overwrite/stale/list/deny 실패 | Contract를 약화하지 않은 actual fix |
| CP12-4 Workflow | Action/retry/completeness/cancel/deadline suite | Semantic decision이 provider에 있음 | Thin mapping + accepted fence |
| CP12-5 Fault/replay | Exhaustive map, codec, crash/replay | Failure collapse/hidden deadline fallback | Lossless mapping + durable ADR |
| CP12-6 Security/Ops | Role matrix, log scan, alarms/recovery | Broad role/leak/missing alarm | Reviewed policy와 negative evidence |
| CP12-7 Migration | Copy receipts, shadow isolation, rollback | Digest conflict/source mutation | Quarantine + corrected new chain |
| CP12-8 Parity | Each-subject oracle pass와 comparison | Skip/mismatch/missing policy | New immutable run/evidence |
| CP12-9 Review | Manifest/review/receipt exact DAG | Cycle, digest mismatch, non-accepted verdict | New version chain + accepted review |

Checkpoint는 구현자가 혼자 서명하지 않는다. Contract, security, operations,
performance, review와 status authority 역할을 분리한다.

### 13.2 Evidence DAG

허용되는 content-reference와 seal 순서는 다음뿐이다.

```text
ProviderConformanceApplicabilityConfig
  ├─→ standalone AWS ProviderEvidenceManifest
  └─→ standalone candidate ProviderEvidenceManifest

config digest + AWS evidence digest + candidate evidence digest
  → ProviderConformanceManifest
  → IndependentPhase12Review(manifest digest)

manifest digest + review digest
  → Phase12AcceptanceReceipt
```

금지 edge:

```text
provider evidence → conformance manifest | review | receipt
conformance manifest → review | receipt
review → provider evidence | receipt
receipt → provider evidence
```

Review finding으로 evidence나 manifest가 바뀌면 기존 bytes를 수정하지 않는다. Affected
provider evidence부터 새 content digest로 다시 seal한다.

### 13.3 Evidence artifact 최소 항목

`ProviderConformanceApplicabilityConfig`:

```text
accepted port/artifact/state/identity versions
approved axes/environment/runtime config
canonical capability count = 22
capability applicability rows with owner/source/case/negative-control/evidence schema
required capability set derived from applicability rows
pre-sealed applicable case IDs
independent oracle version
canonical exact method count = 95
canonical method-name seed SHA-256
sealed exact oracle manifest content SHA-256
provider metadata exclusion projection
security/operations/performance evidence schema
productionAuthority = false
contentDigest
```

각 `ProviderEvidenceManifest`:

```text
providerRole = AWS_REFERENCE | CANDIDATE
provider identity projection
executed applicability config digest
case evidence digests
capability decision matrix digest
canonical method seed and exact execution manifest digests
expected/discovered/executed/pass/fail/error/skip/blocked/N/A counts
missing/unexpected/duplicate exact method IDs
fresh Surefire/Failsafe report digests and run/source/POM/profile identity
independent oracle assertion digest
artifact/state/identity/failure evidence
initial submission create/idempotency/concurrency/lost-ack/crash-recovery evidence
security/observability/operations/performance evidence
unsupported/gated/open differences
no conformance/review/receipt reference
productionAuthority = false
contentDigest
```

`ProviderConformanceManifest`:

```text
entry/adoption/source refs
approved axes
applicability config digest
distinct AWS and candidate evidence content digests
contract/parity/failure/replay/security/ops/performance/migration/rollback refs
capability applicability/decision closure and exact-oracle execution refs
initial run-state/submission idempotency evidence ref
cross-phase blocker resolution refs
unsupported/gated/open differences
adoption recommendation
no review/acceptance reference
productionAuthority = false
contentDigest
```

`IndependentPhase12Review`와 `Phase12AcceptanceReceipt`:

```text
review:
  reviewed conformance manifest digest
  verdict/findings/blockers
  no direct provider-evidence reference
  productionAuthorityGranted = false

receipt:
  exact conformance manifest digest
  exact independent review digest/verdict
  unresolved blockers/handoff disposition
  productionAuthority = false
```

### 13.4 Stop note template

작업을 멈출 때 다음을 남긴다.

```text
phase: 12
work_package:
snapshot_commit:
frozen_live_inventory_blob:
entry_receipt_ref:
approved_provider_and_axes:
last_completed_case_id:
last_immutable_evidence_digest:
blocker:
owner:
last_safe_state:
forbidden_next_action:
restart_condition:
source_refs_and_changed_headings:
rollback_or_quarantine_ref:
productionAuthority: false
```

“Provider API가 예상과 달라서 막힘”처럼 쓰지 않는다. 어떤 required capability,
operation, neutral failure, evidence가 부족한지 적는다.

### 13.5 상태 변경 권한

구현자는 evidence proposal을 제출한다. Independent reviewer는 verdict를 제출한다.
총괄 scheduler만 [Execution Progress §9](../../execution-progress-and-results.md#9-scheduler-update-protocol)에
따라 authoritative registry/status를 변경한다. Guide, test green, deployment
resource 또는 recommendation만으로 `READY`, `ACCEPTED`, `PRODUCTION`을 선언하지 않는다.

## 14. 흔한 오해와 금지 anti-pattern

| Anti-pattern | 왜 깨지는가 | 안전한 대안 |
|---|---|---|
| 예시 목록에서 GCP/ECS/Kubernetes를 임의 선택 | Adoption authority 위반 | Exact decision receipt 대기 |
| Legacy GCP를 candidate baseline으로 복사 | Placeholder를 target authority로 승격 | Characterization negative fixture로만 사용 |
| Candidate 제약에 맞춰 port/state/schema 변경 | Substitution이 semantic fork가 됨 | Unsupported/reject |
| AWS output을 golden expected로 사용 | Shared bug를 못 잡음 | Independent hand/reference oracle |
| Provider URI/ETag/task ID를 fingerprint에 포함 | Migration 때 identity drift | Observation projection으로 분리 |
| CAS를 check-then-write/local lock/list로 흉냄 | Distributed lost update | Provider-native atomic primitive |
| Submission index와 initial state를 따로 check-then-write | 같은 submission에 두 solve/orphan state 발생 | Tenant-scoped atomic create binding + exact-read reconciliation |
| Lost create ack 뒤 새 `SolveId` 발급/state reset | Retry가 새 logical run을 만듦 | Same identity exact read, same/same convergence |
| Run-state token을 action/publication precondition으로 재사용 | Authority/linearization 혼합 | Typed current action + distinct pointer precondition |
| Cancel intent object를 race winner로 사용 | 다른 key CAS 둘 다 성공 가능 | Same-run-state cancel/publish fence |
| Completion order/list로 completeness 판단 | Partial success publish | Declared exact worker set |
| Workflow에서 score/verifier 판단 | Domain authority 복제 | Thin action mapping |
| Retry 때 seed/warm start/work 변경 | Reproducibility 붕괴 | Same logical assignment, new AttemptId only |
| Timeout을 normal max-step으로 변환 | Platform failure 숨김 | Typed exceptional termination |
| Denied/corrupt/stale/indeterminate를 generic error로 축소 | Security/retry/integrity 손실 | Exhaustive neutral failure carrier |
| Java serialization/SDK DTO 저장 | Schema/replay/provider lock-in | Versioned canonical codec |
| Encryption으로 digest를 대체 | Confidentiality와 integrity 혼동 | 두 control 독립 검증 |
| Broad role + tenant string check | Provider-native isolation 부재 | Scoped non-ambient binding |
| Restart deadline을 wall clock으로 복원 | Provider/process마다 의미 drift | Approved durable reconciliation |
| Provider별 test copy에서 assertion 삭제 | Parity evidence 조작 | One parameterized suite |
| Emulator pass를 actual evidence로 표기 | Provider semantics 미검증 | Isolated provider IT |
| 빠르다는 이유로 security/semantic fail 허용 | Gate 순서 위반 | Hard gate 뒤 성능 평가 |
| Test-only threshold를 official default로 승격 | `Q-BENCH-02` 임의 해소 | External approved policy |
| Shadow에 publication/traffic 허용 | Cutover를 Phase 12로 당김 | Authority-zero shadow |
| Phase 13/OR-Tools도 함께 구현 | `C-17` 위반 | Bounded infra handoff only |
| Evidence/review reciprocal hash | Immutable seal 순환 | Forward DAG |
| `*IT`를 Surefire만으로 실행 | 0-test false green | Failsafe + zero-test fail |
| `src/testFixtures/java`를 Maven 기본 test source로 가정 | Fixture가 compile되지 않아 suite가 비어도 모름 | Accepted test-jar 또는 pinned approved source-root 하나 |
| Child POM direct success만으로 reactor 포함 주장 | Root build가 module을 건너뛸 수 있음 | Root `-pl ... -am clean` + standalone owner POM 둘 다 |
| Class count만 보고 exact oracle complete 주장 | Canonical method 누락을 숨김 | 95-method seed hash + fresh XML method reconciliation |
| Resource 생성으로 completion 주장 | Contract/evidence 없음 | Full DoD + receipt |

## 15. 실제 구현 exit checklist와 Definition of Done

### 15.1 Entry와 scope

- [ ] Phase 08~11 exact accepted evidence/review/receipt가 있다.
- [ ] Candidate provider, axes, non-prod scope, owner와 review boundary가 승인됐다.
- [ ] Required capability set, environment, security/ops/performance policy가 explicit하다.
- [ ] Unapproved provider/module/resource와 production action은 0이다.
- [ ] Canonical source drift와 Final Domain/Architecture Q-INFRA drift가 숨겨지지 않았다.

### 15.2 Architecture와 contract

- [ ] Phase 00 accepted receipt가 root/build aggregation, test-fixture 방식, pinned
  Surefire/Failsafe와 verify-phase report closure를 승인한다.
- [ ] Root `-pl build/provider-conformance-tests -am clean`과 standalone owner POM
  full-suite가 같은 source/POM/profile digest로 통과한다.
- [ ] Stable modules의 provider SDK/type reference는 0이다.
- [ ] Candidate/AWS adapter가 서로 compile-depend하지 않는다.
- [ ] Phase 12가 Phase 13 hybrid/vendor module에 의존하지 않는다.
- [ ] Artifact/state/result schema와 semantic identity 변경은 0이다.
- [ ] Same pre-sealed case/config/oracle을 두 subject에 사용한다.
- [ ] Applicable case skip과 사후 reclassification은 0이다.
- [ ] Canonical capability catalog는 `22/22`이고 각 행에 decision owner, source,
  case, negative control과 actual evidence schema가 있다.
- [ ] Canonical exact method seed는 `95/95`와 고정 SHA-256이 일치하고 fresh
  Surefire/Failsafe report의 missing/unexpected/duplicate가 0이다.

### 15.3 Storage, workflow, failure와 replay

- [ ] Applicable storage suite가 actual provider에서 통과한다.
- [ ] Initial submission create가 `ABSENT → SUBMITTED` 한 번만 수행되고 same/same은
  같은 `SolveId`/state, same/different는 mutation 없는 `IDEMPOTENCY_CONFLICT`다.
- [ ] Concurrent create, lost acknowledgement와 crash boundary에서 orphan binding/state,
  blind overwrite, reset과 duplicate workflow start가 0이다.
- [ ] Put-if-absent, exact read, state CAS, distinct publication CAS가 보존된다.
- [ ] Exact current action, declared completeness, retry identity와 same-state cancel fence가 보존된다.
- [ ] Platform timeout/resource/cancel은 normal termination으로 분류되지 않는다.
- [ ] Failure carrier는 denied/missing/corrupt/stale/conflict/indeterminate를 보존한다.
- [ ] Canonical codec, corruption, replay와 durable restart contract가 통과한다.

### 15.4 Security, operations, performance와 migration

- [ ] Non-ambient authorization과 role×operation negative matrix가 통과한다.
- [ ] Tenant existence disclosure, wildcard data role, secret/PII log match가 0이다.
- [ ] Encryption과 digest가 독립적으로 검증된다.
- [ ] Required event/alarm/recovery evidence가 완전하다.
- [ ] Final verification/publication deadline reserve와 candidate new-start rollback
  oracle가 actual evidence에서 통과한다.
- [ ] Approved performance/cost envelope가 pass하거나 typed gated/rejected다.
- [ ] Copy receipt coverage 100%, digest mismatch/source pointer mutation 0이다.
- [ ] Shadow는 publication/state/traffic authority 0이다.
- [ ] Rollback이 immutable artifact를 삭제/overwrite하지 않는다.

### 15.5 Evidence와 authority

- [ ] AWS/candidate evidence가 conformance/review/receipt back-reference 없이 먼저 seal됐다.
- [ ] 두 provider evidence가 capability decision matrix, exact oracle manifest,
  fresh report와 initial-state idempotency evidence digest를 가진다.
- [ ] Conformance manifest가 config와 distinct provider evidence 두 digest를 forward-reference한다.
- [ ] Review는 exact manifest digest만 권위 입력으로 가진다.
- [ ] Receipt는 exact manifest/review digest를 각각 한 번 참조한다.
- [ ] Evidence DAG cycle/digest mismatch가 0이다.
- [ ] Independent review verdict가 acceptance 조건을 만족한다.
- [ ] Recommendation, handoff와 receipt 모두 `productionAuthority=false`다.
- [ ] `Q-BENCH-02`, `C-17`, `Q-VAR-01` 상태가 보존됐다.

### 15.6 DoD 문장

Phase 12는 “candidate provider code가 존재한다”가 아니라 다음이 모두 참일 때만
`ACCEPTED` 후보다.

> 승인된 provider/axis가 AWS reference와 같은 실행 전 봉인된 contract/case/oracle을
> actual environment에서 각각 통과하고, semantic identity·artifact·state·failure·
> security·operations·performance·migration·rollback 차이가 명시적으로 판정되며,
> standalone provider evidence → conformance manifest → independent review →
> acceptance receipt의 exact 단방향 DAG가 완성되고 production authority가 계속
> false다.

Required capability가 unsupported여서 provider adoption을 reject하는 것도 안전한
Phase 12 실행 결과일 수 있다. 그러나 그 provider를 `ACCEPTED/ELIGIBLE`로 표시할 수는
없다.

## 16. 다음 Phase handoff와 broken-contract 증상

### 16.1 Phase 11에서 받아야 할 것

```text
standalone Phase11EvidenceManifest content digest
Phase11PostReviewAcceptanceReceipt content digest
accepted logical port/artifact/state versions
identity/retry projection
non-ambient authorization + lossless failure projection
exact current pending action authorization
distinct publication-pointer precondition
same-run-state cancel/publication fence
durable deadline restart contract
storage/workflow/compute case catalog
local oracle + actual AWS evidence refs
provider metadata exclusion projection
security/operations/cost limitations
productionAuthority = false
```

현재 Phase 11은 review `CHANGES_REQUIRED`, implementation/evidence
`NOT_STARTED/NOT_PRODUCED`, handoff `NOT_READY`이므로 이 목록은 아직 future contract다.

### 16.2 Phase 13으로 넘기는 bounded evidence

Phase 13은 selected runtime이 substituted provider일 때만 다음을 조건부로 소비한다.

```text
Phase12ToPhase13SubstitutionEvidence
  conformance manifest digest
  independent review digest
  acceptance receipt digest
  evaluated execution envelope
  runtime limits/headroom
  artifact movement/latency/cost envelope
  retry/deadline/cancel semantics ref
  serialization/replay/security/ops refs
  unsupported/gated/open differences
  evidenceApplicability = INFRASTRUCTURE_DECISION_INPUT_ONLY
  c17Approval = false
  ortoolsActivationOrDistributionApproval = false
  hybridImplementationAuthority = false
  productionCutoverAuthority = false
```

Phase 13의 보편 entry는 Phase 06/07/08 acceptance, Phase 14A
`ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, 별도 `C-17`/backend/security/ops/cost 승인이다.
Phase 12 evidence는 이를 대체하지 않는다.

### 16.3 Phase 14B로 넘기는 것

Selected substituted provider가 official plan에 들어갈 때:

```text
applicable Phase 12 conformance/evidence/review/receipt
provider deployment identity and limitations
security/operations/performance/cost evidence
artifact migration and shadow receipts
exact rollback point/rehearsal
productionAuthority = false
```

Phase 14B가 별도의 signed provider deployment, official values/manifest,
`G14-PRODUCTION-AUTHORITY`와 pointer/traffic action receipt를 요구한다. Phase 12
recommendation으로 이 gate를 열지 않는다.

### 16.4 Broken-contract 증상

다음은 handoff가 깨졌다는 운영/코드 증상이다.

- 같은 logical run인데 provider만 바꾸면 `SolveId`/artifact/result fingerprint가 바뀜
- Candidate case catalog가 AWS catalog보다 작거나 skip 수가 다름
- Both providers는 같지만 hand oracle/verifier가 fail
- Same submission same/same에서 `SolveId`/initial state가 둘 생기거나
  same/different가 기존 state에 합쳐짐
- Create ack loss/crash 뒤 orphan binding/state 또는 duplicate workflow start가 생김
- Event arrival order를 바꾸면 champion/result가 달라짐
- Lost acknowledgement 뒤 duplicate state transition/pointer write가 발생
- Cancel과 publish가 모두 authoritative success로 남음
- Same `WorkerRunId` retry에서 seed/work/warm start가 바뀜
- Provider timeout이 `MAX_STEPS_REACHED`로 보임
- Cross-tenant exact key가 not-found와 다른 timing/metadata를 노출
- Provider URI/ETag가 canonical result bytes에 들어감
- Shadow run이 production pointer/traffic/state를 바꿈
- Evidence manifest 안에 review/receipt digest가 있음
- Candidate가 빠르다는 이유로 security fail이 recommendation에서 사라짐
- Capability catalog가 22개보다 작거나 owner/evidence/negative-control 없는 행이 있음
- Canonical exact method seed 95개/hash와 fresh report method set이 다름
- Root reactor는 conformance module을 건너뛰었는데 child POM만 green임
- Phase 13 module/OR-Tools가 Phase 12 build에 나타남
- `EligibleForSeparateAdoptionReview`가 production default로 해석됨

하나라도 보이면 new starts/activation을 중단하고 마지막 accepted source/reference
state로 돌아간다.

## 17. Source → requirement → WP → test/evidence traceability

아래 evidence key는 planned requirement다. 현재 evidence가 생산됐다는 뜻이 아니다.

| Source path/section | Requirement | WP | Test/판정 | Planned evidence |
|---|---|---|---|---|
| [Master §4.4~4.6](../../../master-design.md#44-의존-방향) | Provider가 dependency/lifecycle/authority를 바꾸지 않음 | WP12-1/2/4 | SDK leakage, state parity | `E-P12-PROVIDER-CONTRACT` |
| [Master §13](../../../master-design.md#13-termination-reproducibility와-execution-provenance) | Retry/deadline/replay identity 보존 | WP12-5/8 | `ProviderRetryIdentityIT`, replay | `E-P12-PARITY` |
| [Master §14.1](../../../master-design.md#141-publication-gate) | Both-verifier publication authority | WP12-3/4/8 | Both-gate fault matrix | `E-P12-PROVIDER-CONTRACT` |
| [Master §15.10](../../../master-design.md#1510-rm-8--logical-port-integration과-compatibility-migration) | Logical-port migration과 rollback | WP12-1/7 | Shadow/copy/rollback IT | `E-P12-MIGRATION`, `E-P12-ROLLBACK` |
| [Master §16.3](../../../master-design.md#163-deferred-resume-criteria) | AWS 이외 topology는 parity와 별도 승인 | WP12-0/2/9 | Adoption/config denial | Entry/review refs |
| [Domain §2.4](../../../2026-07-26-domain-design.md#24-same-vehicle-exactly-once-precedence와-route-bank-xor) | Provider 교체 중 pair/route-bank invariant 유지 | WP12-4/8 | E2E verifier/corruption | `E-P12-PARITY` |
| [Domain §8](../../../2026-07-26-domain-design.md#8-stable-solution-request-bank와-cow) | Stable source of truth와 derived state 분리 | WP12-4/5 | Replay/cache-free oracle | `E-P12-PARITY` |
| [Domain §15](../../../2026-07-26-domain-design.md#15-verification-finalization과-result) | Result authority provider-neutral | WP12-4/8 | Both-gate application parity | `E-P12-PROVIDER-CONTRACT` |
| [Domain §16~18](../../../2026-07-26-domain-design.md#16-오류와-종료-모델) | Failure/open/gated/deferred typed 유지 | WP12-0/5/9 | Taxonomy/manifest tests | Limitations evidence |
| [Architecture §2.2](../../../2026-07-26-architecture-design.md#22-module-dag와-금지-dependency) | Module DAG와 SDK/vendor isolation | WP12-1/2 | Architecture tests | `E-P12-PROVIDER-CONTRACT` |
| [Architecture §3.6](../../../2026-07-26-architecture-design.md#36-identity-idempotency-retry와-cancellation) | Logical identity와 retry/cancel 보존 | WP12-4/5 | Retry/cancel tests | `E-P12-PARITY` |
| [Architecture §5.1~5.5](../../../2026-07-26-architecture-design.md#51-provider-neutral-운영-port) | Ports/artifact/failure/security/publication | WP12-3~6 | Contract/security tests | Contract/security evidence |
| [Integrated §16.1~16.6](../../../architecture-domain-implementation-design.md#161-orthogonal-substitution) | 최소 independent axis 교체 | WP12-0/2 | Axis/config/capability tests | Axis/capability matrix |
| [Integrated §16.7](../../../architecture-domain-implementation-design.md#167-artifact-migration) | Digest-preserving copy | WP12-7 | `ProviderArtifactMigrationIT` | `E-P12-MIGRATION` |
| [Integrated §16.9~16.10](../../../architecture-domain-implementation-design.md#169-workflowcompute-adapter-contract-suite) | Workflow/compute suite와 gate | WP12-4/8 | Workflow/worker/E2E parity | `E-P12-PARITY` |
| [Integrated §19~21](../../../architecture-domain-implementation-design.md#19-configuration-provenance와-observability) | Config/provenance/failure/security | WP12-5/6 | Codec/failure/security/ops | `E-P12-SECURITY`, `E-P12-OPERATIONS` |
| [Integrated §22~25](../../../architecture-domain-implementation-design.md#22-test와-evidence-matrix) | Test/invariant/anti-pattern/deferred | WP12-1~9 | Full layer matrix | Review refs |
| [Question `Q-INFRA-01`](../../../master-design-open-questions.md#q-infra-01) | AWS reference 선택과 adoption/cutover 분리 | WP12-0/8/9 | Role/authority assertions | Comparison/receipt |
| [Question `Q-BENCH-02`](../../../master-design-open-questions.md#q-bench-02) | Official 수치 open 유지 | WP12-5/8 | Missing policy → gated | Open value list |
| [Question `Q-VAR-01`](../../../master-design-open-questions.md#q-var-01) | Deferred variant 비활성 | WP12-1/2 | No invariant relaxation | Architecture evidence |
| [Phase 11 §14/§18.2](../../phases/phase-11-aws-reference-distribution.md#182-phase-12-handoff) | Standalone AWS evidence/receipt forward input | WP12-0/8/9 | Evidence DAG tests | AWS reference digest |
| [Phase 12 review F-P12-001](../../reviews/phase-12-review.md#f-p12-001--run-state-version이-action-authorization과-publication-precondition을-겸했다) | State/action/publication token separation | WP12-1/3/4 | Type/CAS/race tests | Contract resolution |
| [Phase 12 review F-P12-002](../../reviews/phase-12-review.md#f-p12-002--integration-test를-surefire로-선택해-0-test-green이-가능했다) | Integration zero-test false-green 차단 | WP12-2~9 | Failsafe/report reconciliation | Test reports |
| [Phase 12 review F-P12-003/004](../../reviews/phase-12-review.md#f-p12-003--accessfailureworker-authority-gap을-adapter가-임의로-메울-수-있었다) | Non-ambient access/lossless failure/durable deadline | WP12-0/1/4~6 | Negative/crash tests | Contract/security refs |
| [Phase 12 review F-P12-005](../../reviews/phase-12-review.md#f-p12-005--aws-equality가-semantic-oracle을-대신하고-applicability가-사후-변경될-수-있었다) | Independent oracle + pre-sealed applicability | WP12-1/8 | Catalog/config/parity tests | `E-P12-PARITY` |
| [Phase 12 review F-P12-007](../../reviews/phase-12-review.md#f-p12-007--conformanceevidencereview-manifests가-circular하게-봉인될-수-있었다) | Evidence DAG 순환 금지 | WP12-9 | Graph/digest negative tests | Acceptance receipt |
| [Human review F-HG-P12-001](../reviews/phase-12-review.md#f-hg-p12-001--canonical-required-capability-7개가-applicability-후보에서-빠졌다) | Canonical capability 22개, owner/evidence/negative-control closure | WP12-1/2 | 22-row exact applicability/decision manifest | Capability matrix |
| [Human review F-HG-P12-002](../reviews/phase-12-review.md#f-hg-p12-002--canonical-exact-oracle-7개-누락으로-full-suite-false-green이-가능하다) | Canonical oracle 95개 seed/hash와 7개 branch 복원 | WP12-1/4/6~8 | Exact method/report reconciliation | Oracle execution manifest |
| [Human review F-HG-P12-003](../reviews/phase-12-review.md#f-hg-p12-003--proposed-mavenfailsafetest-fixture-tree가-실제-lifecycle에-연결되지-않는다) | Reactor/test-jar/profile/Failsafe/verify closure | WP12-1~9 | Root `-am clean` + standalone/full profile | Reactor/report evidence |
| [Human review F-HG-P12-004](../reviews/phase-12-review.md#f-hg-p12-004--최초-run-state-생성과-submission-idempotency-계약이-사라졌다) | Atomic initial state와 submission idempotency | WP12-3/4 | Same/same, same/different, concurrent/lost-ack/crash | `E-P12-PROVIDER-CONTRACT`, parity |
| [Phase 13 §4.1/§14.2](../../phases/phase-13-optional-hybrid-route-selection.md#41-모든-조건을-요구하는-activation-gate) | Infra evidence만 조건부 전달 | WP12-9 | Handoff authority assertions | Bounded handoff |
| [Phase 14 §3.2](../../phases/phase-14-official-calibration-cutover.md#32-phase-0012-evidence-receipt) | Applicable provider evidence, production 별도 gate | WP12-9 | Receipt/authority tests | Phase 14B input |
| Frozen actual inventory | Legacy/scaffold는 characterization only | WP12-0/2 | Absence/leakage/read-only inventory | Snapshot note |

## 18. 구현자가 마지막으로 답할 자문 질문

### 18.1 Authority와 scope

1. Candidate provider와 axis를 누가 어떤 immutable record로 승인했는가?
2. 이 작업은 한 최소 axis인가, 편의상 여러 축을 묶었는가?
3. Phase 11 evidence/review/receipt가 실제 accepted인가?
4. Production authority가 false임을 code/config/evidence가 모두 강제하는가?
5. Phase 13/14 gate를 어떤 문장이나 default로도 당기지 않았는가?

### 18.2 Correctness와 identity

1. AWS와 candidate가 각각 independent oracle에 통과했는가?
2. Provider locator/execution ID/version token을 모두 바꿔도 semantic digest가 같은가?
3. Same `WorkerRunId` retry에서 `AttemptId` 외 무엇이 바뀌는가?
4. State CAS, action authorization, publication precondition이 type과 storage key 모두
   분리됐는가?
5. Pair, route-bank XOR, verifier/result authority가 provider code에서 재구현되지
   않았는가?
6. Same submission same/same이 같은 `SolveId`/state로 수렴하고 same/different가
   mutation 없는 conflict인가?
7. Initial create의 concurrent winner, lost ack와 crash recovery가 list/blind write
   없이 exact authority read로 닫히는가?

### 18.3 Failure, security와 operations

1. Unknown provider status는 어떤 typed failure가 되는가?
2. Denied/missing/corrupt/stale/indeterminate가 end-to-end로 구분되는가?
3. Explicit access binding이 없을 때 ambient credential이 있어도 provider call이 0인가?
4. Restart deadline에 wall clock/provider remaining time hidden fallback이 없는가?
5. Required alarm/rollback/cleanup owner가 실제 evidence에 있는가?

### 18.4 Evidence와 handoff

1. Applicable case set은 두 provider 실행 전에 seal됐는가?
2. Provider evidence에 conformance/review/receipt back-reference가 0인가?
3. Review가 exact conformance manifest digest만 검토했는가?
4. Recommendation이 production default처럼 표현되지 않았는가?
5. Phase 13 handoff가 `INFRASTRUCTURE_DECISION_INPUT_ONLY`인가?
6. Capability 22개와 canonical exact method 95개의 count/hash/missing 판정이 evidence에
   있는가?
7. Root `-am clean`, standalone full profile와 fresh Surefire/Failsafe report가 같은
   source/POM/profile/run identity를 가리키는가?

## 19. 이 guide 자체와 현재 scope의 정적 검증

Guide 작성자는 대상 파일 하나만 변경했는지 확인한다.

```bash
test -s docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

rg -n '^# Phase 12 사람용 구현 가이드 — Provider substitution$' \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

git status --short -- \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

git diff --check -- \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md
```

Target이 untracked이면 일반 `git diff --check`가 file content를 검사하지 않을 수 있다.
그 경우 tracked empty file과 비교하는 `--no-index` equivalent 또는 별도 whitespace
scanner를 사용한다. 검증 과정에서도 target 밖 파일을 수정하지 않는다.

필수 semantic marker:

```bash
rg -n 'canonical_phase_count: 15|Q-BENCH-02|C-17|Q-VAR-01|Phase 13|Phase 14|productionAuthority' \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

rg -n '^### WP12-[0-9]' \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

rg -n 'PROPOSED|FUTURE|OPEN|GATED|DEFERRED|EXPERIMENT_REQUIRED' \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

rg -n 'EXACT_READ_AFTER_COMMITTED_WRITE|COOPERATIVE_STOP_AND_CANCEL|TENANT_SCOPED_AUTHORIZATION|DISTINCT_RUN_STATE_AND_PUBLICATION_PRECONDITIONS|ENCRYPTION_IN_TRANSIT|ENCRYPTION_AT_REST|BOUNDED_RETRY_AND_DEADLINE' \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md

rg -n '9efdc487e7f059c0362fa5e3cfdfeeb65d265f8b2193f8cb02161653547efb40|expectedCanonicalMethodCount = 95|createIfAbsent|IDEMPOTENCY_CONFLICT|-am clean' \
  docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md
```

상대 link와 GFM anchor는 Markdown parser로 local target/fragment를 검사한다. Fence 수,
trailing space/tab, CRLF, EOF newline도 검사한다. 검사 도구가 repository에 없으면
temporary read-only validator를 사용하고 결과만 보고한다.

## 20. 남아 있는 gate 요약

| Gate | 현재 상태 | 안전한 다음 행동 |
|---|---|---|
| Phase 00 acceptance | Review 02 `CHANGES_REQUIRED`; Fix 02 `IN_PROGRESS`, receipt 없음 | Fix 02 재생성 + review 03 + exact acceptance receipt 대기 |
| Phase 08~10 contracts | `CHANGES_REQUIRED/NOT_ACCEPTED` | Exact access/failure/commit/action/publication/cancel/deadline 계약 승인 |
| Phase 11 | `CHANGES_REQUIRED`, evidence 0 | Actual AWS standalone evidence + accepted review/receipt |
| Candidate provider/axis | `NOT_SELECTED` | Product/Platform/Ops adoption decision |
| Non-prod environment | 승인 없음 | Scoped credential/budget/cleanup authority |
| Security/Ops | Evidence 없음 | Control profile와 actual negative/recovery tests |
| Performance/cost | Policy/threshold open | Workload-based owner approval |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Phase 14 calibration 전 유지 |
| Phase 13 `C-17` | `GATED_NOT_STARTED` | Phase 14A receipt + 별도 backend/security/ops/cost 승인 |
| `Q-VAR-01` | `DEFERRED` | 질문·구현하지 않음 |
| Phase 14A | `NOT_RUN` | Phase 00~08 accepted 뒤 별도 benchmark |
| Phase 14B/production | `NOT_STARTED/NOT_GRANTED` | Official/provider/production gates 별도 충족 |

현재 안전한 결론은 **Phase 12 구현 시작 불가, contract catalog와 red-test 설계까지만
가능**이다. 이 gate를 legacy code, mock, hidden default, 문서 review 또는 concurrent
Phase 00 scaffold로 닫지 않는다.
