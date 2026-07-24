# RPDPTW Domain Design

```yaml
status: REVIEW
version: 2.2-review
last_updated: 2026-07-24
owner: RPDPTW Domain·Input·Evaluation·Result 설계 역할
scope: Master Design의 도메인, 정규화, travel preparation, 해 상태, 전파, 평가와 결과 계약의 상세화
supersedes: 초안 v1.1 및 세션 29 이전의 legacy 상세 가정
related_designs:
  - architecture-design.md
related_decisions:
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
```

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

### 2.2 현재 비범위

- Multi-trip/rotation의 구현
- Route pool과 MIP 후처리
- MDVRP·OVRP·SDVRP 구현
- 특정 cloud, orchestration product, storage와 deployment topology
- 동적 교통, 실시간 replanning, geocoding과 주소 정제
- `Q-BENCH-02`의 calibration/approval evidence 없는 공식 실행 수치

Multi-trip, infrastructure와 optional variant를 표현하기 위한 좁은 경계는 보존하되 활성화하지 않는다.

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
| Finalization/result | Verified outcome, diagnostic, summary, provenance | Search mutation |

외부 adapter와 infrastructure implementation은 application orchestration/logical port를 통해 domain·evaluation·algorithm의 immutable contract를 소비한다. Core는 backend timezone, provider SDK, storage path와 customer name을 참조하지 않는다.

### 3.1 Architecture package mapping

[Architecture Design §5~§7](architecture-design.md#5-recommended-maven-multi-module-tree)은 위 논리 계층을 다음 Maven module/package에 배치한다.

| Domain 책임 | Maven 경계 | Package 경계 |
|---|---|---|
| Canonical input, immutable domain, normalization, travel | `rpdptw-core` | `input`, `domain`, `normalization`, `travel` |
| Propagation, policy SPI, binding와 insertion evaluation | `rpdptw-core` | `propagation`, `evaluation.api`, `evaluation.runtime`, `evaluation.insertion` |
| Portfolio, COW candidate와 ALNS | `rpdptw-solver` | `solver.portfolio`, `solver.state`, `solver.search`, `solver.termination` |
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
= pickup and delivery on exactly one same vehicle route
  and pickup index < delivery index
  and request not in SearchRequestBank

UNASSIGNED_IN_SEARCH
= neither node on any route
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

`Q-ALG-02`는 `RESOLVED — KEEP_COW`다. 현재 기본은 copy-on-write다.

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
unassigned order count
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

이 결함을 정상 `UNASSIGNED` reason이나 낮은 score로 숨기지 않는다.

### 15.3 Termination과 result

`MAX_STEPS_REACHED`(계획된 stage/worker step 완료), `NO_STRICT_IMPROVEMENT`(완결된 phase-2 batch의 stable champion이 이전 champion보다 엄격히 좋지 않음), `MAX_ROUNDS_REACHED`(configured round 완료)는 정상 품질 종료다. Watchdog, cancellation, resource, platform timeout과 failure는 별도다. 예외 종료의 last committed best도 두 verifier를 통과해야 recovery candidate가 될 수 있고 official benchmark completion으로 표시하지 않는다.

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
```

## 17. Deferred boundaries

| Item | Current boundary | Resume requirement |
|---|---|---|
| Multi-trip/rotation | Oneway + single roundtrip, pair crossing 금지 | Exact trip/reset/depot/resource contract와 별도 승인 |
| Route pool/MIP | Verified ordered route/metrics/fingerprint export | Baseline, measured value, solver/licensing/fallback approval |
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
| Outcome/audit | `Q-RES-01~02` | §10, §14 |
| Benchmark | `Q-BENCH-01`, `Q-BENCH-03`; `Q-BENCH-02` experiment-required | §14 |
| Deferred | `Q-INFRA-01`, `Q-VAR-01` | §4, §16 |

Architecture 배치 traceability:

| Domain area | Architecture Design |
|---|---|
| Input/domain/travel/propagation/evaluation | §5.1, §6.1, §7 |
| Portfolio/COW/ALNS | §6.2, §7, §19 |
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
8. Portfolio creates up to eight independent candidates; phase 1 selects one verified champion, then phase 2 improves it in complete worker batches.
9. COW ALNS mutates request pairs atomically and completes normal termination only through the declared step/round contract.
10. Candidate verifier recomputes the committed candidate cache-free.
11. Finalization creates preliminary outcomes and audits required unassigned requests.
12. Result-integrity verifier checks partition, audit confidence, summary and payload.
13. Only verified result is published or compared.
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
logical execution ≠ physical infrastructure
```

현재 기본은 immutable normalized problem, prepared travel data, atomic request pair, copy-on-write candidate와 two-gate publication이다. 남은 활성 질문은 `Q-BENCH-02`의 공식 실행 수치 calibration 하나뿐이며, infrastructure와 variants는 deferred다.

Java/Maven 배치는 이를 `rpdptw-core`의 package 경계, 독립 `rpdptw-solver`와 `rpdptw-verification`, provider-neutral `rpdptw-application`, 교체 가능한 adapter/profile JAR로 구현한다. Maven module 수를 논리 계층 수와 같게 만들지 않으며, 같은 core module 안에서도 이 문서의 의미 경계는 package와 architecture test로 유지한다.
