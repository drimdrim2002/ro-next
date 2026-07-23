# 세션 15 — 초기해 포트폴리오

> **세션 30 통합 상태 (2026-07-23):** Comparator상 `best_initial_solution`은 항상 ALNS warm-start에 포함하고 diverse 후보는 추가 집합이라는 의미가 확정되었다. 그러나 scorer 공식, randomized start 수, diverse `K`, light-search budget의 본문 임시값은 승인된 default가 아니다. `Q-ALG-01`은 calibration/approval protocol만 확정된 `OPEN — EXPERIMENT_REQUIRED`이며 실제 수치 전에는 explicit experiment/test config만 허용한다. [Master §11.2](../master-design.md#112-현재-범위의-initial-solution-portfolio), [등록부](../master-design-open-questions.md), [세션 30](30-open-question-integration.md)을 따른다.

이 문서는 `docs/arranged/02_initial_solution_heuristics.md`의 전체 내용을 기준으로, 현재 `master-design.md`의 단일 greedy 초기해 설계를 **RPDPTW 초기해 포트폴리오**로 교체하기 위한 설계안이다. 이 세션에서는 코드와 `master-design.md`를 변경하지 않는다.

## 확정 범위

### 현재 단계에 포함한다

초기해 생성은 단일 `ConstructionHeuristic` 구현이 아니라 다음 요소를 가진 포트폴리오로 설계한다.

1. 모든 construction policy가 공유하는 전처리 결과와 삽입 evaluator
2. 서로 다른 구조를 만드는 최소 4개의 construction policy
3. 각 후보에 대한 제한된 light local search
4. 고객별 목적함수 정책이 허용하는 경우의 제한된 route elimination
5. 후보의 실행 가능성 재검증, 중복 제거, 최선해 및 다양해 선택
6. ALNS가 소비할 초기 solution set과 재현성·진단 메타데이터

`master-design.md`의 26.1절에 있는 “greedy 방식으로 초기해를 생성한다”는 설명은 위 포트폴리오 흐름으로 교체한다.

```text
RPDPTW ProblemInstance
        ↓
공통 전처리
        ↓
Construction portfolio
  ├─ Sequential / farthest
  ├─ Sequential / earliest deadline
  ├─ Parallel regret-2
  └─ Randomized regret-3
        ↓
후보별 light local search
        ↓
선택적·제한적 route elimination
        ↓
독립 SolutionVerifier
        ↓
중복 제거 + 고객 정책 기반 best/diverse 선택
        ↓
InitialSolutionSet
        ↓
ALNS warm start
```

### RPDPTW에 맞게 원문의 단위를 변경한다

원문은 CVRPTW의 `customer` 단일 삽입을 중심으로 설명하지만 프로젝트 표준 모델은 RPDPTW다. 따라서 초기해 포트폴리오의 최소 조작 단위는 `Node`나 고객 한 개가 아니라 **pickup과 delivery가 결합된 `Request`**다.

- pickup과 delivery는 같은 차량 경로에 들어간다.
- pickup은 delivery보다 앞선다.
- 두 노드는 함께 삽입·이동·제거·롤백한다.
- 요청은 정확히 한 경로 또는 `RequestBank` 한 곳에만 존재한다.
- construction policy가 위 불변조건을 자체 구현하지 않고 공통 evaluator를 사용한다.

Delivery-only 입력을 RPDPTW로 변환하는 구체적인 의미는 세션 03의 결정을 따른다. 초기해 포트폴리오는 변환 결과인 `ProblemInstance`만 소비하며, 원본 입력 유형에 따른 예외 분기를 갖지 않는다.

### 현재 단계에서 제외한다

다음은 초기해 포트폴리오의 현재 완료 조건에 포함하지 않는다.

- set covering / set partitioning MIP
- MIP에 전달할 전역 route pool 구축·정리·영속화
- ALNS 전체 탐색에서 발견한 route의 column 수집
- racing에 의한 wall-clock 기반 조기 중단과 자원 재배분
- Savings, Sweep, nearest-neighbor를 포함한 모든 후보 정책의 일괄 구현
- 인스턴스 특성 학습을 이용한 정책 자동 추천

Savings와 Sweep 등은 포트폴리오 확장 후보로 문서에 남기되, MVP 완료를 지연시키는 필수 구현으로 두지 않는다.

## MVP 포트폴리오

MVP에는 `02_initial_solution_heuristics.md`의 개발 순서가 제안한 다음 4개 정책을 포함한다.

| ID | Construction policy | 구조적 역할 | 결정성 |
|---|---|---|---|
| `SEQ_FARTHEST` | Sequential insertion + farthest seed | 먼 요청부터 경로를 완성하여 공간적 극단을 먼저 처리 | 결정적 |
| `SEQ_EARLIEST_DEADLINE` | Sequential insertion + earliest-deadline seed | 시간 제약이 급한 요청을 먼저 처리 | 결정적 |
| `PAR_REGRET_2` | Parallel regret-2 insertion | 두 번째 선택지를 잃을 위험이 큰 요청을 우선 처리 | 결정적 |
| `RAND_REGRET_3` | Randomized regret-3 insertion | regret 구조를 유지하면서 다른 지역 최적점 생성 | seed 기반 결정적 |

### 공통 construction 절차

각 정책은 요청 선택 순서만 다르게 하고 아래 수명주기는 공유한다.

```text
1. 모든 요청을 RequestBank에 둔다.
2. 사용 가능한 차량과 비어 있는 route 상태를 준비한다.
3. 정책이 다음 요청 또는 seed 요청을 선택한다.
4. 공통 evaluator로 모든 허용 route의 pickup/delivery 위치 쌍을 평가한다.
5. 정책의 순위 규칙으로 삽입 후보를 선택한다.
6. 선택한 요청 쌍을 원자적으로 적용한다.
7. 더 삽입할 수 없으면 새 route를 활성화하거나 다음 요청으로 진행한다.
8. 어떤 차량에도 넣을 수 없는 요청은 RequestBank에 남긴다.
9. 종료 후 SolutionVerifier로 전체 해를 다시 검증한다.
```

실행 불가능한 요청을 억지로 경로에 넣어 “완전 배정”으로 표현하지 않는다. 초기해가 일부 미배정 요청을 포함하는 것은 정상 상태이며, 미배정은 이후 ALNS repair의 탐색 대상이 된다.

### 정책별 설계

#### `SEQ_FARTHEST`

- 한 route를 더 이상 확장할 수 없을 때까지 구성한 후 다음 route로 이동한다.
- seed 선택은 공통 전처리가 계산한 `farthestSeedScore`를 사용한다.
- 동일 점수는 안정적인 `requestId` 순서로 결정한다.
- “먼 요청”의 정확한 RPDPTW 정의는 하단의 남은 질문에 기록한다. 정책 내부에서 좌표나 특정 거리 공식을 직접 계산하지 않고 `SeedScorer` 계약을 사용한다.

#### `SEQ_EARLIEST_DEADLINE`

- 가장 촉박한 요청을 seed로 선택하고 한 route씩 구성한다.
- 요청의 대표 deadline은 공통 전처리의 `criticalLatestStart`를 사용한다.
- pickup과 delivery의 개별 time window, 선행관계와 이동시간을 고려한 값이어야 하며 단순히 delivery의 `latest`만 읽지 않는다.
- 동일 deadline은 작은 slack, 적은 호환 차량 수, 안정적인 `requestId` 순서로 tie-break한다.

#### `PAR_REGRET_2`

- 활성화된 전체 route를 동시에 후보로 보고 요청마다 최선과 두 번째 삽입 비용을 계산한다.
- 삽입 위치 한 개가 아니라 `(vehicle, route, pickupPosition, deliveryPosition)` 조합을 하나의 insertion option으로 본다.
- 가능한 option이 하나뿐인 요청은 선택지를 잃을 위험이 가장 높은 것으로 처리한다.
- 고객별 우선순위, 시간 긴급도와 호환 차량 희소성은 하드코딩하지 않고 `ConstructionSelectionPolicy`의 보정 항목으로 제공한다.

#### `RAND_REGRET_3`

- regret-3 상위 후보 중 하나를 seed 기반으로 선택한다.
- 전역 난수나 실행 순서에 의존하지 않는다.
- 난수는 `masterSeed + policyId + runOrdinal`에서 안정적으로 파생한다.
- 같은 입력·설정·seed에서는 순차 또는 병렬 실행 여부와 무관하게 같은 후보를 생성해야 한다.
- MVP의 randomized policy 실행 횟수는 설정값이며, 기본값은 남은 질문에서 확정한다.

### 후속 portfolio 확장 후보

MVP 안정화 후 측정 결과에 따라 다음 정책을 독립적으로 추가할 수 있다.

- Savings with full RPDPTW feasibility
- Sweep / cluster-first route-second
- Randomized nearest feasible neighbor
- Regret-4 및 urgency 강화 variant
- compatibility scarcity 중심 variant
- 서로 다른 construction objective weighting variant

새 정책 추가는 공통 evaluator, `Solution`, ALNS 또는 기존 정책의 변경을 요구해서는 안 된다. 정책 등록과 설정만 추가하는 구조를 목표로 한다.

## 공통 evaluator

### 책임 분리

공통 evaluator는 “삽입할 수 있는가”와 “삽입하면 정책 중립적인 측정값이 어떻게 변하는가”를 계산한다. 특정 고객사의 목적함수 우선순위를 evaluator에 하드코딩하지 않는다.

개념적 계약은 다음과 같다.

```text
RequestInsertionEvaluator
  input:
    solution snapshot
    route
    request
    pickup position
    delivery position
    evaluation context

  output:
    feasible / infeasible
    infeasibility diagnostics
    delta RouteMetrics
    resulting RouteMetrics
    적용에 필요한 move 정보
```

역할은 다음 세 부분으로 나눈다.

1. `ConstraintEvaluator`
   - 선행관계
   - 같은 차량 배정
   - 무게·부피 용량
   - time window와 service time
   - 차량 근무 종료와 depot/terminal 도착
   - 차량 크기 유형과 지역 진입 허용
   - 고객사별 추가 hard constraint
2. `RouteMetricEvaluator`
   - 거리, 주행시간, 대기시간, 서비스시간
   - 적재량, slack 등 정책 중립적인 route 측정값
3. `ConstructionScorePolicy`
   - 위 측정값을 construction 내부의 option 순위로 변환
   - urgency, capacity pressure, compatibility scarcity와 noise를 조합

완성된 후보해의 비교는 별도의 고객별 `SolutionComparator` 또는 동등한 목적함수 정책이 담당한다. 이로써 삽입 feasibility, 삽입 선택, 완성해 선택을 서로 독립적으로 변경할 수 있다.

### 공유 범위

동일한 evaluator와 route 전파 규칙을 다음 기능이 모두 사용한다.

- 초기해 sequential insertion
- 초기해 regret insertion
- ALNS greedy/regret repair
- light local search의 move 평가
- route elimination의 재삽입
- 최종 SolutionVerifier의 전량 재계산

Phase 8의 ALNS Regret-2·3·4는 Phase 4에서 만든 regret 순위 계산과 option 열거 기능을 재사용한다. 초기해용 regret와 ALNS repair용 regret를 별도로 구현하지 않는다.

### 평가 순서

정확성을 우선하는 초기 구현은 다음 순서로 검사한다.

```text
정적 호환성·차량 사용 가능성
        ↓
pickup/delivery 위치 및 선행관계
        ↓
route 전체 시간·적재 전파
        ↓
고객사별 추가 hard constraint
        ↓
RouteMetrics 및 delta 계산
        ↓
construction score 계산
```

초기 버전은 삽입 option마다 영향받는 route 전체를 재계산한다. 증분 캐시와 slack 가속은 성능 Phase에서 전량 재계산과 동치임을 검증한 뒤 도입한다.

### 안정적인 동률 처리

재현성을 위해 모든 정책은 점수가 같은 경우의 최종 정렬 키를 명시한다.

```text
policy score
→ objective-neutral delta tuple
→ vehicle stable index
→ route stable index
→ pickup position
→ delivery position
→ request stable index
```

고객별 목적함수 정책이 별도의 tie-break 규칙을 제공하면 그 규칙을 앞에 적용하되, 마지막에는 반드시 안정적인 내부 index를 사용한다.

## 다양성·선택 방식

### 논리적 독립 실행

포트폴리오의 “병렬”은 모든 정책이 다른 정책의 mutable 상태에 의존하지 않는다는 뜻이다. MVP에서 실제 thread 병렬화는 필수가 아니다. 먼저 고정 순서의 순차 실행으로 정확성과 재현성을 확보하고, 필요하면 각 정책에 독립 seed와 독립 solution copy를 제공하여 병렬화한다.

GCP의 seed별 worker 병렬 실행과 한 worker 안의 construction 포트폴리오는 서로 다른 계층이다. 외부 batch 수와 무관하게 각 batch가 동일한 포트폴리오 계약을 사용한다.

### 후보 생성의 다양성 축

MVP는 다음 축에서 구조적 차이를 만든다.

| 다양성 축 | MVP 적용 |
|---|---|
| route 생성 방식 | sequential과 parallel |
| 요청 우선순위 | 거리 극단, deadline, regret |
| route 할당 균형 | route-completion 방식과 global option 방식 |
| 무작위성 | seed 기반 randomized top-m 선택 |
| 개선 경로 | 동일한 light local search를 서로 다른 시작 구조에 적용 |

단순히 동일 정책의 weight만 조금 바꾼 후보를 “다양한 포트폴리오”로 간주하지 않는다.

### 후보 선택 파이프라인

```text
모든 construction 후보
        ↓
light local search / 선택적 route elimination
        ↓
SolutionVerifier 전량 검증
        ↓
동일 solution fingerprint 중복 제거
        ↓
고객별 SolutionComparator로 best 선택
        ↓
허용 quality band 안에서 구조적 거리가 큰 후보를 greedy 선택
        ↓
best 1개 + diverse top-K
```

`best`는 고객별 목적함수 정책으로 선택한다. 미배정 수, 차량 수, 거리, 시간의 순서를 포트폴리오가 고정해서는 안 된다.

`diverse top-K` 선정도 상위 목적을 깨지 않는 고객별 admission policy를 먼저 통과해야 한다. 예를 들어 필수 주문 미배정이 증가한 해를 단지 구조가 다르다는 이유로 유지하면 안 된다.

### solution fingerprint와 구조 거리

차량 식별자만 다른 대칭해를 중복으로 보관하지 않도록 fingerprint를 정규화한다.

```text
각 route의 ordered request/node sequence 생성
→ route sequence들을 안정적으로 정렬
→ RequestBank의 request stable index 추가
→ 전체를 fingerprint로 변환
```

다양성 거리는 최소한 다음을 반영한다.

- 두 해의 directed route arc 차이
- route 경계의 차이
- 차량별 요청 묶음 차이
- assigned/unassigned 요청 집합 차이

초기 구현은 directed arc 집합의 Jaccard distance와 RequestBank의 symmetric difference를 조합한다. 이 값은 목적함수 값이 아니라 top-K 안에서 유사한 해만 반복 보관하지 않기 위한 선택 지표다.

### 포트폴리오 품질 불변조건

MVP에 baseline인 `SEQ_FARTHEST`가 포함되므로, 동일 evaluator와 동일 고객 비교 정책 아래 선택된 `best`는 그 baseline 후보보다 나빠서는 안 된다. 정책 하나가 실패하거나 실행 가능한 해를 만들지 못해도 다른 정책의 유효 후보를 폐기하지 않는다.

## light local search 관계

### 위치와 목적

Light local search는 ALNS를 대체하는 별도 메타휴리스틱이 아니다. construction이 만든 명백한 국소 비효율을 고정된 작은 예산 안에서 정리하여 ALNS의 warm start 품질을 높인다.

각 후보의 처리 순서는 다음과 같다.

```text
construction candidate
→ 실행 가능성 검증
→ bounded light local search
→ 선택적 bounded route elimination
→ 실행 가능성 재검증
→ portfolio candidate pool 등록
```

### MVP move 집합

`02_initial_solution_heuristics.md`가 제안한 move를 RPDPTW 요청 단위로 적용한다.

- intra-route paired relocate
- precedence-safe intra-route 2-opt
- inter-route paired relocate
- limited 2-opt*

`2-opt`와 `2-opt*`는 pickup-delivery 선행관계를 깨뜨릴 수 있으므로 “형태상 가능한 것처럼 보이는 move”를 바로 적용하지 않는다. 공통 evaluator가 요청 쌍의 동일 차량 배정, 선행관계와 전체 route feasibility를 확인한 경우만 수용한다.

### 탐색 예산과 수용 규칙

- 기본 종료는 후보별 최대 move evaluation 수 또는 최대 improvement pass 수다.
- wall-clock 시간은 안전장치로만 사용할 수 있으며 정상 회귀 테스트의 결과 결정 기준으로 사용하지 않는다.
- 기본 수용은 고객별 `SolutionComparator` 기준 strict improvement다.
- 상위 목적이 같고 하위 목적만 개선되는 move도 고객 정책의 비교 결과에 따라 수용한다.
- 동일 점수 move를 다양성 목적으로 수용할지는 별도 설정으로 둔다.
- move 적용 실패 시 해당 route와 RequestBank를 원상복구하고 다음 move를 평가한다.

모든 후보에 같은 고정 예산을 적용하는 것을 MVP 기본값으로 한다. 원문의 racing처럼 유망 후보에만 예산을 추가하는 방식은 baseline 측정 후 후속 최적화로 검토한다.

### route elimination과 Phase 12의 관계

초기해 포트폴리오 단계의 route elimination은 작은 고정 횟수의 후처리다.

```text
제거 후보 route 선택
→ 해당 route의 모든 요청 쌍을 RequestBank로 원자 이동
→ 다른 route에 regret 기반 재삽입
→ 전부 재삽입되고 고객 목적함수상 허용되면 적용
→ 아니면 전체 시도를 롤백
```

고객의 상위 목적이 차량 수가 아니더라도 route 제거 결과가 고객별 comparator에서 우수하거나 허용된 경우에만 적용한다. 포트폴리오가 차량 수 우선순위를 하드코딩하지 않는다.

기존 Phase 12의 `FleetMinimizer`는 이 primitive를 재사용해 반복 선택, 다단계 목적 고정, 더 큰 탐색 예산과 실패 이력을 추가하는 본격적인 fleet 최적화 단계로 정의한다. 즉 Phase 4의 제한적 후처리와 Phase 12의 전체 fleet minimization은 중복 구현이 아니다.

## 산출물

### 설계 계약

구체적인 클래스명은 마스터 설계 통합 시 조정할 수 있지만 책임은 다음처럼 분리한다.

```text
InitialSolutionPortfolio
  - 등록된 construction policy 실행
  - 후보 개선과 검증 orchestration
  - best/diverse 후보 선택

ConstructionPolicy
  - 요청 선택 전략
  - 공통 insertion option 중 하나를 선택

RequestInsertionEvaluator
  - 요청 쌍 삽입 실행 가능성과 delta metrics 계산

LightImprovement
  - 제한된 local move 수행

CandidateSelectionPolicy
  - 고객별 comparator와 quality admission 적용
  - 중복 제거 후 best/diverse top-K 선택

InitialSolutionSet
  - best solution
  - diverse solutions
  - 생성·검증 메타데이터
```

### `InitialSolutionSet`

ALNS에 전달하는 논리적 결과는 다음을 포함한다.

- `bestSolution`: 고객별 비교 정책에서 가장 좋은 검증 완료 해
- `diverseSolutions`: 중복이 아니며 admission policy를 통과한 최대 K개 후보
- 각 후보의 완전한 `RouteMetrics`와 solution-level metrics
- `RequestBank`와 미배정 요청 수
- construction policy ID와 버전
- master seed, 파생 seed, run ordinal
- construction 및 light improvement의 평가 step 수
- 적용된 move와 route elimination 요약
- feasibility 검증 결과
- solution fingerprint
- 설정과 solver/schema 버전

각 후보는 ALNS가 독립적으로 변경할 수 있는 상태여야 한다. 여러 ALNS run이 같은 mutable route나 RequestBank를 공유해서는 안 된다.

### ALNS 연결

ALNS 진입점은 단일 `Solution`만 받는 구조로 고정하지 않고 `InitialSolutionSet`에서 warm start를 선택하는 계약을 둔다.

- 기본 실행은 `bestSolution`을 사용한다.
- multi-start가 활성화되면 `diverseSolutions`를 별도 run의 시작점으로 사용할 수 있다.
- 어떤 후보를 어떤 GCP batch/seed에 연결했는지 결과 메타데이터에 남긴다.
- 후보 선택 방식이 달라도 ALNS의 destroy/repair 구현은 변경되지 않는다.

초기해 후보 pool은 완성해의 집합이다. 후속 MIP의 route column 집합인 route pool과 이름·책임을 명확히 구분한다.

### 관측 및 비교 산출물

각 정책과 최종 선택 결과에 대해 최소 다음을 기록한다.

- 미배정 요청 수 및 정책이 계산한 미배정 비용
- 사용 차량 수
- 전체 거리
- 전체 시간과 그 구성 항목
- construction step 수와 light improvement step 수
- 생성 후보 수, 검증 실패 수, 중복 제거 수
- source policy별 선택·탈락 사유
- best 후보와 baseline 후보의 고객별 비교 결과

이 값은 `win_poc_case.json`을 이용한 초기 품질 비교와 이후 회귀 기준에 사용할 수 있어야 한다. 구체적인 우선순위와 허용 gap은 벤치마크 세션의 결정을 따른다.

## 후속 route pool·MIP

### 현재 단계와의 경계

현재 단계는 `InitialSolutionSet`까지만 구현 대상으로 한다. 다음 항목은 후속 Phase에서 진행한다.

- 초기해와 ALNS 탐색 중 발견한 route의 수집
- ordered request sequence 기반 route key
- 동일 route의 비용·slack·출처 메타데이터 관리
- 동일 customer/request set에 대한 route pruning
- source policy 다양성을 보존하는 pool admission
- set covering / set partitioning model 생성
- MIP 결과를 RPDPTW `Solution`으로 복원하고 독립 검증

현재 단계에서 MIP solver 라이브러리나 `RoutePool` 구현을 미리 도입하지 않는다.

### 지금 보존할 확장 지점

후속 작업을 위해 현재 후보 메타데이터에는 다음만 보존한다.

- source policy와 candidate ID
- route의 ordered node/request sequence
- route metrics
- 사용 vehicle type 또는 호환성 정보
- feasibility 검증 상태

이는 현재 `Solution`과 결과 메타데이터에서 얻을 수 있는 정보여야 하며, MIP 전용 column 타입을 코어 모델에 침투시키지 않는다.

### 후속 Phase 진입 조건

route pool/MIP는 다음이 충족된 뒤 시작한다.

1. 4개 MVP construction policy가 공통 evaluator로 안정적으로 동작한다.
2. ALNS가 `InitialSolutionSet`을 소비하고 재현성 테스트를 통과한다.
3. `win_poc_case.json`에서 미배정 요청 수, 차량 수, 거리, 시간이 반복 측정된다.
4. route 및 solution verifier가 탐색 구현과 독립적으로 동작한다.
5. route 수집량과 MIP 도입 가치에 대한 baseline이 확보된다.

후속 MIP는 선택적 후처리이며, 실패하거나 시간 제한에 도달하면 검증된 ALNS best solution을 그대로 반환할 수 있어야 한다.

## Phase 및 완료 기준

현재 16개 Phase의 번호를 전부 밀기보다 Phase 4를 세분화하고 기존 Phase 2·5·8·12와 책임을 연결한다. 18개 세션을 통합하면서 전체 번호가 바뀌더라도 아래 의미적 순서는 유지한다.

### Phase 2 보강 — Evaluation Engine

주요 산출물:

- pickup-delivery request pair insertion option 열거
- 공통 `RequestInsertionEvaluator`
- 정책 중립적인 delta metrics
- 안정적인 tie-break key
- 전량 route 재계산 기반 feasibility

완료 기준:

- pickup과 delivery를 임의의 유효 위치 쌍에 평가할 수 있다.
- 동일 evaluator를 construction과 ALNS repair 테스트에서 재사용한다.
- 요청 쌍의 다른 차량 분리, delivery 선행, 중복 배정을 모두 거부한다.
- 적용 후 전량 재계산 결과가 평가 결과와 일치한다.
- 고객별 hard constraint를 추가해도 evaluator 본문을 수정하지 않는 확장 계약이 검증된다.

### Phase 4A — Construction Portfolio Core

주요 산출물:

- 공통 전처리 feature
- `SEQ_FARTHEST`
- `SEQ_EARLIEST_DEADLINE`
- `PAR_REGRET_2`
- `RAND_REGRET_3`
- policy별 독립 seed 파생

완료 기준:

- 4개 정책이 모두 `InitialSolutionCandidate`를 반환한다.
- 모든 후보에서 각 요청은 경로 또는 RequestBank 정확히 한 곳에만 있다.
- 모든 경로가 독립 SolutionVerifier를 통과한다.
- 같은 입력·설정·seed에서 후보와 메타데이터가 동일하다.
- 한 정책의 실패가 다른 정책 실행을 오염시키지 않는다.
- 정책 간 구조 차이를 유도하는 전용 fixture에서 두 개 이상의 서로 다른 fingerprint가 생성된다.

### Phase 4B — Light Improvement

주요 산출물:

- intra-route paired relocate
- precedence-safe intra-route 2-opt
- inter-route paired relocate
- limited 2-opt*
- bounded route elimination
- 안전한 실패 롤백

완료 기준:

- 수용된 move는 고객별 comparator에서 원래 후보보다 나쁘지 않다.
- 개선 전후 모든 후보가 독립 verifier를 통과한다.
- 실패한 route elimination 뒤 route와 RequestBank가 원상복구된다.
- step budget이 같으면 동일 seed에서 동일 결과를 만든다.
- 무한 반복을 방지하는 hard time guard가 있더라도 정상 테스트는 step 종료로 끝난다.

### Phase 4C — Portfolio Selection and ALNS Handoff

주요 산출물:

- solution fingerprint와 구조 거리
- 중복 제거
- 고객별 best/admission 정책
- `InitialSolutionSet`
- ALNS warm-start selector
- 정책별 진단 및 관측 결과

완료 기준:

- `bestSolution`은 입력 후보 모두보다 고객별 comparator상 열등하지 않다.
- baseline `SEQ_FARTHEST`가 성공한 경우 최종 best는 baseline보다 열등하지 않다.
- 중복 fingerprint가 하나만 남는다.
- top-K가 설정 상한을 지키고 안정적인 순서로 반환된다.
- ALNS가 best 후보를 기본 warm start로 사용할 수 있다.
- multi-start용 후보를 전달해도 mutable 상태가 공유되지 않는다.
- `win_poc_case.json` 실행에서 후보별 미배정 요청 수, 차량 수, 거리, 시간이 기록된다.

### Phase 5 — ALNS Skeleton 연계 변경

기존 Phase 5는 greedy 한 개를 직접 생성하지 않고 `InitialSolutionSet`을 입력으로 받는다.

완료 기준에 다음을 추가한다.

- 선택한 initial candidate ID와 policy ID가 결과에 남는다.
- ALNS의 최초 best가 선택한 초기해와 일치한다.
- 동일 초기해·설정·seed에서 ALNS 결과가 재현된다.
- 초기해 일부 미배정 요청을 repair가 다시 탐색할 수 있다.

### Phase 8 — Regret Insertion

Phase 8은 regret 로직을 새로 만드는 단계가 아니라 Phase 4A의 option 열거와 regret ranker를 ALNS repair 연산자로 확장하는 단계다.

- Regret-2와 Regret-3은 공통 구현 재사용
- Regret-4 추가
- noise/urgency/고객별 construction score 조합
- 부분해와 RequestBank를 대상으로 반복 repair

### Phase 12 — Fleet Minimization

Phase 4B의 bounded route elimination을 반복 가능한 `FleetMinimizer`로 확장한다.

- 고객별 상위 목적 고정
- route 선택 전략 확대
- 더 큰 step budget
- 재삽입 실패 이력 및 진단
- 안전한 candidate copy/rollback 계약 재사용

### 후속 Phase — Route Pool and MIP

Phase 번호는 전체 마스터 설계 통합 때 확정한다. 이 Phase는 현재 MVP와 별도의 다음 단계이며, 초기해 포트폴리오·ALNS·검증·벤치마크가 안정화된 뒤 수행한다.

완료 기준:

- 초기해와 ALNS에서 route column을 수집한다.
- 중복 제거와 pruning 후에도 source 다양성이 유지된다.
- MIP 출력이 독립 verifier를 통과한다.
- MIP 실패·시간 초과 시 ALNS best로 안전하게 fallback한다.
- MIP를 비활성화하면 기존 ALNS 결과와 실행 경로가 영향을 받지 않는다.

## 남은 질문

아래 질문은 현재 설계 진행을 막지 않는다. 답변 전까지는 각 항목의 임시 기본값을 사용하고, 마스터 설계 통합 시 확정한다.

1. **RPDPTW의 farthest seed 정의**
   - 후보: depot에서 pickup까지의 거리, pickup-delivery 포함 standalone route 비용, compatible vehicle 중 최소 standalone 비용의 최댓값
   - 임시안: 입력 travel matrix와 전체 pickup-delivery 경로를 반영하는 `standalone route cost`를 사용한다.
2. **earliest deadline의 대표값**
   - 후보: pickup latest, delivery latest, delivery latest에서 이동·서비스시간을 역산한 pickup 한계시각
   - 임시안: pickup과 delivery 선행관계를 반영한 `criticalLatestStart`를 전처리에서 계산한다.
3. **randomized regret-3의 실행 횟수**
   - 임시안: policy당 1회로 시작하고 `randomizedStartsPerPolicy` 설정으로 확장한다.
4. **diverse top-K 기본값과 quality band**
   - 임시안: best 외 최대 4개를 유지한다. quality band는 고객별 `CandidateSelectionPolicy`가 정의하며 전역 scalar gap을 두지 않는다.
5. **urgency bonus의 MVP 기본 활성화 여부**
   - 임시안: `RAND_REGRET_3`에서는 비활성화하고, 별도 설정 variant로 측정한 뒤 기본값을 결정한다.
6. **light local search 적용 대상**
   - 후보: 모든 후보에 동일 예산, construction만 전부 실행한 뒤 상위 후보에만 추가 예산
   - 임시안: 4개 MVP 후보 모두에 동일한 고정 step 예산을 적용한다.
7. **bounded route elimination 적용 대상**
   - 임시안: 모든 검증 완료 후보에 동일 횟수로 시도하되 고객별 comparator가 허용한 결과만 수용한다.
8. **ALNS의 diverse 후보 사용 방식**
   - 후보: 항상 best만 사용, GCP batch별 후보를 round-robin 배정, batch seed로 후보 선택, 후보별 별도 multi-start
   - 임시안: 기본은 best를 사용하고, multi-start selector는 확장 지점만 먼저 둔다.
9. **Savings와 Sweep의 도입 시점**
   - 임시안: MVP 4개 정책과 `win_poc_case.json` baseline이 안정화된 뒤, route pool/MIP보다 먼저 또는 같은 품질 개선 milestone에서 측정 기반으로 추가한다.
10. **후속 route pool/MIP Phase의 정확한 위치**
    - 후보: ALNS 기능 완성 직후, benchmark harness 이후, 성능 최적화 이후
    - 임시안: 독립 verifier와 benchmark baseline 완성 이후, 저수준 성능 최적화 전후와 무관하게 별도 선택 Phase로 시작한다.
