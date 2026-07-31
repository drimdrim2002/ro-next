# 세션 21 — 정책·평가·목적 조립 규범 초안

> **세션 30 통합 상태 (2026-07-23):** 세션 29에서 `Q-OBJ-01~03`, `Q-RES-01~02`, `Q-BENCH-01`이 해결되었다. 고객사별 승인 preset과 objective availability, mandatory 최상위 사전식 차원, `DIRECT/LEASE` vehicle volume objective, solver `ASSIGNED/UNASSIGNED`, required final audit와 전체 운영시간 공식은 [Master §9~§10·§14](../master-design.md) 및 [등록부](../deprecated/master-design-open-questions.md)가 현재 계약이다. 본문의 대안/TBD와 `OUTSOURCED`/`DEFERRED` solver status 방향은 역사적 초안으로만 읽는다.

> 상태: `REVIEW INPUT`
>
> 성격: 세션 25의 Master Design에 병합할 정책·평가·목적 조립 계약 초안
>
> 권위 기준: [세션 19 통합 계획](19-integration-plan.md), [세션 20 도메인·입력 초안](20-domain-input-draft.md)
>
> 편집 범위: 이 문서만 작성한다. 코드, 입력 자료, 기존 설계 문서는 변경하지 않는다.
>
> 기준일: 2026-07-23

## 1. 문서 역할과 규범 수준

이 문서는 RPDPTW (Rich Pickup and Delivery Problem with Time Windows)에서 고객사마다 다른 hard constraint, 중립 지표, 가격 정책, 목적 비교 순서와 단계 실행을 최소 코어 변경으로 조립하는 규범 경계를 정의한다.

이 문서는 구현 완료를 주장하지 않으며 Java의 최종 패키지·타입명·메서드 시그니처를 확정하지 않는다. `PropagationFacts`, `MetricContributor`, `MetricSnapshot`, `ScorePolicy`, `ObjectiveSchema`, `ObjectiveScore`, `SolvePlan`, `BoundSiteProfile` 등은 책임 경계를 설명하기 위한 **잠정 라벨**이다. 세션 19의 `P-04`부터 `P-07`까지가 승인되기 전에는 이름과 세부 API를 확정된 외부 계약으로 취급하지 않는다.

규범 표기는 다음과 같다.

| 표기 | 의미 |
|---|---|
| **확정** | 세션 19의 `C-*` 결정과 그 결정을 지키기 위해 필수인 책임 경계. `MUST` 또는 `MUST NOT`으로 해석한다. |
| **잠정** | 세션 19의 `P-*` 방향. 구조는 통합 입력으로 사용하되 이름·표현·세부 기본값은 확정하지 않는다. |
| **열린 질문** | 답에 따라 외부 의미나 최적화 계약이 달라지는 항목. 정확한 세션 19의 `TBD(Q-...)`를 유지한다. |

### 1.1 이 초안이 소유하는 내용

- 정규화된 도메인 사실에서 평가 결과까지의 단방향 책임 구조
- hard constraint, soft/monetized preference, 같은 우선순위의 scalar cost, 사전식 objective/stage의 정확한 구분
- 공통 물리 사실과 선택적 중립 지표의 확장 seam
- 비용 구성요소, 목적 차원, 비교기, `SolvePlan`의 조립 경계
- 고객사 프로필의 해석, solve별 바인딩, 버전·수명·불변성
- 새 고객 정책·제약이 들어올 때의 결정 경로와 예상 변경 범위
- `SearchRequestBank`, 최종 상태, 필수 주문, 외주·이월, 진단과 정책의 경계
- 고객사 solve objective와 `WIN_POC` benchmark comparator의 분리

### 1.2 이 초안이 소유하지 않는 내용

- raw JSON 파싱, legacy alias, 단위·시간·행렬 해석: 세션 20
- 초기해, destroy/repair, local search, acceptance, 연산자 흐름: 세션 22
- 완전한 결과 DTO, 최종 상태 확정 규칙, 독립 verifier와 benchmark card: 세션 23
- provider, product, queue, storage, runtime, deployment topology
- 코드 구현, 모듈 배치, 성능 자료구조의 최종 선택

## 2. 규범적 정책·평가 아키텍처

### 2.1 전체 책임 흐름

**확정 (`C-03`, `C-04`).** 평가 흐름은 다음 책임을 분리해야 한다.

```text
세션 20의 immutable normalized ProblemInstance
+ solve별 immutable bound profile
  │
  ├─ 구조 불변조건과 정적 compatibility gate
  │
  └─ stable route/solution state
       │
       ▼
    RoutePropagator
       │
       ├─ explicit feasible/infeasible propagation outcome
       └─ policy-neutral PropagationFacts
             │
             ├─ common Route/Solution metrics
             ├─ registered MetricContributor
             │     └─ MetricSnapshot
             └─ composed hard Route/Solution constraints
                      │
                      ├─ infeasible: score/objective 비교 대상 아님
                      └─ feasible
                            │
                            ▼
                       ScorePolicy
                            │
                            ├─ scalar costs
                            └─ explainable score breakdown
                                  │
                                  ▼
                         ObjectiveSchema/Score
                                  │
                                  ▼
                         Objective comparator
                                  │
                                  ▼
                           SolvePlan/stages
```

최종 결과와 benchmark에는 별도 흐름이 이어진다.

```text
검증된 최종 solution
  ├─ result 계층: final status와 diagnostics 생성
  └─ benchmark 계층: versioned metric projector와 comparator 적용
```

다음 책임은 서로 대체할 수 없다.

| 책임 | 결정하는 것 | 결정하지 않는 것 |
|---|---|---|
| 세션 20 정규화 | ID, 단위, 시간축, directed travel, `servableVehicles`, service meaning | 고객사 가격, 목적 순서 |
| propagator | 시간·거리·적재 등 경로 진행의 물리적 의미와 사실 | 단가, 우선순위, 최종 진단 |
| hard constraint | 후보가 허용 가능한 해인지 | 위반의 금전 가치 |
| neutral metrics/contributor | 어떤 양이 얼마나 발생했는지 | 좋은지 나쁜지, 허용할지 |
| score policy | feasible 상태의 사실에 적용할 금액·soft penalty | feasibility, stage 순서 |
| objective schema/comparator | feasible 해의 값을 어떤 차원과 방향으로 비교할지 | 탐색 단계 실행과 예산 |
| `SolvePlan` | stage 순서, warm-start, 선행 목표 보호, stage별 실행 계약 | 물리 전파 또는 가격 산식 |
| result/diagnostic | 최종 업무 상태와 근거·신뢰도 | 탐색 중 bank membership 또는 가격 |
| benchmark comparator | 특정 benchmark의 검증된 결과를 비교하는 순서 | 모든 고객사의 solve objective |

### 2.2 단방향 의존 규칙

의존 방향은 다음을 `MUST` 만족한다.

```text
input/normalization
        ↓
normalized domain facts
        ↓
propagation + hard feasibility
        ↓
neutral measurement
        ↓
scoring
        ↓
objective comparison
        ↓
solve orchestration
        ↓
result/benchmark projection
```

- 아래 계층은 위 계층의 의미를 역으로 바꾸지 않는다.
- `ScorePolicy`는 `Route`, 원시 node sequence, raw input DTO, 좌표, legacy 코드 문자열을 읽지 않는다.
- objective와 comparator는 경로를 다시 전파하거나 비용을 다시 계산하지 않는다.
- `SolvePlan`은 constraint를 끄거나 infeasible 후보를 점수로 되살리지 않는다.
- ALNS와 그 연산자는 고객사 ID, 가격 항목, objective 종류를 열거하지 않는다.
- result 계층의 status 또는 diagnostic을 탐색 중 `SearchRequestBank`에 역으로 기록하지 않는다.
- 고객사 확장 구성요소는 공통 평가 계약에 의존할 수 있지만, 공통 계약이 구체 고객사 구현을 참조해서는 안 된다.

## 3. 네 가지 정책 의미의 정확한 분리

### 3.1 Hard constraint

hard constraint는 위반한 후보를 허용 가능한 해로 인정하지 않는 규칙이다.

다음 규칙을 적용한다.

1. 위반 후보는 `ScorePolicy`, objective comparator, stage best의 정상 비교 대상이 아니다.
2. 유한 penalty, 큰 상수, 비용 할인, Simulated Annealing 수용으로 위반을 상쇄할 수 없다.
3. 세션 20의 request pair 불변조건은 구성 가능한 정책이 아니라 구조 규칙이다.
4. static request-vehicle compatibility는 정규화된 `servableVehicles`를 사용한다.
5. 경로 전체 상태가 필요한 고객사 규칙만 실행형 constraint로 둔다.
6. 물리 진행의 뜻이 달라질 때만 propagator seam을 검토한다.

hard constraint의 예는 다음과 같다.

- same vehicle, pickup/delivery 각각 exactly once, pickup-before-delivery, route/bank XOR
- 차량 용량과 승인된 hard time-window 규칙
- 차량 크기·zone·capability를 합성한 정적 호환성 결과
- 경로 최대 자원 한계
- 위험물 조합 금지처럼 경로 사실이 필요한 추가 규칙

세션 20의 request 불변조건은 프로필이 제거하거나 soft penalty로 전환할 수 없다. 필수 주문의 정확한 분류는 별도 열린 질문 `TBD(Q-OBJ-02)`이므로 이 목록에 자동 포함하지 않는다.

### 3.2 Soft 또는 monetized preference

soft preference는 위반하거나 덜 만족해도 해가 구조적·물리적으로 실행 가능하며, 고객사가 그 정도에 비용이나 불이익을 부여하는 규칙이다.

```text
PropagationFacts
→ neutral violation/usage metric
→ ScorePolicy component
→ finite scalar contribution
```

예:

- 거리비, 대기시간비, 야간 운전비
- 운영상 선호인 차량 추가 사용 비용
- soft lateness 또는 SLA 미달량
- 유한한 미배정 페널티

soft preference는 먼저 중립 위반량 또는 사용량으로 측정해야 한다. score component가 node 순서나 시간창을 다시 읽어 위반량을 독자적으로 재계산해서는 안 된다. 같은 업무 규칙을 한 프로필에서 hard rejection과 soft penalty로 중복 적용하지 않는다.

### 3.3 한 우선순위 안의 scalar cost

scalar cost는 고객사가 **서로 교환 가능하다고 명시한** 비용을 같은 목적 차원 안에서 합성한 값이다.

```text
TOTAL_POLICY_COST
= distance charge
+ vehicle activation charge
+ wait charge
+ night charge
```

다음 조건이 필요하다.

- 각 항목은 하나의 계산 scope와 안정된 breakdown key를 가진다.
- 단위와 scale이 명시되고 checked arithmetic을 사용한다.
- 같은 항목을 route, request, solution scope에서 중복 계산하지 않는다.
- 합계와 breakdown 합은 전체 재평가에서 일치한다.
- 비용 정밀도와 rounding은 `TBD(Q-NUM-01)`, `TBD(Q-NUM-02)`를 임의로 닫지 않는다.

scalar 합은 같은 우선순위 안의 명시적 trade-off다. 큰 금액을 선택했다는 사실만으로 hard rule이나 사전식 우선순위가 되지 않는다.

### 3.4 사전식 objective와 stage

서로 양보할 수 없는 우선순위는 Big-M scalar로 합치지 않고 명시적인 ordered dimensions로 비교한다.

```text
ObjectiveScore A = [a1, a2, a3]
ObjectiveScore B = [b1, b2, b3]

첫 번째로 다른 dimension의 direction에 따라 A와 B를 비교
```

objective와 stage는 다음처럼 다르다.

| 개념 | 의미 |
|---|---|
| objective dimension | 한 feasible solution에서 추출한 비교 가능한 정수값 |
| objective schema | dimension ID, slot, 단위, 방향, 출처의 불변 스키마 |
| objective score | 특정 해의 schema-aligned 값 벡터 |
| comparator | dimension의 의미적 비교 순서와 `MINIMIZE`/`MAXIMIZE` 방향 |
| solve stage | 현재 개선할 dimension/comparator, 선행 guard, warm-start, stage 실행 계약 |
| `SolvePlan` | stage의 순서와 최종 comparator를 묶는 orchestration |

벡터 slot 순서는 내부 표현일 뿐이다. 의미적 비교 순서는 versioned comparator가 소유한다. `SolvePlan`은 그 comparator와 stage 순서가 모순되지 않도록 bind 시 검증해야 한다. 별도 `ObjectivePlan`을 하나 더 만들어 같은 순서를 중복 소유하지 않는다 (`P-04`).

사전식 comparator는 두 feasible 해를 한 번에 비교하는 계약이고, 다단계 `SolvePlan`은 각 우선순위를 warm-start로 순차 개선하는 실행 계약이다. 두 계약은 같은 의미를 공유하지만 책임은 다르다.

## 4. 중립 facts와 metrics 확장

### 4.1 세션 20 facts를 소비하는 경계

세션 21의 모든 정책 구성요소는 다음 정규화 결과만 소비한다.

- immutable request/node/vehicle/location dense ID와 외부 ID 매핑
- request별 최종 `servableVehicles`
- normalized capacity, time, distance와 authoritative directed travel matrix
- node role과 delivery-only/real pickup-delivery service meaning
- explicit feasible/infeasible propagation outcome
- adapter, numeric, matrix, time policy provenance

다음을 다시 해석하지 않는다.

- raw `Feature`, zone, `ALL`, legacy alias
- coordinates 또는 vehicle speed로 travel 재생성
- raw decimal의 scale·rounding
- pickup/delivery 의미 또는 delivery-only loading 의미

### 4.2 `PropagationFacts`

**잠정 (`P-06`).** propagator는 한 번의 경로 진행 계산에서 정책 중립적인 읽기 전용 사실을 제공해야 한다.

```text
PropagationFacts
├─ common RouteMetrics
├─ leg facts
├─ visit facts
└─ request-level facts/view
```

개념적으로 필요한 사실은 다음과 같다.

| 범위 | 중립 사실 예 |
|---|---|
| leg | from/to node와 location, 출발·도착 시각, directed distance, travel time, vehicle ID |
| visit | node/request role, 도착·서비스 시작·종료, 대기, 서비스 후 적재량 |
| request | pickup/delivery의 확정된 서비스 시각, vehicle ID, 배정 여부 |
| route | 거리, drive/wait/service/elapsed time, stop count, 활성 여부, 최대 적재 |

이 목록은 최종 record 필드 목록이 아니다. 객체, 배열, event stream 중 무엇을 사용할지는 구현 선택이다. 중요한 계약은 모든 값이 같은 propagation 의미에서 나오며 정책이 원시 경로를 재해석하지 않는다는 점이다.

### 4.3 공통 metrics와 선택적 contributor

모든 고객사에서 반복 사용하는 검증된 물리 집계는 공통 route/solution metrics에 둘 수 있다. 특정 고객사나 일부 정책만 필요한 중립 사실은 `MetricContributor`가 계산한다.

```text
PropagationFacts
  → bound MetricContributor[]
  → schema-aligned MetricSnapshot
```

contributor 계약은 다음과 같다.

- 가격, penalty, feasibility 결론을 반환하지 않는다.
- leg/visit/request facts와 불변 evaluation context만 읽는다.
- 원시 route sequence나 raw input을 받지 않는다.
- 선언한 metric key와 단위만 생성한다.
- 같은 입력에는 같은 값을 반환한다.
- 필요한 propagation fact가 없으면 bind 단계에서 실패한다.
- 등록되지 않은 metric을 score/constraint/objective가 요구하면 solve 시작 전에 실패한다.

예:

| 요구 | contributor가 만드는 중립 metric | 소비자 |
|---|---|---|
| 야간 실제 운전비 | 시간대와 겹친 drive duration | score component |
| soft SLA | request별 lateness amount | score component 또는 objective |
| 특정 arc 통행료 | toll-class별 distance/usage | score component |
| 위험물 적재 중 금지 arc | 해당 상태의 arc/request fact | hard constraint |

고객사 전용 metric이 생겼다는 이유만으로 공통 `RouteMetrics` 필드를 추가하지 않는다. 충분히 반복되고 의미·단위·전파 계약이 공통화된 경우에만 별도 검토를 거쳐 공통 metric으로 승격한다.

### 4.4 Metric schema와 snapshot

metric schema는 최소한 다음 메타데이터를 가진다.

```text
metric ID
unit
scope
aggregation rule
slot
schema ID/version
```

snapshot은 schema에 맞춘 읽기 전용 정수값이다.

- 서로 다른 schema의 snapshot을 합치지 않는다.
- 문자열 key 해석은 바인딩과 결과 설명 경계에서 끝낸다.
- 반복 평가 내부에서는 bind 시 확정된 slot을 사용할 수 있다.
- metric ID의 존재만으로 그 metric이 hard인지 soft인지 결정되지 않는다. 실제 소비자가 책임을 정한다.

## 5. Hard constraint 구성

### 5.1 가장 좁은 표현 선택

새 규칙은 필요한 정보 범위에 따라 다음 순서로 배치한다.

| 질문 | 배치 |
|---|---|
| 입력 자체가 모순되거나 지원되지 않는가? | input/normalization validation |
| request와 vehicle의 정적 사실만으로 결정 가능한가? | transformer가 `servableVehicles`로 컴파일 |
| 기존 propagation facts/metrics로 경로의 허용 여부를 결정할 수 있는가? | bound route/solution constraint |
| 시간·적재·회차의 진행 의미 자체가 달라지는가? | 좁은 propagator seam 확장 검토 |

constraint는 가장 작은 입력 의존성을 선언한다. 저렴한 정적·집계 검사는 앞에서 단락할 수 있지만, 진단 모드가 모든 violation을 수집할지는 세션 23의 별도 계약이다.

### 5.2 구조 불변조건의 비정책성

다음 규칙은 고객사 프로필의 선택 항목이 아니다.

- request pair completeness
- pickup/delivery exactly once
- same vehicle
- pickup-before-delivery
- route 또는 `SearchRequestBank` 중 정확히 한 곳
- 성공한 mutation의 전체 반영과 실패 시 전체 복원
- 고정 terminal과 정규화된 service meaning

고객사 정책은 이를 완화하거나 비용으로 바꿀 수 없다. 새로운 문제 변형이 이 규칙을 바꾼다면 일반 policy 추가가 아니라 세션 19의 별도 범위·feasibility 결정이 필요하다.

### 5.3 차량 크기 `Feature`와 일반 capability

**확정 (`C-05`).** 입력의 `Feature`는 지역의 대형 차량 진입 제한에 사용하는 차량 크기 유형이다.

```text
size compatibility
= vehicle.vehicleSizeType
  IN request.allowedVehicleSizeTypes
```

- request 측 값은 허용 가능한 차량 크기 유형의 OR 집합이다.
- vehicle 측 값은 하나의 차량 크기 유형이다.
- 코드의 숫자 크기나 문자열 순서를 solver가 추론하지 않는다.

냉장, lift, 위험물 자격, 기사 qualification은 별도 축이다.

```text
capability compatibility
= request.requiredCapabilities
  SUBSET OF
  vehicle.vehicleCapabilities
```

size membership과 capability subset을 generic feature 교집합 하나로 합치지 않는다. 세션 21은 raw 입력을 해석하지 않고 세션 20이 만든 `servableVehicles`만 평가 hot path에서 소비한다. 새 capability가 추가될 때도 먼저 정적 compatibility rule로 컴파일하고, 경로 상태가 필요할 때만 별도 constraint를 추가한다.

## 6. Score policy와 비용 소유권

### 6.1 합성 구조

`ScorePolicy`는 하나의 거대한 고객사별 클래스보다 작은 component의 불변 합성으로 구성하는 방향을 `SHOULD` 따른다.

```text
Bound ScorePolicy
├─ route-scope components
├─ assigned-request-scope components
├─ search-unassigned-scope components
└─ solution-scope components
```

각 component는 다음을 선언한다.

- component ID/version
- 입력 metric/fact 의존성
- 계산 scope
- 출력 score dimension
- 출력 단위·scale
- 고유 breakdown key

한 비용 항목에는 소유 scope가 하나만 있다.

| scope | 예 |
|---|---|
| route | route distance, route activation, drive/wait charge |
| assigned request | 서비스 시간대, request별 soft SLA |
| search-unassigned request | 현재 bank membership에 대한 유한 penalty |
| solution | fleet-level 구간요금, 전체 서비스율 조정 |

`SearchRequestBank`를 읽는 component는 request ID membership만 읽는다. 비용, last rejection, final status, diagnostic을 bank에 쓰지 않는다.

### 6.2 순수성·산술·설명 가능성

score component와 합성 정책은 다음을 `MUST` 만족한다.

- immutable facts/context에 대한 결정적 순수 함수
- checked integer arithmetic
- 동일 dimension에 대한 단위·scale 일치
- 구성요소 등록 순서와 무관한 합계
- duplicate breakdown key의 bind-time 거부
- 전체 평가에서 재구성 가능한 breakdown
- incremental/cached 평가와 cache-free full evaluation의 동등성

비가산 solution-level component seam은 허용할 수 있지만, 존재하지 않는 고객사 요구를 기본 활성화하지 않는다. 사용 시에도 partial evaluation과 full evaluation의 같은 산식이 보장되어야 한다.

## 7. Objective schema, comparator, `SolvePlan`

### 7.1 Objective dimension과 score

objective dimension은 중립 metric 또는 score breakdown에서 설명 가능한 정수값을 추출한다.

개념적 정의는 다음 정보를 가진다.

```text
objective dimension
├─ stable ID/version
├─ source metric or score key
├─ unit
├─ MINIMIZE or MAXIMIZE
└─ value extraction rule
```

최대화 차원을 음수로 바꾸어 overflow나 결과 해석 문제를 만들지 않는다. 방향은 comparator가 명시적으로 해석한다.

`ObjectiveSchema`와 `ObjectiveScore`는 잠정 라벨이다 (`P-04`). 핵심 계약은 다음과 같다.

- schema는 사용 가능한 dimension과 score slot을 고정한다.
- score는 정확히 하나의 schema를 참조한다.
- 알 수 없는 dimension, 잘못된 단위, source 누락은 bind 시 실패한다.
- objective value를 얻기 위해 route나 raw input을 다시 읽지 않는다.
- 최종 결과는 모든 dimension과 그 단위·출처를 설명할 수 있어야 한다.

### 7.2 Comparator

comparator는 다음을 소유한다.

- 비교할 dimension의 ordered ID 목록
- 각 dimension의 방향
- 정확 비교 규칙
- 모든 품질 dimension이 같은 경우의 품질상 동률

tie-break를 위해 고객사 비용을 숨겨 추가하지 않는다. 완전히 같은 objective score에서 실행이 하나의 해를 선택해야 한다면 안정된 solution fingerprint 등 별도의 결정적 구조 tie-break를 사용한다. 이 tie-break는 objective vector의 새 차원이 아니다.

### 7.3 `SolvePlan`과 stage

**잠정 (`P-04`).** `SolvePlan`은 단계 실행의 상위 orchestration 이름으로 사용한다. 별도의 `ObjectivePlan`을 같은 책임으로 병치하지 않는다.

각 stage는 개념적으로 다음을 참조한다.

```text
stage ID
active objective dimension/comparator
previous-objective guard
warm-start source
stage strategy reference
stage budget reference
```

이 문서는 strategy 내부와 ALNS operator 흐름을 설계하지 않는다. stage가 요구하는 평가 계약만 정의한다.

`SolvePlan` bind-time 검증은 최소한 다음을 포함한다.

1. stage가 하나 이상 존재한다.
2. stage ID가 중복되지 않는다.
3. 모든 objective, comparator, strategy, guard, budget reference가 해석된다.
4. stage의 objective가 같은 plan의 objective schema에 존재한다.
5. 최종 comparator와 stage의 선행 보호 순서가 모순되지 않는다.
6. 빈 plan이나 unknown ID에 묵시적 fallback을 적용하지 않는다.
7. stage `n`의 시작 solution은 stage `n-1`의 종료 best를 warm-start로 사용한다.

### 7.4 선행 objective 보호

**잠정 (`P-07`).** 기본 보호 방향은 `NO_WORSE_THAN_BEST`다.

```text
stage 진입
→ 앞선 stage에서 발견한 best objective prefix를 guard 기준으로 고정
→ prefix가 개선된 후보는 허용하고 새 기준으로 갱신 가능
→ prefix가 같은 후보에서 현재 stage objective를 비교
→ prefix가 악화된 후보는 하위 objective 개선만으로 stage best가 될 수 없음
```

보호 기준은 수학적으로 증명된 전역 최적값이 아니라 해당 실행에서 발견한 best다. 묵시적 tolerance는 없다. tolerance 또는 탐색 중 relaxation의 허용 여부와 세부 계약은 `P-07`의 미승인 부분으로 남기며 이 초안에서 기본값을 만들지 않는다.

후속 stage가 개선하지 못하거나 실패하면 직전 검증된 best를 보존해야 한다. stage 실행의 구체 rollback과 acceptance는 세션 22가 소유한다.

## 8. 고객사별 profile 조립과 lifecycle

### 8.1 정의, 바인딩, 실행 상태의 분리

**잠정 (`P-05`).** 장기 수명의 registry/provider는 설정과 factory만 보유하고, solve별 데이터는 정규화된 문제에 바인딩해 별도 불변 profile을 만든다.

```text
장기 수명
ProfileRegistry
└─ immutable profile/provider definitions

한 solve 수명
normalized ProblemInstance
+ exact profile key/version/config
→ bind and validate
→ immutable BoundSiteProfile
   ├─ EvaluationContext
   ├─ propagator selection
   ├─ bound constraints
   ├─ metric schema/contributors
   ├─ bound ScorePolicy
   ├─ ObjectiveSchema/comparator
   └─ SolvePlan

한 route 평가 수명
contributor accumulator / propagation scratch

한 search run·seed 수명
route cache / adaptive state / telemetry
```

최종 이름, provider API와 등록 방식은 확정하지 않는다. 수명과 격리 계약은 다음을 `MUST` 만족한다.

- registry는 등록 후 불변이며 문제별 배열·mutable cache를 보유하지 않는다.
- provider는 무상태 factory 또는 불변 설정이다.
- `requestId`/`vehicleId` indexed 정책 배열은 한 `ProblemInstance`에만 바인딩된다.
- bound profile과 evaluation context는 생성 후 불변이다.
- contributor accumulator는 평가별로 격리한다.
- route cache와 search telemetry는 seed/run 사이에 공유하지 않는다.
- 같은 immutable problem과 bound profile을 병렬 실행이 공유하더라도 mutable 상태는 공유하지 않는다.

### 8.2 Binding lifecycle

한 solve의 정책 lifecycle은 다음 순서를 따른다.

```text
1. exact profile key/version resolve
2. 세션 20 계약에 따라 input normalize
3. normalized problem fingerprint 확정
4. profile config를 dense ID와 normalized units에 bind
5. propagator가 제공하는 fact contract 확인
6. contributor → metric schema 의존성 확인
7. constraint의 fact/metric 의존성 확인
8. score component의 metric, scope, unit, key 확인
9. objective source와 direction 확인
10. comparator와 SolvePlan consistency 확인
11. immutable bound profile freeze
12. solve 실행
13. cache-free final full evaluation과 metadata 전달
```

미등록 key/version, 누락 metric, 중복 score key, unit 불일치, unknown objective/stage reference는 탐색 시작 전에 명확한 구성 오류로 거부한다. 실행 중 `latest`로 자동 교체하거나 다른 프로필로 fallback하지 않는다.

### 8.3 Profile binding과 versioning

재현 가능한 실행에는 최소한 다음 식별 정보가 필요하다.

```text
problem fingerprint
profile key + exact version
profile config hash
propagator/fact contract version
constraint IDs/versions
metric schema/contributor versions
score policy/component versions
objective schema/comparator versions
solve plan ID/version
numeric and matrix policy provenance
```

설정·단가·objective 순서·guard가 바뀌어 평가 의미가 달라지면 최소한 config hash와 실행 fingerprint가 달라져야 한다. semantic version 증가 규칙의 최종 형식은 이 문서에서 확정하지 않지만, 같은 fingerprint가 다른 의미를 가리키는 것은 허용하지 않는다.

동일 고객사에서 solve 요청이 objective preset을 선택할 수 있는지는 `TBD(Q-OBJ-01)`이다. 답이 나올 때까지 요청이 임의 plan ID를 주입하거나 registry가 알 수 없는 preset을 기본값으로 치환해서는 안 된다.

## 9. 새 고객사 정책·제약의 결정 경로

### 9.1 Decision path

새 요구는 다음 순서로 분류한다.

```text
새 고객사 요구
  │
  ├─ raw 입력의 의미·단위·compatibility 해석인가?
  │    └─ 세션 20 transformer/normalization 경계
  │
  ├─ 기존 normalized facts로 항상 금지 여부를 판단할 수 있는가?
  │    ├─ request × vehicle 정적 판단
  │    │    └─ compatibility rule → servableVehicles
  │    └─ route/solution 상태 판단
  │         └─ bound hard constraint
  │
  ├─ feasible 해에서 필요한 중립 측정값이 이미 있는가?
  │    ├─ 예
  │    │    ├─ 가격/soft penalty → score component
  │    │    └─ 새 비교 기준 → objective dimension/comparator
  │    └─ 아니오
  │         ├─ 기존 PropagationFacts에서 파생 가능
  │         │    └─ MetricContributor
  │         └─ 기존 facts로는 물리 의미를 정확히 알 수 없음
  │              └─ narrow fact/propagator seam 검토
  │
  ├─ 기존 objective의 순서만 다른가?
  │    └─ comparator/SolvePlan 구성 변경
  │
  └─ 시간·적재·회차 진행의 물리 의미 자체가 다른가?
       └─ propagator contract 확장 검토
```

### 9.2 코어 변경이 정당한 조건

새 고객사라는 이유만으로 코어를 수정하지 않는다. 좁은 공통 seam 변경은 다음 조건을 모두 만족할 때만 정당하다.

1. 기존 정규화 데이터, propagation facts, metric contributor, constraint, score, objective, plan 조합으로 의미를 정확히 표현할 수 없다.
2. 부족한 것이 단가나 우선순위가 아니라 실제 물리 상태 또는 전파 의미다.
3. 필요한 새 fact의 단위, lifecycle, feasibility 관계가 명확하다.
4. 기본 propagator와 기존 고객사의 의미를 바꾸지 않는 호환 경계가 있다.
5. 전체 재계산, 기존 프로필 회귀, independent verification으로 새 의미를 검증할 수 있다.

예를 들어 야간 요금은 기존 leg 시간에서 파생할 수 있으므로 contributor와 score component의 변경이다. 반면 승인된 multi-trip 의미가 적재 reset과 depot 재방문을 실제로 바꾼다면 propagator seam 검토가 필요하다. 단, multi-trip의 실제 계약은 이 문서에서 결정하지 않는다.

### 9.3 변경 영향 표

| 변경 요구 | 변경해야 하는 곳 | 변경하지 않는 곳 | bind/검증 gate |
|---|---|---|---|
| 새 price term | 기존 metric이면 score component+profile config. metric이 없지만 facts에서 파생 가능하면 contributor도 추가 | request/route 구조, propagator, ALNS, comparator의 다른 차원 | unit/scale, scope, unique breakdown key, overflow, full-evaluation 동등성 |
| 새 hard constraint | 정적이면 normalization compatibility rule, 경로 상태면 bound constraint | ScorePolicy, objective 순서, ALNS 고객사 분기 | 필요한 fact/metric 존재, structured violation, penalty로 우회 불가 |
| 새 neutral metric | 기존 facts를 소비하는 contributor와 metric schema | 가격 정책, hard/soft 판정, route core field | stable metric ID/unit/scope, deterministic aggregation, schema compatibility |
| 새 objective order | versioned comparator와 `SolvePlan` preset | propagator, metric 산식, score component, ALNS core | dimension 존재, 방향, stage/guard 일관성, 결과 metadata |
| 새 customer profile | provider definition, 승인된 transformer/config, constraint/contributor/score/objective/plan 조립과 registry 등록 | 공통 ALNS, 공통 route 상태, 기존 profile | exact version/config hash, dependency closure, 기존 profile regression |
| 새 physical propagation meaning | 필요한 최소 propagator/fact contract 구현과 해당 profile binding | 다른 profile의 기본 propagator, price/comparator 일반 계약 | 세션 20 불변조건 보존, fact semantics, full recomputation, verifier handoff |

이 표에서 “새 physical propagation meaning”만이 평가 코어의 좁은 seam 변경을 정당화할 수 있다. 새 price term, objective order 또는 고객 이름 자체는 정당한 코어 변경 사유가 아니다.

## 10. Unassigned, mandatory, outsourced/deferred, diagnostics 경계

### 10.1 `SearchRequestBank`

**확정 (`C-15`).** `SearchRequestBank`는 탐색 중 정규 route에 속하지 않는 request ID membership만 보유한다.

- score policy는 현재 미배정 건수나 request별 finite penalty를 계산하기 위해 membership을 읽을 수 있다.
- objective는 bank에서 파생된 중립 count 또는 policy penalty를 비교할 수 있다.
- bank에는 비용, 목표값, mandatory flag의 정책 해석, last failure, final status, diagnostic을 저장하지 않는다.
- 한 insertion 실패는 최종 unassignment 원인이 아니다.
- 결과 변환은 bank를 그대로 최종 DTO나 단일 reason으로 노출하지 않는다.

### 10.2 Mandatory order

필수 주문은 `TBD(Q-OBJ-02)`이며 이 초안은 다음 대안 중 하나를 선택하지 않는다.

| 가능한 승인 의미 | 평가상 정확한 의미 |
|---|---|
| hard rule | 필수 request가 최종 정규 배정되지 않은 해는 admissible final solution이 아님. 유한 penalty로 복구 불가 |
| finite soft preference | 미배정이 허용되며 명시적 finite penalty가 다른 비용과 같은 scalar 차원에서 trade-off됨 |
| 상위 lexicographic objective | mandatory-unassigned count를 더 낮은 목표보다 우선하지만, 0이 불가능할 때도 최선값은 존재할 수 있으므로 hard guarantee와 같지 않음 |

결정 전에는 “매우 큰 penalty”를 hard rule로 간주하거나, 상위 objective가 0 배정을 보장한다고 서술하지 않는다. 구조적 request pair 불변조건은 이 질문과 관계없이 항상 hard다.

### 10.3 `OUTSOURCED`와 `DEFERRED`

최종 상태를 `ASSIGNED`, `UNASSIGNED`, `DEFERRED`, `OUTSOURCED`로 구분하는 방향은 잠정이다 (`P-08`). 다음 결정은 열려 있다.

- 결과 분류만 할지, search-time 대체 선택지로 최적화할지: `TBD(Q-OBJ-03)`
- `OUTSOURCED`와 `DEFERRED` 확정에 필요한 최소 정보: `TBD(Q-RES-01)`

`Q-OBJ-03`이 결과 분류 모드로 결정되면 외주·이월은 solve objective와 search state에 들어가지 않고 세션 23의 final disposition 책임이 된다.

최적화 결정 모드로 승인되면 다음 계약이 추가로 필요하다.

- `SearchRequestBank`와 분리된 명시적 fallback assignment state
- regular assignment와 fallback을 중복하지 않는 request partition
- 상태별 중립 metrics와 비용 component
- objective에서 regular fleet service와 fallback service를 구분하는 명시적 dimension

이 계약이 승인되기 전에는 dummy 하나에 미배송·외주·이월 의미를 합치거나, bank membership에서 `OUTSOURCED`/`DEFERRED`를 추론하지 않는다.

### 10.4 Diagnostics

진단을 code, scope, confidence, source, evidence로 구조화하는 방향은 잠정이다 (`P-09`). 공개 code 목록, 고객사 확장 코드 노출, final audit 범위는 세션 23이 소유한다.

정책·제약 계층은 다음 증거만 넘길 수 있다.

- normalization precheck의 정적 사실
- candidate evaluation의 구조화된 constraint violation
- 별도 bounded search telemetry
- cache-free final audit에 사용할 metric/fact

이 증거가 자동으로 최종 원인이 되지는 않는다. 최종 해 전체 삽입 감사를 모든 미배정 요청에 수행할지는 `TBD(Q-RES-02)`다. audit 없이 `PROVEN` 또는 `EXHAUSTIVE_FOR_FINAL_SOLUTION`을 생성하지 않는다.

## 11. 고객사 solve objective와 `WIN_POC` benchmark 분리

### 11.1 두 비교 lane

**확정 (`C-18`).** Win PoC의 사전식 순서는 특정 benchmark profile의 comparator이며 모든 고객사의 전역 목적이 아니다.

```text
customer solve lane

Bound customer profile
→ customer ObjectiveSchema/comparator
→ customer SolvePlan
→ customer-selected best
```

```text
WIN_POC benchmark lane

independently verified final solution
→ WIN_POC versioned metric projection
→ WIN_POC comparator
→ benchmark champion/regression result
```

공통 evaluator가 중립 facts를 제공할 수는 있지만 다음을 금지한다.

- 공통 `Solution` 또는 ALNS에 Win PoC 순서를 하드코딩
- 모든 customer `SolvePlan`의 기본 순서를 Win PoC로 지정
- benchmark comparator의 값을 customer price policy로 변환
- customer objective 결과를 검증 없이 benchmark 값으로 재사용

benchmark 실행이 같은 순서를 solve objective로도 사용하려면 명시적인 benchmark 전용 profile로 바인딩해야 한다. 그 선택은 다른 고객사 profile에 영향을 주지 않는다.

### 11.2 확정된 순서와 미확정 산식

Win PoC comparator의 확정 순서는 다음과 같다.

```text
1. 미배정 주문 수
2. 배차 차량 수
3. 전체 거리
4. 전체 시간
```

모든 성분은 같은 benchmark manifest와 independent verifier가 만든 값이어야 한다. 첫 번째로 다른 성분에서 더 작은 값을 우선하는 사전식 비교이며 Big-M으로 바꾸지 않는다.

네 번째 `전체 시간`의 공식은 `TBD(Q-BENCH-01)`이다. 순수 주행시간, 대기·서비스를 포함한 route elapsed time 중 하나를 이 초안에서 선택하지 않는다.

- 공통 `driveTime`을 네 번째 값으로 자동 연결하지 않는다.
- `totalTime`이라는 이름만 보고 산식을 추정하지 않는다.
- 답이 정해지기 전에는 benchmark schema/manifest가 formula ID 없이 공식 baseline 값을 발행해서는 안 된다.

세션 23은 네 성분의 정확한 projector, verifier, card metadata를 소유한다. 이 문서는 순서와 고객사 objective로부터의 격리만 확정한다.

## 12. 계약·lifecycle 검증 항목

후속 구현 설계는 최소한 다음 검증 계약을 가져야 한다.

### 12.1 Feasibility와 policy 격리

- 같은 normalized problem과 route에 다른 score policy를 적용해도 hard feasibility는 같다.
- 같은 score policy에 hard constraint를 추가해도 기존 비용 산식 자체는 바뀌지 않는다.
- infeasible candidate에 가격을 계산해 feasible로 승격하지 않는다.
- 세션 20의 request invariant를 profile 설정으로 비활성화할 수 없다.

### 12.2 Facts와 contributor

- contributor의 출력은 cache-free propagation facts에서 재현된다.
- contributor 등록 순서를 바꿔도 같은 metric snapshot을 얻는다.
- 없는 metric, 잘못된 unit, 다른 schema snapshot을 bind 단계에서 거부한다.
- contributor가 raw route sequence, raw input, 고객사 mutable state에 접근하지 않는다.

### 12.3 Score와 breakdown

- 각 비용은 정확히 한 scope에서 한 번 계산한다.
- breakdown 합과 scalar score dimension이 일치한다.
- 구성요소 순서를 바꿔도 합계가 같다.
- overflow가 큰 값으로 saturate되지 않고 명시적 계산 실패가 된다.
- incremental/cached score와 전체 재평가 score가 같다.

### 12.4 Objective와 stages

- 손으로 비교 가능한 score vector에서 comparator의 ordered dimension과 방향을 검증한다.
- scalar cost 변경은 다른 lexicographic dimension의 순서를 바꾸지 않는다.
- stage `n`은 stage `n-1`의 검증된 best와 구조적으로 같은 warm-start를 받는다.
- `NO_WORSE_THAN_BEST`인 선행 prefix가 후속 stage 종료에서 악화되지 않는다.
- 후속 stage 실패 시 직전 best가 보존된다.
- 최종 objective score, comparator version, stage별 종료값이 일치한다.

### 12.5 Profile 격리·재현성

- 새 고객 profile을 등록해도 공통 ALNS와 기존 profile을 수정하지 않는다.
- 두 problem의 request-indexed 정책 배열이 섞이지 않는다.
- 같은 immutable bound profile의 병렬 seed가 동일 route에 동일 평가를 만든다.
- 이미 바인딩된 profile은 registry/config 변경의 영향을 받지 않는다.
- 결과 metadata만으로 exact profile/config/schema/comparator/plan을 식별할 수 있다.

## 13. 추적성

### 13.1 세션 19 확정 결정

| 결정 | 이 초안의 반영 |
|---|---|
| `C-03` | §2, §8~§9에서 고객사별 조립과 최소 코어 변경 decision path를 확정 |
| `C-04` | §2~§7에서 hard feasibility, neutral measurement, score, comparison, stage orchestration을 분리 |
| `C-05` | §5.3에서 차량 크기 membership과 capability/qualification subset을 분리 |
| `C-15` | §10에서 `SearchRequestBank`를 membership-only search state로 제한하고 final status/diagnostic과 분리 |
| `C-18` | §11에서 Win PoC 순서를 benchmark comparator로만 두고 customer objective와 분리 |

### 13.2 세션 19 잠정안

| 잠정안 | 이 초안의 반영 | 계속 잠정인 부분 |
|---|---|---|
| `P-04` | `SolvePlan`은 orchestration, `ObjectiveSchema/Score`는 evaluation vector, comparator는 의미적 순서를 소유 | 최종 타입명과 API |
| `P-05` | registry/provider definition과 solve별 immutable binding을 분리 | provider API, 등록 방식 |
| `P-06` | `PropagationFacts` → `MetricContributor` → `MetricSnapshot` 확장 seam | 객체/배열 표현, 공통 metric 승격 목록 |
| `P-07` | 선행 objective 기본 보호 방향을 `NO_WORSE_THAN_BEST`로 기술 | tolerance와 search relaxation 허용 여부 |
| `P-08` | final status의 정책 경계만 보존 | 상태별 최소 정보와 첫 구현 활성 범위 |
| `P-09` | structured diagnostic evidence의 handoff만 정의 | 공개 code, 확장 코드 노출, audit 범위 |

### 13.3 관련 근거

| 근거 | 사용한 내용 |
|---|---|
| [세션 04](04-objective-policy-flexibility.md) | score/objective/stage 분리, Big-M 회피, customer-specific plan, warm-start |
| [세션 05](05-vehicle-size-and-constraints.md) | size membership, capability subset, compatibility/constraint/propagator decision |
| [세션 13](13-unassigned-status-and-diagnostics.md) | search bank, final status, diagnostics, fallback option의 책임 분리 |
| [세션 14](14-policy-metrics-extension.md) | propagation facts, metric contributor, component score, profile lifecycle |
| [세션 16](16-win-poc-benchmark.md) | Win PoC가 전역 customer objective가 아닌 benchmark comparator라는 경계 |
| [세션 20](20-domain-input-draft.md) | normalized facts, request invariants, authoritative matrix, explicit infeasible, search bank |
| [문제 정의](../arranged/01_problem_definition.md) | hard feasibility, scalar와 lexicographic comparison의 연구 배경 |
| [실무 확장](../arranged/06_practical_extensions.md) | optional service, fallback, compatibility precheck의 가능성; dummy 의미는 세션 13/19 경계로 제한 |

## 14. 열린 질문

이 문서는 세션 19의 중앙 질문 ID만 사용하며 답을 만들지 않는다.

| ID | 이 초안에서 열린 의미 | 결정 전 안전한 처리 |
|---|---|---|
| `Q-NUM-01` | 비용을 포함한 차원별 유지 소수 자릿수 | score scale을 profile에 명시하지 못하면 bind 금지 |
| `Q-NUM-02` | 비용을 포함한 차원별 rounding mode | 임의 floor/round 금지 |
| `Q-OBJ-01` | solve 요청이 고객사 내 objective preset을 선택할 수 있는지 | 임의 preset 주입이나 unknown fallback 금지 |
| `Q-OBJ-02` | mandatory order가 hard, finite penalty, 상위 objective 중 무엇인지 | 세 의미를 혼합하거나 큰 penalty로 hard를 흉내 내지 않음 |
| `Q-OBJ-03` | 외주·이월이 결과 분류인지 최적화 선택지인지 | fallback search state와 비용을 임의 활성화하지 않음 |
| `Q-RES-01` | `OUTSOURCED`/`DEFERRED` 확정 최소 정보 | bank나 diagnostic에서 final status 추론 금지 |
| `Q-RES-02` | 모든 미배정 요청의 final exhaustive insertion audit 여부 | audit 없이 exhaustive confidence 생성 금지 |
| `Q-BENCH-01` | Win PoC `전체 시간`의 정확한 공식 | `driveTime` 또는 elapsed time을 자동 연결하지 않음 |

입력·호환성·시간·행렬의 다른 `Q-*`는 세션 20에 남아 있다. 정책 계층은 그 답이 반영된 normalized facts를 소비할 뿐 자체 해석으로 질문을 닫지 않는다.

## 15. 후속 세션 handoff

### 15.1 세션 22 — algorithm draft

세션 22는 다음 계약을 소비한다.

- candidate는 세션 20의 안정 상태와 request 불변조건을 만족한다.
- evaluator는 explicit feasibility, neutral metrics, score breakdown, objective score를 분리해 반환한다.
- infeasible candidate는 price/objective로 복구되지 않는다.
- 모든 candidate/current/best 비교는 현재 stage의 comparator와 선행 guard를 사용한다.
- stage는 직전 best를 warm-start로 받고 `P-07` 보호 계약을 지킨다.
- cached/incremental 값은 cache-free full evaluation과 같아야 한다.

세션 22는 이 계약 위에서 operator, acceptance, rollback, step/watchdog을 설계하되 고객사별 objective 분기나 Win PoC 전역 순서를 엔진에 추가하지 않는다.

### 15.2 세션 23 — result/benchmark draft

세션 23은 다음을 입력으로 받는다.

- final cache-free metrics, score breakdown, objective score
- profile/config/constraint/metric/score/comparator/plan의 exact fingerprint
- 검증된 final route와 `SearchRequestBank` 파티션
- normalization facts와 structured constraint evidence
- Win PoC의 확정된 네 성분 순서와 `TBD(Q-BENCH-01)`

세션 23이 소유하는 내용은 다음과 같다.

- `P-08`, `P-09`의 final status·diagnostic 상세
- `Q-RES-01`, `Q-RES-02`가 남은 상태를 드러내는 결과 계약
- independent verifier와 metric projector
- Win PoC formula ID, manifest/card, regression 판정

세션 23은 bank를 final policy-state 저장소로 사용하거나 `Q-BENCH-01`의 산식을 발명하지 않는다.

### 15.3 세션 24 — roadmap/context draft

세션 24는 이 문서의 binding dependency와 gate를 구현 Phase에 배치한다.

```text
normalized domain/input
→ bound policy/evaluation
→ algorithm
→ result/verifier
→ benchmark
```

프로필·지표·score·objective를 하나의 동시 구현 묶음으로 숨기지 않고, 각 단계의 산출물과 완료 gate를 분리한다. provider/product/deployment topology는 추가하지 않는다.

### 15.4 세션 25 — Master 통합

세션 25는 다음 원칙으로 이 초안을 통합한다.

- `SolvePlan`/`ObjectivePlan` 중복은 `P-04`에 따라 제거한다.
- 잠정 라벨을 최종 타입명으로 과도하게 확정하지 않는다.
- `C-03`, `C-04`, `C-05`, `C-15`, `C-18`을 규범 문장으로 유지한다.
- `P-04`~`P-09`는 잠정 상태를 표시한다.
- 이 문서의 `Q-*`를 중앙 질문 파일의 같은 ID에 연결한다.
- Win PoC comparator를 고객사 전역 objective로 승격하지 않는다.

## 16. 범위 self-audit

| 검사 | 결과 |
|---|---|
| 수정 대상 | `docs/master-design-sessions/21-policy-objective-draft.md`만 생성 |
| 표준 문제명 | RPDPTW 사용, 학술 `PDPTW`와 혼동 없음 |
| 정규화 경계 | 세션 20 facts를 소비하며 raw input을 재해석하지 않음 |
| 책임 분리 | hard constraint, neutral metric, score, objective comparator, `SolvePlan`을 구분 |
| 고객사 확장 | profile binding과 decision path로 최소 코어 변경을 설명 |
| `Feature` | vehicle size membership으로 한정하고 capability/qualification subset과 분리 |
| search/result | `SearchRequestBank`를 membership-only로 유지 |
| mandatory/fallback | `Q-OBJ-02`, `Q-OBJ-03`, `Q-RES-01`을 닫지 않음 |
| diagnostics | `P-09`, `Q-RES-02`의 세션 23 소유권 유지 |
| benchmark | Win PoC를 별도 comparator로 두고 `Q-BENCH-01` 산식을 만들지 않음 |
| algorithm | stage가 소비할 평가 계약만 정의하고 ALNS operator 흐름은 설계하지 않음 |
| 결과 | handoff만 정의하고 완전한 result DTO를 설계하지 않음 |
| topology | provider/product/deployment 결정을 포함하지 않음 |
| 구현 주장 | 코드·모듈·API가 완료되었다고 주장하지 않으며 타입명을 잠정으로 표시 |

이 초안은 세션 25가 Master의 변화 수용 아키텍처를 작성할 때 사용하는 규범적 통합 입력이다. 새 고객사의 단가·제약·지표·목적 순서를 profile composition으로 추가하되, 새로운 물리 진행 의미가 필요한 경우에만 검증 가능한 좁은 공통 seam을 확장하는 것이 핵심 계약이다.
