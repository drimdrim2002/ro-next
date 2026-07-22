# 04. HGS 메타휴리스틱

## 1. HGS 개요

HGS는 **Hybrid Genetic Search**의 약자다.

VRP 계열에서 HGS는 단순한 유전 알고리즘이 아니라 다음 요소를 결합한 메타휴리스틱이다.

```text
HGS
= Genetic Search
+ Split
+ Local Search
+ Feasible/Infeasible population management
+ Diversity control
+ Dynamic penalty management
```

한 문장으로 요약하면 다음과 같다.

> HGS는 여러 배송계획 후보를 population으로 유지하면서, 부모 해를 섞어 자식 해를 만들고, local search로 강하게 개선하는 메타휴리스틱이다.

## 2. 전체 흐름

```text
1. 초기 population 생성
2. 부모 해 2개 선택
3. Crossover로 자식 giant tour 생성
4. Split으로 route list 변환
5. Local search 적용
6. Feasible / infeasible 여부 평가
7. 필요 시 repair
8. Population에 삽입
9. 품질과 다양성을 기준으로 population 관리
10. Penalty 동적 조정
11. 종료 조건까지 반복
```

의사코드:

```text
초기 해 여러 개 생성
각 해를 local search로 개선
feasible / infeasible population에 분류

while 종료 조건이 아니면:
    부모 P1, P2 선택
    OX crossover로 자식 순서 생성
    Split으로 route 구성
    local search로 개선
    infeasible이면 repair 시도 가능
    population에 삽입
    clone 또는 나쁜 해 제거
    penalty 조정

best feasible solution 반환
```

## 3. 해 표현: Giant Tour와 Route

CVRP/CVRPTW의 실제 해는 route list다.

```text
Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 6 → 0
Route 3: 0 → 7 → 8 → 10 → 4 → 0
```

하지만 crossover는 route list보다 고객 순열에서 다루기 쉽다. 그래서 depot을 제외한 긴 고객 순서, 즉 giant tour를 사용한다.

```text
Giant tour:
2  1  3  9  5  6  7  8  10  4
```

Split은 giant tour를 좋은 route 구분점으로 자른다.

```text
2  1  3 | 9  5  6 | 7  8  10  4

Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 6 → 0
Route 3: 0 → 7 → 8 → 10 → 4 → 0
```

## 4. Parent Selection과 OX Crossover

부모 선택은 binary tournament를 사용할 수 있다.

```text
1. population에서 해 2개를 무작위로 뽑는다.
2. fitness가 좋은 해를 선택한다.
3. 같은 과정을 한 번 더 해서 부모 2개를 얻는다.
```

fitness는 단순 거리만이 아니라 해 품질과 다양성을 함께 고려한다.

OX, Ordered Crossover는 한 부모의 일부 구간을 그대로 가져오고, 나머지는 다른 부모의 순서를 따라 채운다.

```text
부모 1:
1  2  3  4 [5  6  7  8] 9  10

부모 2:
5  4  6  2  1  3  8  9  7  10

자식:
_  _  _  _ [5  6  7  8] _  _
```

부모 2에서 이미 들어간 고객을 제외하고 남은 순서로 빈칸을 채운다.

## 5. Feasible / Infeasible Population

HGS는 feasible 해만 유지하지 않고 infeasible 해도 함께 관리한다.

```text
Population
├── Feasible subpopulation
└── Infeasible subpopulation
```

infeasible 해를 보관하는 이유는 좋은 feasible 해 근처에 있지만 현재는 capacity나 time window를 조금 위반한 해가 있을 수 있기 때문이다.

예:

```text
Route 1: A(6) + C(6) = 12   ← 용량 10 기준으로 2 초과
Route 2: B(4) + E(5) = 9
Route 3: D(4) + F(5) = 9
```

이 해는 infeasible이지만, 고객을 교환하면 feasible이 될 수 있다.

```text
Route 1: A(6) + B(4) = 10
Route 2: C(6) + D(4) = 10
Route 3: E(5) + F(5) = 10
```

## 6. Penalized Cost와 Repair

infeasible 해는 이동거리만으로 평가하지 않고 위반량에 penalty를 붙인다.

```text
penalized cost
= total_distance
+ capacityViolation * penaltyCapacity
+ durationViolation * penaltyDuration
+ timeWindowViolation * penaltyTime
```

infeasible 해가 너무 많으면 penalty를 높여 feasible 쪽으로 밀고, feasible 해가 너무 많으면 penalty를 완화해 탐색 폭을 넓힐 수 있다.

Repair는 infeasible 해를 feasible 쪽으로 유도하는 단계다.

```text
Local search 후 해가 infeasible
    ↓
capacity/time penalty를 크게 높임
    ↓
local search 재실행
    ↓
위반 감소 또는 feasible 복구 시도
```

## 7. Population Management와 Diversity

좋은 해만 남기면 population이 비슷한 해로 가득 차고, crossover가 새 구조를 만들기 어려워진다.

```text
좋은 해만 남김
→ 모두 비슷해짐
→ 새로운 조합 감소
→ 조기 수렴
```

따라서 HGS는 품질과 다양성을 함께 본다.

```text
좋은 해 + 다른 해들과 충분히 다른 해
→ population에 남길 가치가 큼
```

population이 너무 커지면 다음 순서로 정리한다.

```text
1. clone 제거
2. clone이 없으면 fitness가 나쁜 해 제거
```

다양성은 route 구조나 고객 인접 관계 차이로 측정할 수 있다.

## 8. Local Search의 역할

HGS 성능은 local search 품질에 크게 의존한다.

대표 move:

```text
Relocate
Swap
2-opt
2-opt*
Or-opt
Cross-exchange
SWAP*
```

SWAP*는 HGS-CVRP 논문의 핵심 기여다. 일반 Swap은 서로의 기존 위치를 교환하지만, SWAP*는 서로 route를 바꾼 뒤 각자 상대 route 안의 좋은 위치에 들어간다.

상세 move 설명은 `05_local_search_moves.md`에 통합했다.

## 9. 차량 수가 빡빡한 경우

표준 HGS-CVRP에는 미배정 주문이라는 개념이 없다.

```text
모든 고객은 정확히 한 번 방문되어야 한다.
```

차량 수가 빡빡하더라도 HGS는 모든 주문을 일단 배정하고, 용량 초과가 생기면 infeasible solution으로 취급한다.

상황은 세 가지로 나뉜다.

| 상황 | 의미 | 처리 |
|---|---|---|
| 물리적으로 불가능 | 전체 용량이 수요보다 작음 | feasible solution 없음 |
| 초기 생성이 부족 | 실제로는 가능한데 초기해가 못 찾음 | infeasible 탐색과 repair로 개선 가능 |
| 일부 주문 포기 가능 | 미배정/이월/외주 허용 | 원 CVRP가 아니므로 모델 수정 필요 |

차량 부족과 optional customer 문제는 `06_practical_extensions.md`에서 다룬다.

## 10. HGS와 ALNS 비교

| 항목 | HGS | ALNS |
|---|---|---|
| 기본 방식 | Population 기반 진화 탐색 | Single-solution 기반 destroy & repair |
| 핵심 연산 | Crossover + local search | Removal + insertion |
| 탐색 구조 | 여러 해를 동시에 진화 | 하나의 해를 반복 개선 |
| 강점 | 해 품질, 벤치마크 성능 | 유연성, 확장성, 실무 적용성 |
| 구현 난이도 | 높음 | 중간 |
| 제약 추가 | 상대적으로 어려움 | 상대적으로 쉬움 |
| 디버깅 | 어려움 | 비교적 쉬움 |
| 빠른 개발 | 불리 | 유리 |

표준 CVRPTW에 가깝고 최고 품질이 목표라면 HGS가 강력하다.

```text
단일 depot
동일한 차량 용량
hard time window
모든 고객 1회 방문
목적: 차량 수 최소화 + 거리 최소화
```

하지만 zone, 이기종 차량, 우선순위, 미배송, 외주, 동적 주문 같은 실무 제약이 강하면 ALNS가 더 다루기 쉽다.

## 11. HGS 적용 시 주의점

- crossover가 복잡한 hard constraint를 깨뜨릴 수 있다.
- Split은 CVRP에서는 효율적이지만, time window와 compatibility가 강해지면 더 어려워진다.
- infeasible population은 강력하지만 penalty 튜닝이 필요하다.
- 미배정 주문이 필요한 실무 문제는 원 HGS-CVRP 구조를 수정해야 한다.
- 표준 CVRP benchmark 성능이 좋다고 해서 실무 CVRPTW 확장에서도 그대로 우수하다고 단정하면 안 된다.

