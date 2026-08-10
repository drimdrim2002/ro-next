---
title: Stage 5 — 재검증과 결과 (상세 구현 설계)
stage: 5
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§10 재검증, §11 결과 JSON, §6 Solution·XOR, §7 전파, §3.4 호환성, §12 오류 분류)
  - ../architecture-design.md (§2 모듈·패키지, §2.1 경계 규칙, §3.3 S3 배치)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준, §6 ArchitectureRulesTest)
  - stage-01-canonical-input-normalization.md (§2 canonical 모델, §3 TimeBase.toWallClock)
  - stage-02-travel-and-problem-freeze.md (§2.3 Problem 조회 메서드)
  - stage-03-solution-propagation-evaluation.md (§3.2 VisitFacts·RouteFacts·Evaluation, §4.3 Profile·Scores, N1·N2·N6)
  - stage-04-initial-solution-and-alns.md (§3.2 AlnsResult, N6)
revisions:
  - 2026-08-10 최초 작성
  - 2026-08-10 3계층 반영 — `profile`→`eval` 배치, profile 인자화, score 배열 대조 추가
---

# Stage 5 — 재검증과 결과

solver-core의 `verify` 패키지에 탐색과 분리된 독립 재검증(`SolutionVerifier`)과
결과 모델(`SolveResult`)·미배정 사유 산출(`ResultAssembler`)을 만든다.
주 근거: [Domain §10–§11](../domain-design.md). Stage 3이 core `eval`에 배치한 공유 값 타입
(`VisitFacts`·`RouteFacts`·`Evaluation`·`Scores`)과 Stage 4의 `AlnsResult`(best +
bestEvaluation + bestScore)를 그대로 잇는다 — 같은 개념에 새 이름을 짓지 않는다.

**재검증은 탐색 예산을 보지 않는다** (Domain §2.5.1·§10.2 MUST NOT). `AlnsConfig`가 `solve`에
있고 `verify ↛ solve`가 ArchUnit으로 막히므로, 시간·step·idle 한도는 애초에 도달 불가능하다.

**DoD** ([Plan Stage 5](../implementation-plan.md)): 일부러 오염시킨 해(짝 분리·용량 초과·
점수 불일치)가 전부 FAIL · ArchUnit 규칙 통과.

핵심 구도 — 검증을 통과한 값만이 결과의 재료가 된다 (Master §3-⑥ 유일한 발행 규칙):

```text
[AlnsResult (Stage 4)]   [Problem (동결, Domain §5)] + [Profile (탐색과 같은 인스턴스, §8.4 MUST)]
   best ──(Stage 6이 분해)──▶ routes: Map<VehicleId, List<NodeId>> + bank: Set<RequestId>
   bestEvaluation ────────────▶ reported Evaluation (③ 대조 대상, §10.2)
   bestScore ─────────────────▶ reported score       (④ 대조 대상, §10.2)
                                        │
                              [SolutionVerifier.verify]  ← solve 참조 금지 (ArchUnit)
                                구조(pair·XOR) → 경로 재전파(RouteReplay, 처음부터)
                                → 호환·profile hard → metric·score 재계산 대조
                                        │
                     ┌── Fail(위반 목록) ──▶ 결과 없음 — Stage 6이 status FAILED 기록 (§10.2 MUST)
                     └── Pass(재계산 Evaluation·score·RouteFacts·bank)
                                        │
                              [ResultAssembler.assemble] + RunStamp(Stage 6 제공)
                                        ▼
                              [SolveResult (Domain §11.1 의미 1:1)] → Stage 6이 result.json 직렬화
```

---

## 1. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/verify/` (Architecture §2, Stage 0 §3.1).
`package-info.java`(Stage 0 생성)는 유지한다.

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `verify/SolutionVerifier.java` | 진입점: 해 전체를 캐시 없이 재검증 → `VerificationResult` | Domain §10.2 |
| `verify/VerificationResult.java` | sealed: `Pass`(재계산 값) / `Fail`(위반 목록) | Domain §10.2·§12 |
| `verify/VerifyViolation.java` | 위반 한 건: Kind + 대상 ID + 상세 문자열 (FAILED 원인 기록용) | Domain §10.2·§12 |
| `verify/RouteReplay.java` | 경로 하나를 §7 공식으로 처음부터 재계산 — solve와 독립된 **두 번째 구현** (package-private) | Domain §10.1·§7 |
| `verify/SolveResult.java` | 결과 모델 (Domain §11.1 의미 목록 1:1, 하위 record 중첩) | Domain §11.1 |
| `verify/UnassignedReason.java` | 미배정 사유 enum 4값 | Domain §11.1 |
| `verify/RunStamp.java` | app이 아는 실행 사실 운반체 (inputKey·시각 3종·탐색 예산) | Domain §11.1 run |
| `verify/ResultAssembler.java` | `Pass` + `RunStamp` → `SolveResult` 조립 + 미배정 사유 산출 | Domain §11.1 |

기존 파일 수정 1건: `solver-core/src/test/java/com/ronext/rpdptw/ArchitectureRulesTest.java`에
`@ArchTest` 규칙 1개 추가 (§6). Stage 3·4 산출물(`solve`·`eval`·`problem`)은 수정하지 않는다.

결과 모델이 `verify`에 있는 이유: Architecture §2 트리에 Domain §11의 자리가 따로 없고
(§9 Q1), 재검증 통과가 결과의 유일한 생산 조건이며(Master §3-⑥) 미배정 사유 산출이
`RouteReplay`를 재사용하기 때문이다 (§5.3).

---

## 2. 재검증 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.

### 2.1 진입점 (Stage 3 N6·Stage 4 N6 이행)

`Solution`은 `solve` 소유 타입이라 verify가 받을 수 없다 (ArchUnit `verify ↛ solve`,
Architecture §2.1). verify는 domain 타입 분해값과 탐색이 보고한 `Evaluation`만 받는다 —
**진입 타입 자체가 독립성 계약이다**: 탐색의 증분 캐시·내부 상태는 입력에 존재할 수 없다 (§10.2).

```java
public final class SolutionVerifier {
    /**
     * 결과 저장 직전 1회, 해 전체(경로 + bank)를 캐시 없이 재검증한다 (Domain §10.2).
     * Problem·이동표·profile은 읽기만 한다. routes의 List<NodeId>는 방문 순서 그대로다.
     * profile은 탐색에 넘긴 것과 같은 인스턴스여야 한다 (Domain §8.4 MUST — 호출자 책임).
     * 탐색 예산은 인자에 없다 — 재검증은 예산과 무관하게 해 전체를 본다 (§2.5.1).
     */
    public static VerificationResult verify(Problem problem,
                                            Profile profile,
                                            Map<VehicleId, List<NodeId>> routes,
                                            Set<RequestId> bank,
                                            Evaluation reportedEvaluation,
                                            long[] reportedScore);
}

public sealed interface VerificationResult {
    /** 검증 통과. 담긴 값은 전부 재계산본 — 결과 조립의 유일한 원천 (노트 N4). */
    record Pass(Evaluation evaluation,                  // ③ reported와 동등 확인이 끝난 재계산 값
                long[] score,                           // ④ 〃 (Arrays.equals로 대조)
                Map<VehicleId, RouteFacts> routeFacts,  // §7.3 공식으로 다시 채운 사실
                Set<RequestId> bank)                    // "재검증을 통과한 해의 미배정 집합" (§11.1)
        implements VerificationResult {}
    record Fail(List<VerifyViolation> violations) implements VerificationResult {}
}
```

- `Solution → (routes 맵, bank)` 분해는 호출자(Stage 6 executor)의 일이다.
  `Solution`(record)이 정확히 이 분해값을 노출하므로 변환은 자명하다 (Stage 3 N6).
  분해 시 `toMap`이 중복 `VehicleId`에서 던지는 예외는 버그 신호로 그대로 둔다 (노트 N3).
- `reportedEvaluation`·`reportedScore` = `AlnsResult.bestEvaluation`·`bestScore` (Stage 4 §3.2 —
  best 승격 시점의 정식 평가값). 탐색의 `bestRouteFacts`는 verify가 받지 않는다 — 대조 대상은
  이 두 값뿐이다 (§10.2, §8).
- **`long[]` 대조는 `Arrays.equals`로 한다** — Stage 3 §4.3의 전 Stage 공통 규칙.
  `Pass`/`AlnsResult`/`Feasible`의 자동 생성 `equals`에 기대면 안 된다.

### 2.2 위반 모델

```java
public record VerifyViolation(Kind kind,
                              Optional<VehicleId> vehicleId,
                              Optional<RequestId> requestId,
                              Optional<NodeId> at,
                              String detail) {          // 예: "expected=…, recomputed=…" — FAILED 원인 기록 (§12)
    public enum Kind {
        // 구조 (Domain §1.4·§6.3) — solve.StructureViolation과 같은 의미, 독립 구현 (노트 N2)
        UNKNOWN_VEHICLE, EMPTY_ROUTE, UNKNOWN_NODE, DUPLICATE_NODE,
        PAIR_SPLIT, PAIR_INCOMPLETE, PICKUP_AFTER_DELIVERY,
        ASSIGNED_AND_BANKED, NOT_ASSIGNED_NOT_BANKED, UNKNOWN_REQUEST,
        // hard 재계산 (Domain §6.2·§7) — solve.Violation과 같은 의미, 독립 구현
        TIME_WINDOW, REQ_DATE, WORK_WINDOW,
        CAPACITY_WEIGHT, CAPACITY_VOLUME, END_LOAD_NOT_ZERO,
        MAX_STOP_COUNT, MAX_DRIVE_TIME, MAX_DRIVE_DIST,
        INCOMPATIBLE_VEHICLE, PROFILE_HARD,
        // 점수 대조 (Domain §10.2·§6.4)
        SCORE_MISMATCH
    }
}
```

### 2.3 경로 재계산 (두 번째 구현)

```java
final class RouteReplay {                               // package-private — verify 내부 전용
    sealed interface Outcome {
        record Ok(RouteFacts facts) implements Outcome {}
        record Violated(VerifyViolation violation) implements Outcome {}
    }
    /** Domain §7.1 공식을 처음부터: initialLoad → 출발(waitInDepot) → 방문 루프
        (arrival→대기→시간창→reqDate→load 곡선) → endDepot → 근무창 → 한도 → 종료 load == 0.
        이동은 problem.travel()만 (§4 MUST). solve.RoutePropagator 코드를 재사용하지 않는다. */
    static Outcome replay(Problem problem, VehicleId vehicleId, List<NodeId> visits);
}
```

- `RouteReplay`는 solve의 `RoutePropagator`와 **같은 의미를 독립 코드로** 다시 구현한다
  (Domain §10.1 — 탐색 코드 버그를 잡는 이중 기입). 공유하는 것은 `Problem`의 동결 사실과
  `profile`의 값 타입·공식(`RouteFacts.routeOperationalTimeSec()` 등)뿐이다 (노트 N2).
- Stage 3이 잠정 확정한 해석(§9 Q1 serviceStart ≤ close, Q2 depot 창 미적용, Q3 단일
  근무창·rest 0)을 **동일하게** 따른다 — 해석이 갈리면 두 구현의 값이 어긋나 T5가 실패한다.

---

## 3. 재검증 절차 (`SolutionVerifier.verify`)

번호 순서대로. 위반은 던지지 않고 수집한다 (Stage 6이 FAILED 원인으로 기록). 결정성을 위해
경로는 `VehicleId` 문자열 순, bank는 `RequestId` 문자열 순으로 순회한다.

```text
1. 참조    각 VehicleId가 Problem에 존재 (UNKNOWN_VEHICLE). visits 빈 목록 (EMPTY_ROUTE).
          각 NodeId를 problem.nodeRef로 역참조 — 실패는 UNKNOWN_NODE
          (DELIVERY_ONLY의 가짜 픽업 NodeId는 색인에 없어 여기서 자동 검출, §1.3 MUST NOT).
          해 전체 NodeId 중복 (DUPLICATE_NODE). bank의 RequestId 존재 (UNKNOWN_REQUEST).
2. pair   RequestId별 등장 방문 수집 (§1.4):
          PICKUP_DELIVERY — 두 방문이 다른 경로 → PAIR_SPLIT. 한쪽만 → PAIR_INCOMPLETE.
          같은 경로에서 pickup index > delivery index → PICKUP_AFTER_DELIVERY.
3. XOR    소유된 RequestId가 bank에도 있음 → ASSIGNED_AND_BANKED.
          problem.requests() 전체 중 소유도 bank도 아님 → NOT_ASSIGNED_NOT_BANKED (§6.3).
4. 관문    1–3 위반이 하나라도 있으면 여기서 Fail — 구조가 깨진 경로의 물리 재계산은
          무의미하다 (Domain §6.5의 순서: 구조 검사 → 정식 평가와 동형).
5. 재전파  경로마다 RouteReplay.replay (§2.3) — 적재 곡선(0 ≤ load ≤ capacity, 종료 0),
          시간창·reqDate·근무시간, maxStop·maxDrive 한도까지 전부 다시 계산.
          위반 경로는 기록하고 나머지 경로도 계속 검사한다 (원인을 최대한 모아 보고).
          "이동표 사용"(§10.2)은 검사 항목이 아니라 구조다 — replay가 problem.travel()
          밖의 어떤 이동값도 얻을 수 없다 (표는 모든 쌍 완비, Stage 2 §4 절차 5).
6. 호환    경로가 소유한 각 RequestId에 대해 vehicleId ∈ problem.compatibleVehicles(id)
          (§3.4 동결 사실). 위반 → INCOMPATIBLE_VEHICLE (노트 N5).
7. profile hard  profile.hardConstraints()의 각 h를 h.satisfied(problem, 재계산 RouteFacts)로 적용.
          위반 → PROFILE_HARD (detail에 h.id()). 인자로 받은 profile을 쓸 뿐 verify가
          따로 고르지 않는다 — 탐색과 같은 인스턴스인지는 호출자가 보장한다 (§8.4 MUST).
8. metric  recomputed = Evaluation.aggregate(재계산 facts 전부, bank.size()).
          reportedEvaluation과 record 비교로 다르면 SCORE_MISMATCH (detail에 두 값).
9. score   recomputedScore = profile.score(problem, recomputed, 재계산 facts 전부).
          reportedScore와 Arrays.equals가 아니면 SCORE_MISMATCH (detail에 두 배열).
          (§6.4 — 같은 SoT인데 캐시 값 ≠ 재계산 값이면 버그. 8·9는 각각 독립 검사다:
           metric이 같아도 score가 다를 수 있다 — profile 구현이 비결정적이면 그 자체가 버그.)
10. 판정   위반 0 → Pass(recomputed, recomputedScore, 재계산 facts, bank).
          아니면 Fail(수집한 위반 전부).
```

비용: 경로 ~30 × 방문 ~20 규모에서 전체 재검증 1회는 병목이 아니다 (Domain §10.3).
재검증은 매 trial이 아니라 결과 저장 직전 1회다 (§10.2 — 호출 시점 결선은 Stage 6).

---

## 4. 결과 모델과 조립 (Domain §11)

### 4.1 `SolveResult` — §11.1 의미 목록과 1:1

```java
public record SolveResult(
    String planId,
    Status status,                        // 항상 DONE — FAILED 결과는 생산 경로가 없다 (노트 N6)
    Run run,
    List<RouteResult> routes,             // VehicleId 문자열 오름차순 (결정성)
    List<Unassigned> unassigned,          // RequestId 문자열 오름차순
    Evaluation metrics) {                 // §11.1 metrics 4항목 = Evaluation 그대로 — 새 타입 없음

    public enum Status { DONE, FAILED }

    public record Run(String inputKey,
                      LocalDateTime receivedAt, LocalDateTime startedAt, LocalDateTime finishedAt,
                      String profileId, boolean verified,
                      DeliveryPolicy deliveryPolicy,       // 유효한 답을 정한 값 (Domain §2.5)
                      SearchBudget searchBudget) {}        // 언제 멈췄나 (Domain §2.5.1) — 따로!

    /** AlnsConfig의 예산 부분만 결과용으로 옮긴 값. 알고리즘 튜닝 파라미터는 담지 않는다. */
    public record SearchBudget(long timeLimitSec, OptionalLong maxSteps,
                               OptionalLong idleSteps, OptionalLong idleSec,
                               long seed, String termination) {}

    public record RouteResult(VehicleId vehicleId, List<Visit> visits,   // visits = 방문 순서
                              long driveDistMeter, long driveTimeSec,
                              int stopCount, long routeOperationalTimeSec) {}

    public record Visit(RequestId orderId,               // 식별자는 규약과 같은 이름 (§11.1)
                        boolean pickup,                  // PD의 같은 orderId 방문 2건 구분 (§9 Q4)
                        LocationId locationId,
                        LocalDateTime arrival, LocalDateTime serviceStart, LocalDateTime serviceEnd,
                        long loadWeightMilliKg, long loadVolumeMilliCbm) {}  // 방문 처리 후 적재

    public record Unassigned(RequestId orderId, UnassignedReason reason) {}
}

public enum UnassignedReason {                           // §11.1의 예시 목록 그대로 — 추가하지 않음
    NO_COMPATIBLE_VEHICLE, CAPACITY, TIME_WINDOW_INFEASIBLE, NOT_PLACED
}

public record RunStamp(String inputKey, LocalDateTime receivedAt,
                       LocalDateTime startedAt, LocalDateTime finishedAt,
                       SolveResult.SearchBudget searchBudget) {}   // app이 적용한 예산 (§4.1)

public final class ResultAssembler {
    /** 검증 통과 값에서만 결과를 만든다 — Fail 경로에는 결과가 없다 (Domain §10.2 MUST).
        profile은 run.profileId 기록용 — Problem에 없으므로 인자로 받는다. */
    public static SolveResult assemble(Problem problem, Profile profile,
                                       VerificationResult.Pass pass, RunStamp stamp);
}
```

- 시각은 `timeBase.toWallClock`(Stage 1 §3 — "결과 JSON 역변환용"으로 예약된 메서드)로
  벽시계 `LocalDateTime`으로 되돌린다. timezone 없는 §2.2 표기 관례를 따르며, 문자열 포맷과
  wire 필드명·단위 표현(milli → decimal kg 등)은 Stage 6이 호출 시스템과 협의해 확정한다 (§11.2).
- `metrics`를 `Evaluation`으로 재사용하는 근거: §11.1의 metrics 목록(unassignedCount·
  usedVehicleCount·totalDistance·totalRouteOperationalTime)이 Stage 3 `Evaluation`의 4개
  성분과 정확히 일치한다. 같은 개념에 새 이름 금지.

### 4.2 조립 절차 (`ResultAssembler.assemble`)

```text
1. routes    pass.routeFacts()를 VehicleId 문자열 순으로. RouteResult 필드는 전부
            재계산 RouteFacts에서: 경로 지표 4개(§11.1) + 방문별 Visit
            (VisitFacts의 requestId·pickup·locationId·시각 3종 toWallClock·처리 후 적재).
2. unassigned  pass.bank()를 RequestId 문자열 순으로, 사유는 §4.3 절차로 산출.
3. metrics   = pass.evaluation() (재계산 값 — 탐색 보고값이 아니라).
4. run       = Run(stamp의 4개 값, profileId = profile.id(), verified = true,
                   deliveryPolicy = problem.deliveryPolicy(), searchBudget = stamp의 예산 요약).
            배송정책과 탐색 예산을 **따로** 기록한다 (Domain §11.1 — 같은 문제를 다른
            예산으로 돌린 결과를 구별할 수 있어야 한다).
5. 반환      SolveResult(problem.planId(), Status.DONE, run, routes, unassigned, metrics).
```

탐색의 `bestRouteFacts`는 조립에 쓰지 않는다 — 결과의 모든 수치는 verify의 재계산본에서만
나온다 (노트 N4). "검증된 해 ↔ 결과 JSON 일치"는 별도 실행 단계가 아니라 테스트 T9가
보장한다 (Domain §10.2 — 2단 verifier 폐기 결정의 이행).

### 4.3 미배정 사유 산출 절차

bank는 ID만 갖고 있으므로(§6.3 MUST NOT) 사유는 여기서 계산한다 (§11.1). 축을 차례로
좁히는 결정적 절차다 — 혼합 원인의 모호함이 생기지 않는다 (§5.3 노트).

```text
각 r ∈ pass.bank()에 대해:
1. V₀ = problem.compatibleVehicles(r)              (§3.4 동결 사실)
   V₀ = ∅ → NO_COMPATIBLE_VEHICLE.
2. V₁ = { v ∈ V₀ : r.totalWeightMilliKg ≤ v.maxWeightMilliKg
                 ∧ r.totalVolumeMilliCbm ≤ v.maxVolumeMilliCbm }
   V₁ = ∅ → CAPACITY                               (호환 차량 전부가 혼자서도 실을 수 없음)
3. V₂ = { v ∈ V₁ : RouteReplay.replay(problem, v, r만 있는 단독 경로) = Ok }
   단독 경로: DELIVERY_ONLY → [delivery NodeId], PICKUP_DELIVERY → [pickup, delivery].
   V₂ = ∅ → TIME_WINDOW_INFEASIBLE                 (혼자 넣어도 시간창·reqDate·근무시간·
                                                    maxDrive 등에서 불가 — §9 Q2 라벨 폭 주의)
4. V₂ ≠ ∅ → NOT_PLACED                             (단독으로는 가능 — 탐색이 다른 배정과
                                                    함께 자리를 못 찾았을 뿐)
```

비용: |bank| × |호환 차량| 회의 단독 경로 replay — fixture 규모(452×31 상한)에서 무시 가능.

---

## 5. 설계 노트

| # | 내용 |
|---|---|
| N1 | **의도적 중복**: `RouteReplay`·구조 검사·위반 enum은 solve의 `RoutePropagator`·`StructureCheck`·`Violation`과 같은 의미의 **별도 코드**다. 코드를 공유하면 같은 버그가 양쪽에 숨어 재검증이 무의미해진다 (Domain §10.1). 공유가 허용되는 것은 `Problem`의 동결 사실(이동표·호환성)·인자로 받은 같은 `Profile` 인스턴스(§10.2가 명시한 같은 입력)·core `eval`의 값 타입과 공식(`RouteFacts`·`Evaluation.aggregate`·`Scores.compare` — Stage 3 N1이 이 목적으로 배치)뿐이다. `profile.score`도 공유 대상이다 — 재검증의 목적은 "탐색이 그 profile의 목적식을 정직하게 계산했는가"이지 목적식 자체를 다시 정의하는 것이 아니다 |
| N2 | 위반 enum을 solve와 별도로 두는 것은 N1의 결과다 — `solve.Violation`을 쓰면 `verify → solve` 참조가 생겨 ArchUnit이 막는다. 이름은 의미가 같은 항목끼리 동일하게 맞춘다 (리뷰 용이) |
| N3 | **중복 차량 경로는 verify에 도달하지 못한다**: 진입 타입이 `Map<VehicleId, …>`라 (Stage 3 N6 확정) 한 차량 두 경로는 표현 불가다. 호출자의 `toMap` 분해가 중복 키에서 던지는 예외가 곧 구조 결함 신호이며, Stage 6 executor는 이를 FAILED로 기록한다. ALNS 정상 경로에서는 Stage 4 N2(구조 위반 = 예외)가 이미 걸러 준다 |
| N4 | **결과는 재계산본으로만 조립한다**: 탐색의 `bestRouteFacts`는 검증의 대조에도 결과 조립에도 쓰지 않는다. "탐색의 증분 캐시·내부 상태를 믿지 않는다"(§10.2)의 연장 — 결과 수치의 원천이 검증 코드 하나로 좁혀져, 검증된 해 ↔ 결과 일치가 구조적으로 따라온다 |
| N5 | Domain §10.2 검사 목록에 호환성(§3.4)이 명시돼 있지 않으나, 호환성은 독립 hard 축이고(§3.4) Master §2-③이 "hard 규칙 전부 확인"을 요구하므로 검사에 포함한다. 누락으로 읽으면 비호환 배정이 결과로 새는 구멍이 생긴다 |
| N6 | `Status.FAILED`는 현재 생산 경로가 없다: 재검증 FAIL이면 결과를 저장하지 않고(§10.2) result.json은 DONE일 때만 존재한다(Architecture §3.3). §11.1이 status에 FAILED를 나열하므로 enum 값은 두되, `assemble`은 항상 DONE이다 (§9 Q3) |
| N7 | `Run.verified`도 같은 이유로 항상 true다. §11.1 run 항목("재검증 통과 여부")의 문자적 이행이며, 결과를 직접 읽는 호출 시스템에 대한 자기 서술 값이다 |

---

## 6. ArchUnit 규칙 (DoD "ArchUnit 규칙 통과")

Stage 0 §6의 `ArchitectureRulesTest`에서:

- 기존 `VERIFY_MUST_NOT_DEPEND_ON_SOLVE` — Stage 5에서 처음으로 실제 검사 대상 클래스가
  생긴다 (`allowEmptyShould(true)`는 무해하므로 유지). verify가 `Solution`·`RoutePropagator`
  등을 import하는 순간 빌드가 깨진다.
- 추가 1건 (N1의 역방향 보호 — solve가 verify 코드를 재사용하면 이중 기입이 무너진다):

```java
@ArchTest
static final ArchRule SOLVE_MUST_NOT_DEPEND_ON_VERIFY =
    noClasses().that().resideInAPackage("com.ronext.rpdptw.solve..")
        .should().dependOnClassesThat().resideInAPackage("com.ronext.rpdptw.verify..")
        .allowEmptyShould(true);
```

패키지 의존 최종 그림 (Stage 3 §4.4의 완성): `verify → domain, problem, eval`.
solve와 verify는 서로를 모른다.

---

## 7. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | 경로 0개, 전 Request bank | PASS (유효한 해). 결과: routes=[], 전건 unassigned+사유, metrics(unassigned=n, used=0, 거리 0) | §9.1·§11.1 |
| E2 | reported Evaluation의 한 축만 1 차이 | SCORE_MISMATCH → FAIL (detail에 양쪽 값) | §6.4·§10.2 |
| E3 | PD의 pickup·delivery가 다른 경로 (짝 분리) | PAIR_SPLIT → FAIL (DoD 오염 1) | §1.4 |
| E4 | 경로 수요 합 > capacity (용량 초과) | CAPACITY_* → FAIL (DoD 오염 2). 출발 적재 초과(DELIVERY_ONLY 몰림)도 동일 | §6.2 |
| E5 | DELIVERY_ONLY의 pickup NodeId가 경로에 | UNKNOWN_NODE (nodeRef 색인에 없음 — 구조적 강제) | §1.3 MUST NOT |
| E6 | 구조 위반 존재 | 재전파·점수 대조 생략, Fail에는 구조 위반만 (관문 §3-4) | §6.5 순서 |
| E7 | hard 위반 경로 여럿 | 경로별로 전부 수집해 보고 (첫 경로에서 멈추지 않음) | §12 원인 기록 |
| E8 | serviceStart == close / == reqDate | 통과 — Stage 3 Q1 해석과 동일 공식 (두 구현이 같은 잠정 해석 공유, §2.3) | §2.3·Stage 3 §9 Q1 |
| E9 | waitInDepot=Y 해 | replay도 같은 출발 규칙 (Stage 3 §3.3 절차 1) — 다르면 시각 전체가 어긋나 T5에서 검출 | §2.5 |
| E10 | 종료 load ≠ 0 | pair 완비 구조에서는 산술적으로 불가 — 검사는 replay 자체 버그·모델 변화 방어 (END_LOAD_NOT_ZERO) | §6.2·Stage 3 N3 |
| E11 | bank에 호환 0대 Request | 검증엔 무영향, 사유 NO_COMPATIBLE_VEHICLE | §3.4·§11.1 |
| E12 | 사유 산출에서 시간은 되는데 maxDriveDist만 불가 | TIME_WINDOW_INFEASIBLE (단독 불가 bucket — 라벨 폭은 §9 Q2) | §4.3 절차 3 |
| E13 | 단독으로는 가능한데 bank에 남음 | NOT_PLACED (탐색 품질이지 결함 아님) | §9.1·§11.1 |
| E14 | 비호환 차량에 배정된 채 verify 도달 | INCOMPATIBLE_VEHICLE → FAIL (노트 N5) | §3.4 |
| E15 | 항상-false profile hard | PROFILE_HARD → FAIL (탐색·검증이 같은 profile이므로 정상 경로에선 탐색도 못 만든 해) | §8.4 |
| E16 | 호출자가 탐색과 **다른** profile 인스턴스를 넘김 | verify는 알아챌 수 없다 — 인자를 믿는다. 이 결선은 Stage 6의 단일 지점이 보장하고 그 테스트가 지킨다 | §8.4 MUST |
| E17 | `reportedScore`와 재계산 score의 길이가 다름 | SCORE_MISMATCH (같은 profile이면 길이가 같아야 한다 — Stage 3 E31) | §8.3 |
| E16 | routes 맵 값이 빈 목록 | EMPTY_ROUTE → FAIL (`Route`는 빈 visits를 못 만들지만 verify 입력은 raw 맵 — 방어) | Stage 3 E30 |
| E17 | PD 결과의 같은 orderId 방문 2건 | Visit.pickup으로 구분 (§9 Q4) | §1.3 |
| E18 | 시간 한도로 중단된 탐색의 best | 정상 입력 — verify는 출처를 모른다. hard만 재확인 | §12 "탐색 중단 = 정상" |

---

## 8. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/verify/`. 의존은 JUnit·ArchUnit만.
테스트 코드는 solve 타입을 함께 써도 된다 (`DoNotIncludeTests`, Stage 0 §6) — 오염된 해를
`Solution`으로 조립한 뒤 분해해 verify에 넣는 방식으로 DoD의 "일부러 오염"을 구현한다.
Problem은 Stage 1~3 경로로 손 조립한다 (fixture JSON 파싱은 Stage 6 — Stage 1 §7 원칙).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `SolutionVerifierTest.failsOnPairSplit` | 유효 해에서 PD의 pickup만 다른 경로로 옮긴 오염 → `Fail`, Kind = PAIR_SPLIT (E3) | "짝 분리 … FAIL" |
| T2 | `SolutionVerifierTest.failsOnCapacityExceeded` | 수요 합 > maxWeight 경로 → CAPACITY_WEIGHT. volume 변형·출발 적재 초과(E4) 포함 | "용량 초과 … FAIL" |
| T3 | `SolutionVerifierTest.failsOnScoreMismatch` | 유효 해 + reported `Evaluation` 4축 각각 ±1 조작 → 전부 SCORE_MISMATCH (E2). **그리고** `Evaluation`은 그대로 두고 `reportedScore`만 조작 → 역시 SCORE_MISMATCH (E17 길이 불일치 포함) | "점수 불일치 … FAIL" |
| T4 | `ArchitectureRulesTest` 2규칙 green | verify 실제 클래스 존재 상태에서 `verify ↛ solve` + 신규 `solve ↛ verify` (§6) | "ArchUnit 규칙 통과" |
| T5 | `SolutionVerifierTest.passesValidSolutionAndAgreesWithSolve` | Stage 3 §3.4 예제를 pair 완비로 확장한 유효 해 → `Pass`. 재계산 `RouteFacts`·`Evaluation`이 solve의 `Evaluator.evaluate(problem, profile, solution)` 결과와 record 동등이고, score는 `Arrays.equals` — **두 독립 구현의 합치** (E8·E9 경계 포함). `Feasible`/`Pass` 자체를 `equals`로 비교하지 않는다 (배열 필드 — Stage 3 §4.3 공통 규칙) | (오염 FAIL의 대조군 — 전부 FAIL이 "verify가 무조건 FAIL"이 아님의 증명) |
| T6 | `SolutionVerifierTest.failsOnXorViolations` | E1(위반 0)·ASSIGNED_AND_BANKED·NOT_ASSIGNED_NOT_BANKED·UNKNOWN_REQUEST·E16 | (Plan 범위 문장 "독립 재검증(전체 해)" — §10.2 검사 항목) |
| T7 | `SolutionVerifierTest.failsOnIncompatibleAndProfileHard` | E14·E15 각각 정확한 Kind (같은 profile 인스턴스 사용 확인 포함) | 〃 |
| T8 | `AlnsVerifyIntegrationTest.alnsBestAlwaysVerifies` | 소형 fixture × seed 여러 개로 `AlnsSolver.solve(problem, profile)` → `AlnsResult` 분해 → 같은 profile로 verify `Pass` + `Pass.evaluation` == `bestEvaluation` + `Arrays.equals(Pass.score, bestScore)` (Stage 4 T4의 verify판 — 파이프라인 전 구간 정합) | (§10 전체 흐름의 실증 — 캐시 없는 재계산과 탐색 보고값의 합치) |
| T9 | `ResultAssemblerTest.resultMatchesVerifiedSolution` | 모든 problem request가 routes+unassigned에 정확히 1회 · Visit 시각(toWallClock 역변환)·적재·경로 지표가 `Pass.routeFacts`와 1:1 · metrics == `Pass.evaluation` · run 메타(profileId·verified=true·stamp 값·**deliveryPolicy와 searchBudget이 각각 별도 필드**) | (Plan 범위 문장 "검증된 해 ↔ 결과 일치 테스트") |
| T10 | `ResultAssemblerTest.derivesUnassignedReasons` | 4사유 각 1건: 호환 0대 → NO_COMPATIBLE_VEHICLE / 전 호환 차량 용량 미달 → CAPACITY / 창 도달 불가 → TIME_WINDOW_INFEASIBLE (E12 포함) / 단독 가능 → NOT_PLACED | (Plan 범위 문장 "unassigned+reason") |
| T11 | `ResultAssemblerTest.deterministicOrderingAndStatus` | routes·unassigned 정렬, 같은 입력 → 동등 `SolveResult`, status 항상 DONE | (Plan 범위 문장 "결과 모델" — §11.1 결정성) |

T5~T11은 DoD 두 문장 밖이지만 Plan Stage 5 범위 문장("독립 재검증(전체 해, 캐시 없이),
결과 모델, 검증된 해 ↔ 결과 일치 테스트")의 직접 검증이다 — 보고에서 DoD 보강을 제안한다.

---

## 9. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| result.json 직렬화(Jackson **3** `JsonMapper`)·wire 필드명·시각 문자열 포맷·단위 표현(milli→kg) 확정 | Stage 6 (§11.2 협의) | Domain §11.2, Architecture §2.1·§4, Stage 0 §4.4 |
| verify 호출 결선(executor 흐름)·`Solution → Map` 분해 코드·`RunStamp` 값 채움·FAIL → status FAILED 기록·S3 저장 | Stage 6 | Architecture §3.2·§3.3 |
| 탐색 `bestRouteFacts`와 재계산 facts의 경로별 대조 | 안 함 — §10.2의 대조 대상은 점수뿐 (노트 N4) | Domain §10.2 |
| result-integrity 별도 실행 단계 (2단 verifier) | 안 함 — 테스트 T9로 보장 (폐기 결정) | Domain §10.2·Master §5 |
| run 메타에 `AlnsRunStats`(반복·수락 수) 포함 | 안 함 — §11.1 run 항목 그대로 (Stage 4 Q2 해소: stats는 로그·실험용으로만) | Domain §11.1·Master §3-⑪ |
| 미배정 사유 값 추가·세분 (예: 한도 전용 사유) | 안 함 — §11.1 예시 4개 유지 (§9 Q2) | Domain §11.1 |
| `verify`의 별도 모듈 승격 | 안 함 (필요해지면 그때) | Architecture §2.1 |
| fingerprint·provenance·lineage류 추적 | 안 함 (폐기 확정) | Domain §11.1·Master §3-⑪ |
| depot 창·`depot.taskTime`의 재검증 적용 | Stage 3과 동일 보류 (탐색·검증 동일 해석 유지) | Stage 3 §9 Q2 |
| 재검증 매 trial 실행·증분 재검증 | 안 함 — 저장 직전 1회 | Domain §10.2 |

---

## 10. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 5 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | 결과 모델(Domain §11)의 패키지 자리 — Architecture §2 트리는 `verify`를 "독립 재검증 (Domain §10)"으로만 주석하고 §11의 소유 패키지를 정하지 않았다 | `verify`에 배치 (§1 말미의 근거). Architecture §2 주석을 "재검증 + 결과 모델 (Domain §10–11)"로 보완 제안 |
| Q2 | 미배정 사유 목록이 "예:"로 열려 있고, maxDrive·maxStop 등 한도 때문에 단독 불가한 경우를 정확히 가리키는 값이 없다 | TIME_WINDOW_INFEASIBLE bucket에 포함 (§4.3 절차 3, E12). 새 값을 임의로 추가하지 않고, wire 협의(§11.2) 때 세분 여부를 결정 |
| Q3 | §11.1 status가 FAILED를 나열하지만 §10.2(FAIL 시 결과 미저장)·Architecture §3.3(result.json은 DONE일 때만)과 조합하면 FAILED 결과 JSON은 생산 경로가 없다 | enum 값은 유지하되 `assemble`은 항상 DONE (노트 N6). Domain §11.1 문구 정리 제안 |
| Q4 | PD의 결과 visits에서 같은 orderId 방문 2건(픽업/배송)의 구분 필드가 §11.1에 없다 | `Visit.pickup` boolean을 모델에 추가 (구분 불가면 PD 결과가 무의미 — §1.3의 canonical PD 지원과 정합). 현 규약(전건 DELIVERY_ONLY)에는 무영향, wire 노출은 Stage 6 협의 |
