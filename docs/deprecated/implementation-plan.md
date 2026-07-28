# RPDPTW 전체 구현 계획

```yaml
status: REVIEW
document_role: 현 상태에서 목표 Java 25/Maven multi-module RPDPTW 구현으로 이동하기 위한 상위 migration 계획
last_updated: 2026-07-24
source_baseline:
  master_design:
    version: 3.2-review
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    version: 1.1-review
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    version: 2.2-review
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
active_phases: AR-0..AR-10
deferred_follow_up: AR-11/RM-9
```

## 1. 목적과 사용법

이 문서는 현재 단일 Maven 애플리케이션을 설계 목표의 Java 25/Maven multi-module RPDPTW 시스템으로 옮기기 위한 **실행 순서, 계약, gate와 evidence**를 정의한다. 설계 요약이나 완료 보고가 아니다. 각 후속 LLM 세션은 이 문서를 시작점으로 자신의 phase 문서를 작성하고, 그 문서가 승인된 뒤 테스트 우선으로 구현해야 한다.

규범 원문은 다음 세 문서다.

- [Master Design](../master-design.md): 전체 의미, 불변조건, `RM-*` roadmap과 publication gate
- [Architecture Design](../architecture-design.md): Java 25/Maven module, package, logical port와 `AR-*` phase
- [Domain Design](../domain-design.md): 입력·정규화·travel·상태·전파·평가·결과의 상세 의미

결정 상태와 blocker의 단일 색인은 [Master Design open questions](../master-design-open-questions.md)다. 현재 구현, 실행법과 배포 자료는 [root POM](../../pom.xml), [README](../../README.md), [GCP README](../../gcp/README.md), [Win PoC fixture](../../data/win_poc_case.json)를 기준으로 조사했다.

이 문서의 phase는 Architecture Design §19.2의 `AR-0`~`AR-10`을 그대로 사용한다. 각 `AR-*`를 Master Design의 `RM-*`에 mapping하되 Architecture Design의 분할을 유지한다. `AR-11/RM-9`는 별도 승인 전 착수 금지인 deferred 후속 범위이며 이 문서의 phase 목록과 완료 범위에 포함하지 않는다.

## 2. Authority, 충돌 처리와 계획상 고정 이름

### 2.1 현재 문서 지위

[Master Design](../master-design.md), [Architecture Design](../architecture-design.md), [Domain Design](../domain-design.md)는 모두 `REVIEW`다. 따라서 세 문서와 이 계획은 아직 승인된 public API, wire schema, 저장 schema 또는 provider topology를 증명하지 않는다. 구현 세션은 `REVIEW` 문장을 이미 승인된 외부 계약처럼 인용해서는 안 된다.

충돌은 다음 순서로 처리한다.

1. 채택된 외부 입력·출력 계약과 승인된 Decision Record
2. `APPROVED` Master Design
3. `APPROVED` 상세 설계
4. 현재 `REVIEW` 상태의 Master/Architecture/Domain Design과 이 계획
5. `master-design-sessions`, 연구·역사 자료와 현재 legacy 구현

현재는 1~3의 승인 상태가 확인되지 않았으므로 세 설계 사이의 충돌을 구현으로 임의 해결하지 않는다. 의미 충돌은 `BLOCKED` evidence로 기록하고 영향 문서·질문·ADR을 같은 변경 단위에서 갱신한 뒤 재개한다. Architecture Design은 module/package 배치를, Domain Design은 domain 의미를 소유한다. 배치가 의미를 바꾸거나 의미가 추천 module dependency를 역전시키면 Master의 불변조건과 authority 절차로 되돌아간다.

### 2.2 계획상 고정 이름

아래 이름은 승인된 외부 API가 아니라 downstream 문서 간 모호성을 없애기 위한 **계획상 고정 이름**이다. 승인된 ADR이 같은 변경 단위에서 이 계획과 phase 문서를 갱신하지 않는 한 후속 세션은 임의로 바꾸지 않는다.

- Phase ID와 이 문서 §7의 phase 문서 경로
- Target Maven directory/artifact: `rpdptw/core` / `rpdptw-core`, `rpdptw/solver` / `rpdptw-solver`, `rpdptw/verification` / `rpdptw-verification`, `rpdptw/application` / `rpdptw-application`, `rpdptw/profiles/standard` / `rpdptw-profile-standard`, `adapters/common` / `rpdptw-adapter-common`, `apps/cli` / `rpdptw-cli`, `apps/api` / `rpdptw-api`, `apps/worker` / `rpdptw-worker`
- Base namespace `com.ronext.rpdptw`
- 단계 간 artifact 책임 이름 `CanonicalInput`, `ProblemInstance`, `PreparedTravel`, `BoundProfile`, `SolveSnapshot`, `ExecutionManifest`, `CommittedCandidate`, `VerifiedSolution`, `FinalResult`, `PublishableResult`
- Logical port 책임 이름 `SubmissionPort`, `ArtifactStore`, `RunStateRepository`, `WorkerDispatcher`, `WorkflowExecutionPort`, `CancellationPort`, `ProfileCatalogPort`, `SecretResolver`, `ResultPublisher`, `TelemetryPort`, `Clock`

이 이름들은 internal Java type 또는 interface 후보를 고정하지만 public HTTP field, method signature, serialization과 database 표현을 승인하지 않는다. Phase 문서가 더 구체적인 class/method 이름을 제안할 때는 반드시 `계획상 제안 API`라고 표시하고, 관련 ADR 또는 외부 계약의 승인 여부를 함께 기록한다.

### 2.3 가정과 금지 사항

이 계획은 다음만 가정한다.

- Java 25와 Maven 3.9.14 이상은 현재 build baseline으로 사용할 수 있다.
- 현재 working tree의 설계와 legacy 코드는 characterization 대상이며 사용자 소유 변경이다.
- Canonical wire schema가 확정되지 않아도 provider-neutral canonical test builder와 immutable core contract는 먼저 구현할 수 있다.
- Logical port interface, deterministic fake와 local adapter skeleton은 `AR-0` 뒤 core와 병행할 수 있다.

다음을 임의로 확정하거나 구현하지 않는다.

- `Q-BENCH-02`의 `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`와 watchdog 공식값
- `Q-INFRA-01`의 provider/product/deployment topology
- `Q-VAR-01`의 optional variant, multi-trip/rotation, route pool/MIP
- 현재 Win fixture의 소수 `D/U`를 반올림·절삭한 official input 또는 baseline
- `latest` profile/config, hidden official default, floating tolerance, numeric sentinel, runtime travel fallback
- Customer-name branch, provider SDK의 core/application 침투, search cache를 신뢰하는 verifier
- COW 병목과 별도 승인 없이 apply/undo skeleton 또는 전환
- 두 독립 verifier 중 하나를 생략한 publication, retrieval 또는 benchmark

## 3. 현 상태 inventory

### 3.1 Build와 source

현재 root [pom.xml](../../pom.xml)은 `packaging`을 생략한 단일 `jar` module이다.

| 영역 | 현재 상태 | Migration 해석 |
|---|---|---|
| Toolchain | `maven.compiler.release=25`, Maven Enforcer Java `[25,26)`, Maven `[3.9.14,)` | Target parent에서 보존 |
| Dependency | Google Workflow Executions, Cloud Storage, Jackson, JUnit이 root classpath에 직접 존재 | Provider/JSON/test 경계로 분리 |
| Packaging | Shade plugin이 `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer`를 main으로 한 단일 fat JAR 생성 | Target root는 `packaging=pom`; deployable app별 packaging으로 이동 |
| Source namespace | `com.ronext.optimizer` | Characterization 뒤 target `com.ronext.rpdptw`와 병존하다 cutover |
| Application core | `AlnsBatchEngine` 하나가 `Map<String,Object>`와 `double objective`를 반환하는 deterministic placeholder | Solver 의미 evidence가 아니며 legacy behavior만 보존 |
| HTTP API | `OptimizationApiController`가 SDK client를 직접 만들고 `gs://` input만 받아 Workflow 실행 | Submission/application/provider 책임을 분리 |
| Worker | `OptimizationWorkerController`가 SDK client와 engine을 직접 만들고 candidate를 GCS에 저장 | Worker app, logical port, provider adapter와 solver를 분리 |
| Finalization | prefix 아래 성공 candidate 중 `double objective` 최솟값을 고르고 `COMPLETED` 게시 | Complete-batch, 두 verifier와 CAS publication 계약으로 대체 |
| Runtime | `SERVICE_MODE=api|worker`로 한 JAR을 두 역할에 재사용 | `apps/api`, `apps/worker`, local/CLI artifact로 분리 |

현재 production source는 다음 6개 class뿐이다.

```text
src/main/java/com/ronext/optimizer/
├── application/AlnsBatchEngine.java
└── adapter/in/http/
    ├── HttpJson.java
    ├── JsonSupport.java
    ├── OptimizationApiController.java
    ├── OptimizationHttpServer.java
    └── OptimizationWorkerController.java
```

현재 테스트는 `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java`의 JUnit 5 test 1개뿐이다. 이 test는 `CANDIDATE`, run number와 양의 `double objective`만 확인하며 RPDPTW 입력, pair, travel, propagation, COW, verifier, publication 또는 reproducibility를 검증하지 않는다.

### 3.2 Runtime·GCP·fixture

- [README](../../README.md)는 Cloud Run API/worker, Google Cloud Workflows와 Cloud Storage를 현재 실행 기준으로 설명하고 `parallelRuns=8`, `iterationsPerRun=5000`, seed 예시를 제시한다. 이 값은 official `Q-BENCH-02` 값이 아니다.
- [GCP workflow](../../gcp/workflows/optimization.yaml)는 `parallelRuns`만큼 HTTP worker를 병렬 호출한 뒤 `/internal/finalize`를 한 번 호출한다. 선언 worker completeness, verified candidate, retry identity와 stable round champion을 보장하지 않는다.
- [GCP cloud build](../../gcp/cloudbuild.yaml), [GCP README](../../gcp/README.md), root [Dockerfile](../../Dockerfile)은 현재 topology의 characterization 자료다. `Q-INFRA-01`을 해결하거나 target provider를 승인하지 않는다.
- `data/win_poc_case.json`은 SHA-256 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`인 read-only fixture다. `D/U`가 소수 문자열이므로 integer meter/second 계약에 비준수다. Generic parser rejection test에는 사용할 수 있으나 official normalization, baseline 또는 comparison에는 사용할 수 없다.

### 3.3 Target multi-module inventory

최종 target은 다음 reactor다. 세부 domain 책임은 module 과분할 없이 `rpdptw-core`의 package로 나눈다.

```text
ro-next/
├── pom.xml                              # packaging=pom parent/aggregator
├── build/
│   ├── test-fixtures/                   # test-only builders/oracles
│   └── architecture-rules/              # dependency/package/bytecode rules
├── rpdptw/
│   ├── pom.xml
│   ├── core/                            # input/domain/normalization/travel/
│   │                                    # propagation/evaluation.*
│   ├── solver/                          # portfolio/search/state/termination
│   ├── verification/                    # candidate/finalization/result verification
│   ├── application/                     # use cases, ports, execution identity
│   └── profiles/
│       ├── pom.xml
│       └── standard/
├── adapters/
│   ├── pom.xml
│   └── common/                          # JSON + local adapters
├── apps/
│   ├── pom.xml
│   ├── cli/
│   ├── api/
│   └── worker/
├── gcp/                                 # legacy characterization only
└── docs/
```

허용 compile dependency는 다음 방향뿐이다.

```text
rpdptw-core
├── rpdptw-solver → core
├── rpdptw-verification → core          # solver dependency 금지
├── rpdptw-profile-* → core
└── rpdptw-application → core + solver + verification
    └── adapters/common → core + verification + application
        └── apps/* → application + selected adapters + selected profiles
```

`build/test-fixtures`는 production runtime scope에 들어가지 않는다. Provider adapter와 `deployment/`는 `AR-11/RM-9` 전에는 target tree에 추가하지 않는다.

## 4. 보존, characterization와 migration 전략

### 4.1 Legacy 보존 원칙

1. Phase implementation을 시작할 때 `git status --short`, 대상 file hash와 현재 baseline test를 기록한다.
2. 기존 `com.ronext.optimizer` source, current POM behavior, GCP/Docker/README를 characterization하기 전에는 이동·삭제·재작성하지 않는다.
3. `AR-0`에서 root를 aggregator로 바꿔야 할 때 기존 root app은 migration 전용 임시 module `legacy/current-app`으로 **내용 변경 없이 이동**한다. 이 경로는 계획상 고정 migration 이름이며 최종 target module이 아니다.
4. 이동 전후에 같은 input으로 `AlnsBatchEngine`의 placeholder output, HTTP validation/default, object key, workflow payload와 error mapping을 characterization test로 고정한다.
5. 새 target code는 `com.ronext.rpdptw`에서 작성한다. Legacy class를 새 core의 base class, interface 또는 fixture로 재사용하지 않는다.
6. `AR-1`~`AR-5` 동안 legacy endpoint/publication은 target 완료 evidence가 아니다. 새 core는 local test와 explicit adapter seam 뒤에서만 연결한다.
7. `AR-8`의 compatibility matrix, shadow comparison, versioned cutover와 rollback rehearsal를 통과하기 전에는 legacy 정상 경로를 교체하거나 삭제하지 않는다.
8. `AR-8`은 logical/application cutover까지만 소유한다. 현재 GCP topology의 provider migration이나 production deployment 변경은 `Q-INFRA-01` 승인 전 수행하지 않는다.

### 4.2 Characterization 대상

| 대상 | 먼저 고정할 관찰 | 바꾸지 말아야 할 이유 |
|---|---|---|
| `AlnsBatchEngine.run` | 같은 request/run/seed/iterations의 map key와 objective | Placeholder라도 현재 caller behavior의 기준 |
| `POST /optimizations` | `gs://` 검증, default/clamp, workflow argument와 202 body | External compatibility matrix 입력 |
| `GET /optimizations/{id}` | result object 부재 시 202, 존재 시 raw JSON 반환 | Retrieval migration 입력 |
| `/internal/batches` | required field, candidate object key, status | Worker adapter compatibility |
| `/internal/finalize` | prefix list, minimum double objective, result key와 status | 새 complete-batch/publication과 차이를 드러낼 기준 |
| Workflow YAML | run number, derived seed, retries, finalize 순서 | Logical coordinator가 대체해야 할 semantics |
| Docker/build | fat JAR name, main class, env와 image build | 최종 app별 packaging 전 rollback 기준 |

Characterization test는 목표 동작을 승인하지 않는다. 목표 계약과 다른 동작은 compatibility matrix에서 `LEGACY_ONLY`, `TARGET_ONLY`, `EQUIVALENT`, `INTENTIONAL_BREAK_REQUIRES_APPROVAL` 중 하나로 분류한다.

## 5. 관통 구현 불변조건

모든 phase는 자신의 범위뿐 아니라 다음 불변조건의 회귀를 막아야 한다.

1. **Java 25와 reactor:** Release 25, Maven/Java Enforcer, 재현 가능한 dependency/plugin version과 reactor DAG를 유지한다.
2. **Dependency direction:** Core에는 cloud/HTTP/provider SDK와 customer-name branch가 없고, verification은 solver/search/cache를 compile-depend하지 않는다.
3. **Immutable authority:** `ProblemInstance`, complete `PreparedTravel`, `BoundProfile`, `SolveSnapshot`과 publication artifact는 단계 handoff 뒤 불변이다.
4. **Atomic pair:** 모든 stable search state에서 request는 same-route complete pair 또는 `SearchRequestBank` 중 정확히 하나다.
5. **Complete prepared travel:** 모든 directed physical-location pair와 사용 vehicle별 travel time이 solve 전에 해소된다. Solver/verifier는 raw coordinate/speed fallback을 실행하지 않는다.
6. **Evaluation separation:** Hard feasibility, neutral metric, score, objective comparator와 `SolvePlan`은 서로 대체하지 않는다.
7. **COW:** Changed-route copy와 independent bank가 기본이다. Fail/reject/cancel/watchdog 시 candidate 전체를 discard한다.
8. **Immutable bound profile:** Exact customer/profile/version/preset/dependency closure와 fingerprint를 bind하고 unknown/latest/cross-customer fallback을 거부한다.
9. **두 독립 verifier:** Candidate verifier와 result-integrity verifier는 서로 다른 authority input을 사용하며 둘 다 cache-free다. 둘의 `PASS` 없이는 정상 publication/retrieval/benchmark가 없다.
10. **Logical provider-neutral port:** Application이 port를 소유하고 local/fake adapter가 구현한다. Provider resource type, URI, SDK client와 retry default가 core/application 의미를 정의하지 않는다.
11. **Exact reproducibility:** 고정 problem/travel/profile/config/build/seed/order/step의 정상 종료는 같은 trace, verified solution과 result fingerprint를 만든다.
12. **No hidden values:** Open/deferred 값을 숫자·provider·variant default로 만들지 않는다.

## 6. Phase DAG, 병렬 범위와 critical path

### 6.1 Dependency DAG

```text
AR-0
├── AR-1 → AR-2 → AR-3 → AR-4 → AR-5 → AR-6 → AR-7 → AR-8 → AR-9
│                                  └──────────────→ AR-10
└── AR-6 port/local skeleton ─────────────────────┘
```

- `AR-0` 뒤 `AR-6`의 port interface, deterministic fake, local artifact/state skeleton은 `AR-1`~`AR-5`와 병행할 수 있다.
- `AR-6` 전체를 `DONE`으로 만들려면 `AR-5`의 publishable verified result 경로가 필요하다. Skeleton이 존재한다는 이유로 publication/retrieval gate를 우회할 수 없다.
- `AR-7`의 coordinator state machine과 fake dispatcher skeleton도 `AR-0` 뒤 설계·test scaffold가 가능하지만, complete worker verification과 final publication exit evidence는 `AR-5`와 `AR-6` 뒤에만 성립한다.
- `AR-8`은 `AR-7`까지의 logical identity/port와 `AR-5`의 두 verifier를 모두 소비한다.
- `AR-9`는 `AR-8`과 logical official workflow가 준비되어도 `Q-BENCH-02` 승인 수치와 compliant integer `D/U` fixture가 없으면 `BLOCKED`다.
- `AR-10`은 `AR-4` COW baseline 뒤 준비할 수 있으나 대표 verified execution을 위해 `AR-5`가 필요하다. `AR-9`가 blocker에 걸려도 비공식·대표 fixture로 COW 유지 profiling은 진행할 수 있다.

### 6.2 Critical path

Publishable local solver의 critical path는 `AR-0 → AR-1 → AR-2 → AR-3 → AR-4 → AR-5 → AR-6`이다. Compatibility cutover까지는 `→ AR-7 → AR-8`이 추가된다. Official Win PoC까지는 외부 gate를 만족한 뒤 `→ AR-9`가 추가된다.

`AR-10`은 correctness/publication critical path가 아니며 COW 유지 또는 별도 제안의 decision branch다. `AR-9`의 외부 blocker를 해소하기 위해 `AR-10`의 profiling 결과나 임의 official 수치를 대신 사용할 수 없다.

## 7. Phase 목록과 RM mapping

| Phase | RM mapping | Phase 문서 | 핵심 결과 |
|---|---|---|---|
| `AR-0` | `RM-0` | [phase-00-baseline-and-build-architecture.md](phases/phase-00-baseline-and-build-architecture.md) | Baseline, parent/reactor, architecture enforcement |
| `AR-1` | `RM-1` | [phase-01-input-domain-and-travel.md](phases/phase-01-input-domain-and-travel.md) | Immutable problem과 complete prepared travel |
| `AR-2` | `RM-2` | [phase-02-propagation-evaluation-and-profiles.md](phases/phase-02-propagation-evaluation-and-profiles.md) | Full propagation/evaluation과 immutable bound profile |
| `AR-3` | `RM-3` | [phase-03-atomic-pair-and-initial-portfolio.md](phases/phase-03-atomic-pair-and-initial-portfolio.md) | Atomic pair evaluator와 최대 8개 initial candidate |
| `AR-4` | `RM-4` | [phase-04-cow-alns-and-reproducibility.md](phases/phase-04-cow-alns-and-reproducibility.md) | COW ALNS, termination과 reproducibility |
| `AR-5` | `RM-5` | [phase-05-verification-finalization-and-publication.md](phases/phase-05-verification-finalization-and-publication.md) | 두 verifier와 publishable verified result |
| `AR-6` | `RM-8-local` | [phase-06-local-application-and-logical-ports.md](phases/phase-06-local-application-and-logical-ports.md) | Local application과 provider-neutral ports |
| `AR-7` | `RM-6-logical` | [phase-07-logical-multi-round-coordinator.md](phases/phase-07-logical-multi-round-coordinator.md) | Logical multi-round coordinator와 fake dispatcher |
| `AR-8` | `RM-8-cutover` | [phase-08-compatibility-migration-and-cutover.md](phases/phase-08-compatibility-migration-and-cutover.md) | Compatibility, shadow, logical cutover와 rollback |
| `AR-9` | `RM-6-official` | [phase-09-official-win-poc-workflow.md](phases/phase-09-official-win-poc-workflow.md) | Approved manifest 기반 official Win PoC |
| `AR-10` | `RM-7` | [phase-10-cow-profiling-decision.md](phases/phase-10-cow-profiling-decision.md) | COW 유지 또는 별도 변경 제안 |

`AR-11/RM-9`는 표와 phase 문서 목록에 의도적으로 없다.

## 8. 공통 테스트 우선·evidence 정책

### 8.1 모든 구현 변경의 순서

각 observable behavior는 다음 순서를 별도 commit 또는 명확한 작업 로그로 증명한다.

1. 기대 behavior와 권위 근거를 test name/table에 연결한다.
2. Production code보다 먼저 가장 작은 failing test를 작성한다.
3. 지정 Maven 명령으로 test가 의도한 이유로 실패하는 것을 확인하고 failure message/report를 보존한다.
4. 그 test를 통과시키는 최소 구현만 작성한다.
5. 같은 명령이 통과함을 확인한다.
6. 대상 module의 전체 unit/property/architecture test를 실행한다.
7. `mvn verify` 또는 `-pl ... -am verify`로 선행 module 회귀를 확인한다.
8. Diff scope, 금지 dependency/value 검색과 evidence manifest를 갱신한다.

Compile error는 API skeleton을 처음 만드는 test에서만 유효한 첫 실패다. 의미 test는 가능하면 compile 가능한 test double/fixture를 먼저 만든 뒤 assertion failure를 관찰한다. “테스트가 없어서 실패”, 환경 장애, dependency download 실패와 unrelated test failure는 올바른 red evidence가 아니다.

### 8.2 Test 종류

| 종류 | 목적 | 대표 phase |
|---|---|---|
| Unit | Exact decimal/time/comparator/state transition의 작은 사례 | `AR-1`, `AR-2`, `AR-7` |
| Property | ID bijection, pair partition, comparator law, mutation round-trip 성질 | `AR-1`~`AR-4` |
| Architecture | Module/package dependency, SDK/customer-name/internal package 침투 | `AR-0` 이후 전 phase |
| Integration | Module/port/local end-to-end와 artifact identity | `AR-5`~`AR-8` |
| Corruption | Route/bank/travel/cache/outcome/audit/payload의 독립 손상 거부 | `AR-5` |
| Fault | Exception/cancel/watchdog/resource/retry/duplicate/CAS에서 atomicity 보존 | `AR-4`, `AR-6`~`AR-8` |
| Reproducibility | Fixed envelope 반복에서 trace/solution/result fingerprint 일치 | `AR-4`, `AR-7`, `AR-9` |
| Benchmark | Performance 관측 또는 승인 manifest quality 비교; correctness 대체 금지 | `AR-9`, `AR-10` |

### 8.3 Maven 명령 원칙

현재 baseline 명령은 다음이다.

```bash
mvn -version
mvn test
mvn verify
```

Multi-module 전환 뒤 phase 세션은 다음 pattern을 사용한다.

```bash
mvn -pl <module-path> -am -Dtest=<FirstFailingTest> -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl <module-path> -am test
mvn -pl <module-path> -am verify
mvn verify
```

`-Dsurefire.failIfNoSpecifiedTests=false`는 `-am`으로 함께 빌드되는 선행 module에 해당 이름의 test가 없을 때만 사용한다. 대상 owner module에 test가 실제 존재하고 red/green으로 실행됐는지는 Surefire report로 별도 확인해야 한다. Failsafe `*IT`는 `verify`에서, unit/property/corruption test는 Surefire `test`에서 실행한다. Architecture rule과 reproducibility suite는 skip되지 않는 reactor `verify` gate에 둔다. Official benchmark나 장시간 profiling은 별도 explicit profile/command로 실행할 수 있지만 일반 correctness test를 대체하지 않는다.

## 9. Phase별 실행 계약

아래 모든 phase의 “First failing tests”는 §8.1의 동일한 순서, 즉 **test 작성 → 의도한 assertion/architecture failure 확인 → 최소 구현 → targeted green → module 회귀 → reactor 회귀**로 실행한다. Compile 또는 환경 오류만 관찰하고 최소 구현으로 넘어갈 수 없다.

### 9.1 `AR-0 / RM-0` — Baseline과 build architecture

**목적:** 현재 동작과 toolchain을 보존한 채 root를 Java 25 parent/aggregator로 전환하고 이후 module의 dependency 위반을 즉시 차단한다.

**Entry gate와 선행 phase:** 선행 phase 없음. 세 REVIEW 설계의 version/hash, working tree status, Java 25/Maven 3.9.14과 현 `mvn test` 결과를 기록해야 한다. 사용자 소유 변경이 겹치면 임의 수정하지 않고 대상 overlap만 `BLOCKED`로 보고한다.

**정확한 구현 범위/모듈:**

- Root `pom.xml`의 parent/aggregator 전환과 dependency/plugin management
- `rpdptw`, `rpdptw/profiles`, `adapters`, `apps` aggregator POM
- Leaf skeleton `rpdptw-core`, `rpdptw-solver`, `rpdptw-verification`, `rpdptw-application`, `rpdptw-profile-standard`, `rpdptw-adapter-common`, `rpdptw-cli`, `rpdptw-api`, `rpdptw-worker`
- `build/test-fixtures`, `build/architecture-rules`
- 임시 `legacy/current-app`로 현 source/test/dependency/shade packaging 보존
- Enforcer banned dependency, package/module architecture rule, test-fixture leakage와 reactor cycle 검사

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `LegacyAlnsBatchEngineCharacterizationTest` | 현재 seed/run/iterations에 대한 map contract가 아직 fixture로 고정되지 않음 | Unit/characterization |
| `ReactorStructureTest` | 필수 module/artifact 또는 root `packaging=pom` 누락을 정확히 열거 | Architecture |
| `ForbiddenDependencyArchitectureTest` | Core/solver/verification에 삽입한 test-only cloud dependency를 탐지 | Architecture |
| `InternalPackageBoundaryTest` | 다른 module의 `.internal` test fixture 참조를 탐지 | Architecture |

권장 red/green 명령:

```bash
mvn -Dtest=LegacyAlnsBatchEngineCharacterizationTest test
mvn -pl build/architecture-rules -am -Dtest=ReactorStructureTest,ForbiddenDependencyArchitectureTest test
mvn verify
```

**Deliverables:** Build 가능한 reactor, legacy behavior report, dependency DAG, toolchain report, architecture violation report와 후속 module용 empty production skeleton.

**Exit evidence:** 기존 test와 characterization 통과, 모든 module을 포함한 reactor `mvn verify` 통과, core/solver/verification cloud dependency 0, cycle 0, legacy app output parity, 변경 전후 artifact/main-class rollback 정보.

**완료/중단 조건:** 모든 evidence가 있으면 `DONE`. 기존 behavior를 옮기면서 바꾸거나 root 변경이 사용자 변경을 덮어쓰면 즉시 중단한다. Module 이름/방향이 이 계획과 달라야 하면 ADR과 계획 갱신 전에는 진행하지 않는다.

**다음 phase handoff:** `AR-1`에 parent convention, test fixture API와 core package skeleton을 넘긴다. `AR-6`의 port/local skeleton 작업도 같은 parent 위에서 병행할 수 있다.

### 9.2 `AR-1 / RM-1` — Input, domain과 prepared travel

**목적:** Raw 표현을 재해석하지 않는 immutable `ProblemInstance`와 solver/verifier가 공유할 complete `PreparedTravel`을 만든다.

**Entry gate와 선행 phase:** `AR-0 DONE`. Canonical public wire schema가 미승인인 상태는 core contract/test builder를 막지 않지만 external JSON을 canonical이라고 선언하는 작업은 금지한다.

**정확한 구현 범위/모듈:**

- `rpdptw-core`의 `input`, `domain`, `normalization`, `travel`
- Dense external↔core ID mapping, request/pickup/delivery/location/vehicle/terminal
- Item-first `n=3/FLOOR`, integer cost/distance/time rejection, checked arithmetic
- Planning `[start,end)`, inclusive close, repeating/overnight window, service/ownership/size/capability/zone
- Oneway와 single roundtrip, unsupported rotation
- Provided/generated directed travel, self `0/0`, Great Circle policy hook, vehicle-resolved generated `U`
- Immutable provenance와 fingerprints

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `FixedPointNormalizerTest.itemFirstFloorPrecedesQuantity` | Line-first 계산과 다른 hand-calculated 정수를 얻지 못함 | Unit |
| `TimeNormalizerTest.preservesHalfOpenPlanAndInclusiveClose` | plan end event를 허용하거나 close event를 거부 | Unit |
| `RequestPairPropertiesTest.everyRequestHasExactlyTwoBoundNodes` | Partial/duplicate/reference mismatch 생성이 거부되지 않음 | Property |
| `CompatibilityNormalizerTest.composesSizeCapabilityAndZone` | 세 축 중 하나를 무시하거나 `ALL`을 잘못 해석 | Unit/property |
| `PreparedTravelTest.completesEveryDirectedVehicleViewBeforeSolve` | 누락 arc/vehicle time을 남기거나 reverse fallback을 사용 | Unit/property |
| `PreparedTravelTest.rejectsDecimalWinFixtureForOfficialUse` | 현재 fixture의 소수 `D/U`가 허용됨 | Integration/negative |

권장 명령:

```bash
mvn -pl rpdptw/core -am -Dtest=FixedPointNormalizerTest,TimeNormalizerTest,RequestPairPropertiesTest,PreparedTravelTest test
mvn -pl rpdptw/core -am verify
```

**Deliverables:** `CanonicalInput` internal contract, immutable `ProblemInstance`, complete `PreparedTravel`, dense mapping, normalization/travel provenance와 canonical fingerprints.

**Exit evidence:** Numeric/time hand calculation, boundary/overflow rejection, ID bijection/pair/reference property, delivery-only와 real pair fixture, size/capability/zone cases, provided/generated/self/asymmetric travel coverage, solver/verifier consumer가 같은 travel fingerprint를 받는 test.

**완료/중단 조건:** 모든 required directed pair와 사용 vehicle time이 solve 전에 해소되고 실패가 typed pre-solve error면 `DONE`. Earth radius/library default, external schema, decimal coercion 또는 rotation 의미를 새로 확정해야 하면 해당 부분을 중단하고 결정 gate로 올린다.

**다음 phase handoff:** `ProblemInstance`, `PreparedTravel`, fact vocabulary, test builders와 fingerprint contract를 `AR-2`, `AR-3`, `AR-5`에 넘긴다.

### 9.3 `AR-2 / RM-2` — Propagation, evaluation과 profiles

**목적:** 물리 전파에서 고객 가격·목적을 분리하고 exact profile/preset을 immutable `BoundProfile`로 bind한다.

**Entry gate와 선행 phase:** `AR-1 DONE`. Hand-calculated problem/travel fixture와 exact unit/fingerprint를 사용할 수 있어야 한다.

**정확한 구현 범위/모듈:**

- `rpdptw-core`의 `propagation`, `evaluation.api`, `evaluation.runtime`, `evaluation.insertion`의 API seam
- Load/time/window/wait/rest/stop/drive resource full propagation
- Structural/static hard gate, neutral metric, hard constraint, score, objective vector/comparator, `SolvePlan`
- Exact profile/customer/version/preset/dependency closure와 immutable binding
- `rpdptw/profiles/standard`의 고객 중립 공통 catalog 및 test profile
- Missing/duplicate/unit/schema/cross-customer/`latest` rejection

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `RoutePropagatorTest.restartsWholeArcAtNextWorkWindow` | Partial driving을 누적하거나 rest breakdown이 hand result와 다름 | Unit |
| `RoutePropagatorTest.propagatesMixedDeliveryOnlyAndRealPairs` | Load prefix 또는 terminal/service pattern이 잘못됨 | Unit/property |
| `ProfileBinderTest.rejectsMissingDuplicateAndUnitMismatchedDependencies` | 잘못된 profile이 fallback으로 bind됨 | Unit |
| `CustomerProfileIsolationTest.deniesCrossCustomerPreset` | 다른 customer preset 또는 `latest`가 resolve됨 | Architecture/integration |
| `LexicographicComparatorPropertiesTest.isTransitiveAndUsesStableTieOrder` | Transitivity/antisymmetry/stable total order 위반 | Property |
| `EvaluationLayerArchitectureTest.keepsPriceOutOfPropagationAndMetrics` | Propagator/metric이 score/customer implementation을 참조 | Architecture |

권장 명령:

```bash
mvn -pl rpdptw/core,rpdptw/profiles/standard -am -Dtest=RoutePropagatorTest,ProfileBinderTest,LexicographicComparatorPropertiesTest test
mvn -pl rpdptw/core,rpdptw/profiles/standard -am verify
```

**Deliverables:** Full propagation/evaluation API, exact comparator, immutable `BoundProfile`, profile descriptor/fingerprint와 표준 test profile.

**Exit evidence:** Window policy, full-arc restart, service/wait/rest/stop/resource hand cases, hard-vs-score 분리, dependency rejection, customer isolation, comparator law, stage guard와 immutable/no-shared-scratch evidence.

**완료/중단 조건:** Algorithm이 raw/customer field 없이 bound interface만 소비할 수 있으면 `DONE`. 새 physical fact가 필요한데 기존 seam으로 표현할 수 없으면 typed facet ADR과 verifier 영향 승인 전 중단한다.

**다음 phase handoff:** `BoundProfile`, propagation/evaluation service, side-effect-free insertion seam, comparator/plan contract를 `AR-3`과 `AR-5`에 넘긴다.

### 9.4 `AR-3 / RM-3` — Atomic pair와 initial portfolio

**목적:** 하나의 side-effect-free pair evaluator로 모든 insertion 의미를 통일하고 4×2 최대 8개 독립 initial candidate를 생성한다.

**Entry gate와 선행 phase:** `AR-2 DONE`; `AR-1` authority artifact와 stable comparator가 준비되어야 한다.

**정확한 구현 범위/모듈:**

- `rpdptw-core/evaluation.insertion`의 pickup/delivery position pair enumeration/evaluation
- `rpdptw-solver/portfolio`의 `CLOCK`, `SEQ_FARTHEST`, `SEQ_LARGE_DEMAND`, `SEQ_EARLIEST_DEADLINE`
- `DIRECT_FIRST_LARGE`, `DIRECT_FIRST_SMALL`
- 독립 route/bank/cache/random state, stable tie-break와 route artifact
- `CLOCK` coordinate unavailable 처리와 나머지 조합 지속
- 선택 move의 atomic apply 전후 full recomputation equality

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `AtomicPairInsertionEvaluatorTest.neverRanksPartialOrInfeasiblePair` | Partial pair 또는 hard-infeasible option이 ranking에 나타남 | Unit/property |
| `AtomicPairInsertionEvaluatorTest.matchesCacheFreeAppliedRoute` | 선택 option apply 후 full propagation과 다름 | Property |
| `InitialPortfolioBuilderTest.emitsAllAvailableFourByTwoCombinations` | 조합 identity/trace가 누락 또는 공유됨 | Unit/integration |
| `InitialPortfolioBuilderTest.marksClockUnavailableWithoutFallback` | 좌표 누락을 다른 순서로 조용히 대체 | Unit |
| `PortfolioCandidateIsolationTest.failedInsertionLeavesNoObservableMutation` | Route/bank/cache/fingerprint 중 하나가 변함 | Fault/property |

권장 명령:

```bash
mvn -pl rpdptw/solver -am -Dtest=AtomicPairInsertionEvaluatorTest,InitialPortfolioBuilderTest,PortfolioCandidateIsolationTest test
mvn -pl rpdptw/solver -am verify
```

**Deliverables:** Atomic pair option contract, 최대 8개 independent `CommittedCandidate` 초기 snapshot, source policy/config/ordered route/fingerprint artifact.

**Exit evidence:** 모든 available 조합 trace, request/vehicle/route/position stable tie 사례, `CLOCK UNAVAILABLE`, failed insertion rollback, candidate isolation, evaluator/full recomputation equality와 artifact fingerprint.

**완료/중단 조건:** Feasibility 구현이 policy마다 중복되지 않고 partial pair가 stable state에 노출되지 않으면 `DONE`. Utilization의 missing/zero 처리에 승인되지 않은 의미가 필요하면 explicit policy version/결정 전 해당 ordering을 중단한다.

**다음 phase handoff:** Verified initial candidates, atomic pair mutation boundary와 route artifacts를 `AR-4`에 넘긴다.

### 9.5 `AR-4 / RM-4` — COW ALNS와 reproducibility

**목적:** Changed-route COW, exact step termination과 stable ordering으로 deterministic normal ALNS execution을 만든다.

**Entry gate와 선행 phase:** `AR-3 DONE`; phase-1/worker test에는 explicit 양의 test-only step config를 사용한다. 이 숫자를 official default나 `Q-BENCH-02` 답으로 기록하지 않는다.

**정확한 구현 범위/모듈:**

- `rpdptw-solver/state`: immutable current/stageBest/solveBest, changed-route COW, independent bank, cache invalidation
- `rpdptw-solver/search`: pair destroy/repair, bounded improvement, stage guard, acceptance, adaptive update
- `rpdptw-solver/termination`: completed-step, watchdog/cancel/resource/failure 분리
- Phase-1 candidate screen과 stable champion selection의 solver-side execution
- Worker-run exact seed/config/step lineage와 canonical trace
- Cache hit/miss와 cache-free full recomputation equality

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `CowCandidateStateTest.rejectLeavesCurrentBestAndFingerprintUntouched` | Rejected candidate의 route/bank/cache가 committed snapshot에 보임 | Property |
| `CowCandidateStateFaultTest.discardsOnExceptionCancellationAndWatchdog` | 미완료 step이 state/counter/adaptive value를 전진 | Fault |
| `AlnsStepAccountingTest.countsOnlyFullyCompletedSteps` | Destroy 이후 중단된 step을 completed로 계산 | Unit/fault |
| `CacheFreeEvaluationEquivalenceTest.matchesAcrossHitMissAndPoisonedCache` | Cache 상태에 따라 objective/feasibility가 달라짐 | Corruption/property |
| `PhaseOneScreenTest.selectsChampionAfterEveryAvailableCandidateCompletes` | 일부 완료 또는 completion order가 champion을 바꿈 | Integration |
| `AlnsReproducibilityTest.repeatsTraceSolutionAndFingerprint` | Fixed envelope 반복 결과가 다름 | Reproducibility |

권장 명령:

```bash
mvn -pl rpdptw/solver -am -Dtest=CowCandidateStateTest,CowCandidateStateFaultTest,AlnsStepAccountingTest,AlnsReproducibilityTest test
mvn -pl rpdptw/solver -am verify
```

**Deliverables:** COW search engine, phase-1 champion, exact worker result, termination/provenance, canonical step trace와 reproducibility record.

**Exit evidence:** Accept/reject/exception/cancel/watchdog fault injection, current/best isolation, exact step/seed/operator record, cache corruption 무관성, stable tie/reduction, fixed-envelope 반복 fingerprint equality.

**완료/중단 조건:** 정상 `MAX_STEPS_REACHED`와 예외 종료가 구분되고 미완료 mutation이 0이면 `DONE`. Wall-clock을 quality termination으로 쓰거나 apply/undo를 도입해야만 진행 가능하다는 주장은 중단 사유이며 `AR-10` evidence/별도 승인 전 허용하지 않는다.

**다음 phase handoff:** Cache를 제외한 committed route/bank authority, termination/trace와 reproducibility envelope를 `AR-5`; worker execution contract를 `AR-7`; COW counters를 `AR-10`에 넘긴다.

### 9.6 `AR-5 / RM-5` — Verification, finalization과 publication

**목적:** Search와 finalizer의 주장을 독립적으로 검증하여 두 gate를 모두 통과한 결과만 publishable하게 만든다.

**Entry gate와 선행 phase:** `AR-4 DONE`; `AR-1` problem/travel과 `AR-2` evaluation declaration을 verifier가 solver를 거치지 않고 읽을 수 있어야 한다.

**정확한 구현 범위/모듈:**

- `rpdptw-verification`의 `verification.api`, `verification.candidate`
- `result.api`, `result.finalization`, required insertion audit와 diagnostic evidence
- `verification.result`, canonical payload/result fingerprint
- Candidate `PASS` 전 finalization 차단, result `PASS` 전 publication 차단
- Verification module의 solver/search/cache compile dependency 금지

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `CandidateSolutionVerifierCorruptionTest.rejectsEachAuthorityInputCorruption` | Pair/terminal/travel/metric/objective 손상 중 하나를 통과 | Corruption |
| `CandidateSolutionVerifierCorruptionTest.ignoresPoisonedSearchCache` | Cache 값이 report에 영향을 줌 | Corruption/architecture |
| `FinalResultInsertionAuditTest.auditsEveryNonStaticUnassignedRequest` | Eligible vehicle/position 누락 또는 feasible 발견 자동 적용 | Integration |
| `ResultIntegrityVerifierCorruptionTest.rejectsOutcomeAuditSummaryAndPayloadMismatch` | Exactly-one/confidence/summary/digest 손상 중 하나를 통과 | Corruption |
| `PublicationGateTest.requiresBothIndependentPassReports` | 한 `PASS`, fail/incomplete report로 정상 결과가 노출 | Integration/fault |
| `VerifierDependencyArchitectureTest.forbidsSolverAndSearchPackages` | Verification bytecode/classpath가 solver를 참조 | Architecture |

권장 명령:

```bash
mvn -pl rpdptw/verification -am -Dtest=CandidateSolutionVerifierCorruptionTest,FinalResultInsertionAuditTest,ResultIntegrityVerifierCorruptionTest,PublicationGateTest test
mvn -pl rpdptw/verification -am verify
```

**Deliverables:** `VerifiedSolution`, candidate report, exactly-one outcomes, audit/diagnostics/summary, result-integrity report와 `PublishableResult`.

**Exit evidence:** Hand oracle 또는 small exhaustive oracle, authority input별 독립 corruption matrix, stale cache 무관성, static `PROVEN`과 required audit 분기, confidence ceiling, feasible insertion no-auto-fix, outcome/ownership/summary/payload corruption rejection와 gate별 publication block.

**완료/중단 조건:** 두 verifier가 독립 compile/runtime 경계를 갖고 둘 다 `PASS`인 payload만 publishable하면 `DONE`. 한 verifier를 solver summary/finalizer 판단으로 대체하거나 shared cache가 필요하면 중단한다.

**다음 phase handoff:** `PublishableResult`, both-pass report, rejection/recovery와 canonical artifact identity를 `AR-6`~`AR-9`에 넘긴다.

### 9.7 `AR-6 / RM-8-local` — Local application과 logical ports

**목적:** Provider와 무관한 application use case/port를 local reference runtime에서 실행하고 두 verification gate를 end-to-end로 보존한다.

**Entry gate와 선행 phase:** Port/fake skeleton은 `AR-0 DONE` 뒤 병행 가능. Phase exit는 `AR-5 DONE`이 필수다.

**정확한 구현 범위/모듈:**

- `rpdptw-application`의 inbound use case, outbound port, run identity/state
- `adapters/common`의 JSON mapping과 local immutable artifact/state/dispatcher/publisher
- `apps/cli`, `apps/worker`, 필요 최소 `apps/api` composition root
- Digest-before-deserialization, atomic create/rename, CAS publication
- Idempotent submit, cooperative cancellation, verified retrieval와 structured telemetry
- Secret/provider locator가 core artifact identity에 들어가지 않는 경계

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `ArtifactStoreContractTest.rejectsDigestMismatchBeforeDeserialization` | 손상 bytes를 deserialize하거나 immutable object를 덮어씀 | Contract/corruption |
| `RunStateRepositoryContractTest.enforcesExpectedVersionCas` | Stale version update가 성공 | Contract/fault |
| `LocalSolveEndToEndIT.publishesOnlyAfterBothVerifierPass` | Raw candidate 또는 single-pass result가 조회됨 | Integration |
| `LocalCancellationIT.discardsIncompleteCandidateAndPreservesActualTermination` | Cancellation을 정상 완료로 표시하거나 partial state 보존 | Fault |
| `VerifiedResultRetrievalIT.distinguishesRunningRejectedAndPublished` | 미검증 payload를 정상 result로 반환 | Integration |

권장 명령:

```bash
mvn -pl rpdptw/application,adapters/common,apps/cli,apps/worker -am -Dtest=ArtifactStoreContractTest,RunStateRepositoryContractTest test
mvn -pl apps/cli,apps/worker -am verify
```

**Deliverables:** Provider-neutral ports, local/fake implementations, CLI/worker reference runtime, immutable artifact refs, idempotent state와 verified retrieval.

**Exit evidence:** Local input→snapshot→search→두 verifier→publication end-to-end, digest corruption, duplicate submit, same-key/different-digest conflict, cancellation, CAS conflict, atomic local artifact와 deterministic rerun.

**완료/중단 조건:** Local result가 provider 없이 같은 semantic identity와 both-gate를 보존하면 `DONE`. Cloud URI/event/SDK type가 port signature에 필요하거나 verifier 전 result 노출이 필요하면 중단한다.

**다음 phase handoff:** `ExecutionManifest`/artifact/state/dispatch/publication port와 deterministic fake를 `AR-7`; versioned application seam을 `AR-8`에 넘긴다.

### 9.8 `AR-7 / RM-6-logical` — Logical multi-round coordinator

**목적:** 공식 수치나 provider 없이 declared worker completeness, stable fan-in, retry identity와 multi-round 상태 전이를 검증한다.

**Entry gate와 선행 phase:** 전체 exit에는 `AR-5`, `AR-6 DONE`. Skeleton은 `AR-0` 뒤 가능하다. Test config 수치는 fixture-local이고 official default가 아님을 manifest에 표시해야 한다.

**정확한 구현 범위/모듈:**

- `rpdptw-application/execution`의 run/round/worker/attempt identity와 state machine
- Phase-1 available candidate completeness와 champion fan-in
- Phase-2 declared `WorkerAssignment`, stable completion-order-independent aggregation
- Same logical worker retry, digest convergence/conflict, incomplete round rejection
- Strictly-better next-round warm start, equal/worse `NO_STRICT_IMPROVEMENT`, configured max round
- Fake dispatcher/state/artifact integration; physical provider 없음

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `MultiRoundCoordinatorTest.waitsForEveryDeclaredVerifiedWorker` | 일부 성공 worker만으로 champion/next round 확정 | Integration/fault |
| `CompletionOrderIndependenceTest.selectsSameChampionForEveryPermutation` | Completion order에 따라 champion/digest가 다름 | Property/reproducibility |
| `WorkerRetryIdentityTest.preservesSeedWarmStartConfigAndRunId` | Retry가 logical identity를 바꿈 | Unit/fault |
| `DuplicateWorkerCompletionTest.rejectsSameIdentityDifferentDigest` | 상충 success 중 하나를 임의 선택 | Corruption/fault |
| `IncompleteRoundRejectionTest.neverPublishesBenchmarkVector` | Missing/failed/unverified worker가 있는데 정상 result 생성 | Integration |
| `OfficialManifestGuardTest.rejectsMissingApprovedNumericContract` | Official mode가 hidden 수치를 채움 | Unit/negative |

권장 명령:

```bash
mvn -pl rpdptw/application -am -Dtest=MultiRoundCoordinatorTest,CompletionOrderIndependenceTest,WorkerRetryIdentityTest,IncompleteRoundRejectionTest test
mvn -pl rpdptw/application,adapters/common -am verify
```

**Deliverables:** Logical coordinator, deterministic fake dispatcher, state transition/audit record, round champion와 next-round lineage.

**Exit evidence:** 모든 completion permutation, duplicate/retry/digest mismatch, cancel/timeout/incomplete cases, stable comparator/tie, warm-start lineage, no-hidden-official-value guard와 fixed test-manifest rerun.

**완료/중단 조건:** Arbitrary explicit test manifest에서 logical contract가 완결되고 official mode가 승인 수치 없이는 거부되면 `DONE`. Provider 선택 또는 일부-success fallback이 필요하면 중단한다.

**다음 phase handoff:** Logical run/round/worker contract와 compatibility-testable ports를 `AR-8`; official manifest/workflow seam을 `AR-9`에 넘긴다.

### 9.9 `AR-8 / RM-8-cutover` — Compatibility migration과 logical cutover

**목적:** Legacy 의미를 versioned adapter로 제한하고 target application과 shadow 비교한 뒤 recoverable logical cutover를 증명한다.

**Entry gate와 선행 phase:** `AR-5`, `AR-6`, `AR-7 DONE`. 승인된 external API가 없는 항목은 compatibility matrix에서 unresolved로 남기며 임의 cutover하지 않는다.

**정확한 구현 범위/모듈:**

- `adapters/common`의 versioned legacy input/output mapping과 explicit coercion/alias provenance
- `rpdptw-application`의 idempotency/cancellation/status/retrieval compatibility
- `apps/api` 또는 local API harness의 versioned route/composition
- Side-effect-free shadow runner, semantic compatibility matrix, artifact comparison
- Logical feature/version cutover, rollback pointer와 rehearsal
- `legacy/current-app` 유지/격리/제거 판단
- GCP SDK/topology/deployment 변경은 제외

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `LegacyEndpointCharacterizationTest` | AR-0 baseline과 current legacy response/error가 다름 | Characterization |
| `LegacyInputAdapterCompatibilityTest.recordsEveryAliasAndRejectsAmbiguity` | Silent alias/coercion 또는 raw default가 발생 | Unit/integration |
| `ShadowComparisonIT.classifiesEverySemanticDifference` | Unclassified field/status/metric 차이가 있음 | Integration |
| `CutoverRollbackIT.restoresPreviousLogicalVersionWithoutArtifactLoss` | Rollback 뒤 retrieval/idempotency가 깨짐 | Fault/integration |
| `IdempotencyCancellationCompatibilityIT.preservesIntentAndActualTermination` | Retry/cancel 의미를 legacy status로 뭉갬 | Fault |
| `CutoverPublicationGateIT.blocksUnverifiedTargetAndLegacyCandidate` | 어느 경로든 both-pass 없이 정상 publication | Integration |

권장 명령:

```bash
mvn -pl adapters/common,apps/api -am -Dtest=LegacyInputAdapterCompatibilityTest test
mvn -pl apps/api,apps/worker -am verify
```

**Deliverables:** Semantic compatibility matrix, versioned adapter, shadow report, logical cutover/rollback plan과 rehearsal evidence, legacy module 처분 decision.

**Exit evidence:** Current endpoint characterization parity, intentional break 승인 record, raw→canonical→result lineage, retry/idempotency/cancellation fault matrix, shadow artifact diff, both-gate target publication, rollback 후 동일 artifact identity.

**완료/중단 조건:** 모든 semantic difference가 승인·분류되고 logical version switch/rollback이 재현되면 `DONE`. Provider resource migration, public wire break 또는 production deployment가 필요하면 `Q-INFRA-01`/외부 계약 승인 전 중단한다. `legacy/current-app` 삭제는 rollback 기간과 approval을 충족한 경우에만 수행한다.

**다음 phase handoff:** Approved compatibility contract와 logical workflow entry를 `AR-9`에 넘긴다. Physical provider cutover는 이 계획 밖의 deferred `AR-11/RM-9`다.

### 9.10 `AR-9 / RM-6-official` — Official Win PoC workflow

**목적:** 승인된 수치와 compliant integer fixture만 사용하는 immutable manifest로 exact Win comparator와 complete verified multi-round run을 실행한다.

**Entry gate와 선행 phase:** `AR-8 DONE` 및 다음 두 blocker가 모두 해소되어야 한다.

1. `Q-BENCH-02` calibration 결과와 `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`, watchdog이 명시적으로 승인됨
2. Integer meter/second 계약을 만족하는 Win fixture/matrix와 새 digest가 제공됨

둘 중 하나라도 없으면 이 phase는 `BLOCKED`이며 official baseline, challenger verdict와 production default를 만들지 않는다.

**정확한 구현 범위/모듈:**

- `rpdptw-application` official manifest validation과 immutable comparison card
- `apps/worker` 또는 provider-neutral workflow harness의 complete multi-round execution
- Exact four-component Win comparator
- Approved fixture/profile/build/seed/step/verifier/metric fingerprint 고정
- 모든 declared worker `MAX_STEPS_REACHED` + candidate verification
- Final champion finalization/result verification, baseline/challenger record와 explicit approval
- 특정 cloud product는 포함하지 않음

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `WinComparatorTest.usesExactFourComponentLexicographicOrder` | 뒤 성분이 앞 성분 차이를 뒤집거나 time formula가 다름 | Unit |
| `OfficialManifestValidationTest.requiresApprovedCalibrationAndCompliantFixtureDigest` | 미승인 값/decimal fixture/누락 fingerprint를 허용 | Unit/negative |
| `OfficialWorkflowIT.requiresAllWorkersNormalAndVerified` | 일부 success, 비정상 termination 또는 unverified worker로 완료 | Integration/fault |
| `OfficialWorkflowIT.preservesRetryIdentityAndWarmStartLineage` | Retry/round handoff가 manifest와 다름 | Integration |
| `OfficialRerunReproducibilityIT.repeatsChampionTraceAndResultFingerprint` | 동일 manifest 재실행 결과가 다름 | Reproducibility |
| `WinPocBenchmark` | Approved performance/quality evidence bundle을 생성하지 못함 | Benchmark |

권장 명령은 승인 record가 지정한 manifest 경로를 명시해야 한다. 아래 `/absolute/path/to/approved-manifest.json`은 실행 전에 승인 artifact의 실제 절대 경로로 바꾸며, 임시 manifest나 임의 수치로 대체하지 않는다.

```bash
mvn -pl apps/worker -am -Dtest=WinComparatorTest,OfficialManifestValidationTest test
mvn -pl apps/worker -am -Dit.test=OfficialWorkflowIT,OfficialRerunReproducibilityIT -Drpdptw.official.manifest=/absolute/path/to/approved-manifest.json verify
```

**Deliverables:** Approved immutable execution manifest/card, complete worker/round lineage, verified final champion, immutable baseline/challenger comparison record와 approval evidence.

**Exit evidence:** Calibration approval reference, compliant fixture digest, exact comparator hand cases, phase-1 all-available screen, completion-order independence, retry/incomplete fault cases, all worker normal completion/verification, identical-manifest rerun와 both-gate final result.

**완료/중단 조건:** 위 evidence와 explicit baseline/comparison approval가 모두 있어야 `DONE`. 현재 decimal fixture, README 예시 수치, 인터뷰 설명값 또는 일부 worker 결과를 사용하려는 순간 중단한다.

**다음 phase handoff:** Representative official workload와 measured runtime artifact를 `AR-10` profiling에 선택적으로 제공한다. Official completion이 `AR-11` physical topology를 자동 승인하지 않는다.

### 9.11 `AR-10 / RM-7` — COW profiling decision

**목적:** Correct COW baseline의 copy/allocation/GC 비용을 전체 search cost 안에서 측정하고 COW 유지 또는 별도 apply/undo 제안만 결정한다.

**Entry gate와 선행 phase:** `AR-4 DONE`, 대표 verified path를 위해 `AR-5 DONE`. `AR-9` official workload가 있으면 사용할 수 있으나 `AR-9 BLOCKED` 자체는 representative non-official profiling을 막지 않는다.

**정확한 구현 범위/모듈:**

- `rpdptw-solver`의 read-only profiling counters/hooks
- 별도 benchmark source set/profile의 `CowStateStrategyBenchmark`
- Route copy, allocation, GC, propagation/evaluation/cache와 전체 step cost attribution
- Representative problem/config/fingerprint와 warm-up/run environment record
- Decision report: `KEEP_COW` 또는 별도 experiment/ADR 제안
- Apply/undo production/skeleton 구현은 포함하지 않음

**First failing tests와 실패 관찰:**

| Test | 첫 실패 기준 | 종류 |
|---|---|---|
| `CowProfilingInstrumentationTest.doesNotChangeTraceOrResultFingerprint` | Profiling on/off가 search semantics를 바꿈 | Reproducibility |
| `CowProfilingScenarioTest.attributesCopyAllocationAndTotalStepCost` | COW 비용을 전체 비용과 분리해 기록하지 못함 | Integration/performance |
| `CowStateStrategyBenchmark` | Warm-up/config/build/fingerprint가 없는 비재현 측정 | Benchmark |
| `CowDecisionGateTest.keepsCowWithoutApprovedSwitchEvidence` | 수치 threshold 없이 apply/undo 전환을 선택 | Unit/governance |

권장 명령:

```bash
mvn -pl rpdptw/solver -am -Dtest=CowProfilingInstrumentationTest,CowDecisionGateTest test
mvn -pl rpdptw/solver -am -Pprofiling verify
```

**Deliverables:** Reproducible profile bundle, bottleneck attribution, `KEEP_COW` decision 또는 correctness/reproducibility gate를 포함한 별도 변경 제안.

**Exit evidence:** Profiling on/off exact trace/result equality, representative input/config/runtime fingerprint, copy/allocation/GC와 total search 비율, raw report, decision rationale. 전환 제안 시에도 이 phase에서는 구현하지 않고 별도 approval을 요청한다.

**완료/중단 조건:** Evidence가 병목을 입증하지 못하거나 승인 기준이 없으면 `KEEP_COW`로 `DONE`이다. Correctness/reproducibility/observability 중 하나라도 약화되면 전환 제안을 중단한다.

**다음 phase handoff:** 기본 handoff는 “COW 유지”와 profiling baseline이다. Apply/undo는 새 승인 roadmap이 생긴 경우에만 별도 후속 작업으로 넘긴다.

## 10. Phase 문서 작성 계약

각 후속 문서 작성 세션은 §7의 자기 phase 파일 하나만 생성하거나 수정하고, 다음 항목을 모두 포함해야 한다.

1. **실제 저장소 조사:** 작성 시점의 `git status --short`, `rg --files`, POM/module graph, 대상 production/test source, 선행 phase evidence와 source design version/hash
2. **Authority와 결정 상태:** 관련 Master/Architecture/Domain 절, 질문 ID, `REVIEW`/ADR/API 승인 상태와 충돌 처리
3. **현 상태→목표 상태 gap:** 이미 존재하는 code/test와 새로 만들거나 이동/폐기할 것
4. **정확한 예상 경로:** 변경할 POM, source/resource/test file의 repository-relative path
5. **예상 타입/API:** package, class/record/interface, 책임, 주요 input/output/error; 승인되지 않은 것은 `계획상 제안 API` 표시
6. **Test case 표:** test class/method, 종류, 권위 근거, fixture, expected result, 첫 실패 message/관찰, green 조건
7. **테스트 우선 구현 순서:** failing test 작성 → 의도한 실패 확인 → 최소 구현 → 통과 → module 회귀 → reactor 회귀
8. **명령:** Working directory, exact Maven `-pl/-am`, `-Dtest`/Failsafe/profile, architecture/rg/link/diff check
9. **Deliverable와 evidence:** artifact/type, report/fingerprint, evidence bundle 위치와 다음 phase가 소비하는 방법
10. **Rollback:** POM/module 이동, schema/version, state/artifact와 logical cutover를 되돌리는 단위; 사용자 변경 보존 방법
11. **완료·중단 조건:** 어떤 증거가 있어야 `DONE`, 어떤 unresolved authority/fixture/provider/API에서 `BLOCKED`인지
12. **Handoff:** 선행 artifact identity, test builders, known risks, 다음 phase의 entry gate
13. **Scope exclusions:** 자기 phase가 구현하지 않을 후속/deferred 항목과 금지 shortcut

Phase 문서는 작업량 목록만 나열해서는 안 된다. 각 production change는 먼저 실패할 test와 관찰 기준에 연결되어야 하며, 예상 file/API가 없으면 “구현자가 정함”으로 넘기지 말고 계획상 제안 이름과 변경 절차를 명시한다.

## 11. Evidence bundle과 progress 상태

### 11.1 Evidence bundle

각 phase 실행은 다음 논리 구조를 `target/codex-evidence/<phase-id>/<evidence-id>/`에 생성하고 CI artifact로 보존한다. `target/` 산출물을 source control에 넣을 필요는 없지만 phase 문서와 progress record는 evidence ID/digest를 참조해야 한다.

```text
target/codex-evidence/<phase-id>/<evidence-id>/
├── evidence.json                 # phase, commit/tree, source hashes, toolchain, status
├── commands.log                  # 실행 명령, exit code와 순서
├── red/                          # first failing test report와 의도한 failure 관찰
├── green/                        # targeted green report
├── regression/                   # module/reactor Surefire/Failsafe/architecture report
├── fingerprints/                 # problem/travel/profile/manifest/solution/result digest
├── faults/                       # corruption/fault/retry/cancel matrix 결과
├── reproducibility/              # repeated trace/result comparison
├── benchmark/                    # AR-9/AR-10에만 사용
├── diff/                         # git diff --check, changed-file scope와 dependency graph
└── handoff.md                    # deliverable, blocker, rollback와 next-phase entry
```

`evidence.json`은 최소 `phaseId`, `rmMapping`, `status`, `sourceDesignVersions`, `gitCommitOrTree`, `javaVersion`, `mavenVersion`, `commands`, `testCounts`, `artifactDigests`, `blockers`, `createdAt`을 가진다. Timestamp와 elapsed는 metadata이며 semantic fingerprint나 quality objective에 넣지 않는다.

### 11.2 Progress 상태

향후 progress 문서는 다음 네 값만 사용한다.

| 상태 | 의미 |
|---|---|
| `NOT_STARTED` | Entry gate가 아직 평가되지 않았거나 production/test 작업을 시작하지 않음 |
| `IN_PROGRESS` | Entry gate를 통과하고 범위 내 test/implementation/evidence를 작성 중 |
| `BLOCKED` | 명시된 외부 결정·승인·fixture 또는 선행 evidence가 없어 안전하게 진행할 수 없음 |
| `DONE` | 아래 DONE AND gate를 모두 충족 |

`DONE`은 다음 조건의 AND다.

1. Entry gate와 모든 선행 phase가 충족됨
2. Phase 문서의 구현 범위와 deliverable이 실제 repository에 존재함
3. 각 요구에 red→green evidence가 있음
4. Targeted test, module `verify`, 필요한 reactor/architecture 회귀가 통과함
5. Exit evidence와 evidence bundle digest가 있음
6. 불변조건, 금지 dependency/value, scope exclusion 위반이 0임
7. Blocker가 남지 않았고 중단 조건이 발생하지 않음
8. Rollback과 다음 phase handoff가 재현 가능함
9. 단순 source/test/mock/demo 존재가 아니라 downstream consumer가 artifact를 실제로 검증해 소비함

`BLOCKED`는 실패를 숨기는 상태가 아니다. Blocker ID, 필요한 authority/evidence, 마지막 안전 지점과 재개 조건을 기록한다. `AR-9`는 현재 `Q-BENCH-02` 승인 수치와 compliant integer fixture가 없으므로 구현 착수 관점에서 `BLOCKED`가 정상 상태다.

## 12. 전체 완료 정의

이 계획의 전체 완료는 다음을 모두 만족할 때만 주장한다.

1. `AR-0`~`AR-10`이 각자의 DONE rule을 충족한다. `AR-9` blocker가 남아 있으면 generic solver와 logical workflow가 완료되어도 **official Win PoC 포함 전체 완료**는 아니다.
2. Java 25 reactor와 dependency DAG가 architecture rule로 강제된다.
3. Versioned input에서 immutable problem, complete prepared travel과 bound profile까지의 lineage가 고정된다.
4. Atomic pair/COW ALNS가 exact step/seed/order에서 재현되고 cache-free recomputation과 같다.
5. Candidate verifier와 result-integrity verifier가 독립 corruption suite를 통과하고 둘의 `PASS`만 정상 publication/retrieval로 이어진다.
6. Local logical ports, idempotency, cancellation, artifact digest/CAS와 multi-round completeness가 provider 없이 검증된다.
7. Legacy semantics가 characterization·compatibility·shadow·rollback evidence를 거쳐 논리적으로 cutover된다.
8. Approved integer Win fixture와 `Q-BENCH-02` 수치로 complete verified official workflow를 재실행해 같은 champion/result fingerprint를 얻는다.
9. COW profiling은 COW 유지 또는 별도 승인 제안으로 끝나며 묵시적 apply/undo 전환이 없다.
10. `Q-INFRA-01`, `Q-VAR-01`, multi-trip/rotation, route pool/MIP와 physical topology가 현재 core에 선반영되지 않는다.

Provider deployment, optional variants와 route pool/MIP의 부재는 이 계획의 실패가 아니다. 그것들은 `AR-11/RM-9`의 deferred 후속이며 resume evidence와 별도 scope approval가 있어야 새로운 계획을 만든다.

## 13. 현재 blocker와 재개 조건

| Blocker | 현재 상태 | 차단 범위 | 재개 조건 |
|---|---|---|---|
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED`; 공식 수치 없음 | `AR-9` official manifest/baseline/challenger verdict | Calibration corpus·측정 결과와 수치별 명시적 승인 |
| Win fixture decimal `D/U` | Integer meter/second 계약 비준수 | `AR-9` official normalization/baseline | Compliant integer matrix/fixture와 승인된 digest, 또는 명시적 계약 변경 |
| `Q-INFRA-01` | `DEFERRED` | Provider adapter, deployment topology, production infrastructure cutover | Workload/security/retention/retry/recovery/performance/cost evidence와 별도 scope 승인 |
| `Q-VAR-01` | `DEFERRED` | MDVRP/OVRP/SDVRP feasibility/implementation | 대상·시점·fixture·core 영향에 대한 별도 승인 |

README의 `parallelRuns=8`, `iterationsPerRun=5000`, 현재 Workflow timeout, GCP retry와 기존 초안의 수치는 blocker를 해결하지 않는다. 이 값들은 legacy characterization 또는 explicit test-only config로만 남긴다.
