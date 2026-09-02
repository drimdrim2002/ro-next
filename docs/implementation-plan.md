---
title: RO-Next Implementation Plan
status: CONFIRMED
date: 2026-08-10
supersedes: docs/deprecated/implementation-15phase/ (ARCHIVED)
master: docs/master-design.md
revisions:
  - 2026-09-02 Stage 4 초기해 실물 맞춤 2개(H23·H24) 편입 — 22 → **24개**, 축소 목표 24 → 4
    (heuristics 문서 §5 H23·H24, survey §2.5)
  - 2026-09-02 Stage 4 초기해 포트폴리오 확장 — 문헌·실전 솔버 조사
    ([survey](implementation/stage-04-initial-solution-heuristics-survey.md))로 확장 14개를
    편입해 8개 → **22개(기본 8 + 확장 14)**, 축소 목표 8 → 4 → **22 → 4**.
    Stage 4 절의 개수·DoD 문구만 갱신, 구조 무변경
  - 2026-08-09 최초 확정
  - 2026-08-10 Spring Boot 4.1 · Stage 0 코드 정리에 구 Dockerfile 삭제 명시
  - 2026-08-10 **D4 완료** — 다일 근무창 + 차고 시간창 설계 확정. §2.1 D4 행을 확정 내용으로
    갱신하고, Stage 3 선행 게이트 ②와 [Domain 해석 확정] 칸을 해제. 정본은 Domain §3.2·§7.1·§7.3
  - 2026-08-10 순서 개정 — §2에 "지금 시작하는 결정" 표(D1~D4) 신설, 순서 그림에
    [fixture 실측]·[Domain 해석 확정]·5-재검증/5-결과 분리 반영(구 "adapter·storage는 3 이후"
    문장은 사실 오류라 교체), Stage 3 선행 게이트·Stage 5 순서 근거·Stage 2·4 규모 DoD 추가
  - 2026-08-10 **D1 완료** — `multiRotation` = 바퀴 수로 확정, 판정 반전(통과 = `{0, 1}`).
    §2.1 D1 행을 확정 내용으로 갱신(경로 A·B 소멸 — fixture 교정 불필요, 원본 그대로 접수),
    §2.1 말미 문단과 §2.2 우회로 문장을 "남은 미결 = D2·D3"으로, Stage 1 DoD 판정식 갱신,
    [fixture 실측] 행에 통과 표시. 차량별 `trips` 확정도 같은 문단에 기록. 정본은 Domain §2.5
  - 2026-08-11 검토 반영 — §1 원칙에 DoD 편입 문장 추가(각 stage 문서 테스트 표 전부가 완료
    기준이 된다), D2에 안건 2건 추가(결과 경로 시각의 wire 노출 · 이동표 비대각 D=9999의
    결측 여부 확인)
  - 2026-08-11 **Stage Extra 신설** — Stage 1 오버엔지니어링 검토 결과, 선제 구현된
    add-only 항목 3묶음(차량별 `trips`+`depot.taskTime` · 치수 축 · 차량 소유 구분)을
    유예하고 [stage-extra-deferred-features.md](implementation/stage-extra-deferred-features.md)에
    등재. §0.1 표·§1 말미·§3에 반영하고 Stage 1 DoD에서 차량별 `trips` 문장 삭제.
    D2에 C 결정 3건 종결 기록(소수 거부 유지 · `depot.taskTime` 의미 확정 · `×qty` 확정)
  - 2026-08-11 Stage 설명 보강 — VRPTW 배경·CS 배경 약한 독자용. 읽는 법·개념→Stage 표
    추가, Stage 0–8을 동일 템플릿(배차/시스템 관점·만드는 것·순서·안 하는 것·평문 DoD·
    용어 메모)으로 풀어 씀. 설계·DoD 판정 자체는 불변. Domain 규칙 본문은 복제하지 않음
  - 2026-08-12 Domain 2026-08-12 개정(self arc sentinel·startDepot 부재 규칙·수치 number
    인코딩) 정합 — Stage 1 설명의 "문자열" 표현 정리(문자열 수치 = INVALID_INPUT), D1 칸·
    [fixture 실측] 행의 fixture 수치 인용을 number 표기로. 순서·DoD 판정 무변경
  - 2026-08-12 감사 후속 인터뷰 정합 — D2에서 `driverRestTimeRatio` 종결
    (무시 확정, 0 아닌 값도 거부 안 함 — Domain §2.5)
  - 2026-08-13 전수 감사 기계적 정정 — Stage 0 절에 완료(2026-08-10) 표시 · Stage 5 DoD
    ArchUnit 문면 교정("`solve`의 ALNS 쪽" → `solve` 패키지 전체) · Stage 6 그림 "형식 검증" →
    "형식·정규화 검증"(2026-08-11 접수 깊이 확정 정합) · §2.1 말미 차량별 `trips` 문단 시제
    교정(구현 반영 → 2026-08-11 유예, Stage Extra E1) · §2.1 "어느 Stage도 막고 있지 않다"
    전칭 완화 · Stage Extra 절 "§5 승격 절차"의 소속 문서 명시 · Stage 1 DoD 소수 규칙
    문구 분리(무게·부피 FLOOR / 거리·시간 거부, optional 부재 = 제약 없음·기본값 구분)
  - 2026-08-14 Domain `startDepot` optional + `PICKUP_ONLY` 정합 — Stage 1 DoD에서
    `startDepot`을 "규칙으로 정해진 기본값"에서 제외(부재 = 첫 고객 시작). trips 접기에
    start도 없으면 INVALID 추가. §0 fixture: 직접 정규화 시 start empty, 앱 접수(Stage 6)는
    단일 차고를 채움. 2026-08-12 채움 규칙과 구분
  - 2026-08-17 Domain §4 self arc 원복(sentinel 999,000/86,400 → D=0·U=0) 인지 — 본문 무변경.
    Stage 2 범위·DoD 문장에 대각값이 없고, Stage 3의 E18·E19가 함께 갱신됐다
  - 2026-08-17 Domain §6.2 방문 유일·참조 후속 — Stage 3 그림의 구조 검사 목록만
    `pair·XOR·방문 유일·참조`로 맞춤. 범위·DoD 무변경
  - 2026-09-02 Stage 4 초기해를 **결정적 construction 8개 포트폴리오**로 확정 —
    본문 "만드는 것" 갱신 + DoD 2줄 추가(8개 전부 Feasible·결정성 / 기법별 실측 기록).
    상세는 implementation/stage-04-initial-solution-heuristics.md. 다른 Stage 무변경
  - 2026-09-02 Stage 4를 **4-초기해 / 4-ALNS 두 구현 단계로 분리** (5-재검증/5-결과와 같은
    형식 — 번호는 그대로). §2.2 순서 그림·표 행 추가, Stage 4 절의 '만드는 것'과 DoD를
    단계별로 재작성. 범위·설계 내용 무변경 — 만드는 순서와 완료 판정 시점만 나뉜다
---

# RO-Next Implementation Plan

확정된 설계([Master](master-design.md) · [Domain](domain-design.md) · [Architecture](architecture-design.md))를
코드로 만드는 단계 계획이다. 이전 15-phase 문서 세트(약 27,000줄)는 폐기된 구 설계 기준이라
아카이브했고, 이 문서 하나가 그것을 대체한다.

## 이 문서를 읽는 법

| 문서 | 역할 |
|---|---|
| **이 Plan** | **순서·경계·완료 의미**의 지도. “지금 파이프라인의 어디를 만드는가” |
| [Domain](domain-design.md) | 배차 규칙의 **정본** (시간창·전파·pair·재검증 의미) |
| [Architecture](architecture-design.md) | 모듈·API·S3·배포 배치 |
| [stage-NN 문서](implementation/) | 해당 Stage의 **파일·클래스·시그니처·테스트 표** (구현 직전 계약) |

**권장 읽기**: Plan의 Stage N 본문 → Domain 해당 절 → `implementation/stage-NN-….md`.

**독자 가정**: VRPTW(시간창·경로·적재·ALNS destroy/repair·미배정 등)는 안다고 본다.
모듈·테스트·배포 같은 소프트웨어 용어는 각 Stage **용어 메모**에 이 문서에서의 쓰임만 적는다.
배차 규칙 세부를 이 문서만으로 배우려 하지 않는다 — 그건 Domain이다.

원칙: 단계마다 **동작하는 테스트**가 완료 기준이다. 문서 작성·클래스 존재는 완료가 아니다.
각 단계의 세부 작업 분해는 구현 세션 재량이며, 이 문서는 순서·경계·완료 기준만 고정한다.
각 Stage의 완료 판정은 아래 DoD 문장에 더해 **해당 stage-NN 문서의 테스트 표 전부(green)**를
포함한다 (2026-08-11 편입 — 종전에 stage 문서들이 "DoD 밖"으로 표시하던 테스트가 전부 완료
기준이 됐다. 아래 DoD 문장은 그 표의 요약이다).

## 0. 최종 성공 기준 (변하지 않는 목표)

> [data/win_poc_case_floor.json](../data/win_poc_case_floor.json) (주문 452건·차량 31대)을
> 앱에 접수 → 실제 ALNS로 풀이 → **재검증 통과** → 결과 JSON 생성.
> 이후 같은 입력의 기존 엔진(Win) 결과와 지표(미배정 수·차량 수·총거리) 비교.
> 차량 31대는 wire에 `startDepot`이 없다. **앱 접수(Stage 6 adapter)** 가 단일 차고 `WIN_0`을
> 채우므로 이 기준의 출발은 `WIN_0`이다. `PlanInput`으로 직접 정규화하면 start는 비어 있다
> (Domain §2.4, 2026-08-14 — 정규화는 채우지 않는다).

## 0.1 VRPTW로 보면 — Stage 한 장

이미 아는 배차 개념이 코드 파이프라인의 어디에 붙는지다. 아래 Stage 본문은 이 표를 풀어 쓴다.

| 이미 아는 것 | Stage | 코드에서 고정하는 것 |
|---|---|---|
| (기반) 빌드·패키지 골격 | **0** | 3모듈 뼈대, 경계 검사, 앱 기동 |
| 입력 instance (주문·차량·TW·용량·옵션) | **1** | 정규화된 `Plan` (내부 단위·의미 하나) |
| 거리/시간 행렬 + instance freeze | **2** | 이동표 완비 + 동결 `Problem` |
| 경로 feasibility / 목적함수 비교 | **3** | 전파·`Evaluation`·score 축 |
| 초기해 + metaheuristic 탐색 | **4** | ALNS (연산 단위 = Request/pair) |
| 해의 독립 검증 + 결과 표현 | **5** | 재검증 + 결과 모델 (발행 게이트) |
| 서비스 API·저장 | **6** | 접수/조회 HTTP + 저장소 |
| 클라우드 상시 실행 | **7** | ECS Fargate + 실 S3 |
| 기존 엔진 대비 벤치 | **8** | Win 지표 비교·파라미터 조정 기록 |
| (조건부) 미룬 확장 항목 | **Extra** | 트리거 대기 중인 유예 항목 — 순서상 어디에도 없다 (§1 말미) |

풀이 한 건의 전체 그림(동기 접수 / 비동기 풀이 / 재검증 / 조회)은 [Master §2](master-design.md)다.

## 1. 단계

### Stage 0 — 정리와 뼈대

**완료 (2026-08-10)** — 아래 DoD 전부 충족 (3모듈 뼈대·`mvn verify`·health 응답·GCP 의존 0건).
다음 착수는 Stage 1이다.

**시스템 관점 (한 줄)**  
배차 로직을 쓰기 **전에**, 코드가 들어갈 방 3개와 “빌드·기동·경계 검사” 통로를 만든다.

**이 단계에서 만드는 것**

| 작업 | 내용 |
|---|---|
| 코드 정리 | 구 GCP·placeholder 잔재를 지운다. GCP 의존성 제거(pom), 구 `Dockerfile` 삭제(단일 모듈·`src/` shade 전용 — Stage 0 이후 빌드 불가), `gcp/`·`.serverless/`·빈 모듈 잔재(`rpdptw/`·`adapters/`·`apps/`·`build/` 디렉터리)·구 `src/`(placeholder) 삭제. 새 Dockerfile은 Stage 7 |
| 뼈대 | parent pom + `solver-core`(의존 0) + `solver-profile`(고객 정책) + `app`(**Spring Boot 4.1**, starter-web) **3모듈** 구성 (Architecture §2). Boot 3.5는 2026-06-30 OSS EOL — 라인 선택 근거는 [Stage 0 §4.4](implementation/stage-00-cleanup-and-skeleton.md) |
| 경계 테스트 | ArchUnit: `verify` 패키지가 `solve` 패키지를 참조하면 실패하는 규칙 (빈 패키지여도 룰 파일 먼저) |
| README | 루트 README를 확정 설계에 맞게 갱신 |

**왜 이 순서인가**  
이후 Stage 1–5는 `solver-core`에, 고객 점수 차이는 `solver-profile`에, HTTP·S3는 `app`에 넣는다.
방을 먼저 나누지 않으면 솔버 핵심이 웹/클라우드 코드에 섞이기 쉽다.

**의도적으로 안 하는 것**  
입력 파싱, 이동표, 탐색, 재검증, 배포 이미지 — 전부 이후 Stage.

**완료 기준 (DoD)**  
- `mvn verify`가 통과한다 (컴파일·테스트·jar).  
- `app`을 기동하면 health 응답이 온다.  
- GCP 관련 라이브러리 의존이 0이다.

**용어 메모**

- **모듈**: 빌드 단위(자기 폴더 + “누구에 의존해도 되는지” 규칙). 여기서는 3개.
- **의존 0 (`solver-core`)** : 솔버 핵심이 Spring·Jackson·AWS 같은 외부 라이브러리 없이 컴파일된다. 배차 규칙을 순수 Java로 두려는 장치.
- **ArchUnit**: “패키지 A가 B를 import하면 테스트 실패”처럼 **코드 구조 규칙**을 자동 검사하는 도구. 사람이 리뷰로만 지키지 않는다.
- **pom**: Maven 빌드 설정 파일(의존성·모듈 목록).

**상세 설계**: [stage-00-cleanup-and-skeleton.md](implementation/stage-00-cleanup-and-skeleton.md) · [Architecture §2](architecture-design.md)

---

### Stage 1 — canonical 입력과 정규화 (solver-core)

**배차 관점 (한 줄)**  
호출 쪽이 보낸 **문제 데이터**(주문·차량·시간창·용량·옵션)를, 솔버가 항상 같은 방식으로 읽는
**유일한 내부 단위·의미**로 맞춘다.

**이 단계에서 만드는 것**  
- 정규화된 모델: `Request`·`Vehicle`·`Plan` 등 (Domain §1–§3).  
- 단위 변환(예: kg → ×1000 FLOOR한 정수), 시간 원점(`planStart` 기준 초), serviceTime 공식,
  호환성 판정, 입력 오류 분류.  
- **흐름**: (아직 JSON 파서가 아님) 이미 풀린 raw 값 → 정규화 → `Plan`.  
  외부 규약 JSON을 읽는 쪽은 Stage 6 adapter다.

VRPTW instance의 개념(요청, 차량, TW, capacity)은 그대로다. 달라지는 것은 **표현**이다 —
소수·날짜 없는 시각을, 솔버가 비교·연산할 수 있는 정수 체계로 확정한다.
(문자열로 인코딩된 수치는 정규화 대상이 아니라 `INVALID_INPUT`이다 — Domain §3.1,
2026-08-12 확정. 수치 필드의 JSON 인코딩은 number다.)

**왜 이 순서인가**  
이동표·전파·탐색은 모두 “이미 정규화된 입력”을 전제로 한다.
단위가 흔들리면 이후 Stage 테스트의 숫자가 전부 의미가 없어진다.

**의도적으로 안 하는 것**  
거리 행렬 완비, 경로 평가, 탐색, HTTP 접수.

**완료 기준 (DoD)**  
통과하면 다음이 보장된다.

- 무게·부피는 소수를 받아 **×1000 FLOOR**하고, **거리·시간 소수는 거부**한다 (표기 기준 —
  Domain §3.1). 소수를 조용히 반올림하지 않는다. optional 부재는 한도·용량 축이면
  “그 축 제약 없음”, `speed`·`trips`·`multiRotation`이면 규칙으로 정해진 기본값이다.
  `startDepot` 부재는 기본값이 아니라 **첫 고객에서 시작**이다 (Domain §2.4, 2026-08-14 —
  2026-08-12 단일 차고 채움 규칙은 폐기. 현 규약 wire 채움은 Stage 6).
- `multiRotation`이 core 지원 범위를 넘으면 거부한다 (통과 = `{0, 1}`,
  `-1`·`2 이상` → `UNSUPPORTED_INPUT`, `≤ -2` → `INVALID_INPUT` — Domain §2.5, §2.1 D1 확정).
  즉 multi-trip 요청은 여기서 걸린다.
- `trips` 접기 테스트 — `options.trips`가 `roundtrip`이면 `endDepot` 부재 차량이
  `startDepot`으로 복귀한다. start도 없으면 `INVALID_INPUT`이다. **차량별** `trips`는
  wire에 없어 유예했다
  ([Stage Extra E1](implementation/stage-extra-deferred-features.md), 2026-08-11).

**용어 메모**

- **canonical (정본)**: 솔버가 이해하는 **유일한 의미**. 고객 JSON 필드명·문자열 형식(wire)과 다르다.
- **정규화**: raw 값에 단위·시간·호환성 규칙을 적용해 canonical로 확정하는 과정.
- **`INVALID_INPUT` vs `UNSUPPORTED_INPUT`**: 값 자체가 잘못됨 vs 값은 이해되지만 **이 버전이 아직 안 하는 기능**(예: multi-trip).
- **접기**: 여러 입력 필드를 솔버가 쓰는 더 단순한 필드 하나로 합치는 것(예: `trips` → 차량별 종료 차고 여부).

**상세 설계**: [stage-01-canonical-input-normalization.md](implementation/stage-01-canonical-input-normalization.md) · Domain §1–§3

---

### Stage 2 — 이동표와 Problem 동결 (solver-core)

**배차 관점 (한 줄)**  
거리/시간 행렬을 완비하고, **탐색이 절대 바꾸면 안 되는 문제 정의**를 한 묶음(`Problem`)으로 얼린다.

**이 단계에서 만드는 것**  
- `LocationId` 기준 이동표(거리 D, 시간 U). 빠진 칸은 Great Circle·`ceil(D×3.6/speed)` 등으로 보정
  (Domain §4).  
- `Problem` 생성: 참조 일관성·완전성 검사 후 **동결** (Domain §5).  
- **흐름**: 정규화 `Plan` + 이동표 → 동결 `Problem`.

VRPTW에서 instance를 preprocess 후 freeze하는 것과 같다. 이후 탐색은 **해(경로·미배정)만** 바꾸고
문제(주문 집합·행렬·차량 속성)는 고정이다.

**왜 이 순서인가**  
전파·평가(Stage 3)와 탐색(Stage 4)은 “완성된 이동표 + 고정 instance” 위에서만 의미가 있다.

**의도적으로 안 하는 것**  
경로 전파, 점수, ALNS, 실물 JSON 읽기.

**완료 기준 (DoD)**  
- 이동표 보정 규칙 테스트 통과.  
- `Problem`을 만든 뒤에는 **주문·이동표를 수정하는 코드 경로가 없다**
  (탐색은 `Solution`만 변경).  
- **규모**: 실물과 같은 규모의 합성 입력(장소 453·주문 452·차량 31,
  **이동표는 453² = 205,209쌍 전부가 주어진 상태**)으로 `Problem` 생성 1회 —
  소요 시간·메모리를 기록한다. 쌍이 비어 있으면 Great Circle 보정이 대신 채워
  측정값의 의미가 사라지므로 전 쌍을 채운다.  
  실물 JSON을 읽지 않는다 (app 모듈 불필요) — 입력은 테스트 코드가 프로그램으로 조립한다.
  core만으로 “이 크기에서 문제가 만들어지는지”를 보기 위함이다.

**용어 메모**

- **동결 (freeze)**: 객체를 만든 뒤 내용을 바꾸는 API가 없음. 버그로 탐색 중에 행렬·주문이
  바뀌는 일을 타입/설계로 막는다.
- **규모 DoD**: 품질 목표가 아니라 **성능·메모리 실측 기록**. 아직 탐색하지 않는다.

**상세 설계**: [stage-02-travel-and-problem-freeze.md](implementation/stage-02-travel-and-problem-freeze.md) · Domain §4–§5

---

### Stage 3 — Solution·전파·평가 (solver-core)

**배차 관점 (한 줄)**  
**한 배차안**을 표현하고, 경로를 따라 시각·적재를 **앞에서 뒤로 계산(전파)** 하며,
두 배차안 중 어느 쪽이 나은지 **같은 비교 규칙**으로 판정한다.

**선행 (게이트 2개 — 둘 다 해제됨)**  
① 시간창 close 판정 기준 — **확정됨** (Domain §7.1, 2026-08-10).  
② 차고 시간창과 다일 근무창 — **확정됨** (§2.1 D4 완료, 2026-08-10 — Domain §3.2 + §7.1).  
Stage 3(탐색 쪽 평가)과 Stage 5(재검증)가 **같은 규칙 문장**을 각각 구현한다.
해석이 갈리면 두 코드의 숫자가 어긋나고, 원인이 버그가 아니라 해석 차이가 된다.

**이 단계에서 만드는 것** (Domain §6–§8)

```text
Problem (동결) + Profile(인자)
        │
Solution = routes + bank  ──구조 검사(pair·XOR·방문 유일·참조)──▶ 위반 목록
        │
        ▼
전파(경로마다 시각·적재·hard) ──▶ 사실 값(RouteFacts 등)
        │
        ▼
Evaluation(중립 metric) → profile.score → long[] 사전식 비교 → 승자
```

- 경로 / bank 상태, 적재 부호(initialLoad 포함), 전파 절차·기록 값(Domain §7 — 세부는 Domain).  
- metric(`Evaluation`), profile의 score 축(`long[]`)과 사전식 비교, `DefaultProfile` +
  `ProfileRegistry`(solver-profile 모듈).

**왜 이 순서인가**  
탐색(Stage 4)은 “해를 바꿔 보고 더 나은지 비교”가 전부다. 비교기가 없으면 ALNS를 돌릴 수 없다.
재검증(Stage 5)도 같은 물리 규칙(전파)을 **다른 패키지에서** 다시 구현하므로, 규칙 해석이
Domain에 먼저 고정돼 있어야 한다.

**의도적으로 안 하는 것**  
destroy/repair 루프, 초기해 휴리스틱, HTTP.

**완료 기준 (DoD)**  
- Domain §7.2 숫자 예를 **그대로** 재현한다 (전파가 문서와 일치).  
- XOR 위반·hard 위반을 검출한다.  
- 미등록 `customerId`는 default profile을 쓴다.

**용어 메모**

- **bank**: 미배정 Request 집합. 사유 문자열은 bank에 넣지 않는다(결과는 Stage 5).
- **XOR (이 문서)**: 각 Request는 **경로 위 또는 bank**에 정확히 하나. 둘 다/둘 다 아님 = 구조 결함.
- **전파 (propagation)**: 방문 순서를 따라 arrival·대기·서비스·적재를 앞에서 뒤로 확정하는 계산.
- **profile**: 고객마다 “점수 축에 무엇을 넣을지·추가 hard”를 담는 교체 가능한 정책.
  core에 `if (customerId == …)` 를 쓰지 않기 위한 장치.
- **사전식 `long[]` 비교**: 배열 앞쪽 축이 다르면 그것만으로 승패. 가중합으로 축을 뭉개지 않는다
  (hard 위반을 감점으로 상쇄하지 않는 설계와 한 세트).

**상세 설계**: [stage-03-solution-propagation-evaluation.md](implementation/stage-03-solution-propagation-evaluation.md) · Domain §6–§8

---

### Stage 4 — 초기해와 ALNS (solver-core)

**배차 관점 (한 줄)**  
초기 배차안을 만들고, destroy/repair로 **해를 반복 개선**한다. 한 번에 넣거나 빼는 단위는
항상 Request 전체(pair)다.

**두 단계로 나눠 구현한다 (2026-09-02).** 하나의 Stage지만 **따로 만들고 따로 끝낸다** —
`4-초기해`가 green이 된 뒤 `4-ALNS`를 시작한다 (§2.2 순서 표).

**4-초기해에서 만드는 것** (Domain §9.3 재량)  
- **난수를 쓰지 않는 rule 기반 construction 24개(기본 8 + 확장 14 + 실물 맞춤 2)**와, 그것을 돌려 정식
  평가로 최선 하나를 고르는 포트폴리오. 기법 목록·의사코드·기권 규칙·테스트는
  [stage-04-initial-solution-heuristics.md](implementation/stage-04-initial-solution-heuristics.md),
  확장 14개의 문헌 근거와 실물 맞춤 2개의 실측 근거는 [heuristics-survey](implementation/stage-04-initial-solution-heuristics-survey.md).  
- 삽입 후보 탐색·검증(`InsertionSearch`) — 4-ALNS의 repair 연산자가 그대로 재사용한다.  
- **ALNS 타입을 하나도 참조하지 않는다** — 그래서 이 단계만으로 완결되고 단독으로 green이 된다.

**4-ALNS에서 만드는 것** (Domain §9)  
- destroy / repair(pair 단위), acceptance, 시간 한도 종료.  
- 삽입 shortlist 근사는 재량. **수락·기각 판정은 정식 평가(Stage 3)만** 사용.  
- 초기해는 **인자로 받는 오버로드**가 있어(Stage 4 문서 §3.2), 4-초기해가 없어도 손 조립
  해로 단독 검증된다. 기본 경로에서는 4-초기해의 포트폴리오를 쓴다.

ALNS 이론(파괴·재삽입·수용 기준·시간 예산)은 이미 안다고 가정한다.
이 시스템이 추가로 고정하는 것은 **pair 원자성·XOR 유지·수락은 정식 평가만**이다.

**왜 이 순서인가**  
Stage 3의 평가 없이는 “더 나은 해”를 정의할 수 없다.
재검증(Stage 5의 앞부분)은 ALNS **없이** 만들 수 있고, 오히려 ALNS보다 **먼저** 만드는 것이
맞다 — Stage 5 절 참고. 구현 순서상으로는 3 → 5-재검증 → **4-초기해 → 4-ALNS** → 5-결과.

**의도적으로 안 하는 것**  
결과 JSON 조립, 발행, HTTP. 탐색 파라미터의 최종 튜닝은 Stage 8.

**완료 기준 (DoD) — 4-초기해**  
- 24개가 각각 구조 검사를 통과하고 **정식 평가에서 Feasible**이다
  (미배정이 남는 것은 Feasible이다 — 미배정 수는 점수 1번 축이지 제약 위반이 아니다).
  성립하지 않는 기법은 기권하고 건너뛴다.  
- 난수를 쓰지 않으므로 **같은 입력에 대해 언제나 같은 초기해**가 나온다.  
- **기법별 실측 기록**: 규모 측정에서 24개 각각의 소요·미배정 수·score와
  총 소요 / `timeLimitSec` 비를 출력한다 — 24 → 4 축소 판단의 입력이다 (결정은 Stage 8).  
- **규모**: Stage 2와 같은 합성 문제로 포트폴리오 1회 — 정상 종료를 확인하고 위 수치를 기록한다.  
- 판정: `mvn verify` green + heuristics 문서 §8의 T13–T44.
  **이 시점에 ALNS는 아직 없다** — 그래도 이 목록이 전부 통과해야 다음으로 간다.

**완료 기준 (DoD) — 4-ALNS**  
- 소형 fixture에서 초기해 대비 개선이 관측된다.
  (2026-09-02: 기준값이 **포트폴리오 24개 중 최선**이라 이 단언은 더 엄격해졌다.)  
- pair·XOR 불변식이 탐색 중 유지된다 — **property 테스트**
  (예: 랜덤 스텝 N회 후 구조 검사).  
- **규모**: Stage 2와 같은 합성 문제로 ALNS 1회 — 시간 한도 안에 몇 번 반복했는지 기록한다.
  여기서도 실물 JSON을 읽지 않는다. 실물 fixture 전 구간 실행은 Stage 6.
  (품질을 Win과 맞추는 목표는 Stage 8.)  
- 판정: `mvn verify` green + Stage 4 문서 §7의 T1–T12.

**용어 메모**

- **property 테스트**: 손수 만든 입력이 아니라, 무작위 연산 후에도 “구조 규칙이 항상 참”인지를
  검사하는 테스트. 탐색 버그가 특정 fixture에만 안 보이는 경우를 줄인다.
- **shortlist 근사**: 삽입 후보를 줄이기 위한 빠른 대략 평가. **채택 여부**는 근사로 정하지 않고
  Stage 3 정식 평가로 정한다.

**상세 설계**: [stage-04-alns.md](implementation/stage-04-alns.md) · Domain §9

---

### Stage 5 — 재검증과 결과 (solver-core)

**배차 관점 (한 줄)**  
탐색이 낸 최종 배차안을, **탐색 코드를 믿지 않고** 처음부터 다시 계산해 hard 규칙을 확인한다.
통과한 것만 결과로 만든다 (유일한 발행 규칙).

**순서 — 왜 “5”인데 4보다 먼저 재검증을 만드는가**

재검증은 “해 + 문제(+ 같은 profile)”만 있으면 된다. ALNS 타입이 필요 없다.
그래서 **Stage 3 다음, Stage 4보다 먼저** 재검증을 만든다.

이유 둘:

1. ALNS를 먼저 만들고 그 코드를 보며 재검증을 쓰면, 탐색 쪽 실수를 **그대로 복사**하기 쉽다.
   “독립 검증”의 의미가 사라진다.  
2. ALNS를 개발하는 동안 이미 **독립 검사기**를 손에 둘 수 있다.
   Stage 4 내부 검사만으로는 연산자와 공유하는 구조 검사 버그를 같이 못 볼 수 있다.

결과 모델·미배정 사유 문자열은 **Stage 4 뒤**에 만든다 — 기술 의존이 아니라 **집중을 위한 선택**이다
(결과 조립기는 재검증 Pass만 있어도 만들 수 있다).  
Stage 5 **문서는 쪼개지 않는다** — 문서 안 §1–§3이 재검증, §4가 결과 모델이라 **읽는 순서만** 바꾼다.

**이 단계에서 만드는 것** (Domain §10–§11)  
- `verify` 패키지의 독립 재검증: 해 전체를 **캐시 없이** 재계산 (탐색 예산·시간 한도는 읽지 않음).  
- 결과 모델: routes / unassigned+reason / metrics / run 메타.  
- 검증된 해 ↔ 결과 내용 일치 테스트.

**의도적으로 안 하는 것**  
HTTP 응답 형식의 최종 협의(D2), S3 업로드(Stage 6).

**완료 기준 (DoD)**  
- 일부러 오염시킨 해(짝 분리·용량 초과·점수 불일치)가 **전부 FAIL**.  
- FAIL인 해는 결과로 저장하지 않는 규칙이 코드·테스트로 고정된다.  
- ArchUnit: 재검증이 `solve` **패키지 전체**를 참조하지 않는다 — ALNS만이 아니라
  `Solution` 등 solve 소유 타입 전부다 (stage-05 §2).

**용어 메모**

- **재검증**: 탐색과 **다른 코드 경로**로 같은 물리·구조 규칙을 다시 적용하는 검사.
  “더 좋은 해 찾기”가 아니라 “이 해가 규칙 위반이 아닌가”.
- **캐시 없이**: 탐색 중에 쌓인 중간 계산을 재사용하지 않고, 방문 순서만 보고 다시 전파한다.
- **발행 규칙**: 재검증 PASS인 배차안만 결과 JSON이 된다. FAIL이면 상태 FAILED, 결과 파일 없음.

**상세 설계**: [stage-05-verification-and-result.md](implementation/stage-05-verification-and-result.md) · Domain §10–§11

---

### Stage 6 — 앱 조립 (app)

**시스템 관점 (한 줄)**  
솔버를 **HTTP 서비스**로 감싼다: 접수 → 저장 → 같은 프로세스에서 백그라운드 풀이 → 조회.

**이 단계에서 만드는 것** (Architecture §3)

```text
POST /solves  → 형식·정규화 검증 → 저장 → 200 + solveKey   ← 여기까지 동기 (ALNS 대기 안 함)
       ↓ (비동기 executor)
  adapter → 정규화 → … → ALNS → 재검증 → 결과 저장 / FAILED
GET /solves/{solveKey}[/result]  → 상태·결과 조회
```

- 규약 JSON **adapter** (win_poc fixture로 검증): wire → Stage 1 raw 운반체.  
- `SolveStore` (테스트용 fake + S3 구현).  
- 접수 API, `SolveExecutor`(상태 전이·heartbeat·STALE), 조회 API.

**왜 이 순서인가**  
솔버(1–5)가 먼저 있어야 “접수 후 풀이”가 의미가 있다.
adapter·storage 일부는 기술적으로 더 일찍 만들 수 있으나(§2.2), **기본은 core-first**로
Stage 6에서 모은다.

**의도적으로 안 하는 것**  
클라우드 배포(Stage 7), Win 벤치 튜닝(Stage 8).

**완료 기준 (DoD)**  
- fake 저장소로 e2e: POST 접수 → DONE → GET 결과.  
- `win_poc_case_floor.json` 접수·완주 — **§0 성공 기준 달성 시점**.

**용어 메모**

- **adapter**: 외부 JSON의 필드명·표기 → core가 받는 raw 구조로의 **형식 변환**.
  의미·단위 규칙의 정본은 Stage 1 정규화다. adapter가 추측으로 의미를 만들지 않는다.
- **wire**: 호출 시스템과 주고받는 JSON 형식(필드 이름·시각 문자열 등). canonical과 다름.
- **solveKey**: 풀이 한 건을 가리키는 ID이자 저장 경로 키 (`solves/{customerId}/{planId}/{runId}/` 등).
- **fake 저장소**: 실제 S3 없이 메모리 등으로 같은 저장 인터페이스를 구현해 테스트를 돌림.
- **e2e (이 문서)**: API 입구부터 결과 조회까지 **한 줄로 잇는** 통합 테스트.
- **heartbeat / STALE**: 풀이 중 “아직 살아 있음”을 주기적으로 기록 / 기록이 끊기면 죽은 것으로 보는 상태.

**상세 설계**: [stage-06-app-assembly.md](implementation/stage-06-app-assembly.md) · Architecture §3

---

### Stage 7 — ECS 배포

**시스템 관점 (한 줄)**  
같은 앱을 **클라우드에서 상시 실행**하고, 입력·상태·결과를 실제 S3에 둔다.

**이 단계에서 만드는 것** (Architecture §5)  
- Dockerfile (app jar).  
- ECS Fargate 서비스·태스크 롤(S3 권한)·환경변수·CloudWatch 로그.  
- 선택: 배포 전 LocalStack으로 e2e 1회.

**왜 이 순서인가**  
로컬(fake/실 S3 설정)에서 §0이 통과한 뒤, 배포 환경에서 같은 시나리오를 한 번 더 확인한다.
AWS 계정·버킷·롤 등 준비물(D3)은 코드와 별개로 배포 시점에 필요하다.

**의도적으로 안 하는 것**  
알고리즘 변경, 다계정 multi-region 등 범위 밖 인프라.

**완료 기준 (DoD)**  
배포 환경에서 실제 S3로 §0 시나리오 1회 성공.

**용어 메모**

- **ECS Fargate**: 서버(EC2)를 직접 고르지 않고 컨테이너를 실행하는 AWS 방식.
- **태스크 롤**: 그 컨테이너가 S3 등에 접근할 때 쓰는 **권한 신분증**.
- **LocalStack**: 로컬에서 S3 등을 흉내 내는 도구(선택, 배포 전 연습용).

**상세 설계**: [stage-07-ecs-deployment.md](implementation/stage-07-ecs-deployment.md) · Architecture §5

---

### Stage 8 — 벤치마크 비교 (마무리)

**배차 관점 (한 줄)**  
같은 입력에 대해 **기존 엔진(Win)** 과 지표를 나란히 놓고, 필요하면 탐색 파라미터만 조정한다.

**이 단계에서 만드는 것**  
- win_poc 입력의 Win 결과와 비교: 미배정 수·차량 수·거리·시간 등.  
- 비교 절차와 수치 기록 (사전 목표 수치를 문서에 박아 두지 않음 — 실험으로 정한다).  
- 필요한 만큼 탐색 파라미터 조정.

**이 Stage가 아닌 것**  
새 제약·새 목적함수·MIP 도입 같은 **알고리즘 재설계**가 아니다. “돌려 보고 기록하고 조정”이다.

**완료 기준 (DoD)**  
비교 절차와 수치가 이 단계 산출물로 남는다. (고정 합격선 숫자는 사전 확정하지 않음.)

**상세 설계**: [stage-08-benchmark-comparison.md](implementation/stage-08-benchmark-comparison.md)

---

### Stage Extra — 유예 항목 (트리거 대기)

**한 줄**  
지금 만들지 않기로 했지만 **조건이 되면 만든다**고 정한 항목의 등재부다.
Stage 0–8과 달리 **순서상 자리가 없다** — 트리거가 발동하는 시점에 실행된다.

**여기 들어가는 기준**  
**나중에 넣는 비용이 지금 넣는 비용과 같은가?** 같으면(순수 add-only) 유예하고,
다르면(타입 모양 변경·여러 Stage 동시 수정) 지금 만든다.
"지금 안 쓰는가"는 기준이 아니다 — 그 기준으로 자르면 다일 시간창·정수 단위 체계처럼
**나중에는 못 넣는 것**까지 잘려 나간다.

**현재 등재 항목** (2026-08-11, Stage 1 오버엔지니어링 검토)

| # | 묶음 | 트리거 |
|---|---|---|
| E1 | 차량별 `trips` · `depot.taskTime`(복귀 선적 시간) · 차량별 `multiRotation` | multi-trip을 열 때, 또는 wire에 해당 필드 등장 |
| E2 | item·차량 치수 축 (3D 적재) | 3D 적재 고객 확정 + wire 협의 |
| E3 | 차량 소유 구분(`VehicleOwnership`) | 소유 구분이 점수 축·hard 제약이 될 때 |

**이것이 아닌 것**  
Win과의 **지표 차이 요인**은 [Stage 8 §6](implementation/stage-08-benchmark-comparison.md)의
W 표다 — 벤치마크 해석의 문제이지 구현 작업 항목이 아니다. 두 목록을 섞지 않는다.
**범위 선언**(무엇이 범위 밖인가)은 [Master §4·§6](master-design.md)이다.

**완료 기준 (DoD)**  
없다 — 완료되는 Stage가 아니다. 트리거가 발동하면 Stage Extra 문서의 §6 승격 절차를 거쳐
정식 Stage로 옮겨지고 등재부에서 빠진다.

**상세 설계**: [stage-extra-deferred-features.md](implementation/stage-extra-deferred-features.md)

## 2. 지금 시작하는 결정과 순서

> **이 절의 성격**: Stage 본문(§1)과 읽는 목적이 다르다. 여기는 **완료/미결 결정 로그**다.
> D1·D4는 끝났고, 남은 미결은 D2·D3뿐이다. Stage를 순서대로 구현할 때는 §1만으로 충분하고,
> “왜 예전에 막혔는가 / wire·AWS는 무엇을 남겼는가”를 볼 때 이 절을 연다.

### 2.1 지금 시작하는 결정 (코드 0줄)

Stage 순서와 별개로 **지금 착수할 수 있고, 늦어지면 뒤 Stage를 막는** 결정들이다.
여기 적히기 전에는 이 항목들이 Stage 문서의 "미해결 질문" 사이를 떠돌기만 하고
착수 시점이 없었다. 각 항목의 정본은 이 표이고, Stage 문서는 여기를 가리키기만 한다.

| # | 결정 | 지금 아는 것 / 남은 일 | 막고 있는 것 |
|---|---|---|---|
| **D1** | **`multiRotation` 값 의미와 범위** — **완료 (2026-08-10 확정)** | 문제였던 것: 이 숫자가 무엇을 세는지 몰라 fixture의 `"1"`이 통과인지 거부인지 갈렸고, 그 탓에 §0 최종 성공 기준 fixture가 접수에서 거부돼 Stage 6·7·8이 통째로 막혀 있었다.<br>**확정 내용 (시스템 소유자)** — 정본은 **Domain §2.5**다: 숫자는 **차량이 도는 바퀴 수**를 센다. `1` = 1바퀴 = 차고에서 출발해 한 번 도는 것 = **지원 범위 안**이고, `2`부터가 차고 재방문(multi-trip)이라 범위 밖이다. `0`은 미설정으로 보아 `1`과 같게 취급한다 (규약 기본값이 `0`이므로 값을 안 준 입력이 자연스럽게 통과해야 한다).<br>**판정이 반전됐다**: 종전 `!= 0` 거부(0만 통과) → **통과 = `{0, 1}`**. `-1`(무제한 복귀)·`2` 이상 = `UNSUPPORTED_INPUT`, `≤ -2`(규약이 "greater than -1"로 금지) = `INVALID_INPUT`. **비교식 `> 1`로 쓰지 않는다** — `-1`이 게이트를 그냥 통과해 버린다.<br>**규약 PDF 문면과 어긋난다 (기록):** PDF 4페이지 열거 정의(`0 : can't return to depot` / `1>= : ... designated multi rotation times`)는 숫자를 **차고 복귀 횟수**로 읽게 만들어 확정 의미와 한 칸 어긋난다 — PDF만 보고 구현하면 판정이 정확히 반대로 나온다. 종전 이 칸은 "열거 정의가 권위이고 `multirotation 2` 예시 그림이 부주의"라고 적었는데 **그 판단이 뒤집혔다**: 그 그림(`depot(start) → 1st → 2nd → depot(2nd visit) → 3rd`)은 2를 2바퀴(복귀 1회)로 그려 **바퀴 수 해석과 일치**하고, 어긋나는 것은 `1>=` 한 줄뿐이다. 소유자 확정이 정본이고 PDF 문면이 부정확한 것으로 본다.<br>**연쇄로 풀린 것들** — ① **fixture 교정(옛 경로 B)이 불필요해졌다.** `data/win_poc_case_floor.json`은 `multiRotation: 1` **값 그대로 접수된다** (표기는 2026-08-12 number 인코딩 정정 반영 — Domain §3.1). 값 차이를 추적할 일도 없다. ② 옛 경로 A(호출 시스템 확인)도 소유자 확정으로 닫혔다 — Stage 8 §6 W1의 "무엇을 비교하는가" 물음이 해소된다(양쪽 다 1바퀴). ③ Stage 6 T13·Stage 7 V6·Stage 8 P1의 선행 조건이 전부 해제됐다 | (해제됨) Stage 6 e2e·Stage 7 DoD·Stage 8 전체가 이제 진행 가능하다. 반영된 문서: Master §4·§6 · Domain §2.4·§2.5·§12 · Architecture §3.1 · Stage 0·1·2·6·7·8 |
| **D2** | **wire 협의** | 결과 JSON 필드명·시각 표기·단위 표현 · 조회 경로와 HTTP 상태 · `customerId`의 wire 원천(`shprId` vs 주문 수준 `customerId`) · legacy 시간 필드(주문 수준 `taskTime`)의 처리 · ~~`driverRestTimeRatio`~~ **종결 (2026-08-12, 시스템 소유자)** — 무시 확정, 0이 아닌 값도 거부하지 않는다 (Domain §2.5) · ~~`item.taskTime × qty` 해석~~ **종결 (2026-08-11, 시스템 소유자)** — `taskTime`·`weight`·`volume` **전부 × qty**가 맞고 규약 PDF 문면("not quantity")이 부정확하다. Domain §3.1·§3.3이 정본이고 설계 변경 없음. floor fixture는 qty 전건 `1`·order당 item 1개(실측)라 무영향 · 결과의 경로 시각 2종(`depotDeparture`·`depotReturn` — Domain §11.1, 2026-08-11 추가)과 방문 `departure`(Stage 5 §9 Q5)의 wire 노출 · 이동표의 비대각 `D=9999` 1건(`WIN_2306→WIN_3225`, U=991 — Stage 2 N4)이 실거리인지 결측 표시인지 확인. **첫 작업은 협의 상대를 찾는 것이다** — Stage 6 §10 Q4가 "협의 상대가 현재 없음"으로 멈춰 있다. 의미(Domain §11.1)는 협의 대상이 아니고 이름·형태만 정한다 | Stage 6 result.json wire 확정 (잠정안으로 구현은 가능하나, 협의 후 바뀌면 golden 테스트를 다시 쓴다) |
| **D3** | **AWS 사전 준비** | 배포 계정·리전 · 버킷 이름(`ro-next-solves-{env}`의 `{env}`) · 태스크 롤·실행 롤 · ECR 리포지터리 · ECS 클러스터 · 로그 그룹, 그리고 **엔드포인트 노출 방식**(ALB인지 내부 엔드포인트인지 — 호출 시스템의 네트워크 위치에 달렸다). 전부 Architecture §5가 "배포 시 결정"으로 열어 둔 것이고 코드와 무관하다 | Stage 7 전체 (환경당 1회 준비 — Stage 7 §4) |
| **D4** | **다일 근무창 + 차고 시간창 설계** — **완료 (2026-08-10 확정)** | 문제였던 것: 현행 모델이 다일 계획의 근무 시간대를 표현하지 못했고(`Vehicle.workWindow` 하나·시작일 고정), 차고의 `openTime`/`closeTime`이 전파에서 아예 읽히지 않았다. 둘 다 전파 절차를 바꾸므로 한 설계로 묶었다.<br>**확정 내용** — 정본은 **Domain §3.2(시간창 전개)·§7.1(출발·방문·종료 절차)·§7.3(대기 3종·항등식)**이다: ① **canonical 표현** = 정규화가 **날마다 반복되는 창을 절대 창 목록으로 펼친다** (`List<TimeWindow>` — 근무창·차고 창·**주문 시간창** 셋 다. 규약이 세 창을 전부 날짜 없는 partial-time으로 주므로 반복 말고 다른 해석이 없다) ② **정규화** = 전날부터 planEnd 날짜까지 생성 → `close < open`은 자정 넘김으로 **수용**(`close == open`은 신규 INVALID_INPUT) → `[0, planEndSec − 1]`로 **클리핑**(계획 기간을 넘겨 끝나는 경로가 사라지는 **새 경계**) → 정렬·인접 병합 ③ **전파** = 창 끝을 넘는 이동·서비스는 **다음 창으로 미룬다**(불가 판정이 아니다). 남은 창이 없을 때만 `WORK_WINDOW` ④ **차고 창** = 출발은 "근무창 ∩ startDepot 창 안"(다일이라 상한이 생긴다), 복귀는 "어느 endDepot 창 안"이며 **미루지 않는다** → `DEPOT_WINDOW` ⑤ **`interWorkWindowRestTime`** = 경로 시간 중 근무창 사이의 틈에 있는 시간 전부. 항등식 `routeOperationalTime = routeEnd − spanStart` ⑥ 모든 소요 시간은 **두 시각의 차**로 잰다 (초를 세면 창마다 1초 어긋난다).<br>**현행 fixture(1일)에서는 값이 하나도 바뀌지 않는다** — 근거는 Stage 1 §4 말미의 실측 표 | (해제됨) Stage 3·Stage 5가 이제 Domain의 같은 문장을 보고 구현한다. 반영된 문서: Domain · Stage 1·3·4·5·6·8 |

**네 항목은 성격이 둘로 갈린다.** D2·D3은 **바깥의 답을 기다리는** 항목이라 우리가 할 일은 묻고
기록하는 것이다. D1·D4는 답이 나오면 **우리가 문서를 고치는** 작업이었고, **둘 다 2026-08-10에
끝났다** — D4는 Domain §3.2·§7.1 개정(§7.2.1·§7.3·§13 포함)과 Stage 1·3·4·5·6·8 갱신으로,
D1은 시스템 소유자의 의미 확정(바퀴 수)과 그에 따른 판정 반전으로 닫혔다.

**남은 미결은 D2·D3 둘뿐이고, 지금 착수를 막는 것은 없다.** D2(wire 협의)는 잠정안으로
구현이 가능하고(협의 후 바뀌는 것은 golden 테스트뿐), D3(AWS 사전 준비)은 Stage 7 배포
시점에 필요한 준비물이다. **D1이 닫히면서 §0 최종 성공 기준까지 막고 있던 것이 없어졌다** —
Stage 1부터 8까지 순서대로 진행하면 된다.

**차량별 `trips`도 같은 날 함께 확정됐다** (2026-08-10). D 항목으로 올리지 않은 이유는 이것이
기다릴 답이 아니라 곧바로 반영 가능한 설계 결정이었기 때문이다 — `options.trips`가 전체
기본값이고 차량에 값이 있으면 그 차량이 자기 값을 쓴다. 다만 **구현은 2026-08-11 Stage Extra
신설로 유예됐다** — 의미(차량 값 우선)는 확정된 채
[Stage Extra E1](implementation/stage-extra-deferred-features.md)에 기록돼 있고, 현행 wire에
그 필드가 없어 Stage 1·6에는 반영돼 있지 않다 (Domain §2.5 유예 주석). 되살릴 때도 core는
사실상 무변경이다 — 정규화가 `trips`를 이미 차량별 `endDepot`으로 접고 있어서, 접기 체인
앞에 한 단계가 붙을 뿐이다.

### 2.2 순서와 병행

```text
0 → [fixture 실측] → 1 → 2 → [Domain 해석 확정] → 3 → 5-재검증
  → 4-초기해 → 4-ALNS → 5-결과 → 6 → 7 → 8
```

솔버(1–5)가 중심이고 앱(6)은 얇다. 한 Stage를 끝내고(테스트 green) 다음으로 간다.
대괄호 칸은 **코드를 쓰지 않는 단계**다 — 재고 조사와 해석 확정이라 Stage 번호를 주지 않는다.
**5-재검증이 4보다 앞인 이유**는 Stage 5 절(순서)에 평문으로 적어 두었다.

| 칸 | 무엇 |
|---|---|
| **[fixture 실측]** | 실물 fixture의 크기·값을 **미리 재서** Stage 2·4의 규모 DoD 목표 숫자를 고정한다. 세는 것뿐이라 코드가 없다. 2026-08-10 실측(`data/win_poc_case_floor.json`): 주문 452 · 차량 31 · 차고 1 · 장소 **453** · 이동표 **205,209쌍 = 453² (전 쌍이 입력에 주어짐, 희소하지 않다)** · `multiRotation 1`(**D1 확정 후 이 값은 그대로 접수 통과한다** — 1바퀴, 교정 불필요) · `Termination.secondsSpentLimit 600` · `Optimizer.VehicleMaxStopCount 28` · `Optimizer.DefaultSpeed 45` (수치 표기는 2026-08-12 number 인코딩 정정 반영 — Domain §3.1) |
| **[Domain 해석 확정]** | **완료 (2026-08-10).** 전파 규칙의 해석을 Domain에 올려 Stage 3·5가 **같은 문장**을 보고 구현하게 했다. 시간창 close 기준(Domain §7.1)과 차고 창·다일 근무창(**D4** → Domain §3.2·§7.1·§7.3) 둘 다 확정됐다 |
| **5-재검증 / 5-결과** | Stage 5 문서를 쪼개지 않는다. 그 문서는 이미 §1~§3이 재검증, §4가 결과 모델이라 **읽는 순서만** 바뀐다 (Stage 5 절 참고) |
| **4-초기해 / 4-ALNS** | **따로 구현하고 따로 끝낸다 (2026-09-02).** 5-재검증/5-결과와 달리 문서도 이미 둘이다 — `stage-04-initial-solution-heuristics.md`가 4-초기해, `stage-04-alns.md`가 4-ALNS다. 각자 DoD와 테스트 표를 따로 갖고, 4-초기해는 ALNS 타입을 **하나도 쓰지 않아** 단독으로 green이 된다. 4-ALNS도 초기해를 인자로 받는 오버로드가 있어 손 조립 해로 단독 검증된다 (Stage 4 문서 §3.2) |

**Stage 6의 병행 가능성** (이전 판의 "6의 adapter·storage는 3 이후 병행 가능"은 사실이 틀렸다):

- **6a `storage`** (`SolveKey`·`SolveStore`·`S3SolveStore`·`LocalSolveStore`)는 solver-core 타입을
  **하나도 쓰지 않는다** — Stage 0 이후 언제든 만들 수 있다 (Stage 6 §2.1).
- **6b `input` adapter**의 `PlanJsonAdapter`는 **Stage 1**의 `PlanInput`에만 의존한다 — Stage 3이
  아니라 Stage 1 이후면 된다 (Stage 6 §2.2). 같은 패키지의 `ResultJsonWriter`는 Stage 5가 필요하다.
- 그래도 **기본은 Stage 6에서 함께 한다 (core-first)**. 솔버에 집중하기 위한 선택이며,
  위 두 줄은 "당겨서 하자"는 제안이 아니라 **막고 있는 것이 없다는 사실의 기록**이다.
  앞 Stage가 막혔을 때 쓸 수 있는 우회로인데, **D1이 닫힌 지금은 실제로 막힌 Stage가 없다** —
  이 우회로를 쓸 일은 당분간 없다.

## 3. 하지 않는 것

- MIP 재조합, SQS·Step Functions, multi-trip — 설계 범위 밖 (Master §4·§6)
- **wire에 없는 필드의 선제 구현** — 차량별 `trips`·치수 축·`VehicleOwnership` 등은
  [Stage Extra](implementation/stage-extra-deferred-features.md)에 등재하고 만들지 않는다.
  Master §6이 차량별 `multiRotation`에 대해 이미 정한 원칙("그 전에 미리 필드를 만들어 두지
  않는다")을 전 항목에 적용한 것이다 (2026-08-11)
- 탐색 파라미터의 사전 확정 — Stage 4·8에서 실험으로
- 15-phase 문서의 부활 — 참고가 필요하면 [아카이브](deprecated/implementation-15phase/README.md)를 읽되, 규칙 충돌 시 현행 설계 3문서가 이긴다
