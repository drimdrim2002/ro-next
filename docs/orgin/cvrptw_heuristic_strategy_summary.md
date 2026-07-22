# CVRPTW 초기해 휴리스틱 개발 방향 정리

## 1. 문제 상황과 전체 개발 흐름

현재 개발하려는 대상은 **CVRPTW(Capacitated Vehicle Routing Problem with Time Windows)** 문제입니다.

구상 중인 전체 최적화 흐름은 다음과 같습니다.

```text
휴리스틱 초기해 생성
    ↓
메타휴리스틱
(ALNS + Simulated Annealing)
    ↓
MIP 후처리 / 강화
(Set Covering 또는 Set Partitioning)
```

이 흐름에서 초기 휴리스틱의 역할은 단순히 해를 하나 만드는 것이 아니라, 이후 단계의 성능에도 영향을 주는 기반이 됩니다.

초기 휴리스틱은 다음 역할을 담당합니다.

```text
1. 빠른 시간 안에 feasible initial solution 생성
2. ALNS의 warm start 제공
3. 다양한 route 구조를 생성하여 탐색 다양성 확보
4. MIP set covering / set partitioning 단계에서 사용할 route column 후보 생성
```

따라서 초기 휴리스틱은 **빠르면서도 적당히 고품질이어야 하며**, 동시에 이후 단계에 유용한 **다양한 route pool**을 만들어낼 수 있어야 합니다.

---

## 2. 핵심 질문

논의한 핵심 질문은 다음이었습니다.

> 휴리스틱 로직을 매우 정교하게 만들면 모든 경우에 대해 고품질의 초기해를 만들 수 있을까?  
> 아니면 다양한 휴리스틱 정책을 병렬로 실행하고, 그중 가장 좋은 해를 초기해로 선택하는 방식이 더 좋을까?

이에 대한 결론은 다음과 같습니다.

> **단일한 매우 정교한 휴리스틱 하나에 의존하기보다는, 여러 휴리스틱 정책을 포트폴리오 형태로 구성하고 병렬 또는 멀티스타트 방식으로 실행하는 방향이 더 적합하다.**

특히 CVRPTW는 인스턴스의 성격에 따라 좋은 construction 방식이 달라지므로, 하나의 정책이 모든 경우에 안정적으로 우수하기는 어렵습니다.

---

## 3. 단일 정교 휴리스틱의 한계

CVRPTW에서는 다음과 같은 인스턴스 특성에 따라 좋은 휴리스틱 정책이 달라집니다.

```text
- 고객들이 공간적으로 클러스터링되어 있는가?
- time window가 타이트한가?
- capacity 제약이 더 지배적인가?
- 차량 수 최소화가 중요한가?
- 총 거리 최소화가 중요한가?
- depot 주변에 고객이 몰려 있는가, 멀리 분산되어 있는가?
- 고객별 time window 폭이 균일한가, 편차가 큰가?
```

예를 들어 어떤 문제에서는 거리 중심의 savings heuristic이 좋은 route shape을 만들 수 있지만, time window가 매우 타이트한 문제에서는 regret insertion이나 urgency-aware insertion이 더 유리할 수 있습니다.

따라서 단일 휴리스틱을 아무리 정교하게 만들더라도 다음 문제가 생길 수 있습니다.

```text
1. 특정 유형의 인스턴스에는 강하지만 다른 유형에는 약할 수 있음
2. 규칙이 복잡해질수록 튜닝과 디버깅이 어려워짐
3. 특정 benchmark나 데이터 분포에 overfitting될 수 있음
4. 생성되는 route 구조가 한쪽으로 치우쳐 ALNS/MIP 단계의 다양성이 떨어질 수 있음
```

결론적으로, 단일 휴리스틱은 **baseline 또는 핵심 구성요소**로는 중요하지만, 전체 시스템의 최종 구조는 **포트폴리오 방식**이 더 적합합니다.

---

## 4. 병렬 휴리스틱 포트폴리오 방향

추천하는 방향은 다음과 같습니다.

```text
여러 construction policy를 병렬 또는 멀티스타트로 실행
    ↓
각 후보해에 가벼운 local improvement 적용
    ↓
best solution과 diverse solution을 모두 보관
    ↓
ALNS와 MIP route pool에 전달
```

중요한 점은 **가장 좋은 해 하나만 남기지 않는 것**입니다.

초기 단계에서 얻은 여러 feasible solution은 이후 단계에서 다음과 같이 활용될 수 있습니다.

```text
- 가장 좋은 해: ALNS의 기본 초기해
- 서로 다른 구조의 좋은 해들: ALNS multi-start 후보
- 각 solution의 route들: MIP set covering / set partitioning column 후보
- slack이 좋은 route들: time window repair에 유리한 후보
```

따라서 portfolio runner는 다음 두 가지를 모두 반환하도록 설계하는 것이 좋습니다.

```text
1. best_initial_solution
2. top_k_diverse_solutions + route_pool
```

---

## 5. 추천 초기해 생성 아키텍처

전체 초기해 생성 모듈은 다음과 같은 구조를 추천합니다.

```text
Input instance
    ↓
Preprocessing
    - feasible arc matrix
    - nearest neighbor list
    - time-window slack
    - demand / capacity tightness
    - customer urgency score
    ↓
Parallel construction portfolio
    - sequential insertion variants
    - parallel regret insertion variants
    - savings variants
    - sweep / cluster-first variants
    - nearest-neighbor / urgency-first variants
    - randomized variants
    ↓
Light improvement / repair
    - intra-route relocate
    - intra-route 2-opt
    - inter-route relocate
    - limited 2-opt*
    - route elimination attempt
    ↓
Candidate solution pool
    - best by objective
    - top-K diverse solutions
    - route pool for MIP
    ↓
ALNS + SA
    ↓
MIP set covering / set partitioning
```

이 구조의 핵심은 **공통 insertion evaluator** 위에 여러 policy를 얹는 것입니다.

즉, 각 휴리스틱마다 feasibility logic을 따로 만들지 말고, 다음 기능을 공통화해야 합니다.

```text
can_insert(route, customer, position)
evaluate_insert_cost(route, customer, position)
apply_insert(route, customer, position)
propagate_time(route)
check_capacity(route)
check_depot_return(route)
```

---

## 6. 포트폴리오에 넣을 휴리스틱 후보

### 6.1 Sequential Insertion 계열

Sequential insertion은 route를 하나씩 만들고, 각 route에 고객을 순차적으로 삽입하는 방식입니다.

기본 흐름은 다음과 같습니다.

```text
1. seed customer를 선택하여 새 route 시작
2. 아직 배정되지 않은 고객 중 feasible insertion 가능한 고객 탐색
3. insertion score가 가장 좋은 고객을 route에 삽입
4. 더 이상 삽입할 수 없으면 route 확정
5. 미배정 고객이 남아 있으면 새 route 생성
```

seed 선택 정책을 다양화하면 서로 다른 route 구조를 만들 수 있습니다.

```text
seed policy 후보:
- depot에서 가장 먼 고객
- latest time window가 가장 빠른 고객
- time window 폭이 가장 좁은 고객
- demand가 큰 고객
- depot 기준 각도별 extreme customer
```

장점:

```text
- 구현이 비교적 단순함
- 빠르고 안정적임
- baseline으로 적합함
```

단점:

```text
- 앞에서 만든 route가 좋은 고객을 과도하게 가져갈 수 있음
- 나중 route 품질이 나빠질 수 있음
- 전체 관점에서 고객 배정 균형이 약할 수 있음
```

---

### 6.2 Parallel Regret Insertion 계열

CVRPTW 초기해 생성에서 가장 추천할 만한 핵심 정책입니다.

Sequential insertion과 달리 여러 route를 동시에 고려하고, 각 고객을 어디에 넣을 수 있는지 평가합니다.

핵심 아이디어는 다음과 같습니다.

> 지금 넣지 않으면 나중에 큰 손해가 발생할 고객을 먼저 넣는다.

Regret-k score는 다음과 같이 정의할 수 있습니다.

```text
best_cost_1(u) = 고객 u의 가장 좋은 feasible insertion 비용
best_cost_2(u) = 고객 u의 두 번째로 좋은 feasible insertion 비용
...
best_cost_k(u) = 고객 u의 k번째 feasible insertion 비용

regret_k(u) = Σ_{r=2..k} (best_cost_r(u) - best_cost_1(u))
```

선택 규칙은 다음과 같습니다.

```text
u* = argmax regret_k(u)
u*를 best insertion position에 삽입
```

CVRPTW에서는 time window가 중요하므로 urgency bonus를 함께 쓰는 것이 좋습니다.

```text
urgency_bonus(u)
= λ / (time_window_width(u) + ε)
+ μ / (latest_start(u) - estimated_arrival(u) + ε)
```

최종 score 예시는 다음과 같습니다.

```text
selection_score(u)
= regret_k(u)
+ urgency_bonus(u)
+ demand_priority(u)
```

추천 variant:

```text
- regret-2
- regret-3
- regret-4
- regret-2 + time-window urgency
- regret-3 + randomized top-m selection
```

장점:

```text
- time window가 까다로운 고객을 놓치지 않기 쉬움
- 순차 insertion보다 전체 균형이 좋을 가능성이 큼
- 초기해 품질이 좋은 편임
```

단점:

```text
- 모든 고객 × 모든 route × 모든 position을 평가하면 비용이 커질 수 있음
- nearest neighbor 후보 제한, candidate pruning 등이 필요할 수 있음
```

---

### 6.3 Savings Heuristic 계열

Savings heuristic은 처음에 모든 고객을 단독 route로 두고, route를 합쳤을 때 절약되는 비용이 큰 순서대로 merge하는 방식입니다.

기본 savings 값은 다음과 같습니다.

```text
saving(i, j) = d(i, depot) + d(depot, j) - d(i, j)
```

CVRPTW에서는 merge할 때 다음 feasibility를 반드시 확인해야 합니다.

```text
- capacity feasible
- time window feasible
- service time 포함
- depot return feasible
```

장점:

```text
- 공간적 구조가 강한 문제에서 좋은 route shape을 만들 수 있음
- 빠르게 decent solution을 만들 수 있음
- route pool 다양화에 도움이 됨
```

단점:

```text
- time window가 매우 타이트한 경우 성능이 불안정할 수 있음
- merge 순서에 따라 결과 편차가 큼
```

추천 용도:

```text
- best initial solution 후보
- spatial route shape 생성을 위한 diverse route pool 후보
```

---

### 6.4 Sweep / Cluster-First Route-Second 계열

고객을 depot 기준 각도 또는 공간적 위치로 정렬한 뒤 cluster를 만들고, 각 cluster 내부에서 route를 구성하는 방식입니다.

기본 흐름은 다음과 같습니다.

```text
1. depot 기준 고객 angle 계산
2. angle 순서대로 고객 정렬
3. capacity와 time window를 고려하여 cluster 분할
4. 각 cluster 내부에서 insertion 또는 local search 수행
```

장점:

```text
- 빠름
- 공간적으로 자연스러운 route를 만들 수 있음
- route pool 다양화에 좋음
```

단점:

```text
- time window가 타이트한 문제에서는 품질이 낮을 수 있음
- depot 기준 angle이 실제 time feasibility를 잘 반영하지 못할 수 있음
```

추천 용도:

```text
- 단독 최고해보다는 다양한 route 구조 생성을 위한 보조 정책
```

---

### 6.5 Nearest Feasible Neighbor / Urgency-First 계열

현재 route의 마지막 고객에서 가장 가까운 feasible 고객 또는 가장 긴급한 고객을 선택하는 단순 정책입니다.

score 예시는 다음과 같습니다.

```text
score(i, u)
= α * distance(i, u)
+ β * waiting_time_if_visit_u
+ γ * time_window_tightness(u)
+ δ * capacity_pressure(u)
```

장점:

```text
- 구현이 매우 간단함
- 빠름
- baseline으로 좋음
- randomized variant를 만들기 쉬움
```

단점:

```text
- myopic함
- 전체적인 route 품질은 낮을 수 있음
```

추천 용도:

```text
- 빠른 feasible solution 생성
- benchmark baseline
- randomized multi-start 후보
```

---

## 7. 휴리스틱 다양성을 만드는 축

포트폴리오를 구성할 때 중요한 것은 단순히 정책 수를 늘리는 것이 아닙니다.

서로 비슷한 정책을 많이 넣으면 결과가 크게 달라지지 않습니다. 따라서 다음 축에서 다양성을 만들어야 합니다.

```text
1. route 생성 방식
   - sequential
   - parallel
   - merge-based
   - cluster-first

2. customer 우선순위
   - distance 중심
   - time-window urgency 중심
   - demand 중심
   - regret 중심

3. seed 선택
   - farthest
   - earliest deadline
   - narrowest window
   - high demand
   - random among top candidates

4. objective weighting
   - vehicle count 우선
   - distance 우선
   - waiting time penalty
   - slack preservation

5. randomization
   - top-k random choice
   - softmax choice
   - noise-added insertion cost
   - randomized seed customer
```

초기 추천 포트폴리오는 다음과 같습니다.

```text
P1: Sequential insertion - farthest seed
P2: Sequential insertion - earliest-deadline seed
P3: Parallel regret-2 insertion
P4: Parallel regret-3 insertion + urgency bonus
P5: Savings heuristic with TW feasibility
P6: Sweep + insertion
P7: Randomized nearest feasible neighbor
P8: Randomized regret insertion
```

---

## 8. 병렬 실행과 Racing 전략

가장 단순한 병렬 실행 방식은 다음과 같습니다.

```text
1. 각 휴리스틱 정책을 독립적으로 실행
2. 후보 solution 생성
3. objective 기준으로 가장 좋은 solution 선택
```

하지만 더 좋은 구조는 **racing 전략**입니다.

```text
1. 모든 정책을 짧게 실행
2. 품질이 낮거나 infeasible repair가 오래 걸리는 run 제거
3. 가능성이 높은 run에 더 많은 시간 배정
4. 최종적으로 best solution과 diverse top-K solution 유지
```

예시 시간 배분은 다음과 같습니다.

```text
0.00s ~ 0.20s:
  매우 빠른 construction 여러 개 생성

0.20s ~ 0.60s:
  상위 후보에 light local search 적용

0.60s ~ 0.90s:
  route elimination, regret repair 시도

0.90s ~ 1.00s:
  best solution과 diverse top-K 정리
```

초기해 생성 시간이 더 많이 허용된다면 randomized construction 수를 늘리는 것이 효과적일 수 있습니다.

---

## 9. 목적함수와 평가 기준

CVRPTW에서는 보통 다음 두 목적이 중요합니다.

```text
1차 목적: 차량 수 최소화
2차 목적: 총 거리 또는 총 비용 최소화
```

따라서 solution 비교는 scalar 하나보다는 lexicographic 방식이 적합합니다.

```text
solution_key(S) =
(
  number_of_vehicles,
  total_distance,
  total_waiting_time,
  -total_slack,
  construction_time
)
```

삽입 비용은 다음과 같이 정의할 수 있습니다.

```text
insert_score(u, route r, position p)
= α * delta_distance
+ β * delta_route_duration
+ γ * additional_waiting
+ η * lost_time_slack
+ θ * capacity_pressure
```

여기서 중요한 항목은 `lost_time_slack`입니다.

당장의 distance 증가가 작더라도 뒤쪽 고객들의 time window 여유를 크게 줄이는 삽입은 나중에 infeasibility를 유발할 수 있습니다.

따라서 초기 휴리스틱은 다음을 함께 고려해야 합니다.

```text
- 거리 증가
- route duration 증가
- waiting time 증가
- time window slack 감소
- capacity 여유 감소
- depot return 가능성
```

---

## 10. Route Elimination의 중요성

CVRPTW에서 차량 수가 1차 목적이라면 construction 이후 route elimination을 넣는 것이 좋습니다.

기본 로직은 다음과 같습니다.

```text
Route Elimination Heuristic:

1. 제거할 route 선택
   - 고객 수가 적은 route
   - 총 demand가 작은 route
   - 다른 route와 공간적으로 가까운 고객이 많은 route

2. 해당 route의 고객들을 unassigned로 이동

3. regret insertion으로 다른 route들에 재삽입 시도

4. 모두 삽입 가능하면 차량 수 1 감소

5. 실패하면 rollback

6. 제한 시간 내 반복
```

route elimination은 construction heuristic이 보수적으로 만든 route 수를 줄이는 데 직접적으로 작동합니다.

특히 차량 수 최소화가 중요한 benchmark나 실무 문제에서는 초기해 품질에 큰 영향을 줄 수 있습니다.

---

## 11. ALNS + Simulated Annealing 단계와의 연결

초기 휴리스틱은 ALNS의 출발점을 제공합니다.

하지만 best solution 하나만 넘기는 것보다 다음과 같은 solution pool을 넘기는 것이 좋습니다.

```text
Initial solution pool:
- best objective solution 1개
- 차량 수는 같지만 route 구조가 다른 solution 2~5개
- distance는 약간 나쁘지만 slack이 좋은 solution 1~3개
```

ALNS 운영 방식은 다음 중 하나를 선택할 수 있습니다.

```text
방식 A:
best solution 하나에서 ALNS 시작

방식 B:
top-K initial solution에서 짧은 ALNS를 병렬 실행한 뒤 best run 연장

방식 C:
하나의 ALNS 안에서 incumbent는 best로 시작하되,
초기 route pool은 여러 solution에서 가져옴
```

개발 초기에는 방식 A가 가장 단순합니다.

성능을 끌어올리는 단계에서는 방식 B 또는 C가 유리할 수 있습니다.

---

## 12. MIP Set Covering / Set Partitioning 단계와의 연결

MIP set covering 또는 set partitioning은 좋은 route column이 얼마나 있느냐에 따라 성능이 크게 달라집니다.

따라서 초기 휴리스틱과 ALNS에서 생성되는 모든 route를 route pool에 저장하는 구조가 좋습니다.

route pool entry는 다음 정보를 가질 수 있습니다.

```text
route_pool.add(route):
  key = ordered customer sequence
  cost = distance / duration / vehicle cost
  load
  time feasibility info
  time slack info
  source_policy
  source_solution_id
```

중복과 pool 크기를 제어하기 위한 pruning 기준은 다음과 같습니다.

```text
- 동일 customer sequence 중 최저 cost만 유지
- 동일 customer set에 대해 상위 N개만 유지
- 너무 긴 route 제거
- slack이 매우 나쁜 route 제거
- 최근 ALNS에서 자주 등장한 route 우선 보존
- source policy 다양성이 유지되도록 pruning
```

이 관점에서는 초기 휴리스틱의 목표가 다음처럼 확장됩니다.

```text
기존 목표:
- 좋은 initial solution 하나 생성

확장 목표:
- 좋은 initial solution 생성
- 다양한 feasible route column 생성
- ALNS와 MIP 모두에 유용한 route pool 구축
```

---

## 13. MVP 개발 순서 제안

처음부터 모든 정책을 구현하기보다는 다음 순서로 개발하는 것이 좋습니다.

### 13.1 1단계: 공통 Insertion Evaluator

가장 먼저 구현해야 할 핵심 모듈입니다.

```text
can_insert(route, u, pos):
  - capacity check
  - arrival time propagation
  - service start time 계산
  - waiting time 계산
  - latest time violation check
  - depot return check
```

초기에는 O(route length) propagation으로 구현해도 됩니다.

이후 성능이 필요해지면 prefix/suffix slack, candidate list, delta evaluation 등을 추가합니다.

---

### 13.2 2단계: Construction 4개 구현

초기 MVP에서는 다음 4개만 구현해도 충분히 강한 baseline을 만들 수 있습니다.

```text
H1: Sequential insertion - farthest seed
H2: Sequential insertion - earliest-deadline seed
H3: Parallel regret-2 insertion
H4: Randomized regret-3 insertion
```

이후 성능 비교 결과에 따라 savings, sweep, urgency-first 등을 추가합니다.

---

### 13.3 3단계: Light Local Search

초기해 생성기에서는 너무 깊은 local search보다 빠른 개선이 중요합니다.

추천 연산은 다음과 같습니다.

```text
- intra-route relocate
- intra-route 2-opt
- inter-route relocate
- limited 2-opt*
```

각 연산은 강한 시간 제한을 두고 실행하는 것이 좋습니다.

---

### 13.4 4단계: Route Elimination

차량 수 최소화가 중요하다면 반드시 넣는 것을 추천합니다.

```text
remove one route
    ↓
removed customers를 regret repair로 재삽입
    ↓
feasible하면 accept
    ↓
실패하면 rollback
```

---

### 13.5 5단계: Portfolio Runner

최종적으로 다음 구조를 만듭니다.

```text
for policy in policies parallel:
    for seed in seeds:
        S = construct(policy, seed)
        S = light_improve(S)
        S = route_elimination(S)
        solution_pool.add(S)
        route_pool.add(routes of S)

return best_solution, top_k_diverse_solutions, route_pool
```

---

## 14. 최종 판단

두 가지 방향을 비교하면 다음과 같습니다.

| 방향 | 장점 | 단점 | 판단 |
|---|---|---|---|
| 단일 정교 휴리스틱 | 구현 흐름이 단순함, 디버깅 쉬움 | 특정 인스턴스에 취약, overfitting 위험, route 다양성 부족 | baseline으로는 좋지만 최종 구조로는 약함 |
| 다양한 휴리스틱 병렬 실행 | robust, 다양한 route pool 확보, ALNS/MIP와 궁합 좋음 | 구현량 증가, 평가/선택 로직 필요 | 추천 |
| adaptive portfolio | 정책별 성능을 학습적으로 반영 가능 | 튜닝 필요 | 장기적으로 추천 |

최종 추천은 다음과 같습니다.

> **초기해 휴리스틱은 하나의 매우 정교한 규칙으로 만들기보다, 공통 insertion evaluator 위에 여러 construction policy를 얹은 portfolio 구조로 설계하는 것이 좋다.**

이 portfolio는 단순히 best initial solution을 뽑는 도구가 아니라 다음 역할까지 수행해야 합니다.

```text
1. ALNS warm start 제공
2. ALNS 초기 solution 다양화
3. MIP set covering / set partitioning용 route column 생성
4. instance type별 정책 성능 데이터 축적
```

---

## 15. 실행 체크리스트

개발을 시작할 때 사용할 수 있는 체크리스트입니다.

```text
[ ] CVRPTW instance parser 구현
[ ] distance/time matrix 구성
[ ] feasible arc matrix 구성
[ ] 공통 route feasibility checker 구현
[ ] 공통 insertion evaluator 구현
[ ] sequential insertion 구현
[ ] regret-2 insertion 구현
[ ] randomized regret insertion 구현
[ ] solution objective comparator 구현
[ ] light local search 구현
[ ] route elimination 구현
[ ] solution pool 구현
[ ] route pool 구현
[ ] portfolio runner 구현
[ ] ALNS 초기해 연결
[ ] MIP column 생성 연결
```

---

## 16. 다음으로 구체화할 만한 주제

이후에는 다음 항목들을 더 구체화하면 좋습니다.

```text
1. CVRPTW insertion evaluator의 정확한 구현 방식
2. regret insertion의 효율적 계산 구조
3. route feasibility propagation 최적화
4. local search operator 설계
5. route elimination 상세 로직
6. ALNS destroy/repair operator 설계
7. route pool pruning 전략
8. set partitioning MIP formulation
9. benchmark 실험 설계
10. instance 특성별 adaptive policy selection
```
