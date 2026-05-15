# 07. 논문 리뷰와 성능 분석

## 1. Ropke & Pisinger: ALNS for PDPTW

대상 논문:

```text
An Adaptive Large Neighborhood Search Heuristic
for the Pickup and Delivery Problem with Time Windows
```

핵심 기여:

- PDPTW에 ALNS 프레임워크 적용
- 여러 removal / insertion heuristic 결합
- adaptive weight update로 operator 선택 확률 조정
- simulated annealing과 noise로 탐색 다양화
- 차량 수 최소화에도 적용

핵심 구조:

```text
현재 해
→ 일부 요청 제거
→ 제거 요청 재삽입
→ 후보해 생성
→ simulated annealing으로 수락 여부 결정
→ operator 성과에 따라 가중치 업데이트
```

주요 removal:

| Removal | 의미 |
|---|---|
| Shaw removal | 비슷한 요청들을 함께 제거 |
| Random removal | 무작위 요청 제거 |
| Worst removal | 현재 비용 기여가 큰 요청 제거 |

주요 insertion:

| Insertion | 의미 |
|---|---|
| Basic greedy | 현재 가장 싸게 넣을 수 있는 위치 선택 |
| Regret-2 | 지금 넣지 않으면 손해가 큰 요청 우선 |
| Regret-k | 여러 후보 위치 간 regret 합산 |

실험상 construction heuristic 단독으로는 해 품질이 낮지만, LNS/ALNS 안에서 반복적으로 사용하면 강력한 구성요소가 된다.

중요한 실험 메시지:

- Regret 계열이 greedy보다 일반적으로 좋은 초기 construction 품질을 보인다.
- 한 번에 너무 적게 제거하면 변화가 작고, 너무 많이 제거하면 복구가 어렵다.
- ALNS는 단순 LNS보다 best known 발견 수, 평균 gap, 실패 횟수에서 유리하다.
- 큰 인스턴스일수록 ALNS의 장점이 뚜렷하다.

주의점:

- ALNS는 최적해 보장 알고리즘이 아니다.
- 파라미터가 많고 성능에 영향을 준다.
- 실행 시간은 당시 하드웨어 기준이므로 절대값보다 상대 비교를 봐야 한다.

## 2. Vidal: HGS-CVRP와 SWAP*

대상 논문:

```text
Hybrid genetic search for the CVRP:
Open-source implementation and SWAP* neighborhood
```

핵심 기여:

- CVRP용 공개 HGS 구현 제공
- 간결한 HGS 구조 정리
- SWAP* neighborhood 제안
- CVRPLIB benchmark에서 강한 성능 보고

HGS-CVRP 구성:

```text
Genetic Search
+ Split
+ Local Search
+ Feasible/Infeasible population management
+ Diversity control
+ SWAP* neighborhood
```

SWAP* 핵심:

```text
일반 Swap:
서로의 기존 위치에 들어감

SWAP*:
서로 route를 바꾸되,
각자 상대 route 안에서 가장 좋은 위치에 들어감
```

HGS-CVRP의 강점:

- population 기반으로 전역 탐색을 수행한다.
- local search가 강하다.
- feasible/infeasible population을 함께 관리해 경계 영역을 탐색한다.
- diversity control로 조기 수렴을 줄인다.
- 공개 구현으로 재현 가능성이 높다.

한계:

- 기본 논문과 구현은 canonical CVRP에 초점을 둔다.
- 시간창, 이기종 차량, 다중 depot, 우선순위, 미배정 주문은 별도 확장이 필요하다.
- 차량 수와 capacity로 모든 주문 처리가 불가능한 경우 원 CVRP 모델로는 feasible solution이 없다.

실무 적용 시 해석:

```text
표준 CVRP/CVRPTW benchmark 성능 목표 → HGS 유력
zone, 미배송, 외주, 우선순위가 강한 실무 문제 → ALNS가 더 유연
```

## 3. Voigt: ALNS Operator Review and Ranking

대상 논문:

```text
A review and ranking of operators in adaptive large neighborhood search
for vehicle routing problems
```

분석 범위:

- 2006년부터 2023년까지의 ALNS 기반 VRP 연구
- 211편의 peer-reviewed journal article
- removal operator 사용 사례 1266개
- insertion operator 사용 사례 788개
- 일반 VRP 적용 가능한 removal 57개
- 일반 VRP 적용 가능한 insertion 42개

핵심 질문:

```text
많이 쓰이는 operator가 실제로도 좋은가?
어떤 removal / insertion operator 조합에서 출발해야 하는가?
```

중요 결론:

- 사용 빈도와 성능 순위는 동일하지 않다.
- random removal과 worst removal은 널리 쓰이고 안정적인 기본 연산자다.
- sequence-based removal은 성능상 유망하지만 상대적으로 덜 쓰였다.
- insertion에서는 regret 기반 best-position insertion이 강하다.
- 무작정 많은 operator를 넣기보다 검증된 기본 세트에서 시작해 하나씩 확장하는 것이 좋다.

추천 기본 세트:

```text
Removal:
1. Random customers
2. Worst cost customers
3. Related customers
4. Sequence-based removal

Insertion:
1. Highest position regret at best position
2. Random order at best position
```

성능 평가는 최종 해 품질만 보면 부족하다.

```text
- 최종 objective
- best solution 도달 시간
- 시간 대비 개선 속도
- 안정성
- operator별 ablation 영향
- 문제 유형별 성능 차이
```

## 4. 논문 기반 설계 원칙

### ALNS 설계

```text
1. Random / Worst / Related / Sequence removal로 시작
2. Regret insertion을 기본 repair로 둔다
3. best-position insertion은 유지한다
4. noise와 random order를 다양성 장치로 사용한다
5. operator 추가는 ablation으로 검증한다
```

### HGS 설계

```text
1. giant tour + split 표현을 사용한다
2. feasible/infeasible population을 분리한다
3. local search 품질에 집중한다
4. SWAP*는 강력하지만 구현 난이도를 고려해 단계적으로 추가한다
5. diversity control과 penalty management를 반드시 포함한다
```

### 실무 CVRPTW 설계

```text
1. zone/compatibility는 repair와 local search 후보 필터에 반영한다
2. 차량 부족은 optional customer 또는 dummy vehicle로 모델링한다
3. 미배송 penalty와 우선순위를 목적함수에 포함한다
4. solver 전 pre-check로 명백한 capacity 부족을 진단한다
5. 최종 결과는 route뿐 아니라 미처리 주문과 외주 필요량도 제공한다
```

## 5. 벤치마크 해석 주의점

- 논문별 실험 조건, 목적함수, 하드웨어, 시간 제한이 다르다.
- CVRP에서 좋은 operator가 VRPTW나 PDPTW에서도 항상 좋은 것은 아니다.
- 같은 이름의 operator라도 구현 세부사항이 성능을 바꾼다.
- frequency-based analysis보다 ablation study가 operator 기여도 평가에 더 직접적이다.
- benchmark 성능과 실무 운영 품질은 다를 수 있다.

## 6. 문서 전체의 결론

CVRPTW 문서군에서 일관되게 도출되는 결론은 다음이다.

```text
1. 문제 정의와 feasibility checker가 먼저 안정적이어야 한다.
2. 초기해는 단일 휴리스틱보다 portfolio 방식이 좋다.
3. ALNS는 실무 제약 확장에 강하다.
4. HGS는 표준 문제의 고품질 해 탐색에 강하다.
5. Local search는 HGS와 ALNS 모두의 핵심 성능 요소다.
6. 차량 부족과 zone 제약이 있으면 optional customer와 dummy vehicle 모델이 필요하다.
7. operator 선택은 관습보다 문헌 근거와 ablation으로 검증해야 한다.
```

