# 감사 보고서 — 분할 4

대상: `docs/architecture-design.md` · `docs/implementation-plan.md`
초점: Stage 문서 DoD와의 일치 · 경계 규칙
일자: 2026-08-11

## 0. 사전 보고 (샘플링 아님의 증거)

### 0.1 실제 줄 수 (`wc -l`)

```
     314 docs/architecture-design.md
     550 docs/implementation-plan.md
     864 total
```

### 0.2 기계 검사 5종 — 명령과 건수

| 검사 | 명령 (요지) | 건수 | 판정 |
|---|---|---|---|
| a. 링크 | `grep -nE '\]\(([^)]+)\)' <두 파일>` 후 대상 17개를 `[ -e ]`로 확인 | 링크 27행 · 고유 대상 17개 | **17/17 실재** — 끊어진 링크 0 |
| b. 전칭어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|유일\|항상\|절대\|언제나'` | arch 5행 · plan 28행 | 전 건 판정 — 결함 2건 (아래 #11·#12), 나머지는 규범 문장이거나 실측 일치 |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토'` | arch 11행 · plan 27행 | 전 건 판정 — 결함 2건 (아래 #2·#6), 나머지는 현재 상태와 일치 |
| d. 참조 | `grep -nE '§[0-9]\|Stage [0-9]\|위 표\|아래 주석\|이미 .*는\|같은 방식'` | arch 30행 · plan 100+행 | 전 건 판정 — Stage 0 §4.4·Stage 2 N4·Stage 5 §9 Q5·Stage 6 §10 Q3/Q4/T13·Stage 7 §4/V6·Stage 8 P1/§6 W1·stage-extra E1~E3/§5·Master §2/§4/§6·Domain §2.1/§2.4/§2.5/§3.1/§4/§7.2/§8.3/§8.4/§11.1/§12 전부 실재·내용 일치 확인. 결함 4건 (아래 #2·#3·#10·#13) |
| e. fixture | Python으로 `data/win_poc_case_floor.json` 실측 (아래 0.3) | 문서 주장 12개 항목 | **전부 일치** — fixture 어긋남 0 |

### 0.3 fixture 실측 결과 (검사 e 상세)

```
orders 452 ✓ · vehicles 31 ✓ · depot 1 ✓ · distanceMatrix 205,209 = 453² ✓
options: multiRotation "1" ✓ · Termination.secondsSpentLimit "600" ✓
         Optimizer.VehicleMaxStopCount "28" ✓ · Optimizer.DefaultSpeed "45" ✓
비대각 D=9999: 정확히 1건 (WIN_2306→WIN_3225, U=991) ✓ · 대각 self 453건 D=9999 ✓
item qty: 전건 "1" ✓ · order당 item 1개 ✓
vehicle 키 합집합 7개 (vehicleId·vehicleFeature·maxWeight·maxVolume·workStartTime·workEndTime·speed) — trips 없음 ✓
```

### 0.4 정독 범위

- `docs/architecture-design.md` 1~314행 전체 (Read 1회, 전문).
- `docs/implementation-plan.md` 1~550행 전체 (Read 1회, 전문).
- 교차 검증용 부분 grep(전문 정독 아님 — 해당 분할 소관): domain-design.md, master-design.md,
  stage-00~08, stage-extra, `solver-core/.../ArchitectureRulesTest.java`(전문 — 25줄),
  stage-00 §4.4 본문(sed 349~420행), 저장소 루트 `ls`.

---

## 1. 결함 (심각도 높은 순)

### [D3] docs/architecture-design.md:111-112
문제 — §2.1이 "`solve`의 결과 객체(최종 `Solution`)는 값으로 전달받는다"라고 써서, verify가 `Solution` 타입을 인자로 받는 API로 읽힌다. 같은 문서 §2(94행) `verify ↛ solve` 경계 규칙과 정면 충돌한다.
근거 — 실제 ArchUnit 룰(`solver-core/src/test/java/com/ronext/rpdptw/ArchitectureRulesTest.java`)은 `verify..` → `solve..` **패키지 전체** 참조를 차단한다. stage-05:109는 이를 명시적으로 해소했다: "`Solution`은 `solve` 소유 타입이라 verify가 받을 수 없다 (ArchUnit `verify ↛ solve`)" — `Solution → (routes 맵, bank)` 분해는 호출자(Stage 6 executor)의 일이다(stage-05:140). 오독 시나리오: Architecture(상위)를 따라 `SolutionVerifier.verify(Solution s, …)`로 설계.
증상 — verify가 `Solution`을 import하는 순간 `VERIFY_MUST_NOT_DEPEND_ON_SOLVE`로 BUILD FAILURE. Stage 5 착수자가 상위 문서와 하위 계약 중 어느 쪽이 맞는지 되물어야 한다.
권고 — 문구 분리: "최종 해는 호출자가 (routes, bank)로 분해해 넘긴다 — verify는 `Solution` 타입을 모른다 (stage-05 §2)"로 개정.

### [D5] docs/implementation-plan.md:508-512
문제 — §2.1 말미 문단이 차량별 `trips`를 "확정됐다 … 구현 반영은 Stage 1(§2.3·§4 절차 5·E19·T13)·Stage 6(§4.4·§4.6)이다"라고 현재 사실로 서술하는데, 2026-08-11 Stage Extra 신설로 그 반영이 **되돌려졌다**. 같은 문서 안에서 155-156행("차량별 trips는 wire에 없어 유예"), 464행(E1 등재), 545-548행("만들지 않는다")과 모순.
근거 — stage-01:31 "뺀 것: 차량별 `trips`(§2.3·§4 절차5·E19b~E19e·T13)", stage-01:370 "trips는 전체 설정 하나다 (차량별 지정은 유예 — Stage Extra E1)". stage-06:34 "2026-08-11 Stage 1 유예 반영 — §4.4 adapter 매핑에서 차량 `trips` … 제거", stage-06:485 "`trips` | — | **무시한다**". Domain §2.5(domain-design.md:257) "전체 설정 하나다 — 차량별 지정은 wire에 없어 유예". 즉 이 문단이 가리키는 "구현 반영" 내용(Stage 1 §2.3 차량별 필드·절차 5 차량별 접기·Stage 6 §4.4 읽기 행)은 더 이상 실재하지 않는다(D4 겸함).
증상 — §2.1(결정 로그)만 읽은 구현자가 차량별 `trips` 필드와 우선순위 접기를 Stage 1에 다시 만든다 — 이 감사 체계가 D3 대표 사례로 든 "wire에 없는 필드 선제 구현" 위반의 재발. Master §6(112행) 원칙과도 충돌.
권고 — 문단 개정: "의미(차량 값 우선)는 확정돼 Stage Extra E1에 기록했고, 구현은 유예됐다(2026-08-11)"로 교체. Stage 1·6 반영 문장은 삭제.

### [D4] docs/implementation-plan.md:362
문제 — Stage 6 그림이 "POST /solves → **형식 검증** → 저장 → 200 + solveKey"라고 쓰는데, 2026-08-11 접수 깊이 확정으로 접수 검증은 "파싱 + **정규화 검증**까지 (의미 오류도 4xx, 저장 없음)"로 개정됐다. "형식 수준만" 문면은 명시적으로 폐기된 옛 문면이다.
근거 — architecture-design.md:15-16 "2026-08-11 접수 깊이 확정 — … Domain §12와 충돌하던 '형식 수준만 검증' 문면 폐기", :188-191. stage-06:29·301·555(N1) "접수는 파싱 + 정규화 검증까지 (§10 Q3 해소)". Domain §12(959행)는 `close == open`·소수 거리를 접수 4xx(S3 저장 없음)로 분류. 참고: master-design.md:39도 같은 옛 문면("형식 검증")을 유지 중 — 분할 1·B 소관으로 기록만 한다.
증상 — Plan만 보고 Stage 6을 구현하면 의미 오류 입력이 200 + solveKey로 접수돼 S3에 저장되고 비동기 FAILED로 끝난다 — Domain §12("접수 4xx 시 S3에 아무것도 남기지 않음") 위반. stage-06 T4·T8(a)가 잡아 재작업.
권고 — 그림 문구를 "형식·정규화 검증"으로 교정 (한 단어 수정).

### [D1] docs/architecture-design.md:180
문제 — §3.1 절차 2 "multiRotation이 `{0,1}` 밖 등 미지원 → 4xx `UNSUPPORTED_INPUT`" — "{0,1} 밖"에는 `≤ -2`가 포함되는데 그 구간은 `UNSUPPORTED_INPUT`이 아니라 `INVALID_INPUT`이다. 두 오류 분류가 한 문장에 뭉개졌다.
근거 — Domain §2.5(domain-design.md:258·280-282): `-1`·`2` 이상 = `UNSUPPORTED_INPUT`, `-2` 이하 = `INVALID_INPUT`(§12 입력 오류). stage-06:25 접수 게이트도 "통과 = {0,1}, `-1`·`2` 이상 = 422, `≤ -2` = 400"으로 구분. 같은 저장소의 Plan:151-152도 구분을 명시한다. 오독 시나리오: Architecture만 보고 게이트를 `if (!in {0,1}) return 422` 한 줄로 구현.
증상 — `multiRotation: -3` 입력이 400(INVALID) 대신 422(UNSUPPORTED)로 응답 — Domain §2.5 MUST 위반, stage-06 E1c 테스트 실패로 재작업.
권고 — 문구 분리: "`-1`·`2` 이상 → UNSUPPORTED_INPUT, `≤ -2` → INVALID_INPUT (Domain §2.5)"로 교체.

### [D1] docs/implementation-plan.md:149-150
문제 — Stage 1 DoD "무게·부피 **등**은 버림(FLOOR)·정수 규칙을 따르고, 소수를 조용히 반올림하지 않는다. optional 필드가 없으면 '그 축 제약 없음'이다." — ① 소수를 **거부**해야 하는 차원(거리·시간)이 문장에 없고 "등"이 FLOOR 적용 범위를 열어 둔다. ② "optional 부재 = 제약 없음"은 전 optional에 참이 아니다 — 부재가 **기본값 사용**인 필드가 있다.
근거 — Domain §3.1(362-363·374행): 무게·부피는 ×1000 FLOOR **수용**, "소수 거리·시간 입력은 거부한다 (2026-08-11 재확인 — FLOOR 수용으로 완화하지 않는다)". 기본값 계열: speed 부재 → defaultSpeed → 45(domain:495), trips 부재 → oneway, multiRotation 0 → 1 취급(§2.5). "제약 없음"이 맞는 것은 전역 한도·용량 축(§2.6, domain:345)뿐. doc-audit §5-1·§5-3이 정확히 이 두 지점을 반복 유출부로 지목한다. 오독 시나리오: "등"을 따라 소수 거리도 FLOOR 수용.
증상 — 소수 거리 입력(원본 `win_poc_case.json`은 205,209건 중 204,756건이 소수)이 거부 대신 조용히 버림돼 접수된다 — Domain §3.1 MUST 위반. floor fixture는 정수라 안 잡히고, stage-01 테스트 표까지 가야 드러난다.
권고 — 문구 분리: "무게·부피는 소수를 받아 FLOOR, **거리·시간 소수는 거부**. optional 부재는 용량 축이면 제약 없음, speed·trips·multiRotation이면 기본값 (Domain §2.4·§2.6·§3.1)".

### [D5] docs/architecture-design.md:289-297 (§7)
문제 — §7 "현재 코드와의 차이 (마이그레이션 메모)" 표가 Stage 0 완료(2026-08-10) **이전** 상태(단일 pom·`com.ronext.optimizer`·수제 HTTP 서버·GCP 의존·`gcp/`·`.serverless/`·`AlnsBatchEngine`·구 README)를 "현재 (placeholder)"로 서술한다. 정리는 이미 끝났다.
근거 — 루트 `ls`: `app`·`solver-core`·`solver-profile`·`pom.xml`만 있고 `src/`·`gcp/`·`.serverless/` 없음. git log: `0e9b181 docs: Stage 0 완료 상태에 맞게 CLAUDE.md 갱신`. CLAUDE.md도 "구 placeholder … 삭제됐고"로 확정. Architecture는 08-11까지 개정됐지만 §7은 남았다.
증상 — 읽는 사람이 GCP 의존·AlnsBatchEngine을 찾으러 가거나 Stage 0 정리를 다시 하려 든다. "옛 경로가 보이면 지금은 존재하지 않는 것"이라는 함정 주의가 필요해진 원인 지점.
권고 — 표를 완료형으로 개정("Stage 0에서 정리 완료 — 2026-08-10")하거나 §7을 삭제하고 stage-00 인벤토리로 포인터만 남김.

### [D1] docs/implementation-plan.md:341
문제 — Stage 5 DoD "ArchUnit: 재검증이 탐색(`solve`의 **ALNS 쪽**)을 참조하지 않는다" — 실제 룰은 `solve` 패키지 **전체** 차단인데 "ALNS 쪽"이라는 축소 서술이 비-ALNS 타입(`Solution`·`RoutePropagator`) 참조는 허용되는 것처럼 읽힌다.
근거 — `ArchitectureRulesTest.java`: `noClasses().that().resideInAPackage("com.ronext.rpdptw.verify..").should().dependOnClassesThat().resideInAPackage("com.ronext.rpdptw.solve..")` — 하위 구분 없음. stage-05:109·389는 `Solution`·`RoutePropagator` import 즉시 빌드가 깨진다고 명시. 결함 #1(architecture:111-112)과 같은 방향의 오독을 강화한다.
증상 — verify API 설계 단계에서 `Solution` 파라미터를 허용으로 오판 → BUILD FAILURE 후 재설계.
권고 — 괄호 삭제: "`solve` 패키지를 참조하지 않는다"로 교정.

### [D1] docs/architecture-design.md:81 (·198)
문제 — §2 트리 주석 "input/ — 규약 JSON **↔ canonical** adapter", §3.2 "adapter → canonical" — adapter의 산출물은 canonical이 아니라 Stage 1 **raw 운반체**(`PlanInput`)이고, canonical 확정(단위·의미)은 solver-core 정규화의 일이다.
근거 — Plan:368 "adapter: wire → Stage 1 raw 운반체", Plan:386-387 "의미·단위 규칙의 정본은 Stage 1 정규화다. adapter가 추측으로 의미를 만들지 않는다". stage-06:387·§4.4 동일. 오독 시나리오: app adapter에서 kg×1000·시각 원점 변환까지 수행 — 의미 규칙이 app으로 샌다("의미 규칙은 solver-core 소유" 위반).
증상 — 단위 변환이 두 곳(app·core)에 생기거나 core 정규화를 우회한다. 접수 검증과 executor 재정규화의 "같은 코드 경로" 원칙(stage-06 N1)이 무너진다.
권고 — 문구 분리: "규약 JSON ↔ raw 운반체 (canonical 확정은 core 정규화)"로 교정.

### [D1] docs/architecture-design.md:230
문제 — "GET /solves/{solveKey}/result → result.json (DONE 아니면 **404/409**)" — 어느 경우가 404이고 어느 경우가 409인지 없다.
근거 — stage-06:268-279: solve 미존재 → 404, DONE 전 result → 409. Architecture만 보면 "DONE 전 = 404"로도 읽힌다.
증상 — 호출 시스템이 404를 "solveKey 오타"로, 409를 "아직 진행 중"으로 구분해 쓰려 할 때 구현이 갈린다. wire 협의(D2) 전이라도 내부 구현·테스트가 재작업된다.
권고 — 문구 분리: "미존재 404, DONE 전 409 (stage-06 §3.4)".

### [D4] docs/architecture-design.md:98
문제 — profile이 읽을 수 있는 문제 사실의 예시가 "차급·구역·**치수**"인데, 치수 축은 2026-08-11 유예돼 canonical·`Problem`에 존재하지 않는다.
근거 — domain-design.md:169 "(2026-08-11 유예 — §2.4 유예 표 · Stage Extra E2)", stage-01:127 "치수 3필드 … wire에 없거나 소비처가 없어서", stage-extra E2. 같은 예시 나열이 domain-design.md:523에도 있으나 그쪽은 분할 2~3 소관.
증상 — profile 구현자가 치수 필드를 `Problem`에서 찾다가 없음을 확인하러 되돌아온다.
권고 — 예시에서 "치수" 삭제 또는 "(치수는 E2 승격 후)" 주석.

### [D2] docs/architecture-design.md:155-156
문제 — "고정 가중치·Big-M으로 축을 한 숫자에 뭉개는 구현은 **만들 수 없다** (Domain §8.3이 구조로 강제)" — 전칭 주장이 과잉이다. 구조가 막는 것은 비교기 교체(비교기 SPI 없음)뿐이고, `score()` 구현이 내부에서 가중합 한 축(`long[1]`)을 반환하는 것은 여전히 컴파일·실행 가능하다.
근거 — 같은 파일 §2.2 시그니처: `long[] score(...)` — 길이·내용은 profile 재량. Domain §8.3(802행)은 "비교는 사전식 하나뿐 (MUST)" — 규범이지 타입 강제가 아니다. 축 뭉개기 금지는 리뷰로 지키는 규칙이다(경계표 109행의 "코드 리뷰"와 같은 층).
증상 — "구조가 막아 준다"고 믿고 profile 리뷰에서 Big-M 검사를 생략한다.
권고 — 문구 교정: "비교기는 교체 불가(구조) — 축을 뭉개지 않는 것은 §8.3 MUST(리뷰 대상)"로 분리.

### [D1] docs/implementation-plan.md:503 (vs :495)
문제 — "남은 미결은 D2·D3 둘뿐이고, **어느 Stage도 막고 있지 않다**" — 여덟 줄 위 D3 행의 "막고 있는 것" 열은 "**Stage 7 전체**"다. 같은 절 안에서 문면이 모순된다.
근거 — :495 D3 행 "| … | Stage 7 전체 (환경당 1회 준비 — Stage 7 §4) |" vs :503. 의도는 "지금 당장 진행(1~6)을 막는 것은 없다"이나 문장은 전칭이다.
증상 — 읽는 사람이 D3 행과 이 문장을 오가며 어느 쪽이 맞는지 되묻는다.
권고 — 문구 교정: "지금 착수를 막는 것은 없다 — D3은 Stage 7 도달 시점의 준비물이다".

### [D4] docs/implementation-plan.md:474
문제 — "트리거가 발동하면 **§5 승격 절차**를 거쳐" — 어느 문서의 §5인지 없다. 이 Plan에는 §5가 없다(§0~§3뿐).
근거 — 실제 대상은 stage-extra-deferred-features.md §5(153행 "## 5. 승격 절차")다. Plan 안에서 "§N"은 관행상 자기 문서를 가리켜 왔다(§0·§1·§2.1 등).
증상 — Plan에서 §5를 찾다가 실패하고 추측으로 stage-extra를 연다.
권고 — 문구 교정: "Stage Extra 문서 §5 승격 절차".

### [D5] docs/implementation-plan.md:86-119 (Stage 0 절)
문제 — Stage 0은 2026-08-10 완료됐는데 Plan 어디에도 완료 표시가 없다 — §2.1은 D1·D4 완료를 기록하면서 Stage 진척은 기록하지 않아, 이 문서만 보면 작업 시작점이 Stage 0인지 1인지 보이지 않는다.
근거 — git `0e9b181 docs: Stage 0 완료 상태에 맞게 CLAUDE.md 갱신`, 루트 `ls`(3모듈 뼈대 실재), CLAUDE.md "Stage 0 완료 (2026-08-10) … 다음 작업은 Stage 1". Plan 본문·순서 그림(:517)에는 표시 없음.
증상 — Plan만 받은 사람이 Stage 0부터 착수하거나, 완료 여부를 코드로 재확인한다.
권고 — Stage 0 절 머리에 "**완료 (2026-08-10)**" 한 줄 추가 (D1·D4 행과 같은 표기).

---

## 2. 기계 검사 잔여 판정 로그 (결함 아님으로 닫은 항목의 근거)

- **D1 연쇄 해제 주장(:493)** "Stage 6 T13·Stage 7 V6·Stage 8 P1의 선행 조건이 전부 해제됐다" — stage-06:617(T13 "실행 가능하다")·stage-07:292(V6 "선행 조건 해제됨")·stage-08:55(P1 "해소") 전부 실재 일치.
- **"반영된 문서" 목록(:493)** Master §4(:87-88)·§6(:109-115), Domain §2.5(:258), Architecture §3.1(:13-14), Stage 0(:20-21·817)·1(:19)·2(:16·311)·6(:24-27)·7(:17·318)·8(:23-25) 전부 D1 반영 흔적 실재.
- **시그니처 일치** `HardConstraint.satisfied(Problem, RouteFacts)`·`Profile.score(Problem, Evaluation, Collection<RouteFacts>)`·`ProfileRegistry.resolve(Optional<String>)` = stage-03:469-500 동일. `RouteFacts`는 eval 소유(stage-03:90) — Architecture §2.2 인터페이스가 참조 가능.
- **저장 계약 일치** `SolveStore` 6메서드 = stage-06:169-175 ("Architecture §3.5 그대로" 명시). status.json 필드·STALE 비저장·solveKey 조립 규칙 한 곳(SolveKey.java) = stage-06:94·153-163.
- **규모 DoD 숫자 일치** Plan Stage 2(:193-196)·Stage 4(:293) = stage-02 T12·stage-04 T11 (장소 453·주문 452·차량 31·453² 전 쌍, 실물 JSON 미사용).
- **Boot 4.1 근거 참조(:254-255·:96)** stage-00 §4.4 범위(349~450행) 안 441-447행에 라인 비교표(3.5 EOL 2026-06-30·4.1.0 채택·Java 17-26·OSS 2027-07-31) 실재 — 두 문서 수치 상호 일치 (외부 사실 자체는 이 감사 범위에서 미검증).
- **경계표(:108)** "AlnsConfig가 solve에 있으므로" — stage-04:88 `solve/AlnsConfig.java` 실재.
- **ceil 공식(:175)** `ceil(D×3.6/speed)` = domain:495 동일.
- **trips 접기 DoD(:155)** roundtrip → endDepot 부재 시 startDepot 복귀 = domain:257·stage-01 E19(:494) 동일. fixture `options.trips = "oneway"` (실측).
- **"6a storage는 core 타입을 하나도 쓰지 않는다"(:534)** — SolveStore 시그니처가 byte[]·SolveKey·SolveStatus(전부 app 소유)만 사용, stage-06:169-175로 확인.
- **frontmatter supersedes 대상** `docs/deprecated/2026-07-31-phase-b-architecture-design.md`·`docs/deprecated/implementation-15phase/` 실재.
- **"DoD 밖" 편입 주장(:55-57)** — `grep "DoD 밖" docs/implementation/*.md` 0건: stage 문서들에 옛 표기가 남아 있지 않아 과거형 서술과 정합.

## 3. 심각도 요약

| 심각도 | 건수 | 대표 |
|---|---|---|
| 치명 (구현이 틀림) | 0 | — |
| 중 (재작업 유발) | 6 | architecture:111-112 (verify가 `Solution`을 받는다는 문면 — ArchUnit·stage-05와 모순) |
| 경 (읽기 불편) | 8 | plan:341 ("solve의 ALNS 쪽" — 실제 룰은 solve 전체 차단) |

치명 0의 판정 근거: 상위 두 문서의 오류 문면 5곳(#1·#3·#4·#5·#8)은 전부 Domain(더 상위) 또는
stage 문서의 테스트 표(구속력 있는 구현 계약, Plan §1이 완료 기준으로 편입)가 올바른 규칙을
갖고 있어, 그대로 구현해도 빌드·테스트 단계에서 잡혀 재작업으로 끝난다 — fixture가 안 돌게
되는 경로는 없다 (fixture 주장 12항목 전수 실측 일치, §0.3).
