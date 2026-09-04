# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 이 저장소의 성격 — 설계가 본체, 코드는 입력층까지만 있다

배차 최적화(RPDPTW) 서비스. **Stage 0 완료 (2026-08-10) · Stage 1 완료 (2026-08-16) ·
Stage 2 완료 (2026-08-17) · Stage 3 완료 (2026-09-02) · Stage 4-초기해 완료 (2026-09-02) ·
Stage 4-ALNS 완료 (2026-09-02, **2026-09-04 개정 반영**)** —
구 placeholder(`com.ronext.optimizer`, 수제 `HttpServer`, 합성 데모 `AlnsBatchEngine`)와 GCP 잔재는
삭제됐고, 디스크의 코드는 확정 설계와 같은 3모듈 구조다. 정식 평가(전파·metric·profile)와
초기해 construction 24개 포트폴리오(기본 8 + 확장 14 + 실물 맞춤 2)와 ALNS 본체(연산자 5개·적응 가중치·
acceptance·종료 4조건)까지 서 있고, **재검증은 아직 0**이다.

- **확정 설계**: AWS ECS Fargate 위 단일 Spring Boot 서비스, 저장은 S3만.
- **현 코드**: 4개 pom + `RoNextApplication` + `application.yml` +
  `solver-core`의 `domain`(canonical 모델 20개) · `domain.input`(raw 운반체 8개 + `PlanNormalizer`) ·
  `problem`(`Problem`·`NodeRef`) · `eval`(사실 값·`Evaluation`·profile SPI·`DefaultProfile`·`Scores`) ·
  `solve`(`Solution`·`StructureCheck`·`RoutePropagator`·`Evaluator` + 초기해: `InsertionSearch`·
  `ConstructionHeuristic` SPI·`InitialSolutionBuilder`·`InitialSolutionResult`·`ConstructionOutcome`·
  construction 24개 `*Construction`·`GiantTourSplit`·`ZoneQuotaAllocation` + ALNS: `AlnsSolver`·
  `AlnsConfig`·`AlnsResult`·`AlnsRunStats`(`Termination` 중첩)·`DestroyOperator`·`RepairOperator`·
  `RandomRemoval`·`RouteRemoval`·`StringRemoval`·`GreedyInsertion`·`RegretInsertion`·`AdaptiveWeights`) +
  `solver-profile`의 `ProfileRegistry` + 테스트(solver-core 38클래스 102개, solver-profile 1클래스 1개,
  app 3클래스 4개).
  `solve`에 하위 패키지는 없다.
  `verify`·`api`·`run`·`input`·`storage`는 **아직 빈 패키지**다 — 그 타입들을 grep해서 안 나오는 게
  정상이고, 아직 안 만든 것이지 다른 데 있는 게 아니다.
- Stage 1–3 코드는 각 stage 문서의 §1 파일 표·§2 시그니처·§3~§4 절차와 1:1이고,
  4-초기해 코드는 [stage-04-heuristics](docs/implementation/stage-04-initial-solution-heuristics.md)의
  §2 파일 표·§3 시그니처·§5 의사코드와 1:1이다 (구현 중 정정 3건은 그 문서 frontmatter `revisions`
  2026-09-02 항목 — `apply`의 `Problem` 인자, T25 정차 한도 28, `InsertionSearch.Cache`).
  실물 맞춤 H23·H24(존 배정 DP + 존 내부 적재 2종)의 근거·실측은
  [survey §2.5](docs/implementation/stage-04-initial-solution-heuristics-survey.md)에 있다 —
  실물 fixture에서 H23이 미배정 0·31대(H3는 15). 이 수치는 app 모듈의 T44(`WinPocFixtureTest`)가
  고정한다(규약 JSON → `PlanInput` 매핑은 그 테스트 전용이고, 정식 어댑터는 Stage 6).
  4-ALNS 코드는 [stage-04-alns](docs/implementation/stage-04-alns.md)의 §2 파일 표·§3 시그니처·§4 절차와
  1:1이다 (구현 중 확정 5건은 그 문서 frontmatter `revisions` 2026-09-02 "구현 직전 정합" 항목 —
  `Termination`은 `AlnsRunStats` 중첩, `repair`가 `Profile`을 받음, `RegretInsertion`은 `InsertionSearch.Cache`,
  E14/T5 결정성은 `maxSteps` 종료 + worse 확률 0 조건, T1 fixture는 PD 2건 포함 12건).
  진입점은 `AlnsSolver.withDefaults(config).solve(problem, profile)`(포트폴리오 초기해) 또는
  `solve(problem, profile, initial)`(손 조립 초기해)이고, 탐색 예산은 `AlnsConfig`에만 있다.
  2026-09-04 개정 4건도 반영됐다 (frontmatter `revisions` 2026-09-04 항목) — §4.3의 "이미 Infeasible인
  기존 경로는 후보에서 제외"(`InsertionSearch.collect`는 예외 대신 후보 0개를 낸다 — 비삼각 이동표에서는
  방문을 빼는 것만으로 뒤 방문이 창을 넘긴다, N9), q를 비율에서 **절대 개수 5~20**으로,
  §4.5 acceptance의 **축 가드**(앞 두 축이 동률일 때만 확률 수락), `StringRemoval` 신설(destroy 3종).
  **다음 작업은 Stage 5** — [stage-05](docs/implementation/stage-05-verification-and-result.md) —
  재검증(`verify`, ALNS 결과의 `best`·`bestEvaluation`·`bestScore`를 캐시 없이 대조)과 결과 모델.
  손대기 전에 **해당 단계의 문서를** 읽는다.
- T25(`InitialSolutionScaleTest`)·T11(`AlnsScaleTest`)은 규모 측정이라 수십 초 걸린다 — 단일 테스트를 돌릴 때는
  `-Dtest='!InitialSolutionScaleTest,!AlnsScaleTest' -Dsurefire.failIfNoSpecifiedTests=false`로 뺄 수 있다.
  app 모듈의 T12b(`WinPocAlnsTest`)도 실물 fixture에 ALNS 20초라 그만큼 걸린다.
- 문서·커밋 메시지는 한국어다. 용어(`Request`/pair/`Problem`/`Solution`/bank/profile/재검증/
  solveKey)는 문서 표기를 그대로 쓴다 — 같은 개념에 새 이름을 붙이지 않는다.

## 명령

아래 전부 현재 동작한다. DoD 판정은 항상 **루트 실행** 기준이다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env   # Java 25.0.3-amzn · Maven 3.9.14 (.sdkmanrc)
mvn verify                             # 3모듈 컴파일 + 테스트 + jar (app은 Boot repackage fat jar)
mvn test -pl solver-core -Dtest=ArchitectureRulesTest   # 단일 테스트 — 모듈(-pl)까지 지정할 것
mvn spring-boot:run -pl app            # 앱 기동
curl -s localhost:8080/actuator/health # {"groups":[...],"status":"UP"}
```

`mvn -o`(오프라인)도 동작한다 — 의존성은 캐시돼 있다. shade 플러그인은 Stage 0에서 제거됐다.

의존성 점검(Stage 0 DoD V2·V4)은 **`-q`를 붙이지 않는다** — 아래 함정 절 참고:

```bash
mvn dependency:list -DincludeGroupIds=com.google.cloud \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — GCP 0건"
mvn dependency:list -pl solver-core -DincludeScope=compile \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — core compile 의존 0건"
```

## 설계 문서 = 권위

읽는 순서이자 충돌 시 우선순위:

| # | 문서 | 소유 내용 |
|---:|---|---|
| 1 | `docs/master-design.md` | 목표·전체 흐름·핵심 결정·범위·완료 기준 |
| 2 | `docs/domain-design.md` | 배차 규칙의 **의미** (입력·정규화·이동표·전파·평가·ALNS·재검증·결과) |
| 3 | `docs/architecture-design.md` | 모듈·경계 규칙·앱 구조·S3 배치·ECS 배포 |
| 4 | `docs/implementation-plan.md` | Stage 0–8과 단계별 DoD |
| 5 | `docs/implementation/stage-NN-*.md` | Stage별 구현 직전 상세(파일·클래스·시그니처·테스트). Stage 0–8 전부 추적됨 |

Stage 문서는 **구현 계약**이다 — 표·코드 블록에 없는 선택(다른 좌표·버전·추가 파일)은 하지 않고,
바꿔야 하면 **문서를 먼저 개정**한 뒤 구현한다(각 문서 frontmatter `revisions`에 한 줄).

`docs/deprecated/`는 **효력 없음** — 결정 등록부(A\*/D\*/O\*), gate/evidence 완료 판정,
YAML 2층 카탈로그, 15-phase 구현 세트는 전부 폐기된 체계다. 현행 문서와 충돌하면 항상 현행이 이긴다.

## 작업 방식 — 손대기 전에

LLM이 자주 저지르는 네 가지(추측·과잉 구현·주변 손대기·모호한 완료)를 막는 규칙과,
설명을 요구받았을 때 어떻게 진행할지의 규칙이다.
**충돌하면 위 권위 표와 stage 문서가 이긴다** — 여기 적힌 건 문서가 정하지 않은 영역의 기본값이다.
오타 수정·한 줄 변경 같은 사소한 작업까지 이 절을 형식대로 밟으라는 뜻은 아니다(판단으로 넘긴다).

### 1. 추측하지 않는다 — 막히면 문서부터

- 해석이 갈리면 **먼저 권위 문서를 찾는다**(master → domain → architecture → plan → stage 순).
  문서가 정해 둔 것을 사용자에게 되묻지 않는다.
- 문서가 정하지 않은 것만 묻는다. **조용히 한쪽 해석을 골라 구현하지 않는다** —
  해석이 둘이면 둘 다 말하고, 어느 쪽으로 갈지 정한 뒤 시작한다.
- 전제는 밖으로 꺼낸다. "아마 이럴 것이다"로 코드를 쌓기 전에 그 전제를 한 줄로 적는다.
- 더 단순한 길이 보이면 말한다. 근거가 있으면 반대 의견을 낸다 — 동의부터 하고 시작하지 않는다.

### 2. 최소 구현 — 요청받은 것만

**Stage 문서는 구현 계약이다**(위 절) — 표·코드 블록에 없는 파일·타입·필드는 만들지 않는다.
문서가 다루지 않는 범위에서도 같은 태도를 유지한다:

- 요청에 없는 기능·옵션·확장점을 미리 넣지 않는다. "나중에 쓸지 몰라서"는 이유가 아니다.
- 사용처가 하나인 코드에 인터페이스·전략·팩토리를 두지 않는다.
- 일어날 수 없는 경우의 예외 처리를 만들지 않는다 (불변식으로 막히는 상태는 방어 코드가 아니라 불변식으로 다룬다).
- 200줄을 썼는데 50줄로 되는 일이면 다시 쓴다.

### 3. 수술적 변경 — 내가 건드린 것만

- 인접 코드·주석·포맷을 "개선"하지 않는다. 멀쩡한 코드를 리팩터링하지 않는다.
- 내 취향과 달라도 **주변 스타일에 맞춘다**(주석 밀도·이름 짓기·관용구 포함).
- 관계없는 죽은 코드를 발견하면 **말만 하고 남긴다.** 지우는 건 별건이다.
- 내 변경이 만든 orphan(안 쓰이게 된 import·변수·메서드)은 내가 치운다.
- 판정 기준: 바뀐 줄 하나하나가 요청으로 곧장 이어지는가.

### 4. 검증 가능한 목표 — "동작하게" 대신 T번호·DoD

- 작업을 시작하기 전에 무엇이 통과하면 끝인지 정한다. 이 저장소에는 이미 그 형태가 있다 —
  stage 문서 §7의 **T번호 테스트**와 implementation-plan의 **단계별 DoD**로 환산한다.
- "검증 추가" → 잘못된 입력의 테스트를 먼저 쓰고 통과시킨다.
  "버그 수정" → 재현 테스트를 먼저 쓰고 통과시킨다.
  "리팩터링" → 전후로 같은 테스트가 통과함을 보인다.
- 여러 단계짜리는 단계마다 **확인 명령**을 붙여 계획을 먼저 적는다:
  `1. 이동표 정규화 → 확인: mvn test -pl solver-core -Dtest=TravelMatrixTest`
- 판정은 **루트 `mvn verify`** 기준이고, 단일 테스트는 `-pl`을 반드시 붙인다(§작업 트리의 함정).
  테스트가 깨진 채로 "완료"라고 말하지 않는다 — 실패는 출력과 함께 그대로 보고한다.

### 5. 긴 설명은 방식을 먼저 고르게 한다

사용자가 설명을 요구했고 **답이 길어질 것 같으면**(여러 절·단계·파일·개념에 걸치면)
바로 쏟아내지 말고, 어떤 방식으로 설명할지 **먼저 확인받는다**:

1. **기본 방식** — 한 번에 정리해서 전달한다.
2. **튜터 방식** — 사용자가 따라올 수 있게 조금씩 나눠 설명하고,
   조각마다 이해됐는지 확인한 뒤 다음으로 넘어간다.

- 한두 문단이면 끝나는 질문에는 묻지 않고 그냥 답한다 — 이 확인 자체가 걸림돌이 되면 안 된다.
- 튜터 방식을 고르면 **조각 하나를 끝낼 때마다 멈춘다.** 확인 없이 다음 조각을 이어 붙이지 않는다.
- 설명 방식을 고르는 것이지 내용을 깎는 게 아니다 — 어느 쪽이든 다뤄야 할 범위는 같다.

## 한 건의 요청이 흐르는 길

```text
POST /solves → 규약 JSON 형식·정규화 검증 → S3에 input.json 저장 → 200 + solveKey   ← 동기는 여기까지
             ↓ 같은 프로세스의 executor (비동기)
  adapter(규약→canonical) → 정규화 → 이동표 준비 → Problem 동결
  → 초기해 → ALNS(destroy/repair) → 최선 Solution
  → 재검증: 별도 코드가 캐시 없이 해 전체를 처음부터 재계산
  → PASS면 result.json 저장 + DONE, FAIL이면 **저장 없이** FAILED
GET /solves/{solveKey}[/result] → S3에서 상태·결과 조회
```

S3 배치: `solves/{customerId}/{planId}/{runId}/` 가 solveKey이고 그 아래 `input.json`·
`status.json`·`result.json`. key 조립 규칙은 `storage` 한 곳에만 둔다.

## 모듈 구조 (현행 — Stage 0에서 세워짐, 패키지는 아직 비어 있음)

```text
solver-core/     com.ronext.rpdptw          순수 Java, compile 의존 0 (Spring/Jackson/AWS 금지)
                 패키지: domain · problem · eval · solve · verify
                 의존: problem→domain, eval→{domain,problem}, solve→{domain,problem,eval},
                       verify→{domain,problem,eval} — verify의 solve 참조 금지
solver-profile/  com.ronext.rpdptw.profile  ProfileRegistry(customerId→Profile, 미등록=default)
                 고객 전용 외부 라이브러리는 이 모듈에서만
app/             com.ronext.rpdptw.app      api · run · input · storage — Spring Boot, 배포 단위
```

의존 방향은 `app → solver-profile → solver-core` 한 방향(역방향은 컴파일 불가)이고,
`verify ↛ solve`는 ArchUnit 테스트가 강제한다. 경계를 뚫으려면 pom에 의존성을 추가하는 게 아니라
설계를 바꿔야 한다.

## 기본으로 어기기 쉬운 불변식

전체 18항목은 Domain §13 체크리스트. 특히:

- 배정·제거의 원자 단위는 `Request`(pair) 전체 — pickup만 빼는 연산은 없다.
- 모든 Request는 경로 또는 bank에 **정확히 하나**(XOR). bank에는 ID만 담고 사유 문자열을 넣지 않는다.
- `DELIVERY_ONLY`는 pickup 방문을 만들지 않는다 (출발 적재에만 참여). `PICKUP_ONLY`는 delivery 방문을 만들지 않는다 (`endDepot` 도착에서 하차). 가짜 depot 방문·가상 depot으로 흉내 금지.
- `startDepot`/`endDepot`은 각각 optional. 정규화는 start 부재를 단일 차고로 채우지 않는다 (현 규약 wire 채움은 Stage 6 adapter). `DELIVERY_ONLY`는 start 있는 차만, `PICKUP_ONLY`는 end 있는 차만 호환.
- `Problem`은 동결 — 탐색이 문제·이동표를 고치면 버그. `Profile`과 탐색 예산은 `Problem`에 담지 않고
  인자로 전달한다 (탐색·재검증에 **같은 profile 인스턴스**).
- 비교는 `long[]` 사전식 하나뿐. 가중합·Big-M으로 축을 뭉개지 않고, hard 위반을 감점으로 상쇄하지 않는다.
- core에 `if (customerId == ...)` 금지 — 고객 차이는 profile 구현으로만.
- 재검증은 탐색 예산(시간·step 한도)을 읽지 않고, **통과하지 못한 배차안은 결과로 저장하지 않는다**
  (유일한 발행 규칙).
- 접수 HTTP 요청 안에서 ALNS 완주를 기다리지 않는다.
- 단위: 무게·부피는 ×1000 FLOOR한 `long`, 거리 meter·시간 초 정수. double로 근사한 뒤 변환 금지.
- 시간창(주문·차고·근무)은 **날마다 반복**되고 정규화가 절대 창 목록(`List<TimeWindow>`)으로 펼친다
  (Domain §3.2, 2026-08-10 D4). 이동·서비스는 통째로 한 근무창 안에 들어가야 하고, 창 끝을 넘으면
  **다음 창으로 미룬다**(불가 판정이 아니다). 차고 창은 **있는** 출·도착 순간에 적용 → `DEPOT_WINDOW`.
  모든 소요 시간은 **두 시각의 차**로 잰다 — 초를 세면 창마다 1초 어긋난다.

## 작업 트리의 함정

- **`mvn -q dependency:list`는 항상 빈 출력이다.** `dependency:list`는 결과를 INFO로 찍는데 `-q`가
  그걸 죽인다 — 의존이 있든 없든 빈 출력이라 "GCP 0건·core 순수" 검사가 **무조건 통과처럼 보인다.**
  `-q` 없이 좌표를 grep한다 (위 명령 절). 검사가 살아 있는지는 scope 제한을 푼 대조군
  (`mvn dependency:list -pl solver-core | grep -cE '^\[INFO\]\s+\S+:\S+:\S+:'` → 0이 아니어야 함)으로 본다.
- **루트에서 `mvn test -Dtest=X`는 BUILD FAILURE다.** 3모듈이 된 뒤로는 패턴이 안 맞는 모듈
  (테스트가 1개뿐인 `solver-profile` 등)에서 surefire가 "No tests matching pattern"으로 죽는다.
  `-pl <모듈>`을 같이 주거나 `-Dsurefire.failIfNoSpecifiedTests=false`를 붙인다.
- `rpdptw/`·`adapters/`·`build/`·`apps/`·`gcp/`·`src/`·`.serverless/`·`node_modules/`·`Dockerfile` —
  **전부 삭제됐다** (Stage 0). 옛 문서·대화에서 이 경로가 보이면 지금은 존재하지 않는 것이다.
  Dockerfile은 Stage 7에서 새로 쓴다.
- ArchUnit 룰(`verify ↛ solve`)은 `verify`에 `solve`를 참조할 클래스가 아직 없어 지금은 공회전한다
  (`allowEmptyShould(true)`). 룰 자체가 살아 있음은 확인해 뒀지만(위반 클래스를 넣으면 BUILD FAILURE),
  **green이 곧 경계 준수의 근거는 아니다** — `verify` 구현이 생기는 Stage 5부터 의미가 붙는다.
- `data/win_poc_case_floor.json` (주문 452·차량 31)이 1차 성공 기준의 실행 fixture다.
  규약 원본은 `data/ro_input_json_spec.pdf`, 비교 대상인 기존 엔진(Win) 결과는 `data/alns_result.csv`,
  거리표 소수 FLOOR 처리는 `scripts/floor_win_poc_matrix.py`가 했다. 수치 필드의 문자열→number
  정정(2,813필드, Domain §3.1 2026-08-12 확정)은 `scripts/numify_win_poc_fixture.py`가 했다 —
  원본 `win_poc_case.json`은 문자열 그대로다(수신 원형 보존, 미접수 입력).

## 범위 밖 (하지 않기로 확정)

multi-trip(`multiRotation`이 `2` 이상·`-1`이면 접수 거부. **`0`·`1`은 1바퀴라 통과** — 숫자는
바퀴 수를 센다. 규약 PDF 문면은 복귀 횟수로 읽히니 주의, Domain §2.5) · MIP 재조합 ·
RDB·Redis·SQS·Step Functions·Lambda ·
입력 스키마 다중 버전 운영 · 고객별로 갈라진 canonical·`Problem`·adapter ·
탐색 파라미터의 문서 확정(Stage 4·8에서 실험으로 정한다).
