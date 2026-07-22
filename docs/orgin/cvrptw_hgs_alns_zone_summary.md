# CVRPTW, HGS, ALNS 및 Zone 제약 대응 정리

## 1. CVRPTW 개요

CVRPTW는 **Capacitated Vehicle Routing Problem with Time Windows**의 약자이며, 한국어로는 **시간창 제약이 있는 용량 제한 차량 경로 문제**라고 할 수 있다.

여러 대의 차량이 여러 고객 또는 대리점을 방문해야 할 때, 각 차량의 적재 용량과 고객별 방문 가능 시간대를 동시에 만족하면서 전체 운행 비용, 거리, 시간 등을 최소화하는 문제이다.

### 1.1 기본 구성 요소

| 구성 요소 | 설명 |
|---|---|
| Depot | 차량이 출발하고 복귀하는 장소 |
| Customer / Order | 방문해야 하는 고객 또는 주문 |
| Vehicle | 배송에 사용되는 차량 |
| Demand | 각 고객 또는 주문의 수요량 |
| Capacity | 차량이 실을 수 있는 최대 적재량 |
| Time Window | 고객 방문 또는 서비스가 가능한 시간 구간 |
| Service Time | 고객에게 배송 또는 서비스를 수행하는 데 필요한 시간 |
| Travel Time / Distance | 고객 간 이동 시간 또는 거리 |

### 1.2 대표 제약 조건

CVRPTW의 핵심 제약은 다음과 같다.

1. 모든 고객은 정확히 한 번 방문되어야 한다.
2. 각 차량은 depot에서 출발하고 depot으로 복귀해야 한다.
3. 차량별 적재량은 차량 capacity를 초과할 수 없다.
4. 고객은 지정된 time window 안에서 서비스되어야 한다.
5. 이동 시간, 서비스 시간, 대기 시간을 고려해야 한다.

### 1.3 목적 함수

일반적으로 다음 목적들을 고려한다.

| 목적 | 설명 |
|---|---|
| 차량 수 최소화 | 가능한 적은 차량으로 배송 수행 |
| 총 이동 거리 최소화 | 전체 차량의 이동 거리 합 최소화 |
| 총 운행 시간 최소화 | 전체 운행 시간 최소화 |
| 대기 시간 최소화 | time window 때문에 발생하는 대기 시간 감소 |
| 지연 또는 위반 최소화 | soft time window인 경우 지연 penalty 최소화 |
| 비용 최소화 | 거리, 차량 사용, 외주, 미배송 penalty 등을 통합 |

실무에서는 단일 목적보다는 여러 목적을 계층적으로 적용하는 경우가 많다.

예시:

1. 필수 주문 미배송 최소화
2. 차량 수 최소화
3. time window 위반 최소화
4. 총 이동 거리 최소화
5. 대기 시간 최소화

---

## 2. HGS 알고리즘

HGS는 **Hybrid Genetic Search**의 약자이다.

유전 알고리즘을 기반으로 하지만, 단순한 GA가 아니라 다음 요소를 결합한 강력한 메타휴리스틱이다.

- Population 기반 탐색
- Crossover
- Local Search
- Diversity 관리
- Feasible / infeasible solution 관리
- 동적 penalty 조정

### 2.1 HGS의 기본 아이디어

HGS는 여러 개의 해를 population으로 유지한다.

각 해는 여러 차량 route의 집합이다.

```text
Solution A:
Vehicle 1: Depot → 3 → 7 → 2 → Depot
Vehicle 2: Depot → 5 → 1 → 4 → 6 → Depot
```

좋은 해들을 선택하고, crossover를 통해 새로운 해를 만들며, local search로 개선한다.

### 2.2 HGS의 일반 흐름

```text
1. 초기 population 생성
2. 부모 해 2개 선택
3. Crossover로 자식 해 생성
4. 자식 해에 local search 적용
5. Feasible / infeasible 여부 평가
6. Population에 삽입
7. 품질과 다양성을 기준으로 population 관리
8. Penalty 동적 조정
9. 종료 조건까지 반복
```

### 2.3 HGS의 주요 구성 요소

#### Population

HGS는 하나의 해만 개선하지 않고 여러 해를 동시에 유지한다.

좋은 population은 다음 두 가지를 모두 만족해야 한다.

```text
좋은 품질의 해 + 서로 다양한 구조의 해
```

#### Crossover

부모 해 2개를 조합하여 자식 해를 생성한다.

VRP 계열에서는 단순 배열 교차를 사용할 수 없다. 고객이 중복되거나 누락될 수 있고, 차량 route 구조와 제약을 유지해야 하기 때문이다.

#### Local Search

HGS의 성능은 local search 품질에 크게 의존한다.

대표 move는 다음과 같다.

| Move | 설명 |
|---|---|
| Relocate | 고객 하나를 다른 위치로 이동 |
| Swap | 두 고객의 위치 교환 |
| 2-opt | 한 route 내부의 순서 일부 반전 |
| 2-opt* | 서로 다른 두 route의 tail 교환 |
| Or-opt | 연속된 고객 묶음을 이동 |
| Cross-exchange | 두 route의 부분 경로 교환 |
| SWAP* | 서로 다른 route의 고객을 효율적으로 교환 |

#### Feasible / Infeasible Population

HGS는 feasible 해만 유지하지 않고, 일부 제약을 위반한 infeasible 해도 함께 관리할 수 있다.

```text
Feasible population:
  모든 제약을 만족하는 해

Infeasible population:
  일부 capacity, time window 등을 위반하지만 개선 가능성이 있는 해
```

이 방식은 탐색 범위를 넓혀 지역 최적해에 갇히는 문제를 줄인다.

#### Penalty Management

HGS는 penalty 계수를 고정하지 않고 탐색 상황에 따라 조정한다.

예시:

```text
cost = distance
     + capacityViolationPenalty
     + timeWindowViolationPenalty
```

Feasible 해가 너무 적으면 penalty를 낮추고, infeasible 해가 너무 많으면 penalty를 높이는 방식으로 균형을 맞춘다.

### 2.4 HGS의 장점

| 장점 | 설명 |
|---|---|
| 해 품질이 높음 | VRP 계열 벤치마크에서 강력한 성능 |
| 전역 탐색과 지역 탐색의 균형 | Crossover와 local search를 결합 |
| 다양성 관리 가능 | 비슷한 해로만 수렴하는 것을 방지 |
| Infeasible 탐색 가능 | 제약이 빡빡한 문제에서도 탐색 여지 확보 |
| 표준 VRP 계열에 강함 | CVRP, VRPTW 등에서 검증된 사례가 많음 |

### 2.5 HGS의 단점

| 단점 | 설명 |
|---|---|
| 구현 난이도 높음 | Crossover, diversity, penalty, local search를 모두 구현해야 함 |
| 디버깅 어려움 | Population 기반이라 원인 분석이 복잡함 |
| 제약 추가가 까다로움 | 새로운 제약이 crossover와 local search에 모두 영향을 줌 |
| 튜닝 요소 많음 | Population size, penalty, diversity 기준 등 |
| 빠른 프로토타입에는 부적합 | 처음부터 안정적으로 만들기 어렵다 |

---

## 3. ALNS 알고리즘

ALNS는 **Adaptive Large Neighborhood Search**의 약자이다.

핵심 아이디어는 현재 해의 일부를 제거하고, 다시 삽입하면서 더 좋은 해를 찾는 것이다.

```text
현재 해 → 일부 고객 제거 → 다시 삽입 → 개선 여부 평가
```

### 3.1 ALNS의 기본 아이디어

예를 들어 현재 해가 다음과 같다고 하자.

```text
Vehicle 1: Depot → 1 → 2 → 3 → Depot
Vehicle 2: Depot → 4 → 5 → 6 → Depot
Vehicle 3: Depot → 7 → 8 → 9 → Depot
```

일부 고객을 제거한다.

```text
Removed customers: 2, 5, 8
```

남은 해는 다음과 같다.

```text
Vehicle 1: Depot → 1 → 3 → Depot
Vehicle 2: Depot → 4 → 6 → Depot
Vehicle 3: Depot → 7 → 9 → Depot
```

그 후 제거된 고객을 다시 적절한 위치에 삽입한다.

### 3.2 ALNS의 일반 흐름

```text
1. 초기해 생성
2. Destroy operator 선택
3. 현재 해에서 일부 고객 제거
4. Repair operator 선택
5. 제거된 고객 재삽입
6. 후보 해 평가
7. Acceptance criterion으로 수락 여부 결정
8. Operator 점수 업데이트
9. 종료 조건까지 반복
```

### 3.3 Destroy Operator

Destroy operator는 해를 일부 망가뜨리는 연산이다.

| Destroy 방식 | 설명 |
|---|---|
| Random removal | 고객을 무작위로 제거 |
| Worst removal | 비용 증가를 크게 유발하는 고객 제거 |
| Shaw removal | 서로 유사한 고객들을 함께 제거 |
| Route removal | 특정 route 전체 또는 일부 제거 |
| Time-window removal | 시간창이 비슷하거나 충돌하는 고객 제거 |
| Cluster removal | 지리적으로 가까운 고객 묶음 제거 |

CVRPTW에서는 time window 충돌을 해결하기 위한 removal이 특히 중요하다.

### 3.4 Repair Operator

Repair operator는 제거된 고객을 다시 삽입하는 연산이다.

| Repair 방식 | 설명 |
|---|---|
| Greedy insertion | 삽입 비용이 가장 작은 위치에 삽입 |
| Regret-2 insertion | 지금 넣지 않으면 손해가 큰 고객을 우선 삽입 |
| Regret-k insertion | 여러 후보 위치 간 regret 값 기준 |
| Time-oriented insertion | 시간창 만족 가능성을 우선 고려 |
| Noise insertion | 랜덤성을 추가해 탐색 다양성 확보 |

CVRPTW에서는 단순 greedy보다 regret insertion이 유리한 경우가 많다.

### 3.5 Adaptive Mechanism

ALNS의 “Adaptive”는 operator 선택 확률을 학습한다는 뜻이다.

좋은 해를 자주 만드는 destroy/repair operator의 점수를 높이고, 선택 확률을 증가시킨다.

```text
효과적인 operator → 선택 확률 증가
효과가 낮은 operator → 선택 확률 감소
```

### 3.6 Acceptance Criterion

후보 해가 현재 해보다 좋으면 수락한다.

하지만 나쁜 해도 일정 확률로 수락할 수 있다.

대표 방식:

| 방식 | 설명 |
|---|---|
| Hill climbing | 좋아지는 해만 수락 |
| Simulated annealing | 나빠지는 해도 확률적으로 수락 |
| Record-to-record travel | 현재 최고해와 일정 차이 이내면 수락 |
| Threshold accepting | 허용 임계값 이내면 수락 |

### 3.7 ALNS의 장점

| 장점 | 설명 |
|---|---|
| 구현 구조가 직관적 | Destroy / repair 구조가 명확함 |
| 제약 추가가 쉬움 | Feasibility check, insertion cost에 반영 가능 |
| 실무 확장성이 높음 | Zone, pickup-delivery, heterogeneous fleet 등에 적합 |
| 디버깅이 비교적 쉬움 | 어떤 operator가 효과적인지 분석 가능 |
| 빠른 프로토타입에 적합 | 단계적으로 구현 가능 |

### 3.8 ALNS의 단점

| 단점 | 설명 |
|---|---|
| 성능이 operator 설계에 의존 | Destroy/repair 품질이 중요 |
| 초기해에 민감할 수 있음 | 좋은 초기해가 성능에 영향 |
| 파라미터 튜닝 필요 | 제거 비율, 온도, operator weight 등 |
| 최고 벤치마크 성능은 HGS에 밀릴 수 있음 | 고도로 튜닝된 HGS가 더 강한 경우 있음 |

---

## 4. HGS와 ALNS 비교

| 항목 | HGS | ALNS |
|---|---|---|
| 기본 방식 | Population 기반 진화 탐색 | Single-solution 기반 destroy & repair |
| 핵심 연산 | Crossover + local search | Removal + insertion |
| 탐색 구조 | 여러 해를 동시에 진화 | 하나의 해를 반복 개선 |
| 강점 | 해 품질, 벤치마크 성능 | 유연성, 확장성, 실무 적용성 |
| 구현 난이도 | 높음 | 중간 |
| 제약 추가 | 상대적으로 어려움 | 상대적으로 쉬움 |
| 디버깅 | 어려움 | 비교적 쉬움 |
| 실무 커스터마이징 | 어렵지만 강력 | 매우 좋음 |
| 빠른 개발 | 불리 | 유리 |
| 최고 품질 목표 | 유리 | 경우에 따라 불리 |
| 복잡한 현실 제약 | 구현 부담 큼 | 유리 |

### 4.1 표준 CVRPTW에서는?

표준적인 CVRPTW라면 HGS가 매우 강력하다.

예를 들어:

```text
- 단일 depot
- 동일한 차량 용량
- hard time window
- 모든 고객 1회 방문
- 목적: 차량 수 최소화 + 거리 최소화
- Solomon / Homberger benchmark와 유사
```

이런 경우는 HGS가 좋은 선택이다.

### 4.2 실무형 CVRPTW에서는?

실무 문제에서는 다음과 같은 제약이 자주 추가된다.

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
```

이 경우 ALNS가 더 적합하다.

---

## 5. Zone 제약이 추가된 CVRPTW

Zone 제약은 차량별로 방문 가능한 지역이 제한되는 제약이다.

예시:

```text
차량 A: 서울 성동구 대리점만 배송 가능
차량 B: 종로구 대리점만 배송 가능
차량 C: 성동구 + 종로구 배송 가능
차량 D: 전체 가능
```

이를 차량-고객 호환성 제약으로 표현할 수 있다.

```text
allowed[customer][vehicle] = true / false
```

즉, 고객 `i`는 `allowed[i][k] = true`인 차량 `k`에만 배정될 수 있다.

### 5.1 Zone 제약이 local search에 주는 영향

Zone 제약이 강하면 route 간 이동이 많이 제한된다.

| Move | Zone 제약 영향 |
|---|---|
| Relocate | 고객을 다른 차량 route로 옮길 수 있는 후보가 줄어듦 |
| Swap | 두 고객이 서로의 차량에 들어갈 수 있어야 함 |
| 2-opt* | 교환되는 tail의 모든 고객이 상대 차량에 들어갈 수 있어야 함 |
| Cross-exchange | 교환 구간 전체가 상대 차량 zone과 호환되어야 함 |
| SWAP* | 두 고객이 각각 상대 차량에 들어갈 수 있어야 함 |

따라서 HGS의 강점인 강력한 inter-route local search 효과가 약해질 수 있다.

### 5.2 Zone 제약에서 HGS의 어려움

HGS에서는 crossover가 zone infeasible한 자식 해를 만들 가능성이 있다.

Crossover 후에는 다음을 복구해야 한다.

```text
1. 중복 고객 제거
2. 누락 고객 삽입
3. Capacity 복구
4. Time window 복구
5. Zone compatibility 복구
```

Zone 제약이 hard constraint라면 repair 실패 가능성이 커지고 구현 난이도도 높아진다.

### 5.3 Zone 제약에서 ALNS의 장점

ALNS는 repair 단계에서 zone 제약을 자연스럽게 반영할 수 있다.

```text
고객 i를 삽입할 때
→ 고객 i가 배정 가능한 차량만 후보로 평가
```

예시:

```java
if (!allowedVehicle(customer, vehicle)) {
    continue;
}
```

따라서 zone 제약이 강한 실무 문제에서는 ALNS가 더 적합하다.

---

## 6. Zone 제약 대응 전략

### 6.1 Zone별 subproblem 분해

차량이 완전히 zone 전용이라면 문제를 zone별로 나누는 것이 좋다.

```text
성동구 고객 + 성동구 차량 → CVRPTW subproblem 1
종로구 고객 + 종로구 차량 → CVRPTW subproblem 2
강남구 고객 + 강남구 차량 → CVRPTW subproblem 3
```

장점:

```text
- 문제 크기 감소
- Local search 효율 증가
- 구현 쉬움
- 병렬 처리 가능
- Zone별 SLA/차량 수 관리 쉬움
```

단, multi-zone 차량이나 예외 차량이 많다면 단순 분리는 어렵다.

### 6.2 Vehicle compatibility matrix 사용

문자열로 zone을 매번 비교하지 말고, 고객별 가능한 차량 목록을 미리 계산해야 한다.

```text
customerAllowedVehicles[i] = {v1, v3, v7}
vehicleAllowedCustomers[k] = {c2, c5, c8, c10}
```

성능을 위해 bitset 형태로 관리할 수도 있다.

```text
customerAllowedVehicleBitset[i]
```

### 6.3 Zone-aware destroy operator

Zone 제약이 강한 경우 일반 random removal만으로는 충분하지 않다.

추가할 수 있는 operator:

| Operator | 설명 |
|---|---|
| Zone removal | 특정 zone의 고객 일부 제거 |
| Boundary removal | zone 경계 또는 인접 구역 고객 제거 |
| Compatible group removal | 같은 차량 후보군을 가진 고객 묶음 제거 |
| Flex vehicle removal | multi-zone 차량 route를 제거하고 재구성 |

### 6.4 Zone-aware repair insertion

Repair에서는 다음 고객을 우선 처리하는 것이 좋다.

```text
1. 가능한 차량 수가 적은 고객
2. Feasible insertion 위치가 적은 고객
3. Time window가 좁은 고객
4. Regret 값이 큰 고객
```

추천 방식:

```text
Compatibility-aware regret insertion
```

### 6.5 Local search 후보 필터링

Inter-route move를 평가하기 전에 compatibility를 확인해야 한다.

#### Relocate

```text
고객 i를 route A에서 route B로 이동
```

가능 조건:

```text
고객 i가 route B의 vehicle에 배정 가능해야 함
```

#### Swap

```text
route A의 고객 i와 route B의 고객 j 교환
```

가능 조건:

```text
고객 i가 route B의 vehicle에 가능
고객 j가 route A의 vehicle에 가능
```

#### 2-opt*

```text
route A의 tail과 route B의 tail 교환
```

가능 조건:

```text
route A로 들어오는 tail의 모든 고객이 vehicle A에 가능
route B로 들어오는 tail의 모든 고객이 vehicle B에 가능
```

---

## 7. 차량 부족 및 수요 초과 상황

현재 조건은 일반 CVRPTW보다 더 어려운 실무형 문제이다.

```text
1. 차량 수가 부족함
2. 차량별 zone 제약이 있음
3. 차량 capacity보다 주문 demand가 자주 큼
4. 모든 주문을 정상 배차하는 것이 불가능한 경우가 자주 발생함
```

이 경우 solver는 단순히 “가능한 route”를 찾는 것이 아니라 다음 의사결정을 함께 해야 한다.

```text
어떤 주문을 배송하고,
어떤 주문을 미배송/이월/외주/분할 처리할 것인가?
```

### 7.1 기존 CVRPTW의 한계

기존 CVRPTW는 보통 다음을 전제로 한다.

```text
모든 고객은 정확히 1회 방문되어야 한다.
```

하지만 차량 capacity가 부족하면 해가 없을 수 있다.

예시:

```text
성동구 차량 capa = 100
성동구 주문 demand 합계 = 160
```

이 경우 일반 CVRPTW는 infeasible이 된다.

실무 시스템은 단순히 “해 없음”을 반환하면 안 된다.

대신 다음과 같은 운영안을 제시해야 한다.

```text
성동구 총 수요: 160
가용 capacity: 100
초과 수요: 60
미배송 또는 이월 주문 후보
추가 필요 차량 또는 외주 필요량
```

---

## 8. Optional customer 모델링

차량이 부족하고 demand 초과가 빈번하다면, 모든 주문을 반드시 배차하는 모델이 아니라 optional customer 모델로 바꾸는 것이 좋다.

기존 제약:

```text
모든 고객은 정확히 1회 방문되어야 한다.
```

변경된 제약:

```text
각 고객은 다음 중 하나여야 한다.

1. 정규 차량에 배차됨
2. 미배송 처리됨
3. 이월 처리됨
4. 외주 또는 가상 차량에 배차됨
5. 분할 배송됨
```

간단히 표현하면 다음과 같다.

```text
served[i] + unserved[i] = 1
```

외주와 이월까지 포함하면 다음처럼 볼 수 있다.

```text
regularServed[i] + outsourced[i] + deferred[i] + unserved[i] = 1
```

---

## 9. Dummy vehicle / outsourcing vehicle 전략

차량 부족 상황에서는 **dummy vehicle** 또는 **outsourcing vehicle**을 추가하는 방식이 유용하다.

### 9.1 개념

실제 차량 외에 가상의 차량을 추가한다.

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

### 9.2 장점

| 장점 | 설명 |
|---|---|
| 항상 해가 나옴 | Infeasible로 실패하지 않음 |
| 초과 수요 식별 가능 | 어떤 주문이 정규 차량에 못 들어가는지 알 수 있음 |
| 운영 의사결정 가능 | 외주/이월/추가차 필요량 산출 |
| ALNS와 잘 맞음 | Repair 단계에서 dummy 삽입 가능 |
| 우선순위 반영 쉬움 | 주문별 dummy penalty를 다르게 설정 가능 |

### 9.3 예시

```text
성동구 주문:
A demand 40, priority high
B demand 50, priority normal
C demand 30, priority low
D demand 40, priority low

성동구 차량 capa 100
총 demand = 160
```

결과 예시:

```text
성동구 차량:
A(40) → B(50)
총 적재 = 90

Dummy / Deferred:
C(30), D(40)
총 미처리 = 70
```

---

## 10. 차량 부족 상황에서의 목적 함수

이 상황에서는 단순 이동 거리 최소화가 아니라, 미배송과 외주를 포함한 목적 함수가 필요하다.

예시:

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

Penalty 크기 설정이 매우 중요하다.

예시:

```text
거리 1km = 1
일반 주문 미배송 = 10,000
VIP 주문 미배송 = 1,000,000
Zone 위반 = 500,000
Capacity 위반 = hard reject 또는 1,000,000
```

실제 차량의 capacity 초과는 기본적으로 hard constraint로 두는 것이 안전하다.

```text
실차량 capacity 초과는 불가
초과 주문은 dummy / unserved / deferred / outsourced로 처리
```

---

## 11. 차량 부족 상황에서 ALNS 설계

이 조건에서는 ALNS가 매우 적합하다.

Repair 단계에서 다음 로직을 자연스럽게 구현할 수 있다.

```text
1. 정규 차량에 삽입 가능하면 삽입
2. 정규 차량에 불가능하면 다른 compatible 차량 탐색
3. 그래도 안 되면 dummy vehicle에 배정
4. 또는 unassigned list에 남기고 penalty 부여
```

### 11.1 Repair 의사코드

```java
Solution repair(Solution partial, List<Order> removedOrders) {
    sortByPriorityAndScarcity(removedOrders);

    for (Order order : removedOrders) {
        Insertion best = null;

        for (Vehicle vehicle : realVehicles) {
            if (!isAllowed(order, vehicle)) continue;

            Insertion candidate = findBestInsertion(order, vehicle);

            if (candidate.isFeasibleCapacity()
                && candidate.isFeasibleTimeWindow()
                && candidate.cost < best.cost) {
                best = candidate;
            }
        }

        if (best != null) {
            partial.insert(order, best);
        } else {
            partial.assignToDummy(order);
        }
    }

    return partial;
}
```

### 11.2 주문 삽입 우선순위

차량 capa가 부족하면 어떤 주문을 먼저 넣을지가 중요하다.

추천 우선순위:

```text
1. 필수 배송 주문
2. 우선순위 높은 주문
3. 가능한 차량 수가 적은 주문
4. Time window가 좁은 주문
5. 미배송 penalty가 큰 주문
6. Penalty / demand 비율이 큰 주문
```

실무에서는 다음 기준이 유용하다.

```text
unservedPenalty / demand
```

또는

```text
unservedPenalty / insertionCost
```

---

## 12. 차량 부족 상황에 필요한 Destroy Operator

일반 ALNS destroy만으로는 부족하다.

다음 operator가 필요하다.

### 12.1 Low priority removal

낮은 우선순위 주문을 route에서 제거한다.

```text
현재 정규 차량에 들어간 low priority 주문 제거
→ high priority dummy 주문을 넣을 공간 확보
```

### 12.2 Dummy reinsertion destroy

Dummy vehicle에 있는 주문 일부를 다시 후보로 꺼낸다.

```text
Dummy에 있는 주문 제거
→ 정규 차량에 다시 삽입 시도
```

이 operator는 매우 중요하다.

한 번 dummy로 간 주문이 계속 dummy에 남아 있으면 안 된다.

### 12.3 Capacity pressure removal

Capacity를 많이 차지하는 주문을 일부 제거한다.

```text
큰 demand 주문 제거
→ 작은 고우선순위 주문 여러 개 삽입 가능성 탐색
```

### 12.4 Zone pressure removal

특정 zone에서 초과 수요가 발생하면 해당 zone의 주문을 집중적으로 제거하고 재삽입한다.

```text
성동구 demand 160, capa 100
성동구 route와 dummy 주문을 함께 destroy
→ 다시 재구성
```

### 12.5 Profit-aware removal

배송 가치가 낮은 주문을 제거한다.

```text
profit / capacity 사용량이 낮은 주문 제거
```

---

## 13. Prize-Collecting / Selective VRPTW 관점

차량 수와 capacity가 부족해 모든 주문을 배송할 수 없다면, 문제는 일반 CVRPTW보다 다음 문제에 가깝다.

```text
Prize-Collecting VRPTW
Selective VRPTW
Team Orienteering Problem with Time Windows
CVRPTW with optional customers
```

즉, 모든 고객을 반드시 방문하는 것이 아니라:

```text
제한된 차량과 capacity 안에서
어떤 고객을 선택해서 방문할 것인가?
```

가 핵심이다.

따라서 목적은 다음처럼 바뀐다.

```text
방문한 고객의 가치 최대화
+ 이동 비용 최소화
+ 미방문 penalty 최소화
```

---

## 14. Pre-check의 중요성

Solver를 돌리기 전에 간단한 infeasibility pre-check를 넣는 것이 좋다.

### 14.1 Zone별 demand / capacity check

```text
zoneDemand[z] = 해당 zone 주문 demand 합
zoneCapacity[z] = 해당 zone 처리 가능 차량 capacity 합
```

```text
if zoneDemand[z] > zoneCapacity[z]:
    초과 수요 발생
```

단, multi-zone 차량이 있으면 단순 합산은 과대평가될 수 있다.

예:

```text
차량 C: 성동구 + 종로구 가능
```

이 차량 capacity를 성동구와 종로구에 동시에 더하면 실제보다 capacity가 크게 계산된다.

### 14.2 Compatibility group check

더 좋은 방식은 가능한 차량 집합별로 주문을 묶는 것이다.

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

---

## 15. 최종 권장 설계

현재 조건을 종합하면 문제는 다음과 같다.

```text
CVRPTW
+ Zone compatibility
+ Limited fleet
+ Insufficient capacity
+ Optional / deferred orders
```

따라서 추천 설계는 다음과 같다.

```text
Compatibility-aware ALNS
+ Dummy vehicle
+ Unserved penalty
+ Priority-aware regret insertion
+ Zone pressure destroy
+ Dummy reinsertion operator
```

### 15.1 전체 구조

```text
Preprocessing
  1. 고객별 zone 식별
  2. 차량별 allowed zone 식별
  3. customerAllowedVehicles 계산
  4. zone별 demand / capacity 분석
  5. compatibility group 분석

Initial Solution
  1. 필수/우선순위 주문 먼저 삽입
  2. 가능한 차량 수가 적은 주문 우선
  3. Regret insertion 사용
  4. 불가능한 주문은 dummy/deferred에 배정

ALNS
  Destroy:
    - random removal
    - worst removal
    - time-window conflict removal
    - zone pressure removal
    - low priority removal
    - capacity pressure removal
    - dummy reinsertion removal

  Repair:
    - compatibility-aware greedy insertion
    - compatibility-aware regret-2 / regret-3
    - priority-aware insertion
    - penalty/demand ratio insertion
    - dummy fallback

Local Search:
  - intra-route 2-opt
  - intra-route relocate
  - intra-route Or-opt
  - compatible inter-route relocate
  - compatible inter-route swap
  - dummy ↔ real vehicle exchange

Acceptance:
  - simulated annealing
  또는
  - record-to-record travel

Post Analysis:
  1. 미배송 주문 목록
  2. zone별 초과 demand
  3. 추가 필요 차량 수
  4. 외주 필요량
  5. 우선순위별 미처리 현황
```

---

## 16. 최종 결론

### 16.1 표준 CVRPTW라면

표준 CVRPTW에서 최고 품질 해와 벤치마크 성능이 목표라면 HGS가 강력하다.

```text
표준 문제 + 최고 품질 목표 → HGS 추천
```

### 16.2 Zone 제약이 강하다면

Zone 제약은 local search와 crossover를 크게 제한한다.

이 경우 제약 반영과 확장이 쉬운 ALNS가 더 적합하다.

```text
Zone-restricted CVRPTW → ALNS 추천
```

### 16.3 차량 부족과 demand 초과가 빈번하다면

이 경우는 모든 주문을 배송하는 CVRPTW가 아니라, 선택적 배송과 미배송/이월/외주 판단이 필요한 문제이다.

```text
Limited fleet + insufficient capacity → Selective / Prize-Collecting VRPTW 관점 필요
```

가장 현실적인 추천은 다음이다.

```text
Compatibility-aware ALNS
+ Optional customer modeling
+ Dummy vehicle
+ Priority-aware regret insertion
+ Zone pressure destroy
+ Dummy reinsertion operator
```

### 16.4 한 줄 요약

차량과 capacity가 부족하고 zone 제약이 강한 실무형 CVRPTW에서는, 모든 주문을 무조건 배차하려는 HGS보다 **제한된 차량으로 중요한 주문을 최대한 배차하고, 남는 주문을 미배송/이월/외주로 분류할 수 있는 ALNS 기반 구조**가 더 적합하다.
