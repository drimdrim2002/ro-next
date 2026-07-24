# 02. 초기해 생성 휴리스틱

## 1. 초기해의 역할

CVRPTW 최적화 흐름은 다음처럼 볼 수 있다.

```text
휴리스틱 초기해 생성
    ↓
메타휴리스틱
ALNS + Simulated Annealing 또는 HGS
    ↓
선택적 MIP 후처리
Set Covering / Set Partitioning
```

초기해 생성기는 단순히 첫 feasible solution 하나를 만드는 모듈이 아니다. 다음 역할을 함께 맡는다.

- 빠르게 feasible initial solution 생성
- ALNS 또는 HGS의 warm start 제공
- 서로 다른 route 구조를 만들어 탐색 다양성 확보
- MIP set covering / set partitioning에 사용할 route column 후보 생성
- instance 특성별로 어떤 construction policy가 잘 맞는지 데이터 축적

## 2. 단일 정교 휴리스틱보다 포트폴리오

CVRPTW는 인스턴스 특성에 따라 좋은 초기해 정책이 달라진다.

```text
- 고객들이 공간적으로 클러스터링되어 있는가?
- time window가 타이트한가?
- capacity 제약이 더 지배적인가?
- 차량 수 최소화가 중요한가?
- 총 거리 최소화가 중요한가?
- 고객별 time window 폭이 균일한가?
- depot 주변에 고객이 몰려 있는가?
```

따라서 하나의 매우 정교한 휴리스틱에 모든 것을 맡기기보다, 여러 construction policy를 포트폴리오로 실행하는 방식이 더 견고하다.

```text
여러 construction policy 실행
    ↓
각 후보해에 light local improvement 적용
    ↓
best solution과 diverse solution 모두 보관
    ↓
ALNS와 MIP route pool에 전달
```

초기 생성기는 다음을 반환하는 것이 좋다.

```text
best_initial_solution
top_k_diverse_solutions
route_pool
```

## 3. 추천 아키텍처

```text
Input instance
    ↓
Preprocessing
    - feasible arc matrix
    - nearest neighbor list
    - time-window slack
    - demand / capacity tightness
    - customer urgency score
    - customerAllowedVehicles
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

중요한 설계 원칙은 **공통 insertion evaluator 위에 여러 policy를 얹는 것**이다. 각 휴리스틱마다 feasibility logic을 따로 만들면 중복과 오류가 커진다.

공통 모듈은 다음 기능을 제공해야 한다.

```text
can_insert(route, customer, position)
evaluate_insert_cost(route, customer, position)
apply_insert(route, customer, position)
propagate_time(route)
check_capacity(route)
check_depot_return(route)
check_vehicle_compatibility(customer, vehicle)
```

## 4. 삽입 비용 설계

단순 삽입 비용은 다음과 같다.

```text
delta_distance = d(i, u) + d(u, j) - d(i, j)
```

CVRPTW에서는 distance만 보면 부족하다. time window 여유를 크게 줄이는 삽입은 나중에 infeasibility를 만들 수 있다.

추천 삽입 점수는 다음 형태다.

```text
insert_score(u, route r, position p)
= α * delta_distance
+ β * delta_route_duration
+ γ * additional_waiting
+ η * lost_time_slack
+ θ * capacity_pressure
```

함께 고려할 항목:

- 거리 증가
- route duration 증가
- waiting time 증가
- time window slack 감소
- capacity 여유 감소
- depot return 가능성
- 차량/고객 호환성

## 5. Sequential Insertion

route를 하나씩 만들고, 각 route에 고객을 순차적으로 삽입한다.

```text
1. seed customer를 선택하여 새 route 시작
2. 아직 배정되지 않은 고객 중 feasible insertion 가능한 고객 탐색
3. insertion score가 가장 좋은 고객을 route에 삽입
4. 더 이상 삽입할 수 없으면 route 확정
5. 미배정 고객이 남아 있으면 새 route 생성
```

seed 정책을 바꾸면 다양한 route 구조를 만들 수 있다.

```text
- depot에서 가장 먼 고객
- latest time window가 가장 빠른 고객
- time window 폭이 가장 좁은 고객
- demand가 큰 고객
- depot 기준 각도별 extreme customer
```

장점:

- 구현이 단순하다.
- 빠르고 안정적이다.
- baseline으로 적합하다.

단점:

- 앞 route가 좋은 고객을 과도하게 가져갈 수 있다.
- 뒤 route 품질이 나빠질 수 있다.
- 전체 고객 배정 균형이 약할 수 있다.

## 6. Parallel Regret Insertion

CVRPTW 초기해 생성에서 핵심 후보로 추천되는 방식이다. 모든 고객의 가능한 삽입 위치를 평가하고, 지금 넣지 않으면 손해가 큰 고객을 먼저 넣는다.

Regret-k는 다음과 같이 정의한다.

```text
best_cost_1(u) = 고객 u의 가장 좋은 feasible insertion 비용
best_cost_2(u) = 고객 u의 두 번째로 좋은 feasible insertion 비용
...
best_cost_k(u) = 고객 u의 k번째 feasible insertion 비용

regret_k(u) = Σ_{r=2..k} (best_cost_r(u) - best_cost_1(u))
```

선택 규칙:

```text
u* = argmax regret_k(u)
u*를 best insertion position에 삽입
```

CVRPTW에서는 urgency bonus를 함께 쓰는 것이 좋다.

```text
urgency_bonus(u)
= λ / (time_window_width(u) + ε)
+ μ / (latest_start(u) - estimated_arrival(u) + ε)
```

최종 선택 점수:

```text
selection_score(u)
= regret_k(u)
+ urgency_bonus(u)
+ demand_priority(u)
+ compatibility_scarcity_bonus(u)
```

추천 variant:

- regret-2
- regret-3
- regret-4
- regret-2 + time-window urgency
- regret-3 + randomized top-m selection

## 7. Savings Heuristic

Savings heuristic은 모든 고객을 단독 route로 시작한 뒤, route를 합쳤을 때 절약되는 비용이 큰 순서로 merge한다.

```text
saving(i, j) = d(i, depot) + d(depot, j) - d(i, j)
```

CVRPTW에서는 merge 시 반드시 확인해야 한다.

```text
- capacity feasible
- time window feasible
- service time 포함
- depot return feasible
- vehicle compatibility feasible
```

장점:

- 공간적 구조가 강한 문제에서 좋은 route shape을 만든다.
- 빠르게 decent solution을 만들 수 있다.
- route pool 다양화에 도움이 된다.

단점:

- time window가 타이트하면 불안정할 수 있다.
- merge 순서에 따라 결과 편차가 크다.

## 8. Sweep / Cluster-First Route-Second

고객을 depot 기준 각도나 공간 위치로 정렬한 뒤 cluster를 만들고, 각 cluster 내부에서 route를 구성한다.

```text
1. depot 기준 고객 angle 계산
2. angle 순서대로 고객 정렬
3. capacity와 time window를 고려하여 cluster 분할
4. 각 cluster 내부에서 insertion 또는 local search 수행
```

장점:

- 빠르다.
- 공간적으로 자연스러운 route를 만들 수 있다.
- route pool 다양화에 좋다.

단점:

- time window가 타이트한 문제에서는 품질이 낮을 수 있다.
- depot 기준 angle이 실제 time feasibility를 충분히 반영하지 못한다.

## 9. Nearest Feasible Neighbor / Urgency-First

현재 route의 마지막 고객에서 가장 가까운 feasible 고객 또는 가장 긴급한 고객을 선택한다.

예시 점수:

```text
score(i, u)
= α * distance(i, u)
+ β * waiting_time_if_visit_u
+ γ * time_window_tightness(u)
+ δ * capacity_pressure(u)
```

장점:

- 매우 빠르고 단순하다.
- baseline과 randomized multi-start에 좋다.

단점:

- myopic하다.
- 전체 route 품질은 낮을 수 있다.

## 10. 다양성 축

포트폴리오는 단순히 정책 수를 늘리는 것이 아니라 서로 다른 구조를 만들어야 한다.

| 다양성 축 | 예시 |
|---|---|
| route 생성 방식 | sequential, parallel, merge-based, cluster-first |
| customer 우선순위 | distance, time-window urgency, demand, regret |
| seed 선택 | farthest, earliest deadline, narrowest window, high demand |
| objective weighting | vehicle count, distance, waiting, slack preservation |
| randomization | top-k random, softmax, noise, randomized seed |

초기 추천 포트폴리오(일반 연구 예시이며 현재 프로젝트의 확정 policy는 §11을 따른다):

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

## 11. 프로젝트의 two-phase portfolio ALNS

현재 프로젝트의 initial portfolio는 request-route 성장 정책 4개와 vehicle 순서 2개를 조합한 최대 8개 candidate다. candidate 하나가 8개가 되는 것이 아니라, 8개 조합이 독립적으로 initial solution과 ordered route artifact를 하나씩 만든다.

```text
Phase 1
8개 initial candidate 생성
→ candidate마다 고정 screenMaxSteps ALNS 실행
→ comparator로 phase-1 champion 하나 선택

Phase 2
phase-1 champion을 공통 warm start로 사용
→ seed / destroy / repair config가 다른 worker batch 실행
→ 모든 worker 종료·검증 후 round champion 선택
→ strictly better면 다음 round
→ equal/worse면 종료
→ maxRounds에서도 종료
```

Wall-clock 시간은 quality budget이 아니라 watchdog/관측값이다. `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`는 calibration으로 정한다. worker 하나가 먼저 끝났거나 좋아 보인다는 이유로 batch를 중단하지 않으며, fan-in 뒤 stable comparator로만 champion과 plateau를 판정한다.

## 12. Route Elimination

차량 수가 1차 목적이면 construction 이후 route elimination이 중요하다.

```text
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

route elimination은 보수적인 construction이 만든 route 수를 직접 줄인다.

## 13. ALNS와 MIP 연결

초기해 생성기는 best solution 하나만 넘기기보다 solution pool을 넘기는 것이 좋다.

```text
Initial solution pool:
- best objective solution 1개
- 차량 수는 같지만 route 구조가 다른 solution 2~5개
- distance는 약간 나쁘지만 slack이 좋은 solution 1~3개
```

MIP set covering / set partitioning을 사용할 경우 route pool이 중요하다.

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

route pool pruning 기준:

- 동일 customer sequence 중 최저 cost만 유지
- 동일 customer set에 대해 상위 N개만 유지
- 너무 긴 route 제거
- slack이 매우 나쁜 route 제거
- 최근 ALNS에서 자주 등장한 route 우선 보존
- source policy 다양성 유지

## 14. MVP 개발 순서

```text
1. 공통 insertion evaluator
2. request-route 성장 정책 4개 구현
   - CLOCK
   - SEQ_FARTHEST
   - SEQ_LARGE_DEMAND
   - SEQ_EARLIEST_DEADLINE
3. DIRECT-first vehicle 순서 2개 구현
   - DIRECT_FIRST_LARGE
   - DIRECT_FIRST_SMALL
4. 4 × 2 portfolio runner와 route artifact 기록
5. phase-1 per-candidate ALNS screen과 champion selection
6. phase-2 worker batch, stable fan-in과 plateau/max-round 종료
7. route pool export 경계와 MIP column 연결은 별도 승인 후 구현
```
