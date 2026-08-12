# 분할 1 감사 보고 — docs/master-design.md · docs/README.md

감사일: 2026-08-11 · 초점: 상위 원칙을 하위 문서가 어겼는지 (D3의 출발점)

## 0-1. 대상 파일 실제 줄 수 (`wc -l`)

```
     152 /Users/brown/workspace/ro-next/docs/master-design.md
      28 /Users/brown/workspace/ro-next/docs/README.md
     180 total
```

## 0-2. 기계 검사 5종 — 명령과 건수

| 검사 | 명령 (요약) | 건수 | 판정 결과 |
|---|---|---:|---|
| a. 링크 | `grep -noE '\[[^]]*\]\(([^)]+)\)' docs/master-design.md docs/README.md` + 대상별 `[ -e ]` 실재 확인 (frontmatter `supersedes` 포함 13경로) | master 12 + README 11 = 23 | **끊어진 링크 0** — 13개 대상 경로 전부 `OK` |
| b. 전칭어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|없음\|유일\|항상\|절대\|언제나'` | master 15 + README 4 = 19 | 전 건 판정 완료 — 문제 2건 (결함 #2·#3) |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토'` | master 2 + README 1 = 3 | 전 건 현재 상태와 일치 (11행 최초 확정 · 92행 "확정 안 함" 선언 · README 3행) — 문제 0 |
| d. 참조 | `grep -nE '§[0-9]\|Stage [0-9A-Za-z]\|위 표\|아래 주석\|이미 .*는\|같은 방식\|위 문단'` | master 13 + README 1 = 14 | 전 건 대상 실재·내용 확인 — 약한 참조 1건 (결함 #4). Domain §2.5·Architecture §2(58행)·Plan §1 말미(448~477행 Stage Extra 절)·Stage Extra E1~E3·§2-③·§4·§6 전부 실재 확인 |
| e. fixture | Python으로 `data/win_poc_case_floor.json` 키 합집합·건수 추출 | 문서의 wire 주장 4건 | **전부 실측 일치** — 아래 상세 |

fixture 실측 상세 (e):

```
vehicle count: 31, key union 7개: maxVolume·maxWeight·speed·vehicleFeature·vehicleId·workEndTime·workStartTime
  → master:116 "wire에 없는 차량별 trips·치수 축·차량 소유 구분" 일치 (셋 다 없음)
order count: 452, item count: 452, item key union: itemId·orderId·prodId·qty·taskTime·volume·volumeUnitCd·weight·weightUnitCd
  → 치수(폭·높이·깊이) 필드 없음 — E2 유예 근거 일치. pickup 위치 필드 없음 → master:65 "현 규약 전부 배송만" 일치 (Domain 118행 동일 주장)
options.multiRotation = "1" → master:88·109 "{0,1} 통과·fixture 접수 통과" 일치 (Domain §2.5 표와 동일)
distanceMatrix entries: 205,209 → doc-audit §0 기재값 일치
data/alns_result.csv 실재 → master:99 "기존 엔진(Win) 결과와 지표 비교" 근거 실재
```

## 0-3. 정독한 범위

- `docs/master-design.md` 1~152행 전문 (frontmatter 포함), `docs/README.md` 1~28행 전문 — 샘플링 없음.
- D3 대조용 하위 문서 grep (전문 정독 아님 — 해당 분할 소관): Domain §2.5 절 전체·118행,
  Architecture 27~31·56·58·64·94~114·180·297·313행, Plan 헤더 전체·§1 말미·76·151·287~348·421~464·493~547행,
  stage-01 (trips·multiRotation 잔재), stage-04 421행, stage-08 (파라미터 절), stage-05 56행,
  stage-07 305행, Stage Extra 전 항목 요약, `solver-core/pom.xml` 의존 절.

## 1. 결함 (심각도 높은 순)

### [D4] docs/README.md:11
문제 — Architecture 행 설명이 "모듈(2개)"인데 현행은 3개(core·profile·app)다. 2026-08-10 3계층 개정 때 README만 미개정.
근거 — `grep -n '모듈' docs/architecture-design.md` → 58행 `## 2. 모듈 구조 — 3개 (확정)`, 10행 revisions `2026-08-10 3계층 확정 — 모듈 3개`; master 72행 결정 #10 "모듈 3개". README 11행만 "모듈(2개)".
증상 — 진입점 문서가 상위 결정 #10과 정면 충돌하는 숫자를 안내 → 독자가 어느 쪽이 맞는지 되물어야 하고, README의 "충돌 시 우선" 규칙을 알아야만 자력 해소된다.
권고 — 문구 교정: README 11행을 "모듈(3개)"로 개정 (한 단어 수정).

### [D2] docs/README.md:5-12
문제 — "현행 문서 (**이것만 효력 있음**)" 표에 구현 계약인 `docs/implementation/` Stage 문서 10건(stage-00~08·stage-extra)과 그 목차(README)가 빠져 있어, 문면대로 읽으면 Stage 문서가 효력 없음이 된다.
근거 — `ls docs/implementation/` → README.md + stage 문서 10파일 실재. Plan 84~477행이 Stage별 상세를 이 파일들로 위임하고, master 121행도 Stage Extra를 규범 등재부로 지목한다. 그러나 README 현행 표는 4행(마스터·도메인·아키·플랜)뿐.
증상 — README로 진입한 구현자가 "이것만"을 문자대로 취해 Stage 문서를 비규범 참고자료로 취급할 수 있다 — Stage 문서는 "표·코드 블록에 없는 선택은 하지 않는다"는 계약이라 효력 오인의 파급이 크다.
권고 — 문구 분리: 표에 5행(`implementation/` Stage 상세, 구현 계약)을 추가하거나 "이것만"의 범위가 최상위 4문서 + 그 하위 위임 문서임을 명시.

### [D2] docs/master-design.md:137
문제 — "과거 문서는 **전부** [docs/deprecated/]에 있으며"가 실측과 다르다 — 과거·비규범 문서가 deprecated/ 밖 세 경로에 더 있다.
근거 — `[ -e ]` 확인: `docs/master-design-sessions/`·`docs/arranged/`·`docs/orgin/` 전부 실재. README 23~25행 아카이브 표가 이 셋을 deprecated/와 **별도 행**으로 등재.
증상 — master만 읽은 사람이 그 세 디렉터리의 존재를 모르거나, 반대로 "deprecated/ 밖에 있으니 현행인가"라고 오인할 수 있다 (효력 판정은 README까지 읽어야 닫힌다).
권고 — 문구 교정: "전부 docs/deprecated/에" → "deprecated/ 등 아카이브(README 아카이브 표)에".

### [D4] docs/master-design.md:112
문제 — "차량 값이 `options` 기본값을 덮어쓰는 규칙(Domain §2.5)을 적용하면 된다"가 가리키는 규칙이 Domain §2.5에 **현행 규칙으로는 없다** — 차량별 `trips` 유예 주석의 "되살릴 때" 지침(`차량 trips ▷ options.trips`)으로만 존재하고, 대상 필드도 multiRotation이 아니라 trips다.
근거 — `grep -n '덮어쓰' docs/domain-design.md` → 0건. Domain 307~310행: "**되살릴 때는** `차량 trips ▷ options.trips` 우선순위를 … 끼워 넣으면 되고". Stage Extra 85행도 같은 것을 "복원" 대상으로 등재. 2026-08-11 개정(frontmatter 12~15행)이 "이미 trips가 쓰는 방식" 끊어진 참조를 제거하며 넣은 대체 참조가 다시 유예 상태의 규칙을 가리킨다.
증상 — Domain §2.5에서 "덮어쓰는 규칙"을 찾는 독자가 활성 규칙을 못 찾고 유예 주석에서 유추해야 한다. 미래 시점("multi-trip을 여는 그 시점") 지침이라 지금 구현이 틀리지는 않는다.
권고 — 문구 교정: "(Domain §2.5)" → "(Domain §2.5 유예 주석·Stage Extra E1의 복원 지점)"처럼 유예 상태임을 드러낸다.

### [D1] docs/master-design.md:72 (결정 #10)
문제 — "solver-core(순수 Java, **의존성 0**)"에 적용 범위(scope)가 없다 — 실제로는 compile 의존 0이고 test scope 의존은 있으며, 같은 결정 문장이 ArchUnit **테스트**를 요구해 문면끼리 충돌한다.
근거 — `solver-core/pom.xml` 17~25행: JUnit·ArchUnit 의존 2건(`<scope>test</scope>`). Architecture 104행 "(test scope 제외)"·114행 "테스트 의존성(JUnit, ArchUnit)은 test scope로만 허용"으로 하위 문서가 정밀화. CLAUDE.md DoD 검사도 `-DincludeScope=compile`.
증상 — master만 읽으면 "의존성 0"과 "ArchUnit 테스트로 강제"가 한 칸 안에서 모순으로 보여 되묻게 된다. 극단적으로는 test 의존까지 금지로 읽어 ArchUnit 룰을 못 만든다.
권고 — 문구 교정: "의존성 0" → "compile 의존 0 (test scope 제외)".

### [D1] docs/README.md:14
문제 — 우선순위 문장 "충돌 시 Master → Domain → Architecture 순으로 우선한다"가 표의 4번 문서(Implementation Plan)를 빼놓아, Plan(·Stage 문서)과 충돌할 때의 순위가 README 문면으로는 정해지지 않는다.
근거 — README 7~12행 표는 4개 문서인데 14행 우선순위 사슬은 3개. master 136행 읽는 순서와 doc-audit §0 권위 순서는 Plan을 4순위(그 아래 Stage 문서)로 명시.
증상 — Plan/Stage 문서와 상위 문서가 어긋난 지점을 발견한 독자가 어느 쪽을 따를지 이 문서로 판정 못 하고 되묻는다.
권고 — 문구 교정: "… Architecture → Implementation Plan(→ Stage 상세) 순으로 우선한다".

## 2. D3 대조 결과 (초점 항목 — 위반 미발견 확인 기록)

Master 원칙 → 하위 문서 대조는 grep으로 수행 (전문 정독은 각 분할 소관). 위반이 안 나온 항목도 근거를 남긴다:

| Master 원칙 | 대조 명령 | 결과 |
|---|---|---|
| 결정 #8·#9 RDB·Redis·SQS·Lambda·Step Functions 금지 | `grep -rnE 'RDB\|Redis\|SQS\|Lambda\|Step Functions\|DynamoDB' docs/*.md docs/implementation/*.md` | 4건 전부 부정·폐기·마이그레이션 문맥 (arch 27·297·313, stage-07 305) — **도입 서술 0** |
| §6 "미리 필드를 만들어 두지 않는다" (2026-08-11 전 항목 승격) | `grep -n 'E19b\|차량별 trips\|VehicleInput.trips' docs/implementation/stage-01-*.md` | 8건 전부 revisions 이력·유예 표기 문맥 — **살아 있는 선제 필드 0**. Stage Extra E1~E3 등재·Plan §1 말미 표와 삼각 일치 |
| 결정 #5 MIP 재조합 범위 밖 | `grep -rn 'MIP\|CP-SAT'` 현행 문서 | master §6 자신 외에는 Plan 439행 부정 문맥뿐 — 위반 0 |
| 결정 #7 core에 customerId 분기 금지 | `grep -rn 'customerId'` 현행 하위 문서 | 전건 wire 매핑·solveKey·profile resolve(app 소관) 문맥 — core 분기 서술 0 |
| §4 multiRotation `{0,1}` 통과·`2`이상/`-1` 거부 | `grep -rn 'multiRotation'` 현행 하위 문서 | Architecture 180행·Plan 151·493행·stage-01 §4·E6~E6c·T5 전부 `{0,1}` 통과로 일치 — 반전·모순 0 |
| §4 탐색 파라미터 문서 확정 안 함 | `grep -n '파라미터' stage-04·stage-08` | stage-04 421행 "§3.3 기본값의 실측 조정 → Stage 8", stage-08 §5 조정 실험 — "확정" 선언 없음, 위반 0 |
| 결정 #6 유일 발행 규칙 | `grep -rn '발행'` 현행 문서 | Plan 312·348행, stage-05 56행 전부 같은 규칙의 재진술 — 경쟁 발행 규칙 0 |
| §2-⑤ "(또는 S3 직접 읽기)" | `grep -n '직접' docs/architecture-design.md` | 56행 "S3 권한이 있으면 직접 읽어도 된다" — 실재, 참조 유효 |
| §6 PICKUP_DELIVERY "canonical은 이미 지원" | `grep -n 'PICKUP_DELIVERY' docs/domain-design.md` | 114·118행 "canonical이 이미 정의" — 일치 |
| §6 등재 포인터 "Plan §1 말미" | Plan 헤더 구조 + §1 말미 본문 | 448~477행 `### Stage Extra — 유예 항목`이 `## 1.` 마지막 절로 실재, E1~E3 표 일치 |

## 3. 심각도 요약

| 심각도 | 건수 | 대표 |
|---|---|---|
| 치명 (구현이 틀림) | 0 | — |
| 중 (재작업 유발) | 2 | README:11 "모듈(2개)" (상위 결정 #10과 충돌) · README:5-12 "이것만 효력 있음"에서 Stage 문서 누락 |
| 경 (읽기 불편) | 4 | master:137 "과거 문서 전부 deprecated" · master:112 유예 규칙을 현행처럼 참조 · master:72 "의존성 0" scope 미명시 · README:14 우선순위 사슬에 Plan 누락 |
