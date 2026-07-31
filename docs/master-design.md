---
title: RPDPTW Master Design
status: APPROVED
version: 1.1
date: 2026-07-30
approved_date: 2026-07-31
reorganized_date: 2026-07-31
owner: design
normative_input: docs/2026-07-30-design-interview-phase-a.md
authority: >
  본 문서는 Phase A 인터뷰 정리만을 규범 입력으로 한다.
  기존 explainer·2026-07-26 시리즈·architecture-design.md·implementation/*
  와 충돌 시 인터뷰 정리 및 본 문서가 이긴다.
language: ko
identifiers: en
related_designs:
  - docs/2026-07-31-domain-design.md
  - docs/2026-07-31-architecture-design.md
domain_status: APPROVED
architecture_status: APPROVED
supersedes_claim: >
  기존 dated master 초안·루트 master 부재 상태와 병존할 수 있다.
  SUPERSEDED 일괄 정리는 Phase C(선택) 범위이며 본 문서 작성만으로 구 문서를
  폐기 처리하지 않는다.
out_of_scope:
  - Architecture 본문 재작성 (Architecture는 별도 문서, status APPROVED)
  - implementation phases 재작성
  - 구현 코드·production cutover
  - C-17 활성화·실험 수치 확정
reorganization_note: >
  v1.1: 내용 보존 재정비. 중복 절 통합, 절 번호 재배치, 메타(상태·관련 문서·다음 액션) 정합.
  규범 의미·결정 ID(A*/D*/O*) 변경 없음.
  2026-07-31: Architecture 검수 완료 → architecture_status APPROVED.
---

# RPDPTW Master Design

## 1. 문서 지위·권위·규약

### 1.1 지위·권위·규범 입력

| 항목 | 내용 |
|---|---|
| **지위** | Master — 목표·범위·완료·핵심 결정·e2e·roadmap/gate·검증 개요 |
| **status** | `APPROVED` (2026-07-31) |
| **관련 설계** | Domain: `docs/2026-07-31-domain-design.md` (**APPROVED**). Architecture: `docs/2026-07-31-architecture-design.md` (**APPROVED**) |
| **규범 입력 (MUST)** | `docs/2026-07-30-design-interview-phase-a.md` |
| **비권위 참고** | 기존 Domain/Architecture 상세, explainer, implementation/* — 상속 후보 또는 지형 참고일 뿐 |
| **충돌 규칙** | 인터뷰 정리 §3·§4(A1–A12, D1·D2) 우선. 에이전트 신규 설계 결정 금지 |

### 1.2 문서 경계 (A6)

| 문서 | 소유 (MUST) |
|---|---|
| **Master (본 문서)** | 목표, 범위/비범위, 완료 정의, 핵심 결정, e2e 흐름, roadmap/gate, 검증·publication·benchmark 개요, migration/current vs target |
| **Domain** | 값·수식·정규화·travel·전파·평가·결과의 정확한 의미와 acceptance |
| **Architecture** | Maven module, package, port/SPI, runtime, adapter 배치, dependency 금지선 |

한 문서가 다른 문서의 의미를 단독으로 바꾸지 않는다.

#### Domain에 위임 (상세 의미)

- `Request`/pair, delivery-only, bank, propagation, evaluation, result의 **정확한 의미**
- **단일 고정 입력 계약** 필드·단위·정규화 (multi-version track 제거/비범위)
- travel·시간창·용량 등 수식·acceptance (상속 후보 재구성, D1 충돌 문장 교정)
- verifier acceptance 방향
- O2 필드 목록·optional 확장 규칙 문서화 깊이

#### Architecture에 위임 (배치·경계)

- module/package DAG, dependency 금지선
- port/adapter, profile 확장 seam
- local / worker / distributed 논리 runtime
- compute: **Lambda | ECS 미결정** (후보 병기)
- verifier 격리, optional MIP backend 격리
- AWS 등은 reference/후보, production 승인 아님

#### Master가 소유하지 않는 것

- 구체 수식, acceptance 표 전문
- Maven 좌표·package 트리
- phase별 구현 체크리스트 본문
- benchmark 수치 확정
- C-17 상세 모델·활성화 조건 수치
- compute를 Lambda(또는 ECS)로 **확정**하는 문장
- multi-version 입력 운영 체계
- 새 실험 수치·제품 확정

### 1.3 용어 표기 규약

- 본문: 한국어
- 식별자·고정 용어: 영어 유지 (`Request`, `SearchRequestBank`, `C-17`, `RM-*`, `SolvePlan` 등)
- **MUST / MUST NOT**: 규범 강제
- **GATED**: 상세 서술 가능. 명시 승인·evidence 전 **구현 착수 및 production 기본 활성화 금지**
- **DEFERRED**: 의도적으로 후속 문서·세션에 넘김
- **OPEN**: 미결정. 후보만 기술, 확정 서술 금지
- **기존 유지(미재심)**: Phase A에서 다시 열지 않은 상속 항목. D1·D2와 충돌 시 교정

---

## 2. 목표·범위·완료

### 2.1 한 문장 목표 (A1)

시스템은 단일 고정 입력 계약의 RPDPTW 입력을 읽어 immutable solve snapshot으로 문제 정의를 동결하고,  
모든 `Request`의 pickup/delivery pair와 hard constraint를 보존하는 해를 ALNS로 생성·개선하며,  
선택적으로(별도 승인 시에만) MIP 재조합을 적용한 뒤,  
**독립 verifier 두 단계를 통과한** `ASSIGNED` / `UNASSIGNED` partition과 provenance만 발행한다.

### 2.2 범위 (in scope)

- 단일 고정 canonical 입력 계약 및 (필요 시) 레거시 → canonical **adapter 하나**
- 정규화 → travel 준비 → profile 결합 → **immutable solve snapshot**
- ALNS 기반 initial portfolio → phase-1 screen → phase-2 탐색
- 후보 해·발행 결과에 대한 **독립 verifier 2단계**
- 고객 차이의 **profile ± optional** 격리
- delivery-only(CVRPTW형)를 동일 RPDPTW core에서 처리
- current vs target 분리, gate+evidence 기반 완료 정의
- Hybrid(route pool + MIP)는 **C-17 GATED TARGET** 개요만 (§6.2)

### 2.3 비범위 (out of scope / MUST NOT as completion claim)

- multi-version 입력 스키마 **병행 운영**
- route pool / MIP의 **기본 활성화** 또는 C-17 해제 단정
- round / worker / maxSteps / MIP budget 등 **실험 수치 확정**
- placeholder·orchestration demo·문서 작성 완료를 **솔버 완료 evidence**로 승격
- production cutover 승인 주장
- implementation `phases/*` 재작성 및 본 세션의 코드 구현
- Domain 수식 상세·Architecture module tree 본문 (해당 문서 소유, §1.2)

### 2.4 완료 정의 (A7)

완료는 **class 존재 · API 응답 · 문서 작성 · 단일 fixture 점수 향상**이 아니다.

완료 = **gate + evidence**의 **AND**.

목적별 gate는 서로 다르다 (종류 목록은 §8.3). 예:

| 목적 | 성격 (개요) |
|---|---|
| 일반 발행 | verifier 2단 PASS + 발행 계약 충족 evidence |
| official Win 비교 | 별도 비교 gate + 재현 가능한 benchmark evidence |
| application cutover | cutover 전용 승인·회귀·운영 evidence |
| hybrid 활성화 | C-17 승인 + hybrid 전용 evidence (GATED) |

세부 acceptance·측정 항목은 Domain / 검증 문서·implementation plan에 위임.  
Master는 **완료가 gate+evidence AND**라는 원칙만 고정한다.

---

## 3. 핵심 용어·불변조건

### 3.1 용어

| 용어 | 의미 |
|---|---|
| **단일 고정 입력 계약** | 하나의 canonical 입력 의미·단위. multi-version 병행 운영 없음 (D1) |
| **immutable solve snapshot** | 풀이 시작 직전 동결된 문제 정의 묶음(정규화 문제 + travel + profile 등). 이후 탐색은 해(candidate)만 변경 |
| **독립 verifier** | 솔버 incremental cache를 진실로 쓰지 않는 재검사. ① candidate solution ② result-integrity(발행 payload) |
| **Request / pair** | pickup+delivery 원자 운송 의무. mutation·feasibility·partition의 단위 |
| **SearchRequestBank** | 탐색 중 미배정 membership. 최종 `UNASSIGNED`·실패 사유 저장소가 아님 |
| **profile** | 고객·설정별 평가·제약·계획 조립. core의 고객명 분기 대체 |
| **GATED / C-17** | route pool + MIP hybrid 경로. 승인·evidence 전 구현·기본 활성화 금지 |
| **placeholder / current** | 데모·합성 엔진·미검증 경로. target 완료로 승격 금지 |
| **publishable result** | verifier 2단 PASS 후에만 발행 권위를 갖는 `ASSIGNED`/`UNASSIGNED` partition + provenance |

### 3.2 pair 불변조건 (A3) — MUST

1. 배정·mutation 원자 단위는 고객 한 점이 아니라 **`Request`(pair)** 이다.
2. 같은 Request의 pickup·delivery는 **같은 vehicle route**에 속하고, **pickup이 delivery에 선행**한다.
3. partial pair / cross-vehicle pair는 품질 문제가 아니라 **구조 결함**이다.
4. 탐색 중 미배정(`SearchRequestBank`)과 최종 `UNASSIGNED`를 **섞지 않는다**.

### 3.3 delivery-only (A4) — MUST

- CVRPTW형 delivery-only도 **같은 RPDPTW core**에서 처리한다.
- logical pickup은 pair 소유권에만 참여하며, **실제 정차·travel·service visit을 만들지 않는다**.
- 가짜 depot visit으로 logical pickup을 흉내 내지 **않는다**.

### 3.4 snapshot · 탐색 · verifier — MUST

- snapshot **이후** 탐색은 해(candidate)만 변경한다. 문제·travel·profile 의미를 탐색이 고치지 않는다.
- verifier PASS 전 결과는 **발행 권위가 없다**.
- candidate solution verifier와 result-integrity verifier는 솔버 cache와 **분리**된 독립 재검사다.

### 3.5 profile 격리 (A11) — MUST NOT / MUST

- 공통 route state / propagation / ALNS core에 **고객 이름 분기 금지** (MUST NOT).
- constraint, metric, score, comparator, `SolvePlan` 등은 **profile 조합**으로 격리한다 (MUST).

---

## 4. 결정 등록부

### 4.1 Phase A 의도 교정 (MUST 반영)

| ID | 결정 | 규범 |
|---|---|---|
| **D1** | 입력 계약 | **단일 고정 입력 계약**. multi-version 스키마 운영 없음. 단위(거리·시간 등)는 version으로 분/초 등을 바꾸지 않고 **전역 고정**. 고객 차이 = **profile ± optional**. 레거시 외부 포맷이 있으면 **adapter 하나**로 canonical 변환 (version 체계와 별개) |
| **D2** | Worker/API compute | 구현 후보 = **Lambda 또는 ECS**. **미결정(OPEN)**. 문서에 후보로만 쓰고 확정하지 말 것 |

### 4.2 의미 합의 (유지)

| ID | 결정 | 본문 |
|---|---|---|
| **A1** | 한 문장 목표 | §2.1 |
| **A3** | pair 불변조건 | §3.2 |
| **A4** | delivery-only | §3.3 |
| **A5** | e2e 파이프라인 | §5 |
| **A6** | Master / Domain / Architecture 경계 | §1.2 |
| **A7** | 완료 = gate + evidence AND | §2.4, §7, §8.3 |
| **A8** | ALNS-first; route pool/MIP = C-17 GATED | §6 |
| **A9** | current ≠ target; placeholder ≠ 완료 evidence | §8.4 |
| **A10** | object storage + durable orchestration + worker compute **논리 역할 분리** 유지 가능. 클라우드 선택 ≠ 알고리즘 완료 ≠ production cutover. provider SDK는 adapter/deployment 경계에만 | §5.3 |
| **A11** | profile 격리 | §3.5 |

### 4.3 상속 결정 — 기존 유지(미재심)

Phase A에서 다시 열지 않은 기존 `C-*` / `P-*` / `Q-*` / `RM-*` 등은 **전면 재승인하지 않는다** (O4).

- 상세 수식·acceptance·module tree: Domain/Architecture에서 기존 REVIEW 문서를 **상속 후보**로 재구성 가능.
- **D1·D2와 충돌하는 문장만 교정**. 그 외는 “기존 유지(미재심)”로 명시 가능.
- 본 Master는 상속 ID 목록을 전개하지 않는다. 필요 시 Domain/Architecture 및 기존 등록부 링크.

### 4.4 열린 질문 (OPEN — 확정 서술 금지)

| ID | 질문 | 상태 | Master 처리 |
|---|---|---|---|
| **O1** | Worker/API compute: Lambda vs ECS | OPEN | 후보만 (§5.3, D2) |
| **O2** | 단일 고정 계약 필드·optional 규칙 깊이 | OPEN | Domain |
| **O3** | 레거시 Win JSON adapter 공식 이름·범위 | OPEN | “adapter 하나”만 고정 |
| **O4** | 기존 C-*/Q-* 전부 재승인? | 재승인 안 함 | 충돌 시 D* 승격만 |
| **O5** | Phase C(구 문서 SUPERSEDED·깨진 링크 정리) 여부·시점 | OPEN | 본 문서 범위 밖 |
| **O6** | 상세 수식·module tree 축약 깊이 | OPEN | Domain/Architecture 세션 |

실험 수치(`Q-BENCH-02` 등), MIP budget, production sizing: **확정하지 않음**.

---

## 5. end-to-end 구현 흐름 (A5)

### 5.1 파이프라인

```text
외부 입력
  → (필요 시) adapter → 단일 고정 canonical 계약
  → 정규화
  → travel 준비
  → profile 결합
  → immutable solve snapshot          ← 문제 정의 동결
  → initial portfolio (≤8)
  → phase-1 screen ALNS
  → phase-2 ALNS
  → [optional gated] HybridPhase (route pool + MIP)
  → candidate solution verifier
  → finalization / audit
  → result-integrity verifier
  → publishable result
```

### 5.2 단계 의미 (Master 수준)

| 단계 | 규범 요지 |
|---|---|
| adapter | 레거시/외부 포맷 → **하나의** canonical 계약. multi-version 스키마 운영 아님 (D1) |
| 정규화·travel·profile | snapshot 구성 입력. 의미·단위 상세는 Domain |
| immutable solve snapshot | 문제 정의 동결. 이후 탐색이 문제/travel/profile을 변경하지 않음 |
| initial portfolio (≤8) | 초기 해 집합 상한 개념. 구체 operator·시드는 Domain/구현 계획 (수치 실험 확정 아님) |
| phase-1 screen ALNS | 스크리닝 성격 ALNS |
| phase-2 ALNS | 본 탐색 ALNS — **기본 구현·benchmark 경로** |
| HybridPhase | **C-17 GATED** (§6.2). 별도 승인·evidence 전 구현 착수·production 기본 활성화 금지 |
| candidate solution verifier | 해 구조·pair·hard constraint 등 독립 재검사 |
| finalization / audit | 발행 직전 정리·감사 정보 |
| result-integrity verifier | 발행 payload 무결성 독립 재검사 |
| publishable result | `ASSIGNED` / `UNASSIGNED` partition + provenance. verifier PASS 전 발행 권위 없음 |

### 5.3 논리 runtime 역할 (A10, D2)

| 역할 | 요지 |
|---|---|
| object storage | 입력·산출·artifact 보관 (논리) |
| durable orchestration | 작업 수명·재개·상태 (논리) |
| worker compute | 실제 solve 실행. **Lambda 또는 ECS — OPEN** |

클라우드 선택 ≠ 알고리즘 완료 ≠ production cutover 승인.  
provider SDK는 **adapter / deployment 경계**에만 둔다 (Architecture에서 배치).

---

## 6. ALNS baseline + C-17 gated hybrid (A8)

### 6.1 ALNS-first — MUST

- **기본 구현 경로** = ALNS (initial portfolio → phase-1 screen → phase-2).
- **기본 benchmark 경로** = 동일 ALNS 경로.
- 솔버 “동작·품질”의 1차 evidence는 ALNS 경로의 gate+evidence로 쌓는다.

### 6.2 C-17 Hybrid — GATED TARGET

| 항목 | 규범 |
|---|---|
| 구성 개념 | route pool + MIP 재조합 (`HybridPhase`) |
| 문서 | 목표·경계·격리 요구를 **개요 수준**으로 기술 가능 |
| 구현 착수 | **별도 승인 + evidence 전 금지** |
| production 기본 활성화 | **금지** |
| 기본 경로와의 관계 | 미승인 시 파이프라인에서 **생략**되는 optional gated 단계 |

Master는 MIP 모델·budget·pool 정책을 **확정하지 않는다**.

---

## 7. 검증·publication·benchmark

### 7.1 검증 계층

| 계층 | 역할 |
|---|---|
| 탐색 중 feasibility / score | 솔버 내부. 발행 진실이 아님 |
| candidate solution verifier | 독립. 해 구조·pair·hard constraint 등 |
| result-integrity verifier | 독립. 발행 payload·partition·provenance |
| gate+evidence | 목적별 완료 판정 (A7) |

### 7.2 publication

- publishable result만 외부 발행 권위.
- `ASSIGNED` / `UNASSIGNED` partition과 provenance.
- `SearchRequestBank` 내용을 최종 UNASSIGNED로 **직접 승격하지 않음** (A3).

### 7.3 benchmark

- 기본 경로: ALNS-first (§6.1).
- official Win 비교 등은 **별도 gate** (일반 발행 gate와 동일시하지 않음).
- 실험 수치·워커 수·step 한도 등은 **OPEN / 구현 계획 영역**. Master에서 확정하지 않음.

---

## 8. roadmap · gate · migration · 위험

### 8.1 설계 권위 로드맵

| Phase | 목표 | 산출 | 상태 |
|---|---|---|---|
| **A** | 합의·의도 교정 | `docs/2026-07-30-design-interview-phase-a.md` | **완료** |
| **B** | 설계 재작성 | Master → Domain → Architecture, **각각 사용자 검수** | **완료** — Master·Domain·Architecture **APPROVED** (2026-07-31) |
| **C (선택)** | 구 문서 SUPERSEDED·깨진 링크·README 정합 | O5 | **OPEN** |

Phase B 순서: **Master 단독 → 검수 → Domain → 검수 → Architecture**.  
한 세션에 3종을 동시에 쓰지 않는다.

### 8.2 구현 roadmap

- 세부 phase 계획·realization plan은 **implementation 문서 세트**에 위임.
- 본 Master는 구현 phase 본문을 재작성하지 않는다.
- 구현 진행률 서술 시: **accepted evidence**와 **문서 작성 완료**를 분리 (A9).

### 8.3 gate 종류 (개요만)

1. 설계 문서 gate — Master/Domain/Architecture `REVIEW` → 승인
2. ALNS 기능·품질 gate — baseline 경로 evidence
3. 발행 gate — verifier 2단 + 발행 계약
4. 비교/cutover gate — 목적별 추가
5. hybrid gate — C-17 전용 (GATED)

세부 체크리스트·측정식은 Domain 및 implementation에 링크. 완료 원칙은 §2.4.

### 8.4 current ≠ target (A9) — MUST

| current (예시 성격) | target |
|---|---|
| placeholder 엔진 (`AlnsBatchEngine` 등) | A1 목표 계약의 검증된 솔버 경로 |
| orchestration demo | durable orchestration + worker의 승인된 운영 계약 |
| 설계·implementation **문서 세트 작성 완료** | gate+evidence AND로 정의된 완료 |
| repo에 “무언가 동작” | 독립 verifier PASS 후 publishable result |

용어·문서·진행률에서 둘을 섞지 않는다.  
**placeholder / 현재 코드를 목표 솔버 완료 evidence로 서술하지 않는다.**

### 8.5 입력·compute migration

**입력 (D1)**

- 목표: 모든 지원 입력이 **단일 고정 canonical 계약**으로 정규화.
- 레거시: **adapter 하나** (이름·범위 O3 OPEN).
- multi-version 스키마를 장기 운영 전제로 두지 않음.

**compute (D2)**

- 논리 3역할(storage / orchestration / compute)은 유지 가능 (§5.3).
- compute 구현체(Lambda | ECS)는 **OPEN**. 전환 계획이 있어도 Master에서 하나를 확정하지 않음.

### 8.6 위험 (Master 수준)

| 위험 | 완화 방향 |
|---|---|
| current를 target으로 오인 | A9 용어 분리, 완료=gate+evidence |
| multi-version 입력 재유입 | D1, Domain 계약 단일화 |
| Lambda 단정으로 아키텍처 왜곡 | D2, 후보 병기 |
| C-17 조기 구현·기본 ON | GATED, 승인 전 MUST NOT |
| 고객 분기 core 오염 | A11 profile 격리 |
| verifier 생략 발행 | A1·A5, PASS 전 발행 권위 없음 |
| pair 구조 결함을 품질로 취급 | A3 구조 결함 |

---

## 9. traceability · 다음 액션

### 9.1 Phase A → Master traceability

| Phase A | Master 절 |
|---|---|
| A1 | §2.1 |
| A2 / D1 | §4.1, §5, §8.5 |
| A3 | §3.2 |
| A4 | §3.3 |
| A5 | §5 |
| A6 | §1.2 |
| A7 | §2.4, §7, §8.3 |
| A8 | §6 |
| A9 | §8.4 |
| A10 / D2 | §4.1, §5.3, §8.5 |
| A11 | §3.5 |
| O1–O6 | §4.4 |

### 9.2 다음 액션

1. ~~Master `REVIEW`~~ → **APPROVED** (2026-07-31)
2. ~~Domain `REVIEW`~~ → **APPROVED** (`docs/2026-07-31-domain-design.md`, 2026-07-31)
3. ~~Architecture 검수~~ → **APPROVED** (`docs/2026-07-31-architecture-design.md`, 2026-07-31)
4. **(선택) 다음:** Phase C — 구 문서 권위·링크 정리 (O5)
5. 구현은 별 세션 (implementation 문서 세트)

---

*문서 끝. Master Design `APPROVED` (v1.1 내용 보존 재정비). 규범 입력: `docs/2026-07-30-design-interview-phase-a.md`.*
