---
title: RPDPTW 통합 솔버 Master Design (superseded draft)
status: SUPERSEDED
version: 4.0-review
last_updated: 2026-07-26
owner: RPDPTW 설계 책임 역할
scope: 전체 목표, 완료 정의, 핵심 결정, 구현 흐름, ALNS-MIP 적용 개요, roadmap gate와 검증 evidence
superseded_by: master-design.md
related_documents:
  - 2026-07-26-domain-design.md
  - 2026-07-26-architecture-design.md
  - master-design-open-questions.md
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
---

# RPDPTW 통합 솔버 Master Design

> **SUPERSEDED DRAFT — canonical authority:** 이 문서는 2026-07-26의 중복 통합 초안이다. 현재 목표·질문 상태·AWS runtime 결정은 [Master Design](master-design.md)과 [질문 등록부](master-design-open-questions.md)가 소유한다. 이 문서를 독립적으로 갱신하거나 현재 결정의 authority로 사용하지 않는다.

## 목차

- [1. 이 문서를 읽기 전에](#before-reading)
  - [1.1 독자와 이 문서가 답하는 질문](#audience)
  - [1.2 CVRPTW에서 pickup & delivery로 넘어갈 때 달라지는 것](#cvrptw-to-pd)
  - [1.3 current placeholder와 target 설계를 구분하는 법](#current-vs-target)
  - [1.4 권장 읽기 순서](#reading-order)
  - [1.5 문서 경계와 권위](#document-authority)
- [2. 목표 시스템과 완료 정의](#goal-and-done)
  - [2.1 한 문장 목표](#one-sentence-goal)
  - [2.2 범위와 비범위](#scope)
  - [2.3 완료의 AND gate](#definition-of-done)
- [3. 공통 언어와 불변조건](#language-and-invariants)
  - [3.1 처음 나오는 핵심 용어](#core-terms)
  - [3.2 request partition](#request-partition)
  - [3.3 route와 atomic mutation](#route-and-mutation)
  - [3.4 계산과 정책의 권위 경계](#authority-boundaries)
- [4. 핵심 결정 등록부](#decision-register)
  - [4.1 확정 결정 C-01~C-22](#confirmed-decisions)
  - [4.2 잠정 설계 P-01~P-20](#provisional-designs)
- [5. 입력에서 publication까지의 구현 흐름](#implementation-flow)
  - [5.1 end-to-end 흐름](#end-to-end)
  - [5.2 단계별 책임과 산출물](#stage-contracts)
  - [5.3 의존 방향과 생명주기](#dependency-and-lifecycle)
- [6. 도메인·입력·평가·결과의 Master 계약](#master-contracts)
  - [6.1 canonical domain](#canonical-domain)
  - [6.2 입력·정규화·travel authority](#input-and-travel)
  - [6.3 profile과 evaluation 분리](#profile-and-evaluation)
  - [6.4 search state와 final result 분리](#search-and-result)
- [7. ALNS 기준 구현](#alns-baseline)
  - [7.1 8개 initial portfolio](#initial-portfolio)
  - [7.2 two-phase 실행](#two-phase-alns)
  - [7.3 completed step과 operator 경계](#alns-step)
  - [7.4 COW, cache, 종료와 재현성](#state-and-reproducibility)
- [8. ALNS–MIP 적용 개요와 C-17 gate](#alns-mip)
  - [8.1 왜 route pool과 MIP를 쓰는가](#why-mip)
  - [8.2 evaluated route artifact에서 projected column까지](#artifact-to-column)
  - [8.3 exact partition 원칙](#exact-partition)
  - [8.4 HybridPhase와 채택 규칙](#hybrid-phase)
  - [8.5 fallback과 활성화 금지선](#fallback)
- [9. 독립 검증, publication과 benchmark evidence](#verification)
  - [9.1 두 verifier gate](#two-verifiers)
  - [9.2 Win PoC 비교 계약](#win-poc)
  - [9.3 evidence bundle](#evidence-bundle)
- [10. Implementation roadmap와 phase gate](#roadmap)
  - [10.1 critical path](#critical-path)
  - [10.2 RM-0~RM-8](#rm-0-to-8)
  - [10.3 gated RM-9A~RM-9C](#rm-9)
- [11. Migration, 위험과 deferred work](#migration-and-risks)
  - [11.1 migration 원칙](#migration)
  - [11.2 주요 위험과 방어 gate](#risks)
  - [11.3 deferred 재개 조건](#deferred)
- [12. 질문, 근거와 traceability](#traceability)
  - [12.1 open question 상태](#open-questions)
  - [12.2 세션·연구 링크](#source-links)
  - [12.3 requirement-to-evidence map](#requirement-map)

<a id="before-reading"></a>

## 1. 이 문서를 읽기 전에

<a id="audience"></a>

### 1.1 독자와 이 문서가 답하는 질문

이 문서는 Java를 주 언어로 사용하고 CVRPTW를 구현해 본 개발자가, ro-next와 pickup & delivery 배경지식 없이도 목표 솔버의 전체 그림과 구현 순서를 잡도록 돕는 상위 설계서다.

다 읽은 뒤에는 다음을 설명할 수 있어야 한다.

1. CVRPTW의 단일 고객 방문이 왜 RPDPTW에서는 원자적인 `pickup/delivery pair`가 되는가?
2. 입력이 어떤 권위 경계를 거쳐 immutable problem, search candidate, 검증된 결과로 바뀌는가?
3. ALNS가 무엇을 변경하며 route pool/MIP가 어느 지점에서 선택적으로 개입하는가?
4. 어떤 gate와 evidence가 있어야 “구현 완료”, “게시 가능”, “official benchmark 가능”이라고 말할 수 있는가?
5. 무엇이 확정 결정이고 무엇이 잠정 또는 deferred인가?

이 문서는 구체 Java package, class, wire DTO와 optimizer adapter 배치를 열거하지 않는다. 그 구현 구조는 [2026-07-26 Architecture Design](2026-07-26-architecture-design.md)이 소유한다. 상세 도메인 수식, 전파 규칙, 입력 field와 acceptance 예시는 [2026-07-26 Domain Design](2026-07-26-domain-design.md)이 소유한다.

<a id="cvrptw-to-pd"></a>

### 1.2 CVRPTW에서 pickup & delivery로 넘어갈 때 달라지는 것

CVRPTW 경험을 기준으로 보면 가장 큰 변화는 “고객 하나를 route에 한 번 넣는다”가 더 이상 안전한 mutation 단위가 아니라는 점이다.

| CVRPTW에서 익숙한 관점 | RPDPTW에서 추가되는 의미 | 구현에 미치는 영향 |
|---|---|---|
| 고객 방문 하나가 수요 단위 | 하나의 `Request`가 pickup과 delivery 두 작업을 묶음 | 삽입·삭제·이동·rollback은 `pickup/delivery pair` 전체에 원자적으로 적용 |
| depot 적재 후 고객에서 감소 | 실제 pickup에서 적재가 증가하고 대응 delivery에서 감소할 수 있음 | route의 모든 prefix에서 load를 전파하고 precedence를 검사 |
| 고객 하나의 vehicle 배정 | pickup과 delivery가 같은 vehicle의 같은 route에 있어야 함 | partial pair와 cross-vehicle pair는 낮은 품질이 아니라 구조 결함 |
| 미배정 고객 집합 | 탐색 중 membership인 `SearchRequestBank`와 최종 `UNASSIGNED` 결과가 별개 | bank나 마지막 insertion 실패를 final reason으로 직렬화 금지 |
| route 비용만 비교 | 고객별 hard constraint, 중립 metric, score, lexicographic objective, stage plan이 분리 | ALNS core에 고객사 분기나 숨은 Big-M을 넣지 않음 |
| 하나의 해를 개선 | 여러 route에서 얻은 정확한 route를 재조합하는 optional MIP 가능 | route artifact, projection, reconstruction, full evaluation과 fallback gate 필요 |

RPDPTW는 여기서 **Rich Pickup and Delivery Problem with Time Windows**를 뜻한다. “Rich”는 차량 크기, capability, zone, terminal, ownership, 근무창, 고객별 목적 등 프로젝트가 다루는 확장 축을 가리킨다. 학술 문맥의 PDPTW와 구분해 프로젝트 표준 용어는 RPDPTW다.

<a id="current-vs-target"></a>

### 1.3 current placeholder와 target 설계를 구분하는 법

> **구현 상태 경고:** 2026-07-26 inventory의 current Java/GCP path는 입력 내용을 읽지 않고 합성 objective를 만드는 `AlnsBatchEngine` placeholder와 orchestration demo다. 이는 historical/legacy current-state이며 migration characterization 대상이다. 선택된 target/reference runtime은 **AWS S3 (object storage) + Step Functions (durable orchestration) + Lambda (API/coordinator/worker compute)**다. 이 선택은 목표 ALNS, route pool, MIP, 두 verifier와 official benchmark가 구현·검증되었다는 evidence가 아니다.

| 구분 | 실제 의미 | 이 문서에서의 취급 |
|---|---|---|
| Current placeholder | orchestration과 integration 형태를 보여 주는 demo | migration characterization 대상. ALNS 품질·feasibility 근거가 아님 |
| Target AWS runtime | S3 + Step Functions + Lambda | canonical Master/등록부가 소유하는 선택된 target/reference runtime; provider SDK는 adapter/deployment 경계에만 위치 |
| Target ALNS | immutable domain, atomic pair 연산, COW state, adaptive search, 독립 검증 | `RM-1`~`RM-8`에서 구현하고 evidence로 종료해야 할 목표 |
| Target route pool/MIP | exact route 수집과 solver-neutral route selection | `C-17` 아래의 **GATED TARGET**. `RM-9A`~`RM-9C`와 별도 승인 전 구현 착수·기본 활성화 금지 |

이후 “기본”은 current placeholder의 현재 동작이 아니라 승인될 목표 구현의 기본 계약을 뜻한다. 현재 코드를 설명할 때는 source/test/evidence inventory를 별도로 제시해야 한다.

<a id="reading-order"></a>

### 1.4 권장 읽기 순서

처음 읽는 개발자는 다음 순서를 권장한다.

1. §1~§3에서 CVRPTW와의 차이, 실제 구현 상태, 용어와 불변조건을 잡는다.
2. §5에서 입력부터 publication까지 데이터가 변하는 순서를 본다.
3. §6과 [dated Domain Design](2026-07-26-domain-design.md)으로 도메인·입력·평가 의미를 구체화한다.
4. §7에서 ALNS 기준 경로를 이해한 뒤 §8의 optional ALNS–MIP를 읽는다.
5. §9의 검증 gate를 통과하지 않은 결과는 사용할 수 없다는 점을 확인한다.
6. 실제 backlog와 phase 종료는 §10의 roadmap을 기준으로 삼는다.
7. Java module/package/SPI/runtime 배치는 [dated Architecture Design](2026-07-26-architecture-design.md)에서 찾는다.

<a id="document-authority"></a>

### 1.5 문서 경계와 권위

| 문서 | 소유하는 내용 |
|---|---|
| 이 Master | 전체 목표, 완료 정의, 핵심 결정, end-to-end 구현 흐름, ALNS–MIP 개요, roadmap/gate, 검증 evidence |
| [2026-07-26 Domain Design](2026-07-26-domain-design.md) | 도메인·입력·정규화·travel·전파·평가·결과의 상세 수식과 acceptance 사례 |
| [2026-07-26 Architecture Design](2026-07-26-architecture-design.md) | Java/Maven module, package DAG, ports/SPI, optimizer backend와 runtime 배치 |
| [질문 등록부](master-design-open-questions.md) | `Q-*` 상태, exact decision, evidence, owner와 gate의 단일 색인 |
| [세션 29](master-design-sessions/29-open-question-interview.md) | 사용자 답변과 해석 원문 |
| [세션 30](master-design-sessions/30-open-question-integration.md) | 질문 결정을 문서군에 반영한 기록 |
| [세션 31](master-design-sessions/31-domain-design-integration.md) | Domain Design 재구성과 정합화 기록 |

이 문서의 `status`는 `REVIEW`다. 승인된 기준과 충돌하면 이 문서 한 문장으로 기준을 대체할 수 없다. 승인 후 권위 순서는 외부 입·출력 계약과 승인 Decision Record → approved Master → approved detailed design → review/session 자료 → 연구 자료다.

규범어는 다음처럼 읽는다.

- **MUST / MUST NOT:** 구현과 검증이 지켜야 하는 계약.
- **SHOULD / SHOULD NOT:** 근거와 검토가 있을 때만 이탈할 수 있는 방향.
- **MAY:** 상위 계약을 깨지 않는 선택.
- **GATED TARGET:** 상세화할 수 있지만 명시된 gate와 승인 전 구현·활성화할 수 없는 목표.
- **DEFERRED:** 재개 조건 전에는 현재 roadmap을 막지 않는 보류.

<a id="goal-and-done"></a>

## 2. 목표 시스템과 완료 정의

<a id="one-sentence-goal"></a>

### 2.1 한 문장 목표

시스템은 versioned RPDPTW 입력을 immutable solve snapshot으로 정규화하고, 모든 `Request`의 `pickup/delivery pair`와 hard constraint를 보존하는 해를 ALNS로 생성·개선하며, 선택적으로 gated MIP 재조합을 적용한 뒤, 독립 검증된 `ASSIGNED`/`UNASSIGNED` partition과 provenance를 발행해야 한다.

고객사 변화는 constraint, metric, score, comparator와 `SolvePlan` profile 조합으로 격리한다. 새로운 단가나 목적 우선순위 때문에 공통 route state, propagation 또는 ALNS에 고객사 이름 분기를 추가해서는 안 된다.

<a id="scope"></a>

### 2.2 범위와 비범위

현재 목표 범위는 다음과 같다.

- Versioned adapter, immutable normalized problem과 complete prepared directed travel.
- `Request`, vehicle, terminal, physical location, time/load/resource를 사용하는 hard feasibility.
- 고객사별 immutable profile과 `SolvePlan`.
- 4개 request-route 성장 정책 × 2개 vehicle 순서의 최대 8개 initial candidate.
- Phase-1 screen, phase-2 병렬 ALNS, step 기반 정상 종료와 COW rollback.
- `SearchRequestBank`와 final outcome의 분리.
- Candidate solution verifier와 result-integrity verifier.
- Win PoC의 immutable manifest, complete worker fan-in과 verified champion.
- Provider/product에 독립적인 logical port와 migration gate.
- 별도 승인된 경우에만 route pool, exact partition MIP와 worker-local `HybridPhase`.

다음은 현재 비범위다.

- Route pool/MIP의 production 기본 활성화와 특정 optimizer·license 승인.
- 실험 없는 pool cap, MIP budget, worker/round 수, `maxSteps`, watchdog 수치.
- Multi-trip/rotation, dynamic routing과 선택 변형 문제.
- 특정 cloud/provider/product/deployment topology.
- 현재 소수 `D/U` fixture를 이용한 official baseline.
- 현재 placeholder를 target 구현 또는 benchmark evidence로 승격하는 일.

<a id="definition-of-done"></a>

### 2.3 완료의 AND gate

“class가 있다”, “API가 응답한다”, “한 fixture의 점수가 좋아졌다”는 완료가 아니다. 아래 아홉 조건은 모두 필요한 AND gate다.

| 완료 조건 | 필요한 보장 | 대표 exit evidence |
|---|---|---|
| 1. 구조적 정확성 | stable state마다 완전한 pair가 route 또는 bank 한 곳에만 존재하고 same-vehicle·precedence·load/time을 만족 | mutation/거절/예외/취소/rollback property와 fault test |
| 2. 입력 권위 고정 | input, travel, profile, config가 solve 전에 immutable version/fingerprint로 결합 | missing·ambiguous·cross-profile·travel mismatch 사전 거부 |
| 3. 고객 정책 격리 | 고객 차이가 profile에 있고 common core가 고객 이름을 모름 | 동일 problem에 여러 profile을 bind한 isolation test |
| 4. 계산 진실성 | route sequence, binding과 bank가 source of truth이고 cache는 재생성 가능 | stale/poisoned cache와 full recomputation equality |
| 5. 두 단계 publication 검증 | candidate와 final payload가 각각 독립 verifier `PASS` | route/bank/metric/outcome/summary/payload corruption rejection |
| 6. 정상 실행 재현성 | 고정 envelope와 정상 step 종료가 같은 trace/result를 만듦 | 반복 실행의 step, champion, solution/result fingerprint 일치 |
| 7. benchmark 정당성 | 동일 manifest의 모든 worker가 정상 완료·검증된 뒤 비교 | incomplete worker 차단, retry identity, completion-order 독립성 |
| 8. roadmap gate 종료 | 각 `RM-*`가 다음 phase가 소비할 권위 산출물을 제공 | phase별 entry/deliverable/negative/fault evidence bundle |
| 9. optional hybrid 진실성 | exact route만 pool에 들어가고 projected candidate는 full evaluation 후 strictly better일 때만 채택 | tiny exact oracle, failure fallback, incumbent fingerprint 보존 |

목적별 최소 gate는 다르다.

- 일반 publishable result: 실제 `RM-1`~`RM-5` 산출물과 두 verifier `PASS`.
- Official Win comparison: 위 조건 + compliant integer fixture + `Q-BENCH-02` 승인 수치 + `RM-6` complete-worker evidence.
- Application cutover: 위 조건 + `RM-8` compatibility, idempotency/cancellation, shadow와 rollback evidence.
- Hybrid activation: verified ALNS/result baseline + `RM-9A`~`RM-9C` + 별도 scope/solver/license/native/fallback 승인.

<a id="language-and-invariants"></a>

## 3. 공통 언어와 불변조건

<a id="core-terms"></a>

### 3.1 처음 나오는 핵심 용어

| 용어 | CVRPTW 경험자를 위한 의미 |
|---|---|
| `Request` | 한 pickup과 그에 대응하는 delivery를 묶는 원자적 운송 의무. 단일 고객 node와 동일시하지 않는다. |
| `pickup/delivery pair` | 같은 `Request`에 속한 두 작업. 같은 vehicle route에서 pickup이 delivery보다 먼저 정확히 한 번씩 나타나야 한다. |
| `SearchRequestBank` | 탐색 중 정규 route에 들어 있지 않은 request ID의 membership. 비용, 실패 이유, final status를 저장하지 않는다. |
| evaluated route artifact | 하나의 concrete vehicle에 결합된 hard-feasible immutable route와 authoritative full evaluation을 보존한 projection-independent 기록. |
| projected column | evaluated route artifact를 특정 exact selection model로 투영한 request/vehicle/resource/objective 계수와 stable ID. |
| `HybridPhase` | 한 worker 안에서 ALNS segment → route pool seal → optional selection → reconstruction/full evaluation → adoption을 하나의 commit 경계로 묶은 단계. |
| hard feasibility | 위반 후보를 어떤 penalty나 acceptance로도 정상 해로 인정하지 않는 규칙. |
| neutral metric | 거리, 대기, 사용량처럼 가격·선호를 포함하지 않은 물리 사실. |
| comparator | feasible solution의 ordered objective dimensions를 의미적으로 비교하는 계약. |
| `SolvePlan` | stage 순서, warm start, 선행 목표 guard와 budget reference를 조정하는 상위 실행 계약. |

<a id="request-partition"></a>

### 3.2 request partition

모든 stable search state에서 각 `Request`는 정확히 하나의 상태여야 한다.

```text
ASSIGNED_IN_SEARCH
= pickup과 delivery가 같은 vehicle route에 각각 정확히 한 번 존재
  AND pickup index < delivery index
  AND request가 SearchRequestBank에 없음

UNASSIGNED_IN_SEARCH
= pickup과 delivery가 모든 route에 없음
  AND request가 SearchRequestBank에 정확히 한 번 존재
```

두 상태는 XOR다. Partial pair, 중복 pair, cross-vehicle pair, delivery-before-pickup, route+bank 동시 membership, 양쪽 누락은 infeasible candidate가 아니라 구현 결함이다.

<a id="route-and-mutation"></a>

### 3.3 route와 atomic mutation

각 stable route는 하나의 vehicle과 terminal 정책에 결합되고, current single-trip 범위에서 pair, directed travel, service pattern, capacity prefix, time/window, size/capability/zone과 route resource hard constraint를 만족해야 한다.

Insert, remove, relocate, exchange, destroy/repair와 rollback의 최소 전이 단위는 `Request`다.

- 성공하면 pickup, delivery, route, bank와 관련 cache/fingerprint가 함께 바뀐다.
- 실패·거절·중단·예외면 관찰 가능한 route, bank, aggregate와 fingerprint가 호출 전과 같아야 한다.
- evaluator, comparator, observer와 best snapshot은 stable state만 본다.
- 구조 결함을 낮은 score, insertion failure 또는 `UNASSIGNED` reason으로 숨기지 않는다.

상세 load/time/service/travel 계산은 [dated Domain Design](2026-07-26-domain-design.md)에 위임한다. Master는 위 원자성과 partition을 변경할 수 없는 상위 불변조건으로 소유한다.

<a id="authority-boundaries"></a>

### 3.4 계산과 정책의 권위 경계

평가는 한 방향으로만 흐른다.

```text
normalized immutable facts
→ propagation and structural/static hard gates
→ policy-neutral facts and metrics
→ composed hard constraints
→ score components
→ objective comparator
→ SolvePlan stages
```

Hard violation은 finite penalty, SA temperature 또는 adaptive reward로 상쇄할 수 없다. Comparator는 route를 다시 물리 계산하지 않고, `SolvePlan`은 hard rule을 해제하지 않는다. Route sequence, vehicle/terminal binding과 bank가 source of truth이며 arrival/load/metric/score/objective/cache는 권위 입력에서 재계산 가능한 파생값이다.

<a id="decision-register"></a>

## 4. 핵심 결정 등록부

<a id="confirmed-decisions"></a>

### 4.1 확정 결정 C-01~C-22

| ID | 확정 내용 |
|---|---|
| `C-01` | 프로젝트 표준 용어는 RPDPTW이며 학술 PDPTW와 구분한다. |
| `C-02` | Master는 구현 완료 보고가 아닌 미래 개발의 규범 설계다. |
| `C-03` | 고객사 변화에 대한 유지보수성과 최소 코어 변경이 최우선이다. |
| `C-04` | hard feasibility, 중립 측정, score, objective 비교와 stage 실행을 분리한다. |
| `C-05` | `Feature`는 vehicle size type이고 capability/qualification은 별도 축이다. |
| `C-06` | `Request`는 same-vehicle, exactly-once, precedence, route/bank XOR를 갖는 atomic `pickup/delivery pair`다. |
| `C-07` | Delivery-only 출발 적재와 실제 pickup-delivery의 물리 의미를 구분한다. |
| `C-08` | 정상 품질 종료는 step limit이고 시간은 watchdog이다. |
| `C-09` | 기본 candidate state는 changed-route copy-on-write와 독립 bank다. Apply/undo는 측정된 COW 병목과 별도 승인 전 기본 경로가 아니다. |
| `C-10` | 소수 물리량은 변환 경계에서 fixed-point integer로 정규화한다. |
| `C-11` | 무게·부피는 `n=3`, 비음수 `FLOOR`, item-first 정규화 후 `qty` 곱을 쓴다. 비용·거리·시간은 정수 입력이며 소수를 거부한다. |
| `C-12` | 기본 volume은 999 CBM, 시간의 유한 상한은 plan end이며 실패는 별도 상태다. |
| `C-13` | 제공된 `D` meter/`U` second를 우선하고 명시적 preparation이 누락값을 승인 산식으로 생성한 뒤 complete travel을 solver/verifier에 전달한다. |
| `C-14` | `ro_input_json_spec.pdf`는 legacy CVRPTW 사례이지 canonical RPDPTW schema가 아니다. |
| `C-15` | `SearchRequestBank` membership과 final `ASSIGNED`/`UNASSIGNED` outcome·diagnostic을 분리한다. `DIRECT`/`LEASE` ownership은 outcome status가 아니다. |
| `C-16` | 4개 성장 정책 × 2개 `DIRECT`-first vehicle 순서로 최대 8개 initial solution을 만들고, 각 screen 뒤 phase-1 champion을 고르며 phase 2는 이를 공통 warm start로 쓴다. |
| `C-17` | Route pool/MIP는 **GATED TARGET**이다. Solver-neutral contract와 fallback을 설계할 수 있으나 `RM-9A`~`RM-9C` predecessor evidence와 별도 scope approval 전 구현 착수 및 production default 활성화를 금지한다. |
| `C-18` | Win PoC comparator는 미배정 request 수 → 배차 차량 수 → 전체 거리 → 전체 운영시간이며 official run은 완결된 verified champion만 쓴다. |
| `C-19` | 선택 변형 문제는 구현이 아니라 deferred feasibility work다. |
| `C-20` | **Superseded — canonical Master C-20을 따른다.** 선택된 target/reference topology는 AWS S3 + Step Functions + Lambda이며 Master는 logical port와 semantic contract만 소유한다. |
| `C-21` | 독립 verifier를 통과하지 않은 후보는 정상 publication 또는 benchmark 대상이 아니다. |
| `C-22` | 강한 재현성은 고정 fingerprint/seed/order/step 아래 모든 worker가 계획 step을 완료하고 정상 종료한 실행에 한정한다. |

<a id="provisional-designs"></a>

### 4.2 잠정 설계 P-01~P-20

아래는 검토 방향이며 확정 결정으로 승격하지 않는다.

| ID | 잠정 방향 | 아직 확정하지 않는 것 |
|---|---|---|
| `P-01` | Delivery-only와 real pickup-delivery의 service pattern을 구분 | 최종 타입명, prefix node 대 initial-load 표현 |
| `P-02` | Delivery-only v1 virtual pickup을 route 시작 prefix에 고정 | depot 작업시간 단위, 외부 노출, mixed-route 규칙 |
| `P-03` | Preparation 완료 뒤 complete travel data를 표현 | common `U`/vehicle-resolved `U` 타입·API·저장 |
| `P-04` | `SolvePlan`은 orchestration, objective schema/score는 평가 vector | 최종 타입명과 objective-plan 분리 방식 |
| `P-05` | 장기 profile registry/factory와 solve별 immutable binding 분리 | factory API, lifecycle과 등록 방식 |
| `P-06` | Propagation facts와 metric contributor/snapshot seam | 최종 표현과 common metric 승격 목록 |
| `P-07` | 선행 objective를 기본적으로 no-worse-than-best로 보호 | tolerance와 탐색 중 relaxation |
| `P-08` | **대체됨:** outcome은 `ASSIGNED`/`UNASSIGNED`, ownership은 vehicle에서 구분 | 운영자 후속 외주·이월은 solver status가 아님 |
| `P-09` | Diagnostic을 code, scope, confidence, source, evidence로 구조화 | 공개 code와 audit 범위 |
| `P-10` | Construction/phase runner registry·manifest·artifact 타입 | step/worker/round 수와 cloud adapter API |
| `P-11` | **기본 경로 아님:** 측정된 COW 병목 때만 apply/undo 재제안 | 동등성 evidence와 승인 없는 전환 금지 |
| `P-12` | Versioned legacy adapter의 제한된 coercion/alias | 허용 목록, canonical JSON, unknown-field 정책 |
| `P-13` | Benchmark manifest/card에 fixture, plan과 모든 fingerprint 보존 | 공식 수치는 `Q-BENCH-02` 실험 대기 |
| `P-14` | 실행 경계를 logical port로 분리 | transport, orchestrator, store와 배포 단위 |
| `P-15` | Route pool은 exact identity와 dominance bucket을 분리하고 append/import에 같은 merge 적용 | cap, aging, pruning, persistence |
| `P-16` | Corrected candidate는 explicit unassigned와 actual vehicle consumption을 가진 exact set partition | production 기본 승격과 profile projection 범위 |
| `P-17` | `SET_COVER_THEN_CONVERT`는 OGC 2024 differential/compatibility 실험에만 격리 | legacy converter 장기 유지 |
| `P-18` | MIP 실패·무효·비개선이면 cache-free validated ALNS incumbent 보존 | MIP-required product mode와 degraded 외부 노출 |
| `P-19` | Worker-local inner hybrid를 baseline으로 사용 | Cross-worker pool fan-in과 중앙 selection |
| `P-20` | Corrected ALNS는 중앙 pair editor, versioned operator registry, shortlist→exact insertion과 step-based adaptation을 사용 | production operator subset과 수치 default |

<a id="implementation-flow"></a>

## 5. 입력에서 publication까지의 구현 흐름

<a id="end-to-end"></a>

### 5.1 end-to-end 흐름

```text
external submission
→ versioned adapter
→ canonical business input
→ normalization
   ├─ immutable problem facts
   └─ travel preparation → complete prepared directed travel
→ exact profile/config binding
→ immutable solve snapshot
→ atomic pair evaluator + 최대 8개 initial portfolio
→ phase-1 screen ALNS → phase-1 champion
→ phase-2 worker batch
   ├─ ALNS-only
   └─ [C-17 gate 통과 시] HybridPhase
      → route collection/seal
      → exact route selection
      → reconstruction + authoritative full evaluation
      → strictly-better adoption or ALNS fallback
→ complete worker fan-in → committed solve best
→ candidate solution verifier
→ finalization + required insertion audit
→ result-integrity verifier
→ publishable verified result
→ optional official benchmark comparison
```

뒤 단계는 raw input을 다시 해석하거나 앞 단계가 resolve하지 않은 값을 유사 default로 보완하지 않는다. Cancellation은 미완료 candidate의 discard를 요구하고, lineage는 input부터 final payload까지 모든 identity를 연결한다.

<a id="stage-contracts"></a>

### 5.2 단계별 책임과 산출물

| 단계 | 권위 입력 | 다음 단계가 소비할 산출물 | 실패 경계 |
|---|---|---|---|
| Adapter | external bytes/reference, schema/version | canonical business input, raw digest, coercion provenance | 모호한 schema/reference 사전 거부 |
| Normalization | canonical input | dense immutable domain facts | 단위·pair·reference·overflow 오류 사전 거부 |
| Travel preparation | locations, vehicles, raw travel/coordinates | complete directed `D`와 resolved `U`, provenance/fingerprint | unresolved arc이면 solve 시작 금지 |
| Profile binding | facts, exact customer/profile/preset/config | immutable constraints/metrics/score/comparator/`SolvePlan` | unknown/latest/cross-customer fallback 금지 |
| Portfolio | solve snapshot, exact policy matrix | 독립 initial candidates와 route artifacts | 불변조건/full evaluation 실패 후보 제외 |
| ALNS | initial champion, explicit step/worker config | committed candidate, termination과 trace | 미완료 worker로 champion 확정 금지 |
| Optional route selection | sealed route artifacts, exact projection, budget/backend | selected stable IDs 또는 typed fallback | raw MIP 값을 candidate로 취급 금지 |
| Candidate verification | routes, bank, problem/profile/travel authority | cache-free `PASS`와 verified solution | `PASS` 없으면 finalization 중단 |
| Finalization | verified solution, request universe | outcomes, audit, diagnostics, summary | bank/last failure를 final truth로 사용 금지 |
| Result verification | candidate `PASS`, outcomes/audit/payload | result-integrity `PASS`, payload fingerprint | `PASS` 없으면 publication 금지 |
| Official benchmark | 동일 manifest의 publishable results | complete verified champion/comparison | worker 하나라도 미완료면 `INCOMPLETE` |

<a id="dependency-and-lifecycle"></a>

### 5.3 의존 방향과 생명주기

허용되는 의존 방향은 외부 adapter/infrastructure → application logical ports → domain/normalization/travel → bound evaluation → portfolio/search → independent verification/result다.

- Core는 transport, storage, 인증, cloud SDK와 runtime DTO를 모른다.
- Algorithm은 customer ID와 raw field를 분기하지 않는다.
- Evaluation은 propagation facts를 소비하고 raw route/input을 재해석하지 않는다.
- Finalization은 verified solution을 소비하고 bank나 last failure를 final truth로 쓰지 않는다.
- Publication은 두 verifier 판정을 소비하고 자체 feasibility를 추정하지 않는다.
- Search candidate만 한 step 안에서 mutable하다. Problem, prepared travel, profile, committed snapshot, sealed route pool, verifier report와 publishable result는 경계를 넘을 때 immutable이어야 한다.

구체 package DAG와 logical port 배치는 [dated Architecture Design](2026-07-26-architecture-design.md)에 위임한다.

<a id="master-contracts"></a>

## 6. 도메인·입력·평가·결과의 Master 계약

<a id="canonical-domain"></a>

### 6.1 canonical domain

Canonical model은 `Request`, pickup/delivery node, vehicle, terminal, physical location, route, solution과 `SearchRequestBank`를 분리한다. 여러 solver node가 같은 physical location을 참조할 수 있지만 역할 identity는 합치지 않는다.

Delivery-only는 route 출발 전에 적재된 화물의 논리 pickup이고, real pickup-delivery는 실제 위치·시간·service가 있는 pickup이다. 둘은 같은 route에 섞일 수 있으나 모든 route prefix에서 각 load 차원이 `0 <= load <= capacity`여야 한다. Current 범위는 `oneway`와 depot으로 한 번 복귀하는 single `roundtrip`이며 multi-trip/rotation은 deferred다.

`Feature`는 대소문자를 구분하는 vehicle size type이다. Capability/qualification과 zone은 별도 hard constraint 축이고, `DIRECT`/`LEASE`는 vehicle ownership이지 assignment status가 아니다.

<a id="input-and-travel"></a>

### 6.2 입력·정규화·travel authority

Master가 고정하는 입력 원칙은 다음과 같다.

- 무게·부피는 정확한 decimal을 `n=3`, 비음수 `FLOOR`, item-first 방식으로 fixed-point 정규화한 뒤 `qty`를 곱한다.
- 비용·거리·시간은 integer 계약이며 소수 입력을 조용히 절삭하지 않는다.
- Overflow와 계산 실패는 큰 sentinel이나 plan end로 치환하지 않는다.
- Timezone/DST 해석은 solver 밖에서 끝내고 core는 하나의 monotonic `long` second 축을 사용한다.
- Travel key는 solver node가 아니라 physical location ID다.
- Provided directed integer `D`/`U`가 authoritative하며, missing 값은 solve 전 명시적 preparation에서만 생성한다.
- Reverse 복사, 대칭화, 탐색 중 lazy travel generation을 금지한다.
- Solver와 verifier는 같은 complete prepared travel fingerprint를 사용한다.

[`data/ro_input_json_spec.pdf`](../data/ro_input_json_spec.pdf)는 legacy CVRPTW 사례이지 canonical RPDPTW schema가 아니다. [`data/win_poc_case.json`](../data/win_poc_case.json)은 read-only fixture이며 현재 소수 `D/U` 때문에 정수 계약을 만족하지 않아 official baseline에 쓸 수 없다.

세부 field, service time, window 반복, travel 생성식과 acceptance 예시는 [dated Domain Design](2026-07-26-domain-design.md)이 소유한다.

<a id="profile-and-evaluation"></a>

### 6.3 profile과 evaluation 분리

Solve 시작 전 exact customer/profile/preset/config를 resolve해 immutable bound profile을 만든다. `latest`, 비슷한 preset, 다른 고객 profile로 fallback하지 않는다. Missing dependency, duplicate score key, unit/schema mismatch와 unauthorized objective는 bind 전에 거부한다.

고객별 확장은 input meaning → static compatibility → route/solution hard constraint → neutral metric → score → comparator → `SolvePlan` 중 가장 좁은 seam에 둔다. 실제 물리 상태를 기존 facts로 표현할 수 없을 때만 common propagation contract를 확장한다.

Mandatory 의미는 필요하면 `mandatoryUnassignedCount`를 최상위 lexicographic dimension으로 최소화한다. Hard infeasibility나 숨은 Big-M으로 바꾸지 않는다. Win PoC comparator는 benchmark 전용이지 모든 고객 profile의 objective가 아니다.

<a id="search-and-result"></a>

### 6.4 search state와 final result 분리

`SearchRequestBank`는 request ID membership만 보유한다. Final result는 다음 경로로만 만든다.

```text
committed candidate
→ candidate solution verifier PASS
→ preliminary ASSIGNED/UNASSIGNED partition
→ required final-solution insertion audit
→ outcomes + bounded diagnostics + outcome-derived summary
→ result-integrity verifier PASS
```

모든 input `Request`는 exactly one final outcome을 가져야 한다. `ASSIGNED`는 exactly one verified route/vehicle/pair를 참조하고 `UNASSIGNED`는 route를 참조하지 않는다. `DIRECT`와 `LEASE` route의 request는 모두 `ASSIGNED`다.

Static precheck로 불가능성이 증명된 경우에만 `PROVEN` diagnostic을 쓸 수 있다. 그 밖의 `UNASSIGNED`는 final routes를 고정한 exhaustive insertion audit 대상이다. Audit가 feasible insertion을 찾아도 자동 삽입·재탐색하지 않으며, 현재 final solution 기준의 evidence 범위를 넘는 confidence를 주장하지 않는다.

<a id="alns-baseline"></a>

## 7. ALNS 기준 구현

<a id="initial-portfolio"></a>

### 7.1 8개 initial portfolio

초기 portfolio는 네 request-route 성장 정책과 두 `DIRECT`-first vehicle 순서의 조합으로 최대 8개 independent candidate를 만든다.

| 축 | 값과 의미 |
|---|---|
| Request-route 성장 | `CLOCK`, `SEQ_FARTHEST`, `SEQ_LARGE_DEMAND`, `SEQ_EARLIEST_DEADLINE` |
| Vehicle 순서 | `DIRECT_FIRST_LARGE`, `DIRECT_FIRST_SMALL`; 둘 다 feasible `DIRECT`를 `LEASE`보다 먼저 시도 |

정책은 후보 순서만 정하며 feasibility를 재구현하지 않는다. 모든 pair insertion은 하나의 side-effect-free atomic evaluator를 통과한다. `CLOCK`에 필요한 좌표가 없으면 해당 조합은 `UNAVAILABLE`이고 유사 좌표로 fallback하지 않는다. Candidate끼리 route, bank, cache와 random state를 공유하지 않는다.

<a id="two-phase-alns"></a>

### 7.2 two-phase 실행

각 initial candidate는 exact `screenMaxSteps`의 독립 phase-1 screen을 완료한다. Cache-free validation을 통과한 candidate만 comparator 대상이며, stable tie-break로 phase-1 champion 하나를 고른다.

Phase 2는 champion을 모든 worker의 공통 warm start로 사용한다. 각 worker는 derived seed와 explicit operator config로 exact `phase2MaxSteps`를 완료한다. 선언된 모든 worker가 정상 완료·검증된 뒤에만 round champion을 고른다.

```text
STRICTLY_BETTER → 다음 round의 공통 warm start
EQUAL/WORSE     → NO_STRICT_IMPROVEMENT
maxRounds 도달 → MAX_ROUNDS_REACHED
```

`screenMaxSteps`, worker 수, `phase2MaxSteps`, `maxRounds`와 watchdog의 official 수치는 [Q-BENCH-02](master-design-open-questions.md#q-bench-02)의 실험 승인 전 존재하지 않는다.

<a id="alns-step"></a>

### 7.3 completed step과 operator 경계

하나의 completed ALNS step은 destroy → repair → bounded in-step improvement → stable validation/evaluation → hard/stage guard → acceptance → commit/discard → best/adaptive update 전체를 끝낸 경우에만 1 증가한다.

Destroy/repair 대상 수는 node가 아니라 `Request` 수다. Operator는 ordered unique request IDs와 근거를 제안하고 중앙 pair editor가 실제 pair mutation을 수행한다. Repair hot path는 cheap compatibility/promising shortlist 뒤 공통 evaluator로 모든 합법 pickup/delivery position을 exact 평가한다. Shortlist score는 feasibility, objective 또는 diagnostic의 권위가 아니다.

미완료·invalid·interrupted step은 completed count, acceptance temperature, reward와 adaptive state를 전진시키지 않는다. Non-improving feasible acceptance는 current만 바꿀 수 있고 cache-free validated stageBest/solveBest를 악화시키지 않는다.

<a id="state-and-reproducibility"></a>

### 7.4 COW, cache, 종료와 재현성

기본 state 전략은 changed-route copy-on-write다. Candidate는 first write 전에 변경 route와 독립 bank를 복사한다. Accepted candidate만 freeze해 새 current가 되고 rejected/interrupted/failed candidate는 전체 폐기한다.

Apply/undo는 자동 roadmap 단계가 아니다. Representative profiling에서 COW copy/allocation/GC가 실제 병목임을 보이고, round-trip/fault/trace/cache-free result 동등성과 별도 변경 승인을 얻을 때만 재제안할 수 있다. COW 유지도 정상적인 `RM-7` 완료 결과다.

정상 품질 종료는 `MAX_STEPS_REACHED`, 완결 batch 뒤 `NO_STRICT_IMPROVEMENT`, 또는 `MAX_ROUNDS_REACHED`다. `WATCHDOG_REACHED`, `CANCELLED`, `RESOURCE_LIMIT_REACHED`, `PLATFORM_TIMEOUT`, `FAILED`는 서로 구분되는 비정상/안전 종료다.

강한 재현성은 problem/travel/profile/config/build, seed derivation, stable order, exact step budget과 정상 종료가 고정된 envelope에만 주장한다. Global random, unordered merge, thread first-winner, clock tie-break와 cache hit/miss 의존을 금지한다.

<a id="alns-mip"></a>

## 8. ALNS–MIP 적용 개요와 C-17 gate

<a id="why-mip"></a>

### 8.1 왜 route pool과 MIP를 쓰는가

ALNS는 한 solution을 국소적으로 바꾸는 데 강하지만, 여러 시점에 발견한 좋은 route를 한꺼번에 재조합하지 못할 수 있다. Optional MIP는 ALNS 중 발견한 정확한 route를 column으로 보고 request coverage와 concrete vehicle consumption을 전역적으로 다시 선택한다.

이 기능은 ALNS를 대체하지 않는다. MIP는 더 나은 후보를 제안할 수 있을 뿐이며, raw solver incumbent가 core candidate나 final result의 권위를 얻지는 않는다.

<a id="artifact-to-column"></a>

### 8.2 evaluated route artifact에서 projected column까지

Acceptance와 무관하게 completed hard-feasible route만 `evaluated route artifact`로 수집한다. 전체 solution이 reject되어도 그 안의 route는 수집할 수 있지만 infeasible, interrupted, rollback 중이거나 cache만 평가된 route는 수집하지 않는다.

Artifact는 exact route identity, concrete vehicle/terminal binding, ordered visits, request coverage, authoritative facts/metrics, authority fingerprints와 discovery lineage를 갖는다. Append와 import는 같은 deterministic validation/merge를 사용한다. Pool route는 mutate하지 않고 incumbent route는 seal 전 merge해 pin한다.

Selector는 live pool이 아니라 stable-sorted immutable snapshot을 받는다. 특정 projection이 artifact를 request/vehicle/resource/objective row coefficient로 바꾼 결과가 `projected column`이다. Pool identity와 model projection을 분리하므로 지원하지 않는 profile은 조용히 근사하지 않고 typed skip한다.

<a id="exact-partition"></a>

### 8.3 exact partition 원칙

Corrected selection의 기본 후보는 `SET_PARTITION_EXACT`다.

```text
각 Request i:
  i를 포함하는 selected projected columns의 합 + explicit unassigned_i = 1

각 concrete vehicle v:
  v를 소비하는 selected projected columns의 합 <= 1
```

따라서 모든 request는 정확히 한 route에 들어가거나 명시적으로 unassigned다. Concrete vehicle의 route consumption도 실제 자원 제약으로 모델링한다. Vehicle-class aggregation은 travel/capacity/work window/ownership/objective 동등성이 증명된 별도 mode에서만 허용한다.

Projection은 가산 또는 exact linearizable dimension만 포함한다. Lexicographic priority는 staged exact solve 또는 검증된 backend capability로 구현하며 근거 없는 Big-M을 금지한다. `SET_COVER_THEN_CONVERT`는 OGC 2024 compatibility/differential 실험에만 격리하며 중간 cover는 stable candidate가 아니다. 상세 모델 수식과 projection 계약은 [dated Domain Design](2026-07-26-domain-design.md)에 위임한다.

<a id="hybrid-phase"></a>

### 8.4 HybridPhase와 채택 규칙

기준 baseline은 worker-local `HybridPhase`다.

```text
cache-free validated ALNS warm start
→ ALNS segment + route collection
→ immutable worker-local pool seal
→ optional exact route selection
→ fresh reconstruction
→ authoritative full evaluation
→ ALNS incumbent와 comparator 비교
→ strictly-better candidate만 채택
→ stable champion을 다음 HybridPhase warm start로 전달
```

Cross-worker pool fan-in과 중앙 MIP는 별도 scalability ADR/evidence 전 기본 경로가 아니다. `roundOrdinal`, `workerOrdinal`, `hybridPhaseOrdinal`, `alnsRunOrdinal`은 서로 다른 identity다. Cover, raw solver incumbent와 reconstruction 중 route는 stable search state가 아니다.

<a id="fallback"></a>

### 8.5 fallback과 활성화 금지선

No incumbent, backend/license/native failure, budget 소진, model/build 오류, invalid reconstruction, full-evaluation failure, fingerprint mismatch 또는 `EQUAL/WORSE`이면 route selection 직전 ALNS segment가 완료한 cache-free validated `HybridPhaseIncumbent`를 그대로 보존한다.

- Optional plan은 `DEGRADED_ALNS_ONLY`와 이유를 남기고 계속할 수 있다.
- MIP-required official plan은 같은 상황을 성공으로 숨기지 않고 `INCOMPLETE` 또는 명시적 failure로 끝낸다.
- Raw solver objective나 selected column 목록은 reconstruction/full evaluation/comparator adoption을 우회할 수 없다.
- Strong replay는 backend/version/thread/order/work budget까지 deterministic하다고 증명한 경우에만 주장한다. Deadline multi-thread MIP는 `TIMEBOXED_HYBRID`로 분리한다.

`C-17`은 절대적인 활성화 gate다. Verified ALNS와 both-verifier baseline, `RM-9A`~`RM-9C` evidence, solver/license/native/fallback 승인과 별도 scope approval 전에는 vendor dependency를 core/default path에 넣거나 production에서 MIP를 켜지 않는다.

<a id="verification"></a>

## 9. 독립 검증, publication과 benchmark evidence

<a id="two-verifiers"></a>

### 9.1 두 verifier gate

Publication은 두 독립 gate를 이 순서로 통과해야 한다.

1. **Candidate solution verifier:** immutable problem/profile declaration, candidate route order, `SearchRequestBank`, prepared travel만으로 pair partition, vehicle/terminal, compatibility, directed legs, load/time/resource, hard constraints, neutral metrics, score와 objective를 cache 없이 재계산한다.
2. **Result-integrity verifier:** candidate `PASS`와 verified solution, final outcomes, audit/diagnostic evidence, outcome-derived summary와 payload를 사용해 exactly-one outcome, route/vehicle reference, confidence ceiling, summary와 canonical payload fingerprint를 다시 검증한다.

Solver feasible flag, insertion result, search cache, cached metric, solver summary와 finalizer 주장은 어느 verifier의 권위 입력도 아니다. 하나라도 `FAIL` 또는 미완료이면 정상 route/outcome payload와 benchmark vector를 발행하지 않는다.

<a id="win-poc"></a>

### 9.2 Win PoC 비교 계약

Win PoC 품질 vector는 다음 exact lexicographic order이며 모든 성분은 작을수록 좋다.

```text
(
  unassigned Request count,
  dispatched input vehicle count,
  verifier-recomputed total directed distance,
  verifier-recomputed total route operational time
)
```

처음으로 다른 성분만 승패를 결정한다. Structural tie-break는 다섯 번째 품질 성분이 아니다. 이 vector는 Win PoC benchmark 전용이며 customer objective를 대체하지 않는다.

Official manifest는 fixture, adapter/numeric/time/travel, profile, algorithm/build, seeds/steps, round/worker plan, verifier와 metric formula를 고정한다. 모든 worker가 exact step으로 정상 완료하고 검증된 뒤 stable fan-in한 final champion만 비교한다. Fingerprint가 다른 result는 quality regression으로 비교하지 않는다.

Primary fixture [`data/win_poc_case.json`](../data/win_poc_case.json)의 확인된 SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이지만, 소수 `D/U` 비준수 때문에 정수 matrix를 다시 받기 전 official run에 사용할 수 없다.

<a id="evidence-bundle"></a>

### 9.3 evidence bundle

완료 evidence는 happy path만이 아니라 다음을 함께 보존해야 한다.

- Input/travel/profile/build/config와 canonical solution/result fingerprints.
- Hand-calculated domain/evaluation/comparator 사례.
- Pair mutation, rollback, cancellation, exception과 interrupted-step fault injection.
- Cache hit/miss, stale/poisoned cache와 cache-free full recomputation equality.
- 모든 worker의 requested/completed steps, seed derivation, warm-start lineage와 termination.
- Candidate/result corruption rejection과 publication 차단.
- Benchmark incomplete/retry/completion-order cases.
- Hybrid 사용 시 pool digest, evaluated artifact/projected column 수, projection/model/backend status, reconstruction, full evaluation, adoption/fallback과 next warm-start fingerprint.

Elapsed time과 memory는 성능 evidence이며 품질 vector나 숨은 algorithm input이 아니다.

<a id="roadmap"></a>

## 10. Implementation roadmap와 phase gate

<a id="critical-path"></a>

### 10.1 critical path

```text
RM-0 decision baseline
→ RM-1 normalized problem + prepared travel
→ RM-2 bound evaluation/profile
→ RM-3 atomic pair evaluator + initial portfolio
→ RM-4 COW ALNS + reproducible execution record
→ RM-5 both-verifier publishable result
→ RM-6 official Win workflow
→ RM-7 measured COW decision
→ RM-8 verified integration/cutover

RM-9A → RM-9B → RM-9C = C-17 아래 별도 승인 branch
```

Logical port interface와 verifier fixture 설계는 일찍 병행할 수 있다. 그러나 test double, experiment value나 partial implementation이 다음 phase의 production authority로 자동 승격되지는 않는다.

<a id="rm-0-to-8"></a>

### 10.2 RM-0~RM-8

| Phase | Entry / 목표 | 다음 phase가 소비할 deliverable | Exit evidence와 gate |
|---|---|---|---|
| `RM-0` | 선행 없음 / 결정·문서 baseline | `C-01~C-22`, `P-01~P-20`, 질문 상태와 traceability | 링크/anchor/lint, 결정 수·상태 coverage, 과도한 구현 완료 주장 0건 |
| `RM-1` | `RM-0` / versioned input, immutable domain, travel preparation | Immutable problem, complete directed travel, dense mapping과 fingerprints | numeric/time/service hand cases, overflow rejection, pair/reference/terminal properties, provided/generated/asymmetric travel와 solver/verifier identity |
| `RM-2` | `RM-1` / propagation, evaluation과 bound profile | Immutable constraints/metrics/score/comparator/`SolvePlan`, full evaluation | dependency/unit/schema rejection, customer isolation, hand evaluation, comparator transitivity와 stage guard |
| `RM-3` | `RM-2` / atomic evaluator와 8개 portfolio | 최대 8개 cache-free validated candidates와 source lineage | policy matrix, `CLOCK` unavailable, stable tie-break, evaluator/full equality, failed insertion rollback와 candidate isolation |
| `RM-4` | `RM-3` / two-phase COW ALNS | phase-1 champion, phase-2 solveBest, termination/step/seed/warm-start trace | complete batch fan-in, COW isolation, accept/reject/exception/cancel/watchdog faults, cache equality와 fixed-envelope rerun |
| `RM-5` | `RM-4` + `RM-1/2` authorities / finalization과 두 verifier | verified solution, exactly-one outcomes, audit, both `PASS`, payload fingerprint 또는 explicit rejection | corrupted route/bank/travel/metric/outcome/summary/payload rejection, confidence ceiling과 gate별 publication 차단 |
| `RM-6` | `RM-5` + compliant fixture + `Q-BENCH-02` 승인 / official Win workflow | Immutable manifest와 complete verified final champion | exact comparator, incomplete/retry, worker-order independence, identical-manifest rerun와 explicit approval |
| `RM-7` | measured `RM-4~6` path / COW profiling | COW 유지 판정 또는 별도 apply/undo 변경 제안 | bottleneck attribution; 제안 시 correctness/reproducibility/observability 동등성과 승인. 자동 전환 금지 |
| `RM-8` | interface는 `RM-0` 뒤 가능, cutover는 `RM-1~5` 필요 / logical-port integration | compatibility matrix, idempotent status, versioned cutover/rollback | characterization, shadow, retry/idempotency/cancellation faults, both-gate publication과 rollback rehearsal |

Official 수치가 없는 `Q-BENCH-02`는 logical coordinator/test double을 막지 않지만 `RM-6` baseline 발행을 막는다. Current decimal fixture 문제도 generic `RM-1` 구현을 막지 않지만 official `RM-6` evidence로 사용할 수 없다.

<a id="rm-9"></a>

### 10.3 gated RM-9A~RM-9C

| Phase | Entry / 구현 단위 | Deliverable | Exit evidence와 금지 |
|---|---|---|---|
| `RM-9A` | `RM-4` cache-free ALNS + 별도 scope 승인 / acceptance-independent collector, deterministic merge/pin/seal | Worker-local pool delta와 immutable snapshot | accepted/rejected feasible route harvest, interrupted exclusion, append/import equality, immutable alias, conservative dominance oracle, deterministic digest/memory. Hidden pruning 금지 |
| `RM-9B` | `RM-9A` + projection/backend/license/fallback 승인 / exact partition, warm start, provider-neutral status, reconstruction | Stable selected IDs, model/backend evidence와 full-evaluated candidate 또는 typed fallback | tiny-pool brute-force oracle, request/vehicle rows, status×incumbent, license/model/native failure, cleanup. Hidden Big-M/raw objective 채택 금지 |
| `RM-9C` | `RM-9B` + `RM-5` both-gate + approved `HybridPhase` plan / end-to-end feedback와 shadow | Immutable hybrid record, adopted champion/fallback lineage, activation recommendation 또는 reject | invalid/no-incumbent/license fallback fingerprint 보존, adopted-only feedback, replay class, ALNS-only 대비 quality/memory/native/license shadow |

`RM-9A`~`RM-9C`의 설계 존재는 구현 착수 승인이나 production activation이 아니다. Cross-worker pool fan-in과 중앙 MIP는 worker-local baseline 뒤 별도 scalability ADR을 요구한다.

<a id="migration-and-risks"></a>

## 11. Migration, 위험과 deferred work

<a id="migration"></a>

### 11.1 migration 원칙

Current placeholder와 legacy 문서는 목표 계약 evidence가 아니라 replacement inventory와 characterization 입력이다.

1. 현재 input/output/state/error 의미를 read-only로 기록한다.
2. 목표 계약과 semantic compatibility matrix를 만든다.
3. 새 normalized core, evaluation, algorithm과 verifier를 logical ports 뒤에 격리한다.
4. 승인된 legacy meaning만 versioned adapter로 변환하고 provenance를 보존한다.
5. Side-effect 없는 shadow comparison을 수행한다.
6. 두 publication gate를 통과한 결과만 publication/benchmark candidate로 사용한다.
7. Versioned cutover, retry/idempotency/cancellation과 recoverable rollback을 검증한다.
8. 구현이 설계를 바꾸면 decision record와 영향받는 문서를 같은 변경 단위에서 갱신한다.

<a id="risks"></a>

### 11.2 주요 위험과 방어 gate

| 위험 | 방어 gate |
|---|---|
| Adapter/evaluator/verifier semantic drift | exact policy/source fingerprint와 identity check |
| 고객사 분기의 core 침투 | profile dependency closure와 change-impact review |
| pair/rollback 손상 | 중앙 atomic editor, fault injection, full partition verification |
| stale cache | insertion/acceptance/best/final에서 cache-free equality |
| 재현성 침식 | namespaced seed, stable order, step 종료, complete fan-in |
| verifier coupling | search cache/summary 거부와 corrupted-cache test |
| benchmark mismatch | immutable manifest, fingerprint equality, incomplete 차단 |
| premature state 최적화 | COW first, measured `RM-7`, no-switch 허용 |
| unsafe route dominance | exact identity와 proven component-wise dominance만 사용 |
| projection drift | versioned projection capability, unsupported profile typed skip, reconstructed full comparator |
| solver/native/license failure | optional backend 격리, status gate, deterministic cleanup, exact ALNS fallback |
| hybrid nondeterminism | 재현성 등급, backend/version/order/budget lineage와 timeboxed 통계 |

<a id="deferred"></a>

### 11.3 deferred 재개 조건

| 항목 | 현재 보존할 경계 | 재개 조건 |
|---|---|---|
| Route pool/MIP production activation | evaluated artifact/projected column/`HybridPhase`의 solver-neutral 계약 | `RM-9A~C`, verified baseline, measured value와 solver/license/native/fallback 승인 |
| Optional variants | atomic pair, fixed terminal, bank, travel contract | [Q-VAR-01](master-design-open-questions.md#q-var-01)의 선택·시점, fixture와 core-impact 승인 |
| AWS implementation/cutover | logical ports와 status/artifact/idempotency/cancellation | canonical Master의 `RM-8` parity, two-gate publication, shadow/cutover/rollback 및 운영 승인 |
| AWS 이외 physical topology | AWS와 같은 logical contract | parity evidence와 별도 승인 |
| Academic benchmark 확장 | Win manifest/verifier 재사용 경계 | authoritative format/result, RPDPTW mapping과 별도 manifest 승인 |
| Multi-trip/rotation | current single-trip와 trip을 넘지 않는 pair | trip/reset/depot window/resource 계약과 영향 분석 승인 |
| Dynamic routing | immutable solve snapshot과 cancellation port | event/replanning/state continuity/SLA 계약 승인 |

<a id="traceability"></a>

## 12. 질문, 근거와 traceability

<a id="open-questions"></a>

### 12.1 open question 상태

28개 질문의 단일 등록부는 [Master Design open questions](master-design-open-questions.md)다. 현재 상태는 `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`다.

- `Q-ALG-01`: 8개 initial solution과 phase-1 선별 구조로 해결.
- [`Q-ALG-02`](master-design-open-questions.md#q-alg-02): `RESOLVED — KEEP_COW`.
- [`Q-BENCH-02`](master-design-open-questions.md#q-bench-02): protocol은 확정됐지만 official 수치는 실험 대기.
- [`Q-INFRA-01`](master-design-open-questions.md#q-infra-01): `RESOLVED`; AWS S3 + Step Functions + Lambda가 선택된 target/reference runtime.
- [`Q-VAR-01`](master-design-open-questions.md#q-var-01): `DEFERRED`.

새 질문 ID를 이 문서에서 만들지 않는다.

<a id="source-links"></a>

### 12.2 세션·연구 링크

기존 결정과 연구의 추적 링크는 다음과 같이 보존한다.

| 자료 | Master가 소비한 경계 |
|---|---|
| [세션 18 — governance](master-design-sessions/18-document-governance.md) | REVIEW 상태, 규범어, source hierarchy와 change control |
| [세션 20 — domain/input](master-design-sessions/20-domain-input-draft.md) | canonical model, pair invariant, normalization, matrix와 bank |
| [세션 21 — policy/objective](master-design-sessions/21-policy-objective-draft.md) | hard/metric/score/objective/plan 분리와 profile |
| [세션 22 — algorithm](master-design-sessions/22-algorithm-draft.md) | portfolio, ALNS, COW/cache/rollback와 termination |
| [세션 23 — result/benchmark](master-design-sessions/23-result-benchmark-draft.md) | result partition, verifier, recovery와 Win comparison |
| [세션 24 — roadmap](master-design-sessions/24-roadmap-draft.md) | logical ports, phase/evidence, migration와 deferred criteria |
| [세션 29 — 사용자 인터뷰](master-design-sessions/29-open-question-interview.md) | 사용자 답변과 실험 protocol |
| [세션 30 — 질문 통합](master-design-sessions/30-open-question-integration.md) | 문서군 반영 범위와 validation history |
| [세션 31 — Domain Design 통합](master-design-sessions/31-domain-design-integration.md) | 상세 Domain 재구성과 Master 정합화 history |
| [OGC 2024 DMS ALNS–MIP 분석](../../../optichallenge/2024/2024_algorithms_DMS_v1.0.0/OGC2024_DMS_ALNS_MIP_design_ko.md) | SHA-256 `fd71f3bf03eb04af970e84bf4df8b72d27679e482dd585ea430b165fd3481e84`; destroy/adaptation, promising repair, rejected-route pool과 Set Covering feedback의 비규범 transfer evidence |

OGC 자료의 all-pickups-first, 3 rider types, complete assignment, scalar cost와 확인된 결함은 canonical 계약으로 채택하지 않는다.

RPDPTW 연구 배경은 [문제 정의](arranged/01_problem_definition.md), [초기해 휴리스틱](arranged/02_initial_solution_heuristics.md), [ALNS](arranged/03_alns_metaheuristic.md), [local search](arranged/05_local_search_moves.md), [실무 확장](arranged/06_practical_extensions.md), [논문·benchmark](arranged/07_papers_and_benchmarks.md)에 있다. 연구 예시는 승인된 업무 계약을 대신하지 않는다.

<a id="requirement-map"></a>

### 12.3 requirement-to-evidence map

| 규범 영역 | 결정 | Roadmap | 필수 evidence |
|---|---|---|---|
| 모델과 입력 | `C-01`, `C-05~C-14` | `RM-1` | normalization, pair/property, boundary/overflow, directed travel |
| 확장 평가 | `C-03~C-05`, `C-15`, `C-18` | `RM-2` | profile isolation, bind rejection, full evaluation/comparator |
| 탐색과 상태 | `C-06`, `C-08~C-09`, `C-16`, `C-22`, `P-20` | `RM-3`, `RM-4`, `RM-7` | portfolio, atomic edit, shortlist→exact, rollback/cache, fixed-step rerun |
| 결과와 benchmark | `C-15`, `C-18`, `C-21~C-22` | `RM-5`, `RM-6` | verifier independence, outcome partition, manifest, complete champion |
| Route pool | `C-17`, `P-15`, `P-19` | `RM-9A` | rejected feasible harvest, deterministic merge/seal, dominance/memory |
| Exact selection/hybrid | `C-17`, `P-16~P-19` | `RM-9B`, `RM-9C` | tiny oracle, exact partition, reconstruction/full evaluation, fallback/adoption |
| System/migration | `C-02`, `C-19~C-20` | `RM-0`, `RM-8` | document audit, logical-port compatibility, shadow/cutover/rollback |

이 문서의 변경이 문제 의미, invariant, 입·출력, objective, numeric/time, roadmap gate 또는 logical responsibility를 바꾸면 승인된 decision record와 영향을 받는 dated 상세 문서를 같은 변경 단위에서 갱신해야 한다. 구현 진행 기록은 이 설계를 대체할 수 없다.
