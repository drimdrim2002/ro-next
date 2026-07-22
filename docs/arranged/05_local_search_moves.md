# 05. Local Search와 Neighborhood Move

## 1. 개요

Local search는 현재 해를 조금씩 수정하면서 더 좋은 route 조합을 찾는 과정이다.

CVRPTW와 HGS/ALNS에서 local search는 다음 역할을 한다.

- 초기해 품질 개선
- ALNS repair 이후 candidate solution 개선
- HGS 자식 해를 강하게 개선
- route pool에 더 좋은 column 생성
- 차량 수 감소 시도와 route 재배치 지원

기본 예시는 다음 route를 사용한다.

```text
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0
```

## 2. Relocate

고객 하나를 현재 위치에서 제거한 뒤 같은 route 또는 다른 route의 다른 위치에 삽입한다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

C를 R2의 F 뒤에 삽입

After
R1 = 0 - A - B - D - 0
R2 = 0 - E - F - C - G - H - 0
```

바뀌는 edge:

```text
사라짐: B-C, C-D, F-G
생김:   B-D, F-C, C-G
```

비용 변화:

```text
Δ = [d(B,D) + d(F,C) + d(C,G)]
  - [d(B,C) + d(C,D) + d(F,G)]
```

사용 상황:

- 특정 고객이 다른 route에 더 자연스럽게 속할 때
- route 간 load balance 조정
- 고객 하나만 옮겨도 큰 거리 개선이 가능할 때

## 3. Swap

두 고객의 위치를 서로 교환한다. 같은 route 내부와 다른 route 사이 모두 가능하다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

B와 G 교환

After
R1 = 0 - A - G - C - D - 0
R2 = 0 - E - F - B - H - 0
```

Swap은 단순 relocate로는 capacity가 깨질 때 유용하다. 한 고객을 넣는 대신 다른 고객을 빼오므로 양쪽 route의 적재량 균형을 맞출 수 있다.

## 4. 2-opt

한 route 내부에서 두 edge를 끊고, 그 사이 구간을 뒤집는다.

```text
Before
R1 = 0 - A - B - C - D - E - 0

A-B와 D-E를 끊고 A-D, B-E로 연결

After
R1 = 0 - A - D - C - B - E - 0
```

대칭 거리에서는 바깥 edge만으로 변화량을 계산할 수 있다.

```text
Δ = [d(A,D) + d(B,E)] - [d(A,B) + d(D,E)]
```

주의:

- 비대칭 거리/시간에서는 뒤집힌 내부 edge 방향도 모두 바뀐다.
- time window가 있으면 순서 반전 후 schedule을 다시 확인해야 한다.

## 5. 2-opt*

두 route를 각각 한 지점에서 자른 뒤 뒤쪽 tail을 교환한다.

```text
Before
R1 = 0 - A - B | C - D - 0
R2 = 0 - E - F | G - H - 0

After
R1 = 0 - A - B - G - H - 0
R2 = 0 - E - F - C - D - 0
```

바뀌는 edge:

```text
사라짐: B-C, F-G
생김:   B-G, F-C
```

tail 전체가 교환되므로 capacity, route duration, time window, vehicle compatibility를 반드시 다시 확인해야 한다.

## 6. Or-opt

연속된 고객 묶음을 통째로 다른 위치로 이동한다. Relocate는 Or-opt에서 segment 길이가 1인 특수한 경우로 볼 수 있다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

B-C 묶음을 R2의 G 뒤로 이동

After
R1 = 0 - A - D - 0
R2 = 0 - E - F - G - B - C - H - 0
```

사용 상황:

- 가까운 고객들이 cluster처럼 묶여 있을 때
- 고객 하나씩 옮기는 것보다 묶음 단위 이동이 자연스러울 때
- route 간 지역 cluster를 이동하고 싶을 때

## 7. Cross-exchange

두 route에서 각각 연속 segment를 하나씩 골라 서로 교환한다.

```text
Before
R1 = 0 - A - B - C - D - E - 0
R2 = 0 - F - G - H - I - J - 0

B-C와 G-H 교환

After
R1 = 0 - A - G - H - D - E - 0
R2 = 0 - F - B - C - I - J - 0
```

Swap은 Cross-exchange에서 양쪽 segment 길이가 1인 특수한 경우다. 2-opt*는 두 route의 tail segment를 교환하는 특수한 Cross-exchange로 볼 수 있다.

## 8. SWAP*

SWAP*는 HGS-CVRP에서 중요한 inter-route move다.

일반 Swap은 두 고객을 서로의 기존 위치에 넣는다.

```text
일반 Swap:
R1의 C와 R2의 F를 고른다.
C는 F가 있던 위치로 간다.
F는 C가 있던 위치로 간다.
```

SWAP*는 두 고객이 서로 route를 바꾸되, 각자 상대 route 안의 더 좋은 삽입 위치에 들어갈 수 있다.

```text
SWAP*:
R1의 C와 R2의 F를 제거한다.
F를 R1의 좋은 위치에 삽입한다.
C를 R2의 좋은 위치에 삽입한다.
```

예:

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

Remove C and F
R1 = 0 - A - B - D - 0
R2 = 0 - E - G - H - 0

Possible SWAP* result
R1 = 0 - F - A - B - D - 0
R2 = 0 - E - G - C - H - 0
```

SWAP*가 강력한 이유는 “route 배정은 바꾸고 싶지만 서로의 정확한 기존 위치는 좋지 않은” 경우를 탐색할 수 있기 때문이다.

## 9. SWAP* 가속 아이디어

순진한 SWAP*는 경우의 수가 크다.

```text
고객 v 선택
고객 v' 선택
v를 상대 route의 어느 위치에 넣을지 선택
v'를 상대 route의 어느 위치에 넣을지 선택
```

가속 방법:

### Top-k insertion position

상대 route의 모든 위치를 보지 않고 좋은 삽입 위치 Top 3 정도만 본다.

```text
v를 route r'에 넣을 후보:
1. v'가 빠진 자리
2. v에게 좋은 삽입 위치 1등
3. v에게 좋은 삽입 위치 2등
4. v에게 좋은 삽입 위치 3등
```

### Route pair preprocessing

route pair별로 고객의 좋은 삽입 위치를 미리 계산한다.

```text
route r 안의 각 고객 v:
    v를 route r'에 넣을 좋은 위치 Top 3 계산

route r' 안의 각 고객 v':
    v'를 route r에 넣을 좋은 위치 Top 3 계산
```

### Polar sector filtering

depot 기준으로 완전히 다른 방향의 route는 교환 개선 가능성이 낮을 수 있다. route의 부채꼴 영역이 겹치는 route pair 중심으로 SWAP*를 평가한다.

## 10. Move 간 관계

| Move | 바꾸는 대상 | 같은 route 가능 | 다른 route 가능 | 핵심 특징 |
|---|---:|---:|---:|---|
| Relocate | 고객 1명 | 가능 | 가능 | 하나를 빼서 다른 위치에 삽입 |
| Swap | 고객 2명 | 가능 | 가능 | 두 고객 위치 직접 교환 |
| 2-opt | 한 route 구간 | 가능 | 아니오 | 내부 구간 반전 |
| 2-opt* | 두 route tail | 아니오 | 가능 | 뒤쪽 tail 교환 |
| Or-opt | 연속 고객 묶음 | 가능 | 가능 | segment 순서 유지 이동 |
| Cross-exchange | 두 route segment | 보통 route 간 | 가능 | segment 대 segment 교환 |
| SWAP* | route 간 고객 2명 | 아니오 | 가능 | 고객 교환 후 최적 삽입 위치 고려 |

## 11. Feasibility와 Penalty

move를 평가할 때는 거리 변화만 보면 안 된다.

```text
penalizedCost
= distance
+ capacityViolationPenalty
+ durationViolationPenalty
+ timeWindowViolationPenalty
+ zoneViolationPenalty
```

Hard constraint라면 move 후보를 즉시 reject한다.

```text
if capacity infeasible:
    reject
if time window infeasible:
    reject
if customer not allowed on vehicle:
    reject
```

Penalty 기반 탐색이라면 위반량을 비용에 포함해 비교한다.

## 12. 빠른 구현 전략

### Delta evaluation

route 전체 비용을 매번 다시 계산하지 않고, move로 바뀐 edge만 계산한다.

Relocate 예:

```text
변경 전:
... a → v → b ...
... c → d ...

변경 후:
... a → b ...
... c → v → d ...

Δ = [d(a,b) + d(c,v) + d(v,d)]
  - [d(a,v) + d(v,b) + d(c,d)]
```

### Granular search

모든 고객쌍을 다 검사하지 않고 가까운 고객 후보만 본다.

```text
neighbor_list[i] = i와 가까운 고객 Γ개
```

### 후보 제한

```text
1. 가까운 고객 neighborhood만 탐색
2. 개선 가능성이 높은 route pair만 탐색
3. first improvement 또는 best improvement 선택
4. move 적용 후 영향을 받은 route만 재평가
5. time window상 가능성이 낮은 위치는 사전 제외
```

## 13. 초기 구현 추천 순서

```text
1. intra-route 2-opt
2. intra-route relocate
3. inter-route relocate
4. swap
5. limited 2-opt*
6. Or-opt
7. Cross-exchange
8. SWAP*
```

SWAP*는 강력하지만 구현과 최적화 난이도가 높으므로 HGS 또는 성능 개선 단계에서 추가하는 것이 적절하다.

