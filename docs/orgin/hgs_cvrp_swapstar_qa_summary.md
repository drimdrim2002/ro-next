# HGS-CVRP 논문 및 질의응답 정리

> 대상 논문: **Thibaut Vidal, “Hybrid genetic search for the CVRP: Open-source implementation and SWAP\* neighborhood”**  
> 정리 범위: 논문 개요, HGS-CVRP 알고리즘 구조, 로컬 서치 구성, SWAP\* 이웃구조, 차량 대수가 빡빡한 상황에서의 동작 방식  
> 작성 목적: 현재까지의 질의응답 내용을 큰 주제별로 재구성한 학습용 Markdown 문서

---

## 0. 전체 핵심 요약

HGS-CVRP는 **Capacitated Vehicle Routing Problem, CVRP**를 풀기 위한 하이브리드 메타휴리스틱이다.

한 문장으로 요약하면 다음과 같다.

> **HGS-CVRP = 여러 배송계획 후보를 진화시키는 유전 알고리즘 + 각 후보를 강하게 다듬는 로컬 서치**

핵심 구조는 다음과 같다.

```text
HGS-CVRP
= Genetic Search
+ Split
+ Local Search
+ Feasible/Infeasible population management
+ Diversity control
+ SWAP* neighborhood
```

가장 중요한 메시지는 다음이다.

| 핵심 요소 | 역할 |
|---|---|
| Genetic Search | 다양한 후보해를 만들고 섞어 넓은 탐색 수행 |
| Split | 고객 순열을 차량 route들로 변환 |
| Local Search | route를 세부적으로 개선 |
| Infeasible population | 용량을 조금 넘는 해도 버리지 않고 탐색 |
| Penalty control | infeasible 해가 feasible 쪽으로 이동하도록 유도 |
| SWAP\* | route 간 고객 교환을 더 유연하고 강하게 수행 |

---

## 1. CVRP 기본 개념

### 1.1 CVRP란?

CVRP는 **차량 용량 제약이 있는 차량경로문제**이다.

```text
창고 depot에서 여러 대의 차량이 출발한다.
각 차량은 고객들을 방문한다.
모든 고객은 정확히 한 번 방문되어야 한다.
각 차량의 적재량은 차량 용량을 넘으면 안 된다.
전체 이동거리 또는 비용을 최소화한다.
```

예를 들어 고객 A~H가 있고 depot을 0이라고 하면 하나의 해는 다음과 같다.

```text
Route 1: 0 → A → B → C → 0
Route 2: 0 → D → E → 0
Route 3: 0 → F → G → H → 0
```

CVRP의 핵심 질문은 다음과 같다.

```text
고객들을 어떤 순서로 방문할 것인가?
고객들을 어떤 차량 route로 나눌 것인가?
각 차량의 용량을 어떻게 지킬 것인가?
```

### 1.2 CVRP 제약 요약

| 제약 | 의미 |
|---|---|
| 모든 고객 방문 | 각 고객은 정확히 한 번 방문 |
| 차량 용량 | 각 route의 총 수요량은 차량 용량 이하 |
| depot 출발·복귀 | 각 route는 depot에서 시작하고 depot으로 복귀 |
| 비용 최소화 | 총 거리, 시간 또는 비용 최소화 |

---

## 2. 논문의 핵심 기여

이 논문의 핵심 기여는 크게 네 가지이다.

| 기여 | 설명 |
|---|---|
| 공개 구현 | CVRP 전용 HGS 알고리즘의 C++ 구현 공개 |
| 간결한 HGS 구조 | 기존 HGS 계열 알고리즘을 CVRP에 맞게 정리 |
| SWAP\* 이웃구조 | route 간 고객 교환을 더 유연하게 수행 |
| 강한 실험 성능 | CVRPLIB benchmark에서 매우 우수한 성능 보고 |

특히 SWAP\*는 일반 Swap보다 강력한 route 간 교환 연산이다. 일반 Swap은 두 고객을 서로의 기존 위치에 넣지만, SWAP\*는 두 고객이 서로 route를 바꾼 뒤 각자 상대 route 안의 가장 좋은 위치에 들어간다.

---

## 3. HGS-CVRP 전체 알고리즘 구조

### 3.1 전체 흐름

```text
┌────────────────────┐
│  초기 해 여러 개 생성 │
└─────────┬──────────┘
          ↓
┌────────────────────────────────────┐
│            Population              │
│                                    │
│  ┌──────────────┐  ┌──────────────┐ │
│  │ Feasible 해  │  │ Infeasible 해 │ │
│  │ 용량 OK      │  │ 용량 초과 가능 │ │
│  └──────────────┘  └──────────────┘ │
└─────────┬──────────────────────────┘
          ↓
      반복 시작
          ↓
┌────────────────────┐
│ 1. 부모 2개 선택     │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ 2. OX Crossover    │
│    + Split         │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ 3. Local Search    │
│    + 필요 시 Repair │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ 4. Population 삽입  │
│    다양성·벌점 관리  │
└─────────┬──────────┘
          ↓
    종료 조건 확인
          ↓
┌────────────────────┐
│ Best feasible 해 반환│
└────────────────────┘
```

### 3.2 학부생용 의사코드

```text
1. 랜덤 배송계획을 여러 개 만든다.
2. 각 배송계획을 local search로 다듬는다.
3. feasible population과 infeasible population에 나눠 넣는다.

4. while 종료 조건이 아니면:
      a. 부모 해 P1, P2를 고른다.
      b. P1, P2를 섞어서 자식 순서 C를 만든다.  ← OX crossover
      c. Split으로 C를 실제 차량 route들로 자른다.
      d. Local search로 C를 개선한다.
      e. C가 용량 초과라면 일정 확률로 repair를 시도한다.
      f. C를 알맞은 population에 넣는다.
      g. population이 너무 커지면 나쁜 해나 중복 해를 제거한다.
      h. infeasible 벌점 계수를 조정한다.

5. 가장 좋은 feasible solution을 반환한다.
```

### 3.3 구성요소별 역할

| 구성요소 | 쉬운 비유 | 실제 역할 |
|---|---|---|
| Population | 여러 배송계획 후보 모음 | 다양한 가능성 유지 |
| Parent selection | 좋은 후보끼리 부모로 뽑기 | 좋은 구조를 다음 해로 전달 |
| Crossover | 부모 계획 섞기 | 새로운 고객 방문 순서 생성 |
| Split | 긴 방문 순서를 route로 자르기 | CVRP 해로 복원 |
| Local Search | 손으로 세부 경로 고치기 | 거리 단축, 용량 개선 |
| Repair | 용량 초과 해 고치기 | infeasible 해를 feasible 쪽으로 유도 |
| Population management | 너무 비슷하거나 나쁜 해 제거 | 품질과 다양성 유지 |

---

## 4. 해의 표현: Route와 Giant Tour

### 4.1 Route 표현

CVRP 해는 원래 차량별 route 형태이다.

```text
Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 6 → 0
Route 3: 0 → 7 → 8 → 10 → 4 → 0
```

### 4.2 Giant tour 표현

Crossover를 쉽게 하기 위해 depot 0을 빼고 고객 방문 순서만 긴 배열로 표현한다.

```text
Giant tour:
2  1  3  9  5  6  7  8  10  4
```

이 긴 순서를 어디서 끊느냐에 따라 차량 route가 된다.

```text
Giant tour:
2  1  3 | 9  5  6 | 7  8  10  4

Split 후:
Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 6 → 0
Route 3: 0 → 7 → 8 → 10 → 4 → 0
```

### 4.3 왜 Giant tour를 쓰는가?

| 이유 | 설명 |
|---|---|
| Crossover가 쉬움 | 순열을 섞는 방식으로 자식 생성 가능 |
| 모든 고객 포함 보장 | 고객 중복·누락 관리가 쉬움 |
| Split과 결합 가능 | 순서를 route들로 효율적으로 변환 가능 |

---

## 5. 초기 Population과 Feasible/Infeasible 관리

### 5.1 초기 해 생성

초기에는 랜덤한 배송계획을 여러 개 만든다. 단, 만든 해를 바로 population에 넣지 않고 local search로 한 번 개선한다.

```text
랜덤 해 1 → Local Search → 개선된 해 1
랜덤 해 2 → Local Search → 개선된 해 2
랜덤 해 3 → Local Search → 개선된 해 3
...
```

### 5.2 Feasible/Infeasible subpopulation

HGS-CVRP는 population을 두 그룹으로 나눈다.

```text
Population
│
├── Feasible subpopulation
│   └── 차량 용량을 지키는 해
│
└── Infeasible subpopulation
    └── 차량 용량을 초과할 수 있는 해
```

### 5.3 왜 infeasible 해를 버리지 않는가?

좋은 해 근처에 있지만 현재는 용량을 조금 넘는 해가 있을 수 있다. 이런 해를 바로 버리면 좋은 feasible 해로 이동할 가능성을 잃을 수 있다.

```text
일반적인 생각:
용량 초과 → 버림

HGS-CVRP의 생각:
용량 초과 → 벌점을 주고 보관
           → local search와 repair로 고쳐볼 수 있음
```

### 5.4 Penalized cost

infeasible 해는 단순 이동거리만으로 평가하지 않는다. 초과량에 벌점을 붙인다.

```text
penalized cost
= 총 이동거리
+ 용량초과량 × capacity penalty
+ 운행시간초과량 × duration penalty
```

기본 CVRP에서는 주로 용량초과량이 중요하다.

---

## 6. 부모 선택과 OX Crossover

### 6.1 부모 선택: Binary Tournament

부모를 고를 때는 이진 토너먼트 선택을 사용한다.

```text
1. population에서 해 2개를 랜덤으로 뽑는다.
2. 둘 중 fitness가 좋은 해를 선택한다.
3. 같은 과정을 한 번 더 해서 부모 2개를 얻는다.
```

여기서 fitness는 단순히 거리만 보는 것이 아니다.

```text
fitness = 해의 품질 + 해의 다양성 기여
```

즉, 좋은 해이면서도 다른 해들과 충분히 다른 해가 유리하다.

### 6.2 OX Crossover

HGS-CVRP는 Ordered Crossover, OX를 사용한다. OX는 한 부모의 일부 구간을 그대로 가져오고, 나머지는 다른 부모의 순서를 따라 채우는 방식이다.

예시:

```text
부모 1:
1  2  3  4 [5  6  7  8] 9  10

부모 2:
5  4  6  2  1  3  8  9  7  10
```

부모 1에서 `[5 6 7 8]` 구간을 자식에게 그대로 물려준다.

```text
자식:
_  _  _  _ [5  6  7  8] _  _
```

부모 2의 순서에서 이미 들어간 5, 6, 7, 8을 제거한다.

```text
부모 2에서 5,6,7,8 제거:
4  2  1  3  9  10
```

나머지 자리를 채우면 다음과 같은 자식 giant tour가 만들어질 수 있다.

```text
자식 giant tour:
2  1  3  9 [5  6  7  8] 10  4
```

이 단계에서는 아직 차량별 route가 아니다. 단지 고객 순서일 뿐이다.

---

## 7. Split: 고객 순서를 차량 route로 자르기

### 7.1 Split의 역할

OX 결과는 depot이 없는 긴 고객 순서이다.

```text
2  1  3  9  5  6  7  8  10  4
```

Split은 이 순서를 적절히 잘라 route로 만든다.

```text
2  1  3 | 9  5 | 6  7 | 8  10  4
```

실제 route는 다음과 같다.

```text
Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 0
Route 3: 0 → 6 → 7 → 0
Route 4: 0 → 8 → 10 → 4 → 0
```

### 7.2 Split을 쉽게 이해하기

```text
Giant tour:
[ 2  1  3  9  5  6  7  8  10  4 ]

Split이 하는 일:
[ 2  1  3 ] [ 9  5 ] [ 6  7 ] [ 8  10  4 ]

각 묶음 앞뒤에 depot 0 추가:
0-2-1-3-0
0-9-5-0
0-6-7-0
0-8-10-4-0
```

Split은 아무 곳이나 자르는 것이 아니라, 주어진 고객 순서를 유지하면서 좋은 route 구분점을 찾는 과정이다.

---

## 8. Local Search 전체 개념

### 8.1 Local search란?

현재 해를 조금씩 바꿔보며 더 좋은 해를 찾는 과정이다.

```text
현재 route:
0 → A → B → C → D → 0

조금 바꿔보기:
0 → A → C → B → D → 0

거리가 줄어들면?
→ 바꾼 해를 채택
```

### 8.2 Local search의 기본 루프

```text
현재 해 S
  ↓
작은 변경 move 하나를 시도
  ↓
비용이 줄어드는가?
  ├─ Yes → 즉시 적용, S 갱신
  │          ↓
  │       다시 move 탐색
  │
  └─ No  → 다른 move 시도
             ↓
더 이상 개선 move가 없음
  ↓
Local minimum 도달
```

여기서 local minimum은 “현재 사용하는 move들로는 더 이상 좋아지지 않는 상태”이다. 전역 최적이라는 뜻은 아니다.

### 8.3 HGS-CVRP의 주요 local search move

| Move | 핵심 아이디어 | 같은 route 안에서 가능? | 다른 route 사이에서 가능? |
|---|---|---:|---:|
| Relocate | 고객 하나 또는 연속 고객 묶음을 다른 위치로 옮김 | 가능 | 가능 |
| Swap | 고객 둘 또는 고객 묶음 둘을 서로 교환 | 가능 | 가능 |
| 2-Opt | 한 route 안에서 꼬인 구간을 뒤집음 | 가능 | 보통 같은 route |
| 2-Opt\* | 두 route의 뒷부분을 교환 | 아니오 | 가능 |
| SWAP\* | 서로 다른 route의 고객 둘을 교환하되, 각자 최적 삽입 위치를 찾음 | 아니오 | 가능 |

---

## 9. Local Search Move별 설명

### 9.1 Relocate

Relocate는 고객 하나를 다른 위치로 옮기는 move이다.

```text
변경 전:
0 → A → [B] → C → D → 0

B를 D 뒤로 이동

변경 후:
0 → A → C → D → [B] → 0
```

다른 route로 옮길 수도 있다.

```text
변경 전:
Route 1: 0 → A → [B] → C → 0
Route 2: 0 → D → E → 0

변경 후:
Route 1: 0 → A → C → 0
Route 2: 0 → D → [B] → E → 0
```

주의할 점은 다른 route로 옮겼을 때 차량 용량을 초과할 수 있다는 것이다.

### 9.2 Swap

Swap은 고객 둘의 자리를 바꾸는 move이다.

```text
변경 전:
Route 1: 0 → A → [B] → C → 0
Route 2: 0 → D → [E] → F → 0

B와 E를 교환

변경 후:
Route 1: 0 → A → [E] → C → 0
Route 2: 0 → D → [B] → F → 0
```

Swap은 Relocate보다 용량 제약을 맞추기 쉬운 경우가 있다. 한 고객을 다른 route로 그냥 넣으면 용량 초과가 되지만, 다른 고객을 동시에 빼오면 양쪽 용량이 맞을 수 있기 때문이다.

### 9.3 2-Opt

2-Opt는 route 안에서 간선 두 개를 끊고 중간 구간을 뒤집는 move이다.

```text
변경 전:
0 → A → B → C → D → 0

변경 후:
0 → A → C → B → D → 0
```

간선 관점에서는 다음과 같다.

```text
변경 전 간선:
A → B
C → D

변경 후 간선:
A → C
B → D
```

주로 route가 교차하거나 비효율적인 순서를 가질 때 효과적이다.

### 9.4 2-Opt\*

2-Opt\*는 두 route 사이에서 뒷부분을 교환하는 move이다.

```text
변경 전:
Route 1: 0 → A → B | C → D → 0
Route 2: 0 → E → F | G → H → 0

| 뒤쪽 꼬리를 서로 교환

변경 후:
Route 1: 0 → A → B | G → H → 0
Route 2: 0 → E → F | C → D → 0
```

이 move는 route 두 개의 구조를 크게 바꿀 수 있다.

---

## 10. SWAP\*: 논문의 핵심 이웃구조

### 10.1 일반 Swap과 SWAP\*의 차이

일반 Swap은 서로 다른 route의 두 고객을 서로의 원래 위치에 넣는다.

```text
일반 Swap

변경 전:
Route r : 0 → A → [v]  → B → C → 0
Route r': 0 → D → E → [v'] → F → 0

변경 후:
Route r : 0 → A → [v'] → B → C → 0
Route r': 0 → D → E → [v]  → F → 0
```

SWAP\*는 다르다.

```text
SWAP*

v와 v'를 서로 다른 route에서 교환한다.
단, v를 v'의 원래 위치에 꼭 넣지 않는다.
v는 상대 route 안에서 가장 좋은 위치에 삽입된다.
v'도 마찬가지로 상대 route 안에서 가장 좋은 위치에 삽입된다.
```

예시:

```text
변경 전:
Route r : 0 → A → [v] → B → C → 0
Route r': 0 → D → E → [v'] → F → G → 0
```

일반 Swap:

```text
Route r : 0 → A → [v'] → B → C → 0
Route r': 0 → D → E → [v]  → F → G → 0
```

SWAP\*:

```text
Route r : 0 → A → B → [v'] → C → 0
Route r': 0 → D → [v] → E → F → G → 0
```

### 10.2 왜 SWAP\*가 강력한가?

일반 Swap은 다음처럼 작동한다.

```text
너는 저 사람 자리로 가고,
저 사람은 네 자리로 와.
```

SWAP\*는 다음처럼 작동한다.

```text
너희 둘은 서로 route를 바꾸되,
각 route 안에서 가장 자연스러운 위치를 찾아 들어가.
```

따라서 다음 상황에서 특히 유용하다.

```text
Relocate 하나만 하면 용량 초과
일반 Swap은 거리 개선이 작음
하지만 두 고객을 교환하면서 각각 최적 위치에 넣으면 큰 개선 가능
```

### 10.3 SWAP\*의 계산량 문제와 가속 아이디어

SWAP\*는 강력하지만, 순진하게 구현하면 경우의 수가 많다.

```text
고객 v 선택
고객 v' 선택
v를 상대 route의 어느 위치에 넣을지 선택
v'를 상대 route의 어느 위치에 넣을지 선택
```

논문은 이를 줄이기 위해 다음 아이디어를 사용한다.

#### 아이디어 1. 좋은 삽입 위치 Top 3만 본다

```text
v를 route r'에 넣을 후보 위치:

1. v'가 빠진 자리
2. v에게 좋은 삽입 위치 1등
3. v에게 좋은 삽입 위치 2등
4. v에게 좋은 삽입 위치 3등
```

#### 아이디어 2. Route pair 단위로 전처리한다

```text
Route r  안의 각 고객 v에 대해:
    v를 route r'에 넣을 좋은 위치 Top 3 계산

Route r' 안의 각 고객 v'에 대해:
    v'를 route r에 넣을 좋은 위치 Top 3 계산

그다음:
    v와 v'의 모든 쌍을 보면서
    가장 좋은 SWAP* 조합을 찾음
```

#### 아이디어 3. Polar sector로 route pair를 줄인다

depot 기준으로 지리적으로 완전히 다른 방향에 있는 route끼리는 교환해도 개선 가능성이 낮다.

```text
                북쪽 route
                   /\
                  /  \
                 /    \

Depot 0 ---------------------- 동쪽

                 \    /
                  \  /
                   \/
                남쪽 route
```

따라서 depot에서 본 route의 부채꼴 영역이 겹치는 route pair 중심으로 SWAP\*를 평가해 계산량을 줄인다.

---

## 11. Local Search를 빠르게 만드는 기법

### 11.1 전체 비용을 매번 다시 계산하지 않는다

Relocate 하나를 평가할 때 전체 route 비용을 처음부터 다시 계산하면 느리다. 대신 바뀐 간선만 계산한다.

예를 들어 고객 v를 옮긴다고 하자.

```text
변경 전:
... a → v → b ...
... c → d ...

변경 후:
... a → b ...
... c → v → d ...
```

사라지는 간선:

```text
a → v
v → b
c → d
```

새로 생기는 간선:

```text
a → b
c → v
v → d
```

비용 변화량은 다음처럼 계산한다.

```text
Δ = 새 간선 비용 합 - 기존 간선 비용 합

Δ < 0 이면 거리 감소 → move 적용
Δ ≥ 0 이면 개선 없음 → move 버림
```

### 11.2 Granular Search

모든 고객쌍을 다 검사하면 너무 느리다. 고객 수가 1,000명이라면 가능한 고객쌍만 약 1,000,000개가 된다.

따라서 각 고객에 대해 가까운 고객 몇 명만 후보로 본다.

```text
고객 i의 가까운 후보:

i ─ 가까움 ─ j1
i ─ 가까움 ─ j2
i ─ 가까움 ─ j3
...
i ─ 가까움 ─ jΓ

너무 먼 고객은 일단 무시
```

이 방식은 계산량을 줄이면서도 실제 개선 가능성이 높은 move를 우선 탐색하게 해준다.

---

## 12. Repair와 Penalty 조정

### 12.1 Repair의 목적

HGS-CVRP는 infeasible solution도 보관하지만, 최종 답은 feasible해야 한다. 따라서 local search 후에도 해가 infeasible이면 repair를 시도할 수 있다.

```text
Local search 후 해 C가 infeasible
        ↓
일정 확률로 Repair 실행
        ↓
벌점 계수를 크게 높여 local search 재실행
        ↓
용량 초과를 줄이는 방향으로 유도
```

### 12.2 Repair의 직관

일반 local search는 거리 단축과 용량 개선 사이에서 균형을 본다. Repair에서는 capacity penalty를 크게 높여서 알고리즘이 거리보다 용량 초과 해소를 더 중요하게 보도록 만든다.

```text
일반 탐색:
거리 감소도 중요, 용량 초과도 중요

Repair:
용량 초과 해소를 훨씬 더 중요하게 봄
```

### 12.3 Penalty 조정의 의미

infeasible 해가 너무 많으면 penalty를 키워 feasible 쪽으로 밀고, feasible 해가 너무 많으면 penalty를 완화해 탐색 폭을 넓힐 수 있다.

```text
infeasible 해가 너무 많음
→ penalty 증가
→ 용량 초과를 더 강하게 억제

feasible 해가 너무 많음
→ penalty 완화
→ 경계 근처 infeasible 해도 탐색 가능
```

---

## 13. Population Management와 Diversity Control

### 13.1 왜 다양성이 필요한가?

좋은 해만 남기면 population이 비슷한 해로 가득 차게 된다. 그러면 crossover를 해도 새로운 구조가 잘 나오지 않는다.

```text
좋은 해만 남김
→ 모두 비슷해짐
→ 새로운 조합이 줄어듦
→ 조기 수렴 가능
```

따라서 HGS-CVRP는 해의 품질뿐 아니라 다양성도 고려한다.

```text
좋은 해 + 다른 해들과 충분히 다른 해
→ population에 남길 가치가 큼
```

### 13.2 Clone 제거

Population이 너무 커지면 먼저 거의 같은 해, 즉 clone을 제거한다.

```text
Population이 너무 커짐
        ↓
1. clone이 있으면 clone 제거
        ↓
2. clone이 없으면 fitness가 나쁜 해 제거
```

### 13.3 Diversity의 의미

HGS-CVRP에서는 route 구조가 얼마나 다른지를 본다. 예를 들어 고객 A와 B가 같은 route에서 인접한 쌍으로 등장하는지 등을 비교하여 해 간 차이를 측정한다.

---

## 14. 차량 대수가 빡빡한 경우의 동작 방식

### 14.1 중요한 전제

HGS-CVRP 원 논문 로직에서는 **미배정 주문**이라는 개념이 기본적으로 없다.

CVRP는 모든 고객을 정확히 한 번 방문해야 하는 문제다. 따라서 차량 수가 빡빡하더라도 알고리즘은 주문 일부를 빼는 것이 아니라, 모든 주문을 일단 배정하고 용량 초과가 발생하면 infeasible solution으로 취급한다.

```text
주문 일부 미배정
→ 원 CVRP의 기본 선택지가 아님

모든 주문 배정 + 일부 route 용량 초과
→ HGS-CVRP의 infeasible solution
```

### 14.2 질문 상황의 세 가지 해석

| 상황 | 의미 | HGS-CVRP에서의 처리 |
|---|---|---|
| A. 물리적으로 불가능 | 차량 수와 용량으로 전체 주문을 도저히 처리 불가 | feasible solution이 존재하지 않음 |
| B. 초기 생성 방법이 부족함 | 실제로는 가능한데 초기해가 못 찾음 | infeasible 해를 허용하며 탐색 계속 |
| C. 일부 주문 포기가 허용됨 | 오늘 못 싣는 주문은 미배정/이월 가능 | 원 CVRP가 아니므로 모델 수정 필요 |

### 14.3 초기해가 infeasible인 예

차량 3대, 각 차량 용량 10이라고 하자.

```text
차량 수 m = 3
차량 용량 Q = 10

주문 수요:
A=6, B=4, C=6, D=4, E=5, F=5
```

좋은 배정은 다음과 같다.

```text
Route 1: A(6) + B(4) = 10
Route 2: C(6) + D(4) = 10
Route 3: E(5) + F(5) = 10
```

하지만 초기 랜덤 순서가 나쁘면 다음처럼 될 수 있다.

```text
Route 1: A(6) + C(6) = 12   ← 용량 2 초과
Route 2: B(4) + E(5) = 9
Route 3: D(4) + F(5) = 9
```

HGS-CVRP는 이 해를 버리지 않는다.

```text
모든 주문은 배정됨
Route 1만 용량을 2 초과함
→ infeasible solution으로 저장
→ penalty 부여
→ local search와 repair로 개선 시도
```

### 14.4 Local search로 고쳐지는 예

```text
초기 infeasible 해:
Route 1: A + C = 12
Route 2: B + E = 9
Route 3: D + F = 9
```

Swap 또는 SWAP\*를 통해 다음처럼 바뀔 수 있다.

```text
개선 후 feasible 해:
Route 1: A + B = 10
Route 2: C + D = 10
Route 3: E + F = 10
```

즉, 초기해가 모든 주문을 feasible하게 배정하지 못하는 것은 HGS-CVRP에서 큰 문제가 아니다. 오히려 infeasible 해를 출발점으로 삼아 feasible 해를 찾아갈 수 있다.

---

## 15. 차량 수가 정말로 부족한 경우

### 15.1 총 용량 자체가 부족한 경우

예를 들어 다음과 같은 경우는 명백히 불가능하다.

```text
차량 수 m = 2
차량 용량 Q = 10
총 주문 수요 = 25

전체 차량 용량 = 2 × 10 = 20
총 주문 수요 25 > 전체 차량 용량 20
```

이 경우 원 CVRP로는 모든 주문을 처리할 수 없다.

```text
feasible solution 없음
```

### 15.2 총 용량은 충분해도 조합상 불가능한 경우

다음 예시는 총 용량만 보면 가능해 보인다.

```text
차량 수 m = 3
차량 용량 Q = 10

주문:
A=6, B=6, C=6, D=6

총수요 = 24
전체 차량 용량 = 30
```

하지만 주문 6짜리 두 개를 같은 차량에 넣으면 12가 되어 용량을 넘는다. 따라서 각 주문은 사실상 단독 차량이 필요하다.

```text
A=6 → 단독 route 필요
B=6 → 단독 route 필요
C=6 → 단독 route 필요
D=6 → 단독 route 필요

필요 차량 수 = 4
보유 차량 수 = 3
```

이 경우 HGS-CVRP는 계속 infeasible 해를 만들 수밖에 없다.

```text
Route 1: A(6)
Route 2: B(6)
Route 3: C(6) + D(6) = 12  ← 용량 초과
```

Local search나 repair를 해도 feasible solution은 존재하지 않는다.

### 15.3 이때 알고리즘의 행동

```text
1. 모든 주문을 포함한 해를 계속 만든다.
2. 하지만 모든 해가 infeasible이다.
3. infeasible subpopulation에 좋은 infeasible 해들이 쌓인다.
4. penalty가 커지면서 용량 초과를 줄이려 한다.
5. repair도 시도한다.
6. 그래도 feasible solution은 나오지 않는다.
7. 최종 best feasible solution이 없을 수 있다.
```

---

## 16. 미배정 주문이 허용되는 실무 문제로 확장하기

### 16.1 원 CVRP와 실무 배차의 차이

실무에서는 다음과 같은 상황이 많다.

```text
오늘 차량이 부족하다.
일부 주문은 내일 배송할 수 있다.
일부 주문은 외주 차량으로 보낼 수 있다.
우선순위가 낮은 주문은 미룰 수 있다.
```

하지만 원 CVRP는 모든 고객 방문을 강제한다. 따라서 “일부 주문 미배정”을 허용하려면 모델을 바꿔야 한다.

### 16.2 가능한 모델링 방식

| 실무 상황 | 적합한 모델링 방식 |
|---|---|
| 주문 일부 포기 가능 | 미방문 penalty가 있는 optional customer VRP |
| 주문을 내일로 넘김 | rolling horizon 또는 deferral penalty |
| 외주 차량 사용 가능 | dummy vehicle 또는 outsourcing cost 추가 |
| 용량 초과가 약간 허용됨 | soft capacity VRP |
| 우선순위 높은 주문부터 처리 | service reward 또는 priority penalty 모델 |

### 16.3 목적함수 확장

미배정 주문을 허용하면 목적함수를 다음처럼 바꿀 수 있다.

```text
cost
= route_distance
+ α × capacity_excess
+ β × unassigned_orders_penalty
```

여기서 β가 크면 알고리즘은 가능한 한 주문을 배정하려고 한다.

### 16.4 해 표현도 바뀌어야 한다

기존 HGS-CVRP의 해 표현:

```text
chromT = [모든 고객의 순열]

예:
chromT = [3, 1, 5, 2, 4, 6]

Split 후:
Route 1: 3, 1
Route 2: 5, 2
Route 3: 4, 6
```

미배정 주문을 허용한 해 표현:

```text
servedT = [배정된 고객의 순열]
unserved = [미배정 고객 목록]

예:
servedT = [3, 1, 5, 2]
unserved = [4, 6]

Split 후:
Route 1: 3, 1
Route 2: 5, 2

미배정 penalty:
P4 + P6
```

### 16.5 추가로 필요한 Local Search move

기존 move:

```text
Relocate
Swap
2-Opt
2-Opt*
SWAP*
```

미배정 허용 시 추가 move:

```text
미배정 주문을 route에 삽입
route의 주문을 미배정으로 제거
배정 주문과 미배정 주문을 교환
```

예시:

```text
현재 해:
Route 1: A B
Route 2: C D
Unserved: E F

Move 1: 미배정 삽입
Route 1: A E B
Route 2: C D
Unserved: F

Move 2: 배정 제거
Route 1: A
Route 2: C D
Unserved: B E F

Move 3: 배정/미배정 교환
Route 1: A F
Route 2: C D
Unserved: B E
```

---

## 17. 원 HGS-CVRP와 실무형 확장 HGS 비교

| 항목 | 원 HGS-CVRP | 실무형 주문 미배정 허용 HGS |
|---|---|---|
| 모든 주문 방문 | 반드시 방문 | 선택 가능 |
| 미배정 주문 | 없음 | 있음 |
| infeasible의 의미 | 용량/시간 초과 | 용량/시간 초과 + 미배정 가능 |
| 기본 penalty | 용량초과, 시간초과 | 용량초과, 시간초과, 미배정 |
| 최종 답 | best feasible full-service solution | service level과 비용의 균형 |
| 차량 부족 시 | feasible 없으면 실패 가능 | 일부 주문 제외한 최선안 가능 |
| 적용 대상 | 표준 CVRP benchmark | 실제 배차 운영 문제에 가까움 |

---

## 18. 차량 부족 상황의 의사결정 흐름도

```text
입력:
- 차량 수 m
- 차량 용량 Q
- 모든 주문 수요

            ↓

사전 체크:
총수요 > m × Q ?
            ↓
    ┌────── Yes ──────┐
    │                 │
    │ 원 CVRP는 불가능 │
    │ 주문 이월/외주/미배정 모델 필요
    │                 │
    └─────────────────┘

            No
            ↓

HGS-CVRP 실행:
모든 주문을 chromT에 포함
            ↓
Split으로 route 생성
            ↓
route가 용량 초과할 수 있음
            ↓
infeasible solution으로 저장
            ↓
Local Search + Repair + Penalty 조정
            ↓
feasible solution 발견?
    ├─ Yes → best feasible 갱신
    └─ No  → infeasible 해만 남음
```

---

## 19. 실험 결과와 의의 요약

논문은 CVRPLIB X benchmark를 중심으로 HGS-CVRP를 여러 강한 알고리즘과 비교했다. 주요 비교 대상에는 OR-Tools, LKH-3, HILS, KGLS, SISR, FILO, 기존 HGS-2012 등이 포함된다.

요약하면 HGS-CVRP는 평균 gap과 최대 gap에서 매우 강한 성능을 보였고, 공개 구현을 통해 재현 가능성도 높였다.

| 관점 | 의의 |
|---|---|
| 성능 | benchmark에서 매우 낮은 평균 gap 보고 |
| 재현성 | 공개 C++ 구현 제공 |
| 단순성 | 복잡한 요소를 과도하게 늘리지 않고 핵심 구조를 정리 |
| 확장성 | SWAP\* 아이디어는 다른 VRP 변형에도 참고 가능 |

---

## 20. 한계와 주의점

### 20.1 표준 CVRP에 초점

논문과 기본 구현은 canonical CVRP에 초점을 둔다. 실제 물류 문제에서 자주 등장하는 다음 요소는 별도 확장이 필요할 수 있다.

```text
시간창
이기종 차량
운전자 근무시간
다중 depot
상하차 제약
도로망 기반 비대칭 이동시간
주문 이월
외주 차량
우선순위 배송
```

### 20.2 휴리스틱 알고리즘이라는 점

HGS-CVRP는 매우 강력한 휴리스틱/메타휴리스틱이지만, 모든 경우에 수학적 최적성을 증명하는 exact algorithm은 아니다.

```text
좋은 해를 빠르게 찾는 데 강함
하지만 항상 최적해를 증명하는 방식은 아님
```

### 20.3 미배정 주문은 기본 모델에 없음

차량 부족으로 일부 주문을 배정하지 않는 의사결정이 필요하다면, 원 HGS-CVRP를 그대로 쓰기보다 미배정 penalty, 외주 차량, 주문 이월 등의 모델 확장이 필요하다.

---

## 21. 핵심 암기 요약

### 21.1 HGS-CVRP 전체 구조

```text
1. 여러 배송계획 후보를 만든다.
2. 좋은 후보 두 개를 부모로 뽑는다.
3. OX crossover로 고객 방문 순서를 섞는다.
4. Split으로 차량별 route를 만든다.
5. Local search로 route를 강하게 개선한다.
6. 좋은 해는 유지하고, 너무 비슷하거나 나쁜 해는 제거한다.
7. feasible 해와 infeasible 해를 나눠 관리하면서 탐색 폭을 넓힌다.
8. 이 과정을 반복해 가장 좋은 feasible solution을 찾는다.
```

### 21.2 Local Search 구성

```text
Local Search
= Relocate  : 고객을 옮긴다
+ Swap      : 고객 둘을 바꾼다
+ 2-Opt     : route 안의 꼬인 구간을 뒤집는다
+ 2-Opt*    : route 두 개의 꼬리를 바꾼다
+ SWAP*     : route 간 고객을 교환하되, 각자 최적 위치에 넣는다
+ Granular search : 가까운 후보만 봐서 빠르게 만든다
```

### 21.3 차량 수가 빡빡할 때

```text
초기해가 모든 주문을 feasible하게 배정하지 못함
→ HGS-CVRP는 모든 주문을 일단 배정
→ 용량 초과분에 penalty 부여
→ infeasible population에 저장
→ local search와 repair로 feasible하게 고치려 시도
```

하지만 다음 경우에는 원 CVRP 자체가 불가능할 수 있다.

```text
차량 수와 용량으로 모든 주문을 처리할 수 없음
→ feasible solution 없음
→ 주문 이월, 외주 차량, 미배정 penalty 등의 실무형 확장 필요
```

### 21.4 SWAP\*의 핵심

```text
일반 Swap:
서로의 원래 위치에 들어감

SWAP*:
서로 route를 바꾸되,
각자 상대 route 안에서 가장 좋은 위치에 들어감
```

---

## 22. 참고 링크

- ScienceDirect 논문 페이지: https://www.sciencedirect.com/science/article/pii/S030505482100349X
- arXiv 원문: https://arxiv.org/pdf/2012.10384
- HGS-CVRP GitHub 저장소: https://github.com/vidalt/HGS-CVRP

---

## 23. 한 줄 결론

> HGS-CVRP는 **전역적으로는 다양한 배송계획을 진화시키고**, **국소적으로는 Relocate, Swap, 2-Opt, 2-Opt\*, SWAP\*로 강하게 다듬으며**, **feasible/infeasible 해를 함께 관리해 차량 용량 제약이 빡빡한 상황에서도 좋은 feasible 해를 찾으려는 알고리즘**이다.
