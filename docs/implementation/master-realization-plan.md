# RPDPTW Master Realization Plan

```yaml
document_status: DOCUMENT_SET_COMPLETE
plan_version: 1.3
baseline_date: 2026-07-28
authority_alignment_date: 2026-07-31
scope: Phase 0~14의 구현·검증·전환 계획
implementation_status: ACCEPTED_0_OF_15
source_authority: >
  USER_LOCKED for scope/map/win_poc/15-phase numbering;
  LIVE design meaning = APPROVED Master v1.1 / Domain v1.2 / Architecture v3.4
phase_c_status: COMPLETE
semantic_rebase_core3: AUTHORITY_ALIGNED_2026-07-31
core3_residual_phrasing_pass: 2026-08-01  # mermaid/P00/P02-speed/P06-steps
inventory_platform_reframe: 2026-08-01  # §3 AWS SFN+Lambda|ECS target; GCP=legacy
filename_slug_policy: KEEP_DISPLAY_SEPARATION  # README §5.1; rename only with phase body rebase
semantic_rebase_phases: NOT_DONE
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_overlay_contract_version: ALNS_FIRST_1.0
execution_success_fixture: data/win_poc_case_floor.json
execution_success_status: NOT_RUN
```

## 1. 목적과 사용 범위

이 문서는 현재 `ro-next` checkout을 canonical RPDPTW 설계로 실현하기 위한 실행 기준이다. 구현 순서, phase gate, evidence, handoff, 위험과 rollback을 하나의 15 Phase 계획으로 고정한다. 각 Phase 구현자는 이 계획과 해당 Phase 상세 문서, review 문서를 함께 사용해야 한다.

이 계획은 코드 구현 완료 보고가 아니다. 현재 존재하지 않는 module, API, test, 배포와 evidence를 완료된 것으로 간주하지 않는다. 아래의 type·interface·directory 이름은 상위 계약의 의미 경계를 구현하기 위한 **proposed internal design**이며, 승인된 public API나 wire schema가 아니다.

**권위 (2026-07-31 alignment):**

- 설계 의미의 live authority = [Master](../master-design.md) (`APPROVED` v1.1) ·
  [Domain](../domain-design.md) (`APPROVED` v1.2) ·
  [Architecture](../architecture-design.md) (`APPROVED` v3.4).
- 규범 입력 = [Phase A 인터뷰 정리](../deprecated/2026-07-30-design-interview-phase-a.md)
  (A1–A12, D1·D2).
- 사용자 선언으로 고정된 것은 **이 세트의 scope**: 15 Phase map, 파일 규칙,
  `win_poc_case_floor.json` e2e 성공 기준이다.
- `phases/*` 본문은 2026-07-26 계열 **작성 스냅샷**이며 아직 Phase B 의미 rebase를
  하지 않았다. Phase 본문과 APPROVED 설계가 충돌하면 **APPROVED가 이긴다**
  (D1·D2 충돌 문장은 폐기).
- Core 3(README / 이 계획 / progress)만 authority alignment를 반영했다.
  Phase 상세·review 재작성은 후속 세션이다.

현재 Phase/review 계약은 각 문서의 base `document_version` 또는
`UNVERSIONED_BASE`에 `ALNS_FIRST_1.0` direction overlay와
`019fa901-8776-7f61-b467-a8c6595b970d` task ID를 결합해 식별한다. 따라서 base
version이 유지된 문서도 ALNS-first revision 전 bytes/계약과 동일한 버전으로
해석하지 않는다. 이 합성 identity는 문서 provenance이며 구현/evidence/status를
승격하지 않는다.

### 1.1 사용자 고정 최종 성공 기준

이 구현 작업의 최종 성공 기준은
[win_poc_case_floor.json](../../data/win_poc_case_floor.json)을 실제 solver 실행 경로로
처리하고, 독립 검증된 결과를 생성해 사용자에게 보여주는 것이다. Source 파일·test 파일의
존재, 합성 objective, parser 성공 또는 실행 로그 한 줄은 이 기준을 충족하지 않는다.

실행 입력의 고정 계보는 다음과 같다.

```text
data/win_poc_case.json
  -- scripts/floor_win_poc_matrix.py
     D: exact decimal FLOOR → integer meter
     U: exact decimal FLOOR → integer second
     all other fields unchanged
→ data/win_poc_case_floor.json
```

| Artifact | 역할 | SHA-256 |
|---|---|---|
| `data/win_poc_case.json` | 변경하지 않는 원본 provenance | `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7` |
| `scripts/floor_win_poc_matrix.py` | 사용자 승인 `D/U FLOOR` migration | `0423e0cef4d93ed51525a8f237b48f0c680c7d6e134af1ea70ba3a48b5ed77d0` |
| `data/win_poc_case_floor.json` | 이 계획의 최종 실행 입력 | `c246abd375211877c4ec3467998651deb13fbd01768d2d1d84b8d234f5f56873` |

최종 판정은 §11.3의 AND gate를 사용한다. 이 실행 성공은 solver 구현의 실제
end-to-end acceptance다. 다만 AWS production traffic 전환, Phase 13 optional hybrid
활성화 또는 별도 공식 baseline 승인을 자동으로 의미하지는 않는다.

### 1.2 최신 implementation-direction decision — ALNS-first

2026-07-28 사용자 결정 `ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP`을 canonical 원문을
바꾸지 않는 최신 구현 순서·gate로 적용한다.

1. Phase 05가 ALNS에 필요한 stable state, pair insertion과 initial portfolio를 준비한다.
2. Phase 06이 optimizer vendor, solver license 또는 production authority 없이
   ALNS-only 경로를 구현하고 correctness·quality·performance·reproducibility 측정
   artifact를 만든다.
3. Phase 07이 ALNS candidate와 final result를 독립 검증한다.
4. Phase 08 local reference에서 검증된 ALNS-only 경로를 실행한 뒤 Phase 14A가
   승인된 corpus/protocol에 따라 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`를 발행한다.
5. Phase 13 ALNS↔MIP 연계는 이 receipt와 `C-17`의 나머지 scope/backend/운영 승인이
   모두 있을 때만 열리는 optional branch다.
6. Phase 14B는 ALNS-only 또는 별도 승인된 hybrid 중 명시적으로 선택된 manifest만
   cutover한다. Phase 13은 ALNS-only production path의 predecessor가 아니다.

이 순서는 MIP를 ALNS 구현의 선행 gate, correctness oracle 또는 필수 production
경로로 사용하지 않는다. Mixed-integer route selection은 조합 최적화 문제이므로
worst-case 난도가 높고 실무 solve time도 instance 규모, 제약·formulation,
backend/config와 hardware에 민감할 수 있다. 이는 모든 MIP가 항상 느리다는 보편
명제가 아니며, 따라서 Phase 13의 가치는 승인된 bounded experiment로만 판정한다.

수치 threshold, benchmark corpus의 최종 구성, repeat 수, resource budget와
solver/backend version은 승인 전 `OPEN — EXPERIMENT_REQUIRED` 또는 `GATED`다.
문서에 예시값이나 library default를 넣어 이 결정을 닫지 않는다.

## 2. 입력 권위와 충돌 규칙

### 2.1 고정 입력

| 우선순위/역할 | 입력 | 이 계획에서의 사용 |
|---|---|---|
| 1. 사용자 선언 | 15 Phase canonical map, 파일 규칙, `win_poc_case_floor.json` e2e 성공 기준 | 문서 세트 scope, phase numbering, 사용자 고정 실행 acceptance |
| 2. Phase A 규범 입력 | [Phase A 인터뷰 정리](../deprecated/2026-07-30-design-interview-phase-a.md) | A1–A12, D1·D2. 에이전트 신규 설계 결정 금지 |
| 3. Current Master (APPROVED v1.1) | [Master Design](../master-design.md) | 목표·범위·완료=gate+evidence·e2e·roadmap/gate 개요 |
| 4. Current Domain (APPROVED v1.2) | [Domain Design](../domain-design.md) | 값·불변조건·정규화·travel·전파·평가·해·검증·결과 **의미** |
| 5. Current Architecture (APPROVED v3.4) | [Architecture Design](../architecture-design.md) | module/package/port/runtime **배치** |
| 6. Frozen open-questions (SUPERSEDED) | [Historical open questions](../deprecated/master-design-open-questions.md) | 작성 당시 `Q-*` exact 행·evidence (historical only) |
| 7. Frozen Domain (2026-07-26, SUPERSEDED) | [Historical Domain](../deprecated/2026-07-26-domain-design.md) | `phases/*` 작성 스냅샷; **current Domain과 충돌 시 폐기** |
| 8. Frozen Architecture (2026-07-26, SUPERSEDED) | [Historical Architecture](../deprecated/2026-07-26-architecture-design.md) | 동일 (스냅샷 only) |
| 9. Frozen integrated design (SUPERSEDED) | [Historical integrated design](../deprecated/architecture-domain-implementation-design.md) | 15 Phase 번호·evidence 스키마 참고; 배치 규범 아님 |
| 10. Historical cross-check only | [2026-07-26 Master](../deprecated/2026-07-26-master-design.md) | 누락·퇴행 대조만. 결정 authority 금지 |

`docs/codex/*`는 역사/참고(repo 미존재 가능)다. 입력 권위가 아니다.  
**Phase C (O5):** 완료 — path remap + SUPERSEDED 표기.  
**Core 3 authority alignment (2026-07-31):** 본 절·§4·주요 Phase 요약 정렬.  
**`phases/*` 의미 rebase:** 미실시 — 착수 시 이 절의 우선순위 적용.

### 2.2 충돌 해소

1. 사용자 scope 선언 + Phase A (A\*/D\*) + APPROVED Master가 최우선이다.
2. Domain 의미 충돌 → **current Domain v1.2** (frozen “Final Domain”으로 해소하지 않음).
3. Module/package/runtime 배치 충돌 → **current Architecture v3.4** (frozen Architecture/integrated tree로 해소하지 않음).
4. D1·D2와 충돌하는 frozen/phase 본문 문장은 **폐기**한다 (multi-version 입력 운영, compute=Lambda 단정 등).
5. 15 Phase 번호·파일 규칙·win_poc e2e는 사용자 선언을 유지한다.
6. Proposed 내부 이름이 확정 의미와 충돌하면 이름을 버리고 의미를 보존한다.
7. Historical master / `docs/codex/*` / deprecated Q-등록부 집계는 현재 결정을 되돌릴 수 없다.

현재 확인된 문서 drift 해소:

| Drift | 적용 판단 |
|---|---|
| Frozen 문서의 `Q-INFRA-01` DEFERRED 또는 “Master가 Lambda RESOLVED” 주장 | **Master에 Q-INFRA RESOLVED 문구 없음.** 저장 = **S3 only (Architecture MUST)**. reference platform = **AWS** (S3 + **Step Functions** + **Lambda \| ECS**). worker compute 제품 선택(**Lambda vs ECS**)만 **O1 OPEN**. “Lambda only RESOLVED” 또는 GCP(Cloud Run/Workflows/GCS)를 target으로 승격 금지 |
| Frozen Architecture package tree vs Architecture v3.4 | **v3.4 §4.2 tree 적용** (`profiles/*`, `adapters/s3`, `adapters/input`, `backends/*`). 구 `object-filesystem` / `compute-aws-lambda` / adapters 안 OR-Tools 배치는 폐기 |
| AWS reference와 production cutover 혼동 | reference ≠ 구현 완료 ≠ production authority (A9, A10) |
| Route pool/MIP 상세 설계 존재 | `C-17 GATED TARGET` 유지. 구현 **proposed** backend = OR-Tools direct CP-SAT (Master/Domain 제품 확정 아님). Phase 13 entry 전 착수·기본 ON 금지. config default off ≠ C-17 승인 우회 |
| multi-version / versioned schema 운영 문구 (구 Phase 01) | **D1:** 단일 고정 canonical. 외부 → **adapter 하나**. multi-version 병행 운영 MUST NOT |
| initial portfolio “4×2 / 최대 8” MUST 표현 | Domain §10.5: **예시 / OPEN**. stage 존재·pair/XOR/COW는 MUST; 개수·휴리스틱 이름은 experiment |
| decimal Win fixture만 존재 기록 | 원본 provenance 유지 + `win_poc_case_floor.json` 실행 fixture |
| profile “no fallback” 전면 거부 | Architecture §5.3.5: 미등록 `customerId` → YAML `customers.default` **MUST**. core `switch(customerId)`·classpath first-wins는 계속 금지 |

## 3. 2026-07-28 current-state inventory

조사 기준은 branch `codex/domain-design`, commit `3424277`이다. 문서 작성 전
`git status --short`는 비어 있었다. 이 inventory는 read-only inspection 결과이며
production 배포 사실을 검증한 것이 아니다.

**Declared platform target (README · [Architecture §9.4](../architecture-design.md)):**  
이 프로젝트의 reference distribution은 **Google Cloud가 아니라 AWS**다.

| 역할 | Target (reference) | 비고 |
|---|---|---|
| 저장 | **Amazon S3 only** | DB · Redis 없음. 로컬 통합 = **LocalStack S3** |
| durable orchestration | **AWS Step Functions** | application 각본은 provider-neutral; 엔진 조립은 Phase 11 |
| worker / API compute | **AWS Lambda 또는 ECS** | **O1 OPEN** — 둘 중 하나로 단정하지 않음 |
| 런타임 이미지 | AWS Corretto 25 | `.sdkmanrc` / Dockerfile base와 정합 |

아래 표의 “현재 사실”은 그 목표 대비 **tracked tree에 실제로 있는 것**이다.
`src/**`의 Google SDK·Cloud Run 진입점과 tracked `gcp/*`는 **target 구현이 아니라
legacy placeholder**이며, Phase 00 characterization 입력으로만 보존한다.
ignored `.serverless/`·`node_modules/` 산출물은 로컬 실험 흔적일 뿐 tracked AWS
구현·배포 authority가 아니다.

### 3.1 Build와 dependency

| 항목 | 현재 사실 | AWS target과의 차이 |
|---|---|---|
| Maven | Root [pom.xml](../../pom.xml) 하나, `com.ronext:ro-next:0.1.0-SNAPSHOT` 단일 project | Parent/aggregator multi-module reactor와 architecture enforcement 없음 |
| Toolchain | `.sdkmanrc`: Corretto `25.0.3-amzn`, Maven `3.9.14` | Java 25/Corretto 기준과 일치. reproducible build evidence는 아직 없음 |
| Dependencies | root classpath에 **Google** Workflow Executions · Cloud Storage + Jackson · JUnit 직접 존재. **AWS SDK(S3 / SFN / Lambda) 없음** | Target: cloud SDK는 `adapters/*`·`deployment/` only. S3·Step Functions·Lambda/ECS adapter 미존재. core/application 격리 없음 |
| Packaging | Shade plugin이 `OptimizationHttpServer`를 main으로 한 단일 app JAR | Target: `apps/api` · `apps/worker`(Lambda zip 또는 ECS image) · CLI 등 배포 단위 분리 없음 |
| Container | [Dockerfile](../../Dockerfile)이 Maven build 후 **amazoncorretto:25** 에서 shaded JAR 실행 | 런타임 base는 AWS 정합. 그러나 이미지 내용·entrypoint는 legacy HTTP/GCP 경로용이며 Lambda/ECS reference distribution을 증명하지 않음 |
| CI/IaC | Tracked `.github` workflow, SAM/CDK/Terraform, tracked Serverless 정의 **없음** | Phase 11 AWS reference IaC·parity automation 없음 |

### 3.2 Source와 test

| 항목 | 현재 사실 | 해석 |
|---|---|---|
| Main source | `src/main/java` 6개 Java 파일 (`com.ronext.optimizer`) | HTTP adapter 5 + application placeholder 1. `com.ronext.rpdptw` target 모듈 소스 없음 |
| Platform coupling | `OptimizationApiController`가 **Google Workflows Executions** 를 기동하고, API/worker controller가 **GCS** client를 직접 생성 | Target은 **S3 URI/key + Step Functions StartExecution + Lambda/ECS handler** mapping. 현재 코드는 **GCP 경로**이며 AWS reference와 불일치 |
| Entry point | `OptimizationHttpServer` — 주석상 Cloud Run `SERVICE_MODE` (API \| worker) | Target: API Gateway + `apps/api`, worker는 Lambda handler 또는 ECS 프로세스 (`apps/worker`). compute 제품은 O1 OPEN |
| Solver | `AlnsBatchEngine`이 seed/iterations로 합성 `objective` map 생성 | 입력 parsing, RPDPTW domain, portfolio, COW ALNS, verifier 아님. 배포 흐름 검증용 stub |
| API surface | `/optimizations`, `/internal/batches`, `/internal/finalize` + `Map<String,Object>` | Characterization 대상. public target API/schema 아님 |
| Storage semantics | GCS `candidates/{requestId}/` **prefix listing** 후 raw objective 최소 선택 | Target: S3 exact-key · put-if-absent · CAS pointer · declared completeness. listing/last-write-wins champion 금지 |
| Test | `AlnsBatchEngineTest` 1개 | 합성 status/objective만 검사. phase evidence 아님 |
| Build artifact | ignored `target/` 등에 과거 test report/JAR 존재 가능 | accepted phase evidence로 사용 금지 |

### 3.3 Deployment와 운영 자료

| 항목 | 현재 사실 | 해석 |
|---|---|---|
| AWS target (선언) | [README](../../README.md): S3 입력 URI, **Step Functions** 오케스트레이션, **Lambda 또는 ECS** worker/API | 제품·문서 선언. **구현·배포 완료가 아님** |
| Tracked AWS source | SAM/CDK/Terraform, tracked `serverless.yml`, `adapters/s3`, Step Functions ASL, Lambda/ECS 조립 **없음** | Phase 11 entry 전 reference distribution 미착수 |
| Local ignored AWS 흔적 | ignored `.serverless/`(CloudFormation/state)와 `node_modules/serverless*` 가 working tree에 존재할 수 있음 | 로컬 실험 inventory. DynamoDB results table 등 **target 금지 축(DB)** 이 섞일 수 있어 authority로 쓰지 않음 |
| Legacy GCP build | tracked [gcp/cloudbuild.yaml](../../gcp/cloudbuild.yaml) | **legacy.** AWS target이 아님. characterization/migration 입력 |
| Legacy GCP orchestration | tracked [gcp/workflows/optimization.yaml](../../gcp/workflows/optimization.yaml) — Cloud Workflows parallel batch + finalize | **legacy.** target Step Functions + declared-worker coordinator 의미와 불일치 (prefix listing · raw objective) |
| Legacy GCP guide | [gcp/README.md](../../gcp/README.md) — Cloud Run + Workflows + GCS | **legacy 가이드.** 현재 배포·보안 승인 evidence 아님 |
| Data | [ro_input_json_spec.pdf](../../data/ro_input_json_spec.pdf), [win_poc_case.json](../../data/win_poc_case.json), [win_poc_case_floor.json](../../data/win_poc_case_floor.json) | PDF legacy 참고; 원본 JSON provenance/negative; FLOOR JSON = 사용자 승인 최종 실행 fixture |

### 3.4 현재 gap 요약

현재 코드는 Phase 0~14 어느 exit gate도 통과했다는 evidence bundle이 없다.
이는 “코드가 전혀 없다”가 아니라 **target phase accepted completion이 0/15**라는
뜻이다.

플랫폼 관점 요약:

1. **Target platform = AWS** — S3 + Step Functions + (Lambda \| ECS). Google Cloud
   경로를 유지·확장하는 계획이 아니다.
2. **Current tracked runtime path = GCP legacy placeholder** — GCS + Cloud Workflows +
   Cloud Run HTTP. AWS target과 **불일치**하며 Phase 00에서 characterize 후 대체한다.
3. **AWS 구현 부재** — tracked S3/SFN/Lambda/ECS adapter·IaC·parity evidence 없음.
   ignored serverless 산출물로 구현 완료를 주장하지 않는다.
4. **Lambda vs ECS** 는 O1 OPEN. inventory가 한쪽을 RESOLVED로 올리지 않는다.
5. **Orchestration 의미** 는 application/coordinator가 소유한다. Step Functions는
   durable 엔진 조립 후보이며 Domain 점수·champion·verifier를 소유하지 않는다.

특히 다음을 현재 완료로 주장하지 않는다.

- RPDPTW 단일 고정 canonical 입력, `immutable solve snapshot`, prepared travel
- Pair-aware propagation/evaluation, profile binding (A11)
- initial portfolio stage + COW ALNS (개수·operator는 OPEN)
- Candidate/result 독립 verifier와 publishable result
- Provider-neutral ports, **S3** object-storage CAS, declared-worker coordinator
- **AWS** Step Functions + Lambda/ECS reference distribution, LocalStack parity,
  provider substitution, official cutover
- Route pool/MIP 또는 official benchmark

## 4. 목표 구조와 불변 경계

### 4.1 Proposed target modules

**Authority:** [Architecture §4.2](../architecture-design.md) (v3.4).  
구 구현 세트의 `capabilities` / `object-filesystem` / `compute-aws-lambda` /
adapters 안 OR-Tools tree는 **폐기**한다.

```text
ro-next/
├── pom.xml                          # parent/reactor. cloud·ortools 공통 deps 금지
├── build/
│   ├── architecture-rules/
│   ├── test-fixtures/
│   └── port-contract-tests/         # proposed extension
├── rpdptw/
│   ├── core/
│   ├── solver/
│   ├── verification/
│   ├── application/
│   └── profiles/
│       ├── standard/
│       └── <namespace>/
├── adapters/
│   ├── common/
│   ├── input/                       # D1: external → single canonical
│   ├── s3/                          # LocalStack 동일 adapter (단위 테스트 fake 허용)
│   └── <provider>/                  # DEFERRED / approval-gated
├── backends/
│   └── route-selection-ortools-cpsat/   # OPTIONAL · C-17 GATED only
├── apps/
│   ├── cli/
│   ├── api/                         # REST 접수: validate → S3 put → 200 + s3 key
│   └── worker/                      # solve 본체 (Lambda | ECS 조립 — O1 OPEN)
└── deployment/                      # DEFERRED · IaC/배포 조립
```

이 tree는 Architecture 정합 proposed structure다. Phase 00 ADR에서 leaf 이름을
조정할 수 있으나 다음 경계는 바꿀 수 없다 (Architecture §4.3).

- OR-Tools / MIP vendor → **`backends/*` only** (adapters 금지)
- S3 SDK → **`adapters/s3` only**
- JDBC/JPA/RDB · Redis → **어디에도 MUST NOT**
- verification ↛ solver
- application은 S3/OR-Tools SDK를 직접 쓰지 않음 (port only)
- worker compute 제품(Lambda \| ECS)을 모듈명으로 단정하지 않음 (O1 OPEN)

### 4.2 Compile/runtime invariants

1. Core/solver/verification/application의 cloud SDK reference는 0이다.
2. Generic core/solver/verification의 customer-name branch는 0이다 (A11).
3. Verification은 solver/search/cache를 compile-depend하지 않는다.
4. Generic module의 `com.google.ortools` API reference는 0이며 기본 build/ALNS-only runtime은 OR-Tools/native-loader-free다. OR-Tools는 `backends/*` only.
5. Search는 `immutable solve snapshot`(정규화 문제 + prepared travel + bound profile 등 봉인 묶음)만 소비한다. 탐색이 문제/travel/profile 의미를 바꾸지 않는다.
6. 모든 stable solution은 complete pair와 route/`SearchRequestBank` exact partition(XOR)을 만족한다.
7. Search 중 lazy/reverse/symmetric travel fallback은 0이다.
8. COW `TrialDraft`만 step 안에서 mutable하며 reject/fail/cancel 시 전체 폐기한다. apply/undo 비기본.
9. 두 verifier(`candidate solution` + `result-integrity`) `PASS` 없는 result는 정상 publication/retrieval/benchmark 대상이 아니다. trial마다 verifier를 돌리지 않는다.
10. Artifact는 create-once immutable이고 authoritative state/pointer만 CAS로 전이한다. 저장 = S3 only (LocalStack 동일 adapter). DB/Redis 0.
11. Prefix listing 또는 event 도착 순서는 worker completeness와 champion authority가 아니다.
12. Provider workflow는 objective, comparator, verifier와 publication eligibility를 소유하지 않는다.
13. Retry는 logical identity, seed, warm start와 requested work를 바꾸지 않는다.
14. Open/gated/deferred 값은 hidden default로 채우지 않는다. portfolio/ALNS 수치·MIP budget 포함.
15. Raw optimizer incumbent는 fresh materialization, full evaluation과 verifier를 우회하지 않는다.
16. `servicePattern` only (`DELIVERY_ONLY` | `PICKUP_DELIVERY`). `kind=LOGICAL|REAL` 폐기.
17. `SearchRequestBank` ≠ 최종 `UNASSIGNED` (직접 dump/승격 금지).

## 5. Canonical Phase 파일명

문서 workflow 사실은 **15개 Phase 상세 문서와 15개 review 문서 완료, 구현 `ACCEPTED` 0개**다. 문서 작성·review 완료는 구현 완료나 Phase `ACCEPTED`를 뜻하지 않는다. 아래 표의 slug가 canonical이며 이후 작업은 다른 slug를 만들지 않는다.

**Filename slug 정책 (`KEEP_DISPLAY_SEPARATION`):** slug는 안정 식별자다.  
표시·계약 용어는 Domain English-first를 쓰고, slug 철자와 달라도 **일상 rename하지 않는다.**  
예: path `phase-02-…-immutable-problem.md` ↔ 의미 `immutable solve snapshot`.  
상세 규칙·rename 예외 조건은 [README §5.1](README.md#51-filename-slug-vs-domain-공식-용어-정책--keep--표시-분리).

| Phase | Canonical 상세 문서 (slug) | Canonical review 문서 |
|---:|---|---|
| 00 | [phase-00-build-architecture-skeleton.md](phases/phase-00-build-architecture-skeleton.md) | [phase-00-review.md](reviews/phase-00-review.md) |
| 01 | [phase-01-canonical-input-normalization.md](phases/phase-01-canonical-input-normalization.md) | [phase-01-review.md](reviews/phase-01-review.md) |
| 02 | [phase-02-prepared-travel-immutable-problem.md](phases/phase-02-prepared-travel-immutable-problem.md) · 표시: immutable solve snapshot | [phase-02-review.md](reviews/phase-02-review.md) |
| 03 | [phase-03-route-propagation-evaluation-kernel.md](phases/phase-03-route-propagation-evaluation-kernel.md) | [phase-03-review.md](reviews/phase-03-review.md) |
| 04 | [phase-04-capabilities-customer-profiles.md](phases/phase-04-capabilities-customer-profiles.md) | [phase-04-review.md](reviews/phase-04-review.md) |
| 05 | [phase-05-pair-insertion-initial-portfolio.md](phases/phase-05-pair-insertion-initial-portfolio.md) | [phase-05-review.md](reviews/phase-05-review.md) |
| 06 | [phase-06-cow-alns-reproducibility.md](phases/phase-06-cow-alns-reproducibility.md) | [phase-06-review.md](reviews/phase-06-review.md) |
| 07 | [phase-07-independent-verification-final-result.md](phases/phase-07-independent-verification-final-result.md) | [phase-07-review.md](reviews/phase-07-review.md) |
| 08 | [phase-08-application-ports-local-runtime.md](phases/phase-08-application-ports-local-runtime.md) | [phase-08-review.md](reviews/phase-08-review.md) |
| 09 | [phase-09-object-storage-no-database.md](phases/phase-09-object-storage-no-database.md) | [phase-09-review.md](reviews/phase-09-review.md) |
| 10 | [phase-10-provider-neutral-coordinator.md](phases/phase-10-provider-neutral-coordinator.md) | [phase-10-review.md](reviews/phase-10-review.md) |
| 11 | [phase-11-aws-reference-distribution.md](phases/phase-11-aws-reference-distribution.md) | [phase-11-review.md](reviews/phase-11-review.md) |
| 12 | [phase-12-provider-substitution.md](phases/phase-12-provider-substitution.md) | [phase-12-review.md](reviews/phase-12-review.md) |
| 13 | [phase-13-optional-hybrid-route-selection.md](phases/phase-13-optional-hybrid-route-selection.md) | [phase-13-review.md](reviews/phase-13-review.md) |
| 14 | [phase-14-official-calibration-cutover.md](phases/phase-14-official-calibration-cutover.md) | [phase-14-review.md](reviews/phase-14-review.md) |

## 6. Phase DAG, critical path와 병렬 범위

```mermaid
flowchart TD
    P00["Phase 00 Build/architecture"] --> P01["Phase 01 Canonical input/normalization"]
    P01 --> P02["Phase 02 Prepared travel/immutable solve snapshot"]
    P02 --> P03["Phase 03 Propagation/evaluation kernel"]
    P03 --> P04["Phase 04 Profiles/capabilities"]
    P04 --> P05["Phase 05 Pair insertion/portfolio"]
    P05 --> P06["Phase 06 COW ALNS/reproducibility"]
    P06 --> P07["Phase 07 Independent verification"]
    P07 --> P08["Phase 08 Ports/local runtime"]
    P08 --> P09["Phase 09 S3-only object storage"]
    P09 --> P10["Phase 10 Coordinator"]
    P10 --> P11["Phase 11 AWS reference (compute OPEN)"]
    P10 --> P12["Phase 12 Provider substitution"]
    P08 --> P14A["Phase 14A ALNS benchmark qualification — EVIDENCE GATE"]
    P07 --> P14A
    P14A --> B["ALNS_BENCHMARK_ACCEPTANCE_RECEIPT"]
    B --> P13["Phase 13 Optional hybrid/MIP — C-17 GATED"]
    P11 --> P14B["Phase 14B Official cutover — AUTHORITY GATE"]
    P14A --> P14B
    P13 -. "official hybrid manifest일 때만" .-> P14B
    Q["Q-BENCH-02 승인값 + production authority"] --> P14B
    F["win_poc_case_floor.json + both verifier PASS"] --> S["사용자 고정 실행 성공"]
    P08 --> S
```

### 6.1 Critical path

ALNS 구현·독립 검증·benchmark qualification critical path는 다음이다.

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A
```

AWS ALNS-only production 후보는 Phase 08 뒤 `09 → 10 → 11`을 진행하고,
`14A + 11 → 14B`에서 합류한다. Phase 12는 승인된 대체 provider가 있을 때 실행하는
portability branch다. Phase 13은 `14A`의 ALNS benchmark acceptance 뒤에만 열 수
있는 `C-17` optional branch다. Phase 12와 Phase 13은 ALNS-only AWS cutover의 필수
predecessor가 아니다. Official hybrid cutover를 명시적으로 선택한 경우에만 accepted
Phase 13이 Phase 14B predecessor가 된다.

### 6.2 허용되는 병렬 작업

| 선행 gate 뒤 병렬 가능 | 허용 범위 | 합류 gate |
|---|---|---|
| Phase 0 뒤 | Phase 8 port signature/test fake scaffold, Phase 7 corruption fixture 설계, profile descriptor schema 초안 | Phase 7 실제 authority와 Phase 8 local E2E 전에는 완료 주장 금지 |
| Phase 1 뒤 | Travel oracle/fixture와 domain ID/property test 준비 | Phase 2 complete travel / immutable solve snapshot gate |
| Phase 2 뒤 | Propagation hand oracle, verifier 독립 reference 계산 설계 | Phase 3/7 review |
| Phase 6 뒤 | COW profiling 계측 준비와 ALNS benchmark protocol/corpus 제안 | Phase 07 both-gate와 Phase 08 local execution 전에는 benchmark acceptance 금지 |
| Phase 8 뒤 | Phase 14A ALNS benchmark qualification, Phase 09 storage work | Phase 13은 Phase 14A acceptance receipt + `C-17` 승인 전 시작 금지 |
| Phase 8 뒤 | Filesystem storage contract와 object-common semantics | Phase 9 CAS/contract gate |
| Phase 9 뒤 | Coordinator deterministic fake와 AWS storage adapter contract | Phase 10 semantics 승인 전 AWS workflow 로직 확정 금지 |
| Phase 10 뒤 | Phase 11 AWS adapter, 승인된 경우 Phase 12 대체 provider adapter | 각 provider parity/review |

병렬 branch는 upstream semantic artifact를 임의 stub value로 확정하지 않는다. Fake/test value는 `test-only`로 주입하고 production default가 될 수 없다.

## 7. Phase별 실행·검증 계약

각 Phase 구현은 **Entry 확인 → 상세·review 기준 확인 → 구현 → 자동 검증 → pre-review evidence manifest 봉인 → 독립 review report 봉인 → post-review acceptance receipt 발행 → handoff**의 단방향 순서로 실행한다. 아래 evidence key는 미래 evidence의 요구 이름이며 현재 evidence가 존재한다는 뜻이 아니다.

### Phase 00 — Build와 architecture 뼈대

- 상세/review: [상세 문서](phases/phase-00-build-architecture-skeleton.md) / [review 문서](reviews/phase-00-review.md)
- **Authority overlay:** 모듈/의존 경계는 이 계획 [§4.1](#41-proposed-target-modules) 및
  [Architecture §4.2](../architecture-design.md) (`profiles/*`, `adapters/s3`·`input`,
  `backends/*`, apps `cli|api|worker`). Phase 본문의 구 tree
  (`capabilities`/`object-filesystem`/`compute-aws-lambda`/adapters 안 OR-Tools 등)는
  **폐기**한다. compute 제품을 Lambda로 단정하는 skeleton을 만들지 않는다 (O1 OPEN).
- 목표: **AWS target**(S3 + Step Functions + Lambda\|ECS) 기준 Architecture 정합
  multi-module reactor와 forbidden-dependency guard를 만든다. 현재 tracked tree의
  **GCP legacy placeholder**(GCS + Cloud Workflows + Cloud Run HTTP)는 삭제 전
  golden characterization 입력으로 보존한다 (target 구현으로 승격 금지).
- Entry gate: 이 계획 baseline, §3 current inventory(AWS target 선언 포함),
  source authority/conflict 규칙 승인.
- 입력: root POM/toolchain, current source/test/deployment inventory, target module DAG (§4.1).
- 산출물: Parent/aggregator, core/solver/verification/application/`profiles` skeleton,
  architecture rules, legacy(GCP) characterization, build provenance.
  (OR-Tools backend·AWS compute 제품 모듈은 optional/GATED owner Phase 전 empty 필수화 금지.
   Lambda-only skeleton으로 ECS 경로를 닫지 않는다 — O1 OPEN.)
- Exit gate: Optional-backend-free root build 성공, reactor cycle 0, forbidden
  provider/customer/backend/verifier dependency 0, legacy characterization 통과.
- Evidence/handoff: `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`; Phase 1과 모든 parallel scaffold가 소비.

실행·검증 절차:

1. Current endpoint, payload, storage key, error, **legacy GCP** workflow/GCS/Cloud Run
   과 test behavior를 golden characterization으로 기록한다. 동시에 §3의 AWS target
   (S3 / Step Functions / Lambda\|ECS)과 불일치 항목을 inventory에 남긴다.
2. Root POM을 business dependency 없는 parent/aggregator로 전환하고 Java 25/Maven/reproducible archive 정책을 중앙화한다.
3. §4.1/Architecture §4.2 정합 module skeleton과 `com.ronext.rpdptw` namespace를 만들되 기능 stub을 완료 evidence로 계산하지 않는다.
4. Enforcer/architecture/bytecode 검사로 SDK·vendor·customer·verification 역의존과
   OR-Tools outside `backends/*`, S3 SDK outside `adapters/*`, JDBC/Redis 0을 차단한다.
5. Root `mvn verify`, module DAG, dependency tree, test-scope leakage와 reproducible artifact 검사를 evidence bundle에 저장한다.

### Phase 01 — Fixed input contract + normalization (D1)

- 상세/review: [상세 문서](phases/phase-01-canonical-input-normalization.md) / [review 문서](reviews/phase-01-review.md)
- **Authority overlay:** Phase 본문에 “versioned multi-schema” 표현이 있으면 **D1이 이긴다.**
- 목표: 외부/레거시 입력을 **단일 고정 canonical 계약**과 exact normalized facts로 바꾼다.
  multi-version 입력 스키마 병행 운영 MUST NOT. 레거시 변환 = **`adapters/input` 경로의 adapter 하나**
  (공식 이름·범위 O3 OPEN). wire 메타데이터 ≠ multi-canonical 운영.
- Entry gate: Phase 00 accepted; Domain §4 fixed input contract와 alias/adapter 경계가 상세에 명시됨.
- 입력: External bytes/reference, adapter identity(optional), numeric/time/service/size/capability/zone/trip 계약.
- 산출물: Immutable canonical/normalized input, typed pre-solve errors, raw digest, adapter/coercion provenance.
- Exit gate: Decimal/overflow/alias/time/service/compatibility의 positive·negative·boundary evidence가 모두 통과.
  `reqDate` 의미 = 고객 요청 시각 (`serviceStartTime ≤ reqDate` only; `serviceEndTime` 조건 제외; Domain §4.3).
  `servicePattern` only (`DELIVERY_ONLY` | `PICKUP_DELIVERY`).
- Evidence/handoff: `E-P01-NUMERIC`, `E-P01-TIME`, `E-P01-COMPAT`, `E-P01-ERROR`; Phase 2가 소비.

실행·검증 절차:

1. 외부 DTO와 canonical domain type을 분리한다. `adapters/input` 이 정본으로 변환한다 (D1).
2. 무게·부피를 exact decimal `n=3/FLOOR`, item-first 후 qty 곱으로 checked normalization한다.
3. 비용·거리·시간 소수, 음수·비유한 값, overflow, order-level `taskTime`을 거부한다.
4. `[planStart,planEnd)`, inclusive close, repeating/overnight, full-arc work-window 의미와 service-time 조합을 정규화한다.
5. Size/`["ALL"]`/capability subset, vehicle multi-zone 허용(Domain §5.3), ownership, oneway/single-roundtrip을 property test한다. “차량 zone 1개 강제”는 Domain과 충돌하면 폐기.
6. 같은 input은 stable fingerprint를, 의미가 다른 input은 다른 fingerprint를 만드는지 검증한다.

### Phase 02 — Prepared travel + immutable solve snapshot

- 상세/review: [상세 문서](phases/phase-02-prepared-travel-immutable-problem.md) / [review 문서](reviews/phase-02-review.md)
- **Filename policy (채택 `KEEP_DISPLAY_SEPARATION`):** path slug `…-immutable-problem` 은 **안정 식별자**.  
  공식 의미 이름은 `immutable solve snapshot` ([README §5.1](README.md#51-filename-slug-vs-domain-공식-용어-정책--keep--표시-분리)).  
  slug가 Domain 의미를 정의하지 않는다. 일상 rename 없음; rename은 phase 본문 rebase와 같은 변경 단위에서만 예외.
- **Authority overlay:** 공식 용어 = `immutable solve snapshot` (Domain §7).  
  `ProblemInstance`는 snapshot **구성 요소(문제 본체)** 의 proposed 이름일 수 있으며 freeze 단위 전체가 아니다.
- 목표: 모든 directed physical-location pair와 사용 vehicle의 time을 solve 전에 완성하고
  **immutable solve snapshot** 문제 쪽을 동결한다 (이후 탐색은 해만 변경).
- Entry gate: Phase 01 accepted; approved Great Circle function/version과 typed travel source policy가 명시됨.
- 입력: Normalized locations/vehicles/requests, provided sparse `D/U`, coordinates/speed, generation policy.
- 산출물: Complete `PreparedTravel`, dense ID bijection, snapshot 구성 요소 + source/fingerprint provenance.
  (bound profile 최종 봉인 시점은 Phase 04와 정합 — Domain §7·§10).
- Exit gate: `M²` coverage, self `0/0`, asymmetric/provided/generated priority, rounding, ID/reference와 solver/verifier fingerprint equality 통과.
- Evidence/handoff: `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`; Phase 3/5/7이 소비.

실행·검증 절차:

1. Solver node와 physical location identity를 분리하고 external↔dense mapping을 검증한다.
2. Provided integer directed `D/U`를 우선하고 decimal을 거부한다.
3. Missing `D`를 approved Great Circle + meter `HALF_UP`, missing `U`를 vehicle별 `CEILING(D×3.6/speed)`로 생성한다.
4. Missing speed 기본값(예: 과거 서술 `45 km/h`)은 **official Domain MUST가 아니다.**
   승인된 experiment/test-only config 또는 별도 travel-policy 승인 기록으로만 쓰고,
   present-invalid speed는 거부한다. 숨은 production default로 승격하지 않는다.
5. Runtime lazy/reverse/symmetric fallback을 architecture test로 막는다.
6. Pair/node/location/vehicle/travel completeness와 checked range를 생성 시 검증한다.

### Phase 03 — Route propagation + evaluation kernel

- 상세/review: [상세 문서](phases/phase-03-route-propagation-evaluation-kernel.md) / [review 문서](reviews/phase-03-review.md)
- 목표: Route sequence에서 물리 fact를 cache 없이 재계산하고 hard/metric/score/objective 책임을 분리한다.
- Entry gate: Phase 02 accepted; propagation/evaluation API와 단위 선언이 상세 문서에서 review됨.
- 입력: Snapshot 문제 쪽 + `PreparedTravel`, proposed bound constraint/evaluation declarations, immutable `RoutePlan`.
- 산출물: Stateless propagator, typed infeasibility, neutral fact/metric, evaluation SPI, comparator와 cache-free reference.
- Exit gate: Hand-calculated load/time/wait/rest/stop/resource, hard-no-penalty, comparator 법칙, cache equality 통과.
  `reqDate`: `serviceStartTime ≤ reqDate` (Domain §4.3·§9). delivery-only는 `servicePattern=DELIVERY_ONLY` (가짜 pickup visit 없음).
- Evidence/handoff: `E-P03-PROPAGATION`, `E-P03-EVALUATION`, `E-P03-COMPARATOR`; Phase 4/5/7이 소비.

실행·검증 절차:

1. Terminal에서 route 끝까지 full-arc travel, arrival, wait, service, load와 resource를 순서대로 계산한다.
2. `DELIVERY_ONLY` initial load와 `PICKUP_DELIVERY` pickup/delivery delta를 혼합하고 모든 prefix capacity를 검사한다.
3. Stop/location transition, drive resource와 route operational time breakdown을 독립 보존한다.
4. Structural/hard gate → neutral metric → score → objective → comparator의 단방향 API를 강제한다.
5. Hard violation이 finite penalty/SA/comparator로 통과하지 못하게 한다.
6. Small hand oracle와 property test로 incremental/cache 결과가 full reference와 같은지 검증한다.

### Phase 04 — Profiles / capabilities (A11)

- 상세/review: [상세 문서](phases/phase-04-capabilities-customer-profiles.md) / [review 문서](reviews/phase-04-review.md)
- **Authority overlay:** 모듈 배치는 Architecture `profiles/*` + YAML 카탈로그 (§5).  
  미등록 `customerId` → `customers.default` fallback (Arch §5.3.5).  
  core `switch(customerId)` · classpath first-wins · “latest” 버전 추측 금지.
- 목표: 고객 차이를 reusable typed capability와 immutable data-driven profile/preset으로 bind한다.
- Entry gate: Phase 03 accepted; descriptor format/registry 선택은 ADR로 review되며 public wire로 오인하지 않음.
- 입력: Exact customer/profile/version/preset, approved capability registry, typed parameter/dependency/unit declaration.
- 산출물: Immutable `BoundProfile`, exact dependency closure와 fingerprint, customer authorization/binding errors.
- Exit gate: 잘못된 명시 component/unit mismatch/duplicate 거부; 미등록 customer는 default 행; profile 격리와 fingerprint 회귀 통과.
- Evidence/handoff: `E-P04-BINDING`, `E-P04-ISOLATION`, `E-P04-FACET`; Phase 5/7이 소비.

실행·검증 절차:

1. Customer별 POM/JAR 대신 `profiles/*` + YAML 카탈로그와 reusable capability registry를 구현한다 (Architecture §5).
2. Binder가 schema, capability version, parameter range, fact/metric dependency, objective direction과 `LEASE`/mandatory 계약을 pre-solve 검증한다.
3. Omitted preset/slot은 exact profile version 또는 YAML default 행 merge 규칙만 허용한다 (Arch §5.3.5).
4. 새 물리 상태만 typed facet으로 추가하고 price/label/objective 차이에 facet을 사용하지 않는다.
5. Core customer-name branch와 classpath first-wins / “latest” 추측을 architecture test로 금지한다. 미등록 customerId → `customers.default`.
6. 여러 profile을 같은 problem facts에 bind해 결과 격리와 verifier closure를 검증한다.

### Phase 05 — Pair insertion + initial portfolio

- 상세/review: [상세 문서](phases/phase-05-pair-insertion-initial-portfolio.md) / [review 문서](reviews/phase-05-review.md)
- **Authority overlay:** Domain §10.5 — portfolio **개수·휴리스틱 이름은 예시/OPEN** (구현 MUST 아님).  
  MUST = pair/XOR/`SearchRequestBank`/COW `TrialDraft` 경계 + portfolio **stage 존재**.  
  Phase 본문의 “4×2 / 최대 8”은 experiment/test 예시로만 취급한다.
- 목표: Stable route/bank partition, side-effect-free exact pair insertion과 initial portfolio stage를 만든다.
- Entry gate: Phase 04 accepted; request/route/vehicle stable total order와 (experiment) portfolio config가 명시됨.
- 입력: Immutable solve snapshot, atomic requests, prepared travel, comparator.
- 산출물: `SearchSnapshot`, `SearchRequestBank`, `TrialDraft`/COW primitive, pair editor/evaluator, initial candidates + lineage.
- Exit gate: Pair/bank property, insertion brute-force oracle, rollback/no-alias, terminal/vehicle uniqueness, candidate independence 통과.
- Evidence/handoff: `E-P05-PAIR`, `E-P05-INSERTION`, `E-P05-PORTFOLIO`; Phase 06이 소비하며 MIP/backend는 소비자나 oracle이 아님.

실행·검증 절차:

1. Request가 complete same-vehicle pair 또는 `SearchRequestBank` 중 정확히 하나에 있도록 stable invariant를 구현한다.
2. Destroy/remove/insert 실패 시 route/bank/cache/fingerprint가 원상태인 중앙 atomic editor를 만든다.
3. Cheap shortlist와 모든 합법 pickup/delivery position을 검사하는 exact evaluator를 분리한다.
4. `NEW_ROUTE`가 실제 unused concrete vehicle을 소비하게 한다.
5. Experiment config에 등록된 construction 정책을 독립 실행한다 (이름·개수는 OPEN; Phase 본문 4×2는 예시).
6. 좌표 없는 정책 등은 typed `UNAVAILABLE`로 남기고 다른 정책으로 위장하지 않는다.

### Phase 06 — 복사 후 변경 방식의 ALNS

- 상세/review: [상세 문서](phases/phase-06-cow-alns-reproducibility.md) / [review 문서](reviews/phase-06-review.md)
- **Authority overlay:** Domain §11 — operator 목록·step 수치·난수 시드는
  **OPEN/예시** (구현 MUST로 문서가 채우지 않음). MUST = pair destroy/repair,
  COW `TrialDraft`, completed-step 경계, accept/discard, reproducibility 기록,
  ALNS-only default (OR-Tools-free).
- 목표: COW 기반 pair destroy/repair, completed-step 의미, phase-1 screen과 reproducible worker run을 구현한다.
- Entry gate: Phase 05 accepted; algorithm/operator/acceptance config와 모든
  **test-only 또는 experiment** step 값이 explicit함 (official default 아님).
- 입력: Validated initial candidates, `BoundProfile`/`SolvePlan`, namespaced seed,
  screen/worker step config (**experiment envelope**).
- 산출물: Phase-1 champion, immutable current/stageBest/solveBest, committed worker candidate, termination/trace/reproducibility record.
- Exit gate: Accept/reject/fault/cancel isolation, cache equality, exact step accounting
  (요청된 envelope 기준), same-envelope trace/result fingerprint 통과.
  step 상한 수치 자체는 OPEN — exit가 특정 production `screenMaxSteps`를 확정하지 않음.
- Evidence/handoff: `E-P06-COW`, `E-P06-ALNS`, `E-P06-REPLAY`; Phase 07/08/10과 Phase 14A가 소비. Phase 13은 Phase 14A acceptance 뒤에만 조건부 소비.

실행·검증 절차:

1. Changed route와 independent bank만 first-write copy하고 current/best는 immutable snapshot 교체로 관리한다.
2. Completed step을 destroy→repair→bounded improvement→full evaluation→accept/discard→adaptive update 전체로 정의한다.
3. `INVALID_CANDIDATE`/`INTERRUPTED`가 step, reward, temperature와 adaptive state를 전진시키지 않게 한다.
4. 각 available initial candidate를 **manifest에 명시한** test/experiment
   `screenMaxSteps`(또는 동등 step budget)로 실행해 stable champion을 고른다.
   수치·이름은 OPEN; Phase 본문 예시값을 official default로 승격하지 않는다.
5. Worker single-run이 **요청된** step budget을 수행하고 watchdog/cancel/resource/platform failure를 정상 종료와 분리한다.
6. Global random, unordered reduction, completion-first winner와 clock tie-break를 제거하고 fixed-envelope repeat를 검증한다.
7. Apply/undo는 구현하지 않는다. COW 병목 evidence와 별도 변경 승인 시에만 후속 제안한다.
8. ALNS-only default build/run은 OR-Tools, MIP solver, solver license/server/token,
   native backend와 production authority 없이 Phase 07·08·14A까지 실행 가능해야 한다.
9. Benchmark용 run은 dataset/fixture, seed/repeat, hardware/runtime, timeout/resource
   envelope를 명시하지만 승인 전 값을 production default로 만들지 않는다.

### Phase 07 — Independent verification + publishable result

- 상세/review: [상세 문서](phases/phase-07-independent-verification-final-result.md) / [review 문서](reviews/phase-07-review.md)
- 목표: Solver와 compile/runtime authority가 분리된 **candidate solution verifier** +
  **result-integrity verifier**로 publication을 봉인한다 (Domain §13).
- Entry gate: Phase 06 committed candidate, Phase 02/04 authority, independent corruption oracle 준비.
- 입력: Snapshot + travel/profile declaration, candidate **전체** route/bank, finalization inputs와 proposed payload.
- 산출물: Candidate `PASS`/`FAIL`, `VerifiedSolution`, final audit/outcomes/summary, result `PASS`/`FAIL`, publishable result.
- Exit gate: Pair/travel/cache/metric/objective/outcome/audit/summary/payload corruption 거부와 both-gate publication block 통과.
  `SearchRequestBank`를 최종 UNASSIGNED로 직접 dump하지 않는다.
- Evidence/handoff: `E-P07-CANDIDATE-VERIFY`, `E-P07-AUDIT`, `E-P07-RESULT-VERIFY`; Phase 08/10/11/14A가 소비. Phase 13은 별도 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` 뒤에만 소비.

실행·검증 절차:

1. Verification module이 core만 사용하고 solver/search/cache dependency가 없음을 build로 증명한다.
2. Candidate **전체** route/bank를 prepared travel과 bound declaration에서 cache 없이 재계산한다 (변경 route만 검사 금지).
3. Candidate `PASS` 뒤에만 preliminary `ASSIGNED/UNASSIGNED` partition을 만든다.
4. Static `PROVEN` 외 모든 unassigned request를 final routes 고정 상태에서 모든 eligible vehicle/positions로 audit한다.
5. Feasible insertion 발견을 자동 적용·재탐색하지 않고 confidence를 evidence 범위로 제한한다.
6. Result-integrity verifier가 exactly-one outcome, ownership, audit completeness, summary와 payload digest를 독립 검사한다.
7. 어느 gate든 fail/incomplete이면 정상 result와 benchmark vector를 차단한다.
8. Phase 07 `PASS`는 benchmark quality/performance acceptance가 아니다. Phase 14A가
   immutable benchmark bundle과 독립 acceptance receipt를 별도로 발행해야 한다.

### Phase 08 — Application ports + local runtime

- 상세/review: [상세 문서](phases/phase-08-application-ports-local-runtime.md) / [review 문서](reviews/phase-08-review.md)
- **Authority overlay (Architecture §3.4 · §8):**
  - REST 동기 구간 = validation → `ArtifactStore` put → **200 + s3 key**. ALNS 전체 완료를 HTTP 한 요청에 두지 않음.
  - **local 통합/e2e 타깃 = LocalStack S3** + 동일 `adapters/s3`. 단위 테스트 인메모리 fake 허용.
  - 순수 filesystem E2E는 dev convenience일 수 있으나 Architecture authoritative local 통합이 아님.
- 목표: Provider-neutral use case/port와 local reference runtime을 만든다.
- Entry gate: Interface scaffold는 Phase 00 뒤 가능하지만 exit는 Phase 07 both-gate 경로가 accepted되어야 함.
- 입력: Immutable solve/result artifacts, execution identity, local/LocalStack config.
- 산출물: Inbound use cases, outbound ports, local/LocalStack adapter, deterministic local E2E.
- Exit gate: Exact-key artifact digest, state/publication CAS, cancellation 분리, both-gate success/retrieval과 rerun 통과.
- Evidence/handoff: `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`; Phase 09/10이 소비.

실행·검증 절차:

1. Proposed `AcceptSolve`/`SubmitSolve`, `PrepareSolveSnapshot`, `ExecuteWorkerRun`, `PublishVerifiedResult`, status/result use case를 application이 소유하게 한다.
2. `ArtifactStore`, `RunStateRepository`(≠ JPA/DB), `ResultPublisher`, `WorkerDispatcher` 등 port에서 provider type을 제거한다.
3. Local 단위 = fake ports; local 통합 = LocalStack S3 endpoint로 동일 adapter. CAS state와 dispatcher를 구현한다.
4. Same key/same digest 수렴과 same key/different digest conflict를 검증한다.
5. Local E2E가 두 verifier 뒤에만 success/result를 공개하고 fixed manifest에서 재현되는지 확인한다.

### Phase 09 — Object storage (S3 only, no DB / no Redis)

- 상세/review: [상세 문서](phases/phase-09-object-storage-no-database.md) / [review 문서](reviews/phase-09-review.md)
- **Authority overlay:** Architecture §3.2 — **S3 only**. 관계형 DB MUST NOT. Redis MUST NOT.  
  LocalStack = 동일 `adapters/s3` endpoint. filesystem suite는 non-authoritative 보조일 뿐 1급 저장 경로로 승격하지 않음.
- 목표: Database/Redis 없이 immutable artifact와 단일 authoritative pointer/state CAS로 저장 의미를 구현한다.
- Entry gate: Phase 08 local port semantics accepted; object canonical encoding/key-layout ADR review.
- 입력: Typed `ArtifactKey/Ref`, content digest, state version, tenant scope.
- 산출물: Object storage semantics (S3/LocalStack), exact-key retrieval, immutable artifact + mutable CAS pointer model.
- Exit gate: Put-if-absent, digest, stale-version conflict, tenant isolation, listing-free completeness, publication CAS contract 통과.
- Evidence/handoff: `E-P09-STORAGE-CONTRACT`, `E-P09-CAS`, `E-P09-TENANT`; Phase 10/11이 소비.

실행·검증 절차:

1. Logical artifact identity와 provider opaque locator를 분리한다.
2. Immutable artifact를 먼저 저장·재독해 digest 검증 후 하나의 state/pointer를 CAS commit한다.
3. Multi-object transaction, directory rename, file lock와 last-write-wins를 공통 계약에서 금지한다.
4. Declared exact key를 authority로 사용하고 prefix listing/event를 wake-up hint로만 취급한다.
5. S3와 LocalStack S3가 동일 abstract storage suite를 통과하게 한다.
6. No-DB/no-Redis 기본 query를 exact solve/submission/profile key 조회로 제한한다.

### Phase 10 — 여러 round를 조정하는 coordinator

- 상세/review: [상세 문서](phases/phase-10-provider-neutral-coordinator.md) / [review 문서](reviews/phase-10-review.md)
- 목표: Provider-neutral solve/round/worker state machine, declared completeness와 deterministic champion fan-in을 구현한다.
- Entry gate: Phase 09 storage/CAS, Phase 06 worker semantics, Phase 07 verifier, Phase 08 application ports accepted.
- 입력: Immutable execution manifest, declared assignments, exact outcome refs, state version, cancel intent.
- 산출물: Coordinator action/state machine, phase-1/round fan-out/fan-in, retry identity, finalization/publication orchestration.
- Exit gate: Legal transition, duplicate/retry/cancel, missing worker `INCOMPLETE`, completion-order independence와 publication convergence 통과.
- Evidence/handoff: `E-P10-STATE`, `E-P10-COMPLETENESS`, `E-P10-RETRY`; Phase 11/12/14가 소비.

실행·검증 절차:

1. Provider runtime이 호출하는 `AdvanceSolve`와 provider-neutral action을 application에 둔다.
2. `WorkerRunId`를 manifest/round/worker/warm-start/config에 고정하고 retry는 `AttemptId`만 바꾼다.
3. Manifest가 선언한 모든 worker exact key를 읽고 하나라도 없거나 미검증이면 round를 `INCOMPLETE`로 둔다.
4. Stable worker ordinal과 comparator/tie-break로 champion을 정하고 strict improvement만 다음 warm start로 넘긴다.
5. Duplicate success same digest는 수렴하고 different digest는 integrity failure로 만든다.
6. Cancel intent, dispatch stop와 actual worker termination을 분리한다.

### Phase 11 — AWS reference distribution (compute OPEN)

- 상세/review: [상세 문서](phases/phase-11-aws-reference-distribution.md) / [review 문서](reviews/phase-11-review.md)
- **Authority overlay (Master D2/O1, Architecture §3.3 · §9.4, README):**
  - reference platform = **AWS** (Google Cloud 경로 유지·확장이 아님).
  - **Storage MUST = S3** (LocalStack parity). DB · Redis 없음.
  - durable orchestration reference = **AWS Step Functions**.
    application/coordinator 각본은 provider-neutral; SFN은 엔진 조립이며
    Domain 점수·champion·verifier를 소유하지 않는다.
  - worker/API compute = **Lambda \| ECS — O1 OPEN**. “Lambda only RESOLVED” 금지.
  - AWS reference 조립 ≠ 알고리즘 완료 ≠ production cutover (A9, A10).
  - tracked tree의 GCP(Cloud Run/Workflows/GCS)와 ignored serverless 실험 산출물은
    target evidence가 아니다 (§3).
- 목표: 승인된 AWS **reference**(S3 + Step Functions + Lambda\|ECS) 조립이
  같은 application semantics를 보존하게 한다.
- Entry gate: Phase 10 accepted; AWS resource/IAM/network/retention/cost ADR와 non-production integration environment 승인. compute 축 제품이 아직 OPEN이면 후보 adapter를 병기한다.
- 입력: Provider-neutral actions/ports, S3 state/artifact semantics, worker assignment, orchestration command/wakeup mapping.
- 산출물: S3 adapter, (후보) orchestration/compute adapters, reference deployment 조립, parity/shadow/rollback evidence.
- Exit gate: Local(LocalStack)↔AWS semantic parity, event/error/retry/cancel mapping, S3 CAS, missing-worker block, both-gate publication, security evidence 통과.
- Evidence/handoff: `E-P11-AWS-CONTRACT`, `E-P11-PARITY`, `E-P11-SECURITY`; Phase 14가 소비.

실행·검증 절차:

1. AWS SDK/ARN/event/resource name을 adapter/deployment 밖으로 노출하지 않는다.
2. Orchestration adapter는 command, wait/wakeup, provider retry scheduling과 cancel 전달만 수행한다.
3. API/worker handler는 event를 application command로 mapping하고 domain 의미를 구현하지 않는다.
4. Platform remaining time/timeout을 algorithm 정상 종료로 변환하지 않는다.
5. Local(LocalStack)과 AWS에서 동일 logical manifest의 canonical artifact, outcome, termination과 digest를 비교한다.
6. Least privilege, tenant isolation, encryption, secret/PII redaction, failure/retry와 rollback을 rehearsal한다.

### Phase 12 — Provider substitution

- 상세/review: [상세 문서](phases/phase-12-provider-substitution.md) / [review 문서](reviews/phase-12-review.md)
- 목표: Storage/workflow/compute 축을 독립 교체할 수 있음을 contract와 승인된 provider migration으로 증명한다.
  compute 축은 처음부터 O1 OPEN이므로 “Lambda→ECS 전환”만의 전용 서술이 아니다.
- Entry gate: Phase 10 accepted; 교체 대상 provider와 adoption scope에 별도 승인. 승인 전에는 future module을 빈 skeleton으로 만들지 않음.
- 입력: Stable port contract, source/destination artifact refs, selected provider adapter, parity manifest.
- 산출물: 승인된 adapter/deployment, content-digest migration record, parity/shadow/cutover/rollback playbook.
- Exit gate: 동일 storage/execution suite, locator leakage 0, artifact digest 보존, semantic parity, security/cost/operations approval.
- Evidence/handoff: `E-P12-PROVIDER-CONTRACT`, `E-P12-MIGRATION`, `E-P12-PARITY`; 향후 provider cutover가 소비.

실행·검증 절차:

1. 변경 요구가 storage, workflow, compute 중 어느 축인지 먼저 분리한다.
2. 승인된 최소 축만 adapter로 추가한다 (예: compute Lambda↔ECS, storage S3↔다른 object store, workflow 엔진 교체).
3. Source artifact read/verify → destination put-if-absent → read-back verify → ref mapping → pointer CAS 순으로 migration한다.
4. Provider URI/locator와 runtime execution ID가 domain/result fingerprint를 바꾸지 않게 한다.
5. 동일 port suite, duplicate/retry/cancel/completeness와 local/AWS/new-provider parity를 실행한다.
6. Shadow와 recoverable rollback 뒤에만 해당 provider를 활성화한다.

### Phase 13 — Optional hybrid (C-17 GATED)

- 상세/review: [상세 문서](phases/phase-13-optional-hybrid-route-selection.md) / [review 문서](reviews/phase-13-review.md)
- 목표: Immutable evaluated route pool, exact-projectable selection과 strictly-better adoption을 optional branch로 검증한다.
- **C-17 이중층 (Domain §12):** (1) config flag default **false** — 실행 스위치.
  (2) C-17 승인·evidence — 제품 자격. config true ≠ C-17 우회.
- **Backend (implementation proposed, not Master-normative):** gate 개방 시
  Boolean route/unassigned + integer/fixed-point 목적·제약 → `com.google.ortools.sat` direct CP-SAT
  (`MPSolver` 아님). 모듈 위치 = **`backends/route-selection-ortools-cpsat`** (adapters 금지).
  selected route IDs만 반환. exact version/checksum/SBOM/native 등은 별도 승인.
- Entry gate: Phase 06/07/08 accepted, Phase 14A가 발행한 유효한
  `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, `C-17` scope 승인, OR-Tools exact
  version/checksum/config·Apache-2.0/applicable notice/SBOM·native/platform·Security·
  Operations·Cost·compute admission·fallback/rollback 승인. 하나라도 없으면 상태는
  `GATED`다.
- 입력: Cache-free evaluated ALNS routes, bound projection capability, explicit backend/budget/reproducibility config.
- 산출물: Route pool delta/snapshot, projected columns/model/warm start, provider-neutral outcome, fresh materialization, hybrid record/fallback.
- Exit gate: Deterministic pool, tiny oracle, status×incumbent, no-alias, full evaluation, incumbent preservation, adopted-only feedback와 shadow 통과.
- Evidence/handoff: `E-P13-GATE`, `E-P13-POOL`, `E-P13-SELECTION`,
  `E-P13-HYBRID`; official hybrid를 승인한 경우에만 Phase 14B가 소비.

실행·검증 절차:

1. Accepted/rejected `CompletedTrial`의 hard-feasible full-evaluated route만 immutable artifact로 수집한다.
2. Append/import를 같은 merge rule로 처리하고 safe dominance proof 없이는 route를 제거하지 않는다.
3. Exact request partition `Σa_ir x_r + u_i = 1`과 concrete vehicle `Σh_vr x_r ≤ 1`을 tiny brute-force oracle로 검증한다.
4. Non-projectable profile을 typed skip하고 hidden Big-M/surrogate를 production exact mode에 넣지 않는다.
5. Backend outcome에서 incumbent 확인 후 ID만 읽고 새 route/bank로 materialize해 full evaluate한다.
6. ALNS incumbent보다 strictly better인 candidate만 채택한다. Optional failure는 unchanged incumbent, required failure는 `INCOMPLETE`다.
7. OR-Tools-free ALNS-only default build, direct CP-SAT status×incumbent, native load/temp cleanup, OSS notice/SBOM와 reproducibility class를 검증한다.
8. 승인된 experiment가 선택할 수 있는 연계 형태는 solver-neutral route-selection
   interface, bounded route-pool/subproblem, incumbent warm start, repair 또는
   intensification 등이다. 어느 하나도 숨은 기본값이 아니며 scope approval이 정확히
   하나의 proposed mode와 fallback을 명시해야 한다.
9. Budget 초과, timeout, no incumbent, model/native failure 또는 candidate
   invalid/equal/worse에서는 ALNS incumbent fingerprint를 보존하고 typed fallback을
   기록한다.

### Phase 14 — Official calibration/cutover

- 상세/review: [상세 문서](phases/phase-14-official-calibration-cutover.md) / [review 문서](reviews/phase-14-review.md)
- 목표: `14A`에서 ALNS-only benchmark qualification을 수행하고, `14B`에서 승인된
  manifest와 production authority 아래 versioned cutover/rollback을 실행한다.
- Phase 14A entry gate: Phase 06/07/08 accepted, compliant benchmark
  dataset/fixture와 correctness oracle, 사전 등록된 protocol/acceptance criteria 승인.
  AWS/Phase 11, Phase 13, optimizer backend 또는 production authority는 요구하지 않는다.
- Phase 14B entry gate: Phase 11 accepted, 유효한 Phase 14A
  `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, `Q-BENCH-02` official value approval,
  workload/security/access/retention/retry/recovery/performance/cost와 production
  authority 승인. Official hybrid이면 accepted Phase 13도 추가로 요구한다.
- 입력: Approved manifest values, immutable fixture/profile/build/runtime/provider identities, legacy compatibility matrix, shadow results.
- 산출물: Phase 14A immutable ALNS benchmark bundle/acceptance receipt, official
  manifest/card, complete verified champion/baseline, production cutover record,
  migration digest와 rollback record.
- Phase 14A exit gate: 아래 benchmark evidence 계약, 독립 review와 acceptance receipt.
- Phase 14B exit gate: All-declared-worker normal completion/verification, exact
  comparator, identical-manifest rerun, local/AWS parity, shadow, operational rehearsal와
  explicit production approval.
- Evidence/handoff: `E-P14-ALNS-BENCHMARK`,
  `E-P14-ALNS-BENCHMARK-ACCEPTANCE`, `E-P14-CALIBRATION`,
  `E-P14-OFFICIAL-RUN`, `E-P14-CUTOVER`, `E-P14-ROLLBACK`; Phase 13은 앞의
  acceptance receipt만 소비하고 production operations는 Phase 14B evidence를 소비.

실행·검증 절차:

1. Calibration corpus/protocol을 실행하고 승인 전 결과를 experiment-only로 보존한다.
2. `screenMaxSteps`, `phase2MaxSteps`, worker count, `maxRounds`, watchdog/resource policy와 seed derivation을 승인 record에 연결한다.
3. Compliant integer travel fixture와 모든 semantic/algorithm/build/provider fingerprint를 official manifest로 고정한다.
4. 모든 declared worker가 exact work와 candidate verification을 완료한 run만 champion/benchmark에 포함한다.
5. Legacy/current와 target을 `LEGACY_ONLY`, `TARGET_ONLY`, `EQUIVALENT`, `INTENTIONAL_BREAK_REQUIRES_APPROVAL`로 비교한다.
6. Shadow, versioned endpoint/adapter, artifact/state migration, cancellation/retry/security와 rollback을 rehearsal한다.
7. 명시적 production authority 승인 후에만 pointer/traffic을 전환하고, 승인 전에는 test/staging 결과를 official/production으로 표시하지 않는다.

Phase 14A benchmark bundle은 최소 다음을 모두 포함해야 한다.

| 항목 | 필수 내용 |
|---|---|
| Dataset/fixture | Corpus ID/version, 모든 file/content digest, 생성·정규화 provenance와 split/applicability |
| Seed/repeat | Seed derivation/version, actual seed 목록, repeat 정책과 완료/누락 run accounting |
| Hardware/runtime | CPU/architecture, memory, OS/JVM/build/toolchain, thread/process와 runtime fingerprint |
| Correctness oracle | Hand/exhaustive/reference oracle identity, oracle independence/sensitivity와 expected result provenance |
| Feasibility | Candidate/result verifier report, route/bank·constraint audit와 both-gate verdict |
| Objective/quality | Bound comparator vector, approved baseline/challenger applicability와 compare-not-allowed 판정 |
| Resource budget | Timeout/watchdog, completed-step/work, CPU/memory/storage budget와 timeout/failure 분리 |
| Variance/replay | Per-run result, distribution/variance, identical-envelope replay와 reproducibility class |
| Evidence authority | Immutable manifest/result/report digests, independent review report와 post-review acceptance receipt |

Threshold, repeat count, corpus size, timeout/resource budget와 허용 variance는 현재
`OPEN — EXPERIMENT_REQUIRED`다. Benchmark/Quality owner가 protocol과 restart condition을
승인하기 전에는 Phase 14A를 `ACCEPTED`로 만들거나 Phase 13을 열 수 없다.

### Plan-final execution — `win_poc_case_floor.json`

- 목표: 사용자 승인 integer travel fixture를 실제 end-to-end 경로로 실행해 검증된 결과를 생성하고 표시한다.
- Entry gate: Phase 01~08의 local critical path가 실행 가능하고, Phase 07 candidate/result verifier가 독립적으로 동작하며, 입력·profile·algorithm config가 immutable identity로 고정됨.
- 입력: `data/win_poc_case_floor.json` exact bytes/digest, 명시적 seed/step/worker envelope와 bound profile.
- 산출물: Machine-readable result, 사람이 읽을 수 있는 summary, 실행 manifest, trace/termination, candidate/result verification report와 artifact fingerprints.
- Exit gate: §11.3의 모든 항목이 통과하고 실제 명령, exit code, result path/digest와 핵심 결과가 사용자에게 제시됨.
- 비범위: AWS production cutover, optional hybrid 활성화, 승인되지 않은 quality threshold 또는 서로 다른 manifest 간 우열 주장.

계획된 canonical 실행기는 다음 형태를 제공해야 한다. 최종 구현에서 launcher 내부
배치는 달라질 수 있지만 입력·출력·exit semantics는 유지한다.

```bash
./scripts/run_win_poc.sh data/win_poc_case_floor.json
```

## 8. 공통 테스트 전략

### 8.1 Test pyramid

```text
value/unit/property
→ module/architecture contract
→ hand oracle + cache-free equivalence
→ fault/corruption injection
→ application fake/local E2E
→ storage/workflow/compute contract
→ isolated provider integration
→ semantic parity/shadow/cutover rehearsal
```

### 8.2 필수 test 종류

| 종류 | 목적 | 완료 판정에 필요한 것 |
|---|---|---|
| Unit/boundary | Fixed-point, time/window, travel formula, typed state | Positive/negative/boundary와 overflow |
| Property | Pair/bank XOR, comparator law, ID bijection, deterministic merge | Seed와 shrink 가능한 failure record |
| Oracle | Small insertion, propagation, exact partition | Hand calculation 또는 exhaustive reference |
| Architecture | Dependency/package/vendor/customer/provider leakage | Root build에서 자동 실패 |
| Fault injection | COW exception/cancel, stale cache, CAS conflict, missing worker | Pre-state/fingerprint 보존과 typed failure |
| Corruption | Route/bank/travel/metric/outcome/audit/payload 손상 | 두 verifier가 독립적으로 거부 |
| Port contract | Filesystem/S3/향후 provider 동등성 | 같은 abstract suite와 result semantics |
| Reproducibility | Stable order/seed/step/manifest | Trace, champion, canonical result fingerprint 일치 |
| Security/operations | Tenant/IAM/secret/timeout/cancel/retry/rollback | 승인된 environment의 rehearsal record |

Test-only/experiment 수치는 fixture/config에 명시하고 이름·manifest에 `test-only` 또는 `experiment`를 포함한다. 누락된 official 값의 fallback으로 사용하지 않는다.

## 9. Evidence bundle 규칙

### 9.1 Pre-review evidence manifest

각 Phase의 자동 검증이 끝나면 review를 시작하기 **전에** pre-review evidence manifest를 content-addressed immutable artifact 또는 동등한 digest-protected local artifact로 봉인한다. 이 manifest는 당시 존재하는 immutable implementation·test evidence만 담으며, schema는 아래 allowlist로 닫혀 있다.

<!-- PRE_REVIEW_MANIFEST_ALLOWLIST_BEGIN -->
```yaml
preReviewEvidenceManifest:
  phase
  canonicalPhasePlanDigest
  reviewCriteriaDigest
  sourceCommitDigest
  inputArtifactDigests
  configProfileBuildRuntimeDigests
  commandEnvironmentToolchainExitCodeRecordDigest
  testResultAndFixtureDigests
  requiredEvidenceKeyArtifactDigests
  architectureDependencySecurityReportDigests
  openGatedDeferredSnapshotDigest
  handoffCandidateArtifactDigest
  rollbackPointDigest
```
<!-- PRE_REVIEW_MANIFEST_ALLOWLIST_END -->

`reviewCriteriaDigest`는 review 결과가 아니라 review 전에 고정된 canonical 기준 문서의 digest다. Canonical serialization이 끝난 manifest bytes의 digest를 외부 content address인 `preReviewEvidenceManifestDigest`로 계산한다. 이 digest를 manifest 본문에 self-reference로 삽입하지 않는다.

Pre-review manifest에는 reviewer identity, review verdict·timestamp, review report reference·digest, `independentReviewRef`, acceptance status 또는 acceptance receipt reference·digest를 **절대 포함하지 않는다**. Review 중이나 review 뒤에도 이 정보를 backfill하거나 manifest bytes를 변경할 수 없다.

### 9.2 Independent review report

독립 reviewer는 `preReviewEvidenceManifestDigest`로 봉인된 evidence만 입력으로 사용한다. Review 결과는 manifest를 수정하지 않고 별도의 immutable `independentReviewReport`로 봉인하며 다음을 포함한다.

1. Phase와 `preReviewEvidenceManifestDigest`.
2. 적용한 `reviewCriteriaDigest`.
3. Reviewer identity/role과 independence 확인.
4. Verdict, finding, residual blocker와 required follow-up.
5. Review timestamp와 review command/tool provenance.

Canonical review report bytes의 외부 content address를 `independentReviewReportDigest`로 계산한다. Review report는 이전 manifest digest만 참조하고 acceptance receipt를 참조하지 않는다. Verdict가 exit gate를 통과하지 못하면 acceptance receipt를 발행하지 않고 `FAILED` 또는 사유에 맞는 `BLOCKED`로 전이한다.

### 9.3 Post-review acceptance receipt

Exit gate와 독립 review가 모두 통과한 뒤에만 별도의 immutable `postReviewAcceptanceReceipt`를 발행한다. Receipt는 최소한 Phase, `preReviewEvidenceManifestDigest`, `independentReviewReportDigest`, acceptance authority, acceptance timestamp, handoff artifact digest와 rollback point digest를 포함한다. 두 선행 digest 중 하나라도 없거나 일치하지 않으면 receipt는 무효이며 `ACCEPTED` 전이를 허가하지 않는다.

단방향 provenance는 다음과 같다.

```text
immutable implementation/test evidence
  → preReviewEvidenceManifest [digest M]
  → independentReviewReport [references M, digest R]
  → postReviewAcceptanceReceipt [references M + R]
  → ACCEPTED / handoff
```

Receipt는 manifest나 review report에 다시 삽입되지 않고 두 선행 artifact를 변경하지도 않는다. 따라서 review 또는 acceptance에서 pre-review evidence로 향하는 역참조·역변경 edge는 없다.

### 9.4 금지

- Working tree의 `target/` 파일이나 console 한 줄만 evidence로 인용
- Source/test 파일 존재만으로 `DONE` 처리
- Failed/skipped test를 숨기거나 이전 run과 섞기
- 서로 다른 manifest의 최솟값을 조합해 가상 결과 생성
- Secret, raw PII 또는 credential value를 bundle에 저장
- Digest 없이 mutable “latest” 경로만 참조
- Pre-review manifest에 reviewer/review result/`independentReviewRef`/acceptance receipt를 기록하거나 review 후 backfill
- 두 선행 digest가 없는 acceptance receipt를 만들거나 receipt 없이 `ACCEPTED`로 전이
- `REVIEW`, `PASS`, `VERIFIED`, `OFFICIAL` 용어를 해당 authority 없이 사용

## 10. 상태 전이와 진행률

Phase 실행 상태는 다음을 사용한다.

```text
PLANNED
→ READY
→ IN_PROGRESS
→ IMPLEMENTED_PENDING_EVIDENCE
→ REVIEW_PENDING
→ ACCEPTED

side states:
BLOCKED
FAILED
ROLLED_BACK
GATED
DEFERRED
```

- `READY`: entry gate와 owner가 확인됨.
- `IMPLEMENTED_PENDING_EVIDENCE`: 코드가 있어도 required immutable evidence 또는 pre-review evidence manifest가 완전하게 봉인되지 않음.
- `REVIEW_PENDING`: `preReviewEvidenceManifestDigest`는 고정되었지만 독립 review report와 유효한 post-review acceptance receipt가 아직 없음.
- `ACCEPTED`: exit gate가 통과하고, 별도 acceptance receipt가 일치하는 `preReviewEvidenceManifestDigest`와 `independentReviewReportDigest`를 함께 참조함.
- `BLOCKED`: 해결 가능한 predecessor/authority/evidence가 없음.
- `GATED`: 별도 scope/activation 승인 전 시작 금지.
- `DEFERRED`: restart condition 전 질문·활성화 금지.
- `ROLLED_BACK`: accepted/cutover 결과를 승인된 이전 point로 되돌리고 원인/evidence를 남김.

구현 완료율은 `ACCEPTED phase 수 / applicable phase 수`로만 계산한다. 문서 파일 작성률, source file 수, test pass 수, 배포 성공률과 섞지 않는다. Phase 12/13처럼 조건부인 branch는 applicability 결정 전 분모에 억지로 넣거나 완료 처리하지 않는다. 실제 registry/status/result summary는 [Execution Progress and Results](execution-progress-and-results.md)에서 총괄 스케줄러만 갱신한다.

## 11. Definition of Done

### 11.1 Phase DoD

Phase 하나는 다음을 모두 만족할 때만 `ACCEPTED`다.

- Entry gate와 authority가 확인됨.
- Canonical 상세 문서와 review 문서가 존재하고 승인됨.
- 구현이 해당 scope와 금지 범위를 지킴.
- Positive, negative, boundary, fault/corruption test가 통과함.
- Architecture/security/reproducibility 요구가 적용 범위에서 통과함.
- Pre-review evidence manifest가 immutable identity와 digest를 가지며 reviewer, review result/reference 또는 acceptance 정보를 포함하지 않음.
- 독립 review report가 봉인된 pre-review manifest digest를 참조하여 exit gate를 확인함.
- Post-review acceptance receipt가 pre-review manifest digest와 review report digest를 모두 참조함.
- 다음 Phase가 소비할 handoff artifact와 rollback point가 명시됨.
- OPEN/GATED/deferred 항목을 값으로 채우지 않음.

### 11.2 System DoD

일반 ALNS-only production system은 최소 다음 AND gate를 모두 만족해야 한다.

1. Phase 00~11의 applicable critical-path Phase가 `ACCEPTED`.
2. 모든 stable state에서 complete pair, same vehicle, precedence와 route/bank XOR.
3. Input/travel/profile/config가 solve 전에 immutable fingerprint로 고정.
4. Customer policy가 profile/capability에 격리되고 core customer branch가 0.
5. Cache-free full recomputation과 search 결과 일치.
6. 정상 fixed-envelope 실행의 trace/result reproducibility.
7. Candidate와 result verifier 모두 `PASS`.
8. Declared worker completeness와 completion-order-independent champion.
9. Immutable artifact + CAS publication, idempotency/cancel/retry semantics.
10. 승인된 reference distribution(S3 + 선택된 orchestration/compute 후보)의 parity/security/operations evidence.
    compute 제품(Lambda \| ECS)은 O1 OPEN이므로 System DoD가 한쪽을 단정하지 않는다.
11. Phase 14 calibration, official manifest, shadow, rollback과 production authority 승인.

Phase 13 hybrid는 별도 applicable 결정과 `C-17` gate를 통과한 경우에만 System DoD에 추가한다.
그 경우에도 Phase 14A의 ALNS benchmark acceptance가 먼저 존재해야 하며, Phase 13
결과는 ALNS-only acceptance receipt를 소급 변경하지 않는다.

### 11.3 사용자 고정 실행 성공 DoD

이 구현 요청은 다음을 **모두** 만족할 때만 성공이다.

1. `data/win_poc_case_floor.json`의 SHA-256이 실행 manifest에 기록되고 예상 digest와 일치한다.
2. 입력의 452개 order, 31개 vehicle, 205,209개 directed matrix cell이 누락·중복 없이 해석된다.
3. 모든 provided `D/U`는 integer meter/second이며 solver와 verifier가 같은 immutable prepared travel fingerprint를 소비한다.
4. Synthetic objective가 아닌 initial portfolio와 실제 search step이 실행되고 정상 termination reason과 requested/completed work가 기록된다.
5. 모든 request가 결과에서 `ASSIGNED` 또는 `UNASSIGNED` 정확히 한 번 나타나며 route/bank partition, vehicle, capacity, time window, stop과 terminal 규칙을 만족한다.
6. Candidate solution verifier와 result-integrity verifier가 각각 독립 report로 `PASS`한다.
7. 결과에는 최소한 unassigned count, used vehicle count, total distance meter, total operational time second, route별 vehicle/order sequence와 diagnostic이 포함된다.
8. 같은 manifest를 재실행했을 때 정상 종료 trace, objective vector와 canonical result fingerprint가 일치한다.
9. 실행 명령이 exit code `0`으로 끝나고 machine-readable result와 human-readable summary의 path 및 SHA-256을 남긴다.
10. 사용자에게 실제 objective/result summary, 두 verifier verdict, termination, fingerprint와 재현 명령을 보여준다.

하나라도 실패하거나 실행되지 않았으면 상태는 `NOT_RUN`, `FAILED` 또는
`BLOCKED`이며 성공으로 표시하지 않는다. 숫자 품질 threshold는 `Q-BENCH-02` 승인
전까지 성공 gate가 아니다. 본 §11.3 성공은 §11.2의 AWS production System DoD를
자동 충족시키지 않는다.

## 12. 변경, rollback과 release 전략

### 12.1 변경 통제

문제 의미, invariant, input/output, objective, numeric/time/travel, phase gate 또는 logical responsibility를 바꾸는 변경은 다음을 같은 변경 단위에서 수행한다.

1. 영향 source/decision/phase/evidence 식별.
2. ADR 또는 질문 상태 변경과 승인 record.
3. Canonical Master/상세 설계/이 계획 및 phase 문서 영향 반영.
4. Compatibility와 migration 분류.
5. Regression/corruption/parity evidence 갱신.

이름/package 내부 변경만으로 의미를 바꾸지 않는다. Public API/schema는 별도 승인 전 `proposed` 상태를 유지한다.

### 12.2 Rollback

- Code: 마지막 accepted build/commit/digest로 되돌릴 수 있는 release artifact를 보존한다.
- State: Mutable state/pointer 하나를 CAS로 승인된 이전 immutable artifact에 재지정한다.
- Provider migration: Source artifact와 destination mapping/digest를 보존하고 pointer cutback을 rehearsal한다.
- Cutover: Versioned endpoint/adapter와 traffic/pointer rollback을 사용하며 artifact를 덮어쓰거나 삭제하지 않는다.
- Algorithm: 이전 approved manifest/profile/build fingerprint로 되돌리고 서로 다른 결과를 합성하지 않는다.
- Schema: Reader compatibility 또는 explicit migration을 사용하고 same identity/different bytes overwrite를 금지한다.

## 13. 위험, 보안, 운영, 관측과 재현성

| 영역 | 핵심 위험 | 예방/검증 |
|---|---|---|
| Semantic drift | Adapter/solver/verifier가 같은 값을 다르게 해석 | Exact policy/source fingerprint, shared immutable authority, independent corruption |
| Pair/COW | Partial pair, rollback 오염, stale cache | Central atomic editor, no-alias COW, fault injection, full recomputation |
| Customer extension | Core customer-name branch; classpath first-wins | Exact registry/binding, YAML `customers.default` (Arch §5.3.5), core branch 금지 |
| Benchmark | 다른 manifest/incomplete worker 비교 | Immutable card, all-worker gate, compare-not-allowed |
| Object storage | Listing/last-write-wins/multi-object transaction 의존 | Exact-key declared completeness, put-if-absent, single-pointer CAS |
| Provider coupling | SDK/event/locator가 core/application 의미에 침투 | Ports/adapters, dependency rules, local/provider parity |
| Security | Tenant crossing, PII/secret/log 유출, 과도한 IAM | Typed tenant key, least privilege, classification/encryption, redaction test |
| Operations | Timeout/cancel/retry가 정상 종료로 오인 | Typed state/termination, idempotent attempt identity, rehearsal |
| Observability | Elapsed/completion order가 품질 input이 됨 | Requested/completed work 분리, correlation IDs, elapsed는 metadata만 |
| Reproducibility | Global random, unordered merge, mutable latest | Namespaced seed, stable order, exact version/fingerprint, normal termination |
| Optional MIP | Unsafe dominance, raw incumbent, CP-SAT native/packaging failure | Gated pool/oracle/full evaluation, unchanged ALNS fallback, direct status×incumbent와 cleanup |
| ALNS benchmark | Corpus/seed/hardware 차이, timeout 혼합, 선택적 repeat 또는 quality cherry-pick | 사전 등록 manifest, complete run accounting, both verifier, variance/replay, immutable independent acceptance |

필수 correlation은 적용 가능한 범위에서 tenant/solve/manifest/round/worker/run/attempt/problem/travel/profile/build/termination/verifier/artifact digest를 포함한다. Raw address, full input과 secret value는 log/trace에서 제외한다.

## 14. OPEN, GATED, deferred와 restart condition

| 항목 | 상태 | Owner boundary | 막는 범위 | Restart/해제 조건 |
|---|---|---|---|---|
| `Q-BENCH-02` official 수치 | `OPEN — EXPERIMENT_REQUIRED` | Benchmark·Quality | Phase 14 official manifest/baseline/cutover | Calibration corpus와 protocol 실행, measured result review, explicit approval |
| Raw `win_poc_case.json` decimal `D/U` | `RESOLVED_FOR_PLAN_EXECUTION` | Input·Matrix + Benchmark | 원본 bytes를 직접 canonical 실행하는 경로만 차단 | 사용자 승인 script와 `win_poc_case_floor.json` digest/검증 완료; 원본은 provenance/negative fixture로 유지 |
| `win_poc_case_floor.json` 실제 solver run | `NOT_RUN` | Implementation + Verification | 이 계획의 사용자 고정 최종 성공 | Phase 01~08 local path 구현, both-verifier PASS, deterministic replay와 §11.3 결과 제시 |
| ALNS benchmark acceptance | `NOT_PRODUCED / OPEN — EXPERIMENT_REQUIRED` | Benchmark·Quality + Independent Review | Phase 13 착수와 ALNS quality/performance acceptance 주장 | Phase 06/07/08 accepted, approved corpus/protocol/criteria, complete immutable benchmark bundle, independent review와 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` |
| `C-17` route pool/MIP | `GATED TARGET`; backend policy resolved | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost owners | Phase 13 착수와 production default | 유효한 ALNS benchmark acceptance receipt, separate scope, OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/compute-admission/fallback/rollback 승인, RM-9A~C/Phase 13 evidence |
| `Q-VAR-01` optional variants | `DEFERRED` | Product·Domain·Algorithm | MDVRP/OVRP/SDVRP 질문·구현 | Variant/시점 선택, representative fixture, core-impact feasibility와 별도 승인 |
| Multi-trip/rotation | Deferred feature | Product·Domain·Algorithm | Single-trip 밖 route 의미 | Trip/reset/depot/resource 계약, pair non-crossing, example/evidence와 승인 |
| Phase 12 provider adoption | Approval-gated per provider | Platform·Operations·Security | 특정 GCS/Azure/ECS/Cloud Run/Kubernetes adapter/cutover | Workload, parity, security, retention, retry/recovery, cost와 별도 adoption 승인 |
| Phase 14 production authority | Gate | Product·Operations·Security·Release | 실제 traffic/pointer cutover | Phase 11, approved calibration/fixture, shadow/rollback, operational evidence와 explicit production approval |
| Proposed public API/schema/수치 | OPEN until separately approved | Product/API/Data owner | External compatibility 약속 | Versioned contract, compatibility/security review와 approval |
| Worker/API compute (Lambda vs ECS) | `OPEN` (Master O1 / D2) | Architecture·Platform | compute 제품 단정 | reference = AWS Lambda **또는** ECS; 한쪽 RESOLVED 서술 금지 |
| durable orchestration engine | reference = **AWS Step Functions** | Architecture·Platform | GCP Cloud Workflows를 target으로 유지 | Phase 11 SFN 조립 + local/AWS parity; application 각본은 provider-neutral |
| Initial portfolio count / ALNS step 수치 | `OPEN — EXPERIMENT_REQUIRED` (Domain §10.5·§11) | Domain·Algorithm·Benchmark | 숨은 official default | 승인된 experiment config만 |
| Adapter 공식 이름·범위 (O3) | `OPEN` | Domain·API | 외부 호환 약속 | “adapter 하나” 원칙만 고정 |
| Wire field 깊이 (O2) | `OPEN` | Domain·API | public schema 승인 | Domain 의미 목록 ≠ wire 승인 |

**저장 축:** Architecture MUST = S3 only · no DB · no Redis.  
**platform 축:** reference = **AWS** S3 + **Step Functions** + (**Lambda \| ECS**).  
GCP Cloud Run/Workflows/GCS 는 legacy placeholder (§3).  
**O1 OPEN:** Lambda **vs** ECS 제품 선택만 — “Lambda only RESOLVED” 금지.  
**historical `Q-INFRA-01`:** deprecated 등록부의 “전부 RESOLVED” 문구를 Master 결정으로
인용하지 않는다. production cutover authority는 Phase 14 gate.

## 15. Requirement/source/phase/evidence traceability

아래 evidence key는 **planned requirement**이며 완료 evidence가 아니다.  
**Normative source = current APPROVED Master/Domain/Architecture.**  
Source 열의 deprecated 링크가 남아 있으면 historical fingerprint/trace 용이며,
의미 충돌 시 current 절이 이긴다.

| Requirement | Source (normative first) | Phase | Planned evidence |
|---|---|---:|---|
| `REQ-ARCH-DAG` stable module/provider/vendor/verifier dependency | [Architecture §4](../architecture-design.md) | 00 | `E-P00-ARCH` |
| `REQ-NUMERIC` n=3/FLOOR/item-first/integer-only/checked | [Domain §5.1](../domain-design.md), historical `Q-NUM-*` | 01 | `E-P01-NUMERIC` |
| `REQ-TIME` plan/window/work/service/`reqDate` meaning | [Domain §4.3 · §5.2 · §9](../domain-design.md), historical `Q-TIME-*` | 01,03 | `E-P01-TIME`, `E-P03-PROPAGATION` |
| `REQ-COMPAT` size/capability/zone/ownership | [Domain §5.3](../domain-design.md) | 01,04 | `E-P01-COMPAT`, `E-P04-BINDING` |
| `REQ-INPUT-D1` single fixed canonical; adapter one path; no multi-version ops | [Master D1](../master-design.md), [Domain §4](../domain-design.md) | 01 | `E-P01-COMPAT`, `E-P01-ERROR` |
| `REQ-TRAVEL` complete directed prepared authority | [Domain §6](../domain-design.md), historical `Q-MTX-*` | 02 | `E-P02-TRAVEL` |
| `REQ-SNAPSHOT` immutable solve snapshot freeze | [Master §3.4 · §5](../master-design.md), [Domain §7](../domain-design.md) | 02,04 | `E-P02-PROBLEM`, `E-P04-BINDING` |
| `REQ-PAIR` same-vehicle/exactly-once/precedence/route-bank XOR | [Master A3 · §3.2](../master-design.md), [Domain §2.2–2.3](../domain-design.md), `C-06` | 02,05,07 | `E-P05-PAIR`, `E-P07-CANDIDATE-VERIFY` |
| `REQ-EVAL` hard/metric/score/objective/SolvePlan 분리 | [Domain §10](../domain-design.md), Master A11, `C-04` | 03,04 | `E-P03-EVALUATION`, `E-P04-ISOLATION` |
| `REQ-PROFILE` exact bind + YAML default fallback; no core customer branch | [Architecture §5](../architecture-design.md), [Domain §10.2](../domain-design.md) | 04 | `E-P04-BINDING` |
| `REQ-PORTFOLIO` portfolio stage exists; count/heuristic OPEN | [Domain §10.5](../domain-design.md), Master A5/A8 | 05 | `E-P05-PORTFOLIO` |
| `REQ-COW-ALNS` pair operator, TrialDraft/COW, exact step, replay | [Domain §8.4 · §11](../domain-design.md), `C-08`/`C-09`/`C-22` | 05,06 | `E-P06-COW`, `E-P06-REPLAY` |
| `REQ-RESULT` bank ≠ UNASSIGNED; final audit | [Domain §8.2 · §13](../domain-design.md), `C-15` | 07 | `E-P07-AUDIT` |
| `REQ-VERIFY` two independent verifiers + publication block | [Master §7](../master-design.md), [Domain §13](../domain-design.md), `C-21` | 07 | `E-P07-CANDIDATE-VERIFY`, `E-P07-RESULT-VERIFY` |
| `REQ-LOCAL-PORT` provider-neutral ports; LocalStack S3 e2e | [Architecture §3.4 · §6 · §8](../architecture-design.md) | 08 | `E-P08-LOCAL-E2E` |
| `REQ-FINAL-EXEC` `win_poc_case_floor.json` actual run, both-verifier PASS, replay | 사용자 고정 기준, 이 계획 §1.1/§11.3 | 01~08 | `E-WIN-POC-EXECUTION`, `E-WIN-POC-REPLAY`, `E-WIN-POC-RESULT` |
| `REQ-ALNS-BENCHMARK` MIP-independent acceptance | ALNS_FIRST decision, Master A8, 이 계획 §1.2/Phase 14A | 05~08,14A | `E-P14-ALNS-BENCHMARK`, `E-P14-ALNS-BENCHMARK-ACCEPTANCE` |
| `REQ-NODB` S3 only + exact key + CAS; no DB/Redis; listing 금지 | [Architecture §3.2](../architecture-design.md) | 09 | `E-P09-STORAGE-CONTRACT`, `E-P09-CAS` |
| `REQ-COORD` declared worker completeness/retry identity | [Architecture §3 · §6](../architecture-design.md), `C-22` | 10 | `E-P10-COMPLETENESS`, `E-P10-RETRY` |
| `REQ-AWS` AWS reference parity; storage S3; compute O1 OPEN | [Master A10 · D2](../master-design.md), [Architecture §9](../architecture-design.md) | 11 | `E-P11-AWS-CONTRACT`, `E-P11-PARITY` |
| `REQ-SUBSTITUTION` independent storage/workflow/compute replacement | [Architecture §6 · §9](../architecture-design.md) | 12 | `E-P12-PROVIDER-CONTRACT`, `E-P12-PARITY` |
| `REQ-HYBRID` ALNS receipt 뒤 pool/exact selection/full-eval fallback; backends only | [Master §6.2](../master-design.md), [Domain §12](../domain-design.md), [Architecture §9.2](../architecture-design.md), `C-17` | 13 | `E-P14-ALNS-BENCHMARK-ACCEPTANCE`, `E-P13-GATE`, `E-P13-POOL`, `E-P13-SELECTION`, `E-P13-HYBRID` |
| `REQ-OFFICIAL` approved values, comparator, all-worker official run | Master §7.3 · §8.3, `Q-BENCH-*` | 14 | `E-P14-CALIBRATION`, `E-P14-OFFICIAL-RUN` |
| `REQ-CUTOVER` versioned shadow/cutover/rollback/operations | [Master §8.4–8.5](../master-design.md), Architecture A9 | 11,14 | `E-P11-PARITY`, `E-P14-CUTOVER`, `E-P14-ROLLBACK` |

Phase 상세 작성자는 이 표의 requirement를 삭제하거나 다른 의미로 축약하지 않는다. 새로운 requirement/evidence가 필요하면 source와 owner를 연결하고 총괄 스케줄러의 registry에 반영한다.
