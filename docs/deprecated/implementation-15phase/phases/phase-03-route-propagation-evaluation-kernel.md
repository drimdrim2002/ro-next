# Phase 03 — 경로 전파 계산과 평가

> 부제: Route propagation/evaluation kernel 상세 계획

```yaml
document_status: REVIEWED_CHANGES_REQUIRED
document_version: 1.2
document_review_status: COMPLETE
review_verdict: CHANGES_REQUIRED
review_document: docs/implementation/reviews/phase-03-review.md
phase: "03"
phase_name: route-propagation-evaluation-kernel
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
phase_acceptance_status: NOT_ACCEPTED
evidence_status: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_status: ALNS_FIRST_GATE_OVERLAY_APPLIED_DOCUMENTATION_ONLY
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Core/Evaluation owner role
  upstream_authority: Phase 02 Domain/Travel owner role
  downstream_binding: Phase 04 Capability/Profile owner role
  downstream_search: Phase 05 Pair/Insertion owner role
  review: independent Phase 03 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture
  - Phase 01 accepted normalized numeric/time/service semantics
  - Phase 02 accepted immutable ProblemInstance and complete PreparedTravel
planned_evidence:
  - E-P03-PROPAGATION
  - E-P03-EVALUATION
  - E-P03-COMPARATOR
source_fingerprints_sha256:
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/deprecated/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/deprecated/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/deprecated/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
  docs/deprecated/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d
  docs/implementation/README.md: 6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
  sha256: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
```

## 1. 문서 지위와 권위

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 권위 원문의 `REVIEW` metadata는 provenance로 보존하지만 이 상세 문서 작성을 중단하는 조건이 아니다. 반대로 문서 작성, 제안 API 또는 미래 test 이름만으로 Phase 구현·evidence·review가 끝났다고 주장하지 않는다. 이 문서에서 존재하지 않는 module, type, test, report와 command 결과는 모두 `proposed` 또는 `future`다.

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../2026-07-31-phase-b-master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 propagation/evaluation 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/package/DAG
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 15 Phase 배치
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 권위 입력으로 사용하거나 복사하지 않았고 이 작업에서 수정하지 않는다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 03에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | §4.3~§4.6, §6, §7.2~§7.3, §8~§9, §12, §15.4, §16~§17 | Propagator 책임, pair/route 불변조건, checked arithmetic, time/travel, hard/metric/score/objective 분리, cache 비권위, `RM-2` gate |
| [Final Domain](../../2026-07-26-domain-design.md) | §5.1~§5.2, §6~§7, §9~§10, §17.5~§17.6, §18 | Exact 단위, full-arc restart, load/stop/resource 공식, profile lifecycle, acceptance 경계 |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2.1~§2.7, §5.6, §6 | `rpdptw-core` package owner, module DAG, verifier/search/cache 격리, OR-Tools-free ALNS-only build |
| [Integrated design](../../architecture-domain-implementation-design.md) | §3, §6, §7, §8, §22~§25 | Target tree, Phase 02 artifact, Phase 03 순서/SPI/gate, Phase 04 handoff, corruption test와 anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-NUM-01~03`, `Q-TIME-01~04`, `Q-IN-01~02`, `Q-REQ-01~02`, `Q-OBJ-01~03`, `Q-BENCH-02`, `Q-VAR-01` | 확정 수치/경계, official 미확정 수치, deferred 범위 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§4, Phase 02~04, §8~§15 | Current inventory, phase entry/exit, evidence/DoD, blocker와 traceability |
| [구현 문서 지도](../README.md) | §3~§7 | Authority, canonical filename, planned link와 review/status 규칙 |

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 03은 불변 `RoutePlan` 하나를 앞에서 뒤로 전파하여 물리 사실과 hard feasibility를 **pure, deterministic, cache-free**하게 재계산하고, 그 결과를 neutral metric → composed hard constraint → score → objective vector → comparator로 한 방향으로 평가하는 내부 kernel을 설계·구현·검증한다.

동일한 다음 입력의 canonical bytes와 fingerprint가 같으면 실행 횟수, thread, cache hit/miss, collection iteration order와 무관하게 같은 typed result와 evaluation fingerprint가 나와야 한다.

```text
Phase 02 ProblemInstance identity
+ Phase 02 PreparedTravel identity
+ RoutePlan identity
+ exact Phase 03 evaluation declaration identity
```

### 2.2 포함 범위

- Terminal/trip/visit 구조, same-route complete pair, exactly-once와 pickup-before-delivery의 route-local validation
- Delivery-only initial load와 real pickup/delivery load delta, 무게·부피 모든 prefix capacity와 final zero-load
- Complete directed prepared travel을 사용한 full-arc 이동
- Arrival, customer/depot waiting, service start/end/departure와 반복/overnight window
- `START_ONLY`와 `COMPLETE_WITHIN_WINDOW`의 명시적 선택, inclusive close, due-date completion
- Work-window 안에 전체 arc가 들어가는지 검사하고 다음 work window에서 arc 전체를 다시 시작하는 의미
- Stop, distance, drive time, customer/depot waiting, service, inter-work-window rest와 route operational time
- Vehicle/global inclusive resource limit
- Checked `long` arithmetic와 overflow/corruption fail-closed
- Hard feasibility, neutral metric, score, objective, deterministic comparator의 분리
- Phase 04가 bind하고 Phase 05/07이 재사용할 customer-neutral 내부 SPI와 immutable artifact
- Hand oracle, independent reference oracle, boundary/metamorphic/property/corruption/reproducibility test 명세

### 2.3 명시적 비범위

- Phase 04의 customer/profile descriptor, registry, authorization, exact default preset와 capability 구현
- Phase 05의 pair insertion option 열거, route/bank mutation, initial portfolio
- Phase 06의 ALNS, COW state, acceptance, cache invalidation 정책과 실행 budget
- Phase 07의 candidate/result verifier verdict, final outcome/diagnostic/audit/publication
- Route pool, MIP projection/backend, hybrid adoption과 raw optimizer incumbent
- Multi-trip/rotation, MDVRP/OVRP/SDVRP, 동적 routing
- Raw input parsing, fixed-point normalization, travel generation 또는 rounding 재수행
- AWS/GCP/storage/workflow/HTTP DTO, SDK와 deployment
- Public/wire API, serialization format 또는 production numeric default 승인

`ConstraintRejection`은 현재 route가 어느 hard rule에서 거절됐는지를 나타내는 내부 machine fact다. Phase 07의 최종 `UNASSIGNED` reason, confidence, 운영 diagnostic 또는 publication 판정이 아니다.

§7의 현재 proposed API는 **route-level kernel**만 구체화한다. Canonical Master `RM-2`가 요구하는 solution-level hard/metric/objective aggregation과 Phase 05가 명명한 `CandidateEvaluationArtifact`의 owner, exact inputs, invalidation/fingerprint와 comparator entry point는 아직 닫히지 않았다. 이 gap이 cross-phase review로 해소되기 전에는 route objective vector 합산, unassigned/used-vehicle dimension 추정 또는 Phase 05 ad hoc aggregator를 승인하지 않는다.

## 3. Phase-local 결정과 불변조건

아래 이름과 signature는 **proposed internal design**이다. Phase 03 review가 의미와 API를 승인하기 전 public compatibility 약속이 아니다. 새 `C-*`/`Q-*` decision ID를 만들지 않는다.

| 결정 | 상태 | 내용 |
|---|---|---|
| Result triage | PROPOSED | `Feasible`, 정상 hard `Infeasible`, 구조/authority/overflow `Invalid`를 분리한다. Corruption을 낮은 score나 unassignment로 숨기지 않는다. |
| Physical/evaluation split | PROPOSED | `propagation`은 물리 사실과 canonical hard rule, `evaluation.runtime`은 extension hard/metric/score/objective/comparator를 소유한다. |
| Authority input | FIXED | Raw DTO/좌표/속도/`C`/reverse arc를 보지 않고 Phase 02의 `ProblemInstance`와 `PreparedTravel`만 물리 권위로 소비한다. |
| Cache | FIXED | Full recomputation이 source of truth다. Cache는 외부 최적화이며 hit/miss가 result를 바꾸지 못한다. |
| Numeric | FIXED | Phase 03은 이미 정규화된 integer/fixed-point `long`만 소비한다. 새 decimal/rounding/default를 만들지 않고 모든 합·차·곱을 checked 수행한다. |
| Objective order | FIXED | 양보 불가 우선순위는 ordered lexicographic dimension이다. Hidden Big-M/finite hard penalty를 금지한다. |
| Stable tie | PROPOSED | Objective가 모두 같을 때만 canonical `StableTieKey`로 total order를 만든다. Tie key는 business objective 값이 아니다. |
| Facet seam | OPEN/PROPOSED | `DomainFacetProvider`의 customer-neutral typed seam은 Phase 04/ADR review 대상이다. Base propagation 완료를 막지 않으며 승인 전 임의 script/reflection hook을 만들지 않는다. |
| Solution-level evaluation owner | OPEN/CROSS-PHASE BLOCKER | Canonical `RM-2`의 full route/solution evaluation과 Phase 05 `CandidateEvaluationArtifact` 사이 owner/API가 현재 Phase 03~05 문서에 닫히지 않았다. Route-only artifact를 solution objective authority로 승격하지 않는다. |
| Comparator tie boundary | OPEN/CROSS-PHASE BLOCKER | Phase 03의 business objective equality/solution total order와 Phase 05 insertion-context tie key가 어떤 API에서 분리되는지 cross-phase review가 필요하다. Tie key로 non-tied objective를 뒤집지 않는 의미만 고정한다. |

반드시 지킬 불변조건은 다음과 같다.

1. **Purity:** 입력 artifact를 변경하지 않고 외부 clock, randomness, locale, timezone, global registry와 I/O를 읽지 않는다.
2. **Determinism:** Ordered dense ID/key와 exact declaration을 사용하고 hash iteration/classpath order에 의존하지 않는다.
3. **Precedence:** Real pickup은 같은 request delivery보다 먼저 정확히 한 번 나타난다. Delivery-only logical pickup은 route service visit가 아니다.
4. **Capacity:** Initial load와 모든 service prefix에서 weight/volume 각각 `0 <= load <= capacity`; 완성 single-trip의 final load는 0이다.
5. **Time:** Plan은 `[planStart, planEnd)`, service-window close와 due date는 inclusive, 계산 실패와 feasible time을 같은 숫자로 표현하지 않는다.
6. **Travel:** 실제 통과한 directed `D_meter`와 vehicle-resolved `U_second`만 사용한다. Self `0/0`과 provided/generated 의미는 Phase 02 artifact를 신뢰하되 fingerprint를 대조한다.
7. **Full-arc:** `departure + travelTime <= currentWorkEnd`일 때만 출발한다. 그렇지 않으면 다음 이용 가능 work start에서 같은 arc 전체를 시작한다. Mid-arc pause/resume은 없다.
8. **Service:** `serviceEnd = departure = serviceStart + normalizedServiceTime`; Phase 03은 `duration`/item `taskTime`을 다시 조합하지 않는다.
9. **Resource:** Stop/drive/resource는 route 전체에서 reset하지 않고, limit은 inclusive upper bound다. 누락 limit은 `AbsentConstraint`이지 큰 숫자가 아니다.
10. **Layering:** Hard violation은 metric/score/objective/comparator/SA가 상쇄하지 못한다. Metric에는 가격·선호가 없고 score는 route/raw input을 재해석하지 않는다.
11. **Failure:** Overflow, fingerprint mismatch, missing prepared lookup, duplicate/ill-typed component와 route corruption은 `Invalid`; 정상적으로 값이 계산되지만 hard bound를 넘은 경우만 `Infeasible`다.
12. **No premature responsibility:** Kernel은 insertion, ALNS, verification verdict, final diagnostic와 publication을 소유하지 않는다.

## 4. Entry gate와 확인 방법

문서 작성은 완료할 수 있지만 implementation 착수는 다음 gate가 모두 충족될 때까지 `BLOCKED`다.

| Entry 항목 | 확인 방법 | 현재 checkout 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Accepted review와 content-addressed `E-P00-ARCH`; reactor에 `rpdptw-core`가 존재하고 architecture rule 통과 | Root 단일 project, target reactor 없음 | BLOCKED |
| Phase 01 accepted | `E-P01-NUMERIC`, `E-P01-TIME`, immutable normalized policy fingerprint | Target artifact/test/evidence 없음 | BLOCKED |
| [Phase 02 상세](phase-02-prepared-travel-immutable-problem.md) accepted | Phase 02 phase-accepting review verdict, `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`; complete coverage와 solver/verifier fingerprint equality | 상세/review 문서는 actual이고 document review는 `PASS_AFTER_APPLIED_CORRECTIONS`지만 phase verdict는 `BLOCKED_NOT_IMPLEMENTED`; accepted artifact/evidence 없음 | BLOCKED |
| API/unit declaration review | Phase 03 reviewer가 §7 signature, unit, failure triage와 Phase 04/05 compatibility를 승인 | 이 문서가 최초 proposed input | REVIEW_REQUIRED |
| Owner/scheduler identity | Scheduler task ID와 구현/독립-review 역할 지정 | Scheduler task ID 미제공 | OWNER_GATE |

Phase 02 handoff를 받을 때 단순 type 존재가 아니라 다음을 재검증한다.

```text
ProblemInstance.preparedTravelFingerprint
  == PreparedTravel.fingerprint

all route-reachable SolverNodeId
  → one PhysicalLocationId

all vehicle/location directed pairs
  → one resolved integer distance/time source

numeric/time/service/travel policy fingerprint
  == evidence bundle declaration
```

하나라도 다르면 propagation을 실행하거나 임시 lookup/default로 보완하지 않는다. Owner는 Phase 02 Domain/Travel 역할이며 마지막 안전 지점은 이 문서와 test fixture 설계뿐이다.

## 5. 2026-07-28 current inventory

Read-only inventory 기준은 branch `codex/domain-design`, commit `3424277`이다. 이 문서를 만들기 전부터 `docs/implementation/`은 Git 기준 untracked였으며 기존 사용자 변경으로 취급한다.

| 항목 | 실제 관찰 | Phase 03 해석 |
|---|---|---|
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Java 25 목표와 일치하지만 Phase 03 build/evidence는 아님 |
| Maven | Root `pom.xml` 단일 `com.ronext:ro-next:0.1.0-SNAPSHOT`, `<maven.compiler.release>25</maven.compiler.release>`, module 목록 없음 | `rpdptw-core`와 reactor/architecture gate가 아직 없음 |
| Root dependencies | Google Workflow Executions/Storage, Jackson, JUnit가 같은 classpath | Target core/cloud 격리 전 placeholder inventory |
| Main Java | `com.ronext.optimizer` 아래 HTTP/GCP adapter 5개와 `AlnsBatchEngine` 1개 | RPDPTW propagation/evaluation 구현이 아님 |
| Placeholder behavior | `AlnsBatchEngine`이 `double` random diversification과 합성 objective `Map<String,Object>`를 반환 | Feasibility/objective/evidence로 재사용 금지 |
| Test | `AlnsBatchEngineTest` 1개가 synthetic candidate 필드만 확인 | Phase 03 evidence가 아님 |
| Target Phase/review files | Phase 00~14 상세 15개 actual; final validation 시 Phase 00은 `REVIEWED_WITH_CORRECTIONS`, Phase 01~14 상세는 `READY_FOR_REVIEW`, implementation은 모두 not-started/gated. Phase 00~03 review 4개 actual, Phase 04~14 review 없음 | 공유 workspace에서 새 review를 감지해 재확인했으며, document review 존재와 phase acceptance/evidence를 계속 구분 |

이 문서 작업은 기존 `src`, `pom.xml`, `target`, 공용 README/plan/progress와 다른 Phase/review를 변경하지 않는다.

## 6. Proposed 변경 module/package/file tree

Phase 03 implementation에서 허용할 target은 아래와 같다. 실제 경로는 Phase 00 accepted reactor와 일치해야 하며, 이 문서 작성 turn에서는 어느 파일도 만들지 않는다.

```text
rpdptw/core/
├── pom.xml                                      # Phase 00 owner; Phase 03은 필요 시 test-scope만
├── src/main/java/com/ronext/rpdptw/
│   ├── domain/
│   │   ├── RoutePlan.java
│   │   └── RoutePlanFingerprint.java
│   ├── propagation/
│   │   ├── RoutePropagator.java
│   │   ├── PropagationResult.java
│   │   ├── RouteFacts.java
│   │   ├── VisitFacts.java
│   │   ├── TravelLegFacts.java
│   │   ├── RouteResourceTotals.java
│   │   ├── ConstraintRejection.java
│   │   ├── ConstraintCode.java
│   │   ├── EvaluationFailure.java
│   │   ├── PropagationDeclaration.java
│   │   ├── DomainFacetProvider.java          # OPEN/PROPOSED; ADR gate, final shape not approved
│   │   └── internal/
│   │       ├── ForwardRoutePropagator.java
│   │       ├── RouteStructureValidator.java
│   │       ├── WorkWindowArcResolver.java
│   │       └── CheckedRouteMath.java
│   └── evaluation/
│       ├── api/
│       │   ├── HardConstraint.java
│       │   ├── MetricContributor.java
│       │   ├── ScoreComponent.java
│       │   ├── ObjectiveDimension.java
│       │   ├── ObjectiveComparator.java
│       │   ├── MetricValue.java
│       │   ├── ScoreValue.java
│       │   ├── ObjectiveVector.java
│       │   └── EvaluationUnit.java
│       └── runtime/
│           ├── RouteEvaluationKernel.java
│           ├── RouteEvaluationRequest.java
│           ├── RouteEvaluationResult.java
│           ├── RouteEvaluationArtifact.java
│           ├── EvaluationPlan.java
│           ├── EvaluationPlanFingerprint.java
│           ├── EvaluationSnapshot.java
│           ├── LexicographicObjectiveComparator.java
│           └── internal/
│               ├── DefaultRouteEvaluationKernel.java
│               └── EvaluationContractValidator.java
└── src/test/java/com/ronext/rpdptw/
    ├── propagation/
    │   ├── CoreRouteTestData.java
    │   ├── RouteStructureValidatorTest.java
    │   └── RoutePropagationBoundaryTest.java
    └── evaluation/
        ├── RouteEvaluationLayerTest.java
        ├── RouteEvaluationContractCorruptionTest.java
        └── RouteEvaluationImmutabilityTest.java

build/test-fixtures/
├── src/main/java/com/ronext/rpdptw/testing/
│   ├── RpdptwFixtureBuilder.java
│   ├── HandPropagationFixtureBuilder.java
│   ├── CorruptAuthorityFixtureBuilder.java
│   ├── ExhaustiveRouteOracle.java
│   └── BigIntegerEvaluationOracle.java
└── src/test/java/com/ronext/rpdptw/testing/
    ├── RoutePropagatorHandOracleTest.java
    ├── RoutePropagationPropertyTest.java
    ├── RoutePropagationMetamorphicTest.java
    ├── RoutePropagationCorruptionTest.java
    ├── RouteEvaluationComparatorPropertyTest.java
    ├── RouteEvaluationCacheEquivalenceTest.java
    ├── RoutePropagationReproducibilityTest.java
    └── Phase03OracleSensitivityTest.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase03KernelArchitectureTest.java
```

금지 변경 target:

- 현재 `com.ronext.optimizer.*` placeholder를 Phase 03 kernel로 개조
- `rpdptw-solver`, `rpdptw-verification`, capability/profile catalog, adapter/app/deployment
- Cloud/HTTP/Jackson/vendor dependency를 `rpdptw-core`에 추가
- Root dependency를 Phase 03 편의상 공통 classpath로 승격
- 다른 Phase 상세/review와 공용 scheduler/status 문서

## 7. I/O artifact, contract, identity와 lifecycle

### 7.1 Artifact 계약

| Artifact | Owner/생성 시점 | Identity 최소 구성 | Lifecycle/소비자 |
|---|---|---|---|
| `ProblemInstance` | Phase 02 | Dense mapping + numeric/time/service/compatibility policy fingerprint | 생성 후 immutable; Phase 03이 읽기만 함 |
| `PreparedTravel` | Phase 02 | Coverage, physical-location/vehicle mapping, source/rounding policy fingerprint | 생성 후 immutable; Phase 03은 exact identity 대조 후 directed lookup만 |
| `RoutePlan` | Phase 03 internal contract, 이후 Phase 05가 생성 | Problem identity, vehicle dense ID, ordered solver-node IDs, terminal/trip meaning | 생성 후 immutable; mutation draft를 받지 않음 |
| `PropagationDeclaration` | Phase 03 API, Phase 04가 bind | Explicit service-window rule, ordered typed facet declarations, contract versions | Solve-bound immutable; Phase 03 test는 `test-only` builder로 명시 생성 |
| `EvaluationPlan` | Phase 03 API, Phase 04 binder가 생성 | Ordered hard/metric/score/objective declarations, unit/type closure, comparator/tie policy, fingerprint | Solve-bound immutable; unknown/latest/default 보완 없음 |
| `RouteFacts` | `RoutePropagator` | Input identities, visit/leg facts와 resource totals의 canonical digest | Call-local immutable result; 가격/objective/final diagnostic 없음 |
| `RouteEvaluationArtifact` | Kernel의 `Feasible` 결과 | All input identities + facts/metric/score/objective schema/value fingerprints | Phase 05 ranking/cache와 Phase 07 cache-free recomputation이 소비 |
| `ConstraintRejection` | 정상 hard violation | Exact problem/travel/route/propagation/evaluation identities + stable constraint code, route position/subject, typed actual/bound/unit | Call-local `Infeasible` 안에서만 존재; 최종 unassignment diagnostic이나 reusable cache authority 아님 |
| `EvaluationFailure` | Corruption/contract/overflow | Relevant exact input identities + failure kind, authority/route/component identity와 safe location | Call-local `Invalid`; ranking·penalty·publication·normal rejection 변환 금지 |

Fingerprint encoding/hash algorithm은 Phase 00/serialization ADR가 소유한다. 이 Phase는 **어떤 semantic field가 identity에 반드시 들어가는지**를 고정하며 임의 `toString()`, object identity, unordered map 또는 mutable `latest`를 fingerprint 입력으로 쓰지 않는다.

모든 proposed record는 collection/array/element를 생성 시점에 defensive immutable value로 동결하고 mutable accessor/backing storage를 노출하지 않는다. `Feasible`, `Infeasible`, `Invalid`의 값은 한 호출의 exact input identity에 결합한다. 특히 rejection/failure를 다른 route, problem, travel 또는 evaluation plan에 재사용하거나 stale cache의 keyless reason으로 승격하지 않는다.

Failure taxonomy는 다음 경계를 보존한다.

| Outcome | Exact 범주 | 예시 | 금지 변환 |
|---|---|---|---|
| `Feasible` | 모든 canonical/extension hard rule 통과, complete immutable artifact 생성 | Exact boundary와 inclusive limit 만족 | Missing component/default 보완 뒤 feasible |
| `Infeasible` | Authority와 구조가 정상이고 계산도 성공했지만 hard bound를 넘음 | Capacity prefix, service window/due/plan end, single-window full arc 부재, stop/drive/resource limit, composed hard rejection | Finite penalty/score/ranking으로 상쇄 |
| `Invalid` | 입력/route/component authority가 손상됐거나 계산 결과를 신뢰할 수 없음 | Fingerprint/lookup mismatch, partial/duplicate/precedence/terminal corruption, final non-zero load contradiction, duplicate/ill-typed component, checked overflow, component contract/execution failure | Normal insertion rejection, unassignment reason 또는 낮은 score로 축소 |

여러 defect가 동시에 있는 fixture는 `authority identity → route structure → propagation arithmetic/lookup → component declaration → component execution`의 stable precedence로 첫 failure를 선택한다. Exact subtype/이름은 API review 대상이지만 같은 bytes가 thread/order에 따라 다른 category/code를 내면 evidence가 실패한다.

### 7.2 Proposed Java 25 contract

```java
package com.ronext.rpdptw.domain;

public record RoutePlan(
    ProblemFingerprint problemFingerprint,
    VehicleId vehicleId,
    List<SolverNodeId> orderedNodeIds,
    RoutePlanFingerprint fingerprint
) {
    // compact constructor: defensive copy, non-null, canonical fingerprint equality
}
```

`orderedNodeIds`는 start terminal을 첫 원소로 가진다. `oneway`는 마지막 service node에서 끝나고 single `roundtrip`은 같은 approved terminal을 마지막 원소로 가진다. Delivery-only logical pickup은 포함하지 않는다. Node에서 request/service pattern/location을 푸는 유일한 권위는 `ProblemInstance`다.

```java
package com.ronext.rpdptw.propagation;

public interface RoutePropagator {
    PropagationResult propagate(
        ProblemInstance problem,
        PreparedTravel preparedTravel,
        RoutePlan route,
        PropagationDeclaration declaration
    );
}

public sealed interface PropagationResult
        permits PropagationResult.Completed,
                PropagationResult.Rejected,
                PropagationResult.Invalid {

    record Completed(RouteFacts facts) implements PropagationResult {}
    record Rejected(ConstraintRejection rejection) implements PropagationResult {}
    record Invalid(EvaluationFailure failure) implements PropagationResult {}
}

public enum ServiceWindowCompletion {
    START_ONLY,
    COMPLETE_WITHIN_WINDOW
}

public record PropagationDeclaration(
    ServiceWindowCompletion serviceWindowCompletion,
    List<DomainFacetProvider<?>> facetProviders,
    PropagationDeclarationFingerprint fingerprint
) {}
```

`PropagationDeclaration`은 Phase 04의 `BoundProfile` 자체가 아니다. Phase 03이 소유하는 stable internal input이며 Phase 04 binder가 exact profile/capability를 이 계약으로 materialize한다. `DomainFacetProvider`의 최종 shape/version은 계속 `OPEN/PROPOSED`이며 `ADR-004`와 Phase 07 독립 재계산 계약 전에는 empty list만 base acceptance 대상이다. Provider contract는 propagation package가 소유해 `propagation ↔ evaluation.api` package cycle을 만들지 않는다. Phase 03 test는 누락값을 default로 채우지 않고 `test-only` declaration을 명시한다.

```java
public record RouteFacts(
    RoutePlanFingerprint routePlanFingerprint,
    ProblemFingerprint problemFingerprint,
    PreparedTravelFingerprint preparedTravelFingerprint,
    List<TravelLegFacts> legs,
    List<VisitFacts> visits,
    LoadVector initialLoad,
    LoadVector finalLoad,
    RouteResourceTotals totals,
    FacetSnapshot facets,
    RouteFactsFingerprint fingerprint
) {}

public record VisitFacts(
    int routeIndex,
    SolverNodeId nodeId,
    PhysicalLocationId locationId,
    long arrival,
    long serviceStart,
    long serviceEnd,
    long departure,
    long customerWaitingTime,
    long serviceTime,
    LoadVector loadAfterService
) {}

public record RouteResourceTotals(
    long distance,
    long driveTime,
    long customerWaitingTime,
    long depotWaitingTime,
    long serviceTime,
    long interWorkWindowRestTime,
    long routeOperationalTime,
    long stopCount
) {}
```

Field 이름은 원문의 neutral fact 이름인 `arrival`, `serviceStart`, `serviceEnd/departure`, `loadWeight/loadVolume`, `distance`, `driveTime`, `customerWaitingTime`, `depotWaitingTime`, `serviceTime`, `interWorkWindowRestTime`, `routeOperationalTime`, `stopCount`, typed facet facts를 그대로 보존한다. Java naming 조정은 review 가능하지만 의미 병합은 금지한다.

```java
package com.ronext.rpdptw.evaluation.runtime;

public interface RouteEvaluationKernel {
    RouteEvaluationResult evaluate(RouteEvaluationRequest request);
}

public record RouteEvaluationRequest(
    ProblemInstance problem,
    PreparedTravel preparedTravel,
    RoutePlan route,
    PropagationDeclaration propagation,
    EvaluationPlan evaluation
) {}

public sealed interface RouteEvaluationResult
        permits RouteEvaluationResult.Feasible,
                RouteEvaluationResult.Infeasible,
                RouteEvaluationResult.Invalid {

    record Feasible(RouteEvaluationArtifact artifact)
        implements RouteEvaluationResult {}

    record Infeasible(
        ConstraintRejection rejection,
        Optional<RouteFacts> safePrefixFacts
    ) implements RouteEvaluationResult {}

    record Invalid(EvaluationFailure failure)
        implements RouteEvaluationResult {}
}
```

`safePrefixFacts`는 debugging/evidence용 immutable prefix일 뿐 score/objective를 만들 수 없고 기본 output diagnostic으로 노출하지 않는다.

```java
package com.ronext.rpdptw.evaluation.api;

public interface HardConstraint {
    ConstraintKey key();
    ConstraintCheck check(
        ProblemInstance problem,
        RoutePlan route,
        RouteFacts facts,
        MetricSnapshot neutralMetrics
    );
}

public sealed interface ConstraintCheck
        permits ConstraintCheck.Satisfied,
                ConstraintCheck.Rejected,
                ConstraintCheck.Invalid {
    record Satisfied() implements ConstraintCheck {}
    record Rejected(ConstraintRejection rejection) implements ConstraintCheck {}
    record Invalid(EvaluationFailure failure) implements ConstraintCheck {}
}

public interface MetricContributor {
    MetricDeclaration declaration();
    MetricValue contribute(RouteFacts facts);
}

public interface ScoreComponent<P extends ScoreParameters> {
    ScoreDeclaration<P> declaration();
    ScoreValue score(MetricSnapshot metrics, P parameters);
}

public interface ObjectiveDimension {
    ObjectiveDeclaration declaration();
    ObjectiveComponent evaluate(EvaluationSnapshot snapshot);
}

public interface ObjectiveComparator {
    int compare(ObjectiveVector left, ObjectiveVector right);
}

public record MetricValue(MetricKey key, EvaluationUnit unit, long value) {}
public record ScoreValue(ScoreKey key, EvaluationUnit unit, long value) {}
public record ObjectiveComponent(
    ObjectiveKey key,
    ObjectiveDirection direction,
    long value
) {}
```

Parameter는 typed record/sealed hierarchy만 허용하고 `Map<String,Object>`, expression/script, reflection raw-route access를 금지한다. Metric/score/objective key 중복, unit/type mismatch, declaration/fingerprint 불일치는 evaluation 전 `Invalid(ComponentContractViolation)`이다.

Kernel은 모든 extension component 호출을 fail-closed boundary 안에서 수행한다. Metric/score/objective의 checked arithmetic overflow, declared typed component failure 또는 반환 key/unit/type/fingerprint 위반은 `Invalid(EvaluationFailure)`로 올린다. 예상하지 못한 component exception도 정상 hard rejection으로 바꾸지 않고 call을 `Invalid`로 종료하며 Phase evidence를 실패시킨다. Component가 일부 snapshot을 발행한 뒤 계속 진행하거나 exception을 zero/maximum/default 값으로 바꾸는 것은 금지한다.

`EvaluationPlan`의 logical shape는 다음을 보존한다.

```java
public record EvaluationPlan(
    List<HardConstraint> hardConstraints,
    List<MetricContributor> metricContributors,
    List<BoundScoreComponent<?>> scoreComponents,
    List<ObjectiveDimension> objectiveDimensions,
    ObjectiveComparator comparator,
    StableTiePolicy stableTiePolicy,
    EvaluationPlanFingerprint fingerprint
) {}
```

Phase 04 binder 전 Phase 03 fixture는 명시적인 `test-only` component와 parameter만 사용한다. 고객 가격, mandatory/ownership 공식값 또는 official calibration 값을 Phase 03 production default로 넣지 않는다.

### 7.3 Dependency direction

```text
domain + travel
        ↓
propagation
        ↓
evaluation.api
        ↓
evaluation.runtime

Phase 04 rpdptw-capabilities ───────→ evaluation.api
Phase 04 approved facet capability ─→ propagation/DomainFacetProvider  # ADR-004 only
Phase 04 profile binder/catalog ────→ evaluation.api/runtime contracts
Phase 05 rpdptw-solver ─────────────→ evaluation.runtime
Phase 07 rpdptw-verification ───────→ rpdptw-core cache-free contracts
```

위 화살표는 위쪽 stable fact/package를 아래쪽 package가 소비한다는 뜻이다. `DomainFacetProvider`가 필요한 경우에도 owner는 `propagation` 쪽 stable SPI이며 `evaluation.api`가 이를 소비한다. 반대 import로 package cycle을 만들지 않는다. `build/test-fixtures → rpdptw-core`는 Phase 00의 test-only 단방향 module edge이고, `rpdptw-core` production/test source는 `build/test-fixtures`를 dependency로 갖지 않는다. Independent oracle/property test는 test-fixtures module에서 core public/internal-test contract를 바깥에서 호출한다.

금지 방향:

- `propagation → evaluation.runtime`, customer/profile implementation, solver, verifier, adapter
- `propagation → evaluation.api`와 `rpdptw-core → build/test-fixtures`의 역방향/package cycle
- `rpdptw-core → rpdptw-capabilities` 또는 profile catalog
- `evaluation.api → customer implementation`, search/cache, cloud/vendor SDK
- Phase 05/07이 raw input이나 별도 travel generation으로 kernel authority를 우회

## 8. Route state transition과 pseudocode

### 8.1 구조 gate

전파 전에 다음을 stable dense order로 검사한다.

```text
authority fingerprints agree
→ route vehicle exists
→ first node == vehicle start terminal
→ trip terminal policy is exact
→ no internal/foreign terminal
→ each real pair appears pickup once + delivery once
→ pickup index < delivery index
→ each delivery-only request has exactly one delivery service
→ node/request/service-pattern references agree
```

Partial pair, duplicate node/pair, delivery-before-pickup, foreign terminal과 fingerprint mismatch는 “배정하기 어려운 route”가 아니라 corrupted `RoutePlan`/authority이므로 `Invalid`다. Phase 05의 정상 insertion 탐색은 구조가 유효한 option만 kernel에 전달해야 한다.

### 8.2 Canonical forward propagation

```text
function evaluate(problem, travel, route, propagationDecl, evaluationPlan):
    validateExactIdentitiesOrInvalid()
    validateRouteStructureOrInvalid()

    propagated = propagate(problem, travel, route, propagationDecl)
    if propagated is Rejected:
        return Infeasible(rejection, optionalSafePrefix)
    if propagated is Invalid:
        return Invalid(failure)

    facts = propagated.facts
    metrics = contributeNeutralMetrics(facts)
    for constraint in evaluationPlan.hardConstraints in stable key order:
        check = constraint.check(problem, route, facts, metrics)
        if rejected:
            return Infeasible(rejection, facts)
        if invalid:
            return Invalid(failure)

    scores = calculateScores(metrics)              # no route/raw-input access
    vector = evaluateOrderedObjective(metrics, scores)
    artifact = immutableArtifact(facts, metrics, scores, vector, identities)
    return Feasible(artifact)
```

Structural/static/physical hard gate가 propagation을 거절하면 metric을 포함한 어떤 evaluation component도 호출하지 않는다. Propagation이 completed된 뒤에는 canonical Master/Final Domain 순서대로 neutral metric snapshot을 먼저 만들고, 그 snapshot과 facts를 읽는 composed hard constraint를 평가한다. Composed hard rejection 뒤에는 score/objective를 호출하지 않는다. 따라서 “hard rejection 뒤 downstream call 0”은 propagation hard rejection과 composed hard rejection에서 기대 call set이 서로 다르며 test도 둘을 분리한다.

전파 자체는 다음 state를 가진다.

```text
state:
  currentNode
  currentLocation
  departure
  loadWeight
  loadVolume
  lastCustomerServiceLocation | NONE
  distance
  driveTime
  customerWaitingTime
  depotWaitingTime
  serviceTime
  interWorkWindowRestTime
  stopCount
  typedFacetStates
```

```text
initialLoad
  = Σ normalized demand of assigned delivery-only requests in this route

assertChecked(0 <= initialLoadWeight <= vehicleCapacityWeight)
assertChecked(0 <= initialLoadVolume <= vehicleCapacityVolume)

earliestDeparture = max(vehicleWorkStart, depotOpen)

if waitInDepot == Y:
    departure =
      max(earliestDeparture, firstCustomerOpen - firstArcTravelTime)
    depotWaitingTime += departure - earliestDeparture
else:
    departure = earliestDeparture

for every actual arc (currentNode → nextServiceNode):
    D = preparedTravel.distance(currentLocation, nextLocation).value()
    U = preparedTravel.travelTime(
          vehicle, currentLocation, nextLocation
        ).value()

    containingWorkWindow =
      unique normalized work window containing departure, if any

    if containingWorkWindow exists
       and checked(departure + U) <= containingWorkWindow.end:
        arcWindow = containingWorkWindow
        arcStart = departure
    else:
        arcWindow =
          first stable future normalized work window where
            workWindow.start >= departure
            and checked(workWindow.start + U) <= workWindow.end
        if none inside [planStart, planEnd):
            reject NO_SINGLE_WORK_WINDOW_FOR_FULL_ARC
        interWorkWindowRestTime =
          checked(interWorkWindowRestTime
                  + checked(arcWindow.start - departure))
        arcStart = arcWindow.start                    # restart full arc

    arrival = checked(arcStart + U)
    distance = checked(distance + D)
    driveTime = checked(driveTime + U)

    serviceWindow =
      first expanded/clipped window in which explicit completion rule succeeds
    if none:
        reject SERVICE_WINDOW

    serviceStart = max(arrival, serviceWindow.open)
    customerWaiting = serviceStart - arrival
    serviceEnd = departure = checked(serviceStart + normalizedServiceTime)

    if explicit rule == START_ONLY:
        require serviceStart <= serviceWindow.close
    if explicit rule == COMPLETE_WITHIN_WINDOW:
        require serviceEnd <= serviceWindow.close
    require serviceEnd <= requestDueDate
    require every event < planEnd

    load += canonical delta(nextServiceNode)
      REAL_PICKUP                => +demand
      REAL_DELIVERY              => -demand
      DELIVERY_ONLY_DELIVERY     => -demand
    require every prefix 0 <= load <= capacity

    if next customer location != last customer service location:
        stopCount = checked(stopCount + 1)
    lastCustomerServiceLocation = next customer location

    accumulate service/wait/facet facts

if roundtrip:
    traverse final customer → approved terminal using the same full-arc rule
if oneway:
    do not synthesize a return arc

if final loadWeight != 0 or final loadVolume != 0:
    invalid ROUTE_OR_AUTHORITY_FINAL_LOAD_CORRUPTION
require driveTime <= optional maxDriveTime
require distance <= optional maxDriveDist
require stopCount <= minPresent(vehicleMaxStop, globalMaxStop)

routeOperationalTime =
    checked(driveTime
          + customerWaitingTime
          + depotWaitingTime
          + serviceTime
          + interWorkWindowRestTime)
```

모든 `+`, `-`, initial-load sum, delta, total과 limit 비교는 `Math.addExact`/`subtractExact` 또는 동등한 checked operation을 쓴다. Initial/prefix capacity, window, due date, plan end, full-arc와 resource bound 초과는 값과 authority가 정상인 hard `Infeasible`이다. Final non-zero load는 complete route 구조/authority와 모순되므로 `Invalid`다. Overflow는 saturation/wrap/clamp/plan-end sentinel 또는 `Infeasible`가 아니라 `Invalid(ArithmeticOverflow)`다.

### 8.3 Neutral resource 의미

| Fact | 포함 | 제외/주의 |
|---|---|---|
| `distance` | 실제 통과 directed arc의 `D_meter` | waiting/service/rest, reverse fallback |
| `driveTime` | 실제 통과 vehicle-resolved `U_second` | customer/depot waiting, service, inter-work rest |
| `customerWaitingTime` | Arrival부터 service start까지 customer location 대기 | `waitInDepot=Y`로 depot에 옮긴 대기 |
| `depotWaitingTime` | Explicit wait-in-depot policy로 이동한 조기 대기 | Inter-work-window rest와 depot service 추정 |
| `serviceTime` | Phase 02 normalized node/request service seconds | Phase 03 재정규화, depot `taskTime` |
| `interWorkWindowRestTime` | Full arc를 다음 work start에서 재시작하며 현재 위치에서 쉰 시간 | drive/customer waiting |
| `stopCount` | 첫 customer location 진입 및 직전 customer location과 다른 진입 | Start/end/internal depot, logical pickup; work window에서 reset 금지 |
| `routeOperationalTime` | 위 공식의 다섯 시간 합 | Unused vehicle idle, solver elapsed, finalization elapsed |

`A → B → A`는 첫 `A` 진입을 포함해 stop `3`; `A → A → B`는 stop `2`다. Roundtrip final depot arc는 distance/drive에 포함되지만 stop에는 포함되지 않는다.

## 9. Independent oracle과 exact test plan

### 9.1 Oracle 독립성

Production `ForwardRoutePropagator`, evaluation component, comparator 또는 cache가 expected result를 만들면 안 된다.

- `HandPropagationFixtureBuilder`는 사람이 계산한 event table과 expected typed outcome을 literal fixture로 보존한다.
- `ExhaustiveRouteOracle`은 작은 instance의 가능한 expanded service/work window를 열거해 earliest valid timeline을 고른다. Production forward-loop helper를 호출하지 않는다.
- `BigIntegerEvaluationOracle`은 합·차·objective 비교를 `BigInteger`로 수행한 뒤 `long` 범위를 별도 판정해 production overflow bug를 같은 방식으로 반복하지 않는다.
- Corruption builder는 정상 public factory를 우회한 test-only bytes/fixture로 fingerprint, mapping, duplicate key와 cached summary를 한 필드씩 변조한다.
- Oracle input/expected table/failure seed는 evidence bundle에 canonical digest와 함께 넣는다.

### 9.2 필수 hand-calculated fixture

`HandPropagationFixtureBuilder.mixedRouteWithWorkWindowRestart()`는 다음 **test-only integer** fixture를 만든다. 이 값은 official calibration/default가 아니다.

```text
vehicle:
  trip = roundtrip
  capacityWeight = 10
  workWindows = [0,100], [200,400]
  waitInDepot = N

route:
  D(start) → A(delivery-only 4) → B(real pickup 3)
  → B(real delivery 3, same physical location) → D(end)

directed travel time:
  D→A = 5, A→B = 80, B→B = 0, B→D = 10

directed distance:
  D→A = 100, A→B = 800, B→B = 0, B→D = 120

service:
  A open = 20, service = 10
  pickup B service = 5
  delivery B service = 5
```

Expected event/resource table:

| Event | Arrival | Wait/rest | Service start/end | Load after | Stop |
|---|---:|---:|---:|---:|---:|
| Start D | 0 | 0 | — | initial `4` | 0 |
| A delivery | 5 | customer `15` | `20/30` | `0` | 1 |
| B pickup | 280 | inter-work rest before arc `170` | `280/285` | `3` | 2 |
| B delivery, same location | 285 | 0 | `285/290` | `0` | 2 |
| Return D | 300 | 0 | — | `0` | 2 |

```text
distance = 1,020
driveTime = 95
customerWaitingTime = 15
depotWaitingTime = 0
serviceTime = 20
interWorkWindowRestTime = 170
routeOperationalTime = 95 + 15 + 0 + 20 + 170 = 300
```

이 fixture는 mixed delivery/real pair, prefix load, same-location stop, full-arc restart, roundtrip final arc와 operational breakdown을 한 번에 검출한다. Oracle expected를 production result로 생성하지 않는다.

### 9.3 Exact test class/method matrix

모든 test는 미래 test이며 현재 파일/실행 evidence가 아니다.

| Class | Exact method | Builder/oracle | 검출해야 할 실패와 green 판정 |
|---|---|---|---|
| `RouteStructureValidatorTest` | `rejectsDeliveryBeforePickupAsInvalid()` | core-local `CoreRouteTestData.realPairRoute()` | Delivery-before-pickup을 finite penalty/normal infeasible로 통과시키면 실패; exact `Invalid(RouteCorruption.PRECEDENCE)` |
|  | `rejectsPartialDuplicateAndForeignTerminalRoutes()` | core-local malformed values/test hook | Partial/duplicate/foreign terminal 각각 stable code로 `Invalid`; route/bank reason으로 변환 금지 |
|  | `acceptsDeliveryOnlyLogicalPickupWithoutServiceVisit()` | delivery-only fixture | Logical pickup visit를 요구하거나 stop/travel에 세면 실패 |
| `RoutePropagatorHandOracleTest` | `propagatesMixedDeliveryAndPickupAcrossAllPrefixes()` | §9.2 hand table + `BigIntegerEvaluationOracle` | 모든 visit/time/load/total exact equality |
|  | `restartsWholeArcAtNextWorkWindow()` | A→B 80-second arc | 30→100 구간을 일부 운전하거나 drive 80 외 값을 세면 실패; start 200/arrival 280/rest 170 |
|  | `usesAdvancedWorkWindowForFollowingArcs()` | §9.2 B pickup→delivery→depot suffix | 200~400 창으로 이동한 뒤 B→B와 B→D가 같은 창을 사용; stale first-window end를 쓰면 실패 |
|  | `countsOnlyCustomerLocationTransitions()` | `A→A→B`, `A→B→A` | 각각 stop `2`, `3`; depot/logical pickup은 0 증가 |
|  | `includesFinalArcOnlyForRoundtrip()` | same service sequence, oneway/roundtrip pair | Roundtrip만 final D/U 포함; oneway return 합성 금지 |
| `RoutePropagationBoundaryTest` | `acceptsInclusiveCapacityWindowAndResourceLimits()` | exact-bound fixture | Load=capacity, serviceStart=close, arcEnd=workEnd, resource=limit가 `Feasible` |
|  | `rejectsOneUnitBeyondEveryHardBound()` | rows: weight/volume lower+upper, service close, due, plan end, full-arc work end, stop, drive time/distance | 각 +1 또는 -1이 해당 typed `ConstraintCode`; propagation hard면 metric/score/objective 미생성 |
|  | `distinguishesStartOnlyFromCompleteWithinWindow()` | start=close, positive service | Explicit `START_ONLY` feasible, `COMPLETE_WITHIN_WINDOW` reject; hidden default 없음 |
|  | `consumesExpandedRepeatingAndOvernightWindowsWithoutRawTimeReinterpretation()` | Phase 01/02 normalized expanded windows | 날짜/overnight raw parsing 없이 expanded order와 plan clipping을 사용; boundary exact |
|  | `rejectsEventAtPlanEnd()` | plan-end fixture | `[start,end)` 위반 검출; planEnd clamp 금지 |
|  | `reportsNonZeroFinalLoadAsInvalidCorruption()` | complete-route authority one-field corruption | Normal capacity rejection이 아니라 exact `Invalid`; ranking 금지 |
|  | `reportsAccumulationOverflowAsInvalid()` | `BigIntegerEvaluationOracle` + corrupt near-max input | Wrap/saturation/infeasible 금지; `Invalid(ArithmeticOverflow)` |
| `RoutePropagationPropertyTest` | `everyFeasibleGeneratedRouteRespectsAllLoadPrefixes()` | bounded exhaustive request/route generator | 각 prefix BigInteger oracle equality; 최소 counterexample와 seed/ordinal 기록 |
|  | `increasingCapacityCannotMakeAFeasibleRouteInfeasible()` | monotone capacity generator | 다른 authority가 같은 조건에서 capacity만 증가할 때 monotonicity |
|  | `increasingAbsentOrPresentResourceLimitIsMonotone()` | resource generator | Limit 완화가 rejection을 만들면 실패 |
|  | `constantTimeTranslationPreservesDurationsAndFeasibility()` | translated plan/windows/route | Safe range 안에서 timestamp만 `+k`, 모든 duration/load/resource 동일 |
| `RoutePropagationMetamorphicTest` | `movingEarlyWaitToDepotPreservesServiceAndOperationalTime()` | `waitInDepot N/Y` pair | N은 customer wait, Y는 depot wait; service times/feasibility/operational total 동일 |
|  | `usesDirectedArcWithoutReverseOrSymmetryFallback()` | asymmetric `A→B != B→A` | Route 방향별 exact prepared value; reverse lookup 시 실패 |
|  | `consecutiveSameLocationServiceAddsNoTravelOrStop()` | same physical location/different solver nodes | Self D/U 0, second stop 증가 0, service/load는 각각 적용 |
| `RoutePropagationCorruptionTest` | `rejectsProblemTravelFingerprintMismatch()` | one-field fingerprint corruption | Lookup 전 `Invalid(AuthorityMismatch)` |
|  | `rejectsMissingVehicleResolvedTravelInsteadOfGenerating()` | missing-cell corruption | Coordinate/speed/reverse fallback 없이 `Invalid(IncompletePreparedTravel)` |
|  | `rejectsPoisonedRoutePlanFingerprint()` | route order after fingerprint | Propagation 실행 전 invalid |
| `RouteEvaluationLayerTest` | `doesNotInvokeAnyEvaluationComponentAfterPropagationRejection()` | invocation-count spies, test-only | Structural/physical hard reject 뒤 metric/hard/score/objective call 모두 0 |
|  | `computesNeutralMetricsBeforeComposedHardAndStopsBeforeScore()` | ordered spies + metric-dependent hard rule | Metric → composed hard 순서; hard reject 뒤 score/objective call 0 |
|  | `keepsNeutralMetricsFreeOfPriceAndPreference()` | declared unit contract | Metric component가 cost/preference unit을 선언하면 plan validation invalid |
|  | `scoreReadsOnlyMetricSnapshotAndTypedParameters()` | compile/architecture + test component | Raw route/problem access signature가 있으면 build 실패 |
|  | `reportsEvaluationArithmeticAndComponentFailureAsInvalid()` | BigInteger expected + throwing/wrong-key components | Metric/score/objective overflow·typed failure·wrong return contract를 rejection/default가 아닌 `Invalid`로 종료 |
| `RouteEvaluationComparatorPropertyTest` | `lexicographicComparatorIsAntisymmetric()` | exhaustive small vectors | `sign(cmp(a,b)) == -sign(cmp(b,a))` |
|  | `lexicographicComparatorIsTransitive()` | exhaustive vector triples | `a≤b ∧ b≤c ⇒ a≤c` |
|  | `stableTieKeyCreatesDeterministicTotalOrderOnlyAfterObjectiveTie()` | tied/non-tied vectors | Non-tied business objective를 tie key가 뒤집지 않음 |
| `RouteEvaluationContractCorruptionTest` | `rejectsDuplicateMetricScoreAndObjectiveKeys()` | corrupt declaration builder | First-wins/last-wins 금지; pre-evaluation invalid |
|  | `rejectsUnitTypeAndDeclarationFingerprintMismatch()` | typed corruption | Conversion/default 없이 invalid |
| `RouteEvaluationImmutabilityTest` | `defensivelyCopiesEveryRouteFactPlanSnapshotAndResultCollection()` | mutable constructor/accessor probes | 생성 후 input mutation과 accessor mutation 시도에도 bytes/fingerprint 불변; mutable alias 0 |
| `RouteEvaluationCacheEquivalenceTest` | `cacheHitMissAndFullRecomputationAreExactlyEqual()` | poisoned optional cache + full kernel | Cache miss/full의 artifact bytes 일치; poisoned hit은 identity mismatch로 폐기 후 full recompute |
| `RoutePropagationReproducibilityTest` | `sameInputsProduceSameCanonicalResultAcrossRepeatedAndParallelCalls()` | fixed fixture, 100 sequential + bounded parallel calls | Result variant, fields, stable order, canonical bytes/fingerprint exact equality |
| `Phase03OracleSensitivityTest` | `independentOraclesRejectSeededPartialArcReverseLookupWrapAndComparatorOrderDefects()` | explicit faulty test doubles, not production helper | 각 seeded defect에서 해당 oracle test가 red이고 minimal counterexample/ordinal 고정; compile-failure만 sensitivity evidence로 사용 금지 |
| `Phase03KernelArchitectureTest` | `kernelHasNoCloudSolverVerifierCustomerOrVendorDependency()` | bytecode/dependency rule | Forbidden reference 0 |
|  | `propagationAndEvaluationPackagesAreAcyclic()` | package/DAG rule | `propagation → evaluation.*`와 runtime 역방향 import 0 |
|  | `coreDoesNotDependOnTestFixturesModule()` | Maven dependency tree | Phase 00의 `test-fixtures → core` 단방향만 존재 |

Property library는 새 production dependency를 요구하지 않는다. 우선 작은 bounded domain을 stable lexicographic order로 exhaustive 열거하여 counterexample 자체를 최소화한다. 별도 property framework 채택 시 Phase 00 dependency review와 reproducible seed/shrink report가 필요하다.

### 9.4 Red → green 순서

**Future red test expectation:** 각 행의 test를 production type/logic보다 먼저 추가한다. Type 부재의 compile red는 TDD 순서 evidence일 수 있지만 oracle sensitivity evidence는 아니다. Hand/exhaustive/BigInteger/comparator oracle은 `Phase03OracleSensitivityTest`의 의도적 faulty test double에서 각 결함을 실제 assertion red로 검출하고, 그 red report와 최소 counterexample를 green report와 함께 보존한다.

| 순서 | Red를 먼저 고정할 test | 예상 red | Green 조건 |
|---:|---|---|---|
| 1 | Structure/identity/corruption | Proposed type/validator 부재 또는 wrong-category test double red | Corrupt route/authority가 모두 typed `Invalid`; 정상 minimal route만 통과 |
| 2 | §9.2 hand oracle | Seeded partial-arc/stale-work-window implementation이 exact table mismatch | 모든 event/load/resource와 fingerprint exact |
| 3 | Boundary/full-arc/directed travel | Inclusive/off-by-one/reverse fallback red | Exact boundary와 next-window behavior 통과 |
| 4 | Metric/composed-hard/score/objective layering | Metric-dependent hard가 metric보다 먼저 호출되거나 hard 뒤 score/objective 호출 | Canonical call trace와 typed snapshot 통과 |
| 5 | Comparator laws | Pair/triple counterexample red | Antisymmetry/transitivity/stable tie 전수 통과 |
| 6 | Metamorphic/property/overflow | Translation/monotonicity/checked arithmetic red | Oracle와 최소 counterexample 0 |
| 7 | Cache/corruption/reproducibility/architecture | Poisoned cache나 unordered result 차이 red | Full equality, repeated fingerprint, forbidden reference 0 |

Red test를 skip/disable하거나 expected result를 production helper로 바꾸어 green을 만들면 evidence로 인정하지 않는다. Required test class/method가 실제 Surefire XML에 없으면 command exit code가 0이어도 실패다. Future module이 없는 현재 checkout에서 `./mvnw -pl rpdptw/core ...`가 실패하는 것은 expected implementation-not-started 상태이지 test 또는 oracle-sensitivity evidence가 아니다.

## 10. Ordered work packages

각 package는 앞 package green/evidence를 선행조건으로 한다. Command는 Phase 00 reactor가 존재한 뒤 실행할 **future exact command**다.

### WP-03.0 — Entry와 contract freeze

- **Prerequisite:** Phase 00~02 accepted bundle, owner/task 지정.
- **Change target:** Phase 03 detailed/review, `RoutePlan`/result/unit/failure API review record. Source code 없음.
- **Concrete tasks:** Source fingerprint 대조, Phase 02 artifact field inventory, §7 signature와 API status 확정, facet seam을 accepted/base 또는 deferred로 판정.
- **Verification:** `git diff --check -- docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md`; Markdown link/heading 검사; review checklist.
- **Test/check:** Source SHA-256 재계산, 필수 heading/metadata 검색, actual local-link 존재 검사와 planned-link allowlist를 각각 0 mismatch로 확인.
- **Expected:** No broken actual source link, actual-but-unaccepted Phase 02/04와 planned review를 구분하고 모든 proposed/open 표시 보존.
- **Failure/rollback:** Authority drift 또는 Phase 02 mismatch면 implementation 시작 금지; 문서는 마지막 reviewed version으로 되돌리고 source fingerprint부터 재개.
- **Handoff:** Approved internal contract와 red-test list를 WP-03.1에 전달.

### WP-03.1 — Route structure, identity와 failure triage

- **Prerequisite:** WP-03.0 contract approved.
- **Change target:** `domain/RoutePlan*`, `propagation` result/failure/validator, structure/corruption tests.
- **Concrete tasks:** Defensive immutable sequence, terminal/trip, complete pair/exactly-once/precedence, delivery-only representation, identity equality와 `Feasible/Infeasible/Invalid` triage 구현.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core \
    -Dtest=RouteStructureValidatorTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** §9.3의 `RouteStructureValidatorTest` exact methods 전부 green; structural corruption은 `Invalid`. 정상 physical hard-limit의 `Infeasible` 분류는 WP-03.2의 `RoutePropagationBoundaryTest`에서 검증한다.
- **Failure/rollback:** Route corruption이 normal rejection/score로 내려가거나 Phase 02 artifact를 바꾸어야 하면 중단. WP commit만 revert하고 accepted Phase 02 digest를 보존한다.
- **Handoff:** Validated immutable `RoutePlan`과 failure taxonomy를 WP-03.2에 전달.

### WP-03.2 — Canonical full propagation

- **Prerequisite:** WP-03.1 green, Phase 02 lookup completeness 재확인.
- **Change target:** `ForwardRoutePropagator`, facts/leg/visit/resource types, checked math와 arc resolver, hand/boundary tests.
- **Concrete tasks:** Initial load, per-prefix delta/capacity, directed full arc, window/wait/service, next-work restart, stop/drive/resource, oneway/roundtrip와 operational breakdown 구현.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core \
    -Dtest=RoutePropagationBoundaryTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/test-fixtures -am clean verify
  ```

- **Expected tests:** §9.2 exact table, inclusive/+1 boundary, full-arc, directed asymmetry, plan end와 overflow 판정 green.
- **Failure/rollback:** Partial arc, rounding/default, unchecked arithmetic, raw travel fallback가 하나라도 보이면 package 미완료. WP code를 revert하고 WP-03.1 artifact로 돌아간다.
- **Handoff:** Immutable `RouteFacts`/`ConstraintRejection`와 hand oracle report를 WP-03.3에 전달.

### WP-03.3 — Evaluation SPI와 runtime

- **Prerequisite:** WP-03.2 exact facts green.
- **Change target:** `evaluation.api`, `evaluation.runtime`, contract validator와 layer tests.
- **Concrete tasks:** Stable key/unit/type declaration, neutral metric → composed hard short-circuit → typed score → ordered objective 순서, immutable plan/snapshot/artifact와 duplicate/mismatch/overflow/component-failure rejection 구현.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core \
    -Dtest=RouteEvaluationLayerTest,RouteEvaluationContractCorruptionTest,RouteEvaluationImmutabilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Propagation hard rejection 뒤 evaluation call 0; metric → composed hard 순서와 그 rejection 뒤 score/objective call 0; price-free metric, raw-route-free score, duplicate/unit/fingerprint/overflow/component corruption invalid.
- **Failure/rollback:** Customer name, reflection/script, `Map<String,Object>`, hidden default 또는 finite hard penalty가 필요하면 설계 conflict로 중단하고 Phase 03 review에 반환.
- **Handoff:** Phase 04가 구현체를 bind할 `evaluation.api`와 `EvaluationPlan` contract를 WP-03.4 및 actual-but-unaccepted Phase 04 review에 전달.

### WP-03.4 — Comparator, oracle, property와 cache-free equality

- **Prerequisite:** WP-03.3 green, objective schema 명시.
- **Change target:** Lexicographic comparator, independent oracles, property/metamorphic/cache/reproducibility tests.
- **Concrete tasks:** Ordered direction-aware compare, stable tie, exhaustive BigInteger oracle, monotonic/time-translation/wait-location properties, poisoned cache discard와 repeated/parallel canonical equality 구현.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/test-fixtures -am clean verify
  ```

- **Expected tests:** §9.3의 test-fixtures-owned exact class/method가 Surefire XML에 모두 존재하고 failed/error/skipped 0; seeded-defect sensitivity red report, counterexample 0, full/cache artifact exact equality, repeat/parallel fingerprint exact equality.
- **Failure/rollback:** Tie가 business objective를 덮거나 oracle이 production helper를 공유하면 evidence 폐기. Comparator/cache 최적화만 제거하고 WP-03.3 full path를 last safe point로 유지한다.
- **Handoff:** `E-P03-PROPAGATION`, `E-P03-EVALUATION`, `E-P03-COMPARATOR` 후보 report를 WP-03.5에 전달.

### WP-03.5 — Architecture, bundle, independent review와 downstream handoff

- **Prerequisite:** WP-03.1~4 전체 green, skipped required test 0.
- **Change target:** Architecture rule, evidence manifest, Phase 03 review input. 공용 status는 scheduler만 변경.
- **Concrete tasks:** Forbidden dependency/bytecode 검사, default build, exact command/environment/count/digest, known limitation/gate, rollback point와 downstream compatibility 기록.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/architecture-rules -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/core,build/test-fixtures -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never clean verify
  ```

- **Expected:** OR-Tools-free ALNS-only root build green; required test class/method의 fresh Surefire XML manifest가 exact하고 failed/error/skipped 0; forbidden dependencies/package cycle/customer branches 0; immutable digest-protected bundle과 independent review `PASS`.
- **Failure/rollback:** Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`/handoff authority를 주장하지 않는다. Last accepted predecessor artifact를 유지한다.
- **Handoff:** §13의 Phase 04/05/07 consumers에 exact identities와 compatibility report를 전달한다.

## 11. Verification command와 판정

### 11.1 Layer별 command

| Layer | Future command | 통과 판정 |
|---|---|---|
| Unit/hand/boundary | WP-03.1~3의 selected test command | Exact method 모두 green, failed/error/skipped required test 0 |
| Property/metamorphic | WP-03.4 selected command | Counterexample 0; generator domain/seed-or-ordinal과 oracle digest 기록 |
| Corruption/cache | WP-03.1/3/4 selected command | 각 one-field corruption이 expected `Invalid`; poisoned cache가 full 결과를 바꾸지 않음 |
| Architecture | WP-03.5 architecture command | Forbidden import/dependency/bytecode/customer/vendor reference 0 |
| Module | `./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core,build/test-fixtures -am clean verify` | Core와 independent oracle test module green, reactor cycle 0 |
| Reactor | `./mvnw -B -ntp -Dstyle.color=never clean verify` | OR-Tools-free ALNS-only full reactor green, unrelated required module skip 없음 |

Selected core command는 `-Dsurefire.failIfNoSpecifiedTests=true`를 사용하고 `-am`을 제거해 upstream module의 nonmatching test 때문에 fail-closed 검사를 끄지 않는다. Test-fixtures/architecture는 full module `verify`를 실행한다. 각 command 직전 해당 module report directory를 clean하고, 실행 뒤 §9.3의 exact class/method 이름을 fresh Surefire XML에서 manifest로 대조한다. Missing report/method, duplicate result, failed/error/skipped required test는 exit code가 0이어도 evidence failure다. `-DskipTests`, `-Dmaven.test.skip=true`, 특정 required test disable, stale `target/` report, console summary 한 줄과 이전 run 혼합은 exit evidence가 아니다.

### 11.2 Planned evidence

| Key | 반드시 포함할 내용 | 현재 상태 |
|---|---|---|
| `E-P03-PROPAGATION` | Source/build/problem/travel/route/declaration fingerprints, §9.2 table, boundary/property/metamorphic/corruption/sensitivity report, exact command/toolchain/exit code와 fresh Surefire method manifest | NOT_PRODUCED |
| `E-P03-EVALUATION` | Metric-before-composed-hard call trace, declaration/unit/type closure, failure triage, metric/score/objective artifact, arithmetic/component corruption와 immutability report | NOT_PRODUCED |
| `E-P03-COMPARATOR` | Exhaustive pair/triple oracle, antisymmetry/transitivity/stable-tie sensitivity 결과, cache/full/repeated fingerprint equality | NOT_PRODUCED |

Bundle은 content-addressed immutable artifact 또는 digest-protected local equivalent여야 한다. 구현자가 reviewer/verdict를 자체 합성하거나 이 문서의 표를 pass report로 사용해서는 안 된다.

### 11.3 Security, observability와 safe failure evidence

Phase 03 core는 logger, tracer, clock, environment variable, tenant/customer catalog, raw address/input, provider locator와 credential을 읽지 않는다. Typed result와 evidence report가 관측의 입력이며 외부 telemetry adapter가 안전한 필드만 내보낸다.

허용 가능한 Phase-local 관측 필드는 다음과 같다.

```text
problem/travel/route/propagation/evaluation/build fingerprints
result variant
stable constraint/failure/component code
safe dense route index or redacted subject reference
metric/objective key + unit + non-sensitive aggregate
test fixture ordinal / generator seed
```

Raw external ID, 주소/좌표, full route/input bytes, customer/profile secret, credential/provider locator, arbitrary component exception message와 stack의 입력값은 log/trace/evidence attribute에 넣지 않는다. Fingerprint는 correlation/identity이며 authorization이나 encryption을 대신하지 않는다. Wall-clock duration, thread ID, cache hit order와 completion order는 운영 metadata로만 분리하고 feasibility, objective, tie, canonical bytes 또는 fingerprint의 입력이 될 수 없다.

`E-P03-*`에는 result category별 count, first stable failure code, checked overflow/corruption/sensitivity fixture ordinal, redaction 검사와 full-vs-cache/repeated equality를 넣는다. Core에 telemetry side effect를 추가하거나 failure payload를 최종 사용자 diagnostic으로 직렬화하면 Phase 07/08 책임 침범이다.

## 12. Exit gate, Definition of Done과 anti-pattern

### 12.1 Exit gate

다음 AND 조건을 모두 만족해야 독립 reviewer가 Phase 03 `ACCEPTED`를 권고할 수 있다.

- Entry artifact/fingerprint와 source authority가 exact하게 확인됨.
- §7 internal API/unit/failure triage가 review 승인됨.
- §9.2 hand oracle의 모든 event/load/resource가 exact 일치함.
- Precedence/capacity/time-window/service/travel/stop/resource와 overflow positive/negative/boundary test가 통과함.
- Full-arc restart, directed asymmetry, oneway/roundtrip, wait-in-depot metamorphic test가 통과함.
- Propagation hard rejection 뒤 evaluation component call 0이고, neutral metric → composed hard → score → objective 순서와 composed hard rejection 뒤 score/objective call 0이 확인됨.
- Metric unit/type, score parameter, objective schema/component identity, arithmetic/component failure corruption을 모두 `Invalid`로 거부함.
- Comparator antisymmetry/transitivity/stable total order와 independent oracle가 통과함.
- Cache hit/miss/full, sequential/parallel repeated run의 canonical result/fingerprint가 exact 일치함.
- Core의 cloud/solver/verifier/customer/vendor/test-fixtures 역의존, propagation/evaluation package cycle와 customer-name branch가 0임.
- Security/redaction/safe-failure evidence와 elapsed/thread/cache-order 비의미성이 확인됨.
- Root OR-Tools-free ALNS-only `./mvnw -B -ntp -Dstyle.color=never verify`와 immutable evidence bundle, independent Phase 03 review가 통과함.
- Actual-but-unaccepted Phase 04/05/07 handoff contract와 rollback point가 명시됨.
- OPEN/GATED/deferred/official 미확정 값을 default로 넣지 않음.

### 12.2 Definition of Done

Source 파일이나 test가 존재하는 것만으로 완료되지 않는다. Phase 03의 `ACCEPTED`는 다음을 뜻한다.

1. Phase 02 authority만으로 route physical facts를 처음부터 다시 만들 수 있다.
2. 정상 hard infeasible과 corrupted/overflow state가 typed result로 구분된다.
3. Evaluation layer가 단방향이며 customer/profile/search/finalization 책임을 침범하지 않는다.
4. Independent oracle와 required test가 실제 defect를 검출하고 모두 green이다.
5. Cache가 source of truth가 아니며 reproducibility가 evidence로 확인된다.
6. Phase 04/05가 contract를 재구현하거나 raw input을 읽지 않고 소비할 수 있다.
7. Evidence와 review가 immutable identity로 고정되었다.

### 12.3 금지 anti-pattern

- `double`/epsilon으로 feasibility나 objective를 비교
- Overflow wrap/saturation/clamp, `Long.MAX_VALUE`를 absent/infeasible sentinel로 사용
- Phase 03에서 decimal rounding 또는 official calibration 값 생성
- Missing arc의 reverse copy, 대칭화, 좌표/속도 lazy generation
- Mid-arc pause/resume 또는 work window마다 drive/stop/load reset
- Final load만 보고 prefix capacity를 생략
- Delivery-only logical pickup을 customer stop/travel/service로 계산
- Hard violation을 cost, penalty, temperature, comparator 또는 acceptance로 상쇄
- Metric에 가격/선호, score에 raw route/input parsing, comparator에 physical propagation
- Customer/preset name `if/switch`, classpath first-wins, `latest` fallback
- `Map<String,Object>`, reflection expression 또는 arbitrary script component
- Search cache/summary를 full result로 신뢰하거나 expected oracle로 재사용
- Corrupted route를 ordinary insertion infeasibility/unassignment reason으로 축소
- Phase 04 profile, Phase 05 mutation, Phase 06 ALNS, Phase 07 final diagnostic/verdict를 이 Phase로 당김
- Gated route pool/MIP/vendor API를 core에 선반영

## 13. Blocker, OPEN/GATED/deferred와 restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00~02 accepted evidence 부재 | BLOCKER | Architecture + Domain/Input/Travel | Phase 03 code/test/evidence 착수 | 이 detailed document와 독립 fixture 설계 | Predecessor review/bundle/digest와 actual reactor artifact 전달 |
| Scheduler task/owner 미지정 | BLOCKER | 총괄 scheduler | Authoritative phase status와 review assignment | `scheduler_task_id: TBD_NOT_SUPPLIED` | Exact task ID, implementer/reviewer 역할 지정 |
| Final internal API/type 이름 | PROPOSED/OPEN | Core/Evaluation + Architecture review | Public compatibility와 implementation contract freeze | §7 semantic contract | Phase 03 review가 signature/visibility/package를 승인 |
| Solution-level evaluation owner/contract gap | CROSS-PHASE REVIEW BLOCKER | Core/Evaluation + Capability/Profile + Phase 05 state owner | `RM-2` full solution evaluation, Phase 05 candidate ranking와 comparator handoff | Route-level `RouteEvaluationKernel`만 구현/검증; ad hoc aggregation 금지 | Problem/travel/profile/bank/routes를 받는 exact solution contract, identity/invalidation/failure/comparator owner와 Phase 03~05 reciprocal compile/equality test 승인 |
| Business comparator equality 대 context tie boundary | CROSS-PHASE REVIEW BLOCKER | Core/Evaluation + Phase 05/06 Algorithm | Comparator API freeze와 deterministic option/solution total order | Ordered objective vector와 “non-tied objective 우선” 의미 | Business-equality 관찰 API, solution stable tie와 insertion-context tie의 owner/order/fingerprint를 cross-phase review로 승인 |
| Typed facet SPI | OPEN/PROPOSED | Domain + Capability + Verification | Facet extension만; base propagation/evaluation은 계속 가능 | Facet provider empty list, no customer hook | `ADR-004` 또는 동등 review, Phase 04/07 recomputation evidence |
| `Q-BENCH-02` official steps/workers/rounds/watchdog | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 14 official manifest/baseline/cutover; Phase 03 generic kernel은 안 막음 | 값 없는 exact contract, test-only 명시값만 | Calibration corpus/protocol, measured review, explicit approval |
| Current Win fixture decimal `D/U` | BLOCKER FOR OFFICIAL USE | Input·Matrix + Benchmark | 그 fixture의 official baseline; generic integer Phase 03 fixture는 안 막음 | §9.2 test-only integer fixture | Compliant integer matrix 또는 explicit contract/migration approval |
| `C-17` route pool/MIP | GATED TARGET | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13/production default | Phase 03은 vendor-neutral core만 | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope와 OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback 승인 |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | Optional variant 질문/구현 | Current fixed-terminal single-trip contract | Representative fixture, core-impact feasibility와 별도 승인 |
| Multi-trip/rotation | DEFERRED FEATURE | Product·Domain·Algorithm | Trip/reset/depot 재출발 의미 | Oneway + single roundtrip | Trip/resource/reset/window/pair non-crossing 계약과 승인 |
| Proposed public API/schema/numeric default | OPEN | Product/API/Data | External compatibility 약속 | Internal package-private contract | Versioned contract, compatibility/security review와 approval |

`Q-INFRA-01`은 `RESOLVED`지만 Phase 03에 AWS 구현을 추가하는 근거가 아니다. AWS S3/Step Functions/Lambda 선택은 adapter/distribution phase의 책임이며 core dependency 방향을 바꾸지 않는다.

## 14. Previous/next handoff

### 14.1 Previous — actual document, unaccepted Phase 02

[Phase 02 — Prepared travel/immutable problem](phase-02-prepared-travel-immutable-problem.md)에서 다음을 받아야 한다.

- Immutable `ProblemInstance`, dense external↔internal ID bijection
- Complete `PreparedTravel`, physical-location/vehicle-resolved directed lookup
- Numeric/time/service/compatibility/travel policy와 source fingerprints
- Missing limit의 typed absence, terminal/trip/service-pattern facts
- `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`과 accepted review

Handoff acceptance는 §4 equality/completeness 확인으로 수행한다. Link target과 Phase 02 §13.2의 `ProblemInstanceRef`/`PreparedTravelRef`/`Phase02HandoffManifest`, equality check 및 금지 handoff 목록을 직접 대조했다. Final validation 현재 Phase 02 detailed/review는 actual이고 document review는 `PASS_AFTER_APPLIED_CORRECTIONS`지만 phase acceptance verdict는 `BLOCKED_NOT_IMPLEMENTED`, implementation/evidence는 `NOT_STARTED`/`NOT_AVAILABLE`이므로 Phase 03 implementation을 시작하지 않는다.

### 14.2 Next — actual document, unaccepted Phase 04

[Phase 04 — Capabilities/customer profiles](phase-04-capabilities-customer-profiles.md)는 다음 stable contract를 소비한다.

- `HardConstraint`, `MetricContributor`, `ScoreComponent`, `ObjectiveDimension`, `ObjectiveComparator`
- Typed key/version/parameter/unit/fact dependency declaration
- Immutable `PropagationDeclaration`/`EvaluationPlan`과 fingerprint rules
- Customer-neutral `RouteFacts`/`EvaluationSnapshot`
- Missing/duplicate/unit/type mismatch가 pre-evaluation `Invalid`가 되는 계약

Phase 04는 exact customer/profile/version/preset과 approved capability를 bind하여 위 plan을 만들지만 propagation algorithm을 복제하거나 customer name으로 분기하지 않는다. Facet은 §13 gate가 해제된 경우에만 추가한다. 2026-07-28 inspection에서 link target은 actual이지만 `READY_FOR_REVIEW`/`NOT_STARTED`, evidence/review 없음이므로 accepted handoff는 아니다.

### 14.3 Downstream consumers

| Consumer | 소비할 것 | 소비하면 안 되는 것 | Handoff verification |
|---|---|---|---|
| Phase 05 Pair/Insertion | Pure route kernel, future approved solution-evaluation authority, immutable result, comparator와 exact identities | Kernel 내부 scratch, ad hoc solution aggregation, committed mutation, raw travel/input, invalid result ranking | Same insertion option을 full evaluate했을 때 route + solution artifact exact equality; failure면 pre-state unchanged |
| Phase 07 Candidate verification | Same Phase 02 authority와 cache-free contracts, typed result | Search feasibility flag/cache/summary/final diagnostic | Corrupted cache/route/travel/metric을 독립 fixture로 거부; verifier verdict는 Phase 07 소유 |

Phase 03은 Phase 05를 위해 insertion delta API나 mutable cache를 만들지 않는다. 성능 최적화가 필요하면 full kernel과 exact equality를 기준으로 별도 내부 cache seam을 두고 Phase 05/06에서 수명주기를 소유한다. §13의 solution-evaluation/tie blocker 해제 전에는 Phase 05의 proposed `CandidateEvaluationArtifact` 또는 `BoundInsertionAuthority`를 accepted Phase 03 산출물로 간주하지 않는다.

## 15. Source → requirement → test → evidence traceability

| Requirement | Source | Phase 03 contract | Exact test | Planned evidence |
|---|---|---|---|---|
| `REQ-PAIR` route-local complete pair/precedence | [Master §6](../../2026-07-31-phase-b-master-design.md#6-핵심-불변조건과-atomic-mutation), `Q-REQ-01~02` | Structure gate, delivery-only logical pickup, real pair order | `RouteStructureValidatorTest.*` | `E-P03-PROPAGATION` |
| `REQ-NUMERIC` checked integer/no new rounding | [Master §7.2](../../2026-07-31-phase-b-master-design.md#72-fixed-point와-checked-arithmetic), `Q-NUM-01~03` | Checked math, overflow `Invalid` | `RoutePropagationBoundaryTest.reportsAccumulationOverflowAsInvalid()` | `E-P03-PROPAGATION` |
| `REQ-TIME` plan/window/service/full arc | [Master §7.3](../../2026-07-31-phase-b-master-design.md#73-planning-period와-time), `Q-TIME-01~04`, `Q-IN-01~02` | Explicit window rule, inclusive close, `[start,end)`, full-arc restart | Hand/boundary/metamorphic time methods | `E-P03-PROPAGATION` |
| `REQ-TRAVEL` prepared directed authority | [Master §8](../../2026-07-31-phase-b-master-design.md#8-directed-distancetime-matrix-계약) | Fingerprint equality, directed lookup only, no fallback | `usesDirectedArcWithoutReverseOrSymmetryFallback()`, corruption methods | `E-P03-PROPAGATION` |
| `REQ-LOAD` mixed delivery/real pickup prefix | [Final Domain §9](../../2026-07-26-domain-design.md#9-route-propagation과-resource), `Q-REQ-01` | Initial delivery load, real delta, prefix/final bounds | Hand oracle + load property | `E-P03-PROPAGATION` |
| `REQ-RESOURCE` stop/drive/operational breakdown | [Final Domain §9](../../2026-07-26-domain-design.md#9-route-propagation과-resource), `Q-BENCH-01` | Exact neutral totals and inclusive limits | Stop/final-arc/hand/boundary methods | `E-P03-PROPAGATION` |
| `REQ-EVAL` hard/metric/score/objective separation | [Master §9](../../2026-07-31-phase-b-master-design.md#9-extensible-policy-evaluation과-profile-architecture), `C-04` | Propagation hard → neutral metric → composed hard → score → objective; typed invalid boundary | `RouteEvaluationLayerTest.*` | `E-P03-EVALUATION` |
| `REQ-SOLUTION-EVAL` full route/solution evaluation authority | [Master `RM-2`](../../2026-07-31-phase-b-master-design.md#154-rm-2--propagation-evaluation과-bound-profile), [Plan Phase 03~05](../master-realization-plan.md#phase-03--경로-전파-계산과-평가-kernel) | **Residual blocker:** route-only API를 solution objective로 승격 금지; exact owner/contract review 필요 | Future Phase 03~05 compile/full-equality/corruption suite | `E-P03-EVALUATION` + downstream handoff review |
| `REQ-OBJECTIVE` lexicographic priority | `Q-OBJ-01~03`, [Final Domain §10](../../2026-07-26-domain-design.md#10-evaluation-profile과-objective) | Ordered vector, no Big-M, stable tie after equality | `RouteEvaluationComparatorPropertyTest.*` | `E-P03-COMPARATOR` |
| `REQ-CACHE` full recomputation authority | [Master §12](../../2026-07-31-phase-b-master-design.md#12-candidate-state-cache와-rollback) | Cache hit/miss/full exact artifact equality | `RouteEvaluationCacheEquivalenceTest.*` | `E-P03-EVALUATION`, `E-P03-COMPARATOR` |
| `REQ-REPRO` deterministic kernel | [Master §13](../../2026-07-31-phase-b-master-design.md#13-termination-reproducibility와-execution-provenance) | Stable order/canonical identity, no external state | `sameInputsProduceSameCanonicalResultAcrossRepeatedAndParallelCalls()` | `E-P03-COMPARATOR` |
| `REQ-ARCH-DAG` core/customer/provider/verifier isolation | [Final Architecture §2](../../2026-07-26-architecture-design.md#2-module과-package-경계) | §7.3 dependency direction | `Phase03KernelArchitectureTest.*` | All three keys + architecture report |
| `REQ-SECURITY-OBS` safe failure evidence/no semantic telemetry | [Integrated §19~§20](../../architecture-domain-implementation-design.md#19-configuration-provenance와-observability), [Plan §13](../master-realization-plan.md#13-위험-보안-운영-관측과-재현성) | §11.3 redaction/correlation/elapsed boundary | Failure redaction + architecture/reproducibility tests | All three keys + security report |
| `REQ-ORACLE-SENSITIVITY` independent expected path detects defects | [Integrated §22](../../architecture-domain-implementation-design.md#22-test와-evidence-matrix), [Plan §8](../master-realization-plan.md#8-공통-테스트-전략) | Seeded faulty doubles must produce assertion red | `Phase03OracleSensitivityTest.*` | All three keys |
| `REQ-HANDOFF` Phase 04/05 stable internal contract | [Integrated §7~§9](../../architecture-domain-implementation-design.md#7-phase-3--경로-전파-계산과-평가-kernel) | Immutable API/artifact/fingerprint, no downstream responsibility pull | Compile/architecture + downstream contract suite | Phase 03 review handoff record |

새 요구, rounding, objective dimension, hard rule 또는 facet을 발견하면 이 표에 source/owner/test/evidence를 연결하고 관련 authority/ADR/review를 같은 변경 단위에서 갱신한다. Phase 03 구현 편의를 위해 미확정 의미를 hidden default로 채우지 않는다.
