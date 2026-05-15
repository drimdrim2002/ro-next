# 01. 문제 정의와 모델링

## 1. 핵심 문제군

현재 문서들의 중심 문제는 **CVRPTW, Capacitated Vehicle Routing Problem with Time Windows**이다.

CVRPTW는 여러 차량이 depot에서 출발해 고객을 방문하고 다시 depot으로 돌아오되, 다음 조건을 만족하면서 비용을 최소화하는 차량경로문제다.

- 모든 고객은 정확히 한 번 방문된다.
- 각 차량 route는 depot에서 시작해 depot으로 끝난다.
- 차량별 총 적재량은 capacity를 넘지 않는다.
- 각 고객은 지정된 time window 안에 서비스를 받아야 한다.
- 이동거리, 운행시간, 차량 수, 지연 penalty 등을 최소화한다.

관련 문제군은 다음처럼 정리할 수 있다.

| 문제 | 의미 | CVRPTW와의 관계 |
|---|---|---|
| TSP | 한 차량이 도시들을 한 번씩 방문 | VRP 계열의 기본 구조 |
| CVRP | 차량 용량 제약이 있는 VRP | CVRPTW에서 시간창을 뺀 형태 |
| VRPTW | 시간창이 있는 VRP | capacity 제약을 함께 두면 CVRPTW |
| PDP | pickup-delivery 쌍이 있는 문제 | 픽업/배송 순서와 같은 차량 제약 추가 |
| PDPTW | 시간창이 있는 pickup-delivery 문제 | CVRPTW보다 precedence와 pairing 제약이 강함 |
| Selective / Prize-Collecting VRPTW | 모든 고객 방문이 필수가 아닌 문제 | 차량 부족, 미배송 허용 실무 문제에 적합 |

## 2. 기본 구성 요소

| 구성 요소 | 설명 |
|---|---|
| Depot | 차량이 출발하고 복귀하는 장소 |
| Customer / Order | 방문해야 하는 고객 또는 주문 |
| Vehicle | 배송에 사용되는 차량 |
| Demand | 고객 또는 주문의 수요량 |
| Capacity | 차량이 실을 수 있는 최대 적재량 |
| Time Window | 고객 방문 또는 서비스 가능 시간 구간 |
| Service Time | 고객에게 서비스를 수행하는 데 걸리는 시간 |
| Travel Time / Distance | 노드 간 이동 시간 또는 거리 |

고객 `i`는 보통 다음 데이터를 가진다.

```text
demand[i]       고객 i의 수요량
service[i]      고객 i의 서비스 시간
time_window[i]  [earliest[i], latest[i]]
```

노드 쌍 `(i, j)`에는 다음 정보가 필요하다.

```text
distance[i][j]  이동 거리 또는 비용
travel[i][j]    이동 시간
```

차량과 depot 정보는 다음과 같다.

```text
capacity Q
vehicle_count K
depot node 0
vehicle start/end time
```

## 3. 해 표현

CVRPTW 해는 일반적으로 route list로 표현한다.

```text
solution = [
    [0, 3, 7, 2, 0],
    [0, 5, 1, 9, 4, 0],
    [0, 6, 8, 0]
]
```

각 route는 다음 의미를 가진다.

- 첫 번째 `0`: depot 출발
- 중간 숫자: 방문 고객
- 마지막 `0`: depot 복귀

각 route에 대해 계산해야 할 값은 다음과 같다.

```text
load(route)
arrival_time[i]
service_start_time[i]
waiting_time[i]
total_distance(route)
route_duration(route)
time_window_feasibility(route)
capacity_feasibility(route)
depot_return_feasibility(route)
```

## 4. 시간 계산 방식

route가 다음과 같다고 하자.

```text
[0, 3, 7, 2, 0]
```

시간 계산은 route 순서대로 이동 시간을 누적하면서 진행한다.

```text
time = depot_start_time

0 -> 3 이동
arrival[3] = time + travel[0][3]

if arrival[3] < earliest[3]:
    wait until earliest[3]

if arrival[3] > latest[3]:
    time window violation

service_start[3] = max(arrival[3], earliest[3])
time = service_start[3] + service[3]
```

핵심은 다음이다.

- earliest보다 일찍 도착하면 기다릴 수 있다.
- latest보다 늦게 도착하면 hard time window에서는 infeasible이다.
- soft time window에서는 지연량에 penalty를 부여할 수 있다.

## 5. Feasibility 검사

### Capacity

각 route의 총 demand가 차량 capacity 이하인지 확인한다.

```text
sum(demand[i] for i in route) <= Q
```

이기종 차량이면 route마다 차량 capacity가 다르다.

```text
load(route_k) <= capacity[vehicle_k]
```

### Time Window

route 순서대로 도착 시간과 서비스 시작 시간을 계산하고, 각 고객에 대해 다음을 확인한다.

```text
service_start[i] <= latest[i]
```

earliest보다 빠른 도착은 waiting으로 처리한다.

### Depot Return

차량 근무시간이나 depot close time이 있다면 depot 복귀 시간도 확인해야 한다.

```text
return_time <= depot_latest
```

### Vehicle Compatibility

zone, 차량 유형, 기사 권한 같은 제약이 있으면 고객이 해당 차량에 배정 가능한지도 확인한다.

```text
allowed[customer][vehicle] = true / false
```

## 6. 목적함수

가장 단순한 목표는 총 이동거리 최소화다.

```text
minimize total_distance
```

하지만 CVRPTW에서는 보통 차량 수와 거리를 함께 고려한다.

```text
1순위: 사용 차량 수 최소화
2순위: 총 이동거리 최소화
```

대표적인 scalar 목적함수는 다음과 같다.

```text
Objective(solution)
= M * used_vehicle_count + total_distance
```

`M`은 거리 차이보다 차량 수 차이를 우선하게 만드는 충분히 큰 값이다.

실무에서는 penalty 기반 목적함수가 더 적합할 수 있다.

```text
totalCost =
    travelDistanceCost
  + vehicleUseCost
  + capacityViolationPenalty
  + timeWindowViolationPenalty
  + overtimePenalty
  + unservedPenalty
  + outsourcePenalty
  + zoneViolationPenalty
```

## 7. 비교 기준

해 비교는 단일 숫자보다 lexicographic key가 명확한 경우가 많다.

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

차량 수가 중요한 benchmark에서는 차량 수를 1차 기준으로 둔다. 실무형 문제에서는 미배송 penalty, 외주 비용, 우선순위 penalty가 차량 수보다 더 중요할 수도 있다.

## 8. Feasible-only와 Penalty 탐색

초기 구현에서는 feasible solution만 유지하는 방식이 단순하다.

```text
candidate가 feasible하면 평가
candidate가 infeasible하면 reject
```

장점:

- 구현과 디버깅이 쉽다.
- 최종 best solution의 유효성이 명확하다.

단점:

- 탐색 공간이 좁아질 수 있다.
- 제약이 빡빡한 문제에서 좋은 해 주변의 infeasible 구조를 활용하지 못한다.

Penalty 기반 탐색은 infeasible 해도 비용에 벌점을 붙여 일시적으로 허용한다.

```text
penalizedCost
= total_distance
 + capacity_penalty * capacity_violation
 + time_penalty * time_window_violation
```

HGS는 feasible/infeasible population을 함께 관리하는 방식이 대표적이다. ALNS도 성능 개선 단계에서는 penalty 기반으로 확장할 수 있다.

## 9. 왜 어려운가

CVRPTW는 다음 제약이 동시에 작동한다.

```text
차량 여러 대
+ 고객 배정
+ 방문 순서
+ 차량 용량
+ 시간창
+ 서비스 시간
+ depot 복귀
+ 차량/고객 호환성
```

가까운 고객부터 방문하는 단순 전략은 다음 이유로 실패할 수 있다.

- 가까운 고객을 먼저 방문하면 time window를 놓칠 수 있다.
- time window를 맞추려면 먼 고객을 먼저 가야 할 수 있다.
- 용량 때문에 가까운 고객을 같은 차량에 모두 넣을 수 없을 수 있다.
- zone 제약 때문에 지리적으로 가까워도 같은 차량이 처리할 수 없을 수 있다.

따라서 CVRPTW는 정확해법만으로 대규모 실무 문제를 풀기 어렵고, 휴리스틱과 메타휴리스틱이 중요하다.

## 10. 구현 시 기본 결정 사항

초기 구현을 시작할 때는 다음을 먼저 확정하는 것이 좋다.

| 결정 항목 | 초기 권장 |
|---|---|
| 목적함수 | 차량 수 우선 + 거리 최소화 |
| time window | earliest 이전 waiting 허용, latest 이후 infeasible |
| 차량 수 | 고정 K 또는 충분히 큰 K 중 선택 |
| infeasible 처리 | 1차는 feasible-only, 이후 penalty 확장 |
| 초기해 | regret insertion 중심 portfolio |
| ALNS acceptance | simulated annealing |
| 실무 차량 부족 | optional customer / dummy vehicle 모델로 확장 |

