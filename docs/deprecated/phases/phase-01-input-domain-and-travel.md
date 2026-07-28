# AR-1 / RM-1 — Versioned input, immutable domain과 prepared travel 구현 명세

```yaml
phase: AR-1
rm_mapping: RM-1
status: BLOCKED
document_status: REVIEW
document_role: 후속 구현 세션이 테스트를 먼저 작성해 rpdptw-core의 input/domain/normalization/travel을 구현하기 위한 실행 명세
last_updated: 2026-07-24
working_directory: /Users/brown/workspace/ro-next
source_tree:
  git_commit: 523c23e2e13410885b16e974efe40ffe598106ee
  branch_at_authoring: codex/domain-design
  dirty_worktree: true
source_baseline:
  implementation_plan:
    path: docs/codex/implementation-plan.md
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  master_design:
    version: 3.2-review
    path: docs/master-design.md
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    version: 1.1-review
    path: docs/architecture-design.md
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    version: 2.2-review
    path: docs/domain-design.md
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
shared_build_baseline:
  source: phase-00 isolated execution observation supplied by the management session
  java: 25.0.3-amzn
  maven: 3.9.14
  legacy_test: PASS
  legacy_verify: PASS
  concurrent_target_failures_are_authoritative: false
predecessors:
  - phase: AR-0
    rm_mapping: RM-0
    required_status: DONE
    path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
  - path: docs/codex/implementation-plan.md
    required_sections: ["§5", "§8", "§9.2", "§10", "§11"]
current_blockers:
  - id: B-AR1-ENTRY-01
    condition: AR-0 phase 문서, reactor module과 DONE evidence가 현재 tree에 없음
  - id: B-AR1-GC-01
    condition: 누락 D를 생성할 Great Circle 함수의 exact 식/상수/구현 version authority가 없음
non_blockers:
  - canonical public JSON/wire schema 미승인
  - Q-BENCH-02 공식 수치 미승인
  - Q-INFRA-01과 Q-VAR-01의 DEFERRED 상태
  - 현재 Win fixture의 decimal D/U
```

관련 기준: [전체 구현 계획](../implementation-plan.md), [Master Design](../../master-design.md), [Architecture Design](../../architecture-design.md), [Domain Design](../../domain-design.md), [질문 등록부](../../master-design-open-questions.md)

## 1. 문서 목적과 실행 판정

이 문서는 `AR-1 / RM-1` 구현을 위한 계획이며 승인된 public API, wire schema 또는 저장 schema가 아니다. 아래 Java 이름과 signature는 후속 세션 간 모호성을 없애기 위한 **계획상 제안 API**다. 외부 계약이나 ADR이 승인되어 같은 변경 단위에서 이 문서와 상위 계획을 갱신하지 않는 한 구현 세션은 임의로 이름·책임·error 의미를 바꾸지 않는다.

현재 구현 착수 상태는 `BLOCKED`다. `AR-0`가 만들어야 할 `rpdptw/core`, `build/test-fixtures`, parent convention과 architecture enforcement가 아직 존재하지 않는다. 이 문서 작성은 완료할 수 있지만 production/test 구현은 `B-AR1-ENTRY-01` 해소 전 시작하지 않는다.

`B-AR1-GC-01`은 범위를 더 세밀하게 제한한다. Versioned distance-generation port, policy identity/fingerprint, explicit test double, policy 부재 시 typed pre-solve rejection은 구현할 수 있다. 그러나 승인되지 않은 Earth radius, library default 또는 구체 Great Circle 공식을 production default로 넣어서는 안 된다. 누락 `D`의 production generation까지 포함해 phase를 `DONE`으로 만들려면 exact function/version authority 또는 승인 ADR이 필요하다.

Canonical public wire schema 미승인은 core `CanonicalInput`, test builder와 immutable domain 구현을 막지 않는다. 이 phase는 JSON을 canonical이라고 선언하거나 현재 Win fixture를 정상화하지 않는다.

공통 build baseline은 관리 세션이 전달한 phase-00 격리 실행 결과인 Java `25.0.3-amzn`, Maven `3.9.14`, 기존 `mvn test`/`mvn verify` 성공이다. 여러 문서 세션이 같은 working directory의 `target/`을 동시에 사용해 발생한 shade JAR replace 등 transient failure는 repository baseline 결함, AR-1 계약 실패 또는 blocker evidence로 확정하지 않는다. 그런 실패는 동시 실행이 끝난 격리 환경에서 같은 명령을 재실행하기 전까지 판정 보류다.

## 2. Authority, 결정 상태와 충돌 처리

### 2.1 적용 authority

| 영역 | Authority | 적용 계약 |
|---|---|---|
| 전체 흐름·불변조건 | Master §4~§8, §15.3 | Versioned input → canonical meaning → immutable problem/complete travel; raw 재해석·runtime fallback 금지 |
| Canonical input·numeric·time·compatibility·travel | Domain §4~§9, §15~§16 | Exact decimal, item-first `n=3/FLOOR`, integer D/U, half-open plan, pair/location/terminal, static compatibility, typed errors |
| Module/package/API 배치 | Architecture §5~§8, §18~§19 | `rpdptw-core`의 `input/domain/normalization/travel`; base namespace `com.ronext.rpdptw`; test/evidence 및 build 순서 |
| Phase gate | Implementation Plan §9.2, §10~§11 | Test-first 순서, exact evidence, `ProblemInstance`/`PreparedTravel` handoff, DONE AND gate |
| 질문 상태 | `docs/master-design-open-questions.md` | `Q-NUM-01~03`, `Q-MTX-01~03`, `Q-TIME-01~04`, `Q-IN-01~02`, `Q-COMP-01~02`, `Q-REQ-01~02`, `Q-RES-01`, `Q-BENCH-03`는 `RESOLVED`; `Q-BENCH-02`는 experiment-required; infra/variant는 deferred |

세 설계와 이 문서는 모두 `REVIEW`다. 따라서 내부 type 후보를 public HTTP field, canonical JSON 또는 third-party API로 표현하지 않는다.

### 2.2 충돌 처리

1. 작성/구현 시 source hash가 metadata와 다르면 먼저 변경 절과 의미를 재검토한다.
2. 채택된 외부 계약이나 승인 ADR이 발견되면 그것이 우선한다.
3. Domain 의미와 Architecture 배치가 충돌하면 의미를 임의 변경하지 않고 `BLOCKED` evidence를 남긴다.
4. 구체 wire field, Great Circle 상수, provider 또는 optional variant가 필요해지면 추측하지 않는다.
5. 승인 변경은 질문/ADR, 상위 설계, 구현 계획과 이 문서를 같은 변경 단위에서 갱신한 뒤 재개한다. 이 phase 구현 commit 하나로 `REVIEW` 문서를 묵시 승인하지 않는다.

### 2.3 Blocker와 안전하게 진행 가능한 범위

| ID | 차단 범위 | 마지막 안전 지점 | 재개 조건 |
|---|---|---|---|
| `B-AR1-ENTRY-01` | 모든 source/test/POM 구현 | 이 phase 문서만 작성 | `phase-00...md` 존재, `AR-0 DONE`, target reactor와 architecture evidence 확인 |
| `B-AR1-GC-01` | 누락 `D`의 production Great Circle 구현 및 그 exit evidence | Versioned policy interface, explicit fixture policy, missing-policy rejection | Exact 식·상수·rounding input와 implementation version을 가진 승인 ADR/외부 계약 |
| Win decimal `D/U` | Win fixture의 canonical/official 사용 | Negative rejection test | Integer meter/second fixture와 승인 digest 또는 명시적 계약 변경 |
| `Q-BENCH-02` | Official step/worker/round/watchdog 값 | AR-1에는 숫자 없음 | Calibration 승인; AR-1 완료에는 불필요 |
| `Q-INFRA-01`, `Q-VAR-01` | Physical adapter/topology, optional variant | Logical core 경계만 유지 | 각 deferred resume evidence와 별도 scope 승인 |

## 3. 작성 시점 실제 저장소와 목표 gap

### 3.1 조사 결과

작성 시점 `git status --short`에는 설계 문서와 `data/`, `docs/codex/`를 포함한 다수의 기존 변경·미추적 파일이 있다. 모두 사용자 또는 다른 세션 소유이며 이 phase가 수정·복원·정리하지 않는다.

현재 실제 build/source 상태는 다음과 같다.

| 항목 | 현재 상태 | AR-1 해석 |
|---|---|---|
| Root POM | 단일 `jar`; Java 25, Maven `[3.9.14,)`; Google Cloud/Jackson/JUnit 직접 dependency; shade main | AR-0 이전 상태. AR-1이 root POM이나 legacy packaging을 이동하지 않음 |
| Target module | `rpdptw/core`, `build/test-fixtures`와 aggregator가 없음 | `B-AR1-ENTRY-01` |
| Production source | `com.ronext.optimizer` 아래 6개 class | RPDPTW domain evidence가 아닌 legacy characterization 대상 |
| Application core | `AlnsBatchEngine.run(...)`이 `Map<String,Object>`와 `double objective` 생성 | 새 type의 base/interface/fixture로 재사용 금지 |
| Test | `AlnsBatchEngineTest` 1개; status/run number/positive objective만 검사 | Numeric/time/pair/travel/fingerprint coverage 0 |
| README/GCP | Cloud Run/Workflows/Storage, `parallelRuns=8`, `iterationsPerRun=5000`, provider retry | Legacy characterization. Official value/provider authority가 아님 |
| Docker | 단일 shaded JAR와 `SERVICE_MODE` | AR-1 범위 밖 |
| Win fixture | SHA-256 `ea003...b7d7`, 14,157,512 bytes, 452 orders, 31 vehicles, 453 locations에 해당하는 205,209 matrix cells | Read-only negative fixture |
| Win travel tokens | Decimal lexical `D` 204,756개, decimal lexical `U` 184,119개; diagonal 예에 `D=9999`, `U="0"` | Decimal을 반올림/절삭하지 않고 거부; self는 정상 input에서 `0/0`으로 normalize하되 fixture official 사용 금지 |
| Legacy PDF | `data/ro_input_json_spec.pdf`, legacy CVRPTW 표/예시 | Canonical RPDPTW schema로 복제 금지 |

작성 시 toolchain은 Maven `3.9.14`, Java `25.0.3-amzn`이다. 문서 세션은 source artifact를 만들지 않기 위해 `mvn test/verify`를 실행하지 않았고, phase-00의 격리 실행 `test/verify PASS`를 공통 baseline으로 사용했다. 구현 세션의 red/green 명령은 §9를 따르되 동시 `target/` 충돌을 실제 failure로 분류하지 않는다.

### 3.2 현 상태 → 목표 gap

| Gap | 목표 상태 | 먼저 실패할 test |
|---|---|---|
| Version/schema identity와 source provenance 없음 | `CanonicalInput`이 exact schema/adapter/raw digest/interpretation을 보존 | `CanonicalInputContractTest` |
| `double`/Map placeholder뿐 | Exact scalar와 checked integer domain | `FixedPointNormalizerTest` |
| Time string 의미 없음 | Exact parsing, origin seconds, `[start,end)`, inclusive close, repeating/overnight windows | `TimeNormalizerTest` |
| Request pair/location/terminal 없음 | Immutable dense ID, two-node pair, service pattern, explicit oneway/roundtrip terminal | `RequestPairPropertiesTest`, `VehicleAndTerminalNormalizerTest` |
| Static compatibility 없음 | Size/capability/zone AND와 static evidence | `CompatibilityNormalizerTest` |
| Travel preparation 없음 | Directed `M²`, vehicle-resolved time, self `0/0`, source provenance, no fallback | `PreparedTravelTest` |
| Fingerprint/lineage 없음 | Canonical semantic fingerprints와 immutable provenance | `SemanticFingerprintTest` |
| Shared test builder 없음 | Downstream가 test-scope로 소비할 deterministic builder/oracle | `FixtureContractTest` |
| Win fixture가 decimal travel | Official 사용 거부, lexical 값 보존 negative extraction만 허용 | `WinFixtureTravelRejectionTest` |

Legacy source/test/GCP/Docker/README/data는 이동·삭제·수정하지 않는다. AR-0이 legacy 이동을 아직 하지 않았다면 AR-1이 대신 수행하지 않고 중단한다.

## 4. 구현 범위와 package 책임

### 4.1 대상 module

- 주 owner: `rpdptw/core` (`rpdptw-core`)
- Test-only handoff: `build/test-fixtures`
- Base namespace: `com.ronext.rpdptw`
- Production dependency: JDK만 허용한다. Jackson, cloud/provider SDK, solver, verification, application dependency를 추가하지 않는다.
- JUnit은 test scope다. Property 성질은 JUnit 5의 deterministic 반복/parameterized test로 구현하며 새 property library를 임의 추가하지 않는다.

### 4.2 책임 흐름

```text
versioned adapter output
  → CanonicalInput
  → ProblemNormalizer
      → immutable ProblemInstance
      → TravelPreparationInput
  → TravelPreparer
      → complete PreparedTravel
  → PreparedInputs identity check
```

이 phase의 “versioned input”은 exact `InputSchemaIdentity`, adapter contract/version과 source provenance를 가진 provider-neutral canonical business input 경계다. Production JSON parser와 external field alias 목록은 승인된 wire contract가 아니므로 만들지 않는다. Test-only builder/Win token extractor는 이 경계를 검증할 뿐 production adapter가 아니다.

## 5. 정확한 예상 변경·신규·이동 경로

아래 경로는 계획상 고정 제안이다. AR-0 결과가 이름이나 module 위치를 승인 변경했다면 이 문서를 먼저 갱신한다.

### 5.1 Test를 먼저 생성할 경로

| 동작 | 경로 | 책임 |
|---|---|---|
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/input/CanonicalInputContractTest.java` | Version/schema/source provenance |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/normalization/FixedPointNormalizerTest.java` | Exact decimal, item-first, integer-only, overflow, 999 CBM |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/normalization/TimeNormalizerTest.java` | Plan/window/service/time normalization |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/domain/DenseIdentityMapPropertiesTest.java` | External↔dense bijection/reference |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/domain/RequestPairPropertiesTest.java` | Pair/node/service-pattern invariant |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/domain/VehicleAndTerminalNormalizerTest.java` | Ownership, terminal/trip, rotation |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/normalization/CompatibilityNormalizerTest.java` | Size/capability/zone static compatibility |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/travel/PreparedTravelTest.java` | Complete directed/vehicle-resolved travel |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/travel/WinFixtureTravelRejectionTest.java` | Decimal Win `D/U` negative-only use |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/fingerprint/SemanticFingerprintTest.java` | Canonical encoding, provenance, immutability |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/normalization/ProblemPreparationIT.java` | Canonical input→problem→travel integration |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/testing/CanonicalInputFixtures.java` | Core-test-local builders |
| 신규 | `rpdptw/core/src/test/java/com/ronext/rpdptw/testing/WinFixtureTravelTokens.java` | Raw lexical `D/U` read-only extractor |
| 신규 | `build/test-fixtures/src/test/java/com/ronext/rpdptw/testing/FixtureContractTest.java` | Downstream fixture stability |

### 5.2 Test red를 확인한 뒤 생성할 production 경로

| 동작 | 경로 | 책임 |
|---|---|---|
| 수정 | `rpdptw/core/pom.xml` | JUnit/Failsafe test convention 상속, repository-root test property; production dependency 추가 없음 |
| 수정 | `build/test-fixtures/pom.xml` | `rpdptw-core`를 test-fixture compile owner로 참조; runtime app leakage 금지 |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/InputSchemaIdentity.java` | Exact schema name/version |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/SourceProvenance.java` | Raw digest, adapter version, aliases/coercions/ignored values |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalScalar.java` | 원문 10진 lexical 값; `double` 선변환 금지 |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalInput.java` | Canonical business aggregate |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalPlan.java` | Plan/depot/trip/resource input meaning |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalRequest.java` | Sealed delivery-only/real pair input |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalVehicle.java` | Vehicle canonical meaning |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalLocation.java` | Physical location/coordinate input |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/input/contract/CanonicalTravelInput.java` | Sparse directed provided D/U tokens |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/RequestId.java` | Dense request ID |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/VehicleId.java` | Dense vehicle ID |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/SolverNodeId.java` | Dense solver-node ID |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/PhysicalLocationId.java` | Dense physical-location ID |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/DenseMappings.java` | Typed external↔dense bijections |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/PlanningHorizon.java` | Origin과 half-open end-second |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/TimeWindow.java` | Inclusive open/close normalized window |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/ServiceNode.java` | Terminal/logical pickup/physical pickup/delivery |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/Request.java` | Immutable exactly-two-node pair와 static facts |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/Vehicle.java` | Immutable capacity/feature/capability/zone/ownership/resource |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/TerminalPolicy.java` | Oneway 또는 single roundtrip |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/StaticCompatibility.java` | Immutable dense vehicle set와 proven evidence |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/NormalizationProvenance.java` | Numeric/time/service/compatibility/trip policy lineage |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/ProblemInstance.java` | Immutable domain authority |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/fingerprint/SemanticFingerprint.java` | Algorithm/version/digest value |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/fingerprint/internal/CanonicalFingerprintWriter.java` | Domain-separated deterministic encoding |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/InputErrorCode.java` | Stable typed pre-solve code |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/InputViolation.java` | Code/path/message/rejected token |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/InputPreparationException.java` | Ordered immutable violations |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/NormalizationPolicies.java` | Exact policy IDs/versions, no hidden tunable |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/FixedPointNormalizer.java` | Weight/volume/integer/checked arithmetic |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/TimeNormalizer.java` | Exact parsing/window expansion/service time |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/CompatibilityNormalizer.java` | Size/capability/zone and static evidence |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/ProblemNormalizer.java` | Canonical input→problem + travel preparation input |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/DistanceGenerationPolicy.java` | Explicit versioned Great Circle hook |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/TravelPreparationPolicy.java` | Generation/rounding/default-speed identity |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/TravelValueSource.java` | `SELF_NORMALIZED`, `PROVIDED`, `GENERATED` |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/TravelPreparationInput.java` | Normalized sparse source and coordinates |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/TravelProvenance.java` | Per-value source, policy, coverage |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/PreparedTravel.java` | Complete immutable query authority |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/TravelPreparer.java` | Pre-solve completion/rejection |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/PreparedInputs.java` | Problem/travel identity pair |
| 신규 | `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/ProblemPreparer.java` | End-to-end orchestration |

### 5.3 Green 뒤 downstream handoff용 경로

| 동작 | 경로 | 책임 |
|---|---|---|
| 신규 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testing/CanonicalProblemFixture.java` | Delivery-only/real-pair hand fixture |
| 신규 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testing/PreparedTravelFixture.java` | Complete small directed travel fixture |
| 신규 | `build/test-fixtures/src/main/java/com/ronext/rpdptw/testing/IndependentFingerprintOracle.java` | Production writer와 독립된 test oracle |

이 phase의 이동·삭제 예상 경로는 **없다**. Root POM, legacy `src/`, README, GCP, Docker, `data/`, 설계·progress·다른 phase 문서를 수정하지 않는다.

## 6. 계획상 제안 type/API, error와 불변조건

### 6.1 Canonical input

아래는 내부 계약 제안이며 public wire schema가 아니다.

```java
public record InputSchemaIdentity(String schemaName, String schemaVersion) {}

public record SourceProvenance(
        SemanticFingerprint rawDigest,
        String adapterId,
        String adapterVersion,
        List<Interpretation> interpretations) {}

public record CanonicalScalar(String lexical) {}

public record CanonicalInput(
        InputSchemaIdentity schema,
        SourceProvenance source,
        CanonicalPlan plan,
        List<CanonicalLocation> locations,
        List<CanonicalRequest> requests,
        List<CanonicalVehicle> vehicles,
        CanonicalTravelInput travel) {}
```

`CanonicalRequest`는 sealed interface로 제안한다.

```java
public sealed interface CanonicalRequest
        permits CanonicalRequest.DeliveryOnly, CanonicalRequest.PickupDelivery {
    String externalRequestId();
    List<CanonicalItem> items();

    record DeliveryOnly(/* delivery task and restrictions */) implements CanonicalRequest {}
    record PickupDelivery(/* pickup + delivery tasks and restrictions */) implements CanonicalRequest {}
}
```

Signature에 쓰이는 보조 타입은 새 public file을 임의로 늘리지 않고 다음 owner file의 public nested type으로 고정 제안한다.

| 보조 type | Owner file | 의미 |
|---|---|---|
| `SourceProvenance.Interpretation` | `SourceProvenance.java` | Alias/coercion/ignored raw value의 code/path/from/to |
| `CanonicalRequest.CanonicalItem` | `CanonicalRequest.java` | Qty, weight, volume, per-item task time |
| `CanonicalRequest.Task` | `CanonicalRequest.java` | Location/window/duration/size/capability/zone |
| `CanonicalPlan.TripInput` | `CanonicalPlan.java` | Raw trips/multiRotation/wait/resource meaning |
| `CanonicalLocation.Coordinate` | `CanonicalLocation.java` | Exact decimal latitude/longitude; travel policy 외 계산 금지 |
| `CanonicalTravelInput.DirectedArc` | `CanonicalTravelInput.java` | External from/to와 optional provided D/U lexical tokens |

규칙:

- 모든 list/map/set은 constructor에서 null element, duplicate identity와 mutable alias를 거부하고 defensive immutable copy를 만든다.
- `CanonicalScalar`는 lexical 값을 보존하며 `double`/`float`로 변환하지 않는다.
- Schema 지원 여부는 normalization 전에 검사한다. Unknown version을 가까운 version으로 fallback하지 않는다.
- Alias/coercion/ignored raw value는 `SourceProvenance.interpretations`에 stable order로 남긴다.
- 현재 Win JSON을 `CanonicalInput`의 승인 예제로 만들지 않는다.

### 6.2 Numeric/time normalization

```java
public final class FixedPointNormalizer {
    public long nonNegativeFloor(CanonicalScalar value, int scale, String path);
    public long positiveQuantity(CanonicalScalar value, String path);
    public long integerOnly(CanonicalScalar value, String path);
    public long multiplyExact(long normalizedItem, long quantity, String path);
    public long addExact(long left, long right, String path);
}

public final class TimeNormalizer {
    public PlanningHorizon planningHorizon(
            String exactPlanStart, String exactPlanEnd, String path);
    public List<TimeWindow> expandDailyWindow(
            String exactOpen, String exactClose, PlanningHorizon plan, String path);
    public long serviceSeconds(
            CanonicalScalar duration, List<CanonicalItem> items, String path);
}
```

불변조건:

- Weight/volume은 비음수 exact decimal을 item별 `n=3/FLOOR` 후 positive qty와 곱한다.
- Cost/distance/time은 lexical integer만 허용한다. `1.0`, `310708.03`, `17265.5`는 값이 수학적으로 정수인지와 무관하게 decimal input으로 거부한다.
- 모든 곱/합은 `Math.multiplyExact`/`Math.addExact` 또는 동등한 checked path를 사용한다.
- Overflow, invalid/non-finite/negative를 sentinel 또는 saturation으로 바꾸지 않는다.
- Volume dimension 미사용이 canonical meaning에 **명시된 경우에만** `999_000` milli-CBM을 적용하고 provenance에 policy ID/version을 남긴다.
- Exact datetime 형식은 `yyyy-MM-dd HH:mm:ss`; offset/timezone suffix를 거부한다.
- Domain time은 planning origin 기준 `long` seconds다. Plan은 `[0,endExclusive)`, window close는 inclusive다.
- `openTime > closeTime`은 하나의 overnight window다. `openTime == closeTime`은 typed ambiguity error다.
- `serviceTime = duration + Σ(item.taskTime × qty)`. Order-level `taskTime`은 거부한다.

### 6.3 Dense domain과 pair/vehicle/terminal

```java
public record RequestId(int value) {}
public record VehicleId(int value) {}
public record SolverNodeId(int value) {}
public record PhysicalLocationId(int value) {}

public record Request(
        RequestId id,
        SolverNodeId pickupNode,
        SolverNodeId deliveryNode,
        long demandMilliKg,
        long demandMilliCbm,
        ServicePattern servicePattern,
        StaticCompatibility staticCompatibility) {}

public record TerminalPolicy(
        TripMode tripMode,
        SolverNodeId startTerminal,
        Optional<SolverNodeId> endTerminal) {}

public final class ProblemInstance {
    public DenseMappings mappings();
    public List<Request> requests();
    public List<ServiceNode> nodes();
    public List<Vehicle> vehicles();
    public List<PhysicalLocation> physicalLocations();
    public PlanningHorizon planningHorizon();
    public NormalizationProvenance provenance();
    public SemanticFingerprint fingerprint();
}
```

`ServiceNode.Role`, `Request.ServicePattern`, `Vehicle.Ownership`, `Vehicle.VehicleSizeCode`, `Vehicle.CapabilityCode`, `Vehicle.ZoneCode`, `TerminalPolicy.TripMode`, `StaticCompatibility.ImmutableVehicleIdSet`, `StaticCompatibility.StaticUnassignabilityEvidence`와 `ProblemInstance.PhysicalLocation`은 각각 표시된 owner file의 public nested type으로 둔다. Nested type도 constructor에서 exact validation과 defensive copy를 수행한다.

불변조건:

- Dense IDs는 canonical ordered input에서 0부터 연속 배정한다. External→dense와 dense→external은 bijection이며 duplicate/unknown reference를 typed error로 거부한다.
- Pickup, delivery, terminal과 physical location identity를 합치지 않는다.
- 모든 request는 pickup/delivery `SolverNodeId`를 정확히 하나씩 가지며 서로 다르다.
- Delivery-only pickup은 `LOGICAL_START_PICKUP`으로서 physical location/travel/stop을 만들지 않는다.
- Real pickup과 delivery는 각자 physical location/time/service를 가진다.
- `oneway`는 start terminal만 가지며 raw `multiRotation`을 무시하되 provenance에 남긴다.
- `single roundtrip`은 같은 depot의 explicit end terminal을 가지며 `multiRotation=0`만 허용한다.
- Non-oneway `multiRotation != 0`은 `UNSUPPORTED_ROTATION`이다.
- Ownership은 missing/null/empty=`DIRECT`, exact `DIRECT`/`LEASE`만 허용한다.
- 누락 route-resource limit는 `OptionalLong.empty()` 또는 동등한 absent value다. 큰 정수 sentinel을 사용하지 않는다.

### 6.4 Static compatibility

```java
public final class CompatibilityNormalizer {
    public StaticCompatibility evaluate(
            CanonicalRequest request,
            List<Vehicle> normalizedVehicles,
            DenseMappings mappings);
}

public record StaticCompatibility(
        ImmutableVehicleIdSet servableVehicles,
        Optional<StaticUnassignabilityEvidence> provenEvidence) {}
```

규칙:

- Vehicle size는 concrete non-empty, case-sensitive code 하나다. Vehicle `"ALL"`은 오류다.
- Request는 하나 이상의 concrete code 또는 정확히 `["ALL"]`이다. `ALL` 혼합은 오류다.
- Capability는 request required set이 vehicle set의 subset인지 검사한다.
- Zone missing/null/empty는 `"ALL"`이다. Size, capability, zone은 AND다.
- Real pair의 size는 pickup/delivery 허용 집합 교집합, capability는 두 작업 요구의 합집합이다.
- Real pair의 concrete pickup/delivery zone이 다르면 해당 request는 정적으로 배정 불가다.
- 호환 vehicle 0개는 structural input error가 아니다. `StaticUnassignabilityEvidence`를 만들고 downstream bank/finalization이 소비한다.
- Vehicle set은 mutable `BitSet`을 직접 노출하지 않고 defensive immutable dense set을 사용한다.

### 6.5 Prepared travel

```java
public interface DistanceGenerationPolicy {
    String policyId();
    String policyVersion();
    BigDecimal greatCircleMeters(Coordinate from, Coordinate to);
}

public record TravelPreparationPolicy(
        Optional<DistanceGenerationPolicy> distanceGeneration,
        String distanceRoundingPolicyId,
        String generatedTimePolicyId,
        long missingSpeedKmh) {}

public final class TravelPreparer {
    public PreparedTravel prepare(
            ProblemInstance problem,
            TravelPreparationInput input,
            TravelPreparationPolicy policy);
}

public final class PreparedTravel {
    public int locationCount();
    public int vehicleCount();
    public long distanceMeters(PhysicalLocationId from, PhysicalLocationId to);
    public long travelTimeSeconds(
            VehicleId vehicle, PhysicalLocationId from, PhysicalLocationId to);
    public TravelValueSource distanceSource(
            PhysicalLocationId from, PhysicalLocationId to);
    public TravelValueSource timeSource(
            VehicleId vehicle, PhysicalLocationId from, PhysicalLocationId to);
    public TravelProvenance provenance();
    public SemanticFingerprint fingerprint();
    public void requireCompatibleProblem(ProblemInstance problem);
}
```

Normalization과 travel orchestration signature는 다음으로 고정 제안한다.

```java
public final class ProblemNormalizer {
    public NormalizedProblem normalize(
            CanonicalInput input, NormalizationPolicies policies);

    public record NormalizedProblem(
            ProblemInstance problem,
            TravelPreparationInput travelInput) {}
}

public record PreparedInputs(
        ProblemInstance problem,
        PreparedTravel travel) {
    public PreparedInputs {
        travel.requireCompatibleProblem(problem);
    }
}

public final class ProblemPreparer {
    public PreparedInputs prepare(
            CanonicalInput input,
            NormalizationPolicies normalizationPolicies,
            TravelPreparationPolicy travelPolicy);
}
```

`DistanceGenerationPolicy.Coordinate`는 `CanonicalLocation.Coordinate`가 아니라 정규화된 exact coordinate value다. 외부 lexical/DTO를 policy에 넘기지 않는다. `TravelPreparationPolicy`의 `missingSpeedKmh`는 v1 계약에서 명시적으로 `45`여야 하며 다른 값을 임의 tunable로 허용하지 않는다.

`TravelPreparationPolicy.missingSpeedKmh`는 resolved `45 km/h` 계약을 명시적으로 담되 provenance/fingerprint에 포함한다. Present but `<=0` speed는 missing으로 취급하지 않고 `INVALID_SPEED`다.

불변조건:

- Travel key는 `PhysicalLocationId`다.
- 최종 `PreparedTravel`은 모든 directed `M²` distance와 모든 사용 vehicle×`M²` time을 가진다.
- Flattened array size 곱도 checked arithmetic으로 검증한다.
- Self는 raw 값과 무관하게 `0m/0s`, source=`SELF_NORMALIZED`다.
- Provided `D`/`U`는 directed authoritative integer다. `C`는 input에 남더라도 계산/fingerprint branch의 authority가 아니다.
- Missing `D`는 explicit versioned distance policy가 있을 때만 그 결과를 `HALF_UP` integer meter로 만든다. 정책/coordinate가 없으면 pre-solve error다.
- Missing `U`는 vehicle별 `CEILING(D_meter × 3.6 ÷ speed_km_h)` exact rational arithmetic으로 만든다.
- Provided common `U`와 vehicle별 generated `U` source를 구분한다.
- Reverse copy, symmetric average, runtime generation과 raw coordinate/speed API 노출을 금지한다.
- Constructor 종료 뒤 missing marker/sentinel은 존재하지 않는다.

### 6.6 Error model

```java
public enum InputErrorCode {
    UNSUPPORTED_SCHEMA_VERSION,
    INVALID_DECIMAL,
    DECIMAL_NOT_ALLOWED,
    NEGATIVE_VALUE,
    NON_POSITIVE_QUANTITY,
    ARITHMETIC_OVERFLOW,
    INVALID_DATE_TIME,
    INVALID_PLAN_RANGE,
    TIME_OUTSIDE_PLAN,
    AMBIGUOUS_DAILY_WINDOW,
    ORDER_LEVEL_TASK_TIME_NOT_ALLOWED,
    DUPLICATE_EXTERNAL_ID,
    UNKNOWN_REFERENCE,
    INVALID_REQUEST_PAIR,
    INVALID_VEHICLE_FEATURE,
    INVALID_ALL_MIX,
    INVALID_OWNERSHIP,
    UNSUPPORTED_ROTATION,
    DUPLICATE_TRAVEL_ARC,
    MISSING_COORDINATE,
    MISSING_DISTANCE_GENERATOR,
    INVALID_SPEED,
    INCOMPLETE_PREPARED_TRAVEL,
    FINGERPRINT_MISMATCH
}
```

모든 pre-solve failure는 `InputPreparationException`의 ordered `List<InputViolation>`으로 반환한다. 각 violation은 stable `code`, canonical path, PII를 포함하지 않는 제한된 rejected token, 사람이 읽을 message를 갖는다. Error 순서는 canonical input order/path order로 안정적이어야 한다. `NumberFormatException`, `ArrayIndexOutOfBoundsException`, NPE와 `ArithmeticException`을 외부 failure contract로 누출하지 않는다.

구조 pair 손상이나 prepared travel runtime missing은 정상 unassignment가 아니라 구현 결함이다. `ProblemInstance`/`PreparedTravel` constructor invariant가 이를 생성 시점에 차단한다.

### 6.7 Fingerprint와 provenance

계획상 내부 fingerprint encoding은 `rpdptw-semantic-v1` domain separator를 사용한다.

- SHA-256, UTF-8, field tag + byte length + bytes의 length-prefix encoding을 사용한다.
- List는 semantic order와 count를, set은 normalized stable sort를 기록한다.
- Absent와 empty를 구분한다.
- 정규화된 integer는 canonical base-10 표현을 사용한다.
- Java serialization, record `toString()`, JSON object iteration order, unordered map iteration에 의존하지 않는다.
- `ProblemInstance` fingerprint에는 schema/raw digest/adapter, dense mapping, numeric/time/service/compatibility/trip policy와 적용 provenance를 포함한다.
- `PreparedTravel` fingerprint에는 problem fingerprint, location/vehicle mapping, 모든 resolved D/U, source classification, generation/rounding/speed policy를 포함한다.
- Timestamp, elapsed, provider locator, secret와 object identity는 제외한다.
- Downstream consumer는 artifact를 받을 때 fingerprint를 재계산하거나 `requireCompatibleProblem`으로 identity mismatch를 거부한다.

`SemanticFingerprintTest`와 `IndependentFingerprintOracle`은 production writer의 구현을 그대로 호출하지 않고 독립 encoding으로 expected digest를 만든다.

## 7. 세분화된 test case 표

각 행은 해당 test가 production 변경보다 먼저 repository에 추가되어야 한다. “첫 실패”가 확인되지 않은 행의 production 구현을 시작하지 않는다.

| ID | Test class.method | 종류/권위 | Fixture | Expected | 정상 첫 실패 관찰 | Green |
|---|---|---|---|---|---|---|
| T01 | `CanonicalInputContractTest.rejectsUnsupportedSchemaBeforeNormalization` | Unit; Master §7.1 | `schema=x/99` | `UNSUPPORTED_SCHEMA_VERSION`, path `$.schema` | 최초 slice는 `cannot find symbol: class InputSchemaIdentity`; skeleton 뒤에는 no exception | Exact typed violation |
| T02 | `CanonicalInputContractTest.preservesSourceVersionAndInterpretations` | Unit/provenance | raw digest + alias + ignored oneway rotation | 모든 interpretation stable order 보존 | expected list와 actual empty | Immutable equality와 fingerprint 반영 |
| T03 | `FixedPointNormalizerTest.itemFirstFloorPrecedesQuantity` | Unit; `Q-NUM-03` | weight `0.0009`, qty `2` | item-first 결과 `0`; line-first `1` 금지 | `expected <0> but was <1>` | Exact `0` |
| T04 | `FixedPointNormalizerTest.floorsWeightAndVolumeAtThreePlaces` | Parameterized; `Q-NUM-01~02` | `1.2349`, `0`, `999.9999` | `1234`, `0`, `999999` | rounding/scale mismatch | 모든 boundary exact |
| T05 | `FixedPointNormalizerTest.rejectsDecimalCostDistanceAndTime` | Negative; `Q-NUM-02`, `Q-MTX-01` | `1.0`, `310708.03`, `17265.5` | 각 `DECIMAL_NOT_ALLOWED` | parse 성공 또는 rounding result | Code/path 3건 |
| T06 | `FixedPointNormalizerTest.rejectsNegativeNonFiniteAndCheckedOverflow` | Negative | negative/`NaN`/huge×qty | typed code; sentinel 없음 | raw arithmetic exception 또는 wrap | `INVALID_DECIMAL`/`NEGATIVE_VALUE`/`ARITHMETIC_OVERFLOW` |
| T07 | `FixedPointNormalizerTest.applies999CbmOnlyForExplicitUnusedVolumeDimension` | Unit; Master §7.4 | explicit unused vs missing capacity | 전자 `999000`+provenance, 후자는 error | 둘 다 default 또는 둘 다 error | 분기와 policy version exact |
| T08 | `TimeNormalizerTest.preservesHalfOpenPlanAndInclusiveClose` | Unit; `Q-TIME-02` | 1-day plan, event at end, serviceStart at close | plan end 거부, close 허용 | end accepted 또는 close rejected | 두 assertion 동시 green |
| T09 | `TimeNormalizerTest.expandsRepeatingAndOvernightWindows` | Unit; `Q-TIME-04` | 2-day plan, `22:00:00→02:00:00` | 하나의 overnight window/day, plan clip | 두 window로 분리/누락 | exact normalized seconds |
| T10 | `TimeNormalizerTest.rejectsEqualOpenCloseAsAmbiguous` | Negative; Domain §6.4 | `08:00:00→08:00:00` | `AMBIGUOUS_DAILY_WINDOW` | 0h/24h로 silent 해석 | typed violation |
| T11 | `TimeNormalizerTest.computesDurationPlusPerItemTaskTime` | Unit; `Q-IN-01` | duration 10, task 3, qty 4 | `22s`; order-level taskTime 거부 | `12`/`10` 또는 alias 허용 | exact value+error |
| T12 | `DenseIdentityMapPropertiesTest.roundTripsEveryExternalAndDenseId` | Property; Domain §9.1 | deterministic 1..100 unique IDs | 양방향 bijection, contiguous IDs | unknown/duplicate/ordering mismatch | 모든 generated case green |
| T13 | `DenseIdentityMapPropertiesTest.rejectsDuplicateAndUnknownReferences` | Negative | duplicate request, missing location | `DUPLICATE_EXTERNAL_ID`, `UNKNOWN_REFERENCE` | overwrite/NPE | stable ordered violations |
| T14 | `RequestPairPropertiesTest.everyRequestHasExactlyTwoBoundNodes` | Property; Master §6 | delivery-only + real pair permutations | distinct pickup/delivery and one owner | partial/duplicate accepted | constructor/normalizer rejects all corruptions |
| T15 | `RequestPairPropertiesTest.distinguishesLogicalStartPickupFromPhysicalPickup` | Unit; `Q-REQ-01` | mixed service patterns | logical pickup has no location/travel; real pickup does | fake depot arc/merged node | role/location assertions green |
| T16 | `VehicleAndTerminalNormalizerTest.preservesOnewayAndSingleRoundtripPolicy` | Unit; `Q-IN-02`, `Q-BENCH-03` | oneway+rotation1, roundtrip+0, roundtrip+1 | oneway ignores+records; roundtrip explicit end; nonzero reject | oneway rejected/rotation enabled/end assumed | exact policy/provenance/error |
| T17 | `VehicleAndTerminalNormalizerTest.defaultsAndValidatesOwnership` | Parameterized; `Q-RES-01` | missing/null/empty/DIRECT/LEASE/`direct` | first 4 DIRECT, LEASE, last error | case-fold/fallback | exact ownership/error |
| T18 | `CompatibilityNormalizerTest.composesSizeCapabilityAndZone` | Unit/property; `Q-COMP-01~02` | vehicles differing one axis each | only all-three compatible vehicle selected | one axis ignored | exact dense set |
| T19 | `CompatibilityNormalizerTest.enforcesExactAllAndPairIntersection` | Negative/property | `["ALL","T1"]`, pair size intersection, zone mismatch | mixed ALL error; empty/mismatch static proven | ALL wildcard mix or union | exact error/evidence |
| T20 | `CompatibilityNormalizerTest.noVehicleIsStaticEvidenceNotInputError` | Unit; Domain §9.5 | valid unknown size code | empty servable set + `PROVEN`; problem builds | input rejected | immutable evidence |
| T21 | `PreparedTravelTest.normalizesSelfAndPreservesProvidedAsymmetry` | Unit; `Q-MTX-01~02` | A→B D=10/U=2, B→A D=30/U=7, self D=9999 | self `0/0`; asymmetric values unchanged | self 9999 또는 symmetric copy | values/source exact |
| T22 | `PreparedTravelTest.completesEveryDirectedVehicleViewBeforeSolve` | Property; `Q-MTX-03` | 3 locations, 2 vehicles, sparse arcs | 9 distances, 18 vehicle times queryable | missing marker/runtime fallback | all queries succeed; coverage exact |
| T23 | `PreparedTravelTest.roundsExplicitGeneratedDistanceAndVehicleTimeExactly` | Unit; `Q-MTX-03` | explicit test policy returns `1000.5m`; speeds 45/없음 | D `1001`; U `81`; missing speed source=45 | HALF_EVEN/double truncation or 80 | exact integer/source/fingerprint |
| T24 | `PreparedTravelTest.rejectsMissingGeneratorCoordinateInvalidSpeedAndDuplicateArc` | Negative | one failure each | matching typed code | reverse/default/last-write wins | all four typed rejections |
| T25 | `WinFixtureTravelRejectionTest.rejectsDecimalWinFixtureForOfficialUse` | Integration-negative; Master §8 | read-only `data/win_poc_case.json` lexical tokens | `D`/`U` decimal 발견, `DECIMAL_NOT_ALLOWED`; no converted artifact | parser accepts/coerces 또는 fixture copy 생성 | Both token classes rejected; fixture hash unchanged |
| T26 | `SemanticFingerprintTest.changesForEveryAuthoritativeFieldButNotRuntimeMetadata` | Property | one-field mutations | semantic/provenance change→digest change; timestamp/elapsed→same | same digest or metadata pollution | independent oracle equality |
| T27 | `SemanticFingerprintTest.defensivelyCopiesAllCollections` | Immutability | mutate source list/array after build | problem/travel/digest unchanged; exposed mutation impossible | value/fingerprint changes | defensive copy assertions |
| T28 | `ProblemPreparationIT.preparesMixedProblemAndCompleteTravelWithoutRawFallback` | Failsafe integration | hand-calculated delivery-only+real pair, 2 vehicles, 3 locations | immutable problem + complete travel + exact fingerprints | unresolved arc/compile or assertion failure | all hand values and identity green |
| T29 | `ProblemPreparationIT.givesSolverAndVerifierTheSameTravelAuthority` | Contract integration | two independent consumer stubs | same type/fingerprint and query results | consumer-specific reconstruction differs | byte-for-byte oracle equality |
| T30 | `FixtureContractTest.exportsOnlyDeterministicTestScopeBuilders` | Architecture/contract | `build/test-fixtures` | fixed fingerprints, no runtime/app dependency | artifact unavailable or nondeterministic | downstream coordinate and expected digests stable |

T23의 distance policy는 test-only 명시 구현이다. 그것을 production Great Circle default로 등록하지 않는다. `B-AR1-GC-01`이 해소되지 않았다면 T23의 hook/rounding contract와 missing-policy rejection까지만 green일 수 있고, production function acceptance row는 evidence에서 `BLOCKED`로 남는다.

## 8. 테스트 우선 구현 순서

모든 slice는 아래 순서를 반복한다.

```text
test file 작성
→ targeted command 실행
→ 의도한 compile/assertion/error-code failure 확인·보존
→ 그 failure만 해소하는 최소 production API/구현
→ 같은 targeted command green
→ 다음 semantic test red
```

Production 파일을 먼저 일괄 scaffold하지 않는다. 한 test가 compile되지 않는 동안 다른 test를 한꺼번에 추가해 첫 failure를 가리지 않는다.

### Slice 순서

1. **Preflight/entry gate:** AR-0 DONE, tree overlap, source hash, Java/Maven을 확인한다. 실패 시 구현 0건으로 중단한다.
2. **Versioned canonical input:** T01 test를 먼저 추가해 missing type compile failure를 본다. 최소 identity/source/canonical aggregate를 만든 뒤 T01 green, T02 red→green을 진행한다.
3. **Numeric:** T03만 먼저 추가하고 `FixedPointNormalizer` missing compile failure를 본다. 최소 skeleton 후 T03의 `0 vs 1` assertion red를 확인한다. T04~T07을 한 행씩 red→green한다.
4. **Time/service:** T08 missing type/API red 후 최소 plan/window type을 만든다. T08~T11을 순차 red→green한다.
5. **Dense IDs/pair:** T12 missing ID compile red 후 ID/map 최소 구현. T12~T15를 순차 수행한다.
6. **Vehicle/terminal/compatibility:** T16~T20을 순차 red→green한다. Static no-compatible evidence를 exception으로 구현하지 않는다.
7. **Travel:** T21 missing `PreparedTravel` compile red 후 query-only skeleton. T21~T24를 순차 수행한다. T22가 green되기 전 solver-style consumer API를 추가하지 않는다.
8. **Win negative fixture:** T25를 먼저 red로 실행한다. Test helper는 lexical token만 읽고 fixture를 변환/복사하지 않는다. Decimal rejection green 뒤에도 official normalized artifact를 만들지 않는다.
9. **Fingerprint/immutability:** T26~T27을 red→green한다. Production fingerprint writer와 독립 oracle의 중복 구현을 의도적으로 유지한다.
10. **Integration:** T28~T29를 Failsafe red→green한다. Unit test green만으로 complete handoff를 주장하지 않는다.
11. **Downstream fixture:** Core module green 뒤에만 `build/test-fixtures` source와 T30을 작성해 red→green한다.
12. **Target green:** `rpdptw/core` 전체 `test`, `verify`; test-fixtures `verify`.
13. **Architecture/forbidden search:** Cloud/customer/wire/decimal coercion/sentinel/runtime fallback 침투를 검사한다.
14. **Reactor regression:** Root `mvn verify`.
15. **Evidence/diff/handoff:** Red와 green report, fingerprints, commands, diff scope를 bundle로 고정한다.

어떤 slice도 의도한 failure를 보지 못했거나 Surefire/Failsafe report에서 대상 method가 실행되지 않았다면 production 단계로 넘어가지 않는다. Dependency download, Java mismatch, 선행 module failure 또는 “No tests found”는 정상 red evidence가 아니다.

동일 working directory에서 다른 세션의 Maven 실행이 진행 중인 경우 shade artifact replace, report overwrite 또는 `target/` rename failure도 정상 red가 아니다. 해당 출력은 `CONCURRENT_TARGET_COLLISION_SUSPECTED`로 별도 기록하고, 다른 실행이 끝난 뒤 격리 재실행 결과만 red/green/regression 판정에 사용한다.

## 9. Exact 명령

모든 명령은 `/Users/brown/workspace/ro-next`에서 실행한다.

### 9.1 Preflight와 source drift

```bash
pwd
git status --short
git rev-parse HEAD
mvn -version
sha256sum docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md data/win_poc_case.json
test -f docs/codex/phases/phase-00-baseline-and-build-architecture.md
rg -n '^status: DONE$|phase: AR-0|rm_mapping: RM-0' docs/codex/phases/phase-00-baseline-and-build-architecture.md
rg --files pom.xml rpdptw build src README.md gcp data | sort
```

Expected source hashes는 metadata와 같아야 하고 Win fixture는 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이어야 한다. Hash drift나 AR-0 absence가 있으면 production/test 작성 전에 중단한다.

### 9.2 Targeted red/green

```bash
mvn -pl rpdptw/core -am -Dtest=CanonicalInputContractTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=FixedPointNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=TimeNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=DenseIdentityMapPropertiesTest,RequestPairPropertiesTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=VehicleAndTerminalNormalizerTest,CompatibilityNormalizerTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=PreparedTravelTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=WinFixtureTravelRejectionTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dtest=SemanticFingerprintTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/core -am -Dit.test=ProblemPreparationIT -Dfailsafe.failIfNoSpecifiedTests=false verify
mvn -pl build/test-fixtures -am -Dtest=FixtureContractTest -Dsurefire.failIfNoSpecifiedTests=false test
```

각 명령을 red와 green에 동일하게 사용한다. `-Dsurefire.failIfNoSpecifiedTests=false`는 `-am` 선행 module에 같은 test명이 없을 때만 허용한다. 대상 module의 `target/surefire-reports` 또는 `target/failsafe-reports`에서 class/method 실행과 failure reason을 별도로 확인한다.

### 9.3 Module·reactor regression

```bash
mvn -pl rpdptw/core -am test
mvn -pl rpdptw/core -am verify
mvn -pl build/test-fixtures -am verify
mvn verify
```

위 명령의 authoritative 결과는 다른 세션이 같은 working tree의 `target/`을 사용하지 않는 격리 실행에서 얻어야 한다. 동시 실행 중 shade JAR replace, report truncation, artifact rename 같은 transient failure는 blocker 표나 baseline 결함으로 옮기지 않고 격리 재실행한다.

### 9.4 금지 dependency/value/shortcut 검색

```bash
rg -n 'com\\.google|software\\.amazon|aws\\.|gcp\\.|CloudStorage|Workflow|HttpServer|ObjectMapper|JsonNode' rpdptw/core/src/main/java
rg -n 'WINCOMMERCE|WCM|customer(Name|Id).*equals|switch.*customer' rpdptw/core/src/main/java
rg -n '\\bdouble\\b|\\bfloat\\b|Math\\.round|Math\\.floor|Math\\.ceil' rpdptw/core/src/main/java
rg -n 'Long\\.MAX_VALUE|Integer\\.MAX_VALUE|9999|sentinel|reverseFallback|lazy.*travel|latest' rpdptw/core/src/main/java
rg -n 'parallelRuns|iterationsPerRun|screenMaxSteps|phase2MaxSteps|maxRounds|watchdog' rpdptw/core/src/main/java build/test-fixtures/src/main/java
rg -n 'data/win_poc_case\\.json' rpdptw/core/src/main/java build/test-fixtures/src/main/java
```

Expected match는 0건이다. Exact arithmetic 구현에서 `BigDecimal.setScale(..., FLOOR/HALF_UP)`과 rational CEILING을 써야 하므로 `Math.floor/ceil`을 허용하지 않는다. Test source의 명시 fixture 값은 official default로 오인되지 않도록 이름과 주석에 `TEST_ONLY`를 포함한다.

### 9.5 Link, heading와 diff

```bash
test -f docs/codex/phases/phase-01-input-domain-and-travel.md
rg -n '^#{1,3} ' docs/codex/phases/phase-01-input-domain-and-travel.md
rg -n 'docs/|rpdptw/|build/|src/|data/|gcp/' docs/codex/phases/phase-01-input-domain-and-travel.md
git diff --check -- docs/codex/phases/phase-01-input-domain-and-travel.md
git diff --name-only
git status --short
```

구현 세션은 시작 시 `git status --short` snapshot을 evidence에 보존하고 종료 snapshot과 비교한다. 기존 변경은 허용하되 AR-1이 새로 만든 차이는 §5의 경로와 `target/codex-evidence/AR-1/...`뿐이어야 한다.

## 10. Step-by-step implementation checklist

- [ ] `B-AR1-ENTRY-01` 해소와 AR-0 evidence ID/digest를 기록한다.
- [ ] 네 source 문서와 Win fixture hash가 metadata와 같은지 확인한다.
- [ ] 작업 시작 전 dirty tree와 각 대상 POM hash를 evidence에 저장한다.
- [ ] T01 test만 추가하고 expected compile red를 보존한다.
- [ ] Canonical version/source API 최소 구현 후 T01 green, T02 red→green을 만든다.
- [ ] T03 compile/assertion red를 본 뒤 numeric 최소 구현을 시작한다.
- [ ] T03~T07이 exact value/error로 green인지 확인한다.
- [ ] T08 compile red 뒤 time type을 만들고 T08~T11을 green으로 만든다.
- [ ] T12 compile red 뒤 dense IDs/map을 만들고 T12~T15 pair/reference 성질을 green으로 만든다.
- [ ] T16~T20의 terminal/ownership/compatibility red→green을 각각 보존한다.
- [ ] T21 compile red 뒤 travel query skeleton을 만들고 T21~T24를 순차 green으로 만든다.
- [ ] Great Circle production function을 승인 없이 구현하지 않는다. Blocker가 남으면 hook/error까지의 evidence와 차단 행을 분리한다.
- [ ] T25가 현재 Win fixture의 decimal D와 U를 모두 의도한 code로 거부하는지 확인한다.
- [ ] Win fixture 또는 변환본을 source/resource/target official artifact로 생성하지 않는다.
- [ ] T26~T27 fingerprint/immutability를 독립 oracle로 green으로 만든다.
- [ ] T28~T29 Failsafe integration을 red→green한다.
- [ ] Core green 뒤에만 downstream test-fixtures와 T30을 작성한다.
- [ ] Target module test/verify, test-fixtures verify, reactor verify를 순서대로 실행한다.
- [ ] Forbidden `rg` 검색 결과 0건과 dependency graph를 evidence에 넣는다.
- [ ] Error code/path, coverage count, fingerprints와 fixture hash를 evidence bundle에 넣는다.
- [ ] `git diff --check`, changed-path scope와 pre/post status diff를 검증한다.
- [ ] DONE/BLOCKED 판정과 다음 phase handoff를 작성한다.

## 11. Deliverables와 evidence bundle

### 11.1 Deliverables

| Deliverable | Identity/내용 | 소비자 |
|---|---|---|
| `CanonicalInput` contract | Schema, raw digest, adapter version, interpretation provenance | 향후 versioned adapter, `ProblemPreparer` |
| `ProblemInstance` | Dense mapping, request/nodes/vehicle/location/terminal, normalized policies, static evidence, fingerprint | AR-2 propagator/binder, AR-3 evaluator, AR-5 verifier |
| `PreparedTravel` | Complete directed distance, vehicle-resolved time, source/provenance/fingerprint | AR-2 propagation, AR-3 insertion, AR-5 candidate verifier |
| `PreparedInputs` | Problem/travel compatibility gate | Application preparation과 downstream fixtures |
| Typed error model | Stable pre-solve code/path/message | Adapter/application mapping |
| `rpdptw-test-fixtures` | Hand problem/travel builders와 independent fingerprint oracle | AR-2, AR-3, AR-5 tests |
| AR-1 evidence bundle | Red/green/regression/fingerprint/fault/diff/handoff | Progress 판정과 다음 phase entry |

### 11.2 Evidence 위치

```text
target/codex-evidence/AR-1/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
├── green/
├── regression/
├── fingerprints/
│   ├── problem.sha256
│   ├── prepared-travel.sha256
│   ├── canonical-encoding.txt
│   └── win-fixture.sha256
├── faults/
│   ├── numeric-time-errors.json
│   ├── pair-reference-errors.json
│   └── travel-completeness-errors.json
├── diff/
│   ├── status-before.txt
│   ├── status-after.txt
│   ├── changed-paths.txt
│   ├── forbidden-rg.txt
│   └── git-diff-check.txt
└── handoff.md
```

`evidence.json`에는 공통 필드 외에 `problemFingerprint`, `preparedTravelFingerprint`, `inputSchemaIdentity`, `numericPolicy`, `timePolicy`, `travelPolicy`, `matrixCoverage`, `winFixtureDigest`, `redTestMethods`, `greenTestMethods`, `greatCircleAuthority`를 기록한다.

Red report는 test class명만이 아니라 method, exit code, expected failure substring과 실제 관찰을 담는다. Compile red 뒤 assertion red가 필요한 slice는 둘 다 보존한다.

## 12. Rollback

### 12.1 원칙

- 기존 dirty change를 `git reset`, broad `git restore`, `git clean`으로 되돌리지 않는다.
- 시작 시 target path별 존재 여부/hash를 기록하고 AR-1 ownership manifest를 만든다.
- 신규 파일은 manifest에서 “AR-1이 생성했고 이후 다른 세션이 수정하지 않음”이 확인된 파일만 제거 대상으로 삼는다.
- 기존 `rpdptw/core/pom.xml`, `build/test-fixtures/pom.xml` 수정은 저장한 before blob과 AR-1 hunk만 역적용한다.
- Legacy source, root POM, README, GCP, Docker, data와 문서는 rollback 대상이 아니다.

### 12.2 Rollback 단위

| 단위 | 되돌릴 것 | 보존할 것 |
|---|---|---|
| Canonical/numeric/time slice | 해당 test와 §5.2 input/normalization 신규 파일, owner POM hunk | Red evidence와 pre-existing files |
| Domain/compatibility slice | ID/domain/compatibility test와 신규 파일 | 앞 slice green artifact가 다른 consumer에 쓰였으면 전체 AR-1 rollback로 승격 |
| Travel slice | Travel test/production 신규 파일 | Win fixture 원본, negative evidence |
| Fingerprint version | `rpdptw-semantic-v1` consumer가 없을 때만 code 제거 | 소비 artifact가 있으면 silent rewrite 금지; 새 version/ADR 필요 |
| Test-fixtures | Fixture module의 AR-1 신규 source와 dependency hunk | Core production과 evidence |

AR-1에는 DB/state/public logical cutover가 없다. 이미 downstream가 특정 fingerprint artifact를 소비했다면 기존 artifact를 덮어쓰거나 같은 version으로 encoding을 바꾸지 않는다. Rollback은 consumer 중단/호환 version 증가와 함께 수행한다.

## 13. DONE/BLOCKED 판정

### 13.1 `DONE` AND gate

다음을 모두 만족해야 `DONE`이다.

1. `AR-0 DONE`과 parent/module/architecture evidence가 존재한다.
2. §5의 required production/test/test-fixture가 존재하며 scope 밖 파일 변경이 없다.
3. T01~T30 각각에 의도한 red와 targeted green evidence가 있다.
4. Numeric/time/service hand values와 boundary/overflow rejection이 exact하다.
5. Dense ID bijection, pair/reference/terminal/service-pattern property test가 통과한다.
6. Size/capability/zone cases와 no-compatible static evidence가 통과한다.
7. 모든 `M²` distance와 vehicle×`M²` time이 solve 전에 해소되고 runtime fallback API가 없다.
8. Provided/generated/self/asymmetric source와 fingerprint가 독립 oracle과 같다.
9. Solver/verifier consumer stub가 같은 `PreparedTravel` fingerprint를 사용한다.
10. Current Win fixture는 decimal `D/U` 때문에 typed rejection되며 official artifact가 생성되지 않는다.
11. Great Circle missing-distance production path가 승인된 exact versioned policy로 구현되었거나, phase scope/authority가 승인 변경되어 같은 문서에 반영되었다. Test-only function만으로 충족할 수 없다.
12. Core/test-fixtures module verify와 reactor `mvn verify`가 통과한다.
13. Forbidden dependency/value/shortcut 검색 0건, evidence bundle digest와 재현 가능한 handoff가 있다.

### 13.2 `BLOCKED`

다음 중 하나면 `BLOCKED`다.

- AR-0 evidence/module이 없음 또는 target path가 사용자 변경과 충돌함
- Source hash drift가 의미 변경인지 판정되지 않음
- Great Circle exact production function/version을 추측해야만 missing-D 경로를 완료할 수 있음
- Canonical/wire schema를 승인된 것처럼 고정해야만 진행 가능함
- Integer-only 계약을 만족시키기 위해 decimal 값을 반올림/절삭해야 함
- Partial pair, numeric sentinel, reverse/lazy travel fallback 또는 floating tolerance가 필요함
- Target/architecture/reactor test failure가 unrelated 환경 오류가 아닌 실제 계약 위반임

`BLOCKED` evidence는 blocker ID, 필요한 authority, 마지막 green slice, 미실행 test와 재개 명령을 기록한다. Partial implementation을 `DONE`으로 표시하지 않는다.

## 14. 다음 phase handoff

### 14.1 AR-2 / RM-2

다음을 exact artifact identity로 전달한다.

- `ProblemInstance.fingerprint`
- `PreparedTravel.fingerprint`와 `requireCompatibleProblem`
- `PlanningHorizon`, expanded window, service pattern, vehicle/terminal/resource facts
- Static compatibility/servable vehicle set와 `PROVEN` evidence
- `CanonicalProblemFixture`, `PreparedTravelFixture`, independent fingerprint oracle
- Known blocker/정책 version과 numeric/time/travel provenance

AR-2는 raw coordinate/speed/input string을 다시 읽지 않고 `ProblemInstance`와 `PreparedTravel`만 사용해야 한다.

### 14.2 AR-3 / RM-3

- Stable dense request/vehicle/node/location IDs
- Exactly-two-node pair와 delivery-only logical pickup 의미
- Side-effect-free consumer가 사용할 immutable compatibility/travel query
- No-compatible request fixture와 asymmetric travel fixture

### 14.3 AR-5 / RM-5

- Candidate verifier가 solver/search/cache 없이 읽을 problem/travel authority
- Independent fingerprint oracle
- Pair/reference/terminal/travel corruption fixture의 원본 builder
- Runtime unresolved travel은 input rejection 또는 implementation defect라는 error boundary

다음 phase entry는 AR-1 evidence bundle의 digest와 두 artifact fingerprint를 확인해야 한다. 단순 class 존재나 unit test 수만으로 handoff를 수락하지 않는다.

## 15. Scope exclusions와 금지 shortcut

### 15.1 이 phase가 구현하지 않는 것

- Public/canonical JSON schema, HTTP API, Jackson production adapter와 legacy alias 최종 목록
- Propagation, route feasibility, wait/rest/stop/drive accumulation
- Profile binding, metric/score/objective/`SolvePlan`
- Pair insertion, portfolio, ALNS, COW state와 termination
- Candidate/result verifier, finalization, publication
- Application port, local runtime, multi-round coordinator
- Provider adapter, physical topology, deployment와 GCP migration
- Official Win manifest/baseline, `Q-BENCH-02` 수치
- Multi-trip/rotation, optional variant, route pool/MIP

### 15.2 금지 shortcut

- Core type에 외부 JSON을 직접 deserialize하거나 Jackson annotation 추가
- `double`/`float` 선변환, tolerance, 반올림/절삭으로 decimal cost/D/U 수용
- Item line total을 먼저 합산한 뒤 한 번만 `FLOOR`
- `Long.MAX_VALUE`, `9999`, plan end 또는 음수로 missing/failure 표현
- Pickup/delivery/terminal/location identity 병합
- Delivery-only logical pickup에 travel/stop/depot revisit 생성
- Partial pair를 infeasible/unassigned로 처리
- Vehicle size의 숫자/톤급 순서 추론, fixed allowlist, case folding
- `ALL`과 concrete size 혼합 허용, size/capability/zone 중 한 축 생략
- Missing D/U의 reverse copy, symmetry, runtime generation
- 승인되지 않은 Earth radius/library default
- 현재 Win fixture를 round/trim/converted “official” fixture로 생성
- README의 `8/5000`, GCP retry/timeout 또는 `Q-BENCH-02` 값을 core default로 복제
- Provider/customer name branch, `latest`, unordered registry/map iteration
- Java serialization/`toString()`/JSON order를 fingerprint authority로 사용
- Test red를 보지 않고 production 파일을 먼저 생성
- Surefire “no tests” 또는 unrelated compile failure를 red evidence로 인정

## 16. Phase 문서 계약 대응표

| Implementation Plan §10 항목 | 이 문서 |
|---|---|
| 1. 실제 저장소 조사 | §3, §9.1 |
| 2. Authority와 결정 상태 | §2 |
| 3. 현 상태→목표 gap | §3.2 |
| 4. 정확한 예상 경로 | §5 |
| 5. 예상 type/API/error | §6 |
| 6. Test case 표 | §7 |
| 7. 테스트 우선 순서 | §8 |
| 8. Exact 명령 | §9 |
| 9. Deliverable/evidence | §11 |
| 10. Rollback | §12 |
| 11. 완료·중단 조건 | §13 |
| 12. Handoff | §14 |
| 13. Scope exclusions | §15 |
