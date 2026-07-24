# RPDPTW 통합 솔버 Master Design: 입문자용 안내서

> 대상 독자: Java 7 개발 경험이 있고 CVRPTW와 메타휴리스틱의 기본 개념은 알지만, RPDPTW·ALNS·Simulated Annealing(SA)·검증 결과 모델은 아직 익숙하지 않은 사람
>
> 기준 문서: [master-design.md](master-design.md) v3.0-review (`2026-07-24`)
>
> 함께 볼 문서: [Domain Design](domain-design.md), [질문 등록부](master-design-open-questions.md)
>
> 이 문서의 목적은 기준 문서를 **쉽게 읽게 돕는 것**이다. 새 계약을 정하거나 기준 문서와 충돌하는 구현 결정을 내리지 않는다. 모호하거나 다르게 읽히는 경우에는 항상 `master-design.md`와 승인된 결정이 우선한다.

## 1. 먼저 한 문장으로 이해하기

이 설계가 만들려는 것은 단순히 “좋은 경로를 찾는 ALNS 프로그램”이 아니다.

**고객별 규칙을 profile로 바꿔 끼우되 pickup/delivery 쌍과 물리 제약을 절대 깨뜨리지 않고, 검색 결과를 두 단계로 독립 검증하며, 같은 고정 실행 조건에서 재현 가능한 RPDPTW 솔버**를 만드는 설계다.

CVRPTW에서 출발하면 다음처럼 확장해서 생각하면 된다.

```text
CVRPTW의 방문 순서·용량·시간창
  + pickup과 delivery의 쌍 관계
  + 차량 크기·자격·구역·근무창 같은 업무 제약
  + 고객사별 비용과 우선순위
  + 검색 상태와 최종 업무 결과의 분리
  + 독립 검증과 실행 provenance
  = RPDPTW 통합 솔버
```

여기서 “통합”은 고객 A와 B가 같은 알고리즘 코어를 쓰되 서로 다른 policy/profile을 조립한다는 뜻이다. `if (customer == "A")`를 route propagator나 ALNS 본체에 추가하는 구조는 피한다.

## 2. 기준 문서의 지위와 읽는 법

`master-design.md`의 상태는 `REVIEW`다. 현재 코드나 benchmark가 이미 구현됐다는 보고가 아니라, **앞으로 구현과 검증이 따라야 할 계약과 순서**다. 따라서 코드 구조를 먼저 정하기보다 “어떤 상태가 절대 나오면 안 되는가?”와 “어떤 결과만 공개할 수 있는가?”를 먼저 읽는다.

| 표기 | 쉽게 말하면 | 구현할 때의 태도 |
|---|---|---|
| MUST / MUST NOT | 깨지면 안 되는 계약 | 테스트와 verifier가 반드시 확인한다. |
| SHOULD | 특별한 근거 없이는 따를 기본 방향 | 벗어나면 이유와 영향 범위를 남긴다. |
| `RESOLVED` | 결정된 질문 | 그 정확한 의미를 적용한다. |
| `OPEN — EXPERIMENT_REQUIRED` | 실험 방식은 정했지만 공식 수치는 없음 | production 기본값을 만들지 않는다. |
| `DEFERRED` | 이번 roadmap 밖 | 임의로 활성화하거나 현재 설계에 섞지 않는다. |

Master는 “무엇을 어떤 순서로 구현하는가”를, Domain Design은 domain·정규화·travel·결과의 더 세밀한 의미를, 질문 등록부는 질문 상태를 맡는다. 세션 문서는 판단의 이력을 보존하지만 현재 구현의 독립 규범 문서는 아니다.

## 3. 전체 흐름: 입력부터 publication까지

솔버의 중심 흐름은 다음과 같다.

```text
외부 제출
  → versioned adapter / canonical business input
  → 정규화 + Travel Matrix preparation
  → immutable problem + exact bound profile
  → 결정론적 initial solution + COW ALNS
  → committed candidate
  → candidate solution verifier
  → verified solution + final-solution insertion audit
  → final outcomes / diagnostics
  → result-integrity verifier
  → publication / retrieval
```

| 단계 | 하는 일 | 하면 안 되는 일 |
|---|---|---|
| Adapter·정규화 | 외부 field, ID, 단위, 시간, 서비스 의미를 canonical model로 고정 | 알고리즘 중 raw JSON 의미를 다시 추측 |
| Travel preparation | sparse/누락 travel을 모두 해소한 directed travel snapshot 생성 | search 중 reverse arc를 복사하거나 lazy 생성 |
| Profile binding | 정확한 고객 profile/version/config를 immutable snapshot에 결합 | `latest`나 비슷한 profile로 fallback |
| Portfolio·ALNS | feasible candidate를 생성·개선 | hard 제약 위반을 낮은 점수로 숨김 |
| Candidate verifier | candidate의 구조·물리 제약·평가를 cache 없이 재계산 | solver cache/summary/feasible flag 신뢰 |
| Finalization·audit | 모든 request의 결과와 근거를 만들고 검증 | search bank나 마지막 삽입 실패를 final truth로 사용 |
| Result verifier·publication | 결과 partition, summary, payload를 다시 확인한 뒤 공개 | verifier 하나만 통과한 결과를 정상 발행 |

이 흐름의 핵심은 “검색이 찾은 후보”와 “외부에 공개 가능한 업무 결과”가 다른 산출물이라는 점이다.

## 4. 문제의 핵심 객체

### 4.1 `Request`는 고객 노드 하나가 아니라 운송 의무다

실제 pickup-delivery request는 pickup과 delivery를 묶은 **원자적 pair**다.

```text
Request R17
  pickup:  P17에서 화물 +3 적재
  delivery: D17에서 같은 화물 -3 하차
```

정상 검색 상태에서는 다음 둘 중 하나만 가능하다.

```text
ASSIGNED_IN_SEARCH
  = 같은 vehicle route에 pickup과 delivery가 각각 한 번 있고 pickup이 먼저
    + SearchRequestBank에는 없음

UNASSIGNED_IN_SEARCH
  = 모든 route에 pickup과 delivery가 없음
    + SearchRequestBank에 request ID가 정확히 한 번 있음
```

pickup만 넣고 delivery를 “나중에” 넣는 중간 상태를 다른 코드가 보면 안 된다. 삽입·삭제·교환·rollback의 최소 단위는 node가 아니라 request pair다.

`delivery-only` order도 있다. 이는 실제 depot 방문을 추가하는 pair가 아니라, 출발 전 이미 적재된 화물을 고객에서 내리는 서비스다. 같은 route의 delivery-only demand 합이 initial load가 되며, 중간 depot 재적재로 용량을 우회할 수 없다. 실제 pickup-delivery와 delivery-only는 한 single-trip route에 함께 있을 수 있다.

### 4.2 `Route`, `Solution`, `SearchRequestBank`

```text
Solution
 ├─ Route(vehicle V1): start terminal → 서비스 노드 … → 끝
 ├─ Route(vehicle V2): start terminal → 서비스 노드 … → 끝
 └─ SearchRequestBank: 아직 배정되지 않은 request ID 집합
```

Route는 하나의 vehicle에 결합된다. 현재 범위는 single-trip이다. `oneway`는 start terminal에서 출발해 마지막 고객에서 끝나고, `roundtrip + multiRotation=0`은 같은 depot으로 한 번 돌아온다. 중간 depot 재방문, 다회전(multi-trip/rotation)은 현재 범위 밖이다.

`SearchRequestBank`는 검색 중 membership만 가진다. 비용, last failure, 최종 미배정 사유, 외주/이월 상태를 넣지 않는다. 그것들은 finalization 후 별도 result에만 있을 수 있다.

### 4.3 차량 크기, capability, zone, ownership은 별도 축이다

`Feature`는 차량 크기 유형이다. vehicle은 구체 코드 하나, request는 허용 코드 집합을 가진다. 코드의 숫자나 순서로 대소 관계를 추론하지 않고 대소문자를 구분한 exact equality로 비교한다.

```text
sizeCompatible
= request.allowedVehicleSizeTypes 에 vehicle.vehicleSizeType 이 포함됨
```

request의 정확한 `['ALL']`은 모든 size를 허용한다. `ALL`과 구체 code를 섞거나 vehicle의 size를 비워 두는 것은 허용하지 않는다.

냉장·리프트·위험물·기사 자격은 별도 capability subset이며, size와 AND로 적용한다. zone도 독립 hard constraint다. route에는 `ALL`을 제외한 구체 zone 하나만 나타날 수 있다. pickup과 delivery의 구체 zone이 다르면 pair는 배정할 수 없다.

`DIRECT`와 `LEASE` vehicle은 모두 입력 fleet의 실제 배정 자원이다. `LEASE` 배정도 `ASSIGNED`이며, solver가 fleet 밖의 외주 vehicle을 새로 만들거나 미배정을 외주/이월로 추정하지 않는다.

## 5. 안전장치: 불변조건과 atomic mutation

### 5.1 Pair partition은 항상 지킨다

다음은 “삽입이 불가능했다”가 아니라 구현 결함이다.

- pickup 또는 delivery만 존재하는 partial pair
- 같은 node/pair의 중복
- pickup과 delivery가 다른 vehicle route에 있음
- delivery가 pickup보다 앞섬
- route와 bank에 동시에 있거나, 둘 다에 없음

### 5.2 Route feasibility는 점수가 아니다

각 안정 route는 vehicle/terminal 정책, service pattern, load prefix, directed travel, 시간창, 근무창, stop·drive resource, profile의 hard constraint를 만족해야 한다. 각 load 차원은 항상 `0 <= load <= capacity`다.

Hard-infeasible candidate는 벌점이 큰 해가 아니다. **비교와 acceptance에 도달하기 전에 탈락**한다. SA의 높은 온도도 이를 뒤집을 수 없다.

### 5.3 후보 변경은 전부 성공하거나 전혀 남기지 않는다

```text
변경 성공
  → pair, 영향 route, bank, cache/aggregate/fingerprint를 함께 갱신

변경 실패·거절·취소·예외
  → route, bank, cache visibility, score/objective, fingerprint가 호출 전과 동일
```

초기 기본 전략은 copy-on-write(COW)다. 변경할 route만 복사한 candidate에서 수정하고, accepted candidate만 새 immutable snapshot으로 확정한다. `current`, `stageBest`, `solveBest`를 직접 mutate하거나 기본 경로로 apply/undo를 쓰지 않는다.

## 6. 입력 정규화: 알고리즘 전에 의미를 고정한다

### 6.1 외부 입력을 core에 바로 넣지 않는다

```text
external bytes/reference
  → schema/version adapter
  → syntax/reference validation
  → business meaning
  → canonical business model
  → numeric/time/location/compatibility normalization
  → immutable ProblemInstance + provenance
```

Adapter는 승인된 제한적 alias/coercion만 처리한다. 예를 들어 legacy field를 해석했다면 그 사실을 provenance에 남긴다. 모호하거나 지원하지 않는 의미는 solve 전에 거부하며, core·search·verifier가 raw input을 다시 해석하지 않는다.

### 6.2 숫자와 시간 계약은 이제 구체적이다

기준 문서에는 더 이상 “적당한 scale을 나중에 고른다”는 기본값이 없다.

- 무게·부피는 non-negative decimal을 소수 셋째 자리에서 `FLOOR`한 fixed-point 정수로 정규화한다. item별로 먼저 정규화한 뒤 `qty`를 곱한다.
- 비용·거리·시간 입력은 정수 계약이다. 소수값을 반올림해 받아들이지 않는다.
- overflow, 비유한 값, 계산 실패를 큰 숫자나 `planEnd`로 바꾸지 않는다. checked arithmetic으로 오류를 낸다.
- 부피 차원을 쓰지 않는다고 adapter가 명시한 경우에만 vehicle volume capacity 기본값은 유한한 **999 CBM**이다. 무제한을 뜻하지 않는다.

모든 solver 시간은 timezone/offset이 제거된 exact `yyyy-MM-dd HH:mm:ss`를 adapter가 plan origin 기준 `long` second로 바꾼 하나의 축을 쓴다. planning period는 `planStart <= t < planEnd`다. customer window close는 포함 경계이고 기본 `START_ONLY` profile에서는 `serviceStart <= closeTime`이면 된다. `COMPLETE_WITHIN_WINDOW` profile은 `serviceEnd <= closeTime`을 요구할 수 있다.

날짜 없는 window는 planning period에 포함되는 날마다 반복한다. overnight window도 전개한다. travel arc는 하나의 이용 가능한 근무창 안에 전부 들어가야 하며, arc 중간에 멈췄다가 다음 근무창에 이어 갈 수 없다.

### 6.3 Service time을 임의로 해석하지 않는다

`duration`은 request 수준 고정 서비스시간이고 `item.taskTime`은 item 한 단위의 처리시간이다.

```text
serviceTime = request.duration + Σ(item.taskTime × item.qty)
```

order-level `taskTime`을 item에 나누거나 `duration` alias로 간주하지 않는다. legacy `reqDate`와 `dueDate`는 같은 완료기한 alias로 해석한다. `oneway`에서는 `multiRotation` 원문이 있어도 route 의미에 영향을 주지 않는다.

### 6.4 Matrix는 위치 기준의 준비 완료 travel authority다

```text
raw provided/generated travel sources
  → Travel Matrix preparation
  → complete directed distances + vehicle-resolved times
  → solver node의 physical location lookup
  → solver와 verifier가 함께 사용
```

Matrix key는 request/node ID가 아니라 physical location ID다. 같은 위치에 pickup, delivery, terminal이 있어도 역할이 합쳐지는 것은 아니다.

제공된 `D`는 directed meter, 제공된 `U`는 directed second이며 정수만 받는다. self arc는 언제나 `D=0`, `U=0`이다. 누락 `D`는 좌표에서 Great Circle로 계산해 `HALF_UP`한 meter로 만들고, 누락 `U`는 vehicle speed(없으면 45 km/h)로 `CEILING(D × 3.6 ÷ speed)`한 second로 만든다. 모든 physical-location pair와 사용 vehicle의 time을 **solve 전에** 해소해야 한다.

`A→B`를 `B→A`에 복사하거나 대칭 평균을 내지 않는다. travel은 search 중 lazy 생성하지 않는다. solver와 verifier는 동일한 prepared travel fingerprint를 사용한다.

## 7. 고객별 규칙을 넣는 위치

이 설계는 큰 cost 함수 하나에 모든 규칙을 넣지 않고 역할을 나눈다.

```text
normalized immutable facts
  → propagation + structural/static hard gates
  → policy-neutral facts and metrics
  → hard constraints
  → score components
  → objective comparator
  → SolvePlan stages
```

| 층 | 예시 | 원칙 |
|---|---|---|
| Propagation / hard gate | 시간 진행, 용량, size·zone·자격 | 물리 사실과 허용 가능성만 결정 |
| Neutral metric | 총 거리, customer/depot waiting, 냉장 차량 사용량 | 가격이나 최종 원인을 포함하지 않는 사실 |
| Score policy | 거리 비용, 유료도로 비용, soft penalty | feasible 사실을 비용으로 변환 |
| Objective comparator | 미배정 수 우선, 그다음 비용 | 양보 불가 우선순위는 ordered dimension으로 비교 |
| `SolvePlan` | stage, warm-start, guard, budget 참조 | hard rule을 풀 권한 없음 |

새 요구는 input meaning → static compatibility → hard constraint → neutral metric → score → comparator → `SolvePlan`의 가장 좁은 seam에 둔다. 고객 ID나 objective label로 common propagator/ALNS를 분기하지 않는다.

실행 하나는 exact profile key/version/config에 bind된다. missing dependency, unit/schema mismatch, unknown/unauthorized preset은 실행 전에 거부한다. `latest`나 다른 고객사의 비슷한 preset으로 fallback하지 않는다.

`mandatoryUnassignedCount`는 필요한 profile에서 최상위 objective dimension일 수 있다. 이것은 hard constraint나 Big-M penalty가 아니므로, 완전 배정이 불가능할 때도 가장 적은 미배정의 verified partial solution을 비교할 수 있다.

## 8. 초기해와 ALNS를 이 설계 안에서 보기

### 8.1 초기 portfolio는 8개 독립 candidate를 만든다

초기 portfolio는 request-route 성장 정책 4개와 vehicle 순서 2개를 조합한다. 따라서 가능한 조합은 최대 8개이며, 한 candidate가 8개로 늘어나는 것이 아니라 **각 조합이 독립적으로 initial solution 하나씩** 만든다.

| Request-route 성장 정책 | seed와 route 성장 방식 |
|---|---|
| `CLOCK` | 선택 vehicle의 depot에서 0도 clockwise로 request entry location을 순회한다. 필요한 depot/request 좌표가 없으면 이 조합은 생성하지 못한다. |
| `SEQ_FARTHEST` | depot에서 가장 먼 request를 seed로 고르고, 이후 마지막 service location에서 가까운 request를 고른다. |
| `SEQ_LARGE_DEMAND` | 선택 vehicle 대비 weight/volume utilization이 큰 request를 seed로 고르고, 동률이면 이른 `reqDate`, request ID 순으로 고른다. |
| `SEQ_EARLIEST_DEADLINE` | 가장 이른 `reqDate`를 seed로 고르고, 동률이면 utilization이 큰 request, request ID 순으로 고른다. |

두 vehicle 순서는 모두 `DIRECT`를 먼저 시도하고 불가능할 때만 `LEASE`를 본다. `DIRECT_FIRST_LARGE`는 request 대비 여유가 큰 vehicle, `DIRECT_FIRST_SMALL`은 request를 더 촘촘하게 채우는 vehicle을 우선한다. 이때 큰/작은 판단은 `Feature` 문자열이 아니라 weight/volume capacity 대비 request demand 비율로 한다.

정책은 후보 순서만 정한다. 모든 request는 공통 pair evaluator가 pickup/delivery, time window, capacity, travel, zone과 capability를 확인한 뒤 feasible할 때만 route에 들어간다. 각 candidate는 생성 route와 policy/config/fingerprint를 기록한다.

### 8.2 Phase-1 screen과 phase-2 ALNS

사용 가능한 initial candidate는 각각 고정 `screenMaxSteps`만큼 ALNS를 실행한다. 모든 screen 결과를 comparator로 비교해 가장 좋은 **phase-1 champion** 하나를 고른다.

Phase 2는 이 champion을 공통 warm start로 사용한다. seed와 destroy/repair 설정이 다른 worker들을 한 batch로 실행하고, worker가 모두 끝난 뒤 round champion을 고른다. 이전 champion보다 엄격히 좋으면 다음 round로 진행하고, 같거나 나쁘면 종료한다. `maxRounds`도 별도 종료 상한이다.

시간은 품질 종료 조건이 아니다. `screenMaxSteps`, phase-2 worker의 `phase2MaxSteps`, worker 수와 `maxRounds`는 아직 실험으로 정할 값이다. cloud orchestration은 이 logical worker들을 나중에 실행·재시도·fan-in하는 역할이며 core 알고리즘의 일부가 아니다.

### 8.3 Pair insertion evaluator는 side-effect-free다

```text
stable solution + bank request + target route + pickup/delivery positions
  → structural/service-pattern gate
  → servableVehicles gate
  → hard-feasibility propagation
  → feasible option의 facts/metrics/ranking
```

Evaluator는 committed state를 먼저 바꾸지 않는다. feasible option만 ranking에 넣고, 마지막 동점은 stable request/vehicle/route/position identity로 해소한다. 선택한 move를 atomic 적용한 결과는 cache-free 재계산 결과와 정확히 같아야 한다.

### 8.4 ALNS의 한 step

```text
committed current
  → request-pair destroy
  → bank 기반 pair repair
  → configured bounded improvement
  → stable candidate validation/evaluation
  → hard-feasibility + stage guard
  → acceptance
  → commit 또는 discard
  → current/best와 adaptive state 갱신
  → completed step +1
```

destroy와 repair의 단위는 request 수다. repair 뒤 삽입되지 못한 request는 완전한 pair가 route에 없는 채 bank에 남는다. feasible하지만 나쁜 candidate는 SA acceptance로 `current`가 될 수 있지만, `stageBest`와 `solveBest`를 나쁘게 만들 수 없고 stage guard도 넘어설 수 없다.

## 9. Cache, 종료, 재현성

Route sequence, request ownership, vehicle/terminal binding, bank가 source of truth다. arrival/load, feasibility, metrics, score, objective, insertion table, aggregate는 모두 버리고 다시 계산할 수 있는 파생 cache다. route나 bank가 바뀌면 영향받은 cache와 fingerprint를 무효화한다.

정상 품질 예산은 stage별 양의 `maxSteps`다. wall-clock은 quality를 결정하는 시간이 아니라 병리적 장기 실행을 막는 monotonic watchdog 용도다.

| 종료 | 의미 |
|---|---|
| `MAX_STEPS_REACHED` | 모든 계획 stage의 budget을 완료한 정상 종료 |
| `NO_STRICT_IMPROVEMENT` | phase-2의 완결 batch가 이전 champion을 엄격히 개선하지 못한 정상 종료 |
| `MAX_ROUNDS_REACHED` | configured phase-2 round 상한에 도달한 정상 종료 |
| `WATCHDOG_REACHED` | 안전상 장기 실행을 중단 |
| `CANCELLED` | 외부 취소를 협력 처리 |
| `RESOURCE_LIMIT_REACHED` | solver 자원 안전 한계 |
| `PLATFORM_TIMEOUT` | 상위 실행 경계 때문에 termination record를 완성하지 못함 |
| `FAILED` | 구현·실행·platform 오류 |

미완료 step은 counter, temperature/acceptance, adaptive state를 전진시키지 않고 candidate를 완전히 discard한다. 예외 종료 뒤 last committed best가 있어도 두 verifier를 통과한 recovery candidate일 뿐, 정상 완료나 official benchmark run은 아니다.

강한 재현성은 정상 `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED`에서만 주장한다. problem/prepared travel/numeric-time-adapter fingerprint, exact profile/config, algorithm/operator/state 전략 version, build/runtime, base·derived seed, warm-start lineage, phase별 `maxSteps`, stable iteration/reduction/tie-break order가 모두 같아야 한다. seed만 같다고 충분하지 않다.

## 10. 후보 검증과 최종 결과를 분리하는 이유

### 10.1 첫 번째 gate: candidate solution verifier

solver는 성능을 위해 incremental cache를 쓴다. publication 전에 candidate verifier가 다음 권위 입력만으로 cache 없이 다시 계산한다.

```text
immutable problem/profile declaration
+ candidate route/node order
+ candidate SearchRequestBank
+ authoritative prepared directed travel
```

이 verifier는 pair partition, terminal·vehicle·service pattern, size/capability/zone, 모든 directed leg, load/time/window/resource, hard constraints, metric/score/objective를 확인한다. solver feasibility flag, cached value, insertion result, solver summary는 권위 입력이 아니다.

### 10.2 finalization은 bank 직렬화가 아니다

candidate `PASS`와 verified solution 뒤에만 finalization이 시작된다.

```text
verified solution
  → preliminary ASSIGNED / UNASSIGNED partition
  → required final-solution insertion audit
  → final outcomes / structured diagnostics
```

모든 input request는 정확히 하나의 `ASSIGNED` 또는 `UNASSIGNED` outcome을 가진다. `ASSIGNED`는 exactly one verified route/vehicle/pair를 참조하고, `UNASSIGNED`는 route를 참조하지 않는다. `LEASE` route에 배정된 request도 `ASSIGNED`다.

정규화/static precheck가 순서와 탐색에 무관한 불가능성을 증명한 request만 `PROVEN` diagnostic을 받을 수 있다. 나머지 `UNASSIGNED` request는 final routes를 고정한 채 모든 eligible vehicle과 합법 pickup/delivery position pair를 검사하는 insertion audit를 거친다.

audit가 모든 option의 실패를 확인하면 `EXHAUSTIVE_FOR_FINAL_SOLUTION` confidence를 사용할 수 있다. 이는 **현재 final routes에서** insertion이 없다는 뜻이지, 전역 재배치 불가능성 증명은 아니다. audit가 feasible insertion을 발견해도 자동 삽입·재탐색하지 않으며, 발견 사실만 내부 audit record에 남긴다.

### 10.3 두 번째 gate: result-integrity verifier

두 번째 verifier는 candidate `PASS` report와 verified solution, final outcomes, diagnostic/audit evidence, outcome-derived summary, publishable payload를 입력으로 받는다. exactly-one outcome partition, assignment/reference 일치, audit completeness와 confidence ceiling, summary와 payload fingerprint를 cache 없이 검증한다.

어느 gate든 `FAIL` 또는 미완료면 정상 route/outcome payload와 benchmark vector를 발행하지 않는다. candidate verifier 실패를 단순 미배정으로 바꾸거나, result verifier 실패를 candidate `PASS`로 덮을 수 없다.

## 11. Win PoC benchmark를 올바르게 읽기

1차 fixture는 `data/win_poc_case.json`이다. 읽기 전용 input fixture일 뿐 정답 route나 baseline이 아니다. 현재 fixture의 소수 `D/U`는 canonical 정수 meter/second 계약에 맞지 않으므로, 정수 matrix가 제공되기 전에는 official baseline에 쓸 수 없다.

Win PoC 전용 quality vector는 다음의 exact lexicographic order다.

```text
미배정 주문 수
  → 배차 차량 수
  → 전체 거리
  → 전체 운영시간
```

첫 번째로 다른 성분만 승패를 결정한다. 거리 1km를 줄여도 미배정 주문이 하나 늘면 더 나쁘다. 전체 운영시간은 사용 route들의 drive time, customer/depot waiting, service time, 근무창 사이 rest를 합한 값이며, solver/검증 elapsed나 미사용 vehicle 시간은 품질 성분이 아니다.

이 comparator는 모든 고객사의 일반 objective가 아니라 Win PoC benchmark 전용이다. fixture, adapter, numeric/time/matrix, profile, algorithm/build, seed/step, verifier와 metric formula가 같은 immutable manifest/card에 고정되지 않으면 quality를 비교하지 않는다.

official multi-round 실행에서는 manifest가 선언한 모든 worker가 같은 contract와 각자의 derived seed로 정확한 `maxSteps`를 완료하고 검증을 통과해야 한다. 일부 worker만 성공해 champion을 만들거나 다음 round로 갈 수 없다. worker completion order와 물리 병렬성은 결과에 영향을 주지 않아야 한다. round/worker 수와 official budget은 아직 실험 대기다.

## 12. 구현 순서를 읽는 법

```text
RM-0 결정·문서 baseline
  → RM-1 versioned input / immutable domain / prepared travel
  → RM-2 propagation / evaluation / bound profile
  → RM-3 pair evaluator / initial portfolio
  → RM-4 COW ALNS / cache / termination / reproducibility
  → RM-5 두 verifier / finalization / publication
  → RM-6 Win PoC official workflow
  → RM-7 COW profiling과 선택적 state 전략 재검토
  → RM-8 logical-port integration / migration
  → RM-9 별도 승인 후속 작업
```

이 순서는 기능 목록이 아니라, 다음 단계가 믿을 immutable authority를 먼저 만드는 순서다. 예를 들어 ALNS를 먼저 구현해도 travel, pair invariant, profile, verifier가 없다면 “좋은 해”를 주장할 근거가 없다.

`RM-7`도 중요하다. apply/undo는 성능 기법일 뿐 자동 다음 단계가 아니다. COW가 실제 병목이라는 측정, COW와 같은 trace/candidate/final result, fault injection과 full verifier 동등성, 별도 승인이 모두 있을 때만 검토한다. 그렇지 않으면 COW를 유지하는 것이 정상 결과다.

## 13. 기준 문서 추천 읽기 순서

1. **§2와 §4**: 범위, 전체 컴포넌트와 흐름을 잡는다.
2. **§5~§8**: request pair, service pattern, normalization, time, travel을 이해한다.
3. **§6**: 무엇이 bug인지인 invariant와 atomicity를 확인한다.
4. **§9~§10**: customer policy와 search state/final result의 경계를 본다.
5. **§11~§13**: portfolio, ALNS, COW, cache, termination, reproducibility를 본다.
6. **§14**: 두 verifier와 Win PoC publication/benchmark gate를 본다.
7. **§15~§16**: 구현 phase, evidence, migration과 deferred work를 확인한다.
8. **§17과 질문 등록부**: 무엇이 이미 결정됐고 무엇을 임의로 정하면 안 되는지 확인한다.

## 14. 읽으며 자주 생기는 오해

| 오해 | 실제 의미 |
|---|---|
| “미배정 request는 곧 외주/이월이다.” | search bank는 membership일 뿐이다. final result는 `ASSIGNED`/`UNASSIGNED`이며 solver가 fleet 밖의 처분을 추정하지 않는다. |
| “큰 penalty면 hard constraint도 목적 함수에 넣을 수 있다.” | 안 된다. hard-infeasible 후보는 ranking/acceptance 전에 탈락한다. |
| “SA면 시간창 위반도 받아들일 수 있다.” | SA는 feasible candidate 사이의 current 이동만 결정한다. |
| “matrix는 거리 계산용 보조 데이터다.” | prepared directed travel은 solver와 verifier가 공유하는 유일한 travel authority다. |
| “없는 arc는 반대 방향 값을 쓰면 된다.” | 안 된다. preparation이 좌표·속도 규칙으로 해소하거나 입력 오류로 막는다. |
| “같은 seed면 재현된다.” | 모든 fingerprint, version, step budget, stable order, warm-start lineage도 같아야 한다. |
| “cache가 있으니 verifier도 재사용하면 된다.” | verifier의 독립성이 사라진다. 두 verifier 모두 cache/solver summary를 권위로 삼지 않는다. |
| “audit가 가능한 삽입을 찾으면 solver가 자동 고쳐야 한다.” | audit의 역할은 진단 범위를 검증하는 것이며 자동 수정·재탐색이 아니다. |
| “TBD/Open은 보통의 값으로 채워도 된다.” | 실험 대기 항목은 explicit test/experiment config만 쓰고 official default를 만들지 않는다. |

## 15. 최종 요약: 이 설계의 여섯 가지 원칙

1. **Pair를 지켜라.** pickup/delivery는 함께 움직이며 route 또는 bank 중 정확히 한 곳에만 있다.
2. **의미를 일찍 고정하라.** input, numeric/time, service, travel, profile을 algorithm 전에 immutable snapshot으로 만든다.
3. **물리 제약과 정책을 섞지 마라.** hard feasibility, metric, score, objective, plan은 다른 책임이다.
4. **후보와 결과를 구분하라.** search bank는 결과가 아니며 final outcome은 verified solution에서 만든다.
5. **검증을 두 번 통과시켜라.** candidate와 final payload는 서로 다른 authority input으로 독립 검증한다.
6. **빠름보다 먼저 동일함을 증명하라.** COW, step budget, stable order, cache-free equality로 정확성과 재현성을 확보한 뒤 최적화를 검토한다.

이 여섯 문장을 잡고 `master-design.md`를 읽으면, 세부 규칙은 흩어진 제한이 아니라 확장성·정확성·검증 가능성·재현성을 함께 지키는 장치로 보인다.
