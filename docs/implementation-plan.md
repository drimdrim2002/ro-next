---
title: RO-Next Implementation Plan
status: CONFIRMED
date: 2026-08-10
supersedes: docs/deprecated/implementation-15phase/ (ARCHIVED)
master: docs/master-design.md
revisions:
  - 2026-08-09 최초 확정
  - 2026-08-10 Spring Boot 4.1 · Stage 0 코드 정리에 구 Dockerfile 삭제 명시
  - 2026-08-10 **D4 완료** — 다일 근무창 + 차고 시간창 설계 확정. §2.1 D4 행을 확정 내용으로
    갱신하고, Stage 3 선행 게이트 ②와 [Domain 해석 확정] 칸을 해제. 정본은 Domain §3.2·§7.1·§7.3
  - 2026-08-10 순서 개정 — §2에 "지금 시작하는 결정" 표(D1~D4) 신설, 순서 그림에
    [fixture 실측]·[Domain 해석 확정]·5-재검증/5-결과 분리 반영(구 "adapter·storage는 3 이후"
    문장은 사실 오류라 교체), Stage 3 선행 게이트·Stage 5 순서 근거·Stage 2·4 규모 DoD 추가
---

# RO-Next Implementation Plan

확정된 설계([Master](master-design.md) · [Domain](domain-design.md) · [Architecture](architecture-design.md))를
코드로 만드는 단계 계획이다. 이전 15-phase 문서 세트(약 27,000줄)는 폐기된 구 설계 기준이라
아카이브했고, 이 문서 하나가 그것을 대체한다.

원칙: 단계마다 **동작하는 테스트**가 완료 기준이다. 문서 작성·클래스 존재는 완료가 아니다.
각 단계의 세부 작업 분해는 구현 세션 재량이며, 이 문서는 순서·경계·완료 기준만 고정한다.

## 0. 최종 성공 기준 (변하지 않는 목표)

> [data/win_poc_case_floor.json](../data/win_poc_case_floor.json) (주문 452건·차량 31대)을
> 앱에 접수 → 실제 ALNS로 풀이 → **재검증 통과** → 결과 JSON 생성.
> 이후 같은 입력의 기존 엔진(Win) 결과와 지표(미배정 수·차량 수·총거리) 비교.

## 1. 단계

### Stage 0 — 정리와 뼈대

| 작업 | 내용 |
|---|---|
| 코드 정리 | GCP 의존성 제거(pom), 구 `Dockerfile` 삭제(단일 모듈·`src/` shade 전용 — Stage 0 이후 빌드 불가), `gcp/`·`.serverless/`·빈 모듈 잔재(`rpdptw/`·`adapters/`·`apps/`·`build/` 디렉터리)·구 `src/`(placeholder) 삭제. 새 Dockerfile은 Stage 7 |
| 뼈대 | parent pom + `solver-core`(의존 0) + `solver-profile`(고객 정책) + `app`(**Spring Boot 4.1**, starter-web) **3모듈** 구성 (Architecture §2). Boot 3.5는 2026-06-30 OSS EOL — 라인 선택 근거는 [Stage 0 §4.4](implementation/stage-00-cleanup-and-skeleton.md) |
| 경계 테스트 | ArchUnit: `verify.. ↛ solve..` 규칙 (빈 패키지 상태로도 룰 파일 먼저) |
| README | 루트 README를 확정 설계에 맞게 갱신 |

**DoD**: `mvn verify` 통과. `app` 기동 후 health 응답. GCP 의존성 0.

### Stage 1 — canonical 입력과 정규화 (solver-core)

Domain §1–§3. canonical 모델(`Request`·`Vehicle`·`Plan`), 단위 정규화(kg×1000 FLOOR 등),
시간 원점(planStart 기준 초), serviceTime 공식, 호환성 판정, 입력 오류·`UNSUPPORTED_INPUT` 분류.

**DoD**: 단위·경계값(FLOOR, 소수 거부, optional 부재 = 제약 없음) 단위 테스트.
`multiRotation`이 core 지원 범위를 넘으면 거부하는 테스트 (현행 판정 `!= 0` — Domain §2.5).

### Stage 2 — 이동표와 Problem 동결 (solver-core)

Domain §4–§5. `LocationId` 기반 이동표(D/U, 누락 보정: Great Circle·`ceil(D×3.6/speed)`),
`Problem` 생성 시 참조·완전성 검증과 동결.

**DoD**: 이동표 보정 규칙 테스트. Problem 생성 후 불변성(문제 쪽 mutator 부재) 확인.
**규모**: 실물과 같은 규모의 합성 입력(장소 453·주문 452·차량 31, **이동표는 453² = 205,209쌍
전부가 주어진 상태**)으로 `Problem` 생성 1회 — 소요 시간·메모리를 기록한다. 쌍이 비어 있으면
Great Circle 보정이 대신 채워 측정값의 의미가 사라지므로 전 쌍을 채운다.
실물 JSON을 읽지 않는다 (app 모듈 불필요) — 입력은 프로그램으로 조립한다.

### Stage 3 — Solution·전파·평가 (solver-core)

**선행 (게이트 2개 — 둘 다 해제됨)**: ① 시간창 close 판정 기준 — **확정됨** (Domain §7.1, 2026-08-10).
② 차고 시간창과 다일 근무창 — **확정됨** (§2.1 D4 완료, 2026-08-10 — Domain §3.2 시간창 전개 +
§7.1 출발·방문·종료 절차). Stage 3(탐색)과 Stage 5(재검증)가 같은 규칙을 각각 구현하므로,
해석이 갈리면 두 코드의 숫자가 어긋나고 원인이 버그가 아니라 해석 차이가 된다 — 이제 둘 다
Domain의 같은 문장을 보고 구현한다.

Domain §6–§8. 경로/bank 상태와 XOR, 적재 부호 규칙(initialLoad 포함), 전파 절차
(출발(근무창 ∩ 차고 창)→arrival→대기→서비스→`reqDate`→load→hard→다음 창으로 미루기→종료),
기록 값(§7.3 — `departure`·대기 3종·항등식), metric(`Evaluation`),
profile의 score 축(`long[]`)과 사전식 비교(`Scores.compare`), `DefaultProfile`(core `eval`) +
`ProfileRegistry`(solver-profile 모듈).

**DoD**: Domain §7.2 숫자 예를 그대로 재현하는 테스트. XOR 위반·hard 위반 검출 테스트.
미등록 customerId → default profile 테스트.

### Stage 4 — 초기해와 ALNS (solver-core)

Domain §9. 초기해 생성(greedy 삽입 등 재량), destroy/repair(pair 단위), acceptance,
시간 한도 종료. 삽입 shortlist 근사는 재량 (수락은 정식 평가만).

**DoD**: 소형 fixture에서 초기해 대비 개선 확인. pair·XOR 불변식이 탐색 중 유지되는
property 테스트 (예: 랜덤 스텝 N회 후 구조 검사).
**규모**: Stage 2와 같은 합성 문제로 ALNS 1회 — 시간 한도 안에 몇 번 반복했는지 기록한다.
여기서도 실물 JSON을 읽지 않는다. 실물 fixture 전 구간 실행은 Stage 6 그대로.

### Stage 5 — 재검증과 결과 (solver-core)

**순서**: 재검증(`SolutionVerifier`·`RouteReplay`)은 Stage 3만 있으면 만들 수 있고 **Stage 4보다
먼저 만든다** — `SolutionVerifier.verify`의 시그니처에 Stage 4 타입이 하나도 없다. 이유 둘:
(a) ALNS를 먼저 만들면 재검증을 그 코드를 보며 쓰게 되어 같은 실수가 복사된다 — 따로 만드는
의미가 사라진다. (b) ALNS를 개발하는 동안 **독립된 검사 도구**가 이미 손에 있다. Stage 4 자체
검사는 연산자와 `StructureCheck`를 공유하므로 그 코드의 버그를 같이 못 본다.
결과 모델·미배정 사유는 Stage 4 뒤에 만든다 — 이것은 **의존이 아니라 집중을 위한 선택**이다.
`ResultAssembler`도 기술적으로는 Stage 4 없이 만들 수 있다 (`Pass` + `RunStamp`만 쓴다).
Stage 5 문서는 쪼개지 않는다 — 이미 §1~§3이 재검증, §4가 결과 모델이라 읽는 순서만 바뀐다.

Domain §10–§11. `verify` 패키지의 독립 재검증(전체 해, 캐시 없이), 결과 모델
(routes/unassigned+reason/metrics/run 메타), 검증된 해 ↔ 결과 일치 테스트.

**DoD**: 일부러 오염시킨 해(짝 분리·용량 초과·점수 불일치)가 전부 FAIL. ArchUnit 규칙 통과.

### Stage 6 — 앱 조립 (app)

Architecture §3. 규약 JSON adapter(win_poc fixture로 검증), `SolveStore`(fake + S3 구현),
접수 API(검증→저장→200+solveKey), `SolveExecutor`(상태 전이·heartbeat·STALE), 조회 API.

**DoD**: fake 저장소로 e2e 통합 테스트 — POST 접수 → DONE까지 → GET 결과.
win_poc_case_floor.json 접수·완주 (**성공 기준 §0 달성 시점**).

### Stage 7 — ECS 배포

Architecture §5. Dockerfile(app jar), ECS Fargate 서비스·태스크 롤(S3), 환경변수 설정,
CloudWatch 로그. 선택: 배포 전 LocalStack e2e 1회.

**DoD**: 배포 환경에서 실제 S3로 §0 시나리오 1회 성공.

### Stage 8 — 벤치마크 비교 (마무리)

win_poc 입력의 Win 결과와 지표 비교(미배정·차량 수·거리·시간), 필요한 만큼 탐색 파라미터 조정.
비교 절차와 수치는 이 단계에서 기록한다 (사전 확정하지 않음).

## 2. 지금 시작하는 결정과 순서

### 2.1 지금 시작하는 결정 (코드 0줄)

Stage 순서와 별개로 **지금 착수할 수 있고, 늦어지면 뒤 Stage를 막는** 결정들이다.
여기 적히기 전에는 이 항목들이 Stage 문서의 "미해결 질문" 사이를 떠돌기만 하고
착수 시점이 없었다. 각 항목의 정본은 이 표이고, Stage 문서는 여기를 가리키기만 한다.

| # | 결정 | 지금 아는 것 / 남은 일 | 막고 있는 것 |
|---|---|---|---|
| **D1** | **`multiRotation` 값 의미와 범위** | **값의 의미는 사실상 해소됐다** — 규약 PDF 4페이지 열거 정의가 이 숫자를 **차고 복귀 횟수**로 정의한다: `-1` = 무제한 복귀, `0` = 복귀 없음(기본값), `1 이상` = 지정한 횟수만큼 복귀. 따라서 fixture의 `"1"`은 "복귀 1회 = 최대 2적재"이고 **현재 지원 범위 밖**이다 (Domain §2.5 = 경로당 trip 1개). 반증 하나는 기록해 둔다: 같은 PDF의 `multirotation 2` 예시 그림이 복귀를 1회만 그린다 — 열거 정의 쪽이 권위이고 그림은 규약의 부주의로 판단한다.<br>**남은 것은 해석이 아니라 범위 결정이다.** 두 갈래를 나눠 본다 — **경로 B (fixture 교정)**: `data/win_poc_case_floor.json`의 값을 `"0"`으로 고친다. 외부 의존 0이고, 닫는 즉시 개발·로컬 실행이 열린다. `scripts/floor_win_poc_matrix.py`는 `options`를 건드리지 않으므로(D·U만 FLOOR) 스크립트 수정은 필요 없다. 교정 사실은 기록해 원본과의 차이를 추적한다. **경로 A (호출 시스템 확인)**: `"1"`은 기본값이 아니라 누가 **의도해서 넣은 값**이고 Stage 0 §11 Q3에 2회전 고객이 실재한다는 기록이 있다 — 이 케이스가 정말 2적재로 풀린 것인지 확인해야 벤치마크 비교가 무엇을 비교하는지 말할 수 있다 (Stage 8 §6 W1).<br>**교정해도 벤치마크는 성립한다** (2026-08-10 대조): `data/alns_result.csv`의 Win 참조 해를 차량별로 합산한 결과 31대 전부 자기 용량 안이었다 — 무게 초과 0대·부피 초과 0대(부피 사용률 81~100%, 31대 중 27대가 93% 이상). Win의 답이 **이미 전 차량 단일 적재**라 W1의 위험은 낮다 | Stage 6 e2e·Stage 7 DoD·Stage 8 전체 (§0 fixture가 접수에서 거부된다) |
| **D2** | **wire 협의** | 결과 JSON 필드명·시각 표기·단위 표현 · 조회 경로와 HTTP 상태 · `customerId`의 wire 원천(`shprId` vs 주문 수준 `customerId`) · legacy 시간 필드(주문 수준 `taskTime`·`driverRestTimeRatio`)의 처리 · `item.taskTime × qty` 해석(규약 PDF 문면이 Domain §3.3과 반대로 적혀 있다). **첫 작업은 협의 상대를 찾는 것이다** — Stage 6 §10 Q4가 "협의 상대가 현재 없음"으로 멈춰 있다. 의미(Domain §11.1)는 협의 대상이 아니고 이름·형태만 정한다 | Stage 6 result.json wire 확정 (잠정안으로 구현은 가능하나, 협의 후 바뀌면 golden 테스트를 다시 쓴다) |
| **D3** | **AWS 사전 준비** | 배포 계정·리전 · 버킷 이름(`ro-next-solves-{env}`의 `{env}`) · 태스크 롤·실행 롤 · ECR 리포지터리 · ECS 클러스터 · 로그 그룹, 그리고 **엔드포인트 노출 방식**(ALB인지 내부 엔드포인트인지 — 호출 시스템의 네트워크 위치에 달렸다). 전부 Architecture §5가 "배포 시 결정"으로 열어 둔 것이고 코드와 무관하다 | Stage 7 전체 (환경당 1회 준비 — Stage 7 §4) |
| **D4** | **다일 근무창 + 차고 시간창 설계** — **완료 (2026-08-10 확정)** | 문제였던 것: 현행 모델이 다일 계획의 근무 시간대를 표현하지 못했고(`Vehicle.workWindow` 하나·시작일 고정), 차고의 `openTime`/`closeTime`이 전파에서 아예 읽히지 않았다. 둘 다 전파 절차를 바꾸므로 한 설계로 묶었다.<br>**확정 내용** — 정본은 **Domain §3.2(시간창 전개)·§7.1(출발·방문·종료 절차)·§7.3(대기 3종·항등식)**이다: ① **canonical 표현** = 정규화가 **날마다 반복되는 창을 절대 창 목록으로 펼친다** (`List<TimeWindow>` — 근무창·차고 창·**주문 시간창** 셋 다. 규약이 세 창을 전부 날짜 없는 partial-time으로 주므로 반복 말고 다른 해석이 없다) ② **정규화** = 전날부터 planEnd 날짜까지 생성 → `close < open`은 자정 넘김으로 **수용**(`close == open`은 신규 INVALID_INPUT) → `[0, planEndSec − 1]`로 **클리핑**(계획 기간을 넘겨 끝나는 경로가 사라지는 **새 경계**) → 정렬·인접 병합 ③ **전파** = 창 끝을 넘는 이동·서비스는 **다음 창으로 미룬다**(불가 판정이 아니다). 남은 창이 없을 때만 `WORK_WINDOW` ④ **차고 창** = 출발은 "근무창 ∩ startDepot 창 안"(다일이라 상한이 생긴다), 복귀는 "어느 endDepot 창 안"이며 **미루지 않는다** → `DEPOT_WINDOW` ⑤ **`interWorkWindowRestTime`** = 경로 시간 중 근무창 사이의 틈에 있는 시간 전부. 항등식 `routeOperationalTime = routeEnd − spanStart` ⑥ 모든 소요 시간은 **두 시각의 차**로 잰다 (초를 세면 창마다 1초 어긋난다).<br>**현행 fixture(1일)에서는 값이 하나도 바뀌지 않는다** — 근거는 Stage 1 §4 말미의 실측 표 | (해제됨) Stage 3·Stage 5가 이제 Domain의 같은 문장을 보고 구현한다. 반영된 문서: Domain · Stage 1·3·4·5·6·8 |

**D4는 D1~D3과 성격이 달랐다.** D1~D3은 **바깥의 답을 기다리는** 항목이라 우리가 할 일은 묻고
기록하는 것이다. D4는 **우리가 하는 설계 작업**이었고, **2026-08-10에 끝났다** — Domain §3.2·§7.1이
개정됐고(§7.2.1·§7.3·§13 포함) Stage 1·3·4·5·6·8이 따라 갱신됐다. **이제 Stage 3을 시작할 수 있다.**
남은 미결은 D1~D3이며, 그중 Stage 3을 막는 것은 없다.

### 2.2 순서와 병행

```text
0 → [fixture 실측] → 1 → 2 → [Domain 해석 확정] → 3 → 5-재검증 → 4 → 5-결과
  → 6 → 7 → 8
```

솔버(1–5)가 중심이고 앱(6)은 얇다. 한 Stage를 끝내고(테스트 green) 다음으로 간다.
대괄호 칸은 **코드를 쓰지 않는 단계**다 — 재고 조사와 해석 확정이라 Stage 번호를 주지 않는다.

| 칸 | 무엇 |
|---|---|
| **[fixture 실측]** | 실물 fixture의 크기·값을 **미리 재서** Stage 2·4의 규모 DoD 목표 숫자를 고정한다. 세는 것뿐이라 코드가 없다. 2026-08-10 실측(`data/win_poc_case_floor.json`): 주문 452 · 차량 31 · 차고 1 · 장소 **453** · 이동표 **205,209쌍 = 453² (전 쌍이 입력에 주어짐, 희소하지 않다)** · `multiRotation "1"` · `Termination.secondsSpentLimit "600"` · `Optimizer.VehicleMaxStopCount "28"` · `Optimizer.DefaultSpeed "45"` |
| **[Domain 해석 확정]** | **완료 (2026-08-10).** 전파 규칙의 해석을 Domain에 올려 Stage 3·5가 **같은 문장**을 보고 구현하게 했다. 시간창 close 기준(Domain §7.1)과 차고 창·다일 근무창(**D4** → Domain §3.2·§7.1·§7.3) 둘 다 확정됐다 |
| **5-재검증 / 5-결과** | Stage 5 문서를 쪼개지 않는다. 그 문서는 이미 §1~§3이 재검증, §4가 결과 모델이라 **읽는 순서만** 바뀐다 (Stage 5 절 참고) |

**Stage 6의 병행 가능성** (이전 판의 "6의 adapter·storage는 3 이후 병행 가능"은 사실이 틀렸다):

- **6a `storage`** (`SolveKey`·`SolveStore`·`S3SolveStore`·`LocalSolveStore`)는 solver-core 타입을
  **하나도 쓰지 않는다** — Stage 0 이후 언제든 만들 수 있다 (Stage 6 §2.1).
- **6b `input` adapter**의 `PlanJsonAdapter`는 **Stage 1**의 `PlanInput`에만 의존한다 — Stage 3이
  아니라 Stage 1 이후면 된다 (Stage 6 §2.2). 같은 패키지의 `ResultJsonWriter`는 Stage 5가 필요하다.
- 그래도 **기본은 Stage 6에서 함께 한다 (core-first)**. 솔버에 집중하기 위한 선택이며,
  위 두 줄은 "당겨서 하자"는 제안이 아니라 **막고 있는 것이 없다는 사실의 기록**이다.
  앞 Stage가 막혔을 때(예: D1 대기) 쓸 수 있는 우회로다.

## 3. 하지 않는 것

- MIP 재조합, SQS·Step Functions, multi-trip — 설계 범위 밖 (Master §4·§6)
- 탐색 파라미터의 사전 확정 — Stage 4·8에서 실험으로
- 15-phase 문서의 부활 — 참고가 필요하면 [아카이브](deprecated/implementation-15phase/README.md)를 읽되, 규칙 충돌 시 현행 설계 3문서가 이긴다
