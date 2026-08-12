# 분할 5 감사 보고 — stage-00 · stage-01 · stage-extra

감사일 2026-08-11\~12 · 초점: fixture 대조(D6) · 2026-08-11 개정분

## 0. 전제 보고 (샘플링 아님의 증거)

### 실제 줄 수 (`wc -l`)

```
 820 docs/implementation/stage-00-cleanup-and-skeleton.md
 573 docs/implementation/stage-01-canonical-input-normalization.md
 168 docs/implementation/stage-extra-deferred-features.md
1561 total
```

### 기계 검사 5종 — 명령과 건수

| 검사 | 명령 | 건수 |
|---|---|---|
| a. 링크 | `grep -noE '\[[^]]*\]\(([^)]+)\)' <파일>` + 대상 `ls` 실재 확인 | 35건 (13+17+5). 본문 링크 전부 실재. stage-00:675\~698의 7건은 §6 README **코드펜스 내부 콘텐츠**(루트 기준 경로)라 문서 링크 아님 — 정상 |
| b. 전칭어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|유일\|항상\|절대\|언제나'` | 63건 (11+44+8) — 전건 판정, 위반은 아래 #2·#4·#7 |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토'` | 33건 (8+21+4) — 전건 판정, 위반은 아래 #6 |
| d. 참조 | `grep -cE '§[0-9]\|Stage [0-9]\|위 표\|아래 주석\|이미 [^ ]*는\|같은 방식'` | 326건 (83+208+35). 타 문서 앵커는 표적 grep으로 대조: Stage 3 §4.4(차고 NodeId 색인 제외, :551·:569 ✓) · Stage 5 §4 `deliveryPolicy`(:257\~275 ✓) · Stage 4 `AlnsConfig`(:15·:34 ✓) · Stage 8 §6 W 표(✓) · Stage 2 §9 Q3(:333 ✓) · Plan §1 말미 등재표(:460 ✓) · Plan §2.1 D1(✓) · Plan 편입 문장(:55 ✓) · Plan/Architecture/Domain의 §들(Master §6 :112, Domain §2.1.1 :145·:166, §2.4 유예 표 :237, §2.5.1 :326, §3.2 예2 :432, §12, Architecture §2.1·§2.3·§4·§7 전부 ✓). 위반은 아래 #5·#8 |
| e. fixture | 아래 Python 재계산 (전 항목) | 문서 수치 25개 항목 재확인 — 위반은 아래 #3 (규약↔fixture 어휘 혼동) |

### fixture 재계산 결과 (검사 e — `python3` + `json`/`decimal`)

`data/win_poc_case_floor.json`: 차량 31 · 키 합집합 정확히 7종(`vehicleId·vehicleFeature·maxWeight·maxVolume·workStartTime·workEndTime·speed`) ✓ · 전 차량 `00:00:00~23:30:00`·speed `"45"` ✓ · maxVolume에 `"5.95"` 포함 ✓ · 차고 1개 `WIN_0` `00:00:00~23:59:59` ✓ · dateRange `2023-09-13 00:00:00~2023-09-14 00:00:00` ✓ · 주문 452·item 452(주문당 1) ✓ · weight 소수 436·volume 소수 449 ✓ · `26.2`·`0.21` 실재 ✓ · 창 분포 `05:45~10:30` 282 / `05:45~13:30` 169 / `05:45~17:30` 1 ✓ · reqDate 전건 `2023-09-14 00:00:00`(=planEnd) ✓ · customerId 전건 `WINCOMMERCE`·plan `shprId "S3853"` ✓ · options `trips "oneway"`·`multiRotation "1"`·`Optimizer.VehicleMaxStopCount "28"` ✓ · 이동표 205,209행(×2 = 410,418 ≈ "41만 번") ✓ · 행 키 `C/T/D/U/F` ✓ · floor판 D·U 소수 0건 ✓ · **weight·volume 둘 다 정수인 주문 0건**("한 건도 통과하지 못한다" 실증: weight 정수 16·volume 정수 3·교집합 0) ✓.
`data/win_poc_case.json`: `D "310708.03"` 실재 ✓ · `U "17265.5"` 실재 ✓ (D 소수 201,198 · U 소수 183,715).
`data/ro_input_json_spec.pdf`(pypdf 추출): `"greater than -1"` 실재 ✓ · `"Solver will create a plan in date range"` 실재 ✓ · trips 기본 `"oneway"`·multiRotation 기본 `0` ✓ · **차량 표 필드 = vehicleId·vehicleFeature·workStartTime·workEndTime·maxWeight·maxVolume·speed·maxStopCnt·maxDriveDist·maxDriveTime(10종) + 예시 JSON의 `driverSkill`** (→ #3) · order 표에 capability 요구 필드 없음 (→ #4) · `vhclOwnTyp`·차량별 `trips`·치수 필드는 규약 표에도 없음 ✓.
stage-00 V4 대조군: `mvn -o dependency:list -pl solver-core | grep -cE '^\[INFO\]\s+\S+:\S+:\S+:'` → **15** (문서 :768의 "구현 시점 15" ✓).

### 정독 범위

세 파일 전부 1행부터 끝행까지 (820·573·168행). 순서: stage-extra → stage-01 → stage-00.
상위 문서(Master·Domain·Plan·Architecture)와 타 Stage 문서는 규칙 대조에 필요한 부분만 표적 grep.

---

## 1. 결함 (심각도 높은 순)

### [D1] docs/implementation/stage-01-canonical-input-normalization.md:289-290 (동일 문구 :34 · stage-extra:76-79)
문제 — "그 값은 복귀 선적 시간이고 … **1바퀴에는 복귀가 없어** 발생하지 않는다"(stage-extra는 "**차고 복귀 자체가 없으므로**")는 거짓 전제다. 1바퀴에도 `roundtrip`이면 복귀는 **있다** — 없는 것은 복귀 '후 다음 바퀴 선적'이다.
근거 — 같은 파일 :209 `Depot.windows` 주석 "출발·**복귀** 순간에 적용 (Domain §7.1)" · :391\~394/E19 "부재 + roundtrip → startDepot"(= 복귀 존재). Domain도 §2.5 :260 "출발·복귀 두 순간에 적용" 바로 다음 행 :261에 "복귀 자체가 없으므로"가 인접해 있고 §7.1 :663·§13 :989는 복귀 검사를 MUST로 강제한다 — 반대 방향 규칙이 한 칸 옆에 있는 전형적 D1이고, 문구의 발원지는 Domain §2.5다.
증상 — Stage 3·5 구현자가 "현재 범위엔 복귀가 없다"로 읽고 roundtrip 입력의 복귀 순간 `DEPOT_WINDOW` 검사를 생략할 수 있다. fixture는 oneway라 테스트로 안 잡히고, roundtrip 입력에서만 조용히 뚫린다.
권고 — 상위 문서 개정(Domain §2.5 :261) 후 세 곳 문구 통일: "복귀가 없다" → "복귀 후 **다음 바퀴 선적**이 없다".

### [D2] docs/implementation/stage-extra-deferred-features.md:140-141
문제 — "Stage 2·3·5 문서 전체에 이 개념(차량 소유 구분)이 **한 번도 등장하지 않는다**"는 전칭 주장이 거짓이다.
근거 — `grep -n "소유\|LEASE\|DIRECT" docs/implementation/stage-03-*.md` → :28 revision "§4.3 **소유 비용축**" · :522 "**소유(LEASE/DIRECT) 축**은 지금 없다 — … Stage Extra E3" · :697 "LEASE/DIRECT 비용 축·cost metric … 필요 시 profile 추가". 개념이 최소 3곳 등장한다 (부정 언급이지만 '등장'이다). doc-audit.md §1 D2가 예시로 든 바로 그 문장이 문구만 바뀌어 잔존한 형태다.
증상 — 독자가 grep 한 번으로 반증을 찾는 순간 이 절의 "지운 근거" 전체의 신뢰가 무너지고, E3 유예 판단을 처음부터 재검토하게 된다.
권고 — 문구 교정: "등장하지 않는다" → "소비처가 없다(등장하는 곳은 유예 사실의 기록뿐 — Stage 3 §4.3·§8)".

### [D6] docs/implementation/stage-01-canonical-input-normalization.md:285-287
문제 — "**현행 규약 차량 키는** `vehicleId`\~`speed` **7개뿐**이라(fixture 실측) 읽을 값 자체가 없다" — 7개는 **fixture**의 키이지 규약의 키가 아니다. 규약과 fixture를 한 단어로 뭉쳤다.
근거 — `data/ro_input_json_spec.pdf` 차량 표(pypdf 추출)는 `maxStopCnt`·`maxDriveDist`·`maxDriveTime`을 포함한 10종을 정의하고 예시 JSON에 `driverSkill`(= Domain §2.4 capabilities 대응)이 있다. 같은 절의 `VehicleInput` 시그니처(:284)가 바로 그 규약 필드들(`maxStopCnt·maxDriveTimeSec·maxDriveDistMeter·capabilities` 등 14개)을 유지하고 있어 자기모순이다. Domain §2.4 :237도 "규약 문서가 정의하더라도"라며 둘을 구분해 쓴다 (실물 wire = fixture).
증상 — ① Stage 6 adapter 구현자가 "규약에 그 키가 없다"고 믿고 `maxStopCnt`·`maxDriveTime` 등의 wire 매핑을 빼먹으면 그 필드를 실은 입력의 한도 제약이 조용히 소실된다. ② 반대로 "7개뿐인데 왜 VehicleInput은 14필드인가"를 되묻게 만든다 — trips는 'wire에 없어서' 지웠는데 startDepot·endDepot·zoneIds는 남긴 기준도 이 문장으로는 설명되지 않는다.
권고 — 문구 분리: "규약 차량 키는 7개뿐" → "**floor fixture의** 차량 키는 7개뿐(실측)이고, 유예한 세 필드는 **규약 표에도 없다**" + 유지한 optional 필드의 근거(규약 정의 존재)를 한 줄 명시.

### [D1] docs/implementation/stage-01-canonical-input-normalization.md:461-463 (T12 :530 동일)
문제 — "capability·zone 두 축은 현행 wire에서 항상 참이다 — **차량에 `capabilities`·`zoneIds`가 없기 때문이다**" — 근거가 zone 축에만 성립한다. capability 축이 참인 이유는 차량이 아니라 **주문 쪽**(요구 필드가 wire에 없음)이다.
근거 — 같은 파일 :451 식 `capability = 미요구 OR 요구 ⊆ v.capabilities`: 차량 capabilities 부재(빈 집합 = "능력 없음", :202)는 요구가 있으면 축을 **거짓**으로 만드는 조건이다. 참을 보장하는 것은 fixture 주문 키 14종(실측)과 규약 order 표 어디에도 요구 필드가 없다는 사실이다. 게다가 규약 차량 예시에는 `driverSkill: "INSTALL"`이 실재해 "차량에 없다"는 wire 수준에서도 부정확하다. Domain §2.4의 두 부재 의미가 정반대(capabilities 부재 = 능력 없음 / zoneIds 부재 = 전 구역)인데 한 문장으로 뭉쳐 그 비대칭을 지웠다 — doc-audit §5-3이 지목한 바로 그 유형.
증상 — "차량 필드 부재 → 축 통과"로 일반화해 외우면, driverSkill을 실은 입력이 들어오는 순간 T12가 "도달 불가능"으로 버린 조합(요구 있음 × 능력 없음 → 거짓)이 실제로 도달하고 테스트 공백이 된다.
권고 — 문구 분리: "zone 축은 **차량** `zoneIds` 부재로, capability 축은 **주문** 요구 필드 부재로 각각 항상 참" + 차량 driverSkill이 규약 예시에 존재함을 병기.

### [D2] docs/implementation/stage-01-canonical-input-normalization.md:127-129 (:33-35 동일)
문제 — `Depot.nodeId` 삭제에 대해 "근거·트리거·되살릴 지점은 Stage Extra에 **등재돼 있다**"고 썼으나 등재돼 있지 않다.
근거 — `grep -n "nodeId" docs/implementation/stage-extra-deferred-features.md` → 0건 (E1 표는 `Depot.taskTimeSec`만 수록). Plan §1 말미 등재표(:460\~466)의 E1 행에도 없음. 함께 나열된 나머지 4항목(trips·치수·ownership·taskTimeSec)은 전부 등재돼 있어 이 항목만 누락이다.
증상 — 유예 항목 관리 방침("미룬 것은 계획 문서에 등재" — Plan §1)이 첫 적용에서 구멍난다. `Depot.nodeId`를 되살릴 조건·지점을 찾는 독자가 빈손이 되고, 애초에 이것이 '유예'(조건부 복원)인지 '설계 확정 삭제'(Stage 3 §4.4가 차고 NodeId를 색인에서 구조적으로 제외)인지도 판정할 수 없다.
권고 — 둘 중 하나로 교정: Stage Extra에 등재하거나, 유예 목록에서 빼고 "설계상 불필요(Stage 3 §4.4) — 유예 아님"으로 분리.

### [D5] docs/implementation/stage-00-cleanup-and-skeleton.md:812-820
문제 — §11 "미해결 질문" 표의 세 행 전부(Q1 "해소" · Q2 "해소" · Q3 "구현하지 않음")가 종결된 사안인데 미해결 표에 잔존한다.
근거 — :816 "**해소.** Plan Stage 0 코드 정리에 반영" · :817 "**해소 (2026-08-10, Plan §2.1 D1 확정)**" · :818 "**구현하지 않음.**" — 미해결 행 0개. 같은 사안을 stage-01은 2026-08-11에 정리했다(:26-27 "§8의 해소된 multiRotation 행 삭제 — **종결된 사안의 잔재**" · :566 "닫힌 질문은 여기 남기지 않는다"). 같은 날 개정 물결이 stage-00 §11만 건너뛰었다.
증상 — doc-audit §1 D5의 실사례(Stage 1 §9)와 동일 패턴 — 닫힌 논의를 한 문단씩 다시 읽고 고민하게 만든다.
권고 — 삭제: stage-01 :566 방식대로 포인터(Plan D1·Plan Stage 0)만 남기고 세 행 제거.

### [D2] docs/implementation/stage-extra-deferred-features.md:51 (:148-149 연쇄)
문제 — "전부 **순수 add-only**다 — 지금 넣든 나중에 넣든 비용이 같아서 미뤘다"가 같은 문서의 자기 서술과 충돌하고, :149 "'필드 하나'로 끝나지 않을 수 있는 **유일한** 항목이다"(E3)도 거짓이다.
근거 — §1 표 E1 행 "multi-trip 본체는 **대(구조 변경)**" · :91 "전파·연산자·재검증·결과 JSON이 전부 바뀐다. 필드 추가와 같은 급으로 다루지 않는다" · E3 :148 "점수 축이 되면 … 재검증의 점수 재계산도 같이 바뀐다". add-only가 아닌 것이 문서 안에 두 개(E1 본체·E3 점수 축) 있으므로 "전부"도 "유일"도 성립하지 않는다.
증상 — E1을 통째로 "되살리면 그만인 싼 것"으로 읽고 multi-trip 트리거 발동 시 비용 산정을 틀린다.
권고 — 문구 분리: "전부"의 주어를 "**지운 필드들은**"으로 한정하고, :149의 "유일한"을 삭제(E1 본체 병기).

### [D4] docs/implementation/stage-extra-deferred-features.md:78-79
문제 — 인용 『trip이 1개뿐이라 상차 시간 개념 보류』가 현행 Domain §2.5에 존재하지 않는다 — 2026-08-11 개정으로 대체된 옛 판의 문구다.
근거 — `grep -n "상차" docs/domain-design.md` → 0건. 현행 §2.5 :261은 확정 문구("차고로 복귀해 다시 선적하는 데 걸리는 시간이다")로 교체돼 있다.
증상 — "괄호로 추측해 둔 것"을 현행 Domain에서 찾으려는 독자가 실패한다. 과거형 서술이라 오독까지는 아니나 검증 불가능한 인용이다.
권고 — 문구 교정: 과거 판 인용임을 명시("개정 전 Domain §2.5가 …라고 추측해 뒀고")하거나 인용 자체를 삭제.

---

## 2. 판정만 하고 결함으로 올리지 않은 것 (근거 기록)

- stage-00:675\~698의 `docs/...` 링크 7건 — §6 README **전문 코드펜스 내부**(루트 기준 경로) — 링크 검사 대상 아님.
- stage-00:768 "대조군 15" — `mvn -o` 재실행으로 **15** 재확인 (위 검사 e).
- stage-01:58-60 "무게·부피까지 거부하면 fixture가 한 건도 통과하지 못한다" — 재계산으로 참 확인 (weight 정수 16 ∩ volume 정수 3 = **0건**).
- stage-01:419-423·E5·E6·§4 실측 표·T16 수치·E11·E15·E16·E20·Q4·Q6 — 전부 fixture 실측과 일치 (위 검사 e).
- stage-01 E6c·E31의 규약 인용("greater than -1"·"Solver will create a plan in date range") — PDF 추출 텍스트에 실재.
- stage-01 §6 번호 결번(E13, E19b\~e) — 삭제 항목의 결번 유지는 참조 보존 목적으로 정상.
- stage-01:31 "하나도 건드리지 않았다" — 주어가 "나중에 못 넣는 것(목록 명시)"으로 한정돼 있고 `Units` 시그니처 변경은 같은 revision이 별도 고지(:36) — 모순 없음.
- stage-00 Boot 4.1.0/JUnit 6.0.3/ArchUnit 1.5.0 버전·지원기간 표 — 외부 사실이라 문서만으로 미확인. 단 Stage 0 완료(`mvn verify` green) 상태가 좌표 유효성을 간접 입증.
- 검사 e 중 발견한 **Domain**의 수치 "204,756건이 소수"(domain-design.md:375) — 내 실측은 D 소수 201,198·U 소수 183,715 (합집합 미계산). 분할 2·3 담당 확인 사항으로 이관.

---

## 3. 요약

| 심각도 | 건수 | 대표 |
|---|---|---|
| 치명 (구현이 틀림) | 0 | — |
| 중 (재작업 유발) | 5 | "1바퀴에는 복귀가 없다" (stage-01:289 · stage-extra:77 · 발원지 Domain §2.5) |
| 경 (읽기 불편) | 3 | stage-00 §11 종결 질문 잔존 |
