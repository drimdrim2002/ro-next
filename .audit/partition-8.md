# 분할 8 감사 보고 — stage-06 · stage-07 · stage-08

감사일 2026-08-11 · 초점: wire 매핑이 fixture와 맞는가 · 벤치 전제 · 편집 없음(발견만)

## 0-1. 대상 파일별 실제 줄 수 (`wc -l`)

```
     655 docs/implementation/stage-06-app-assembly.md
     320 docs/implementation/stage-07-ecs-deployment.md
     327 docs/implementation/stage-08-benchmark-comparison.md
    1302 total
```

## 0-2. 기계 검사 5종 — 명령과 건수

| 검사 | 명령 | stage-06 | stage-07 | stage-08 |
|---|---|---:|---:|---:|
| a. 링크 | `grep -noE '\[[^]]*\]\(([^)]+)\)' <f>` 후 대상 `ls` | 7건 | 7건 | 4건 |
| b. 전칭어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|유일\|항상\|절대\|언제나' <f>` | 30건 | 12건 | 37건 |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토' <f>` | 36건 | 15건 | 41건 |
| d. 참조 | `grep -noE '§[0-9.]+\|Stage [0-9]+\|위 표\|아래 주석\|이미 [^ ]*는\|같은 방식' <f>` | 387건 | 177건 | 214건 |
| e. fixture | `python3 fixture_check.py` (아래 부록 A — 두 fixture·CSV 전량 로드, 키 합집합·값 분포·용량 재생 재계산) | 대조 주장 25건 | — | — |

- a: 링크 대상은 `../implementation-plan.md`·`../architecture-design.md`·`../master-design.md` 3종뿐 — 전부 실재. `docs/implementation/benchmark-results.md` 부재는 Stage 8 §4가 "실행 시점 생성"으로 선언 — 정상.
- b·c·d: 각 목록 전 건을 열람 판정 — 결함으로 판정된 것은 §1 목록에, 나머지는 부록 B의 그룹 판정.
- e: 25건 중 24건 데이터 일치, 1건은 문구 결함(§1-6). **수치 불일치 0건** — 문서의 fixture 수치 주장(452·31·205,209·453²·600·28·12.4MB·81~100%·27/31·9999 1건 등)은 전부 재계산과 일치했다.

## 0-3. 정독 범위

세 파일 모두 1행부터 끝 행까지 전량 정독 (stage-06: 1–655, stage-07: 1–320, stage-08: 1–327).
상위 문서 대조는 분할 규칙대로 Master·Domain의 해당 절 grep만 수행 (부록 C).
타 분할 파일(Stage 0~5·Architecture·Plan 본문)은 열지 않음 — 그쪽으로 나가는 참조는 부록 D "미확인"에 등재.

---

## 1. 결함 목록 (심각도 높은 순)

### 중

### [D1] docs/implementation/stage-06-app-assembly.md:500
문제 — §4.6이 `multiRotation`을 OptionsInput에 "(검증만, 미보관)"이라 쓰는데, §3.1-2 게이트는 `planInput.options()의 multiRotation`을 읽고, 같은 절이 "같은 규칙을 PlanNormalizer도 갖는다"고 한다 — 미보관이면 둘 다 불가능하다.
근거 — L500 `| multiRotation | (검증만, 미보관) |` vs L294 `planInput.options()의 multiRotation이 지원 범위 밖` · L299 `같은 규칙을 PlanNormalizer(Stage 1 절차 2)도 갖는다`. 오독 시나리오: 표대로 필드를 빼고 검증을 adapter 안에 넣으면 §10 Q1의 "adapter 단독 우회 금지(의미 규칙은 solver-core 소유)"를 위반하고, store 직접 투입 입력(N1의 최종 방어 경로)에서 normalizer가 multiRotation을 못 본다.
증상 — 컴파일 단계에서 §3.1-2를 구현할 수 없거나, adapter 단독 검증으로 이행돼 접수 우회 입력의 `multiRotation: "2"`가 FAILED 대신 실행된다.
권고 — 문구 분리: "미보관"의 주어(canonical `Plan`인지 `OptionsInput`인지)를 명시하고 Stage 1 §2.3 운반체 정본과 일치시킨다 (정본 대조는 분할 5 몫).

### [D1] docs/implementation/stage-06-app-assembly.md:504
문제 — §4.6이 `Termination.secondsSpentLimit`을 "adapter가 별도로 꺼내 `AlnsConfigFactory`에 넘긴다"고 쓰는데, §2.2의 adapter 출력은 `parse(byte[]) → PlanInput` 하나뿐이라 그 값의 운반 경로가 시그니처에 없다.
근거 — L504 `(canonical 아님) AlnsConfig.timeLimitSec — adapter가 별도로 꺼내 AlnsConfigFactory에 넘긴다` vs L198 `public PlanInput parse(byte[] wireJson);` · L329 `alnsConfig = alnsConfigs.create(adapter가 읽은 wire 시간 한도)` — parse와 create 사이를 잇는 필드·반환값이 어디에도 정의돼 있지 않다.
증상 — 구현자가 경로를 발명해야 한다. 최악 발명(adapter 인스턴스 필드에 마지막 파싱 값 보관)은 `concurrency: 2` 설정에서 다른 run의 시간 한도가 섞이는 경합 버그가 된다.
권고 — 문구 분리: OptionsInput(또는 PlanInput)에 담기는 필드명을 명시하고 "canonical(Plan)에는 안 담긴다"와 구분해 적는다.

### [D4] docs/implementation/stage-06-app-assembly.md:512
문제 — §5 서두가 "필드명은 §11.1의 이름 그대로"라 하는데, Domain §11.1의 이름과 §5 예시가 7개 필드에서 다르다.
근거 — Domain §11.1 (L934·L936): `driveDist, driveTime, stopCount, routeOperationalTime` · `totalDistance, totalRouteOperationalTime` · visits의 `load` ↔ §5 예시: `driveDistMeter`·`driveTimeSec`·`routeOperationalTimeSec`·`totalDistanceMeter`·`totalRouteOperationalTimeSec`·`loadWeightKg`/`loadVolumeCbm` (단위 접미사 부가 + load 2분해).
증상 — §11.1만 보고 ResultJsonWriter를 구현하면 T11 golden·Stage 8 §2 표(접미사 이름 인용)와 불일치 — 어느 쪽이 정본인지 재질문 발생.
권고 — 문구 교정: "이름 그대로"를 "§11.1의 의미 목록에 단위 접미사를 붙인 wire 표기(잠정)"로 바꾼다.

### [D5] docs/implementation/stage-07-ecs-deployment.md:240
문제 — 배포 절차 8이 "(§7 V6 — §9 Q1 해소 선행)"을 달고 있고 L243-244가 "fixture 충돌(Q1)과 분리"를 이유로 대는데, Q1은 해소됐고 DoD 문단(L31)은 "선행 조건은 없다"고 확정했다 — revision(2026-08-10)은 "V6의 'Q1 해소 선행' 조건을 해제"했다고 기록했으나 §4에 그 문구가 남았다.
근거 — L240 `8. DoD … (§7 V6 — §9 Q1 해소 선행)` vs L31 `**선행 조건은 없다**` · L292 V6 `(선행 조건 해제됨 — §9 Q1 해소)` · frontmatter L17.
증상 — DoD 실행자가 절차 8에서 멈춰 "아직 남은 선행 조건이 있는가"를 §9까지 되짚어 확인하게 된다.
권고 — 삭제: L240의 괄호 문구와 L243-244의 "fixture 충돌(Q1)" 근거를 해소 후 문장으로 정리 (Q1 항목이 이미 "분리 이점은 남는다"로 대체 근거를 제공).

### [D1] docs/implementation/stage-08-benchmark-comparison.md:235
문제 — §5 표가 `AlnsConfig` 튜닝 목록에 `maxIterations`를 넣었는데, 같은 문서 §4 표의 종료 값은 `MAX_STEPS`이고 Stage 6 §1.1·§2.3(budgetOf)·Domain §2.5.1은 최대 step 수를 예산으로 분류한다 — 같은 개념의 두 이름이거나, 정의 없는 새 파라미터다.
근거 — L235 `AlnsConfig 나머지 (… maxIterations)` vs L204 `"종료"(TIME_LIMIT/MAX_STEPS/IDLE_STEPS/IDLE_TIME)` · stage-06 L123 `max-steps` · Domain §2.5.1 L333 "최대 실행 step 수 … 운영 설정만". maxSteps와 같다면 §3 표의 예산/튜닝 분리(예산은 searchBudget에 기록, 튜닝은 §4 표 소유)와도 충돌한다.
증상 — 실험자가 "maxIterations를 코드로 바꾸는" 실험을 하면 그 값이 결과 run 메타에 남는 예산인지 §4 표에만 적을 튜닝인지 갈려 기록 체계가 깨진다.
권고 — 실측 후 교정: Stage 4 §3.3의 실제 필드명으로 통일 (대조는 분할 7·B 몫) — maxSteps라면 §5 표에서 예산 축을 빼야 한다.

### 경

### [D1] docs/implementation/stage-06-app-assembly.md:493
문제 — §4.5 C행 "구조적 배제: 필드 자체가 없다"의 주어가 문장에 없어 "wire에 C가 없다"로 읽히는데, 실제 wire에는 C가 전 건 있다.
근거 — python 실측: floor·원본 모두 이동표 205,209건 전부에 `C` 키 존재 (값 `O` 204,805 · `G` 404) — 괄호의 "fixture 실값 O·G"가 이를 시사하나 본문과 반대 방향이다.
증상 — "없다"로 읽은 구현자가 C 존재를 오류로 거부하거나, 매핑 대상으로 착각한다.
권고 — 문구 분리: "TravelEntryInput에 C 필드를 만들지 않는다 (wire에는 있으나 읽지 않음)"로.

### [D4] docs/implementation/stage-06-app-assembly.md:304
문제 — §3.1-2b가 "빈 `zoneIds`"를 "Domain §12의 접수 4xx 분류"로 귀속시키는데 Domain §12에 그 항목이 없다.
근거 — `grep -n '빈 집합\|빈 zoneIds' docs/domain-design.md` → 0건. Domain §12 입력 오류 예(L959)는 "스키마 위반, 소수 거리, close==open 시간창, multiRotation ≤ -2"뿐. §4.4(L482)는 같은 규칙을 "정규화" 소유로 적는다.
증상 — Domain §12에서 근거를 찾다 실패 — 규칙의 정본 위치(Stage 1 추정)를 재추적하게 된다.
권고 — 참조 교정: 근거를 Stage 1의 해당 규칙 번호로 바꾸거나 예시에서 제외.

### [D4] docs/implementation/stage-06-app-assembly.md:653
문제 — §10 Q3 셀이 Domain §12를 "소수 거리·**알 수 없는 vhclOwnTyp**을 접수 4xx로 분류"로 인용하나, 현행 Domain §12에는 vhclOwnTyp이 없다 (2026-08-11 유예 때 제거).
근거 — `grep -n vhclOwnTyp docs/domain-design.md` → L33(개정 이력)·L247(유예 표)뿐, §12에 없음. 본 문서 revision(L34-36)도 "접수 4xx 예시에서 vhclOwnTyp 제거"를 선언했는데 Q3 셀에는 남았다.
증상 — Q3의 충돌 서사를 검증하려고 Domain §12를 열면 인용문이 없다.
권고 — 문구 교정: "(당시 문면)" 표기를 붙이거나 vhclOwnTyp을 셀에서 제거.

### [D1] docs/implementation/stage-06-app-assembly.md:161,307
문제 — `SolveKey.issue`는 `Optional<String> customerId`를 받는데 §3.1-3은 `planInput.customerId()`를 직접 넘기고, §4.1은 부재를 null 관례로 정의한다 — Optional/null 관례가 문서 안에서 갈린다.
근거 — L161 `issue(Optional<String> customerId, …)` vs L307 `SolveKey.issue(planInput.customerId(), …)` vs L419 공통 규칙 5 "부재(optional)는 전부 null로 넘긴다".
증상 — 타입 불일치로 구현 시 즉시 드러나긴 하나, PlanInput 접근자의 반환 타입을 재질문하게 된다.
권고 — 문구 교정: §3.1-3에 `Optional.ofNullable(...)` 감싸기를 명시하거나 시그니처를 null 허용으로.

### [D5] docs/implementation/stage-06-app-assembly.md:645-655 · stage-07-ecs-deployment.md:312-321
문제 — 두 문서의 "미해결 질문" 절 전 항목이 실제로는 해소(stage-06 Q1·Q3, stage-07 Q1) 또는 D1~D3 이관(stage-06 Q2·Q4·Q5, stage-07 Q2·Q3) 상태다 — 절 서두 "확정 문서로 답이 안 나오는 것만 남긴다"와 어긋난다.
근거 — stage-06 §10 5행 전부·stage-07 §9 3행 전부의 "잠정 처리" 열이 "해소" 또는 "이관"으로 시작한다. doc-audit.md D5의 실제 사례(Stage 1 §9 해소 Q1·Q2 잔존)와 동일 패턴.
증상 — 닫힌 논의 8건을 미결로 알고 한 문단씩 다시 읽는다.
권고 — 문구 분리: stage-08 §9처럼 "해소된 질문(기록용)" 절로 분리하거나 표 제목을 "질문 이력"으로.

### [D5] docs/implementation/stage-08-benchmark-comparison.md:322-326,287-288
문제 — §9 "남은 질문" 표 안의 Q1 내용이 "해소"라 표 제목과 모순이고, §7 말미가 "선행 조건(§1) 미해소로 비교가 불가능한 동안은 … 남는 것은 P1의 결정"이라며 §1이 닫은 P1을 미결 시나리오로 되살린다.
근거 — L322 `**남은 질문**:` 아래 L326 Q1 셀 `**해소 (2026-08-10 …)**` · L287-288 vs L59 `**미해소 조건은 하나도 남지 않았다**`.
증상 — §1과 §7·§9가 서로 반대 상태를 말해 어느 쪽이 최신인지 확인하게 된다.
권고 — 문구 교정: Q1을 위 "해소된 질문(기록용)" 표로 옮기고 §7 말미 문장은 과거형 주석으로.

### [D2] docs/implementation/stage-08-benchmark-comparison.md:284
문제 — §7 자기 검증 "이 문서에는 실측 지표 값이 등장하지 않는다"가 전칭인데 문서 안에 실측 지표 값이 있다.
근거 — §2 표에 Win의 미배정 **0**·차량 수 **31**(비교 4축 중 2축의 확정치), §6 W1에 부피 사용률 **81~100%·27/31 ≥93%** — 전부 CSV·fixture 실측치다. (Plan의 의도인 "목표 수치·허용 오차 사전 확정 금지" 자체는 안 어겼다 — 거짓인 것은 검증 문장이다.)
증상 — 자기 검증 문장을 믿은 검토자가 §2·W1의 수치를 보고 문서 신뢰를 재평가한다.
권고 — 문구 교정: "우리 솔버의 실측 지표·목표치가 등장하지 않는다"로 좁힌다.

### [D4] docs/implementation/stage-08-benchmark-comparison.md:297
문제 — §8이 "폐기 패턴 (Master 서두·§3-⑪)"을 근거로 드는데 Master §3 결정 표는 1~10번뿐이라 ⑪이 없다.
근거 — `grep -n '^| [0-9]' docs/master-design.md` → 행 1~10 (L63-72). 폐기 서술 자체는 Master L21·L101(서두·§5)에 있다.
증상 — 존재하지 않는 결정 번호를 찾아 Master를 뒤진다.
권고 — 참조 교정: "Master 서두·§5"로 (또는 실재하는 행 번호로).

---

## 2. 미확인 (근거를 확보하지 못한 주장 — 결함 아님, 판정 불가 기록)

| 위치 | 주장 | 사유 |
|---|---|---|
| stage-06:479 | 규약 PDF 표기가 `maxDriveDistc`(오타 추정) | PDF 텍스트 기계 추출 실패 (zlib 해제 후 텍스트 연산자 미검출 — CID 인코딩 추정) |
| stage-06:462·E4 | 규약이 itemId Mandatory | 〃 |
| stage-06:655 Q5 | 스펙 샘플 `driverRestTimeRatio` 0.15 | 〃 |
| stage-06:294 | "부재는 규약 default 0" (규약 문면) | 〃 (Domain §2.5 L258 "0은 미설정(규약 기본값)"과는 일치) |
| stage-06·07·08 전반 | Stage 0~5·Architecture·Plan 세부 절 참조 (운반체 필드·AlnsConfig 필드명·`RouteFacts`·`NodeId.delivery`·Verification 타입·Architecture §3·§5·§6·§8 문면 등) | 분할 규칙 — 해당 분할(4·5·6·7)·통합 B 몫. 특히 §1-1·§1-2·§1-5는 Stage 1 §2.3·Stage 4 §3.3 대조가 필요함을 B에 인계 |
| stage-07:83,95 | docker 베이스 이미지 태그 2종 실재 | 오프라인 — 레지스트리 조회 불가 |
| stage-07:255 | AWS SDK Java 2.21+의 `AWS_ENDPOINT_URL_S3` 지원 | 오프라인 (문서 스스로 "지원하지 않으면 재량" 폴백 명시) |
| stage-07:166-169, 264 | S3 ListBucket 없으면 GetObject 403 · ECS SIGTERM 유예 기본 30초 | 오프라인 — 일반 지식과는 부합 |

## 3. 심각도 요약

| 심각도 | 건수 | 대표 |
|---|---:|---|
| 치명 (구현이 틀림) | 0 | — |
| 중 (재작업 유발) | 5 | stage-06:500 — §4.6 `multiRotation` "(검증만, 미보관)" ↔ §3.1-2 게이트·정규화 최종 방어 모순 |
| 경 (읽기 불편) | 8 | stage-08:297 — Master §3-⑪ 끊어진 참조 |

치명 0의 근거: fixture 수치 주장 25건 전량 재계산 일치(부록 A) — 이 분할의 초점인 "wire 매핑 ↔ fixture"와 "벤치 전제(P2·W1·W7·W8·W15·W16)"에서 데이터가 틀린 곳은 없었다. 중 5건은 전부 "구현자가 멈춰서 물어야 하는" 종류다.

---

## 부록 A. 기계 검사 e — fixture 대조 25건 전 판정

스크립트: 스크래치 `fixture_check.py` (win_poc_case_floor.json·win_poc_case.json·alns_result.csv 전량 로드, Decimal 재계산). 주요 출력 요지와 판정:

| # | 문서 주장 (위치) | 실측 | 판정 |
|---|---|---|---|
| 1 | 주문 452·차량 31·이동표 205,209 (S6 T1) | 452 · 31 · 205,209 | 일치 |
| 2 | weight `26.2` BigDecimal 원문 (S6 T1) | 문자열 `"26.2"` 존재 (item weight/volume/qty 전건 문자열) | 일치 |
| 3 | duration 300 (S6 T1) | 전 주문 `"300"` | 일치 |
| 4 | qty `"1"`→1 (S6 T1) | 452건 전부 문자열 `"1"` | 일치 |
| 5 | floor D 310708 정수 (S6 T1·공통 규칙 1) | D 205,209건 전부 int, 310708 존재 | 일치 |
| 6 | 원본 D `"310708.03"` (S6 공통 규칙 1) | 존재. 원본 D = int 453(대각) + 소수 문자열 204,756 | 일치 |
| 7 | 혼재: qty 문자열·taskTime 숫자 0 (S6 공통 규칙 1) | qty str · item taskTime int 0 | 일치 |
| 8 | options 매핑 7키 / wire 10키 (S6 T1·§4.6) | 최상위 10키 평면 (`Optimizer.DefaultSpeed` 등 점 포함 문자 키), 무시 3키 = driverRestTimeRatio(`"0.00"`)·difficultySortType(`"WINCOMMERCE"`)·customerAbbr(`"WCM"`) | 일치 |
| 9 | Termination 600 · VehicleMaxStopCount 28 (S6 §4.6) | `"600"` · `"28"` | 일치 |
| 10 | 두 fixture 모두 multiRotation `"1"` (S6 Q1 등) | 원본·floor 둘 다 `"1"` | 일치 |
| 11 | shprId S3853 · planId WINCOMMERCE_TEST_CBM_2 · 주문 수준 customerId WINCOMMERCE (S6 §4.1·Q2·§5) | 일치 (customerId 452건 전부 WINCOMMERCE) | 일치 |
| 12 | itemId 전건 blank 452/452 (S6 §4.3·E4) | 452/452 blank | 일치 |
| 13 | wire C 실값 O·G (S6 §4.5 괄호) | O 204,805 · G 404 — 단 본문 "필드 자체가 없다"는 §1-6 결함 | 데이터 일치 |
| 14 | body ≈ 12.4 MB (S6 E17) | 12,373,785 bytes | 일치 |
| 15 | 매핑표 커버리지 — plan 11키·order 14키·item 9키·vehicle 7키·depot 8키·matrix 5키 전부 매핑/무시 행에 존재 | 키 합집합 대조 — 누락 0 (fixture 차량 키는 정확히 7종: vehicleId·vehicleFeature·maxWeight·maxVolume·workStartTime·workEndTime·speed) | 일치 |
| 16 | CSV BOM 존재·헤더 orderNo,vehicleId,stopSeqNo·452행 (S8 P2·§3.1) | BOM ✓ · 헤더 ✓ · 452행 | 일치 |
| 17 | 차량 V001~V031 31대 전부 = fixture와 일치 (S8 P2) | 31대, 집합 동일 | 일치 |
| 18 | stopSeqNo 차량마다 1부터 연속 (S8 P2) | 비연속 차량 0 | 일치 |
| 19 | 주문 452건 1:1, 미배정 0 (S8 P2·§2) | 양방향 차집합 0 | 일치 |
| 20 | Win 해 31대 전부 용량 내, 무게·부피 초과 0 (S8 W1) | 초과 0·0 (Decimal 재계산) | 일치 |
| 21 | 부피 사용률 81~100%, 27/31이 ≥93% (S8 W1) | 81.0%~100.0% · 27/31 | 일치 |
| 22 | 차고 00:00:00~23:59:59·taskTime `"60"`·trips oneway·endDepot 부재 (S8 W7) | 전부 일치 (depot 1건) | 일치 |
| 23 | 근무창 31대 전부 00:00:00~23:30:00 · dateRange 1일 (S8 W15) | 전부 일치 (2023-09-13~09-14) | 일치 |
| 24 | item taskTime 전건 0·qty 전건 1·order당 item 1개 (S8 W8) | 전부 일치 | 일치 |
| 25 | 지점 453·453²=205,209 완전 행렬 · 대각 453건 전부 D=9999 · 비대각 D=9999 정확 1건 WIN_2306→WIN_3225 (U=991) (S8 W9·W16) | 전부 일치 | 일치 |

코드 대조(문서 주장 ↔ 저장소 실물): `.dockerignore`에 `target` 있음 (S7 E13 ✓) · 루트 pom enforcer Java `[25,26)`·Maven `[3.9.14,)` (S7 D3·E12 ✓) · app artifactId `ro-next-app` + spring-boot-maven-plugin repackage (S7 §2.2 jar 이름 ✓) · 루트 `Dockerfile` 부재 (S7 서두 "재작성" 전제 ✓).

## 부록 B. 전칭어·상태어 전 건 판정 요약

- 전칭어 79건 (30+12+37): 실측 가능한 것 전부 부록 A로 검증 — "452건 전부"·"31대 전부"·"전건"·"한 번도"류 12건 일치. 규칙 서술("항상 먼저"·"유일한 자리"·"전부 optional" 등)은 적용 대상이 문장 안에 있는지 확인 — 결함은 stage-08:284 1건(§1-12), stage-06:493 1건(§1-6). stage-08:59 "미해소 조건은 하나도 남지 않았다"는 같은 문단이 P3(Stage 7 완료 대기)를 명시해 "미결정 없음" 의미로 무해 판정.
- 상태어 92건 (36+15+41): "해소·확정·이관" 표기 자체는 개정 이력·Plan D1~D3 인계와 정합. 결함은 잔존 배치 3건 — §1-4(stage-07 절차 8), §1-10(미해결 표), §1-11(stage-08 §7·§9).

## 부록 C. 참조 778건 그룹 판정

- **자기 문서 내부** (§N·EN·TN·VN·WN·QN·PN): 전 건 대상 실재 확인 — stage-06 E1~E22(E1b·E1c·E6b·E6c 포함)·N1~N7·T1~T16(개번된 T14·T16 정합), stage-07 V1~V6·E1~E14·D1~D7, stage-08 P1~P3·W1~W16·Q1~Q5. 끊어진 내부 참조 0. (stage-08 §6 표는 W7 뒤에 W15가 삽입된 비순차 배열 — 결함 아닌 배치 특이로만 기록.)
- **Master·Domain 인용** 17건 대조: Master §5(L99 문장 그대로) ✓ · Master §3-⑥(행 6) ✓ · **Master §3-⑪ ✗ (§1-13)** · Domain §2.5 집합 판정 {0,1}/-1·2↑=422/≤-2=400 (L258·L282·L959-960) ✓ · Domain §12 — 소수 거리·close==open ✓, **빈 zoneIds ✗ (§1-7)**, **vhclOwnTyp ✗ (§1-8)** · Domain §11.1 depotDeparture·depotReturn·따로 기록(배송정책/예산) ✓, **필드명 "그대로" ✗ (§1-3)** · Domain §2.5.1 예산 5키 "운영 설정만" ✓ · Domain §7.3 5성분 합(주행+고객대기+차고대기+서비스+휴식, L725-726) — stage-08 §2 문면과 정확 일치 ✓ · Domain §7.4 stopCount(depot 미산입·연속 첫 진입)·복귀 arc ✓ · Domain §2.4 유예 표 3행(치수·차량 trips·vhclOwnTyp) ✓ · Domain §2.3 legacy dueDate ✓ · Domain §2.2 shprId ✓ · Domain §3.1 ×qty ✓ · Domain §8.4·§10.2·§10.3(병목) ✓ · Domain §2.5 depot.taskTime=복귀 선적(L261) — stage-06 §4.2·stage-08 W7 근거 ✓.
- **타 분할 문서** (Stage 0~5·Architecture·Plan 세부): 미정독 — 부록 D(§2 표)로 인계. 분할 B 대조 우선순위: ① Stage 1 §2.3 운반체에 multiRotation·secondsSpentLimit 필드가 있는가 (§1-1·§1-2) ② Stage 4 §3.3 AlnsConfig에 maxIterations라는 필드가 있는가 (§1-5) ③ Architecture §3.1 개정(2026-08-11 접수 2b)이 실제 반영됐는가 (stage-06 N1·Q3의 전제).
