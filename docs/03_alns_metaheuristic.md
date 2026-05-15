# 03. ALNS 메타휴리스틱

## 1. ALNS 개요

ALNS는 **Adaptive Large Neighborhood Search**의 약자다.

핵심 아이디어는 현재 해의 일부를 크게 제거한 뒤 다시 삽입하면서 새로운 해를 만들고, 이 과정을 반복하면서 더 좋은 해를 찾는 것이다.

```text
현재 해
  ↓
일부 고객 제거, destroy
  ↓
제거 고객 재삽입, repair
  ↓
새 후보해 생성
  ↓
수락 여부 결정
  ↓
operator 성과에 따라 가중치 갱신
  ↓
반복
```

ALNS의 구성요소:

```text
1. Initial solution generator
2. Destroy operators
3. Repair operators
4. Acceptance criterion
5. Adaptive operator scoring and weighting
6. Optional local search
7. Stopping condition
```

## 2. Destroy Operator

Destroy operator는 현재 해에서 일부 고객을 제거한다. 제거 방식은 탐색 방향을 결정하기 때문에 ALNS 성능에 직접적인 영향을 준다.

### Random Removal

무작위로 고객을 제거한다.

```text
remove q random customers
```

장점은 단순하고 탐색 다양성을 제공한다는 점이다. 단점은 현재 해의 문제점을 직접 겨냥하지 않는다는 점이다.

### Worst Removal

현재 위치에서 비용을 많이 유발하는 고객을 제거한다.

고객 `i`가 `prev - i - next` 형태로 있을 때 제거 이득은 다음과 같다.

```text
saving(i) = distance[prev][i] + distance[i][next] - distance[prev][next]
```

`saving(i)`가 큰 고객일수록 현재 배치가 비효율적일 가능성이 크다.

### Shaw / Related Removal

서로 관련성이 높은 고객을 함께 제거한다.

관련성은 다음 요소로 정의할 수 있다.

```text
- 지리적 거리
- time window 유사성
- demand 유사성
- 같은 route에 속하는지 여부
- 현재 해에서의 위치 관계
```

예시:

```text
relatedness(i, j)
= a * distance[i][j]
+ b * abs(earliest[i] - earliest[j])
+ c * abs(demand[i] - demand[j])
```

값이 작을수록 두 고객은 더 유사하다. 관련 고객 묶음을 같이 제거하면 지역 구조를 재구성하기 좋다.

### Route Removal

route 하나를 통째로 제거한다.

```text
Vehicle 2: Depot → 5 → 1 → 9 → 4 → Depot
removed_customers = [5, 1, 9, 4]
```

차량 수 감소, route elimination, 경로 단위 재구성에 유용하다.

### Time Window Removal

time window가 비슷하거나 충돌하는 고객을 제거한다.

예:

```text
오전 시간대 고객 일부 제거
타이트한 deadline 고객 묶음 제거
지연을 유발하는 고객 주변 제거
```

시간 제약이 강한 CVRPTW에서 중요하다.

### Sequence-Based Removal

route 안에서 연속된 고객 구간을 제거한다.

```text
0 → A → B → C → D → E → 0
        [B → C → D] 제거
```

단일 고객보다 큰 구조 변화를 만들 수 있다. ALNS operator ranking 문헌에서는 sequence-based removal이 유망한 성능을 보인 것으로 정리된다.

### 실무 확장용 Removal

Zone, 차량 부족, optional customer 문제가 있으면 다음 operator를 추가할 수 있다.

| Operator | 목적 |
|---|---|
| Zone pressure removal | 초과 수요가 있는 zone 재구성 |
| Compatible group removal | 같은 차량 후보군을 가진 고객 묶음 제거 |
| Low priority removal | 낮은 우선순위 주문을 빼서 고우선순위 주문 공간 확보 |
| Capacity pressure removal | 큰 demand 주문 제거 후 재배치 |
| Dummy reinsertion removal | dummy/미배송 주문을 다시 정규 차량 후보로 복귀 |

## 3. Repair Operator

Repair operator는 제거된 고객을 다시 route에 삽입한다.

핵심 질문은 두 가지다.

```text
1. 어떤 고객부터 넣을 것인가?
2. 그 고객을 어느 위치에 넣을 것인가?
```

### Greedy Insertion

각 제거 고객의 모든 가능한 삽입 위치를 평가하고, 비용 증가가 가장 작은 위치를 선택한다.

```text
delta_cost = distance[i][u] + distance[u][j] - distance[i][j]
```

단기적으로 비용 증가를 최소화하지만, 나중에 삽입하기 어려운 고객을 뒤로 미룰 수 있다.

### Regret-k Insertion

지금 넣지 않으면 나중에 손해가 큰 고객을 먼저 넣는다.

Regret-2:

```text
regret(u) = second_best_insertion_cost(u) - best_insertion_cost(u)
```

Regret-k:

```text
regret_k(u) = Σ_{r=2..k} (best_cost_r(u) - best_cost_1(u))
```

CVRPTW에서는 time window가 빡빡한 고객을 늦게 넣으면 feasible position이 사라질 수 있으므로 regret insertion이 특히 중요하다.

### Randomized Greedy / Noise Insertion

항상 최선 위치만 선택하면 탐색이 deterministic해진다. 상위 후보 중 랜덤 선택하거나 cost에 noise를 추가할 수 있다.

```text
candidate_positions = top p insertion positions
choose one randomly
```

```text
noisy_cost = delta_cost + random_noise
```

### Compatibility-Aware Repair

Zone이나 차량별 제약이 있으면 삽입 후보를 먼저 필터링한다.

```text
for vehicle in realVehicles:
    if !allowed(order, vehicle):
        continue
    evaluate feasible insertion positions
```

삽입 우선순위:

```text
1. 필수 배송 주문
2. 우선순위 높은 주문
3. 가능한 차량 수가 적은 주문
4. Time window가 좁은 주문
5. 미배송 penalty가 큰 주문
6. Penalty / demand 비율이 큰 주문
```

## 4. Simulated Annealing Acceptance

항상 좋은 해만 받아들이면 local optimum에 갇히기 쉽다. Simulated Annealing은 나쁜 해도 일정 확률로 받아들여 탐색 다양성을 확보한다.

```text
delta = F(s_new) - F(s_current)
```

minimization 문제에서 수락 규칙은 다음이다.

```text
if delta <= 0:
    accept
else:
    accept with probability exp(-delta / T)
```

온도 `T`는 반복이 진행될수록 낮춘다.

```text
T = cooling_rate * T
```

초기 실험값:

```text
cooling_rate = 0.995 ~ 0.999
```

## 5. Adaptive Operator Weight

ALNS의 adaptive는 성과가 좋은 operator를 더 자주 선택한다는 뜻이다.

초기 weight:

```text
random_removal: 1.0
worst_removal: 1.0
shaw_removal: 1.0
route_removal: 1.0
```

선택은 roulette wheel 방식으로 할 수 있다.

```text
선택 확률 = operator_weight / total_weight
```

Reward 예시:

```text
새 global best를 찾음:       score += 30
현재 해보다 좋은 해를 찾음:   score += 10
나쁜 해지만 accepted됨:       score += 3
rejected됨:                  score += 0
```

일정 segment마다 weight를 갱신한다.

```text
weight[o] = (1 - reaction) * weight[o]
            + reaction * average_score[o]
```

추천 범위:

```text
reaction = 0.1 ~ 0.3
segment_length = 100
```

## 6. Removal Size

한 번에 제거할 고객 수 `q_remove`도 중요하다.

```text
q_remove_min = max(2, 0.05 * n)
q_remove_max = max(5, 0.20 * n)
q_remove = random integer between q_remove_min and q_remove_max
```

너무 적게 제거하면 변화가 작고, 너무 많이 제거하면 복구가 어렵다. 문헌에서는 문제와 operator에 따라 30~40% 수준의 큰 removal도 효과적인 경우가 보고된다.

## 7. Feasible-only와 Penalty ALNS

초기 구현은 feasible-only가 좋다.

```text
candidate가 feasible하면 acceptance rule 적용
candidate가 infeasible하면 reject
```

이후 성능 개선 단계에서는 penalty 기반 infeasible 허용으로 확장할 수 있다.

```text
Objective(solution)
= total_distance
+ vehicle_penalty * used_vehicle_count
+ capacity_penalty * capacity_violation
+ time_penalty * time_window_violation
```

최종 best solution은 feasible만 저장하는 것이 안전하다.

## 8. Local Search 결합

Repair 이후 local search를 수행하면 candidate 품질이 올라간다.

초기 추가 순서:

```text
1. intra-route 2-opt
2. relocate
3. swap
4. inter-route 2-opt*
```

시간 제한이 빡빡하면 1차 버전에서는 local search 없이 시작하고, 이후 추가해도 된다.

## 9. 전체 의사코드

```text
Algorithm CVRPTW_ALNS_SA

Input:
    customers
    depot
    vehicle_capacity
    distance_matrix
    travel_time_matrix
    demand
    service_time
    time_windows
    max_iterations

Initialize:
    current = GenerateInitialSolution()
    best = current
    T = initial_temperature
    destroy_weights = 1
    repair_weights = 1

for iteration = 1 to max_iterations:

    d = SelectOperator(destroy_operators, destroy_weights)
    r = SelectOperator(repair_operators, repair_weights)
    q_remove = RandomInteger(q_remove_min, q_remove_max)

    partial, removed = d(copy(current), q_remove)
    candidate = r(partial, removed)

    if candidate is feasible:
        candidate = LocalSearch(candidate)
        delta = Objective(candidate) - Objective(current)

        if delta <= 0:
            current = candidate
            accepted = true
        else:
            probability = exp(-delta / T)
            accepted = RandomUniform(0, 1) < probability
            if accepted:
                current = candidate

        if Objective(candidate) < Objective(best):
            best = candidate
            reward = sigma_global_best
        else if accepted and delta <= 0:
            reward = sigma_improved
        else if accepted:
            reward = sigma_accepted_worse
        else:
            reward = 0
    else:
        accepted = false
        reward = 0

    update selected operator scores and usage
    T = cooling_rate * T

    if iteration mod segment_length == 0:
        UpdateWeights()
        reset scores and usage

return best
```

## 10. 1차 구현 권장 설정

```text
Objective:
  M * used_vehicle_count + total_distance

Feasibility:
  feasible-only

Destroy:
  RandomRemoval
  WorstRemoval
  ShawRemoval
  RouteRemoval

Repair:
  GreedyInsertion
  Regret2Insertion

Acceptance:
  Simulated Annealing

Adaptive:
  reaction = 0.2
  segment_length = 100
  sigma_global_best = 30
  sigma_improved = 10
  sigma_accepted_worse = 3

Removal:
  q_remove_min = max(2, 0.05 * n)
  q_remove_max = max(5, 0.20 * n)
```

