---
phase: AR-5
rm_mapping: RM-5
title: Verification, finalization과 publication
status: BLOCKED
document_status: REVIEW_DRAFT
implementation_entry_gate: BLOCKED
implementation_entry_gate_reason: AR-0~AR-4 실제 구현/DONE evidence와 AR-1/AR-2 authority artifact가 현재 저장소에 없음
document_role: 후속 LLM 구현 세션용 테스트 우선 실행 명세
language: ko
source_baseline:
  implementation_plan:
    path: docs/codex/implementation-plan.md
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  master_design:
    path: docs/master-design.md
    version: 3.2-review
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    path: docs/architecture-design.md
    version: 1.1-review
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    path: docs/domain-design.md
    version: 2.2-review
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
repository_observation:
  observed_at: 2026-07-24
  git_head: 523c23e2e13410885b16e974efe40ffe598106ee
  branch: codex/domain-design
  java: 25.0.3-amzn
  maven: 3.9.14
predecessor_documents:
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    required_status: DONE
    observed_at_authoring: absent
  - path: docs/codex/phases/phase-01-input-domain-and-travel.md
    required_status: DONE
    observed_at_authoring: absent
  - path: docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
    required_status: DONE
    observed_at_authoring: absent
  - path: docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
    required_status: DONE
    observed_at_authoring: absent
  - path: docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
    required_status: DONE
    observed_at_authoring: absent
authority_status:
  master_design: REVIEW
  architecture_design: REVIEW
  domain_design: REVIEW
  approved_public_api: false
  approved_wire_schema: false
  approved_storage_schema: false
---

# AR-5 / RM-5 — Verification, finalization과 publication

## 1. 목적과 문서 사용 규칙

이 phase의 목적은 search가 만든 `CommittedCandidate`의 주장을 solver와 search cache에 독립적으로 다시 계산하고, candidate `PASS` 뒤에만 finalization을 수행하며, final outcome·audit·diagnostic·summary·payload를 다시 검증한 뒤에만 내부 `PublishableResult`를 발급하는 것이다.

이 문서는 코드나 완료 보고가 아니다. 현재 설계는 모두 `REVIEW`이고 승인된 외부 API·wire schema·저장 schema가 없다. 아래 Java 이름과 signature는 후속 구현 세션 사이의 모호성을 없애기 위한 **계획상 제안 API**다. 승인된 외부 계약처럼 노출하거나, 외부 JSON field와 storage representation을 이 문서에서 확정한 것으로 해석하지 않는다.

후속 구현 세션은 다음 순서를 바꾸면 안 된다.

1. 이 문서의 source hash와 entry gate를 다시 확인한다.
2. 각 slice의 test source와 fixture를 production source보다 먼저 만든다.
3. 지정한 compile failure 또는 assertion failure를 실제로 관찰하고 보존한다.
4. 의도한 red evidence가 없으면 production 구현을 시작하지 않는다.
5. 최소 구현으로 targeted green을 만든다.
6. module verify, architecture check, reactor regression 순서로 넓힌다.
7. 두 verifier의 실제 `PASS`, corruption rejection과 publication/recovery gate evidence가 없으면 `DONE`으로 바꾸지 않는다.

## 2. Authority, 결정 상태와 blocker

### 2.1 규범 근거

| 근거 | 이 phase가 따를 계약 |
|---|---|
| [Master Design](../../master-design.md) §10 | Search bank와 final outcome 분리, candidate `PASS` 뒤 preliminary partition과 required audit, exactly-one outcome, provenance |
| [Master Design](../../master-design.md) §14.1 | Candidate verifier의 네 authority input, cache-free full recomputation, result-integrity verifier의 별도 authority, 두 gate 순서 |
| [Master Design](../../master-design.md) §15.7 | `RM-5` entry/deliverable/금지/exit evidence와 recovery/publication 경계 |
| [Domain Design](../../domain-design.md) §13 | `ASSIGNED`/`UNASSIGNED` two-state outcome, static `PROVEN`, required final-solution insertion audit, no-auto-fix |
| [Domain Design](../../domain-design.md) §15~§16 | Pre-solve/search defect/result failure 분리와 state/result acceptance evidence |
| [Architecture Design](../../architecture-design.md) §6.3 | `rpdptw-verification` package 책임과 `rpdptw-core` only compile dependency |
| [Architecture Design](../../architecture-design.md) §7 | Verification → solver/search/cache dependency 금지와 build enforcement |
| [Architecture Design](../../architecture-design.md) §18~§19 | 독립 corruption fixture, hand/small exhaustive oracle, module evidence와 `AR-5` gate |
| [전체 구현 계획](../implementation-plan.md) §5, §8, §9.6, §10~§11 | 관통 불변조건, red→green 순서, AR-5 실행 계약, 13개 phase 문서 항목과 evidence bundle |

### 2.2 관련 질문과 결정

| ID | 상태 | 이 phase의 적용 |
|---|---|---|
| `Q-RES-01` | `RESOLVED` | Solver outcome은 `ASSIGNED`/`UNASSIGNED`만 사용한다. `DIRECT`/`LEASE`는 vehicle ownership이고 둘 다 배정되면 `ASSIGNED`다. |
| `Q-RES-02` | `RESOLVED` | Static `PROVEN`을 제외한 모든 `UNASSIGNED`에 final routes 고정 exhaustive insertion audit가 필요하다. Feasible insertion을 찾아도 자동 적용·재탐색하지 않는다. |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | AR-5 generic verifier/finalizer는 막지 않는다. 공식 step/worker/round/watchdog 값, official manifest나 baseline을 만들 수는 없다. |
| `Q-INFRA-01` | `DEFERRED` | Physical publisher, provider SDK, object/database topology는 이 phase 밖이다. Logical eligibility만 만든다. |
| `Q-VAR-01` | `DEFERRED` | Optional variant를 위해 pair/terminal/audit 의미를 완화하지 않는다. |

### 2.3 Authority 충돌 처리

1. 채택된 외부 계약 또는 승인 ADR이 생기면 그것이 우선한다.
2. 승인 전 현재 `REVIEW` 문서 사이 의미 충돌은 코드로 임의 해결하지 않는다.
3. Domain 의미 충돌은 Master의 불변조건과 질문 등록부로, module/package 충돌은 Architecture Design으로 되돌린다.
4. 계획상 제안 API가 선행 phase의 승인된 실제 API와 다르면 adapter나 solver dependency로 우회하지 않는다. 영향 문서와 ADR을 같은 변경 단위에서 갱신한 뒤 재개한다.
5. External payload/canonical encoding 승인 부재는 내부 typed result와 versioned canonical projection test를 막지 않는다. 다만 이를 public wire contract로 게시하는 일은 `BLOCKED`다.

### 2.4 현재 blocker와 재개 조건

| Blocker | 현재 관찰 | 차단 범위 | 재개 조건 |
|---|---|---|---|
| `B-AR5-01` 선행 phase evidence 부재 | 현재 repository에는 AR-0~AR-4 phase 문서/evidence와 target reactor가 없음 | 모든 AR-5 production 구현과 `DONE` | AR-0~AR-4 문서가 `DONE`, 실제 artifact와 evidence digest가 존재 |
| `B-AR5-02` candidate 중립 authority projection 부재 | 현재 `CommittedCandidate`, immutable route/bank/evaluation projection이 없음 | Solver dependency 없는 candidate verifier 입력 | AR-4 actual `WorkerRunResult`가 last committed route/bank와 declared cache-free evaluation을 제공하고 application-side lossless projection test가 통과 |
| `B-AR5-03` full evaluation/insertion seam 부재 | `ProblemInstance`, `PreparedTravel`, `BoundProfile`, stateless full evaluation과 insertion evaluator가 없음 | Cache-free recomputation과 required audit | AR-1/AR-2가 immutable authority와 side-effect-free evaluator를 제공 |
| `B-AR5-04` static proof vocabulary 부재 | Static unassignability evidence type/contract가 없음 | `PROVEN` audit skip | AR-1/AR-2가 versioned static proof code·evidence·request binding을 제공 |
| `B-AR5-05` internal canonical encoding ADR 미확인 | External/public result schema는 미승인 | Public serialization/publication | 내부 `RPDPTW_RESULT_CANON_V1` 제안은 test 가능. 외부 노출은 승인 전 차단 |

`B-AR5-01`~`04`는 현재 구현 entry를 막는다. 문서 작성은 진행할 수 있지만 placeholder domain이나 mock-only authority로 phase를 `DONE` 처리할 수 없다. `Q-BENCH-02`, decimal Win fixture, provider/product와 optional variant는 AR-5 generic 구현의 blocker가 아니며 이 phase에서 값을 추측해 해소해서도 안 된다.

## 3. 작성 시점 실제 저장소 조사

### 3.1 Build/module graph

- Root [pom.xml](../../../pom.xml)은 `packaging`을 생략한 단일 `jar` project다.
- Java release 25, Maven `[3.9.14,)`, Java `[25,26)` Enforcer는 존재한다.
- Google Workflow Executions, Cloud Storage, Jackson과 JUnit이 root classpath에 직접 있다.
- Shade main class는 `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer`다.
- 목표 `rpdptw/core`, `rpdptw/solver`, `rpdptw/verification`, `rpdptw/application`, `build/test-fixtures`, `build/architecture-rules` module은 아직 없다.
- 작성 시점 toolchain은 Maven 3.9.14, Amazon Java 25.0.3이다.
- 관리 세션이 격리 실행한 phase-00 공통 baseline에서 기존 root `mvn test`와 `mvn verify`는 성공했다. 여러 문서 세션이 같은 `target/`에서 동시에 Maven을 실행해 생기는 shade JAR replace 등 transient failure는 repository baseline 결함이나 AR-5 blocker로 판정하지 않는다. 이 문서 세션은 Maven을 재실행해 경쟁하지 않고 link/hash/diff/scope 검증만 수행한다.

현재 목표 dependency와 차이는 다음과 같다.

```text
current:
  one root jar
  └─ Google SDK + Jackson + placeholder application + HTTP controllers

target before AR-5 can start:
  rpdptw-verification
  └─ compile: rpdptw-core
     test: JUnit/property test support only

forbidden:
  rpdptw-verification → rpdptw-solver
  rpdptw-verification → application/adapters/provider SDK
  verification.* → search/cache package
```

### 3.2 Production/test 현황

현재 production source는 다음 6개 class뿐이다.

```text
src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java
src/main/java/com/ronext/optimizer/adapter/in/http/HttpJson.java
src/main/java/com/ronext/optimizer/adapter/in/http/JsonSupport.java
src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java
src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java
src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java
```

현재 test는 `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` 하나다. 이 test는 placeholder `Map<String,Object>`의 `status=CANDIDATE`, run number와 양의 `double objective`만 검사한다. RPDPTW route, request pair, bank, travel, metric, objective, verifier, audit, outcome, summary, fingerprint와 publication gate evidence는 전혀 없다.

Legacy `OptimizationWorkerController.finalizeResult`는 다음 동작을 한다.

1. `candidates/<requestId>/` prefix의 존재하는 object만 나열한다.
2. 선언 worker completeness를 확인하지 않는다.
3. `double objective` 최소 하나를 고른다.
4. Candidate verification, required insertion audit와 result verification 없이 `status=COMPLETED`를 쓴다.
5. `results/<requestId>.json`을 unconditional create하고 API가 raw bytes를 200으로 반환한다.

이 동작은 characterization/compatibility 대상이지 AR-5 목표 구현이나 publication evidence가 아니다. AR-5는 legacy controller, endpoint, GCP workflow를 수정하지 않는다.

### 3.3 README/GCP/data 현황

- Root [README](../../../README.md)의 `parallelRuns=8`, `iterationsPerRun=5000`과 seed 예시는 legacy 실행 예시이며 `Q-BENCH-02` 공식값이 아니다.
- [GCP workflow](../../../gcp/workflows/optimization.yaml)는 worker batch 뒤 `/internal/finalize`를 호출하지만 completeness, verifier와 CAS gate를 보장하지 않는다.
- [GCP README](../../../gcp/README.md), [cloudbuild.yaml](../../../gcp/cloudbuild.yaml)과 root Docker path는 `Q-INFRA-01`의 답이 아니라 legacy characterization 자료다.
- `data/win_poc_case.json`의 SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이다. 452 orders, 31 vehicles, 205,209 matrix rows가 있고 대다수 `D/U`가 소수 문자열이다. Official normalization/baseline에는 사용할 수 없다.
- `data/ro_input_json_spec.pdf`는 1-page legacy CVRPTW 사례로 canonical RPDPTW input schema가 아니다.
- 작성 시점에는 `target/codex-evidence/` 선행 evidence가 없다.

### 3.4 Source baseline과 drift 판정

| Source | 상위 계획에 기록된 SHA-256 | 작성 시점 SHA-256 | Drift |
|---|---|---|---|
| `docs/master-design.md` | `5e6a7901...45f9c` | `5e6a7901...45f9c` | 없음 |
| `docs/architecture-design.md` | `161b08e8...8b212` | `161b08e8...8b212` | 없음 |
| `docs/domain-design.md` | `3a98d34b...f98e2` | `3a98d34b...f98e2` | 없음 |

설계 파일이 working tree에서 수정 상태여도 content hash는 상위 구현 계획의 source baseline과 일치한다. 후속 구현 세션은 이 표를 믿고 건너뛰지 말고 §12 명령으로 다시 계산한다. 하나라도 다르면 `SOURCE_DRIFT`로 중단하고 영향 절을 재검토한다.

## 4. 현 상태 → 목표 상태 gap

| 영역 | 현재 | AR-5 목표 | 이 phase의 처리 |
|---|---|---|---|
| Candidate authority | `Map<String,Object>`와 `double objective` | Immutable problem/profile/travel + route order + bank + declared evaluation | AR-1/2/4 artifact를 소비; legacy map 변환 금지 |
| Candidate 검증 | 없음 | Solver/search/cache 없는 full traversal과 recomputation | `rpdptw-verification`에 독립 verifier 신규 |
| Search cache | Verifier 자체가 없음 | Input type에서 cache를 표현하지 않고 poisoned cache와 결과 독립 | API shape + architecture/corruption test |
| Final partition | Candidate object를 통째로 best로 선택 | Verified routes에서 preliminary `ASSIGNED`, request universe 차집합으로 `UNASSIGNED` | Finalizer 신규 |
| Static proof | 없음 | Versioned normalization/static evidence가 있을 때만 `PROVEN` | Proof binding 검증, audit skip을 명시 |
| Required audit | 없음 | Non-static unassigned의 모든 eligible vehicle/legal pair 검사 | Exhaustive auditor 신규 |
| Feasible audit finding | 해당 없음 | Internal record만 보존, route/outcome 자동 변경 금지 | No-auto-fix test |
| Outcome | `COMPLETED`와 raw best candidate | 모든 request exactly-one `ASSIGNED/UNASSIGNED`; ownership 분리 | Typed result API 신규 |
| Diagnostic | 없음 | Code/scope/confidence/source/evidence, confidence ceiling | Typed internal contract 신규 |
| Summary | Candidate map | Outcomes에서 재계산한 summary | Finalizer 생성 + result verifier 재계산 |
| Result integrity | 없음 | Outcome/audit/summary/payload independent verification | Result verifier 신규 |
| Fingerprint | 없음 | Versioned canonical solution/result/payload identity | Internal canonical projection/encoder 신규 |
| Publication | GCS raw write, one-gate도 없음 | Both-pass normal `PublishableResult`; exceptional recovery 별도 | Logical gate만 신규; physical publisher는 AR-6 |
| Architecture | 단일 root JAR | Verification → core only, solver/search/cache/SDK 0 | POM/bytecode/package architecture gate |
| Legacy path | 실제 정상 path | Characterization 뒤 AR-8까지 유지 | 이 phase에서 이동·삭제·수정 금지 |

폐기 또는 이동은 AR-5 범위에 없다. 특히 `src/main/java/com/ronext/optimizer/**`, root POM, README, GCP와 data는 변경하지 않는다. AR-0 migration 결과에 `legacy/current-app`이 존재하면 그대로 보존한다.

## 5. Entry contract와 선행 artifact

### 5.1 구현 시작 전 AND gate

다음이 모두 참이어야 첫 test 작성에 들어간다.

- AR-0~AR-4 phase 문서가 `DONE`이고 evidence bundle digest를 제공한다.
- Reactor에 `rpdptw-core`, `rpdptw-solver`, `rpdptw-verification`, `build/test-fixtures`, `build/architecture-rules`가 존재한다.
- `mvn -pl rpdptw/verification -am test`가 AR-5 test 추가 전 baseline으로 통과한다.
- AR-1의 immutable `ProblemInstance`, complete `PreparedTravel`, dense ID, static precheck evidence와 fingerprints를 읽을 수 있다.
- AR-2의 immutable `BoundProfile`, stateless full propagation/evaluation과 side-effect-free atomic insertion evaluator를 읽을 수 있다.
- AR-4의 `CommittedCandidate`가 route/bank source of truth, declared metric/objective, identity와 termination을 immutable하게 제공한다.
- Verification module이 solver class를 받지 않도록 AR-4 candidate에서 AR-5 `CandidateAuthoritySnapshot`으로 lossless projection할 수 있다.
- 선행 source/evidence digest가 phase 문서와 실제 artifact에서 일치한다.

하나라도 거짓이면 status는 `BLOCKED`다. Test double로 빈자리를 채워 package를 만드는 것은 가능해도 phase exit나 production authority로 승격할 수 없다.

### 5.2 선행 API에 요구하는 중립 handoff

관리 세션의 교차 검토로 AR-3/AR-4 선행 계획은 `CommittedCandidate` canonical owner/path를 `com.ronext.rpdptw.solver.state` / `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CommittedCandidate.java`로 정합화했다. Portfolio는 이 state candidate를 생산·소비할 뿐 별도 candidate type, alias나 compatibility wrapper를 소유하지 않는다. 이 문서도 해결된 선행 계획 계약을 그대로 소비한다. 아래는 그 handoff를 verification의 금지 dependency와 함께 만족시키는 **계획상 제안 projection API**다. 승인된 public API가 아니며, 선행 phase가 동등한 다른 이름을 승인했다면 이 문서와 trace를 먼저 갱신한다.

```java
// rpdptw-verification API가 소유하는 immutable input projection.
// application/composition test가 AR-4 candidate에서 cache field 없이 lossless하게 만든다.
public record CandidateAuthoritySnapshot(
        SolutionIdentity identity,
        List<CandidateRouteSnapshot> routes,
        Set<RequestId> searchRequestBank,
        DeclaredSolutionEvaluation declaredEvaluation
) {
}

// AR-4 WorkerRunResult가 보장해야 할 verification handoff 의미.
// 아래 solver type은 verification signature에 등장하지 않는다.
public interface WorkerRunVerificationHandoff {
    CommittedCandidate lastCommittedCandidate(); // stable routes + bank + identity
    SolutionEvaluation declaredEvaluation();     // last committed candidate의 cache-free claim
    TerminationRecord termination();
    CandidateLineage lineage();
}
```

`CandidateAuthoritySnapshot`에는 arrival/load/cache/insertion table, solver feasibility flag, search summary 또는 mutable collection이 들어가면 안 된다. Verification module은 `CommittedCandidate`, `WorkerRunResult`나 solver package에 compile-depend하지 않는다. Application-side integration test는 last committed candidate의 모든 route/node, bank membership, declared cache-free evaluation과 identity가 누락·재해석 없이 복사되는지 검증한다. AR-6 application/worker가 같은 projection과 별도 termination lineage를 production gate에 전달한다.

## 6. 정확한 예상 경로

아래 표의 모든 신규 이름은 **계획상 제안 경로/API**다. Test source를 먼저 만들고, 해당 red를 관찰한 뒤에만 연결된 production row를 생성한다.

### 6.1 먼저 생성할 test와 fixture

| 순서 | 경로 | 신규/변경 | 책임 |
|---:|---|---|---|
| 1 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/fixture/CandidateVerificationFixtures.java` | 신규 | Hand oracle candidate, 독립 corruption 변형, cache poison 비입력 fixture |
| 2 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/fixture/FinalizationFixtures.java` | 신규 | Static-proven, exhaustive-no-fit, feasible-insertion-found의 literal option oracle |
| 3 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/fixture/ResultCorruptionFixtures.java` | 신규 | Outcome/audit/summary/payload 단일-field corruption |
| 4 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/candidate/CandidateSolutionVerifierContractTest.java` | 신규 | 정상 hand oracle와 report binding |
| 5 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/candidate/CandidateSolutionVerifierCorruptionTest.java` | 신규 | Authority input별 독립 corruption matrix |
| 6 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/candidate/CandidateSolutionVerifierCacheIndependenceTest.java` | 신규 | Poisoned search cache가 input/result에 영향 없음 |
| 7 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/result/finalization/PreliminaryOutcomeBuilderTest.java` | 신규 | Verified routes에서 outcome partition 파생 |
| 8 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/result/finalization/FinalResultInsertionAuditTest.java` | 신규 | Static skip, required option coverage, no-auto-fix |
| 9 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/result/finalization/DiagnosticConfidenceTest.java` | 신규 | Evidence source별 confidence ceiling |
| 10 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/result/ResultIntegrityVerifierContractTest.java` | 신규 | Exactly-one, ownership, summary와 canonical identity |
| 11 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/result/ResultIntegrityVerifierCorruptionTest.java` | 신규 | Outcome/audit/summary/payload corruption matrix |
| 12 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/result/CanonicalResultEncoderTest.java` | 신규 | Stable order/version/fingerprint |
| 13 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/result/PublicationGateTest.java` | 신규 | Both-pass, fail/incomplete, normal/recovery |
| 14 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/result/PublicationGateFaultTest.java` | 신규 | Exception/forged/mismatched report가 publication으로 변환되지 않음 |
| 15 | `rpdptw/verification/src/test/java/com/ronext/rpdptw/verification/VerificationCorruptionSuiteTest.java` | 신규 | Candidate와 result corruption case inventory 누락 방지 |
| 16 | `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/VerifierDependencyArchitectureTest.java` | 변경/신규 | Verification의 solver/search/cache/SDK dependency와 package edge 금지 |

`*IT`가 실제 cross-module handoff를 요구하는 경우에만 아래 test를 추가한다. AR-4의 actual `CommittedCandidate`를 application-side test harness가 중립 view로 넘기되 verification module POM에는 solver test dependency를 추가하지 않는다.

| 경로 | 신규/변경 | 책임 |
|---|---|---|
| `rpdptw/application/src/test/java/com/ronext/rpdptw/application/verification/CommittedCandidateVerificationIT.java` | 신규 | 실제 AR-4 candidate → neutral authority → candidate verifier → finalization → result verifier 연결 |

### 6.2 Test red 뒤 생성할 production source

| Slice | 정확한 경로 | 신규/변경 | 책임 |
|---|---|---|---|
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateSolutionVerifier.java` | 신규 | Candidate verifier port |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateVerificationInput.java` | 신규 | 네 authority input만 보유 |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateAuthoritySnapshot.java` | 신규 | AR-4 route/bank/declared evaluation의 cache-free 중립 projection |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateRouteSnapshot.java` | 신규 | Vehicle/terminal과 ordered node ID만 가진 immutable route projection |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateVerificationResult.java` | 신규 | `Passed`/`Failed`/`Incomplete` sealed result |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateVerificationReport.java` | 신규 | Contract/version, binding fingerprints, decision, ordered findings |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateVerificationFinding.java` | 신규 | Typed failure code/scope/evidence |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/CandidateFailureCode.java` | 신규 | Candidate corruption category |
| Candidate API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/api/VerifiedSolution.java` | 신규 | Cache-free recomputed immutable solution |
| Candidate impl | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/candidate/CacheFreeCandidateSolutionVerifier.java` | 신규 | Full traversal, propagation/evaluation, declared-vs-recomputed 비교 |
| Candidate impl | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/candidate/CandidateVerificationOrder.java` | 신규 | Stable finding/traversal order |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/RequestOutcome.java` | 신규 | Sealed `Assigned`/`Unassigned` internal result contract |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/OutcomeStatus.java` | 신규 | `ASSIGNED`, `UNASSIGNED`만 허용 |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/UnassignedDiagnostic.java` | 신규 | Code/scope/confidence/source/evidence |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/DiagnosticConfidence.java` | 신규 | `PROVEN`, `EXHAUSTIVE_FOR_FINAL_SOLUTION`, `SEARCH_OBSERVED`, `UNKNOWN` |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/AuditOptionKey.java` | 신규 | Request/vehicle/pickup-position/delivery-position identity |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/AuditOptionResult.java` | 신규 | Feasible 또는 typed rejection; candidate mutation 없음 |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/RequestInsertionAudit.java` | 신규 | Required/static-skip disposition, ordered option set, counters, completion |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/FinalizationAuditBundle.java` | 신규 | 모든 unassigned request audit disposition |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/ResultSummary.java` | 신규 | Outcome-derived counts와 ownership breakdown |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/FinalResult.java` | 신규 | Outcomes, diagnostics, audit reference, summary, lineage |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/ResultPayloadProjection.java` | 신규 | Internal canonical payload projection; public wire DTO 아님 |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/PublishableResult.java` | 신규 | Both-pass normal result artifact |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/RecoveryCandidate.java` | 신규 | Exceptional termination의 both-pass 별도 artifact |
| Result API | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/api/PublicationTermination.java` | 신규 | AR-4 termination reason/record fingerprint의 solver-free projection |
| Finalization | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/finalization/PreliminaryOutcomeBuilder.java` | 신규 | Verified routes와 request universe로 preliminary partition |
| Finalization | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/finalization/FinalSolutionInsertionAuditor.java` | 신규 | Required exhaustive option enumeration/evaluation |
| Finalization | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/finalization/FinalResultFinalizer.java` | 신규 | Candidate pass 전용 finalization port |
| Finalization | `rpdptw/verification/src/main/java/com/ronext/rpdptw/result/finalization/DefaultFinalResultFinalizer.java` | 신규 | Audit, diagnostic, outcome-derived summary 조립 |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/ResultIntegrityVerifier.java` | 신규 | Result verifier port |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/ResultVerificationInput.java` | 신규 | Candidate pass + final artifacts + payload |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/ResultVerificationResult.java` | 신규 | `Passed`/`Failed`/`Incomplete` |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/ResultIntegrityReport.java` | 신규 | Ordered findings와 canonical bindings |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/ResultFailureCode.java` | 신규 | Outcome/audit/summary/payload failure categories |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/CacheFreeResultIntegrityVerifier.java` | 신규 | Finalizer package를 신뢰하지 않는 independent 검증 |
| Result verify | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/CanonicalResultEncoder.java` | 신규 | `RPDPTW_RESULT_CANON_V1` canonical bytes/fingerprint |
| Gate | `rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result/PublicationGate.java` | 신규 | Normal publishable/recovery/rejection 판정 |
| Build | `rpdptw/verification/pom.xml` | 필요 시 변경 | Core compile + test dependency만; solver/application/SDK 금지 |

### 6.3 소비만 하고 수정하지 않을 선행 경로

다음은 선행 phase의 예상 owner다. AR-5 구현이 이 파일을 수정해야 한다면 먼저 scope/ADR과 phase 문서를 갱신한다.

```text
rpdptw/core/src/main/java/com/ronext/rpdptw/domain/ProblemInstance.java
rpdptw/core/src/main/java/com/ronext/rpdptw/travel/PreparedTravel.java
rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/BoundProfile.java
rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/FullEvaluationEngine.java
rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionEvaluator.java
rpdptw/core/src/main/java/com/ronext/rpdptw/domain/StaticCompatibility.java
rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CommittedCandidate.java
rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/WorkerRunResult.java
rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/TerminationRecord.java
```

경로가 선행 phase의 승인된 계획상 제안과 달라질 수는 있으나 의미 owner와 dependency direction은 달라질 수 없다.

## 7. 계획상 제안 type/API와 불변조건

### 7.1 Candidate verification API

```java
public interface CandidateSolutionVerifier {
    CandidateVerificationResult verify(CandidateVerificationInput input);
}

public record CandidateVerificationInput(
        ProblemInstance problem,
        PreparedTravel preparedTravel,
        BoundProfile boundProfile,
        CandidateAuthoritySnapshot candidate
) {}

public sealed interface CandidateVerificationResult {
    record Passed(
            CandidateVerificationReport report,
            VerifiedSolution verifiedSolution
    ) implements CandidateVerificationResult {}

    record Failed(CandidateVerificationReport report)
            implements CandidateVerificationResult {}

    record Incomplete(
            CandidateVerificationReport report,
            VerificationAbort abort
    ) implements CandidateVerificationResult {}
}
```

계약:

- `CandidateVerificationInput`에는 solver type, cache, feasibility flag, insertion result와 solver summary가 없다.
- Candidate identity에 기록된 problem/travel/profile fingerprint가 실제 authority와 다르면 첫 binding 단계에서 `FAIL`이다.
- 모든 route와 bank를 stable dense/external ID order로 순회한다.
- Pair partition, exactly once, same vehicle, precedence, terminal/service pattern과 bank XOR를 full traversal한다.
- 모든 actual directed leg를 `PreparedTravel`에서 조회한다. Missing/unresolved arc는 fallback이 아니라 `FAIL`이다.
- Load/time/window/resource/static compatibility와 registered hard constraints를 stateless full propagation으로 재계산한다.
- Neutral metrics, score breakdown과 objective vector를 다시 계산하고 candidate declared value와 exact 비교한다.
- `VerifiedSolution`은 recomputed values만 포함하며 candidate cache/summary를 복사하지 않는다.
- Semantic corruption은 예외가 아니라 deterministic ordered finding의 `Failed`다.
- Cancellation, resource interruption 또는 internal exception은 `Incomplete`이며 `UNASSIGNED`나 `PASS`로 바꾸지 않는다.

계획상 제안 candidate failure code:

```text
IDENTITY_MISMATCH
REQUEST_PARTITION_INVALID
PAIR_DUPLICATE_OR_PARTIAL
PAIR_SPLIT_OR_PRECEDENCE_INVALID
TERMINAL_POLICY_INVALID
SERVICE_PATTERN_INVALID
STATIC_COMPATIBILITY_INVALID
TRAVEL_FINGERPRINT_MISMATCH
TRAVEL_ARC_MISSING
ROUTE_HARD_INFEASIBLE
DECLARED_METRIC_MISMATCH
DECLARED_SCORE_MISMATCH
DECLARED_OBJECTIVE_MISMATCH
CANONICAL_SOLUTION_FINGERPRINT_MISMATCH
VERIFICATION_ABORTED
```

### 7.2 Preliminary outcome와 finalization API

```java
public interface FinalResultFinalizer {
    FinalizationResult finalize(
            CandidateVerificationResult.Passed candidatePass,
            ApprovedDiagnosticEvidence diagnosticEvidence
    );
}

public sealed interface RequestOutcome {
    RequestId requestId();

    record Assigned(
            RequestId requestId,
            RouteId routeId,
            VehicleId vehicleId,
            NodeId pickupNodeId,
            NodeId deliveryNodeId
    ) implements RequestOutcome {}

    record Unassigned(
            RequestId requestId,
            UnassignedDiagnostic diagnostic
    ) implements RequestOutcome {}
}
```

불변조건:

- Preliminary outcome은 verified route membership에서 만든다. Search bank의 final serialization이 아니다.
- 모든 input request는 preliminary와 final result에서 정확히 한 번 나타난다.
- `DIRECT`와 `LEASE` vehicle route는 모두 `Assigned`다. Ownership은 vehicle reference와 summary breakdown에서만 구분한다.
- `Assigned`는 verified route/vehicle/pair를 정확히 참조한다. `Unassigned`는 route/vehicle reference를 가질 수 없다.
- Static proof는 problem/request/fingerprint와 exact proof contract version에 bind되어야 한다.
- Static proof가 없는 모든 unassigned request는 required audit 대상이다.
- Finalizer는 candidate `Passed` type 없이는 호출할 수 없다. `Failed`/`Incomplete`를 받는 overload를 만들지 않는다.

### 7.3 Required final-solution insertion audit

```java
public interface FinalSolutionInsertionAuditor {
    RequestInsertionAudit audit(
            VerifiedSolution fixedSolution,
            RequestId unassignedRequestId
    );
}

public record AuditOptionKey(
        RequestId requestId,
        VehicleId vehicleId,
        int pickupInsertionIndex,
        int deliveryInsertionIndex
) {}
```

Audit 규칙:

1. Verified final routes와 다른 request placement를 고정한다.
2. 해당 request의 모든 eligible input vehicle을 stable vehicle ID order로 열거한다.
3. 각 vehicle route의 모든 합법 pickup/delivery position pair를 stable position order로 열거한다.
4. 각 option을 AR-2의 side-effect-free atomic pair insertion evaluator와 cache-free propagation/evaluation으로 검사한다.
5. Attempt key, feasible/rejection, rejection code, 검사 vehicle/position 수와 contract version을 기록한다.
6. Work count와 elapsed metadata를 ALNS step/quality vector에서 분리한다.
7. 모든 option이 실패한 완결 audit만 `EXHAUSTIVE_FOR_FINAL_SOLUTION`과 `NO_FEASIBLE_INSERTION_IN_FINAL_SOLUTION`을 허용한다.
8. Feasible option을 하나라도 찾아도 enumeration 계약이 “전수”이면 나머지 option까지 검사한다. Early-success stop으로 exhaustive evidence를 위조하지 않는다.
9. Feasible option 발견 시 route/bank/outcome을 변경하거나 solver를 재호출하지 않는다. Internal audit finding만 남긴다.
10. Audit 중 exception/cancel/resource interruption은 `INCOMPLETE`다. Result verification과 publication을 차단한다.

Audit disposition은 정확히 다음 중 하나다.

```text
STATIC_PROVEN_AUDIT_NOT_REQUIRED
REQUIRED_COMPLETED_NO_FEASIBLE_INSERTION
REQUIRED_COMPLETED_FEASIBLE_INSERTION_FOUND
REQUIRED_INCOMPLETE
```

### 7.4 Diagnostic와 confidence ceiling

계획상 제안 내부 diagnostic 구조:

```java
public record UnassignedDiagnostic(
        DiagnosticCode code,
        DiagnosticScope scope,
        DiagnosticConfidence confidence,
        DiagnosticSource source,
        EvidenceRef evidence
) {}
```

| Evidence | 허용 confidence 상한 | 금지 |
|---|---|---|
| Versioned normalization/static proof | `PROVEN` | Search failure를 static proof로 승격 |
| Complete audit, feasible option 0 | `EXHAUSTIVE_FOR_FINAL_SOLUTION` | 전역 재배치 불가능으로 표현 |
| Complete audit, feasible option ≥1 | `SEARCH_OBSERVED` 또는 `UNKNOWN`인 별도 승인 source만 | `NO_FEASIBLE_INSERTION...`, audit finding의 public proven reason화 |
| Bounded search telemetry | `SEARCH_OBSERVED` | `PROVEN`/`EXHAUSTIVE` |
| Source/evidence 없음 | `UNKNOWN` | Last failure나 summary 추측 |

정확한 public diagnostic code와 JSON 표현은 승인되지 않았다. 이 phase는 internal versioned typed contract와 검증 rule까지만 만든다.

### 7.5 Result-integrity API

```java
public interface ResultIntegrityVerifier {
    ResultVerificationResult verify(ResultVerificationInput input);
}

public record ResultVerificationInput(
        CandidateVerificationResult.Passed candidatePass,
        FinalResult finalResult,
        FinalizationAuditBundle auditBundle,
        ResultSummary claimedSummary,
        ResultPayloadProjection payload
) {}

public sealed interface ResultVerificationResult {
    record Passed(ResultIntegrityReport report) implements ResultVerificationResult {}
    record Failed(ResultIntegrityReport report) implements ResultVerificationResult {}
    record Incomplete(
            ResultIntegrityReport report,
            VerificationAbort abort
    ) implements ResultVerificationResult {}
}
```

Result verifier는 다음을 finalizer implementation에 의존하지 않고 다시 확인한다.

- Candidate report, verified solution과 final artifact의 problem/travel/profile/solution fingerprint binding
- Input request universe와 outcome의 exactly-one partition
- Assigned route/vehicle/pair/ownership reference와 verified solution의 일치
- Unassigned가 verified route에 없고 route reference도 없는지
- Static proof request만 audit skip했는지
- 모든 non-static unassigned에 audit가 있고 complete인지
- Expected eligible vehicle/legal position `AuditOptionKey` 집합과 recorded set이 정확히 같은지
- Audit option finding과 confidence ceiling이 일치하는지
- Outcomes에서 재계산한 summary와 claimed summary가 정확히 같은지
- Canonical payload projection을 재생성했을 때 field/order/value가 같은지
- Canonical solution/result/payload fingerprint binding이 같은지

`verification.result` package는 `result.finalization` implementation을 import하지 않는다. 공통으로 필요한 immutable result types는 `result.api`, independent option evaluation은 `rpdptw-core`만 사용한다.

계획상 제안 result failure code:

```text
CANDIDATE_PASS_BINDING_MISMATCH
OUTCOME_MISSING_OR_DUPLICATE
ASSIGNED_REFERENCE_MISMATCH
UNASSIGNED_ROUTE_REFERENCE_PRESENT
OWNERSHIP_REFERENCE_MISMATCH
STATIC_PROOF_INVALID
AUDIT_REQUIRED_BUT_MISSING
AUDIT_OPTION_SET_INCOMPLETE
AUDIT_FINDING_MISMATCH
DIAGNOSTIC_CONFIDENCE_OVERSTATED
SUMMARY_MISMATCH
PAYLOAD_PROJECTION_MISMATCH
RESULT_FINGERPRINT_MISMATCH
PAYLOAD_FINGERPRINT_MISMATCH
RESULT_VERIFICATION_ABORTED
```

### 7.6 Canonical result와 fingerprint

`CanonicalResultEncoder`는 **계획상 제안 내부 artifact contract**인 `RPDPTW_RESULT_CANON_V1`만 처리한다.

- Request, route, vehicle, audit option과 finding은 stable semantic ID/order로 정렬한다.
- Map iteration, object identity, timestamp, elapsed, provider locator와 completion order는 canonical bytes에 넣지 않는다.
- Numeric 값은 predecessor exact integer/value representation을 그대로 사용한다.
- Unknown field skip, `latest` schema, locale/timezone formatting과 floating serialization을 금지한다.
- Fingerprint algorithm ID와 canonicalization contract ID를 report와 artifact에 함께 기록한다.
- External JSON/wire DTO는 AR-8/승인된 API 작업까지 별도다.

Encoding contract 또는 digest algorithm이 선행 승인과 충돌하면 임의 호환 layer를 넣지 말고 `B-AR5-05`로 중단한다.

### 7.7 Publication/recovery gate

```java
public interface PublicationGate {
    PublicationDecision evaluate(
            PublicationTermination termination,
            CandidateVerificationResult candidateVerification,
            ResultVerificationResult resultVerification,
            FinalResult finalResult
    );
}

public sealed interface PublicationDecision {
    record Normal(PublishableResult result) implements PublicationDecision {}
    record RecoveryOnly(RecoveryCandidate candidate) implements PublicationDecision {}
    record Rejected(PublicationRejection rejection) implements PublicationDecision {}
}
```

`PublicationTermination`은 AR-4 `TerminationRecord`의 exact reason, requested/completed boundary, rollback-integrity flag와 source-record fingerprint를 보존하는 **계획상 제안 solver-free projection**이다. Verification module은 AR-4 enum/class를 import하지 않는다. Application-side `CommittedCandidateVerificationIT`가 reason mapping의 totality, record fingerprint 보존과 unknown reason rejection을 검증한다.

Gate truth table:

| Termination | Candidate gate | Result gate | 판정 |
|---|---|---|---|
| 정상 품질 종료 | `PASS` | `PASS` | `Normal(PublishableResult)` |
| 정상 품질 종료 | `FAIL`/`INCOMPLETE` | 무엇이든 | `Rejected`; finalization 미실행 또는 artifact 폐기 |
| 정상 품질 종료 | `PASS` | `FAIL`/`INCOMPLETE` | `Rejected`; payload 정상 노출 금지 |
| Watchdog/cancel/resource/platform/failure | `PASS` | `PASS` | `RecoveryOnly`; 정상 publication/official benchmark 금지 |
| 예외 종료 | `FAIL`/`INCOMPLETE` | 무엇이든 | `Rejected`; recovery도 금지 |

Physical publish, CAS, storage와 retrieval은 AR-6 application port의 책임이다. AR-5 `PublishableResult`는 publication eligibility가 검증된 immutable internal artifact이며 실제 외부 상태 변경을 하지 않는다. Recovery 외부 노출 정책은 `ADR-ARCH-009` 또는 승인된 product contract 전까지 차단한다.

## 8. Error와 실패 처리 계약

| 상황 | 반환/상태 | 금지 shortcut |
|---|---|---|
| Candidate identity/structure/feasibility/metric corruption | Candidate `Failed` + typed finding | Exception을 `UNASSIGNED`로 변환 |
| Candidate verification interruption/internal exception | Candidate `Incomplete` | Last partial report를 `PASS`로 표시 |
| Candidate `FAIL`/`INCOMPLETE` | Finalization 미호출 | Bank를 final result로 직렬화 |
| Static proof mismatch | Result `Failed` | Proof를 `UNKNOWN`으로 조용히 낮춰 publication |
| Required audit missing/incomplete | Result `Failed`/`Incomplete` | 일부 option 결과로 exhaustive 주장 |
| Feasible insertion discovered | Internal audit record, outcome은 기존 `UNASSIGNED` 유지 가능 | Auto insert/re-solve/recursive finalization |
| Outcome/summary/payload mismatch | Result `Failed` | Candidate `PASS`로 덮기 |
| Both-pass exceptional candidate | `RecoveryOnly` | `COMPLETED`, official result나 benchmark vector |
| Canonical encoding/fingerprint failure | Result `Incomplete` 또는 `Failed` | 임의 field drop/alternate digest fallback |

Finding과 audit option의 order는 deterministic해야 한다. 같은 단일 corruption은 같은 failure code, scope와 report fingerprint를 만들어야 한다. 여러 corruption fixture는 하나씩 독립 주입해 어떤 authority가 gate를 막았는지 분리한다.

## 9. 세분화된 test case

### 9.1 Candidate verifier

| ID | Test class.method | 종류·권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green |
|---|---|---|---|---|---|---|
| `CV-01` | `CandidateSolutionVerifierContractTest.verifiesHandCalculatedCandidateCacheFree` | Unit; Master §14.1 | 1 vehicle, 1 real pair, literal directed travel와 hand-calculated load/time/metric/objective | `Passed`, oracle와 exact verified values/fingerprint | 최초 API 전 `cannot find symbol: class CandidateSolutionVerifier`; skeleton 뒤 `expected: PASS but was: INCOMPLETE` | 모든 hand value와 report binding 일치 |
| `CV-00` | `CandidateSolutionVerifierCorruptionTest.rejectsEachAuthorityInputCorruption` | Dynamic corruption inventory; 구현 계획 §9.6 | `CV-02`~`CV-06`의 single-axis case registry | 모든 registered case가 candidate gate에서 `Failed` | `corruption case <id> unexpectedly passed` | Required case ID 누락 0, 모두 typed fail |
| `CV-02` | `CandidateSolutionVerifierCorruptionTest.rejectsPartialDuplicateSplitAndReversePair` | Parameterized corruption; Master §6/§14.1 | 정상 fixture에서 pair authority 한 필드만 변형 | 각 case `Failed(REQUEST_PARTITION_INVALID/PAIR_*)` | `expected Failed but was Passed`와 case label | 4 corruption 모두 지정 code로 거부 |
| `CV-03` | `CandidateSolutionVerifierCorruptionTest.rejectsWrongTerminalAndServicePattern` | Corruption; Domain §10 | Oneway/roundtrip terminal, delivery-only/real pair 변형 | Terminal/service finding | `expected TERMINAL_POLICY_INVALID` 또는 `SERVICE_PATTERN_INVALID` 누락 | 두 축 독립 거부 |
| `CV-04` | `CandidateSolutionVerifierCorruptionTest.rejectsTravelArcAndFingerprintCorruption` | Corruption; Master §14.1 | 한 directed arc 값 또는 travel fingerprint만 변형 | Travel mismatch/arc failure | `expected TRAVEL_FINGERPRINT_MISMATCH but findings were []` | Reverse/lazy fallback 없이 fail |
| `CV-05` | `CandidateSolutionVerifierCorruptionTest.rejectsStaticCompatibilityAndHardFeasibilityCorruption` | Corruption; Domain §7/§11 | Size/capability/zone/load/window/resource 한 축 변형 | Typed static/hard finding | `expected Failed but was Passed` | 각 hard 축이 score 전에 fail |
| `CV-06` | `CandidateSolutionVerifierCorruptionTest.rejectsDeclaredMetricScoreAndObjectiveCorruption` | Corruption; Master §15.7 | Route는 같고 declared metric/score/objective 한 값만 변형 | 각 mismatch code | `expected DECLARED_METRIC_MISMATCH` 등 누락 | Recomputed exact value와 비교해 거부 |
| `CV-07` | `CandidateSolutionVerifierCorruptionTest.ignoresPoisonedSearchCache` | Architecture/corruption; 구현 계획 §9.6 | 같은 `CandidateAuthoritySnapshot`, 외부 test-only poisoned cache object | 결과/report/fingerprint 동일 | API가 cache argument를 요구하면 compile failure 또는 두 report 불일치 | Cache가 signature/bytecode에 없고 exact equality |
| `CV-08` | `CandidateSolutionVerifierContractTest.ordersFindingsDeterministically` | Reproducibility | 동일 multi-corruption을 다른 collection insertion order로 생성 | Ordered findings/report fingerprint 동일 | `expected reports equal` failure | Stable semantic order |
| `CV-09` | `CandidateSolutionVerifierContractTest.returnsIncompleteOnAbortWithoutVerifiedSolution` | Fault | Propagator cancellation/fault injection | `Incomplete`, `VerifiedSolution` 없음 | `expected Incomplete but was Failed/Passed` | Partial values 노출 0 |

### 9.2 Preliminary outcomes, audit와 diagnostics

| ID | Test class.method | 종류·권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green |
|---|---|---|---|---|---|---|
| `FN-01` | `PreliminaryOutcomeBuilderTest.derivesExactlyOneOutcomeFromVerifiedRoutes` | Unit; Master §10.2 | VerifiedSolution의 2 assigned + 2 absent requests; bank order 교란 | 2 preliminary assigned/2 unassigned, request ID stable order | `expected 4 outcomes but was 0` 또는 bank order 따라 차이 | Universe exactly once, route-derived |
| `FN-02` | `PreliminaryOutcomeBuilderTest.treatsDirectAndLeaseRoutesAsAssigned` | Unit; `Q-RES-01` | DIRECT route 1, LEASE route 1 | 둘 다 `ASSIGNED`; ownership summary만 분리 | `expected ASSIGNED but was OUTSOURCED/UNASSIGNED` | Two-state outcome만 존재 |
| `AU-01` | `FinalResultInsertionAuditTest.skipsOnlyFingerprintBoundStaticProvenRequest` | Unit; Domain §13.3 | No-compatible-vehicle static proof + 같은 request/fingerprint | `STATIC_PROVEN_AUDIT_NOT_REQUIRED`, `PROVEN` | `expected static skip but audit executed` 또는 proof mismatch 허용 | Exact proof만 skip |
| `AU-02` | `FinalResultInsertionAuditTest.rejectsStaleOrForeignStaticProof` | Corruption | 다른 problem/request/contract fingerprint proof | Static skip 금지, required audit 또는 result failure | `expected STATIC_PROOF_INVALID but was PROVEN` | Foreign proof는 publication 불가 |
| `AU-03` | `FinalResultInsertionAuditTest.auditsEveryNonStaticUnassignedRequest` | Integration; `Q-RES-02` | 2 non-static unassigned, literal eligible vehicle/position option set | 각 request에 exact option-key set과 complete disposition | `expected option <key> was not audited` | Missing/extra option 0 |
| `AU-04` | `FinalResultInsertionAuditTest.recordsNoFeasibleInsertionOnlyAfterAllOptionsFail` | Unit/integration | 모든 literal options가 서로 다른 hard reason으로 fail | `REQUIRED_COMPLETED_NO_FEASIBLE_INSERTION`, rejection counts, exhaustive diagnostic | `expected N attempts but was N-1` 또는 confidence가 `UNKNOWN` | Full set + exact counters |
| `AU-05` | `FinalResultInsertionAuditTest.doesNotAutoApplyFeasibleInsertion` | Fault/integration; Master §10.2 | 한 option feasible, 나머지 fail | `FEASIBLE_INSERTION_FOUND`; routes/bank/solution fingerprint unchanged; result still unassigned | `expected solution fingerprint unchanged` failure 또는 assigned count 증가 | Mutation/re-solve 호출 0 |
| `AU-06` | `FinalResultInsertionAuditTest.doesNotStopAtFirstFeasibleOption` | Integration | 첫 stable option feasible, 뒤 option 존재 | 모든 literal option 검사 | `expected attemptCount=<all> but was 1` | Full enumeration |
| `AU-07` | `FinalResultInsertionAuditTest.marksInterruptedAuditIncomplete` | Fault | Option k에서 exception/cancel | `REQUIRED_INCOMPLETE`, exhaustive diagnostic 없음 | `expected INCOMPLETE but was COMPLETED` | Publication gate 차단 |
| `DG-01` | `DiagnosticConfidenceTest.enforcesEvidenceSpecificCeiling` | Parameterized; Domain §13 | Static, no-fit audit, feasible-found audit, search telemetry, no source | 표의 상한만 허용 | `expected DIAGNOSTIC_CONFIDENCE_OVERSTATED` 누락 | 모든 invalid elevation 거부 |
| `DG-02` | `DiagnosticConfidenceTest.keepsFeasibleAuditFindingInternal` | Contract | Feasible insertion audit record + payload projection | Public outcome에 finding/proven reason 없음 | Payload에 internal option/feasible finding 노출 | Internal/public projection 분리 |

### 9.3 Result-integrity verifier와 canonical identity

| ID | Test class.method | 종류·권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green |
|---|---|---|---|---|---|---|
| `RV-01` | `ResultIntegrityVerifierContractTest.acceptsCompleteOutcomeAuditSummaryAndPayload` | Unit/integration; Master §14.1 | Candidate pass + valid final artifacts | `Passed`, result/payload fingerprints | Skeleton 뒤 `expected PASS but was INCOMPLETE` | Independent recomputation exact |
| `RV-00` | `ResultIntegrityVerifierCorruptionTest.rejectsOutcomeAuditSummaryAndPayloadMismatch` | Dynamic corruption inventory; 구현 계획 §9.6 | `RV-02`~`RV-08` single-axis case registry | 모든 registered case가 result gate에서 `Failed` | `corruption case <id> unexpectedly passed` | Outcome/audit/summary/payload category 누락 0 |
| `RV-02` | `ResultIntegrityVerifierCorruptionTest.rejectsMissingDuplicateAndContradictoryOutcome` | Parameterized corruption | Outcome remove/duplicate/assigned-as-unassigned | Exact outcome failure code | `expected OUTCOME_MISSING_OR_DUPLICATE` 누락 | 각 single corruption 거부 |
| `RV-03` | `ResultIntegrityVerifierCorruptionTest.rejectsRouteVehiclePairAndOwnershipMismatch` | Corruption | Assigned reference 한 필드씩 변경 | Assigned/ownership mismatch | `expected ASSIGNED_REFERENCE_MISMATCH` 누락 | Verified solution과 exact bind |
| `RV-04` | `ResultIntegrityVerifierCorruptionTest.rejectsMissingIncompleteAndExtraAuditOption` | Corruption | Audit 제거, incomplete, option remove/add | Audit failure code | `expected AUDIT_OPTION_SET_INCOMPLETE` 누락 | Expected set 독립 재열거 |
| `RV-05` | `ResultIntegrityVerifierCorruptionTest.rejectsOverstatedDiagnosticConfidence` | Corruption | UNKNOWN source를 PROVEN/EXHAUSTIVE로 변경 | Confidence failure | `expected DIAGNOSTIC_CONFIDENCE_OVERSTATED` 누락 | Source ceiling 재검증 |
| `RV-06` | `ResultIntegrityVerifierCorruptionTest.rejectsSummaryMismatch` | Corruption | Assigned/unassigned/ownership count 한 값 변경 | `SUMMARY_MISMATCH` | `expected Failed but was Passed` | Outcome-derived exact recomputation |
| `RV-07` | `ResultIntegrityVerifierCorruptionTest.rejectsPayloadAndFingerprintMismatch` | Corruption | Payload field/order/value 또는 digest 한 축 변경 | Projection/fingerprint failure | `expected PAYLOAD_PROJECTION_MISMATCH` 또는 digest code 누락 | Canonical regeneration으로 거부 |
| `RV-08` | `ResultIntegrityVerifierCorruptionTest.rejectsCandidatePassBoundToDifferentVerifiedSolution` | Corruption | Pass report와 verified solution fingerprint 교차 | `CANDIDATE_PASS_BINDING_MISMATCH` | `expected Failed but was Passed` | Gate chaining exact |
| `CR-01` | `CanonicalResultEncoderTest.isStableAcrossCollectionInsertionOrders` | Property/reproducibility | 같은 semantic result의 여러 map/list construction order | Canonical bytes/fingerprint 동일 | Byte arrays differ | Stable ID/order 사용 |
| `CR-02` | `CanonicalResultEncoderTest.excludesTimestampElapsedAndProviderLocator` | Unit/architecture | Metadata만 다른 두 result envelope | Semantic fingerprints 동일 | Fingerprint changes with metadata | Non-semantic field 제외 |
| `CR-03` | `CanonicalResultEncoderTest.rejectsUnknownContractVersion` | Negative | `latest`, unknown version | Typed failure, fallback 없음 | Unknown version encoded | Exact version만 |

### 9.4 Publication/recovery와 architecture

| ID | Test class.method | 종류·권위 | Fixture | Expected | 정상적인 첫 실패 관찰 | Green |
|---|---|---|---|---|---|---|
| `PG-01` | `PublicationGateTest.requiresBothIndependentPassReports` | Parameterized integration | Candidate/result `PASS/FAIL/INCOMPLETE` Cartesian cases | 오직 PASS/PASS만 eligible | `expected Rejected but was Normal` | Single-pass 노출 0 |
| `PG-02` | `PublicationGateTest.issuesNormalPublishableResultForNormalBothPass` | Unit | Normal termination + both-pass exact bindings | `Normal(PublishableResult)` | `expected Normal but was Rejected` | Reports/fingerprints 포함 |
| `PG-03` | `PublicationGateTest.issuesRecoveryOnlyForExceptionalBothPass` | Unit; Master §13.1 | Watchdog/cancel/resource/platform/failure 각각 + both-pass | `RecoveryOnly`, normal/official flag 없음 | `expected RecoveryOnly but was Normal` | 모든 exceptional 분리 |
| `PG-04` | `PublicationGateFaultTest.neverRecoversFailedOrIncompleteVerification` | Fault | Exceptional + candidate/result fail/incomplete | `Rejected` | Recovery artifact 생성 | 두 gate 없이는 recovery 0 |
| `PG-05` | `PublicationGateFaultTest.rejectsForgedOrMismatchedPassBindings` | Corruption | Report/result/payload fingerprints 교차 | `Rejected` | `expected Rejected but was Normal` | Binding 재확인 |
| `AR-01` | `VerifierDependencyArchitectureTest.forbidsSolverSearchCacheAndProviderDependencies` | Architecture; Architecture §7 | Verification POM/source/bytecode | 위반 0 | 삽입한 test-only forbidden reference의 package/artifact를 정확히 보고 | Solver/search/cache/SDK 0 |
| `AR-02` | `VerifierDependencyArchitectureTest.forbidsResultVerifierDependingOnFinalizerImplementation` | Architecture | `verification.result` import graph | `result.api`/core만 허용 | `verification.result -> result.finalization` edge 보고 | Independent package path |
| `CS-01` | `VerificationCorruptionSuiteTest.coversEveryRequiredAuthorityCorruptionCategory` | Governance | Required case ID set와 registered dynamic tests | 누락 0 | `missing corruption cases: [...]` | Candidate/result matrix 완전 |
| `IT-01` | `CommittedCandidateVerificationIT.consumesActualAr4AuthorityWithoutSolverDependencyInVerification` | Cross-module integration | AR-4 actual committed candidate | both-pass publishable/rejection cases | Actual type bridge 또는 fingerprint mismatch | Verification POM solver dependency 없이 end-to-end |

## 10. Corruption suite 최소 matrix

각 row는 정상 fixture에서 한 축만 바꾼다. 여러 corruption을 한 번에 넣어 첫 failure 하나로 나머지를 가리지 않는다.

| Authority/artifact | 최소 corruption | 막아야 할 gate |
|---|---|---|
| Problem identity | 다른 problem fingerprint, request universe 변경 | Candidate |
| Profile declaration | 다른 profile/preset/dependency fingerprint | Candidate |
| Prepared travel | Fingerprint, directed arc distance/time, missing arc | Candidate |
| Route structure | Partial, duplicate, split, reverse precedence | Candidate |
| Vehicle/terminal | Wrong vehicle, wrong start/end, internal depot | Candidate |
| Static compatibility | Size/capability/zone/servable vehicle mismatch | Candidate |
| Physical propagation | Load, window, travel/resource violation | Candidate |
| Declared evaluation | Metric, score component, objective vector | Candidate |
| Search cache | Stale arrival/load/metric/score/insertion table | 어떤 report에도 영향 없음; architecture input에서 배제 |
| Candidate pass binding | Report와 verified solution 교차 | Result/publication |
| Outcome | Missing, duplicate, wrong status, route ref on unassigned | Result |
| Ownership | DIRECT/LEASE를 outcome status로 변경, wrong vehicle ownership ref | Result |
| Static proof | Foreign request/problem/version, evidence 없음 | Result |
| Audit | Missing request, incomplete, option remove/add, counter mismatch | Result |
| Diagnostic | Confidence elevation, source/evidence mismatch | Result |
| Summary | Assigned/unassigned/used DIRECT/LEASE count | Result |
| Payload | Field/value/order/version, result identity | Result |
| Fingerprint | Solution/result/payload digest 한 축 | Result/publication |
| Termination | Exceptional을 normal로 변경 | Publication/recovery |

Oracle 우선순위는 hand calculation → literal small exhaustive option set → 별도 reference implementation이다. Search가 만든 “정상” candidate의 self-consistency만 expected value로 사용하지 않는다.

## 11. 강제 red → green 구현 순서

### 11.1 Slice 0 — Baseline과 entry gate

- [ ] `git status --short`와 사용자/다른 세션 소유 변경을 evidence에 저장한다.
- [ ] source design hash와 선행 phase/evidence digest를 검사한다.
- [ ] AR-0~AR-4 `DONE`, lossless `CandidateAuthoritySnapshot` projection과 stateless evaluator를 확인한다.
- [ ] `mvn -pl rpdptw/verification -am test` baseline을 기록한다.
- [ ] Blocker가 있으면 production/test를 만들지 않고 `BLOCKED` handoff를 기록한다.

### 11.2 Slice 1 — Candidate verifier API와 hand oracle

- [ ] `CandidateVerificationFixtures`, `CandidateSolutionVerifierContractTest`를 먼저 만든다.
- [ ] production type이 없는 상태에서 `cannot find symbol: CandidateSolutionVerifier`가 발생하는지 확인한다.
- [ ] 환경/dependency/unrelated failure이면 red로 인정하지 않는다.
- [ ] 최소 API/result skeleton을 추가한다.
- [ ] 같은 test가 이제 compile되고 `expected PASS but was INCOMPLETE`로 실패하는지 확인한다.
- [ ] Identity binding과 hand-calculated 1-route full traversal만 최소 구현한다.
- [ ] Targeted green 뒤 corruption test를 하나씩 먼저 추가하고 각 의도한 failure를 관찰한다.
- [ ] Pair→terminal/service→travel→hard feasibility→metric/score/objective 순서로 최소 구현한다.
- [ ] Cache를 API에 추가하지 않고 `CV-07`, deterministic report와 abort cases를 green으로 만든다.

### 11.3 Slice 2 — Preliminary outcomes와 required audit

- [ ] `PreliminaryOutcomeBuilderTest`, `FinalResultInsertionAuditTest`, literal option oracle를 먼저 만든다.
- [ ] `cannot find symbol: PreliminaryOutcomeBuilder` compile failure를 보존한다.
- [ ] 최소 outcome/audit API skeleton 뒤 `expected 4 outcomes but was 0` 또는 `expected option <key> was not audited` assertion red를 확인한다.
- [ ] Verified route-derived preliminary partition만 구현한다.
- [ ] Exact static proof binding과 audit-not-required disposition을 구현한다.
- [ ] 모든 eligible vehicle/legal position pair enumeration을 구현한다.
- [ ] Option evaluator를 side-effect-free로 호출하고 complete/no-fit evidence를 만든다.
- [ ] Feasible 발견 시 전체 enumeration 지속, no-auto-fix와 fingerprint unchanged를 green으로 만든다.
- [ ] Interruption을 incomplete로 만들고 diagnostic confidence test를 green으로 만든다.

### 11.4 Slice 3 — Result integrity와 canonical identity

- [ ] `ResultIntegrityVerifierContractTest`와 모든 단일-field corruption test를 먼저 만든다.
- [ ] `cannot find symbol: ResultIntegrityVerifier`를 첫 red로 보존한다.
- [ ] API skeleton 뒤 정상 fixture가 `expected PASS but was INCOMPLETE`로 실패하는지 확인한다.
- [ ] Candidate pass binding과 exactly-one outcome 검증부터 최소 구현한다.
- [ ] Assigned/ownership reference, static proof와 independent audit option-set 재열거를 구현한다.
- [ ] Confidence, summary recomputation을 구현한다.
- [ ] `CanonicalResultEncoderTest`를 먼저 red로 만든 뒤 `RPDPTW_RESULT_CANON_V1` internal encoder를 구현한다.
- [ ] Payload projection/result fingerprint corruption까지 green으로 만든다.
- [ ] Result verifier가 finalizer implementation을 import하지 않는 architecture test를 green으로 만든다.

### 11.5 Slice 4 — Publication/recovery gate

- [ ] `PublicationGateTest`와 Cartesian fixture를 먼저 만든다.
- [ ] `cannot find symbol: PublicationGate` 또는 skeleton의 `expected Normal but was Rejected` red를 보존한다.
- [ ] Both-pass normal case만 `PublishableResult`를 만드는 최소 gate를 구현한다.
- [ ] Candidate/result fail/incomplete가 모두 rejection인지 green으로 만든다.
- [ ] Exceptional both-pass는 `RecoveryOnly`, exceptional single-pass는 rejection으로 만든다.
- [ ] Forged/mismatched binding과 termination corruption을 green으로 만든다.
- [ ] Physical publisher, GCS/API/retrieval 코드는 추가하지 않는다.

### 11.6 Slice 5 — Target green → module verify → reactor regression

- [ ] Candidate/finalization/result/publication targeted test를 각각 실행한다.
- [ ] 전체 `rpdptw-verification` unit/property/corruption suite를 실행한다.
- [ ] `*IT`는 Failsafe `verify`에서 실제 실행됐는지 report를 확인한다.
- [ ] Architecture rule, dependency tree와 `jdeps` evidence를 확인한다.
- [ ] `mvn -pl rpdptw/verification -am verify`를 통과한다.
- [ ] `mvn verify` reactor regression을 통과한다.
- [ ] 금지 dependency/value 검색과 diff scope를 확인한다.
- [ ] Evidence bundle과 handoff를 완성한 뒤에만 `DONE`을 판정한다.

어느 checklist에서도 “test를 작성했으나 red를 실행하지 않음”은 완료가 아니다. Compile red는 최초 API skeleton에서만 허용하고, skeleton 뒤의 의미 test는 반드시 구체 assertion failure로 red를 보여야 한다.

## 12. Exact 실행 명령

모든 명령의 working directory:

```bash
cd /Users/brown/workspace/ro-next
```

### 12.1 착수 전 baseline/hash

```bash
git status --short
git rev-parse HEAD
mvn -version
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md
find docs/codex/phases -maxdepth 1 -type f -print | sort
find target/codex-evidence -maxdepth 4 -type f -print 2>/dev/null | sort
mvn -pl rpdptw/verification -am test
```

Expected source hashes는 YAML metadata와 같아야 한다. Module/path가 없거나 선행 phase가 `DONE`이 아니면 마지막 Maven 명령으로 억지 scaffold를 만들지 말고 `BLOCKED`다.

### 12.2 Candidate red/green

최초 test-only red:

```bash
mvn -pl rpdptw/verification -am \
  -Dtest=CandidateSolutionVerifierContractTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected red 1:

```text
cannot find symbol
  symbol: class CandidateSolutionVerifier
```

API skeleton 뒤 expected semantic red:

```text
expected: <PASS> but was: <INCOMPLETE>
```

Targeted candidate suite:

```bash
mvn -pl rpdptw/verification -am \
  -Dtest=CandidateSolutionVerifierContractTest,CandidateSolutionVerifierCorruptionTest,CandidateSolutionVerifierCacheIndependenceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

### 12.3 Finalization/audit red/green

```bash
mvn -pl rpdptw/verification -am \
  -Dtest=PreliminaryOutcomeBuilderTest,FinalResultInsertionAuditTest,DiagnosticConfidenceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

첫 compile red는 `cannot find symbol: PreliminaryOutcomeBuilder` 또는 `FinalSolutionInsertionAuditor`여야 한다. Skeleton 뒤에는 `expected option <AuditOptionKey> was not audited`, `expected solution fingerprint unchanged` 같은 table의 assertion으로 실패해야 한다.

### 12.4 Result/publication red/green

```bash
mvn -pl rpdptw/verification -am \
  -Dtest=ResultIntegrityVerifierContractTest,ResultIntegrityVerifierCorruptionTest,CanonicalResultEncoderTest,PublicationGateTest,PublicationGateFaultTest,VerificationCorruptionSuiteTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected semantic red는 최소 `expected: <PASS> but was: <INCOMPLETE>`, `expected AUDIT_OPTION_SET_INCOMPLETE`, `expected Rejected but was Normal` 중 현재 slice에 지정된 하나여야 한다.

### 12.5 Integration/module/reactor

```bash
mvn -pl rpdptw/application -am \
  -Dit.test=CommittedCandidateVerificationIT \
  -Dfailsafe.failIfNoSpecifiedTests=false verify

mvn -pl rpdptw/verification -am test
mvn -pl rpdptw/verification -am verify
mvn -pl build/architecture-rules -am \
  -Dtest=VerifierDependencyArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
mvn verify
```

Surefire/Failsafe report 존재와 실제 test count:

```bash
find rpdptw/verification/target/surefire-reports -type f -maxdepth 1 -print | sort
find rpdptw/application/target/failsafe-reports -type f -maxdepth 1 -print | sort
rg -n 'tests="[1-9][0-9]*".*failures="0".*errors="0"' \
  rpdptw/verification/target/surefire-reports \
  rpdptw/application/target/failsafe-reports
```

### 12.6 Dependency/package/금지 shortcut

```bash
mvn -pl rpdptw/verification dependency:tree -Dscope=compile

rg -n --glob 'pom.xml' \
  '<artifactId>rpdptw-solver</artifactId>|google-cloud|aws-sdk|azure-|spring-web|jakarta\.ws\.rs' \
  rpdptw/verification

rg -n --glob '*.java' \
  'com\.ronext\.rpdptw\.solver|solver\.(search|state|portfolio)|SearchCache|RouteCache|InsertionCache|SolverSummary' \
  rpdptw/verification/src/main/java

rg -n --glob '*.java' \
  'com\.ronext\.rpdptw\.result\.finalization' \
  rpdptw/verification/src/main/java/com/ronext/rpdptw/verification/result

rg -n --glob '*.java' \
  'OUTSOURCED|DEFERRED|auto.?insert|re.?solve|latest|System\.nanoTime|currentTimeMillis|HashMap' \
  rpdptw/verification/src/main/java
```

위 네 `rg`는 의도된 allowlist 주석/enum 선언을 제외하고 match 0이 기대다. `DiagnosticConfidence`의 `SEARCH_OBSERVED` 등 합법 문자열이 검색에 걸리면 금지 검색을 더 좁히고 evidence에 이유를 기록한다. Match를 무시하거나 source를 rename해 숨기지 않는다.

Jar가 생성된 뒤 bytecode 확인:

```bash
jdeps --recursive --verbose:class \
  rpdptw/verification/target/rpdptw-verification-*.jar \
  | rg 'com\.ronext\.rpdptw\.solver|com\.google\.cloud|software\.amazon\.awssdk|com\.azure'
```

Expected는 match 0이다. `jdeps` classpath 누락으로 분석이 실패하면 architecture test 결과로 대체했다고 주장하지 말고 classpath를 바로잡아 다시 실행한다.

### 12.7 Diff/link/document scope

```bash
git diff --check
git status --short
git diff --name-status
git diff -- rpdptw/verification build/architecture-rules rpdptw/application/src/test
rg -n '^#{1,6} ' docs/codex/phases/phase-05-verification-finalization-and-publication.md
rg -n '\]\(([^)]+)\)' docs/codex/phases/phase-05-verification-finalization-and-publication.md
```

Implementation 변경 허용 범위는 §6 표의 AR-5 file, 필요한 `rpdptw/verification/pom.xml`, architecture rule, application integration test와 evidence/progress record뿐이다. 선행 core/solver, legacy, README/GCP/data 또는 다른 phase 문서가 diff에 나타나면 사용자/다른 session 소유인지 먼저 판정하고 AR-5가 수정하지 않는다.

## 13. Step-by-step implementation checklist

### 13.1 준비

- [ ] Source hash drift 0.
- [ ] AR-0~AR-4 `DONE`과 evidence digest 확인.
- [ ] Neutral candidate authority가 cache/search type을 노출하지 않음.
- [ ] Static proof와 insertion evaluator contract/version 확인.
- [ ] Baseline test/verify 결과와 dirty tree를 evidence에 보존.

### 13.2 Candidate verifier

- [ ] Test/fixture files를 production보다 먼저 생성.
- [ ] Compile red와 skeleton 뒤 semantic red를 각각 보존.
- [ ] Identity/fingerprint binding.
- [ ] Route/bank partition, pair/terminal/service pattern.
- [ ] Static compatibility와 complete directed travel.
- [ ] Full load/time/window/resource propagation.
- [ ] Registered hard constraints.
- [ ] Neutral metric, score, objective exact recomputation.
- [ ] Declared-vs-recomputed comparison.
- [ ] Deterministic findings/report fingerprint.
- [ ] Abort는 incomplete, verified solution 없음.
- [ ] Poisoned cache 무관성과 solver dependency 0.

### 13.3 Finalization/audit

- [ ] Candidate `Passed`만 finalizer input.
- [ ] Verified route-derived preliminary outcomes.
- [ ] DIRECT/LEASE both assigned.
- [ ] Static proof exact binding과 skip.
- [ ] Non-static unassigned 전체 audit.
- [ ] Eligible vehicle/legal position literal oracle equality.
- [ ] No-fit complete audit와 rejection counters.
- [ ] Feasible found 뒤 full enumeration 지속.
- [ ] No route/bank/outcome mutation, no re-solve.
- [ ] Incomplete audit가 exhaustive diagnostic을 만들지 않음.
- [ ] Diagnostic source/confidence ceiling.
- [ ] Summary를 outcomes에서만 계산.

### 13.4 Result verifier/canonical artifact

- [ ] Test/corruption fixture를 먼저 생성하고 red 관찰.
- [ ] Candidate pass/verified solution binding.
- [ ] Exactly-one outcome와 assigned/ownership references.
- [ ] Static proof/audit-required set 독립 검증.
- [ ] Expected audit option-key set 독립 재열거.
- [ ] Diagnostic confidence ceiling 재검증.
- [ ] Summary 재계산.
- [ ] Canonical payload projection 재생성.
- [ ] Solution/result/payload fingerprint binding.
- [ ] Finalizer implementation dependency 0.
- [ ] Deterministic report/fingerprint.

### 13.5 Publication/recovery

- [ ] PASS/PASS normal만 `PublishableResult`.
- [ ] Single fail/incomplete의 모든 조합 rejection.
- [ ] Exceptional PASS/PASS는 `RecoveryOnly`.
- [ ] Exceptional single-pass는 recovery도 금지.
- [ ] Forged/mismatched report binding rejection.
- [ ] Physical publisher/SDK/state write 0.

### 13.6 Verification/evidence

- [ ] 각 case의 red와 green report 보존.
- [ ] Candidate/result corruption suite case inventory 누락 0.
- [ ] Targeted unit/property/corruption suite green.
- [ ] Cross-module `CommittedCandidateVerificationIT` green.
- [ ] Verification module `verify` green.
- [ ] Architecture test와 `jdeps` green.
- [ ] Full reactor `mvn verify` green.
- [ ] Forbidden dependency/value match 0.
- [ ] `git diff --check` green과 diff scope 승인 범위 내.
- [ ] Evidence bundle digest와 다음 phase handoff 작성.

## 14. Deliverables와 evidence bundle

### 14.1 Deliverable

| Deliverable | 최소 내용 | Downstream 소비 |
|---|---|---|
| Candidate verification API/report | Contract/version, authority bindings, decision/findings | AR-6/7/8/9 worker/application gate |
| `VerifiedSolution` | Recomputed routes/facts/metrics/score/objective와 canonical identity | Finalizer, benchmark metric consumer |
| Exactly-one outcomes | Assigned route/vehicle/pair 또는 unassigned diagnostic | Result retrieval/payload adapter |
| Audit bundle | Static skip 또는 complete option evidence, internal feasible finding | Result verifier, restricted audit artifact |
| Diagnostics | Bounded code/scope/confidence/source/evidence | Result payload projection |
| Outcome-derived summary | Assigned/unassigned와 ownership/route counts | Result verifier/retrieval |
| Result-integrity report | Outcome/audit/summary/payload bindings와 decision | Publication gate |
| `PublishableResult` | Both-pass reports, result/payload fingerprint, lineage | AR-6 logical publisher |
| `RecoveryCandidate` | Exceptional termination + both-pass, non-normal marker | AR-6 recovery policy gate |
| Corruption reports | Candidate/result authority별 single corruption outcomes | Regression/AR-8 cutover/AR-9 workflow |

### 14.2 Evidence bundle 위치와 내용

```text
target/codex-evidence/AR-5/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
│   ├── candidate-api-compile-red.log
│   ├── candidate-semantic-red.log
│   ├── finalization-api-compile-red.log
│   ├── audit-semantic-red.log
│   ├── result-api-compile-red.log
│   ├── result-semantic-red.log
│   └── publication-semantic-red.log
├── green/
│   ├── candidate-targeted.log
│   ├── finalization-targeted.log
│   ├── result-targeted.log
│   └── publication-targeted.log
├── regression/
│   ├── verification-test.log
│   ├── verification-verify.log
│   ├── application-it.log
│   ├── architecture.log
│   └── reactor-verify.log
├── fingerprints/
│   ├── authority-bindings.json
│   ├── verified-solution.json
│   ├── final-result.json
│   └── publishable-payload.json
├── faults/
│   ├── candidate-corruption-matrix.json
│   ├── audit-fault-matrix.json
│   ├── result-corruption-matrix.json
│   └── publication-gate-matrix.json
├── reproducibility/
│   └── report-and-fingerprint-repeat.json
├── diff/
│   ├── git-status.txt
│   ├── git-diff-check.txt
│   ├── dependency-tree.txt
│   ├── forbidden-rg.txt
│   └── jdeps.txt
└── handoff.md
```

`evidence.json`은 전체 계획 §11.1 공통 field에 더해 다음을 가져야 한다.

```text
candidateVerifierContractVersion
resultVerifierContractVersion
canonicalResultContractVersion
candidateCorruptionCaseIds
resultCorruptionCaseIds
staticProofContractVersion
auditContractVersion
candidateReportDigest
verifiedSolutionDigest
auditBundleDigest
resultIntegrityReportDigest
resultDigest
payloadDigest
publicationDecision
```

Timestamp/elapsed는 metadata이며 solution/result/quality fingerprint에 넣지 않는다. Audit elapsed도 quality vector나 ALNS step으로 합치지 않는다.

## 15. Rollback

### 15.1 Source/build rollback 단위

- AR-5 source/test/POM/architecture-rule 변경을 한 독립 commit 또는 명시적 patch 단위로 유지한다.
- 이미 commit했다면 broad reset 대신 해당 AR-5 commit의 `git revert`를 사용한다.
- 미커밋 rollback은 §6의 정확한 경로만 대상으로 하고, 실행 전 `git status --short`와 patch를 보존한다.
- Root POM, 선행 core/solver, legacy source, 다른 phase 문서와 사용자 변경에는 `git restore`, checkout, reset을 실행하지 않는다.
- `rpdptw/verification/pom.xml` rollback 시 AR-5가 추가한 dependency/plugin row만 되돌리고 AR-0 parent convention은 보존한다.

### 15.2 Artifact/schema/state rollback

- `RPDPTW_RESULT_CANON_V1` artifact는 immutable create-once다. 잘못된 version artifact를 overwrite하지 않고 rejected/quarantined evidence로 남긴다.
- AR-5는 physical publication pointer, database, bucket이나 endpoint를 변경하지 않으므로 외부 state rollback을 수행하지 않는다.
- AR-6 이후 logical publication이 연결된 상태에서 AR-5 rollback이 필요하면 새 result 생성/normal publication을 차단하고 이전 verified result pointer를 CAS로 유지한다. 기존 immutable artifact를 삭제하지 않는다.
- Recovery artifact는 normal result pointer가 아니므로 rollback 중 정상 status로 승격하지 않는다.
- Public schema/cutover rollback은 AR-8의 versioned compatibility 계획 소유다.

### 15.3 Rollback 검증

- Legacy characterization test가 rollback 전후 동일하다.
- 선행 AR-0~AR-4 reactor test와 artifact digest가 동일하다.
- AR-5 package/dependency가 제거되어도 core/solver가 verification을 역참조하지 않는다.
- 다른 session/user file diff가 rollback patch에 포함되지 않는다.

## 16. DONE / BLOCKED 판정

### 16.1 `DONE` AND gate

다음을 모두 만족해야 `DONE`이다.

1. AR-0~AR-4가 실제 `DONE`이고 source/evidence digest가 일치한다.
2. §6 production/test deliverable이 repository에 존재한다.
3. 모든 `CV/FN/AU/DG/RV/CR/PG/AR/CS/IT` 요구에 의도한 red와 green evidence가 있다.
4. Candidate verifier가 solver/search/cache/summary 없이 actual AR-4 authority를 full recompute한다.
5. Static-proven과 required-audit 분기가 모든 unassigned request를 완전하게 덮는다.
6. Feasible insertion 발견이 route/outcome을 자동 수정하지 않는다.
7. Result verifier가 outcome, ownership, audit, confidence, summary와 payload corruption을 독립 거부한다.
8. Normal `PublishableResult`가 오직 normal termination + candidate `PASS` + result `PASS`에서만 나온다.
9. Exceptional both-pass는 `RecoveryOnly`, single-pass는 recovery도 거부된다.
10. Verification compile/runtime dependency가 core only이고 solver/search/cache/provider SDK 참조가 0이다.
11. Targeted test, module verify, architecture/`jdeps`, application IT와 full reactor verify가 통과한다.
12. Evidence bundle digest, rollback과 AR-6 handoff가 재현 가능하다.
13. Downstream-style integration test가 actual `CommittedCandidate`를 소비한다. Mock/demo/type 존재만으로 끝내지 않는다.

### 16.2 `BLOCKED` 판정

다음 중 하나면 `BLOCKED`다.

- AR-1/2/4 authority artifact 또는 evidence가 없거나 fingerprint가 불일치한다.
- Verification이 solver/search/cache dependency 없이는 구현될 수 없다고 판단된다.
- Static proof 또는 insertion evaluator 의미가 설계와 충돌한다.
- Candidate/result canonical identity가 승인된 internal contract 없이 floating/default serialization에 의존해야 한다.
- Candidate `FAIL`을 unassignment로 바꾸거나 result `FAIL`을 candidate `PASS`로 덮어야만 기존 flow와 연결된다.
- Audit feasible insertion을 자동 적용/재탐색해야 한다는 요구가 생긴다.
- External public API/storage/provider 선택 없이는 generic internal gate를 구현할 수 없다는 전제가 생긴다.
- User/다른 session 소유 변경과 AR-5 target path가 겹쳐 보존할 수 없다.

`Q-BENCH-02` 수치, compliant Win fixture, physical provider와 optional variant 부재만으로 AR-5를 `BLOCKED` 처리하지 않는다. 반대로 README/GCP의 임시값이나 current provider를 사용해 그 결정을 채우지 않는다.

## 17. 다음 phase handoff

### 17.1 AR-6 / RM-8-local에 전달

- `PublishableResult`와 `RecoveryCandidate`의 internal typed contract/version
- Candidate/result verifier report와 exact binding digest
- Result/payload fingerprint와 canonicalization contract ID
- Publication truth table과 rejection reason
- Local `ResultPublisher`가 normal both-pass만 CAS하도록 하는 contract fixture
- Retrieval이 `RUNNING`/`REJECTED`/`RECOVERY_ONLY`/`PUBLISHED`를 미검증 payload와 구분할 evidence
- Restricted internal audit artifact와 public payload projection의 분리

AR-6는 physical/local artifact와 state를 구현하되 verifier 판정을 재계산하거나 single-pass payload를 노출하면 안 된다.

### 17.2 AR-7 / AR-8 / AR-9에 전달

- AR-7: Worker candidate gate, same logical worker report/digest identity, incomplete worker rejection
- AR-8: Legacy raw candidate와 target both-pass result 차이를 분류할 compatibility fixture; legacy finalize를 목표 evidence로 승격 금지
- AR-9: Publishable verified result와 exact candidate/result verifier version/fingerprint. Official 수치와 compliant fixture는 별도 entry gate

### 17.3 Known risks

- Solver와 verifier가 core full evaluator를 공유하므로 같은 bug 가능성이 남는다. Hand oracle과 literal small exhaustive oracle로 완화한다.
- Audit option 수가 클 수 있으나 이 phase에서 early stop이나 incomplete audit로 의미를 바꾸지 않는다. Performance는 관측하고 correctness를 유지한다.
- Internal canonical projection이 향후 public schema와 다를 수 있다. Versioned adapter로 변환하며 내부 fingerprint를 public JSON serialization에 종속시키지 않는다.
- Static proof vocabulary가 너무 넓으면 confidence overclaim 위험이 있다. Exact code/version/request/problem binding과 corruption suite가 gate다.

## 18. Scope exclusions와 금지 shortcut

### 18.1 이 phase가 구현하지 않는 것

- Solver, ALNS, COW, search cache와 operator
- Input adapter, normalization, travel preparation과 profile binding
- Physical `ResultPublisher`, storage/database, CAS state repository와 retrieval transport
- API/worker endpoint 변경, GCP workflow/cloudbuild/deployment 수정
- Legacy `/internal/finalize` 또는 `results/<requestId>.json` cutover
- Public JSON/wire schema와 database schema 승인
- `Q-BENCH-02` 공식 step/worker/round/watchdog 값
- Win fixture 변환, 반올림/절삭, official baseline/challenger
- Provider/product/topology 선택 또는 provider adapter
- Optional variant, multi-trip/rotation, route pool/MIP
- Apply/undo 또는 COW profiling
- Automatic insertion, repair, re-solve, recursive finalization

### 18.2 명시적 금지 shortcut

- Solver `feasible=true`, summary, cached arrival/load/metric/objective를 verifier authority로 사용
- Verification POM에 `rpdptw-solver` test/compile/runtime dependency 추가
- Search bank를 final outcome으로 그대로 직렬화
- Partial/duplicate/split pair를 ordinary `UNASSIGNED` reason으로 변환
- Static proof가 없는 request의 audit 생략
- 일부 eligible vehicle/position만 검사하고 exhaustive confidence 부여
- Feasible insertion을 찾은 뒤 result를 자동 수정하거나 solver 재호출
- Audit finding을 전역 불가능성 또는 public `PROVEN`으로 과장
- `OUTSOURCED`/`DEFERRED` request outcome 추가
- Candidate `PASS` 하나로 normal publication
- Result `PASS`가 candidate failure를 덮도록 구성
- Exceptional termination을 `COMPLETED`/official로 rename
- Timestamp, elapsed, provider locator, collection iteration order를 semantic fingerprint에 포함
- `latest`, floating tolerance, numeric sentinel, hidden fallback/version
- Q-BENCH/README/GCP 값을 test 밖의 default로 사용
- Physical infrastructure를 logical publication/recovery gate 안에 구현

## 19. 문서 작성 세션 검증 기록

이 절은 이 Markdown 작성 자체의 검증 기록이다. AR-5 구현 evidence가 아니다.

### 19.1 확인 대상

- Target path: `docs/codex/phases/phase-05-verification-finalization-and-publication.md`
- Required top-level heading: `# AR-5 / RM-5 — Verification, finalization과 publication`
- Source baseline: YAML metadata의 네 SHA-256
- 허용 작성 범위: 위 target Markdown 한 파일

### 19.2 작성 후 실행할 검증

```bash
test -f docs/codex/phases/phase-05-verification-finalization-and-publication.md
rg -n '^# AR-5 / RM-5 — Verification, finalization과 publication$' \
  docs/codex/phases/phase-05-verification-finalization-and-publication.md
rg -n '^## (2\. Authority|4\. 현 상태|6\. 정확한 예상 경로|7\. 계획상 제안 type/API|9\. 세분화된 test case|11\. 강제 red|12\. Exact 실행 명령|14\. Deliverables|15\. Rollback|16\. DONE / BLOCKED|17\. 다음 phase handoff|18\. Scope exclusions)' \
  docs/codex/phases/phase-05-verification-finalization-and-publication.md
shasum -a 256 docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md
git diff --check -- docs/codex/phases/phase-05-verification-finalization-and-publication.md
git diff --no-index --check /dev/null \
  docs/codex/phases/phase-05-verification-finalization-and-publication.md
git status --short -- docs/codex/phases/phase-05-verification-finalization-and-publication.md
```

Untracked 새 파일의 `git diff --no-index`는 whitespace 오류가 없어도 내용 차이 때문에 exit 1이 정상이다. Output에 whitespace error가 있으면 수정한다. Repository 전체 dirty tree는 사용자/다른 session 소유 변경을 포함하므로, 이 세션은 target file의 scoped status와 실제 edit operation을 기준으로 변경 범위를 판정한다.

### 19.3 작성 시점 판정

- 상위 구현 계획 §10의 13개 항목은 이 문서 §3, §2, §4, §6, §7~§8, §9~§10, §11, §12, §14, §15, §16, §17~§18에 각각 대응한다.
- YAML front matter parse: `PASS` (`phase=AR-5`, `status=BLOCKED`, `document_status=REVIEW_DRAFT`).
- Target file/path와 필수 H1 확인: `PASS`.
- Required heading 검색: `PASS`.
- 상대 Markdown link target 검사: broken link 0건.
- Source design hash drift: 0건. YAML의 implementation plan/Master/Architecture/Domain SHA-256과 작성 후 재계산값이 모두 일치했다.
- Whitespace 검사: scoped `git diff --check`와 untracked-file용 `git diff --no-index --check /dev/null <target>` output 0건. 후자는 내용 차이 때문에 exit 1이 정상임을 확인했다.
- 변경 범위: 작성 전 `docs/codex/phases/`에는 phase file이 없었고 첫 scoped 검증 시 이 target 하나뿐이었다. 이후 병행 세션들이 다른 phase Markdown을 같은 directory에 생성했으며, 이 세션은 정합성 확인을 위해 필요한 선행 draft를 read-only로 조회했을 뿐 수정하지 않았다. 이 세션의 edit/apply 범위와 target scoped status는 계속 `docs/codex/phases/phase-05-verification-finalization-and-publication.md` 하나다.
- Maven baseline: 관리 세션의 격리 phase-00 관찰(Java 25.0.3, Maven 3.9.14, 기존 `mvn test`/`mvn verify` 성공)을 사용했다. Shared `target/` 동시 실행 결과는 이 문서의 blocker/evidence로 사용하지 않았다.
- AR-5 production code/test/POM/progress는 이 문서 작성 세션에서 변경하지 않는다.
- 현재 progress status는 `BLOCKED`다. Phase-00만 `NOT_STARTED`이고, AR-5는 AR-0~AR-4 실제 구현/DONE evidence가 해소되기 전 안전하게 진행할 수 없다. `document_status=REVIEW_DRAFT`는 이 실행 명세의 작성·검토 상태일 뿐 코드 구현이 시작됐다는 뜻이 아니다.
- Implementation blocker는 `B-AR5-01`~`04`이며, 해소 전 production/test 구현을 시작하지 않는다.
