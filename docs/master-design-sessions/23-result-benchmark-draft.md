# 세션 23 — 결과·독립 검증·Win PoC 벤치마크 규범 초안

> **세션 30 통합 상태 (2026-07-23):** 본문의 결과/benchmark `TBD`는 `Q-BENCH-02`를 제외하고 세션 29에서 해결되었다. Solver outcome은 `ASSIGNED/UNASSIGNED`, `LEASE` 배정도 `ASSIGNED`, static `PROVEN` 외 모든 미배정은 final-solution audit 대상이다. Matrix는 solver 전 preparation에서 누락값을 생성하며 core/verifier 내부 lazy fallback만 금지한다. Win 전체 시간은 운영시간 합이고 oneway는 rotation 값을 무시한다. Official 실행은 complete multi-round champion protocol이며 실제 round/worker/step/watchdog 수치는 미확정이다. [Master §8·§10·§14](../master-design.md)와 [등록부](../master-design-open-questions.md)가 우선한다.

> 상태: `REVIEW INPUT`
>
> 성격: 세션 25의 Master Design에 병합할 결과 finalization, 독립 verifier, benchmark 계약 초안
>
> 권위 기준: [세션 19 통합 계획](19-integration-plan.md), [세션 20 도메인·입력 초안](20-domain-input-draft.md), [세션 21 정책·평가·목적 초안](21-policy-objective-draft.md), [세션 22 알고리즘 초안](22-algorithm-draft.md)
>
> 편집 범위: 이 문서만 작성한다. 코드, 입력 fixture, 기존 설계 문서는 변경하지 않는다.
>
> 기준일: 2026-07-23

## 1. 문서 역할과 규범 수준

이 문서는 RPDPTW (Rich Pickup and Delivery Problem with Time Windows) 실행이 검색 상태를 종료한 뒤, 검증 가능한 결과로 finalization되는 계약을 정의한다. 핵심은 검색 중 `SearchRequestBank`와 최종 업무 상태를 분리하고, solver의 cache나 출력 집계를 신뢰하지 않는 독립 검증을 통과한 후보만 정상 결과 또는 benchmark 비교 대상으로 삼는 것이다.

이 문서는 구현 완료를 주장하지 않는다. `RunRecord`, `PublishedSolveResult`, `RequestOutcome`, `VerificationReport`, `BenchmarkCard` 같은 이름은 책임을 설명하기 위한 논리적 라벨이다. 외부 JSON 필드명, Java 타입, API transport, 저장 위치, cloud 제품과 배포 topology를 확정하지 않는다.

규범 표기는 다음과 같다.

| 표기 | 의미 |
|---|---|
| **확정** | 세션 19의 `C-*` 결정 또는 이를 지키기 위한 필수 계약. `MUST` 또는 `MUST NOT`으로 해석한다. |
| **잠정** | 세션 19의 `P-*` 방향. 의미 경계는 유지하되 enum, code 목록, 외부 schema와 활성 범위는 확정하지 않는다. |
| **열린 질문** | 답에 따라 결과 의미, 검증, benchmark 비교 가능성이 달라지는 항목. 세션 19의 정확한 `TBD(Q-...)`를 유지한다. |

### 1.1 이 초안이 소유하는 내용

- 실행 기록과 게시 가능한 solve 결과의 의미적 구분
- route, stop, request outcome과 입력 request의 완전한 mapping
- metric, score breakdown, objective vector와 fingerprint/provenance
- 최종 assignment partition과 구조화된 unassignment diagnostic
- 독립적이고 cache-free인 final verifier
- 정상·불완전·예외 종료 결과의 무결성 및 recovery candidate 정책
- `data/win_poc_case.json`의 read-only fixture manifest
- Win PoC 전용 사전식 comparator, baseline/challenger/regression workflow
- 결과·검증·재현성·benchmark acceptance 및 test plan

### 1.2 이 초안이 소유하지 않는 내용

- raw parser, legacy alias, 단위·rounding·시간·matrix 의미 결정: 세션 20
- 고객사 constraint, score 산식, objective engine과 `SolvePlan` 내부: 세션 21
- initial portfolio, ALNS operator, acceptance, cache와 rollback 구현: 세션 22
- provider/product/API/cloud/storage/runtime topology: deferred
- route pool/MIP, MDVRP·OVRP·SDVRP와 다른 선택 변형: deferred
- benchmark의 공식 seed, `maxSteps`, watchdog, 품질·성능 임계값
- Win PoC `전체 시간`의 공식

## 2. 검색 종료에서 결과 publication까지

### 2.1 규범적 finalization 흐름

**확정 (`C-15`, `C-21`).** 결과는 검색 state를 직렬화한 사본이 아니다. 다음 경계를 거쳐야 한다.

```text
세션 22의 마지막 committed candidate 또는 recovery candidate
+ immutable normalized ProblemInstance
+ immutable bound profile
+ algorithm termination/provenance
        │
        ▼
독립·cache-free verifier
  ├─ 구조·request partition
  ├─ route hard feasibility
  ├─ matrix 기반 시간·거리·자원 재전파
  ├─ metric/score/objective 재계산
  └─ candidate integrity fingerprint
        │
        ├─ FAIL
        │    └─ run failure/invalid record만 생성
        │       정상 solution payload publication 금지
        │
        └─ PASS
             ▼
        final assignment partition
        + disposition policy가 승인된 경우의 명시적 disposition
        + structured diagnostics
             ▼
        결과 자체의 partition/reference/summary 재검증
             ▼
        published solve/run result
             ▼
        benchmark 대상이면 별도 benchmark projection과 card
```

한 insertion failure, 마지막 repair 실패, 높은 rejection count 또는 최종 bank membership 하나만으로 최종 사유를 확정해서는 안 된다. 검색 telemetry는 finalization의 제한된 증거 중 하나일 뿐이다.

### 2.2 실행 기록과 게시 결과

논리적으로 다음 두 기록을 구분한다.

| 기록 | 목적 | solution payload 조건 |
|---|---|---|
| run/execution record | 정상·중단·실패를 포함한 모든 실행의 종료, counter, seed, 오류와 provenance 보존 | 검증된 후보가 없으면 없어야 한다. 후보가 있어도 publication 정책과 검증 상태를 명시한다. |
| published solve result | 소비자가 route와 request outcome을 업무 결과로 해석할 수 있는 완전한 결과 | 독립 verifier `PASS`, result partition 검증 `PASS`, fingerprint 일치가 필수다. |

이 구분은 특정 API endpoint나 저장 객체 두 개를 요구하지 않는다. 하나의 envelope 안에 표현할 수도 있지만, 실행이 종료되었다는 사실과 정상 결과가 게시 가능하다는 사실은 서로 다른 상태여야 한다.

여러 run 또는 seed 중 하나를 solve 결과로 선택하는 경우 결과는 선택된 실제 run을 참조해야 한다. 서로 다른 run의 metric 최소값, route, request outcome을 조합해 존재하지 않는 가상 결과를 만들 수 없다.

## 3. 게시 결과의 규범적 내용

### 3.1 상위 결과 envelope

게시 가능한 결과는 최소한 다음 의미를 보존해야 한다.

```text
PublishedSolveResult
├─ contract/schema identity
├─ solve/run identity와 selected-candidate lineage
├─ result status와 termination status
├─ routes[]
├─ requestOutcomes[]
├─ route/solution metrics
├─ score breakdown
├─ objective schema/vector/comparator
├─ configuration/profile/adapter/matrix/algorithm fingerprints
├─ seed/step/termination/rollback provenance
├─ solution/result integrity fingerprints
└─ independent verification report reference/summary
```

필드의 exact wire 이름은 확정하지 않지만 다음은 `MUST` 성립한다.

1. 결과 계약의 schema ID/version을 식별할 수 있다.
2. 선택된 solution이 어느 run, stage, committed candidate에서 왔는지 역추적할 수 있다.
3. normalized problem과 원 입력 fixture의 identity를 둘 다 식별할 수 있다.
4. route, outcome, summary, metric과 score가 동일한 verified solution을 가리킨다.
5. 정상 종료와 예외 종료를 별도 필드 또는 동등하게 명확한 구조로 구분한다.
6. verifier ID/version, 적용 constraint/profile과 최종 판정을 식별할 수 있다.
7. fingerprint가 다른 입력, profile, matrix 또는 algorithm 결과를 같은 실행으로 가장할 수 없다.

### 3.2 Route와 stop

각 게시 route는 최소한 다음 의미를 가진다.

- result-local stable route identity
- 정규 차량의 외부 `vehicleId`와 normalized vehicle identity의 mapping
- 고정 start/end terminal identity와 physical location reference
- 순서가 보존된 terminal, pickup, delivery stop 또는 동등한 service event
- 각 request stop의 외부 order/request identity와 normalized request identity
- node role, service meaning, physical location과 sequence position
- 검증된 arrival, service start/end, wait, load-after와 적용 가능한 route-resource facts
- 이전 stop에서 온 directed matrix arc의 거리·시간
- route-level distance, time breakdown, stop/request count와 활성 상태

delivery-only virtual pickup을 결과 view에서 생략할 수 있는지 여부는 외부 API 결정이다. 어떤 view를 선택해도 verifier와 result integrity 계층에는 다음 mapping을 보존해야 한다.

```text
input request
↔ normalized pickup/delivery pair
↔ verified route/vehicle
↔ pickup/delivery service-event reference
```

가상 pickup은 실제 고객 방문이나 중간 depot 재적재로 표시해서는 안 된다. 실제 pickup과 delivery-only start loading의 의미가 결과 표현에서 구분되어야 한다.

### 3.3 Request outcome

모든 입력 request는 최종 결과에 정확히 하나의 `RequestOutcome`을 가져야 한다.

```text
RequestOutcome
├─ external order/request identity
├─ normalized request identity
├─ final assignment status
├─ assignment reference 또는 explicit fallback reference
├─ diagnostics[]
└─ optional recommended actions
```

`ASSIGNED` outcome은 정확히 하나의 verified regular route, vehicle, pickup과 delivery event를 참조해야 한다. `UNASSIGNED` outcome은 regular route를 참조해서는 안 된다. `DEFERRED` 또는 `OUTSOURCED`가 승인된 profile에서 사용되더라도 regular assignment와 중복되어서는 안 된다.

호환용 `unassignedOrders` view가 필요하다면 `requestOutcomes`에서 파생해야 한다. 두 목록을 독립 생성하여 불일치할 수 있게 해서는 안 된다.

### 3.4 Metric, score와 objective breakdown

결과는 다음 세 층을 혼합하지 않는다.

| 층 | 의미 | 필수 metadata |
|---|---|---|
| neutral metric | 거리, 시간, 대기, 서비스, route 자원, 상태별 request count 등 발생한 물리·구조 사실 | metric ID/version, scope, unit, scale/formula provenance |
| score breakdown | bound 고객사 정책이 feasible solution에 계산한 비용·soft penalty 구성요소 | score policy/component ID/version, key, scope, unit, checked total |
| objective vector | comparator가 실제 solution을 선택할 때 사용한 ordered dimension 값 | objective schema/comparator ID/version, dimension order, direction, source |

다음을 `MUST` 만족한다.

- route metric의 합과 solution metric은 같은 verified route에서 재계산된다.
- score total과 breakdown 합은 정책 계약에 따라 일치한다.
- objective vector의 각 값은 선언된 metric 또는 score source로 설명 가능하다.
- cached 값과 solver가 출력한 합계는 verifier의 재계산값과 정확히 일치해야 한다.
- Win PoC benchmark vector는 고객사 objective vector와 별도 namespace/profile에 둔다.
- solver elapsed time은 route의 `전체 시간`이나 고객 시간 metric에 섞지 않는다.

### 3.5 Fingerprint와 provenance

결과는 최소한 다음 exact identity/version 또는 그 전체를 대표하는 충돌 저항성 fingerprint를 보존해야 한다.

| 영역 | 필요한 identity/provenance |
|---|---|
| input | 원본 digest, adapter ID/version, 적용 coercion/alias, normalized problem fingerprint |
| numeric/time | 차원별 unit/scale/rounding policy, planning time contract와 version |
| matrix | 원본 matrix identity, field mapping/diagonal/coverage policy, normalized directed matrix fingerprint |
| profile | profile key/exact version/config hash, constraint·metric contributor·score component IDs/versions |
| objective | objective schema, comparator, `SolvePlan`, guard versions |
| algorithm | build/runtime compatibility, initial portfolio/selected candidate, operator registry/config, acceptance/adaptive config |
| randomness | base seed, stage/run별 derived seed, derivation version |
| budget | stage별 requested/completed steps, 별도 construction/local/fleet work counters |
| state | candidate state strategy와 version, evaluator/cache version |
| termination | algorithm reason, 상위 execution state, 마지막 completed stage/step, rollback/integrity 상태 |
| result | canonical solution fingerprint, result payload fingerprint, result schema version |
| verification | verifier ID/version, constraint set, verification input fingerprint, 판정과 오류 evidence |

wall-clock timestamp와 elapsed time은 관측 metadata다. 이를 품질 fingerprint의 hidden input이나 objective tie-break로 사용할 수 없다.

### 3.6 Integrity와 verification status

결과 계층은 최소한 다음 상태 의미를 구분할 수 있어야 한다. 정확한 외부 enum 이름은 확정하지 않는다.

| 의미 상태 | 해석 | route/outcome publication |
|---|---|---|
| verified | 독립 verifier와 result partition/integrity 검증이 모두 통과 | 허용 |
| invalid | 독립 verifier 또는 result consistency가 실패 | 금지 |
| not verified | 후보가 없거나 verifier가 완결되지 않음 | 금지 |
| verified recovery candidate | 예외 종료 뒤 마지막 committed candidate를 독립 verifier가 통과 | 정상 완료로 가장하지 않는 조건부 recovery attachment만 가능 |

검증 report는 boolean만 기록하지 않는다. 적용한 verifier와 constraint의 ID/version, 확인한 problem/matrix/profile fingerprints, 검증한 solution fingerprint, 오류 code/scope/evidence를 보존해야 한다.

## 4. 최종 assignment partition과 status

### 4.1 Search state와 final state의 분리

**확정 (`C-15`).**

```text
검색 중
Solution
├─ Route[]
└─ SearchRequestBank
   └─ requestId membership only

finalization 후
PublishedSolveResult
└─ RequestOutcome[]
   ├─ final assignment status
   ├─ assignment/fallback reference
   └─ diagnostics
```

`SearchRequestBank`는 다음을 말할 수 있다.

> 이 verified search solution에서 request가 regular route에 없다.

다음은 말할 수 없다.

> request가 왜 전역적으로 배정 불가능한가, 외주가 확정되었는가, 다음 계획으로 이월되었는가.

bank에 마지막 failure reason, customer message, disposition 또는 diagnostic을 역으로 저장해서는 안 된다.

### 4.2 최종 partition

잠정 status model (`P-08`)은 다음 네 업무 의미를 구분한다.

| status 의미 | 규범적 해석 | 필요한 reference/evidence |
|---|---|---|
| `ASSIGNED` | verified regular vehicle route에 실제 배정 | route, vehicle, pickup/delivery service event |
| `UNASSIGNED` | current plan의 regular route에 없고 확정된 대체 disposition도 없음 | 하나 이상의 diagnostic 또는 unknown/undetermined diagnostic |
| `DEFERRED` | current plan 밖의 후속 계획으로 이월하기로 명시적으로 확정 | 승인된 최소 disposition 정보. 정확한 최소값은 `TBD(Q-RES-01)` |
| `OUTSOURCED` | 외부 운송 수단 처리로 명시적으로 확정 | 승인된 최소 disposition 정보. 정확한 최소값은 `TBD(Q-RES-01)` |

모든 입력 request 집합을 `R`, verified regular route request 집합을 `A`, final outcome 집합을 `O`라고 할 때 다음이 성립해야 한다.

```text
|O| = |R|
각 r ∈ R은 O에 정확히 한 번 존재

status(r) = ASSIGNED
⇔
r ∈ A이고 정확히 하나의 verified route/vehicle/pair를 참조

status(r) ∈ {UNASSIGNED, DEFERRED, OUTSOURCED}
⇒
r ∉ A
```

외주·이월이 search-time 대체 선택지인지 finalization 분류인지는 `TBD(Q-OBJ-03)`이다. 어느 쪽이 승인되더라도 regular route, fallback state와 bank 사이의 중복·누락을 금지하는 전체 partition 검증이 필요하다.

### 4.3 `Q-RES-01`이 열린 동안의 보수적 처리

`DEFERRED`와 `OUTSOURCED`의 최소 확정 정보가 정해지지 않았으므로, 승인된 disposition contract가 없는 profile은 다음처럼 행동해야 한다.

1. verified regular route request만 `ASSIGNED`로 만든다.
2. final bank request를 기본적으로 `UNASSIGNED`로 만든다.
3. diagnostic 또는 권고 행동만으로 `DEFERRED`/`OUTSOURCED`를 추론하지 않는다.
4. “외주 고려”와 “외주 확정”, “재계획 가능”과 “이월 확정”을 구분한다.
5. `DUMMY` 또는 가상 차량을 final 업무 status로 노출하지 않는다.
6. 첫 구현에서 외주·이월을 결과 분류로 활성화할지 search option으로 활성화할지는 이 문서가 결정하지 않는다.

이 보수적 처리는 `Q-RES-01`의 답을 대신하지 않는다. 향후 status contract가 승인되면 exact profile/version과 disposition source를 결과 provenance에 기록해야 한다.

### 4.4 상태와 요약 count

result summary는 request outcome에서 파생해야 한다.

```text
totalRequestCount
= assignedRequestCount
+ unassignedRequestCount
+ deferredRequestCount
+ outsourcedRequestCount
```

각 count의 단위는 **입력 request/order 건수**다. pickup과 delivery node를 각각 세거나 같은 physical location의 여러 order를 합치지 않는다.

`assignedRequestCount`는 regular route에 배정된 request 수다. `unassignedRequestCount`는 status가 정확히 `UNASSIGNED`인 request 수다. regular fleet에 배정되지 않은 전체를 보고하려면 이름이 명확한 별도 파생 metric을 사용한다.

## 5. 구조화된 unassignment diagnostic

### 5.1 진단 구조

**잠정 (`P-09`).** 각 진단은 최소한 다음 의미를 가져야 한다.

```text
UnassignmentDiagnostic
├─ code
├─ scope
├─ confidence
├─ source
├─ constraint/profile reference (적용 시)
└─ bounded evidence
```

| 필드 | 의미 |
|---|---|
| `code` | 기계 판독 가능한 안정된 사유 범주. 고객사 확장은 namespaced code를 사용할 수 있다. 공개 목록은 아직 미확정이다. |
| `scope` | 특정 request, compatibility group, zone 또는 fleet 중 근거가 실제로 성립하는 범위 |
| `confidence` | 근거가 증명인지, final solution에 한정된 exhaustive audit인지, 검색 관찰인지, unknown인지 |
| `source` | normalization/precheck, final-solution audit, bounded search telemetry, termination/finalization 중 evidence 생성 경계 |
| constraint/profile reference | 해당 hard rule과 exact version을 식별 |
| `evidence` | 판단을 재검토할 수 있는 제한된 정수·count·reference. 전체 탐색 trace나 mutable cache dump가 아님 |

사용자 표시 문구와 번역은 안정된 `code`와 분리한다. 메시지 문자열을 API 판정 근거로 사용하지 않는다.

### 5.2 Confidence

진단 confidence는 다음 의미를 구분해야 한다. 정확한 wire enum 이름은 잠정이다.

| 의미 | 사용 조건 | 금지되는 과장 |
|---|---|---|
| proven | route 순서나 휴리스틱 탐색 품질과 무관한 정적/논리적 불가능성을 승인된 규칙으로 증명 | 검색 중 많이 거절되었다는 이유만으로 사용 |
| exhaustive for final solution | 고정된 final routes에 대해 승인된 합법 vehicle/position 전체를 감사했으나 삽입 불가 | 다른 route 재배치까지 포함한 전역 불가능 증명이라고 표현 |
| search observed | bounded telemetry에서 특정 constraint rejection이 반복 관찰 | 최종 원인 또는 불가능 증명으로 표현 |
| unknown | bank membership과 종료 정보 외에 충분한 근거 없음 | 임의로 가장 빈번한 failure를 확정 |

group/zone/fleet 수준 shortage를 특정 request의 proven 원인으로 복제해서는 안 된다. request outcome은 group diagnostic reference를 가질 수 있지만 scope를 보존해야 한다.

### 5.3 Source와 evidence 규칙

| source 의미 | 가능한 evidence | 허용 가능한 최고 confidence |
|---|---|---|
| normalization/precheck | empty eligible-vehicle set, 모든 eligible vehicle보다 큰 단일 request demand, 승인된 planning-horizon 불일치 | 근거가 독립적으로 충분하면 proven |
| final-solution audit | 허용 vehicle/position 수, constraint별 rejection count, audit contract/version | exhaustive for final solution |
| search telemetry | bounded rejection count, first/last observed completed step, operator/constraint code | search observed |
| termination/finalization | termination reason, completed/requested steps, audit 미실행 | unknown |

evidence를 만들 때도 `Q-NUM-*`, `Q-MTX-*`, `Q-TIME-*`에 대한 승인된 policy를 사용해야 한다. 미결정 의미를 값의 분포나 필드명으로 추정하여 proven 진단을 만들 수 없다.

### 5.4 `Q-RES-02`가 열린 동안의 보수적 처리

모든 미배정 request에 exhaustive final insertion audit를 수행할지는 `TBD(Q-RES-02)`다. 답이 정해질 때까지 다음을 지킨다.

- audit를 실제로 수행하고 audit 범위·version·완결성을 기록한 request에만 exhaustive confidence를 부여한다.
- audit가 없으면 precheck proof 또는 search-observed/unknown evidence만 게시한다.
- 구체 근거가 없는 `UNASSIGNED` outcome에는 unknown/undetermined 진단을 하나 이상 둔다.
- 한 insertion failure 또는 마지막 observed rejection을 대표 final reason으로 승격하지 않는다.
- audit 비용은 solver ALNS `completedSteps`와 별도 counter/time으로 측정한다.

진단 code의 최종 공개 vocabulary, 고객사 내부 constraint ID 노출, 대표 사유 선택 규칙과 UI 표시는 여전히 잠정이다.

## 6. 독립 verifier 계약

### 6.1 독립성

**확정 (`C-21`).** 최종 후보와 benchmark 후보는 독립적이고 cache-free인 verifier를 통과해야 한다.

Verifier는 다음을 입력으로 사용한다.

```text
immutable normalized ProblemInstance
+ immutable bound profile/constraint declarations
+ candidate의 route/vehicle/node 순서
+ candidate의 SearchRequestBank membership
+ authoritative normalized directed matrix
```

다음은 권위 입력으로 사용하지 않는다.

- solver의 route feasibility flag
- insertion evaluator의 성공 판정
- cached arrival/load/metric/score
- solver가 출력한 summary, objective vector 또는 matrix 합계
- structural hash만으로 한 동일성 주장
- last failure와 search telemetry

Verifier는 immutable value 정의와 승인된 policy 선언을 공유할 수 있지만, search evaluator가 자기 cache와 자기 판정을 다시 읽어 확인하는 경로가 되어서는 안 된다. 고객사 hard constraint가 있으면 같은 constraint ID/version에 대응하는 독립 검증 규칙 또는 동등한 cache-free 검증 경로가 bind되어야 한다.

### 6.2 검증 순서

Verifier는 최소한 다음을 순서대로 확인한다.

#### 1. Identity와 입력 결합

- candidate의 problem, adapter, numeric/time, matrix와 profile fingerprints가 검증 입력과 일치
- 모든 외부/normalized request, node, vehicle, terminal과 location reference가 존재
- unknown ID, 중복 ID, 다른 run/profile의 참조가 없음
- verified solution fingerprint가 실제 candidate 구조에서 재생성됨

#### 2. Request pair와 전체 partition

- 모든 입력 request가 exactly once assignment 또는 bank 중 정확히 한 곳에 존재
- assigned request의 pickup/delivery가 같은 vehicle route에 각각 정확히 한 번 존재
- pickup이 delivery보다 앞섬
- bank request의 두 node가 어느 route에도 없음
- partial pair, duplicate pair, split vehicle, route+bank 동시 포함, 양쪽 누락이 없음
- delivery-only service pattern과 승인된 virtual-pickup/initial-load 정규형을 만족

#### 3. Vehicle과 terminal

- route가 정확히 하나의 known regular vehicle에 결합
- 첫/마지막 node가 해당 vehicle의 고정 start/end terminal
- 다른 vehicle terminal이나 허용되지 않은 중간 terminal이 없음
- 한 vehicle의 복수 route 표현이 승인되었다면 시각·자원 중복과 해당 profile 규칙을 검증
- oneway, multi-trip/rotation과 depot revisit는 승인된 input/profile 의미만 사용

#### 4. Size, capability와 정적 compatibility

- vehicle의 size type이 request의 승인된 allowed size set에 포함
- required capability/qualification이 vehicle 보유 집합의 subset
- dedicated vehicle, zone 등 승인된 정적 규칙이 반영된 `servableVehicles` membership과 일치
- size membership과 capability subset을 generic feature intersection으로 합치지 않음
- null/empty/`ALL`과 unknown code를 verifier가 자체 추정하지 않음

#### 5. Directed matrix와 physical location

- 모든 solver node가 valid physical location index를 참조
- route의 각 연속 event가 정확한 directed `(fromLocation, toLocation)` arc를 사용
- reverse arc, 대칭 평균, 좌표/Haversine/속도 기반 재생성, 부분 source 보충이 없음
- required arc가 없으면 검증 실패하며 silent fallback하지 않음
- self arc, same-coordinate/different-ID arc와 diagonal 값은 exact adapter/matrix policy에 따라 처리
- matrix fingerprint와 field-mapping provenance가 결과 metadata와 일치

#### 6. Capacity, time와 route resource

- start load, pickup/delivery delta와 final load를 처음부터 재전파
- 모든 weight/volume/resource 차원에서 `0 <= load <= capacity`
- request/node time window, vehicle work window와 planning period를 승인된 boundary policy로 검증
- arrival, wait, service start/end와 end-terminal time을 matrix와 service duration에서 재계산
- max stop, max distance, max drive time과 등록된 route resource rule을 승인된 reset/scope 계약으로 검증
- 등록된 고객사 hard constraint를 exact ID/version으로 검증
- overflow, missing policy와 explicit calculation failure를 큰 수나 plan-end sentinel로 대체하지 않음

#### 7. Metrics, score와 objective

- route별 거리·시간·대기·서비스·elapsed와 resource metric을 node sequence에서 재계산
- solution aggregate를 verified route와 final bank에서 재계산
- bound metric contributor, score component와 objective projector를 cache-free로 재평가
- 결과의 route metric, solution metric, score breakdown과 objective vector가 재계산값과 정확히 일치
- fixed-point canonical integer를 exact comparison하며 `EPS`를 사용하지 않음

#### 8. Result finalization integrity

- request outcome이 verified route/bank partition과 일치
- assignment/fallback reference가 status 의미와 일치
- status summary count가 outcome에서 재계산한 count와 일치
- diagnostic confidence가 실제 source/audit evidence보다 높지 않음
- result payload fingerprint가 게시할 payload에서 재계산됨

### 6.3 Verifier output과 실패 정책

Verifier report는 다음을 식별해야 한다.

- verifier ID/version과 verification contract version
- problem, profile, matrix와 solution fingerprints
- 적용한 constraint/adapter/numeric/time versions
- pass/fail
- 실패 code, scope, request/route/arc reference와 bounded evidence
- 재계산한 route/solution metric과 result 비교 결과

검증 실패는 미배정 request의 정상 diagnostic이 아니다. candidate/result 무결성 실패이며 정상 결과 생성을 막아야 한다. benchmark에서는 metric vector가 좋아도 해당 run 전체를 비교 대상에서 제외한다.

### 6.4 Matrix fallback 금지

검증 중 matrix arc나 의미가 부족하면 다음 중 하나만 가능하다.

1. 승인된 adapter/matrix contract에 따라 이미 normalized input에 존재하는 arc를 사용한다.
2. 입력 또는 candidate를 invalid로 판정한다.

좌표, `GreatCircle`, Haversine, vehicle speed, reverse arc 또는 0으로 조용히 대체하는 세 번째 경로는 없다. 외부 matrix preparation이 필요하면 solver/verifier 실행 전에 새 source와 provenance를 가진 완전한 입력을 만들어야 한다.

## 7. 불완전·예외 실행과 recovery candidate

### 7.1 종료 상태의 해석

세션 22의 종료 계약을 결과에 그대로 보존한다.

| 상태 | 의미 | 정상 품질 결과 |
|---|---|---|
| `MAX_STEPS_REACHED` | 모든 계획 stage가 fixed `maxSteps`를 정상 완료 | 가능. 독립 verifier 통과 필수 |
| `WATCHDOG_REACHED` | solver watchdog에 의한 exceptional safety termination | 같은 품질 예산으로 비교 불가 |
| `CANCELLED` | 명시적 협력 취소 | 정상 완료 아님 |
| `RESOURCE_LIMIT_REACHED` | 처리 가능한 solver 자원 한계 | 정상 완료 아님 |
| `PLATFORM_TIMEOUT` | 상위 실행 경계가 algorithm termination record 완성을 막음 | 정상 완료 아님 |
| `FAILED` | 입력 이후 실행/implementation/platform failure | 정상 완료 아님 |

input/config binding 실패는 search termination으로 가장하지 않는다. 실행 시작 전 오류로 별도 기록한다.

### 7.2 마지막 후보를 노출할 수 있는 최소 조건

예외 종료 시 마지막 후보는 다음 조건을 모두 만족할 때만 **verified recovery candidate**가 될 수 있다.

1. 미완료 mutation/step이 완전히 discard 또는 rollback되었다.
2. 후보가 마지막 committed immutable best에서 왔다.
3. candidate의 problem/profile/matrix/seed lineage가 완전하다.
4. 종료 후 독립 verifier가 candidate를 처음부터 검증해 `PASS`했다.
5. 결과 partition과 integrity fingerprint도 검증을 통과했다.
6. 원래 exceptional termination reason과 requested/completed steps를 그대로 표시한다.

미완료 step의 부분 상태, solver cache만 검증된 candidate, process 종료 때문에 verifier가 완결되지 않은 candidate는 노출할 수 없다.

### 7.3 노출 정책의 경계

이 문서는 verified recovery candidate를 외부 응답에 반환할지, 내부 운영 복구용으로만 보존할지, 어떤 사용자 역할에 보여 줄지를 확정하지 않는다. 이는 후속 product/API 정책이다.

결과 계약이 허용하는 것은 다음뿐이다.

- run record는 exceptional termination을 항상 정확히 보고할 수 있다.
- product policy가 승인한 경우 verified recovery candidate를 명시적 recovery attachment로 포함할 수 있다.
- recovery attachment를 `MAX_STEPS_REACHED` 정상 결과나 공식 benchmark run으로 표시할 수 없다.
- product policy가 없으면 exceptional run에는 정상 published solution을 만들지 않는 보수적 동작을 사용한다.

### 7.4 Rollback과 termination metadata

불완전·예외 실행 record에는 최소한 다음 의미가 필요하다.

- exact algorithm reason과 상위 execution state
- stage별 requested/completed steps
- 마지막 completed stage와 step
- interrupted step이 counter/adaptive state에 commit되었는지 여부
- rollback/discard 완료, 불필요 또는 확인 불가 중 해당 integrity 상태
- 마지막 committed candidate fingerprint와 verifier 판정
- cancellation/watchdog/resource/platform signal의 source
- elapsed breakdown은 관측값으로 별도 기록

rollback이 확인되지 않거나 process-level failure로 상태를 확인할 수 없으면 recovery candidate를 추정해 만들지 않는다.

## 8. Win PoC fixture와 manifest

### 8.1 Read-only identity evidence

세션 23에서 `data/win_poc_case.json`을 수정하지 않고 독립적으로 다시 확인한 사실은 다음과 같다.

| 항목 | 확인값 |
|---|---:|
| 경로 | `data/win_poc_case.json` |
| 파일 크기 | 14,157,512 byte |
| SHA-256 | `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7` |
| 최상위 형식 | JSON object |
| date range 원문 | `2023-09-13 00:00:00` ~ `2023-09-14 00:00:00` |
| depot | 1 |
| orders / 고유 order ID | 452 / 452 |
| order location ID | 452개, 모두 고유 |
| items | 452개, 각 order에 1개 |
| vehicles / 고유 vehicle ID | 31 / 31 |
| 기존 routes | 0 |
| 입력 location union | depot 1 + order 452 = 453 |
| matrix arcs / 고유 `(F,T)` pair | 205,209 / 205,209 = `453 × 453` |
| matrix endpoint union | 453, 입력 location union과 일치 |

관찰된 legacy shape는 다음과 같다.

| 관찰 항목 | read-only 사실 | 규범적으로 추론하지 않는 것 |
|---|---|---|
| options | `trips="oneway"`, `multiRotation="1"`, `Optimizer.VehicleMaxStopCount="28"`, `Termination.secondsSpentLimit="600"` | rotation, depot 재방문, task reset, 공식 benchmark watchdog 의미 |
| matrix fields | 각 arc에 `C`, `D`, `F`, `T`, `U` | `D/U/C`의 공식 의미·단위 |
| `D` representation | diagonal 453건은 JSON number, 나머지 204,756건은 numeric string | canonical coercion과 rounding |
| `U` representation | 205,209건 모두 numeric string | 공식 time 단위 |
| diagonal | 453건 모두 `D=9999`, `U="0"` | sentinel, 정상 self-distance 또는 0 치환 |
| `C` 분포 | `O` 204,805건, `G` 404건 | `O/G`의 업무 의미 |
| routes | 빈 배열 | 정답, baseline 또는 incumbent solution |

fixture option의 `distanceTimeCalculate="GreatCircle"`도 관찰되지만, 이를 solver/verifier의 좌표 fallback 권한으로 해석하지 않는다. `C-13`의 authoritative normalized directed matrix와 no-fallback 계약이 우선한다.

### 8.2 Benchmark manifest

**잠정 (`P-13`).** 한 benchmark comparison set은 versioned manifest를 가져야 한다.

```text
benchmark/profile identity
fixture:
  path + byte length + SHA-256 + structural fingerprint
input interpretation:
  adapter ID/version
  approved alias/coercion
  date/time interpretation
  oneway/rotation/resource interpretation
matrix:
  field mapping, unit/scale/rounding
  diagonal/self-arc policy
  coverage policy
  raw + normalized fingerprints
domain/policy:
  service pattern
  constraint/profile versions
benchmark projection:
  metric IDs/order/directions
  formula IDs
execution:
  build/algorithm/operator/portfolio/state versions
  official seeds, derivation version, per-stage maxSteps, watchdog
verification:
  verifier ID/version + required checks
comparison gates:
  champion/seed quality policy
  deterministic rerun policy
  performance environment/gates
```

경로가 같아도 digest가 다르면 다른 fixture다. adapter, matrix mapping, numeric policy, total-time formula, seed/step budget 또는 comparator semantics가 다르면 같은 baseline과 직접 비교해서는 안 된다.

### 8.3 Adapter interpretation provenance

Win PoC adapter는 다음 legacy interpretation을 명시적으로 versioning해야 한다.

- mixed JSON number/numeric-string 수용 범위
- 외부 order, item, vehicle, location의 canonical mapping
- order-level vehicle size restriction과 별도 capability 의미
- date range, request date, open/close/work time의 time contract
- `oneway`, `multiRotation`, depot task/wait, max-stop/resource의 scope와 reset
- `F/T/D/U/C` field mapping과 단위
- diagonal `D=9999,U=0` 및 same/different location arc 처리
- fixed-point scale, rounding, quantity aggregation

adapter provenance가 없거나 unresolved 질문의 답을 필요로 하면 공식 baseline을 만들지 않는다. fixture shape가 완전하다는 사실은 field semantics가 확정되었다는 뜻이 아니다.

## 9. Win PoC comparator

### 9.1 정확한 네 성분

**확정 (`C-18`).** Win PoC의 benchmark-profile 전용 품질 벡터는 다음 순서다.

```text
WinPocVector = (
  unassignedOrderCount,
  dispatchedVehicleCount,
  totalDistance,
  totalTime
)
```

모든 성분은 작을수록 좋다.

```text
A < B
⇔
A.unassignedOrderCount < B.unassignedOrderCount
또는
앞 성분이 같고 A.dispatchedVehicleCount < B.dispatchedVehicleCount
또는
앞 두 성분이 같고 A.totalDistance < B.totalDistance
또는
앞 세 성분이 같고 A.totalTime < B.totalTime
```

이 순서를 weighted sum이나 Big-M scalar로 바꾸지 않는다. 네 성분이 모두 같으면 품질상 동률이다. deterministic champion 하나를 선택하기 위한 canonical solution fingerprint/run/seed tie-break는 다섯 번째 품질 성분이 아니다.

이 comparator는 `WIN_POC` benchmark profile에만 적용한다. 모든 고객사의 objective 또는 공통 ALNS comparator로 사용하지 않는다.

### 9.2 미배정 주문 수

`unassignedOrderCount`는 입력 request/order 중 verified regular route에 `ASSIGNED`되지 않은 request 수다.

```text
unassignedOrderCount
= count(final status in {UNASSIGNED, DEFERRED, OUTSOURCED})
= total input requests - verified regular-route assigned requests
```

fixture에 현재 fallback option이 관찰되지 않으므로 보수적 finalization에서는 final bank request와 `UNASSIGNED` outcome 수가 같아야 한다.

다음은 세지 않는다.

- pickup과 delivery node를 두 건으로 계산
- 같은 physical location의 여러 order를 한 건으로 병합
- 검색 도중 일시적으로 bank에 들어간 request
- invalid/누락 request를 정상 미배정으로 보정

partition 검증이 실패하면 수치를 만들지 않고 run을 invalid로 판정한다.

### 9.3 배차 차량 수

`dispatchedVehicleCount`는 verified final regular route에 request가 하나 이상 배정된 **고유 regular vehicle identity 수**다.

다음과 구분한다.

| 세는 값 | 세지 않는 값 |
|---|---|
| 주문을 하나 이상 수행하는 고유 regular vehicle | 입력의 available fleet 31대 전체 |
| 같은 vehicle의 route fragment가 여러 개면 identity 기준 한 대 | route object 개수 |
| 승인된 rotation이 여러 개여도 같은 vehicle이면 한 대 | 빈 route, 예비 차량 |
| regular fleet vehicle | outsourced, dummy, virtual vehicle |

같은 vehicle이 서로 겹치는 route를 수행하거나 승인되지 않은 fragment를 가지면 count 문제 이전에 feasibility 실패다.

### 9.4 전체 거리

`totalDistance`는 independent verifier가 각 used regular route의 실제 연속 event arc를 authoritative directed matrix에서 재계산한 route distance의 합이다.

- 포함할 start/end terminal arc와 oneway 종료 arc는 승인된 adapter/profile 의미를 따른다.
- 같은 좌표라는 이유로 location ID를 병합하지 않는다.
- reverse arc, 대칭화, 좌표 fallback을 사용하지 않는다.
- canonical unit/scale/rounding과 diagonal 처리 의미는 `Q-MTX-*`, `Q-NUM-*`가 승인된 manifest에 명시한다.

현재 legacy `D`를 특정 거리 단위로 확정하지 않으며 공식 baseline 수치를 발행하지 않는다.

### 9.5 전체 시간

네 번째 성분의 위치와 이름은 확정되었지만 정확한 공식은 `TBD(Q-BENCH-01)`이다.

이 문서는 다음 중 하나를 선택하지 않는다.

- 순수 주행시간 합
- 주행·대기·서비스를 포함한 route elapsed time 합
- 그 밖의 승인된 전체 시간 정의

따라서 다음을 금지한다.

- 공통 `driveTime`을 자동 연결
- `totalTime`이라는 이름만 보고 산식 추정
- formula ID/version 없이 공식 baseline vector 발행
- solver elapsed/runtime을 route `totalTime`에 포함

비교 순서는 유지하되, `Q-BENCH-01`이 해결되고 manifest에 formula가 고정되기 전 공식 수치 비교는 준비 미완료다.

## 10. Baseline, challenger와 regression workflow

### 10.1 Baseline card 발행 전

fixture의 `routes`는 빈 배열이므로 입력 파일 자체에는 정답이나 baseline이 없다. 최초 baseline은 다음 절차를 모두 통과한 실제 실행으로만 만든다.

```text
fixture digest/shape 확인
→ approved adapter/matrix/numeric/time interpretation
→ exact benchmark manifest freeze
→ official seed/maxSteps/watchdog profile freeze
→ 모든 official run 실행
→ 각 run independent verification
→ per-run card 작성
→ 실제 run vector 중 lexicographic champion 선택
→ deterministic rerun 확인
→ baseline review/approval
→ immutable baseline card 발행
```

`Q-BENCH-01`, `Q-BENCH-02`와 실행에 필요한 `Q-MTX-*`, `Q-NUM-*`, `Q-BENCH-03`이 해결되기 전 기대 metric 숫자를 추측하지 않는다.

### 10.2 Per-run 및 aggregate card

각 run card에는 최소한 다음이 필요하다.

- manifest fingerprint
- run/seed/derived-seed identity
- stage별 requested/completed steps
- exact termination reason
- verifier status/version
- canonical solution fingerprint
- 네 성분의 완전한 metric vector
- result/request partition summary
- elapsed parsing/binding/construction/search/verification/finalization breakdown
- peak memory 등 성능값이 수집된다면 별도 performance section

aggregate card는 실제 per-run card를 참조하고 다음을 제공한다.

- official run의 complete/missing/invalid 상태
- lexicographic champion run과 완전한 vector
- seed별 vector 분포
- deterministic rerun 결과
- quality, integrity, reproducibility, performance의 분리된 판정

각 성분의 독립 최솟값을 조합한 synthetic champion은 금지한다.

### 10.3 Challenger 비교

challenger는 다음 순서로 판정한다.

1. fixture, adapter, matrix, normalization, profile, comparator, formula, seed와 step fingerprints를 비교한다.
2. 다르면 algorithm quality regression을 판정하지 않고 `NOT COMPARABLE` 의미로 분류한다.
3. 입력 또는 result integrity가 실패한 run은 hard invalid다.
4. official quality run 중 `MAX_STEPS_REACHED`가 아닌 run이 있으면 complete quality set이 아니다.
5. deterministic rerun의 vector 또는 canonical solution fingerprint가 다르면 reproducibility failure다.
6. complete하고 verified인 실제 challenger champion을 baseline champion과 네 성분 사전식으로 비교한다.
7. 첫 번째로 달라진 성분과 방향을 기록한다.
8. seed별 변화와 분포를 함께 보고한다.

champion no-worse를 hard gate로 할지 모든 seed no-worse를 요구할지는 `TBD(Q-BENCH-02)`다. 이 문서는 어느 gate도 확정하지 않는다.

### 10.4 Baseline 갱신

test나 benchmark runner가 baseline을 자동 덮어써서는 안 된다. 갱신에는 최소한 다음 review evidence가 필요하다.

- 이전/새 manifest와 card
- 코드·algorithm·profile·adapter 변경 이유
- fixture와 모든 fingerprint 비교
- independent verification report
- metric 성분별 변화와 first-difference 설명
- constraint, matrix 또는 formula 완화가 가짜 개선을 만들지 않았다는 확인
- deterministic rerun과 performance report

더 좋은 vector라는 이유만으로 의미가 다른 결과를 같은 baseline으로 교체하지 않는다.

### 10.5 품질과 성능의 분리

Win PoC 네 성분은 solution quality다. 다음은 별도 performance 측정이다.

- adapter parsing/normalization time
- profile binding time
- initial portfolio time
- ALNS search time
- independent verification/finalization time
- total elapsed time
- peak memory, allocation, copied route/node와 GC

wall-clock 값은 quality comparator나 deterministic tie-break에 들어가지 않는다. 성능 환경과 threshold가 정해지지 않았으므로 공식 performance gate 수치는 `TBD(Q-BENCH-02)`와 후속 roadmap에 남긴다.

## 11. Acceptance와 test plan

### 11.1 Result contract tests

- 모든 입력 request가 outcome에 정확히 한 번 존재한다.
- `ASSIGNED` outcome은 정확히 하나의 verified route/vehicle/pair를 참조한다.
- non-assigned outcome은 regular route reference를 가지지 않는다.
- route stop의 request/node/location mapping이 normalized input과 일치한다.
- summary count가 outcome에서 재계산한 count와 일치한다.
- compatibility `unassignedOrders` view가 있다면 canonical outcomes와 정확히 같다.
- metric total, score breakdown과 objective vector가 verifier 재계산과 일치한다.
- schema/fingerprint/provenance 필수 항목이 없으면 publication을 거부한다.

### 11.2 Search/result partition tests

- search bank에는 request membership만 있고 diagnostic/status가 없다.
- insert/destroy/reject/rollback 뒤 route XOR bank가 유지된다.
- finalization이 bank를 수정하거나 status를 다시 search state에 쓰지 않는다.
- 한 insertion failure가 final reason이나 proven confidence가 되지 않는다.
- explicit disposition contract 없이 bank request는 `UNASSIGNED`가 된다.
- `DEFERRED`/`OUTSOURCED`와 regular assignment 중복을 거부한다.

### 11.3 Diagnostic confidence tests

- empty eligible vehicle set처럼 독립적으로 증명된 경우에만 proven을 허용한다.
- group shortage를 특정 request proven diagnostic으로 복제하지 않는다.
- 완전한 final-solution audit 없이 exhaustive confidence를 거부한다.
- search rejection count만으로 proven/exhaustive를 만들지 않는다.
- audit가 없고 구체 근거도 없으면 unknown/undetermined를 보존한다.
- 고객사 namespaced code와 exact constraint version을 유실하지 않는다.
- evidence가 선언한 scope와 source를 초과하지 않는다.

### 11.4 Verifier independence tests

- solver의 feasibility cache를 고의로 잘못된 값으로 바꿔도 verifier가 실제 위반을 탐지한다.
- solver metric/score/summary를 변조해도 route에서 독립 재계산하여 불일치를 탐지한다.
- duplicate/missing/split pair, pickup-after-delivery와 route+bank 동시 포함을 거부한다.
- wrong terminal, unknown vehicle, overlapping vehicle use와 invalid route reference를 거부한다.
- size incompatibility, missing capability, capacity/time/resource 위반을 각각 거부한다.
- directed asymmetric arc를 reverse로 읽은 결과를 거부한다.
- missing matrix arc, wrong matrix fingerprint와 좌표 fallback을 거부한다.
- overflow와 unresolved policy를 sentinel 값으로 통과시키지 않는다.
- invalid final result에는 benchmark metric을 게시하지 않는다.

### 11.5 Corrupted input/result rejection

- fixture digest, order/vehicle/location cardinality 또는 matrix pair coverage가 manifest와 다르면 실행 전 중단한다.
- duplicate ID, unknown endpoint, duplicate arc와 malformed numeric representation을 adapter policy에 따라 거부한다.
- adapter version 또는 matrix field mapping이 빠진 result는 비교 불가다.
- result route 순서, outcome status, summary, metric 또는 fingerprint 하나를 변조하면 integrity 검증이 실패한다.
- verifier report의 solution fingerprint와 게시 payload가 다르면 publication을 거부한다.

### 11.6 Termination, rollback과 recovery tests

- `MAX_STEPS_REACHED`는 requested/completed stage steps가 정확히 일치한다.
- mid-step watchdog/cancellation/resource signal은 미완료 step과 adaptive state를 commit하지 않는다.
- COW discard 또는 apply/undo rollback 뒤 last committed best의 canonical fingerprint가 유지된다.
- rollback 확인 불가 또는 verifier 미완료 candidate를 recovery attachment로 게시하지 않는다.
- verified recovery candidate를 정상 completion 또는 official benchmark run으로 잘못 분류하지 않는다.
- `PLATFORM_TIMEOUT`, `FAILED`, input/config error를 algorithm watchdog으로 가장하지 않는다.

### 11.7 Deterministic rerun

**확정 (`C-22`).** 같은 normalized problem/matrix/profile/algorithm/build, seeds, stage별 `maxSteps`, stable ordering과 tie-break를 사용하고 `MAX_STEPS_REACHED`로 끝난 정상 실행은 다음이 같아야 한다.

- route와 bank의 canonical solution fingerprint
- request outcome partition
- neutral metrics, score breakdown과 objective vector
- diagnostic source가 deterministic input/audit에서 나왔다면 diagnostic code/confidence/evidence
- Win PoC metric vector
- stage별 completed steps와 selected candidate lineage

watchdog, cancellation, resource 또는 platform timing 결과에 같은 강한 보장을 적용하지 않는다.

### 11.8 Benchmark comparison tests

- 네 성분을 손으로 비교 가능한 vector로 exact lexicographic order 검증
- 더 낮은 distance가 더 많은 unassigned order나 vehicle을 이기지 못함을 검증
- 완전 동률에서 structural tie-break가 품질 vector를 바꾸지 않음을 검증
- actual per-run vector만 champion 후보가 되며 synthetic vector를 거부
- fingerprint가 다른 card는 `NOT COMPARABLE`
- invalid/incomplete official run set에는 quality pass를 내리지 않음
- baseline 갱신은 명시적 승인 없이는 불가능

### 11.9 Performance 측정 분리

- unit/contract suite는 수치 performance threshold에 의존하지 않는다.
- benchmark quality 판정과 elapsed/memory 판정을 별도 결과로 낸다.
- independent verification time을 search time에 숨기지 않는다.
- watchdog 여유 조사와 algorithm quality budget을 혼합하지 않는다.
- 성능 regression threshold를 데이터 없이 발명하지 않는다.

## 12. Traceability와 열린 질문

### 12.1 확정 결정

| 결정 | 이 초안의 반영 |
|---|---|
| `C-15` | §2, §4~§5에서 `SearchRequestBank`를 membership-only search state로 제한하고 final status/diagnostic을 별도 생성 |
| `C-18` | §8~§10에서 primary fixture와 정확한 `미배정 주문 수 → 배차 차량 수 → 전체 거리 → 전체 시간` benchmark 전용 순서 정의 |
| `C-21` | §2, §6~§7에서 final publication과 benchmark 전에 independent/cache-free full verification 필수화 |
| `C-22` | §7, §10~§11에서 fixed fingerprint/seed/order의 정상 `MAX_STEPS_REACHED` 실행에만 강한 재현성 적용 |

### 12.2 잠정 사항

| 잠정안 | 이 초안의 반영 | 계속 잠정인 부분 |
|---|---|---|
| `P-08` | §4의 `ASSIGNED`, `UNASSIGNED`, `DEFERRED`, `OUTSOURCED` 의미 partition | 외주·이월 최소 정보, 첫 구현 활성 범위, exact external enum/schema |
| `P-09` | §5의 code/scope/confidence/source/evidence 구조 | 공개 code 목록, 고객사 내부 code 노출, 대표 사유와 audit 범위 |
| `P-13` | §8~§10의 fixture/adapter/profile/solver/verifier fingerprint manifest와 card | 공식 seed, budget, watchdog, champion/seed/performance gate |

### 12.3 결과와 benchmark의 중앙 질문

아래 ID는 [세션 19](19-integration-plan.md)의 질문을 그대로 사용한다.

| ID | 이 초안과의 관계 | 결정 전 안전한 처리 |
|---|---|---|
| `Q-RES-01` | `OUTSOURCED`/`DEFERRED` 확정 최소 정보 | 승인 contract 없는 profile은 `ASSIGNED`/`UNASSIGNED`만 보수적으로 생성 |
| `Q-RES-02` | 모든 미배정 request의 exhaustive final insertion audit 여부 | 실제 audit된 request만 exhaustive confidence, 나머지는 proven precheck/search-observed/unknown |
| `Q-BENCH-01` | Win PoC `전체 시간` 공식 | formula ID 없이 공식 baseline vector 발행 금지 |
| `Q-BENCH-02` | 공식 seeds, `maxSteps`, watchdog, champion/seed별 quality와 performance gate | 숫자·threshold 발명 금지; exact manifest 없이는 official comparison 금지 |
| `Q-BENCH-03` | fixture의 `oneway + multiRotation=1` 의미 | terminal/depot revisit/rotation arc를 adapter가 추정하지 않음 |

### 12.4 관련 numeric, matrix와 objective 질문

| ID | 결과/verifier/benchmark 영향 | 결정 전 안전한 처리 |
|---|---|---|
| `Q-NUM-01` | 차원별 유지 소수 자릿수와 result unit | scale 없는 값과 baseline 발행 금지 |
| `Q-NUM-02` | 차원별 rounding mode | verifier와 solver가 임의 rounding하지 않음 |
| `Q-NUM-03` | item 정규화와 quantity 곱 순서 | demand/capacity proof와 benchmark fingerprint에 승인 policy 필수 |
| `Q-MTX-01` | legacy `D/U` 공식 의미·단위·정밀도 | 거리/시간 field mapping 추정 금지 |
| `Q-MTX-02` | `C=O/G`, diagonal `D=9999,U=0` 의미 | self arc와 0 arc 임의 보정 금지 |
| `Q-MTX-03` | canonical production matrix가 항상 complete directed `M²`인지 | manifest coverage contract 필수, 누락 arc fallback 금지 |
| `Q-OBJ-01` | 고객사 내 objective preset 선택 | exact selected profile/plan provenance 없이는 result publication 금지 |
| `Q-OBJ-02` | mandatory order의 hard/finite/lexicographic 의미 | unassigned outcome을 임의 invalid 또는 penalty로 해석하지 않음 |
| `Q-OBJ-03` | 외주·이월이 result classification인지 search option인지 | fallback search state나 비용을 임의 활성화하지 않음 |

Verifier는 세션 20에서 승인된 `Q-TIME-01`~`Q-TIME-04`, `Q-IN-02`, `Q-COMP-01`~`Q-COMP-02`, `Q-REQ-01`~`Q-REQ-02`의 결과도 소비한다. 해당 질문이 열려 있는 입력/profile에 임의 time boundary, rotation reset, compatibility wildcard 또는 mixed-request 의미를 추가하지 않는다.

### 12.5 기존 Master inventory에서 교체할 내용

현재 [Master Design](../master-design.md)의 result/test/benchmark 관련 서술은 역사적 inventory로만 사용했다. 세션 25는 다음을 교체해야 한다.

| 기존 표현 | 교체 계약 |
|---|---|
| `RequestBank`에 미배정 reason 저장 | §4~§5의 search membership / final status / diagnostic 분리 |
| 단일 `unassignedOrders[{reason}]` 중심 result | §3의 모든 input request를 덮는 canonical outcome partition |
| solver cache/summary 중심 result | §6의 independent cache-free verification |
| iteration/time 중 먼저 종료 | 세션 22와 §7의 step-normal / watchdog-exceptional 구분 |
| 동일 input/config/seed만으로 전 종료 유형 재현 주장 | §11.7의 fixed fingerprint/seed/step `MAX_STEPS_REACHED` envelope |
| Solomon 우선 milestone과 존재하지 않는 baseline 가정 | §8~§10의 read-only Win PoC fixture, no-answer-input, versioned card |
| generic best/average/gap 집계 | exact four-component Win PoC vector, actual-run champion과 comparability gate |
| result와 worker/cloud 저장 구조 결합 | 논리적 result/verification contract만 유지하고 topology는 deferred |
| 약 3초 같은 근거 없는 test threshold | contract/quality와 별도 measured performance gate |

## 13. 후속 세션 handoff

### 13.1 세션 24 — roadmap/context

세션 24는 다음 dependency를 Phase와 완료 gate에 배치한다.

```text
normalized input + bound evaluation
→ algorithm committed candidate
→ independent verifier
→ final status/diagnostic/result integrity
→ Win PoC manifest
→ baseline calibration
→ challenger regression
→ 별도 performance measurement
```

세션 24는 다음을 유지해야 한다.

- result publication 전에 independent verifier와 partition gate
- `Q-RES-01`/`Q-RES-02`가 열린 상태의 보수적 finalization
- benchmark formula/budget 질문이 해결되기 전 baseline number 미발행
- recovery candidate 외부 노출은 product/API 정책으로 남김
- provider/product/deployment/storage topology를 추가하지 않음
- route pool/MIP, optional variants와 topology는 deferred

### 13.2 세션 25 — Master 통합

세션 25는 다음을 Master의 result, termination, verification, benchmark 절에 통합한다.

- 게시 result의 route/request mapping, metric/score/objective와 provenance
- search bank와 final status/diagnostic의 분리
- independent/cache-free verifier와 no-matrix-fallback
- 정상 `MAX_STEPS_REACHED`와 exceptional run/recovery candidate의 정확한 지위
- Win PoC primary fixture identity와 exact four-component comparator
- versioned manifest/card와 baseline/challenger workflow
- quality, reproducibility, integrity와 performance 판정의 분리
- 이 문서의 `Q-*`를 중앙 질문 파일의 같은 ID에 연결

통합 과정에서 잠정 라벨을 concrete API/enum으로 확정하거나, total-time formula, seeds, `maxSteps`, watchdog, threshold와 fixed-point/matrix 의미를 새로 만들지 않는다.

## 14. 범위 self-audit

| 검사 | 결과 |
|---|---|
| 수정 대상 | `docs/master-design-sessions/23-result-benchmark-draft.md`만 생성 |
| 문서 전용 | 코드, build, deployment, test, fixture, PDF 변경 없음 |
| RPDPTW | 표준 문제명 사용, 요청 pair 의미 유지 |
| search/result | `SearchRequestBank`를 membership-only로 제한하고 final status/diagnostic 분리 |
| result contract | routes/stops/request mapping, metric/score/objective, fingerprints, termination/provenance, verification 포함 |
| final partition | 모든 input request가 exactly one outcome, node count와 request count를 구분 |
| diagnostics | code/scope/confidence/source/evidence와 `Q-RES-*` 보수적 처리 포함 |
| verifier | independent/cache-free recomputation, pair/partition/terminal/compatibility/capacity/time/resource/metric 검사 포함 |
| matrix | normalized directed matrix만 사용하고 silent fallback 금지 |
| incomplete run | recovery candidate 최소 조건과 product/API 미결정 경계 포함 |
| fixture | digest, byte size, counts, matrix shape와 legacy representation을 read-only로 독립 확인 |
| comparator | 미배정 주문 수 → 배차 차량 수 → 전체 거리 → 전체 시간의 exact lexicographic order |
| 미결정 | `Q-BENCH-01`, `Q-BENCH-02`와 matrix/numeric/objective 질문을 닫지 않음 |
| 재현성 | fixed fingerprints/seeds/order의 정상 `MAX_STEPS_REACHED`로 한정 |
| test plan | result, partition, diagnostics, verifier independence, corruption, termination, rerun, benchmark, performance 분리 포함 |
| 비범위 | algorithm 내부, customer objective engine, parser/API/cloud/storage topology, threshold numbers, route pool/MIP, variants 제외 |

이 초안은 세션 25가 Master의 해 상태와 결과 모델, 종료 metadata, 독립 검증, Win PoC benchmark와 acceptance 절을 작성할 때 사용하는 규범적 통합 입력이다.
