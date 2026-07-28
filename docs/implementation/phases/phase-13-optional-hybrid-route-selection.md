# Phase 13 — Optional hybrid

> 부제: Route selection

```yaml
document_status: REVIEWED_WITH_CORRECTIONS
document_version: 1.5
phase: "13"
phase_name: optional-hybrid-route-selection
baseline_date: "2026-07-28"
phase_applicability: CONDITIONAL_NOT_APPROVED
implementation_status: GATED_NOT_STARTED
activation_status: C17_GATE_CLOSED
production_activation_status: NOT_AUTHORIZED
evidence_status: NOT_PRODUCED
alns_benchmark_acceptance_status: NOT_PRODUCED
review_status: COMPLETE_DOCUMENT_CONTRACT_ONLY
review_verdict: PASS_WITH_RESIDUAL_BLOCKERS_GATE_REMAINS_CLOSED
review_ref: ../reviews/phase-13-review.md
entry_gate_status: CLOSED_MULTIPART_AUTHORITY_AND_EVIDENCE_GATE
handoff_status: SKIP_CONTRACT_DEFINED_NO_ACTIVATED_HANDOFF
signed_applicability_receipt_status: NOT_PRODUCED
signing_trust_policy_status: OPEN_GATED_NOT_APPROVED
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Optional Hybrid owner role
  product_scope_and_route_selection_authority: Product/Algorithm authority role
  architecture_and_dependency: RPDPTW Architecture owner role
  upstream_alns: Phase 06 COW ALNS/Reproducibility owner role
  upstream_verification: Phase 07 Independent Verification/Result owner role
  conditional_provider_substitution_evidence: Phase 12 Provider Substitution owner role
  solver_backend: Optimizer Backend owner role
  oss_license_and_legal: Open Source/Legal/Supply-chain owner role
  operations: Platform/SRE owner role
  security: Platform Security owner role
  cost: FinOps/Cost authority role
  performance_and_experiment: Algorithm Performance/Benchmark owner role
  independent_pool_oracle: Phase 13 Pool/Selection oracle owner role
  downstream_official: Phase 14 Calibration/Cutover owner role
  applicability_and_status: total scheduler role
  applicability_signing_and_trust: Security/Release evidence-trust owner role
  review: independent Phase 13 reviewer role
prerequisites:
  - Phase 06 accepted COW ALNS, cache-free route evaluation and replay baseline
  - Phase 07 accepted independent candidate/result verification and both-gate publication baseline
  - Phase 08 accepted optimizer-free local reference execution
  - Phase 14A accepted ALNS correctness/quality/performance/reproducibility benchmark receipt
  - if the approved hybrid scope selects a substituted provider/runtime, the applicable Phase 12 evidence and independent review are approved
  - C-17 separate scope approval
  - explicit hybrid meaning and route-selection authority contract approval
  - Google OR-Tools direct CP-SAT exact version/checksum/config approval
  - Apache-2.0/applicable third-party notice, SBOM, native redistribution and supported-platform approval
  - operations/retry/cancellation/rollback approval
  - security/tenant/secret/access approval
  - cost/capacity/budget authority approval
  - measured ALNS-only baseline and explicit reproducibility classification
planned_evidence:
  - E-P13-GATE
  - E-P13-POOL
  - E-P13-SELECTION
  - E-P13-HYBRID
  - E-P13-SECURITY-LICENSE-OPS-COST
  - E-P13-SHADOW-ROLLBACK
  - E-P13-HANDOFF
source_sections:
  canonical_master: "§1~4, §10~§17; especially C-17, §11.7~11.10, RM-9A~RM-9C and §16.3"
  final_domain: "§7~§10, §12~§18; artifact/selection/hybrid/verification/gate meaning"
  final_architecture: "§2~§6; especially optional backend boundary, lifecycle, verification and AR-H1~AR-H3"
  integrated_design: "§3, §10~§25, §27~§28; especially Phase 12~14"
  open_questions: "Q-BENCH-02, Q-INFRA-01, Q-VAR-01 and §3~4"
  implementation_readme: "§1~§7"
  master_realization_plan: "§2~§7 Phase 06/07/12/13/14 and §8~§15"
  execution_progress: "§2, §5, §8~§9"
  phase_06_actual: "§16.3 Phase 13 downstream handoff projection only"
  phase_07_actual: "§15.3 Phase 13 downstream handoff projection only"
  phase_12_actual: "§18.3 conditional infrastructure-decision input only; not a universal Phase 13 predecessor"
  phase_14_actual: "v1.4 §0.1, §3.1~3.2 and §5.3 Phase 14A benchmark prerequisite plus Phase 14B applicability/handoff compatibility"
  root_readme_and_inventory: "README technology/deployment/placeholder plus actual POM/Java/test/GCP inventory"
historical_cross_check:
  file: docs/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
neighbor_phase_documents:
  phase_06: ACTUAL_REVIEWED_NOT_STARTED_NOT_ACCEPTED
  phase_07: ACTUAL_REVIEWED_NOT_STARTED_NOT_ACCEPTED
  phase_12: ACTUAL_V1_3_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_BLOCKED_NOT_IMPLEMENTED_HANDOFF_NOT_READY
  phase_14: ACTUAL_V1_4_REVIEW_COMPLETE_CHANGES_REQUIRED_14A_NOT_RUN_14B_NOT_STARTED_HANDOFF_NOT_READY
  phase_13_review: ACTUAL_COMPLETE_PASS_WITH_RESIDUAL_BLOCKERS_DOCUMENT_CONTRACT_ONLY
  all_phase_reviews: ACTUAL_15_OF_15_COMPLETE_NOT_PHASE_ACCEPTANCE
authoring_inventory_snapshot_status: HISTORICAL_CODE_BUILD_CHARACTERIZATION
neighbor_validation_policy: LATEST_NAMED_CONTRACT_AND_BLOCKER_SECTIONS_READ_ONCE_SEMANTIC_COMPARISON_ONLY
adjacent_digest_acceptance: FORBIDDEN
reciprocal_document_fingerprint_acceptance: FORBIDDEN
```

## 1. 문서 지위, 권위와 source 해석

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. Source 문서의
`REVIEW` metadata는 provenance로 보존하지만 상세 문서 작성을 중단하는 조건이
아니다. 반대로 이 문서에 구체적인 type, interface, state, test와 work package가
있다는 사실은 Phase 13의 구현, backend dependency/config 승인, OSS/native 배포 승인, evidence, review,
official hybrid 실행 또는 production activation이 시작·완료되었다는 뜻이 아니다.

상태 축을 섞지 않는다.

| 축 | 현재 값 | 의미 |
|---|---|---|
| Source authority | `USER_LOCKED_FOR_THIS_DOCUMENT_SET` | 지정 입력을 대조해 문서를 작성할 수 있다. |
| 문서 | `REVIEWED_WITH_CORRECTIONS` | 조건부 계약만 독립 검토됐다. |
| Applicability | `CONDITIONAL_NOT_APPROVED` | Phase 13을 applicable branch로 승인한 기록이 없다. |
| `C-17` | `GATED TARGET` | Scope/evidence/authority gate 전 구현 착수·default 활성화 금지다. |
| ALNS benchmark acceptance | `NOT_PRODUCED` | Phase 14A receipt 없이는 다른 approval과 무관하게 entry가 닫힌다. |
| 구현 | `GATED_NOT_STARTED` | Target module/type/test/backend가 존재한다고 주장하지 않는다. |
| Backend policy | `GOOGLE_OR_TOOLS_DIRECT_CP_SAT_SELECTED_NOT_IMPLEMENTED` | C-17이 열릴 경우의 canonical backend만 고정한다. 활성화·구현 완료가 아니다. |
| Dependency/config | `OPEN/GATED_NOT_APPROVED` | OR-Tools exact version/checksum, workers, seed, time/gap과 platform matrix가 승인되지 않았다. |
| OSS license/legal | `APACHE_2_0_IDENTIFIED_DISTRIBUTION_REVIEW_OPEN` | OR-Tools 자체 license는 확인했으나 native/transitive notice·SBOM·재배포 evidence가 없다. |
| Operations/security/cost | `NOT_APPROVED` | 운영, 접근, 비용, capacity와 rollback 승인이 없다. |
| Evidence | `NOT_PRODUCED` | `E-P13-*`는 미래 bundle requirement다. |
| Review | `COMPLETE_DOCUMENT_CONTRACT_ONLY` | [독립 review](../reviews/phase-13-review.md)는 구현/evidence/activation acceptance가 아니다. |
| Phase 14B handoff | `SKIP_ONLY_WHILE_CLOSED` | Gate가 닫히면 Phase 13 산출물 없이 ALNS-only cutover 경로로 건너뛴다. |
| Signed applicability | `NOT_PRODUCED` | Proposed schema만 있으며 signed envelope와 action-time verification receipt는 없다. |
| Signing/trust policy | `OPEN/GATED_NOT_APPROVED` | Algorithm, trust root, revocation/time authority와 숫자를 이 문서가 정하지 않는다. |

권위 적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 immutable route,
   projection, materialization, verification 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의
   module/package/OR-Tools/native/OSS-license lifecycle 경계
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의
   15 Phase와 Phase 12~14 배치
6. [Master Realization Plan](../master-realization-plan.md),
   [구현 문서 지도](../README.md)와 actual predecessor handoff

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는
누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 역사 자료이며 현재
authority, API, backend, 승인 또는 evidence로 사용하지 않는다.

Final Domain §18과 Final Architecture §6 말미의 `Q-INFRA-01 DEFERRED`,
`25/1/2` 표기는 최신 Canonical Master와 질문 등록부의
`Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. AWS 선택은 Phase 13 backend activation
활성화나 OR-Tools dependency/config/native 배포 승인이 아니며 Phase 13 activation evidence를 대신하지 않는다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 13에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §1~§4, §10~§17, 특히 `C-17`, §11.7~§11.10, `RM-9A~C`, §16.3 | GATED target, immutable pool, exact projection, materialize/full-evaluate/strict adoption, two-gate authority, fallback와 별도 approval |
| [Final Domain](../../2026-07-26-domain-design.md) | §7~§10, §12~§18 | Artifact/column identity 분리, exact partition, typed outcome, fresh materialization, hybrid state, acceptance evidence |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2~§6 | OR-Tools-free default DAG, direct CP-SAT adapter boundary/native lifecycle, full evaluation, security와 AR-H1~H3 |
| [Integrated design](../../architecture-domain-implementation-design.md) | §3, §10~§25, 특히 §16~§18 | Phase 12 독립 substitution branch, Phase 13 contract, Phase 14 conditional predecessor, provenance/failure/test/anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01`, §3~§4 | Official 수치 open, AWS target resolved, optional variant deferred; 어느 것도 hybrid hidden default를 만들지 않음 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§7 Phase 06/07/08/12/13/14A/14B, §8~§15 | Current inventory, ALNS benchmark predecessor, GATED status, evidence/DoD/rollback, conditional Phase 14B handoff |
| [구현 문서 지도](../README.md) | §1~§7 | Canonical filename, status/review/scheduler 권한과 conditional branch |
| [Execution Progress](../execution-progress-and-results.md) | §2, §5, §8~§9 | Phase 13 registry `GATED`, task ID 미지정, scheduler-only status/applicability |
| [Actual Phase 06](phase-06-cow-alns-reproducibility.md) | §16.3 only | Accepted ALNS baseline/candidate/replay/state-lifecycle seam만 소비; raw backend incumbent/apply-undo 금지 |
| [Actual Phase 07](phase-07-independent-verification-final-result.md) | §15.3 only | Materialize/full-evaluate된 candidate도 동일 both-gate path를 통과 |
| [Actual Phase 12](phase-12-provider-substitution.md) | §18.3 only, plus Integrated §16 and Master Plan Phase 12 | `INFRASTRUCTURE_DECISION_INPUT_ONLY`; approved hybrid scope가 substituted provider/runtime을 실제 선택할 때만 해당 evidence를 조건부 소비하며 `C-17`/backend activation/hybrid authority를 부여하지 않음 |
| [Actual Phase 14](phase-14-official-calibration-cutover.md) | v1.4 §0.1, §3.1~§3.2 and §5.3, plus Integrated §18 and Master Plan Phase 14 | Phase 14A ALNS benchmark acceptance prerequisite와 Phase 14B scheduler-owned skip/official hybrid handoff를 분리 |
| [Root README](../../../README.md)와 actual inventory | 기술 기준·placeholder/GCP 흐름 | 현재 코드가 target ALNS, pool, selector, verifier 또는 approved runtime이 아님 |

현재 Phase 00~14 review는 `15/15` 모두 완료됐다. Phase 12 v1.3의 문서 verdict는
`CHANGES_REQUIRED`, phase acceptance는 `BLOCKED_NOT_IMPLEMENTED`, handoff는
`NOT_READY`다. Phase 14 v1.4의 문서 verdict는 `CHANGES_REQUIRED`, 14A는
`NOT_RUN/RECEIPT_NOT_PRODUCED`, 14B는 `NOT_STARTED/BLOCKED_NOT_READY`, 전체 phase
acceptance와 handoff는 `BLOCKED_NOT_READY`/`NOT_READY`다. Phase 06/07 상세와 review도 actual이지만
implementation/evidence/acceptance는 각각
`NOT_STARTED`/`NOT_PRODUCED`/`NOT_ACCEPTED`다. Review 완료는 accepted handoff가
아니다. 이 문서와 [Phase 13 review](../reviews/phase-13-review.md)는 문서 계약만
다루며 Phase 13 implementation/evidence/activation을 만들지 않는다.

### 1.2 인접 계약 검증 정책

동시 batch의 최신 Phase 12/14 named contract/blocker section은 이 review에서 한 번
읽고 semantic compatibility를 대조했다. 인접 whole-file/section digest, reciprocal
fingerprint 또는 반복 hash 추적은 entry, acceptance, handoff evidence가 아니다.
향후 인접 의미가 바뀌면 changed section과 requirement/test impact를 다시 review하고,
실제 handoff는 accepted immutable artifact/evidence identity로만 고정한다.

### 1.3 ALNS-first gate와 MIP 복잡도 해석

Phase 13은 `05 → 06 → 07 → 08 → 14A` ALNS-only 경로가 독립적으로 구현·검증되고
`ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`를 받은 뒤에만 검토하는 optional branch다.
Phase 06/07 pass, OR-Tools 설치 또는 route pool 설계만으로 이 gate를 열 수 없다.

Mixed-integer route selection은 조합 최적화 문제이므로 worst-case 난도가 높으며,
실무 solve time은 request/route/column 수, 제약 밀도와 formulation, coefficient
scaling, backend/config, thread와 hardware에 민감할 수 있다. 이는 작은 bounded
instance나 잘 구성된 model까지 항상 느리다는 뜻이 아니다. 채택 판단은 ALNS-only
accepted baseline과 동일 authority의 bounded experiment로만 한다.

승인 가능한 연계 제안은 다음 선택지를 포함할 수 있으나 어느 것도 숨은 기본값이 아니다.

| Proposed mode | 경계 | 필수 fallback |
|---|---|---|
| Solver-neutral route selection | Sealed immutable pool의 exact-projectable column만 선택 | No incumbent/failure/timeout이면 ALNS incumbent 보존 |
| Bounded subproblem | 승인된 request/route/column 범위와 resource budget 안에서만 실행 | 범위 밖은 ALNS-only, partial raw result 채택 금지 |
| Warm start | Cache-free validated ALNS incumbent를 hint로만 제공 | Hint 거부/무시가 ALNS result를 바꾸지 않음 |
| Repair | 명시된 infeasible/partial selection을 fresh materialization·full evaluation 전에 격리 | Repair 실패 시 raw selection 폐기 |
| Intensification | ALNS incumbent 주변의 승인된 제한 탐색 | Equal/worse/invalid이면 incumbent 유지 |

Scope approval은 proposed interface/mode, bounded input, budget, timeout/resource failure,
optional/required 의미와 rollback을 명시해야 한다. 승인 없는 mode 선택, library default,
provider availability 또는 빠른 한 사례를 production 기본값으로 사용하지 않는다.

## 2. 목표, 범위, 비범위와 불변조건

### 2.1 목표

Phase 13의 조건부 목표는 accepted ALNS-only baseline을 훼손하지 않는
**adapter/selection boundary**를 통해 다음 가능성을 검증하는 것이다.

```text
accepted Phase 06 ALNS candidate/replay
→ completed hard-feasible evaluated route collection
→ immutable sealed pool
→ exact-projectable route-selection request
→ provider-neutral outcome
→ fresh materialization + authoritative full evaluation
→ strictly-better-only adoption or unchanged incumbent
→ Phase 07의 동일한 independent both-gate verification
```

여기서 “hybrid”의 고정된 최소 경계는 **ALNS가 만든 route artifact를 별도
selection 경계에 전달하고, 반환된 ID를 새 candidate로 재구성해 다시 평가한 뒤
ALNS incumbent와 비교하는 조건부 흐름**이다. 다음은 원문에서 production
계약으로 확정되지 않았으므로 §3의 open contract로 남긴다.

- Selection을 어떤 solve, customer, workload, profile에 적용할지
- Worker-local인지 cross-worker인지, 몇 회/cadence로 호출할지
- Optional fallback만 허용할지 required mode도 제품에 노출할지
- 누가 route selection 요청·채택·production activation authority를 갖는지
- OR-Tools exact version/checksum, supported platform/native packaging과 실행 파라미터를 어떻게 승인할지
- Pool cap, pruning, budget, threshold, traffic split과 성능/cost 기준

### 2.2 포함 범위 — gate가 열린 뒤에만

- `C-17` activation receipt와 모든 prerequisite evidence의 fail-closed 검증
- Gate-closed canonical absence/no-op와 unauthorized activation rejection
- Same-authority immutable evaluated route artifact admission과 delta/snapshot
- Deterministic append/import/reload, no-alias, incumbent pin과 conservative dominance
- Profile projection capability와 typed non-projectable skip
- Request/unassigned/concrete-vehicle exact partition model 후보
- Backend-neutral session/outcome/budget/cancellation boundary
- Selected ID만 반환하는 direct Java CP-SAT adapter와 native/OSS-license lifecycle
- Stale/partial candidate, stale snapshot, fingerprint divergence와 corruption 거부
- Fresh materialization, exact partition, full propagation/evaluation과 strict adoption
- Optional fallback의 exact incumbent preservation과 required-mode incomplete 구분
- Pool/model/warm-start/backend/decision/reproducibility lineage
- Phase 07 independent candidate/result verifier를 우회하지 않는 worker handoff
- Security, OSS license/SBOM, operations, performance, cost와 rollback/shadow evidence
- Phase 14용 gate-closed skip 또는 gate-open accepted evidence handoff

### 2.3 명시적 비범위

- Gate 충족 전 Java/module/test/backend/dependency profile/IaC 구현 시작
- OR-Tools CP-SAT 외 Gurobi, `MPSolver` 또는 다른 exact backend를 production default/fallback으로 가정
- Hidden `enabled=false`, 자동 backend discovery, classpath first-wins 또는 silent fallback
- Pool cap, age, memory threshold, route count, MIP node/time/work budget의 무승인 기본값
- Hybrid cadence, 적용 비율, customer allowlist 또는 traffic split의 무승인 기본값
- `SET_PARTITION_EXACT`, compatibility cover mode 또는 required mode의 product default 승인
- Cross-worker pool fan-in, central selector 또는 distributed backend broker의 자동 포함
- Core result/outcome/publication 의미, Phase 07 verifier와 comparator 재설계
- Raw optimizer incumbent, objective, feasibility flag 또는 selected column 직접 publication
- Phase 12 cloud provider adapter 재구현 또는 provider cutover
- Phase 14 official 수치, benchmark, traffic/pointer cutover 또는 production authority
- `Q-BENCH-02` 공식값, current decimal `D/U`의 official 사용
- `Q-VAR-01`, MDVRP/OVRP/SDVRP, multi-trip/rotation 또는 apply/undo 활성화
- External API/wire schema, degraded status와 public error의 최종 승인

### 2.4 고정 불변조건

1. Gate가 닫혀 있으면 production/test assembly에 Phase 13 backend를 자동
   resolve하거나 호출하지 않는다.
2. Hybrid 요청이 **없음**은 canonical absence/no-op다. 승인 없는 명시적 요청은
   no-op이 아니라 `UNAUTHORIZED_HYBRID_ACTIVATION`으로 fail closed한다.
3. Gate receipt는 `C-17` scope, Phase 06/07 acceptance,
   OR-Tools dependency/native/OSS-license/operations/security/cost authority를 AND로 확인한다. Approved
   hybrid scope가 substituted provider/runtime을 선택한 경우에만 그 선택에 해당하는
   accepted Phase 12 evidence를 추가로 요구한다.
4. Generic core/solver/verification/application의 `com.google.ortools` API reference는
   0이며 기본 root build와 ALNS-only runtime은 OR-Tools/native-loader-free다.
5. Pool artifact와 projected column은 같은 type/identity가 아니다.
6. Pool에는 same problem/travel/profile authority의 nonempty, pair-complete,
   completed, hard-feasible, full-evaluated immutable route만 들어간다.
7. Interrupted, invalid, partial, stale, cross-fingerprint candidate는 수집하지 않는다.
8. Pool merge/seal은 stable ordering을 사용한다. Completion/thread/map order는
   snapshot fingerprint 또는 selection tie-break의 hidden input이 아니다.
9. Safe dominance proof가 없으면 route를 제거하지 않는다. Memory pressure가
   correctness를 바꾸는 hidden pruning을 활성화하지 않는다.
10. Non-projectable constraint/dimension은 누락·surrogate·hidden Big-M로 바꾸지
    않고 typed skip한다.
11. Backend는 direct Java CP-SAT로 고정하되 exact dependency/checksum, platform,
    mode, budget, `num_workers`, `random_seed`, time/gap, numeric setting과
    reproducibility class는 explicit approved config와 fingerprint를 가진다.
12. Backend outcome은 CP-SAT status × incumbent 계약을 확인한 뒤 selected stable
    ID만 노출한다. `CpModel`, `CpSolver`, raw status/handle은 adapter 밖으로 나오지 않는다.
13. Selected ID는 exact pool/model identity에 속해야 하며 stale/partial/different
    fingerprint면 materialization 전에 거부한다.
14. Materialization은 pool route를 alias/mutate하지 않고 새 route/bank를 만든다.
15. Raw objective/status/incumbent는 result authority가 아니다. Full
    propagation/evaluation과 comparator가 selection candidate authority다.
16. Selector candidate는 cache-free validated ALNS incumbent보다
    `STRICTLY_BETTER`일 때만 adopt한다.
17. Equal/worse/invalid/no-incumbent/failure에서 optional path의 incumbent,
    pool snapshot과 next warm start fingerprint는 변하지 않는다.
18. Required policy failure를 degraded normal success로 바꾸지 않는다.
19. Phase 13 candidate도 Phase 07 candidate verifier → finalization/audit →
    result verifier를 모두 통과해야 publication/official comparison 대상이다.
20. Retry는 logical pool/model/warm-start/run identity를 바꾸지 않는다.
    Timeboxed optimize 시작 뒤 result-bearing retry는 새 logical run을 요구한다.
21. Secret, raw PII와 provider credential은 artifact,
    fingerprint, log/trace에 포함하지 않는다.
22. Gate-closed Phase 14 handoff에는 pool/model/outcome/hybrid evidence가
    존재한다고 쓰지 않는다. Skip은 non-applicability이며 Phase 13 성공이 아니다.
23. Gate-open Phase 14 handoff는 accepted Phase 13 review와 immutable
    `E-P13-*` evidence를 추가 요구하며 “backend 호출 성공”으로 대체할 수 없다.
24. Open/gated/deferred 항목을 boolean, 숫자, provider 또는 traffic default로
    채우지 않는다.

## 3. Fixed contract와 open contract

### 3.1 현재 고정할 수 있는 것

| 항목 | 상태 | 고정 의미 |
|---|---|---|
| Phase 지위 | FIXED | `C-17 GATED/optional`; critical path 기본 predecessor가 아님 |
| ALNS baseline | FIXED | Phase 06 accepted candidate/replay가 last safe source |
| Verification | FIXED | Phase 07 independent both-gate path를 동일하게 사용 |
| Pool source | FIXED | Completed hard-feasible full-evaluated route only |
| Identity | FIXED | Problem/travel/profile/route/projection/model/warm-start/run identity 분리 |
| Selection output | FIXED | Provider-neutral typed status와 stable selected IDs only |
| Reconstruction | FIXED | Fresh clone/materialization + exact partition + full evaluation |
| Adoption | FIXED | Strictly-better only; otherwise incumbent unchanged |
| Failure | FIXED | Optional fallback와 required incomplete를 구분 |
| Exact backend policy | FIXED | Google OR-Tools direct Java CP-SAT; `MPSolver` 사용 금지 |
| Exact model class | FIXED | Boolean decision variables + integer/fixed-point coefficients only |
| Default build | FIXED | OR-Tools/native-loader-free ALNS-only path |
| Gate-closed behavior | FIXED | Omitted branch = absence/no-op; explicit unauthorized request = fail closed |
| Phase 14 skip | FIXED | Phase 13 output 없이 ALNS-only path 진행 가능 |

### 3.2 승인 전 확정하지 않는 open contract

| Contract | 현재 상태 | Owner/evidence | 금지되는 hidden 결정 |
|---|---|---|---|
| “Hybrid” 제품 의미 | OPEN/GATED | Product + Algorithm + Architecture | 품질 향상 기능, fallback 기능 또는 required 기능을 암묵 선택 |
| Route selection authority | OPEN/GATED | Product/Algorithm authority + independent review | Backend가 candidate/adoption/publication authority를 가짐 |
| Applicable solve/customer/profile | OPEN/GATED | Product/Profile owner | 모든 solve, 특정 고객명 또는 classpath profile 자동 적용 |
| Model mode | PROPOSED/OPEN | Projection owner + tiny oracle + approval | `SET_PARTITION_EXACT` 또는 cover mode 자동 선택 |
| OR-Tools dependency/version/checksum | OPEN/GATED | Architecture + Security/Supply-chain | Maven example/latest를 승인 pin으로 사용 |
| Native/platform packaging | OPEN/GATED | Architecture/Operations/Security | 모든 OS/arch transitive JAR 또는 temp extraction을 검증 없이 허용 |
| OSS license/notice/SBOM | OPEN/GATED | Legal/Supply-chain/Security | Apache-2.0 확인만으로 bundled/transitive notice 의무가 완료됐다고 주장 |
| Pool retention/cap/pruning | OPEN — EXPERIMENT_REQUIRED | Performance/Algorithm | 고정 route 수, age, memory threshold |
| Selection budget/CP-SAT params | OPEN — EXPERIMENT_REQUIRED | Performance/Cost | time/workers/seed/gap/deterministic-time 수치 |
| Cadence/phase count | OPEN — EXPERIMENT_REQUIRED | Algorithm/Product | 매 segment 또는 매 round 자동 실행 |
| Traffic split/rollout | OPEN/GATED | Product/Release/Operations | 1%, 10%, 50% 같은 비율 |
| Optional vs required exposure | OPEN/GATED | Product/Operations | 실패를 degraded 또는 incomplete로 임의 분류 |
| Reproducibility class | OPEN per approved config | Algorithm/Benchmark | Fixed seed나 timeboxed multi-worker 실행에 strong replay 주장 |
| Cross-worker selection | DEFERRED SEPARATE ADR | Architecture/Operations | Worker-local baseline과 함께 자동 구현 |
| Public degraded/error schema | OPEN | Product/API/Data | Internal enum을 public compatibility로 승격 |
| Cost/performance go-live bar | OPEN — EXPERIMENT_REQUIRED | FinOps/Performance/Product | 측정 없는 threshold와 pass claim |

Open contract는 `TBD`를 production value로 직렬화하거나 runtime에서 적당한 값으로
보완하지 않는다. 승인되지 않은 assembly에는 해당 config field 자체를 노출하지
않거나, 명시적 요청을 typed unauthorized rejection으로 처리한다.

### 3.3 Gate-closed absence와 unauthorized activation

| Input/상태 | Required behavior | 생성 가능한 것 | 생성하면 안 되는 것 |
|---|---|---|---|
| Hybrid 요청 없음 + gate closed | 기존 ALNS-only path 그대로 실행 | Scheduler-owned proposed signed `Phase13ApplicabilityReceipt.Skip`; actual receipt NOT_PRODUCED | Pool/model/backend/hybrid record |
| Explicit hybrid request + gate closed | Solve 시작 전 fail closed | `UNAUTHORIZED_HYBRID_ACTIVATION` typed rejection/audit | Silent ALNS fallback, fake success |
| Receipt 일부 누락 | `INCOMPLETE_ACTIVATION_RECEIPT` | 누락 owner/evidence key 목록 | Best-effort activation |
| Receipt 만료/stale fingerprint | `STALE_ACTIVATION_RECEIPT` | Expected/actual identity diff | 이전 approval 재사용 |
| Backend 없음 + authorized optional run | Gate-open policy에 따른 typed fallback | `BACKEND_UNAVAILABLE`, unchanged incumbent | Fake empty selection |
| Backend 없음 + authorized required run | `INCOMPLETE` | Failure/rollback record | Degraded normal success |

Gate-closed no-op은 Phase 13 code가 호출됐다가 fallback한 결과가 아니다. Phase 13
branch가 execution graph에 들어오지 않은 **absence semantics**다. 따라서 no-op
경로는 `HybridPhaseOrdinal`, `RouteSelectionRunId`, pool/model/backend fingerprint를
만들지 않는다.

이 판정은 Phase 13 module/assembly 바깥의 scheduler/control plane이 solve dispatch
전에 수행한다. Gate가 닫혀 있고 요청이 없으면 scheduler가 skip receipt만 만들고
canonical ALNS-only path를 호출한다. Gate가 닫힌 명시적 hybrid 요청은 Phase 13
class loading, dependency resolution, backend discovery, pool allocation 또는 run-id
생성 전에 거부한다. Gate-open applicability와 validated receipt가 모두 있는 경우에만
Phase 13 assembly를 포함하고 그 내부 scope verifier를 호출할 수 있다.

## 4. Entry gate와 evidence 확인

### 4.1 모든 조건을 요구하는 activation gate

Phase 13 implementation/evidence 착수 조건은 다음 AND 식이다.

```text
Phase06.accepted
AND Phase07.accepted
AND Phase08.accepted
AND Phase14A.ALNS_BENCHMARK_ACCEPTANCE_RECEIPT.valid
AND C17 scope approved
AND HybridMeaningContract approved
AND RouteSelectionAuthorityContract approved
AND OR-Tools exact version/checksum/config approved
AND Apache-2.0/applicable notices/SBOM/native/platform distribution approved
AND operations/retry/cancel/rollback approved
AND security/tenant/access/native-supply-chain approved
AND cost/budget/capacity approved
AND measured ALNS-only baseline approved
AND scheduler task/owners assigned
AND (selected runtime is not substituted
     OR applicable Phase12 substitution evidence and review approved)
= Phase13 implementation READY
```

한 항목이라도 없으면 `GATED`다. Product scope 승인만으로 dependency/native/security
gate를 대신할 수 없다. Phase 12 parity 결과는 selected substituted runtime의
조건부 infrastructure evidence일 뿐 Phase 13 applicability, backend activation 또는
hybrid authority가 아니다.

### 4.2 Activation receipt 후보

이 schema와 이름은 **proposed internal control-plane contract**다.

```text
Phase13ActivationReceipt
  receiptSchemaVersion
  c17ScopeApprovalRef/digest
  hybridMeaningContractRef/digest
  routeSelectionAuthorityContractRef/digest
  phase06AcceptedReview/evidence refs and digests
  phase07AcceptedReview/evidence refs and digests
  phase08AcceptedLocalExecutionReview/evidence refs and digests
  alnsBenchmarkAcceptanceReceiptRef/digest
  alnsBenchmarkCorpusProtocolCriteriaFingerprint
  selectedRuntimeIdentity
  optional Phase12ToPhase13SubstitutionEvidence ref/digest
    required only when selectedRuntimeIdentity denotes an approved substituted runtime
  ortoolsDependencyConfigApprovalRef/digest
  ossLicenseNoticeSbomNativeApprovalRef/digest
  operationsApprovalRef/digest
  securityApprovalRef/digest
  costBudgetApprovalRef/digest
  alnsBaselineExperimentRef/digest
  reproducibilityDeclarationRef/digest
  allowedScopeFingerprint
  validityWindowOrVersion
  issuedByAuthorities
  schedulerApplicabilityDecisionRef
  receiptContentDigest
```

Secret/credential, commercial infrastructure price와 raw customer data는 receipt에
넣지 않는다. Approval ref는 immutable record identity만 담는다. Receipt
validation은 서명/issuer/expiry/schema/content digest와 allowed scope를 모두
확인한다.

### 4.3 2026-07-28 local entry 판정

| Entry 항목 | 확인할 evidence | Local 관찰 | 판정 |
|---|---|---|---|
| Phase 06 accepted | `E-P06-COW/ALNS/REPLAY`, accepted review | Actual detail은 `NOT_STARTED`, handoff `NOT_READY` | BLOCKED |
| Phase 07 accepted | `E-P07-CANDIDATE-VERIFY/AUDIT/RESULT-VERIFY`, accepted review | Actual detail은 `NOT_STARTED`, handoff `NOT_READY` | BLOCKED |
| Phase 08 accepted | `E-P08-PORT/LOCAL-E2E/IDEMPOTENCY`, accepted review | Actual detail은 `NOT_STARTED`, handoff `NOT_READY` | BLOCKED |
| Phase 14A ALNS benchmark acceptance | Dataset/fixture·seed/repeat·hardware/runtime·oracle·both-verifier·quality/resource·variance/replay bundle + independent acceptance receipt | `NOT_PRODUCED`; corpus/threshold/repeat/budget/variance는 `OPEN — EXPERIMENT_REQUIRED` | BLOCKED/GATED |
| Conditional Phase 12 evidence | Selected runtime이 substituted provider/runtime일 때 해당 `E-P12-*`, accepted review와 §18.3 bounded handoff | Current Phase 13 approved runtime selection 자체가 없고 Phase 12도 `NOT_STARTED/NOT_PRODUCED/NOT_READY` | CONDITIONAL; current universal entry blocker 아님 |
| `C-17` scope | Explicit approval record | 없음 | GATED |
| Hybrid meaning | Approved contract | 원문은 최소 boundary만 있고 product meaning 미확정 | CONTRACT_GATE |
| Route selection authority | Approved authority matrix | Backend 비권위는 고정, 요청/채택/rollout authority 미승인 | CONTRACT_GATE |
| Backend policy | Google OR-Tools direct CP-SAT | User decision and current canonical documents | FIXED_POLICY_ONLY |
| OR-Tools dependency/config | Exact version/checksum/workers/seed/time/gap | 없음 | GATED |
| OSS license/legal/native | Apache-2.0 + applicable notice/SBOM/redistribution/platform approval | OR-Tools license만 식별; distribution evidence 없음 | GATED |
| Operations | Retry/cancel/fallback/rollback runbook approval | 없음 | GATED |
| Security | Tenant/access/secret/log/native review | 없음 | GATED |
| Cost | Capacity/budget/cost authority | 없음 | GATED |
| ALNS-only baseline | Accepted representative quality/performance/replay evidence | Target ALNS implementation 없음 | BLOCKED |
| Scheduler/owners | Exact task ID and separated roles | `TBD_NOT_SUPPLIED` | OWNER_GATE |
| Phase 13 review | Canonical independent review | Document-contract review actual; implementation/evidence review 없음 | IMPLEMENTATION_REVIEW_GATE |

**현재 결론은 `C17_GATE_CLOSED`, `IMPLEMENTATION_NOT_STARTED`다.** 이 문서 review,
test fixture 설계와 future command 정의는 가능하지만 source/module/test/backend
구현을 시작했다고 표시하지 않는다.

## 5. Current repository inventory와 proposed change tree

### 5.1 Actual inventory

#### 5.1.1 Live document/review status

최종 정합성 감사에서 document/review registry를 다시 확인했다. 이 live status는
아래 작성 당시 code/build snapshot과 분리한다.

| 영역 | 현재 실제값 | Phase 13 해석 |
|---|---|---|
| Phase review inventory | Phase 00~14 review `15/15 COMPLETE` | 문서 review 완료이며 implementation/evidence/phase acceptance 완료가 아님 |
| Phase 12 | v1.3, review `COMPLETE_CHANGES_REQUIRED`, acceptance `BLOCKED_NOT_IMPLEMENTED`, handoff `NOT_READY` | Selected substituted runtime에만 conditional input; `C-17`/hybrid authority 없음 |
| Phase 14 | v1.4, review verdict `CHANGES_REQUIRED`, 14A `NOT_RUN`, 14B `NOT_STARTED/BLOCKED_NOT_READY`, handoff `NOT_READY` | Signed applicability consumer seam은 정렬됐지만 benchmark evidence와 official/production gate는 닫힘 |
| Phase 13 review | `COMPLETE`, `PASS_WITH_RESIDUAL_BLOCKERS`, document contract only | `C17_GATE_CLOSED`, implementation/evidence/activation 불변 |

#### 5.1.2 Historical authoring-time code/build snapshot

다음 code/build inventory는 문서 작성 당시인 2026-07-28 local checkout branch
`codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`의
historical characterization이다. 현재 live review registry나 phase acceptance를
추론하는 자료가 아니다. 당시 `docs/implementation/`은 shared checkout에서 untracked
문서 세트로 보여 파일별 상태와 내용을 직접 검사했다.

| 영역 | 실제 사실 | Phase 13 해석 |
|---|---|---|
| Maven | Root `pom.xml` 하나, `com.ronext:ro-next`, Java 25 | Multi-module `rpdptw-solver/application/verification` 없음 |
| Dependencies | Google Workflow, GCS, Jackson, JUnit가 root classpath | `com.google.ortools:ortools-java`와 Phase 13 adapter/module은 없음 |
| Main Java | `com.ronext.optimizer` 아래 6개 파일 | `com.ronext.rpdptw.*` target namespace 없음 |
| Engine | `AlnsBatchEngine`이 `SplittableRandom`으로 합성 `double objective` 생성 | RPDPTW/ALNS/route evaluation/pool/selection이 아님 |
| API | `Map<String,Object>`, `gs://`, 현재 시각/UUID와 numeric fallback | Approved hybrid request/config/authority boundary가 아님 |
| Worker/finalize | GCS prefix listing 뒤 raw objective 최소값 선택 | Declared completeness, independent verification, selection authority를 충족하지 않음 |
| Test | `AlnsBatchEngineTest` 1개 | Phase 13 gate/pool/oracle/fallback/security evidence가 아님 |
| Deployment | GCP Cloud Run/Workflows/GCS 자료 | Historical/current migration inventory; Phase 12/13 approval가 아님 |
| AWS | Tracked source/IaC 없음; ignored `.serverless` 생성물만 존재 | AWS 또는 route-selection backend evidence가 아님 |
| Phase 06/07 | Actual detail/review, implementation `NOT_STARTED`, phase `NOT_ACCEPTED` | Accepted predecessor artifact 없음 |
| Phase 12/14 authoring snapshot | 당시 actual detail은 있었으나 current review verdict는 아직 반영 전 | Live 값은 §5.1.1의 Phase 12 `CHANGES_REQUIRED`와 Phase 14 `CHANGES_REQUIRED/BLOCKED_NOT_READY`; approved substitution/official handoff 없음 |
| Route pool/selection | Source/type/test/backend 0 | Phase 13 구현 완료 0 |
| Evidence/review authoring snapshot | Accepted Phase 00~14 implementation/evidence bundle 0 | Live `15/15` document-review 완료와 구분; activation 및 completion 주장 금지 |

Actual code의 `parallelRuns=8`, `iterationsPerRun=5000`,
`System.nanoTime()`, prefix listing과 `objective`는 legacy characterization 값이다.
이를 pool cap, selector budget, traffic split, seed contract 또는 hybrid default로
재사용하지 않는다.

### 5.2 Gate-open 뒤의 proposed change tree

아래 tree는 **후보**다. Gate 전에는 만들지 않는다. Generic type/package 이름은
Phase 00/Architecture/C-17 review에서 바뀔 수 있지만 backend module boundary는
OR-Tools CP-SAT 격리를 나타낸다.

```text
rpdptw/
├── solver/
│   └── src/main/java/com/ronext/rpdptw/solver/
│       ├── pool/
│       │   ├── EvaluatedRouteArtifact.java
│       │   ├── RoutePoolDelta.java
│       │   ├── RoutePoolSnapshot.java
│       │   ├── RoutePoolCollector.java
│       │   └── RouteDominancePolicy.java
│       ├── selection/api/
│       │   ├── RouteSelectionSolverFactory.java
│       │   ├── RouteSelectionSession.java
│       │   ├── RouteSelectionModelSpec.java
│       │   ├── RouteSelectionOutcome.java
│       │   └── RouteSelectionBudget.java
│       ├── selection/projection/
│       └── selection/conversion/
├── application/
│   └── # scheduler-owned generic applicability/skip/rejection only; no solver edge
├── hybrid-application/
│   └── src/main/java/com/ronext/rpdptw/hybrid/application/
│       ├── Phase13AuthorizedScopeVerifier.java
│       ├── HybridRouteSelectionOrchestrator.java
│       ├── HybridPhaseRecord.java
│       └── Phase13Handoff.java
└── verification/
    # Phase 07 verifier를 재사용하며 Phase 13 전용 shortcut을 만들지 않음

adapters/
└── route-selection-ortools-cpsat/
    # C-17과 dependency/native/OSS-license/security/ops/cost 승인 뒤에만 생성

build/
├── test-fixtures/
│   └── phase13/
└── architecture-rules/

docs/implementation/evidence/phase-13/
    # 미래 immutable evidence location 후보; 현재 생성하지 않음
```

Dependency 후보:

```text
rpdptw-solver              → rpdptw-core
rpdptw-application         → rpdptw-core; ↛ solver/pool/backend
rpdptw-hybrid-application  → rpdptw-core + solver + verification
route-selection-ortools-cpsat → rpdptw-core + rpdptw-solver
verification              ↛ solver/search/pool/backend
core/solver/application    ↛ com.google.ortools
```

OR-Tools CP-SAT adapter는 route reconstruction, full evaluation, comparator,
candidate/result verification과 publication을 소유하지 않는다. Phase 12의 cloud
storage/workflow/compute adapter와 Phase 13 optimizer backend adapter는 서로 다른
축이며 한 module로 합치지 않는다.

Gate-closed canonical assembly는 `rpdptw-hybrid-application`, `rpdptw-solver`와
`route-selection-ortools-cpsat`를 dependency graph에 포함하지 않는다. Gate-open
scheduler disposition과 validated receipt가 있는 별도 assembly만 이 optional
module들을 포함할 수 있다.

## 6. I/O artifact, contract, identity와 lifecycle

### 6.1 경계별 I/O

| 경계 | Authority input | Output | 비권위/금지 입력 |
|---|---|---|---|
| Activation gate | Immutable approval/evidence refs, requested scope | Authorized scope 또는 typed rejection | Environment boolean, classpath presence |
| Route collection | Completed trial route, exact full evaluation, authority refs | `RoutePoolDelta` | Interrupted/partial route, stale cache only |
| Pool seal | Validated deltas, pins, explicit policy | Immutable stable `RoutePoolSnapshot` | Live mutable pool, unordered map iteration |
| Projection | Bound profile projection declaration, snapshot | Exact projected columns/model manifest or typed skip | Hidden Big-M/surrogate |
| Selection | Model, pool, warm start, explicit budget/backend/cancel | Provider-neutral outcome + selected IDs | Domain mutation, publication state |
| Materialization | Outcome IDs, exact pool/model, authority | Fresh route/bank draft | Pool alias, stale ID |
| Full evaluation | Fresh draft, problem/travel/profile | `EvaluatedSelectionCandidate` or typed failure | Backend feasibility/objective authority |
| Adoption | Candidate + cache-free ALNS incumbent + comparator | Adopted or retained candidate record | Traffic/completion order |
| Verification | Committed candidate + Phase 07 authority | Candidate/result PASS path or rejection | Search cache/backend claim |
| Handoff | Accepted review/evidence or closed applicability | Activated evidence handoff or skip receipt | Partial evidence/fake approval |

### 6.2 Artifact 후보와 ownership

| Artifact | Owner | 최소 내용 | Lifecycle |
|---|---|---|---|
| `Phase13ActivationReceipt` | Scheduler/control plane | 모든 authority/evidence ref와 allowed scope | Immutable; validation 전 사용 금지 |
| `RoutePoolDelta` | Solver pool | Admitted/rejected IDs, lineage, validation result | Append-by-new-artifact |
| `RoutePoolSnapshot` | Solver pool | Stable ordered artifacts, pins, policy, digest | Seal once; selector read-only |
| `RouteSelectionProjection` | Bound profile/projection | Supported rows/dimensions, units/ranges, fingerprint | Solve-bound immutable |
| `ProjectedRouteColumn` | Projection | Artifact ID, exact rows/coefficients, digest | Snapshot/model-scoped |
| `RouteSelectionModelManifest` | Selection API | Mode, row/column maps, coefficient proofs, identity | Immutable before backend |
| `MipWarmStartManifest` | Selection API | Selected/unassigned mapping and feasibility proof | Exact model-scoped |
| `RouteSelectionOutcome` | Backend adapter through API | Generic status/termination/provenance, incumbent presence와 selected route IDs only | Immutable; candidate 아님 |
| `MaterializationRecord` | Conversion/application | ID membership, fresh copy, exact partition | Immutable evidence |
| `EvaluatedSelectionCandidate` | Core evaluation/application | Fresh routes/bank, recomputed facts/objective, fingerprint | Adoption input only |
| `HybridPhaseRecord` | Application | ALNS/pool/model/backend/materialization/adoption/fallback lineage | Commit once |
| `Phase13EvidenceManifest` | Evidence owner | Test/oracle/security/OSS-license/SBOM/ops/cost/shadow refs | Content-addressed |
| `Phase13ApplicabilityReceipt` | Scheduler + Security/Release trust owner | Exact version/identity, signed envelope/trust/validity and action-time verification for `Skip`, or accepted `Activated` | Phase 14 entry control; actual signed receipt NOT_PRODUCED |
| `Phase13ActivatedHandoff` | Phase 13 owner/reviewer | Accepted review, `E-P13-*`, rollback point | Official hybrid candidate only |

`verified`라는 단어는 Phase 07 independent verifier 결과에만 사용한다.
Pool admission의 full evaluation 또는 backend status를 “verified route/result”라
부르지 않는다.

### 6.3 Identity와 fingerprint

```text
RouteSignature
  problemFingerprint
  preparedTravelFingerprint
  boundProfileFingerprint
  concreteVehicleId
  terminalPolicy
  orderedServiceVisitIds

RouteCoverageKey
  same authority fingerprints
  same concreteVehicleId
  exact RequestId set

RouteArtifactId
  RouteSignature digest
  authoritativeEvaluationFingerprint

ProjectedColumnId
  RouteArtifactId
  projectionFingerprint
  rowAndObjectiveCoefficientDigest

RoutePoolSnapshotId
  authority fingerprints
  pool policy fingerprint
  stable ordered RouteArtifactIds
  pins/dominance proof digests

RouteSelectionRunId
  solve/round/worker/hybrid phase identity
  pool/model/warm-start/backend/budget fingerprints
  requiredOrOptionalPolicy fingerprint
```

Fingerprint algorithm/version은 upstream serialization/identity ADR을 소비한다.
이 문서의 SHA-256 source fingerprint를 product artifact의 hidden hash default로
재사용하지 않는다.

Provider locator, native handle, elapsed time, completion order와
traffic assignment는 domain/result identity가 아니다. Backend version, thread,
seed, numeric/work configuration은 route-selection reproducibility identity에
포함할 수 있지만 secret value는 제외한다.

### 6.4 Candidate와 config lifecycle

```text
approval refs
→ validated Phase13ActivationReceipt
→ exact HybridExecutionPlan binding
→ ALNS incumbent committed
→ route delta commit
→ pool seal
→ projection/model/warm start freeze
→ backend session
→ outcome ID extraction
→ fresh materialization
→ full evaluation
→ strict adoption/retention
→ HybridPhaseRecord commit
→ worker committed candidate
→ Phase 07 both-gate verification
```

Config는 solve 시작 뒤 mutable registry/environment에서 다시 읽지 않는다.
Approval receipt, route-pool config, projection, backend config, budget, fallback
policy와 reproducibility class는 `HybridExecutionPlan`에 exact identity로 묶는다.

### 6.5 Phase 14의 canonical skip/activated handoff

Phase 14는 scheduler/control-plane module이 소유하는 다음 sum type과 동등한
applicability 판정을 소비한다. 이름은 proposed지만 의미 분리는 고정한다. 이
control record를 Phase 13 solver/backend module에 배치하거나 closed path에서 그
module을 load하는 것은 금지한다.

```java
// Proposed scheduler/Phase 14 control-plane references, not Phase 13 artifacts.
record SignedApplicabilityEnvelopeRef(
    ApplicabilityEnvelopeIdentity identity,
    Fingerprint canonicalEnvelopeDigest,
    SignatureProfileRef signatureProfile,
    TrustPolicyRef trustPolicy,
    ValidityWindow validityWindow,
    RevocationPolicyRef revocationPolicy
) {}

record ApplicabilityActionTimeVerificationRef(
    Fingerprint verificationReceiptDigest,
    Fingerprint verifierBuildDigest,
    Fingerprint trustRootSetDigest,
    RevocationSnapshotRef revocationSnapshot,
    FreshnessAuthoritySnapshotRef freshnessSnapshot,
    Phase14ActionScope checkedAction,
    Instant checkedAt,
    VerificationVerdict verdict
) {}

sealed interface Phase13ApplicabilityReceipt {
    record Skip(
        SkipReason reason,                 // C17_GATE_CLOSED
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

`decisionTime`은 scheduler observation일 뿐 applicability semantics나
`alnsOnlyPlanIdentity`에 포함하지 않는다. Skip identity는 reason, exact scheduler
authority/version과 ALNS-only plan identity에 결합한다. Signed envelope는 이 exact
identity와 canonical payload digest를 승인 scope에 묶는다. `checkedAt`도 observation이며
과거 `PASS`를 영구 권위로 만드는 identity가 아니다.

Phase 14가 manifest 생성, calibration, canary, activation 또는 pointer action에서
`Skip`을 소비할 때마다 approved verifier로 signature/trust policy, validity window,
scope/action, current trust-root set, revocation과 freshness authority snapshot을 다시
검증한다. 그 action-time 결과를 별도 immutable verification receipt로 남긴다.
Unsigned, unknown profile, wrong subject/scope/action, expired/not-yet-valid, revoked,
stale, unverifiable 또는 non-`PASS` 결과는
`UNTRUSTED_OR_INVALID_PHASE13_APPLICABILITY`로 fail closed하며 Phase 14 manifest/action과
Phase 13 load를 모두 0으로 유지한다.

정확한 signature algorithm, certificate/key format, trust root/store, revocation/time
authority, freshness window와 모든 숫자는 이 문서의 권위 밖이다. 이는
`OPEN/GATED_NOT_APPROVED`이며 Phase 14 `G14-SIGNING-TRUST`와 Security/Release 승인을
요구한다. Test signer, self-signed receipt, repository private key 또는 과거 verification
한 줄을 official applicability로 승격하지 않는다. Actual signed `Skip`/`Activated`
envelope와 action-time verification receipt는 모두 `NOT_PRODUCED`다.

`G14-SIGNING-TRUST`는 downstream Phase 14가 scheduler-owned applicability를 소비할
때의 gate다. 이를 Phase 13 implementation entry prerequisite나 Phase 13이 생산해야 할
evidence로 역전하지 않으며 `Phase 13 → Phase 14 → Phase 13` dependency cycle을 만들지
않는다.

`Activated` applicability도 Phase 14 consumption 전에 동등한 signed envelope와
action-time verification을 요구한다. Accepted Phase 13 handoff 자체가 signature/trust
gate를 대체하지 않는다.

Gate closed:

```text
C17_GATE_CLOSED
→ scheduler declares Phase 13 NOT_APPLICABLE for this Phase 14 path
→ scheduler issues exact signed applicability envelope under an approved trust policy
→ Phase 14 action-time verifier checks signature/scope/validity/revocation/freshness
→ no RoutePoolDelta/Snapshot
→ no selection model/outcome
→ no HybridPhaseRecord
→ no E-P13 implementation evidence claim
→ Phase 13 module/provider/service discovery/class loading count 0
→ only a current PASS verification receipt allows Phase 14 to continue as ALNS-only
  if every other Phase 14 gate passes
```

이 skip receipt는 Phase 13 구현 산출물이 아니라 scheduler-owned applicability
control record/evidence다. Phase 13을 `ACCEPTED`로 만들지 않으며 `E-P13-*`로
이름 붙이지 않는다. Signed envelope 또는 action-time verification이 없거나 실패하면
closed-path 의미를 ALNS-only authority로 추정하지 않고 Phase 14를 fail closed한다.

Gate open:

```text
accepted Phase13 implementation/evidence review
+ E-P13-GATE/POOL/SELECTION/HYBRID
+ security/OSS-license/SBOM/native/ops/cost evidence
+ shadow/rollback evidence
+ exact official-hybrid manifest selection
= Phase13ActivatedHandoff
```

Scheduler가 gate-open applicability와 validated activation receipt를 확인한 뒤에만
Phase 13 assembly를 구성하고 `Activated`를 만들 수 있다. Phase 14는 이
`Activated` applicability도 action-time signature/trust/validity/revocation/freshness
검증 후에만 소비한다. `Activated` receipt가 있어도 Phase 14의 `Q-BENCH-02`,
compliant integer
travel, Phase 11, production authority와 다른 gate를 우회하지 않는다.

## 7. Proposed Java 25 contract와 dependency

이 절의 이름/signature/config는 모두 **PROPOSED/OPEN INTERNAL**이다. 승인된
public API, wire schema, backend implementation/config approval 또는 gate 통과 증거가 아니다.

### 7.1 Scheduler applicability, authorized scope와 plan

```java
// Scheduler/control-plane owned; it has no dependency on the Phase 13 assembly.
public sealed interface SchedulerPhase13Disposition {
    record Skip(Phase13ApplicabilityReceipt.Skip receipt)
            implements SchedulerPhase13Disposition {}
    record Reject(UnauthorizedHybridActivation failure)
            implements SchedulerPhase13Disposition {}
    record Open(Phase13ActivationReceipt receipt)
            implements SchedulerPhase13Disposition {}
}

// Phase 13 assembly owned and callable only after SchedulerPhase13Disposition.Open.
public interface Phase13AuthorizedScopeVerifier {
    ValidatedPhase13Scope verifyOpenScope(
        HybridActivationRequest request,
        Phase13ActivationReceipt receipt,
        RequiredHybridAuthorityEvidence evidence
    );
}

public record HybridExecutionPlan(
    HybridPlanVersion version,
    HybridApplicabilityScope scope,
    RouteCollectionPolicy poolPolicy,
    RouteSelectionProjectionRef projection,
    RouteSelectionBackendRef backend,
    RouteSelectionBudget budget,
    SelectorRequirementPolicy requirementPolicy,
    HybridReproducibilityClass reproducibilityClass,
    FallbackPolicy fallbackPolicy,
    Fingerprint fingerprint
) {}
```

Scheduler/control plane은 `HybridActivationRequest`가 없고 gate가 닫힌 경우에만
`Skip`을 만들며 Phase 13 assembly를 resolve하지 않는다. 명시적 request와 closed
gate/receipt 부재는 scheduler 단계의 `Reject`다. `Open`만
`Phase13AuthorizedScopeVerifier` 호출을 허용한다. `backend`, `budget`, `scope`,
`requirementPolicy`를 생략했을 때 runtime default로 채우지 않는다.

### 7.2 Pool과 projection

```java
public interface RoutePoolCollector {
    RoutePoolDelta collect(
        CompletedTrialProjection completedTrial,
        RouteAdmissionContext authority
    );
}

public interface RoutePool {
    RoutePoolAppendResult append(RoutePoolDelta delta);
    RoutePoolAppendResult importDelta(RoutePoolDelta delta);
    RoutePoolSnapshot seal(RoutePoolSealRequest request);
}

public interface RouteSelectionProjector {
    RouteProjectionResult project(
        BoundProfileProjectionDeclaration declaration,
        RoutePoolSnapshot snapshot
    );
}

public sealed interface RouteProjectionResult {
    record Exact(
        RouteSelectionProjection projection,
        RouteSelectionModelSpec model
    ) implements RouteProjectionResult {}

    record SkippedNonProjectable(
        NonProjectableReport report
    ) implements RouteProjectionResult {}

    record Rejected(
        ProjectionIntegrityFailure failure
    ) implements RouteProjectionResult {}
}
```

`CompletedTrialProjection`은 solver의 mutable trial/cache를 넘기는 type이 아니라
canonical route와 full evaluation/authority identity만 담는 handoff projection
후보다.

### 7.3 Backend-neutral selection session과 OR-Tools isolation

```java
public interface RouteSelectionSolverFactory {
    BackendCapability capability();
    RouteSelectionSession openSession(ApprovedBackendConfig config);
}

public interface RouteSelectionSession extends AutoCloseable {
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
```

`ApprovedBackendConfig`, budget와 backend capability key는 activation receipt의
allowed scope와 exact match해야 한다. `route-selection-ortools-cpsat`만
`com.google.ortools:ortools-java:${ortools.version}`와
`com.google.ortools.sat` API를 참조한다. `${ortools.version}`, artifact checksum,
supported OS/architecture, `num_workers`, `random_seed`, wall/deterministic time와
gap 값은 모두 `OPEN/GATED_NOT_APPROVED`다. `MPSolver`나 `MPSolver("SAT")`를
중간 계층으로 사용하지 않는다.

Direct CP-SAT surface는 `Loader.loadNativeLibraries()`, `CpModel`, `BoolVar`,
`LinearExpr`, `CpSolver`, `CpSolverStatus`, `SatParameters`로 제한한다. 모델은
Boolean 변수와 checked int64 fixed-point 계수만 만들며 rounding/scale/overflow
proof가 없으면 `MODEL_BUILD_FAILED`로 종료한다.

이 결정의 최신 기술 사실은 Google 공식 자료만으로 확인했다.

- [MIP solver 선택 가이드](https://developers.google.com/optimization/mip#which_solver_should_i_use)와
  [integer-only CP-SAT 계약](https://developers.google.com/optimization/cp/cp_solver):
  현재 0-1 pure integer model에는 direct CP-SAT가 canonical이다.
- [Java 설치 문서](https://developers.google.com/optimization/install/java):
  Maven coordinate는 `com.google.ortools:ortools-java`; 예시/latest version은
  project 승인 pin이 아니므로 `${ortools.version}`은 `OPEN`이다.
- [CP-SAT status 정의](https://github.com/google/or-tools/blob/stable/ortools/sat/cp_model.proto)와
  [Java `CpSolver`](https://or-tools.github.io/docs/java/classcom_1_1google_1_1ortools_1_1sat_1_1CpSolver.html):
  solution 값은 `OPTIMAL`/`FEASIBLE`에서만 읽고 cancellation은 `stopSearch()`로 전달한다.
- [`SatParameters`](https://github.com/google/or-tools/blob/stable/ortools/sat/sat_parameters.proto):
  time, workers, seed, deterministic-time와 gap은 명시적으로 bind한다. Library
  default나 hardware core count를 production policy로 상속하지 않는다.
- [`Loader` source](https://or-tools.github.io/docs/java/ortools_2java_2com_2google_2ortools_2Loader_8java_source.html)와
  [OR-Tools license](https://github.com/google/or-tools/blob/stable/LICENSE):
  native resource fallback extraction/`deleteOnExit()`와 Apache-2.0은 확인했지만
  supported-platform packaging과 bundled/transitive notice/SBOM evidence는 여전히 gate다.

### 7.4 Outcome, materialization과 adoption

```java
public enum RouteSelectionStatus {
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

public record RouteSelectionOutcome(
    RouteSelectionStatus status,
    IncumbentPresence incumbentPresence,
    List<ProjectedColumnId> selectedColumns,
    RouteSelectionTerminationCause terminationCause,
    RouteSelectionRunId runId,
    Fingerprint modelFingerprint,
    Fingerprint poolFingerprint,
    Fingerprint outcomeFingerprint
) {}

public interface RouteSelectionMaterializer {
    MaterializationResult materialize(
        RouteSelectionOutcome outcome,
        RouteSelectionModelSpec model,
        RoutePoolSnapshot pool,
        ImmutableProblemAuthority authority
    );
}

public interface HybridCandidateAdopter {
    HybridAdoptionDecision compareAndAdopt(
        CacheFreeValidatedAlnsIncumbent incumbent,
        EvaluatedSelectionCandidate selectorCandidate,
        BoundObjectiveComparator comparator
    );
}
```

CP-SAT mapping은 `OPTIMAL → OPTIMAL`, `FEASIBLE → FEASIBLE_LIMIT`,
`INFEASIBLE → PROVEN_INFEASIBLE`, `MODEL_INVALID → MODEL_INVALID`,
`UNKNOWN → NO_INCUMBENT_LIMIT`이다. Native/JNI load failure는 raw enum 밖의
`NATIVE_RUNTIME_UNAVAILABLE`이다. `OPTIMAL`/`FEASIBLE`만 incumbent가 있으며 이
두 상태에서만 Boolean 값을 읽어 selected IDs를 만든다. 다른 상태에서 value,
objective, bound를 읽는 것은 계약 위반이다.

Time limit, caller cancellation과 resource termination은 raw status에서 추측하지
않고 `RouteSelectionTerminationCause`로 별도 기록한다. `CpSolver.objectiveValue()`
는 `double`이므로 outcome/adoption authority에 넣지 않는다. Unassigned set은
selected columns의 coverage complement로 materialization 경계가 재구성한다.
Outcome constructor/decoder가 status × incumbent field admissibility를 검증해야 한다.

### 7.5 Application orchestration boundary

```java
public interface HybridRouteSelectionOrchestrator {
    HybridExecutionResult execute(
        ValidatedPhase13Scope scope,
        HybridExecutionPlan plan,
        CacheFreeValidatedAlnsIncumbent incumbent,
        CompletedAlnsSegment segment,
        ImmutableProblemAuthority authority,
        SolverCancellationProbe cancellation
    );
}

public sealed interface HybridExecutionResult {
    record Committed(
        CommittedCandidate candidate,
        HybridPhaseRecord record
    ) implements HybridExecutionResult {}

    record Incomplete(
        HybridPhaseFailure failure,
        Fingerprint unchangedIncumbent
    ) implements HybridExecutionResult {}
}
```

Gate decision이 `Skip`이면 이 orchestrator를 호출하지 않는다. `Reject`이면
solve 준비 단계에서 종료한다. `Committed` candidate는 아직 verified/publishable
result가 아니며 Phase 07 경계로 전달한다.

## 8. State transition, pseudocode와 failure/rollback

### 8.1 Gate state

```text
DOCUMENTED_GATED
├─ no request
│  → NOT_APPLICABLE_GATE_CLOSED
│  → PHASE14_SKIP_RECEIPT
├─ explicit request + missing/invalid receipt
│  → UNAUTHORIZED_OR_INCOMPLETE_ACTIVATION
│  → REJECTED_BEFORE_ALLOCATION
└─ explicit request + complete validated receipt
   → READY_FOR_IMPLEMENTATION
   → implementation/evidence/review
   → ACCEPTED_FOR_APPROVED_SCOPE
```

Receipt validation 전에는 pool memory, native loader/session, backend
client와 selection run ID를 만들지 않는다.

### 8.2 Gate-open hybrid execution

```text
ALNS_INCUMBENT_COMMITTED
→ ROUTE_COLLECTION_COMPLETED
→ POOL_SEALED
├─ PROJECTION_SKIPPED
│  ├─ optional → INCUMBENT_RETAINED → HYBRID_COMMITTED
│  └─ required → HYBRID_INCOMPLETE
└─ MODEL_FROZEN
   → RUNTIME_ADMISSION_GRANTED
   → NATIVE_RUNTIME_READY
   → SESSION_OPENED
   → SELECTION_FINISHED
   ├─ NO_INCUMBENT / FAILED / CANCELLED
   │  ├─ optional and clean → INCUMBENT_RETAINED → HYBRID_COMMITTED
   │  └─ required or contaminated → HYBRID_INCOMPLETE
   └─ INCUMBENT_IDS_RETURNED
      → IDENTITY_VALIDATED
      → FRESH_MATERIALIZED
      → EXACT_PARTITION_VALIDATED
      → FULL_EVALUATED
      ├─ STRICTLY_BETTER → SELECTOR_ADOPTED → HYBRID_COMMITTED
      └─ EQUAL/WORSE/INVALID → INCUMBENT_RETAINED → HYBRID_COMMITTED
```

`CpSolver.stopSearch()` cancellation registration과 solve-local reference는
`finally`에서 해제한다. OR-Tools JNI load는 JVM lifetime이고 per-solve native
environment unload API가 없으므로 commercial-optimizer-style model/environment/lease close 순서를
만들지 않는다.

### 8.3 Pseudocode

```text
# scheduler/control plane; no Phase 13 dependency or class loading
disposition = scheduler.decidePhase13Applicability(request, controlPlaneAuthority)

if disposition is Skip:
    assert phase13AssemblyResolutionCount == 0
    assert phase13ClassLoadCount == 0
    assert backendDiscoveryCount == 0
    return runCanonicalAlnsOnlyPathAndAttachSchedulerSkip(disposition.receipt)

if disposition is Reject:
    assert phase13AssemblyResolutionCount == 0
    return rejectBeforeSolve(UNAUTHORIZED_HYBRID_ACTIVATION)

# only SchedulerPhase13Disposition.Open may cross the Phase 13 assembly boundary
scope = authorizedScopeVerifier.verifyOpenScope(
    request,
    disposition.receipt,
    requiredHybridAuthorityEvidence
)
assert plan.fingerprint is explicitly bound to scope
assert no open config value is omitted

pre = fingerprint(incumbent)
delta = collectOnlyCompletedHardFeasibleRoutes(segment, authority)
poolAfterAppend = pool.append(delta)
snapshot = poolAfterAppend.seal(stableOrder, incumbentPins, approvedPolicy)

projection = projector.project(boundProfileDeclaration, snapshot)
if projection is typed skip:
    return optionalRetainOrRequiredIncomplete(plan, pre, projection)
if projection is rejected:
    return integrityFailureWithoutMutation(pre, projection)

model = freezeModel(projection, snapshot, explicitConfig)
warmStart = buildAndValidateWarmStart(incumbent, model, snapshot)

session = null
try:
    requireRuntimeAdmission(plan, remainingOverallBudget)
    ensureOrToolsNativeRuntimeLoadedAndSmokeTested(plan.backend.config)
    session = factory.openSession(plan.backend.config)
    outcome = session.solve(model, snapshot, warmStart, plan.budget, cancellation)
finally:
    clearCancellationRegistrationAndSolveReferences(session)
    closeJavaSession(session)

if outcome has no admissible incumbent:
    return optionalRetainOrRequiredIncomplete(plan, pre, outcome)

assert outcome.poolFingerprint == snapshot.fingerprint
assert outcome.modelFingerprint == model.fingerprint
assert every selected ID belongs to model and snapshot

draft = materializeFresh(outcome.selectedIds, snapshot, authority)
assert exactRequestAndConcreteVehiclePartition(draft)
evaluated = fullPropagateAndEvaluate(draft, authority)

if evaluated invalid or fingerprint diverges:
    return optionalRetainOrRequiredIncompleteWithoutMutation(plan, pre, evaluated)

decision = comparator.compare(evaluated, incumbent)
champion = decision == STRICTLY_BETTER ? freshCommit(evaluated) : incumbent
record = commitHybridRecord(allLineage, decision, fingerprint(champion))

assert fingerprint(incumbent) == pre unless selector was strictly better
return Committed(champion, record)
```

### 8.4 Failure, rollback과 last safe point

| Failure | Classification | Optional action | Required action | Rollback assertion |
|---|---|---|---|---|
| Unauthorized activation | Gate failure | Solve precheck reject | Same | No Phase 13 allocation/artifact |
| Partial/stale receipt | Gate integrity | Reject | Reject | ALNS path not started under invalid plan |
| Stale/cross-authority route | Pool integrity | Discard delta; investigate | Incomplete | Incumbent/pool previous snapshot unchanged |
| Same signature/different evaluation | Corruption | Fail closed | Fail closed | No arbitrary winner |
| Non-projectable profile | Typed skip | Retain incumbent if approved | Incomplete | No surrogate |
| Backend unavailable | Capability | Retain if policy allows | Incomplete | No fake outcome |
| Native runtime unavailable | JNI/platform/packaging | Retain if policy allows; alert | Incomplete | No outcome/value read |
| No incumbent limit | Selection | Retain | Incomplete | Selected attributes unread |
| Model invalid/build overflow | Defect/integrity | Only clean fallback; investigate | Incomplete | Model/pool immutable |
| Cancel | Cancellation | No normal success conversion | Incomplete/cancelled | Draft/session discarded |
| Stale/partial selected IDs | Integrity | Reject outcome | Incomplete | No materialization commit |
| Materialization/full-eval divergence | Integrity/defect | Retain if uncontaminated | Incomplete | Pool/incumbent fingerprints unchanged |
| Equal/worse candidate | Normal non-adoption | Retain | Retain unless approved policy says otherwise | Next warm start exact incumbent |
| Nondeterministic fixed-envelope selection | Repro failure | No strong claim; rollback activation | Same | Last accepted ALNS-only manifest |
| Security/OSS-license/SBOM/cost breach | Operational gate | Disable applicable scope, rollback | Same | Accepted ALNS-only distribution/plan |

Last safe point는 항상 **accepted ALNS-only candidate/manifest/build와 Phase 07
both-gate path**다. Phase 13 rollback은 pool/selection artifacts를 삭제하거나
덮어쓰는 것이 아니라 activated plan/assembly를 비선택 상태로 되돌리고 이전
immutable ALNS-only pointer/manifest를 사용한다.

## 9. Exact future-red test plan

모든 class/method 이름은 proposed internal test name이다. Fixture 수치는
`TEST_ONLY` manifest에만 존재하며 production default가 아니다.

### 9.1 Fixture와 independent oracle

| Fixture | 내용 | Oracle |
|---|---|---|
| `P13_GATE_CLOSED_TEST_ONLY` | Receipt/activation 모두 없음 | Branch absence, Phase 13 artifact count 0 |
| `P13_UNAUTHORIZED_REQUEST_TEST_ONLY` | Explicit request, receipt 없음 | Pre-allocation typed rejection |
| `P13_PARTIAL_RECEIPT_TEST_ONLY` | Required cost ref 또는 selected substituted runtime의 applicable Phase 12 evidence ref 누락 | Missing-field exact list |
| `P13_STALE_RECEIPT_TEST_ONLY` | Old profile/scope/source fingerprint | Stale receipt rejection |
| `P13_TINY_EXACT_4R_2V_TEST_ONLY` | 3 request, 2 concrete vehicle, small route set, explicit unassigned | Exhaustive subset enumeration + hand lexicographic vector |
| `P13_NON_PROJECTABLE_TEST_ONLY` | Solution-level nonadditive hard rule | Typed non-projectable skip |
| `P13_STALE_POOL_TEST_ONLY` | Same IDs, changed profile/travel fingerprint | Integrity rejection before backend |
| `P13_PARTIAL_CANDIDATE_TEST_ONLY` | Pickup only/route-bank duplicate/interrupted trial | Pool admission rejection |
| `P13_COLUMN_DIVERGENCE_TEST_ONLY` | Row/coefficient digest differs from model | Projection integrity failure |
| `P13_STALE_SELECTED_ID_TEST_ONLY` | Outcome column not in frozen model/snapshot | No materialization |
| `P13_ALIAS_ATTACK_TEST_ONLY` | Backend/converter attempts pool route mutation | Snapshot/incumbent byte/fingerprint unchanged |
| `P13_NATIVE_LOAD_FAILURE_TEST_ONLY` | Unsupported/missing/corrupt JNI resource | `NATIVE_RUNTIME_UNAVAILABLE`, no value read |
| `P13_BACKEND_ABSENT_TEST_ONLY` | OR-Tools-free ALNS-only assembly | `BACKEND_UNAVAILABLE`, default build green |
| `P13_SELECTION_TIE_TEST_ONLY` | Multiple equal objective combinations | Stable explicit tie rule or no strong replay claim per approved contract |
| `P13_MATERIALIZATION_DIVERGENCE_TEST_ONLY` | Backend coefficient claim differs from full evaluator | Candidate rejected, incumbent preserved |
| `P13_SECURITY_CROSS_TENANT_TEST_ONLY` | Wrong tenant artifact/runtime scope | Access denied before read/solve |
| `P13_TIMEBOXED_POST_OPTIMIZE_RETRY_TEST_ONLY` | Optimize began then timebox | No same logical result-bearing retry |
| `P13_PHASE14_SKIP_TEST_ONLY` | C-17 closed, all other Phase 14 prerequisites simulated | ALNS-only predecessor accepted without P13 artifacts |
| `P13_UNSIGNED_OR_STALE_SKIP_TEST_ONLY` | Unsigned, unknown-profile, wrong-scope, expired, revoked와 stale verification variants | Phase 14 action fail closed; Phase 13 load and manifest/action count 0 |

`TinyRouteSelectionOracle`은 production projector, backend, comparator helper 또는
serialization code를 호출하지 않는다. Stable `RouteArtifactId` 집합의 모든 subset과
explicit \(u_i\) 조합을 열거하고 request exact partition, concrete vehicle
consumption과 hand-authored lexicographic vector를 계산한다.

### 9.2 Gate/absence/security test

| Class | Exact method | Expected |
|---|---|---|
| `Phase13SchedulerApplicabilityTest` | `closedGateWithoutRequestUsesCanonicalAlnsPathWithoutLoadingPhase13` | Scheduler `Skip(C17_GATE_CLOSED)`; Phase 13 resolution/class-load count 0 |
| `Phase13SchedulerApplicabilityTest` | `closedGateExplicitRequestRejectsBeforePhase13Resolution` | `UNAUTHORIZED_HYBRID_ACTIVATION`; Phase 13 allocation count 0 |
| `Phase13AuthorizedScopeVerifierTest` | `partialReceiptListsEveryRequiredAuthority` | Missing required cost/etc exact |
| `Phase13AuthorizedScopeVerifierTest` | `selectedSubstitutedRuntimeRequiresApplicablePhase12Evidence` | Missing selected-runtime evidence exact |
| `Phase13AuthorizedScopeVerifierTest` | `baselineRuntimeDoesNotInventPhase12Prerequisite` | Phase 12 ref absent and no Phase 12 blocker |
| `Phase13AuthorizedScopeVerifierTest` | `staleReceiptCannotAuthorizeChangedScope` | No pool/backend allocation |
| `Phase13AuthorizedScopeVerifierTest` | `allApplicableAuthoritiesAndEvidenceAreConjunctive` | One required field removal always rejects |
| `Phase13Phase14SkipContractTest` | `closedGateCreatesSchedulerSkipWithoutPhase13Artifacts` | Artifact/evidence refs absent |
| `Phase13Phase14SkipContractTest` | `skipDoesNotMarkPhase13Accepted` | Applicability skip only |
| `Phase13Phase14SkipContractTest` | `unsignedSkipCannotAuthorizePhase14Action` | `UNTRUSTED_OR_INVALID_PHASE13_APPLICABILITY`; manifest/action 0 |
| `Phase13Phase14SkipContractTest` | `expiredRevokedStaleOrWrongScopeSkipFailsAtActionTime` | Current verification non-`PASS`; Phase 13 load 0 |
| `Phase13Phase14SkipContractTest` | `pastPassCannotBypassCurrentRevocationOrFreshnessPolicy` | Action-time re-verification required |
| `HybridSecurityContractTest` | `crossTenantPoolReadIsDeniedBeforeBackendOpen` | Backend open count 0 |
| `HybridSecurityContractTest` | `providerSecretsNeverAppearInArtifactLogOrFingerprint` | Redaction/allowlist exact |
| `HybridArchitectureTest` | `defaultReactorHasNoOrToolsDependencyOrServiceDiscoveryActivation` | `com.google.ortools` refs 0 |
| `HybridArchitectureTest` | `closedCanonicalAssemblyHasNoPhase13SolverPoolBackendOrProviderEdge` | Phase 13 assembly/provider edge 0 |

### 9.3 Pool/identity/determinism test

| Class | Exact method | Fixture/oracle | Expected |
|---|---|---|---|
| `RoutePoolAdmissionTest` | `admitsRejectedSolutionRouteOnlyWhenTrialCompletedAndHardFeasible` | Completed/rejected pair | Valid delta |
| `RoutePoolAdmissionTest` | `rejectsInterruptedPartialAndStaleCandidate` | Partial/stale fixtures | Exact typed failures |
| `RoutePoolAdmissionTest` | `sameSignatureDifferentEvaluationIsIntegrityFailure` | One-field evaluation corruption | No arbitrary tie |
| `RoutePoolMergePropertyTest` | `appendImportReloadProduceSameSnapshotDigestForEveryOrder` | Permutation/property | One digest |
| `RoutePoolMergePropertyTest` | `crossFingerprintImportNeverMerges` | Different profile/travel | Rejection |
| `RoutePoolAliasTest` | `snapshotAndIncumbentRemainByteIdenticalAfterConversionFault` | Alias attack | Exact unchanged |
| `RoutePoolDominanceOracleTest` | `unsafeScalarDominanceNeverDropsParetoRoute` | Hand Pareto set | Conservative retention |
| `RoutePoolPinTest` | `incumbentArtifactsSurviveApprovedPruningPolicy` | Pinned fixture | All pins present |
| `RoutePoolDeterminismTest` | `parallelCompletionOrderDoesNotChangeSnapshotEnumerationOrFingerprint` | Permuted completions | Exact stable bytes |

### 9.4 Projection/selection/oracle test

| Class | Exact method | Fixture/oracle | Expected |
|---|---|---|---|
| `RouteSelectionProjectionTest` | `nonProjectableDimensionReturnsTypedSkipWithoutSurrogate` | Non-projectable fixture | Exact skip |
| `RouteSelectionProjectionTest` | `requestUnassignedAndConcreteVehicleRowsRoundTripExactly` | Tiny exact | Hand coefficients |
| `RouteSelectionProjectionTest` | `coefficientOverflowOrLossyRoundTripIsRejected` | Boundary long | No backend call |
| `TinyExactRouteSelectionOracleTest` | `backendOutcomeMatchesExhaustiveOptimumAndStableIds` | Tiny exhaustive oracle | Exact lexicographic optimum |
| `CpSatAdapterContractTest` | `usesDirectSatApiWithoutMPSolver` | Bytecode/dependency scan | `com.google.ortools.sat` only; `MPSolver` refs 0 |
| `CpSatAdapterContractTest` | `onlyOptimalOrFeasibleReadBooleanValues` | CP-SAT status matrix | Unsafe value/objective read 0 |
| `CpSatAdapterContractTest` | `looseGapCannotClaimExactOptimality` | Test-only gap profile | Exact profile bind/solve rejected |
| `LexicographicSelectionTest` | `feasibleLimitDoesNotOptimizeLowerPriorityDimension` | Staged fake backend | Lower stage not called |
| `WarmStartContractTest` | `warmStartMustReferenceEveryPinnedIncumbentColumnAndExactBank` | Tiny exact | Exact row feasibility |
| `SelectionOutcomeContractTest` | `noIncumbentStatusesNeverReadSelectedOrObjectiveAttributes` | Status×incumbent matrix | Unsafe getter call 0 |
| `SelectionOutcomeContractTest` | `staleSelectedIdIsRejectedBeforeMaterialization` | Stale selected ID | Draft count 0 |
| `BackendLifecycleTest` | `nativeLoadFailureDiffersFromBackendAssemblyAbsence` | Two fixtures | Distinct status |
| `BackendLifecycleTest` | `cancellationCallsStopSearchAndClearsSolveReferences` | Fault/race matrix | No retained callback/reference |
| `BackendLifecycleTest` | `alnsOnlyAssemblyNeverLoadsNativeLibraries` | Default assembly | Loader call count 0 |

### 9.5 Materialization/divergence/fallback/repro test

| Class | Exact method | Fixture/oracle | Expected |
|---|---|---|---|
| `RouteSelectionMaterializationTest` | `selectedArtifactsAreClonedIntoFreshExactPartition` | Tiny exact | No alias, exact bank |
| `RouteSelectionMaterializationTest` | `unassignedSetIsRebuiltAsSelectedCoverageComplement` | Tiny exact + corrupted fake side-channel | Side-channel ignored; exact complement |
| `HybridAdoptionTest` | `strictlyBetterCandidateIsOnlyAdoptableCandidate` | Better/equal/worse | Adopt count 1 |
| `HybridAdoptionTest` | `rawBackendObjectiveNeverOverridesFullComparator` | Divergence fixture | Incumbent retained |
| `HybridFallbackTest` | `optionalNoIncumbentPreservesIncumbentAndNextWarmStartFingerprint` | No incumbent | Exact pre/post equality |
| `HybridFallbackTest` | `requiredBackendFailureIsIncompleteNotDegradedSuccess` | Backend absent | `INCOMPLETE` |
| `HybridFallbackTest` | `staleOrPartialCandidateCannotPoisonPoolOrIncumbent` | Partial/stale | All pre fingerprints exact |
| `HybridRetryTest` | `timeboxedOptimizeStartForbidsSameLogicalResultBearingRetry` | Post-optimize timebox | New run required |
| `HybridReproducibilityTest` | `fixedStrongEnvelopeRepeatsPoolModelOutcomeDecisionAndCandidateFingerprints` | Deterministic fake | Exact trace/result |
| `HybridReproducibilityTest` | `nondeterministicBackendCannotClaimStrongReplay` | Permuted fake | Classification failure |
| `HybridPhase07BoundaryTest` | `adoptedCandidateStillRequiresCandidateAndResultVerifierPass` | Corrupted result | Publication blocked |
| `HybridRollbackTest` | `securitySupplyChainOrCostTripRestoresAcceptedAlnsOnlyPlan` | Operational fault | Prior plan/pointer |

### 9.6 Performance/cost tests without hidden threshold

| Class | Exact method | Measurement | Pass rule |
|---|---|---|---|
| `RoutePoolWorkAccountingTest` | `reportsAdmissionMergeSealBytesAndPeakMemoryWithoutChangingSelection` | Route count/bytes/RSS/work | Counters complete; no quality input |
| `RouteSelectionWorkAccountingTest` | `separatesQueueAdmissionBuildSolveMaterializeAndFullEvaluationWork` | Phase work counters | All declared stages accounted |
| `HybridExperimentManifestTest` | `requiresExplicitTestOrExperimentBudgetAndScope` | Missing each field | Bind failure |
| `HybridCostAuthorityTest` | `unapprovedBudgetOrCapacityCannotOpenBackend` | Cost approval absent | Backend open 0 |
| `HybridTrafficAuthorityTest` | `missingTrafficSplitAuthorityCannotSelectHybridPopulation` | No rollout approval | No implicit split |

성능/비용 “통과 수치”는 현재 없다. 위 test의 pass는 계측 누락·hidden threshold·
권위 없는 activation이 없다는 뜻이다. Go-live threshold는 measured experiment와
explicit approval 뒤 별도 manifest/version으로 추가한다.

### 9.7 Red → green 순서

| 순서 | Red condition | Green 조건 | Layer |
|---:|---|---|---|
| 1 | Gate closed인데 branch/backend가 호출됨 | Absence 0 artifact + unauthorized fail closed | Unit/application |
| 2 | Partial/stale receipt가 통과 | 모든 authority/evidence conjunctive | Contract/security |
| 3 | Invalid/stale route가 pool에 들어감 | Admission/identity fail closed | Solver unit/property |
| 4 | Merge order에 따라 digest 변화 | Append/import/reload stable exact | Property/repro |
| 5 | Non-projectable 의미가 사라짐 | Typed skip, surrogate 0 | Projection contract |
| 6 | Tiny model이 oracle와 불일치 | Exact rows/vector/optimum | Independent oracle |
| 7 | Status가 없는 incumbent attribute를 읽음 | Status×incumbent safe matrix | Adapter contract |
| 8 | Selected ID가 pool alias를 mutate | Fresh clone + exact partition | Conversion/fault |
| 9 | Backend objective가 comparator를 우회 | Full evaluate + strict adoption | Core/application |
| 10 | Failure가 incumbent를 바꿈 | Optional exact fallback / required incomplete | Fault/rollback |
| 11 | Fixed envelope가 nondeterministic인데 strong claim | Exact replay or declared weaker class | Repro |
| 12 | OR-Tools/secret/tenant/cost authority leakage | Architecture/security/OSS-license/SBOM/cost green | Root/integration |
| 13 | Phase 07 bypass 또는 skip handoff 불명확 | Both-gate + Phase 14 conditional contract | E2E/handoff |
| 14 | Evidence/review 일부만 있음 | Required test skipped 0 + immutable bundle + independent implementation/evidence review PASS | Review |

### 9.8 Future verification commands와 pass criteria

Target module/profile 이름은 proposed다. Exact reactor가 Phase 00에서 승인되면
명령을 그 이름에 맞춰 freeze한다.

```bash
./mvnw -f rpdptw/application/pom.xml clean test \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest=Phase13SchedulerApplicabilityTest,Phase13Phase14SkipContractTest

./mvnw -f rpdptw/solver/pom.xml clean test \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest=RoutePoolAdmissionTest,RoutePoolMergePropertyTest,RoutePoolDeterminismTest

./mvnw -f rpdptw/solver/pom.xml clean test \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest=RouteSelectionProjectionTest,TinyExactRouteSelectionOracleTest,SelectionOutcomeContractTest,WarmStartContractTest

./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest=Phase13AuthorizedScopeVerifierTest,RouteSelectionMaterializationTest,HybridAdoptionTest,HybridFallbackTest,HybridRetryTest

./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest=HybridReproducibilityTest,HybridPhase07BoundaryTest

./mvnw -f build/architecture-rules/pom.xml clean test \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dtest=HybridArchitectureTest,HybridSecurityContractTest

./mvnw clean verify
```

각 selected run은 해당 owner module의 fresh Surefire XML에서 expected class/method가
모두 정확히 한 번 이상 실행됐고 discovered count가 non-zero이며
failure/error/skipped가 모두 0임을 fail closed로 대조한다. 이름 오타, report 누락,
disabled test 또는 empty selection은 exit code 0이어도 evidence가 아니다.

Gate가 열리고 dependency/native/OSS-license 승인이 난 뒤에만 isolated OR-Tools profile에서 다음 범주의 exact
command를 approval record에 고정한다.

```text
./mvnw -P<approved-phase13-ortools-integration-profile> \
  -pl adapters/route-selection-ortools-cpsat -am verify
```

Pass criteria:

- Required test failed/error/skipped `0`
- Gate-closed backend open/native-load/session/artifact count `0`
- Gate-closed Phase 14 action에는 trusted/in-scope/in-window/non-revoked/fresh
  action-time applicability verification `PASS`; unsigned/stale/non-`PASS` acceptance `0`
- Fixed-envelope pool/model/outcome/decision/candidate fingerprint exact equality
- Tiny exhaustive oracle와 selected stable IDs/objective exact equality
- Fault/cancel/security/native path callback/session/reference leak `0`
- Core/generic solver/application/verification `com.google.ortools` reference `0`
- Verification→solver/search/pool/backend dependency `0`
- Secret/raw PII match `0`
- Unauthorized traffic/config default `0`
- Optional failure 전후 incumbent/next-warm-start fingerprint exact equality
- Required failure의 degraded-success count `0`
- Both-gate 없는 publication/official result count `0`
- Evidence command/toolchain/test count/digest와 independent implementation/evidence review `PASS`

## 10. Ordered conditional work packages

모든 WP는 `C17_GATE_CLOSED`인 현재 상태에서 **대기**한다. 문서 review와
test-fixture review 외 Phase 13 source 작업을 시작하지 않는다. Scheduler-owned
gate-closed skip/rejection contract는 scheduler/Phase 14 control-plane 책임이며,
구현되더라도 Phase 13 module, solver, pool, provider 또는 `E-P13-*`를 만들지 않는다.

### WP-13.0 — Gate-open authorized scope와 predecessor receipt

- **Prerequisite/gate:** Scheduler task/owners 지정, Phase 06/07/08 accepted,
  Phase 14A의 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, `C-17` scope와
  hybrid meaning/route-selection authority approval. Selected
  runtime이 substituted provider/runtime이면 그때만 applicable Phase 12 evidence와
  independent review도 approved여야 한다.
- **Change target:** Gate-open `Phase13AuthorizedScopeVerifier`와 receipt validation.
  Scheduler applicability/skip/rejection과 공용 registry/status는 Phase 13 source가
  소유하지 않는다.
- **Concrete tasks:** `Open` disposition 뒤에만 §4의 AND gate,
  issuer/schema/digest/staleness, benchmark receipt의 corpus/protocol/criteria와
  Phase 06/07/08 evidence closure, selected-runtime conditional Phase 12 검증을
  수행한다. Closed/unauthorized path에서 Phase 13 code를 호출하지 않는다.
  Scheduler/Phase 14 contract test는 signed applicability envelope와 action-time
  signature/scope/validity/revocation/freshness verification을 검증하되 signing/trust
  값을 발명하지 않는다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=Phase13SchedulerApplicabilityTest,Phase13Phase14SkipContractTest

  ./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=Phase13AuthorizedScopeVerifierTest
  ```

- **Expected:** Closed gate에서 application control-plane test는 Phase 13
  resolution/class-load/artifact/backend count 0을 증명한다. Gate-open scope
  verifier는 applicable prerequisite 하나를 제거할 때마다 reject하며 baseline
  runtime에는 Phase 12 prerequisite를 발명하지 않는다. Unsigned, expired, revoked,
  stale 또는 wrong-scope skip은 Phase 14 action 전에 fail closed한다.
- **Failure/rollback:** Receipt를 boolean/env/classpath presence로 대체하거나 partial
  approval 또는 과거 signature `PASS` 한 줄로 대체하면 WP 전체를 revert하고
  `GATED` 유지. Approved signing/trust policy 전에는 signed receipt가
  `NOT_PRODUCED`인 future-red 상태를 보존한다.
- **Handoff:** `ValidatedPhase13Scope`와 `E-P13-GATE` candidate를 WP-13.1에 전달.

### WP-13.1 — Immutable evaluated route pool

- **Prerequisite/gate:** WP-13.0 green, accepted Phase 06 cache-free route
  evaluation/candidate/replay contract와 approved explicit pool experiment policy.
- **Change target:** `solver.pool` artifact/admission/delta/merge/pin/seal과
  independent pool fixtures.
- **Concrete tasks:** Completed hard-feasible route만 수집하고 partial/stale/cross
  authority를 거부한다. Append/import/reload 동일 merge, stable order, no-alias,
  incumbent pin과 safe dominance/conservative retention을 구현한다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/solver/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=RoutePoolAdmissionTest,RoutePoolMergePropertyTest,RoutePoolAliasTest,RoutePoolDominanceOracleTest,RoutePoolPinTest,RoutePoolDeterminismTest
  ```

- **Expected:** Permutation마다 exact snapshot digest; integrity conflict arbitrary
  winner 0; invalid/interrupted admission 0; pin loss 0.
- **Failure/rollback:** Memory pressure나 scalar cost로 hidden pruning, mutable alias
  또는 nondeterministic digest가 발생하면 pool feature를 disable하지 말고 WP를
  미완료로 되돌려 ALNS-only baseline을 유지한다.
- **Handoff:** Immutable `RoutePoolDelta/Snapshot`, identity/dominance report와
  `E-P13-POOL` candidate를 WP-13.2에 전달.

### WP-13.2 — Projection capability와 exact model contract

- **Prerequisite/gate:** WP-13.1 green, approved projection/model-mode scope,
  bound profile dependency/unit/range declaration과 independent oracle owner 지정.
- **Change target:** `selection.projection`, projected column/model/warm-start
  manifests와 coefficient proofs.
- **Concrete tasks:** 각 dimension/constraint를 exact/projectable/non-projectable로
  분류하고 request/unassigned/concrete-vehicle rows, lexicographic stages,
  coefficient range/round-trip과 warm-start feasibility를 만든다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/solver/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=RouteSelectionProjectionTest,LexicographicSelectionTest,WarmStartContractTest,TinyExactRouteSelectionOracleTest
  ```

- **Expected:** Tiny oracle exact; non-projectable typed skip; overflow/lossy
  coefficient backend call 0; hidden Big-M/surrogate 0.
- **Failure/rollback:** Projection이 profile 의미를 완전히 표현하지 못하면 해당
  scope를 `SKIPPED_NON_PROJECTABLE_PROFILE`로 남기고 모델을 근사해 진행하지 않는다.
- **Handoff:** Frozen model/warm-start contract, oracle report와
  `E-P13-SELECTION` partial candidate를 WP-13.3에 전달.

### WP-13.3 — Backend-neutral API와 deterministic fake

- **Prerequisite/gate:** WP-13.2 green, route-selection authority matrix 승인.
  OR-Tools production dependency/native approval는 아직 필요하지 않으며 fake는 test-only다.
- **Change target:** `selection.api`, outcome validation, fake backend/session,
  cancellation와 status×incumbent contract.
- **Concrete tasks:** Backend-neutral factory/session, typed outcome, selected stable
  IDs, incumbent-presence guard, cancellation, work accounting과 deterministic
  fake/oracle adapter를 구현한다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/solver/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=SelectionOutcomeContractTest,TinyExactRouteSelectionOracleTest,BackendLifecycleTest
  ```

- **Expected:** `com.google.ortools` reference 0; status/incumbent unsafe attribute read 0; fake
  selected IDs가 oracle와 exact 일치.
- **Failure/rollback:** Fake 값을 production default나 backend approval evidence로
  승격하면 WP를 거부한다. Solver-neutral contract만 last safe draft로 보존한다.
- **Handoff:** Reviewed API/outcome contract와 fake oracle evidence를 WP-13.4/13.5에 전달.

### WP-13.4 — Approved OR-Tools dependency, CP-SAT adapter와 native lifecycle

- **Prerequisite/gate:** WP-13.3 green, OR-Tools CP-SAT policy에 대한 exact
  version/checksum/config, Apache-2.0/applicable notice/SBOM/native redistribution와
  platform matrix, security, operations와 cost approval가 모두 유효.
- **Change target:** `adapters/route-selection-ortools-cpsat`와 isolated
  integration profile/assembly. 승인 전 module을 만들지 않는다.
- **Concrete tasks:** Direct `com.google.ortools.sat` model build, warm-start hint
  mapping, solve/`stopSearch`, raw status × incumbent mapping, stable ID extraction,
  process-wide `Loader.loadNativeLibraries()` smoke test, callback/reference cleanup,
  temp-resource operations와 OSS notice/SBOM evidence만 구현한다. `MPSolver`와
  commercial license/capacity lease contract는 만들지 않는다.
- **Verification commands/tests:**

  ```text
  ./mvnw -P<approved-phase13-ortools-integration-profile> \
    -pl adapters/route-selection-ortools-cpsat -am verify
  ```

- **Expected:** OR-Tools-free default `./mvnw clean verify` green; isolated CP-SAT
  status/oracle parity; normal/exception/cancel callback/reference leak 0; native
  smoke/platform/SBOM/notice evidence complete.
- **Failure/rollback:** OSS-license/SBOM/native/security/cost authority 만료 또는 cleanup
  failure 시 backend assembly를 비선택 상태로 되돌리고 default ALNS-only build를
  유지한다. Fake success로 대체하지 않는다.
- **Handoff:** Approved backend capability/config fingerprint와 integration evidence를
  WP-13.5/13.7에 전달.

### WP-13.5 — Fresh materialization, full evaluation과 strict adoption

- **Prerequisite/gate:** WP-13.2~13.3 green; WP-13.4 backend 또는 deterministic
  fake가 명시적 test scope로 선택됨; accepted Phase 03/04 evaluation/comparator.
- **Change target:** `selection.conversion`, application materializer/adopter,
  Phase 07 candidate handoff.
- **Concrete tasks:** Outcome ID membership을 검증하고 새 route/bank를 복사해 exact
  request/vehicle partition, full propagation/evaluation, comparator와
  strictly-better adoption을 구현한다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=RouteSelectionMaterializationTest,HybridAdoptionTest,HybridPhase07BoundaryTest
  ```

- **Expected:** Pool mutation/alias 0; stale selected ID draft 0; equal/worse/invalid
  adopt 0; raw backend objective authority 0; both-gate 우회 0.
- **Failure/rollback:** Divergence나 verifier bypass가 있으면 candidate를 폐기하고
  exact incumbent fingerprint를 유지한다. Partial draft를 warm start로 넘기지 않는다.
- **Handoff:** `EvaluatedSelectionCandidate` 또는 typed failure, adoption record와
  Phase 07 compatibility report를 WP-13.6에 전달.

### WP-13.6 — Hybrid lifecycle, fallback, retry와 reproducibility

- **Prerequisite/gate:** WP-13.1~13.5 green, approved optional/required policy와
  reproducibility class. Cadence/traffic/budget 생략값 없음.
- **Change target:** `application.hybrid` orchestrator/state/record, retry/fallback,
  next warm start와 fault/repro suites.
- **Concrete tasks:** ALNS→pool→selection→materialize→evaluate→adopt commit boundary,
  optional exact fallback, required incomplete, pre/post optimize retry, stable
  lineage와 strong/timeboxed classification을 구현한다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=HybridFallbackTest,HybridRetryTest,HybridReproducibilityTest,HybridRollbackTest
  ```

- **Expected:** Every failure 전후 incumbent/pool/next-warm-start equality;
  required degraded success 0; fixed strong envelope exact repeat 또는 weaker class
  explicit.
- **Failure/rollback:** Nondeterminism, stale/partial contamination 또는 post-optimize
  retry 중복이 발생하면 activated plan을 중단하고 accepted ALNS-only manifest로
  rollback한다.
- **Handoff:** Immutable `HybridPhaseRecord`, fallback/repro report와
  `E-P13-HYBRID` candidate를 WP-13.7에 전달.

### WP-13.7 — Security, OSS license/SBOM, operations, performance와 cost evidence

- **Prerequisite/gate:** WP-13.4/13.6 green, isolated approved environment,
  owner별 test protocol과 **실험용 explicit 수치** 승인.
- **Change target:** Tenant/access/secret tests, runtime admission/cancel/recovery/rollback,
  pool/model work/memory, backend CPU/memory/native/cost/shadow measurement.
- **Concrete tasks:** Cross-tenant denial, secret redaction, least privilege,
  admission exhaustion, cancel/timeout/native temp cleanup, pool growth, selection work,
  latency/quality/fallback/cost 측정과 no-hidden-threshold 검사를 수행한다.
- **Verification commands/tests:**

  ```bash
  ./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=RoutePoolWorkAccountingTest,RouteSelectionWorkAccountingTest,HybridExperimentManifestTest,HybridCostAuthorityTest,HybridTrafficAuthorityTest

  ./mvnw -f build/architecture-rules/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=HybridSecurityContractTest
  ```

- **Expected:** Unauthorized access/backend open 0; secret leak 0; cleanup leak 0;
  counters complete; 측정되지 않은 threshold/pass/traffic default 0.
- **Failure/rollback:** Security/OSS-license/SBOM/operations/cost approval 또는 measured bar가
  충족되지 않으면 production recommendation을 만들지 않고 ALNS-only plan을
  유지한다. Experiment 결과를 official evidence로 이름 바꾸지 않는다.
- **Handoff:** `E-P13-SECURITY-LICENSE-OPS-COST`와 go/no-go owner verdict를 WP-13.8에 전달.

### WP-13.8 — Shadow, rollback, full bundle, review와 Phase 14 handoff

- **Prerequisite/gate:** WP-13.0~13.7 green, required test failed/error/skipped 0,
  accepted Phase 06/07 receipts, applicable conditional Phase 12 receipt when a
  substituted runtime is selected, independent reviewer 지정.
- **Change target:** ALNS-only A/B shadow, rollback rehearsal, immutable
  `Phase13EvidenceManifest`, review input와 conditional Phase 14 handoff.
- **Concrete tasks:** Same authority/manifest의 ALNS-only와 hybrid를 비교하되
  independent verifier, reproducibility class, quality/memory/native/cost/fallback
  evidence를 분리한다. Rollback rehearsal와 activated handoff를 content-address한다.
- **Verification commands/tests:**

  ```bash
  ./mvnw clean verify

  ./mvnw -f rpdptw/hybrid-application/pom.xml clean test \
    -Dsurefire.failIfNoSpecifiedTests=true \
    -Dtest=HybridPhase07BoundaryTest,HybridRollbackTest
  ```

- **Expected:** Root OR-Tools-free ALNS-only green; shadow lineage same-authority; both-gate
  failure 0 accepted; rollback exact; complete `E-P13-*`; independent Phase 13
  implementation/evidence review `PASS`.
- **Failure/rollback:** Bundle/review/authority 중 하나라도 불완전하면 최대
  `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`, official hybrid와 production activation을
  주장하지 않는다.
- **Handoff:** Gate-open인 경우만 `Phase13ActivatedHandoff`를 Phase 14 owner에게
  전달한다. Gate-closed 경로는 scheduler skip receipt를 사용하며 Phase 13 산출물을
  요구하지 않는다.

## 11. Evidence bundle과 verification layer

### 11.1 Planned evidence

| Evidence key | 최소 내용 | 현재 상태 |
|---|---|---|
| `E-P13-GATE` | All authority/evidence receipt validation, absence/unauthorized tests | NOT_PRODUCED |
| `E-P13-POOL` | Admission/merge/dominance/pin/no-alias/digest/memory | NOT_PRODUCED |
| `E-P13-SELECTION` | Projection/tiny oracle/model/warm start/status/backend lifecycle | NOT_PRODUCED |
| `E-P13-HYBRID` | Materialization/full evaluation/strict adoption/fallback/repro | NOT_PRODUCED |
| `E-P13-SECURITY-LICENSE-OPS-COST` | Tenant/secret/Apache-2.0/applicable notices/SBOM/native/admission/cancel/capacity/cost owner verdict | NOT_PRODUCED |
| `E-P13-SHADOW-ROLLBACK` | ALNS-only A/B, parity, fallback statistics, rollback rehearsal | NOT_PRODUCED |
| `E-P13-HANDOFF` | Accepted implementation/evidence review와 gate-open activated handoff only | NOT_PRODUCED |
| Scheduler Phase 13 skip receipt | `C17_GATE_CLOSED`, exact schema/version/decision/ALNS-only identity, signed envelope, trust-policy refs, validity, action-time revocation/freshness verification, Phase 13 refs absent | NOT_PRODUCED; scheduler-owned, not `E-P13-*` |

각 bundle은 source/build/runtime/problem/travel/profile/config/pool/model/warm-start/
backend/reproducibility identity, exact command, environment, exit code, passed/failed/
skipped count, fixture/oracle ref, owner verdict, known limitation과 rollback point를
가진다. Raw secret, mutable latest와 console 한 줄은 evidence가 아니다.

### 11.2 Layer matrix

| Layer | 필수 test | OR-Tools/native 필요 여부 |
|---|---|---|
| Gate/unit | Absence, unauthorized, partial/stale receipt | 없음 |
| Pool unit/property | Admission, merge, no-alias, dominance, digest | 없음 |
| Projection/oracle | Exact rows, brute force, range, warm start | 없음 |
| Solver-neutral contract | Status×incumbent, fake, cancel | 없음 |
| Materialization/application | Clone, full eval, strict adoption | 없음 |
| Phase 07 boundary | Both-gate and corruption block | 없음 |
| Architecture | OR-Tools/verification/provider leakage | 없음 |
| OR-Tools CP-SAT integration | Native/status/oracle/cancel/cleanup | **승인 뒤 필요** |
| Security/operations | Tenant/secret/admission/cancel/recovery | 승인된 isolated env |
| Performance/cost | Pool/model/work/fallback/cost measurement | 승인된 experiment |
| Shadow/rollback | Same-authority ALNS-only A/B | 승인된 staging |
| Official hybrid | Phase 14 gates까지 포함 | 별도 production authority |

## 12. Exit gate, Definition of Done과 anti-pattern

### 12.1 Gate-closed 판정

Gate가 닫혀 있는 현재 Phase 13 implementation은 `GATED`이며 `ACCEPTED`가 될 수
없다. 문서 review가 통과해도 다음만 말할 수 있다.

- Conditional contract가 review 가능함
- Phase 13 implementation은 시작되지 않음
- Phase 14 ALNS-only signed skip/action-time verification semantics가 proposed됨
- Actual signed applicability envelope/verification receipt는 `NOT_PRODUCED`
- Explicit unauthorized activation은 금지됨

이를 `PHASE_13_DONE`, `HYBRID_DISABLED_IMPLEMENTED`, `E-P13-HANDOFF_COMPLETE`로
표현하지 않는다.

### 12.2 Gate-open exit gate

- §4 activation receipt의 모든 owner/evidence가 valid
- Phase 06/07/08 accepted와 Phase 14A의 immutable ALNS benchmark bundle,
  independent review, acceptance receipt가 valid; selected runtime이 substituted provider/runtime이면
  applicable Phase 12 substitution evidence와 review도 approved
- Pool admission/import/reload deterministic digest, no alias, pin, safe dominance
- Tiny exact oracle와 projection/model/warm-start exact
- All outcome × incumbent status safe access
- Approved OR-Tools dependency/native/platform/SBOM/security isolation
- Fresh materialization, exact partition, full recomputation
- Strictly-better-only adoption
- Optional failure exact fallback, required failure incomplete
- Stale/partial/divergent candidate fail closed
- Fixed-envelope deterministic result 또는 explicit weaker reproducibility class
- Phase 07 both-gate bypass 0
- Security/OSS-license/SBOM/operations/performance/cost/shadow/rollback evidence
- Required test failed/error/skipped 0
- Immutable `E-P13-*` bundle과 independent Phase 13 implementation/evidence review `PASS`
- Phase 14 activated handoff와 rollback point
- 별도 production activation authority가 없다면 default/traffic 활성화 0

### 12.3 Definition of Done

Phase 13은 gate-open scope에서 다음 AND gate를 모두 만족할 때만 scheduler가
`ACCEPTED`로 전이할 수 있다.

1. Applicability와 exact approved scope가 immutable receipt로 고정됐다.
2. Phase 14A ALNS benchmark acceptance receipt가 dataset/fixture, seed/repeat,
   hardware/runtime, correctness oracle, both-verifier, objective/quality,
   timeout/resource와 variance/replay evidence를 완전하게 참조한다.
3. Proposed API 이름이 아니라 pool/selection/materialization/adoption 책임 경계가
   architecture test로 보장된다.
4. Same-authority route만 deterministic immutable pool에 들어간다.
5. Exact projection이 가능하거나 typed skip하며 hidden approximation은 0이다.
6. Independent tiny oracle가 model/backend result를 검증한다.
7. OR-Tools dependency/native runtime이 generic build와 semantic module에서 격리되고 Apache-2.0/applicable notice/SBOM evidence가 있다.
8. Outcome ID만으로 fresh candidate를 만들고 full evaluator/comparator가 권위다.
9. Optional/required failure와 retry가 incumbent/identity를 오염시키지 않는다.
10. Strong replay claim은 fixed deterministic envelope에서만 evidence가 있다.
11. Phase 07 candidate/result verifier를 모두 통과한 결과만 downstream에 간다.
12. Security, operations, OSS-license/SBOM/legal, performance, cost owner verdict가 모두
    approved scope와 일치한다.
13. ALNS-only shadow/rollback이 재현되고 Phase 14 handoff가 conditional contract와
    일치한다.
14. 모든 evidence/review가 immutable identity와 digest를 가진다.
15. Open/gated/deferred/provider/budget/threshold/traffic 값을 숨은 default로
    만들지 않는다.

### 12.4 금지 anti-pattern

- Gate가 닫힌데 `hybrid.enabled=false`를 넣고 구현 완료라고 주장
- 명시적 hybrid 요청을 승인 없이 ALNS-only success로 조용히 fallback
- Phase 12 evidence 또는 provider SDK 설치를 `C-17`, OR-Tools activation이나 hybrid
  authority의 대체물로 간주
- Baseline/non-substituted runtime에 Phase 12 evidence를 보편 선행조건으로 발명
- 문서의 OR-Tools version 예시를 승인된 production pin/config로 승격
- Root/common dependency에 OR-Tools JAR/native library 추가
- ServiceLoader/classpath 순서로 backend를 자동 선택
- Environment variable 하나로 C-17/traffic/required policy 활성화
- Route pool cap/pruning/budget/threshold를 코드 상수로 숨김
- Interrupted/partial/stale/cross-fingerprint route 수집
- 같은 coverage에서 scalar objective 하나만 보고 route 제거
- Live/mutable pool을 selector에 전달
- Non-projectable profile을 surrogate/Big-M로 무음 근사
- Completion order, elapsed time 또는 traffic assignment를 selection tie-break로 사용
- Incumbent 없는 status에서 selected/objective attribute 접근
- Pool artifact 또는 selected column을 `CommittedCandidate`로 cast
- Backend objective/status/feasibility flag를 full comparator authority로 사용
- Converter가 pool route를 mutate하거나 stale cache를 복사
- Equal/worse/invalid selector candidate를 “diversification” 이유로 채택
- Optional failure 뒤 incumbent/next warm start fingerprint 변경
- Required failure를 `DEGRADED_ALNS_ONLY` normal success로 표시
- Optimize 시작 뒤 같은 logical run으로 result-bearing retry
- Secret/raw PII를 log/artifact/fingerprint에 포함
- Timeboxed multi-thread backend에 strong reproducibility 주장
- Phase 07 verifier 하나 또는 둘 다 생략
- Gate-closed skip receipt에 가짜 `E-P13-*` 또는 hybrid artifact ref 포함
- Unsigned/test/self-signed skip 또는 과거 verification `PASS`로 Phase 14 action 승인
- Signature algorithm, trust root, revocation/freshness window 숫자를 target default로 발명
- Gate-open handoff를 backend log/solver objective 한 줄로 대체
- Phase 13 acceptance만으로 Phase 14 official/production authority 주장

## 13. Blocker, owner, last safe point와 restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 06 accepted evidence 부재 | BLOCKER | Phase 06 Algorithm + reviewer | Pool source/ALNS baseline | Phase 06 detailed contract only | Accepted `E-P06-*` and review |
| Phase 07 accepted evidence 부재 | BLOCKER | Phase 07 Verification + reviewer | Both-gate baseline | Pure verifier contract only | Accepted `E-P07-*` and review |
| Phase 08 accepted local evidence 부재 | BLOCKER | Phase 08 Application/Local + reviewer | Reproducible optimizer-free benchmark execution seam | Proposed local reference only | Accepted `E-P08-*` and review |
| ALNS benchmark acceptance receipt 부재 | GATED BLOCKER | Benchmark/Quality + Independent Review + Phase 14A owner | 모든 Phase 13 착수/activation | ALNS-only implementation/verification path; no MIP | Approved corpus/protocol/criteria와 complete immutable correctness/quality/performance/reproducibility bundle, independent review, `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` |
| Full-solution evaluation/comparator contract gap | CROSS-PHASE BLOCKER | Phase 03~07 Core/Profile/Algorithm/Verification owners | Fresh full evaluation, strict adoption, Phase 07 publication | Route-level kernel + immutable ordered routes/bank; no ad hoc aggregation | Accepted solution API/identity/reuse/failure/comparator and cross-phase equality/corruption tests |
| Conditional Phase 12 evidence 부재 | CONDITIONAL BLOCKER | Phase 12 Platform/Operations/Security | Selected substituted runtime을 사용하는 Phase 13 activation만 | Baseline/non-substituted runtime; no Phase 12 prerequisite | Applicable `E-P12-*`, bounded handoff and accepted review |
| `C-17` scope | GATED | Product·Algorithm·Architecture | 모든 Phase 13 source/evidence | ALNS-only path | Separate scope approval |
| Hybrid meaning | OPEN/GATED | Product + Algorithm | Applicable behavior/plan | Minimum adapter boundary only | Versioned meaning contract approval |
| Route selection authority | OPEN/GATED | Product/Algorithm authority | Request/adoption/rollout 권한 | Backend non-authority fixed | Authority matrix approval |
| Backend policy | RESOLVED POLICY — IMPLEMENTATION GATED | Solver Backend owner | Canonical adapter family | Direct Java CP-SAT policy | C-17 remains closed; no implementation authority |
| OR-Tools dependency/config | OPEN/GATED | Architecture/Security/Operations | Adapter/integration | Backend-neutral API proposal | Exact version/checksum/params/platform approval |
| OSS license/SBOM/native | GATED | Legal/Supply-chain/Security | Notice/SBOM/redistribution/platform packaging | OR-Tools-free ALNS-only build | Written approval and isolated profile |
| Operations | GATED | SRE/Platform | Retry/cancel/recovery/rollback | ALNS-only runbook | Approved runbook/rehearsal |
| Security | GATED | Platform Security | Tenant/access/secret/native | No backend | Threat model and negative tests approved |
| Cost/capacity | GATED | FinOps/Product | Budget/admission/rollout | No selector spend | Measured cost/capacity approval |
| Pool/budget/cadence/traffic 수치 | OPEN — EXPERIMENT_REQUIRED | Performance/Product/FinOps | Official config/rollout | Explicit TEST_ONLY values | Measured experiment + approval |
| Reproducibility class | OPEN per approved CP-SAT config | Algorithm/Benchmark | Strong claim/official comparison | ALNS-only replay | Fixed envelope evidence or explicit weaker class |
| Scheduler task/owners | BLOCKER | Total scheduler | Authoritative READY/status | `TBD_NOT_SUPPLIED` | Exact task ID and role separation |
| Scheduler gate-closed signed skip receipt/evidence 부재 | HANDOFF BLOCKER | Scheduler + Phase 14 Application owner | Gate-closed Phase 14 predecessor receipt | Canonical ALNS-only path; no Phase 13 artifact/acceptance claim | Exact signed envelope, action-time verification receipt, Phase 13 assembly/class-load/provider edge 0 and unauthorized pre-solve rejection evidence |
| Applicability signing/trust policy 미승인 | OPEN/GATED BLOCKER | Security + Release evidence-trust owner | Phase 14 consumption of Skip/Activated applicability | Unsigned proposed schema and negative tests only; no Phase 14 action | Approved signature profile/trust roots/revocation/time/freshness/canonicalization/verifier policy and evidence |
| Phase 13 implementation/evidence review | REVIEW_GATE | Independent reviewer | ACCEPTED/activated handoff | Reviewed conditional document; no implementation/evidence | Complete evidence + implementation review PASS |
| Phase 12/13/14 reciprocal contract alignment | RESOLVED — NOT A BLOCKER | Phase 12/13/14 documentation owners | 없음; semantic regression 시에만 재개 | Phase 12 v1.3 conditional bounded evidence (introduced in v1.2) + Phase 13 v1.5 signed applicability + Phase 14 v1.4 14B consumer seam | Universal Phase12→13 premise와 adjacent/reciprocal digest acceptance가 제거되고 signed envelope/action-time verification 의미가 정렬됨 |
| Phase 14 official values | OPEN/SEPARATE | Benchmark/Quality | Official hybrid 또는 ALNS cutover | Experiment-only | `Q-BENCH-02`, compliant fixture, Phase 14 authority |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | Optional variant 질문/구현 | Current pair/terminal/bank | Exact restart evidence + approval |
| Cross-worker central selector | DEFERRED SEPARATE ADR | Architecture/Operations | Non-local hybrid | Worker-local candidate boundary | RM-9C evidence + scalability ADR |

같은 blocker가 반복되어도 가짜 owner, evidence, approval, solver, threshold 또는
traffic default를 만들지 않는다. Gate가 닫힌 동안의 restart point는 이 문서와
accepted ALNS-only predecessor뿐이다.

## 14. Previous/next handoff와 traceability

### 14.1 Previous — Phase 06/07

Actual Phase 06 §16.3에서 다음만 소비한다.

- Accepted COW ALNS baseline
- Immutable candidate/replay/evidence identity
- Explicit state lifecycle seam
- Phase 07 both-gate baseline dependency

Backend API, raw selector incumbent, apply/undo, final result claim은 받지 않는다.

Actual Phase 07 §15.3에서 다음을 소비한다.

- Materialize/full-evaluate 뒤 동일 candidate/result both-gate path
- Publishable result authority와 verifier independence

Raw MIP incumbent, `ObjVal`, selected columns, backend status는 넘기지 않는다.

### 14.2 Conditional input — actual but unaccepted Phase 12

[Actual Phase 12 — Provider substitution](phase-12-provider-substitution.md)은
v1.2 review `COMPLETE_CHANGES_REQUIRED`, acceptance `BLOCKED_NOT_IMPLEMENTED`,
implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY`다. Phase 12는
Master Realization Plan의 Phase 10에서 갈라지는 독립 provider-substitution
branch이며 Phase 13의 보편 predecessor가 아니다. Canonical Phase 13 DAG는
accepted Phase 06/07/08, Phase 14A ALNS benchmark acceptance와 별도 `C-17`
authority에서 시작한다.

Approved hybrid scope가 substituted provider/runtime을 실제 선택할 때만
Phase 13은 actual Phase 12 §18.3의 bounded handoff에서 그 선택에 해당하는
accepted evidence를 조건부로 소비한다. 그 경우에도 다음 제한을 보존한다.

```text
evidenceApplicability = INFRASTRUCTURE_DECISION_INPUT_ONLY
c17Approval = NOT_GRANTED_BY_THIS_HANDOFF
ortoolsActivationOrDistributionApproval = NOT_GRANTED_BY_THIS_HANDOFF
hybridImplementationAuthority = false
productionCutoverAuthority = false
```

Phase 12의 cloud provider locator, SDK DTO, execution ID와 traffic setting은
Phase 13 pool/model/result identity로 가져오지 않는다. Phase 12 parity/evidence는
substitution 경계만 입증하며 route-selection solver/provider, `C-17` activation,
performance, official evidence 또는 production authority를 만들지 않는다.
Baseline/non-substituted runtime에는 Phase 12 evidence 누락을 Phase 13 blocker로
보고하지 않는다.

### 14.3 Next — Phase 14B gate closed

Actual Phase 14 v1.4의 document verdict는 `CHANGES_REQUIRED`, phase acceptance는
`BLOCKED_NOT_READY`, handoff는 `NOT_READY`다. 아래 consumer seam 정렬은 이 상태나
Phase 14 자체 gate를 올리지 않는다.

Gate가 닫히면 canonical handoff는 다음이다.

```text
Phase13ApplicabilityReceipt.Skip
  reason = C17_GATE_CLOSED
  receipt schema/version
  exact scheduler applicability decision fingerprint
  exact ALNS-only plan identity
  signed applicability envelope ref/digest
  signature-profile and trust-policy refs
  validity window and revocation-policy ref
  action-time verification receipt
    verifier build/trust-root-set digests
    revocation/freshness snapshots
    checked Phase 14 action/scope
    verdict = PASS
  Phase13 pool/model/outcome/hybrid refs = ABSENT
  E-P13 implementation refs = ABSENT
```

Phase 14A는 이 applicability receipt를 소비하지 않고 ALNS-only benchmark acceptance를
먼저 완료한다. Phase 14B는 Phase 13 산출물 없이 ALNS-only official/cutover path를
계속할 수 있다. 단, Phase 11, Phase 14A acceptance, `Q-BENCH-02`, compliant integer `D/U`,
security/operations/cost/production authority 등 자기 gate는 그대로 충족해야 한다.
Skip은 Phase 13 accepted evidence가 아니며 applicability 분모에서 조건부 branch를
제외하는 근거다. Unsigned, expired, revoked, stale, wrong-scope/action 또는
non-`PASS` action-time verification은 Phase 14 manifest/action 전에 fail closed한다.
정확한 crypto/trust/freshness policy는 OPEN/GATED이고 actual signed receipt는
`NOT_PRODUCED`다.

### 14.4 Next — Phase 14B gate open

Official hybrid manifest를 **별도로 승인해 선택한 경우에만** 다음을 추가한다.

```text
Phase13ActivatedHandoff
  activation receipt fingerprint
  signed applicability envelope ref/digest
  action-time trust/validity/revocation/freshness verification receipt
  exact approved scope/hybrid meaning/authority fingerprints
  Phase06/07/08 accepted receipt refs
  ALNS_BENCHMARK_ACCEPTANCE_RECEIPT ref/digest
  optional applicable Phase12 accepted receipt ref
    only when the selected runtime is substituted
  pool/projection/model/backend/config/reproducibility contract fingerprints
  E-P13-GATE
  E-P13-POOL
  E-P13-SELECTION
  E-P13-HYBRID
  E-P13-SECURITY-LICENSE-OPS-COST
  E-P13-SHADOW-ROLLBACK
  accepted Phase13 implementation/evidence review ref/digest
  last accepted ALNS-only rollback point
```

Phase 14는 이 handoff의 scope와 official manifest가 exact 일치하는지 확인한다.
다른 customer/profile/backend/budget/traffic에 재사용하지 않는다.

### 14.5 Requirement → contract → test → evidence

| Requirement | Source | Phase 13 contract | Exact test | Planned evidence |
|---|---|---|---|---|
| `REQ-C17-GATE` | `C-17`, Master §15.11/§16.3 | Scheduler-owned closed-path absence/unauthorized; gate-open conjunctive scope | `Phase13SchedulerApplicabilityTest.*`, `Phase13AuthorizedScopeVerifierTest.*` | Scheduler skip/rejection receipt; gate-open `E-P13-GATE` only |
| `REQ-ALNS-BENCHMARK-FIRST` | 사용자 `ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP`, Plan §1.2/Phase 14A | §1.3/§4 Phase 14A acceptance receipt prerequisite | `Phase13AuthorizedScopeVerifierTest.missingOrMismatchedAlnsBenchmarkReceiptRejectsBeforeModuleLoad()` | `E-P14-ALNS-BENCHMARK-ACCEPTANCE` + `E-P13-GATE` |
| `REQ-P12-CONDITIONAL-RUNTIME` | Actual Phase 12 §18.3, Integrated §16, Plan Phase 12/13 DAG | §4/§14.2 selected substituted runtime에만 applicable bounded handoff | `selectedSubstitutedRuntimeRequiresApplicablePhase12Evidence`, `baselineRuntimeDoesNotInventPhase12Prerequisite` | Conditional ref in `E-P13-GATE` |
| `REQ-POOL` | Master §11.7, Domain §12, `RM-9A` | §6 pool artifacts/identity | `RoutePool*Test.*` | `E-P13-POOL` |
| `REQ-SELECTION` | Master §11.8, Domain §13, `RM-9B` | Exact projection/outcome/IDs | `RouteSelection*Test.*`, tiny oracle | `E-P13-SELECTION` |
| `REQ-HYBRID` | Master §11.9~11.10, Domain §14, `RM-9C` | §8 lifecycle/adoption/fallback | `Hybrid*Test.*` | `E-P13-HYBRID` |
| `REQ-VERIFY` | `C-21`, Phase 07 §15.3 | Same both-gate path | `HybridPhase07BoundaryTest.*` | `E-P13-HYBRID` |
| `REQ-IDENTITY` | Master §13, Integrated §19 | Stable pool/model/run/result identity | Determinism/divergence tests | `E-P13-POOL/SELECTION/HYBRID` |
| `REQ-LICENSE-SECURITY-OPS-COST` | Architecture §4~§5, Plan §13~§14 | Approved OR-Tools native/OSS notice/SBOM/admission/runbook/cost | `HybridSecurity*`, lifecycle/cost tests | `E-P13-SECURITY-LICENSE-OPS-COST` |
| `REQ-ROLLBACK` | Master §16.2, Integrated §21, Plan §12 | Exact ALNS-only fallback/rollback | `HybridRollbackTest.*` | `E-P13-SHADOW-ROLLBACK` |
| `REQ-P14-SKIP` | User fixed input, Plan Phase 13~14, Phase 14 `G14-P13-APPLICABILITY/G14-SIGNING-TRUST` | §6.5/§14.3 no-output signed skip + action-time trust/validity/revocation/freshness verification | `Phase13Phase14SkipContractTest.*` | Scheduler signed applicability envelope + action-time verification receipt, both NOT_PRODUCED |
| `REQ-P14-ACTIVATED` | Plan Phase 14 | §6.5/§14.4 accepted evidence handoff | Activated handoff contract tests | `E-P13-HANDOFF` |
| `REQ-NO-HIDDEN-DEFAULT` | `Q-BENCH-02`, `C-17`, `Q-VAR-01` | §3 open matrix | Config/traffic/cost negative tests | All `E-P13-*` known limitations |

새 hybrid meaning, authority, OR-Tools config, pool policy, projection mode, public
status, threshold, traffic rule 또는 retry 의미가 발견되면 source/owner/test/evidence를
이 표에 연결하고 approval record와 relevant canonical/Phase 12~14 semantic
compatibility를 review한다. 인접 whole-file/section digest나 reciprocal fingerprint를
acceptance로 추가하지 않으며, 구현 편의를 위해 open/gated/deferred 항목을 숨은
default로 채우지 않는다.
