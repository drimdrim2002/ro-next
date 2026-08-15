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
  - 2026-08-10 순서 개정 반영 — 재검증(§1~§3)을 Stage 4보다 먼저 만든다는 서두 한 문단 추가
    (Stage 4 의존은 §8 T8과 서두 그림뿐). §2.3·§7 E8은 시간창 close를 Domain §7.1 확정 해석으로,
    depot 창·복수 근무창은 Plan §2.1 D4 대기로 표기. 파일·클래스·시그니처·테스트 무변경
  - 2026-08-10 **D4 확정 반영 — 설계 변경**: `VerifyViolation.Kind`에 `DEPOT_WINDOW` 추가 ·
    §2.3 `RouteReplay`가 다일 근무창·차고 창을 재계산 · §3에 절차 5-a(창 포함 검사, Stage 3 N5 대체) ·
    §4.3 절차 3의 bucket 설명 확장 · N1 공유 허용 목록에 "정규화된 창 목록" + 창 걷는 코드는 공유 금지 ·
    E19~E22 신설 · T5에 다일 대조 추가, T12 신설 · §9 Q5(`departure` wire 노출) 신설
  - 2026-08-11 검토 반영 — §4.1 `RouteResult`에 경로 시각 2종(depotDeparture·depotReturn) 추가
    (Domain §11.1 개정 이행 — wire 노출은 Plan D2) · §4.2 절차 1·T9 동기 · §7 표의 중복 번호
    E16/E17을 E23/E24로 개번(T6 참조 갱신) · §9 Q3 해소(Domain §11.1 문구 정리 완료) ·
    §8 말미 문구를 Plan §1 DoD 편입으로 갱신
  - 2026-08-11 Stage 1 유예 반영 — §8 `depot.taskTime` 행(canonical에 없어 읽을 값이 없다).
    차고 창 재검증은 무변경
  - 2026-08-12 감사 후속 인터뷰 정합 — §4.3 미배정 사유 절차가 Domain §11.1 **판정 규칙
    정본**으로 승격됨(내용 무변경 — 순서·bucket 변경은 Domain 선개정) ·
    `AlnsResult.bestRouteFacts` 삭제(Stage 4) 반영 — 관련 4곳 문구 정리
  - 2026-08-13 감사 후속 인터뷰 정합 — §3에 둘째 관문(7-a) 추가: 5~7 위반 시 8·9 생략
    (위반 해의 부분 facts 대조가 만드는 가짜 SCORE_MISMATCH 차단 — 절차 4와 동형) ·
    존재하지 않는 `Master §3-⑪` 인용 2곳(§8 표)을 Domain §11.1로 교체
  - 2026-08-13 감사 결함 정정 (분할 7 F2·F5·F8, 통합 §2) — 서두 "ArchUnit으로 도달 불가능"
    거짓 인과 교정(값의 차단은 verify 시그니처 — §2.1) · `SearchBudget.termination` 삭제
    (종료 사유는 stage-04 `AlnsRunStats` 소유의 실행 통계고, Domain §11.1 run 항목은
    배송정책·탐색 예산뿐 — record 주석·§9 행과의 삼중 모순 해소) · §2.3 공유 목록의
    `RouteFacts` 오귀속 교정(profile 아닌 core `eval` — N1과 통일) · §2.3에 Domain §7.1
    위반 귀속 규약(두 창 축 동시 소진 시) 전파
  - 2026-08-14 Domain `startDepot` optional + `PICKUP_ONLY` — pair 검사·단독 경로에
    PICKUP_ONLY. `RouteResult.depotDeparture` Optional. replay는 Stage 3과 같은 출발/하차 규칙
  - 2026-08-15 재검증 절차 6 `at`을 Stage 3 Evaluator와 정합 (있는 방문 NodeId).
    `DEPOT_WINDOW` 설명을 Domain §7.1에 맞춤 (있는 출·도착만)
---

# Stage 5 — 재검증과 결과

solver-core의 `verify` 패키지에 탐색과 분리된 독립 재검증(`SolutionVerifier`)과
결과 모델(`SolveResult`)·미배정 사유 산출(`ResultAssembler`)을 만든다.
주 근거: [Domain §10–§11](../domain-design.md). Stage 3이 core `eval`에 배치한 공유 값 타입
(`VisitFacts`·`RouteFacts`·`Evaluation`·`Scores`)과 Stage 4의 `AlnsResult`(best +
bestEvaluation + bestScore)를 그대로 잇는다 — 같은 개념에 새 이름을 짓지 않는다.

**재검증은 탐색 예산을 보지 않는다** (Domain §2.5.1·§10.2 MUST NOT). 이를 지키는 장치는
두 겹이되 역할이 다르다 — `verify ↛ solve` ArchUnit 규칙은 `AlnsConfig` 등 **타입 참조**를
차단할 뿐이고, 예산 **값**이 검사 로직에 들어오는 것의 차단은 `SolutionVerifier.verify`
시그니처에 예산 인자가 없다는 것(§2.1)이 담당한다. 예산 값 자체는 결과 기록용
`SearchBudget`(§4.1)으로 verify 패키지에 존재하므로, ArchUnit green이 곧 "예산 무관 검증"의
증거는 아니다.

**순서 — 재검증(§1~§3)은 Stage 4보다 먼저 만든다** (Plan Stage 5, 2026-08-10). `SolutionVerifier.verify`의
시그니처에 Stage 4 타입이 하나도 없어 Stage 3만 있으면 만들 수 있다 (§2.1). **Stage 4에 실제로
의존하는 것은 §8 T8(`AlnsVerifyIntegrationTest.alnsBestAlwaysVerifies`) 하나뿐**이고, 아래 핵심
구도의 `[AlnsResult (Stage 4)]` 입구는 **전방 참조**다 — 그 둘만 Stage 4 뒤로 미룬다.
결과 모델(§4)은 Stage 4 뒤에 만들지만 이는 **의존이 아니라 집중을 위한 선택**이다
(`ResultAssembler`도 `Pass` + `RunStamp`만 쓰므로 기술적으로는 Stage 4 없이 만들 수 있다).
문서는 쪼개지 않는다 — 읽는 순서만 바뀐다.

**DoD** ([Plan Stage 5](../implementation-plan.md)): 일부러 오염시킨 해(짝 분리·용량 초과·
점수 불일치)가 전부 FAIL · ArchUnit 규칙 통과. **두 문장 모두 재검증(§1~§3) 쪽이라 위 순서
변경으로 DoD는 달라지지 않는다.** §6의 `SOLVE_MUST_NOT_DEPEND_ON_VERIFY` 규칙이 Stage 4보다
먼저 심어지는 것은 덤이다 — Stage 4가 verify 코드를 끌어 쓰는 것을 작성 시점에 막는다.

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
  best 승격 시점의 정식 평가값). 탐색 쪽 경로 facts는 verify가 받지 않는다 — 대조 대상은
  이 두 값뿐이다 (§10.2, §8. 이를 운반하던 `bestRouteFacts` 필드 자체가 소비자 없음으로
  2026-08-12 삭제됐다 — Stage 4).
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
        DEPOT_WINDOW,                     // 차고 창 밖 있는 출·도착 (Domain §7.1, D4 신설)
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
    /** Domain §7.1 절차를 처음부터: initialLoad → 출발(start 있으면 근무창 ∩ startDepot 창,
        waitInDepot; 없으면 앞 arc 없음·첫 방문 arrival = spanStart) →
        방문 루프(arrival→대기→시간창→reqDate→load 곡선, 창을 넘으면 다음 창으로 미룸) →
        endDepot 도착의 차고 창 + PICKUP_ONLY만 −수요 → 휴식 계산 → 한도 → 종료 load == 0.
        이동은 problem.travel()만 (§4 MUST). solve.RoutePropagator 코드를 재사용하지 않는다.
        창을 걷는 코드(fitArc/fitService 상당)도 **여기서 따로 구현한다** (Stage 3 노트 N8). */
    static Outcome replay(Problem problem, VehicleId vehicleId, List<NodeId> visits);
}
```

- `RouteReplay`는 solve의 `RoutePropagator`와 **같은 의미를 독립 코드로** 다시 구현한다
  (Domain §10.1 — 탐색 코드 버그를 잡는 이중 기입). 공유하는 것은 `Problem`의 동결 사실 ·
  인자로 받은 같은 `Profile` 인스턴스 · core `eval`의 값 타입과 공식
  (`RouteFacts.routeOperationalTimeSec()` 등 — Stage 3 N1이 이 목적으로 배치)뿐이다 (노트 N1).
- 전파 해석은 Stage 3과 **동일하게** 따른다 — 갈리면 두 구현의 값이 어긋나 T5가 실패한다.
  - 시간창 close = `serviceStart ≤ closeTime`: **Domain §7.1 확정 해석** (2026-08-10, Stage 3 §9 Q1 해소).
    창이 여럿이면 "`serviceStart`가 **어느 창 안**인가"로 판정한다 — 마지막 창의 close 하나만 보면
    창 사이 틈에서 시작하는 서비스를 통과시킨다 (Domain §7.1 MUST).
  - **다일 근무창·차고 창: Domain §3.2·§7.1 확정 해석** (2026-08-10 D4, Stage 3 §9 Q2·Q3 해소).
    창 목록은 정규화가 전개해 `Problem`에 동결된 **사실**이라 그대로 읽고, 그 목록을 해석해
    시각을 정하는 **코드는 공유하지 않는다** (Stage 3 N8·아래 노트 N1).
  - **위반 귀속 (Domain §7.1, 2026-08-11)**: 한 지점에서 두 창 축이 **같은 시각에 함께
    소진**되면 절차 문장에 먼저 적힌 쪽을 기록한다 — 절차 0(출발)은 `WORK_WINDOW`,
    절차 2(서비스)는 `TIME_WINDOW`. 가능/불가 판정은 달라지지 않지만, 탐색과 재검증이
    FAILED 원인 종류까지 같게 내기 위한 규약이다 (Stage 3 §3.3 절차 1·2와 같은 문장).
  - 시간 측정 규약(모든 소요 시간 = 두 시각의 차, Domain §3.2)을 여기서도 그대로 쓴다 —
    한쪽만 "초를 세면" 창마다 1초씩 어긋나 T5가 원인 불명으로 실패한다.

---

## 3. 재검증 절차 (`SolutionVerifier.verify`)

번호 순서대로. 위반은 던지지 않고 수집한다 (Stage 6이 FAILED 원인으로 기록). 결정성을 위해
경로는 `VehicleId` 문자열 순, bank는 `RequestId` 문자열 순으로 순회한다.

```text
1. 참조    각 VehicleId가 Problem에 존재 (UNKNOWN_VEHICLE). visits 빈 목록 (EMPTY_ROUTE).
          각 NodeId를 problem.nodeRef로 역참조 — 실패는 UNKNOWN_NODE
          (DELIVERY_ONLY의 가짜 픽업·PICKUP_ONLY의 가짜 하차 NodeId는 색인에 없어 여기서
          자동 검출, §1.3 MUST NOT).
          해 전체 NodeId 중복 (DUPLICATE_NODE). bank의 RequestId 존재 (UNKNOWN_REQUEST).
2. pair   RequestId별 등장 방문 수집 (§1.4):
          DELIVERY_ONLY — delivery 방문 1개.
          PICKUP_ONLY — pickup 방문 1개.
          PICKUP_DELIVERY — 두 방문이 다른 경로 → PAIR_SPLIT. 한쪽만 → PAIR_INCOMPLETE.
          같은 경로에서 pickup index > delivery index → PICKUP_AFTER_DELIVERY.
3. XOR    소유된 RequestId가 bank에도 있음 → ASSIGNED_AND_BANKED.
          problem.requests() 전체 중 소유도 bank도 아님 → NOT_ASSIGNED_NOT_BANKED (§6.3).
4. 관문    1–3 위반이 하나라도 있으면 여기서 Fail — 구조가 깨진 경로의 물리 재계산은
          무의미하다 (Domain §6.5의 순서: 구조 검사 → 정식 평가와 동형).
5. 재전파  경로마다 RouteReplay.replay (§2.3) — 적재 곡선(0 ≤ load ≤ capacity, 종료 0),
          시간창·reqDate·근무창·차고 창, maxStop·maxDrive 한도까지 전부 다시 계산.
          위반 경로는 기록하고 나머지 경로도 계속 검사한다 (원인을 최대한 모아 보고).
          "이동표 사용"(§10.2)은 검사 항목이 아니라 구조다 — replay가 problem.travel()
          밖의 어떤 이동값도 얻을 수 없다 (표는 모든 쌍 완비, Stage 2 §4 절차 5).
5-a. 창 포함  재계산이 끝난 뒤 그 사실을 한 번 더 훑어 **창 안에 있는지 확인**한다
          (Domain §10.2 — Stage 3 노트 N5를 대체하는 검사):
          · 모든 이동 [departure, 다음 arrival]이 한 근무창 안 → 아니면 WORK_WINDOW
          · 모든 서비스 [serviceStart, serviceEnd]가 한 근무창 안 → 아니면 WORK_WINDOW
          · startDepot 있으면 차고 출발 순간이 그 창 안, endDepot 있으면 도착 순간이 그 창 안
            → 아니면 DEPOT_WINDOW. 없는 쪽은 검사하지 않는다
          replay가 옳게 배치했다면 산술적으로 통과하는 검사다 — END_LOAD_NOT_ZERO와 같은
          성격의 자기 방어이며, 창 걷는 코드를 따로 구현했기 때문에(N8) 값이 아니라
          **배치 자체**를 확인할 자리가 필요하다.
6. 호환    경로가 소유한 각 RequestId에 대해 vehicleId ∈ problem.compatibleVehicles(id)
          (§3.4 동결 사실). 위반 → INCOMPATIBLE_VEHICLE, at = 그 request의 있는 방문 NodeId
          (DELIVERY_ONLY → delivery, PICKUP_ONLY → pickup, PICKUP_DELIVERY → delivery —
          Stage 3 §4.2와 같음) (노트 N5).
7. profile hard  profile.hardConstraints()의 각 h를 h.satisfied(problem, 재계산 RouteFacts)로 적용.
          위반 → PROFILE_HARD (detail에 h.id()). 인자로 받은 profile을 쓸 뿐 verify가
          따로 고르지 않는다 — 탐색과 같은 인스턴스인지는 호출자가 보장한다 (§8.4 MUST).
7-a. 관문  5~7 위반이 하나라도 있으면 여기서 Fail — 8·9를 실행하지 않는다 (2026-08-13 확정).
          위반 해의 부분 facts로 하는 점수 대조는 가짜 SCORE_MISMATCH만 만들어 진짜 원인을
          가린다. 점수 대조(8·9)는 "정상 해에서 캐시 = 재계산"의 검증이다 — 절차 4와 동형의
          관문이고, FAILED 원인 보고에는 5~7이 모은 실제 위반만 남는다.
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

    /** AlnsConfig의 예산 부분만 결과용으로 옮긴 값 (Domain §2.5.1의 시간·step·idle·seed).
        알고리즘 튜닝 파라미터를 담지 않고, 종료 사유(termination) 등 실행 통계도 담지 않는다
        — 그건 `AlnsRunStats`(Stage 4 §3.2)의 몫이고 run 메타에 실리지 않는다 (Domain §11.1,
        §9. 종전 `String termination` 필드는 2026-08-13 삭제 — stats 배제 원칙과의 모순). */
    public record SearchBudget(long timeLimitSec, OptionalLong maxSteps,
                               OptionalLong idleSteps, OptionalLong idleSec,
                               long seed) {}

    public record RouteResult(VehicleId vehicleId, List<Visit> visits,   // visits = 방문 순서
                              Optional<LocalDateTime> depotDeparture,    // 차고 출발 (§7.1 절차 0 = RouteFacts.departureSec).
                                                                         // 부재 = startDepot 없는 경로
                              Optional<LocalDateTime> depotReturn,       // 도착 차고 도착 (§7.1 절차 8 = endDepotArrivalSec).
                                                                         // 부재 = endDepot 없는 경로
                              long driveDistMeter, long driveTimeSec,
                              int stopCount, long routeOperationalTimeSec) {}
                              // 경로 시각 2종은 Domain §11.1의 2026-08-11 추가분 — wire 필드명·노출 형식은 D2 협의

    public record Visit(RequestId orderId,               // 식별자는 규약과 같은 이름 (§11.1)
                        boolean pickup,                  // PD의 같은 orderId 방문 2건 구분 (§9 Q4)
                        LocationId locationId,
                        LocalDateTime arrival, LocalDateTime serviceStart, LocalDateTime serviceEnd,
                        long loadWeightMilliKg, long loadVolumeMilliCbm) {}  // 방문 처리 후 적재
                        // `departure` 노출 여부는 wire 협의(Plan D2) — §9 Q5

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
            재계산 RouteFacts에서: 경로 지표 4개(§11.1) + 경로 시각 2종(depotDeparture·
            depotReturn — toWallClock, 2026-08-11) + 방문별 Visit
            (VisitFacts의 requestId·pickup·locationId·시각 3종 toWallClock·처리 후 적재).
2. unassigned  pass.bank()를 RequestId 문자열 순으로, 사유는 §4.3 절차로 산출.
3. metrics   = pass.evaluation() (재계산 값 — 탐색 보고값이 아니라).
4. run       = Run(stamp의 4개 값, profileId = profile.id(), verified = true,
                   deliveryPolicy = problem.deliveryPolicy(), searchBudget = stamp의 예산 요약).
            배송정책과 탐색 예산을 **따로** 기록한다 (Domain §11.1 — 같은 문제를 다른
            예산으로 돌린 결과를 구별할 수 있어야 한다).
5. 반환      SolveResult(problem.planId(), Status.DONE, run, routes, unassigned, metrics).
```

탐색 쪽 경로 facts는 조립에 쓰지 않는다 (운반 필드 `bestRouteFacts`는 2026-08-12 삭제 —
Stage 4) — 결과의 모든 수치는 verify의 재계산본에서만 나온다 (노트 N4). "검증된 해 ↔ 결과 JSON 일치"는 별도 실행 단계가 아니라 테스트 T9가
보장한다 (Domain §10.2 — 2단 verifier 폐기 결정의 이행).

### 4.3 미배정 사유 산출 절차

bank는 ID만 갖고 있으므로(§6.3 MUST NOT) 사유는 여기서 계산한다 (§11.1). 축을 차례로
좁히는 결정적 절차다 — 혼합 원인의 모호함이 생기지 않는다 (§5.3 노트).
이 절차는 2026-08-12 Domain §11.1에 **판정 규칙 정본**으로 승격됐다 — 순서·bucket 정의를
바꾸려면 Domain을 먼저 개정한다.

```text
각 r ∈ pass.bank()에 대해:
1. V₀ = problem.compatibleVehicles(r)              (§3.4 동결 사실)
   V₀ = ∅ → NO_COMPATIBLE_VEHICLE.
2. V₁ = { v ∈ V₀ : r.totalWeightMilliKg ≤ v.maxWeightMilliKg
                 ∧ r.totalVolumeMilliCbm ≤ v.maxVolumeMilliCbm }
   V₁ = ∅ → CAPACITY                               (호환 차량 전부가 혼자서도 실을 수 없음)
3. V₂ = { v ∈ V₁ : RouteReplay.replay(problem, v, r만 있는 단독 경로) = Ok }
   단독 경로: DELIVERY_ONLY → [delivery NodeId], PICKUP_ONLY → [pickup NodeId],
              PICKUP_DELIVERY → [pickup, delivery].
   V₂ = ∅ → TIME_WINDOW_INFEASIBLE                 (혼자 넣어도 시간창·reqDate·근무창·
                                                    차고 창·maxDrive 등에서 불가 —
                                                    DEPOT_WINDOW·WORK_WINDOW로 불가한 경우도
                                                    이 bucket이다, §9 Q2 라벨 폭 주의)
4. V₂ ≠ ∅ → NOT_PLACED                             (단독으로는 가능 — 탐색이 다른 배정과
                                                    함께 자리를 못 찾았을 뿐)
```

비용: |bank| × |호환 차량| 회의 단독 경로 replay — fixture 규모(452×31 상한)에서 무시 가능.

---

## 5. 설계 노트

| # | 내용 |
|---|---|
| N1 | **의도적 중복**: `RouteReplay`·구조 검사·위반 enum은 solve의 `RoutePropagator`·`StructureCheck`·`Violation`과 같은 의미의 **별도 코드**다. 코드를 공유하면 같은 버그가 양쪽에 숨어 재검증이 무의미해진다 (Domain §10.1). 공유가 허용되는 것은 `Problem`의 동결 사실(이동표·호환성·**정규화된 시간창 목록** — 근무창·차고 창·방문 시간창, Domain §3.2)·인자로 받은 같은 `Profile` 인스턴스(§10.2가 명시한 같은 입력)·core `eval`의 값 타입과 공식(`RouteFacts`·`Evaluation.aggregate`·`Scores.compare` — Stage 3 N1이 이 목적으로 배치)뿐이다. `profile.score`도 공유 대상이다 — 재검증의 목적은 "탐색이 그 profile의 목적식을 정직하게 계산했는가"이지 목적식 자체를 다시 정의하는 것이 아니다. **반대로, 그 창 목록을 걷는 코드(`fitArc`/`fitService` 상당)는 공유하지 않는다** — 목록은 사실이고 걷는 규칙은 해석이라, 해석을 공유하면 이중 기입이 무너진다 (Stage 3 노트 N8에 같은 문장) |
| N2 | 위반 enum을 solve와 별도로 두는 것은 N1의 결과다 — `solve.Violation`을 쓰면 `verify → solve` 참조가 생겨 ArchUnit이 막는다. 이름은 의미가 같은 항목끼리 동일하게 맞춘다 (리뷰 용이) |
| N3 | **중복 차량 경로는 verify에 도달하지 못한다**: 진입 타입이 `Map<VehicleId, …>`라 (Stage 3 N6 확정) 한 차량 두 경로는 표현 불가다. 호출자의 `toMap` 분해가 중복 키에서 던지는 예외가 곧 구조 결함 신호이며, Stage 6 executor는 이를 FAILED로 기록한다. ALNS 정상 경로에서는 Stage 4 N2(구조 위반 = 예외)가 이미 걸러 준다 |
| N4 | **결과는 재계산본으로만 조립한다**: 탐색 쪽 경로 facts는 검증의 대조에도 결과 조립에도 쓰지 않는다 (이를 운반하던 `AlnsResult.bestRouteFacts`는 소비자가 없어 2026-08-12 삭제). "탐색의 증분 캐시·내부 상태를 믿지 않는다"(§10.2)의 연장 — 결과 수치의 원천이 검증 코드 하나로 좁혀져, 검증된 해 ↔ 결과 일치가 구조적으로 따라온다 |
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
| E5 | DELIVERY_ONLY의 pickup NodeId·PICKUP_ONLY의 delivery NodeId가 경로에 | UNKNOWN_NODE (nodeRef 색인에 없음 — 구조적 강제) | §1.3 MUST NOT |
| E6 | 구조 위반 존재 | 재전파·점수 대조 생략, Fail에는 구조 위반만 (관문 §3-4) | §6.5 순서 |
| E7 | hard 위반 경로 여럿 | 경로별로 전부 수집해 보고 (첫 경로에서 멈추지 않음) | §12 원인 기록 |
| E8 | serviceStart == **그 창의** close / == reqDate | 통과 — 양끝 포함. **Domain §7.1 확정 해석**(`serviceStart`가 어느 창 안인가 — 마지막 창의 close 하나만 보지 않는다)을 solve와 verify가 같이 따른다 (§2.3) | Domain §7.1·§2.3 |
| E9 | waitInDepot=Y 해 | replay도 같은 출발 규칙 (Stage 3 §3.3 절차 1) — 다르면 시각 전체가 어긋나 T5에서 검출 | §2.5 |
| E10 | 종료 load ≠ 0 | pair 완비 구조에서는 산술적으로 불가 — 검사는 replay 자체 버그·모델 변화 방어 (END_LOAD_NOT_ZERO) | §6.2·Stage 3 N3 |
| E11 | bank에 호환 0대 Request | 검증엔 무영향, 사유 NO_COMPATIBLE_VEHICLE | §3.4·§11.1 |
| E12 | 사유 산출에서 시간은 되는데 maxDriveDist만 불가 | TIME_WINDOW_INFEASIBLE (단독 불가 bucket — 라벨 폭은 §9 Q2) | §4.3 절차 3 |
| E13 | 단독으로는 가능한데 bank에 남음 | NOT_PLACED (탐색 품질이지 결함 아님) | §9.1·§11.1 |
| E14 | 비호환 차량에 배정된 채 verify 도달 | INCOMPATIBLE_VEHICLE → FAIL (노트 N5) | §3.4 |
| E15 | 항상-false profile hard | PROFILE_HARD → FAIL (탐색·검증이 같은 profile이므로 정상 경로에선 탐색도 못 만든 해) | §8.4 |
| E16 | 호출자가 탐색과 **다른** profile 인스턴스를 넘김 | verify는 알아챌 수 없다 — 인자를 믿는다. 이 결선은 Stage 6의 단일 지점이 보장하고 그 테스트가 지킨다 | §8.4 MUST |
| E17 | `reportedScore`와 재계산 score의 길이가 다름 | SCORE_MISMATCH (같은 profile이면 길이가 같아야 한다 — Stage 3 E31) | §8.3 |
| E23 | routes 맵 값이 빈 목록 | EMPTY_ROUTE → FAIL (`Route`는 빈 visits를 못 만들지만 verify 입력은 raw 맵 — 방어) — 2026-08-11 개번, 종전 번호 E16이 중복이었다 | Stage 3 E30 |
| E24 | PD 결과의 같은 orderId 방문 2건 | Visit.pickup으로 구분 (§9 Q4) — 2026-08-11 개번, 종전 번호 E17이 중복이었다 | §1.3 |
| E18 | 시간 한도로 중단된 탐색의 best | 정상 입력 — verify는 출처를 모른다. hard만 재확인 | §12 "탐색 중단 = 정상" |
| E19 | 다일 해 (근무창 여럿) | replay도 같은 미루기 규칙으로 재계산 — `interWorkWindowRestTimeSec`·방문별 `departureSec`가 solve와 일치해야 한다 (T5에서 대조). 두 구현이 창을 다르게 걸으면 여기서 드러난다 | Domain §3.2·§7.3 |
| E20 | 창 밖에 배치된 이동·서비스가 섞인 오염 해 | 절차 5-a의 포함 검사에서 WORK_WINDOW → FAIL | Domain §10.2 |
| E21 | endDepot 도착이 차고 창 밖인 해 | DEPOT_WINDOW → FAIL (미루지 않는다 — Stage 3 E36과 같은 해석) | Domain §7.1 |
| E22 | 현행 fixture 모양(창 1개·차고 전일창·endDepot 없음) | `interWorkWindowRestTimeSec == 0`, `departureSec == serviceEndSec`, DEPOT_WINDOW 미발화 — D4 전과 같은 값 | Stage 3 E40 |

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
| T5 | `SolutionVerifierTest.passesValidSolutionAndAgreesWithSolve` | Stage 3 §3.4 예제를 pair 완비로 확장한 유효 해 → `Pass`. 재계산 `RouteFacts`·`Evaluation`이 solve의 `Evaluator.evaluate(problem, profile, solution)` 결과와 record 동등이고, score는 `Arrays.equals` — **두 독립 구현의 합치** (E8·E9 경계 포함). **다일 케이스도 같은 방식으로 대조한다** (Stage 3 §3.5 예제 — `interWorkWindowRestTimeSec`·방문별 `departureSec`까지, E19). `Feasible`/`Pass` 자체를 `equals`로 비교하지 않는다 (배열 필드 — Stage 3 §4.3 공통 규칙) | (오염 FAIL의 대조군 — 전부 FAIL이 "verify가 무조건 FAIL"이 아님의 증명) |
| T6 | `SolutionVerifierTest.failsOnXorViolations` | E1(위반 0)·ASSIGNED_AND_BANKED·NOT_ASSIGNED_NOT_BANKED·UNKNOWN_REQUEST·E23 | (Plan 범위 문장 "독립 재검증(전체 해)" — §10.2 검사 항목) |
| T7 | `SolutionVerifierTest.failsOnIncompatibleAndProfileHard` | E14·E15 각각 정확한 Kind (같은 profile 인스턴스 사용 확인 포함) | 〃 |
| T8 | `AlnsVerifyIntegrationTest.alnsBestAlwaysVerifies` | 소형 fixture × seed 여러 개로 `AlnsSolver.solve(problem, profile)` → `AlnsResult` 분해 → 같은 profile로 verify `Pass` + `Pass.evaluation` == `bestEvaluation` + `Arrays.equals(Pass.score, bestScore)` (Stage 4 T4의 verify판 — 파이프라인 전 구간 정합) | (§10 전체 흐름의 실증 — 캐시 없는 재계산과 탐색 보고값의 합치) |
| T9 | `ResultAssemblerTest.resultMatchesVerifiedSolution` | 모든 problem request가 routes+unassigned에 정확히 1회 · Visit 시각(toWallClock 역변환)·적재·경로 지표·경로 시각 2종(depotDeparture·depotReturn)이 `Pass.routeFacts`와 1:1 · metrics == `Pass.evaluation` · run 메타(profileId·verified=true·stamp 값·**deliveryPolicy와 searchBudget이 각각 별도 필드**) | (Plan 범위 문장 "검증된 해 ↔ 결과 일치 테스트") |
| T10 | `ResultAssemblerTest.derivesUnassignedReasons` | 4사유 각 1건: 호환 0대 → NO_COMPATIBLE_VEHICLE / 전 호환 차량 용량 미달 → CAPACITY / 창 도달 불가 → TIME_WINDOW_INFEASIBLE (E12 포함) / 단독 가능 → NOT_PLACED | (Plan 범위 문장 "unassigned+reason") |
| T11 | `ResultAssemblerTest.deterministicOrderingAndStatus` | routes·unassigned 정렬, 같은 입력 → 동등 `SolveResult`, status 항상 DONE | (Plan 범위 문장 "결과 모델" — §11.1 결정성) |
| T12 | `SolutionVerifierTest.failsOnWorkAndDepotWindow` | **D4 확정분.** 유효한 다일 해에서 ① 한 이동이 근무창 틈에 걸치도록 오염 → WORK_WINDOW (E20, 절차 5-a) ② endDepot 도착이 차고 창 밖이 되도록 오염 → DEPOT_WINDOW (E21) ③ 오염 없는 대조군은 `Pass` | (Plan 범위 문장 "독립 재검증(전체 해)" — Domain §10.2 검사 항목) |

T5~T12는 Plan DoD 요약 문장 밖이지만 Plan Stage 5 범위 문장("독립 재검증(전체 해, 캐시 없이),
결과 모델, 검증된 해 ↔ 결과 일치 테스트")의 직접 검증이다 — Plan §1의 편입(2026-08-11)에 따라
이 표 전부가 완료 기준이다.

---

## 9. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| result.json 직렬화(Jackson **3** `JsonMapper`)·wire 필드명·시각 문자열 포맷·단위 표현(milli→kg) 확정 | Stage 6 (§11.2 협의) | Domain §11.2, Architecture §2.1·§4, Stage 0 §4.4 |
| verify 호출 결선(executor 흐름)·`Solution → Map` 분해 코드·`RunStamp` 값 채움·FAIL → status FAILED 기록·S3 저장 | Stage 6 | Architecture §3.2·§3.3 |
| 탐색 쪽 경로 facts와 재계산 facts의 경로별 대조 | 안 함 — §10.2의 대조 대상은 점수뿐 (노트 N4. 운반 필드 `bestRouteFacts` 자체가 2026-08-12 삭제됨) | Domain §10.2 |
| result-integrity 별도 실행 단계 (2단 verifier) | 안 함 — 테스트 T9로 보장 (폐기 결정) | Domain §10.2·Master §5 |
| run 메타에 `AlnsRunStats`(반복·수락 수) 포함 | 안 함 — §11.1 run 항목 그대로 (Stage 4 Q2 해소: stats는 로그·실험용으로만) | Domain §11.1 |
| 미배정 사유 값 추가·세분 (예: 한도 전용 사유) | 안 함 — §11.1 예시 4개 유지 (§9 Q2) | Domain §11.1 |
| `verify`의 별도 모듈 승격 | 안 함 (필요해지면 그때) | Architecture §2.1 |
| fingerprint·provenance·lineage류 추적 | 안 함 (폐기 확정) | Domain §11.1 |
| `depot.taskTime`의 재검증 적용 | **canonical에 없어 읽을 값이 없다** (2026-08-11 — 복귀 선적 시간이라 1바퀴엔 미발생, Domain §2.5). 차고 **창**은 적용한다 (D4 확정, §2.3·§3 절차 5-a·`DEPOT_WINDOW`) | Stage Extra E1 |
| 재검증 매 trial 실행·증분 재검증 | 안 함 — 저장 직전 1회 | Domain §10.2 |

---

## 10. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 5 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | 결과 모델(Domain §11)의 패키지 자리 — Architecture §2 트리는 `verify`를 "독립 재검증 (Domain §10)"으로만 주석하고 §11의 소유 패키지를 정하지 않았다 | `verify`에 배치 (§1 말미의 근거). Architecture §2 주석을 "재검증 + 결과 모델 (Domain §10–11)"로 보완 제안 |
| Q2 | 미배정 사유 목록이 "예:"로 열려 있고, maxDrive·maxStop 등 한도 때문에 단독 불가한 경우를 정확히 가리키는 값이 없다. **D4로 `DEPOT_WINDOW`·다일 `WORK_WINDOW`가 추가돼 이 bucket이 더 넓어졌다** | TIME_WINDOW_INFEASIBLE bucket에 포함 (§4.3 절차 3, E12). 새 값을 임의로 추가하지 않고, wire 협의([Plan §2.1 D2](../implementation-plan.md)) 때 세분 여부를 결정 |
| Q3 | §11.1 status가 FAILED를 나열하지만 §10.2(FAIL 시 결과 미저장)·Architecture §3.3(result.json은 DONE일 때만)과 조합하면 FAILED 결과 JSON은 생산 경로가 없다 | **해소 (2026-08-11).** Domain §11.1이 문구를 정리했다 — FAILED는 열거 호환용이고 result.json은 언제나 DONE, 실패는 status.json에만 남는다. enum 값 유지, `assemble`은 항상 DONE (노트 N6 그대로) |
| Q4 | PD의 결과 visits에서 같은 orderId 방문 2건(픽업/배송)의 구분 필드가 §11.1에 없다 | `Visit.pickup` boolean을 모델에 추가 (구분 불가면 PD 결과가 무의미 — §1.3의 canonical PD 지원과 정합). 현 규약(전건 DELIVERY_ONLY)에는 무영향, wire 노출은 Stage 6 협의 |
| Q5 | **`departure`를 결과 `Visit`에 노출할지** (2026-08-10 D4로 생긴 항목) — 다일 해에서는 방문의 출발 시각이 `serviceEnd`와 달라진다(밤을 새우고 다음 창에 출발). 결과에 없으면 야간 휴식이 결과에서 사라져 호출 측이 경로 시각을 재구성할 수 없다 | 내부 사실 `VisitFacts.departureSec`는 **지금 만든다** (Stage 3 §3.2). 결과 wire 노출은 **[Plan §2.1 D2](../implementation-plan.md)(wire 협의)**로 넘긴다 — 1일 입력에서는 `serviceEnd`와 같아 당장 정보 손실이 없다 |
