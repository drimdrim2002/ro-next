# 세션 24 — 구현 Roadmap·논리 시스템 경계 규범 초안

> **세션 30 통합 상태 (2026-07-23):** 본문의 `OPEN 26 + DEFERRED 2` gate map은 현재 상태가 아니다. 세션 29 통합 후 `RESOLVED 24`, `OPEN — EXPERIMENT_REQUIRED 2`, `DEFERRED 2`다. `RM-1/2/5`는 해결된 exact 결정으로 진행하고, `RM-3/6`의 official configuration·baseline만 두 실험 항목이 막는다. `RM-7`은 COW 기본을 유지한 profiling/선택적 재제안이고 apply/undo 자동 전환 phase가 아니다. Physical topology와 variants는 계속 deferred다. 현재 roadmap은 [Master §15](../master-design.md#15-implementation-roadmap와-phase-gates), 상태는 [등록부](../deprecated/master-design-open-questions.md)를 따른다.

> 상태: `REVIEW INPUT`
>
> 성격: 세션 25의 Master Design에 병합할 의존성 기반 구현 roadmap, 논리 시스템 경계, 위험·마이그레이션·deferred gate 초안
>
> 권위 기준: [세션 19 통합 계획](19-integration-plan.md), [세션 20 도메인·입력 초안](20-domain-input-draft.md), [세션 21 정책·평가·목적 초안](21-policy-objective-draft.md), [세션 22 알고리즘 초안](22-algorithm-draft.md), [세션 23 결과·벤치마크 초안](23-result-benchmark-draft.md)
>
> 역사적 입력: [세션 01](01-gcp-architecture.md)의 provider-specific 구조는 당시의 조사·관심사만 보존한다. 최신 cloud/product-neutral 결정이 목표 topology를 대체한다.
>
> 편집 범위: 이 문서만 작성한다. 코드, 입력 자료, 기존 Master, 질문 파일, 인덱스와 배포 자료를 변경하지 않는다.

## 1. 문서 역할과 규범 수준

이 문서는 RPDPTW (Rich Pickup and Delivery Problem with Time Windows) 구현을 언제까지 끝낼지 예측하는 일정표가 아니다. 세션 20부터 23까지 확정한 계약을 어떤 의존 순서로 향후 개발하고, 어떤 증거가 있어야 다음 단계로 넘어갈 수 있는지 정의하는 **규범적 개발 roadmap**이다.

이 문서는 현재 코드가 해당 계약을 구현했거나 준수한다고 주장하지 않는다. 아래의 “산출물”은 모두 향후 만들어야 할 개발 산출물이며, 실제 완료 상태는 구현 착수 후 별도 진행 기록과 검증 증거로만 판단한다.

규범 표기는 다음과 같다.

| 표기 | 의미 |
|---|---|
| **확정** | 세션 19의 `C-*` 결정 또는 그 결정을 지키기 위한 필수 의존성·gate. `MUST` 또는 `MUST NOT`으로 해석한다. |
| **잠정** | 세션 19의 `P-*` 방향. 구조는 roadmap 입력으로 사용하되 이름·기본값·수치·세부 API는 확정하지 않는다. |
| **열린 질문** | 답에 따라 특정 의미·기능·공식 증거가 달라지는 항목. 세션 19의 정확한 `TBD(Q-...)`를 유지한다. |
| **DEFERRED** | 재개 조건과 별도 승인이 올 때까지 현재 구현 범위에서 제외된 항목. 새로운 결정 상태가 아니다. |

### 1.1 이 초안이 소유하는 내용

- roadmap 원칙, 의존성 graph, phase와 milestone
- phase별 진입 의존성, 설계 입력, 향후 개발 산출물, 검증 gate, 종료 증거와 비목표
- 결정 차단 질문과 차단하지 않는 구현 작업의 분리
- 임계 경로와 contract double·작은 예제로 병행 가능한 작업
- 위험 register와 책임 경계
- `C-*`와 세션 20~23의 phase·증거·최종 Master 절 추적성
- route pool/MIP, optional variants, concrete infrastructure topology와 학술 benchmark 확장의 deferred 재개 조건
- 현재 코드와 목표 계약 사이의 migration·compatibility·문서 거버넌스 경로
- provider/product와 무관한 논리 port와 책임 경계
- 세션 25 통합 handoff와 이 문서의 scope self-audit

### 1.2 이 초안이 소유하지 않는 내용

- 새 도메인, 수치, 시간, 행렬, 목적함수 또는 알고리즘 결정
- 열린 `Q-*`의 답, 임시 기본값, 정확한 수치 threshold
- 일정, 날짜, 인원, 팀 규모 또는 납기
- exact 외부 API/JSON schema, Java 타입·패키지·메서드
- 특정 cloud, provider, product, queue, storage, runtime, 배포 단위 또는 물리 module topology
- 코드 구현·수정, test/fixture 생성, build·배포 변경
- 현재 코드·테스트·배포가 목표 계약을 이미 충족한다는 선언

## 2. Normative roadmap 원칙

### 2.1 계약이 기능보다 먼저다

개발 순서는 기능 이름이 아니라 의미 의존성으로 정한다.

```text
문서·결정 통제
→ normalized domain/input
→ bound evaluation/profile
→ common pair evaluator와 initial portfolio
→ ALNS + copy-on-write
→ independent verifier와 final result
→ Win PoC manifest/baseline
→ measured performance와 apply/undo 결정
→ compatibility migration과 후속 확장
```

아래 계층은 위 계층의 미결정 의미를 자체 기본값으로 채울 수 없다.

- 평가 계층은 raw input을 다시 해석하지 않는다.
- 알고리즘은 customer ID, legacy 필드, 가격 항목 또는 benchmark 순서를 해석하지 않는다.
- 결과 계층은 search cache나 마지막 삽입 실패를 진실로 취급하지 않는다.
- benchmark 계층은 verifier를 우회하거나 다른 manifest의 값을 비교하지 않는다.
- 실행 adapter는 solver 의미를 transport나 provider 제약에 맞추어 바꾸지 않는다.

### 2.2 결정 gate는 필요한 의미만 차단한다

열린 질문 하나가 전체 개발을 중단시키는 것은 아니다. 질문은 그 답을 실제로 소비하는 가장 이른 gate만 차단한다.

예를 들어 정확한 rounding이 열려 있어도 다음 작업은 진행할 수 있다.

- fixed-point policy interface와 provenance 계약 설계
- 명시적 test-only policy를 주입한 경계 예제
- overflow와 missing-policy rejection test 설계
- immutable normalized model과 pair invariant test

반면 승인된 rounding 없이 다음은 진행 완료로 인정할 수 없다.

- 실제 업무 입력의 canonical normalization
- 실제 수요·용량 경계의 공식 feasibility 판정
- Win PoC 공식 baseline 수치 발행

### 2.3 정확성 gate를 성능 gate보다 앞에 둔다

다음 순서를 바꾸지 않는다.

```text
구조 불변조건
→ hard feasibility
→ cache-free evaluation 동등성
→ independent verification
→ step-limited reproducibility
→ benchmark baseline
→ profiling
→ apply/undo 또는 다른 최적화 결정
```

성능 자료구조는 검증되지 않은 후보를 허용하거나 재현성 envelope을 약화시키는 근거가 될 수 없다. apply/undo를 도입하지 않고 copy-on-write를 유지하는 것도 유효한 측정 결과다.

### 2.4 고객 확장은 조립 경계로 수용한다

새 고객의 단가·제약·지표·목적 순서는 다음의 가장 좁은 경계에 배치한다.

```text
input/normalization
→ static compatibility
→ bound hard constraint
→ neutral metric contributor
→ score component
→ objective/comparator
→ SolvePlan/profile composition
```

새 물리 진행 의미가 필요하다는 증거가 있을 때만 좁은 공통 propagator/fact seam 확장을 검토한다. 고객 이름이나 새 가격 항목만으로 공통 route state, ALNS 또는 verifier에 고객별 분기를 추가해서는 안 된다 (`C-03`, `C-04`).

### 2.5 정상 품질 실행과 안전 중단을 분리한다

- 정상 품질 예산은 `maxSteps`와 완료된 step이다 (`C-08`).
- wall-clock limit는 watchdog 안전장치이며 같은 품질 budget이 아니다.
- 강한 재현성은 고정 fingerprint·seed·step·tie-break 조건에서 `MAX_STEPS_REACHED`로 끝난 실행에 한정한다 (`C-22`).
- cancellation, resource limit, platform failure와 watchdog은 서로 다른 상태와 증거를 가져야 한다.

### 2.6 검증과 publication은 solver 탐색에서 독립한다

최종 후보는 solver의 cache, feasibility flag, summary 또는 objective를 신뢰하지 않는 독립 verifier를 통과해야 한다 (`C-21`). 결과 publication과 benchmark projection은 verifier `PASS` 뒤에만 진행한다.

### 2.7 물리 topology 결정은 마지막이다

현재 Master는 submit/input, solve execution, status/artifact, result retrieval, cancellation/finalization의 논리 port와 책임만 정의한다 (`C-20`, `P-14`). 물리 provider/product/runtime/deployment topology는 다음이 확보되기 전 재개하지 않는다.

- core와 result 계약의 안정성
- 실제 workload와 성능 측정
- retry·idempotency·cancellation·retention 요구
- 보안·접근·감사 요구
- `Q-INFRA-01`에 대한 별도 승인

세션 01의 provider-specific 구조는 위 관심사를 발견한 역사적 evidence일 뿐 현재 목표 topology가 아니다.

## 3. 논리 시스템 port와 책임 경계

### 3.1 논리 port

| 논리 port | 입력 의미 | 책임 | 출력 의미 | 이 단계에서 정하지 않는 것 |
|---|---|---|---|---|
| submission/input | 입력 payload 또는 불변 input reference, 요청 identity, exact profile/config 선택 정보 | 접수, 형식 경계, idempotent submission identity, 입력 lineage 보존 | accepted/rejected submission과 normalization 요청 | transport, endpoint, object key, 인증 제품 |
| normalization/binding | 외부 입력, adapter/version, numeric/time/matrix policy, exact profile key/version | 세션 20 정규화와 세션 21 dependency binding | immutable normalized problem과 immutable bound profile 또는 명시적 오류 | wire schema 이름, registry 제품, 물리 module |
| solve execution | normalized problem, bound profile, plan, seed, step budget, safety signal contract | portfolio, ALNS stage, committed candidate와 run provenance 생성 | committed candidate 또는 정확한 termination/failure record | process/container/job 종류와 병렬 실행 제품 |
| candidate finalization | committed/recovery candidate, immutable inputs와 provenance | independent verification, outcome/diagnostic/result integrity 생성 | verified published-result candidate 또는 invalid record | storage 제품과 공개 API shape |
| status/artifact | submission/run/result identity와 immutable lineage | 상태 전이, artifact identity, fingerprint와 retention hook 보존 | 조회 가능한 상태·artifact reference | database, bucket, queue, path naming |
| result retrieval | solve identity와 접근 context | verified result만 반환하고 incomplete/invalid/recovery 의미를 구분 | status, verified result 또는 명시적 unavailable/error | protocol, pagination, client SDK |
| cancellation | solve identity와 cancellation intent | 협력적 signal 전달, 미완료 mutation rollback 요구, 실제 종료 사유 기록 | cancellation acknowledgement와 최종 run state | signal transport, process kill 정책 |

`result retrieval`은 verifier가 완료되지 않은 route를 정상 결과로 반환해서는 안 된다. `cancellation`은 watchdog이나 failure를 사용자 취소로 다시 이름 붙여서는 안 된다.

### 3.2 계층 책임과 의존 방향

```text
external adapters
  ├─ submission/input adapter
  ├─ status/artifact adapter
  └─ retrieval/cancellation adapter
            │
            ▼
application orchestration
  ├─ normalize/bind use case
  ├─ solve execution use case
  ├─ finalization/publication use case
  └─ status/idempotency coordination
            │
            ▼
domain + evaluation + algorithm
  ├─ immutable RPDPTW model
  ├─ propagation/constraints/metrics/score/objective
  ├─ portfolio/ALNS/candidate state
  └─ independent result verification contract
```

의존 방향은 외부에서 안쪽으로 향한다.

- core는 transport, storage, orchestration 또는 인증 타입을 참조하지 않는다.
- application은 논리 port를 조정하지만 raw business field의 의미를 새로 해석하지 않는다.
- adapter는 외부 representation을 변환하지만 core invariant와 comparator를 바꾸지 않는다.
- verifier는 search cache와 같은 mutable algorithm state에 의존하지 않는다.
- 하나의 물리 artifact나 여러 artifact 중 어느 형태를 택할지는 이 문서의 결정이 아니다.

### 3.3 논리 실행 상태

정확한 enum과 외부 schema는 후속 계약이지만 최소 의미는 구분해야 한다.

```text
접수됨
→ 실행 준비/실행 중
→ 정상 완료 + verified result
또는
→ 취소됨
→ watchdog/resource safety 종료
→ 실행 실패
→ result verification 실패
```

상태가 없다는 이유만으로 “실행 중”이라고 추정하지 않는다. 정상 algorithm 종료, 상위 execution 상태, verification 상태와 result publication 상태는 서로 다른 의미다.

## 4. 전체 dependency graph와 phase sequence

### 4.1 의존성 graph

```text
RM-0 문서·결정 통제
  │
  ├─ 계약 example/double lane ───────────────────────────────┐
  │                                                          │
  └─ 필요한 Q-* 승인 lane                                   │
       │                                                     │
       ▼                                                     │
RM-1 normalized domain/input                                 │
       │                                                     │
       ▼                                                     │
RM-2 bound evaluation/profile                                │
       │                                                     │
       ▼                                                     │
RM-3 common pair evaluator + initial portfolio               │
       │                                                     │
       ▼                                                     │
RM-4 ALNS stages + copy-on-write                             │
       │                                                     │
       ▼                                                     │
RM-5 independent verifier + result finalization              │
       │                                                     │
       ▼                                                     │
RM-6 Win PoC manifest + official baseline                    │
       │                                                     │
       ▼                                                     │
RM-7 measured performance + apply/undo decision              │
       │                                                     │
       ▼                                                     │
RM-8 compatibility migration through logical ports ◀─────────┘
       │
       ▼
RM-9 separately approved deferred follow-ups
  ├─ route pool/MIP
  ├─ optional variants
  ├─ academic benchmark expansion
  └─ concrete infrastructure/provider/product topology — last
```

`RM-8`의 port contract·fake adapter·state example 설계는 `RM-0` 뒤 병행할 수 있다. 실제 compatibility acceptance와 publication 전환은 `RM-5`의 verified result와 `RM-6`의 기준선 없이 완료할 수 없다.

### 4.2 Phase 상태 규칙

각 phase는 다음 상태만 가진다고 가정한다. exact 진행 기록 schema는 후속 문서가 소유한다.

| 상태 의미 | 조건 |
|---|---|
| not started | 진입 dependency나 필수 결정 gate가 충족되지 않음 |
| design ready | 문서 계약, example과 acceptance plan이 준비됨 |
| implementation in progress | 향후 코드 작업이 실제 시작되었으나 exit evidence가 미완성 |
| blocked | 해당 phase가 요구하는 정확한 `Q-*` 또는 외부 evidence가 없음 |
| done | 모든 exit gate와 독립 evidence가 충족됨 |

문서 작성 완료는 개발 phase의 `done`이 아니다.

## 5. Phase별 규범 계약

### 5.1 `RM-0` — 문서·결정 통제와 계약 baseline

| 항목 | 내용 |
|---|---|
| 진입 의존성 | 세션 19의 `C-*`, `P-*`, 중앙 `Q-*`; 세션 20~23 draft; 세션 18 governance |
| 문서·설계 입력 | 최종 Master 목표 목차, source hierarchy, current-vs-target inventory, historical provider-specific evidence |
| 향후 개발 산출물 | `REVIEW` 상태의 통합 Master, 중앙 open-question register, 결정·요구·invariant ID, 상세 계약 링크, phase acceptance index |
| 검증·acceptance gate | `C-01`~`C-22` 누락 0건; 모든 미결정은 정확한 `Q-*`에 연결; 잠정 라벨 유지; 구현 완료·topology 주장 없음; 금지된 구 의미와 묵시적 기본값 없음 |
| 종료 artifact/evidence | 검토 가능한 Master version, 질문별 상태·owner·evidence·반영 위치, traceability report, 문서 lint/link report |
| 명시적 비목표 | 코드·fixture·배포 작성, 열린 질문 답변 발명, 기존 코드 compliance 선언, 일정 산정 |

`RM-0` 완료는 모든 질문이 답해졌다는 뜻이 아니다. 질문이 어느 gate를 차단하며 그전의 안전한 동작이 무엇인지 등록되었다는 뜻이다.

### 5.2 `RM-1` — Normalized domain과 input contract

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-0`; 세션 20 계약; 실제 adapter/profile 활성화에 필요한 `Q-NUM-*`, `Q-MTX-*`, `Q-TIME-*`, `Q-IN-*`, `Q-COMP-*`, `Q-REQ-*`의 scoped 승인 |
| 문서·설계 입력 | 세션 20의 request/node/vehicle/location, service meaning, planning period, fixed-point, matrix, compatibility, bank와 mutation 계약 |
| 향후 개발 산출물 | immutable normalized problem model; versioned adapter/validation pipeline; fixed-point policy binding; physical-location directed matrix mapping; service-pattern binding; `servableVehicles`; search-only bank; pair/terminal invariant validation |
| 검증·acceptance gate | same vehicle, pair completeness, exactly once, precedence, route/bank XOR; delivery-only start-loading 의미; 999 CBM 적용 provenance; plan-end 유한 상한과 explicit infeasible; overflow rejection; no matrix fallback; external/core ID round-trip |
| 종료 artifact/evidence | 작은 hand-calculated normalized examples, boundary/overflow cases, adapter provenance sample, directed-asymmetry case, invariant property report, unresolved-field rejection report |
| 명시적 비목표 | objective/price, ALNS, final diagnostics, benchmark 수치, concrete external schema, multi-trip/variant 의미 |

질문이 열려 있는 동안 구조 model과 validator는 명시적 contract double로 시험할 수 있다. 실제 legacy input을 canonical production instance로 승인하는 것은 필요한 의미가 결정된 뒤에만 가능하다.

### 5.3 `RM-2` — Bound evaluation, profile과 objective assembly

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-1`의 immutable normalized facts; 세션 21 계약; 활성 profile이 사용하는 `Q-OBJ-*`와 numeric policy |
| 문서·설계 입력 | propagation/hard feasibility/neutral metric/score/objective/`SolvePlan` 분리, profile binding과 lifecycle |
| 향후 개발 산출물 | policy-neutral propagation facts; composed hard constraints; metric contributor/schema; score component/breakdown; objective schema/comparator; `SolvePlan`; immutable bound profile과 dependency validator |
| 검증·acceptance gate | raw input 재해석 없음; infeasible candidate를 cost로 복구하지 않음; missing metric/unit/version bind-time rejection; score breakdown 합계; comparator exact order; previous-objective guard; profile/run mutable-state 격리 |
| 종료 artifact/evidence | 서로 다른 profile 조립 example, score policy만 바꾼 same-route hard-feasibility invariance evidence, metric/score/objective hand calculation, missing dependency rejection, profile fingerprint와 lifecycle test report |
| 명시적 비목표 | operator 흐름, benchmark comparator를 전역 objective로 지정, exact registry/API, customer-specific core branch |

새 고객 요구가 기존 fact로 표현되지 않는 경우에만 narrow fact/propagator seam review를 요청한다. 그 review 없이 공통 route state를 확장하지 않는다.

### 5.4 `RM-3` — Common pair evaluator와 initial-solution portfolio

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-2` bound evaluator/comparator; 세션 20 pair mutation; 세션 22 portfolio 계약 |
| 문서·설계 입력 | common pair insertion evaluator, deterministic ranking, four-policy portfolio, bounded light improvement, validation/deduplication/diversity |
| 향후 개발 산출물 | atomic pair insertion primitive; `SEQ_FARTHEST`, `SEQ_EARLIEST_DEADLINE`, `PAR_REGRET_2`, `RAND_REGRET_3`; bounded pair-safe improvement; candidate fingerprint/deduplication; best + admitted diverse initial candidate set |
| 검증·acceptance gate | 네 policy가 같은 evaluator 사용; infeasible option ranking 제외; stable tie-break; failure isolation; cache-free validation; exact duplicate 제거; baseline policy보다 best가 열등하지 않음; derived seed provenance |
| 종료 artifact/evidence | policy별 deterministic trace, pair insertion expected-vs-full recomputation report, light-move rollback evidence, selected-candidate lineage, 명시적 configuration record |
| 명시적 비목표 | route pool/MIP, hidden default for `Q-ALG-01`, 무제한 local search, single greedy로 범위 축소 |

`Q-ALG-01`이 열려 있어도 policy seam과 explicit test configuration을 구현·시험할 수 있다. 공식 default와 Win PoC configuration은 질문이 해결되기 전 승인할 수 없다.

### 5.5 `RM-4` — ALNS stages와 copy-on-write candidate state

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-3`의 verified initial candidate set; 세션 21 stage/guard; 세션 22 algorithm/state 계약 |
| 문서·설계 입력 | seeded destroy/repair, acceptance, adaptive selection, current/stageBest/solveBest, exact step, COW, cache invalidation, rollback |
| 향후 개발 산출물 | request-pair destroy/repair ALNS; stage orchestration와 warm-start; guarded acceptance; adaptive operator state; changed-route copy-on-write; independent bank; local/fleet attempt boundary; termination/provenance |
| 검증·acceptance gate | mid-step failure/watchdog/cancellation/resource signal에서 full discard; incomplete step counter 미증가; current/best 불변성; hard infeasible rejection; guard 보존; cache-free/incremental equivalence; fixed seed/step stable trace |
| 종료 artifact/evidence | step transition trace, injected interruption report, COW isolation/rollback proof, stage handoff evidence, cache corruption detection, `MAX_STEPS_REACHED` deterministic rerun result |
| 명시적 비목표 | apply/undo 기본 경로, route pool/MIP, wall-clock quality budget, customer/fixture branch, exact tuning number |

watchdog safety는 필수지만 정상 품질 비교에는 사용하지 않는다. apply/undo를 위한 mutation seam은 보존하되 `RM-7` 전에는 COW가 안전 기준이다.

### 5.6 `RM-5` — Independent verifier, result finalization과 publication gate

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-4`의 committed candidate와 provenance; 세션 23 계약; 세션 20~21 immutable inputs |
| 문서·설계 입력 | search/result 분리, complete outcome partition, diagnostic confidence, independent cache-free verifier, recovery candidate 의미 |
| 향후 개발 산출물 | independent verifier; run/execution record; verified result model; route/stop/request mapping; assignment outcomes; bounded diagnostics; result fingerprint/integrity; recovery-candidate validation path |
| 검증·acceptance gate | request partition, pair, terminal, compatibility, capacity/time/resource, directed matrix와 metric/score/objective를 처음부터 재검산; verifier `FAIL` 시 정상 payload 금지; summary/outcome/result fingerprint 일치 |
| 종료 artifact/evidence | verifier independence fault-injection report, corrupted-candidate rejection, full result partition test, diagnostic confidence evidence, exceptional termination/recovery matrix |
| 명시적 비목표 | final status를 bank에 저장, unverified result publication, product별 recovery exposure 정책, exact external DTO/API |

`Q-RES-01`이 열려 있는 profile은 verified regular assignment만 `ASSIGNED`, 나머지는 보수적으로 `UNASSIGNED`로 finalization한다. `Q-RES-02`가 열려 있으면 실제 audit를 수행한 request 외에는 exhaustive confidence를 발행하지 않는다.

### 5.7 `RM-6` — Win PoC manifest, baseline과 regression contract

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-1`의 fixture 해석에 필요한 승인; `RM-5` verifier/result; `Q-BENCH-01`~`Q-BENCH-03`, 공식 실행에 필요한 numeric/matrix/time/input 결정 |
| 문서·설계 입력 | 세션 23의 read-only [`data/win_poc_case.json`](../../data/win_poc_case.json) fixture identity, manifest/card, comparator, comparability, deterministic rerun과 challenger workflow |
| 향후 개발 산출물 | immutable benchmark manifest; fixture/adapter/matrix/profile/algorithm/verifier fingerprints; official run cards; actual-run champion; baseline card; challenger comparability/regression report |
| 검증·acceptance gate | 모든 official quality run이 verified이고 `MAX_STEPS_REACHED`; comparator가 **미배정 주문 수 → 배차 차량 수 → 전체 거리 → 전체 시간**의 exact lexicographic order; synthetic champion 금지; deterministic rerun; fingerprint mismatch는 `NOT COMPARABLE` |
| 종료 artifact/evidence | approved manifest, per-run verification/card set, champion lineage, reproducibility report, baseline review record |
| 명시적 비목표 | baseline 숫자 추측, formula 없는 `전체 시간`, wall-clock quality metric, 자동 baseline overwrite, 학술 benchmark 확장 |

Win PoC fixture는 read-only input이지 정답 해가 아니다. 최초 baseline은 이 phase를 통과한 실제 run evidence로만 생성한다.

### 5.8 `RM-7` — Measured performance와 apply/undo 결정

| 항목 | 내용 |
|---|---|
| 진입 의존성 | `RM-6`의 stable baseline과 correctness/reproducibility suite; 세션 22 `P-11`; `Q-ALG-02`의 승인된 판단 기준 |
| 문서·설계 입력 | COW cost 관찰, cache-free verifier, step trace equivalence, quality/performance 분리 |
| 향후 개발 산출물 | profile report; allocation/copy/cache/verification cost breakdown; 필요한 경우 apply/undo candidate path; fault injection과 trace equivalence suite; keep-COW 또는 switch 결정을 기록한 evidence |
| 검증·acceptance gate | 측정된 병목; `apply → undo` 구조 round-trip; COW와 step별 outcome/current/best/adaptive state/final verifier 동등; interruption rollback; 재현성·관측 가능성 회귀 없음; 승인된 performance criterion 충족 |
| 종료 artifact/evidence | before/after comparable report, equivalence result, independent verification, decision record와 rollback plan |
| 명시적 비목표 | threshold 발명, profiling 전 최적화, cache 복구 shortcut, COW 폐기 자동화 |

성능 이득이 충분하지 않거나 동등성 gate가 실패하면 COW 유지로 phase를 정상 종료할 수 있다. 이 경우 apply/undo는 구현 목표가 아니라 검토 결과 `NO_SWITCH`다.

### 5.9 `RM-8` — Compatibility migration과 논리 port integration

| 항목 | 내용 |
|---|---|
| 진입 의존성 | port contract 설계는 `RM-0`; 실제 verified integration은 `RM-5`; 기준 비교는 `RM-6`; 성능·state strategy는 `RM-7` 결과 |
| 문서·설계 입력 | §3 논리 port, versioned adapter, idempotency/status/artifact lineage, current-vs-target inventory |
| 향후 개발 산출물 | compatibility matrix; versioned legacy adapter; logical application use cases; shadow/dual-evaluation plan; result/status mapping; cancellation and retry contract; controlled cutover/rollback procedure |
| 검증·acceptance gate | 동일 input/profile identity의 lineage 보존; adapter coercion provenance; retry가 같은 logical run을 가리킴; unverified result 미노출; cancellation 정확한 상태; old/new 의미 차이 명시; Win baseline과 independent verifier 회귀 |
| 종료 artifact/evidence | compatibility report, shadow comparison, verified end-to-end port test, cutover decision evidence, rollback rehearsal record |
| 명시적 비목표 | 현재 코드가 이미 compliant라는 선언, 기존 경로 즉시 삭제, provider/product 선택, fixed deployment unit, exact public API schema |

현재 코드 또는 실행 골격은 migration inventory와 adapter candidate일 수 있지만, 각 계약의 test evidence 없이 목표 설계와 호환된다고 간주하지 않는다.

### 5.10 `RM-9` — 별도 승인된 후속 작업

`RM-9`는 자동 착수 phase가 아니다. §11의 각 deferred 항목이 자체 resume criteria와 scope 승인을 통과한 경우에만 독립 roadmap을 만든다. concrete infrastructure topology는 이 lane에서도 가장 마지막에 다룬다.

## 6. Milestone과 critical path

### 6.1 Milestone

| Milestone | phase exit | 의미 | 필수 evidence |
|---|---|---|---|
| `MS-DOC` | `RM-0` | Master와 질문·추적성의 검토 기준이 준비됨 | `C-*` coverage, `Q-*` registry, 문서 상태와 lint |
| `MS-SEMANTIC` | `RM-1` | 승인된 의미로 immutable normalized problem을 만들 수 있음 | normalization examples, invariant/overflow/matrix evidence |
| `MS-EVAL` | `RM-2` | 고객 정책을 core branch 없이 bind하고 비교할 수 있음 | profile isolation, full evaluation, comparator/guard evidence |
| `MS-PORTFOLIO` | `RM-3` | 네 initial policy가 공통 evaluator로 verified candidate set을 만듦 | deterministic traces, dedup/best/diversity evidence |
| `MS-SEARCH` | `RM-4` | COW ALNS가 step-normal, rollback-safe, reproducible run을 만듦 | interruption, cache, stage, deterministic rerun evidence |
| `MS-VERIFIED-RESULT` | `RM-5` | search state에서 독립 검증된 완전한 결과를 만들 수 있음 | independent verifier, partition, publication rejection evidence |
| `MS-WIN-BASELINE` | `RM-6` | Win PoC의 비교 가능한 최초 공식 기준이 존재 | manifest, official verified cards, actual champion, rerun |
| `MS-PERF-DECISION` | `RM-7` | COW 유지 또는 apply/undo 전환이 측정과 동등성으로 결정됨 | profile, equivalence, decision record |
| `MS-COMPATIBILITY` | `RM-8` | logical ports를 통한 migration 가능성이 증명됨 | compatibility/shadow/end-to-end/rollback evidence |

### 6.2 Critical path

현재 목표인 verified Win PoC baseline까지의 임계 경로는 다음이다.

```text
MS-DOC
→ 필요한 numeric/matrix/time/input 의미 승인
→ MS-SEMANTIC
→ MS-EVAL
→ MS-PORTFOLIO
→ MS-SEARCH
→ MS-VERIFIED-RESULT
→ benchmark formula/budget 승인
→ MS-WIN-BASELINE
```

다음은 임계 경로 뒤의 측정 lane이다.

```text
MS-WIN-BASELINE
→ MS-PERF-DECISION
→ MS-COMPATIBILITY
```

route pool/MIP, optional variants, provider topology와 학술 benchmark 확장은 이 임계 경로에 넣지 않는다.

### 6.3 승인 전 진행 가능한 것과 진행할 수 없는 것

| 작업 | contract double/example로 진행 가능 | 의미 승인 전 공식 완료 불가 |
|---|---|---|
| numeric normalization | policy interface, checked arithmetic, overflow/missing-policy rejection | 실제 차원별 scale·rounding과 수요/용량 공식값 |
| time | monotonic internal axis, explicit boundary policy seam, hand-authored windows | 실제 timezone/date-time, close boundary, start/complete, overnight 의미 |
| matrix | physical-location mapping, directed lookup, missing-arc rejection, asymmetric example | legacy field mapping/unit/diagonal과 official fixture travel |
| input adapter | versioning, provenance, unsupported-field rejection, synthetic canonical DTO | ambiguous legacy aliases·resource reset·rotation 의미 |
| compatibility | explicit registry와 precomputed `servableVehicles` example | null/empty/`ALL`, size/zone 합성의 실제 legacy 처리 |
| domain invariant | request pair, terminal, route/bank XOR, atomic mutation property | mixed service route와 multi-trip 의미 |
| evaluation/profile | fake neutral facts, contributor/score/comparator binding | 활성 고객 profile의 unresolved mandatory/fallback 의미 |
| portfolio/ALNS | explicit test configuration, synthetic normalized fixture, stable seeds | official defaults, Win PoC run configuration와 baseline |
| result/verifier | synthetic verified/invalid candidates, conservative statuses | `DEFERRED`/`OUTSOURCED` activation과 exhaustive audit policy |
| logical ports | fake adapters, state-machine examples, idempotency/cancellation contract | physical topology, product policy와 provider-specific operation |

## 7. 결정 차단 질문과 non-blocking work

### 7.1 분류 원칙

질문은 다음 세 범주로 관리한다.

| 분류 | 의미 |
|---|---|
| 현재 기준선 임계 경로 | 답이 없으면 실제 input 의미, verified solution 또는 공식 Win baseline을 만들 수 없음 |
| 기능 활성화 한정 | core 계약과 다른 기능은 진행할 수 있으나 해당 profile/status/optimization을 활성화할 수 없음 |
| deferred 전용 | 현재 portfolio→ALNS→verifier→Win baseline 경로를 차단하지 않음 |

### 7.2 중앙 질문별 earliest gate

| ID | 분류·가장 이른 decision gate | owner 경계와 필요한 evidence | 해결 전 안전한 동작 | downstream impact |
|---|---|---|---|---|
| `Q-NUM-01` | 임계 경로; `RM-1` 실제 normalization | Domain/Input owner; 차원별 외부 계약, 경계값·표시·overflow 분석 | explicit policy 없이는 canonical normalize 금지; test-only policy만 명시 주입 | capacity/time/distance/cost, verifier, baseline |
| `Q-NUM-02` | 임계 경로; `RM-1` 실제 normalization | Domain/Input owner; 초과 자릿수 사례와 업무 승인 | 임의 floor/round 금지 | feasibility 경계, 재현성, benchmark |
| `Q-NUM-03` | 임계 경로; `RM-1` request demand | Domain/Input owner; item/line 계산 예와 실제 계약 | 합산 순서 없는 legacy demand 확정 금지 | capacity proof, result, baseline |
| `Q-MTX-01` | 임계 경로; `RM-1` legacy matrix binding | Input/Matrix owner; field spec, 단위·정밀도와 sample reconciliation | 값 크기로 의미·단위 추정 금지 | travel, route resource, Win metrics |
| `Q-MTX-02` | 임계 경로; `RM-1` fixture adapter | Input/Matrix owner; source contract와 diagonal/category evidence | diagonal을 0 또는 정상 arc로 임의 교정 금지 | fixture feasibility, distance/time 합계 |
| `Q-MTX-03` | 기능 활성화 한정; `RM-1` canonical production schema | Input/Matrix owner; coverage requirement와 storage/validation evidence | selected contract의 complete coverage 요구; missing arc fallback 금지 | validation, representation, verifier |
| `Q-TIME-01` | 임계 경로; `RM-1` planning normalization | Domain/Input owner; timezone/date-time source, DST examples | zone 없는 시간을 임의 zone과 결합 금지 | fingerprint, all time feasibility |
| `Q-TIME-02` | 임계 경로; `RM-1` boundary comparison | Domain owner; plan-end/window-close boundary cases | 승인 전 실제 boundary result 발행 금지 | propagator, verifier, result |
| `Q-TIME-03` | 임계 경로; `RM-1` service-window normalization | Product/Domain owner; start-vs-completion examples와 profile need | 전역 start-only/completion default 금지 | feasibility, customer profiles |
| `Q-TIME-04` | 기능 활성화 한정; `RM-1` advanced time semantics | Product/Domain owner; overnight/repeat/work-end cases | 반복·pause/resume 의미를 활성화하지 않음 | propagator, route resource |
| `Q-IN-01` | 임계 경로; `RM-1` legacy order mapping | Input-contract owner; field producer definition과 representative records | 이름만으로 alias/release/deadline 추론 금지 | request windows, service duration |
| `Q-IN-02` | 임계 경로 또는 기능 한정; `RM-1` fixture resource binding | Input/Product owner; depot/rotation/wait/limit scope와 reset examples | standard single-trip; ambiguous resource field는 unsupported로 거부 | delivery-only, route resource, benchmark |
| `Q-COMP-01` | 임계 경로; `RM-1` legacy compatibility | Input/Product owner; code registry와 null/unknown cases | explicit known code만 허용; wildcard/unknown 추정 금지 | `servableVehicles`, diagnostics |
| `Q-COMP-02` | 임계 경로; `RM-1` compatibility compilation | Product/Domain owner; size/zone provenance와 pickup/delivery cases | independent rule 승인 전 자동 AND/교집합 금지 | eligible fleet, unassignment |
| `Q-REQ-01` | 기능 활성화 한정; `RM-1` mixed service binding | Domain/Product owner; mixed-route load/service examples와 invariant proof | mixed canonical route 생성 금지 | evaluator, operators, verifier |
| `Q-REQ-02` | 기능 활성화 한정 또는 deferred; `RM-1` multi-trip | Domain/Product owner; trip boundary, load reset, terminal/resource contract | 표준 single-trip 유지; pair가 trip 경계를 넘는 의미 금지 | route model, propagator, ALNS, result |
| `Q-OBJ-01` | 기능 활성화 한정; `RM-2` profile selection | Product/Policy owner; authorization/use cases와 exact preset lineage | exact bound profile 사용; request가 unknown preset 주입 금지 | submission, metadata, reproducibility |
| `Q-OBJ-02` | 기능 활성화 한정; `RM-2` mandatory profile | Product/Policy owner; hard/finite/lexicographic 업무 사례 | mandatory 의미를 활성화하지 않고 큰 penalty로 흉내 내지 않음 | feasibility, comparator, outcome |
| `Q-OBJ-03` | 기능 활성화 한정; `RM-2` fallback state | Product/Policy/Result owners; search-choice 대 result-classification 요구 | fallback search state 비활성; result에서 임의 disposition 금지 | solution partition, objective, result |
| `Q-ALG-01` | 기능 활성화 한정; `RM-3` official portfolio configuration | Algorithm/Benchmark owners; synthetic/real experiments와 reproducibility | explicit test config만 사용; official default 발행 금지 | candidate quality, official runs |
| `Q-ALG-02` | 기능 활성화 한정; `RM-7` state strategy switch | Algorithm/Performance owner; COW profile과 trace/verifier equivalence | COW 유지 | apply/undo adoption, performance |
| `Q-RES-01` | 기능 활성화 한정; `RM-5` disposition publication | Product/Result owner; 승인 행위·reference·audit trail | `ASSIGNED`/`UNASSIGNED`만 보수적으로 생성 | result schema, counts, objective if fallback |
| `Q-RES-02` | 기능 활성화 한정; `RM-5` exhaustive confidence | Result/Verification owner; audit cost와 completeness contract | audit 없는 request에 exhaustive confidence 금지 | diagnostics, finalization cost |
| `Q-BENCH-01` | 임계 경로; `RM-6` official vector | Benchmark/Product owner; “전체 시간” 업무 정의와 hand example | formula ID 없는 official baseline 금지 | fourth metric, comparability |
| `Q-BENCH-02` | 임계 경로; `RM-6` official run set/gates | Benchmark/Quality owner; experiment protocol과 variance evidence | non-official exploratory cards만 허용; pass/fail 발행 금지 | baseline, challenger, performance gate |
| `Q-BENCH-03` | 임계 경로; `RM-1` fixture adapter와 `RM-6` manifest | Input/Product owner; oneway/rotation producer contract와 route examples | terminal/depot revisit/rotation을 추정하지 않음 | fixture feasibility와 comparator |
| `Q-INFRA-01` | deferred 전용; `RM-9` topology 재개 | Application/Platform/Product owners; workload, security, retention, retry, cost evidence | §3 논리 port만 사용 | physical deployment and operations |
| `Q-VAR-01` | deferred 전용; `RM-9` variant study | Product/Domain/Algorithm owners; chosen variant definition, fixture, core-impact analysis | 현재 pair/terminal/bank invariant 유지 | variant roadmap and core changes |

### 7.3 질문과 무관하게 진행할 수 있는 non-blocking work

- Master 문서의 구조·용어·추적성 정리
- immutable domain shape와 pair/terminal/bank invariant example
- policy/provider/profile dependency validator와 fake fact source
- common pair evaluator interface와 synthetic directed-matrix example
- deterministic seed namespace, stable iteration과 exact step counter contract
- COW ownership, commit/discard와 interruption fault-injection harness 설계
- verifier independence와 corrupted-candidate suite 설계
- result partition, conservative status와 diagnostic confidence examples
- benchmark manifest/card schema의 논리 필드와 comparability algorithm
- submission/execution/status/result/cancellation logical port doubles
- deferred item의 resume checklist와 decision-record template

이 작업은 unresolved 의미를 test fixture 안의 명시적 값으로 제한해야 한다. test-only 값이 production default 또는 공식 benchmark 의미로 승격되어서는 안 된다.

## 8. Risk register

| 위험 | trigger / 조기 신호 | 영향 | mitigation·gate | owner 경계 |
|---|---|---|---|---|
| semantic drift | adapter, evaluator, verifier가 같은 legacy 필드를 서로 다르게 해석; `Q-*` 없이 default 등장 | 같은 입력에 다른 feasibility/result, baseline 무효 | `RM-0` 질문 추적; `RM-1` immutable provenance; verifier가 exact policy fingerprint 확인 | Document + Domain/Input |
| customer branching | customer ID, profile 이름 또는 업무 문자열이 ALNS/route state/common evaluator에 등장 | 코어 복제, 회귀 범위 폭증, cache 의미 혼합 | `RM-2` decision path; profile dependency closure; 새 profile이 core를 수정하지 않는 acceptance | Policy/Evaluation |
| pair atomicity/rollback | pickup/delivery 일부만 변경, bank와 route 동시 membership, interruption 후 fingerprint 변화 | 해 구조 손상, best 오염, 잘못된 결과 | `RM-1` mutation contract; `RM-3~4` fault injection; full partition gate | Domain + Algorithm |
| cache correctness | cache key에 profile/route/bank generation 누락; hit/miss에 따라 score 차이 | 잘못된 acceptance, non-reproducible best | cache-free equivalence at insertion/acceptance/best/final; 의심 cache 폐기 | Evaluation + Algorithm |
| reproducibility erosion | global random, unordered iteration, parallel first-winner, wall-clock 품질 종료 | rerun과 regression 비교 불가 | namespaced derived seeds, stable total order, exact step commit, `MAX_STEPS_REACHED` gate | Algorithm + Benchmark |
| verifier independence loss | verifier가 solver cache/summary/evaluator flag를 재사용 | 같은 결함을 이중 승인, invalid publication | 별도 cache-free path, corrupted cache/result fault injection, exact verifier version | Result/Verification |
| benchmark comparability | fixture·adapter·formula·seed·step·build fingerprint가 다른 card 비교 | 가짜 개선·회귀, baseline 신뢰 상실 | immutable manifest, `NOT COMPARABLE`, actual-run champion, manual baseline approval | Benchmark/Quality |
| premature optimization | baseline 전 undo/slack/cache 복잡화, 측정 없는 threshold | rollback bug, 개발 지연, 의미 추적 상실 | COW first; `RM-6` baseline 후 `RM-7` profiling; no-switch 허용 | Algorithm/Performance |
| infrastructure coupling | core에 transport/storage/runtime 타입이나 provider timeout이 침투 | portability 상실, topology 변경이 solver 변경으로 전파 | §3 logical ports, dependency check, topology `DEFERRED`, physical decision last | Application/Platform |
| numeric overflow/rounding mismatch | unchecked arithmetic, solver/verifier policy version 불일치 | 경계 feasibility 오류, silent wrap, benchmark 불일치 | checked arithmetic, missing-policy rejection, exact policy fingerprint | Domain/Input + Verification |
| authoritative matrix violation | reverse arc, coordinate fallback, diagonal 임의 보정 | 거리·시간·feasibility 왜곡 | `RM-1` no-fallback validation, asymmetric fixture, `RM-5` independent arc replay | Input/Matrix + Verification |
| result overclaim | bank membership이나 마지막 failure로 proven diagnostic/status 생성 | 사용자 오판, partition 불일치 | conservative finalization, confidence/source gate, `Q-RES-*` 유지 | Result/Product |
| migration false equivalence | 기존 payload/상태/metric 이름이 같다는 이유로 의미도 같다고 간주 | silent compatibility break | versioned adapter, semantic compatibility matrix, shadow comparison, verified cutover | Application + Domain + Result |
| document/implementation drift | phase가 evidence 없이 `done`, 잠정 이름이 API로 고정, 진행 기록이 설계를 덮음 | 서로 다른 source of truth | versioned Master/Decision Record, evidence-linked progress, phase exit audit | Document owner + 각 계약 owner |

위험 owner는 조직명이나 팀 규모를 뜻하지 않는다. 해당 의미를 승인하고 evidence를 보존해야 하는 책임 경계다.

## 9. Traceability

### 9.1 `C-*` → phase → acceptance evidence → 최종 Master

최종 Master 절 이름은 세션 19의 목표 목차를 사용한다. 세션 25에서 장 번호가 조정되어도 의미 소유권은 유지해야 한다.

| 결정 | 주요 phase | acceptance evidence | 최종 Master 절 |
|---|---|---|---|
| `C-01` RPDPTW 표준 용어 | `RM-0`, `RM-1` | 용어 lint, 학술 `PDPTW` 구분 | §2 목표·범위, §3 용어·결정 |
| `C-02` normative future design | `RM-0`, 전 phase | 문서 상태, no-completion-claim audit | §1 문서 상태, §15 roadmap, §16 위험·migration |
| `C-03` 최소 코어 변경 customer extensibility | `RM-2`, `RM-8` | new-profile change impact와 기존 profile regression | §2 성공 기준, §9 변화 수용 |
| `C-04` feasibility/measurement/cost/objective/stage 분리 | `RM-2` | layer isolation, bind-time dependency, full evaluation | §9 변화 수용 |
| `C-05` Feature=vehicle size type | `RM-1`, `RM-2`, `RM-5` | size membership/capability subset cases와 verifier | §3 용어, §7 입력, §9 변화 수용 |
| `C-06` atomic pickup-delivery request | `RM-1`, `RM-3~5` | pair property, rollback, independent partition verification | §5 모델, §6 invariant, §11 알고리즘, §14 검증 |
| `C-07` delivery-only와 real pickup 구분 | `RM-1`, `RM-3`, `RM-5` | start-loading example, no-mid-route-reload, result mapping | §5 모델, §6 invariant, §7 입력 |
| `C-08` step normal/watchdog safety | `RM-4~6` | exact completed-step, interruption, termination/card evidence | §13 종료·결정성, §14 benchmark, §15 roadmap |
| `C-09` COW first, later apply/undo | `RM-4`, `RM-7` | COW isolation; optional trace/undo equivalence | §12 candidate/cache/rollback, §15 roadmap |
| `C-10` fixed-point normalization | `RM-1`, `RM-5` | boundary/overflow, solver-verifier exact equality | §7 입력·정규화, §14 검증 |
| `C-11` precision/rounding unresolved | `RM-0`, `RM-1`, `RM-6` | `Q-NUM-*` gate와 no-hidden-default audit | §7 입력, §17 질문·추적 |
| `C-12` 999 CBM, plan end, explicit infeasible | `RM-1`, `RM-5` | provenance, finite-boundary, sentinel rejection | §7 입력, §10 result, §14 verifier |
| `C-13` authoritative directed matrix | `RM-1`, `RM-3~6` | asymmetric/missing-arc test와 verifier replay | §8 distance/time, §14 verifier |
| `C-14` legacy PDF는 비규범 evidence | `RM-0`, `RM-1`, `RM-8` | versioned adapter와 source-status audit | §7 입력, §16 migration |
| `C-15` search bank와 final result 분리 | `RM-1`, `RM-5` | membership-only test, outcome partition, no-write-back | §10 해·결과, §14 verifier |
| `C-16` initial portfolio current scope | `RM-3` | four-policy traces, common evaluator, best/diverse evidence | §11 알고리즘, §15 roadmap |
| `C-17` route pool/MIP deferred | `RM-0`, `RM-9` | no-current-scope dependency audit, resume checklist | §11 경계, §15 roadmap, §16 비범위 |
| `C-18` Win PoC primary comparator | `RM-6` | exact four-component projector/comparator와 actual champion | §14 benchmark, §15 roadmap |
| `C-19` optional variants deferred study | `RM-9` | no-invariant-relaxation audit, feasibility resume record | §16 비범위·deferred |
| `C-20` concrete topology deferred | `RM-0`, `RM-8~9` | logical-port-only review, provider-neutrality audit | §4 시스템 경계, §15 roadmap, §16 비범위 |
| `C-21` independent verifier required | `RM-5`, `RM-6` | corrupted cache/result detection, publication rejection | §14 verifier·benchmark |
| `C-22` scoped strong reproducibility | `RM-4`, `RM-6` | fixed fingerprint/seed/step normal rerun | §13 종료·metadata, §14 benchmark |

### 9.2 세션 20~23 contract chain

| source draft | roadmap 소비 phase | 소비 계약 | phase acceptance evidence | 최종 Master 절 |
|---|---|---|---|---|
| 세션 20 domain/input | `RM-1`, 이어서 `RM-2~6` | normalized identities/units/time/matrix, compatibility, pair/bank invariants, explicit infeasible | normalization/invariant/matrix/provenance report와 verifier replay | §5~§8, §10 |
| 세션 21 policy/objective | `RM-2`, 이어서 `RM-3~6` | propagator facts, hard constraints, neutral metrics, score, comparator, `SolvePlan`, profile binding | isolation/binding/full-evaluation/comparator/guard report | §9, §13 |
| 세션 22 algorithm | `RM-3`, `RM-4`, `RM-7` | portfolio, common pair evaluator, ALNS, step/watchdog, COW/cache/rollback, apply/undo gate | deterministic trace, rollback/cache equivalence, profiling decision | §11~§13, §15 |
| 세션 23 result/benchmark | `RM-5`, `RM-6` | result partition, diagnostics, independent verifier, manifest/card/comparator | publication rejection, result integrity, official verified run cards | §10, §13~§14 |

다음 역방향은 허용하지 않는다.

```text
benchmark formula → customer objective 기본값
result diagnostic → SearchRequestBank state
ALNS optimization → domain meaning
profile cost → hard feasibility
legacy input shape → canonical contract default
```

## 10. Migration, compatibility와 document governance

### 10.1 현재와 목표의 지위

현재 `docs/master-design.md`와 저장소의 코드·실행 골격은 **replacement inventory와 migration evidence candidate**다. 다음을 뜻하지 않는다.

- 현재 code가 세션 20~23 계약을 준수한다.
- 기존 이름이 새 logical responsibility와 일치한다.
- 기존 result, timeout, input 또는 matrix 의미가 호환된다.
- 기존 provider-specific 실행 형태가 목표 topology다.
- placeholder 또는 골격이 실제 RPDPTW solver다.

호환성은 파일·클래스 존재가 아니라 contract test와 independent evidence로 판단한다.

### 10.2 단계적 migration path

1. **문서 baseline**
   - 세션 25가 Master를 `REVIEW`로 통합한다.
   - `C-*`, `P-*`, `Q-*`, source와 phase gate를 안정된 링크로 남긴다.
   - 기존 Master의 구 약어, 숨은 입력 default, single greedy, mixed bank/result, 동등 wall-time budget과 provider-specific 목표 topology를 규범에서 제거한다.

2. **Contract characterization**
   - 향후 구현 전에 현재 입력·출력·상태·재시도·오류 의미를 read-only로 관찰한다.
   - 관찰 사실과 목표 계약을 compatibility matrix로 비교한다.
   - 관찰되지 않거나 test가 없는 항목을 “호환”으로 표시하지 않는다.

3. **새 core를 logical ports 뒤에서 격리**
   - normalized domain, evaluation, portfolio/ALNS와 verifier를 external adapter와 분리해 만든다.
   - synthetic examples와 contract doubles로 port와 core를 독립 검증한다.
   - 현재 실행 경로의 mutable state나 result DTO를 core domain으로 유입하지 않는다.

4. **Versioned adapter와 provenance**
   - legacy input은 승인된 coercion·alias·field meaning만 versioned adapter에서 처리한다.
   - exact adapter, profile, numeric/time/matrix policy를 결과 lineage에 남긴다.
   - 모호한 입력은 silent fallback 대신 명시적으로 거부한다.

5. **Shadow/parallel comparison**
   - 같은 logical input identity에서 기존 경로의 관찰 결과와 새 verified 결과를 side effect 없는 비교 대상으로 수집할 수 있다.
   - 두 결과의 이름이 아니라 request partition, route meaning, metric formula와 termination semantics를 비교한다.
   - 기존 결과를 정답으로 간주하지 않고, 새 결과도 independent verifier 없이 우월하다고 간주하지 않는다.

6. **Verified publication gate**
   - 새 결과는 `RM-5` verifier와 result integrity를 통과한 경우에만 publication candidate가 된다.
   - Win PoC 비교는 `RM-6` manifest가 같을 때만 수행한다.
   - exceptional/recovery 결과를 정상 완료로 표시하지 않는다.

7. **Controlled cutover와 rollback**
   - logical submission/status/result contract의 version 전환과 backward compatibility 범위를 명시한다.
   - retry/idempotency/cancellation과 artifact lineage를 검증한다.
   - cutover failure 시 마지막 verified 계약 경로로 되돌릴 수 있는 evidence를 준비한다.
   - 물리 deployment 방법은 `Q-INFRA-01` 재개 전 결정하지 않는다.

8. **진행 evidence와 문서 동기화**
   - phase 상태는 실제 artifact와 test/benchmark/verifier report에 연결한다.
   - 구현에서 승인된 설계를 바꿔야 하면 Decision Record와 관련 Master/상세 문서를 같은 변경 단위에서 갱신한다.
   - 진행 기록은 설계를 대체하지 않는다.

### 10.3 Compatibility matrix의 최소 축

| 축 | 확인할 의미 | 호환으로 인정할 evidence |
|---|---|---|
| input identity | 같은 external request가 같은 normalized request/location/vehicle을 가리키는가 | versioned mapping과 round-trip report |
| numeric/time/matrix | 단위·rounding·boundary·directed arc가 같은가 | exact policy fingerprint와 boundary cases |
| assignment | request 단위 route/bank/final outcome partition이 같은 의미인가 | independent verifier와 outcome report |
| metric/objective | 같은 이름이 같은 formula/unit/order를 뜻하는가 | schema/formula/comparator version |
| termination | 정상 step 종료와 watchdog/cancel/failure가 구분되는가 | run record와 injected termination tests |
| result integrity | route, outcome, summary와 fingerprint가 같은 candidate를 가리키는가 | finalization integrity report |
| retry/idempotency | 같은 logical run의 재시도가 중복·혼합 result를 만들지 않는가 | repeat submission/execution evidence |
| cancellation | 부분 candidate가 결과로 노출되지 않는가 | rollback과 final state evidence |

### 10.4 Document governance

- Master는 미래 개발 설계이며 구현 progress report가 아니다 (`C-02`).
- `REVIEW` 상태에서는 열린 질문을 보존하며 `APPROVED`처럼 인용하지 않는다.
- 새 결정이 핵심 invariant, input/output, objective, numeric/time, phase gate 또는 logical responsibility를 바꾸면 Decision Record가 필요하다.
- 중앙 질문은 세션 19의 exact ID를 유지한다. 중복 ID를 만들지 않는다.
- 설명용 논리 라벨을 exact Java/API 이름으로 승격하지 않는다.
- historical provider-specific 자료는 근거·이력으로 링크할 수 있지만 목표 topology로 복원하지 않는다.
- 실제 code/test 파일은 존재와 통과가 확인된 뒤에만 구현 evidence로 연결한다.

## 11. Explicit deferred backlog와 resume criteria

| deferred 항목 | 현재 보존할 경계 | resume criteria | 재개 후 첫 산출물 | 재개 전 금지 |
|---|---|---|---|---|
| route pool/MIP (`C-17`) | verified ordered route, source policy/run, metrics와 solution fingerprint를 export할 수 있는 논리 경계 | `MS-WIN-BASELINE`; independent verifier; reproducible portfolio/ALNS; route collection이 줄 수 있는 가치의 측정; 별도 scope/solver/licensing/fallback 승인 | column meaning, pool admission/pruning, model, reconstruction, ALNS fallback과 verification 설계 | MIP dependency/type를 core에 선반영, current phase 완료 조건에 포함 |
| optional variants (`C-19`, `Q-VAR-01`) | 현재 request atomicity, fixed terminal, search bank와 directed matrix contract | 선택할 변형과 제한 범위 승인; 대표 fixture와 hand result; session 17의 transform/extension/core-change 판정; input/result/verifier/core regression 분석 | feasibility study와 `GO_TRANSFORM`/`GO_EXTENSION`/`GO_CORE_CHANGE`/`DEFER`/`NO_GO` 결정 | variant 구현, split delivery를 위해 current invariant 완화 |
| concrete infrastructure/provider/product topology (`C-20`, `Q-INFRA-01`) | §3 logical ports, idempotency/status/artifact/cancellation 책임 | `MS-VERIFIED-RESULT`와 workload evidence; security/access/retention/audit requirements; retry/cancellation/recovery policy; performance/cost constraints; 별도 승인 | topology options/decision record, port mapping, failure and migration plan | 특정 service, product, runtime, deployment unit을 Master 목표로 고정 |
| academic benchmark expansion | Win PoC manifest/card와 independent verifier 재사용 가능 경계 | `MS-WIN-BASELINE`; 추가 benchmark의 목적; authoritative format/known result source; RPDPTW mapping과 objective comparability; separate adapter/manifest; 실행 비용 승인 | benchmark-specific interpretation, manifest, expected-result provenance와 comparability policy | Win PoC baseline을 대체, 다른 metric/gap을 같은 card에 혼합 |
| multi-trip/rotation (`Q-REQ-02`, 필요 시 `Q-IN-02`) | standard single-trip semantics | exact trip/reset/depot/pair-boundary/resource contract; representative examples; domain/operator/verifier impact 승인 | 별도 domain/algorithm/result decision | legacy option 값만 보고 활성화 |
| 다른 research algorithm 또는 exact method | 현재 verified ALNS best와 result/verifier fallback | Win baseline, 명확한 목표와 measured gap, invariant/result compatibility, 별도 scope | algorithm comparison design과 fallback contract | current ALNS에 묵시적 variant로 혼합 |
| real-time/dynamic routing | immutable solve snapshot과 logical cancellation port | event/replanning semantics, state continuity, SLA와 conflict policy 승인 | dynamic problem state와 replan contract | 현재 batch problem 의미를 조용히 변경 |

route pool/MIP, optional variants와 academic expansion은 서로 묶어 한 번에 승인하지 않는다. 각각 독립적인 의미·위험·검증 gate를 가져야 한다.

concrete infrastructure detail은 위 backlog 중에서도 마지막이다. core contract를 physical topology에 맞추어 재설계하는 것이 아니라, 안정된 logical ports를 workload evidence에 맞는 topology로 mapping해야 한다.

## 12. 세션 25 integration handoff

### 12.1 Master에 병합할 내용

세션 25는 이 문서에서 다음을 가져가야 한다.

- §1~2: Master가 normative future development design이라는 상태와 roadmap 원칙
- §3: submit/input, normalization/binding, solve execution, finalization, status/artifact, retrieval, cancellation logical ports
- §4~6: `RM-0`부터 `RM-9`까지의 dependency와 phase gate, milestone, critical path
- §7: exact session-19 `Q-*`의 earliest gate, owner/evidence, safe behavior와 downstream impact
- §8: 위험 register
- §9: `C-*`와 세션 20~23 traceability
- §10: existing-code non-compliance presumption 없는 migration·compatibility·governance
- §11: deferred backlog와 resume criteria

### 12.2 최종 Master 절 배치

| 이 문서 내용 | 세션 19 목표 Master 절 |
|---|---|
| 문서 역할·normative principle | §1 문서 상태와 규범, §2 목표·범위 |
| logical ports와 dependency direction | §4 논리 시스템 context |
| phase sequence, milestone, critical path | §15 구현 roadmap |
| risk, migration, compatibility | §16 위험·마이그레이션·비범위 |
| open-question gate와 traceability | §17 열린 질문과 추적성 |
| deferred backlog | §11 algorithm 경계, §15 roadmap, §16 비범위 |

### 12.3 통합 시 유지해야 할 금지선

세션 25는 다음을 추가해서는 안 된다.

- provider/product/deployment topology 또는 fixed physical module
- exact API/JSON schema
- 일정, 기간, 인원 또는 수치 threshold
- 질문에 없는 default, precision, rounding, time boundary, matrix unit
- `Q-BENCH-01` 이전의 `전체 시간` formula
- `Q-BENCH-02` 이전의 official seeds/steps/watchdog/regression gate
- 기존 code/test/fixture가 새 contract를 통과한다는 주장
- route pool/MIP 또는 variants를 current implementation phase로 승격

### 12.4 세션 25 acceptance checklist

- 세션 20→21→22→23의 dependency가 Master roadmap에서 역전되지 않는다.
- initial solution portfolio는 current scope다.
- route pool/MIP, variants와 topology는 deferred다.
- COW는 initial safe path이고 apply/undo는 measured decision gate다.
- step 정상 종료, watchdog 안전, strong reproducibility envelope이 명시된다.
- independent verifier가 result publication과 Win comparison 앞에 있다.
- Win PoC comparator의 네 성분과 순서가 정확하다.
- unresolved input/benchmark 의미는 exact `Q-*`에 연결된다.
- logical ports는 provider/product neutral하다.
- implementation completion status는 별도 evidence 없이는 기록되지 않는다.

## 13. Scope self-audit

| 검사 | 결과 |
|---|---|
| 수정 대상 | `docs/master-design-sessions/24-roadmap-draft.md` 한 파일만 생성 |
| 문서 성격 | 미래 개발 roadmap이며 구현 완료·현재 compliance 주장 없음 |
| dependency | 세션 20 normalized domain → 21 evaluation/profile → 22 portfolio/ALNS → 23 result/verifier/benchmark 순서 유지 |
| roadmap 범위 | documentation/decision → domain/input → evaluation → portfolio → ALNS/COW → verifier/result → Win baseline → measured apply/undo → migration/follow-up 포함 |
| phase 계약 | 모든 phase에 entry, design input, future deliverable, acceptance gate, exit evidence, non-goal 포함 |
| 질문 | 세션 19의 exact 28개 `Q-*`만 사용; earliest gate, owner/evidence, safe behavior, impact 포함 |
| critical path | numeric/matrix/time/input 의미 승인 전 차단 항목과 contract double 병행 작업 분리 |
| 위험 | semantic drift, customer branching, atomicity/rollback, cache, reproducibility, verifier independence, benchmark comparability, premature optimization, infrastructure coupling 포함 |
| 추적성 | `C-01`~`C-22`, 세션 20~23, phase/evidence와 final Master 절 연결 |
| deferred | route pool/MIP, optional variants, concrete topology, academic benchmark expansion의 resume criteria 포함 |
| migration | 기존 code를 compliant로 선언하지 않고 characterization→adapter→shadow→verified cutover 경로 정의 |
| logical system | submission/input, execution, finalization, status/artifact, retrieval, cancellation port만 정의 |
| topology | 특정 provider, cloud service, product, runtime, deployment unit 결정 없음 |
| 발명 금지 | 일정·날짜·팀 규모·numeric threshold·exact API schema·새 domain/algorithm 결정 없음 |
| 비변경 | source, build, test, fixture, PDF, data, deployment와 기존 문서 미수정 |

이 초안은 세션 25가 Master의 논리 시스템 context, 구현 roadmap, phase gate, 위험, migration, deferred backlog와 질문 추적성을 통합할 때 사용하는 규범적 입력이다.
