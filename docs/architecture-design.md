# RPDPTW Architecture Design

```yaml
status: REVIEW
version: 1.4-review
last_updated: 2026-07-28
owner: RPDPTW Architecture·Application·Platform 설계 역할
scope: Java 25와 Maven 기반 project/module 구조, 의존 방향, 확장 seam, logical execution·integration 경계
supersedes: null
related_documents:
  - master-design.md
  - domain-design.md
  - master-design-open-questions.md
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
```

## 1. 문서 목적, 독자와 결정 표기

### 1.1 목적과 대상 독자

이 문서는 [Master Design](master-design.md)과 [Domain Design](domain-design.md)의 논리 책임을 **Java 25, Maven multi-module project, application port와 배포 가능한 runtime 단위**로 구체화하는 `REVIEW` 상태의 Architecture Design이다.

주 독자는 다음과 같다.

- Maven reactor와 Java package를 처음 구성하는 개발자
- Domain, evaluation, search, verification과 result module을 구현하는 개발자
- 고객사별 rule, metric, score, objective와 input/output adapter를 추가하는 개발자
- Local runner, API, worker와 multi-round coordinator를 구현하는 개발자
- logical port, local runner와 향후 infrastructure adapter의 경계를 구현하는 platform 개발자
- Dependency, reproducibility, publication gate와 배포 변경을 검토하는 reviewer

이 문서를 읽은 개발자는 최소한 다음에 답할 수 있어야 한다.

1. 어떤 Maven module이 어떤 책임과 데이터를 소유하는가?
2. 어떤 dependency는 허용되고 어떤 dependency는 build에서 차단해야 하는가?
3. 고객 요구를 common core 수정 없이 어느 extension seam에 추가하는가?
4. Local single-run과 distributed multi-round가 같은 core contract를 어떻게 재사용하는가?
5. logical port가 선택된 AWS topology와 미래 provider 대체를 어떻게 분리하는가?
6. 어떤 순서로 구현하고 어떤 evidence가 있어야 다음 phase로 갈 수 있는가?

### 1.2 결정 종류

이 문서는 확정 계약, 추천 구조, 배포 reference와 미결정을 다음처럼 구분한다.

| 표기 | 의미 | 변경 권위 |
|---|---|---|
| **`[CONTRACT]`** | Master/Domain/질문 등록부에서 상속한 현재 계약. 이 문서가 새로 만든 결정이 아님 | 원문 결정과 Master §1 절차 |
| **`[RECOMMENDED]`** | 위 계약을 구현하기 위한 이 문서의 구체 project architecture 제안 | Architecture review와 ADR 승인 |
| **`[PORTABLE]`** | Provider/product에 독립적으로 유지해야 하는 logical contract | Application architecture와 compatibility test |
| **`[OPEN-EXPERIMENT]`** | Protocol은 정해졌지만 공식 수치가 없는 항목 | 질문 등록부의 실험·승인 gate |
| **`[DEFERRED]`** | 현재 활성화하거나 해결하지 않는 항목 | 질문 등록부의 resume gate |

표기가 없는 설명은 가장 가까운 상위 절의 표기를 따른다. `MUST`, `MUST NOT`, `SHOULD`는 Master §1의 규범어 의미를 사용한다.

### 1.3 범위와 비범위

이 문서의 범위는 다음이다.

- Java 25/Maven multi-module 목표 tree와 build order
- Module별 책임, public contract와 dependency 방향
- Java package/namespace와 dependency enforcement 규칙
- Stable core, volatile policy, adapter, runtime와 infrastructure 분리
- 고객 profile/preset과 rule/metric/score/objective 조립·등록·검증
- Local single-run과 distributed multi-round application flow
- Provider-neutral port, idempotency, retry, cancellation과 result publication
- 선택된 AWS S3 + Step Functions + Lambda integration boundary와 미래 provider substitution boundary
- Configuration, secret, artifact, result, provenance와 observability
- Optional worker-local ALNS→route pool→route-selection hybrid와 optimizer backend isolation
- Module별 test/evidence와 구현 phase/gate

다음은 비범위다.

- 이 문서만으로 production infrastructure를 승인·활성화하는 일
- AWS resource 이름, IAM/IaC, network·retention·cost/resource sizing과 production activation
- 비용·성능 evidence 없는 compute/runtime 선택과 resource sizing
- Round/worker 수, worker별 `maxSteps`, watchdog의 공식 수치
- Multi-trip/rotation과 optional variant 활성화
- Route pool/MIP의 production 기본 활성화, optimizer 제품·라이선스 배포 승인과 공식 solver budget
- Canonical wire schema, database schema와 public API의 최종 승인
- 현재 code/build/deploy file을 이 목표 구조로 이미 migration했다고 주장하는 일

### 1.4 다른 설계 문서와의 관계

| 문서 | 소유하는 질문 | 이 문서가 하는 일 |
|---|---|---|
| [Master Design](master-design.md) | 무엇을 어떤 책임과 gate로 구현하는가 | 논리 컴포넌트를 Maven module/application runtime으로 배치 |
| [Domain Design](domain-design.md) | Domain 값과 propagation/evaluation/result가 정확히 무엇을 뜻하는가 | 그 의미를 깨지 않는 package, module과 extension seam 지정 |
| [질문 등록부](master-design-open-questions.md) | 28개 질문의 상태·결정·evidence | `Q-INFRA-01`의 AWS target 선택과 남은 experiment/deferred 상태를 보존 |
| [세션 29](master-design-sessions/29-open-question-interview.md) | 사용자 답변 원문 | logical multi-round와 historical deferred infrastructure 의도 추적 |
| [세션 30](master-design-sessions/30-open-question-integration.md) | Master/register 반영 기록 | 수치·historical infrastructure 확대 해석 방지 |
| [세션 31](master-design-sessions/31-domain-design-integration.md) | Domain v2 반영 기록 | Legacy 의미를 목표 architecture로 재도입하지 않도록 검증 |

충돌 시 이 문서가 Master나 Domain의 의미를 바꾸지 않는다. 현재 모든 상위 설계는 `REVIEW`이며 Master §1의 authority와 변경 절차를 따른다.

현재 tracked Java path는 실제 ALNS/MIP가 아니라 synthetic objective placeholder다. 아래 ALNS, pool, route selection과 Google OR-Tools CP-SAT backend는 모두 **TARGET / NOT IMPLEMENTED**이며 current module이나 배포가 그 계약을 만족한다고 읽어서는 안 된다. CP-SAT backend 정책 선택, `C-17` activation, 구현 acceptance와 production 활성화는 서로 별도 결정이다.

## 2. Current-state inventory와 migration 기준

### 2.1 조사한 현재 구조

현재 repository는 다음 특성을 가진 단일 Maven project다.

```text
ro-next/
├── pom.xml
├── src/main/java/com/ronext/optimizer/
│   ├── application/AlnsBatchEngine.java
│   └── adapter/in/http/...
├── src/test/java/com/ronext/optimizer/application/...
├── gcp/
│   ├── cloudbuild.yaml
│   └── workflows/optimization.yaml
└── docs/
```

- Root `pom.xml`은 Java compiler release 25와 Maven/Java Enforcer를 이미 사용한다.
- Google Cloud Workflows와 Cloud Storage SDK dependency가 root application classpath에 직접 있다.
- HTTP controller가 Cloud Storage와 Workflow client를 직접 생성한다.
- `gcp/`에는 Cloud Run/Workflows를 전제로 한 build/deployment 자료가 있다.
- 현재 source는 목표 Master/Domain contract의 구현 완료 evidence가 아니다.

### 2.2 Migration 해석

**`[CONTRACT]`** 현재 code, root dependency와 GCP deployment는 characterization할 legacy/current-state inventory다. AWS S3 + Step Functions + Lambda가 선택된 target/reference runtime이며, GCP path는 목표 topology나 `Q-INFRA-01` 해결 근거로 승격하지 않는다.

**`[RECOMMENDED]`** Migration은 다음 원칙을 따른다.

1. 현재 endpoint, payload, storage key, workflow와 error behavior를 read-only characterization한다.
2. 새 core module을 기존 `com.ronext.optimizer` package와 분리된 namespace에서 만든다.
3. 기존 provider 호출은 provider-neutral application port 뒤의 legacy adapter로 격리한다.
4. Core/result end-to-end가 두 verifier를 통과하기 전에는 기존 endpoint를 새 정상 result 경로로 바꾸지 않는다.
5. Shadow comparison, versioned cutover와 rollback evidence 뒤에만 기존 경로를 교체한다.

이 문서 작업은 source, test, build/deploy file 또는 `data/`를 변경하지 않는다.

## 3. Architecture principles

### 3.1 Stable core와 volatile edge

**`[CONTRACT]`** 변경 빈도와 실패 영향에 따라 책임을 다음 네 영역으로 분리한다.

```text
stable semantic core
  domain → prepared travel → propagation → evaluation contracts
       → search → independent verification → result integrity

volatile business composition
  customer profile/preset → constraint/metric/score/objective/SolvePlan

volatile interface edge
  versioned input/output adapter → API/CLI/worker transport

volatile platform edge
  workflow/compute/state/artifact/secret/telemetry provider adapter
```

의존은 바깥에서 안쪽으로만 향한다. 변경 빈도가 높은 고객사 policy나 cloud product가 stable semantic core의 type과 dependency를 결정해서는 안 된다.

### 3.2 핵심 원칙

1. **Semantic ownership:** 한 의미에는 한 owner module만 둔다. 다른 module은 owner의 immutable contract를 소비한다.
2. **Dependency inversion:** Application이 logical port를 소유하고 local 또는 향후 승인된 infrastructure adapter가 이를 구현한다.
3. **No reinterpretation:** Adapter, search, verifier와 publisher가 앞 단계 의미를 다시 추정하거나 default로 보완하지 않는다.
4. **Immutable handoff:** Canonical input 이후 problem, prepared travel, bound profile, manifest와 verified result는 단계 사이에서 immutable snapshot으로 전달한다.
5. **Customer isolation:** Customer name, price label과 objective key를 common propagator/search 분기로 사용하지 않는다.
6. **Provider isolation:** Provider SDK, locator, runtime event와 storage DTO를 core/application contract에 넣지 않는다.
7. **Independent verification:** Search cache와 solver summary가 verifier의 authority가 되지 않는다.
8. **Evidence before optimization:** COW가 기본이며 apply/undo와 runtime 선택은 측정 evidence 뒤에 재검토한다.
9. **Explicit versioning:** Schema, policy, profile, algorithm, build와 artifact는 exact version/fingerprint를 가진다. `latest` fallback을 금지한다.
10. **References over payloads:** Orchestrator는 큰 problem/route/result payload 대신 digest가 있는 immutable artifact reference를 전달한다.

## 4. Logical boundaries와 end-to-end flow

### 4.1 주요 경계

**`[CONTRACT]`** 다음 경계를 합치지 않는다.

| 경계 | 입력 | 출력 | 금지 |
|---|---|---|---|
| Versioned adapter | External bytes/reference + schema identity | Canonical business input + raw provenance | Core 직접 deserialization, silent alias |
| Normalization | Canonical business input | Immutable normalized domain facts | 가격, search, provider SDK |
| Travel preparation | Raw travel/coordinates + normalized location/vehicle | Complete prepared directed travel | Runtime lazy fallback |
| Profile binding | Exact customer/profile/preset/config | Immutable bound profile/plan | Other-customer/unknown fallback |
| Propagation | Route sequence + problem + prepared travel | Physical facts + hard feasibility | 가격, final diagnostic |
| Evaluation | Propagation facts + bound policy | Neutral metrics, score, objective vector | Raw input 재해석 |
| Search | Immutable solve snapshot + explicit run config | Committed candidate + termination lineage | Customer/cloud branch |
| Route collection/pool | Completed hard-feasible route + exact evaluation | Immutable evaluated-artifact delta/snapshot | Candidate commit, pool route mutation |
| Route-selection projection | Bound profile + pool schema | Exact model spec or typed non-projectable result | Hidden Big-M/surrogate |
| Optimizer backend | Model spec + projected columns + warm start + budget | Provider-neutral selected-column outcome | Stable solution/final feasibility 판정 |
| Reconstruction/adoption | Selected projected columns + ALNS incumbent | Fresh materialized/full-evaluated candidate or fallback | Raw backend objective 채택 |
| Candidate verification | Candidate authority inputs | Cache-free `PASS` + verified solution | Search cache 신뢰 |
| Finalization | Verified solution | Outcome, audit, diagnostic, summary | Bank 직렬화, 자동 재탐색 |
| Result verification | Candidate `PASS` + final artifacts | Result-integrity `PASS` + payload fingerprint | Candidate verifier 대체 |
| Publication | Both `PASS` + immutable lineage | Publishable result reference | 미검증 payload 노출 |
| Application orchestration | Artifact/config refs + logical run identity | Durable run/round/worker state | Domain 의미 재정의 |

### 4.2 Main flow

```text
Submission
  → resolve external input reference and exact schema/profile/config identity
  → versioned adapter
  → canonical business input
  → normalization
  → travel preparation
  → exact profile/preset binding
  → immutable SolveSnapshot artifact
  → initial portfolio
  → phase-1 per-candidate COW ALNS screen
  → stable phase-1 champion
  → phase-2 worker batches
      → ALNS segment(s)
      → [approved optional hybrid]
          → validated immutable route pool
          → route-selection backend
          → selected projected-column materialization + full evaluation
          → ALNS incumbent adoption/fallback
  → committed candidate
  → candidate solution verifier
  → finalization + required insertion audit
  → result-integrity verifier
  → verified result artifact
  → publication compare-and-set
  → retrieval
```

Multi-round execution은 `SolveSnapshot` 뒤의 worker execution을 반복한다. Worker-local inner hybrid가 reference-adaptation baseline이고, distributed outer `ExecutionRound`와 `HybridPhase` identity를 분리한다. 각 worker candidate는 cache-free candidate verification을 통과해야 fan-in에 참여할 수 있고, 최종 champion만 finalization과 result-integrity verification을 거쳐 publishable result가 된다. Cross-worker pool fan-in과 중앙 MIP는 별도 scalability ADR 전에는 기본 flow가 아니다.

### 4.3 주요 immutable artifact

| Artifact | Contract owner | Identity 최소 구성 |
|---|---|---|
| `CanonicalInput` | Input contract/adapter | Schema version, raw digest, adapter version |
| `ProblemInstance` | Normalization/domain | Dense mappings, numeric/time/service/compatibility fingerprints |
| `PreparedTravel` | Travel preparation | Coverage, source policy, location/vehicle mapping, fingerprint |
| `BoundProfile` | Evaluation runtime | Profile/preset/config and dependency closure fingerprint |
| `SolveSnapshot` | Application preparation | Problem + travel + bound profile identities |
| `ExecutionManifest` | Application orchestration | Snapshot, build/runtime, algorithm/config, seeds/steps, logical round plan |
| `RoutePlan` | `rpdptw-core` domain contract | Concrete vehicle, terminal, ordered service visits와 structural fingerprint |
| `CommittedCandidate` | Search | Route/bank source of truth, termination and trace identity |
| `RoutePoolDelta` | `solver.pool` | Source trial/step, admitted/replaced route artifact IDs와 authority fingerprints |
| `RoutePoolSnapshot` | `solver.pool` | Stable evaluated routes, incumbent pins, merge/pruning policy와 content digest |
| `RouteSelectionModelManifest` | `solver.selection.api` | Mode, request/vehicle mapping, projected column IDs/objective dimensions, pool/config fingerprints |
| `MipWarmStartManifest` | `solver.selection.api` | Model fingerprint, selected projected-column/unassigned IDs, feasibility disposition와 incumbent lineage |
| `RouteSelectionOutcome` | `solver.selection.api` | Generic status/termination/provenance, incumbent presence와 selected route IDs only |
| `PartitionConversionRecord` | `solver.selection.conversion` | Duplicate decisions, fresh route IDs, recomputation and exact-partition evidence |
| `HybridPhaseRecord` | `application.hybrid` | ALNS, pool, selection, reconstruction, adoption/fallback와 next warm-start lineage |
| `VerifiedSolution` | Candidate verifier | Recomputed facts/metrics/objective + verifier report |
| `FinalResult` | Finalization | Outcomes, audit, diagnostics, summary and provenance |
| `PublishableResult` | Result verifier/publication | Both verifier reports, canonical result/payload fingerprint |

## 5. Recommended Maven multi-module tree

### 5.1 Target directory tree

**`[RECOMMENDED]`** Maven module은 논리 컴포넌트 수만큼 만들지 않는다. 다음 중 하나가 필요한 경우에만 module 경계를 둔다.

1. 독립 배포 artifact가 필요하다.
2. Cloud SDK처럼 무겁고 교체 가능한 외부 dependency를 격리해야 한다.
3. Search와 verifier처럼 compile dependency를 물리적으로 차단해야 한다.
4. 고객 profile처럼 독립 등록·version·배포 lifecycle이 필요하다.

그 밖의 domain/normalization/travel/propagation/evaluation 세부 책임은 `rpdptw-core` 안의 Java package로 구분한다. 목표 directory tree는 다음을 기준으로 한다.

```text
ro-next/
├── pom.xml                               # reactor aggregator + parent
├── build/
│   ├── architecture-rules/              # reactor 뒤에서 실행할 forbidden dependency/package tests
│   └── test-fixtures/                    # core contract 뒤 빌드할 test-only builders
├── rpdptw/
│   ├── pom.xml                            # semantic/application aggregator
│   ├── core/                              # artifactId: rpdptw-core
│   │   └── src/main/java/com/ronext/rpdptw/
│   │       ├── input/
│   │       ├── domain/
│   │       ├── normalization/
│   │       ├── travel/
│   │       ├── propagation/
│   │       └── evaluation/
│   │           ├── api/
│   │           ├── runtime/
│   │           └── insertion/
│   ├── solver/                            # artifactId: rpdptw-solver
│   │   └── src/main/java/com/ronext/rpdptw/solver/
│   │       ├── portfolio/
│   │       ├── search/
│   │       │   ├── destroy/
│   │       │   ├── repair/
│   │       │   ├── acceptance/
│   │       │   └── adaptive/
│   │       ├── state/
│   │       ├── pool/
│   │       ├── selection/
│   │       │   ├── api/
│   │       │   └── conversion/
│   │       ├── hybrid/
│   │       └── termination/
│   ├── verification/                      # artifactId: rpdptw-verification
│   │   └── src/main/java/com/ronext/rpdptw/
│   │       ├── verification/api/
│   │       ├── verification/candidate/
│   │       ├── result/api/
│   │       ├── result/finalization/
│   │       └── verification/result/
│   ├── application/                       # artifactId: rpdptw-application
│   │   └── src/main/java/com/ronext/rpdptw/application/
│   │       ├── port/in/
│   │       ├── port/out/
│   │       ├── service/
│   │       ├── execution/
│   │       └── hybrid/
│   └── profiles/
│       ├── pom.xml
│       ├── standard/                      # artifactId: rpdptw-profile-standard
│       └── <profile-namespace>/           # 독립 customer/profile extension JAR
├── adapters/
│   ├── pom.xml
│   ├── common/                            # artifactId: rpdptw-adapter-common
│   │   └── src/main/java/com/ronext/rpdptw/adapter/
│   │       ├── json/
│   │       └── local/
│   ├── route-selection-ortools-cpsat/      # OPTIONAL/GATED: artifactId rpdptw-route-selection-ortools-cpsat
│   │   └── src/main/java/com/ronext/rpdptw/adapter/routeselection/ortools/cpsat/
│   ├── object-s3/                          # selected AWS target adapter
│   ├── workflow-aws-stepfunctions/         # selected AWS target adapter
│   ├── compute-aws-lambda/                 # selected AWS target adapter
│   └── <provider>/                         # future substitution adapter only
│       └── src/main/java/com/ronext/rpdptw/adapter/<provider>/
│           ├── artifact/
│           ├── state/
│           ├── workflow/
│           ├── compute/
│           └── telemetry/
├── apps/
│   ├── pom.xml
│   ├── cli/                               # deployable artifactId: rpdptw-cli
│   ├── api/                               # deployable artifactId: rpdptw-api
│   └── worker/                            # deployable artifactId: rpdptw-worker
├── deployment/
│   └── aws/                               # selected target assembly; IaC/resource sizing remains separate
└── docs/
```

Maven leaf module은 `core`, `solver`, `verification`, `application`, 각 profile JAR, optional optimizer backend, provider adapter와 deployable app이다. `route-selection-ortools-cpsat`는 cloud provider adapter가 아니라 algorithm backend이며 `C-17`/`RM-9B` gate 전에는 기본 reactor assembly나 production image에 포함하지 않는다. 각 leaf는 일반적으로 다음 shape를 사용한다.

```text
<module>/
├── pom.xml
├── src/main/java/
├── src/main/resources/
└── src/test/java/
```

Integration test는 같은 module의 `src/test/java`에서 `*IT` suffix로 분리하고 Maven Failsafe가 실행한다. `rpdptw-core` 내부 package 경계는 package-private visibility와 architecture test로 강제한다. Cross-module/logical-port contract test는 `build/architecture-rules` 또는 해당 adapter module이 소유한다.

### 5.2 Root parent 책임

Root `pom.xml`은 business dependency를 소유하지 않는 `packaging=pom` parent/aggregator로 전환하는 것을 권장한다.

- Java release 25와 current Maven/Java Enforcer 범위를 중앙 관리한다.
- Dependency version은 `<dependencyManagement>`, plugin version은 `<pluginManagement>`에서 고정한다.
- Compiler, Surefire, Failsafe, Enforcer, reproducible archive와 test report 정책을 중앙화한다.
- Cloud SDK는 parent의 공통 `<dependencies>`에 넣지 않는다.
- Customer profile module은 `rpdptw-core`의 compile dependency가 아니라 app assembly에서 선택한다.
- `mvn verify`가 architecture rule, unit, property, integration과 logical-port contract test를 순서대로 실행하게 한다.

현재 root POM의 Java 25와 Maven Enforcer 설정은 유지 가능한 current-state input이지만 Google Cloud dependency와 shaded application main은 목표 root parent에서 provider/app module로 이동해야 한다.

## 6. Module responsibility와 package boundary

### 6.1 `rpdptw-core`

`rpdptw-core`는 외부 SDK가 없는 stable semantic kernel이다. 내부 package별 책임은 다음과 같다.

| Package | 책임 | MUST NOT |
|---|---|---|
| `input` | Provider-neutral canonical business input, schema/source identity | Jackson/cloud event annotation 강제 |
| `domain` | Dense ID, immutable request/node/vehicle/location/problem와 invariant vocabulary | Raw DTO, customer name, cloud/storage |
| `normalization` | Numeric/time/service/location/compatibility normalization | Search, price, storage I/O |
| `travel` | Complete directed travel preparation와 fingerprint | Runtime lazy fallback, provider routing SDK |
| `propagation` | Load/time/window/travel/stop/resource의 stateless full propagation | Price, customer/preset 분기, final reason |
| `evaluation.api` | Typed fact, constraint/metric/score/objective/profile SPI | Customer implementation, mutable run state |
| `evaluation.runtime` | Exact profile binding, dependency closure, full evaluation/comparator/plan | Customer fallback, classpath order 의존 |
| `evaluation.insertion` | Side-effect-free pair option enumeration/evaluation | Candidate mutation, search acceptance |

`evaluation.api`에 물리 fact contract를 두는 이유는 propagation과 policy SPI 사이의 package cycle을 막기 위해서다. `propagation`은 fact를 만들고 policy는 fact를 소비하며 `evaluation.runtime`이 둘을 조립한다.

Core 내부에서도 모든 package를 public API로 만들지 않는다. External adapter/app이 사용할 canonical input, `ProblemInstance`, prepared/bound snapshot과 evaluation SPI만 의도적으로 export하고 normalization/propagation 구현 세부는 package-private 또는 `.internal`로 숨긴다.

### 6.2 `rpdptw-solver`

| Package | 책임 |
|---|---|
| `solver.portfolio` | 4개 construction policy × 2 vehicle order의 independent candidate, validation과 phase-1 champion selection |
| `solver.search` | ALNS engine/segment, stage guard와 completed-step state transition |
| `solver.search.destroy` | Request-ID removal proposal와 related/worst/history operator registry |
| `solver.search.repair` | Promising shortlist, exact pair insertion과 vehicle/new-route option |
| `solver.search.acceptance` | Stage-local acceptance와 step-based cooling |
| `solver.search.adaptive` | Operator outcome/reward/probability immutable snapshot |
| `solver.state` | Changed-route COW, bank, cache invalidation, commit/discard |
| `solver.pool` | Immutable evaluated route artifact, append/import merge, dominance, pin과 stable snapshot |
| `solver.selection.api` | Model spec, projection capability, budget, warm start와 provider-neutral outcome |
| `solver.selection.conversion` | Optional cover-to-partition clone/recompute record |
| `solver.hybrid` | Solver-local ALNS/pool/selection 상태 machine contract; application이 orchestration |
| `solver.termination` | Step counter, watchdog/cancellation/resource/failure separation |

Solver는 `rpdptw-core`만 의존하고 verifier, application, adapter, optimizer vendor 또는 customer profile implementation에는 의존하지 않는다. 공용 immutable `RoutePlan`, assignment partition과 evaluation declaration은 core contract에 두고 mutable COW만 solver 내부에 둔다. 그래야 verifier와 route-selection backend가 solver internals를 참조하지 않는다.

### 6.3 `rpdptw-verification`

| Package | 책임 | 의존 금지 |
|---|---|---|
| `verification.api` | `VerifiedSolution`, verifier report, failure category/version | Solver implementation/cache |
| `verification.candidate` | Problem/profile/route/bank/prepared travel에서 cache-free 전체 재계산 | Search feasibility flag, solver summary |
| `result.api` | `ASSIGNED/UNASSIGNED`, diagnostic, audit, summary와 publishable contract | Storage DTO |
| `result.finalization` | Partition, exhaustive insertion audit, outcome/diagnostic/summary | Automatic insert/re-solve |
| `verification.result` | Outcome/audit/summary/payload identity의 독립 검증 | Finalizer 판정의 무검증 신뢰 |

이 module은 `rpdptw-core`에만 의존하고 `rpdptw-solver`에는 의존하지 않는다. Candidate verifier와 solver는 core의 stateless propagation/evaluation library를 공유할 수 있지만 verifier가 route 전체를 독립 traversal하고 search cache/summary를 받지 않아야 한다. Result verifier는 candidate `PASS`를 입력으로 받되 candidate verifier 역할을 대신하지 않는다.

### 6.4 `rpdptw-application`

| Package | 책임 |
|---|---|
| `application.port.in` | Submit, prepare, execute, aggregate, publish, cancel, retrieve use case |
| `application.port.out` | Artifact, state, worker dispatch, workflow, cancellation, telemetry port |
| `application.service` | Core/solver/verifier use case 조립 |
| `application.execution` | Run/round/worker/hybrid-phase identity, state transition, idempotency와 manifest |
| `application.hybrid` | ALNS segment, pool seal, selection, reconstruction, candidate verification 이전 full-evaluation, adoption/fallback 조립 |

Application/platform outbound port는 application API가 소유한다. Optimizer algorithm SPI인 `RouteSelectionSolverFactory`/`RouteSelectionSession`은 `solver.selection.api`가 소유한다. 어떤 adapter도 역으로 interface를 정의해 application이나 solver가 그것을 구현하게 해서는 안 된다.

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

이 이름은 **`[RECOMMENDED]`** API 후보이며 public wire API 확정이 아니다. `ExecuteHybridPhase`는 `ExecuteWorkerRun` 내부의 application command/state machine이지 별도 public inbound use case가 아니다.

### 6.5 Profile modules

- `rpdptw-profile-standard`는 여러 고객이 명시적으로 재사용할 수 있는 공통 constraint/metric/score catalog만 가진다.
- `rpdptw-profile-<profile-namespace>`는 특정 고객 또는 계약군의 profile/preset composition과 필요한 extension 구현을 가진다.
- `rpdptw-core`는 어떤 profile module도 compile-depend하지 않는다.
- `apps/worker`가 배포 bundle에 포함할 profile providers를 assembly한다.
- 한 bundle에 여러 profile을 넣어도 registry는 exact customer/profile/version으로 격리한다.

### 6.6 Adapter와 app modules

| Module | 책임 |
|---|---|
| `adapters/common` | `adapter.json`과 `adapter.local` package; JSON mapping, local artifact/state/dispatch |
| `adapters/route-selection-ortools-cpsat` | Optional `RouteSelectionSolverFactory`/session implementation, CP-SAT model/hint/status mapping, native-loader와 integration test |
| `adapters/object-s3`, `workflow-aws-stepfunctions`, `compute-aws-lambda` | 선택된 AWS target의 artifact/state, durable workflow, compute/telemetry event mapping |
| `adapters/<provider>` | ECS, GCP, Kubernetes 등 future substitution의 artifact/state/workflow/compute/telemetry package |
| `apps/cli` | Local/offline invocation과 human-readable failure |
| `apps/api` | Submission/status/result transport entrypoint |
| `apps/worker` | Headless prepare/search/verify/finalize process entrypoint |

App module은 composition root다. SDK client, optional route-selection backend, profile provider와 application runtime을 생성·주입하는 곳이며 domain/search 내부에서 global singleton, backend model/solver 또는 SDK default client를 만들지 않는다. Route-selection backend capability가 없는 assembly는 MIP 성공을 흉내 내는 fake가 아니라 typed `UnavailableRouteSelectionSolverFactory`를 주입하고 `BACKEND_UNAVAILABLE`로 optional fallback 또는 required failure를 명시한다. OR-Tools assembly가 있으나 native load/platform 초기화가 실패한 `NATIVE_RUNTIME_UNAVAILABLE`과 구분한다.

JSON과 local 구현은 별도 Maven module로 쪼개지 않고 `adapters/common`의 package로 둔다. AWS module과 deployment assembly는 선택된 target이지만 `RM-8` parity/cutover gate 전에는 production 완료를 뜻하지 않는다. AWS SDK dependency는 domain·core·solver·verification·application classpath에 넣지 않고 해당 adapter/deployment module에만 격리한다. future provider도 같은 규칙을 따른다.

### 6.7 Route-selection backend contract

`solver.selection.api`는 vendor-neutral interface만 노출한다.

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

구체 이름은 추천안이지만 다음 책임 분리는 계약이다.

- `RouteSelectionModelSpec`: Domain §12.5의 exact projection, coefficient range와 model fingerprint
- `RoutePoolSnapshot`: Stable evaluated-artifact IDs/order와 incumbent pins
- `MipWarmStart`: Projected-column/unassigned mapping과 prechecked feasibility
- `RouteSelectionBudget`: Approved CP-SAT time/deterministic-work budget, wall-clock reserve와 required/optional policy. 숫자는 승인 전 `OPEN`
- `RouteSelectionOutcome`: Generic status, incumbent presence, selected projected-column IDs, provenance와 termination cause. Unassigned는 materialization이 coverage complement로 재구성한다.
- `SolverCancellationProbe`: `solver.termination`이 소유하는 provider-neutral read-only cancellation contract. Application type이나 SDK context를 참조하지 않음

OR-Tools CP-SAT adapter는 checked integer model build, hint/parameter mapping, solve, raw `CpSolverStatus`×incumbent gate, selected ID extraction, cancellation hook와 callback/reference cleanup만 소유한다. Reconstruction, cover conversion, full route evaluation, comparator adoption과 final verification은 backend adapter 밖에 둔다.

Architecture test는 이 backend가 exported `solver.selection.api`와 immutable `solver.pool` contract만 참조하고 `solver.search`, `solver.state`, `application.hybrid`, `verification`을 0회 참조하는지 검사한다.

OR-Tools CP-SAT/native lifecycle:

1. `RouteSelectionSolverFactory.openSession()`은 명시적
   `RouteSelectionSession extends AutoCloseable`을 반환한다. Session close는
   cancellation registration과 callbacks를 clear하고 Java references를 해제한다.
2. Process startup 또는 isolated capability initialization에서
   `Loader.loadNativeLibraries()`를 호출한다. Load/platform 실패는
   `NATIVE_RUNTIME_UNAVAILABLE`로 fail closed한다.
3. `CpModel`은 `BoolVar`/integer linear constraint와 checked int64 coefficient만
   사용한다. 이 0-1 exact model에는 MPSolver를 사용하지 않는다.
4. ALNS incumbent는 CP-SAT hint로 전달하되 채택을 가정하지 않는다.
5. Application deadline의 remaining budget을 각 lexicographic stage의
   `max_time_in_seconds`에 매핑하고 cancellation은 실행 중인
   `CpSolver.stopSearch()`로 전파한다. 숫자는 승인 전 `OPEN`이다.
6. Raw `CpSolverStatus.OPTIMAL`/`FEASIBLE`에서만 selected value를 읽는다.
   `INFEASIBLE`/`MODEL_INVALID`/`UNKNOWN`에서는 `value()`/`booleanValue()` 접근이
   0이어야 한다. Stop/time limit cause는 raw status와 별도 기록한다.
7. `num_workers`, `random_seed`, time/deterministic-work limit, absolute/relative
   gap과 model/order는 config/fingerprint에 명시한다. Single-worker/fixed-seed
   profile도 `PROPOSED TEST_ONLY`이며 evidence 없이 strong replay를 주장하지
   않는다. Gap limit 충족으로 반환된 `OPTIMAL`을 exact proof로 받아들이지 않는다.
8. Native library는 process-wide로 load되며 solve마다 unload하지 않는다. Packaged
   resource가 임시 디렉터리에 풀리면 JVM `deleteOnExit` cleanup, writable temp와
   잔여 temp를 runtime test에서 확인한다.
9. OR-Tools는 Apache License 2.0이다. 선택된 artifact/version의 LICENSE와 포함된
   NOTICE/third-party attribution, transitive native closure를 distribution에 보존하고
   SBOM·checksum·취약점 evidence를 만든다. 상용 license/server/token/capacity lease를
   가정하지 않는다.

Default build/test는 OR-Tools artifact/native runtime 없이 통과해야 한다.
Gate-open integration은 별도 Maven/CI profile과 isolated environment를 사용한다.
`com.google.ortools:ortools-java:${ortools.version}`는 backend module에만 선언하고
`${ortools.version}`은 승인 전 `OPEN`이다.

## 7. Dependency direction와 cycle 방지

### 7.1 허용 dependency DAG

**`[RECOMMENDED]`** Compile dependency는 다음 DAG의 화살표 방향으로만 허용한다.

```text
rpdptw-core
  # 다른 repository Maven module dependency 없음

rpdptw-solver
  → rpdptw-core

rpdptw-verification
  → rpdptw-core
  # rpdptw-solver dependency 금지

rpdptw-profile-*
  → rpdptw-core

rpdptw-application
  → rpdptw-core
  → rpdptw-solver
  → rpdptw-verification

adapters/route-selection-ortools-cpsat (optional/gated)
  → rpdptw-core
  → rpdptw-solver

adapters/common
  → rpdptw-core
  → rpdptw-verification
  → rpdptw-application

adapters/<provider> (승인 후)
  → rpdptw-application
  → adapters/common  # JSON/local shared contract가 필요할 때만

apps/*
  → rpdptw-application
  → selected route-selection backend
  → selected adapter modules
  → selected profile modules

deployment/*
  → app artifact/image coordinates only
```

`A → B`는 **A의 Maven compile dependency가 B를 가리킨다**는 뜻이다. 실제 runtime call은 port inversion으로 반대 방향일 수 있다. 같은 단계의 module 사이에서 위 목록에 없는 dependency가 필요하면 자동 추가하지 않고 semantic owner와 cycle 가능성을 review한다.

### 7.2 금지 dependency

| From | 금지 대상 | 이유 |
|---|---|---|
| `rpdptw-core`, `rpdptw-solver`, `rpdptw-verification` | Provider SDK, HTTP/framework package | Provider/runtime 침투 방지 |
| `rpdptw-core`, generic solver, verification, application | `com.google.ortools.*`와 다른 optimizer backend API | Algorithm backend 격리와 optimizer-free build |
| `rpdptw-core` | Solver, verification, application, adapter/app/deployment | Stable kernel의 inward dependency 보존 |
| `rpdptw-solver` | Verification, application, customer profile implementation, adapter | Customer branch와 미검증 publication 방지 |
| `rpdptw-verification` | `rpdptw-solver`와 그 search/cache package | 독립 검증 |
| `result.*` package | Provider storage/database DTO | Result 의미와 저장 표현 분리 |
| `profiles/*` | `search` 내부 package | Policy가 algorithm을 조작하는 것 방지 |
| `rpdptw-application` | Provider SDK | Portable port 유지 |
| `adapters/<provider>` | 다른 provider adapter | Provider 간 transitive coupling 방지 |
| 모든 module | 다른 module의 `.internal` package | Public contract 우회 방지 |

### 7.3 Enforcement

`mvn verify`는 최소한 다음을 자동 검사해야 한다.

1. Maven Enforcer `bannedDependencies`로 `rpdptw-core`, solver와 verification의 cloud/transport dependency를 차단한다.
2. Architecture test가 `rpdptw-core` 내부 package dependency와 cross-module layer rule을 검사한다.
3. `jdeps` 또는 동등한 bytecode inspection으로 shaded/transitive cloud dependency 침투를 확인한다.
4. Core source에서 provider package, customer-name switch와 global SDK client 생성을 검색한다.
5. Maven reactor dependency graph에 cycle이 없음을 검사한다.
6. Test-fixture module이 production scope로 들어오지 않음을 검사한다.
7. `com.google.ortools.*` bytecode reference가 `adapters/route-selection-ortools-cpsat` 밖에 0개인지 검사한다.
8. Optimizer-free profile에서 core/solver/application/unit test가 실행되고, gated assembly만 backend capability를 광고하는지 검사한다.

Module 간 순환을 interface module 추가로 숨기지 않는다. 순환이 발견되면 공유 의미의 진짜 owner를 정하고 더 안쪽의 작은 contract module로 옮긴다.

## 8. Java package와 namespace 규칙

### 8.1 Base namespace

**`[RECOMMENDED]`** 새 목표 architecture는 기존 current-state `com.ronext.optimizer`와 구분하여 다음 base namespace를 사용한다.

```text
com.ronext.rpdptw
```

추천 package mapping:

```text
com.ronext.rpdptw.input.contract
com.ronext.rpdptw.domain
com.ronext.rpdptw.normalization
com.ronext.rpdptw.travel
com.ronext.rpdptw.evaluation.api
com.ronext.rpdptw.evaluation.runtime
com.ronext.rpdptw.propagation
com.ronext.rpdptw.solver.search
com.ronext.rpdptw.solver.search.destroy
com.ronext.rpdptw.solver.search.repair
com.ronext.rpdptw.solver.search.acceptance
com.ronext.rpdptw.solver.search.adaptive
com.ronext.rpdptw.solver.state
com.ronext.rpdptw.solver.pool
com.ronext.rpdptw.solver.selection.api
com.ronext.rpdptw.solver.selection.conversion
com.ronext.rpdptw.solver.hybrid
com.ronext.rpdptw.verification.api
com.ronext.rpdptw.verification.candidate
com.ronext.rpdptw.result.api
com.ronext.rpdptw.result.finalization
com.ronext.rpdptw.verification.result
com.ronext.rpdptw.application.port.in
com.ronext.rpdptw.application.port.out
com.ronext.rpdptw.application.service
com.ronext.rpdptw.application.execution
com.ronext.rpdptw.application.hybrid
com.ronext.rpdptw.adapter.json
com.ronext.rpdptw.adapter.local
com.ronext.rpdptw.adapter.routeselection.ortools.cpsat  # optional/gated algorithm backend
com.ronext.rpdptw.adapter.object.s3
com.ronext.rpdptw.adapter.workflow.aws.stepfunctions
com.ronext.rpdptw.adapter.compute.aws.lambda
com.ronext.rpdptw.adapter.<provider>  # future substitution only
com.ronext.rpdptw.profile.<stable_namespace>
```

### 8.2 Package rules

- 한 Maven module의 exported package는 다른 module이 사용할 의도적 contract만 포함한다.
- 구현 세부는 `.internal`에 두고 다른 Maven module에서 참조하지 않는다.
- 범용 `util`, `common`, `shared`, `manager`, `helper` package를 새 semantic owner 대신 사용하지 않는다.
- DTO suffix는 external/application boundary type에만 사용한다. Domain value를 `*Dto`로 부르지 않는다.
- AWS provider/product name과 SDK type은 AWS adapter/deployment package 밖에 나타나지 않는다. Gated optimizer vendor name은 해당 route-selection backend package와 explicit assembly/profile에만 나타난다.
- Customer identifier는 `profiles/` module과 그 resource/config 안에서만 허용한다.
- `record`, sealed type와 immutable collection은 의미에 맞게 사용할 수 있지만 Java type 선택이 wire compatibility를 암묵적으로 결정하지 않는다.
- Static mutable registry, global random, system clock과 unordered classpath discovery 결과를 core에서 사용하지 않는다.
- JPMS `module-info.java` 채택은 별도 ADR로 결정한다. Maven module boundary와 architecture test는 JPMS 여부와 관계없이 필수다.

## 9. Customer extension model

### 9.1 Extension hierarchy

**`[CONTRACT]`** 새 고객 요구는 다음 순서에서 표현 가능한 가장 좁은 seam에 둔다.

```text
versioned input adapter mapping
→ static compatibility
→ bound hard constraint
→ neutral metric contributor
→ score component
→ objective dimension/comparator
→ SolvePlan/profile composition
→ typed domain/propagation facet extension
```

앞 단계로 표현할 수 있는 요구를 뒤 단계 또는 core change로 올리지 않는다.

### 9.2 Profile와 preset

**`[RECOMMENDED]`** Registry identity는 다음 의미를 분리한다.

```text
CustomerKey
ProfileKey
ProfileVersion
PresetKey
ProfileSchemaVersion
ImplementationContractVersion
```

- Solve 요청은 exact customer/profile/version과 허용된 preset만 선택한다.
- Preset 생략은 해당 exact profile version에 선언된 exact default preset을 사용한다.
- `latest`, 비슷한 이름, 다른 고객의 preset 또는 dependency fallback은 금지한다.
- Profile version은 immutable하다. 같은 identity의 내용이 바뀌면 startup/build가 실패해야 한다.
- 등록된 profile descriptor와 resource는 canonical fingerprint를 가진다.

추천 provider contract:

```text
ProfileProvider
  → profile identities
  → constraint descriptors
  → metric descriptors
  → score descriptors
  → objective presets
  → SolvePlan descriptors
  → optional typed domain facet providers
```

Java `ServiceLoader`를 사용할 수 있지만 discovery order는 의미가 아니다. 모든 provider를 읽은 뒤 stable identity로 정렬하고 duplicate를 거부한다. Reflection/classpath scanning과 “먼저 발견된 구현 승리”는 금지한다. ServiceLoader 대 build-generated catalog 선택은 ADR로 승인한다.

### 9.3 Composition과 validation

Profile은 solve 전에 다음을 모두 검증하고 immutable `BoundProfile`을 만든다.

1. Customer/profile/version/preset의 존재와 권한
2. Duplicate constraint, metric, score와 objective key
3. Fact/metric dependency closure
4. Unit, scale, value type와 schema compatibility
5. Constraint와 metric parameter의 range/required field
6. Objective dimension availability와 comparator direction
7. `SolvePlan` stage가 참조하는 objective/operator availability
8. `LEASE` fleet와 outsourced objective 지원의 일치
9. Mandatory support와 top-level objective ordering
10. Domain facet provider와 verifier support version 일치
11. 모든 implementation/config/resource fingerprint
12. Route-selection model mode와 profile별 projection capability/version/fingerprint
13. Exact request/unassigned/concrete-vehicle/global-resource row mapping
14. Projected coefficient unit, integer range, scaling과 round-trip
15. 단계별 또는 native multi-objective의 lexicographic priority/tolerance 보존
16. Non-projectable dimension/constraint의 typed skip; production exact mode에서 silent surrogate 금지
17. `MipWarmStartManifest`의 model identity와 row feasibility

Validation failure는 pre-solve binding error이며 default profile로 재시도하지 않는다.

### 9.4 Constraint, metric, score와 objective

| Extension | 소비할 수 있는 것 | 반환 | 금지 |
|---|---|---|---|
| Hard constraint | Normalized facts, propagation facts | Typed feasible/rejection evidence | Finite penalty로 hard 위반 허용 |
| Metric contributor | Policy-neutral facts | Unit-aware neutral amount/fact | 가격, 선호, final reason |
| Score component | Bound metric snapshot + config | Cost/soft-penalty component | Route/raw input 재해석 |
| Objective dimension | Score/metric/outcome value | Ordered comparable value | Physical propagation |
| Comparator | Ordered objective vector | Stable total order | Stage 실행, hidden Big-M |
| `SolvePlan` | Available objectives/operators + explicit budget refs | Stage/handoff/guard plan | Hard rule 해제 |

### 9.5 Domain seam extension criteria

Common domain/propagation seam은 다음을 모두 만족할 때만 확장한다.

1. 기존 normalized fact와 propagation fact로 실제 물리 상태를 표현할 수 없다.
2. 단가, label, 우선순위 또는 output formatting 문제가 아니다.
3. 한 고객 implementation을 core field로 직접 넣지 않고 typed facet contract로 격리할 수 있다.
4. Full propagation과 candidate verifier가 같은 의미를 독립적으로 계산할 수 있다.
5. 기존 profile에서 facet 부재가 기존 fingerprint와 결과를 바꾸지 않는다.
6. Domain/algorithm/verifier 영향과 migration이 ADR에 기록된다.

`Map<String,Object>`, raw JSON node와 customer-specific nullable field를 `Request`, `Vehicle`, `RouteState`에 추가하는 방식은 금지한다. Typed immutable facet은 stable key/version, owner module, normalization, propagation, verifier와 fingerprint contract를 가져야 한다.

### 9.6 Customer-name branch 금지

다음은 architecture violation이다.

```java
if (customerId.equals("...")) { ... }
switch (customerName) { ... }
if (presetName.contains("outsourcing")) { ... }
```

Core는 bound interface와 typed facts만 사용한다. Customer module 이름은 app assembly/registry metadata에 존재할 수 있지만 common domain, propagator, evaluator, search와 verifier가 분기 기준으로 읽어서는 안 된다.

## 10. Local/single-run runtime

### 10.1 목적

**`[PORTABLE]`** Local runner는 cloud의 축소판이 아니라 같은 application use case와 verification gate를 사용하는 reference execution이다. 개발, hand calculation, deterministic rerun과 provider adapter contract test의 기준이 된다.

### 10.2 Flow

```text
CLI/API input
→ local versioned adapter
→ PrepareSolveSnapshot
→ local immutable artifact store
→ ExecuteWorkerRun in same process
   └─ [approved plan] declared HybridPhase loop with local pool + injected route-selection backend
→ candidate verification
→ finalization/audit
→ result verification
→ local publication
→ exit status + result reference
```

추천 local port 구현:

| Port | Local implementation |
|---|---|
| `ArtifactStore` | Explicit workspace directory + atomic rename + digest check |
| `RunStateRepository` | In-memory 또는 local append-only file |
| `WorkerDispatcher` | Same-process executor |
| `CancellationSignal` | In-memory cooperative token |
| `ResultPublisher` | Atomic local pointer/CAS |
| `TelemetrySink` | Structured log + test recorder |

Local runner도 raw result를 직접 stdout에 정상 결과로 표시하지 않는다. 두 verifier `PASS` 뒤의 result만 정상 exit/result로 노출한다.

### 10.3 Single-run과 local multi-worker

- Single-run은 하나의 explicit run config와 warm start를 실행한다.
- Hybrid single-run은 worker-local pool과 backend session을 solve/run scope에 묶고 종료 시 callback/cancellation registration을 정리한다. Process-wide native library를 solve마다 unload하지 않는다.
- Backend가 없을 때 optional plan은 typed ALNS-only fallback, required plan은 explicit unavailable failure를 반환한다.
- Local multi-worker test는 같은 `WorkerAssignment` 목록을 stable order로 실행하고 completion order와 무관하게 aggregator가 champion을 고르는지 검증한다.
- Thread pool size는 result fingerprint나 algorithm budget이 아니다.
- Local wall-clock timeout을 정상 `MAX_STEPS_REACHED`로 바꾸지 않는다.

## 11. Distributed multi-round runtime

### 11.1 Logical state machine

**`[PORTABLE]`** Distributed execution은 다음 logical state를 provider와 무관하게 보존한다.

```text
Solve/ExecutionRound:
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

WorkerRun/HybridPhase (각 worker가 독립 substate를 가짐):
ASSIGNED
→ WORKER_RUNNING
→ ALNS_RUNNING
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
→ WORKER_VERIFYING
→ WORKER_SUCCEEDED | DEGRADED_ALNS_ONLY

exceptional solve/worker:
  REJECTED_INPUT | BINDING_FAILED | CANCEL_REQUESTED | CANCELLED
  WATCHDOG_REACHED | RESOURCE_LIMIT_REACHED | PLATFORM_TIMEOUT
  BACKEND_UNAVAILABLE | NATIVE_RUNTIME_UNAVAILABLE | MODEL_INVALID
  | ROUTE_SELECTION_FAILED
  FAILED | INCOMPLETE | PUBLICATION_REJECTED
```

같은 outer round의 worker는 서로 다른 substate에 있을 수 있다. Optional selector unavailable/failure는 pool/candidate isolation이 증명될 때만 worker `DEGRADED_ALNS_ONLY`가 될 수 있고, required selector failure는 worker failure를 통해 round를 `INCOMPLETE`로 만든다. 상태 이름은 추천안이지만 정상/예외 종료 의미를 합치지 않는 것은 계약이다.

### 11.2 Multi-round flow

```text
immutable ExecutionManifest
→ build up to 8 independent initial candidates
→ phase-1: run each available candidate with exact screenMaxSteps to MAX_STEPS_REACHED
→ require normal completion + cache-free validation for every available screen candidate
→ stable comparator/tie-break selects phase-1 champion
→ prepare phase-2 WorkerAssignment list with that common warm start
→ fan-out declared phase-2 logical workers
    → load identical SolveSnapshot and declared warm start
    → derive declared seed
    → for each declared HybridPhase:
        → run that phase's exact ALNS segment-step budget
        → collect hard-feasible immutable evaluated route artifacts
        → if approved hybrid plan:
            → seal worker-local pool
            → route selection with separate work/time budget or typed skip/fallback
            → reconstruct + authoritative full evaluation
            → adopt only strictly better candidate as next phase warm start
    → require Σ completed ALNS segment steps = phase2MaxSteps
    → candidate verify
    → persist immutable verified-candidate outcome reference
→ fan-in
    → require declared worker identity completeness
    → reject mismatched fingerprints/termination/candidate gate
    → stable comparator + deterministic tie-break
    → persist RoundChampion
→ compare RoundChampion with previous champion
    → STRICTLY_BETTER: next round uses it as common warm start
    → EQUAL or WORSE: NO_STRICT_IMPROVEMENT
    → configured maxRounds complete: MAX_ROUNDS_REACHED
→ final champion → finalize/audit → result verify → publication
```

**`[CONTRACT]`** Official benchmark에서 선언된 worker 하나라도 정상 completion과 verification을 끝내 충족하지 못하면 round와 전체 실행은 `INCOMPLETE`다. 성공 worker 일부만으로 champion을 확정하거나 다음 round를 시작하지 않는다.

### 11.3 Logical identity와 idempotency

추천 identity:

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

규칙:

- 같은 submission idempotency key에 다른 input/profile/manifest digest가 오면 conflict다.
- `WorkerRunId`는 logical round/worker/warm-start/config identity에 고정된다.
- Retry `AttemptId`는 바뀔 수 있지만 seed, warm start, requested steps와 logical worker identity는 바뀌지 않는다.
- `STRONG_REPLAY` route-selection은 같은 pool/model/warm-start/backend config와 `RouteSelectionRunId`를 보존하고 attempt만 바꿀 수 있다.
- `TIMEBOXED_HYBRID`는 solve가 시작되기 전 dispatch/admission/native-init failure만 같은 `RouteSelectionRunId`로 retry한다. Solve가 시작된 뒤에는 같은 logical ID로 result-bearing retry를 하지 않고 optional plan은 ALNS fallback, required plan은 `INCOMPLETE`다. 다시 실행하려면 새 logical run/manifest를 만든다.
- 같은 logical worker의 duplicate success가 같은 verified-candidate digest면 하나로 수렴할 수 있다.
- `STRONG_REPLAY`의 같은 logical worker identity에 서로 다른 verified-candidate digest가 생기면 integrity violation이며 임의의 하나를 선택하지 않는다. Timeboxed execution은 위 no-result-bearing-retry rule로 이 충돌을 예방한다.
- Champion과 final publication은 compare-and-set으로 한 번만 확정한다.
- Orchestrator completion order는 comparator input order가 아니다.

## 12. Provider-neutral logical ports

### 12.1 Port catalog

**`[PORTABLE]`** 구체 interface 이름은 추천안이지만 다음 책임은 provider-neutral하게 유지한다.

| Logical port | 책임 | Core/application에 보이는 값 |
|---|---|---|
| `SubmissionPort` | Idempotent solve 접수 | Submission identity, input/profile/config refs |
| `ArtifactStore` | Immutable artifact put/get/verify | `ArtifactRef`, digest, media/schema version |
| `RunStateRepository` | Solve/round/worker 상태와 CAS | Logical identity, state/version |
| `WorkerDispatcher` | Logical worker 시작·조회·stop 요청 | `WorkerAssignment`, dispatch handle |
| `WorkflowExecutionPort` | Durable top-level execution 시작·조회·중단 | Manifest ref, workflow handle |
| `CancellationPort` | Cancellation intent 기록·조회·전파 | Solve/worker identity, requested-at/reason |
| `ProfileCatalogPort` | Approved profile inventory/config snapshot 제공 | Exact identity + signed/digested content |
| `SecretResolver` | Adapter credential/secret resolution | Opaque secret handle; core에 전달하지 않음 |
| `ResultPublisher` | Both-gate verified result CAS publication | Result ref + reports + expected state version |
| `TelemetryPort` | Structured event/metric/trace export | Provider-neutral event fields |
| `Clock` | Application elapsed/deadline 관측 | Monotonic/application time; algorithm quality에 미사용 |

### 12.2 Port boundary rules

- Port method에 provider resource name/URI, workflow event, function context 또는 job request type을 넣지 않는다.
- `ArtifactRef`의 provider location은 adapter-owned opaque locator다. Core는 digest와 schema identity만 사용한다.
- `SecretResolver` 결과를 domain/profile fingerprint에 넣지 않는다. Secret value는 log/artifact에 쓰지 않는다.
- OR-Tools CP-SAT에는 commercial entitlement/capacity lease port를 두지 않는다.
  WorkerDispatcher/deployment의 일반 queue·CPU·memory admission으로 동시 solve를
  제한하며 구체 수치는 측정과 승인 전 `OPEN`이다.
- Orchestration port는 route, matrix와 result 전체를 state payload로 운반하지 않는다.
- Provider retry와 application retry를 구분한다. Logical attempt 기록 없이 SDK가 business operation을 무한 재시도하게 두지 않는다.

## 13. Physical topology와 provider boundary

**`[CONTRACT]`** 선택된 target/reference topology는 **AWS S3 (object storage) + AWS Step Functions (durable orchestration) + AWS Lambda (API/coordinator/worker compute)**다. 현재 GCP deployment 자료와 SDK usage는 §2의 migration characterization 대상이며 target architecture가 아니다. AWS 선택은 resource sizing, IaC, cost/security operations 또는 production cutover approval을 자동으로 포함하지 않는다.

이 문서가 지금 고정하는 것은 논리 책임뿐이다.

| Logical responsibility | 현재 고정할 contract | 물리 구현 |
|---|---|---|
| Submission/status | idempotent submission identity와 verified retrieval | AWS API Lambda adapter |
| Orchestration | run/round/worker transition, declared fan-out/fan-in와 completeness | AWS Step Functions thin adapter |
| Worker execution | immutable assignment, exact seed/config, cooperative cancellation | AWS Lambda worker adapter |
| Artifact | immutable reference, digest, schema identity | AWS S3 adapter |
| State/publication | CAS, retry identity, terminal state와 audit trail | AWS S3-backed adapter; application semantics remain portable |
| Secret/config/telemetry | opaque secret, exact config identity, portable correlation field | AWS adapter/deployment boundary |

AWS implementation은 artifact reference만 전달하고 domain/result 의미, seed derivation, ALNS step, comparator 또는 verifier를 재구현해서는 안 된다. AWS SDK, ARN, event DTO와 retry client policy는 AWS adapter/deployment에만 둔다. Local runner와 AWS는 같은 input/profile/manifest에서 같은 semantic artifact/result fingerprint와 worker/round completion, retry identity, two-gate publication 의미를 보여야 한다. ECS, GCP, Kubernetes 등의 채택은 Master §16.3의 evidence, parity suite와 별도 approval 뒤에만 가능하다.

## 14. Logical multi-round execution

**`[PORTABLE]`** 물리 dispatcher와 storage가 정해지지 않아도 application은 다음 논리 흐름을 구현·test double로 검증할 수 있다.

```text
immutable ExecutionManifest
→ build up to 8 independent initial candidates
→ phase-1: run each available candidate with exact screenMaxSteps to MAX_STEPS_REACHED and cache-free validation
→ stable comparator/tie-break selects phase-1 champion
→ build declared phase-2 WorkerAssignment list with that common warm start
→ dispatch phase-2 logical workers
    → load identical SolveSnapshot and declared warm start
    → derive declared seed
    → for each declared worker-local HybridPhase:
        → run exact ALNS segment-step budget
        → route-pool delta → stable snapshot
        → optional route-selection with separate work/time budget or typed fallback
        → reconstruct → full evaluate → strict adoption
        → adopted/retained champion becomes next phase warm start
    → require Σ completed ALNS segment steps = phase2MaxSteps
    → candidate verify
    → return immutable verified-candidate outcome reference
→ require declared worker completeness
→ stable comparator/tie-break selects one round champion
→ strictly better champion becomes next-round common warm start
→ otherwise NO_STRICT_IMPROVEMENT; max round ends MAX_ROUNDS_REACHED
→ final champion → finalize/audit → result verify → publication
```

Each retry preserves `WorkerRunId`, seed, warm start, requested steps and logical worker identity; only the attempt identity may change. One missing, failed or unverified declared worker makes the round and official execution `INCOMPLETE`. Completion order never decides the champion. The round/worker counts, step budgets, max rounds and watchdog have no official values before `Q-BENCH-02` calibration.

Hybrid execution adds `HybridPhaseOrdinal`, `RouteSelectionRunId`, pool/model/backend fingerprints and required/optional fallback policy to the same identity. `SET_COVER_THEN_CONVERT` intermediate and raw backend incumbent are not worker candidates. Cross-worker pool fan-in is not part of this baseline; if later approved, it must preserve declared-worker completeness and stable worker-ordinal merge independently of completion order.

Cancellation is a cooperative intent. An incomplete COW candidate is discarded, and intent, actual worker termination and last completed boundary are recorded separately. A retained committed candidate is never a normal result without both verification gates.

## 15. Selected AWS target implementation gate

`Q-INFRA-01`은 2026-07-26 사용자 승인으로 AWS S3 + Step Functions + Lambda target/reference runtime을 선택했다. AWS adapter/deployment 구현과 cutover는 Master §15.10/§16.3의 evidence를 통과해야 하며, 선택 자체가 code, IaC 또는 production activation 완료를 뜻하지 않는다.

AWS adapter는 SDK/event mapping, Step Functions definition, Lambda invocation, S3 locator, state transaction/lease, secret resolution, network/encryption, telemetry exporter와 deployment pipeline만 구현한다. Canonical input/travel/profile fingerprint, worker/round identity, termination semantics, candidate/result verification, champion selection, outcome/provenance identity 또는 retry semantics는 바꿀 수 없다. Future provider adapter도 같은 제한을 만족해야 한다.

## 16. Configuration, secret, artifact와 provenance

### 16.1 Configuration layers

**`[PORTABLE]`** Configuration을 다음 계층으로 분리한다.

| Layer | 예 | Version/fingerprint |
|---|---|---|
| Semantic policy | Numeric/time/travel/service rules | Problem/snapshot에 필수 |
| Customer profile | Constraint/metric/score/objective/preset | Bound profile에 필수 |
| Algorithm config | Operator, acceptance, explicit experiment values | Execution manifest에 필수 |
| Route-pool config | Admission, dominance, cap/pruning/persistence policy | Hybrid manifest와 snapshot에 필수 |
| Route-selection config | Projection/mode, backend/version, seed/thread/numeric/work/time budget | Hybrid manifest/model record에 필수 |
| Logical execution | Round/worker/warm-start/seed/steps contract | Manifest에 필수 |
| Platform config | Region, queue/concurrency, CPU/memory, retry backoff | Deployment/run metadata |
| Secret | Credential/token/key material | 값은 fingerprint/log에서 제외 |

Environment variable은 app bootstrap에서 platform config/secret locator를 제공할 수 있지만 core가 직접 읽지 않는다. Solve 시작 뒤 semantic/profile/algorithm config를 mutable remote config에서 다시 읽지 않는다.

**`[OPEN-EXPERIMENT]`** `Q-BENCH-02`의 `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`와 watchdog은 explicit experiment/test config로만 제공한다. Official default가 없을 때 생략을 숫자 default로 채우지 않고 official manifest creation을 거부한다. 8개 initial portfolio와 phase-1/phase-2 structure 자체는 `Q-ALG-01`의 확정 계약이다.

### 16.2 Artifact contract

추천 `ArtifactRef` 의미:

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

- Digest 검증 전에는 deserialization하지 않는다.
- Artifact는 immutable create-once다.
- Mutable “latest result” pointer는 실제 result가 아니라 publication index이며 CAS로 갱신한다.
- Orchestrator state에는 artifact reference만 넣는다.
- Input, snapshot, warm start, worker candidate/report, final result와 audit evidence의 retention/classification을 분리한다.
- Hybrid 사용 시 route-pool delta/snapshot, model manifest, backend outcome, conversion과 phase record를 각각 독립 schema/digest로 저장한다. Mutable solver model/native handle은 artifact가 아니다.
- Large pool은 workflow/state payload로 운반하지 않고 `ArtifactRef`로 전달하며 load 시 digest와 authority fingerprint를 모두 확인한다.
- Audit의 feasible insertion 발견 내부 record는 public result payload와 다른 access/retention policy를 가질 수 있다.

### 16.3 Provenance

Publishable result는 다음 lineage를 연결한다.

```text
submission/input digest
→ adapter/canonical schema
→ normalized problem
→ prepared travel
→ customer profile/preset
→ algorithm/build/runtime
→ manifest/round/worker/attempt
→ optional hybrid phase + route-pool snapshot
→ route-selection model/warm-start/backend outcome
→ conversion/full evaluation/adoption or fallback
→ committed candidate
→ candidate verifier report
→ finalization/audit
→ result verifier report
→ published payload digest
```

향후 provider execution ID, runtime/image digest와 deployment revision은 runtime compatibility metadata로 남길 수 있지만 domain result identity를 provider product에 종속시키지 않는다.

## 17. Observability, retry, failure와 security boundary

### 17.1 Structured observability

모든 application/provider event는 가능한 범위에서 다음 correlation field를 갖는다.

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
buildRuntimeFingerprint
termination
candidateVerification
resultVerification
artifactDigest
routePoolFingerprint
routeSelectionModelFingerprint
routeSelectionStatus
```

관측 category:

- Phase duration: adapter, normalization, travel, binding, portfolio, ALNS, pool seal, model build, route selection, conversion, verification, finalization, publication
- Search work: requested/completed steps, operator별 call/reward/weight/removal count, repair cheap/exact/feasible insertion count
- Pool: imported/new/rejected-origin/dominated/replaced/pruned/protected artifacts, bytes와 seal/merge time; projection별 column count
- Route selection: variables/request/vehicle/resource rows, warm-start coverage/disposition, raw CP-SAT/generic status, incumbent presence, deterministic work/wall time, selected count와 fallback/adoption reason
- Resource: heap/native RSS, CPU, GC, artifact bytes, cache statistic와 application queue/admission/concurrency
- Reliability: retry, duplicate, native load/temp state, cancellation latency, callback cleanup와 rollback/discard outcome
- Quality: verified objective vector와 metric breakdown
- Integrity: fingerprint mismatch, verifier failure, incomplete audit와 publication rejection

Elapsed time과 provider completion order는 quality objective, seed selection 또는 strong reproducibility fingerprint의 hidden input이 아니다.

### 17.2 Retry/failure matrix

| Failure | Retry owner | Retry identity | 정상 result 가능성 |
|---|---|---|---|
| Schema/numeric/reference error | 없음 | New corrected submission 필요 | 없음 |
| Profile/binding dependency error | 없음 | New corrected config/version 필요 | 없음 |
| Artifact digest/schema mismatch | 없음, integrity incident | Same artifact 재사용 금지 | 없음 |
| Transient artifact/state SDK error | Provider adapter/orchestrator | Same logical operation/idempotency key | 성공 후 가능 |
| Worker platform start failure | Orchestrator | Same `WorkerRunId`, new attempt | 완료·검증 후 가능 |
| Watchdog/resource/platform timeout | Orchestrator 정책 | Same logical worker only if manifest allows retry | Official worker 완료 전 불가 |
| Search implementation failure | 자동 성공 변환 금지 | Fault classification 후 결정 | 두 gate 없이는 불가 |
| Pool integrity/dominance failure | 자동 retry 금지 | Same corrupt snapshot 재사용 금지 | Pool 전체를 폐기했고 alias가 없으며 cache-free validated ALNS incumbent가 byte/fingerprint상 그대로일 때만 optional degraded 가능; 그 외 worker failure |
| Route-selection `FEASIBLE_LIMIT` + incumbent | Hybrid policy | Retry하지 않고 해당 incumbent를 materialize/full-evaluate | Strictly-better adoption 뒤 가능 |
| Route-selection `NO_INCUMBENT_LIMIT` | Hybrid policy | Same model/pool/warm start retry는 manifest/reproducibility policy가 허용할 때만 | Optional이면 ALNS fallback, required이면 불가 |
| Route-selection `PROVEN_INFEASIBLE` | 자동 성공 변환 금지 | Feasible warm start/model manifest 조사 | Model/projection/snapshot integrity defect; 정상 fallback으로 품질 결과화 금지 |
| Route-selection `MODEL_INVALID` | 자동 성공 변환 금지 | Model validation/overflow/config 조사 | Optional clean fallback 외 정상 결과 불가 |
| Backend capability unavailable | Application assembly | `BACKEND_UNAVAILABLE`; capability 변경 전 same attempt retry 없음 | Optional이면 `DEGRADED_ALNS_ONLY`, required이면 불가 |
| OR-Tools native runtime unavailable | Application/backend owner | `NATIVE_RUNTIME_UNAVAILABLE`; solve 시작 전 같은 `RouteSelectionRunId`의 새 attempt만 policy에 따라 가능 | Optional이면 `DEGRADED_ALNS_ONLY`, required이면 불가 |
| Model build/numeric/backend failure | 자동 성공 변환 금지 | Defect/config investigation | Optional이면 ALNS fallback만 가능 |
| Conversion/full-evaluation failure | 정상 retry 대상 아님 | Converter/model investigation | Raw solver incumbent는 불가; ALNS fallback만 가능 |
| Candidate verifier `FAIL` | 정상 retry 대상 아님 | Defect/input investigation | 불가 |
| Result verifier `FAIL` | 정상 retry 대상 아님 | Finalization/publication defect investigation | 불가 |
| Publication CAS conflict | Publisher | Same result digest and expected state | 동일 digest면 수렴 가능 |
| Cancellation | Cancellation coordinator | Intent idempotent | 정상 완료로 표시 불가 |

### 17.3 Security and data boundary

- Customer input/result/audit artifact는 classification과 tenant access scope를 가진다.
- Worker role은 필요한 artifact prefix/object와 state item에 최소 권한만 가진다.
- Orchestrator role과 worker role을 분리한다.
- Secret value, raw address/PII와 full input을 log/trace attribute로 남기지 않는다.
- Profile/preset 선택 권한을 API adapter와 binder 양쪽에서 검증한다.
- Artifact digest와 encryption은 별도다. 암호화되었다고 identity 검증을 생략하지 않는다.
- Deployment artifact/image는 immutable digest로 manifest/runtime metadata에 연결한다.
- Provider admin operation은 application cancellation/status API와 구분해 audit한다.

## 18. Test structure와 module evidence

### 18.1 Test pyramid

```text
domain/value unit + property tests
→ module contract tests
→ cache-free equivalence and fault injection
→ application port tests with deterministic fakes
→ local end-to-end
→ provider adapter integration
→ shadow/cutover and operational rehearsal
```

Fixture와 expected result가 비준수 입력을 canonical contract로 바꾸지 않는다. 현재 Win fixture의 decimal `D/U`는 official baseline evidence로 사용할 수 없다.

### 18.2 Module별 evidence

| Module/group | 필수 evidence |
|---|---|
| Input contract/normalization | Schema version/alias conflict, item-first `n=3/FLOOR`, decimal rejection, boundary/overflow, exact time parsing |
| Domain | Dense ID bijection, pair/reference property, terminal/service pattern, immutable snapshot |
| Travel | Provided/generated priority, self `0/0`, asymmetric pair, Great Circle/vehicle-time rounding, completeness와 fingerprint |
| Propagation | Hand-calculated load/time/window/wait/rest/stop/drive resource, full-arc restart |
| Evaluation API/runtime | Dependency closure, duplicate/unit mismatch rejection, profile isolation, comparator transitivity/stable tie |
| Profile modules | Approved preset availability, default exactness, missing objective, cross-customer denial, config fingerprint |
| Search | 8개 policy combination trace, `CLOCK` unavailable case, phase-1 champion selection, 9 operator family fixtures, pair atomicity, shortlist→exact insertion oracle, adaptive/acceptance trace, COW isolation, cache-free equality, fault/cancel discard |
| Route pool | Accepted/rejected route admission, interrupted exclusion, append/import equality, no-alias, safe dominance/Pareto oracle, incumbent pin, stable digest와 memory growth |
| Selection API/conversion | Tiny exact-model oracle, request/unassigned/concrete-vehicle/global-resource rows, projection version/fingerprint와 coefficient range/round-trip, lexicographic stage-optimality preservation, warm-start manifest, status×incumbent result, non-projectable typed skip, clone-not-mutate conversion와 full recomputation |
| OR-Tools CP-SAT backend | Optimizer-free default build, direct integer model/hint, raw status×incumbent mapping, feasible/no-incumbent/infeasible/model-invalid/cancel/native-init fault, TIMEBOXED solve 전/후 retry identity, callback/temp/native lifecycle, Apache-2.0 notice·SBOM/native closure |
| Hybrid application | ALNS→pool→selection→materialization/full evaluation→ALNS feedback, invalid/worse/no-incumbent fallback fingerprint, adopted-only warm start와 required/optional behavior |
| Candidate verifier | Corrupted pair/terminal/travel/metric/objective rejection, poisoned cache 무관성 |
| Finalization | Static `PROVEN`, required exhaustive audit, feasible insertion 발견의 no-auto-fix, bounded diagnostic |
| Result verifier | Outcome exactly-one, ownership reference, summary/payload corruption, both-gate publication block |
| Application runtime | State transition, idempotency conflict, duplicate worker, retry identity, completeness, cancellation |
| Local adapter | Atomic artifact write, digest check, deterministic same-process run |
| 승인된 provider adapter | Port contract parity, SDK error mapping, CAS, artifact digest, stop/cancel and retry fault |
| Architecture rules | Forbidden SDK/customer dependency, package layer, cycle, test dependency leakage |

### 18.3 Independent verifier evidence

Verifier test는 search가 만든 정상 candidate만 사용해서는 안 된다. 각 authority input을 독립적으로 손상시킨 negative fixture를 가져야 한다.

- Route/bank partial/duplicate/split
- Wrong vehicle/terminal/service pattern
- Stale arrival/load/metric/score cache
- Prepared travel fingerprint mismatch
- Outcome/route/ownership mismatch
- Missing/incomplete audit evidence
- Overstated diagnostic confidence
- Summary/payload digest corruption

Search와 verifier가 같은 bug를 공유하지 않도록 expected result는 hand calculation, small exhaustive oracle 또는 별도 reference implementation으로 만든다.

### 18.4 Provider compatibility suite

선택된 AWS adapter와 이후의 모든 provider adapter는 같은 abstract port test를 실행한다.

```text
idempotent submit
same-key/different-digest conflict
conditional state transition
immutable artifact put/get/digest
duplicate worker completion
retry with same logical identity
cancellation intent vs actual termination
incomplete round rejection
both-gate publication CAS
```

Provider emulator만으로 access control, timeout, cancellation과 service integration 의미가 충분히 검증되지 않으면 isolated test environment에서 operational rehearsal를 추가한다.

## 19. Maven build order와 implementation phases

### 19.1 Reactor build order

Maven은 dependency graph로 실제 순서를 계산하지만 review 기준의 topological order는 다음이다.

```text
1. parent + aggregators
2. rpdptw-core
3. owner contract를 소비하는 test-fixtures
4. rpdptw-solver + rpdptw-verification + profile modules
5. optional route-selection backend (GATED: `C-17` scope approval과 `RM-9B`, OR-Tools CP-SAT integration profile 분리 때만)
6. rpdptw-application
7. adapters/common
8. apps/cli + apps/api + apps/worker
9. architecture-rules + logical-port/end-to-end verification
10. selected AWS adapter/deployment packaging; future provider packaging is separately approved
```

`mvn -pl <module> -am verify`가 필요한 선행 module과 evidence를 함께 실행해야 한다. App packaging만 성공하고 core verification이 생략되는 별도 fast path를 release build로 사용하지 않는다.

### 19.2 Implementation phases와 gates

| Phase | 구현 module/산출물 | 완료 gate |
|---|---|---|
| `AR-0 / RM-0` | Parent/aggregator skeleton, architecture rules, status/traceability baseline | Question count와 forbidden value check, no cycle, document/link validation |
| `AR-1 / RM-1` | `rpdptw-core`의 input/domain/normalization/travel package | Immutable `ProblemInstance` + complete `PreparedTravel`; numeric/time/pair/matrix evidence |
| `AR-2 / RM-2` | `rpdptw-core`의 propagation/evaluation package + profile registry | Immutable `BoundProfile`; package/profile isolation, dependency/comparator evidence |
| `AR-3 / RM-3` | Core insertion evaluator + `rpdptw-solver` portfolio | 최대 8개 independent candidate와 policy/route artifact; rollback/trace evidence |
| `AR-4 / RM-4` | `rpdptw-solver`의 COW ALNS, termination, cache | Fault/cancel isolation, cache-free equality, normal deterministic rerun |
| `AR-5 / RM-5` | `rpdptw-verification`의 candidate/finalization/result packages | Corruption rejection, complete audit/outcomes, both-gate publishable result |
| `AR-6 / RM-8-local` | `rpdptw-application`, `adapters/common`, CLI/worker | Idempotent local end-to-end, cancellation, artifact identity, retrieval |
| `AR-7 / RM-6-logical` | Provider-neutral multi-round coordinator와 fake dispatcher | Completion-order independence, retry identity, incomplete round rejection |
| `AR-8 / RM-8-cutover` | Versioned adapter와 logical-port application integration | Compatibility, idempotency/cancellation, shadow와 rollback evidence |
| `AR-9 / RM-6-official` | Approved manifest와 official workflow | `Q-BENCH-02` calibration 승인 + compliant integer travel + all worker `MAX_STEPS_REACHED`/verification |
| `AR-10 / RM-7` | COW profiling | COW 유지 또는 별도 evidence/ADR; 자동 apply/undo 전환 없음 |
| `AR-H1 / RM-9A` | `rpdptw-solver` immutable evaluated-artifact pool/snapshot | Admission/merge/dominance/digest/memory evidence와 separate scope approval |
| `AR-H2 / RM-9B` | Selection SPI + tiny fake/oracle + optional OR-Tools CP-SAT backend | Exact integer model/hint/status/fallback/native packaging·callback cleanup; optimizer-free default build |
| `AR-H3 / RM-9C` | `AR-5/RM-5` both-gate application baseline과 `AR-H2/RM-9B` 위의 worker-local hybrid application + shadow | Adopted-only feedback, optional/required fallback, reproducibility class와 ALNS-only A/B |
| `AR-11 / RM-8-aws` | AWS S3/Step Functions/Lambda adapter/deployment | Local↔AWS semantic parity, SDK isolation, retry/CAS/cancel fault, two-gate publication, shadow/rollback evidence |
| `AR-12 / RM-9-other` | Future topology/variant/multi-trip 후속 | provider parity or deferred resume evidence와 별도 scope 승인 |

### 19.3 Phase dependencies

- Port interface와 fake/local adapter는 `AR-0` 이후 core와 병행할 수 있다.
- AWS adapter/deployment work는 선택된 target 범위에서 진행할 수 있지만 production cutover는 `AR-5`를 우회할 수 없고 `AR-11` parity/shadow/rollback evidence가 필요하다. Future provider work는 별도 approval을 요구한다.
- Official `AR-9`는 `Q-BENCH-02` 승인 수치와 compliant integer `D/U` fixture 없이는 닫을 수 없다.
- Apply/undo, optional variant와 multi-trip은 앞 phase 편의를 위해 미리 core에 넣지 않는다.
- Route pool/MIP는 `AR-H1`~`AR-H3`의 별도 scope approval과 순서를 지킨다. `AR-H3`는 `AR-H2`뿐 아니라 `AR-5`의 independent both-gate verification과 application baseline을 선행조건으로 한다. Solver-neutral contract를 먼저 만들고 vendor backend를 generic solver/core에 넣지 않는다.
- Cross-worker pool fan-in/central selection은 `AR-H3` 완료가 아니라 별도 scalability ADR이다.

## 20. Architecture invariants와 anti-pattern

### 20.1 Invariants

1. Core reactor에서 cloud SDK dependency는 0이다.
2. Common core에서 customer-name conditional은 0이다.
3. Search는 normalized problem, prepared travel과 bound interfaces만 소비한다.
4. 모든 `SearchSnapshot`/committed stable candidate와 full-evaluated selector candidate는 pair/route-bank partition을 만족한다. Materialization 전 draft와 overlap 가능한 `CoverSelection`은 명시적 intermediate type이며 stable candidate가 아니다.
5. Prepared travel 이후 solver/verifier는 raw coordinate/speed fallback을 실행하지 않는다.
6. Bound profile은 exact version/dependency closure와 fingerprint를 가진다.
7. COW candidate만 step 안에서 mutable하며 reject/fail/cancel 시 전체 discard한다.
8. Candidate verifier는 search implementation/cache에 의존하지 않는다.
9. Finalizer는 verified solution만 소비하고 bank를 final status로 직렬화하지 않는다.
10. Both verifier `PASS` 없는 result는 정상 publication/retrieval/benchmark 대상이 아니다.
11. Retry는 logical worker의 seed/warm start/config를 바꾸지 않는다.
12. Official round는 모든 선언 worker가 정상 완료·검증되어야 champion을 확정한다.
13. Orchestrator/provider completion order는 quality comparison에 영향을 주지 않는다.
14. Secret/provider locator는 domain/result 의미에 들어가지 않는다.
15. Open/deferred 값을 hidden default로 채우지 않는다.
16. Pool에는 same-authority hard-feasible immutable evaluated route artifact만 들어가고 live pool은 selector에 전달하지 않는다.
17. Raw optimizer incumbent/`ObjVal`은 reconstruction/full evaluation/candidate gate를 우회하지 않는다.
18. Optional route-selection failure는 ALNS incumbent를 바꾸지 않고 required failure를 정상 성공으로 바꾸지 않는다.
19. Generic core/solver/application의 optimizer vendor dependency는 0이다.

### 20.2 금지 anti-pattern

| Anti-pattern | 문제 |
|---|---|
| Domain/normalization/travel 등 package마다 Maven module 생성 | Reactor/POM/API surface가 불필요하게 증가하고 변경 단위가 파편화됨 |
| Root POM에 모든 cloud/customer dependency 추가 | 모든 module에 volatile dependency 전파 |
| Controller에서 SDK client 생성 후 solver 호출 | Transport/application/platform 책임 결합 |
| `customerId` switch in propagator/ALNS | Customer 요구가 core release를 강제 |
| Generic rule expression이 raw route/object를 reflection으로 읽음 | Type/unit/verifier closure 상실 |
| `Map<String,Object>` domain extension | Fingerprint, validation, hot-path 안정성 상실 |
| Provider workflow definition에 objective/comparator 구현 | Provider migration과 semantic parity 불가 |
| Provider URI를 domain ID로 사용 | Storage migration이 result identity 변경 |
| Platform timeout을 `MAX_STEPS_REACHED`로 변환 | 정상 품질 종료와 platform failure 혼합 |
| Retry 때 새 seed/warm start 선택 | 동일 logical worker가 아님 |
| 성공 worker 일부로 official champion 확정 | Multi-round completeness 계약 위반 |
| Search cache를 verifier와 공유 | Independent verification 붕괴 |
| Audit feasible insertion을 자동 적용/재solve | 승인되지 않은 loop와 상태 변경 |
| `latest` profile/config/image tag 사용 | Reproducibility와 provenance 상실 |
| Apply/undo skeleton을 미리 core에 삽입 | `KEEP_COW`와 evidence-first gate 위반 |
| Pool route를 conversion 중 mutate | Alias, stale metric과 다음 snapshot 오염 |
| `(vehicle type, request set)` scalar minimum만 보존 | Concrete vehicle·terminal·다차원 objective의 안전하지 않은 dominance |
| Raw MIP selected columns를 `CommittedCandidate`로 cast | Overlap/vehicle conflict와 verifier 우회 |
| Generic solver에 `com.google.ortools.*` import | OR-Tools/native lifecycle이 core에 침투 |
| MIP no-incumbent/native-runtime failure를 empty solution으로 변환 | Valid ALNS incumbent 손실과 false success |

## 21. 남은 ADR와 decision backlog

이 문서는 다음 항목을 구현 전에 ADR로 닫을 것을 권장한다. ADR 작성은 질문 상태를 자동 변경하지 않는다.

| ADR candidate | 결정할 것 | 현재 상태/선행 evidence |
|---|---|---|
| `ADR-ARCH-001` | Maven module 이름, parent/aggregator와 architecture enforcement | 이 문서 `REVIEW` |
| `ADR-ARCH-002` | Canonical input/output serialization과 Jackson annotation 격리 | External contract 승인 필요 |
| `ADR-ARCH-003` | Profile registration: ServiceLoader vs generated catalog | Duplicate/order/reproducibility test |
| `ADR-ARCH-004` | Typed domain/propagation facet SPI | 실제 고객 seam 사례와 verifier 영향 |
| `ADR-ARCH-005` | Artifact canonical encoding, digest와 CAS model | Size/retention/security evidence |
| `ADR-ARCH-006` | Run state, lease, duplicate completion과 publication consistency | Fault/idempotency rehearsal |
| `ADR-ARCH-007` | AWS S3/Step Functions/Lambda adapter/deployment mapping | `Q-INFRA-01 RESOLVED`; local↔AWS parity, security/operations evidence |
| `ADR-ARCH-008` | AWS logical dispatcher의 concurrency/retry adapter mapping | `Q-BENCH-02` + downstream capacity; future provider는 별도 parity ADR |
| `ADR-ARCH-009` | Cancellation/recovery result external exposure | Product/error contract 승인 |
| `ADR-ARCH-010` | Approved provider adapter parity와 cutover/rollback | Golden manifest와 shadow evidence |
| `ADR-ARCH-011` | JPMS 사용 여부 | Dependency/toolchain compatibility |
| `ADR-ARCH-012` | AWS production cutover 또는 future provider substitution | AWS cutover evidence 또는 future provider parity·별도 승인 |
| `ADR-ARCH-013` | Route-selection production activation, exact partition vs compatibility mode와 profile projection 범위 | `C-17`, `RM-9A/B`, tiny-model/shadow evidence |
| `ADR-ARCH-014` | Worker-local hybrid 유지 vs cross-worker pool fan-in/central selector | `RM-9C` 뒤 artifact/CPU·memory bottleneck/admission/idempotency evidence |

별도 backlog:

- `Q-BENCH-02`: round/worker, warm-start assignment, `maxSteps`, watchdog calibration
- AWS production cutover: `RM-8` parity/shadow/rollback 및 운영 승인
- `Q-VAR-01`: Optional variant feasibility 시점/대상
- Multi-trip/rotation: Exact trip/reset/depot/resource contract
- Route pool/MIP production activation: Master §11.7~§11.10과 `RM-9A`~`RM-9C`; OR-Tools version/config/native packaging/Apache-2.0 notice·SBOM/security/operations/compute-cost/admission/fallback/rollback와 shadow decision

## 22. Traceability와 상태 보존

### 22.1 Requirement traceability

| Architecture area | Master | Domain | 질문/세션 |
|---|---|---|---|
| Input/domain/travel modules | §4, §5~§8, `RM-1` | §4~§9 | `Q-NUM-*`, `Q-MTX-*`, `Q-TIME-*`, `Q-IN-*` |
| Pair/propagation/search | §6, §11~§13, `RM-3~4` | §10~§12 | `Q-REQ-*`, `Q-ALG-01~02` |
| Optional pool/route-selection hybrid | §11.7~§11.10, `RM-9A~C` | §10.6~§10.9, §12.5~§12.8 | `C-17` production gate, `P-15~P-19` |
| Profile/policy extension | §9, `RM-2` | §12, §20 | `Q-OBJ-*`, `Q-COMP-*` |
| Verification/result | §10, §14.1, `RM-5` | §13~§16 | `Q-RES-*` |
| Multi-round application | §13~§14.4, `RM-6` | §14.3 | `Q-BENCH-01~03` |
| Logical ports/migration | §4, §15.10, §16 | §3, §17 | `Q-INFRA-01` AWS target + `RM-8` parity |
| Deferred variant | §16.3, `RM-9` | §17 | `Q-VAR-01` |

### 22.2 질문 상태

**`[CONTRACT]`** 이 Architecture Design 작성 뒤에도 질문 상태는 다음과 같다.

```text
RESOLVED 26
OPEN — EXPERIMENT_REQUIRED 1
DEFERRED 1
TOTAL 28
```

- `Q-ALG-02`: **`RESOLVED — KEEP_COW`**
- `Q-ALG-01`: **`RESOLVED`**. 4개 request-route 성장 정책 × 2 vehicle 순서, phase-1 screen과 phase-2 champion warm start가 확정됨
- `Q-BENCH-02`: **`OPEN — EXPERIMENT_REQUIRED`**, 공식 수치 없음
- `Q-INFRA-01`: **`RESOLVED`**. AWS S3 + Step Functions + Lambda가 target/reference runtime이며 SDK는 adapter/deployment 경계에만 위치
- `Q-VAR-01`: **`DEFERRED`**

### 22.3 최종 architecture summary

```text
stable:
  canonical domain
  prepared travel
  propagation/evaluation contracts
  COW search
  independent verification/result integrity

pluggable:
  customer profile/preset
  constraint/metric/score/objective/SolvePlan
  typed domain facet under approval
  input/output adapter
  solver-neutral route-selection backend

gated optional:
  immutable route pool
  exact-projectable route selection
  worker-local ALNS↔MIP hybrid
  OR-Tools CP-SAT/native optimizer adapter

portable:
  application use cases
  worker/round identity
  idempotency/cancellation
  artifact/result/provenance

replaceable:
  selected AWS adapter/deployment; future provider substitution is separately approved
  local filesystem/in-memory execution
```

구현의 기준은 “특정 cloud에서 실행된다”가 아니라 **같은 immutable semantic snapshot과 logical execution contract가 local runner와 향후 승인된 infrastructure adapter에서 같은 verified result 의미를 보존한다**는 것이다.

현재 placeholder는 이 target을 구현하지 않는다. Optional hybrid의 구현 완료는 generic API나 OR-Tools dependency의 존재가 아니라 `AR-H1`~`AR-H3`의 pool integrity, exact CP-SAT model, native/status fallback, full reconstruction evaluation과 shadow evidence로만 주장할 수 있다.
