# HGS Local Search Move 정리

## 1. 전체 개요

HGS(Hybrid Genetic Search)에서 local search는 현재 해(solution)를 조금씩 수정하면서 더 좋은 route 조합을 찾는 과정이다.  
VRP/CVRP 계열 문제에서는 route의 총 거리, 차량 capacity, duration, time window, penalty 등을 함께 고려하여 move 적용 여부를 판단한다.

이 문서에서는 다음 local search move들을 큰 주제별로 정리한다.

| Move | 핵심 아이디어 |
|---|---|
| Relocate | 고객 하나를 다른 위치로 이동 |
| Swap | 두 고객의 위치를 교환 |
| 2-opt | 한 route 내부의 일부 순서를 뒤집음 |
| 2-opt* | 두 route 사이의 tail을 교환 |
| Or-opt | 연속된 고객 묶음을 이동 |
| Cross-exchange | 두 route의 부분 경로를 교환 |
| SWAP* | 서로 다른 route의 고객을 더 유연한 위치로 교환 |

기본 예시는 다음과 같이 둔다.

```text
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

0 = depot
A~H = 고객
```

---

## 2. 고객 단위 이동 Move

### 2.1 Relocate

Relocate는 고객 하나를 현재 위치에서 제거한 뒤, 같은 route 또는 다른 route의 다른 위치에 삽입하는 move이다.

#### 예시

`C`를 R1에서 제거하고 R2의 `F` 뒤에 삽입한다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

After
R1 = 0 - A - B - D - 0
R2 = 0 - E - F - C - G - H - 0
```

#### 바뀌는 edge

```text
사라지는 edge:
B-C, C-D, F-G

생기는 edge:
B-D, F-C, C-G
```

#### 비용 변화

```text
Δ = [d(B,D) + d(F,C) + d(C,G)]
    - [d(B,C) + d(C,D) + d(F,G)]
```

`Δ < 0`이면 총 거리가 감소하므로 좋은 move이다.

#### 사용 상황

- 특정 고객이 현재 route보다 다른 route에 더 자연스럽게 속할 때
- route 간 load balance를 조정하고 싶을 때
- 고객 하나만 옮겨도 큰 거리 개선이 가능한 경우

---

### 2.2 Swap

Swap은 두 고객의 위치를 서로 교환하는 move이다.  
같은 route 내부에서도 가능하고, 서로 다른 route 사이에서도 가능하다.

#### 예시

R1의 `B`와 R2의 `G`를 교환한다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

After
R1 = 0 - A - G - C - D - 0
R2 = 0 - E - F - B - H - 0
```

#### 바뀌는 edge

```text
사라지는 edge:
A-B, B-C, F-G, G-H

생기는 edge:
A-G, G-C, F-B, B-H
```

#### 비용 변화

```text
Δ = [d(A,G) + d(G,C) + d(F,B) + d(B,H)]
    - [d(A,B) + d(B,C) + d(F,G) + d(G,H)]
```

#### Relocate와의 차이

```text
Relocate:
고객 하나만 이동한다.

Swap:
두 고객이 동시에 서로의 위치를 바꾼다.
```

#### 사용 상황

- 두 route의 고객 배정이 서로 뒤바뀐 듯한 경우
- 한 고객을 단순히 옮기면 capacity가 깨지지만, 다른 고객과 교환하면 feasible한 경우
- route 간 고객 구성을 동시에 조정하고 싶을 때

---

## 3. Route 내부 순서 개선 Move

### 3.1 2-opt

2-opt는 한 route 내부에서 두 edge를 끊고, 그 사이 구간을 뒤집는 move이다.  
TSP와 VRP에서 매우 자주 사용되는 대표적인 local search 연산자이다.

#### 예시

```text
Before
R1 = 0 - A - B - C - D - E - 0
```

`A-B`와 `D-E`를 끊고, `A-D`, `B-E`로 다시 연결한다.

```text
After
R1 = 0 - A - D - C - B - E - 0
```

즉, 중간 구간 `B-C-D`가 뒤집힌다.

#### 바뀌는 edge

```text
사라지는 edge:
A-B, D-E

생기는 edge:
A-D, B-E
```

#### 비용 변화

거리 행렬이 대칭인 경우에는 다음과 같이 계산할 수 있다.

```text
Δ = [d(A,D) + d(B,E)]
    - [d(A,B) + d(D,E)]
```

#### 주의점

거리 또는 시간이 비대칭인 경우에는 구간을 뒤집을 때 내부 edge의 방향도 모두 바뀐다.  
따라서 단순히 끊고 새로 연결되는 두 edge만 비교해서는 안 된다.

#### 사용 상황

- route 내부에 동선이 교차하는 경우
- 방문 순서가 꼬여 있어서 일부 구간을 뒤집으면 거리가 줄어드는 경우
- 한 route 안의 순서를 정리하고 싶을 때

---

## 4. Route 간 구조 변경 Move

### 4.1 2-opt*

2-opt*는 두 route를 각각 한 지점에서 자른 뒤, 뒤쪽 tail을 서로 교환하는 move이다.  
2-opt가 한 route 내부의 순서를 바꾸는 연산이라면, 2-opt*는 두 route 사이의 연결 구조를 바꾸는 연산이다.

#### 예시

R1은 `B` 뒤, R2는 `F` 뒤에서 자른다.

```text
Before
R1 = 0 - A - B | C - D - 0
R2 = 0 - E - F | G - H - 0
```

`|` 뒤쪽 tail을 서로 교환한다.

```text
After
R1 = 0 - A - B - G - H - 0
R2 = 0 - E - F - C - D - 0
```

#### 바뀌는 edge

```text
사라지는 edge:
B-C, F-G

생기는 edge:
B-G, F-C
```

#### 비용 변화

```text
Δ = [d(B,G) + d(F,C)]
    - [d(B,C) + d(F,G)]
```

#### 제약 확인

2-opt*는 tail 전체를 교환하므로 각 route의 총 demand가 크게 바뀔 수 있다.

```text
R1 새 load = R1 앞부분 load + R2 tail load
R2 새 load = R2 앞부분 load + R1 tail load
```

따라서 다음 제약을 반드시 확인해야 한다.

- 차량 capacity
- route duration
- time window
- 기타 문제별 제약

#### 사용 상황

- 두 route의 뒤쪽 구간이 서로 바뀌면 더 자연스러운 경우
- route 간 고객 묶음이 잘못 배정된 경우
- tail 단위의 큰 구조 변경이 필요한 경우

---

## 5. 연속 고객 묶음 기반 Move

### 5.1 Or-opt

Or-opt는 연속된 고객 묶음을 통째로 다른 위치로 이동하는 move이다.  
Relocate가 고객 1명을 옮기는 move라면, Or-opt는 길이 2 또는 3 정도의 segment를 옮기는 move로 볼 수 있다.

#### 예시

R1의 `B-C` 묶음을 R2의 `G` 뒤로 이동한다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

After
R1 = 0 - A - D - 0
R2 = 0 - E - F - G - B - C - H - 0
```

`B-C`의 내부 순서는 유지된다.

```text
B - C  그대로 이동
C - B  로 뒤집지 않음
```

#### 바뀌는 edge

```text
사라지는 edge:
A-B, C-D, G-H

생기는 edge:
A-D, G-B, C-H
```

#### 비용 변화

```text
Δ = [d(A,D) + d(G,B) + d(C,H)]
    - [d(A,B) + d(C,D) + d(G,H)]
```

#### Relocate와의 관계

```text
Relocate = Or-opt에서 segment 길이가 1인 특수한 경우
```

#### 사용 상황

- 가까운 고객들이 하나의 cluster처럼 묶여 있을 때
- 고객 하나씩 옮기는 것보다 묶음 단위 이동이 더 자연스러운 경우
- 같은 지역의 고객들이 다른 route로 함께 이동해야 하는 경우

---

### 5.2 Cross-exchange

Cross-exchange는 두 route에서 각각 연속된 segment를 하나씩 골라 서로 교환하는 move이다.

#### 예시

R1의 `B-C`와 R2의 `G-H`를 교환한다.

```text
Before
R1 = 0 - A - B - C - D - E - 0
R2 = 0 - F - G - H - I - J - 0

After
R1 = 0 - A - G - H - D - E - 0
R2 = 0 - F - B - C - I - J - 0
```

#### 바뀌는 edge

```text
사라지는 edge:
A-B, C-D, F-G, H-I

생기는 edge:
A-G, H-D, F-B, C-I
```

#### 비용 변화

```text
Δ = [d(A,G) + d(H,D) + d(F,B) + d(C,I)]
    - [d(A,B) + d(C,D) + d(F,G) + d(H,I)]
```

#### Swap과의 관계

```text
Swap = Cross-exchange에서 양쪽 segment 길이가 1인 특수한 경우
```

#### Or-opt와의 차이

```text
Or-opt:
한 segment를 다른 위치로 이동한다.

Cross-exchange:
두 route에서 segment를 하나씩 뽑아 서로 교환한다.
```

#### 사용 상황

- 두 route에 잘못 배정된 작은 고객 묶음이 있을 때
- 고객 하나만 바꿔서는 개선이 작고, segment 단위 교환이 필요한 경우
- route 간 지역 cluster를 교환하고 싶을 때

---

## 6. HGS 특화 Inter-route Move

### 6.1 SWAP*

SWAP*는 HGS-CVRP에서 중요한 inter-route move 중 하나이다.  
일반 Swap은 두 고객이 서로의 기존 위치를 그대로 교환하지만, SWAP*는 서로 다른 route의 두 고객을 교환하되 각 고객을 상대 route 안의 더 좋은 위치에 삽입할 수 있다.

즉, SWAP*는 다음과 같이 이해할 수 있다.

```text
일반 Swap:
R1의 C와 R2의 F를 고른다.
C는 F가 있던 위치로 간다.
F는 C가 있던 위치로 간다.

SWAP*:
R1의 C와 R2의 F를 고른다.
C는 R2로 가고, F는 R1로 간다.
하지만 꼭 서로의 기존 위치에 들어가지 않아도 된다.
각 route 안에서 더 좋은 삽입 위치를 고려한다.
```

#### 일반 Swap 예시

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

C와 F를 일반 Swap

After
R1 = 0 - A - B - F - D - 0
R2 = 0 - E - C - G - H - 0
```

여기서 `C`는 `F`가 있던 자리에 들어가고, `F`는 `C`가 있던 자리에 들어간다.

#### SWAP* 예시

먼저 `C`와 `F`를 각각의 route에서 제거한다.

```text
Before
R1 = 0 - A - B - C - D - 0
R2 = 0 - E - F - G - H - 0

Remove C and F

R1 = 0 - A - B - D - 0
R2 = 0 - E - G - H - 0
```

그다음 `F`를 R1의 좋은 위치에, `C`를 R2의 좋은 위치에 삽입한다.

```text
Possible SWAP* result

R1 = 0 - F - A - B - D - 0
R2 = 0 - E - G - C - H - 0
```

여기서 `F`는 원래 `C`가 있던 `B-D` 사이에 들어가지 않았고, `C`도 원래 `F`가 있던 `E-G` 사이가 아니라 `G-H` 사이에 들어갔다.  
이 점이 일반 Swap과 SWAP*의 핵심 차이이다.

#### 왜 강력한가?

일반 Swap은 “서로의 자리”라는 제약이 있다.  
하지만 실제로는 고객을 상대 route로 보내는 것은 좋지만, 상대 고객이 있던 정확한 위치에 넣는 것은 좋지 않은 경우가 많다.

SWAP*는 route 간 고객 교환을 하면서도 삽입 위치를 더 유연하게 고려하므로 일반 Swap보다 더 넓은 neighborhood를 탐색할 수 있다.

#### 사용 상황

- 고객 두 명의 route 배정을 바꾸는 것이 좋지만, 정확히 서로의 위치에 넣는 것은 비효율적인 경우
- route 간 고객 교환과 insertion 최적화를 동시에 하고 싶은 경우
- HGS에서 inter-route 개선 성능을 높이고 싶은 경우

---

## 7. Move 간 관계 정리

### 7.1 포함 관계

```text
Relocate ≈ Or-opt에서 segment 길이가 1인 경우

Swap ≈ Cross-exchange에서 양쪽 segment 길이가 1인 경우

2-opt* ≈ 두 route의 tail segment를 교환하는 특수한 Cross-exchange

SWAP* ≈ route 간 Swap을 더 강하게 만든 HGS 특화 move
```

### 7.2 비교표

| Move | 바꾸는 대상 | 같은 route 가능? | 다른 route 가능? | 핵심 특징 |
|---|---:|---:|---:|---|
| Relocate | 고객 1명 | 가능 | 가능 | 하나를 빼서 다른 위치에 삽입 |
| Swap | 고객 2명 | 가능 | 가능 | 두 고객의 위치를 직접 교환 |
| 2-opt | 한 route의 구간 | 가능 | 아니오 | 내부 구간을 뒤집어 꼬임 제거 |
| 2-opt* | 두 route의 tail | 아니오 | 가능 | 두 route를 자르고 뒤쪽 tail 교환 |
| Or-opt | 연속 고객 묶음 | 가능 | 가능 | segment를 순서 유지한 채 이동 |
| Cross-exchange | 두 route의 segment 2개 | 보통 route 간 | 가능 | segment 대 segment 교환 |
| SWAP* | route 간 고객 2명 | 아니오 | 가능 | 고객 교환 후 더 좋은 삽입 위치 고려 |

---

## 8. 실무 구현 관점

### 8.1 비용 계산은 바뀐 edge만 본다

Local search를 빠르게 구현하려면 route 전체 비용을 매번 다시 계산하지 않고, move로 인해 바뀐 edge만 계산하는 것이 중요하다.

예를 들어 Relocate에서는 전체 route를 다시 계산하지 않고 다음 edge 변화만 본다.

```text
사라지는 edge:
B-C, C-D, F-G

생기는 edge:
B-D, F-C, C-G
```

이렇게 하면 move 하나의 비용 변화량을 빠르게 계산할 수 있다.

---

### 8.2 feasibility와 penalty를 함께 본다

HGS 계열 알고리즘은 feasible solution만 탐색하지 않고, 경우에 따라 infeasible solution도 penalty를 붙여 일시적으로 허용할 수 있다.

따라서 local search에서 move를 평가할 때는 단순 거리뿐 아니라 다음 항목도 함께 고려한다.

```text
1. 총 거리 변화
2. 차량 capacity 초과 여부
3. route duration 초과 여부
4. time window 위반 여부
5. penalty가 포함된 objective 변화
```

일반적인 평가 형태는 다음과 같다.

```text
penalizedCost = distance
              + capacityViolationPenalty
              + durationViolationPenalty
              + timeWindowViolationPenalty
```

---

### 8.3 move 탐색 순서

실제 구현에서는 모든 move를 무작정 탐색하면 시간이 많이 걸린다.  
따라서 다음과 같은 전략을 함께 사용한다.

```text
1. 가까운 고객 neighborhood만 탐색
2. 개선 가능성이 높은 route 쌍만 탐색
3. first improvement 또는 best improvement 선택
4. move 적용 후 영향을 받은 route만 다시 평가
5. granular neighborhood를 사용하여 후보 수 제한
```

---

## 9. 직관적인 비유

배송 route가 다음과 같다고 가정한다.

```text
차량 1: 강남 → 서초 → 잠실 → 송파
차량 2: 마포 → 홍대 → 여의도 → 영등포
```

각 move는 다음과 같이 이해할 수 있다.

```text
Relocate:
잠실 하나를 차량 2 route로 옮긴다.

Swap:
차량 1의 잠실과 차량 2의 여의도를 서로 바꾼다.

2-opt:
차량 1 안에서 서초-잠실-송파 순서가 꼬였으니 일부 순서를 뒤집는다.

2-opt*:
차량 1의 뒤쪽 잠실-송파와 차량 2의 뒤쪽 여의도-영등포를 교환한다.

Or-opt:
잠실-송파 묶음을 통째로 차량 2 route의 어떤 위치로 옮긴다.

Cross-exchange:
차량 1의 서초-잠실 묶음과 차량 2의 홍대-여의도 묶음을 교환한다.

SWAP*:
차량 1의 잠실과 차량 2의 여의도를 route 간 교환하되,
각 고객이 상대 route 안에서 가장 자연스러운 위치에 들어가도록 한다.
```

---

## 10. 핵심 요약

Local search move들은 크게 다음과 같이 나눌 수 있다.

```text
고객 단위 이동:
- Relocate
- Swap
- SWAP*

route 내부 순서 개선:
- 2-opt

route 간 tail 또는 segment 교환:
- 2-opt*
- Cross-exchange

연속 고객 묶음 이동:
- Or-opt
```

가장 중요한 차이는 다음과 같다.

```text
Relocate:
하나를 옮긴다.

Swap:
두 고객을 서로 바꾼다.

2-opt:
한 route 안에서 순서를 뒤집는다.

2-opt*:
두 route의 뒤쪽 구간을 바꾼다.

Or-opt:
연속된 고객 묶음을 옮긴다.

Cross-exchange:
두 route의 연속 구간을 서로 바꾼다.

SWAP*:
route 간 고객을 교환하되, 더 좋은 삽입 위치까지 고려한다.
```

HGS에서 local search의 핵심은 단순히 다양한 move를 많이 넣는 것이 아니라, 각 move의 비용 변화와 제약 위반 변화를 빠르게 계산하고, 좋은 후보 neighborhood만 효율적으로 탐색하는 것이다.

---

## 11. 참고 링크

- HGS-CVRP GitHub: https://github.com/vidalt/HGS-CVRP
- HGS-CVRP 논문: https://arxiv.org/abs/2012.10384
