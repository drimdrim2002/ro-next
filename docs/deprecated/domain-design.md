# RPDPTW Domain Design

```yaml
status: SUPERSEDED
version: 2.4-review
last_updated: 2026-07-28
owner: RPDPTW Domain·Input·Evaluation·Result 설계 역할
scope: Master Design의 도메인, 정규화, travel preparation, 해 상태, 전파, 평가와 결과 계약의 상세화
supersedes: 초안 v1.1 및 세션 29 이전의 legacy 상세 가정
related_designs:
  - architecture-design.md
related_decisions:
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
superseded_by: docs/domain-design.md
phase_c: path-and-status-only

```

<!-- phase-c-authority-banner -->
> **SUPERSEDED (Phase C)** — current authority: [`docs/domain-design.md`](../domain-design.md). This file is historical only. Do not use as conflict authority.


## 1. 문서 지위와 읽기 규칙

이 문서는 [Master Design](master-design.md)을 상세화하는 `REVIEW` 상태의 Domain Design이다. 구현 완료 보고가 아니며, Master보다 높은 conflict authority를 주장하지 않는다. [Architecture Design](architecture-design.md)은 이 문서의 의미 경계를 Java 25/Maven module과 package에 배치한다.

권위 순서는 Master §1을 따른다.

1. 채택된 외부 입력·출력 계약과 승인된 Decision Record
2. `APPROVED` Master
3. `APPROVED` 상세 설계
4. 현재 `REVIEW` 문서와 `master-design-sessions`
5. 연구·역사 자료

이 문서에서 질문 결정의 유일한 사용자 답변 근거는 [세션 29](master-design-sessions/29-open-question-interview.md)다. [질문 등록부](master-design-open-questions.md)는 28개 질문의 현재 상태·결정·evidence·gate를 제공하고, [세션 30](master-design-sessions/30-open-question-integration.md)은 Master 반영 기록, [세션 31](master-design-sessions/31-domain-design-integration.md)은 이 문서의 반영 기록이다.

이 문서가 domain 의미와 계층 책임을 소유하고 Architecture Design이 Maven/package 배치를 소유한다. Architecture Design의 `rpdptw-core`, `rpdptw-solver`, `rpdptw-verification`, `rpdptw-application`과 adapter 구조는 `RECOMMENDED`이며 승인된 public API 이름이 아니다. 사용자 답변으로 확정된 외부 field와 의미를 제외한 구체 Java 이름, package, wire DTO와 저장 표현도 승인된 API가 아니다.

2026-07-26 source inventory에서 실제 Java path는 입력을 해석하지 않는 합성 objective placeholder이며 ALNS, route pool, MIP와 독립 verifier를 구현하지 않는다. 따라서 이 문서의 “기본”, “stable”, “current”는 별도 current-state inventory를 명시하지 않는 한 **목표 domain contract**를 뜻한다. Route pool/MIP 절은 `C-17`의 production-activation gate 아래에 있는 구현 가능한 target이며 현재 코드 또는 approved default를 뜻하지 않는다.

## 2. 목표와 비범위

### 2.1 목표

도메인 구조는 다음을 동시에 만족해야 한다.

- Pickup-delivery pair, delivery-only와 실제 pickup-delivery 의미를 정확히 보존한다.
- 고객사별 constraint, metric, score와 objective를 공통 ALNS core 변경 없이 조립한다.
- Hot path는 immutable ID, 정수, 배열과 bitset 중심으로 계산한다.
- Numeric/time/travel 의미는 solve 시작 전에 정규화하고 fingerprint로 동결한다.
- Cached/incremental 평가와 cache-free 전체 재계산이 정확히 같다.
- Search state, final outcome, diagnostic과 verification을 분리한다.
- Oneway와 single roundtrip을 같은 core에서 명시적으로 처리한다.
- ALNS의 atomic destroy/repair, immutable evaluated route artifact, projected MIP column, route pool과 route-selection intermediate state를 stable solution/final outcome과 분리한다.
- MIP를 사용할 때 exact request/vehicle partition과 explicit unassigned 의미를 canonical domain에서 보존한다.

### 2.2 현재 비범위

- Multi-trip/rotation의 구현
- Route pool/MIP의 production 기본 활성화와 특정 optimizer 제품·라이선스 승인
- Pool cap/pruning, solver budget과 hybrid cadence의 실험 없는 공식값
- MDVRP·OVRP·SDVRP 구현
- 특정 cloud, orchestration product, storage와 deployment topology
- 동적 교통, 실시간 replanning, geocoding과 주소 정제
- `Q-BENCH-02`의 calibration/approval evidence 없는 공식 실행 수치

Multi-trip, infrastructure와 optional variant를 표현하기 위한 좁은 경계는 보존하되 활성화하지 않는다. Route pool/MIP는 §10.6~§10.9와 §12.5~§12.8에서 상세 target을 정의하지만 `RM-9A`~`RM-9C`와 별도 승인 전에는 default execution에 활성화하지 않는다.

## 3. 계층과 단방향 책임

```text
external bytes/reference
→ versioned input adapter
→ canonical business input
→ normalization
→ immutable ProblemInstance + prepared travel data
→ exact profile/config binding
→ immutable solve snapshot
→ portfolio/ALNS candidate state
→ [optional hybrid] evaluated route artifacts → sealed route pool → projected columns
→ route selection → reconstruction → authoritative full evaluation
→ candidate solution verifier
→ finalization audit + outcomes/diagnostics
→ result-integrity verifier
→ publishable result
```

| 계층 | 소유하는 것 | 소유하지 않는 것 |
|---|---|---|
| Adapter | Schema/version, alias, syntax, raw provenance | Feasibility, objective, silent fallback |
| Normalization | Numeric/time/service/location/compatibility 의미 | Search와 customer price |
| Travel preparation | Provided/generated `D/U`, complete coverage, provenance | ALNS 중 lazy fallback |
| Immutable domain | Dense IDs, pair, vehicle, node, location, prepared facts | 고객사 문자열과 transport |
| Propagation | Load/time/travel/resource의 물리 사실과 hard feasibility | 가격과 final diagnostic |
| Metrics | Policy-neutral 발생량 | 좋고 나쁨과 가격 |
| Bound profile | Constraint, score, objective/comparator, `SolvePlan` | Raw input 재해석 |
| Search state | Route sequence, bank membership, current/best | Final status와 final reason |
| Route pool | Exact-evaluated immutable route artifacts, merge/dominance와 discovery lineage | SearchRequestBank, live candidate mutation, final result |
| Route selection | Projection/model/warm-start/selected column intermediate | Stable solution, authoritative objective, publication |
| Finalization/result | Verified outcome, diagnostic, summary, provenance | Search mutation |

외부 adapter와 infrastructure implementation은 application orchestration/logical port를 통해 domain·evaluation·algorithm의 immutable contract를 소비한다. Core는 backend timezone, provider SDK, storage path와 customer name을 참조하지 않는다.

### 3.1 Architecture package mapping

[Architecture Design §5~§7](architecture-design.md#5-recommended-maven-multi-module-tree)은 위 논리 계층을 다음 Maven module/package에 배치한다.

| Domain 책임 | Maven 경계 | Package 경계 |
|---|---|---|
| Canonical input, immutable domain, normalization, travel | `rpdptw-core` | `input`, `domain`, `normalization`, `travel` |
| Propagation, policy SPI, binding와 insertion evaluation | `rpdptw-core` | `propagation`, `evaluation.api`, `evaluation.runtime`, `evaluation.insertion` |
| Portfolio, COW candidate와 ALNS | `rpdptw-solver` | `solver.portfolio`, `solver.state`, `solver.search`, `solver.termination` |
| Route pool, selection SPI와 conversion | `rpdptw-solver` | `solver.pool`, `solver.selection.api`, `solver.selection.conversion`, `solver.hybrid` |
| Google OR-Tools CP-SAT backend | optional/gated algorithm-backend adapter | Architecture가 정하는 `route-selection-ortools-cpsat` package; exported immutable domain/selection contract는 소비할 수 있으나 domain 의미 재해석·mutation 금지 |
| Candidate verification, finalization과 result verification | `rpdptw-verification` | `verification.candidate`, `result.finalization`, `verification.result` |
| Use case, logical port와 distributed execution identity | `rpdptw-application` | `application.port`, `application.service`, `application.execution` |
| Customer-specific composition | 독립 profile JAR | `profile.<stable_namespace>` |
| JSON/local mapping과 future infrastructure mapping | `adapters/common`과 향후 승인된 provider adapter | `adapter.json`, `adapter.local`, 승인 후 확정할 provider package |

Domain, normalization, travel, propagation과 evaluation이 같은 `rpdptw-core` Maven module에 있어도 이 절의 단방향 의미 책임은 합쳐지지 않는다. Package-private visibility와 architecture test가 다음 package 규칙을 강제해야 한다.

```text
input/domain → normalization/travel
domain → evaluation.api
domain/travel/evaluation.api → propagation
propagation/evaluation.api → evaluation.runtime/insertion
```

`evaluation.insertion`은 side-effect-free option evaluation까지만 소유한다. COW mutation, acceptance와 adaptive state는 `rpdptw-solver`에만 둔다. `rpdptw-verification`은 `rpdptw-core`를 사용할 수 있지만 `rpdptw-solver`와 search cache에 compile-depend하지 않는다.

## 4. Canonical business input

### 4.1 Plan envelope

Canonical solve input은 최소한 다음 의미를 제공한다.

```text
plan identity
planStart
planEnd
depot/location
depot open/close and duration
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

### 4.2 Order/request와 item

Delivery-only order의 canonical 입력은 다음 의미를 갖는다.

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
mandatory flag when supported by the profile
customer extension
```

실제 pickup-delivery request는 pickup과 delivery의 location, window, duration, size/zone restriction을 각각 제공한다. 같은 request의 두 작업은 하나의 immutable pair로 bind한다.

`reqDate`와 `dueDate`는 같은 서비스 완료기한의 legacy 별칭이다.

```text
serviceStart <= serviceEnd <= reqDate(dueDate)
```

- 두 alias가 함께 있으면 같은 normalized value여야 한다.
- Release date나 “이 날짜 이후 배송”으로 해석하지 않는다.
- Plan end와 같더라도 plan의 제외 경계를 늘리지 않는다.

`duration`과 `taskTime`은 별칭이 아니다.

```text
serviceTime
= request.duration
  + Σ(item.taskTime × item.qty)
```

- `duration`: 차량 진입 등 order/request 수준의 고정 서비스시간
- `item.taskTime`: item 한 단위의 선적시간
- `qty`: 양의 정수
- Legacy order-level `taskTime`: item에 배분하거나 `duration`으로 바꾸지 않고 입력 오류

### 4.3 Vehicle

Vehicle input은 최소한 다음 의미를 제공한다.

```text
vehicleId
maxWeight
maxVolume
vehicleFeature
capabilities
zoneId
vhclOwnTyp
workStart / workEnd
speed
maxStopCnt
maxDriveTime
maxDriveDist
terminal policy binding
```

`vhclOwnTyp`은 optional이다.

| Raw value | Normalized ownership |
|---|---|
| missing, `null`, empty | `DIRECT` |
| exact `DIRECT` | `DIRECT` |
| exact `LEASE` | `LEASE` |
| 그 밖의 non-empty value | Input error |

값은 대소문자를 구분한다. `DIRECT/LEASE`는 차량 자원의 소유 유형이며 request outcome status가 아니다.

### 4.4 Depot와 trip options

- `depot.taskTime`은 시간 전파에 적용하지 않는다.
- Depot의 회차 간 작업시간은 `depot.duration`이다.
- 최초 출발과 마지막 복귀에는 depot duration을 적용하지 않는다.
- 향후 rotation에서 한 trip 복귀 후 다음 trip 재출발 전에만 적용한다.

Trip option은 현재 다음만 허용한다.

| Input | Current meaning |
|---|---|
| `trips=oneway` | Depot에서 한 번 출발하고 마지막 고객에서 종료. `multiRotation` 값은 무시 |
| `roundtrip + multiRotation=0` | Depot에서 출발해 고객을 방문하고 같은 depot으로 한 번 복귀 |
| Non-oneway + `multiRotation != 0` | `UNSUPPORTED_INPUT` |

Oneway에서 무시한 raw `multiRotation`은 provenance에 남긴다.

## 5. Numeric normalization

### 5.1 차원별 계약

| 차원 | 외부 계약 | 내부 표현 |
|---|---|---|
| 무게 | Exact decimal kg, `n=3`, 비음수 `FLOOR` | `long`, kg × 1,000 |
| 부피 | Exact decimal CBM, `n=3`, 비음수 `FLOOR` | `long`, CBM × 1,000 |
| 비용 | Integer only | Checked integer |
| 거리 | Integer meter only | Checked integer meter |
| 시간 | Integer second only | Checked `long` second |
| 수량 | Positive integer | Checked integer |

Decimal은 IEEE-754 `double`로 먼저 근사하지 않고 원문 10진수로 읽는다.

### 5.2 Item-first 순서

```text
raw item weight/volume
→ exact decimal validation
→ n=3 FLOOR
→ normalized integer × qty
→ request sum
→ route/solution checked sum
```

Line decimal 합계를 먼저 만든 뒤 한 번만 내리는 방식은 사용하지 않는다. Demand와 capacity는 같은 scale과 rounding을 사용한다.

### 5.3 Error와 overflow

- 비용·거리·시간의 소수 입력은 반올림·절삭하지 않고 거부한다.
- 음수 demand/capacity/time/distance와 비유한 값은 거부한다.
- Scale, item × qty, request sum, route sum과 objective sum은 checked arithmetic을 사용한다.
- Overflow를 포화값, wraparound 또는 도메인 sentinel로 바꾸지 않는다.
- 부피 차원을 사용하지 않는다고 adapter가 명시한 vehicle의 volume capacity는 유한한 `999 CBM`으로 정규화한다.
- 누락 resource limit는 큰 수가 아니라 explicit “constraint absent”다.

Policy ID/version, unit, scale, rounding과 normalization order를 instance/result fingerprint에 포함한다.

## 6. Time normalization과 service windows

### 6.1 Core time axis

Adapter는 exact date-time string을 parsing하고 planning origin 기준 `long` second로 변환한다.

```text
origin = normalized planStart
normalizedTime = seconds from origin
```

Core는 `ZoneId`, UTC offset, DST, `LocalDateTime`과 input string을 다루지 않는다.

### 6.2 Boundary rules

- Plan: `[planStart, planEnd)`
- Window open: 포함
- Window close: 포함
- Close를 1초 줄이거나 plan end를 포함으로 바꾸지 않는다.
- Feasible time과 `INFEASIBLE(reason)`은 별도 타입/상태로 표현한다.

### 6.3 Customer service

```text
serviceStart = max(arrival, openTime)
customerWaiting = serviceStart - arrival
serviceEnd = departure = serviceStart + serviceTime
```

Default window policy는 `START_ONLY`다.

```text
serviceStart <= closeTime
```

Profile은 `COMPLETE_WITHIN_WINDOW`를 선택할 수 있다.

```text
serviceEnd <= closeTime
```

Waiting은 neutral metric이며 현재 objective에 자동 포함하지 않는다.

### 6.4 Repeating and overnight windows

- 날짜 없는 customer/depot/vehicle window는 plan에 포함되는 각 날짜에 반복한다.
- `openTime > closeTime`은 `D open → D+1 close`의 하나의 overnight window다.
- 두 독립 window나 빈 window로 바꾸지 않는다.
- Expansion 결과는 `[planStart, planEnd)`로 clip한다.
- 한 반복 window를 놓치면 plan 안의 다음 window까지 기다릴 수 있다.

`openTime == closeTime`의 24시간/0시간 의미는 별도 명시적 schema 없이는 모호하므로 adapter가 거부한다. 24시간 운영은 승인된 별도 표현을 사용해야 한다.

### 6.5 Vehicle work window와 travel

Arc는 다음 조건에서만 현재 work window에 출발할 수 있다.

```text
departure + fullTravelTime <= currentWorkEnd
```

현재 window에 전체 arc가 들어가지 않고 plan 안에 다음 work window가 있으면:

1. 현재 location에서 쉰다.
2. 다음 `workStart`에 같은 arc 전체를 처음부터 시작한다.
3. 중간까지 운전한 거리를 누적하지 않는다.

Arc 중간 pause/resume은 금지한다. 전체 travel time이 어떤 단일 available work window에도 들어가지 않거나 다음 window가 plan 안에 없으면 infeasible이다.

Inter-work-window rest는 drive/customer-wait에서 제외하고 별도 rest와 route operational time에 포함한다.

### 6.6 `waitInDepot`

```text
earliestDeparture = max(vehicleWorkStart, depotOpen)
```

- `N`: earliestDeparture에 출발하고 첫 고객에서 필요하면 기다린다.
- `Y`: `max(earliestDeparture, firstCustomerOpen - travelTime)`으로 같은 조기 대기를 depot으로 옮긴다.

이 옵션은 hard feasibility를 완화하거나 customer wait을 없애지 않는다. Depot waiting과 customer waiting을 별도 metric으로 기록한다.

## 7. Vehicle size, capability와 zone

### 7.1 Size field contract

Vehicle:

```text
vehicle.vehicleFeature = one concrete non-empty code
```

Order:

```text
order.vehicleFeatureList
= one or more concrete codes
  or exactly ["ALL"]
```

- Vehicle missing/null/empty/`"ALL"`: input error
- Order missing/null/empty array: input error
- `["ALL", "T1"]`: input error
- Size code: free-form, case-sensitive exact string
- Fixed registry/allowlist 없음
- Fleet에 없는 order code: input error가 아니라 eligible vehicle을 만들지 않음

Legacy 배열형 `order.vehicleFeature`는 versioned alias로 읽을 수 있다. 신규 field와 함께 있으면 exact list equality가 필요하고, legacy 단일 string order field는 거부한다.

### 7.2 Capability

Size와 냉장·lift·위험물·기사 자격을 generic feature 하나로 합치지 않는다.

```text
sizeCompatible
= vehicle.vehicleFeature in request.vehicleFeatureList
  or request list == ["ALL"]

capabilityCompatible
= request.requiredCapabilities
   subsetOf vehicle.capabilities
```

실제 pickup과 delivery의 size list가 다르면 같은 vehicle이 두 작업을 수행하도록 교집합을 사용한다.

### 7.3 Zone

Vehicle/order zone의 missing/null/empty는 `"ALL"`로 정규화한다. `"ALL"` 외 code는 free-form case-sensitive exact string이다.

Route의 구체 zone 집합:

```text
concreteRouteZones
= { order.zoneId | visited order and zoneId != "ALL" }
```

Hard rule:

```text
size(concreteRouteZones) <= 1
```

- Zone-neutral vehicle(`ALL`)은 임의의 한 구체 zone route 또는 ALL-only route를 수행할 수 있다.
- Concrete-zone vehicle은 같은 concrete zone과 `ALL` order만 방문한다.
- `서울 → ALL → 서울`: 가능
- `서울 → ALL → 경기도`: 불가능
- 실제 pickup/delivery의 concrete zone이 다르면 request는 배정 불가
- Delivery-only depot logical pickup은 zone 방문이 아님

Size와 zone은 독립 hard constraint이며 AND로 적용한다.

## 8. Travel Matrix preparation

### 8.1 Identity와 authority

Travel key는 solver node가 아니라 physical location ID다. 여러 solver node가 같은 physical location을 참조할 수 있다.

```text
solver node
→ physicalLocationIndex
→ prepared directed travel
```

Provided fields:

| Field | Meaning |
|---|---|
| `D` | Authoritative directed integer meter |
| `U` | Authoritative directed integer second, vehicle-independent |
| `C` | Non-authoritative; feasibility/score/generation에 사용하지 않음 |

소수 `D/U`는 거부한다. 현재 `data/win_poc_case.json`의 소수 `D/U`는 이 계약에 비준수이며 정수 matrix를 다시 받거나 별도 명시적 계약 변경 전 official benchmark에 사용할 수 없다.

### 8.2 External coverage와 preparation

External input은 sparse arc 또는 matrix 전체 생략을 허용한다. 명시적 preparation은 solve 전에 모든 physical location directed `M²` pair를 해소한다.

Priority:

1. Provided `D/U`
2. Missing `D` generation
3. Missing `U` generation

Self arc는 raw value와 관계없이:

```text
D = 0 meter
U = 0 second
```

### 8.3 Missing `D`

- 좌표 기반 Great Circle function을 사용한다.
- 소수 meter 결과를 `HALF_UP`으로 가장 가까운 integer meter로 만든다.
- 필요한 coordinate가 없으면 solve 전 input error다.
- Reverse arc를 복사하거나 대칭 평균하지 않는다.

Great Circle의 구체 함수/version은 configuration과 fingerprint에 포함한다. 이 문서는 승인되지 않은 Earth radius나 library default를 만들지 않는다.

### 8.4 Missing `U`

```text
generatedUSeconds
= CEILING(D_meter × 3.6 ÷ speed_km_h)
```

- Vehicle speed가 없으면 `45 km/h`
- Provided `U`는 vehicle과 무관한 authoritative common time
- Missing `U`는 vehicle별 생성 time
- Present but non-positive/invalid speed는 missing으로 조용히 바꾸지 않고 validation error

Prepared representation은 common provided `U`와 vehicle-resolved generated `U`를 구분해야 한다. 구체 class/array layout은 잠정이지만 solver 시작 전 모든 사용 vehicle/pair 시간이 해소되어야 한다.

### 8.5 Runtime rule와 provenance

Solver core, candidate verifier와 result verifier는 prepared travel data만 사용한다.

금지:

- Search 중 coordinate/speed lazy calculation
- Missing arc의 reverse lookup
- 대칭화
- Provided/generated source 혼동
- Solver와 verifier가 다른 preparation 구현 사용

보존:

- Raw input digest
- Location mapping
- Provided/generated source per value
- Great Circle function/version
- Speed source와 default 사용 여부
- Rounding formula
- Coverage/diagonal policy
- Prepared travel fingerprint

## 9. Immutable solver model

### 9.1 Dense identity mapping

Normalization result는 external ID와 dense core ID의 양방향 mapping을 가진다.

```text
RequestId
VehicleId
SolverNodeId
PhysicalLocationId
```

각 identity는 다른 의미다. 같은 physical location의 pickup, delivery와 terminal node를 합치지 않는다.

### 9.2 Request와 service pattern

각 request는 pickup node와 delivery node 하나씩을 참조한다.

```text
Request
  id
  pickupNodeId
  deliveryNodeId
  demandWeight
  demandVolume
  servableVehicles
  servicePattern
```

Service pattern의 책임:

| Pattern | Pickup meaning |
|---|---|
| Delivery-only | Route 출발 전 적재된 화물의 logical start loading |
| Real pickup-delivery | 실제 location/time/service를 가진 pickup |

Prefix node 대 initial-load state의 최종 내부 표현은 잠정이다. 어느 표현도 travel/stop을 만들거나 pair invariant를 약화할 수 없다.

### 9.3 Vehicle

Normalized vehicle은 다음 의미를 갖는다.

```text
capacityWeight
capacityVolume
vehicleFeature code
capabilities
normalized zone
ownership DIRECT/LEASE
work windows
terminal/trips policy
optional maxStop/maxDriveTime/maxDriveDist
prepared travel-time view
```

누락 limit는 `Optional/AbsentConstraint` 의미로 표현한다. 큰 numeric sentinel을 사용하지 않는다.

### 9.4 Terminal policy

- 모든 current route는 start depot/terminal에서 시작한다.
- Oneway route는 마지막 service node에서 끝난다.
- Single roundtrip은 같은 depot/end terminal에서 끝난다.
- 내부 depot revisit는 current scope에서 금지한다.
- Node layout은 terminal policy를 명시적으로 표현해야 하며 모든 route에 end terminal이 있다고 가정하지 않는다.

### 9.5 Problem instance

Immutable `ProblemInstance`는 최소한 다음을 bind한다.

```text
dense IDs and external mappings
requests/nodes/vehicles
physical locations
prepared travel data
numeric/time/service policies
compatibility facts
profile dependencies
provenance and fingerprints
```

생성 시 다음을 검증한다.

- ID/index bijection과 array length
- Pair/node/location reference
- Numeric/time boundaries와 checked ranges
- Prepared travel completeness
- Vehicle-resolved missing `U` completion
- Size/capability schema
- Pair static compatibility facts
- Profile dependency closure

호환 vehicle이 없는 request는 구조 input error가 아니다. Static precheck에서 `PROVEN` unassignability evidence를 만들고 search bank에 남길 수 있다.

## 10. Stable solution, request bank와 COW

### 10.1 Stable partition

모든 stable state에서 request는 정확히 하나다.

```text
ASSIGNED_IN_SEARCH
= exactly one route owns RequestId
  and every required physical service visit is complete and ordered
  and a delivery-only logical pickup is represented by that route's initial-load ownership
  and request not in SearchRequestBank

UNASSIGNED_IN_SEARCH
= no route owns RequestId and no physical service visit is present
  and request in SearchRequestBank exactly once
```

Partial pair, duplicate, split vehicle, reverse precedence, route+bank 중복과 양쪽 누락은 infeasible candidate가 아니라 implementation defect다.

### 10.2 Route invariants

- Exactly one vehicle
- Terminal policy 일치
- Approved service pattern
- Pair completeness and precedence
- 모든 load prefix에서 `0 <= load <= capacity`
- Prepared directed travel만 사용
- Time/window/resource hard feasibility
- Internal depot revisit 없음

Delivery-only와 real pickup-delivery는 같은 single-trip route에 혼합할 수 있다.

```text
initialLoad
= Σ(demand of assigned delivery-only requests)
```

- Delivery-only customer: load 감소
- Real pickup: load 증가
- Real delivery: load 감소
- Delivery-only 보충을 위한 중간 재상차 금지

### 10.3 SearchRequestBank

Bank는 request ID membership만 가진다.

저장 금지:

- Node
- Cost
- Last insertion failure
- Final status
- Diagnostic
- Outsourced/deferred 의미

### 10.4 Candidate state

`Q-ALG-02`는 `RESOLVED — KEEP_COW`다. 목표 구현의 기본은 copy-on-write이며 현재 placeholder 구현 상태를 뜻하지 않는다.

```text
committed immutable current
→ copy changed routes + independent bank
→ invalidate derived state
→ validate/evaluate
→ accept: freeze as new current
→ reject/fail/interrupt: discard whole candidate
```

`current`, `stageBest`, `solveBest`를 candidate가 직접 변경하지 않는다.

Apply/undo는 기본 경로가 아니다. COW copy/allocation/GC가 실제 병목으로 측정되고 별도 변경이 승인된 경우에만 round-trip, fault, trace, verifier 동등성 실험을 제안할 수 있다.

### 10.5 Cache

Source of truth:

```text
route sequence
request ownership
vehicle/terminal binding
SearchRequestBank
```

Derived:

```text
arrival/service/departure/load
travel/resource aggregates
metrics/score/objective
insertion tables
fingerprints
```

Mutation은 영향 cache와 fingerprint를 무효화한다. Cache-free full recomputation과 정확히 같지 않으면 candidate를 정상 비교·게시할 수 없다.

### 10.6 ALNS search state와 iteration outcome

범용 `candidate`라는 한 단어로 construction, COW trial, MIP intermediate와 verified solution을 섞지 않는다.

| Type | 의미 | Stable solution인가 |
|---|---|---:|
| `ConstructionCandidate` | Initial portfolio가 만든 독립 초기 route/bank snapshot | Cache-free validation 뒤 예 |
| `SearchSnapshot` | Immutable routes-by-vehicle, bank, authoritative evaluation과 fingerprint | 예 |
| `TrialDraft` | 한 ALNS step 안에서만 mutable한 changed-route COW 작업 상태 | 아니오 |
| `CompletedTrial` | Structural exactness와 authoritative full evaluation을 마친 freeze 직전 결과 | 아니오; commit 전 |
| `RouteSelectionIncumbent` | Backend의 raw status/termination/provenance와 selected projected-column IDs only | 아니오 |
| `MaterializedSelectionDraft` | Selected projected columns를 새 routes/bank로 복사한 full-evaluation 전 결과 | 아니오 |
| `EvaluatedSelectionCandidate` | Structural exactness와 authoritative full evaluation을 통과한 selector 후보 | 예; verifier 전 |
| `VerifiedSolution` | 독립 candidate verifier가 `PASS`한 immutable solution | 예, publication 전 단계 |

ALNS state의 최소 의미는 다음과 같다.

```text
AlnsSearchState
  current: SearchSnapshot
  stageBest: SearchSnapshot
  solveBest: SearchSnapshot
  progress: completedStep/stage/alnsRun
  operatorLearning: immutable snapshot
  acceptanceState: immutable snapshot
  rngLineage
  routePoolSnapshotId?   // hybrid mode일 때만
```

- `current`, `stageBest`, `solveBest`는 서로 alias되지 않는다.
- `TrialDraft`만 step 안에서 mutable하고 commit/discard 뒤 참조되지 않는다.
- `CompletedTrial`은 `SearchSnapshot`으로 commit되거나 폐기된다. 문서에서 stable candidate란 `SearchSnapshot` 또는 verifier 이후 `VerifiedSolution`만 뜻한다.
- Operator/history/acceptance state는 completed step 뒤에만 새 immutable snapshot으로 교체한다.
- `RESET_EACH_ALNS_PHASE`와 `CARRY_ACROSS_HYBRID_PHASES`는 route pool persistence와 별도 policy다.
- Solution comparison은 raw cost가 아니라 bound comparator를 사용한다.

Iteration outcome은 `GLOBAL_BEST_IMPROVED`, `CURRENT_IMPROVED`, `ACCEPTED_NON_IMPROVING`, `REJECTED`, `INVALID_CANDIDATE`, `INTERRUPTED`를 구분한다. Reward policy는 이 enum을 consume하며 `INVALID_CANDIDATE`와 `INTERRUPTED`가 일반 reject 보상이나 completed-step을 만들 수 없다.

### 10.7 Destroy와 repair 결과

Destroy operator는 실제 route mutation이 아니라 제거 제안을 만든다.

```text
DestroyProposal
  operatorId/version
  requestedRemovalCount
  ordered unique RequestId list
  source RouteId set
  score/tie evidence
  decisionTraceDigest
```

중앙 atomic editor가 proposal을 적용해 다음 `DestroyResult`를 만든다.

```text
DestroyResult
  actuallyRemovedRequests
  changedRouteIds
  postDestroyTrial
  before/after structural fingerprints
```

각 제거 request는 적용 전 route에 정확히 한 번 배정돼 있어야 하고, 적용 뒤 pickup/delivery가 모두 사라져 bank에 정확히 한 번 존재해야 한다. Worst/Semi-worst operator도 route/fleet/cost를 직접 고치지 않으며 removal ranking만 제안한다. Mutable route list index 대신 stable `RouteId`를 쓴다.

Repair 결과는 다음 sealed 의미를 갖는다.

```text
COMPLETE_REINSERTION
PARTIAL_REINSERTION
NO_FEASIBLE_INSERTION
DEFECT
```

```text
RepairResult
  scope: REMOVED_ONLY | REMOVED_PLUS_EXISTING_BANK
  attemptedRequests
  appliedInsertions
  requestsRemainingInBank
  changedRouteIds
  postRepairTrial
  routeCollectionCandidates
```

Partial/no-feasible 결과도 route/bank exact partition과 hard feasibility를 만족하면 정상 `CompletedTrial`로 freeze할 수 있다. `DEFECT`는 evaluation·acceptance로 진행할 수 없다.

### 10.8 Repair shortlist와 authoritative insertion

Cheap ranking type과 exact insertion type을 분리한다.

```text
RepairRouteCandidate
  requestId
  targetRouteId | NEW_ROUTE
  rankBasisVehicleId
  promisingScore
  staticGate
  routeFingerprint

InsertionEvaluation
  FEASIBLE(InsertionOption)
  | REJECTED(typed constraint evidence)
```

`InsertionOption`은 request, target route 또는 `NEW_ROUTE`, concrete vehicle binding/rebinding plan, pickup/delivery position, 새 ordered service sequence, exact propagated facts, objective delta와 new route fingerprint를 가진다.

- Real pickup-delivery는 모든 합법 `pickupPosition < deliveryPosition` 조합을 평가할 수 있다.
- Delivery-only logical pickup은 가짜 travel/stop/service 위치를 만들지 않는다.
- OGC의 all-pickups-first/all-deliveries-later 제약은 canonical invariant가 아니며 legacy operator 안에만 격리할 수 있다.
- `NEW_ROUTE`는 실제 unused `VehicleId`를 소비한다. Vehicle type/count sentinel을 사용하지 않는다.
- Promising score는 shortlist용이고 feasibility·objective·diagnostic authority가 아니다.
- Shortlist miss는 품질 문제이지 stable solution의 feasibility 위반이 아니다.

### 10.9 Route identity, column과 pool

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

Pool은 MIP projection 이전의 evaluated route artifact를 보존한다. `verified`는 독립 verifier 결과에만 사용하며 pool admission과 혼용하지 않는다.

```text
EvaluatedRouteArtifact
  RouteArtifactId
  RouteSignature
  RouteCoverageKey
  immutable RoutePlan
  RequestCoverage
  ExactRouteEvaluation
  discovery lineage
  evaluation fingerprint

ProjectedRouteColumn
  ProjectedColumnId
  RouteArtifactId
  exact request/vehicle/resource row coefficients
  exact objective coefficients
  projection fingerprint
```

Pool 불변조건:

1. 모든 artifact는 nonempty, pair-complete, hard-feasible이고 같은 problem/travel/profile authority에 속한다.
2. `RoutePlan`과 evaluation은 immutable이며 pool route를 변환·후처리하면서 수정하지 않는다.
3. 같은 signature/authority의 duplicate는 evaluation·coverage가 같을 때 lineage만 deterministic merge한다. 값이 다르면 어느 하나를 고르지 않고 artifact integrity defect로 거부한다.
4. Base pool은 같은 coverage라는 이유만으로 artifact를 제거하지 않는다. Bound profile의 versioned route-fact dominance가 모든 소비 경로에 안전함을 증명한 경우만 pool-level 제거할 수 있다. 특정 projection의 projected-column pruning은 같은 concrete vehicle에서 objective뿐 아니라 모든 hard/resource/linearization row coefficient까지 component-wise dominance를 확인할 때만 허용한다. 그 밖에는 nondominated frontier 또는 conservative retention을 사용한다. Vehicle-class aggregation은 별도 model mode와 equivalence proof가 필요한 기능이며 이 bucket에 섞지 않는다.
5. Append/import/reload는 같은 validation, dominance와 tie-break를 사용한다.
6. Incumbent artifacts는 snapshot pruning에서 보호한다.
7. Live pool은 selector 입력이 아니다. Stable order로 seal한 `RoutePoolSnapshot`만 전달한다.
8. 다른 fingerprint의 snapshot/delta import는 input error가 아니라 hybrid artifact integrity failure다.

`RoutePoolDelta`는 candidate acceptance와 독립된 append-only derived artifact다. Hard-feasible full evaluation을 마친 `CompletedTrial`의 route는 전체 candidate가 reject되어도 수집할 수 있지만, interrupted/invalid draft의 route는 수집할 수 없다. 이 delta의 commit은 `current` commit과 별도이며 committed route/bank를 바꾸지 않는다.

## 11. Route propagation과 resources

### 11.1 One-pass state

Route propagation은 stable sequence를 앞에서 뒤로 계산한다.

```text
terminal/start state
→ resolve full travel in a work window
→ arrival
→ customer/depot waiting
→ service start
→ service end/departure
→ load change
→ stop/resource accumulation
→ next leg
```

최소 neutral facts:

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

### 11.2 Stop count

Stop은 assigned request 수나 unique location count가 아니다.

```text
if current customer service location
   != immediately previous customer service location
then stopCount += 1
```

- 연속 same location orders: 첫 진입만 증가
- `A → B → A`: 세 번의 location 진입/전환을 각각 계산
- 시작/종료/middle depot와 logical pickup: 제외
- Route 전체 누적
- 날짜/근무창/rest에서 reset하지 않음

Vehicle limit와 global `Optimizer.VehicleMaxStopCount`가 함께 있으면 포함 상한의 `min`을 사용한다. 하나만 있으면 그 값을, 둘 다 없으면 추가 stop constraint를 두지 않는다.

### 11.3 Drive resources

```text
driveDist = Σ(actual traversed D_meter arcs)
driveTime = Σ(actual traversed vehicle-resolved U_second arcs)
```

포함:

- Start depot → first customer
- Customer-to-customer
- Roundtrip final customer → depot
- 향후 승인된 rotation의 실제 depot arcs

제외:

- Customer/depot waiting
- Request/depot service
- Inter-work-window rest

`driveTime <= maxDriveTime`, `driveDist <= maxDriveDist`의 포함 상한이다. Route 전체 누적이고 날짜/rest에서 reset하지 않는다.

### 11.4 Multi-trip boundary

Multi-trip/rotation은 current scope가 아니다. 향후 활성화해도:

- Pair는 하나의 trip 안에서 완료
- Delivery-only request도 하나의 trip에 완전히 속함
- Trip 종료 시 미완료 load 없음
- 다음 trip은 승인된 depot duration 뒤 새 trip load로 시작

`multiRotation` 값, reset, depot window와 route representation은 별도 승인을 요구한다.

## 12. Evaluation, profile과 objective

### 12.1 Layer separation

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
- Metric은 가격과 선호를 포함하지 않는다.
- Score는 raw route/input을 재해석하지 않는다.
- Comparator는 physical propagation을 다시 계산하지 않는다.
- `SolvePlan`은 hard rule을 해제하지 않는다.

### 12.2 Bound profile

Long-lived profile definition/registry와 solve-bound immutable profile을 분리한다.

Binding:

1. Exact customer profile key/version resolve
2. Objective preset resolve
3. Normalized facts/IDs/unit/schema에 dependencies bind
4. Missing metric, duplicate key, unit mismatch, unknown reference reject
5. Immutable bound snapshot과 fingerprint 생성

Solve 요청은 해당 고객사에 등록·승인된 preset만 선택할 수 있다. Objective order, weight와 formula를 직접 주입할 수 없다. Preset 생략 시 customer config에 exact key/version으로 지정된 default를 사용한다.

특정 objective/metric은 일부 고객사에만 존재할 수 있다. 다른 고객사의 dimension을 fallback하지 않는다.

Architecture 배치는 다음 분리를 보존한다.

- Constraint/metric/score/objective/profile SPI와 binder는 `rpdptw-core`의 `evaluation.api`와 `evaluation.runtime` package가 소유한다.
- Standard/customer 구현과 preset composition은 별도 profile JAR가 소유한다.
- `rpdptw-core`는 구체 profile JAR를 compile-depend하지 않는다.
- Worker application assembly가 배포 bundle의 provider를 등록하고 binder가 exact customer/profile/version/preset을 검증한다.
- Customer name이나 preset label을 `rpdptw-core`, `rpdptw-solver` 또는 verifier의 분기 조건으로 사용하지 않는다.

### 12.3 Mandatory와 ownership objectives

Customer preset이 mandatory를 지원하면:

```text
mandatoryUnassignedCount
```

가 최상위 lexicographic objective다. Hard constraint나 finite penalty가 아니다. 0이 불가능하면 최소 양수의 verified partial solution이 존재할 수 있다.

Vehicle volume objective:

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

- `regularVehicleVolumeCost`는 기본 dimension
- `LEASE`를 사용하는 customer preset만 outsourced dimension 추가
- LEASE vehicle이 입력됐지만 preset이 outsourced objective를 지원하지 않으면 bind error
- 고정 `1:100`, 음수 score 또는 instance Big-M로 strict priority를 흉내 내지 않음

### 12.4 Portfolio와 phase configuration boundary

`Q-ALG-01`의 현재 계약은 4개 request-route 성장 정책(`CLOCK`, `SEQ_FARTHEST`, `SEQ_LARGE_DEMAND`, `SEQ_EARLIEST_DEADLINE`)과 2개 `DIRECT`-first vehicle 순서(`DIRECT_FIRST_LARGE`, `DIRECT_FIRST_SMALL`)를 조합한 최대 8개 독립 candidate다. 모든 construction은 같은 side-effect-free atomic pair evaluator와 bound comparator를 사용한다.

각 available candidate는 exact `screenMaxSteps`의 phase-1 ALNS screen을 `MAX_STEPS_REACHED`로 완료하고, cache-free validation 뒤 stable comparator로 하나의 phase-1 champion을 고른다. Phase 2는 그 champion만 공통 warm start로 사용해 declared worker batch를 실행한다. `screenMaxSteps`, `phase2MaxSteps`, worker 수, `maxRounds`, watchdog의 공식 수치만 `Q-BENCH-02` calibration 전까지 없다. Domain config는 이 값에 hidden official default를 제공하지 않는다.

### 12.5 Route-selection projection과 exact partition

MIP에 전달하는 값은 arbitrary objective expression이 아니라 solve-bound `RouteSelectionProjection`이다.

```text
RouteSelectionProjection
  projectionVersion/fingerprint
  modelMode
  request mapping
  concrete vehicle/resource mapping
  ordered objective dimensions
  route/unassigned coefficient encoders
  supported hard constraints
  coefficient unit/range declaration
```

Projection binder는 각 profile dimension을 다음 중 하나로 분류한다.

- `ROUTE_ADDITIVE`: route column coefficient의 합으로 정확 표현
- `UNASSIGNED_ADDITIVE`: request별 unassigned coefficient의 합으로 정확 표현
- `EXACT_LINEARIZATION`: versioned 추가 변수/제약 encoder가 정확 표현
- `NON_PROJECTABLE`: 현재 selector에서 정확 표현 불가

`NON_PROJECTABLE` dimension이나 solution-level hard constraint를 조용히 빼거나 surrogate coefficient로 바꾸지 않는다. Production exact mode는 route selection을 `SKIPPED_NON_PROJECTABLE_PROFILE`로 끝낸다. 별도 `SURROGATE_EXPERIMENT_ONLY` mode를 승인할 수는 있지만 exact mode와 다른 algorithm/projection fingerprint를 사용하고, 결과를 production adoption authority로 취급하지 않는다.

Corrected exact-partition 모델의 집합과 변수는 다음과 같다.

- \(I\): input request 집합
- \(R\): 한 `RoutePoolSnapshot`에서 exact projection으로 생성한 `ProjectedRouteColumn` 집합
- \(V\): concrete input vehicle 집합
- \(a_{ir}=1\): column \(r\)이 request \(i\)의 완전한 pair를 포함
- \(h_{vr}=1\): column \(r\)이 vehicle \(v\)를 소비
- \(x_r\in\{0,1\}\): route column 선택
- \(u_i\in\{0,1\}\): request를 route 밖 bank에 유지

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

Column admission 전에 pair, capacity, time, terminal, capability, zone와 route-level hard feasibility를 이미 검증한다. 여러 concrete vehicle을 한 class/count row로 합치는 것은 prepared travel, work window, terminal, capacity, ownership, objective coefficient와 모든 MIP-relevant resource가 동등하다는 별도 equivalence proof가 있을 때만 허용한다.

Mandatory request는 domain에서 무조건 \(u_i=0\)인 hard assignment로 바뀌지 않는다. 현재 mandatory 의미는 `mandatoryUnassignedCount`를 최상위 objective로 최소화하는 것이다. Exact assignment가 별도 preset에서 승인된 경우에만 해당 request의 \(u_i=0\)을 projection contract가 명시한다.

Ordered dimension \(d\)가 route/unassigned 단위로 가산 가능하면 다음 값을 사용한다.

$$
Q_d(x,u)=
\sum_{r\in R} q^{route}_{dr}x_r
+
\sum_{i\in I} q^{unassigned}_{di}u_i
$$

Objective는 bound comparator와 같은 dimension order의 lexicographic minimization이다. Google OR-Tools CP-SAT 단계별 solve는 dimension \(d\)의 exact `OPTIMAL`이 증명된 뒤에만 integer equality \(Q_d=Q_d^\*\)를 다음 model stage에 추가하고 \(d+1\)로 진행한다. CP-SAT gap limit 충족도 `OPTIMAL`로 보고될 수 있으므로 loose absolute/relative gap은 exact proof가 아니며 exactness를 보존하는 gap 정책 승인 전에는 활성화하지 않는다. `FEASIBLE_LIMIT`이면 더 낮은 priority dimension으로 진행하지 않는다. 근거 없는 finite Big-M로 strict priority를 평탄화하지 않는다. Authoritative coefficient와 constraint는 CP-SAT의 integer-only model에 checked `long`으로 전달하며 model 전체의 int64 domain·합계 bound와 overflow를 model build 전에 검증한다. Continuous variable가 없으므로 MPSolver는 canonical backend가 아니다.

Win PoC projection 예시는 다음이다.

```text
Q1 = Σ_{i∈I} u_i
Q2 = Σ_{r∈R} x_r
Q3 = Σ_{r∈R} routeDistance_r x_r
Q4 = Σ_{r∈R} routeOperationalTime_r x_r
```

이는 Win preset의 예시일 뿐 모든 customer objective가 아니다.

### 12.6 Model mode와 warm start

Model mode는 의미가 다른 두 경로를 분리한다.

| Mode | Request row | 지위 |
|---|---|---|
| `SET_PARTITION_EXACT` | \(\sum_{r\in R} a_{ir}x_r+u_i=1\) | Canonical RPDPTW corrected target 후보 |
| `SET_COVER_THEN_CONVERT` | \(\sum_{r\in R} a_{ir}x_r\ge1\), \(u_i\) 없음 | Complete-cover warm start가 존재하는 OGC 2024 compatibility/differential experiment 전용 |

두 mode의 품질·성능 결과를 같은 algorithm fingerprint로 비교하지 않는다. Set cover는 explicit unassigned/bank 의미를 표현하지 않으며 모든 request를 적어도 한 번 덮는 snapshot에서만 실행한다. 그 결과는 request 중복을 허용하므로 `SearchSnapshot`이나 `VerifiedSolution`이 아니다.

`MipWarmStart`는 다음을 가진다.

```text
cache-free-validated phase incumbent fingerprint
poolSnapshotId/fingerprint
selected incumbent ProjectedColumnIds
incumbent SearchRequestBank as u_i (`SET_PARTITION_EXACT` only)
problem/travel/profile/projection fingerprints
feasibility check record
```

Warm start를 만들기 전에 incumbent의 모든 route를 exact evaluation으로 pool에 merge하고 pinned artifact로 둔다. Seal/model-build 때 각 artifact를 projection해 만든 selected `ProjectedColumnId`가 model manifest에 존재하고 request exact partition, concrete vehicle consumption과 모든 row를 만족해야 한다. Backend start는 hint이고 채택 보장이 아니므로 `OFFERED`, `ACCEPTED_BY_BACKEND`, `REJECTED_BY_BACKEND`, `UNKNOWN`을 구분할 수 있다.

ALNS warm start와 MIP warm start를 혼용하지 않는다.

- `AlnsWarmStart`: stable routes/bank snapshot, pool lineage와 RNG/adaptive continuation policy
- `MipWarmStart`: fixed column/unassigned variable values와 model fingerprint

### 12.7 Selection outcome, conversion과 adoption

Provider-neutral selection outcome은 최소 다음 sealed category를 가진다.

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

Outcome은 raw `CpSolverStatus`, incumbent presence, selected `ProjectedColumnId`, work/elapsed, backend/config fingerprint, warm-start disposition과 별도 termination cause를 보존한다. Backend adapter의 선택 반환은 route IDs뿐이며 unassigned set은 selected coverage의 exact complement로 materialization 경계가 재구성한다. `CpSolver.objectiveValue()`/bound/gap은 outcome 반환이나 adoption authority에 넣지 않는다. CP-SAT `OPTIMAL`은 `OPTIMAL`, `FEASIBLE`은 `FEASIBLE_LIMIT`, `INFEASIBLE`은 `PROVEN_INFEASIBLE`, `MODEL_INVALID`는 `MODEL_INVALID`, `UNKNOWN`은 `NO_INCUMBENT_LIMIT`으로 매핑한다. `value()`/`booleanValue()`와 selected IDs는 `OPTIMAL`/`FEASIBLE`에서만 읽는다. Time limit, `stopSearch()` cancellation, memory/custom limit은 raw status와 분리한 cause이며, cancellation 뒤 `FEASIBLE`이면 incumbent-present이고 `UNKNOWN`이면 no-incumbent다. `BACKEND_UNAVAILABLE`은 optional assembly 부재이고 `NATIVE_RUNTIME_UNAVAILABLE`은 `Loader.loadNativeLibraries()` 또는 platform-native 초기화 실패다. Feasible warm start가 있는 exact model의 `PROVEN_INFEASIBLE`은 품질 결과가 아니라 model/snapshot defect다.

`SET_PARTITION_EXACT` materialization은 selected immutable artifacts를 새 `RoutePlan` 목록으로 복사하고 \(I-\bigcup coverage(selected)\)로 새 `SearchRequestBank`를 만든다. 이 complement와 exact request row/model identity를 교차검증하며 backend의 \(u_i\) value를 별도 반환하지 않는다. Pool entry를 alias하거나 mutate하지 않는다.

`SET_COVER_THEN_CONVERT`의 `CoverSelection`은 다음 순서를 지킨다.

1. Duplicate request를 stable ID 또는 전용 seeded stream 순서로 처리한다.
2. 각 duplicate의 keep choice마다 route 제거, empty-route 삭제, vehicle count와 전체 candidate objective까지 반영한 temporary solution을 authoritative full evaluation/comparator로 비교한다.
3. 제거 route에서는 pickup/delivery pair를 atomic하게 제거해 새 route를 만든다.
4. Empty route를 제거하고 모든 changed route의 propagation, metric, objective와 signature를 재계산한다.
5. Exact request/vehicle partition을 다시 검증한다.

단일 additive cost compatibility mode에서는 request의 route별 marginal contribution \(\Delta_i(r)=C(r)-C(r\setminus\{i\})\)을 계산해 \(\Delta_i(r)\)가 가장 작은 route에 request를 남기고, 비용 절감이 더 큰 나머지 route에서 제거하는 OGC 규칙을 재현할 수 있다. 일반 lexicographic/non-additive profile은 각 keep choice의 전체 temporary solution을 full-evaluate하거나 mode를 skip한다. Backend `ObjVal`은 conversion 전 model evidence이며 evaluated candidate objective가 아니다.

`MaterializedSelectionDraft`는 structural partition, authoritative full propagation/evaluation과 fingerprint 검사를 통과해야 `EvaluatedSelectionCandidate`가 된다. 비교 기준인 `HybridPhaseIncumbent`는 직전 ALNS segment가 완료한 cache-free validated `solveBest`다. Selector 후보가 이 incumbent보다 `STRICTLY_BETTER`인 경우에만 next inner hybrid phase의 `AlnsWarmStart`로 채택한다. Equal/worse/invalid/no-incumbent/backend/native-runtime failure이면 incumbent fingerprint를 보존한다.

Search bank의 \(u_i\)나 MIP failure는 final `UNASSIGNED` outcome/diagnostic이 아니다. 최종 champion은 기존 candidate verifier → final insertion audit → result verifier를 그대로 거친다.

### 12.8 Hybrid phase artifact와 commit

ALNS step, ALNS segment, inner hybrid phase와 distributed outer round를 분리한다.

```text
ALNS step
  < ALNS segment (declared completed-step budget)
  < HybridPhase (ALNS → pool seal → selection → adoption)
  < WorkerRun
  < ExecutionRound (declared worker fan-out/fan-in)
```

`HybridPhaseRecord`의 최소 contract:

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

다음 inner phase는 `HYBRID_PHASE_COMMITTED`의 champion만 소비한다. Interrupted conversion이나 failed selector가 current/stageBest/solveBest, pool snapshot 또는 next warm start를 부분 변경할 수 없다. Worker-local pool persistence가 reference-adaptation baseline이며 cross-worker pool merge/central selection은 별도 scalability decision이다.

## 13. Finalization, outcomes와 diagnostics

### 13.1 Publication sequence

```text
committed candidate
→ candidate solution verifier
→ preliminary ASSIGNED/UNASSIGNED partition
→ required final-solution insertion audit
→ final outcomes/diagnostics
→ post-finalization result-integrity verifier
→ publication
```

### 13.2 Two-state outcome

모든 input request는 exactly one outcome을 가진다.

| Status | Meaning |
|---|---|
| `ASSIGNED` | Exactly one verified input vehicle route/pair reference |
| `UNASSIGNED` | 어떤 verified input vehicle route에도 없음 |

`DIRECT`와 `LEASE` vehicle에 배정된 request는 모두 `ASSIGNED`다. Ownership은 vehicle reference와 objective breakdown으로 표현한다.

Solver는 fleet 밖 공급자를 만들지 않고, 운영자의 후속 외주·이월을 `OUTSOURCED`/`DEFERRED` status로 생성하지 않는다.

### 13.3 Final-solution insertion audit

Normalization/static precheck만으로 순서와 탐색 품질에 무관한 불가능성이 증명된 request는 `PROVEN` evidence를 사용할 수 있고 중복 audit를 생략한다.

그 밖의 모든 `UNASSIGNED` request는:

- Final routes와 다른 request placement를 고정
- 모든 eligible vehicle 검사
- 모든 합법 pickup/delivery position pair 검사
- Constraint별 rejection count와 work count 기록

모든 option이 실패한 경우에만 `EXHAUSTIVE_FOR_FINAL_SOLUTION`을 사용할 수 있다. 이는 current final solution 기준이며 전역 재배치 불가능성 증명이 아니다.

Audit가 feasible insertion을 찾으면:

- 자동 insert하지 않음
- Solver를 자동 재호출하지 않음
- 수정/재탐색 loop를 시작하지 않음
- Outcome은 `UNASSIGNED`로 게시 가능
- 발견 사실은 internal audit record에만 보존
- External diagnostic은 별도로 성립하는 proven/search-observed/`UNKNOWN`만 사용

### 13.4 Verification boundaries

Candidate verifier authority:

```text
immutable problem/profile declaration
candidate route/node order
candidate SearchRequestBank
prepared authoritative travel data
```

Result-integrity verifier authority:

```text
candidate PASS report + verified solution
final outcomes
diagnostic source/audit evidence
outcome-derived summary
publishable payload
```

두 verifier는 search cache와 solver summary를 권위 입력으로 사용하지 않는다. 어느 gate든 fail/incomplete이면 정상 route/outcome과 benchmark vector를 게시하지 않는다.

## 14. Result metrics와 Win PoC

### 14.1 Route operational time

Win PoC fourth metric:

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

미사용 vehicle idle, route 전 업무 무관 시간, solver/verifier/serialization elapsed는 포함하지 않는다. Breakdown과 total은 verifier가 재계산할 수 있어야 한다.

### 14.2 Exact comparator

```text
unassigned request count
→ dispatched vehicle count
→ total directed distance
→ total route operational time
```

모든 성분은 작을수록 좋고 첫 번째 다른 성분이 승패를 정한다. Customer solve objective와 Win comparator를 혼합하지 않는다.

### 14.3 Multi-round execution contract

Domain/result lineage는 다음을 표현할 수 있어야 한다.

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
hybrid phase ordinal and ALNS segment lineage
route-pool before/delta/after fingerprints
projection/model/warm-start/backend identities
selection/conversion/full-evaluation/adoption/fallback
```

모든 declared worker가 normal max-step completion과 verification을 통과해야 round complete다. Round/worker 수, `maxSteps`, watchdog의 실제 공식 수치는 `Q-BENCH-02` calibration 전까지 없다. Logical fan-out/fan-in은 physical infrastructure를 결정하지 않는다.

## 15. Error model

### 15.1 Pre-solve errors

- Schema/version/alias conflict
- Invalid numeric/time string
- Decimal cost/distance/time
- Order-level `taskTime`
- Invalid size/zone/ownership code shape
- Unresolved physical location
- Missing coordinate required for generated `D`
- Incomplete prepared travel after preparation
- Unsupported non-oneway rotation
- Unknown/unauthorized profile or preset
- Missing profile dependency
- Checked arithmetic overflow

### 15.2 Search/domain defects

- Partial/duplicate/split pair
- Route+bank duplicate or omission
- Wrong terminal policy
- Stale cache mismatch
- Failed rollback/discard isolation
- Unresolved prepared travel at runtime
- Destroy proposal의 duplicate/non-assigned request 또는 pair-atomic edit 실패
- Route pool artifact의 authority fingerprint mismatch, mutable alias 또는 stale evaluation
- Unsafe dominance로 nondominated route 손실
- Selected projected-column materialization 뒤 request/vehicle partition 위반

이 결함을 정상 `UNASSIGNED` reason이나 낮은 score로 숨기지 않는다.

### 15.3 Termination과 result

`MAX_STEPS_REACHED`(계획된 stage/worker step 완료), `NO_STRICT_IMPROVEMENT`(완결된 phase-2 batch의 stable champion이 이전 champion보다 엄격히 좋지 않음), `MAX_ROUNDS_REACHED`(configured round 완료)는 정상 품질 종료다. Watchdog, cancellation, resource, platform timeout과 failure는 별도다. 예외 종료의 last committed best도 두 verifier를 통과해야 recovery candidate가 될 수 있고 official benchmark completion으로 표시하지 않는다.

Route-selection의 `NO_INCUMBENT_LIMIT`, `BACKEND_UNAVAILABLE`, `NATIVE_RUNTIME_UNAVAILABLE`, `MODEL_INVALID`, `MODEL_BUILD_FAILED`, `SOLVER_FAILED`, conversion/full-evaluation failure는 ALNS step termination이 아니다. Optional plan에서는 `HybridPhaseIncumbent`를 보존한 typed hybrid fallback이고, MIP-required plan에서는 worker/run `INCOMPLETE` 또는 failure다. Raw backend incumbent와 model objective는 recovery candidate가 아니다.

## 16. Acceptance evidence

### 16.1 Numeric

- `n=3/FLOOR` boundary
- Item-first vs line-first 차이
- Decimal cost/distance/time rejection
- Checked multiplication/sum overflow
- 999 CBM provenance

### 16.2 Time/service

- `[start,end)` plan boundary와 inclusive close
- Early arrival/customer wait
- `START_ONLY` vs `COMPLETE_WITHIN_WINDOW`
- Repeating/overnight windows
- Full-arc next-window restart and no partial driving
- `reqDate/dueDate` completion deadline
- `duration + Σ(taskTime×qty)`
- Depot/customer wait separation

### 16.3 Compatibility

- Free-form case-sensitive size
- Exact `["ALL"]`
- Legacy alias conflict
- Capability subset
- `ALL` zone와 one-concrete-zone route
- Pickup/delivery intersection
- No-compatible-vehicle static `PROVEN`

### 16.4 Travel

- Provided D/U priority
- Decimal D/U rejection
- Self `0/0`
- Great Circle `HALF_UP`
- Vehicle-specific generated U `CEILING`
- Missing speed 45km/h
- Asymmetric arcs
- No runtime lazy generation
- Solver/verifier prepared fingerprint equality

### 16.5 State/result

- Pair/bank property tests
- Mixed delivery-only/real pickup load prefixes
- Oneway/roundtrip terminal policy
- Stop/location transition
- Route-level drive resources
- COW isolation and interruption discard
- Cache-free equality
- DIRECT/LEASE both assigned
- Required audit and confidence ceiling
- Candidate/result verifier corruption rejection

### 16.6 Reproducibility

Strong reproducibility requires fixed:

```text
problem/travel/policy/profile fingerprints
build/runtime compatibility
algorithm/operator/state strategy
seed derivation and actual seeds
round/run/warm-start lineage
stage maxSteps
stable iteration/reduction/tie-break
normal `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED` termination
hybrid 사용 시 stable pool-artifact/projected-column/projection/conversion order
backend version/thread/seed/numeric/work-budget identity
```

Time-limited multi-worker CP-SAT는 이 strong class를 자동 만족하지 않는다. OR-Tools version/platform, `num_workers`, `random_seed`, time/deterministic-work limit과 모든 model-order identity를 manifest에 명시한다. 구체 숫자는 승인 전 `OPEN`이며 single-worker/fixed-seed profile도 먼저 `PROPOSED TEST_ONLY`다. `TIMEBOXED_HYBRID`는 model/pool/conversion identity를 보존하되 품질 분포와 fallback rate를 별도 evidence로 사용한다.

### 16.7 ALNS, route pool과 route selection

- Destroy proposal의 ordered-unique request와 central pair edit 전후 partition
- Repair complete/partial/no-feasible 각 결과의 stable route/bank partition
- Cheap shortlist가 authoritative feasibility/objective로 사용되지 않음
- Exact insertion과 small-route brute-force oracle 일치
- Operator reward/probability가 finite·positive·sum-one이며 interrupted step 미전진
- Rejected hard-feasible trial route admission, invalid/interrupted route exclusion
- Pool append/import/reload의 동일 merge와 deterministic digest
- Immutable no-alias, incumbent pin과 safe dominance/Pareto oracle
- Snapshot 동안 stable artifact enumeration과 fingerprint
- Tiny exact model의 request/vehicle rows와 brute-force optimum 일치
- Explicit unassigned와 mandatory objective cases
- Warm-start projected-column completeness와 feasibility
- 모든 selection status × incumbent-presence의 attribute-access/fallback
- Compatibility conversion의 pair-atomic clone, full recomputation과 exact partition
- Backend/conversion/validation failure 뒤 ALNS incumbent fingerprint 불변
- Strictly better adopted result만 다음 `AlnsWarmStart`로 연결
- Backend model objective가 final comparator authority로 사용되지 않음

## 17. Deferred boundaries

| Item | Current boundary | Resume requirement |
|---|---|---|
| Multi-trip/rotation | Oneway + single roundtrip, pair crossing 금지 | Exact trip/reset/depot/resource contract와 별도 승인 |
| Route pool/MIP production activation | §10.9, §12.5~§12.8의 target types/model; backend policy는 OR-Tools CP-SAT이고 default execution은 off | Master `RM-9A`~`RM-9C`, verified baseline, measured value와 OR-Tools version/config/native packaging/Apache-2.0 notice·SBOM/security/operations/compute-cost/admission/fallback/rollback 승인 |
| Physical topology | Logical ports와 run/round lineage | Workload/security/retention/retry/cost evidence와 `Q-INFRA-01` 별도 승인 |
| Optional variants | Pair, terminal, bank, travel contract 유지 | `Q-VAR-01` 선택·fixture·core-impact study 승인 |

`Q-INFRA-01`과 `Q-VAR-01`은 계속 `DEFERRED`이며 이 문서가 질문하거나 활성화하지 않는다.

## 18. Decision traceability

| Domain area | Question decisions | Master |
|---|---|---|
| Numeric | `Q-NUM-01~03` | §7.2 |
| Travel preparation | `Q-MTX-01~03` | §8 |
| Time/window | `Q-TIME-01~04` | §7.3 |
| Legacy service/options | `Q-IN-01~02`, `Q-BENCH-03` | §6~§8, §14 |
| Size/zone | `Q-COMP-01~02` | §5.3, §7.5 |
| Service pattern/trip | `Q-REQ-01~02` | §5~§6 |
| Profile/objective | `Q-OBJ-01~03` | §9 |
| Candidate strategy | `Q-ALG-01`, `Q-ALG-02` | §11~§12 |
| Optional route pool/MIP | `C-17` production gate, Master `P-15~P-19` | Master §11.7~§11.10, `RM-9A~C` |
| Outcome/audit | `Q-RES-01~02` | §10, §14 |
| Benchmark | `Q-BENCH-01`, `Q-BENCH-03`; `Q-BENCH-02` experiment-required | §14 |
| Deferred | `Q-INFRA-01`, `Q-VAR-01` | §4, §16 |

Architecture 배치 traceability:

| Domain area | Architecture Design |
|---|---|
| Input/domain/travel/propagation/evaluation | §5.1, §6.1, §7 |
| Portfolio/COW/ALNS | §6.2, §7, §19 |
| Route pool/selection/backend | §6.2, §6.4, §7, §17~§21 |
| Verification/finalization/result | §6.3, §18~§19 |
| Profile/customer extension | §6.5, §9 |
| Application port와 provider adapter | §6.4, §6.6, §10~§17 |

Question status count:

```text
RESOLVED 25
OPEN — EXPERIMENT_REQUIRED 1
DEFERRED 2
TOTAL 28
```

## 19. End-to-end example

```text
1. Versioned adapter parses exact input and aliases.
2. Weight/volume items are n=3/FLOOR normalized before qty multiplication.
3. Time strings become planning-origin long seconds.
4. Repeating/overnight windows and completion deadline are compiled.
5. Size/capability/zone produce static facts and route-level zone constraint.
6. Travel preparation preserves provided D/U and generates only missing values.
7. Immutable problem/profile/plan snapshots are fingerprinted.
8. Portfolio creates up to eight independent candidates; phase 1 selects one cache-free validated champion, then phase 2 improves it in complete worker batches.
9. COW ALNS mutates request pairs atomically; completed hard-feasible routes may create an acceptance-independent pool delta.
10. If an approved hybrid plan is enabled, the worker seals its pool, runs exact-projectable route selection, reconstructs and full-evaluates the result.
11. Only a strictly better reconstructed candidate is adopted; every selector failure or invalid/worse result preserves the ALNS incumbent.
12. Candidate verifier recomputes the committed worker candidate cache-free.
13. Finalization creates preliminary outcomes and audits required unassigned requests.
14. Result-integrity verifier checks partition, audit confidence, summary and payload.
15. Only verified result is published or compared.
```

## 20. New customer extension procedure

1. Define external schema/version and exact customer profile.
2. Map input meaning in the adapter; do not branch in ALNS.
3. Reuse normalized facts and add the narrowest required hard constraint/metric/score.
4. Add an objective dimension only to that customer’s approved preset.
5. Extend propagation facts only when existing physical state cannot express the requirement.
6. Bind exact dependencies and version/config hash before solve.
7. Add full-recomputation, profile-isolation and verifier evidence.
8. Preserve existing customer regression and result lineage.

Customer name, price term, objective order or output label alone is not a reason to change common route state or ALNS.

Architecture placement:

- Input/output 표현 차이는 `adapters/common` 또는 provider adapter의 typed package에 둔다.
- 기존 fact로 표현 가능한 constraint/metric/score/objective는 customer profile JAR에서 `rpdptw-core` SPI를 구현한다.
- 실제 물리 진행 의미가 부족한 경우에만 `rpdptw-core`의 typed domain/propagation seam 변경을 ADR로 검토한다.
- ALNS/COW 동작 자체의 고객별 차이는 `rpdptw-solver` 분기가 아니라 bound profile/operator contract로 전달한다.
- Profile JAR가 `rpdptw-solver` 내부 package나 provider SDK를 참조하면 extension validation 이전에 architecture violation으로 차단한다.

## 21. Final summary

이 Domain Design의 핵심은 다음 네 분리다.

```text
external meaning ≠ normalized solver facts
hard feasibility ≠ neutral metrics ≠ score/objective
search membership ≠ final outcome/diagnostic
route pool/selection intermediate ≠ stable solution
logical execution ≠ physical infrastructure
```

목표 기본은 immutable normalized problem, prepared travel data, atomic request pair, copy-on-write trial과 two-gate publication이다. Optional ALNS–MIP는 explicit unassigned exact partition, immutable evaluated artifacts/projected columns, fresh materialization과 full evaluation, ALNS fallback을 유지하는 상세 target이지만 `C-17`과 `RM-9A`~`RM-9C` gate 전에는 production 기본이 아니다. 질문 등록부의 상태는 여전히 `Q-BENCH-02` 한 건이 experiment-required이고 infrastructure와 variants가 deferred다.

Java/Maven 배치는 이를 `rpdptw-core`의 package 경계, 독립 `rpdptw-solver`와 `rpdptw-verification`, provider-neutral `rpdptw-application`, 교체 가능한 adapter/profile JAR로 구현한다. Maven module 수를 논리 계층 수와 같게 만들지 않으며, 같은 core module 안에서도 이 문서의 의미 경계는 package와 architecture test로 유지한다.
