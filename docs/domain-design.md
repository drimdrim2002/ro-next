---
title: RO-Next Domain Design
status: CONFIRMED
date: 2026-08-10
supersedes: docs/deprecated/2026-07-31-phase-b-domain-design.md
master: docs/master-design.md
revisions:
  - 2026-08-09 최초 확정
  - 2026-08-10 §2.1 canonical 확장 기준 추가 · §2.5 배송정책/탐색설정 분리 ·
    §5 profile 동결 제거 · §8.3 long[] 사전식 비교 · §8.4 확장 지점 3종
---

# RO-Next Domain Design

배차 문제의 **의미와 규칙**을 정의하는 문서다. 입력이 무슨 뜻인지, 어떤 배차안이 유효한지,
점수를 어떻게 매기는지, 결과에 무엇을 담는지를 여기서 정한다.
코드·모듈 배치는 [Architecture Design](architecture-design.md), 전체 흐름은 [Master](master-design.md) §2.

표기: 본문은 한국어, 코드에 등장할 식별자만 영어(`Request`, `reqDate` 등).
**MUST/MUST NOT**로 표시된 문장은 구현이 반드시 지켜야 하는 규칙이다.

| 절 | 내용 |
|---|---|
| §1 | 용어·식별자·pair 규칙 |
| §2 | 입력 계약 (규약 ↔ canonical) |
| §3 | 정규화 (단위·시간·서비스 시간·호환성) |
| §4 | 이동표 준비 |
| §5 | `Problem` — 동결된 문제 |
| §6 | `Solution` — 배차안 (경로·bank·XOR·시도) |
| §7 | 전파 — 물리 계산 |
| §8 | 평가 — metric·score·비교·profile |
| §9 | ALNS 탐색 |
| §10 | 재검증 |
| §11 | 결과 JSON |
| §12 | 오류 분류 |
| §13 | 구현·리뷰 체크리스트 |

---

## 1. 용어와 식별자

### 1.1 기본 용어

| 용어 | 뜻 | 헷갈리지 말 것 |
|---|---|---|
| **order (주문)** | 규약 JSON에 들어오는 일감 한 건 | 변환 후에는 `Request`로 다룸 |
| **`Request`** | 운송 의무 한 건 = pickup+delivery **짝(pair)**. 배정·변경의 원자 단위 | 지점 하나가 아님 |
| **visit (방문)** | 경로에 실제로 찍히는 정차 한 번 | `DELIVERY_ONLY`의 pickup 쪽은 방문이 아님 |
| **route (경로)** | 차량 한 대의 방문 순서 | 차종별 집계가 아님 |
| **depot (차고)** | 차량의 출발/도착 거점 | 고객 방문 지점이 아님 |
| **bank (미배정 목록)** | 탐색 중 아직 어느 경로에도 넣지 않은 `RequestId` 집합 | 최종 결과의 미배정이 아님 (§6.3) |
| **`Problem`** | 동결된 문제 묶음 (§5) | 배차안(`Solution`)이 아님 |
| **`Solution`** | 배차안: 경로들 + bank (§6) | 문제 정의가 아님 |
| **hard 제약** | 깨지면 그 배차안 자체가 불가능한 규칙 (용량·시간창 등) | 점수 감점(soft)이 아님 |
| **profile** | 고객별 점수·제약 구성 묶음 (§8) | core의 고객명 분기가 아님 |

이전 문서의 `immutable solve snapshot`/`SearchSnapshot` 용어는 폐기했다.
**변하지 않는 것은 `Problem`, 변하는 것은 `Solution`** — 이름만 봐도 구분되게 한다.

### 1.2 식별자 4종

| ID | 무엇 | 쓰임 |
|---|---|---|
| **`RequestId`** | 일감 한 건 | 배정·bank·결과의 ASSIGNED/UNASSIGNED |
| **`VehicleId`** | 차 한 대 | 경로 소유, 용량·차고 연결 |
| **`NodeId`** | "그 장소에서 하는 역할 한 칸" (pickup/delivery/depot) | 경로의 방문 순서, 시간창·서비스 시간 |
| **`LocationId`** | 지도상 한 장소 | 이동표(거리·시간) 조회 키, 좌표 |

- 같은 장소(`LocationId`)라도 pickup 역할과 delivery 역할은 **다른 `NodeId`**다.
- 이동 시간 조회는 `LocationId` 쌍으로, 방문 순서·시각은 `NodeId`로 다룬다.

```text
예: Request R2 (PICKUP_DELIVERY)
  pickup  NodeId N1 → LocationId LOC-WH   (분당창고)
  delivery NodeId N2 → LocationId LOC-CUST (강남 고객)
  N1→N2 이동 시간 = 이동표[LOC-WH → LOC-CUST]
```

### 1.3 `servicePattern` — Request의 두 형태

| `servicePattern` | 경로 방문 | 시간 입력 |
|---|---|---|
| **`DELIVERY_ONLY`** | delivery **만** 방문 | delivery 쪽 시간창·서비스시간·`reqDate` |
| **`PICKUP_DELIVERY`** | pickup·delivery **둘 다** 방문 | 양쪽 각각 시간창·서비스시간·`reqDate` |

- `DELIVERY_ONLY`의 pickup 쪽은 "짝 소유권"과 **출발 적재(initial load)에만** 참여한다.
  이동·정차·서비스 방문을 만들지 않는다 (MUST NOT). 가짜 depot 방문으로 흉내 내지 않는다 (MUST NOT).
- 현 규약의 주문은 전부 `DELIVERY_ONLY`다. `PICKUP_DELIVERY`는 canonical이 이미 정의하며,
  규약(wire) 확장은 호출 시스템과 협의 후 adapter만 추가한다.

### 1.4 pair 규칙 (MUST)

1. 배정·변경(넣기/빼기)의 원자 단위는 **`Request` 전체**다. pickup만 빼거나 delivery만 넣는 연산은 없다.
2. 같은 `Request`의 pickup·delivery는 **같은 차량 경로**에 있고, **pickup이 delivery보다 앞**이다.
3. 짝이 갈라진 상태(한쪽만 배정, 서로 다른 차량)는 점수가 나쁜 게 아니라 **구조 결함(버그)**이다.

---

## 2. 입력 계약

### 2.1 규약과 canonical의 관계

```text
[호출 시스템]  ──규약 JSON──▶  [adapter]  ──canonical──▶  [솔버·재검증]
```

- **규약(wire)**: 호출 시스템과 공유된 형식. [data/ro_input_json_spec.pdf](../data/ro_input_json_spec.pdf)
  (Plan / Plan Option / Order / Vehicle) + 실전 확장 필드(`distanceMatrix`, `zoneId` 등 —
  fixture [win_poc_case.json](../data/win_poc_case.json) 참고).
- **canonical(정본)**: 솔버가 이해하는 유일한 의미. 이 장이 정의한다.
- 변환 경로는 **adapter 하나** (MUST). 스키마 버전을 여러 개 병행 운영하지 않는다 (MUST NOT).
- 규약에 없는 canonical 필드(예: pickup 쪽 시간창)는 optional이며, 없으면 그 축을 쓰지 않는다.
- adapter는 애매한 값을 **추측하지 않는다** — 규칙으로 정해진 기본값만 채우고, 그 외는 입력 오류.

### 2.1.1 canonical 확장 기준 (MUST, 2026-08-10 확정)

고객마다 필요한 개념이 다를 때 canonical을 어떻게 늘릴지의 규칙이다.

> **그 개념이 다른 고객에게도 의미가 있는가?**

| 답 | 처리 |
|---|---|
| **예** (치수·온도대 등 배차 일반 개념) | **공통 canonical에 optional 필드**로 추가. 부재 = 그 축 미사용 (§2.4와 같은 규칙) |
| **아니오** (그 고객 사내 코드 체계 등) | 고객 전용 데이터 경로를 그때 설계한다. **선제적으로 만들지 않는다** |

- **고객마다 별도 canonical·별도 `Problem`·별도 adapter를 만들지 않는다 (MUST NOT).**
  `Problem`이 고객별로 갈라지면 재검증(§10)도 고객별이 되어 "탐색과 독립된 하나의 검증"이라는
  안전장치가 무너지고, 탐색 엔진이 고객 개념을 알게 된다.
- canonical은 "우리가 지원하는 배차 개념의 **합집합**"으로 커진다. 이는 정상이다 —
  의미가 겹치는 개념은 고객 간에 재사용되므로 필드가 고객 수만큼 늘지 않는다.
- 규약(wire)에 없는 필드를 새로 받으려면 호출 시스템과 협의한다 (§11.2와 같은 원칙).
  adapter는 "있으면 읽는다"일 뿐, 고객별로 분기하지 않는다 (MUST NOT).

```text
예: 3D 적재 고객 (item 치수)
  wire      item에 width/height/length 추가              ← 호출 시스템 협의
  canonical Item에 optional 치수 3필드 추가               ← core 변경은 이것뿐
            (Vehicle의 maxWidth/maxHeight/maxLength는 §2.4에 이미 있음)
  Problem   변경 없음 — 동결 절차·이동표·색인 그대로
  profile   치수를 읽는 HardConstraint 하나 추가          ← 고객 코드는 여기만
```

이 예처럼 **필드 추가로 끝나는 확장**과, 경로·전파·결과의 모양이 바뀌는 **구조 변경**
(예: 회전 분할 §2.5)은 비용이 완전히 다르다. 구조 변경은 이 기준의 대상이 아니며
설계 변경으로 별도 결정한다.

### 2.2 Plan (한 번 풀이의 봉투)

| canonical 항목 | 뜻 | 규약 대응 |
|---|---|---|
| `planId` | 풀이 식별자 | `planId` |
| `planStart` / `planEnd` | 계획 기간 `[planStart, planEnd)` — 끝 미포함 | `dateRange.from/to` |
| `customerId` | 어느 고객 요청인지 (profile 선택에 사용, §8.4) | `shprId`(또는 협의 필드) |
| depots | 차고 목록 (여러 개 가능) | `depot[]` |
| requests | 일감 목록 | `orders[]` |
| vehicles | 차량 목록 | `vehicles[]` |
| travel input | 이동 거리·시간 입력 (§4) | `distanceMatrix[]` 또는 좌표 |
| options | 운행 형태 등 (§2.5) | `options` |

- 시각 문자열: timezone 없는 `yyyy-MM-dd HH:mm:ss` (기존 유지). timezone 해석은 솔버 밖 책임.
  adapter는 규약의 RFC3339(`...T...Z`) 표기도 받아 이 형식으로 통일한다.

### 2.3 Request 필드

방문 쪽(side)별 필드 — `DELIVERY_ONLY`는 delivery 쪽만, `PICKUP_DELIVERY`는 양쪽 각각:

| 필드 | 뜻 | 비고 |
|---|---|---|
| `locationId` | 방문 장소 | 규약 `locId` (없으면 좌표로 생성) |
| `openTime` / `closeTime` | 시간창 (양끝 포함) | 기본 00:00:00 / 23:59:59 |
| `duration` | 방문 자체에 드는 시간(주차 등, 초) | item 작업시간과 별개 |
| `reqDate` | 고객 요청 시각 (아래 권위 정의) | 규약 `reqDate`/legacy `dueDate` |
| `zoneId` | 방문 장소의 구역 | 차량 `zoneIds`와 대조 |
| `vehicleFeatureList` | 허용 차급 목록. `["ALL"]` = 전 차급 | 규약 `vehicleFeature` |
| `items[]` | itemId, weight(kg), volume(cbm), qty, taskTime(초) | 규약 동일 |

**`reqDate` 권위 정의 (MUST)**

| 항목 | 규칙 |
|---|---|
| 의미 | 고객이 원하는 요청 시각. 제약 대상은 **서비스 시작 시각** |
| 제약 | 그 방문에서 **`serviceStartTime <= reqDate`** 하나뿐. `serviceEndTime`은 조건에 넣지 않는다 (MUST NOT) |
| 적용 | `DELIVERY_ONLY`: delivery 쪽. `PICKUP_DELIVERY`: pickup·delivery **각각 자기** `reqDate` |
| 기본값 | 미입력이면 planEnd (사실상 제약 없음 — 규약 default와 동일) |
| 금지 | "reqDate 이후 배송" 해석 금지. 한쪽 `reqDate`로 다른 쪽을 대체 금지 |

- `duration`과 `item.taskTime`은 다른 값이다. 방문 서비스 시간 계산은 §3.3.

### 2.4 Vehicle 필드

**공통(대부분 존재)**: `vehicleId`, `maxWeight`, `maxVolume`, `vehicleFeature`(차급 코드 하나),
`workStart`/`workEnd`, `speed`(km/h), `maxStopCnt`, `maxDriveTime`(초), `maxDriveDist`(m), `startDepot`.

**Optional — 없으면 그 축의 제약을 아예 적용하지 않는다 (MUST: 몰래 기본값을 채우지 않는다)**

| 필드 | 뜻 |
|---|---|
| `maxWidth`/`maxHeight`/`maxLength` | 치수 한도. 없으면 치수 제약 없음 |
| `capabilities` | 특수 능력(설치 기술 등). 규약 `driverSkill` 대응 |
| `zoneIds` (복수) | 운행 가능 구역. **미입력 = 전 구역 가능** |
| `endDepot` | 도착 차고. 있으면 start와 달라도 됨. 없으면 마지막 고객에서 종료 |
| `vhclOwnTyp` | 소유 구분. 값이 있으면 정확히 `DIRECT`\|`LEASE`만 허용, 그 외 non-empty는 오류. 미입력 = 소유 축 미사용 (DIRECT로 채우기 금지) |

### 2.5 차고·운행 형태 — 배송정책 (MUST)

규약 `options`로 들어오는 값 중 **유효한 답의 집합을 바꾸는 것**들이다. 정규화 결과는
`Problem`에 동결되고 재검증이 같은 값을 읽는다 (§5·§10).

| 규칙 | 내용 |
|---|---|
| 차고 여러 개 | 가능. 차량마다 `startDepot` 지정, `endDepot`은 optional |
| `trips` | `oneway` = 도착 차고 없음(차량에 `endDepot` 있으면 그것 우선). `roundtrip` = `endDepot` 미지정 차량은 `startDepot`으로 복귀 |
| `multiRotation` | **core 지원 범위 = 경로당 trip 1개** (차고 재방문 없음). 입력이 이 범위를 넘으면 `UNSUPPORTED_INPUT`으로 거부 (MUST) — 아래 주석 |
| `waitInDepot` | `Y` = 첫 방문 시간창에 맞춰 차고에서 늦게 출발 가능. `N` = 근무 시작 즉시 출발 |
| `depot.taskTime` | 현재 시간 계산에 **미적용** (trip이 1개뿐이라 상차 시간 개념 보류) |
| `defaultSpeed` | 이동표 준비 규칙에 사용 (§4) |

**`multiRotation`은 금지가 아니라 지원 범위 선언이다** (2026-08-10 변경). 엔진이 나중에 trip
분할을 지원하면 이 문서의 MUST를 뒤집는 대신 지원 범위 숫자를 올린다. 다만 회전 분할은
필드 추가가 아니라 **구조 변경**(경로 = trip 목록 → 전파·연산자·재검증·결과 JSON 전부)이므로,
실제 필요가 확인되기 전에는 구현하지 않는다.

> **미확정 (호출 시스템 확인 대기):** 규약의 `multiRotation` 값이 무엇을 세는지 —
> "차량이 도는 횟수"(1 = trip 1개 = 현재 지원)인지 "추가 회차 수"(1 = trip 2개 = 미지원)인지.
> fixture 두 개가 모두 `"1"`이므로 해석에 따라 접수 통과 여부가 갈린다.
> §2.1의 "adapter는 추측하지 않는다"에 따라 임의 해석 금지.

### 2.5.1 탐색설정 — 배송정책이 아니다 (MUST)

`Termination.secondsSpentLimit` 같은 **탐색을 언제 멈출지**에 관한 값들이다.

| 값 | 출처 |
|---|---|
| 최대 실행 시간 | 규약 `Termination.secondsSpentLimit` ▷ 운영 설정 기본값 |
| 최대 실행 step 수 · idle 허용 step 수 · idle 허용 시간 · 난수 seed | 운영 설정만 (규약에 없음) |

- 이 값들은 **`Problem`에 넣지 않는다 (MUST NOT).** 답이 달라질 뿐 무효가 되지 않으므로
  "무엇이 옳은 답인가"의 정의에 섞이면 안 된다.
- **재검증은 이 값들을 읽지 않는다 (MUST NOT).** 재검증은 예산과 무관하게 해 전체를 검사한다.
- 배송정책과 하나의 타입으로 묶지 않는다 (묶는 순간 위 두 규칙을 지킬 수 없다).
- `distanceTimeCalculate`는 어느 쪽도 아니다 — 이동표 준비 동작을 분기시키지 않으므로(§4)
  canonical에서 제거하고, 필요하면 결과 run 메타에만 남긴다 (2026-08-10 확정).

### 2.6 전역 한도

`Optimizer.VehicleMaxStopCount` 같은 전역 한도가 차량별 한도와 같이 있으면 **min(둘)** 적용.
한도가 아예 없으면 **제약 없음**이다 — 큰 수(sentinel)로 채우지 않는다 (MUST NOT).

---

## 3. 정규화 — 숫자·시간을 하나의 의미로

정규화 결과는 솔버와 재검증이 **공유하는 유일한 의미**다. 같은 입력이면 같은 정규화 결과가 나와야 한다.

### 3.1 단위 (전역 고정, MUST)

| 차원 | 외부(규약) | 내부(canonical) |
|---|---|---|
| 무게 | decimal kg | `long`, kg×1000 (소수 3자리 FLOOR) |
| 부피 | decimal CBM | `long`, CBM×1000 (소수 3자리 FLOOR) |
| 거리 | integer meter | integer meter |
| 시간 | 초 | `long` 초 |
| 비용 | integer | overflow 검사하는 integer |
| 수량 | 양의 정수 | integer |

- `double`로 먼저 근사한 뒤 변환하지 않는다 (MUST NOT). item 단위로 먼저 환산 후 합산한다.
- 소수 거리·시간 입력은 거부한다.

### 3.2 시간 원점

```text
origin = planStart
normalizedTime = origin 기준 경과 초 (long)
```

- 계획 기간 `[planStart, planEnd)`. 시간창 open/close는 양끝 포함.
- 근무 구간 규칙: 한 이동(arc)은 통째로 한 근무창 안에 들어가야 한다.
  "오늘 운전하다 내일 이어서"식 분할 금지 (MUST NOT).

### 3.3 방문 서비스 시간

```text
serviceTime(방문) = duration + Σ(item.taskTime × item.qty)
serviceStartTime  = max(arrival, openTime)
serviceEndTime    = serviceStartTime + serviceTime
```

- order 수준 `taskTime`만 있고 item이 없는 형태는 입력 오류 (기존 유지).

### 3.4 호환성 판정 (독립 hard, 전부 AND)

```text
sizeCompatible       = vehicle.vehicleFeature ∈ request.vehicleFeatureList  OR  list == ["ALL"]
capabilityCompatible = request가 capability 미요구  OR  요구 ⊆ vehicle.capabilities
zoneCompatible       = vehicle.zoneIds 미입력(전 구역)  OR  방문 zoneId ∈ vehicle.zoneIds
치수                  = optional 치수 한도가 있을 때만 검사
```

호환 차량이 0대인 Request는 구조 오류가 아니다 — 풀이는 진행되고 그 Request는 미배정+사유로 남는다.

---

## 4. 이동표 준비 (travel)

키 = **`LocationId` 쌍** (방향 있음). 탐색·재검증 모두 **준비된 표만** 사용한다 (MUST).
탐색 중 좌표로 즉석 계산하거나 대칭화하지 않는다 (MUST NOT).

| 값 | 뜻 | 규칙 |
|---|---|---|
| `D` | 거리 (integer meter) | 소수 거부. self arc = 0 |
| `U` | 시간 (integer second) | 소수 거부. self arc = 0 |
| `C` 등 기타 | 참고 값 | **feasibility·점수에 사용 금지** (MUST NOT) |

누락 보정 (기존 유지):

```text
D 누락 → Great Circle 거리 (HALF_UP 반올림)
U 누락 → ceil(D × 3.6 / speedKmH)   // speed 없으면 defaultSpeed, 그것도 없으면 45
```

---

## 5. `Problem` — 동결된 문제 (MUST)

풀이 시작 시 다음을 **한 번에 검증하고 동결**한 묶음이 `Problem`이다:

```text
requests / vehicles / depots 정의 (정규화 완료)
LocationId 목록과 이동표
단위·시간 해석 규칙 (§3)
호환성 사실 (§3.4)
배송정책 (§2.5)
```

규칙:

1. `Problem` 생성 이후 그 내용은 **일절 변경하지 않는다** (읽기 전용, MUST).
2. 탐색·재검증은 같은 `Problem`을 읽기만 한다. 탐색이 문제·이동표·배송정책을 고치면 버그다.
3. 새 입력이 오면 **새 `Problem`**을 만든다. 기존 것을 수정하지 않는다.
4. 생성 시점에 ID 참조·이동표 완전성·pair 참조를 검증하고, 실패하면 풀이를 시작하지 않는다.

**`Problem`에 담지 않는 것 (MUST NOT)** — 2026-08-10 확정:

| 담지 않는 것 | 이유 | 대신 |
|---|---|---|
| `Profile` (§8.4) | 담으면 profile 코드가 `Problem`을 참조할 수 없게 되어(순환) 차급·구역·치수 같은 문제 사실을 영영 못 읽는다 | 탐색·재검증에 **인자로 전달**. 한 번 resolve한 같은 인스턴스를 양쪽에 넘긴다 |
| 탐색 예산 (§2.5.1) | "언제 멈출지"가 "무엇이 옳은 답인지"에 섞인다 | 탐색기에만 전달. 재검증은 못 본다 |

둘 다 `Problem`과 나란히 전달되므로, "탐색과 재검증이 같은 것을 본다"는 보장 수준은
`Problem` 자체와 동일하다.

---

## 6. `Solution` — 배차안

### 6.1 구성과 예

```text
Problem (고정)                          Solution (탐색이 바꿈)
  R1 = DELIVERY_ONLY, 강남100, 10kg      Route of V1: 차고A → R2픽(분당) → R1배(강남100) → R2배(강남200)
  R2 = PICKUP_DELIVERY, 분당→강남200      bank = {}          // 전부 배정된 상태
  V1 = startDepot 차고A, end 없음
                                        다른 예: Route of V1: 차고A → R2픽 → R2배
                                                bank = { R1 }   // R1은 아직 미배정
```

### 6.2 경로가 유효하려면 (MUST)

| 규칙 | 내용 |
|---|---|
| 차량 하나 | 한 경로는 정확히 한 `VehicleId` 소유 |
| 출발/도착 | `startDepot`에서 출발. `endDepot` 있으면 거기서 종료, 없으면 마지막 고객에서 종료 |
| pair | 같은 Request의 pickup·delivery가 같은 경로에, pickup 먼저 (§1.4) |
| `servicePattern` | `DELIVERY_ONLY`는 delivery 방문만 (가짜 픽업 방문 금지) |
| 적재 | 모든 구간에서 `0 ≤ load ≤ capacity`. 아래 부호 규칙 |
| 이동 | 준비된 이동표만 사용 |
| hard | 시간창·근무시간·`reqDate`·maxStop·maxDrive 등 전부 충족 |
| 차고 재방문 | 경로 중간의 depot 재방문 금지 (multi-trip 비범위) |

**적재 부호 규칙 (MUST)**

```text
initialLoad(route) = Σ 그 경로에 배정된 DELIVERY_ONLY request의 수요   // 출발 전 적재
DELIVERY_ONLY delivery 방문      → load 감소
PICKUP_DELIVERY pickup 방문      → load 증가
PICKUP_DELIVERY delivery 방문    → load 감소
경로 종료 시 load = 0            // 미완료 pair로 우회 금지
```

### 6.3 route–bank 배타 규칙 (XOR, MUST)

확정된 `Solution`에서 각 `Request`는 정확히 다음 중 하나다:

```text
배정됨   = 정확히 한 경로가 소유 (필요한 방문 완비)  AND  bank에 없음
미배정   = 어떤 경로도 소유하지 않음 (방문 없음)     AND  bank에 정확히 한 번
```

둘 다이거나 둘 다 아니면 **구조 결함(버그)**이다. 점수 문제가 아니다.

bank는 **`RequestId`만** 담는다. 실패 사유·에러 메시지·비용을 저장하지 않는다 (MUST NOT).
미배정 **사유**는 풀이가 끝난 뒤 결과를 만들 때 기록한다 (§11).

### 6.4 진실(SoT)과 파생 (MUST)

| 구분 | 예 |
|---|---|
| **SoT — 이것이 바뀌면 해가 바뀐 것** | 경로의 방문 순서 / 누가 어떤 Request를 소유 / 경로↔차량 묶임 / bank |
| **파생 — SoT에서 다시 계산 가능** | 방문별 도착·서비스 시각, load 곡선, 총거리, 점수, 삽입 후보 캐시 |

1. SoT를 바꾸면 관련 파생을 무효화하고 다시 계산한다.
2. **같은 SoT인데 캐시 점수 ≠ 처음부터 재계산한 점수이면 버그다** — 이 원칙이 §10 재검증의 근거다.

### 6.5 시도(trial)는 복사본에서 (기존 유지)

탐색은 확정 해를 제자리에서 고치지 않는다:

```text
[확정 Solution]  →  바뀌는 부분만 복사한 draft에서 destroy/repair 시도
                 →  구조 검사(pair·XOR) → 정식 평가(시간·용량·점수)
                 →  더 좋으면 draft를 새 확정 Solution으로 승격 (accept)
                 →  아니면 draft 폐기 (확정 해는 그대로)
```

`current`(현재 기준 해)와 `best`(지금까지 최선 해)는 서로 다른 확정 Solution일 수 있다.
한 객체를 별칭으로 공유하지 않는다.

---

## 7. 전파 (propagation) — 순서가 정해진 경로의 물리 계산

방문 순서가 주어졌을 때 앞에서 뒤로 한 번 훑으며 **도착·대기·서비스·적재·주행 사실**을 계산하는
단계다. 점수·선호 판단은 하지 않는다 (그건 §8).

### 7.1 방문마다 하는 일

```text
1. arrival        = 직전 출발 시각 + 이동표[직전 장소 → 이번 장소]
2. serviceStart   = max(arrival, openTime)          // 이르면 대기
3. serviceEnd     = serviceStart + serviceTime      // §3.3
4. reqDate 검사    : serviceStart ≤ reqDate          // §2.3. serviceEnd는 조건 아님
5. load 갱신       : 픽업 +, 배송 −                   // §6.2 부호 규칙
6. hard 검사       : 용량·시간창·근무시간·한도
7. departure      = serviceEnd
```

### 7.2 숫자 예 (단위 분·kg, V1 capacity 30)

```text
경로: 차고A(08:00 출발) → R2픽(창 09:00–12:00, 서비스 5분, reqDate 10:00)
                       → R1배(창 13:00–18:00, 서비스 14분, reqDate 15:00)
이동: 차고→픽 40분, 픽→R1배 50분
```

| 단계 | 시각 | load |
|---|---|---|
| 차고A 출발 | 08:00 | **10** (R1이 DELIVERY_ONLY라 출발 적재) |
| R2픽 도착 | 08:40 → 창 열릴 때까지 대기 | 10 |
| R2픽 서비스 | 09:00–09:05, reqDate 검사 09:00≤10:00 통과 | 픽업 후 **20** |
| R1배 도착 | 09:55 → 13:00까지 대기 | 20 |
| R1배 서비스 | 13:00–13:14, reqDate 검사 13:00≤15:00 통과 | 배송 후 **10** |

만약 serviceStart가 16:00이고 reqDate가 15:00이면 `16:00 ≤ 15:00` 거짓 → 그 경로는 **불가**.
전파는 가능/불가 **사실**만 알려주고, 점수로 덮지 않는다.

### 7.3 기록하는 값 (재검증이 같은 공식으로 재합산할 수 있어야 함)

`arrival`, `serviceStartTime`, `serviceEndTime`, `loadWeight`, `loadVolume`,
`distance`, `driveTime`, `customerWaitingTime`, `depotWaitingTime`, `serviceTime`,
`interWorkWindowRestTime`, `stopCount`,

```text
routeOperationalTime = driveTime + customerWaitingTime + depotWaitingTime
                     + serviceTime + interWorkWindowRestTime
```

### 7.4 stopCount와 주행 (기존 유지)

```text
stopCount: 직전 고객 서비스 장소와 이번 장소가 다르면 +1
           (depot는 세지 않음. 같은 장소 연속 방문은 첫 진입만 +1)
driveDist = Σ 실제 지난 D (meter)      // depot→첫고객, 고객→고객, (있으면) 마지막→endDepot
driveTime = Σ 실제 지난 U (second)     // 대기·서비스·휴식은 불포함
```

### 7.5 하면 안 되는 것 (MUST NOT)

- hard 위반을 "감점하고 통과"시키기 — hard가 깨지면 그 경로는 불가다.
- 전파 중 문제·이동표 수정 (§5 위반).
- 전파 단계에서 점수·고객 선호 판단 (§8의 일).

---

## 8. 평가 — 어느 배차안이 더 좋은가

### 8.1 층 분리 (MUST)

```text
① Problem 사실        (주문·차량·이동표)                    ← core
② 전파 + hard 판정     (§7 — 가능/불가)                      ← core (+ profile의 추가 hard)
③ metric (중립 지표)   측정값. 좋다/나쁘다 해석 전            ← core. 결과 JSON에 실림 (§11)
                      예: totalDistance, unassignedCount
④ score (점수 성분)    이 profile이 줄이려는 축의 목록        ← profile이 소유 (§8.4)
                      예: [미배정 수, 요율표 비용, 총거리]
⑤ 비교               두 해 중 승자 결정                     ← core. 사전식 하나뿐 (§8.3)
```

- ③과 ④는 **다른 것**이다. ③은 누구에게나 같은 측정값이고 결과에 그대로 실린다.
  ④는 이번 고객이 무엇을 먼저 줄이려는지의 목록이며 비교에만 쓰인다.
- ⑤에는 고객별 선택지가 없다. 무엇을 비교할지는 ④가 이미 정했다.

| MUST NOT | 예 |
|---|---|
| hard 위반을 점수로 상쇄 | 용량 초과인데 −1000점 하고 수락 |
| metric에 선호 섞기 | "거리" 지표 안에 "용차 싫음" 넣기 |
| 비교 단계에서 전파 재실행 | 비교는 이미 계산된 ③·④만 사용 |
| core에 고객명 분기 | `if (customerId == "...")` — profile로만 (§8.4) |

### 8.2 metric 예

`unassignedCount`(미배정 수) · `totalDistance` · `usedVehicleCount` · `totalRouteOperationalTime` ·
경로별 `driveTime`. metric은 측정값일 뿐이고, 그중 무엇을 어떤 우선순위로 줄일지는 profile이 정한다.

### 8.3 비교는 사전식(lexicographic) 하나뿐 (MUST)

profile의 `score`는 **"작을수록 좋다"인 축의 순서 있는 목록**이다. 비교는 앞 축부터 차례로
보고, 같을 때만 다음 축을 본다. 비교 방식은 이것 하나이며 고객이 바꾸지 않는다.

```text
예 (한 profile의 축 구성 — 전 고객 강제 아님):
  default 고객   score = [미배정 수, 사용 차량 수, 총거리, 운행 시간]
  요율표 고객     score = [미배정 수, 요율표 비용, 총거리]

해 A: (미배정 0, 100km)  vs  해 B: (미배정 1, 80km)
"미배정 우선" 축 구성 → A 승 (거리가 짧아도 B 패배)
축 구성이 다른 profile → 다른 승패 가능. metric 값(③)은 같고 축 목록(④)만 다름
```

- 고정 가중치(1:100)나 Big-M으로 여러 축을 한 숫자로 뭉개지 않는다 (MUST NOT).
  **비교기 자체가 사전식 하나뿐이므로 가중합 비교기를 만들 수 없다** — 관례가 아니라 구조다.
- 크게 만들고 싶은 축은 부호를 뒤집어 넣는다 (전 축이 최소화 대상이라는 규칙을 깨지 않는다).
- 같은 profile이 만드는 축 목록의 **길이는 항상 같다.** 다르면 버그다.
- `vhclOwnTyp`(소유)이 없는 입력·profile이면 LEASE/DIRECT 비용 축을 **아예 쓰지 않는다**.

### 8.4 profile — 고객 차이의 격리 (MUST)

```text
공통 엔진 (경로 상태·전파·ALNS·비교)   ❌ 고객명 분기 없음
profile (고객별)                     ✅ 추가 hard 제약, score 축 구성
```

- 연결: **코드 레지스트리** — `customerId → profile` 맵 하나. **미등록 고객은 default profile** (MUST).
  (이전 설계의 YAML 2층 카탈로그는 폐기. 고객이 늘어 설정 파일이 필요해지면 그때 별도 결정.)
- 탐색과 재검증은 **같은 profile 인스턴스**를 사용한다 (MUST) — 다르면 검증이 무의미해진다.
  한 번 resolve해 양쪽에 **인자로 전달**한다 (§5 — `Problem`에 담지 않는다).
- 요청 JSON이 가중치·수식을 직접 주입하지 않는다 (MUST NOT).
  요율표 같은 고객 데이터는 그 profile 구현이 코드로 소유한다.

#### 확장 지점 — 세 개 (이게 전부다)

| 고객이 바꾸려는 것 | 방법 | 비용 |
|---|---|---|
| 목적식 (우선순위·요율표 비용) | 기본 profile을 **상속**해 `score` 축 구성을 재정의 | 고객 코드만 |
| 추가 hard 제약 (3D 적재 등) | `HardConstraint` 구현 후 profile의 목록에 등록. 판정에 필요한 문제 사실(차급·구역·치수)은 `Problem`에서 직접 읽는다 | 고객 코드만 |
| 도메인 필드 (item 치수 등) | canonical에 **optional 필드** 추가 (§2.1.1) | core record 필드 |

- 이 셋으로 표현되지 않는 요구는 **구조 변경**이다 (예: 회전 분할 §2.5). profile로 우회하지 말고
  설계 변경으로 결정한다.
- profile이 요구하는 입력이 실제로 왔는지 검사하는 자리는 **아직 없다.** 첫 고객 구현 시
  `Profile`에 검사 메서드를 추가한다 (없어도 동작하며, 진단 품질만 떨어진다).
- profile 구현의 성능(예: 적재 판정 반복 비용)은 그 구현의 책임이다. core는 이를 위한
  캐시·상태 자리를 미리 만들지 않는다.

---

## 9. ALNS — 기본 탐색

### 9.1 한 스텝

```text
[current Solution]
  → destroy: 일부 Request를 경로에서 뺀다     ← 반드시 pair 단위 (pickup만 빼기 금지)
  → 뺀 Request는 bank에 정확히 한 번 (§6.3)
  → repair: bank의 Request를 다시 넣어 본다   ← pair 삽입 (PICKUP_DELIVERY는 두 위치, 픽업 선행)
  → 구조 검사 → 정식 평가 (전파 + profile 점수)
  → acceptance: 더 좋으면(또는 수락 규칙상 허용이면) 새 current로, 아니면 폐기
```

- 새 경로 시작은 실제 미사용 `VehicleId`를 소비한다 (가짜 차량 타입 카운트 금지).
- repair가 일부만 넣거나 하나도 못 넣어도, hard·XOR을 지키면 **정상적인 시도**다
  (전부 bank에 남은 해도 유효한 해다). 구조가 깨졌을 때만 결함이다.

### 9.2 빠른 후보 추리기 (shortlist)

삽입 위치 후보를 줄이기 위한 근사 점수는 써도 된다. 단 —

- 근사 점수로 **수락·최종 비교·결과 확정을 하지 않는다** (MUST NOT). 정식 평가만 그 권위를 가진다.
- 좋은 삽입을 근사가 놓치는 것은 탐색 품질 이슈일 뿐 구조 결함이 아니다.

### 9.3 설계가 고정하지 않는 것

초기해 개수·생성 휴리스틱, destroy/repair 연산자 목록, 반복 수·온도·시간 한도, 난수 시드 정책 —
전부 **구현·실험 재량**이다. 문서는 pair·XOR·정식 평가·수락 경계만 고정한다.
(이전 설계의 "초기해 ≤8 → phase-1 screen → phase-2" 구조는 폐기. "초기해 생성 → ALNS 개선"이 전부다.)

---

## 10. 재검증 — 결과 저장 전 안전장치 (MUST)

### 10.1 왜

탐색은 속도를 위해 증분 계산·캐시를 쓴다. 그 코드에 버그가 있으면 "규칙을 어긴 배차안"이
좋은 점수로 살아남을 수 있다. 그래서 결과 저장 직전에 **탐색 코드와 분리된 검증 코드**가
최종 배차안을 **캐시 없이 처음부터** 재계산한다.

### 10.2 규칙

| 항목 | 내용 |
|---|---|
| 시점 | 결과 저장 직전 1회 (매 trial마다 돌리지 않는다) |
| 입력 | 최종 `Solution` 전체 + 같은 `Problem` + **같은 profile 인스턴스** (§8.4) |
| 검사 | 모든 경로에 대해: pair·XOR(§6.3), 선행, 적재 곡선, 시간창·`reqDate`·근무시간, 이동표 사용, profile hard. 그리고 metric(③)과 score 축(④)을 각각 재계산해 탐색이 보고한 값과 대조 |
| 범위 | 바뀐 경로만이 아니라 **해 전체** (bank 포함) |
| 독립성 | 탐색의 증분 캐시·내부 상태를 믿지 않는다. 코드도 `verify` 패키지로 분리, 탐색 내부 참조 금지 (Architecture §2) |
| 예산 무관 | 탐색 예산(§2.5.1)을 읽지 않는다 (MUST NOT). 시간·step 한도와 무관하게 해 전체를 검사한다 |
| FAIL | **결과를 저장하지 않는다.** 상태 FAILED + 원인 기록. 원인은 대개 탐색 코드 버그다 |

결과 JSON 변환이 올바른지(검증된 해 ↔ JSON 일치)는 별도 실행 단계가 아니라 **테스트로 보장**한다.
(이전 설계의 2단 verifier 중 result-integrity 단계는 이 결정으로 폐기.)

### 10.3 비용

경로 ~30개 × 방문 ~20개 규모에서 전체 재검증 1회는 병목이 아니다 (병목은 탐색 자체).

---

## 11. 결과 JSON

### 11.1 내용 (의미 목록)

```text
planId, status (DONE | FAILED)
run   : inputKey(접수 시 S3 key), 접수·시작·종료 시각, 사용한 profileId, 재검증 통과 여부
        적용된 배송정책(§2.5)과 탐색 예산(§2.5.1) — 둘을 **따로** 기록한다.
        같은 문제를 다른 예산으로 돌린 결과를 구별할 수 있어야 한다 (벤치마크 비교의 전제)
routes: 차량별로 —
  vehicleId
  visits[]: orderId(RequestId), locationId, arrival, serviceStart, serviceEnd, load
  경로 지표: driveDist, driveTime, stopCount, routeOperationalTime
unassigned[]: orderId + reason (예: NO_COMPATIBLE_VEHICLE, TIME_WINDOW_INFEASIBLE, CAPACITY, NOT_PLACED)
metrics: unassignedCount, usedVehicleCount, totalDistance, totalRouteOperationalTime
```

- 미배정 사유는 결과 생성 시 계산해 붙인다 (bank는 ID만 갖고 있으므로, §6.3).
  탐색이 남긴 bank가 그대로 결과가 되는 게 아니라 **재검증을 통과한 해의 미배정 집합**이 결과다.
- 식별자는 규약과 같은 이름(`orderId`, `vehicleId` 등)을 쓴다.
- 이전 설계의 fingerprint·algorithm lineage 등 추적 장치는 위의 간단한 `run` 메타로 대체 (확정).

### 11.2 wire 확정

규약 PDF는 **입력만** 정의하고 결과 형식은 정의하지 않는다. 위 §11.1이 결과의 **의미 정본**이며,
최종 wire 필드명·배치는 호출 시스템과 협의해 [Implementation Plan](implementation-plan.md) 단계에서
확정한다 (의미 변경 없이 이름·형태만).

---

## 12. 오류 분류

| 분류 | 예 | 처리 |
|---|---|---|
| 입력 오류 | 스키마 위반, 소수 거리, 알 수 없는 `vhclOwnTyp` | 접수 시 4xx (S3 저장 없음) |
| 미지원 입력 | `multiRotation`이 core 지원 범위 초과 (§2.5) | `UNSUPPORTED_INPUT` — 접수 거부 |
| Problem 생성 실패 | ID 참조 깨짐, 이동표 불완전 | FAILED 상태 + 원인 |
| 탐색 중단 | 시간 한도 도달 | 그 시점 best로 재검증 진행 (정상) |
| 구조 결함 | pair 분리, XOR 위반, 캐시≠재계산 | 버그. 재검증 FAIL → 결과 미저장 |
| 재검증 FAIL | hard 위반 발견 | FAILED 상태 + 원인. 결과 저장 금지 |

---

## 13. 구현·리뷰 체크리스트

| # | 질문 | 기대 |
|---|---|---|
| 1 | destroy/repair가 pair 단위인가? | 예 |
| 2 | 각 Request가 경로 또는 bank 정확히 하나에 있는가? | 예 |
| 3 | `DELIVERY_ONLY`에 픽업 방문이 생기지 않는가? | 예 (initial load만) |
| 4 | 탐색이 `Problem`·이동표를 수정하지 않는가? | 예 |
| 5 | bank에 사유 문자열을 저장하지 않는가? | 예 (ID만) |
| 6 | hard 위반을 감점으로 통과시키지 않는가? | 예 |
| 7 | 같은 해의 캐시 점수 = 전체 재계산 점수인가? | 예 (다르면 버그) |
| 8 | core에 고객명 분기가 없는가? | 예 (profile만) |
| 9 | 탐색과 재검증이 같은 profile 인스턴스·이동표를 쓰는가? | 예 |
| 10 | 재검증 FAIL 시 결과가 저장되지 않는가? | 예 |
| 11 | 미등록 customerId가 default profile로 풀리는가? | 예 |
| 12 | optional 필드 부재 시 그 축 제약이 사라지는가? | 예 (몰래 채움 없음) |
| 13 | `Problem`에 profile·탐색 예산이 들어 있지 않은가? | 예 (§5) |
| 14 | 재검증이 시간·step·idle 한도를 참조하지 않는가? | 예 (§2.5.1·§10.2) |
| 15 | 여러 축을 한 숫자로 뭉개는 비교가 없는가? | 예 — 사전식 비교 하나뿐 (§8.3) |
| 16 | 고객별로 갈라진 canonical·`Problem`·adapter가 없는가? | 예 (§2.1.1) |
