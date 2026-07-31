---
title: RPDPTW Domain Design
status: SUPERSEDED
version: 3.1-review
last_updated: 2026-07-28
owner: RPDPTW Domain·Input·Evaluation·Result 설계 역할
scope: Master Design의 도메인, 정규화, travel preparation, 해 상태, 전파, 평가, ALNS–MIP 경계와 결과 계약의 상세화
supersedes: domain-design.md (v2.3-review)
related_designs:
  - 2026-07-26-master-design.md
  - 2026-07-26-architecture-design.md
related_decisions:
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
superseded_by: docs/domain-design.md
phase_c: path-and-status-only

---

<!-- phase-c-authority-banner -->
> **SUPERSEDED (Phase C)** — current authority: [`docs/domain-design.md`](../domain-design.md). This file is historical only. Do not use as conflict authority.


# RPDPTW Domain Design

## 상세 목차

- [1. 문서 지위와 읽는 법](#document-status)
- [2. CVRPTW 개발자를 위한 RPDPTW 입문](#primer)
  - [2.1 CVRPTW와 RPDPTW의 차이](#cvrptw-vs-rpdptw)
  - [2.2 Order, Request, Node, Visit, Route](#core-vocabulary)
  - [2.3 Delivery-only와 real pickup-delivery](#service-patterns)
  - [2.4 네 가지 핵심 불변조건과 route-bank XOR](#core-invariants)
- [3. 전체 처리 흐름과 책임 경계](#pipeline)
- [4. 입력 계약](#input)
  - [4.1 Plan envelope](#plan-envelope)
  - [4.2 Order, request와 item](#order-request-item)
  - [4.3 Vehicle](#vehicle-input)
  - [4.4 Depot와 trip option](#depot-trip)
- [5. 정규화](#normalization)
  - [5.1 수치와 단위](#numeric-normalization)
  - [5.2 시간축과 service window](#time-normalization)
  - [5.3 Size, capability와 zone](#compatibility-normalization)
- [6. Travel preparation](#travel)
- [7. Immutable solver model](#immutable-model)
- [8. Stable solution, request bank와 COW](#stable-cow)
- [9. Route propagation과 resource](#propagation)
- [10. Evaluation, profile과 objective](#evaluation)
- [11. ALNS state와 operator](#alns)
- [12. Evaluated route artifact와 route pool](#route-pool)
- [13. MIP projection과 exact partition](#mip)
- [14. Hybrid phase와 typed fallback](#hybrid)
- [15. Verification, finalization과 result](#verification-result)
- [16. 오류와 종료 모델](#errors-termination)
- [17. Acceptance evidence](#acceptance-evidence)
- [18. Gate, deferred boundary와 추적성](#gates-traceability)
- [19. 처음부터 끝까지 보는 작은 예](#end-to-end-example)
- [20. 새 customer extension 절차](#customer-extension)
- [21. 핵심 요약](#summary)

<a id="document-status"></a>
## 1. 문서 지위와 읽는 법

이 문서는 [2026-07-26 Master Design](2026-07-26-master-design.md)을 상세화하는 `REVIEW` 상태의 Domain Design이다. 구현 완료 보고가 아니며 Master보다 높은 conflict authority를 주장하지 않는다. [2026-07-26 Architecture Design](2026-07-26-architecture-design.md)은 여기서 정의한 의미를 Java 25/Maven module과 package에 배치한다.

권위 순서는 Master §1을 따른다.

1. 채택된 외부 입력·출력 계약과 승인된 Decision Record
2. `APPROVED` Master
3. `APPROVED` 상세 설계
4. 현재 `REVIEW` 문서와 `master-design-sessions`
5. 연구·역사 자료

질문 결정의 유일한 사용자 답변 근거는 [세션 29](master-design-sessions/29-open-question-interview.md)다. [질문 등록부](master-design-open-questions.md)는 28개 질문의 상태·결정·evidence·gate를 제공하고, [세션 30](master-design-sessions/30-open-question-integration.md)은 Master 반영 기록, [세션 31](master-design-sessions/31-domain-design-integration.md)은 Domain Design 반영 기록이다. 이 링크들은 역사적 evidence이므로 새 형제 문서 링크로 바꾸지 않는다.

이 문서가 domain 의미와 계층 책임을 소유하고 Architecture Design이 Maven/package 배치를 소유한다. 외부 field와 승인된 의미를 제외한 구체 Java 이름, package, wire DTO와 저장 표현은 설명용 pseudo type이며 승인된 public API가 아니다.

2026-07-26 source inventory에서 실제 Java path는 입력을 해석하지 않는 합성 objective placeholder이며 ALNS, route pool, MIP와 독립 verifier를 구현하지 않는다. 따라서 별도 current-state inventory가 없는 “기본”, “stable”, “current”는 **목표 domain contract**를 뜻한다. Route pool/MIP는 `C-17` production gate 아래의 구현 가능한 target이지 현재 코드나 approved default가 아니다.

현재 비범위는 multi-trip/rotation 구현, route pool/MIP의 production 기본 활성화, optimizer 제품·라이선스 승인, 실험 없는 pool cap·budget·cadence 공식값, MDVRP·OVRP·SDVRP, 특정 cloud/storage/deployment topology, 동적 교통·실시간 replanning·geocoding이다. `Q-BENCH-02` calibration 전에는 round/worker/step/watchdog의 공식 수치도 없다.

규범어는 다음처럼 읽는다.

- “해야 한다/금지한다”는 target contract다.
- “권장한다/잠정이다”는 구현 선택 여지가 남아 있다.
- “예시”는 모든 customer profile의 계약으로 일반화하지 않는다.
- `PROVEN`, `PASS`, `VERIFIED`, `OPTIMAL` 같은 증거 용어는 각 절에 정의한 authority가 있을 때만 쓴다.

<a id="primer"></a>
## 2. CVRPTW 개발자를 위한 RPDPTW 입문

<a id="cvrptw-vs-rpdptw"></a>
### 2.1 CVRPTW와 RPDPTW의 차이

CVRPTW에서는 보통 한 customer가 하나의 방문이고, depot에서 실은 demand를 customer에서 내린다. 차량 용량은 route를 따라 대체로 감소하며, customer를 어느 route의 어느 위치에 넣을지가 핵심이다.

RPDPTW에서는 하나의 운송 요청이 **pickup과 delivery라는 두 service node**를 만들 수 있다. 두 node는 같은 차량이 모두 수행해야 하고 pickup이 delivery보다 앞서야 한다. Pickup에서 load가 증가하고 delivery에서 감소하므로, 단순한 총 demand가 아니라 모든 route prefix의 load를 검사해야 한다.

| 질문 | CVRPTW의 흔한 답 | 이 문서의 RPDPTW 답 |
|---|---|---|
| 업무 단위 | customer 하나 | `Request` 하나가 pickup+delivery pair를 소유 |
| route 삽입 | 위치 하나 선택 | `pickupPosition < deliveryPosition` 두 위치 선택 |
| 차량 일관성 | customer가 route 하나에 존재 | pair 전체가 같은 concrete vehicle route에 존재 |
| 선후관계 | 보통 없음 | pickup은 delivery보다 반드시 먼저 |
| load 변화 | depot 적재 후 delivery마다 감소 | real pickup `+demand`, real delivery `-demand` |
| 미배정 | customer가 어떤 route에도 없음 | request 전체가 `SearchRequestBank`에 정확히 한 번 |
| exact partition | customer/route 기준 | request pair/route 또는 explicit unassigned 기준 |

작은 예를 보자. 차량 `V1`의 capacity가 10이고, 요청 `R1`은 `P1`에서 6을 싣고 `D1`에서 내리며, `R2`는 `P2`에서 5를 싣고 `D2`에서 내린다.

```text
가능: P1(+6) → D1(-6) → P2(+5) → D2(-5)
load:      6        0        5        0

불가능: P1(+6) → P2(+5) → D1(-6) → D2(-5)
load:      6       11        5        0  // capacity 10 초과
```

두 route의 방문 집합은 같아도 순서에 따라 feasibility가 달라진다. 이것이 pair-aware propagation과 authoritative full evaluation이 필요한 첫 번째 이유다.

<a id="core-vocabulary"></a>
### 2.2 `Order`, `Request`, `Node`, `Visit`, `Route`

초심자가 가장 자주 섞는 다섯 단어를 분리한다.

| 용어 | 의미 | 같은 것으로 취급하면 안 되는 것 |
|---|---|---|
| `Order` | 외부 business input의 주문 표현 | solver의 pair identity |
| `Request` | 정규화 뒤의 원자적 운송 업무 | pickup node 또는 delivery node 하나 |
| `Node` | service/terminal 의미를 가진 immutable solver 정의 | route 안의 특정 출현 |
| `Visit` | 어떤 route 순서에서 node를 수행하는 한 occurrence | physical location |
| `Route` | concrete vehicle, terminal policy와 ordered visits의 결합 | vehicle class 또는 단순 request set |

Java형 pseudo type은 다음 의미를 전달하기 위한 것이다.

```java
record RequestId(int value) {}
record VehicleId(int value) {}
record SolverNodeId(int value) {}
record PhysicalLocationId(int value) {}

enum NodeKind { START_TERMINAL, PICKUP, DELIVERY, END_TERMINAL }
enum ServicePattern { DELIVERY_ONLY, REAL_PICKUP_DELIVERY }

record Request(
    RequestId id,
    SolverNodeId pickupNodeId,
    SolverNodeId deliveryNodeId,
    long demandWeightMilliKg,
    long demandVolumeMilliCbm,
    ServicePattern servicePattern
) {}

record Node(
    SolverNodeId id,
    NodeKind kind,
    PhysicalLocationId locationId,
    long serviceSeconds
) {}

record Visit(SolverNodeId nodeId) {}

record RoutePlan(
    VehicleId vehicleId,
    TerminalPolicy terminalPolicy,
    List<Visit> orderedServiceVisits
) {}
```

`Node`와 physical location도 다르다. 같은 건물에서 pickup과 delivery를 하더라도 node identity는 둘이고 location identity만 같을 수 있다. 반대로 같은 node가 여러 route에 중복 출현하는 것은 stable solution defect다.

<a id="service-patterns"></a>
### 2.3 Delivery-only와 real pickup-delivery

이 프로젝트는 두 service pattern을 같은 core에서 처리한다.

**Real pickup-delivery**는 실제 pickup location, time window와 service time이 있다. Route는 pickup visit와 delivery visit을 모두 포함한다.

```text
depot → R1.pickup(+4) → ... → R1.delivery(-4)
```

**Delivery-only**는 CVRPTW처럼 depot 출발 전에 이미 적재된 화물이다. 내부적으로 pair 의미는 유지하지만 logical pickup은 travel, stop, service visit을 만들지 않는다.

```text
initialLoad = Σ(그 route에 배정된 delivery-only request demand)
depot → R2.delivery(-3) → ...
```

```java
sealed interface PickupSemantics {
    record LogicalInitialLoad() implements PickupSemantics {}
    record PhysicalService(SolverNodeId pickupNodeId) implements PickupSemantics {}
}

record NormalizedRequest(
    RequestId id,
    PickupSemantics pickup,
    SolverNodeId deliveryNodeId,
    long demandWeight,
    long demandVolume
) {}
```

Delivery-only logical pickup을 가짜 depot visit으로 만들면 depot service, stop count, travel과 time window가 오염된다. 반대로 pair identity를 없애면 same-vehicle/exactly-once와 MIP coverage 의미가 두 갈래로 갈라진다. 따라서 logical pickup은 pair 소유권에는 참여하지만 physical propagation에는 참여하지 않는다.

두 pattern은 한 single-trip route에 섞을 수 있다.

```text
initialLoad = delivery-only R2의 3
depot → R1.pickup(+4) → R2.delivery(-3) → R1.delivery(-4)
load:              7                  4                 0
```

<a id="core-invariants"></a>
### 2.4 Same-vehicle, exactly-once, precedence와 route-bank XOR

모든 stable state는 request마다 다음 네 가지를 동시에 만족한다.

1. **Same vehicle**: real pair의 pickup과 delivery는 같은 concrete `VehicleId` route에 있다. 두 차량으로 split할 수 없다.
2. **Exactly once**: 필요한 physical service visit은 각각 정확히 한 번이다. duplicate와 partial pair를 허용하지 않는다.
3. **Precedence**: real pickup의 route position은 delivery position보다 작다.
4. **Route-bank XOR**: request는 완전하게 route 하나에 있거나 bank에 정확히 한 번 있다. 둘 다 또는 둘 다 아님은 금지한다.

```java
sealed interface RequestPlacement {
    record Assigned(VehicleId vehicleId, RouteId routeId)
        implements RequestPlacement {}
    record InBank() implements RequestPlacement {}
}
```

집합으로 쓰면 모든 input request \(i\in I\)에 대해 stable search state에서 다음이 성립한다.

$$
\operatorname{routeOwnerCount}(i)
+
\operatorname{bankMembershipCount}(i)
= 1
$$

여기서 route owner count가 1이면 pair completeness, same-vehicle와 precedence까지 모두 참이어야 한다. 즉 “pickup은 route에 있고 delivery는 bank에 있다” 같은 중간 상태는 `TrialDraft` 내부의 순간적인 편집으로도 외부에 노출할 수 없고 `CompletedTrial`로 끝낼 수도 없다.

작은 예:

```text
정상 assigned:
  V1 route = [R1.pickup, R1.delivery]
  bank     = []

정상 unassigned:
  V1 route = []
  bank     = [R1]

결함:
  V1 route = [R1.pickup]
  bank     = [R1]       // partial pair + route-bank duplicate
```

이 위반은 “infeasible candidate”나 낮은 점수가 아니라 implementation defect다. 이후의 COW editor, destroy/repair, route pool materialization, MIP conversion과 verifier가 모두 이 동일한 request-level partition을 지킨다.

<a id="pipeline"></a>
## 3. 전체 처리 흐름과 책임 경계

개념 학습 순서와 실행 데이터 흐름은 같다.

```text
external bytes/reference
→ versioned input adapter
→ canonical business input
→ normalization
→ prepared directed travel
→ immutable ProblemInstance + exact bound profile/config
→ stable SearchSnapshot / COW TrialDraft
→ propagation
→ full evaluation
→ ALNS operator/acceptance state
→ evaluated route artifacts
→ sealed RoutePoolSnapshot
→ projected MIP columns and exact partition
→ materialization + authoritative full evaluation
→ hybrid adoption/fallback
→ candidate verifier
→ final insertion audit + outcomes/diagnostics
→ result-integrity verifier
→ publishable result
```

| 계층 | 소유하는 것 | 소유하지 않는 것 |
|---|---|---|
| Adapter | schema/version, alias, syntax, raw provenance | feasibility, objective, silent fallback |
| Normalization | numeric/time/service/location/compatibility 의미 | search와 customer price |
| Travel preparation | provided/generated `D/U`, coverage, provenance | ALNS 중 lazy fallback |
| Immutable domain | dense IDs, pair, vehicle, node, location, prepared facts | customer 문자열과 transport |
| Stable state/COW | route sequence, ownership, bank, commit/discard | final status와 final diagnostic |
| Propagation | load/time/travel/resource의 물리 사실과 hard feasibility | 가격과 final outcome |
| Evaluation/profile | neutral metric, constraint, score, comparator, `SolvePlan` | raw input 재해석 |
| ALNS | operator proposal, completed trial, acceptance/adaptive state | route artifact 변형과 publication |
| Route pool | exact-evaluated immutable route artifacts와 lineage | projected coefficient, live route mutation |
| MIP projection | model rows, coefficients, selection evidence | authoritative route evaluation |
| Hybrid | seal/select/materialize/evaluate/adopt 또는 typed fallback | partial commit |
| Verification/result | independent PASS, outcome, diagnostic, summary, provenance | search cache 권위화 |

외부 adapter와 infrastructure는 application orchestration/logical port를 통해 immutable contract를 소비한다. Core는 backend timezone, provider SDK, storage path와 customer name을 참조하지 않는다.

Architecture mapping은 다음 의미 분리를 보존해야 한다.

| Domain 책임 | Maven 경계 | Package 경계 |
|---|---|---|
| Input, immutable domain, normalization, travel | `rpdptw-core` | `input`, `domain`, `normalization`, `travel` |
| Propagation, policy SPI, binding, insertion evaluation | `rpdptw-core` | `propagation`, `evaluation.api`, `evaluation.runtime`, `evaluation.insertion` |
| Portfolio, COW와 ALNS | `rpdptw-solver` | `solver.portfolio`, `solver.state`, `solver.search`, `solver.termination` |
| Route pool, selection SPI와 conversion | `rpdptw-solver` | `solver.pool`, `solver.selection.api`, `solver.selection.conversion`, `solver.hybrid` |
| Google OR-Tools CP-SAT backend | optional/gated algorithm-backend adapter | domain 의미를 재해석하거나 mutate하지 않는 `route-selection-ortools-cpsat` package |
| Candidate/result verification | `rpdptw-verification` | `verification.candidate`, `result.finalization`, `verification.result` |
| Use case와 execution identity | `rpdptw-application` | `application.port`, `application.service`, `application.execution` |
| Customer composition | 독립 profile JAR | `profile.<stable_namespace>` |
| JSON/local/provider mapping | adapter | `adapter.json`, `adapter.local`, 승인된 provider package |

같은 `rpdptw-core` module 안에서도 package 의존 방향은 유지한다.

```text
input/domain → normalization/travel
domain → evaluation.api
domain/travel/evaluation.api → propagation
propagation/evaluation.api → evaluation.runtime/insertion
```

`evaluation.insertion`은 side-effect-free option evaluation까지만 소유한다. COW mutation, acceptance와 adaptive state는 solver에 둔다. Verification은 core를 사용할 수 있지만 solver와 search cache에 compile-depend하지 않는다.

<a id="input"></a>
## 4. 입력 계약

<a id="plan-envelope"></a>
### 4.1 Plan envelope

Canonical solve input은 최소한 다음 의미를 제공한다.

```text
plan identity
planStart / planEnd
depot location, open/close and duration
orders or requests
vehicles
travel input
trips policy
waitInDepot
global route-resource limits
customer profile key/version
objective preset key or omission
```

- Date-time string은 timezone/offset 없는 exact `yyyy-MM-dd HH:mm:ss`다.
- Frontend/backend가 timezone과 offset을 solver 밖에서 처리한다.
- 모든 string은 동일한 전역 고정 시간 기준으로 정렬되어 있어야 한다.
- Solver는 timezone 이름을 추정하거나 hidden default/fingerprint로 만들지 않는다.
- Planning period는 `planStart <= t < planEnd`인 반개구간이다.

<a id="order-request-item"></a>
### 4.2 Order, request와 item

Delivery-only order는 다음 business 의미를 제공한다.

```text
orderId
delivery location
openTime / closeTime
duration
reqDate or dueDate
items[]
vehicleFeatureList
zoneId
capability requirements
mandatory flag when supported by profile
customer extension
```

Real pickup-delivery request는 pickup과 delivery의 location, window, duration, size/zone restriction을 각각 제공한다. 같은 request의 두 작업은 정규화에서 하나의 immutable pair로 bind한다.

`reqDate`와 `dueDate`는 같은 서비스 완료기한의 legacy alias다.

```text
serviceStart <= serviceEnd <= reqDate(dueDate)
```

- 둘 다 있으면 normalized value가 같아야 한다.
- release date나 “이 날짜 이후 배송”으로 읽지 않는다.
- deadline이 plan end와 같아도 plan의 제외 경계를 늘리지 않는다.

`duration`과 `taskTime`은 alias가 아니다.

```text
serviceTime
= request.duration
  + Σ(item.taskTime × item.qty)
```

- `duration`: 차량 진입 등 request 수준의 고정 service time
- `item.taskTime`: item 한 단위의 선적시간
- `qty`: 양의 정수
- legacy order-level `taskTime`: item에 배분하거나 `duration`으로 바꾸지 않고 input error

<a id="vehicle-input"></a>
### 4.3 Vehicle

Vehicle input은 최소 다음 의미를 제공한다.

```text
vehicleId
maxWeight / maxVolume
vehicleFeature
capabilities
zoneId
vhclOwnTyp
workStart / workEnd
speed
maxStopCnt
maxDriveTime / maxDriveDist
terminal policy binding
```

`vhclOwnTyp`은 optional이며 case-sensitive다.

| Raw value | Normalized ownership |
|---|---|
| missing, `null`, empty | `DIRECT` |
| exact `DIRECT` | `DIRECT` |
| exact `LEASE` | `LEASE` |
| 그 밖의 non-empty value | input error |

`DIRECT/LEASE`는 차량 소유 유형이다. Request outcome은 아니다.

<a id="depot-trip"></a>
### 4.4 Depot와 trip option

- `depot.taskTime`은 시간 전파에 적용하지 않는다.
- Depot 회차 간 작업시간은 `depot.duration`이다.
- 최초 출발과 마지막 복귀에는 depot duration을 적용하지 않는다.
- 향후 rotation에서 한 trip 복귀 뒤 다음 trip 재출발 전에만 적용한다.

| Input | Current meaning |
|---|---|
| `trips=oneway` | depot에서 한 번 출발하고 마지막 customer에서 종료; `multiRotation`은 무시 |
| `roundtrip + multiRotation=0` | depot 출발, customer 방문, 같은 depot으로 한 번 복귀 |
| non-oneway + `multiRotation != 0` | `UNSUPPORTED_INPUT` |

Oneway에서 무시한 raw `multiRotation`도 provenance에는 남긴다.

<a id="normalization"></a>
## 5. 정규화

정규화의 결과는 “편리하게 바꾼 값”이 아니라 solver와 verifier가 공유하는 단 하나의 의미다. Numeric/time/service/compatibility policy ID와 version, unit, rounding, order를 fingerprint에 포함한다.

<a id="numeric-normalization"></a>
### 5.1 수치와 단위

| 차원 | 외부 계약 | 내부 표현 |
|---|---|---|
| 무게 | exact decimal kg, `n=3`, 비음수 `FLOOR` | checked `long`, kg × 1,000 |
| 부피 | exact decimal CBM, `n=3`, 비음수 `FLOOR` | checked `long`, CBM × 1,000 |
| 비용 | integer only | checked integer |
| 거리 | integer meter only | checked integer meter |
| 시간 | integer second only | checked `long` second |
| 수량 | positive integer | checked integer |

Decimal은 IEEE-754 `double`로 먼저 근사하지 않고 원문 10진수로 읽는다. Item-first 순서는 규범이다.

```text
raw item weight/volume
→ exact decimal validation
→ n=3 FLOOR
→ normalized integer × qty
→ request sum
→ route/solution checked sum
```

Line decimal 합계를 먼저 만든 뒤 한 번 내리지 않는다. Demand와 capacity는 같은 scale과 rounding을 사용한다.

- 비용·거리·시간 소수는 반올림/절삭하지 않고 거부한다.
- 음수 demand/capacity/time/distance와 비유한 값은 거부한다.
- scale, item×qty, request/route/objective sum은 checked arithmetic을 사용한다.
- overflow를 saturation, wraparound, sentinel로 바꾸지 않는다.
- volume 미사용을 adapter가 명시한 vehicle은 유한한 `999 CBM`으로 정규화하고 provenance를 남긴다.
- 누락 resource limit는 큰 수가 아니라 explicit `constraint absent`다.

<a id="time-normalization"></a>
### 5.2 시간축과 service window

Adapter는 exact date-time string을 parsing한 뒤 planning origin 기준 `long` second로 변환한다.

```text
origin = normalized planStart
normalizedTime = seconds from origin
```

Core는 `ZoneId`, UTC offset, DST, `LocalDateTime`과 raw string을 다루지 않는다.

Boundary:

- Plan은 `[planStart, planEnd)`.
- Window open과 close는 모두 포함한다.
- Close를 1초 줄이거나 plan end를 포함으로 바꾸지 않는다.
- Feasible time과 `INFEASIBLE(reason)`은 별도 타입/상태다.

Customer service:

```text
serviceStart = max(arrival, openTime)
customerWaiting = serviceStart - arrival
serviceEnd = departure = serviceStart + serviceTime
```

Default `START_ONLY`는 `serviceStart <= closeTime`을 요구한다. Profile이 `COMPLETE_WITHIN_WINDOW`를 고르면 `serviceEnd <= closeTime`을 요구한다. Waiting은 neutral metric이며 objective에 자동 포함하지 않는다.

날짜 없는 customer/depot/vehicle window:

- plan에 포함되는 각 날짜에 반복한다.
- `openTime > closeTime`은 `D open → D+1 close`인 하나의 overnight window다.
- 두 독립 window나 empty window로 바꾸지 않는다.
- expansion 결과는 `[planStart, planEnd)`로 clip한다.
- 하나를 놓치면 plan 안의 다음 window까지 기다릴 수 있다.
- `openTime == closeTime`은 24시간/0시간이 모호하므로 명시적 schema 없이는 거부한다.

Vehicle work window에서 arc는 다음 조건으로만 현재 window에 출발한다.

```text
departure + fullTravelTime <= currentWorkEnd
```

현재 window에 전체 arc가 들어가지 않고 다음 work window가 plan 안에 있으면 현재 location에서 쉬고 다음 `workStart`에 같은 arc 전체를 처음부터 시작한다. 중간 운전 거리/time을 누적한 pause/resume은 금지한다. 어떤 단일 available window에도 arc 전체가 들어가지 않으면 infeasible다.

Inter-work-window rest는 drive/customer waiting에서 제외하고 별도 rest와 route operational time에 포함한다.

`waitInDepot`:

```text
earliestDeparture = max(vehicleWorkStart, depotOpen)
```

- `N`: earliest departure에 출발하고 first customer에서 기다린다.
- `Y`: `max(earliestDeparture, firstCustomerOpen - travelTime)`으로 같은 조기 대기를 depot으로 옮긴다.
- hard feasibility를 완화하거나 wait을 없애지 않는다.
- depot waiting과 customer waiting은 별도 metric이다.

<a id="compatibility-normalization"></a>
### 5.3 Size, capability와 zone

Vehicle size:

```text
vehicle.vehicleFeature = one concrete non-empty code
```

Order size:

```text
order.vehicleFeatureList
= one or more concrete codes
  or exactly ["ALL"]
```

- Vehicle missing/null/empty/`"ALL"`: input error
- Order missing/null/empty array: input error
- `["ALL", "T1"]`: input error
- code는 free-form, case-sensitive exact string이며 fixed registry가 없다.
- fleet에 없는 order code는 input error가 아니라 eligible vehicle을 만들지 않는다.
- legacy 배열형 `order.vehicleFeature`는 versioned alias로 읽을 수 있다.
- 신규/legacy field가 함께 있으면 exact list equality가 필요하다.
- legacy 단일-string order field는 거부한다.

Size와 capability는 분리한다.

```text
sizeCompatible
= vehicle.vehicleFeature in request.vehicleFeatureList
  or request list == ["ALL"]

capabilityCompatible
= request.requiredCapabilities
  subsetOf vehicle.capabilities
```

Real pickup과 delivery의 size list가 다르면 같은 차량이 양쪽을 수행할 수 있도록 교집합을 사용한다.

Zone missing/null/empty는 `"ALL"`로 정규화한다. 다른 code는 free-form case-sensitive exact string이다.

```text
concreteRouteZones
= { request.zoneId | visited request and zoneId != "ALL" }

size(concreteRouteZones) <= 1
```

- Zone-neutral vehicle(`ALL`)은 임의의 한 concrete-zone route 또는 ALL-only route를 수행할 수 있다.
- Concrete-zone vehicle은 같은 zone과 `ALL` request만 방문한다.
- `서울 → ALL → 서울`: 가능
- `서울 → ALL → 경기도`: 불가능
- pickup/delivery concrete zone이 다르면 request는 배정 불가
- delivery-only logical pickup은 zone visit이 아니다.
- Size, capability, zone은 독립 hard rule이며 AND로 적용한다.

<a id="travel"></a>
## 6. Travel preparation

Travel key는 solver node가 아니라 physical location ID다.

```text
solver node
→ physicalLocationIndex
→ prepared directed travel
```

| Field | Authority |
|---|---|
| `D` | authoritative directed integer meter |
| `U` | authoritative directed integer second, vehicle-independent |
| `C` | non-authoritative; feasibility/score/generation에 사용하지 않음 |

소수 `D/U`는 거부한다. 현재 `data/win_poc_case.json`의 소수 `D/U`는 비준수이며 정수 matrix를 다시 받거나 명시적 계약을 바꾸기 전 official benchmark에 사용할 수 없다.

External input은 sparse arc나 matrix 전체 생략을 허용할 수 있지만 preparation은 solve 전에 모든 physical location directed \(M^2\) pair를 해소한다.

```text
1. Provided D/U
2. Missing D generation
3. Missing U generation
```

Self arc는 raw value와 관계없이 `D=0 meter`, `U=0 second`다.

Missing `D`:

- coordinate 기반 Great Circle function을 사용한다.
- fractional meter를 `HALF_UP`으로 nearest integer meter로 만든다.
- 필요한 coordinate가 없으면 pre-solve input error다.
- reverse arc 복사나 대칭 평균을 하지 않는다.
- 함수/version은 config와 fingerprint에 포함한다.
- 승인되지 않은 Earth radius나 library default를 이 문서가 만들지 않는다.

Missing `U`:

$$
\operatorname{generatedUSeconds}(v,p,q)
=
\left\lceil
\frac{D_{pq}\times 3.6}{\operatorname{speedKmH}(v)}
\right\rceil
$$

- Vehicle speed가 missing이면 `45 km/h`.
- Provided `U`는 vehicle-independent authoritative common time이다.
- Missing `U`는 vehicle별 generated time이다.
- present-but-non-positive/invalid speed는 missing으로 바꾸지 않고 validation error다.
- representation은 common provided `U`와 vehicle-resolved generated `U`를 구분한다.
- solver 시작 전 모든 사용 vehicle/location pair가 해소돼야 한다.

Solver, candidate verifier와 result verifier는 prepared travel만 사용한다. Search 중 coordinate/speed lazy 계산, missing arc reverse lookup, 대칭화, source 혼동, verifier의 별도 preparation 구현은 금지한다.

Provenance는 최소 다음을 보존한다.

```text
raw input digest
location mapping
provided/generated source per value
Great Circle function/version
speed source and default use
rounding formula
coverage/diagonal policy
prepared travel fingerprint
```

<a id="immutable-model"></a>
## 7. Immutable solver model

정규화 결과는 external ID와 dense core ID의 양방향 mapping을 가진다.

```text
RequestId
VehicleId
SolverNodeId
PhysicalLocationId
```

각 identity는 별개다. 같은 location의 pickup, delivery와 terminal node를 합치지 않는다.

각 `Request`는 pickup node와 delivery node 하나씩을 참조한다.

```java
record SolverRequest(
    RequestId id,
    SolverNodeId pickupNodeId,
    SolverNodeId deliveryNodeId,
    long demandWeight,
    long demandVolume,
    VehicleBitSet servableVehicles,
    ServicePattern servicePattern
) {}
```

Delivery-only pickup은 route 출발 전 initial-load ownership이고 real pickup은 실제 location/time/service를 가진다. Prefix node와 explicit initial-load state 중 구체 내부 표현은 잠정이지만, 어느 쪽도 가짜 travel/stop을 만들거나 pair invariant를 약화할 수 없다.

Normalized vehicle은 capacity, concrete size, capabilities, normalized zone, `DIRECT/LEASE`, work windows, terminal/trips policy, optional resource limits와 prepared travel-time view를 가진다. Missing limit는 `Optional/AbsentConstraint`이지 numeric sentinel이 아니다.

Terminal policy:

- 모든 current route는 start depot/terminal에서 시작한다.
- Oneway는 마지막 service node에서 끝난다.
- Single roundtrip은 같은 depot/end terminal에서 끝난다.
- 내부 depot revisit는 current scope에서 금지한다.
- 모든 route에 end terminal이 있다고 가정하지 않는다.

Immutable `ProblemInstance`는 다음을 bind한다.

```text
dense IDs and external mappings
requests/nodes/vehicles
physical locations
prepared travel
numeric/time/service policies
compatibility facts
profile dependencies
provenance and fingerprints
```

생성 시 ID/index bijection, array length, pair/node/location reference, numeric/time checked range, prepared travel completeness, vehicle-resolved `U`, size/capability schema, pair static compatibility와 profile dependency closure를 검증한다.

호환 차량이 없는 request는 구조 input error가 아니다. Static precheck가 `PROVEN` unassignability evidence를 만들 수 있고 request는 search bank에 남는다.

<a id="stable-cow"></a>
## 8. Stable solution, request bank와 COW

모든 stable state의 request는 `ASSIGNED_IN_SEARCH` 또는 `UNASSIGNED_IN_SEARCH` 중 정확히 하나다.

```text
ASSIGNED_IN_SEARCH
= exactly one route owns RequestId
  and every required physical visit is complete and ordered
  and delivery-only logical pickup belongs to that route initial load
  and request not in SearchRequestBank

UNASSIGNED_IN_SEARCH
= no route owns RequestId
  and no physical service visit is present
  and request in SearchRequestBank exactly once
```

Stable route invariant:

- exactly one concrete vehicle
- terminal policy 일치
- approved service pattern
- pair completeness, same-vehicle와 precedence
- 모든 load prefix에서 `0 <= load <= capacity`
- prepared directed travel만 사용
- time/window/resource hard feasibility
- internal depot revisit 없음

Delivery-only와 real pair를 혼합할 때:

```text
initialLoad = Σ(demand of assigned delivery-only requests)
delivery-only delivery: load decreases
real pickup:             load increases
real delivery:           load decreases
```

중간 depot 재상차는 금지한다.

`SearchRequestBank`는 request ID membership만 가진다. Node, cost, last insertion failure, final status, diagnostic, outsourced/deferred 의미는 저장하지 않는다.

`Q-ALG-02`는 `RESOLVED — KEEP_COW`다. 목표 기본은 copy-on-write다.

```text
committed immutable current
→ copy changed routes + independent bank
→ invalidate derived state
→ structural validation + full evaluation
→ accept: freeze as new current
→ reject/fail/interrupt: discard entire draft
```

`TrialDraft`는 `current`, `stageBest`, `solveBest`를 직접 변경하지 않는다. Apply/undo는 기본 경로가 아니다. COW allocation/GC가 실제 병목으로 측정되고 별도 변경이 승인된 경우에만 round-trip, fault, trace, verifier 동등성 evidence와 함께 제안할 수 있다.

Source of truth:

```text
route sequence
request ownership
vehicle/terminal binding
SearchRequestBank
```

Derived state:

```text
arrival/service/departure/load
travel/resource aggregates
metrics/score/objective
insertion tables
fingerprints
```

Mutation은 영향 cache와 fingerprint를 무효화한다. Cache-free full recomputation과 정확히 같지 않으면 candidate를 비교하거나 게시할 수 없다.

초심자가 알아야 할 search와 selection 상태 전이는 다음과 같다.

```text
ALNS:
TrialDraft
  ── structural exactness + authoritative full evaluation ──▶ CompletedTrial
  ── commit/freeze ──▶ SearchSnapshot

MIP selection:
MaterializedSelectionDraft
  ── structural exactness + authoritative full evaluation ──▶ EvaluatedSelectionCandidate
  ── independent candidate verifier PASS ──▶ VerifiedSolution
```

| Type | 의미 | Stable solution인가 |
|---|---|---:|
| `ConstructionCandidate` | 초기 portfolio의 독립 route/bank | cache-free validation 뒤 예 |
| `TrialDraft` | 한 ALNS step 안의 mutable changed-route COW 작업 | 아니오 |
| `CompletedTrial` | exact structure와 full evaluation을 마친 commit 직전 결과 | 아니오 |
| `SearchSnapshot` | immutable routes/bank/evaluation/fingerprint | 예 |
| `RouteSelectionIncumbent` | backend raw status/termination/provenance와 selected projected-column IDs only | 아니오 |
| `MaterializedSelectionDraft` | selected artifacts를 새 route/bank로 복사한 evaluation 전 결과 | 아니오 |
| `EvaluatedSelectionCandidate` | exact structure와 authoritative evaluation을 통과한 selector 결과 | 예, verifier 전 |
| `VerifiedSolution` | independent candidate verifier가 `PASS`한 immutable solution | 예, publication 전 |

<a id="propagation"></a>
## 9. Route propagation과 resource

Route propagation은 immutable sequence를 앞에서 뒤로 한 번 계산한다.

```text
terminal/start state
→ resolve full travel in one work window
→ arrival
→ customer/depot waiting
→ service start
→ service end/departure
→ load change
→ stop/resource accumulation
→ next leg
```

최소 policy-neutral facts:

```text
arrival
serviceStart
serviceEnd/departure
loadWeight/loadVolume
distance
driveTime
customerWaitingTime
depotWaitingTime
serviceTime
interWorkWindowRestTime
routeOperationalTime
stopCount
```

Propagation은 route의 물리적 사실과 hard feasibility를 계산한다. Customer 가격, objective priority나 final diagnostic을 계산하지 않는다.

Load update는 visit의 service pattern에 따라 다르다.

```java
long initialLoad(RoutePlan route) {
    return sumDeliveryOnlyDemand(route);
}

long loadDelta(Visit visit) {
    return switch (visit.kind()) {
        case REAL_PICKUP -> +visit.requestDemand();
        case REAL_DELIVERY, DELIVERY_ONLY_DELIVERY -> -visit.requestDemand();
        default -> 0;
    };
}
```

모든 prefix에서 lower/upper capacity를 검사한다. Final load는 0이어야 하며 미완료 pair나 중간 재상차로 이를 맞출 수 없다.

Stop은 assigned request 수나 unique location 수가 아니다.

```text
if current customer service location
   != immediately previous customer service location
then stopCount += 1
```

- 연속 same-location orders는 첫 진입만 증가한다.
- `A → B → A`는 세 customer location 진입/전환을 센다.
- start/end/internal depot와 logical pickup은 제외한다.
- route 전체 누적이며 날짜/work window/rest에서 reset하지 않는다.
- vehicle limit와 global `Optimizer.VehicleMaxStopCount`가 모두 있으면 inclusive upper bound의 `min`을 쓴다.
- 하나만 있으면 그 값을, 둘 다 없으면 stop constraint를 두지 않는다.

Drive resource:

```text
driveDist = Σ(actual traversed D_meter arcs)
driveTime = Σ(actual traversed vehicle-resolved U_second arcs)
```

포함:

- start depot → first customer
- customer → customer
- roundtrip final customer → depot
- 향후 승인된 rotation의 실제 depot arc

제외:

- customer/depot waiting
- request/depot service
- inter-work-window rest

`driveTime <= maxDriveTime`, `driveDist <= maxDriveDist`는 inclusive upper bound다. Route 전체 누적이며 날짜/rest에서 reset하지 않는다.

`routeOperationalTime`은 다음 neutral breakdown의 합으로 계산할 수 있어야 한다.

```text
driveTime
+ customerWaitingTime
+ depotWaitingTime
+ serviceTime
+ interWorkWindowRestTime
```

Multi-trip/rotation은 current scope가 아니다. 향후에도 pair와 delivery-only request는 한 trip 안에서 완결되고, trip 종료 시 load가 0이며, 다음 trip은 승인된 depot duration 뒤 새 initial load로 시작해야 한다. `multiRotation`, reset, depot window와 representation은 별도 승인을 요구한다.

<a id="evaluation"></a>
## 10. Evaluation, profile과 objective

평가는 다음 층을 섞지 않는다.

```text
normalized immutable facts
→ propagation + structural/static hard gates
→ policy-neutral metrics
→ composed hard constraints
→ score components
→ objective schema/comparator
→ SolvePlan stages
```

- Hard violation을 finite penalty로 상쇄하지 않는다.
- Metric은 좋고 나쁨, 가격과 선호를 포함하지 않는다.
- Score는 raw route/input을 재해석하지 않는다.
- Comparator는 physical propagation을 다시 계산하지 않는다.
- `SolvePlan`은 hard rule을 해제하지 않는다.

Long-lived profile definition/registry와 solve-bound immutable profile을 분리한다.

Binding 순서:

1. Exact customer profile key/version resolve
2. Objective preset resolve
3. Normalized facts/IDs/unit/schema에 dependency bind
4. Missing metric, duplicate key, unit mismatch, unknown reference reject
5. Immutable bound snapshot과 fingerprint 생성

Solve request는 그 customer에 등록·승인된 preset만 선택할 수 있다. Objective order, weight와 formula를 request가 직접 주입하지 않는다. Preset이 빠지면 customer config에 exact key/version으로 지정된 default를 사용한다. 일부 customer에만 있는 metric/objective를 다른 customer dimension으로 fallback하지 않는다.

Architecture는 다음을 보존한다.

- Constraint/metric/score/objective/profile SPI와 binder는 core의 `evaluation.api`/`evaluation.runtime`이 소유한다.
- Standard/customer 구현과 preset composition은 별도 profile JAR가 소유한다.
- Core는 구체 profile JAR를 compile-depend하지 않는다.
- Worker assembly가 provider를 등록하고 binder가 exact key/version/preset을 검증한다.
- Customer name이나 preset label로 core, solver, verifier에서 분기하지 않는다.

Mandatory를 지원하는 preset에서:

```text
mandatoryUnassignedCount
```

는 최상위 lexicographic objective다. Hard constraint나 finite penalty가 아니다. 0이 불가능하면 최소 양수인 verified partial solution도 유효할 수 있다.

Ownership objective:

```text
regularVehicleVolumeCost
= Σ(maxVolume of each used DIRECT vehicle once)

outsourcedVehicleVolumeCost
= Σ(maxVolume of each used LEASE vehicle once)
```

Ordering:

```text
optional mandatoryUnassignedCount
→ totalUnassignedCount
→ optional outsourcedVehicleVolumeCost
→ regularVehicleVolumeCost
→ remaining customer objectives
```

- `regularVehicleVolumeCost`는 기본 dimension이다.
- `LEASE`를 사용하는 preset만 outsourced dimension을 추가한다.
- LEASE vehicle이 입력됐는데 preset이 dimension을 지원하지 않으면 bind error다.
- 고정 `1:100`, 음수 score 또는 instance Big-M로 strict priority를 흉내 내지 않는다.

Win PoC result comparator는 customer solve objective와 별개다.

```text
unassigned request count
→ dispatched vehicle count
→ total directed distance
→ total route operational time
```

모든 성분은 작을수록 좋고 첫 번째 다른 성분이 승패를 정한다.

```text
totalRouteOperationalTimeSeconds
= Σ_used_routes(
    driveTime
  + customerWaitingTime
  + depotWaitingTime
  + serviceTime
  + interWorkWindowRestTime
)
```

미사용 vehicle idle, route 전 업무 무관 시간, solver/verifier/serialization elapsed는 포함하지 않는다. Breakdown과 total을 verifier가 재계산할 수 있어야 한다.

Portfolio 계약은 4개 request-route 성장 정책과 2개 `DIRECT`-first vehicle 순서를 조합한 최대 8개 독립 construction이다.

```text
Growth:
  CLOCK
  SEQ_FARTHEST
  SEQ_LARGE_DEMAND
  SEQ_EARLIEST_DEADLINE

Vehicle order:
  DIRECT_FIRST_LARGE
  DIRECT_FIRST_SMALL
```

모든 construction은 같은 side-effect-free atomic pair evaluator와 bound comparator를 사용한다. 각 available candidate는 exact `screenMaxSteps`의 phase-1 screen을 정상 `MAX_STEPS_REACHED`로 완료하고 cache-free validation 뒤 champion 하나를 고른다. Phase 2는 그 champion만 공통 warm start로 사용해 declared worker batch를 실행한다.

`screenMaxSteps`, `phase2MaxSteps`, worker 수, `maxRounds`, watchdog의 **공식 수치**는 `Q-BENCH-02` calibration 전까지 없다. Domain config는 생략값을 hidden official default로 채우지 않는다.

<a id="alns"></a>
## 11. ALNS state와 operator

ALNS 상태는 stable solution과 adaptive/search metadata를 명시적으로 분리한다.

```java
record AlnsSearchState(
    SearchSnapshot current,
    SearchSnapshot stageBest,
    SearchSnapshot solveBest,
    SearchProgress progress,
    OperatorLearningSnapshot operatorLearning,
    AcceptanceStateSnapshot acceptanceState,
    RngLineage rngLineage,
    Optional<RoutePoolSnapshotId> routePoolSnapshotId
) {}
```

- `current`, `stageBest`, `solveBest`는 서로 alias되지 않는다.
- `TrialDraft`만 step 안에서 mutable하고 commit/discard 뒤 참조되지 않는다.
- `CompletedTrial`은 `SearchSnapshot`으로 commit되거나 폐기된다.
- Stable candidate는 `SearchSnapshot` 또는 verifier 뒤 `VerifiedSolution`만 뜻한다.
- Operator/history/acceptance state는 completed step 뒤에만 새 immutable snapshot으로 교체한다.
- `RESET_EACH_ALNS_PHASE`와 `CARRY_ACROSS_HYBRID_PHASES`는 route pool persistence와 별도 policy다.
- 비교는 raw cost가 아니라 bound comparator를 사용한다.

Iteration outcome은 다음을 구분한다.

```text
GLOBAL_BEST_IMPROVED
CURRENT_IMPROVED
ACCEPTED_NON_IMPROVING
REJECTED
INVALID_CANDIDATE
INTERRUPTED
```

Reward policy는 이 enum을 소비한다. `INVALID_CANDIDATE`와 `INTERRUPTED`는 일반 reject reward나 completed-step을 만들 수 없다.

Destroy operator는 route를 직접 변경하지 않고 제거 proposal만 만든다.

```java
record DestroyProposal(
    OperatorVersion operator,
    int requestedRemovalCount,
    List<RequestId> orderedUniqueRequests,
    Set<RouteId> sourceRoutes,
    ScoreAndTieEvidence evidence,
    Digest decisionTraceDigest
) {}
```

Central atomic editor가 proposal을 적용한다.

```java
record DestroyResult(
    Set<RequestId> actuallyRemovedRequests,
    Set<RouteId> changedRouteIds,
    TrialDraft postDestroyTrial,
    Fingerprint beforeStructural,
    Fingerprint afterStructural
) {}
```

제거 전 request는 route 하나에 완전히 배정돼 있어야 한다. 제거 뒤 pair 전체가 사라지고 bank에 정확히 한 번 있어야 한다. Worst/Semi-worst operator도 fleet/cost를 직접 고치지 않고 ranking만 제안한다. Mutable list index 대신 stable `RouteId`를 쓴다.

Repair result:

```text
COMPLETE_REINSERTION
PARTIAL_REINSERTION
NO_FEASIBLE_INSERTION
DEFECT
```

```java
record RepairResult(
    RepairScope scope, // REMOVED_ONLY | REMOVED_PLUS_EXISTING_BANK
    Set<RequestId> attemptedRequests,
    List<InsertionOption> appliedInsertions,
    Set<RequestId> requestsRemainingInBank,
    Set<RouteId> changedRouteIds,
    TrialDraft postRepairTrial,
    List<RouteCollectionCandidate> routeCollectionCandidates
) {}
```

Partial/no-feasible도 route-bank exact partition과 hard feasibility를 만족하면 정상 `CompletedTrial`로 full-evaluate할 수 있다. `DEFECT`는 evaluation/acceptance로 진행하지 않는다.

Cheap ranking과 authoritative insertion evaluation은 다른 타입이다.

```java
record RepairRouteCandidate(
    RequestId requestId,
    RouteTarget target, // RouteId | NEW_ROUTE
    VehicleId rankBasisVehicleId,
    long promisingScore,
    StaticGate staticGate,
    Fingerprint routeFingerprint
) {}

sealed interface InsertionEvaluation {
    record Feasible(InsertionOption option) implements InsertionEvaluation {}
    record Rejected(ConstraintEvidence evidence) implements InsertionEvaluation {}
}
```

`InsertionOption`은 request, target route 또는 `NEW_ROUTE`, concrete vehicle binding/rebinding plan, pickup/delivery position, 새 ordered visits, exact propagated facts, objective delta와 new fingerprint를 가진다.

- Real pair는 모든 합법 `pickupPosition < deliveryPosition` 조합을 평가할 수 있다.
- Delivery-only logical pickup은 가짜 travel/stop/service position을 만들지 않는다.
- OGC의 all-pickups-first/all-deliveries-later는 canonical invariant가 아니며 compatibility operator 안에만 격리한다.
- `NEW_ROUTE`는 실제 unused `VehicleId`를 소비한다. Vehicle type/count sentinel을 사용하지 않는다.
- Promising score는 shortlist용이며 feasibility/objective/diagnostic authority가 아니다.
- Shortlist miss는 품질 문제이지 stable solution feasibility 위반이 아니다.

Hard-feasible `CompletedTrial`의 route는 candidate acceptance와 독립적으로 route pool delta 후보가 될 수 있다. Rejected trial에서 수집할 수 있지만 invalid/interrupted draft에서는 수집할 수 없다. Pool delta commit과 `current` commit은 별도다.

<a id="route-pool"></a>
## 12. Evaluated route artifact와 route pool

**Evaluated route artifact와 projected MIP column은 같은 객체가 아니다.** Artifact는 domain route와 authoritative evaluation을 보존한다. Column은 특정 projection/model의 row와 objective coefficient를 encode한다. Profile projection을 바꾸면 같은 artifact에서 다른 column이 생길 수 있지만 artifact 자체는 바뀌지 않는다.

세 identity를 분리한다.

```text
RouteSignature
  problemFingerprint
  preparedTravelFingerprint
  boundProfileFingerprint
  concrete VehicleId
  terminalPolicy
  ordered service visit IDs

RouteCoverageKey
  same authority fingerprints
  same concrete VehicleId
  exact RequestId set

RouteArtifactId
  RouteSignature digest
  + authoritative evaluation fingerprint

ProjectedColumnId
  RouteArtifactId
  + projection fingerprint
  + encoded row/objective coefficient digest
```

Pseudo type:

```java
record EvaluatedRouteArtifact(
    RouteArtifactId id,
    RouteSignature signature,
    RouteCoverageKey coverageKey,
    RoutePlan immutableRoute,
    RequestCoverage coverage,
    ExactRouteEvaluation evaluation,
    DiscoveryLineage lineage,
    Fingerprint evaluationFingerprint
) {}

record ProjectedRouteColumn(
    ProjectedColumnId id,
    RouteArtifactId sourceArtifactId,
    ExactRequestVehicleResourceRows rows,
    ExactObjectiveCoefficients coefficients,
    Fingerprint projectionFingerprint
) {}
```

`verified`라는 말은 independent verifier 결과에만 사용한다. Pool admission의 exact full evaluation을 “verified route”라고 부르지 않는다.

Pool invariant:

1. 모든 artifact는 nonempty, pair-complete, hard-feasible이며 같은 problem/travel/profile authority에 속한다.
2. `RoutePlan`과 evaluation은 immutable이다. Pool route를 conversion/post-processing하면서 수정하지 않는다.
3. 같은 signature/authority duplicate는 evaluation과 coverage가 같을 때 lineage만 deterministic merge한다. 값이 다르면 하나를 고르지 않고 integrity defect로 거부한다.
4. Base pool은 coverage가 같다는 이유만으로 artifact를 제거하지 않는다.
5. Profile의 versioned route-fact dominance가 모든 소비 경로에서 안전함을 증명할 때만 pool-level 제거한다.
6. 특정 projection column pruning은 같은 concrete vehicle에서 objective뿐 아니라 모든 hard/resource/linearization row coefficient의 component-wise dominance가 있을 때만 허용한다.
7. 그 밖에는 nondominated frontier 또는 conservative retention을 사용한다.
8. Vehicle-class aggregation은 별도 model mode와 equivalence proof가 필요하며 coverage bucket에 섞지 않는다.
9. Append/import/reload는 같은 validation, dominance, tie-break를 사용한다.
10. Incumbent artifact는 snapshot pruning에서 보호한다.
11. Live pool은 selector 입력이 아니다. Stable order로 seal한 `RoutePoolSnapshot`만 selector에 전달한다.
12. 다른 authority fingerprint의 snapshot/delta import는 input error가 아니라 hybrid artifact integrity failure다.

`RoutePoolDelta`는 append-only derived artifact다. 각 accepted/rejected `CompletedTrial`에서 full-evaluated hard-feasible route만 수집하고, committed route/bank state는 바꾸지 않는다.

<a id="mip"></a>
## 13. MIP projection과 exact partition

<a id="mip-projection"></a>
### 13.1 Route-selection projection

MIP에 전달하는 것은 arbitrary objective expression이 아니라 solve-bound projection이다.

```java
record RouteSelectionProjection(
    ProjectionVersion version,
    Fingerprint fingerprint,
    ModelMode modelMode,
    RequestMapping requests,
    ConcreteVehicleResourceMapping vehicles,
    List<ObjectiveDimension> orderedDimensions,
    RouteCoefficientEncoder routeEncoder,
    UnassignedCoefficientEncoder unassignedEncoder,
    Set<SupportedHardConstraint> supportedHardConstraints,
    CoefficientRangeDeclaration coefficientDeclaration
) {}
```

각 profile dimension은 다음 중 하나로 분류한다.

- `ROUTE_ADDITIVE`: route column coefficient 합으로 정확히 표현
- `UNASSIGNED_ADDITIVE`: request별 unassigned coefficient 합으로 정확히 표현
- `EXACT_LINEARIZATION`: versioned 추가 변수/constraint encoder로 정확히 표현
- `NON_PROJECTABLE`: 현재 selector로 정확 표현 불가

`NON_PROJECTABLE` dimension이나 solution-level hard constraint를 조용히 버리거나 surrogate로 바꾸지 않는다. Production exact mode는 `SKIPPED_NON_PROJECTABLE_PROFILE`로 끝낸다. 별도 승인된 `SURROGATE_EXPERIMENT_ONLY`는 exact mode와 다른 algorithm/projection fingerprint를 사용하며 production adoption evidence가 아니다.

<a id="exact-partition"></a>
### 13.2 Canonical `SET_PARTITION_EXACT`

집합과 인덱스를 먼저 고정한다.

- \(I\): 모든 input request 집합
- \(R\): 한 sealed `RoutePoolSnapshot`을 exact projection해 얻은 column 집합
- \(V\): 모든 concrete input vehicle 집합
- \(D\): ordered lexicographic objective dimension index 집합
- \(a_{ir}\in\{0,1\}\): column \(r\in R\)이 request \(i\in I\)의 완전한 pair를 포함
- \(h_{vr}\in\{0,1\}\): column \(r\in R\)이 concrete vehicle \(v\in V\)를 소비
- \(x_r\in\{0,1\}\): route column \(r\) 선택
- \(u_i\in\{0,1\}\): request \(i\)를 route 밖 explicit bank에 유지

Request exact partition:

$$
\sum_{r\in R} a_{ir}x_r + u_i = 1
\qquad \forall i\in I
$$

Concrete vehicle single-route consumption:

$$
\sum_{r\in R} h_{vr}x_r \le 1
\qquad \forall v\in V
$$

Column admission 전에 pair, capacity, time, terminal, capability, zone와 route-level hard feasibility는 이미 full-evaluated 상태다. 여러 차량을 class/count row로 합치려면 prepared travel, work window, terminal, capacity, ownership, objective coefficient와 모든 MIP-relevant resource가 동등하다는 별도 proof가 필요하다.

Mandatory request를 자동으로 \(u_i=0\) hard assignment로 만들지 않는다. 기본 의미는 `mandatoryUnassignedCount` 최우선 최소화다. Exact assignment가 별도 preset에서 승인될 때만 projection이 해당 \(u_i=0\)을 명시한다.

각 objective dimension \(d\in D\)이 route/unassigned 단위로 가산 가능하면:

$$
Q_d(x,u)
=
\sum_{r\in R} q^{\mathrm{route}}_{dr}x_r
+
\sum_{i\in I} q^{\mathrm{unassigned}}_{di}u_i
$$

Objective는 bound comparator와 같은 dimension order의 lexicographic minimization이다. 단계별 solve는 \(Q_d\)의 optimality가 증명된 뒤에만 \(Q_d=Q_d^\*\)를 고정하고 다음 dimension으로 간다. `FEASIBLE_LIMIT`에서는 더 낮은 priority dimension을 풀지 않는다.

Canonical backend는 Google OR-Tools CP-SAT다. 단계 \(d\)의 exact `OPTIMAL`이 증명된 뒤에만 integer equality \(Q_d=Q_d^\*\)를 다음 model stage에 추가한다. CP-SAT gap limit 충족도 `OPTIMAL`로 보고될 수 있으므로 loose absolute/relative gap은 exact proof가 아니며 exactness를 보존하는 gap 정책 승인 전에는 활성화하지 않는다. 모든 variable, constraint와 objective coefficient를 checked int64 model로 구성하고 domain/sum overflow를 model build 전에 검증한다. Continuous variable가 없으므로 MPSolver는 canonical backend가 아니다. 근거 없는 finite Big-M로 strict priority를 평탄화하지 않는다.

Win PoC projection은 다음과 같은 **예시**다.

```text
Q1 = Σ_{i∈I} u_i
Q2 = Σ_{r∈R} x_r
Q3 = Σ_{r∈R} routeDistance_r x_r
Q4 = Σ_{r∈R} routeOperationalTime_r x_r
```

모든 customer objective가 이 네 dimension인 것은 아니다.

<a id="model-modes"></a>
### 13.3 Model mode: canonical과 OGC compatibility

| Mode | Request row | 지위 |
|---|---|---|
| `SET_PARTITION_EXACT` | \(\sum_{r\in R}a_{ir}x_r+u_i=1\) | canonical RPDPTW target |
| `SET_COVER_THEN_CONVERT` | \(\sum_{r\in R}a_{ir}x_r\ge1\), \(u_i\) 없음 | complete-cover OGC 2024 compatibility/differential experiment 전용 |

두 mode를 같은 algorithm fingerprint로 비교하지 않는다. Set cover는 explicit unassigned/bank를 표현하지 못하므로 모든 request를 한 번 이상 덮는 snapshot에서만 실행한다. Raw cover 결과는 duplicate coverage를 허용하므로 `SearchSnapshot`이나 `VerifiedSolution`이 아니다.

<a id="mip-warm-start"></a>
### 13.4 MIP warm start

```text
MipWarmStart
  cache-free-validated phase incumbent fingerprint
  poolSnapshotId/fingerprint
  selected incumbent ProjectedColumnIds
  incumbent SearchRequestBank as u_i (SET_PARTITION_EXACT only)
  problem/travel/profile/projection fingerprints
  feasibility check record
```

Warm start 전에 incumbent route를 exact evaluation으로 pool에 merge하고 pinned artifact로 둔다. Seal/model-build 시 projection한 selected `ProjectedColumnId`가 manifest에 존재하고 request partition, concrete vehicle consumption과 모든 row를 만족해야 한다.

Backend start는 hint다. Disposition은 `OFFERED`, `ACCEPTED_BY_BACKEND`, `REJECTED_BY_BACKEND`, `UNKNOWN`을 구분할 수 있다.

`AlnsWarmStart`와 `MipWarmStart`를 섞지 않는다.

- `AlnsWarmStart`: stable routes/bank, pool lineage, RNG/adaptive continuation policy
- `MipWarmStart`: fixed column/unassigned variable values와 model fingerprint

<a id="selection-outcome"></a>
### 13.5 Selection status와 evidence

Provider-neutral outcome:

```text
OPTIMAL
FEASIBLE_LIMIT
NO_INCUMBENT_LIMIT
PROVEN_INFEASIBLE
MODEL_INVALID
BACKEND_UNAVAILABLE
NATIVE_RUNTIME_UNAVAILABLE
MODEL_BUILD_FAILED
SOLVER_FAILED
SKIPPED_NON_PROJECTABLE_PROFILE
SKIPPED_NO_BUDGET
```

Outcome은 raw backend status, incumbent presence, selected `ProjectedColumnId`, work/elapsed, backend/config fingerprint와 warm-start disposition을 보존한다. Backend adapter의 선택 반환은 route IDs뿐이며 unassigned set은 selected coverage의 exact complement로 materialization 경계가 재구성한다. `CpSolver.objectiveValue()`/bound/gap은 outcome 반환이나 adoption authority에 넣지 않는다.

- CP-SAT `OPTIMAL`/`FEASIBLE`에서만 selected value를 읽고 각각 provider-neutral
  `OPTIMAL`/`FEASIBLE_LIMIT`로 매핑한다.
- `INFEASIBLE`/`MODEL_INVALID`/`UNKNOWN`은 각각 `PROVEN_INFEASIBLE`/
  `MODEL_INVALID`/`NO_INCUMBENT_LIMIT`이며 selected value를 읽지 않는다.
- Time limit, asynchronous `stopSearch()` cancellation, memory/custom limit은 raw status와
  별도 cause다. Cancellation 뒤 `FEASIBLE`이면 incumbent-present,
  `UNKNOWN`이면 no-incumbent다.
- `BACKEND_UNAVAILABLE`: optional assembly capability 부재
- `NATIVE_RUNTIME_UNAVAILABLE`: `Loader.loadNativeLibraries()` 또는 platform-native 초기화 실패
- Feasible warm start가 있는 exact model의 `PROVEN_INFEASIBLE`: 품질 결과가 아니라 model/snapshot defect
- Backend model objective는 conversion 뒤 evaluated solution objective의 authority가 아니다.

<a id="materialization-conversion"></a>
### 13.6 Materialization과 OGC conversion

`SET_PARTITION_EXACT`는 selected artifact의 immutable `RoutePlan`을 **새 route 목록으로 복사**한다.

$$
\operatorname{newBank}
=
I \setminus
\bigcup_{r:x_r=1}\operatorname{coverage}(r)
$$

이 complement와 exact request row/model identity를 교차검증하며 backend의 \(u_i\) value를 별도 반환하지 않는다. Pool entry를 alias하거나 mutate하지 않는다.

`SET_COVER_THEN_CONVERT`는 다음 순서를 지킨다.

1. Duplicate request를 stable ID 또는 전용 seeded stream 순서로 처리한다.
2. Duplicate를 남길 각 route 선택마다 route removal, empty route 삭제, vehicle count와 **전체 candidate objective**까지 반영한 temporary solution을 authoritative full evaluation/comparator로 비교한다.
3. 제거 route에서는 pickup/delivery pair를 atomic하게 제거해 새 route를 만든다.
4. Empty route를 제거하고 changed route propagation, metric, objective와 signature를 재계산한다.
5. Exact request/vehicle partition을 다시 검증한다.

단일 additive-cost compatibility mode만 다음 OGC marginal 규칙을 재현할 수 있다.

$$
\Delta_i(r)
= C(r)-C(r\setminus\{i\})
$$

\(\Delta_i(r)\)가 가장 작은 route에 request를 남기고 비용 절감이 더 큰 나머지 route에서 제거한다. 일반 lexicographic/non-additive profile은 각 keep choice의 전체 temporary solution을 full-evaluate하거나 mode를 skip한다.

Backend `ObjVal`은 conversion 전 model evidence다. `MaterializedSelectionDraft`는 structural partition, authoritative full propagation/evaluation과 fingerprint를 통과해야 `EvaluatedSelectionCandidate`가 된다.

<a id="hybrid"></a>
## 14. Hybrid phase와 typed fallback

ALNS step, ALNS segment, inner hybrid phase와 distributed outer round를 분리한다.

```text
ALNS step
  < ALNS segment (declared completed-step budget)
  < HybridPhase (ALNS → pool seal → selection → adoption)
  < WorkerRun
  < ExecutionRound (declared worker fan-out/fan-in)
```

Hybrid 비교 기준은 직전 ALNS segment가 완료한 cache-free validated `solveBest`, 즉 `HybridPhaseIncumbent`다. Selector의 `EvaluatedSelectionCandidate`가 bound comparator에서 `STRICTLY_BETTER`일 때만 다음 inner phase의 `AlnsWarmStart`로 채택한다.

```text
selector candidate = equal/worse/invalid
or no incumbent
or backend/native-runtime/model/conversion/evaluation failure
→ preserve HybridPhaseIncumbent fingerprint
```

Search bank의 \(u_i\)나 MIP failure를 final `UNASSIGNED` outcome/diagnostic으로 바꾸지 않는다.

`HybridPhaseRecord`는 최소 다음을 보존한다.

```text
round/worker/hybridPhase/alnsRun identities
input champion fingerprint
problem/travel/profile/config/build fingerprints
phase budget + reproducibility class
ALNS termination/best/operator stats/decision trace digest
pool before/delta/after fingerprints + admission/pruning counts
projection/model/warm-start/backend fingerprints
selection status/incumbent/selected projected-column IDs
materialized unassigned coverage complement
conversion/materialization/full-evaluation record
incumbent-vs-selector comparator decision
adopted champion or typed fallback
next warm-start fingerprint
```

Commit state:

```text
STARTED
→ ALNS_COMPLETED
→ POOL_SEALED
├─ SELECTION_SKIPPED | NO_INCUMBENT | SELECTION_FAILED
│  ├─ optional → INCUMBENT_RETAINED → HYBRID_PHASE_COMMITTED
│  └─ required → HYBRID_PHASE_INCOMPLETE
└─ SELECTION_WITH_INCUMBENT
   → MATERIALIZED
   ├─ EVALUATION_FAILED
   │  ├─ optional → INCUMBENT_RETAINED → HYBRID_PHASE_COMMITTED
   │  └─ required → HYBRID_PHASE_INCOMPLETE
   └─ FULL_EVALUATED
      → INCUMBENT_RETAINED | SELECTOR_CANDIDATE_ADOPTED
      → HYBRID_PHASE_COMMITTED
```

다음 phase는 `HYBRID_PHASE_COMMITTED` champion만 소비한다. Interrupted conversion이나 failed selector가 current/stageBest/solveBest, sealed pool snapshot 또는 next warm start를 부분 변경할 수 없다.

Route-selection status는 ALNS termination과 다르다. Optional plan에서는 typed fallback으로 incumbent를 유지할 수 있다. MIP-required plan에서 selection/materialization/evaluation failure는 worker/run `INCOMPLETE` 또는 failure다.

Worker-local pool persistence가 reference-adaptation baseline이다. Cross-worker pool merge와 central selection은 `RM-9C` 뒤의 별도 scalability decision이다.

Multi-round lineage는 다음을 표현한다.

```text
round ordinal
worker/run ordinal
base/derived seed and derivation version
warm-start candidate fingerprint
requested/completed maxSteps
termination
candidate verification
round champion
next-round lineage
hybrid phase and ALNS segment lineage
route-pool before/delta/after fingerprints
projection/model/warm-start/backend identities
selection/conversion/full-evaluation/adoption/fallback
```

모든 declared worker가 normal max-step completion과 verification을 통과해야 round complete다. 일부 성공 worker만으로 champion을 만들지 않는다. Round/worker 수, `maxSteps`, watchdog 공식값은 `Q-BENCH-02` 전까지 없다. Logical fan-out/fan-in은 physical infrastructure를 결정하지 않는다.

<a id="verification-result"></a>
## 15. Verification, finalization과 result

Publication은 두 gate와 그 사이의 finalization을 통과한다.

```text
committed SearchSnapshot or EvaluatedSelectionCandidate
→ independent candidate solution verifier
→ VerifiedSolution
→ preliminary ASSIGNED/UNASSIGNED partition
→ required final-solution insertion audit
→ final outcomes/diagnostics
→ post-finalization result-integrity verifier
→ publishable result
```

Candidate verifier의 authority:

```text
immutable problem/profile declaration
candidate route/node order
candidate SearchRequestBank
prepared authoritative travel
```

Verifier는 search propagation cache, insertion cache와 solver summary를 권위 입력으로 쓰지 않는다. Pair, route-bank partition, terminal, load/time/travel/resource, metrics, objective와 fingerprints를 cache-free recompute한다. `PASS` 뒤에만 `VerifiedSolution`을 만든다.

모든 input request는 final result에서 정확히 하나의 outcome을 가진다.

| Status | Meaning |
|---|---|
| `ASSIGNED` | exactly one verified input vehicle route/pair reference |
| `UNASSIGNED` | 어떤 verified input vehicle route에도 없음 |

`DIRECT`와 `LEASE` vehicle에 배정된 request는 모두 `ASSIGNED`다. Ownership은 vehicle reference와 objective breakdown으로 표현한다. Solver는 fleet 밖 provider를 만들지 않고 운영자의 후속 외주/이월을 `OUTSOURCED`/`DEFERRED` outcome으로 생성하지 않는다.

Final-solution insertion audit:

- Normalization/static precheck만으로 순서·탐색 품질과 무관한 불가능성이 증명된 request는 `PROVEN` evidence를 사용하고 중복 audit를 생략할 수 있다.
- 나머지 모든 `UNASSIGNED` request는 final routes와 다른 request placement를 고정한다.
- 모든 eligible concrete vehicle과 모든 합법 pickup/delivery position pair를 검사한다.
- Constraint별 rejection count와 work count를 기록한다.
- 모든 option이 실패해야 `EXHAUSTIVE_FOR_FINAL_SOLUTION`을 사용할 수 있다.
- 이는 현재 final solution에 대한 exhaustive insertion이지 전역 재배치 불가능성 증명이 아니다.

Audit가 feasible insertion을 찾더라도 자동 insert, solver 재호출, 수정 loop를 시작하지 않는다. Outcome은 `UNASSIGNED`로 게시할 수 있고 발견은 internal audit record에 보존한다. External diagnostic은 독립적으로 성립한 `PROVEN`, search-observed 또는 `UNKNOWN` evidence만 사용한다.

Result-integrity verifier의 authority:

```text
candidate PASS report + VerifiedSolution
final outcomes
diagnostic source/audit evidence
outcome-derived summary
publishable payload
```

이 verifier는 exactly-one outcome, assigned reference, unassigned evidence ceiling, summary 재계산과 payload consistency를 검사한다. 어느 gate든 fail/incomplete이면 정상 route/outcome과 benchmark vector를 게시하지 않는다.

Result summary는 authoritative route/outcome에서 파생한다. Win PoC metric은 §10의 exact comparator와 operational-time 식을 사용한다. Backend `ObjVal`, stale solver cache, request bank의 임시 failure reason은 result authority가 아니다.

<a id="errors-termination"></a>
## 16. 오류와 종료 모델

Pre-solve error:

- schema/version/alias conflict
- invalid numeric/time string
- decimal cost/distance/time 또는 decimal `D/U`
- order-level `taskTime`
- invalid size/zone/ownership shape
- unresolved physical location
- generated `D`에 필요한 coordinate 누락
- preparation 뒤 incomplete travel
- unsupported non-oneway rotation
- unknown/unauthorized profile 또는 preset
- missing profile dependency
- checked arithmetic overflow

Search/domain defect:

- partial/duplicate/split pair
- route+bank duplicate 또는 omission
- wrong terminal policy
- stale cache/full-recomputation mismatch
- failed COW discard isolation
- runtime unresolved travel
- destroy proposal duplicate/non-assigned request
- pair-atomic edit failure
- route artifact authority mismatch, mutable alias 또는 stale evaluation
- unsafe dominance로 nondominated route 손실
- selected column materialization 뒤 request/vehicle partition 위반
- projected coefficient digest/model manifest mismatch

이 결함을 정상 `UNASSIGNED` reason, finite penalty나 낮은 score로 숨기지 않는다.

정상 ALNS/round 품질 종료:

- `MAX_STEPS_REACHED`: 계획된 stage/worker completed-step budget 완료
- `NO_STRICT_IMPROVEMENT`: 완결된 phase-2 batch champion이 이전 champion보다 엄격히 좋지 않음
- `MAX_ROUNDS_REACHED`: configured round 완료

Watchdog, cancellation, resource/platform timeout과 failure는 별도다. 예외 종료의 last committed best도 두 verifier를 통과해야 recovery candidate가 될 수 있으며 official benchmark completion으로 표시하지 않는다.

`NO_INCUMBENT_LIMIT`, backend/native-runtime/model/solver/conversion/full-evaluation failure는 ALNS step termination이 아니다. Optional hybrid plan에서는 typed incumbent-preserving fallback이고, MIP-required plan에서는 `INCOMPLETE`/failure다. Raw backend incumbent와 model objective는 recovery candidate가 아니다.

<a id="acceptance-evidence"></a>
## 17. Acceptance evidence

이 절은 구현이 “문서와 비슷해 보임”이 아니라 규범 계약을 만족함을 증명하는 최소 evidence다.

### 17.1 Numeric

- `n=3/FLOOR` boundary
- item-first와 line-first 결과가 달라지는 fixture
- decimal cost/distance/time rejection
- checked scale/multiplication/request/route/objective sum overflow
- 999 CBM 정규화와 provenance
- absent constraint가 numeric sentinel로 바뀌지 않음

### 17.2 Time과 service

- `[start,end)` plan과 inclusive close
- early arrival/customer waiting
- `START_ONLY` 대 `COMPLETE_WITHIN_WINDOW`
- repeating/overnight window expansion과 clip
- full-arc next-window restart, no partial driving
- `reqDate/dueDate` completion deadline와 alias conflict
- `duration + Σ(taskTime×qty)`
- depot/customer wait separation
- `waitInDepot`가 feasibility를 바꾸지 않음

### 17.3 Compatibility

- free-form case-sensitive size
- exact `["ALL"]`
- legacy alias conflict
- capability subset
- `ALL` zone와 one-concrete-zone route
- pickup/delivery size intersection과 zone conflict
- no-compatible-vehicle static `PROVEN`

### 17.4 Travel

- provided `D/U` priority
- decimal `D/U` rejection
- self `0/0`
- Great Circle `HALF_UP`
- vehicle-specific generated `U` `CEILING`
- missing speed `45 km/h`와 invalid-present speed rejection
- asymmetric directed arc
- no runtime lazy/reverse/symmetric generation
- solver/candidate verifier/result verifier의 prepared fingerprint equality

### 17.5 Stable state, propagation과 result

- pair/bank property tests
- same-vehicle, exactly-once, precedence corruption rejection
- mixed delivery-only/real-pair load prefix
- oneway/roundtrip terminal
- stop/location transition
- route-level drive resources와 work-window rest
- COW isolation, reject/interruption discard
- cache-free equality
- `TrialDraft → CompletedTrial → SearchSnapshot` 전이에서 불완전 상태 비노출
- `MaterializedSelectionDraft → EvaluatedSelectionCandidate → VerifiedSolution` 전이에서 verifier 권위 유지
- DIRECT/LEASE 모두 `ASSIGNED`
- required final audit와 confidence ceiling
- candidate/result verifier corruption rejection

### 17.6 ALNS

- destroy proposal의 ordered-unique request
- central pair edit 전후 route-bank exact partition
- repair complete/partial/no-feasible 각각 stable partition 유지
- cheap shortlist가 authoritative feasibility/objective가 아님
- exact insertion과 small-route brute-force oracle 일치
- operator reward/probability가 finite, positive, sum-one
- invalid/interrupted step이 completed-step/adaptive state를 전진시키지 않음
- comparator가 raw scalar cost로 대체되지 않음

### 17.7 Route pool

- rejected hard-feasible completed route admission
- invalid/interrupted route exclusion
- artifact와 projected column type/identity 분리
- append/import/reload의 동일 merge와 deterministic digest
- immutable no-alias
- incumbent pin
- safe dominance/Pareto oracle
- sealed snapshot의 stable enumeration/fingerprint
- acceptance commit과 pool-delta commit의 독립성

### 17.8 MIP와 hybrid

- tiny exact model의 request/vehicle row와 brute-force optimum
- explicit \(u_i\) unassigned와 mandatory objective cases
- lexicographic stage optimality 고정과 `FEASIBLE_LIMIT` 저순위 중단
- integer-to-backend coefficient range/round-trip
- warm-start column completeness와 row feasibility
- 모든 selection status × incumbent presence의 safe attribute access
- `BACKEND_UNAVAILABLE`/`NATIVE_RUNTIME_UNAVAILABLE` 구분과 CP-SAT status×incumbent matrix
- exact materialization의 complement bank와 backend \(u_i\) cross-check
- OGC conversion의 pair-atomic clone, empty-route removal, full recomputation과 exact partition
- general lexicographic profile에서 marginal scalar rule을 오용하지 않음
- backend/conversion/evaluation failure 뒤 ALNS incumbent fingerprint 불변
- strictly-better candidate만 next `AlnsWarmStart`
- backend model objective를 final comparator authority로 사용하지 않음
- optional과 required fallback이 다른 completion status를 만듦

### 17.9 Reproducibility

Strong reproducibility는 다음을 고정한다.

```text
problem/travel/policy/profile fingerprints
build/runtime compatibility
algorithm/operator/state strategy
seed derivation and actual seeds
round/run/warm-start lineage
stage maxSteps
stable iteration/reduction/tie-break
normal termination
hybrid pool-artifact/column/projection/conversion order
backend version/thread/seed/numeric/work-budget identity
```

Time-limited multi-worker CP-SAT는 strong class를 자동 만족하지 않는다. OR-Tools
version/platform, `num_workers`, `random_seed`, time/deterministic-work limit과 model
order를 manifest에 명시한다. 구체 숫자는 승인 전 `OPEN`이며 single-worker/fixed-seed
profile도 먼저 `PROPOSED TEST_ONLY`다. `TIMEBOXED_HYBRID`는 model/pool/conversion
identity를 보존하되 quality distribution과 fallback rate를 별도 evidence로 쓴다.

<a id="gates-traceability"></a>
## 18. Gate, deferred boundary와 추적성

### 18.1 `C-17`과 `RM-9A`~`RM-9C`

`C-17`은 route pool/MIP를 **GATED TARGET**으로 유지한다. Solver-neutral contract, fallback과 phase gate를 설계·review할 수 있지만 predecessor evidence와 별도 scope approval 전에는 구현 착수와 production default 활성화를 금지한다.

| Roadmap gate | Target | 필요한 evidence |
|---|---|---|
| `RM-9A` | immutable evaluated-artifact pool/snapshot | admission, merge, dominance, digest, memory, incumbent pin과 separate scope approval |
| `RM-9B` | solver-neutral selection + optional OR-Tools CP-SAT adapter | exact integer model, tiny oracle, CP-SAT hint/status/fallback, native packaging/load/temp cleanup, optimizer-free default build와 OR-Tools version/license-notice/SBOM 승인 |
| `RM-9C` | worker-local hybrid feedback/shadow | `RM-5` both-gate publication baseline, adopted-only feedback, optional/required fallback, reproducibility class, ALNS-only A/B |

순서는 verified ALNS baseline → `RM-9A` pool → `RM-9B` selector → `RM-9C` feedback/shadow다. 이 target이 문서에 상세하다는 사실은 current reactor에 vendor dependency를 추가하거나 default `SolvePlan`에서 MIP를 켤 권한이 아니다.

### 18.2 Deferred boundary

| Item | Current boundary | Resume requirement |
|---|---|---|
| Multi-trip/rotation | oneway + single roundtrip, pair crossing 금지 | exact trip/reset/depot/resource contract와 별도 승인 |
| Route pool/MIP production | §§12~14 target, OR-Tools CP-SAT policy, default off | `C-17`, `RM-9A~C`, verified baseline, measured value, OR-Tools version/config/native packaging/Apache-2.0 notice·SBOM/security/operations/compute-cost/admission/fallback/rollback 승인 |
| Physical topology | logical port와 run/round lineage | workload/security/retention/retry/cost evidence, `Q-INFRA-01` 승인 |
| Optional variants | pair, terminal, bank, travel contract 유지 | `Q-VAR-01` 선택·fixture·core-impact 승인 |

`Q-INFRA-01`과 `Q-VAR-01`은 `DEFERRED`다. `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`이며 공식 execution 수치를 막지만 logical fan-out/fan-in test를 막지 않는다.

### 18.3 Decision traceability

| Domain area | Question/gate | Master area |
|---|---|---|
| Numeric | `Q-NUM-01~03` | §7.2 |
| Travel | `Q-MTX-01~03` | §8 |
| Time/window | `Q-TIME-01~04` | §7.3 |
| Legacy input/service | `Q-IN-01~02`, `Q-BENCH-03` | §6~§8, §14 |
| Size/zone | `Q-COMP-01~02` | §5.3, §7.5 |
| Service pattern/trip | `Q-REQ-01~02` | §5~§6 |
| Profile/objective | `Q-OBJ-01~03` | §9 |
| Candidate/ALNS | `Q-ALG-01`, `Q-ALG-02` | §11~§12 |
| Pool/MIP | `C-17`, `P-15~P-19`, `RM-9A~C` | §11.7~§11.10 |
| Outcome/audit | `Q-RES-01~02` | §10, §14 |
| Benchmark | `Q-BENCH-01`, `Q-BENCH-03`; `Q-BENCH-02` experiment-required | §14 |
| Deferred | `Q-INFRA-01`, `Q-VAR-01` | §4, §16 |

Question status:

```text
RESOLVED 25
OPEN — EXPERIMENT_REQUIRED 1
DEFERRED 2
TOTAL 28
```

<a id="end-to-end-example"></a>
## 19. 처음부터 끝까지 보는 작은 예

입력:

```text
Vehicle V1
  capacityWeight = 10
  trips = roundtrip

Order O1 (delivery-only)
  demand = 3
  delivery = A

Request R2 (real pickup-delivery)
  demand = 4
  pickup = B
  delivery = C
```

1. Adapter는 `O1`을 delivery-only request로, `R2`를 real pair로 읽는다.
2. Item 수치는 exact decimal → `n=3/FLOOR` → qty 곱 순서로 정규화한다.
3. 시간은 `planStart` origin의 `long` second가 된다.
4. Size/capability/zone과 pair-level eligible vehicle를 계산한다.
5. 모든 location pair와 V1에 대해 prepared `D/U`를 완성한다.
6. `ProblemInstance`와 bound profile을 fingerprint로 동결한다.
7. Construction은 `O1`, `R2`를 route 또는 bank에 정확히 한 번 둔다.
8. 한 route가 둘을 맡으면 initial load는 3이다.

```text
depot(load 3)
→ B: R2 pickup(+4), load 7
→ A: O1 delivery(-3), load 4
→ C: R2 delivery(-4), load 0
→ depot
```

9. Propagation은 directed travel, windows, service, load prefix, stop과 drive resource를 계산한다.
10. Bound profile은 hard feasibility, neutral metrics와 lexicographic objective를 평가한다.
11. ALNS destroy는 request ID proposal을 만들고 central editor가 pair를 atomic하게 bank로 옮긴다.
12. Repair가 만든 `TrialDraft`는 exact partition과 full evaluation 뒤 `CompletedTrial`이 된다.
13. Acceptance 시에만 immutable `SearchSnapshot`으로 commit한다.
14. Full-evaluated route는 acceptance와 독립적으로 `EvaluatedRouteArtifact`가 될 수 있다.
15. Approved hybrid라면 sealed pool artifact를 projection해 `ProjectedRouteColumn`을 만든다.
16. Canonical MIP는 request마다 selected route coverage와 \(u_i\)의 합을 정확히 1로 만든다.
17. Selected artifact를 새 route/bank로 copy materialize하고 full-evaluate한다.
18. `EvaluatedSelectionCandidate`가 ALNS incumbent보다 strictly better일 때만 hybrid가 채택한다.
19. Final champion은 candidate verifier를 통과해 `VerifiedSolution`이 된다.
20. Finalization은 `ASSIGNED/UNASSIGNED`와 필요한 insertion audit를 만들고 result verifier가 publication payload를 검사한다.

<a id="customer-extension"></a>
## 20. 새 customer extension 절차

1. External schema/version과 exact customer profile을 정의한다.
2. Input 의미를 adapter에서 map하고 ALNS에서 customer name으로 분기하지 않는다.
3. Normalized fact를 재사용하고 가장 좁은 hard constraint/metric/score를 추가한다.
4. Objective dimension은 그 customer의 승인 preset에만 추가한다.
5. 기존 fact로 물리 의미를 표현할 수 없을 때만 propagation fact를 확장한다.
6. Exact dependency와 version/config hash를 solve 전에 bind한다.
7. Full recomputation, profile isolation과 verifier evidence를 추가한다.
8. 기존 customer regression과 result lineage를 보존한다.
9. Hybrid projection을 지원하려면 각 새 dimension을 exact projection class로 선언하고 oracle evidence를 제공한다.
10. `NON_PROJECTABLE`이면 production exact selector를 skip하고 silent surrogate를 만들지 않는다.

Architecture placement:

- Input/output 표현은 common/provider adapter의 typed package에 둔다.
- 기존 fact로 가능한 constraint/metric/score/objective는 customer profile JAR에서 core SPI를 구현한다.
- 실제 물리 진행 의미가 부족할 때만 core domain/propagation seam 변경을 ADR로 검토한다.
- 고객별 ALNS 동작은 solver 분기가 아니라 bound profile/operator contract로 전달한다.
- Profile JAR가 solver internal package나 provider SDK를 참조하면 architecture violation으로 차단한다.

<a id="summary"></a>
## 21. 핵심 요약

이 설계는 다음 경계를 끝까지 유지한다.

```text
external Order ≠ normalized Request
Request ≠ Node ≠ Visit ≠ Route
physical location ≠ solver node
delivery-only logical pickup ≠ physical pickup visit
hard feasibility ≠ neutral metric ≠ score/objective
TrialDraft ≠ CompletedTrial ≠ SearchSnapshot
evaluated route artifact ≠ projected MIP column
MaterializedSelectionDraft ≠ EvaluatedSelectionCandidate ≠ VerifiedSolution
search bank membership ≠ final UNASSIGNED outcome/diagnostic
backend model evidence ≠ authoritative full evaluation
logical execution ≠ physical infrastructure
```

기본 target은 immutable normalized problem, fully prepared directed travel, atomic request pair, route-bank XOR, COW trial과 two-gate publication이다. Canonical route selection은 concrete vehicle, explicit \(u_i\), exact request partition과 lexicographic objective를 보존한다. OGC `SET_COVER_THEN_CONVERT`는 complete-cover compatibility experiment이며 pair-atomic conversion과 full evaluation 뒤에만 canonical state로 돌아올 수 있다.

Optional ALNS–MIP는 immutable artifact/column 분리, fresh materialization, authoritative full evaluation, strictly-better adoption과 typed ALNS fallback을 유지한다. 그러나 `C-17`과 `RM-9A`~`RM-9C` evidence 및 별도 승인 전에는 production 기본이 아니다.
