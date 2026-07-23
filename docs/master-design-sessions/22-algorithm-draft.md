# 세션 22 — 알고리즘 파이프라인 규범 초안

> **세션 30 통합 상태 (2026-07-23):** Mixed delivery-only/real pickup route와 single roundtrip은 허용되고 multi-trip만 후속이다. `Q-ALG-02`는 `RESOLVED — KEEP_COW`; apply/undo는 기본 경로·필수 roadmap이 아니다. `Q-ALG-01`과 `Q-BENCH-02`는 protocol만 확정된 `OPEN — EXPERIMENT_REQUIRED`이므로 scorer, randomized starts, `K`, light-search budget, round/worker 수, `maxSteps`, watchdog 수치를 본문이나 코드 default로 만들 수 없다. 현재 의미는 [Master §11~§14](../master-design.md)과 [등록부](../master-design-open-questions.md)를 따른다.

> 상태: `REVIEW INPUT`
>
> 성격: 세션 25의 Master Design에 병합할 초기해·ALNS·후보 상태·종료 계약 초안
>
> 권위 기준: [세션 19 통합 계획](19-integration-plan.md), [세션 20 도메인·입력 초안](20-domain-input-draft.md), [세션 21 정책·평가·목적 초안](21-policy-objective-draft.md)
>
> 편집 범위: 이 문서만 작성한다. 코드, 입력 자료, 기존 설계 문서는 변경하지 않는다.
>
> 기준일: 2026-07-23

## 1. 문서 역할과 규범 수준

이 문서는 RPDPTW (Rich Pickup and Delivery Problem with Time Windows)의 정규화된 문제와 solve별 bound 평가 계약을 받아 초기해 포트폴리오를 만들고, ALNS (Adaptive Large Neighborhood Search)로 개선하며, 검증된 안정 상태를 후속 결과 계층에 넘기는 알고리즘 계약을 정의한다.

이 문서는 구현 완료를 주장하지 않는다. 클래스명, 패키지, 메서드 시그니처, 저장 형식, full result DTO, benchmark 임계값, 실행 provider와 배포 topology를 확정하지 않는다. `InitialSolutionSet`, `CandidateSolution`, `stageBest`, `Move`, `UndoLog` 등은 책임을 설명하기 위한 논리적 이름이며 외부 API 타입을 선점하지 않는다.

규범 표기는 다음과 같다.

| 표기 | 의미 |
|---|---|
| **확정** | 세션 19의 `C-*` 결정 또는 그 결정을 보존하기 위해 필수인 계약. `MUST` 또는 `MUST NOT`으로 해석한다. |
| **잠정** | 세션 19의 `P-*` 방향. 구조는 통합 입력으로 사용하되 수치·기본값·세부 표현은 확정하지 않는다. |
| **열린 질문** | 답에 따라 알고리즘 설정이나 전환 gate가 달라지는 항목. 세션 19의 정확한 `TBD(Q-...)`를 재사용한다. |

### 1.1 이 초안이 소유하는 내용

- 초기해 포트폴리오와 공통 request-pair insertion evaluator의 사용 계약
- 네 개의 필수 construction policy와 deterministic ranking
- bounded light improvement, 후보 검증, 중복 제거, best/diverse 선택과 ALNS handoff
- ALNS의 seeded initialization, destroy/repair, acceptance, adaptive weight, local/fleet improvement
- `current`, stage best, solve best, candidate의 소유권과 갱신 경계
- `SolvePlan` stage, 선행 guard, warm-start와 알고리즘의 상호작용
- 정확한 ALNS step 의미, counter 소유권, 종료 사유와 재현성 범위
- 초기 copy-on-write, commit/discard, cache 무효화
- 후속 apply/undo transaction과 동등성·성능 전환 gate
- 연산자 사전조건·성공 후조건·실패 rollback
- cache-free 재계산, 독립 verifier와 알고리즘 acceptance gate
- route pool/MIP와 선택 변형 문제의 deferred 경계

### 1.2 이 초안이 소유하지 않는 내용

- raw input parser, legacy alias, 단위·시간·행렬 의미의 결정: 세션 20
- 고객사 constraint, metric, price, objective, comparator와 guard의 조립: 세션 21
- full result DTO, 최종 assignment status와 diagnostic, benchmark projector/card: 세션 23
- 구현 Phase 번호, 배포·실행 topology와 전체 roadmap: 세션 24
- 정확한 수치 기본값, 확률, 온도, 보상, weight, seed 값, watchdog 시간
- route pool, set covering/set partitioning MIP 구현
- MDVRP·OVRP·SDVRP 등 선택 변형 문제 구현

## 2. 알고리즘 입력과 end-to-end 파이프라인

### 2.1 소비하는 불변 입력

알고리즘은 다음 두 입력을 불변 snapshot으로 소비한다.

```text
세션 20의 immutable normalized ProblemInstance
+ 세션 21의 solve별 immutable bound profile
```

`ProblemInstance`에서 소비하는 내용은 다음과 같다.

- request, pickup/delivery node, vehicle, terminal, physical location의 고정 ID와 매핑
- request별 정규화된 service meaning과 `servableVehicles`
- fixed-point capacity, time, distance와 그 provenance
- physical location 기준 authoritative directed distance/time matrix
- request pair와 terminal의 정적 불변조건

bound profile에서 소비하는 내용은 다음과 같다.

- propagator와 policy-neutral facts 계약
- composed hard constraints
- metric contributors와 schema
- score policy와 explainable breakdown
- objective schema와 comparator
- `SolvePlan`, stage, previous-objective guard
- stage strategy가 사용하는 construction ranking과 acceptance 계약

알고리즘은 raw `Feature`, 고객사 ID, 가격 항목, Win PoC 전용 순서, 좌표, 속도, legacy matrix 필드를 해석하지 않는다. 고객사마다 다른 의미는 solve 전에 bound contract로 완성되어야 하며, ALNS 본문에는 고객사 또는 특정 fixture 분기가 없어야 한다.

### 2.2 규범적 전체 흐름

```text
immutable normalized ProblemInstance
+ immutable bound profile / SolvePlan
        │
        ▼
bind-time dependency와 configuration 검증
        │
        ▼
초기해 포트폴리오
  ├─ SEQ_FARTHEST
  ├─ SEQ_EARLIEST_DEADLINE
  ├─ PAR_REGRET_2
  └─ RAND_REGRET_3
        │
        ▼
후보별 bounded light improvement
+ 허용된 경우 bounded route elimination
        │
        ▼
cache-free 검증
→ canonical fingerprint 중복 제거
→ bound comparator/admission에 따른 best + diverse 후보 선택
        │
        ▼
InitialSolutionSet
        │
        ▼
SolvePlan stage 1
  warm-start → ALNS steps → verified stage best
        │
        ▼
SolvePlan stage 2..N
  이전 stage best warm-start
  + previous-objective guard
  + ALNS/local/fleet improvement
        │
        ▼
마지막 committed solve best의 독립 full verification
        │
        ▼
세션 23 result/verifier 경계로 handoff
```

각 화살표는 안정 상태 사이의 전이다. destroy 중 pickup만 제거된 상태, repair 중 pickup만 삽입된 상태, apply/undo transaction 중간 상태는 evaluator, comparator, observer, stage orchestrator 또는 결과 변환에 노출할 수 없다.

### 2.3 안정 상태 계약

**확정 (`C-06`).** 알고리즘이 공개하거나 보관하는 모든 solution 상태는 각 request에 대해 다음 중 정확히 하나를 만족해야 한다.

```text
ASSIGNED_IN_SEARCH
= pickup과 delivery가 같은 vehicle route에 각각 정확히 한 번 존재
  AND pickup이 delivery보다 앞섬
  AND requestId가 SearchRequestBank에 없음

UNASSIGNED_IN_SEARCH
= pickup과 delivery가 어떤 route에도 없음
  AND requestId가 SearchRequestBank에 정확히 한 번 존재
```

두 상태는 XOR이다. 부분 pair, 중복 pair, 서로 다른 차량에 나뉜 pair, route와 bank의 동시 membership, route와 bank 양쪽에서 누락된 request는 정상적인 infeasible 후보가 아니라 구조 오류다.

안정 상태는 다음 수준을 구분한다.

| 수준 | 필수 조건 | 허용되는 사용 |
|---|---|---|
| 구조적으로 안정 | request partition, pair, terminal과 bank 불변조건 충족 | mutation 경계 내부 검증 |
| 평가 가능 | 구조적으로 안정하고 변경 route 전파가 완료됨 | hard feasibility 판정 |
| admissible | 모든 bound hard constraint와 stage guard를 통과 | score, acceptance, current 후보 |
| committed | acceptance와 상태 갱신이 완료되고 불변 snapshot이 됨 | 다음 step의 `current` |
| verified best | cache-free/full 검증을 통과한 committed state | `stageBest`, `solveBest`, 최종 handoff |

`SearchRequestBank`에 request가 남아 있는 사실만으로 solution 전체가 hard-infeasible인 것은 아니다. 미배정의 허용 여부와 우선순위는 bound policy가 결정한다. 알고리즘은 mandatory 의미가 미결정인 `Q-OBJ-02`를 임의로 닫지 않는다.

## 3. 초기해 포트폴리오

### 3.1 공통 시작 상태

**확정 (`C-16`).** 초기해는 단일 greedy가 아니라 네 개 policy의 최소 포트폴리오다. 모든 policy는 같은 정규화 문제, bound evaluator와 pair mutation 계약을 사용한다.

각 construction run의 시작 상태는 다음을 만족한다.

```text
모든 route = 고정 start/end terminal만 포함
SearchRequestBank = 전체 request ID 집합
candidate-specific mutable state = 다른 policy/run과 격리
```

빈 경로 상태도 terminal, 초기 적재, delivery-only 표현과 bound hard constraint의 검증을 통과해야 한다. 하나의 policy 실패나 예외가 다른 policy의 route, bank, seed stream, cache 또는 후보를 오염시켜서는 안 된다.

### 3.2 공통 atomic pair insertion evaluator

모든 construction, ALNS repair, paired relocate와 fleet reinsertion은 하나의 공통 pair insertion 의미를 사용해야 한다.

```text
논리적 입력
- stable solution snapshot
- SearchRequestBank에 있는 requestId
- target vehicle/route
- 최종 route 기준 pickupPosition, deliveryPosition
- immutable evaluation context

논리적 출력
- INFEASIBLE + structured bound violation/evaluation evidence
  또는
- FEASIBLE + complete pair move
             + resulting route facts/metrics
             + policy ranking에 필요한 bound values
```

evaluator 계약은 다음과 같다.

1. `pickupPosition < deliveryPosition`이며 두 위치는 두 node를 모두 넣은 최종 route 기준이다.
2. pickup과 delivery를 동일 route에 함께 배치한다.
3. target vehicle이 request의 정규화된 `servableVehicles`에 포함되는지 먼저 확인한다.
4. delivery-only는 세션 20에서 bound된 prefix/initial-load 표현만 사용하고 일반 pickup처럼 임의 위치를 열거하지 않는다.
5. 시간·적재·경로 진행은 authoritative directed physical-location matrix와 bound propagator만 사용한다 (`C-13`).
6. 구조·호환성·hard feasibility를 모두 통과한 option만 ranking 대상으로 공개한다.
7. 평가 과정은 committed route와 bank를 바꾸지 않는다.
8. 선택된 move의 atomic 적용이 성공한 뒤에만 request를 bank에서 제거한다.
9. evaluator의 예상 결과는 move 적용 후 cache-free route 재계산과 같아야 한다.
10. infeasible 또는 계산 실패에 큰 수, plan end, infinity, `EPS`를 점수 sentinel로 사용하지 않는다.

evaluator는 고객사 objective 순서를 하드코딩하지 않는다. feasibility, neutral facts/metrics, score와 objective는 세션 21의 단방향 계약에 따라 분리된 채 반환되거나 참조되어야 한다.

### 3.3 Feasibility-first deterministic ranking

삽입 option의 전체 순서는 다음 계층을 지켜야 한다.

```text
1. 구조 사전조건과 service-pattern 적합성
2. static compatibility
3. bound hard feasibility
4. policy가 선언한 ranking vector
5. stable structural tie-break
```

1~3을 통과하지 못한 option은 유한 penalty를 받아 4단계로 진입하는 것이 아니라 ranking 집합에서 제외된다. 4단계의 값과 방향은 bound construction policy가 선언해야 하며, ALNS core가 고객사 비용이나 Win PoC comparator를 숨겨 추가하지 않는다.

마지막 tie-break는 적어도 다음의 안정된 정체성을 사용해 total order를 만들어야 한다.

```text
vehicle stable index
→ route stable index
→ pickup final position
→ delivery final position
→ request stable index
```

정책이 request 선택을 먼저 수행하는 경우에도 request stable index와 option의 위 순서를 결합해 완전한 total order를 만든다. hash map/set 순회 순서, thread 완료 순서, 객체 주소는 tie-break가 될 수 없다.

### 3.4 네 개의 필수 policy

| Policy ID | 규범적 구조 | 결정성 계약 |
|---|---|---|
| `SEQ_FARTHEST` | bound farthest scorer가 선택한 seed로 route 하나를 더 이상 확장할 수 없을 때까지 구성한 뒤 다음 route로 이동 | scorer 결과와 stable tie-break에 대해 결정적 |
| `SEQ_EARLIEST_DEADLINE` | bound deadline scorer가 가장 촉박하다고 판정한 seed부터 route 하나씩 구성 | scorer 결과와 stable tie-break에 대해 결정적 |
| `PAR_REGRET_2` | 활성 route 전체의 feasible pair insertion option을 보고 각 request의 best/second option 손실을 비교 | stable option order와 regret rank에 대해 결정적 |
| `RAND_REGRET_3` | regret-3 구조를 유지하면서 bound candidate set/distribution에서 파생 seed로 선택 | 같은 seed derivation version과 run ordinal에서 결정적 |

정확한 farthest 의미, deadline 대표값, randomized start 수와 선택 분포는 이 문서가 발명하지 않는다. 이는 `TBD(Q-ALG-01)`이다.

`PAR_REGRET_2`에서 feasible option이 하나뿐인 request는 “유일한 선택지를 잃을 위험”으로 구조적으로 구분할 수 있어야 한다. 이를 infinity나 overflow 가능한 큰 비용으로 표현하지 않는다. feasible option이 하나도 없는 request는 bank에 남고 regret ranking 대상에서 제외된다.

`RAND_REGRET_3`는 전역 난수를 사용하지 않는다. policy ID, run ordinal과 solve에서 전달받은 seed를 versioned derivation contract로 결합한다. 정확한 seed 값과 derivation 공식은 이 문서에서 정하지 않지만, 공식 ID/version과 실제 파생 seed는 metadata에 남겨야 한다.

### 3.5 Construction 완료 조건

각 policy는 더 이상 자신의 규칙으로 적용할 feasible insertion이 없을 때 안정적으로 종료한다.

- 삽입한 request는 같은 route에 완전한 pair로 존재한다.
- 삽입하지 못한 request는 두 node 모두 route에 없고 bank에 남는다.
- route 활성화는 bound vehicle/route 계약을 따른다.
- policy 종료는 wall clock 품질 예산이 아니라 결정적인 work/step 경계로 제한한다.
- watchdog, cancellation 또는 resource signal이 오면 진행 중 pair mutation을 공개하지 않고 해당 policy candidate를 폐기한다.
- 종료된 candidate는 light improvement 전에 cache-free 구조·route 검증을 통과해야 한다.

construction의 insertion evaluation 수, 적용 move 수와 policy별 work counter는 관측 metadata다. ALNS의 `completedSteps`에 포함하지 않는다.

### 3.6 Bounded light improvement

light improvement는 ALNS를 대체하는 별도 무제한 탐색이 아니다. 각 construction candidate에 명시적인 결정적 work budget을 적용한다.

현재 최소 move family는 다음과 같다.

- intra-route paired relocate
- inter-route paired relocate
- pair와 precedence를 보존하는 intra-route 2-opt
- 제한된 2-opt*
- bound policy가 허용하는 bounded route elimination

일반 CVRPTW의 customer move를 node 단위로 그대로 사용하지 않는다. 2-opt 또는 2-opt*가 request pair를 서로 다른 route로 나누거나 delivery를 pickup보다 앞에 두거나 delivery-only prefix를 깨뜨리면 후보 생성 단계에서 제외한다.

light improvement의 규칙은 다음과 같다.

1. move evaluation 순서는 안정적이어야 한다.
2. 모든 move는 공통 pair evaluator와 bound comparator/guard를 사용한다.
3. 기본 개선 lane은 strict improvement만 commit한다.
4. 동점 move를 다양성 목적으로 허용하려면 bound strategy가 이를 명시하고 유한한 work budget과 deterministic tie-break를 함께 제공해야 한다.
5. 실패한 move와 route elimination은 candidate 시작 상태로 전부 rollback한다.
6. 한 candidate에 배정할 move-evaluation/pass budget은 숫자를 발명하지 않고 `TBD(Q-ALG-01)`로 남긴다.
7. watchdog은 내부 열거 loop를 보호하지만 정상 종료 또는 후보 간 품질 예산으로 사용하지 않는다.

bounded route elimination은 제거 route의 모든 request를 bank로 원자 이동한 뒤 다른 route에 pair 단위로 재삽입한다. 전부 재삽입되고 bound comparator와 guard가 결과를 허용할 때만 전체 시도를 commit한다. 고객사 objective가 차량 수를 우선한다고 ALNS core가 가정해서는 안 된다.

### 3.7 검증, 중복 제거와 다양성 선택

후보 선택 파이프라인은 다음 순서를 지킨다.

```text
construction candidate
→ cache-free validation
→ bounded light improvement / route elimination
→ cache-free validation
→ canonical solution fingerprint
→ exact duplicate 제거
→ bound comparator로 best 선택
→ bound admission과 versioned structural-distance policy로 diverse 후보 선택
```

fingerprint는 최소한 다음 의미를 포함한다.

- 각 route의 vehicle 의미와 ordered request/node sequence
- route 경계와 terminal
- `SearchRequestBank` request stable index 집합
- 정규화가 허용한 경우에만 적용되는 vehicle symmetry 규칙

차량 ID가 다르다는 이유만으로 항상 다른 해로 보거나, 반대로 이기종 차량을 검증 없이 대칭으로 취급해서는 안 된다. 대칭 정규화는 bound 비용·호환성·terminal 의미가 동일하다고 증명된 차량에만 적용한다.

**잠정 (`P-10`).** 논리적 `InitialSolutionSet`은 comparator상 best 하나와 제한된 diverse 후보를 제공한다.

- exact duplicate는 하나만 남긴다.
- diverse 후보는 quality/admission guard를 먼저 통과해야 한다.
- 다양성은 objective가 아니라 후보 보존용 구조 지표다.
- K, quality band, distance formula/version, randomized start 수와 ALNS run 배정 방식은 확정하지 않는다.
- 포트폴리오가 성공한 `SEQ_FARTHEST` baseline을 포함한 경우 best는 같은 bound comparator에서 그 baseline보다 열등할 수 없다.

### 3.8 ALNS handoff

ALNS 진입은 검증된 initial candidate ID를 명시적으로 선택해야 한다.

```text
InitialSolutionSet
→ bound warm-start selection
→ candidate별 독립 immutable snapshot
→ stage/run별 current와 best 초기화
```

한 initial candidate를 여러 run이 사용하더라도 mutable route, `SearchRequestBank`, cache, adaptive weight와 random stream을 공유하지 않는다. 어떤 initial candidate/policy가 어느 run과 stage에 연결되었는지 metadata에 기록한다.

best만 사용할지, diverse 후보를 어떻게 multi-start에 배정할지는 `P-10`의 잠정 부분이며 이 문서가 기본값을 만들지 않는다. 실행 가능한 plan은 선택 방식을 명시적으로 bind해야 한다.

## 4. ALNS 실행 계약

### 4.1 Seeded initialization

각 ALNS run은 다음을 명시적으로 받아 시작한다.

```text
verified initial candidate
stage ID와 bound stage contract
run ordinal
base seed와 seed derivation version
operator registry/version
stage maxSteps
```

algorithm core는 실행 중 새 base seed를 만들지 않는다. seed 생략을 외부 입력이 허용한다면 application boundary가 접수 시 한 번 생성·보존한 실제 값을 core에 전달해야 한다.

난수 stream은 최소한 다음 namespace를 분리해 파생한다.

- initial portfolio randomized policy
- destroy operator selection
- destroy 내부 random choice
- repair operator selection
- repair/noise 내부 random choice
- acceptance
- multi-start 또는 stage/run assignment

한 stream의 draw 수 변화가 무관한 stream의 결과를 바꾸지 않도록 derivation namespace와 version을 고정한다. 정확한 seed 값과 해시/혼합 공식은 이 문서의 기본값이 아니지만 실제 seed와 derivation version은 재현성 metadata다.

### 4.2 한 ALNS step의 규범적 흐름

```text
committed current를 base로 candidate boundary 시작
→ destroy operator 선택
→ q개의 request pair를 제거하고 bank에 등록
→ repair operator 선택
→ bank request를 feasible pair option으로 재삽입
→ configured in-step local improvement가 있으면 bounded 수행
→ candidate 안정화
→ 구조 검증 + cache-free affected-route propagation
→ bound hard feasibility 평가
→ previous-objective guard 평가
→ metric / score / objective 평가
→ acceptance 판정
→ candidate commit 또는 discard/undo
→ current 갱신 여부 확정
→ verified stageBest 갱신 여부 확정
→ operator outcome, weight, acceptance state 갱신
→ step 전체 commit
→ stageCompletedSteps를 정확히 한 번 증가
```

선택된 operator가 현재 상태에 적용 가능하지 않으면 mutation 없는 `NOT_APPLICABLE` outcome으로 step을 안정적으로 끝낼 수 있다. 이 경우에도 operator outcome과 adaptive state를 계약대로 마감한 뒤에만 completed step으로 센다. 구성 오류, 구조 오류, 예외, watchdog 또는 cancellation으로 step이 완결되지 못하면 completed step이 아니다.

destroy/repair가 선택하는 `q`는 node 수가 아니라 request 수다. q의 공식, 최소·최대 범위와 분포는 bound algorithm configuration이 제공하고 fingerprint에 포함해야 하며 이 문서가 수치 기본값을 정하지 않는다.

### 4.3 Destroy operator 계약

현재 operator registry는 research-backed request removal family를 지원할 수 있다.

| Family | request 단위 의미 |
|---|---|
| random removal | 파생 seed와 stable population order로 assigned request를 선택 |
| worst removal | bound evaluation에서 request pair 전체의 현재 기여를 비교 |
| related/Shaw removal | pickup·delivery 위치, 시간, 수요, 호환성 등 등록된 중립 fact로 request 간 관련성을 비교 |
| sequence-based removal | node 조각이 아니라 선택 구간과 교차하는 완전한 request 집합을 제거 |
| time-oriented removal | 등록된 시간 fact가 관련된 request 집합을 제거 |
| route removal | 선택 route의 모든 request를 완전한 pair로 제거 |

이 표는 모든 family를 처음부터 활성화하라는 수치·제품 기본값이 아니다. active operator set과 각 operator의 설정/version은 bound stage strategy가 명시한다.

destroy는 assigned request만 선택한다. pickup 또는 delivery 하나만 고르거나, 두 node 중 하나를 찾지 못했는데 나머지만 제거하거나, 이미 bank에 있는 request를 중복 제거할 수 없다. 성공 시 두 node 제거와 bank membership 추가가 한 atomic mutation이다.

### 4.4 Repair operator 계약

repair는 `SearchRequestBank`의 request를 선택하고 공통 evaluator가 제공한 feasible pair option만 적용한다.

현재 repair family는 다음 구조를 지원할 수 있다.

- greedy best-position pair insertion
- regret-2 pair insertion
- regret-3/4 pair insertion
- seed-based randomized/noise variant

초기해와 ALNS repair는 option enumeration, pair feasibility와 regret ranking primitive를 공유한다. 고객사별 urgency, 가격, compatibility 의미를 repair 본문에 복제하지 않는다.

repair 종료 후에도 삽입되지 않은 request는 두 node 모두 route에 없는 채 bank에 남는다. 한 request의 삽입 실패가 전체 candidate의 구조 실패는 아니며, final unassignment diagnostic도 아니다.

### 4.5 Feasibility와 acceptance 경계

acceptance는 다음 gate를 순서대로 통과한 candidate에만 적용한다.

```text
구조적으로 안정
→ bound hard-feasible
→ current stage의 previous-objective guard admissible
→ metric/score/objective evaluation 성공
→ acceptance policy
```

다음 규칙은 확정이다.

1. 구조 오류 또는 hard-infeasible candidate는 즉시 폐기한다.
2. hard-infeasible candidate를 penalty, 높은 온도, noise, operator reward로 되살릴 수 없다.
3. 이전 stage objective guard를 통과하지 못한 candidate는 하위 objective 개선만으로 current나 best가 될 수 없다.
4. comparator상 current보다 좋은 admissible candidate는 commit한다.
5. current보다 좋지 않은 admissible candidate는 stage가 명시적으로 bind한 acceptance policy가 허용할 때만 current가 될 수 있다.
6. Simulated Annealing을 사용하면 acceptance energy, unit/scale, temperature schedule, random stream과 모든 파라미터가 bound configuration에 명시되어야 한다.
7. 사전식 comparator를 숨은 Big-M scalar로 평탄화해 SA delta를 만들지 않는다.
8. stage 의미에 맞는 acceptance energy를 bind할 수 없으면 probabilistic acceptance를 묵시적으로 활성화하지 않는다.
9. `stageBest`와 `solveBest`는 non-improving acceptance로 악화되지 않는다.

SA는 feasible 영역 안에서 `current` chain을 다양화하는 장치다. feasibility 경계나 확정된 guard를 완화하는 수단이 아니다. 선행 guard의 exact relaxation/tolerance가 향후 승인되더라도 세션 21의 bound guard contract를 통해서만 들어오며 ALNS가 독자적으로 결정하지 않는다.

### 4.6 Adaptive operator weight

destroy와 repair operator의 선택·학습 상태는 run과 stage별로 격리한다.

각 completed step은 선택된 operator pair에 하나의 결정적 outcome classification을 제공한다. 예를 들면 다음 의미 범주를 구분할 수 있다.

- verified new stage best
- current comparator상 improvement
- accepted non-improving admissible candidate
- rejected admissible candidate
- hard-infeasible candidate
- operator `NOT_APPLICABLE`

정확한 reward 값, 초기 weight, reaction factor, update segment 길이와 선택 확률 공식은 이 문서가 정하지 않는다. bound configuration이 이를 모두 제공하고 fingerprint에 포함해야 한다.

adaptive state 갱신은 다음 계약을 따른다.

1. operator registry와 iteration order는 안정적이다.
2. 선택과 갱신은 같은 seed와 completed-step trace에서 동일하다.
3. 미완료 step은 score, usage count, weight, temperature/schedule을 전진시키지 않는다.
4. 구조 오류나 구현 예외를 정상적인 낮은 reward로 숨기지 않는다.
5. segment boundary 갱신도 step commit의 일부다.
6. 병렬 candidate race의 first-winner를 weight 입력으로 사용하지 않는다.
7. weight는 탐색 선택 확률이지 feasibility나 objective 우선순위가 아니다.

### 4.7 `current`, incumbent와 best

모호한 `incumbent` 하나로 서로 다른 상태를 부르지 않는다.

| 상태 | 의미 | 변경 규칙 |
|---|---|---|
| `candidate` | 한 step 또는 fleet attempt 안의 임시 상태 | candidate boundary 안에서만 변경 |
| `current` | 다음 ALNS step의 기준인 마지막 accepted committed state | admissible acceptance 성공 때만 교체 |
| `stageEntryBest` | 이전 stage에서 받은 검증된 warm-start/fallback | stage 동안 불변 |
| `stageBest` | 현재 stage comparator와 guard 아래 발견한 최선의 verified committed state | verified strict improvement 때만 교체 |
| `solveBest` | 마지막으로 완료된 stage가 넘긴 검증된 best | stage handoff에서만 교체 |

외부 문서나 telemetry가 `incumbent`라는 말을 사용한다면 `current` 또는 `stageBest` 중 무엇인지 명시해야 한다.

stage 시작 시 `current`, `stageEntryBest`, `stageBest`는 같은 검증된 warm-start 구조에서 시작한다. non-improving acceptance는 `current`만 바꿀 수 있다. 후속 stage가 실패하거나 중단되면 마지막 검증된 `solveBest` 또는 해당 stage의 검증된 best를 오염시키지 않는다.

`best`는 committed state여야 한다. mutable candidate를 best로 가리키거나, full verification 전에 best snapshot을 외부에 공개해서는 안 된다.

### 4.8 Local search와 fleet improvement

ALNS 내 local improvement는 bound stage strategy가 호출 위치와 deterministic work budget을 명시할 때만 수행한다.

- candidate repair 직후의 in-step local improvement
- 일정한 completed-step 경계에서의 stage-local intensification
- stage 종료 전의 bounded fleet improvement

각 위치의 counter와 ALNS step 포함 여부는 plan metadata에 명확해야 한다. 한 in-step local search는 해당 ALNS step의 완료 전 작업이며 별도 ALNS step을 증가시키지 않는다. stage-local/fleet phase의 move-evaluation counter도 ALNS `completedSteps`와 분리한다.

move는 request pair 단위다.

- paired relocate
- paired exchange/swap
- pair-safe 2-opt
- pair-safe 2-opt*
- 완전한 request segment의 Or-opt/cross-exchange
- route elimination/fleet removal

fleet improvement는 차량 수가 모든 고객사의 최상위 목표라고 가정하지 않는다. 제거 route의 request를 전부 재배정한 안정 candidate를 bound comparator와 guard가 허용할 때만 commit한다. 실패하면 route, bank, active-vehicle state, cache visibility와 aggregate를 시도 전 상태로 전부 복원한다.

## 5. Step, 종료와 재현성

### 5.1 ALNS step의 정확한 의미

**확정 (`C-08`).** 정상 품질 예산의 단위는 ALNS step이며 wall clock이 아니다.

하나의 completed ALNS step은 다음을 모두 끝낸 원자적인 알고리즘 전이다.

```text
operator selection
+ destroy outcome
+ repair outcome
+ configured in-step improvement
+ stable candidate 검증·평가
+ acceptance
+ candidate commit/discard
+ current/best 갱신 판정
+ operator/adaptive/acceptance state 갱신
= completed step 1
```

다음은 ALNS step이 아니다.

- 입력 정규화와 profile binding
- 초기해 portfolio construction
- portfolio light improvement와 diverse selection
- stage 간 warm-start handoff
- 별도 stage-local/fleet phase의 move evaluation
- 최종 full verification
- 결과 변환·직렬화

step 중 watchdog, cancellation, resource signal, 예외 또는 구조 오류가 발생하면 candidate를 discard/undo하고 adaptive state도 step 시작 상태로 되돌린다. 이 step은 completed counter에 포함하지 않는다.

### 5.2 Counter 소유권

counter는 다음처럼 단일 writer를 가진다.

| Counter | 소유자 | 증가 시점 |
|---|---|---|
| construction evaluations/applied moves | 각 portfolio policy run | 해당 deterministic work unit 완료 후 |
| light improvement move evaluations/passes | 해당 light-improvement runner | move 평가/pass 완료 후 |
| `stageCompletedSteps` | 현재 ALNS stage loop | step 전체 commit 직후 정확히 한 번 |
| solve aggregate completed steps | `SolvePlan` orchestrator | stage counter snapshot을 checked sum으로 집계 |
| verifier/recomputation counts | verifier/telemetry | 검증 호출 완료 후 |

operator, evaluator, local search와 observer는 `stageCompletedSteps`를 직접 증가시킬 수 없다.

각 ALNS stage는 bound budget reference에서 명시적인 양의 `maxSteps`를 받아야 한다. 정상적으로 완료된 solve의 aggregate requested/completed steps는 stage별 값을 순서대로 보존하며, solve-level 합계를 노출한다면 stage `maxSteps`의 checked sum이어야 한다. 별도의 숨은 전역 counter와 stage counter를 동시에 품질 예산으로 사용하지 않는다.

stage `n`은 정확히 자신의 `maxSteps`를 완료하면 정상 종료한다. 모든 stage가 정상 step 종료와 handoff를 마친 경우 solve의 정상 종료 사유는 `MAX_STEPS_REACHED`다.

### 5.3 종료 사유의 두 계층

algorithm boundary의 최소 종료 사유는 다음과 같다.

| 종료 사유 | 의미 | 품질·재현성 지위 |
|---|---|---|
| `MAX_STEPS_REACHED` | 모든 계획된 stage의 deterministic step budget 정상 완료 | 정상 품질 예산; 강한 재현성 대상 |
| `WATCHDOG_REACHED` | monotonic deadline으로 병리적 장기 실행을 안전 중단 | 예외적 안전 종료; 동등 품질 예산 아님 |
| `CANCELLED` | 명시적 외부 취소 신호를 협력적으로 처리 | 사용자/상위 실행 의도에 따른 중단; watchdog과 다름 |
| `RESOURCE_LIMIT_REACHED` | solver가 감지·처리 가능한 메모리/작업공간 등 안전 자원 한계 도달 | 예외적 자원 종료; 품질 예산 아님 |

`PLATFORM_TIMEOUT`과 `FAILED`는 solver가 정상 algorithm termination record를 완성하지 못했을 때의 상위 실행 상태다. 이를 `WATCHDOG_REACHED`나 `MAX_STEPS_REACHED`로 가장해서는 안 된다. input/config binding 실패도 탐색 종료 사유가 아니라 solve 시작 전 오류다.

중단 시 마지막 verified committed best가 있다면 세션 23에 recovery candidate로 전달할 수 있다. 이를 최종 성공 결과로 인정할지와 외부 상태 표현은 세션 23의 책임이다. verified best가 없으면 성공 후보를 만들지 않는다.

### 5.4 Watchdog, cancellation과 resource handling

watchdog은 monotonic clock을 사용한다. wall clock은 제출·시작·종료 시각 같은 관측 metadata에만 사용한다.

deadline/cancellation/resource checkpoint는 다음처럼 오래 걸릴 수 있는 내부 경계에도 있어야 한다.

- construction option enumeration
- destroy selection과 relatedness ranking
- repair의 request/route/position 열거
- local search neighborhood 열거
- route elimination/fleet reinsertion
- cache-free verification

signal을 확인한 연산자는 새 mutation을 시작하지 않고, 진행 중 mutation 또는 transaction을 전부 rollback한 뒤 상위 호출자에게 정확한 signal 종류를 전달한다.

처리 가능한 자원 한계와 예외적으로 프로세스가 즉시 종료되는 자원 고갈을 구분한다. 후자의 경우 rollback이나 candidate 기록을 주장하지 않고 상위 실행이 `FAILED`로 판정한다.

watchdog timeout 값, 자원 한계와 cleanup 여유의 정확한 숫자는 이 문서가 정하지 않는다. 특히 wall-clock watchdog을 늘리거나 줄여 정상 품질 budget처럼 비교하지 않는다.

### 5.5 강한 재현성 envelope

**확정 (`C-22`).** 강한 재현성은 다음 조건을 모두 고정하고 `MAX_STEPS_REACHED`로 끝난 실행에 한정한다.

```text
normalized problem fingerprint
+ authoritative directed matrix fingerprint
+ numeric/time/adapter provenance
+ exact bound profile/config fingerprint
+ constraint/metric/score/objective/comparator/SolvePlan versions
+ initial portfolio configuration와 selected candidate fingerprint
+ operator registry와 algorithm configuration
+ build/runtime compatibility fingerprint
+ base/derived seeds와 derivation version
+ stage별 maxSteps
+ stable iteration/tie-break rules
+ candidate state strategy version
= 동일한 canonical trace와 최종 verified solution
```

재현성을 위해 다음을 지킨다.

- 전역 random과 암묵적 seed 생성을 금지한다.
- 모든 collection iteration은 안정된 순서를 가진다.
- equal objective score는 versioned canonical structural tie-break로 결정한다.
- candidate/portfolio/run 병렬 완료 순서는 선택 결과에 영향을 주지 않는다.
- parallel reduction이 필요하면 결정적 merge order를 정의한다.
- fixed-point/numeric policy를 소비하며 algorithm hot path에서 새 rounding이나 floating tolerance를 만들지 않는다.
- cache hit/miss 여부가 evaluation 또는 tie-break 결과를 바꾸지 않는다.

`WATCHDOG_REACHED`, `CANCELLED`, `RESOURCE_LIMIT_REACHED`, `PLATFORM_TIMEOUT`, `FAILED`는 같은 품질·step 예산의 강한 재현성 비교 대상이 아니다.

### 5.6 최소 algorithm provenance

세션 23에 넘길 algorithm provenance는 full DTO를 정의하지 않되 최소한 다음 의미를 식별할 수 있어야 한다.

- problem, matrix, numeric/time/adapter fingerprints
- bound profile, evaluator, comparator, guard와 `SolvePlan` versions
- portfolio configuration, source policy/run ordinal, initial candidate fingerprint
- base seed, stage/run별 실제 derived seed, derivation version
- operator registry와 각 operator configuration/version
- acceptance/adaptive configuration versions
- stage별 requested/completed steps와 separate inner-work counters
- termination reason과 마지막 완결 stage/step
- state strategy (`COPY_ON_WRITE` 또는 gate를 통과한 후속 strategy)와 version
- cache/evaluator/verifier versions
- canonical final solution fingerprint
- elapsed time은 관측값으로만 기록

공식 benchmark seed, maxSteps, watchdog과 regression gate의 숫자는 세션 19의 `TBD(Q-BENCH-02)`이며 이 문서가 발명하지 않는다.

## 6. 후보 상태, commit/discard와 cache

### 6.1 초기 copy-on-write 모델

**확정 (`C-09`).** 초기 안전 구현은 변경 route copy-on-write와 독립 `SearchRequestBank`를 사용한다.

```text
committed Solution snapshot
├─ immutable/shared ProblemInstance
├─ immutable Route references
├─ independent SearchRequestBank value
├─ validated aggregate snapshot
└─ structural fingerprint

Candidate boundary
├─ base committed Solution
├─ shallow route reference container
├─ first-write 시 복사된 changed routes
├─ independent copied SearchRequestBank
└─ invalidated candidate-level aggregate/cache visibility
```

규칙은 다음과 같다.

1. `current`, `stageBest`, `solveBest`는 committed immutable snapshot으로 취급한다.
2. candidate가 처음 route를 변경하기 전에 해당 route의 구조를 복사한다.
3. 같은 candidate에서 이미 복사한 route는 그 candidate만 재사용할 수 있다.
4. 변경하지 않은 route는 공유할 수 있지만 mutation API를 노출하지 않는다.
5. candidate bank는 base bank와 독립이다.
6. 문제·matrix·bound profile 등 진짜 불변 데이터만 run 간 공유한다.
7. current와 best의 route/cache를 candidate가 직접 바꿀 수 없다.

초기 모델은 매 step 전체 solution deep copy를 요구하지 않는다. 동시에 apply/undo gate 전에는 committed current를 시험적으로 직접 변경한 뒤 수작업 보상하는 방식도 허용하지 않는다.

### 6.2 Commit과 discard

candidate lifecycle은 다음 두 경로만 가진다.

```text
candidate 유효 + accepted
→ 필요한 full gate 통과
→ freeze
→ 새 immutable current로 atomic reference 교체

candidate infeasible/rejected/interrupted/failed
→ candidate 전체 discard
→ 기존 current 그대로 유지
```

freeze 후 candidate mutation API는 사용할 수 없다. best 갱신이 필요한 경우 current와 분리된 불변 snapshot으로 보존한다. 공유 불변성을 구현에서 강제할 수 없다면 best 갱신 시 독립 snapshot을 만드는 안전 경로를 택해야 한다.

Fleet improvement와 stage 실패도 별도 보상 규칙을 만들지 않고 같은 candidate commit/discard 의미를 사용한다.

### 6.3 Cache 소유권과 무효화

route sequence, request ownership, vehicle/terminal binding과 필요한 active-vehicle state가 원본이다. 다음은 모두 재계산 가능한 파생 상태다.

- arrival, service start/end, waiting과 load
- route feasibility와 propagation facts
- route/solution metrics
- score breakdown과 objective vector
- solution aggregate와 structural hash
- insertion option/cost table

cache 규칙은 다음과 같다.

1. cache는 정확히 하나의 problem/profile/run과 구조 version에 속한다.
2. mutable cache는 policy run, ALNS run 또는 candidate 사이에 공유하지 않는다.
3. pair insert/remove/reorder, vehicle/terminal 변경은 해당 route cache를 무효화한다.
4. route 또는 bank가 바뀌면 solution metric, score, objective, fingerprint cache를 무효화한다.
5. bank가 ranking/admission에 영향을 주면 관련 insertion/solution cache도 무효화한다.
6. insertion cache key는 route/vehicle identity, route version, request, 두 position, evaluator/profile version과 필요한 solution/bank generation을 구분해야 한다.
7. cache가 의심스러우면 복원하지 않고 버린 뒤 cache-free 전량 재계산한다.
8. structural hash는 빠른 중복 후보 탐지 도구일 뿐 verifier를 대체하지 않는다.

승인된 모든 상태에서 다음 등식이 성립해야 한다.

```text
cached/incremental feasibility, facts, metrics, score, objective
=
cache-free full recomputation 결과
```

### 6.4 후속 apply/undo transaction

apply/undo는 초기 경로가 아니라 검증된 성능 최적화 후보다.

```text
begin candidate transaction on mutable current
→ 모든 Move.apply와 side effect를 undo log에 기록
→ stable candidate view에서 검증·평가
   ├─ accepted: commit, undo log 폐기, immutable 경계 재설정
   └─ rejected/interrupted/failed: reverse order로 undo
→ current 구조와 cache visibility 검증
→ transaction 종료
```

undo contract는 최소한 다음을 복원한다.

- 변경 route의 node/request sequence와 길이
- `SearchRequestBank` membership
- active-vehicle 또는 fleet 상태가 존재할 경우 그 값
- solution 구조 aggregate의 원본 의미
- transaction 전 observer-visible state

best snapshot은 transaction 중 mutable current와 가변 route를 공유할 수 없다. 여러 move는 적용 역순으로 undo한다. 중간 move 실패, 예외, watchdog, cancellation과 resource signal은 모두 같은 transaction 종료 경로를 사용한다.

첫 apply/undo 구현은 과거 cache 객체를 복원하려 하기보다 영향을 받은 cache를 전부 무효화하고 구조에서 다시 계산하는 안전 경로를 사용해야 한다. stale cache를 되살려 구조만 같아 보이게 해서는 안 된다.

### 6.5 동등성·성능 전환 gate

**잠정 (`P-11`).** apply/undo를 기본 경로로 바꾸기 전에 다음 gate를 모두 통과해야 한다.

1. 공통 move suite에서 `apply → undo` 후 route sequence, bank, fleet state와 canonical fingerprint가 원래와 같다.
2. 중간 예외·watchdog·cancellation·resource signal 주입 뒤에도 current와 best가 보존된다.
3. 같은 problem, profile, initial candidate, operator 선택 trace와 seed에서 copy-on-write와 apply/undo의 step별 candidate outcome이 같다.
4. 각 completed step의 accept/reject, current, stageBest, objective와 adaptive update가 같다.
5. cache-free verifier 결과와 최종 canonical solution이 같다.
6. 실제 benchmark fixture와 microbenchmark에서 copy/scratch가 측정된 병목임을 보여준다.
7. allocation, copied nodes/routes, GC, solve throughput/latency 등 승인된 성능 지표에서 전환 가치가 확인된다.
8. 정확성·재현성·관측 가능성 회귀가 없다.

어떤 측정 기준과 임계값에서 기본 경로로 전환할지는 `TBD(Q-ALG-02)`다. gate 값, 적용 규모, cache 복구 방식과 전환 시점을 이 문서가 발명하지 않는다. 성능 이득이 입증되지 않으면 copy-on-write를 유지하는 것도 유효한 gate 결과다.

## 7. Operator 사전조건·후조건과 rollback

### 7.1 공통 mutation 계약

모든 construction, destroy, repair, local/fleet move는 다음 공통 계약을 가진다.

```text
사전조건
- 입력은 verified 또는 최소한 구조적으로 안정한 solution
- 대상 request의 route/bank 상태가 연산 종류와 일치
- target route/vehicle/position이 정규화된 문제에 존재
- mutation boundary가 열려 있음

성공 후조건
- pickup, delivery, route와 bank가 함께 변경
- 전체 request partition과 pair invariant 유지
- changed route의 terminal/service-pattern/hard feasibility 유지
- 관련 cache/aggregate/fingerprint가 무효화 또는 새 구조와 일치
- 안정 상태에서만 결과 공개

실패 후조건
- route sequence, bank, fleet state, cache visibility,
  score/objective aggregate와 fingerprint가 호출 전과 동일
- partial, duplicated, missing 또는 split request가 남지 않음
- current와 best가 변경되지 않음
```

### 7.2 연산별 계약

| 연산 | 추가 사전조건 | 성공 후조건 | 실패·중단 후조건 |
|---|---|---|---|
| pair insertion | request가 bank에 있고 두 node는 모든 route에 없음 | 같은 target route에 pickup/delivery 각각 1회, pickup 선행, bank에서 제거 | route와 bank 모두 호출 전 상태 |
| pair removal/destroy | request가 한 route에 완전 pair로 배정되고 bank에 없음 | 두 node 제거, bank에 request 정확히 1회 | 한 node도 제거된 채 남지 않음 |
| paired relocate | request가 source route에 완전 배정, target option이 feasible | source에서 pair 제거, target에 pair 삽입, bank 미노출 | source/target/bank 전부 복원 |
| paired exchange/swap | 두 request가 각자 완전 배정되고 중복 선택 아님 | 두 request 전체가 허용된 route/position으로 atomic 교환 | 한쪽만 교환된 상태 금지 |
| sequence/segment move | 포함·교차하는 request 집합이 완전하게 계산됨 | 모든 대상 request가 pair invariant를 유지 | raw node segment 일부만 이동 금지 |
| pair-safe 2-opt/2-opt* | 결과 route 전체에서 same-vehicle, precedence, service pattern 사전 검증 가능 | 결과 route 모두 full propagation/hard-feasible | reversal/tail 교환 일부 적용 금지 |
| route removal/fleet attempt | 대상 route의 request 집합이 완전하고 fallback snapshot 존재 | 모든 request가 다른 route에 재삽입되고 제거 route/fleet 상태가 일관 | attempt 전체를 시작 snapshot으로 복원 |
| candidate reject | candidate가 stable evaluation까지 도달했으나 acceptance 실패 | 없음; base current 유지 | candidate cache/bank가 current에 노출되지 않음 |

delivery-only virtual pickup이 prefix 방식으로 bound되었다면 모든 move는 그 prefix의 정규형을 유지한다. real pickup-delivery와 delivery-only 혼합, multi-trip과 trip 경계의 의미는 세션 20의 `Q-REQ-01`, `Q-REQ-02`가 해결된 bound input만 소비하며 알고리즘이 자체 규칙을 만들지 않는다.

### 7.3 Failure 분류와 복구

연산 결과는 다음을 구분한다.

| 분류 | 의미 | 알고리즘 처리 |
|---|---|---|
| infeasible option | 정상적인 hard constraint 위반 | mutation 없이 option 제외 |
| `NOT_APPLICABLE` | 현재 stable state에 operator 사전조건이 맞지 않음 | mutation 없이 deterministic outcome 처리 |
| rejected candidate | admissible하지만 acceptance 실패 | candidate discard 또는 full undo |
| interrupted | watchdog, cancellation, resource signal | full rollback, 미완료 step, 정확한 종료 사유 전달 |
| structural violation | pair/bank/terminal 불변조건 실패 | candidate 폐기·current 검증; 구현 결함으로 처리 |
| evaluation/calculation failure | overflow, missing bound dependency 등 | 큰 값으로 대체하지 않고 solve failure 경계로 전달 |
| implementation exception | 예상하지 못한 연산 실패 | rollback 후 current 검증; 검증 실패 시 solve 중단 |

구조 오류를 infeasible request나 낮은 operator reward로 숨겨 탐색을 계속하지 않는다. rollback 뒤 current의 full 구조 검증이 실패하면 사용할 수 있는 committed state가 없다고 보고 solve를 실패시킨다.

observer, comparator와 telemetry hook은 mutation 중간을 읽지 않는다. 실패 evidence를 최종 unassignment reason으로 직접 저장하지도 않는다.

## 8. Cache-free 재계산, verifier와 acceptance gate

### 8.1 Cache-free 기준 계산

cache-free 기준 계산은 route sequence, bank와 immutable inputs에서 다음을 처음부터 다시 만든다.

- request partition, exactly-once, same-vehicle, precedence
- terminal과 service-pattern 규칙
- 정규화된 `servableVehicles` 호환성
- authoritative directed matrix를 사용한 leg distance/time
- time, load, route resource propagation
- bound hard constraints
- policy-neutral facts와 metric snapshots
- score breakdown, objective vector와 comparator inputs

기준 계산은 search cache, insertion table, cached objective나 structural hash의 유효성을 전제로 하지 않는다. 공통 immutable domain과 bound policy 의미는 공유할 수 있지만 incremental cache path가 자기 결과를 스스로 검증하는 구조가 되어서는 안 된다.

### 8.2 필수 동등성 지점

| 지점 | 비교할 값 | 실패 처리 |
|---|---|---|
| insertion evaluator | 예상 resulting route/facts/metrics 대 move 적용 후 cache-free route 계산 | move/evaluator 결함; candidate 금지 |
| pair mutation 후 | 국소 후조건 대 full request partition 검사 | rollback 후 구현 결함 |
| construction 완료 | candidate cache 대 cache-free full solution | 해당 policy 후보 폐기 |
| light/fleet improvement 완료 | 개선 전후 stable state와 cache-free objective/guard | attempt rollback 또는 후보 폐기 |
| ALNS acceptance 직전 | full 구조 + changed-route cache-free propagation + solution composition | acceptance 금지 |
| `stageBest` 갱신 전 | 독립 full feasibility/metrics/objective | best 갱신 금지 |
| stage handoff | stage best의 canonical full verification | 이전 verified solve best 유지 |
| solve 종료 | 최종 solve best의 독립 full verification | 정상 결과 생성 금지 |
| apply/undo gate | COW와 apply/undo의 step trace·final full result | 기본 경로 전환 금지 |

acceptance hot path를 최적화하더라도 full 구조 검사와 changed-route cache-free 검증을 생략하지 않는다. solution-level hard constraint나 metric이 전체 route/bank 상태에 의존하면 그 composition도 acceptance 전에 다시 평가한다.

### 8.3 테스트·acceptance gate

후속 구현은 최소한 다음 suite를 가져야 한다.

#### Request와 operator

- 모든 request에 대해 `ASSIGNED_IN_SEARCH XOR UNASSIGNED_IN_SEARCH`
- pair insertion/removal/relocate/exchange 성공 후 same vehicle, exactly once, precedence 유지
- 실패·거절·중단 전후 route, bank, cache visibility와 fingerprint 동일
- sequence, 2-opt와 2-opt*가 pair/service-pattern을 깨는 경우 사전 거절
- route elimination 마지막 reinsertion 실패에도 전체 rollback

#### Portfolio

- 네 policy가 모두 같은 common evaluator를 사용
- 같은 problem/profile/config/seed에서 동일 후보와 metadata 생성
- 한 policy 실패가 다른 policy와 seed stream을 오염시키지 않음
- exact duplicate는 하나만 남고 best가 baseline보다 열등하지 않음
- diverse selection은 admission을 먼저 지키고 stable order를 가짐
- `Q-ALG-01`의 미결정 수치가 코드 상수로 숨어 있지 않음

#### ALNS와 stages

- hard-infeasible candidate는 모든 온도와 reward 설정에서 reject
- guard 위반 후보는 하위 objective가 좋아도 current/best가 되지 않음
- accepted non-improving candidate는 current만 바꾸고 best는 보존
- stage `n`은 stage `n-1`의 verified best와 구조적으로 같은 warm-start에서 시작
- stage 실패·중단 시 이전 verified solve best 보존
- operator selection, adaptive update와 tie-break가 stable iteration order를 따름

#### Step과 종료

- 정확히 stage `maxSteps`개 commit 후 정상 stage 종료
- mid-step exception/watchdog/cancellation/resource signal은 completed count와 adaptive state를 증가시키지 않음
- fake monotonic clock으로 watchdog 경계와 rollback 검증
- `MAX_STEPS_REACHED`만 강한 재현성 quality suite에 포함
- `WATCHDOG_REACHED`, `CANCELLED`, `RESOURCE_LIMIT_REACHED`, platform failure를 서로 다른 suite로 검증

#### State와 cache

- COW candidate 변경이 base current/best route와 bank에 영향 없음
- changed route만 복사되고 shared route는 immutable
- cache hit/miss 여부와 무관하게 동일 evaluation·tie-break 결과
- cached/incremental result와 cache-free result 완전 일치
- directed asymmetry fixture에서 reverse arc나 좌표 fallback을 사용하지 않음
- apply/undo round-trip과 COW trace equivalence

성능 gate는 정확성 suite를 통과한 뒤 수행한다. 이 문서는 benchmark 숫자, 허용 gap, latency, allocation 또는 apply/undo 전환 임계값을 만들지 않는다.

## 9. 현재 범위와 deferred 경계

### 9.1 현재 설계·구현 범위

현재 범위는 다음이다.

- 공통 atomic pair insertion evaluator
- 네 policy의 initial-solution portfolio
- deterministic bounded light improvement
- 검증, 중복 제거, best/diverse `InitialSolutionSet`
- pair-based destroy/repair ALNS
- bound acceptance와 adaptive operator selection
- stage guard, warm-start와 local/fleet improvement
- step-normal termination과 exceptional watchdog/cancellation/resource handling
- initial copy-on-write candidate state
- cache invalidation과 cache-free equivalence
- apply/undo의 미래 호환 mutation boundary와 전환 gate

### 9.2 Route pool/MIP

**확정 (`C-17`).** 현재 범위는 `InitialSolutionSet`과 검증된 ALNS best까지다. 다음은 deferred다.

- 초기해·ALNS route column 수집과 전역 route pool
- route pool pruning, admission, persistence
- set covering/set partitioning model
- MIP solver 선택과 실행
- MIP solution을 RPDPTW solution으로 복원

현재 solution/candidate metadata에서 ordered route sequence, source policy와 verified metrics를 얻을 수 있는 확장 경계만 보존한다. MIP 전용 column 타입이나 dependency를 algorithm core에 미리 넣지 않는다.

재개하려면 최소한 portfolio/ALNS 재현성, 독립 verifier, benchmark baseline과 route 수집 가치가 먼저 입증되고 별도 범위 승인이 있어야 한다. MIP가 향후 도입되더라도 실패·중단 시 마지막 verified ALNS best를 오염시키지 않아야 한다.

### 9.3 선택 변형과 topology

MDVRP·OVRP·SDVRP 등 선택 변형 구현은 `Q-VAR-01`에 따라 deferred다. 특히 split delivery를 지원하기 위해 현재 request 원자성을 느슨하게 만들지 않는다.

실제 provider/product/deployment topology는 `Q-INFRA-01`에 따라 deferred다. 이 문서는 논리적 cancellation, watchdog, resource signal과 metadata만 정의하며 특정 queue, job, container, cloud timeout 또는 storage를 알고리즘 계약으로 고정하지 않는다.

HGS, route pool/MIP, 논문상의 infeasible-penalty search와 고객사별 fallback/dummy 상태를 현재 ALNS의 묵시적 variant로 활성화하지 않는다. 새 범위는 현재 pair·feasibility·result 경계에 대한 별도 검토가 필요하다.

## 10. 결정·질문 추적성

### 10.1 확정 결정

| 결정 | 이 초안의 반영 |
|---|---|
| `C-06` | §2.3, §3.2, §4.3~§4.4, §7에서 request pair를 모든 mutation의 최소 단위로 사용하고 stable route/bank XOR를 보장 |
| `C-08` | §5에서 `maxSteps`를 정상 종료로, watchdog을 예외적 안전 종료로 분리 |
| `C-09` | §6에서 초기 changed-route COW + independent bank와 후속 apply/undo gate를 정의 |
| `C-13` | §2.1, §3.2, §8에서 authoritative directed physical-location matrix만 사용 |
| `C-16` | §3에서 네 construction policy, common evaluator, light improvement, 검증·best/diverse selection을 현재 범위로 정의 |
| `C-17` | §9.2에서 route pool/MIP를 deferred로 제한 |
| `C-22` | §5.5에서 같은 fingerprint/seed/step/tie-break의 `MAX_STEPS_REACHED` 실행만 강한 재현성 대상으로 정의 |

독립 verifier gate에는 세션 19의 `C-21`도 함께 소비하지만, full result와 benchmark 판정의 소유권은 세션 23에 남긴다.

### 10.2 잠정 사항

| 잠정안 | 이 초안의 반영 | 계속 잠정인 부분 |
|---|---|---|
| `P-10` | §3.7~§3.8의 best + limited diverse initial candidates | K, quality band, distance/admission 세부, randomized start 수와 ALNS 배정 방식 |
| `P-11` | §6.5의 profiling + trace/full-verification equivalence gate | 전환 임계값, cache 복구 세부, 기본 경로 전환 시점 |

세션 21의 provisional guard 방향은 algorithm core가 결정하지 않는다. 이 초안은 solve별로 bind된 comparator와 guard만 소비한다.

### 10.3 정확한 inherited open-question ID

이 문서는 새 `Q-*`를 만들지 않는다.

| ID | 이 초안과의 관계 | 결정 전 안전한 처리 |
|---|---|---|
| `Q-ALG-01` | farthest/deadline scorer, randomized start 수, top-K, light-search step budget | 숫자·공식을 하드코딩하지 않고 명시적 bound configuration 없이는 해당 선택을 확정하지 않음 |
| `Q-ALG-02` | apply/undo 기본 경로 전환 측정 기준 | COW를 안전 기준으로 유지하고 동등성·프로파일링 자료만 수집 |
| `Q-BENCH-02` | 공식 benchmark seeds, maxSteps, watchdog, champion/seed별 regression gate | runtime algorithm contract과 benchmark 공식 숫자를 분리; 공식 baseline 숫자 발행 금지 |
| `Q-REQ-01` | delivery-only와 real pickup-delivery의 route 혼합 | 세션 20이 승인한 normalized service meaning만 소비 |
| `Q-REQ-02` | multi-trip과 pair의 trip 경계 | 현재 bound single-trip 의미를 유지하고 algorithm이 trip 규칙을 만들지 않음 |
| `Q-OBJ-02` | mandatory order의 hard/soft/lexicographic 의미 | bank membership을 임의 hard failure나 penalty로 바꾸지 않음 |
| `Q-OBJ-03` | outsourced/deferred가 결과 분류인지 search option인지 | 별도 fallback search state를 활성화하지 않음 |
| `Q-VAR-01` | optional variant feasibility study 시점 | 현재 pair/state 계약을 느슨하게 만들지 않음 |
| `Q-INFRA-01` | 실제 provider/product/deployment topology | 논리적 signal과 metadata만 정의 |

수치 정규화의 `Q-NUM-01`, `Q-NUM-02`, `Q-NUM-03`과 행렬·시간 입력 질문은 세션 20이 소유한다. 알고리즘은 답이 반영된 immutable normalized values와 provenance를 소비할 뿐 별도 rounding 또는 fallback으로 질문을 닫지 않는다.

## 11. 후속 세션 handoff

### 11.1 세션 23 — result와 benchmark

세션 23은 이 초안에서 다음을 입력으로 받는다.

- 독립 full verification을 통과한 final routes와 `SearchRequestBank`
- canonical solution fingerprint
- initial candidate/policy와 stage/run seed provenance
- stage별 requested/completed steps와 별도 inner-work counters
- 정확한 algorithm termination reason
- 마지막 completed stage/step과 recovery candidate 유무
- final cache-free facts, metrics, score와 objective
- state strategy, evaluator/cache/verifier versions

세션 23은 full result DTO, final assignment status, diagnostics, partial-result 공개 정책과 benchmark card를 소유한다. algorithm의 insertion 실패 또는 bank membership을 최종 원인으로 그대로 노출해서는 안 된다. `Q-BENCH-01`, `Q-BENCH-02`의 공식 benchmark 산식·숫자도 세션 23에서 열린 상태를 보존한다.

### 11.2 세션 24 — roadmap과 Phase gate

세션 24는 다음 dependency를 구현 Phase와 완료 gate로 배열한다.

```text
normalized domain
→ bound evaluation
→ common pair evaluator
→ four-policy portfolio
→ COW ALNS/stages
→ independent verifier/result
→ benchmark baseline
→ measured apply/undo decision
```

`P-11`은 성능 최적화를 자동 구현하는 Phase가 아니라 측정·동등성·전환 판단 gate로 배치해야 한다. route pool/MIP, optional variants와 provider topology는 별도 재개 조건이 있는 deferred 항목으로 유지한다.

### 11.3 세션 25 — Master 통합

세션 25는 다음을 유지한다.

- single greedy와 wall-clock 동등 품질 budget 서술을 이 초안으로 교체
- request-pair operator와 stable-state 계약
- 네 policy portfolio와 `P-10`의 잠정 표지
- initial COW와 `P-11`, `Q-ALG-02` apply/undo gate
- `MAX_STEPS_REACHED`의 정상 종료 지위와 exceptional reasons의 구분
- 고객사별 objective/constraint가 bound profile을 통해 들어오는 경계
- route pool/MIP와 optional variants의 deferred 지위

통합 과정에서 이 초안의 논리적 이름을 최종 Java/API 타입으로 과도하게 확정하거나 숫자 기본값을 추가하지 않는다.

## 12. 범위 self-audit

| 검사 | 결과 |
|---|---|
| 수정 대상 | `docs/master-design-sessions/22-algorithm-draft.md`만 생성 |
| 구현 주장 | 문서 계약만 작성하고 코드·빌드·배포 변경 없음 |
| request 단위 | same vehicle, exactly once, pickup-before-delivery, route/bank XOR와 atomic failure 포함 |
| customer 격리 | bound evaluator/comparator/stage/guard만 소비하고 고객사·Win PoC 분기 없음 |
| travel | authoritative directed physical-location matrix만 사용 |
| 초기해 | 네 named policy, common evaluator, deterministic ranking, bounded improvement, 검증·중복·best/diverse·ALNS handoff 포함 |
| ALNS | seeded destroy/repair, feasibility-bounded acceptance, adaptive weight, current/best, local/fleet와 stages 포함 |
| 종료 | exact step/counter ownership, maxSteps 정상 종료, watchdog/cancellation/resource/platform 구분 포함 |
| 재현성 | strong envelope을 `MAX_STEPS_REACHED`로 한정하고 seed/tie/order/metadata 포함 |
| 후보 상태 | initial COW, commit/discard, cache invalidation과 later apply/undo transaction 포함 |
| 성능 gate | trace/full-verifier equivalence와 profiling을 요구하고 `Q-ALG-02` 임계값 미발명 |
| 검증 | cache-free recomputation points, independent best/final verification과 acceptance tests 포함 |
| 미결정 | `Q-ALG-01`, `Q-ALG-02`, `Q-BENCH-02` 등 inherited ID만 사용 |
| 비범위 | full result DTO, parser, benchmark threshold, provider topology, route pool/MIP, optional variant 구현 제외 |

이 초안은 세션 25가 Master의 algorithm pipeline, candidate state/cache/rollback, termination/reproducibility 절을 작성할 때 사용하는 규범적 통합 입력이다.
