# 세션 14 — 고객사별 정책·측정 확장 구조

> **세션 30 통합 상태 (2026-07-23):** 이 문서의 policy seam은 유지하지만 사용자 질문의 임시 기본 가정은 현재 결정이 아니다. 세션 29에서 고객사별 승인 preset, mandatory 최상위 사전식 objective, `DIRECT/LEASE` vehicle ownership과 고객사 선택적 외주 vehicle volume objective, solver의 two-state outcome과 final audit가 확정되었다. 충돌하는 finite-penalty·외주/이월 status 예시는 [Master §9~§10](../master-design.md) 및 [등록부](../master-design-open-questions.md)의 결정으로 대체한다.

이 문서는 향후 구현 설계를 명확히 하기 위한 세션 산출물이다. 실제 코드와 `master-design.md`는 이 세션에서 변경하지 않는다.

## 문제

현재 설계가 제시한 다음 분리는 올바른 출발점이다.

```text
Route
  → RoutePropagator
  → RouteMetrics
  → ScorePolicy
  → CostFunction
```

다만 현재 `RouteMetrics`는 거리, 운전시간, 대기시간, 서비스시간, 경로 지속시간, 정차 수, 사용 일수, 최대 적재량 정도만 가진다. 이 정보만으로는 다음 요구를 정확하게 계산할 수 없다.

- 실제 야간 운전 구간에만 부과하는 야간 운행비
- 고객 방문 시각 또는 서비스 구간에 따라 달라지는 시간대 비용
- 주문별 우선순위에 따른 미배정 페널티
- 특정 주문의 지연, 대기, 서비스 수준에 따른 비용
- 차량을 한 번 사용했을 때, 하루 사용했을 때, 교대조마다 부과하는 고정비
- 경로 비용과 별개인 해 전체 비용 또는 다단계 목적

반대로 필요한 정보가 생길 때마다 `RouteMetrics`에 고객사 전용 필드를 추가하면 코어 모델이 고객사 요구에 따라 계속 변경된다. `ScorePolicy`가 `Route.seq`를 직접 순회하도록 허용하면 시간 전파 규칙이 정책마다 중복되고, 탐색 중 증분 점수와 최종 재계산 점수가 달라질 수 있다.

현재 `SiteProfile`이 이미 생성된 `ScorePolicy`, `RouteConstraint`, `RoutePropagator` 인스턴스를 장기간 보유하는 구조도 보완이 필요하다. 예를 들어 `priorityPenalty[requestId]`는 특정 `ProblemInstance`의 내부 ID에 바인딩된다. 이를 다른 계획이나 병렬 실행에서 재사용하면 데이터가 섞인다.

따라서 해결해야 할 핵심 문제는 다음과 같다.

1. 경로 순서를 가격 정책이 직접 재해석하지 않아도 필요한 사실을 얻을 수 있어야 한다.
2. 고객사별 비용과 제약을 작은 구성요소의 조합으로 추가할 수 있어야 한다.
3. `requestId`, `vehicleId`, 시간축과 결합된 데이터는 문제 단위로 격리해야 한다.
4. 경로 비용, 주문 비용, 미배정 비용, 해 전체 비용의 책임과 중복 계산 방지 규칙이 필요하다.
5. 프로필 선택과 버전을 재현 가능하게 관리해야 한다.
6. 성능 최적화 후에도 전체 재계산과 증분 평가가 같은 값을 내야 한다.

## 설계 원칙

### 1. 전파는 사실을 만들고, 정책은 사실에 가격을 매긴다

`RoutePropagator`는 시간·거리·적재의 물리적 진행 의미만 책임진다. 고객사 요율, 우선순위 가중치, 페널티는 알지 못한다.

```text
RoutePropagator: 무엇이 언제 발생했는가
MetricContributor: 정책에 필요한 중립적 측정값은 무엇인가
ScorePolicy: 그 측정값의 가격 또는 목적값은 얼마인가
RouteConstraint: 그 사실이 실행 가능한가
```

예를 들어 야간 운행비가 있더라도 `RoutePropagator`가 야간 요율을 계산하지 않는다. 전파 과정에서 확정된 각 이동 구간의 출발·도착 시각을 바탕으로 `nightDriveSeconds`라는 사실을 계산하고, 정책이 그 시간에 요율을 곱한다.

### 2. `ScorePolicy`는 원시 경로를 받지 않는다

`ScorePolicy`와 그 하위 비용 구성요소에는 `Route`, `int[] seq`, mutable cache를 전달하지 않는다. 정책이 받을 수 있는 입력은 다음으로 제한한다.

- 불변 `RouteEvaluation`
- 불변 `RequestMetrics`
- 불변 `SolutionMetrics`
- 문제에 바인딩된 불변 `EvaluationContext`

정책이 직접 노드 순서를 읽어 도착시각이나 야간 구간을 다시 계산하는 우회로를 만들지 않는다. 새 정책에 필요한 사실이 없다면 측정 확장 지점에 추가한다.

### 3. 공통 사실과 고객사 파생 사실을 분리한다

모든 고객사가 사용하는 물리적 집계는 `RouteMetrics`에 둔다. 일부 고객사만 필요한 사실은 등록된 `MetricContributor`가 계산하여 `MetricSnapshot`에 둔다.

```text
공통 RouteMetrics
- distance
- driveTime
- waitTime
- serviceTime
- routeDuration
- stopCount
- maxLoadWeight / maxLoadVolume

고객사별 MetricSnapshot 예
- nightDriveSeconds
- peakTimeServiceCount
- priority1ServedCount
- tollRoadDistance
```

이 구분으로 고객사 요구 하나 때문에 공통 `RouteMetrics` record와 모든 호출부를 계속 수정하는 일을 피한다. 여러 고객사가 반복해서 사용하는 사실은 충분히 검증한 뒤 공통 지표로 승격할 수 있다.

### 4. 실행 가능성, 비용, 목적 순서를 분리한다

- `RouteConstraint`: hard constraint의 통과/실패와 진단을 반환한다.
- `ScorePolicy`: feasible 해 사이의 비용 또는 목적값을 계산한다.
- `ObjectivePlan`: 여러 목적 차원의 비교 순서와 단계별 고정 조건을 정의한다.

soft constraint는 `RouteConstraint`가 실패를 반환하는 대신 위반량을 측정값으로 만들고, 해당 고객사의 비용 구성요소가 페널티를 부과한다. 같은 규칙을 hard constraint와 soft penalty 양쪽에서 동시에 적용하지 않는다.

고객별 목적 우선순위가 다르므로 코어에 `미배정 수 → 차량 수 → 거리` 순서를 고정하지 않는다. 프로필이 목적 차원의 스키마와 비교 순서를 제공한다.

### 5. 비용은 합성하되 계산 소유권은 하나만 둔다

각 비용 항목은 다음 중 한 범위만 소유한다.

- 경로 범위: 거리비, 운전시간비, 차량/일별 활성 비용
- 배정 주문 범위: 서비스 시간대 비용, 주문별 SLA 비용
- 미배정 주문 범위: 우선순위별 미배정 페널티
- 해 전체 범위: 전체 차량 수 구간요금, 최소 서비스율 보너스·페널티

한 항목을 두 범위에서 중복 계산하지 않는다. 예를 들어 차량 고정비가 경로 비용이면 해 전체 비용에서 다시 차량 수를 곱하지 않는다.

### 6. 프로필은 팩토리이고 실행 컨텍스트는 문제에 바인딩한다

장기 수명의 `SiteProfile` 또는 `SiteProfileProvider`는 설정과 팩토리만 보유한다. 내부 `requestId`에 맞춘 배열, 시간대 인덱스, 차량별 요율표는 변환이 끝난 뒤 해당 `ProblemInstance`에 바인딩하여 생성한다.

```text
장기 수명: ProfileRegistry, SiteProfileProvider, 불변 설정
문제 수명: BoundSiteProfile, EvaluationContext, request/vehicle별 배열
평가 수명: contributor accumulator, 임시 propagation state
```

### 7. 모든 결과는 분해 가능해야 한다

최종 목적값 하나만 반환하지 않고 항목별 `ScoreBreakdown`을 함께 재구성할 수 있어야 한다.

```text
총점 148,000
├─ 거리비 42,000
├─ 차량 고정비 30,000
├─ 야간 운전비 16,000
└─ 미배정 페널티 60,000
```

탐색의 hot path에서는 압축된 숫자 배열을 사용해도 되지만, 최종 결과와 진단에서는 같은 스키마로 사람이 읽을 수 있는 이름을 복원한다.

## 권장 구성요소

아래 인터페이스는 구현 방향을 설명하기 위한 의사 계약이며 이 세션에서 실제 코드를 만들지 않는다.

### 1. `PropagationFacts`

`RoutePropagator`가 한 번의 전방 계산으로 확정하는 정책 중립적인 사실이다.

```text
PropagationFacts
├─ RouteMetrics
├─ LegFacts
└─ VisitFacts
```

`LegFact`의 최소 정보:

- 출발 노드 ID와 도착 노드 ID
- 출발시각과 도착시각
- 거리와 실제 운전시간
- 사용 차량 ID

`VisitFact`의 최소 정보:

- 노드 ID, 요청 ID, 노드 종류
- 도착, 서비스 시작, 서비스 종료 시각
- 대기시간
- 서비스 직후 적재량

이 사실은 전파가 이미 확정한 결과다. 정책이 이동 행렬이나 시간창을 다시 읽어 같은 의미를 재계산하지 않는다.

모든 후보 평가에서 객체를 과도하게 생성하지 않도록 구현 시에는 배열 또는 전파 이벤트 형태를 사용할 수 있다. 중요한 계약은 표현 방식이 아니라 동일한 전파 결과에서 나온 읽기 전용 사실이라는 점이다.

### 2. `RouteMetrics`

모든 고객사가 공통으로 사용하는 경로 집계값을 유지한다. 기존 필드 외에 다음 공통 사실은 명시할 가치가 있다.

- `startTimeSec`, `endTimeSec`
- 경로가 비어 있는지 또는 차량이 활성인지
- 사용한 근무일/교대조 수를 정확히 정의한 값

단, `nightCost`, `priorityPenalty`처럼 가격 또는 고객사 의미가 들어간 값은 두지 않는다. `daysUsed`도 단순 날짜 수인지 과금 일수인지 구분하고, 과금 일수는 필요한 경우 파생 지표로 계산한다.

### 3. `RequestMetrics`

경로 안의 각 요청에 대해 전파 결과로 확인된 사실을 요청 단위로 묶는다.

```text
RequestMetrics
- requestId
- vehicleId
- pickupArrival / pickupServiceStart / pickupServiceEnd
- deliveryArrival / deliveryServiceStart / deliveryServiceEnd
- attributedWaitTime
- assigned
```

배송 전용 변환 모델이면 존재하지 않는 pickup 필드는 명시적인 미존재 상태를 사용한다. `priority`, `미배정 페널티 요율`은 측정값이 아니라 `EvaluationContext`의 정책 입력 데이터다.

`RequestMetrics`는 다음 요구에 사용한다.

- 특정 요청의 서비스 시작 시간대에 따른 비용
- 요청별 SLA 또는 soft lateness 비용
- 배정 주문의 서비스 수준 집계
- 결과 DTO의 주문별 시간 복원

미배정 요청에는 경로 전파 결과가 없으므로 별도 가짜 `RequestMetrics`를 만들지 않는다. 미배정 여부는 `RequestBank`와 `SolutionMetrics`에서 다룬다.

### 4. `MetricContributor`

프로필에 등록된 파생 지표 계산기다. 가격을 계산하지 않고 사실만 집계한다.

```text
MetricContributorFactory.bind(EvaluationContext)
    → BoundMetricContributor

BoundMetricContributor
    - 전파 시작 시 accumulator 생성
    - LegFact 수신
    - VisitFact 수신
    - RouteMetrics와 함께 finish
    → MetricSnapshot
```

예:

- `TimeBandDriveMetricContributor`: 각 시간대와 겹치는 실제 운전 초
- `TimeBandServiceMetricContributor`: 시간대별 서비스 건수 또는 초
- `RequestServiceMetricContributor`: 요청별 SLA 위반량
- `VehicleActivationMetricContributor`: 사용 일수·교대조 수

`MetricContributor`는 경로의 원시 `seq`를 받지 않는다. 전파가 생성한 leg/visit 사실만 받기 때문에 시간 진행의 의미를 복제하지 않는다.

필요한 지표 목록은 프로필 바인딩 시 확정한다. 등록되지 않은 지표를 정책이 요구하면 solve 시작 전에 설정 오류로 실패시킨다.

### 5. `MetricSnapshot`과 `MetricSchema`

고객사마다 다른 파생 지표를 공통 record 필드로 계속 추가하지 않기 위한 구조다.

```text
MetricSchema
- metric key
- 단위
- 집계 방식
- 설명
- 내부 slot

MetricSnapshot
- schema ID
- long[] values
```

문자열 map은 프로필 구성과 결과 설명에만 사용하고, 반복 평가의 hot path에서는 바인딩 시 결정된 slot의 `long[]`을 사용한다. 서로 다른 스키마의 snapshot을 섞으면 오류로 처리한다.

### 6. `RouteEvaluation`

경로 한 개에 대한 단일 진실이다.

```text
RouteEvaluation
├─ feasibility / violations
├─ RouteMetrics
├─ RequestMetrics view
└─ MetricSnapshot
```

초기 구현은 정확성을 위해 전체 경로를 다시 전파하여 이 결과를 만든다. 이후 증분 캐시를 도입하더라도 최종 전체 재계산으로 동일한 `RouteEvaluation`을 얻을 수 있어야 한다.

### 7. 문제에 바인딩된 `EvaluationContext`

다음 데이터를 한 문제 실행에 묶은 불변 컨텍스트다.

```text
EvaluationContext
├─ problemId / problem fingerprint
├─ profileKey / profileVersion / configHash
├─ TimeContext와 계획 시간 범위
├─ requestId별 priority, penalty, SLA 데이터
├─ vehicleId별 요율·차종·과금 데이터
├─ 정규화된 시간대 구간 인덱스
├─ MetricSchema
└─ ObjectiveSchema
```

배열은 `ProblemInstance`의 dense ID와 같은 인덱스를 사용한다. 생성 시 길이, ID 범위, 단위, 정렬, 중복 시간대와 필수 지표 의존성을 모두 검증한다. 생성 후에는 변경하지 않는다.

`ProblemInstance`에는 최적화에 공통적인 물리 데이터만 두고, 고객사 전용 정책 배열은 `EvaluationContext`에 둔다.

### 8. 합성 가능한 `ScorePolicy`

하나의 거대한 고객사 클래스 대신 작은 비용 구성요소를 합성한다.

```text
CompositeScorePolicy
├─ RouteScoreComponent[]
├─ AssignedRequestScoreComponent[]
├─ UnassignedRequestScoreComponent[]
└─ SolutionScoreComponent[]
```

각 구성요소는 자신이 소유한 범위의 불변 사실과 `EvaluationContext`만 읽고 `ScoreContribution`을 반환한다.

```text
ScoreContribution
- objective dimension
- value
- breakdown key
```

권장 기본 구성요소 예:

- `DistanceCostComponent`
- `DriveTimeCostComponent`
- `WaitingCostComponent`
- `VehicleFixedCostComponent`
- `NightDriveCostComponent`
- `PriorityUnassignedPenaltyComponent`
- `MinimumServiceLevelComponent`

구성요소의 평가 순서는 결과에 영향을 주지 않아야 한다. 동일 목적 차원에 대한 값은 검증된 안전 합산 규칙으로 더한다. 설명용 breakdown key의 중복은 프로필 바인딩 시 거부한다.

### 9. `ObjectiveSchema`, `ObjectiveScore`, `ObjectivePlan`

고객별 목적 우선순위도 구성으로 분리한다.

```text
ObjectiveSchema
- UNASSIGNED_COUNT
- WEIGHTED_UNASSIGNED_PENALTY
- USED_VEHICLE_COUNT
- TOTAL_COST

ObjectiveScore
- schema에 맞춘 long[] dimensions

ObjectivePlan
- 차원 비교 순서
- 각 단계에서 고정할 상위 차원
- stage별 허용 오차 또는 정확 일치 규칙
```

예를 들어 고객사 A는 `필수 미배정 → 가중 미배정 → 차량 수 → 비용` 순서를, 고객사 B는 `가중 총비용` 하나를 사용할 수 있다. ALNS 수용 판단, best 해 비교, 병렬 seed 결과 비교, 최종 결과 선정이 모두 같은 `ObjectivePlan`을 사용해야 한다.

### 10. 합성 가능한 `RouteConstraint`

```text
CompositeRouteConstraint
├─ CorePhysicalConstraints
├─ customer constraint A
├─ customer constraint B
└─ ...
```

각 제약은 다음 셋 중 필요한 가장 작은 입력만 선언한다.

- `RouteMetrics`만 필요
- 특정 `MetricSnapshot` 지표 필요
- 순서 의존 상세 사실인 `PropagationFacts` 필요

제약은 가격을 반환하지 않고 통과 또는 구조화된 위반 결과를 반환한다. 일반 탐색에서는 비용이 싼 순서로 단락 평가하고, 진단 모드에서는 모든 위반을 수집할 수 있다.

요청-차량 호환성처럼 변환 단계에서 비트셋으로 컴파일 가능한 규칙은 계속 데이터 기반 사전 필터에 둔다. 실행형 `RouteConstraint`는 경로 전체 상태가 있어야 판단할 수 있는 규칙에만 사용한다.

### 11. `RoutePropagator`

기본 전파와 멀티트립처럼 경로 진행 의미가 정말 달라질 때만 교체한다. 비용 항목이 하나 추가됐다는 이유로 새 propagator를 만들지 않는다.

모든 propagator 구현은 동일한 `LegFact`/`VisitFact` 의미를 만들어야 한다. 그래야 같은 metric contributor와 score component를 재사용할 수 있다. 커스텀 propagator가 특정 사실을 제공하지 못하면 바인딩 시 의존성 검증에서 거부한다.

### 12. `SiteProfileProvider`, `BoundSiteProfile`, `ProfileRegistry`

기존 `SiteProfile`의 역할을 정의와 실행 인스턴스로 나눈다.

```text
SiteProfileProvider                   장기 수명, 상태 없음
├─ profile key와 지원 version
├─ transformer factory
├─ propagator factory
├─ metric contributor factories
├─ score component factories
├─ constraint factories
└─ objective plan factory

BoundSiteProfile                     문제 수명, 불변
├─ EvaluationContext
├─ RoutePropagator
├─ bound contributors
├─ CompositeScorePolicy
├─ CompositeRouteConstraint
└─ ObjectivePlan
```

`ProfileRegistry`는 `(profileKey, version)`으로 provider를 찾는다. 다음 규칙을 둔다.

- 모르는 key/version은 solve 전에 명확히 실패한다.
- 암묵적으로 최신 버전으로 바꾸지 않는다.
- 실행 결과에 profile key, version, config hash를 기록한다.
- registry에는 문제별 배열이나 mutable cache를 넣지 않는다.
- 신규 고객사는 provider 등록으로 추가하고 ALNS 코어의 조건문은 수정하지 않는다.

고객사 모듈을 별도로 추가할 계획이라면 코어의 `sealed interface permits ...` 목록에 고객사 타입을 계속 추가하는 방식은 피한다. 비즈니스 입력 확장 타입은 고객사 transformer 경계 안에 두거나, 코어 수정 없이 provider가 추가될 수 있는 비밀봉 확장 계약을 사용한다.

## 평가 흐름

### 1. 프로필 해석과 문제 바인딩

```text
입력의 site/profile key + 명시적 version
  → ProfileRegistry.resolve
  → SiteProfileProvider 선택
  → 입력 검증 및 ProblemTransformer 실행
  → ProblemInstance 생성
  → EvaluationContext 생성
  → contributor/score/constraint/objective 의존성 검증
  → BoundSiteProfile 생성
```

이 단계가 끝나면 탐색 도중 설정 lookup, 문자열 key 해석, 고객사 분기문을 수행하지 않는다.

### 2. 경로 평가

```text
Route + SolverVehicle
  → RoutePropagator의 단일 전방 계산
  → RouteMetrics + LegFact + VisitFact
  → MetricContributor들이 필요한 파생 지표 집계
  → RequestMetrics와 MetricSnapshot 완성
  → CompositeRouteConstraint 검사
  → feasible이면 CompositeScorePolicy의 경로·배정요청 비용 평가
  → RouteEvaluation + RouteScore 반환
```

hard constraint가 실패한 후보는 정상적인 탐색용 infeasible 결과로 반환한다. 비용 정책은 feasibility를 바꾸지 않는다.

### 3. 해 전체 평가

```text
활성 경로의 route contribution 합
+ 배정 요청 contribution 합
+ RequestBank의 각 요청에 대한 unassigned contribution
+ SolutionMetrics 기반 solution contribution
  → ObjectiveScore
  → ObjectivePlan으로 비교
```

미배정 페널티는 `RequestBank`를 기준으로 요청당 정확히 한 번만 계산한다. 경로에서 제거되거나 삽입될 때 해당 요청의 배정 비용과 미배정 비용 사이의 상태 전환을 함께 반영한다.

### 4. 삽입·제거의 증분 평가

초기 구현에서는 변경 전·후 경로를 전체 재평가한다.

```text
delta
= after route contributions - before route contributions
+ 요청 assignment 상태 변화 contribution
+ solution-level contribution 변화
```

이후 캐시나 undo-log를 도입해도 동일한 구성요소와 `ObjectiveSchema`를 사용한다. 증분 전용 별도 가격식을 만들지 않는다.

### 5. 최종 검증과 결과 설명

선택된 최종 해는 캐시를 사용하지 않는 전체 전파·전체 점수 계산으로 다시 검증한다. 결과에는 다음을 함께 남긴다.

- `ObjectiveScore`의 모든 차원
- 사람이 읽을 수 있는 `ScoreBreakdown`
- 사용한 profile key/version/config hash
- 지표 및 비용의 단위
- 미배정 요청별 페널티와 정책상 사유
- hard constraint 진단 결과

## 확장 예시

### 1. 실제 야간 운전시간에 대한 비용

요구사항:

```text
22:00~06:00에 실제 운전한 시간 × 야간 초당 요율
```

구성:

```text
EvaluationContext
- 계획 시간축에 펼쳐진 야간 구간 인덱스
- vehicleId별 야간 요율

TimeBandDriveMetricContributor
- 각 LegFact의 [departure, arrival)와 야간 구간의 교집합 초를 합산
→ NIGHT_DRIVE_SECONDS

NightDriveCostComponent
- NIGHT_DRIVE_SECONDS × 차량별 요율
```

`daysUsed - 1`을 야간 운전비의 근사치로 쓰지 않는다. 휴식·대기·서비스가 야간에 있었다는 이유로 운전비가 붙지도 않는다. 다만 고객이 “야간에 경로가 활성 상태이면 부과”를 원한다면 별도 지표와 별도 비용 구성요소로 표현한다.

### 2. 우선순위 주문의 미배정 페널티

요구사항:

```text
P1 주문 미배정: 100,000
P2 주문 미배정: 30,000
P3 주문 미배정: 5,000
```

구성:

```text
EvaluationContext
- priorityByRequestId[]
- unassignedPenaltyByRequestId[]

PriorityUnassignedPenaltyComponent
- RequestBank의 requestId로 penalty 배열 조회
→ WEIGHTED_UNASSIGNED_PENALTY 차원 또는 TOTAL_COST 차원
```

P1 주문을 반드시 배정해야 한다면 매우 큰 수를 넣어 흉내 내지 않는다. `mandatoryUnassignedCount` 같은 상위 목적 차원으로 두거나, 해당 문제를 infeasible로 판단하는 명시적 규칙을 선택한다.

### 3. 고객별 서로 다른 목적 순서

고객사 A:

```text
1. 필수 주문 미배정 수
2. 가중 미배정 페널티
3. 사용 차량 수
4. 총비용
```

고객사 B:

```text
1. 전체 가중비용
  = 거리비 + 차량비 + 야간비 + 미배정비
```

차이는 `ObjectiveSchema`, `ObjectivePlan`, score component 조합에만 있다. 경로 전파, ALNS 연산자, 해 표현은 그대로 유지한다.

### 4. 차량 고정비와 경로 전체 비용

차량을 한 번이라도 쓰면 비용이 발생하는 경우:

```text
RouteMetrics.active
  → VehicleFixedCostComponent
  → 차량당 1회 비용
```

하루마다 비용이 발생하는 경우:

```text
VehicleActivationMetricContributor
  → ACTIVE_BILLING_DAYS
  → DailyVehicleCostComponent
```

여러 경로가 같은 물리 차량을 공유할 수 있는 모델이라면 차량당 중복 과금을 막기 위해 경로가 아니라 `SolutionMetrics` 범위에서 계산한다. 비용의 과금 단위가 route, vehicle, day, shift, trip 중 무엇인지 프로필 설정에 명시한다.

### 5. 새로운 고객사의 통행료 도로 비용

경로 순서를 정책이 읽게 하지 않는다.

```text
TollRoadMetricContributor
- LegFact의 arc ID 또는 사전 계산된 arc 속성 사용
→ TOLL_DISTANCE 또는 TOLL_AMOUNT_BASE

TollCostComponent
- 지표에 고객사 요율 적용

SiteProfileProvider
- 두 구성요소 등록
```

ALNS 코어, `Route`, 공통 `RouteMetrics`는 변경하지 않는다.

### 6. 새로운 경로 제약

“특정 위험물 주문을 실은 상태에서는 터널 구간을 통과할 수 없다”는 비용이 아니라 순서·상태 의존 hard constraint다.

```text
HazmatTunnelConstraint
- PropagationFacts와 문제별 위험물 배열 조회
- 위반 arc와 requestId를 구조화된 violation으로 반환
```

이 제약 때문에 이동이나 적재 전파 자체의 의미가 바뀌지 않는다면 `RoutePropagator`를 교체하지 않는다.

## 동시성·수명

### 수명 구분

| 구성요소 | 권장 수명 | 상태 규칙 |
|---|---|---|
| `ProfileRegistry` | 애플리케이션 | 등록 후 불변, provider만 보유 |
| `SiteProfileProvider` | 애플리케이션 | 상태 없는 팩토리 또는 불변 설정 |
| `ProblemInstance` | 한 solve 입력 | 불변 |
| `BoundSiteProfile` | 한 문제 | 불변 |
| `EvaluationContext` | 한 문제 | 문제 ID에 바인딩된 불변 배열 |
| `RoutePropagator` | 한 문제 또는 평가기 | 공유하려면 무상태·불변이어야 함 |
| `CompositeScorePolicy` | 한 문제 | 순수 함수, 불변 |
| `CompositeRouteConstraint` | 한 문제 | 불변; mutable memoization은 별도 격리 |
| contributor accumulator | 한 경로 평가 | thread-confined, 평가 후 폐기 또는 안전한 풀 반환 |
| route cache/undo state | 한 탐색 실행 | 다른 seed/thread와 공유 금지 |

같은 `ProblemInstance`와 완전히 불변인 `BoundSiteProfile`은 여러 seed 실행이 읽기 전용으로 공유할 수 있다. 단, 적응형 operator 가중치, route cache, 임시 accumulator, 진단 수집기는 seed마다 분리한다.

### 캐시 규칙

- 문제별 dense ID 배열을 전역 static cache에 저장하지 않는다.
- 캐시 키에는 최소한 problem fingerprint, profile version/config hash, vehicle ID, route signature가 포함되어야 한다.
- 파생 지표 캐시는 그것을 만든 `MetricSchema`와 함께 검증한다.
- 고객사 제약의 memoization이 필요하면 `BoundSiteProfile` 내부의 무제한 mutable map으로 두지 않는다. 탐색 실행별 제한된 캐시 또는 명시적인 동시성 안전 캐시를 사용한다.
- 최종 결과는 캐시와 독립적인 전체 재계산으로 검증한다.

### 재현성 규칙

결과 재현의 입력에는 다음이 포함된다.

```text
ProblemInstance fingerprint
+ profile key/version/config hash
+ ObjectiveSchema/MetricSchema version
+ solver 설정
+ seed
```

프로필의 `latest` 별칭만 저장해서는 과거 결과를 재현할 수 없다. 실행 시 해석된 정확한 버전을 결과에 고정한다.

## 테스트 계약

### 1. 전파 사실 정확성

- 각 leg의 출발·도착 시각 합이 경로 전파 결과와 일치한다.
- 각 visit의 도착, 서비스 시작, 서비스 종료, 대기시간이 시간창 규칙과 일치한다.
- pickup과 delivery의 `RequestMetrics`가 같은 requestId에 정확히 결합된다.
- 경로 집계값은 leg/visit 사실을 독립 합산한 값과 일치한다.

### 2. 야간 시간대 경계

- 야간 시작 직전·정확히 시작·정확히 종료 시각을 검증한다.
- 자정을 넘는 이동 구간의 교집합을 검증한다.
- 여러 계획일과 여러 야간 구간을 통과하는 경로를 검증한다.
- 대기·서비스 시간은 “야간 운전시간”에 포함되지 않음을 검증한다.
- 시간대 구간은 `[start, end)` 등 하나의 경계 규칙으로 통일한다.

### 3. 비용 구성요소 단위 테스트

- 거리, 대기, 야간, 차량 고정비를 각각 독립적으로 검증한다.
- 요율 0인 구성요소는 점수에 영향을 주지 않는다.
- 구성요소 등록 순서를 바꿔도 동일한 `ObjectiveScore`가 나온다.
- overflow 상한과 합산 정책을 경계값에서 검증한다.
- breakdown 합이 해당 목적 차원의 총값과 일치한다.

### 4. 미배정과 우선순위

- 각 미배정 requestId에 페널티가 정확히 한 번 적용된다.
- 배정된 요청에는 미배정 페널티가 적용되지 않는다.
- 배정 ↔ 미배정 이동의 증분 값이 전체 재계산 차이와 같다.
- 같은 미배정 건수라도 우선순위가 다른 해가 프로필의 목적 순서대로 비교된다.
- mandatory와 finite penalty의 의미가 섞이지 않는다.

### 5. 정책과 제약의 격리

- 같은 `RouteEvaluation`에 다른 `ScorePolicy`를 적용하면 feasibility는 변하지 않는다.
- 같은 비용 정책에 `RouteConstraint`를 추가하면 비용 계산식 자체는 변하지 않는다.
- 정책이 `Route`나 mutable cache에 접근할 수 없는 API 계약을 유지한다.
- soft constraint는 hard rejection과 penalty에 동시에 중복 적용되지 않는다.

### 6. 합성 계약

- 필요한 metric key가 없으면 profile bind 시 실패한다.
- 같은 breakdown key 또는 목적 차원의 잘못된 단위 조합은 bind 시 실패한다.
- 서로 다른 `MetricSchema`의 snapshot 결합을 거부한다.
- route/request/unassigned/solution 범위별 비용이 중복되지 않는 fixture를 유지한다.

### 7. 전체 재계산 동등성

초기해, 삽입, 제거, 교환, destroy/repair, rollback 이후 각각 다음을 검증한다.

```text
증분 ObjectiveScore
= 캐시를 버리고 전체 경로와 해를 다시 평가한 ObjectiveScore
```

모든 병렬 seed의 최종 후보 비교도 동일한 `ObjectivePlan`을 사용한다.

### 8. 프로필 격리와 동시성

- 서로 다른 문제의 `priorityPenalty[]`가 같은 provider를 통해 생성되어도 섞이지 않는다.
- 같은 문제의 병렬 seed가 동일한 점수를 계산한다.
- 한 프로필의 설정 변경이 이미 생성된 `BoundSiteProfile`에 영향을 주지 않는다.
- 미등록 버전, 설정 hash 불일치, 배열 길이 불일치를 solve 시작 전에 발견한다.

### 9. 회귀 fixture

최소한 다음 프로필 fixture를 유지한다.

1. 거리만 사용하는 기본 프로필
2. 거리 + 차량 고정비 프로필
3. 야간 운전비 프로필
4. 우선순위별 미배정 페널티 프로필
5. 위 항목을 모두 합성한 프로필
6. 동일 사실에 서로 다른 목적 순서를 적용하는 두 프로필

각 fixture는 최종 점수뿐 아니라 `ScoreBreakdown`, 미배정별 비용, profile version까지 고정한다.

## 사용자에게 남길 질문

아래 항목은 구조 설계를 막지는 않지만 실제 프로필 계약을 확정하려면 답이 필요하다. 답이 정해지기 전의 권장 기본 가정도 함께 적는다.

1. **야간비의 과금 대상은 무엇인가?** 실제 운전시간의 야간 구간, 야간 서비스, 야간에 걸친 전체 경로, 또는 이들의 조합 중 어느 것인가? 기본 가정은 “각 이동 leg에서 야간 구간과 겹친 실제 운전 초”이다.
2. **야간 시간대와 경계는 고객사·요일·공휴일별로 달라지는가?** 기본 가정은 계획의 시간대 기준으로 매일 반복되는 `[22:00, 06:00)` 구간이며, 향후 달력별 구간을 `EvaluationContext`에서 펼칠 수 있게 한다.
3. **주문 우선순위는 유한한 미배정 비용인가, 반드시 배정해야 하는 계층인가?** 기본 가정은 우선순위별 유한 페널티이고, 필수 주문이 존재하면 별도의 상위 목적 차원으로 둔다.
4. **미배정, 이월, 외주를 같은 상태와 비용으로 볼 것인가?** 기본 가정은 서로 다른 assignment status와 별도 비용 구성요소로 설계하되, 첫 구현에서는 `ASSIGNED`와 `UNASSIGNED`만 활성화한다.
5. **차량 고정비의 과금 단위는 무엇인가?** 물리 차량 1대, 활성 경로 1개, 일, 교대조, trip 중 하나를 정해야 한다. 기본 가정은 “활성 물리 차량당 solve 전체에서 1회”이다.
6. **고객별 목적은 가중합과 사전식 단계 중 무엇을 허용해야 하는가?** 기본 가정은 둘 다 지원하되, 한 프로필에서 비교 의미가 명시된 하나의 `ObjectivePlan`만 사용한다.
7. **최종 결과에 비용 breakdown을 어느 수준까지 노출해야 하는가?** 경로별, 주문별, 미배정 주문별, 해 전체 중 필요한 범위를 정해야 한다. 기본 가정은 해 전체와 경로별 항목, 그리고 미배정 주문별 페널티를 제공한다.
8. **프로필 버전은 입력이 명시하는가, 사이트 설정이 선택하는가?** 재현성을 위해 실행 결과에는 항상 해석된 정확한 버전과 설정 hash를 기록해야 한다. 기본 가정은 입력 또는 작업 메타데이터가 명시적 버전을 제공하고, 미지정 시 사용한 기본 버전을 결과에 고정한다.
