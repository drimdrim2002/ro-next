# 감사 보고 — 분할 2: `docs/domain-design.md` §1~§5

감사일 2026-08-11~12 · 초점: fixture 대조(D6) · 단위·시간창 규칙의 오독 여지(D1)

## 0. 필수 선행 보고

### 0.1 대상 파일 실제 줄 수

```
$ wc -l docs/domain-design.md
     989 docs/domain-design.md
```

담당 범위 = 1~530행 (frontmatter + §1~§5. §6은 531행부터 — 분할 3 소관).

### 0.2 기계 검사 5종 — 명령과 건수

| 검사 | 명령 (범위 = 1~530행 추출본) | 건수 | 결과 |
|---|---|---|---|
| a. 링크 | `grep -oE '\]\(([^)]+)\)'` → 대상 존재 확인 | 5개 대상 (+frontmatter supersedes 1) | **끊어진 링크 0** — 6개 전부 실재 |
| b. 전칭어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|유일\|항상\|절대\|언제나'` | 23건 | 전 건 판정 §3.1 — 실측 대상 9건 전부 검증됨, 판정표 참조 |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토'` | 21건 | 전 건 판정 §3.2 — **D5 잔존 0건** |
| d. 참조 | `grep -nE '§[0-9]\|Stage [0-9A-Za-z]\|위 표\|아래 주석\|이미 .*는'` | 82건 | 전 건 판정 §3.3 — 앵커 전부 실재, 끊어진 참조 0 |
| e. fixture | Python 스크립트 3종 (scratchpad `fixture_check{,2,3}.py`) + pdftotext | 아래 실측표 | 결함 다수 — §2 본문 |

fixture 실측 요약 (win_poc_case_floor.json / win_poc_case.json / alns_result.csv / ro_input_json_spec.pdf):

```
orders 452 · vehicles 31 · distanceMatrix 205,209 (= 453²) · depot 1(배열)
차량 키 합집합 = {vehicleId, vehicleFeature, maxWeight, maxVolume,
                 workStartTime, workEndTime, speed} 7종, 각 31/31        ← §2.4 주장과 일치
자기 arc(F==T) 453건 전부 D=9999, U=0 (원본·floor 동일) + 비자기 D=9999 1건
원본 소수: D에 점 포함 204,756 / 행 기준 any 204,756 / 값이 비정수인 행 204,047
item: 소수 weight 436 · 소수 volume 449 / qty 전부 "1" / itemId 전부 "" (452건)
수치 인코딩: weight·volume·qty·duration·maxWeight·speed·options 전부 JSON 문자열,
            distanceMatrix D·U와 item.taskTime만 JSON number (혼재)
options 10키: trips oneway · multiRotation "1" · driverRestTimeRatio "0.00" ·
  difficultySortType "WINCOMMERCE" · Optimizer.VehicleMaxStopCount "28" ·
  distanceTimeCalculate "GreatCircle" · Termination.secondsSpentLimit "600" ·
  waitInDepot "N" · Optimizer.DefaultSpeed "45" · customerAbbr "WCM"
Win 결과(alns_result.csv): 배정 452/452 · 차량 31대 · 최대 정차 28회 (V027·V030)
시각 형식: 주문 open/close·차고 open/close·근무 전부 HH:mm:ss, dateRange·reqDate만 날짜 포함
open==close 주문 0건 · close<open 주문 0건 · reqDate 452건 전부 planEnd와 동일
```

### 0.3 정독 범위와 순서

1~530행을 처음부터 끝까지 순서대로 정독 (샘플링 없음). frontmatter → §1 → §2(2.1~2.6) →
§3(3.1~3.4) → §4 → §5. 이후 PDF 8쪽 전부를 이미지로 정독 + pdftotext 대조.
§6~§13은 참조 앵커 존재 확인(grep)만 했다 — 내용 판정은 분할 3 소관.

---

## 1. 결함 (심각도 높은 순)

### [D6·D1] docs/domain-design.md:487-488 — §4 "self arc = 0"이 fixture와 정면 충돌

문제 — 이동표 D·U 행의 규칙 "self arc = 0"이 입력 검증인지(0 아니면 거부) 정규화 강제인지(입력 무시하고 0으로) 불명인데, 실물 자기 arc의 D는 0이 아니라 9999다.
근거 — `[e for e in dm if e["F"]==e["T"]]` → 원본·floor 모두 453건 전부 `D=9999, U=0`. 비자기 arc에도 D=9999가 1건(WIN_2306→WIN_3225, U=991) 있어 9999는 실거리 아닌 sentinel로 보인다. 문서 인용: "| `D` | 거리 (integer meter) | 소수 거부. self arc = 0 |" — 같은 칸의 "소수 거부"가 거부 규칙이라 "self arc = 0"도 검증 규칙으로 읽힌다.
증상 — 검증으로 구현하면 §5 규칙 4(이동표 완전성)에서 453건 위반으로 **fixture가 접수조차 안 된다.** "입력이 원래 0"이라 믿고 그대로 쓰면 자기 arc 9999m가 지표에 흘러들 수 있다.
권고 — 문구 분리: "자기 arc는 입력값과 무관하게 정규화가 D=0·U=0으로 강제한다(실물은 D=9999 sentinel이 온다)" 한 문장으로 확정.

### [D3·D6] docs/domain-design.md:222-223, 256 — `startDepot`: wire 어디에도 없는데 부재 시 규칙이 없다

문제 — §2.4가 `startDepot`을 "공통(대부분 존재)"으로 분류하고 §2.5가 "차량마다 `startDepot` 지정"이라 쓰지만, 실물 wire에 이 필드가 전무하고 부재 시 기본값 규칙이 §1~§5 어디에도 없다.
근거 — fixture 차량 31/31 `startDepot` 없음 (키 합집합 7종). PDF Vehicle 표(7-8쪽)에도 startDepot·endDepot 자체가 없고, Plan 절(3쪽)은 "Each Plan has only one depot policy"다. 문서 자신의 MUST와 충돌: §2.1:143 "규칙으로 정해진 기본값만 채우고, 그 외는 입력 오류", §2.4:229 "몰래 기본값을 채우지 않는다". 전체 문서 grep(`grep -n startDepot`)에도 부재 시 규칙 없음 — 539·549·622행 전부 존재를 전제.
증상 — §2.1 추측 금지를 그대로 지키면 **31/31 차량이 입력 오류 → fixture가 안 돈다.** 관대하게 "유일 depot로 귀속"을 발명하면 §2.1 MUST 위반이고 다중 depot 입력에서 구현마다 갈린다.
권고 — 규칙 추가: "`startDepot` 미지정이면 depot이 하나일 때 그 차고, 여러 개면 INVALID_INPUT" 같은 부재 규칙을 §2.4에 명시.

### [D6·D1] docs/domain-design.md:358-368 — 수치의 JSON 인코딩: 실물 wire는 대부분 문자열인데 문서·규약은 number를 전제

문제 — §3.1 표의 외부(규약) 열("decimal kg"·"integer meter")과 문서가 정본으로 가리키는 PDF의 타입 선언(weight: number double, taskTime: integer int32)대로 strict 바인딩하면 실물이 안 읽힌다 — 실물은 수치 대부분을 JSON 문자열로 보낸다.
근거 — fixture 실측: `"weight": "26.2"`(문자열) 452건, `"qty": "1"`, `"maxWeight": "2100.0"`, `"speed": "45"`, `"duration": "300"`, `"multiRotation": "1"` 전부 문자열. 반면 distanceMatrix `D`·`U`와 `item.taskTime`(452건)은 JSON number — **혼재**다. 문서는 시각 문자열 형식(§2.2:191)은 정하면서 수치 인코딩은 어디에도 안 적었다.
증상 — 타입 그대로 구현하면 **452/452 주문·31/31 차량 파싱 실패 → fixture가 안 돈다.** taskTime만 number라 부분 성공-부분 실패로 원인 찾기가 더 어렵다.
권고 — 문구 추가: §2 입력 계약에 "실물 wire는 수치를 JSON 문자열로 보낸다(number와 혼재). adapter는 양쪽 표기를 받는다"를 명시.

### [D1] docs/domain-design.md:374-376 — '소수 거부'의 판정 기준(문자열 점 vs 비정수 값)이 미정의

문제 — "소수 거리·시간 입력은 거부한다"에서 `"15.0"`(점은 있으나 값은 정수)을 거부하는지가 문장에 없고, 인용 수치 204,756은 '문자열에 점' 기준에서만 재현된다.
근거 — 원본 재계산: D에 점 포함 204,756(문서 수치와 일치 — 이 중 3,558건은 "x.0"꼴 정수값), 값이 비정수인 행은 204,047. 두 기준의 차 709행. 명령: `fixture_check3.py` (hasdot vs float.is_integer 비교).
증상 — adapter·재검증이 기준을 다르게 읽으면 "x.0" 입력의 접수 판정이 구현마다 갈린다. floor fixture는 전부 정수라 지금은 안 드러난다.
권고 — 문구 분리: 판정 기준 한 줄("표기에 소수점이 있으면 값과 무관하게 거부" 등)을 §3.1에 명시하고 204,756이 그 기준의 수치임을 밝힘.

### [D6·D2] docs/domain-design.md:222-223 — "공통(대부분 존재)" 10개 중 4개가 실물 0/31

문제 — §2.4 공통 목록의 `maxStopCnt`·`maxDriveTime`·`maxDriveDist`·`startDepot`은 실물 wire에 한 건도 없어 "대부분 존재"가 실측과 어긋나고, 15줄 아래 유예 표 도입부("키는 7개뿐")와 같은 절 안에서 충돌한다.
근거 — fixture 31/31 차량에 4필드 전부 부재 (per-field presence 스크립트 출력 0/31). PDF에서도 셋은 Optional, startDepot은 미정의.
증상 — 구현자가 wire에 흔히 오는 필드로 오해해 매핑·테스트 데이터 우선순위를 잘못 잡고, 같은 절의 두 문장 중 어느 쪽이 맞는지 되묻게 된다.
권고 — 문구 교정: 공통 목록을 "실물 실존 7필드"와 "규약 정의 optional(실물 미출현)"로 분리.

### [D6·D4] docs/domain-design.md:223 — 문서 `maxDriveDist` vs 규약 PDF 표기 `maxDriveDistc`

문제 — 문서 필드명과 규약 PDF의 속성명 철자가 다른데(끝의 c) 문서가 이를 안 적었다.
근거 — `pdftotext -f 8 -l 8` → 속성명이 "maxDriveDi"+"stc"로 줄바꿈 = `maxDriveDistc`. fixture에는 0/31이라 실물 표기는 미확인.
증상 — 호출 시스템이 PDF 철자로 보내면 adapter가 `maxDriveDist` 키만 읽어 한도를 조용히 놓친다 — optional 부재 = 제약 없음이라 **오류조차 안 난다.**
권고 — 실측 후 수치 교정: 호출 시스템에 실제 키 철자를 확인해 §2.4에 wire 표기(오타 여부 포함)를 기록.

### [D6] docs/domain-design.md:249-262 — 실물 wire 필드 다수의 처분(읽음/무시/거부)이 문서에 없다

문제 — 실물 options 10키 중 3키(`driverRestTimeRatio`·`difficultySortType`·`customerAbbr`), 주문의 `DEPOT`("N"×452)·`district`·`customerId`, top-level `continent`(PDF Mandatory)·`lssId`·`routes`, item의 `prodId`·`weightUnitCd`·`volumeUnitCd`가 문서 전체(989줄)에 0건이고, 미지 필드 일반 정책도 §2에 없다.
근거 — `grep -nE 'driverRestTimeRatio|difficultySortType|customerAbbr|lssId|continent|locTcd|weightUnitCd|volumeUnitCd|prodId|district|DEPOT' docs/domain-design.md` → 해당 필드명 0건 (DEPOT은 `DEPOT_WINDOW`만 검출). `driverRestTimeRatio`는 PDF 샘플(0.15)과 fixture(0.00) 양쪽에 실재하며, 이름상 유효한 답의 집합을 바꾸는 값(휴식 비율)이다.
증상 — 무시하고 구현했는데 비율>0 입력이 오면 Win 엔진과 결과가 갈리고, 반대로 미지 키 거부로 구현하면 fixture가 안 돈다. 구현마다 다르게 갈린다.
권고 — 상위 문서 개정: 세 options 키의 처분(무시 근거 또는 배송정책 편입)과 "미지 wire 필드는 무시한다/거부한다" 일반 정책을 §2.5에 명시.

### [D1] docs/domain-design.md:457-459 — §3.3 `serviceStartTime = max(arrival, openTime)`이 §3.2의 '창 목록' 모델과 부정합

문제 — §3.2:407-408이 "방문 쪽의 시간창은 전부 목록"이라 확정했는데 §3.3 공식은 스칼라 `openTime` 하나를 참조해, 어느 창의 open인지·창 사이 틈에 도착하면 어찌 되는지 이 공식만으로는 알 수 없다.
근거 — 인용: §3.2 "차량의 근무창·차고의 창·방문 쪽의 시간창은 전부 **목록**이다" vs §3.3 "`serviceStartTime  = max(arrival, openTime)`" (§7.1 참조 없음). 오독 시나리오: §3.3만 보고 첫 창의 open으로 구현 → 다일 입력에서 §7.1 절차와 다른 serviceStart.
증상 — 탐색·재검증이 같은 규칙을 독립 구현하는 지점이라 두 코드의 숫자가 갈린다. 1일 fixture(창 1개)에서는 안 드러나 다일 입력에서 처음 터진다.
권고 — 문구 분리: §3.3에 "여기의 openTime은 §7.1이 고른 적용 창의 open" 한 줄 참조 추가.

### [D1] docs/domain-design.md:467, 222 — 차량 쪽 `vehicleFeature = "ALL"`(규약 기본값)의 처리 미정의

문제 — PDF는 차량 `vehicleFeature`의 Default를 "ALL"로 정의하는데, §3.4 식(`vehicle.vehicleFeature ∈ request.vehicleFeatureList OR list == ["ALL"]`)대로면 feature가 "ALL"인 차량은 `["ALL"]`을 명시한 주문 외 **전부와 비호환**이 된다.
근거 — PDF 7쪽 vehicleFeature 행 Default "ALL". fixture는 차량 31대 전부 구체 코드(T1~T5), `["ALL"]` 주문 0건이라 실물에서는 미발화 — 즉 규약 기본값 경로가 한 번도 검증 안 된 상태다.
증상 — vehicleFeature를 생략한(또는 "ALL"로 보낸) 차량이 오류 없이 조용히 유령 차량이 된다.
권고 — 문구 추가: §3.4에 차량 쪽 "ALL" 와일드카드 처리(전 주문 호환) 또는 거부를 명시.

### [D6] docs/domain-design.md:206 — 실물 `itemId`가 452건 전부 빈 문자열인데 문서가 빈 값 허용 여부를 안 정함

문제 — §2.3이 items[]에 `itemId`를 나열하고 PDF는 "Unique Id for item"(Mandatory)이라 하는데, 실물은 452건 전부 `""`다(식별은 `prodId`가 하고 있음 — 452건 전부 비어 있지 않음).
근거 — `Counter(it["itemId"] ...)` → `{"": 452}`, distinct 1. `prodId` nonempty 452/452.
증상 — PDF대로 비어 있지 않음·유일성을 검증하면 **452/452 거부로 fixture가 안 돈다.** 검증 안 하면 itemId 기반 참조가 전부 충돌한다.
권고 — 문구 추가: §2.3에 "itemId는 빈 값 허용(식별자로 쓰지 않는다)" 또는 prodId 대체 규칙을 명시.

### [D1] docs/domain-design.md:185, 256 — "차고 목록 (여러 개 가능)" vs 규약 "Each Plan has only one depot policy"

문제 — canonical의 다중 차고 지원과 wire의 단일 depot 정책이 구분 없이 적혀, 규약으로 지금 여러 depot이 올 수 있다고 읽힌다.
근거 — PDF 3쪽: "Each Plan has only one depot policy. If user want multiple depot in simulation, please contact administrator." fixture depot 배열 길이 1.
증상 — multi-depot wire 테스트 케이스를 만드는 헛수고, startDepot 결함과 결합한 혼선.
권고 — 문구 분리: §2.2 규약 대응 열에 "규약은 단일 depot 정책(다중은 협의)" 주석 한 줄.

### [D6] docs/domain-design.md:262, 495 — `defaultSpeed` vs 실물 키 `Optimizer.DefaultSpeed`

문제 — 문서(§2.5·§4)는 `defaultSpeed`만 언급하는데 실물 options에는 그 키가 없고 `Optimizer.DefaultSpeed`("45")만 있다.
근거 — fixture options 키 목록 실측(0.2절). PDF는 `defaultSpeed`로 정의 — 실물이 접두어 표기로 갈라진 것.
증상 — adapter가 `defaultSpeed`만 읽으면 실물의 45를 놓친다 — §4 최후 기본값이 우연히 같은 45이고 fixture에 U 누락이 0건이라 지금은 무증상, 값이 달라지는 입력에서 U 보정이 조용히 어긋난다.
권고 — 실측 기록: §2.5에 wire 별칭 `Optimizer.DefaultSpeed`를 병기.

### [D6] docs/domain-design.md:358-376 — 단위 코드 필드(`weightUnitCd`·`volumeUnitCd`) 검증 규칙 부재

문제 — §3.1은 kg·CBM 고정을 전제하는데 실물 item에는 단위 코드 필드가 실려 오고, 불일치 시 거부한다는 규칙이 없다.
근거 — fixture 452건 전부 `("KG","CBM")`. PDF: "weight unit code must be KG", "volume unit code must be CBM". 문서 grep 0건.
증상 — 다른 단위 코드가 오면 §3.1 환산이 코드 무시하고 그대로 적용돼 무증상으로 값이 틀어진다 (현 실물은 전부 KG/CBM이라 무해).
권고 — 문구 추가: adapter가 단위 코드를 검증(불일치 = INVALID_INPUT)한다고 §3.1에 한 줄.

---

## 2. 기계 검사 전 건 판정

### 2.1 전칭어 23건

| 행 | 문구 | 판정 |
|---|---|---|
| 38·370 | weight·volume·taskTime **전부** ×qty | 규칙 선언. PDF 문면("calculated by item type not quntity" — 원문 오탈자)과의 상충은 문서가 스스로 기록 ✓ |
| 40 | 설계 규칙 변경은 **없다** | 개정 이력 서술 — 실害 없음 |
| 118 | 현 규약의 주문은 **전부** DELIVERY_ONLY | **검증 ✓** PDF 전문에 pickup 개념 없음, fixture 주문 전부 단면(수하 쪽만) |
| 123 | 연산은 없다 / 140·356 유일 | 설계 선언 — 판정 대상 아님 |
| 237-239 | 실물 wire에 **없다**·키 **7개뿐** | **검증 ✓** 키 합집합 정확히 7종, 각 31/31 |
| 245 | 검사 대상 자체가 **없다** | **검증 ✓** item 키 9종에 치수 없음 |
| 246 | wire에 없다 (차량별 trips) | **검증 ✓** 0/31 |
| 247 | wire에 없고 소비처가 **없다** | **검증 ✓** 0/31 + `grep vhclOwnTyp` 본문 소비처 0건 |
| 266·272·320·338 | 전부/여지가 없다/지킬 수 없다 | 설계 논증 — 판정 대상 아님 |
| 399 | 근무창에는 이 문장이 **없다** | **검증 ✓** pdftotext — 근무 시간 서술은 "All vehicles can move between workStartTime and workEndTime."뿐. "wailt until next opening time"(오탈자 원문 그대로)은 주문·차고 창 4곳에만 |
| 407-408 | 시간창은 전부 **목록** | 규칙 — 단 §3.3 공식과 부정합 (결함 F8) |
| 425·431·464·472·482 | 규칙·논리 설명 | 431의 산술("1초 틈") 검산 ✓ (86399+1≥86400 병합), 472 검증 ✓ |

### 2.2 상태어 21건

8~40행(frontmatter revisions) 15건은 개정 이력 — 각 이력이 가리키는 본문(§2.5 판정 반전, §3.2 신설, §2.6 경계 포함, 유예 표 등)이 실제로 그 상태로 존재함을 본문 정독으로 확인 ✓. 본문 6건(145, 245, 261, 269, 292-296, 340, 440, 473, 519행)은 전부 "확정" 표기이고 내용이 확정 상태와 일치 — 미결이 확정처럼, 확정이 미결처럼 남은 곳 **0건**. 특히 261행(depot.taskTime)은 frontmatter 37행이 말한 대로 종전 "보류" 문구가 확정 문구로 대체돼 있음 ✓.

### 2.3 참조 82건

- **외부 문서**: `Stage Extra E1`(58행 앵커)·`E2`(95행) 실재 ✓ · `Master §6`(master-design.md:103, 차량별 multiRotation 문단 109행) ✓ · `Plan §2.1 D4`(implementation-plan.md:496) ✓ · architecture-design.md·PDF·win_poc_case.json 실재 ✓
- **문서 내 §참조** (본문 발신 전부): §2.1·§2.1.1·§2.3·§2.4·§2.5·§2.5.1·§2.6·§3.2·§3.3·§3.4·§4·§5·§6·§6.3·§7.1·§7.3·§8·§8.4·§9·§10·§11.2·§12·§13 — 앵커 grep으로 전부 실재 확인 ✓ (frontmatter 발신 §6.2·§7.2.1·§7.5·§8.1~§8.3·§10.2·§11.1 포함)
- **"위 표"(304행)** → §2.5 표 trips 행 실재 ✓ · **"아래 주석"(257·258행)** → 264행 이하 주석 실재 ✓ · **"이미 정의하며"(118행)** → §1.3 PICKUP_DELIVERY 정의 실재 ✓
- 끊어진 참조 **0건**

### 2.4 검증돼 결함이 아닌 fixture 주장 (근거 기록)

| 문서 주장 | 실측 |
|---|---|
| §2.6 Win 참조 해 정차 28회 = 한도 28 경계 | ✓ alns_result.csv 452배정·31대, 최대 28회 — V027·V030 두 대가 정확히 28 |
| §2.5 PDF 4쪽 인용 3종 (`-1`/`0`/`1>=` 열거, `multirotation 2` 그림, "greater than -1") | ✓ 전부 원문과 일치. 그림 `depot(start) → 1st → 2nd → depot(2nd visit) → 3rd` 확인 |
| §2.3 reqDate 기본값 = planEnd (규약 default와 동일) | ✓ PDF Default "Plan End Date Time" · fixture 452건 전부 planEnd와 동일 값 |
| §3.2 시각 3종 전부 날짜 없는 partial-time | ✓ PDF·fixture 모두 HH:mm:ss, 날짜는 dateRange·reqDate뿐 |
| §3.1 원본 미접수·floor가 실행 fixture | ✓ floor는 D·U 전부 JSON 정수, 원본은 소수 다수 (기준 문제는 결함 F4) |
| §3.2 규칙 3 (close==open 입력 오류) | fixture 위반 0건 — 이 규칙으로 fixture가 깨지지 않음 |
| doc-audit §0의 주문 452·차량 31·이동표 205,209 | ✓ 205,209 = 453² (452 주문 + depot 1) |

---

## 3. 심각도 요약

| 심각도 | 건수 | 대표 |
|---|---|---|
| 치명 (구현이 틀림) | 3 | §4 "self arc = 0" vs 실물 453건 D=9999 |
| 중 (재작업 유발) | 7 | 소수 판정 기준 미정의 (204,756은 lexical 기준에서만 재현) |
| 경 (읽기 불편) | 3 | `defaultSpeed` vs 실물 키 `Optimizer.DefaultSpeed` |
