# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 이 저장소의 성격 — 설계가 본체, 코드는 폐기 예정 placeholder

배차 최적화(RPDPTW) 서비스. **확정 설계와 디스크의 코드가 서로 다르다.** `src/`를 읽고 그 방식을
따라가면 안 된다.

- **확정 설계**: AWS ECS Fargate 위 단일 Spring Boot 서비스, 저장은 S3만.
- **현 코드**: GCP Cloud Run/Workflows 실험 잔재 (`com.ronext.optimizer`, 수제 `HttpServer`,
  합성 데모 `AlnsBatchEngine`). Stage 0에서 전부 삭제·재구성한다. placeholder가 도는 것은
  솔버가 동작한다는 근거가 아니다.
- 문서·커밋 메시지는 한국어다. 용어(`Request`/pair/`Problem`/`Solution`/bank/profile/재검증/
  solveKey)는 문서 표기를 그대로 쓴다 — 같은 개념에 새 이름을 붙이지 않는다.

## 명령

현재 저장소 상태(단일 모듈 placeholder)에서 실제로 동작하는 것:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env   # Java 25.0.3-amzn · Maven 3.9.14 (.sdkmanrc)
mvn verify                             # 컴파일 + 테스트 + shade jar (의존성 캐시됨 — mvn -o 도 동작)
mvn test -Dtest=AlnsBatchEngineTest    # 단일 테스트
```

Stage 0 이후에도 같은 `mvn verify`가 그대로 쓰이며, 그때는 3모듈 전체를 돈다
(DoD 판정은 항상 **루트 실행** 기준).

아래는 **Stage 0(3모듈 재구성) 이후에야 존재하는** 명령이다 — 지금은 `app` 모듈도
Spring Boot도 없으므로 실패한다:

```bash
mvn spring-boot:run -pl app            # 앱 기동
curl localhost:8080/actuator/health
mvn dependency:list -pl solver-core    # compile/runtime 의존이 0건이어야 함
```

## 설계 문서 = 권위

읽는 순서이자 충돌 시 우선순위:

| # | 문서 | 소유 내용 |
|---:|---|---|
| 1 | `docs/master-design.md` | 목표·전체 흐름·핵심 결정·범위·완료 기준 |
| 2 | `docs/domain-design.md` | 배차 규칙의 **의미** (입력·정규화·이동표·전파·평가·ALNS·재검증·결과) |
| 3 | `docs/architecture-design.md` | 모듈·경계 규칙·앱 구조·S3 배치·ECS 배포 |
| 4 | `docs/implementation-plan.md` | Stage 0–8과 단계별 DoD |
| 5 | `docs/implementation/stage-NN-*.md` | Stage별 구현 직전 상세(파일·클래스·시그니처·테스트). 아직 미추적 작업본 |

**이 우선순위의 예외 하나 (중요):** 모듈 개수는 **3개**(`solver-core`·`solver-profile`·`app`)가
현행이다. 2026-08-10 3계층 개정 때 Architecture·Plan·루트 README만 갱신되고
Master §3 결정 #10과 `docs/README.md`는 "모듈 2개"로 남았다. 이 항목에 한해 Master가 아니라
Architecture를 따른다.

`docs/deprecated/`는 **효력 없음** — 결정 등록부(A\*/D\*/O\*), gate/evidence 완료 판정,
YAML 2층 카탈로그, 15-phase 구현 세트는 전부 폐기된 체계다. 현행 문서와 충돌하면 항상 현행이 이긴다.

## 한 건의 요청이 흐르는 길

```text
POST /solves → 규약 JSON 형식 검증 → S3에 input.json 저장 → 200 + solveKey   ← 동기는 여기까지
             ↓ 같은 프로세스의 executor (비동기)
  adapter(규약→canonical) → 정규화 → 이동표 준비 → Problem 동결
  → 초기해 → ALNS(destroy/repair) → 최선 Solution
  → 재검증: 별도 코드가 캐시 없이 해 전체를 처음부터 재계산
  → PASS면 result.json 저장 + DONE, FAIL이면 **저장 없이** FAILED
GET /solves/{solveKey}[/result] → S3에서 상태·결과 조회
```

S3 배치: `solves/{customerId}/{planId}/{runId}/` 가 solveKey이고 그 아래 `input.json`·
`status.json`·`result.json`. key 조립 규칙은 `storage` 한 곳에만 둔다.

## 목표 모듈 구조 (Stage 0 이후)

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

전체 16항목은 Domain §13 체크리스트. 특히:

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

## 작업 트리의 함정

- `rpdptw/`·`adapters/`·`build/`·`apps/` — **소스가 없다.** 폐기된 다중 모듈 시도의 `target/` jar와
  surefire 리포트만 남은 미추적 디렉터리다. grep에 잡히면 노이즈이고 Stage 0에서 삭제한다.
- `gcp/`(추적됨)·pom `<description>`의 "Cloud Run and Google Cloud Workflows" — 폐기된 GCP 실험.
- `.serverless/`·`node_modules/` — 옛 Serverless Framework(Lambda) 실험 잔재. Node 프로젝트가 아니다.
- `data/win_poc_case_floor.json` (주문 452·차량 31)이 1차 성공 기준의 실행 fixture다.
  규약 원본은 `data/ro_input_json_spec.pdf`, 비교 대상인 기존 엔진(Win) 결과는 `data/alns_result.csv`,
  거리표 소수 FLOOR 처리는 `scripts/floor_win_poc_matrix.py`가 했다.

## 범위 밖 (하지 않기로 확정)

multi-trip(`multiRotation != 0`은 접수 거부) · MIP 재조합 · RDB·Redis·SQS·Step Functions·Lambda ·
입력 스키마 다중 버전 운영 · 고객별로 갈라진 canonical·`Problem`·adapter ·
탐색 파라미터의 문서 확정(Stage 4·8에서 실험으로 정한다).
