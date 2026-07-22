# 06. 실무 확장 모델

## 1. 왜 확장이 필요한가

표준 CVRPTW는 보통 다음을 가정한다.

```text
모든 고객은 정확히 한 번 방문된다.
모든 차량은 같은 capacity를 가진다.
모든 차량은 모든 고객을 방문할 수 있다.
모든 고객은 hard time window 안에 서비스된다.
```

실무에서는 다음 제약이 자주 추가된다.

```text
- 차량별 용량이 다름
- 차량별 출발/도착지가 다름
- 기사별 근무시간이 다름
- 특정 고객은 특정 차량만 방문 가능
- 우선순위 고객 존재
- 지연 허용 penalty 존재
- 주문 취소/추가 등 동적 변경
- pickup & delivery 혼합
- 휴게시간 제약
- 권역/zone 제약
- 차량 부족과 demand 초과
- 미배송, 이월, 외주 허용
```

이 경우 문제는 단순 CVRPTW보다 **실무형 선택적 배차 최적화**에 가까워진다.

## 2. Zone 제약

Zone 제약은 차량별 방문 가능 지역이 제한되는 제약이다.

예:

```text
차량 A: 성동구만 배송 가능
차량 B: 종로구만 배송 가능
차량 C: 성동구 + 종로구 가능
차량 D: 전체 가능
```

이는 차량-고객 호환성으로 표현할 수 있다.

```text
allowed[customer][vehicle] = true / false
```

또는 성능을 위해 미리 가능한 차량 목록을 계산한다.

```text
customerAllowedVehicles[i] = {v1, v3, v7}
vehicleAllowedCustomers[k] = {c2, c5, c8, c10}
customerAllowedVehicleBitset[i]
```

## 3. Zone 제약이 local search에 주는 영향

Zone 제약은 route 간 이동 가능성을 크게 줄인다.

| Move | Zone 제약 확인 |
|---|---|
| Relocate | 고객이 이동 대상 차량에 배정 가능해야 함 |
| Swap | 두 고객이 각각 상대 차량에 배정 가능해야 함 |
| 2-opt* | 교환되는 tail의 모든 고객이 상대 차량과 호환되어야 함 |
| Cross-exchange | 교환 segment 전체가 상대 차량과 호환되어야 함 |
| SWAP* | 두 고객이 각각 상대 차량에 배정 가능해야 함 |

따라서 HGS의 inter-route local search 효과가 약해질 수 있고, crossover repair도 어려워진다.

ALNS는 repair 단계에서 compatibility를 자연스럽게 반영하기 쉬워 zone 제약이 강한 문제에 잘 맞는다.

## 4. Zone 대응 전략

### Zone별 subproblem 분해

차량이 완전히 zone 전용이라면 문제를 zone별로 나눌 수 있다.

```text
성동구 고객 + 성동구 차량 → CVRPTW subproblem 1
종로구 고객 + 종로구 차량 → CVRPTW subproblem 2
강남구 고객 + 강남구 차량 → CVRPTW subproblem 3
```

장점:

- 문제 크기 감소
- local search 효율 증가
- 구현 단순화
- 병렬 처리 가능
- zone별 SLA/차량 수 관리 쉬움

단, multi-zone 차량이 많으면 단순 분리가 어렵다.

### Zone-aware destroy

| Operator | 설명 |
|---|---|
| Zone removal | 특정 zone 고객 일부 제거 |
| Boundary removal | zone 경계 또는 인접 구역 고객 제거 |
| Compatible group removal | 같은 차량 후보군 고객 묶음 제거 |
| Flex vehicle removal | multi-zone 차량 route 제거 후 재구성 |

### Zone-aware repair

```text
1. 가능한 차량 수가 적은 고객 우선
2. Feasible insertion 위치가 적은 고객 우선
3. Time window가 좁은 고객 우선
4. Regret 값이 큰 고객 우선
```

추천 방식:

```text
Compatibility-aware regret insertion
```

## 5. 차량 부족과 수요 초과

실무에서는 다음 상황이 자주 발생할 수 있다.

```text
1. 차량 수가 부족함
2. 차량별 zone 제약이 있음
3. 차량 capacity보다 주문 demand가 자주 큼
4. 모든 주문을 정상 배차하는 것이 불가능함
```

표준 CVRPTW는 모든 고객 방문을 강제하므로 이런 경우 단순히 infeasible이 된다.

예:

```text
성동구 차량 capa = 100
성동구 주문 demand 합계 = 160
초과 수요 = 60
```

실무 solver는 “해 없음”만 반환하면 안 된다. 대신 다음을 알려야 한다.

```text
초과 수요
미배송 또는 이월 주문 후보
외주 필요량
추가 필요 차량 수
우선순위별 미처리 현황
```

## 6. Optional Customer 모델링

차량과 capacity가 부족하다면 모든 주문을 반드시 배차하는 모델에서 optional customer 모델로 바꾸는 것이 좋다.

기존 제약:

```text
모든 고객은 정확히 1회 방문되어야 한다.
```

확장 제약:

```text
각 고객은 다음 중 하나여야 한다.

1. 정규 차량에 배차됨
2. 미배송 처리됨
3. 이월 처리됨
4. 외주 또는 가상 차량에 배차됨
5. 분할 배송됨
```

간단한 표현:

```text
served[i] + unserved[i] = 1
```

외주와 이월을 나누면 다음과 같다.

```text
regularServed[i] + outsourced[i] + deferred[i] + unserved[i] = 1
```

이 관점은 다음 문제군과 가깝다.

```text
Prize-Collecting VRPTW
Selective VRPTW
Team Orienteering Problem with Time Windows
CVRPTW with optional customers
```

## 7. Dummy Vehicle / Outsourcing Vehicle

차량 부족 상황에서는 가상의 dummy vehicle 또는 outsourcing vehicle을 추가하는 방식이 유용하다.

```text
Real Vehicle 1: 성동구, capa 100
Real Vehicle 2: 종로구, capa 80
Dummy Vehicle: 전체 zone 가능, capa 매우 큼, 비용 매우 비쌈
```

Dummy vehicle에 배정된 주문은 실제로는 다음 의미를 가진다.

```text
- 미배송
- 이월
- 외주 필요
- 긴급 추가차 필요
```

장점:

| 장점 | 설명 |
|---|---|
| 항상 해가 나옴 | infeasible 실패 대신 운영안 생성 |
| 초과 수요 식별 | 어떤 주문이 정규 차량에 못 들어가는지 확인 |
| 운영 의사결정 가능 | 외주/이월/추가차 필요량 산출 |
| ALNS와 잘 맞음 | repair 단계에서 dummy fallback 가능 |
| 우선순위 반영 쉬움 | 주문별 dummy penalty 차등 설정 |

## 8. 목적함수 확장

실무형 문제는 단순 거리 최소화가 아니라 service level과 penalty를 함께 본다.

```text
totalCost =
    travelDistanceCost
  + unservedPenalty
  + outsourcePenalty
  + zoneViolationPenalty
  + capacityViolationPenalty
  + timeWindowViolationPenalty
  + overtimePenalty
```

Penalty 예:

```text
거리 1km = 1
일반 주문 미배송 = 10,000
VIP 주문 미배송 = 1,000,000
Zone 위반 = 500,000
Capacity 위반 = hard reject 또는 1,000,000
```

실차량 capacity 초과는 hard constraint로 두고, 초과 주문은 dummy / unserved / deferred / outsourced로 보내는 것이 안전하다.

## 9. 차량 부족 상황의 ALNS 설계

Repair 단계:

```text
1. 정규 차량에 삽입 가능하면 삽입
2. 정규 차량에 불가능하면 다른 compatible 차량 탐색
3. 그래도 안 되면 dummy vehicle에 배정
4. 또는 unassigned list에 남기고 penalty 부여
```

의사코드:

```text
for order in removedOrders sorted by priority and scarcity:
    best = null

    for vehicle in realVehicles:
        if !isAllowed(order, vehicle):
            continue

        candidate = findBestInsertion(order, vehicle)

        if candidate feasible and candidate.cost < best.cost:
            best = candidate

    if best exists:
        insert order using best
    else:
        assign order to dummy
```

필요한 destroy operator:

| Operator | 목적 |
|---|---|
| Low priority removal | 낮은 우선순위 주문 제거 후 고우선순위 주문 공간 확보 |
| Dummy reinsertion destroy | dummy 주문을 다시 정규 차량 후보로 꺼냄 |
| Capacity pressure removal | 큰 demand 주문 제거 후 작은 고우선순위 주문 삽입 가능성 탐색 |
| Zone pressure removal | 특정 zone 초과 수요를 집중 재구성 |
| Profit-aware removal | profit/capacity가 낮은 주문 제거 |

## 10. 미배정 허용 HGS 확장

원 HGS-CVRP의 해 표현은 모든 고객을 포함하는 순열이다.

```text
chromT = [모든 고객의 순열]
```

미배정 주문을 허용하려면 해 표현을 바꿔야 한다.

```text
servedT = [배정된 고객의 순열]
unserved = [미배정 고객 목록]
```

추가 local search move도 필요하다.

```text
미배정 주문을 route에 삽입
route의 주문을 미배정으로 제거
배정 주문과 미배정 주문을 교환
```

원 HGS-CVRP와 실무형 확장 HGS 비교:

| 항목 | 원 HGS-CVRP | 실무형 확장 |
|---|---|---|
| 모든 주문 방문 | 반드시 방문 | 선택 가능 |
| 미배정 주문 | 없음 | 있음 |
| penalty | 용량/시간 초과 | 용량/시간 초과 + 미배정 |
| 최종 답 | full-service feasible solution | service level과 비용 균형 |
| 차량 부족 시 | feasible 없으면 실패 가능 | 일부 주문 제외한 최선안 가능 |

## 11. Pre-check

solver 실행 전에 infeasibility pre-check를 넣는 것이 좋다.

### Zone별 demand / capacity

```text
zoneDemand[z] = 해당 zone 주문 demand 합
zoneCapacity[z] = 해당 zone 처리 가능 차량 capacity 합
```

```text
if zoneDemand[z] > zoneCapacity[z]:
    초과 수요 발생
```

multi-zone 차량이 있으면 capacity를 여러 zone에 중복 합산하지 않도록 주의해야 한다.

### Compatibility group check

가능한 차량 집합별로 주문을 묶는다.

```text
Group A: 가능한 차량 {V1}
Group B: 가능한 차량 {V2}
Group C: 가능한 차량 {V1, V3}
Group D: 가능한 차량 {V2, V3}
```

특히 가능한 차량이 1대뿐인 주문들이 중요하다.

```text
V1만 가능한 주문 demand 합 > V1 capacity
```

이면 일부 주문은 반드시 미배송, 이월, 외주 처리되어야 한다.

## 12. 최종 권장 구조

현재 조건이 다음과 같다면:

```text
CVRPTW
+ Zone compatibility
+ Limited fleet
+ Insufficient capacity
+ Optional / deferred orders
```

추천 구조:

```text
Compatibility-aware ALNS
+ Dummy vehicle
+ Unserved penalty
+ Priority-aware regret insertion
+ Zone pressure destroy
+ Dummy reinsertion operator
```

전체 흐름:

```text
Preprocessing
  - 고객별 zone 식별
  - 차량별 allowed zone 식별
  - customerAllowedVehicles 계산
  - zone별 demand / capacity 분석
  - compatibility group 분석

Initial Solution
  - 필수/우선순위 주문 먼저 삽입
  - 가능한 차량 수가 적은 주문 우선
  - regret insertion 사용
  - 불가능한 주문은 dummy/deferred에 배정

ALNS
  Destroy:
    random, worst, time-window conflict
    zone pressure, low priority, capacity pressure
    dummy reinsertion

  Repair:
    compatibility-aware greedy
    compatibility-aware regret-2 / regret-3
    priority-aware insertion
    penalty/demand ratio insertion
    dummy fallback

Local Search:
  - intra-route 2-opt
  - intra-route relocate
  - compatible inter-route relocate
  - compatible inter-route swap
  - dummy ↔ real vehicle exchange

Post Analysis:
  - 미배송 주문 목록
  - zone별 초과 demand
  - 추가 필요 차량 수
  - 외주 필요량
  - 우선순위별 미처리 현황
```

