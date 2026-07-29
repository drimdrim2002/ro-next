# Phase 13 사람용 구현 가이드 — Optional hybrid route selection

```yaml
guide_status: CORRECTED_ROUND_01_AWAITING_INDEPENDENT_RECHECK
guide_scope: Phase 13 only
correction_round: "01"
correction_status: HG13_R001_TO_R005_ADDRESSED
canonical_phase_count: 15
phase: "13"
phase_name: optional-hybrid-route-selection
canonical_phase_document: docs/implementation/phases/phase-13-optional-hybrid-route-selection.md
canonical_phase_review: docs/implementation/reviews/phase-13-review.md
canonical_phase_document_version: "1.5"
canonical_phase_document_status: REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: PASS_WITH_RESIDUAL_BLOCKERS
implementation_status_observed: GATED_NOT_STARTED
phase_acceptance_status_observed: C17_GATE_CLOSED_NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
alns_benchmark_acceptance_status_observed: NOT_PRODUCED
production_activation_status_observed: NOT_AUTHORIZED
entry_gate_status: CLOSED_MULTIPART_AUTHORITY_AND_EVIDENCE_GATE
handoff_status_observed: SKIP_CONTRACT_PROPOSED_ACTIVATED_HANDOFF_NOT_READY
signed_applicability_receipt_status_observed: NOT_PRODUCED
signing_trust_policy_status_observed: OPEN_GATED_NOT_APPROVED
implementation_direction: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_snapshot_at: "2026-07-29T02:26:03+0900"
inventory_snapshot_timezone: Asia/Seoul
inventory_snapshot_policy: ONE_TIME_LIVE_SNAPSHOT_UNCOMMITTED_UNAPPROVED
inventory_status_snapshot_blob: 85277a4a7ef615383ac0665dd295f7fed09ad208
inventory_drift_status: CONCURRENT_PHASE00_SCAFFOLD_AND_PROGRESS_UPDATE_OBSERVED
source_fingerprint_scheme: current_file_git_blob_at_correction_snapshot
source_sections_and_fingerprints:
  docs/master-design.md: "§1~4, §10~§17; especially C-17, §11.7~11.10, §15.11, §16.3 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "§3, §10.6~10.9, §12.4~12.8, §15~§18 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "§4~§7, §11, §14, §17~§20, §22 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/architecture-domain-implementation-design.md: "§2~§3, §16~§22, §25~§29 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~§4 and Q-BENCH-02/Q-INFRA-01/Q-VAR-01 | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§0~§7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~§7 Phase 06/07/08/12/13/14, §8~§15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§1~§10 | correction snapshot f875edbdeb79fb18710421b56e126462a4d6d38c"
  docs/implementation/phases/phase-12-provider-substitution.md: "§18.1~§18.4; especially §18.3 | 63b9defb25d7771bce590d593db9485367724918"
  docs/implementation/phases/phase-13-optional-hybrid-route-selection.md: "§1~§14 | cb3cd961c87b034625ad138046b5745822df16bd"
  docs/implementation/reviews/phase-13-review.md: "§1~§8, §10 and F-P13-001~008 | 691620dc29b819543d6f36081c71bcad928b579d"
  docs/implementation/phases/phase-14-official-calibration-cutover.md: "§0.1, §3, §5.3, §6.4A, §13~§16 | c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26"
historical_cross_check_fingerprints:
  docs/2026-07-26-domain-design.md: "historical only | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "historical only | d51339e251dee1e032e711144dc63d6d07d7323b"
expected_reader:
  - Java의 interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW route, capacity, time-window propagation을 구현해 보았다
  - RPDPTW pair identity, immutable route pool과 exact route selection은 처음 접한다
owner_roles:
  implementation: RPDPTW Optional Hybrid owner
  scope_and_authority: Product + Algorithm + Architecture
  upstream_alns: Phase 06 owner
  upstream_verification: Phase 07 owner
  upstream_application: Phase 08 owner
  alns_benchmark_acceptance: Phase 14A Benchmark/Quality + independent reviewer
  conditional_provider_substitution: Phase 12 owner
  backend: Optimizer Backend owner
  license_and_supply_chain: Legal + Supply-chain
  security: Platform Security
  operations: Platform/SRE
  cost_and_capacity: FinOps + Product
  status_authority: total scheduler
  downstream_official: Phase 14B owner
planned_evidence:
  - E-P13-GATE
  - E-P13-POOL
  - E-P13-SELECTION
  - E-P13-HYBRID
  - E-P13-SECURITY-LICENSE-OPS-COST
  - E-P13-SHADOW-ROLLBACK
  - E-P13-HANDOFF
```

> 이 가이드는 Java와 CVRPTW 경험이 있는 신규 구현자가 Phase 13의 의미를 배우고,
> **모든 gate가 실제로 열린 뒤에만** optional hybrid를 구현하도록 돕는 교육형
> 지시서다. 현재 해야 할 구현 행동은 **멈춤**이다. 아래의 package, file, type,
> signature, test 이름은 별도 표시가 없는 한 `PROPOSED INTERNAL` 또는
> `FUTURE RED`이며, 현재 API·승인된 public contract·구현 evidence가 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 13은 ALNS가 이미 발견하고 완전 평가한 경로들을 immutable pool로 모아 exact
route-selection backend가 조합하게 하되, 반환된 ID를 새 해로 재구성·완전 평가한
후 **기존 ALNS incumbent보다 엄격히 좋을 때만** 채택하는 선택적 branch다.

이 한 문장을 네 문장으로 풀면 다음과 같다.

```text
ALNS가 만든 route artifact는 selector의 입력일 수 있다.
selector가 고른 column은 아직 solution이 아니다.
fresh materialization + full evaluation + Phase 07 two-gate가 권위다.
실패·무효·동률·열화이면 accepted ALNS-only incumbent가 그대로 남는다.
```

현재 상태도 네 축으로 분리한다.

```text
상세 문서와 review       = 존재하고 검토됨
Phase 13 구현            = GATED_NOT_STARTED
Phase 13 evidence        = NOT_PRODUCED
official/production 권한 = NOT_AUTHORIZED
```

`C-17` gate가 닫혀 있다는 것은 “hybrid를 구현했지만 boolean을 false로 두었다”는
뜻이 아니다. canonical ALNS-only execution graph에 Phase 13 pool, selection,
backend discovery, native load와 run identity가 **아예 들어오지 않는 absence
semantics**다.

## 2. 큰 그림, 배경과 필요한 이유

### 2.1 ALNS만으로도 먼저 완성해야 하는 이유

ALNS는 하나의 전체 해를 destroy/repair하며 탐색한다. 좋은 전체 해가 채택되지
않더라도 그 안의 개별 route 중에는 다른 후보의 route와 조합하면 더 좋은 전체 해를
만들 수 있는 것이 있다. Optional hybrid는 이 “좋은 부분 경로의 재조합 가능성”을
검증하는 후속 단계다.

하지만 route selection을 ALNS보다 먼저 만들면 다음 질문에 답할 수 없다.

- Pool에 넣은 route가 정말 hard-feasible하고 cache 없이 재계산된 것인가?
- Selector가 실패했을 때 보존해야 할 믿을 수 있는 incumbent가 있는가?
- “더 좋다”를 판단하는 full-solution comparator가 승인됐는가?
- 재구성한 solution을 독립적으로 검증하고 publication을 막을 수 있는가?
- Hybrid가 ALNS-only보다 실제로 가치가 있는지 같은 corpus와 envelope에서 비교했는가?

그래서 순서는 고정된다.

```text
Phase 05 pair insertion/portfolio
→ Phase 06 COW ALNS/replay
→ Phase 07 independent candidate/result verification
→ Phase 08 provider-free local execution
→ Phase 14A ALNS-only benchmark qualification
→ ALNS_BENCHMARK_ACCEPTANCE_RECEIPT
→ [별도 C-17 및 운영 권한] Phase 13 optional hybrid
```

Phase 14A receipt는 Phase 13의 필요조건이지 충분조건이 아니다. Receipt가 있어도
`C-17`, hybrid 의미와 authority, OR-Tools version/config/native/SBOM, security,
operations, cost, capacity, fallback과 rollback 승인이 따로 없으면 시작할 수 없다.

### 2.2 CVRPTW의 “route 조합”보다 어려운 이유

CVRPTW에서는 route column이 customer 집합과 비용을 가진다고 단순화하기 쉽다.
RPDPTW에서는 그 shortcut이 위험하다.

- 한 `Request`는 pickup과 delivery의 원자적 pair다.
- 두 visit은 같은 concrete vehicle에 있고 pickup이 delivery보다 앞서야 한다.
- Route 순서에 따라 load prefix와 time feasibility가 달라진다.
- Delivery-only request는 logical pickup identity는 있지만 physical pickup visit은 없다.
- Vehicle class가 같아 보여도 terminal, work window, travel-time view와 ownership
  objective가 다를 수 있다.
- Customer profile의 objective가 route 단위로 exact additive하지 않을 수 있다.
- Backend의 objective는 full evaluator/comparator와 의미·정밀도·우선순위가 다를 수 있다.

따라서 Phase 13의 route column은 “order ID 집합 + scalar cost”가 아니다.
`problem/travel/profile`, concrete vehicle, terminal policy, ordered visits,
authoritative evaluation과 projection coefficient identity를 분리해 보존해야 한다.

### 2.3 왜 backend를 격리하는가

Gate가 열릴 경우 canonical exact backend 정책은 Google OR-Tools direct Java
CP-SAT다. 이 결정이 고정하는 것은 backend family뿐이다.

| 이미 고정된 것 | 아직 고정되지 않은 것 |
|---|---|
| Boolean route/unassigned variable와 checked integer/fixed-point model | Exact Maven version과 artifact checksum |
| Direct `com.google.ortools.sat` CP-SAT 사용 | Supported OS/architecture/native packaging |
| `MPSolver`를 canonical fallback으로 사용하지 않음 | `num_workers`, seed, time/work/gap 값 |
| Backend는 selected stable ID만 반환 | Pool cap, pruning, cadence, traffic split |
| Generic build는 OR-Tools/native-loader-free | Performance/cost go-live threshold |

Native library와 vendor API가 generic core/solver/application/verification에 새면
ALNS-only build도 backend 설치에 종속되고, gate-closed path가 class loading만으로
side effect를 만들 수 있다. 그래서 optional adapter와 composition root만 OR-Tools를
알아야 한다.

### 2.4 Canonical 15 Phase 안의 위치

| Phase | Producer output 또는 책임 | Phase 13과의 계약 |
|---:|---|---|
| 00 | Reactor, module/package, forbidden dependency rule | Optional adapter 격리와 zero-test 방지 기반 |
| 01 | Canonical input와 numeric/time normalization | Selector가 raw input을 재해석하면 안 됨 |
| 02 | Complete `PreparedTravel`, immutable `ProblemInstance` | 모든 pool artifact의 authority fingerprint |
| 03 | Propagation, evaluation, comparator | Route admission과 materialized solution의 재계산 권위 |
| 04 | `BoundProfile`, projection dependency와 objective 의미 | Exact-projectable 여부와 comparator 순서의 생산자 |
| 05 | Pair insertion, route/bank partition, 초기 portfolio | Pair·concrete-vehicle exactness의 생산자 |
| 06 | COW ALNS, completed trial, incumbent, replay | Pool source와 rollback할 ALNS-only incumbent |
| 07 | Candidate/result verifier와 `PublishableResult` | Phase 13 candidate도 우회할 수 없는 two-gate |
| 08 | Provider-neutral application/local execution | Gate 판정과 local benchmark seam의 선행 계약 |
| 09 | Immutable artifact/CAS storage | Phase 13 artifact를 저장할 때 의미를 보존하는 후속 기반 |
| 10 | Run/round/worker coordinator | Scheduler applicability와 execution identity owner |
| 11 | AWS reference distribution | ALNS-only Phase 14B의 provider predecessor; Phase 13의 보편 predecessor 아님 |
| 12 | Provider substitution | 선택한 substituted runtime에만 조건부 evidence 제공 |
| **13** | **Pool → exact selection → fresh evaluation → strict adoption** | **이 가이드의 유일한 구현 범위** |
| 14A | ALNS-only benchmark qualification과 acceptance receipt | Phase 13보다 먼저 완료되어야 하는 producer |
| 14B | Official manifest, provider cutover, production authority | Skip 또는 accepted activated handoff의 consumer |

핵심 DAG는 다음과 같다.

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A
                                                   │
                                  accepted receipt │
                                                   ▼
                              separate approvals → 13

10 → 11 ─────────────────────────────────────────→ 14B
14A ─────────────────────────────────────────────→ 14B
13 ─ - official hybrid manifest를 선택한 경우만 - → 14B

10 → 12   # 승인된 provider substitution의 독립 branch
```

Phase 12를 모든 Phase 13의 predecessor로 만들지 않는다. 승인된 hybrid scope가
substituted provider/runtime을 실제로 선택한 경우에만 그 선택에 해당하는 accepted
Phase 12 evidence를 추가한다.

### 2.5 Producer와 consumer 계약

| 경계 | Producer가 보장할 것 | Phase 13/consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 03/04 → 13 | Full route/solution evaluation, exact comparator와 profile projection declaration | Route cost를 임의 합산하거나 non-projectable rule을 숨김 |
| Phase 05/06 → 13 | Pair-complete completed route, immutable incumbent/replay identity | Interrupted draft나 stale cache route를 pool에 수집 |
| Phase 07 → 13 | Candidate verifier → finalization/audit → result verifier | Backend feasible flag로 publication 우회 |
| Phase 08 → 13 | Provider-free local execution과 exact plan binding | Environment boolean이나 classpath presence로 activation |
| Phase 14A → 13 | Immutable accepted ALNS-only benchmark receipt | Receipt를 C-17/backend/production 승인으로 확대 해석 |
| Phase 12 → 13 | 선택된 substituted runtime의 bounded infrastructure evidence | C-17, OR-Tools 또는 hybrid authority로 해석 |
| Scheduler → 14B closed | Scheduler-owned signed `Skip`, Phase 13 refs 없음 | Fake `E-P13-*` 또는 accepted Phase claim 생성 |
| Phase 13 accepted producer → 14B open | Signing/trust 없는 accepted handoff | Phase 14B signed/action-time authority를 handoff 내부에 혼합 |

## 3. Source authority, fingerprint와 정확한 읽기 순서

### 3.1 충돌 해소 순서

구현 중 문장이 충돌하면 다음 순서로 판단한다.

```text
현재 사용자 지시
→ Canonical Master
→ 질문 등록부의 exact Q-* 상태
→ Canonical Domain의 현재 의미
→ Canonical Architecture의 현재 module/runtime 배치
→ Integrated design의 canonical 15 Phase
→ Master Realization Plan과 actual Phase/review
→ historical 문서의 회귀 대조
```

[문서 지도](../../../README.md)가 현재 top-level entry로 지정한 non-dated
[Domain](../../../domain-design.md)과
[Architecture](../../../architecture-design.md)를 이 correction의 현재 authority와
fingerprint source로 사용한다.
[2026-07-26 Master](../../../2026-07-26-master-design.md),
[Domain](../../../2026-07-26-domain-design.md),
[Architecture](../../../2026-07-26-architecture-design.md)는 historical semantic
cross-check일 뿐 현재 authority가 아니다. Historical 문서, legacy code, test-only
값과 현재 working tree의 미커밋 scaffold는 gate나 API를 확정하지 않는다.

[Implementation README](../../README.md)와 이 human-guide
[README](../README.md)가 날짜형 Domain/Architecture를 `Final`/최소 authority로
나열하는 표현은 현재 repository top-level 지도와 충돌한다. 이 가이드에서는 사용자
고정 canonical 5문서와 top-level 지도를 우선했다. 두 README의 source index 정렬은
각 owner의 별도 문서 변경이며 이 correction이 대신 수정하지 않는 known
documentation blocker다.

### 3.2 검증 가능한 source fingerprint

아래 git blob은 correction snapshot `2026-07-29T02:26:03+0900`의 file bytes를
`git hash-object`로 계산한 fingerprint다. HEAD tracked 여부, document review,
implementation acceptance와는 서로 다른 축이다. Hash가 같다는 사실은 semantic
acceptance가 아니다. 변경 시에는 named section의 의미 영향을 다시 검토한다.

| Source path | 먼저 읽을 heading/section | Git blob 또는 frozen live blob |
|---|---|---|
| [Canonical Master](../../../master-design.md) | `§1.5`, `§2`, `§3`, `§10`, `§11.7~11.10`, `§15.1`, `§15.11`, `§16.3` | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Canonical Domain](../../../domain-design.md) | `§3`, `§10.6~10.9`, `§12.4~12.8`, `§15~18` | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Canonical Architecture](../../../architecture-design.md) | `§4~7`, `§11`, `§14`, `§17~20`, `§22` | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | `§2~3`, `§16~22`, `§25~29` | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | `§1~4`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | `§0~§7`, 특히 source authority와 Phase 순서 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | `§2~§7`, Phase 13/14, `§8~§15` | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | `§2`, `§5`, `§8~§10` | correction snapshot `f875edbdeb79fb18710421b56e126462a4d6d38c` |
| [Phase 12 original](../../phases/phase-12-provider-substitution.md) | `§18.1~18.4`, 특히 bounded handoff `§18.3` | `63b9defb25d7771bce590d593db9485367724918` |
| [Phase 13 original](../../phases/phase-13-optional-hybrid-route-selection.md) | `§1~§14` 전체 | `cb3cd961c87b034625ad138046b5745822df16bd` |
| [Phase 13 review](../../reviews/phase-13-review.md) | `§1~§8`, `§10`, `F-P13-001~008` | `691620dc29b819543d6f36081c71bcad928b579d` |
| [Phase 14 original](../../phases/phase-14-official-calibration-cutover.md) | `§0.1`, `§3`, `§5.3`, `§6.4A`, `§13~16` | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` |
| [Historical Domain](../../../2026-07-26-domain-design.md) | Current Domain과 semantic regression 대조만 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Historical Architecture](../../../2026-07-26-architecture-design.md) | Current Architecture와 semantic regression 대조만 | `d51339e251dee1e032e711144dc63d6d07d7323b` |

### 3.3 구현 전 정확한 읽기 순서

1. [Master §1.5](../../../master-design.md#15-규범어-결정-상태와-충돌-처리)에서
   `GATED TARGET`, `OPEN — EXPERIMENT_REQUIRED`, `DEFERRED`를 구분한다.
2. [Master §2.4](../../../master-design.md#24-구현-완료의-의미)에서 class 존재가
   완료가 아닌 이유와 optional hybrid 완료 조건을 읽는다.
3. [Canonical Domain §10.6](../../../domain-design.md#106-alns-search-state와-iteration-outcome)과
   [§10.9](../../../domain-design.md#109-route-identity-column과-pool)에서 stable
   search state, evaluated artifact와 projected column을 구분한다.
4. [Master §11.7~11.10](../../../master-design.md#117-immutable-route-pool)과
   [Canonical Domain §12.5~12.8](../../../domain-design.md#125-route-selection-projection과-exact-partition)을
   나란히 읽어 projection, 두 warm start, selection, materialization과 hybrid commit을
   구분한다.
5. [Canonical Architecture §6.7](../../../architecture-design.md#67-route-selection-backend-contract),
   [§11.2](../../../architecture-design.md#112-multi-round-flow)와
   [Integrated §17](../../../architecture-domain-implementation-design.md#17-phase-13--optional-route-pool과-route-selection)에서
   package/DAG/native lifecycle, ALNS step accounting과 retry를 확인한다.
6. [Master Plan Phase 13](../../master-realization-plan.md#phase-13--optional-hybrid)과
   [Phase 13 original](../../phases/phase-13-optional-hybrid-route-selection.md)을
   전체 읽는다.
7. [Phase 13 review](../../reviews/phase-13-review.md)에서 수정된 함정과 residual
   blocker를 읽는다.
8. [Phase 12 §18.3](../../phases/phase-12-provider-substitution.md#183-phase-13으로-넘기는-bounded-evidence)와
   [Phase 14 §0.1](../../phases/phase-14-official-calibration-cutover.md#01-alns-first-14a14b-gate-분리),
   `§5.3`, `§6.4A`를 대조한다.
9. 마지막에 [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry)와
   `§10`에서 scheduler task, live status와 receipt를 확인한다.

읽는 중 proposed type 이름이 서로 다르면 이름을 합성하지 않는다. 고정 의미를
기록하고 module/type naming ADR을 entry gate의 승인 항목으로 올린다.

### 3.4 Entry gate를 확인하는 순서

사람 승인자는 다음 AND 식을 왼쪽부터 확인한다.

```text
Phase06.accepted
AND Phase07.accepted
AND Phase08.accepted
AND Phase14A.ALNS_BENCHMARK_ACCEPTANCE_RECEIPT.valid
AND C17 scope approved
AND hybrid meaning approved
AND route-selection authority approved
AND exact OR-Tools version/checksum/config approved
AND native/platform/license/notice/SBOM approved
AND operations/retry/cancel/rollback approved
AND security/tenant/access/supply-chain approved
AND cost/budget/capacity/admission approved
AND measured ALNS-only baseline approved
AND scheduler task and separated owners assigned
AND (
  baseline runtime selected
  OR applicable accepted Phase12 substitution evidence exists
)
```

한 항목이라도 `missing`, `open`, `stale`, `wrong scope`이면 판정은 `GATED`다.
`Phase14A receipt valid`와 `C17 approved`를 한 칸으로 합치지 않는다.

Entry 검토자는 최소 다음 필드를 실제 immutable reference로 대조한다.

- Phase 06/07/08 evidence manifest, independent review와 acceptance receipt digest
- Phase 14A corpus/protocol/criteria/build/runtime/oracle/verifier/quality/resource/
  variance/replay closure
- C-17 scope와 hybrid 의미/authority의 exact subject·version·validity
- 선택 runtime identity와 Phase 12 evidence 적용 여부
- Backend artifact checksum, native platform matrix, config fingerprint
- License/notice/SBOM/CVE와 redistribution decision
- Security/operations/cost/capacity owner verdict
- Fallback/rollback last-safe identity
- Scheduler task ID와 status change authority

현재 이 순서의 완료 신호는 없다. 따라서 이 가이드의 WP는 모두 `WAITING`이다.

## 4. RPDPTW primer, 용어집, identity, lifecycle과 invariant

### 4.1 CVRPTW customer에서 RPDPTW request로

CVRPTW의 한 customer visit을 RPDPTW의 한 request와 동일시하지 않는다.

```text
CVRPTW:
  customer C1 → route의 한 위치

RPDPTW:
  request R1 → pickup P1 + delivery D1
  same concrete vehicle
  position(P1) < position(D1)
  pair 전체가 route 또는 bank에 존재
```

Capacity 10인 차량과 demand 6, 5인 두 request를 생각한다.

```text
P1(+6) → D1(-6) → P2(+5) → D2(-5)  # feasible
P1(+6) → P2(+5) → D1(-6) → D2(-5)  # prefix load 11, infeasible
```

방문 집합이 같아도 순서가 다르면 feasibility가 달라진다. 그래서 pool artifact의
identity에는 ordered service visit이 들어가며, selector가 request set만 반환해서는
안 된다.

### 4.2 필수 용어집

| 용어 | 이 Phase에서의 정확한 뜻 | 흔한 오해 |
|---|---|---|
| `Request` | Pickup과 delivery 의미를 소유하는 원자적 운송 의무 | Node 하나 또는 order 문자열 |
| `RoutePlan` | Concrete vehicle, terminal policy와 ordered visits | Vehicle class + request set |
| `SearchRequestBank` | Stable search state에서 route 밖 request ID membership | 최종 `UNASSIGNED` reason |
| `CompletedTrial` | Pair/structure와 full evaluation을 마친 commit 전 ALNS trial | Accepted solution |
| `SearchSnapshot` | Immutable committed routes/bank/evaluation | Mutable pool view |
| `EvaluatedRouteArtifact` | Projection과 독립된 immutable route + authoritative evaluation | Verified route 또는 MIP column |
| `ProjectedRouteColumn` | 한 projection/model의 exact rows/coefficient를 encode한 column | Domain route |
| `RoutePoolDelta` | Admission 결과를 담는 append-only derived artifact | Search state mutation |
| `RoutePoolSnapshot` | Stable order로 seal된 read-only pool | Live collector |
| `AlnsWarmStart` | Committed stable routes/bank와 RNG/adaptive continuation policy | Model variable hint |
| `MipWarmStart` | Model variable에 대한 hint와 feasibility record | 다음 ALNS의 route/bank state |
| `RouteSelectionOutcome` | Status, incumbent presence와 selected column IDs | Candidate solution |
| `MaterializedSelectionDraft` | Selected artifacts를 fresh copy해 만든 미검증 draft | Committed candidate |
| `EvaluatedSelectionCandidate` | Exact partition과 full evaluation을 마친 adoption 입력 | Publishable result |
| `HybridPhaseIncumbent` | 직전 ALNS segment의 cache-free validated best | Raw backend incumbent |
| `HybridPhaseRecord` | Pool/model/backend/materialization/adoption/fallback lineage | Phase acceptance receipt |
| `Phase13ApplicabilityReceipt` | Scheduler/Phase 14 control-plane의 `Skip` 또는 `Activated` | Phase 13 구현 artifact |

`verified`는 Phase 07 independent verifier의 `PASS` 뒤에만 쓴다. Pool admission에서
full evaluation을 했다는 이유로 `VerifiedRoute`라고 이름 붙이지 않는다.

### 4.3 Identity를 한 digest로 뭉치지 않기

Phase 13은 최소 다음 identity를 분리한다.

```text
RouteSignature
  = problem + travel + profile
  + concrete vehicle + terminal policy
  + ordered service visit IDs

RouteCoverageKey
  = same authority + same concrete vehicle + exact RequestId set

RouteArtifactId
  = RouteSignature digest + authoritative evaluation fingerprint

ProjectedColumnId
  = RouteArtifactId + projection fingerprint + row/coefficient digest

RoutePoolSnapshotId
  = authority + approved pool-policy + stable artifact order + pins/proofs

RouteSelectionRunId
  = solve/round/worker/hybrid phase
  + pool/model/warm-start/backend/budget/policy fingerprints

AlnsSegmentRunId
  = solve/round/worker/hybrid phase + segment ordinal
  + requested ALNS completed-step budget
```

이 분리가 필요한 이유는 다음과 같다.

- 같은 request coverage라도 visit order가 다르면 load/time이 다르다.
- 같은 route artifact도 projection이 달라지면 다른 column이 된다.
- 같은 model이라도 backend config가 달라지면 reproducibility envelope가 다르다.
- Provider locator, elapsed time, completion order는 semantic identity가 아니다.
- Secret은 어느 fingerprint에도 포함하지 않는다.

Fingerprint algorithm/version은 upstream identity/serialization ADR을 소비한다.
이 가이드에 기록한 git blob을 product artifact hash의 숨은 기본값으로 재사용하지 않는다.

### 4.4 Stable lifecycle

ALNS lifecycle:

```text
immutable SearchSnapshot
→ changed-route COW TrialDraft
→ structural exactness + authoritative full evaluation
→ CompletedTrial
→ accept: new SearchSnapshot
→ reject/fail/cancel: draft 전체 discard
```

Pool lifecycle:

```text
CompletedTrial의 completed hard-feasible routes
→ authority/admission validation
→ RoutePoolDelta
→ append/import with one merge rule
→ pin + conservative dominance
→ stable-order seal
→ immutable RoutePoolSnapshot
```

Selection lifecycle:

```text
RoutePoolSnapshot
→ exact projection or typed skip
→ frozen model + MIP warm start
→ backend status + selected ProjectedColumnIds
→ identity validation
→ fresh MaterializedSelectionDraft
→ exact request/vehicle partition
→ full propagation/evaluation
→ EvaluatedSelectionCandidate
→ strictly-better comparison
→ adopt or retain incumbent
```

Outer hybrid lifecycle과 work accounting:

```text
ExecutionRound
→ WorkerRun(requested phase2MaxSteps)
→ HybridPhase[0..n)
   → ALNS segment(requested/completed ALNS steps)
   → pool seal
   → selector(admission wait + separate work/time budget)
   → materialize/evaluate/adopt-or-retain
   → exactly one committed AlnsWarmStart for the next phase
→ require Σ completed ALNS segment steps = phase2MaxSteps
```

`phase2MaxSteps`, segment 분할, phase 수, selector work/time/admission budget의 실제
숫자는 계속 `Q-BENCH-02`/approved hybrid config 소유의
`OPEN — EXPERIMENT_REQUIRED` 또는 `OPEN/GATED`다. 다만 숫자가 승인되면 합계와
분리 accounting을 지켜야 한다는 구조적 invariant는 지금 고정한다. Selector의
deterministic work, wall time, admission wait, model build나 materialization work를
ALNS completed step으로 세지 않는다.

Publication lifecycle:

```text
committed hybrid champion
→ Phase 07 candidate verifier PASS
→ finalization + insertion audit
→ result-integrity verifier PASS
→ publishable result
```

Backend status와 selected IDs는 publication lifecycle의 시작도 끝도 아니다.

### 4.5 Fixed invariant

1. Request의 same-vehicle, exactly-once, precedence, route-bank XOR를 항상 지킨다.
2. Pool route는 nonempty, pair-complete, completed, hard-feasible, full-evaluated다.
3. Problem/travel/profile authority가 다른 artifact는 같은 pool에 들어가지 않는다.
4. Append/import/reload는 같은 validation과 deterministic merge를 사용한다.
5. Same signature와 다른 evaluation은 임의 winner가 아니라 integrity failure다.
6. Safe dominance proof가 없으면 같은 coverage route를 제거하지 않는다.
7. Incumbent artifact는 seal/pruning에서 pin한다.
8. Selector는 live mutable pool이 아니라 sealed snapshot만 받는다.
9. Artifact와 column type/identity를 합치지 않는다.
10. Non-projectable constraint/dimension은 typed skip하며 surrogate/hidden Big-M로
    바꾸지 않는다.
11. Request exact partition과 concrete-vehicle consumption을 model에 명시한다.
12. Backend는 incumbent가 있는 status에서만 selected value를 읽는다.
13. Backend output의 selected ID가 exact model/pool에 속하는지 확인한다.
14. Materialization은 pool route를 alias/mutate하지 않는 fresh copy다.
15. Full propagation/evaluation과 bound comparator가 adoption authority다.
16. `STRICTLY_BETTER`만 채택한다.
17. Equal/worse/invalid/no-incumbent/failure에서 incumbent와 next warm start는 같다.
18. Required failure는 degraded normal success가 아니다.
19. Phase 07 candidate/result verifier를 모두 통과해야 publish할 수 있다.
20. Optimize 시작 뒤 같은 logical run의 result-bearing retry를 하지 않는다.
21. Gate가 닫히면 Phase 13 allocation, identity, provider discovery와 native load는 0이다.
22. Open/gated/deferred 값을 boolean·숫자·provider default로 채우지 않는다.
23. `ALNS step < ALNS segment < HybridPhase < WorkerRun < ExecutionRound` 계층을
    보존하고 worker의 completed ALNS segment-step 합은 declared
    `phase2MaxSteps`와 정확히 같다.
24. Selector work/time/admission budget과 ALNS completed-step budget을 서로
    차감·대입하지 않는다.
25. `AlnsWarmStart`와 `MipWarmStart`는 다른 type/identity/lifecycle이며 서로
    대입하거나 한 fingerprint로 합치지 않는다.
26. Strict improvement만 fresh champion에서 새 `AlnsWarmStart`를 만든다.
    Equal/worse/invalid/skip/failure이면 이전 incumbent routes/bank,
    RNG/adaptive continuation과 next `AlnsWarmStart` bytes가 모두 같다.
27. 한 hybrid phase는 이전 incumbent를 mutate하지 않고 champion/record/next
    warm start를 정확히 한 번 atomic commit한다. Gate, projection, session,
    materialization 또는 cleanup 실패 전에는 결과-bearing side effect가 0이다.

### 4.6 Selection 수학을 작게 읽기

Canonical proposed model은 `SET_PARTITION_EXACT`다.

- \(I\): 모든 input request
- \(R\): sealed snapshot에서 projection한 route columns
- \(V\): 모든 concrete input vehicles
- \(x_r\): route column \(r\) 선택 여부
- \(u_i\): request \(i\)를 bank에 남길지 여부
- \(a_{ir}\): route \(r\)이 request \(i\)의 complete pair를 포함하는지
- \(h_{vr}\): route \(r\)이 concrete vehicle \(v\)를 쓰는지

```text
모든 request i:
  Σ(a_ir × x_r) + u_i = 1

모든 concrete vehicle v:
  Σ(h_vr × x_r) ≤ 1
```

`u_i`가 있기 때문에 “모든 request를 반드시 route에 넣어 infeasible하게 만들기”와
“누락을 허용해 silently drop하기”를 모두 피한다. Mandatory request도 승인된 exact
assignment mode가 아니면 자동으로 `u_i = 0` hard constraint가 아니다. Bound profile의
최상위 `mandatoryUnassignedCount` 의미를 따른다.

Lexicographic objective는 각 상위 dimension의 exact optimality가 증명된 뒤에만
그 값을 고정하고 다음 dimension을 푼다. `FEASIBLE_LIMIT`에서 하위 dimension을 계속
최적화하거나 임의 Big-M 하나로 평탄화하지 않는다.

## 5. 실제 repository inventory — HEAD baseline, live drift와 목표

### 5.1 조사 방법과 고정 정책

Correction inventory는 `2026-07-29T02:26:03+0900` 한 시점에 고정했다. 그 뒤
다른 작업의 변화를 accepted evidence처럼 추적하거나 이 문서의 의미를 바꾸지
않는다. 실제 구현 entry에서는 scheduler가 가리키는 exact accepted source로 새
snapshot을 만들고 아래 값과 다르면 semantic/POM/test-topology impact를 재검토한다.

```text
HEAD baseline:
  branch  = codex-implementation
  commit  = 7cc890ee1d0805df5ae14b633127fade4f978639

frozen live snapshot:
  status output blob = 85277a4a7ef615383ac0665dd295f7fed09ad208
  status output SHA-256 = 7ff2b5b6c96f7a76c6b2e21e904ed1a7f1cfa3c18f11682221c63a537475a4fb
  root POM blob = 1dc675ba17b7f2202f34a22131f152cc2868b075
  solver/application/verification POM blobs
    = facf6c32ea130d7506a3bf4567ecede27de194e7
    / a580f6ca04ae661131bc820aeb4348b27aa5aadc
    / 4a61586ff0d40c566c802799c5d458f61f028d6e
  architecture guard blob = 4b37ab6b0132e6d76a91d4ecf9ae6950e40be49e
  uncommitted/unapproved concurrent Phase 00 scaffold observed
```

HEAD와 live를 분리하는 이유는 live 파일 존재가 accepted evidence가 아니기 때문이다.
현재 [Execution Progress](../../execution-progress-and-results.md)는 correction
snapshot blob `f875edb…`이고 Phase 00 prerequisite remediation을 기록한다. 이
변경은 scheduler 소유이며 이 가이드가 수정하거나 Phase 13 status로 확대 해석하지
않는다.

### 5.2 HEAD baseline과 live snapshot

| 항목 | HEAD baseline | Frozen live snapshot | Phase 13 판정 |
|---|---:|---:|---|
| Root POM | 1개, blob `f8a411e…` | Reactor parent로 변경, blob `1dc675b…` | 미커밋 Phase 00 scaffold |
| 전체 POM | 1 | 13 (`node_modules`, `target` 제외) | Module 존재는 Phase 13 entry/evidence가 아님 |
| Main Java | 6 | 33 | 23개는 stable module의 `package-info.java`, 10개는 legacy executable source |
| Test Java | 1 | 16 Java / executable `*Test`·`*IT` 14 | Architecture 9 + fixture Java 2 + legacy test 5; Phase 13 test 0 |
| Maven wrapper | HEAD 없음 | `mvnw`, `mvnw.cmd`, `.mvn/wrapper` 존재 | 미커밋 scaffold; 사용 가능성만 관찰 |
| Stable modules | 없음 | `core`, `solver`, `verification`, `application`, `capabilities`, `profile-catalog` POM 존재 | Package skeleton, semantic 구현 아님 |
| Legacy | Root `src/**` | `legacy/gcp-placeholder`로 이동 중 | Placeholder 보존 작업 |
| Phase 13 pool package | 없음 | 없음 | 구현 부재 |
| Phase 13 selection package | 없음 | 없음 | 구현 부재 |
| Phase 13 hybrid package/module | 없음 | 없음 | 구현 부재 |
| OR-Tools adapter dir | 없음 | 없음 | 구현/activation 부재 |
| OR-Tools dependency | 없음 | 없음 | Denylist/self-test 문자열만 존재 |
| Stable owner test dependency | 없음 | solver/application/verification에 JUnit·test-fixtures test-jar 없음 | 미래 test는 현재 compile/discover 불가 |
| Failsafe | 없음 | root/owner POM에 plugin/execution 없음 | `*IT` 파일만 추가하면 실행되지 않음 |
| Surefire default | 없음 | root `failIfNoTests=false` | Phase 13 owner module에서 explicit non-zero gate 필요 |
| Phase 13 evidence | 없음 | 없음 | `NOT_PRODUCED` |

Live root는 `rpdptw`, `build`, `legacy`를 module로 선언하고, `rpdptw/pom.xml`은
여섯 stable module을 선언한다. `rpdptw/solver`의 executable semantic type은 0이며
`portfolio`, `search`, `state`, `termination`의 `package-info.java`만 있다.
`solver.pool`, `solver.selection`, `solver.hybrid` 디렉터리는 없다.

[ProviderAndVendorIsolationArchitectureTest](../../../../build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/ProviderAndVendorIsolationArchitectureTest.java)는
현재 default reactor가 `adapters/route-selection-ortools-cpsat`와 route-selection
service/type을 광고하지 않는지 검사한다. 이 test와 architecture files도 live
untracked scaffold이며 Phase 13 accepted evidence가 아니다.

현재 guard의
`defaultReactorAdvertisesNoRouteSelectionCapability()`는 adapter directory 자체가
없어야 한다고 assert한다. Gate-open WP가 optional adapter source directory를 만들면
default reactor/profile/dependency/service/native load가 0이어도 이 assertion은
실패한다. 그러므로 WP13-4는 guard를 삭제하지 않고 §8.4의 versioned transition으로
바꾼 뒤 positive/negative profile test를 함께 봉인해야 한다.

### 5.3 현재 존재·부재·placeholder를 정확히 읽기

| 분류 | 실제 예 | 할 수 있는 주장 | 할 수 없는 주장 |
|---|---|---|---|
| 존재 | Reactor POM, stable package-info, architecture tests | Phase 00 scaffold가 관찰됨 | Phase 00 또는 13 accepted |
| 부재 | Pool/selection/hybrid Java, backend adapter, E-P13 bundle | Phase 13 implementation이 없음 | “disabled hybrid 구현 완료” |
| Placeholder | `legacy/gcp-placeholder`의 synthetic `AlnsBatchEngine` | Legacy characterization 대상 | Accepted ALNS incumbent/pool source |
| Negative guard | OR-Tools denylist와 no-capability test | Default stable module vendor 격리 의도 | Approved backend integration |
| Planned | Phase 문서의 proposed tree/type/test | Gate-open 뒤의 review input | 현재 API 또는 compile evidence |

특히 exact-token 검색에서 `com.google.ortools`, `CpSolver`, `MPSolver`는 stable
production source/dependency가 아니라 architecture denylist와 self-test에만 나타났다.
문자열 0 또는 denylist 존재는 semantic architecture proof 전체가 아니다.

### 5.4 Read-only inventory 명령

다음 명령은 source를 바꾸지 않고 current/HEAD를 다시 조사할 때 쓸 수 있다.

```bash
git rev-parse HEAD
git status --short
git ls-tree -r --name-only HEAD

rg --files -g 'pom.xml' -g '!**/target/**'
rg --files -g '**/src/main/java/**/*.java' -g '!**/target/**'
rg --files -g '**/src/test/java/**/*.java' -g '!**/target/**'

rg -n 'com\.google\.ortools|ortools-java|CpModel|CpSolver|MPSolver' \
  --glob 'pom.xml' --glob '*.java' --glob '!legacy/**' --glob '!**/target/**'
```

외부 변경이 계속되면 새 상태를 승인된 baseline으로 만들지 않는다. 구현 시작 시
총괄 scheduler가 exact source commit/build/evidence receipt를 새로 지정해야 한다.

## 6. Scope, non-scope, 결정 상태와 사람 승인

### 6.1 Gate가 열린 뒤의 포함 범위

- Complete authority receipt의 fail-closed 검증
- Gate-closed absence와 unauthorized request rejection
- Same-authority immutable evaluated route admission, delta, merge와 seal
- Incumbent pin과 conservative/safe dominance
- Exact profile projection 또는 typed non-projectable skip
- Request/unassigned/concrete-vehicle exact partition
- Backend-neutral session, budget, cancellation과 typed outcome
- Approved direct CP-SAT adapter의 integer model/status/native lifecycle
- Selected ID membership과 fingerprint validation
- Fresh materialization, exact partition, full propagation/evaluation
- Strictly-better adoption과 exact incumbent preservation
- Optional fallback와 required incomplete의 구분
- ALNS segment-step 합계, selector work/time/admission의 별도 accounting
- `AlnsWarmStart`/`MipWarmStart` 분리, hybrid identity, retry와 reproducibility class
- Phase 07 two-gate handoff
- Security/license/SBOM/operations/performance/cost/shadow/rollback evidence
- Phase 14B용 closed `Skip` 또는 accepted open `Activated` handoff

### 6.2 명시적 비범위

- 현재 gate가 닫힌 상태에서 Phase 13 Java/POM/test/backend를 만드는 일
- Root/common/stable generic module에 OR-Tools dependency를 추가하는 일
- Backend/classpath 자동 발견 또는 `hybrid.enabled=false`를 canonical absence로 쓰는 일
- OR-Tools version, workers, seed, time, gap, pool cap, cadence, traffic split의
  숨은 기본값
- Gurobi, 다른 commercial optimizer 또는 `MPSolver`를 silent fallback으로 쓰는 일
- Non-projectable profile을 Big-M/surrogate로 조용히 근사하는 일
- Cross-worker pool fan-in/central selector를 worker-local baseline에 포함하는 일
- Phase 12 provider adapter를 Phase 13에서 재구현하는 일
- Phase 07 verifier/result/publication 의미를 Phase 13에 복제하는 일
- Phase 14A acceptance criteria, Phase 14B official values, traffic/pointer cutover와
  production authority를 정하는 일
- `Q-VAR-01`, multi-trip/rotation, MDVRP/OVRP/SDVRP를 함께 활성화하는 일
- Public API/wire error schema를 internal enum에서 자동 확정하는 일

### 6.3 확정·proposed/open·gated·deferred

| 항목 | 상태 | 구현자가 지켜야 할 의미 |
|---|---|---|
| Phase 13 optional branch | `FIXED / C-17 GATED` | Critical path 기본 predecessor가 아님 |
| ALNS-first 순서 | `FIXED` | Phase 14A receipt 전 시작 금지 |
| Pool artifact/column 분리 | `FIXED` | Type과 identity를 합치지 않음 |
| Exact projection/typed skip | `FIXED` | Hidden approximation 0 |
| Selected IDs only | `FIXED` | Raw backend objective/status 비권위 |
| Fresh full evaluation | `FIXED` | Materialization 뒤 authoritative recomputation |
| Strictly-better adoption | `FIXED` | Equal/worse/failure에서 incumbent 유지 |
| Direct Java CP-SAT | `FIXED POLICY` | Backend family만 고정, 구현 승인이 아님 |
| Exact OR-Tools version/config | `OPEN/GATED` | 임의 pin 또는 library default 금지 |
| Module/type 이름 | `PROPOSED INTERNAL` | Phase 00/C-17 architecture review에서 확정 |
| Model mode 노출 | `PROPOSED/OPEN` | `SET_PARTITION_EXACT`를 product default로 자동 승격 금지 |
| Pool cap/pruning/budget/cadence | `OPEN — EXPERIMENT_REQUIRED` | TEST_ONLY 값만 explicit |
| Optional vs required product 의미 | `OPEN/GATED` | Failure status를 임의 선택 금지 |
| Reproducibility class | `OPEN per config` | 반복 evidence 없는 strong claim 금지 |
| Cross-worker central selector | `DEFERRED / separate ADR` | Worker-local evidence 뒤 별도 승인 |
| `Q-BENCH-02` official 수치 | `OPEN — EXPERIMENT_REQUIRED` | Phase 14B official gate |
| `Q-VAR-01` optional variants | `DEFERRED` | 질문·활성화 금지 |
| Phase 14 production authority | `SEPARATE / NOT_GRANTED` | Phase 13 완료로 대체 불가 |

### 6.4 사람 승인이 필요한 결정

| 승인 질문 | 승인 주체 | 없을 때 행동 |
|---|---|---|
| 어떤 solve/customer/profile에 hybrid를 적용하는가? | Product + Algorithm | Phase 13 path 없음 |
| Worker-local route selection의 정확한 의미와 authority는? | Product + Algorithm + Architecture | Interface 구현 시작 금지 |
| Optional인가 required인가? | Product + Operations | Failure mapping 확정 금지 |
| Exact projection/model mode는 무엇인가? | Profile/Projection owner | Typed skip까지만 설계 |
| OR-Tools artifact/version/checksum/platform은? | Architecture + Security/Supply-chain | Adapter module 생성 금지 |
| Native extraction/redistribution/SBOM/CVE는? | Legal + Security + Operations | Packaging/배포 금지 |
| Budget/workers/seed/time/gap은? | Algorithm + Benchmark + FinOps | Runtime value 없음 |
| Pool cap/pruning/cadence는? | Algorithm + Performance | Hidden policy 없음 |
| Traffic split와 activation scope는? | Product + Release + Operations | Production traffic 0 |
| Signed applicability trust 정책은? | Phase 14B Security + Release | Phase 13 entry/acceptance는 역차단하지 않되 Phase 14B action은 0 |

마지막 행은 Phase 13 owner의 producer approval 목록이 아니다.
`G14-SIGNING-TRUST`는 accepted `Phase13ActivatedHandoff` 또는 scheduler-owned
`Skip`을 Phase 14B가 소비할 때만 적용한다. Phase 13 implementation/acceptance가
signature/trust `PASS`를 생산하거나 Phase 14B trust gate가 Phase 13 entry로
역방향 의존하는 두 경우를 모두 금지한다.

### 6.5 마지막 안전 지점

현재와 모든 실패 상황의 last safe point는 다음이다.

```text
accepted ALNS-only build/manifest
+ cache-free validated ALNS incumbent
+ Phase 07 both-gate publication path
+ no Phase 13 assembly/provider/native activation
```

Rollback은 pool artifact를 삭제하거나 overwrite하는 일이 아니다. Activated
composition/plan을 선택하지 않고 이전 immutable ALNS-only pointer/manifest로
되돌리는 일이다. Accepted ALNS-only receipt는 Phase 13 실험 결과로 소급 수정하지
않는다.

## 7. 사람을 위한 학습 경로

### 7.1 1단계 — 개념을 자기 말로 설명하기

다음 질문에 code 없이 답한다.

- 왜 `RequestId set`만으로 route identity가 충분하지 않은가?
- 왜 rejected whole solution의 route도 pool에 들어갈 수 있는가?
- 왜 `EvaluatedRouteArtifact`와 `ProjectedRouteColumn`이 다른가?
- 왜 CP-SAT `FEASIBLE`이 곧 adoptable candidate가 아닌가?
- 왜 `u_i`가 필요하며 mandatory를 자동 hard assignment로 만들지 않는가?
- 왜 equal candidate를 채택하지 않는가?
- 왜 Phase 14A가 Phase 13보다 먼저인데 Phase 14B는 나중일 수 있는가?

완료 신호는 각 답에 producer, authority, failure와 rollback이 포함되는 것이다.

### 7.2 2단계 — 작은 탐색으로 계약 확인하기

종이에 3 request, 2 concrete vehicle, 5~8개의 route column을 만든다.

1. 각 route에 ordered visits와 request coverage를 적는다.
2. Pair가 불완전하거나 capacity/time이 위반된 route는 pool admission 전에 제거한다.
3. 각 request row의 `Σa_ir x_r + u_i = 1`을 손으로 만든다.
4. 각 concrete vehicle row의 `Σh_vr x_r ≤ 1`을 만든다.
5. 가능한 모든 column subset을 열거해 exact optimum을 구한다.
6. 선택된 route를 새 목록으로 복사하고 bank를 coverage complement로 만든다.
7. Full evaluator가 backend coefficient와 다른 결론을 내는 corruption 사례를 만든다.
8. Better/equal/worse를 각각 comparator에 넣어 better 하나만 채택되는지 설명한다.

완료 신호는 backend 구현을 사용하지 않은 독립 expected result가 생기는 것이다.

### 7.3 3단계 — 현재 repository를 읽기

다음 사실을 직접 확인한다.

- `rpdptw/solver/pom.xml`은 현재 `rpdptw-core`만 compile-depend한다.
- Stable production source는 `package-info.java` skeleton뿐이다.
- `solver.pool`, `solver.selection`, `solver.hybrid`가 없다.
- `adapters/route-selection-ortools-cpsat`가 없다.
- Default architecture test가 route-selection capability absence를 검사한다.
- OR-Tools dependency가 없다.
- Phase 14A receipt와 E-P13 evidence가 없다.

완료 신호는 “지금 구현할 파일 목록”이 아니라 “왜 지금 멈춰야 하는가”를 inventory와
receipt로 설명하는 것이다.

### 7.4 4단계 — gate-open 뒤 작은 변경에서 통합으로

Gate가 실제로 열리면 다음 순서로 학습한다.

```text
admission predicate
→ immutable delta/snapshot
→ tiny exact projection
→ deterministic fake backend
→ materialization/full evaluation
→ optional fallback
→ approved CP-SAT adapter
→ Phase 07 boundary
→ shadow/rollback/evidence
```

Backend부터 시작하지 않는다. Fake와 tiny oracle로 solver-neutral contract가 먼저
green이어야 CP-SAT integration 실패를 domain/model 결함과 분리할 수 있다.

### 7.5 통합 완료 자문 질문

- Closed gate에서 Phase 13 class-load/provider/native count가 정확히 0인가?
- Pool snapshot은 append/import/reload 순서와 무관하게 같은 bytes/digest인가?
- Tiny oracle가 production projector/backend helper를 전혀 호출하지 않는가?
- Non-projectable profile이 typed skip으로 보이는가?
- Selected ID가 stale면 materialization count가 0인가?
- Full evaluator 결과가 backend objective보다 우선하는가?
- 모든 failure 전후 incumbent/next warm-start fingerprint가 같은가?
- Phase 07 두 verifier 중 하나라도 fail하면 publication count가 0인가?
- Strong replay claim에 exact backend envelope와 반복 evidence가 있는가?
- Evidence manifest가 review/acceptance receipt를 역참조하지 않는가?

## 8. 목표 module/package/file과 dependency

### 8.1 예상 change tree

아래 tree는 gate-open 뒤의 후보이며 현재 생성하지 않는다.

```text
rpdptw/
├── solver/
│   ├── pom.xml                            # JUnit + test-fixtures + Surefire closure
│   ├── src/main/java/com/ronext/rpdptw/solver/
│       ├── pool/
│       │   ├── EvaluatedRouteArtifact.java
│       │   ├── RoutePoolDelta.java
│       │   ├── RoutePoolSnapshot.java
│       │   ├── RoutePoolCollector.java
│       │   └── RouteDominancePolicy.java
│       ├── selection/
│       │   ├── api/
│       │   ├── projection/
│       │   └── conversion/
│       └── hybrid/                       # exact owner is PROPOSED
│   └── src/test/java/...                 # pool/projection/session tests
├── application/
│   ├── pom.xml                            # JUnit + test-fixtures + Surefire closure
│   ├── src/main/java/com/ronext/rpdptw/application/
│       └── hybrid/                       # exact owner is PROPOSED
│   └── src/test/java/...                 # lifecycle/adoption tests
└── verification/
    ├── pom.xml                            # required contract-test closure
    └── existing Phase 07 API reuse only

rpdptw/hybrid-application/                # alternative PROPOSED isolation
├── pom.xml
└── optional assembly + tests; Phase 00/C-17 ADR required

adapters/
├── pom.xml                               # optional profile/reactor registration
└── route-selection-ortools-cpsat/        # approved backend only
    ├── pom.xml                            # OR-Tools only here; Failsafe for *IT
    ├── src/main/java/...
    └── src/test/java/...                  # unit + native/integration *IT

build/test-fixtures/
├── pom.xml                               # test-jar producer
└── phase13/                              # test-only builders/oracles

build/architecture-rules/
├── pom.xml
└── src/test/java/...                     # versioned default/profile guards
```

Canonical docs에는 application 내부 `hybrid` 후보와 별도
`rpdptw-hybrid-application` 후보가 모두 보인다. 이를 이 가이드에서 임의로 하나로
확정하지 않는다. Gate-open architecture review가 다음 두 불변조건을 만족하는
배치를 고른다.

1. Closed ALNS-only composition은 Phase 13 type/provider/native를 resolve/load하지 않는다.
2. Verification은 solver/search/pool/backend에 compile-depend하지 않는다.

### 8.2 Compile dependency 후보

```text
rpdptw-solver             → rpdptw-core
rpdptw-verification       → rpdptw-core
rpdptw-application        → core + approved solver API + verification
optional hybrid assembly  → core + solver API + verification
route-selection-ortools-cpsat → core + exported selection API

verification              ↛ solver/search/pool/backend
core/generic solver/application/verification ↛ com.google.ortools
default ALNS-only distribution ↛ optional backend/native loader
```

Phase 12 cloud provider adapter와 Phase 13 optimizer backend adapter는 직교 축이다.
한 `adapters/provider` module에 합치지 않는다.

### 8.3 책임 배치 자문표

| 질문 | 올바른 owner 후보 | 잘못된 배치 |
|---|---|---|
| Route가 pool에 들어갈 수 있는가? | `solver.pool` admission | CP-SAT adapter |
| Profile이 exact-projectable한가? | Projection capability/bound profile seam | Backend status mapper |
| CP-SAT variable/status를 어떻게 매핑하는가? | Optional backend adapter | Core domain |
| Selected IDs로 route를 새로 만드는가? | Conversion/application | Backend |
| Candidate가 더 좋은가? | Bound full evaluator/comparator | `CpSolver.objectiveValue()` |
| Publish 가능한가? | Phase 07 verification/result | Hybrid orchestrator |
| Closed/open applicability는? | Scheduler/control plane | Solver pool |
| Production traffic을 켜는가? | Phase 14B authority | Phase 13 |

### 8.4 Gate-open Maven/test closure와 architecture guard transition

이 절도 `PROPOSED/CONDITIONAL`이다. Exact module/profile 이름과 dependency version은
승인 receipt가 freeze한다. 다만 미래 source/test를 실제로 compile·discover·execute하는
closure는 다음 불변조건을 모두 만족해야 한다.

1. Test를 소유하는 solver/application/verification/optional assembly POM은 JUnit과
   `build/test-fixtures` test-jar를 필요한 scope로 선언한다. Source만 추가하고 owner
   POM을 비워 두지 않는다.
2. Root parent의 Surefire policy와 owner module의 selected-test policy를 함께 고정한다.
   Exact `-Dtest` 실행은 `surefire.failIfNoSpecifiedTests=true`이고 report에서 expected
   class/method가 non-zero임을 대조한다.
3. `*IT`는 승인된 optional integration profile에서 Failsafe
   `integration-test`/`verify` execution으로 실행한다. Exact `-Dit.test` 실행은
   `failsafe.failIfNoSpecifiedTests=true`이며 Surefire success로 대체하지 않는다.
4. Default reactor는 optional adapter를 resolve하지 않는다. Approved profile만 exact
   assembly/adapter module을 reactor에 넣고 OR-Tools dependency/native loader를
   그 module 경계 안에 둔다.
5. Cold local repository에서도 sibling prerequisite가 우연히 선설치돼 있다고
   가정하지 않는다. Gate-open evidence는 fresh isolated Maven repository에서 먼저
   root `-pl <exact-owner>,<exact-prerequisites> -am clean install -DskipTests`로
   prerequisite closure를 만든 뒤 owner-selected test를 실행한다.
6. Required owner test와 integration test는 zero-test success를 허용하지 않는다.
   Surefire/Failsafe XML의 tests/failures/errors/skipped와 command exit를 함께 봉인한다.

현재
`ProviderAndVendorIsolationArchitectureTest.defaultReactorAdvertisesNoRouteSelectionCapability()`
의 “adapter directory가 없어야 한다” assertion은 optional source tree가 생기는 순간
낡는다. WP13-4는 guard를 삭제하지 않고 같은 변경 세트에서 다음 versioned contract로
전이한다.

- Default effective reactor/profile/module/dependency/service descriptor/class-load/native-load
  count는 모두 0이다.
- Stable core/solver/application/verification compile graph의 OR-Tools reference는 0이다.
- Approved integration profile에서는 exact optional assembly와 adapter가 각각 한 번
  resolve되고, neutral contract/tiny oracle/native `*IT`가 non-zero로 실행된다.
- Wrong/missing profile, duplicate provider, generic dependency leak과 service discovery
  activation은 negative test로 실패한다.
- Old directory-absence assertion의 의도와 replacement test identity를 evidence와
  architecture decision에 남긴다.

따라서 “guard를 지워 build를 green으로 만들기”, 이미 warm한 local repository에서
owner `-f`만 실행하기, `*IT`를 Surefire가 잡을 것이라 가정하기는 모두 금지다.

## 9. Proposed/Open Java 계약, state transition과 pseudocode

### 9.1 표기 규칙

이 절의 모든 Java는 **skeletal contract**다.

- `PROPOSED`: 책임과 타입 차이를 토론하기 위한 후보
- `OPEN`: 이름, field, config 또는 public exposure가 미승인
- `FIXED MEANING`: 이름이 바뀌어도 보존해야 하는 의미

완성 implementation을 복붙하지 않는다. Constructor validation, serialization,
visibility와 error taxonomy는 승인된 upstream contract에 맞춰 설계한다.

### 9.2 Scheduler disposition과 activation

```java
// PROPOSED control-plane type. Closed path는 Phase 13 assembly에 의존하지 않는다.
sealed interface SchedulerPhase13Disposition {
    record Skip(Phase13ApplicabilityReceipt.Skip receipt)
        implements SchedulerPhase13Disposition {}

    record Reject(UnauthorizedHybridActivation failure)
        implements SchedulerPhase13Disposition {}

    record Open(Phase13ActivationReceipt receipt)
        implements SchedulerPhase13Disposition {}
}

// PROPOSED Phase 13 boundary. Open 뒤에만 호출한다.
interface Phase13AuthorizedScopeVerifier {
    ValidatedPhase13Scope verifyOpenScope(
        HybridActivationRequest request,
        Phase13ActivationReceipt receipt,
        RequiredHybridAuthorityEvidence evidence
    );
}
```

`backend`, `budget`, `scope`, `requirementPolicy`가 생략되면 runtime default로 채우지
않는다. Missing field는 activation failure다.

### 9.3 Pool과 projection

```java
// PROPOSED: mutable TrialDraft가 아니라 canonical completed projection을 받는다.
interface RoutePoolCollector {
    RoutePoolDelta collect(
        CompletedTrialProjection trial,
        RouteAdmissionContext authority
    );
}

interface RoutePool {
    RoutePoolAppendResult append(RoutePoolDelta delta);
    RoutePoolAppendResult importDelta(RoutePoolDelta delta);
    RoutePoolSnapshot seal(RoutePoolSealRequest request);
}

interface RouteSelectionProjector {
    RouteProjectionResult project(
        BoundProfileProjectionDeclaration declaration,
        RoutePoolSnapshot snapshot
    );
}

sealed interface RouteProjectionResult {
    record Exact(RouteSelectionProjection projection,
                 RouteSelectionModelSpec model)
        implements RouteProjectionResult {}

    record SkippedNonProjectable(NonProjectableReport report)
        implements RouteProjectionResult {}

    record Rejected(ProjectionIntegrityFailure failure)
        implements RouteProjectionResult {}
}
```

`SkippedNonProjectable`은 exception을 삼킨 성공이 아니다. Exact projection을 만들 수
없다는 정상 typed disposition이며 optional/required plan이 다음 상태를 다르게 정한다.

### 9.4 Backend-neutral session과 outcome

```java
interface RouteSelectionSolverFactory {
    BackendCapability capability();
    RouteSelectionSession openSession(ApprovedBackendConfig config);
}

interface RouteSelectionSession extends AutoCloseable {
    RouteSelectionOutcome solve(
        RouteSelectionModelSpec model,
        RoutePoolSnapshot pool,
        MipWarmStart warmStart,
        RouteSelectionBudget budget,
        SolverCancellationProbe cancellation
    );

    @Override
    void close();
}

enum RouteSelectionStatus {
    OPTIMAL,
    FEASIBLE_LIMIT,
    NO_INCUMBENT_LIMIT,
    PROVEN_INFEASIBLE,
    MODEL_INVALID,
    BACKEND_UNAVAILABLE,
    NATIVE_RUNTIME_UNAVAILABLE,
    MODEL_BUILD_FAILED,
    SOLVER_FAILED,
    SKIPPED_NON_PROJECTABLE_PROFILE,
    SKIPPED_NO_BUDGET
}

record RouteSelectionOutcome(
    RouteSelectionStatus status,
    IncumbentPresence incumbentPresence,
    List<ProjectedColumnId> selectedColumns,
    RouteSelectionTerminationCause terminationCause,
    RouteSelectionRunId runId,
    Fingerprint modelFingerprint,
    Fingerprint poolFingerprint,
    Fingerprint outcomeFingerprint
) {}
```

`MipWarmStart`는 frozen model의 projected-column/unassigned variable 값과 model
fingerprint만 담는 selector-local hint다. 반면 `AlnsWarmStart`는 stable
route/bank, incumbent lineage와 다음 ALNS segment가 이어받을 RNG/adaptive state를
담는다. 두 타입, fingerprint와 lifecycle을 합치거나 selector 실패 뒤
`MipWarmStart`를 다음 ALNS segment에 넘기지 않는다. Exact field/name은
`PROPOSED/OPEN`이지만 이 의미 분리는 고정이다.

Outcome constructor/decoder는 status × incumbent field 조합을 검증해야 한다.
CP-SAT mapping의 고정 의미는 다음과 같다.

| CP-SAT raw | Incumbent | Provider-neutral |
|---|---:|---|
| `OPTIMAL` | 있음 | `OPTIMAL` |
| `FEASIBLE` | 있음 | `FEASIBLE_LIMIT` |
| `INFEASIBLE` | 없음 | `PROVEN_INFEASIBLE` |
| `MODEL_INVALID` | 없음 | `MODEL_INVALID` |
| `UNKNOWN` | 없음 | `NO_INCUMBENT_LIMIT` |
| JNI/native load failure | 없음 | `NATIVE_RUNTIME_UNAVAILABLE` |

`OPTIMAL`/`FEASIBLE`에서만 Boolean value를 읽는다. Time limit, cancellation,
resource cause는 raw status에서 추측하지 않고 별도 termination cause로 기록한다.

### 9.5 Materialization과 adoption

```java
interface RouteSelectionMaterializer {
    MaterializationResult materialize(
        RouteSelectionOutcome outcome,
        RouteSelectionModelSpec model,
        RoutePoolSnapshot pool,
        ImmutableProblemAuthority authority
    );
}

interface HybridCandidateAdopter {
    HybridAdoptionDecision compareAndAdopt(
        CacheFreeValidatedAlnsIncumbent incumbent,
        EvaluatedSelectionCandidate candidate,
        BoundObjectiveComparator comparator
    );
}

sealed interface HybridExecutionResult {
    record Committed(CommittedCandidate candidate,
                     HybridPhaseRecord record)
        implements HybridExecutionResult {}

    record Incomplete(HybridPhaseFailure failure,
                      Fingerprint unchangedIncumbent)
        implements HybridExecutionResult {}
}
```

`Committed`는 verified/publishable을 뜻하지 않는다. Phase 07 boundary로 전달할 수
있는 stable candidate라는 뜻이다.

### 9.6 Dependency와 state transition

Gate state:

```text
DOCUMENTED_GATED
├─ no request
│  → NOT_APPLICABLE_GATE_CLOSED
│  → scheduler-owned signed Skip control record
│  → canonical ALNS-only path
├─ explicit request + missing/invalid receipt
│  → UNAUTHORIZED_OR_INCOMPLETE_ACTIVATION
│  → reject before Phase 13 resolution/allocation
└─ explicit request + complete validated receipt
   → READY_FOR_IMPLEMENTATION
   → implementation/evidence/review
   → ACCEPTED_FOR_APPROVED_SCOPE
```

Gate-open execution:

```text
ALNS_INCUMBENT_COMMITTED
→ ROUTE_COLLECTION_COMPLETED
→ POOL_SEALED
├─ projection skipped/rejected
│  ├─ optional → INCUMBENT_RETAINED → COMMITTED
│  └─ required → INCOMPLETE
└─ MODEL_FROZEN
   → RUNTIME_ADMISSION
   → NATIVE_READY
   → SESSION_OPEN
   → SELECTION_FINISHED
   ├─ no incumbent/failure/cancel
   │  ├─ optional and uncontaminated → INCUMBENT_RETAINED → COMMITTED
   │  └─ required or contaminated → INCOMPLETE
   └─ selected IDs
      → IDENTITY_VALIDATED
      → FRESH_MATERIALIZED
      → EXACT_PARTITION
      → FULL_EVALUATED
      ├─ STRICTLY_BETTER → SELECTOR_ADOPTED → COMMITTED
      └─ EQUAL/WORSE/INVALID → INCUMBENT_RETAINED → COMMITTED
```

각 normal worker run은
`ALNS step < ALNS segment < HybridPhase < WorkerRun < ExecutionRound` 계층을
manifest에 남긴다. `HybridPhaseRecord`의 최소 의미는 ALNS segment별
requested/completed step, 그 합, separate selector admission/work/time, pre/post
ALNS incumbent bytes/fingerprint, pre/post RNG/adaptive state, MIP warm-start/model
fingerprint, session open/solve/cleanup/close count, typed outcome, adoption decision,
다음 `AlnsWarmStart`, commit count와 last-safe point다. Normal completion은
`Σ completedAlnsSegmentSteps == phase2MaxSteps`이고 selector work/time/admission은
그 합에 들어가지 않는다. Exact `phase2MaxSteps`, segment cadence와 selector budget은
여전히 `OPEN/GATED`; library default로 채우지 않는다.

### 9.7 Skeletal pseudocode

```text
# scheduler/control plane; no Phase 13 dependency
disposition = decideApplicability(request, controlAuthority)

if disposition is Skip:
    assert phase13ResolutionCount == 0
    assert backendDiscoveryCount == 0
    assert sideEffectCountBeforeGate == 0
    return runAlnsOnlyAndAttachSchedulerSignedSkip(disposition.receipt)

if disposition is Reject:
    assert phase13AllocationCount == 0
    assert sideEffectCountBeforeGate == 0
    return rejectBeforeSolve(UNAUTHORIZED_HYBRID_ACTIVATION)

# only Open crosses the optional assembly boundary
scope = verifyOpenScope(request, disposition.receipt, authorityEvidence)
requireEveryOpenConfigExplicitlyBound(scope, plan)
assert phase14SigningTrustWasNotRequiredForPhase13Entry
assert sideEffectCountBeforeGate == 0

alnsWarmStart = validatedStartingAlnsWarmStart
completedPhase2AlnsSteps = 0

for each declared HybridPhase in WorkerRun:
    require hierarchy == ALNS_STEP < ALNS_SEGMENT < HYBRID_PHASE < WORKER_RUN < EXECUTION_ROUND
    requestedSegmentSteps = explicitlyBoundSegmentBudget(plan)
    segment = runAlnsSegment(alnsWarmStart, requestedSegmentSteps)
    require segment.completedSteps == requestedSegmentSteps
    completedPhase2AlnsSteps += segment.completedSteps

    oldIncumbentBytes = canonicalBytes(segment.committedIncumbent)
    oldIncumbentFingerprint = fingerprint(oldIncumbentBytes)
    oldRngAdaptive = fingerprint(segment.nextAlnsWarmStart.rngAndAdaptiveState)
    lastSafePoint = acceptedAlnsOnlyPoint(segment)

    delta = collectCompletedHardFeasibleRoutes(segment, authority)
    snapshot = appendAndSeal(delta, stableOrder, pins, approvedPolicy)
    projection = projectExactOrTypedSkip(profileDeclaration, snapshot)

    if projection is skip/rejection:
        preparedDisposition = optionalRetainOrRequiredIncompleteWithoutMutation(
            segment.committedIncumbent, segment.nextAlnsWarmStart, projection, lastSafePoint)
    else:
        model = freezeModel(projection, snapshot, explicitConfig)
        mipWarmStart = buildAndValidateMipWarmStart(
            segment.committedIncumbent, model, snapshot)

        session = null
        outcome = null
        lifecycleFailure = null
        try:
            requireRuntimeAdmission(plan)             # selector accounting, not ALNS steps
            ensureApprovedNativeRuntime(plan.backend)
            session = factory.openSession(plan.backendConfig)   # exactly once
            outcome = session.solve(
                model, snapshot, mipWarmStart, plan.selectorBudget, cancel)  # exactly once
        catch open/solve failure:
            lifecycleFailure = typedFailureWithoutMutation()
        finally:
            if session != null:
                try cleanupSolveReferencesAndCallbacksExactlyOnce(session)
                catch cleanup failure: recordTypedCleanupFailure()
                try closeExactlyOnce(session)
                catch close failure: recordTypedCloseFailure()

        if cleanup/close failure:
            preparedDisposition = typedContaminatedIncompleteWithoutCommit(
                segment.committedIncumbent,
                segment.nextAlnsWarmStart,
                lastSafePoint)
        else if lifecycleFailure or outcome has no admissible incumbent:
            preparedDisposition = optionalRetainOrRequiredIncompleteWithoutMutation(
                segment.committedIncumbent,
                segment.nextAlnsWarmStart,
                typedOutcome,
                lastSafePoint)
        else:
            validateOutcomeIdentityAndMembership(outcome, model, snapshot)
            draft = materializeFresh(outcome.selectedIds, snapshot, authority)
            validateExactRequestAndVehiclePartition(draft)
            evaluated = fullPropagateAndEvaluate(draft, authority)
            decision = comparator.compare(evaluated, segment.committedIncumbent)

            if decision == STRICTLY_BETTER:
                freshChampion = prepareFreshChampion(evaluated)
                nextAlnsWarmStart = deriveNewAlnsWarmStart(
                    freshChampion, approvedRngAndAdaptiveContinuation)
                preparedDisposition = prepareStrictBetterCommit(
                    freshChampion, nextAlnsWarmStart, fullLineage)
            else:
                preparedDisposition = prepareRetainedCommit(
                    segment.committedIncumbent,
                    segment.nextAlnsWarmStart,
                    decision,
                    fullLineage)

    assert canonicalBytes(segment.committedIncumbent) == oldIncumbentBytes
    assert fingerprint(segment.committedIncumbent) == oldIncumbentFingerprint
    if preparedDisposition is Incomplete:
        assert atomicCommitCount == 0
        return typedIncompleteWithLastSafePoint(preparedDisposition, lastSafePoint)

    candidateCommit = preparedDisposition.commitCandidate
    assert failure/equal/worse implies
        fingerprint(candidateCommit.nextAlnsWarmStart.rngAndAdaptiveState) == oldRngAdaptive
    committed = atomicCommitExactlyOnce(candidateCommit)
    assert committed.commitCount == 1
    alnsWarmStart = committed.nextAlnsWarmStart

require completedPhase2AlnsSteps == phase2MaxSteps
assert selectorAdmissionWorkTimeWasNotCountedAsAlnsSteps
return finishWorkerRunWithSingleCommittedRecordPerHybridPhase()
```

이 의사코드는 순서와 불변조건만 고정한다. 완성 class, exception 처리나 concurrency
구현이 아니다. Admission/native/session open 전 gate 실패에는 side effect가 0이어야
한다. Session은 open 성공 시 하나만 cleanup/close하며 open/solve/cleanup/close 실패
어느 경우에도 old incumbent bytes를 바꾸지 않는다. Strict-better에서도 old object를
mutate하지 않고 fresh champion과 새 `AlnsWarmStart`를 준비한 뒤 하나의 atomic
commit으로만 공개한다.

## 10. Ordered conditional work packages

모든 WP는 현재 `WAITING_C17_AND_ALNS_BENCHMARK_GATE`다. 각 WP의 검증 명령은
해당 module/type/test가 gate-open 구현으로 실제 생성된 뒤에만 실행하는
`FUTURE` 명령이다.

Test를 추가하는 WP는 같은 변경에서 §8.4의 owner POM dependency/plugin/report
closure도 추가해야 한다. Cold repository prerequisite build와 non-zero
Surefire/Failsafe 확인이 없는 source-only test는 그 WP evidence가 아니다.

### WP13-0 — Entry receipt, scope와 drift freeze

- **목적:** 구현을 시작할 권한과 exact scope를 한 immutable receipt로 확인한다.
- **왜 먼저 하는가:** Partial approval, stale evidence 또는 classpath presence로
  backend를 열면 이후 모든 test가 unauthorized implementation을 검증하는 꼴이 된다.
- **사전조건:** Phase 06/07/08 accepted, Phase 14A accepted receipt, C-17와 모든
  owner approval, scheduler task/role separation.
- **예상 file/package/type:** Scheduler-owned disposition/skip/reject는 generic
  control plane; optional assembly의 `Phase13AuthorizedScopeVerifier`,
  `Phase13ActivationReceipt`, `ValidatedPhase13Scope`.
- **구체 행동:** Receipt schema/issuer/digest/validity/scope를 확인한다. Selected
  runtime이 substituted runtime일 때만 Phase 12 evidence를 요구한다. Baseline runtime에
  Phase 12 prerequisite를 발명하지 않는다. Frozen source/build/inventory identity를
  evidence proposal에 기록한다. Phase 13 entry/acceptance schema에는 signed
  applicability/action-time trust를 넣지 않고 `G14-SIGNING-TRUST`를 역방향
  prerequisite로 요구하지 않는다.
- **근거:** Current Architecture `§6.7`, Phase 13 original `§3.3`, `§4`, review
  F-P13-001/002/008, Phase 14 human guide `§6.4`, `§9.7`.
- **금지 shortcut:** `hybrid.enabled`, env var, ServiceLoader, OR-Tools JAR 존재,
  문서 review `PASS`, 과거 signed `PASS`로 gate 열기.
- **FUTURE 검증:**

  ```bash
  ./mvnw -f rpdptw/application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=Phase13SchedulerApplicabilityTest,Phase13Phase14SkipContractTest,Phase13ApplicabilityDependencyTest
  ```

  Optional assembly가 별도 module로 승인된 경우에만:

  ```text
  ./mvnw -f <approved-hybrid-application-pom> clean test
    -Dsurefire.failIfNoSpecifiedTests=true
    -Dtest=Phase13AuthorizedScopeVerifierTest
  ```

- **기대:** Closed path의 Phase 13 resolution/class-load/artifact/backend count 0.
  Applicable authority 하나를 제거할 때마다 exact rejection. Wrong scope/stale receipt
  backend open count 0. Phase 13 producer handoff graph의 signing/trust dependency 0,
  Phase 14B consumer wrapper에는 signed envelope/action-time verification이 필수다.
- **실패 해석:** Application test 실패는 solver 결함이 아니라 gate/assembly
  ownership 결함이다. Test 0건은 green이 아니라 command/module mismatch다.
- **Rollback:** Source 생성 전이면 `GATED` 유지. Source가 생겼다면 optional
  composition에서 제외하고 ALNS-only graph와 pre-change manifest를 보존한다.
- **Handoff:** `ValidatedPhase13Scope`, exact plan-binding requirement와
  `E-P13-GATE` candidate를 WP13-1에 전달한다.

### WP13-1 — Immutable evaluated route pool

- **목적:** ALNS acceptance와 독립적으로 재사용 가능한 exact route artifact를
  deterministic snapshot으로 만든다.
- **왜 필요한가:** Whole solution이 rejected되어도 좋은 route는 있을 수 있지만,
  mutable/stale/partial route를 모으면 selector가 corruption을 재조합한다.
- **사전조건:** WP13-0 green, accepted Phase 06 completed-trial/cache-free evaluation
  seam, explicit pool experiment policy.
- **예상 file/package/type:** `rpdptw/solver/.../pool/`,
  `EvaluatedRouteArtifact`, `RoutePoolDelta`, `RoutePoolSnapshot`,
  `RoutePoolCollector`, `RouteDominancePolicy`.
- **구체 행동:** Same-authority, nonempty, pair-complete, hard-feasible, full-evaluated
  route만 admit한다. Append/import/reload에 같은 merge rule을 쓰고 stable order,
  no-alias, incumbent pin, safe dominance 또는 conservative retention을 구현한다.
- **근거:** Master `§11.7`, Current Domain `§10.9`, `§12.5`, Phase 13 original
  `§6.2~6.4`.
- **금지 shortcut:** Scalar cost 하나로 pruning, completion/map order tie-break,
  memory pressure hidden eviction, pool route mutation, stale cache admission.
- **FUTURE 검증:**

  ```bash
  ./mvnw -f rpdptw/solver/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=RoutePoolAdmissionTest,RoutePoolMergePropertyTest,RoutePoolAliasTest,RoutePoolDominanceOracleTest,RoutePoolPinTest,RoutePoolDeterminismTest
  ```

- **기대:** 모든 permutation에서 같은 snapshot bytes/digest. Interrupted/partial/
  cross-fingerprint admission 0. Integrity conflict arbitrary winner 0. Pin loss 0.
- **실패 해석:** Digest 차이는 flaky test가 아니라 hidden ordering/identity input이다.
  Same signature/different evaluation은 duplicate가 아니라 corruption이다.
- **Rollback:** New delta/snapshot을 publish하지 않고 직전 immutable snapshot과 ALNS
  incumbent를 유지한다. Artifact를 삭제/overwrite하지 않는다.
- **Handoff:** Sealed snapshot, admission/rejection/dominance report와
  `E-P13-POOL` candidate를 WP13-2에 전달한다.

### WP13-2 — Projection capability와 exact model

- **목적:** Bound profile의 의미를 route/unassigned/concrete-vehicle integer rows로
  정확히 표현하거나 명시적으로 skip한다.
- **왜 필요한가:** Backend가 빠르고 optimal이어도 잘못 투영된 문제의 optimal answer는
  원래 RPDPTW 해의 권위가 아니다.
- **사전조건:** WP13-1 green, profile projection declaration, model-mode scope,
  coefficient unit/range와 independent oracle owner 승인.
- **예상 file/package/type:** `solver.selection.projection`,
  `RouteSelectionProjection`, `ProjectedRouteColumn`,
  `RouteSelectionModelSpec`, `MipWarmStart`.
- **구체 행동:** 각 constraint/dimension을 `ROUTE_ADDITIVE`,
  `UNASSIGNED_ADDITIVE`, `EXACT_LINEARIZATION`, `NON_PROJECTABLE`로 분류한다.
  Request/unassigned/concrete-vehicle rows와 checked int64 coefficient proof를 만들고,
  staged lexicographic rule과 warm-start feasibility를 고정한다.
- **근거:** Current Domain `§12.5~12.6`, Master `§11.8`, Integrated
  `§17.4~17.6`.
- **금지 shortcut:** Hidden Big-M, lossy double→long, mandatory 자동 hard assignment,
  vehicle-class aggregation proof 생략, `FEASIBLE_LIMIT` 뒤 하위 dimension solve.
- **FUTURE 검증:**

  ```bash
  ./mvnw -f rpdptw/solver/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=RouteSelectionProjectionTest,LexicographicSelectionTest,WarmStartContractTest,TinyExactRouteSelectionOracleTest
  ```

- **기대:** Tiny exhaustive oracle의 rows/vector/optimum과 exact 일치.
  Non-projectable은 typed skip. Overflow/lossy coefficient에서 backend call 0.
- **실패 해석:** Oracle 불일치는 CP-SAT tuning 문제가 아니라 projection/model defect다.
  Unsupported dimension은 feature downgrade가 아니라 scope skip이다.
- **Rollback:** 해당 profile/scope를 `SKIPPED_NON_PROJECTABLE_PROFILE`로 남기고 model을
  seal하지 않는다. ALNS-only incumbent는 그대로다.
- **Handoff:** Frozen model/warm-start contract, coefficient proof와
  `E-P13-SELECTION` partial candidate를 WP13-3에 전달한다.

### WP13-3 — Backend-neutral API와 deterministic fake

- **목적:** Vendor 없이 session/outcome/status/cancel contract를 검증한다.
- **왜 필요한가:** Domain/model 결함과 JNI/backend lifecycle 결함을 분리해야 한다.
- **사전조건:** WP13-2 green, route-selection authority matrix 승인.
- **예상 file/package/type:** `solver.selection.api`,
  `RouteSelectionSolverFactory`, `RouteSelectionSession`,
  `RouteSelectionOutcome`, test-only deterministic fake.
- **구체 행동:** Status × incumbent admissibility, selected ID only, explicit budget,
  cancellation, termination cause, work accounting을 구현한다. Fake는 tiny oracle가
  지정한 selected IDs만 반환한다. Solver owner POM에 JUnit/test-fixtures/Surefire
  non-zero closure를 같은 변경으로 추가하고 session open/solve/cleanup/close
  exactly-once fault matrix를 실행한다.
- **근거:** Current Architecture `§6.7`, `§18~19`, Phase 13 original `§7.3~7.4`.
- **금지 shortcut:** Fake를 production backend로 등록, raw objective/bound 노출,
  no-incumbent status에서 getter 호출, empty selection을 success로 합성.
- **FUTURE 검증:**

  ```bash
  ./mvnw -f rpdptw/solver/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=SelectionOutcomeContractTest,TinyExactRouteSelectionOracleTest,BackendLifecycleTest
  ```

- **기대:** Generic code의 OR-Tools ref 0. Unsafe attribute read 0. Fake result와
  exhaustive optimum/stable IDs exact 일치. Open/solve/close 정상 count `1/1/1`;
  open/solve/cleanup failure에서 commit 0이고 old incumbent bytes는 같다.
- **실패 해석:** Fake와 oracle가 같은 helper를 쓰면 일치해도 false-green이다.
  Cancellation cleanup 실패는 session lifecycle defect다.
- **Rollback:** Vendor-neutral API draft만 보존하고 production capability registration은
  하지 않는다.
- **Handoff:** Reviewed neutral API/outcome/fake evidence를 WP13-4와 WP13-5에 전달한다.

### WP13-4 — Approved OR-Tools direct CP-SAT adapter

- **목적:** 승인된 exact backend를 optional isolated module에 구현한다.
- **왜 필요한가:** CP-SAT Java/native lifecycle, status와 cancellation은 neutral fake로
  완전히 검증할 수 없다.
- **사전조건:** WP13-3 green, exact artifact/version/checksum/config/platform,
  Apache-2.0와 applicable notice/SBOM/CVE/native redistribution, security, operations,
  cost/capacity approval.
- **예상 file/package/type:** `adapters/route-selection-ortools-cpsat`,
  adapter-internal `CpModel`, `BoolVar`, `LinearExpr`, `CpSolver`,
  `CpSolverStatus`, `SatParameters`.
- **구체 행동:** Checked integer model, hint, solve/`stopSearch`, status × incumbent,
  selected ID extraction, process-wide `Loader.loadNativeLibraries()` smoke test,
  callback/reference/temp-resource cleanup을 구현한다. Approved config 값을 exact bind한다.
  Adapter POM에는 OR-Tools와 Failsafe `integration-test`/`verify`, non-zero `*IT`
  policy를 넣고 approved profile에서만 module을 등록한다. 현재 directory-absence
  guard는 §8.4의 default-negative/profile-positive guard로 versioned transition한다.
- **근거:** C-17, Current Architecture `§5`, `§6.7`, `§18~19`, Integrated
  `§17.9`, Phase 13 original `§7.3`.
- **금지 shortcut:** `MPSolver`, latest version, root dependency, library defaults,
  all-platform 가정, commercial license lease abstraction, per-solve native unload 흉내.
- **FUTURE 검증:** Exact profile/module 이름은 approval record에 freeze한다.

  ```text
  ./mvnw -Dmaven.repo.local=<fresh-isolated-repository>
    -P<approved-phase13-ortools-integration-profile>
    -pl adapters/route-selection-ortools-cpsat -am verify
    -Dfailsafe.failIfNoSpecifiedTests=true
    -Dit.test=CpSatAdapterNativeIT
  ```

- **기대:** Default ALNS-only root verify는 OR-Tools/native 없이 green. Default
  effective reactor/dependency/service/class/native count 0. Isolated adapter profile은
  exact provider 1개와 tiny oracle/status/cancel/native smoke, non-zero `*IT`,
  notice/SBOM/platform matrix green.
- **실패 해석:** JNI load 실패는 `BACKEND_UNAVAILABLE`이 아니라
  `NATIVE_RUNTIME_UNAVAILABLE`이다. Unsupported platform은 fake success가 아니다.
- **Rollback:** Optional backend composition을 비선택 상태로 되돌리고 accepted
  ALNS-only distribution을 사용한다. Dependency와 artifact를 generic module로 옮기지 않는다.
- **Handoff:** Approved backend capability/config fingerprint와 integration evidence를
  WP13-5/7에 전달한다.

### WP13-5 — Fresh materialization, full evaluation과 strict adoption

- **목적:** Selected IDs를 실제 RPDPTW solution 후보로 안전하게 재구성한다.
- **왜 필요한가:** Backend model은 projection일 뿐 full domain feasibility/objective의
  authority가 아니다.
- **사전조건:** WP13-2/3 green, approved backend 또는 explicit test-only fake,
  accepted Phase 03/04 full-solution evaluator/comparator 계약.
- **예상 file/package/type:** `solver.selection.conversion`,
  application materializer/adopter, `MaterializationRecord`,
  `EvaluatedSelectionCandidate`.
- **구체 행동:** Model/pool membership을 확인하고 selected artifact를 fresh copy한다.
  Bank를 exact coverage complement로 만들고 request/vehicle partition, propagation,
  evaluation과 comparator를 실행한다. Better만 fresh champion과 새
  `AlnsWarmStart` candidate를 준비한다. Strict-better에서도 old ALNS incumbent
  object/bytes는 mutate하지 않고 atomic commit 전까지 비공개다.
- **근거:** Current Domain `§12.6~12.8`, `§13.1~13.4`, Current Architecture
  `§6.7`, `§11.2`, review F-P13-005.
- **금지 shortcut:** Pool alias, backend `u_i` side channel 권위화, raw objective 비교,
  stale selected ID materialization, equal/worse diversification 채택.
- **FUTURE 검증:**

  ```text
  ./mvnw -f <approved-hybrid-application-pom> clean test
    -Dsurefire.failIfNoSpecifiedTests=true
    -Dtest=RouteSelectionMaterializationTest,HybridAdoptionTest,HybridPhase07BoundaryTest
  ```

- **기대:** Pool mutation/alias 0, stale ID draft 0, equal/worse/invalid adopt 0,
  strict-better에서도 old incumbent byte mutation 0, fresh champion/next
  `AlnsWarmStart`만 single commit, backend-objective authority 0, Phase 07 bypass 0.
- **실패 해석:** Full-evaluation divergence는 “backend approximation” 정상 결과가
  아니라 integrity/model defect다. Upstream comparator가 unresolved면 WP는 blocked다.
- **Rollback:** Draft와 outcome을 폐기하고 exact pre-incumbent fingerprint를 유지한다.
  Partial draft를 next warm start로 넘기지 않는다.
- **Handoff:** Evaluated candidate 또는 typed failure, adoption record와 Phase 07
  compatibility report를 WP13-6에 전달한다.

### WP13-6 — Hybrid lifecycle, fallback, retry와 reproducibility

- **목적:** ALNS→pool→selection→evaluation→adoption을 하나의 atomic hybrid commit으로
  묶는다.
- **왜 필요한가:** 각 component가 맞아도 failure 중간에 state가 새면 incumbent,
  pool과 warm start가 서로 다른 logical run을 가리킬 수 있다.
- **사전조건:** WP13-1~5 green, approved optional/required policy, cadence, budget,
  reproducibility class.
- **예상 file/package/type:** Approved application/hybrid owner,
  `HybridRouteSelectionOrchestrator`, `HybridPhaseRecord`,
  `HybridExecutionResult`.
- **구체 행동:** Commit boundary, optional exact fallback, required incomplete,
  pre/post optimize retry, cancellation, next warm start와 stable lineage를 구현한다.
  `ALNS step < segment < HybridPhase < WorkerRun < ExecutionRound` identity와
  `Σ completed segment steps == phase2MaxSteps`를 봉인하고 selector
  admission/work/time을 별도 account한다. `MipWarmStart`는 selector-local로 끝내고
  실패/equal/worse에는 incumbent·RNG·adaptive state·next `AlnsWarmStart`를 그대로
  둔다. Strict-better만 fresh champion에서 새 `AlnsWarmStart`를 만든다.
- **근거:** Master `§11.9~11.10`, Current Domain `§12.6~12.8`, `§15~16`, Current
  Architecture `§11.2`, `§14`, `§17`, Integrated `§17.10`.
- **금지 shortcut:** Post-optimize same-run retry, required failure를 degraded success로
  변환, timeboxed multi-worker에 strong replay, failure 뒤 pool/incumbent 오염.
- **FUTURE 검증:**

  ```text
  ./mvnw -f <approved-hybrid-application-pom> clean test
    -Dsurefire.failIfNoSpecifiedTests=true
    -Dtest=HybridFallbackTest,HybridRetryTest,HybridReproducibilityTest,HybridRollbackTest,HybridStepBudgetTest,HybridWarmStartLifecycleTest,HybridAtomicCommitTest,BackendLifecycleTest
  ```

- **기대:** 모든 injected failure 전후 incumbent/pool/RNG/adaptive/
  next-`AlnsWarmStart` byte equality. Normal run의 ALNS step 합 exact, selector work
  혼입 0, session/commit count 불변조건 exact. Required degraded success 0. Fixed
  strong envelope exact repeat 또는 weaker class 명시.
- **실패 해석:** Nondeterminism은 seed 하나를 추가해 숨길 일이 아니라 ordering,
  backend config와 run identity를 재감사할 신호다.
- **Rollback:** Activated plan을 중단하고 last accepted ALNS-only manifest/pointer로
  돌아간다. 실패 artifact는 immutable incident evidence로 보존한다.
- **Handoff:** `HybridPhaseRecord`, fallback/repro report와 `E-P13-HYBRID` candidate를
  WP13-7에 전달한다.

### WP13-7 — Security, license/SBOM, operations, performance와 cost

- **목적:** Correct algorithm이 실제 runtime에서 tenant, native supply chain,
  capacity와 cost 경계를 지키는지 검증한다.
- **왜 필요한가:** Native load, temp extraction, CPU/memory pressure와 cross-tenant
  artifact access는 unit model test로 잡히지 않는다.
- **사전조건:** WP13-4/6 green, isolated approved environment와 explicit experiment
  manifest. Threshold는 승인된 실험값만 사용한다.
- **예상 file/package/type:** Security contract tests, runtime admission/cancel/
  recovery runbook, work/memory/cost telemetry와 evidence manifest.
- **구체 행동:** Cross-tenant denial, secret redaction, least privilege, native artifact
  checksum/SBOM/CVE, admission exhaustion, cancel/timeout/temp cleanup, pool growth,
  stage별 CPU/memory/latency/fallback/cost를 측정한다.
- **근거:** Current Architecture `§16~18`, Integrated `§19~22`, Phase 13 original
  `WP-13.7`.
- **금지 shortcut:** “Apache-2.0이므로 review 불필요”, 측정 없는 threshold,
  unapproved traffic, raw PII/secret logging, provider locator를 result identity로 사용.
- **FUTURE 검증:**

  ```text
  ./mvnw -f <approved-hybrid-application-pom> clean test
    -Dsurefire.failIfNoSpecifiedTests=true
    -Dtest=RoutePoolWorkAccountingTest,RouteSelectionWorkAccountingTest,HybridExperimentManifestTest,HybridCostAuthorityTest,HybridTrafficAuthorityTest

  ./mvnw -f build/architecture-rules/pom.xml clean test
    -Dsurefire.failIfNoSpecifiedTests=true
    -Dtest=HybridSecurityContractTest,HybridArchitectureTest
  ```

- **기대:** Unauthorized backend open 0, secret/PII match 0, resource/reference leak 0,
  counters complete, hidden threshold/traffic default 0.
- **실패 해석:** 성능 threshold가 없을 때 test pass는 “빠르다”가 아니라 “측정이
  완전하고 숨은 정책이 없다”는 뜻이다.
- **Rollback:** Production recommendation을 만들지 않고 ALNS-only plan을 유지한다.
  Experiment 결과를 official evidence로 이름 바꾸지 않는다.
- **Handoff:** `E-P13-SECURITY-LICENSE-OPS-COST`와 owner별 go/no-go verdict를
  WP13-8에 전달한다.

### WP13-8 — Shadow, rollback, evidence seal과 Phase 14B handoff

- **목적:** Same-authority ALNS-only baseline과 hybrid를 비교하고 완전한 evidence
  DAG와 rollback을 검증한다.
- **왜 필요한가:** 한 fixture의 개선이나 backend 성공은 가치·재현성·운영 안전을
  증명하지 못한다.
- **사전조건:** WP13-0~7 green, required test failed/error/skipped 0, independent
  implementation reviewer와 evidence acceptance authority 지정.
- **예상 file/package/type:** `Phase13EvidenceManifest`,
  `Phase13ActivatedHandoff`, shadow card, rollback record. Gate-closed Phase 13
  `Skip` control record는 scheduler-owned이고 Phase 13 evidence가 아니다.
  Gate-open signing/trust wrapper는 Phase 14B owner다.
- **구체 행동:** Same corpus/case/seed applicability에서 ALNS-only와 hybrid의
  algorithm/backend 차이를 preregister하고 quality, memory, native, cost, fallback,
  reproducibility를 비교한다. Rollback을 rehearsal하고 pre-review manifest →
  review → acceptance receipt를 단방향으로 seal한다. Accepted Phase 13 producer
  handoff에는 signed envelope/action-time verdict를 넣지 않는다. Phase 14B가 이를
  별도 `Phase13ApplicabilityReceipt.Activated`로 wrap한다.
- **근거:** Phase 13 original `§11~14`, Phase 14 original `§5.3`, `§13~16`,
  Phase 14 human guide `§6.4`, `§9.7`.
- **금지 shortcut:** Best-run cherry-pick, mixed manifest 합성, incomplete bundle을
  `ACCEPTED`로 표시, review 결과를 pre-review manifest에 backfill.
- **FUTURE 검증:**

  ```bash
  ./mvnw clean verify
  ```

  ```text
  ./mvnw -f <approved-hybrid-application-pom> clean test
    -Dsurefire.failIfNoSpecifiedTests=true
    -Dtest=HybridPhase07BoundaryTest,HybridRollbackTest,Phase13ApplicabilityDependencyTest
  ```

- **기대:** Default ALNS-only root build green, same-authority shadow, Phase 07
  both-gate bypass 0, Phase 13 → Phase 14B trust reverse edge 0, exact rollback,
  complete `E-P13-*`, independent review `PASS`.
- **실패 해석:** Bundle 일부가 없으면 최대 `IMPLEMENTED_PENDING_EVIDENCE`다.
  Backend success와 production readiness는 별개다.
- **Rollback:** Activated composition/traffic proposal을 철회하고 accepted ALNS-only
  last-safe point로 복귀한다.
- **Handoff:** Gate-open/accepted일 때만 signing/trust가 없는
  `Phase13ActivatedHandoff` producer payload를 Phase 14B에 전달한다. Closed path는
  scheduler-owned signed `Phase13ApplicabilityReceipt.Skip` control record를
  사용한다. Open path의 signed applicability와 action-time verification만
  Phase 14B consumer `Activated` wrapper에서 붙는다.

## 11. Test-first 전략, fixture, builder, oracle와 false-green 방지

### 11.1 Fixture와 builder

| Fixture | Builder가 명시할 것 | 독립 expected result |
|---|---|---|
| `P13_GATE_CLOSED_TEST_ONLY` | Request 없음, gate closed | Phase 13 load/artifact 0 |
| `P13_UNAUTHORIZED_REQUEST_TEST_ONLY` | Explicit request, receipt 없음 | Pre-allocation rejection |
| `P13_PARTIAL_RECEIPT_TEST_ONLY` | Cost 또는 applicable Phase 12 ref 하나 누락 | Exact missing list |
| `P13_STALE_RECEIPT_TEST_ONLY` | Profile/scope fingerprint 변경 | Backend open 0 |
| `P13_TINY_EXACT_3R_2V_TEST_ONLY` | 3 requests, 2 vehicles, explicit route columns | Exhaustive subset optimum |
| `P13_NON_PROJECTABLE_TEST_ONLY` | Nonadditive solution hard rule | Typed skip |
| `P13_PARTIAL_CANDIDATE_TEST_ONLY` | Pickup-only/duplicate/interrupted | Pool rejection |
| `P13_STALE_POOL_TEST_ONLY` | Same IDs, changed profile/travel | Integrity rejection |
| `P13_COLUMN_DIVERGENCE_TEST_ONLY` | Coefficient digest mismatch | Model rejection |
| `P13_STALE_SELECTED_ID_TEST_ONLY` | Outcome ID not in frozen model | Draft count 0 |
| `P13_ALIAS_ATTACK_TEST_ONLY` | Converter attempts pool mutation | Exact unchanged bytes |
| `P13_NATIVE_LOAD_FAILURE_TEST_ONLY` | Missing/corrupt native resource | Native unavailable |
| `P13_SELECTION_TIE_TEST_ONLY` | Equal objective combinations | Stable approved tie or weaker replay |
| `P13_MATERIALIZATION_DIVERGENCE_TEST_ONLY` | Backend claim vs full evaluator mismatch | Candidate rejected |
| `P13_TIMEBOXED_POST_OPTIMIZE_RETRY_TEST_ONLY` | Optimize started then timebox | New logical run required |
| `P13_ALNS_STEP_SUM_UNDER_OVER_TEST_ONLY` | Segment 합이 `phase2MaxSteps`보다 작거나 큼 | Normal completion 거부 |
| `P13_SELECTOR_WORK_AS_STEP_TEST_ONLY` | Selector admission/work/time을 ALNS step에 혼입 | Accounting rejection |
| `P13_MIP_AS_ALNS_WARM_START_TEST_ONLY` | Model-scoped MIP hint를 다음 ALNS segment에 전달 | Type/lifecycle rejection |
| `P13_STRICT_BETTER_OLD_ALIAS_TEST_ONLY` | Fresh champion 준비 중 old incumbent mutate 시도 | Old bytes exact, commit 0 |
| `P13_SESSION_OPEN_FAILURE_TEST_ONLY` | `openSession` throws | Solve/cleanup/commit 0 |
| `P13_SESSION_SOLVE_FAILURE_TEST_ONLY` | Solve throws after open | Cleanup/close 1, commit 0 |
| `P13_SESSION_CLEANUP_FAILURE_TEST_ONLY` | Callback cleanup 또는 close fault | Typed incomplete, old bytes exact |
| `P13_CROSS_TENANT_TEST_ONLY` | Wrong tenant scope | Deny before backend open |
| `P13_UNSIGNED_STALE_SKIP_TEST_ONLY` | Unsigned/expired/revoked/wrong scope | Phase 14B action 0 |

Builder는 production class의 private constructor를 reflection으로 우회해 impossible
state를 “정상 fixture”로 만들지 않는다. Corruption fixture는 bytes/field를 명시적으로
손상시키고 어느 validation boundary에서 거부돼야 하는지 기록한다.

### 11.2 Independent oracle

`TinyExactRouteSelectionOracle`은 다음을 지킨다.

1. Production projector, model builder, backend, comparator helper를 호출하지 않는다.
2. Stable route artifact subset을 전수 열거한다.
3. Request exact partition과 concrete vehicle consumption을 손으로 검사한다.
4. Explicit unassigned variables를 포함한다.
5. Hand-authored lexicographic vector와 stable tie order를 사용한다.
6. Model/backend selected IDs와 full materialization 결과를 별도로 비교한다.

Oracle가 production coefficient encoder를 재사용하면 같은 bug를 공유한다. 이 경우
green은 독립 evidence가 아니다.

### 11.3 Exact test class/method 후보

| Class | Method | 주 검출 결함 |
|---|---|---|
| `Phase13SchedulerApplicabilityTest` | `closedGateWithoutRequestUsesCanonicalAlnsPathWithoutLoadingPhase13` | Closed path class-load |
| 같은 class | `closedGateExplicitRequestRejectsBeforePhase13Resolution` | Silent fallback |
| `Phase13AuthorizedScopeVerifierTest` | `allApplicableAuthoritiesAndEvidenceAreConjunctive` | Partial receipt acceptance |
| 같은 class | `selectedSubstitutedRuntimeRequiresApplicablePhase12Evidence` | Missing conditional evidence |
| 같은 class | `baselineRuntimeDoesNotInventPhase12Prerequisite` | Universal Phase 12 premise |
| `Phase13Phase14SkipContractTest` | `unsignedSkipCannotAuthorizePhase14Action` | Unsigned authority |
| 같은 class | `pastPassCannotBypassCurrentRevocationOrFreshnessPolicy` | Stale trust |
| `RoutePoolAdmissionTest` | `rejectsInterruptedPartialAndStaleCandidate` | Invalid pool admission |
| 같은 class | `sameSignatureDifferentEvaluationIsIntegrityFailure` | Arbitrary duplicate winner |
| `RoutePoolMergePropertyTest` | `appendImportReloadProduceSameSnapshotDigestForEveryOrder` | Hidden ordering |
| `RoutePoolAliasTest` | `snapshotAndIncumbentRemainByteIdenticalAfterConversionFault` | Mutable alias |
| `RoutePoolDominanceOracleTest` | `unsafeScalarDominanceNeverDropsParetoRoute` | Unsafe pruning |
| `RouteSelectionProjectionTest` | `nonProjectableDimensionReturnsTypedSkipWithoutSurrogate` | Hidden approximation |
| 같은 class | `coefficientOverflowOrLossyRoundTripIsRejected` | Numeric corruption |
| `TinyExactRouteSelectionOracleTest` | `backendOutcomeMatchesExhaustiveOptimumAndStableIds` | Model/backend mismatch |
| `LexicographicSelectionTest` | `feasibleLimitDoesNotOptimizeLowerPriorityDimension` | Priority violation |
| `SelectionOutcomeContractTest` | `noIncumbentStatusesNeverReadSelectedOrObjectiveAttributes` | Unsafe getter |
| 같은 class | `staleSelectedIdIsRejectedBeforeMaterialization` | Stale outcome |
| `CpSatAdapterContractTest` | `usesDirectSatApiWithoutMPSolver` | Backend policy drift |
| 같은 class | `onlyOptimalOrFeasibleReadBooleanValues` | Status misuse |
| `BackendLifecycleTest` | `alnsOnlyAssemblyNeverLoadsNativeLibraries` | Default contamination |
| 같은 class | `cancellationCallsStopSearchAndClearsSolveReferences` | Callback/reference leak |
| 같은 class | `normalPathOpensSolvesCleansAndClosesExactlyOnce` | Duplicate/missing lifecycle call |
| 같은 class | `openFailureNeverSolvesCleansClosesOrCommits` | Unopened session cleanup |
| 같은 class | `solveFailureCleansAndClosesExactlyOnceWithoutCommit` | Exceptional lifecycle leak |
| 같은 class | `cleanupOrCloseFailurePreservesOldIncumbentBytesAndPreventsCommit` | Contaminated commit |
| `RouteSelectionMaterializationTest` | `selectedArtifactsAreClonedIntoFreshExactPartition` | Alias/partition |
| 같은 class | `unassignedSetIsRebuiltAsSelectedCoverageComplement` | Backend side-channel |
| `HybridAdoptionTest` | `strictlyBetterCandidateIsOnlyAdoptableCandidate` | Equal/worse adoption |
| 같은 class | `rawBackendObjectiveNeverOverridesFullComparator` | Authority bypass |
| `HybridFallbackTest` | `optionalNoIncumbentPreservesIncumbentAndNextWarmStartFingerprint` | Fallback contamination |
| 같은 class | `requiredBackendFailureIsIncompleteNotDegradedSuccess` | Status conflation |
| `HybridStepBudgetTest` | `completedAlnsSegmentStepsSumExactlyToPhase2MaxSteps` | Under/over ALNS budget |
| 같은 class | `selectorAdmissionWorkAndTimeNeverCountAsAlnsSteps` | Budget-domain mixing |
| `HybridWarmStartLifecycleTest` | `mipWarmStartCannotBecomeNextAlnsWarmStart` | Warm-start conflation |
| 같은 class | `failureEqualAndWorsePreserveIncumbentRngAdaptiveAndNextWarmStartBytes` | Continuation drift |
| 같은 class | `strictBetterCreatesFreshChampionAndAlnsWarmStartWithoutMutatingOldIncumbent` | Old-state alias |
| `HybridAtomicCommitTest` | `eachHybridPhasePublishesExactlyOneCompleteCommitOrNone` | Partial/double commit |
| `HybridRetryTest` | `timeboxedOptimizeStartForbidsSameLogicalResultBearingRetry` | Duplicate logical run |
| `HybridReproducibilityTest` | `fixedStrongEnvelopeRepeatsPoolModelOutcomeDecisionAndCandidateFingerprints` | Replay drift |
| `HybridPhase07BoundaryTest` | `adoptedCandidateStillRequiresCandidateAndResultVerifierPass` | Publication bypass |
| `HybridSecurityContractTest` | `crossTenantPoolReadIsDeniedBeforeBackendOpen` | Tenant leakage |
| `HybridArchitectureTest` | `defaultReactorHasNoOrToolsDependencyOrServiceDiscoveryActivation` | Vendor leakage |
| 같은 class | `approvedProfileResolvesExactlyOneOptionalAdapterAndRunsNativeIt` | Positive profile not wired |
| 같은 class | `defaultGuardDoesNotRequireOptionalSourceDirectoryAbsence` | Obsolete guard contract |
| `Phase13ApplicabilityDependencyTest` | `phase13HandoffHasNoSigningTrustOrActionTimeDependency` | Phase 14B authority mixed into producer |
| 같은 class | `phase14ActivatedWrapperRequiresHandoffEnvelopeAndActionTimeVerification` | Consumer trust bypass |

### 11.4 Red → green 순서

| 순서 | Red | Green |
|---:|---|---|
| 1 | Closed gate인데 Phase 13가 resolve/load됨 | Absence 0 + explicit request fail closed |
| 2 | Partial/stale receipt 통과 | 모든 applicable authority conjunctive |
| 3 | Invalid route가 pool에 들어감 | Admission/identity fail closed |
| 4 | Merge 순서에 따라 digest가 바뀜 | Stable exact snapshot |
| 5 | Non-projectable 의미가 사라짐 | Typed skip, surrogate 0 |
| 6 | Tiny model이 exhaustive oracle와 다름 | Exact rows/vector/IDs |
| 7 | No-incumbent status에서 value를 읽음 | Status×incumbent safe matrix |
| 8 | Selected route가 pool을 mutate함 | Fresh copy + exact partition |
| 9 | Backend objective가 comparator를 이김 | Full evaluation + strict adoption |
| 10 | Segment 합/selector accounting이 phase2 budget을 왜곡 | Exact ALNS sum + separate selector counters |
| 11 | Failure/equal/worse가 incumbent 또는 RNG/adaptive/warm start를 바꿈 | Byte-exact continuation |
| 12 | Session open/solve/cleanup fault가 leak/partial commit 생성 | Exactly-once lifecycle + commit 0 |
| 13 | Strict-better가 old incumbent를 mutate | Fresh champion/warm start + single atomic commit |
| 14 | Fixed envelope가 달라지는데 strong claim | Exact repeat 또는 weaker class |
| 15 | Vendor/secret/tenant/cost authority가 샘 | Architecture/security/cost green |
| 16 | Phase 07 또는 Phase 14 gate를 우회 | Two-gate + consumer-owned trust wrapper |
| 17 | Evidence 일부만 있는데 accepted | Complete bundle + independent review/receipt |

### 11.5 False-green 방지

- `-Dtest` 이름이 틀렸는데 0 test로 exit 0인 결과를 받지 않는다.
- Selected command는 `-Dsurefire.failIfNoSpecifiedTests=true`를 사용한다.
- Selected `*IT` command는 Failsafe `verify`,
  `-Dfailsafe.failIfNoSpecifiedTests=true`와 exact `-Dit.test`를 사용한다.
- Fresh Surefire XML에서 expected class/method가 각각 1회 이상 실행됐는지 대조한다.
- Fresh Failsafe XML도 expected integration class/method가 각각 non-zero인지 대조한다.
- Required test의 failure/error/skipped는 모두 0이어야 한다.
- 기존 root test나 legacy characterization 성공을 Phase 13 evidence로 쓰지 않는다.
- Deterministic fake 성공을 OR-Tools/native integration evidence로 쓰지 않는다.
- OR-Tools adapter test 성공을 full materialization/Phase 07 evidence로 쓰지 않는다.
- Production builder와 oracle가 같은 coefficient/comparator helper를 공유하지 않는다.
- Test-only seed, time, route cap을 production default로 복사하지 않는다.
- Pre/post equality는 object reference가 아니라 canonical bytes/fingerprint로 확인한다.
- Status enum만 검사하지 말고 forbidden getter/backend-open/materialization call count를
  함께 검사한다.
- One fast fixture improvement를 shadow value/production recommendation으로 쓰지 않는다.
- 현재 live uncommitted scaffold의 test 결과를 accepted source commit evidence로 쓰지 않는다.

### 11.6 Test 종류별 적용성, pass 판정과 미적용 이유

| Test 종류 | Phase 13 적용 | Pass 판정 | 현재 미실행 이유 |
|---|---|---|---|
| Unit | 필수 | Admission/status/materialization의 exact expected | Source/type가 없고 gate closed |
| Property | 필수 | Merge permutation, pair/partition, no-alias 불변 | Pool 구현 없음 |
| Contract | 필수 | Gate, projection, session/outcome, Phase 07 seam | Receipt/API 미승인 |
| Integration | Gate-open 뒤 필수 | Isolated CP-SAT/native/status/oracle/cancel | Adapter/version/platform 미승인 |
| E2E | Gate-open 뒤 필수 | ALNS→pool→selection→Phase 07 both-gate | Phase 06~08/14A unaccepted |
| Architecture | 필수 | Generic vendor ref 0, verifier reverse edge 0 | 현재 negative Phase 00 scaffold만 존재 |
| Fault | 필수 | Cancel/native/model/timeout 뒤 exact incumbent | Hybrid source 없음 |
| Corruption | 필수 | Stale ID/fingerprint/alias/cross-tenant fail closed | Artifact types 없음 |
| Reproducibility | 필수 | Fixed envelope exact trace 또는 weaker class | Backend config/open values 미승인 |
| Security | Gate-open 뒤 필수 | Tenant/secret/native supply-chain negative matrix | Isolated environment/authority 없음 |
| Performance | 실험 단계 필수 | Counters/measurement complete, hidden threshold 0 | Go-live 수치 `OPEN — EXPERIMENT_REQUIRED` |
| Cost | 실험 단계 필수 | Approved budget 없으면 backend open 0 | Cost authority 미승인 |
| Provider parity | 조건부 | Selected substituted runtime이면 Phase 12 accepted evidence | Runtime 선택 없음; 보편 prerequisite 아님 |
| Public API compatibility | 현재 미적용 | 별도 versioned public contract가 승인될 때 정의 | Phase 13 types는 internal proposed |
| Production canary | Phase 13 exit에 미적용 | Phase 14B authority가 소유 | Production activation은 Phase 13 비범위 |

### 11.7 Maven 명령 — 현재와 미래를 구분하기

현재 inventory에서 실제로 존재하는 owner POM은 다음과 같다.

```text
pom.xml
rpdptw/solver/pom.xml
rpdptw/application/pom.xml
rpdptw/verification/pom.xml
build/architecture-rules/pom.xml
```

하지만 Phase 13 source/test는 0이므로 지금 `./mvnw clean verify`가 성공해도
`E-P13-*`는 생성되지 않는다. 현재 root verify는 concurrent Phase 00 scaffold와
legacy characterization을 검증할 수 있을 뿐이다.

Live POM inspection에서 solver/application/verification은 JUnit과 test-fixtures
test-jar가 없고, root의 Surefire는 `failIfNoTests=false`이며 Failsafe
plugin/execution은 없다. Optional hybrid application/adapter POM/profile도 없다.
그러므로 미래 source/test를 쓰는 WP가 다음 POM closure를 같은 patch로 만들지 않으면
compile/discovery/`*IT` execution evidence가 성립하지 않는다.

Gate-open 검증 순서는 exact 승인 이름을 대입한 아래 skeleton으로 고정한다.

```text
# 1. Fresh/isolated local repository에서 exact prerequisite closure
./mvnw -Dmaven.repo.local=<fresh-isolated-repository>
  -P<approved-profile-if-needed>
  -pl <owner-module>,<exact-prerequisite-modules> -am
  clean install -DskipTests

# 2. Owner selected unit/contract tests; report count non-zero
./mvnw -Dmaven.repo.local=<same-fresh-isolated-repository>
  -f <owner-pom> test
  -Dsurefire.failIfNoSpecifiedTests=true
  -Dtest=<exact-required-test-list>

# 3. Optional adapter integration; Failsafe owns *IT
./mvnw -Dmaven.repo.local=<same-fresh-isolated-repository>
  -P<approved-phase13-integration-profile>
  -pl <approved-adapter-module> -am verify
  -Dfailsafe.failIfNoSpecifiedTests=true
  -Dit.test=<exact-required-it-list>

# 4. Default negative architecture closure without optional profile
./mvnw -Dmaven.repo.local=<fresh-default-repository>
  clean verify
```

각 command 후 fresh Surefire/Failsafe report의 expected class/method count,
tests/failures/errors/skipped를 manifest와 대조한다. Warm local repository에서 naked
owner `-f`만 성공한 결과, `*IT`를 Surefire로 실행한 결과와 zero-test success는
evidence가 아니다. `rpdptw/hybrid-application/pom.xml`, adapter POM/profile은 현재
없으므로 예시 path를 지금 실행해 실패하는 것도 Phase 13 red evidence가 아니다.

## 12. 사람 checkpoint, evidence, stop과 resume

### 12.1 Checkpoint

| Checkpoint | 사람이 확인할 것 | 통과 신호 | 실패 시 stop |
|---|---|---|---|
| CP0 Entry | Phase 06/07/08 + 14A receipt + C-17 + owner approvals | Immutable conjunctive receipt | Source/POM 생성 전 |
| CP1 Pool | Admission/identity/merge/no-alias/dominance | `E-P13-POOL` candidate | Model/backend 시작 전 |
| CP2 Projection | Exact rows/range/oracle/`MipWarmStart` scope | Tiny oracle exact | Adapter 시작 전 |
| CP3 Backend | Version/checksum/native/SBOM/status/cancel/session lifecycle | Isolated non-zero integration green | Composition/traffic 전 |
| CP4 Adoption | Fresh full evaluation/strictly-better/`AlnsWarmStart`/Phase 07 | Bypass 0, old incumbent bytes exact | Hybrid commit 전 |
| CP5 Operations | Security/admission/cost/performance/rollback | Owner verdict complete | Shadow recommendation 전 |
| CP6 Evidence | Full tests, immutable manifest, independent review | Acceptance receipt valid | Phase 14B handoff 전 |
| CP7 Production | Phase 14B official/production gates | Separate authority | Traffic/pointer action 전 |

현재는 CP0에서 멈춘다.

### 12.2 Evidence DAG

```text
implementation + exact test artifacts
→ Phase13PreReviewEvidenceManifest [digest M]
→ independent Phase13 review [references M, digest R]
→ post-review acceptance receipt [references M + R]
→ scheduler ACCEPTED for exact approved scope
→ Phase13ActivatedHandoff
```

Pre-review manifest는 reviewer, review result/reference 또는 acceptance receipt를
미리 포함하지 않는다. Review 뒤 manifest를 backfill하지 않는다.

Gate-closed branch는 이 DAG를 만들지 않고 scheduler/control-plane 소유의 signed
`Skip`을 사용한다. Gate-open accepted Phase 13 producer handoff에는 Phase 14B
signing/trust evidence를 역혼합하지 않는다. Phase 14B consumer만 다음 wrapper를
만든다.

```text
Phase13ApplicabilityReceipt.Activated(
  phase14aAcceptanceReceiptDigest,
  phase13AcceptedHandoffDigest,
  signedApplicabilityEnvelope,
  actionTimeVerificationReceiptDigest,
  lastAcceptedAlnsOnlyRollbackPointDigest)
```

이 wrapper는 Phase 14B action-time trust/validity/revocation/freshness를 통과한 뒤에만
소비한다. Scheduler-signed `Skip`에도 같은 현재 검증이 필요하지만 `E-P13-*`나
Phase 13 acceptance는 아니다.

### 12.3 Evidence manifest 최소 항목

- Source commit과 clean/dirty scope
- Problem/travel/profile/config/build/runtime fingerprint
- Activation receipt와 exact allowed scope
- Pool policy/delta/snapshot/pin/dominance identities
- ALNS step/segment/HybridPhase/WorkerRun/ExecutionRound identities
- Segment별 requested/completed ALNS steps, completed 합과 explicit `phase2MaxSteps`
- Selector admission/work/time/budget counters와 ALNS step 합에서의 분리 증명
- Projection/model/coefficient/`MipWarmStart` identities
- Pre/post `AlnsWarmStart`, incumbent bytes, RNG/adaptive state identities
- Backend artifact/version/checksum/platform/native/config identity
- Route-selection run/status/termination/selected IDs와 session
  open/solve/cleanup/close count
- Materialization/full evaluation/comparator/adoption, fresh champion와 single atomic
  commit count
- Phase 07 candidate/result verifier refs
- Exact command/toolchain/environment와 test class/method counts
- Failed/error/skipped counts와 disabled/omitted reason
- Fixture/oracle source와 independence statement
- Security/license/notice/SBOM/CVE/operations/cost owner verdict
- Shadow/reproducibility/rollback result
- Known limitation, open gate와 last safe point
- Immutable content digest

Secret, raw PII, private key, mutable `latest`, console 한 줄과 object listing은 evidence가
아니다.

### 12.4 Stop note template

```text
phase: 13
checkpoint:
status: GATED | BLOCKED | FAILED | IMPLEMENTED_PENDING_EVIDENCE
failed_or_missing_gate:
owner:
observed_source_commit:
receipt_or_evidence_checked:
last_safe_alns_only_manifest:
phase13_artifacts_created:
phase13_provider_or_native_loaded:
incumbent_pre_fingerprint:
incumbent_post_fingerprint:
known_contamination:
phase2_max_steps:
completed_alns_segment_steps:
selector_admission_work_time:
alns_warm_start_pre_post:
mip_warm_start_model:
session_open_solve_cleanup_close_counts:
atomic_commit_count:
restart_condition:
next_consumer:
```

### 12.5 현재 stop·resume 판정

현재 stop:

```text
Phase 06/07/08 accepted = no
Phase 14A receipt = NOT_PRODUCED
C-17 scope approval = absent
backend/native/SBOM/security/ops/cost approval = absent
scheduler Phase 13 implementation task = not assigned for execution
result = GATED_NOT_STARTED
```

Resume는 “문서가 충분히 자세해졌다”가 아니라 위 entry receipt의 모든 immutable
reference가 valid하고 scheduler가 exact task/scope를 `READY`로 전이했을 때다.

## 13. 금지 anti-pattern

- Gate가 닫힌 상태에서 `hybrid.enabled=false`와 empty module을 만들고 완료라고 주장
- Closed path에서 Phase 13 factory를 호출한 뒤 `BACKEND_UNAVAILABLE`로 fallback
- Explicit unauthorized request를 조용히 ALNS-only success로 바꿈
- Phase 12 parity를 C-17 또는 OR-Tools activation으로 해석
- Baseline runtime에도 Phase 12 evidence를 보편 predecessor로 요구
- Phase 14A receipt를 production/traffic authority로 해석
- Root/common dependency management에서 OR-Tools version을 몰래 pin
- `latest` 또는 문서 예시 version을 승인된 artifact로 사용
- ServiceLoader/classpath first-wins로 backend 선택
- Root/stable generic module에 `com.google.ortools` API를 노출
- Pool route와 projected column을 같은 record로 만듦
- Interrupted/partial/stale/cross-fingerprint route를 수집
- Same request set의 scalar minimum route 하나만 남김
- Live pool을 selector에 전달
- Memory pressure에서 hidden pruning/eviction
- Profile의 non-projectable dimension을 생략
- Lexicographic objective를 검증 없는 Big-M 하나로 변환
- Mandatory request를 자동 hard assignment
- Concrete vehicle를 proof 없이 class/count row로 합침
- `FEASIBLE_LIMIT`에서 하위 priority를 계속 solve
- Incumbent 없는 status에서 selected/objective getter 호출
- Raw `objectiveValue()`/bound/gap을 adoption authority로 사용
- Selected IDs를 `CommittedCandidate`로 cast
- Pool artifact를 alias해 conversion 중 mutate
- Backend의 unassigned side channel을 coverage complement보다 신뢰
- Equal/worse/invalid candidate를 diversification 이유로 채택
- `MipWarmStart`를 다음 ALNS segment의 `AlnsWarmStart`로 재사용
- Selector admission/work/time을 ALNS step으로 세거나 ALNS segment 합을
  `phase2MaxSteps`보다 작게/크게 normal complete 처리
- Optional failure/equal/worse 뒤 incumbent/RNG/adaptive/next `AlnsWarmStart`
  bytes 변경
- Strict-better 경로에서 old incumbent를 in-place mutate
- Session을 열기 전에 `session.solve`/cleanup을 호출하거나 open/solve/cleanup/close를
  두 번 호출
- Gate/admission/native 실패 뒤 partial record/state를 publish
- 한 HybridPhase에서 partial/double commit
- Required failure를 `DEGRADED_ALNS_ONLY` normal success로 표시
- Optimize 시작 뒤 같은 run ID로 결과-bearing retry
- Timeboxed multi-worker 실행에 strong reproducibility 주장
- Phase 07 candidate 또는 result verifier 하나를 생략
- Phase 13 producer `ActivatedHandoff`에 signing trust/action-time receipt를 넣음
- Phase 14B signing/trust를 Phase 13 entry 또는 acceptance prerequisite로 역연결
- Signed `Skip`에 fake `E-P13-*`를 넣음
- Test/self-signed/expired/revoked applicability로 Phase 14B action 승인
- Phase 13 acceptance로 Phase 14B official/production authority 주장
- Test-only 숫자와 작은 fixture 개선을 production default로 승격
- Optional adapter source가 생겼다는 이유로 architecture guard를 삭제
- Warm local repository에서 sibling prerequisite 없이 owner `-f`만 실행하고 closure 주장
- Failsafe execution 없이 `*IT` 파일 또는 Surefire success를 integration evidence로 주장
- Console output, existing target report 또는 zero-test success를 evidence로 사용

## 14. 실제 구현 exit checklist와 Definition of Done

### 14.1 Entry와 scope

- [ ] Phase 06/07/08 accepted evidence/review/receipt가 valid하다.
- [ ] Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`가 valid하다.
- [ ] `C-17`, hybrid meaning과 route-selection authority가 exact scope로 승인됐다.
- [ ] Selected runtime과 conditional Phase 12 적용 여부가 명시됐다.
- [ ] Backend/native/license/security/ops/cost/capacity/fallback/rollback 승인이 valid하다.
- [ ] Open config field를 생략값/default로 채우지 않았다.

### 14.2 Architecture와 contract

- [ ] Closed ALNS-only path의 Phase 13 load/provider/native count가 0이다.
- [ ] Generic module의 OR-Tools reference가 0이다.
- [ ] Verification → solver/search/pool/backend edge가 0이다.
- [ ] Default reactor/profile/dependency/service/class/native route-selection activation이
  모두 0이고 approved profile의 optional adapter는 exact 1개다.
- [ ] Directory-absence guard를 삭제하지 않고 versioned default-negative/
  profile-positive guard로 전이했다.
- [ ] Pool artifact와 projected column type/identity가 분리됐다.
- [ ] Append/import/reload가 same deterministic merge를 사용한다.
- [ ] No-alias, incumbent pin과 safe dominance가 증명됐다.
- [ ] Non-projectable profile은 typed skip한다.
- [ ] Request/unassigned/concrete-vehicle exact rows가 oracle와 일치한다.

### 14.3 Backend와 lifecycle

- [ ] Direct CP-SAT만 사용하고 `MPSolver` reference가 0이다.
- [ ] Exact version/checksum/platform/config가 manifest에 있다.
- [ ] Checked int64 coefficient/range/round-trip이 통과한다.
- [ ] Status × incumbent safe-access matrix가 통과한다.
- [ ] Native load/cancel/callback/reference/temp cleanup이 통과한다.
- [ ] Session open/solve/cleanup/close normal count와 각 failure count가 exactly-once
  matrix와 일치한다.
- [ ] License/notice/SBOM/CVE/native redistribution evidence가 complete하다.

### 14.4 Materialization, fallback과 verification

- [ ] Selected IDs가 exact model/pool identity에 속한다.
- [ ] Fresh route/bank와 exact partition을 만든다.
- [ ] Full propagation/evaluation과 bound comparator를 사용한다.
- [ ] Strictly-better candidate만 채택한다.
- [ ] ALNS step/segment/HybridPhase/WorkerRun/ExecutionRound identity가 연결된다.
- [ ] Completed ALNS segment step 합이 exact `phase2MaxSteps`이고 selector
  admission/work/time과 분리된다.
- [ ] `MipWarmStart`와 `AlnsWarmStart`의 type/identity/lifecycle이 분리된다.
- [ ] Equal/worse/invalid/failure에서 incumbent/RNG/adaptive/next
  `AlnsWarmStart` bytes가 같다.
- [ ] Strict-better에서도 old incumbent bytes가 같고 fresh champion과 새
  `AlnsWarmStart`만 준비된다.
- [ ] 한 HybridPhase의 visible state는 single atomic commit 또는 no commit이다.
- [ ] Required failure는 incomplete다.
- [ ] Phase 07 candidate/result verifier를 모두 통과한다.
- [ ] Raw backend result로 publication/benchmark를 승인하는 경로가 0이다.

### 14.5 Evidence와 authority

- [ ] Required tests의 failed/error/skipped가 모두 0이고 discovered count가 non-zero다.
- [ ] Owner POM의 JUnit/test-fixtures/Surefire closure와 adapter Failsafe
  `*IT` closure가 cold repository에서 통과한다.
- [ ] Surefire/Failsafe selected-test zero-test 방지가 켜져 있고 report count를
  command와 대조했다.
- [ ] Independent tiny oracle와 corruption/fault test가 producer code와 독립적이다.
- [ ] Fixed envelope은 exact repeat하거나 weaker reproducibility class를 명시한다.
- [ ] Security/operations/performance/cost/shadow/rollback evidence가 complete하다.
- [ ] Pre-review manifest → independent review → acceptance receipt DAG가 단방향이다.
- [ ] Scheduler가 exact approved scope만 `ACCEPTED`로 전이했다.
- [ ] Phase 13 `ActivatedHandoff`에는 Phase 14B signing/action-time authority가
  없고 `14B → 13` 역방향 dependency가 0이다.
- [ ] Gate-open path에서는 Phase 14B consumer `Activated` wrapper만 signed
  envelope와 action-time verification을 요구한다.
- [ ] 별도 Phase 14B production authority 전 traffic/default activation이 0이다.

### 14.6 Definition of Done

Phase 13 DoD는 위 checklist의 AND다. 그 결과도 세 상태를 구분한다.

| 결과 | 허용되는 상태 | 허용되지 않는 주장 |
|---|---|---|
| Gate closed | `GATED_NOT_STARTED` | Disabled implementation done |
| Code complete, evidence/review 불완전 | `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING` | Accepted/official |
| Exact scope의 evidence/review/receipt complete | Scheduler `ACCEPTED` 가능 | Production active |
| Phase 14B authority와 activation까지 complete | Phase 14B가 production 상태 결정 | Phase 13 단독 activation |

## 15. 다음 Phase handoff와 broken-contract 증상

### 15.1 Gate-closed Phase 14B handoff

Closed branch는 scheduler-owned control record다.

```text
Phase13ApplicabilityReceipt.Skip
  reason = C17_GATE_CLOSED
  exact receipt schema/version
  scheduler decision fingerprint
  exact ALNS-only plan identity
  signed applicability envelope
  trust/validity/revocation/freshness policy refs
  current action-time verification verdict = PASS
  Phase13 pool/model/outcome/hybrid refs = ABSENT
  E-P13 implementation refs = ABSENT
```

Phase 14A는 이 receipt를 요구하지 않는다. Phase 14B는 다른 모든 gate가 충족된
경우에만 이 signed skip을 소비해 ALNS-only path를 계속한다. Skip은 Phase 13
success/acceptance가 아니다.

### 15.2 Gate-open Phase 14B handoff

Phase 13 acceptance가 완료되면 producer는 다음 handoff만 만든다.

```text
Phase13ActivatedHandoff
  activation receipt fingerprint
  exact scope/hybrid meaning/authority
  Phase06/07/08 accepted receipts
  Phase14A ALNS benchmark acceptance receipt
  optional applicable Phase12 accepted receipt
  pool/projection/model/backend/config/reproducibility fingerprints
  E-P13-GATE
  E-P13-POOL
  E-P13-SELECTION
  E-P13-HYBRID
  E-P13-SECURITY-LICENSE-OPS-COST
  E-P13-SHADOW-ROLLBACK
  accepted Phase13 implementation/evidence review
  last accepted ALNS-only rollback point
```

이 handoff에는 signed applicability envelope, trust root/algorithm, revocation/
freshness verdict와 current action-time verification을 넣지 않는다. 따라서
`G14-SIGNING-TRUST`가 아직 열리지 않아도 exact Phase 13 scope의
implementation/evidence/review/acceptance와 위 handoff는 완료할 수 있다.

Phase 14B가 official hybrid manifest를 별도로 선택해 실제로 소비할 때만 다음
consumer wrapper를 만든다.

```text
Phase13ApplicabilityReceipt.Activated
  phase14aAcceptanceReceiptDigest
  phase13AcceptedHandoffDigest
  signedApplicabilityEnvelope
  actionTimeVerificationReceiptDigest
  lastAcceptedAlnsOnlyRollbackPointDigest
```

`Phase13ActivatedHandoff`는 wrapper의 sibling input이지 내부 signed field가 아니다.
Phase 14B는 현재 action마다 signed envelope의 trust/validity/revocation/freshness를
검증하고 handoff, rollback point와 official manifest scope가 exact match인지 확인한다.
다른 customer/profile/backend/budget/traffic에 재사용하지 않는다.

### 15.3 Phase 14의 권위를 보존하기

Phase 13이 `ACCEPTED`여도 다음은 Phase 14B 소유다.

- `Q-BENCH-02` official 값
- Official execution manifest와 benchmark card
- Phase 11/provider deployment evidence
- Signing trust와 current action verification
- Shadow/canary observation
- Traffic/pointer transition
- Production authority envelope
- Production activation/rollback record

Phase 13은 “production activation recommendation 또는 reject decision”을 evidence로
넘길 수 있을 뿐 production action을 실행하거나 승인하지 않는다.
`G14-SIGNING-TRUST`는 Phase 14B consumer gate이며 Phase 13 entry, C-17 optional
route-selection activation, implementation, evidence review 또는 acceptance의
predecessor가 아니다. 반대로 Phase 13 owner도 이 gate의 `PASS`를 자체 생산하지 않는다.

### 15.4 Broken-contract 증상

다음 중 하나가 보이면 handoff를 중단한다.

- `Skip`에 pool/model/backend/evidence ref가 있음
- `Activated`에 Phase 14A receipt가 없음
- `Phase13ActivatedHandoff` 내부에 signed envelope/action-time verification이 있음
- Phase 14B signing/trust 부재 때문에 Phase 13 acceptance/handoff를 차단함
- Phase 14B `Activated` wrapper에 handoff/signed envelope/current action-time
  verification 중 하나가 없음
- Baseline runtime인데 Phase 12 ref 부재를 blocker로 처리
- Substituted runtime인데 applicable Phase 12 accepted evidence가 없음
- Backend selected IDs 대신 raw objective/solution bytes를 넘김
- Full evaluator/Phase 07 report가 없음
- Rollback point가 mutable latest 또는 삭제 대상임
- Phase 14B action 시점 trust/revocation/freshness verification이 없음
- Phase 13 owner가 traffic/pointer scope를 승인함
- 다른 scope의 old receipt를 재사용함

## 16. Source → requirement → WP → test/evidence traceability

| Source | Requirement | WP | Test 또는 evidence |
|---|---|---|---|
| Master `C-17`, `§15.11`, `§16.3` | Separate gate 전 구현/default 금지 | WP13-0 | `Phase13SchedulerApplicabilityTest`, `E-P13-GATE` |
| 사용자 ALNS-first + Plan Phase 14A | Accepted ALNS benchmark receipt가 먼저 | WP13-0 | Missing/mismatched receipt negative test, `E-P14-ALNS-BENCHMARK-ACCEPTANCE` |
| Phase 12 `§18.3` | Substituted runtime에만 bounded evidence | WP13-0 | Two conditional-runtime scope tests |
| Current Domain `§10.9`, `§12.5`, Master `§11.7` | Same-authority immutable route pool | WP13-1 | `RoutePool*Test`, `E-P13-POOL` |
| Current Domain `§12.5` | Exact projection과 request/vehicle partition | WP13-2 | `RouteSelectionProjectionTest`, tiny oracle |
| Current Domain `§12.6` | Model mode와 exact model-scoped `MipWarmStart` | WP13-2 | `WarmStartContractTest` |
| Current Architecture `§6.7` | Backend-neutral status/incumbent/session | WP13-3 | `SelectionOutcomeContractTest`, `BackendLifecycleTest` |
| C-17 + Current Architecture `§5`, `§6.7`, `§18~19` | Direct CP-SAT, native isolation과 executable POM/test closure | WP13-3/4 | Adapter contract/native `*IT`, cold-reactor report |
| Current Domain `§12.7`, `§13.1~13.4` | Fresh materialization/full evaluation | WP13-5 | `RouteSelectionMaterializationTest` |
| Current Domain `§12.6~12.8` | `AlnsWarmStart`/`MipWarmStart` 분리, strict adoption과 atomic commit | WP13-5/6 | `HybridWarmStartLifecycleTest`, `HybridAtomicCommitTest` |
| Current Architecture `§11.2`, `§14` | ALNS step 계층·합과 selector accounting 분리 | WP13-6 | `HybridStepBudgetTest`, `E-P13-HYBRID` |
| Current Architecture `§17` | Typed failure/retry와 post-optimize same-run retry 금지 | WP13-6 | `HybridRetryTest`, lifecycle fault tests |
| Current Domain `§16.6~16.7`, Current Architecture `§17~18` | Evidence-bounded reproducibility class | WP13-6/8 | `HybridReproducibilityTest`, shadow card |
| Current Architecture `§16~18`, Integrated `§20~22` | Security/native/ops/cost evidence | WP13-7 | `HybridSecurityContractTest`, `E-P13-SECURITY-LICENSE-OPS-COST` |
| Phase 07 contract, Master `C-21` | Two verifier를 우회하지 않음 | WP13-5/8 | `HybridPhase07BoundaryTest`, `E-P13-HYBRID` |
| Master rollback, Plan `§12` | Accepted ALNS-only last-safe point | WP13-6/8 | `HybridRollbackTest`, `E-P13-SHADOW-ROLLBACK` |
| Phase 13 `§6.5/§14.3` + Phase 14 `§5.3` | Closed signed skip, Phase 13 refs absent | WP13-0/8 | `Phase13Phase14SkipContractTest`, scheduler receipt |
| Phase 13 `§14.4` + Phase 14 human `§6.4`, `§9.7` | Accepted handoff와 Phase 14B signing/action-time wrapper 분리 | WP13-8 | `Phase13ApplicabilityDependencyTest`, `E-P13-HANDOFF` |
| Register `Q-BENCH-02`/`Q-VAR-01` | Open/deferred 값을 숨은 default로 만들지 않음 | 모든 WP | Config/traffic/cost negative tests와 known limitations |

## 17. 구현자가 마지막으로 답할 자문 질문

1. 지금 가진 것은 문서 review인가, implementation acceptance receipt인가?
2. Phase 14A receipt와 C-17 approval을 서로 다른 reference로 제시할 수 있는가?
3. Selected runtime 때문에 Phase 12 evidence가 필요한지 정확히 설명할 수 있는가?
4. Gate가 닫혔을 때 Phase 13 code가 한 줄도 실행되지 않음을 어떻게 증명하는가?
5. Route identity, coverage key, artifact ID와 projected column ID를 구분했는가?
6. Pool admission이 solution acceptance와 독립인 이유를 설명할 수 있는가?
7. Non-projectable profile을 만났을 때 무엇을 생략하지 않고 어떻게 멈추는가?
8. Tiny oracle가 production code와 독립이라는 evidence가 있는가?
9. No-incumbent status에서 호출하면 안 되는 backend API를 알고 있는가?
10. Selected IDs가 stale/cross-fingerprint일 때 materialization이 0인가?
11. Full evaluator와 backend objective가 다르면 누구를 믿는가?
12. Equal/worse/invalid/failure 뒤 incumbent와 warm start가 byte-identical한가?
13. Completed ALNS segment-step 합이 `phase2MaxSteps`와 같고 selector work가
    별도인지 증명하는가?
14. `MipWarmStart`가 다음 `AlnsWarmStart`로 새지 않고 RNG/adaptive continuation이
    보존되는가?
15. Session open/solve/cleanup/close와 atomic commit count가 정상·fault마다
    exactly-once인가?
16. Optional과 required failure status가 분리됐는가?
17. Timeboxed optimize 뒤 retry가 새 logical run을 만드는가?
18. Cold repository의 reactor prerequisite, owner Surefire와 adapter Failsafe가
    non-zero test를 실행하는가?
19. Default architecture guard와 approved profile positive guard를 모두 보존했는가?
20. Generic build와 ALNS-only distribution이 OR-Tools/native 없이 통과하는가?
21. License/notice/SBOM/native platform evidence와 algorithm evidence를 구분했는가?
22. Phase 07 두 verifier 중 하나라도 fail하면 publication이 0인가?
23. Phase 13 evidence와 scheduler-owned signed `Skip`을 구분했는가?
24. Phase 13 accepted handoff와 Phase 14B `Activated` signing/action-time wrapper를
    구분했는가?
25. Phase 13 acceptance와 Phase 14B production authority를 구분했는가?
26. 실패하면 삭제가 아니라 어느 immutable ALNS-only point로 rollback하는가?

이 질문 중 하나라도 evidence로 답할 수 없으면 다음 WP 또는 Phase 14B로 넘기지 않는다.
