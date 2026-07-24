# 세션 04 — 고객사별 목적함수 정책의 유연성

이 문서는 `master-design.md`, `domain-design.md`, `arranged/01_problem_definition.md`, `arranged/06_practical_extensions.md`를 바탕으로 고객사마다 다른 목적함수와 우선순위를 코어 ALNS 변경 없이 적용하기 위한 설계 계약을 정리한다. 구현 코드는 이 문서의 범위가 아니다.

# 확정 요구

1. 목적함수와 목표 우선순위는 고객사마다 다를 수 있다.
2. 고객사별 목적함수와 우선순위는 `SiteProfile` 또는 그 하위 최적화 정책에서 교체할 수 있어야 한다.
3. 기존 측정값과 비용 요소를 조합하는 변경은 ALNS 코어 수정 없이 처리해야 한다.
4. 새로운 고객사 추가 때문에 `AlnsEngine`, destroy/repair 연산자, 삽입 평가 흐름에 고객사별 조건문을 추가해서는 안 된다.
5. 실행 가능성 판단과 선호도 판단을 분리한다.
   - 반드시 지켜야 하는 규칙은 `RouteConstraint` 또는 입력 검증의 책임이다.
   - 가능한 해 중 무엇이 더 좋은지는 목적함수 정책의 책임이다.
6. 하나의 거대한 Big-M 점수로 모든 우선순위를 흉내 내지 않는다.
7. 여러 목표에 명확한 우선순위가 필요하면 사전식 다단계 최적화를 사용한다.
8. 각 단계는 직전 단계 결과를 warm-start로 사용하고, 상위 단계에서 확보한 결과를 하위 단계가 임의로 훼손하지 못하게 한다.
9. 고객사별 정책과 계획은 문제 실행 단위로 생성한다. 특정 `requestId`에 대응하는 배열을 가진 정책 인스턴스를 여러 실행에서 공유하지 않는다.
10. 결과에는 최종 값뿐 아니라 적용한 정책·계획 버전과 목표별 값을 남겨 결과를 설명하고 재현할 수 있어야 한다.

현재 문서에서 유지할 부분은 `RouteMetrics`와 `ScorePolicy`의 분리, Big-M 회피, warm-start 방식이다. 다음 부분은 보완이 필요하다.

- `미배정 → 차량 수 → 총비용` 순서가 모든 고객사의 고정 규칙처럼 기술되어 있다.
- `ObjectivePriority`의 책임과 계약이 정의되어 있지 않다.
- 현재 `ScorePolicy`는 경로비와 미배정 페널티의 단순 합산에는 적합하지만, 해 전체에 적용되는 비가산 비용이나 배정 상태별 비용을 충분히 표현하지 못한다.
- `SiteProfile`에 `SolvePlan` 생성 책임이 없어 고객사별 우선순위 교체 지점이 명확하지 않다.

# 설계 원칙

## 1. 사실, 업무 비용, 비교 순서를 분리한다

평가 흐름을 다음 네 단계로 구분한다.

```text
RoutePropagator
  → RouteMetrics / SolutionMetrics       객관적 사실
  → ScorePolicy                          고객사 비용과 페널티
  → Objective                            한 가지 비교 기준
  → SolvePlan                            비교 기준의 실행 순서
```

각 구성요소의 책임은 다음과 같다.

| 구성요소 | 책임 | 알면 안 되는 것 |
|---|---|---|
| `RouteMetrics` | 경로에서 발생한 거리·시간·정차 수 등의 사실 | 고객사 단가와 목표 우선순위 |
| `SolutionMetrics` | 사용 차량 수, 배정 상태, 전체 거리·시간 등의 해 전체 사실 | 고객사 단가와 stage 순서 |
| `ScorePolicy` | 사실과 배정 상태에 고객사 단가·페널티를 적용 | 실행 가능성 판정, ALNS 연산자 선택, stage 순서 |
| `Objective` | 평가 결과에서 하나의 수치 기준을 추출하고 두 해를 비교 | 고객사 식별자, ALNS 내부 구현 |
| `SolveStage` | 활성 목표, 이전 목표 보호 규칙, 탐색 전략과 예산 정의 | 고객사별 조건문 |
| `SolvePlan` | stage 순서와 최종 사전식 비교 벡터 정의 | 경로 시간·용량 계산 세부사항 |

`ScorePolicy`와 `SolvePlan`은 같은 개념이 아니다.

- `ScorePolicy`는 “미배정 VIP 주문의 비용은 얼마인가?”를 결정한다.
- `Objective`는 “가중 미배정 비용을 비교할 것인가, 미배정 건수를 비교할 것인가?”를 결정한다.
- `SolvePlan`은 “그 기준을 차량 수나 거리보다 먼저 볼 것인가?”를 결정한다.

## 2. 코어가 업무 목표의 종류를 열거하지 않는다

`ObjectivePriority`를 고객사 목표를 모두 담는 고정 enum으로 만들면 새로운 목표가 추가될 때마다 코어를 수정해야 한다. 대신 코어는 `Objective` 계약과 순서가 있는 `SolvePlan`만 이해해야 한다.

다음과 같은 표준 목표는 공용 구현으로 제공할 수 있다.

- 미배정 주문 수
- 가중 미배정 페널티
- 사용 차량 수
- 전체 거리
- 전체 운전시간
- 전체 경로시간
- 고객사 총비용
- 외주·이월 비용

고객사 고유 목표는 같은 `Objective` 계약으로 고객사 모듈에 추가한다. ALNS 코어에 `switch (siteId)` 또는 `switch (objectiveType)`를 두지 않는다.

## 3. 사전식 순서와 비용 가중합을 혼동하지 않는다

반드시 보존해야 하는 우선순위는 서로 다른 stage로 표현한다.

```text
Stage 1: 가중 미배정 페널티 최소화
Stage 2: 사용 차량 수 최소화
Stage 3: 고객사 총비용 최소화
```

같은 우선순위 안에서 교환 가능한 비용만 하나의 `ScorePolicy` 합계로 표현한다.

```text
고객사 총비용
= 거리비
+ 차량 고정비
+ 대기시간비
+ 야간 운행비
```

따라서 VIP 주문과 일반 주문의 미배정 차이를 절대로 양보할 수 없다면 별도 상위 목표 또는 hard rule로 둔다. 단순히 큰 금액을 넣어 우연히 우선되게 하지 않는다.

## 4. hard constraint와 soft preference를 명시적으로 구분한다

다음은 목적함수로 우회하지 않는다.

- 법규·안전상 금지된 배차
- 차량 용량과 hard time window
- 반드시 같은 차량이어야 하는 pickup-delivery 쌍
- 계약상 절대 미배정할 수 없는 주문

이들은 `RouteConstraint`, 모델 불변조건 또는 배정 상태 제약으로 표현한다. `ScorePolicy`는 실행 불가능한 경로를 가능하게 만들 수 없다.

선호나 운영상 비용에 해당하는 항목은 목적함수로 둔다.

- 일반 주문의 미배정 비용
- 외주와 이월의 상대 비용
- 차량 한 대 추가 사용 비용
- 거리, 대기, 야간 운행 비용
- SLA 목표 초과 비용

## 5. 목표 정의와 정책 인스턴스는 실행 범위에 묶는다

고객사 우선순위, 주문별 페널티, 외주 단가 등은 변환된 `requestId`와 연결될 수 있다. 따라서 `SiteProfile`은 상태를 가진 싱글턴 정책을 직접 보유하기보다 다음을 생성하는 팩토리 역할을 해야 한다.

```text
Plan + ProblemInstance
  → OptimizationPolicyFactory
  → 실행 전용 ScorePolicy
  → 실행 전용 Objective 집합
  → 실행 전용 SolvePlan
```

동일한 설정을 공유할 수는 있지만 요청별 배열과 캐시는 공유하지 않는다.

## 6. “최소 변경”의 범위를 명확히 한다

| 요구 변경 | 예상 변경 범위 |
|---|---|
| 기존 비용의 단가 변경 | 고객사 설정만 변경 |
| 기존 목표의 우선순위 변경 | `SolvePlan` 설정만 변경 |
| 기존 측정값을 이용한 새 비용식 | 고객사 `ScorePolicy`만 추가·교체 |
| 기존 평가값을 이용한 새 비교 목표 | 고객사 `Objective`와 plan만 추가 |
| 새 물리적 측정값 필요 | 측정 확장 지점 추가 후 정책에서 사용 |
| 경로 실행 가능성 규칙 추가 | `RouteConstraint` 추가 |
| 시간·적재 전파 의미 변경 | `RoutePropagator` 변경 검토 |

“모든 요구를 무조건 코어 무수정으로 처리”하는 것이 아니라, 기존 사실을 어떻게 평가하고 어떤 순서로 비교할지만 바뀌는 경우 코어를 수정하지 않는 것이 목표다.

# 권장 인터페이스 수준 계약(의사코드 가능)

## 1. 공통 평가 결과

`RouteMetrics`에 더해 해 전체의 객관적 사실을 모은 `SolutionMetrics`가 필요하다.

```java
record SolutionMetrics(
    int assignedRequestCount,
    int unassignedRequestCount,
    int outsourcedRequestCount,
    int deferredRequestCount,
    int usedVehicleCount,
    long totalDistance,
    long totalDriveTime,
    long totalWaitTime,
    long totalServiceTime,
    long totalRouteDuration
) {}
```

필드 목록은 예시이며 최종 목록은 다른 세션의 측정값 설계와 맞춘다. 핵심 계약은 “고객사 단가가 적용되지 않은 사실”만 포함한다는 것이다.

한 번의 완전 평가 결과는 다음처럼 구성한다.

```java
record SolutionEvaluation(
    SolutionMetrics metrics,
    CostBreakdown costs
) {}
```

`CostBreakdown`은 최소한 다음을 지원한다.

- 전체 고객사 비용
- 비용 요소별 값: 거리비, 차량 고정비, 미배정, 외주, 이월, 야간 운행 등
- 산술 overflow가 검증된 `long` 값
- 결과 보고에 사용할 안정적인 비용 요소 ID

비용 요소 ID는 문자열 업무 데이터를 그대로 담는 용도가 아니라 타입이 있는 식별자다. 성능이 필요하면 실행 시작 시 ID를 정수 슬롯으로 컴파일해 내부적으로 `long[]`를 사용할 수 있다.

## 2. `ScorePolicy`

현재의 `routeCost + unassignedPenalty` 계약은 다음 세 수준을 표현할 수 있도록 확장하는 것이 좋다.

```java
interface ScorePolicy {
    CostBreakdown routeCost(
        RouteMetrics route,
        SolverVehicle vehicle,
        ScoringContext context
    );

    CostBreakdown requestDispositionCost(
        int requestId,
        AssignmentStatus status,
        ScoringContext context
    );

    CostBreakdown solutionAdjustment(
        SolutionMetrics solution,
        ScoringContext context
    );
}
```

각 메서드의 의미는 다음과 같다.

- `routeCost`: 거리비, 차량 고정비, 대기시간비처럼 경로별로 더할 수 있는 비용
- `requestDispositionCost`: 배정, 미배정, 외주, 이월 등 요청 상태별 비용
- `solutionAdjustment`: 구간 단가, 전체 SLA 달성률, 차량 수 구간 할인처럼 단순 경로 합으로 표현할 수 없는 비용

`solutionAdjustment`는 선택적인 확장 지점이며 기본값은 0이다. 성능상 모든 삽입 후보에서 전체 해를 재평가하지 않도록 가산 비용과 비가산 비용을 구분해 캐시할 수 있어야 한다. 다만 최종 점수는 항상 완전 재평가로 재현 가능해야 한다.

`ScorePolicy` 계약은 다음을 보장한다.

- 같은 입력에는 같은 결과를 내는 순수 함수다.
- 경로 실행 가능성을 변경하지 않는다.
- 원본 JSON이나 고객사 DTO를 직접 읽지 않는다.
- 가변 전역 상태와 전역 캐시를 사용하지 않는다.
- 경로 순서를 다시 해석하지 않고 평가 계층이 제공한 사실만 사용한다.
- 계산 결과와 비용 요소 합계가 완전 평가에서도 동일하다.

## 3. `Objective`

모든 목표를 고정 enum으로 제한하지 않고 다음 계약으로 표현한다.

```java
interface Objective {
    ObjectiveId id();
    OptimizationDirection direction();
    long value(SolutionEvaluation evaluation);

    // direction을 반영한 비교 결과
    int compare(long candidate, long reference);

    // candidate가 reference보다 나쁠 때 SA가 사용할 안전한 크기,
    // 나쁘지 않으면 0
    long worseningAmount(long candidate, long reference);
}
```

모든 목표값은 설명 가능한 정수여야 한다. `MINIMIZE`와 `MAXIMIZE`를 명시적으로 구분하며, 최대화 목표를 무조건 음수로 바꾸어 overflow 위험이나 결과 해석 문제를 만들지 않는다.

표준 목표 예시는 다음과 같다.

```text
UnassignedCountObjective     → metrics.unassignedRequestCount
WeightedUnassignedObjective  → costs[UNASSIGNED]
UsedVehicleCountObjective    → metrics.usedVehicleCount
TotalDistanceObjective       → metrics.totalDistance
TotalDurationObjective       → metrics.totalRouteDuration
PolicyTotalCostObjective     → costs.total
OutsourceCostObjective       → costs[OUTSOURCE]
```

`Objective`는 값을 추출하고 비교할 뿐, 탐색 연산자를 직접 실행하지 않는다.

## 4. `ObjectiveCatalog`

한 실행에서 사용할 수 있는 표준 목표와 고객사 목표를 모은 읽기 전용 레지스트리를 둔다.

```java
interface ObjectiveCatalog {
    Objective require(ObjectiveId id);
}
```

고객사 plan은 ID로 목표를 선택하되, 실행 시작 전에 모든 ID가 실제 구현으로 해석되는지 검증한다. 실행 중 미등록 ID를 만나 fallback하지 않고 입력 오류로 종료한다.

## 5. `SolveStage`와 보호 규칙

```java
record SolveStage(
    StageId id,
    Objective objective,
    StageSearchStrategy strategy,
    PreviousObjectiveGuard guard,
    StageBudget budget
) {}
```

- `objective`: 이 단계에서 개선할 단일 목표
- `strategy`: 일반 ALNS, 차량 경로 제거 중심 탐색 등 재사용 가능한 탐색 전략
- `guard`: 이전 단계 결과를 얼마나 엄격하게 보존할지 정의
- `budget`: step 수, 보조 시간 제한, stage별 탐색 설정

`FleetMinimizer`는 “항상 두 번째 단계”가 아니라 `UsedVehicleCountObjective`에 적합한 재사용 가능한 `StageSearchStrategy`로 취급한다. 고객사 plan이 차량 수 목표를 사용하지 않으면 실행하지 않는다.

기본 보호 규칙은 `NO_WORSE_THAN_BEST`다.

```text
Stage n 진입 시
→ 앞선 각 stage의 종료 시점 best 값을 보호 기준으로 설정
→ 앞선 목표 벡터가 더 좋아진 후보는 우선 수용하고 보호값 갱신
→ 앞선 목표 벡터가 같은 후보끼리 현재 stage 목표 비교
→ 앞선 목표 벡터가 나빠진 후보는 하위 목표가 좋아도 stage best가 될 수 없음
```

여기서 보호하는 값은 수학적으로 증명된 전역 최적값이 아니라 해당 탐색에서 발견한 최선값이다. 후속 stage가 상위 목표를 더 개선하는 것은 허용하며, 개선된 값이 새로운 보호 기준이 된다.

필요하면 고객사가 명시적으로 허용한 경우에만 다음 확장을 고려한다.

- `FINAL_TOLERANCE`: 최종 결과가 상위 목표를 지정 범위 안에서 악화시킬 수 있음
- `SEARCH_RELAXATION`: 탐색 중 임시 악화는 허용하되 stage 종료 결과는 원래 보호 범위를 만족해야 함

묵시적 tolerance는 허용하지 않는다.

## 6. `SolvePlan`

```java
record SolvePlan(
    SolvePlanId id,
    String version,
    List<SolveStage> stages
) {}
```

plan 검증 계약은 다음과 같다.

- stage가 하나 이상 존재한다.
- stage ID는 중복되지 않는다.
- 모든 objective와 strategy가 실행 전에 해석된다.
- 각 stage의 budget과 guard가 유효하다.
- 빈 plan이나 알 수 없는 objective에 대한 암묵적 기본값은 두지 않는다.
- 최종 해 비교 벡터는 stage 순서와 동일하다.
- 값이 완전히 같은 해의 결정적 tie-break는 고객사 비용이 아니라 안정적인 해 구조 서명으로 처리한다.

## 7. `SiteProfile` 조정

`SiteProfile`은 고객사별 최적화 정책 생성 지점을 포함해야 한다.

```java
record SiteProfile(
    String siteId,
    ProblemTransformer transformer,
    OptimizationPolicyFactory optimizationPolicyFactory,
    List<RouteConstraint> extraConstraints,
    RoutePropagator propagator
) {}
```

```java
record OptimizationPolicy(
    ScorePolicy scorePolicy,
    ObjectiveCatalog objectives,
    SolvePlan solvePlan
) {}
```

하나의 고객사가 여러 운영 모드를 가진다면 `siteId`에 조건문을 추가하지 않고 다음처럼 versioned plan preset을 선택한다.

```text
ACME / SERVICE_FIRST / v3
ACME / FLEET_FIRST   / v2
ACME / COST_FIRST    / v5
```

선택된 preset과 버전은 결과 메타데이터에 기록한다.

# SolvePlan 관계

## 1. 실행 규칙

```text
초기해 포트폴리오 생성
  → 첫 stage의 Objective로 초기 incumbent 선택
  → Stage 1 실행
  → Stage 1 best와 보호값 저장
  → Stage 1 best를 Stage 2 warm-start로 전달
  → Stage 2 guard를 만족하는 범위에서 Stage 2 목표 개선
  → 다음 stage 반복
  → 모든 stage 목표값과 비용 breakdown을 결과에 기록
```

후속 stage가 실패하거나 개선하지 못하면 직전 stage의 best를 그대로 반환한다. 하위 목표 개선을 위해 상위 목표를 묵시적으로 악화시키지 않는다.

후보 평가는 먼저 선행 objective vector를 사전식으로 비교한다. 선행 목표가 개선되면 현재 stage 목표와 관계없이 더 좋은 후보이고, 선행 목표가 같을 때만 현재 stage의 활성 목표 delta를 SA acceptance에 전달한다. 선행 목표가 악화되면 기본 guard에서는 거부한다. 이로써 서로 다른 단위의 목표를 Big-M으로 섞지 않는다.

## 2. 고객사별 plan 예시

서비스 수준을 가장 중요하게 보는 고객사:

```text
1. 가중 미배정 페널티 최소화
2. 사용 차량 수 최소화
3. 고객사 총비용 최소화
```

모든 주문 배정이 hard rule이고 차량 계약 대수가 가장 중요한 고객사:

```text
1. 사용 차량 수 최소화
2. 전체 거리 최소화
```

외주를 허용하고 실제 지출을 직접 비교하는 고객사:

```text
1. 필수 주문 미배정 수 최소화
2. 정규 운행비 + 외주비 + 이월비 최소화
3. 전체 경로시간 최소화
```

학술 벤치마크:

```text
1. 사용 차량 수 최소화
2. 전체 거리 최소화
```

이 예시는 preset일 뿐 코어의 고정 순서가 아니다. 기존 D16의 설명은 “사전식 stage를 지원한다”로 일반화하고, 현재 적힌 `미배정 → 차량 수 → 총비용`은 기본 업무 preset 중 하나로 내려야 한다.

## 3. 검색 전략과 목표의 결합 방식

목표마다 효과적인 탐색 전략은 다를 수 있지만 엔진에 목표별 조건문을 넣지 않는다.

```text
SolveStage
  ├─ Objective
  ├─ StageSearchStrategy
  └─ StageBudget
```

예:

- 미배정 개선 stage: dummy/unassigned reinsertion과 priority-aware repair의 가중치 강화
- 차량 수 stage: `FleetMinimizer`와 route removal 사용
- 거리·비용 stage: 일반 destroy/repair와 local search 사용

이는 동일한 연산자 구현을 stage별 설정으로 조합한다는 뜻이며 고객사 전용 ALNS 복제를 의미하지 않는다.

# 테스트 계약

## 1. 정책 격리

동일한 `ProblemInstance`와 동일한 해에 서로 다른 `ScorePolicy`를 적용한다.

- `RouteMetrics`, `SolutionMetrics`, 실행 가능성은 동일해야 한다.
- 비용 breakdown과 목적값은 정책에 따라 달라질 수 있다.
- 정책 변경 때문에 경로의 가능/불가능 판정이 바뀌면 실패다.

## 2. 고객사별 우선순위

손으로 비교 가능한 해 A, B, C를 만들고 plan별 순서가 달라지는지 검증한다.

예:

```text
A: 미배정 0, 차량 5, 거리 100
B: 미배정 1, 차량 3, 거리 80
```

- 서비스 우선 plan은 A를 선택해야 한다.
- 미배정을 허용하고 차량 수를 실제 최상위 목표로 둔 plan은 의도적으로 B를 선택할 수 있다.
- ALNS 코어 코드는 두 경우에 동일해야 한다.

VIP와 일반 주문의 미배정 페널티가 다른 정책에서는 같은 미배정 건수라도 가중 페널티 목표가 올바른 해를 선택해야 한다.

## 3. 사전식 보호 불변조건

모든 후속 stage에 대해 다음을 검증한다.

```text
NO_WORSE_THAN_BEST인 선행 objective vector의 후속 stage 종료값
<= 사전식 비교 기준으로 직전 stage 종료값
```

허용 범위 정책을 사용하면 명시된 범위 안인지 검증한다. 후속 stage가 개선에 실패하면 직전 best가 보존되어야 한다.

## 4. warm-start와 결과 비교

- Stage n의 시작 해가 Stage n-1의 종료 best와 구조적으로 같아야 한다.
- 최종 결과의 objective vector 순서가 `SolvePlan.stages`와 같아야 한다.
- plan의 사전식 비교 결과와 stage별 실행 결과가 일치해야 한다.

## 5. 부분 평가와 완전 평가의 일치

삽입·제거 시 계산한 증분 비용과 해 전체 재평가 결과가 같아야 한다.

```text
cached route/request/solution cost
= full SolutionEvaluation cost
```

비가산 `solutionAdjustment`를 사용하는 정책도 별도 테스트한다.

## 6. 확장성 회귀 테스트

새 고객사 policy·objective·plan fixture를 고객사 모듈에 추가한 상태에서 다음을 확인한다.

- 공용 ALNS 패키지 수정 없이 등록되고 실행된다.
- 기존 고객사 결과와 정책 버전이 변하지 않는다.
- 알 수 없는 objective, 비용 요소 또는 strategy ID는 시작 전에 명확한 오류로 거부된다.
- `siteId`별 조건문이 ALNS·평가 코어에 유입되지 않았는지 아키텍처 테스트로 검사한다.

## 7. 결정성과 감사 가능성

동일 입력, 동일 seed, 동일 step 제한, 동일 정책·plan 버전이면 다음이 같아야 한다.

- 최종 해
- stage별 best objective 값
- 최종 objective vector
- 비용 breakdown

결과 메타데이터에는 최소한 다음을 남긴다.

```text
siteProfileId
scorePolicyId / version
solvePlanId / version
stage별 objectiveId와 종료값
최종 CostBreakdown
```

# 남은 질문

다음 항목은 문서 개정 전에 결정하거나, 미결정 상태와 기본 권장값을 명시해야 한다.

1. `SolvePlan` 선택 단위는 고객사별 하나인가, 동일 고객사 내에서도 계획 실행 요청마다 preset을 선택할 수 있어야 하는가?
   - 권장: 고객사 기본 preset을 두고, 허용된 preset ID에 한해 실행 요청에서 선택한다.
2. 선행 stage의 값은 항상 정확히 보존해야 하는가, 일부 고객사는 차량 한 대 증가와 서비스 수준 개선 같은 명시적 허용 범위를 요구하는가?
   - 권장: 기본은 `NO_WORSE_THAN_BEST`, tolerance는 고객사가 수치와 단위를 명시한 경우만 허용한다.
3. “필수 주문”은 정말 미배정 불가인 hard rule인가, 매우 큰 손실을 감수하면 미배정 가능한 soft priority인가?
   - 권장: 계약상 불가하면 hard rule, 운영상 선호이면 가중 미배정 목표로 구분한다.
4. 외주, 이월, 미배송을 서로 다른 `AssignmentStatus`로 구분하고 각각 독립 비용과 목표를 제공할 것인가?
   - 권장: 처음부터 상태를 구분한다. 하나의 `unassigned`로 합치면 후속 정책 확장 시 데이터 의미를 복구하기 어렵다.
5. 고객사 비용에 전체 SLA 달성률, 차량 대수 구간 할인, 일별 최소요금처럼 경로별 합으로 표현할 수 없는 비가산 비용이 실제로 존재하는가?
   - 권장: 계약에는 `solutionAdjustment` 확장 지점을 두되, 실제 요구가 생길 때만 활성화한다.
6. 결과 사용자에게 목표별 값과 비용 breakdown을 어느 수준까지 공개해야 하는가?
   - 권장: 운영 응답 또는 저장 결과에는 전체 breakdown과 정책·plan 버전을 남기고, 사용자 화면 노출 범위만 별도로 정한다.
7. 설정 값 변경도 정책 버전 변경으로 볼 것인가?
   - 권장: 결과 비교와 재현을 위해 단가, 우선순위, stage 순서 또는 guard가 바뀌면 버전을 변경한다.
