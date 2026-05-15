# HGS-CVRP 논문 질의응답 정리

> 대상 논문: **Thibaut Vidal, “Hybrid genetic search for the CVRP: Open-source implementation and SWAP\* neighborhood”**  
> 주제: **CVRP를 위한 Hybrid Genetic Search 구조, Local Search 구성, 차량 수가 빡빡한 상황에서의 작동 방식**

---

## 목차

1. [논문과 문제의 큰 그림](#1-논문과-문제의-큰-그림)
2. [CVRP 기본 개념](#2-cvrp-기본-개념)
3. [HGS-CVRP 전체 알고리즘 구조](#3-hgs-cvrp-전체-알고리즘-구조)
4. [해의 표현: Giant Tour와 Split](#4-해의-표현-giant-tour와-split)
5. [Population 관리와 Infeasible 해의 활용](#5-population-관리와-infeasible-해의-활용)
6. [Local Search 구성](#6-local-search-구성)
7. [SWAP\*: 이 논문의 핵심 Local Search 기여](#7-swap-이-논문의-핵심-local-search-기여)
8. [차량 대수가 빡빡한 상황에서의 작동 방식](#8-차량-대수가-빡빡한-상황에서의-작동-방식)
9. [실무 적용 시 필요한 확장](#9-실무-적용-시-필요한-확장)
10. [핵심 요약](#10-핵심-요약)
11. [참고 링크](#11-참고-링크)

---

# 1. 논문과 문제의 큰 그림

이 논문은 **Capacitated Vehicle Routing Problem, CVRP**를 풀기 위한 강력한 메타휴리스틱 알고리즘을 제안하고, 그 구현을 공개한 연구입니다.

논문의 핵심은 다음 두 가지입니다.

1. **HGS-CVRP**  
   CVRP 전용 **Hybrid Genetic Search** 알고리즘입니다.

2. **SWAP\***  
   서로 다른 route 사이의 고객을 교환하되, 단순히 서로의 기존 위치에 넣는 것이 아니라 **상대 route 안에서 가장 좋은 삽입 위치를 찾아 넣는** 새로운 local search neighborhood입니다.

한 문장으로 요약하면 다음과 같습니다.

> **HGS-CVRP는 여러 배송계획 후보를 진화시키는 유전 알고리즘과, 각 후보를 강하게 다듬는 Local Search를 결합한 CVRP 전용 고성능 알고리즘이다.**

---

# 2. CVRP 기본 개념

## 2.1 CVRP란?

CVRP는 다음과 같은 문제입니다.

> 하나의 창고, 즉 depot에서 여러 대의 차량이 출발하여 고객들을 방문한 뒤 다시 depot으로 돌아온다.  
> 모든 고객은 정확히 한 번 방문해야 하고, 각 차량은 정해진 적재용량을 초과하면 안 된다.  
> 이때 전체 이동거리 또는 비용을 최소화한다.

간단한 예시는 다음과 같습니다.

```text
Route 1: 0 → A → B → C → 0
Route 2: 0 → D → E → 0
Route 3: 0 → F → G → H → 0

0 = depot, 즉 창고
A~H = 고객
```

CVRP에서 알고리즘이 결정해야 하는 것은 크게 두 가지입니다.

| 결정 항목 | 설명 |
|---|---|
| 고객 묶음 | 어떤 고객들을 같은 차량 route에 넣을 것인가? |
| 방문 순서 | 각 차량이 고객들을 어떤 순서로 방문할 것인가? |

즉, 단순히 가까운 고객부터 방문하는 문제가 아니라, **고객 배정과 방문 순서를 동시에 최적화하는 문제**입니다.

## 2.2 CVRP의 핵심 제약

| 제약 | 의미 |
|---|---|
| 모든 고객 방문 | 고객은 정확히 한 번 방문되어야 함 |
| 차량 용량 | 각 route의 총 수요량은 차량 용량을 넘으면 안 됨 |
| depot 출발·도착 | 각 차량은 depot에서 출발하고 depot으로 돌아와야 함 |
| 비용 최소화 | 전체 이동거리 또는 비용을 최소화해야 함 |

---

# 3. HGS-CVRP 전체 알고리즘 구조

## 3.1 HGS-CVRP의 기본 아이디어

HGS-CVRP는 이름 그대로 **Hybrid Genetic Search**입니다.

여기서 hybrid라는 말은 두 가지 접근을 결합한다는 뜻입니다.

| 구성 | 역할 |
|---|---|
| Genetic Search | 여러 후보 해를 섞고 진화시켜 새로운 해를 만듦 |
| Local Search | 만들어진 해를 세부적으로 고쳐 더 좋은 해로 개선함 |

즉, HGS-CVRP는 다음과 같은 방식으로 작동합니다.

```text
여러 배송계획 후보를 만든다
        ↓
좋은 후보 두 개를 부모로 고른다
        ↓
부모를 섞어 새로운 자식 해를 만든다
        ↓
자식 해를 local search로 강하게 개선한다
        ↓
좋은 해는 population에 보관한다
        ↓
이 과정을 반복한다
```

## 3.2 전체 흐름도

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
│ Binary tournament  │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ 2. OX Crossover    │
│ + Split            │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ 3. Local Search    │
│ + 필요 시 Repair   │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ 4. Population 삽입  │
│ 다양성·벌점 관리    │
└─────────┬──────────┘
          ↓
   개선 없음 / 시간 종료?
          ↓
┌────────────────────┐
│ Best feasible 해 반환│
└────────────────────┘
```

## 3.3 학부생용 의사코드

```text
1. 랜덤 배송계획들을 여러 개 만든다.
2. 각각을 local search로 다듬는다.
3. feasible population과 infeasible population에 나눠 넣는다.

4. while 종료 조건이 아니면:
      a. 부모 해 P1, P2를 고른다.
      b. P1, P2를 섞어서 자식 순서 C를 만든다.
      c. Split으로 C를 실제 차량 route들로 자른다.
      d. Local search로 C를 개선한다.
      e. C가 용량 초과라면, 일정 확률로 repair를 시도한다.
      f. C를 알맞은 population에 넣는다.
      g. population이 너무 커지면 나쁜 해나 중복 해를 제거한다.
      h. infeasible 벌점 계수를 조정한다.

5. 가장 좋은 feasible solution을 반환한다.
```

## 3.4 주요 구성요소 요약

| 구성요소 | 쉬운 비유 | 실제 역할 |
|---|---|---|
| Population | 여러 개의 배송계획 후보 모음 | 다양한 가능성을 유지 |
| Parent selection | 좋은 후보끼리 부모로 뽑기 | 좋은 구조를 다음 해로 전달 |
| Crossover | 부모 계획을 섞기 | 새로운 고객 방문 순서 생성 |
| Split | 긴 방문 순서를 차량별 route로 자르기 | CVRP 해로 복원 |
| Local Search | 손으로 세부 경로를 고치기 | 거리 단축, 용량 개선 |
| Repair | 용량 초과 해를 고치기 | infeasible 해를 feasible 쪽으로 유도 |
| Population management | 너무 비슷하거나 나쁜 해 제거 | 품질과 다양성 유지 |

---

# 4. 해의 표현: Giant Tour와 Split

## 4.1 Route 표현

CVRP 해는 원래 차량별 route 형태입니다.

```text
Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 6 → 0
Route 3: 0 → 7 → 8 → 10 → 4 → 0
```

하지만 crossover를 할 때는 이 형태가 다루기 어렵습니다. 그래서 depot을 잠시 빼고, 고객 방문 순서만 긴 배열로 표현합니다.

이를 **Giant Tour**라고 생각하면 됩니다.

```text
Giant tour:
2  1  3  9  5  6  7  8  10  4
```

## 4.2 Split의 역할

Split은 giant tour를 실제 차량 route들로 다시 자르는 과정입니다.

```text
Giant tour:
2  1  3  9  5  6  7  8  10  4

Split 결과:
2  1  3 | 9  5  6 | 7  8  10  4

Route 1: 0 → 2 → 1 → 3 → 0
Route 2: 0 → 9 → 5 → 6 → 0
Route 3: 0 → 7 → 8 → 10 → 4 → 0
```

Split은 아무렇게나 자르는 것이 아니라, 주어진 고객 순서를 유지하면서 좋은 route 구분점을 찾습니다.

## 4.3 OX Crossover 예시

HGS-CVRP는 **Ordered Crossover, OX**를 사용합니다.

예를 들어 부모가 다음과 같다고 합시다.

```text
부모 1:
1  2  3  4 [5  6  7  8] 9  10

부모 2:
5  4  6  2  1  3  8  9  7  10
```

부모 1의 `[5 6 7 8]` 구간을 자식에게 그대로 물려줍니다.

```text
자식:
_  _  _  _ [5  6  7  8] _  _
```

그다음 부모 2의 순서를 따라, 이미 들어간 5, 6, 7, 8을 제외하고 나머지를 채웁니다.

```text
부모 2에서 5, 6, 7, 8 제거:
4  2  1  3  9  10
```

결과적으로 자식 giant tour는 다음과 같은 형태가 됩니다.

```text
자식 giant tour:
2  1  3  9 [5  6  7  8] 10  4
```

이 자식은 아직 차량별 route가 아닙니다. 이후 Split이 route 구분점을 넣어 실제 CVRP 해로 바꿉니다.

---

# 5. Population 관리와 Infeasible 해의 활용

## 5.1 Feasible 해와 Infeasible 해를 모두 사용한다

HGS-CVRP의 중요한 특징은 **용량을 초과한 해를 즉시 버리지 않는다는 점**입니다.

Population은 크게 두 그룹으로 나뉩니다.

```text
Population
│
├── Feasible subpopulation
│   └── 용량 제약을 만족하는 해
│
└── Infeasible subpopulation
    └── 용량을 초과하지만 penalty를 붙여 보관하는 해
```

## 5.2 왜 infeasible 해를 보관하는가?

어떤 해는 현재는 용량을 조금 초과하지만, 좋은 feasible 해 근처에 있을 수 있습니다.

예를 들어 다음과 같은 해가 있다고 합시다.

```text
Route 1: A(6) + C(6) = 12   ← 용량 10 기준으로 2 초과
Route 2: B(4) + E(5) = 9
Route 3: D(4) + F(5) = 9
```

이 해는 infeasible입니다. 하지만 고객을 조금만 바꾸면 다음처럼 feasible하게 만들 수 있습니다.

```text
Route 1: A(6) + B(4) = 10
Route 2: C(6) + D(4) = 10
Route 3: E(5) + F(5) = 10
```

따라서 처음부터 infeasible 해를 버리면 좋은 해로 갈 수 있는 기회를 잃을 수 있습니다.

## 5.3 Penalty 기반 비용

Infeasible 해는 그냥 좋은 해로 취급하지 않습니다. 용량을 초과한 만큼 벌점을 받습니다.

```text
penalized cost
= 총 이동거리
+ 용량초과량 × penaltyCapacity
+ 운행시간초과량 × penaltyDuration
```

순수 CVRP에서는 duration constraint가 없을 수 있으므로, 핵심은 다음입니다.

```text
penalized cost
= 총 이동거리 + 용량초과량 × penalty
```

즉, 용량을 많이 초과할수록 나쁜 해가 됩니다.

## 5.4 Repair

Local search 후에도 해가 infeasible이면, HGS-CVRP는 일정 확률로 repair를 시도합니다.

Repair는 쉽게 말해 다음과 같습니다.

```text
현재 해가 용량 초과 상태
        ↓
벌점 계수를 크게 높임
        ↓
Local Search를 다시 수행
        ↓
용량 초과를 줄이는 방향으로 강하게 유도
```

흐름으로 보면 다음과 같습니다.

```text
Local search 후 해 C가 infeasible
        ↓
일정 확률로 Repair 실행
        ↓
penalty를 크게 높여 Local Search 재실행
        ↓
feasible이 되면 Feasible population에 저장
        ↓
infeasible이 남으면 Infeasible population에 저장
```

## 5.5 Diversity 관리

Population에 좋은 해만 남기면 모든 해가 비슷해질 수 있습니다.

```text
좋은 해만 남김
    ↓
해들이 서로 비슷해짐
    ↓
새로운 조합이 잘 나오지 않음
    ↓
탐색이 정체됨
```

그래서 HGS-CVRP는 해의 품질뿐 아니라 **다양성**도 고려합니다.

| 평가 요소 | 의미 |
|---|---|
| 해의 품질 | 이동거리 또는 penalized cost가 낮은가? |
| 해의 다양성 | 기존 population의 해들과 충분히 다른가? |

즉, 단순히 가장 짧은 거리의 해만 남기는 것이 아니라, 다른 구조를 가진 해도 보존하여 탐색 폭을 유지합니다.

---

# 6. Local Search 구성

## 6.1 Local Search란?

Local Search는 현재 해를 조금씩 바꿔보면서 더 좋은 해를 찾는 과정입니다.

예를 들어 현재 route가 다음과 같다고 합시다.

```text
현재 route:
0 → A → B → C → D → 0
```

고객 B와 C의 순서를 바꿔보면 다음과 같습니다.

```text
변경 후:
0 → A → C → B → D → 0
```

변경 후 route가 더 짧다면 이 변경을 받아들입니다.

## 6.2 Local Search의 기본 루프

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

여기서 local minimum은 **현재 사용 중인 move들로는 더 이상 좋아지지 않는 상태**입니다. 전역 최적해라는 뜻은 아닙니다.

## 6.3 HGS-CVRP에서 사용하는 주요 move

| Move | 핵심 아이디어 | 같은 route 안에서 가능? | 다른 route 사이에서 가능? |
|---|---|---:|---:|
| Relocate | 고객 하나 또는 연속 고객 2개를 다른 위치로 옮김 | 가능 | 가능 |
| Swap | 고객 둘 또는 고객 묶음 둘을 서로 교환 | 가능 | 가능 |
| 2-Opt | 한 route 안에서 꼬인 구간을 뒤집음 | 가능 | 보통 같은 route |
| 2-Opt\* | 두 route의 뒷부분을 교환 | 아니오 | 가능 |
| SWAP\* | 서로 다른 route의 고객 둘을 교환하되, 각자 가장 좋은 삽입 위치를 찾음 | 아니오 | 가능 |

---

## 6.4 Relocate

Relocate는 고객 하나를 다른 위치로 옮기는 move입니다.

### 같은 route 안에서의 Relocate

```text
변경 전:
0 → A → [B] → C → D → 0

B를 D 뒤로 이동

변경 후:
0 → A → C → D → [B] → 0
```

### 다른 route로의 Relocate

```text
변경 전:
Route 1: 0 → A → [B] → C → 0
Route 2: 0 → D → E → 0

변경 후:
Route 1: 0 → A → C → 0
Route 2: 0 → D → [B] → E → 0
```

단, 다른 route로 옮길 때는 차량 용량을 초과하는지 확인해야 합니다.

---

## 6.5 Swap

Swap은 고객 둘의 자리를 바꾸는 move입니다.

```text
변경 전:
Route 1: 0 → A → [B] → C → 0
Route 2: 0 → D → [E] → F → 0

B와 E를 교환

변경 후:
Route 1: 0 → A → [E] → C → 0
Route 2: 0 → D → [B] → F → 0
```

Swap은 Relocate보다 용량 제약을 맞추기 쉬운 경우가 있습니다.

예를 들어 B를 Route 2로 그냥 옮기면 용량 초과가 되지만, E를 Route 1로 동시에 보내면 양쪽 용량이 맞을 수 있습니다.

---

## 6.6 2-Opt

2-Opt는 route 안에서 간선 두 개를 끊고, 중간 구간을 뒤집는 move입니다.

```text
변경 전:
0 → A → B → C → D → 0

간선 A-B와 C-D를 끊고 재연결

변경 후:
0 → A → C → B → D → 0
```

좀 더 추상적으로는 다음과 같습니다.

```text
기존 간선:
A → B
C → D

새 간선:
A → C
B → D
```

2-Opt는 route가 교차하거나 비효율적인 순서를 가질 때 매우 효과적입니다.

---

## 6.7 2-Opt\*

2-Opt\*는 두 route 사이에서 뒤쪽 꼬리 부분을 교환하는 move입니다.

```text
변경 전:
Route 1: 0 → A → B | C → D → 0
Route 2: 0 → E → F | G → H → 0

| 뒤쪽 꼬리를 서로 교환

변경 후:
Route 1: 0 → A → B | G → H → 0
Route 2: 0 → E → F | C → D → 0
```

이 move는 단일 고객 이동보다 더 큰 구조 변화를 만들 수 있습니다.

---

## 6.8 Local Search를 빠르게 만드는 방법

### 변경된 간선만 계산한다

move 하나를 평가할 때 전체 route 길이를 처음부터 다시 계산하면 느립니다.

예를 들어 고객 v를 옮긴다고 합시다.

```text
변경 전:
... a → v → b ...
... c → d ...

변경 후:
... a → b ...
... c → v → d ...
```

이때 바뀐 간선만 보면 됩니다.

```text
사라지는 간선:
a → v
v → b
c → d

새로 생기는 간선:
a → b
c → v
v → d
```

따라서 비용 변화량은 다음처럼 계산할 수 있습니다.

```text
Δ = 새 간선 비용 합 - 기존 간선 비용 합

Δ < 0  → 거리 감소, move 적용
Δ ≥ 0  → 개선 없음, move 버림
```

### 가까운 고객쌍만 우선 검사한다

고객이 많으면 모든 고객쌍을 검사하는 것이 너무 비쌉니다.

```text
고객 1,000명
→ 가능한 고객쌍 약 1,000,000개
```

그래서 HGS-CVRP는 각 고객마다 가까운 고객 후보만 살펴봅니다.

```text
고객 i의 가까운 이웃 Γ개:

i ─ 가까움 ─ j1
i ─ 가까움 ─ j2
i ─ 가까움 ─ j3
...
i ─ 가까움 ─ jΓ

너무 먼 고객은 우선 제외
```

이 방식을 **granular search**라고 이해하면 됩니다.

---

# 7. SWAP\*: 이 논문의 핵심 Local Search 기여

## 7.1 일반 Swap과 SWAP\*의 차이

일반 Swap은 서로 다른 route의 두 고객을 **서로의 기존 위치에 그대로 교환**합니다.

```text
변경 전:
Route r : 0 → A → [v]  → B → C → 0
Route r': 0 → D → E → [v'] → F → G → 0

일반 Swap 후:
Route r : 0 → A → [v'] → B → C → 0
Route r': 0 → D → E → [v]  → F → G → 0
```

반면 SWAP\*는 두 고객을 교환하되, 각 고객을 상대 route 안에서 가장 좋은 위치에 삽입합니다.

```text
변경 전:
Route r : 0 → A → [v] → B → C → 0
Route r': 0 → D → E → [v'] → F → G → 0

SWAP* 후:
Route r : 0 → A → B → [v'] → C → 0
Route r': 0 → D → [v] → E → F → G → 0
```

핵심 차이는 다음입니다.

| 방식 | 설명 |
|---|---|
| 일반 Swap | v는 v′의 원래 자리로, v′는 v의 원래 자리로 이동 |
| SWAP\* | v와 v′가 route를 바꾸되, 각자 상대 route 안의 가장 좋은 위치로 이동 |

## 7.2 왜 SWAP\*가 강력한가?

일반 Swap은 이렇게 말하는 것과 같습니다.

```text
너는 저 사람 자리로 가고,
저 사람은 네 자리로 와.
```

SWAP\*는 이렇게 말하는 것과 같습니다.

```text
너희 둘은 서로 route를 바꾸되,
각 route 안에서 가장 자연스러운 위치를 찾아 들어가.
```

그래서 SWAP\*는 다음 상황에서 특히 유용합니다.

```text
Relocate 하나만 하면 용량 초과
일반 Swap 제자리 교환은 거리 개선이 작음
하지만 두 고객을 교환하면서 각각 최적 위치에 넣으면 큰 개선 가능
```

## 7.3 SWAP\*의 계산량 문제

SWAP\*는 강력하지만, 순진하게 구현하면 경우의 수가 매우 많습니다.

고객 v와 v′를 고른 뒤에도 다음을 모두 따져야 하기 때문입니다.

```text
v를 route r'의 어디에 넣을까?
v'를 route r의 어디에 넣을까?
```

모든 위치를 다 보면 계산량이 커집니다.

## 7.4 SWAP\*를 빠르게 만드는 아이디어

### 아이디어 1: 좋은 삽입 위치 Top 3만 사용

어떤 고객 v를 route r′에 넣을 때 모든 위치를 다 보지 않고, 좋은 후보 위치만 봅니다.

```text
v를 route r'에 넣을 후보 위치:

1. v'가 빠진 바로 그 자리
2. v에게 좋아 보였던 삽입 위치 1등
3. v에게 좋아 보였던 삽입 위치 2등
4. v에게 좋아 보였던 삽입 위치 3등
```

### 아이디어 2: route pair 단위로 전처리

SWAP\* 탐색은 두 route 쌍을 기준으로 진행됩니다.

```text
Route r 안의 각 고객 v에 대해:
    v를 route r'에 넣을 좋은 위치 Top 3 계산

Route r' 안의 각 고객 v'에 대해:
    v'를 route r에 넣을 좋은 위치 Top 3 계산

그다음:
    v와 v'의 모든 쌍을 보면서
    가장 좋은 SWAP* 조합을 찾음
```

### 아이디어 3: polar sector로 route pair를 줄임

지리적으로 완전히 다른 방향의 route끼리는 교환해도 좋아질 가능성이 낮습니다.

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

따라서 depot 기준으로 route들이 차지하는 부채꼴 영역이 겹치는 경우를 우선적으로 평가합니다.

---

# 8. 차량 대수가 빡빡한 상황에서의 작동 방식

## 8.1 중요한 전제

HGS-CVRP 원 논리에서는 **미배정 주문**이라는 개념이 기본적으로 없습니다.

CVRP는 모든 고객을 정확히 한 번 방문해야 하는 문제입니다. 따라서 차량 대수가 빡빡하더라도 알고리즘은 주문을 빼는 것이 아니라, **모든 주문을 일단 route에 포함**시킵니다.

문제가 되는 경우에는 일부 route가 용량을 초과한 **infeasible solution**이 됩니다.

## 8.2 상황을 세 가지로 나누어 이해하기

| 상황 | 의미 | HGS-CVRP에서의 처리 |
|---|---|---|
| A. 물리적으로 불가능 | 차량 수와 용량으로는 전체 주문을 도저히 실을 수 없음 | feasible solution이 존재하지 않음 |
| B. 초기 생성 방법이 부족함 | 실제로는 가능한데 초기해가 못 찾음 | infeasible 해를 허용하며 탐색 계속 |
| C. 일부 주문 포기가 허용됨 | 오늘 못 싣는 주문은 미배정 또는 이월 가능 | 원 CVRP가 아니므로 모델 수정 필요 |

HGS-CVRP가 잘 대응하는 상황은 주로 **B**입니다.

즉, 초기해가 나빠서 용량을 초과하더라도, local search와 repair를 통해 feasible solution을 찾아갈 수 있습니다.

---

## 8.3 초기해가 infeasible이지만 feasible solution이 존재하는 경우

예를 들어 차량 3대, 차량 용량 10, 주문 6개가 있다고 합시다.

```text
차량 수 m = 3
차량 용량 Q = 10

주문 수요:
A=6, B=4, C=6, D=4, E=5, F=5
```

좋은 배정은 다음과 같습니다.

```text
Route 1: A(6) + B(4) = 10
Route 2: C(6) + D(4) = 10
Route 3: E(5) + F(5) = 10
```

그런데 초기 Split 결과가 나쁘면 다음처럼 될 수 있습니다.

```text
Route 1: A(6) + C(6) = 12   ← 용량 2 초과
Route 2: B(4) + E(5) = 9
Route 3: D(4) + F(5) = 9
```

이때 HGS-CVRP는 A나 C를 미배정으로 빼지 않습니다.

대신 이렇게 판단합니다.

```text
모든 주문은 배정됨
하지만 Route 1이 용량을 2만큼 초과함
따라서 이 해는 infeasible solution
```

그다음 local search가 다음과 같은 변경을 시도합니다.

```text
Relocate:
C를 Route 1에서 다른 route로 옮길 수 있는가?

Swap:
C와 B를 바꾸면 어떤가?

SWAP*:
C와 B를 바꾸되, 서로의 route 안에서 가장 좋은 위치에 넣으면 어떤가?
```

개선 후 다음과 같은 feasible 해를 찾을 수 있습니다.

```text
Route 1: A(6) + B(4) = 10
Route 2: C(6) + D(4) = 10
Route 3: E(5) + F(5) = 10
```

즉, **초기해가 모든 주문을 feasible하게 배정하지 못하는 것 자체는 HGS-CVRP에서 큰 문제가 아닙니다.**

---

## 8.4 차량 수가 실제로 부족해 feasible solution이 없는 경우

다음 예시를 보겠습니다.

```text
차량 수 m = 3
차량 용량 Q = 10

주문 수요:
A=6, B=6, C=6, D=6
```

총수요는 24이고 전체 차량 용량은 30입니다.

```text
총수요 = 24
전체 차량 용량 = 3 × 10 = 30
```

겉보기에는 가능해 보입니다. 하지만 각 주문이 6이므로 한 차량에 주문 두 개를 실으면 12가 되어 용량을 초과합니다.

```text
6 + 6 = 12 > 10
```

따라서 주문 4개를 각각 단독 차량에 실어야 하지만 차량은 3대뿐입니다.

```text
필요 route 수 = 4
사용 가능 차량 수 = 3
```

이 경우 feasible solution은 없습니다.

HGS-CVRP는 다음처럼 작동합니다.

```text
1. 모든 주문을 포함한 해를 계속 만든다.
2. 하지만 모든 해가 infeasible이다.
3. infeasible subpopulation에 좋은 infeasible 해들이 쌓인다.
4. penalty가 점점 커지면서 용량 초과를 줄이려 한다.
5. repair도 시도한다.
6. 그래도 feasible solution은 나오지 않는다.
7. 최종적으로 best feasible solution이 없을 수 있다.
```

중요한 점은 다음입니다.

> **HGS-CVRP는 “이 주문은 미배정하자”라고 자동 판단하지 않는다.**

원 CVRP의 목적은 모든 고객을 방문하는 것이기 때문입니다.

---

## 8.5 전체 용량 자체가 부족한 경우

예를 들어 다음은 명백히 불가능합니다.

```text
차량 수 m = 2
차량 용량 Q = 10
총 주문 수요 = 25

전체 차량 용량 = 2 × 10 = 20
총 주문 수요 = 25
```

이 경우에는 전체 용량이 총수요보다 작으므로 어떤 배정을 해도 모든 주문을 처리할 수 없습니다.

```text
총수요 > m × Q
→ 원 CVRP는 infeasible
```

HGS-CVRP 구현에서는 이런 기본적인 불가능성을 사전에 검사할 수 있습니다.

---

## 8.6 차량이 빡빡한 상황의 작동 흐름

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

# 9. 실무 적용 시 필요한 확장

## 9.1 실제 배차에서는 미배정 주문이 필요할 수 있다

실무에서는 다음과 같은 상황이 자주 발생합니다.

```text
오늘 차량이 부족하다.
일부 주문은 내일 배송해야 한다.
일부 주문은 외주 차량으로 보내야 한다.
일부 고객은 배송 지연 안내를 해야 한다.
```

이런 상황은 원 CVRP와 다릅니다.

원 CVRP는 모든 고객을 반드시 방문해야 하지만, 실무에서는 **일부 주문 미배정**, **이월**, **외주 처리** 같은 선택지가 있을 수 있습니다.

## 9.2 가능한 모델링 확장

| 실무 상황 | 적합한 모델링 방식 |
|---|---|
| 주문을 일부 포기 가능 | 미방문 penalty가 있는 optional customer VRP |
| 주문을 내일로 넘김 | rolling horizon 또는 deferral penalty |
| 외주 차량 사용 가능 | dummy vehicle 또는 outsourcing cost 추가 |
| 용량 초과가 조금 허용됨 | soft capacity VRP |
| 우선순위 높은 주문부터 처리 | service reward 또는 priority penalty 모델 |

## 9.3 미배정 주문 penalty를 추가한 목적함수

가장 단순한 확장은 다음과 같습니다.

```text
목적함수 =
    총 이동거리
  + 용량초과 penalty
  + 미배정 주문 penalty
```

즉,

```text
cost =
    route_distance
  + α × capacity_excess
  + β × unassigned_orders_penalty
```

여기서 β를 크게 주면 알고리즘은 가능한 한 많은 주문을 배정하려고 합니다.

## 9.4 해 표현도 바꿔야 한다

기존 HGS-CVRP의 해 표현은 모든 고객을 포함합니다.

```text
chromT = [모든 고객의 순열]

예:
chromT = [3, 1, 5, 2, 4, 6]
```

미배정 주문을 허용하려면 해 표현을 다음처럼 바꿔야 합니다.

```text
servedT = [배정된 고객의 순열]
unserved = [미배정 고객 목록]

예:
servedT = [3, 1, 5, 2]
unserved = [4, 6]
```

Split 후에는 다음과 같이 됩니다.

```text
servedT:
[3, 1, 5, 2]

Split:
Route 1: 3, 1
Route 2: 5, 2

Unserved:
4, 6
```

그리고 미배정 주문에는 penalty가 붙습니다.

## 9.5 Local Search에도 새 move가 필요하다

미배정 주문을 허용하면 기존 move만으로는 충분하지 않습니다.

기존 move는 다음과 같습니다.

```text
- Relocate
- Swap
- 2-Opt
- 2-Opt*
- SWAP*
```

미배정 주문이 있는 모델에서는 다음 move가 추가되어야 합니다.

```text
- 미배정 주문을 route에 삽입
- route의 주문을 미배정으로 제거
- 배정 주문과 미배정 주문을 교환
```

예시는 다음과 같습니다.

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

## 9.6 원 HGS-CVRP와 실무형 확장의 차이

| 항목 | 원 HGS-CVRP | 실무형 주문 미배정 허용 HGS |
|---|---|---|
| 모든 주문 방문 | 반드시 방문 | 선택 가능 |
| 미배정 주문 | 없음 | 있음 |
| infeasible의 의미 | 용량/시간 초과 | 용량/시간 초과 + 미배정 가능 |
| 기본 penalty | 용량초과, 시간초과 | 용량초과, 시간초과, 미배정 |
| 최종 답 | best feasible full-service solution | service level과 비용의 균형 |
| 차량 부족 시 | feasible 없으면 실패 가능 | 일부 주문 제외한 최선안 가능 |

---

# 10. 핵심 요약

## 10.1 HGS-CVRP 전체 요약

```text
1. 여러 배송계획 후보를 갖고 시작한다.
2. 좋은 후보 두 개를 부모로 뽑는다.
3. OX crossover로 고객 방문 순서를 섞는다.
4. Split으로 차량별 route를 만든다.
5. Local search로 route를 강하게 개선한다.
6. 좋은 해는 유지하고, 너무 비슷하거나 나쁜 해는 제거한다.
7. feasible 해와 infeasible 해를 나눠 관리하면서 탐색 폭을 넓힌다.
8. 이 과정을 반복해 가장 좋은 feasible solution을 찾는다.
```

## 10.2 Local Search 요약

```text
Local Search =
    Relocate  : 고객을 옮긴다
  + Swap      : 고객 둘을 바꾼다
  + 2-Opt     : route 안의 꼬인 구간을 뒤집는다
  + 2-Opt*    : route 두 개의 꼬리를 바꾼다
  + SWAP*     : route 간 고객을 교환하되, 각자 최적 위치에 넣는다
  + Granular search : 가까운 후보만 봐서 빠르게 만든다
```

## 10.3 차량이 빡빡할 때 요약

```text
차량이 빡빡함
→ 초기 route가 용량을 넘을 수 있음
→ 주문을 미배정으로 빼지 않음
→ 모든 주문을 일단 배정함
→ 용량 초과분에 penalty를 줌
→ infeasible subpopulation에 저장함
→ local search와 repair로 feasible하게 고치려 함
→ feasible 해가 존재하면 점차 찾아감
→ feasible 해가 애초에 없으면 best feasible은 나오지 않음
```

## 10.4 가장 중요한 결론

> **초기해가 모든 주문을 feasible하게 배정하지 못하는 것은 HGS-CVRP에서 큰 문제가 아니다.**  
> 알고리즘은 모든 주문을 일단 포함한 뒤, 용량 초과를 penalty로 처리하고, local search와 repair로 feasible solution을 찾아간다.

하지만 다음도 중요합니다.

> **차량 수와 용량으로는 애초에 모든 주문 처리가 불가능한 상황이라면, 원 CVRP 모델 자체가 infeasible이다.**  
> 이 경우에는 주문 이월, 외주 차량, 미배정 penalty 같은 실무형 확장이 필요하다.

---

# 11. 참고 링크

- ScienceDirect 논문 페이지  
  https://www.sciencedirect.com/science/article/pii/S030505482100349X

- arXiv 공개 원문  
  https://arxiv.org/pdf/2012.10384

- HGS-CVRP GitHub 구현  
  https://github.com/vidalt/HGS-CVRP

