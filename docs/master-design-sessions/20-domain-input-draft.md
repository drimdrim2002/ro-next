# 세션 20 — RPDPTW 도메인·입력 규범 초안

> **세션 30 통합 상태 (2026-07-23):** 이 초안의 `Q-NUM/MTX/TIME/IN/COMP/REQ` TBD는 세션 29 사용자 결정으로 해결되었다. 숫자·matrix preparation·시간·legacy field·size/zone·mixed route·single roundtrip 의미는 [Master §5~§8](../master-design.md)과 [질문 등록부](../deprecated/master-design-open-questions.md)가 현재 계약이다. 특히 본문의 no-fallback, mixed-route 금지, fixed terminal 일반화와 임의 wildcard 금지는 각각 preparation generation, mixed 허용, oneway/roundtrip terminal 정책과 exact `ALL` 규칙으로 대체된다. 이 문서는 역사적 integration input으로 보존하며 본문 TBD 표를 현재 질문 상태로 읽지 않는다.

> 상태: `REVIEW INPUT`
>
> 성격: 세션 25의 Master Design에 병합할 도메인·입력 계약 초안
>
> 권위 기준: [세션 19 통합 계획](19-integration-plan.md)
>
> 편집 범위: 이 문서만 작성한다. 코드, 입력 자료, 기존 설계 문서는 변경하지 않는다.
>
> 기준일: 2026-07-23

## 1. 문서 역할과 규범 표기

이 문서는 RPDPTW의 표준 문제 의미, 도메인 경계, 입력 adapter와 정규화, 거리·시간 행렬, 차량 크기 호환성, 요청 쌍 불변조건, 탐색 중 `SearchRequestBank`의 책임을 정의한다. 구현 완료를 주장하거나 구체 클래스·외부 JSON·배포 구조를 확정하는 문서가 아니다.

이 문서의 상태 표기는 다음과 같다.

| 표기 | 의미 | 작성 규칙 |
|---|---|---|
| **확정** | 세션 19의 `C-*` 결정 또는 그 결정에 필수인 계약 | `MUST`, `MUST NOT`으로 규범화 |
| **잠정** | 세션 19의 `P-*` 방향이며 이름·형태·세부 정책은 미승인 | `SHOULD`로 작성하고 잠정 ID를 병기 |
| **열린 질문** | 외부 의미나 구현 계약이 달라지는 미결정 | 임의 기본값 없이 정확한 `TBD(Q-...)` 사용 |

`DEFERRED`는 결정 상태가 아니라 현재 구현 범위에서 제외하는 처리 방식이다.

세션 19의 `C-01`, `C-05`부터 `C-15`까지를 이 문서에서 다음처럼 다룬다.

| 결정 | 이 초안의 처리 |
|---|---|
| `C-01` | §3에서 표준 용어와 학술 `PDPTW`의 경계를 확정 |
| `C-05` | §6에서 `Feature`를 차량 크기 유형으로 확정하고 capability/qualification과 분리 |
| `C-06` | §7에서 같은 차량, 각각 정확히 한 번, pickup 선행, route/bank XOR, 원자적 상태 전이를 확정 |
| `C-07` | §3.5에서 delivery-only와 실제 pickup-delivery 의미를 분리하고 중간 재적재를 금지 |
| `C-08` | 종료 정책은 세션 22 소유다. 이 문서는 시간 값이나 infeasible 상태를 실행 watchdog과 혼합하지 않는 경계만 제공 |
| `C-09` | §7에서 후보 표현 방식과 무관한 성공·실패 후조건을 확정한다. copy-on-write와 apply/undo 전환은 세션 22 소유 |
| `C-10` | §8에서 변환 경계 고정소수점과 코어 정수 계산을 확정 |
| `C-11` | §8과 §11에서 정확한 자릿수·rounding을 열린 질문으로 유지 |
| `C-12` | §8~9에서 999 CBM, plan end 유한 상한, 별도 infeasible 상태, Constants/configuration 역할을 확정 |
| `C-13` | §5에서 입력된 directed distance/time matrix의 권위와 physical location mapping을 확정 |
| `C-14` | §4.4에서 legacy PDF의 비규범적 지위를 확정 |
| `C-15` | §10에서 검색 bank와 최종 상태·진단의 책임을 분리 |

## 2. 범위와 권위 경계

### 2.1 이 초안이 소유하는 내용

- RPDPTW 문제와 표준 도메인 용어
- request, paired nodes, route, vehicle, terminal, physical location의 의미
- delivery-only와 실제 pickup-delivery의 의미 보존
- 입력 adapter, 의미 검증, 정규화, `ProblemInstance` 생성 경계
- 계획기간과 내부 시간축의 최소 계약
- 입력 distance/time matrix와 node-to-location mapping
- 차량 크기 유형과 정적 차량 호환성
- 요청 쌍 및 route/bank 불변조건
- 수치 단위, 고정소수점, overflow, 유한 한계값
- 탐색 중 `SearchRequestBank`의 멤버십 책임

### 2.2 이 초안이 소유하지 않는 내용

- 목적함수, 점수 조립, 사전식 비교, `SolvePlan` 세부: 세션 21
- 초기해 포트폴리오, ALNS, 연산자 선택, acceptance, step/watchdog, 후보 저장 방식: 세션 22
- 최종 assignment status, unassignment diagnostic, 결과 DTO, benchmark gate: 세션 23
- 실행 provider, product, queue, storage, runtime, 배포 topology: 현재 범위 밖
- MDVRP·OVRP·SDVRP 구현: `C-19`에 따라 deferred feasibility study

이 문서는 위 항목을 구현할 수 있도록 입력과 불변조건을 넘기지만, 그 세부 정책을 선점하지 않는다.

## 3. RPDPTW 표준 문제 모델

### 3.1 표준 용어

**확정 (`C-01`).** 이 프로젝트의 표준 문제 모델은 **RPDPTW (Rich Pickup and Delivery Problem with Time Windows)**다.

- `PDPTW`는 일반 학술 문제와 Ropke-Pisinger 연구의 문제명을 가리킬 때 유지한다.
- `RPDPTW`는 PDPTW에 업무용 용량, 근무, 지역, 차량 호환성, 경로 자원, 고객사별 확장 제약을 포함하는 프로젝트 표준 모델이다.
- 두 용어는 별칭이 아니다.
- 세션 02에서 금지한 구 표기는 신규 설계에 사용하지 않는다.

RPDPTW 해는 정규화된 요청 집합을 차량별 route와 검색 bank 사이에 분할하고, 배정된 모든 요청에 대해 구조적 pair 불변조건과 차량·시간·용량·경로 자원 제약을 만족하는 상태다.

### 3.2 핵심 개념

| 개념 | 규범 의미 |
|---|---|
| `Request` | 하나의 원자적 운송 의무. pickup node 하나와 delivery node 하나를 참조한다. |
| `PickupNode` | 요청 물량이 차량 적재에 들어오는 논리 작업. 실제 방문일 수도 있고 delivery-only 출발 적재의 가상 표현일 수도 있다. |
| `DeliveryNode` | 같은 요청 물량이 차량 적재에서 빠지는 배송 작업. |
| `Route` | 특정 차량에 결합되고 그 차량의 start terminal에서 시작해 end terminal에서 끝나는 안정 상태의 노드 순서. |
| `Vehicle` | 용량, 고정 terminal, 근무 가능 데이터, 경로 자원 한계, 하나의 차량 크기 유형을 가진 운송 자원. |
| `Terminal` | route 구조의 시작 또는 종료를 표시하는 solver node. 물리 위치, 시간 제약, 적재 경계를 명시적으로 참조한다. |
| `PhysicalLocation` | 이동 거리·시간 행렬의 endpoint가 되는 물리 장소. solver node와 별도 식별·인덱스를 가진다. |
| `SearchRequestBank` | 탐색 중 어느 정규 route에도 배정되지 않은 request ID의 집합. |

외부 주문 ID, 차량 ID, 위치 ID와 코어의 조밀한 정수 ID 사이의 양방향 매핑은 입력 정규화 결과의 일부다. 코어 ID 배열은 `ProblemInstance` 생성 뒤 바뀌지 않아야 한다.

### 3.3 Request와 paired nodes

**확정 (`C-06`).** 모든 표준 `Request`는 다음 정적 관계를 가져야 한다.

```text
Request
├─ requestId
├─ pickupNodeId
├─ deliveryNodeId
├─ normalized demand dimensions
├─ service meaning
└─ servableVehicles
```

정적 모델은 다음을 `MUST` 만족한다.

1. pickup node ID와 delivery node ID는 서로 다르다.
2. 두 node가 같은 물리 위치를 참조해도 solver node ID는 구분한다.
3. 하나의 request node는 정확히 하나의 request에만 속한다.
4. terminal은 어떤 request에도 속하지 않는다.
5. pickup load delta와 delivery load delta는 차원별로 정확히 상쇄된다.
6. request-to-node, node-to-request, 외부 ID-to-core ID 매핑은 생성 후 불변이다.
7. request의 정적 차량 호환성은 pickup과 delivery 전체를 수행할 수 있는 차량만 포함한다.

`precedence` 같은 일반 node 속성은 request pair의 기준 정보가 아니다. 같은 차량, pair completeness, exactly-once, route/bank XOR은 `Request` 관계와 해 전체 검증으로 보장해야 한다.

### 3.4 Route, vehicle, terminal

표준 범위의 각 route는 다음을 `MUST` 만족한다.

1. 정확히 하나의 vehicle ID에 결합된다.
2. 첫 node는 그 차량의 고정 start terminal이다.
3. 마지막 node는 그 차량의 고정 end terminal이다.
4. 다른 차량의 terminal은 route 내부에 들어갈 수 없다.
5. route 내부의 request node는 완전한 pair 상태로만 관찰된다.
6. start terminal에서 첫 pickup을 적용하기 전 적재량은 0이다.
7. 모든 적재 차원은 전 구간에서 `0 <= load <= vehicleCapacity`를 만족한다.
8. 완성된 표준 route는 요청 pair의 순변화가 0이므로 종료 적재량도 0이어야 한다.

start terminal과 end terminal은 동일하거나 서로 다른 physical location을 참조할 수 있다. 단, 종료 정책과 물리 위치·이동 비용은 입력 계약이 명시적으로 정규화해야 하며, solver가 좌표나 옵션 문자열만으로 추론해서는 안 된다.

합성 sink, 동적 terminal 선택, depot 공유 자원, multi-depot 의사결정은 이 표준 계약에 포함하지 않는다. 이는 [세션 17](17-optional-variant-feasibility.md)의 deferred feasibility 범위다.

### 3.5 Delivery-only와 실제 pickup-delivery

**확정 (`C-07`).** 하나의 RPDPTW 코어를 사용하되 다음 두 서비스 의미를 구분해야 한다.

| 서비스 의미 | pickup의 의미 | 위치 자유도 | 적재 의미 |
|---|---|---|---|
| delivery-only | route 출발 전에 준비된 화물의 출발 적재 | 고객 방문 사이의 자유 삽입 대상이 아님 | 첫 고객 전에 route의 delivery-only 수요 전체가 적재됨 |
| real pickup-delivery | 입력에 존재하는 실제 pickup 작업 | 같은 route에서 delivery 전의 실행 가능한 위치 | 실제 pickup에서 증가하고 delivery에서 감소 |

서비스 의미를 구분하는 필드의 최종 타입명과 enum 값은 잠정이다. `DELIVERY_ONLY`와 `PICKUP_DELIVERY`는 의미 설명용 이름이다 (`P-01`).

**잠정 (`P-02`).** delivery-only v1은 요청 쌍 구조를 유지하고 모든 가상 pickup을 start terminal 직후의 연속된 route prefix에 두는 방식을 `SHOULD` 사용한다.

```text
허용되는 delivery-only 정규형

start terminal
→ virtual pickup A
→ virtual pickup B
→ delivery A
→ delivery B
→ end terminal
```

```text
표준 단일 회차에서 금지

start terminal
→ virtual pickup A
→ delivery A
→ virtual pickup B
→ delivery B
→ end terminal
```

표현 방식과 무관하게 다음 의미는 확정이다.

- 가상 pickup은 실제 고객 stop이나 중간 depot 재방문이 아니다.
- 가상 pickup 표현은 원래 delivery-only 문제에 없던 물리 이동을 추가해서는 안 된다.
- 가상 pickup prefix 종료 시 적재량은 그 route에 배정된 delivery-only 요청 수요의 합과 같아야 한다.
- 첫 고객 이후 delivery-only 화물을 다시 적재해서 용량을 우회할 수 없다.
- 실제 pickup은 입력된 physical location, time window, service time을 가진 실제 방문이다.
- 가상 pickup의 depot 작업시간 집계 단위는 `TBD(Q-IN-02)`다.
- delivery-only와 real pickup-delivery의 route 혼합은 `TBD(Q-REQ-01)`이다.
- multi-trip 지원과 pair의 trip 경계 의미는 `TBD(Q-REQ-02)`다.

가상 pickup을 route prefix node로 둘지 초기 적재 상태로 둘지는 `P-01`에 따라 잠정이다. 어느 표현을 선택해도 request 원자성, 용량 판정, travel 합계, 외부 업무 의미가 같아야 한다.

## 4. 입력 adapter와 정규화 계약

### 4.1 경계 파이프라인

외부 입력을 코어 타입으로 직접 역직렬화하지 않는다.

```text
external input bytes
→ schema/version별 input adapter DTO
→ 문법·자료형·필수값 검증
→ 외부 필드의 업무 의미 해석
→ canonical business model
→ 시간·단위·위치·호환성 정규화
→ immutable normalized ProblemInstance
```

각 단계의 책임은 다음과 같다.

| 단계 | 책임 | 금지 |
|---|---|---|
| input adapter | 버전, 필드명, 허용 직렬화 형태 처리 | 코어에 legacy alias·문자열 option 전파 |
| 의미 검증 | 필드 관계, 참조, 지원 정책, 단위 확인 | 모호한 이름을 추정해 alias로 합치기 |
| 정규화 | 고정소수점, 절대 시간축, 위치 인덱스, 정적 호환성 계산 | 고객사 문자열을 탐색 hot loop에 전달 |
| `ProblemInstance` 생성 | 불변 배열·매핑·정책 snapshot 확정 | 실행 중 정책 또는 단위 변경 |

입력 오류와 정상적인 최적화 불가능을 구분한다.

- 알 수 없는 위치 참조, 중복 ID, 잘못된 수치, 필수 정책 누락, 모순된 request-node 매핑은 입력/변환 오류다.
- 형식과 의미가 유효하지만 허용 차량이 없는 request는 정상적인 배정 불가 상태다.
- 실행 가능한 입력의 특정 insertion candidate가 시간·용량 제약을 위반하는 것은 candidate infeasible이며 입력 오류가 아니다.

### 4.2 Canonical과 legacy adapter

canonical 입력은 단일한 필드명, 단위, 자료형, 시간 형식, 의미를 가져야 한다. exact JSON 이름과 unknown-field 정책은 이 초안에서 확정하지 않는다.

**잠정 (`P-12`).** 제한된 legacy adapter는 schema/version별로 승인된 숫자 문자열, object/1원소 array, 필드 alias를 `MAY` 수용한다. 단, 다음 조건이 필요하다.

1. 허용 목록이 adapter 버전에 고정되어야 한다.
2. canonical 출력과 코어 모델에는 legacy 형태가 남지 않아야 한다.
3. 적용한 alias·coercion·compatibility rule을 provenance에 기록해야 한다.
4. 의미가 확인되지 않은 필드는 조용히 무시하거나 이름만 보고 합치지 않아야 한다.
5. unsupported 의미는 solve 전에 명시적으로 거부해야 한다.

특히 `reqDate`와 `dueDate`, `duration`과 주문 수준 `taskTime`은 `TBD(Q-IN-01)`이므로 alias로 확정하지 않는다.

### 4.3 Planning period와 시간 계약

정규화 전 business model은 정확한 계획 시작과 종료를 필수로 가져야 한다.

```text
PlanningPeriod
├─ start
├─ end
├─ planning zone / offset contract
└─ boundary policy
```

다음은 확정이다.

1. `start < end`여야 한다.
2. `baseDate + days`는 정확한 시작·종료를 대신할 수 없다.
3. 내부 시간축은 planning origin으로부터의 단조 증가 fixed-point time unit을 사용한다.
4. 계획 시작이 local midnight가 아니어도 그 이전 시간을 열어서는 안 된다.
5. node windows, terminal windows, vehicle work windows는 같은 시간축으로 정규화하고 계획기간과의 관계를 검증한다.
6. plan end는 유한한 허용 시간 상한이며 실패 sentinel이 아니다 (`C-12`).
7. 시간대, canonical date-time, 경계 포함 여부, 창 완료 정책이 정해지지 않으면 adapter가 임의 기본값을 선택해서는 안 된다.

다음 의미는 열린 상태다.

- planning zone과 canonical date-time 형식: `TBD(Q-TIME-01)`
- plan end와 time-window close의 포함/제외: `TBD(Q-TIME-02)`
- 서비스 시작만 제한하는지 완료까지 제한하는지: `TBD(Q-TIME-03)`
- 반복 일간 창, overnight 창, 근무 종료를 넘는 이동: `TBD(Q-TIME-04)`

`reqDate`/`dueDate` 의미가 확정되기 전에는 release, deadline, requested service date 중 하나로 변환하지 않는다 (`Q-IN-01`).

### 4.4 Read-only legacy evidence의 지위

| 자료 | 확인된 용도와 사실 | 규범적 지위 |
|---|---|---|
| [`ro_input_json_spec.pdf`](../../data/ro_input_json_spec.pdf) | 10쪽짜리 기존 CVRPTW 입력 사례. kg, cbm, sec, km/h와 Plan/Order/Vehicle 필드를 설명하지만 표와 예시의 필드명·자료형·cardinality가 충돌하고 distance matrix 계약은 없음 | `C-14`: canonical RPDPTW 명세가 아니다. legacy adapter와 미결정 의미를 식별하는 read-only evidence |
| [`win_poc_case.json`](../../data/win_poc_case.json) | SHA-256 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`; 452 orders, 31 vehicles, 453 location IDs, 205,209 matrix arcs. size-type 코드와 mixed numeric representation을 관찰할 수 있음 | read-only input fixture. 정답 해나 canonical schema가 아니며 결과 의미를 증명하지 않음 |

PDF의 `OSRM`/`GreatCircle`, 속도 설명은 과거 입력 맥락이지 현재 solver의 travel fallback 계약이 아니다. fixture의 관찰값도 아래 `Q-*`에 대한 답으로 승격하지 않는다.

## 5. Authoritative distance/time matrix

### 5.1 권위와 방향성

**확정 (`C-13`).** 모든 solve는 정규화된 실제 directed distance/time input을 travel의 권위 값으로 사용해야 한다.

```text
travel(fromNode, toNode)
→ fromNode.locationIndex
→ toNode.locationIndex
→ directed matrix[fromLocation][toLocation]
→ normalized distance and travel time
```

다음은 금지한다.

- `from → to`로 `to → from`을 채우기
- 양방향 평균 또는 대칭화
- distance에서 travel time을 재계산해 입력 time을 덮어쓰기
- 좌표, Haversine, GreatCircle, 고정속도, vehicle speed로 누락 arc를 조용히 생성
- 일부 arc만 다른 source로 조용히 보충
- 입력 배열 순서나 order 배열 인덱스를 matrix 인덱스로 간주

명시적 matrix preparation이 향후 필요하더라도 solver core 밖에서 완전한 입력과 provenance를 만드는 별도 계약이어야 한다. provider나 배포 topology는 이 문서에서 고정하지 않는다.

### 5.2 Physical location mapping

solver node와 physical location을 분리한다.

```text
SolverNode
├─ nodeId
├─ role / requestId
└─ locationIndex ───────────────┐
                                ▼
PhysicalLocation
├─ locationId
├─ normalized attributes
└─ matrixIndex
```

다음 규칙을 적용한다.

1. matrix key는 request ID나 node ID가 아니라 physical location ID다.
2. 여러 node가 같은 physical location을 참조할 수 있다.
3. 하나의 request에서 pickup과 delivery가 같은 위치를 참조해도 node 역할은 구분된다.
4. 같은 location ID가 여러 입력 객체에 나타나면 좌표와 정규화에 사용되는 물리 속성이 일관되어야 한다.
5. 다른 location ID가 같은 좌표를 가져도 adapter가 임의로 하나의 location으로 병합해서는 안 된다.
6. terminal, 실제 pickup, delivery, delivery-only loading location은 모두 명시적인 location mapping을 가져야 한다.

이 구조 때문에 matrix 저장 크기는 solver node 수가 아니라 고유 physical location 수에 의해 결정된다.

### 5.3 Matrix validation

solve 시작 전 최소한 다음을 검증한다.

1. 모든 필수 node가 유효한 location ID와 matrix index를 참조한다.
2. 모든 arc endpoint가 알려진 location ID다.
3. 동일한 directed `(from, to)` arc가 중복되지 않는다.
4. 선택된 matrix 계약이 요구하는 arc coverage가 완전하다.
5. distance와 time은 정확한 10진수로 파싱 가능하고 유한하며 음수가 아니다.
6. 거리와 시간 각각에 등록된 단위·scale·rounding 정책이 있다.
7. 정규화와 경로 누적이 overflow하지 않는다.
8. 입력 matrix와 정규화 정책·변환 이력을 fingerprint/provenance에 포함한다.

**잠정 (`P-03`).** canonical v1은 physical location 기준 complete directed dense matrix를 `SHOULD` 사용한다. production 계약이 항상 `M²`인지, 별도 sparse 형식을 지원하는지는 `TBD(Q-MTX-03)`이다. 이 질문이 열려 있어도 누락 arc를 좌표 기반으로 채우는 fallback은 허용되지 않는다.

### 5.4 Legacy `F/T/D/U/C` 경계

fixture에서 `F`, `T`, `D`, `U`, `C`가 관찰되지만 canonical core에는 의미 없는 축약명을 전파하지 않는다.

```text
legacy arc
→ versioned adapter
→ fromLocationId
→ toLocationId
→ distance value + unit policy
→ travelTime value + unit policy
→ source metadata
```

다음은 열린 질문이다.

- `D`, `U`의 공식 의미·단위·허용 정밀도: `TBD(Q-MTX-01)`
- `C=O/G`와 diagonal `D=9999, U=0`의 공식 의미: `TBD(Q-MTX-02)`

따라서 legacy diagonal을 정상 거리로 누적하거나, 0으로 바꾸는 규칙을 이 초안에서 확정하지 않는다. 해당 fixture를 실행하려면 승인된 adapter 버전이 `Q-MTX-02`의 답과 provenance를 가진 호환 규칙을 먼저 제공해야 한다. 가상 pickup 표현 역시 unresolved diagonal에 의존해 추가 travel을 만들어서는 안 된다.

## 6. 차량 크기 `Feature`와 별도 capability

### 6.1 Vehicle size type

**확정 (`C-05`).** 기존 입력의 `Feature`는 냉장·리프트 같은 일반 기능 집합이 아니라 지역의 대형 차량 진입 제한에 사용하는 **차량 크기 유형**이다.

```text
예: 1t, 3t, 5t
legacy/fixture 예: T1, T1.4, T3.5, T5
```

의미 계약은 다음과 같다.

- vehicle은 하나의 `vehicleSizeType`을 가진다.
- request 또는 방문 제약은 허용 대안의 집합 `allowedVehicleSizeTypes`를 가진다.
- size compatibility는 membership이다.

```text
sizeCompatible(request, vehicle)
= vehicle.vehicleSizeType
  IN request.allowedVehicleSizeTypes
```

허용 집합의 여러 코드는 OR다. 코어는 코드 문자열의 숫자 크기나 톤급 순서를 추론하지 않는다. “최대 3t” 같은 입력은 adapter/profile이 승인된 registry로 명시적 허용 집합을 만든다.

### 6.2 Capability와 qualification

냉장, lift, 위험물 자격, 기사 qualification은 size type과 별도 축이다.

```text
capabilityCompatible(request, vehicle)
= request.requiredCapabilities
   SUBSET OF
   vehicle.vehicleCapabilities
```

| 축 | request 측 | vehicle 측 | 판정 |
|---|---|---|---|
| 차량 크기 | 허용 대안 집합 | 단일 유형 | membership |
| capability/qualification | 모두 필요한 요구 집합 | 보유 집합 | subset |
| 전용 차량 | 허용 vehicle ID 집합 | 단일 vehicle ID | membership |
| zone | 입력/profile이 정의한 허용 관계 | vehicle zone data | 승인된 규칙 |

size와 capability를 하나의 `features` 교집합으로 합치면 안 된다. capability 입력이 실제로 도입될 때는 별도 schema, code registry, 정적 호환 규칙으로 추가한다.

### 6.3 정적 호환성의 컴파일

adapter/transformer는 schema/profile이 서로 독립된 hard rule이라고 승인한 정적 규칙을 합성해 request별 `servableVehicles`를 만든다.

```text
candidate vehicles = all vehicles
for each approved independent static rule:
    candidate vehicles
    = candidate vehicles
      INTERSECT rule.allowedVehicles(request)

servableVehicles = candidate vehicles
```

size와 zone이 실제로 서로 독립된 두 규칙인지, 같은 제한의 중복 표현인지는 이 식을 적용하기 전에 `Q-COMP-02`로 결정해야 한다. 독립 규칙으로 승인된 경우에만 교집합으로 합성한다. 코어의 `Request.servableVehicles`는 최종 vehicle ID membership만 보유하며 ALNS나 route evaluator는 업무 코드 문자열을 다시 해석하지 않는다.

다음은 열린 질문이다.

- null/empty/`ALL`, code registry, unknown size code 정책: `TBD(Q-COMP-01)`
- size restriction과 zone의 결합, pickup/delivery 제한이 다를 때의 합성: `TBD(Q-COMP-02)`

답이 정해질 때까지 wildcard를 코어로 전달하거나 unknown code를 임의 허용해서는 안 된다. 후보 차량이 0인 유효 request는 정상적인 배정 불가이며, 호환 규칙 자체가 잘못 구성된 경우는 입력 오류다.

## 7. 요청 불변조건과 mutation/validation 경계

### 7.1 안정 상태의 정확한 파티션

전체 request 집합을 `R`, 안정 상태 route 집합을 `Routes`, `SearchRequestBank`를 `B`라고 한다. 각 request `r`의 전체 route 출현 횟수를 `pickupCount(r)`, `deliveryCount(r)`로 정의한다.

```text
ASSIGNED_IN_SEARCH(r)
= pickupCount(r) = 1
  AND deliveryCount(r) = 1
  AND 두 node가 같은 vehicle route에 존재
  AND pickupIndex(r) < deliveryIndex(r)
  AND r NOT IN B

UNASSIGNED_IN_SEARCH(r)
= pickupCount(r) = 0
  AND deliveryCount(r) = 0
  AND r IN B

모든 r에 대해
ASSIGNED_IN_SEARCH(r) XOR UNASSIGNED_IN_SEARCH(r)
```

이는 최종 업무 상태 enum을 정의하는 식이 아니다. 탐색 중 구조 파티션만 정의한다.

다음 불변조건은 모든 안정 상태에서 `MUST` 성립한다.

| 불변조건 | 규범 의미 |
|---|---|
| 완전성 | 모든 request는 route 또는 bank 중 정확히 한 곳에 있다. |
| 배타성 | route와 bank 멤버십은 상호 배타적이다. |
| pair 완전성 | pickup만 또는 delivery만 있는 partial request는 없다. |
| exactly-once | 배정 request의 두 node는 전체 해에 각각 정확히 한 번 있다. |
| same-vehicle | 두 node는 같은 vehicle route에 있다. |
| precedence | pickup이 delivery보다 앞선다. |
| request 단위 전이 | insert, remove, relocate, exchange, route removal, rollback은 request 전체를 상태 전이 단위로 삼는다. |
| 안정 상태 관찰 | evaluator, score, acceptance, observer, result conversion은 안정 상태만 관찰한다. |

delivery-only의 virtual pickup prefix 규칙은 위 공통 불변조건에 추가된다. optional split delivery를 위해 이 불변조건을 느슨하게 만들지 않는다.

### 7.2 Atomic mutation 계약

모든 공개 pair mutation은 구현 방식과 무관하게 다음 계약을 가진다.

```text
사전조건
- 입력 Solution이 전체 request 불변조건을 만족
- 대상 request와 route/bank 상태가 연산 종류에 맞음

성공 후조건
- pickup과 delivery, route, bank가 함께 변경됨
- 출력 Solution이 전체 request 불변조건을 만족
- 변경 route의 derived cache와 Solution aggregate/hash가
  무효화되거나 새 구조와 일치하게 재계산됨

실패 후조건
- route sequence, bank membership, cache visibility,
  score aggregate, structural hash가 호출 전과 동일
- partial, duplicated, missing request가 남지 않음
```

“원자적”은 데이터베이스 transaction이나 병렬 lock을 뜻하지 않는다. 내부에서 여러 단계를 수행해도 호출자와 관찰자는 전부 성공한 상태 또는 완전히 복원된 상태만 볼 수 있다는 뜻이다.

삽입 후보의 pickup/delivery position은 두 node를 모두 반영한 최종 route 인덱스로 해석하고 `pickupPosition < deliveryPosition`을 만족해야 한다. delivery-only pattern은 일반 pickup 위치를 탐색하지 않고 승인된 prefix/initial-load 표현을 사용한다.

초기 copy-on-write와 후속 apply/undo 중 어느 방식으로 계약을 구현할지는 `C-09`와 `P-11`에 따라 세션 22가 정의한다. 두 방식은 위 성공·실패 후조건과 전체 재검증 결과가 같아야 한다.

### 7.3 Validation boundary

| 시점 | 필수 검증 | 실패 분류 |
|---|---|---|
| adapter parsing 후 | schema/version, 형식, 필수값, ID·참조 | input error |
| `ProblemInstance` 생성 전 | request-node 일대일 매핑, load 상쇄, location/matrix, 단위·정책 snapshot | normalization error |
| mutation 전 | 대상 request의 route/bank 사전조건 | invalid move 또는 implementation error |
| mutation 반영 후 | 대상 pair, 변경 route, bank, cache 후조건 | rollback 후 implementation error |
| 안정 candidate 공개 전 | 전체 request 파티션과 route feasibility | candidate 폐기 |
| 최종 변환 전 | 전체 request 파티션 재검증 | 결과 생성 금지; 세션 23 verifier로 인계 |

구조 불변조건 실패는 정상적인 infeasible request가 아니라 구현 결함이다. 시간·용량·호환성에 의해 특정 후보가 거절되는 것과 같은 진단 코드 체계로 섞지 않는다.

## 8. 고정소수점, 단위, overflow, Constants

### 8.1 Fixed-point normalization

**확정 (`C-10`, `C-11`).** 소수 물리량은 변환 경계에서 정확한 10진수로 파싱하고 fixed-point integer로 정규화한다.

- 코어 feasibility와 누적에 `double` 또는 `EPS`를 사용하지 않는다.
- 같은 차원의 수요와 용량은 같은 input unit, scale, rounding을 사용한다.
- 거리와 비교되는 거리 제한, 시간과 비교되는 시간 제한도 각각 같은 내부 단위로 변환한다.
- 한 `ProblemInstance`의 정규화 정책은 생성 후 불변이다.
- policy ID/version, 단위, 자릿수, rounding, adapter version을 provenance/fingerprint에 포함한다.

외부 의미의 기본 단위는 최소한 다음처럼 구분한다.

| 차원 | 외부 의미 | 내부 표현 |
|---|---|---|
| weight | kg | `long` fixed-point weight unit |
| volume | CBM | `long` fixed-point volume unit |
| distance | matrix 계약의 거리 단위 | `long` fixed-point distance unit |
| time | planning/date-time 및 duration 계약 | `long` fixed-point time unit |
| quantity | 양의 정수 | checked integer |
| cost | 세션 21의 정책 계약 | 이 초안에서 산식 미정 |

정확한 유지 자릿수는 `TBD(Q-NUM-01)`, 초과 자릿수 rounding은 `TBD(Q-NUM-02)`, item 정규화와 qty 곱 순서는 `TBD(Q-NUM-03)`이다. `n=3`, floor, `HALF_UP` 등을 예시에서 기본값으로 가져오지 않는다.

### 8.2 Overflow와 잘못된 수치

다음 연산은 결과가 overflow한 뒤 검사하지 않고 사전 범위 검사 또는 checked arithmetic을 사용해야 한다.

```text
10^n scale 생성
field 정규화
item value × quantity
request line/item 합계
route load 누적
arc 합계
time/distance adjustment가 명시된 경우의 곱셈
route 및 solution aggregate
```

overflow, `NaN`, infinity, 음수 travel, wrap-around는 saturating 값이나 특별한 큰 수로 치환하지 않는다. 입력/정책 오류 또는 명시적 계산 실패로 처리하고 차원·필드·연산을 식별할 수 있어야 한다.

### 8.3 999 CBM

**확정 (`C-12`).** 부피 차원을 사용하지 않는 문제의 기본 차량 부피 용량은 `999 CBM`이다.

```text
request volume = 0
vehicle volume capacity = toVolumeUnits(999 CBM)
capacity source = DEFAULT_FOR_UNUSED_DIMENSION
```

다음 규칙을 적용한다.

- `999 CBM`은 유한한 기본값이며 infinity나 시스템 상한이 아니다.
- adapter가 부피 차원을 사용하지 않는다고 명시적으로 정규화한 경우에만 적용한다.
- 입력에 실제 vehicle volume capacity가 있으면 덮어쓰지 않는다.
- 실제 수요나 용량을 999로 clamp하지 않는다.
- 적용 여부와 정책 버전을 provenance에 남긴다.
- engine은 정규화 후 999를 특별 분기하지 않고 일반 유한 capacity로 처리한다.

실제 volume demand가 있는데 capacity 의미가 모호하거나 누락된 입력은 999를 “무제한”으로 조용히 적용하지 않는다. 해당 schema/version의 명시적 의미가 없으면 canonical instance를 만들지 않는다.

### 8.4 Constants와 configuration

수치 소유권은 다음 세 층으로 분리한다.

| 층 | 예 | 책임 |
|---|---|---|
| 의미별 정적 기본 정책 | `DEFAULT_VOLUME_CAPACITY_CBM = 999` | 같은 의미의 기본값을 한 곳에서 제공 |
| solve/configuration snapshot | numeric policy, adapter policy, matrix unit policy | 고객·입력별 선택과 버전·출처 |
| instance value | plan start/end, actual vehicle capacity, normalized arc | 이번 solve의 불변 값 |

범용 `Constants` 파일에 고객별 비용, plan end, route 계산 상태, 탐색 중 값, provider 설정을 모아서는 안 된다. tunable 값은 의미별 configuration으로 두고 출처와 버전을 보존한다.

## 9. Plan-end 상한과 explicit infeasible state

**확정 (`C-12`).** plan end는 시간축에서 허용되는 유한한 최댓값이며 infeasible sentinel이 아니다.

개념 계약은 다음과 같다.

```text
TimeComputationResult
├─ FEASIBLE(time)
└─ INFEASIBLE(reason)
```

구체 객체 할당 방식은 구현 선택이지만 상태와 값은 논리적으로 분리해야 한다.

- feasible일 때만 time 값을 읽을 수 있다.
- infeasible 결과에 travel/service duration을 더하지 않는다.
- `Long.MAX_VALUE`나 `planEnd + 1`을 시간 실패로 사용하지 않는다.
- 계산 overflow와 계획기간 초과를 구분한다.
- plan end를 넘는 값을 plan end로 clamp하지 않는다.
- plan end 자체의 포함 여부는 `TBD(Q-TIME-02)`다.

명시적 infeasible 상태는 route 계산 결과이며 `SearchRequestBank`에 저장하는 최종 사유가 아니다. 한 insertion의 시간 실패가 request의 최종 미배정 원인을 확정하지 않는다.

## 10. `SearchRequestBank` 경계

### 10.1 책임

**확정 (`C-15`).** `SearchRequestBank`는 탐색 중 어느 정규 route에도 속하지 않은 request ID membership만 관리한다.

```text
Search-time Solution
├─ Route[]
└─ SearchRequestBank
   └─ requestId membership
```

다음은 bank의 책임이다.

- request 단위 membership 추가·제거·조회
- 초기 빈 route 해에서 전체 request 보유
- pair insertion 성공 후 해당 request 제거
- pair destroy 성공 후 해당 request 추가
- candidate 복사, 수용, 거절, rollback에 route와 함께 참여

다음은 bank에 저장하지 않는다.

- pickup/delivery node별 membership
- 마지막 insertion 실패 이유
- 최종 assignment status
- unassignment diagnostic 또는 사용자 message
- deferred/outsourced 결정
- 고객사별 결과 정책 객체

삽입 실패는 route와 bank를 바꾸지 않는다. 거절 횟수 같은 탐색 통계가 필요하면 bank와 분리된 bounded telemetry로 수집하며, 그것도 최종 원인 증명이 아니다.

### 10.2 최종 결과와의 경계

탐색 종료 시 bank는 “정규 route에 현재 배정되지 않음”만 말한다. 최종 상태와 진단은 세션 23의 결과 계층이 다음 근거를 사용해 만든다.

- 검증된 최종 route/bank 파티션
- 정규화 단계의 정적 사실
- 세션 22의 종료·탐색 메타데이터
- 필요한 경우 명시적으로 수행한 최종 감사

세션 23은 bank를 결과 DTO로 그대로 노출하거나 단일 실패 reason으로 해석해서는 안 된다.

## 11. 잠정 사항과 열린 질문

### 11.1 잠정 사항

| ID | 이 초안에서의 잠정 방향 | 확정하지 않는 부분 |
|---|---|---|
| `P-01` | delivery-only와 real pickup-delivery의 service pattern을 명시적으로 구분 | 최종 타입·enum 이름, prefix node 대 initial-load 표현 |
| `P-02` | delivery-only v1의 virtual pickup을 route start prefix에 고정 | depot 작업시간 단위, 외부 노출, mixed route 세부 의미 |
| `P-03` | physical location 기준 complete directed dense matrix | production이 항상 `M²`인지, 별도 sparse 계약 |
| `P-12` | versioned legacy adapter가 제한된 coercion·alias를 처리 | 허용 alias, canonical JSON, unknown-field 정책 |

### 11.2 중앙 열린 질문

아래 ID는 [세션 19](19-integration-plan.md)의 중앙 질문을 그대로 사용한다. 이 초안은 답을 만들지 않는다.

| ID | 열린 의미 | 이 초안의 안전한 처리 |
|---|---|---|
| `Q-NUM-01` | 차원별 유지 소수 자릿수 | policy 없이는 정규화 시작 금지 |
| `Q-NUM-02` | 차원별 rounding mode | 임의 floor/round 금지 |
| `Q-NUM-03` | item 정규화와 qty 곱 순서 | 순서를 adapter 정책에 명시하기 전 합계 확정 금지 |
| `Q-MTX-01` | legacy `D/U` 의미·단위·정밀도 | 값 크기로 단위 추정 금지 |
| `Q-MTX-02` | `C=O/G`, diagonal `D=9999,U=0` 의미 | 정상 arc 또는 0으로 임의 해석 금지 |
| `Q-MTX-03` | canonical matrix의 항상-complete `M²` 여부 | P-03은 잠정, 누락 arc fallback은 금지 |
| `Q-TIME-01` | planning zone과 canonical date-time | 시간대 없는 값을 임의 zone에 결합 금지 |
| `Q-TIME-02` | plan end와 close의 포함 경계 | 경계 비교를 승인 전 전역 기본값으로 고정 금지 |
| `Q-TIME-03` | 서비스 시작 대 완료 제한 | profile별 변환 가능성만 보존 |
| `Q-TIME-04` | 반복·overnight·근무 종료 초과 이동 | 기존 문서의 daily repeat/pause-resume를 기본값으로 승격 금지 |
| `Q-IN-01` | `reqDate/dueDate`, `duration/taskTime` 의미 | 이름만으로 alias 또는 release/deadline 해석 금지 |
| `Q-IN-02` | depot task, multi-rotation, wait, stop/drive limit scope | reset·집계·회차 의미를 임의 결정 금지 |
| `Q-COMP-01` | size null/empty/`ALL`, registry, unknown code | wildcard와 unknown code를 코어로 전달 금지 |
| `Q-COMP-02` | size/zone 결합과 pickup/delivery 제한 합성 | transformer가 승인 전 교집합/AND를 가정하지 않음 |
| `Q-REQ-01` | delivery-only와 real pickup-delivery route 혼합 | 혼합 의미 승인 전 canonical mixed route 생성 금지 |
| `Q-REQ-02` | multi-trip 지원과 pair의 trip 경계 | 현재 표준 단일 회차를 유지; multi-trip은 OPEN/DEFERRED |

## 12. Traceability

### 12.1 확정 결정 추적

| 결정 | 이 초안 절 | 주요 근거 |
|---|---|---|
| `C-01` | §3.1 | [세션 02](02-rpdptw-terminology.md) |
| `C-05` | §6 | [세션 05](05-vehicle-size-and-constraints.md), fixture read-only 관찰 |
| `C-06` | §3.3, §7 | [세션 06](06-request-pair-invariants.md), [PDPTW 원본 요약](../orgin/alns_pdptw_paper_summary_ko.md) |
| `C-07` | §3.5 | [세션 03](03-rpdptw-model-recheck.md), [세션 11](11-input-schema-and-time-contract.md) |
| `C-08` | §1, §13.2 | 세션 19; 종료 세부는 세션 22로 인계 |
| `C-09` | §7.2 | 세션 19; 표현·rollback 세부는 세션 22로 인계 |
| `C-10` | §8.1 | [세션 09](09-fixed-point-policy.md) |
| `C-11` | §8.1, §11.2 | [세션 09](09-fixed-point-policy.md), 세션 19 |
| `C-12` | §4.3, §8.3~§9 | [세션 10](10-domain-limits-and-constants.md), 세션 19 |
| `C-13` | §5 | [세션 12](12-distance-time-input.md), fixture read-only 관찰 |
| `C-14` | §4.4 | [세션 11](11-input-schema-and-time-contract.md), [세션 12](12-distance-time-input.md) |
| `C-15` | §10 | [세션 13](13-unassigned-status-and-diagnostics.md) |

### 12.2 기존 설계에서 교체할 내용

현재 [Master Design](../master-design.md)과 [Domain Design](../domain-design.md)은 역사적 초안으로만 사용한다. 세션 25 통합 시 다음 의미를 이 초안으로 교체해야 한다.

| 기존 표현 | 교체 계약 |
|---|---|
| 구 문제 약어 | §3.1의 RPDPTW/PDPTW 구분 |
| generic feature 교집합 | §6의 size membership과 capability subset 분리 |
| 자유로운 depot pickup 삽입 | §3.5의 delivery-only start-loading 의미 |
| precedence 위주의 pair 검사 | §7의 same vehicle, exactly once, route/bank XOR, atomic handling |
| node 수 기준 travel matrix | §5의 physical-location matrix와 node mapping |
| Haversine/속도 기반 matrix 생성 | §5의 authoritative directed input과 no-fallback |
| `Long.MAX_VALUE` volume/time sentinel | §8.3과 §9의 999 CBM·plan end·explicit infeasible |
| `RequestBank`의 최종 reason 저장 | §10의 search-only membership |
| 고정 `n=3`, floor, g/L 예시 | §8의 차원별 fixed-point와 `TBD(Q-NUM-*)` |
| base date와 days 위주의 horizon | §4.3의 exact planning period |
| `reqDate` release, start-only, pause/resume의 전역 확정 | `Q-IN-01`, `Q-TIME-03~04` |

## 13. 세션 21·22·23 handoff

### 13.1 세션 21이 소비하는 계약

세션 21의 policy/objective 설계는 다음 정규화 facts를 입력으로 사용하고 물리 의미를 다시 계산하지 않는다.

- immutable request/node/vehicle/location ID mappings
- request별 `servableVehicles`
- normalized capacity, time, directed distance/time
- explicit feasible/infeasible propagation outcome
- neutral route facts를 만들 수 있는 node roles와 service meaning
- numeric, adapter, matrix provenance snapshot

세션 21은 size code, legacy alias, coordinates, raw decimal을 다시 해석해 feasibility를 바꾸지 않는다. cost와 objective 비교는 이 초안의 hard feasibility와 분리한다.

### 13.2 세션 22가 소비하는 계약

세션 22의 algorithm 설계는 다음을 모든 construction/destroy/repair/local/fleet move의 사전·후조건으로 사용한다.

- request pair는 operator의 최소 이동 단위
- same vehicle, each node exactly once, pickup-before-delivery
- route XOR `SearchRequestBank`
- delivery-only의 pattern-aware insertion 위치
- mutation 성공 시 전체 반영, 실패·거절 시 전체 복원
- observer, score, acceptance는 안정 상태만 관찰
- travel lookup은 physical-location directed matrix만 사용

정상 종료 `maxSteps`와 watchdog 분리 (`C-08`), 초기 copy-on-write와 후속 apply/undo (`C-09`, `P-11`)는 세션 22가 위 외부 계약을 보존하도록 상세화한다.

### 13.3 세션 23이 소비하는 계약

세션 23의 result/verifier 설계는 다음을 입력으로 받는다.

- 외부 order ID와 normalized request ID의 양방향 매핑
- 검증된 final routes와 `SearchRequestBank` 파티션
- request pair, terminal, capacity, time, compatibility 불변조건
- normalized authoritative matrix와 provenance
- adapter/numeric/time 정책 snapshot
- explicit 계산 실패와 구조 오류의 구분

세션 23은 최종 assignment status와 diagnostic을 별도로 만들고 bank에 역으로 저장하지 않는다. 독립 verifier는 좌표 기반 travel을 만들지 않고 같은 정규화 matrix로 거리·시간을 재계산해야 한다.

## 14. 범위 self-audit

| 검사 | 결과 |
|---|---|
| 수정 대상 | 이 문서만 생성 |
| 표준 문제명 | RPDPTW 사용, 학술 `PDPTW` 의미 보존 |
| vehicle `Feature` | size type membership으로 한정, capability/qualification 분리 |
| request pair | same vehicle, exactly once, pickup-before-delivery, route/bank XOR, atomic failure 포함 |
| delivery-only | start loading 의미 보존, 표준 단일 회차의 중간 재적재 금지 |
| 수치 | fixed-point 필수, `n`·rounding·qty 순서는 열린 질문 |
| 한계값 | 999 CBM과 plan end를 유한값으로 사용, failure는 explicit state |
| travel | 입력 directed matrix 권위, physical-location mapping, 묵시적 fallback 금지 |
| legacy evidence | PDF와 fixture를 read-only·비규범 자료로 한정 |
| search/result 경계 | `SearchRequestBank`에는 membership만 저장 |
| 열린 질문 | 세션 19의 `Q-NUM/MTX/TIME/IN/COMP/REQ` ID만 재사용하고 답을 발명하지 않음 |
| 비범위 | objective/ALNS/benchmark/result DTO/provider topology/optional variant 구현 세부 제외 |

이 초안은 세션 25가 Master의 표준 문제 모델, 핵심 불변조건, 입력·정규화, 거리·시간 데이터, search-state 경계를 작성할 때 사용하는 규범적 통합 입력이다.
