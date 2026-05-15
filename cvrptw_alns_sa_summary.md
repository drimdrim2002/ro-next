# CVRPTW 문제를 ALNS + Simulated Annealing으로 접근하기 위한 개념 정리

## 0. 문서 목적

이 문서는 현재까지 논의한 **CVRPTW, Capacitated Vehicle Routing Problem with Time Windows** 문제를 **ALNS, Adaptive Large Neighborhood Search**와 **Simulated Annealing** 관점에서 접근하기 위한 1차 설계 내용을 큰 주제별로 정리한 것이다.

향후 질의응답을 통해 다음 항목들을 점진적으로 구체화하는 것을 목표로 한다.

- 문제 정의
- 해 표현 방식
- 목적함수 설계
- feasibility 검사 방식
- ALNS 구조
- destroy / repair operator 설계
- Simulated Annealing 기반 acceptance rule
- adaptive operator weight update
- 초기해 생성 방식
- local search 추가 여부
- 구현용 pseudo code

---

# 1. CVRPTW 문제 개요

## 1.1 문제 정의

CVRPTW는 **Capacitated Vehicle Routing Problem with Time Windows**의 약자이다.

목표는 여러 고객을 차량으로 방문하되, 다음 제약을 만족하면서 비용을 최소화하는 것이다.

- 모든 고객은 정확히 한 번 방문되어야 한다.
- 각 차량은 depot에서 출발해 depot으로 돌아와야 한다.
- 각 차량의 적재량은 차량 용량을 초과할 수 없다.
- 각 고객은 지정된 time window 안에 서비스를 받아야 한다.
- 차량 이동 거리 또는 이동 시간의 총합을 최소화한다.
- 경우에 따라 사용 차량 수를 우선적으로 최소화한다.

## 1.2 기본 입력 데이터

각 고객 `i`는 다음 정보를 가진다.

```text
demand[i]       고객 i의 수요량
service[i]      고객 i에서의 서비스 시간
time_window[i]  [earliest[i], latest[i]]
```

각 노드 쌍 `(i, j)`에 대해서는 다음 정보가 필요하다.

```text
distance[i][j]  이동 거리 또는 이동 비용
travel[i][j]    이동 시간
```

차량과 depot 관련 정보는 다음과 같다.

```text
capacity Q      차량 용량
vehicle_count K 사용 가능한 차량 수, 또는 충분히 큰 차량 수
depot node 0    출발 및 복귀 depot
```

---

# 2. 해의 표현 방식

## 2.1 Route List 표현

CVRPTW의 해는 일반적으로 route list 형태로 표현한다.

```text
solution = [
    [0, 3, 7, 2, 0],
    [0, 5, 1, 9, 4, 0],
    [0, 6, 8, 0]
]
```

각 route는 depot `0`에서 시작해서 depot `0`으로 끝난다.

## 2.2 Route 평가 항목

각 route에 대해 다음 값을 계산할 수 있어야 한다.

```text
load(route)
arrival_time[i]
waiting_time[i]
total_distance(route)
time_window_feasibility(route)
capacity_feasibility(route)
```

## 2.3 시간 계산 방식

route가 다음과 같다고 하자.

```text
[0, 3, 7, 2, 0]
```

시간 계산은 다음 방식으로 진행된다.

```text
time = depot_start_time

0 -> 3 이동
arrival[3] = time + travel[0][3]

if arrival[3] < earliest[3]:
    wait until earliest[3]

if arrival[3] > latest[3]:
    time window violation

service at 3
time = max(arrival[3], earliest[3]) + service[3]

3 -> 7 이동
...
```

즉, 고객에 일찍 도착하는 것은 허용되며, 해당 고객의 earliest time까지 waiting이 발생한다. 반면 latest time 이후 도착하면 time window violation이다.

---

# 3. 목적함수 설계

## 3.1 기본 목적함수

가장 단순한 목적은 총 이동거리 최소화이다.

```text
minimize total_distance
```

하지만 CVRPTW에서는 보통 차량 수와 총 거리를 함께 고려한다.

```text
1순위: 사용 차량 수 최소화
2순위: 총 이동거리 최소화
```

## 3.2 차량 수 우선 목적함수

차량 수를 거리보다 우선적으로 줄이고 싶다면 다음과 같은 목적함수를 사용할 수 있다.

```text
Objective(solution)
= M * used_vehicle_count + total_distance
```

여기서 `M`은 충분히 큰 값이다.

예시:

```text
M = 100000
```

이 값이 충분히 크면 차량 수 감소가 총 거리 감소보다 우선된다.

## 3.3 Feasible-only Objective

feasible solution만 탐색한다면 목적함수는 단순하다.

```text
Objective(solution):

    used_vehicle_count ← number of non-empty routes
    total_distance ← sum of route distances

    return vehicle_weight * used_vehicle_count + total_distance
```

## 3.4 Penalty 기반 Objective

infeasible solution도 탐색 중 허용한다면 penalty 기반 목적함수를 사용할 수 있다.

```text
Objective(solution):

    total_distance ← 0
    used_vehicle_count ← 0
    capacity_violation ← 0
    time_window_violation ← 0

    for each route in solution:

        if route is not empty:
            used_vehicle_count += 1

        total_distance += RouteDistance(route)

        route_load ← sum demand of customers in route

        if route_load > Q:
            capacity_violation += route_load - Q

        Simulate route schedule

        for each customer i in route:
            if arrival_time[i] > latest[i]:
                time_window_violation += arrival_time[i] - latest[i]

    return total_distance
           + vehicle_penalty * used_vehicle_count
           + capacity_penalty * capacity_violation
           + time_penalty * time_window_violation
```

이 방식에서는 `current_solution`은 infeasible일 수 있지만, 최종 `best_solution`은 feasible solution만 저장하는 것이 안전하다.

---

# 4. Feasibility 검사

## 4.1 Capacity Feasibility

각 route의 총 demand가 차량 capacity 이하인지 확인한다.

```text
load(route) <= Q
```

## 4.2 Time Window Feasibility

route 순서대로 이동 시간을 누적하면서 각 고객의 도착 시간이 latest time을 넘지 않는지 확인한다.

```text
arrival_time[i] <= latest[i]
```

단, earliest time보다 일찍 도착하는 것은 허용된다.

```text
if arrival_time[i] < earliest[i]:
    waiting_time = earliest[i] - arrival_time[i]
```

## 4.3 Feasible-only 방식과 Penalty 방식

초기 구현에서는 두 가지 방식 중 하나를 선택해야 한다.

### 방식 A. Feasible-only ALNS

- 항상 feasible solution만 유지한다.
- 구현이 단순하다.
- 탐색 공간이 제한될 수 있다.

### 방식 B. Penalty 기반 Infeasible 허용 ALNS

- infeasible solution도 penalty를 부여해 탐색 중 허용한다.
- 탐색이 더 유연하다.
- penalty coefficient 조정이 필요하다.

1차 구현에서는 feasible-only 방식으로 시작하고, 이후 성능 개선 단계에서 penalty 기반 방식으로 확장하는 것이 적절하다.

---

# 5. ALNS 전체 개념

## 5.1 ALNS란?

ALNS는 **Adaptive Large Neighborhood Search**이다.

현재 해에서 일부 고객을 제거한 뒤 다시 삽입하면서 새로운 해를 만들고, 이를 반복적으로 개선한다.

기본 흐름은 다음과 같다.

```text
현재 해
→ 일부 고객 제거, destroy
→ 제거된 고객 재삽입, repair
→ 새 해 생성
→ accept/reject 판단
→ operator 성능에 따라 가중치 업데이트
```

## 5.2 ALNS의 핵심 구성요소

ALNS는 크게 다음 구성요소로 이루어진다.

```text
1. Initial solution generator
2. Destroy operators
3. Repair operators
4. Acceptance criterion
5. Adaptive operator scoring and weighting
6. Optional local search
7. Stopping condition
```

---

# 6. Destroy Operators

Destroy operator는 현재 해에서 일부 고객을 제거한다.

## 6.1 Random Removal

무작위로 고객을 제거한다.

```text
remove q random customers
```

장점은 단순하고 탐색 다양성을 제공한다는 점이다.

## 6.2 Worst Removal

현재 위치에서 비용 증가를 많이 유발하는 고객을 제거한다.

고객 `i`가 route에서 `prev - i - next` 형태로 있을 때 제거 이득은 다음과 같다.

```text
saving(i) = distance[prev][i] + distance[i][next] - distance[prev][next]
```

`saving(i)`가 큰 고객일수록 제거했을 때 비용이 많이 줄어든다.

## 6.3 Shaw Removal / Related Removal

서로 관련성이 높은 고객들을 묶어서 제거한다.

고객 간 relatedness는 다음 요소로 정의할 수 있다.

```text
거리상 가까움
time window가 비슷함
demand가 비슷함
같은 route에 있음
```

예시:

```text
relatedness(i, j)
= a * distance[i][j]
+ b * abs(earliest[i] - earliest[j])
+ c * abs(demand[i] - demand[j])
```

값이 작을수록 두 고객은 더 유사하다고 본다.

## 6.4 Route Removal

route 하나를 통째로 제거한다.

예시:

```text
solution = [
    [0, 3, 7, 2, 0],
    [0, 5, 1, 9, 4, 0],
    [0, 6, 8, 0]
]
```

두 번째 route를 제거하면 다음 고객들이 removed set에 들어간다.

```text
removed_customers = [5, 1, 9, 4]
```

이 operator는 차량 수를 줄이려는 시도에 유용하다.

## 6.5 Time Window Removal

time window가 비슷한 고객들을 제거한다.

예를 들어 오전 시간대 고객들을 일부 제거하고 다시 재배치하는 방식이다.

시간 제약이 강한 CVRPTW에서 효과적일 수 있다.

---

# 7. Repair Operators

Repair operator는 destroy 단계에서 제거된 고객들을 다시 route에 삽입한다.

## 7.1 Greedy Insertion

각 제거 고객에 대해 가능한 모든 삽입 위치를 확인하고, 가장 비용 증가가 작은 위치에 삽입한다.

고객 `u`를 route의 `i`와 `j` 사이에 넣는 경우 비용 증가는 다음과 같다.

```text
delta_cost = distance[i][u] + distance[u][j] - distance[i][j]
```

삽입 후 capacity와 time window를 만족해야 한다.

## 7.2 Regret-k Insertion

Greedy insertion보다 더 강력한 방식이다.

각 고객에 대해 가장 좋은 삽입 비용, 두 번째로 좋은 삽입 비용, ..., k번째로 좋은 삽입 비용을 계산한다.

Regret-2의 경우:

```text
regret(u) = second_best_insertion_cost(u) - best_insertion_cost(u)
```

regret 값이 큰 고객을 먼저 삽입한다.

의미는 다음과 같다.

```text
이 고객은 지금 제일 좋은 위치에 넣지 않으면 나중에 비용이 크게 증가할 수 있다.
```

CVRPTW에서는 time window가 빡빡한 고객을 늦게 넣으면 feasible insertion이 사라질 수 있기 때문에 regret insertion이 중요하다.

## 7.3 Randomized Greedy Insertion

항상 최선 위치만 고르면 탐색이 너무 deterministic해질 수 있다.

따라서 상위 후보 중 하나를 랜덤하게 선택할 수 있다.

```text
candidate_positions = top p insertion positions
choose one randomly
```

또는 insertion cost에 noise를 추가할 수도 있다.

```text
noisy_cost = delta_cost + random_noise
```

---

# 8. Simulated Annealing Acceptance Rule

ALNS에서 candidate solution을 만들었을 때 항상 더 좋은 해만 받아들이면 local optimum에 쉽게 갇힐 수 있다.

따라서 Simulated Annealing 방식으로 나쁜 해도 일정 확률로 받아들인다.

현재 해를 `s_current`, 새 해를 `s_new`라고 하면 다음과 같다.

```text
delta = F(s_new) - F(s_current)
```

minimization 문제이므로 acceptance rule은 다음과 같다.

```text
if delta <= 0:
    accept
else:
    accept with probability exp(-delta / T)
```

여기서 `T`는 temperature이다.

초기에는 `T`가 커서 나쁜 해도 비교적 잘 받아들인다. 반복이 진행될수록 `T`를 낮춘다.

```text
T = alpha * T
```

예시 cooling rate:

```text
alpha = 0.995
alpha = 0.999
alpha = 0.9995
```

초기 실험에서는 `0.995 ~ 0.999` 정도부터 시작할 수 있다.

---

# 9. Adaptive Operator Weight

## 9.1 Operator 선택 방식

ALNS에서는 destroy / repair operator를 고정 확률로 선택하지 않고, 성능이 좋았던 operator가 더 자주 선택되도록 가중치를 업데이트한다.

초기 weight는 모두 동일하게 둘 수 있다.

```text
random_removal: 1.0
worst_removal: 1.0
shaw_removal: 1.0
route_removal: 1.0
```

매 iteration마다 weight에 비례해서 operator를 선택한다.

## 9.2 Reward Score

좋은 결과를 낸 operator에게 score를 부여한다.

예시:

```text
새 global best를 찾음:       score += 30
현재 해보다 좋은 해를 찾음:   score += 10
나쁜 해지만 accepted됨:       score += 3
rejected됨:                  score += 0
```

## 9.3 Weight Update

일정 iteration마다 operator weight를 업데이트한다.

```text
weight[o] = (1 - reaction) * weight[o]
            + reaction * average_score[o]
```

예시 parameter:

```text
reaction = 0.1 ~ 0.3
segment_length = 100
```

---

# 10. Removal Size 설정

한 번에 제거할 고객 수 `q_remove`도 중요하다.

고객 수를 `n`이라고 하면 다음처럼 설정할 수 있다.

```text
q_remove_min = max(2, 0.05 * n)
q_remove_max = max(5, 0.20 * n)
q_remove = random integer between q_remove_min and q_remove_max
```

예를 들어 고객이 100명이라면 다음 범위가 된다.

```text
q_remove = 5 ~ 20명
```

문제가 크고 복잡할수록 더 큰 removal size가 필요할 수 있다.

---

# 11. 초기해 생성

## 11.1 초기해의 역할

ALNS는 기존 해를 반복적으로 파괴하고 복구하는 방식이므로, 시작점이 되는 초기해가 필요하다.

초기해는 반드시 최적에 가까울 필요는 없지만, 가능하면 feasible해야 한다.

## 11.2 Sequential Insertion 방식

가장 단순한 초기해 생성 방식은 sequential insertion이다.

```text
GenerateInitialSolution():

    unrouted ← all customers
    routes ← empty list

    while unrouted is not empty:

        route ← [0, 0]

        while true:

            best_customer ← None
            best_position ← None
            best_cost ← infinity

            for each customer u in unrouted:
                for each insertion position p in route:

                    candidate_route ← Insert u at position p

                    if candidate_route is feasible:

                        cost ← insertion cost

                        if cost < best_cost:
                            best_cost ← cost
                            best_customer ← u
                            best_position ← p

            if best_customer is None:
                break

            Insert best_customer into route at best_position
            Remove best_customer from unrouted

        Add route to routes

    return routes
```

---

# 12. Local Search

## 12.1 Local Search의 역할

ALNS의 repair 이후 local search를 수행하면 candidate solution의 품질을 더 높일 수 있다.

하지만 1차 구현에서는 local search 없이 시작하고, 이후 성능 개선 단계에서 추가해도 된다.

## 12.2 대표적인 Local Search Operator

```text
Relocate: 한 고객을 다른 위치로 이동
Swap: 두 고객 위치 교환
2-opt: 같은 route 내부의 edge 뒤집기
2-opt*: 서로 다른 두 route의 tail 교환
Cross-exchange: 두 route의 연속 구간 교환
```

## 12.3 권장 추가 순서

초기 구현 이후 다음 순서로 추가하는 것이 적절하다.

```text
1. intra-route 2-opt
2. relocate
3. swap
4. inter-route 2-opt*
```

## 12.4 Local Search Pseudo Code

```text
LocalSearch(solution):

    improved ← true

    while improved:

        improved ← false

        for each neighborhood operator n in local_search_operators:

            candidate ← BestImprovement(solution, n)

            if candidate is feasible and Objective(candidate) < Objective(solution):

                solution ← candidate
                improved ← true
                break

    return solution
```

---

# 13. Operator별 Pseudo Code

## 13.1 Random Removal

```text
RandomRemoval(solution, q_remove):

    removed ← empty list
    customers ← all customers in solution

    selected ← random q_remove customers from customers

    for each customer u in selected:
        Remove u from its route
        Add u to removed

    Remove empty routes from solution

    return solution, removed
```

## 13.2 Worst Removal

```text
WorstRemoval(solution, q_remove):

    removed ← empty list

    while size(removed) < q_remove:

        best_saving ← -infinity
        worst_customer ← None

        for each route in solution:
            for each customer u in route excluding depot:

                prev ← previous node of u
                next ← next node of u

                saving ← distance[prev][u]
                         + distance[u][next]
                         - distance[prev][next]

                if saving > best_saving:
                    best_saving ← saving
                    worst_customer ← u

        Remove worst_customer from solution
        Add worst_customer to removed

    Remove empty routes from solution

    return solution, removed
```

## 13.3 Shaw Removal

```text
ShawRemoval(solution, q_remove):

    removed ← empty list

    seed ← random customer from solution
    Remove seed from solution
    Add seed to removed

    while size(removed) < q_remove:

        candidate_list ← all customers still in solution

        for each customer u in candidate_list:

            relatedness_score[u] ← min over v in removed:
                alpha * distance[u][v]
              + beta  * abs(earliest[u] - earliest[v])
              + gamma * abs(demand[u] - demand[v])

        selected ← customer with small relatedness_score
                   optionally randomized among top candidates

        Remove selected from solution
        Add selected to removed

    Remove empty routes from solution

    return solution, removed
```

## 13.4 Greedy Insertion

```text
GreedyInsertion(partial_solution, removed_customers):

    while removed_customers is not empty:

        best_customer ← None
        best_route ← None
        best_position ← None
        best_cost ← infinity

        for each customer u in removed_customers:

            for each route in partial_solution:
                for each insertion position p in route:

                    candidate_route ← Insert u into route at p

                    if candidate_route is feasible:

                        cost ← insertion_cost(u, route, p)

                        if cost < best_cost:
                            best_cost ← cost
                            best_customer ← u
                            best_route ← route
                            best_position ← p

            candidate_route ← [0, u, 0]

            if candidate_route is feasible:
                cost ← distance[0][u] + distance[u][0]

                if cost < best_cost:
                    best_cost ← cost
                    best_customer ← u
                    best_route ← new route
                    best_position ← between depot and depot

        if best_customer is None:
            return infeasible solution or trigger repair failure

        Insert best_customer into best_route at best_position
        Remove best_customer from removed_customers

    return partial_solution
```

## 13.5 Regret-2 Insertion

```text
Regret2Insertion(partial_solution, removed_customers):

    while removed_customers is not empty:

        selected_customer ← None
        selected_best_insert ← None
        max_regret ← -infinity

        for each customer u in removed_customers:

            insertion_options ← empty list

            for each route in partial_solution:
                for each insertion position p in route:

                    candidate_route ← Insert u into route at p

                    if candidate_route is feasible:

                        cost ← insertion_cost(u, route, p)
                        Add (cost, route, p) to insertion_options

            if [0, u, 0] is feasible:
                cost ← distance[0][u] + distance[u][0]
                Add (cost, new_route, position) to insertion_options

            if insertion_options is empty:
                continue

            Sort insertion_options by cost ascending

            best ← insertion_options[0]

            if size(insertion_options) >= 2:
                second_best ← insertion_options[1]
                regret ← second_best.cost - best.cost
            else:
                regret ← large_value

            if regret > max_regret:
                max_regret ← regret
                selected_customer ← u
                selected_best_insert ← best

        if selected_customer is None:
            return infeasible solution or trigger repair failure

        Insert selected_customer using selected_best_insert
        Remove selected_customer from removed_customers

    return partial_solution
```

---

# 14. Operator Selection과 Weight Update Pseudo Code

## 14.1 Roulette Wheel Operator Selection

```text
SelectOperator(operators, weights):

    total_weight ← sum weights[o] for each o in operators

    x ← RandomUniform(0, total_weight)

    cumulative ← 0

    for each operator o in operators:

        cumulative ← cumulative + weights[o]

        if x <= cumulative:
            return o
```

## 14.2 Weight Update

```text
UpdateWeights(operators, weights, scores, usages):

    for each operator o in operators:

        if usages[o] > 0:

            average_score ← scores[o] / usages[o]

            weights[o] ← (1 - reaction) * weights[o]
                         + reaction * average_score

        else:

            weights[o] ← weights[o]
```

---

# 15. 전체 알고리즘 Pseudo Code

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

Parameters:
    segment_length
    reaction
    initial_temperature
    cooling_rate
    q_remove_min
    q_remove_max
    sigma_global_best
    sigma_improved
    sigma_accepted_worse

Operators:
    Destroy:
        RandomRemoval
        WorstRemoval
        ShawRemoval
        RouteRemoval

    Repair:
        GreedyInsertion
        Regret2Insertion

Procedure:

    current ← GenerateInitialSolution()
    best ← current

    T ← initial_temperature

    Initialize destroy weights to 1
    Initialize repair weights to 1

    Initialize operator scores and usage counts to 0

    for iteration = 1 to max_iterations:

        d ← SelectOperator(destroy_operators, destroy_weights)
        r ← SelectOperator(repair_operators, repair_weights)

        q_remove ← RandomInteger(q_remove_min, q_remove_max)

        partial, removed ← d(copy(current), q_remove)

        candidate ← r(partial, removed)

        if candidate is feasible:

            candidate ← LocalSearch(candidate)

            delta ← Objective(candidate) - Objective(current)

            if delta <= 0:

                current ← candidate
                accepted ← true

                if Objective(candidate) < Objective(best):

                    best ← candidate
                    reward ← sigma_global_best

                else:

                    reward ← sigma_improved

            else:

                probability ← exp(-delta / T)

                if RandomUniform(0, 1) < probability:

                    current ← candidate
                    accepted ← true
                    reward ← sigma_accepted_worse

                else:

                    accepted ← false
                    reward ← 0

        else:

            accepted ← false
            reward ← 0

        Add reward to score of selected destroy operator
        Add reward to score of selected repair operator
        Increase usage count of selected operators

        T ← cooling_rate * T

        if iteration mod segment_length == 0:

            Update destroy weights
            Update repair weights

            Reset scores and usage counts

    return best
```

---

# 16. 1차 구현 권장 설정

## 16.1 Objective

초기 objective는 다음을 권장한다.

```text
Objective(solution)
= M * used_vehicle_count + total_distance
```

예시:

```text
M = 100000
```

## 16.2 Feasibility 정책

초기 구현에서는 feasible-only 방식을 권장한다.

```text
candidate가 feasible한 경우에만 acceptance rule 적용
candidate가 infeasible하면 reject
```

이후 성능 개선 단계에서 penalty 기반 infeasible 허용 방식으로 확장할 수 있다.

## 16.3 Destroy Operators

1차 구현에는 다음 operator를 포함한다.

```text
RandomRemoval
WorstRemoval
ShawRemoval
RouteRemoval
```

## 16.4 Repair Operators

1차 구현에는 다음 operator를 포함한다.

```text
GreedyInsertion
Regret2Insertion
```

## 16.5 Local Search

1차 버전에서는 생략 가능하다.

다만 성능 개선 단계에서는 다음 순서로 추가하는 것을 권장한다.

```text
1. intra-route 2-opt
2. relocate
3. swap
4. inter-route 2-opt*
```

## 16.6 기본 Parameter 예시

```text
reaction = 0.2
segment_length = 100
sigma_global_best = 30
sigma_improved = 10
sigma_accepted_worse = 3
sigma_rejected = 0
sigma_infeasible = 0
cooling_rate = 0.995 ~ 0.999
q_remove_min = max(2, 0.05 * n)
q_remove_max = max(5, 0.20 * n)
```

---

# 17. 향후 질의응답으로 구체화할 사항

다음 단계에서는 아래 의사결정들을 순서대로 확정하면 좋다.

## 17.1 목적함수 우선순위

다음 중 하나를 선택해야 한다.

```text
A. 차량 수 우선 최소화 + 총 거리 최소화
B. 총 거리 최소화만 사용
C. 차량 수, 거리, 시간 위반, 용량 위반을 penalty로 통합
```

초기 권장안은 다음이다.

```text
A. 차량 수 우선 최소화 + 총 거리 최소화
```

## 17.2 Feasibility 처리 방식

다음 중 하나를 선택해야 한다.

```text
A. Feasible-only ALNS
B. Penalty 기반 infeasible 허용 ALNS
```

초기 권장안은 다음이다.

```text
A. Feasible-only ALNS
```

## 17.3 Time Window 모델링 세부사항

다음 항목을 확정해야 한다.

```text
depot time window가 있는가?
vehicle start time은 고정인가?
고객에게 일찍 도착하면 waiting이 허용되는가?
late arrival은 절대 금지인가, penalty 허용인가?
```

초기 가정은 다음이다.

```text
고객에게 일찍 도착하면 waiting 허용
latest 이후 도착은 infeasible
```

## 17.4 차량 수 제한

다음 중 하나를 선택해야 한다.

```text
A. 차량 수 K가 고정되어 있음
B. 차량 수는 충분히 많고 사용 차량 수를 최소화함
```

## 17.5 구현 단계

권장 구현 순서는 다음과 같다.

```text
1. 데이터 구조 정의
2. route feasibility checker 구현
3. objective function 구현
4. initial solution 생성기 구현
5. random removal 구현
6. greedy insertion 구현
7. ALNS main loop 구현
8. SA acceptance rule 추가
9. worst removal, shaw removal, route removal 추가
10. regret insertion 추가
11. adaptive weight update 추가
12. local search 추가
13. penalty 기반 infeasible 허용 방식 검토
```

---

# 18. 현재까지의 핵심 결론

현재까지의 1차 결론은 다음과 같다.

```text
CVRPTW는 route list로 해를 표현한다.
초기 objective는 차량 수 우선 + 거리 최소화를 권장한다.
초기 구현은 feasible-only 방식이 적절하다.
ALNS는 destroy + repair + acceptance + adaptive weight 구조로 설계한다.
Simulated Annealing을 사용해 나쁜 해도 확률적으로 받아들인다.
Destroy operator는 Random, Worst, Shaw, Route Removal부터 시작한다.
Repair operator는 Greedy Insertion과 Regret-2 Insertion부터 시작한다.
Local search는 1차 구현 이후 추가해도 된다.
```

