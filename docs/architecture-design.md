---
title: RPDPTW Architecture Design
status: APPROVED
version: 3.4
date: 2026-07-31
approved_date: 2026-07-31
owner: design
normative_input:
  - docs/deprecated/2026-07-30-design-interview-phase-a.md
  - docs/master-design.md
  - docs/domain-design.md
authority: >
  Phase A 인터뷰 정리와 APPROVED Master·Domain 이 규범이다.
  기존 Architecture(2026-07-26)·deprecated 구 architecture-design.md·explainer·implementation/*
  는 상속 후보/참고일 뿐, D1·D2 및 A1–A12·APPROVED Domain 과 충돌하면 채택하지 않는다.
  본 문서는 2026-07-31 사용자 검수 완료로 APPROVED 이다.
language: ko
identifiers: en
terminology_style: >
  주요 용어는 English first, 이어서 괄호로 쉬운 한국어.
  예: port (포트·경계 인터페이스)
inheritance:
  candidate: docs/deprecated/2026-07-26-architecture-design.md
  policy: >
    module DAG·package·port·runtime 배치를 상속 후보로 재구성.
    multi-version 입력 전제·compute=Lambda 단정·C-17 기본 ON·실험 수치 확정은 배제.
    Domain 의미(수식·invariant)는 재정의하지 않고 모듈 배치만 한다.
storage_premise: >
  관계형 DB 미사용. Redis 미사용(포기).
  실행 결과·중간 결과·artifact·run 상태·접수 input 은 S3 객체에 저장·로드 (§3.2).
  RunStateRepository 는 port 이름일 뿐 JPA/DB 전제가 아님.
local_environment: >
  로컬 통합 환경은 AWS LocalStack(S3) 으로 구성 (§3.2, §8).
  단위 테스트 인메모리 fake 허용. e2e/통합 타깃은 LocalStack S3.
section_3_policy: >
  A6: 논리 runtime 역할의 의미·개요는 Master §5.2 소유.
  Architecture §3 은 역할→module/port/adapter 배치, 저장·로컬 전제,
  REST 접수→S3 key→worker 타임라인만 둔다.
  철학·온보딩 장문은 본 문서에 두지 않는다 (2026-07-31 검수 1번 합의).
reorg_note: >
  v3.4: 내용 보존 + 중복 제거 + 구조 재정비. 신규 설계·합의 변경 없음.
  2026-08-01 platform reframe (version 유지): §3.1/§8/§9.4/§10 에서
  reference = AWS S3 + Step Functions + (Lambda|ECS), current tracked =
  GCP legacy 를 명시. O1(Lambda vs ECS) OPEN 유지. production cutover 아님.
out_of_scope:
  - Domain 수식·의미 재작성
  - Master/Domain front-matter 외 본문 재작성
  - implementation phases 재작성·코드 구현
  - C-17 구현 착수·production 기본 활성화
  - round/worker/maxSteps/MIP budget 등 실험 수치 확정
  - production cutover·provider 최종 승인
  - Phase C(구 문서 SUPERSEDED 일괄)
  - 관계형 DB·JPA·SQL 마이그레이션·Redis 를 저장 백본으로 두는 설계
---

# RPDPTW Architecture Design

## 1. 문서 지위·권위·용어

**이 장:** 문서 권위·경계·용어·current≠target.  
**이 장이 아님:** 모듈 DAG, REST 흐름, YAML 스키마 (각각 §4 · §3 · §5).

| 항목 | 내용 |
|---|---|
| **지위** | Architecture — Maven module, package, port/SPI, runtime, adapter 배치, dependency 금지선 |
| **status** | **`APPROVED`** (2026-07-31). v3.4 · 구조 재정비 후 검수 완료 (내용 보존·중복 제거, 신규 합의 없음) |
| **규범 입력 (MUST)** | Phase A · Master (`APPROVED`) · Domain (`APPROVED`) |
| **비권위 상속 후보** | `docs/deprecated/2026-07-26-architecture-design.md` 등 |

### 1.1 문서 경계 (A6) — MUST

| 문서 | 소유 |
|---|---|
| **Master** | 목표·범위·gate·e2e·roadmap 개요 |
| **Domain** | 값·수식·정규화·travel·전파·평가·해·검증·결과 **의미** |
| **Architecture (본 문서)** | module/package/port/runtime/adapter **배치**. Domain 의미를 바꾸지 않음 |

한 문서가 다른 문서의 의미를 단독으로 바꾸지 않는다.

**Architecture §3 정책 (검수 합의):** 논리 runtime 역할의 **의미**는 Master §5.2.  
본 문서 §3은 **module/port/adapter 배치 + 저장·로컬 전제 + REST 접수 타임라인**만 둔다.

### 1.2 용어 표기

- 주요 개념: **`English term (쉬운 한국어)`**  
  예: `port (포트·경계 인터페이스)`, `immutable solve snapshot (풀이용 문제 고정본)`
- 식별자·Maven 좌표·package: 영어 유지
- 규범어: **MUST / MUST NOT / GATED / DEFERRED / OPEN / 기존 유지(미재심)**
- **port / SPI / adapter** 전문 정의 → **§6.2** (한 곳). 다른 장은 한 줄+링크.

### 1.3 current ≠ target (A9) — MUST

| current (현재) | target (목표) |
|---|---|
| 단일 `pom.xml` + `com.ronext.optimizer` placeholder | multi-module + `com.ronext.rpdptw` |
| `AlnsBatchEngine` 등 합성/데모 경로 | Domain·Master 계약의 ALNS → verifier 2단 경로 |
| 문서 세트 작성 완료 | gate + evidence AND 로 정의된 완료 |

placeholder·orchestration demo·문서 완료·LocalStack 기동 ≠ 솔버 완료 evidence.  
migration 표 → **§10**.

---

## 2. 목표·범위·비범위

**이 장:** Architecture 가 맡는 목표·in/out of scope 목록.  
**이 장이 아님:** 배치 상세(→ §3–§9).

### 2.1 한 줄 목표 (Architecture 관점)

Master A1 목표 계약을 **module DAG · port · runtime 배치 · adapter seam** 에 붙인다.  
논리 runtime 역할 **의미**는 Master §5.2. Domain 의미는 재정의하지 않는다.

### 2.2 범위 (in scope)

- **저장 전제 (배치):** DB 없음 · Redis 없음 · **S3 only** (→ **§3.2**)
- **로컬 통합 (배치):** **LocalStack S3** (→ **§3.2**, **§8**)
- **제공 형태 (배치):** **REST API 접수** — 규약 스펙 호출 → validation → input S3 저장 → **HTTP 200 + s3 key**. 풀이 본체는 이후 worker (→ **§3.5**)
- Maven module tree · package 책임 · dependency 금지선 (→ **§4**)
- `port` / SPI / adapter 배치 (→ **§6**)
- Domain 개념 → 모듈 **매핑** (→ **§7**)
- local / worker / distributed **배치** (→ **§8**)
- 고객·업종 요구 확장 (YAML · SPI) (→ **§5**)
- optional MIP/hybrid 격리 (C-17, config default off) (→ **§9.2**)
- verifier 격리 (→ **§9.3**)
- AWS 등 **reference/후보** (production 승인 아님) (→ **§9.4**)
- current vs target · migration 방향 · 위험 (→ **§10**)

### 2.3 비범위 (MUST NOT as Architecture claim)

- multi-version 입력 스키마 병행 운영 (D1)
- compute = Lambda **또는** ECS **확정** (D2 → O1 OPEN)
- C-17 기본 ON, config true 로 C-17 승인 우회
- round / worker / maxSteps / MIP budget 등 실험 수치 확정
- Domain 수식·`reqDate` 의미·pair invariant 재정의
- implementation `phases/*` 재작성, production cutover 주장
- provider/product 최종 선정, IaC sizing
- **public REST/wire 필드 사전 재작성** — 스펙은 **이미 공유된 규약** 을 따름. Architecture 는 **모듈·port 배치** 만
- **관계형 DB** · JPA · SQL 마이그레이션 · **Redis** 를 저장 백본으로 두는 설계
- **논리 runtime 역할의 의미·철학·온보딩 장문** (Master §5.2·A10 소유. Arch 는 배치만)
- HTTP 한 요청 안에서 **ALNS 전체 완료** 를 전제로 하는 설계 (접수는 **검증+S3 저장 후 200**)

---

## 3. 저장·로컬·런타임 배치 · REST 접수 타임라인

**이 장 함:** 저장/로컬 전제, Master 논리 역할→module/port 배치, compute OPEN 한 줄, **REST 접수→S3 key→worker** 타임라인.  
**이 장 안 함:** “왜 세 역할인가” 철학, Domain 수식, port 용어 전문 정의(→ **§6**), 모듈 tree(→ **§4**), YAML(→ **§5**).

**역할 개요(의미):** `docs/master-design.md` **§5.2** (A10, D2).  
**e2e 단계 의미:** 같은 Master **§5**.

### 3.1 Master 역할 → 배치 (한 표)

| Master 논리 역할 | Architecture 배치 | 비고 |
|---|---|---|
| **object storage** | `ArtifactStore` · `ResultPublisher` → **`adapters/s3`** (S3) | 바이트 창고. 의미 재판정 금지 |
| **durable orchestration** | 각본: `rpdptw-application` + `RunStateRepository` port. 상태 바이트: **S3**. AWS reference 엔진: **Step Functions** (adapter/deployment) | Domain 점수 소유 아님. port 목록 → **§6.4** |
| **worker compute** | `ExecuteWorkerRun` 등 · `apps/worker` · Dispatcher adapter | **Lambda \| ECS — O1 OPEN**. 로컬 프로세스 가능 |

```text
provider / S3 / LocalStack SDK  →  adapters/* · deployment/  만
OR-Tools / MIP vendor           →  backends/*  만 (C-17 optional)
core · solver · verification    →  위 SDK 금지
```

의존 금지 상세 → **§4.3**.

### 3.2 저장 전제 · 로컬 (adapter 경계) — MUST

| 전제 | 규범 |
|---|---|
| RDB | **MUST NOT** |
| Redis | **MUST NOT** (포기) |
| 결과·중간·artifact·run 상태·**접수 input** | **S3 객체** only · port 경유 |
| 로컬 통합 S3 | **LocalStack** endpoint (같은 `adapters/s3`) |
| 단위 테스트 | 인메모리 fake 허용 |

`RunStateRepository` 는 **port 이름**일 뿐 JPA/DB 전제가 아니다. 구현은 S3 객체.  
로컬 모드 표 → **§8**. AWS 후보 표 → **§9.4**.

### 3.3 compute (D2 / O1) — OPEN

**Lambda 또는 ECS. 미결정.** 당장 확정 불필요. 한쪽 단정 **MUST NOT**.

### 3.4 REST 접수 → S3 key → worker 타임라인 — MUST

**public REST 필드 사전** 은 이 문서가 다시 쓰지 않는다. **이미 공유된 규약** 을 전제로, **모듈·흐름 배치만** 한다.

#### “동기”의 의미 (오해 방지)

| 구분 | 이 제품 |
|---|---|
| **HTTP 요청 한 번** | 규약 input **검증** → 문제 없으면 **S3 에 input 저장** → **200** + **s3 key** 까지가 **동기** |
| **경로 탐색(ALNS 등) 완료** | **같은 HTTP 요청 안에서 끝내지 않음.** 이후 worker/배치가 진행. 호출 측은 **받은 s3 key** 로 진행 확인 |

즉 “REST 동기 API” = **접수·검증·저장·키 반환이 한 요청에서 끝남**.  
“솔버 전부 동기 완료” 가 **아님**.

#### 호출 측 · 이 프로젝트 역할

```text
[호출 시스템]
  · 이미 공유된 REST 스펙 / input 작성 규약 대로 요청
           │
           ▼
[ apps/api + application ]   ← 이 레포가 하는 일 (접수 구간)
  1) 스펙·스키마에 맞게 왔는지 확인 (validation)
  2) 문제 있으면 4xx 등 (규약에 따름) — S3 저장 안 함
  3) 문제 없으면:
       · plan id + customer id 등으로 **S3 object key** 생성
       · input 바이트를 S3 에 put  (ArtifactStore / 입력 전용 저장)
  4) HTTP 200 + body 에 **s3 key** (및 규약이 정한 필드)
           │
           ▼
[호출 시스템]
  · 응답의 s3 key 를 보관
  · 이후 그 key(또는 key prefix) 로 **진행 과정** 확인
      - S3 객체를 직접 읽거나
      - (있으면) 조회용 API 에 key 를 넘겨 상태 조회
```

#### S3 key

| 항목 | 규범 |
|---|---|
| 재료 | 최소 **plan id(또는 solve/plan 식별자)** + **customer id** 조합 (규약·구현이 정한 추가 토큰 가능) |
| 역할 | 이후 진행·결과 artifact 의 **공통 루트/식별** |
| 생성 위치 | application 각본 (또는 그 use case). **key 규칙 하드코딩을 controller 에만 두지 말 것** — 테스트·worker 와 공유 |
| 저장 내용 (접수 시점) | 검증 통과한 **input** (및 규약이 요구하는 메타) |

exact key 문자열 템플릿·버킷명은 **implementation / 공유 스펙**. Architecture 는 **조합 재료와 port 경유** 만 고정.

#### 모듈 매핑 (접수 HTTP)

| 단계 | 모듈 · port |
|---|---|
| HTTP 수신 · 스펙 라우팅 | `apps/api` |
| body → 검증용 모델 | apps 또는 `adapters/input` (규약 DTO) |
| validation | application use case (`AcceptSolve` / `SubmitSolve` 접수 단계) — 스키마·필수값·(Domain 가능하면) 정본 전 검사 |
| 정본 변환이 필요하면 | `adapters/input` → canonical (D1: 정본 하나) |
| S3 put | **port.out** `ArtifactStore` → **`adapters/s3`** |
| 200 + s3 key | `apps/api` 가 use case 결과를 HTTP 로 매핑 |
| 이후 탐색 | `apps/worker` (또는 동등) 가 같은 key 의 input 을 읽어 `ExecuteWorkerRun` … — **접수 HTTP 와 분리** |
| 진행 확인 | 호출 측이 **s3 key** 로 상태/중간/결과 객체 조회. (선택) `GetSolveStatus` 가 key 를 받아 S3 를 읽도록 제공 가능 |

#### 풀이 전체 타임라인 (한 그림)

```text
[동기 — REST 한 요청]
  규약 input
    → validation
    → S3 에 input 저장 (key = f(planId, customerId, …))
    → 200 + s3Key

[비동기 — 같은 key 로 이어짐]
  worker 가 s3Key 의 input 로드
    → (필요 시) 정본·BoundProfile
    → solver 탐색 → verification
    → 진행/결과를 같은 key 체계 아래 S3 에 갱신
  호출 측이 s3Key 로 진행 확인
```

#### 이 흐름이 문서 전체를 뒤집지 않는 이유

| 그대로인 것 | 이유 |
|---|---|
| §4 DAG · verification ↛ solver | 탐색·검증은 접수 이후 |
| S3 only · no DB/Redis (§3.2) | 접수 input·진행도 S3 |
| §5 YAML 고객 규칙 | 탐색 시 BoundProfile — 접수 200 과는 별 단계 |
| port / adapter (§6) | API 는 port 를 부르고, S3 는 adapter |

**추가·명시되는 것:** `apps/api` 의 **동기 접수 계약** + **s3 key 가 진행 핸들** 이라는 점.

port 창구 이름 목록 → **§6.4**. REST↔port 짧은 매핑 → **§6.3**.

### 3.5 이 장에서 의도적으로 뺀 것

- 온보딩 비유·“왜 세 역할” 서술 (Master/발표 노트)
- orchestration 타임라인·상태 enum 장문 (창구 목록은 §6.4)
- workflow 엔진 선정 강의
- port / SPI / adapter 용어 전문 정의 (→ **§6.2**)

---

## 4. Module / package DAG · dependency 금지선

**이 장:** **모듈 의미 + 의존 방향** 을 먼저 보고, 그다음 **디렉터리(tree)** · 금지선 · 배포 경계 · build 검사.  
**이 장이 아님:** Domain 수식, 고객 특화 점수(→ **§5**), Lambda/ECS 제품 확정(O1 OPEN), REST 타임라인(→ **§3.4**).

**합의 전제 (한 줄):** 저장 → **§3.2**. OR-Tools = optional backends. verification ↛ solver. core 고객명 분기 금지.

**읽기 순서:** **4.1 의미·DAG → 4.2 트리** → 4.3 이하.

### 4.1 모듈 의미 · 의존 방향 (DAG)

폴더 경로보다 **각 모듈이 무엇을 하는지** 와 **누가 누구에게 기대도 되는지** 를 먼저 둔다.  
`A → B` = A 의 pom 이 B 를 compile 의존한다.

#### 4.1.1 각 모듈의 의미

| 모듈 | 의미 (한 줄) | 하지 않는 일 · pom 에 두면 안 되는 것 |
|---|---|---|
| **core** | 문제 **의미 kernel** — 정본 입력, 정규화, travel, 전파, 평가 SPI 구멍 | I/O·클라우드·OR-Tools·DB/Redis; solver/verification 을 모름 |
| **solver** | **후보 해 탐색** (ALNS 등). 휴리스틱·캐시 가능 | 최종 “발행해도 된다” 권위 없음; verification·고객 분기·vendor type |
| **verification** | **독립 검증** — cache 없이 core 규칙으로 다시 계산 | **solver 의존 금지**; 솔버 캐시를 권위로 믿음 |
| **application** | **각본** — use case, port, snapshot 동결, 탐색→검증 순서 | S3/OR-Tools SDK 직접 (port 만) |
| **profiles/\*** | 고객·업종 **정책** — 제약·점수·목적 SPI 구현 (§5) | solver search 내부 조작; core 에 고객명 분기 역유입 |
| **adapters/** | **I/O·연동** — 입력→정본, S3/LocalStack, (나중) provider | Domain 점수 재판정; kernel 에 SDK 침투 |
| **backends/** | **선택적 계산 벤더** (OR-Tools 등 경로 선택) | 기본 빌드 필수화; verification 의존; adapters 자리 아님 (B1) |
| **apps/\*** | **진입점·배포 조립** (cli/api/worker) | 금지선 우회 import |
| **build/** | 아키텍처 규칙·테스트 fixture | 비즈니스 로직 |

**adapters vs backends (B1)**  
- adapters = 바깥 세상과의 **연결** (바이트·포맷)  
- backends = 탐색을 돕는 **선택 알고리즘 엔진** (기본 경로에 없어도 됨)

**라이브러리가 붙는 위치**

```text
AWS S3 / LocalStack  →  adapters/* 만
OR-Tools 등          →  backends/* 만 (선택 조립)
JDBC · Redis         →  어디에도 MUST NOT   (저장 전제 → §3.2)
core·solver·verification·application  →  위 SDK MUST NOT
```

#### 4.1.2 의존 그림

```text
                 rpdptw-core
                ╱    │     ╲
        solver     verification   profile-*
           │           │            │
           └─────┬─────┘            │
                 ▼                  │
          application ◄─────────────┘
                 │
            adapters/*  →  apps/*
                 │
    (선택) backends/* → core, solver   # ↛ verification
```

| From | → 허용 | ↛ 금지 (핵심) |
|---|---|---|
| core | (없음) | 모든 바깥 모듈 |
| solver | core | verification, app, profile 구현, vendor type |
| verification | core | **solver** |
| profile-\* | core | search internal |
| application | core, solver, verification | S3/OR-Tools SDK |
| adapters | application port, 필요 시 core 공개 타입 | solver 내부 |
| backends | core, solver | verification |
| apps | application + 고른 adapters/profiles (+ 선택 backends) | kernel 규칙 우회 |

**세 줄**

1. **안쪽으로만** 기대기 (core 가 가장 안쪽).  
2. **verification 은 solver 를 모른다** — 독립 검증.  
3. **application 만** solver 와 verification 을 순서대로 묶는다.

**런타임 호출 ≠ Maven 의존**

```text
apps → application → core / solver / verification / (adapters로 I/O)
```

zip 안에 solver.jar 와 verification.jar 가 **나란히** 있어도 된다. 금지는 **import·pom 방향** 이다.

**기본 경로 vs 선택 경로**

| | 모듈 |
|---|---|
| 기본 | core + solver + verification + application + adapters (backends 없음) |
| 선택 hybrid | + `backends/*` 를 apps 가 조립할 때만. 설정 default off. 엔진 결과 직발행 금지 |

### 4.2 모듈 트리

§4.1 의 모듈을 **디렉터리로 펼친 배치** 다. 의미·의존은 4.1 을 본다.

모듈을 나누는 이유(O6) — Domain 클래스마다 jar 를 만들지 **않는다**. 아래 중 하나가 필요할 때만 모듈화한다.

| 기준 | 예 |
|---|---|
| compile 의존 차단 | verification ↛ solver |
| 무거운 SDK 격리 | S3 SDK → `adapters/s3`; OR-Tools → `backends/*` |
| optional 수명주기 | 선택 hybrid 백엔드 제외 가능 |
| 독립 조립 | `profiles/*`, `apps/*` |

```text
ro-next/
├── pom.xml                          # parent/reactor. cloud·ortools 공통 dependencies 금지
├── build/
│   ├── architecture-rules/
│   └── test-fixtures/
├── rpdptw/
│   ├── core/
│   ├── solver/
│   ├── verification/
│   ├── application/
│   └── profiles/
│       ├── standard/
│       └── <namespace>/             # §5
├── adapters/
│   ├── common/ · input/
│   ├── s3/                          # LocalStack 동일 adapter
│   └── <provider>/                  # DEFERRED
├── backends/
│   └── route-selection-ortools-cpsat/   # OPTIONAL
├── apps/ cli/ · api/ · worker/
├── deployment/                      # DEFERRED · 보통 pom 없음
└── docs/
```

### 4.3 금지선 (MUST NOT)

| From | MUST NOT | 이유 |
|---|---|---|
| core, solver, verification | Cloud/HTTP 인프라 SDK | kernel 보호 |
| core, solver, verification, application | OR-Tools / MIP vendor API | 기본 optimizer-free |
| core … application | JDBC/JPA/RDB, Redis | 저장 전제 (§3.2) |
| verification | solver | 독립 검증 |
| profiles | solver search internal | 정책 격리 |
| application | S3·provider·OR-Tools SDK | port 유지 |
| 모든 모듈 | 타 모듈 `.internal` | 공개 API 우회 |
| backends | verification | 검증 우회 금지 |

허용: verification 이 **core** 순수 계산을 공유.  
금지: solver 증분 캐시·내부 객체를 검증이 권위로 믿음.

### 4.4 Package 책임 (요약)

Base: `com.ronext.rpdptw`. 세부는 implementation. **Domain 의미 재정의 금지.**

| 구역 | 책임 요약 | 경계 |
|---|---|---|
| core / input·domain·normalization·travel·propagation | 정본·사실·전파 | multi-version 없음, I/O 금지 |
| core / evaluation.api · runtime | SPI · BoundProfile 결합 | 고객명 분기 금지 |
| solver / search·state·portfolio | ALNS·상태·포트폴리오 | 최종 발행 권위 없음 |
| solver / pool·selection.api·hybrid | 선택 hybrid 전제 | vendor type 금지, GATED |
| verification / candidate·result | cache-free 재계산·무결성 | solver dep 금지 |
| application / execution·port.\* | 각본·port | 상태 바이트는 S3 adapter |
| adapters/input·s3 | 정본 변환 · S3/LocalStack | core 침투 금지 |
| backends/route-selection-\* | OR-Tools 등 구현 | optional |
| profiles/\* | SPI 구현 | core only 의존 |

### 4.5 모듈 ≠ 배포 단위 · 패키지 크기

| 구분 | 의미 | 개수 감각 |
|---|---|---|
| Maven module | 컴파일 경계 | 십수 개 전후 |
| 배포 단위 | Lambda zip / 컨테이너 이미지 | 보통 **`apps/worker` 등 소수** |

```text
여러 rpdptw-* · adapters jar
        │  apps/worker 가 의존으로 조립
        ▼
  worker.zip 또는 이미지 1개 (흔함)  →  Lambda 또는 ECS (O1 OPEN)
```

- **pom 개수 ≠ 함수 개수 ≠ zip 개수.**  
- **배포 크기** 는 모듈 개수가 아니라 **worker 조립에 넣는 의존 집합** 으로 결정. 같은 기능이면 multi-module 합본 ≈ 단일 fat jar.  
- default 에서 backends 제외 → 기본 배포를 작게 유지 가능.  
- 이 트리는 “Lambda 전형 폴더”가 아니라 **솔버 모듈 DAG** 이다.

### 4.6 Build 로 막기

| # | 검사 | 기대 |
|---|---|---|
| 1 | core/solver/verification 의 cloud SDK | 0 |
| 2 | core…application 의 JDBC/Redis | 0 |
| 3 | S3 SDK outside `adapters/*` | 0 |
| 4 | OR-Tools outside `backends/*` | 0 |
| 5 | verification → solver | 0 |
| 6 | reactor cycle | 없음 |
| 7 | (권장) `.internal` 교차 참조 | 0 |

도구(Enforcer/ArchUnit 등)는 implementation. **위반 시 빌드 실패** 가 요구.  
배치: `build/architecture-rules`, parent 에 business/cloud/ortools 공통 의존 금지.

### 4.7 §4 한 줄

**의미·탐색·검증은 SDK 없이. verification ↛ solver. I/O는 adapters, 선택 엔진은 backends. 배포는 apps 가 포장. 고객 규칙은 §5.**

---

## 5. 고객·업종 요구 확장 가이드

**이 장:** 고객 요구를 **어디에 적고**, **어느 모듈에 코드를 두며**, **`customerId` 를 YAML 로 SPI 구현에 어떻게 연결하는지**.  
**이 장이 아님:** 단가 수치, Domain pair 수식 재정의, 개별 고객 구현 승인, port 용어 전문 정의(→ **§6.2**).

### 5.1 원칙

| # | 원칙 |
|---|---|
| 1 | 정본 입력 **하나** (D1). 고객마다 스키마 multi-version 금지 → `adapters/input` |
| 2 | 규칙(제약·점수·목적) = **`profiles/<namespace>`** 가 core SPI 를 구현 |
| 3 | plan 에 **`customerId`**. 연결은 **YAML 카탈로그** (§5.3) — core/solver 에 `switch(customerId)` 금지 |
| 4 | default 구현 + 필요할 때만 특화 (`implements` / `extends Default*`). **solver 엔진 상속 금지** |
| 5 | solver 와 verification 은 **같은 BoundProfile** |
| 6 | **미등록·미정의 고객/슬롯 → default 자동 적용** (§5.3.5). 신규 고객이 YAML 행 없이 와도 풀이 가능해야 함 |

```text
plan.customerId
  → YAML (customer-binding + component-catalog)
  → BoundProfile (domain / score / objective / hard …)
  → solver · verification   # customerId 재분기 없음
```

### 5.2 꼭 알 이름 (2개면 충분)

**다섯 개를 나란히 외울 필요 없다.**  
실행 관점에서 기억할 것은 사실상 **둘** 이다.

| 꼭 알 것 | 쉬운 말 |
|---|---|
| **customerId** | plan 에 있는 **고객사 코드** (“누구 요청인가”) |
| **BoundProfile** | bind 후 solver/verification 이 쓰는 **이번 풀이 도구 세트** (점수·제약 구현 실물) |

```text
plan.customerId
      │
      ▼
 YAML 이 읽고 조립   (상세는 §5.3 — 여기 이름을 식별자 체계로 늘리지 않음)
      │
      ▼
 BoundProfile  ──► solver / verification
```

- YAML 에 행이 없으면 **`customers.default`** 로 BoundProfile 을 만든다 (§5.3.5).  
- core/solver 는 customerId 로 분기하지 않고, **BoundProfile 만** 본다.

#### YAML 안에만 있는 이름 (부록 · 설정 필드)

아래는 **식별자 체계를 하나 더 만든 것이 아니라**, YAML/설정의 **필드 이름** 이다.  
§5.3 을 볼 때만 알면 된다.

| 필드 | 왜 있나 | 없어도 되나 |
|---|---|---|
| **`components.*` 의 값** (예: `score-default`) | catalog 에서 클래스를 찾기 위한 **짧은 키** | binding 에 없으면 default 행 키 사용 |
| **`profileKey`** | 어느 profile 모듈 묶음인지 표시·조립 힌트 | default 행 값으로 merge 가능 |
| **`requirements[]`** | 설계 카드 추적용 메모 (감사). **실행 클래스가 아님** | 생략 가능 (빈 목록·default) |

**RequirementId / component id / ProfileKey** 를 customerId·BoundProfile 과 **동급 식별자 5종** 으로 외우지 않는다.  
필요하면 “YAML 필드” 로만 취급한다.

#### 한 줄

**손님(`customerId`) → YAML이 도구 세트를 고름 → 그 결과물(`BoundProfile`)로 푼다.**

### 5.3 YAML 카탈로그 (기본 · 합의)

**형식: YAML MUST.**  
역할: **“이번 customerId 풀이에 어떤 SPI 구현을 꽂을지”** 를 선언한다.  
로직 본문·수식·ALNS·시크릿·Domain 의미 재정의는 YAML 이 아님 (Java / Domain / 별 설정).  
YAML = **플러그 배치도** (선택·버전·component id·paramsRef).

필수 두 파일 (2층):

| 파일 | 한 줄 |
|---|---|
| **`customer-binding.yaml`** | **누구(customerId)** → 어떤 profile · 어떤 요구 · 각 슬롯에 **어느 component id** |
| **`component-catalog.yaml`** | **component id** → 어느 **Java 클래스**(또는 등록 키) · 파라미터 참조 |

위치 관례: `apps/*/src/main/resources/rpdptw/`  
profile 모듈이 component 조각 YAML 을 낼 수 있으나 **명시 merge**, first-wins 금지.

아래 필드명은 **Architecture 관례(스키마 초안)** 이다. 구현 시 동일 의미면 rename 가능하나, **슬롯 집합·2층 구조·default fallback** 은 유지한다.

#### 5.3.1 BoundProfile 슬롯 목록 (꽂을 수 있는 자리)

슬롯 = BoundProfile 이 들고 solver/verification 이 호출하는 **SPI 자리**.

| 슬롯 키 | 필수? | 개수 | 대응 SPI (관례) | 하는 일 | 비우면 |
|---|---|---|---|---|---|
| **`domainSupport`** | 권장 | 1 | `DomainEvaluationSupport` 등 | 평가가 쓰는 domain 쪽 훅·기본 동작 묶음 | 생략 시 **default 행** merge (§5.3.5) |
| **`scoreCalculator`** | 필수(최종) | 1 | `ScoreCalculator` | 해/route 점수 산출 | 생략 시 default 행; default 에도 없으면 fail |
| **`objective`** | 필수(최종) | 1 | `Objective` / comparator | 점수·다목적 비교 (사전식·가중 등) | 생략 시 default 행; default 에도 없으면 fail |
| **`hardConstraints`** | 선택 | 0..n | `HardConstraint` | 깨지면 불가인 제약들 | 생략/빈 목록 → default 행 또는 추가 hard 없음 |
| **`metrics`** | 선택 | 0..n | `MetricCalculator` | 점수 전에 재는 중립 지표 묶음 | score 가 내부에서 직접 재도 됨 |
| **`softConstraints`** | 선택 | 0..n | soft 제약 SPI (있을 때) | 깨져도 가능·페널티 | 없으면 생략 |
| **`solvePlan`** / profile 조립 | 선택 | 0..1 | plan 조합자 | metric→score→비교 파이프 명시 | Factory 기본 조립 |

- **단일 슬롯** (`scoreCalculator` 등): component id **문자열 하나**.  
- **목록 슬롯** (`hardConstraints` 등): component id **배열**. 순서 = 평가 순서(관례; 구현이 문서화).  
- 슬롯에 적는 값은 **component-catalog 의 키** 이지, Java FQCN 을 binding 파일에 직접 쓰지 않는 것을 권장 (2층 분리).

#### 5.3.2 `customer-binding.yaml` 필드

**루트**

| 필드 | 필수 | 타입 | 설명 |
|---|---|---|---|
| `catalogVersion` | 권장 | string | 이 binding 파일 스키마/내용 버전. 재현·감사 |
| `customers` | 필수 | map | 키 = **customerId**. **`default` 키 MUST** (미등록 고객 fallback, §5.3.5) |

**`customers.<customerId>` 한 행**

| 필드 | 필수 | 타입 | 설명 |
|---|---|---|---|
| `profileKey` | 필수 | string | profile 모듈/정책 묶음 id (예: `cvs`, `standard`) |
| `profileVersion` | 권장 | string | 해당 profile 호환 버전. 내용 바뀌면 버전 up |
| `requirements` | 권장 | string[] | **RequirementId** 목록. 설계 카드·감사 추적. bind 시 components 와 **교차 검증** 가능 |
| `components` | 필수 | object | 슬롯 → component id (또는 id 목록). **§5.3.1** |
| `description` | 선택 | string | 운영 설명 |
| `enabled` | 선택 | bool | `false` 면 resolve 실패 또는 거부 (기본 true) |
| `configRef` | 선택 | string | 고객 전역 설정 묶음 참조 (단가 루트 등). 본문 아님 |

**`components` 안 (슬롯 채우기)**

| 키 | 값 형태 | 예 |
|---|---|---|
| `domainSupport` | string (component id) | `domain-default` |
| `scoreCalculator` | string | `score-default` / `score-cvs-contract-cost` |
| `objective` | string | `objective-default` |
| `hardConstraints` | string[] | `[hard-bulk-3d-pack]` |
| `metrics` | string[] | `[metric-vehicle-count, metric-contract-cost]` |
| `softConstraints` | string[] | 선택 |

**이 파일에서 하지 않는 것:** class FQCN, 수식, ALNS 파라미터 실험 수치 확정.

#### 5.3.3 `component-catalog.yaml` 필드

**루트**

| 필드 | 필수 | 타입 | 설명 |
|---|---|---|---|
| `catalogVersion` | 권장 | string | catalog 파일 버전 |
| `components` | 필수 | map | 키 = **component id** (binding 이 가리키는 이름) |

**`components.<componentId>` 한 항목**

| 필드 | 필수 | 타입 | 설명 |
|---|---|---|---|
| `slot` | 필수 | string | 이 구현이 속하는 슬롯 (`scoreCalculator`, `hardConstraint` 등). binding 과 **불일치 시 fail** |
| `class` | 조건부 | string | Java FQCN. runtime 인스턴스화 대상 |
| `bean` / `registrationKey` | 조건부 | string | `class` 대신 기동 시 코드 등록 맵 키 (둘 중 하나 MUST) |
| `paramsRef` | 선택 | string | 외부 파라미터 문서/파일/artifact id (단가표 등) |
| `params` | 선택 | object | **스칼라·짧은** 설정만 (가중치 키, 플래그). 장문 수식·대용량 표 금지 |
| `extends` / `base` | 선택 | string | 다른 component id 를 감싸 decorate 할 때 (선택 패턴) |
| `description` | 선택 | string | 설명 |

**`params` 에 넣어도 되는 것 / 안 되는 것**

| 가능 예 | 불가 예 |
|---|---|
| `weightVehicle: 1.0` | `formula: "cost = a*x + …"` 장문 |
| `tariffTableId: cvs-2026-q3` | 전체 요금 CSV 본문 |
| `enableContractCost: true` | IAM 키, DB URL |

#### 5.3.4 requirements 와 components 의 관계

| | `requirements` | `components` |
|---|---|---|
| 역할 | **문서·감사** — 이 고객에 어떤 요구가 켜져 있는지 | **실행** — 실제로 어떤 클래스가 도는지 |
| bind | 없어도 실행 가능한 구현도 있음 | **없으면 실행 불가** (필수 슬롯) |
| 권장 | 설계 카드 ID 와 1:1 | 요구를 만족하는 component 를 슬롯에 배치 |
| 검증 | (권장) 요구 X 가 있으면 대응 component 가 있는지 검사 | slot·class 존재·classpath 검사 |

예: 요구 `CVS-SCORE-CONTRACT-COST` 가 있으면  
`scoreCalculator: score-cvs-contract-cost` 가 있어야 하고,  
catalog 에 그 id 의 class 가 있어야 한다.

#### 5.3.5 검증 규칙 (bind / 기동) · **default 자동 적용 (합의)**

**예 — 정의가 없으면 default 로 간다.**  
신규 고객이 YAML 에 아직 없을 수 있으므로, **미등록 customerId 도 풀이 가능** 해야 한다 (2026-07-31 합의).

##### Fallback 대상 (MUST)

| 상황 | 동작 |
|---|---|
| `customers` 에 plan.`customerId` **행 없음** | **`customers.default`** (또는 동등 키 `__default__`) 행을 사용 |
| 고객 행은 있으나 **필수 슬롯 누락** | 해당 슬롯만 **default 행의 같은 슬롯** (또는 전역 default component id) 으로 채움 |
| 선택 슬롯 생략 (`hardConstraints` 등) | default 행 값 또는 **빈 목록** (default 행 정의 따름) |
| `requirements` 생략 | default 행의 requirements (보통 표준 요구만) 또는 빈 목록 |

##### Fallback 이 아닌 것 (여전히 fail)

| 상황 | 결과 |
|---|---|
| **`customers.default` 행 자체가 없음** | **기동/bind fail** — fallback 의 기준점이 없음 |
| 장착할 component id 가 catalog 에 없음 | **fail** |
| `slot` 불일치 · class 가 classpath 에 없음 | **fail** |
| 고객 행에 **명시** 한 component 가 잘못된 경우 | **fail** (오타를 default 로 덮지 않음 — “명시한 값” 우선) |

##### 규범 키 · 해석 순서

```text
1. customers.<plan.customerId>  조회
2. 없으면 → customers.default  사용  (fallbackUsed = true)
3. 고객 행 + default 행 을 슬롯 단위로 merge
     - 고객 행에 슬롯이 있으면 그 값
     - 없으면 default 행 슬롯
4. component-catalog 로 인스턴스화 → BoundProfile
5. 관측: fallbackUsed, resolvedCustomerId, effective component ids 를 로그/telemetry 에 남김 (권장)
```

**`customers.default` 는 MUST 로 카탈로그에 존재** 한다. 예:

```yaml
customers:
  default:                         # 미등록 고객·슬롯 공백의 기준
    profileKey: standard
    profileVersion: "1.0.0"
    requirements: []
    components:
      domainSupport: domain-default
      scoreCalculator: score-default
      objective: objective-default
      hardConstraints: []

  CUS-CVS-001:                     # 등록 고객 — 필요한 슬롯만 덮어씀
    profileKey: cvs
    profileVersion: "1.0.0"
    requirements: [CVS-SCORE-CONTRACT-COST]
    components:
      scoreCalculator: score-cvs-contract-cost
      objective: objective-lex-cost-then-vehicles
      # domainSupport 생략 → default 행의 domain-default 자동
```

##### 운영 함의

| 함의 | 설명 |
|---|---|
| 신규 고객 즉시 풀이 | YAML 행 추가 전에도 **standard default** 로 동작 |
| 특화는 점진 등록 | 나중에 `CUS-…` 행·component 만 추가 |
| 실수 감지 | fallback 사용 시 **로그/메트릭** 으로 “미등록 고객 유입” 가시화 권장 |
| 특화 강제 고객 | 별도 게이트(허용 목록)는 **제품/운영 정책** — Architecture 기본은 fallback 허용 |

**core/solver 는 여전히 `switch(customerId)` 하지 않는다.**  
fallback 은 **YAML binder (application/config 층)** 만 수행한다.

#### 5.3.6 전체 예시 (필드가 드러나게)

**customer-binding.yaml**

```yaml
catalogVersion: "1.0.0"
customers:
  # MUST: 미등록 고객·슬롯 공백 fallback (§5.3.5)
  default:
    profileKey: standard
    profileVersion: "1.0.0"
    requirements: []
    components:
      domainSupport: domain-default
      scoreCalculator: score-default
      objective: objective-default
      hardConstraints: []

  CUS-CVS-001:
    profileKey: cvs
    profileVersion: "1.0.0"
    description: "편의점 — 계약 비용 score"
    requirements:
      - CVS-SCORE-CONTRACT-COST
      - STD-VEHICLE-COUNT
    components:
      # domainSupport 생략 → default 행 자동 merge
      scoreCalculator: score-cvs-contract-cost
      objective: objective-lex-cost-then-vehicles
      metrics:
        - metric-vehicle-count
        - metric-contract-cost
    configRef: config/cvs-root

  CUS-BULK-002:
    profileKey: bulk-cargo
    profileVersion: "1.0.0"
    requirements:
      - BULK-HARD-3D-PACKING
    components:
      scoreCalculator: score-default
      objective: objective-default
      hardConstraints:
        - hard-bulk-3d-pack
```

**component-catalog.yaml**

```yaml
catalogVersion: "1.0.0"
components:
  domain-default:
    slot: domainSupport
    class: com.ronext.rpdptw.profiles.standard.DefaultDomainEvaluationSupport
    description: "표준 domain 훅"

  score-default:
    slot: scoreCalculator
    class: com.ronext.rpdptw.profiles.standard.DefaultScoreCalculator

  score-cvs-contract-cost:
    slot: scoreCalculator
    class: com.ronext.rpdptw.profiles.cvs.ContractCostScoreCalculator
    paramsRef: config/cvs-contract-tariff
    # params: { }  # 짧은 플래그만

  objective-lex-cost-then-vehicles:
    slot: objective
    class: com.ronext.rpdptw.profiles.cvs.LexCostThenVehiclesObjective

  metric-vehicle-count:
    slot: metric
    class: com.ronext.rpdptw.profiles.standard.VehicleCountMetric

  metric-contract-cost:
    slot: metric
    class: com.ronext.rpdptw.profiles.cvs.ContractCostMetric

  hard-bulk-3d-pack:
    slot: hardConstraint
    class: com.ronext.rpdptw.profiles.bulk.ThreeDPackHardConstraint
```

#### 5.3.7 처리 순서 (application)

1. plan 에서 `customerId`  
2. `customer-binding.yaml` resolve — **없으면 `customers.default`**, 슬롯 공백은 default 행과 merge (§5.3.5)  
3. 각 슬롯 component id → `component-catalog.yaml`  
4. class 인스턴스화 → **BoundProfile**  
5. (권장) fallback 사용 여부 로그  
6. worker 에 전달 — 이후 **customerId 비즈 분기 금지**

#### 5.3.8 컴파일 vs 실행 — YAML 이 “솔루션에 반영” 되는 방식

**YAML 은 컴파일러가 아니다.**  
점·도메인 로직 **소스 코드** 는 평소처럼 Java 로 작성·Maven 으로 **미리** `.class` 가 된다.  
YAML 은 **이미 빌드된 클래스 중 무엇을 이번 풀이에 쓸지** 고르는 **설정** 이다.

```text
[빌드 타임 — javac / mvn package]
  rpdptw-core
    → ScoreCalculator.java (interface)  ──► ScoreCalculator.class
  rpdptw/profiles/standard
    → DefaultScoreCalculator.java     ──► DefaultScoreCalculator.class
  rpdptw/profiles/cvs
    → ContractCostScoreCalculator.java ──► ContractCostScoreCalculator.class
  apps/worker
    → pom 이 profiles/* 를 dependency 로 끌어 모음
    → resources 에 customer-binding.yaml, component-catalog.yaml 포함
    → worker.jar / zip 안에
         · 위 .class 들
         · YAML 텍스트
       가 같이 들어감

[실행 타임 — plan 한 건]
  1. plan.customerId 읽기
  2. YAML 파싱 (텍스트 → 맵)
       scoreCalculator: score-cvs-contract-cost
  3. component-catalog 에서
       score-cvs-contract-cost
         → class: …ContractCostScoreCalculator
  4. Class.forName + new  (또는 기동 시 등록된 Factory 맵에서 get)
       → ScoreCalculator 인스턴스
  5. BoundProfile 에 넣기
  6. solver 는 예전에 컴파일된 코드 그대로:
       boundProfile.scoreCalculator().calculate(...)
       // 구체 클래스 이름을 소스에 안 적었음
```

| 질문 | 답 |
|---|---|
| YAML 에 적으면 컴파일이 다시 되나? | **아니오.** YAML 변경만이면 **재컴파일 없이** 재시작·재로드로 다른 구현을 고를 수 있음 (클래스가 **이미 jar 안**에 있을 때) |
| 새 ScoreCalculator 클래스를 **처음** 추가하면? | **Java 작성 + `mvn package` 필요.** YAML 줄만으로는 새 로직이 생기지 않음 |
| solver 는 고객 클래스를 import 하나? | **안 함.** compile 의존은 **interface (`ScoreCalculator`)** 뿐 |
| 클래스가 jar 에 없는데 YAML 만 있으면? | 실행 시 **ClassNotFound / bind 실패 (fail fast)** |
| 그래서 apps pom 에 profiles 가 필요한 이유 | 구현 `.class` 를 **classpath(배포 패키지)** 에 넣기 위함 |

**한 줄:**  
**컴파일 = interface + 구현 클래스들을 jar 에 넣기.  
YAML = 그 jar 안에서 이번 customerId 용 구현을 고르기.  
솔루션(탐색) 코드는 interface 만 보고 이미 컴파일되어 있음.**

XML/properties 는 이 제품 **기본 경로 아님**.

### 5.4 SPI 구현 패턴

**SPI 용어 정의 → §6.2.** 여기서는 **고객 규칙 구현 패턴** 만 둔다.

- core/solver 는 `ScoreCalculator` **이름과 메서드** 만 알고 호출한다.  
- 편의점용·기본용 구현 클래스 이름은 몰라도 된다.  
- 어떤 구현을 쓸지는 **YAML 카탈로그** 가 고른다 (§5.3).  
- 이 제품에서 SPI = **“core 소유 interface + profile 구현”** 패턴 (Java `ServiceLoader` 기술 단정이 아님).

| Contract (SPI 예) | 정의(owner) | 구현 |
|---|---|---|
| ScoreCalculator, DomainEvaluationSupport, HardConstraint, Objective, Profile 조립 | **core `evaluation.api`** | **profiles/\*** |
| 입력 → canonical | adapter 계약 | **adapters/input** |
| ArtifactStore 등 | application.port.out | **adapters/** (→ §6) |

```text
interface ScoreCalculator                    # core
DefaultScoreCalculator implements …          # profiles/standard
ContractCostScoreCalculator
  extends DefaultScoreCalculator             # profiles/cvs — YAML 이 가리킬 때만
```

| 해도 됨 | 금지 |
|---|---|
| implements / extends **Default\*** | `extends` solver 엔진 |
| YAML 로 구현 선택 | core/solver `switch(customerId)` |
| 같은 BoundProfile 로 검증 | 탐색만 특화·검증은 다른 calculator |
| 새 SPI 구멍 = Domain 합의 후 공통 필요 시만 | 한 고객 전용 메서드를 core interface 에 추가 |

### 5.5 코드·설정을 둘 위치

| 할 일 | 위치 |
|---|---|
| 거의 항상: 정책·점수·hard | `rpdptw/profiles/<ns>/` (`constraint|metric|score|plan|…`) |
| 입력 포맷 | `adapters/input/` |
| 정본에 필드 없음 | Domain 합의 → `core` 값 타입 → adapter 매핑 (고객 전용 타입명 금지) |
| 고객 매핑 표 | **`customer-binding.yaml` + `component-catalog.yaml`** |
| 배포에 profile 포함 | `apps/*/pom` dependency + resources 에 YAML |
| 손대지 않음 | `solver/search` 고객 분기, verification 고객 포크, `adapters/s3` 에 비즈 로직 |

### 5.6 요구 기록 · 분류

한 요구 = 한 카드 (이슈/부록 등 팀 관례).

| 항목 | 예 |
|---|---|
| RequirementId | `CVS-SCORE-CONTRACT-COST` |
| 적용 customerId | `CUS-CVS-001` |
| 한 줄 | 계약 비용 최소화 score |
| 분류 | 아래 A–F |
| hard/soft · SPI 슬롯 | soft / scoreCalculator |
| Domain 변경? | 없음 또는 합의 필요 |
| YAML component id | `score-cvs-contract-cost` |
| MUST NOT | core 고객 분기, 검증 생략 |

| 분류 | 성격 | 손대는 곳 |
|---|---|---|
| A | 점수·목적 | profiles + YAML score/objective |
| B | hard | profiles constraint + YAML |
| C | 입력 사실 부족 | Domain → core 필드 → adapter |
| D | pick/drop 구조 | Domain 먼저 → core/adapter/profile |
| E | 포맷만 | adapters/input |
| F | 외부 엔진 | backends/* (선택) |

### 5.7 시나리오 (배치만)

| 시나리오 | 요구 요지 | 작업 위치 |
|---|---|---|
| **편의점** | 대수 + 계약 비용 score (`vehicleFeature`, 최종 지역) | core 사실 필드(필요 시) · `profiles/cvs` ScoreCalculator · YAML 슬롯 · adapter 매핑 |
| **대형 화물** | 치수 + 3D packing **hard** | Domain 치수 의미 · core 필드 · `profiles/bulk` HardConstraint · YAML hardConstraints · verification 동일 제약 |
| **공장 배송** | multi pick → single drop (대리점 pick, 공장 drop) | Domain: 기존 pair N건으로 표현 가능한지 **먼저** · 가능 시 adapter 매핑+profile · 불가 시 Domain 확장 후 core. solver 에 공장 모드 분기 금지 |

### 5.8 착수 체크리스트

1. 요구 카드 (Id + customerId)  
2. 분류 A–F  
3. **YAML** 두 파일 갱신  
4. Domain 공백 있으면 Domain 합의  
5. 필요 시 core 사실 필드  
6. adapter 매핑 · plan.customerId 유지  
7. profiles 에 Default/특화 클래스  
8. apps 에 profile + YAML 조립  
9. 테스트: 동일 customerId 로 탐색=검증; **미등록 customerId → default 행** 으로 풀이 성공  
10. 테스트: `customers.default` 삭제 시 기동/bind 실패; 잘못된 명시 component 는 fail  
11. PR: core/solver 고객 분기 없음 · 수식 YAML 장문 임베드 없음  

### 5.9 §5 한 줄

**형식은 adapter, 사실은 core(필요 시), 규칙은 profiles SPI, 연결은 YAML(`customerId`→component, 미등록은 default 자동), 실행은 BoundProfile — core/solver 는 고객 이름을 모른다.**

**관련:** 모듈 지도 §4 · port 목록 §6 · profile seam 한 줄 §9.1 · 의미는 Domain · gate 는 Master.

---

## 6. Port / SPI / adapter

**이 장:** port / SPI / adapter **용어 정의(한 곳)** · 창구 목록 · REST↔port 짧은 매핑.  
**이 장이 아님:** REST 접수 전체 타임라인(→ **§3.4**), 모듈 tree(→ **§4**), YAML 필드 표(→ **§5.3**).

### 6.1 이 장이 말하려는 것

| 사람 말로 | 코드 쪽 |
|---|---|
| **HTTP 입구** | `apps/api` — 이미 공유된 **REST 스펙** 으로 받음 |
| **연출·진행 담당** | `application` — 접수·검증·S3 저장·(이후) 탐색·검증·발행 **순서** |
| **문제 푸는 두뇌** | `core` + `solver` + `verification` |
| **고객마다 다른 점수 규칙** | `profiles` + YAML (§5) |
| **S3 · 입력 변환 실무** | `adapters` |

§6 질문:

1. application 이 S3·규칙·탐색을 **어떤 창구(port/SPI/adapter)** 로 연결하나?  
2. REST 단계가 어느 **port.in** 에 붙나? (흐름 본체는 **§3.4**)

### 6.2 세 단어 — 전문 정의 (한 곳)

#### adapter (어댑터) = “바깥 세상 플러그”

**실제 기술** 을 다루는 코드.

| 예 | 하는 일 |
|---|---|
| `adapters/input` | 고객사 JSON/CSV → **우리 정본(canonical)** 한 가지 형식으로 변환 |
| `adapters/s3` | 파일을 **S3**(또는 로컬 LocalStack)에 올리거나 받기 |

- “AWS 를 어떻게 부르는지” 는 **여기만** 알면 된다.  
- application / core / solver 는 AWS SDK 를 **직접 쓰지 않는다**.

#### port (포트) = “application 이 쓰는 창구 이름”

**연출 담당이 바깥에 부탁할 때 부르는 메뉴 이름.**  
Java 로는 보통 `interface`.

| 종류 | 쉬운 말 | 예 |
|---|---|---|
| **안으로 들어오는 창구** (`port.in`) | 앱이 application 에게 “이 일 해 줘” | `SubmitSolve` (접수), `GetSolveStatus` (상태 조회) |
| **바깥으로 나가는 창구** (`port.out`) | application 이 “저장해 / worker 띄워” | `ArtifactStore` (파일 저장), `WorkerDispatcher` (worker 실행) |

**port 와 adapter 관계:**

```text
application:  “ArtifactStore 에 저장해”
                    │
                    │  port.out  (창구 이름만 앎)
                    ▼
adapters/s3:  진짜로 S3 SDK 호출
                    │
                    ▼
                 AWS S3
```

- **port** = “저장해” 라는 **말**  
- **adapter** = 그 말을 듣고 **S3 에 올리는 손**  

저장소를 바꿔도 application 각본 문장은 그대로 두고, **adapter 구현만** 갈 수 있다.  
테스트할 때는 S3 대신 **가짜(fake) adapter** 를 꽂으면 된다.

> 이름 주의: `RunStateRepository` 처럼 Repository 가 들어가도 **DB/JPA 가 아니다.**  
> “상태 저장 창구” port 이름일 뿐이다. 구현은 S3 객체 (→ **§3.2**).

#### SPI = “점수·규칙 꽂는 콘센트”

**고객마다 다른 점수·hard 규칙** 을 넣기 위한 구멍.  
core 가 “점수는 이렇게 계산하는 물건이 필요하다” 고 **모양만** 정해 둔다 (`ScoreCalculator` 등).  
실제 계산기는 `profiles` 가 만들고, YAML 이 고른다 (**§5**).

| | port | SPI |
|---|---|---|
| **비유** | 은행 창구 (“돈 맡겨 주세요”) | 콘센트 (“이 규격 가전 꽂으세요”) |
| **다루는 것** | 저장, 실행, 입력 형식 | 점수, hard, 목적 비교 |
| **구현 위치** | **adapters** | **profiles** (선택 엔진은 **backends**) |
| **application 과의 관계** | 각본이 **직접** port 를 호출 | 각본/solver 는 **BoundProfile** 안 구현을 호출 |

**한 줄:**  
- 파일·클라우드 → **port + adapter**  
- 고객 점수 규칙 → **SPI + profiles + YAML** (§5)

### 6.3 REST ↔ port 매핑 (짧게)

흐름 본체·“동기” 정의·S3 key 규범 → **§3.4**. 여기서는 창구 연결만.

```text
[호출 시스템]  공유 REST 스펙으로 POST
        │
        ▼
[ apps/api ]
        │  port.in  AcceptSolve / SubmitSolve
        ▼
[ application ]
   · validation
   · s3Key = f(planId, customerId, …)
   · port.out ArtifactStore.put(input) ──► [ adapters/s3 ] ──► S3
        │
        ▼
   HTTP 200 + s3Key
        │
        (이후) worker · ExecuteWorkerRun · BoundProfile · solver · verification
        └─ 진행/결과도 같은 key 체계로 S3 갱신
```

application 소스에 있으면 **안 되는 것:**

- 컨트롤러/`application` 에 `new S3Client` 난립 (→ **adapter** 만)  
- `if (customerId.equals("편의점"))` 점수 분기 (→ **§5 SPI**)  
- 접수 API 안에서 ALNS 끝까지 돌리기 (→ **§3.4 접수 계약과 불일치**)

### 6.4 우리가 쓰는 창구 목록 (이름만)

#### 앱 → application (port.in)

| 이름 (관례) | 사람 말 | REST 와의 관계 |
|---|---|---|
| `AcceptSolve` / `SubmitSolve` (접수) | 스펙 검증 + S3 input 저장 + **s3 key 반환** | **동기 200** 구간의 본체 (§3.4) |
| `PrepareSolveSnapshot` | 풀이용 문제 고정본 | 접수 후·worker 쪽 (HTTP 200 이후) |
| `ExecuteWorkerRun` | worker 한 번 탐색 | 접수 HTTP **밖** |
| `CompleteWorkerRun` | worker 끝 처리 | 동일 |
| `SelectRoundChampion` | round champion | 동일 |
| `PublishVerifiedResult` | 검증 통과 결과 발행 | 동일 |
| `RequestCancellation` | 취소 | (선택) API |
| `GetSolveStatus` / `GetVerifiedResult` | 상태·결과 | **s3 key** 로 조회할 때 사용 가능. 호출 측이 S3 직접 읽어도 됨 |

`apps/api` 는 위 이름만 부르면 된다. ALNS·S3 SDK 세부를 몰라도 된다.

#### application → 바깥 (port.out) + adapter

| 이름 | 사람 말 | 손 (adapter) |
|---|---|---|
| `ArtifactStore` | 큰 파일·결과 바이트 저장/읽기 | `adapters/s3` (또는 테스트 fake) |
| `RunStateRepository` | 진행 상태 짧게 저장 | 역시 S3 쪽 (DB 아님, §3.2) |
| `WorkerDispatcher` | worker 프로세스/함수 실행 | 로컬 실행기 또는 클라우드 adapter |
| `ResultPublisher` | 최종 결과 발행 | S3 등 |
| 취소 / 로그·메트릭 | 취소, 관측 | adapter |

#### 입력 변환 (adapter, D1)

```text
고객마다 다른 JSON/엑셀  →  adapters/input  →  정본 딱 하나  →  솔버
```

- 정본을 고객마다 여러 버전으로 운영하지 **않는다**.  
- “점수 다르게” 는 입력 스키마가 아니라 **§5 규칙** 으로.

#### 점수·규칙 · 선택 엔진

| 구멍 | 구현 | 상세 |
|---|---|---|
| `ScoreCalculator` 등 SPI | `profiles/*` + YAML | **§5** |
| 경로 선택 엔진 (선택) | `backends/*` (OR-Tools 등). 기본은 안 씀 | **§9.2** |

### 6.5 일부러 섞지 말 것

| 하지 말 것 | 이유 |
|---|---|
| application / controller 에 AWS SDK | adapter 가 할 일 |
| 접수 API 안에서 ALNS 전체 완료 | 제품은 **검증+S3+200** (§3.4); 탐색은 이후 |
| core/solver 에 `if (고객)` | SPI + YAML (§5) |
| adapters/s3 에 점수 공식 | profiles |
| 입력 adapter 에 ALNS | solver |
| `RunStateRepository` = DB 필수 | 이름만 Repository, 구현은 S3 (§3.2) |
| Architecture 가 REST 필드 사전 재작성 | **공유 스펙** 소유. 배치는 이 문서 |

### 6.6 이동거리 · 검증 (한 줄)

| 것 | 어디 | 사람 말 |
|---|---|---|
| Travel (이동) | `core` | 지점 사이 이동 비용. 의미는 Domain. DB 표 아님 |
| 검증 | `verification` | 솔버 답을 **캐시 없이** 다시 확인. **발행 전** (접수 200 이후 단계). 격리 요약 → **§9.3** |

### 6.7 §6 한 줄

**application 은 순서를 port 로 짜고, S3 는 adapter 가 한다.  
REST 접수 계약(200+s3 key)은 §3.4. 고객 점수 규칙은 §5 SPI.**

---

## 7. Domain 개념 → 모듈 매핑

**이 장:** Domain 개념을 모듈 라벨에 붙이는 표만.  
**이 장이 아님:** Domain 의미 재정의.

| Domain | 모듈 | 주의 |
|---|---|---|
| canonical / adapter | core/input + adapters/input | multi-version 없음 |
| prepared travel · bound profile | core | |
| **immutable solve snapshot** | application 조립 + core 구성 | **해 아님** |
| Request / servicePattern / IDs | core/domain | |
| route–bank · COW | solver/state | bank ≠ UNASSIGNED |
| ALNS | solver/search | 기본 경로 |
| Hybrid | pool + selection + optional backend | C-17 GATED (§9.2) |
| verifier 2단 | verification | 발행 전 |
| publishable | application + S3 publisher | PASS 전 권위 없음 |

```text
immutable solve snapshot = 문제 고정본
SearchSnapshot/candidate = 해
PublishableResult        = 2단 PASS 후 (S3 에 저장)
```

---

## 8. local / worker / distributed (배치)

**이 장:** 실행 모드별 저장·compute 배치.  
**이 장이 아님:** 저장 전제 재정의(→ **§3.2**), REST 타임라인(→ **§3.4**).

| 모드 | 저장·조율 | compute |
|---|---|---|
| **local 단위** | 인메모리 port fake | 같은 JVM worker |
| **local 통합** | **LocalStack S3** + 같은 adapter | `apps/worker` 등 로컬 프로세스 |
| **worker** | S3 (실 또는 LocalStack) | portfolio → ALNS → (optional hybrid) → candidate |
| **distributed** | 실 S3 + **Step Functions** (reference, **§9.4**) | fan-out/fan-in (수치 OPEN); compute = Lambda\|ECS (O1 OPEN) |

**MUST:** 같은 application use case · 같은 verifier gate.  
**차이는 port 구현체·endpoint 뿐** (LocalStack ↔ 실 AWS).

Outer `ExecutionRound` / `WorkerRun` vs inner `HybridPhase`: hybrid 는 worker-local (C-17, **§9.2**).  
orchestration 각본은 application; durable 엔진 **reference** = **AWS Step Functions** (**§9.4**).  
worker/API compute 제품(**Lambda vs ECS**)만 **O1 OPEN**.
### 8.1 LocalStack (로컬 통합 타깃)

| 규칙 | 규범 |
|---|---|
| 로컬 e2e/통합 | **LocalStack S3** |
| application | LocalStack 모름 — endpoint 는 adapter/설정 |
| core/solver | LocalStack API 직접 호출 **MUST NOT** |
| LocalStack 기동 | 솔버 완료 evidence **아님** (A9) |

compose·이미지 태그: **OPEN** (`deployment/`).

### 8.2 Identity

SolveId / RoundOrdinal / WorkerRunId / AttemptId / ArtifactDigest.  
wire key schema O2/구현. 의미는 application port.

---

## 9. profile seam · hybrid · verifier · AWS (압축)

**이 장:** §5·§4·§3 과 겹치지 않게 **짧은 규범 요약** 만. 상세는 링크.

### 9.1 profile 확장 seam (A11)

optional 필드 → constraint/metric/score/`SolvePlan` (profile SPI) → BoundProfile.  
profiles JAR → core only. `latest`/first-wins MUST NOT.  
`if (customerId.equals(...))` in core/solver = violation.

상세(요구 카드 · YAML 카탈로그 · 시나리오) → **§5**.

### 9.2 optional MIP / hybrid (C-17)

| 항목 | 규범 |
|---|---|
| 기본 경로 | **ALNS only** |
| route pool + MIP | **C-17 GATED** |
| config | on/off 가능, **default off** |
| config true | **≠ C-17 승인 우회** |

default build optimizer-free. vendor module = **`backends/*` optional** assembly only (합의 B1; `adapters/` 아님).  
verification ↛ selection backend. MIP outcome 직발행 금지.

수치·OR-Tools 버전·production ON = OPEN.

### 9.3 verifier 격리

| 단계 | 모듈 |
|---|---|
| candidate solution verifier | verification/candidate |
| result-integrity verifier | verification/result |

verification ↛ solver (§4.3). cache 비신뢰. 발행 전. core pure 함수 공유 허용.  
별 JVM = OPEN (1차는 Maven 차단).

### 9.4 AWS · LocalStack reference

**Reference platform = AWS.** Google Cloud (Cloud Run / Workflows / GCS) 는
current tree의 **legacy placeholder** 이며 target 확장이 아니다.

| 역할 | reference | 비고 |
|---|---|---|
| 저장 (결과·중간·상태·접수 input) | **S3** | **only** (§3.2). DB·Redis 없음 |
| 로컬 통합 | **LocalStack (S3)** | §3.2 · §8 |
| orchestration 각본 | application | provider-neutral. Domain 점수 소유 아님 |
| durable 엔진 | **AWS Step Functions** | reference 조립. 논리 계약은 coordinator/port |
| worker / API compute | **Lambda 또는 ECS** | **O1 OPEN** — 한쪽 단정 MUST NOT |
| API 조립 | API Gateway + `apps/api` 등 | placeholder ≠ target 완료 |

클라우드 SDK = adapters/deployment 만. OR-Tools = backends/* 만.  
LocalStack compose 세부 OPEN. production cutover ≠ reference 선택 (A9, A10).

---

## 10. current vs target · migration · 위험

| 축 | current (tracked tree) | target / reference |
|---|---|---|
| Maven | 단일 project | multi-module |
| package | `com.ronext.optimizer` | `com.ronext.rpdptw` |
| 계산 | synthetic placeholder | ALNS + verifier 2단 |
| 클라우드 | **GCP legacy** (GCS + Cloud Workflows + Cloud Run) | **AWS** (S3 + Step Functions + Lambda\|ECS) |
| 저장 | GCS direct SDK · prefix listing | **S3 only** · exact-key/CAS · DB·Redis 없음 (§3.2) |
| 로컬 | (미정) | **LocalStack S3** |
| orchestration | Cloud Workflows placeholder | **Step Functions** + application 각본 |
| compute | Cloud Run HTTP | **Lambda \| ECS — O1 OPEN** |
| hybrid | evidence 없음 | C-17 GATED default off |

Migration: legacy GCP characterization → semantic core → ports → AWS reference
(LocalStack parity) → shadow → gated cutover.  
Verifier PASS 전 placeholder 를 정상 발행으로 승격 금지.

| 위험 | 완화 |
|---|---|
| verification→solver | enforcer (§4.6) |
| Redis/RDB 재도입 | §3.2 · §4.3 |
| LocalStack = 완료 | A9 (§1.3 · §8.1) |
| C-17 config 우회 | Domain 규칙 · §9.2 |
| snapshot/해 혼동 | §7 |

---

## 11. OPEN / DEFERRED / traceability

| ID | 내용 |
|---|---|
| O1 | Lambda vs ECS |
| O2 | wire 필드 깊이 (Domain) |
| O3 | adapter 이름 |
| O5 | Phase C — **DONE** (`docs/README.md`) |
| O6 | module 상세 깊이 |
| — | S3 키·CAS·retention · LocalStack compose · workflow 엔진 |

| 출처 | 절 |
|---|---|
| A1,A5,A6,A8–A11,D1,D2 | §2–§9 |
| 저장 S3 only · Redis 포기 | **§3.2** (링크: §4.3 · §9.4) |
| LocalStack | **§3.2 · §8** (링크: §9.4) |
| REST 접수 200+s3 key | **§3.4** |
| §3 축소 (A6 배치만) | §3 머리 · §3.5 |
| 고객·업종 확장·SPI | **§5** (용어: §6.2) |
| 모듈 DAG · verification ↛ solver | **§4** |
| C-17 hybrid | **§9.2** |

의도적 미포함: Domain 수식 전문 · phase 체크리스트 · C-17 수치 · RDB/Redis 스키마 · 실험 수치 확정.

---

## 12. 검수 요청 (완료)

1. **D1** 단일 canonical + adapter 하나  
2. **D2/O1** compute 미결정 OK  
3. **저장** DB·**Redis 없음** · **S3 only** · Repository≠JPA (**§3.2**)  
4. **로컬** **LocalStack S3** (단위 fake 별도) (**§8**)  
5. **C-17** ALNS-first · default off · 승인 우회 금지 (**§9.2**)  
6. **Module** verification ↛ solver (**§4**)  
7. **Verifier** 2단 · cache 비의존 (**§9.3**)  
8. **Profile** SPI · **§5** (`customerId`→**YAML 카탈로그**→BoundProfile, default+override, core 분기 금지)  
9. **Snapshot** ≠ 해 (**§7**)  
10. LocalStack/S3 ≠ production 승인·솔버 완료 evidence (**§1.3 · §8.1**)  
11. **§3** 은 런타임 철학이 아니라 **배치 표 + REST 타임라인** (Master §5.2 링크)  
12. **REST** 동기 = validation+S3+200+s3 key; ALNS 전체 완료 아님 (**§3.4**)  

~~승인 시 `status: APPROVED`.~~ → **`APPROVED`** (2026-07-31). 커밋은 사용자 요청 시만.  
본 v3.4 는 구조 재정비 후 검수 완료본이며, 검수 과정에서 반영된 합의 외 신규 설계 변경 없음.

---

## 13. 다음 액션

1. ~~사용자 검수~~ → **`APPROVED`** (2026-07-31)  
2. ~~Master/Domain 다음 액션 링크 갱신~~ (동시 반영)  
3. ~~Phase C (O5)~~ → **완료** — 권위 지도·SUPERSEDED·링크 remap (`docs/README.md`)  
4. 구현은 별 세션 (implementation 문서 세트)

---

*Phase B Architecture **APPROVED** v3.4. 규범: Phase A + APPROVED Master/Domain.  
저장: **S3 only** · Redis 포기 · DB 없음. 로컬: **LocalStack S3**.*
