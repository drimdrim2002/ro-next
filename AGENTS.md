# ro-next

배차 최적화(RPDPTW) 서비스. 설계 문서가 권위이고 코드는 그 계약의 구현이다.
always-on 규칙은 이 파일 하나다. `CLAUDE.md`는 `@AGENTS.md` 포인터다. 본문을 복제하지 않는다.

## 해당할 때만 연다

| 작업 | 파일 |
|---|---|
| 설계 문서대로 구현 (Stage, T번호, DoD) | `.agents/skills/implement-from-design/SKILL.md` |
| Java 코드를 수정 | `.agents/skills/java-clean-code/SKILL.md` |
| 어디를 고칠지 모름 | `.agents/skills/project-map/SKILL.md` |

## 권위

충돌 시 위가 이긴다.

| # | 문서 | 소유 |
|---:|---|---|
| 1 | `docs/master-design.md` | 목표·전체 흐름·범위 |
| 2 | `docs/domain-design.md` | 배차 규칙의 의미 |
| 3 | `docs/architecture-design.md` | 모듈·경계·앱·S3·ECS |
| 4 | `docs/implementation-plan.md` | Stage 0–8과 단계별 DoD |
| 5 | `docs/implementation/stage-NN-*.md` | 파일·시그니처·테스트 계약 |

Stage 문서는 구현 계약이다. 표·코드 블록에 없는 파일·타입·필드는 만들지 않는다. 바꿔야 하면 문서를 먼저 개정하고 그 문서 frontmatter `revisions`에 한 줄.

`docs/deprecated/`는 효력 없음. 현행 문서와 충돌하면 현행이 이긴다.

문서·커밋 메시지는 한국어. 용어(`Request` / pair / `Problem` / `Solution` / bank / profile / 재검증 / solveKey)는 문서 표기를 그대로 쓴다.

## 작업 방식

LLM이 자주 저지르는 네 가지(추측·과잉 구현·주변 손대기·모호한 완료)와, 긴 설명을 어떻게 할지의 기본값이다.
충돌하면 위 권위 표와 stage 문서가 이긴다. 오타·한 줄 수정까지 이 절을 형식대로 밟지는 않는다.

### 1. 추측하지 않는다 — 막히면 문서부터

- 해석이 갈리면 먼저 권위 문서를 찾는다 (master → domain → architecture → plan → stage).
  문서가 정해 둔 것을 사용자에게 되묻지 않는다.
- 문서가 정하지 않은 것만 묻는다. 조용히 한쪽 해석을 골라 구현하지 않는다 —
  해석이 둘이면 둘 다 말하고, 어느 쪽으로 갈지 정한 뒤 시작한다.
- 전제는 밖으로 꺼낸다. 코드를 쌓기 전에 그 전제를 한 줄로 적는다.
- 더 단순한 길이 보이면 말한다. 근거가 있으면 반대 의견을 낸다 — 동의부터 하고 시작하지 않는다.

### 2. 최소 구현 — 요청받은 것만

- 요청에 없는 기능·옵션·확장점을 미리 넣지 않는다. "나중에 쓸지 몰라서"는 이유가 아니다.
- 사용처가 하나인 코드에 인터페이스·전략·팩토리를 두지 않는다.
- 일어날 수 없는 경우의 예외 처리를 만들지 않는다 (불변식이 막는 상태는 불변식으로 다룬다).
- 200줄을 썼는데 50줄로 되는 일이면 다시 쓴다.

### 3. 수술적 변경 — 내가 건드린 것만

- 인접 코드·주석·포맷을 "개선"하지 않는다. 멀쩡한 코드를 리팩터링하지 않는다.
- 내 취향과 달라도 주변 스타일에 맞춘다 (주석 밀도·이름 짓기·관용구 포함).
- 관계없는 죽은 코드를 발견하면 말만 하고 남긴다. 지우는 건 별건이다.
- 내 변경이 만든 orphan(안 쓰이게 된 import·변수·메서드)은 내가 치운다.
- 판정 기준: 바뀐 줄 하나하나가 요청으로 곧장 이어지는가.

### 4. 검증 가능한 목표 — "동작하게" 대신 확인 가능한 끝

- 시작 전에 무엇이 통과하면 끝인지 정한다. Stage 작업이면 문서 §7 T번호와 plan의 단계별 DoD.
- "검증 추가" → 잘못된 입력의 테스트를 먼저 쓰고 통과시킨다.
  "버그 수정" → 재현 테스트를 먼저 쓰고 통과시킨다.
  "리팩터링" → 전후로 같은 테스트가 통과함을 보인다.
- 여러 단계짜리는 단계마다 확인 명령을 붙인다:
  `1. 이동표 정규화 → 확인: mvn test -pl solver-core -Dtest=TravelMatrixTest`
- 판정은 **루트 `mvn verify`**. 단일 테스트는 `-pl`을 붙인다. 테스트가 깨진 채로 완료라고 하지 않는다 — 실패는 출력과 함께 보고한다.

### 5. 긴 설명은 방식을 먼저 고르게 한다

사용자가 설명을 요구했고 답이 길어질 것 같으면 (여러 절·단계·파일·개념) 바로 쏟지 말고 고른다.

1. **기본 방식** — 한 번에 정리해서 전달한다.
2. **튜터 방식** — 조각마다 이해됐는지 확인한 뒤 다음으로 간다.

한두 문단이면 묻지 않고 답한다. 튜터를 고르면 조각 하나마다 멈춘다. 어느 쪽이든 다뤄야 할 범위는 같다.

## 모듈 경계

```text
solver-core     com.ronext.rpdptw          순수 Java, compile 의존 0 (Spring/Jackson/AWS 금지)
solver-profile  com.ronext.rpdptw.profile  고객 Profile. 외부 라이브러리는 여기만
app             com.ronext.rpdptw.app      api · run · input · storage. Spring Boot 배포 단위
```

의존은 `app → solver-profile → solver-core` 한 방향. `verify ↛ solve` (ArchUnit). 경계를 뚫으려면 pom이 아니라 설계를 바꾼다. 패키지 지도는 `.agents/skills/project-map/SKILL.md`.

## 잘 깨지는 불변식

전체는 Domain §13. 특히:

- 배정·제거의 원자는 `Request`(pair) 전체. pickup만 빼는 연산은 없다.
- 모든 Request는 경로 또는 bank에 정확히 하나 (XOR). bank에는 ID만. 사유 문자열을 넣지 않는다.
- `DELIVERY_ONLY`는 pickup 방문을 만들지 않는다. `PICKUP_ONLY`는 delivery 방문을 만들지 않는다. 가짜 depot 금지.
- `startDepot`/`endDepot`은 각각 optional. 정규화는 start 부재를 단일 차고로 채우지 않는다.
- `Problem`은 동결. `Profile`과 탐색 예산은 인자로 전달한다 (탐색·재검증에 같은 profile).
- 비교는 `long[]` 사전식 하나. 가중합·Big-M으로 축을 뭉개지 않는다. hard 위반을 감점으로 상쇄하지 않는다.
- core에 `if (customerId == ...)` 금지. 고객 차이는 profile 구현으로만.
- 재검증은 탐색 예산(시간·step 한도)을 읽지 않는다. 통과하지 못한 배차안은 결과로 저장하지 않는다.
- 접수 HTTP 요청 안에서 ALNS 완주를 기다리지 않는다.
- 무게·부피는 ×1000 FLOOR한 `long`, 거리 meter·시간 초 정수. double로 근사한 뒤 변환 금지.
- 시간창은 날마다 반복되고 정규화가 절대 창 목록으로 펼친다. 창 끝을 넘으면 다음 창으로 미룬다 (불가 판정이 아니다). 소요는 두 시각의 차. Domain §3.2·§7.1.

## 명령

DoD 판정은 항상 루트 실행.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env   # Java 25.0.3-amzn · Maven 3.9.14 (.sdkmanrc)
mvn verify                             # 3모듈 컴파일 + 테스트 + jar
mvn test -pl solver-core -Dtest=ArchitectureRulesTest   # 단일 테스트는 -pl
mvn spring-boot:run -pl app
```

의존성 점검에 `-q`를 붙이지 않는다. `dependency:list`는 결과를 INFO로 찍어 `-q`면 무조건 빈 출력이다.

```bash
mvn dependency:list -DincludeGroupIds=com.google.cloud \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — GCP 0건"
mvn dependency:list -pl solver-core -DincludeScope=compile \
  | grep -E '^\[INFO\]\s+\S+:\S+:\S+:' || echo "PASS — core compile 의존 0건"
```

함정:

- 루트 `mvn test -Dtest=X`는 패턴이 안 맞는 모듈에서 BUILD FAILURE다. `-pl`을 붙인다.
- `mvn -q dependency:list`는 항상 빈 출력이다. 검사가 살아 있는지는 scope를 푼 대조군
  (`mvn dependency:list -pl solver-core | grep -cE '^\[INFO\]\s+\S+:\S+:\S+:'`)이 0이 아니어야 한다.
- 규모 테스트 `InitialSolutionScaleTest`, `AlnsScaleTest`, `ZoneQuotaAllocationScaleTest`와 app `WinPocAlnsTest`는 수십 초다. 빼려면 `-pl`과 함께 `-Dtest='!…'` 또는 `-Dsurefire.failIfNoSpecifiedTests=false`.

## 범위 밖

multi-trip (`multiRotation`이 `2` 이상·`-1`이면 접수 거부. `0`·`1`은 1바퀴라 통과 — 숫자는 바퀴 수. 규약 PDF 문면은 복귀 횟수로 읽히니 주의, Domain §2.5) · MIP 재조합 · RDB·Redis·SQS·Step Functions·Lambda · 입력 스키마 다중 버전 · 고객별로 갈라진 canonical·`Problem`·adapter.

## 세션에서

요청에 없는 커밋·푸시·문서 개정은 하지 않는다.
