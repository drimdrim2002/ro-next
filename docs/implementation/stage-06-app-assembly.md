---
title: Stage 6 — 앱 조립 (상세 구현 설계)
stage: 6
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../architecture-design.md (§3 실행 흐름·상태, §4 Spring 규칙, §6 로컬 테스트, §2 모듈·패키지)
  - ../domain-design.md (§2 입력 계약, §11 결과 JSON, §12 오류 분류)
  - ../../data/ro_input_json_spec.pdf + win_poc_case.json·win_poc_case_floor.json (wire 실물)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준, §4.4 app pom, §11 Q2)
  - stage-01-canonical-input-normalization.md (§2.3 raw 운반체, §3 PlanNormalizer·InputException, §9 Q1·Q4·Q6)
  - stage-04-initial-solution-and-alns.md (§3.2 AlnsSolver·AlnsConfig·AlnsResult, §9 Q1)
  - stage-05-verification-and-result.md (§2 SolutionVerifier, §4 SolveResult·ResultAssembler·RunStamp, §9 인계)
revisions:
  - 2026-08-10 최초 작성
  - 2026-08-10 3계층 반영 — app→solver-profile 의존, profile resolve·전달을 app이 소유,
    탐색 예산(idle 포함) 설정 키 추가
  - 2026-08-10 Jackson 3 기준 정합 — `ObjectMapper` → `JsonMapper` (`tools.jackson`), Stage 0 §4.4
  - 2026-08-10 §10 Q1을 Plan §2.1 D1으로, Q2·Q4·Q5를 D2(wire 협의)로 이관 — 포인터와
    D1의 새 사실(규약 열거 정의 = 차고 복귀 횟수)만 반영. 설계·매핑표·테스트 무변경
  - 2026-08-10 D4 확정 반영 — §4.2·§4.3·§4.4 매핑표의 "앵커링은 정규화"를 "전개는 정규화"로
    (Domain §3.2), §10 Q5에 `driverRestTimeRatio` ≠ `interWorkWindowRestTime` 주의 추가.
    adapter는 여전히 `LocalTime` 한 쌍을 넘길 뿐이라 매핑 구조·테스트 무변경
  - 2026-08-10 **D1 확정 반영 (§10 Q1 해소) + 차량별 `trips`** — ① 접수 게이트 §3.1-2를
    집합 판정(통과 = `{0,1}`, `-1`·`2 이상` = 422, `≤ -2` = 400)으로 반전 · E1 뒤집고
    E1b·E1c 신설 · T4에 대조군(`"1"` → 200) 추가 · T13의 "실행 불가" 해제 · DoD 선행 조건 해제.
    ② §4.4에 차량 `trips` 행("있으면 읽는다" — 현행 규약엔 없음), §4.6 `trips`에 "전체 기본값" 명시.
    adapter는 값을 옮기기만 하고 우선순위 접기는 정규화가 한다 — 매핑 외 구조·테스트 무변경
  - 2026-08-11 접수 깊이 확정 (§10 Q3 해소) — §3.1에 절차 2b(정규화 검증 → 4xx, 저장 없음)
    추가, N1·E3·T4·T8(a) 동기 (Architecture §3.1 개정·Domain §12 이행). 결과 경로 시각 반영 —
    §5 잠정안에 depotDeparture 추가 (Domain §11.1·Stage 5 §4.1). 그 외 정리: sources의
    stage-00 절 번호 정정(§4.3→§4.4), 중복 번호 T14를 T16으로 개번(N6 참조 포함),
    T1 "options 7키"를 "매핑 7키(wire 10키)"로, §8 말미 문구를 Plan §1 DoD 편입으로 갱신
  - 2026-08-11 Stage 1 유예 반영 — §4.4 adapter 매핑에서 차량 `trips`·`vhclOwnTyp`·치수 3필드,
    §4.3 `depot.taskTime`을 "무시"로 · §4.6 `trips`를 전체 설정 하나로 · 접수 4xx 예시에서
    `vhclOwnTyp` 제거(읽지 않는 필드라 예시가 될 수 없다) · `taskTime ×qty` 포인터를 Domain으로
  - 2026-08-12 Domain 2026-08-12 개정(self arc sentinel·startDepot 부재 규칙·수치 number
    인코딩) 정합 — §4 공통 규칙 1을 "문자열·숫자 둘 다 수용"에서 **number만 수용(문자열 수치 =
    INVALID_INPUT)**으로 반전 · §4.5에 self arc 행 배제 주석 · E1\~E1c·E3·T1·T2·T4·T13의
    fixture 값 표기를 number로(원본 win_poc_case.json은 문자열 보존 = 미접수) · N2에 인코딩
    검증 사유
  - 2026-08-12 감사 후속 인터뷰 ①·② 정합 — ① 소수 거부 = **표기 기준**(Domain §3.1 확정):
    §4 공통 규칙 1의 정수 필드 소수 거부에 "값이 정수라도 소수점 표기면 거부" 명시,
    §4.5 D/U 행의 검사 명칭을 소수 표기 검사로 · ② §4.4 `maxDriveDist`/`maxDriveDistc`
    별칭의 정본을 Domain §2.4로 명시, 둘 다 오면 `INVALID_INPUT` 추가 · ③ itemId 폴백을
    `prodId`에서 **`orderId`로 교체** (§4.4·E4·T2 — prodId 폴백은 소유자 확정이 아닌
    작성 시 발명이라 폐기, Domain §2.3 정본) · ④ options 3키 무시 확정 반영
    (§4.6·E19·Q5 — `driverRestTimeRatio`는 0 아닌 값도 거부 안 함, Domain §2.5) ·
    ⑤ 탐색 예산 운반 경로 확정 — `parse`가 **`ParseResult(PlanInput, OptionalLong
    secondsSpentLimit)` 봉투**를 반환 (무상태·파싱 1회 — §2.2·§3.1·§3.2·§4.6)
  - 2026-08-13 감사 결함 정정 (분할 8 #1·#6·#7·#8·#9·#10, 분할 7 F5 연쇄) — §4.6
    `multiRotation` 행의 생략된 주어 명시(`OptionsInput`에는 담는다, 미보관 주어는 canonical
    `Plan` — 정본 Stage 1 §2.3) · §4.5 `C` 행의 주어 명시(wire에는 전 행 존재, 운반체에
    필드를 안 만든다) · §3.1-2b의 빈 `zoneIds` 근거를 Domain §12에서 Stage 1 §4 절차 5로 ·
    §3.1-3에 `Optional.ofNullable` 명시 · Stage 5 `SearchBudget.termination` 삭제 연쇄 반영
    (`budgetOf` 시그니처·§3.2-i·§5 예시 — 종료 사유는 stats라 run 메타에 안 실린다,
    Domain §11.1) · §10 Q3 셀의 Domain §12 인용에 "당시 문면" 표시 · §10 서두를 README
    공통 규칙(표시하고 남김)으로. 매핑·절차 자체는 무변경
  - 2026-08-13 감사 결함 정정 (분할 8 §1-3) — §5 서두 "필드명은 §11.1의 이름 그대로" 거짓
    교정: §11.1은 의미 정본, 필드명·형태는 §11.2(Plan D2) 소유 — wire 표기는 이 절 잠정안
    (Domain §11.1 2026-08-13 의미 정본 좁히기의 후속)
  - 2026-08-14 Domain `startDepot` optional + `PICKUP_ONLY` — §4.4 현 규약 CVRPTW 채움
    (start 키 없음 + depot 1개 → 그 ID를 VehicleInput에). 정규화는 채우지 않는다.
    §4.3 pickup만 있는 wire → PICKUP_ONLY (현 규약은 계속 delivery only)
  - 2026-08-15 §4.2 차고 창 매핑 주석을 Domain §7.1에 맞춤 (있는 출·도착만)
  - 2026-08-15 §4.4 차량 `vehicleFeature` 행의 "비교는 문자 그대로" 삭제 — 2026-08-12
    E26(부재·`"ALL"` = 전 차급 접기)으로 폐기된 규칙의 잔재였다. adapter는 값을 그대로
    넘기고 접기는 Stage 1이 한다 (구역 `"ALL"`도 같다 — Domain §3.4 2026-08-15). 매핑 무변경
  - 2026-08-16 §4.6 `VehicleMaxStopCount` 행·§4 공통 규칙 5: 정차 한도 접기는 정규화의
    **차량 > 전역 > 없음** (Domain §2.6). adapter는 값을 옮기기만 한다. 매핑 무변경
  - 2026-08-16 itemId 폴백 수행 위치를 Stage 1 정규화로 명시 (§4.3·E4). adapter는 blank를
    그대로 `ItemInput`에 담는다. 규칙(orderId 사용)은 Domain §2.3 그대로
---

# Stage 6 — 앱 조립

app 모듈의 `api`·`run`·`input`·`storage` 네 패키지를 채워 solver-core(Stage 1~5)를
동작하는 서비스로 조립한다. 주 근거: [Architecture §3·§4·§6](../architecture-design.md).
Stage 1의 `PlanInput` 운반체·`PlanNormalizer`·`InputException`, Stage 4의
`AlnsSolver`·`AlnsConfig`·`AlnsResult`, Stage 5의 `SolutionVerifier`·`ResultAssembler`·
`SolveResult`·`RunStamp`를 그대로 잇는다 — 같은 개념에 새 이름을 짓지 않는다.
solver-core는 이 Stage에서 수정하지 않는다.

**DoD** ([Plan Stage 6](../implementation-plan.md)): fake 저장소로 e2e 통합 테스트 —
POST 접수 → DONE까지 → GET 결과 · win_poc_case_floor.json 접수·완주 (성공 기준 §0 달성 시점).
**두 문장 모두 선행 조건 없이 달성 가능하다** — 종전 두 번째 문장을 막던 multiRotation 충돌은
[Plan §2.1 D1](../implementation-plan.md)이 닫았다 (2026-08-10: 값은 바퀴 수, fixture의 `1`은
1바퀴라 그대로 접수된다. §10 Q1).

핵심 구도 — Architecture §1의 한 장 그림을 클래스로 결선한다:

```text
[호출 시스템] ─POST /solves (규약 JSON)─▶ [api] SolveController → SolveService.accept (§3.1)
    PlanJsonAdapter.parse → multiRotation 게이트 → SolveKey.issue
    → putInput + putStatus(RECEIVED) → SolveExecutor.submit → 200 {solveKey}
                          │ (in-process 큐 — 같은 프로세스, Architecture §3.1-5)
                          ▼
[run] SolveExecutor: RUNNING 기록 + heartbeat 시작 ──▶ SolveRunner.run (§3.2)
    getInput → PlanJsonAdapter → PlanNormalizer → Problem.freeze(plan)
    → profiles.resolve(customerId) = profile   ← 이 run의 유일한 resolve (§3.2 d')
    → AlnsSolver.solve(problem, profile) → best 분해(Map/Set)
    → SolutionVerifier.verify(problem, profile, …)   ← 같은 profile 인스턴스
    → Pass: ResultAssembler.assemble(+RunStamp) → ResultJsonWriter → putResult → DONE
    → Fail·예외: FAILED + 원인 (결과 저장 없음 — Domain §10.2 MUST)
                          │
                    [storage] SolveStore ── S3SolveStore(운영) | LocalSolveStore(fake)
                          ▲
[호출 시스템] ─GET 상태·결과─┘ [api] 조회 + STALE 판정 (§3.4)
```

---

## 1. 파일/클래스 목록

전부 `app/src/main/java/com/ronext/rpdptw/app/` 아래 (Architecture §2, Stage 0 §3.1).
`package-info.java`(Stage 0 생성)는 유지한다.

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `api/SolveController.java` | HTTP 매핑만: POST /solves, GET 상태·결과 | Architecture §3.1·§3.4 |
| `api/SolveService.java` | 접수 절차(§3.1)와 조회 절차(STALE 판정 포함, §3.4) 구현 | Architecture §3.1–§3.4 |
| `api/ApiErrorHandler.java` | `InputException` → 4xx, 그 외 → 5xx 매핑 (@RestControllerAdvice) | Domain §12 |
| `api/SolveAccepted.java` | 접수 응답 record: `solveKey` | Architecture §3.1-6 |
| `api/SolveStatusResponse.java` | 상태 응답 record: state(STALE 포함)·heartbeatAt·error | Architecture §3.4 |
| `api/ApiError.java` | 오류 응답 record: error(kind)·field·message | Domain §12 |
| `input/PlanJsonAdapter.java` | 규약 JSON → `PlanInput` 파싱 (§4 매핑표가 계약) | Domain §2.1, Stage 1 §1.2 |
| `input/ResultJsonWriter.java` | `SolveResult` → result.json 바이트 (§5 wire 잠정안) | Domain §11 |
| `run/SolveExecutor.java` | 고정 스레드풀·큐·heartbeat·최종 상태 기록 | Architecture §3.2 |
| `run/SolveRunner.java` | 한 건의 풀이 파이프라인 (입력 로드 → … → 결과 저장) | Architecture §3.2 |
| `storage/SolveKey.java` | solveKey 발급·검증·객체 key 조립 — key 규칙의 유일한 자리 | Architecture §3.3 |
| `storage/SolveState.java` | enum RECEIVED·RUNNING·DONE·FAILED (STALE은 저장 상태가 아님) | Architecture §3.2–§3.3 |
| `storage/SolveStatus.java` | status.json 내용 record | Architecture §3.3 |
| `storage/SolveStore.java` | 저장 인터페이스 (Architecture §3.5 그대로) | Architecture §3.5 |
| `storage/S3SolveStore.java` | 운영 구현 — S3 SDK는 이 클래스 뒤에만 | Architecture §3.5·§8 |
| `storage/LocalSolveStore.java` | 로컬 디렉터리 구현 (테스트 fake + local 실행 겸용) | Architecture §3.5·§6 |
| `storage/StatusJson.java` | package-private: `SolveStatus` ↔ JSON 바이트 (두 구현 공용, Jackson 3 `JsonMapper`) | Architecture §3.3 |
| `AppConfig.java` | @Configuration: store 선택(Spring profile)·executor·adapter(`JsonMapper` 주입, §6 N2)·`ProfileRegistry.builtIn()`(solver-profile 모듈)·`AlnsConfigFactory` 조립 | Architecture §4 |
| `RoNextProperties.java` | @ConfigurationProperties `ro-next.*` (§1.1) | Architecture §4 |

기존 파일 수정 2건:

| 파일 | 조치 |
|---|---|
| `app/pom.xml` | 추가: `software.amazon.awssdk:bom`(import) + `software.amazon.awssdk:s3`. JSON은 starter-web이 끄는 **Jackson 3** 그대로 (별도 jackson 의존·`spring-boot-jackson2` 금지 — Stage 0 §4.4). `ro-next-solver-profile`·`ro-next-solver-core` 의존은 Stage 0에서 이미 선언됨 |
| `app/src/main/resources/application.yml` | §1.1의 운영 설정 키 추가 (Architecture §4 — 점수·제약 로직 금지) |

### 1.1 설정 키 (application.yml)

```yaml
ro-next:
  storage:
    bucket: ""            # S3 모드 필수 (예: ro-next-solves-dev — 배포 설정, Architecture §3.3)
    local-dir: ""         # local profile에서 LocalSolveStore 루트
  solve:
    concurrency: 1              # 동시 실행 수 (Architecture §3.2 "기본 1~2")
    heartbeat-interval-sec: 15  # RUNNING heartbeat 주기 (재량)
    stale-after-sec: 60         # RUNNING인데 heartbeat가 이보다 오래되면 STALE 응답 (재량)
    fallback-time-limit-sec: 60 # 입력에 Termination이 없을 때 쓰는 시간 한도 (Domain §2.5.1)
    max-steps:                  # 비우면 미적용 — 최대 실행 step 수
    idle-steps:                 # 비우면 미적용 — best 미개선 step 수 한도 (Stage 4 §3.3)
    idle-sec:                   # 비우면 미적용 — best 미개선 시간 한도
    seed: 0                     # AlnsConfig.seed — 전 run 고정 (재현성 우선, 재량. 정책 변경은 Stage 8)
```

- Spring profile `local` → `LocalSolveStore(local-dir)`, 그 외 → `S3SolveStore(bucket)` (Architecture §6).
- **`ro-next.solve.*`의 예산 키 5개가 Domain §2.5.1의 "운영 설정만" 항목이다.** 규약(wire)에
  있는 것은 시간 한도(`Termination.secondsSpentLimit`)뿐이고, 나머지 넷은 여기서만 온다.
- `AlnsConfig` 조립 (run마다, §3.2 절차 3-d′):

  ```text
  timeLimitSec = wire Termination.secondsSpentLimit (운반: ParseResult — §2.2)  ▷  fallback-time-limit-sec
  maxSteps·idleSteps·idleSec = 설정값 (비어 있으면 OptionalLong.empty)
  seed = 설정값
  나머지 튜닝 파라미터 = AlnsConfig.defaults(...)의 값 그대로
  ```

  **배송정책과 달리 이 값들은 `Problem`에 들어가지 않는다** (Domain §2.5.1 MUST NOT).
  `PlanNormalizer`가 아니라 여기서 조립하는 이유가 그것이다.

---

## 2. 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.

### 2.1 `storage` (Architecture §3.3·§3.5)

```java
public enum SolveState { RECEIVED, RUNNING, DONE, FAILED }

/** status.json 내용. STALE은 저장하지 않는다 — 조회 시 파생 (Architecture §3.2). */
public record SolveStatus(SolveState state, Instant heartbeatAt, Optional<String> error) {}

/** key 조립 규칙의 유일한 자리 — 컨트롤러·executor에 문자열 조립 금지 (Architecture §3.3). */
public record SolveKey(String customerSegment, String planId, String runId) {
    public String value();                        // "solves/{customerSegment}/{planId}/{runId}"
    public static SolveKey issue(Optional<String> customerId, String planId,
                                 Clock clock, RandomGenerator rng);      // §3.3 절차
    public static SolveKey of(String customerSegment, String planId, String runId); // 조회 경로 재조립 + 형식 검증
    public String inputObjectKey();               // value() + "/input.json"
    public String statusObjectKey();              // value() + "/status.json"
    public String resultObjectKey();              // value() + "/result.json"
}

public interface SolveStore {                     // Architecture §3.5 그대로
    void putInput(SolveKey key, byte[] body);
    void putStatus(SolveKey key, SolveStatus status);
    void putResult(SolveKey key, byte[] resultJson);
    Optional<SolveStatus> getStatus(SolveKey key);
    Optional<byte[]> getInput(SolveKey key);
    Optional<byte[]> getResult(SolveKey key);
}

public final class S3SolveStore implements SolveStore {
    public S3SolveStore(S3Client s3, String bucket);
}
public final class LocalSolveStore implements SolveStore {
    public LocalSolveStore(Path root);            // 객체 key = root 밑 상대 경로. 테스트는 @TempDir로 사용
}
```

### 2.2 `input`

```java
// Jackson 3 (tools.jackson) — Boot 4.1 기본. ObjectMapper(Jackson 2) 금지 (Stage 0 §4.4)
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;

public final class PlanJsonAdapter {
    /** @param mapper floats를 BigDecimal로 읽도록 구성한 JsonMapper (§6 N2) */
    public PlanJsonAdapter(JsonMapper mapper);
    /** 규약 JSON → ParseResult. wire 형식 변환만 — 의미 변환·기본값은 PlanNormalizer (Stage 1 서두).
        실패 시 InputException(INVALID_INPUT, field=JSON 경로). §4 매핑표가 계약. */
    public ParseResult parse(byte[] wireJson);

    /** parse의 반환 봉투 (2026-08-12 확정). 탐색 예산은 canonical(PlanInput)에 담기지 않으므로
        (Domain §2.5.1) 같은 파싱 1회의 별도 산출물로 함께 반환한다 — adapter는 무상태라
        동시 요청(§3.2 concurrency 2)에서 값이 섞일 자리가 없다. */
    public record ParseResult(PlanInput plan, OptionalLong secondsSpentLimit) {}
}

public final class ResultJsonWriter {
    public ResultJsonWriter(JsonMapper mapper);
    public byte[] write(SolveResult result);      // §5 wire 잠정안 그대로 직렬화
}
```

### 2.3 `run` (Architecture §3.2)

```java
public final class SolveRunner {
    public SolveRunner(SolveStore store, PlanJsonAdapter adapter, ResultJsonWriter resultWriter,
                       ProfileRegistry profiles, AlnsConfigFactory alnsConfigs,
                       Verifier verifier, Clock clock);

    /** 한 건 동기 실행 (§3.2 절차 2). 상태 기록은 하지 않는다 — Outcome을 executor가 기록.
        결과 저장(putResult)은 여기서 한다 (DONE 기록보다 항상 먼저이도록). */
    public Outcome run(SolveKey key, Instant receivedAt);

    public record Outcome(SolveState state, Optional<String> error) {}   // state ∈ {DONE, FAILED}

    /** verify 결선 seam — 기본 SolutionVerifier::verify. 테스트 T8이 Fail 경로 주입에 사용. */
    @FunctionalInterface
    public interface Verifier {
        VerificationResult verify(Problem problem, Profile profile,
                                  Map<VehicleId, List<NodeId>> routes, Set<RequestId> bank,
                                  Evaluation reported, long[] reportedScore);
    }

    /** 설정 기본값 + 이번 입력의 시간 한도로 run별 AlnsConfig를 만든다 (§1.1).
        구현은 `RoNextProperties.solve`(fallback-time-limit-sec·max-steps·idle-*·seed)를
        생성 시점에 담아 두고, run마다 wire 값만 받아 조립한다 — AppConfig가 람다로 제공한다.
        wireTimeLimitSec이 empty면 fallback-time-limit-sec을 쓴다 (§1.1의 ▷ 체인). */
    @FunctionalInterface
    public interface AlnsConfigFactory {
        AlnsConfig create(OptionalLong wireTimeLimitSec);
    }

    /** AlnsConfig의 예산 필드를 결과용 SearchBudget으로 옮긴다 (Stage 5 §4.1).
        알고리즘 튜닝 파라미터는 담지 않는다 — 그건 결과에 남지 않고 Stage 8 §4 표가 소유한다.
        종료 사유·반복 카운터 등 `AlnsRunStats`도 담지 않는다 — stats는 로그·실험용으로만
        (Domain §11.1, Stage 5 §9. 종전의 termination 인자는 2026-08-13 삭제). */
    static SolveResult.SearchBudget budgetOf(AlnsConfig config);
}

public final class SolveExecutor implements AutoCloseable {
    public SolveExecutor(SolveRunner runner, SolveStore store,
                         int concurrency, Duration heartbeatInterval, Clock clock);
    public void submit(SolveKey key, Instant receivedAt);   // 접수 §3.1-5. 큐는 무제한 (in-process)
    @Override public void close();                          // 풀 종료 (진행 중 solve는 사라짐 — §3.2 한계)
}
```

### 2.4 `api`

```java
@RestController
public final class SolveController {              // HTTP만 — 절차는 SolveService
    @PostMapping("/solves")
    ResponseEntity<SolveAccepted> submit(@RequestBody byte[] body);
    @GetMapping("/solves/{customer}/{planId}/{runId}")
    SolveStatusResponse status(...);              // 경로 = solveKey 재해석 (§3.4 노트)
    @GetMapping("/solves/{customer}/{planId}/{runId}/result")
    ResponseEntity<byte[]> result(...);           // application/json, 저장 바이트 그대로
}

public final class SolveService {
    public SolveService(SolveStore store, SolveExecutor executor, PlanJsonAdapter adapter,
                        Duration staleAfter, Clock clock, RandomGenerator rng);
    public SolveAccepted accept(byte[] wireJson);           // §3.1 절차
    public SolveStatusResponse status(SolveKey key);        // §3.4 (미존재 → 404)
    public byte[] result(SolveKey key);                     // §3.4 (DONE 아니면 409)
}

public record SolveAccepted(String solveKey) {}
public record SolveStatusResponse(String state, Instant heartbeatAt, String error) {}
public record ApiError(String error, String field, String message) {}
```

HTTP 상태 잠정 (wire 협의 대상 — Domain §11.2와 동일 원칙): `INVALID_INPUT` → 400,
`UNSUPPORTED_INPUT` → 422 (Architecture §3.1 "4xx UNSUPPORTED_INPUT"의 구체화),
solve 미존재 → 404, DONE 전 result → 409, 그 외 예외 → 500. 404·409는 Spring
`ResponseStatusException`으로 던진다 (전용 예외 타입을 만들지 않는다).

---

## 3. 절차

### 3.1 접수 (동기 — Architecture §3.1 확장)

```text
POST /solves (body = 규약 JSON 바이트)
1. parsed = PlanJsonAdapter.parse(body), planInput = parsed.plan()
   실패 → 400 (S3에 아무것도 남기지 않음 — Domain §12).
   JSON 문법 오류·필수 필드 부재·타입 파싱 불가·단위 코드 위반(§4 공통 규칙)이 여기서 걸린다
   — "규약에 맞는 JSON인가" 수준 (Architecture §3.1 주석).
2. 미지원 게이트: planInput.options()의 multiRotation이 지원 범위 밖 (부재는 규약 default 0).
   통과는 집합 {0, 1}뿐 — 0·1 둘 다 "1바퀴"다 (Domain §2.5, D1 확정).
   -1 또는 2 이상 → InputException(UNSUPPORTED_INPUT) → 422, 저장 없음.
   -2 이하는 규약이 금지한 값이라 INVALID_INPUT → 400.
   비교식 `> 1`로 쓰지 않는다 — -1이 게이트를 통과한다 (Domain §2.5 MUST NOT).
   같은 규칙을 PlanNormalizer(Stage 1 절차 2)도 갖는다 — 접수 게이트는 §3.1-2의 문면 이행이고
   executor 쪽이 최종 방어다.
2b. 정규화 검증 (2026-08-11 — Architecture §3.1 개정·Domain §12 이행, §10 Q3 해소):
   PlanNormalizer.normalize(planInput)을 실행하고 결과 Plan은 버린다.
   InputException(INVALID_INPUT) → 400, (UNSUPPORTED_INPUT) → 422 — 저장 없음.
   소수 거리·`close == open` 시간창(Domain §12의 접수 4xx 분류)과 빈 `zoneIds`
   (Stage 1 §4 절차 5의 정규화 규칙 — Domain §12 예시에는 없다)가 여기서 걸린다.
   Problem.freeze는 하지 않는다 — 무거운 작업(이동표·동결)은 여전히 executor에서.
   parse·normalize 결과는 검증에만 쓰고 버린다 (executor가 저장본에서 다시 파싱·정규화).
3. key = SolveKey.issue(Optional.ofNullable(planInput.customerId()), planInput.planId(),
                        clock, rng)   // §3.3 — PlanInput.customerId는 nullable String (§4 공통 규칙 5)
   planId·customerId가 key 안전 문자 집합 밖이면 400 (§7 E5 — 잠정 규칙).
4. store.putInput(key, body)     — 받은 바이트 원문 그대로 (재직렬화 금지. "검증 통과본" = 원문)
5. store.putStatus(key, RECEIVED, heartbeatAt = now)
6. executor.submit(key, receivedAt = now)
7. 200 + { "solveKey": key.value() }
```

### 3.2 executor — 상태 전이·heartbeat (Architecture §3.2 확장)

```text
SolveExecutor.submit → 고정 크기 풀(concurrency)의 큐에 등록. 워커 스레드가 잡으면:
1. startedAt = now. putStatus(RUNNING, heartbeatAt = now).
2. heartbeat 예약: heartbeat-interval마다 putStatus(RUNNING, heartbeatAt = now)
   (ScheduledExecutorService 1개 공용. run별 락으로 3과 직렬화 — §7 E10).
3. outcome = SolveRunner.run(key, receivedAt):
   a. bytes = store.getInput(key)              부재 → FAILED("INPUT_MISSING") (저장 불변식 위반)
   b. parsed = adapter.parse(bytes), planInput = parsed.plan()   InputException → FAILED("INPUT: {field} {message}")
   c. plan = PlanNormalizer.normalize(planInput)   InputException → FAILED(〃) — 의미 오류는
      여기서 걸린다 (소수 거리, `close == open` 시간창 등 — Architecture §3.1 주석, §10 Q3)
   d. problem = Problem.freeze(plan)               ProblemCreationException → FAILED("PROBLEM: …")
   d'. profile = profiles.resolve(plan.customerId())   // ★ 이 한 줄이 유일한 resolve 지점
       alnsConfig = alnsConfigs.create(adapter가 읽은 wire 시간 한도)   // §1.1
       ─ 아래 e와 g에 **같은 profile 지역변수**를 넘긴다. 이것이 Domain §8.4 MUST
         ("탐색과 재검증이 같은 profile")를 지키는 방식 전부다 — resolve가 두 번 일어나는
         코드 경로를 만들지 않는다 (테스트 T14).
   e. result = AlnsSolver.withDefaults(alnsConfig).solve(problem, profile)
      IllegalStateException(구조 결함 — Stage 4 N2) → FAILED("SOLVER_BUG: …") (Domain §12)
   f. best 분해 (Stage 5 §2.1 인계): routes = best.routes()를 toMap(Route::vehicleId,
      Route::visits)로, bank = best.bank(). toMap 중복 키 예외 → FAILED (Stage 5 N3)
   g. verification = verifier.verify(problem, profile, routes, bank,
                                     result.bestEvaluation(), result.bestScore())
   h. Fail → FAILED("VERIFY: {kind}@{id} … (총 n건)" — 첫 5건 + 총수). putResult 없음
      (재검증 FAIL 시 결과 미저장 — Domain §10.2 MUST, Master §3-⑥)
   i. pass(= VerificationResult.Pass) → finishedAt = now.
      stamp = RunStamp(key.inputObjectKey(), utc(receivedAt), utc(startedAt), utc(finishedAt),
                       budgetOf(alnsConfig))   // Stage 5 §4.1 — stats(종료 사유 포함)는 안 싣는다
      solveResult = ResultAssembler.assemble(problem, profile, pass, stamp)
      store.putResult(key, resultWriter.write(solveResult)) → DONE
4. heartbeat 취소 + 진행 중인 heartbeat put 완료 대기 (run별 락).
5. putStatus(최종: DONE 또는 FAILED + error, heartbeatAt = now).
6. 3에서 잡지 못한 예외(저장 I/O 포함)도 catch-all로 4~5를 FAILED로 수행.
```

- **저장 순서 불변식**: result.json put(3-i)은 status DONE put(5)보다 항상 먼저다.
  "DONE인데 result 없음"은 불변식 위반이다 (§7 E14).
- 시각은 전부 UTC `Instant`로 재고, `RunStamp`의 `LocalDateTime`은 UTC 벽시계로 변환한다
  (잠정 — 결과 시각의 timezone 표기는 wire 협의에 묶임, §10 Q4).
- 전체 풀이 타임박스는 두지 않는다 — `AlnsSolver`가 자기 예산(입력 옵션 ▷ 설정 기본값)만 지키고
  (Stage 4 §9 Q1의 해소), verify·assemble은 병목이 아니며(Domain §10.3) 행업은 STALE로 드러난다.
- **profile resolve는 절차 d′ 한 곳뿐이다.** 컨트롤러·executor·assembler 어디에서도 다시
  resolve하지 않는다 — `Problem`이 profile을 담지 않게 된 이후 이 단일 지점이 §8.4 MUST의
  유일한 근거다 (Architecture §2.2).
- 재시작 한계는 Architecture §3.2 그대로 정직하게 둔다: 앱 재시작 시 큐·진행 중 풀이는
  사라지고, 호출 측이 STALE을 보고 재접수한다. 자동 재개를 만들지 않는다.

### 3.3 S3 key 조립 (Architecture §3.3 확장 — `SolveKey`만 안다)

```text
solveKey 값     = "solves/{customerSegment}/{planId}/{runId}"      (후행 슬래시 없음)
customerSegment = customerId (§4 — 잠정 wire 원천 shprId, §10 Q2) ▷ "unknown" (부재 시)
runId           = UTC 접수 시각 "yyyyMMdd'T'HHmmssSSS" + "-" + 난수 4 hex   (재량 — 시각 기반 +
                  같은 ms 재접수 충돌 회피. 같은 planId 재접수 = 새 runId, 덮어쓰기 없음)
객체            = {solveKey}/input.json · status.json · result.json        (Architecture §3.3 그대로)
세그먼트 문자    = [A-Za-z0-9._-]+ — 밖이면 접수 400 (§7 E5, 잠정 규칙: key·URL 경로 안전)
버킷            = ro-next.storage.bucket 설정 주입 (환경별 ro-next-solves-{env} — 배포 설정)
```

### 3.4 조회 API (Architecture §3.4 확장)

```text
GET /solves/{customer}/{planId}/{runId}
1. key = SolveKey.of(경로 3조각) — 형식 위반 → 400
2. status = store.getStatus(key) — 부재 → 404
3. 표시 state = (state == RUNNING && now − heartbeatAt > stale-after) ? "STALE" : state.name()
   (저장 상태는 바꾸지 않는다. STALE이면 호출 측이 재접수 — Architecture §3.2)
4. 200 + { state, heartbeatAt, error? }

GET /solves/{customer}/{planId}/{runId}/result
1~2 동일.
3. state != DONE → 409 (본문 ApiError에 현재 state. RECEIVED·RUNNING·FAILED 전부 409 —
   404는 solve 자체가 없을 때만)
4. bytes = store.getResult(key) — 부재 → 500 (§7 E14 불변식 위반 신호)
5. 200 + result.json 바이트 그대로 (application/json)
```

**경로 재해석 노트**: solveKey 값이 `solves/`로 시작하는 슬래시 포함 prefix이므로
(Architecture §3.3), `GET /solves/{solveKey}`를 문자 그대로 구현하면 경로가
`/solves/solves/...`가 된다. 잠정 wire는 **`GET /{solveKey}`** — solveKey가 이미
`solves/{customer}/{planId}/{runId}`라서 위 3-세그먼트 매핑과 정확히 일치하고, 접수 응답의
solveKey를 그대로 URL에 붙이면 된다. 최종 경로·필드명은 호출 시스템과 협의 확정한다
(Architecture §3.4, §10 Q4).

---

## 4. wire JSON → `PlanInput` 매핑표

대상 타입은 Stage 1 §2.3의 raw 운반체 그대로다. wire 실물 근거: 규약 PDF + fixture 두 개.

**공통 규칙**

1. 수치 값은 **JSON number만** 수용한다 — 문자열로 인코딩된 수치는 `INVALID_INPUT`이다
   (Domain §3.1, 2026-08-12 확정). 대상은 `weight`·`volume`·`qty`·`taskTime`·`duration`·
   `speed`·`maxWeight`·`maxVolume`·좌표·이동표 `D`/`U`·options의 수치 값(`multiRotation` 등)
   전부이고, 시각(`HH:mm:ss`)·날짜·ID·코드(`weightUnitCd`·이동표 `C` 등)는 수치가 아니라
   문자열 그대로다. floor fixture는 2026-08-12 number로 정정 완료(`qty: 1`·`duration: 300`·
   `speed: 45` 등), 원본 win_poc_case.json은 수신 원형으로 문자열 그대로 보존 — 이 규칙만으로도
   미접수다. 정수 필드(`qty`·`duration`·`taskTime`·`speed`·`multiRotation` 등)에 소수 →
   `INVALID_INPUT` — 판정은 **표기 기준**이라 `300.0`처럼 값이 정수라도 소수점 표기면
   거부한다 (Domain §3.1, 2026-08-12 확정). `BigDecimal` 필드는 원문 자릿수 보존
   (§6 N2 — double 경유 금지, Domain §3.1 MUST NOT).
2. DateTime은 `yyyy-MM-dd HH:mm:ss`와 RFC3339(`...T...Z`) 둘 다 수용해 `LocalDateTime`으로
   통일한다 (Domain §2.2). RFC3339의 offset은 버리고 표기된 벽시계를 쓴다 (timezone 해석은
   솔버 밖 책임 — Domain §2.2. §7 E21).
3. partial-time(`HH:mm:ss`) → `LocalTime`. 파싱 불가 → `INVALID_INPUT`.
4. 좌표는 **원문 문자열 그대로** `latText`/`lonText`에 담는다 (`LocationId.generated`의
   결정성 — Stage 1 §2.1).
5. 부재(optional)는 전부 null로 넘긴다 — 기본값 채움·정차 한도 접기(차량 > 전역 > 없음)는 `PlanNormalizer`의 일이다
   (Stage 1 서두: adapter는 wire 형식 변환만).
6. 아래 "무시" 필드와 미지(unknown) 필드는 조용히 버린다 (관용 수신 — wire 확장에 견딤).
   매핑된 필드의 값 오류만 거부한다. (정본 Domain §2.1 — 2026-08-12 시스템 소유자 확정.
   실물 무시 대상 목록도 그곳에 등재.)

### 4.1 Plan 봉투 → `PlanInput`

| wire | PlanInput | 규칙 |
|---|---|---|
| `planId` | `planId` | 필수 |
| `dateRange.from` / `.to` | `planStart` / `planEnd` | 필수, 공통 규칙 2 |
| `shprId` | `customerId` | **잠정** (Domain §2.2 "shprId(또는 협의 필드)", §10 Q2). 부재 → null |
| `continent`, `lssId`, `routes` | 무시 | 이동은 준비된 표만 사용 (Domain §4) — OSRM 대륙 선택 불사용 |

### 4.2 `depot[]` → `DepotInput` (배열 그대로 — canonical은 다중 차고 허용, Domain §2.5)

| wire | DepotInput | 규칙 |
|---|---|---|
| `locId` | `locId` | 부재 → null (좌표 기반 생성 — Stage 1 E22) |
| `latitude` / `longitude` | `latText` / `lonText` | 필수, 원문 보존 |
| `openTime` / `closeTime` | `openTime` / `closeTime` | 부재 → null (**전개는 정규화** — Domain §3.2). 차고 창은 있는 출·도착 순간에만 적용된다 (Domain §7.1) |
| `taskTime` | — | **무시한다** (2026-08-11). 복귀 선적 시간이라 1바퀴엔 발생하지 않아 canonical에 자리가 없다 (Domain §2.5 · Stage Extra E1) |
| `zoneId` | `zoneId` | 보관만 |
| `locTcd` | 무시 | canonical 밖 (Stage 1 §8 인계 목록) |

### 4.3 `orders[]` → `RequestInput` (현 규약 전부 DELIVERY_ONLY — pickup = null 고정, Domain §1.3)

현 규약 wire는 delivery 한 칸만 준다. pickup만 있는 wire가 협의되면 그 칸을 `pickup`에 넣고
`delivery = null`로 두면 정규화가 `PICKUP_ONLY`로 확정한다. 지금은 그 경로를 열지 않는다.

| wire | RequestInput | 규칙 |
|---|---|---|
| `orderId` | `orderId` | 필수 |
| `locId` | `delivery.locId` | 부재 → null |
| `latitude` / `longitude` | `delivery.latText` / `lonText` | 필수 |
| `openTime` / `closeTime` | `delivery.openTime` / `closeTime` | 부재 → null (**전개는 정규화** — 주문 창도 날마다 반복, Domain §3.2) |
| `duration` | `delivery.durationSec` | 부재 → null |
| `reqDate` ▷ `dueDate` | `delivery.reqDate` | legacy `dueDate` 수용 (Domain §2.3). 부재 → null (→ planEnd) |
| `zoneId` | `delivery.zoneId` | 부재 → null |
| `vehicleFeature` | `vehicleFeatures` | 부재 → null (전 차급). `["ALL"]` 정규화는 Stage 1 |
| — | `requiredCapabilities` | 항상 null — 요구 능력의 wire 원천이 규약에 없음 (축 휴면, §7 E20) |
| `customerId`(주문 수준), `district`, `DEPOT`, 주문 수준 `taskTime` | 무시 | §10 Q2 / §7 E19 |

`items[]` → `ItemInput`:

| wire | ItemInput | 규칙 |
|---|---|---|
| `itemId` | `itemId` | 원문 그대로 (blank·부재 포함). **orderId 폴백은 Stage 1 정규화** (Domain §2.3). floor fixture는 452건 전부 `""` (§7 E4) |
| `weight` / `volume` | `weightKg` / `volumeCbm` | 필수, BigDecimal 원문. `weightUnitCd` 존재 시 `"KG"`, `volumeUnitCd` 존재 시 `"CBM"`만 허용 — 그 외 `INVALID_INPUT` (정본 Domain §3.1, 2026-08-12 확정) |
| `qty` | `qty` | 부재 → null (→ 1) |
| `taskTime` | `taskTimeSec` | 부재 → null (→ 0). **×qty 확정** (2026-08-11 소유자 — Domain §3.1·§3.3) |
| `orderId`(중복), `prodId` | 무시 | 종전 `prodId` 폴백은 폐기 — 시스템 소유자 확정이 아닌 작성 시 발명이었다 (Domain §2.3, 2026-08-12) |

### 4.4 `vehicles[]` → `VehicleInput`

| wire | VehicleInput | 규칙 |
|---|---|---|
| `vehicleId` | `vehicleId` | 필수 |
| `vehicleFeature` | `vehicleFeature` | 부재 → `"ALL"` (규약 default 열). **접기는 Stage 1이 한다** — adapter는 값을 그대로 넘기고, 정규화가 부재·blank·`"ALL"`을 전 차급(`Optional.empty`)으로 접는다 (Stage 1 E26. 종전 "비교는 문자 그대로"는 2026-08-12 폐기된 규칙의 잔재였다) |
| `maxWeight` / `maxVolume` | `maxWeightKg` / `maxVolumeCbm` | 필수, BigDecimal |
| `workStartTime` / `workEndTime` | `workStart` / `workEnd` | 부재 → null (**전개는 정규화** — 날마다 반복되는 근무창, Domain §3.2) |
| `speed` | `speedKmH` | 정수만 — 소수 → `INVALID_INPUT` (Stage 1 §9 Q4 잠정 유지) |
| `maxStopCnt` | `maxStopCnt` | 부재 → null |
| `maxDriveTime` | `maxDriveTimeSec` | 부재 → null |
| `maxDriveDist` / `maxDriveDistc` | `maxDriveDistMeter` | 규약 PDF 표기는 `maxDriveDistc`(오타 추정, 실물 wire 출현 0건) — 두 철자 다 수용, **둘 다 오면 `INVALID_INPUT`** (정본 Domain §2.4, 2026-08-12 확정) |
| `maxWidth` / `maxHeight` / `maxLength` | — | **무시한다** — canonical에 치수 축이 없다 (2026-08-11 유예, Domain §2.4 유예 표 · Stage Extra E2) |
| `driverSkill` | `capabilities` (싱글턴 집합) | 부재 → null (→ 빈 집합). `"ALL"`도 문자 그대로 — 축 휴면이라 무영향 (§7 E20) |
| `zoneIds` | `zoneIds` | 확장 (Domain §2.4) — 그대로 전달 (빈 집합 거부는 정규화) |
| `vhclOwnTyp` | — | **무시한다** — canonical에 소유 축이 없다 (2026-08-11 유예, Domain §2.4 유예 표 · Stage Extra E3) |
| `startDepot` / `endDepot` | `startDepotLocId` / `endDepotLocId` | 확장. **현 규약 CVRPTW 채움 (adapter 정책, Domain §2.4 2026-08-14)**: wire에 `startDepot` 키가 없고 계획 depot가 정확히 1개면 그 차고 ID를 `startDepotLocId`에 넣는다. depot가 0개·2개 이상이거나 start가 명시되면 채우지 않는다 (명시는 그대로, 부재는 null). 이 채움은 정규화가 하지 않는다 — Domain에 암묵 채움을 남기지 않기 위함. Win 1차 성공 기준은 이 앱 접수 경로다 |
| `trips` | — | **무시한다** — 차량별 지정은 wire에 없어 유예했다. `options.trips` 하나만 쓴다 (2026-08-11, Domain §2.4 유예 표 · Stage Extra E1) |

### 4.5 `distanceMatrix[]` → `TravelEntryInput`

| wire | TravelEntryInput | 규칙 |
|---|---|---|
| `F` / `T` | `fromLocId` / `toLocId` | 필수 |
| `D` / `U` | `distance` / `time` | BigDecimal 원문 — 소수 표기 검사(scale > 0 거부, 표기 기준)는 정규화 (Stage 1 절차 7). self arc(`F` == `T`) 행은 정규화가 값을 읽지 않고 버린다 — 소수 검사 대상도 아니다 (Domain §4, 2026-08-12) |
| `C` | — | 구조적 배제: **`TravelEntryInput`에 `C` 필드를 만들지 않는다** — wire에는 전 행에 존재하지만(fixture 실값 O·G) 읽지 않는다 (Domain §4 MUST NOT) |

### 4.6 `options` → `OptionsInput` (전부 optional — 부재 → null, 기본값은 정규화가)

| wire | OptionsInput | 규칙 |
|---|---|---|
| `trips` | `trips` | **전체 설정 하나다** — 차량별 지정은 유예 (§4.4, Domain §2.5) |
| `multiRotation` | `multiRotation` | **`OptionsInput`에는 담는다** (정본 Stage 1 §2.3) — "검증만, 미보관"의 주어는 canonical `Plan`이다 (정규화가 검증 후 보관하지 않는다, Stage 1 §4 절차 2). 접수 게이트 §3.1-2의 대상. 통과는 `{0, 1}`(= 1바퀴)뿐 — `-1`·`2 이상`은 거부 (Domain §2.5) |
| `waitInDepot` | `waitInDepot` | |
| `distanceTimeCalculate` ▷ `distanceCalculate` | `distanceTimeCalculate` | fixture·Domain 표기 우선, 규약 PDF 표기(`distanceCalculate`)도 수용 |
| `Optimizer.DefaultSpeed` ▷ `defaultSpeed` | `defaultSpeedKmH` | 정수만 |
| `Termination.secondsSpentLimit` | (canonical 아님) `AlnsConfig.timeLimitSec` | floor fixture 값 600. **`Plan`에 담기지 않는다** — parse 반환 봉투 `ParseResult.secondsSpentLimit`(§2.2, 2026-08-12 확정)로 함께 나와 `AlnsConfigFactory`로 간다 (Domain §2.5.1) |
| `Optimizer.VehicleMaxStopCount` | `globalVehicleMaxStopCount` | fixture 값 28 (차량 우선 접기는 정규화 — Domain §2.6) |
| `driverRestTimeRatio`, `difficultySortType`, `customerAbbr` | 무시 | **무시 확정** — Domain §2.5 (2026-08-12 시스템 소유자). 0이 아닌 비율도 거부하지 않는다 (§7 E19) |

---

## 5. result.json wire 잠정안 (Stage 5 §9 인계 — 협의 전 잠정, 의미는 Domain §11.1)

필드명은 §11.1의 **의미 항목에 1:1 대응**하되 wire 표기는 이 절의 잠정안이다 — §11.1은 의미
정본일 뿐 필드명·형태는 §11.2(Plan D2 협의) 소유라 "이름 그대로"가 아니다 (2026-08-13 교정.
예: 의미 `totalDistance` → wire `totalDistanceMeter`, 적재 2축 → `loadWeightKg`·`loadVolumeCbm`).
시각은 `yyyy-MM-dd HH:mm:ss`(UTC 벽시계 — Domain §2.2 표기 관례), 무게·부피는 입력과 대칭인
decimal kg/cbm 문자열(milli ÷ 1000, 소수 3자리), 거리 m·시간 초는 정수다.

```json
{
  "planId": "WINCOMMERCE_TEST_CBM_2",
  "status": "DONE",
  "run": {
    "inputKey": "solves/S3853/WINCOMMERCE_TEST_CBM_2/20230913T001500123-a1b2/input.json",
    "receivedAt": "2023-09-13 00:15:00", "startedAt": "2023-09-13 00:15:01",
    "finishedAt": "2023-09-13 00:25:01", "profileId": "default", "verified": true,
    "deliveryPolicy": { "trips": "oneway", "waitInDepot": false },
    "searchBudget": { "timeLimitSec": 600, "seed": 0 }
  },
  "routes": [{
    "vehicleId": "V009",
    "depotDeparture": "2023-09-13 05:20:00",
    "visits": [{
      "orderId": "WIN_2AD0", "type": "DELIVERY", "locationId": "WIN_2AD0",
      "arrival": "2023-09-13 05:40:00", "serviceStart": "2023-09-13 05:45:00",
      "serviceEnd": "2023-09-13 05:50:00", "loadWeightKg": "812.400", "loadVolumeCbm": "3.210"
    }],
    "driveDistMeter": 123456, "driveTimeSec": 9876, "stopCount": 17, "routeOperationalTimeSec": 21000
  }],
  "unassigned": [{ "orderId": "WIN_9Z01", "reason": "NOT_PLACED" }],
  "metrics": { "unassignedCount": 1, "usedVehicleCount": 12,
               "totalDistanceMeter": 1234567, "totalRouteOperationalTimeSec": 250000 }
}
```

- `type`은 Stage 5 `Visit.pickup`의 wire 표현(`PICKUP`|`DELIVERY`) — PD 확장 대비 (Stage 5 §9 Q4).
- `depotDeparture`는 경로 시각(Domain §11.1, 2026-08-11 추가)의 wire 잠정 표기다.
  `depotReturn`은 endDepot가 있는 경로에만 실린다 (이 예는 oneway라 없음). 최종 필드명은 D2 협의.
- `SolveResult` → JSON은 값 그대로의 기계적 변환이다. 계산·필터링을 하지 않는다
  (검증된 해 ↔ 결과 일치는 Stage 5 T9 + 본 Stage T11이 보장 — Domain §10.2).
- status.json wire (storage 내부 형식): `{ "state": "RUNNING", "heartbeatAt": "2023-09-13T00:15:30Z", "error": null }`.

---

## 6. 설계 노트

| # | 내용 |
|---|---|
| N1 | **접수는 파싱 + 정규화 검증까지** (2026-08-11 개정 — §10 Q3 해소): 접수가 `parse` + multiRotation 게이트 + `PlanNormalizer.normalize`를 실행해 의미 오류까지 4xx로 돌려주고 결과는 버린다 (Domain §12 이행, Architecture §3.1 같은 날 개정). Problem 동결은 여전히 executor에서. executor가 저장된 input.json을 **같은 adapter·같은 normalizer로 다시** 처리한다 — 접수 검증과 실행 입력이 한 코드 경로이고, executor 쪽 정규화(§3.2-c)는 접수를 우회해 store에 직접 놓인 입력에 대한 최종 방어로 남는다 |
| N2 | **BigDecimal 파싱 경로 (Jackson 3)**: adapter 전용 `JsonMapper`(`tools.jackson.databind.json.JsonMapper`)를 구성해 JSON 숫자(float/double)를 `BigDecimal`로 읽고, tree(`JsonNode`) 기반 수동 매핑을 쓴다. Feature 이름은 구현 시 Jackson 3 API로 확인 — 의도는 Jackson 2의 `USE_BIG_DECIMAL_FOR_FLOATS`와 동등. `double` 경유 금지(Domain §3.1 MUST NOT)를 wire 파싱까지 관철한다 (databind POJO 자동 매핑은 원문 보존과 인코딩 검증 — 수치 필드의 token이 number인지 확인해 문자열 수치를 `INVALID_INPUT`으로 거부(공통 규칙 1, Domain §3.1) — 에 부적합). **Jackson 2 `ObjectMapper`·`com.fasterxml.jackson`·`spring-boot-jackson2`는 쓰지 않는다** (Stage 0 §4.4) |
| N3 | **heartbeat와 최종 기록의 직렬화**: heartbeat put과 종료 put이 겹치면 DONE 뒤에 RUNNING이 덮일 수 있다. run별 락 + "취소 후 진행 중 put 완료 대기"(§3.2 절차 4)로 마지막 쓰기가 항상 최종 상태이게 한다 |
| N4 | **runner/executor 분리**: `SolveRunner`는 스레드·heartbeat를 모르는 동기 파이프라인이라 fake store만으로 단위 테스트가 된다 (T7·T8). `SolveExecutor`는 비동기 감싸개(풀·큐·heartbeat·최종 기록)만 갖는다 |
| N5 | **`Verifier` seam 하나만**: DoD급 임무인 "Fail → status FAILED 기록"을 테스트하려면 Fail을 만들어야 하는데, 정상 ALNS는 verify를 통과하는 해만 낸다 (Stage 5 T8). 함수형 인터페이스 주입(기본 `SolutionVerifier::verify`)이 최소 seam이다. 다른 core 호출(normalize·freeze·solve)은 입력으로 실패를 만들 수 있어 seam을 두지 않는다 |
| N6 | **awssdk 격리의 자동 강제**: Architecture §8("컨트롤러·executor에 S3 SDK 직접 호출 금지")을 app 테스트의 ArchUnit 규칙 1개로 강제한다 — `..app.storage..` 밖 클래스는 `software.amazon.awssdk..` 참조 금지 (T16). solver-core의 규칙(Stage 0 §6)과 별개 파일이다 |
| N7 | **LocalSolveStore 하나로 fake와 local 실행을 겸한다** (Architecture §3.5 "인메모리 또는 로컬 디렉터리" 중 디렉터리 선택): 테스트는 @TempDir, local profile은 설정 디렉터리. 인메모리 별도 구현을 만들지 않는다 — 클래스 수 최소화 |

---

## 7. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | `multiRotation: 1` POST (floor fixture 원본 그대로) | **200 — 접수된다** (1바퀴, D1 확정). 저장·실행이 정상 진행. 문자열 `"1"`은 200이 아니라 400이다 — 공통 규칙 1 (Domain §3.1, 2026-08-12) | Domain §2.5 MUST |
| E1b | `multiRotation: 2`·`-1` POST | 422 UNSUPPORTED_INPUT, 저장 없음 (차고 재방문 = 범위 밖) | Domain §2.5 MUST·§12 |
| E1c | `multiRotation: -2` POST | 400 INVALID_INPUT, 저장 없음 (규약이 금지한 값) | Domain §2.5·§12 |
| E2 | JSON 문법 오류·필수 필드 부재·단위 코드 위반 | 400, S3 저장 없음 | Domain §12, Architecture §3.1-1 |
| E3 | 문자열 수치 인코딩(원본 win_poc_case.json)·소수 D/U(number)·`close == open` 시간창 | **400 — 저장 없음.** 문자열 수치는 파싱·매핑(공통 규칙 1, Domain §3.1 — 2026-08-12)에서, 소수 D/U·시간창은 접수 절차 2b의 정규화 검증(2026-08-11, Domain §12)에서 걸린다 — 원본 fixture는 인코딩 규칙만으로도 미접수다. 접수를 우회해 store에 직접 놓인 입력은 executor 정규화가 FAILED로 잡는다 (T8a — 최종 방어) | Domain §3.1·§12, §3.1-2b |
| E4 | itemId 전건 blank (floor fixture 실측 452/452) | adapter는 `""`를 `ItemInput`에 그대로 담음. **orderId 폴백은 Stage 1 정규화** (종전 prodId 폴백·adapter 폴백 폐기) | Domain §2.3 · Stage 1 E37 |
| E5 | planId·shprId에 `/`·공백 등 key 안전 문자 밖 | 400 INVALID_INPUT — **잠정 규칙** (Domain 미규정, key·URL 안전) | Architecture §3.3 (key 조립 소유) |
| E6 | customerId(shprId) 부재 | key 세그먼트 `unknown`, profile은 default | §3.3, Domain §8.4 |
| E6b | wire에 `Termination` 부재 | `fallback-time-limit-sec` 적용. 접수는 거부하지 않는다 (예산은 유효성 문제가 아님) | §1.1, Domain §2.5.1 |
| E6c | `idle-steps`·`idle-sec` 미설정 | `OptionalLong.empty` — 시간·step 한도만 적용 (Stage 4 E13b) | §1.1 |
| E7 | 같은 planId 재접수 | 새 runId — 기존 run 그대로 (덮어쓰기 없음) | Architecture §3.3 |
| E8 | 같은 ms 동시 접수로 runId 충돌 | 난수 suffix 4 hex로 실질 배제 (재량) | §3.3 |
| E9 | putInput 성공 후 putStatus 실패 | 500 응답. input.json 고아 — status 없어 GET 404, 재접수는 새 runId (무해) | §3.1 순서의 결과 |
| E10 | heartbeat put과 DONE/FAILED put 경합 | run별 락 + 취소 후 대기 → 최종 쓰기가 마지막 | N3 |
| E11 | 앱 재시작 (RUNNING 중) | RUNNING 고착 → heartbeat 노화 → GET이 STALE 응답, 호출 측 재접수 | Architecture §3.2 (정직한 제약) |
| E12 | 앱 재시작 (RECEIVED 큐 대기 중) | RECEIVED 고착 — STALE 판정 없음 (판정은 RUNNING만, Architecture §3.2 문면). 호출 측 자체 타임아웃으로 재접수 — 한계로 명시 | Architecture §3.2 |
| E13 | 큐 대기가 길어짐 (동시 접수 > concurrency) | 정상 — RECEIVED 유지, 순서대로 처리 (내부 서비스·낮은 동시성 전제) | Architecture §3.2 |
| E14 | status DONE인데 result.json 부재 | GET result 500 — 저장 순서 불변식(§3.2) 위반 신호 | §3.2·§3.4 |
| E15 | 재검증 Fail | FAILED + 위반 요약(첫 5건+총수), result 미저장 | Domain §10.2 MUST |
| E16 | ALNS `IllegalStateException` (구조 결함) | FAILED + 원인 — 조용히 삼키지 않음 | Stage 4 N2, Domain §12 |
| E17 | body ≈ 12.4 MB (floor fixture 실측) | 수용 — 서블릿 raw body에 기본 크기 제한 없음. 상한 설정을 추가하지 않는다 (내부 서비스) | Plan §0 |
| E18 | orders 빈 배열 | 접수 수용 → 빈 해로 완주 (DONE, 전부 지표 0) | Stage 2 E19·Stage 4 E1 |
| E19 | 주문 수준 `taskTime`(규약 샘플에 존재)·`driverRestTimeRatio` ≠ 0 | 무시 — serviceTime 공식(Domain §3.3)과 canonical에 자리가 없음. `driverRestTimeRatio`는 **무시 확정, 거부 안 함** (Domain §2.5, 2026-08-12). 주문 `taskTime`의 거부 여부만 §10 Q5(D2 협의)에 남는다 | Domain §3.3·§2.5 |
| E20 | 차량 `driverSkill: "ALL"` | 문자 그대로 집합 {"ALL"} — 주문 요구 능력의 wire 원천이 없어 capability 축 전체가 휴면 (항상 통과) | Domain §3.4, §4.4 |
| E21 | reqDate가 RFC3339 (`2017-07-21T17:32:28Z`) | offset 버리고 벽시계로 통일 (잠정) | Domain §2.2 |
| E22 | 탐색이 시간 한도로 중단된 best | 정상 DONE (재검증 통과 시) | Domain §12 |
| E23 | floor fixture 차량 31대 start 키 없음 + depot `WIN_0` 1개 | adapter가 `startDepotLocId = "WIN_0"`을 채움. depot 2개 합성·start 명시 합성은 채우지 않음 | Domain §2.4·§4.4 |

---

## 8. 테스트 목록 — DoD 1:1 대응

위치: `app/src/test/java/com/ronext/rpdptw/app/` (하위는 패키지별). fixture JSON을 직접 읽는
테스트는 이 Stage부터 가능하다 (Jackson **3** 사용 가능 — Stage 1 §7에서 이관된 몫, Stage 0 §4.4).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `PlanJsonAdapterTest.parsesFloorFixture` | win_poc_case_floor.json 전체 파싱 → 건수(452·31·205,209)·실값 spot check (weight `26.2`→BigDecimal 원문, duration 300, qty 1, D 310708 int, options **매핑 7키**(wire 원문은 10키 — `driverRestTimeRatio`·`difficultySortType`·`customerAbbr` 3키는 무시, §4.6), dateRange) · **E23: 차량 31대 `startDepotLocId == "WIN_0"`** (현 규약 CVRPTW 채움) | (Plan 범위 문장 "규약 JSON adapter — win_poc fixture로 검증") |
| T2 | `PlanJsonAdapterTest.toleratesWireVariants` | 원본 win_poc_case.json → 문자열 수치 인코딩으로 INVALID_INPUT (공통 규칙 1·E3, Domain §3.1 — 수신 원형 보존 fixture) · itemId blank가 `ItemInput`에 그대로 실림(E4). 폴백 후 orderId는 정규화(Stage 1 T19) · `dueDate`·RFC3339(E21) 소형 케이스 · 소수 D/U(number)는 파싱 통과, BigDecimal 보존 — 거부는 정규화 몫 · E23 대조: depot 2개 또는 start 명시면 `startDepotLocId`를 채우지 않음 | 〃 |
| T3 | `SolveApiTest.acceptStoresInputAndStatus` | 유효 소형 JSON POST → 200 + solveKey 형식 · store에 input.json(원문 바이트 동일)+RECEIVED | (Plan 범위 문장 "접수 API — 검증→저장→200+solveKey") |
| T4 | `SolveApiTest.rejectsWithoutStoring` | 문법 오류·필수 누락 → 400 · **소수 거리·문자열 수치 등 → 400 (E3 — 2026-08-11 절차 2b·2026-08-12 공통 규칙 1)** · multiRotation `2`·`-1` → 422 UNSUPPORTED_INPUT · `-2` → 400 · 이 경우 전부 store 빈 상태 (E1b·E1c·E2). **대조군으로 `1`은 200 + 저장됨**을 같이 단언한다 (E1) — 게이트가 반대로 구현되는 것을 막는 지점이다 | 〃 (Domain §12 "S3 저장 없음") |
| T5 | `SolveKeyTest.issueAndObjectKeys` | 조립 형식·문자 집합 검증(E5)·runId 유일성·객체 key 3종·`of` 왕복 | (Plan 범위 문장 — Architecture §3.3 key 규칙) |
| T6 | `LocalSolveStoreTest.roundTrip` | put/get 왕복 3종 · 부재 시 `Optional.empty` | (Plan 범위 문장 "`SolveStore`(fake …)") |
| T7 | `SolveRunnerTest.happyPathToDone` | 소형 입력(짧은 한도) → Outcome DONE · result.json 존재·파싱 가능 · putResult가 최종 상태 기록보다 선행(§3.2 불변식) | (Plan 범위 문장 "SolveExecutor — 상태 전이") |
| T8 | `SolveRunnerTest.failurePathsRecordFailed` | (a) 소수 거리 입력을 store에 **직접 심고** run → FAILED "INPUT:…" (E3 — 접수를 우회한 입력에 대한 최종 방어) (b) 주입 Verifier가 Fail 반환 → FAILED "VERIFY:…" + result 미저장 (E15) (c) toMap 중복 키 → FAILED | 〃 (Stage 5 인계 "Fail → status FAILED 기록") |
| T9 | `SolveExecutorTest.heartbeatAndTransitions` | 짧은 주기 설정 → RECEIVED→RUNNING 전이·heartbeatAt 갱신 관찰 → DONE 후 heartbeat 미발생 (E10) | 〃 ("heartbeat") |
| T10 | `SolveApiTest.statusAndResultQueries` | 미존재 404 · RUNNING+오래된 heartbeatAt를 store에 심고 GET → "STALE" · DONE 전 result 409 · DONE 후 200 바이트 동일 (E14 포함) | (Plan 범위 문장 "조회 API"·"STALE") |
| T11 | `ResultJsonWriterTest.wireFormatGolden` | 손조립 `SolveResult` → §5 잠정안 필드·시각 포맷·decimal 변환(milli→"812.400") 정확 일치 | (Stage 5 인계 "result.json 직렬화") |
| T12 | `SolveE2eTest.postToDoneToResult` | @SpringBootTest(RANDOM_PORT)+local profile(@TempDir): 소형 규약 JSON POST → GET 폴링으로 DONE 대기(짧은 시간 한도) → GET result → metrics·routes 검증 | **"fake 저장소로 e2e 통합 테스트 — POST 접수 → DONE까지 → GET 결과"** |
| T13 | `FloorFixtureE2eTest` (조건부 — `-De2e.floor=true`류 태그, CI 기본 제외) | win_poc_case_floor.json POST → DONE(한도 600초, 최대 ~11분 폴링) → result 검증 + 재검증 통과(verified=true). **실행 가능하다** — Q1(D1) 해소로 `multiRotation: 1`이 그대로 접수된다 (2026-08-10) | **"win_poc_case_floor.json 접수·완주 (성공 기준 §0 달성 시점)"** |
| T14 | `SolveRunnerTest.resolvesProfileOnceAndSharesIt` | 스파이 `ProfileRegistry`로 `resolve` 호출이 정확히 1회임을 단언 + solver와 verifier에 전달된 `Profile`이 **동일 인스턴스**(`assertSame`)임을 단언 | (Domain §8.4 MUST — `Problem`이 profile을 담지 않게 된 이후 이것이 유일한 보장 지점) |
| T15 | `SolveRunnerTest.buildsBudgetFromWireThenConfig` | wire `Termination` 있음 → 그 값이 `AlnsConfig.timeLimitSec`, 없음 → `fallback-time-limit-sec` (E6b). idle 키 미설정 시 `empty` (E6c). 결과 run 메타에 `deliveryPolicy`와 `searchBudget`이 **각각** 실림 | (Domain §2.5.1·§11.1) |
| T16 | `AppArchitectureRulesTest.awsSdkOnlyInStorage` | `..app.storage..` 밖 → `software.amazon.awssdk..` 참조 금지 (N6) — 2026-08-11 개번, 종전 번호 T14가 중복이었다 | (Architecture §8 강제) |

`S3SolveStore`의 실 SDK 경로 확인(LocalStack)은 선택 사항으로 Stage 7에 둔다 (Architecture §6).
T1~T11·T14~T16은 Plan DoD 요약 문장 밖이지만 Plan Stage 6 범위 문장의 직접 검증이다 —
Plan §1의 편입(2026-08-11)에 따라 이 표 전부가 완료 기준이다.

---

## 9. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| Dockerfile 재작성·ECS Fargate 서비스·태스크 롤·CloudWatch·환경변수 배포 설정 | Stage 7 | Architecture §5, Plan Stage 7 |
| LocalStack e2e (실 S3 SDK 경로 확인) | Stage 7 (선택) | Architecture §6 |
| Win 지표 비교·탐색 파라미터 튜닝·seed 정책 변경 | Stage 8 | Plan Stage 8 |
| 결과·조회 wire의 **최종** 확정 (호출 시스템 협의) | 협의 후 (§5·§3.4는 잠정) | Domain §11.2, Architecture §3.4 |
| PICKUP_DELIVERY·PICKUP_ONLY의 wire 확장 (canonical은 이미 지원) | 협의 후 별도 | Master §6 |
| 고객 특화 Profile 구현·레지스트리 등록 (`builtIn()` = 전 고객 default) | 필요 시 별도 | Domain §8.4 |
| 자동 재개·큐 이관(SQS)·다중 태스크 수평 확장 | 범위 밖 (필요 시 설계 변경) | Architecture §3.2·§8 |
| 접수 인증·rate limit·요청 크기 상한 | 범위 밖 (내부 서비스 — 요구 없음) | Architecture §5 |
| status.json에 시각 3종(received/started/finished) 확장 | 안 함 — RunStamp 값은 in-process로 전달 (Architecture §3.3 형식 유지) | Architecture §3.3 |
| solver-core 코드·시그니처 변경 | 안 함 (그대로 소비) | Stage 1~5 |

---

## 10. 미해결 질문

닫힌 질문은 해소 표시를 달아 남긴다 ([README](README.md) 공통 규칙, 2026-08-13) —
Q1·Q3은 해소됐고, Q2·Q4·Q5는 [Plan §2.1 D2](../implementation-plan.md) 협의로 이관 상태다.
Stage 6 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | **multiRotation** (Stage 0 §11 Q2 → Stage 1 §9 Q1 인계): 두 fixture 모두 `multiRotation: "1"`이라 최종 성공 기준 fixture(Plan §0)가 접수(§3.1-2)에서 422로 거부된다 | **해소 (2026-08-10, [Plan §2.1 D1](../implementation-plan.md) 확정).** 숫자는 **차량이 도는 바퀴 수**이고 값 `1` = 1바퀴 = **지원 범위 안**이다 — fixture는 그대로 접수되며 **Stage 6에 남은 선행 조건은 없다**(T13 실행 가능). 판정은 `!= 0` 거부에서 **통과 = `{0, 1}`**로 반전됐다 (§3.1-2·E1\~E1c·T4). 판정 규칙은 `PlanNormalizer`(Stage 1 절차 2)와 접수 게이트(§3.1-2)가 **같은 조건**을 갖는다 — adapter 단독 우회 금지(의미 규칙은 solver-core 소유)는 그대로다. **주의: 규약 PDF 문면은 이 숫자를 복귀 횟수로 읽게 적혀 있어 PDF만 보고 게이트를 짜면 정확히 반대가 된다** (Domain §2.5 주석) |
| Q2 | **customerId의 wire 원천** (Stage 1 §9 Q6 인계): fixture에 plan `shprId="S3853"`와 주문 수준 `customerId="WINCOMMERCE"`가 공존한다. profile 선택 키·S3 key 세그먼트가 어느 쪽인지 협의 필요 (profile 레지스트리 키 명명에 직결) | **[Plan §2.1 D2](../implementation-plan.md)(wire 협의)로 이관 (2026-08-10).** 그때까지 Domain §2.2의 명시 대응(`shprId`)을 매핑 (§4.1). 주문 수준 customerId는 무시. 현재는 어느 쪽이든 default profile이라 동작 차이 없음 |
| Q3 | **접수 4xx의 깊이**: Domain §12는 "소수 거리·알 수 없는 vhclOwnTyp"(당시 문면 — `vhclOwnTyp`은 2026-08-11 유예로 현행 §12에서 빠졌다)을 접수 4xx(S3 저장 없음)로 분류하는데, Architecture §3.1은 "canonical 변환은 접수에서 하지 않고 의미 오류는 풀이 단계 FAILED"라 한다 — 두 정본이 충돌 | **해소 (2026-08-11 결정).** Domain §12를 따른다 — 접수가 `PlanNormalizer.normalize`까지 실행해 의미 오류도 4xx·저장 없음 (§3.1 절차 2b·N1·E3·T4). Architecture §3.1이 같은 날 개정됐다. executor 쪽 정규화는 최종 방어로 유지 (T8a) |
| Q4 | **wire 최종 협의 부재**: 결과 JSON 필드명·시각 timezone 표기·단위 표현(§5), 조회 경로(§3.4), HTTP 상태 배정(§2.4) — Domain §11.2·Architecture §3.4가 "협의 확정"으로 열어 둠. 협의 상대가 현재 없음 | **[Plan §2.1 D2](../implementation-plan.md)로 이관 (2026-08-10).** "협의 상대를 찾는 것"이 D2의 첫 작업으로 올라갔다. 그때까지 §5·§3.4의 잠정안으로 구현·테스트(T11 golden). 협의 후 변경은 wire 표기만 — 의미(Domain §11.1)는 불변 |
| Q5 | **legacy 시간 필드의 처리**: 규약 샘플의 주문 수준 `taskTime`, `driverRestTimeRatio`(스펙 샘플 값 0.15) — canonical·serviceTime 공식에 자리가 없어 무시하면 호출 측 기대와 조용히 어긋날 수 있다 | **[Plan §2.1 D2](../implementation-plan.md)로 이관 (2026-08-10)** — 호출 측이 이 값을 무엇으로 기대하는지가 답이라 협의 항목이다. 그때까지 무시 (E19 — 공식 밖 입력 미사용). **`driverRestTimeRatio`는 부분 종결 (2026-08-12, 시스템 소유자)** — 무시 확정, 0이 아닌 값도 거부하지 않는다 (Domain §2.5, 수용된 위험). 주문 수준 `taskTime`의 처리만 D2에 남는다. **주의: `driverRestTimeRatio`(운전 시간에 비례한 휴식)는 Domain §7.3의 `interWorkWindowRestTime`(근무창 사이의 야간 휴식)과 다른 개념이다** — D4가 후자를 실계산하게 됐다고 이 필드가 해소되는 것이 아니다 |
