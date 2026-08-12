# 설계 문서 감사 — 분할 6 (stage-02 · stage-03, 초점: 전파 규칙·이동표 보정)

감사일 2026-08-11 · 감사자: doc-audit 분할 6

## 0. 전제 보고 (doc-audit.md 필수 3항)

### 0.1 대상 파일 실제 줄 수 (`wc -l`)

```
     333 /Users/brown/workspace/ro-next/docs/implementation/stage-02-travel-and-problem-freeze.md
     712 /Users/brown/workspace/ro-next/docs/implementation/stage-03-solution-propagation-evaluation.md
    1045 total
```

### 0.2 기계 검사 5종 — 명령과 건수

| 검사 | 명령 | 건수 | 판정 결과 |
|---|---|---|---|
| a. 링크 | `grep -nE '\]\(([^)#]+)' <두 파일> \| grep -v 'https://'` | stage-02 **2건** · stage-03 **4건** | 대상은 `../domain-design.md`·`../implementation-plan.md` 둘뿐 — 둘 다 실재 (`ls` 확인). 끊어진 링크 0 |
| b. 전칭어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|유일\|항상\|절대\|언제나'` | stage-02 **22줄** · stage-03 **35줄** | 전 건 판정. fixture 관련 전칭(§0.3의 e)은 스크립트 실측으로 전부 참 확인. 나머지는 계약 선언(예: "mutator는 존재하지 않는다")으로 실측 대상 아님. stage-03:704 "Q1·Q2·Q3는 전부 해소됐다" — §9 표와 대조해 참 |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토'` | stage-02 **22줄** · stage-03 **17줄** | 전 건 판정. 해소 표기(D1·D4·Q3 등)는 Plan §2.1 D1/D4 행·Domain §2.5/§2.5.1/§3.2/§7.1로 실재 확인. 잠정 표기(stage-02 Q1·Q2)는 실제 미결로 정합. 잔존 문제 1건 → 결함 #7 |
| d. 참조 | `grep -cE '§[0-9]\|Stage [0-9]\|위 표\|아래 주석\|이미 .*는\|같은 방식'` | stage-02 **127줄** · stage-03 **248줄** | Domain §1.3·§1.4·§2.3·§2.4·§2.5·§2.5.1·§2.6·§3.1–§3.4·§4·§5·§6.2–§6.5·§7.1–§7.5·§8.1–§8.4·§9.1·§9.2·§10.1·§10.2·§11.1·§12 전부 본문 대조(아래 0.4). Plan §0(L59)·§1 편입 문장(L56, 2026-08-11)·§2.1 D1(L493)·D2(L494)·D4(L496) 실재. 인접 Stage anchor는 grep으로 실재만 확인: Stage 1 E30(L506)·§2.2 `NodeId`/`Depot`(L210)·Stage 5 N1 공유 허용 목록(L374)·N2(L375)·절차 5-a(L21). **미확인 2건**: stage-02 N4의 "Stage 8 W16", stage-03 §4.4의 "Stage 6 §조립" — 타 분할 파일이라 내용 대조 안 함 (분할 8·통합 몫). 결함 #4·#5·#9는 이 검사에서 나옴 |
| e. fixture | Python으로 `data/win_poc_case_floor.json` 전수 (아래 실측값) | 문서 주장 **17건** 대조 | 16건 참 · **1건 거짓 → 결함 #3** |

fixture 실측 (스크립트 출력):

```
matrix rows: 205209 (= 453²) · locations: 453 · dup pairs: 0
self arcs: 453, 전건 D=9999·U=0
비대각 D=9999: 정확히 1건 — WIN_2306→WIN_3225, U=991     ← stage-02 N4 주장과 일치
vehicles: 31, 키 7종(vehicleId·vehicleFeature·maxWeight·maxVolume·workStartTime·workEndTime·speed)
speed: 전 31대 "45" (부재 0대)                              ← stage-02 E8 "(fixture 상황)"과 모순
Optimizer.DefaultSpeed: "45" (options의 리터럴 키) · distanceTimeCalculate: "GreatCircle"
multiRotation: "1" · waitInDepot: "N" · C 열 distinct: {G, O}
orders: 452 (고유 locId 452) · depot: 1개, openTime 00:00:00 · closeTime 23:59:59 (전일창)
근무: 전 차량 00:00:00~23:30:00 · dateRange: 1일 (2023-09-13 ~ 09-14)
재계산: 453² = 205,209 ✓ · 452×31 = 14,012 ✓ · dense int[]×2 = 1,641,672 B ≈ 1.6 MB ✓
```

수치 재검산(문서 표 값): stage-02 E12(1000/45→80초, 1001/45→81초 ✓) · T1(R=6,371,000·위도 1° → 111,194.93 → HALF_UP 111,195 ✓) · stage-03 §3.4 표 전 셀(31200/32400/32700/35700/46800/47640, 집계 5400/12300/1140/2, 항등식 18840 = 47640−28800 ✓) · §3.5 표 전 셀(145800+5400=151200>147600, 201600, 207000, 1800+54000=55800, 3600+54000 ✓) — 전부 Domain §7.2·§7.2.1 원문 수치와 일치.

### 0.3 정독 범위

- `stage-02-travel-and-problem-freeze.md` 1~333줄 전체 (Read, 샘플링 없음)
- `stage-03-solution-propagation-evaluation.md` 1~712줄 전체 (Read 2회 분할, 샘플링 없음)
- 대조용 상위 문서: `domain-design.md` §1.3·§2.3~§2.6·§3·§4·§5·§6·§7 전체·§8 전체·§9~§12 해당부를 sed/grep으로 발췌 정독. `implementation-plan.md`는 Stage 2·3 절(L169–266)·§2.1 결정 표·§1 편입 문장만 발췌 (전문 정독은 분할 4 소관)

---

## 1. 결함

### [D1] docs/implementation/stage-03-solution-propagation-evaluation.md:634 (E35, T14 연동)
문제 — E35가 "근무창 ∩ 차고 창 = ∅"이면 무조건 `DEPOT_WINDOW`라고 단언하지만, 같은 문서 §3.3 절차 1의 규칙("근무창이 먼저 소진되면 WORK_WINDOW, Ds가 먼저 소진되면 DEPOT_WINDOW")대로면 결과는 **어느 목록이 먼저 소진되느냐(데이터)에 따라 갈린다**.
근거 — 절차 1(L307) 인용: "근무창이 먼저 소진되면 WORK_WINDOW, Ds가 먼저 소진되면 DEPOT_WINDOW (at 부재)". 반례: 근무 06:00~07:00·차고 08:00~09:00(둘 다 매일 반복) → 교집합 ∅이지만 포인터를 밀면 **근무창 목록이 먼저 소진**되어 절차 1대로는 `WORK_WINDOW`다. E35는 창 구성 조건 없이 "∅ → DEPOT_WINDOW"로 적었고 T14(L675)가 이 기대값을 그대로 인용한다.
증상 — T14 작성자가 임의의 ∅ 케이스(예: 위 반례)를 조립하면 구현은 `WORK_WINDOW`를 내고 테스트는 `DEPOT_WINDOW`를 기대해 실패 — 구현 버그가 아닌데 버그로 오인. Stage 5가 E35 문구를 따라 재검증을 짜면 탐색↔재검증의 FAILED 원인 종류가 갈린다 (가능/불가 판정은 동일).
권고 — 문구 분리: E35 상황 열에 "차고 창 목록이 먼저 소진되는 경우"라는 데이터 조건을 명시하거나, 구체 창 값(예: 차고 창이 근무창보다 먼저 끝나는 구성)을 박아 기대값을 결정적으로 만든다.

### [D3] docs/implementation/stage-03-solution-propagation-evaluation.md:307,321 (§3.3 절차 1·2)
문제 — Domain §7.1이 2026-08-11에 정본화한 **"위반 귀속" 동시-소진 tie-break**(두 창 축이 같은 시각에 함께 소진되면 절차 문장에 먼저 적힌 쪽 — 절차 0은 `WORK_WINDOW`, 절차 2는 `TIME_WINDOW`)가 Stage 3 전파 절차에 반영되지 않았다.
근거 — Domain §7.1 마지막 불릿(정독 확인): "**위반 귀속 (2026-08-11)**: 한 지점에서 두 창 축이 **같은 시각에 함께 소진**되면 절차 문장에 먼저 적힌 쪽을 기록한다 … 탐색과 재검증이 FAILED 원인 종류까지 같게 내기 위한 규약이다." Stage 3 §3.3은 "먼저 소진되면"만 적고 동시 소진의 정의·처리가 없다. Stage 3 revisions에 2026-08-11 항목이 둘 있으나(L23~28) 위반 귀속 반영은 없다.
증상 — Stage 문서는 구현 계약("표·코드 블록에 없는 선택은 하지 않는다")이므로 Stage 3 구현자는 동시 소진을 임의로 처리한다. Stage 5가 Domain을 따라 tie-break를 넣으면 같은 해에 대해 탐색과 재검증의 위반 종류가 어긋난다 — Domain이 이 규약을 만든 이유 그 자체가 재발한다.
권고 — 상위 문서 개정 아님(Domain이 이미 정본) — Stage 3 §3.3 절차 1·2에 동시 소진 한 줄씩 추가(Domain §7.1 위반 귀속 인용)하고 revisions에 기록.

### [D6] docs/implementation/stage-02-travel-and-problem-freeze.md:260 (E8)
문제 — E8이 "차량 speed 부재 + defaultSpeed=45 **(fixture 상황)**"이라고 적었지만 fixture 차량 31대는 **전부 `speed:"45"`를 갖고 있어**(부재 0대) "차량 speed 부재"는 fixture 상황이 아니다. 같은 문서 N4(L242)의 "전 차량 speed \"45\""와도 내부 모순이다.
근거 — 실측: `python3` 스캔 결과 `vehicles without speed: 0`, `speeds: {'45'}` (§0.2 e). fixture 상황은 "차량 speed 45 + defaultSpeed 45"이며 체인 1단에서 이미 45가 확정된다.
증상 — resolved 값이 어느 쪽이든 45라 동작·테스트 결과는 같다. 다만 E8을 fixture 재현으로 읽은 사람이 "fixture는 차량 speed를 안 준다"고 오인해 Stage 6 adapter·합성 입력을 잘못 조립하거나, N4와의 모순을 발견하고 다시 물어보게 된다.
권고 — 문구 분리: "(fixture 상황)"을 삭제하거나 "(defaultSpeed=45는 fixture 값. 차량 speed 부재는 합성 케이스 — fixture는 전 차량 speed 45, N4)"로 교정.

### [D4] docs/implementation/stage-03-solution-propagation-evaluation.md:569-570 (§4.4)
문제 — "`nodeRef` 색인은 freeze가 만든다 (**차고 NodeId는 색인에 넣지 않는다** — 경로 visits에 올 수 없는 값이므로 조회 실패가 곧 구조 신호다)" — 2026-08-11 Stage 1 유예로 **차고는 NodeId 자체를 갖지 않게 됐는데**, 이 문구는 여전히 차고 NodeId가 존재하되 색인에서 빼는 것처럼 읽힌다.
근거 — stage-02 revisions(L20-21): "차고 NodeId 중복 검사 제거(**차고는 NodeId를 갖지 않는다**)". stage-01 anchor grep: L210 "차고는 Route.visits에 오지 않아 NodeId가 없다 (Stage 3 §4.4)", L33 "`Depot.nodeId`(소비처 0 — **Stage 3 §4.4가 차고 NodeId를 색인에서 제외**)". 즉 Stage 1은 삭제 근거로 Stage 3 §4.4를 들고, Stage 3 §4.4는 삭제 전 어휘로 남아 **서로가 서로의 옛 상태를 가리킨다**. stage-03의 2026-08-11 revisions(L27-28)에 §4.4의 이 문구 정정은 없다.
증상 — 구현자가 "차고 NodeId"가 어딘가에서 발급되는 줄 알고 Stage 1을 뒤지거나, freeze에서 차고용 NodeId를 만들어 색인에서 빼는 불필요한 코드를 짠다.
권고 — 문구 교정: "(차고는 NodeId를 갖지 않으므로(Stage 1 §2.2) 경로 visits의 미등록 값 조회 실패가 곧 구조 신호다)"로 교체.

### [D1] docs/implementation/stage-02-travel-and-problem-freeze.md:271 (E19)
문제 — "vehicles 또는 requests가 빈 목록 → 통과 — 전부 bank(**또는 빈 경로**)인 해로 풀이 진행"의 "빈 경로"가 Stage 3의 구조 규칙과 충돌한다 — Stage 3은 빈 visits의 `Route` 생성을 금지한다(미사용 차량 = Route 부재).
근거 — stage-03 L133-135: "visits: … 비어 있으면 IllegalArgumentException — 미사용 차량은 Route가 없는 것이다", E30(L646) 동일. 오독 시나리오: Stage 4 초기해 작성자가 stage-02 E19를 보고 "requests 빈 입력 → 차량마다 빈 경로를 깐 해"를 만들면 `Route` 생성자에서 예외.
증상 — 생성자 예외로 즉시 드러나므로 조용한 오답은 아니나, "문서 간 어느 쪽이 맞나"를 다시 묻게 만든다.
권고 — 문구 분리: "(또는 빈 경로)"를 "(또는 경로 0개)"로 교정 — 빈 Route가 아니라 Route가 없는 해다.

### [D1] docs/implementation/stage-03-solution-propagation-evaluation.md:127-135 (§2.1)
문제 — "compact constructor에서 방어 복사(...)**만** 한다"고 선언한 직후, `Route`의 주석·E30·T는 생성자가 빈 visits에 `IllegalArgumentException`을 던진다고 정한다 — "복사만"이 거짓이라 검사 위치(생성자 vs `StructureCheck`)가 문장마다 다르게 읽힌다.
근거 — L127 "방어 복사(`List.copyOf`/`Set.copyOf`)만 한다" vs L133-134 "비어 있으면 IllegalArgumentException" vs L129 "pair·XOR 규칙 검사는 생성자가 아니라 StructureCheck 한 곳이 소유한다". 오독 시나리오: "만 한다"를 계약으로 읽은 구현자가 빈 visits 검사를 StructureCheck로 옮기면 E30·T가 요구하는 생성자 예외가 없다.
증상 — E30 테스트 작성 시점에 "어디서 던지나"를 다시 묻게 된다.
권고 — 문구 분리: "방어 복사와 빈 visits 거부만 한다 — pair·XOR은 StructureCheck 소유"로 한 문장에 정리.

### [D5] docs/implementation/stage-03-solution-propagation-evaluation.md:707-711 · stage-02:333 (§9)
문제 — "미해결 질문" 절에 **해소된** 질문이 본문 설명째 잔존한다 — stage-03 §9는 4행 중 3행(Q1·Q2·Q3)이 해소분이고, stage-02 §9도 ~~Q3~~ 해소 행을 유지한다. doc-audit.md D5의 실제 사례(Stage 1 §9)와 같은 유형.
근거 — grep 상태어 검사(§0.2 c): stage-03 L709-711 세 행 전부 "**해소 (2026-08-10 …)**"로 시작하는 한 문단씩. stage-02 L333 "~~Q3~~ … 해소됨 (2026-08-10)". 해소 내용 자체는 Domain·Plan 대조로 전부 참(오정보는 아님).
증상 — "미해결"을 찾아 §9로 온 독자가 닫힌 논의 3문단을 다시 읽고 재고한다. stage-03은 서두(L704-705)가 "남은 미결은 Q4 하나"라고 완화하고 있어 오판 위험은 낮다.
권고 — 삭제: 해소 행은 revisions 한 줄 + 반영 위치 표기로 접고 §9에는 미결(stage-03 Q4, stage-02 Q1·Q2)만 남긴다.

### [D4] docs/implementation/stage-02-travel-and-problem-freeze.md:34-35 (DoD 요약)
문제 — 상단 DoD 요약이 Plan Stage 2 DoD 3항목 중 2항목("이동표 보정 규칙 테스트 · Problem 불변성")만 적고 세 번째 **규모 DoD**(453² 합성 입력 1회, 시간·메모리 기록)를 빠뜨린다.
근거 — Plan L189-197(정독): DoD 불릿 3개 — 보정 테스트 · 수정 경로 없음 · "**규모**: … 소요 시간·메모리를 기록한다". stage-02는 T12(L294)와 revisions(L14-15)로 규모를 다루지만 상단 요약에는 없다.
증상 — 상단 요약만 보고 완료 판정을 내리면 규모 측정이 빠진 채 Stage 2를 닫는다 — Plan §1 편입 문장("테스트 표 전부 green")이 최후 방어선이라 실제 누락 확률은 낮다.
권고 — 문구 보완: 상단 DoD 줄에 "· 규모 측정(T12)" 추가.

### [D4] docs/implementation/stage-02:296-297 · stage-03:679-680 (Plan 인용 문자열)
문제 — 두 문서가 따옴표로 Plan "범위 문장"을 인용하지만 그 문자열이 Plan 원문에 **그대로는 존재하지 않는다** (의역을 직접 인용처럼 표기).
근거 — stage-02 인용 "「`Problem` 생성 시 참조·완전성 검증과 동결」" vs Plan L178 원문 "`Problem` 생성: 참조 일관성·완전성 검사 후 **동결**". stage-03 인용 "「적재 부호 규칙, 전파 루프, 기록 값(§7.3), metric, 사전식 비교」" vs Plan L240-242 원문 "적재 부호(initialLoad 포함), 전파 절차·기록 값(Domain §7 …) · metric(`Evaluation`), … 사전식 비교". grep으로 인용 문자열 검색 시 0건.
증상 — 근거를 확인하려고 Plan을 grep한 사람이 문장을 못 찾아 "근거가 삭제됐나"를 조사하게 된다 (D4 서술형 참조의 전형).
권고 — 문구 교정: 직접 인용 따옴표를 풀어 "Plan Stage N 범위 문장 취지" 같은 의역 표기로 바꾸거나 원문 그대로 인용.

---

## 2. 미확인 항목 (판정 불가 — 타 분할 소관)

| 위치 | 참조 | 사유 |
|---|---|---|
| stage-02:242 (N4) | "Stage 8 W16" | stage-08은 분할 8 파일. Plan D2 행(L494)에 같은 안건(비대각 D=9999 확인)이 실재함까지는 확인 |
| stage-03:567 (§4.4) | "Stage 6 §조립" | stage-06은 분할 8 파일 |
| stage-02:309 · stage-03 frontmatter | Architecture §2·§2.1·§2.2 세부 | 분할 4 파일. 모듈 3개·의존 방향 개요는 CLAUDE.md와 정합 확인만 |

## 3. 심각도 요약

| 심각도 | 건수 | 대표 |
|---|---|---|
| 치명 (구현이 틀림) | 0 | — |
| 중 (재작업 유발) | 2 | E35 "∅ → DEPOT_WINDOW" 과일반화 · Domain §7.1 동시-소진 위반 귀속 미반영 |
| 경 (읽기 불편) | 7 | E8 "(fixture 상황)" 사실 오류, §4.4 차고 NodeId 잔존 등 |

심각도 판단 메모: 두 "중" 결함 모두 가능/불가 판정은 바꾸지 않고 **위반 종류(FAILED 원인)와 테스트 기대값**만 가른다 — fixture(1일·전일 차고창·근무 00:00~23:30)는 교집합이 비지 않아 실행 경로에서 촉발되지 않으므로 치명 아님. 수치·공식·전파 절차 본문(§3.3~§3.5)·이동표 보정 절차(§3)·edge 표의 수치 주장은 전 건 Domain 원문·fixture 실측과 일치했다.
