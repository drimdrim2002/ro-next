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
  - 2026-08-10 §7.1 시간창 close 판정을 serviceStart 기준으로 정본화 (Stage 3 §9 Q1 해소 —
    탐색과 재검증이 같은 규칙을 각각 구현하므로 해석이 갈리면 두 코드의 판정이 어긋난다).
    **§3.2 근무창과 차고 시간창은 이번 개정 대상이 아니다** — Plan §2.1 D4(다일 근무창 +
    차고 창)가 소유하며, D4가 확정되면 §3.2·§7.1을 함께 개정한다
  - 2026-08-10 **D4 확정 — 다일 근무창 + 차고 시간창** (바로 위 줄이 예고한 개정의 이행):
    §3.2 시간창 전개 규칙 신설(날마다 반복·자정 넘는 창·병합·planEnd 클리핑·시간 측정 규약) ·
    §7.1 출발·방문·종료 절차 재작성(창 밖이면 다음 창으로 미룸, 차고 창 적용, `DEPOT_WINDOW`) ·
    §7.2.1 다일 숫자 예 신설 · §7.3 대기 3종 정의와 항등식 · §2.3·§2.4·§2.5·§6.2·§7.5·§10.2·§12
    문장 정합 · §13 체크리스트 17·18 추가. 시간창 close 기준(§7.1 MUST)은 그대로 보존한다
  - 2026-08-10 **D1 확정 — `multiRotation` = 바퀴 수, 차량별 `trips`**: §2.5 판정 반전
    (통과 = `{0,1}`, `-1`·`2 이상` = `UNSUPPORTED_INPUT`, `≤ -2` = `INVALID_INPUT` —
    `> 1` 비교식 금지) · 규약 PDF 문면 어긋남을 §2.5에 기록(열거 정의는 복귀 횟수로 읽히나
    `multirotation 2` 예시 그림은 바퀴 수와 일치) · `trips`를 차량별 지정 가능으로
    (§2.4 optional 행 신설, §2.5 `차량 trips ▷ options.trips` 우선순위와 접기 유지 판단) ·
    §12 오류 분류 두 행 갱신. 차량별 `multiRotation`은 multi-trip 개방 시점으로 이관(Master §6)
  - 2026-08-11 검토 반영 — §7.2.1 미룬 시간 귀속 정정(55800 전액 휴식 → 대기 1800 + 휴식 54000,
    §7.3 공식과의 모순 해소) · §7.1 waitInDepot=Y 정밀 규칙 정본화(가장 늦은 출발, N 가정 선계산 —
    종전에는 Stage 3 문서에만 있었다) · §7.1 위반 귀속 규약(두 창 축 동시 소진 시) ·
    §2.6 한도 경계 포함(≤) 명시 · §3.2 규약 인용 정정("다음 열림" 문장은 주문·차고 창에만 있다 —
    PDF 원문 재확인) · §11.1 경로 시각 2종(depotDeparture·depotReturn) 추가 + FAILED 결과
    무생산 명시(Stage 5 §9 Q3 제안 이행)
  - 2026-08-11 **유예 3묶음 + C 결정 3건** — ① wire에 없는 차량 축 셋(치수 3필드 · 차량별
    `trips` · `vhclOwnTyp`)을 §2.4 표에서 빼고 **유예 표**로 대체(트리거·근거 명시,
    정본 등재는 Stage Extra). 연쇄 정합: §2.1.1 3D 예시 · §2.5 `trips` 행과 접기 주석 ·
    §3.4 치수 축 삭제 · §8.3 소유 비용축 · §12 입력 오류 예시.
    ② `depot.taskTime` = **복귀 선적 시간**으로 의미 확정(시스템 소유자) — 1바퀴에는 발생하지
    않으므로 canonical 미보유, multi-trip 개방 시 부활. 종전 "보류" 문구를 대체.
    ③ §3.1에 `weight`·`volume`·`taskTime` **전부 ×qty** 명시(규약 PDF 문면이 부정확) ·
    소수 거리·시간 거부를 재확인(FLOOR 완화 안 함)하고 원본 fixture 미접수 사실 기록.
    **설계 규칙 변경은 없다** — ②③은 기존 규칙의 근거·문구 확정이고, ①은 선제 구현 되돌리기다
  - 2026-08-12 **전수 감사 치명 결함 3건 수선 (시스템 소유자 확정)** — ① §4 self arc:
    "= 0" 규정 폐기, 정규화가 sentinel **D = 999,000 m · U = 86,400초**로 강제(입력 self arc
    값은 읽지 않고 버린다 — 실물 wire는 D=9999를 보낸다) · ② §2.4 `startDepot` 부재 규칙 신설
    (이 도메인은 CVRPTW도 덮는다 — depot이 하나면 그 차고, 여러 개면 `INVALID_INPUT`) ·
    ③ §3.1 수치 필드의 JSON 인코딩 = **number** 확정(문자열 수치는 `INVALID_INPUT`,
    실행 fixture를 number로 정정). §12 오류 예시 2건 추가
  - 2026-08-12 감사 후속 인터뷰 — ① §3.1 소수 거부 판정 = **표기 기준**(number 토큰에
    소수점이 있으면 값과 무관하게 거부, `15.0`도 거부) 확정. 인용 수치 204,756이 이 기준임을
    명기(그중 3,558건은 정수값 소수 표기) · ② §2.4 `maxDriveDist`의 PDF 철자 `maxDriveDistc`
    별칭 수용(두 철자 다 읽고, 둘 다 오면 `INVALID_INPUT`) · ③ §2.3 `itemId` 필수 —
    **비어 있으면 orderId 사용** (stage-06의 `prodId` 폴백은 소유자 확정이 아닌
    작성 시 발명(commit edb2aae)이라 폐기) · ④ §2.5 실물 options 3키
    (`driverRestTimeRatio`·`difficultySortType`·`customerAbbr`) **무시 확정** —
    0이 아닌 비율도 거부하지 않음(수용된 위험) · ⑤ §2.1 미지 wire 필드 일반 정책 = **무시**
    확정 + 실물 무시 대상 목록 등재(`DEPOT`·`district`·주문 `customerId`·`continent`·
    `lssId`·`routes`·`prodId`) · ⑥ §3.4 차량 쪽 `vehicleFeature` 부재·`"ALL"` =
    **전 차급 와일드카드** 확정(규약 Default "ALL" 차량이 유령이 되던 결함 수선) ·
    ⑦ §3.1 단위 코드 검증 확정(`KG`/`CBM` 외 `INVALID_INPUT`, 부재 = KG·CBM) ·
    ⑧ §7.4 stopCount **첫 고객 방문도 +1** 확정(Win 실측 28방문=28정차=한도 경계로 검산) ·
    ⑨ §11.1 미배정 reason 판정 규칙 정본화(4종 고정 + 검사 순서: 호환 0대 → 정적 용량 →
    단독 전파 불가 → NOT_PLACED — Stage 5 §4.3 절차를 정본으로 승격) · ⑩ §10.2 검사 목록을
    **참조형으로 전환**(§6.2 전 항목 + §6.3 XOR + §3.4 호환성 + §2.6 경계 + profile hard —
    한도 축 누락이 낳은 상하위 역전을 구조적으로 봉합)
  - 2026-08-13 전수 감사 기계적 정정 — §2.2 depots 행에 규약 단일 depot 정책 주석 ·
    §2.4 공통 목록을 "실물 wire 실존 7필드 / 규약 정의 optional(실물 미출현)"로 분리하고
    규약(PDF 표 정의)·실물 wire(fixture 실측) 용어 구분 명시 · §2.5 depot.taskTime
    "복귀 자체가 없으므로" → "복귀 후 재선적이 없으므로"(roundtrip의 복귀는 있다) ·
    §2.5 defaultSpeed의 실물 wire 표기(`Optimizer.DefaultSpeed`) 병기 · §3.3 openTime이
    §7.1 적용 창의 open임을 명시 · §6.2 hard 행의 "등" 제거·한도 축 전수 나열 · §7.1 절차 6이
    새로 판정하는 것(용량·한도) 명시 · §7.2 숫자 예에 R2배 방문 보충(pair 완결·종료 load 0 —
    자체 MUST 위반이던 예 정정) · §7.3 마지막 방문 departure = serviceEnd 명시 · §8.3
    "가중합 비교기를 만들 수 없다" 단언 완화(축 구성은 구조가 못 막는다 — 리뷰 대상) ·
    §11.1을 의미 정본으로 한정(필드명·형태는 §11.2 소유)·visits 적재 2축(loadWeight·
    loadVolume) 표기·run "재검증 통과 여부"에 항상 PASS 주석. 규칙 변경 없음
  - 2026-08-14 **`startDepot` optional + `PICKUP_ONLY`** — 화물 축(`Request`)과 차의
    출·종료 축(`Vehicle.startDepot`/`endDepot`)을 분리. ② 2026-08-12 §2.4 `startDepot`
    부재 채움 규칙 **폐기**(차고 1개여도 비움 유지. 현 규약 wire의 `WIN_0` 채움은 Stage 6
    adapter 정책 — Domain에 암묵 채움을 남기지 않는다. fixture 파일은 불변).
    `Vehicle.startDepot`을 `endDepot`과 대칭 optional로. ③ `Request.delivery` optional +
    `ServicePattern.PICKUP_ONLY` (§1.3 세 값). 적재 항등식 3줄(§6.2).
    `DELIVERY_ONLY`×start 부재 / `PICKUP_ONLY`×end 부재는 정규화 오류가 아니라 호환성
    (§3.4 depot 앵커 — 호환 0대 = `NO_COMPATIBLE_VEHICLE`). 둘 다 없는 Request는
    `INVALID_INPUT`. `trips=roundtrip` + end 부재 + start 부재 = `INVALID_INPUT` (§2.5).
    §7.1 출발을 start 유무로 분기(없으면 앞 arc 없음, 첫 방문 arrival = spanStart).
    가상 depot·수량 부호 뒤집기·차고 NodeId 방문 금지
  - 2026-08-15 §2.5 차고 창 행을 §7.1에 맞춤 — **있는** 출·도착 순간에만 적용 (없는 쪽은
    검사하지 않는다). 종전 "출발·복귀 두 순간"은 양쪽 차고가 있을 때의 문면
  - 2026-08-15 **구역 `"ALL"` 와일드카드 + 목록 원소 null 금지 (시스템 소유자 확정)** —
    ① §3.4 `zoneCompatible` 식 개정: `"ALL"`은 구역 이름이 될 수 없는 예약어이고 목록에
    **단독(`["ALL"]`)으로만** 올 수 있다 — `["ALL"]` = 전 구역, `["ALL","ZONE_1"]` =
    `INVALID_INPUT`(차급 `["ALL","T1"]`과 같은 규칙), 방문 `zoneId`가 부재·blank·`"ALL"`이면
    구역 제약 없음. 정규화가 양쪽을 접는다. §2.3·§2.4 표 칸 정합. 종전 문면은 구역 칸이 없는
    방문을 구역 제한 차량과 불합격으로 읽히던 결함이 있었다 ·
    ② §3 서두에 목록·집합 원소 null = `INVALID_INPUT` 신설 — 관문에서 막고 이후 코드는
    null을 가정하지 않는다 (Stage 1 감사 발견: `Set.copyOf`의 NPE가 4xx여야 할 것을 5xx로 만든다)
  - 2026-08-16 §2.6 정차 한도 접기를 min(차량, 전역)에서 **차량 > 전역 > 없음**으로 변경
    (덮어쓰기. 숫자 Default·sentinel 없음). `maxDrive*`·용량의 경계 포함(≤)은 그대로다
  - 2026-08-16 §2.3 `itemId` 폴백의 수행 위치를 **정규화**로 명시 (adapter는 원문 전달).
    규칙 자체는 2026-08-12 확정 그대로
  - 2026-08-16 §3.1 소수 거부의 **표기 기준 판정 대상을 이동표 `D`·`U`로 한정** (시스템 소유자
    확정). `duration`·`taskTime`·`maxDrive*`는 raw가 `long`이라 표기가 남지 않아 판정 불가라는
    구조를 명시한 것 — 규칙 완화가 아니라 적용 범위의 확정이다 (Stage 1 구현 검토 발견)
  - 2026-08-17 **self arc 원복 — D = 0 · U = 0** (2026-08-12 항목 ①의 sentinel 999,000/86,400
    강제를 폐기). 전제가 틀렸다 — 서로 다른 두 Request가 같은 `LocationId`를 쓰는 것은 정상
    입력이라 큰 값이 정상 케이스에 벌점을 매겼고, 같은 장소 연속 방문을 "정차 1회"로 세는 §7.4와
    모순이었다. 자기 순환·가상 depot 금지는 값이 아니라 구조 검사(`DUPLICATE_NODE`·
    `UNKNOWN_NODE`·`NodeId` 없는 차고)가 소유한다. §4 값 표·MUST 문단, §2.4 `startDepot` 괄호.
    입력 self 행을 읽지 않고 버리는 규칙은 무변경 (Stage 2 구현 검토 발견)
  - 2026-08-17 self arc 원복 후속 — §4 제목의 "정규화가 0으로 확정"을 **이동표 준비**로 교정
    (입력 self 행을 버리는 일은 §3 정규화, 대각에 0을 기입하는 일은 이 절). §6.2에 방문 유일·
    참조 행을 추가하고 §6.5·§12 구조 검사 목록을 맞춤. **값(D=0·U=0)과 금지 대상은 무변경**
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
| §3 | 정규화 (단위·시간·시간창 전개·서비스 시간·호환성) |
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
| **visit (방문)** | 경로에 실제로 찍히는 정차 한 번 | `DELIVERY_ONLY`의 pickup 쪽·`PICKUP_ONLY`의 delivery 쪽은 방문이 아님 |
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

### 1.3 `servicePattern` — Request의 세 형태

화물(`Request`)이 어디서 생기고 어디서 사라지는가와, 차(`Vehicle`)가 어디서 출발·종료하는가는
다른 축이다. 패턴은 화물 쪽만 정한다.

| `servicePattern` | pickup | delivery | 경로 방문 | 시간 입력 |
|---|---|---|---|---|
| **`DELIVERY_ONLY`** | ∅ | 고객 | delivery **만** | delivery 쪽 시간창·서비스시간·`reqDate` |
| **`PICKUP_ONLY`** | 고객 | ∅ | pickup **만** | pickup 쪽 시간창·서비스시간·`reqDate` |
| **`PICKUP_DELIVERY`** | 장소1 | 장소2 | pickup·delivery **둘 다** | 양쪽 각각 시간창·서비스시간·`reqDate` |

- `DELIVERY_ONLY`의 pickup 쪽은 "짝 소유권"과 **출발 적재(initial load)에만** 참여한다.
  화물은 그 차량의 `startDepot`에서 실린다 (차고는 방문이 아니다 — `NodeId` 없음).
  이동·정차·서비스 방문을 만들지 않는다 (MUST NOT). 가짜 depot 방문으로 흉내 내지 않는다 (MUST NOT).
  이 패턴을 받는 차량은 `startDepot`이 있어야 한다 — 없으면 실을 곳이 없어 호환되지 않는다 (§3.4).
- `PICKUP_ONLY`의 delivery 쪽은 "짝 소유권"과 **도착 하차에만** 참여한다.
  화물은 그 차량의 `endDepot` **도착 사건**에서 내린다 (차고는 방문이 아니다).
  이동·정차·서비스 방문을 만들지 않는다 (MUST NOT). 가짜 depot 방문으로 흉내 내지 않는다 (MUST NOT).
  이 패턴을 받는 차량은 `endDepot`이 있어야 한다 — 없으면 내릴 곳이 없어 호환되지 않는다 (§3.4).
- 양쪽 side가 모두 없으면 `INVALID_INPUT`이다 (§12).
- 현 규약의 주문은 전부 `DELIVERY_ONLY`다. `PICKUP_DELIVERY`·`PICKUP_ONLY`는 canonical이
  정의하며, 규약(wire) 확장은 호출 시스템과 협의 후 adapter만 추가한다.

### 1.4 pair 규칙 (MUST)

1. 배정·변경(넣기/빼기)의 원자 단위는 **`Request` 전체**다. pickup만 빼거나 delivery만 넣는 연산은 없다.
2. 같은 `Request`의 **있는 방문**은 **같은 차량 경로**에 있다. pickup·delivery **둘 다** 있으면
   pickup이 delivery보다 앞이다 (`PICKUP_DELIVERY`). 한쪽만 있으면 그 방문만 경로에 있다
   (`DELIVERY_ONLY` = delivery, `PICKUP_ONLY` = pickup).
3. 짝이 갈라진 상태(있어야 할 방문이 빠지거나 서로 다른 차량)는 점수가 나쁜 게 아니라 **구조 결함(버그)**이다.

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
- **미지 wire 필드는 무시한다 (2026-08-12 확정).** 이 문서가 처분을 정하지 않은 키는 검증도
  보관도 하지 않고 조용히 버린다 — wire는 호출 시스템이 관리하므로 저쪽의 필드 추가가 접수를
  깨면 안 된다. 실물에서 확인된 무시 대상: 주문 `DEPOT`·`district`·주문 수준 `customerId`
  (profile 키는 `shprId` — §2.2), top-level `continent`·`lssId`·`routes`, item `prodId`(§2.3),
  options 3키(§2.5). PDF가 Mandatory로 표기한 `continent`도 소비처가 없어 존재 검증조차
  하지 않는다.

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
  canonical Item에 optional 치수 3필드 추가               ← core 변경은 여기와 아래 한 줄
            Vehicle에 maxWidth/maxHeight/maxLength 3필드도 같이 되살린다
            (2026-08-11 유예 — §2.4 유예 표 · Stage Extra E2)
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
| `planStart` / `planEnd` | 계획 기간 `[planStart, planEnd)` — 끝 미포함. **여러 날 가능** (시간창 전개는 §3.2) | `dateRange.from/to` |
| `customerId` | 어느 고객 요청인지 (profile 선택에 사용, §8.4) | `shprId`(또는 협의 필드) |
| depots | 차고 목록 (canonical은 여러 개 가능) | `depot[]` — 규약은 단일 depot 정책이다 (다중은 협의 사항, PDF 3쪽). 차량 `startDepot`/`endDepot`은 각각 optional (§2.4). 현 규약 wire의 단일 차고 채움은 Stage 6 adapter |
| requests | 일감 목록 | `orders[]` |
| vehicles | 차량 목록 | `vehicles[]` |
| travel input | 이동 거리·시간 입력 (§4) | `distanceMatrix[]` 또는 좌표 |
| options | 운행 형태 등 (§2.5) | `options` |

- 시각 문자열: timezone 없는 `yyyy-MM-dd HH:mm:ss` (기존 유지). timezone 해석은 솔버 밖 책임.
  adapter는 규약의 RFC3339(`...T...Z`) 표기도 받아 이 형식으로 통일한다.

### 2.3 Request 필드

방문 쪽(side)별 필드 — 있는 side만 채운다 (`DELIVERY_ONLY`는 delivery, `PICKUP_ONLY`는 pickup,
`PICKUP_DELIVERY`는 양쪽). 둘 다 없으면 `INVALID_INPUT` (§1.3·§12).

| 필드 | 뜻 | 비고 |
|---|---|---|
| `locationId` | 방문 장소 | 규약 `locId` (없으면 좌표로 생성) |
| `openTime` / `closeTime` | 시간창 (양끝 포함). **계획 기간의 날마다 반복** (§3.2) | 기본 00:00:00 / 23:59:59 |
| `duration` | 방문 자체에 드는 시간(주차 등, 초) | item 작업시간과 별개 |
| `reqDate` | 고객 요청 시각 (아래 권위 정의) | 규약 `reqDate`/legacy `dueDate` |
| `zoneId` | 방문 장소의 구역. 부재·`"ALL"` = 구역 제약 없음 (§3.4) | 차량 `zoneIds`와 대조 |
| `vehicleFeatureList` | 허용 차급 목록. `["ALL"]` = 전 차급 | 규약 `vehicleFeature` |
| `items[]` | itemId, weight(kg), volume(cbm), qty, taskTime(초) | 규약 동일 |

**`reqDate` 권위 정의 (MUST)**

| 항목 | 규칙 |
|---|---|
| 의미 | 고객이 원하는 요청 시각. 제약 대상은 **서비스 시작 시각** |
| 제약 | 그 방문에서 **`serviceStartTime <= reqDate`** 하나뿐. `serviceEndTime`은 조건에 넣지 않는다 (MUST NOT) |
| 적용 | `DELIVERY_ONLY`: delivery 쪽. `PICKUP_ONLY`: pickup 쪽. `PICKUP_DELIVERY`: pickup·delivery **각각 자기** `reqDate` |
| 기본값 | 미입력이면 planEnd (사실상 제약 없음 — 규약 default와 동일) |
| 금지 | "reqDate 이후 배송" 해석 금지. 한쪽 `reqDate`로 다른 쪽을 대체 금지 |

- `duration`과 `item.taskTime`은 다른 값이다. 방문 서비스 시간 계산은 §3.3.
- **`itemId`는 필수다** (규약 Mandatory와 일치). **비어 있으면 그 주문의 `orderId`를
  itemId로 쓴다** (2026-08-12 시스템 소유자 확정 — §2.1이 허용하는 '규칙으로 정해진 기본값'.
  실물 fixture는 452건 전부 빈 문자열이라 전건 orderId를 쓴다). **수행은 정규화**다
  (2026-08-16 — adapter는 wire 원문을 그대로 넘긴다). `prodId`는 canonical이 읽지
  않는다 — 종전 stage 문서의 `prodId` 폴백은 시스템 소유자 확정이 아닌 작성 시 발명이라 폐기했다.

### 2.4 Vehicle 필드

용어 구분 — 이 절에서 **규약**은 PDF 표의 정의를, **실물 wire**는 fixture 실측
([win_poc_case_floor.json](../data/win_poc_case_floor.json))을 가리킨다. 둘은 다르다 —
규약이 정의해도 실물 wire에 안 오는 필드가 있다.

**공통 — 실물 wire 실존 7필드** (fixture 차량 31/31): `vehicleId`, `maxWeight`, `maxVolume`,
`vehicleFeature`(차급 코드 하나 — 부재·`"ALL"` = 전 차급 와일드카드, §3.4),
`workStart`/`workEnd`, `speed`(km/h).

**규약 정의 optional — 실물 wire 미출현 (0/31)**: `maxStopCnt`, `maxDriveTime`(초),
`maxDriveDist`(m). `startDepot`도 실물 미출현이다 (규약 표에는 정의가 없는 canonical 필드 —
부재는 비움으로 둔다. 현 규약 wire의 단일 차고 채움은 Stage 6 adapter — 아래).

- `workStart`/`workEnd`는 **날마다 반복되는 근무창**이다 (§3.2). 계획 기간이 여러 날이면 날짜마다
  같은 시간대의 창이 하나씩 생긴다. `maxDriveTime`·`maxDriveDist`·`maxStopCnt`는 **경로 전체 합계**의
  한도이며 하루치 한도가 아니다 (규약이 기간을 말하지 않는다 — 하루 한도가 필요해지면 그때 축을 더한다).
- `maxDriveDist`의 규약 PDF 표기는 `maxDriveDistc`다 (8쪽 — 끝의 c. 오타로 추정되나 실물
  wire 출현이 0건이라 미확인). adapter는 **두 철자를 모두 읽고**, 두 키가 같이 오면
  `INVALID_INPUT`이다 (2026-08-12 확정).

**`startDepot` / `endDepot`은 둘 다 optional이다 (MUST, 2026-08-14 — 2026-08-12 채움 규칙 폐기).**
화물 방향과 차의 출·종료는 다른 축이다. 정규화는 부재를 채우지 않는다.

| 필드 | 부재의 뜻 |
|---|---|
| `startDepot` | 첫 고객 방문에서 경로 시작. 앞 arc 없음. 가상 depot으로 흉내 내지 않는다 (MUST NOT — 차고는 `NodeId`가 없어 경로에 넣을 수 없고, 같은 `NodeId` 재방문은 §6.2 방문 유일 위반이다) |
| `endDepot` | 마지막 고객에서 종료. 복귀 arc 없음 |

명시한 값이 차고 목록에 없으면 `INVALID_INPUT`이다. 차고 개수는 부재 해석을 바꾸지 않는다
(1개여도 비움 유지, 여러 개여도 비움 유지).

**현 규약 wire의 채움은 adapter 정책이다 (Stage 6).** floor fixture는 차량 31/31이 `startDepot`
없이 오고 차고는 `WIN_0` 하나다. 이 파일을 `PlanInput`으로 직접 넣으면 canonical `startDepot`은
비어 있다. 앱 접수(Stage 6)는 현 규약 CVRPTW(주문 전부 `DELIVERY_ONLY`, start 키 없음, depot
정확히 1개)에서 그 차고 ID를 `VehicleInput.startDepotLocId`에 넣는다 — Domain/정규화가 채우지
않는다. 1차 성공 기준·Win 비교는 이 앱 접수 경로 기준이라 `WIN_0` 출발이 유지된다.
fixture 파일 자체는 고치지 않는다.

`DELIVERY_ONLY`를 받으려면 그 차량에 `startDepot`이 있어야 하고, `PICKUP_ONLY`를 받으려면
`endDepot`이 있어야 한다. 없으면 그 차량과 호환되지 않는다 (§3.4). 정규화는 이 조합을 거절하지
않는다 — 혼합 차대가 유효하다.

**Optional — 없으면 그 축의 제약을 아예 적용하지 않는다 (MUST: 몰래 기본값을 채우지 않는다)**

| 필드 | 뜻 |
|---|---|
| `capabilities` | 특수 능력(설치 기술 등). 규약 `driverSkill` 대응 |
| `zoneIds` (복수) | 운행 가능 구역. **미입력 = 전 구역 가능**. `["ALL"]` 단독도 전 구역, 다른 구역과 섞이면 `INVALID_INPUT` (§3.4) |
| `startDepot` | 출발 차고. 있으면 그 장소에서 출발. 없으면 첫 고객에서 시작 |
| `endDepot` | 도착 차고. 있으면 start와 달라도 됨. 없으면 마지막 고객에서 종료 |

**canonical에 담지 않는 차량 축 (2026-08-11 유예)** — 아래 셋은 **실물 wire에 없다**
(fixture 차량 31대의 키는 `vehicleId`·`vehicleFeature`·`maxWeight`·`maxVolume`·
`workStartTime`·`workEndTime`·`speed` 7개뿐). 규약 문서가 정의하더라도 canonical은 담지
않으며, 되살릴 조건·지점은
[Stage Extra](implementation/stage-extra-deferred-features.md) 등재부에 있다.

| 축 | 유예 사유 | 트리거 |
|---|---|---|
| `maxWidth`/`maxHeight`/`maxLength` | `Item`에 대응 치수가 없어 §3.4 치수 축의 **검사 대상 자체가 없다** | 3D 적재 고객 확정 (§2.1.1 경로) |
| `trips` (차량별) | wire에 없다. `options.trips`만으로 접기가 완결된다 (§2.5) | multi-trip을 열 때, 또는 wire에 등장 |
| `vhclOwnTyp` | wire에 없고 호환성·점수·재검증 어디에도 **소비처가 없다** | 소유 구분이 점수 축·hard 제약이 될 때 |

### 2.5 차고·운행 형태 — 배송정책 (MUST)

규약 `options`로 들어오는 값 중 **유효한 답의 집합을 바꾸는 것**들이다. 정규화 결과는
`Problem`에 동결되고 재검증이 같은 값을 읽는다 (§5·§10).

| 규칙 | 내용 |
|---|---|
| 차고 여러 개 | 가능. 차량마다 `startDepot`/`endDepot`은 각각 optional (§2.4). 명시한 값만 차고 목록에서 검증 |
| `trips` | `oneway` = 도착 차고 없음(차량에 `endDepot` 있으면 그것 우선). `roundtrip` = `endDepot` 미지정 차량은 `startDepot`으로 복귀 — **`startDepot`도 없으면 `INVALID_INPUT`** (접을 대상이 없다. 조용히 oneway로 떨어뜨리지 않는다). **전체 설정 하나다** — 차량별 지정은 wire에 없어 유예했다 (§2.4 유예 표). 아래 주석 |
| `multiRotation` | **차량이 도는 바퀴 수**를 센다. **core 지원 범위 = 경로당 trip 1개**(= 1바퀴, 차고 재방문 없음)이므로 **통과하는 값은 `{0, 1}` 둘뿐**이다 — `0`은 미설정(규약 기본값)이고 `1`과 같게 취급한다. `-1`(무제한 복귀)·`2` 이상은 범위를 넘으므로 `UNSUPPORTED_INPUT`으로 거부 (MUST). `-2` 이하는 규약 자체가 금지한 값이라 `INVALID_INPUT`. 아래 주석 |
| `waitInDepot` | `Y` = 첫 방문 시간창에 맞춰 차고에서 늦게 출발 가능 (`startDepot` 있을 때만 적용. 없으면 무시 — 오류 아님). `N` = **근무 시작과 차고 개장 중 늦은 쪽에 즉시 출발** (start 없으면 절차 0' — 앞 arc 없음) |
| `depot.openTime`/`closeTime` | 차고 시간창. **있는** 출·도착 순간에만 적용한다 (§7.1). 날마다 반복 (§3.2). 위반은 `DEPOT_WINDOW` |
| `depot.taskTime` | **차고로 복귀해 다시 선적하는 데 걸리는 시간이다** (2026-08-11 시스템 소유자 확정). `multiRotation`이 `{0, 1}` = 1바퀴뿐인 현재 범위에서는 **복귀 후 재선적이 없으므로 이 시간도 발생하지 않는다** (roundtrip의 복귀 자체는 있다 — §7.1 `DEPOT_WINDOW` 검사 대상) — canonical에 담지 않고 시간 계산에도 쓰지 않는다. multi-trip을 여는 시점에 함께 되살린다 ([Stage Extra E1](implementation/stage-extra-deferred-features.md)) |
| `defaultSpeed` | 이동표 준비 규칙에 사용 (§4). 실물 wire 표기는 `Optimizer.DefaultSpeed`다 (fixture 실측 — PDF 정의는 `defaultSpeed`) |

**실물 options 중 읽지 않는 3키 (2026-08-12 확정)** — `driverRestTimeRatio`·`difficultySortType`·
`customerAbbr`는 **무시한다** (canonical에 담지 않고 검증도 하지 않는다 — 무슨 값이 와도 접수는
통과). `customerAbbr`(고객 축약명)·`difficultySortType`(Win 내부 정렬 힌트로 추정)은 답의 의미와
무관하다. `driverRestTimeRatio`는 이름상 운전 시간에 비례한 휴식 비율로 보이나 Win 쪽 의미가
미확인이라 소비하지 않는다 — **0이 아닌 값도 거부하지 않으므로, 그런 입력에서는 Win 엔진과
결과가 다를 수 있다 (수용된 위험, 시스템 소유자 확정).** §7.3의 `interWorkWindowRestTime`
(근무창 사이 휴식)과는 다른 개념이니 혼동하지 말 것. 의미가 확인되고 필요해지면 배송정책으로
편입한다.

**`multiRotation`은 금지가 아니라 지원 범위 선언이다** (2026-08-10 변경). 엔진이 나중에 trip
분할을 지원하면 이 문서의 MUST를 뒤집는 대신 지원 범위 숫자를 올린다. 다만 회전 분할은
필드 추가가 아니라 **구조 변경**(경로 = trip 목록 → 전파·연산자·재검증·결과 JSON 전부)이므로,
실제 필요가 확인되기 전에는 구현하지 않는다.

**`multiRotation`의 의미 — 확정 (2026-08-10, 시스템 소유자)**

숫자는 **차량이 도는 바퀴 수**를 센다. 차고에서 출발해 한 바퀴 돌면 `1`이고, 이것이 지금
지원하는 전부다. `2`부터가 차고로 돌아왔다가 다시 나가는 multi-trip이라 범위 밖이다.
`0`은 "값을 안 줬다"는 뜻이므로 `1`과 같게 본다 — 규약 기본값이 `0`이라, 이 필드를 아예
안 보낸 입력이 자연스럽게 통과해야 하기 때문이다.

| 값 | 뜻 | 처리 |
|---|---|---|
| `0` | 미설정 (규약 기본값) = 1바퀴 | **통과** |
| `1` | 1바퀴 | **통과** |
| `2` 이상 | 2바퀴 이상 = 차고 재방문 (multi-trip) | `UNSUPPORTED_INPUT` |
| `-1` | 무제한 복귀 (규약이 정의한 값) | `UNSUPPORTED_INPUT` — 규약상 유효한 값이지만 우리가 지원하지 않는다 |
| `-2` 이하 | 규약이 "greater than -1"로 금지한 범위 | `INVALID_INPUT` — 형식 오류 (§12 입력 오류) |

**판정은 비교식이 아니라 집합으로 쓴다 (MUST): 통과 = `{0, 1}`.** `> 1`로 적으면 `-1`이
게이트를 그냥 통과해 무제한 multi-trip이 솔버로 들어간다 — 지금 고치려는 것보다 나쁜 버그다.

> **규약 PDF 문면과 다르다 — 구현자가 반드시 읽을 것.**
> [ro_input_json_spec.pdf](../data/ro_input_json_spec.pdf) 4페이지의 열거 정의는 이 숫자를
> **차고 복귀 횟수**로 읽게 적혀 있다:
> `-1 : vehicles can return to depot multiple times (unlimited)` /
> `0 : vehicles can't return to depot` / `1>= : vehicles can return to depot designated multi rotation times`.
> 1바퀴 = 복귀 0회이므로 이 문면은 확정된 의미와 **한 칸 어긋난다**.
> **PDF만 보고 구현하면 판정이 정확히 반대로 나온다** (`"1"`을 거부하게 된다).
> 시스템 소유자의 확정이 정본이고, PDF 문면은 부정확한 것으로 본다.
>
> 다만 PDF 전체가 틀린 것은 아니다 — 같은 페이지를 뜯어 보면 두 곳은 확정 의미와 맞는다:
> ① `0 : can't return to depot`는 "0 = 1바퀴 = 복귀 없음"과 일치한다.
> ② `multirotation 2` 예시 그림(`depot(start) → 1st → 2nd → depot(2nd visit) → 3rd`)은
> **2를 2바퀴(복귀 1회)로 그린다** — 바퀴 수 해석 그대로다.
> 어긋나는 것은 `1>= : ... designated multi rotation times` 한 줄뿐이다.

**`trips`는 `endDepot`으로 접혀 사라진다 (설계 판단)**

정규화가 `trips`를 **차량마다 `endDepot`이 있냐 없냐로 접어** 없앤다 (위 표의 `trips` 행 =
접기 규칙). canonical `Vehicle`에는 차량별 `endDepot`(과 `startDepot`)만 남는다.

접기 표 (MUST):

| `trips` | start 입력 | end 입력 | canonical `endDepot` |
|---|---|---|---|
| `oneway` | ∅ / 있음 | 명시 | 그 값 (차고 목록 검증) |
| `oneway` | ∅ / 있음 | 부재 | empty |
| `roundtrip` | 있음 | 부재 | `:= startDepot` |
| `roundtrip` | 없음 | 부재 | **`INVALID_INPUT`** — 복귀할 차고가 없다 |
| `roundtrip` | ∅ / 있음 | 명시 | 그 값 (차고 목록 검증) |

- **차량별 `trips` 지정은 유예했다 (2026-08-11).** 현행 wire에 그 필드가 없어
  `options.trips` 하나로 접기가 완결된다. 되살릴 때는 `차량 trips ▷ options.trips`
  우선순위를 접기 직전에 한 단계 끼워 넣으면 되고, 그 값도 `{oneway, roundtrip}` 밖이면
  `INVALID_INPUT`이다 — 상세는 [Stage Extra E1](implementation/stage-extra-deferred-features.md).
- **접기를 유지한다:** canonical `Vehicle`에 `trips` 필드를 남기지 않는다.
  전파·지표·재검증이 필요로 하는 것은 "복귀 지점이 있는가/어디인가"뿐이고 그것은 `endDepot`
  하나로 완전히 표현된다. 접기로 잃는 것은 **원인 정보**다 — `endDepot`이 있을 때 그것이
  roundtrip 때문인지 명시된 도착 차고 때문인지 구분되지 않는다(`Optional.empty`만 oneway로
  단정할 수 있다). 이는 진단·설명용 정보이지 계산에 쓰이는 값이 아니므로, `multiRotation`을
  검증만 하고 보관하지 않는 것과 같은 취급으로 둔다. 운영자에게 "이 차량은 oneway였다"를
  보여줄 필요가 생기면 그때 결과 run 메타에 원문을 싣는다 (canonical 확장이 아니다).

**`multiRotation`의 차량별 지정은 지금 하지 않는다.** 통과하는 값이 `{0, 1}`뿐이라 모든 차량이
1바퀴여서 차량마다 달라질 여지가 없다. 장기적으로는 필요하다는 요구가 있으므로,
**multi-trip(`2` 이상)을 여는 시점에 차량별 `trips`와 함께 다룬다** — 둘 다 "차량 값이
`options` 기본값을 덮어쓴다"는 같은 규칙이고, 지금은 **둘 다 유예 상태**다
([Stage Extra E1](implementation/stage-extra-deferred-features.md) · Master §6).
그 전에 `Vehicle`에 `multiRotation` 필드를 미리 만들지 않는다.

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

정차 한도만 전역 값이 있다. `effectiveMaxStopCount`는 **차량 > 전역 > 없음**이다 (MUST):
차량 `maxStopCnt`가 있으면 그 값, 없으면 전역 `Optimizer.VehicleMaxStopCount`, 둘 다
없으면 **제약 없음**. 덮어쓰기이며 min이 아니다. 큰 수(sentinel)로 채우지 않는다 (MUST NOT).

`maxDriveTime`·`maxDriveDist`·용량에는 전역 한도가 없다. 그 축의 차량 값 부재는 제약 없음이며,
정차 한도의 접기 규칙과 섞지 않는다.

모든 한도 축(`maxStopCnt`·`maxDriveTime`·`maxDriveDist`·용량)은 **경계 포함**이다 (MUST) —
값이 한도와 **같으면 통과**, 넘어야 위반이다. 시간창의 양끝 포함(§3.2)과 같은 규약이다.
탐색과 재검증이 이 경계를 다르게 읽으면 안 되는 실전 근거: Win 참조 해가 정차 28회 = 한도 28
경계 위에 실재한다 (2026-08-11 실측).

---

## 3. 정규화 — 숫자·시간을 하나의 의미로

정규화 결과는 솔버와 재검증이 **공유하는 유일한 의미**다. 같은 입력이면 같은 정규화 결과가 나와야 한다.

**목록·집합의 원소가 빈 값(null)이면 `INVALID_INPUT`이다 (MUST, 2026-08-15 시스템 소유자 확정).**
차량·주문·차고·이동표 목록, item 목록, `capabilities`·`zoneIds`·`vehicleFeatureList` 같은 집합
전부에 적용한다. 관문이 `field` 경로와 함께 막으므로 **정규화 이후의 코드(전파·평가·탐색·재검증)는
원소가 null인 경우를 가정하지 않는다** — 뒤에서 null을 방어하지 말고 앞에서 못 들어오게 한다.

### 3.1 단위 (전역 고정, MUST)

| 차원 | 외부(규약) | 내부(canonical) |
|---|---|---|
| 무게 | decimal kg | `long`, kg×1000 (소수 3자리 FLOOR) |
| 부피 | decimal CBM | `long`, CBM×1000 (소수 3자리 FLOOR) |
| 거리 | integer meter | integer meter |
| 시간 | 초 | `long` 초 |
| 비용 | integer | overflow 검사하는 integer |
| 수량 | 양의 정수 | integer |

- **수치 필드의 JSON 인코딩은 number다 (MUST, 2026-08-12 확정).** `weight`·`volume`·`qty`·
  `taskTime`·`duration`·`speed`·`maxWeight`·`maxVolume`·좌표·이동표 `D`/`U`, 그리고 `options`의
  수치 값(`multiRotation`·`Optimizer.VehicleMaxStopCount`·`Termination.secondsSpentLimit` 등)을
  JSON 문자열로 보내면 `INVALID_INPUT`이다 (§12) — 문자열 표기를 병행 수용하면 소수 판정(아래)이
  '문자열의 점'과 '값' 두 기준으로 갈린다. 시각(`HH:mm:ss`)·날짜 문자열(§3.2)은 수치가 아니다.
  실물 fixture는 수치 대부분이 문자열이어서 2026-08-12에 number로 정정했다
  (`win_poc_case_floor.json`. 원본 `win_poc_case.json`은 수신 원형 그대로 보존 — 어차피 아래
  소수 거리 규칙으로 미접수다).
- `double`로 먼저 근사한 뒤 변환하지 않는다 (MUST NOT). item 단위로 먼저 환산 후 합산한다.
- **`weight`·`volume`·`taskTime`은 전부 '개당' 값이고 `× qty`로 합산한다** (MUST, 2026-08-11
  시스템 소유자 확정). 규약 PDF가 `taskTime`을 "calculated by item type not quantity"로 적어
  두었으나 **문면이 부정확한 것으로 본다** — 이 문서가 정본이다 (§3.3도 같은 규칙).
  합산은 `multiplyExact`/`addExact`로 하고 overflow는 입력 오류다.
- **단위 코드는 검증한다 (MUST, 2026-08-12 확정).** item의 `weightUnitCd`·`volumeUnitCd`가
  있으면 각각 `KG`·`CBM`이어야 하고, 다른 값은 `INVALID_INPUT`이다 — 코드를 무시하면 다른
  단위로 보낸 값이 무증상으로 kg/CBM 취급된다. 부재면 KG·CBM으로 본다 (규칙으로 정해진
  기본값, §2.1). 실물 fixture는 452건 전부 `KG`/`CBM` (PDF: "must be KG"·"must be CBM").
- 소수 거리·시간 입력은 거부한다 (2026-08-11 재확인 — FLOOR 수용으로 완화하지 않는다).
  **판정은 표기 기준이다 (MUST, 2026-08-12 확정): number 토큰에 소수점이 있으면 값과 무관하게
  거부한다** — `15.0`도 거부, `15`만 통과. 값 기준(비정수만 거부)이 아니므로 구현은 raw 토큰
  (또는 `BigDecimal` scale > 0)을 본다 — double로 파싱하면 `15.0`과 `15`를 구분할 수 없다.
  실물 원본 `win_poc_case.json`은 이동표 205,209건 중 204,756건이 이 기준의 소수(D 토큰에
  소수점 — 그중 3,558건은 `15.0`꼴 정수값)라 **접수되지 않는다**;
  정수화 전처리를 거친 `win_poc_case_floor.json`이 실행 fixture다.
- **표기 기준 판정의 적용 대상은 이동표 `D`·`U` 두 필드다 (2026-08-16 시스템 소유자 확정).**
  그래서 raw 운반체에서 `BigDecimal`인 수치는 이 둘과 무게·부피(FLOOR 대상)뿐이다.
  `duration`·`taskTime`·`maxDriveTime`·`maxDriveDist`는 raw가 `long`이라 표기가 남지 않고,
  정규화는 이 축에 소수 판정을 **하지 않는다** — 정수로 바인딩하는 것까지가 adapter 계약이다
  (Stage 1 §2.3). 이동표만 표기를 보는 이유는 소수 D·U가 실물 205,209건 중 204,756건이라
  조용히 잘리면 거리·시간이 통째로 어긋나기 때문이다.

### 3.2 시간 원점과 시간창 전개

```text
origin = planStart
normalizedTime = origin 기준 경과 초 (long)
```

- 계획 기간 `[planStart, planEnd)`. 시간창 open/close는 양끝 포함.
- **모든 소요 시간은 두 시각의 차(b − a)로 잰다 (MUST). "그 구간 안의 초 개수"를 세지 않는다.**
  창이 양끝 포함이라 `[0, 86399]` 같은 창에서 초를 세면 창마다 1초가 어긋난다. 예를 들어 23:30에
  닫히고 다음 날 00:00에 여는 창 사이의 휴식은 **차로 재면 1800초**이고 초를 세면 1799초다 —
  탐색(§9)과 재검증(§10)이 각각 구현하므로, 재는 방법이 다르면 두 코드의 숫자가 조용히 갈린다.

#### 시간창은 날마다 반복된다 (MUST)

규약이 주는 시각은 **날짜 없는 `HH:mm:ss`(partial-time) 하나뿐**이다 — 주문의
`openTime`/`closeTime`, 차고의 `openTime`/`closeTime`, 차량의 `workStart`/`workEnd` 셋 다 그렇다
(날짜가 붙는 것은 계획 기간 `dateRange`와 `reqDate`뿐이다). 그래서 "2일차만 다른 시간대"는
규약으로 **표현할 방법이 없고**, 계획 기간이 여러 날일 때 가능한 해석은 **매일 같은 시간대의
반복** 하나뿐이다. 규약 문면도 **주문 창과 차고 창에는** "닫혀 있으면 **다음 열림 시각**까지
기다린다"(*"If then, they must wailt until next opening time."* — 원문 표기 그대로)로 적어
창의 재개를 전제한다. **근무창에는 이 문장이 없다** (2026-08-11 원문 재확인 — 규약의 근무 시간
서술은 "workStartTime과 workEndTime 사이에 움직일 수 있다"뿐이다). 근무창 반복의 근거는
문장이 아니라 위의 형식(날짜 없는 partial-time) 하나다.

주문 창도 같이 반복시킨다 (MUST). 주문 창만 1일차에 고정하면 3일 계획에서 기본창
(00:00:00\~23:59:59) 주문조차 1일차에만 서비스할 수 있어, 다일 근무창이 "차고에 늦게 돌아오는"
기능으로만 남는다. 어느 날에 서비스할지를 좁히는 값은 `reqDate`(§2.3) 하나다.

정규화는 이 반복을 **계획 기간에 맞춘 절대 창 목록**으로 펼친다. canonical에서
차량의 근무창·차고의 창·방문 쪽의 시간창은 전부 **목록**이다.

전개 규칙 (MUST — 세 종류 창에 똑같이 적용):

```text
1. planStart 날짜의 '하루 전'부터 planEnd 날짜까지, 날짜마다 창 하나를 만든다.
     (하루 전까지 보는 이유는 규칙 2 — 전날 밤에 시작한 창이 계획 첫날 아침까지 이어질 수 있다.)
2. close < open이면 그 창은 자정을 넘는다 — close를 '다음 날짜'의 그 시각으로 한다.
     예: 야간조 22:00~06:00 → 그 날 22:00부터 다음 날 06:00까지.
3. close == open은 입력 오류다 — 1초짜리 창인지 24시간인지 애매하다 (§2.1 추측 금지).
4. 각 창을 계획 기간 [0, planEndSec − 1]로 자른다. 남는 부분이 없으면 버린다.
     (planEnd는 기간에 포함되지 않고 시간 단위가 초이므로, 계획의 마지막 초는 planEndSec − 1이다.)
5. open 순으로 정렬하고, 맞닿거나 겹치는 창(앞.close + 1 ≥ 뒤.open)을 하나로 합친다.
```

전개 결과의 불변식 (MUST): **정렬돼 있고, 서로 겹치지 않으며, 맞닿은 창이 남아 있지 않다.**
전파는 이 순서를 믿고 포인터를 앞으로만 밀며 훑는다 (§7.1) — 정렬·병합은 성능이 아니라
정합성 조건이다. 목록이 비면 그 차량(또는 차고)은 계획 기간에 쓸 수 없다 — **입력 오류가 아니다**
(§3.4의 "호환 차량 0대인 Request"와 같은 취급이며, 그 차량을 쓰는 경로가 불가가 될 뿐이다).

```text
예1  기본창 00:00:00~23:59:59, 3일 계획
     [0,86399] · [86400,172799] · [172800,259199] → 규칙 5로 합쳐져 [0,259199] 하나.
     (합치지 않으면 자정마다 1초짜리 틈이 남아 자정을 넘는 이동이 전부 막힌다.)
예2  근무 08:00~17:00, 3일 계획 (planStart가 1일차 자정)
     [28800,61200] · [115200,147600] · [201600,234000] — 창 사이 간격은 15시간씩.
예3  근무 08:00~17:00인데 planStart가 1일차 10:00
     1일차 창은 [0(=10:00), 25200(=17:00)]으로 앞이 잘린다.
예4  야간조 22:00~06:00
     계획 시작 '전날'에 시작한 창이 [0, 첫날 06:00]으로 잘려 들어온다 (규칙 1·2).
```

**규칙 4(planEnd 클리핑)는 새 경계다 (2026-08-10 확정).** 지금까지는 planEnd를 넘겨 끝나는
경로를 아무도 막지 않았다. 이제 창이 계획 기간에서 잘리므로 **어떤 경로도 planEnd를 넘겨
끝나지 않는다.** 하루짜리 계획이라도 planEnd가 차량의 `workEnd`보다 이르면 동작이 바뀐다
(예: planEnd 18:00, `workEnd` 23:59:59 → 근무창이 `[…, 17:59:59]`로 잘린다). 근거는 규약의
`dateRange` 설명 *"Solver will create a plan in date range."*이다.

#### 근무 구간 규칙 (MUST / MUST NOT)

- 한 이동(arc)은 통째로 한 근무창 안에 들어가야 한다. "오늘 운전하다 내일 이어서"식 분할
  금지 (MUST NOT).
- 한 방문의 **서비스도** 통째로 한 근무창 안에 들어가야 한다 (같은 이유).
- 창 끝을 넘는 이동·서비스는 **다음 창으로 미룬다** — 불가로 판정하지 않는다. 남은 창이
  없을 때만 불가다 (§7.1). 미룬 만큼이 `interWorkWindowRestTime`이다 (§7.3).

### 3.3 방문 서비스 시간

```text
serviceTime(방문) = duration + Σ(item.taskTime × item.qty)
serviceStartTime  = max(arrival, openTime)
serviceEndTime    = serviceStartTime + serviceTime
```

- 여기의 `openTime`은 **§7.1(절차 2)이 고른 적용 창의 open**이다 — 창은 목록이므로(§3.2)
  스칼라 하나가 아니고, 어느 창을 쓰는지는 §7.1이 정한다.
- order 수준 `taskTime`만 있고 item이 없는 형태는 입력 오류 (기존 유지).

### 3.4 호환성 판정 (독립 hard, 전부 AND)

```text
sizeCompatible       = vehicle이 전 차급(vehicleFeature 부재·"ALL")  OR  list == ["ALL"]
                       OR  vehicle.vehicleFeature ∈ request.vehicleFeatureList
capabilityCompatible = request가 capability 미요구  OR  요구 ⊆ vehicle.capabilities
zoneCompatible       = vehicle이 전 구역(zoneIds 미입력, 또는 ["ALL"] 단독)
                       OR  방문 zoneId 부재·"ALL"  OR  방문 zoneId ∈ vehicle.zoneIds
                       (존재하는 side만 AND — 없는 side는 검사하지 않는다)
depotAnchors         = DELIVERY_ONLY → vehicle.startDepot 존재
                       PICKUP_ONLY   → vehicle.endDepot 존재
                       PICKUP_DELIVERY → 참 (start/end 각각 optional)
```

**차량 쪽 `"ALL"`·부재는 와일드카드다 (2026-08-12 확정).** 규약 PDF가 차량 `vehicleFeature`의
Default를 `"ALL"`로 정의하므로, 생략됐거나 `"ALL"`인 차량은 **전 주문과 차급 호환**이다 —
주문 쪽 `["ALL"]`(전 차급 허용)과 대칭이고, "optional 부재 = 그 축 제약 없음"(§2.4)과 같은
철학이다. 정규화는 부재와 `"ALL"`을 같은 상태(전 차급)로 접는다. 문자 비교만 하면 이런 차량이
어느 주문도 싣지 못하는 유령 차량이 된다 — 종전 문면의 결함이었다.

**구역의 `"ALL"`도 와일드카드다 (2026-08-15 시스템 소유자 확정).** `"ALL"`은 **구역 이름이 될 수
없는 예약어**이고, **목록에는 단독(`["ALL"]`)으로만 올 수 있다.** 차량 `zoneIds`가 `["ALL"]`이면
전 구역 운행이고, 다른 구역과 섞인 `["ALL","ZONE_1"]`은 `INVALID_INPUT`이다 — 전 구역인지 그
구역만인지 애매하다 (§2.1 추측 금지. 주문 차급의 `["ALL","T1"]`과 **같은 규칙**이다).
방문 쪽 `zoneId`가 `"ALL"`·빈 문자열·부재면 그 방문에 구역 제약이 없다 (2026-08-15 확정 — 부재를
`"ALL"`과 같게 본다).
정규화가 양쪽을 **같은 상태(제약 없음)로 접으므로** 판정은 "빈 값이면 통과" 하나만 본다 —
차량 차급과 같은 방식이다. 실물 wire는 차고 `zoneId`가 `"ALL"`이고 주문
452건은 전부 구체 구역(`ZONE_15`\~`ZONE_29` 11종), 차량 31대는 `zoneIds` 키 자체가 없다 (실측).

**치수 축은 없다 (2026-08-11 유예).** `Item`에 치수가 없어 검사 대상 자체가 없었다.
3D 적재 고객이 확정되면 §2.1.1 경로로 되살리고, **판정은 core `Compatibility`가 아니라
그 고객 profile의 `HardConstraint`가 한다** ([Stage Extra E2](implementation/stage-extra-deferred-features.md)).

호환 차량이 0대인 Request는 구조 오류가 아니다 — 풀이는 진행되고 그 Request는 미배정+사유로 남는다.
`DELIVERY_ONLY`인데 모든 차량이 `startDepot` 없거나, `PICKUP_ONLY`인데 모든 차량이 `endDepot`
없어도 같다 — 정규화는 통과하고 사유는 `NO_COMPATIBLE_VEHICLE`이다 (§11.1). 혼합 차대
(일부만 차고 출발, 일부만 복귀)는 유효하다.

---

## 4. 이동표 준비 (travel)

키 = **`LocationId` 쌍** (방향 있음). 탐색·재검증 모두 **준비된 표만** 사용한다 (MUST).
탐색 중 좌표로 즉석 계산하거나 대칭화하지 않는다 (MUST NOT).

| 값 | 뜻 | 규칙 |
|---|---|---|
| `D` | 거리 (integer meter) | 소수 거부. self arc는 입력과 무관하게 0 — 아래 주석 |
| `U` | 시간 (integer second) | 소수 거부. self arc는 입력과 무관하게 0 — 아래 주석 |
| `C` 등 기타 | 참고 값 | **feasibility·점수에 사용 금지** (MUST NOT) |

**self arc는 이동표 준비가 0으로 확정한다 (MUST, 2026-08-17 원복).** 출발지 == 도착지면
움직이지 않으므로 **D = 0 m · U = 0초**다. 서로 다른 두 Request가 같은 `LocationId`를 쓰는
것은 **정상 입력**이고(§7.4의 stopCount가 같은 장소 연속 방문을 "첫 진입만 +1"로 세는 것이
그 전제다), 그 사이 arc는 이동이 아니라 0이다.

일은 두 단계다. **입력 self 행을 버리는 것**은 정규화(§3) — 값을 읽지 않으므로 소수 검사
대상도 아니다. **표의 대각에 0을 기입하는 것**은 이 절의 준비. 정규화가 `TravelEntry(self, 0, 0)`을
만들어 남기면 "입력 self 값은 읽지 않는다"가 깨진다.

**퇴화 경로 금지는 값이 아니라 구조가 소유한다 (MUST).** 2026-08-12~08-16에는 "유효한 경로에
self arc가 나타나면 안 된다"를 근거로 큰 수(D = 999,000 · U = 86,400)를 강제했으나 그 전제가
틀렸다 — 금지 대상은 **같은 노드를 두 번 방문하는 것**과 **차고를 방문으로 위장하는 것**이지,
같은 장소를 다시 밟는 것이 아니다. 그 둘은 §6.2가 집행한다: 차고는 `NodeId`를 갖지 않고(§1.3)
경로는 `NodeId`만 담아 가상 depot을 표현할 수 없으며, 같은 `NodeId` 두 번은 방문 유일 위반,
없는 `NodeId`는 참조 위반이다. 검사 시점은 §6.5(구조 검사 → 정식 평가)와 재검증(§10)이다.
거리·시간 값으로 벌점을 매기면 정상 입력에 24시간이 붙어 **오답**이 된다 — 같은 모델이 한
지점을 "정차 1회"(§7.4)이자 "24시간 거리"로 보는 모순이었다.

입력 이동표의 self arc 행은 **값을 읽지 않고 버린다** — 있어도 없어도 되고, 소수 거부의 검사
대상도 아니다 (실물 wire는 self arc 453건 전부 D=9999·U=0을 보내는데, 이는 legacy sentinel이지
실거리가 아니다 — fixture 실측). §5 규칙 4의 이동표 완전성 검사에서 self arc는 누락으로 세지
않는다.

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
                                        start 없는 V2: 첫 고객에서 경로 시작 (앞 arc 없음)
```

### 6.2 경로가 유효하려면 (MUST)

| 규칙 | 내용 |
|---|---|
| 차량 하나 | 한 경로는 정확히 한 `VehicleId` 소유 |
| 출발/도착 | `startDepot` 있으면 거기서 출발, 없으면 첫 고객에서 시작. `endDepot` 있으면 거기서 종료, 없으면 마지막 고객에서 종료 |
| pair | 같은 Request의 **있는 방문**이 같은 경로에. 둘 다 있으면 pickup 먼저 (§1.4) |
| 방문 유일 | 해 전체에서 같은 `NodeId`는 **한 번**. 같은 장소(`LocationId`)의 다른 방문은 허용 (§4·§7.4) |
| 참조 | 경로의 `NodeId`는 `Problem`에 있는 것만. 없는 side를 방문으로 넣으면 여기 걸린다 (§1.3) |
| `servicePattern` | `DELIVERY_ONLY`는 delivery 방문만, `PICKUP_ONLY`는 pickup 방문만 (가짜 픽업·하차 방문 금지) |
| 적재 | 모든 구간에서 `0 ≤ load ≤ capacity`. 아래 부호 규칙 |
| 이동 | 준비된 이동표만 사용 |
| hard | 시간창·근무창·**차고 창**·`reqDate`·한도 축(`maxStopCnt`·`maxDriveTime`·`maxDriveDist` — §2.6 경계 포함) 전부 충족 |
| 차고 재방문 | 경로 중간의 depot 재방문 금지 (multi-trip 비범위) |

**적재 부호 규칙 (MUST)**

```text
initialLoad(route) = Σ 그 경로에 배정된 DELIVERY_ONLY request의 수요   // 출발 전 적재

DELIVERY_ONLY   +startDepot(방문 아님)  −delivery방문
PICKUP_ONLY     +pickup방문             −endDepot도착(방문 아님)
PICKUP_DELIVERY +pickup방문             −delivery방문

경로 종료 시 load = 0            // 미완료 pair로 우회 금지
```

- `PICKUP_ONLY`는 `initialLoad`에 넣지 않는다. 집하 방문에서 늘고, `endDepot` **도착 사건**에서
  그 경로에 배정된 `PICKUP_ONLY` 수요만 뺀다.
- endDepot 도착에서 "남은 짐 전부 하차"로 뭉개지 않는다 (MUST NOT) — 배송 잔량과 backhaul을
  구분하지 못하게 되고, 미하차 `DELIVERY_ONLY`/`PICKUP_DELIVERY` 버그를 숨긴다.
- `delivery` 칸에서 수량 부호만 뒤집는 backhaul 우회, pickup=고객·delivery=depot 좌표로 차고를
  `NodeId` 방문으로 만드는 우회는 금지한다 (MUST NOT).

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
                 →  구조 검사(pair·XOR·방문 유일·참조) → 정식 평가(시간·용량·점수)
                 →  더 좋으면 draft를 새 확정 Solution으로 승격 (accept)
                 →  아니면 draft 폐기 (확정 해는 그대로)
```

`current`(현재 기준 해)와 `best`(지금까지 최선 해)는 서로 다른 확정 Solution일 수 있다.
한 객체를 별칭으로 공유하지 않는다.

---

## 7. 전파 (propagation) — 순서가 정해진 경로의 물리 계산

방문 순서가 주어졌을 때 앞에서 뒤로 한 번 훑으며 **도착·대기·서비스·적재·주행 사실**을 계산하는
단계다. 점수·선호 판단은 하지 않는다 (그건 §8).

### 7.1 출발·방문·종료 절차

전제: 차량의 근무창 목록, (있으면) 출발 차고의 창 목록, (있으면) 도착 차고의 창 목록은 전부 §3.2로
전개·병합된 목록이다. 경로를 따라 시각은 줄지 않으므로 각 목록은 포인터를 앞으로만 밀며 훑는다.

```text
0. 출발  — startDepot 있음
   spanStart = 첫 근무창의 open                      // 경로 시간의 기준점 (§7.3)
   departure = 아래를 모두 만족하는 가장 이른 시각
       · 어느 근무창 안                                (근무 중에만 움직인다)
       · 어느 startDepot 창 안                         (차고가 열려 있을 때만 나간다)
       · 첫 이동이 그 근무창 안에 통째로 들어간다       (§3.2)
     그런 시각이 없으면 → 근무창이 원인이면 WORK_WINDOW, 차고 창이 원인이면 DEPOT_WINDOW
   waitInDepot = Y면 출발을 늦춘다 (MUST — 정밀 규칙, 2026-08-11 정본화):
     departure = 위 세 조건을 만족하면서 arrival ≤ 첫 방문 serviceStart인 **가장 늦은** 시각.
     첫 방문 serviceStart는 **N 출발을 가정하고 먼저 계산**한다 (N의 출발 시각도 조건을
     만족하므로 후보 집합은 비지 않는다). Y는 늦추기만 한다 — serviceStart 시각들은 N과 같고,
     대기의 귀속만 옮겨진다 (§7.3)

0'. 출발 — startDepot 없음
   spanStart = 첫 근무창의 open
   앞 arc 없음. 차고 창 검사 없음. waitInDepot 미적용 (적용할 차고가 없다 — 오류가 아니다).
   첫 방문 arrival = spanStart. 이어서 절차 2부터 (고객 창·근무창에 서비스 배치).
   depotDeparture 부재 (§11.1 — depotReturn이 endDepot 없을 때 부재인 것과 대칭)

1. arrival        = 직전 출발 시각 + 이동표[직전 장소 → 이번 장소]
                    (이동을 통째로 담는 창을 골라 출발했으므로 arrival은 항상 근무창 안이다)
2. serviceStart   = arrival 이상이면서 아래를 모두 만족하는 가장 이른 시각
                    · 그 방문의 시간창 중 하나 안                   // 이르면 대기
                    · 서비스 전체 [t, t + serviceTime]이 한 근무창 안 // 닫혔으면 다음 창까지 쉼
                    시간창이 먼저 소진되면 TIME_WINDOW, 근무창이 먼저 소진되면 WORK_WINDOW
3. serviceEnd     = serviceStart + serviceTime      // §3.3
4. reqDate 검사    : serviceStart ≤ reqDate          // §2.3. serviceEnd는 조건 아님
5. load 갱신       : 픽업 +, 배송 −                   // §6.2 부호 규칙
6. hard 검사       : 이 절차가 새로 판정하는 것은 **용량과 한도(경로 누적, §2.6)**뿐이다
                    (시간창(serviceStart ≤ closeTime)·근무창은 절차 2·7이, 차고 창은 절차 0·8이 판정한다)
7. departure      = serviceEnd 이상이면서 다음 이동을 통째로 담는 가장 이른 시각
                    창 끝을 넘으면 다음 창으로 미룬다. 남은 창이 없으면 WORK_WINDOW
                    (근무창이 하나뿐이면 항상 departure = serviceEnd다 — 종전과 같다)

8. 종료
   endDepot 없음 : routeEnd = 마지막 serviceEnd
   endDepot 있음 : routeEnd = endDepot 도착 시각.
                   그 시각이 endDepot 창 어느 것에도 들어가지 않으면 DEPOT_WINDOW
                   도착 사건에서 그 경로에 배정된 PICKUP_ONLY 수요만 뺀다 (§6.2).
                   잔량을 0으로 대입하지 않는다 (MUST NOT)
```

- **시간창 close 판정 기준은 `serviceStart`다 (MUST).** 그 방문에서 `serviceStartTime ≤ closeTime`이면
  통과이고, 서비스가 창을 넘겨 끝나는 것(`serviceEndTime > closeTime`)은 위반이 아니다 —
  `serviceEndTime`을 조건에 넣지 않는다 (MUST NOT). `reqDate` 규칙(§2.3)과 같은 형태다.
  같은 규칙을 탐색(§9)과 재검증(§10)이 각각 따로 구현하므로, 두 곳이 다른 식을 쓰면 같은 배차안을
  두고 가능/불가가 갈린다.
  **시간창이 여럿이면(§3.2) 판정은 "`serviceStart`가 어느 창 안인가"다** — 그 창에서
  `open ≤ serviceStart ≤ close`. 창 **사이의 틈은 창 안이 아니므로** 그때는 다음 창까지 기다린다
  (마지막 창의 close 하나만 보는 구현은 틈에서 시작하는 서비스를 통과시킨다 — 금지). 남은 창이
  없으면 `TIME_WINDOW`다. 어느 경우에도 `serviceEnd`는 조건이 아니다.
  (**근무창은 이와 다르다** — 서비스는 근무창을 넘겨 끝날 수 없다. 고객의 close는 "언제까지
  받아 주는가"이고, 근무창은 "언제까지 일할 수 있는가"라 성격이 다르다.)
- **차고 창은 있는 출·도착 순간에만 적용한다 (MUST).** `startDepot`이 있으면 출발 순간,
  `endDepot`이 있으면 도착 순간. 없는 쪽은 검사하지 않는다. 규약이 *"All vehicles must
  **depart and arrive** between opening time and closing time of depot."*로 정의하는 것은
  차고가 있을 때의 규칙이다. 차고 창도 날마다 반복되므로(§3.2) 판정은 "어느 창 안인가"이지
  "어느 하루의 close 이하인가"가 아니다.
- **복귀는 미루지 않는다** (2026-08-10 확정). 도착 시각이 차고 창 밖이면 그대로 `DEPOT_WINDOW`이며,
  "문 열 때까지 기다렸다 들어간다"로 미루지 않는다 — 경로는 거기서 끝나므로 미뤄 봐야 마지막
  고객 지점에서 하루를 버릴 뿐이다. 나중에 완화하더라도 canonical은 그대로이고 전파 규칙만
  느슨해지는 **순수 완화**다.
- 근무창·차고 창 검사는 **별도의 양 끝점 비교가 아니다.** 절차가 이동·서비스를 창 안에
  배치하고, 배치할 창이 없을 때만 위반이다. 재검증은 배치 결과가 실제로 창 안에 있는지
  한 번 더 훑어 확인한다 (§10.2).
- 어떤 이동·서비스가 **어느 창에도 들어갈 수 없으면**(예: 9시간 창에 12시간짜리 이동) 남은 창을
  아무리 넘겨도 통과하지 못하므로 `WORK_WINDOW`다.
- **위반 귀속 (2026-08-11)**: 한 지점에서 두 창 축이 **같은 시각에 함께 소진**되면 절차 문장에
  먼저 적힌 쪽을 기록한다 — 절차 0(출발)은 `WORK_WINDOW`, 절차 2(서비스)는 `TIME_WINDOW`.
  가능/불가 판정은 달라지지 않지만, 탐색과 재검증이 FAILED 원인 종류까지 같게 내기 위한 규약이다.

### 7.2 숫자 예 (단위 분·kg, V1 capacity 30)

```text
경로: 차고A(08:00 출발) → R2픽(창 09:00–12:00, 서비스 5분, reqDate 10:00)
                       → R1배(창 13:00–18:00, 서비스 14분, reqDate 15:00)
                       → R2배(창 13:00–18:00, 서비스 5분, reqDate 15:00)
이동: 차고→픽 40분, 픽→R1배 50분, R1배→R2배 10분
```

| 단계 | 시각 | load |
|---|---|---|
| 차고A 출발 | 08:00 | **10** (R1이 DELIVERY_ONLY라 출발 적재) |
| R2픽 도착 | 08:40 → 창 열릴 때까지 대기 | 10 |
| R2픽 서비스 | 09:00–09:05, reqDate 검사 09:00≤10:00 통과 | 픽업 후 **20** |
| R1배 도착 | 09:55 → 13:00까지 대기 | 20 |
| R1배 서비스 | 13:00–13:14, reqDate 검사 13:00≤15:00 통과 | 배송 후 **10** |
| R2배 도착 | 13:24 (departure 13:14 + 10분) | 10 |
| R2배 서비스 | 13:24–13:29, reqDate 검사 13:24≤15:00 통과 | 배송 후 **0** — 종료 load 0 (§6.2), pair 완결 (§6.3) |

만약 serviceStart가 16:00이고 reqDate가 15:00이면 `16:00 ≤ 15:00` 거짓 → 그 경로는 **불가**.
전파는 가능/불가 **사실**만 알려주고, 점수로 덮지 않는다.

이 예는 근무창이 하나인 하루짜리 계획이다. 창이 하나뿐이면 미루기가 일어날 수 없고
`interWorkWindowRestTime`은 0이므로, **위 숫자는 §3.2 전개 규칙이 생기기 전과 똑같다.**

### 7.2.1 숫자 예 — 다일 (창을 넘길 때)

```text
계획: 3일 (planStart 1일차 00:00, planEnd 4일차 00:00)
근무: 매일 08:00~17:00 → 전개하면 [28800,61200] · [115200,147600] · [201600,234000]  (§3.2 예2)
```

| 상황 | 계산 | 결과 |
|---|---|---|
| 2일차 **16:30**(=145800)에 90분(5400초) 이동을 시작하려 함 | `145800 + 5400 = 151200 > 147600`(2일차 17:00) → 그 창에 통째로 안 들어감 | 다음 창(**3일차 08:00** = 201600)으로 **미룬다** |
| 미룬 시간의 귀속 (§7.3) | 55800초 중 근무창에 걸친 `[145800, 147600]`의 1800초는 고객 지점 대기, 창 사이 `[147600, 201600]`의 54000초만 휴식 (2026-08-11 정정 — 전액 휴식은 §7.3 공식과 모순) | `customerWaitingTime += 1800` · `interWorkWindowRestTime += 54000` |
| 도착 | `201600 + 5400 = 207000` | 3일차 **09:30** |
| 만약 2일차 **16:00**(=144000)에 서비스가 끝나고 같은 이동을 한다면 | 출발까지 57600초를 기다린다. 그중 **근무 시간에 걸친 3600초**(16:00\~17:00)는 고객 지점에서의 대기, **창 사이 54000초**는 휴식 | `customerWaitingTime += 3600`, `interWorkWindowRestTime += 54000` (§7.3) |

이동이 어느 창에도 통째로 들어갈 수 없으면(예: 9시간 창에 12시간 이동) 미뤄도 소용없으므로
`WORK_WINDOW` 위반이다.

### 7.3 기록하는 값 (재검증이 같은 공식으로 재합산할 수 있어야 함)

`arrival`, `serviceStartTime`, `serviceEndTime`, **`departure`**, `loadWeight`, `loadVolume`,
`distance`, `driveTime`, `customerWaitingTime`, `depotWaitingTime`, `serviceTime`,
`interWorkWindowRestTime`, `stopCount`,

```text
routeOperationalTime = driveTime + customerWaitingTime + depotWaitingTime
                     + serviceTime + interWorkWindowRestTime
```

방문의 `departure`는 지금까지 `serviceEnd`와 항상 같아 따로 기록하지 않았다. 근무창이 여럿이면
둘이 달라지므로(다음 창까지 쉬고 출발 — §7.1 절차 7) **별도로 기록한다.**
후속 이동이 없는 **마지막 방문**의 `departure`는 `serviceEnd`다 (절차 7의 "다음 이동"이 없다 —
대기 합산의 마지막 항이 0이 되어 아래 항등식이 유지된다).

**대기 3종의 정의 (MUST)** — 경로 시간 중 **근무창 사이의 틈에 있는 시간은 전부**
`interWorkWindowRestTime`이고, 근무 시간 중 기다린 것만 고객·차고 대기다:

```text
spanStart               = 첫 근무창의 open                       // §7.1 절차 0
customerWaitingTime     = Σ 방문마다 [(serviceStart − arrival) + (departure − serviceEnd)]
                          중 근무창에 걸친 부분     // 방문 지점에서 근무 시간 중 기다린 시간
depotWaitingTime        = startDepot 있음: [spanStart, 출발 시각] 중 근무창에 걸친 부분
                          startDepot 없음: 0
interWorkWindowRestTime = (routeEnd − spanStart) − (그 구간 중 근무창에 걸친 시간)
```

이동과 서비스는 창 하나 안에 통째로 들어가므로(§3.2) 틈에 걸칠 수 없다. 따라서 위 다섯 성분이
경로 시간을 빠짐없이·겹치지 않게 나누고, 다음 **항등식**이 성립한다 — 탐색과 재검증이 어긋났는지
보는 가장 싼 검사다:

```text
routeOperationalTime = routeEnd − spanStart          // routeEnd = §7.1 절차 8
```

근무창이 하나면 `interWorkWindowRestTime = 0`, `departure = serviceEnd`,
`depotWaitingTime = 출발 시각 − 근무 시작`이 되어 **종전 정의와 값이 같다.**

### 7.4 stopCount와 주행 (기존 유지)

```text
stopCount: 서로 다른 연속 고객 장소 그룹마다 +1 — 첫 고객 방문도 +1이다 (2026-08-12 확정:
           직전 고객 장소가 없어도 센다). depot는 세지 않음. 같은 장소 연속 방문은 첫 진입만 +1.
           검산: Win 참조 해 V027·V030 = 방문 28곳 = 정차 28 = 한도 28 경계 (§2.6과 정합 —
           첫 방문을 안 세면 27이 되어 경계 실측이 성립하지 않는다)
driveDist = Σ 실제 지난 D (meter)      // (있으면) startDepot→첫고객, 고객→고객, (있으면) 마지막→endDepot
driveTime = Σ 실제 지난 U (second)     // 대기·서비스·휴식은 불포함. start 없으면 앞 arc를 더하지 않는다
```

### 7.5 하면 안 되는 것 (MUST NOT)

- hard 위반을 "감점하고 통과"시키기 — hard가 깨지면 그 경로는 불가다.
- 이동·서비스를 근무창 경계에서 쪼개기 ("17:00까지 운전하고 나머지는 내일") — §3.2 위반.
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
  비교기 자체는 사전식 하나뿐이지만, profile이 축 하나에 가중합(`w1·a + w2·b`)을 담으면 그대로
  가중합 비교가 된다 — **구조가 막아 주지 않으므로** 이 MUST NOT은 리뷰로 지킨다
  (§13 체크리스트 15).
- 크게 만들고 싶은 축은 부호를 뒤집어 넣는다 (전 축이 최소화 대상이라는 규칙을 깨지 않는다).
- 같은 profile이 만드는 축 목록의 **길이는 항상 같다.** 다르면 버그다.
- 소유 구분(LEASE/DIRECT) 비용 축은 **지금 없다** — canonical에 소유 필드를 담지 않기
  때문이다 (2026-08-11 유예, §2.4 유예 표). 되살릴 때는 축을 하나 늘리는 일이라
  **재검증의 점수 재계산도 같이 바뀐다** ([Stage Extra E3](implementation/stage-extra-deferred-features.md)).

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
| 검사 | **유효 조건의 정본을 참조한다 (2026-08-12 참조형 전환)**: §6.2 표 전 항목(pair·servicePattern·적재 곡선·종료 load=0·이동표 사용·시간창·`reqDate`·근무창·차고 창·maxStop·maxDrive 한도) + §6.3 배정 XOR + §3.4 호환성 + §2.6 경계 포함(≤) + profile hard(§8.4) — 전부 캐시 없이 재계산한다. 근무창·차고 창은 재계산한 시각이 실제로 창 안에 있는지 훑어 확인한다 (모든 이동·서비스가 한 근무창 안, **있는** 차고 출발·복귀 순간이 그 차고 창 안 — §7.1). 그리고 metric(③)과 score 축(④)을 각각 재계산해 탐색이 보고한 값과 대조. **여기서 목록을 따로 유지하지 않는다** — hard 축이 늘면 §6.2가 늘고 재검증은 자동으로 따라간다 |
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

이 절은 결과에 담기는 **의미의 목록**이다 — wire 필드명·형태는 §11.2(Plan D2 협의)가
소유하며, 아래 이름은 의미를 가리키는 표기이지 wire 표기의 확정이 아니다.

```text
planId, status (DONE | FAILED)
run   : inputKey(접수 시 S3 key), 접수·시작·종료 시각, 사용한 profileId,
        재검증 통과 여부(항상 PASS다 — FAILED 결과는 생산 경로가 없다(아래). 재검증이
        실행됐다는 증빙으로만 남는다)
        적용된 배송정책(§2.5)과 탐색 예산(§2.5.1) — 둘을 **따로** 기록한다.
        같은 문제를 다른 예산으로 돌린 결과를 구별할 수 있어야 한다 (벤치마크 비교의 전제)
routes: 차량별로 —
  vehicleId
  visits[]: orderId(RequestId), locationId, arrival, serviceStart, serviceEnd,
            loadWeight·loadVolume (적재 2축 — §7.3)
            (방문의 `departure`(§7.3)는 계산·기록되지만 결과 노출 여부는 wire 협의 항목이다 —
             다일 계획에서는 `serviceEnd`와 달라진다)
  경로 시각: depotDeparture(차고 출발 — §7.1 절차 0, startDepot 있을 때만),
            depotReturn(도착 차고 도착 — §7.1 절차 8, endDepot 있을 때만). 2026-08-11 추가,
            2026-08-14 start 부재와 대칭. 이 값이 없으면 호출 측이 이동표 없이는
            "차가 몇 시에 차고를 나서는가/돌아오는가"를 알 수 없다. wire 필드명·형식은 협의(§11.2, Plan D2)
  경로 지표: driveDist, driveTime, stopCount, routeOperationalTime
unassigned[]: orderId + reason — 4종 고정(NO_COMPATIBLE_VEHICLE, CAPACITY,
              TIME_WINDOW_INFEASIBLE, NOT_PLACED), 판정 규칙은 아래 (2026-08-12 확정)
metrics: unassignedCount, usedVehicleCount, totalDistance, totalRouteOperationalTime
```

- 미배정 사유는 결과 생성 시 계산해 붙인다 (bank는 ID만 갖고 있으므로, §6.3).
  탐색이 남긴 bank가 그대로 결과가 되는 게 아니라 **재검증을 통과한 해의 미배정 집합**이 결과다.
- **reason 판정 규칙 (MUST, 2026-08-12 확정)** — 요청마다 아래를 순서대로 검사해 **첫 번째로
  해당하는 값**을 붙인다 (순서 고정 = 결정적. 전파가 필요 없는 정적 검사를 앞에 둔다):
  1. `NO_COMPATIBLE_VEHICLE` — 호환 차량이 0대 (§3.4 동결 사실).
  2. `CAPACITY` — 호환 차량 전부가 이 요청 하나도 싣지 못한다 (총수요 > 차량 최대 용량,
     정적 비교).
  3. `TIME_WINDOW_INFEASIBLE` — 남은 호환 차량 전부에서 **빈 경로에 이 요청만** 넣은 단독
     경로가 전파(§7)로 불가 — 시간창·`reqDate`·근무창·차고 창·주행 한도 계열 전부 이 bucket이다.
  4. `NOT_PLACED` — 단독으로는 가능한데 이번 해에서 자리 경합으로 밀렸다.
  비용은 |미배정| × |호환 차량| 회의 단독 전파뿐이다. 절차 상세는 Stage 5 §4.3이 구현 계약으로 갖는다.
- 식별자는 규약과 같은 이름(`orderId`, `vehicleId` 등)을 쓴다.
- `status`의 `FAILED`는 열거 호환용이다 — **FAILED 결과 JSON은 생산 경로가 없다** (2026-08-11 명시).
  재검증에 실패한 배차안은 결과를 저장하지 않으므로(§10.2) result.json은 언제나 `DONE`이고,
  실패는 status.json에만 남는다 (Architecture §3.3).
- 이전 설계의 fingerprint·algorithm lineage 등 추적 장치는 위의 간단한 `run` 메타로 대체 (확정).

### 11.2 wire 확정

규약 PDF는 **입력만** 정의하고 결과 형식은 정의하지 않는다. 위 §11.1이 결과의 **의미 정본**이며,
최종 wire 필드명·배치는 호출 시스템과 협의해 [Implementation Plan](implementation-plan.md) 단계에서
확정한다 (의미 변경 없이 이름·형태만).

---

## 12. 오류 분류

| 분류 | 예 | 처리 |
|---|---|---|
| 입력 오류 | 스키마 위반, 소수 거리, 수치를 문자열로 인코딩(§3.1), `close == open`인 시간창(§3.2), pickup·delivery 둘 다 없는 Request(§1.3), `trips=roundtrip`인데 접을 차고가 없음(end 부재 + start 부재, §2.5), 명시한 start/end가 차고 목록 밖(§2.4), `multiRotation ≤ -2`(규약이 금지한 값, §2.5) | 접수 시 4xx (S3 저장 없음) |
| 미지원 입력 | `multiRotation`이 `{0, 1}` 밖 — 즉 `-1` 또는 `2` 이상 (§2.5) | `UNSUPPORTED_INPUT` — 접수 거부 |
| Problem 생성 실패 | ID 참조 깨짐, 이동표 불완전 | FAILED 상태 + 원인 |
| 탐색 중단 | 시간 한도 도달 | 그 시점 best로 재검증 진행 (정상) |
| 구조 결함 | pair 분리, XOR 위반, 같은 `NodeId` 두 번, 없는 `NodeId`, 캐시≠재계산 | 버그. 재검증 FAIL → 결과 미저장 |
| 재검증 FAIL | hard 위반 발견 | FAILED 상태 + 원인. 결과 저장 금지 |

---

## 13. 구현·리뷰 체크리스트

| # | 질문 | 기대 |
|---|---|---|
| 1 | destroy/repair가 pair 단위인가? | 예 |
| 2 | 각 Request가 경로 또는 bank 정확히 하나에 있는가? | 예 |
| 3 | `DELIVERY_ONLY`에 픽업 방문이, `PICKUP_ONLY`에 하차 방문이 생기지 않는가? | 예 (각각 initial load / endDepot 도착 하차만) |
| 4 | 탐색이 `Problem`·이동표를 수정하지 않는가? | 예 |
| 5 | bank에 사유 문자열을 저장하지 않는가? | 예 (ID만) |
| 6 | hard 위반을 감점으로 통과시키지 않는가? | 예 |
| 7 | 같은 해의 캐시 점수 = 전체 재계산 점수인가? | 예 (다르면 버그) |
| 8 | core에 고객명 분기가 없는가? | 예 (profile만) |
| 9 | 탐색과 재검증이 같은 profile 인스턴스·이동표를 쓰는가? | 예 |
| 10 | 재검증 FAIL 시 결과가 저장되지 않는가? | 예 |
| 11 | 미등록 customerId가 default profile로 풀리는가? | 예 |
| 12 | optional 필드 부재 시 그 축 제약이 사라지는가? `startDepot` 부재를 단일 차고로 채우지 않는가? | 예 (몰래 채움 없음 — 현 규약 wire 채움은 Stage 6) |
| 13 | `Problem`에 profile·탐색 예산이 들어 있지 않은가? | 예 (§5) |
| 14 | 재검증이 시간·step·idle 한도를 참조하지 않는가? | 예 (§2.5.1·§10.2) |
| 15 | 여러 축을 한 숫자로 뭉개는 비교가 없는가? | 예 — 사전식 비교 하나뿐 (§8.3) |
| 16 | 고객별로 갈라진 canonical·`Problem`·adapter가 없는가? | 예 (§2.1.1) |
| 17 | 모든 이동·서비스가 한 근무창 안에 통째로 들어가는가? | 예 — 창을 넘으면 다음 창으로 미룬다 (§3.2·§7.1) |
| 18 | 차고 창을 **있는** 출·도착에 적용했는가? | 예 (§7.1 — 위반은 `DEPOT_WINDOW`. 없는 쪽은 검사하지 않는다) |
