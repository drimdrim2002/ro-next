# Phase 05 — Pickup-delivery pair, 삽입과 초기 후보군

```yaml
document_status: REVIEWED_CHANGES_REQUIRED
document_version: 1.3
phase: "05"
phase_name: pair-insertion-initial-portfolio
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
evidence_status: NOT_PRODUCED
review_status: INDEPENDENT_REVIEW_COMPLETED
phase_acceptance_status: NOT_ACCEPTED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Pair/Insertion/Portfolio owner role
  upstream_kernel: Phase 03 Core/Evaluation owner role
  upstream_binding: Phase 04 Capability/Profile owner role
  downstream_search: Phase 06 COW ALNS owner role
  architecture: RPDPTW Architecture owner role
  review: independent Phase 05 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture
  - Phase 01 accepted normalized request/vehicle/time/compatibility semantics
  - Phase 02 accepted immutable ProblemInstance and complete PreparedTravel
  - Phase 03 accepted route propagation/evaluation kernel and comparator
  - Phase 04 accepted exact BoundProfile/SolvePlan/capability binding
  - accepted stable request/route/vehicle total-order and explicit portfolio config
planned_evidence:
  - E-P05-PAIR
  - E-P05-INSERTION
  - E-P05-PORTFOLIO
source_baseline:
  repository_commit_observed: 3424277c9c74f8151a83be056a07dd4659331beb
  drift_policy: SOURCE_COMMIT_PLUS_EXACT_CITED_SECTION_STATUS_AND_REQUIREMENT_IMPACT_REVIEW
  whole_file_reciprocal_hashes: NOT_USED
adjacent_document_status:
  phase_04: ACTUAL_REVIEWED_CHANGES_REQUIRED_NOT_STARTED_NOT_ACCEPTED_DIRECTLY_READ
  phase_06: ACTUAL_CHANGES_REQUIRED_REVIEW_COMPLETE_NOT_STARTED_NOT_ACCEPTED_DIRECTLY_READ
  fingerprints: NOT_RECIPROCALLY_PINNED
  phase_04_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
  phase_06_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
```

## 1. 문서 지위와 권위

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 권위 문서의 `REVIEW` metadata는 provenance로 보존하지만 Phase 05 상세 문서 작성을 중단시키지 않는다. 반대로 이 문서의 존재, proposed type, future test 또는 command만으로 구현·evidence·review가 끝났다고 주장하지 않는다.

현재 checkout에 없는 module, type, test, report, review와 evidence는 모두 `proposed`, `planned` 또는 `future`다. 미확정 public API, 수치, 정렬 convention과 serialization은 `PROPOSED/OPEN` 또는 `test-only`로 표시한다. 새 production default를 만들지 않는다.

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../2026-07-31-phase-b-master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 pair/stable-state/evaluation 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/package/DAG
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 15 Phase 배치
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)
7. Actual predecessor인 [Phase 03 상세](phase-03-route-propagation-evaluation-kernel.md)의 planned handoff

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 권위 입력으로 사용하거나 복사하지 않았고 이 작업에서 수정하지 않는다.

이 문서의 독립 검토 결과와 남은 cross-Phase blocker는 [Phase 05 review](../reviews/phase-05-review.md)에 기록한다. Review는 canonical source 전체 hash를 서로 복제하지 않고 source commit, exact section, 상태와 requirement/test 영향으로 drift를 판정한다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 05에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | §4.1~§4.6, §6, §9, §10.1, §11.1~§11.2, §12, §15.5~§15.6, §16~§17 | Pair evaluator 책임, stable partition, atomic mutation, 최대 8개 portfolio, full evaluation 권위, Phase 06 경계 |
| [Final Domain](../../2026-07-26-domain-design.md) | §2.1~§2.4, §3, §5.3, §6~§11, §17.5~§17.6, §18 | Pair/service pattern, bank와 stable state, prepared travel, insertion option, portfolio와 acceptance evidence |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2.1~§2.7, §5.6, §6 | `evaluation.insertion`, `solver.portfolio`, `solver.state`, module DAG, OR-Tools-free ALNS-only architecture gate |
| [Integrated design](../../architecture-domain-implementation-design.md) | §3, §6~§10, §22~§25 | Upstream artifact, Phase 04 binding, Phase 05 stable/insertion/portfolio, Phase 06 COW 분리, test/corruption/anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-REQ-01~02`, `Q-OBJ-01~03`, `Q-ALG-01~02`, `Q-BENCH-02`, `Q-VAR-01` | Mixed service pattern, objective binding, 4×2 portfolio, COW의 다음 Phase 소유, official 수치 open, variant deferred |
| [Master Realization Plan](../master-realization-plan.md) | §3~§4, Phase 03~06, §8~§15 | Current inventory, Phase 05 entry/exit, test/evidence/DoD, blocker와 traceability |
| [Phase 03 상세](phase-03-route-propagation-evaluation-kernel.md) | §7, §9, §13~§15 | Pure kernel, typed `Feasible/Infeasible/Invalid`, comparator, exact identity, Phase 05 handoff와 금지 우회 |
| [Actual Phase 04](phase-04-capabilities-customer-profiles.md) | §3~§4, §7~§8, §12~§15 | Immutable `BoundProfile`/snapshot, existing Phase 03 declarations, catalog 재조회 금지, Phase 05 consumer equality와 open facet/API gate |
| [Actual Phase 06](phase-06-cow-alns-reproducibility.md) | §2~§4, §7~§9, §14~§17 | Phase 05 seed/portfolio 소비, `KEEP_COW`, Phase 06 소유 destroy/repair/acceptance/screen, adjacent proposed-name 정합화 gate |
| [구현 문서 지도](../README.md) | §3~§7 | Authority, canonical filename, planned/actual link와 review/status 규칙 |
| [Root README](../../../../README.md), root `pom.xml` | 전체 current placeholder inventory | Java 25/Maven 선언과 현재 GCP/placeholder가 target RPDPTW evidence가 아님 |

Phase 04/06 detailed와 independent review는 모두 actual이다. 두 review verdict는 `CHANGES_REQUIRED`이고 두 Phase 모두 implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `NOT_ACCEPTED`다. 서로가 서로의 whole-file SHA-256을 metadata에 넣으면 authoring 중 순환 drift가 생기므로 `DIRECTLY_READ` 상태와 exact section/verdict만 기록한다. Implementation entry에서는 accepted review가 승인한 immutable artifact/evidence digest를 handoff manifest에 pin하되, 이 상세 문서끼리 reciprocal whole-file hash를 복제하지 않는다.

Final Domain §18의 `Q-INFRA-01 DEFERRED`와 `RESOLVED 25 / DEFERRED 2` 문구는 최신 질문 등록부보다 오래된 historical status이므로 Phase 05 상태 판정에 사용하지 않는다. 최신 exact 상태는 질문 등록부의 `Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`이다.

### 1.2 ALNS-first 방향 적용

Phase 05는 ALNS 독립 구현 경로의 준비 단계다. Exit와 Phase 06 handoff는 optimizer
vendor, MIP solver, license/server/token, native backend 또는 production authority를
요구하지 않는다. Insertion의 `exact`는 모든 선언 위치를 Phase 03/04 propagation과
hard constraint로 권위 평가한다는 뜻이며 MIP/exact optimizer 호출을 뜻하지 않는다.

Phase 05 artifact와 oracle은 `05 → 06 → 07 → 08 → 14A` ALNS-only 경로를 우선
지원한다. Phase 13은 Phase 14A의 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` 뒤에만 열리는
후속 consumer이므로 Phase 05 API, portfolio 또는 evidence에 route pool/MIP 요구를
선반영하지 않는다.

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 05는 다음 세 책임을 구현·검증할 수 있는 상세 계약을 만든다.

1. 모든 stable solution에서 request가 complete same-route pair 또는 `SearchRequestBank` 중 정확히 하나에 있는 구조.
2. Base solution을 바꾸지 않고 한 pickup-delivery pair의 합법 위치를 열거하여 Phase 03/04 bound evaluation으로 평가하는 atomic insertion.
3. `CLOCK`, `SEQ_FARTHEST`, `SEQ_LARGE_DEMAND`, `SEQ_EARLIEST_DEADLINE`과 `DIRECT_FIRST_LARGE`, `DIRECT_FIRST_SMALL`을 조합한 최대 8개 독립 initial seed candidate.

동일한 authority fingerprints, explicit construction config와 stable input order에서 portfolio의 canonical member order, 각 solution bytes와 fingerprint, trace가 실행 횟수·thread·map iteration order와 무관하게 같아야 한다.

```text
ProblemInstance identity
+ PreparedTravel identity
+ Phase 03 PropagationDeclaration/EvaluationPlan identity
+ Phase 04 BoundProfile/SolvePlan/comparator identity
+ explicit Phase 05 portfolio/enumeration/tie config identity
→ immutable SeedPortfolio identity
```

### 2.2 포함 범위

- Request-level route/bank XOR와 same-route/exactly-once/precedence validation
- Delivery-only logical pickup ownership과 real pickup-delivery physical visit의 구분
- 한 concrete vehicle당 stable route 최대 하나, terminal/single-trip 구조의 유지
- Real pair의 모든 합법 final service position `pickupPosition < deliveryPosition`
- Delivery-only의 delivery service position과 route initial-load ownership을 한 option으로 취급
- Explicit exhaustive enumeration과 completeness를 과장하지 않는 explicit bounded enumeration seam
- Existing route와 실제 unused concrete `VehicleId`를 소비하는 `NEW_ROUTE`
- Side-effect-free option materialization과 Phase 03/04 bound evaluation 위임
- 정상 hard infeasibility, invalid/corruption과 search-quality miss의 typed 분리
- Immutable construction transition, bank removal과 route replacement/new-route creation의 단일 성공 전이
- Immutable `SearchSnapshot`, `ConstructionCandidate`, `SeedPortfolio` identity와 no-alias
- 4×2 policy matrix, `CLOCK UNAVAILABLE`, deterministic request/vehicle/route/position tie-break
- Hand-calculated/exhaustive small oracle, property/corruption/reproducibility test
- 필수 defect suite: pair split, candidate aliasing, order sensitivity, corruption, reproducibility tests
- Phase 06이 COW ALNS의 warm-start로 소비할 seed solution/portfolio contract

### 2.3 명시적 비범위

- Phase 03의 load/time/window/travel/stop/resource propagation 재구현
- Phase 04의 compatibility, customer profile, capability, score, objective/comparator 또는 `SolvePlan` binding 재구현
- Insertion failure를 final `UNASSIGNED` reason이나 confidence로 변환
- Phase 06의 changed-route COW, `TrialDraft`, destroy/repair operator, acceptance, adaptive state, phase-1 screen, champion, step/round/seed 실행
- Phase 07의 candidate verifier, final exhaustive insertion audit, result finalization, diagnostic와 publication
- Phase 13 route pool/MIP/hybrid와 route-selection materialization
- Apply/undo, mutable best/current, rollback log 또는 incremental mutation cache
- Raw input/coordinate/speed/matrix 해석, travel generation, fixed-point/rounding/default 생성
- Multi-trip/rotation, MDVRP/OVRP/SDVRP와 cross-trip pair
- Public/wire API, artifact serialization, official numeric budget 또는 production calibration
- AWS/GCP/storage/workflow/HTTP DTO, provider SDK와 deployment

Phase 05의 “atomic apply”는 **initial construction에서 선택된 완전한 option을 새 immutable snapshot으로 옮기는 함수형 전이**다. Phase 06의 COW trial, destroy/remove/repair/acceptance 구현을 앞당기는 근거가 아니다.

## 3. Phase-local 결정과 불변조건

### 3.1 결정 상태

아래 type과 signature는 의미를 명확히 하는 **proposed internal design**이다. Phase 05 review와 선행 Phase handoff가 승인하기 전 public compatibility 약속이 아니다.

| 결정 | 상태 | 내용 |
|---|---|---|
| Stable partition | FIXED | 각 request는 complete assigned pair 또는 bank 중 정확히 하나다. Partial/duplicate/split/reverse/overlap/missing은 defect다. |
| Evaluation authority | FIXED | Compatibility, capacity, time, resource와 customer hard rule은 Phase 03/04 bound result만 소비한다. Phase 05가 공식이나 조건문을 복제하지 않는다. |
| Enumeration | FIXED + PROPOSED seam | Real pair exhaustive mode는 모든 `p < d`를 평가한다. Bounded mode는 explicit ordered position set/version을 요구하고 `NO_FEASIBLE_WITHIN_BOUND`만 말할 수 있다. |
| Phase 05 portfolio mode | FIXED | Exit evidence용 initial portfolio는 exhaustive position evaluation을 사용한다. Hidden bound나 shortlist를 쓰지 않는다. |
| State update | FIXED | Feasible option만 새 immutable snapshot을 만들며 실패/invalid/stale option은 base bytes/fingerprint를 바꾸지 않는다. |
| Stable tie | FIXED semantics / PROPOSED type | Bound business comparator가 먼저다. 완전 동률일 때만 request→ownership/vehicle→route target→position identity로 total order를 만든다. |
| Portfolio | FIXED | 4 growth × 2 DIRECT-first vehicle order의 최대 8개 independent member다. Phase 05는 screen이나 champion을 만들지 않는다. |
| Portfolio traversal | PROPOSED_REVIEW_REQUIRED | §9의 ordered `(request,target,position)` traversal은 원문의 policy 역할을 구현 가능하게 한 내부 후보다. 승인 없이 다른 hidden order를 사용하지 않는다. |
| CLOCK coordinate convention | OPEN/PROPOSED | 0도 축, 좌표 orientation과 angle implementation/version은 explicit config/review 대상이다. 누락 시 다른 정책으로 fallback하지 않는다. |
| Utilization edge policy | OPEN/PROPOSED | Missing/zero capacity와 exact rational tie 의미는 Phase 04 bound policy/config가 제공해야 한다. `double` 또는 Feature 문자열 추론 금지다. |
| Identity encoding | PROPOSED/OPEN | Semantic identity field는 §7로 고정하되 canonical byte encoding/hash는 Phase 00/serialization review가 소유한다. |

새 `C-*` 또는 `Q-*` ID를 이 문서에서 만들지 않는다.

### 3.2 반드시 지킬 불변조건

1. **Pair atomicity:** Real pair의 pickup/delivery physical visit와 delivery-only pair ownership은 한 request 단위로 생성·적용한다.
2. **Same route/vehicle:** Real pickup과 delivery는 같은 concrete vehicle route에 정확히 한 번씩 있고 pickup index가 작다.
3. **Route-bank XOR:** Assigned request는 bank에 없고, bank request는 어느 route에도 physical visit/initial-load ownership이 없다.
4. **Delivery-only meaning:** Logical pickup은 position, node, arc, stop, service 또는 depot revisit를 만들지 않는다.
5. **Vehicle uniqueness:** 한 concrete input vehicle은 stable snapshot에서 route 하나만 소유한다. `NEW_ROUTE`는 unused vehicle만 소비한다.
6. **Terminal/single-trip:** Insertion은 start/end terminal과 oneway/roundtrip 의미를 바꾸거나 내부 depot visit을 만들지 않는다.
7. **Pure enumeration:** Position materialization과 evaluation은 base route/snapshot/bank/artifact/cache를 변경하지 않는다.
8. **Delegated feasibility:** Static compatibility, capability/zone, capacity, time/window, travel, stop/resource와 extension hard rule의 판정은 Phase 03/04 result가 유일한 권위다.
9. **Failure separation:** `Infeasible`는 정상 option rejection, `Invalid`는 authority/corruption/overflow/contract defect다. Invalid를 다음 option으로 숨기거나 rank하지 않는다.
10. **Exhaustiveness honesty:** 검사하지 않은 position/route/vehicle이 있으면 전수 infeasible이라고 말하지 않는다.
11. **Immutable identity:** Candidate, route list, bank, evaluation artifact, trace와 portfolio는 생성 후 바뀌지 않고 constructor/accessor/cross-candidate alias가 없다.
12. **Deterministic total order:** Business comparator가 다른 option을 stable key가 뒤집지 않는다. Clock, object identity, enum ordinal, hash iteration, classpath order와 random을 tie-break로 쓰지 않는다.
13. **Candidate independence:** 8개 member는 route, bank, cache, builder scratch와 random state를 공유하지 않는다. Immutable upstream authority만 공유할 수 있다.
14. **Full recomputation:** 선택된 option과 완성 candidate는 cache-free Phase 03/04 evaluation artifact와 exact identity가 같아야 한다.
15. **No downstream pull:** Seed candidate 생성 뒤 멈춘다. Phase 06 screen/ALNS와 Phase 07 verification/finalization을 실행하지 않는다.

## 4. Entry gate와 evidence 확인

문서 작성은 완료할 수 있지만 implementation/test/evidence 착수는 다음 gate가 모두 충족될 때까지 `BLOCKED`다.

| Entry 항목 | 필요한 확인/evidence | 2026-07-28 current checkout 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Accepted review, `E-P00-ARCH`, target reactor/module/package rules | Root 단일 project; target reactor 없음 | BLOCKED |
| Phase 01 accepted | `E-P01-NUMERIC`, `E-P01-TIME`, `E-P01-COMPAT`; normalized policy fingerprints | Detailed document는 존재하지만 accepted review/evidence 없음 | BLOCKED |
| Phase 02 accepted | `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`; immutable problem/travel equality | Detailed `REVIEWED_WITH_CORRECTIONS`/implementation `NOT_STARTED`; review `FINAL`/`PASS_AFTER_APPLIED_CORRECTIONS`; evidence `NOT_PRODUCED`(`NOT_AVAILABLE`), acceptance `BLOCKED_NOT_IMPLEMENTED`; accepted artifact 없음 | BLOCKED |
| [Phase 03](phase-03-route-propagation-evaluation-kernel.md) accepted | `E-P03-PROPAGATION`, `E-P03-EVALUATION`, `E-P03-COMPARATOR`; pure kernel/result/comparator | Detailed `REVIEWED_CHANGES_REQUIRED`/implementation `NOT_STARTED`/evidence `NOT_PRODUCED`/acceptance `NOT_ACCEPTED`; review `COMPLETE`/`CHANGES_REQUIRED`; accepted artifact 없음 | BLOCKED |
| [Actual Phase 04](phase-04-capabilities-customer-profiles.md) accepted | `E-P04-BINDING`, `E-P04-ISOLATION`, applicable `E-P04-FACET`; exact `BoundProfile`, existing Phase 03 declarations/comparator와 consumer equality | Detailed `REVIEWED_CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_PRODUCED`; review `COMPLETE`/`CHANGES_REQUIRED`; accepted artifact 없음 | BLOCKED |
| Full-solution evaluation contract | Ordered routes + bank의 cache-free aggregation, route artifact reuse/invalidation, exact failure/fingerprint와 comparator equality API | Phase 03/04에는 route kernel/declaration만 있고 owner/API 없음 | CROSS_PHASE_REVIEW_REQUIRED |
| Stable total order/config | Request/vehicle/route/position canonical order, CLOCK convention, utilization edge rule, exhaustive mode | 이 문서가 proposed contract를 처음 제시 | REVIEW_REQUIRED |
| API/identity review | §7~§9 internal signature, fingerprint fields, `Invalid` handling, Phase 06 compatibility | 승인 record 없음 | REVIEW_REQUIRED |
| Scheduler/owner | Exact scheduler task ID, implementer와 independent reviewer 지정 | 전달되지 않음 | OWNER_GATE |

Entry에서 다음 equality와 closure를 한 번에 확인한다.

```text
ProblemInstance.preparedTravelFingerprint
  == PreparedTravel.fingerprint

Phase04Binding.problemFingerprint
  == ProblemInstance.fingerprint

Phase04Binding.propagationDeclarationFingerprint
  == Phase03 PropagationDeclaration.fingerprint

Phase04Binding.evaluationPlanFingerprint
  == Phase03 EvaluationPlan.fingerprint

Phase04Binding.profile/config/comparator/solvePlan fingerprints
  == handoff manifest declarations

all input RequestId
  → one immutable pair/service-pattern definition

all candidate VehicleId
  → one input concrete vehicle and stable total-order key
```

Mismatch, missing component, duplicate key, unknown/latest fallback 또는 incomplete prepared travel이면 empty portfolio나 all-bank solution으로 낮추지 않고 implementation run 자체를 시작하지 않는다.

## 5. 2026-07-28 current inventory와 변경 범위

### 5.1 실제 checkout inventory

Read-only inventory 기준은 branch `codex/domain-design`, commit `3424277c9c74`다. 이 문서 작성 전 `docs/implementation/` 전체는 Git 기준 untracked였으며 기존 사용자 작업으로 취급한다.

| 항목 | 실제 관찰 | Phase 05 해석 |
|---|---|---|
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | 목표 Java/Maven과 맞지만 Phase 05 evidence가 아님 |
| Maven | Root 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT`, Java release 25, module 목록 없음 | `rpdptw-core`/`rpdptw-solver`와 architecture gate가 아직 없음 |
| Dependencies | Google Workflow/Storage, Jackson, JUnit가 같은 root classpath | Target core/solver/provider 격리 전 placeholder |
| Main Java | HTTP/GCP adapter 5개와 `AlnsBatchEngine` 1개 | Pair/insertion/portfolio 구현이 아님 |
| Placeholder algorithm | `AlnsBatchEngine`은 `double`, `SplittableRandom`, 합성 objective `Map<String,Object>`를 반환 | Candidate, comparator, seed portfolio 또는 reproducibility evidence로 재사용 금지 |
| Test | `AlnsBatchEngineTest` 1개가 합성 candidate 필드만 확인 | Phase 05 pair/oracle/isolation evidence가 아님 |
| Phase documents/reviews | Phase 00~14 detailed 15개와 review 15개 actual; Phase 04/06 review는 모두 `COMPLETE`/`CHANGES_REQUIRED`; accepted Phase evidence 없음 | 파일 존재/document review와 implementation/evidence/Phase acceptance를 분리한다. Phase 04/06은 actual read-only source로 대조했지만 accepted handoff는 아님 |

이 문서 작업은 `src`, `pom.xml`, target artifact, 공용 README/plan/progress, 다른 Phase/review와 `docs/codex`를 변경하지 않는다.

### 5.2 Proposed future change tree

아래 tree는 Phase 00~04 accepted layout에 맞춰 다시 확인할 **future target**이다. 이 문서 작성 turn에서는 어느 Java/test 파일도 만들지 않는다.

```text
rpdptw/
├── core/
│   └── src/
│       ├── main/java/com/ronext/rpdptw/
│       │   └── evaluation/insertion/
│       │       ├── PairInsertionEvaluator.java
│       │       ├── PairInsertionRequest.java
│       │       ├── PairInsertionEvaluation.java
│       │       ├── PairPositionEnumeration.java
│       │       ├── PairPositionKey.java
│       │       ├── InsertionTarget.java
│       │       ├── InsertionAttempt.java
│       │       ├── FeasibleInsertionOption.java
│       │       ├── EnumerationReceipt.java
│       │       ├── BoundInsertionAuthority.java
│       │       └── internal/
│       │           ├── ExhaustivePairPositionEnumerator.java
│       │           ├── OrderedBoundedPairPositionEnumerator.java
│       │           ├── ImmutableRouteMaterializer.java
│       │           └── DefaultPairInsertionEvaluator.java
│       └── test/java/com/ronext/rpdptw/evaluation/insertion/
│           ├── PairPositionEnumeratorTest.java
│           ├── PairInsertionEvaluatorTest.java
│           ├── PairInsertionDelegationTest.java
│           ├── PairInsertionCorruptionTest.java
│           └── PairInsertionReproducibilityTest.java
└── solver/
    └── src/
        ├── main/java/com/ronext/rpdptw/solver/
        │   ├── state/
        │   │   ├── SearchRequestBank.java
        │   │   ├── EvaluatedRoute.java
        │   │   ├── SearchSnapshot.java
        │   │   ├── SolutionFingerprint.java
        │   │   ├── StableSolutionValidator.java
        │   │   ├── ConstructionTransition.java
        │   │   └── internal/FunctionalConstructionTransition.java
        │   └── portfolio/
        │       ├── InitialPortfolioBuilder.java
        │       ├── InitialPortfolioBuildRequest.java
        │       ├── InitialPortfolioBuildResult.java
        │       ├── SeedPortfolio.java
        │       ├── PortfolioMember.java
        │       ├── ConstructionCandidate.java
        │       ├── ConstructionTrace.java
        │       ├── GrowthPolicy.java
        │       ├── VehicleOrderPolicy.java
        │       ├── PortfolioConstructionConfig.java
        │       └── internal/
        │           ├── DeterministicInitialPortfolioBuilder.java
        │           ├── ClockGrowthOrder.java
        │           ├── SequentialGrowthOrder.java
        │           └── DeterministicVehicleOrder.java
        └── test/java/com/ronext/rpdptw/solver/
            ├── state/
            │   ├── StableSolutionValidatorTest.java
            │   ├── ConstructionTransitionTest.java
            │   └── SearchSnapshotImmutabilityTest.java
            └── portfolio/
                ├── InitialPortfolioBuilderTest.java
                ├── InitialPortfolioIsolationTest.java
                ├── PortfolioPolicyOrderTest.java
                ├── PortfolioCorruptionTest.java
                └── PortfolioReproducibilityTest.java

build/test-fixtures/                         # test module → core의 test-scope edge만
└── src/test/java/com/ronext/rpdptw/testing/phase05/
    ├── PairInsertionHandOracleTest.java
    ├── Phase05OracleSensitivityTest.java
    ├── PairInsertionFixtureBuilder.java
    ├── ExhaustivePairPermutationOracle.java
    └── DelegatingBoundInsertionAuthoritySpy.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase05ArchitectureTest.java
```

`rpdptw-core` test가 `build/test-fixtures`를 소비하는 역방향 edge는 만들지 않는다. Independent core oracle은 `build/test-fixtures` test module이 test-scope로 core를 소비해 실행한다. Solver policy의 `BigInteger` expected 계산과 corruption builder는 `rpdptw-solver/src/test` 안의 test-only helper로 두며 production helper를 호출하지 않는다. Phase 00의 accepted fixture classifier/DAG가 달라지면 먼저 architecture review를 갱신한다.

다음 이름의 Phase 06 책임 type은 이 tree에 두지 않는다.

```text
TrialDraft
CompletedTrial
DestroyProposal / DestroyResult
RepairOperator / RepairResult
AcceptanceState
AdaptiveState
Phase1Champion
AlnsStep
```

## 6. Pair 위치와 feasibility 의미

### 6.1 Service position 정의

Terminal을 제외한 base service sequence 길이를 `m`이라고 한다. Position은 `RoutePlan.orderedNodeIds`의 raw index가 아니라 **terminal 바깥 service sequence의 final index**다.

Real pickup-delivery는 최종 길이 `m + 2`인 service sequence에서 다음 조합을 가진다.

```text
0 <= pickupPosition < deliveryPosition <= m + 1
```

기존 `m`개 service의 상대 순서는 유지한다. 따라서 exhaustive option 수는 다음과 같다.

```text
(m + 2 choose 2) = (m + 1)(m + 2) / 2
```

Delivery-only는 logical pickup position을 만들지 않는다. 최종 길이 `m + 1`에서 delivery position만 다음 범위를 가진다.

```text
0 <= deliveryPosition <= m
logical pickup = route initial-load ownership
```

Oneway/roundtrip terminal은 position 수에 포함하지 않으며 materialization 후 원래 terminal policy를 그대로 감싼다. 같은 physical location이라도 solver node identity와 service는 유지한다.

### 6.2 Exhaustive와 bounded enumeration

```text
EXHAUSTIVE
→ real pair는 pickup position 오름차순, 같은 pickup 안에서 delivery position 오름차순
→ delivery-only는 delivery position 오름차순
→ 모든 legal position key를 위 canonical order로 정확히 한 번
→ inspected == legal total
→ feasible 0이면 NO_FEASIBLE_INSERTION_FOR_THIS_TARGET 가능

BOUNDED_EXPLICIT
→ config에 포함된 ordered unique position key만
→ ordered key 수 <= maxEvaluations; 초과하면 실행 중 truncate하지 않고 declaration Invalid
→ inspected == ordered unique key 수 <= legal total
→ feasible 0이어도 NO_FEASIBLE_WITHIN_BOUND만 가능
```

Bound는 수치 하나로 loop를 임의 중단하는 방식이 아니다. `policyKey/version`, ordered position keys 또는 이를 만드는 승인된 deterministic declaration, `maxEvaluations`, completeness receipt와 fingerprint가 필요하다. Declaration을 bind할 때 duplicate/out-of-range key와 `orderedUniquePositions.size() > maxEvaluations`를 fail-closed로 거부한다. 실행은 승인된 ordered set 전체를 정확히 한 번 평가하며 clock elapsed, allocation pressure, first-feasible break와 unordered sampling은 enumeration completeness를 바꿀 수 없다.

Phase 05 initial portfolio와 `E-P05-INSERTION` correctness gate는 `EXHAUSTIVE`를 사용한다. Bounded seam은 [actual but unaccepted Phase 06](phase-06-cow-alns-reproducibility.md)의 explicit repair config가 나중에 소비할 수 있는 proposed contract일 뿐 Phase 05 candidate 수나 결과를 몰래 줄이지 않는다.

### 6.3 Evaluation 위임과 result triage

각 materialized route의 structural/physical feasibility는 Phase 03/04가 bind한 단일 authority로 평가한다.

```text
request + target + position
→ structural materialization
→ Phase 04 bound static/compatibility gate
→ Phase 03 RouteEvaluationKernel full propagation/evaluation
→ approved full-solution projection/evaluation/comparator
→ FEASIBLE | REJECTED | INVALID
```

마지막 full-solution 단계의 exact owner/API/identity/failure/comparator contract는 현재 Phase 03/04/05 문서 사이에서 닫히지 않았다. Phase 03은 route-level kernel을, Phase 04는 bound declaration을 제공하지만 이 문서의 proposed `CandidateEvaluationArtifact`/`CandidateRankingArtifact`를 만드는 승인된 solution evaluator는 없다. Mandatory unassigned count, used vehicle와 ownership objective처럼 solution 전체를 읽는 dimension을 route artifact만으로 계산하거나 delta로 추정해서는 안 된다.

따라서 last safe point는 다음과 같다.

- Route materialization과 Phase 03/04 route-level `Feasible/Infeasible/Invalid` 의미만 고정한다.
- Solution ranking artifact, `ConstructionTransition`의 fresh candidate evaluation과 portfolio comparison API는 Phase 03/04/05 공동 contract 승인 전 구현하지 않는다.
- 임시 route delta, search aggregate, placeholder `double objective` 또는 Phase 05 전용 objective 복제는 금지한다.
- Restart는 problem/travel/profile + ordered routes + bank를 입력으로 하는 cache-free solution evaluator, route-artifact reuse/invalidation, exact aggregation/fingerprint, failure precedence와 business-equality comparator test가 승인될 때만 가능하다.

| Result | 의미 | Phase 05 동작 |
|---|---|---|
| `FEASIBLE` | Exact bound profile 아래 route/solution option이 hard-feasible | Immutable option으로 보존하고 ranking에 포함 |
| `REJECTED(ConstraintRejection)` | Compatibility, capacity, time, resource 또는 bound hard rule의 정상 실패 | Option rejection count/trace에만 포함; ranking 제외 |
| `INVALID(EvaluationFailure)` | Fingerprint mismatch, partial/corrupt route, missing authority, overflow, component defect | Enumeration/build를 fail-closed; 다음 option으로 숨기지 않음 |

Phase 05는 rejection code를 얻기 위해 size/capability/zone/load/time/resource 공식을 다시 계산하지 않는다. Pre-filter를 두더라도 Phase 04가 전달한 immutable precomputed fact/bitset과 exact fingerprint만 읽으며, authoritative result를 대체하거나 새로운 rejection reason을 만들 수 없다.

한 insertion failure 또는 bounded miss는 final unassignment 진단이 아니다. Phase 07 final-solution insertion audit의 completeness/confidence와 공유하지 않는다.

## 7. Artifact, contract, identity와 lifecycle

### 7.1 Input/output artifact

| Artifact | Owner | Identity 최소 구성 | Phase 05 lifecycle/소비자 |
|---|---|---|---|
| `ProblemInstance` | Phase 02 | Dense IDs, pair/service/vehicle/terminal/compatibility facts와 policy fingerprint | Immutable read-only |
| `PreparedTravel` | Phase 02 | Complete directed coverage, location/vehicle mapping, source policy fingerprint | Exact equality 뒤 read-only |
| `PropagationDeclaration` | Phase 03, Phase 04 bind | Service-window/facet contract와 version | Immutable bound input |
| `EvaluationPlan` | Phase 03, Phase 04 bind | Ordered hard/metric/score/objective/comparator/tie declarations | Immutable bound input |
| `BoundProfile`/`SolvePlan` | Phase 04 | Exact customer/profile/version/preset/config/dependency closure | Immutable; customer name 분기 없이 소비 |
| `BoundInsertionAuthority` | Phase 03/04 handoff의 Phase 05 view | 위 authority fingerprints와 route/solution evaluation contract version | Call-local invocation; Phase 05가 구현 의미를 복제하지 않음 |
| `SearchRequestBank` | Phase 05 solver state | Problem identity + ordered unique bank request IDs | Snapshot마다 immutable value |
| `EvaluatedRoute` | Phase 05 조립 | Vehicle, `RoutePlan`, `RouteEvaluationArtifact` identities | Snapshot에 immutable 포함 |
| `SearchSnapshot` | Phase 05 construction | 모든 authority, routes, bank, candidate evaluation과 semantic content | Stable immutable seed solution |
| `FeasibleInsertionOption` | Pure evaluator | Base target/request/position/result route/evaluation/ranking identities | 선택 또는 폐기; base와 mutable alias 없음 |
| `ConstructionCandidate` | Portfolio member build | Member policy/config/trace + final `SearchSnapshot` | Phase 06 warm-start 후보 |
| `SeedPortfolio` | Phase 05 | Canonical matrix trace + available candidates + unavailable records | 생성 후 immutable; Phase 06만 seed set으로 소비 |

Fingerprint encoding/hash algorithm은 Phase 00/serialization authority가 소유한다. 이 Phase는 semantic field를 누락하지 않는 것을 고정한다.

### 7.2 Identity 최소 구성

`SearchSnapshot` semantic identity:

```text
ProblemFingerprint
PreparedTravelFingerprint
PropagationDeclarationFingerprint
EvaluationPlanFingerprint
BoundProfileFingerprint
SolvePlanFingerprint
ordered [
  VehicleId
  RoutePlanFingerprint
  RouteEvaluationArtifactFingerprint
]
ordered SearchRequestBank RequestIds
CandidateEvaluationArtifactFingerprint
stable solution contract version
```

`CandidateEvaluationArtifactFingerprint`는 필요한 semantic identity field를 나타내는 placeholder다. 승인된 full-solution evaluator와 artifact contract가 없으므로 현재 이 field를 route artifact 합, mutable aggregate 또는 placeholder scalar에서 생성할 수 없다.

`FeasibleInsertionOption` identity:

```text
requestId
target kind (EXISTING | NEW_ROUTE)
target concrete vehicleId
base route fingerprint or explicit empty-new-route identity
service-pattern-specific position key
position-enumeration declaration fingerprint
materialized route fingerprint
route evaluation artifact fingerprint
candidate ranking/objective fingerprint
bound authority fingerprints
```

`ConstructionCandidate` identity:

```text
PortfolioMemberId
growth policy key/version
vehicle order policy key/version
portfolio/enumeration/tie config fingerprint
ordered request/vehicle/target/position decision trace digest
final SearchSnapshot fingerprint
```

`SeedPortfolio` identity:

```text
authority fingerprints
canonical 4×2 matrix declaration
ordered member status and lineage
ordered available candidate fingerprints derived from member order
portfolio contract version
```

같은 solution bytes를 만든 두 policy member는 같은 `SolutionFingerprint`를 가질 수 있다. 그러나 서로 다른 `PortfolioMemberId`와 construction lineage를 보존하며 mutable object를 공유하지 않는다. Policy identity를 solution의 물리 내용에 섞어 같은 해를 다른 해로 위장하지 않는다.

Raw elapsed, thread ID, object address, map iteration order, log correlation, current timestamp와 provider locator는 semantic identity에 포함하지 않는다.

### 7.3 Lifecycle과 state transition

```text
accepted immutable Phase 02/03/04 authorities
→ empty stable SearchSnapshot
    routes = []
    bank = all RequestId in canonical order
→ pure position enumeration/materialization/evaluation
→ selected FeasibleInsertionOption
→ functional ConstructionTransition
    route replace/create + bank request removal + fresh evaluation
→ next immutable SearchSnapshot
→ all construction attempts completed
→ cache-free candidate validation
→ ConstructionCandidate
→ ordered SeedPortfolio
→ Phase 06 seed input
```

Phase 05에는 long-lived mutable `current`, `stageBest`, `solveBest`가 없다. Builder scratch는 외부에 노출하지 않고 member 종료 시 폐기한다. Snapshot 간 structural sharing을 사용하려면 공유되는 객체가 transitively immutable이고 accessor로 mutable backing을 노출하지 않는다는 no-alias test가 필요하다.

### 7.4 Proposed Java 25 contract

아래 `public`은 module contract shape를 보여 주기 위한 표기다. External/public API 승인이 아니다.

```java
package com.ronext.rpdptw.evaluation.insertion;

public sealed interface PairPositionKey
        permits PairPositionKey.RealPair,
                PairPositionKey.DeliveryOnly {

    record RealPair(int pickupPosition, int deliveryPosition)
        implements PairPositionKey {
        // 0 <= pickupPosition < deliveryPosition <= baseServiceCount + 1
    }

    record DeliveryOnly(int deliveryPosition)
        implements PairPositionKey {
        // 0 <= deliveryPosition <= baseServiceCount
    }
}

public sealed interface PairPositionEnumeration
        permits PairPositionEnumeration.Exhaustive,
                PairPositionEnumeration.ExplicitBounded {

    EnumerationPolicyFingerprint fingerprint();

    record Exhaustive(
        EnumerationPolicyVersion version,
        EnumerationPolicyFingerprint fingerprint
    )
        implements PairPositionEnumeration {}

    record ExplicitBounded(
        EnumerationPolicyKey key,
        EnumerationPolicyVersion version,
        List<PairPositionKey> orderedUniquePositions,
        int maxEvaluations,
        EnumerationPolicyFingerprint fingerprint
    ) implements PairPositionEnumeration {}
}
```

위 record의 canonical constructor는 null, duplicate/out-of-range key, count overflow와 `orderedUniquePositions.size() > maxEvaluations`를 거부하고 `List.copyOf`로 방어 복사해야 한다. 다른 모든 collection-bearing record도 같은 defensive immutable constructor/accessor rule을 적용한다. 주석만으로 validation/immutability를 구현했다고 간주하지 않는다.

```java
public sealed interface InsertionTarget
        permits InsertionTarget.ExistingRoute,
                InsertionTarget.NewRoute {

    VehicleId vehicleId();

    record ExistingRoute(
        VehicleId vehicleId,
        RoutePlan baseRoute,
        RouteEvaluationArtifact baseEvaluation
    ) implements InsertionTarget {}

    record NewRoute(
        VehicleId vehicleId,
        NewRouteTerminalDeclaration terminalDeclaration
    ) implements InsertionTarget {}
}

public record PairInsertionRequest(
    ProblemInstance problem,
    PreparedTravel preparedTravel,
    RequestId requestId,
    InsertionTarget target,
    PairPositionEnumeration enumeration,
    BoundInsertionAuthority authority,
    PairInsertionRequestFingerprint fingerprint
) {}
```

`NewRouteTerminalDeclaration`은 Phase 02 vehicle/terminal policy를 가리키는 immutable reference다. 임의 terminal, vehicle class/count sentinel 또는 새 vehicle을 만들 수 없다.

```java
public interface BoundInsertionAuthority {
    BoundInsertionResult evaluate(
        RequestId requestId,
        InsertionTarget target,
        PairPositionKey position,
        RoutePlan materializedRoute
    );

    BoundAuthorityFingerprint fingerprint();
}

public sealed interface BoundInsertionResult
        permits BoundInsertionResult.Feasible,
                BoundInsertionResult.Rejected,
                BoundInsertionResult.Invalid {

    record Feasible(
        RouteEvaluationArtifact routeEvaluation,
        CandidateRankingArtifact candidateRanking
    ) implements BoundInsertionResult {}

    record Rejected(ConstraintRejection rejection)
        implements BoundInsertionResult {}

    record Invalid(EvaluationFailure failure)
        implements BoundInsertionResult {}
}
```

`BoundInsertionAuthority`의 최종 이름과 placement는 Phase 03/04/05 cross-phase review가 정한다. Actual Phase 04 §7.4는 `BoundProfile`의 `PropagationDeclaration`/`EvaluationPlan`을 기존 Phase 03 `RouteEvaluationKernel`에 그대로 전달하는 계약을 고정한다. 따라서 이 이름은 그 호출을 Phase 05 안에서 묶는 **proposed consumer adapter/view**일 뿐 새 Phase 04 provider나 별도 compatibility/load/time evaluator가 아니다. 아래 `CandidateRankingArtifact`는 승인된 solution evaluator가 아직 없으므로 compile-ready contract가 아니며 이 unresolved type을 ad hoc route delta로 채워 구현해서는 안 된다.

```java
public interface PairInsertionEvaluator {
    PairInsertionEvaluation evaluate(PairInsertionRequest request);
}

public sealed interface PairInsertionEvaluation
        permits PairInsertionEvaluation.Completed,
                PairInsertionEvaluation.Invalid {

    record Completed(
        List<InsertionAttempt> orderedAttempts,
        List<FeasibleInsertionOption> orderedFeasibleOptions,
        EnumerationReceipt receipt
    ) implements PairInsertionEvaluation {}

    record Invalid(
        EvaluationFailure failure,
        Optional<PairPositionKey> safePosition
    ) implements PairInsertionEvaluation {}
}

public sealed interface InsertionAttempt
        permits InsertionAttempt.Feasible,
                InsertionAttempt.Rejected {

    PairPositionKey position();

    record Feasible(
        PairPositionKey position,
        FeasibleInsertionOption option
    ) implements InsertionAttempt {}

    record Rejected(
        PairPositionKey position,
        ConstraintRejection rejection
    ) implements InsertionAttempt {}
}

public record FeasibleInsertionOption(
    RequestId requestId,
    InsertionTarget target,
    PairPositionKey position,
    RoutePlan resultRoute,
    RouteEvaluationArtifact routeEvaluation,
    CandidateRankingArtifact candidateRanking,
    StableInsertionTieKey stableTieKey,
    InsertionOptionFingerprint fingerprint
) {}

public record EnumerationReceipt(
    EnumerationCompleteness completeness, // EXHAUSTIVE | BOUNDED
    long legalPositionCount,
    long evaluatedPositionCount,
    long feasiblePositionCount,
    long rejectedPositionCount,
    EnumerationPolicyFingerprint policyFingerprint
) {}
```

Count 계산도 checked arithmetic을 사용한다. Position 수 overflow, duplicate/out-of-range bound key 또는 receipt 불일치는 `Invalid(ComponentContractViolation)`이며 일부 결과를 publish하지 않는다.

Solver state는 core insertion package를 소비한다.

```java
package com.ronext.rpdptw.solver.state;

public record SearchRequestBank(
    ProblemFingerprint problemFingerprint,
    List<RequestId> orderedUniqueRequestIds,
    SearchRequestBankFingerprint fingerprint
) {}

public record EvaluatedRoute(
    VehicleId vehicleId,
    RoutePlan routePlan,
    RouteEvaluationArtifact evaluation
) {}

public record SearchSnapshot(
    SolveAuthorityRef authority,
    List<EvaluatedRoute> routesInStableVehicleOrder,
    SearchRequestBank bank,
    CandidateEvaluationArtifact evaluation,
    SolutionFingerprint fingerprint
) {}

public interface ConstructionTransition {
    ConstructionTransitionResult apply(
        SearchSnapshot base,
        FeasibleInsertionOption option
    );
}

public sealed interface ConstructionTransitionResult
        permits ConstructionTransitionResult.Applied,
                ConstructionTransitionResult.StaleOrMismatched,
                ConstructionTransitionResult.Invalid {

    record Applied(SearchSnapshot next)
        implements ConstructionTransitionResult {}

    record StaleOrMismatched(TransitionRejection rejection)
        implements ConstructionTransitionResult {}

    record Invalid(EvaluationFailure failure)
        implements ConstructionTransitionResult {}
}
```

`apply`는 다음을 모두 확인한 뒤에만 `Applied`를 만든다.

- Request가 base bank에 정확히 한 번 있고 어느 route에도 없다.
- Existing target의 base route/evaluation fingerprint가 snapshot과 exact 일치한다.
- New target의 concrete vehicle이 input fleet에 있고 base에서 unused다.
- Option의 materialized route가 complete pair, terminal과 target vehicle identity를 보존한다.
- Option의 bound authority와 snapshot authority fingerprints가 같다.
- Route replacement/create와 bank removal 뒤 fresh candidate evaluation/fingerprint가 일치한다.

실패 시 base object뿐 아니라 base canonical bytes와 fingerprint가 같아야 한다.

Portfolio contract:

```java
package com.ronext.rpdptw.solver.portfolio;

public enum GrowthPolicy {
    CLOCK,
    SEQ_FARTHEST,
    SEQ_LARGE_DEMAND,
    SEQ_EARLIEST_DEADLINE
}

public enum VehicleOrderPolicy {
    DIRECT_FIRST_LARGE,
    DIRECT_FIRST_SMALL
}

public record PortfolioMemberId(
    GrowthPolicy growthPolicy,
    VehicleOrderPolicy vehicleOrderPolicy
) {}

public sealed interface PortfolioMember
        permits PortfolioMember.Available,
                PortfolioMember.Unavailable {

    PortfolioMemberId id();

    record Available(
        PortfolioMemberId id,
        ConstructionCandidate candidate
    ) implements PortfolioMember {}

    record Unavailable(
        PortfolioMemberId id,
        PortfolioUnavailableReason reason,
        UnavailableEvidence evidence
    ) implements PortfolioMember {}
}

public record ConstructionCandidate(
    PortfolioMemberId memberId,
    PortfolioConstructionConfigFingerprint configFingerprint,
    ConstructionTrace trace,
    SearchSnapshot seedSolution,
    ConstructionCandidateFingerprint fingerprint
) {}

public record SeedPortfolio(
    SolveAuthorityRef authority,
    List<PortfolioMember> membersInCanonicalMatrixOrder,
    SeedPortfolioFingerprint fingerprint
) {
    // availableCandidates는 members의 Available 항목에서 canonical order로
    // 계산한 immutable projection이며 두 번째 저장 authority가 아니다.
}

public interface InitialPortfolioBuilder {
    InitialPortfolioBuildResult build(InitialPortfolioBuildRequest request);
}

public sealed interface InitialPortfolioBuildResult
        permits InitialPortfolioBuildResult.Succeeded,
                InitialPortfolioBuildResult.Invalid {

    record Succeeded(SeedPortfolio portfolio)
        implements InitialPortfolioBuildResult {}

    record Invalid(EvaluationFailure failure)
        implements InitialPortfolioBuildResult {}
}
```

`Unavailable`은 현재 `CLOCK_COORDINATE_MISSING`처럼 source가 허용한 expected condition만 사용한다. Kernel `Invalid`, stale fingerprint, alias, transition defect 또는 candidate full-evaluation mismatch를 `Unavailable`로 낮추지 않는다.

`SeedPortfolio`는 member 목록과 별도의 mutable/independently supplied candidate 목록을 두지 않는다. Available candidate projection은 canonical member 목록에서 유일하게 파생하며 projection count/order/fingerprint equality를 constructor와 corruption test가 검증한다.

### 7.5 Dependency direction

```text
rpdptw-core:
domain + travel
  → propagation + evaluation.api/runtime
    → evaluation.insertion

rpdptw-solver:
core immutable/evaluation/insertion contracts
  → solver.state immutable seed values
    → solver.portfolio

Actual Phase 06:
solver.state + solver.portfolio seed contracts
  → COW ALNS implementation
```

금지 방향:

- `rpdptw-core/evaluation.insertion → rpdptw-solver`
- `rpdptw-core → customer profile implementation`, cloud/provider/vendor SDK
- `solver.portfolio → Phase 06 destroy/repair/acceptance/adaptive/termination`
- `solver.state → mutable cache`, application, adapter, verifier 또는 result finalization
- Phase 03/04가 Phase 05를 위해 raw input을 다시 읽거나 insertion mutation을 수행
- Phase 06이 `SeedPortfolio` 내부 builder scratch나 mutable references를 받는 경로

## 8. Atomic insertion transition pseudocode

### 8.1 Pure route option evaluation

```text
evaluatePairInsertion(request):
  verify all authority fingerprints
  verify request exists and target vehicle/route identity is well-formed
  derive legal service-position count from service pattern
  resolve explicit enumeration declaration
  reject duplicate/out-of-range/overflowed keys as Invalid

  attempts = []
  feasible = []

  for position in canonical declared order:
    materialized = immutableRouteMaterializer.insertCompleteRequest(
        base route or empty terminal route,
        request,
        position
    )

    # No compatibility/capacity/time/resource formula here.
    result = boundInsertionAuthority.evaluate(
        request.id,
        target,
        position,
        materialized
    )

    switch result:
      Feasible(routeArtifact, ranking):
        option = immutable option with exact identity
        append Feasible attempt and option
      Rejected(constraintEvidence):
        append Rejected attempt
      Invalid(failure):
        discard partial lists
        return Invalid(failure, position)

  verify receipt counts and completeness
  stable-sort feasible by:
    bound business comparator
    then StableInsertionTieKey
  return Completed(attempts, feasible, receipt)
```

Materializer는 real pair를 한 번에 배치하고 delivery-only면 delivery visit과 logical initial-load ownership을 함께 표현한다. 중간 partial route를 callback, observer, comparator 또는 cache에 노출하지 않는다.

### 8.2 Functional apply

```text
apply(baseSnapshot, selectedOption):
  verify option authority == base authority
  verify request is bank-only in base
  verify target base fingerprint or NEW_ROUTE unused vehicle
  verify option is internally complete and evaluation identity matches

  nextRoutes =
    existing target ? immutable replace exactly one route
                    : immutable insert one vehicle route in stable order

  nextBank = immutable remove exactly one RequestId
  nextEvaluation = approved full-solution evaluator(
      authority, nextRoutes, nextBank, reusable route artifacts)
  next = new immutable SearchSnapshot(nextRoutes, nextBank, nextEvaluation)
  validate stable partition and all identities
  return Applied(next)

on any rejection/exception/invalid:
  expose no next snapshot
  assert canonicalBytes(base) and fingerprint(base) unchanged
```

Phase 05에는 “apply 후 undo”가 없다. 함수형 전이가 실패하면 새 value를 폐기한다. Phase 06이 seed에서 changed-route COW를 시작하는 것은 별도 implementation/evidence다.

위 `approved full-solution evaluator`는 설명용 placeholder이며 현재 승인된 Java contract가 아니다. §6.3의 cross-Phase blocker가 닫히기 전 `ConstructionTransition`과 portfolio builder의 compile freeze를 해제하지 않는다.

### 8.3 Stable option tie key

Bound comparator가 `left != right`이면 그 결과가 최종이다. Objective가 완전 동률일 때만 다음 canonical tuple을 비교한다.

```text
RequestId stable key
→ vehicle ownership rank: DIRECT before LEASE
→ VehicleId stable key
→ target kind/key: existing concrete-vehicle route or NEW_ROUTE
→ service-pattern tag
→ pickup position, then delivery position
→ result RoutePlanFingerprint as final corruption-resistant discriminator
```

`DIRECT_FIRST_LARGE/SMALL` 정책 rank는 vehicle/request traversal을 정한다. 이미 다른 business objective인 option을 tie key로 뒤집지 않는다. Elapsed, “먼저 완료된 future”, object identity와 random draw는 key에 들어가지 않는다.

Phase 03의 business comparator equality API, solution-level stable tie와 이 insertion-context tie의 owner/identity는 아직 공동 승인되지 않았다. 또한 existing-route 대 `NEW_ROUTE` target kind/key의 exact byte/order는 현재 proposed 상태다. Last safe point는 “business vector가 다르면 tie가 뒤집지 않는다”와 stable semantic field 집합이다. Phase 03/05 comparator API, explicit target-key order/version, transitivity/antisymmetry와 exhaustive option-order test가 승인되기 전 exact type/encoding을 freeze하지 않는다.

## 9. Initial portfolio construction

### 9.1 Canonical policy matrix

Matrix member order는 explicit config에 다음 순서를 canonical로 선언한다.

```text
CLOCK × DIRECT_FIRST_LARGE
CLOCK × DIRECT_FIRST_SMALL
SEQ_FARTHEST × DIRECT_FIRST_LARGE
SEQ_FARTHEST × DIRECT_FIRST_SMALL
SEQ_LARGE_DEMAND × DIRECT_FIRST_LARGE
SEQ_LARGE_DEMAND × DIRECT_FIRST_SMALL
SEQ_EARLIEST_DEADLINE × DIRECT_FIRST_LARGE
SEQ_EARLIEST_DEADLINE × DIRECT_FIRST_SMALL
```

Java enum ordinal에 의존하지 않는다. Key/version/order는 `PortfolioConstructionConfigFingerprint`에 포함한다.

### 9.2 Growth policy 의미

| Policy | 고정 의미 | Exact tie/authority | 금지 |
|---|---|---|---|
| `CLOCK` | 선택 vehicle start terminal/depot을 원점으로 request entry location을 0도부터 clockwise 순회 | Explicit coordinate convention/version 뒤 stable request ID | 좌표 누락 시 farthest/ID order fallback |
| `SEQ_FARTHEST` | Depot에서 directed distance가 가장 먼 request를 seed로 하고 이후 현재 route 마지막 확정 service location에서 가까운 request 우선 | Phase 02 prepared `D[current][entry]`, stable request ID | Reverse/symmetric/raw coordinate distance |
| `SEQ_LARGE_DEMAND` | 선택 vehicle 대비 `max(weight ratio, volume ratio)`가 큰 request seed; 동률이면 이른 `reqDate`, stable request ID; 이후 가까운 request | Phase 04 exact utilization edge policy와 Phase 02 prepared `D` | `double`, Feature 숫자 parsing, hidden zero-capacity rule |
| `SEQ_EARLIEST_DEADLINE` | 이른 `reqDate` seed; 동률이면 utilization 큰 request, stable request ID; 이후 가까운 request | Normalized deadline와 exact rational utilization | Raw date/string 또는 input list order |

Request entry location:

```text
REAL_PICKUP_DELIVERY → physical pickup location
DELIVERY_ONLY        → delivery location
```

“가까움”은 `D[currentServiceLocation][requestEntryLocation]`의 directed integer meter다. 같은 거리면 stable request ID가 결정한다. 현재 route에 확정 customer service가 없으면 그 vehicle의 start terminal location을 anchor로 사용한다.

여러 route를 어떻게 순회하며 다음 `(request,target)`을 고르는지의 정확한 traversal은 source에서 API 수준으로 확정되지 않았다. Proposed baseline은 다음이다.

1. Member마다 all-bank empty snapshot에서 시작한다.
2. Vehicle policy로 concrete target vehicle/route context를 stable 정렬한다.
3. 각 target context에서 growth policy가 아직 시도하지 않은 bank request rank를 만든다.
4. `(growth rank, vehicle rank, stable request ID, target key)`의 ordered request-target 후보를 만든다.
5. 한 request에 대해 existing eligible route와 모든 unused concrete `NEW_ROUTE` target의 exhaustive positions를 평가한다.
6. Feasible option을 bound comparator, §8.3 tie key로 고르고 함수형 적용한다.
7. 모든 target에서 정상 rejection이면 request는 bank에 남고 construction trace에만 기록한다.
8. 각 request를 member 안에서 최소 한 번, policy가 정한 deterministic scope로 시도한 뒤 cache-free final evaluation을 수행한다.

이 traversal은 Phase 05 review에서 승인하거나 원문의 의미를 보존하는 대체 internal traversal로 교체해야 한다. 승인 전 first-feasible, input order 또는 collection order를 hidden contract로 굳히지 않는다.

### 9.3 Vehicle order 의미

두 정책 모두 모든 feasible `DIRECT` concrete vehicle을 모든 `LEASE` concrete vehicle보다 먼저 둔다.

```text
utilization(request, vehicle)
= max(
    requestWeight / vehicleWeightCapacity,
    requestVolume / vehicleVolumeCapacity
  )
```

- `DIRECT_FIRST_LARGE`: 낮은 utilization부터, 즉 현재 request에 상대적으로 큰 vehicle 우선.
- `DIRECT_FIRST_SMALL`: 높은 utilization부터, 즉 현재 request에 상대적으로 작은 vehicle 우선.
- Ratio 비교는 normalized integer의 exact rational cross-product 또는 동등한 overflow-safe 방식으로 수행한다.
- Missing/zero capacity는 Phase 04 bound edge policy가 typed ineligible/rejection 또는 exact 의미를 제공해야 한다. Phase 05가 `0`, infinity 또는 epsilon을 만들지 않는다.
- Utilization 동률은 ownership rank 뒤 stable `VehicleId`로 결정한다.
- `vehicleFeature` 문자열의 숫자·사전식 순서·입력 나열 순서로 크기를 추론하지 않는다.

### 9.4 Member availability와 failure

`CLOCK` member preflight에서 필요한 depot 또는 request entry coordinate 하나라도 없으면 그 member는 `UNAVAILABLE(CLOCK_COORDINATE_MISSING)`이다. 두 CLOCK member가 unavailable이어도 나머지 여섯 조합은 독립적으로 생성할 수 있다.

다음은 정상 available member의 일부다.

- 모든 request가 삽입되어 bank가 빈 candidate
- 일부 request가 exhaustive normal rejection 뒤 bank에 남은 partial-quality candidate
- 모든 request가 bank에 남지만 stable partition과 exact evaluation을 만족한 candidate

다음은 member unavailable이나 낮은 품질이 아니라 build `Invalid`다.

- Partial/duplicate/split/reverse pair
- Kernel/binding `Invalid`
- Stale/mismatched option, route, evaluation 또는 profile fingerprint
- Route/vehicle 중복, bank corruption, constructor/accessor alias
- Candidate cache-free evaluation mismatch
- Enumeration receipt/count/order corruption

### 9.5 Portfolio build pseudocode

```text
buildInitialPortfolio(authorities, config):
  verify entry fingerprints/config/total orders
  members = []

  for memberId in config.canonicalPolicyMatrix:
    if memberId.growth == CLOCK and clockPreflightMissingCoordinate:
      members += Unavailable(memberId, exact missing identities)
      continue

    state = empty stable snapshot:
      routes = []
      bank = all requests in stable order
      full candidate evaluation under Phase 04 binding

    trace = new member-local immutable trace builder
    for request-target decision in approved deterministic traversal:
      evaluations = pairEvaluator(EXHAUSTIVE)
      if evaluations is Invalid:
        discard entire member and return portfolio Invalid
      if feasible options is empty:
        keep state unchanged; record normal rejections
      else:
        selected = min(bound comparator, then stable tie)
        applied = constructionTransition.apply(state, selected)
        if not Applied:
          discard entire member and return portfolio Invalid
        state = applied.next
        record exact identities and rejection counts

    finalState = cache-free full candidate evaluation
    validate stable pair/bank/vehicle/terminal invariants
    candidate = immutable ConstructionCandidate(memberId, trace, finalState)
    members += Available(memberId, candidate)

  portfolio = immutable SeedPortfolio(members)
  verify portfolio.availableCandidates
      == canonical projection of Available members
  return portfolio
```

Phase 05는 `screenMaxSteps`를 읽지 않고 각 member의 ALNS를 호출하지 않으며 champion을 고르지 않는다. Actual Phase 06 §8.3은 available 4×2 slot과 unavailable CLOCK slot을 받아 explicit test/experiment run config로 screen하고 stable champion을 고르는 책임을 자기 Phase에 둔다.

## 10. Independent oracle과 exact test plan

모든 test/type은 **future**다. 현재 파일 또는 실행 evidence가 아니다.

### 10.1 Oracle 독립성

- `ExhaustivePairPermutationOracle`은 final service permutation을 작은 범위에서 전수 생성한 뒤 기존 service 상대 순서, pair completeness와 precedence를 독립 filter한다. Production position enumerator를 호출하지 않는다.
- `BigIntegerLoadAndUtilizationOracle`은 prefix load와 rational utilization cross-product를 `BigInteger`로 계산하여 production checked-`long`/comparison bug를 반복하지 않는다.
- Hand fixture expected sequence, option count, feasible/rejected 위치와 selected tie key는 literal table로 보존한다.
- `DelegatingBoundInsertionAuthoritySpy`는 option별 predeclared `Feasible/Rejected/Invalid`를 반환하고 compatibility/capacity/time/resource 호출 경로와 invocation count를 기록한다.
- `CorruptSeedPortfolioBuilder`는 승인되지 않은 serialization 형식에 의존하지 않는다. Validation boundary가 받는 test-only field projection을 독립 조립해 pair, bank, route, artifact, fingerprint와 alias를 한 필드씩 손상한다. Canonical encoding이 승인된 뒤에만 별도 byte-corruption fixture를 추가한다.
- Expected fingerprint bytes를 production `toString()`, hash map iteration 또는 production builder로 만들지 않는다.
- `Phase05OracleSensitivityTest`는 production 구현을 호출해 expected를 생성하지 않고 누락 position, pickup/delivery reverse, delivery-only fake pickup visit, reverse `D` lookup, duplicate authority call, `Invalid→Rejected`, comparator 우선순위 역전과 cross-member alias를 각각 심은 faulty double이 oracle/assertion에서 실제 red가 되는지 증명한다.

### 10.2 Hand-calculated exhaustive small fixture

`PairInsertionFixtureBuilder.capacitySeparatesSixRealPairPositions()`는 다음 **test-only integer** fixture를 만든다. Official default/calibration이 아니다.

```text
vehicle V1:
  roundtrip D
  weightCapacity = 5
  volumeCapacity = 5
  all service/time/resource windows wide enough

existing request R1:
  real pickup P1 = +3
  real delivery D1 = -3
  base service sequence = [P1, D1]

bank request R2:
  real pickup P2 = +4
  real delivery D2 = -4

prepared travel:
  every distinct required arc D=1 meter, U=1 second
  self arc D=0, U=0

service:
  all serviceSeconds = 0
```

Base service count `m=2`이므로 legal position pair는 6개다.

| `(pickupPosition, deliveryPosition)` | Final service sequence | Prefix weight | Expected |
|---|---|---|---|
| `(0,1)` | `P2,D2,P1,D1` | `4,0,3,0` | FEASIBLE |
| `(0,2)` | `P2,P1,D2,D1` | `4,7,3,0` | REJECTED capacity at 7 |
| `(0,3)` | `P2,P1,D1,D2` | `4,7,4,0` | REJECTED capacity at 7 |
| `(1,2)` | `P1,P2,D2,D1` | `3,7,3,0` | REJECTED capacity at 7 |
| `(1,3)` | `P1,P2,D1,D2` | `3,7,4,0` | REJECTED capacity at 7 |
| `(2,3)` | `P1,D1,P2,D2` | `3,0,4,0` | FEASIBLE |

모든 distinct arc cost가 같아 두 feasible option의 business vector가 동률이면 stable position tie가 `(0,1)`을 선택한다. Capacity 판정 자체는 production Phase 03/04 authority가 반환하며 Phase 05 enumerator가 위 표의 합을 구현하지 않는다. `BigIntegerLoadAndUtilizationOracle`과 literal expected table이 독립 검증한다.

Delivery-only fixture는 base `m=2`에 delivery position `0,1,2`만 만들고 logical pickup position/node/arc/stop이 0개임을 확인한다.

### 10.3 Exact test class/method matrix

| Layer/Class | Exact method | Fixture/builder/oracle | Green pass criteria |
|---|---|---|---|
| Core `PairPositionEnumeratorTest` | `enumeratesEveryLegalRealPairPositionExactlyOnce()` | Test-local independent expected table/generator, `m=0..6` | Count `C(m+2,2)`, all `p<d`, duplicate/missing 0, canonical order exact |
|  | `enumeratesDeliveryOnlyDeliveryPositionsWithoutLogicalPickupVisit()` | delivery-only `m=0..6` | Exactly `m+1`; pickup position/node/arc/stop 0 |
|  | `rejectsDuplicateOutOfRangeAndOverflowedBoundDeclarations()` | corrupt bounded config | Partial list publish 없이 typed `Invalid` |
|  | `boundedMissNeverClaimsExhaustiveInfeasibility()` | bound excluding known feasible position | `BOUNDED`, `NO_FEASIBLE_WITHIN_BOUND`; exhaustive reason 금지 |
| External test module `PairInsertionHandOracleTest` | `enumeratesSixOptionsAcceptsTwoAndSelectsStableFirst()` | §10.2 table + two independent oracles | 6/2/4 receipt, rejection codes delegated, selected `(0,1)` |
| Core `PairInsertionEvaluatorTest` | `materializesCompleteSameRoutePairForEveryOption()` | real pair route builder | Every materialized route complete, same vehicle, precedence; base bytes unchanged |
|  | `createsDeliveryOnlyInitialLoadOwnershipWithoutFakeVisit()` | mixed service builder | Delivery visit + ownership only; logical pickup physical effects 0 |
|  | `evaluatesNewRouteAgainstUnusedConcreteVehicleAndTerminal()` | two-vehicle fixture | Exact `VehicleId`, approved terminal; sentinel/new vehicle 0 |
|  | `ordersFeasibleOptionsByBusinessComparatorThenStableTie()` | tied/non-tied ranking stub | Non-tied vector never reversed; tied total order deterministic |
| Core `PairInsertionDelegationTest` | `delegatesCompatibilityCapacityTimeAndResourceWithoutReimplementation()` | spy returning codes for size/capability/zone/capacity/time/resource | One authority call per position; Phase 05 condition/formula invocation 0 |
|  | `neverRanksRejectedOptions()` | mixed feasible/rejected spy | Comparator input contains feasible only |
|  | `abortsOnInvalidInsteadOfCallingItInfeasible()` | invalid at middle position | Partial result discarded, later calls 0, final `Invalid` |
|  | `selectedOptionEqualsCacheFreeFullRecomputation()` | Approved Phase 03/04/05 route + solution evaluation contract fixture | Route artifact와 full-solution evaluation/ranking bytes/fingerprints exact; contract 미승인 상태에서는 test/implementation 시작 금지 |
| Core `PairInsertionCorruptionTest` | `rejectsBaseRouteAndEvaluationFingerprintMismatch()` | one-field corruption | Materialization 전에 typed invalid |
|  | `rejectsResultRouteChangedUnderOldOptionFingerprint()` | corrupt serialized option | Apply/rank 금지 |
| Core `PairInsertionReproducibilityTest` | `sameInputsProduceSameAttemptsOptionsBytesAndFingerprints()` | fixed fixture, repeated/parallel | Order, variants, bytes, fingerprints exact equality |
| Solver `StableSolutionValidatorTest` | `acceptsAssignedOrBankXorForEveryRequest()` | assigned/all-bank/mixed fixtures | Complete same-route pair or bank exactly once |
|  | `rejectsPartialDuplicateSplitReverseOverlapAndMissingAsDefect()` | table-driven corruption | 각 corruption stable typed defect; normal infeasible로 변환 0 |
|  | `rejectsSameConcreteVehicleOwnedByTwoRoutes()` | two-route corrupt fixture | Candidate 생성 전 invalid |
|  | `acceptsDeliveryOnlyOwnershipWithoutPhysicalPickup()` | mixed fixture | Logical pickup absence가 defect가 아님 |
| Solver `ConstructionTransitionTest` | `atomicallyReplacesRouteAndRemovesRequestFromBank()` | feasible existing-route option | Next만 변경; pair/route/bank/fresh evaluation exact |
|  | `atomicallyCreatesNewRouteAndConsumesUnusedVehicle()` | `NEW_ROUTE` option | New route one, vehicle duplicate 0, bank request 제거 |
|  | `leavesBaseBytesUnchangedAfterRejectedStaleInvalidOrException()` | fault-injecting authority/option | Base canonical bytes/fingerprint before==after; next 미노출 |
| Solver `SearchSnapshotImmutabilityTest` | `defensivelyCopiesRoutesBankArtifactsAndTraceInputs()` | mutable constructor inputs/accessor attempts | Post-mutation bytes/fingerprint 불변 |
|  | `snapshotsAndPortfolioMembersHaveNoMutableCrossAlias()` | identity graph inspector | 허용 immutable authority 외 mutable identity intersection 0 |
| Portfolio `PortfolioPolicyOrderTest` | `clockUsesExplicitZeroAxisAndClockwiseConvention()` | test-only coordinate convention | Expected angular order exact; convention fingerprint 포함 |
|  | `clockMissingCoordinateReturnsUnavailableWithoutFallback()` | missing depot/entry coordinate | Exact unavailable evidence; farthest/ID fallback call 0 |
|  | `farthestSeedsFromDepotThenUsesDirectedDistanceFromLastService()` | asymmetric prepared `D` | Expected seed/next order; reverse lookup 0 |
|  | `largeDemandUsesExactUtilizationThenDeadlineAndRequestId()` | rational-tie `BigInteger` oracle | Expected order; floating/Feature inference 0 |
|  | `earliestDeadlineUsesDeadlineThenUtilizationAndRequestId()` | normalized deadline fixture | Expected exact order |
|  | `directVehiclesAlwaysPrecedeLeaseAndLargeSmallReverseUtilization()` | ownership/capacity fixture | DIRECT block first; two policies exact inverse where non-tied |
| Portfolio `InitialPortfolioBuilderTest` | `buildsEightMembersInCanonicalPolicyMatrixOrder()` | fully coordinated fixture | 8 available, exact matrix order, unique member IDs |
|  | `keepsSixNonClockMembersWhenBothClockMembersUnavailable()` | missing coordinate fixture | 2 unavailable + 6 available; whole portfolio success |
|  | `keepsNormallyInfeasibleRequestsInBankWithoutFinalDiagnostic()` | no-feasible bound results | Stable candidate; bank membership only, final reason/confidence 없음 |
|  | `recordsPolicyVehicleRequestTargetPositionAndEvaluationLineage()` | trace oracle | Every decision identity and rejection counts reconstructable |
|  | `doesNotRunScreenChooseChampionOrCreateAlnsState()` | forbidden-call spies/architecture | Phase 06 call count/types 0 |
| Portfolio `InitialPortfolioIsolationTest` | `membersDoNotShareMutableRouteBankCacheOrTraceState()` | eight-member fixture | Cross-member mutable aliases 0 |
|  | `equalSolutionsRetainDistinctMemberLineageWithoutChangingSolutionIdentity()` | symmetric fixture | Same solution fingerprint allowed, member IDs distinct |
| Portfolio `PortfolioCorruptionTest` | `rejectsPairSplitBankOverlapCandidateAliasingAndStaleEvaluation()` | `CorruptSeedPortfolioBuilder` | 모든 corruption build `Invalid`; unavailable/low score로 은폐 0 |
| Portfolio `PortfolioReproducibilityTest` | `sameAuthorityAndConfigProduceIdenticalPortfolioAcrossRepeatedAndParallelBuilds()` | 100 sequential + bounded parallel | Member order, trace, solution/portfolio bytes/fingerprint exact |
|  | `inputCollectionOrderDoesNotAffectCandidateOrPortfolioIdentity()` | permuted map/list fixture | Canonical result exact |
| Architecture `Phase05ArchitectureTest` | `coreInsertionDoesNotDependOnSolverCustomerProviderVendorOrVerifier()` | dependency/bytecode rule | Forbidden reference 0 |
|  | `portfolioDoesNotContainAlnsDestroyRepairAcceptanceOrFinalVerification()` | package/type/import rule | Forbidden Phase 06/07 responsibility 0 |
|  | `placeholderOptimizerIsNotASeedPortfolioDependency()` | dependency rule | `com.ronext.optimizer.*` reference 0 |
| External oracle `Phase05OracleSensitivityTest` | `detectsMissingReverseFakePickupWrongDelegationComparatorAndAliasFaults()` | Independent faulty enumerator/materializer/authority/comparator/alias doubles | Seeded defect별 expected assertion red와 minimal counterexample; surviving defect 0 |

“Order sensitivity”는 같은 request set이라도 service 순서를 바꿔 §10.2 capacity 결과와 directed distance가 달라짐을 확인하고, builder가 request set을 정렬해 route sequence 의미를 잃지 않는 별도 assertion으로 각 hand/oracle test에 포함한다.

### 10.4 Red → green 순서

각 production 구현 전에 해당 future test가 정확한 이유로 red인 것을 기록한다.

| 순서 | Red test group | Expected red | Green 조건 |
|---:|---|---|---|
| 1 | Stable partition/identity/immutability | Type/validator 부재 또는 partial/alias 통과 | 모든 stable corruption typed invalid, no-alias |
| 2 | Position exhaustive/bounded contract | Count/order/completeness mismatch | Oracle와 `m=0..6` 전수 일치 |
| 3 | Hand insertion/delegation | Feasibility 복제, invalid 은폐 또는 6-option mismatch | 6/2/4 exact, authority one-call, invalid fail-closed |
| 4 | Functional transition/NEW_ROUTE | Bank/route 부분 갱신 또는 base 오염 | Atomic next snapshot, failure pre-state exact |
| 5 | Policy order/4×2 matrix | Hidden input order/coordinate fallback/floating tie | Exact policy trace, 8 또는 2 unavailable+6 |
| 6 | Corruption/order sensitivity/isolation | Split/alias/stale artifact 통과 | 모든 corruption invalid, candidate independence |
| 7 | Reproducibility/architecture/handoff | Parallel/order 차이 또는 Phase 06/07 dependency | Canonical equality와 forbidden reference 0 |

Red test를 disabled/skip하거나 expected 값을 production enumerator/kernel/cache로 만들어 green을 만들면 evidence로 인정하지 않는다. Current checkout에서 target module/type이 없어 future command가 실패하는 것은 expected `NOT_STARTED` 상태이지 red evidence bundle이 아니다.

### 10.5 Test layer와 pass 기준

| Layer | 적용 범위 | Pass 기준 |
|---|---|---|
| Unit | Position range/count, value constructors, stable key | Positive/negative/boundary/overflow exact |
| Contract | Phase 03/04 bound authority delegation, identity closure | One authority path, duplicate/default/fallback 0 |
| Oracle | Small real/delivery-only insertion, policy rational order | Literal/exhaustive/BigInteger expected와 exact |
| Property | Pair/bank XOR, count formula, permutation/order sensitivity | Bounded exhaustive domain counterexample 0 |
| Fault/corruption | Invalid mid-enumeration, stale option, alias, exception | No partial publish, pre-state/fingerprint 보존 |
| Architecture | Core/solver/package/Phase boundary | Forbidden imports/dependencies/types 0 |
| Reproducibility | Repeat, parallel, input permutation | Canonical bytes/fingerprints/trace exact |

## 11. Ordered implementation work packages

모든 command는 Phase 00 reactor와 해당 future tests가 존재한 뒤 실행한다. 각 WP는 선행 WP green/evidence를 prerequisite로 한다.

### WP-05.0 — Entry, authority와 contract freeze

- **Prerequisites:** Phase 00~04 accepted handoff manifest, scheduler task/owners, 이 문서 review 시작.
- **Targets:** §4 equality checklist, full-solution evaluator/comparator ownership, final internal package/visibility, traversal/coordinate/utilization/enumeration config status.
- **Concrete work:** Source commit과 exact cited section/status/requirement impact 재대조; actual Phase 03과 Phase 04 artifact fields 확인; stable request/vehicle/position total order 승인; public/open/test-only 항목 분리; red-test manifest 생성. Reciprocal whole-file source hash map은 만들지 않는다.
- **Verification command/tests:**

  ```bash
  git rev-parse HEAD

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/core,rpdptw/solver -am \
    compile
  ```

- **Expected:** Source commit/section/status/handoff exact; missing/latest/default 없음; internal contract compile; proposed/open 목록 review record.
- **Failure/rollback:** Drift/missing predecessor면 code/test 착수 금지. 이 문서와 independent fixture specification만 last safe point로 유지한다.
- **Handoff:** Approved Phase 05 contract, authority refs와 red-test list를 WP-05.1에 전달.

### WP-05.1 — Stable solution, bank, identity와 no-alias

- **Prerequisites:** WP-05.0 accepted internal contract.
- **Targets:** `SearchRequestBank`, `EvaluatedRoute`, `SearchSnapshot`, stable validator, canonical solution identity.
- **Concrete work:** Complete pair/bank XOR, vehicle uniqueness, terminal/service-pattern validation; defensive copy; canonical order/fingerprint; corruption builders.
- **Verification command/tests:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/solver \
    -Dtest=StableSolutionValidatorTest,SearchSnapshotImmutabilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test
  ```

- **Expected:** Valid assigned/all-bank/mixed states pass; partial/duplicate/split/reverse/overlap/missing/vehicle-duplicate fail; mutable alias 0.
- **Failure/rollback:** Factory가 invalid snapshot을 노출하면 production state type을 publish하지 않는다. Last safe point는 immutable Phase 02/03/04 inputs다.
- **Handoff:** Validated empty/all-bank snapshot factory와 stable solution identity를 WP-05.2/05.3에 전달.

### WP-05.2 — Pair position enumeration과 pure bound evaluation

- **Prerequisites:** WP-05.1 stable identity, accepted route-level `BoundInsertionAuthority`와 full-solution evaluator/comparator contract.
- **Targets:** `PairPositionKey`, exhaustive/bounded declaration, materializer, evaluator/result/receipt.
- **Concrete work:** Real/delivery-only position enumeration; terminal-preserving materialization; exact authority delegation; `Feasible/Rejected/Invalid` triage; no partial result; stable option ranking.
- **Verification command/tests:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/core \
    -Dtest=PairPositionEnumeratorTest,PairInsertionEvaluatorTest,PairInsertionDelegationTest,PairInsertionCorruptionTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/test-fixtures \
    -Dtest=PairInsertionHandOracleTest,Phase05OracleSensitivityTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test
  ```

- **Expected:** Count/formula/oracle exact; compatibility/capacity/time/resource reimplementation 0; rejected rank 0; invalid fail-closed; base route unchanged.
- **Failure/rollback:** Count, delegation, identity 또는 full-recompute equality 하나라도 실패하면 evaluator artifact를 폐기하고 WP-05.1 state만 유지한다.
- **Handoff:** Pure feasible option list, enumeration receipt와 `E-P05-INSERTION` 후보 report를 WP-05.3/05.4에 전달.

### WP-05.3 — Functional atomic construction transition

- **Prerequisites:** WP-05.1 stable snapshot, WP-05.2 immutable feasible option, approved cache-free full-solution evaluator.
- **Targets:** `ConstructionTransition`, existing-route replacement, concrete unused `NEW_ROUTE`, fresh candidate evaluation.
- **Concrete work:** Request bank-only/stale checks; route replace/create + bank removal one-shot; new snapshot freeze; failure/exception no-state test; no destroy/remove/undo API.
- **Verification command/tests:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/solver \
    -Dtest=ConstructionTransitionTest,StableSolutionValidatorTest,SearchSnapshotImmutabilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test
  ```

- **Expected:** Successful transition만 next snapshot; pair same-route/precedence, vehicle uniqueness, fresh evaluation exact; 모든 실패에서 base bytes/fingerprint exact.
- **Failure/rollback:** Partial update/alias/stale apply가 관찰되면 transition implementation 전체를 폐기한다. Apply/undo로 우회하지 않는다.
- **Handoff:** Phase 05 construction-only immutable transition과 `E-P05-PAIR` 후보 report를 WP-05.4에 전달.

### WP-05.4 — Growth/vehicle policy와 최대 8개 portfolio

- **Prerequisites:** WP-05.2 evaluator와 WP-05.3 transition green; approved traversal, CLOCK convention, utilization policy.
- **Targets:** Policy implementations, exact matrix config, trace, candidate, portfolio builder.
- **Concrete work:** 4 growth/2 vehicle order; prepared directed distance; exact utilization comparison; CLOCK preflight; per-member isolated state; cache-free final evaluation; canonical matrix/member identity.
- **Verification command/tests:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/solver \
    -Dtest=PortfolioPolicyOrderTest,InitialPortfolioBuilderTest,InitialPortfolioIsolationTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test
  ```

- **Expected:** Full fixture 8 available; CLOCK missing fixture 2 unavailable+6 available; trace complete; no mutable cross-alias; screen/champion/ALNS calls 0.
- **Failure/rollback:** 한 member가 다른 member를 오염시키거나 hidden fallback/order가 있으면 portfolio 전체를 invalid 처리한다. Green한 evaluator/transition은 독립 artifact로 보존 가능하다.
- **Handoff:** Immutable available seed candidates와 matrix trace를 WP-05.5에 전달.

### WP-05.5 — Oracle, property, corruption과 reproducibility

- **Prerequisites:** WP-05.1~05.4 functional green.
- **Targets:** Independent oracle suite, order sensitivity, exhaustive property, corruption/fault, repeated/parallel deterministic build.
- **Concrete work:** `m=0..6` exhaustive permutation; BigInteger prefix/utilization; pair split/candidate alias/stale artifact corruption; input permutation; fixed bounded parallel repeat; counterexample digest.
- **Verification command/tests:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/core \
    -Dtest=PairInsertionReproducibilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/solver \
    -Dtest=PortfolioCorruptionTest,PortfolioReproducibilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/test-fixtures \
    -Dtest=PairInsertionHandOracleTest,Phase05OracleSensitivityTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test
  ```

- **Expected:** Oracle mismatch/counterexample 0; corruption 전부 typed invalid; repeated/parallel/input-permuted canonical bytes/fingerprint exact.
- **Failure/rollback:** Reproducibility나 corruption이 실패하면 portfolio를 Phase 06 handoff하지 않는다. Last safe point는 WP-05.4 이전 개별 green artifact와 failure seed다.
- **Handoff:** `E-P05-PAIR`, `E-P05-INSERTION`, `E-P05-PORTFOLIO` 후보 evidence를 WP-05.6에 전달.

### WP-05.6 — Architecture, evidence, independent review와 Phase 06 handoff

- **Prerequisites:** 모든 functional/test WP green, evidence candidate complete.
- **Targets:** Root architecture verify, immutable evidence bundle, independent review, seed contract compatibility.
- **Concrete work:** Dependency/bytecode 검사; placeholder/provider/customer/vendor/Phase 06·07 leakage 검사; exact command/toolchain/result 수집; review verdict; Phase 06 consumer contract compile test.
- **Verification command/tests:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never clean verify

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/architecture-rules \
    -Dtest=Phase05ArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true \
    test
  ```

- **Expected:** Default OR-Tools-free ALNS-only build pass; forbidden dependency/type 0; test pass/fail/skip exact report; handoff manifest와 rollback point digest.
- **Failure/rollback:** Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED` 또는 Phase 06 handoff authority를 주장하지 않는다.
- **Handoff:** §15.2의 `Phase05SeedPortfolioHandoff`와 accepted evidence identities를 actual but unaccepted Phase 06에 전달.

## 12. Verification command, evidence와 판정

### 12.1 Future exact command set

```bash
# Fresh full-reactor gate
./mvnw -B -ntp -Dstyle.color=never clean verify

# Core insertion
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core \
  -Dtest=PairPositionEnumeratorTest,PairInsertionEvaluatorTest,PairInsertionDelegationTest,PairInsertionCorruptionTest,PairInsertionReproducibilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  test

# Solver stable state and portfolio
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/solver \
  -Dtest=StableSolutionValidatorTest,ConstructionTransitionTest,SearchSnapshotImmutabilityTest,PortfolioPolicyOrderTest,InitialPortfolioBuilderTest,InitialPortfolioIsolationTest,PortfolioCorruptionTest,PortfolioReproducibilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  test

# Independent hand oracle and seeded-defect sensitivity
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/test-fixtures \
  -Dtest=PairInsertionHandOracleTest,Phase05OracleSensitivityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  test

# Architecture
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules \
  -Dtest=Phase05ArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  test
```

Selected-test command는 upstream `-am` module에서 같은 test 이름이 없어서 생기는 false failure/skip을 피하기 위해 target module만 실행한다. Fresh full-reactor `clean verify`를 먼저 통과해 dependency artifact와 전체 required test를 검증한다. 각 selected command는 fresh Surefire XML에서 expected class/method가 실제 실행됐고 non-zero이며 failed/error/skipped가 0인지 fail-closed로 대조한다.

Command, module path, profile 또는 test class가 Phase 00 accepted build에서 달라지면 review record와 이 문서를 함께 갱신한다. 존재하지 않는 test를 skip하거나 stale report를 재사용해 성공한 command는 evidence가 아니다.

### 12.2 Planned evidence

| Evidence key | 최소 내용 | 현재 상태 |
|---|---|---|
| `E-P05-PAIR` | Source commit/cited-section status, build/authority artifact fingerprints, pair/bank/vehicle/terminal property matrix, functional transition pre/post bytes, failure/exception unchanged proof, no-alias와 safe telemetry report | NOT_PRODUCED |
| `E-P05-INSERTION` | Position formula/receipt, §10.2 literal table, exhaustive/permutation/BigInteger oracle, seeded-defect sensitivity, Phase 03/04 delegation call trace, feasible/rejected/invalid/full-solution-recompute equality | NOT_PRODUCED |
| `E-P05-PORTFOLIO` | Exact 4×2 config/version, 8-member and CLOCK unavailable traces, policy/vehicle/request/position tie cases, derived available projection equality, candidate isolation, repeated/parallel canonical identity와 redaction report | NOT_PRODUCED |

각 bundle은 plan §9의 공통 규칙에 따라 source commit, detailed/review version, exact command/exit code, toolchain, test counts, failure seeds, known limitation, OPEN/GATED/deferred, reviewer/verdict/timestamp, handoff/rollback identity를 포함한다.

Working tree `target/`, console 한 줄, test 존재, 문서 작성 또는 placeholder candidate를 evidence로 인용하지 않는다.

### 12.3 Exit gate

다음 AND gate가 모두 참이어야 independent reviewer가 Phase 05 acceptance를 검토할 수 있다.

- Phase 00~04 predecessor와 exact fingerprints가 accepted다.
- Pair/bank XOR, same vehicle, exactly once, precedence, vehicle uniqueness가 property/corruption test를 통과한다.
- Real/delivery-only exhaustive position oracle와 bounded completeness honesty가 통과한다.
- Phase 03/04 route delegation, 승인된 full-solution evaluator/comparator contract와 cache-free result equality가 통과하며 duplicated feasibility/objective logic이 없다.
- Existing/`NEW_ROUTE` transition의 failure/exception pre-state가 exact 보존된다.
- 4×2 full matrix 또는 documented CLOCK unavailable matrix가 deterministic하게 생성된다.
- Candidate route/bank/artifact/trace no-alias와 candidate independence가 증명된다.
- Order sensitivity, seeded-defect oracle sensitivity, corruption, repeat/parallel/input-permutation reproducibility가 통과한다.
- Phase 06/07 책임, customer/provider/vendor/placeholder dependency가 0이다.
- Safe aggregate telemetry만 노출되고 raw address/input/coordinate/secret/provider locator와 elapsed/completion order가 의미 identity·tie·quality에 들어가지 않는다.
- 세 evidence bundle이 immutable digest와 independent review를 가진다.

### 12.4 Security, observability, reproducibility와 failure evidence

Phase 05 core/solver는 logger, tracer, environment, provider locator, wall clock 또는 thread 정보를 의미 입력으로 읽지 않는다. Typed result와 evidence adapter가 소비할 수 있는 safe aggregate는 다음으로 제한한다.

```text
problem/travel/profile/config/build fingerprints
portfolio member policy key/version
enumeration completeness와 legal/evaluated/feasible/rejected counts
transition result category와 stable failure code
candidate/portfolio fingerprint와 no-alias/corruption counters
requested/completed test work와 reproducibility envelope ID
```

Raw address/coordinate/input, external order/customer text, full route/trace payload, secret, credential, signed URL, provider locator, exception payload와 arbitrary descriptor content는 log/trace field에 넣지 않는다. Request/vehicle ID가 tenant-sensitive하면 aggregate evidence에서는 stable count 또는 scoped digest만 사용한다. Elapsed, timestamp, thread/parallel completion order와 object identity는 solution/portfolio fingerprint, tie-break, candidate quality 또는 pass/fail oracle이 아니다.

Failure evidence는 `Rejected`와 `Invalid`의 code/category, non-secret identity digest, exact safe position/target ordinal, authority fingerprints와 last safe snapshot fingerprint를 보존한다. `Invalid`, exception, cancellation 또는 evidence corruption 뒤 partial attempt/member/portfolio를 정상 artifact로 seal하지 않는다. Repeat/parallel count는 explicit `TEST_ONLY_*` config로 기록하며 production default나 official reproducibility 수치로 승격하지 않는다.

## 13. Definition of Done과 anti-pattern

### 13.1 Phase 05 Definition of Done

Phase 05는 다음을 모두 만족할 때만 `ACCEPTED`다.

1. Entry gate/owner/authority가 확인되고 이 detailed document와 review가 승인됨.
2. Proposed/open internal API와 traversal/config decision이 review에서 명시적으로 처리됨.
3. Positive, negative, boundary, property, oracle, fault, corruption, architecture, reproducibility test가 applicable layer에서 통과함.
4. `E-P05-PAIR`, `E-P05-INSERTION`, `E-P05-PORTFOLIO`가 immutable identity/digest를 가짐.
5. Stable snapshot과 portfolio가 immutable/no-alias이고 full evaluation identity와 일치함.
6. Normal infeasibility와 invalid defect를 섞지 않으며 bounded miss의 confidence를 과장하지 않음.
7. 최대 8개 policy lineage와 `CLOCK UNAVAILABLE`가 원문 의미를 보존함.
8. Phase 06이 seed candidates만 소비할 수 있는 handoff와 rollback point가 확인됨.
9. `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip과 public API OPEN 상태를 값/완료로 바꾸지 않음.

### 13.2 금지 anti-pattern

- Pickup과 delivery를 별도 evaluator/apply call로 삽입
- Partial pair를 낮은 score, normal infeasible 또는 bank 상태로 숨김
- Delivery-only logical pickup을 depot/customer visit으로 생성
- Pair를 다른 route/vehicle에 split하거나 delivery-first option을 만들기
- Compatibility, zone/capability, capacity, time/window/resource 공식을 Phase 05에서 복제
- Route artifact delta나 placeholder scalar로 solution objective/`CandidateRankingArtifact`를 합성
- Phase 03/04 `Invalid`를 rejection으로 낮추고 다음 option 계속
- First feasible에서 hidden break하고 exhaustive라고 기록
- `orderedUniquePositions.size() > maxEvaluations`를 runtime truncate하고 bounded receipt를 성공으로 발행
- Bounded miss를 `NO_FEASIBLE_INSERTION` 또는 final diagnostic으로 표현
- `NEW_ROUTE`에 vehicle type/count/synthetic sentinel 사용
- `double` ratio/epsilon, Feature 문자열 숫자, input/hash iteration으로 vehicle 크기/tie 결정
- CLOCK 좌표 누락을 다른 policy나 stable ID order로 fallback
- Candidate/member가 mutable route/bank/cache/trace를 공유
- Member 목록과 별도의 independently supplied available-candidate 목록을 두어 두 authority가 drift
- `toString()`, object identity, timestamp, thread/completion order로 fingerprint/tie 결정
- Search cache/summary를 cache-free authority로 사용
- Placeholder `AlnsBatchEngine`의 objective/map/candidate를 seed contract로 감싸기
- Phase 05에서 COW `TrialDraft`, destroy/repair, acceptance, adaptive, screen/champion을 구현
- Phase 07 verifier/final audit/diagnostic/publication을 호출하거나 흉내 내기
- Test-only position bound, coordinate convention, capacity edge 값을 production default로 승격

## 14. Blocker, OPEN/GATED/deferred와 restart

| 항목 | 상태 | Owner | 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00~04 accepted evidence 부재 | BLOCKER | Architecture + Phase 01~04 owners | Phase 05 code/test/evidence | 이 detailed document와 independent oracle 설계 | 모든 predecessor review/bundle/digest와 actual artifact 전달 |
| Phase 04 accepted contract/evidence 부재 | BLOCKER | Capability/Profile + Core/Evaluation | Bound feasibility/comparator 소비 contract | Actual Phase 04 §7의 immutable `BoundProfile` consumer contract + 이 문서 §6~§7 view | Phase 04 accepted binding/closure/evidence와 Phase 03~05 compatibility review |
| Full-solution evaluator/ranking contract 부재 | CROSS-PHASE BLOCKER | Core/Evaluation + Capability/Profile + Phase 05 State/Portfolio | `CandidateEvaluationArtifact`, `CandidateRankingArtifact`, transition fresh evaluation, portfolio comparison과 evidence | Phase 03/04 route-level kernel/declarations와 immutable route/bank source of truth; ad hoc aggregation 금지 | Problem/travel/profile + ordered routes + bank API, route artifact reuse/invalidation, aggregation/failure/fingerprint/comparator contract와 reciprocal compile/full-equality/corruption test 승인 |
| Business comparator equality와 insertion tie API 미승인 | CROSS-PHASE BLOCKER | Core/Evaluation + Phase 05/06 Algorithm | Stable option total-order type/encoding과 deterministic candidate choice freeze | Business objective first, stable semantic key field set, non-tied reversal 금지 | Business equality API, solution tie 대 insertion tie owner, explicit target-key order/version과 comparator law/exhaustive test 승인 |
| Actual Phase 06 proposed API/name acceptance 부재 | BLOCKER FOR CONTRACT FREEZE | Phase 05/06 + Architecture reviewers | `SeedPortfolio`, `SearchSnapshot`, `PairInsertionEvaluator` exact signature/visibility/version | 두 actual 문서가 immutable seed + pure evaluator, Phase 06-owned removal/COW에 의미상 정합 | Phase 05/06 cross-review에서 compatible signatures와 evidence handoff 승인 |
| Scheduler/owner 미지정 | BLOCKER | 총괄 scheduler | Authoritative status/review assignment | `scheduler_task_id: TBD_NOT_SUPPLIED` | Exact task ID, implementer와 independent reviewer 지정 |
| Final internal API/type/package | PROPOSED/OPEN | Pair/Insertion + Architecture | Compatibility freeze | §5.2/§7 semantic contract | Phase 05 review와 predecessor signature alignment |
| Portfolio traversal exact algorithm | PROPOSED_REVIEW_REQUIRED | Algorithm | Production deterministic builder | §9.2 proposed traversal, no hidden order | Policy-role preservation review와 golden trace 승인 |
| CLOCK coordinate convention/version | OPEN/PROPOSED | Domain/Input + Algorithm | CLOCK member availability/ordering | Missing coordinate면 `UNAVAILABLE`; non-CLOCK 진행 | Explicit zero-axis/orientation/function/version과 reference-vector approval |
| Utilization missing/zero/tie policy | OPEN/PROPOSED | Phase 04 Policy + Algorithm | LARGE/SMALL vehicle/request order | Exact positive-capacity test-only fixture만 | Bound typed edge policy/version과 rational comparison review |
| Identity encoding/hash | PROPOSED/OPEN | Architecture/Serialization | External compatibility와 evidence encoding | Semantic field set, internal package-private identity | Canonical encoding/version/migration review |
| `Q-BENCH-02` official steps/workers/rounds/watchdog | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 06 official run/Phase 14; Phase 05 deterministic construction은 안 막음 | Phase 05는 step/worker 숫자를 갖지 않음 | Calibration protocol, measured review, explicit approval |
| `Q-ALG-02` | RESOLVED — KEEP_COW | Algorithm·Performance | Phase 06 state strategy; Phase 05 functional construction은 영향 없음 | Immutable seed snapshot | COW는 Phase 06에서 구현; apply/undo는 별도 evidence/승인 전 금지 |
| `C-17` route pool/MIP | GATED TARGET | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13/production default | Phase 05 seed portfolio only | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope + OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback 승인 |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | MDVRP/OVRP/SDVRP 질문/구현 | Fixed-terminal single-trip pair | Representative fixture, impact study와 별도 승인 |
| Multi-trip/rotation | DEFERRED FEATURE | Product·Domain·Algorithm | Trip crossing/reset/depot revisit | Oneway + single roundtrip, pair non-crossing | Trip/resource/reset/window/pair 계약과 승인 |
| Proposed public API/schema/numeric default | OPEN | Product/API/Data | External compatibility 약속 | Internal immutable contract/test-only values | Versioned contract, compatibility/security review와 approval |

`Q-INFRA-01`은 AWS target으로 `RESOLVED`지만 Phase 05 core/solver에 AWS SDK 또는 topology 의미를 추가하는 근거가 아니다. Current Win fixture의 decimal `D/U` official blocker도 generic integer Phase 05 hand fixture를 막지 않는다.

## 15. Previous/next handoff

### 15.1 Previous — actual but unaccepted Phase 03/04

[Actual but unaccepted Phase 03](phase-03-route-propagation-evaluation-kernel.md)에서 다음을 받아야 한다.

- Immutable `RoutePlan`
- Pure `RouteEvaluationKernel`
- `RouteEvaluationResult.Feasible/Infeasible/Invalid`
- `RouteEvaluationArtifact`, `ConstraintRejection`, `EvaluationFailure`
- `ObjectiveComparator`, `StableTiePolicy`와 exact evaluation fingerprints
- Same route option의 cache-free full recomputation equality contract

[Actual but unaccepted Phase 04](phase-04-capabilities-customer-profiles.md)에서 다음을 받아야 한다.

- Exact customer/profile/version/preset과 immutable `BoundProfile`/`SolvePlan`
- Bound compatibility/hard/metric/score/objective closure와 comparator
- Phase 05가 소비할 customer-neutral insertion evaluation view
- Ordered routes + bank의 full-solution evaluation/aggregation/comparator contract 또는 그 contract의 공동 owner 승인
- Vehicle utilization edge policy와 portfolio에 필요한 approved config/declarations
- Unknown/latest/cross-customer/missing/duplicate/unit mismatch rejection
- `E-P04-BINDING`, `E-P04-ISOLATION`, applicable `E-P04-FACET`와 accepted review

Phase 04 §7.4/§8.4를 직접 대조하면 accepted handoff의 끝은 immutable `BoundProfile`/snapshot과 existing Phase 03 declaration이다. Phase 04는 insertion option, bank, portfolio policy 또는 seed를 만들지 않으며 현재 문서 세트에는 solution-level evaluator owner/API도 없다. 현재 문서와 review는 actual이고 review verdict는 `CHANGES_REQUIRED`지만 implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `NOT_ACCEPTED`이므로 implementation handoff는 아직 성립하지 않는다. Phase 05는 그 사이 placeholder evaluator, route-only objective delta, permissive profile, default comparator 또는 customer branch를 만들지 않는다.

### 15.2 Next — actual but unaccepted Phase 06

[Actual Phase 06 — COW ALNS/reproducibility](phase-06-cow-alns-reproducibility.md)는 document/review `CHANGES_REQUIRED`/`COMPLETE`, implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, acceptance `NOT_ACCEPTED`이며 accepted consumer가 아니다. 그 §7.1/§7.6/§8.3/§16.1과 이 문서의 정합한 handoff 교집합은 immutable seed solution/portfolio, stable order, pure pair insertion evaluator와 Phase 05 evidence다.

```text
Phase05SeedPortfolioHandoff
  source commit + Phase 05 detailed/review/evidence digests
  Problem/PreparedTravel/BoundProfile/SolvePlan fingerprints
  portfolio config/enumeration/tie contract fingerprints
  ordered available ConstructionCandidate refs
  each immutable SearchSnapshot/SolutionFingerprint
  each member policy/trace/fingerprint
  unavailable member records
  cache-free validation report refs
  E-P05-PAIR / E-P05-INSERTION / E-P05-PORTFOLIO refs
  rollback point
```

Phase 06 handoff acceptance:

- Candidate bytes/digest/fingerprint와 manifest가 exact 일치한다.
- 모든 candidate가 stable pair/bank/vehicle/terminal invariant를 만족한다.
- 모든 candidate solution evaluation이 승인된 full-solution evaluator의 cache-free result와 exact 일치한다.
- Candidate 사이 mutable alias가 없다.
- Phase 06은 candidate를 직접 mutate하지 않고 자기 Phase의 changed-route COW state로 가져간다.
- Screen config, seed namespace, operator/acceptance/adaptive/step budget은 Phase 06 manifest가 별도로 제공한다.

Actual Phase 06의 최신 직접 대조 내용은 이 경계와 정합한다.

- `Phase05SeedPortfolioHandoff`, `SeedPortfolio`, `SearchSnapshot`, pure `PairInsertionEvaluator`를 upstream으로 요구한다.
- Phase 05의 `ConstructionTransition`을 destroy editor로 cast하지 않는다.
- Central removal editor, COW apply, `CompletedTrial`, repair orchestration, screen/RNG/operator/acceptance/adaptive/step state는 Phase 06 소유다.
- 두 문서의 type 이름과 visibility는 여전히 proposed이므로 accepted cross-phase compile/evidence review 전 compatibility를 주장하지 않는다.

Phase 05가 넘기지 않는 것:

```text
phase-1 champion
screenMaxSteps or any official number
destroy/repair proposal
TrialDraft/current/stageBest/solveBest
acceptance/adaptive/RNG state
termination or completed-step record
candidate verifier PASS
final outcome/diagnostic/publication status
```

현재 Phase 06 link target은 actual이지만 predecessor acceptance, cross-phase signature review와 evidence가 없으므로 handoff status는 계속 `NOT_ACCEPTED`다.

### 15.3 Rollback point

Phase 06 handoff 전 실패하면 last accepted predecessor인 Phase 04 bound artifacts와 Phase 03 kernel을 보존하고 incomplete Phase 05 portfolio를 폐기한다. 일부 member, 가장 좋은 route 또는 console output을 새 seed portfolio로 합성하지 않는다. Accepted Phase 05 이후 회귀가 발견되면 이전 accepted `Phase05SeedPortfolioHandoff` digest로 돌아가며 같은 identity의 bytes를 overwrite하지 않는다.

## 16. Source → requirement → test → evidence traceability

| Requirement | Source | Phase 05 contract | Exact future test | Planned evidence |
|---|---|---|---|---|
| `REQ-PAIR` same-vehicle/exactly-once/precedence/route-bank XOR | [Master §6](../../2026-07-31-phase-b-master-design.md#6-핵심-불변조건과-atomic-mutation), `C-06`, Domain §2.4/§8 | Stable validator, atomic complete option, bank-only request | `StableSolutionValidatorTest.*`, `ConstructionTransitionTest.*` | `E-P05-PAIR` |
| `REQ-SERVICE-PATTERN` delivery-only + real pair | `Q-REQ-01`, [Domain §2.3](../../2026-07-26-domain-design.md#23-delivery-only와-real-pickup-delivery) | Logical initial-load ownership vs physical pickup position | `createsDeliveryOnlyInitialLoadOwnershipWithoutFakeVisit()`, delivery-only enumerator methods | `E-P05-PAIR`, `E-P05-INSERTION` |
| `REQ-INSERTION-POSITION` legal complete pair positions | [Master §11.1](../../2026-07-31-phase-b-master-design.md#111-공통-pair-evaluator), Domain §11 | Service-level `p<d`, exhaustive/explicit bounded receipt | `PairPositionEnumeratorTest.*`, hand six-option oracle | `E-P05-INSERTION` |
| `REQ-DELEGATED-FEASIBILITY` compatibility/resource/time/capacity authority | [Master §4.3/§11.1](../../2026-07-31-phase-b-master-design.md#43-논리-컴포넌트와-책임), Phase 03 §14.3 | Single `BoundInsertionAuthority`, no formula duplication | `delegatesCompatibilityCapacityTimeAndResourceWithoutReimplementation()` | `E-P05-INSERTION` |
| `REQ-FULL-SOLUTION-EVAL` route artifacts + ordered routes + bank의 objective authority | [Master §4.2/§9/§15.4~§15.5](../../2026-07-31-phase-b-master-design.md#42-단계별-데이터-계약), Phase 03 review F-P03-004 | Cross-Phase blocker; route delta/ad hoc aggregate 금지, approved cache-free solution evaluator 필요 | Contract 승인 뒤 reciprocal compile/full-equality/corruption test | `E-P05-INSERTION`, `E-P05-PORTFOLIO` |
| `REQ-FAILURE-TRIAGE` infeasible vs invalid | Phase 03 §7/§13, [Master §6.3](../../2026-07-31-phase-b-master-design.md#63-atomic-mutation) | Rejected rank exclusion, invalid fail-closed, unchanged base | Delegation invalid/rejected methods, transition failure method | `E-P05-PAIR`, `E-P05-INSERTION` |
| `REQ-NEW-ROUTE` unused concrete vehicle | [Master §11.6](../../2026-07-31-phase-b-master-design.md#116-promising-repair와-exact-insertion), Integrated §9.8 | `NewRoute(VehicleId)`, terminal authority, vehicle uniqueness | `evaluatesNewRouteAgainstUnusedConcreteVehicleAndTerminal()`, `atomicallyCreatesNewRouteAndConsumesUnusedVehicle()` | `E-P05-PAIR`, `E-P05-INSERTION` |
| `REQ-PORTFOLIO` 4×2 initial candidates | `C-16`, `Q-ALG-01`, [Master §11.2](../../2026-07-31-phase-b-master-design.md#112-현재-범위의-initial-solution-portfolio) | Canonical matrix, independent construction, max 8 | `buildsEightMembersInCanonicalPolicyMatrixOrder()` | `E-P05-PORTFOLIO` |
| `REQ-CLOCK` clockwise/missing coordinate | [Master §11.2](../../2026-07-31-phase-b-master-design.md#112-현재-범위의-initial-solution-portfolio) | Explicit convention/version; missing → unavailable, no fallback | CLOCK policy methods, six-non-clock member method | `E-P05-PORTFOLIO` |
| `REQ-SEQUENTIAL-POLICY` farthest/large/deadline and directed closeness | Master §11.2, `Q-ALG-01` | Prepared `D`, entry location, exact rational utilization/deadline ties | `PortfolioPolicyOrderTest.*` | `E-P05-PORTFOLIO` |
| `REQ-DETERMINISTIC-TIE` comparator then stable identities | Master §11.1~§11.2, Phase 03 comparator contract | Objective-first total order; no clock/random/hash | `ordersFeasibleOptionsByBusinessComparatorThenStableTie()`, policy tie tests | `E-P05-INSERTION`, `E-P05-PORTFOLIO` |
| `REQ-IMMUTABLE-IDENTITY` candidate/solution no-alias | Master §4.5~§4.6/§12, Domain §8 | Defensive immutable snapshot/member/portfolio fingerprints | `SearchSnapshotImmutabilityTest.*`, `InitialPortfolioIsolationTest.*` | `E-P05-PAIR`, `E-P05-PORTFOLIO` |
| `REQ-ORACLE-CORRUPTION` independent small oracle and corruption | Integrated §22.2~§22.4, Plan §8 | Literal/permutation/BigInteger oracle, one-field corruption | Hand oracle, `PortfolioCorruptionTest.*` | All Phase 05 evidence |
| `REQ-REPRO` stable order/identity | [Master §13](../../2026-07-31-phase-b-master-design.md#13-termination-reproducibility와-execution-provenance) | Repeat/parallel/input-permutation exact portfolio | Both reproducibility classes | `E-P05-PORTFOLIO` |
| `REQ-SECURITY-OBS` safe aggregate/redaction/elapsed 비의미성 | [Integrated §19~§21](../../architecture-domain-implementation-design.md#19-configuration-provenance와-observability), Plan §13 | §12.4 safe fields, raw input/PII/secret/provider locator 금지 | Redaction, safe failure, reproducibility identity tests | All Phase 05 evidence |
| `REQ-ARCH-DAG` core/solver/Phase responsibility | [Architecture §2](../../2026-07-26-architecture-design.md#2-module과-package-경계), Integrated §23~§24 | `evaluation.insertion` core, state/portfolio solver, Phase 06/07 exclusion | `Phase05ArchitectureTest.*` | All Phase 05 evidence + architecture report |
| `REQ-HANDOFF-P06` seed only, no ALNS pull | Plan Phase 05~06, user-locked scope | `Phase05SeedPortfolioHandoff`, explicit excluded fields | `doesNotRunScreenChooseChampionOrCreateAlnsState()`, architecture test | Phase 05 review/handoff record |

새 requirement, hard rule, ranking dimension, numeric default, public schema 또는 traversal 의미를 발견하면 source/owner/test/evidence를 연결하고 영향 authority/ADR/review를 같은 변경 단위에서 갱신한다. Phase 05 구현 편의를 위해 미확정 값을 hidden default로 채우지 않는다.
