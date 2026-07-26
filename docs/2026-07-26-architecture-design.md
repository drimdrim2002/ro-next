---
title: RPDPTW Architecture Design
status: REVIEW
version: 2.0-review
last_updated: 2026-07-26
owner: RPDPTW Architecture·Application·Platform 설계 역할
scope: Java 25와 Maven 기반 module/package 경계, local·worker·distributed runtime, optional route-selection backend, 운영·검증·구현 gate
supersedes: architecture-design.md
related_documents:
  - 2026-07-26-master-design.md
  - 2026-07-26-domain-design.md
  - master-design-open-questions.md
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
---

# RPDPTW Architecture Design

## 목차

- [1. 왜 새 경계가 필요한가](#1-왜-새-경계가-필요한가)
  - [1.1 이 문서의 독자와 결정 표기](#11-이-문서의-독자와-결정-표기)
  - [1.2 Current placeholder와 target architecture](#12-current-placeholder와-target-architecture)
  - [1.3 CVRPTW에서 RPDPTW로 갈 때 달라지는 것](#13-cvrptw에서-rpdptw로-갈-때-달라지는-것)
  - [1.4 먼저 보는 전체 그림](#14-먼저-보는-전체-그림)
  - [1.5 범위와 관련 문서](#15-범위와-관련-문서)
- [2. Module과 package 경계](#2-module과-package-경계)
  - [2.1 Maven target tree](#21-maven-target-tree)
  - [2.2 Module DAG와 금지 dependency](#22-module-dag와-금지-dependency)
  - [2.3 Package 책임](#23-package-책임)
  - [2.4 먼저 알아야 할 immutable artifact](#24-먼저-알아야-할-immutable-artifact)
  - [2.5 먼저 알아야 할 interface와 port](#25-먼저-알아야-할-interface와-port)
  - [2.6 Profile과 고객 확장](#26-profile과-고객-확장)
  - [2.7 Build와 architecture enforcement](#27-build와-architecture-enforcement)
- [3. Runtime 경계](#3-runtime-경계)
  - [3.1 공통 실행 파이프라인](#31-공통-실행-파이프라인)
  - [3.2 Local과 worker 실행](#32-local과-worker-실행)
  - [3.3 Outer ExecutionRound·WorkerRun과 inner HybridPhase](#33-outer-executionroundworkerrun과-inner-hybridphase)
  - [3.4 ALNS step budget 계약](#34-alns-step-budget-계약)
  - [3.5 Distributed state와 fan-out·fan-in](#35-distributed-state와-fan-outfan-in)
  - [3.6 Identity, idempotency, retry와 cancellation](#36-identity-idempotency-retry와-cancellation)
- [4. Optional route pool과 MIP backend](#4-optional-route-pool과-mip-backend)
  - [4.1 C-17 gated target](#41-c-17-gated-target)
  - [4.2 Route pool부터 adoption까지](#42-route-pool부터-adoption까지)
  - [4.3 Factory와 session contract](#43-factory와-session-contract)
  - [4.4 Capacity lease와 backend·license 상태](#44-capacity-lease와-backendlicense-상태)
  - [4.5 Gurobi와 native lifecycle](#45-gurobi와-native-lifecycle)
  - [4.6 TIMEBOXED retry와 fallback](#46-timeboxed-retry와-fallback)
  - [4.7 Full materialization과 evaluation 경계](#47-full-materialization과-evaluation-경계)
- [5. 운영, 검증과 publication](#5-운영-검증과-publication)
  - [5.1 Provider-neutral 운영 port](#51-provider-neutral-운영-port)
  - [5.2 Artifact, configuration과 provenance](#52-artifact-configuration과-provenance)
  - [5.3 상태와 failure 계약](#53-상태와-failure-계약)
  - [5.4 Independent verification과 publication](#54-independent-verification과-publication)
  - [5.5 Observability와 security](#55-observability와-security)
  - [5.6 Test와 evidence](#56-test와-evidence)
- [6. 구현 순서와 gate](#6-구현-순서와-gate)
  - [6.1 Reactor build order](#61-reactor-build-order)
  - [6.2 AR·RM phase](#62-arrm-phase)
  - [6.3 Phase dependency와 deferred decision](#63-phase-dependency와-deferred-decision)
  - [6.4 Architecture invariants](#64-architecture-invariants)
  - [6.5 ADR backlog와 상태 보존](#65-adr-backlog와-상태-보존)

## 1. 왜 새 경계가 필요한가

### 1.1 이 문서의 독자와 결정 표기

이 문서는 Java와 Maven을 주로 쓰고 CVRPTW를 구현해 본 개발자가, ro-next의 코드와 pickup & delivery/RPDPTW 용어를 처음 접한다는 전제로 쓴다. [Master Design](2026-07-26-master-design.md)이 “무엇을 어떤 gate로 구현하는가”를, [Domain Design](2026-07-26-domain-design.md)이 “값과 계산의 정확한 의미가 무엇인가”를 소유한다면, 이 문서는 그 책임을 Maven module, Java package, runtime과 adapter에 배치한다.

| 표기 | 의미 |
|---|---|
| **`[CONTRACT]`** | Master/Domain/질문 등록부에서 상속한 현재 계약 |
| **`[RECOMMENDED]`** | 계약을 구현하기 위한 목표 architecture |
| **`[PORTABLE]`** | provider·product와 무관하게 지켜야 할 logical contract |
| **`[OPEN-EXPERIMENT]`** | protocol은 있으나 공식 수치는 아직 없는 항목 |
| **`[DEFERRED]`** | resume evidence와 별도 승인 전에는 선택하지 않을 항목 |

`MUST`, `MUST NOT`, `SHOULD`는 상위 설계의 규범어 의미를 따른다. 이 문서는 `REVIEW` 상태이며 상위 문서의 domain 의미나 질문 상태를 단독으로 바꾸지 않는다.

### 1.2 Current placeholder와 target architecture

먼저 “지금 checkout에 있는 것”과 “이 문서가 설계하는 것”을 섞지 않는다.

| 구분 | Current | Target |
|---|---|---|
| Maven | root `pom.xml` 하나인 단일 project | parent/aggregator 아래의 multi-module reactor |
| Java namespace | `com.ronext.optimizer` | `com.ronext.rpdptw` |
| 계산 | synthetic objective placeholder | normalization → propagation → evaluation → ALNS → independent verification |
| 외부 의존 | root classpath에 Cloud SDK, controller가 client 생성 | core/application port 뒤의 provider adapter |
| 배포 | 현재 GCP 자료가 존재 | `Q-INFRA-01` 승인 전에는 physical topology 미결정 |
| Route selection | 실제 ALNS/MIP 계약의 완료 evidence가 아님 | `C-17`과 `RM-9A~C`를 거치는 optional target |

현재 구조는 다음과 같은 **characterization 대상**이다.

```text
ro-next/
├── pom.xml
├── src/main/java/com/ronext/optimizer/
│   ├── application/AlnsBatchEngine.java
│   └── adapter/in/http/...
├── src/test/java/com/ronext/optimizer/application/...
├── gcp/
└── docs/
```

따라서 현재 endpoint, storage key, workflow와 error behavior는 migration 시 읽고 비교할 수 있지만 목표 contract로 승격하지 않는다. Target migration은 current behavior characterization → 새 semantic core → provider-neutral port → shadow comparison → versioned cutover → rollback evidence 순서를 따른다. Candidate와 result verifier를 통과하기 전에는 placeholder endpoint를 새 정상 result 경로로 바꾸지 않는다.

### 1.3 CVRPTW에서 RPDPTW로 갈 때 달라지는 것

CVRPTW에서는 대개 한 customer visit을 한 insertion/removal 단위로 볼 수 있다. RPDPTW의 request는 pickup과 delivery 두 service visit의 **한 쌍**이다. 이 차이가 단순한 field 추가로 끝나지 않는다.

| CVRPTW 개발자의 익숙한 가정 | RPDPTW에서 필요한 계약 | Architecture 영향 |
|---|---|---|
| 한 visit을 제거·삽입한다 | pickup과 delivery를 atomic request로 이동하고 pickup이 delivery보다 앞서야 한다 | `domain`, insertion evaluator, search state가 request ID를 공통 단위로 사용 |
| route feasibility flag를 search가 보유한다 | pair, load, time, terminal, travel을 최종 route에서 cache 없이 다시 계산한다 | solver와 verifier의 Maven dependency를 물리적으로 차단 |
| 한 incumbent만 있으면 된다 | 여러 ALNS 경로를 immutable evaluated route로 모아 selection할 수 있다 | live candidate와 `RoutePoolSnapshot`을 분리 |
| solver가 고른 route가 곧 solution이다 | projected column을 concrete vehicle route로 materialize하고 full evaluation해야 한다 | backend와 reconstruction/adoption을 분리 |
| timeout은 한 budget이다 | ALNS completed-step과 MIP work/time budget의 의미가 다르다 | `phase2MaxSteps`와 `RouteSelectionBudget`을 분리 |

핵심 경계가 필요한 이유는 세 가지다.

1. **Pickup-delivery atomicity:** pickup만 route에 남거나 서로 다른 route로 갈라진 중간 상태가 stable candidate로 새면 안 된다.
2. **Independent verification:** search cache, incremental delta와 MIP summary가 모두 같은 오류를 공유할 수 있으므로, publication 권위는 cache-free verifier에 있어야 한다.
3. **Route pool/MIP isolation:** optional licensed/native backend는 Java core, default build와 정상 ALNS fallback을 오염시키지 않아야 한다.

### 1.4 먼저 보는 전체 그림

아래 작은 그림이 문서 전체의 지도다. 화살표는 data가 다음 경계로 이동함을 뜻한다.

```text
external input
  → canonical input
  → normalized problem + prepared travel + bound profile
  → immutable SolveSnapshot
  → portfolio + COW ALNS
  → [optional] evaluated route pool → route selection
                   ↓                    ↓
              immutable routes    projected IDs only
                   └──── materialize + full evaluate ────┐
                                                         ↓
  ALNS incumbent ───────────── strict adoption ─→ committed candidate
                                                         ↓
                         cache-free candidate verifier → finalization/audit
                                                         ↓
                                  independent result verifier → publication
```

Module 관계를 먼저 한 줄로 읽으면 다음과 같다.

```text
rpdptw-solver ─────────────→ rpdptw-core ←──────── rpdptw-verification
       ↑                            ↑                         ↑
       └────────────── rpdptw-application ───────────────────┘
                                    ↑
              adapters / profile providers / apps

route-selection-gurobi ─────→ rpdptw-solver + rpdptw-core
                              # verification으로 가는 edge는 없음
```

화살표는 compile dependency가 향하는 방향이다. Runtime callback은 application이 소유한 port를 adapter가 구현하므로 호출 방향이 반대로 보일 수 있다.

### 1.5 범위와 관련 문서

이 문서의 범위는 Java 25/Maven 목표 구조, package와 dependency 규칙, local·worker·distributed logical runtime, optional worker-local ALNS↔route-selection hybrid, provider-neutral port, verification/publication, evidence와 구현 순서다.

다음은 이 문서가 승인하지 않는다.

- production provider/product, physical topology, IaC와 resource sizing
- round/worker 수, `screenMaxSteps`, `phase2MaxSteps`, watchdog의 공식 수치
- multi-trip/rotation 또는 optional variant 활성화
- route pool/MIP의 production 기본 활성화
- optimizer 제품·라이선스 배포와 공식 MIP budget
- canonical wire schema, database schema, public API의 최종 승인

추적 문서는 다음과 같다.

| 문서 | 역할 |
|---|---|
| [Master Design](2026-07-26-master-design.md) | 전체 요구, algorithm, risk와 gate |
| [Domain Design](2026-07-26-domain-design.md) | domain, propagation, evaluation, result의 의미 |
| [질문 등록부](master-design-open-questions.md) | 28개 질문의 상태와 evidence |
| [세션 29](master-design-sessions/29-open-question-interview.md) | 사용자 답변 원문 |
| [세션 30](master-design-sessions/30-open-question-integration.md) | 질문 답변의 Master/register 반영 기록 |
| [세션 31](master-design-sessions/31-domain-design-integration.md) | Domain v2 반영 기록 |

## 2. Module과 package 경계

### 2.1 Maven target tree

**`[RECOMMENDED]`** Maven module은 “독립 배포”, “무거운 외부 dependency 격리”, “compile dependency 차단”, “독립 등록/version lifecycle” 중 하나가 필요할 때만 만든다. Domain의 모든 세부 책임을 module로 쪼개지는 않는다.

```text
ro-next/
├── pom.xml                              # parent + reactor aggregator
├── build/
│   ├── architecture-rules/
│   └── test-fixtures/
├── rpdptw/
│   ├── pom.xml
│   ├── core/                            # rpdptw-core
│   ├── solver/                          # rpdptw-solver
│   ├── verification/                    # rpdptw-verification
│   ├── application/                     # rpdptw-application
│   └── profiles/
│       ├── standard/
│       └── <profile-namespace>/
├── adapters/
│   ├── common/
│   ├── route-selection-gurobi/          # OPTIONAL, C-17 GATED
│   └── <provider>/                      # DEFERRED, Q-INFRA-01 뒤
├── apps/
│   ├── cli/
│   ├── api/
│   └── worker/
├── deployment/                          # DEFERRED
└── docs/
```

Root `pom.xml`은 `packaging=pom` parent/aggregator가 되고 business dependency를 갖지 않는다. Java release 25, dependency/plugin version, Surefire/Failsafe, Enforcer, reproducible archive 정책을 중앙 관리한다. Cloud SDK, customer implementation과 `gurobi.*`는 parent의 공통 dependencies에 넣지 않는다.

### 2.2 Module DAG와 금지 dependency

다음 DAG에서 `A → B`는 “A가 B를 Maven compile dependency로 가진다”는 뜻이다.

```text
rpdptw-core

rpdptw-solver          → rpdptw-core
rpdptw-verification    → rpdptw-core
rpdptw-profile-*       → rpdptw-core

rpdptw-application     → rpdptw-core
rpdptw-application     → rpdptw-solver
rpdptw-application     → rpdptw-verification

route-selection-gurobi → rpdptw-core
route-selection-gurobi → rpdptw-solver

adapters/common        → rpdptw-core
adapters/common        → rpdptw-verification
adapters/common        → rpdptw-application

adapters/<provider>    → rpdptw-application
adapters/<provider>    → adapters/common       # 필요할 때만

apps/*                 → rpdptw-application
apps/*                 → selected profiles/adapters/backend
```

가장 중요한 금지는 다음과 같다.

| From | MUST NOT depend on | 이유 |
|---|---|---|
| `core`, `solver`, `verification` | Cloud/HTTP/provider SDK | stable semantic kernel 보호 |
| `core`, generic `solver`, `verification`, `application` | `gurobi.*` 또는 optimizer vendor API | license-free build와 backend 격리 |
| `core` | solver, verification, application, adapter/app | inward-only dependency |
| `solver` | verification, application, customer implementation, adapter | algorithm과 publication 분리 |
| `verification` | **solver 전체와 search/cache package** | independent verification |
| `profiles/*` | search internal | policy가 algorithm을 직접 조작하지 못하게 함 |
| `application` | provider SDK | portable outbound port 유지 |
| 모든 module | 다른 module의 `.internal` | public contract 우회 금지 |

Candidate verifier가 stateless propagation/evaluation library를 `core`와 공유하는 것은 허용한다. 그러나 solver의 feasibility flag, incremental cache, route summary 또는 `CommittedCandidate` 내부 구현을 권위로 받는 것은 금지한다.

### 2.3 Package 책임

Base namespace는 current placeholder와 구분한 `com.ronext.rpdptw`다.

| Module / package | 책임 | 경계 |
|---|---|---|
| `rpdptw-core / input` | provider-neutral canonical input와 schema/source identity | Jackson/cloud annotation 강제 금지 |
| `rpdptw-core / domain` | dense ID, request pair, node, vehicle, route, problem immutable value | raw DTO/customer name 금지 |
| `rpdptw-core / normalization` | numeric/time/service/location/compatibility normalization | search/price/I/O 금지 |
| `rpdptw-core / travel` | complete directed travel과 fingerprint | runtime lazy fallback 금지 |
| `rpdptw-core / propagation` | load/time/window/travel/stop/resource full propagation | 가격/final reason 금지 |
| `rpdptw-core / evaluation.api` | fact, constraint, metric, score, objective/profile SPI | customer implementation 금지 |
| `rpdptw-core / evaluation.runtime` | exact profile binding, closure, full evaluation/comparator | fallback/classpath-order 의존 금지 |
| `rpdptw-core / evaluation.insertion` | pickup-delivery pair option enumeration/evaluation | candidate mutation 금지 |
| `rpdptw-solver / solver.portfolio` | 4 construction policy × 2 vehicle order candidate | 최대 8개 독립성 보존 |
| `rpdptw-solver / solver.search.*` | ALNS segment, destroy/repair/acceptance/adaptive | customer/provider 분기 금지 |
| `rpdptw-solver / solver.state` | changed-route COW, bank, invalidation, commit/discard | step 밖 mutable alias 금지 |
| `rpdptw-solver / solver.pool` | immutable evaluated route admission/merge/dominance/snapshot | live route mutation 금지 |
| `rpdptw-solver / solver.selection.api` | model, budget, warm start, generic outcome | vendor type 금지 |
| `rpdptw-solver / solver.selection.conversion` | cover-to-partition clone/recompute | pool route mutate 금지 |
| `rpdptw-solver / solver.hybrid` | solver-local hybrid state contract | distributed orchestration 금지 |
| `rpdptw-verification / verification.candidate` | route/bank 전체 cache-free 재계산 | solver dependency 금지 |
| `rpdptw-verification / result.finalization` | outcome, exhaustive insertion audit, diagnostic/summary | 자동 insert/re-solve 금지 |
| `rpdptw-verification / verification.result` | outcome/audit/summary/payload integrity | finalizer 판정 무검증 신뢰 금지 |
| `rpdptw-application / application.execution` | run/round/worker/attempt identity와 state | domain 의미 재해석 금지 |
| `rpdptw-application / application.hybrid` | ALNS/pool/selection/reconstruction/adoption 조립 | vendor API 금지 |
| `route-selection-gurobi / adapter.routeselection.gurobi` | Gurobi model/status/native lifecycle | reconstruction/final verification 금지 |

구현 세부는 `.internal` 또는 package-private로 숨긴다. 새 semantic owner가 필요한데 `util`, `common`, `shared`, `manager`, `helper`로 회피하지 않는다. Customer-specific nullable field나 `Map<String,Object>`를 `Request`, `Vehicle`, `RouteState`에 추가하지 않는다.

### 2.4 먼저 알아야 할 immutable artifact

각 단계는 이전 단계의 mutable 객체를 계속 잡고 있지 않고, 명시적 identity를 가진 immutable artifact를 넘긴다.

| Artifact | Owner | 최소 identity / 의미 |
|---|---|---|
| `CanonicalInput` | input adapter contract | schema, raw digest, adapter version |
| `ProblemInstance` | core domain | dense mapping과 numeric/time/service/compatibility fingerprint |
| `PreparedTravel` | core travel | complete coverage와 mapping/fingerprint |
| `BoundProfile` | evaluation runtime | exact profile/preset/config와 dependency closure |
| `SolveSnapshot` | application preparation | problem + travel + bound profile identities |
| `ExecutionManifest` | application execution | snapshot, build/runtime, config, seed/step/round plan |
| `RoutePlan` | core domain | concrete vehicle, terminal, ordered pickup/delivery visits |
| `CommittedCandidate` | solver | route/bank source of truth와 termination lineage |
| `RoutePoolDelta` | solver pool | admitted/replaced route artifact IDs |
| `RoutePoolSnapshot` | solver pool | stable evaluated routes, pins, policy와 content digest |
| `RouteSelectionModelManifest` | selection API | mode, row/column mapping, objective와 fingerprints |
| `MipWarmStartManifest` | selection API | model identity와 selected/unassigned mapping |
| `RouteSelectionOutcome` | selection API | generic status, incumbent count, selected IDs, evidence |
| `PartitionConversionRecord` | conversion | duplicate decisions, fresh routes와 exact-partition evidence |
| `HybridPhaseRecord` | application hybrid | ALNS/pool/selection/adoption/fallback lineage |
| `VerifiedSolution` | candidate verifier | recomputed facts/metrics/objective와 report |
| `FinalResult` | finalization | outcomes, audit, diagnostic, summary, provenance |
| `PublishableResult` | result verifier | 두 verifier report와 payload fingerprint |

`RouteSelectionOutcome`은 candidate가 아니다. `RoutePoolSnapshot`은 live pool view가 아니다. `FinalResult`도 아직 publishable result가 아니다. Type 경계 자체가 이 차이를 드러내야 한다.

### 2.5 먼저 알아야 할 interface와 port

Interface owner를 먼저 정하면 dependency inversion이 명확해진다.

| Contract | Owner | Implementer / consumer |
|---|---|---|
| `RouteSelectionSolverFactory`, `RouteSelectionSession` | `solver.selection.api` | optional backend가 구현, application hybrid가 소비 |
| `SubmitSolve`, `PrepareSolveSnapshot`, `ExecuteWorkerRun` 등 inbound use case | `application.port.in` | app entrypoint가 호출 |
| `ArtifactStore`, `RunStateRepository`, `WorkerDispatcher` | `application.port.out` | local/provider adapter가 구현 |
| `OptimizerCapacityLeasePort` | `application.port.out` | local bounded lease 또는 distributed capacity adapter가 구현 |
| `ResultPublisher`, `CancellationPort`, `TelemetryPort` | `application.port.out` | adapter가 구현 |
| Profile/constraint/metric/score/objective SPI | `core.evaluation.api` | profile module이 구현 |
| Candidate/result verifier API | `verification` | application이 호출 |

추천 inbound use case는 다음 책임 단위다.

```text
SubmitSolve
PrepareSolveSnapshot
ExecuteWorkerRun
CompleteWorkerRun
SelectRoundChampion
PublishVerifiedResult
RequestCancellation
GetSolveStatus
GetVerifiedResult
```

`ExecuteHybridPhase`는 `ExecuteWorkerRun` 내부 command/state machine이며 별도 public API로 만들지 않는다.

### 2.6 Profile과 고객 확장

새 고객 요구는 가능한 가장 좁은 seam에 둔다.

```text
versioned input mapping
→ static compatibility
→ hard constraint
→ neutral metric
→ score component
→ objective/comparator
→ SolvePlan/profile composition
→ 승인된 typed domain/propagation facet
```

Registry는 `CustomerKey`, `ProfileKey`, `ProfileVersion`, `PresetKey`, `ProfileSchemaVersion`, `ImplementationContractVersion`을 구분한다. `latest`, 비슷한 이름, 다른 고객의 preset, classpath first-wins fallback은 금지한다. 같은 identity의 내용이 바뀌면 startup/build가 실패해야 한다.

Profile binding은 solve 전에 dependency closure, unit/type, parameter range, comparator direction, operator availability, projection capability, request/unassigned/concrete-vehicle/global-resource row mapping, coefficient range/round-trip과 lexicographic priority를 검증해 immutable `BoundProfile`을 만든다. 표현할 수 없는 dimension/constraint는 typed non-projectable 결과가 되어야 하며 production exact mode에서 surrogate나 hidden Big-M으로 조용히 바꾸지 않는다.

다음 customer-name branch는 architecture violation이다.

```java
if (customerId.equals("...")) { ... }
switch (customerName) { ... }
if (presetName.contains("outsourcing")) { ... }
```

### 2.7 Build와 architecture enforcement

License-free 기본 `mvn verify`는 Gurobi 설치나 라이선스 없이 통과해야 한다. 최소한 다음을 자동 검사한다.

1. Enforcer로 core/solver/verification의 cloud·transport dependency를 차단한다.
2. Architecture test로 package direction과 cross-module rule을 검사한다.
3. Reactor DAG에 cycle이 없는지 검사한다.
4. `jdeps` 또는 동등한 bytecode 검사로 transitive SDK 침투를 찾는다.
5. `gurobi.*` reference가 `adapters/route-selection-gurobi` 밖에 0개인지 검사한다.
6. `rpdptw-verification`의 `rpdptw-solver` dependency가 0개인지 검사한다.
7. Test fixture가 production scope로 새지 않는지 검사한다.
8. Default assembly가 route-selection capability를 거짓으로 광고하지 않는지 검사한다.

## 3. Runtime 경계

### 3.1 공통 실행 파이프라인

Local, same-process worker와 향후 distributed worker는 같은 application use case와 검증 gate를 재사용한다.

```text
Submission
→ resolve exact input/schema/profile/config references
→ canonical adapter
→ normalization + travel preparation + profile binding
→ immutable SolveSnapshot
→ initial portfolio
→ phase-1 screen + stable champion
→ phase-2 WorkerRun(s)
→ candidate verification
→ round champion
→ finalization + exhaustive insertion audit
→ result verification
→ publication CAS
→ retrieval
```

Adapter는 raw 입력을 core가 추측하게 하지 않는다. Travel preparation 이후 solver/verifier는 coordinate나 speed로 runtime fallback하지 않는다. Search가 성공해도 두 verifier가 통과하기 전에는 정상 result가 아니다.

### 3.2 Local과 worker 실행

Local runner는 cloud의 축소판이 아니라 **reference execution**이다.

```text
CLI/API input
→ PrepareSolveSnapshot
→ local immutable artifact store
→ ExecuteWorkerRun in same process
   └─ declared HybridPhase loop
→ candidate verify
→ finalization/audit
→ result verify
→ atomic local publication
```

| Port | Local implementation 예 |
|---|---|
| `ArtifactStore` | explicit workspace + atomic rename + digest check |
| `RunStateRepository` | in-memory 또는 append-only file |
| `WorkerDispatcher` | same-process executor |
| `CancellationPort` | cooperative in-memory token |
| `OptimizerCapacityLeasePort` | bounded in-process lease |
| `ResultPublisher` | atomic pointer/CAS |
| `TelemetryPort` | structured log + test recorder |

Local multi-worker test도 `WorkerAssignment`을 stable order로 만들고, completion order와 무관하게 champion을 선택한다. Thread pool size는 algorithm budget도 result identity도 아니다. Wall-clock timeout을 정상 `MAX_STEPS_REACHED`로 바꾸지 않는다.

### 3.3 Outer ExecutionRound·WorkerRun과 inner HybridPhase

가장 자주 혼동하기 쉬운 경계다.

```text
Solve
└─ ExecutionRound[roundOrdinal]                 # outer coordination
   ├─ WorkerRun[workerOrdinal=0]                # outer completeness unit
   │  ├─ HybridPhase[0]                         # inner worker-local phase
   │  │  ├─ ALNS segment(s)
   │  │  └─ optional pool → selection → adoption
   │  ├─ HybridPhase[1]
   │  └─ candidate verification
   ├─ WorkerRun[workerOrdinal=1]
   │  └─ ...
   └─ fan-in → RoundChampion                    # 모든 선언 worker 뒤
```

| 경계 | 소유 책임 | 하지 않는 일 |
|---|---|---|
| `ExecutionRound` | 선언 worker 목록, fan-out/fan-in, completeness, champion | ALNS operator나 native model 관리 |
| `WorkerRun` | seed, common warm start, total ALNS completed-step, final candidate verification | 다른 worker pool 병합 |
| `HybridPhase` | ALNS segment, worker-local pool seal, optional selection, strict adoption/fallback | round completeness나 publication |
| `AlnsRun` | COW step transition과 completed-step | MIP work/time 계수 |
| `RouteSelectionRun` | 한 sealed pool/model/warm start의 backend attempt | ALNS step 소비 |

Cross-worker pool fan-in과 중앙 MIP는 baseline이 아니다. 필요하면 `AR-H3` 뒤 별도 scalability ADR에서 stable merge order, license bottleneck, retry identity를 다시 승인한다.

### 3.4 ALNS step budget 계약

**`[CONTRACT]`** `phase2MaxSteps`는 해당 `WorkerRun`의 모든 `HybridPhase`에서 **정상 완료된 ALNS step 수의 합**만 소비한다.

```text
Σ hybridPhase.alns.completedSteps == workerAssignment.phase2MaxSteps
```

다음은 `phase2MaxSteps`를 소비하지 않는다.

- route pool seal·merge·pruning
- MIP model projection/build
- optimizer capacity lease 대기
- route-selection optimize
- selected-column materialization
- full propagation/evaluation
- candidate/result verification

MIP는 `RouteSelectionBudget`의 deterministic work/node/solution budget과 wall-clock watchdog/reserve를 별도로 가진다. MIP timeout을 ALNS completed-step으로 환산하거나 MIP 재시도로 부족한 ALNS step을 채우지 않는다. ALNS segment가 정상 completed-step을 충족하지 못하면 optional MIP fallback 여부와 무관하게 worker의 공식 step 계약은 미완료다.

### 3.5 Distributed state와 fan-out·fan-in

Outer state는 provider와 무관하게 다음 의미를 보존한다.

```text
Solve / ExecutionRound:
SUBMITTED
→ PREPARING
→ PREPARED
→ ROUND_DISPATCHING
→ ROUND_RUNNING
→ ROUND_VERIFYING
→ ROUND_AGGREGATING
→ next ROUND_DISPATCHING or FINALIZING
→ PUBLISHING
→ SUCCEEDED
```

Inner worker state는 별도다.

```text
WorkerRun:
ASSIGNED
→ WORKER_RUNNING
→ HybridPhase[0..n]
→ WORKER_VERIFYING
→ WORKER_SUCCEEDED | DEGRADED_ALNS_ONLY

HybridPhase:
ALNS_RUNNING
→ POOL_SEALING
├─ ROUTE_SELECTION_SKIPPED
│  ├─ optional → INCUMBENT_RETAINED → HYBRID_HANDOFF
│  └─ required → HYBRID_PHASE_INCOMPLETE
└─ ROUTE_SELECTION_RUNNING
   ├─ NO_INCUMBENT | ROUTE_SELECTION_FAILED
   │  ├─ optional → INCUMBENT_RETAINED → HYBRID_HANDOFF
   │  └─ required → HYBRID_PHASE_INCOMPLETE
   └─ SELECTION_WITH_INCUMBENT
      → RECONSTRUCTING
      ├─ FULL_EVALUATION_FAILED
      │  ├─ optional → INCUMBENT_RETAINED → HYBRID_HANDOFF
      │  └─ required → HYBRID_PHASE_INCOMPLETE
      └─ FULL_EVALUATED
         → INCUMBENT_RETAINED | SELECTOR_CANDIDATE_ADOPTED
         → HYBRID_HANDOFF
```

Exceptional state는 `REJECTED_INPUT`, `BINDING_FAILED`, `CANCEL_REQUESTED`, `CANCELLED`, `WATCHDOG_REACHED`, `RESOURCE_LIMIT_REACHED`, `PLATFORM_TIMEOUT`, `BACKEND_UNAVAILABLE`, `LICENSE_UNAVAILABLE`, `ROUTE_SELECTION_FAILED`, `FAILED`, `INCOMPLETE`, `PUBLICATION_REJECTED`를 정상 termination과 구분한다.

공식 execution에서 선언 worker 하나라도 정상 completion과 cache-free candidate verification을 충족하지 못하면 round 전체는 `INCOMPLETE`다. 성공 worker 일부만으로 champion을 고르지 않는다. 모든 verified worker가 모인 뒤 stable comparator와 deterministic tie-break로 `RoundChampion`을 고른다. 이전 champion보다 strictly better일 때만 다음 round의 common warm start가 된다.

### 3.6 Identity, idempotency, retry와 cancellation

```text
SubmissionId
SolveId
ManifestFingerprint
RoundOrdinal
WorkerOrdinal
WorkerRunId
HybridPhaseOrdinal
AlnsRunOrdinal
RouteSelectionRunId
AttemptId
ArtifactDigest
```

- 같은 submission key에 다른 input/profile/manifest digest가 오면 conflict다.
- `WorkerRunId`는 round/worker/warm-start/config identity에 고정한다.
- Retry는 `AttemptId`만 바꿀 수 있고 seed, warm start, requested ALNS steps는 바꾸지 않는다.
- Duplicate success가 같은 verified digest면 수렴할 수 있다.
- 같은 strong-replay identity에서 다른 verified digest가 나오면 임의 선택하지 않고 integrity violation으로 처리한다.
- Champion과 publication은 CAS로 한 번만 확정한다.
- Completion order는 comparator input order가 아니다.
- Cancellation은 cooperative intent다. 미완료 COW candidate는 discard하고 intent, 실제 종료와 마지막 completed boundary를 따로 기록한다.

## 4. Optional route pool과 MIP backend

### 4.1 C-17 gated target

Route pool/MIP는 목표 architecture에 포함되지만 **production 기본 기능이 아니다**.

```text
ALNS-only baseline
  → C-17 phase-scope approval
  → AR-H1 / RM-9A: immutable evaluated route pool
  → AR-H2 / RM-9B: solver-neutral selection SPI + oracle + optional backend
  → AR-H3 / RM-9C: worker-local hybrid shadow + A/B evidence
  → C-17 별도 production activation decision
```

`C-17` 전에는 다음을 주장할 수 없다.

- Gurobi dependency가 있으므로 hybrid가 구현 완료되었다.
- native library가 설치되었으므로 production에서 사용할 수 있다.
- raw MIP objective가 좋으므로 final result도 더 좋다.
- licensed backend가 없으면 default build가 실패해도 된다.

기본 build/test/image는 license-free다. Gurobi integration test와 assembly는 별도 Maven/CI profile, isolated environment와 license review를 사용한다. Vendor package는 `com.ronext.rpdptw.adapter.routeselection.gurobi`에만 존재한다.

### 4.2 Route pool부터 adoption까지

```text
completed hard-feasible ALNS routes
→ immutable evaluated route artifacts
→ admission / safe dominance / pin / stable order
→ sealed RoutePoolSnapshot
→ exact projection or typed NON_PROJECTABLE
→ RouteSelectionModelManifest + MipWarmStartManifest
→ backend outcome
→ selected projected IDs
→ fresh concrete-route materialization
→ exact partition + full propagation/evaluation
→ strict comparator against ALNS incumbent
→ adopt or retain
```

Pool에는 동일한 problem/travel/profile/evaluation authority로 만든 completed hard-feasible route만 들어간다. Interrupted draft, live mutable route, stale metric, raw backend column은 들어가지 않는다. Dominance는 concrete vehicle, terminal, global resource와 다차원 objective를 보존해야 하며 `(vehicle type, request set)`의 scalar minimum만 남기는 식으로 축약하지 않는다.

### 4.3 Factory와 session contract

`solver.selection.api`가 vendor-neutral contract를 소유한다.

```java
public interface RouteSelectionSolverFactory {
    RouteSelectionSession openSession();
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

| Type | 책임 |
|---|---|
| `RouteSelectionModelSpec` | exact projection, row/column mapping, coefficient range, fingerprint |
| `RoutePoolSnapshot` | stable route artifact ID/order와 incumbent pins |
| `MipWarmStart` | selected projected-column/unassigned mapping과 prechecked feasibility |
| `RouteSelectionBudget` | MIP 전용 work/node/solution/time budget와 required/optional policy |
| `RouteSelectionOutcome` | generic status, incumbent count, selected IDs, bounded backend evidence |
| `SolverCancellationProbe` | provider-neutral read-only cancellation |

Backend는 model build, warm start mapping, optimize, status/incumbent 확인, selected ID extraction과 native cleanup만 소유한다. Cover conversion, concrete reconstruction, full evaluation, adoption과 verification은 backend 밖에 둔다.

### 4.4 Capacity lease와 backend·license 상태

`OptimizerCapacityLeasePort`는 distributed license/session capacity를 application이 명시적으로 관리하기 위한 outbound port다.

```text
acquire backend capability lease
→ open RouteSelectionSession
→ build/optimize/extract
→ close session
→ release lease in finally
```

Backend 내부 process-local semaphore는 여러 worker/process의 license concurrency를 보장하지 못한다. Lease에는 capability key, opaque owner/lease identity와 expiry만 넣고 secret/license value는 넣지 않는다.

상태를 합치지 않는다.

| 상태 | 의미 | Optional plan | Required plan |
|---|---|---|---|
| `BACKEND_UNAVAILABLE` | assembly에 backend capability가 없거나 load할 수 없음 | ALNS incumbent 유지, `DEGRADED_ALNS_ONLY` | worker/phase `INCOMPLETE` |
| `LICENSE_UNAVAILABLE` | backend는 있으나 lease/license 획득 실패 | 정책상 retry 후 ALNS fallback 가능 | retry 소진 뒤 `INCOMPLETE` |
| `NO_INCUMBENT_LIMIT` | optimize는 수행했으나 incumbent 없음 | ALNS incumbent 유지 | `INCOMPLETE` |
| `FEASIBLE_LIMIT` | limit에서 incumbent 존재 | materialize/full-evaluate 후에만 adoption 후보 | 평가 실패 시 `INCOMPLETE` |
| `INFEASIBLE_MODEL` | feasible warm start와 모순될 수 있는 model/integrity defect | 정상 fallback으로 품질 결과화 금지 | failure/investigation |
| `ROUTE_SELECTION_FAILED` | build/numeric/native/backend failure | 오염 없음이 증명된 ALNS fallback만 | `INCOMPLETE` |

Capability가 없는 assembly는 fake MIP success를 만들지 않고 typed unavailable factory를 주입한다.

### 4.5 Gurobi와 native lifecycle

Gurobi adapter는 다음 순서를 지킨다.

1. `openSession()`이 명시적 `AutoCloseable` session을 만든다.
2. 하나의 environment를 여러 solve thread가 공유하지 않는다.
3. 정상·예외·cancel 경로 모두에서 model을 먼저 dispose한다.
4. 소유 environment는 그 environment의 모든 model 뒤에 닫는다.
5. Session close가 끝난 뒤 capacity lease를 반환한다.
6. Status와 incumbent count를 확인하기 전 variable value, objective, bound/gap을 읽지 않는다.
7. Secret/license material은 log, artifact, fingerprint에 넣지 않는다.
8. Native JAR/library 재배포는 별도 license review 없이 허용하지 않는다.

### 4.6 TIMEBOXED retry와 fallback

`STRONG_REPLAY`와 `TIMEBOXED_HYBRID`를 구분한다.

- `STRONG_REPLAY`는 같은 pool/model/warm-start/backend config와 `RouteSelectionRunId`를 보존하고 attempt만 바꿀 수 있다.
- `TIMEBOXED_HYBRID`는 **optimize 시작 전** dispatch/lease failure만 같은 logical run ID로 retry한다.
- Optimize가 시작된 뒤에는 같은 `RouteSelectionRunId`로 결과-bearing retry를 하지 않는다.
- Optimize 시작 뒤 timebox/failure에서 optional plan은 검증 가능한 기존 ALNS incumbent로 fallback한다.
- 같은 경우 required plan은 `INCOMPLETE`다.
- 다시 결과를 얻고 싶다면 새 logical run/manifest를 만든다.

Fallback은 “empty MIP solution을 정상 solution으로 변환”하는 것이 아니다. Optional fallback은 selector가 ALNS incumbent를 변경하지 않았고 pool/model/native failure가 incumbent와 alias되지 않았음을 증명할 때만 허용한다.

### 4.7 Full materialization과 evaluation 경계

Backend가 반환하는 것은 projected column ID다. 다음 단계를 모두 통과하기 전에는 `CommittedCandidate`로 cast할 수 없다.

1. Selected ID가 model/pool fingerprint에 속하는지 확인한다.
2. Cover mode라면 duplicate request 결정을 명시하고 pool route를 mutate하지 않은 fresh route를 만든다.
3. Concrete vehicle, terminal, request ownership과 route/bank exact partition을 구성한다.
4. Prepared travel로 route 전체를 다시 propagation한다.
5. Bound profile로 constraint/metric/score/objective를 full evaluation한다.
6. Invalid, infeasible, non-projectable mismatch는 selector candidate를 폐기한다.
7. Stable comparator에서 ALNS incumbent보다 **strictly better**일 때만 adopt한다.
8. Adopted 또는 retained candidate가 다음 `HybridPhase` warm start가 된다.
9. Worker 끝에서는 별도 candidate verifier가 다시 cache-free 검증한다.

Raw `ObjVal`, selected variable value, backend feasibility flag와 `PartitionConversionRecord`만으로 adoption 또는 publication을 승인하지 않는다.

## 5. 운영, 검증과 publication

### 5.1 Provider-neutral 운영 port

**`[PORTABLE]`** Provider가 정해지기 전에도 다음 logical port를 고정한다.

| Port | 책임 |
|---|---|
| `SubmissionPort` | idempotent solve 접수 |
| `ArtifactStore` | immutable artifact put/get/digest verify |
| `RunStateRepository` | solve/round/worker state와 CAS |
| `WorkerDispatcher` | logical worker dispatch/status/stop intent |
| `WorkflowExecutionPort` | durable top-level execution |
| `CancellationPort` | cancellation intent 기록·전파 |
| `ProfileCatalogPort` | exact approved profile/config snapshot |
| `SecretResolver` | opaque secret handle resolution |
| `OptimizerCapacityLeasePort` | distributed backend/session capacity |
| `ResultPublisher` | both-gate result CAS publication |
| `TelemetryPort` | provider-neutral event/metric/trace |
| `Clock` | lease/elapsed 관측; quality 결정에는 사용하지 않음 |

Port method에 provider URI, workflow event, function context, SDK DTO를 넣지 않는다. 큰 problem/route/pool/result는 orchestrator payload가 아니라 digest가 있는 `ArtifactRef`로 전달한다. `Q-INFRA-01` 승인 전에는 provider, queue, workflow, storage, database, region과 deployment topology를 고르지 않는다.

### 5.2 Artifact, configuration과 provenance

추천 `ArtifactRef`:

```text
artifactKind
schemaVersion
contentDigest
contentLength
mediaType
opaqueLocator
encryptionClassification
createdByRun
```

Digest 검증 전에는 역직렬화하지 않는다. Artifact는 create-once immutable이며 “latest” pointer는 result 자체가 아닌 CAS publication index다. Route-pool delta/snapshot, model manifest, backend outcome, conversion과 phase record는 각각 독립 schema/digest를 가진다. Native model/session/handle은 artifact가 아니다.

Configuration은 다음처럼 분리한다.

| Layer | Manifest/fingerprint |
|---|---|
| semantic policy | problem/snapshot에 필수 |
| customer profile/preset | bound profile에 필수 |
| algorithm config | execution manifest에 필수 |
| route-pool config | hybrid manifest/snapshot에 필수 |
| route-selection config | model/backend/version/seed/thread/budget에 필수 |
| logical execution | round/worker/warm-start/ALNS step에 필수 |
| platform config | deployment/run metadata |
| secret | 값은 fingerprint/log에서 제외 |

`Q-BENCH-02`의 공식 수치가 없을 때 생략값을 hidden default로 채우지 않는다. Official manifest 생성은 승인된 explicit value가 없으면 실패해야 한다.

Publishable provenance는 다음 lineage를 연결한다.

```text
submission/input
→ adapter/schema
→ problem + prepared travel + bound profile
→ build/runtime + execution manifest
→ round/worker/attempt
→ hybrid phase + pool/model/warm start/backend outcome
→ materialization/full evaluation/adoption or fallback
→ committed candidate
→ candidate verifier
→ finalization/audit
→ result verifier
→ published payload digest
```

### 5.3 상태와 failure 계약

| Failure | Retry owner | 정상 result 조건 |
|---|---|---|
| schema/numeric/reference error | 없음 | corrected new submission만 가능 |
| profile/binding error | 없음 | corrected exact config/version만 가능 |
| artifact digest mismatch | 없음, integrity incident | same artifact 재사용 금지 |
| transient artifact/state SDK error | adapter/orchestrator | same logical idempotency key로 성공 후 가능 |
| worker platform start failure | orchestrator | same `WorkerRunId`, new attempt, 완료·검증 후 가능 |
| watchdog/resource/platform timeout | manifest policy | 공식 completion 전에는 불가 |
| pool integrity failure | 자동 retry 금지 | pool을 폐기하고 ALNS incumbent 무오염이 증명된 optional fallback만 |
| backend/license unavailable | application/lease owner | optional fallback 또는 required incomplete |
| `FEASIBLE_LIMIT` + incumbent | hybrid policy | materialize/full-evaluate/strict adoption 뒤 가능 |
| `INFEASIBLE_MODEL` | 자동 성공 변환 금지 | defect 조사 전 불가 |
| conversion/full-evaluation failure | 정상 retry 아님 | raw incumbent 불가; optional ALNS fallback만 |
| candidate verifier `FAIL` | 정상 retry 아님 | 불가 |
| result verifier `FAIL` | 정상 retry 아님 | 불가 |
| publication CAS conflict | publisher | 동일 verified digest면 수렴 가능 |
| cancellation | coordinator | 정상 완료로 표시 불가 |

### 5.4 Independent verification과 publication

검증은 두 gate다.

```text
CommittedCandidate
→ CandidateVerifier
   - route/bank partition
   - pickup-delivery pair/order
   - vehicle/terminal/service pattern
   - travel/load/time/resource
   - metric/score/objective cache-free recomputation
→ VerifiedSolution
→ Finalization
   - ASSIGNED/UNASSIGNED exactly one
   - required exhaustive insertion audit
   - diagnostic + summary
→ FinalResult
→ ResultVerifier
   - outcome/ownership/audit/summary/payload integrity
→ PublishableResult
→ ResultPublisher CAS
```

`rpdptw-verification`은 `rpdptw-solver`를 compile-depend하지 않는다. Candidate verifier는 search cache나 solver summary를 입력 권위로 사용하지 않는다. Finalizer는 feasible insertion을 발견해도 자동 적용하거나 re-solve하지 않고 audit evidence로 남긴다. Result verifier는 candidate verifier를 대신하지 않으며, 두 `PASS`가 모두 없으면 정상 publication, retrieval, benchmark 대상이 아니다.

### 5.5 Observability와 security

구조화 event는 가능한 범위에서 다음 correlation field를 갖는다.

```text
solveId
manifestFingerprint
roundOrdinal
workerOrdinal
workerRunId
hybridPhaseOrdinal
alnsRunOrdinal
routeSelectionRunId
attemptId
problemFingerprint
travelFingerprint
profileFingerprint
routePoolFingerprint
routeSelectionModelFingerprint
termination
candidateVerification
resultVerification
artifactDigest
```

ALNS requested/completed steps와 MIP work/time/nodes는 별도 metric이다. Capacity lease wait, backend/license state, model→environment cleanup, fallback/adoption reason도 기록한다. Elapsed time과 completion order는 quality objective, seed 또는 strong-reproducibility fingerprint의 hidden input이 아니다.

Customer input/result/audit artifact는 tenant access scope와 classification을 가진다. Worker와 orchestrator role을 분리하고 최소 권한을 준다. Secret, raw address/PII와 full input을 log/trace에 남기지 않는다. Artifact digest와 encryption은 서로 다른 통제이므로 하나로 다른 하나를 대체하지 않는다.

### 5.6 Test와 evidence

```text
domain/value unit + property
→ module contract
→ cache-free equivalence + fault injection
→ application port with deterministic fakes
→ local end-to-end
→ gated backend/provider integration
→ shadow/cutover + operational rehearsal
```

| Module/group | 필수 evidence |
|---|---|
| input/domain/travel | schema/alias, decimal rejection, overflow, pair/reference, complete asymmetric travel, fingerprint |
| propagation/evaluation | hand-calculated load/time/window/resource, dependency closure, unit, comparator transitivity |
| portfolio/search | 4×2 construction trace, atomic pair destroy/repair, COW isolation, completed-step, fault/cancel discard |
| route pool | admission, interrupted exclusion, no alias, safe dominance, pin, stable digest, memory growth |
| selection API/conversion | tiny exact oracle, row/column mapping, coefficient round-trip, warm start, non-projectable skip, clone/recompute |
| Gurobi backend | license-free default build, licensed status test, backend/license fault, TIMEBOXED pre/post-optimize retry, cleanup/concurrency |
| hybrid application | ALNS→pool→selection→materialize/full-evaluate→adopt, invalid/worse/no-incumbent fallback |
| candidate verifier | corrupted pair/terminal/travel/metric/objective와 poisoned cache rejection |
| finalization/result verifier | exhaustive audit, no-auto-fix, exactly-one outcome, payload corruption, both-gate block |
| runtime | state transition, idempotency conflict, duplicate worker, retry identity, completeness, cancellation |
| architecture rules | forbidden SDK/vendor/customer dependency, verifier dependency 금지, package layer와 DAG cycle |

Verifier expected result는 search가 만든 정상 candidate만 재사용하지 않는다. Hand calculation, small exhaustive oracle 또는 별도 reference implementation을 사용하고 route/bank partial·duplicate·split, stale cache, fingerprint mismatch, incomplete audit와 payload corruption을 독립적으로 주입한다.

## 6. 구현 순서와 gate

### 6.1 Reactor build order

Maven이 실제 topological order를 계산하지만 review 기준은 다음과 같다.

```text
1. parent + aggregators
2. rpdptw-core
3. test-fixtures
4. rpdptw-solver + rpdptw-verification + profile modules
5. optional route-selection backend
   # C-17 scope와 RM-9B, 별도 licensed profile일 때만
6. rpdptw-application
7. adapters/common
8. apps/cli + apps/api + apps/worker
9. architecture-rules + logical-port/end-to-end verification
10. provider adapter/deployment
    # Q-INFRA-01 승인 뒤
```

Release build는 app packaging만 성공하고 core/verification evidence를 생략하는 fast path를 사용하지 않는다.

### 6.2 AR·RM phase

| Phase | 산출물 | 완료 gate / evidence |
|---|---|---|
| `AR-0 / RM-0` | parent/aggregator, architecture rules, traceability baseline | question count, forbidden value, link, no-cycle 검사 |
| `AR-1 / RM-1` | input/domain/normalization/travel | immutable `ProblemInstance`, complete `PreparedTravel`, numeric/time/pair/matrix evidence |
| `AR-2 / RM-2` | propagation/evaluation + profile registry | immutable `BoundProfile`, dependency/unit/comparator/profile isolation |
| `AR-3 / RM-3` | insertion evaluator + portfolio | 최대 8개 independent candidate, atomic pair와 rollback trace |
| `AR-4 / RM-4` | COW ALNS, termination, cache | fault/cancel isolation, cache-free equality, deterministic rerun |
| `AR-5 / RM-5` | candidate/finalization/result verification | corruption rejection, complete audit/outcome, both-gate publication |
| `AR-6 / RM-8-local` | application, adapters/common, CLI/worker | idempotent local E2E, cancellation, artifact identity, retrieval |
| `AR-7 / RM-6-logical` | provider-neutral multi-round + fake dispatcher | completion-order independence, retry identity, incomplete round rejection |
| `AR-8 / RM-8-cutover` | versioned adapter + logical-port integration | compatibility, shadow, idempotency/cancellation, rollback |
| `AR-9 / RM-6-official` | approved manifest + official workflow | `Q-BENCH-02`, compliant integer travel, all worker completion/verification |
| `AR-10 / RM-7` | COW profiling | evidence/ADR 없이는 apply/undo로 전환하지 않음 |
| `AR-H1 / RM-9A` | immutable evaluated route pool/snapshot | admission/merge/dominance/digest/memory + 별도 scope |
| `AR-H2 / RM-9B` | selection SPI + fake/oracle + optional Gurobi | exact model/warm start/status/fallback/cleanup, license-free default |
| `AR-H3 / RM-9C` | worker-local hybrid + shadow | adopted-only feedback, optional/required fallback, ALNS-only A/B |
| `AR-11 / RM-9-other` | physical topology/variant/multi-trip 후속 | 각 deferred resume evidence와 별도 승인 |

### 6.3 Phase dependency와 deferred decision

- `AR-H1 → AR-H2 → AR-H3` 순서를 지킨다.
- `AR-H3`는 `AR-H2`뿐 아니라 `AR-5`의 independent both-gate verification과 application baseline을 선행조건으로 한다.
- `C-17`은 route-selection production scope/activation gate이며 vendor dependency 추가만으로 통과하지 않는다.
- Provider adapter/deployment는 `Q-INFRA-01` resume evidence와 별도 scope 승인 뒤에 시작한다.
- Official `AR-9`는 `Q-BENCH-02` 승인 수치와 compliant integer `D/U` fixture 없이는 닫지 않는다.
- Apply/undo, optional variant, multi-trip을 앞 phase 편의를 위해 core에 미리 넣지 않는다.
- Cross-worker pool fan-in/central selector는 `AR-H3` 완료와 별개의 scalability ADR이다.

### 6.4 Architecture invariants

1. Core/solver/verification의 cloud SDK dependency는 0이다.
2. Generic core/solver/application의 optimizer vendor dependency는 0이다.
3. Verification의 solver/search/cache dependency는 0이다.
4. Common core의 customer-name conditional은 0이다.
5. Stable candidate는 pickup-delivery pair와 route/bank exact partition을 만족한다.
6. Prepared travel 뒤에는 raw coordinate/speed fallback이 없다.
7. COW candidate는 reject/fail/cancel 시 전체 discard한다.
8. `phase2MaxSteps`는 ALNS completed-step만 센다.
9. Pool에는 same-authority immutable hard-feasible evaluated route만 들어간다.
10. Raw optimizer incumbent는 materialization/full evaluation을 우회하지 않는다.
11. Optional selector failure는 기존 ALNS incumbent를 변경하지 않는다.
12. Required selector failure를 정상 성공으로 바꾸지 않는다.
13. Official round는 모든 선언 worker가 완료·검증되어야 한다.
14. Completion order는 champion에 영향을 주지 않는다.
15. Both verifier `PASS` 없는 result는 publish하지 않는다.
16. Open/deferred value를 hidden default로 채우지 않는다.

### 6.5 ADR backlog와 상태 보존

| ADR | 결정 | 선행 evidence |
|---|---|---|
| `ADR-ARCH-001` | module 이름, parent/aggregator, enforcement | 이 문서 review |
| `ADR-ARCH-002` | canonical serialization/Jackson 격리 | external contract 승인 |
| `ADR-ARCH-003` | ServiceLoader vs generated profile catalog | duplicate/order/reproducibility |
| `ADR-ARCH-004` | typed domain/propagation facet | 실제 고객 seam + verifier 영향 |
| `ADR-ARCH-005` | artifact encoding/digest/CAS | size/retention/security |
| `ADR-ARCH-006` | state/lease/duplicate/publication consistency | fault/idempotency rehearsal |
| `ADR-ARCH-007/012` | physical provider/topology | `Q-INFRA-01 DEFERRED` resume evidence |
| `ADR-ARCH-008` | dispatcher concurrency/retry mapping | `Q-BENCH-02`와 provider 결정 |
| `ADR-ARCH-009` | cancellation/recovery external exposure | product/error contract |
| `ADR-ARCH-010` | provider parity/cutover/rollback | golden manifest + shadow |
| `ADR-ARCH-011` | JPMS | toolchain compatibility |
| `ADR-ARCH-013` | route-selection activation/mode/projection | `C-17`, `RM-9A/B`, oracle/shadow |
| `ADR-ARCH-014` | worker-local vs cross-worker selection | `RM-9C` 뒤 bottleneck/idempotency |

이 문서 작성 뒤에도 질문 상태는 바뀌지 않는다.

```text
RESOLVED 25
OPEN — EXPERIMENT_REQUIRED 1
DEFERRED 2
TOTAL 28
```

- `Q-ALG-01`: `RESOLVED` — 4개 construction policy × 2 vehicle order, phase-1 screen과 phase-2 champion warm start
- `Q-ALG-02`: `RESOLVED — KEEP_COW`
- `Q-BENCH-02`: `OPEN — EXPERIMENT_REQUIRED` — 공식 round/worker/step/watchdog 수치 없음
- `Q-INFRA-01`: `DEFERRED` — logical port만 유지
- `Q-VAR-01`: `DEFERRED`

최종 구현 기준은 특정 cloud나 optimizer에서 실행된다는 사실이 아니다. 같은 immutable semantic snapshot, pickup-delivery atomicity, ALNS step 계약, logical execution identity와 두 verification gate가 local runner, worker, 향후 승인된 provider adapter와 optional backend에서 동일한 verified result 의미를 보존해야 한다.
