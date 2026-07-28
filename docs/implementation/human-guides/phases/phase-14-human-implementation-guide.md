# Phase 14 사람용 구현 가이드 — Official calibration과 cutover

```yaml
guide_status: CORRECTED_ROUND_01_AWAITING_INDEPENDENT_REVIEW
guide_scope: Phase 14 only
correction_round: "01"
canonical_phase_count: 15
phase: "14"
phase_name: official-calibration-cutover
canonical_phase_document: docs/implementation/phases/phase-14-official-calibration-cutover.md
canonical_phase_review: docs/implementation/reviews/phase-14-review.md
canonical_phase_document_version: "1.4 + ALNS_FIRST_1.0 overlay"
canonical_phase_document_status: REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: CHANGES_REQUIRED
human_guide_review: docs/implementation/human-guides/reviews/phase-14-review.md
human_guide_review_verdict: CHANGES_REQUIRED
phase_acceptance_status_observed: BLOCKED_NOT_READY
implementation_status_observed: GATED_NOT_STARTED
phase_14a_alns_benchmark_status_observed: NOT_RUN
phase_14a_acceptance_receipt_status_observed: NOT_PRODUCED
phase_14b_cutover_status_observed: NOT_STARTED
calibration_status_observed: NOT_RUN
official_manifest_status_observed: NOT_CREATED
official_run_status_observed: NOT_RUN
selected_provider_deployment_status_observed: NOT_DEPLOYED
production_authority_observed: NOT_GRANTED
cutover_status_observed: NOT_STARTED
evidence_status_observed: NOT_PRODUCED
handoff_status_observed: NOT_READY
entry_gate_status: CLOSED
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_snapshot_at: "2026-07-29T01:13:22+0900"
inventory_snapshot_policy: ONE_TIME_LIVE_SNAPSHOT_UNCOMMITTED_UNAPPROVED
inventory_drift_status: CONCURRENT_PHASE00_WORK_CONTINUED_THEN_SNAPSHOT_FROZEN
live_inventory_path_set_sha256: 59255347c009741847d9de18a2ccf93454a535883cb75acbd28d8e90d5608a01
source_fingerprint_scheme: explicit_git_blob_with_authoring_and_correction_time_separation
source_sections_and_fingerprints:
  docs/master-design.md: "§1~4, §7~8, §11~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "§1~3, §8~18 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "§1~7, §10~20, §22 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/architecture-domain-implementation-design.md: "§1~3, §12~30; especially §18~26 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and Q-MTX-01~03/Q-BENCH-01~03/Q-INFRA-01/Q-VAR-01 | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/2026-07-26-domain-design.md: "historical semantic cross-check only | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "historical semantic cross-check only | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§1~15; especially Phase 14 and §8~14 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§1~10 | correction-time live 0419f69199b3140dd44020f78278b1352e6517b8"
  docs/implementation/human-guides/execution-progress-and-results.md: "§1~7 | correction-time live b65d32e611e389925a106d0d535e7aefb02a99a1"
  docs/implementation/phases/phase-06-cow-alns-reproducibility.md: "§1.2, §7~8, §14~17 | 984b6978981fffa662bcf4cf5b4f8a14c2287c09"
  docs/implementation/phases/phase-07-independent-verification-final-result.md: "§1.2, §7~10, §13~16 | 1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115"
  docs/implementation/phases/phase-08-application-ports-local-runtime.md: "§1.2, §6~11, §14~18 | 2aff093a6f2728470a7ccbb22b7e1a1a71f5b963"
  docs/implementation/phases/phase-11-aws-reference-distribution.md: "§3~18; especially §18.3 | 14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8"
  docs/implementation/phases/phase-12-provider-substitution.md: "§3~6, §15~19; especially §18.4 | 63b9defb25d7771bce590d593db9485367724918"
  docs/implementation/phases/phase-13-optional-hybrid-route-selection.md: "§1.3, §4, §6.5, §12~14 | cb3cd961c87b034625ad138046b5745822df16bd"
  docs/implementation/human-guides/phases/phase-13-human-implementation-guide.md: "§2.5, §9.2, §14~16 | correction-time live ef7224e3e7654745927f47deab894075924b799b"
  docs/implementation/phases/phase-14-official-calibration-cutover.md: "§0~17 | c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26"
  docs/implementation/reviews/phase-14-review.md: "§1~9 and F-P14-001~014 | 39105379b34dbc856ca2a9162906ca00449de1e5"
  docs/implementation/human-guides/reviews/phase-14-review.md: "HG14-R001~R006 | dc05a4e81907f2bfce56d66ec9356ac2fb2d1e9b"
expected_reader:
  - Java의 interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, capacity, time-window propagation을 구현해 보았다
  - RPDPTW pair, immutable evidence, calibration authority와 production cutover는 처음 접한다
owner_roles:
  implementation: Phase 14 Calibration/Cutover owner
  algorithm: Algorithm/ALNS owner
  benchmark_quality: Benchmark/Quality owner
  input_matrix: Input/Matrix owner
  independent_verification: Independent Verification/Result owner
  signing_trust: Security/Release evidence-trust owner
  provider_deployment: Selected-provider Platform/Release owner
  security: Platform Security owner
  operations: Production SRE/Operations owner
  cost: FinOps owner
  product: Product authority owner
  rollback: Production SRE/Incident owner
  review: Independent Phase 14 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P14-ALNS-BENCHMARK
  - E-P14-ALNS-BENCHMARK-ACCEPTANCE
  - E-P14-CALIBRATION
  - E-P14-OFFICIAL-RUN
  - E-P14-CUTOVER
  - E-P14-ROLLBACK
```

> 이 문서는 Java와 CVRPTW 경험이 있는 신규 구현자가 15개 canonical Phase의 마지막
> 단계인 Phase 14를 배우고, gate가 열린 뒤에만 실제 구현·검증·전환을 수행하도록
> 돕는 교육형 지시서다. 현재 checkout에 보이는 reactor와 package 골격은 다른 작업자의
> **미커밋·미승인 Phase 00 진행물**이다. Phase 14 Java type, calibration runner,
> official manifest, selected-provider distribution, cutover code/test/evidence는 없다.
> 아래 이름과 signature는 명시적으로 확정이라고 표시하지 않는 한
> **`PROPOSED INTERNAL`**이며 존재하는 API, public/wire 계약 또는 완료 evidence가 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 14는 “solver를 한 번 실행해 가장 좋은 숫자를 고르는 단계”가 아니다.

**14A는 미리 승인한 ALNS-only 실험을 완결하고 독립 검토된 acceptance receipt를
발행하는 evidence authority이고, 14B는 별도로 승인된 official manifest와 action별
production authority 아래 shadow·canary·activation·rollback을 수행하는 production
authority다.**

두 권한을 다음처럼 분리해서 기억한다.

```text
14A:
  frozen ALNS-only experiment
  → complete run accounting
  → independent recomputation/review
  → ALNS_BENCHMARK_ACCEPTANCE_RECEIPT

14B:
  accepted 14A receipt + accepted provider path + approved official values
  → sealed official manifest/run/replay
  → shadow
  → scoped canary authority
  → separate activation authority
  → provider pointer read-back/reconciliation
  → ACTIVE or typed rollback/rejection
```

14A receipt는 production 권한이 아니다. Non-production deployment 성공도 production
권한이 아니다. Phase 13 hybrid acceptance도 production 권한이 아니다. 반대로
production approver가 서명했다고 해서 미완결 calibration, 누락 worker 또는 verifier
실패가 정상이 되지도 않는다.

## 2. 큰 그림과 필요한 이유

### 2.1 Canonical 15 Phase의 마지막 위치

15개 Phase는 앞 단계가 만든 권위 artifact를 다음 단계가 소비하는 순서다.

| Phase | Producer output 또는 책임 | Phase 14가 보는 이유 |
|---:|---|---|
| 00 | Reactor, architecture rule, legacy characterization | 재현 가능한 build와 forbidden dependency의 뿌리 |
| 01 | Canonical input와 numeric/time normalization | 실험 입력 의미를 한 번만 확정 |
| 02 | Complete `PreparedTravel`, immutable problem | integer travel authority와 solver/verifier 동일성 |
| 03 | Propagation, full evaluation, comparator | benchmark vector를 독립 재계산하는 권위 |
| 04 | Exact `BoundProfile`, preset, capability | 서로 다른 profile 결과를 섞지 않는 identity |
| 05 | Pair insertion, 최대 4×2 initial portfolio | available candidate 전수 accounting |
| 06 | COW ALNS, seed/work/termination/replay | 14A가 측정할 ALNS-only producer |
| 07 | Candidate/result 두 verifier, publishable result | correctness hard gate |
| 08 | Provider-neutral local runtime와 immutable execution artifacts | 14A가 cloud 없이 실행할 local oracle |
| 09 | No-DB exact-key, immutable artifact, CAS | 14B의 artifact/state 권위 |
| 10 | Declared-worker coordinator, retry/cancel | 14B official run completeness |
| 11 | AWS S3 + Step Functions + Lambda reference evidence | AWS reference를 선택했을 때의 provider 입력 |
| 12 | 승인된 provider substitution | 실제 대체 provider가 선택된 경우에만 조건부 입력 |
| 13 | Optional route pool/CP-SAT hybrid | accepted 14A 뒤 별도 `C-17` 승인 때만 조건부 입력 |
| **14A** | **ALNS benchmark evidence와 acceptance receipt** | **Phase 13의 필요조건이지만 자동 activation 아님** |
| **14B** | **Official run, provider cutover와 production record** | **최종 production authority 소비자** |

Canonical Phase 번호는 `00`부터 `14`까지 15개다. 별도 `Phase 15`는 없다.
`14A/14B`는 한 canonical Phase 안의 두 권한 stage다.

```text
ALNS correctness/benchmark:
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A

AWS ALNS-only production 후보:
08 → 09 → 10 → 11
14A + 11 → 14B

조건부:
10 → 12                              # provider substitution 승인 시
14A receipt + C-17 approvals → 13    # optional hybrid
13 → 14B                             # official hybrid manifest일 때만
```

금지되는 역방향은 다음과 같다.

```text
13 -X-> 14A receipt 생성/수정
14B -X-> 13 entry evidence 생산
provider deploy -X-> 14A acceptance
production approval -X-> missing benchmark evidence 보완
```

### 2.2 왜 14A와 14B를 분리하는가

CVRPTW 경험자는 종종 “성능 실험을 마쳤으니 바로 배포해도 된다”고 생각한다. 그러나
이 시스템에서는 서로 다른 질문이 섞인다.

| 질문 | 답을 소유하는 권한 |
|---|---|
| 이 ALNS 구현이 올바르고 재현 가능한가? | Phase 06/07/08 evidence + 14A independent acceptance |
| 어떤 corpus/seed/repeat/resource/variance 기준을 썼는가? | Benchmark/Quality의 preregistered plan |
| 어떤 algorithm parameter가 official인가? | Algorithm + Quality의 별도 approval |
| worker/step/round/watchdog 공식 수치는 무엇인가? | `Q-BENCH-02` calibration + 별도 execution-value approval |
| 어떤 provider revision이 준비됐는가? | Platform/Security/Ops/FinOps deployment evidence |
| production traffic을 지금 바꿔도 되는가? | Product/Release/Security/Ops의 action-specific authority |
| 실패하면 어디로 돌아가는가? | SRE/Incident의 verified last safe point와 rollback authority |

하나의 boolean `approved=true`로 이 질문을 모두 답하면 다음 false-green이 생긴다.

- 좋은 seed만 남기고 실패·timeout run을 제외한다.
- test fixture 또는 legacy `8`, `5000`을 official 값으로 승격한다.
- `sam deploy` 성공을 traffic authority로 오인한다.
- provider action 전 control state를 `ACTIVE`로 써서 실제 traffic과 상태가 갈라진다.
- rollback을 안전하게 수행했는데도 production acceptance라고 보고한다.
- Phase 13이 닫혀 있는데 hybrid package를 class-load하거나 반대로 hybrid 결과를
  ALNS correctness oracle로 사용한다.

### 2.3 Producer와 consumer 계약

| 경계 | Producer가 보장할 것 | Phase 14가 하면 안 되는 것 |
|---|---|---|
| Phase 06 → 14A | Immutable candidate/trace/replay, seed·work·termination identity | Test 수치를 official threshold로 승격, MIP를 correctness oracle로 사용 |
| Phase 07 → 14A | Candidate/result verifier report, publishable/rejected의 exhaustive 구분 | Candidate `PASS`만으로 result/quality acceptance 합성 |
| Phase 08 → 14A | Local manifest, all-run status, artifact digest, deterministic rerun | 선택된 성공 run만 전달하거나 corpus/threshold를 소급 선택 |
| 14A → Phase 13 | `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`만 | `C-17`, backend/version/native/license/production 권한 부여 |
| Phase 09/10 → 14B | Exact-key immutable artifact, distinct CAS, declared worker completeness | Listing이나 completion order로 champion/closure 추정 |
| Phase 11 → 14B | Accepted AWS provider evidence, rollback/limitations | Provider evidence를 traffic authority로 취급 |
| Phase 12 → 14B | 선택된 대체 provider의 accepted bounded evidence | 미선택 provider를 구현·배포하거나 AWS test로 대체 |
| Scheduler/control plane `Skip` → 14B | `C-17` closed ALNS-only applicability와 action-time verification | Phase 13 실행/acceptance/hybrid ref 합성 |
| Gate-open Phase 13 handoff + Phase 14B `Activated` wrapper → 14B | Accepted hybrid evidence, last ALNS-only rollback point와 current applicability verification | Raw `ObjVal`, incumbent 또는 partial backend log 사용 |
| 14B → Operations | Exact active identity, evidence index, activation 또는 terminal rollback record | Secret/PII, mutable latest, 빈 activation record 전달 |

Phase 13 branch별 schema/producer/action authority를 합치지 않는다.

| Branch | Producer/action owner | Artifact 의미 | Phase 14 소유 범위 | Closed/open gate 보존 |
|---|---|---|---|---|
| `Skip` | Scheduler/control plane | `C17_GATE_CLOSED`인 exact ALNS-only applicability control record | Consumer projection, current signature/trust/scope verification | Phase 13 source/dependency/class-load/`E-P13-*` 0 |
| `Activated` input/wrapper | Gate-open Phase 13은 unsigned `Phase13ActivatedHandoff`만 생산; Phase 14B/control plane이 selected official-hybrid action에서 signed consumer wrapper를 조립 | Exact accepted hybrid closure와 last ALNS-only rollback point + sibling signed applicability/action-time verification | Consumer wrapper, official manifest scope match와 action-time verification | 14A receipt + 전체 `C-17` + Phase 13 evidence 전 handoff 금지; `G14-SIGNING-TRUST` 전 wrapper/action 금지 |

공통 sum type의 consumer schema를 Phase 14/control-plane 경계에 두는 것과 각 branch의
producer가 같다는 것은 다른 주장이다. Phase 14는 어느 branch도 self-sign하거나 Phase 13
implementation module에 type/dependency를 역으로 제공하지 않는다.

### 2.4 완료 신호를 미리 구별하기

| 보이는 사건 | 말할 수 있는 것 | 아직 말할 수 없는 것 |
|---|---|---|
| Unit test green | 한 계약 조각이 구현됨 | 14A accepted, official, production |
| 모든 ALNS run 완료 | measurement set이 complete할 수 있음 | acceptance criteria 통과, receipt 발행 |
| Independent review `ACCEPTED` | 봉인된 evidence가 criteria를 통과 | 14B 시작 또는 Phase 13 자동 activation |
| Official manifest seal | exact run template가 생김 | run 성공, provider 준비, traffic authority |
| Provider shadow parity | 같은 의미를 보존한 후보 revision | canary 또는 activation 승인 |
| Canary 성공 | 승인 범위의 관찰 evidence | full activation 승인 |
| Rollback 성공 | 안전 terminal과 복구 evidence | Phase 14B production `ACCEPTED` |
| `ACTIVE` read-back + final CAS + review | production activation 후보 완료 | evidence index/operations handoff 전 전체 DoD |

## 3. Source authority, fingerprint와 정확한 읽기 순서

### 3.1 충돌 해소 순서

```text
사용자 고정 지시
→ canonical docs/master-design.md
→ question register의 exact Q-* 상태
→ current canonical docs/domain-design.md의 domain 의미
→ current canonical docs/architecture-design.md의 module/port 배치
→ Integrated design의 15 Phase/no-DB/AWS 구조
→ implementation README/plan/progress
→ Phase 14 detail/review
→ producer/adjacent Phase의 stable handoff section
→ 날짜형 2026-07-26 Domain/Architecture의 historical semantic cross-check
```

[2026-07-26 이전 Master 초안](../../../2026-07-26-master-design.md),
`docs/deprecated/*`, `docs/codex/*`와 연구 예시는 현재 authority가 아니다.
[Current Domain](../../../domain-design.md)과
[Current Architecture](../../../architecture-design.md)는 이 guide가 사용하는 current
canonical 상세 지도다. 날짜형 [Domain](../../../2026-07-26-domain-design.md)과
[Architecture](../../../2026-07-26-architecture-design.md)는 section 이름이나 과거
semantic 결정을 대조하는 historical source일 뿐 current authority fingerprint가 아니다.
Current Architecture의 distributed state/identity, provider-neutral port,
security/failure와 provider compatibility/test evidence를 Phase 14 requirement에
적용한다. `Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`은
[Canonical Master](../../../master-design.md)와
[question register](../../../master-design-open-questions.md)를 따른다. 이 해소는 AWS
구현·배포·cutover 완료를 뜻하지 않는다.

Implementation README와 일부 사람용 guide가 날짜형 source index를 유지하는 것은
repository-level source-index 정렬 과제다. 이 guide에서는 current canonical source를
우선하고, 의미가 다르면 hash 일치로 해소하지 않고 `SOURCE_CONTRACT_IMPACT_UNREVIEWED`로
중지한다.

### 3.2 검증 가능한 source fingerprint

아래 Git blob은 metadata의 `inventory_observed_at_commit`에서
`git rev-parse HEAD:<path>`로 얻은 content identity다. Blob 일치는 읽은 bytes를
재현하기 위한 fingerprint일 뿐 compatibility, implementation acceptance 또는 gate
receipt가 아니다. Source가 바뀌면 hash만 교체하지 말고 해당 heading의 semantic
impact를 requirement, WP, test, evidence와 gate에 다시 연결한다.

| Source | 직접 읽을 heading/section | HEAD Git blob |
|---|---|---|
| [Canonical Master §4](../../../master-design.md#4-구현-아키텍처와-책임-경계) | §1~4, §7~8, §11~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Current Domain §3](../../../domain-design.md#3-계층과-단방향-책임) | §1~3, §8~18, 특히 travel/result/comparator/multi-round/evidence | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Current Architecture §11](../../../architecture-design.md#11-distributed-multi-round-runtime) | §1~7, §10~20, §22, 특히 distributed state/port/security/test | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Integrated design §18](../../../architecture-domain-implementation-design.md#18-phase-14--calibration-migration과-cutover) | §1~3, §12~30 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register `Q-BENCH-02`](../../../master-design-open-questions.md#q-bench-02) | §1~4, `Q-MTX-*`, `Q-BENCH-*`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Dated Domain](../../../2026-07-26-domain-design.md) | Historical semantic cross-check only | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Dated Architecture](../../../2026-07-26-architecture-design.md) | Historical semantic cross-check only | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Implementation README §6](../../README.md#6-phase-작업-순서) | §0~7, canonical 15 Phase와 ALNS-first DAG | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Plan Phase 14](../../master-realization-plan.md#phase-14--official-calibrationcutover) | §1~15, Phase 14, evidence DAG와 DoD | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress §8](../../execution-progress-and-results.md#8-현재-blockers-open-gates와-남은-이슈) | §1~10, scheduler authority와 correction-time live blocker | `0419f69199b3140dd44020f78278b1352e6517b8` |
| [Human-guide Progress §6](../execution-progress-and-results.md#6-보존해야-하는-gate와-blocker) | 사람용 guide/review/correction 상태와 보존 gate | `b65d32e611e389925a106d0d535e7aefb02a99a1` |
| [Phase 06 §16.3](../../phases/phase-06-cow-alns-reproducibility.md#163-other-downstream-consumers) | ALNS candidate/replay → 14A | `984b6978981fffa662bcf4cf5b4f8a14c2287c09` |
| [Phase 07 §15.3](../../phases/phase-07-independent-verification-final-result.md#153-other-downstream-consumers) | Both-gate result → 14A | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` |
| [Phase 08 §16.5](../../phases/phase-08-application-ports-local-runtime.md#165-phase-14a-alns-benchmark-qualification-handoff) | Local manifest/all-run artifact → 14A | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| [Phase 11 §18.3](../../phases/phase-11-aws-reference-distribution.md#183-phase-14-handoff) | AWS evidence → 14B | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` |
| [Phase 12 §18.4](../../phases/phase-12-provider-substitution.md#184-future-adoptioncutover-boundary) | Conditional provider adoption boundary | `63b9defb25d7771bce590d593db9485367724918` |
| [Phase 13 §14.3](../../phases/phase-13-optional-hybrid-route-selection.md#143-next--phase-14b-gate-closed) | Signed `Skip`/`Activated` branch | `cb3cd961c87b034625ad138046b5745822df16bd` |
| [Phase 13 human guide §15](../phases/phase-13-human-implementation-guide.md#15-다음-phase-handoff와-broken-contract-증상) | Closed `Skip`, unsigned accepted handoff와 Phase 14B signed consumer wrapper 분리 | `ef7224e3e7654745927f47deab894075924b799b` |
| [Phase 14 §3.1](../../phases/phase-14-official-calibration-cutover.md#31-gate-matrix) | 전체, 특히 §3~16 | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` |
| [Phase 14 review §4](../../reviews/phase-14-review.md#4-findings) | `F-P14-001`~`014`과 residual blocker | `39105379b34dbc856ca2a9162906ca00449de1e5` |
| [Human-guide review §5](../reviews/phase-14-review.md#5-findings) | `HG14-R001`~`R006` correction input | `dc05a4e81907f2bfce56d66ec9356ac2fb2d1e9b` |

재검증 예:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/master-design.md
git rev-parse HEAD:docs/implementation/phases/phase-14-official-calibration-cutover.md
git rev-parse HEAD:docs/implementation/reviews/phase-14-review.md
git hash-object docs/implementation/execution-progress-and-results.md
```

`execution-progress-and-results.md`는 scheduler/Phase 00 작업으로 HEAD 뒤 live bytes가
달라졌다. 최초 author snapshot은 §5에 역사로 보존하고 correction 시점의 두 progress
map은 위 blob으로 별도 고정한다. 어느 live blob도 acceptance authority가 아니다.
구현 시작에는 scheduler가 승인한 새 immutable freeze와 semantic diff가 필요하다.

### 3.3 구현 전 정확한 읽기 순서

| 순서 | 읽을 곳 | 답해야 할 질문 | 확인할 module/file/evidence |
|---:|---|---|---|
| 1 | [Implementation README §0~7](../../README.md) | 15 Phase, ALNS-first DAG와 status owner는 누구인가? | Phase 14 canonical detail/review |
| 2 | [Question register](../../../master-design-open-questions.md) exact rows | 어떤 수치·provider·variant가 확정/open/deferred인가? | `Q-MTX-*`, `Q-BENCH-*`, `Q-INFRA-01`, `Q-VAR-01` |
| 3 | Master §4, §8, §13~16 | Identity, travel, replay, two-gate, comparator, migration 의미는? | Immutable artifact, official run, rollback |
| 4 | Current Domain §3, §8~18 | Pair, prepared travel, result/termination/evidence의 정확한 의미는? | Request/route-bank, verifier authority |
| 5 | Current Architecture §5~7, §10~20, §22 | Module DAG, distributed identity/state, logical port, CAS, provider/verifier/security 격리는 어디인가? | `rpdptw-application`, adapter/distribution, provider compatibility evidence |
| 6 | Integrated §18~26 | Phase 14, config/provenance/security/failure/test checklist는? | Official manifest gate와 cutover |
| 7 | Master Plan Phase 06~14, §8~14 | Entry/exit/evidence DAG, DoD와 OPEN/GATED는? | `E-P14-*`, acceptance receipt |
| 8 | Phase 06 §16.3, Phase 07 §15.3, Phase 08 §16.5 | 14A가 실제로 받을 producer artifact는? | ALNS replay, both-verifier, local run closure |
| 9 | Phase 11 §18.3, Phase 12 §18.4 | 14B provider evidence의 조건부 범위는? | AWS 또는 selected substituted provider |
| 10 | Phase 13 §1.3/§4/§6.5/§14.3~14.4 | `Skip`과 `Activated`는 무엇을 보장하는가? | Signed applicability/action-time receipt |
| 11 | Phase 14 전체 | Gate, artifact, lifecycle, WP, test, DoD와 handoff는? | §3~16 |
| 12 | Phase 14 review 전체 | 어떤 false-green이 교정됐고 무엇이 residual인가? | `F-P14-001`~`014`, `CHANGES_REQUIRED` |
| 13 | Execution Progress §8~10 | 현재 status, scheduler task와 blocker는? | `0/15 ACCEPTED`, Phase 00 live work |
| 14 | 실제 repository inventory | Proposed module/type/test/provider evidence가 실제로 있는가? | §5 HEAD/live/target 표 |

### 3.4 Entry gate를 읽는 순서

Phase 14 전체를 한 번에 `READY`로 만들지 않는다.

1. `G14A-PREDECESSOR`: Phase 00~08 중 14A에 필요한 accepted evidence graph 확인.
2. `G14A-BENCHMARK-PROTOCOL`: corpus/fixture/seed/repeat/runtime/oracle/criteria 승인.
3. 14A 실행·review·receipt 발행.
4. 14B를 시작할 때만 Phase 09~11과 selected-provider branch를 확인.
5. ALNS-only면 actual signed Phase 13 `Skip`, hybrid이면 accepted `Activated` 확인.
6. Integer fixture, Great Circle, ALNS parameter, `Q-BENCH-02`, signing trust,
   provider deployment, calibration acceptance를 **각각** 확인.
7. Official manifest/run/replay를 완료.
8. Shadow 뒤 canary authority를 확인.
9. Canary hold 뒤 **별도** activation authority를 확인.
10. Provider action receipt/read-back/reconciliation 뒤에만 final state를 `ACTIVE`로 CAS.

한 단계의 `PASS`를 다음 단계의 authority로 복사하지 않는다.

## 4. RPDPTW primer, 용어집, identity, lifecycle과 invariant

### 4.1 CVRPTW customer와 RPDPTW request

CVRPTW에서는 customer 하나가 한 방문이고 demand가 대체로 감소한다. RPDPTW에서는
`Request` 하나가 pickup과 delivery라는 두 service 의미를 소유할 수 있다.

```text
R1: pickup P1(+6) → delivery D1(-6)
R2: pickup P2(+5) → delivery D2(-5)

capacity 10:
P1 → D1 → P2 → D2  = feasible
P1 → P2 → D1 → D2  = prefix load 11, infeasible
```

모든 stable state에서 request는 다음 중 정확히 하나다.

```text
complete same-vehicle route pair
XOR
SearchRequestBank membership exactly once
```

Delivery-only request는 logical pickup을 initial load ownership으로 유지하지만 가짜
depot visit을 만들지 않는다. Final result의 `UNASSIGNED`는 search bank를 직렬화한
값이 아니라 candidate verifier `PASS` 뒤 finalization/audit가 만드는 outcome이다.

Phase 14가 이 기초를 알아야 하는 이유는 official comparator의 첫 성분인 unassigned
count, route payload, worker result와 corruption oracle이 모두 request-level exact
partition에서 시작하기 때문이다. Scalar objective 한 개만 보관하면 잘못된 pair를
“좋은 해”로 고를 수 있다.

### 4.2 용어집

| 용어 | 이 가이드의 뜻 | 혼동하면 안 되는 것 |
|---|---|---|
| Calibration | 사전 등록한 candidate/config/run을 측정·분석하는 과정 | 승인값 또는 production 권한 |
| Preregistration | 첫 measured run 전 input/config/seed/method를 동결 | 실행 뒤 좋은 조건 선택 |
| Declared run set | 실행하기로 미리 고정한 case×candidate×seed×repeat 집합 | 성공한 run 목록 |
| Acceptance policy | 완료·correctness·quality·resource·variance 판정식 | 구현자 판단 또는 library default |
| 14A receipt | ALNS-only benchmark evidence를 독립 review 뒤 승인한 immutable record | 14B/Phase 13/production 자동 시작 |
| Official value | 측정 결과와 별도 권한으로 승인된 explicit value | test/legacy/default 숫자 |
| Official manifest | 모든 authority ref와 exact run template를 봉인한 immutable artifact | mutable config bag 또는 한 번 쓰고 수정하는 object |
| Official execution receipt | manifest의 한 invocation/attempt 결과 | manifest 자체 |
| Candidate verifier | route/bank/feasibility/objective를 cache-free 재계산 | solver feasible flag |
| Result verifier | outcome/audit/summary/payload 무결성을 재검증 | candidate verifier 대체 |
| Exact comparator | unassigned → used vehicles → distance → operational time | scalar `double objective` |
| Phase 13 `Skip` | `C-17` closed인 ALNS-only 14B applicability | Phase 13 accepted/implemented |
| Phase 13 `Activated` | accepted hybrid scope/evidence handoff | raw backend incumbent |
| Provider deployment evidence | exact environment revision의 parity/security/ops 증거 | traffic 변경 승인 |
| Production authority | exact action/environment/manifest/deployment/pointer/traffic/time scope 승인 | broad role 또는 deploy success |
| Shadow | production output/pointer를 바꾸지 않는 same-manifest 비교 | canary |
| Canary | 승인된 제한 traffic을 실제 candidate로 전환 | full activation |
| Hold | 관찰·판정 동안 forward transition을 멈춘 상태 | 자동 성공 |
| Last safe point | 이전 accepted revision/pointer + immutable artifacts + rollback role | 백업 파일 하나 |
| Evidence index | exact ref로 완전한 evidence graph를 닫는 root | object prefix listing |
| CAS | 예상 version/digest가 같을 때만 pointer/state 전환 | check-then-write |
| OPEN | 결정/승인값이 아직 없음 | 편의상 채울 TODO |
| GATED | 별도 authority 전 착수/activation 금지 | 구현이 어려운 상태 |
| DEFERRED | restart evidence 전 질문·활성화 금지 | 우선순위가 낮은 open item |

### 4.3 Identity를 하나의 fingerprint로 합치지 않기

```text
semantic identity:
  canonical input + problem + prepared travel + profile/policy

algorithm identity:
  portfolio/operator/repair/acceptance/adaptive/state strategy
  approved ALNS parameter authority

execution identity:
  screen/worker/step/round/watchdog/resource
  seed derivation + declared run/worker lineage

build/runtime identity:
  source commit + dependency/toolchain + artifact/image + JVM/architecture

provider deployment identity:
  provider/stage/region-class + IaC/change-set + resource revision

production action identity:
  authority envelope + previous/desired pointer versions + traffic scope

attempt/observation identity:
  invocation attempt, provider request ID, telemetry window
```

Provider execution ID, ARN, bucket key, Lambda request ID와 completion order는 관측
metadata다. Semantic result fingerprint에 넣지 않는다. Secret/private key는 어떤
fingerprint에도 넣지 않는다.

### 4.4 14A evidence lifecycle

```text
approved explicit experiment candidates
→ frozen CalibrationPlan
→ exact DeclaredRunSet
→ raw result/trace/telemetry/cost for every declared run
→ CalibrationExecutionOutcome.Complete
   [declared-run closure + raw index + producer analysis only]
→ neutral PreAnalysisCalibrationRecord.EXPERIMENT_REQUIRED
→ separate IndependentCalibrationAnalysis
   [references exact execution outcome; independent owner/provenance]
→ CalibrationDecision.EligibleForIndependentReview or typed blocked/rejected
→ preReviewEvidenceManifest [digest M]
→ independentReviewReport [references M, digest R]
→ verified post-review acceptance authority
→ AlnsBenchmarkAcceptanceReceipt [references M + R + exact authority verification]
```

각 화살표는 새 immutable artifact를 만든다. Review 결과를 pre-review manifest에
backfill하지 않는다. Acceptance receipt를 review report나 manifest에 역참조하지 않는다.
누락·실패·timeout run을 삭제하지 않고 typed outcome으로 accounting한다.
Runner는 독립 분석 digest나 독립 verdict를 생성·주입할 수 없다. 독립 분석 전의
`EXPERIMENT_REQUIRED`는 성공/실패 방향을 갖지 않는 neutral record다. 이 record나
`EligibleForIndependentReview`도 post-review `ACCEPTED` receipt가 아니다.

14A receipt가 만들어지는 순간에도 다음은 그대로다.

```text
Phase 13 = GATED until separate C-17 approvals
Phase 14B = NOT_STARTED until its own entry gates
production authority = NOT_GRANTED
```

### 4.5 14B cutover lifecycle

```text
accepted 14A receipt
→ separate ALNS-parameter approval
→ separate Q-BENCH-02 execution-value approval
→ Phase 13 signed Skip or accepted Activated
→ sealed OfficialExecutionManifest
→ all-declared-worker official run
→ candidate PASS
→ finalization/audit
→ result PASS
→ exact benchmark card
→ identical-manifest replay
→ selected-provider deployment/shadow
→ scoped canary authority
→ canary pointer action/read-back/reconciliation
→ durable observation hold
→ separate activation authority
→ activation pointer action/read-back/reconciliation
→ ACTIVE + ProductionActivationRecord
→ sealed ImplementationEvidenceIndex + operations handoff
```

Manifest는 immutable execution template다. Official run과 replay는 manifest를
`used=true`로 바꾸지 않고 별도 append-only execution receipt를 만든다.

### 4.6 Cutover의 두 상태를 분리하기

Traffic을 바꾸는 transition에는 적어도 두 version이 있다.

```text
controlStateVersion     # application-owned desired/pending/final state
providerPointerVersion # 실제 provider traffic/publication pointer
```

안전한 순서:

```text
read exact current state/pointer
→ append immutable transition intent
→ CAS same semantic state + pendingTransitionId
→ provider pointer CAS
→ provider receipt + exact read-back
→ in-flight reconciliation
→ final control-state CAS to CANARY_RUNNING/ACTIVE/ROLLED_BACK
```

Control state를 먼저 `ACTIVE`로 쓰면 provider action 실패 시 false `ACTIVE`가 된다.
Provider action 성공 뒤 process가 죽으면 같은 transition identity로 read-back하고
수렴시킨다. 새 divergent action을 보내지 않는다.

### 4.7 반드시 지킬 invariant

1. Official 숫자는 signed/approved authority artifact에 explicit하게 존재한다.
2. `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`는 measured review 전 닫히지 않는다.
3. ALNS parameter와 execution values는 별도 identity와 approval을 가진다.
4. Provided `D/U`는 integer meter/second이며 fractional cell 하나도 official로 받지 않는다.
5. Missing `D`는 승인된 Great Circle function/version/model/constants/precision/vector와
   meter `HALF_UP`을 사용한다.
6. Missing `U`는 vehicle별
   `CEILING(D_meter × 3.6 ÷ speed_km_h)`이고 speed missing일 때만 `45 km/h`다.
   Present-invalid speed는 거부한다.
7. Provided/generated와 speed source provenance를 보존한다.
8. 같은 official manifest의 결과만 quality 비교한다.
9. Comparator는 unassigned → used vehicles → distance meter → operational seconds다.
10. 모든 declared worker가 정상 완료·candidate 검증되지 않으면 run은 `INCOMPLETE`다.
11. Candidate/result verifier 둘 다 `PASS`하기 전 publication/benchmark는 없다.
12. Seed/repeat/outlier를 실행 뒤 선별하지 않는다.
13. Elapsed/completion order는 quality/seed/fingerprint의 hidden input이 아니다.
14. 14A는 Phase 13/backend/license/native/provider/production authority 없이 완결된다.
15. `C-17` closed면 ALNS-only가 정상 branch이며 Phase 13 code를 호출하지 않는다.
16. Official hybrid는 accepted 14A 뒤 accepted Phase 13 evidence를 추가로 요구한다.
17. Provider deployment evidence와 production authority는 서로 대체하지 않는다.
18. Traffic/pointer transition은 expected control state와 provider pointer를 따로 확인한다.
19. Artifact는 create-once immutable이고 cutover/rollback은 pointer/traffic만 바꾼다.
20. Missing telemetry/clock/authority evidence는 success가 아니라 hold/fail-closed다.
21. Rollback target/role/rehearsal 전에는 canary/activation을 시작하지 않는다.
22. Irreversible delete/key destruction/schema rewrite는 Phase 14 activation 비범위다.

## 5. 실제 repository inventory — HEAD, live drift와 목표

### 5.1 스냅샷 정책

HEAD 기준선:

```text
branch: codex-implementation
commit: 7cc890ee1d0805df5ae14b633127fade4f978639
HEAD pom blob: f8a411eadd4a5c01d8dd09fdea462738ca63d65f
```

Live worktree에는 이 가이드 작업 전부터 대규모 Phase 00 변경이 있었다. 조사 중에도
`build/generate-phase-00-evidence.sh`가 추가되는 등 외부 변경이 계속됐다. 따라서
다음 한 시점만 동결한다.

```text
snapshot: 2026-07-29T01:13:22+0900
policy: ONE_TIME_LIVE_SNAPSHOT_UNCOMMITTED_UNAPPROVED
selected path count: 67
sorted path-set SHA-256:
  59255347c009741847d9de18a2ccf93454a535883cb75acbd28d8e90d5608a01
live pom Git-style blob:
  912a84c4f894e6a3af1148aad31eaf62ba0c0fed
tracked input SHA-256 at frozen snapshot:
  data/win_poc_case.json =
    ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7
  scripts/floor_win_poc_matrix.py =
    0423e0cef4d93ed51525a8f237b48f0c680c7d6e134af1ea70ba3a48b5ed77d0
  data/win_poc_case_floor.json =
    c246abd375211877c4ec3467998651deb13fbd01768d2d1d84b8d234f5f56873
```

이후 생긴 외부 drift는 이 문서의 baseline도, 승인도 아니다. Entry 시 실제 구현자는
새 scheduler-authorized freeze를 만들고 semantic diff를 review해야 한다.

Correction 01은 원래 snapshot을 바꾸지 않고 다음 **별도 read-only 재관측**을
`2026-07-29T02:37:46+0900`에 고정했다. 이후 shared-checkout drift는 recheck 세션이
안정된 최종 상태에서 다시 검증하며, 이 correction은 반복 추적하지 않는다.

| 재관측 항목 | Correction-time 값 | 판정 |
|---|---|---|
| HEAD/branch | `7cc890ee1d0805df5ae14b633127fade4f978639` / `codex-implementation` | 원 author baseline과 동일 |
| Root POM | Git blob `1dc675ba17b7f2202f34a22131f152cc2868b075` | 미커밋 Phase 00 candidate, acceptance 아님 |
| Official progress map | SHA-256 `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c`; blob `0419f69199b3140dd44020f78278b1352e6517b8` | Phase 00 review 02 `CHANGES_REQUIRED`, fix 02 in progress, Phase 14 entry closed |
| Human-guide progress map | SHA-256 `ccad802fe0f238966a8810a8a1f72a4e5901bc2e4244c2bc269e991b221247d7`; blob `b65d32e611e389925a106d0d535e7aefb02a99a1` | Correction workflow 관측, actual implementation `0%` |
| Adjacent Phase 13 human guide | SHA-256 `68c35d77638d440513ec55bd025599bb6d34a7e7c429dacdfe362ceb3d5feba1`; blob `ef7224e3e7654745927f47deab894075924b799b` | Closed `Skip`, unsigned handoff, Phase 14B signed wrapper의 current 교육형 경계 |
| Non-`target`, non-`node_modules` POM | 13개 | Phase 14 module/profile acceptance 아님 |
| Main/test Java | 33/16개 | `rpdptw/**` 23개는 전부 `package-info.java`; Phase 14 match 0 |
| Failsafe/Phase 14 IT wiring | POM match 0 | `*IT` discovery/profile/report authority 없음 |
| Non-`target` Maven/Java inventory manifest SHA-256 | `4245739baf8f5cf6ab7ec2f6ed065211009282004ecdeef8726fdcd4525dd206` | Correction-time path+bytes 관측, acceptance identity 아님 |

이 재관측은 원 snapshot을 “최신값”으로 덮어쓰지 않는다. Shared checkout drift와 두
progress map의 시점을 분리하는 correction evidence다. 실제 구현자가 사용할 freeze는
scheduler task, accepted predecessor receipt, source/POM/test manifest와 같은 시점을
가리켜야 하며 다르면 `SOURCE_OR_INVENTORY_DRIFT`로 중지한다.

### 5.2 HEAD baseline과 frozen live inventory

| 영역 | HEAD baseline | Frozen live snapshot | Phase 14 판정 |
|---|---|---|---|
| Root Maven | 단일 GCP-oriented app JAR POM | `packaging=pom`, `rpdptw/build/legacy` aggregator | 외부 미커밋 Phase 00 scaffold; accepted 아님 |
| POM | root 1개 | 총 13개 POM | 존재는 evidence/receipt가 아님 |
| Wrapper | 없음 | executable `./mvnw` 있음 | 미승인 scaffold; future command 후보만 가능 |
| HEAD Java | main 6, test 1 | 전체 Java 47 | 수량은 기능 완료가 아님 |
| Target `rpdptw` | 없음 | Java 23개, 전부 `package-info.java` | Package placeholder만 존재 |
| Build tests | 없음 | architecture/test-fixture Java 9개 | Phase 00 진행물; Phase 14 test 0 |
| Legacy | root `src/**`의 GCP placeholder | `legacy/gcp-placeholder`: main 10, test 5 | Characterization 대상 |
| `adapters/`, `apps/` | 없음 | directory는 보이나 inventory file 없음 | Placeholder/empty |
| `distributions/`, `deployment/` | 없음 | 없음 | Local/AWS/Phase 14 distribution·IaC 부재 |
| Phase 14 type/test | 없음 | `CalibrationPlan`, `OfficialExecutionManifest`, `CutoverState`, `ProductionAuthority`, `Phase14*` source match 0 | 실제 구현 0 |
| AWS source/IaC | 없음 | 없음 | Deployment evidence 0 |
| GCP 자료 | Docker/GCP workflow/controller | legacy로 이동 중, tracked GCP docs 유지 | AWS/production evidence 아님 |
| FLOOR fixture | tracked | tracked, SHA-256 `c246abd3…f56873` | 계획 실행 입력; 14A/14B 자동 authority 아님 |
| Progress | Phase 00 not accepted | Phase 00 remediation in progress 기록 | Phase 14 entry closed |

Correction-time live 상태도 같은 결론이다. Phase 00은 review 02
`CHANGES_REQUIRED`와 fix 02 `IN_PROGRESS`, acceptance receipt는 `NOT_PRODUCED`;
Phase 14 Java/test는
0개이고 Failsafe binding도 없다. 따라서 아래 future 계약을 현재 reactor에 실행해
green이라고 주장하거나 stale `target/**/surefire-reports`를 acceptance에 재사용하지
않는다.

### 5.3 존재, placeholder와 부재

현재 **존재**:

- `data/win_poc_case.json`, `scripts/floor_win_poc_matrix.py`,
  `data/win_poc_case_floor.json`과 고정 digest
- 미커밋 root aggregator/wrapper와 `rpdptw/*` module POM
- Package boundary를 설명하는 `package-info.java`
- Phase 00 architecture/test-fixture test와 legacy characterization
- Canonical Phase 00~14 detail/review 문서

현재 **placeholder 또는 미승인 진행물**:

- Root reactor와 architecture rule
- `rpdptw/core|solver|verification|application|capabilities|profile-catalog`
- `legacy/gcp-placeholder`
- `build/test-fixtures`, `build/architecture-rules`, evidence scripts

현재 **부재**:

- Phase 01~08 accepted implementation/evidence graph
- 실제 RPDPTW domain/ALNS/verifier/application code
- `CalibrationPlan`, calibration runner와 acceptance evaluator
- Official manifest/run/replay/comparator implementation
- Signed evidence trust implementation과 actual receipt
- Phase 13 signed `Skip`/`Activated` receipt
- Object/storage/coordinator/AWS distribution
- Provider deployment/shadow/cutover/rollback implementation
- `E-P14-*`, accepted review, production authority와 activation record

### 5.4 Read-only inventory 명령

아래는 현재 사실 확인용이다. 성공을 Phase 14 evidence로 봉인하지 않는다.

```bash
git rev-parse HEAD
git status --short --untracked-files=all
rg --files -g 'pom.xml' -g '!target/**'
rg --files rpdptw build legacy adapters apps -g '*.java' -g '!target/**'
rg -n -i \
  'CalibrationPlan|OfficialExecutionManifest|CutoverState|ProductionAuthority|Phase14' \
  rpdptw build legacy -g '*.java' -g '*.xml' -g '*.sh'
shasum -a 256 \
  data/win_poc_case.json \
  scripts/floor_win_poc_matrix.py \
  data/win_poc_case_floor.json
```

`./mvnw -v`는 toolchain diagnostic으로 실행할 수 있지만, scaffold owner의 freeze와
accepted Phase 00 evidence 없이 root build 결과를 Phase 14 baseline으로 사용하지 않는다.

## 6. Scope, non-scope, 결정 상태와 사람 승인

### 6.1 Phase 14 in scope

- 14A predecessor receipt와 ALNS-only benchmark protocol
- Integer travel/Great Circle authority의 검증 seam
- Corpus/config/seed/run/runtime/analysis preregistration과 freeze
- Complete declared-run execution, independent oracle/analysis와 acceptance receipt
- ALNS parameter와 `Q-BENCH-02` execution values의 separate authority
- Phase 13 `Skip`/`Activated` applicability의 14B consumption
- Official manifest/run/all-worker/two-verifier/exact comparator/replay
- Selected-provider deployment evidence, compatibility/migration/shadow
- Cutover state machine, distinct control/pointer CAS, durable observation
- Scoped canary/activation authority와 rollback/reconciliation
- Evidence index, activation/rollback record와 Operations handoff

### 6.2 명시적 non-scope

- Corpus, repeat 수, confidence, threshold, budget, variance, SLO, canary 비율/시간 추정
- Legacy `8`, `5000`, clock seed 또는 library default를 official 값으로 사용
- Great Circle earth radius/function/precision을 구현자가 선택
- Phase 13을 calibration/cutover 일정 때문에 강제 실행
- OR-Tools version/native/license/SBOM/security/ops/cost gate를 Phase 14가 대신 닫기
- 미선택 Phase 12 provider module을 빈 골격으로 생성하거나 배포
- GCP placeholder/generated `.serverless`를 AWS/rollback authority로 승격
- Production account/region/credential/secret 또는 보편 traffic command를 문서에 고정
- Legacy artifact/state/key/versioned endpoint 삭제
- Multi-trip/rotation, optional variants, dynamic routing
- Phase 00~13 source/POM/test/deployment를 이 가이드 작업에서 수정

### 6.3 확정, proposed, open, gated와 deferred

| 분류 | 항목 | 구현자가 취할 행동 |
|---|---|---|
| 확정 | 15 Phase, ALNS-first DAG, two verifier, exact comparator, all-worker, immutable/CAS | Test와 architecture rule로 보존 |
| 확정 | AWS S3 + Step Functions + Lambda target/reference 선택 | Phase 11 evidence 없이는 배포 완료 주장 금지 |
| 확정 | FLOOR fixture는 현재 계획의 final execution input | 14A corpus/official scope는 별도 승인 |
| 확정 | 14A와 14B 권한 분리 | 하나의 status/approval로 합치지 않음 |
| `PROPOSED INTERNAL` | Java 이름, package 상세, cutover enum/state 이름 | ADR/review 전 public API로 고정 금지 |
| `OPEN — EXPERIMENT_REQUIRED` | `Q-BENCH-02`, corpus/threshold/repeat/budget/variance | Explicit experiment candidate만 허용 |
| `GATED` | Signing trust, provider deployment, production authority | Owner evidence/approval 전 action 0 |
| `GATED TARGET` | Phase 13/OR-Tools hybrid | Accepted 14A receipt + `C-17` 전체 gate 전 source/dependency 0 |
| 조건부 | Phase 12 provider substitution | Selected substituted provider일 때만 |
| `DEFERRED` | `Q-VAR-01`, multi-trip/rotation | Restart evidence 전 질문·활성화 금지 |
| 비가역 비범위 | Delete/key destruction/schema rewrite | 별도 decommission approval/plan으로 분리 |

### 6.4 Gate matrix와 마지막 안전 지점

| Gate | 현재 | Owner | 열리면 허용되는 범위 | 닫혀 있을 때 마지막 안전 지점 |
|---|---|---|---|---|
| `G14A-PREDECESSOR` | `CLOSED` | Scheduler + Phase 00~08 reviewers | 14A benchmark 실행 | 문서·test design, run 0 |
| `G14A-BENCHMARK-PROTOCOL` | `OPEN — EXPERIMENT_REQUIRED` | Benchmark/Quality + reviewer | Approved ALNS-only plan | Explicit exploratory plan only |
| `G14-INTEGER-FIXTURE` | `CLOSED` | Input/Matrix + Benchmark | Approved corpus/official input | FLOOR bytes read-only, no relabel |
| `G14-GREAT-CIRCLE` | `CLOSED` | Input/Matrix + Domain/Architecture | Missing-`D` readiness | Missing-`D` official path off |
| `G14-CALIBRATION-ACCEPTANCE` | `CLOSED` | Benchmark/Quality + independent verifier | 14A receipt | Incomplete result sealed |
| `G14B-PREDECESSOR` | `CLOSED` | Scheduler + reviewers | Official manifest/run 준비 | Accepted local benchmark only |
| `G14-P13-APPLICABILITY` | `CLOSED` | Scheduler + Product/Algorithm/Architecture | ALNS-only 또는 hybrid branch 선택 | Phase 13 refs/code 0 |
| `G14-ALNS-PARAMETERS` | `CLOSED` | Algorithm + Quality | Official algorithm config | Experiment config only |
| `G14-QBENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Benchmark/Quality | Official execution values | No official manifest |
| `G14-SIGNING-TRUST` | `CLOSED` | Security/Release | Signed 14B evidence consumption | Unsigned schema/negative test only |
| `G14-PROVIDER-DEPLOYMENT` | `CLOSED` | Platform/Security/Ops/FinOps | Shadow/canary 준비 | Local/non-prod accepted path |
| `G14-PRODUCTION-AUTHORITY` | `NOT_GRANTED` | Product/Release/Security/Ops | Exact canary 또는 activation action | Shadow/pre-canary hold, pointer unchanged |

14A acceptance DAG에는 production signing trust를 선행조건으로 발명하지 않는다.
14B에서 signed applicability/authority를 실제 소비할 때만 approved trust profile,
revocation/time/freshness와 action-time verification을 요구한다.

### 6.5 사람 승인 checkpoint

코드 작성자가 혼자 결정할 수 없는 것:

1. Corpus/fixture의 official 또는 benchmark scope
2. Seed/repeat/run matrix와 timeout/resource/variance/quality criteria
3. Great Circle policy와 reference vectors
4. Official ALNS parameter envelope
5. `Q-BENCH-02` execution values
6. Signature profile, trust roots, revocation/time/freshness policy
7. Selected provider와 Phase 12 applicability
8. Phase 13 `Skip` 또는 `Activated`의 exact scope
9. Provider deployment parity/security/ops/cost acceptance
10. Canary traffic/window/stop criteria와 action authority
11. Separate activation authority
12. Rollback target/role와 irreversible change 여부

승인이 없으면 `TBD`, `OPEN`, `GATED`, `NOT_PRODUCED`로 남긴다. 숫자나 fake signature로
빈칸을 채우지 않는다.

## 7. 학습 경로 — 개념 → 작은 탐색 → 실제 변경 → 통합

### 7.1 1단계 — 개념을 말로 설명하기

먼저 코드 없이 다음을 설명한다.

- 14A receipt가 왜 production authority가 아닌가?
- Phase 13은 왜 14A 뒤 optional이고 ALNS-only 14B에는 불필요한가?
- ALNS parameter와 `Q-BENCH-02` execution value는 왜 다른가?
- Manifest와 execution receipt는 왜 별개인가?
- Candidate verifier와 result verifier는 무엇을 각각 검증하는가?
- Provider deployment evidence와 traffic authority는 왜 다른가?
- Control state CAS와 provider pointer CAS를 왜 분리하는가?
- Rollback 성공이 왜 production acceptance가 아닌가?

완료 신호: 동료가 “deploy가 성공했으니 active” 같은 반례를 제시했을 때 어떤 gate와
evidence가 빠졌는지 정확히 말할 수 있다.

자문 질문:

1. 지금 보고 있는 것은 측정값, 승인값, 실행 template, 실행 결과, 배포 증거,
   traffic 권한 중 무엇인가?
2. 이 artifact를 누가 만들고 누가 독립 검증하는가?
3. 실패하면 마지막 authoritative pointer는 바뀌는가?

### 7.2 2단계 — 작은 손 탐색

#### 탐색 A: comparator

```text
A = (unassigned=1, vehicles=2, distance=1000, time=5000)
B = (unassigned=0, vehicles=9, distance=99999, time=99999)
winner = B
```

첫 번째 다른 성분만 본다. Weighted sum으로 바꾸지 않는다.

```text
C = (0, 2, 1000, 5000)
D = (0, 2, 1000, 5000)
quality = tie
```

Structural fingerprint tie-break는 deterministic selection에는 쓸 수 있어도 다섯 번째
quality 성분은 아니다.

#### 탐색 B: declared run closure

```text
declared = {r1, r2, r3, r4}
observed = {r1=PASS, r2=PASS, r3=TIMEOUT}
missing  = {r4}
```

결론은 “3개 중 2개 성공”이 아니라 `INCOMPLETE`다. `r3`, `r4`를 제외하고 분석하지 않는다.

#### 탐색 C: cutover crash

```text
pending intent written
provider pointer CAS succeeded
process crashed before final state CAS
```

재시작은 같은 transition ID로 provider read-back을 한다. Desired pointer라면 final state로
수렴시키고, 다르면 hold/rollback/incident다. 새 traffic action을 추측해 실행하지 않는다.

완료 신호: 세 탐색을 작은 truth table로 만들고 expected typed outcome을 쓸 수 있다.

### 7.3 3단계 — 가장 작은 실제 변경 순서

Gate가 열린 뒤에도 production code부터 쓰지 않는다.

1. Authority/identity value와 negative test
2. Frozen plan과 declared-set completeness pure function
3. Independent oracle와 corruption fixture
4. Calibration execution port의 local deterministic fake
5. Pre-review evidence DAG와 receipt factory
6. Official manifest factory의 fail-closed validation
7. Official run completeness와 both-gate integration
8. Pure cutover reducer/state truth table
9. Provider control port contract와 response-loss reconciliation
10. Selected provider adapter/shadow
11. Approved release pipeline의 read-only preflight/post-read-back

완료 신호: 각 vertical slice가 앞 단계 artifact만 받고 hidden environment/default를 읽지
않으며, 실패 시 이전 immutable artifact와 pointer가 그대로다.

### 7.4 4단계 — 통합과 handoff

통합 순서:

```text
local 14A complete
→ independent review/receipt
→ 14B official-value authorities
→ official run/replay
→ provider shadow
→ canary authority/action/hold
→ activation authority/action
→ evidence index/operations handoff
```

완료 신호:

- Test report의 expected/discovered/executed method가 exact하다.
- Run/worker 누락, verifier fail, signature mismatch가 모두 fail-closed다.
- Local/provider semantic artifact가 같고 provider metadata는 별도다.
- `ACTIVE`는 provider receipt/read-back/reconciliation 뒤에만 보인다.
- ProductionActivationRecord가 active read-back evidence와 exact 연결된다.

### 7.5 구현 전 최종 자문

1. 이 WP의 predecessor receipt가 실제 accepted인가, 문서에 이름만 있는가?
2. Open value를 누락하면 factory가 실패하는가?
3. Test signer/fake approval이 production trust에서 반드시 거부되는가?
4. Declared run/worker를 listing 없이 exact key로 셀 수 있는가?
5. Failed/timeout/missing run이 분석에서 사라질 경로가 0인가?
6. Manifest가 immutable이고 run/replay receipt가 별도인가?
7. Phase 13 closed path가 class-load/dependency 없이 ALNS-only인가?
8. Provider deploy와 production action이 서로 다른 authority를 요구하는가?
9. Provider action 전 false `ACTIVE`가 될 수 있는가?
10. Crash 뒤 clock/window를 추측해 완료 처리하는가?
11. Rollback이 artifact 삭제가 아니라 pointer/reconciliation인가?
12. Scheduler 외 주체가 status를 올리려 하는가?

하나라도 “모르겠다”면 다음 WP로 가지 않고 §12의 blocker note를 작성한다.

## 8. 목표 module, package, file과 dependency

### 8.1 예상 change tree

아래 tree는 accepted Phase 00~13 contract 뒤의 **`PROPOSED INTERNAL`** 후보다.
현재 존재를 주장하지 않는다. Phase 00 architecture ADR과 실제 owner module이 우선한다.

```text
rpdptw/application/
├── src/main/java/com/ronext/rpdptw/application/
│   ├── calibration/
│   │   ├── CalibrationPlanService.java
│   │   ├── CalibrationRunner.java
│   │   ├── CalibrationAcceptanceEvaluator.java
│   │   └── AlnsBenchmarkAcceptanceReceiptFactory.java
│   ├── official/
│   │   ├── OfficialManifestFactory.java
│   │   ├── OfficialBenchmarkRunner.java
│   │   ├── OfficialRunCompletenessVerifier.java
│   │   └── ImplementationEvidenceIndexer.java
│   └── cutover/
│       ├── CutoverCoordinator.java
│       ├── CutoverStateMachine.java
│       ├── ProductionAuthorityVerifier.java
│       └── RollbackCoordinator.java
└── src/test/java/com/ronext/rpdptw/application/phase14/

adapters/common/
├── src/main/java/com/ronext/rpdptw/adapter/common/evidence/
└── src/test/java/com/ronext/rpdptw/adapter/common/evidence/

apps/coordinator/
├── src/main/java/com/ronext/rpdptw/app/coordinator/
│   ├── OfficialRunCommandHandler.java
│   └── CutoverCommandHandler.java
└── src/test/java/com/ronext/rpdptw/app/coordinator/phase14/

distributions/local/
└── src/test/java/com/ronext/rpdptw/distribution/local/phase14/

distributions/aws-serverless/              # AWS_REFERENCE 선택 때만
└── src/test/java/com/ronext/rpdptw/distribution/aws/phase14/

deployment/aws/                            # AWS_REFERENCE 선택 때만
├── parameters/
│   ├── calibration.example.json
│   └── cutover.example.json
├── alarms/
│   └── phase14-stop-conditions.yaml
└── runbooks/
    ├── shadow.md
    ├── activation.md
    └── rollback.md

build/test-fixtures/
└── src/test/java/com/ronext/rpdptw/fixture/phase14/

build/port-contract-tests/                 # 현재 frozen inventory에는 부재
└── src/test/java/com/ronext/rpdptw/contract/phase14/
```

`build/test-fixtures`의 builder/oracle/test signer는 test source와 Phase 00이 승인한
tests-classifier/test scope에서만 소비한다. Production runtime, official authority
factory, private key 또는 cutover action을 넣지 않는다.

`*.example.json`은 field/schema 설명만 가진다. Official 값을 예시로 넣지 않는다.
누락값이 있는 example을 실행하면 validation failure여야 한다.

Phase 13 gate가 닫힌 ALNS-only branch에서는 다음을 Phase 14 때문에 만들지 않는다.

```text
rpdptw/solver/solver.pool
rpdptw/solver/solver.selection
rpdptw/application/application.hybrid
adapters/route-selection-ortools-cpsat
```

### 8.2 Compile dependency 방향

```text
rpdptw-core
   ↑        ↑
rpdptw-solver    rpdptw-verification
        ↑            ↑
        └─ rpdptw-application ─┘
                    ↑
        adapters/common evidence implementation
                    ↑
          apps/coordinator + selected distribution
                    ↑
              deployment/runbook
```

허용:

- Application은 provider-neutral evidence/artifact/state/workflow/publisher/telemetry port를
  정의하고 호출한다.
- Adapter는 그 port를 AWS 또는 selected substituted provider SDK로 구현한다.
- Verification은 core authority에서 result를 재계산한다.

금지:

- Core/solver/verification/application에 cloud SDK, KMS DTO, ARN, provider event 추가
- Verification이 solver/search/cache/cutover package를 compile-depend
- Application이 OR-Tools API를 import하거나 Phase 13 backend를 classpath 탐색
- Deployment parameter가 algorithm/semantic 숫자의 권위 source가 됨
- Cutover state machine이 comparator/verifier를 재구현
- Provider workflow가 worker completeness/champion/publication을 결정

### 8.3 책임 배치 자문표

| 변경할 책임 | 우선 owner 후보 | 잘못된 배치 신호 |
|---|---|---|
| Frozen plan/declared runs | `application.calibration` | AWS handler가 seed/run matrix 생성 |
| ALNS acceptance predicate | `application.calibration` + independent oracle | Solver가 자기 quality를 승인 |
| Evidence signature verification port | Application-owned port | KMS type가 application method에 노출 |
| Signature provider implementation | `adapters/common/evidence` 또는 selected adapter | Core가 trust store를 직접 읽음 |
| Manifest factory | `application.official` | Environment variable bag에서 숫자 fallback |
| Official run completeness | `application.official`/Phase 10 seam | Prefix listing으로 결과 수집 |
| Comparator | Accepted core/evaluation authority 재사용 | Phase 14가 scalar comparator 새로 구현 |
| Pure cutover reducer | `application.cutover` | Provider SDK call이 reducer 안에 있음 |
| Provider pointer action | Selected provider adapter | State enum이 provider ARN을 소유 |
| Release mutation | Approved release pipeline | Maven test가 traffic 변경 |
| Operations handoff | Evidence index/record + runbook | Console screenshot/mutable latest |

## 9. PROPOSED Java skeletal contract와 상태 전이

### 9.1 표기 원칙

- 아래 이름은 `PROPOSED INTERNAL`이다.
- 의미와 failure는 고정할 수 있지만 package/public/wire 이름은 ADR 전 확정하지 않는다.
- `record`는 immutable value 후보, `sealed interface`는 exhaustive outcome 후보다.
- `Map<String,String>` 같은 untyped production bag은 사용하지 않는다.
- Digest algorithm, canonical serializer, crypto algorithm과 trust store는 승인 전 open이다.
- 예제는 완성 코드가 아니라 책임 경계를 드러내는 skeleton이다.

### 9.2 Evidence와 authority value 후보

```java
// PROPOSED INTERNAL
public record EvidenceDigest(String algorithmId, String value) {}

public record ImmutableEvidenceRef(
    String evidenceKind,
    String schemaVersion,
    EvidenceDigest subjectDigest,
    EvidenceDigest envelopeDigest,
    String opaqueLocator
) {}

public record EvidenceVerificationContext(
    String expectedAction,
    String expectedEnvironment,
    EvidenceDigest expectedSubject,
    Instant checkedAt,
    String trustPolicyVersion
) {}

public sealed interface EvidenceVerificationReport
    permits EvidenceVerificationReport.Pass,
            EvidenceVerificationReport.Reject {

    record Pass(
        EvidenceDigest verificationReceiptDigest,
        Instant checkedAt
    ) implements EvidenceVerificationReport {}

    record Reject(
        EvidenceFailure failure,
        EvidenceDigest reportDigest
    ) implements EvidenceVerificationReport {}
}

public enum EvidenceFailure {
    DIGEST_MISMATCH,
    UNTRUSTED_SIGNER,
    EXPIRED,
    REVOKED,
    STALE,
    WRONG_ROLE,
    WRONG_SCOPE,
    WRONG_ACTION,
    TEST_SIGNER,
    UNKNOWN_SCHEMA
}
```

`opaqueLocator`는 identity가 아니다. Bytes digest를 확인한 뒤 deserialize하고,
signature/trust/scope/action을 확인한 뒤에만 payload를 사용한다.

```java
// PROPOSED INTERNAL; exact crypto profile remains GATED.
public interface SignedEvidenceVerifier {
    EvidenceVerificationReport verify(
        ImmutableEvidenceRef evidence,
        EvidenceVerificationContext context
    );
}
```

### 9.3 Travel authority 후보

```java
// PROPOSED INTERNAL
public record IntegerTravelFixtureAuthority(
    String fixtureId,
    String schemaVersion,
    EvidenceDigest contentDigest,
    long directedCellCount,
    EvidenceDigest sourceOrMigrationDigest,
    EvidenceDigest allCellValidationDigest,
    EvidenceDigest provenanceDigest,
    ImmutableEvidenceRef approval
) {}

public record GreatCirclePolicyAuthority(
    String policyId,
    String functionVersion,
    String earthModelId,
    EvidenceDigest constantsDigest,
    String coordinateContractVersion,
    String precisionPolicyVersion,
    EvidenceDigest approvedReferenceVectorDigest,
    ImmutableEvidenceRef approval
) {}
```

이 record는 “필드가 채워졌다”는 이유만으로 approved가 아니다.
`SignedEvidenceVerifier`와 독립 all-cell/vector oracle가 gate를 통과해야 한다.

### 9.4 Calibration execution, 독립 분석과 neutral decision 후보

```java
// PROPOSED INTERNAL
public record DeclaredRunId(
    String caseId,
    String candidateId,
    long seed,
    int repeatOrdinal
) {}

public record CalibrationPlan(
    String planId,
    String schemaVersion,
    EvidenceDigest corpusManifestDigest,
    List<EvidenceDigest> candidateAlnsParameterDigests,
    List<EvidenceDigest> candidateExecutionValueDigests,
    List<DeclaredRunId> declaredRuns,
    EvidenceDigest buildRuntimeEnvironmentDigest,
    EvidenceDigest analysisMethodDigest,
    EvidenceDigest acceptancePolicyDigest,
    ImmutableEvidenceRef preregistrationApproval
) {
    // Constructor/factory must reject absence, duplicate run IDs and hidden defaults.
}

public sealed interface CalibrationExecutionOutcome
    permits CalibrationExecutionOutcome.Complete,
            CalibrationExecutionOutcome.Incomplete,
            CalibrationExecutionOutcome.InvalidEvidence {

    record Complete(
        EvidenceDigest declaredRunSetDigest,
        EvidenceDigest rawRunIndexDigest,
        EvidenceDigest producerAnalysisDigest,
        EvidenceDigest executionOutcomeContentDigest
    ) implements CalibrationExecutionOutcome {}

    record Incomplete(
        Set<DeclaredRunId> missing,
        Map<DeclaredRunId, String> failedOrTimedOut,
        EvidenceDigest reportDigest
    ) implements CalibrationExecutionOutcome {}

    record InvalidEvidence(
        List<String> failures,
        EvidenceDigest reportDigest
    ) implements CalibrationExecutionOutcome {}
}
```

```java
// PROPOSED INTERNAL
public interface CalibrationRunner {
    CalibrationExecutionOutcome execute(CalibrationPlan frozenPlan);
}

public enum PreAnalysisDisposition {
    EXPERIMENT_REQUIRED
}

public record PreAnalysisCalibrationRecord(
    PreAnalysisDisposition disposition,
    EvidenceDigest completeExecutionOutcomeDigest,
    Set<String> remainingRequiredAnalysisAndReviewInputs,
    EvidenceDigest neutralRecordContentDigest
) {
    // Must not contain independent-analysis identity, verdict or acceptance direction.
}

public record IndependentCalibrationAnalysis(
    EvidenceDigest completeExecutionOutcomeDigest,
    EvidenceDigest independentlyReconstructedRawTableDigest,
    EvidenceDigest independentDecisionDigest,
    EvidenceDigest producerIndependentEqualityReportDigest,
    EvidenceDigest analyzerBuildRuntimeDigest,
    ImmutableEvidenceRef independenceProvenance,
    EvidenceDigest analysisContentDigest
) {}

public interface IndependentCalibrationAnalyzer {
    IndependentCalibrationAnalysis analyze(
        CalibrationPlan frozenPlan,
        CalibrationExecutionOutcome.Complete completeExecution,
        ApprovedAnalysisMethod approvedMethod
    );
}

public interface CalibrationAcceptanceEvaluator {
    CalibrationDecision evaluate(
        CalibrationPlan plan,
        CalibrationExecutionOutcome.Complete completeExecution,
        IndependentCalibrationAnalysis independentAnalysis,
        AcceptancePolicy approvedPolicy
    );
}

public sealed interface CalibrationDecision
    permits CalibrationDecision.EligibleForIndependentReview,
            CalibrationDecision.Blocked,
            CalibrationDecision.Rejected {

    record EligibleForIndependentReview(
        EvidenceDigest completeExecutionOutcomeDigest,
        EvidenceDigest independentAnalysisDigest,
        EvidenceDigest decisionDigest
    )
        implements CalibrationDecision {}

    record Blocked(Set<String> openOrMissingCriteria)
        implements CalibrationDecision {}

    record Rejected(List<String> failedCriteria, EvidenceDigest reportDigest)
        implements CalibrationDecision {}
}
```

Ownership과 순서는 다음처럼 fail-closed다.

| Artifact | 유일 producer | 포함 가능 | 포함 금지 | 다음 단계 실패 시 last safe point |
|---|---|---|---|---|
| `CalibrationExecutionOutcome.Complete` | `CalibrationRunner` | Declared closure, raw index, producer analysis | Independent digest/verdict, acceptance | Frozen plan + immutable raw/producer evidence |
| `PreAnalysisCalibrationRecord.EXPERIMENT_REQUIRED` | Application evidence recorder | Exact execution outcome ref, 남은 분석/review 입력 | Winner/accept/reject 방향, official 값 | Complete execution outcome; decision 없음 |
| `IndependentCalibrationAnalysis` | Runner/producer와 분리된 analyzer owner | Exact execution ref, 재구성 table, equality, analyzer provenance | Receipt/acceptance authority | Complete outcome + neutral record |
| `CalibrationDecision` | Approved-policy evaluator | Complete+independent exact refs와 criteria 판정 | Post-review verdict/signature | M을 봉인하지 않고 blocked/rejected record |
| `AlnsBenchmarkAcceptanceReceipt` | 별도 post-review acceptance factory | §9.5의 M/R/authority closure | Producer self-approval | M+R 및 이전 ALNS-only last-safe ref |

`EligibleForIndependentReview`는 최종 receipt가 아니다. Pre-review manifest M을 봉인하고
독립 review R을 만든 뒤 §9.5 factory가 M+R과 별도 acceptance authority를 검증해야 한다.
`EXPERIMENT_REQUIRED`에서 독립 분석을 건너뛰어 `EligibleForIndependentReview` 또는
`ACCEPTED`로 전이하는 edge는 없다.

### 9.5 14A acceptance receipt 후보

`AcceptanceReceipt`는 “관련 digest가 모였다”는 목록이 아니다. 누가, 어떤 official
ALNS-only manifest/calibration/result를, 어떤 post-review authority로 `ACCEPTED`했는지
action 시점에 다시 증명하는 contract다. Exact wire schema, crypto algorithm, key format,
trust-store provider와 validity 숫자는 `GATED`이며 이 guide가 만들지 않는다.

14A의 subject는 exact benchmark `CalibrationPlan`, complete execution outcome,
independent analysis/result와 M/R closure다. 14B의 `OfficialExecutionManifest`는 이
receipt가 소급 승인하지 않으며, accepted 14A receipt를 별도 gate input으로 소비해
WP14-4/5에서 새 authority graph로 생성한다.

```java
// PROPOSED INTERNAL
public enum BenchmarkAcceptanceVerdict {
    ACCEPTED
}

public record AcceptanceSubject(
    String phaseAndSubstage, // exact Phase 14A
    String acceptanceAction,
    EvidenceDigest calibrationPlanDigest,
    EvidenceDigest completeExecutionOutcomeDigest,
    EvidenceDigest independentCalibrationAnalysisDigest,
    EvidenceDigest preReviewEvidenceManifestDigest,
    EvidenceDigest independentReviewReportDigest,
    EvidenceDigest calibrationDecisionDigest,
    EvidenceDigest handoffArtifactDigest,
    EvidenceDigest lastSafePointDigest
) {}

public record AcceptanceIssuer(
    ImmutableEvidenceRef authorityIdentity,
    String authorityRole,
    String authorityVersion,
    EvidenceDigest delegationOrAppointmentProvenanceDigest
) {}

public record RestartCondition(
    String stableReasonCode,
    String responsibleOwnerRole,
    String resumeCheckpoint,
    String requiredEvidenceKind
) {}

public record AcceptanceClaims(
    BenchmarkAcceptanceVerdict verdict,
    EvidenceDigest acceptedScopeDigest,
    Instant notBefore,
    Instant expiresAt,
    EvidenceDigest replayNonceOrUniquenessDigest,
    List<RestartCondition> restartConditions
) {}

public record AcceptanceProvenance(
    EvidenceDigest approvedCriteriaDigest,
    EvidenceDigest approvalRequestDigest,
    EvidenceDigest approvalProcessRecordDigest,
    EvidenceDigest reviewerAcceptorIndependenceDigest,
    EvidenceDigest sourceAndToolProvenanceDigest
) {}

public record AcceptanceSignatureEnvelope(
    EvidenceDigest canonicalUnsignedPayloadDigest,
    ImmutableEvidenceRef signatureProfile,
    ImmutableEvidenceRef detachedSignature
) {}

public record PostReviewAcceptanceAuthorityEnvelope(
    String schemaVersion,
    AcceptanceSubject subject,
    AcceptanceIssuer issuer,
    AcceptanceClaims claims,
    AcceptanceProvenance provenance,
    AcceptanceSignatureEnvelope signature,
    EvidenceDigest canonicalEnvelopeDigest
) {}

public record AcceptanceAuthorityVerificationContext(
    String expectedAction,
    AcceptanceSubject expectedSubject,
    ImmutableEvidenceRef approvedTrustPolicy,
    EvidenceDigest currentTrustRootSetDigest,
    EvidenceDigest revocationSnapshotDigest,
    EvidenceDigest freshnessAndClockAuthorityDigest,
    Instant checkedAt
) {}

public sealed interface AcceptanceAuthorityVerification
    permits AcceptanceAuthorityVerification.Pass,
            AcceptanceAuthorityVerification.Reject {

    record Pass(
        EvidenceDigest authorityEnvelopeDigest,
        EvidenceDigest verifiedSubjectDigest,
        EvidenceDigest trustRootSetDigest,
        EvidenceDigest revocationAndFreshnessDigest,
        Instant checkedAt,
        EvidenceDigest verificationReceiptDigest
    ) implements AcceptanceAuthorityVerification {}

    record Reject(
        Set<AcceptanceVerificationFailure> failures,
        EvidenceDigest failureReportDigest,
        EvidenceDigest lastSafePointDigest
    ) implements AcceptanceAuthorityVerification {}
}

public enum AcceptanceVerificationFailure {
    ENVELOPE_DIGEST_MISMATCH,
    UNKNOWN_OR_DUPLICATE_SCHEMA_FIELD,
    SUBJECT_MANIFEST_MISMATCH,
    SUBJECT_REVIEW_MISMATCH,
    SUBJECT_DECISION_OR_HANDOFF_MISMATCH,
    ISSUER_UNKNOWN_OR_WRONG_ROLE,
    DELEGATION_PROVENANCE_INVALID,
    CLAIM_ACTION_SCOPE_OR_VERDICT_MISMATCH,
    CLAIM_TIME_INVALID_OR_STALE,
    REPLAY_OR_NONCE_REUSED,
    APPROVAL_PROVENANCE_INCOMPLETE,
    REVIEWER_ACCEPTOR_OR_PRODUCER_INDEPENDENCE_VIOLATION,
    SIGNATURE_PROFILE_UNAPPROVED,
    SIGNATURE_INVALID,
    TRUST_ROOT_UNAPPROVED_OR_CHANGED,
    REVOKED,
    FRESHNESS_OR_CLOCK_UNVERIFIABLE,
    RESTART_CONDITION_MISSING,
    LAST_SAFE_POINT_MISSING_OR_MISMATCH
}

public record AlnsBenchmarkAcceptanceReceipt(
    String schemaVersion,
    boolean alnsOnly,
    EvidenceDigest corpusManifestDigest,
    EvidenceDigest declaredRunMatrixDigest,
    EvidenceDigest hardwareRuntimeDigest,
    EvidenceDigest correctnessOracleAndSensitivityDigest,
    EvidenceDigest candidateVerifierReportsDigest,
    EvidenceDigest resultVerifierReportsDigest,
    EvidenceDigest qualityAnalysisDigest,
    EvidenceDigest timeoutResourceAnalysisDigest,
    EvidenceDigest varianceReplayAnalysisDigest,
    EvidenceDigest calibrationPlanDigest,
    EvidenceDigest completeExecutionOutcomeDigest,
    EvidenceDigest independentCalibrationAnalysisDigest,
    EvidenceDigest preReviewEvidenceManifestDigest,
    EvidenceDigest independentReviewReportDigest,
    EvidenceDigest calibrationDecisionDigest,
    BenchmarkAcceptanceVerdict verdict,
    EvidenceDigest postReviewAcceptanceAuthorityEnvelopeDigest,
    EvidenceDigest actionTimeAuthorityVerificationReceiptDigest,
    String approvedCriteriaVersion,
    Instant acceptedAt,
    List<RestartCondition> restartConditions,
    EvidenceDigest handoffArtifactDigest,
    EvidenceDigest lastSafePointDigest,
    EvidenceDigest receiptContentDigest
) {}

public sealed interface AcceptanceReceiptCreationResult
    permits AcceptanceReceiptCreationResult.Issued,
            AcceptanceReceiptCreationResult.Rejected {

    record Issued(AlnsBenchmarkAcceptanceReceipt receipt)
        implements AcceptanceReceiptCreationResult {}

    record Rejected(
        Set<AcceptanceVerificationFailure> failures,
        EvidenceDigest failureReportDigest,
        EvidenceDigest lastSafePointDigest
    ) implements AcceptanceReceiptCreationResult {}
}
```

Factory 입력과 검증 순서는 다음과 같다.

```text
inputs are separate immutable bytes/refs:
  complete execution outcome
  independent analysis
  eligible-for-review decision
  pre-review manifest M
  independent review R
  post-review acceptance authority envelope
  action-time trust/revocation/freshness/clock context
  handoff artifact + last-safe point

verify envelope digest before deserialization
→ reject unknown/duplicate/missing/coerced field
→ verify signature profile/signature/trust root/revocation/freshness
→ verify subject == exact plan + complete execution + independent analysis
                    + M + R + decision + handoff + last-safe point
→ verify issuer role/delegation and producer/reviewer/acceptor separation
→ verify claims action/scope/verdict/time/replay/restart conditions
→ verify alnsOnly == true and complete declared-run accounting
→ verify both verifier hard gate and approved criteria OPEN count == 0
→ verify R references exact immutable M and accepted criteria
→ issue receipt with verdict ACCEPTED
```

Receipt verifier/factory는 raw authority envelope나 provider response를 신뢰 값으로
노출하지 않고 `Issued` 또는 typed `Rejected`만 반환한다. `Rejected`는 partial receipt를
생성하지 않으며 다음 표의 last safe point를 보존한다.

| 실패 축 | Typed failure 예 | Last safe point | 금지 행동 |
|---|---|---|---|
| Subject | M/R/decision/handoff mismatch | Exact immutable M+R, receipt 없음 | 가까운 digest 선택, backfill |
| Issuer/claims | Wrong role/scope/action/verdict/time | Reviewed experiment evidence | Producer self-approval, broad role 추정 |
| Provenance | Approval/independence/restart 누락 | `EligibleForIndependentReview` 또는 review rejection | 빈 provenance를 “manual”로 통과 |
| Signature/trust root | Invalid/unapproved/revoked/stale | Unsigned schema/negative evidence only | 임의 algorithm/key/provider 선택 |
| Handoff/rollback | Last-safe 또는 restart condition 누락 | Accepted 전 ALNS-only artifact/pointer | Phase 13/14B/production 시작 |

Restart condition은 빈 자유문이 아니다. 각 항목은 stable reason, owner, resume
checkpoint와 필요한 evidence kind를 가져야 한다. 정확한 조건 목록은 승인 주체가
experiment/review 결과에 맞게 제공하며 factory default는 없다.

`receiptContentDigest`는 self-reference를 제외한 approved canonical receipt bytes의
외부 digest다. Signature/trust verification receipt와 acceptance receipt는 별도
content-addressed artifact다. 이 record에는 Phase 13 implementation ref, provider
deployment ref 또는 production authority를 넣지 않는다. 반대로 receipt 이름이나 digest
하나만으로 authority 검증을 대체하지도 않는다.

### 9.6 ALNS parameter와 execution value 분리

```java
// PROPOSED INTERNAL; exact fields follow accepted Phase 06 contract.
public record OfficialAlnsParameters(
    String schemaVersion,
    String portfolioVersion,
    String operatorSetVersion,
    String selectionPolicyVersion,
    ExplicitOperatorWeights initialWeights,
    ExplicitRewardVector rewardVector,
    ExplicitAdaptiveParameters adaptive,
    ExplicitAcceptanceParameters acceptance,
    String stateStrategy,
    EvidenceDigest canonicalDigest,
    ImmutableEvidenceRef approval
) {}

public record OfficialExecutionValues(
    long screenMaxSteps,
    int phase2WorkerCount,
    long phase2MaxSteps,
    int maxRounds,
    String watchdogPolicyId,
    String resourcePolicyId,
    String seedDerivationPolicyId,
    ImmutableEvidenceRef approval
) {}
```

`OfficialAlnsParameters`에 worker/round/watchdog를 넣지 않는다.
`OfficialExecutionValues`에 operator/reward/temperature를 넣지 않는다.
새 조합을 cherry-pick하면 새 candidate이며 재측정·재승인이 필요하다.

### 9.7 Phase 13 applicability 후보

```java
// PROPOSED Phase 14 consumer projection.
// Skip producer: scheduler/control plane.
// Activated input producer: accepted gate-open Phase 13 handoff path.
// Activated wrapper assembler/verifier: Phase 14B control plane at consumption time.
public sealed interface Phase13ApplicabilityReceipt
    permits Phase13ApplicabilityReceipt.Skip,
            Phase13ApplicabilityReceipt.Activated {

    record Skip(
        String reason, // exact C17_GATE_CLOSED
        EvidenceDigest schedulerDecisionDigest,
        EvidenceDigest alnsOnlyPlanIdentity,
        ImmutableEvidenceRef signedApplicabilityEnvelope,
        EvidenceDigest actionTimeVerificationReceiptDigest
    ) implements Phase13ApplicabilityReceipt {}

    record Activated(
        EvidenceDigest phase14aAcceptanceReceiptDigest,
        EvidenceDigest phase13AcceptedHandoffDigest,
        ImmutableEvidenceRef signedApplicabilityEnvelope,
        EvidenceDigest actionTimeVerificationReceiptDigest,
        EvidenceDigest lastAcceptedAlnsOnlyRollbackPointDigest
    ) implements Phase13ApplicabilityReceipt {}
}
```

`Skip`에는 pool/model/outcome/hybrid/`E-P13-*`가 없어야 한다.
`Activated`에는 accepted Phase 13 evidence와 selected substituted runtime일 때의
applicable Phase 12 evidence가 있어야 한다. 두 branch 모두 현재 signed artifact는
`NOT_PRODUCED`다.

Closed path의 factory/verification은 Phase 13 assembly 밖에 있어야 한다. Phase 13
class가 없다는 사실만으로 `Skip`을 합성하지 않고, scheduler-owned signed control record와
current action-time verification이 없으면 14B를 fail closed한다. Open path의 Phase 13
owner는 accepted 14A receipt와 `C-17` 전체 승인, Phase 13 accepted evidence가 모두
존재한 뒤 `Phase13ActivatedHandoff`만 생산한다. 그 handoff 안에는 Phase 14B signature
profile/trust root/revocation/freshness/action-time verdict가 없다. Phase 14B가 official
hybrid를 별도로 선택할 때 control plane의 signed envelope와 current verification을
sibling input으로 받아 위 `Activated` consumer wrapper를 만든다. 어느 artifact도
Phase 14의 official calibration, `Q-BENCH-02`, provider deployment 또는 production
authority를 대신하지 않는다.

### 9.8 Official manifest와 run outcome 후보

```java
// PROPOSED INTERNAL
public record OfficialExecutionManifest(
    String manifestId,
    String schemaVersion,
    EvidenceDigest manifestFingerprint,
    EvidenceDigest sourceBuildRuntimeDigest,
    EvidenceDigest problemTravelProfileDigest,
    EvidenceDigest alnsParameterAuthorityDigest,
    EvidenceDigest executionValueAuthorityDigest,
    EvidenceDigest phase14aAcceptanceReceiptDigest,
    EvidenceDigest phase13ApplicabilityDigest,
    EvidenceDigest providerDeploymentEvidenceDigest,
    EvidenceDigest declaredPortfolioAndWorkerPlanDigest,
    EvidenceDigest candidateAndResultVerifierVersionDigest,
    EvidenceDigest signedSealDigest
) {}

public sealed interface OfficialRunOutcome
    permits OfficialRunOutcome.Publishable,
            OfficialRunOutcome.Incomplete,
            OfficialRunOutcome.Invalid {

    record Publishable(
        EvidenceDigest publishableResultDigest,
        EvidenceDigest officialExecutionReceiptDigest,
        EvidenceDigest bothVerifierReportsDigest
    ) implements OfficialRunOutcome {}

    record Incomplete(
        Set<String> missingOrAbnormalWorkerIds,
        EvidenceDigest reportDigest
    ) implements OfficialRunOutcome {}

    record Invalid(
        List<String> failures,
        EvidenceDigest reportDigest
    ) implements OfficialRunOutcome {}
}
```

```java
// PROPOSED INTERNAL
public interface OfficialManifestFactory {
    OfficialManifestResult create(
        OfficialManifestDraft draft,
        VerifiedGateBundle verifiedGates
    );
}

public interface OfficialBenchmarkRunner {
    OfficialRunOutcome execute(OfficialExecutionManifest manifest);
}
```

Manifest factory는 absent numeric, unknown enum, defaulted field, stale signature,
wrong subject/scope/provider와 unavailable portfolio의 권위 없는 silent skip을 거부한다.

### 9.9 Cutover state와 command 후보

아래 이름은 public/wire contract가 아닌 `PROPOSED INTERNAL`이다.

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
    permits StartShadow,
            HoldShadow,
            ApproveCanary,
            StartCanary,
            HoldCanary,
            ApproveActivation,
            Activate,
            RequestRollback,
            ExecuteRollback,
            AbortCutover {}

public record CutoverSnapshot(
    String cutoverId,
    ProposedCutoverState state,
    String controlStateVersion,
    String providerPointerVersion,
    EvidenceDigest activePointerDigest,
    EvidenceDigest desiredPointerDigest,
    EvidenceDigest manifestDigest,
    EvidenceDigest deploymentDigest,
    EvidenceDigest lastSafePointDigest,
    String pendingTransitionId,
    EvidenceDigest providerActionReceiptDigest,
    String observationWindowRef
) {}
```

```java
// PROPOSED INTERNAL
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

### 9.10 State transition 표

| From | Command | Required evidence/authority | To |
|---|---|---|---|
| `PREPARED` | `START_SHADOW` | Official card, provider deployment, migration/rollback ready | `SHADOW_RUNNING` |
| `SHADOW_RUNNING` | `HOLD_SHADOW` | Complete observation 또는 stop | `SHADOW_HOLD` |
| `SHADOW_HOLD` | `APPROVE_CANARY` | Separate scoped canary authority | `CANARY_APPROVED` |
| `CANARY_APPROVED` | `START_CANARY` | Exact pointer, healthy rollback, valid authority | `CANARY_RUNNING` after receipt/read-back |
| `CANARY_RUNNING` | `HOLD_CANARY` | Durable window complete 또는 manual hold | `CANARY_HOLD` |
| `CANARY_HOLD` | `APPROVE_ACTIVATION` | Separate activation authority + canary acceptance | `ACTIVATION_APPROVED` |
| `ACTIVATION_APPROVED` | `ACTIVATE` | Exact valid scope/pointer/deployment/manifest | `ACTIVE` after receipt/read-back |
| Running/hold/active | `REQUEST_ROLLBACK` | Stop condition 또는 authorized operator | `ROLLBACK_REQUESTED` |
| `ROLLBACK_REQUESTED` | `EXECUTE_ROLLBACK` | Last safe point, role, reconciliation | `ROLLED_BACK` after receipt/read-back |
| Before active | `ABORT` | Phase owner, no forward action | `ABORTED` |

### 9.11 Official manifest pseudocode

```text
createOfficialManifest(draft):
  reports = verifyEachGateIndependently(draft.gateRefs)
  require every required report == PASS
  require no absent/defaulted/unknown numeric field
  require integer fixture authority subject == exact fixture bytes
  require approved Great Circle policy/reference vectors
  require ALNS parameters and execution values have separate approvals
  require phase13 receipt is exact Skip or accepted Activated for this action
  require provider deployment digest matches selected provider
  require declared 4x2 availability accounting is complete
  return immutable canonical manifest + signed seal
```

### 9.12 Official run pseudocode

```text
officialRun(manifest):
  reverify seal, scope and authority graph
  declaredCandidates = exact manifest availability table
  execute every AVAILABLE construction candidate
  require at least one candidate available
  champion = stable phase-one fan-in

  declaredWorkers = exact worker IDs in manifest
  execute/retry workers without changing logical ID, seed, warm start or work
  outcomes = read exact refs, never prefix-list for authority
  require outcomes.keySet == declaredWorkers
  require every worker normal termination and candidateVerifier == PASS

  champion = stable exact comparator(outcomes)
  final = finalizeAndResultVerify(champion)
  require final.resultVerifier == PASS

  replay = execute same manifest bytes with separate execution receipt
  require approved replay fingerprint contract
  return publishable card or typed INCOMPLETE/INVALID
```

### 9.13 Cutover pseudocode

```text
apply(command, expected):
  current = read exact cutover state
  require current.controlStateVersion == expected.controlStateVersion
  require legalTransition(current.state, command)
  verify every evidence digest and current action scope

  if traffic/pointer changes:
    reverify production authority now
    require rollback target/role/telemetry healthy
    observed = providerControl.readExact()
    require observed.version == expected.providerPointerVersion
    require observed.digest == current.activePointerDigest

  if structuralStopCondition:
    return HOLD or REQUEST_ROLLBACK

  transitionId = deterministic(command, current, authority)
  append immutable intent(transitionId)
  pending = CAS current control version
            to same semantic state + transitionId

  providerReceipt = providerControl.CAS(
      expectedPointerVersion,
      current.activePointerDigest,
      desiredPointerDigest,
      transitionId)

  readBack = providerControl.readExact()
  require receipt + readBack == exact desired pointer and traffic scope
  reconcile in-flight work and observation window
  finalState = pureReducer(pending, command, providerReceipt, readBack)
  require no traffic-semantic final state without verified receipt/readBack
  return final control-state CAS + immutable result event
```

### 9.14 Durable observation pseudocode

```text
resumeObservation(policy, persistedWindow, clockAuthority):
  verify signed policy and required telemetry/alarm set
  verify absolute deadline and clock authority provenance
  if clock jump, expiry, missing telemetry or unknown origin:
    return HOLD_CLOCK_OR_TELEMETRY_INDETERMINATE
  remaining = approvedClock(deadline) - approvedClock(now)
  use process-local monotonic only inside this attempt
  return CONTINUE or COMPLETE according to approved predicate
```

새 JVM의 monotonic origin, provider remaining time 또는 wall-clock library default로
완료를 추정하지 않는다.

## 10. Ordered work packages

모든 WP는 순서대로 실행한다. 각 표의 command/test 이름은 target reactor가 accepted된
뒤 확정할 **future candidate**다. 현재 frozen inventory에서 green으로 실행 가능한
Phase 14 module/test/profile이 아니다.

### WP14-0 — Entry receipt, source impact와 drift freeze

| 항목 | 지시 |
|---|---|
| 목적/이유 | 문서 존재나 partial scaffold를 accepted predecessor로 오인하지 않고 14A가 소비할 exact evidence graph를 고정한다. |
| 사전조건/권한 | Scheduler task, Phase 00~08 owners/reviewers; 문서·inventory review는 gate closed에서도 가능 |
| 예상 file/package/type | `Phase14AEntryReceipt`, `SourceContractImpactReport`, blocker ledger; production code보다 evidence schema/test 우선 |
| 구체 행동 | HEAD/live drift 분리, cited heading semantic diff, accepted `E-P00-*`~`E-P08-*` closure, source/build/rollback identity, legacy exclusion |
| 근거 | Master §15, Plan Phase 14, Phase 14 §3.2, review F-P14-008 |
| 금지 shortcut | Adjacent hash=compatibility, review file=implementation acceptance, root build=Phase evidence |
| 검증 command/test | Future `Phase14PredecessorReceiptTest`; evidence graph missing/cycle/mixed-commit/stale-review negative cases |
| 기대 결과 | Missing/stale/mixed evidence 0; 14A gate와 14B-only gate를 분리한 receipt |
| 실패 해석 | `PREDECESSOR_EVIDENCE_MISSING` 또는 `SOURCE_CONTRACT_IMPACT_UNREVIEWED` |
| Rollback/last safe | Code/run 0, 문서·offline test design 유지 |
| 사람 checkpoint/handoff | Scheduler가 exact entry receipt를 승인한 뒤 WP14-1/2로 전달 |

### WP14-1 — 14A fixture, oracle와 protocol preregistration

| 항목 | 지시 |
|---|---|
| 목적/이유 | 첫 measured run 전에 무엇을 왜 측정하고 어떻게 판정할지 고정해 post-hoc 선택을 막는다. |
| 사전조건/권한 | WP14-0, Input/Matrix, Benchmark/Quality, independent oracle owner; production signing/Phase 13/provider 불필요 |
| 예상 file/package/type | `IntegerTravelFixtureAuthority`, `GreatCirclePolicyAuthority`, `CalibrationPlan`, `AcceptancePolicy`, `DeclaredRunId` |
| 구체 행동 | Corpus/fixture digest/provenance, all-cell integer scan, generated `U`/speed provenance, Great Circle vector, candidate config, seed/repeat/run matrix, runtime, analysis/exclusion/abort/criteria freeze |
| 근거 | Master §8/§13~14, Domain §6/§17, questions `Q-MTX-*`, Phase 14 §6 |
| 금지 shortcut | FLOOR fixture를 official로 relabel, earth radius/library default, 누락 threshold fallback, 실행 뒤 seed 선택 |
| 검증 command/test | `IntegerTravelFixtureAuthorityTest`, `GreatCirclePolicyAuthorityTest`, `CalibrationPlanFreezeTest`, independent scanner/vector oracle |
| 기대 결과 | 모든 freeze field가 digest에 영향; absent/open criteria면 plan execution blocked |
| 실패 해석 | `INTEGER_TRAVEL_AUTHORITY_MISSING`, `GREAT_CIRCLE_POLICY_MISSING`, `CALIBRATION_PLAN_NOT_FROZEN` |
| Rollback/last safe | Test-only hand fixture와 explicit exploratory plan; official label 0 |
| 사람 checkpoint/handoff | Owners가 plan/authority scope를 승인한 뒤 immutable plan을 WP14-2로 전달 |

### WP14-2 — ALNS-only calibration execution과 complete accounting

| 항목 | 지시 |
|---|---|
| 목적/이유 | 모든 declared run을 동일 규칙으로 실행하고 failure/timeout/resource를 품질 결과와 섞지 않는다. |
| 사전조건/권한 | Frozen WP14-1 plan, accepted Phase 06/07/08, approved isolated experiment environment와 cost guard |
| 예상 file/package/type | `CalibrationRunner`, immutable raw run index, `CalibrationExecutionOutcome`, neutral `PreAnalysisCalibrationRecord`, telemetry/cost artifacts |
| 구체 행동 | case×candidate×seed×repeat 실행, logical run/attempt 분리, retry identity 보존, candidate/result verifier, exact-key artifact 저장, complete set reconciliation; runner output은 raw/producer evidence까지만 닫고 독립 digest/verdict를 넣지 않음 |
| 근거 | Phase 06 replay, Phase 07 both-gate, Phase 08 §16.5, Phase 14 §6.3 |
| 금지 shortcut | Successful-only table, fastest run champion, prefix listing completeness, timeout=max-steps, failed seed 삭제 |
| 검증 command/test | `CalibrationDeclaredRunCompletenessIT.accountsForEveryDeclaredRunIncludingFailures()`, `CalibrationProducerBoundaryArchitectureTest.runnerCannotWriteIndependentAnalysisIdentity()`, retry/fault/resource/telemetry tests |
| 기대 결과 | Declared=observed exact set, 모든 outcome typed, raw evidence immutable, 다음 disposition은 방향 없는 `EXPERIMENT_REQUIRED` |
| 실패 해석 | `CALIBRATION_RUN_SET_INCOMPLETE` 또는 `INVALID_EVIDENCE`; partial acceptance 금지 |
| Rollback/last safe | Candidate set을 incomplete/invalid로 seal, frozen plan과 raw evidence 보존 |
| 사람 checkpoint/handoff | Complete execution outcome과 neutral record만 WP14-3 independent analyzer에 전달; independent digest는 아직 없음 |

### WP14-3 — Independent recomputation, acceptance와 14A receipt

| 항목 | 지시 |
|---|---|
| 목적/이유 | Producer summary와 무관한 oracle/분석으로 correctness·quality·resource·variance·replay를 검증하고 단방향 evidence DAG를 닫는다. |
| 사전조건/권한 | WP14-2 `CalibrationExecutionOutcome.Complete` + neutral `EXPERIMENT_REQUIRED`, preregistered method, producer와 분리된 analyzer/reviewer/acceptance authority |
| 예상 file/package/type | `IndependentCalibrationAnalyzer`, `IndependentCalibrationAnalysis`, `CalibrationAcceptanceEvaluator`, `AlnsBenchmarkAcceptanceReceiptFactory`, authority envelope/verification, M/R/receipt artifacts |
| 구체 행동 | Exact execution outcome에서 raw table 독립 재생성, producer equality, oracle sensitivity, both-verifier hard gate, paired comparison, timeout/resource, variance/replay, limitation; eligible decision → M seal → review R → subject/issuer/claims/provenance/signature/trust-root action-time 검증 → receipt(M,R,authority) |
| 근거 | Plan §9, Phase 14 §6.4A/§13.2, review ALNS-first addendum |
| 금지 shortcut | Producer가 자기 receipt 서명, review 뒤 M 수정, open criterion 자동 pass, 서로 다른 candidate 성분 cherry-pick |
| 검증 command/test | `CalibrationIndependentAnalysisIT`, `CalibrationProducerBoundaryArchitectureTest`, `AlnsBenchmarkAcceptanceReceiptTest`, `PostReviewAcceptanceAuthorityVerifierContractTest`, manifest-cycle/one-field-corruption/sensitivity tests |
| 기대 결과 | Execution→independent 단방향, exact M/R/authority digest, criteria open 0, explicit `ACCEPTED`, restart/handoff/last-safe를 가진 immutable receipt |
| 실패 해석 | `BLOCKED_OPEN_VALUE`, `INDEPENDENT_ANALYSIS_MISMATCH`, typed authority verification failure 또는 review rejection |
| Rollback/last safe | Frozen plan + complete execution + independent/review artifact를 보존; receipt 미발행, Phase 13/14B action 0 |
| 사람 checkpoint/handoff | 14A acceptance owner가 receipt를 Phase 13 optional gate와 WP14-4에 별도 전달; 자동 시작 없음 |

### WP14-4 — 14B trust, separate official values와 Phase 13 applicability

| 항목 | 지시 |
|---|---|
| 목적/이유 | 14A evidence authority를 14B production authority와 분리하고 모든 official input을 별도 승인한다. |
| 사전조건/권한 | Accepted 14A receipt, Phase 09~11 applicable receipts, Security/Release trust approval, Algorithm/Quality/Benchmark authorities |
| 예상 file/package/type | `SignedEvidenceVerifier`, `OfficialAlnsParameters`, `OfficialExecutionValues`, `Phase13ApplicabilityReceipt` consumer |
| 구체 행동 | Signature canonicalization/trust/revocation/time/freshness, subject/scope/action 검증; separate ALNS/Q-BENCH approvals; scheduler-owned closed `Skip`, gate-open Phase 13 `ActivatedHandoff`, Phase 14B/control-plane signed `Activated` consumer wrapper의 owner와 시점을 각각 검증 |
| 근거 | Phase 13 §6.5/§14.3~14.4, Phase 14 §3.1/§5.3, review F-P14-010 |
| 금지 shortcut | Test/self-signed signer, 과거 PASS 재사용, one approval bag, Phase 13 forced call, 14B→13 reverse entry |
| 검증 command/test | `SignedEvidenceVerifierContractTest`, `OfficialParameterSeparationTest`, `Phase13ApplicabilityTest`, `Phase13ApplicabilityDependencyTest`; closed path의 Phase 13 source/dependency/class-load/`E-P13-*`/self-signed receipt count 0 |
| 기대 결과 | Trusted scoped evidence만 PASS; Skip에는 P13 artifact 0, Activated에는 accepted P13 closure |
| 실패 해석 | `UNTRUSTED_OR_INVALID_SIGNATURE`, `AUTHORITY_SCOPE_MISMATCH`, `PHASE13_SCOPE_MISMATCH` |
| Rollback/last safe | Accepted ALNS-only receipt, official manifest 0 |
| 사람 checkpoint/handoff | Security/Algorithm/Benchmark/Scheduler가 각 receipt를 별도 승인해 WP14-5로 전달 |

### WP14-5 — Official manifest, all-worker run, exact card와 replay

| 항목 | 지시 |
|---|---|
| 목적/이유 | 한 exact authority envelope에서만 official result를 만들고 incomplete worker/one-verifier/other-manifest 비교를 차단한다. |
| 사전조건/권한 | WP14-4 verified gate bundle, selected execution path, official-run environment authorization |
| 예상 file/package/type | `OfficialManifestFactory`, `OfficialBenchmarkRunner`, `OfficialRunCompletenessVerifier`, `OfficialBenchmarkCard` |
| 구체 행동 | 4×2 availability 전수 선언, all available screen, all declared worker, stable fan-in, both verifier, exact comparator, immutable execution receipt, identical-manifest replay |
| 근거 | Master §14, `Q-BENCH-01~03`, Phase 14 §7, review F-P14-003/004 |
| 금지 shortcut | 항상 8개라고 거짓 주장, silent unavailable fallback, partial worker, fastest winner, manifest mutable-used flag, scalar objective |
| 검증 command/test | `OfficialManifestFactoryTest`, `OfficialRunCompletenessIT`, `OfficialBenchmarkComparatorTest`, `OfficialReplayIT`, corruption suite |
| 기대 결과 | Worker 누락/abnormal 0, two verifier PASS, exact vector/card/replay PASS |
| 실패 해석 | `OFFICIAL_MANIFEST_INVALID`, `OFFICIAL_RUN_INCOMPLETE`, `CANDIDATE_OR_RESULT_VERIFICATION_FAILED` |
| Rollback/last safe | No baseline/publication; immutable failed receipt와 이전 accepted receipt 유지 |
| 사람 checkpoint/handoff | Benchmark/independent reviewer가 card를 승인한 뒤 provider shadow packet을 WP14-6으로 전달 |

### WP14-6 — Selected-provider deployment, migration, shadow와 rollback rehearsal

| 항목 | 지시 |
|---|---|
| 목적/이유 | 실제 provider revision이 local semantic authority를 보존하고 되돌릴 수 있음을 production traffic 전 증명한다. |
| 사전조건/권한 | WP14-5 card, Phase 11 accepted; substituted provider면 applicable Phase 12 accepted; non-production deploy scope |
| 예상 file/package/type | `ProviderDeploymentEvidence`, compatibility matrix, migration mapping, shadow report, `LastSafePoint` |
| 구체 행동 | IaC/change-set/revision 검증, immutable candidate deploy, source→destination digest copy/read-back, same-manifest shadow, fault/security/ops/cost, rollback rehearsal |
| 근거 | Master §16.2, Integrated §18, Phase 11 §18.3, Phase 12 §18.4 |
| 금지 shortcut | AWS test를 substituted provider에 사용, emulator=actual, deploy exit 0=authority, mutable latest, legacy GCP=target fallback |
| 검증 command/test | AWS 선택 시 `ProviderDeploymentEvidenceIT`, `ArtifactMigrationDigestIT`, `AwsShadowParityIT`; 대체 provider는 accepted Phase 12 동등 suite |
| 기대 결과 | Provider/local semantic mismatch 0, security/ops/cost evidence, verified rollback target |
| 실패 해석 | `PROVIDER_DEPLOYMENT_EVIDENCE_MISSING`, `SHADOW_NOT_COMPARABLE`, parity/security gate failure |
| Rollback/last safe | New starts off, previous accepted path/pointer 유지, immutable orphan은 retention/quarantine |
| 사람 checkpoint/handoff | Platform/Security/Ops/FinOps가 deployment evidence를 승인해 WP14-7 preflight로 전달 |

### WP14-7 — Pure cutover reducer, authority와 preflight

| 항목 | 지시 |
|---|---|
| 목적/이유 | Traffic action 전에 legal state, action-specific authority, pointer precondition, stop/clock/rollback 조건을 검증한다. |
| 사전조건/권한 | WP14-6, approved cutover policy, rollback owner; canary authority는 아직 action별로 검증 |
| 예상 file/package/type | `CutoverStateMachine`, `ProductionAuthorityVerifier`, `CutoverObservationPolicy`, provider control port contract |
| 구체 행동 | Pure transition table, distinct control/pointer versions, pending intent, authority expiry/revocation/scope, durable deadline/telemetry, response-loss reconciliation tests |
| 근거 | Phase 14 §8~9, review F-P14-001/007 |
| 금지 shortcut | Provider call in reducer, one opaque version, wall-clock fallback, missing telemetry=healthy, test가 production action 수행 |
| 검증 command/test | `CutoverStateMachineTest`, `ProductionAuthorityVerifierTest`, `CutoverObservationWindowTest`, `CutoverControlPortContractTest` |
| 기대 결과 | Illegal/expired/wrong-scope/clock-gap 모두 fail-closed; false terminal state 0 |
| 실패 해석 | `PRODUCTION_AUTHORITY_MISSING`, `CUTOVER_STOP_CONDITION` 또는 hold |
| Rollback/last safe | Shadow/pre-canary hold, traffic/pointer unchanged |
| 사람 checkpoint/handoff | Product/Release/Security/Ops가 exact canary authority를 발행할 때만 WP14-8 진행 |

### WP14-8 — Canary, hold와 separate activation 또는 rollback

| 항목 | 지시 |
|---|---|
| 목적/이유 | 승인된 제한 traffic을 실제 provider pointer에 적용하고 관찰한 뒤 별도 full activation 결정을 내린다. |
| 사전조건/권한 | WP14-7, exact canary authority, healthy last safe point; activation은 canary hold 뒤 별도 authority |
| 예상 file/package/type | `CutoverCoordinator`, immutable transition intent/result, canary/activation/rollback records |
| 구체 행동 | Canary provider CAS/read-back/final state, persisted observation/hold, canary acceptance; separate activation revalidation/action 또는 immediate rollback/reconciliation |
| 근거 | Master §16.2, Phase 14 §8.2~8.5, review F-P14-005 |
| 금지 shortcut | Canary approval 재사용해 full activate, control state 먼저 ACTIVE, scope 확장, rollback artifact delete |
| 검증 command/test | Release pipeline read-only preflight/post-read-back; `ProductionAuthorityScopeIT`, selected-provider canary contract, `ProductionActivationIT` 또는 `RollbackRehearsalIT` |
| 기대 결과 | Exact active pointer + no pending/in-flight unresolved + `ACTIVE`, 또는 independently verified `ROLLED_BACK` |
| 실패 해석 | Rollback/reject는 안전 terminal일 수 있으나 Phase 14B `ACCEPTED` 아님; rollback failure는 incident |
| Rollback/last safe | Previous accepted pointer/revision, immutable evidence 전부 보존 |
| 사람 checkpoint/handoff | Active면 activation record 후보, 아니면 terminal rollback/reject record를 WP14-9로 전달 |

### WP14-9 — Evidence closure, independent review와 Operations handoff

| 항목 | 지시 |
|---|---|
| 목적/이유 | Exact evidence graph와 현재 production identity를 listing 없이 해석할 수 있게 봉인한다. |
| 사전조건/권한 | WP14-8 terminal result, independent Phase 14 reviewer, scheduler와 Operations owner |
| 예상 file/package/type | `ImplementationEvidenceIndex`, `ProductionActivationRecord` 또는 `RollbackRecord`/`CutoverRejectedRecord`, runbooks |
| 구체 행동 | All ref/digest/signature/closure audit, active manifest/build/deployment/pointer, limitation/open gates, authority expiry, owner/on-call, rollback refs seal |
| 근거 | Plan §9~12, Phase 14 §13/§16 |
| 금지 shortcut | Empty activation record, mutable latest, prefix discovery, review/receipt 역참조, secret/PII |
| 검증 command/test | `ImplementationEvidenceIndexTest`, `ProductionActivationRecordTest`, graph-cycle/link/digest/signature audit |
| 기대 결과 | Active 성공 때만 activation record; rollback/reject는 별도 terminal record; Operations exact lookup 가능 |
| 실패 해석 | `REVIEW_PENDING`, `GATED`, `ROLLED_BACK` 또는 `FAILED`; scheduler가 구분 |
| Rollback/last safe | 이미 terminal인 state/pointer를 임의 변경하지 않고 evidence closure만 재수행 |
| 사람 checkpoint/handoff | Independent review accepted 뒤 scheduler만 authoritative status를 갱신 |

## 11. 테스트를 설계하는 법: “실행됐다”가 아니라 “권한과 의미가 보존됐다”

### 11.1 Fixture, builder, oracle을 분리한다

신규 구현자가 가장 먼저 피해야 할 일은 benchmark 입력, manifest 생성, expected result 계산을 한 helper에 넣는 것이다. 그러면 같은 버그가 입력과 기대값 양쪽에 복제되어 green이 된다.

| 역할 | 하는 일 | 하면 안 되는 일 | 제안 이름 |
|---|---|---|---|
| Fixture | 원본 instance, vehicle/request, D/U/speed의 존재·부재·오류 조합을 고정한다. | Production normalizer를 호출해 expected fixture를 만들지 않는다. | `RpdptwOfficialFixture` |
| Builder | 긴 manifest/receipt를 읽기 좋게 조립하되 필수값 누락을 숨기지 않는다. | `official=true`, 45 km/h, tolerance, provider를 몰래 기본값으로 넣지 않는다. | `CalibrationPlanBuilder`, `OfficialManifestBuilder` |
| Oracle | 독립적인 작은 문제의 정답 또는 검증 가능한 불변식을 제공한다. | SUT의 comparator/verifier/거리 계산기를 그대로 재사용하지 않는다. | `IndependentLexicographicOracle`, `TinyScheduleOracle` |
| Corruptor | 한 번에 한 축만 손상시킨다. | 여러 필드를 동시에 바꿔 어떤 검사가 잡았는지 모호하게 하지 않는다. | `EvidenceCorruptor` |
| Authority fixture | 서명·scope·expiry·revocation·nonce의 경계값을 만든다. | Production key나 실제 권한을 test fixture에 넣지 않는다. | `TestAuthorityFixture` |

작은 탐색 순서는 다음과 같다.

1. 요청 2개, 차량 1~2대, 방향성 D/U가 명시된 hand-checkable fixture를 만든다.
2. 동일한 unassigned 수에서 vehicle count가 승패를 바꾸는 쌍을 만든다.
3. 앞 두 항목도 같고 distance만 다른 쌍, 마지막으로 operational seconds만 다른 쌍을 만든다.
4. D/U가 모두 없는 경우, D만 없는 경우, U만 없는 경우, speed가 없는 경우, present-but-invalid speed인 경우를 각각 별도 fixture로 만든다.
5. 같은 semantic manifest를 재직렬화했을 때 digest가 같고, 의미 필드 하나가 달라지면 digest가 달라지는지 확인한다.

완료 신호는 “fixture 수가 많다”가 아니라 각 fixture가 하나의 규칙을 이름으로 설명하고, 독립 oracle과 source requirement를 가리키는 것이다.

### 11.2 Red → green → false-green 방지

각 계약은 다음 순서로 구현한다.

1. **Red:** 잘못된 구현 또는 아직 없는 구현에서 정확한 failure code로 실패하는 가장 작은 test를 먼저 만든다.
2. **Green:** 해당 규칙 하나만 만족시키는 최소 구현을 만든다.
3. **Mutation:** comparator tuple 순서를 바꾸거나, `CEILING`을 `HALF_UP`으로 바꾸거나, verifier 하나를 생략하거나, authority scope 검사를 끄면 test가 반드시 red가 되는지 확인한다.
4. **Independent replay:** production builder와 다른 경로로 만든 manifest/fixture에서도 같은 판정이 나오는지 확인한다.
5. **Negative evidence:** 빈 디렉터리, 이름만 그럴듯한 파일, unsigned receipt, digest mismatch가 절대 acceptance로 집계되지 않는지 확인한다.

False-green을 막는 필수 규칙:

- expected value를 production method의 반환값으로 만들지 않는다.
- test 이름에 `valid`, `works` 같은 모호한 말을 쓰지 말고 조건과 결과를 쓴다.
- exception class만 보지 말고 stable failure code와 phase/substage context도 검사한다.
- unavailable worker test와 failed worker test를 분리한다.
- rollback 성공 test가 activation acceptance를 기대하지 않게 한다.
- local emulator 결과를 actual-provider evidence fixture로 라벨링하지 않는다.
- clock test는 test용 승인 clock을 주입하되 “현재 시각 없으면 통과”를 허용하지 않는다.
- generated evidence를 test 종료 후 다시 읽어 digest, signature, closure, referenced identity를 검증한다.

### 11.3 테스트 class/method 계약표

아래 이름은 모두 **PROPOSED**다. 저장소에 같은 책임의 accepted 이름이 생기면 의미를 보존한 채 그 이름을 따른다.

| 계층 | 제안 class / 핵심 method | 핵심 fixture·builder·oracle | PASS 판정 | 적용 여부 |
|---|---|---|---|---|
| Unit—travel | `TravelAuthorityTest#missingDistanceUsesGreatCircleMeterHalfUp`, `#missingDurationUsesCeilingPerVehicleSpeed`, `#missingSpeedUsesOnlyApproved45Kmh`, `#presentInvalidSpeedIsRejected` | Hand-calculated coordinates, per-vehicle speed cases | meter/second 값과 provenance가 정확하고 invalid present value는 stable code로 reject | 14A/14B 필수 |
| Unit—comparator | `OfficialComparatorTest#ordersByExactFourTuple` | Four tie-breaking pairs, independent tuple oracle | 네 축의 strict lexicographic order가 모두 맞음 | 14A/14B 필수 |
| Unit—state | `CutoverStateMachineTest#rejectsIllegalTransition`, `#doesNotMakeProviderCall` | Pure state/action table | 허용 edge만 생성하고 side effect 0 | 14B 필수 |
| Unit—authority | `ProductionAuthorityVerifierTest#rejectsExpiredRevokedWrongScopeAndReplay` | Test-only signed envelope | subject/action/resource/from/to/expiry/revocation/nonce 모두 일치할 때만 PASS | 14B 필수 |
| Contract—evidence | `EvidenceEnvelopeContractTest#rejectsDigestSignatureAndClosureMismatch` | Canonical bytes + one-axis corruptor | 깨진 축 각각 정확한 failure code | 14A/14B 필수 |
| Contract—ports | `OfficialWorkerPortContractTest#allDeclaredWorkersProduceTerminalReceipt`, `SolutionVerifierPortContractTest#bothVerifiersAgree` | 모든 declared worker/verifier adapter | 누락·중복·silent fallback 0 | 14A/14B 필수 |
| Contract—provider | `CutoverControlPortContractTest#casReadBackAndReconcileResponseLoss` | Selected-provider contract harness | CAS와 read-back이 일치하고 response loss 후 실제 pointer로 수렴 | 14B 필수, provider 선택 후 |
| Integration—calibration | `CalibrationPipelineIT#runsAllAvailablePairsAndSealsReviewableBundle` | Frozen calibration fixture set | availability closure, all available runs, both verifier, immutable outcome | 14A 필수 |
| Integration—official | `OfficialRunCompletenessIT#requiresAllDeclaredWorkersAndBothVerifiers` | Exact official manifest | abnormal/missing worker 0, both verifier PASS, exact comparator | 14B 필수 |
| Integration—manifest | `OfficialReplayIT#replaysIdenticalManifestWithoutSemanticDrift` | Manifest M + execution receipts E1/E2 | semantic identity와 selected result가 동일하고 receipts는 별도 | 14B 필수 |
| Integration—cutover | `ProductionActivationIT#persistsIntentBeforePointerAndFinalStateAfterReadBack` | Fake provider with observable calls | intent→provider CAS/read-back→reconcile→control CAS 순서 | 14B 필수 |
| E2E—non-production | `OfficialCandidateToShadowE2E` | Accepted 14A receipt, official values, Phase13 applicability, non-prod provider | Card→deploy→migration→shadow까지 identity 연속 | 14B 필수 |
| E2E—production | Release-run canary/activation scenario | Real approved provider, action-specific authority | Exact pointer/traffic/read-back/observation/activation record | 권한 있는 release window에서만; 일반 Maven test로 실행 금지 |
| Architecture | `Phase14DependencyArchitectureTest#keepsDomainAndApplicationProviderAgnostic`, `#forbidsReverseEvidenceEdges` | Package graph, evidence graph | Domain→provider 의존과 review/receipt reverse edge 0 | 필수 |
| Fault | `CutoverFaultInjectionIT#recoversAfterProviderSuccessResponseLoss`, `#failsClosedOnTelemetryGap` | Timeout/duplicate/reorder/lost response/clock anomaly | false terminal 0, hold/rollback/reconcile가 결정적 | 14B 필수 |
| Corruption | `OfficialEvidenceCorruptionTest#rejectsEachProtectedFieldMutation` | Manifest/receipt/signature/digest corruptor | 보호 필드 한 축 변조마다 reject | 필수 |
| Reproducibility | `OfficialReproducibilityIT#matchesAcrossCleanBuildAndReplay` | Clean build identity, exact manifest, approved repeat policy | 허용된 nondeterminism 밖의 semantic drift 0 | 14A/14B 필수 |
| Security | `ProductionAuthoritySecurityIT#preventsReplayAndCrossScopeUse`, secret scan | Expired/revoked/replayed/cross-provider tokens | 권한 오용 0, evidence 내 secret/PII 0 | 14B 필수 |
| Performance | `OfficialResourceEnvelopeIT#staysWithinApprovedCpuMemoryAndTimeoutEnvelope` | Approved workload/envelope | numeric limit이 manifest/evidence와 일치하고 초과는 fail-closed | 값 승인 후 필수 |

#### Canonical 37-method coverage manifest

위 교육용 분류표는 원본 Phase 14의 exact matrix를 대체하지 않는다. 아래 37개
class/method는 [원본 Phase 14 §11.2](../../phases/phase-14-official-calibration-cutover.md#112-exact-classmethod-matrix)의
최소 coverage authority다.

```yaml
canonical_exact_test_source:
  git_blob: c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26
  sha256: c8f3b4fd2e48d35547d7e2fe31169a5e0a882730859ac97d9c5f60a165802eae
  exact_method_count: 37
current_discovery_status: BLOCKED_PENDING_CONTRACT
current_discovered_phase14_method_count: 0
```

`R-BLOCKED`는 Phase 14 stage가 열리면 필수지만 현재 API/module/Failsafe contract가 없어
red/blocked로 보존한다는 뜻이다. `P-BLOCKED`는 selected-provider 또는 production
authority branch가 열릴 때 필수이며 지금 N/A/PASS로 바꾸지 않는다는 뜻이다.

| ID | Canonical exact class#method | Exact oracle/requirement | WP · DoD · evidence | Disposition |
|---|---|---|---|---|
| `P14-T001` | `IntegerTravelFixtureAuthorityTest#rejectsAnyFractionalDistanceOrDuration()` | Fractional `D/U` 1개도 typed reject | WP14-1 · 14A-D/U · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T002` | `IntegerTravelFixtureAuthorityTest#doesNotRelabelRoundedLegacyFixtureAsApproved()` | Approval 없는 rounded legacy conversion 거부 | WP14-1 · 14A-fixture · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T003` | `IntegerTravelFixtureAuthorityTest#requiresCanonicalGeneratedDurationAndSpeedSourceProvenance()` | Formula/`CEILING`/missing-only 45/provenance drift fail | WP14-1 · 14A-D/U · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T004` | `GreatCirclePolicyAuthorityTest#requiresFunctionEarthModelPrecisionAndReferenceVectors()` | Function/model/precision/vector 누락마다 fail | WP14-1 · 14A-D/U · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T005` | `GreatCirclePolicyAuthorityTest#matchesEveryApprovedReferenceVectorWithMeterHalfUp()` | 승인 vector 전부 integer meter exact | WP14-1 · 14A-D/U · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T006` | `OfficialParameterSeparationTest#keepsExecutionValuesSeparateFromAlnsParameters()` | Cross-owned/hidden field 0 | WP14-4 · 14B-official values · `E-P14-CALIBRATION` | `R-BLOCKED` |
| `P14-T007` | `CalibrationPlanFreezeTest#fingerprintsCorpusBuildConfigSeedsAndEnvironment()` | 모든 freeze field가 digest에 영향 | WP14-1 · 14A-plan · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T008` | `CalibrationPlanFreezeTest#rejectsAbsentThresholdInsteadOfDefaulting()` | Absent criterion은 neutral blocked | WP14-1/3 · 14A-criteria · `E-P14-ALNS-BENCHMARK-ACCEPTANCE` | `R-BLOCKED` |
| `P14-T009` | `CalibrationAcceptanceEvaluatorTest#usesPreregisteredPairedMethodOnly()` | Frozen raw table의 preregistered method만 허용 | WP14-3 · 14A-independent decision · `E-P14-ALNS-BENCHMARK-ACCEPTANCE` | `R-BLOCKED` |
| `P14-T010` | `OfficialManifestFactoryTest#rejectsUnapprovedQBenchValues()` | Unsigned/open value에서 manifest 미생성 | WP14-4/5 · 14B-manifest · `E-P14-CALIBRATION` | `R-BLOCKED` |
| `P14-T011` | `OfficialManifestFactoryTest#rejectsTestSignerExpiredOrRevokedApproval()` | Test/expired/revoked approval typed reject | WP14-4/5 · 14B-trust · `E-P14-CALIBRATION` | `R-BLOCKED` |
| `P14-T012` | `Phase13ApplicabilityTest#doesNotRequireOrExecutePhase13WhenGateIsClosed()` | Phase 13 ref/call/class-load 0 | WP14-4 · 14B-applicability · `E-P14-CALIBRATION` | `R-BLOCKED` |
| `P14-T013` | `Phase13ApplicabilityTest#requiresAcceptedPhase13EvidenceForHybridManifest()` | Hybrid는 accepted Phase 13 closure 필수 | WP14-4 · 14B-applicability · `E-P14-CALIBRATION` | `R-BLOCKED` |
| `P14-T014` | `Phase13ApplicabilityTest#requiresSignedEnvelopeAndFreshActionVerificationForSkipAndActivated()` | Current subject/scope/action/trust `PASS` 외 전부 fail | WP14-4 · 14B-applicability · `E-P14-CALIBRATION` | `R-BLOCKED` |
| `P14-T015` | `Phase13ApplicabilityDependencyTest#allows14ATo13To14BButRejects14BTo13EntryAnd13To14AReceiptMutation()` | 허용 DAG만 존재, reverse/mutation 0 | WP14-4 · 14A/13/14B boundary · architecture evidence | `R-BLOCKED` |
| `P14-T016` | `OfficialManifestFactoryTest#accountsForEveryConstructionCombinationAndRejectsSilentUnavailableFallback()` | 4×2 전수 availability, 무권위 unavailable fail | WP14-5 · 14B-manifest · `E-P14-OFFICIAL-RUN` | `R-BLOCKED` |
| `P14-T017` | `OfficialBenchmarkComparatorTest#comparesFourComponentsLexicographically()` | Exact four-tuple winner/tie | WP14-5 · 14A/14B-comparator · `E-P14-OFFICIAL-RUN` | `R-BLOCKED` |
| `P14-T018` | `OfficialBenchmarkComparatorTest#doesNotUseStructuralTieBreakAsQualityDimension()` | Equal vector는 quality tie | WP14-5 · 14A/14B-comparator · `E-P14-OFFICIAL-RUN` | `R-BLOCKED` |
| `P14-T019` | `OfficialRunCompletenessVerifierTest#rejectsAnyMissingOrAbnormalDeclaredWorker()` | Partial success never complete | WP14-5 · 14B-workers · `E-P14-OFFICIAL-RUN` | `R-BLOCKED` |
| `P14-T020` | `CutoverStateMachineTest#acceptsOnlyLegalProposedTransitions()` | Illegal transition 전부 reject | WP14-7 · 14B-state · `E-P14-CUTOVER` | `R-BLOCKED` |
| `P14-T021` | `CutoverStateMachineTest#doesNotEnterTrafficSemanticStateBeforeProviderReceiptAndReadBack()` | Receipt/read-back 전 terminal 0 | WP14-7/8 · 14B-state · `E-P14-CUTOVER` | `R-BLOCKED` |
| `P14-T022` | `CutoverObservationWindowTest#restoresPersistedDeadlineAndFailsClosedOnClockOrTelemetryGap()` | Restart/clock/telemetry false completion 0 | WP14-7/8 · 14B-observation · `E-P14-CUTOVER` | `R-BLOCKED` |
| `P14-T023` | `ProductionAuthorityVerifierTest#rejectsWrongEnvironmentActionDigestScopeOrTime()` | Scope/action/time expansion 0 | WP14-7/8 · 14B-authority · `E-P14-CUTOVER` | `R-BLOCKED` |
| `P14-T024` | `RollbackCoordinatorTest#restoresExpectedPointerAndPreservesArtifacts()` | Pointer restored, artifact delete 0 | WP14-8 · rollback checklist · `E-P14-ROLLBACK` | `R-BLOCKED` |
| `P14-T025` | `SignedEvidenceVerifierContractTest#verifiesDigestBeforeDeserializeAndSignatureBeforeUse()` | Unverified object exposure 0 | WP14-3/4 · 14A-receipt/14B-trust · signed evidence report | `R-BLOCKED` |
| `P14-T026` | `CutoverControlPortContractTest#convergesDuplicateSameCommandAndRejectsDivergentReplay()` | Same/same converge, same/different fail | WP14-7/8 · 14B-CAS · `E-P14-CUTOVER` | `R-BLOCKED` |
| `P14-T027` | `CutoverControlPortContractTest#reconcilesProviderSuccessBeforeControlStateCommit()` | Response loss 후 같은 transition 수렴 | WP14-7/8 · 14B-CAS · `E-P14-CUTOVER` | `R-BLOCKED` |
| `P14-T028` | `CalibrationDeclaredRunCompletenessIT#accountsForEveryDeclaredRunIncludingFailures()` | Missing/excluded declared run 0 | WP14-2 · 14A-run closure · `E-P14-ALNS-BENCHMARK` | `R-BLOCKED` |
| `P14-T029` | `CalibrationIndependentAnalysisIT#recomputesProducerDecisionFromImmutableRawEvidence()` | Producer/independent table·decision equality | WP14-3 · 14A-independent analysis · `E-P14-ALNS-BENCHMARK-ACCEPTANCE` | `R-BLOCKED` |
| `P14-T030` | `OfficialRunCompletenessIT#publishesOnlyAfterAllWorkersAndBothVerifiersPass()` | False publication 0 | WP14-5 · 14B-workers/verifiers · `E-P14-OFFICIAL-RUN` | `R-BLOCKED` |
| `P14-T031` | `OfficialReplayIT#replaysIdenticalNormalManifestToRequiredFingerprints()` | Strong-envelope exact equality | WP14-5 · 14B-replay · `E-P14-OFFICIAL-RUN` | `R-BLOCKED` |
| `P14-T032` | `ArtifactMigrationDigestIT#copiesReadBackVerifiesThenCasSwitches()` | Copy/read-back digest 전 pointer 전환 0 | WP14-6 · 14B-migration · `E-P14-CUTOVER` | `P-BLOCKED` |
| `P14-T033` | `AwsShadowParityIT#preservesLogicalArtifactsResultAndTermination()` | AWS selected 때 local/AWS semantic mismatch 0 | WP14-6 · 14B-shadow · provider evidence | `P-BLOCKED` |
| `P14-T034` | `AwsCanaryCutoverIT#neverExceedsAuthorityAndCanRollback()` | AWS selected/scoped authority에서 breach 0 | WP14-8 · 14B-canary/rollback · `E-P14-CUTOVER` | `P-BLOCKED` |
| `P14-T035` | `ProductionActivationIT#activatesExactApprovedRevisionAndWritesAudit()` | Exact pointer/audit/health | WP14-8/9 · 14B-activation · `E-P14-CUTOVER` | `P-BLOCKED` |
| `P14-T036` | `RollbackRehearsalIT#reconcilesInflightStateWithoutDeletingImmutableArtifacts()` | Unresolved in-flight/delete 0 | WP14-6/8 · rollback checklist · `E-P14-ROLLBACK` | `P-BLOCKED` |
| `P14-T037` | `Phase14DependencyRulesTest#keepsCloudCryptoCutoverAndVendorTypesOutOfStableModules()` | Stable module forbidden reference 0 | WP14-0/4/7 · entry/14B architecture · architecture evidence | `R-BLOCKED` |

Correction 01이 추가한 contract 분리에는 다음 supplementary exact tests가 필요하다.
이들은 37개를 대체하거나 count를 줄이지 않는다.

| ID | Supplementary exact method | Detects | DoD/evidence |
|---|---|---|---|
| `P14-HG-T001` | `CalibrationProducerBoundaryArchitectureTest#runnerCannotWriteIndependentAnalysisIdentity()` | Runner가 미래 independent digest/verdict를 선취 | 14A execution→independent order; `E-P14-ALNS-BENCHMARK` |
| `P14-HG-T002` | `CalibrationPreAnalysisDecisionTest#remainsExperimentRequiredUntilIndependentAnalysisExists()` | 독립 분석 전 directional/accepted decision | 14A neutral decision evidence |
| `P14-HG-T003` | `AlnsBenchmarkAcceptanceReceiptTest#rejectsMissingSubjectIssuerClaimsProvenanceSignatureTrustVerificationRestartOrLastSafePoint()` | Digest-only/권한 없는 receipt | 14A receipt DoD; `E-P14-ALNS-BENCHMARK-ACCEPTANCE` |
| `P14-HG-T004` | `AlnsBenchmarkAcceptanceReceiptTest#rejectsProducerReviewerAcceptorIdentityOverlap()` | Producer self-approval/independence 위반 | 14A receipt DoD; authority verification report |
| `P14-HG-T005` | `Phase13ApplicabilityDependencyTest#closedSkipLoadsNoPhase13SourceDependencyClassOrEvidence()` | Closed `Skip`의 producer ownership 역전 | 14B applicability; architecture evidence |

Implementation 시 machine-readable `phase14-required-tests.psv`는 최소 다음 field를
stable UTF-8/LF/ID 순서로 봉인한다.

```text
contractId|className|methodName|layer|applicability|ownerModule
sourceRequirement|wp|dodItem|evidenceKey
```

Pre-review manifest에는 PSV bytes SHA-256, expected contract-ID set digest, canonical
source blob/SHA-256, accepted module/profile/POM digests, command digest와 각 fresh XML
digest를 넣는다. Checker는 다음을 모두 exact equality로 검증한다.

```text
expected IDs == discovered IDs == executed IDs == passed IDs
missing == extra == duplicate == failed == error == skipped == 0
required expected count > 0
canonical base count == 37
supplementary correction count == 5
```

Conditional `P-BLOCKED`는 해당 branch가 닫혀 있을 때 `PASS` 집합에 넣지 않고
`BLOCKED_PENDING_SELECTED_PROVIDER_OR_PRODUCTION_AUTHORITY`로 별도 disposition manifest에
남긴다. Branch가 열리면 accepted applicability record와 함께 expected set에 추가한다.
Coverage disposition을 변경하려면 source requirement, owner approval, replacement
method와 evidence key를 같은 immutable manifest에 기록한다. 단순 rename은 old→new
one-to-one mapping과 동일 fixture/oracle/pass criterion을 요구한다.

#### “미적용”을 PASS처럼 쓰지 않는다

| 상황 | 올바른 기록 | 잘못된 기록 |
|---|---|---|
| Phase 13을 쓰지 않는 ALNS-only production | 유효한 signed `Phase13ApplicabilityReceipt.Skip`을 검증 | Phase13 test를 삭제하고 “N/A” |
| Substituted provider가 선택되지 않음 | Phase 12가 현재 경로에 not applicable인 근거와 selected provider identity 기록 | Phase12를 암묵 skip |
| Production authority가 아직 없음 | Production E2E `GATED`, 실행 0, blocker/owner 기록 | Mock authority test green을 production PASS로 집계 |
| Numeric Q-BENCH-02 값 미승인 | Performance/official-run `OPEN—EXPERIMENT_REQUIRED` | 임의 timeout/seed/run count로 green |
| Q-VAR-01 | `DEFERRED`, baseline과 분리 | 기본 후보 생성기로 몰래 구현 |

### 11.4 현재 실행 가능한 명령과 미래 명령을 구분한다

이 문서 작성 시점의 frozen live inventory에는 executable `./mvnw`와 Phase 00 reactor skeleton이 보이지만, Phase 14 production type/test는 없고 외부 작업은 미커밋·미승인이다. 그러므로 다음 명령은 의미가 다르다.

**현재 inventory에서 읽기 전용으로 가능한 확인**

```bash
./mvnw -q -N help:evaluate -Dexpression=project.artifactId -DforceStdout
find rpdptw build legacy -type f -name '*.java' -print | sort
find adapters apps distributions deployment -type f -print 2>/dev/null | sort
rg -n 'CalibrationPlan|OfficialExecutionManifest|CutoverState|ProductionAuthority|Phase14' \
  rpdptw build adapters apps distributions deployment 2>/dev/null
```

첫 명령도 plugin resolution/network가 필요할 수 있다. 이 guide 작성에서는 실행하지 않았다. 성공해도 Phase 14 gate를 통과한 것이 아니라 reactor identity를 읽었을 뿐이다.

**FUTURE—해당 module과 test가 실제로 생긴 뒤에만 유효**

현재 POM에는 Failsafe plugin, Phase 14 IT execution/profile과 아래 distribution/module이
없다. 따라서 이 block은 accepted Phase 00 reactor와 Phase 14 module owner가 wiring,
plugin/version/profile을 승인한 뒤 사용하는 **fail-closed baseline**이다.

```bash
# 0. Accepted reactor 전체의 stale target/report를 제거하고 clean baseline을 검증한다.
./mvnw -B -ntp clean install

# 1. Unit/contract는 Surefire selector와 module-local POM을 사용한다.
./mvnw -B -ntp -f rpdptw/application/pom.xml \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest='IntegerTravelFixtureAuthorityTest,GreatCirclePolicyAuthorityTest,OfficialParameterSeparationTest,CalibrationPlanFreezeTest,CalibrationAcceptanceEvaluatorTest,OfficialManifestFactoryTest,Phase13ApplicabilityTest,OfficialBenchmarkComparatorTest,OfficialRunCompletenessVerifierTest,CutoverStateMachineTest,CutoverObservationWindowTest,ProductionAuthorityVerifierTest,RollbackCoordinatorTest,CalibrationPreAnalysisDecisionTest,AlnsBenchmarkAcceptanceReceiptTest' \
  clean test

./mvnw -B -ntp -f build/port-contract-tests/pom.xml \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest='SignedEvidenceVerifierContractTest,PostReviewAcceptanceAuthorityVerifierContractTest,CutoverControlPortContractTest' \
  clean test

./mvnw -B -ntp -f build/architecture-rules/pom.xml \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest='Phase13ApplicabilityDependencyTest,CalibrationProducerBoundaryArchitectureTest,Phase14DependencyRulesTest' \
  clean test

# 2. *IT는 Failsafe selector, accepted profile, clean verify를 사용한다.
./mvnw -B -ntp -f distributions/local/pom.xml \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dit.test='CalibrationDeclaredRunCompletenessIT,CalibrationIndependentAnalysisIT,OfficialRunCompletenessIT,OfficialReplayIT' \
  -Pphase14-local-it \
  clean verify

# 3. AWS_REFERENCE가 실제 selected/accepted provider일 때만 실행한다.
./mvnw -B -ntp -f distributions/aws-serverless/pom.xml \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dit.test='ArtifactMigrationDigestIT,AwsShadowParityIT,AwsCanaryCutoverIT,ProductionActivationIT,RollbackRehearsalIT' \
  -Pphase14-aws-it \
  clean verify
```

- `build/port-contract-tests`, `distributions/local`, `distributions/aws-serverless`는 현재 frozen inventory에 **ABSENT**다.
- 현재 root는 Surefire `failIfNoTests=false`이고 Failsafe binding/profile은 0개다. 이
  상태의 `verify` exit `0`은 Phase 14 IT 실행 증거가 아니다.
- `*IT`에 `-Dtest`를 쓰지 않는다. Unit/contract는 `-Dtest`, Failsafe integration은
  `-Dit.test`와 `-Dfailsafe.failIfNoSpecifiedTests=true`를 사용한다.
- Targeted acceptance selector에 무조건 `-am`을 붙이면 upstream module에도 같은 selector가
  적용돼 “upstream에 지정 class 없음”과 “target 0 tests”를 혼동할 수 있다. 먼저 full
  reactor `clean install`을 통과시키고, strict selector는 module-local `-f`에서 실행한다.
  다른 accepted reactor layout이 `-am`을 요구하면 upstream selector miss를 명시적으로
  분리하는 module/profile config와 target exact-set checker를 같은 review에서 승인한다.
- AWS 명령은 AWS가 selected/accepted provider일 때만 유효하다. Substituted provider면 Phase 12가 승인한 provider-specific module/suite로 바꾼다.
- `-DskipTests`, 특정 verifier 제외, 실패 module 제외, test report 삭제 후 재실행은 acceptance 명령이 아니다.
- 각 command 시작 시 command ID, 시작 시각, POM/profile/source/toolchain digest와 expected
  method manifest digest를 봉인한다. `clean` 뒤 target module의 해당 report directory가
  비었음을 확인하고, command 종료 직후 그 실행이 새로 만든 XML만 수집한다.
- Fresh XML checker는 §11.3의 expected/discovered/executed/passed exact equality,
  canonical 37와 supplementary 5 coverage, required count `> 0`, failure/error/skipped/
  missing/extra/duplicate `= 0`을 검사한다. XML 없음, command 시작 전 timestamp, 다른
  module/profile/run의 XML 또는 manifest/POM digest mismatch는 Maven exit `0`이어도 fail이다.
- PASS는 process exit code 0만이 아니라 fresh Surefire/Failsafe reconciliation,
  expected worker/verifier count, failure-code assertion, evidence
  digest/signature/closure, manifest identity와 provider read-back까지 모두 만족한 상태다.
- Actual production canary/activation은 Maven test 명령이 아니다. 사람의 action-specific authority를 받는 별도 release 절차다.

## 12. 사람 checkpoint, evidence, 중단과 재개

### 12.1 Checkpoint 표

| Checkpoint | 사람이 보는 것 | 승인해야 하는 사람/역할 | 승인 후 허용되는 다음 행동 | STOP 조건 |
|---|---|---|---|---|
| C14-0 Entry | Phase 07/08 accepted refs, freeze refs, Q status, source fingerprints | Tech lead, benchmark owner | Calibration protocol 구현 | upstream receipt 없음, source conflict |
| C14-1 Protocol | Fixture/protocol hash, D/U/speed authority, comparator, worker accounting | Benchmark owner, domain reviewer | Calibration execution | hidden default, scalar objective |
| C14-2 Outcome | 모든 available pair outcome, abnormal/timeout, both verifier, resource data | Benchmark owner | Independent pre-review | 일부 실행/선별 제출 |
| C14-3 14A acceptance | Manifest M, review R, receipt A(M,R), independent signature | Independent calibration reviewer | 14B preparation; optional Phase13 gating input | review가 manifest/outcome에 역참조, Q-BENCH-02를 해결했다고 과장 |
| C14-4 14B readiness | Accepted 14A, Phase11/provider path, official ALNS/Q-BENCH-02 values, trust, Phase13 receipt | Release, Security, Platform, benchmark owner | Official manifest/run | 임의 numeric value, unsigned skip |
| C14-5 Official card | Exact manifest, all-worker/two-verifier closure, replay, card | Benchmark owner, independent reviewer | Provider shadow | 다른 manifest 비교, incomplete run |
| C14-6 Provider readiness | Deploy/migration/shadow/security/ops/cost/rollback evidence | Platform, Security, Ops, FinOps | Cutover preflight | actual provider evidence 없음 |
| C14-7 Canary authority | Exact from/to pointer, scope, traffic, expiry, stop/rollback limits | Product/Release/Security/Ops 권한자 | Canary 한 번 | 권한 만료·취소·scope 불일치 |
| C14-8 Activation authority | Canary observation/hold, current pointer/state, fresh authority | 같은 권한 체계의 별도 activation approvers | Full activation 한 번 | canary authority 재사용 |
| C14-9 Closure | Active 또는 rollback/reject terminal record, evidence index, runbook/on-call | Independent Phase14 reviewer, scheduler owner | Authoritative status/handoff | dangling refs, unresolved in-flight |

### 12.2 Evidence bundle의 최소 구성

**14A bundle**

- `CalibrationPlan`/manifest reference와 canonical digest
- fixture/protocol/source freeze refs
- availability matrix와 authority-backed `UNAVAILABLE` 사유
- 모든 available pair의 immutable execution outcome
- Runner-owned complete execution outcome와 방향 없는
  `PreAnalysisCalibrationRecord.EXPERIMENT_REQUIRED`
- 별도 owner가 exact execution outcome에서 만든 `IndependentCalibrationAnalysis`와
  producer/independent equality report
- worker completeness, abnormal/timeout/resource accounting
- two-verifier results와 exact comparator trace
- `phase14-required-tests.psv`, canonical source/PSV/POM/profile/command/fresh XML digest,
  expected/discovered/executed/passed exact-set report
- immutable pre-review manifest M과 independent review R
- Acceptance subject/issuer/claims/provenance/signature envelope, approved trust-root/
  revocation/freshness/clock context와 action-time verification receipt
- Explicit `ACCEPTED` verdict, restart conditions, handoff artifact와 last-safe point를 가진
  `AlnsBenchmarkAcceptanceReceipt(M, R, authorityVerification, ...)`

**14B readiness/official bundle**

- accepted 14A receipt ref
- Phase 11 acceptance 및 selected provider identity
- applicable Phase 12 receipt 또는 명시적 not-applicable reason
- official ALNS parameter-set ref
- 별도 승인된 `Q-BENCH-02` numeric execution-values ref
- Phase13 signed `Skip` 또는 accepted `Activated`
- signing keys/trust snapshot/revocation evidence
- exact official manifest와 all-worker/two-verifier receipts
- benchmark card와 identical-manifest replay
- Canonical 37-method 중 14B expected set과 supplementary coverage의 fresh XML exact-set
  reconciliation; 닫힌 provider branch는 PASS가 아니라 별도 blocked disposition

**Cutover/closure bundle**

- build/runtime/deployment revision identity
- artifact migration digest와 provider read-back
- shadow/security/ops/cost/rollback-rehearsal reports
- pending transition intent, provider CAS/read-back/reconciliation, final state result
- canary authority와 activation authority를 **서로 다른 ref**로 보존
- durable observation-window/clock/telemetry evidence
- `ProductionActivationRecord` 또는 별도 `RollbackRecord`/`CutoverRejectedRecord`
- `ImplementationEvidenceIndex`, runbook, on-call/owner, open/gated/deferred 목록

Evidence DAG의 허용 방향은 다음과 같다.

```text
source freeze + protocol + fixture
          │
          ▼
 calibration manifest M ──► execution outcomes
                                  │
                                  ▼
                     neutral EXPERIMENT_REQUIRED
                                  │
                                  ▼
                      independent analysis IA
                                  │
          └──────────► pre-review manifest M
                                  │
                         independent review R
                                  │
                  verified acceptance authority V
                                  │
                       acceptance A(M, R, V)
                                  │
        Phase11/provider + official values + Phase13 applicability
                                  │
                                  ▼
                    official manifest OM
                                  │
                   run/replay/card receipts
                                  │
                    deployment/shadow evidence
                                  │
             canary authority ─► canary result
                                  │
          activation authority ─► activation record
```

Execution outcome은 미래 IA/R/A digest를 포함하지 않고, `M`이나 IA가 미래의 `R`/`A`를
참조하도록 backfill하지 않는다. `OM`도 mutable `used=true` 상태를 갖지 않는다. 실행과
replay는 별도 append-only receipt다.

### 12.3 Stop note와 resume protocol

안전한 중단은 실패가 아니다. 권한·관찰·evidence가 불충분하면 마지막 안전 지점에서 멈추는 것이 올바른 구현이다.

**PROPOSED stop-note 필드**

```text
phase/substage, status(OPEN|GATED|DEFERRED|FAILED|HOLD),
reasonCode, blockedAction, lastSafeState, lastSafePointer,
controlStateVersion, providerPointerVersion,
manifestRef, evidenceRefs, authorityRef/expiry(if non-secret),
pendingIntentRef, inFlightOperationRefs,
owner, nextCheckpoint, capturedAt/by, snapshotPolicy
```

중단할 때:

1. 새 traffic/action을 시작하지 않는다.
2. 이미 provider call을 보냈다면 응답 부재를 실패로 추측하지 말고 provider read-back으로 reconcile한다.
3. immutable intent/result/observation과 정확한 last-safe pointer를 기록한다.
4. secret, raw credential, PII는 기록하지 않는다.
5. scheduler 상태와 사람용 note가 다르면 scheduler의 authoritative status를 임의로 바꾸지 않고 차이를 blocker로 기록한다.

재개할 때:

1. source/manifest/evidence digest와 current provider pointer를 다시 읽는다.
2. control/provider 두 version을 모두 다시 검증한다.
3. authority expiry, revocation, scope, nonce/replay를 다시 검증한다.
4. telemetry gap과 승인 clock continuity를 확인한다.
5. previously pending/in-flight operation을 먼저 reconcile한다.
6. 전제 하나라도 달라졌으면 이전 승인을 재사용하지 않고 해당 checkpoint로 돌아간다.

## 13. Anti-pattern과 즉시 고치는 방향

| Anti-pattern | 왜 위험한가 | 올바른 방향 |
|---|---|---|
| “Phase 14 test green이므로 production-ready” | 14A receipt, 14B authority, actual provider evidence를 혼합한다. | Substage별 gate와 사람 승인을 별도 evidence로 판정 |
| Official 값에 constructor default 사용 | 승인되지 않은 정책을 코드가 만든다. | 값 없음은 `OPEN—EXPERIMENT_REQUIRED`/`GATED` |
| Calibration parameter를 official run에 재사용 | 두 권한 경계를 무너뜨린다. | 별도 `OfficialAlnsParameterSet` |
| Manifest에 mutable run status 저장 | 동일 identity의 의미가 시간에 따라 바뀐다. | Immutable manifest + append-only execution receipt |
| 4×2 중 성공한 조합만 보고 | candidate space와 failure accounting을 왜곡한다. | 전수 availability와 all-available execution |
| Verifier 하나만 사용 | 독립 검증 계약을 깨뜨린다. | 두 verifier 모두, disagreement는 fail |
| Weighted-sum/scalar objective | 공식 lexicographic 의미를 바꾼다. | 정확한 4-tuple comparator |
| D/U/speed fallback을 같은 “default”로 처리 | 제공값 우선권과 계산 규칙이 다르다. | 필드별 authority/provenance와 rounding |
| Phase13 directory가 없으면 ALNS-only | signed applicability decision이 아니다. | 유효한 `Skip` receipt |
| Phase13 failed hybrid를 ALNS-only로 자동 전환 | optional activation 실패를 우회한다. | explicit reject/rollback 후 새 authority |
| Emulator 결과를 provider evidence로 사용 | 실제 IAM/network/revision/control plane을 증명하지 못한다. | actual selected-provider shadow/canary evidence |
| Control state를 먼저 `ACTIVE`로 씀 | pointer failure 때 false terminal이 된다. | intent→provider CAS/read-back→final CAS |
| Provider timeout이면 자동 rollback 후 성공 기록 | provider가 실제로 바뀌었을 수 있다. | read-back/reconciliation부터 수행 |
| Canary approval로 full activation | action/scope가 다르다. | 별도 activation authority |
| Missing telemetry를 zero-error로 처리 | 관찰 불가능을 건강으로 오판한다. | fail-closed hold/rollback |
| Rollback 성공을 Phase14B accepted로 기록 | 안전 복귀와 production acceptance는 다르다. | 별도 rollback terminal record |
| Artifact/evidence 삭제로 rollback | 감사·재현성을 잃는다. | pointer 전환, immutable retention |
| Prefix/listing으로 “latest” 찾기 | exact identity와 completeness를 보장하지 못한다. | manifest/index의 exact refs |
| Empty activation record | 무엇이 active인지 알 수 없다. | manifest/build/deploy/pointer/authority/evidence refs 필수 |
| Historical 문서의 stale count 복사 | current authority와 충돌한다. | authoritative source + fingerprint |

## 14. 실제 구현 Exit checklist와 Definition of Done

### 14.1 Entry/scope checklist

- [ ] 15개 canonical Phase 중 마지막 Phase 14를 구현하며 새 Phase를 만들지 않았다.
- [ ] 14A와 14B를 별도 status, receipt, authority로 모델링했다.
- [ ] Phase 07/08 accepted refs와 source freeze가 exact ref/digest로 고정됐다.
- [ ] `Q-BENCH-02=OPEN—EXPERIMENT_REQUIRED`, `Q-VAR-01=DEFERRED`를 보존했다.
- [ ] Phase 11/provider 경로와 Phase 12 applicability를 구분했다.
- [ ] Phase 13 signed `Skip` 또는 accepted `Activated` 없이 14B로 가지 않는다.
- [ ] Historical 문서를 권위 입력으로 사용하지 않았다.
- [ ] Secret/PII나 production credential을 artifact/test/log에 넣지 않았다.

### 14.2 Phase 14A DoD

- [ ] Protocol/fixture/manifest가 execution 전에 immutable identity로 고정됐다.
- [ ] 4 algorithm × 2 parallelism mode의 availability가 모두 설명됐다.
- [ ] 모든 available pair가 실행됐고 0 available은 incomplete로 판정됐다.
- [ ] D/U/speed authority와 provenance가 exact rule대로 검증됐다.
- [ ] 모든 declared worker가 terminal receipt를 냈고 abnormal/timeout/resource가 집계됐다.
- [ ] 두 verifier가 모두 PASS했고 exact four-tuple comparator를 사용했다.
- [ ] Runner-owned complete outcome에는 raw/producer evidence만 있고 independent
      analysis digest/verdict가 없으며, 독립 분석 전 상태는 neutral
      `EXPERIMENT_REQUIRED`다.
- [ ] Independent analyzer가 exact complete outcome을 별도 provenance로 재계산했고
      producer/independent equality 또는 typed mismatch를 봉인했다.
- [ ] Independent pre-review가 실행/분석 이후 작성됐다.
- [ ] Acceptance receipt가 exact `(manifest M, review R, authority verification)`을
      참조하고 reverse/backfill edge가 없다.
- [ ] Receipt의 subject/issuer/claims/provenance/signature/trust-root verification,
      explicit `ACCEPTED`, timestamp, restart conditions, handoff와 last-safe point가
      content-addressed closure에 있다.
- [ ] `P14-T001`~`P14-T009`, `P14-T017`~`T018`, `P14-T025`,
      `P14-T028`~`T029`와 `P14-HG-T001`~`T004`의 applicable expected set이 fresh
      Surefire/Failsafe XML과 exact 일치하고 누락/skip 0이다.
- [ ] Receipt는 ALNS-only qualification임을 명시하고 production/MIP/provider 권한으로 과장하지 않는다.

### 14.3 Phase 14B DoD

- [ ] Accepted 14A receipt가 exact ref로 검증됐다.
- [ ] Official ALNS parameter values와 Q-BENCH-02 numeric/provider execution values가 별도 승인 artifact로 존재한다.
- [ ] Phase 11과 selected provider acceptance가 존재하고, applicable Phase 12가 닫혔다.
- [ ] Phase13 applicability가 signed `Skip` 또는 accepted `Activated`다.
- [ ] Closed `Skip`은 scheduler/control-plane producer이며 Phase 13
      source/dependency/class-load/`E-P13-*`/self-signed artifact가 0이다.
- [ ] Gate-open Phase 13은 accepted 14A + 전체 `C-17` + accepted evidence 뒤
      unsigned `Phase13ActivatedHandoff`만 생산했고, signed `Activated` wrapper는
      Phase 14B/control plane이 official-hybrid consumption 시점에 별도 조립·검증했다.
- [ ] Signing/trust/revocation 검증과 official-run environment authorization이 통과했다.
- [ ] Exact official manifest에서 all-declared-worker, both-verifier, exact comparator가 닫혔다.
- [ ] Identical-manifest replay와 exact benchmark card가 통과했다.
- [ ] Actual provider deploy/migration/shadow/security/ops/cost evidence가 존재한다.
- [ ] Rollback target과 rehearsal이 검증됐다.
- [ ] Pure state machine, pending intent, provider CAS/read-back/reconciliation, final control CAS 순서를 지켰다.
- [ ] Canary와 activation은 서로 다른 action-specific authority를 사용했다.
- [ ] 승인 clock/absolute deadline/telemetry window가 durable하고 gap/anomaly는 fail-closed다.
- [ ] 성공 경로는 exact provider pointer와 control state `ACTIVE`가 일치한다.
- [ ] `ProductionActivationRecord`와 `ImplementationEvidenceIndex`가 non-empty, closure-complete, independently reviewed다.
- [ ] Canonical 37-method manifest의 14B applicable set과 supplementary
      `P14-HG-T005`가 accepted profile의 fresh XML과 exact 일치하며 Failsafe IT
      required count가 0보다 크다.

### 14.4 Rollback/reject checklist

- [ ] Previous accepted pointer/revision으로 실제 read-back이 확인됐다.
- [ ] Pending/in-flight operation이 0이거나 모두 reconciliation됐다.
- [ ] Failed/candidate artifact와 evidence를 삭제·변조하지 않았다.
- [ ] `ROLLED_BACK` 또는 `REJECTED` record가 원인, authority, pointer, evidence를 보존한다.
- [ ] 이 terminal을 Phase 14B `ACCEPTED`로 집계하지 않았다.
- [ ] 재시도에는 fresh manifest/authority/checkpoint가 필요한지 명시했다.

**최종 DoD:** Phase 14A는 독립적으로 검토된 ALNS benchmark acceptance receipt를 만들고, Phase 14B는 그 receipt와 별도의 official 값·provider·Phase13 applicability·production authority를 검증한 뒤 actual provider에서 관찰 가능한 안전한 cutover를 완료하여 exact active identity를 가진 activation record를 낸 경우에만 완료다. 안전한 rollback/reject는 올바른 terminal 결과지만 Phase 14B 완료가 아니다.

## 15. 최종 production handoff와 broken 증상

### 15.1 Handoff record가 반드시 답해야 하는 질문

`ImplementationEvidenceIndex`의 제안 필드:

- source freeze/commit 및 authority document fingerprints
- 14A manifest/review/acceptance refs
- official parameter/execution-values/Phase13 applicability refs
- official manifest/run/replay/card refs
- build/runtime/deployment/provider identities
- deploy/migration/shadow/security/ops/cost/rollback refs
- cutover intent/result/observation/authority refs
- activation 또는 rollback/reject record ref
- open/gated/deferred items, owner, next review

`ProductionActivationRecord`의 제안 필드:

- exact official manifest digest
- selected result/solution and verifier receipt refs
- build artifact/runtime/deployment revision
- provider account/region/resource identity와 active pointer
- final control/provider versions
- canary/activation authority refs와 승인자 역할
- observation window/clock/telemetry refs
- rollback target/runbook/on-call
- activated-at/by와 independent review ref

Operations가 받는 것은 “배포됨”이라는 문장이 아니라 active identity를 exact lookup할 수 있는 index, alerts/stop conditions, rollback runbook, owner/on-call, authority expiry와 limitation이다. Operations는 benchmark 정책을 재결정하거나 manifest를 수정하거나 listing으로 latest를 추측하지 않는다.

### 15.2 Broken 증상과 첫 안전 행동

| 증상 | 뜻할 수 있는 것 | 첫 안전 행동 |
|---|---|---|
| Activation record는 있는데 provider pointer가 다름 | False terminal 또는 out-of-band change | New starts hold, provider read-back, versions/intent reconcile, incident |
| `ACTIVE`인데 pending intent가 남음 | Finalization/closure 불완전 | Traffic 확대 금지, in-flight reconcile |
| Canary 후 telemetry가 비어 있음 | 관찰 실패이지 zero error가 아님 | Hold/rollback policy 실행 |
| Official card의 manifest digest가 run receipt와 다름 | 다른 조건 비교/오염 | Publication/cutover 중단, exact manifest audit |
| Declared worker 수와 terminal receipt 수가 다름 | Incomplete official run | Result 무효, missing worker 원인 기록 |
| 두 verifier disagreement | 후보/결과 의미 불일치 | Fail closed, 선택/배포 금지 |
| `UNAVAILABLE`인데 authority evidence가 없음 | Silent capability suppression | Calibration/official run incomplete |
| Phase13 receipt가 없는데 hybrid artifact가 active | Optional gate 우회 | Activation reject/rollback, scope audit |
| Numeric value source가 코드 default뿐 | Q-BENCH-02 authority 부재 | `OPEN—EXPERIMENT_REQUIRED`, official run 중단 |
| Rollback 후 candidate artifact가 사라짐 | Evidence immutability 위반 | Incident/retention audit; 복구 전 재승인 금지 |
| 서명은 맞지만 authority가 만료/취소됨 | Action authorization 없음 | Action 금지, fresh authority 요청 |
| 재실행 결과가 다르고 nondeterminism envelope가 없음 | Reproducibility failure | Baseline/publication 보류, source/runtime/seed audit |

## 16. Source → requirement → WP → test/evidence 추적성

| Source section | Requirement | WP | Test/evidence |
|---|---|---|---|
| Master §8.5, Current Domain §8, Current Architecture §4~7 | D/U 제공값 우선, missing D Great Circle meter `HALF_UP`, missing U per-vehicle `CEILING`, missing speed만 45 km/h | WP14-1, WP14-2 | `P14-T001`~`T005`, provenance receipt |
| Master §10.3/§14, `Q-BENCH-03` | Exact `(unassigned, vehicles, distance, operational seconds)` lexicographic comparator | WP14-1, WP14-5 | `OfficialComparatorTest`, comparator trace |
| Master §11/§14, `Q-BENCH-01` | 4×2는 candidate maximum; availability 전수, all available 실행, all declared workers | WP14-2, WP14-5 | availability matrix, `OfficialRunCompletenessIT` |
| Master §14, Phase14 §5~6 | 14A는 ALNS-only independent acceptance; Phase09~13/production authority 불필요 | WP14-0~3 | `P14-T007`~`T009`, `T028`~`T029`, independent review |
| Phase14 §6.3~6.4A, human review `HG14-R002` | Runner complete는 raw/producer까지만 소유; neutral `EXPERIMENT_REQUIRED` 뒤 별도 independent analysis | WP14-2, WP14-3 | `P14-HG-T001`~`T002`, execution/analysis digests |
| Plan §9.1~9.3, Phase14 §6.4A, human review `HG14-R003` | M→R→verified acceptance authority→receipt; subject/issuer/claims/provenance/signature/trust-root/restart/last-safe 필수 | WP14-3, WP14-9 | `P14-HG-T003`~`T004`, authority verification/receipt |
| Plan §8~9, Phase14 §4/§11~13 | Pre-review manifest → review → acceptance, no reverse edge; exact test manifest와 fresh XML closure | WP14-3, WP14-9 | `P14-T001`~`T037`, `P14-HG-T001`~`T005`, PSV/XML report |
| Master §12.4, `Q-BENCH-02` | Official numeric execution values는 experiment+사람 승인 전 open | WP14-4 | values approval artifact 또는 `OPEN—EXPERIMENT_REQUIRED` gate |
| Master §16.1/§16.2, Phase13 §6.5/§14, human review `HG14-R006` | Scheduler-owned closed `Skip`, gate-open Phase 13 handoff와 Phase 14B signed `Activated` consumer wrapper의 owner/시점 분리 | WP14-4 | `P14-T012`~`T015`, `P14-HG-T005`, signed receipt |
| Master §16.2, Phase11/12 | Actual selected provider deployment acceptance와 substituted-provider 조건 | WP14-4, WP14-6 | Phase11/12 receipts, deploy/shadow evidence |
| Phase14 §7, review F-P14-003/004 | Immutable official manifest, separate executions, all-worker/two-verifier/replay | WP14-5 | manifest/run receipts, `OfficialReplayIT` |
| Phase14 §8.2~8.4, review F-P14-001 | Pending intent → provider CAS/read-back → reconcile → final control CAS; two versions | WP14-7, WP14-8 | state/port/fault tests, transition receipts |
| Phase14 §8.5, review F-P14-007 | Durable observation, approved clock/deadline, missing telemetry fail closed | WP14-7, WP14-8 | `CutoverObservationWindowTest`, telemetry/clock evidence |
| Phase14 §9, review F-P14-005 | Canary와 full activation의 authority 분리, expiry/revocation/scope/replay 검증 | WP14-7, WP14-8 | authority tests, distinct authority refs |
| Master §16.2, Phase14 §10~11 | Rollback은 pointer-based, artifact immutable; safe terminal ≠ acceptance | WP14-6, WP14-8, WP14-9 | rehearsal, rollback/reject record |
| Plan §9~12, Phase14 §13/§16 | Exact evidence index와 non-empty activation handoff | WP14-9 | index/activation tests, independent review |
| Phase14 §11~12, human review `HG14-R004/R005`, Current Architecture §18~19 | Canonical 37 methods coverage disposition, Failsafe IT `>0`, full-reactor clean과 fresh XML exact reconciliation | 모든 WP exit | PSV/POM/profile/command/XML digests, missing/extra/skip 0 |
| Open Questions summary | `Q-VAR-01=DEFERRED` 보존 | 모든 WP scope | Deferred marker, no hidden generator/default |

## 17. 문서 자체 검증과 현재 남은 gate

### 17.1 이 guide에 적용할 정적 검증

Repository root에서 다음을 실행한다.

```bash
target='docs/implementation/human-guides/phases/phase-14-human-implementation-guide.md'

# 상대 Markdown link의 파일 및 GFM anchor를 검사한다.
# 구현 시 repository의 link checker가 생기면 그 accepted checker를 우선한다.

# trailing whitespace와 conflict marker
rg -n '[[:blank:]]+$|^(<<<<<<<|=======|>>>>>>>)' "$target"

# target scope와 whitespace error
git status --short -- "$target"
git diff --check -- "$target"
git diff --no-index --check /dev/null "$target"
```

판정 방법:

- Link target/anchor missing 0건이어야 한다.
- Trailing whitespace/conflict marker 0건이어야 한다.
- 대상 status는 새 파일이면 `??`, 이미 추적 중이면 의도한 `M` 하나만 허용한다.
- `git diff --check -- "$target"`와 untracked-aware `git diff --no-index --check`의 whitespace error 출력이 없어야 한다. `--no-index`는 새 파일의 내용 차이 때문에 exit 1일 수 있으므로 **출력된 whitespace error의 유무**를 판정한다.
- 다른 파일의 status는 이 guide의 scope가 아니며 수정·정리·stage하지 않는다.

### 17.2 작성 시점 남은 gate

이 guide는 구현 명세와 학습 경로이지 acceptance evidence가 아니다. 현재 확정적으로 남은 것은 다음과 같다.

- Phase 00 reactor/live inventory는 외부 작업의 미커밋·미승인 snapshot이며 authoritative implementation baseline이 아니다.
- Phase 14 application/domain/adapters/distributions/deployment type와 test는 현재 frozen inventory에 없다.
- Phase 07/08/11의 실제 accepted receipts와 evidence graph를 구현 단계에서 확인해야 한다.
- `Q-BENCH-02`는 `OPEN—EXPERIMENT_REQUIRED`; official numeric/provider values를 만들지 않았다.
- `Q-VAR-01`은 `DEFERRED`; 구현 scope로 끌어오지 않았다.
- Phase13 applicability decision과 official hybrid activation evidence가 없다.
- Actual selected-provider deploy/shadow/security/ops/cost/rollback evidence가 없다.
- Production canary/activation authority가 없고 어떤 production action도 승인되지 않았다.
- Canonical Phase 14 review verdict는 `CHANGES_REQUIRED`, acceptance는 `BLOCKED_NOT_READY`다.
- Human-guide review `HG14-R001`~`R006`은 correction 01에 반영했지만 새 독립 re-review와
  scheduler acceptance를 아직 받지 않았다.
- Implementation README/다른 guide의 날짜형 source index와 이 guide가 교정한 current
  canonical Domain/Architecture index의 repository-level 정렬은 별도 owner 과제다.

따라서 현재 가능한 마지막 안전 지점은 이 guide와 권위 source/frozen inventory를 읽고 구현 계획을 검토하는 데까지다. Official receipt, production readiness, cutover acceptance를 선언하면 안 된다.
