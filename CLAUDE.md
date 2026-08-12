# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 이 저장소의 성격 — 설계가 본체, 코드는 아직 빈 뼈대

배차 최적화(RPDPTW) 서비스. **Stage 0 완료 (2026-08-10)** — 구 placeholder(`com.ronext.optimizer`,
수제 `HttpServer`, 합성 데모 `AlnsBatchEngine`)와 GCP 잔재는 삭제됐고, 디스크의 코드는 이제
확정 설계와 같은 3모듈 구조다. 다만 **뼈대일 뿐 솔버 로직은 아직 0**이다.

- **확정 설계**: AWS ECS Fargate 위 단일 Spring Boot 서비스, 저장은 S3만.
- **현 코드**: 4개 pom + `package-info.java` 10개 + `RoNextApplication` + `application.yml` +
  테스트 클래스 2개(메서드 3개)가 전부.
  `domain`·`problem`·`eval`·`solve`·`verify`·`profile`·`api`·`run`·`input`·`storage`는 **전부 빈 패키지**다
  — 도메인 타입을 grep해서 안 나오는 게 정상이고, 아직 안 만든 것이지 다른 데 있는 게 아니다.
- **다음 작업은 Stage 1** (canonical 입력·정규화, solver-core). 구현 직전 상세는
  `docs/implementation/stage-01-canonical-input-normalization.md`.
- 문서·커밋 메시지는 한국어다. 용어(`Request`/pair/`Problem`/`Solution`/bank/profile/재검증/
  solveKey)는 문서 표기를 그대로 쓴다 — 같은 개념에 새 이름을 붙이지 않는다.

## 명령

아래 전부 현재 동작한다 (Stage 0 완료 상태). DoD 판정은 항상 **루트 실행** 기준이다.

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
- `DELIVERY_ONLY`는 pickup 방문을 만들지 않는다 (출발 적재에만 참여). 가짜 depot 방문으로 흉내 금지.
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
  **다음 창으로 미룬다**(불가 판정이 아니다). 차고 창은 출발·복귀 두 순간에 적용 → `DEPOT_WINDOW`.
  모든 소요 시간은 **두 시각의 차**로 잰다 — 초를 세면 창마다 1초 어긋난다.

## 작업 트리의 함정

- **`mvn -q dependency:list`는 항상 빈 출력이다.** `dependency:list`는 결과를 INFO로 찍는데 `-q`가
  그걸 죽인다 — 의존이 있든 없든 빈 출력이라 "GCP 0건·core 순수" 검사가 **무조건 통과처럼 보인다.**
  `-q` 없이 좌표를 grep한다 (위 명령 절). 검사가 살아 있는지는 scope 제한을 푼 대조군
  (`mvn dependency:list -pl solver-core | grep -cE '^\[INFO\]\s+\S+:\S+:\S+:'` → 0이 아니어야 함)으로 본다.
- **루트에서 `mvn test -Dtest=X`는 BUILD FAILURE다.** 3모듈이 된 뒤로는 패턴이 안 맞는 모듈
  (`solver-profile`은 테스트 0개)에서 surefire가 "No tests matching pattern"으로 죽는다.
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
