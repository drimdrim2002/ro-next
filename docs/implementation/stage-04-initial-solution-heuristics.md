---
title: Stage 4 — 초기해 휴리스틱 포트폴리오 (상세 구현 설계)
stage: 4
date: 2026-09-02
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§4 이동표 MUST NOT, §8 평가·비교, §8.4 profile SPI, §9.1·§9.3 ALNS)
  - ../master-design.md (§4 범위, §6 향후 옵션 — 분산 병렬 탐색)
  - ../architecture-design.md (§2 모듈·경계, §3.2 executor 스레드풀)
  - stage-02-travel-and-problem-freeze.md (§2.3 Problem.compatibleVehicles)
  - stage-03-solution-propagation-evaluation.md (§2 Solution·StructureCheck, §3 RoutePropagator, §4 Evaluator)
  - stage-04-alns.md (§3.4 초기해 진입점, §4.3 삽입 후보 탐색, §5 N5·N7·N8)
revisions:
  - 2026-09-02 최초 작성 — stage-04 §4.1의 "결정적 greedy 1개"를 **결정적 construction 8개
    포트폴리오**로 대체. 인터뷰 확정 사항(D1~D8)을 계약으로 고정
  - 2026-09-02 검토 정정 2차 — X8을 T18에 맞춤(`outcomes.elapsedMillis`는 관측값이라
    `InitialSolutionResult`를 `equals`로 비교하지 않는다) · §5 복잡도 표에 `PICKUP_DELIVERY`
    배수 명시(`(i ≤ j)` 쌍 나열로 위치가 `O(L²)` → H1~H6에 `L`이 한 번 더 곱해진다) ·
    T25에 "총 소요 / `timeLimitSec` 비" 추가(절대 초는 해석 불가 — 8 → 4 판단의 기준값)
  - 2026-09-02 자체 검토 정정 — `zoneId` 부재를 H3·H6의 기권 사유에서 **삭제**.
    존이 없으면 단일 그룹으로 퇴화할 뿐 정책(차량 외곽 루프 / FFD 분할)은 그대로 살아 있어
    H4와 다른 해를 낸다. 그 결과 기본 8개로는 전원 기권이 불가능해져 §6-3·X3·T16의
    도달 경로를 오버로드로 명시 (§4.4·§5 H6·§7 X3·§8 T16)
  - 2026-09-02 **4-초기해 / 4-ALNS 분리 구현** 반영 (Plan §2.2) — 서두에 이 단계가
    ALNS 타입을 하나도 참조하지 않아 단독으로 완결·green이 된다는 점을 명시하고,
    §9에 ALNS 위임 행 추가. 설계 무변경
---

# Stage 4 — 초기해 휴리스틱 포트폴리오

`solver-core`의 `solve` 패키지에 **난수를 쓰지 않는 rule 기반 construction 휴리스틱 8개**와,
그 8개를 실행해 **정식 평가로 최선 하나를 고르는 포트폴리오 실행기**를 만든다.
[stage-04](stage-04-alns.md)가 ALNS 본체를 소유하고, 이 문서는 그
**진입점(초기해)만**을 소유한다 — 두 문서가 충돌하면 배차 규칙은 언제나
[Domain](../domain-design.md)이 이긴다.

**이 문서는 `4-초기해`를 소유하고, 단독으로 완결된다 (2026-09-02).** Stage 4는 따로 만들고
따로 끝내는 두 단계이고(Plan §2.2), 이 단계가 먼저다. 여기서 만드는 어떤 타입도
`AlnsSolver`·`AlnsConfig`·`AlnsResult`·`DestroyOperator`·`RepairOperator`를 **참조하지 않는다** —
그래서 ALNS가 한 줄도 없는 상태에서 §8의 T13–T25가 전부 green이 될 수 있고, 그것이 이 단계의
완료 판정이다. 의존은 한 방향(ALNS → 초기해)뿐이고, 그쪽마저
[stage-04 §3.2](stage-04-alns.md)의 오버로드로 끊을 수 있다.

```text
[Problem (동결)] + [Profile]
        │
        ▼
  H1 … H8  (결정적 construction 8개 — 기권한 기법은 건너뜀)
        │  각 결과: StructureCheck 통과 + Evaluator Feasible  ← 아니면 버그(예외)
        ▼
  Scores.compare 최소 → best 1개                      ← 선택 권위 = 정식 평가뿐
        │
        ▼
  [initial Solution] → stage-04 §4.2의 ALNS 루프
```

**왜 8개인가.** 참고 자료([cvrptw_heuristic_strategy_summary](../orgin/cvrptw_heuristic_strategy_summary.md) §3)의
결론과 같다 — CVRPTW/PDPTW는 인스턴스 성격(클러스터링 정도·시간창 타이트함·용량 지배 여부)에 따라
좋은 construction 방식이 달라져, 하나를 아무리 정교하게 만들어도 다른 유형에 약하다.
다만 그 자료가 권하는 **난수·multi-start·racing은 채택하지 않는다** (§1 고정 항목).
여기서 얻는 것은 분산 감소가 아니라 **instance 적응성** 하나뿐이다.

---

## 0. 이 문서가 stage-04에서 이어받는 것 / 바꾸는 것

| stage-04의 종전 규정 | 이 문서의 처리 |
|---|---|
| §3.4 `InitialSolutionBuilder.build(Problem)` — 결정적 greedy 1개 | **개정** — `build(Problem, Profile)`, 8개 포트폴리오 (§3.1) |
| §4.1 "초기해는 1개다" | **개정** — 후보 8개, 결과 1개 (§5) |
| §4.3 후보 검증 = `RoutePropagator`만 | **개정** — profile hard 포함 (§4.1). Infeasible 초기해가 원천 불가능해진다 |
| §4.2-2 초기해 Infeasible → 빈 해 강등 (E16·N8) | **폐기** — Infeasible이면 버그(예외). E16b만 존치 (§7 X6) |
| §4.3 후보 나열·위치 규칙·비용 사전식 | **그대로 이어받는다** (§4.1) |
| §5 N3 결정성·N7 경로 전파의 성격 | **그대로 이어받는다** |
| §9 Q1 (시간 한도의 적용 범위) | **해소** — `timeLimitSec`은 ALNS 루프에만 (§4.3) |

같은 개념에 새 이름을 붙이지 않는다 — `InitialSolutionBuilder`라는 이름은 유지하고
그 **내부가** 포트폴리오가 된다 ([README](README.md) 이름 규칙).

---

## 1. 문서가 고정하는 것 / 구현·실험 재량

| 구분 | 내용 | 근거 |
|---|---|---|
| **고정** | 8개 전부 **결정적**이다 — `RandomGenerator`를 받지도 쓰지도 않는다. 같은 `Problem`·`Profile`이면 언제나 같은 해 | 사용자 확정(rule 기반) |
| **고정** | 모든 정렬·선택 비교자는 **`RequestId`/`VehicleId` 문자열로 끝맺어** 총 순서를 만든다. `Set`·`Map` 순회 순서에 의존 금지 | stage-04 N3 · §9 |
| **고정** | 후보 검증 = 호환 필터 + `RoutePropagator` Feasible + **profile hard 전부 satisfied**. 그래서 construction 결과는 Infeasible일 수 없다 | §4.1 · Domain §8.4 |
| **고정** | best 선택의 권위는 **정식 평가(`Evaluator`)뿐**. 근사·삽입 비용으로 고르지 않는다 | Domain §9.2 MUST NOT |
| **고정** | 배정·제거 단위는 `Request`(pair) 전체. 결과는 전부 `StructureCheck` 통과 | Domain §1.4·§6.3 |
| **고정** | 미배정은 Infeasible이 **아니다** — 전원 bank인 해도 유효하다 | Domain §9.1 · stage-04 E2 |
| **고정** | 거리·시간 값은 `TravelMatrix`에서만 온다. 좌표는 **정렬·분할에만** 쓰고 거리 계산에 쓰지 않으며 표를 대칭화하지 않는다 | Domain §4 MUST NOT |
| **고정** | 새 경로는 실제 미사용 `VehicleId`를 소비한다 | Domain §9.1 |
| **고정** | 초기해에 **시간 상한이 없다.** 종료는 시간이 아니라 구조로 보장한다 (§4.3) | 사용자 확정(D2·D3) |
| 재량 | 8개의 구성·우선순위·기법별 파라미터(적재율 목표 0.85, 4분위 등급 등) | Domain §9.3 |
| 재량 | 8 → 4 축소 (Stage 8 실측 후 사용자 결정) | §9 |

---

## 2. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/solve/`. **하위 패키지를 만들지 않는다**
(stage-04 §2와 같은 규정 — `solve` 한 층 유지).

| 파일 | 책임 한 줄 |
|---|---|
| `solve/ConstructionHeuristic.java` | construction SPI: 기권 판정 + 해 1개 생성 (§3.2) |
| `solve/InitialSolutionBuilder.java` | **포트폴리오 실행기** — 8개 실행·정식 평가·best 선택 (§3.1·§5) |
| `solve/InitialSolutionResult.java` | best + 선택된 기법 id + 평가·score + 기법별 결과 요약 |
| `solve/ConstructionOutcome.java` | 기법 1개의 실행 결과(id·상태·score·소요) — 로그·실험용 |
| `solve/InsertionSearch.java` | **공통 삽입 후보 탐색·검증·비용** (stage-04 §4.3의 구현체, §4.1) |
| `solve/ScarcityRegret2Construction.java` | H1 — 희소성 우선 regret-2 |
| `solve/UrgencyRegret3Construction.java` | H2 — 시간창 긴급 등급 + regret-3 |
| `solve/VehicleZoneFillConstruction.java` | H3 — 차량 외곽 루프 + 최선 존 커밋 |
| `solve/DeadlineSequentialConstruction.java` | H4 — 마감 임박 순 순차 삽입 (종전 baseline) |
| `solve/FarthestSeedSequentialConstruction.java` | H5 — 최원거리 seed 경로별 순차 삽입 |
| `solve/ZoneFirstFfdConstruction.java` | H6 — zone 분할 → FFD 용량 bin → bin 내부 regret-2 |
| `solve/SavingsMergeConstruction.java` | H7 — Clarke-Wright 병합 (방향성 표) |
| `solve/SweepNextFitConstruction.java` | H8 — depot 기준 각도 sweep → next-fit 분할 |

Stage 3 산출물(`Solution`·`Route`·`StructureCheck`·`RoutePropagator`·`Evaluator`)과
`Problem`·`Profile`·`Scores`는 **수정하지 않는다.** 테스트는 §8.

---

## 3. 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.
**어디에도 `RandomGenerator` 인자가 없다** — 그것이 이 문서의 결정성 보증이다.

### 3.1 진입점

```java
public final class InitialSolutionBuilder {
    /** 우선순위 순으로 고정된 기본 8개 (§5). */
    public static List<ConstructionHeuristic> defaults();

    /**
     * 8개(또는 주어진 목록)를 순서대로 실행해 정식 평가로 최선 하나를 고른다.
     * 기권한 기법은 건너뛴다. 전원 기권이면 빈 해(전 Request bank)를 반환한다.
     * profile은 인자다 — Problem에 담기지 않으며, 호출자가 탐색·재검증에 같은 인스턴스를
     * 넘긴다 (Domain §8.4 MUST).
     */
    public static InitialSolutionResult build(Problem problem, Profile profile);
    public static InitialSolutionResult build(Problem problem, Profile profile,
                                              List<ConstructionHeuristic> heuristics);
}

public record InitialSolutionResult(
    Solution best,
    String heuristicId,              // best를 만든 기법. 전원 기권이면 "empty"
    Evaluation evaluation,           // 층 ③ — stage-04 AlnsResult.initialEvaluation로 그대로 간다
    long[] score,                    // 층 ④ — 〃 initialScore
    List<ConstructionOutcome> outcomes) {}   // 우선순위 순. 로그·실험용 (Domain §11.1)

public record ConstructionOutcome(
    String heuristicId,
    Status status,                   // BUILT | ABSTAINED
    OptionalInt unassignedCount,     // BUILT일 때만
    Optional<long[]> score,          // 〃
    long elapsedMillis) {             // 8→4 축소 판단의 입력 (§9)
    public enum Status { BUILT, ABSTAINED }
}
```

- `outcomes`는 `AlnsRunStats`와 같은 성격이다 — **결과 JSON의 run 메타에 넣지 않는다**
  (Domain §11.1, stage-04 §9 Q2 해소와 동일 취급). 로그와 Stage 8 실험용이다.
- `InitialSolutionResult` 자체를 `equals`로 비교하지 않는다 — `long[]` 필드 때문
  (Stage 3 §4.3 공통 규칙).

### 3.2 construction SPI

```java
public interface ConstructionHeuristic {
    String id();

    /**
     * 이 Problem에서 이 기법이 성립하지 않으면 true — 실행하지 않고 건너뛴다.
     * 기권은 실패가 아니다 (§4.4). Problem만 보고 판정하며 부작용이 없다.
     */
    boolean abstains(Problem problem);

    /**
     * 빈 해에서 시작해 Request를 pair 단위로 삽입한 Solution 하나를 만든다.
     * 넣지 못한 Request는 bank에 남는다 — 유효한 해다 (Domain §9.1).
     * 반환 해는 StructureCheck를 통과하고 Evaluator Feasible이어야 한다 (§4.1).
     */
    Solution construct(Problem problem, Profile profile);
}
```

### 3.3 공통 삽입 탐색

```java
public final class InsertionSearch {
    /** Request 하나를 넣을 수 있는 모든 (경로, 위치) 후보를 비용 오름차순으로. 없으면 빈 목록. */
    public static List<Candidate> candidates(Problem problem, Profile profile,
                                             Solution current, RequestId requestId);

    /** 후보를 적용한 새 Solution (불변 — 원본은 그대로). */
    public static Solution apply(Solution current, Candidate candidate);

    public record Candidate(
        VehicleId vehicleId,
        boolean newRoute,
        List<NodeId> visits,          // 삽입이 끝난 그 경로의 전체 방문 목록
        long deltaDriveDistMeter,
        long deltaRouteOperationalTimeSec) {

        /** 사전식 비용: (새 경로 여부, Δ거리, Δ운행시간). 후보 '고르기' 전용 (§4.2). */
        public static Comparator<Candidate> byCost();
    }
}
```

---

## 4. 공통 기반

### 4.1 후보 탐색과 검증 (stage-04 §4.3의 개정판)

```text
대상 경로:  problem.compatibleVehicles(requestId)에 든 차량의 기존 경로
          + 그중 미사용 차량 하나로 여는 새 경로
            (미사용 호환 차량이 여럿이면 VehicleId 문자열 순 첫 번째만 — 결정성)
위치:      DELIVERY_ONLY  — delivery NodeId를 각 위치 0..n
          PICKUP_ONLY    — pickup NodeId를 각 위치 0..n
          PICKUP_DELIVERY— pickup 위치 i ≤ delivery 위치 j 의 모든 (i, j)
                           픽업 선행이 후보 생성 규칙 자체로 보장된다
검증:      ① RoutePropagator.propagate(problem, vehicleId, visits′)
              → Infeasible이면 탈락 (그 경로의 정확한 물리 계산 — stage-04 N7)
           ② profile.hardConstraints() 전부 satisfied(problem, facts)
              → 하나라도 거짓이면 탈락                              ← 이 문서가 추가하는 것
비용:      (newRoute, ΔdriveDistMeter, ΔrouteOperationalTimeSec) 사전식.
           동률이면 (VehicleId, 삽입 위치) 문자열·정수 순 — 총 순서.
           이 비용은 후보 '고르기'에만 쓴다. best 선택은 §5의 정식 평가만이 정한다.
```

**②가 성립하는 이유 — 경로 단위 검증이 완결적이다.** `HardConstraint.satisfied`는
**시그니처가 `(Problem, RouteFacts)`** 라 경로 하나만 본다 (Stage 3 §4.2). 그러므로
"모든 경로가 satisfied"면 `Evaluator`가 profile hard로 Infeasible을 낼 방법이 없다.
해 전체 수준의 요구(최소 배정량·경로 간 균형 등)는 이 SPI로 **표현 자체가 불가능**하고,
그런 요구는 hard가 아니라 점수 축으로 정의하라는 것이 이미 stage-04 E16b의 규정이다.
따라서 검증 3종(호환 필터·전파·profile hard)은 `Evaluator`가 보는 것 전부와 같고,
construction 결과의 Infeasible은 **논리적으로 불가능** — 나오면 버그다 (§7 X5).

- ΔrouteOperationalTimeSec은 삽입 전후 `RouteFacts.routeOperationalTimeSec()`의 차다.
  **초를 세지 않고 두 값의 차로 잰다** (Domain §3.2 — 창마다 1초 어긋나는 것을 막는다).
- 시간 조회는 `problem.resolvedSpeedKmH(vehicleId)`를 넘겨 `TravelMatrix.timeSec`로 한다.
  **속도별 표이므로 "그" 시간표를 캐싱하면 혼합 속도 차대에서 버그다.**
- 증분 계산·shortlist는 이 문서 범위 밖이다 (stage-04 N4 그대로 — 후보 1개 검증은 경로
  전체 재전파다). 도입 시 "캐시 점수 = 전체 재계산 점수" 대조 테스트가 필수다.

### 4.2 결정성 규칙 (전 기법 공통)

| 규칙 | 이유 |
|---|---|
| 모든 비교자는 `RequestId`/`VehicleId`/`LocationId` 문자열 비교로 끝난다 | 총 순서가 없으면 동률에서 순회 순서가 결과를 바꾼다 |
| `HashSet`/`HashMap` **순회로 승자를 고르지 않는다** — 후보를 목록으로 모아 정렬한 뒤 첫 원소 | 참고 구현(ro-core)의 4개 기법이 난수 없이도 run 간 재현이 안 되는 원인이 정확히 이것이다 |
| 부동소수(각도·적재율)는 **정렬 키로만** 쓰고 동률 판정에 쓰지 않는다 — 동률은 문자열로 깬다 | 부동소수 동률은 플랫폼 의존 |
| `Problem.locations()`는 삽입 순서를 보존한다 — 그 순서에 의존하지 않고 명시 정렬한다 | 순서 보존은 인덱스 안정성 목적이지 정렬 계약이 아니다 |

### 4.3 종료 보장 — 시간이 아니라 구조로

초기해에는 **시간 상한이 없다** (D2). `AlnsConfig.timeLimitSec`은 ALNS 루프에만 걸린다
(stage-04 §9 **Q1 해소**). 무한 루프는 다음 불변식으로 막는다:

```text
모든 기법의 바깥 루프는 매 반복에서 다음 중 하나를 반드시 수행한다:
  (a) Request 정확히 1개를 경로에 삽입한다        → 미처리 집합 크기 −1
  (b) Request 1개를 "후보 0개"로 판정해 제외한다   → 미처리 집합 크기 −1
⇒ 바깥 루프는 최대 |problem.requests()| 회에서 끝난다.
방어 카운터가 그 상한을 넘으면 IllegalStateException — 조용히 자르지 않는다
(stage-04 N2와 같은 취급: 구조 결함은 품질 문제가 아니라 버그다).
```

- **방어 카운터는 바깥 루프에만 건다.** regret 계열은 매 바깥 반복마다 bank 전체의
  최선·차선을 다시 계산하므로(§5 H1·H2), 안쪽 재계산 횟수는 이 상한과 무관하다.
- H7(병합)은 예외적으로 "경로 수가 매 병합마다 1 줄어든다"가 상한이다 — 초기 경로 수는
  `|requests|`이므로 같은 결론이다.
- 초기해가 오래 걸린다는 사실 자체는 정상 종료를 막지 않는다. 전체 응답 시간의 관리는
  Stage 6 executor 몫이다 (§9).

### 4.4 기권 (abstain)

기법이 성립하지 않는 `Problem`에서는 **실행하지 않고 건너뛴다.** 기권은 실패가 아니라
정상 경로다 — 참고 자료가 말하는 merge·cluster-first 계열은 "고객 1명 = 방문 1개"를
전제로 정의돼 있어 `PICKUP_DELIVERY`가 섞이면 다른 알고리즘이 되기 때문이다.

| 기권 사유 | 해당 기법 |
|---|---|
| `PICKUP_DELIVERY` Request가 하나라도 있음 | H7·H8 |
| `PICKUP_ONLY` Request가 하나라도 있음 | H8 |
| `problem.depots().size() != 1` | H5·H7·H8 |

**`zoneId` 부재는 기권 사유가 아니다.** 존이 하나도 없으면 H3·H6는 전체를 단일 그룹으로
다루도록 **퇴화**하는데, 그래도 각자의 정책(차량 외곽 루프 / FFD 용량 분할)은 그대로 살아
있어 H4와 다른 해를 낸다. 기권시킬 이유가 없다.

- **기본 8개에서는 H1·H2·H3·H4·H6가 어떤 `Problem`에서도 기권하지 않는다** — 즉 전원 기권은
  일어나지 않는다. §6-3의 빈 해 경로는 기법 목록을 직접 넘기는 오버로드(§3.1)를 위한 것이고,
  T16이 그 오버로드로 검증한다.
- 실물 fixture(`data/win_poc_case_floor.json` — 단일 depot·`DELIVERY_ONLY`·`zoneId` 존재)에서는
  **8개 전부 실행된다.**

---

## 5. 8개 기법 (우선순위 순)

우선순위 = **win 규모(주문 452·차량 31)에서의 기대 품질 × 구현 비용**의 잠정 판단이다.
Stage 8 실측으로 재정렬될 수 있다 (§9). 우선순위는 두 곳에서 실제로 쓰인다 —
**실행 순서**와, best가 **동점일 때의 승자**(앞선 기법이 이긴다).

생성 방식 계열을 흩어 놓았다 (참고 자료 §7 — 비슷한 정책을 여럿 넣으면 결과가 갈리지 않는다):
parallel-regret 2 · vehicle-outer 1 · sequential 2 · cluster-first 2 · merge 1.

기호: `n` = Request 수, `m` = 차량 수, `L` = 경로 평균 길이.
"반복당 작업"은 **후보 1개 검증 = 경로 전체 재전파 `O(L)`** 를 곱한 값이다 (§4.1).

**아래 표는 단일 방문(`DELIVERY_ONLY`·`PICKUP_ONLY`) 기준이다.**
`PICKUP_DELIVERY`는 §4.1이 `(i ≤ j)` 쌍을 전부 나열하므로 위치 수가 `O(L)` → `O(L²)`가 되고,
따라서 **전 패턴을 지원하는 H1~H6는 PD Request 비중만큼 `L`이 한 번 더 곱해진다**
(예: H1·H2는 `O(n·m·L²)` → `O(n·m·L³)`). 실물 fixture는 `DELIVERY_ONLY` 지배라 표의 값이
그대로지만, PD가 섞인 입력에서는 이 배수를 계산에 넣어야 한다.

| # | id | 계열 | 지원 패턴 | 바깥 루프 | 반복당 작업 |
|---:|---|---|---|---|---|
| H1 | `scarcity-regret2` | parallel-regret | 전 패턴 | ≤ n | `O(n · m · L²)` |
| H2 | `urgency-regret3` | parallel-regret | 전 패턴 | ≤ n | `O(n · m · L²)` |
| H3 | `vehicle-zone-fill` | vehicle-outer | 전 패턴 | ≤ m | `O(존 수 · n · L²)` |
| H4 | `deadline-sequential` | sequential | 전 패턴 | = n | `O(m · L²)` |
| H5 | `farthest-seed-sequential` | sequential | 전 패턴 | ≤ n | `O(n · L²)` |
| H6 | `zone-first-ffd` | cluster-first | 전 패턴 | ≤ n | `O(bin 내 n · L²)` |
| H7 | `savings-merge` | merge | PD 제외 | ≤ n | `O(1)` 병합 + `O(L)` 전파 |
| H8 | `sweep-next-fit` | cluster-first | DELIVERY_ONLY | = n | `O(L²)` |

H1·H2가 가장 비싸고 가장 좋을 것으로 본다. H4·H5·H8은 싸다. **이 표의 실측값이 8→4 결정의
입력**이므로 T25가 기법별 소요를 출력한다 (§8).

### H1 `scarcity-regret2` — 희소성 우선 regret-2

> 지금 넣지 않으면 나중에 못 넣을 Request를 먼저 넣는다. "못 넣을 위험"을 **쓸 수 있는
> 차량이 몇 대뿐인가**로 먼저 재고, 같은 희소도 안에서 regret으로 잰다.

참고 구현 `EntranceAwareScarcityHeuristics`의 희소성 개념을 옮긴 것이다. ro-next에서는
그 값이 이미 계산돼 있다 — `problem.compatibleVehicles(requestId).size()`.

```text
미처리 = 모든 RequestId
while 미처리 ≠ ∅:
  각 r ∈ 미처리에 대해 InsertionSearch.candidates(problem, profile, s, r) 계산
    best1(r) = 최소 비용, best2(r) = 차선 비용
    후보 0개  → r을 미처리에서 제외하고 bank에 남긴다      (종료 규칙 b)
    후보 1개  → regret(r) = +∞  (지금 못 넣으면 기회를 잃는다)
    그 외     → regret(r) = best2.Δ거리 − best1.Δ거리
  선택 키 = ( compatibleVehicles(r).size() ASC,   ← 희소한 것 먼저
              regret(r) DESC,
              RequestId ASC )
  선택된 r을 best1 위치에 삽입                                 (종료 규칙 a)
```

- 희소도가 regret보다 **앞선다.** 호환 차량이 1대뿐인 Request는 그 차가 차기 전에 넣어야
  하고, 그 손실은 regret이 재는 "비용 격차"보다 크다 (미배정은 score 1번 축이다).
- `+∞`는 `Long.MAX_VALUE`로 두지 않고 `boolean onlyCandidate` 축을 하나 더 두어
  `(희소도, onlyCandidate DESC, regret DESC, RequestId)`로 비교한다 — 오버플로 회피.

### H2 `urgency-regret3` — 시간창 긴급 등급 + regret-3

> 시간창이 좁은 Request를 먼저 넣는다. H1과 같은 계열이지만 **급한 정도의 정의가 다르고**
> (차량 희소성 → 시간창 폭), regret 깊이가 3이라 서로 다른 해를 낸다.

```text
긴급 등급 = 문제 전체 Request의 창 폭 합을 오름차순 정렬해 4등분한 사분위 등급 (0..3).
  창 폭 합 = (delivery가 있으면 delivery, 없으면 pickup) side의 Σ(close − open)
  등급 경계는 Problem에서 한 번 계산하고 동결한다 — 삽입 중에 바뀌지 않는다 (결정적).
미처리 = 모든 RequestId
while 미처리 ≠ ∅:
  각 r에 대해 후보 상위 3개 비용 c1 ≤ c2 ≤ c3 (Δ거리 기준)
    후보 0개 → 제외 (규칙 b)
    regret3(r) = (c2 − c1) + (c3 − c1),  없는 항은 그 자리를 c1으로 채우지 않고
                 "후보 수 ASC"를 선택 키 앞에 두어 대신한다 (후보가 적을수록 급하다)
  선택 키 = ( 긴급 등급 ASC, 후보 수 ASC, regret3 DESC, RequestId ASC )
  선택된 r을 c1 위치에 삽입 (규칙 a)
```

- 등급을 **4분위로 뭉개는 것**이 핵심이다. 창 폭을 그대로 1순위 키로 쓰면 regret이 동률
  깨기로만 작동해 H1과 사실상 같은 정책이 된다.
- 참고 자료 §6.2의 `urgency_bonus`를 가중합으로 넣지 않는다 — 축을 한 숫자로 뭉개지 않는
  이 저장소의 규칙(Domain §8.3)과 같은 태도를 삽입 규칙에도 유지한다.

### H3 `vehicle-zone-fill` — 차량 외곽 루프 + 최선 존 커밋

> 차량 하나를 골라, **그 차에 가장 잘 맞는 존 하나를 통째로** 채우고 다음 차량으로 넘어간다.
> 차를 가득 채워 보내므로 **사용 차량 수(score 2번 축)를 construction 단계에서 직접 줄인다.**

참고 구현 `BrownHeuristics`를 옮긴 것이다. **이 문서가 route elimination을 범위에서
제외했으므로(D4), 차량 수를 직접 공략하는 유일한 기법이다.**

```text
차량 순서 = ( 그 차량이 담당 가능한 Request 수 ASC,   ← 까다로운 차부터
              maxWeight DESC, VehicleId ASC )
for v in 차량 순서:
  미배정 Request를 zoneId로 그룹핑 (zoneId 부재는 "(none)" 그룹)
  존 순서 = ( 그 존 Request들의 호환 차량 집합 교집합 크기 ASC,
              존 Request 수 DESC, zoneId ASC )
  for z in 존 순서:                                   ← what-if: 커밋하지 않는다
     z의 Request를 (요구 weight DESC, RequestId ASC)로 v의 빈 경로에 §4.1 최소 비용 삽입
     채점 = ( 전원 삽입 성공 여부, 적재율 )
       적재율 = max(Σweight / v.maxWeight, Σvolume / v.maxVolume)
  커밋 규칙: "전원 성공 ∧ 적재율 > 0.85"인 존이 있으면 그중 적재율 최대,
             없으면 적재율 최대 존. 동률은 zoneId ASC.
  그 존의 what-if 결과만 실제 경로로 채택. 나머지는 버린다.
  v가 아무것도 못 받으면 v는 사용하지 않는다 (빈 경로를 만들지 않는다 — Stage 3 E30)
미배정이 남으면 다음 차량으로. 차량이 떨어지면 남은 것은 bank.
```

- **롤백이 필요 없다.** `Solution`·`Route`가 불변 record라 what-if 결과를 그냥 버리면 된다
  (참고 구현이 dummy 차량으로 되돌리던 부분이 여기서는 사라진다).
- 0.85는 재량 상수다 (참고 구현의 값). Stage 8 조정 대상.
- 종료: 바깥 루프가 차량 목록이므로 ≤ `m`. 매 반복이 최소 0개 이상을 배정하고 차량을
  소비하므로 진행이 보장된다.

### H4 `deadline-sequential` — 마감 임박 순 순차 삽입

> stage-04 §4.1의 종전 greedy를 **그대로** 기법 하나로 편입한 것이다. baseline 역할이고,
> 다른 7개가 이것보다 나쁘면 그 기법에 문제가 있다는 신호다.

```text
삽입 순서 = ( 있는 delivery의 마지막 창 close ASC,
              delivery가 없으면 pickup의 마지막 창 close ASC,
              RequestId ASC )
각 Request를 §4.1 최소 비용 위치에 삽입. 후보 0개면 bank에 남긴다.
```

- "마지막 창 close"인 이유: 시간창이 `List<TimeWindow>`이므로 어느 close인지 정해야 하고,
  **가장 늦게까지 받아 주는 시각**이 급한 정도를 나타낸다. 창이 하나면 종전 값과 같다
  (Domain §3.2, 2026-08-10 D4).

### H5 `farthest-seed-sequential` — 최원거리 seed 경로별 순차 삽입

> 경로를 **하나씩 완성**한다. depot에서 가장 먼 Request로 경로를 열고 더 못 넣을 때까지
> 채운 뒤 확정한다. H4가 "모든 경로를 동시에 키우는" 것과 반대 방향이다.

```text
while 미처리 ≠ ∅:
  seed = 미처리 중 ( depot으로부터의 거리 DESC, RequestId ASC ) 첫 번째
         거리 = travel.distanceMeter(기준 depot, seed의 첫 방문 지점)
  seed를 호환 미사용 차량(VehicleId ASC 첫)의 새 경로로 연다. 열 수 없으면 seed 제외 (규칙 b)
  반복: 미처리 중 이 경로에 넣을 수 있는 것 가운데 §4.1 최소 비용 하나를 삽입 (규칙 a)
        넣을 수 있는 것이 없으면 이 경로를 확정하고 바깥 루프로
```

- 먼 곳부터 여는 이유: depot 근처 Request는 나중에 어느 경로에도 싸게 붙지만, 먼 것은
  늦게 남으면 자기 전용 경로를 하나 더 열게 만든다(= 차량 수 증가).
- `problem.depots().size() != 1`이면 기권 — "그 depot"이 정의되지 않는다 (§4.4).

### H6 `zone-first-ffd` — zone 분할 → FFD 용량 bin → bin 내부 regret-2

> 먼저 **어떤 Request들이 한 차에 실릴지**를 용량으로 정하고(1단계), 그 다음 각 묶음
> 안에서만 순서를 정한다(2단계). 참고 자료 §6.4의 cluster-first route-second.

참고 구현 `ZoneFirstTwoPhaseHeuristics`의 구조를 옮긴 것이다.

```text
1단계 — 분할
  Request를 zoneId로 그룹핑. 존 순서 = ( 그 존을 담당 가능한 차량 수 ASC,
                                        존 총 weight DESC, zoneId ASC )
  각 존 안에서 First-Fit Decreasing:
    정렬 키 = ( max(weight / 최대차 maxWeight, volume / 최대차 maxVolume) DESC, RequestId ASC )
    bin = 그 존과 호환인 미사용 차량 (maxWeight DESC, VehicleId ASC 순으로 연다)
    각 Request를 들어가는 첫 bin에 넣는다. 어느 bin에도 안 들어가면 새 bin(새 차량).
    ※ 이 단계는 용량만 본다 — 시간창은 2단계가 판정한다.
2단계 — 순서
  각 bin 안에서 H1의 regret-2를 그 bin의 차량 하나만 대상으로 적용해 방문 순서를 만든다.
  용량은 맞지만 시간창 때문에 못 들어가는 Request는 bank에 남긴다 (규칙 b).
```

- 1단계가 용량만 보는 것은 의도적이다. 이것이 H1·H2와 다른 해를 내는 이유고, 그 대가로
  시간창이 타이트한 문제에서는 미배정이 늘 수 있다 — 그럴 때 포트폴리오가 다른 기법을 고른다.
- `zoneId`가 하나도 없으면 전체가 단일 그룹이 된다 — 기권하지 않는다. 그래도 1단계의 FFD
  용량 분할이 H4·H5와 다른 묶음을 만든다 (§4.4).

### H7 `savings-merge` — Clarke-Wright 병합

> 모든 Request를 **각자 단독 경로**로 두고, 합쳤을 때 절감이 큰 순서로 이어 붙인다.
> 공간 구조가 강한 문제에서 좋은 경로 모양을 만든다 (참고 자료 §6.3).

```text
기권: PICKUP_DELIVERY가 있거나 depots.size() != 1
초기: 각 Request r → 단독 경로 (호환 미사용 차량 VehicleId ASC 첫). 열 수 없으면 bank.
절감: saving(i, j) = d(i, depot) + d(depot, j) − d(i, j)
      d는 travel.distanceMeter — **방향성 그대로**, 대칭화하지 않는다 (Domain §4 MUST NOT).
      saving ≤ 0인 쌍은 후보에서 제외.
정렬: ( saving DESC, RequestId i ASC, RequestId j ASC )
병합: i가 자기 경로의 꼬리이고 j가 다른 경로의 머리일 때만 이어 붙인다.
      합친 Request 전체와 호환인 미사용 차량 중 (maxWeight ASC, VehicleId ASC) 첫 번째를
      재배정하고 §4.1 검증. 통과하면 확정, 아니면 이 쌍은 버린다.
      경로 수가 매 병합마다 1 줄어든다 → 상한 |requests| (§4.3)
```

- **가장 작은 적합 차량으로 재배정**하는 것이 차량 수가 아니라 *차량 크기*를 아끼는 부분이다
  (참고 구현 `SavingsHeuristics`와 같은 규칙).
- 절감식이 depot을 하나 요구하므로 depot이 2개 이상이면 기권한다 — 임의로 하나를 고르면
  결과가 그 선택에 좌우되는데, 그것을 정할 근거가 문서에 없다.

### H8 `sweep-next-fit` — depot 기준 각도 sweep → next-fit 분할

> depot을 중심으로 **부채꼴을 훑으며** 차를 하나씩 채운다. 가장 싸고, 공간적으로 자연스러운
> 경로 모양을 만든다 (참고 자료 §6.4).

```text
기권: DELIVERY_ONLY가 아닌 Request가 있거나 depots.size() != 1
각도: θ(r) = atan2(lat(r) − lat(depot), lon(r) − lon(depot))     ← 좌표는 여기서만, 정렬 전용
      Domain §4 MUST NOT 준수 — 거리·시간 값은 여전히 TravelMatrix에서만 온다.
정렬: ( θ ASC (−π부터), RequestId ASC )                          ← 부동소수 동률은 문자열로
분할: next-fit — 차량 순서 (maxWeight DESC, VehicleId ASC)로 하나를 열고,
      각도 순으로 담다가 weight 또는 volume이 넘치면 **다음 차량으로 넘어간다**
      (앞의 차로 되돌아가지 않는다 — 그래야 각도 연속성이 유지된다)
순서: 각 차량 묶음 안에서 §4.1 최소 비용 순차 삽입으로 방문 순서를 만든다.
      시간창 때문에 못 들어가는 것은 bank (규칙 b).
```

- **next-fit이지 FFD가 아니다.** FFD는 크기 순으로 재정렬하므로 각도 순서를 파괴한다 —
  둘을 동시에 쓸 수 없다. 용량 우선 분할은 H6가 담당하고, 여기서는 각도 연속성을 지킨다.
- 각도의 원점을 `−π`로 고정한다. 시작 각도를 바꾸는 multi-start는 난수·다중 실행이라
  채택하지 않는다 (§1 고정).

---

## 6. 포트폴리오 실행과 선택

```text
1. outcomes = []
2. for h in heuristics (우선순위 순):
     h.abstains(problem) → outcomes += ABSTAINED, 다음 기법
     s = h.construct(problem, profile)
     StructureCheck.check(problem, s) 비어 있지 않으면 → IllegalStateException (버그)
     r = Evaluator.evaluate(problem, profile, s)
     r이 Infeasible이면 → IllegalStateException (§4.1에 의해 불가능 — 버그)
     outcomes += BUILT(h.id(), s.bank().size(), r.score(), 소요)
     후보 목록에 (h.id(), s, r) 적재
3. 후보가 비면 (전원 기권):
     empty = Solution([], 모든 RequestId)
     r = Evaluator.evaluate(problem, profile, empty)
     r이 Infeasible이면 IllegalStateException — 빈 해를 거부하는 hard는 점수 축으로
       정의해야 한다 (stage-04 E16b 그대로)
     return InitialSolutionResult(empty, "empty", …, outcomes)
4. best = Scores.compare 최소. **동률이면 우선순위가 앞선 기법**이 이긴다 (결정적).
5. return InitialSolutionResult(best, 그 기법 id, 그 평가, 그 score, outcomes)
```

- 4번의 동률 규칙 때문에 §5의 우선순위는 문서 장식이 아니라 **결과에 영향을 주는 계약**이다.
- 이 결과가 stage-04 §4.2-2의 `initial`이 되고, `AlnsResult.initialEvaluation`·
  `initialScore`로 그대로 흐른다 — DoD "초기해 대비 개선"의 기준값이 **8개 중 최선**이 된다.
  기준이 올라가므로 그 DoD는 더 엄격해진다.

---

## 7. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| X1 | requests 빈 목록 | 8개 전부 즉시 빈 해 반환. best = 빈 해 | Domain §9.1 |
| X2 | vehicles 빈 목록 / 전 Request 호환 0대 | 전부 bank인 해. 예외 아님 | stage-04 E2 |
| X3 | 전원 기권 | 빈 해 반환 (§6-3). **기본 8개로는 일어나지 않는다** — H1·H2·H3·H4·H6는 기권하지 않는다. 기법 목록을 직접 넘기는 오버로드에서만 도달한다 | §4.4 |
| X4 | 한 기법이 0건 삽입 | 정상 — 전부 bank인 후보로 참여하고, 대개 score 1번 축에서 패배 | Domain §9.1 |
| X5 | construction 결과가 Evaluator Infeasible | **IllegalStateException.** §4.1의 검증 3종이 Evaluator와 같은 것을 보므로 논리적으로 불가능하다 | 사용자 확정(D7)·§4.1 |
| X6 | 빈 해 평가마저 Infeasible | IllegalStateException — profile 구성 결함 | stage-04 E16b 존치 |
| X7 | 두 기법의 score가 완전히 같음 | 우선순위 앞선 기법의 해를 쓴다 (§6-4) | 결정성 |
| X8 | 같은 `Problem`·`Profile` 재실행 | 동일한 `best`·`heuristicId`·`score` (rng 없음). **`InitialSolutionResult` 자체를 `equals`로 비교하지 않는다** — `long[]` 필드 때문이기도 하고, `outcomes.elapsedMillis`는 실행마다 달라지는 관측값이기 때문이다 (stage-04 T5와 같은 규칙) | §1 고정 |
| X9 | H3에서 어떤 차량도 아무 존을 못 받음 | 그 차량은 사용하지 않는다 — 빈 `Route`를 만들지 않는다 | Stage 3 E30 |
| X10 | H6 1단계가 용량만 보고 만든 bin이 시간창 때문에 다 안 들어감 | 남는 것은 bank. 정상 — 포트폴리오가 다른 기법을 고른다 | §5 H6 |
| X11 | H7에서 saving ≤ 0만 존재 | 병합 0회 — 전부 단독 경로인 해. 차량 수 축에서 패배할 뿐 유효 | §5 H7 |
| X12 | H8에서 두 Request의 각도가 부동소수로 같음 | `RequestId` 문자열로 동률을 깬다 | §4.2 |
| X13 | 바깥 루프가 `\|requests\|`를 넘음 | IllegalStateException — 종료 불변식 위반 = 버그 | §4.3 |
| X14 | profile hard가 강해 어떤 삽입도 통과 못 함 | 전부 bank인 해가 8개 나온다. Feasible이므로 정상 | §4.1 |

---

## 8. 테스트 목록

위치: `solver-core/src/test/java/com/ronext/rpdptw/solve/`. 의존은 JUnit만
(Stage 0 §4.2 — property 라이브러리를 추가하지 않는다). `Problem`은 Stage 1·2 경로로
손 조립한다 (fixture JSON 파싱은 Stage 6).
번호는 stage-04 §7의 T1–T12에 **이어서** 붙인다.

| # | 테스트 | 내용 | 대응 |
|---|---|---|---|
| T13 | `InsertionSearchTest.candidateValidationIncludesProfileHard` | 테스트 전용 hard 제약("경로당 방문 1개 초과 금지") profile에서 2개째 삽입 후보가 **후보 단계에서** 탈락 | §4.1 ② |
| T14 | `InitialSolutionBuilderTest.selectsBestByOfficialEvaluation` | 결과가 다른 기법 2개 이상에서 `Scores.compare` 최소인 해가 선택됨 · 동률이면 우선순위 앞선 기법 (X7) | §6-4 |
| T15 | `InitialSolutionBuilderTest.abstainedHeuristicsAreSkipped` | PD 포함 문제 → H7·H8이 `ABSTAINED`, 나머지는 `BUILT`, 결과 유효 | §4.4 |
| T16 | `InitialSolutionBuilderTest.allAbstainedYieldsEmptySolution` | **기권 기법만 담은 목록**을 `build(problem, profile, heuristics)` 오버로드에 넘김 → 빈 해, `heuristicId == "empty"`, 예외 없음 (X3) | §6-3 |
| T17 | `InitialSolutionBuilderTest.everyConstructionResultIsFeasible` | 8개 각각을 직접 `Evaluator.evaluate` → 전부 Feasible. profile hard 있는 문제에서도 (X5·X14) | D7 |
| T18 | `InitialSolutionBuilderTest.deterministicAcrossRuns` | 같은 `Problem`·`Profile`로 두 번 → best 해·`heuristicId` 동등, `score`는 `Arrays.equals` (X8) | §1 |
| T19 | `ConstructionHeuristicsTest.outerLoopBoundedByRequestCount` | 8개 각각 바깥 루프 반복 수 ≤ `\|requests\|` · 방어 카운터 초과 시 예외 (X13) | §4.3 |
| T20 | `ConstructionHeuristicsTest.structureHoldsForEveryHeuristic` | 8개 결과 전부 `StructureCheck` 위반 0 · pair 원자성 · XOR. PD 포함 문제로도 수행 | Domain §6.3 |
| T21 | `SavingsMergeConstructionTest.usesDirectedMatrixOnly` | `d(i,j) ≠ d(j,i)`인 이동표에서 saving이 방향을 구분함 · 좌표로 거리를 재계산하지 않음 | Domain §4 |
| T22 | `SweepNextFitConstructionTest.coordinatesOrderOnly` | 좌표를 바꿔 각도 순서만 바꾸고 이동표는 고정 → 방문 **순서**는 바뀌되 보고된 거리는 표 값과 일치 | Domain §4 |
| T23 | `VehicleZoneFillConstructionTest.commitsOnlyBestZonePerVehicle` | 차량 1대·존 2개에서 적재율 규칙대로 한 존만 커밋 · what-if가 다른 존을 오염시키지 않음 | §5 H3 |
| T24 | `InitialSolutionBuilderTest.recordsPerHeuristicOutcome` | `outcomes`가 우선순위 순이고 기법마다 상태·미배정 수·score·소요를 담음 | §9 (8→4 입력) |
| T25 | `InitialSolutionScaleTest.runsPortfolioOnFullScaleSyntheticProblem` | **규모 측정.** Stage 2 T12·stage-04 T11과 **같은 합성 문제**(장소 453·주문 452·차량 31·이동표 453² 전 쌍)로 포트폴리오 1회 → 정상 종료. **기법별 소요·미배정 수·score와, 포트폴리오 총 소요를 `AlnsConfig.timeLimitSec`으로 나눈 비(比)를 출력해 기록한다** — 절대 초는 그 자체로 해석되지 않는다. 이 비가 1에 가까우면 초기해가 ALNS 예산만큼 시간을 쓰고 있다는 뜻이고, 그것이 8 → 4 축소의 판단 근거가 된다. **한도가 아니다** — 넘겨도 중단하지 않는다 (§4.3). 품질은 판정하지 않는다 | Plan Stage 4 규모 DoD |

---

## 9. 이 문서에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| ALNS 본체(destroy/repair·acceptance·종료)와 그 테스트 T1–T12 | `4-ALNS` ([stage-04](stage-04-alns.md)) | 이 문서는 `4-초기해`만 소유 |
| 경량 개선(intra/inter-route relocate·2-opt)·route elimination | ALNS (stage-04 §4.4) | 사용자 확정(D4) |
| 난수·multi-start·randomized top-m·noise 삽입 비용·racing | **안 함** | 사용자 확정(rule 기반) |
| route pool·MIP set covering/partitioning column 생성 | **범위 밖** | Master §4·§6 |
| top-K 다양해 보관 (best 1개만 넘긴다) | **안 함** | Domain §9.3 "초기해 생성 → ALNS 개선"이 전부 |
| 병렬 실행 (Phase B) | 트리거 대기 | [Stage Extra E4](stage-extra-deferred-features.md) |
| 8 → 4 축소 | Stage 8 실측 후 **사용자 결정** | §5 표의 실측값이 입력 |
| 증분 평가·삽입 shortlist | 재량 (필요 시) | stage-04 N4 |
| 전체 응답 시간(초기해+ALNS+재검증) 타임박스 | Stage 6 executor | stage-04 §9 Q1 |
| 탐색 파라미터(0.85 적재율·4분위 등급 등) 최종값 | Stage 8 | Domain §9.3 |

---

## 10. 미해결 질문

닫힌 질문은 해소 표시를 달아 남긴다 ([README](README.md) 공통 규칙).

| # | 질문 | 처리 |
|---|---|---|
| Q1 | 8개 중 어느 4개를 남길 것인가 | **미결 — 의도된 미결.** Stage 8이 T25로 기법별 소요·품질을 실측하고, 그 표를 보고 사용자가 정한다. 그때까지 8개 전부 실행한다 |
| Q2 | 병렬 실행 시 스레드 총량 | **미결.** Architecture §3.2의 executor 동시 실행 수(기본 1~2)와 곱해지므로 함께 정해야 한다. 스레드풀을 core가 소유할지 app이 주입할지도 그때 결정 ([Stage Extra E4](stage-extra-deferred-features.md)) |
| Q3 | 초기해 실행 시간의 상한 | **해소 (2026-09-02).** 두지 않는다 — 문제 규모에 따라 적정값이 달라져 고정 상한이 오히려 해를 버린다. 종료는 §4.3의 구조적 보장이 담당하고, 시간 컷오프는 결정성(X8)을 깨므로 채택하지 않는다 |
| Q4 | construction이 profile hard를 볼 것인가 | **해소 (2026-09-02).** 본다 (§4.1 ②). stage-04 N8이 "Stage 8에서 필요가 확인되면"으로 유예했던 항목인데, 포트폴리오 선택이 정식 평가로 이뤄지는 이상 Infeasible 후보를 만들어 놓고 버리는 것이 낭비라 지금 당긴다 |
