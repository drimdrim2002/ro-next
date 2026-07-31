---
title: RPDPTW Domain Design
status: APPROVED
version: 1.1
date: 2026-07-31
approved_date: 2026-07-31
owner: design
normative_input: docs/2026-07-30-design-interview-phase-a.md
approved_master: docs/2026-07-30-master-design.md
authority: >
  Phase A 인터뷰 정리와 APPROVED Master가 규범이다.
  기존 Domain(2026-07-26)·explainer·implementation/* 는 상속 후보/참고일 뿐,
  D1·D2 및 A1–A12와 충돌하면 채택하지 않는다.
  본 문서는 2026-07-31 사용자 검수·인터뷰를 반영하여 APPROVED 이다.
language: ko
identifiers: en
terminology_style: >
  주요 용어는 English first, 이어서 괄호로 쉬운 한국어.
  예: immutable solve snapshot (풀이용 문제 고정본)
inheritance:
  candidate: docs/2026-07-26-domain-design.md
  policy: >
    pair·정규화·travel·전파·평가·결과 등 상세 의미는 상속 후보에서 재구성.
    multi-version 입력 스키마 전제·compute=Lambda 단정은 배제(D1·D2).
out_of_scope:
  - Architecture module/package/runtime 본문
  - implementation phases 재작성·코드 구현
  - C-17 구현 착수·production 기본 활성화
  - round/worker/maxSteps/MIP budget 등 실험 수치 확정
  - production cutover 주장
---

# RPDPTW Domain Design

## 1. 문서 지위·권위·경계

| 항목 | 내용 |
|---|---|
| **지위** | Domain — 값·수식·normalization·travel·propagation·evaluation·**solution/search**·**verification/result** 의 **정확한 의미**와 acceptance 방향 |
| **status** | **`APPROVED`** (2026-07-31). 다음: Architecture |
| **규범 입력 (MUST)** | `docs/2026-07-30-design-interview-phase-a.md` |
| **상위 설계** | `docs/2026-07-30-master-design.md` (`APPROVED`) |
| **비권위 상속 후보** | `docs/2026-07-26-domain-design.md` 등 |

### 1.0 Domain 이 다루는 범위 (의도 명시)

**이 문서는 input domain 만 다루는 문서가 아니다.** (Master A6·Phase A)

| Domain 이 **의미**를 소유 | Domain 이 **안** 함 (Architecture 등) |
|---|---|
| 입력·정규화·travel·snapshot (문제 쪽) | Maven module, package, class 배치 |
| route·bank·mutation·propagation (해·물리) | port/SPI 배선, runtime/Lambda\|ECS |
| evaluation·profile·score (평가) | provider SDK, 배포 topology |
| ALNS 상태·operator **의미** (탐색) | 실험 수치 확정, 코드 구현 |
| hybrid 개념·GATED 경계 (C-17) | MIP 제품 라이선스·production ON |
| verifier·발행 result 의미 | wire JSON 최종 schema 승인(O2) |

인터뷰 초반에 §4 입력·§7–10 을 깊게 보강하다 보니 §11–13 이 상대적으로 짧아 보였을 수 있다.  
**의도상 공백이 아니라 서술 밀도 차이**이며, §11–13 도 solution/search/result domain 으로 **동등하게 의미를 적는다.**

### 1.1 용어 표기 규칙 (인터뷰 확정)

- 주요 개념: **`English term (쉬운 한국어)`**  
  예: `immutable solve snapshot (풀이용 문제 고정본)`
- 식별자·enum·필드명: 영어 유지 (`Request`, `C-17`, `reqDate`)
- 같은 용어를 문서 전체에서 이 규칙을 따른다.

### 1.2 문서 경계 (A6)

| 문서 | 역할 |
|---|---|
| **Master** | 목표·gate (관문)·e2e 개요·결정 등록부 |
| **Domain (본 문서)** | 의미·단위·invariant (불변조건)·acceptance 방향 |
| **Architecture** | module/package/port/runtime 배치. Domain 의미를 바꾸지 않음 |

### 1.3 규범어

| 표기 | 의미 |
|---|---|
| **MUST / MUST NOT** | 목표 domain contract (도메인 계약) |
| **GATED** | 승인·evidence (증거) 전 구현·기본 활성화 금지 |
| **DEFERRED** | 후속 문서 |
| **OPEN** | 미결정 |
| **기존 유지(미재심)** | Phase A에서 다시 열지 않음. D1·D2 충돌 시만 교정 |

### 1.4 current ≠ target (A9)

- **target (목표 계약)**: 이 문서가 말하는 의미  
- **current / placeholder (현재 데모·미완성 경로)**: 목표 완료 evidence가 **아님**

### 1.5 한 문장 목표 정렬 (A1)

단일 fixed input contract (고정 입력 계약) →  
`immutable solve snapshot (풀이용 문제 고정본)` →  
`Request` pair 와 hard constraint (필수 제약)를 지키는 해를 ALNS로 탐색 →  
(optional GATED) MIP →  
`independent verifier (독립 검증기)` 2단 PASS 후에만  
`publishable result (발행 가능 결과)`: `ASSIGNED` / `UNASSIGNED` + `provenance (출처 기록)`.

---

## 2. 핵심 용어·invariant (불변조건)

### 2.1 용어집

| English (쉬운 말) | 의미 | 혼동 금지 |
|---|---|---|
| **Order** (외부 주문 표현) | 연동·레거시 쪽 일감 표현 | 정규화 후 원자 단위 |
| **`Request` / pair** (운송 요청 한 건) | pickup+delivery 원자 의무 | 점 하나·고객 한 점 |
| **`NodeId` / Node** (서비스 지점 ID·정의) | pickup/delivery 역할·시간창 등 | `LocationId` 와 동일시 |
| **Visit** (경로상 실제 방문) | route 에 찍히는 정차 | bank 소속 |
| **Route** (한 대 차량 경로) | 한 vehicle 의 방문 순서 | 차종 집계 |
| **Depot** (차고·터미널) | 출발/도착 거점 | |
| **`SearchRequestBank`** (탐색 중 미배정 바구니) | 탐색 중 미배정 `RequestId` 집합 | 최종 UNASSIGNED 창고 |
| **`immutable solve snapshot`** (풀이용 문제 고정본) | **이후 변경되지 않는 문제 쪽 domain 묶음** (요청·차·장소·거리표·profile 등 freeze). 해가 아님 | `SearchSnapshot` (탐색 해), 개별 fact 조각 이름과 동일시 |
| **`prepared travel`** (미리 만든 거리·시간표) | 풀이 전 확정된 D/U | 탐색 중 즉석 계산 |
| **`profile`** (고객별 규칙·점수 묶음) | constraint/metric/score/`SolvePlan` 조합 | core 고객명 분기 |
| **`bound profile`** (이번 풀이에 묶인 설정) | solve 에 freeze 된 profile | 장기 registry 전체 |
| **`independent verifier`** (독립 검증기) | 솔버 cache 를 믿지 않는 재검사 | 탐색 중 증분 점수 |
| **`provenance`** (출처 기록) | 어떻게 만들어졌는지 | |
| **`fingerprint`** (내용 지문) | 동일성 비교용 digest | |
| **`canonical`** (정본) | 솔버가 보는 유일 입력 의미 | multi-version 병행 |
| **`adapter`** (형식 변환기) | 외부/운영 스펙 → 정본 | 두 번째 공식 스펙 |
| **hard constraint** (필수 제약) | 깨지면 불가 | soft / penalty |
| **authoritative evaluation** (정식 평가) | 수락·비교에 쓰는 정답 계산 | approximate ranking |
| **metric** (중립 지표) | 해/route 에서 잰 **측정값** (좋/나쁨 해석 전). §10.3.1 | score / 근사 순위 |
| **score** (점수 성분) | profile 이 순위에 쓰는 항 | metric 자체 |
| **approximate ranking** (근사 순위) | 후보 줄이기용 힌트 점수 | 정식 점수·metric |
| **cache** (캐시·증분 결과) | 빠른 재사용 값 | 근사 힌트와 다름 |

### 2.2 pair invariant (pair 불변조건) (A3) — MUST

1. 배정·mutation (변경) 원자 단위 = **`Request` (pair)**.
2. 같은 Request 의 pickup·delivery = **같은 vehicle route**, **pickup 선행**.
3. partial pair / cross-vehicle pair = 품질 문제가 아니라 **structure defect (구조 결함)**.
4. `SearchRequestBank` (탐색 중 미배정) 와 최종 `UNASSIGNED` 를 **섞지 않음**.

### 2.3 route–bank XOR (경로·바구니 배타) — MUST

stable search state (확정된 탐색 상태) 에서 각 `Request` 는 정확히 하나:

```text
ASSIGNED_IN_SEARCH (탐색 중 배정됨)
  = 정확히 한 route 가 RequestId 소유
    and 필요한 route 방문 완료·순서 충족
    and DELIVERY_ONLY 수요는 그 route 의 initial load (출발 전 적재)에 포함
    and RequestId ∉ SearchRequestBank

UNASSIGNED_IN_SEARCH (탐색 중 미배정)
  = 어떤 route 도 소유하지 않음
    and 해당 request 의 서비스 방문 없음
    and RequestId ∈ SearchRequestBank 정확히 한 번
```

위반 = low score 가 아니라 **implementation defect (구현 결함)**.

### 2.4 `servicePattern` (업무 패턴) (A4) — MUST

- 노드 `kind = LOGICAL | REAL` **폐기**. 구분은 **`servicePattern` only**.

| `servicePattern` | 의미 |
|---|---|
| **`DELIVERY_ONLY`** | route 방문은 delivery 만. pickup 쪽은 pair 소유 + `initial load (출발 전 적재)` 만. travel/stop/service visit **없음** |
| **`PICKUP_DELIVERY`** | pickup·delivery **둘 다** route 방문. 시간창·서비스 시간 **각각** 입력 |

- 가짜 depot visit 으로 픽업 흉내 **금지**.
- (역사) “logical pickup” = `DELIVERY_ONLY` 의 route 비방문 pickup 쪽. **필드/enum 으로 쓰지 않음**.

```text
initialLoad(route) = Σ demand of assigned DELIVERY_ONLY requests
DELIVERY_ONLY delivery visit     → load 감소
PICKUP_DELIVERY pickup visit     → load 증가
PICKUP_DELIVERY delivery visit   → load 감소
```

중간 depot 재상차 금지. final load = 0 (미완료 pair 로 우회 금지).

### 2.5 snapshot 이후 (A5) — MUST

`immutable solve snapshot` 이 만들어진 뒤:

- 그 안에 든 **문제 쪽 domain 은 일절 변경하지 않음** (읽기 전용).
- 탐색은 **solution candidate (해 후보)** — route, bank, 배정 — 만 변경.
- 문제 정의·`prepared travel`·`bound profile` 의미를 탐색이 고치지 않음.

상세·혼동 방지: **§7.2**.

### 2.6 정식 점수·cache·근사·검증 (인터뷰 확정) — MUST

| 구분 | 규칙 |
|---|---|
| **동일 해 + authoritative evaluation (정식 평가)** | `cache` 있든 없든 **같은 값**. 다르면 **bug (버그)** |
| **approximate ranking (근사 순위)** | shortlist (후보 축소) 용. 정식 점수와 달라도 됨. 수락·비교·발행에 **쓰지 않음** |
| **탐색** | 많은 trial (시도). 난수·휴리스틱으로 **다른 이웃**을 봄. trial 마다 independent verifier 를 돌리지 **않음** |
| **independent verifier** | 주로 **발행 전**, 최종 후보 해 전체 + 발행 payload. 솔버 cache 비신뢰 |

```text
[ALNS 다수 trial]  cache/증분 가능 → 수락/거절
        ↓
[최종 후보 해]
        ↓
candidate solution verifier (해 독립 검증)  — 해 전체
        ↓
finalization (발행 준비)
        ↓
result-integrity verifier (결과 묶음 검증)
        ↓
publishable result
```

규모 참고: route ~30 × 방문 ~20+ 에서 끝 1~2회 전체 검증은 보통 **병목 아님** (병목은 탐색).

---

## 3. 처리 흐름 (Domain 관점)

```text
외부 입력
  → (필요 시) adapter (형식 변환기) → canonical (정본)     ← D1
  → normalization (정규화)
  → travel preparation (이동 준비)
  → profile binding (설정 결합)
  → immutable solve snapshot (풀이용 문제 고정본)
  → initial portfolio (초기 해 묶음; 개수·구성은 예시·OPEN)
  → phase-1 screen ALNS
  → phase-2 ALNS
  → [optional gated] HybridPhase (route pool + MIP)       ← C-17
  → candidate solution verifier (해 독립 검증)
  → finalization / audit (발행 준비·감사)
  → result-integrity verifier (결과 묶음 검증)
  → publishable result (발행 가능 결과)
```

| 층 | 하는 일 | MUST NOT |
|---|---|---|
| 입력·정규화·travel | 정본 의미·단위·prepared arc | multi-version 스키마 병행 |
| snapshot | 문제 동결 | 탐색 중 문제 재해석 |
| propagation (전파) | load/time 등 **물리 사실**·hard feasibility | 가격·최종 결과 결정 |
| evaluation / profile | metric → constraint → score → comparator → `SolvePlan` | hard 를 finite penalty 로 상쇄 |
| ALNS | operator·trial·acceptance | 발행 권위 |
| Hybrid (GATED) | pool + MIP 개요 | 미승인 기본 ON |
| verifier | cache-free 재검사 | 솔버 cache 를 진실로 사용 |
| publication | ASSIGNED/UNASSIGNED + provenance | PASS 전 발행 |

Architecture 배치: DEFERRED.

---

## 4. fixed input contract (고정 입력 계약) (D1)

### 4.1 원칙 — MUST

| 원칙 | 규범 |
|---|---|
| **single canonical (정본 하나)** | solver·verifier 공유 의미는 **하나** |
| **no multi-version** | version 으로 schema/단위 병행 운영 금지 |
| **단위 전역 고정** | 거리·시간·무게·부피 scale 전역 고정 |
| **고객 차이** | `profile` ± optional 필드 (core 고객명 분기 금지) |
| **legacy** | 외부 → 정본 = **`adapter` 하나** (이름 O3 OPEN) |

`profile key` = 설정 식별. 입력 schema multi-version 이 **아님**.

### 4.2 Plan envelope (한 번 풀이 입력 봉투)

최소 의미:

```text
plan identity (계획 ID)
planStart / planEnd (계획 기간)
depot(s) (차고 정보)
orders 또는 requests (일감)
vehicles (차량; startDepot; end·소유 등은 optional)
travel input (이동 입력)
trips policy (편도/복귀 등)
waitInDepot (차고 대기 여부)
global route-resource limits (optional, 전역 한도)
customer profile identity (어떤 profile 쓸지)
objective preset (목표 프리셋; 생략 시 profile 기본)
```

- 시각 문자열: timezone 없는 `yyyy-MM-dd HH:mm:ss` (기존 유지·미재심). timezone 은 solver 밖.
- plan 기간: `[planStart, planEnd)` (끝 미포함).

**Depot 범위 (인터뷰)**

| 단계 | 내용 |
|---|---|
| **현재** | 차량별 **start depot (출발 차고)**. **end depot optional** (있으면 start 와 달라도 됨). **소유 optional** |
| **추후 가능** | (2) plan depot 목록 선택 · (3) 본격 multi-depot |
| **추후** | 현재 필수 아님 |

wire 이름·optional 깊이: **O2 OPEN**. 본 절은 의미 목록 (공개 JSON schema 승인 아님).

### 4.3 `Request` / item

**`servicePattern` only** (`kind` 없음).

| `servicePattern` | route 방문 | 시간 입력 |
|---|---|---|
| `DELIVERY_ONLY` | delivery 만 | delivery 시간창·서비스 + **delivery `reqDate`** |
| `PICKUP_DELIVERY` | pickup + delivery | 시간창·서비스·**`reqDate` 를 pickup·delivery 각각** |

**가상 데이터 (Request domain 컬럼)**

| 컬럼 | row1 `DELIVERY_ONLY` | row2 `PICKUP_DELIVERY` |
|---|---|---|
| `requestId` | `R-DO-01` | `R-PD-02` |
| `servicePattern` | `DELIVERY_ONLY` | `PICKUP_DELIVERY` |
| `pickup.locationId` | — (route 방문 없음) | `LOC-BUNDANG-WH3` |
| `pickup.openTime` | — | `2026-08-01 09:00:00` |
| `pickup.closeTime` | — | `2026-08-01 12:00:00` |
| `pickup.duration` | — | 5분 |
| `pickup.reqDate` | — | `2026-08-01 10:00:00` |
| `pickup.zoneId` | — | `경기` |
| `delivery.locationId` | `LOC-GANGNAM-100` | `LOC-GANGNAM-200` |
| `delivery.openTime` | `2026-08-01 13:00:00` | `2026-08-01 14:00:00` |
| `delivery.closeTime` | `2026-08-01 18:00:00` | `2026-08-01 18:00:00` |
| `delivery.duration` | 10분 | 10분 |
| `delivery.reqDate` | `2026-08-01 15:00:00` | `2026-08-01 16:00:00` |
| `delivery.zoneId` | `서울` | `서울` |
| `delivery.vehicleFeatureList` | `["1T"]` | `["1T","2.5T"]` |
| `items[]` | 박스A×2 (5kg, taskTime 2분) | 박스B×1 (10kg, taskTime 3분) |
| route 실제 방문 | delivery 1곳 | pickup 1 + delivery 1 |

```text
serviceTime = duration + Σ(item.taskTime × item.qty)   // 방문(쪽)별 duration 규칙 적용

// 고객 요청 시각이 있는 각 서비스 방문마다 (pickup · delivery 둘 다):
serviceStartTime <= reqDate
// serviceEndTime 은 이 조건에 넣지 않음 (인터뷰 정정)
```

**`reqDate` (고객 요청 시각) — 인터뷰 교정**

| 항목 | 규범 |
|---|---|
| 정본 | 방문 쪽 필드 **`reqDate`**. legacy `dueDate` → 해당 쪽 `reqDate` 로 흡수 |
| 의미 | 고객이 원하는 **요청 시각**. 의도는 **`serviceStartTime` (서비스 시작)** 쪽 |
| 제약 | 그 방문에 대해 **`serviceStartTime <= reqDate`** 만. **`serviceEndTime` 은 조건에서 제외** |
| **적용 범위** | **`DELIVERY_ONLY`**: delivery 쪽. **`PICKUP_DELIVERY`**: **pickup 과 delivery 둘 다** (각각 자신의 `reqDate`) |
| MUST NOT | release / “이후 배송” 해석; 한쪽 `reqDate` 로 다른 쪽을 대체하지 않음; 예전 `… <= serviceEndTime` 이중 상한으로 단정하지 않음 |

- 레거시가 request 당 `reqDate` 하나면 adapter 가 pickup/delivery 로 어떻게 나누는지는 **O2/adapter** (의미: 정본은 쪽별 가능).
- `duration` ≠ `item.taskTime`. order-level `taskTime` 단독 = input error (기존 유지·미재심).

### 4.4 Vehicle (차량)

**공통에 가까움**

```text
vehicleId
maxWeight / maxVolume
vehicleFeature (차급 코드 하나)
workStart / workEnd
speed
maxStopCnt
maxDriveTime / maxDriveDist
startDepot (출발 차고)
```

**Optional** (특정 고객·케이스; 없으면 그 축 미적용)

| 필드 | 의미 |
|---|---|
| `maxWidth` / `maxHeight` / `maxLength` | 치수 한도. 없으면 치수 제약 없음 |
| `capabilities` | 특수 능력. 정의할 때만 |
| `zoneIds` (복수) | 갈 수 있는 구역. **미입력 = 전 구역**. 여러 개 가능 |
| **`endDepot`** | 도착 차고 optional. 있으면 start 와 달라도 됨. 없으면 도착 고정 없음 |
| **`vhclOwnTyp` (소유)** | optional. 있으면 `DIRECT` \| `LEASE`. **미입력 = 소유 축 미사용** (DIRECT 로 숨은 채움 **금지**) |

값이 있을 때 `vhclOwnTyp`: exact `DIRECT`/`LEASE` only. 그 외 non-empty = error.

### 4.5 Depot · trip · terminal (차고·운행 형태)

- depot 여러 개 가능.
- **start depot** 있음. **endDepot optional** (start ≠ end 허용).
- end 없음 → 마지막 고객 종료 등.
- multi-trip: 현재 비범위. `multiRotation != 0` (non-oneway) → `UNSUPPORTED_INPUT` (기존 유지·미재심).
- `depot.taskTime` → 시간 전파 미적용. 회차 간은 `depot.duration` (최초 출발·마지막 복귀 미적용).
- trips 와 end 동시 지정 우선순위: **O2 OPEN**.

### 4.6 `adapter` (형식 변환기) (D1, O3)

**대표 쓰임:** 운영 중 시스템 입력 스펙 ≠ 지금 정본이어도  
`adapter` 로 정본에 맞춘 뒤 솔버에 넣는다.

```text
[운영/레거시 입력] → adapter → [canonical 정본] → solver · verifier
```

- adapter **사라지지 않음**. multi-version 없음 ≠ adapter 금지.
- 변환 경로 **하나**로 모음. 솔버는 정본만.
- 이름·범위 **O3 OPEN**.
- 표현 불가 업무는 adapter 만으로 불가. 애매 값 추측 금지.

---

## 5. normalization (정규화)

정규화 결과 = solver·verifier 가 공유하는 **유일 의미**.  
unit·rounding·order 는 `fingerprint (내용 지문)` 에 포함.  
policy “version” 필드 ≠ 입력 multi-track (D1).

### 5.1 수치·단위 — 전역 고정 (D1)

| 차원 | 외부 | 내부 |
|---|---|---|
| 무게 | decimal kg, n=3 FLOOR | long, kg×1000 |
| 부피 | decimal CBM, n=3 FLOOR | long, CBM×1000 |
| 비용 | integer | checked integer |
| 거리 | integer meter | integer meter |
| 시간 | integer second | long second |
| 수량 | positive integer | integer |

- double 선근사 금지. item-first 합산. 소수 거리·시간 거부.
- 누락 limit = **constraint absent (제약 없음)**, 큰 수 sentinel 금지.

### 5.2 time · service window (시간·서비스 창)

```text
origin = planStart
normalizedTime = origin 기준 초
```

- Plan `[planStart, planEnd)`. window open/close inclusive.
- `serviceStartTime = max(arrival, openTime)` 등 (기존 유지·미재심).  
  `serviceEndTime = serviceStartTime + serviceTime` (동일 방문).
- `reqDate` 제약: 방문마다 **`serviceStartTime <= reqDate`** 만 (§4.3). `serviceEndTime` 미사용.  
  `PICKUP_DELIVERY` 는 **pickup·delivery 양쪽** 각각. 고객 요청 시각은 service start 의도.
- `waitInDepot` N/Y (기존 유지·미재심).

### 5.3 size · capability · zone

```text
sizeCompatible
  = vehicle.vehicleFeature ∈ request.vehicleFeatureList
    OR list == ["ALL"]

capabilityCompatible
  = request 가 capability 미요구
    OR required ⊆ vehicle.capabilities

vehicleZoneOk
  = zoneIds 미입력 → 전 구역 가능
    OR 방문 zone ∈ vehicle.zoneIds
```

- vehicle `zoneIds` 복수. 없으면 전 구역.
- optional 치수 없으면 그 축 생략.
- Size/capability/zone/치수 = 독립 hard, **AND**.

---

## 6. travel preparation (이동 준비)

Key = **`LocationId` (장소 ID)** (`NodeId` 아님 — 거리표는 장소 기준).

| Field | 의미 |
|---|---|
| `D` | directed 거리 (integer meter) |
| `U` | directed 시간 (integer second) |
| `C` | non-authoritative — feasibility/score 에 사용 금지 |

- 소수 D/U 거부. self arc D=0,U=0.
- Missing D: Great Circle + HALF_UP (기존 유지·미재심).
- Missing U: `ceil(D×3.6/speedKmH)`; speed 없으면 45 (기존 유지·미재심).
- solver·**양쪽 verifier** = `prepared travel` only. 탐색 중 lazy 계산·대칭화 금지.

---

## 7. immutable solver model · snapshot (풀이 모델·문제 고정본)

### 7.1 Identity (식별자)

이름은 짧게. 접두어 `Solver` / `Physical` **쓰지 않음** (인터뷰 확정).

| ID | 쉬운 말 | 무엇 | 용도 |
|---|---|---|---|
| **`RequestId`** | 요청 ID | 일감(`Request`) 한 건 | 배정·bank·ASSIGNED/UNASSIGNED |
| **`VehicleId`** | 차량 ID | 차 한 대 | 어느 차의 route 인지, 용량·depot 연결 |
| **`NodeId`** | 서비스 지점 ID | 그 장소에서 하는 **역할 한 칸** (픽업/배송/터미널 등) | route 방문 순서, 시간창·서비스, pair 의 pickup/delivery 연결 |
| **`LocationId`** | 장소 ID | 지도상 **한 장소** | `prepared travel` 거리·시간 표 키, 좌표 |

- 같은 `LocationId` 라도 pickup / delivery / terminal 은 **`NodeId` 를 합치지 않음**.
- 각 `Request` → pickup `NodeId` + delivery `NodeId` 개념.  
  `DELIVERY_ONLY` 의 pickup 쪽 = route 방문 없음, `initial load` 소속만.
- 이동 조회: `LocationId` 쌍. 경로·시각: `NodeId` 순서.

```text
예: Request R2
  RequestId = R2
  pickup  NodeId = N1 → LocationId = LOC-WH
  delivery NodeId = N2 → LocationId = LOC-CUST
이동 N1→N2 시간 = prepared travel[ LOC-WH → LOC-CUST ]
```

### 7.2 `immutable solve snapshot` (풀이용 문제 고정본) — MUST

#### 7.2.1 한 줄 정의 (강조)

> **`immutable solve snapshot` = 풀이 시작 직후에 *변경되지 않는* 문제 쪽 domain 들의 묶음(freeze bundle).**  
> 해가 아니다. 탐색이 만지는 route / bank / 배정이 아니다.

- **immutable (불변):** snapshot 생성·봉인 **이후** 내용 수정 금지 (읽기 전용).
- **묶음:** 클래스 하나일 필요는 없음. **함께 얼려야 하는 domain 조각들의 논리 단위**.
- **문제 쪽:** “무엇을 어떤 규칙·거리표로 풀지”. “누가 어떤 순서로 갔는지”는 **§8 solution**.

```text
[ 변경 안 됨 — snapshot 안 ]
  Request 정의, Vehicle, Node, Location,
  prepared travel, bound profile, policies, fingerprints …

[ 변경됨 — snapshot 밖 · 해 쪽 ]
  Route 방문 순서, SearchRequestBank, 배정, SearchSnapshot, …
```

#### 7.2.2 왜 “묶음”인가

정규화·travel·profile 을 따로 두면, 탐색 중 일부가 바뀌거나 버전 불일치가 날 수 있다.  
snapshot 은 다음을 **한 번에 봉인**한다.

| 묶음에 포함 (문제 domain) | 쉬운 말 | 봉인 후 |
|---|---|---|
| dense IDs ↔ external mappings | 연속 번호 ↔ 바깥 ID | 고정 |
| requests / nodes / vehicles | 일감·서비스 지점·차 **정의** | 고정 |
| locations (`LocationId`) | 장소 | 고정 |
| `prepared travel` | 거리·시간표 | 고정 |
| numeric / time / service policies | 단위·시간 해석 규칙 | 고정 |
| compatibility facts | 차급·zone 등 호환 사실 | 고정 |
| `bound profile` + dependencies | 이번 풀이 제약·점수 설정 | 고정 |
| `provenance` / `fingerprints` | 출처·내용 지문 | 고정 |

생성 시: ID 짝·travel 완전성·pair 참조 등 검증 후 **닫음**.  
닫힌 뒤 탐색·ALNS·hybrid 가 위 표를 **다시 쓰지 않음**.

#### 7.2.3 혼동 금지

| 이것 | snapshot 과 다름 |
|---|---|
| **`SearchSnapshot` (탐색 중 확정 해)** | **해** 쪽. route·bank·평가가 들어감. trial 수락 때마다 **바뀔 수 있음** |
| **개별 Request / Vehicle 객체** | snapshot **안**의 조각. 조각 이름 ≠ 묶음 전체 |
| **`ProblemInstance` (문제 인스턴스, 상속 후보 이름)** | 보통 문제 **본체** 쪽 타입 후보. snapshot 은 본체 + travel + profile 등 **더 넓은 봉인 단위**일 수 있음 |
| **ProblemFact (문제 사실, 일반/Opta 용어)** | “풀이 중 안 바꾸는 재료 조각” 뉘앙스. 이 문서 **공식 용어 아님**. 그 조각들이 모인 **봉인 묶음** = snapshot |
| **authoritative evaluation cache** | 같은 해의 빠른 재계산. 문제 정의를 바꾸는 것이 아님. 문제 쪽 수정에 쓰면 안 됨 |

#### 7.2.4 생명주기

```text
입력 → adapter → normalization → travel preparation → profile binding
        │
        ▼  검증 통과 시 freeze
  immutable solve snapshot  생성  ← 이 시점 이후 문제 쪽 불변
        │
        ├─ ALNS / (GATED) hybrid  →  해만 변경 (읽기: snapshot)
        ├─ candidate solution verifier  →  snapshot + 해 를 재검
        └─ 새 입력으로 다시 풀이  →  새 snapshot (이전 봉인을 고치지 않음)
```

MUST:

1. snapshot 이후 **문제 쪽 domain mutation 금지**.  
2. 탐색은 **solution 쪽만** mutation.  
3. verifier·평가도 snapshot 의 travel/profile/요청 정의를 **다른 버전으로 바꿔 쓰지 않음**.  
4. fingerprint 로 “어떤 문제 봉인 위에서 나온 해인지” 추적 가능해야 함.

#### 7.2.5 기타 규칙 (기존·인터뷰)

- 호환 차량 0 = 구조 error 아닐 수 있음 → unassign evidence + bank 가능 (기존 유지·미재심).  
- Missing limit = absent. start depot 있음; end·소유 optional. multi-trip 비범위.  
- Java public 클래스 하나인지는 Architecture. Domain 은 **불변 묶음 의미**만 강제.

---

## 8. solution state · bank · mutation (해 상태·바구니·변경)

> §7 `immutable solve snapshot` = **안 바뀌는 문제 묶음** (일감 정의·거리표·profile…).  
> §8 = **바뀌는 해 상태** (누가 어떤 순서로 다니고, 뭐가 아직 안 넣었는지).  
> 둘 다 “snapshot”이 들어가도 **다른 것**이다.

---

### 8.0 한 장 그림 + 구체 예

문제 쪽(§7)은 이미 고정됐다고 가정:

```text
Request R1 = DELIVERY_ONLY, delivery @ 강남100, demand 10kg
Request R2 = PICKUP_DELIVERY, pickup @ 분당창고, delivery @ 강남200, demand 10kg
Vehicle V1, startDepot=차고A, endDepot 없음(optional)
```

**해 상태 예시 하나** (`SearchSnapshot` 감각):

```text
Route of V1:
  차고A 출발
  → R2 pickup @ 분당창고
  → R1 delivery @ 강남100
  → R2 delivery @ 강남200
  → (end 없음 → 마지막 고객에서 종료)

SearchRequestBank = { }   // 둘 다 경로에 있음 → 바구니 비움

(파생) 각 방문 도착시각·load·총거리·점수 … 는 아래 SoT에서 계산
```

**다른 해 상태 예** (R1 아직 미배정):

```text
Route of V1:  차고A → R2픽 → R2배
SearchRequestBank = { R1 }   // R1 만 바구니에
```

ALNS 한 스텝 = “이런 해 상태를 **조금 바꿔** 더 좋은 해를 찾아보기”.

---

### 8.1 한 route 가 “괜찮다”는 것 — stable route invariant — MUST

**Route (한 대 경로)** = 특정 `VehicleId` 하나에 묶인 **방문 순서**.

| 규칙 | 구체 예로 보면 |
|---|---|
| 차량 정확히 하나 | 이 목록은 V1 것. V1+V2 섞인 한 route 없음 |
| start / optional end | V1 은 차고A에서 출발. endDepot 있으면 마지막에 그 차고 |
| `servicePattern` | R1 은 배송 방문만 경로에 있음 (픽업 가짜 방문 없음) |
| pair 완전·같은 차 | R2 픽업·배송이 **둘 다 V1** 에 있고, **픽업이 배송보다 앞** |
| load | 모든 구간에서 0 ≤ load ≤ capacity. DELIVERY_ONLY 는 출발 시 initial load 에 포함 |
| 이동 | `prepared travel` 만 사용 (좌표 즉석 계산 금지) |
| 시간·자원 hard | 시간창·근무시간·maxStop 등 깨면 이 경로는 불가 |
| 중간 차고 재방문 | 지금 범위에서 금지 |

**깨진 예 (구조 결함 — 점수 나쁨이 아님)**

```text
나쁜 예 1: R2 pickup 은 V1, R2 delivery 는 V2     → cross-vehicle pair
나쁜 예 2: 경로에 R2 delivery 만 있고 pickup 없음 → partial pair
나쁜 예 3: R2 배송이 픽업보다 앞                 → precedence 위반
나쁜 예 4: R1 이 V1 경로에도 있고 bank 에도 있음 → route–bank XOR 위반
```

---

### 8.2 `SearchRequestBank` (탐색 중 미배정 바구니) — MUST

| | 내용 |
|---|---|
| **무엇인가** | “아직 **어느 route 에도 안 넣은** `RequestId` 집합” |
| **저장하는 것** | ID 만 (들어 있나 / 없나) |
| **저장하지 않는 것** | 실패 사유, 마지막 삽입 에러 메시지, 최종 고객용 UNASSIGNED 코드, 비용 |

**XOR 규칙 (같은 순간)**

```text
R 가 어떤 route 소유  →  bank 에 없음
R 가 bank 에 있음     →  어떤 route 도 소유하지 않음
둘 다 또는 둘 다 아님 → defect
```

**구체**

```text
시점 A:  bank = { R1, R2 }     // 둘 다 미배정, 경로 비움 가능
시점 B:  V1 이 R2 만 운행      // bank = { R1 }
시점 C:  V1 이 R1·R2 모두      // bank = { }
```

**발행과 섞지 말 것**

```text
SearchRequestBank     = 탐색 중 “아직 안 넣음”
최종 UNASSIGNED       = verifier 통과 후 “결과로 미배정 확정” (+ 사유·provenance)
```

bank 내용을 그대로 고객 결과의 UNASSIGNED 로 복사하는 것은 **금지**.  
(탐색 끝 bank → finalization·verifier 를 거쳐야 함.)

---

### 8.3 source of truth vs derived (진실 vs 파생) — MUST

해 상태를 두 층으로 나눈다.

#### Source of truth (SoT, 진실 — 이게 바뀌면 해가 바뀐 것)

| SoT | 구체 예 |
|---|---|
| route 의 **방문 순서** | V1: [R2픽, R1배, R2배] |
| **누가 어떤 Request 소유** | R2→V1, R1→V1 |
| **vehicle / terminal 묶임** | 이 route = V1, start=차고A |
| **`SearchRequestBank`** | { } 또는 { R1 } |

#### Derived (파생 — SoT 에서 **다시 계산** 가능)

| Derived | 구체 예 |
|---|---|
| 각 방문 arrival / serviceStart / departure | “분당창고 09:40 도착” |
| load 곡선 | 출발 10 → 픽업 후 20 → … |
| 총 거리·주행시간·점수 | totalDistance=52.3km, score=… |
| 삽입 후보 표, fingerprint | 다음 trial 용 캐시 |

**규칙**

1. SoT 를 바꾸면 (예: R1 을 경로에서 빼 bank 로) → 관련 derived **무효화** 후 다시 계산.  
2. **같은 SoT** 인데 cache 점수 ≠ 처음부터 다시 계산한 점수 → **bug**.  
3. approximate ranking (근사 순위) 은 derived 정식 점수가 **아님** (후보 줄이기용).

```text
예: V1 경로에서 R1 제거
  SoT:  순서에서 R1배 삭제, bank 에 R1 추가
  Derived:  옛 “총거리 50” cache 를 그대로 쓰면 안 됨 → 재계산
```

---

### 8.4 mutation 과 COW trial (변경·복사 후 시도) — 기존 유지·미재심

탐색은 확정 해를 **제자리에서 마구 고치지 않는다**.  
바뀌는 부분만 복사해 시험하고, 괜찮으면 새 확정 해로 올린다.

#### 한 trial 의 구체 시나리오

```text
[확정 해 current]
  V1: [R2픽, R1배, R2배]
  bank: {}

[Destroy] R1 을 빼 보자
  → draft 복사
  V1: [R2픽, R2배]
  bank: { R1 }

[Repair] R1 을 다른 위치(또는 상태)로 다시 넣어 보자
  → draft 수정 후
  structural check (pair·XOR·…)
  → authoritative evaluation (정식 평가: 시간·용량·점수)
  → 더 좋으면 accept → 새 current
  → 아니면 discard draft (current 유지)
```

| 이름 | 쉬운 말 | 확정 해? | 비고 |
|---|---|---|---|
| **`TrialDraft`** | 시도 중 임시 해 | 아니오 | current 를 직접 덮지 않음 |
| **`CompletedTrial`** | 평가까지 끝난 시도 | 아직 | accept/reject 대기 |
| **`SearchSnapshot`** | 탐색에서 확정된 해 | **예** | routes+bank+평가 지문 등 |
| **`VerifiedSolution`** | 독립 검증 통과 해 | **예** | 발행 직전 단계 (§13) |

- `current` / `stageBest` / `solveBest` 는 **서로 다른** 확정 해 스냅샷일 수 있음. 한 객체를 공유 alias 하지 않음.  
- apply/undo 로 한 경로를 뜯어고치는 방식은 **기본 경로 아님** (COW 가 기본, 기존 유지·미재심).

---

### 8.5 상태 이름 정리 (한눈에)

```text
immutable solve snapshot     §7  문제 고정 (안 바뀜)
        │
        ▼ 탐색
TrialDraft                   시도 중 (임시)
        │ accept
SearchSnapshot               탐색 확정 해 (바뀜 가능: 다음 trial 의 기준)
        │ candidate verifier PASS
VerifiedSolution             검증된 해
        │ result-integrity PASS
publishable result           고객/연동에 내보내는 결과
```

---

### 8.6 §8 체크리스트 (구현·리뷰용)

| # | 질문 | 기대 |
|---|---|---|
| 1 | 문제 정의(거리표·요청 창)를 trial 이 바꿨나? | 아니요 → 그건 snapshot 위반 |
| 2 | 각 Request 가 route 또는 bank **정확히 하나**? | 예 |
| 3 | pair 가 한 차·픽업 선행? | 예 |
| 4 | bank 에 사유 문자열을 저장하나? | 아니요 |
| 5 | 경로만 바꿨는데 옛 점수로 비교하나? | 아니요 (derived 무효화) |
| 6 | draft 실패 시 current 가 오염됐나? | 아니요 (discard) |

---

## 9. route propagation (경로 전파)

### 9.0 한 줄

**Route 방문 순서가 이미 정해져 있을 때**, 앞에서 뒤로 한 번 훑으며  
**도착·대기·서비스·적재·주행** 같은 **물리 사실**을 계산하는 단계.

| 한다 | 안 한다 |
|---|---|
| “이 순서면 몇 시에 도착하나, load 는?” | “점수가 좋은가, 어떤 고객 목표인가” |
| hard 가능/불가 (시간창·용량 등) 재료 | `profile` 가격·objective 우선순위 |
| SoT(방문 순서) → derived 시각·load | 문제 정의(snapshot) 수정 |

§8 의 **derived** 를 채우는 대표 엔진이 propagation 이다.

---

### 9.1 한 방문에서 하는 일 (루프)

각 고객 방문(및 end depot 이 있으면 마지막 복귀)마다 대략:

```text
1. 이전 출발 시각 + prepared travel → 이번 장소 arrival (도착)
2. openTime 전이면 대기 → serviceStartTime = max(arrival, openTime)
3. serviceEndTime = serviceStartTime + serviceTime
4. (있으면) reqDate: **serviceStartTime ≤ reqDate** (`serviceEndTime` 조건 없음)
5. load 갱신 (픽업 +, 배송 −)
6. 용량·시간창·근무시간 등 hard 검사
7. 다음 구간으로 departure = serviceEndTime (정책에 따라)
```

**전체 route:** start depot 상태 → 방문1 → 방문2 → … → (optional end depot).

중간에 “내일 이어서 운전” 식 pause/resume 분할은 금지 (work window 는 arc 전체가 한 window 에 들어가야 함 — §5.2).

---

### 9.2 구체 숫자 예

가정 (단순화, 단위 분·kg):

```text
V1 capacity = 30kg
R1 DELIVERY_ONLY demand 10kg, delivery 창 13:00–18:00, service 14분, reqDate 15:00
R2 PICKUP_DELIVERY demand 10kg
  pickup  창 09:00–12:00, service 5분,  reqDate 10:00
  delivery 창 14:00–18:00, service 13분, reqDate 16:00

경로: 차고A(08:00 가능) → R2픽 → R1배 → R2배
travel: 차고→픽 40분, 픽→R1배 50분, R1배→R2배 20분
```

| 단계 | 시각 감각 | load |
|---|---|---|
| 차고A 출발 | 08:00 (예) | **initialLoad = 10** (R1 DELIVERY_ONLY) |
| 이동 40분 | | 10 |
| R2 pickup 도착 | 08:40 | 10 |
| 창 09:00까지 대기 | serviceStart 09:00 | 10 |
| 서비스 5분 | serviceEnd 09:05 | 픽업 후 **20** |
| 이동 50분 | | 20 |
| R1 delivery 도착 | 09:55 | 20 |
| 창 13:00까지 대기 | serviceStart 13:00 | 20 |
| 서비스 14분 | serviceEnd 13:14 | 배송 후 **10** |
| reqDate 15:00 | **13:00 ≤ 15:00** → **통과** (`serviceEnd` 는 조건 아님) | |

실패 예 (같은 규칙): serviceStart 가 **16:00** 이고 reqDate 가 **15:00** 이면  
`16:00 ≤ 15:00` 이 거짓 → **실패** (서비스 시작이 고객 요청 시각보다 늦음).

propagation 은 “이 순서면 통과/실패”를 **사실로 알려 줄 뿐**, 점수로 덮지 않는다.

**load 규칙 요약**

```text
출발 initialLoad = Σ (경로에 배정된 DELIVERY_ONLY demand)
PICKUP_DELIVERY pickup visit   → +demand
PICKUP_DELIVERY delivery visit → −demand
DELIVERY_ONLY delivery visit   → −demand
모든 prefix: 0 ≤ load ≤ capacity
최종 load = 0
```

---

### 9.3 기록하는 값 (policy-neutral facts)

가격·고객 선호가 **아닌** 물리·운영 사실 (최소):

| 필드 | 쉬운 말 |
|---|---|
| `arrival` | 도착 시각 |
| `serviceStartTime` / `serviceEndTime` | 서비스 시작·끝 |
| `loadWeight` / `loadVolume` | 그 시점 적재 |
| `distance` / `driveTime` | 실제 주행 거리·시간 |
| `customerWaitingTime` | 고객 앞에서 연 시각 대기 |
| `depotWaitingTime` | 차고 대기 |
| `serviceTime` | 작업 시간 |
| `interWorkWindowRestTime` | 근무창 사이 휴식 |
| `routeOperationalTime` | 운행 관련 시간 합 |
| `stopCount` | 정차 횟수 (규칙 아래) |

```text
routeOperationalTime
  = driveTime
  + customerWaitingTime + depotWaitingTime
  + serviceTime + interWorkWindowRestTime
```

Verifier 가 같은 공식으로 **다시 합산**할 수 있어야 한다.

---

### 9.4 stopCount (정차 수)

```text
직전 고객 서비스 location 과 이번 고객 서비스 location 이 다르면 stopCount += 1
```

- start/end/internal depot 는 고객 stop 에 안 넣음.  
- `DELIVERY_ONLY` 의 route 비방문 pickup 쪽은 stop 아님.  
- 같은 장소 연속 방문이면 첫 진입만 증가하는 식 (기존 유지·미재심).  
- vehicle `maxStopCnt` 와 전역 한도가 둘 다 있으면 `min`.

---

### 9.5 drive (주행)

```text
driveDist = Σ 실제 지난 D (meter)
driveTime = Σ 실제 지난 U (second, vehicle-resolved)
```

포함: depot→첫 고객, 고객→고객, end depot 있으면 마지막→depot.  
제외: waiting, service, inter-work-window rest.

---

### 9.6 propagation 이 아닌 것

| 하지 않음 | 어디로 |
|---|---|
| 점수·objective·mandatory 우선순위 | §10 evaluation / profile |
| 근사 순위로 위치 고르기 | ALNS shortlist |
| 문제·거리표 수정 | §7 snapshot 위반 |
| “거의 괜찮으니 감점만” hard 통과 | 금지 |

hard 위반 시: 이 순서의 route 는 **infeasible**. 점수로 상쇄하지 않음.

---

### 9.7 §8·§9·§10 연결

```text
SoT: 방문 순서 (§8)
  → propagation (§9): arrival, load, waiting, …  (물리)
  → evaluation (§10): metric → constraint → score → comparator
```

같은 순서를 cache 로 빨리 계산해도, full propagation 결과와 다르면 **bug** (§2.6·§8.3).

---

## 10. evaluation · profile · objective (평가·설정·목표) (A11)

### 10.0 한 줄

§9 가 “이 순서면 **물리적으로** 어떤가” 라면,  
§10 은 “이 해가 **얼마나 좋은가 / 어떤 규칙으로 고를까**” 이다.

| §9 propagation | §10 evaluation |
|---|---|
| 도착·load·가능/불가 재료 | metric·constraint·score·비교 |
| 고객 이름 없음 | 고객 차이는 **`profile`** 로만 |

---

### 10.1 층 분리 — MUST (구체)

아래 층을 **섞지 않는다**.

```text
① snapshot 사실 (요청·차·거리표·단위)
② propagation + hard 가능/불가          ← §9
③ policy-neutral metrics (중립 지표)   ← “총 거리 50km” 같은 숫자 (좋/나쁨 해석 전)
④ hard constraints (필수 제약 판정)    ← 깨지면 해 자체가 불가
⑤ score components (점수 성분)         ← profile 이 “무얼 줄일지”
⑥ objective / comparator (목표·비교)   ← 두 해 중 어느 쪽이 나은지
⑦ SolvePlan stages (단계 계획)         ← phase-1 screen / phase-2 등 진행
```

| MUST NOT | 예 |
|---|---|
| hard 위반을 감점으로 통과 | 용량 초과인데 점수 −1000 하고 수락 |
| metric 에 가격·선호 섞기 | “거리” metric 안에 “용차 싫음” 넣기 |
| score 가 raw 입력 재해석 | 점수 계산이 JSON 을 다시 읽어 단위 바꿈 |
| comparator 가 전파 재실행 | 비교기가 또 전체 propagation (권위는 이미 계산된 facts) |
| `SolvePlan` 이 hard 해제 | “2단계에서 시간창 무시” |
| core 에 고객명 분기 | `if (customer == "Win")` |
| approximate ranking 으로 수락·발행 | shortlist 힌트로 최종 승자 결정 |

**구체 예 — 두 해 비교**

```text
해 A: 미배정 0, 총거리 100km, 자차 2대
해 B: 미배정 1, 총거리  80km, 자차 1대

profile 이 “미배정 수가 최우선(적을수록 좋음)” 이면
  → A 가 B 보다 나음 (거리 짧아도 B 패배)
hard 로 미배정을 막지 않는 한, B 도 “가능 해” 일 수 있음
```

---

### 10.2 `profile` (고객별 규칙·점수 묶음) — MUST (A11)

| 구분 | 쉬운 말 |
|---|---|
| **long-lived profile** | 시스템에 등록된 고객/프리셋 정의 (여러 solve 에 재사용) |
| **`bound profile`** | **이번 풀이** 에 묶어 snapshot 에 넣은 설정 (봉인 후 불변) |

**묶는 순서 (개념)**

```text
1. profile 식별 (어느 고객 설정인가)
2. objective preset 선택 (또는 기본)
3. 정규화된 사실·단위에 bind
4. 빠진 metric / 단위 불일치 → reject
5. bound profile + fingerprint → snapshot 에 포함
```

- 요청 JSON 이 가중치·수식을 **직접 주입**하지 않음.  
- 없는 preset 을 다른 고객 것으로 fallback 하지 않음.  
- Architecture: SPI / profile JAR 배치 DEFERRED. Domain: **조합으로 격리**.

```text
공통 엔진 (core)
  route state, propagation, ALNS …
  ❌ if (고객A) …

profile 쪽
  제약·metric·score·comparator·SolvePlan 조립
  ✅ 고객A preset / 고객B preset
```

---

### 10.3 metric · constraint · score · comparator

| 용어 | 쉬운 말 | 예 |
|---|---|---|
| **metric** (중립 지표) | **측정값**. 아직 “좋다/나쁘다” 아님 | `totalDistance`, `unassignedCount` |
| **hard constraint** | 깨면 불가 | capacity, time window, `serviceStartTime ≤ reqDate` |
| **score component** | profile 이 줄이거나 늘리려는 항 | 미배정 수, 자차 부피 비용 |
| **comparator** | 두 해 순위 | 사전식(lexicographic): 1순위 같으면 2순위… |
| **objective** | 무엇을 최적화할지 스키마 | 위 성분 순서·정의 |

#### 10.3.1 metric 이 여기서 의미하는 것 (보강)

**metric = 해(또는 route)에서 재 수 있는 숫자(측정값)** 이지,  
아직 **점수·승패가 아니다.**

```text
metric  = 측정값 (사실·집계)     예: “미배정 2건”, “총거리 85km”
score   = profile 이 순위에 쓰는 값  예: “미배정 2라서 이 해가 더 나쁨”
```

| 층 | 하는 일 | 예 |
|---|---|---|
| **propagation (§9)** | 시각·load 계산 | 도착 13:00, load 20 |
| **metric** | 그걸 **집계·이름 붙인 숫자** | `totalDistance`, `unassignedCount` |
| **hard constraint** | 한도 안인가 | load ≤ capacity |
| **score** | profile 이 줄이/늘리려는 항 | 미배정 최소화 |
| **comparator** | 두 해 승패 | A vs B |

- metric 에 “용차 싫음”, “VIP 고객” 같은 **선호**를 넣지 않음 → score / profile 쪽.
- 같은 metric 목록을 두고 profile 마다 **무엇을 score 로 쓸지** 만 달라질 수 있음.
- approximate ranking (shortlist 힌트) 은 metric/정식 score 가 **아님**.

**metric 예 (이름 예시)**

| metric | 의미 |
|---|---|
| `unassignedCount` | 미배정 일감 수 |
| `totalDistance` | 총 주행 거리 |
| `usedVehicleCount` | 사용 차량 수 |
| `totalRouteOperationalTime` | 운행 관련 시간 합 |
| route 별 `driveTime` | 순수 주행 시간 |

**metric 이 아닌 것**

| 아님 | 이유 |
|---|---|
| “이 해 70점” | score / 종합 결과 |
| hard 통과 여부 | constraint 판정 |
| shortlist 근사 점수 | approximate ranking |
| reqDate·시간창 raw 입력 | snapshot 입력 사실 (metric 재료일 수는 있음) |

**비유:** propagation = 경기 기록 → metric = 통계(점유율·슈팅) → score/comparator = 리그 승점 규칙(profile 마다 다름).

```text
예: 동일 metric
  해 A: unassignedCount=0, totalDistance=100
  해 B: unassignedCount=1, totalDistance=80

profile “미배정 최우선” → score/comparator 가 A 승
profile “거리만”         → 다른 승패 가능
// metric 값 자체는 그대로; 해석(순위)만 profile
```

**lexicographic (사전식) 비교 예** (특정 profile 예시, 전 고객 강제 아님):

```text
1순위: mandatoryUnassignedCount  (필수 일감 미배정 — 적을수록 좋음)
2순위: totalUnassignedCount
3순위: (있으면) LEASE 차량 비용
4순위: DIRECT 차량 부피 비용
5순위: 그 외 고객 목표
```

```text
해 A: (0, 0, …)
해 B: (0, 1, …)
→ 2순위에서 A 승

해 C: (1, 0, …)
해 A: (0, 5, …)
→ 1순위에서 A 승 (C 는 필수 미배정 있음)
```

고정 `1:100` 가중치나 Big-M 으로 순위를 **한 숫자로 뭉개지 않음** (기존 방향·미재심).

**소유 optional:** `vhclOwnTyp` 없는 차량·profile 이면 LEASE/DIRECT 비용 dimension 을 **안 씀**.

---

### 10.4 Win PoC comparator (예시 — 풀이 objective 와 별개)

official 비교용 예시 (작을수록 좋음, 첫 차이로 승패):

```text
unassigned request count
→ dispatched vehicle count
→ total directed distance
→ total route operational time
```

고객 일상 solve objective 와 **동일하다고 가정하지 않음**.

---

### 10.5 initial portfolio (초기 해 묶음)

**지위: 예시 / OPEN — 구현 MUST 아님** (인터뷰 정정)

- 파이프라인에 “여러 초기 해를 만든 뒤 ALNS 로 이어 간다”는 **단계 개념**만 유지.
- 아래에 적힌 개수·growth 이름·vehicle order 조합은 **설명용 예**일 뿐,  
  **반드시 그렇게 구현한다는 뜻이 아니다.**
- 구체 개수, 생성 휴리스틱, phase-1/2 step 수, worker 수 등은 **확정하지 않음** (OPEN).  
  hidden official default 로 문서가 채우지 않음.

**예시로만 보는 과거 서술 (비규범)**

```text
예: 최대 8개 = growth 4 × vehicle order 2
예: phase-1 screen 후 champion → phase-2 warm start
```

규범으로 남기는 것:

- 초기 해든 ALNS 든 **pair 단위**·hard·bound comparator 정합.
- approximate ranking 으로 최종 승자·발행 결정 금지.

---

### 10.6 approximate ranking 과 §10

| | shortlist 근사 | §10 정식 |
|---|---|---|
| 목적 | 후보 줄이기 | 수락·비교·발행 |
| hard 통과 보장 | 없음 | hard 깨면 불가 |
| cache = full | 해당 없음 (다른 식) | 같아야 함 |

---

### 10.7 §10 체크리스트

| # | 질문 | 기대 |
|---|---|---|
| 1 | hard 위반 해를 점수로 수락했나? | 아니요 |
| 2 | core 에 고객명 if 분기? | 아니요 |
| 3 | 이번 풀이 설정이 snapshot 에 bound 됐나? | 예 |
| 4 | 두 해 비교가 bound comparator 인가? | 예 |
| 5 | shortlist 점수로 최종 승자 정했나? | 아니요 |

---

## 11. ALNS baseline (기본 탐색 domain)

> **Search / operator domain.** 입력이 아님.  
> §7 문제 고정본 위에서 §8 해를 반복 변형하는 **의미**를 적는다.  
> operator 목록·step 수치·난수 시드는 **OPEN/예시** 가능 (확정 MUST 아님).

### 11.0 한 줄

ALNS = **빼고(destroy) · 넣고(repair) · 받을지 말지(acceptance)** 를 반복해  
`SearchSnapshot` 을 개선하는 탐색.  
기본 구현·benchmark 경로 (Master A8). hybrid 없음이 기본.

### 11.1 한 step 이 하는 일 (구체)

```text
[current SearchSnapshot]   V1: [R2픽, R1배, R2배], bank={}
        │
        ▼ Destroy (제거 제안)
  “R1 을 빼자” → 아직 경로를 직접 안 뜯음 / 또는 editor 가 pair 단위 제거
        │
        ▼ central editor 적용 (pair 전체)
  V1: [R2픽, R2배], bank={R1}
        │
        ▼ Repair (재삽입)
  R1 을 넣을 (route, 위치) 후보…
    - 근사 순위로 shortlist 가능 (§2.6)
    - 살아남은 것만 authoritative evaluation
        │
        ▼ CompletedTrial
  hard OK + 정식 점수 → acceptance
    accept → 새 current SearchSnapshot
    reject → draft 폐기, current 유지
```

### 11.2 Destroy / Repair 의미 — MUST

| | 의미 | MUST |
|---|---|---|
| **Destroy** | 어떤 `RequestId` 들을 경로에서 빼 **제안** | **pair 단위**. pickup 만 빼기 금지 |
| **적용 후** | 해당 Request 는 모든 route 에서 사라지고 `SearchRequestBank` 에 **정확히 한 번** | XOR |
| **Repair** | bank 의 request 를 다시 경로에 넣기 | pair 삽입 (PICKUP_DELIVERY 는 두 위치, 픽업 선행) |
| **`NEW_ROUTE`** | 새 경로 시작 | 실제 미사용 `VehicleId` 소비 (가짜 type count 금지) |
| **`DELIVERY_ONLY`** | 삽입 시 배송 위치만 route 방문 | 가짜 픽업 visit 금지 |

**Repair 결과 구분 (의미)**

| 결과 | 말 |
|---|---|
| `COMPLETE_REINSERTION` | 목표한 request 를 다 넣음 |
| `PARTIAL_REINSERTION` | 일부만 넣음. bank 에 남음. hard·XOR 지키면 **정상 trial** 가능 |
| `NO_FEASIBLE_INSERTION` | 못 넣음. 역시 가능 해일 수 있음 (전부 bank) |
| `DEFECT` | 구조 깨짐 → evaluation/acceptance **진행 금지** |

### 11.3 안정 해 vs 임시

| 이름 | domain 역할 |
|---|---|
| `TrialDraft` | step 안 임시 해. current 를 덮지 않음 |
| `CompletedTrial` | 구조+정식 평가 끝. 수락 대기 |
| `SearchSnapshot` | 탐색 **확정** 해 (routes+bank+평가 지문) |
| `current` / `stageBest` / `solveBest` | 서로 **다른** snapshot 가능. alias 공유 금지 |

비교: raw cost 한 줄이 아니라 **bound comparator** (§10).

### 11.4 approximate ranking (다시)

- shortlist 전용. hard·수락·발행 권위 없음.  
- 놓친 좋은 삽입 = 탐색 품질 이슈. pair 구조 결함 아님.

### 11.5 ALNS 가 Domain 에서 안 박는 것

- destroy/repair **operator 이름 목록** 필수화  
- 매 step 제거 개수, temperature, maxSteps **공식 수치**  
- 난수 시드 정책 세부  

→ 구현·bench OPEN. Domain 은 **pair·XOR·정식 평가·수락 경계**.

### 11.6 §11 체크리스트

| # | 질문 | 기대 |
|---|---|---|
| 1 | destroy 가 점(node) 단위인가? | 아니요 → Request/pair |
| 2 | 뺀 뒤 bank 와 route 이중 소유? | 아니요 |
| 3 | DEFECT 를 점수로 수락? | 아니요 |
| 4 | hybrid 없이 ALNS 만으로 발행 경로? | 예 (기본) |

---

## 12. Hybrid · route pool · MIP — C-17 GATED

> **Optional algorithm domain.** 기본 제품 경로 아님.  
> 상세 식·budget 은 OPEN. **의미·경계·금지** 만 분명히.

### 12.0 지위 — MUST

| 항목 | 규범 |
|---|---|
| 기본 구현·benchmark | **ALNS only** |
| route pool + MIP 재조합 | **C-17 GATED TARGET** |
| 별도 승인·evidence 전 구현 착수 | **MUST NOT** (실제 MIP/pool 본문) |
| production 기본 활성화 | **MUST NOT** |
| 문서에 개념 서술 | 가능 (활성화 승인 아님) |

미승인 시 파이프라인에서 Hybrid 단계는 **생략** (정상 baseline).

#### 12.0.1 config on/off (인터뷰 확정)

Hybrid/MIP 는 **config 로 on/off 할 수 있다.**  
다만 config 는 **실행 스위치**이고, **C-17 승인을 대체하지 않는다.**

| 층 | 역할 |
|---|---|
| **C-17 GATED** | 기능을 제품·production 에 쓸 **자격** (승인·evidence) |
| **config on/off** | 자격 있는 배포에서 **이번 실행에 켤지** |

```text
config (이름 예): hybridMip.enabled
  default = false  →  ALNS only (정상 baseline)
  true             →  C-17 허용 환경에서만 유효. 미승인이면 true 여도 production 규범 위반
```

| MUST | 말 |
|---|---|
| 기본값 | **off** (`false`) |
| config `true` 만으로 미승인 production ON | **금지** (C-17 우회 아님) |
| 미승인·off | Hybrid 단계 생략. ALNS 만으로 발행 경로 유지 |
| flag 이름·스키마 | **O2 / Architecture** (의미: boolean 스위치 + 기본 off) |

구현 해석 (의미 수준):

- 승인 전: flag 인터페이스·기본 false·no-op 경로는 가능 논의 여지. **실제 pool/MIP 본문 착수** 는 여전히 GATED.  
- 승인 후: config 로 실행 단위 on/off.

### 12.1 한 줄 그림 (개념)

```text
ALNS 가 돌아간 경로들에서
  → hard-feasible 한 route 조각을 pool 에 모음 (artifact)
  → (GATED) MIP/CP-SAT 등이 “route 고르기 + 미배정” partition
  → 골라진 것으로 새 해 후보 materialize
  → 다시 정식 평가 + independent verifier
```

ALNS 를 없애고 MIP 만 쓰는 것이 **기본이 아님**.

### 12.2 Artifact vs column — MUST 구분

| 이름 | 쉬운 말 | 담는 것 |
|---|---|---|
| **`EvaluatedRouteArtifact`** | 평가까지 끝난 **경로 조각** | 불변 route + 정식 평가 + 어느 문제/travel/profile 인지 |
| **`ProjectedRouteColumn`** | MIP 에 넣는 **열** | 그 artifact 를 특정 식으로 encode 한 것 |

- 같은 artifact 에서 projection 이 바뀌면 column 은 달라질 수 있음. artifact 자체는 안 바꿈.  
- pool 에 넣는 것은 **pair-complete · hard-feasible** 이고, **같은 snapshot 권위**(problem/travel/profile fingerprint) 여야 함.  
- “pool 에 넣음” ≠ **verified**. verified 는 **independent verifier** 만.

### 12.3 exact partition 개념 (수식은 개요)

요청 집합 \(I\), 선택 변수 \(x_r\), 미배정 \(u_i\):

\[
\sum_r a_{ir} x_r + u_i = 1 \quad (\text{각 request 정확히 한 번: 어떤 route 또는 unassigned})
\]

차량은 보통 한 column 에 한 대 소비 (\(h_{vr}\)).

- \(a_{ir}=1\) 이면 column 이 request \(i\) 의 **완전한 pair** 포함.  
- search bank 용어와 최종 UNASSIGNED 를 섞지 않음 (§8·§13).  
- profile dimension 을 조용히 버리고 exact 인 척 금지.

### 12.4 Domain 이 확정하지 않음 (OPEN / GATED)

- pool 크기 cap, MIP time budget, 호출 빈도  
- backend 제품 (OR-Tools CP-SAT 등은 상속 후보·미재심 또는 재검토)  
- production 켜는 조건  

### 12.5 §12 체크리스트

| # | 질문 | 기대 |
|---|---|---|
| 1 | hybrid 가 기본 ON 인가? | 아니요 (config default false) |
| 2 | config 로 on/off 가능한가? | 예 (C-17 우회 아님) |
| 3 | 미승인 production 에서 config true? | 금지 |
| 4 | artifact 와 column 을 같은 타입으로 취급? | 아니요 |
| 5 | pool 입장을 verified 라고 부르나? | 아니요 |
| 6 | 미승인인데 MIP 본문 구현 착수? | MUST NOT |

---

## 13. verification · finalization · result (검증·마무리·결과 domain)

> **Publication domain.** 입력이 아님.  
> “탐색이 끝난 후보”를 **믿어도 되는 발행 결과**로 바꾸는 의미.

### 13.0 왜 탐색과 분리하나

| 탐색 중 | 발행 전 |
|---|---|
| 빠른 cache·증분 평가 | 솔버 cache **비신뢰** |
| 수백만 trial | 후보 해 **소수** (보통 최종 1개 경로) |
| “더 좋은 draft” | “밖에 내보낼 권위” |

매 trial independent verifier 가 **아님** (§2.6).

### 13.1 independent verifier 2단 — MUST

#### ① candidate solution verifier (해 독립 검증)

| 항목 | 내용 |
|---|---|
| **입력** | 최종 후보 해 전체 + **같은** immutable solve snapshot |
| **하는 일** | pair/XOR, precedence, load·시간·`reqDate`(`serviceStart≤reqDate`), prepared travel, bound profile hard 를 **처음부터** 재검 |
| **범위** | 변경된 route 만이 아니라 **해의 모든 route + bank/미배정 집합** |
| **cache** | 솔버 incremental cache 를 진실로 쓰지 않음 |
| **PASS** | → `VerifiedSolution` 후보 |
| **FAIL** | 발행 진행 금지. (원인은 대개 구현 bug 또는 오염) |

#### ② result-integrity verifier (결과 묶음 검증)

| 항목 | 내용 |
|---|---|
| **입력** | finalization 이 만든 **발행 payload** |
| **하는 일** | ASSIGNED/UNASSIGNED partition, ID, provenance, 재계산 가능 지표가 **검증된 해와 같은 이야기인지** |
| **FAIL** | 잘못된 응답 발행 금지 |

```text
ALNS (및 optional GATED hybrid)
  → candidate solution verifier   ①
  → finalization / audit
  → result-integrity verifier     ②
  → publishable result
```

PASS 전 결과 = **발행 권위 없음**.

### 13.2 finalization / audit (발행 준비)

verifier ① PASS 후, 밖용 표현으로 정리:

- 내부 dense ID → external ID  
- audit 필드, integrity digest  
- 미배정 **사유**를 붙일 수 있음 (bank 가 사유 저장소는 아님 — 여기서 생성)

payload 는 ② 가 다시 검사 가능해야 함.

### 13.3 publishable result — MUST

```text
ASSIGNED
  : 사용된 routes + 각 route 가 소유한 Request (pair-complete)
UNASSIGNED
  : 명시적 미배정 Request + reasons/provenance
  : SearchRequestBank dump 가 아님
provenance
  : problem/travel/profile fingerprints
  : algorithm lineage (ALNS / hybrid 여부 등)
  : verifier ①② outcomes
```

placeholder 엔진 출력 ≠ publishable.

### 13.4 규모·속도 (재확인)

- route ~30 × 방문 20+ 에서 ① 1회 전체 재검증은 보통 **병목 아님**.  
- 병목은 ALNS trial 쪽.

### 13.5 §13 체크리스트

| # | 질문 | 기대 |
|---|---|---|
| 1 | trial 마다 verifier ①? | 아니요 |
| 2 | ① 이 변경 route 만? | 아니요 → 해 전체 |
| 3 | bank 를 UNASSIGNED 로 복붙? | 아니요 |
| 4 | ① 또는 ② FAIL 인데 발행? | 아니요 |
| 5 | cache 점수와 ① 재계산 불일치? | bug · FAIL |

---

## 14. 오류·종료 (개요)

| 구분 | 예 |
|---|---|
| input / normalization error | 단위·의미 위반, travel 불완전 |
| unsupported | multi-trip 등 |
| interrupt | time/step (수치 OPEN) |
| defect | pair XOR, cache≠정식 재계산 |
| verifier FAIL | 발행 금지 |
| GATED skip | hybrid 미활성 (정상 baseline) |

완료 = **gate + evidence AND** (Master A7).

---

## 15. acceptance 방향

| 영역 | evidence 방향 |
|---|---|
| D1 | 단일 계약·고정 단위; multi-version path 없음 |
| pair/bank | XOR 위반 = defect |
| DELIVERY_ONLY | pickup route 방문 없음 |
| travel | prepared only; verifier 동일 |
| snapshot | 탐색 후 problem fingerprint 불변 |
| evaluation | hard ≠ penalty 통과 |
| profile | core 고객명 분기 없음 |
| cache vs 정식 | 동일 해 불일치 = bug |
| verifier | 발행 전; 해 전체 |
| ALNS | hybrid 없이 가능 |
| C-17 | 미승인 비활성 |

---

## 16. 비범위 · OPEN · DEFERRED

**비범위:** multi-trip; 본격 MDVRP 필수화; hybrid 기본 ON; 실험 수치 확정; compute 확정(D2); cutover.

| ID | 내용 |
|---|---|
| O1 | Lambda vs ECS — Domain 비소유 |
| O2 | wire 이름·optional 깊이; trips vs end 우선순위 |
| O3 | adapter 공식 이름·범위 |
| O5 | Phase C |
| Depot 확대 | (2)(3) 추후 가능 |

Architecture: module DAG, port, compute 후보, verifier 프로세스 격리 등 DEFERRED.

---

## 17. Traceability (추적)

| 출처 | Domain |
|---|---|
| A1 | §1.5, §13 |
| A3–A5 | §2–3 |
| A8 C-17 | §12 |
| A9 | §1.4 |
| A11 | §10 |
| D1 | §4–5 |
| D2 | §16 (비소유) |
| 인터뷰 2026-07-31 | …; reqDate serviceStart≤reqDate; hybrid **config on/off·default off·C-17 우회 금지**; … |

### 의도적 미포함

Architecture 배치; C-17 수치; multi-version 입력; 새 실험 수치; implementation phase 본문.

---

## 18. 핵심 요약

1. **D1:** canonical 하나 · 단위 고정 · profile±optional · adapter 하나 (운영 스펙 변환 가능).  
2. **pair / servicePattern:** 원자 단위; `DELIVERY_ONLY` vs `PICKUP_DELIVERY`.  
3. **`immutable solve snapshot`** = **이후 변경 없는 문제 쪽 domain 묶음**. 해(`SearchSnapshot` 등)와 혼동 금지. 봉인 후 해만 변경.  
4. **동일 해 정식 점수:** cache 유무 무관하게 동일 (아니면 bug). 근사 순위는 shortlist only.  
5. **verifier:** 발행 전 해 전체 + payload.  
6. **ALNS baseline; C-17 GATED.**  
7. **용어:** `English (쉬운 한국어)` 전 문서.

---

## 19. 다음 액션

1. ~~Domain `REVIEW` 검수~~ → **`APPROVED`** (2026-07-31, deep-interview 반영)  
2. **다음:** `docs/YYYY-MM-DD-architecture-design.md` (Domain·Master 규범)  
3. (선택) Phase C — 구 문서 SUPERSEDED·링크  

---

*Phase B Domain **APPROVED**. 규범: Phase A + APPROVED Master + 2026-07-31 인터뷰 교정. 용어 스타일 v1.1.*
