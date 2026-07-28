# Phase 00 — Build와 architecture 뼈대

```yaml
phase: "00"
canonical_filename: phase-00-build-architecture-skeleton.md
document_authoring_status: DRAFT_COMPLETE
document_status: REVIEWED_WITH_CORRECTIONS
implementation_status: NOT_STARTED
phase_acceptance_status: PLANNED
evidence_status: NOT_PRODUCED
baseline_date: 2026-07-28
inventory_checkout:
  repository: /Users/brown/workspace/ro-next
  branch: codex/domain-design
  commit: 3424277
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_status: ALNS_FIRST_GATE_OVERLAY_APPLIED_DOCUMENTATION_ONLY
source_fingerprints_sha256:
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
  docs/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d
  docs/implementation/README.md: 6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358
  docs/2026-07-26-master-design.md: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
source_sections:
  canonical_master: "§1.1~§1.5, §2.4, §3.2~§3.3, §4.3~§4.7, §15.1~§15.3, §16, §17"
  final_domain: "§1, §3, §7~§8, §17~§18"
  final_architecture: "§1.2~§1.5, §2.1~§2.7, §5.6, §6.1~§6.5"
  integrated_design: "§1.1~§1.5, §2, §3.1~§3.6, §4, §22~§25, §27~§28"
  open_questions: "§1~§4; Q-BENCH-02, Q-INFRA-01, Q-VAR-01 exact rows"
  realization_plan: "§1~§4, §6~§7 Phase 00, §8~§15"
  implementation_readme: "§1~§7"
  superseded_historical_cross_check: "§1.3~§1.5, §4, §10~§12 only"
public_api_status: NOT_DEFINED
scheduler_task_id: TBD
owners:
  implementation: Build·Architecture
  legacy_characterization: Application·Platform
  quality_gate: Build·Quality
  review: Architecture reviewer + Build reviewer
  handoff: Phase 01 Domain·Input owner
prerequisites:
  - 사용자 선언으로 고정된 source authority와 conflict rule
  - docs/implementation/master-realization-plan.md의 Phase 00 entry contract
  - commit 3424277의 read-only build/source/test/deployment inventory
  - 미확정 값을 production default로 만들지 않는다는 합의
handoff:
  next_phase: "01"
  next_document: phase-01-canonical-input-normalization.md
review:
  document: ../reviews/phase-00-review.md
  verdict: PASS_WITH_RESIDUAL_BLOCKERS
  implementation_authorized: false
```

## 1. 문서 지위, 권위와 사용법

이 문서는 Phase 00 구현을 위한 상세 설계이며 구현 완료 보고가 아니다. `document_authoring_status`는 이 Markdown의 작성 상태이고, `implementation_status`, `phase_acceptance_status`, `evidence_status`는 실제 코드·빌드·검증 상태다. 이 세 상태를 섞지 않는다.

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 원문 metadata의 `REVIEW`는 provenance로
보존하지만 이 문서 작성을 멈추는 조건이 아니다. 이 문서의 `REVIEWED_WITH_CORRECTIONS`는 문서
contract review만 끝났다는 뜻이며 Phase 00 구현, exit evidence 또는 acceptance가 끝났다는 뜻이 아니다.

다음 충돌 규칙을 적용한다.

1. 사용자 선언과 canonical Master의 최신 결정이 우선한다.
2. 질문 상태는 질문 등록부의 exact `Q-*` 행을 따른다.
3. Domain 의미는 canonical Master 불변조건을 유지하면서 Final Domain의 상세로 해석한다.
4. Java/Maven 배치는 의미를 바꾸지 않는 범위에서 Final Architecture를 따른다.
5. 15 Phase, no-DB, capability/profile, AWS reference와 provider substitution 구조는 구현 중심 통합 설계를 따른다.
6. [2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용한다.
7. `docs/codex/*`는 역사/참고 자료이며 이 Phase에서 복사·수정·삭제하거나 현재 authority로 사용하지 않는다.

Final Domain §18과 Final Architecture §6 일부의 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 최신 canonical Master와 질문 등록부에 의해 대체되었다. 현재 적용 상태는 `Q-INFRA-01 RESOLVED`, 질문 집계 `26/1/1`이며 target/reference는 AWS S3 + Step Functions + Lambda다. 다만 실제 AWS adapter, 배포, parity와 cutover는 Phase 11/14 gate다. Phase 00은 AWS SDK나 배포 skeleton을 만들지 않는다.

### 1.1 권위 입력 fingerprint와 사용 section

Fingerprint는 이 문서 작성 시 읽은 bytes의 SHA-256이다. 원문이 바뀌면 구현 착수 전에 이 표를 다시 계산하고 영향 section을 review한다.

| 역할 | 입력과 SHA-256 | Phase 00에서 직접 적용하는 section |
|---|---|---|
| Canonical Master | [Master Design](../../master-design.md), `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | §1.1~§1.5, §2.4, §3.2~§3.3, §4.3~§4.7, §15.1~§15.3, §16, §17 |
| Final Domain | [2026-07-26 Domain Design](../../2026-07-26-domain-design.md), `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | §1, §3의 Maven/package mapping, §7~§8의 immutable/COW 경계, §17~§18의 evidence와 drift 확인 |
| Final Architecture | [2026-07-26 Architecture Design](../../2026-07-26-architecture-design.md), `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | §1.2~§1.5, §2.1~§2.7, §5.6, §6.1~§6.5 |
| 구현 중심 통합 설계 | [Architecture-domain implementation design](../../architecture-domain-implementation-design.md), `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | §1.1~§1.5, §2, §3.1~§3.6, §4, §22~§25, §27~§28 |
| 질문 등록부 | [Master Design open questions](../../master-design-open-questions.md), `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | §1~§4 전체, 특히 `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` exact 행 |
| 총괄 실행 계획 | [Master Realization Plan](../master-realization-plan.md), `993979f67e8c07cf80a27c3aa3bd5bc379b12bc27657c4520ff44b093fda99be` | §1~§4, §6~§7의 Phase 00, §8~§15 |
| 구현 문서 지도 | [Implementation README](../README.md), `ad7534e51a967528efebe045926edca2c800d85437eda87e4bac6f29365a97cb` | §1~§7 전체 |
| Historical cross-check only | [2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md), `5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a` | §1.3~§1.5, §4, §10~§12를 현재 결정의 누락·퇴행 확인에만 사용 |

### 1.2 규범 표기

| 표기 | 의미 |
|---|---|
| **CONTRACT** | 상위 source에서 상속한 불변조건 |
| **PROPOSED INTERNAL** | Phase review에서 이름을 바꿀 수 있는 내부 module/package/type 설계 |
| **TEST-ONLY** | Test fixture/config에만 존재하며 production default가 아닌 값 또는 타입 |
| **FUTURE RED** | 해당 코드를 만들기 전에는 실패가 정상인 실행 가능한 test/command |
| **OPEN** | Owner와 승인 없이는 public contract나 공식값으로 확정할 수 없음 |
| **GATED** | 선행 evidence와 별도 scope 승인 전 구현·활성화 금지 |
| **DEFERRED** | Restart condition 전 질문·구현·활성화 금지 |

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 00의 목표는 다음 Phase가 잘못된 dependency 방향 위에서 시작할 수 없도록 **빌드 가능한 경계와 자동 위반 차단 장치**를 먼저 만드는 것이다.

1. 현재 단일 Maven/GCP placeholder를 의미 변경 없이 보존·characterize한다.
2. Java 25 기반 parent/aggregator reactor와 stable module DAG를 만든다.
3. `com.ronext.rpdptw` target namespace와 package ownership을 빈 marker class가 아닌 package/build contract로 예약한다.
4. Provider, customer, optional backend, verification 역의존과 test fixture production leakage를 `mvn verify`에서 차단한다.
5. Toolchain, plugin, dependency와 archive 생성 조건을 pin하고 online warm-up 뒤 offline build 가능한 조건을 evidence로 남긴다.
6. Phase 01이 canonical input/normalization을 구현할 수 있는 `rpdptw-core` skeleton과 test/evidence seam만 handoff한다.

### 2.2 범위

- Root parent/aggregator와 하위 aggregator/POM
- Java 25와 Maven 3.9.14 wrapper/toolchain enforcement
- Dependency/plugin version 중앙 관리와 reproducible archive policy
- `rpdptw-core`, `rpdptw-solver`, `rpdptw-verification`, `rpdptw-application`, `rpdptw-capabilities`, `rpdptw-profile-catalog` skeleton
- `build/test-fixtures`, `build/architecture-rules`
- Current `com.ronext.optimizer` source/test의 explicit legacy module 격리
- Existing HTTP/GCS/Workflow/synthetic objective behavior의 golden characterization
- ArchUnit, Maven Enforcer, dependency tree와 bytecode/source boundary 검사
- Optional-backend-free default `verify`, offline-prefetched `verify`, deterministic artifact digest evidence
- `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY` bundle과 rollback/handoff

### 2.3 비범위

- Canonical input field, alias, normalization, `ProblemInstance`, `PreparedTravel` 또는 RPDPTW 계산 구현
- Pickup-delivery pair, propagation, evaluation, profile binding, portfolio, ALNS, verifier 또는 result 구현
- Legacy endpoint를 target public API로 승인하거나 현재 합성 objective를 target behavior로 보존하는 결정
- S3, Step Functions, Lambda adapter/IaC, GCP cutover 또는 provider parity
- Database/object-storage CAS, coordinator와 multi-round runtime 구현
- Route pool/MIP module, `com.google.ortools` dependency 또는 capability 광고
- Public wire schema, public Java API, 공식 performance threshold
- `Q-BENCH-02`의 step/worker/round/watchdog 수치 확정
- Decimal `D/U` Win fixture를 official baseline으로 사용
- `Q-VAR-01`, multi-trip/rotation 또는 다른 optional variant

Phase 00에서 `CanonicalInput`, `ProblemInstance`, `PreparedTravel`, `BoundProfile`, `SearchSnapshot` 같은 빈 production class를 “나중을 위한 placeholder”로 만들지 않는다. Phase 01 이후의 owner가 불변조건과 test를 함께 구현할 때 처음 도입한다.

## 3. 관련 결정과 architecture 불변조건

Phase 00이 직접 강제할 결정은 `C-02`, `C-03`, `C-04`, `C-17`, `C-19`, `C-20`, `C-21`과 Architecture dependency contract다. Domain 계산을 구현하지 않지만 이후 계산이 침범할 수 없는 경계를 만든다.

### 3.1 Compile dependency DAG

별도 label이 없는 `A → B`는 A가 B를 production compile dependency로 가진다는 뜻이다.
`A -TEST→ B`는 A의 test compilation/execution에만 필요한 dependency이며 A의 main artifact나
production consumer에 전이되면 안 된다.

```text
rpdptw-core

rpdptw-solver           → rpdptw-core
rpdptw-verification     → rpdptw-core
rpdptw-capabilities     → rpdptw-core
rpdptw-profile-catalog  → rpdptw-core

rpdptw-application      → rpdptw-core
rpdptw-application      → rpdptw-solver
rpdptw-application      → rpdptw-verification

rpdptw-test-fixtures    -TEST→ rpdptw-core
  - src/test/java를 classifier tests의 test-jar로 package
  - consumer는 type=test-jar, classifier=tests, scope=test로만 의존

rpdptw-architecture-rules -TEST→ 모든 stable target module
  - test scope로 stable module bytecode를 읽음
  - production main artifact/runtime dependency로 소비되지 않음

legacy-gcp-placeholder
  - 기존 Google/Jackson dependency를 자체 module 안에만 보존
  - target module이 의존하지 않음
```

`rpdptw-profile-catalog`은 Phase 00에서 capability implementation을 compile-depend하지 않는다. 향후 distribution composition root가 approved capability registry와 profile catalog를 조립한다. Customer 수가 POM 수를 늘리지 않는다.

### 3.2 MUST/MUST NOT

1. Root `pom.xml`은 `packaging=pom`이며 business, customer, cloud 또는 optimizer dependency를 갖지 않는다.
2. Target stable module의 base namespace는 `com.ronext.rpdptw`다.
3. `core`는 다른 target module을 compile-depend하지 않는다.
4. `verification`은 `solver`, search/cache package 또는 legacy module을 compile-depend하지 않는다.
5. Core/solver/verification/application의 AWS/GCP/Azure/Kubernetes/HTTP SDK reference는 0이다.
6. Generic target module의 `com.google.ortools` API reference는 0이다.
7. Core/solver/verification의 customer-name conditional과 customer-specific package는 0이다.
8. 다른 module의 `.internal` package에 접근하지 않는다.
9. Test fixture bytecode는 attached `tests` classifier에만 넣고 consumer가
   `type=test-jar`, `classifier=tests`, `scope=test`로만 의존한다. Main artifact나 production
   compile/runtime classpath로 새지 않는다.
10. Default root `verify`는 cloud credential, OR-Tools 설치 또는 native runtime 없이 통과해야 한다.
11. Core는 environment variable, system clock, global random과 static mutable registry를 읽지 않는다.
12. OPEN/GATED/DEFERRED 기능을 나타내는 fake success provider나 production default를 만들지 않는다.
13. Legacy GCP dependency는 명시적 legacy allowlist 경계에서만 허용하며 target DAG의 예외로 전파하지 않는다.
14. Skeleton module 성공을 domain 기능 또는 Phase 01 evidence로 계산하지 않는다.

## 4. Entry gate와 선행 evidence 확인

### 4.1 Future implementation entry gate

| Entry 조건 | 확인 방법 | 현재 문서 작성 시 판정 |
|---|---|---|
| Source authority가 고정되고 fingerprint가 일치 | `shasum -a 256`으로 §1.1 재계산 | 확인됨. 구현 착수 시 재확인 필요 |
| Canonical Phase 파일명과 scope가 일치 | [Implementation README §4~§5](../README.md#4-canonical-phase와-review-index) 대조 | 확인됨 |
| Current checkout identity가 기록됨 | `git rev-parse --abbrev-ref HEAD`, `git rev-parse --short HEAD` | `codex/domain-design`, `3424277` |
| Dirty/untracked 사용자 작업을 덮어쓰지 않음 | `git status --short` 기록 후 대상 파일 overlap 검토 | `docs/implementation/`이 untracked. 이 문서 외 변경 금지 |
| Current build/source/test/deployment inventory가 read-only로 확인됨 | §5의 명령과 file fingerprint | 확인됨 |
| Phase implementation owner와 reviewer가 지정됨 | Scheduler task/review assignment | `scheduler_task_id: TBD`; 구현 시작 전 필요 |
| Phase 00 상세 문서가 review됨 | [Phase 00 review](../reviews/phase-00-review.md) | 문서 review 완료. `PASS_WITH_RESIDUAL_BLOCKERS`; 구현 acceptance/evidence는 미충족 |
| Code change authorization가 있음 | 별도 구현 task scope | 이 요청은 문서 작성만 허용. 아직 미충족 |

`scheduler_task_id` 또는 implementation authorization가 없으므로 이 문서 작성 직후 실제 POM/source 이동을 시작하지 않는다. 이는 설계 blocker이지 문서 작성 blocker가 아니다.

### 4.2 Inventory 재현 명령

다음은 read-only inventory 명령이다.

```bash
git rev-parse --abbrev-ref HEAD
git rev-parse --short HEAD
git status --short
java -version
mvn -version
git ls-files | sort
find src/main/java src/test/java gcp -type f -print | sort
rg -n '<module>|<packaging>|<maven.compiler.release>' pom.xml
rg -n 'com\.google|software\.amazon|com\.azure|io\.kubernetes|gurobi' pom.xml src
# Phase 13 gate-closed specific check:
rg -n 'com\.google\.ortools|route-selection-ortools-cpsat' pom.xml src
```

## 5. 현 상태 inventory와 target gap

### 5.1 Build/toolchain

| 항목 | 2026-07-28 현재 사실 | Phase 00 target gap |
|---|---|---|
| Maven project | Root `pom.xml` 하나, implicit `jar`, `com.ronext:ro-next:0.1.0-SNAPSHOT`; `<modules>` 없음 | Parent/aggregator reactor와 module DAG 없음 |
| Java/Maven pin | `.sdkmanrc`에 `25.0.3-amzn`, Maven `3.9.14`; local도 Corretto 25.0.3/Maven 3.9.14 | Maven Wrapper와 wrapper distribution checksum 없음 |
| Compiler/enforcer | release 25, Java `[25,26)`, Maven `[3.9.14,)` | Exact wrapper, upper-bound/compatibility policy와 모든 module enforcement 필요 |
| Plugin pin | Enforcer `3.6.1`, compiler `3.14.1`, Surefire `3.5.4`, Shade `3.6.1`은 직접 pin | Lifecycle 전체/effective POM pinning 및 새 plugin/ArchUnit version policy 없음 |
| Dependencies | Google Workflow Executions `2.94.0`, GCS `2.70.0`, Jackson `2.19.2`, JUnit `5.13.1`이 root classpath | Provider와 JSON dependency가 stable core에서 분리되지 않음 |
| Resolved dependency conflict | Current verbose tree는 Google Storage의 Jackson `2.18.3`과 직접 선언 `2.19.2`가 섞여 resolve됨 | Strict convergence를 legacy에도 즉시 적용하면 baseline build가 막힘. §14의 reviewed legacy-only policy 결정 전 silent alignment/upgrade 금지 |
| Packaging | Shade main `com.ronext.optimizer...OptimizationHttpServer` | Target distribution과 stable module artifact 없음 |
| Shade collision | Current `mvn verify`는 module descriptor, Jackson/gRPC service entry, license/notice/manifest 중복을 경고함 | Source move 전후 dependency graph, collision inventory와 selected service-resource semantics를 golden evidence로 비교해야 함 |
| Container | Root Dockerfile이 Maven build 후 Corretto 25에서 shaded JAR 실행 | Legacy image만 표현하며 target/local/AWS distribution이 아님 |
| Offline | Docker가 `dependency:go-offline` 후 build하지만 local controlled cache evidence 없음 | Wrapper cache + isolated repository warm-up 뒤 `-o verify` evidence 필요 |
| Reproducibility | Encoding은 UTF-8 | Archive timestamp, ordered content, two-clean-build digest evidence 없음 |
| Quality gate | 현재 Enforcer tool version과 한 JUnit test | DAG, forbidden dependency, internal package, test leakage, bytecode gate 없음 |

현재 build file fingerprint:

| 파일 | SHA-256 |
|---|---|
| `pom.xml` | `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6` |
| `.sdkmanrc` | `25c276822911b813a58c317ee47b86608e51c79ba921c65706df9f9f74793be5` |
| `Dockerfile` | `2aae6615e6c3dba5184064593e29407f7cded4e63dd416719a0b2fb30846d985` |

### 5.2 Source와 test

| 항목 | 현재 사실 | 해석 |
|---|---|---|
| Main source | 6개 Java file, `com.ronext.optimizer` | Target `com.ronext.rpdptw` code는 0개 |
| Application placeholder | `AlnsBatchEngine.run(...)`이 input bytes를 읽지 않고 seed/iterations로 `double objective` 생성 | ALNS, feasibility, normalization evidence가 아님 |
| HTTP/API | `/optimizations`, `/internal/batches`, `/internal/finalize`; `Map<String,Object>` payload | Legacy characterization 대상, target public API 아님 |
| Provider coupling | Controller가 GCS/Workflow client를 직접 생성하고 env를 직접 읽음 | Target port/adapter 경계와 불일치 |
| Completion | `candidates/{requestId}/` prefix listing 후 raw `objective` 최솟값 선택 | Declared completeness와 independent verifier 계약에 위배되는 legacy behavior |
| Test | `AlnsBatchEngineTest` 1개, synthetic candidate만 검사 | Target Phase evidence가 아님 |
| Ignored output | `target/`에 이전 JAR와 1-test Surefire report가 있음 | 이번 Phase evidence가 아니며 재사용 금지 |

Current source/test SHA-256은 다음과 같다.

```text
90bf71d9...  HttpJson.java
33e6d1cd...  JsonSupport.java
3dcab11f...  OptimizationApiController.java
2284c20a...  OptimizationHttpServer.java
846e64f1...  OptimizationWorkerController.java
4120203d...  AlnsBatchEngine.java
947cf045...  AlnsBatchEngineTest.java
```

위 축약 표기는 inventory 식별용이다. Evidence bundle은 full 64자리 값을 저장해야 한다.

### 5.3 Deployment/operations inventory

| 항목 | 현재 사실 | Phase 00 취급 |
|---|---|---|
| `gcp/cloudbuild.yaml` | Docker image build, SHA `284663c833bb3550497f412e507e25a4d12b64f4bcae0ad993c9bd576f76b209` | Legacy characterization |
| `gcp/workflows/optimization.yaml` | parallel batch HTTP 후 finalize, SHA `65eeef9344a63b743af1684b915c9bdfa976454e560ebb8624d75ed00fd874c1` | Legacy characterization |
| `gcp/README.md` | Cloud Run/Workflows/GCS/IAM 예시, SHA `8294e4c3bbb7b92b0d7f51136b18aaa342fe3c9f06ec822dc23ce5a75c915913` | Deployment guide일 뿐 배포 사실 아님 |
| Tracked CI/AWS IaC | `.github` workflow, tracked Serverless/SAM/CDK/Terraform 없음 | AWS implementation/CI 완료를 추론하지 않음 |
| Ignored local 흔적 | `.serverless/`, `node_modules/`, `target/` 존재 | Source authority/evidence가 아니며 수정·복사하지 않음 |

Phase 00은 실제 cloud에 접속해 배포 여부를 확인하지 않는다. 이 inventory는 tracked local checkout의 source 상태만 말한다.

## 6. 변경 대상 module/package/file tree

다음은 **PROPOSED INTERNAL** target이다. Phase 00 review에서 이름은 바꿀 수 있지만 §3의 의존 방향과 격리는 바꿀 수 없다.

```text
ro-next/
├── pom.xml                                  # parent + root aggregator, no business dependency
├── .mvn/
│   ├── wrapper/
│   │   └── maven-wrapper.properties         # Maven 3.9.14 URL + official checksum pin
│   └── toolchains.example.xml               # path/secret-free JDK 25 local setup example
├── mvnw
├── mvnw.cmd
├── build/
│   ├── pom.xml                              # build-only aggregator
│   ├── test-fixtures/
│   │   ├── pom.xml                          # attached tests classifier; consumer scope=test only
│   │   └── src/test/java/com/ronext/rpdptw/fixture/
│   └── architecture-rules/
│       ├── pom.xml
│       └── src/test/java/com/ronext/rpdptw/architecture/
├── rpdptw/
│   ├── pom.xml                              # target semantic parent/aggregator + banned deps
│   ├── core/
│   │   ├── pom.xml
│   │   └── src/main/java/com/ronext/rpdptw/
│   │       ├── input/package-info.java
│   │       ├── domain/package-info.java
│   │       ├── normalization/package-info.java
│   │       ├── travel/package-info.java
│   │       ├── propagation/package-info.java
│   │       └── evaluation/
│   │           ├── api/package-info.java
│   │           ├── runtime/package-info.java
│   │           └── insertion/package-info.java
│   ├── solver/
│   │   ├── pom.xml
│   │   └── src/main/java/com/ronext/rpdptw/solver/
│   │       ├── portfolio/package-info.java
│   │       ├── search/package-info.java
│   │       ├── state/package-info.java
│   │       └── termination/package-info.java
│   ├── verification/
│   │   ├── pom.xml
│   │   └── src/main/java/com/ronext/rpdptw/
│   │       ├── verification/
│   │       │   ├── api/package-info.java
│   │       │   ├── candidate/package-info.java
│   │       │   └── result/package-info.java
│   │       └── result/
│   │           ├── api/package-info.java
│   │           └── finalization/package-info.java
│   ├── application/
│   │   ├── pom.xml
│   │   └── src/main/java/com/ronext/rpdptw/application/
│   │       ├── port/in/package-info.java
│   │       ├── port/out/package-info.java
│   │       ├── service/package-info.java
│   │       └── execution/package-info.java
│   ├── capabilities/
│   │   ├── pom.xml
│   │   └── src/main/java/com/ronext/rpdptw/capability/package-info.java
│   └── profile-catalog/
│       ├── pom.xml
│       └── src/main/java/com/ronext/rpdptw/profile/catalog/package-info.java
├── legacy/
│   ├── pom.xml                              # legacy-only aggregator
│   └── gcp-placeholder/
│       ├── pom.xml                          # current dependencies + shade packaging
│       ├── src/main/java/com/ronext/optimizer/...
│       └── src/test/java/com/ronext/optimizer/...
├── gcp/                                     # tracked legacy deployment inventory preserved
├── Dockerfile                               # legacy module build path만 조정; semantic change 금지
└── docs/
```

Phase 00은 `adapters/object-s3`, `workflow-aws-stepfunctions`, `compute-aws-lambda`, `apps`, `distributions`, `deployment/aws`를 빈 module로 만들지 않는다. 각 owner Phase가 contract와 test를 함께 추가한다. `solver.pool`, `solver.selection`, `solver.hybrid`도 `C-17` 승인 전 package skeleton을 만들지 않는다.

### 6.1 Package ownership

| Package root | Phase 00에서 허용하는 내용 | 금지 |
|---|---|---|
| `com.ronext.rpdptw.input/domain/normalization` | Javadoc/package contract만 | DTO, normalization logic, numeric default |
| `com.ronext.rpdptw.travel/propagation/evaluation` | Dependency direction을 설명하는 `package-info.java`만 | Travel/evaluation placeholder implementation |
| `com.ronext.rpdptw.solver` | Package ownership 선언만 | ALNS fake, random objective, route pool/MIP |
| `com.ronext.rpdptw.verification/result` | Solver 독립성 선언만 | `alwaysPass` verifier |
| `com.ronext.rpdptw.application` | Provider-neutral ownership 선언만 | S3/GCS/Lambda/HTTP type |
| `com.ronext.rpdptw.capability` | Reusable capability ownership 선언만 | Customer 이름 class |
| `com.ronext.rpdptw.profile.catalog` | Data-driven catalog ownership 선언만 | `latest` fallback, arbitrary executable rule |
| `com.ronext.optimizer` | Legacy behavior와 characterization | Target type의 역유입 |

## 7. 입력·출력 artifact, contract, identity와 lifecycle

### 7.1 Phase 입력

| 입력 | Authority |
|---|---|
| Source fingerprint set | §1.1 |
| Checkout/build inventory | §5 |
| Target module DAG | §3.1 |
| Existing legacy source/test/deployment | `pom.xml`, `src/**`, `gcp/**`, `Dockerfile` |
| Explicit build configuration | JDK/Maven/plugin/dependency pin과 architecture policy |

### 7.2 Phase 출력

| Artifact | 내용 | Consumer |
|---|---|---|
| `BuildBaseline` | Commit, dirty-state manifest, source/build fingerprints, toolchain | 모든 후속 Phase |
| `ReactorTopology` | Module coordinate, packaging, parent, allowed compile edge | Phase 01~14와 architecture rule |
| `BuildPolicySnapshot` | JDK/Maven/plugin/dependency/archive/offline policy | Build/CI/release |
| `ArchitecturePolicySnapshot` | Forbidden group/package/module/internal/test edge | 모든 target module |
| `TestFixtureArtifactContract` | `tests` classifier, test-jar type, test-only dependency edge와 leakage rule | Phase 01 이후 test consumer |
| `LegacyCharacterizationReport` | Endpoint, payload, default, object key, workflow, synthetic result와 known mismatch | Migration/Phase 14 |
| `E-P00-BUILD` | Root online/offline/reproducible build evidence | Phase 01 entry/release |
| `E-P00-ARCH` | Enforcer/ArchUnit/dependency/bytecode/source scan evidence | Phase 01 entry |
| `E-P00-LEGACY` | Legacy golden test와 unchanged behavior report | Migration |
| `Phase01SkeletonHandoff` | `rpdptw-core`, test fixture seam, package ownership, exact build command | Phase 01 |

이 artifact 이름은 evidence 책임을 표현하는 **PROPOSED INTERNAL** 이름이다. Public Java API나 wire schema가 아니다.

### 7.3 Public/internal contract

Phase 00은 public runtime API를 정의하지 않는다. 외부 호환성 약속은 0개다.

Phase 00의 internal contract는 다음 세 가지다.

1. Maven coordinate와 compile edge.
2. Java package의 owner/visibility.
3. Root `verify`가 실행하는 quality gate와 evidence 형식.

Legacy HTTP endpoint와 GCS object key는 `LEGACY_ONLY` characterization이며 target contract가 아니다.

### 7.4 Identity와 lifecycle

```text
DISCOVERED
→ BASELINED
→ LEGACY_CHARACTERIZED
→ REACTOR_ASSEMBLED
→ ARCHITECTURE_GUARDS_GREEN
→ EVIDENCE_SEALED
→ REVIEW_PENDING
→ ACCEPTED

failure:
  any pre-review state
  → discard unaccepted target skeleton changes
  → restore last recorded safe commit without deleting user work
```

권장 identity:

```text
BuildBaselineId
  = repository commit
  + tracked/untracked scope manifest digest
  + source authority fingerprint-set digest

BuildRuntimeFingerprint
  = JDK distribution/version
  + Maven wrapper distribution/checksum
  + effective POM/plugin/dependency lock digest
  + OS/architecture compatibility metadata

ArchitecturePolicyId
  = allowed module DAG
  + forbidden group/package patterns
  + rule implementation/version digest

LegacyCharacterizationId
  = legacy source/deployment digest
  + golden fixture digest
  + characterization test/report digest
```

Elapsed build time과 machine path는 관측 metadata이며 artifact content identity가 아니다.

## 8. Java 수준 설계

### 8.1 Production type 방침

Phase 00 production source는 `package-info.java`와 필요한 module descriptor 수준에 그친다. 단순히 JAR을 non-empty로 만들기 위한 `CoreMarker`, `SolverMarker`, 빈 service interface 또는 항상 성공하는 verifier를 만들지 않는다.

`module-info.java`/JPMS는 Final Architecture의 ADR backlog에 남아 있으므로 Phase 00 default로 도입하지 않는다. Maven module 경계와 package/bytecode test를 먼저 사용한다.

### 8.2 Architecture rule test type 후보

다음 이름과 signature는 **TEST-ONLY / PROPOSED INTERNAL**이다. 실제 구현은 ArchUnit API를 직접 사용해도 된다.

```java
final class StableModuleDependencyArchitectureTest {
    @Test void coreHasNoOutboundProjectDependency();
    @Test void solverDependsOnlyOnCore();
    @Test void verificationDependsOnlyOnCore();
    @Test void applicationDependsOnlyOnCoreSolverAndVerification();
}

final class ProviderAndVendorIsolationArchitectureTest {
    @Test void stableModulesDoNotReferenceCloudTransportOrVendorApis();
    @Test void defaultAssemblyAdvertisesNoRouteSelectionCapability();
}

final class PackageBoundaryArchitectureTest {
    @Test void modulesDoNotAccessAnotherModulesInternalPackages();
    @Test void dtoSuffixExistsOnlyAtExternalOrApplicationBoundary();
}

final class CustomerIsolationArchitectureTest {
    @Test void genericModulesContainNoCustomerSpecificPackage();
    @Test void genericSourceContainsNoCustomerIdentityConditional();
}

final class TestScopeLeakageArchitectureTest {
    @Test void productionClasspathContainsNoTestFixtureArtifact();
}
```

ArchUnit rule factory 후보:

```java
final class RpdptwArchitectureRules {
    static ArchRule stableSemanticModulesDoNotDependOnProviderPackages();
    static ArchRule verificationDoesNotDependOnSolverOrSearchPackages();
    static ArchRule noCrossModuleInternalPackageAccess();
    static ArchRule genericModulesDoNotDependOnOptimizerVendorPackages();
}
```

Rule은 target `com.ronext.rpdptw..`만 검사하고 explicit legacy module을 target success로 위장하지 않는다. Maven Enforcer가 artifact-level dependency를, ArchUnit/jdeps가 bytecode/package-level dependency를, source scan이 customer-name conditional과 forbidden literal을 보완한다. 한 도구의 blind spot을 다른 도구의 성공으로 덮지 않는다.

### 8.3 Rule result hierarchy 후보

Custom report가 필요할 때만 다음 **TEST-ONLY** hierarchy를 쓴다. ArchUnit report로 충분하면 만들지 않는다.

```java
sealed interface ArchitectureViolation
    permits ForbiddenArtifactDependency,
            ForbiddenPackageDependency,
            InternalPackageLeak,
            TestScopeLeak {
    String ruleId();
    String evidence();
}

record ForbiddenArtifactDependency(
    String ruleId,
    String fromArtifact,
    String toArtifact,
    String scope,
    String evidence
) implements ArchitectureViolation {}

record ForbiddenPackageDependency(
    String ruleId,
    String originClass,
    String targetClass,
    String evidence
) implements ArchitectureViolation {}

record InternalPackageLeak(
    String ruleId,
    String originClass,
    String targetInternalPackage,
    String evidence
) implements ArchitectureViolation {}

record TestScopeLeak(
    String ruleId,
    String productionArtifact,
    String leakedFixture,
    String evidence
) implements ArchitectureViolation {}
```

이 hierarchy는 domain/public API가 아니며 build report serialization을 승인하지 않는다.

### 8.4 Phase 01 owner에게 예약하는 type 경계

다음은 Phase 01 설계 논의를 위한 **PROPOSED, PHASE-01-OWNED** 후보다. Phase 00에서 구현하지 않는다.

```java
record CanonicalInput(/* Phase 01 reviewed fields */) {}

sealed interface NormalizationResult
    permits NormalizationResult.Accepted,
            NormalizationResult.Rejected {
    record Accepted(/* immutable normalized facts */)
        implements NormalizationResult {}
    record Rejected(/* typed pre-solve errors */)
        implements NormalizationResult {}
}

interface CanonicalInputNormalizer {
    NormalizationResult normalize(CanonicalInput input);
}
```

Field, error code, method name, visibility와 serialization은 OPEN이며 Phase 01 source/requirement/test review에서 확정한다. 위 후보는 `external DTO ≠ canonical input ≠ normalized facts`라는 dependency direction만 설명한다.

### 8.5 Build dependency pseudo-code

```text
for each Maven target module:
    resolve compile/runtime graph from effective POM
    reject edge not present in allowed DAG
    reject cloud/transport/vendor group in stable semantic modules
    reject test-fixture artifact on production classpath

import compiled target classes:
    reject verification → solver/search/cache bytecode edge
    reject cross-module .internal access
    reject provider/vendor package reference in stable namespace

scan target source:
    reject declared customer-identity token/conditional outside authorized boundary
    reject System.getenv/currentTimeMillis/nanoTime/global random in core

record scanner coverage manifest and require independent change review

if any check is incomplete or skipped:
    root verify is not green
```

## 9. Ordered implementation work packages

각 package는 앞 package의 green evidence를 입력으로 받는다. 중간 실패 시 아직 review되지 않은 skeleton을 Phase 01 authority로 넘기지 않는다.

### WP-00-0 — Baseline과 변경 격리

**사전조건**

- §4 entry inventory를 다시 실행한다.
- Implementation task ID, owner와 reviewer가 지정된다.
- 사용자 작업과 겹치는 file을 식별한다.

**수정 대상**

- Evidence workspace 또는 digest-protected bundle만 생성.
- 아직 POM/source를 변경하지 않는다.

**구체 작업**

1. Authority source 8개의 full SHA-256과 exact commit을 고정한다.
2. `git status --short`, tracked file manifest, ignored artifact 존재를 기록한다.
3. `pom.xml`, `.sdkmanrc`, Docker/GCP/source/test full digest를 저장한다.
4. Current Maven dependency, plugin, packaging과 endpoint/object-key/workflow inventory를 report로 고정한다.
5. Verbose resolved dependency tree, omitted-for-conflict 행, Shade duplicate/collision warning과 selected
   service-resource inventory를 고정한다.
6. Secret, env value, raw PII를 report에 넣지 않는다.

**검증 명령**

```bash
git rev-parse HEAD
git status --short
git ls-files -z | xargs -0 shasum -a 256
java -version
mvn -version
```

**기대 결과**

- 다른 machine/reviewer가 같은 commit의 tracked digest를 재계산할 수 있다.
- Dirty/untracked 파일은 삭제·흡수되지 않고 별도 scope로 표시된다.

**Failure/rollback**

- Source fingerprint mismatch면 새 결정을 만들지 않고 영향 source를 다시 읽어 문서 review로 돌아간다.
- 대상 file overlap이면 구현을 중단하고 owner에게 범위를 확인한다.

**다음 handoff**

- `BuildBaselineId`를 WP-00-1과 WP-00-2에 전달한다.

### WP-00-1 — Legacy placeholder characterization과 격리

**사전조건**

- WP-00-0 baseline 고정.
- Legacy behavior가 target contract가 아니라는 review 확인.

**수정 대상**

- `legacy/pom.xml`
- `legacy/gcp-placeholder/pom.xml`
- 현재 `src/main/java/com/ronext/optimizer/**`
- 현재 `src/test/java/com/ronext/optimizer/**`
- `Dockerfile`의 module build/copy path
- Characterization fixture/test
- `gcp/**`는 semantic 변경 없이 경로 참조가 필요한 최소 변경만 허용

**구체 작업**

1. `git mv`로 기존 Java source/test를 `legacy/gcp-placeholder` module로 옮겨 history를 보존한다.
2. Current dependency와 shade main class를 legacy POM에만 둔다.
3. Synthetic objective, endpoint, default/clamp, env, object key, prefix listing, workflow parallel/finalize 순서를 golden characterization한다.
4. Invalid/missing input, 잘못된 URI, unsupported method/path, missing result, empty candidate list,
   client/storage failure의 현재 HTTP status와 redacted error body를 golden characterization한다.
5. 이동 전후 verbose dependency graph, shaded JAR main manifest, entry/service-resource inventory와
   collision report를 비교한다. 충돌을 임의 exclusion이나 dependency upgrade로 조용히 해소하지 않는다.
6. Runtime testability를 위해 꼭 필요한 경우 package-private constructor/facade seam만 추가하고 외부 response, default와 storage key는 바꾸지 않는다.
7. Target module이 legacy artifact를 의존하지 못하도록 Enforcer rule input을 준비한다.
8. Dockerfile은 같은 legacy shaded app을 build하도록 module path만 조정한다. Target/AWS image로 이름을 바꾸지 않는다.

**정확한 test 후보**

```text
com.ronext.optimizer.application.AlnsBatchEngineCharacterizationTest
  sameSeedRunAndIterationsProduceSameSyntheticCandidate()
  inputUriBytesAreNotReadByCurrentPlaceholder()

com.ronext.optimizer.adapter.in.http.LegacyOptimizationContractCharacterizationTest
  exposesCurrentPublicAndInternalPaths()
  appliesCurrentParallelRunIterationAndSeedDefaults()
  storesCandidatesAndResultAtCurrentObjectKeys()
  finalizesFromVisiblePrefixAndRawMinimumObjective()
  returnsCurrentNotFoundMethodAndValidationErrors()
  returnsRunningWhenResultObjectIsMissing()
  returnsCurrentRedactedFailureForStorageWorkflowAndEmptyCandidateFailures()

com.ronext.optimizer.adapter.in.http.LegacyWorkflowCharacterizationTest
  dispatchesDeclaredParallelRangeThenFinalize()
  derivesCurrentWorkerSeedFromBaseSeedAndRunNumber()

com.ronext.optimizer.adapter.in.http.LegacyShadedArtifactCharacterizationTest
  preservesMainClassAndSelectedServiceResources()
  reportsEveryBaselineDependencyConflictAndShadeCollision()
```

**검증 명령**

```bash
./mvnw -B -ntp -pl legacy/gcp-placeholder -am test
./mvnw -B -ntp -pl legacy/gcp-placeholder -am package
```

첫 명령은 wrapper를 만드는 WP-00-2 전에는 **FUTURE RED**다. WP-00-1만 임시 검증할 때는 pinned local `mvn`을 사용할 수 있지만 최종 evidence는 wrapper 명령으로 다시 만든다.

**기대 결과**

- Legacy test와 shaded app이 이동 전과 같은 observable contract를 보존한다.
- Google/Jackson dependency가 legacy module 밖 target classpath에 없다.
- Legacy dependency conflict/collision은 reviewed policy 없이 사라지거나 새로 생기지 않으며, 예외가
  있으면 coordinate/rule/reason/owner가 `E-P00-LEGACY`에 기록된다.
- Report는 prefix listing/raw objective/current default를 목표가 아닌 known mismatch로 분류한다.

**Failure/rollback**

- Golden behavior가 의도치 않게 바뀌면 source move와 test seam을 되돌리고 baseline source에서 다시 시작한다.
- Strict convergence와 current legacy graph가 충돌하면 version alignment나 broad Enforcer skip을
  선택하지 않고 §14의 owner 결정을 기다린다.
- Cloud credential이 필요한 test는 Phase 00 gate로 채택하지 않고 deterministic fake로 대체한다. 실제 GCP 배포 성공을 만들지 않는다.

**다음 handoff**

- `E-P00-LEGACY` 초안을 WP-00-5에 전달한다.

### WP-00-2 — Parent/aggregator, toolchain pin과 offline/reproducible build

**사전조건**

- WP-00-0 baseline.
- Legacy module coordinate가 정해짐.
- §14의 legacy dependency convergence/upper-bound와 Shade collision policy가 Build·Legacy owner에게 review됨.

**수정 대상**

- Root `pom.xml`
- `.mvn/wrapper/maven-wrapper.properties`, `mvnw`, `mvnw.cmd`
- `.mvn/toolchains.example.xml`과 machine-local toolchain 설정 안내
- `rpdptw/pom.xml`, `build/pom.xml`, `legacy/pom.xml`
- `.gitignore`의 generated evidence/cache 경로

**구체 작업**

1. Root를 business dependency가 없는 `packaging=pom` parent/aggregator로 전환한다.
2. Maven Wrapper distribution을 Maven `3.9.14`와 official distribution SHA-256으로 pin한다. Checksum은 공식 release source에서 확인해 저장하며 추정값을 쓰지 않는다.
3. Java release 25, UTF-8, JUnit과 build plugin version을 parent property/`dependencyManagement`/`pluginManagement`에 중앙화한다.
4. Maven Toolchains Plugin도 exact version으로 pin하고 compiler/test가 version `[25,26)` JDK를 선택하게 한다. Vendor는 고정하지 않고 실제 vendor/version을 evidence에 기록한다.
5. Repository에는 절대 `jdkHome`, credential, 사용자 홈 경로가 없는 `.mvn/toolchains.example.xml`만 둔다. 실제 경로는 machine-local toolchains file 또는 Maven JDK discovery로 공급하며 source에 commit하지 않는다.
6. 현재 직접 pin된 Enforcer `3.6.1`, compiler `3.14.1`, Surefire `3.5.4`, Shade `3.6.1`은 변경 근거가 없으면 initial baseline으로 이관한다.
7. 새 ArchUnit 및 lifecycle plugin은 exact version을 POM에 기록한다. `LATEST`, range, unversioned plugin을 금지한다.
8. `requireJavaVersion [25,26)`, exact wrapper Maven, `requirePluginVersions`, duplicate/dependency convergence와 upper-bound 정책을 설정한다.
9. `project.build.outputTimestamp`의 source를 명시적으로 고정한다. 값/derivation은 build ADR에 기록하고 wall clock default를 사용하지 않는다.
10. Wrapper bootstrap과 controlled local repository warm-up 뒤 `-o verify`가 network 없이 동작함을 검증한다.
11. Target module에는 strict convergence/upper-bound를 적용한다. Legacy module은 §14에서 승인된
    exact policy가 있을 때만 좁은 coordinate/rule 예외를 허용하며 global skip은 금지한다.
12. 구현 source를 commit 또는 content-addressed source archive로 동결한 뒤, 그 동일 snapshot에서
    두 독립 clean workspace를 만들고 같은 output timestamp로 publishable JAR digest를 비교한다.

ArchUnit/plugin exact version과 archive timestamp derivation은 public API가 아닌 build 결정이지만 **값을 POM에 넣기 전 Build owner review가 필요**하다. Placeholder property나 floating version으로 gate를 우회하지 않는다.

**검증 명령**

```bash
./mvnw --version
./mvnw -B -ntp toolchains:display-discovered-jdk-toolchains
./mvnw -B -ntp -Dstyle.color=never validate
./mvnw -B -ntp -Dstyle.color=never -Dmaven.repo.local=target/phase-00-m2 dependency:go-offline
./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 verify
./build/verify-reproducible-build.sh
```

`./mvnw`와 `./build/verify-reproducible-build.sh`는 현재 checkout에 없으므로 구현 전 **FUTURE RED**다.
Script는 evidence에 기록한 exact implementation commit 또는 content-addressed source archive에서 두
임시 clean workspace를 만들고, 같은 wrapper/cache/output timestamp, stable artifact list와 SHA-256을
비교한다. Dirty/untracked implementation bytes가 snapshot에서 빠졌거나 두 workspace의 source manifest가
다르면 build 전에 non-zero로 종료해야 한다.

**기대 결과**

- Wrapper는 Maven 3.9.14로 실행되고 compiler/test toolchain은 Java `[25,26)`를 선택한다. 현재 local Corretto 25.0.3은 허용되는 관측값이지 vendor-wide 요구사항이 아니다.
- Warm-up이 완료된 isolated repository에서 offline root `verify`가 성공한다.
- 같은 source의 두 clean build artifact digest가 일치한다.
- Default root build가 cloud credential/OR-Tools native runtime을 요구하지 않는다.

**Failure/rollback**

- Offline failure는 자동 network fallback으로 숨기지 않고 누락 dependency/plugin과 repository source를 report한다.
- Digest mismatch는 timestamp, archive order, generated metadata를 조사하고 `E-P00-BUILD`를 seal하지 않는다.
- Wrapper checksum이 공식 source와 다르면 실행하지 않는다.

**다음 handoff**

- Pinned `BuildPolicySnapshot`을 WP-00-3/4에 전달한다.

### WP-00-3 — Stable module과 package skeleton

**사전조건**

- WP-00-2 parent/aggregator가 green.
- §3.1 DAG review 완료.

**수정 대상**

- §6의 `rpdptw/**`와 `build/test-fixtures/**`
- 각 module POM과 `package-info.java`

**구체 작업**

1. Core, solver, verification, application, capabilities, profile-catalog, test-fixtures module을 생성한다.
2. POM compile edge를 §3.1 allowlist와 정확히 맞춘다.
3. Package Javadoc에 owner, allowed dependency, forbidden responsibility를 쓴다.
4. 구현 세부용 `.internal` convention을 정의하되 cross-module access를 허용하지 않는다.
5. Empty marker/fake service/domain DTO 없이 compile 가능한 skeleton을 만든다.
6. Phase 13 GATED package와 future provider module은 생성하지 않는다.
7. `rpdptw-test-fixtures`는 `src/test/java`를 Maven test-jar의 `tests` classifier로 attach한다.
   Consumer 예제와 effective POM은 `type=test-jar`, `classifier=tests`, `scope=test`를 모두 명시한다.
8. `rpdptw-core`가 Phase 01의 유일한 semantic production-code 시작점임을 handoff manifest에 기록한다.

**검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/capabilities,rpdptw/profile-catalog -am verify
./mvnw -B -ntp -Dstyle.color=never -pl build/test-fixtures -am verify
./mvnw -B -ntp -Dstyle.color=never help:effective-pom
./mvnw -B -ntp -Dstyle.color=never dependency:tree
```

**기대 결과**

- Reactor가 cycle 없이 topological order로 build한다.
- `verification` dependency tree에 solver/legacy/provider가 없다.
- Root/business dependency가 0이고 test-fixtures attached classifier가 실제 생성되며 test consumer에서
  resolve된다. Main artifact/production compile/runtime dependency tree에는 fixture가 없다.

**Failure/rollback**

- Cycle 또는 금지 edge가 생기면 module 합병으로 숨기지 않고 owner/contract를 다시 나눈다.
- Skeleton을 compile시키기 위한 fake production class가 필요하면 POM/package 설계를 고치고 class를 만들지 않는다.

**다음 handoff**

- Compiled module bytecode와 `ReactorTopology`를 WP-00-4에 전달한다.

### WP-00-4 — Enforcer, ArchUnit, bytecode와 negative fixture gate

**사전조건**

- WP-00-3 target bytecode 생성.
- Forbidden group/package/customer/internal 규칙 review 완료.

**수정 대상**

- `build/architecture-rules/pom.xml`
- `build/architecture-rules/src/test/**`
- Target parent Enforcer configuration
- 필요 시 `build/verify-bytecode-boundaries.sh`

**구체 작업**

1. Maven Enforcer로 artifact-level forbidden dependency와 test-scope leakage를 막는다.
2. ArchUnit으로 verification isolation, package direction, provider/vendor/internal access를 검사한다.
3. `jdeps` 또는 동등 bytecode scan으로 transitive/provider reference를 확인한다.
4. Source scan으로 core environment/clock/random과 generic customer-name conditional을 검사한다.
5. 의도적 bad fixture가 각 rule에 의해 실패하는 self-test를 만든다.
6. Legacy allowlist는 module coordinate와 namespace로 좁게 제한한다. Target rule을 끄는 global skip property를 제공하지 않는다.
7. Skipped/incomplete architecture test가 root `verify` 성공으로 보이지 않게 한다.
8. Customer-identity 검사는 승인된 identity/token manifest, 허용 package
   (`profile-catalog`, 향후 adapter authorization)와 AST/bytecode dependency rule을 함께 사용한다.
   Scanner self-test는 conditional/string/switch bad fixture를 각각 검출해야 한다. 선언되지 않은 미래
   업무 문자열까지 정적으로 완전 검출한다고 주장하지 않고 coverage manifest와 독립 change review를
   evidence에 남긴다.
9. Phase 00에는 target distribution이 없으므로 “default assembly capability 없음” oracle은 reactor module,
   service registration/resource와 capability advertisement class의 부재를 검사한다. 존재하지 않는
   runtime assembly를 실행했다고 주장하지 않는다.

**검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test
./mvnw -B -ntp -Dstyle.color=never verify
./build/verify-bytecode-boundaries.sh
if rg -n 'com\.google|software\.amazon\.awssdk|com\.azure|io\.kubernetes|gurobi' rpdptw; then exit 1; fi
if rg -n 'com\.google\.ortools|route-selection-ortools-cpsat' pom.xml rpdptw adapters; then exit 1; fi
```

**기대 결과**

- Reactor cycle 0.
- Stable module provider/vendor dependency 0.
- Verification → solver/search/cache dependency 0.
- Cross-module `.internal` access 0.
- Test fixture production leakage 0.
- Declared customer-identity manifest와 structural rule 범위의 unauthorized match 0.
- Target reactor/service-resource inventory에 route-selection capability advertisement 0.
- Bad fixture는 rule self-test에서 예상 violation으로 검출된다.

**Failure/rollback**

- False positive는 broad exclude로 끄지 않고 package owner/allowlist를 좁게 문서화한다.
- False negative가 발견되면 gate를 green으로 인정하지 않고 negative fixture를 먼저 추가한다.
- Rule 실행이 JDK/ArchUnit version에 따라 달라지면 exact build/runtime fingerprint와 compatibility를 갱신한다.

**다음 handoff**

- `E-P00-ARCH` 초안을 WP-00-5에 전달한다.

### WP-00-5 — Root verification, evidence seal과 Phase 01 handoff

**사전조건**

- WP-00-1~4 green.
- OPEN/GATED/DEFERRED 목록이 값으로 채워지지 않음.

**수정 대상**

- Digest-protected Phase 00 evidence bundle
- Phase 01 handoff manifest
- [Phase 00 review](../reviews/phase-00-review.md)는 reviewer owner가 독립 작성하며 구현 evidence review 때 현재 source snapshot과 다시 연결

**구체 작업**

1. Optional-backend-free online root `verify`와 prefetched offline root `verify`를 각각 실행한다.
2. Test/architecture/legacy/reproducible build 결과와 exact command/exit code를 저장한다.
3. `./build/verify-evidence-bundle.sh`로 모든 regular file의 canonical relative path, byte length와
   SHA-256을 stable-order manifest에 넣고 nested file 누락, symlink, same path/different bytes와 manifest
   mutation을 non-zero로 거부한다.
4. Evidence artifact를 digest로 seal하고 exact implementation source snapshot, document version,
   toolchain과 known limitation을 연결한다.
5. Reviewer가 `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`를 독립 확인한다.
6. `rpdptw-core`, exact test-fixture classifier/scope, allowed dependency와 build commands를 Phase 01 handoff로 고정한다.
7. Phase 01 문서의 canonical filename, metadata와 Phase 00 evidence dependency를 확인한다. 파일 존재만으로 Phase 01 entry가 열린 것으로 간주하지 않는다.

**검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never clean verify
./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 verify
./build/verify-reproducible-build.sh
./build/verify-evidence-bundle.sh target/phase-00-evidence
git diff --check
git status --short
```

**기대 결과**

- 모든 필수 test가 pass하고 required test가 skipped되지 않는다.
- Evidence가 exact immutable implementation source snapshot과 handoff artifact를 가리킨다.
- Phase 01은 legacy/GCP/provider dependency 없이 `rpdptw-core`에서 시작할 수 있다.

**Failure/rollback**

- 어느 required evidence가 누락돼도 `IMPLEMENTED_PENDING_EVIDENCE` 이상으로 승격하지 않는다.
- Reviewer reject 시 마지막 green work package로 돌아가며 evidence를 덮어쓰지 않고 새 digest로 만든다.

**다음 handoff**

- §14의 Phase 01 entry bundle을 전달한다.

## 10. Build pinning, deterministic/offline assumption과 quality gate

### 10.1 Pinning contract

| 대상 | Phase 00 계약 |
|---|---|
| Java language level | release 25, runtime compatibility `[25,26)` |
| Local developer selection | `.sdkmanrc`의 `25.0.3-amzn`, Maven `3.9.14` |
| Compiler/test JDK | Exact-version Maven Toolchains Plugin이 `[25,26)` 선택; vendor/version/path는 runtime evidence에 기록하고 local path는 commit 금지 |
| Maven executable | Wrapper 3.9.14 distribution URL + official SHA-256 |
| Dependencies | Parent property/`dependencyManagement`의 exact version; range/`LATEST` 금지 |
| Plugins | 모든 lifecycle/invoked plugin exact version; `requirePluginVersions` |
| Test engine | JUnit exact version과 Surefire exact version |
| Architecture engine | ArchUnit exact version, rule-set digest |
| Archive | Explicit `project.build.outputTimestamp`, stable file order, generated timestamp 제거 |
| Encoding/locale | Source/report encoding UTF-8; locale 의존 output을 test oracle로 사용하지 않음 |

Java vendor/version 전체를 public result identity로 만들지는 않는다. Build/runtime compatibility fingerprint에는 포함한다. Patch update 허용 범위는 Build owner가 evidence로 review하며 hidden auto-upgrade를 허용하지 않는다.

### 10.2 Offline의 정확한 의미

Offline build는 “처음부터 network가 전혀 필요 없다”는 주장이 아니다.

```text
wrapper distribution을 checksum 검증해 한 번 획득
→ isolated Maven repository에 모든 plugin/dependency warm-up
→ network 차단 또는 Maven -o
→ 같은 source/effective POM으로 root verify
```

Warm-up cache가 없거나 artifact가 누락되면 offline build는 typed failure여야 한다. 다른 local `~/.m2`의 우연한 content, remote fallback 또는 ignored `node_modules/.serverless`를 evidence로 사용하지 않는다.

### 10.3 Reproducible build의 정확한 의미

동일한 immutable implementation source snapshot(commit 또는 content-addressed source archive), wrapper,
effective POM, dependency bytes, JDK compatibility, output timestamp와 build command에서 publishable
JAR의 SHA-256이 같아야 한다. 두 workspace의 source manifest도 먼저 같아야 한다. Surefire XML의 elapsed
time, absolute temp path와 console ordering은 artifact digest 비교 대상에서 제외하되 evidence metadata로 남긴다.

현재 Phase 00은 cross-OS bit-for-bit 동일성을 자동 주장하지 않는다. macOS aarch64에서 같은 runtime envelope의 두 clean build를 최소 gate로 하고, CI/Linux parity는 build/release owner가 별도 evidence로 확장한다.

### 10.4 Quality gate layering

```text
Maven model/enforcer
→ compiler/package ownership
→ unit + architecture rule self-test
→ ArchUnit bytecode dependency
→ jdeps/source forbidden-reference scan
→ legacy characterization
→ root reactor integration
→ offline verify
→ two-clean-build artifact digest
```

상위 단계 실패를 하위 단계 성공으로 덮지 않는다.

## 11. 테스트 작성 수준 설계

### 11.1 Red → green 순서

1. **Red inventory:** root packaging이 implicit `jar`, wrapper와 target modules/architecture tests가 없음을 기록한다.
2. **Red guard self-test:** Forbidden dependency/internal/test-leak fixture가 rule에 의해 실제 실패하는지 먼저 만든다.
3. **Green legacy isolation:** legacy module test가 기존 observable behavior를 보존한다.
4. **Green module DAG:** target skeleton이 allowlist edge만으로 compile한다.
5. **Green architecture:** Enforcer + ArchUnit + bytecode/source scan이 실제 target bytecode에 대해 통과한다.
6. **Green root integration:** Optional-backend-free root `verify`가 통과한다.
7. **Green offline:** Controlled cache에서 `-o verify`가 통과한다.
8. **Green reproducibility:** 두 clean build artifact digest가 일치한다.
9. **Review green:** Evidence digest와 rollback/handoff가 독립 review를 통과한다.

현재 checkout에서 아래는 실패가 정상인 **FUTURE RED**다.

```bash
./mvnw --version
mvn -B -ntp -pl rpdptw/core -am verify
mvn -B -ntp -pl build/architecture-rules -am test
```

Wrapper/module/file이 아직 없기 때문이다. 이 실패를 현재 defect나 Phase evidence로 오인하지 않는다.

### 11.2 Exact test class/method 후보

| Test 종류 | Class / method 후보 | Fixture·builder·oracle | 합격 판정 |
|---|---|---|---|
| Unit | `AllowedModuleGraphTest#containsOnlyReviewedEdges()` | Immutable expected edge set | Exact edge set 일치 |
| Unit | `ForbiddenArtifactPredicateTest#rejectsProviderAndVendorGroups()` | Synthetic Maven coordinates | 모든 forbidden coordinate 검출 |
| Architecture | `StableModuleDependencyArchitectureTest#verificationDependsOnlyOnCore()` | Compiled target modules | Solver/search/cache edge 0 |
| Architecture | `ProviderAndVendorIsolationArchitectureTest#stableModulesDoNotReferenceCloudTransportOrVendorApis()` | ArchUnit imported classes + dependency tree | Forbidden ref 0 |
| Architecture | `PackageBoundaryArchitectureTest#modulesDoNotAccessAnotherModulesInternalPackages()` | Deliberate bad test fixture | Bad fixture 검출, production 0 |
| Architecture | `CustomerIsolationArchitectureTest#genericSourceContainsNoDeclaredCustomerIdentityConditional()` | Approved token manifest + AST/bytecode/source scanner | Covered unauthorized match 0 + coverage manifest |
| Contract | `ReactorTopologyTest#allExpectedModulesParticipateInRootVerify()` | Effective POM/module list | Missing/duplicate/cycle 0 |
| Contract | `PluginPinningContractTest#allResolvedBuildPluginsHaveExplicitVersions()` | Effective POM | Floating/unversioned plugin 0 |
| Contract | `JavaToolchainContractTest#compilerAndTestsSelectReviewedJava25Toolchain()` | Effective POM + toolchain display + Surefire runtime property | Compiler/test JDK가 모두 `[25,26)`, 실제 vendor/version 기록 |
| Contract | `TestScopeLeakageArchitectureTest#productionClasspathContainsNoTestFixtureArtifact()` | Runtime dependency tree | Leakage 0 |
| Contract | `TestFixtureArtifactContractTest#consumerResolvesOnlyAttachedTestsClassifier()` | Attached test-jar + consumer effective POM/tree | `tests` classifier resolve, production tree 0 |
| Integration | `MavenWrapperContractIT#usesPinnedDistributionAndChecksum()` | Wrapper properties + `--version` | Exact Maven/checksum |
| Integration | `OfflineBuildIT#prefetchedRepositorySupportsOfflineRootVerify()` | Isolated repository | Network 없이 exit 0 |
| Reproducibility | `ReproducibleArchiveIT#twoCleanBuildsProduceSameJarDigest()` | Two temporary clean workspaces | Artifact list와 SHA-256 동일 |
| Legacy contract | `AlnsBatchEngineCharacterizationTest#sameSeedRunAndIterationsProduceSameSyntheticCandidate()` | Fixed seed/run/iterations | Current map/objective 동일 |
| Legacy contract | `LegacyOptimizationContractCharacterizationTest#storesCandidatesAndResultAtCurrentObjectKeys()` | Fake storage/workflow gateway | Golden key/status/response 동일 |
| Legacy fault | `LegacyOptimizationContractCharacterizationTest#returnsCurrentRedactedFailureForStorageWorkflowAndEmptyCandidateFailures()` | Deterministic failing gateways + empty listing | Exact status/body, secret/exception leakage 0 |
| Legacy workflow | `LegacyWorkflowCharacterizationTest#dispatchesDeclaredParallelRangeThenFinalize()` | Parsed tracked YAML | Golden logical order 동일 |
| Fault | `ArchitectureRuleSelfTest#forbiddenFixtureIsRejected()` | Provider/internal/test-leak bad fixtures | 각 rule이 예상 위반 반환 |
| Fault | `ArchitectureRuleSelfTest#customerConditionalAndCapabilityAdvertisementFixturesAreRejected()` | Declared token + conditional/switch/service-registration bad fixtures | 각 covered rule이 예상 위반 반환 |
| Corruption | `EvidenceBundleIntegrityTest#digestMismatchIsRejected()` | One-byte-mutated report | Seal/read 검증 실패 |
| Security | `EvidenceRedactionTest#bundleContainsNoCredentialSecretOrRawPii()` | Known marker fixture + scanner | Marker 검출, real secret 출력 0 |
| Performance observation | `BuildFootprintObservationIT#recordsElapsedDependencyCountAndArtifactBytesWithoutThreshold()` | Full reactor metrics | 측정값과 환경 fingerprint 기록; 승인되지 않은 threshold 판정 없음 |
| Build E2E | Root `clean verify` | Full reactor | Required test skipped 0, exit 0 |

Type과 method 이름은 **PROPOSED INTERNAL**이며 review에서 바꿀 수 있다. 의미와 negative fixture coverage는 줄일 수 없다.

### 11.3 Fixture/builder/oracle

| 이름 | 목적 | 규칙 |
|---|---|---|
| `AllowedModuleGraphFixture` | Expected coordinate/edge set | Target DAG만 포함, legacy는 explicit isolated node |
| `ForbiddenProviderDependencyFixture` | Enforcer/ArchUnit negative test | Test tree 안에서만 존재, production dependency 금지 |
| `InternalPackageLeakFixture` | `.internal` cross-module failure 증명 | Expected-failure self-test |
| `TestScopeLeakFixture` | Fixture artifact의 compile leakage 검출 | Expected-failure self-test |
| `LegacyRequestFixture` | Current HTTP request/default/response | Target canonical input fixture로 재사용 금지 |
| `LegacyStorageFixture` | Candidate/result current keys와 listing | Target object-store contract로 승격 금지 |
| `EffectivePomOracle` | Module/dependency/plugin version 비교 | Wrapper로 생성한 effective POM digest 사용 |
| `ArtifactDigestOracle` | Reproducible archive 비교 | Stable artifact relative path + SHA-256 |

### 11.4 Test category 적용 범위

| Category | Phase 00 적용 |
|---|---|
| Unit | Rule predicate, module graph, pinning parser |
| Contract | Reactor, wrapper, legacy observable contract, evidence schema |
| Integration | Full Maven reactor와 legacy shaded packaging |
| E2E | Application solve E2E는 비범위. Root build/evidence lifecycle E2E만 수행 |
| Architecture | 필수; Enforcer/ArchUnit/jdeps/source scan |
| Fault | Bad POM/dependency/package fixture, offline cache 누락, interrupted evidence seal |
| Corruption | Evidence digest mutation, same identity/different bytes |
| Reproducibility | Two clean build digest와 command/toolchain identity |
| Security | Provider/secret/test fixture leakage와 evidence redaction |
| Performance | Build elapsed, dependency count, artifact size를 관측만 함. 승인된 수치 threshold가 없으므로 pass/fail 공식값을 만들지 않음 |

Solver quality, route performance, ALNS memory와 benchmark는 Phase 00에 해당하지 않는다. Build performance 수치는 관측 evidence일 뿐 `Q-BENCH-02`나 production SLA가 아니다.

## 12. Gate, evidence bundle과 Definition of Done

### 12.1 Exit gate

다음은 AND 조건이다.

```text
optional-backend-free root verify PASS
AND prefetched offline root verify PASS
AND two-clean-build artifact digest equality
AND reactor cycle = 0
AND stable provider/vendor dependency = 0
AND verification → solver/search/cache dependency = 0
AND cross-module internal access = 0
AND test fixture production leakage = 0
AND declared customer-identity/structural rule 범위의 unauthorized match = 0
AND legacy characterization PASS
AND required tests skipped = 0
AND evidence bundle digest/review PASS
```

### 12.2 Evidence bundle

| Key | 최소 내용 |
|---|---|
| `E-P00-BUILD` | Immutable implementation source snapshot, wrapper/JDK/Maven, effective POM/plugin/dependency digest, online/offline commands와 exit, source/artifact digest comparison |
| `E-P00-ARCH` | Allowed DAG, fixture classifier/scope contract, Enforcer/ArchUnit/jdeps/source scan coverage/result, negative fixture self-test, dependency trees |
| `E-P00-LEGACY` | Legacy source/deployment/resolved-dependency/shade-collision digest, golden fixture, positive/failure test list, observable behavior, known target mismatch |

공통 bundle에는 다음이 포함돼야 한다.

- Phase/document/review version과 implementation commit 또는 content-addressed source snapshot
- Exact command, working directory, toolchain, exit code
- Passed/failed/skipped test 수
- Input/config/build fingerprints
- Architecture/security 검사
- OPEN/GATED/DEFERRED와 non-applicable 이유
- Reviewer/verdict/timestamp
- Handoff artifact와 rollback point
- Canonical relative-path evidence manifest와 bundle verifier result

Console 한 줄, ignored `target/`, source file 존재 또는 이전 run 결과만으로 evidence를 만들지 않는다.

### 12.3 Definition of Done

Phase 00은 다음을 모두 만족해야만 `ACCEPTED`다.

1. Entry authority와 checkout baseline이 재현 가능하다.
2. Canonical 상세 문서와 Phase 00 review 문서가 승인됐다.
3. Legacy source/test/deployment가 explicit module/report로 보존됐다.
4. Root parent/aggregator와 target module DAG가 build된다.
5. Java/Maven/dependency/plugin/archive policy가 exact하게 pin됐다.
6. Architecture rule이 positive target과 negative fixture 양쪽에서 작동한다.
7. Online, controlled offline와 reproducible build evidence가 있다.
8. Provider/customer/vendor/verifier/test-scope 금지 경계가 자동 검증된다.
9. Phase 01 domain/normalization logic을 미리 구현하지 않았다.
10. `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`가 digest와 reviewer verdict를 가진다.
11. Phase 01 handoff와 rollback point가 명시됐다.
12. OPEN/GATED/DEFERRED 항목을 default/완료로 바꾸지 않았다.
13. Legacy dependency conflict/convergence policy가 §14 owner에게 승인되고 dependency/shade collision
    baseline이 보존됐다.

현재 이 문서 작성 시점에는 위 구현/evidence가 없으므로 Phase 00은 `NOT_STARTED / PLANNED`다.

## 13. 금지 anti-pattern

| Anti-pattern | 금지 이유 |
|---|---|
| Root parent에 Google/AWS/Azure/optimizer dependency 유지 | Stable module 전체로 volatile SDK 전파 |
| Legacy GCP module을 target adapter라고 이름만 변경 | Characterization과 target contract 혼동 |
| 빈 `CanonicalInput`, `ProblemInstance`, verifier marker 생성 | Phase 01/07 의미와 evidence 선취 |
| `always true` verifier/fake route-selection provider | Capability와 완료 상태 거짓 광고 |
| Customer마다 module/POM 생성 | Customer 수가 reactor/release를 변경 |
| `util`, `common`, `helper`, `manager`로 owner 회피 | Dependency/semantic 책임 불명확 |
| ArchUnit rule에서 legacy/전체 package를 broad exclude | 실제 target leakage 은폐 |
| Enforcer/architecture test를 profile/skip property로 기본 비활성화 | Root verify가 quality gate가 아님 |
| `LATEST`, version range, unversioned lifecycle plugin | Build 비결정성 |
| 우연한 `~/.m2` cache로 offline 성공 주장 | Clean-room 재현 불가 |
| Dirty working tree를 누락한 commit으로 clean build 두 번 실행 | 구현하지 않은 source의 재현성을 거짓 증명 |
| `shasum evidence/*`로 nested bundle seal 주장 | 하위 file 누락·directory error·same-path mutation 검출 불가 |
| Build wall-clock을 archive identity에 포함 | 동일 source digest 불일치 |
| Ignored `.serverless`, `node_modules`, `target`을 source/evidence로 사용 | Tracked authority와 재현성 상실 |
| GCP/AWS 실제 배포 성공을 Phase 00 exit로 요구 | Provider phase와 build phase 혼합 |
| `Q-BENCH-02` 수치를 POM/test default로 고정 | OPEN decision의 무단 확정 |
| Phase 13 package/OR-Tools dependency를 미리 생성 | `C-17` gate 우회 |

## 14. Blocker, deferred/open decision과 마지막 안전 지점

| 항목 | 상태 | Owner | Phase 00 영향 | 마지막 안전 지점 | 재개/해제 조건 |
|---|---|---|---|---|---|
| Scheduler task/implementation authorization | Blocker for code, not for this document | Scheduler + Build owner | 실제 POM/source 변경 시작 금지 | Commit `3424277` + 이 문서 | Task ID, scope, owner/reviewer 지정 |
| Phase 00 document review | Complete for this document revision | Architecture reviewer | 구현 acceptance를 열지 않음 | 이 문서 + [review](../reviews/phase-00-review.md) | 구현 후 evidence-bound exit review를 별도 수행 |
| Legacy Jackson convergence와 Shade collision policy | Blocker for reactor implementation | Build + Legacy characterization owner | Strict rule 전역 적용, silent dependency alignment와 broad skip 금지 | Current verbose tree + current shaded artifact/collision inventory | Target strict rule + narrow legacy-only coordinate/rule policy, compatibility test와 reviewer 승인 |
| ArchUnit/new plugin exact version | OPEN internal build choice | Build owner | Placeholder/floating version 금지 | Current pinned plugin baseline | Exact version, compatibility와 checksum/effective-POM review |
| Archive timestamp derivation | OPEN internal build choice | Build/Release | Reproducible gate seal 금지 | UTF-8/current build baseline | Explicit deterministic value/derivation ADR와 two-build proof |
| `Q-BENCH-02` official values | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 00을 막지 않음; 수치 default 금지 | No official numeric manifest | Calibration + explicit approval |
| Current Win fixture decimal `D/U` | Blocker for official fixture only | Input·Matrix + Benchmark | Phase 00을 막지 않음 | Fixture read-only | Compliant integer matrix 또는 계약 변경 승인 |
| `C-17` route pool/MIP | GATED TARGET; backend policy = direct OR-Tools CP-SAT | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain owners | Module/package/OR-Tools dependency 생성 금지 | ALNS-only DAG | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, scope와 OR-Tools version/config/native/OSS-license/SBOM/fallback 승인 |
| `Q-VAR-01` optional variants | DEFERRED | Product·Domain·Algorithm | 질문·skeleton·구현 금지 | Single-trip boundary | Variant/fixture/core-impact 승인 |
| Multi-trip/rotation | Deferred feature | Product·Domain·Algorithm | Phase 01 skeleton에 hook 미리 추가 금지 | Current single-trip contract | Trip/reset/depot/resource 계약과 승인 |
| AWS implementation/cutover | Selected target, implementation gated | Platform·Operations·Security | AWS module/IaC 생성 금지 | Provider-neutral target DAG | Phase 10/11 entry, parity/security/operations approval |
| Public Java/API/schema | OPEN | Product/API/Data owner | Phase 00 internal build contract만 허용 | No public API | Versioned contract와 compatibility/security review |
| Adjacent implementation index/Phase 01 pre-review wording | Read-only documentation consistency gap | Documentation map owner + Phase 01 reviewer | Phase 00 구현을 막지 않지만 planned/absent 표현을 현재 사실로 인용 금지 | 이 review와 Phase 01 `BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE` 상태 | README/master plan actual label과 Phase 01 inventory를 각 owner scope에서 갱신·review |

Work package별 마지막 안전 지점은 다음이다.

```text
WP-00-0: commit 3424277의 immutable inventory
WP-00-1: 이동 전 legacy source digest + golden report
WP-00-2: wrapper/parent green commit 또는 patch digest
WP-00-3: cycle-free target skeleton digest
WP-00-4: negative fixture까지 통과한 architecture policy digest
WP-00-5: reviewer가 승인한 evidence bundle digest
```

Rollback은 사용자 작업을 지우는 `git reset --hard`나 broad checkout을 사용하지 않는다. 승인되지 않은 Phase 변경만 명시적 patch/commit 단위로 되돌리고 legacy artifact와 evidence digest를 보존한다.

## 15. Phase 01 handoff

직후 Phase는 [Phase 01 — 내부 표준 입력과 정규화](phase-01-canonical-input-normalization.md)다. 최종 self-check 시 canonical 파일이 실제 존재하고 metadata가 `BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE`, `NOT_STARTED`로 Phase 00 handoff를 기다리는 것을 확인했다. 파일 존재는 entry evidence 충족이나 구현 시작을 뜻하지 않는다. Phase 01 §1/§4의 “Phase 00 review 없음” inventory 문구와 README/master plan의 `planned` label은 이 review 생성으로 stale해졌지만 다른 Phase/index는 이 reviewer의 read-only 범위다. 각 owner가 갱신하기 전에는 해당 문구를 현재 entry evidence로 인용하지 않는다.

### 15.1 Phase 01에 전달할 것

| Handoff | Phase 01이 사용하는 방법 |
|---|---|
| Accepted `rpdptw-core` module | Canonical input/normalization production code의 유일한 시작점 |
| `com.ronext.rpdptw.input/domain/normalization` package contract | DTO/domain/normalization dependency direction 유지 |
| `rpdptw-test-fixtures` attached `tests` classifier | Consumer가 `type=test-jar`, `classifier=tests`, `scope=test`로만 사용하고 production classpath와 분리 |
| `BuildPolicySnapshot` | Java 25, Maven wrapper, exact dependency/plugin/archive policy 상속 |
| `ArchitecturePolicySnapshot` | Provider/customer/vendor/internal/test leakage를 Phase 01 코드에도 즉시 적용 |
| `BuildRuntimeFingerprint` | Phase 01 evidence의 build identity |
| `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY` | Entry evidence와 migration context |
| Legacy characterization | Alias/schema authority가 아니라 compatibility inventory로만 사용 |

### 15.2 Phase 01 entry 확인

Phase 01은 다음을 모두 확인하기 전 구현을 시작하지 않는다.

1. Phase 00 상태가 reviewer에 의해 `ACCEPTED`.
2. Root online/offline `verify`와 architecture gate가 green.
3. `rpdptw-core` dependency tree에 provider/legacy/solver/application이 없음.
4. Test fixture artifact가 production scope에 없음.
5. `CanonicalInput`/normalization type이 Phase 00에서 fake로 선점되지 않음.
6. Phase 01 문서가 numeric/time/service/compatibility source와 exact error/test를 소유함.
7. `Q-BENCH-02`, `C-17`, `Q-VAR-01`이 여전히 OPEN/GATED/DEFERRED로 보존됨.

## 16. Source → requirement → test → evidence traceability

| Requirement ID | Source | Phase 00 requirement | Planned test/command | Evidence |
|---|---|---|---|---|
| `P00-REQ-AUTH` | Master §1.5/§17, Realization §2, README §3 | User-locked authority와 drift rule 고정 | Source SHA 재계산, link/section audit | `E-P00-BUILD` |
| `P00-REQ-BASELINE` | Master §1.1, Realization §3 | Placeholder를 target completion으로 오인하지 않음 | Inventory digest + legacy report | `E-P00-LEGACY` |
| `P00-REQ-DAG` | Final Architecture §2.1~§2.3, Integrated §3.3~§3.5 | Stable module DAG와 owner 분리 | `AllowedModuleGraphTest`, `dependency:tree` | `E-P00-ARCH` |
| `P00-REQ-ROOT` | Final Architecture §2.1/§2.7, Integrated §4 | Business dependency 없는 parent/aggregator | `ReactorTopologyTest`, effective POM | `E-P00-BUILD` |
| `P00-REQ-JAVA25` | Final Architecture metadata/§2.7, Integrated §4.2 | Java 25와 Maven pin | Wrapper/enforcer contract test | `E-P00-BUILD` |
| `P00-REQ-OFFLINE` | Integrated §4.2/§26.4, Realization Phase 00 | Controlled cache offline root verify | `OfflineBuildIT`, wrapper `-o verify` | `E-P00-BUILD` |
| `P00-REQ-REPRO` | Final Architecture §2.1, Integrated §4.2/§26.4, Realization Phase 00/§13 | Immutable source snapshot의 deterministic archive/build identity | `ReproducibleArchiveIT` | `E-P00-BUILD` |
| `P00-REQ-PROVIDER` | `C-20`, Final Architecture §2.2/§2.7 | Stable module provider SDK reference 0 | Enforcer + provider isolation ArchUnit/jdeps | `E-P00-ARCH` |
| `P00-REQ-VERIFY-ISO` | `C-21`, Final Architecture §2.2, Integrated §23.10 | Verification → solver/search/cache 0 | `verificationDependsOnlyOnCore()` | `E-P00-ARCH` |
| `P00-REQ-CUSTOMER` | `C-03`, Final Architecture §2.6, Integrated §3.6 | Generic customer branch/module 0 | Customer isolation source/package test | `E-P00-ARCH` |
| `P00-REQ-VENDOR` | `C-17`, Final Architecture §2.2/§4.1 | OR-Tools-free default build, no Phase 13 skeleton | Dependency Enforcer/ArchUnit + tree absence | `E-P00-ARCH` |
| `P00-REQ-TEST-SCOPE` | Final Architecture §2.7, Integrated §3.6 | Test fixture production leakage 0 | `TestScopeLeakageArchitectureTest` | `E-P00-ARCH` |
| `P00-REQ-LEGACY` | Master §16.2, Integrated §1.3/§18.2, Realization Phase 00 | Existing GCP behavior를 golden characterization | Legacy contract/workflow tests | `E-P00-LEGACY` |
| `P00-REQ-NO-DOMAIN` | Realization Phase 00/01, README §2 | Phase 01 normalization logic 선취 금지 | Production class inventory, marker/fake scan | `E-P00-ARCH` |
| `P00-REQ-HANDOFF` | Realization §6~§7, README §6 | Phase 01이 소비할 skeleton/evidence만 소유 | Handoff checklist + Phase 01 entry audit | Bundle index |

새 requirement/test/evidence를 추가하면 source와 owner를 연결한다. 이 표의 requirement를 삭제하거나 더 약한 “파일 존재” test로 바꾸지 않는다.

## 17. 작성 문서 자체 검증

이 문서 작성 작업의 self-check는 구현 evidence와 분리한다.

```bash
test -s docs/implementation/phases/phase-00-build-architecture-skeleton.md
rg -n '^## (1\. 문서 지위|2\. 목표|4\. Entry gate|5\. 현 상태|6\. 변경 대상|7\. 입력·출력|8\. Java 수준|9\. Ordered implementation|11\. 테스트|12\. Gate|13\. 금지|14\. Blocker|15\. Phase 01|16\. Source)' docs/implementation/phases/phase-00-build-architecture-skeleton.md
git diff --check -- docs/implementation/phases/phase-00-build-architecture-skeleton.md
git status --short
```

Relative link target 확인 규칙:

- Authority와 plan/README 링크는 실제 파일이어야 한다.
- Phase 00 review 링크는 actual [Phase 00 review](../reviews/phase-00-review.md)를 가리켜야 한다.
- Phase 01 link는 실제 canonical file과 title/entry metadata를 확인한다.
- `docs/codex/*`를 링크 source authority로 추가하지 않는다.
- README/master plan/Phase 01의 `planned` 또는 review 미존재 문구는 이 reviewer scope 밖의 read-only
  잔여 정합성 gap으로 §14와 review report에 기록하며 Phase 00 evidence로 인용하지 않는다.
