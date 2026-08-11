---
title: Stage 4 — 초기해와 ALNS (상세 구현 설계)
stage: 4
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§9 ALNS, §6 Solution·XOR·trial, §8.3 비교, §12 오류 분류)
  - ../architecture-design.md (§2 모듈·패키지, §3.2 시간 한도)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준, §4.2 test 의존 고정)
  - stage-01-canonical-input-normalization.md (§2.2 DeliveryPolicy — 탐색 예산은 여기 없음)
  - stage-02-travel-and-problem-freeze.md (§2.3 Problem.compatibleVehicles)
  - stage-03-solution-propagation-evaluation.md (§2 Solution·StructureCheck, §3 RoutePropagator, §4 Evaluator·비교 규약)
revisions:
  - 2026-08-10 최초 작성
  - 2026-08-10 탐색 예산을 `AlnsConfig`가 소유 (Domain §2.5.1) — idle 종료 조건 추가,
    profile 인자화, 비교를 `Scores.compare`로
  - 2026-08-10 §7에 규모 테스트 T11 추가 (Plan Stage 4 규모 DoD 대응 — Stage 2 T12와 같은
    합성 문제로 ALNS 1회, 시간 한도 안의 반복 수 기록). 다른 설계 무변경
  - 2026-08-10 D4 확정 반영 — §4.1 삽입 순서의 정렬 키를 "delivery의 **마지막** 창 close"로
    명시 (시간창이 `List<TimeWindow>`가 됨, Domain §3.2). 창 1개 입력에서는 종전과 같은 값이라
    연산자·acceptance·테스트는 무변경
  - 2026-08-11 정리 — §7 말미 문구를 Plan §1 DoD 편입으로 갱신. 설계 무변경
---

# Stage 4 — 초기해와 ALNS

solver-core의 `solve` 패키지에 초기해 생성과 ALNS 탐색(destroy/repair·acceptance·종료)을
만든다. 주 근거: [Domain §9](../domain-design.md). Stage 3이 확정한
`Solution`·`Route`·`StructureCheck`·`RoutePropagator`·`Evaluator`·`EvaluationResult`·
`Evaluation`·비교 규약(`Scores.compare < 0 ⟺ 더 좋음`)과 Stage 2·3의
`Problem`(`freeze(Plan)`·`compatibleVehicles()`·`nodeRef()`)을 그대로 잇는다 —
같은 개념에 새 이름을 짓지 않는다.

**탐색 예산은 `AlnsConfig`가 전부 소유한다** (Domain §2.5.1). `Problem`에는 시간·step·idle
한도가 없다 — 그래서 재검증(Stage 5)이 이 값들에 도달할 수 없고, `verify ↛ solve` ArchUnit
규칙이 그것을 강제한다 (Architecture §2.1).

**DoD** ([Plan Stage 4](../implementation-plan.md)): 소형 fixture에서 초기해 대비 개선 확인 ·
pair·XOR 불변식이 탐색 중 유지되는 property 테스트 (예: 랜덤 스텝 N회 후 구조 검사).

핵심 구도 — Domain §9.1의 한 스텝을 Stage 3 타입 위에 그대로 올린다:

```text
[Problem (동결)]  ──InitialSolutionBuilder──▶  [initial Solution]  = current = best
                                                      │
      ┌────────── 반복 (시간·step·idle 한도까지, Domain §12) ─────────────┐
      │ current ─destroy(pair 단위)─▶ draft ─repair(pair 삽입)─▶ draft'  │
      │ draft' ─StructureCheck─▶ 위반 있으면 예외 (구조 결함 = 버그)        │
      │        ─Evaluator(정식 평가)─▶ Infeasible이면 폐기                 │
      │        ─acceptance(Scores.compare)─▶ current/best 갱신            │
      └──────────────────────────────────────────────────────────────────┘
                                                      ▼
              [AlnsResult: best + Evaluation(③) + score(④) + RouteFacts]  → Stage 5 재검증
```

---

## 1. 문서가 고정하는 것 / 구현·실험 재량 (Domain §9.3)

이 경계를 문서 전체에서 유지한다. 아래 "고정"을 어기면 결함이고, "재량"은 기본값 제안일 뿐이다.

| 구분 | 내용 | 근거 |
|---|---|---|
| **고정** | destroy/repair는 **pair 단위** — `RequestId`로만 넣고 뺀다. pickup만 빼는 연산은 없다 | §1.4·§9.1 MUST |
| **고정** | 뺀 Request는 bank에 정확히 한 번. 매 trial 후 XOR 성립 | §6.3·§9.1 |
| **고정** | 순서: 구조 검사 → 정식 평가 → acceptance. **수락·최종 비교의 권위는 정식 평가(`Evaluator`)뿐** — 근사·shortlist 점수로 수락 금지 | §6.5·§9.2 MUST NOT |
| **고정** | trial은 복사본(draft)에서. `current`/`best`는 별개 확정 해 — 별칭 공유 금지 | §6.5 |
| **고정** | 새 경로 시작은 실제 미사용 `VehicleId` 소비 (가짜 차량 카운트 금지) | §9.1 |
| **고정** | repair가 일부만 넣거나 0건 넣어도 정상 시도. 전부 bank인 해도 유효 | §9.1 |
| **고정** | 종료 조건 도달(시간·step·idle) = 정상 종료 — 그 시점 best를 반환하고 재검증으로 넘긴다 | §12·Master §2 |
| **고정** | 탐색은 `Problem`·이동표·profile을 수정하지 않는다 (읽기 전용) | §5 MUST |
| **고정** | 탐색 예산은 `AlnsConfig`에만 있다. `Problem`에서 예산을 읽는 코드 금지 | §2.5.1 MUST NOT |
| 재량 | 초기해 휴리스틱·개수(본 문서: 결정적 greedy 1개) | §9.3 |
| 재량 | 연산자 목록(본 문서: destroy 2 + repair 2)·q 범위·적응 가중치 | §9.3 |
| 재량 | acceptance 세부(동점·worse 수락 확률)·예산 기본값·시드 정책 | §9.3 |
| 재량 | 삽입 후보 shortlist·증분 계산 (도입 시 노트 N4의 대조 테스트 필수) | §9.2·§6.4 |

---

## 2. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/solve/` (Architecture §2 — "solve:
Solution·전파·평가·ALNS", Stage 0 §3.1). 하위 패키지를 만들지 않는다.

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `solve/AlnsSolver.java` | ALNS 본체: 반복 루프·acceptance·종료·best 관리 | Domain §9 |
| `solve/AlnsConfig.java` | **탐색 예산**(시간·step·idle 한도·seed) + 알고리즘 튜닝 (§3.3) | Domain §2.5.1·§9.3 |
| `solve/AlnsResult.java` | 탐색 산출: best + `Evaluation`(③) + `long[] score`(④) + `RouteFacts` + 통계 | Domain §9·§10.2 |
| `solve/AlnsRunStats.java` | 간단한 실행 통계 (반복·수락·경과·종료 사유) — 추적 장치 아님 | Master §3-⑪ |
| `solve/InitialSolutionBuilder.java` | 결정적 greedy 초기해 1개 생성 | Domain §9.3 재량 |
| `solve/DestroyOperator.java` | destroy SPI: pair 단위로 빼서 bank로 | Domain §9.1 |
| `solve/RepairOperator.java` | repair SPI: bank의 Request를 pair 삽입 | Domain §9.1 |
| `solve/RandomRemoval.java` | 배정된 Request 중 무작위 q개 제거 | 재량 기본 연산자 |
| `solve/RouteRemoval.java` | 무작위 경로 하나를 통째로 제거 (차량 축 탐색용) | 재량 기본 연산자 |
| `solve/GreedyInsertion.java` | 후보 중 최소 비용 위치에 순차 삽입 | 재량 기본 연산자 |
| `solve/RegretInsertion.java` | regret-2: 차선과의 격차가 큰 Request부터 삽입 | 재량 기본 연산자 |
| `solve/AdaptiveWeights.java` | 연산자 룰렛 선택 + segment 가중치 갱신 | 재량 (ALNS 적응층) |

Stage 3 산출물(`Solution`·`StructureCheck`·`Evaluator` 등)과 `Problem`·`Profile`·`Scores`는
수정하지 않는다. 테스트 파일은 §7.

---

## 3. 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.
난수는 JDK `java.util.random.RandomGenerator`만 쓴다 (solver-core 외부 의존 0, Architecture §2.1).

### 3.1 연산자 SPI (Domain §9.1)

```java
public interface DestroyOperator {
    String id();
    /**
     * current에서 Request들을 pair 단위로 빼 bank에 넣은 새 Solution을 반환한다.
     * removeCount는 목표치(힌트)다 — 연산자는 pair 단위를 지키는 한 덜 뺄 수 있다.
     * 반환 해는 구조 검사를 통과해야 한다 (빈 visits 경로 금지 → 경로째 제거, §6 E4).
     */
    Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng);
}

public interface RepairOperator {
    String id();
    /**
     * bank의 Request들을 (이전부터 bank였던 것 포함) 가능한 만큼 pair 삽입한 새 Solution.
     * 0건 삽입도 정상 반환이다 (Domain §9.1). 반환 해는 구조 검사를 통과해야 한다.
     */
    Solution repair(Problem problem, Solution destroyed, RandomGenerator rng);
}
```

- 두 SPI 모두 `Solution`(불변 record)을 받아 **새 인스턴스**를 만든다 — draft 복사 규칙
  (Domain §6.5)이 타입으로 지켜진다. 확정 해를 제자리에서 고칠 방법 자체가 없다.

### 3.2 solver·결과

```java
public final class AlnsSolver {
    public AlnsSolver(AlnsConfig config,
                      List<DestroyOperator> destroyOperators,
                      List<RepairOperator> repairOperators);   // 각각 1개 이상
    public static AlnsSolver withDefaults(AlnsConfig config);  // §2의 기본 연산자 4개

    /** 초기해 → ALNS 반복 → best 반환. Problem·profile은 읽기만 한다 (Domain §5).
        profile은 인자다 — Problem에 담기지 않으며, 호출자가 재검증에도 같은 인스턴스를
        넘긴다 (Domain §8.4 MUST). */
    public AlnsResult solve(Problem problem, Profile profile);
}

public record AlnsResult(
    Solution best,
    Evaluation bestEvaluation,               // 층 ③ — Stage 5 재검증의 대조 대상 (§10.2)
    long[] bestScore,                        // 층 ④ — 〃 (Arrays.equals로 대조)
    Map<VehicleId, RouteFacts> bestRouteFacts,
    Evaluation initialEvaluation,            // DoD "초기해 대비 개선"의 기준값
    long[] initialScore,                     // 〃 (개선 판정은 score로 — Scores.compare)
    AlnsRunStats stats) {}

public record AlnsRunStats(
    long iterations, long accepted, long infeasibleDiscarded,
    long bestImproved, long elapsedMillis,
    Termination termination) {}                 // 어느 조건으로 멈췄는지 (§3.3)

public enum Termination { TIME_LIMIT, MAX_STEPS, IDLE_STEPS, IDLE_TIME }
```

- `bestEvaluation`·`bestScore`·`bestRouteFacts`는 마지막에 새로 계산한 값이 아니라 **best
  승격 시점의 정식 평가 결과**를 그대로 보관한 것이다. 재검증(Stage 5)이 "탐색이 보고한
  값"(§10.2)으로 이 둘을 대조한다 — 캐시 ≠ 재계산이면 버그 (§6.4, 테스트 T4).
- `AlnsRunStats`는 로그·실험용 카운터다. 결과 JSON의 run 메타(Domain §11.1)에 넣을지는
  Stage 5·6 결정이다 (§9 Q2). fingerprint·lineage류 추적 장치는 만들지 않는다 (Master §3-⑪).

### 3.3 설정 (전부 재량 — Domain §9.3)

```java
public record AlnsConfig(
    // ---- 탐색 예산 (Domain §2.5.1 — Problem에 담기지 않는 값들) ----
    long timeLimitSec,                // 최대 실행 시간. 호출자가 이미 결정해 넘긴다 (아래)
    OptionalLong maxSteps,            // 최대 실행 step 수. 부재 = 시간 한도만
    OptionalLong idleSteps,           // best 미개선이 이만큼 연속되면 종료. 부재 = 미적용
    OptionalLong idleSec,             // best 미개선이 이만큼 지속되면 종료. 부재 = 미적용
    long seed,                        // 시드 정책은 호출자(Stage 6 executor) 몫 — core는 받기만
    // ---- 알고리즘 튜닝 (전부 재량) ----
    double minDestroyFraction,        // q 하한 비율 (기본 0.1)
    double maxDestroyFraction,        // q 상한 비율 (기본 0.3)
    int maxDestroyCount,              // q 절대 상한 (기본 60)
    double worseAcceptStartProbability, // 기본 0.05 — 경과 시간에 비례해 0으로 선형 감소
    int weightSegmentLength,          // 적응 가중치 segment (기본 100 반복)
    double weightDecay,               // ρ (기본 0.5)
    int rewardNewBest, int rewardImproved, int rewardAcceptedWorse) {  // σ₁·σ₂·σ₃ (기본 5·2·1)

    public static AlnsConfig defaults(long seed, long timeLimitSec);
}
```

**시간 한도의 출처는 core가 모른다.** `timeLimitSec`은 이미 결정된 값으로 들어온다 —
호출자(Stage 6 executor)가 `wire Termination.secondsSpentLimit ▷ application.yml 기본값`
체인을 적용한 결과다. `Problem`을 뒤져 예산을 찾는 코드는 존재하지 않는다 (Domain §2.5.1 MUST NOT).

**종료 조건 4개** — 하나라도 걸리면 종료하고 `AlnsRunStats.termination`에 어느 것인지 남긴다:

| 조건 | 의미 | 부재 시 |
|---|---|---|
| `timeLimitSec` | 경과 시간 초과 | **항상 존재** (필수) |
| `maxSteps` | 반복 횟수 초과 | 미적용 |
| `idleSteps` | best가 이 횟수만큼 연속 미개선 (정체) | 미적용 |
| `idleSec` | best가 이 시간만큼 미개선 (정체) | 미적용 |

- idle 두 축은 **best 갱신 시점**을 기준으로 리셋한다 (수락 시점이 아니다 — worse 수락이
  정체 카운터를 되돌리면 종료가 무한정 밀린다).
- 네 조건 모두 "언제 멈출지"일 뿐 **유효한 답이 무엇인지를 바꾸지 않는다.** 값이 달라지면
  결과가 달라질 수 있지만 무효가 되지는 않으며, 재검증은 이 값들을 보지 않는다 (Domain §10.2).
- 규약(wire)에 있는 것은 시간 한도뿐이다. 나머지 셋은 `application.yml`에서만 온다 (Stage 6).

### 3.4 초기해·적응층

```java
public final class InitialSolutionBuilder {
    /**
     * 결정적 greedy 1개: 빈 해(전 Request bank)에서 GreedyInsertion을 결정적 순서로 적용.
     * 같은 Problem이면 같은 초기해. 삽입 못 한 Request는 bank에 남는다 (유효한 해, §9.1).
     */
    public static Solution build(Problem problem);
}

public final class AdaptiveWeights {
    public AdaptiveWeights(int operatorCount, double decay, int segmentLength,
                           int rewardNewBest, int rewardImproved, int rewardAcceptedWorse);
    public int select(RandomGenerator rng);                  // 가중치 비례 룰렛
    public void reward(int operatorIndex, Outcome outcome);  // enum NEW_BEST/IMPROVED/ACCEPTED_WORSE
    public void endIteration();                              // segment 경계에서 w ← (1−ρ)w + ρ·score
}
```

---

## 4. 절차

### 4.1 초기해 생성 (`InitialSolutionBuilder.build`) — 재량 기본

```text
1. 시작해 = Solution(routes = [], bank = 모든 RequestId).
2. 삽입 순서: delivery의 **마지막 창 close** 오름차순, 동률은 RequestId 문자열 순
   (결정적 — rng 없음. 시간창이 목록이 됐으므로(Domain §3.2) 어느 close인지 정한다 —
    "가장 늦게까지 받아 주는 시각"이 급한 정도를 나타내고, 창이 하나면 종전 값과 같다).
3. 각 Request를 §4.3의 후보 탐색으로 최소 비용 위치에 삽입. 후보 0개면 bank에 남긴다.
4. 반환. (이전 설계의 "초기해 ≤8개" 구조는 폐기 — 초기해는 1개다, Domain §9.3.)
```

- 호환 차량 0대·시간창 불가능 Request가 bank에 남아도 정상이다 — 사유 기록은 Stage 5의 일
  (bank는 ID만, §6.3 MUST NOT).

### 4.2 ALNS 루프 (`AlnsSolver.solve`)

```text
1. 준비    rng = config.seed 기반 RandomGenerator.
          deadline = now + config.timeLimitSec.        // 예산은 이미 결정돼 들어온다 (§3.3)
          lastBestStep = 0, lastBestAt = now.          // idle 카운터 기준점
          weights = destroy·repair 각각 AdaptiveWeights (초기 가중치 균등).
2. 초기해  initial = InitialSolutionBuilder.build(problem).
          StructureCheck 위반 or Evaluator Infeasible → IllegalStateException (버그 — §12 구조 결함.
          초기해는 경로 단위 전파를 통과한 삽입만 했으므로 실패할 수 없다).
          current = best = initial. currentEval = bestEval = initialEval.
          currentScore = bestScore = initialScore.
3. 반복    while (종료 조건 미충족):
        종료 조건 = now ≥ deadline                                    → TIME_LIMIT
                 | maxSteps 존재 ∧ iterations ≥ maxSteps             → MAX_STEPS
                 | idleSteps 존재 ∧ iterations − lastBestStep ≥ 그 값 → IDLE_STEPS
                 | idleSec  존재 ∧ now − lastBestAt ≥ 그 값           → IDLE_TIME
   a. q = clamp(round(U[minFrac, maxFrac] × 배정 Request 수), 1, maxDestroyCount).
      배정 0건이면 destroy 생략 (draft = current — repair만 시도).
   b. destroy = weights로 룰렛 선택 → draft = destroy(problem, current, q, rng).
   c. repair  = weights로 룰렛 선택 → draft' = repair(problem, draft, rng).
   d. StructureCheck.check(problem, draft') 비어 있지 않으면 IllegalStateException —
      연산자 버그를 조용히 버리지 않는다 (노트 N2).
   e. result = Evaluator.evaluate(problem, profile, draft').
      Infeasible → 폐기, infeasibleDiscarded++, 다음 반복 (연산자가 경로 단위로 사전
      검증하므로 드물어야 정상 — §6 E10).
   f. acceptance (§4.5): 수락이면 current = draft', currentEval·currentScore = 평가 결과.
      Scores.compare(score, bestScore) < 0 이면 best = draft', bestEval·bestScore·bestFacts 갱신,
      bestImproved++, lastBestStep = iterations, lastBestAt = now.   // ← idle 리셋은 여기서만
   g. 두 연산자에 결과 보상 기록, weights.endIteration().
4. 종료    AlnsResult(best, bestEval, bestScore, bestFacts, initialEval, initialScore, stats) 반환.
          어느 조건으로 멈췄든 정상 종료다 — 그 시점 best로 재검증 진행 (Domain §12).
          반복 0회(한도가 이미 지남)면 initial이 곧 best다.
```

- **idle 리셋은 best 갱신(f)에서만** 한다. worse 수락에서 리셋하면 정체 종료가 무한정 밀린다.

### 4.3 삽입 후보 탐색 (repair 공통 루틴)

Request 하나를 경로 하나에 넣는 후보 나열과 검증. `GreedyInsertion`·`RegretInsertion`·
`InitialSolutionBuilder`가 공유한다 (노트 N5).

```text
대상 경로:  problem.compatibleVehicles(requestId)에 든 차량의 기존 경로
          + 그중 미사용 차량 하나로 여는 새 경로 (실제 VehicleId 소비 — §9.1.
            미사용 호환 차량이 여럿이면 VehicleId 문자열 순 첫 번째만 후보 — 결정성, 노트 N3).
위치:      DELIVERY_ONLY — delivery NodeId를 각 삽입 위치 0..n에.
          PICKUP_DELIVERY — pickup 위치 i ≤ delivery 위치 j 의 모든 (i, j) 쌍.
            픽업 선행이 후보 생성 규칙 자체로 보장된다 (§1.4·§9.1).
검증:      후보 방문 목록으로 RoutePropagator.propagate(problem, vehicleId, visits′) —
          Infeasible이면 후보 탈락. (그 경로의 정확한 물리 계산이지 근사가 아니다 — 노트 N7.)
비용(재량): (새 경로 여부, ΔdriveDistMeter, ΔrouteOperationalTimeSec) 사전식 —
          기존 경로 우선, 거리 증가 최소. 이 비용은 후보 '고르기'에만 쓴다.
          수락은 §4.4의 정식 평가만이 결정한다 (§9.2 MUST NOT).
```

### 4.4 연산자 4개 (재량 기본값 — 각 한 스텝)

```text
RandomRemoval.destroy(current, q):
  1. 배정된 RequestId 수집 (경로 방문을 problem.nodeRef로 역참조, RequestId 정렬 — 노트 N3).
  2. rng로 q개 선택. 각 Request의 방문 NodeId 전부(=pair)를 경로에서 제거.
  3. 빈 visits가 된 경로는 경로째 제거 (§6 E4). 제거된 RequestId를 bank에 추가.

RouteRemoval.destroy(current, q):
  1. 경로가 없으면 current 그대로. 있으면 rng로 경로 하나 선택 (removeCount 무시 — 힌트).
  2. 그 경로 전체 제거, 소유 Request 전부 bank로. (usedVehicleCount 축을 직접 흔든다 —
     사용 차량 수가 비교 2순위인 default profile에서 유효한 이웃.)

GreedyInsertion.repair(destroyed):
  1. bank 전체를 RequestId 정렬 후 rng로 순서 셔플.
  2. 순서대로: §4.3 후보 탐색 → 최소 비용 후보에 삽입, bank에서 제거. 후보 0개면 남긴다.

RegretInsertion.repair(destroyed):
  1. bank의 각 Request에 대해 §4.3 최선·차선 후보 비용 계산.
  2. regret = 차선 − 최선 (ΔdriveDistMeter 기준, 차선 없으면 +∞ 취급 — 지금 못 넣으면
     기회를 잃는 Request 우선). regret 최대 Request를 최선 위치에 삽입.
  3. 삽입할 때마다 영향 경로의 후보 비용 재계산. bank가 비거나 전원 후보 0개면 종료.
```

### 4.5 acceptance (비교 부호만 사용 — 노트 N1)

```text
cmp = Scores.compare(draftScore, currentScore)      // < 0 ⟺ draft가 더 좋음
cmp ≤ 0  → 수락 (동점 수락 = 재량 결정 — 정체 구간 표류 허용, Stage 3 E27 인계).
cmp > 0  → rng.nextDouble() < p(t) 이면 수락.
           p(t) = worseAcceptStartProbability × (1 − 경과/한도)  (선형 감쇠, 재량)
best 갱신은 항상 strict: Scores.compare(draftScore, bestScore) < 0 일 때만.
```

- 감쇠에 쓰는 "경과/한도"는 `timeLimitSec` 기준이다 — idle 한도는 감쇠에 쓰지 않는다
  (그 둘을 섞으면 정체 시 수락 확률이 튀어 정체를 더 길게 만든다).

---

## 5. 설계 노트

| # | 내용 |
|---|---|
| N1 | **acceptance에 스칼라 Δ 없음**: 고전 SA의 `exp(−Δ/T)`는 두 해의 점수 차를 한 숫자로 요구한다. 비교는 사전식이고 축을 한 숫자로 뭉개지 않는다(§8.3)가 확정이므로, 기본 acceptance는 비교 **부호**와 감쇠 확률만 쓴다. 다른 규칙(late acceptance 등)으로의 교체는 §9.3 재량 — 단 "정식 평가가 낸 `long[] score`만 입력"이라는 경계는 유지 |
| N2 | **구조 위반은 폐기가 아니라 예외**: 구조 결함은 품질 문제가 아니라 버그다 (§1.4·§6.3·§12). draft를 조용히 버리면 버그가 재검증(Stage 5)까지 숨는다. `IllegalStateException`으로 solve를 중단시키고 executor(Stage 6)가 FAILED로 기록한다 |
| N3 | **같은 seed = 같은 결과**: `Set`·`Map` 순회 순서에 의존하지 않도록 연산자는 후보를 ID 문자열 정렬 후 rng를 적용한다. 시드 정책 자체는 재량(§9.3)이지만, 시드가 주어졌을 때의 결정성은 테스트 안정성(T1·T5)의 전제라 기본 연산자의 계약으로 둔다 |
| N4 | **증분 계산은 아직 없다**: 이 Stage의 평가는 항상 전체 재계산(Stage 3 N7)이고 후보 검증은 경로 단위 전파다. 규모(경로 ~30 × 방문 ~20)에서 충분하다. 증분 캐시·shortlist를 나중에 넣는 것은 재량이나, 도입 시 "캐시 점수 = 전체 재계산 점수" 대조 테스트(§6.4)가 필수다 |
| N5 | **초기해 = 빈 해의 repair**: `InitialSolutionBuilder`는 §4.3 후보 탐색을 결정적 순서로 쓰는 특수 사례다. 삽입 루틴을 하나만 구현·검증하면 된다 |
| N6 | **Stage 5 인계**: 재검증 진입값은 `AlnsResult.best`(분해는 Stage 3 N6 — routes/bank가 곧 domain 타입 분해값)와 `bestEvaluation`(점수 대조 대상, §10.2)이다. verify가 `solve` 타입을 직접 받을 수 없으므로(ArchUnit) 변환 어댑팅은 Stage 5가 정의한다 |
| N7 | **경로 단위 전파는 근사가 아니다**: `RoutePropagator`는 그 경로의 정확한 물리·hard 판정이다(§7). 다만 호환성·profile hard·해 전체 집계는 `Evaluator`만 하므로, 수락 직전의 전체 정식 평가는 생략할 수 없다 (§4.2-e가 항상 돈다) |

---

## 6. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | requests 빈 목록 | initial = (routes ∅, bank ∅), 반복은 돌지만 no-op — 즉시라도 best 유효 | §9.1·Stage 2 E19 |
| E2 | vehicles 빈 목록 / 전 Request 호환 0대 | 전부 bank인 초기해로 진행·반환 (유효한 해) | §3.4·§9.1 |
| E3 | q > 배정 수 | 배정 수로 clamp. 배정 0이면 destroy 생략 (§4.2-a) | §9.3 재량 |
| E4 | destroy로 경로의 마지막 Request 제거 | 경로째 제거 — 빈 visits Route는 생성 불가 (Stage 3 E30). 차량은 미사용 풀로 복귀 | §6.2·§8.2 |
| E5 | repair 0건 삽입 | 정상 시도 — draft는 대개 미배정 증가로 패배, acceptance가 판단 | §9.1 |
| E6 | destroy 이전부터 bank였던 Request | repair 대상에 포함 — 매 trial이 재배치 기회 | §6.3 (bank는 ID 집합일 뿐) |
| E7 | PD 삽입 위치 | (i ≤ j) 쌍만 생성 — 픽업 선행이 규칙으로 보장. i = j는 pickup 바로 뒤 delivery | §1.4 |
| E8 | 동점 (cmp == 0) | current 교체 수락, best는 불변 (strict <) | §8.3·Stage 3 E27 인계 |
| E9 | 시간 한도가 초기해 생성 중 지남 | 반복 0회, initial = best 반환 — FAILED 아님 | §12 "탐색 중단 = 정상" |
| E10 | draft'가 Evaluator Infeasible | 폐기 + 카운트. 연산자는 경로 전파로 사전 검증하므로 빈발하면 연산자 버그 신호 (로그로 관찰) | §8.1 (hard 감점 통과 금지) |
| E11 | 한 trial이 deadline을 넘겨 끝남 | 다음 반복 조건에서 종료 — 약간의 초과는 허용 (연산자는 deadline을 모른다) | §12·재량 |
| E11b | idle 한도와 시간 한도가 동시에 걸림 | 검사 순서대로 첫 번째 것을 `termination`에 기록 (TIME_LIMIT 우선). 어느 쪽이든 정상 종료 | §3.3 |
| E12 | 연산자가 비호환 차량에 삽입 | 정상 경로에선 불가능(§4.3 사전 필터). 뚫리면 Evaluator INCOMPATIBLE_VEHICLE → 폐기 | §3.4·Stage 3 §4.2 |
| E13 | 시간 한도 없이 step 한도만 | 불가능 — `timeLimitSec`은 필수 필드다 (호출자가 항상 결정해 넘긴다). 네 조건 전부 검사 | §3.3·§4.2-3 |
| E13b | `idleSteps`·`idleSec` 둘 다 부재 | 정상 — 시간·step 한도만으로 종료 (기존 동작과 동일) | §3.3 |
| E14 | 같은 seed·같은 Problem 재실행 | 동일한 AlnsResult (노트 N3) | 재량이되 계약화 |
| E15 | destroy가 removeCount보다 덜 뺌 (RouteRemoval 등) | 허용 — removeCount는 힌트 (§3.1) | §9.3 재량 |

---

## 7. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/solve/`. 의존은 JUnit만 —
jqwik류 property 라이브러리를 추가하지 않는다 (Stage 0 §4.2가 test 의존을 JUnit·ArchUnit으로
고정). property 테스트는 seed 루프로 손수 만든다. Problem은 Stage 1·2 경로로 손 조립한다
(fixture JSON 파싱은 Stage 6 — Stage 1 §7과 동일 원칙).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `AlnsSolverTest.improvesOverInitialOnSmallFixture` | 소형 fixture(손 조립: 두 지역 클러스터 × 차량 2대, 창 마감 순 greedy가 클러스터를 교차 배정하도록 배치) + 고정 seed + `maxSteps` 상한 → `Scores.compare(bestScore, initialScore) < 0` | "소형 fixture에서 초기해 대비 개선 확인" |
| T2 | `AlnsInvariantPropertyTest.structureHoldsUnderRandomSteps` | seed ~20개 × 랜덤 연산자 시퀀스 ~200스텝: **destroy 직후와 repair 직후 각각** `StructureCheck.check` 위반 0 단언 + 각 스텝의 배정↔bank 이동이 Request 단위(부분 pair 이동 없음)임을 단언. PICKUP_DELIVERY 포함 문제로 수행 | "pair·XOR 불변식이 탐색 중 유지되는 property 테스트 (랜덤 스텝 N회 후 구조 검사)" |
| T3 | `AlnsSolverTest.stopsAtTimeLimitAndReturnsBest` | 아주 짧은 한도로 solve → 정상 반환·best 존재·`stats.elapsedMillis` 기록·`termination == TIME_LIMIT` (E9 포함: 한도 0 → initial 반환) | (Plan 범위 문장 "시간 한도 종료") |
| T4 | `AlnsSolverTest.reportedEvaluationMatchesFreshEvaluation` | solve 후 `Evaluator.evaluate(problem, profile, result.best())`를 새로 실행 → `bestEvaluation`은 record 동등, `bestScore`는 `Arrays.equals` (§6.4 "캐시 = 재계산", 체크리스트 #7 — Stage 5 대조의 전제) | (Plan 범위 문장 "acceptance" — 수락 권위가 정식 평가임의 증명) |
| T5 | `AlnsSolverTest.sameSeedSameResult` | 같은 seed 두 번 solve → best 해·`bestEvaluation` 동등, `bestScore`는 `Arrays.equals` (노트 N3). `AlnsResult` 자체를 `equals`로 비교하지 않는다 — 배열 필드 때문 (Stage 3 §4.3 공통 규칙) | (T1·T2의 안정성 전제) |
| T6 | `InsertionOperatorsTest.pairInsertionKeepsPickupFirst` | PD Request 삽입 후보가 전부 i ≤ j (E7), 삽입 결과 경로에서 픽업 선행 | (Plan 범위 문장 "destroy/repair(pair 단위)") |
| T7 | `RemovalOperatorsTest.pairRemovalAndEmptiedRouteDrop` | PD 제거 시 두 방문이 함께 사라짐 · E3 clamp · E4 빈 경로 제거 | 〃 |
| T8 | `InsertionOperatorsTest.usesOnlyCompatibleRealVehicles` | 비호환 차량 후보 없음(E12) · 새 경로는 실제 미사용 VehicleId(§9.1) · 호환 0대 Request는 bank 유지(E2) | 〃 |
| T9 | `AlnsSolverTest.degenerateProblemsReturnValidResult` | E1(주문 0)·E2(차량 0) → 예외 없이 유효한 AlnsResult | (Plan 범위 문장 "초기해 생성") |
| T10 | `AlnsSolverTest.stopsOnIdleLimits` | 넉넉한 시간 한도 + 작은 `idleSteps`(및 별도 케이스로 `idleSec`) → 한도 훨씬 전에 종료하고 `termination`이 IDLE_STEPS / IDLE_TIME. worse 수락이 일어나도 idle 카운터가 리셋되지 않음을 단언 (§4.2-f) | (본 개정에서 추가된 종료 조건의 직접 검증) |
| T11 | `AlnsScaleTest.runsOnFullScaleSyntheticProblem` | **규모 측정.** Stage 2 T12와 **같은 합성 문제**(장소 453·주문 452·차량 31·이동표 453² 전 쌍)로 `AlnsSolver.solve` 1회 → 시간 한도 안에 정상 종료. **시간 한도 안에서 몇 번 반복했는지(`AlnsRunStats`의 반복·수락 수, `elapsedMillis`)를 출력해 기록한다.** 해의 품질·개선폭은 판정하지 않는다 (그건 Stage 8). 실물 JSON은 읽지 않는다 — 입력은 프로그램으로 조립한다 | Plan Stage 4 "**규모**" 문장 |

T3–T10은 Plan DoD 요약 문장 밖이지만 Plan Stage 4 범위 문장("초기해 생성, destroy/repair(pair 단위),
acceptance, 시간 한도 종료")의 직접 검증이다 — Plan §1의 편입(2026-08-11)에 따라 이 표 전부가
완료 기준이다.

---

## 8. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| 재검증(`verify`)·점수 대조 실행·미배정 사유(`NO_COMPATIBLE_VEHICLE` 등) | Stage 5 | Domain §10–§11 |
| 결과 JSON·run 메타 확정 (stats 포함 여부 포함) | Stage 5·6 | Domain §11, §9 Q2 |
| wire `Termination.secondsSpentLimit` 파싱·executor의 전체 시간 관리·config 주입 | Stage 6 | Architecture §3.2, Stage 1 §2.2 |
| 탐색 파라미터 튜닝·Win 지표 비교 (§3.3 기본값의 실측 조정) | Stage 8 | Plan §1 Stage 8 |
| 증분 평가·삽입 캐시의 실제 도입 | 재량 (필요 시) | Domain §9.2, 노트 N4 |
| 추가 연산자 (Shaw/worst removal 등) — SPI로 열려 있음 | 필요 시 (Stage 8 실험) | Domain §9.3 |
| 병렬·분산 탐색, MIP 재조합 | 범위 밖 | Master §4·§6 |
| profile별 탐색 예산 차등 (profile은 hard 제약·score 축만 소유) | 안 함 | Domain §8.4·§2.5.1 |
| property 라이브러리(jqwik 등) 의존 추가 | 안 함 | Stage 0 §4.2 |
| `Solution`·`Evaluator` 등 Stage 3 타입 변경 | 안 함 (그대로 소비) | Stage 3 §2–§4 |

---

## 9. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 4 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | wire `Termination.secondsSpentLimit`의 적용 범위 — ALNS 루프만인지, 초기해·Problem 동결·재검증까지 포함한 전체 풀이인지 어느 문서도 정의하지 않았다 | `AlnsSolver`는 자기 예산(초기해 + 반복 루프)에만 적용. 전체 풀이 타임박스가 필요하면 Stage 6 executor에서 별도 결정 (Architecture §3.2 "시간 한도 = 입력 옵션 또는 설정"과 정합) |
| Q2 | `AlnsRunStats`(반복·수락 수 등)를 결과 JSON run 메타에 넣을지 | **부분 해소 (2026-08-10).** Domain §11.1이 run에 "적용된 배송정책과 탐색 예산을 따로 기록"을 요구하므로 `AlnsConfig`는 기록한다. 반복·수락 카운터까지 넣을지는 Stage 5·6에서 wire 협의(§11.2)와 함께 결정 |
