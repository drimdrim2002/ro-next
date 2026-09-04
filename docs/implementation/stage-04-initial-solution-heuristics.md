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
  - stage-04-initial-solution-heuristics-survey.md (확장 14개의 출처·근거·기각 사유·비교 프로토콜)
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
  - 2026-09-02 **확장 14개(H9~H22) 편입** — 문헌·실전 솔버 조사
    ([survey 문서](stage-04-initial-solution-heuristics-survey.md))의 채택분.
    포트폴리오는 기본 8 + 확장 14 = **22개**가 되고 축소 목표는 8 → 4에서 **22 → 4**로.
    §3.3에 `standaloneDistMeter`·`Candidate.deltaForwardSlackSec` 추가(H11·H14·H13 전용),
    §4.3 종료 상한을 기법별 명시로 일반화(차량 소비 (c) 추가), §4.4 기권 표에 H19·H20,
    §5 표·의사코드 14개, §7 X15~X19, §8 T26~T37 추가. 기존 H1~H8 규정은 무변경
  - 2026-09-02 구현 중 정정 — `InsertionSearch.apply`에 `Problem` 인자 추가.
    bank에서 뺄 `RequestId`는 삽입된 `NodeId`로부터 `Problem.nodeRef`로만 알 수 있다
    (`Solution`·`Candidate`에는 NodeId뿐). XOR을 apply 한 곳에서 지키기 위한 최소 변경
  - 2026-09-02 **실물 맞춤 2개(H23·H24) 편입** — 실물 fixture 실측(부피 여유 4.2%, 허용 차급이
    존과 1:1, 경로 구역 단일성)에 맞춘 존 배정 DP(공통 `ZoneQuotaAllocation`) + 존 내부 적재 2종.
    포트폴리오 22 → **24개**, 축소 목표 24 → 4. §2 파일 3개·§3.4 시그니처·§4.3 종료 상한·
    §4.4 기권 사유(유형 조합 수 > 65,536)·§5 표·의사코드·§7 X20~X23·§8 T38~T43 추가.
    `InsertionSearch.remove` 추가(H23 교환 전용). 근거는 survey §2.5. 기존 H1~H22 규정은 무변경
  - 2026-09-02 **T44 실물 fixture 테스트** — `data/win_poc_case_floor.json`으로 H23 전량 배정·경로 감사·
    H3 대비 사전식 우위를 고정. solver-core 테스트는 JUnit만 쓰므로 Jackson이 있는 **app 모듈**에 두고,
    규약 JSON → `PlanInput` 매핑은 테스트 전용(Stage 6 adapter가 대체)
  - 2026-09-02 구현 중 정정 — **T25 합성 문제에 정차 한도 28 추가**(실물 fixture의
    `VehicleMaxStopCount 28`, Plan fixture 실측). Stage 2 T12 문제 그대로(시간창 종일·용량 무제한·
    정차 한도 없음)는 **경로 하나가 452건을 전부 삼켜** regret 계열이 `O(n⁴)`가 된다 — 실측 H1
    724초(routes 1). 그 값은 실물과 무관한 단일 거대 경로의 소요라 22 → 4 판단의 입력이 못 된다.
    같은 이유로 §9의 재량 항목 **증분 평가를 parallel-regret·matching·global-cheapest 계열
    (H1·H2·H9·H10·H12·H17)에 도입** — `InsertionSearch.Cache`(경로 버전 캐시, 차량별 상위 3개)와
    "캐시 = 전체 재계산" 대조 테스트(T13b). 나머지 기법은 전체 재전파 그대로 (§4.1)
---

# Stage 4 — 초기해 휴리스틱 포트폴리오

`solver-core`의 `solve` 패키지에 **난수를 쓰지 않는 rule 기반 construction 휴리스틱
24개(기본 8 + 확장 14 + 실물 맞춤 2)**와, 그 전부를 실행해 **정식 평가로 최선 하나를 고르는 포트폴리오
실행기**를 만든다. 확장 14개의 출처·근거·기각된 후보는
[survey 문서](stage-04-initial-solution-heuristics-survey.md)가 소유한다 — 이 문서의 §5
의사코드가 구현 계약이다.
[stage-04](stage-04-alns.md)가 ALNS 본체를 소유하고, 이 문서는 그
**진입점(초기해)만**을 소유한다 — 두 문서가 충돌하면 배차 규칙은 언제나
[Domain](../domain-design.md)이 이긴다.

**이 문서는 `4-초기해`를 소유하고, 단독으로 완결된다 (2026-09-02).** Stage 4는 따로 만들고
따로 끝내는 두 단계이고(Plan §2.2), 이 단계가 먼저다. 여기서 만드는 어떤 타입도
`AlnsSolver`·`AlnsConfig`·`AlnsResult`·`DestroyOperator`·`RepairOperator`를 **참조하지 않는다** —
그래서 ALNS가 한 줄도 없는 상태에서 §8의 T13–T44가 전부 green이 될 수 있고, 그것이 이 단계의
완료 판정이다. 의존은 한 방향(ALNS → 초기해)뿐이고, 그쪽마저
[stage-04 §3.2](stage-04-alns.md)의 오버로드로 끊을 수 있다.

```text
[Problem (동결)] + [Profile]
        │
        ▼
  H1 … H24  (결정적 construction 24개 — 기권한 기법은 건너뜀)
        │  각 결과: StructureCheck 통과 + Evaluator Feasible  ← 아니면 버그(예외)
        ▼
  Scores.compare 최소 → best 1개                      ← 선택 권위 = 정식 평가뿐
        │
        ▼
  [initial Solution] → stage-04 §4.2의 ALNS 루프
```

**왜 여러 개인가.** 참고 자료([cvrptw_heuristic_strategy_summary](../orgin/cvrptw_heuristic_strategy_summary.md) §3)의
결론과 같다 — CVRPTW/PDPTW는 인스턴스 성격(클러스터링 정도·시간창 타이트함·용량 지배 여부)에 따라
좋은 construction 방식이 달라져, 하나를 아무리 정교하게 만들어도 다른 유형에 약하다.
다만 그 자료가 권하는 **난수·multi-start·racing은 채택하지 않는다** (§1 고정 항목).
여기서 얻는 것은 분산 감소가 아니라 **instance 적응성** 하나뿐이다.
확장 14개는 2026-09-02 문헌·실전 솔버 조사에서 기본 8개에 없는 축(동적 희소도·경로 수 seed·
DP 분할·시간창 거리 클러스터·매칭 모드 등)을 여는 것만 채택했다 —
전 기법을 T25로 실측한 뒤 4개만 남긴다 ([survey §5](stage-04-initial-solution-heuristics-survey.md)).
실물 맞춤 2개(H23·H24, 2026-09-02)는 실물 fixture 구조 실측에서 나왔다 — 근거는 [survey §2.5](stage-04-initial-solution-heuristics-survey.md).

---

## 0. 이 문서가 stage-04에서 이어받는 것 / 바꾸는 것

| stage-04의 종전 규정 | 이 문서의 처리 |
|---|---|
| §3.4 `InitialSolutionBuilder.build(Problem)` — 결정적 greedy 1개 | **개정** — `build(Problem, Profile)`, 포트폴리오 (§3.1) |
| §4.1 "초기해는 1개다" | **개정** — 후보 24개, 결과 1개 (§5) |
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
| **고정** | 24개 전부 **결정적**이다 — `RandomGenerator`를 받지도 쓰지도 않는다. 같은 `Problem`·`Profile`이면 언제나 같은 해 | 사용자 확정(rule 기반) |
| **고정** | 모든 정렬·선택 비교자는 **`RequestId`/`VehicleId` 문자열로 끝맺어** 총 순서를 만든다. `Set`·`Map` 순회 순서에 의존 금지 | stage-04 N3 · §9 |
| **고정** | 후보 검증 = 호환 필터 + `RoutePropagator` Feasible + **profile hard 전부 satisfied**. 그래서 construction 결과는 Infeasible일 수 없다 | §4.1 · Domain §8.4 |
| **고정** | best 선택의 권위는 **정식 평가(`Evaluator`)뿐**. 근사·삽입 비용으로 고르지 않는다 | Domain §9.2 MUST NOT |
| **고정** | 배정·제거 단위는 `Request`(pair) 전체. 결과는 전부 `StructureCheck` 통과 | Domain §1.4·§6.3 |
| **고정** | 미배정은 Infeasible이 **아니다** — 전원 bank인 해도 유효하다 | Domain §9.1 · stage-04 E2 |
| **고정** | 거리·시간 값은 `TravelMatrix`에서만 온다. 좌표는 **정렬·분할에만** 쓰고 거리 계산에 쓰지 않으며 표를 대칭화하지 않는다 | Domain §4 MUST NOT |
| **고정** | 새 경로는 실제 미사용 `VehicleId`를 소비한다 | Domain §9.1 |
| **고정** | 초기해에 **시간 상한이 없다.** 종료는 시간이 아니라 구조로 보장한다 (§4.3) | 사용자 확정(D2·D3) |
| 재량 | 24개의 구성·우선순위·기법별 파라미터(적재율 목표 0.85, 4분위 등급, H17 seed 비율 0.25, H22 라운드 5, H23·H24 유형 조합 한계 65,536, H24 부피 양자화 20,000·pool 배율 1.5·정차 하한 비례 0.8 등) | Domain §9.3 |
| 재량 | 24 → 4 축소 (Stage 8 실측 후 사용자 결정 — [survey §5](stage-04-initial-solution-heuristics-survey.md) 프로토콜) | §9 |

---

## 2. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/solve/`. **하위 패키지를 만들지 않는다**
(stage-04 §2와 같은 규정 — `solve` 한 층 유지).

| 파일 | 책임 한 줄 |
|---|---|
| `solve/ConstructionHeuristic.java` | construction SPI: 기권 판정 + 해 1개 생성 (§3.2) |
| `solve/InitialSolutionBuilder.java` | **포트폴리오 실행기** — 24개 실행·정식 평가·best 선택 (§3.1·§5) |
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
| `solve/FeasibleRoutesRegretMConstruction.java` | H9 — 동적 삽입 가능 경로 수 우선 + regret-m |
| `solve/ChainSeedRegretMConstruction.java` | H10 — 시간 연쇄 배제 seed + regret-m |
| `solve/I1SavingsSequentialConstruction.java` | H11 — 단독 경로 대비 절감 최대 순차 삽입 (Solomon I1 c2) |
| `solve/RouteBiddingConstruction.java` | H12 — 경로 측이 요청을 고르는 입찰 라운드 |
| `solve/SlackPreservingSequentialConstruction.java` | H13 — 시간창 여유 손실 최소 순차 삽입 |
| `solve/VehicleFillRemainingRegretConstruction.java` | H14 — 차량 순서 채움 + 남은 차량 대비 regret |
| `solve/WeakestFitDecreasingConstruction.java` | H15 — difficulty 순 요청 × strength 순 차량 (WFD) |
| `solve/ConstrainedPathExtensionConstruction.java` | H16 — 제약 우선 arc 확장 (경로 끝에 잇기) |
| `solve/FarthestSeedGlobalCheapestConstruction.java` | H17 — farthest seed + 전역 최저 삽입 |
| `solve/GiantTourSplit.java` | H18·H19 공통 — 요청 순열의 DP 최적 분할 (Split) |
| `solve/HilbertSplitConstruction.java` | H18 — Hilbert 곡선 순열 + Split |
| `solve/NearestNeighborSplitConstruction.java` | H19 — 방향성 표 NN 순열 + Split |
| `solve/GapSweepBidirectionalConstruction.java` | H20 — 최대 간극 제로각 양방향 sweep (전 패턴) |
| `solve/SpatiotemporalClusterConstruction.java` | H21 — 시공간 거리 응집 클러스터 → 차량 배정 |
| `solve/SqueakyWheelSequentialConstruction.java` | H22 — 구성→blame→재정렬 반복 (SWO) |
| `solve/ZoneQuotaAllocation.java` | H23·H24 공통 — 존 → 차량 유형 대수 배정 DP (호환 그룹 누적 검사 포함) |
| `solve/ZoneQuotaBalancedFillConstruction.java` | H23 — 존 배정 DP + 부호 있는 정차 예산 best-fit + 1-1 교환 |
| `solve/ZoneQuotaSubsetFillConstruction.java` | H24 — 존 배정 DP + seed pool subset-sum DP + 오라클 피드백 |

Stage 3 산출물(`Solution`·`Route`·`StructureCheck`·`RoutePropagator`·`Evaluator`)과
`Problem`·`Profile`·`Scores`는 **수정하지 않는다.** 테스트는 §8.

---

## 3. 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.
**어디에도 `RandomGenerator` 인자가 없다** — 그것이 이 문서의 결정성 보증이다.

### 3.1 진입점

```java
public final class InitialSolutionBuilder {
    /** 우선순위 순으로 고정된 24개 — 기본 8 + 확장 14 + 실물 맞춤 2 (§5). */
    public static List<ConstructionHeuristic> defaults();

    /**
     * 24개(또는 주어진 목록)를 순서대로 실행해 정식 평가로 최선 하나를 고른다.
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

    /** 후보를 적용한 새 Solution (불변 — 원본은 그대로). 삽입된 Request는 bank에서 빠진다
     *  — NodeId → RequestId는 problem.nodeRef로 푼다 (XOR 유지, Domain §6.3). */
    public static Solution apply(Problem problem, Solution current, Candidate candidate);

    /**
     * 요청 하나를 차량 v의 단독 경로로 돌 때의 이동표 거리 합 —
     * start→(pickup→)delivery→end 중 그 요청·차량에 있는 구간만 더한다. 오라클이 아니라
     * 표 조회다 (feasibility를 보장하지 않는다). H11 절감·H14 regret 전용 (Solomon c2의 λ·d_0u 항).
     */
    public static long standaloneDistMeter(Problem problem, VehicleId vehicleId, RequestId requestId);

    /** Request 하나를 경로에서 빼 bank로 돌린 새 Solution (불변). 경로가 비면 그 Route는 사라진다 —
     *  빈 Route를 두지 않는다 (Stage 3 E30). H23 1-1 교환 전용 (§5 H23). */
    static Solution remove(Problem problem, Solution current, RequestId requestId);

    public record Candidate(
        VehicleId vehicleId,
        boolean newRoute,
        List<NodeId> visits,          // 삽입이 끝난 그 경로의 전체 방문 목록
        long deltaDriveDistMeter,
        long deltaRouteOperationalTimeSec,
        long deltaForwardSlackSec) {  // 기존 방문들의 forward slack 합 감소량 (§5 H13 전용 — 새 방문 제외)

        /** 사전식 비용: (새 경로 여부, Δ거리, Δ운행시간). 후보 '고르기' 전용 (§4.2).
         *  deltaForwardSlackSec은 여기 들지 않는다 — H13만 자기 비교자로 쓴다 (§5 H13). */
        public static Comparator<Candidate> byCost();
    }
}
```

### 3.4 존 배정 (H23·H24 공통)

```java
final class ZoneQuotaAllocation {
    static final long MAX_COMBINATIONS = 65_536L;     // Π(유형별 대수 + 1) 한계 — 넘으면 H23·H24 기권 (§4.4)

    /** 차량 유형 = (호환 Request 집합, maxWeight, maxVolume, effectiveMaxStopCount)이 같은 차량들.
     *  유형 순서 (maxVolume ASC, maxWeight ASC, 첫 VehicleId ASC) · 유형 안 VehicleId ASC. */
    record VehicleType(Set<RequestId> compatible, long maxWeight, long maxVolume,
                       OptionalInt maxStopCount, List<VehicleId> vehicles) {}
    /** 존 = anchor side zoneId(부재 "(none)")별 Request. 존 순서 (호환 유형 수 ASC, Σvolume DESC, zoneId ASC). */
    record Zone(String zoneId, List<RequestId> members) {}
    /** 유형 목록·존 순서대로의 존 목록·존별 배정 차량(유형 순서 × 유형 안 VehicleId ASC). 배정 없는 존은 빈 목록. */
    record Allocation(List<VehicleType> types, List<Zone> zones, Map<String, List<VehicleId>> vehiclesByZone) {}

    static List<VehicleType> vehicleTypes(Problem problem);
    static long combinations(List<VehicleType> types);            // Π(대수 + 1) — abstains()가 본다
    static Allocation allocate(Problem problem);                  // §5 H23 공통 의사코드의 DP
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

- `deltaForwardSlackSec` = 삽입 전 그 경로 **기존 방문들**의 forward slack 합 − 삽입 후 같은
  방문들의 합. 방문 하나의 forward slack = (그 서비스가 시작된 시간창의 close −
  `VisitFacts.serviceStartSec`) — 전파 결과와 `Problem`의 창 목록으로 계산하고, 새로 삽입된
  방문은 합에 넣지 않는다 (Lu & Dessouky의 "다른 방문의 유연성 손실"만 잰다).
- ΔrouteOperationalTimeSec은 삽입 전후 `RouteFacts.routeOperationalTimeSec()`의 차다.
  **초를 세지 않고 두 값의 차로 잰다** (Domain §3.2 — 창마다 1초 어긋나는 것을 막는다).
- 시간 조회는 `problem.resolvedSpeedKmH(vehicleId)`를 넘겨 `TravelMatrix.timeSec`로 한다.
  **속도별 표이므로 "그" 시간표를 캐싱하면 혼합 속도 차대에서 버그다.**
- 증분 계산·shortlist는 이 문서 범위 밖이다 (stage-04 N4 그대로 — 후보 1개 검증은 경로
  전체 재전파다). 도입 시 "캐시 점수 = 전체 재계산 점수" 대조 테스트가 필수다.
  **2026-09-02 도입분**: `InsertionSearch.Cache` — 요청별·차량별로 "그 경로의 방문 목록 → 그 경로의
  상위 3개 후보"를 기억하고, 경로가 바뀐 차량만 다시 전파한다. 후보 1개의 검증 자체는 여전히
  경로 전체 재전파다(shortlist 아님). 상위 3개면 regret-2·regret-3·regret-m(경로별 최선)·최선이
  전부 전체 재계산과 같다 — T13b가 이를 대조한다. 사용처는 H1·H2·H9·H10·H12·H17뿐이다.

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
  (c) 차량(경로) 1개를 확정해 닫는다              → 남은 차량 수 −1   [H14·H16·H20]
⇒ 바깥 루프는 최대 |requests| + |vehicles| 회에서 끝난다 (기본 8개는 (a)(b)만 쓰므로
  종전 상한 |requests| 그대로다. H12는 라운드마다 (a) 1회 이상 또는 (b)만 남음).
예외 상한 — 루프가 아니라 구조가 유한한 기법:
  H7 병합 ≤ |requests| (경로 수가 매 병합마다 1 감소) ·
  H18/H19 DP 상태 수 (|requests|+1)×(|vehicles|+1) 고정 ·
  H21 병합 ≤ |requests|−1 후 bin당 regret ≤ |requests| ·
  H22 고정 R=5 라운드 × 라운드당 |requests| ·
  H23·H24 존 배정 DP 상태 수 ≤ Π(유형별 대수 + 1) ≤ 65,536 고정(§4.4) ·
  H24 bin당 재DP ≤ |pool| (반복마다 제외 집합 +1 또는 nmax −1).
방어 카운터가 기법별 상한(§5 표)을 넘으면 IllegalStateException — 조용히 자르지 않는다
(stage-04 N2와 같은 취급: 구조 결함은 품질 문제가 아니라 버그다).
```

- **방어 카운터는 바깥 루프에만 건다.** regret 계열은 매 바깥 반복마다 bank 전체의
  최선·차선을 다시 계산하므로(§5 H1·H2), 안쪽 재계산 횟수는 이 상한과 무관하다.
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
| `problem.depots().size() != 1` | H5·H7·H8·H19·H20 |
| 차량 유형 조합 수 Π(유형별 대수 + 1) > 65,536 (`ZoneQuotaAllocation.combinations`) | H23·H24 |

**`zoneId` 부재는 기권 사유가 아니다.** 존이 하나도 없으면 H3·H6는 전체를 단일 그룹으로
다루도록 **퇴화**하는데, 그래도 각자의 정책(차량 외곽 루프 / FFD 용량 분할)은 그대로 살아
있어 H4와 다른 해를 낸다. 기권시킬 이유가 없다.

- **기본 24개에서는 H1·H2·H3·H4·H6와 확장 12개(H9~H18·H21·H22)가 어떤 `Problem`에서도
  기권하지 않는다** — 즉 전원 기권은 일어나지 않는다. 확장 14개 중 기권할 수 있는 것은
  H19·H20(단일 depot 요구)뿐이고, 실물 맞춤 H23·H24는 차량 유형 조합 수가 한계를 넘으면 기권한다
  (전 차량이 제각각인 31대면 2³¹ — DP 상태 수를 구조로 묶기 위한 한계다). §6-3의 빈 해 경로는 기법 목록을 직접 넘기는 오버로드(§3.1)를 위한 것이고,
  T16이 그 오버로드로 검증한다.
- 실물 fixture(`data/win_poc_case_floor.json` — 단일 depot·`DELIVERY_ONLY`·`zoneId` 존재)에서는
  **24개 전부 실행된다** (유형 6종, 조합 25,920).

---

## 5. 24개 기법 (우선순위 순 — 기본 H1~H8 + 확장 H9~H22 + 실물 맞춤 H23~H24)

우선순위 = **win 규모(주문 452·차량 31)에서의 기대 품질 × 구현 비용**의 잠정 판단이다.
Stage 8 실측으로 재정렬될 수 있다 (§9). 우선순위는 두 곳에서 실제로 쓰인다 —
**실행 순서**와, best가 **동점일 때의 승자**(앞선 기법이 이긴다).
**확장 14개는 기본 8개 뒤에, 실물 맞춤 2개는 그 뒤에 둔다** — 실측 전에는 검증된 순서를 앞세운다는 뜻이고,
동점에서 기존 기법이 이기므로 확장 편입이 기존 결과를 바꾸는 경우는 "확장이 엄격히
더 좋은 해를 낸 경우"뿐이다. 확장 기법의 출처·근거 수치는
[survey 문서 §2~§3](stage-04-initial-solution-heuristics-survey.md)에 있다 — 여기 의사코드가 계약이다.

생성 방식 계열을 흩어 놓았다 (참고 자료 §7 — 비슷한 정책을 여럿 넣으면 결과가 갈리지 않는다):
parallel-regret 4 (H1·H2·H9·H10) · sequential 5 (H4·H5·H11·H13·H22) · vehicle-outer 3 (H3·H14·H15) ·
cluster-first 3 (H6·H20·H21) · merge 1 (H7) · sweep 1 (H8) · matching 1 (H12) ·
extension 1 (H16) · global-cheapest 1 (H17) · route-first(split) 2 (H18·H19) · zone-quota 2 (H23·H24).

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
| H9 | `feasible-routes-regret-m` | parallel-regret | 전 패턴 | ≤ n | `O(n · m · L²)` |
| H10 | `chain-seed-regret-m` | parallel-regret | 전 패턴 | ≤ n | `O(n · m · L²)` |
| H11 | `i1-savings-sequential` | sequential | 전 패턴 | ≤ n | `O(n · L²)` + 표 조회 |
| H12 | `route-bidding` | matching | 전 패턴 | ≤ n 라운드 | `O(n · m · L²)` |
| H13 | `slack-preserving-sequential` | sequential | 전 패턴 | = n | `O(m · L²)` |
| H14 | `vehicle-fill-remaining-regret` | vehicle-outer | 전 패턴 | ≤ n + m | `O(n · L²)` + 표 조회 |
| H15 | `weakest-fit-decreasing` | vehicle-outer | 전 패턴 | = n | `O(m · L²)` |
| H16 | `constrained-path-extension` | extension | 전 패턴 | ≤ 2n + m | `O(n · L)` |
| H17 | `farthest-seed-global-cheapest` | global-cheapest | 전 패턴 | ≤ n | `O(n · m · L²)` |
| H18 | `hilbert-split` | route-first(split) | 전 패턴 | DP 고정 | `O(n · B · m)` 전파 (B = 경로 방문 상한) |
| H19 | `nearest-neighbor-split` | route-first(split) | 전 패턴 | DP 고정 | `O(n · B · m)` 전파 |
| H20 | `gap-sweep-bidirectional` | cluster-first(sweep) | 전 패턴 | ≤ 2(n + m) | `O(L²)` × 2방향 |
| H21 | `spatiotemporal-cluster` | cluster-first | 전 패턴 | ≤ 2n | `O(n² log n)` + bin당 regret |
| H22 | `squeaky-wheel-sequential` | sequential(반복) | 전 패턴 | R·n (R = 5) | R × `O(m · L²)` |
| H23 | `zone-quota-balanced-fill` | zone-quota | 전 패턴 | ≤ 3n | 배정 DP `O(Z · S · F · G)` (S ≤ Π(c_t+1), F ≤ S, G = 호환 그룹 수) + `O(m_z · L²)` |
| H24 | `zone-quota-subset-fill` | zone-quota | 전 패턴 | ≤ n + m, bin당 재DP ≤ P | 배정 DP + bin당 `O(P · C · B)` × 반복 (P = pool, C ≤ 20,000, B = 정차 한도) + `O(P · L²)` |

H1·H2·H9·H10·H12·H17이 가장 비싸고, H4·H5·H8·H13·H15·H16은 싸다. H23·H24는 실물 fixture 실측
507 ms·773 ms(2026-09-02)이고 그중 존 배정 DP가 약 500 ms다(전이 3.3M — 두 기법이 각각 계산한다).
**이 표의 실측값이 24→4
결정의 입력**이므로 T25가 기법별 소요를 출력한다 (§8).

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

확장 기법 공통 표기: **anchor(r)** = r의 첫 방문 side(픽업이 있으면 픽업, 없으면 delivery).
anchor 지점 = 그 side의 `LocationId`, anchor 창 = 그 side의 `List<TimeWindow>`.

### H9 `feasible-routes-regret-m` — 동적 삽입 가능 경로 수 우선 + regret-m

> H1의 희소도(정적 호환 차량 수)를 **지금 이 부분해에서 실제로 들어갈 수 있는 경로 수**로
> 바꾸고, regret 폭을 2에서 전 경로 합으로 넓힌 것. Ropke & Pisinger의 단독 실험에서
> regret-m만이 전 문제 전량 배정을 냈다 (survey §2.1).

```text
미처리 = 모든 RequestId
while 미처리 ≠ ∅:
  각 r ∈ 미처리: cands = InsertionSearch.candidates(…). 후보 0개 → 제외 (규칙 b)
    경로별 최선(v) = 같은 vehicleId 후보 중 byCost 최소 (새 경로 후보도 하나의 "경로")
    feasibleRoutes(r) = 그런 경로의 수
    regretM(r) = Σ_경로별최선 (Δ거리 − 전체최선.Δ거리)
  선택 키 = ( feasibleRoutes(r) ASC, regretM(r) DESC, RequestId ASC )
  선택 r을 전체최선 위치에 삽입 (규칙 a)
```

- H1과의 차이: 희소도가 삽입이 진행될수록 **줄어드는 동적 값**이다 — 경로가 차면
  feasibleRoutes가 떨어져 그 요청이 앞으로 당겨진다. big-M 없이 축을 분리했다 (H1의
  onlyCandidate와 같은 취지).

### H10 `chain-seed-regret-m` — 시간 연쇄 배제 seed + regret-m

> "시간상 한 차로 이어 돌 수 없는 요청들"만 seed로 남겨 경로 수 하한을 seed 단계에 박는다
> (Diana & Dessouky — 차량 −8% 근거, survey §2.1).

```text
EPT(r) = anchor 창의 첫 open 최소값 · LDT(r) = 마지막 방문 side 창의 마지막 close
vSlow = 전 차량 resolvedSpeedKmH 최소값   ← 가장 느린 속도로도 이어지면 확실히 이어진다
1) EPT ASC(동률 RequestId) 순회하며 seed 선정. 직전 선정 seed k에 대해
   LDT(k) + travel.timeSec(k의 마지막 방문 지점 → r의 첫 방문 지점, vSlow) ≤ EPT(r)
   이면 r은 k 뒤에 이어 붙일 수 있으므로 seed에서 제외. seed 수 상한 = 미사용 호환 차량 수
2) 각 seed를 호환 미사용 차량(VehicleId ASC 첫)의 새 경로로 연다. 못 열면 그 seed 취소
3) 나머지는 H9의 루프. 단 새 경로 후보는 기존 경로 후보가 0개인 요청에만 허용
   — seed가 정한 경로 수를 지키다가, 정말 안 들어갈 때만 늘린다
```

### H11 `i1-savings-sequential` — 단독 경로 대비 절감 최대 (Solomon I1 c2)

> H5처럼 경로를 하나씩 완성하되, 다음에 넣을 요청을 "**혼자 돌게 하면 얼마나 비싼가**"로
> 고른다 — Solomon c2 = λ·d_0u − c1의 λ=1 형태이고, Sartori & Buriol 2020의 실제 초기해다.
> 가중합이 아니다 — 같은 단위(meter)끼리의 차다.

```text
while 미처리 ≠ ∅:
  seed = 미처리 중 ( anchor 창 마지막 close ASC, RequestId ASC ) 첫 번째   ← Solomon 두 번째 seed 규칙
  seed를 호환 미사용 차량(VehicleId ASC 첫)의 새 경로로 연다. 못 열면 seed 제외 (규칙 b)
  반복: 이 경로에 후보가 있는 r 중
    절감(r) = InsertionSearch.standaloneDistMeter(problem, v, r) − 이 경로 최선.Δ거리
    ( 절감 DESC, RequestId ASC ) 최대를 최선 위치에 삽입 (규칙 a)
    넣을 것 없으면 경로 확정, 바깥 루프로
```

- H5와의 차이 두 가지: seed(마감 임박 vs 최원거리)와 선택 기준(단독 대비 절감 vs 최소 Δ거리).
  절감 기준은 "혼자 두면 비싼 먼 요청"을 먼저 흡수한다.

### H12 `route-bidding` — 경로 측이 요청을 고르는 입찰 (Antes & Derigs)

> 지금까지의 21개는 전부 "요청이 경로를 고른다". 이 기법만 방향이 반대다 — 매 라운드
> 요청들이 최선 경로에 입찰하고, **각 경로가 받은 입찰 중 하나를 고른다.** 여러 경로가
> 한 라운드에 동시에 자라므로 앞 경로가 좋은 요청을 독식하지 않는다.

```text
while 미처리 ≠ ∅:
  각 r: best(r) = candidates 첫 원소 (byCost). 후보 0개 → 제외 (규칙 b)
  입찰함[best(r).vehicleId] += (r, best(r))
  각 차량(VehicleId ASC): 입찰함에 든 것 중 ( byCost, RequestId ASC ) 최소 1건만 수락·삽입
  수락된 삽입은 서로 다른 경로라 동시에 유효하다 — 같은 차량에 몰린 입찰은 1건만 남는다
  이 라운드 수락 0건이면 (전원 제외였음) 종료
```

- 라운드마다 (a)가 1회 이상 일어나거나 전원 (b)이므로 라운드 수 ≤ n (§4.3).
- 원문의 "경로 수 −1 재실행" 루프는 채택하지 않는다 — 반복 실행 비용이며, 차량 수 축은
  포트폴리오의 다른 기법(H10·H14·H18)이 공략한다.

### H13 `slack-preserving-sequential` — 시간창 여유 손실 최소 (Lu & Dessouky)

> H4와 같은 순서로 넣되, 위치를 "**이 삽입이 경로의 다른 방문들에서 시간 여유를 얼마나
> 빼앗는가**"로 고른다. 지금 싼 삽입이 뒤 요청의 자리를 없애는 것을 사전에 피한다 —
> 다일 근무창에서 창 끝을 넘겨 다음 창으로 밀리는 큰 지연(Domain §3.2)을 정확히 겨눈다.

```text
삽입 순서 = H4와 동일 (마감 임박 순)
위치 선택 비교자만 다르다:
  ( newRoute ASC, deltaForwardSlackSec ASC, Δ거리 ASC, Δ운행시간 ASC, VehicleId·위치 순 )
deltaForwardSlackSec 정의는 §4.1 — 기존 방문만 센다. 후보 0개면 bank (규칙 b)
```

- 비용축을 가중합으로 섞지 않고 **사전식 첫 키를 바꾼** 것이다 (§1 태도 유지).

### H14 `vehicle-fill-remaining-regret` — 차량 순서 채움 + 남은 차량 대비 regret (VROOM)

> VROOM의 기본 construction. 차량을 큰 것부터 하나씩 채우되, 요청 선택이 "지금 이 차에
> 넣는 비용 − **남은 차들 중 최선의 단독 비용**"이라 뒤로 미뤄도 되는 요청을 뒤로 미룬다.
> H3과 같은 vehicle-outer지만 존 단위 커밋이 아니라 요청 단위 regret이다.

```text
차량 순서 = ( effectiveMaxStopCount DESC(부재 = 최우선), maxWeight DESC,
              Σ근무창 길이 DESC, VehicleId ASC )                          ← 큰 차부터
for v in 차량 순서:
  seed = v와 호환·미처리 중 ( totalWeight DESC, totalVolume DESC, RequestId ASC ) 순으로
         첫 새 경로 삽입 성공. 전부 실패면 v 사용 안 함, 다음 차량 (규칙 c)
  반복: v 경로에 후보가 있는 r에 대해
    regret(r) = min_{v′: 차량 순서상 v 이후 · r과 호환} standaloneDistMeter(problem, v′, r)
    그런 v′가 없으면 noAlt(r) = true   ← 지금 못 넣으면 끝 — 최우선
    선택 키 = ( noAlt DESC, Δ거리(v) − regret(r) ASC, Δ거리 ASC, RequestId ASC )
    선택 r을 v 최선 위치에 삽입 (규칙 a). 없으면 v 확정, 다음 차량 (규칙 c)
차량 소진 후 남은 것은 bank
```

- regret은 이동표 조회(standalone)라 오라클 호출이 없다 — H3보다 반복당 싸다.
- VROOM의 λ 격자·INIT 변종은 채택하지 않는다 (survey §4 보류 — Stage 8 파라미터 실험).

### H15 `weakest-fit-decreasing` — difficulty × strength (Timefold WFD)

> 요청을 **어려운 순**으로, 차량을 **약한 순**으로 — 강한 차량(큰 용량·긴 근무창)을 어려운
> 요청 몫으로 아껴 둔다. bin packing의 WFD를 호환성·시간창이 있는 배차로 옮긴 것.

```text
difficulty 순 = ( compatibleVehicles(r).size() ASC, anchor 창 폭 합 ASC,
                  totalWeight DESC, RequestId ASC )                       ← 어려운 것 먼저
strength 순  = ( maxWeight ASC, maxVolume ASC, Σ근무창 길이 ASC, VehicleId ASC ) ← 약한 차 먼저
각 r (difficulty 순):
  1) 열린 경로를 strength 순으로 훑어 첫 후보 있는 경로의 최선 위치에 삽입
  2) 없으면 미사용 호환 차량을 strength 순으로 — 첫 새 경로 성공에 삽입
  둘 다 없으면 bank (규칙 b)
```

- H1과 신호(호환 수)는 겹치지만 **쓰는 곳이 다르다** — H1은 요청 순서에, H15는 차량
  선택에. H6의 FFD(큰 차부터 여는 것)와 정확히 반대 방향이다.

### H16 `constrained-path-extension` — 제약 우선 arc 확장 (OR-Tools PATH_MOST_CONSTRAINED_ARC)

> 삽입이 아니라 **경로 끝에 잇는다**(path extension). 다음에 이을 요청을 "가장 제약이 심한
> 것"부터 — OR-Tools가 허용 차량이 적은 노드가 있을 때 자동 선택하는 전략의 이식이다.
> 포트폴리오에서 유일한 extension 계열이라 경로 모양이 다르게 나온다.

```text
차량 순서 = H14와 동일
for v in 차량 순서: 경로 = 빈 경로
  반복: 후보 = 미처리 중 v와 호환인 r 전부
    이을 형태: DELIVERY_ONLY/PICKUP_ONLY는 [ …, node ] · PICKUP_DELIVERY는 [ …, p, d ]
              ← pair를 통째로 이어 원자성·픽업 선행이 구조로 보장된다
    키 = ( compatibleVehicles(r).size() ASC, 시작 거리 ASC, RequestId ASC )
      시작 거리 = travel.distanceMeter(경로 마지막 방문 지점, r의 첫 방문 지점).
                  빈 경로면 startDepot 기준, startDepot 없는 차량이면 0 (전원 동률 → 다음 키)
    키 순으로 append를 §4.1 검증(전파 + profile hard). 첫 통과를 확정 (규칙 a)
    전부 실패 → v 확정, 다음 차량 (규칙 c)
차량 소진 후 남은 것은 bank
```

- OR-Tools comparator의 "mandatory" 축은 우리에게 전 요청이 동급이라 없고, "vehicle 도메인
  크기"가 `compatibleVehicles` 수에 대응한다.

### H17 `farthest-seed-global-cheapest` — farthest seed + 전역 최저 삽입 (OR-Tools PCI)

> 차량 일부에 "가장 먼 요청"을 seed로 심은 뒤, 매번 **모든 (요청, 위치) 중 전역 최저**를
> 넣는다 — regret-0(전역 greedy)이며, OR-Tools가 PD 문제의 기본으로 고르는 조합이다.
> H1·H2(regret)와도, H4(고정 순서)와도 다른 해를 낸다.

```text
seed 단계: 차량 ( maxWeight DESC, VehicleId ASC ) 순으로 ⌈0.25 · m⌉대에 대해
  기준 지점 = startDepot(부재 시 endDepot; 둘 다 없으면 그 차량 seed 생략)
  미처리·호환 r을 ( travel.distanceMeter(기준 지점, anchor 지점) DESC, RequestId ASC ) 순으로
  첫 새 경로 성공을 심는다
본 단계: while 미처리 ≠ ∅:
  각 r의 best(r) 중 ( byCost, RequestId ASC ) 전역 최소를 삽입 (규칙 a)
  후보 0개인 r은 제외 (규칙 b)
```

- 0.25는 재량 상수다 (OR-Tools `cheapest_insertion_farthest_seeds_ratio` 대응, Stage 8 조정).

### H18 `hilbert-split` / H19 `nearest-neighbor-split` — 요청 순열 + DP 최적 분할 (Split)

> 지금까지의 기법은 전부 "다음에 무엇을 어디에"를 탐욕으로 정한다. Split은 **순열을 고정한
> 뒤 경로 경계를 DP로 전역 최적으로** 자른다 (Beasley/Prins — HGS의 표준 부품). 같은
> 순열이면 어떤 탐욕 분할(next-fit 포함)보다 나쁠 수 없다. 순열 규칙만 다른 두 기법이다.

공통 `GiantTourSplit` (H18·H19가 순열만 바꿔 공유):

```text
입력: 요청 순열 σ[1..n] · 차량 순서 V[1..m] = ( maxWeight DESC, VehicleId ASC )
상태 f(i, k) = "σ[1..i] 처리·V[1..k] 소비"의 최소 키, 키 = ( bank 수, 사용 차량 수, Σ거리 ) 사전식
전이 ① f(i, k+1) ← f(i, k)                       차량 V[k+1]을 건너뜀
     ② f(i+1, k) ← f(i, k) + (1, 0, 0)           σ[i+1]을 bank
     ③ f(j, k+1) ← f(i, k) + (0, 1, 거리),  i < j ≤ i + B(V[k+1])
        B(v) = v.effectiveMaxStopCount (부재 시 |requests|)
        경로 방문 = σ[i+1..j]를 순열 순으로. PICKUP_DELIVERY는 p 바로 뒤에 d
        검증 = RoutePropagator + profile hard (§4.1). 불가하면 전이 없음. 거리 = 그 경로 driveDistMeter
동률 = 먼저 계산된 값 유지 — i ASC, k ASC, 전이 ①→②→③ 순서 고정이라 결정적
역추적으로 Solution 복원. 상태 수 (n+1)(m+1) 고정 — 루프 상한이 아니라 구조로 종료 (§4.3)
```

- **H18 순열**: anchor 좌표(PD는 픽업·배송의 중점)를 경계 상자로 정규화해 **Hilbert 곡선
  (차수 16) 인덱스 ASC**, 동률 RequestId (Bartholdi & Platzman). 좌표는 순서에만 —
  거리는 전부 ③의 전파가 이동표로 잰다 (Domain §4). depot 무관이라 기권 없음.
- **H19 순열**: 단일 depot에서 시작하는 **방향성 표 위 nearest-neighbor** —
  현재 지점에서 `travel.distanceMeter(현재, r의 첫 방문 지점)` 최소(동률 RequestId)를 잇고
  현재 지점 = r의 마지막 방문 지점. 좌표가 아예 필요 없다. 기권: `depots.size() != 1`.

### H20 `gap-sweep-bidirectional` — 최대 간극 제로각 양방향 sweep (Hertrich 2019)

> H8의 sweep을 세 가지로 일반화한다: 전 패턴(anchor 각도) · 분할 기준이 용량이 아니라
> **오라클 실현성** · **양방향**(시계/반시계 중 정식 평가로 택1). 제로각을 요청이 없는
> 최대 각도 간극에 두어 부채꼴이 클러스터를 가르지 않게 한다. TW 병목 인스턴스에서
> 차량 수 근거(35.0 vs 42.1대)가 있다 (survey §2.2).

```text
기권: depots.size() != 1
θ(r) = H8과 같은 atan2, 단 anchor 지점 기준 (전 패턴)
제로각 = 인접 θ 간극이 최대인 곳 (동률은 앞 요청 RequestId ASC 쪽)
두 방향(시계·반시계) 각각 해를 만든다:
  차량 순서 ( maxWeight DESC, VehicleId ASC ). 현재 차량 경로를 열고 각도 순으로:
    r이 v와 비호환 → 건너뛴다 (뒤 차량 몫으로 남긴다 — H8의 next-fit과 다른 점)
    호환인데 §4.1 후보 0개 → 이 경로 확정, r부터 다음 차량 (규칙 c)
    후보 있으면 최선 위치에 삽입 (규칙 a)
  차량 소진 → 남은 것 bank
두 방향을 Evaluator로 정식 평가해 Scores.compare 최소를 반환. 동률 = 시계 방향
```

- 기법 안에서 Evaluator를 쓰는 것은 §9.2 위반이 아니다 — 근사가 아니라 정식 평가 그 자체다.
- H8은 그대로 존치한다 — 용량 next-fit(H8)과 오라클 sweep(H20)은 다른 해를 낸다.

### H21 `spatiotemporal-cluster` — 시공간 거리 응집 클러스터 (Kerscher & Minner)

> H6의 존 경계를 "**시간창이 서로 맞는가**가 들어간 거리"로 바꾼다. zone이 없거나 zone이
> 시간창과 어긋난 입력에서 H6와 완전히 다른 묶음을 만든다. 응집(agglomerative) 클러스터링은
> 초기화가 없어 그 자체로 결정적이다 — 원 논문이 "deterministic clustering이 우월"을 명시.

```text
표기: e_i = anchor 창 첫 open, l_i = anchor 창 마지막 close, s_i = anchor serviceTimeSec,
      w_i = totalWeight, D_ij = travel.distanceMeter(anchor_i, anchor_j),
      t_ij = travel.timeSec(anchor_i, anchor_j, v0 속도), v0 = ( maxWeight DESC, VehicleId ASC ) 첫 차량
f_ij = l_j − (e_i + s_i + t_ij)          ← i 다음 j가 가능하려면 ≥ 0
h_ij = max(e_j − (l_i + s_i + t_ij), 0)  ← 최소 대기
S_ij = D_ij × (2 − (f_ij − h_ij)/planEndSec + (w_i + w_j)/W),  W = 전 차량 maxWeight 최대
S(i,j) = 유효한(f ≥ 0) 방향의 S 최소값. 양방향 다 무효면 병합 금지 쌍
클러스터링: 요청마다 클러스터 1개에서 시작. complete-linkage —
  병합 가능(호환 차량 교집합 ≠ ∅ · Σweight·Σvolume이 교집합 내 최대 차량 이하 ·
  요청 수 ≤ 그 차량 B) 쌍 중 linkage 최소를 병합. 없을 때까지 (≤ n−1 회)
  linkage(double)는 정렬 키로만 — 동률은 (min RequestId, 상대 min RequestId) 문자열 (§4.2)
배정: 클러스터 순서 ( 교집합 크기 ASC, Σweight DESC, min RequestId ASC ).
  차량 = 교집합 내 미사용 ( maxWeight ASC, VehicleId ASC ) 첫 번째.
  그 차량 하나를 대상으로 H6 2단계와 같은 regret-2로 방문 순서를 만든다.
  못 드는 요청·차량을 못 받은 클러스터의 요청은 bank (규칙 b)
```

### H22 `squeaky-wheel-sequential` — 구성 → blame → 재정렬 반복 (SWO)

> H1의 희소도는 정적 신호다. SWO는 **직전 구성 결과에서 배운 난이도**로 순서를 바꿔
> 다시 짓는다 (Lim–Lim–Rodrigues, PDPTW 원문). 난수 없는 규칙 갱신이라 결정적이고,
> 라운드 수가 고정이라 종료가 구조로 보장된다 (§4.3).

```text
priority(r) = 0. R = 5 라운드 (재량 상수):
  구성: ( priority DESC, H4의 마감 키 ASC, RequestId ASC ) 순으로 H4와 동일한 순차 최소 비용 삽입
  평가: Evaluator 정식 평가. best 갱신은 Scores.compare (동률이면 앞 라운드 유지)
  blame: bank에 남은 r → priority(r) += 2
         방문 수가 "이 해의 경로 방문 수 중앙값" 미만인 경로의 r → priority(r) += 1
  조기 종료: priority가 직전 라운드와 완전히 같으면 이후 라운드도 같은 해 — 즉시 종료
반환 = best 라운드의 해
```

- multi-start가 아니다 — 난수가 없고, 라운드 간 입력(priority)이 결정적으로 이어진다.
  §1이 금지한 것은 난수 기반 다중 실행이다.

### H23 `zone-quota-balanced-fill` / H24 `zone-quota-subset-fill` — 존 배정 DP (공통) + 존 내부 적재 2종 (실물 맞춤, 2026-09-02)

> 실물 fixture 실측([survey §2.5](stage-04-initial-solution-heuristics-survey.md))이 보인 구조 —
> 부피 여유 4.2%, 허용 차급이 존과 1:1, 한 경로는 구체 구역 1종(Domain §3.4) — 에 맞춘 두 기법이다.
> 공통 부품은 **"존에 어떤 차량 유형을 몇 대 주는가"를 전역 DP로 정하는 것**이고, 그 뒤 존 내부를
> 채우는 축이 다르다: H23은 부피·정차 2차원 균형 greedy, H24는 부분집합 부피 합의 정확한 DP.
> H3(`vehicle-zone-fill`)는 둘 다 한 차량씩 greedy라 존이 조각나고 뒤 차량이 반만 찬다 — 실측 15 미배정.
> **H3 대비 새 정보 한 줄**: 존 전체의 차급 조합을 전역으로, 경로 하나의 적재를 부분집합 단위로 정한다.

**공통 — 존 배정 DP (`ZoneQuotaAllocation.allocate`, §3.4).**

```text
차량 유형 = (호환 Request 집합, maxWeight, maxVolume, effectiveMaxStopCount)이 같은 차량들.
  유형 순서 = (maxVolume ASC, maxWeight ASC, 첫 VehicleId ASC). 유형 안 차량 = VehicleId ASC.
  근무창·depot·속도는 유형에 넣지 않는다 — 근사 단계이고 그 차이는 오라클(§4.1)이 판정한다.
존 = anchor(r).zoneId 별 Request 목록 (부재 = "(none)"). 존 순서 = (호환 유형 수 ASC, Σvolume DESC, zoneId ASC).
  호환 차량 0대인 Request는 존에 넣지 않는다 — 어느 경로에도 못 들어가므로 처음부터 bank다 (X2).
존 수요 = 요청을 "호환 유형 집합"으로 다시 묶은 호환 그룹 g (|호환 유형 수| ASC, 유형 비트 ASC 정렬)의
  누적값: 접두 k마다 Σvolume_k · Σweight_k · Σ방문수_k(단일 1, PD 2) · 접두 유형 합집합 mask_k,
  그룹마다 최대 단품 volume·weight.
covers(s, z)  — s = 유형별 대수 벡터:
  ∀k: Σ_{t∈mask_k} s_t·maxVolume_t ≥ Σvolume_k ∧ Σ_{t∈mask_k} s_t·maxWeight_t ≥ Σweight_k
      ∧ Σ_{t∈mask_k} s_t·B_t ≥ Σ방문수_k            (B_t = effectiveMaxStopCount, 부재 = +∞)
  ∀g: s 안에 g와 호환이며 maxVolume ≥ 최대 단품 volume·maxWeight ≥ 최대 단품 weight인 유형이 있다
  ← 호환 그룹 누적 검사(Hall 조건). 이것이 없으면 큰 차가 큰 차 금지 존에 배정된다 (T38).
프론티어(남은 r, z) = 유형 순서로 재귀 나열(대수 0..r_t, covers가 되는 순간 그 유형에서 중단)한 s 중
  (a) covers(s) ∧ ¬covers(s − e_{min 유형}) — 최소 덮개
  (b) ¬covers(s) ∧ ∃t (r_t > s_t) ∧ ∀t (r_t = s_t ∨ covers(s + e_t)) — 한 대 모자란 최대 비덮개
      (덮을 수 없는 존은 (c)만 남는다 — 남은 차량을 전부 쏟아붓는 선택지를 만들지 않는다)
  (c) s = 0
DP: 상태 = 남은 유형별 대수 벡터. f(초기 = 전 대수) = (0, 0, 0).
  존 순서대로: 상태마다 프론티어 s를 나열해 값 + (부족, 낭비, Σs) 로 상태 r − s를 갱신.
    부족 = covers면 0, 아니면 max(1, Σvolume_z − Σ_t s_t·maxVolume_t) · 낭비 = covers면 Σ_t s_t·maxVolume_t − Σvolume_z, 아니면 0
  값 비교 = (Σ부족 ASC, Σ낭비 ASC, Σ사용 대수 ASC) 사전식. 동률 = 먼저 계산된 값 유지 (존 순서·상태 순회
  순서·프론티어 나열 순서가 전부 고정이라 결정적).
  존을 다 처리한 뒤 값 최소 상태(동률 = 남은 벡터 사전식 최소)에서 역추적 → 존별 (유형 → 대수)
  → 존별 차량 목록 = 유형 순서 × 유형 안 VehicleId ASC로 소비.
종료: 상태 수 ≤ Π(대수_t + 1) 고정, 전이 ≤ 상태 × 프론티어 — 루프 상한이 아니라 구조 (§4.3).
기권: Π(대수_t + 1) > 65,536 (재량 상수, §4.4·X20). 실물 fixture 25,920 · T25 합성 32.
```

- 한 존을 여러 차량이 나누는 것은 (유형 → 대수)로 표현되고, 한 차량이 여러 존을 맡는 것은 상태가
  대수를 차감하므로 불가능하다 — Domain §3.4의 비대칭이 DP 상태로 그대로 옮겨진다.
- 공급이 수요보다 작으면 존 단위로 덮거나(최소 덮개) 비운다(빈 집합) — 부분 배정은 존 내부 적재가
  남긴 요청과 함께 아래 leftover pass가 맡는다 (X21).
- fixture 실측(부피 수준): DP 배정이 Win 엔진 결과의 존→차급 조합과 사실상 일치(ZONE_19·21·18·23·29
  동일), 낭비 12.29 CBM = 여유 전부. 존 순서 greedy(제약 많은 존 우선·낭비 최소)는 같은 잣대에서
  32 미배정 — 전역 DP가 필요한 이유다 ([survey §2.5](stage-04-initial-solution-heuristics-survey.md)).

**H23 `zone-quota-balanced-fill` — 존 내부: 부호 있는 정차 예산 best-fit + 1-1 교환.**

```text
기권: Π(대수_t + 1) > 65,536
배정 = ZoneQuotaAllocation.allocate(problem)
for z in 존 순서:
  bins = z에 배정된 차량 (전부 빈 경로). 순서 = 배정 목록 순
  잔여 = z의 Request를 ( 호환 유형 수 ASC, totalVolume DESC, RequestId ASC ) 순으로
  for r in 잔여 순서:                                                            (규칙 a/b — 바깥 루프 ≤ n)
    평균 = 아직 처리하지 않은 z 요청(r 제외)의 totalVolume 평균 (없으면 0)
    후보 bin = InsertionSearch.candidatesFor(problem, profile, s, r, v)가 비어 있지 않은 bin
    bin마다 남을 부피 = (maxVolume − Σ실린 volume − volume(r)) − 평균 × (B_v − 정차 수 − 1)
             정차 한도 부재면 남을 부피 = maxVolume − Σ실린 volume − volume(r)   (= 부피 best-fit)
    선택 키 = ( 남을 부피 < 0 (정차가 먼저 바닥남) ASC, |남을 부피| ASC, 남은 부피 ASC, VehicleId ASC )
    선택 bin의 최선 후보에 삽입. 후보 bin 없으면 z의 leftover
  1-1 교환 (z의 leftover r, totalVolume DESC·RequestId 순):                       (성공마다 bank −1)
    z의 경로 route (VehicleId ASC)마다, route의 방문 q (totalVolume ASC, RequestId ASC) 중 volume(q) < volume(r):
      s′ = InsertionSearch.remove(problem, s, q) ; r을 s′의 route에 candidatesFor 최선 삽입 가능
      ∧ q를 s′의 z 다른 경로에 candidatesFor 최선 삽입 가능 → 둘 다 apply, r 완료, 다음 leftover
    첫 성공에서 멈춘다. 실패한 r은 leftover 그대로
leftover pass: 전 존의 leftover를 ( 호환 유형 수 ASC, totalVolume DESC, RequestId ASC ) 순으로
  InsertionSearch.candidates(problem, profile, s, r) 첫 원소(byCost)에 삽입 — 기존 경로든 미사용 차량의 새 경로든.
  후보 0개면 bank (규칙 b)
```

- 선택 키의 뜻: "남은 정차를 평균 크기 요청으로 다 채웠을 때 남을 부피". 음수면 정차가 먼저
  바닥나 부피가 낭비되므로 뒤로 미루고, 양수 중 가장 작은 bin이 가장 잘 맞는 bin이다. 정차 한도가
  지배하는 소량·다건 존(ZONE_24·29)에서 부피 best-fit이 큰 차의 정차를 소량 주문으로 다 써 버리는
  것을 막는다 (T40). 부동소수는 정렬 키로만 쓰고 동률은 VehicleId로 깬다 (§4.2).
- 요청 순서의 첫 키(호환 유형 수 ASC)가 필수다 — 부피순만이면 여유 0.12 CBM 존(ZONE_21)에서 13 미배정,
  호환 수 우선이면 2 (부피 수준 실측).
- 종료: 존 내부 ≤ n, 교환 ≤ n (성공마다 bank −1, 실패는 다음 leftover로), leftover pass ≤ n → 바깥 루프 ≤ 3n.
- **실물 fixture 정식 평가 실측 (2026-09-02, 스크래치 실행기)**: score [0, 31, 4,198,408, 1,002,069] · 507 ms —
  452건 전량 배정·31대, 경로별 감사(구역 1종·차급·부피·무게·정차·시간창·reqDate) 31/31 PASS.
  H3(15)보다 사전식으로 좋고 Win 엔진 결과(452 전량·31대)와 같은 축이다. 존 배정은 Win과 ZONE_19·21·23·29에서 동일.

**H24 `zone-quota-subset-fill` — 존 내부: seed pool + subset-sum DP + 오라클 피드백.**

```text
기권·배정: H23과 동일
for z in 존 순서:
  bins 순서 = ( z 요청 중 호환 수 ASC, maxVolume DESC, VehicleId ASC )         ← 까다로운 차부터
  미적재 = z의 Request
  for b (i번째 bin):
    후보 = 미적재 중 b 호환. 없으면 다음 bin
    seed = 후보 중 ( travel.timeSec(b.startDepot → anchor 지점, b 속도) DESC, RequestId ASC ) 첫
           (startDepot 부재면 endDepot 기준, 둘 다 없으면 RequestId ASC 첫)
    pool = 후보를 ( travel.timeSec(seed anchor → anchor, b 속도) ASC, RequestId ASC )로 정렬해
           Σvolume ≥ ⌈1.5 × maxVolume_b⌉ 이 되는 최소 접두, 단 ≥ B_b 개 (B_b 부재면 후보 전부)
    nmax = min(B_b, |pool|)                                                  (B_b 부재 = |pool|)
    nmin = min( nmax, max( |미적재| − Σ_{뒤 bin} B, ⌊0.8 × |미적재| × maxVolume_b / Σ_{i 이후 bin} maxVolume⌋ ) )
    제외 = ∅
    반복 (bin당 ≤ |pool| 회 — 매 반복이 제외를 늘리거나 nmax를 줄인다):
      S = subsetSum(pool − 제외, maxVolume_b, maxWeight_b, nmin, nmax)
      S = ∅ → bin 종료
      순서화: S를 ( totalVolume DESC, RequestId ASC ) 순으로 candidatesFor(b) 최선 위치에 삽입.
              실패 집합 F (그 요청은 넣지 않고 계속)
      F = ∅ → bin 확정(삽입된 해 채택), 미적재 −= S, 다음 bin
      F ≠ ∅ → 이 반복의 삽입을 버리고(해는 불변 — 그냥 base로 돌아간다) 제외 ∪= F, nmax = |S| − |F|
              nmax < max(1, nmin) 이면 nmin = 0. nmax = 0 → bin 종료
  z의 bin이 끝나면 남은 미적재를 z의 경로에 (bins 순서, candidatesFor 첫 성공) 삽입. 남으면 leftover
leftover pass: H23과 동일

subsetSum(items, cap, wcap, nmin, nmax):
  u = ⌈cap / 20,000⌉ (재량 상수) · vol_i = ⌈totalVolume_i / u⌉ (올림 — 절대 넘치지 않는다) · C = ⌊cap / u⌋
  c_i = 방문 수 (단일 1, PD 2)
  상태 f[v][n] = 부피 합 v·방문 수 합 n인 부분집합의 최소 weight (wcap 초과는 버린다). f[0][0] = 0
  items를 RequestId ASC로 하나씩, v = C..vol_i, n = nmax..c_i 내림차순 갱신 — 0/1 knapsack.
    갱신됐으면 taken[i][v][n] = true (BitSet)
  정차 한도 부재면 n 축이 없다 (f[v], nmin·nmax 무시)
  선택 = ( v DESC, n ASC ) 첫 f[v][n] < ∞ 이며 nmin ≤ n ≤ nmax. 없으면 nmin = 0으로 다시 선택
  역추적: i = 마지막..첫, taken[i][v][n]이면 포함하고 v −= vol_i, n −= c_i
```

- pool을 이동표 시간으로 자르는 이유: 존 폭 70 km인 ZONE_29에서 부피만 보는 subset-sum은 흩어진
  소량 주문 28건을 한 경로에 몰아 시간창을 10건 어긴다 — 좌표가 아니라 `TravelMatrix` 시간이다
  (Domain §4). 정차 하한이 없으면 큰 bin이 큰 주문만 먹고 마지막 bin이 정차 한도에 걸린다 (T41·T42).
- 오라클 피드백이 이 기법의 유일한 되돌림이다. 상한은 구조: 반복마다 제외 집합이 ≥1 늘거나
  nmax가 ≥1 준다 → bin당 ≤ |pool| 회 (T43). 경로 간 relocate·교환은 없다.
- 비용: subsetSum 1회 = O(|pool| · C · B) 셀. fixture 최대 87 × 17,640 × 29 ≈ 44M(T5 bin, pool은
  α로 잘려 실제 ≈ 40건). 메모리 = f 4 MB + taken 5.5 MB. T25 합성(용량 10⁶)은 u = 50이라 C = 20,000.
- **실물 fixture 정식 평가 실측 (2026-09-02)**: score [8, 31, 3,942,905, 974,992] · 773 ms — H3(15)보다 좋지만
  H23(0)에 진다. 남은 8건은 원거리 분산 존 몫이고, 거리 축은 H23보다 6% 짧다. 어느 쪽이 남을지는 Stage 8.

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
  `initialScore`로 그대로 흐른다 — DoD "초기해 대비 개선"의 기준값이 **24개 중 최선**이 된다.
  기준이 올라가므로 그 DoD는 더 엄격해진다.

---

## 7. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| X1 | requests 빈 목록 | 24개 전부 즉시 빈 해 반환. best = 빈 해 | Domain §9.1 |
| X2 | vehicles 빈 목록 / 전 Request 호환 0대 | 전부 bank인 해. 예외 아님 | stage-04 E2 |
| X3 | 전원 기권 | 빈 해 반환 (§6-3). **기본 24개로는 일어나지 않는다** — 기권 가능한 것은 H5·H7·H8·H19·H20·H23·H24뿐이다 (§4.4). 기법 목록을 직접 넘기는 오버로드에서만 도달한다 | §4.4 |
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
| X14 | profile hard가 강해 어떤 삽입도 통과 못 함 | 전부 bank인 해가 24개 나온다. Feasible이므로 정상 | §4.1 |
| X15 | H12 한 라운드에 수락 0건 | 전원 후보 0개였다는 뜻 — 남은 것 전부 bank, 정상 종료 | §5 H12 |
| X16 | H18/H19 DP에서 어떤 경로 전이에도 못 드는 요청 | 전이 ②(bank)가 항상 있으므로 DP는 완주한다. 사전식 1축이 그 비용을 진다 | §5 H18 |
| X17 | H21에서 병합 가능한 쌍이 0 | 요청당 클러스터 1개로 퇴화 — 단독 경로 위주 해. 유효 | §5 H21 |
| X18 | H22에서 priority가 라운드 간 불변 | 불변점 — 이후 라운드도 같은 해이므로 조기 종료. R회를 다 돌지 않는 것은 정상 | §5 H22 |
| X19 | H14에서 남은 호환 차량이 없는 요청 | `noAlt` 축이 최우선으로 끌어올린다 — big-M을 쓰지 않는다 (H1 onlyCandidate와 동일 수법) | §5 H14 |
| X20 | H23·H24에서 차량 유형 조합 수 Π(대수+1) > 65,536 | 기권 — DP 상태 수를 구조로 묶는 한계. 예외 아님 | §4.4 |
| X21 | H23·H24 존 배정 DP에서 어떤 존도 못 덮음 (공급 < 수요) | 프론티어에 빈 집합(c)이 항상 있어 DP는 완주한다. 존 단위로 덮거나 비우고, 남은 요청은 leftover pass가 기존 경로·미사용 차량에 삽입한다 | §5 H23 |
| X22 | H24 순서화(오라클)가 subset-sum이 고른 요청을 못 넣음 | 그 요청을 bin의 제외 집합에 넣고 nmax를 줄여 재DP. 반복은 bin당 ≤ \|pool\| — 조용히 자르지 않고 구조로 끝난다 | §5 H24 |
| X23 | H23·H24에서 정차 한도 부재 | H23 예산 = 남은 부피(부피 best-fit으로 퇴화) · H24 subsetSum은 방문 수 축 없이 부피만 (nmin·nmax 무시). 기권하지 않는다 | §5 H23·H24 |

---

## 8. 테스트 목록

위치: `solver-core/src/test/java/com/ronext/rpdptw/solve/`. 의존은 JUnit만
(Stage 0 §4.2 — property 라이브러리를 추가하지 않는다). `Problem`은 Stage 1·2 경로로
손 조립한다 (fixture JSON 파싱은 Stage 6). 예외는 T44 하나 — 실물 fixture는 Jackson이 있는 app 모듈
(`app/src/test/java/com/ronext/rpdptw/app/`)에서 테스트 전용 매핑으로 읽는다.
번호는 stage-04 §7의 T1–T12에 **이어서** 붙인다.

| # | 테스트 | 내용 | 대응 |
|---|---|---|---|
| T13b | `InsertionSearchTest.cachedCandidatesMatchFullRecomputation` | `InsertionSearch.Cache`의 상위 3개·차량별 최선이 삽입을 거듭한 여러 상태에서 전체 재계산과 일치 (§4.1 대조 테스트) | §4.1 |
| T13 | `InsertionSearchTest.candidateValidationIncludesProfileHard` | 테스트 전용 hard 제약("경로당 방문 1개 초과 금지") profile에서 2개째 삽입 후보가 **후보 단계에서** 탈락 | §4.1 ② |
| T14 | `InitialSolutionBuilderTest.selectsBestByOfficialEvaluation` | 결과가 다른 기법 2개 이상에서 `Scores.compare` 최소인 해가 선택됨 · 동률이면 우선순위 앞선 기법 (X7) | §6-4 |
| T15 | `InitialSolutionBuilderTest.abstainedHeuristicsAreSkipped` | PD 포함(다중 depot) 문제 → H7·H8(·H19·H20)이 `ABSTAINED`, 나머지는 `BUILT`, 결과 유효 | §4.4 |
| T16 | `InitialSolutionBuilderTest.allAbstainedYieldsEmptySolution` | **기권 기법만 담은 목록**을 `build(problem, profile, heuristics)` 오버로드에 넘김 → 빈 해, `heuristicId == "empty"`, 예외 없음 (X3) | §6-3 |
| T17 | `InitialSolutionBuilderTest.everyConstructionResultIsFeasible` | 24개 각각을 직접 `Evaluator.evaluate` → 전부 Feasible. profile hard 있는 문제에서도 (X5·X14) | D7 |
| T18 | `InitialSolutionBuilderTest.deterministicAcrossRuns` | 같은 `Problem`·`Profile`로 두 번 → best 해·`heuristicId` 동등, `score`는 `Arrays.equals` (X8) | §1 |
| T19 | `ConstructionHeuristicsTest.outerLoopBoundedByRequestCount` | 24개 각각 바깥 루프 반복 수 ≤ 기법별 상한(§4.3·§5 표) · 방어 카운터 초과 시 예외 (X13) | §4.3 |
| T20 | `ConstructionHeuristicsTest.structureHoldsForEveryHeuristic` | 24개 결과 전부 `StructureCheck` 위반 0 · pair 원자성 · XOR. PD 포함 문제로도 수행 | Domain §6.3 |
| T21 | `SavingsMergeConstructionTest.usesDirectedMatrixOnly` | `d(i,j) ≠ d(j,i)`인 이동표에서 saving이 방향을 구분함 · 좌표로 거리를 재계산하지 않음 | Domain §4 |
| T22 | `SweepNextFitConstructionTest.coordinatesOrderOnly` | 좌표를 바꿔 각도 순서만 바꾸고 이동표는 고정 → 방문 **순서**는 바뀌되 보고된 거리는 표 값과 일치 | Domain §4 |
| T23 | `VehicleZoneFillConstructionTest.commitsOnlyBestZonePerVehicle` | 차량 1대·존 2개에서 적재율 규칙대로 한 존만 커밋 · what-if가 다른 존을 오염시키지 않음 | §5 H3 |
| T24 | `InitialSolutionBuilderTest.recordsPerHeuristicOutcome` | `outcomes`가 우선순위 순이고 기법마다 상태·미배정 수·score·소요를 담음 | §9 (8→4 입력) |
| T25 | `InitialSolutionScaleTest.runsPortfolioOnFullScaleSyntheticProblem` | **규모 측정.** Stage 2 T12·stage-04 T11과 **같은 합성 문제**(장소 453·주문 452·차량 31·이동표 453² 전 쌍)에 **정차 한도 28**(실물 fixture `VehicleMaxStopCount`)을 더해 포트폴리오 1회 → 정상 종료. 한도가 없으면 경로 하나가 전 주문을 삼켜(2026-09-02 실측 H1 724초·routes 1) 실물과 무관한 값이 된다. **기법별(24개) 소요·미배정 수·score와, 포트폴리오 총 소요를 `AlnsConfig.timeLimitSec`으로 나눈 비(比)를 출력해 기록한다** — 절대 초는 그 자체로 해석되지 않는다. 이 비가 1에 가까우면 초기해가 ALNS 예산만큼 시간을 쓰고 있다는 뜻이고, 그것이 24 → 4 축소의 판단 근거가 된다 ([survey §5](stage-04-initial-solution-heuristics-survey.md)). **한도가 아니다** — 넘겨도 중단하지 않는다 (§4.3). 품질은 판정하지 않는다 | Plan Stage 4 규모 DoD |
| T26 | `InsertionSearchTest.standaloneDistUsesDirectedMatrix` | `d(i,j) ≠ d(j,i)`인 이동표에서 `standaloneDistMeter`가 방향별 값을 그대로 합산 · 좌표 미사용 | Domain §4 |
| T27 | `InsertionSearchTest.candidateReportsForwardSlackDelta` | `deltaForwardSlackSec`이 "삽입 전후를 각각 전파해 기존 방문 slack 합을 재계산한 값"과 일치 · 새 방문은 불포함 | §4.1 |
| T28 | `FeasibleRoutesRegretMConstructionTest.dynamicFeasibleRouteCountLeads` | 정적 호환 수는 같지만 경로가 차서 동적 가능 경로 수가 다른 두 요청에서 적은 쪽이 먼저 삽입됨 | §5 H9 |
| T29 | `ChainSeedRegretMConstructionTest.chainExclusionDropsLinkableSeeds` | 시간상 이어 붙일 수 있는 요청이 seed에서 빠지고, 이어 붙일 수 없는 요청 수만큼 경로가 열림 | §5 H10 |
| T30 | `RouteBiddingConstructionTest.eachRoundEachRouteAcceptsAtMostOne` | 같은 차량에 입찰이 몰려도 라운드당 1건만 수락 · 라운드 수 ≤ `\|requests\|` (X15) | §5 H12 |
| T31 | `VehicleFillRemainingRegretConstructionTest.regretUsesOnlyRemainingVehicles` | 차량 순서상 지나간 차량은 regret 계산에서 제외 · `noAlt` 요청이 최우선 (X19) | §5 H14 |
| T32 | `WeakestFitDecreasingConstructionTest.strongVehicleReservedForHardRequest` | 약한 차로 충분한 요청이 강한 차를 쓰지 않아, 나중의 어려운 요청이 강한 차에 들어감 | §5 H15 |
| T33 | `ConstrainedPathExtensionConstructionTest.pairAppendedAtomically` | PD 요청이 `[…, p, d]`로 통째로 붙음 — pickup만 붙는 상태가 존재하지 않음 | §5 H16 |
| T34 | `GiantTourSplitTest.splitNeverWorseThanNextFitOnSameTour` | 같은 순열·같은 차량 순서에서 Split 결과의 (bank, 차량 수, 거리) 사전식 키 ≤ next-fit 분할 (X16) | §5 H18 |
| T35 | `GapSweepBidirectionalConstructionTest.coordinatesOrderOnlyAndDeterministicDirection` | 좌표 변경이 순서만 바꾸고 거리는 표 값과 일치(T22와 동형) · 양방향 동률이면 시계 방향 | §5 H20 |
| T36 | `SpatiotemporalClusterConstructionTest.distanceUsesDirectedTravelOnly` | `S(i,j)`가 두 방향을 각각 계산해 최소를 취함 · `f < 0`인 방향 배제 · 좌표로 거리를 재계산하지 않음 (X17) | §5 H21 |
| T37 | `SqueakyWheelSequentialConstructionTest.fixedRoundsDeterministicBlame` | 같은 입력 두 번 → 같은 라운드 전개·같은 best · priority 불변 시 조기 종료 (X18) | §5 H22 |
| T38 | `ZoneQuotaAllocationTest.hallConditionKeepsBigVehiclesOffRestrictedZones` | 큰 차 1대·작은 차 2대, 큰 차 금지 존 A(작은 차 2대 몫)와 자유 존 B에서 DP가 A에 작은 차 2대·B에 큰 차를 배정. 낭비만 보면 큰 차가 A로 가는 수치의 입력 | §5 H23 공통 |
| T39 | `ZoneQuotaAllocationTest.combinationsBoundAbstains` | 유형이 서로 다른 차량 17대(2¹⁷ > 65,536)면 H23·H24 `abstains` true · 같은 유형 31대(32)면 false (X20) | §4.4 |
| T40 | `ZoneQuotaBalancedFillConstructionTest.signedBudgetAvoidsWastingStops` | 차량 (부피 20·정차 4)·(4·4), 요청 4×4건 + 1×4건: 부피 best-fit은 3건을 놓치지만 부호 예산 규칙은 전량 배정 · 동률은 VehicleId | §5 H23 |
| T41 | `ZoneQuotaSubsetFillConstructionTest.subsetSumFillsExactly` | 용량 10 bin 2개·요청 5,4,4,3,2,2: 부피 best-fit은 1건을 놓치고 subsetSum은 두 bin을 정확히 채움 · 부피 올림 양자화가 용량을 넘기지 않음 | §5 H24 |
| T42 | `ZoneQuotaSubsetFillConstructionTest.countLowerBoundSpreadsSmallRequests` | 용량 8·정차 3 bin 2개·요청 4,4,3,3,1,1: 하한 없이는 첫 bin이 {4,4}를 먹어 둘째 bin이 정차 한도에 걸리고, 하한(6 − 3 = 3)이 있으면 전량 배정 | §5 H24 |
| T43 | `ZoneQuotaSubsetFillConstructionTest.oracleFeedbackExcludesInfeasibleAndTerminates` | 시간창 때문에 함께 못 도는 요청이 순서화에서 실패 → 제외·nmax 축소 후 재DP가 \|pool\| 안에 끝나고 그 요청은 bank · 결과 StructureCheck 통과·Feasible (X22) | §5 H24 |
| T44 | `WinPocFixtureTest.zoneQuotaBalancedFillAssignsEveryRequestOnRealFixture` (**app 모듈**) | `data/win_poc_case_floor.json`(주문 452·차량 31·정차 28)을 테스트 전용 매핑으로 `Problem`까지 올려 H23 실행 → bank 0·31경로·`Evaluator` Feasible·score[0]=0·재실행 동일 score(X8)·H3보다 `Scores.compare` 우위 · 경로마다 구역 1종·차급·부피·무게·정차 28·시간창·reqDate 감사 | §5 H23 실측 |

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
| 24 → 4 축소 | Stage 8 실측 후 **사용자 결정** | §5 표의 실측값이 입력 ([survey §5](stage-04-initial-solution-heuristics-survey.md) 프로토콜) |
| 보류 후보 구현 (Ioannou impact·2-petal·backhaul 매칭·다중 depot 배정·VROOM λ 격자) | 트리거 발동 시 survey 개정 후 | [survey §4](stage-04-initial-solution-heuristics-survey.md) |
| route elimination 계열 (Nagata–Bräysy ejection pool) | `4-ALNS` 연산자 후보 | 사용자 확정 D4 · survey §4 F5 |
| 증분 평가·삽입 shortlist | 재량 (필요 시) | stage-04 N4 |
| 전체 응답 시간(초기해+ALNS+재검증) 타임박스 | Stage 6 executor | stage-04 §9 Q1 |
| 탐색 파라미터(0.85 적재율·4분위 등급 등) 최종값 | Stage 8 | Domain §9.3 |

---

## 10. 미해결 질문

닫힌 질문은 해소 표시를 달아 남긴다 ([README](README.md) 공통 규칙).

| # | 질문 | 처리 |
|---|---|---|
| Q1 | 24개 중 어느 4개를 남길 것인가 | **미결 — 의도된 미결.** Stage 8이 T25로 기법별 소요·품질을 실측하고, 그 표와 [survey §5](stage-04-initial-solution-heuristics-survey.md) 프로토콜을 보고 사용자가 정한다. 그때까지 24개 전부 실행한다 |
| Q2 | 병렬 실행 시 스레드 총량 | **미결.** Architecture §3.2의 executor 동시 실행 수(기본 1~2)와 곱해지므로 함께 정해야 한다. 스레드풀을 core가 소유할지 app이 주입할지도 그때 결정 ([Stage Extra E4](stage-extra-deferred-features.md)) |
| Q3 | 초기해 실행 시간의 상한 | **해소 (2026-09-02).** 두지 않는다 — 문제 규모에 따라 적정값이 달라져 고정 상한이 오히려 해를 버린다. 종료는 §4.3의 구조적 보장이 담당하고, 시간 컷오프는 결정성(X8)을 깨므로 채택하지 않는다 |
| Q4 | construction이 profile hard를 볼 것인가 | **해소 (2026-09-02).** 본다 (§4.1 ②). stage-04 N8이 "Stage 8에서 필요가 확인되면"으로 유예했던 항목인데, 포트폴리오 선택이 정식 평가로 이뤄지는 이상 Infeasible 후보를 만들어 놓고 버리는 것이 낭비라 지금 당긴다 |
