# RPDPTW 통합 솔버 Master Design

```yaml
status: SUPERSEDED
version: 3.5-review
last_updated: 2026-07-28
owner: RPDPTW 설계 책임 역할
scope: 개발자가 RPDPTW 솔버를 구현하기 위한 전체 구조, 컴포넌트 책임, 계약, 구현 순서와 검증 evidence
supersedes: 이 파일의 기존 legacy 통합 초안
related_decisions:
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
  - master-design-sessions/31-domain-design-integration.md
approval_records:
  - 2026-07-26 user-approved AWS S3 + Step Functions + Lambda target runtime (`Q-INFRA-01`)
  - 2026-07-28 user-selected Google OR-Tools CP-SAT as the only Phase 13 exact route-selection backend if `C-17` is later activated; this does not activate or accept Phase 13
superseded_by: docs/master-design.md
phase_c: path-and-status-only

```

<!-- phase-c-authority-banner -->
> **SUPERSEDED (Phase C)** — current authority: [`docs/master-design.md`](../master-design.md). This file is historical only. Do not use as conflict authority.


## 목차

- 문서 안내와 기준
  - **1. 이 문서의 목적, 독자와 사용법**
  - **2. 구현할 시스템과 완료 정의**
  - **3. 용어와 결정 요약**
- 전체 설계
  - **4. 구현 아키텍처와 책임 경계**
- 도메인·입력·평가 계약
  - **5. Canonical RPDPTW 도메인 모델**
  - **6. 핵심 불변조건과 atomic mutation**
  - **7. 입력, 정규화와 domain value 계약**
  - **8. Directed distance/time matrix 계약**
  - **9. Extensible policy, evaluation과 profile architecture**
  - **10. Search solution과 final result**
- 탐색과 실행 제어
  - **11. Initial portfolio, ALNS와 route-selection pipeline**
  - **12. Candidate state, cache와 rollback**
  - **13. Termination, reproducibility와 execution provenance**
- 검증과 구현 전환
  - **14. Independent verification, publication과 Win PoC benchmark**
  - **15. Implementation roadmap와 phase gates**
  - **16. Risks, migration, deferred work와 non-scope**
  - **17. Open questions와 traceability**

## 빠른 탐색 지도

이 문서는 독자가 **왜·무엇을 만들지 → 어떤 경계와 계약으로 만들지 → 어떻게 탐색·검증할지 → 어떤 evidence로 완료를 주장할지**의 순서로 이해하도록 구성했다.

1. 처음 읽을 때는 §1~§3에서 문서의 권위, 범위, 핵심 용어와 확정 결정을 확인한다.
2. 구현을 설계할 때는 §4를 먼저 읽어 입력부터 publication까지의 책임 경계와 데이터 흐름을 잡는다.
3. 구현 순서는 데이터가 변환되는 흐름대로 §5~§10(도메인·입력·평가·결과 모델), §11~§13(탐색·상태·실행 제어)을 따른다.
4. 결과를 외부에 사용하거나 비교하기 전에는 §14의 두 검증 gate를 적용한다.
5. 실제 착수·전환·후속 확장 판단은 §15~§17의 roadmap, migration, 질문 등록부를 기준으로 한다.

## 1. 이 문서의 목적, 독자와 사용법

### 1.1 목적과 대상 독자

이 문서는 **향후 RPDPTW 솔버를 구현할 개발자가 이 파일 하나에서 구현 대상의 전체 구조와 구현 순서를 잡을 수 있게 하는 Master 개발 설계서**다. 주 독자는 domain/input, evaluation, algorithm, verification/result, application integration을 구현하거나 review하는 개발자와 기술 책임자다.

이 문서를 읽은 개발자는 최소한 다음 질문에 답할 수 있어야 한다.

1. 외부 RPDPTW 입력이 어떤 단계를 거쳐 불변 solver problem과 bound profile이 되는가?
2. 초기해, ALNS, candidate state와 final result를 어떤 컴포넌트가 소유하는가?
3. 컴포넌트 사이에 어떤 데이터만 전달되며 의존 방향은 무엇인가?
4. 어떤 불변조건과 독립 verifier가 잘못된 해의 publication을 막는가?
5. 무엇을 어떤 선행 순서로 구현하고, 어떤 evidence가 있어야 각 단계가 끝나는가?

이 문서는 구현 완료 보고가 아니다. 현재 코드, 테스트, 배포 또는 benchmark가 아래 설계를 구현했다고 주장하지 않는다. 상태는 `REVIEW`이며 `APPROVED` 기준으로 인용할 수 없다.

2026-07-26 inventory 기준 상태를 다음처럼 구분한다.

| 구분 | 실제 상태 | 이 문서의 해석 |
|---|---|---|
| Current Java/GCP path | 입력 내용을 읽지 않고 합성 objective를 만드는 `AlnsBatchEngine` placeholder와 orchestration demo | Migration characterization 대상이며 ALNS 품질·feasibility evidence가 아님 |
| Target AWS runtime | S3 object storage, Step Functions durable orchestration, Lambda API/coordinator/worker compute | 선택된 target/reference runtime. 실제 AWS resource·IaC·solver 구현 완료 또는 production cutover evidence가 아님 |
| Target ALNS | 본 문서의 immutable domain, pair destroy/repair, COW, adaptive search와 verifier | 상세 설계 단계이며 구현·benchmark 완료가 아님 |
| Target route pool/MIP | §11.7~§11.10과 `RM-9A`~`RM-9C`의 optional hybrid contract; 활성화 시 exact backend는 Google OR-Tools CP-SAT | `C-17` gate 아래의 구현 가능한 설계. Backend 정책 선택은 구현·실험·production 활성화 승인이 아니며 version/config/native packaging 수치는 `OPEN` |

이후 “현재 기본”은 실제 placeholder가 그 동작을 한다는 뜻이 아니라 **승인될 목표 구현의 기본 계약**을 뜻한다. 실제 구현 상태를 주장할 때는 source/test/evidence inventory를 별도로 제시해야 한다.

### 1.2 이 문서로 할 수 있는 일과 할 수 없는 일

이 문서는 다음 용도로 사용한다.

- 솔버의 논리 컴포넌트, 책임과 입·출력 경계를 나눈다.
- 핵심 데이터가 입력에서 검증된 결과로 변하는 순서를 정한다.
- domain, policy, search, verification과 publication의 의존 방향을 정한다.
- 구현 phase의 선행 조건, 산출물과 완료 evidence를 정한다.
- 상세 계약의 공통 진입점과 변경 영향 범위를 제공한다.

이 문서만으로 다음을 확정해서는 안 된다.

- 구체 Java package/class/method, wire DTO, 저장 schema와 배포 topology
- AWS target의 세부 resource 이름, IAM/IaC, network·retention·cost/resource sizing 또는 production activation
- GCP/ECS/Kubernetes 등 미래 대체 후보의 채택
- 실험 결과가 없는 phase별 `maxSteps`, phase-2 worker/round 수와 watchdog
- 현재 코드가 목표 ALNS/MIP를 이미 구현했다는 주장
- Route pool/MIP의 production 활성화, 특정 optimizer 제품·라이선스와 공식 수치의 승인
- 현재 범위 밖의 multi-trip/rotation 또는 선택 변형

문서의 논리 컴포넌트명과 산출물명은 책임을 설명하는 구현 경계다. 사용자 답변으로 확정된 외부 field와 의미를 제외하면 그대로 API 이름이 되어야 한다는 뜻이 아니다.

### 1.3 독자별 상세 읽기 순서

| 독자의 목적 | 먼저 읽을 절 | 이어서 확인할 절 |
|---|---|---|
| 전체 구현 구조 파악 | §2 → §4 | §15 implementation roadmap |
| Domain/input 구현 | §4의 component map | §5~§8, §15의 `RM-1` |
| Evaluation/profile 구현 | §4의 dependency rule | §9, §15의 `RM-2` |
| Initial solution/ALNS 구현 | §4의 artifact flow | §6, §11~§13, §15의 `RM-3`~`RM-4` |
| Result/verifier 구현 | §4의 publication flow | §10, §14.1, §15의 `RM-5` |
| Benchmark 구현 | §4의 verified-result boundary | §13~§14, §15의 `RM-6` |
| Integration/migration 구현 | §4의 logical ports | §16.2, §15의 `RM-8` |
| 결정 상태·변경 영향 확인 | §3 | §17과 중앙 질문 등록부 |

§5~§14는 §4의 컴포넌트 계약을 상세화한다. §15는 그 컴포넌트를 구현하는 순서와 완료 evidence를 정한다. 상세 규칙을 읽을 때는 해당 절 첫 문장의 구현 phase 연결을 함께 적용해야 한다.

### 1.4 관련 문서의 역할

| 문서 | 역할 | 이 Master와의 관계 |
|---|---|---|
| 이 Master | 구현할 전체 시스템, 컴포넌트 책임, 의존 방향, 구현 순서와 phase gate | 개발자가 시작하는 단일 상위 설계 |
| [Domain Design](domain-design.md) | domain/input/normalization/travel/state/evaluation/result의 더 세밀한 규칙과 acceptance 사례 | Master 경계를 상세화하며 Master보다 높은 conflict authority를 갖지 않음 |
| [Architecture Design](architecture-design.md) | Java/Maven module, package DAG, optimizer backend와 runtime/port 배치 | Master/Domain 의미를 구현 구조에 배치하며 별도 semantic authority를 만들지 않음 |
| [질문 등록부](master-design-open-questions.md) | 28개 `Q-*`의 현재 상태, exact decision, evidence, owner와 gate | 질문 상태의 단일 색인 |
| [세션 29](master-design-sessions/29-open-question-interview.md) | 사용자 답변과 해석 원문 | 질문 결정의 권위 있는 답변 근거 |
| [세션 30](master-design-sessions/30-open-question-integration.md) | 세션 29 결정을 Master/register/역사 세션에 반영한 기록 | 통합 범위와 validation history |
| [세션 31](master-design-sessions/31-domain-design-integration.md) | Domain Design v2 재구성과 Master 정합화 기록 | 상세 문서 반영 history |
| 그 밖의 `master-design-sessions` | 조사, 초안, review와 과거 판단 기록 | 현재 구현 진입점이나 독립 규범 문서가 아님 |

Master는 “어떤 시스템을 어떤 순서로 구현하는가”를 소유하고, Domain Design은 “그 경계 안의 domain 의미를 어떻게 계산하고 검증하는가”를 상세화한다. 질문 등록부는 결정 상태만 소유한다. 세션 문서는 결정이 만들어지고 통합된 경위를 보존하며 Master의 구현 흐름을 대체하지 않는다.

### 1.5 규범어, 결정 상태와 충돌 처리

| 표기 | 의미 |
|---|---|
| **MUST / MUST NOT** | 구현과 검증이 반드시 지켜야 하는 계약 또는 불변조건 |
| **SHOULD / SHOULD NOT** | 특별한 근거와 검토가 있을 때만 이탈할 수 있는 잠정 또는 권장 방향 |
| **MAY** | 계약을 깨지 않는 선택 또는 확장점 |
| **TBD** | 중앙 질문의 결정 없이는 의미를 확정할 수 없음 |
| **DEFERRED** | 재개 조건과 별도 승인 전에는 현재 구현 범위가 아님 |
| **GATED TARGET** | 상세 target contract는 설계·review할 수 있지만 구현 착수와 production default 활성화는 명시된 gate·별도 승인 전 금지 |

결정 상태는 다음처럼 해석한다.

- **확정**: `C-*` 결정과 그 결정에 필수인 계약이다.
- **질문 결정**: 질문 등록부의 `RESOLVED` 질문과 세션 29에 기록된 사용자 답변이다.
- **잠정**: 사용자 결정으로 대체되지 않은 `P-*` 방향이다. 책임 경계는 검토 입력이지만 이름, API, 기본값 또는 활성 범위는 확정되지 않았다.
- **실험 대기**: `OPEN — EXPERIMENT_REQUIRED`다. 프로토콜은 결정되었지만 공식 수치는 승인된 실험 결과 전까지 만들 수 없다.
- **Gated target**: 의미·API 경계와 exit evidence를 구체화하되 predecessor evidence와 별도 scope approval 전에는 구현/활성화하지 않는다. `C-17` route pool/MIP가 여기에 해당한다.
- **보류**: `DEFERRED`이며 현재 roadmap을 막지 않고 질문하거나 활성화하지 않는다.

현재 질문 상태는 `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`이다. `Q-ALG-01`은 8개 초기해와 phase-1 선별 구조로 해결됐고, `Q-ALG-02`는 `RESOLVED — KEEP_COW`다. `Q-INFRA-01`은 2026-07-26 사용자 승인으로 AWS S3 + Step Functions + Lambda target/reference runtime을 선택했다. `Q-BENCH-02`만 수치 없는 실험 대기이며 `Q-VAR-01`만 보류다.

현재 `REVIEW` 단계에서 이 문서는 conflict resolver가 아니라 검토 제안이다. 승인된 기준과 충돌할 때 이 문서의 문장만으로 그 기준을 대체할 수 없다. 이 문서가 `APPROVED`가 된 뒤의 authority 순서는 다음과 같다.

1. 채택된 외부 입력·출력 계약과 승인된 Decision Record
2. `APPROVED` 상태의 이 Master
3. `APPROVED` 상태의 상세 설계
4. review input과 `master-design-sessions` 문서
5. 연구·원본 자료

최신 사용자 결정은 문서 반영 전에도 적용 범위를 명시한 override지만 관련 문서 전체를 자동 승인하지 않는다. 영향 범위를 식별하고 질문·Decision Record·Master·상세 문서를 같은 변경 단위에서 갱신해야 한다. 예시와 논문 설명은 어느 상태에서도 규범이 아니다.

## 2. 구현할 시스템과 완료 정의

### 2.1 해결할 문제

시스템은 **RPDPTW (Rich Pickup and Delivery Problem with Time Windows)** 입력을 정규화하고, request pickup-delivery pair와 hard constraint를 보존하는 해를 생성·개선한 뒤, 독립 검증된 결과와 provenance를 발행해야 한다.

입력은 고객사별 schema, sparse travel data, vehicle와 request 의미를 포함할 수 있다. 출력은 검색 중 우연히 남은 상태가 아니라 모든 input request의 검증된 `ASSIGNED`/`UNASSIGNED` partition, route, metric, objective, diagnostic과 실행 lineage다.

가장 중요한 아키텍처 목표는 고객사마다 다른 목적과 제약을 **최소 코어 변경**으로 조립하는 것이다. 새 고객사의 단가, 제약, 중립 지표 또는 목적 순서가 공통 route state나 ALNS에 고객사 분기를 추가하게 해서는 안 된다. 실제 물리 진행 의미가 기존 경계로 표현되지 않을 때만 좁고 검증 가능한 common seam을 확장한다.

### 2.2 구현 범위

현재 roadmap은 다음 구현 단위를 포함한다.

- versioned adapter와 immutable normalized problem
- solver 시작 전 complete travel data를 만드는 Travel Matrix preparation
- request pair, vehicle, terminal, physical location과 directed travel을 사용하는 hard feasibility
- 고객사별 constraint, neutral metric, score, objective, `SolvePlan`과 immutable profile binding
- 4개 request-route 성장 정책과 2개 vehicle 순서를 조합한 8개 initial portfolio, phase-1 ALNS 선별과 phase-2 병렬 ALNS 개선
- request-pair ALNS, bounded improvement와 fleet attempt
- **`[C-17 GATED TARGET]`** 수락 여부와 분리된 hard-feasible route 수집, immutable route pool snapshot과 deterministic merge
- **`[C-17 GATED TARGET]`** Solver-neutral route-selection projection, exact set-partition 후보와 ALNS incumbent fallback
- **`[C-17 GATED TARGET]`** Worker-local ALNS segment → route pool seal → optional MIP selection → full-evaluated next warm start의 inner hybrid phase
- copy-on-write candidate state, cache 무효화와 rollback/discard
- step 기반 정상 종료와 watchdog/cancellation/resource/failure 분리
- search state와 final result의 분리 및 evidence-bounded diagnostic
- 서로 독립되고 cache-free인 candidate solution verifier와 result-integrity verifier
- `data/win_poc_case.json`을 입력으로 삼는 Win PoC benchmark 계약
- 고정 manifest의 logical round/worker fan-out과 verified champion fan-in
- provider/product에 독립적인 application port와 migration gate

### 2.3 명시적 비범위

- Route pool/MIP를 production 기본값으로 활성화하거나 특정 상용 optimizer·라이선스를 승인하는 일
- Route pool pruning 상한, MIP work/time budget과 official hybrid schedule의 실험 없는 고정
- MDVRP, OVRP, SDVRP 등 선택 변형 구현
- 구체 cloud/provider/product/runtime, 실행·저장 service 또는 배포 단위
- 실시간 동적 routing, 교통정보 결합, geocoding과 주소 정제
- 승인되지 않은 multi-trip/rotation
- 공식 수치가 없는 phase별 `maxSteps`, worker/round 수, watchdog과 성능 threshold
- compliant integer `D/U`를 다시 받기 전 현재 Win fixture의 official baseline 사용

### 2.4 구현 완료의 의미

여기서 구현 완료는 class를 만들거나 happy path를 한 번 실행했다는 뜻이 아니다. **입력 의미부터 공개 가능한 결과와 benchmark 비교까지의 모든 권위 경계가, 정상 사례와 실패 사례 모두에서 계약대로 작동한다는 재현 가능한 evidence가 있을 때** 이 Master의 목표를 충족한다. 아래 조건은 점수처럼 일부만 충족해도 되는 목록이 아니라 모두 필요한 AND gate다.

| 완료 조건 | 구현이 보장해야 하는 것 | 필요한 evidence | 이것만으로는 부족한 것 |
|---|---|---|---|
| 1. 안정 해의 구조적 정확성 | 모든 committed solution에서 request는 완전한 pickup-delivery pair로 route 또는 `SearchRequestBank` 중 한 곳에만 있고, same-vehicle·precedence·terminal·single-trip·load/time hard constraint를 만족한다. | 정상 삽입·제거·교환뿐 아니라 거절, 예외, 취소와 rollback 뒤에도 partition과 route feasibility가 보존되는 property/fault test. | 대표 입력에서 route 하나가 feasible하게 나온 것, 또는 infeasible insertion을 단순히 거절한 것. |
| 2. 입력·travel·profile의 권위 고정 | Adapter가 해석한 business meaning, complete prepared directed travel, exact customer profile/preset과 algorithm config가 solve 전에 immutable snapshot·version·fingerprint·provenance로 결합된다. Search와 verifier는 raw input, `latest` profile, lazy travel fallback을 다시 해석하지 않는다. | 제공/생성 travel, adapter coercion, profile dependency와 version이 lineage에 남고, missing·ambiguous·fingerprint mismatch 입력이 solve 전에 거부되는 test. | JSON을 파싱해 candidate를 만들 수 있는 것, 또는 실행 중 누락 arc/profile을 비슷한 값으로 보완하는 것. |
| 3. 고객별 정책의 격리 | 고객 차이는 constraint·metric·score·comparator·`SolvePlan` profile에서 조립되고, common propagation, pair invariant, candidate state와 ALNS core의 물리 의미는 고객 이름이나 preset에 따라 바뀌지 않는다. | 동일 problem facts에 서로 다른 승인 profile을 bind해 각각의 결과와 dependency closure를 검증하고, unknown/cross-customer fallback을 거부하는 test. | profile마다 `if (customer == ...)` 분기를 core에 추가해 우연히 요구를 통과시키는 것. |
| 4. 계산의 진실성 | Route sequence, vehicle/terminal binding과 bank가 source of truth이며 cache·incremental aggregate·objective는 버리고 다시 계산할 수 있는 파생값이다. 어느 cache 상태에서도 feasibility, metric, score와 objective가 full recomputation과 같다. | 삽입, acceptance, best 선택과 finalization에서 cache-free 재계산 동등성; stale/poisoned cache, cache hit/miss와 rollback fault injection. | 성능이 좋은 cache, 또는 정상 경로에서만 cache 값이 맞는 것. |
| 5. 공개 결과의 두 단계 검증 | Candidate solution verifier가 route/bank와 authoritative travel에서 candidate를 독립 검증한 뒤에만 finalization이 시작한다. Result-integrity verifier는 final outcome, diagnostic/audit, summary와 payload를 다시 검증한다. 둘 다 `PASS`인 결과만 정상 publication과 benchmark vector가 된다. | 손상된 route, bank, travel, metric, objective, outcome, summary와 payload가 각 gate에서 거부되고, `FAIL`/미완료가 publication을 막는 test. | solver의 feasible flag·summary·cache를 verifier가 신뢰하는 것, 또는 첫 verifier만 통과한 결과를 공개하는 것. |
| 6. 정상 실행의 재현성 | 고정된 problem/travel/profile/config/build, seed lineage, step budget과 stable order에서 정상 종료한 실행은 같은 trace·solution·result fingerprint를 만든다. Watchdog, cancellation, resource limit과 failure는 정상 품질 종료와 구분된다. | fixed reproducibility envelope의 반복 실행에서 completed step/round, champion, solution/result fingerprint가 일치하는 trace; 중단된 step이 state를 전진시키지 않는 fault test. | seed만 같게 두는 것, 또는 시간 제한으로 우연히 같은 결과가 나온 한 번의 실행. |
| 7. Win benchmark 비교의 정당성 | Official baseline과 challenger는 완전히 같은 immutable manifest의 fixture, contracts, profile, build, seed/step, metric과 verifier를 사용하며, 선언된 모든 worker가 정상 완료·검증된 champion만 비교한다. | complete-batch fan-in, retry identity, incomplete worker 차단, completion-order 독립성, 동일 manifest 재실행과 exact comparator 사례. | 일부 성공 worker의 가장 좋은 해, 다른 fingerprint의 결과, 또는 현재 비준수 fixture로 만든 baseline. |
| 8. roadmap gate의 실제 종료 | 각 `RM-*` phase는 entry condition을 충족하고 다음 phase가 소비할 deliverable을 만들며, §15의 exit evidence로 계약과 failure handling을 입증한다. | phase별 evidence bundle과 traceability: 특히 `RM-1`~`RM-5` end-to-end verified result, `RM-6` official workflow, `RM-8` cutover/rollback rehearsal. | source file, test file, mock, demo 화면 또는 happy-path test가 존재하는 것만으로 phase를 `DONE` 처리하는 것. |
| 9. Optional hybrid recombination의 진실성 | 활성화한 경우 pool의 모든 artifact는 immutable exact route이고, selector는 profile 의미를 exact column으로 투영하거나 명시적으로 skip하며, materialization/full evaluation 뒤 strictly better candidate만 warm start가 된다. | Rejected-route harvest, deterministic pool merge/dominance, tiny exact-model oracle, status/native/conversion fault fallback, adopted-only feedback와 incumbent fingerprint 보존. | Backend objective를 반환한 것, raw selected route를 이어 붙인 것, 또는 ALNS보다 한 사례에서 좋아진 것. |

완료 주장은 목적에 따라 필요한 gate를 구분해야 한다.

- **정상 publishable solver result**는 최소 `RM-1`~`RM-5`의 실제 권위 산출물과 두 verifier `PASS`를 요구한다.
- **Win PoC official baseline 또는 challenger 비교**는 여기에 `RM-6`의 compliant integer fixture와 `Q-BENCH-02` 승인 수치, complete verified worker evidence를 추가로 요구한다.
- **실제 application cutover**는 여기에 `RM-8`의 semantic compatibility, idempotency/cancellation과 rollback evidence를 추가로 요구한다.
- `RM-7`은 COW를 apply/undo로 바꾸기 위한 자동 관문이 아니다. COW 유지도 profiling evidence가 있으면 정상적인 완료 결과다.
- `RM-9A`~`RM-9C`는 route pool/MIP의 구현 가능한 상세 target이지만 `C-17`에 따라 자동 착수·production 활성화 항목은 아니다. Verified ALNS baseline, OR-Tools CP-SAT dependency/config/native packaging/SBOM, security/operations/compute-cost/admission/fallback/rollback 검토와 별도 scope approval을 받아야 한다.

따라서 “최적해를 찾았다”, “benchmark 숫자가 좋아졌다”, “API가 응답한다”는 각각 일부 관측일 뿐 구현 완료 증명은 아니다. 완료 evidence는 정확성, 의미 보존, 독립 검증, 재현성, 비교 가능성과 운영 전환 안전성을 함께 보여야 한다.

## 3. 용어와 결정 요약

### 3.1 표준 용어

| 용어 | 규범 의미 |
|---|---|
| RPDPTW | 프로젝트의 표준 Rich Pickup and Delivery Problem with Time Windows 모델 |
| PDPTW | 논문과 학술 문제를 가리키는 원래 문제명 |
| `Request` | pickup 하나와 delivery 하나를 갖는 원자적 운송 의무 |
| `Feature` | 지역의 대형 차량 진입 제한에 쓰는 차량 크기 유형. 예: 1t, 3t, 5t |
| capability / qualification | 냉장, lift, 위험물 자격, 기사 자격 등 `Feature`와 분리된 확장 constraint 축 |
| hard feasibility | 위반 후보를 허용 가능한 해로 인정하지 않는 규칙 |
| neutral fact / metric | 발생한 물리·구조 사실이며 가격이나 선호를 포함하지 않음 |
| score policy | feasible 사실을 비용 또는 soft penalty로 변환하는 정책 |
| objective comparator | feasible 해의 ordered dimension을 의미적으로 비교하는 계약 |
| `SolvePlan` | stage 순서, warm-start, 선행 목표 보호와 budget reference를 조정하는 상위 계약 |
| `SearchRequestBank` | 탐색 중 정규 route에 없는 request ID membership |
| final outcome | 검증된 최종 request의 `ASSIGNED` 또는 `UNASSIGNED` partition |
| evaluated route artifact | 하나의 hard-feasible immutable route와 authoritative evaluation을 보존하는 projection-independent pool 항목 |
| projected route column | evaluated route artifact를 특정 exact projection으로 변환한 request/vehicle/resource/objective 계수와 stable ID |
| route pool | 동일 solve authority에 속하는 evaluated route artifact를 deterministic merge한 저장소. Live pool이 아니라 sealed snapshot만 selector에 전달 |
| hybrid phase | 한 worker 안의 ALNS segment, worker-local pool seal, optional route selection, reconstruction·full evaluation과 champion adoption을 하나의 commit 경계로 묶은 실행 |

### 3.2 확정 결정 색인

| ID | Master에 통합된 의미 |
|---|---|
| `C-01` | 표준 프로젝트 용어는 RPDPTW이며 학술 PDPTW와 구분한다. |
| `C-02` | Master는 구현 완료 보고가 아닌 미래 개발의 규범 설계다. |
| `C-03` | 고객사 변화에 대한 유지보수성과 최소 코어 변경이 최우선이다. |
| `C-04` | hard feasibility, 중립 측정, score, objective 비교와 stage 실행을 분리한다. |
| `C-05` | `Feature`는 vehicle size type이고 capability/qualification은 별도 축이다. |
| `C-06` | request는 same-vehicle, exactly-once, precedence, route/bank XOR를 갖는 atomic pair다. |
| `C-07` | delivery-only 출발 적재와 실제 pickup-delivery의 물리 의미를 구분한다. |
| `C-08` | 정상 품질 종료는 step limit이고 시간은 watchdog이다. |
| `C-09` | 현재 기본 candidate state는 changed-route copy-on-write와 독립 bank다. apply/undo는 측정된 COW 병목과 별도 변경 승인 전에는 기본 경로가 아니다. |
| `C-10` | 소수 물리량은 변환 경계에서 fixed-point integer로 정규화한다. |
| `C-11` | 무게·부피는 `n=3`, 비음수 `FLOOR`, item-first 정규화 후 `qty` 곱을 사용한다. 비용·거리·시간은 정수 입력이며 소수 입력을 거부한다. |
| `C-12` | 기본 volume은 999 CBM, 시간의 유한 상한은 plan end이며 실패는 별도 상태다. |
| `C-13` | 제공된 `D` meter/`U` second를 우선하고, 명시적 preparation이 누락 `D/U`를 승인 산식으로 생성한 뒤 solver/verifier에 complete travel data를 전달한다. |
| `C-14` | `ro_input_json_spec.pdf`는 legacy CVRPTW 사례이지 canonical RPDPTW schema가 아니다. |
| `C-15` | search bank의 membership과 final `ASSIGNED`/`UNASSIGNED` outcome·diagnostic을 분리한다. 차량의 직영/외주 소유 유형은 outcome status가 아니다. |
| `C-16` | 4개 request-route 성장 정책과 2개 `DIRECT`-first vehicle 순서를 조합해 최대 8개 initial solution을 만든다. 각 후보는 fixed `screenMaxSteps` ALNS를 거친 뒤 comparator상 phase-1 champion 하나를 고르고, phase 2는 그 champion을 공통 warm start로 사용한다. |
| `C-17` | Route pool/MIP는 **GATED TARGET**이다. Solver-neutral target contract, fallback과 phase gate는 설계·review할 수 있지만 `RM-9A`~`RM-9C`의 predecessor evidence와 별도 scope approval 전에는 구현 착수와 production default 활성화를 금지한다. 이 gate가 나중에 열릴 때 exact route-selection backend는 **Google OR-Tools CP-SAT**이며 MPSolver나 상용 optimizer로 자동 대체하지 않는다. 이 backend 정책은 Phase 13 activation/acceptance가 아니고 dependency version, time/work limit, worker 수와 seed 값은 별도 승인 전 `OPEN`이다. |
| `C-18` | Win PoC 전용 네 성분 comparator는 미배정 수, 배차 차량 수, 전체 거리, 전체 운영시간 순이며, official run은 고정 round plan의 완결된 verified champion을 사용한다. |
| `C-19` | 선택 변형 문제는 구현이 아니라 deferred feasibility work다. |
| `C-20` | 선택된 target/reference topology는 **AWS S3 + Step Functions + Lambda**다. Master는 logical port와 semantic contract를 소유하며 AWS SDK, ARN/event DTO, resource/IaC 세부를 소유하지 않는다. SDK는 adapter/deployment 경계에만 위치하고 provider 구현은 worker/round completion, retry identity, candidate/result verification 또는 publication 의미를 바꾸지 않는다. |
| `C-21` | 독립 verifier를 통과하지 않은 후보는 정상 publication 또는 benchmark 대상이 아니다. |
| `C-22` | 강한 재현성은 고정 fingerprint/seed/order/step 아래 모든 worker가 계획 step을 완료하고 coordinator가 `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED`로 정상 종료한 실행에 한정한다. |

### 3.3 잠정 설계 색인

아래 항목은 확정 결정으로 승격하지 않는다.

| ID | 잠정 방향 | 아직 확정하지 않는 것 |
|---|---|---|
| `P-01` | delivery-only와 real pickup-delivery의 service pattern을 구분 | 최종 타입명, prefix node 대 initial-load 표현 |
| `P-02` | delivery-only v1의 virtual pickup을 route 시작 prefix에 고정 | depot 작업시간 단위, 외부 노출, mixed-route 규칙 |
| `P-03` | preparation 완료 후 complete travel data를 표현하는 내부 구조 | 공통 `U`와 차량별 생성 `U`의 최종 타입/API 및 저장 표현 |
| `P-04` | `SolvePlan`은 orchestration, objective schema/score는 평가 vector | 최종 타입명과 별도 objective-plan 타입 필요 여부 |
| `P-05` | 장기 profile registry/factory와 solve별 immutable binding 분리 | 정확한 factory API, lifecycle 구현과 등록 방식 |
| `P-06` | propagation facts와 metric contributor/snapshot seam | 최종 표현과 공통 metric 승격 목록 |
| `P-07` | 선행 objective를 기본적으로 no-worse-than-best로 보호 | tolerance와 탐색 중 relaxation |
| `P-08` | **대체됨:** solver outcome은 `ASSIGNED`/`UNASSIGNED`이고 직영/외주는 vehicle ownership으로 구분 | 운영자의 후속 외주·이월은 solver result status가 아님 |
| `P-09` | diagnostic을 code, scope, confidence, source, evidence로 구조화 | 공개 code, 확장 code 노출과 audit 범위 |
| `P-10` | construction/phase runner의 registry·manifest·artifact 타입 | 고정 step 수, worker/round 수와 cloud adapter의 최종 API |
| `P-11` | **기본 경로 아님:** 측정된 COW 병목이 있을 때만 apply/undo 재제안 가능 | 별도 실험·동등성 증거·변경 승인 없는 전환 금지 |
| `P-12` | versioned legacy adapter의 제한된 coercion과 alias 허용 | 허용 목록, canonical JSON과 unknown-field 정책 |
| `P-13` | benchmark manifest/card에 fixture, round/worker plan과 전 계약 fingerprint 보존 | round/worker 수, `maxSteps`, watchdog은 `Q-BENCH-02` 실험 대기 |
| `P-14` | 실행 경계를 논리 port로 분리 | transport, orchestrator, artifact store와 배포 단위 |
| `P-15` | Route pool은 exact route identity와 dominance bucket을 분리하고 append/import에 같은 deterministic merge를 적용 | Pool cap, aging, Pareto pruning과 persistence 정책 |
| `P-16` | Corrected route-selection 후보는 request별 explicit unassigned 변수와 실제 vehicle consumption을 가진 exact set partition | `SET_PARTITION_EXACT`의 production 기본 승격과 profile별 projection 지원 범위 |
| `P-17` | `SET_COVER_THEN_CONVERT`는 OGC 2024 differential/compatibility 실험에만 격리 | Legacy converter의 장기 유지 여부 |
| `P-18` | MIP no-incumbent, OR-Tools CP-SAT native/runtime 초기화·solve failure, invalid reconstruction 또는 non-improvement이면 해당 phase의 cache-free validated ALNS incumbent를 보존 | MIP-required product mode와 degraded-result 외부 노출 정책 |
| `P-19` | Worker-local inner hybrid를 reference-adaptation baseline으로 사용 | Cross-worker pool fan-in과 중앙 route selection은 별도 scalability ADR/evidence 뒤 검토 |
| `P-20` | Corrected ALNS는 중앙 pair editor, versioned operator registry, promising shortlist→exact insertion과 step-based acceptance/adaptation을 사용 | Production operator subset과 수치 default |

## 4. 구현 아키텍처와 책임 경계

이 절의 컴포넌트와 산출물 이름은 구체 API 승인이 아니라 **독립적으로 구현·검증할 책임 단위**다. 구현은 이름을 바꿀 수 있지만 책임을 합쳐 verifier 독립성, profile 격리 또는 의존 방향을 깨서는 안 된다.

### 4.1 입력에서 publication까지의 전체 흐름

```text
external submission
  → versioned adapter
  → canonical business input
  → normalization ─┬→ immutable problem facts
                   └→ travel preparation → complete prepared travel
  → exact profile/config binding
  → immutable solve snapshot
  → pair evaluator + 8개 initial portfolio
  → phase-1 screen ALNS
  → phase-1 champion
  → phase-2 parallel ALNS workers
      → [optional hybrid worker] ALNS segment
      → validated route collection + immutable pool snapshot
      → solver-neutral route selection
      → reconstruction + authoritative full evaluation
      → ALNS incumbent와 selector candidate 중 full-evaluated adoption
  → complete worker fan-in
  → committed solve best
  → committed candidate
  → candidate solution verifier
  → verified solution
  → preliminary request partition
  → required final-solution insertion audit
  → final outcomes + diagnostics + outcome-derived summary
  → result-integrity verifier
  → publishable verified result
  → optional official benchmark comparison
```

위 흐름에서 앞 단계의 산출물은 다음 단계의 유일한 의미 입력이다. 뒤 단계가 raw input을 다시 해석하거나 앞 단계의 정책을 자체 default로 보완해서는 안 된다. Cancellation은 실행 중 협력 signal로 전달되고 미완료 candidate의 discard를 요구한다. Lineage는 input, prepared travel, profile, run, candidate, verifier report와 result identity를 끝까지 연결한다.

### 4.2 단계별 데이터 계약

| 단계 | 권위 입력 | 반드시 만드는 산출물 | 실패 시 경계 |
|---|---|---|---|
| 접수·adapter | External bytes/reference, schema version, requested profile/config identity | Canonical business input, raw digest, alias/coercion provenance | 모호한 schema·alias·reference는 solve 전 거부 |
| Normalization | Canonical business input | Dense immutable domain facts, numeric/time/service/compatibility policies와 provenance | 잘못된 단위·경계·pair/reference·overflow는 input error |
| Travel preparation | Raw travel/coordinates, normalized locations/vehicles, travel policy | Complete directed distance와 모든 사용 vehicle의 resolved time, source별 provenance와 fingerprint | 필수 pair를 해소하지 못하면 solve 시작 금지 |
| Profile binding | Normalized facts, exact customer/profile/preset/config version | Immutable bound constraints/metrics/score/comparator/`SolvePlan` snapshot | Unknown/latest fallback, missing dependency와 unit mismatch 금지 |
| Portfolio construction | Immutable solve snapshot, exact request/vehicle policy matrix | 최대 8개의 independent initial candidates와 route artifacts | 불변조건·full evaluation 검증 실패 후보 제외 |
| Phase-1 screen | Initial candidate, exact screen ALNS config | 각 candidate의 screen best와 phase-1 champion | 미완료·미검증 candidate는 champion 비교 제외 |
| Phase-2 execution | Phase-1 champion, batch worker configs | Worker별 ALNS 또는 optional hybrid champion, round champion과 provenance | 미완료 worker가 있으면 round champion 확정 금지 |
| Route collection | Completed hard-feasible route, exact evaluation, discovery lineage | `RoutePoolDelta`; append/import가 같은 merge 규칙을 쓴 worker-local pool | Infeasible·aborted·rollback 중 route 수집 금지 |
| Pool sealing | Incumbent-pinned pool, exact authority fingerprints, dominance policy | Stable-sorted immutable evaluated-artifact `RoutePoolSnapshot`과 digest | Live/mutable pool, 다른 solve/profile artifact 혼합 금지 |
| Route selection | Pool snapshot, exact projection, feasible warm start, explicit budget/backend config | Provider-neutral status/termination/provenance, incumbent presence와 selected `ProjectedColumnId` only | Status/incumbent 확인 전 값 접근, backend 객체 노출 금지 |
| Reconstruction/adoption | Selected projected columns, optional conversion policy, phase ALNS incumbent | Fresh materialization, authoritative full evaluation과 comparator adoption/fallback record | Raw MIP objective·중간 cover를 candidate로 취급 금지 |
| Candidate verification | Problem/profile declaration, route order, bank, prepared travel | Cache-free `PASS` report와 verified solution 또는 `FAIL` | `PASS` 없이는 finalization 중단 |
| Finalization·audit | Verified solution, immutable request universe, approved diagnostic sources | Exactly-one outcomes, required audit record, bounded diagnostics, outcome-derived summary | Audit 미완료·confidence 과장은 result verification 실패 |
| Result verification | Candidate `PASS`, verified solution, outcomes/audit/summary/payload | Result-integrity `PASS`, canonical result/payload fingerprint | `PASS` 없이는 정상 publication 금지 |
| Publication·retrieval | 두 verifier의 `PASS`와 immutable lineage | Verified result/status/artifact reference | 미검증·불완전 payload를 정상 결과처럼 노출 금지 |
| Official benchmark | Publishable verified results, identical approved manifest | 완결 round champion lineage와 final comparison record | 선언 worker 하나라도 미완료면 `INCOMPLETE` |

### 4.3 논리 컴포넌트와 책임

| 논리 컴포넌트 | 구현 책임 | 의존하는 것 | 의존하거나 결정하면 안 되는 것 |
|---|---|---|---|
| Submission/Input port | 접수 identity, idempotency 의미, requested exact profile/config와 input lineage 전달 | 외부 application contract | Raw field의 업무 의미, transport 제품 |
| Solve execution port | Immutable solve snapshot과 explicit run config를 받아 portfolio/search를 실행하고 committed candidate와 termination provenance 반환 | Profile binder, portfolio와 ALNS engine | External result status, storage topology, 미검증 route publication |
| Cancellation port | 취소 의도를 협력 signal로 전달하고 미완료 mutation discard와 실제 종료 상태 보존 | Application orchestration과 ALNS safe point | Watchdog/resource/failure를 cancellation으로 이름 변경 |
| Versioned adapter | Schema/version 선택, 제한된 alias/coercion, syntax/reference validation | Submission과 승인된 input contract | Search, objective, silent fallback |
| Normalizer | Numeric, time, service, ID/location, size/capability/zone 의미와 static facts 동결 | Canonical input | 고객 가격, ALNS, result status |
| Travel Matrix preparer | Provided/generated `D/U`, complete coverage, source provenance와 fingerprint | Normalized locations/vehicles와 travel policy | Search 중 lazy generation, reverse/directed 의미 변경 |
| Profile binder | Exact customer profile/preset의 constraint/metric/score/comparator/plan dependency closure | Immutable normalized facts | Raw input 재해석, 다른 고객 profile fallback |
| Route propagator | Load, time, window, travel, stop/resource의 물리 진행과 hard fact 계산 | Problem, prepared travel, bound hard policy | 가격, 최종 diagnostic, search acceptance |
| Atomic pair evaluator | 한 request pair의 합법 insertion option을 side-effect 없이 평가 | Propagator, bound profile | Committed state mutation, partial pair |
| Portfolio builder | 4 request-route 정책 × 2 vehicle 순서의 독립 candidate 생성과 route artifact 기록 | Pair evaluator, explicit policy matrix | 고객사 분기, policy별 feasibility 중복 구현 |
| Phase coordinator | Phase-1 screen fan-in, phase-2 worker batch fan-out/fan-in, stable champion·plateau 판정 | Comparator, cache-free validation, exact run configs | AWS SDK, worker 완료 순서 의존, 일부 성공 worker champion |
| COW candidate state | Changed-route copy, 독립 bank, cache invalidation과 commit/discard 격리 | Immutable problem/profile과 committed snapshot | Best/current 직접 mutation, 기본 apply/undo |
| ALNS engine | Pair destroy/repair, stage guard, acceptance, adaptive update와 step accounting | Pair evaluator, COW state, bound `SolvePlan` | 고객사 분기, wall-clock quality termination |
| Route collector/pool | Acceptance와 독립적으로 completed hard-feasible route를 projection-independent immutable artifact로 수집하고 merge·pin·seal | Core full evaluator와 stable route identity | Pool route mutation, scalar-cost-only dominance, vendor solver |
| Route-selection projection | Bound profile의 request/vehicle/resource/objective 의미를 exact column model로 투영 가능 여부와 계수로 변환 | Bound evaluation declaration | Hidden Big-M, 비가산 objective의 무음 근사 |
| Route-selection solver port | Immutable model spec/projected columns/warm start/budget을 받아 provider-neutral status/termination/provenance, incumbent presence와 projected-column ID만 반환 | Solver backend adapter | Domain route mutation, final feasibility 판정, raw backend status 노출 |
| Reconstruction/adoption | Cover conversion이 필요하면 새 route로 변환하고 full evaluation 뒤 ALNS incumbent와 비교 | Core evaluator, bound comparator | Pool alias mutation, solver `ObjVal` 권위화 |
| Candidate solution verifier | Candidate 구조·feasibility·metric·objective를 cache 없이 전체 재계산 | Immutable declaration과 prepared travel | Solver feasibility flag, search cache/summary 신뢰 |
| Result finalizer/auditor | Preliminary partition, required exhaustive insertion audit, final outcomes/diagnostics/summary 생성 | Verified solution과 approved evidence sources | Search bank를 final status로 직렬화, 자동 수정·재탐색 |
| Result-integrity verifier | Outcome partition, audit confidence, summary와 payload identity를 cache 없이 검증 | Candidate `PASS`, verified solution, final result artifacts | Candidate verifier 역할 대체, solver summary 신뢰 |
| Publication gateway | 두 `PASS`를 확인하고 verified result만 상태/artifact로 노출 | Result-integrity `PASS`와 lineage | Storage 제품 선택, fail/incomplete payload 정상 발행 |
| Result retrieval port | Verified result와 incomplete/invalid/recovery 상태를 구분하여 조회 계약으로 노출 | Publication status와 immutable artifact identity | 미검증 candidate를 정상 result로 공개 |
| Benchmark coordinator | Fixed logical round/worker 실행, verified champion fan-in과 next-round lineage | Solve execution port, verifier, manifest | Provider/product topology, 일부 성공 worker로 champion 확정 |

Candidate verifier와 result-integrity verifier는 구현 코드 경로와 authority input이 분리되어야 한다. 같은 계산 library를 공유할 수 있더라도 search cache, solver summary 또는 finalizer의 판정을 그대로 신뢰하는 shortcut은 허용하지 않는다.

### 4.4 의존 방향

허용되는 상위 의존은 다음과 같다.

```text
external adapters / infrastructure implementations
  → application orchestration and logical ports
    → domain + normalization + prepared travel
    → bound evaluation contracts
    → portfolio/search
    → independent verification/result contracts
```

구체적으로 다음을 지킨다.

1. Core는 transport, storage, 인증, cloud SDK 또는 runtime DTO를 참조하지 않는다.
2. Adapter는 표현을 바꿀 수 있지만 pair invariant, travel 의미, comparator와 termination 의미를 바꿀 수 없다.
3. Algorithm은 normalized problem과 bound interfaces만 보고 customer ID나 raw field를 분기하지 않는다.
4. Evaluation은 propagation fact를 소비하며 route/raw input을 다시 해석하지 않는다.
5. Finalization은 verified solution을 소비하며 search bank나 last failure를 final truth로 사용하지 않는다.
6. Publication은 verifier 판정을 소비하며 자체 feasibility나 summary를 추정하지 않는다.
7. Infrastructure는 logical port를 구현할 뿐 round/worker, idempotency, cancellation과 result 의미를 역으로 정의하지 않는다.

선택된 target/reference topology는 [Q-INFRA-01](master-design-open-questions.md#q-infra-01)의 AWS S3 + Step Functions + Lambda다. GCP Java/deployment path는 migration characterization inventory로 보존하며 target topology로 읽지 않는다. Logical port와 test double은 AWS adapter와 local runner 모두에 적용되고, AWS SDK·event DTO·resource locator는 adapter/deployment 밖으로 나갈 수 없다. ECS, GCP, Kubernetes 등은 같은 contract/parity evidence와 별도 승인 뒤의 대체 후보일 뿐 현재 기본 target이 아니다.

### 4.5 상태와 산출물의 생명주기

| 생명주기 | 변경 가능성 | 소유자 | 다음 단계에 전달할 것 |
|---|---|---|---|
| Raw/canonical input | Adapter 단계에서만 변환 | Adapter | Raw digest, 적용 alias/coercion과 canonical meaning |
| Normalized problem/prepared travel | 생성 뒤 immutable | Normalizer/Travel preparer | Dense mapping, policies, source provenance와 fingerprints |
| Bound profile/plan | Solve별 생성 뒤 immutable | Profile binder | Exact dependency closure, versions와 config fingerprint |
| Search candidate | 한 step 내부에서만 mutable | COW candidate state | Accept 시 freeze한 committed snapshot, reject/interruption 시 아무것도 전달하지 않음 |
| Current/stageBest/solveBest | Immutable snapshot 교체만 허용 | ALNS execution | Route/bank source of truth와 execution lineage |
| Route pool delta/live pool | 한 worker의 hybrid execution 안에서만 누적 | Route collector | Exact-evaluated immutable route artifacts와 discovery lineage |
| Route pool snapshot | Seal 뒤 immutable | Hybrid worker/application | Stable artifacts, incumbent pin, merge/pruning policy와 fingerprint |
| Route-selection outcome | Solver 호출 뒤 immutable evidence | Solver adapter | Status/termination/provenance와 selected route IDs only |
| Materialized/evaluated selector candidate | 한 adoption 시도 동안만 존재 | Hybrid orchestration | Fresh routes/bank, authoritative full evaluation와 conversion record |
| Verified solution | Candidate verifier `PASS` 뒤 immutable | Candidate verifier | Recomputed feasibility, metrics, objective와 identity |
| Final outcomes/audit | Finalization 동안 생성 후 result verifier에 전달 | Finalizer/auditor | Exactly-one partition, evidence-bounded diagnostics와 summary |
| Publishable result | Result-integrity `PASS` 뒤 immutable | Result verifier/publication | Payload fingerprint, both verifier reports와 full lineage |

Search cache, insertion table, aggregate와 solver summary는 어느 생명주기에서도 source of truth가 아니다. 의심되면 폐기하고 권위 입력에서 재계산한다.

### 4.6 구현을 지배하는 불변조건

모든 컴포넌트는 다음 cross-cutting invariant를 공유한다. 상세 규칙과 evidence 위치는 괄호의 절을 따른다.

- **Pair partition:** 모든 stable search state에서 request는 same-route complete pair 또는 bank membership 중 정확히 하나다 (§6).
- **Physical feasibility:** 모든 route는 terminal, load prefix, directed travel, time/window/resource와 bound hard constraint를 만족한다 (§5~§9).
- **Atomicity:** Pair mutation은 성공 시 관련 route/bank/cache를 함께 바꾸고 실패·중단 시 관찰 가능한 상태를 남기지 않는다 (§6, §12).
- **Policy separation:** Hard feasibility, neutral metric, score, comparator와 `SolvePlan`을 서로 대체하지 않는다 (§9).
- **State/result separation:** Search bank는 membership이고 final outcome은 candidate `PASS` 뒤 finalization이 만든다 (§10).
- **Pool isolation:** Pool artifact는 완전한 pair와 hard-feasible route만 포함하며, pool/projected-column/cover artifact는 stable solution이나 final outcome이 아니다 (§11.7~§11.10).
- **Selector safety:** Raw MIP incumbent는 reconstruction, authoritative recomputation, candidate verification과 comparator adoption을 우회할 수 없다 (§11.8~§11.10).
- **Independent verification:** Search와 finalization의 주장만으로 publication하지 않으며 두 verifier의 `PASS`가 필요하다 (§14.1).
- **Deterministic normal completion:** 동일한 고정 envelope와 정상 step 종료는 동일한 canonical 결과를 만든다 (§13).
- **No hidden official value:** 실험 대기 항목은 explicit test/experiment config만 사용하고 production default를 만들지 않는다 (§11.2, §14.4, §17.1).

### 4.7 구현 순서 한눈에 보기

상세 phase 계약은 §15에 있다. 개발 순서는 단순 기능 나열이 아니라 **다음 단계가 신뢰할 권위 산출물을 먼저 만드는 순서**다.

| 순서 | 구현 목표 | 선행 권위 산출물 | 다음 단계에 넘길 것 | 완료 evidence 요약 |
|---|---|---|---|---|
| `RM-0` | 결정·문서 baseline 고정 | 없음 | 적용 결정, 질문 상태와 traceability | 결정 coverage, link/lint, 과도한 완료 주장 없음 |
| `RM-1` | Input/domain/travel snapshot 구축 | `RM-0` | Immutable problem과 complete prepared travel | Hand calculation, boundary/overflow, pair/location/matrix property evidence |
| `RM-2` | Evaluation/profile binding 구축 | `RM-1` | Immutable bound profile/comparator/plan | Layer isolation, dependency rejection, full evaluation/comparator evidence |
| `RM-3` | Pair evaluator와 8개 initial portfolio 구축 | `RM-2` | Independent initial candidates와 route artifacts | Policy-matrix trace, evaluator/full equality, rollback/lineage |
| `RM-4` | Two-phase COW ALNS와 termination 구축 | `RM-3` | Phase-1 champion, phase-2 solve best와 reproducibility record | Fault injection, COW isolation, batch fan-in, cache equality, normal deterministic rerun |
| `RM-5` | 두 verifier와 final result 구축 | `RM-4` | Publishable verified result 또는 explicit rejection | Corruption rejection, audit completeness, complete outcome partition, gate별 rejection |
| `RM-6` | Official Win workflow 구축 | `RM-5` + 실험 승인 + compliant fixture | Immutable manifest와 verified final champion | 모든 worker normal completion, exact comparator, rerun/approval |
| `RM-7` | COW baseline profiling | `RM-4`~`RM-6`의 측정 가능한 경로 | COW 유지 판정 또는 별도 변경 제안 | Measured bottleneck과 correctness/reproducibility evidence |
| `RM-8` | Logical-port integration과 migration | Verified 선행 core/result | Versioned adapter/cutover/rollback evidence | Compatibility, idempotency/cancellation, shadow와 rollback |
| `RM-9A~C` | Optional route pool→selector→hybrid feedback | `RM-4`, `RM-5` + scope/solver gate | Pool/model/selection/hybrid artifacts와 activation 판정 | Deterministic pool, tiny-model oracle, status/fallback, adopted-only feedback와 shadow evidence |

`RM-8`의 port interface와 test double은 `RM-0` 뒤 병행할 수 있다. 다만 실제 cutover와 정상 publication은 `RM-1`~`RM-5`를 우회할 수 없다. `RM-6`은 `Q-BENCH-02`의 승인된 실험 수치와 compliant integer travel fixture 없이는 official baseline을 발행할 수 없다.

## 5. Canonical RPDPTW 도메인 모델

이 절은 `RM-1`이 만들어야 하는 immutable domain의 의미를 정하고 `RM-2`~`RM-5`가 공유하는 vocabulary를 제공한다. 세부 field/type 선택은 [Domain Design](domain-design.md)을 따르되 아래 의미를 바꿀 수 없다.

### 5.1 핵심 개념

| 개념 | 규범 의미 |
|---|---|
| `Request` | immutable pickup node와 delivery node를 참조하는 atomic pair |
| pickup node | 물량이 차량 적재에 들어오는 논리 작업 |
| delivery node | 같은 물량이 차량 적재에서 빠지는 논리 작업 |
| vehicle | 용량, 하나의 vehicle size type, `vhclOwnTyp`, terminal 정책, 근무와 route-resource 데이터를 가진 자원 |
| terminal | route의 고정 start/end solver node |
| physical location | directed matrix endpoint. solver node와 별도 identity/index를 가짐 |
| route | 한 vehicle에 결합된 single-trip ordered sequence. `oneway`는 start terminal에서 마지막 고객까지, `roundtrip`은 start terminal에서 같은 depot 복귀까지다. |
| solution | 안정 route 집합과 독립 `SearchRequestBank`의 request partition |

외부 ID와 조밀한 core ID의 양방향 mapping은 정규화 결과에 포함하고 problem 생성 후 바꾸지 않는다. 여러 solver node가 같은 physical location을 참조할 수 있으며, 같은 위치라도 pickup, delivery와 terminal 역할은 합치지 않는다.

### 5.2 Service meaning

하나의 RPDPTW core를 사용하되 다음 의미를 보존해야 한다.

| Pattern 의미 | pickup | 규범 조건 |
|---|---|---|
| delivery-only | route 출발 전에 준비된 화물의 논리적 start loading | 같은 route의 모든 delivery-only demand 합이 initial load이며 첫 고객 뒤의 중간 재적재로 용량을 우회할 수 없음 |
| real pickup-delivery | 입력에 존재하는 실제 pickup 작업 | 같은 route에서 delivery보다 먼저 실제 위치·시간·service를 수행 |

Delivery-only와 real pickup-delivery는 같은 single-trip route에 섞을 수 있다. 시작 load는 모든 delivery-only demand의 합이고 delivery-only 고객에서 감소한다. 실제 pickup에서는 증가하고 그 delivery에서 감소한다. 모든 route prefix에서 각 load 차원이 `0 <= load <= capacity`를 만족해야 하며, delivery-only 논리 pickup은 travel, stop 또는 depot 재방문을 만들지 않는다.

Delivery-only의 prefix node 대 initial-load 내부 표현과 이름은 잠정이지만 어느 표현도 위 물리 의미를 바꿀 수 없다. `oneway`와 depot으로 한 번 복귀하는 `roundtrip + multiRotation=0`은 현재 single-trip 범위다. 같은 차량이 depot 복귀 뒤 재출발하는 multi-trip/rotation은 후속 범위이며, 활성화하더라도 pickup-delivery pair는 하나의 trip 안에서 완료해야 한다.

### 5.3 Vehicle size와 capability

입력의 `Feature`는 vehicle size type이다. Vehicle은 구체 코드 하나를 `vehicleFeature`로 받고, order는 허용 코드 배열을 `vehicleFeatureList`로 받는다.

```text
sizeCompatible
= vehicle.vehicleSizeType
  IN request.allowedVehicleSizeTypes
```

Request 측 size 값은 허용 대안의 집합이고 vehicle 측은 단일 유형이다. 코드는 비어 있지 않은 자유 형식 문자열이고 대소문자를 구분한 exact equality로 비교한다. 고정 allowlist를 두거나 문자열의 숫자·순서에서 톤급 관계를 추론하지 않는다. Order의 정확한 `["ALL"]`은 모든 vehicle을 허용하며 구체 코드와 섞을 수 없다.

일반 capability/qualification은 별도 subset 축이다.

```text
capabilityCompatible
= request.requiredCapabilities
  SUBSET OF vehicle.vehicleCapabilities
```

전용 차량과 zone도 별도 hard constraint다. Missing/null/empty zone은 `"ALL"`로 정규화한다. Route가 방문하는 `zoneId != "ALL"` 집합의 cardinality는 최대 1이어야 한다. 구체 zone vehicle은 같은 zone과 `ALL` order만, zone-neutral vehicle은 한 route에서 하나의 구체 zone과 `ALL` order만 방문할 수 있다. Size와 zone은 AND로 적용한다. 실제 pickup/delivery의 size 목록은 교집합을 사용하고 두 실제 작업의 구체 zone이 다르면 배정할 수 없다. Delivery-only의 depot 논리 pickup은 zone 방문이 아니다.

Vehicle ownership은 optional `vhclOwnTyp`으로 표현한다. Missing/null/empty는 `DIRECT`, 허용값은 대소문자를 구분한 `DIRECT`와 `LEASE`뿐이다. 이 축은 request assignment status와 분리한다.

## 6. 핵심 불변조건과 atomic mutation

이 절은 `RM-1`의 domain validation, `RM-3`의 pair evaluator, `RM-4`의 mutation/rollback과 `RM-5`의 verifier가 같은 기준으로 구현해야 하는 invariant다.

### 6.1 Request partition

모든 안정 상태에서 각 request는 정확히 하나의 상태여야 한다.

```text
ASSIGNED_IN_SEARCH
= pickup과 delivery가 같은 vehicle route에 각각 정확히 한 번 존재
  AND pickup index < delivery index
  AND request가 SearchRequestBank에 없음

UNASSIGNED_IN_SEARCH
= pickup과 delivery가 모든 route에 없음
  AND request가 SearchRequestBank에 정확히 한 번 존재
```

두 상태는 XOR이다. 다음은 구현 결함이며 정상적인 infeasible 후보가 아니다.

- pickup 또는 delivery만 존재하는 partial pair
- 한 node 또는 pair의 중복
- pair가 서로 다른 vehicle route에 분리됨
- delivery가 pickup보다 앞섬
- route와 bank 동시 membership
- route와 bank 양쪽에서 누락

### 6.2 Route와 feasibility

각 안정 route는 다음을 MUST 만족한다.

1. 정확히 하나의 vehicle에 결합한다.
2. 첫 node는 그 vehicle의 start terminal이다. `oneway`의 마지막 node는 마지막 service node이고, `roundtrip`의 마지막 node는 같은 depot/end terminal이다.
3. 현재 single-trip에서는 내부 depot 재방문과 다른 vehicle의 terminal을 허용하지 않는다.
4. 승인된 service pattern을 보존한다.
5. 모든 load 차원에서 `0 <= load <= capacity`다.
6. 실제 directed matrix, 승인된 time contract와 route-resource rule로 hard-feasible하다.
7. 표준 single-trip의 완성 route는 request pair의 순변화가 0이어야 한다.
8. 고객 service location이 직전 service location과 다를 때만 stop count를 1 증가시키며 depot은 세지 않는다. `maxStopCnt`는 route 전체 누적이고 vehicle/global 한도가 모두 있으면 `min`을 포함 상한으로 사용한다.
9. 실제 통과 arc의 `D`와 vehicle-resolved `U`만 route 전체 `driveDist`/`driveTime`에 누적하며 대기·서비스·근무창 사이 휴식은 제외한다. 명시된 한도가 없으면 해당 추가 hard constraint가 없다.

### 6.3 Atomic mutation

Insert, remove, relocate, exchange, destroy/repair, route elimination과 rollback은 request pair를 최소 전이 단위로 사용해야 한다.

- 성공하면 pickup, delivery, route와 bank를 함께 갱신하고 영향받은 cache/aggregate/fingerprint를 무효화하거나 재계산한다.
- 실패, 거절, 중단 또는 예외면 route sequence, bank, fleet state, cache visibility, score/objective aggregate와 fingerprint가 호출 전과 같아야 한다.
- Evaluator, comparator, observer, best snapshot과 result conversion은 안정 상태만 볼 수 있다.
- 구조 불변조건 실패를 낮은 score, insertion infeasibility 또는 unassignment reason으로 숨겨서는 안 된다.

## 7. 입력, 정규화와 domain value 계약

이 절은 `RM-1`의 adapter/normalizer 구현 계약이다. 이 단계의 산출물이 immutable problem으로 동결된 뒤에는 search, verifier와 result가 raw input을 다시 해석해서는 안 된다.

### 7.1 Adapter 경계

외부 입력을 core 타입으로 직접 역직렬화하지 않는다.

```text
external bytes/reference
→ schema/version adapter
→ syntax/reference validation
→ business meaning
→ canonical business model
→ numeric/time/location/compatibility normalization
→ immutable ProblemInstance + provenance
```

Versioned legacy adapter는 승인된 제한적 coercion/alias만 처리할 수 있다. 적용한 해석을 provenance에 기록하고, 모호하거나 지원되지 않는 의미는 solve 전에 거부한다.

[`data/ro_input_json_spec.pdf`](../data/ro_input_json_spec.pdf)는 legacy CVRPTW 입력 사례다. 표와 예시가 충돌하고 RPDPTW의 matrix·pair 계약을 제공하지 않으므로 canonical schema로 복제해서는 안 된다. [`data/win_poc_case.json`](../data/win_poc_case.json)도 read-only fixture이지 canonical schema나 정답 해가 아니다.

### 7.2 Fixed-point와 checked arithmetic

소수 물리량은 변환 경계에서 정확한 10진수로 읽어 fixed-point integer로 정규화한다.

- 무게와 부피는 `n=3`이며 비음수 원문 값을 소수 셋째 자리까지 `FLOOR`한다.
- 각 item을 먼저 정규화한 뒤 그 정수값에 양의 정수 `qty`를 곱한다. Decimal line 합계를 먼저 만든 뒤 한 번만 내리지 않는다.
- 비용, 거리와 시간은 `n=0`의 정수 입력 계약이다. 소수 입력을 절삭·반올림하지 않고 입력 오류로 거부한다.
- Core feasibility와 누적에는 floating tolerance를 사용하지 않는다.
- 같은 차원의 demand와 capacity, travel과 resource limit는 같은 unit/scale/rounding policy를 사용한다.
- 정규화, quantity 곱, route/solution 합산은 checked arithmetic을 사용한다.
- Overflow, 비유한 값 또는 계산 실패를 큰 numeric value로 치환하지 않는다.
- Policy ID/version, 단위, scale, rounding과 adapter version을 fingerprint에 포함한다.

### 7.3 Planning period와 time

Frontend/backend는 timezone과 UTC offset 변환을 solver 밖에서 처리한다. Backend는 solver에 timezone/offset 없는 exact `yyyy-MM-dd HH:mm:ss` 문자열을 전달한다. Solver adapter는 이를 parsing하여 planning origin 기준 `long` second로 바꾸고, core는 timezone, DST와 문자열을 다루지 않는다. Solver에 전달되는 모든 planning/window 값은 이미 같은 전역 고정 시간 기준으로 정렬되어 있어야 한다. 그 전역 timezone의 이름은 solver default나 fingerprint 값으로 추정하지 않는다.

정규화된 problem은 정확한 plan start와 plan end를 가져야 한다. Planning period는 `planStart <= t < planEnd`인 반개구간이다. Node, terminal과 vehicle window는 같은 단조 `long` second 축을 사용한다.

- Plan end는 유한한 허용 상한이지 계산 실패 표현이 아니다.
- Plan end와 같은 event는 현재 plan에 포함하지 않는다.
- Time-window close는 포함 경계다.
- Feasible time 값과 `INFEASIBLE(reason)` 상태는 논리적으로 분리한다.
- 계산 실패에 numeric sentinel을 사용하거나 초과값을 plan end로 clamp하지 않는다.

고객 `openTime/closeTime`은 실제 영업시간이다. `serviceStart = max(arrival, openTime)`, `waitingTime = serviceStart - arrival`, `serviceEnd = departure = serviceStart + serviceTime`으로 계산한다. Waiting time은 중립 metric이다. 기본 `START_ONLY` profile은 `serviceStart <= closeTime`을 요구하므로 close와 같은 service start와 close 이후 departure를 허용한다. Profile은 `COMPLETE_WITHIN_WINDOW`를 선택하여 `serviceEnd <= closeTime`을 요구할 수 있다.

날짜 없는 고객/depot/vehicle window는 plan에 포함되는 각 날짜에 반복한다. `openTime > closeTime`은 당일 open부터 다음 날 close까지의 하나의 overnight window다. 전개 결과는 `[planStart, planEnd)`로 clip한다. 이동은 `departure + fullTravelTime <= currentWorkEnd`일 때만 시작한다. 현재 근무창에 전체 arc가 들어가지 않지만 plan 안에 다음 반복 근무창이 있으면 현재 위치에서 쉬고 다음 `workStart`에 전체 arc를 처음부터 시작한다. Arc 중간 pause/resume은 금지한다. 전체 arc가 어떤 단일 이용 가능 근무창에도 들어가지 않으면 infeasible이다.

Legacy `reqDate`와 `dueDate`는 같은 완료기한 별칭이며 `serviceStart <= serviceEnd <= reqDate(dueDate)`를 만족해야 한다. `duration`은 order/request 수준 진입 등 고정 서비스시간이고, `item.taskTime`은 item 한 단위 선적시간이다.

```text
serviceTime
= request.duration
  + Σ(item.taskTime × item.qty)
```

Order-level `taskTime`은 item에 배분하거나 `duration` 별칭으로 바꾸지 않고 입력 오류로 거부한다. Depot `taskTime`은 적용하지 않는다. Depot `duration`은 후속 rotation에서 한 trip 복귀 뒤 다음 trip 재출발 전에는 적용하지만 최초 출발과 마지막 복귀에는 적용하지 않는다.

`trips=oneway`이면 `multiRotation` 값은 비권위 입력으로 무시하고 depot에서 한 번 출발해 마지막 고객에서 끝난다. 그 외 `multiRotation != 0`은 rotation 기능 승인 전 `UNSUPPORTED_INPUT`이다. `roundtrip + multiRotation=0`은 같은 depot으로 한 번 복귀하는 현재 single-trip이다.

`waitInDepot=N`은 가능한 가장 이른 `max(vehicleWorkStart, depotOpen)` 출발을 사용한다. `waitInDepot=Y`는 `max(earliestDeparture, firstCustomerOpen - travelTime)`으로 동일한 조기 대기를 depot으로 옮긴다. Depot/customer waiting은 별도 metric이며 이 출발 정책은 feasibility를 완화하지 않는다.

`maxStopCnt`, `maxDriveTime`, `maxDriveDist`는 route 전체 누적이고 날짜·근무창 사이 휴식에서 reset하지 않는다. 차량별 stop 한도와 전역 한도가 함께 있으면 둘 다 hard constraint이며 `min`을 사용한다. 누락 한도는 명시적 “제약 없음”이며 큰 numeric sentinel로 바꾸지 않는다.

### 7.4 Constants와 유한 기본값

부피 차원을 사용하지 않는다고 adapter가 명시적으로 정규화한 경우 vehicle volume capacity의 기본값은 **999 CBM**이다.

- 999 CBM은 유한한 값이며 무제한 표현이 아니다.
- 실제 volume demand/capacity가 있으면 덮어쓰거나 clamp하지 않는다.
- 적용 출처와 policy version을 provenance에 남긴다.
- Core는 정규화 뒤 이 값을 일반 유한 capacity로 다룬다.

Tunable은 의미별 configuration이 소유해야 한다. 의미별 static default, solve별 immutable configuration snapshot과 instance value를 분리하고 출처·version을 보존한다. 범용 constants 공간에 고객 가격, plan 값, 탐색 상태 또는 platform 설정을 섞지 않는다.

### 7.5 Static compatibility

Adapter/transformer와 bound route constraint는 다음 규칙으로 `servableVehicles`와 route zone state를 만든다.

- `vehicle.vehicleFeature`는 missing/null/empty/`"ALL"`을 거부하고 구체 자유 형식 코드 하나를 요구한다.
- 신규 order는 하나 이상의 구체 코드 또는 정확히 `["ALL"]`인 `vehicleFeatureList`를 사용한다. Missing/null/empty와 `ALL` 혼합은 거부한다.
- Legacy 배열형 `order.vehicleFeature`는 alias로 읽되 신규 필드와 함께 있으면 exact list equality를 요구한다. Legacy 단일 문자열 order field는 거부한다.
- Size code는 registry에 없어도 유효하며 대소문자를 구분한 exact equality로 비교한다. Fleet에 일치 차량이 없으면 해당 request의 배정 가능 차량이 없는 것이다.
- Size와 zone은 독립 hard constraint로 AND한다. 실제 pickup/delivery size 목록은 교집합을 사용한다.
- Vehicle/order zone의 missing/null/empty는 `"ALL"`로 정규화하고, `ALL`을 제외한 route의 구체 zone 집합은 최대 하나다.
- 실제 pickup과 delivery의 구체 zone이 다르면 배정할 수 없다. Delivery-only의 depot 논리 pickup은 zone 검사에서 제외한다.

## 8. Directed distance/time matrix 계약

이 절은 `RM-1`의 Travel Matrix preparer 계약이다. 완료된 prepared travel fingerprint가 `RM-3`~`RM-6`의 solver, verifier와 benchmark가 공유하는 유일한 travel authority다.

외부 production input은 sparse directed arc 또는 matrix 전체 생략을 허용한다. Solver 시작 전의 명시적 Travel Matrix preparation이 모든 physical location의 directed `M²` pair와 모든 사용 vehicle의 travel time을 해소한다.

```text
raw provided/generated travel sources
→ Travel Matrix preparation
→ complete physical-location directed distances
  + provided common or generated vehicle-resolved travel times
→ solver node location lookup
→ solver + verifier
```

다음을 MUST 지킨다.

1. Travel key는 request/node ID가 아니라 physical location ID다.
2. 모든 solver node는 유효한 location mapping을 갖고 preparation은 모든 directed physical-location pair를 열거한다.
3. 제공된 `D`는 authoritative directed meter, 제공된 `U`는 vehicle-independent authoritative directed second다. 둘 다 정수만 허용하고 소수값은 거부한다.
4. Legacy `C`는 비권위 필드이며 feasibility, score 또는 generation 분기에 사용하지 않는다.
5. 모든 self arc는 입력값과 관계없이 `D=0 meter`, `U=0 second`로 정규화한다.
6. 누락 `D`는 좌표 기반 Great Circle로 계산하고 `HALF_UP`으로 가장 가까운 정수 meter를 만든다. 필요한 좌표가 없으면 solve 전 입력 오류다.
7. 누락 `U`는 vehicle별 `CEILING(D_meter × 3.6 ÷ speed_km_h)` 정수 second로 만든다. Vehicle 속도가 없으면 `45 km/h`를 사용한다.
8. 제공된 common `U`와 vehicle별 생성 `U`를 구분하고 solver 시작 전 모든 사용 vehicle/pair의 시간이 해소되어야 한다.
9. Reverse arc 복사, 대칭 평균 또는 탐색 중 lazy generation을 금지한다.
10. Raw input, provided/generated source, generation policy, unit, coverage, diagonal policy와 raw/prepared fingerprint를 보존한다.
11. Solver core와 verifier는 같은 준비 완료 travel data만 사용하고 좌표·속도 계산을 반복하지 않는다.

현재 [`data/win_poc_case.json`](../data/win_poc_case.json)의 소수 `D/U`는 이 정수 계약에 비준수다. 정수 matrix를 다시 받거나 별도 명시적 계약 변경이 있기 전에는 canonical normalization, official baseline 또는 official benchmark에 사용하지 않는다.

## 9. Extensible policy, evaluation과 profile architecture

이 절은 `RM-2`의 route propagation, evaluation layer와 profile binding 계약이다. `RM-3` 이후 algorithm은 여기서 bind된 interface만 소비하고 고객사 의미를 직접 해석하지 않는다.

이 절의 `facts`, contributor/snapshot, objective schema/score, profile registry와 `SolvePlan`은 책임을 설명하는 잠정 라벨이다 (`P-04`~`P-06`). 구현은 아래 책임 분리를 지켜야 하지만 최종 타입명, API와 내부 표현은 아직 확정하지 않는다.

### 9.1 단방향 평가 구조

```text
normalized immutable facts
→ propagation + structural/static hard gates
→ policy-neutral facts and metrics
→ composed hard constraints
→ score components
→ objective schema/comparator
→ SolvePlan stages
```

각 책임은 다음을 MUST 지킨다.

| 책임 | 소유하는 의미 | 금지 |
|---|---|---|
| normalization | ID, unit, time, travel, service meaning, static compatibility | 고객 가격·목적 순서 |
| propagator | route 진행의 물리 사실과 explicit feasibility | 단가·최종 진단 |
| hard constraint | 후보 허용 가능성 | 유한 penalty로 위반 상쇄 |
| neutral metric | 발생량과 사실 | 좋고 나쁨, 가격, 최종 원인 |
| score policy | feasible 사실의 비용·soft penalty | route/raw input 재해석 |
| objective comparator | ordered dimension과 방향 | stage 실행, 물리 재계산 |
| `SolvePlan` | stage, warm-start, guard와 budget reference | hard rule 해제 |

Hard-infeasible 후보는 score, acceptance, temperature 또는 reward로 feasible이 될 수 없다. 같은 우선순위에서 고객이 교환 가능하다고 명시한 비용만 scalar로 합성한다. 양보할 수 없는 우선순위는 ordered dimensions로 비교하며 숨은 Big-M scalar로 평탄화하지 않는다.

### 9.2 Neutral metrics와 policy extension

공통 물리 집계만 common metrics에 둔다. 선택적인 고객사 지표는 잠정 contributor/snapshot seam으로 propagation facts에서 계산한다. Contributor는 가격이나 feasibility를 반환하지 않으며 raw route나 input을 재해석하지 않는다.

새 요구는 다음의 가장 좁은 경계에 둔다.

```text
input meaning
→ static compatibility
→ bound route/solution hard constraint
→ neutral metric contributor
→ score component
→ objective/comparator
→ SolvePlan/profile composition
```

기존 facts로 실제 물리 상태를 표현할 수 없고, 단가나 우선순위 문제가 아니며, 기존 profile과 verifier의 의미를 보존할 좁은 seam이 있을 때만 propagator/fact contract 확장을 검토한다.

### 9.3 Profile binding과 lifecycle

장기 profile definition/registry와 solve별 immutable bound profile을 분리하는 방향은 잠정이다. 어떤 구체 이름을 사용하든 다음 lifecycle은 MUST 지킨다.

- exact profile key/version/config를 resolve하고 unknown/latest fallback을 금지한다.
- 정규화된 dense ID, unit과 fact contract에 constraint/metric/score/objective/plan을 bind한다.
- Missing dependency, duplicate score key, unit/schema mismatch와 unknown reference를 solve 전에 거부한다.
- Bound profile은 생성 후 불변이고 request-indexed array는 한 problem에만 속한다.
- Evaluation scratch, route cache, adaptive state와 telemetry는 evaluation/run/seed 사이에 공유하지 않는다.
- Problem, profile config, constraint/metric/score/comparator/plan versions를 결과 fingerprint로 전달한다.

Solve 요청은 해당 고객사에 등록·승인된 objective preset만 선택할 수 있다. 요청이 objective 순서·가중치·수식을 직접 주입하거나 다른 고객사의 preset을 선택할 수 없다. Preset 생략 시 그 고객사 설정에 exact key/version으로 지정된 default를 사용한다. Unknown/unauthorized preset, 해당 고객사에 없는 objective 또는 unmet metric dependency는 bind 전에 거부하며 비슷한 preset으로 fallback하지 않는다.

특정 objective dimension은 일부 고객사에만 존재할 수 있다. Mandatory 의미를 사용하는 preset은 `mandatoryUnassignedCount`를 최상위 사전식 objective로 둔다. 이는 hard constraint나 유한 Big-M penalty가 아니므로 0이 불가능해도 최소 양수의 verified partial solution을 반환할 수 있다.

입력 fleet의 `DIRECT`와 `LEASE` vehicle은 모두 실제 배정 resource다. Solver가 fleet 밖의 공급자를 만들거나 미배정 request를 외주/이월로 추정하지 않는다. Customer objective의 기본/선택 구조는 다음 의미를 보존한다.

```text
optional mandatoryUnassignedCount
→ totalUnassignedCount
→ optional outsourcedVehicleVolumeCost
→ regularVehicleVolumeCost
→ remaining customer objectives
```

- `regularVehicleVolumeCost`는 사용한 `DIRECT` vehicle별 `maxVolume`을 차량당 한 번 합산해 최소화한다.
- `LEASE`를 허용하는 고객 preset은 사용한 `LEASE` vehicle별 `maxVolume` 합인 `outsourcedVehicleVolumeCost`를 더 앞선 차원으로 추가한다.
- `LEASE` vehicle이 입력되었는데 bound preset이 외주 objective를 지원하지 않으면 solve 전 binding 오류다.
- 고정 `1:100` scalar나 음수 score 예시로 strict priority를 흉내 내지 않는다.

## 10. Search solution과 final result

이 절은 `RM-4`의 search membership과 `RM-5`의 verified outcome 사이 경계를 정한다. Bank 직렬화가 아니라 candidate verification과 finalization을 통해서만 최종 결과를 만든다.

### 10.1 Search-time state

`SearchRequestBank`는 request ID membership만 보유한다.

- Pair insertion 성공 후 request를 제거한다.
- Pair destroy 성공 후 request를 추가한다.
- Candidate copy, acceptance, rejection과 rollback에 routes와 함께 참여한다.
- Pickup/delivery node membership, 비용, last failure, final status, diagnostic, deferred/outsourced 의미를 저장하지 않는다.

한 insertion failure나 반복된 rejection은 최종 reason이 아니다. Search telemetry가 필요하면 bank와 분리된 bounded record로 수집한다.

### 10.2 Finalization과 result

최종 결과는 검색 state의 직렬화가 아니다.

```text
committed candidate + immutable inputs + provenance
→ candidate solution verifier
→ verifier PASS report + verified solution
→ preliminary ASSIGNED/UNASSIGNED partition
→ required final-solution insertion audit
→ final outcomes/structured diagnostics
→ post-finalization result-integrity verifier
→ publication
```

Candidate solution verifier는 immutable problem/profile declaration, candidate route/node order, bank와 normalized matrix만으로 candidate를 cache-free 검증한다. `PASS` report와 verified solution이 없으면 finalization은 게시 가능한 outcome을 만들 수 없다.

모든 입력 request는 결과에 정확히 하나의 outcome을 가져야 한다. `ASSIGNED`는 exactly one verified route/vehicle/pair를 참조하고 `UNASSIGNED`는 route를 참조할 수 없다. `DIRECT`와 `LEASE` route에 배정된 request는 모두 `ASSIGNED`이며 ownership은 vehicle reference에서 구분한다. 입력 fleet 어디에도 배정되지 않은 request만 `UNASSIGNED`다. Solver는 운영자의 후속 외주·이월을 `OUTSOURCED` 또는 `DEFERRED` outcome으로 생성하지 않는다. Summary count는 outcome에서 파생한다.

Normalization/static precheck로 순서·탐색과 무관한 불가능성이 증명된 `UNASSIGNED` request는 `PROVEN` diagnostic을 사용할 수 있고 중복 insertion audit를 생략한다. 그 밖의 모든 `UNASSIGNED` request는 publication 전에 final routes를 고정한 채 모든 eligible vehicle과 합법 pickup/delivery position pair를 검사한다.

- 모든 option이 실패한 경우에만 `EXHAUSTIVE_FOR_FINAL_SOLUTION`과 `NO_FEASIBLE_INSERTION_IN_FINAL_SOLUTION` 계열 진단을 사용할 수 있다.
- 이 confidence는 현재 final routes 기준이며 전역 재배치 불가능성 증명이 아니다.
- Audit가 feasible insertion을 찾으면 자동 삽입, 재호출 또는 재탐색하지 않고 outcome은 `UNASSIGNED`로 게시할 수 있다.
- Feasible insertion 발견은 내부 audit record에만 남기고 외부 outcome에는 일반 `UNASSIGNED`와 별도로 성립하는 proven/search-observed/`UNKNOWN` 근거만 노출한다.
- Audit contract/version, 검사 vehicle/position 수, constraint별 rejection count, completion, work와 elapsed를 기록하고 ALNS step/quality vector와 분리한다.

Diagnostic code/scope/confidence/source/evidence의 정확한 wire 표현은 잠정이지만 실제 source 범위를 넘는 confidence를 만들 수 없다.

Post-finalization result-integrity verifier는 candidate verifier의 `PASS` report와 verified solution, final outcomes, diagnostic source/audit evidence, summary와 publishable payload를 입력으로 받아 outcome partition, vehicle ownership reference, audit completeness/confidence ceiling, summary와 payload fingerprint를 검증한다. 두 verifier 모두 search cache와 solver summary를 권위 입력으로 거부한다. Search telemetry가 diagnostic evidence로 허용되더라도 §10.1의 bounded record와 실제 source 범위를 넘는 confidence를 만들 수 없다. 어느 verifier든 `FAIL`이거나 미완료이면 정상 route/outcome payload와 benchmark vector publication을 거부한다.

### 10.3 Result provenance

게시 가능한 result는 최소한 다음 의미를 역추적할 수 있어야 한다.

- input digest, adapter와 normalized problem
- numeric/time/matrix policy
- profile, constraints, metrics, score, objective, comparator와 `SolvePlan`
- portfolio source, seeds, operators, algorithm/state strategy와 build/runtime compatibility
- stage별 requested/completed steps와 별도 inner-work counters
- termination과 상위 execution 상태
- canonical solution/result fingerprint
- 두 verifier의 contract/version, 판정과 bounded evidence

Run 종료와 정상 solution publication은 별도 상태다. 서로 다른 run의 route와 metric 최솟값을 합쳐 가상 결과를 만들지 않는다.

## 11. Initial portfolio, ALNS와 route-selection pipeline

§11.1~§11.2는 `RM-3`, §11.3~§11.6은 `RM-4`, §11.7~§11.10은 gated `RM-9A`~`RM-9C`의 주 구현 계약이다. 모든 phase는 같은 atomic pair evaluator와 bound comparator를 사용해야 한다.

### 11.1 공통 pair evaluator

Construction, repair, paired move와 fleet reinsertion은 하나의 atomic pair insertion 의미를 사용해야 한다.

```text
stable solution + bank request + target route + pickup/delivery positions
→ structural/service-pattern gate
→ servableVehicles gate
→ hard-feasibility propagation
→ feasible option facts/metrics/ranking values
```

Evaluator는 committed state를 바꾸지 않는다. 선택된 move를 atomic 적용한 뒤 cache-free route 재계산과 같은 결과를 내야 한다. Feasible option만 ranking에 들어가며 마지막 total order는 stable request/vehicle/route/position identity로 결정한다.

### 11.2 현재 범위의 initial-solution portfolio

초기 portfolio는 4개의 request-route 성장 정책과 2개의 vehicle 순서를 조합한 최대 8개 independent construction을 MUST 생성한다. 각 조합은 route/bank/cache/random state를 공유하지 않는다.

| Request-route 성장 정책 | 규범 역할 |
|---|---|
| `CLOCK` | 선택 vehicle의 start terminal/depot을 원점으로, 0도에서 clockwise로 request entry location을 순회해 route를 성장. depot 또는 request entry location의 좌표가 없으면 이 조합은 `UNAVAILABLE`이다. |
| `SEQ_FARTHEST` | depot에서 가장 먼 request를 seed로 선택하고, 이후 현재 route의 마지막 확정 service location에서 가까운 request를 우선한다. |
| `SEQ_LARGE_DEMAND` | 선택 vehicle 대비 weight/volume utilization이 큰 request를 seed로 선택하고, 동률이면 이른 `reqDate`, stable request ID 순으로 결정한 뒤 가까운 request로 route를 성장한다. |
| `SEQ_EARLIEST_DEADLINE` | 가장 이른 `reqDate` request를 seed로 선택하고, 동률이면 utilization이 큰 request, stable request ID 순으로 결정한 뒤 가까운 request로 route를 성장한다. |

두 vehicle 순서는 모두 feasible `DIRECT` vehicle을 `LEASE`보다 먼저 시도한다. `DIRECT_FIRST_LARGE`는 현재 candidate request에 대해 낮은 utilization을, `DIRECT_FIRST_SMALL`은 높은 utilization을 우선한다. 여기서 `utilization = max(requestWeight / vehicleWeightCapacity, requestVolume / vehicleVolumeCapacity)`이며, 두 capacity dimension의 정확한 missing/zero 처리와 동점 순서는 bound policy version에 포함한다. `Feature` 문자열의 숫자·나열 순서로 크기를 추론하지 않는다.

정책은 request/vehicle 후보 순서만 정한다. 각 request는 공통 atomic pair evaluator를 통과해 pickup/delivery, time window, capacity, travel, zone과 capability를 모두 만족할 때만 삽입한다. “가까움”은 prepared directed distance `D[currentServiceLocation][request.entryLocation]`를 뜻하며, 실제 pickup-delivery request의 entry location은 pickup, delivery-only request의 entry location은 delivery다. 후보별 route는 source policy, vehicle policy, ordered sequence, metric과 fingerprint를 artifact로 기록한다.

### 11.3 Phase-1 screen과 phase-2 ALNS

사용 가능한 initial candidate 각각은 exact `screenMaxSteps`로 독립 ALNS screen을 실행한다. `screenMaxSteps`는 품질 예산이며 wall-clock 1분은 watchdog/관측값일 뿐 종료 조건이 아니다. 모든 screen candidate가 정상 step 종료와 cache-free validation을 통과한 뒤 comparator로 하나의 **phase-1 champion**을 고른다.

Phase 2는 phase-1 champion을 공통 warm start로 사용한다. 각 round는 서로 다른 derived seed와 destroy/repair/operator config를 가진 worker batch를 실행하고, worker마다 exact `phase2MaxSteps`를 완료한다. worker 완료 순서와 병렬성은 champion에 영향을 주지 않는다.

```text
phase-1 champion
→ phase-2 worker batch
→ all worker cache-free validation
→ stable round champion fan-in
→ compare(roundChampion, previousChampion)
→ STRICTLY_BETTER: 다음 round의 공통 warm start
→ EQUAL 또는 WORSE: NO_STRICT_IMPROVEMENT 종료
→ maxRounds 도달: MAX_ROUNDS_REACHED 종료
```

한 worker의 결과만으로 round를 끝내거나, 일부 성공 worker로 champion을 만들 수 없다. `screenMaxSteps`, `phase2MaxSteps`, batch worker 수와 `maxRounds`는 [Q-BENCH-02](master-design-open-questions.md#q-bench-02)의 실험 대기 수치다. logical coordinator/worker는 core 계약이며 선택된 AWS Step Functions/Lambda adapter는 그 contract를 구현할 뿐 의미를 소유하지 않는다.

### 11.4 ALNS step

한 completed ALNS step은 다음 전체 전이다.

```text
committed current
→ request-pair destroy
→ bank 기반 pair repair
→ configured bounded in-step improvement
→ stable candidate validation/evaluation
→ [hybrid mode] exact-evaluated feasible routes를 acceptance-independent pool delta에 수집
→ hard-feasibility + stage guard
→ acceptance
→ commit 또는 discard
→ current/best 판정
→ adaptive/acceptance state 갱신
→ completed-step counter +1
```

Destroy와 repair의 대상 수는 node가 아니라 request 수다. Repair 뒤 삽입되지 않은 request는 완전한 pair가 route에 없는 채 bank에 남는다. Candidate가 hard-infeasible이면 acceptance에 도달하지 않는다.

`bounded in-step improvement`는 repair 뒤 실행하는 명시적 vehicle rebind, route elimination 또는 pair relocate/exchange family와 각 최대 attempt/work budget을 뜻한다. 무제한 local-search loop나 hidden wall-clock stop이 아니다. 지원 가능한 destroy/repair/local/fleet family는 bound registry/config가 ID·version·순서·budget과 함께 명시한다. ALNS core는 고객사 ID, 가격 key 또는 Win PoC 순서를 열거하지 않는다. Non-improving feasible acceptance는 `current`만 바꿀 수 있고 cache-free validated `stageBest`/`solveBest`를 악화시킬 수 없다.

각 stage는 이전 cache-free validated best를 warm-start로 받는다. 선행 objective guard를 통과하지 못한 후보는 하위 목표 개선만으로 current/best가 될 수 없다. Stage 실패나 중단은 마지막 cache-free validated best를 보존한다. 독립 verifier의 `verified` 판정은 search 이후 publication gate에서만 사용한다.

### 11.5 Destroy, repair와 adaptive selection

**`[P-20]`** ALNS의 operator는 node가 아니라 atomic `Request`를 다룬다. Destroy operator가 route를 직접 임의 수정하고 별도 ID 목록을 돌려주는 API는 금지한다. Operator는 ordered unique request IDs와 근거를 제안하고, 중앙 pair editor가 COW candidate에 pickup/delivery 제거와 bank 추가를 한 번에 적용한다.

목표 registry는 최소 다음 family를 지원할 수 있어야 한다.

| Family | 대상 선택 의미 | Canonical 보정 |
|---|---|---|
| Random removal | Assigned request에서 seeded random subset | Stable request universe와 전용 RNG stream |
| Related/Shaw removal | Distance, time-window, demand/resource similarity로 seed 주변 선택 | Prepared travel·bound facts만 사용하고 고객 field를 직접 읽지 않음 |
| Route removal | 선택 route의 request를 함께 제거 | Nonempty solution에서 0개 제거가 되지 않도록 policy가 최소/최대와 rounding을 명시 |
| Historical edge/action removal | 낮은 품질 adjacency나 transition history를 흔듦 | Immutable service identity 사용, route index 사용 금지 |
| Worst/Semi-worst removal | 제거 후 objective improvement가 클 것으로 보이는 request에 편향 | 실제 삭제는 중앙 editor가 수행하고 full comparator가 최종 권위 |
| Location-oriented removal | Pickup, delivery 또는 service-entry location 관계로 선택 | Delivery-only와 real pair의 entry 의미를 구분 |

`OGC2024_LEGACY_REPLAY` preset은 9개 선택 슬롯, request 비율 `5%..15%`, `maxDestroy=1000`, Shaw 가중치 `9/3/2`, worst rank exponent `3`을 재현할 수 있다. 이 값들은 general RPDPTW production default가 아니다. 일반 profile은 removal lower/upper bound, active operator IDs/versions와 tie order를 explicit algorithm config로 제공한다.

반복 결과 분류는 다음과 같다.

```text
GLOBAL_BEST_IMPROVED
CURRENT_IMPROVED
ACCEPTED_NON_IMPROVING
REJECTED
INVALID_CANDIDATE
INTERRUPTED
```

Adaptive policy는 이 분류를 보상으로 변환한다. Legacy preset의 `20 / 10 / 2 / 0`, update period `100`, reaction factor `0.5`, exploration floor `0.01`은 differential test 대상이다. 목표 기본 계약은 수치 자체가 아니라 다음이다.

1. 분모가 0인 operator를 안전하게 처리한다.
2. 모든 활성 operator의 선택 확률은 finite·positive이고 합이 정확한 허용오차 안에서 1이다.
3. 미완료/invalid step은 보상, 온도, 호출 횟수와 completed-step을 전진시키지 않는다.
4. 동일 result class와 pre-update snapshot에서 deterministic next snapshot을 만든다.
5. Adaptive/history/acceptance state의 `RESET_EACH_ALNS_PHASE` 또는 `CARRY_ACROSS_HYBRID_PHASES`를 manifest에 명시한다. OGC 호환은 reset이며 production 선택은 실험·version 대상이다.

Acceptance는 bound comparator의 stage guard를 먼저 통과한 feasible candidate에만 적용한다. Simulated Annealing을 사용할 경우 scalar energy projection, 초기 온도, step-based cooling과 scale을 config가 명시해야 한다. Lexicographic vector를 임의 Big-M 합으로 바꾸지 않는다. 첫 phase SA·후속 Hill Climbing은 legacy compatibility option이지 hidden lifecycle rule이 아니다.

### 11.6 Promising repair와 exact insertion

Repair의 hot path는 기준 구현의 가장 중요한 성능 아이디어인 **cheap shortlist → exact pair insertion**을 채택한다.

```text
bank의 repair 대상 request
→ static compatibility/capacity/time-overlap fast gate
→ 모든 existing route의 cheap promising score
→ stable top-N 또는 seeded power-biased shortlist
→ shortlist + 모든 합법 NEW_ROUTE/unused-vehicle option
→ 공통 atomic pair evaluator의 exact insertion
→ bound comparator + stable tie-break
→ COW apply 또는 bank 유지
```

Promising score는 후보 수를 줄이는 ranking hint일 뿐 feasibility, objective delta 또는 final diagnostic의 권위 값이 아니다. Exact evaluator는 real pickup-delivery의 모든 합법 `pickupPosition < deliveryPosition`, delivery-only service 의미, directed travel, vehicle/terminal/work-window/capability/zone/resource를 평가한다. 모든 기존 route가 탈락해도 unused concrete vehicle의 `NEW_ROUTE` option은 같은 경쟁 집합에 들어간다.

`OGC2024_LEGACY_REPLAY`는 다음 shortlist 식을 별도 preset으로 재현할 수 있다.

$$
N=\min(\max(12,\lfloor 2K/100\rfloor), candidateRouteCount)
$$

그리고 상위 `5N` 안의 power-biased sampling을 선택할 수 있다. General preset의 `N`, prefilter와 score coefficient는 instance size, route length와 exact-evaluation budget을 계측해 정하며 공식값은 config/version 없이 존재하지 않는다.

한 request에서 \(R\)은 existing route 수, \(N\)은 exact shortlist 크기다. 대표 비용은 cheap scoring `O(R)`, route ranking `O(R log R)`, 선택된 `N`개 route의 pair-position exact evaluation 합이다. 성능 evidence는 wall-clock만이 아니라 `cheapCandidates`, `exactEvaluations`, `feasibleOptions`, `newRouteOptions`, allocation과 completed steps를 기록해야 한다.

Repair 결과는 모든 request의 재삽입을 가정하지 않는다.

```text
COMPLETE_REINSERTION
PARTIAL_REINSERTION
NO_FEASIBLE_INSERTION
DEFECT
```

`PARTIAL_REINSERTION`과 `NO_FEASIBLE_INSERTION`도 route/bank partition과 hard feasibility를 만족하면 commit 가능한 completed trial이 될 수 있고, commit 뒤에만 stable `SearchSnapshot`이다. 채택 여부는 unassigned dimension을 포함한 bound comparator가 결정한다. Repair scope가 `REMOVED_ONLY`인지 `REMOVED_PLUS_EXISTING_BANK`인지도 operator config와 trace에 남긴다.

### 11.7 Immutable route pool

Route pool은 solution acceptance와 별도다. Completed candidate가 hard-feasible full route evaluation을 끝낸 시점이면, 전체 solution이 거부되어도 그 안의 좋은 route를 `RoutePoolDelta`로 수집할 수 있다. Infeasible, interrupted, rollback 중이거나 derived cache만 평가된 route는 수집하지 않는다.

Pool entry는 projection-independent evaluated route artifact다. MIP column은 snapshot seal/model-build에서 특정 projection으로 별도 생성한다.

```text
EvaluatedRouteArtifact
  routeArtifactId/content digest
  problem/preparedTravel/boundProfile fingerprints
  concrete vehicle binding + terminal policy
  ordered service visits + exact request coverage
  authoritative route facts/metrics
  discovery round/worker/phase/step/operator lineage
  evaluation/version fingerprint

ProjectedRouteColumn
  projectedColumnId
  routeArtifactId
  exact request/vehicle/resource row coefficients
  exact objective coefficients
  projection fingerprint
```

Identity와 dominance를 분리한다.

- `RouteSignature`: vehicle binding과 ordered service sequence를 포함한 exact identity
- `RouteCoverageKey`: 같은 authority fingerprint, **같은 concrete vehicle**과 request coverage의 dominance bucket
- `RouteArtifactId`: exact signature와 authoritative evaluation fingerprint의 stable content ID
- `ProjectedColumnId`: route artifact, projection과 encoded coefficient digest의 stable content ID

OGC의 `(rider type, sorted order IDs) → lowest scalar cost` key는 동질 vehicle와 단일 비용 전제에만 맞는다. 일반 RPDPTW에서는 같은 coverage라도 ordering, concrete vehicle, terminal/work window와 다차원 objective가 다를 수 있다. Base pool은 같은 coverage만으로 artifact를 제거하지 않는다. Pool-level route-fact dominance가 모든 소비 경로에 안전함이 versioned contract로 증명되거나, 특정 projection의 projected-column pruning이 같은 concrete vehicle에서 objective와 모든 hard/resource/linearization row coefficient의 component-wise dominance를 확인할 때만 제거한다. 그 밖에는 nondominated frontier를 유지한다. Vehicle-class aggregation은 별도 model mode와 equivalence proof 없이는 이 bucket에 섞지 않는다.

Pool rule:

1. Append와 import는 같은 validation·merge를 사용한다. 같은 signature/authority의 evaluation·coverage가 같으면 lineage를 합치고, 다르면 임의 tie-break 없이 integrity defect로 거부한다.
2. Pool route는 절대 mutate하지 않는다. 주문 제거나 vehicle 변경은 새 route/signature/evaluation을 만든다.
3. 다른 problem/travel/profile fingerprint의 artifact를 혼합하지 않는다. Projection mismatch는 pool import 문제가 아니라 model-build 단계의 typed incompatibility다.
4. Incumbent route는 snapshot seal 전에 merge하고 pruning에서 pin한다.
5. Selector는 stable-sorted immutable snapshot만 받는다.
6. Pool cap, aging, pruning과 persistence는 정책/version/telemetry 없이 숨은 default로 활성화하지 않는다.

### 11.8 Route-selection model boundary

Route selection의 규범 수학 모델과 domain type은 [Domain Design §12.5~§12.8](domain-design.md#125-route-selection-projection과-exact-partition)이 소유한다. Master는 적용 원칙만 고정한다.

- Corrected 후보 `SET_PARTITION_EXACT`는 request마다 route coverage와 explicit unassigned 변수를 합해 정확히 1로 만든다.
- Concrete vehicle 하나는 최대 route 하나만 소비한다. Vehicle-class aggregation은 travel, capacity, work window, ownership과 objective가 완전히 동등하다는 증명이 있을 때만 허용한다.
- Mandatory request를 무조건 hard infeasible로 만들지 않는다. Bound preset의 의미에 따라 mandatory-unassigned를 최우선 lexicographic dimension으로 최소화하거나, 별도 승인된 exact-assignment mode에서만 unassigned를 고정한다.
- General objective는 `RouteSelectionProjection`이 route/unassigned 단위로 가산 또는 exact linearizable하다고 증명한 dimension만 투영한다.
- Strict priority는 단계별 solve 또는 exact-priority backend capability로 구현한다. 단계 \(d\)의 optimality를 증명해 \(Q_d=Q_d^\*\)를 고정한 뒤에만 다음 dimension으로 진행하며 `FEASIBLE_LIMIT`에서 낮은 priority를 계속 최적화하지 않는다. 근거 없는 Big-M, `cost/K` 또는 scalar surrogate를 authoritative comparator로 사용하지 않는다.
- `SET_COVER_THEN_CONVERT`는 \(\sum_{r\in R}a_{ir}x_r\ge1\)이고 \(u_i\)가 없는 complete-cover OGC 2024 compatibility/differential 실험 모드다. 중간 cover는 stable candidate가 아니며 immutable conversion 뒤 full evaluation을 거쳐야 한다.

Solver port는 immutable model spec, projected columns, warm start, budget와 backend config를 받고 provider-neutral status/termination/provenance와 selected `ProjectedColumnId`만 반환한다. Unassigned set은 selected route coverage의 exact complement로 materialization 경계가 재구성한다. Canonical backend adapter는 Google OR-Tools CP-SAT를 사용한다. `com.google.ortools.*`, `CpModel`, `CpSolver`, `CpSolverStatus`와 native-loader 세부는 optional backend module 밖으로 나올 수 없다. 이 모델은 0-1 변수와 checked integer/fixed-point 계수만 사용하므로 continuous variable용 MPSolver는 canonical backend가 아니다.

### 11.9 Hybrid phase와 outer round

기준 적용 baseline은 **worker-local inner hybrid**다. 현재 distributed phase-2 round와 용어·commit을 섞지 않는다.

```text
outer WorkerRun
  → cache-free validated common ALNS warm start
  → inner HybridPhase 0: ALNS segment + route collection
  → seal worker-local RoutePoolSnapshot
  → optional route selection
  → reconstruct + authoritative full evaluation
  → ALNS incumbent와 strict comparator adoption
  → adopted champion을 next inner HybridPhase warm start로 사용
  → configured inner phases 완료
  → candidate verification
  → immutable worker outcome

outer coordinator
  → declared worker completeness
  → stable fan-in
  → round champion
  → next outer round common warm start
```

Worker-local pool은 inner phase 사이에 유지된다. Route pool 지속과 adaptive destroy/history/annealing state 지속은 서로 독립된 config다. `roundOrdinal`, `workerOrdinal`, `hybridPhaseOrdinal`과 `alnsRunOrdinal`을 별도 identity로 기록한다.

여러 worker의 `RoutePoolDelta`를 중앙에서 합쳐 exact selection을 한 번 수행하는 방식은 잠재적으로 더 큰 조합 공간을 제공하지만 reference baseline보다 큰 분산 설계다. `CROSS_WORKER_POOL_FAN_IN`은 pool artifact size, deterministic merge, partial-worker 처리, CP-SAT CPU/memory bottleneck과 application admission-control evidence를 가진 별도 ADR 전에는 기본 경로가 아니다.

한 inner phase의 commit 순서는 다음이다.

```text
ALNS_COMPLETED
→ POOL_SEALED
├─ ROUTE_SELECTION_SKIPPED | NO_INCUMBENT | ROUTE_SELECTION_FAILED
│  ├─ optional → INCUMBENT_RETAINED → HYBRID_PHASE_COMMITTED
│  └─ required → HYBRID_PHASE_INCOMPLETE
└─ ROUTE_SELECTION_WITH_INCUMBENT
   → MATERIALIZED
   ├─ FULL_EVALUATION_FAILED
   │  ├─ optional → INCUMBENT_RETAINED → HYBRID_PHASE_COMMITTED
   │  └─ required → HYBRID_PHASE_INCOMPLETE
   └─ FULL_EVALUATED
      → INCUMBENT_RETAINED | SELECTOR_CANDIDATE_ADOPTED
      → HYBRID_PHASE_COMMITTED
```

다음 phase는 `HYBRID_PHASE_COMMITTED`의 stable champion만 소비한다. Cover selection, raw solver incumbent와 변환 중 route는 stable search state가 아니다.

### 11.10 Status, adoption과 fallback

Route-selection outcome은 최소 다음 범주를 구분한다.

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

Backend는 incumbent 존재를 확인한 뒤에만 selected route variables를 읽는다. CP-SAT `OPTIMAL`과 `FEASIBLE`만 incumbent-present로 매핑한다. `INFEASIBLE`, `MODEL_INVALID`, `UNKNOWN`에서는 selected value를 읽지 않으며 `UNKNOWN`은 limit/cancellation 원인을 별도 termination evidence로 보존한 no-incumbent outcome이다. `CpSolver.objectiveValue()`/bound/gap은 port 반환값이나 adoption authority가 아니다. `BACKEND_UNAVAILABLE`은 optional assembly 부재이고 `NATIVE_RUNTIME_UNAVAILABLE`은 `Loader.loadNativeLibraries()` 또는 platform-native 초기화 실패다. `OPTIMAL` 또는 `FEASIBLE_LIMIT`이어도 raw result는 candidate가 아니다. 선택 projected column을 새 route/bank로 materialize하고, unassigned를 exact coverage complement로 재구성하며, 필요한 cover-to-partition conversion, authoritative full recomputation과 stable partition check를 끝낸 뒤 해당 phase의 cache-free validated ALNS incumbent와 bound comparator로 비교한다.

채택 가능한 후보와 champion은 다음과 같다.

```text
eligible = HybridPhaseIncumbent
         + successfully full-evaluated selector candidate
champion = bound comparator가 선택한 strictly better eligible candidate
raw solver incumbent = never eligible
```

No incumbent, backend/native-runtime failure, exhausted budget, invalid conversion, full-evaluation failure, fingerprint mismatch 또는 `EQUAL/WORSE`이면 `HybridPhaseIncumbent` fingerprint를 그대로 보존한다. Optional MIP plan은 `DEGRADED_ALNS_ONLY`와 이유를 남기고 계속할 수 있다. MIP-required official plan은 같은 상황을 조용히 성공 처리하지 않고 `INCOMPLETE` 또는 명시적 failure로 끝낸다.

ALNS의 exact step budget과 MIP의 solver work/time budget을 같은 재현성 등급으로 취급하지 않는다. CP-SAT config는 exact OR-Tools version/platform, `num_workers`, `random_seed`, time/deterministic-work limit, absolute/relative gap, model/order와 warm-start identity를 명시한다. 모든 숫자는 승인 전 `OPEN`이고 hidden library default를 production 계약으로 사용하지 않는다. CP-SAT가 gap limit 충족도 `OPTIMAL`로 보고할 수 있으므로 loose gap을 exact proof로 받아들이지 않는다. Integer objective의 exactness를 보존하는 gap 정책과 staged lexicographic proof가 승인되지 않으면 exact route-selection을 활성화하지 않는다. Single-worker/fixed-seed/deterministic-limit 조합도 먼저 `PROPOSED TEST_ONLY`이며 strong replay는 반복 evidence가 있을 때만 주장한다. Deadline-limited multi-worker CP-SAT는 model/pool/conversion lineage를 고정하더라도 `TIMEBOXED_HYBRID` 재현성 등급과 다중 실행 품질 evidence를 사용한다.

`C-17`은 이 상세 설계의 production 활성화를 계속 gate한다. `RM-9A`~`RM-9C`의 evidence와 별도 scope approval 전에는 current reactor에 OR-Tools dependency를 넣거나 default execution plan에서 MIP를 켜지 않는다.

## 12. Candidate state, cache와 rollback

§12.1~§12.2는 `RM-4`의 안전 baseline이고 §12.3은 `RM-7`에서만 재검토할 수 있는 최적화 gate다. `Q-ALG-02`에 따라 COW가 기본이다.

### 12.1 Initial copy-on-write

초기 안전 구현은 changed-route copy-on-write와 독립 bank를 사용한다.

```text
committed immutable snapshot
├─ immutable ProblemInstance/profile
├─ immutable/shared unchanged routes
├─ independent bank value
└─ validated aggregate/fingerprint

candidate
├─ base snapshot
├─ first write 전에 복사한 changed routes
├─ independent copied bank
└─ invalidated derived state
```

`current`, `stageBest`, `solveBest`는 immutable snapshot으로 취급한다. Candidate는 committed/best route나 cache를 직접 바꿀 수 없다. Accepted candidate만 freeze 후 새 current가 되고, rejected/interrupted/failed candidate는 전체 폐기한다.

### 12.2 Cache contract

Route sequence, request ownership, vehicle/terminal binding과 bank가 source of truth다. Arrival/load, feasibility, metrics, score, objective, aggregate, insertion table과 structural hash는 재계산 가능한 파생 상태다.

- Cache는 한 problem/profile/run/structure version에만 속한다.
- Route 또는 bank mutation은 영향받은 route/solution cache와 fingerprint를 무효화한다.
- Cache key는 route/request/positions뿐 아니라 evaluator/profile과 필요한 generation을 구분한다.
- 의심 cache는 복원하지 않고 버린 뒤 전체 재계산한다.
- Cached/incremental 결과는 cache-free full recomputation과 정확히 같아야 한다.

### 12.3 Later apply/undo gate

Copy-on-write는 목표 구현의 기본 경로다. 현재 placeholder가 이를 구현했다는 뜻이 아니다. Apply/undo는 roadmap의 자동 전환 대상이나 필수 산출물이 아니다. COW baseline profiling에서 route copy·allocation·GC가 실제 병목으로 입증되고 별도 변경 제안이 승인된 경우에만 다음 동등성 gate를 갖춘 실험 후보가 될 수 있다.

1. 모든 move의 apply/undo round-trip 뒤 route, bank, fleet state와 fingerprint가 같다.
2. 중간 예외, watchdog, cancellation과 resource signal fault injection 뒤 current/best가 보존된다.
3. 같은 initial state, seed와 operator trace에서 COW와 step별 candidate outcome, acceptance, current/best와 adaptive update가 같다.
4. Cache-free full verifier와 최종 canonical solution이 같다.
5. 실제 fixture와 microbenchmark에서 copy/allocation이 측정된 병목이다.
6. 승인된 성능 기준을 충족하고 정확성·재현성·관측 가능성 회귀가 없다.

임의의 자동 전환 threshold를 두지 않는다. 정확성·재현성·관측 가능성 동등성 중 하나라도 실패하거나 성능 이득이 충분하지 않으면 COW를 계속 유지한다. 향후 apply/undo 채택은 [Q-ALG-02](master-design-open-questions.md#q-alg-02)의 `RESOLVED — KEEP_COW`를 묵시적으로 뒤집을 수 없으며 새로운 profiling evidence와 별도 변경 승인이 필요하다.

## 13. Termination, reproducibility와 execution provenance

이 절은 `RM-4`의 실행 의미와 `RM-6`의 official rerun 계약을 연결한다. 정상 품질 종료와 안전·platform 종료를 같은 상태로 취급해서는 안 된다.

### 13.1 종료 의미

정상 품질 예산은 phase-1 screen과 phase-2 worker의 양의 step budget이다. Wall-clock 시간은 monotonic watchdog으로만 사용한다.

| 의미 | 지위 |
|---|---|
| `MAX_STEPS_REACHED` | 모든 계획 stage의 step budget과 handoff를 완료한 정상 종료 |
| `NO_STRICT_IMPROVEMENT` | 완결된 phase-2 worker batch의 round champion이 이전 champion보다 엄격히 좋지 않아 coordinator가 정상 종료 |
| `MAX_ROUNDS_REACHED` | configured `maxRounds`의 모든 phase-2 batch를 완료한 정상 종료 |
| `WATCHDOG_REACHED` | 병리적 장기 실행을 중단한 예외적 안전 종료 |
| `CANCELLED` | 외부 취소 의도를 협력 처리한 종료 |
| `RESOURCE_LIMIT_REACHED` | solver가 처리 가능한 자원 안전 한계 |
| `PLATFORM_TIMEOUT` | 상위 실행 경계가 algorithm termination record 완성을 막은 상태 |
| `FAILED` | 실행, 구현 또는 platform failure |

입력/config binding 실패는 탐색 종료가 아니라 시작 전 오류다. 미완료 step은 completed count, acceptance/temperature와 adaptive state를 전진시키지 않고 candidate를 완전히 rollback/discard한다. Watchdog, cancellation, resource와 platform failure를 서로 또는 정상 종료로 다시 이름 붙여서는 안 된다.

예외 종료 뒤 마지막 committed best가 있더라도 candidate solution verifier와 post-finalization result-integrity verifier를 통과한 경우에만 recovery candidate가 될 수 있다. 정상 완료나 공식 benchmark run으로 표시할 수 없으며 외부 노출 여부는 별도 product 계약이다.

### 13.2 Strong reproducibility envelope

강한 재현성은 다음이 고정되고 `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED`로 정상 종료한 실행에 적용한다.

```text
problem + normalized matrix + numeric/time/adapter fingerprints
+ exact profile/config/evaluation/objective/SolvePlan versions
+ portfolio/algorithm/operator/acceptance/state-strategy versions
+ build/runtime compatibility fingerprint
+ base/derived seeds and derivation version
+ round/run ordinal and warm-start lineage when using multi-round execution
+ stage maxSteps
+ stable iteration, reduction and tie-break order
+ hybrid 사용 시 pool/admission/projection/backend/conversion versions와 stable artifact/projected-column order
+ hybrid 사용 시 solver version, threads, seed, numeric parameters와 deterministic work/node budget
```

이 envelope에서는 canonical step trace, verified solution, outcome partition, metrics/score/objective와 result fingerprint가 같아야 한다. Global random, unordered collection iteration, thread first-winner, clock-based tie-break와 cache hit/miss 의존을 금지한다.

MIP `TimeLimit`와 multi-thread scheduling만으로 끝나는 실행은 위 strong envelope에 자동 포함되지 않는다. 이런 실행은 `TIMEBOXED_HYBRID`로 표시하고 pool/model/conversion identity, status와 candidate fingerprint를 보존하되 최종 품질은 반복 분포로 평가한다. `OPTIMAL` 또는 deterministic backend work limit만으로도 동일 결과가 보장되는지는 backend별 contract test와 version 고정으로 입증해야 한다.

### 13.3 Provenance

관측 elapsed time은 metadata일 뿐 ALNS quality budget이나 fingerprint의 hidden input이 아니다. Execution record는 stage별 requested/completed steps, separate construction/local/fleet/audit/verifier work, actual base/derived seeds, round/run ordinal, warm-start candidate, selected initial candidate, operator/config versions, exact termination, 마지막 completed stage/step과 rollback integrity를 보존해야 한다. Hybrid record는 추가로 `hybridPhaseOrdinal`, pool before/delta/after digest, evaluated-artifact/projected-column counts와 pruning, projection/model/warm-start fingerprint, backend capability/version/config, build/solve work와 elapsed, status/incumbent, selected projected-column IDs, materialization이 재구성한 unassigned complement, conversion/full-evaluation/adoption/fallback과 next warm-start fingerprint를 보존한다. Seed는 “좋은 값”을 선별하는 품질 대상이 아니라 고정 manifest 안에서 서로 다른 탐색 경로를 만드는 다양성 입력이다.

## 14. Independent verification, publication과 Win PoC benchmark

§14.1은 `RM-5`의 publication gate이고 §14.2~§14.4는 `RM-6`의 benchmark 계약이다. Benchmark는 검증을 우회하는 별도 실행 경로가 아니라 publishable verified result만 소비하는 상위 workflow다.

### 14.1 Publication gate

Publication은 서로 다른 입력과 책임을 갖는 다음 두 gate를 정확히 이 순서로 통과해야 한다.

```text
committed candidate
→ candidate solution verifier
→ preliminary request partition
→ required final-solution insertion audit
→ final request outcomes/diagnostics
→ post-finalization result-integrity verifier
→ publication
```

#### Candidate solution verifier

Candidate solution verifier의 권위 입력은 다음 네 가지다.

- immutable problem/profile declaration
- candidate route/node order
- candidate의 `SearchRequestBank`
- authoritative prepared directed travel data

이 verifier는 위 입력에서 cache 없이 다음을 다시 계산해야 한다.

1. Problem/profile/matrix/solution identity와 fingerprint
2. 모든 request의 route/bank partition, exactly-once, same-vehicle와 precedence
3. Vehicle, oneway/roundtrip terminal 정책과 service-pattern 규칙
4. Size membership, capability subset과 `servableVehicles`
5. 모든 directed leg의 location mapping, distance와 time
6. Load, time, planning/vehicle windows와 route resources
7. 등록된 hard constraints
8. Neutral metrics, score breakdown과 objective vector

Solver feasibility flag, insertion 결과, cached arrival/load/metric/score, search cache, solver summary와 structural hash는 candidate solution verifier의 권위 입력이 아니다. Missing arc나 unresolved policy는 검증 실패다. `PASS` report와 verified solution이 없으면 finalization은 게시 가능한 outcome 생성으로 진행할 수 없다.

#### Post-finalization result-integrity verifier

Post-finalization result-integrity verifier의 권위 입력은 다음이다.

- candidate solution verifier의 `PASS` report와 verified solution
- final outcomes
- diagnostic source/audit evidence
- outcome-derived summary
- publishable payload

이 verifier는 모든 input request와 verified solution 사이의 exactly-one `ASSIGNED`/`UNASSIGNED` outcome partition, status와 route/vehicle ownership reference의 일치, required audit의 완결성과 diagnostic confidence ceiling, outcome에서 재계산한 summary, canonical solution/result identity와 게시할 payload fingerprint를 cache 없이 검증해야 한다.

Search cache와 solver summary는 post-finalization result-integrity verifier의 권위 입력도 아니다. Bounded search telemetry가 승인된 diagnostic source로 전달될 수는 있지만 그 자체가 solver summary를 권위화하거나 proven/exhaustive confidence를 만들지는 않는다. 이 gate의 `FAIL` 또는 미완료도 publication을 거부한다.

두 gate 중 하나라도 `FAIL`이거나 미완료이면 정상 route/outcome payload와 benchmark vector를 발행하지 않는다. Candidate verifier 실패는 정상 unassignment diagnostic으로 바꾸지 않고, result-integrity 실패는 candidate solution `PASS`로 덮어쓰지 않는다.

### 14.2 Primary fixture와 manifest

1차 end-to-end fixture는 [`data/win_poc_case.json`](../data/win_poc_case.json)이다.

- Read-only input fixture이며 정답 route나 baseline을 포함하지 않는다.
- 확인된 SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이다.
- Fixture의 `trips=oneway`가 우선하므로 `multiRotation=1`은 route 의미에 영향을 주지 않는다. Depot에서 한 번 출발하고 중간·최종 depot 방문 없이 마지막 고객에서 끝난다.
- Raw `multiRotation` 값은 provenance에 남기되 feasibility, route 생성과 metric에 사용하지 않는다.
- 현재 fixture의 소수 `D/U`는 정수 meter/second 입력 계약에 비준수이므로 정수 matrix를 다시 받기 전 official run에 사용할 수 없다.
- Official comparison은 fixture, adapter, numeric/time/matrix, profile, algorithm/build, seeds/steps, verifier, metric formula와 gate를 하나의 비교 계약 snapshot으로 고정해야 한다.
- 그 snapshot을 round/worker plan과 함께 versioned manifest/card로 표현하는 방식은 잠정이다 (`P-13`). Exact schema와 official 수치는 아직 확정하지 않는다.
- Fingerprint가 다르면 quality regression을 판정하지 않고 비교 불가로 처리한다.
- Baseline은 실제 verified official run의 비교 record와 champion lineage로만 만들고 자동 덮어쓰지 않는다.

### 14.3 Win PoC comparator

Win PoC 전용 품질 vector는 다음 exact lexicographic order다.

```text
미배정 request 수
→ 배차 차량 수
→ 전체 거리
→ 전체 시간
```

```text
(
  unassigned request count,
  dispatched vehicle count,
  total distance,
  total time
)
```

모든 성분은 작을수록 좋으며 첫 번째로 다른 성분만 승패를 결정한다.

- `unassigned request count`: verified input vehicle route에 배정되지 않은 input request 수
- `dispatched vehicle count`: request를 하나 이상 수행하는 고유 input vehicle 수
- `total distance`: verifier가 actual directed route arcs에서 재계산한 합
- `total time`: 모든 used route의 운영시간 합

네 성분이 모두 같으면 품질상 동률이다. Deterministic structural tie-break는 다섯 번째 품질 성분이 아니다. 이 comparator는 Win PoC benchmark 전용이며 모든 고객사의 solve objective가 아니다.

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

Used vehicle의 route 시작 전 업무와 무관한 유휴시간, 미사용 vehicle 시간, solver/검증/직렬화 elapsed는 포함하지 않는다. Drive, customer/depot wait, service, inter-work-window rest와 total을 각각 재계산 가능한 breakdown으로 보존한다.

Quality, integrity, reproducibility와 performance 판정은 분리한다. Parsing, binding, construction, search, verification, finalization의 elapsed/memory 값은 성능 관측이며 네 성분 quality vector에 들어가지 않는다.

### 14.4 Multi-round official execution

Official benchmark는 manifest에 고정된 round plan을 정확히 실행한다.

1. 8개 initial candidate는 각자 `screenMaxSteps` screen을 완료하고, comparator가 phase-1 champion을 고른다.
2. 첫 phase-2 round worker는 phase-1 champion을 공통 warm start로 사용한다.
3. 같은 round의 worker는 동일 execution contract와 서로 다른 derived seed/destroy/repair config를 사용한다.
4. 모든 worker candidate를 독립 검증한 뒤 stable comparator/tie-break로 단일 round champion을 고른다.
5. round champion이 이전 champion보다 엄격히 좋으면 다음 round의 모든 worker가 이를 공통 warm start로 사용한다. 그렇지 않으면 `NO_STRICT_IMPROVEMENT`로 끝낸다.
6. `maxRounds` 안의 마지막 verified champion만 전체 official result다.

Manifest가 선언한 모든 worker는 exact `maxSteps`로 `MAX_STEPS_REACHED`하고 독립 검증을 통과해야 round가 완료된다. 실패 worker는 같은 round/run identity, seed와 warm start로 재시도할 수 있지만, 하나라도 끝내 완료되지 않으면 round와 전체 benchmark는 `INCOMPLETE`다. 성공한 일부 worker만으로 champion을 정하거나 다음 round를 시작할 수 없다.

`C-17` gate를 통과한 hybrid manifest라면 각 worker의 `phase2MaxSteps`는 모든 inner ALNS segment의 **completed-step 합**으로만 소비된다. Route-selection은 별도의 deterministic work/node/time budget과 downstream reserve를 가진다. Worker는 하나 이상의 `ALNS segment → pool seal → route selection/adoption` inner phase를 실행할 수 있으며, completion은 ALNS step 합뿐 아니라 마지막 pool/selector status/fallback, reconstruction/full evaluation과 final candidate verification까지 포함한다. MIP-required manifest의 fallback은 worker 정상 성공이 아니며, optional manifest만 `DEGRADED_ALNS_ONLY`를 정상 worker outcome으로 허용할 수 있다. ALNS-only와 hybrid result는 algorithm/pool/projection/backend fingerprint가 다르므로 같은 official comparison card에서 직접 quality regression으로 판정하지 않는다.

Seed별 no-worse나 “좋은 seed” 선정은 official hard gate가 아니다. Worker 완료 순서와 물리 병렬 순서는 champion에 영향을 주지 않는다. 결과 의존 종료는 complete batch의 stable fan-in 뒤 `NO_STRICT_IMPROVEMENT` 판정에만 허용하며, worker 중간 종료나 전체 wall-clock quality deadline을 사용하지 않는다.

`screenMaxSteps`, phase-2 round별 worker 수·`phase2MaxSteps`·`maxRounds`와 watchdog은 [Q-BENCH-02](master-design-open-questions.md#q-bench-02)의 확정된 calibration/approval protocol을 따라야 하지만 실제 공식 수치는 아직 없다. 인터뷰의 동작 설명용 수치나 기존 초안의 임시값을 공식값으로 사용하지 않는다. 논리 fan-out/fan-in은 provider-neutral하며 선택된 AWS Step Functions/Lambda adapter와 local runner가 같은 completion·retry·verification 의미를 구현해야 한다.

## 15. Implementation roadmap와 phase gates

이 절은 §5~§14의 계약을 실제 개발 순서로 바꾼다. 아래 산출물은 모두 미래 개발 대상이며, 문서·class·test file이 존재하거나 happy path가 실행된다는 사실만으로 phase가 완료되지 않는다.

각 phase는 다음 네 가지를 모두 가져야 한다.

1. **Entry condition:** 선행 phase가 제공한 권위 산출물과 아직 막혀 있는 결정
2. **Implementation unit:** 이 phase에서 책임질 컴포넌트와 금지되는 scope
3. **Deliverable:** 다음 phase가 직접 소비할 immutable contract/artifact
4. **Exit evidence:** 계약을 만족하고 failure를 거부한다는 재현 가능한 증거

### 15.1 Critical path와 gate 규칙

```text
RM-0 decision baseline
  → RM-1 normalized problem + prepared travel
  → RM-2 bound evaluation/profile
  → RM-3 pair evaluator + initial portfolio
  → RM-4 COW ALNS + execution record
  → RM-5 two-gate verified result
  → RM-6 official Win workflow
  → RM-7 measured COW decision
  → RM-8 verified integration/cutover

RM-9 = separate approval only
```

- `RM-8`의 logical port/interface와 test double은 `RM-0` 뒤 병행할 수 있다. 실제 cutover는 `RM-1`~`RM-5`를 우회할 수 없다.
- Optional hybrid branch는 `RM-4`의 cache-free validated ALNS baseline 뒤 `RM-9A` route pool → `RM-9B` solver-neutral selector → `RM-9C` feedback/shadow 순서로 진행한다. Publication 경로는 별도로 `RM-5` both-gate를 요구하며 `RM-9C` 전에는 default solve path를 바꾸지 않는다.
- Verifier skeleton과 corruption fixture 설계도 일찍 시작할 수 있지만 `RM-5` 완료 주장은 `RM-1`~`RM-4`의 실제 권위 산출물에 대한 독립 검증 evidence가 필요하다.
- 한 phase의 test double, experiment value 또는 partial implementation은 다음 phase의 production authority로 자동 승격되지 않는다.
- 해결된 질문은 [세션 29](master-design-sessions/29-open-question-interview.md)의 exact decision을 소비한다.
- `Q-BENCH-02`는 logical fan-out/fan-in test를 막지 않지만 official `RM-6` manifest/baseline을 막는다.
- 모든 test/experiment 값은 명시적으로 주입하고 production default로 승격하지 않는다.

### 15.2 `RM-0` — 결정과 구현 baseline

| 구분 | 계약 |
|---|---|
| Entry | 선행 phase 없음. 이 Master와 질문 등록부는 `REVIEW` 상태 |
| 구현 단위 | 적용할 `C-*`, `P-*`, `Q-*`, 문서 authority와 change-control rule을 개발 backlog/test trace의 기준으로 고정 |
| Deliverable | `C-01`~`C-22`, `P-01`~`P-20` coverage, 28개 질문 상태, component/phase-to-contract traceability baseline |
| Exit evidence | Local link/anchor와 Markdown lint, 결정/질문 수 검증, `REVIEW`를 구현 완료·benchmark 완료·topology 승인으로 오인하는 표현 0건 |
| 완료 후 소비자 | 모든 후속 phase의 acceptance criteria와 change-impact review |

`RM-0`은 문서를 승인 상태로 만드는 phase가 아니다. 구현 중 발견된 충돌을 어느 기록과 절에서 해결할지 정해 이중 기준을 막는 준비 단계다.

### 15.3 `RM-1` — Versioned input, immutable domain과 prepared travel

| 구분 | 계약 |
|---|---|
| Entry | `RM-0`; 승인된 input meaning과 §5~§8의 numeric/time/service/compatibility/travel 계약 |
| 구현 단위 | Versioned adapter, canonical business input, dense identity mapping, request/vehicle/node/location model, numeric/time/window/service normalization, static compatibility, terminal/trip policy와 Travel Matrix preparation |
| Deliverable | Immutable `ProblemInstance` 의미, complete prepared directed travel, external↔dense ID mapping, policy/source provenance와 fingerprints |
| 금지 | Core 직접 deserialization, floating tolerance, numeric sentinel, runtime travel fallback, partial pair, oneway/rotation 의미 추정 |
| Exit evidence | Hand-calculated normalization/service/window/load 사례, boundary와 checked-overflow rejection, pair/reference/terminal property test, size/capability/zone cases, provided/generated/self/asymmetric travel coverage와 solver/verifier fingerprint equality |
| 완료 후 소비자 | `RM-2` binder/propagator, `RM-3` evaluator, `RM-5` candidate verifier |

현재 Win fixture의 소수 `D/U` 비준수는 generic `RM-1` 계약 구현을 막지 않는다. 다만 그 fixture를 compliant input이나 official baseline evidence로 사용하는 것은 `RM-6`에서 계속 차단한다.

### 15.4 `RM-2` — Propagation, evaluation과 bound profile

| 구분 | 계약 |
|---|---|
| Entry | `RM-1`의 immutable problem과 prepared travel |
| 구현 단위 | Route propagator, structural/static hard gate, neutral metrics, composed constraint, score component, lexicographic comparator, `SolvePlan`, exact customer profile/preset binding |
| Deliverable | Solve별 immutable bound profile/plan, full route/solution evaluation과 exact dependency/version fingerprint |
| 금지 | Hard violation의 finite penalty화, metric에 가격 포함, comparator의 physical recalculation, unknown/latest/customer-crossing fallback, request가 objective 수식 직접 주입 |
| Exit evidence | Layer dependency test, missing/duplicate/unit/schema rejection, hand-calculated full propagation/evaluation, customer profile isolation, comparator transitivity/stable tie order와 stage guard evidence |
| 완료 후 소비자 | `RM-3` initial construction, `RM-4` ALNS, `RM-5` candidate verifier |

새 고객 요구를 구현할 때는 §9.2의 가장 좁은 extension seam을 선택한다. Customer name이나 objective label만으로 propagator와 ALNS core를 바꾸면 이 phase는 완료될 수 없다.

### 15.5 `RM-3` — Atomic pair evaluator와 8개 initial portfolio

| 구분 | 계약 |
|---|---|
| Entry | `RM-2`의 bound profile/comparator/plan과 `RM-1`의 immutable problem |
| 구현 단위 | Side-effect-free atomic pair evaluator, 4 request-route 성장 정책 × 2 `DIRECT`-first vehicle 순서, 독립 candidate state, route artifact 기록과 cache-free validation |
| Deliverable | 최대 8개의 cache-free validated initial candidates, source request/vehicle policy·config·evaluation lineage와 ordered route artifacts |
| 금지 | Partial pair option, infeasible option ranking, policy별 feasibility 재구현, raw coordinate/matrix 재해석, `CLOCK` 좌표 누락 fallback |
| Exit evidence | 8개 policy combination trace, `CLOCK` coordinate-unavailable case, request/vehicle tie-break, pair evaluator와 cache-free full recomputation equality, failed insertion rollback, candidate isolation과 artifact fingerprint |
| 완료 후 소비자 | `RM-4` warm-start set과 first-round logical assignment |

`CLOCK`이 좌표 누락으로 `UNAVAILABLE`이면 남은 조합은 정상적으로 생성할 수 있다. 8개 candidate의 quality screen은 `RM-4`가 담당하며, `RM-3`은 candidate 생성과 verifier 동등성만 책임진다.

### 15.6 `RM-4` — COW ALNS, cache, termination과 reproducibility

| 구분 | 계약 |
|---|---|
| Entry | `RM-3`의 cache-free validated initial candidates, `RM-2`의 bound stages/operators와 explicit phase-1/phase-2 run config |
| 구현 단위 | Phase-1 per-candidate screen, stable phase-1 champion fan-in, phase-2 worker batch fan-out/fan-in, pair destroy/repair, stage guard/acceptance, adaptive update, changed-route COW와 independent bank, cache invalidation, step/round counter와 watchdog/cancellation/resource/failure handling |
| Deliverable | Phase-1 champion, last committed phase-2 champion/solveBest, exact termination, completed step/round/operator/seed/warm-start lineage와 reproducibility record |
| 금지 | Committed/best 직접 mutation, 기본 apply/undo, 미완료 worker로 round champion 확정, worker completion-order winner, wall-clock quality termination, worker 중간 plateau 종료 |
| Exit evidence | 8개 screen deterministic rerun, complete-batch fan-in과 comparator plateau cases, accept/reject/exception/cancel/watchdog fault injection, COW isolation, cache hit/miss와 full recomputation equality, exact step/round accounting과 fixed-envelope trace/solution fingerprint equality |
| 완료 후 소비자 | `RM-5` candidate verifier와 recovery gating, `RM-6` worker execution |

정상 품질 완료는 per-worker `MAX_STEPS_REACHED` 뒤 coordinator의 `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED`를 포함한다. 예외 종료에서 last committed best가 남아도 `RM-5`의 두 gate 없이는 publishable result나 official run이 아니다.

### 15.7 `RM-5` — Independent verification, finalization과 publication

| 구분 | 계약 |
|---|---|
| Entry | `RM-4` committed candidate, `RM-1` problem/travel authority, `RM-2` bound evaluation declaration |
| 구현 단위 | Candidate solution verifier, preliminary partition, static `PROVEN` 분리, required final-solution insertion audit, structured diagnostics, outcome-derived summary, result-integrity verifier, publication rejection/recovery path |
| Deliverable | Candidate `PASS`와 verified solution, exactly-one final outcomes, complete audit/evidence, result-integrity `PASS`, canonical result/payload fingerprint 또는 명시적 gate failure |
| 금지 | Search cache/solver summary 권위화, bank를 final status로 직렬화, audit 발견 insertion 자동 적용·재탐색, verifier failure를 unassignment로 변환, 한 verifier의 `PASS`로 다른 실패 덮기 |
| Exit evidence | Corrupted route/bank/travel/metric/objective rejection, stale/poisoned cache 무관성, static-proven과 required-audit coverage, confidence ceiling, feasible-insertion 발견 처리, corrupted outcome/summary/payload rejection, 각 gate fail/incomplete의 publication 차단 |
| 완료 후 소비자 | Normal result retrieval, `RM-6` benchmark와 `RM-8` application integration |

두 verifier의 authority input과 실패 경로를 독립적으로 검증해야 한다. 완전한 request partition과 올바른 summary가 있어도 candidate `PASS`가 없으면 게시할 수 없고, candidate가 유효해도 final outcome/payload gate가 실패하면 게시할 수 없다.

### 15.8 `RM-6` — Win PoC official workflow와 baseline

| 구분 | 계약 |
|---|---|
| Entry | `RM-5` publishable verified result 경로, compliant integer travel fixture, `Q-BENCH-02`의 승인된 calibration result |
| 구현 단위 | Immutable comparison manifest/card, exact four-component comparator, fixed round/worker logical orchestration, retry identity, verified champion fan-in, next-round warm-start lineage와 challenger comparison |
| Deliverable | 모든 선언 worker가 정상 완료·검증된 final champion, immutable baseline/comparison record와 full manifest fingerprints |
| 금지 | 현재 decimal fixture의 official 사용, 일부 성공 worker champion, seed별 hard gate, worker 중간의 result-dependent stop, 다른 fingerprint 간 quality 판정, AWS implementation을 quality/semantic contract로 승격 |
| Exit evidence | Exact comparator hand cases, phase-1 8-candidate selection, worker completion-order 독립성, incomplete/retry cases, 모든 worker `MAX_STEPS_REACHED`+verification, plateau/max-round lineage, identical-manifest deterministic rerun와 explicit approval |
| 완료 후 소비자 | Regression/challenger workflow, `RM-7` performance profiling과 `RM-8` operational integration evidence |

실험 승인 전에는 logical coordinator와 test double까지만 검증할 수 있다. 인터뷰에서 동작을 설명한 수치나 과거 초안 값은 entry condition을 만족하지 않는다.

### 15.9 `RM-7` — COW profiling과 선택적 state-strategy 재검토

| 구분 | 계약 |
|---|---|
| Entry | `RM-4`의 정확한 COW baseline과 대표 verified execution path |
| 구현 단위 | Route copy/allocation/GC와 전체 search cost의 profiling. 실제 COW 병목이 있을 때만 별도 apply/undo experiment proposal 작성 |
| Deliverable | COW 유지 판정 또는 measured evidence를 포함한 독립 변경 제안 |
| 금지 | 자동 threshold, roadmap상 필수 apply/undo 전환, 성능 측정만으로 `Q-ALG-02` 묵시적 변경 |
| Exit evidence | Representative profile, bottleneck attribution, 제안 시 apply/undo round-trip/fault/trace/cache-free verifier/final solution 동등성과 별도 변경 승인 |
| 완료 후 소비자 | 기본적으로 COW 유지. 승인된 경우에만 별도 state-strategy implementation roadmap |

성능 이득이 충분하지 않거나 정확성·재현성·관측 가능성 중 하나라도 동등하지 않으면 **COW 유지가 정상 완료 결과**다.

### 15.10 `RM-8` — Logical-port integration과 compatibility migration

| 구분 | 계약 |
|---|---|
| Entry | Interface/test double은 `RM-0` 뒤 가능. Cutover는 최소 `RM-1`~`RM-5`의 verified end-to-end 필요 |
| 구현 단위 | Submission/input, solve execution, cancellation, finalization, status/artifact와 retrieval port 구현, versioned legacy adapter, shadow/cutover/rollback |
| Deliverable | Semantic compatibility matrix, input→result lineage, idempotent execution/status model, versioned cutover와 recoverable rollback plan |
| 금지 | Legacy GCP 동작을 목표 계약 evidence로 간주, AWS infrastructure DTO/SDK의 core·domain·solver·verifier 침투, 미검증 candidate 노출, AWS 구현 때문에 logical 의미 변경 |
| Exit evidence | Characterization와 shadow comparison, retry/idempotency/cancellation fault cases, both-gate end-to-end publication, artifact identity, versioned cutover와 rollback rehearsal |
| 완료 후 소비자 | Product/application integration과 선택된 AWS target/reference runtime. 실제 cutover는 이 evidence와 운영 승인 뒤에만 가능 |

`RM-8`은 core 의미를 application 환경에 맞춰 바꾸는 phase가 아니라, 검증된 core/result 계약을 논리 port 뒤에서 보존하는 phase다.

### 15.11 `RM-9` — Gated ALNS–MIP hybrid와 그 밖의 후속 roadmap

`RM-9A`~`RM-9C`는 구현자가 route pool/MIP를 활성화하기 전에 수행할 수 있는 순서와 exit evidence를 구체화한다. `C-17`에 따라 자동 착수나 production default 전환을 허용하지 않으며, 각 단계는 별도 scope approval과 predecessor evidence를 요구한다.

#### `RM-9A` — Immutable route pool

| 구분 | 계약 |
|---|---|
| Entry | `RM-4`의 정확한 COW ALNS baseline과 cache-free route evaluation; route pool scope 승인 |
| 구현 단위 | Acceptance-independent collector, `RouteSignature`/coverage key/route-artifact ID, append/import merge, incumbent pin, deterministic seal, telemetry와 bounded experiment policy |
| Deliverable | Worker-local `RoutePoolDelta`와 stable `RoutePoolSnapshot`; live state와 분리된 artifact schema |
| 금지 | Infeasible/aborted route 수집, pool route mutation, scalar-cost-only unsafe dominance, fingerprint 혼합, hidden pruning |
| Exit evidence | Accepted/rejected feasible route harvest, interrupted candidate exclusion, import/append equality, immutable alias test, Pareto/conservative dominance oracle, deterministic digest, incumbent retention과 memory-growth profile |

#### `RM-9B` — Solver-neutral route selection

| 구분 | 계약 |
|---|---|
| Entry | `RM-9A` snapshot, profile projection capability와 OR-Tools CP-SAT version/config/native packaging/SBOM/security/operations/compute-cost/admission/fallback/rollback 승인 |
| 구현 단위 | Exact partition model spec, explicit unassigned와 concrete vehicle rows, staged lexicographic projection, CP-SAT hint, provider-neutral status mapping, optional compatibility converter와 isolated backend adapter |
| Deliverable | Stable projected-column selection outcome, model/warm-start/solver evidence와 materialized/full-evaluated candidate 또는 typed fallback |
| 금지 | OR-Tools type의 core 침투, unsupported profile 무음 근사, hidden Big-M, status/incumbent 확인 전 attribute 접근, raw objective 채택 |
| Exit evidence | Tiny-pool brute-force oracle, int64 coefficient/range check, exact request/vehicle constraints, warm-start feasibility, CP-SAT status×incumbent matrix, model/native-runtime failure, conversion immutability, cancellation과 callback/temp/native lifecycle |

#### `RM-9C` — Hybrid feedback, verification과 shadow

| 구분 | 계약 |
|---|---|
| Entry | `RM-9B`, `RM-5` both-gate path와 `SolvePlan`이 소유하는 approved explicit worker-local `HybridPhasePlan` extension |
| 구현 단위 | ALNS→pool→selector→reconstruction→full evaluation→adoption, next-phase warm start, optional/required fallback, reproducibility class와 shadow comparison |
| Deliverable | Immutable `HybridPhaseRecord`와 worker-level hybrid summary, adopted champion/fallback lineage와 production activation recommendation 또는 reject decision |
| 금지 | Raw cover/solver incumbent를 warm start로 사용, invalid/worse candidate 채택, MIP failure로 ALNS incumbent 오염, timeboxed run에 strong replay 주장 |
| Exit evidence | End-to-end hybrid fixtures, invalid/no-incumbent/native-runtime fallback fingerprint 보존, adopted-only feedback, fixed-envelope replay 또는 declared timeboxed statistics, ALNS-only 대비 quality/memory/native/compute-cost shadow |

Cross-worker pool fan-in과 중앙 MIP는 `RM-9C`의 기본 완료조건이 아니다. Worker-local baseline 뒤 artifact size, complete-worker merge, idempotency, CP-SAT CPU/memory bottleneck과 application admission-control evidence로 별도 scalability ADR을 승인해야 한다.

Optional variants, academic benchmark expansion, multi-trip/rotation과 **AWS 이외의 physical topology 채택**은 계속 독립 후속 scope다. 다음 행위는 어느 `RM-9` 준비에서도 허용하지 않는다.

- 현재 pair/terminal/bank invariant를 optional variant 가능성 때문에 완화
- OR-Tools dependency를 `rpdptw-core` 또는 backend-neutral package에 추가
- 특정 infrastructure product에 맞춰 logical round/result 의미 변경
- Deferred 질문을 `Q-BENCH-02` 실험과 묶어 자동 활성화

## 16. Risks, migration, deferred work와 non-scope

### 16.1 주요 위험과 gate

| 위험 | 조기 신호 | Gate/대응 |
|---|---|---|
| semantic drift | adapter/preparation/evaluator/verifier가 같은 입력을 다르게 해석 | exact policy/source fingerprint, unresolved generation rejection, verifier identity check |
| customer branching | 고객사 이름이나 업무 문자열이 common evaluator/ALNS에 등장 | profile dependency closure와 new-profile change-impact review |
| pair/rollback 손상 | partial pair, route+bank 중복, 중단 뒤 fingerprint 변화 | atomic mutation, fault injection과 full partition verification |
| stale cache | hit/miss에 따라 feasibility/score가 달라짐 | cache-free equality at insertion, acceptance, best와 final |
| reproducibility erosion | global random, unordered merge, time-quality termination | namespaced seeds, stable order와 normal-step rerun |
| verifier coupling | solver cache/summary를 재사용 | independent path와 corrupted-cache fault injection |
| benchmark mismatch | 다른 manifest card나 incomplete round를 직접 비교 | immutable multi-round manifest와 compare-not-allowed/`INCOMPLETE` 판정 |
| premature optimization | baseline 전에 complex undo/cache 최적화 | COW first, `RM-7` measured decision와 no-switch 허용 |
| infrastructure coupling | core가 transport/runtime 타입을 참조 | logical ports와 dependency review |
| result overclaim | bank/last failure가 proven status/reason이 됨 | conservative finalization과 evidence-bounded confidence |
| unsafe route dominance | 같은 request set의 ordering/vehicle/objective trade-off 손실 | Exact identity 분리, proven component-wise dominance 또는 conservative retention |
| pool/model growth | memory·seal·MIP build가 search budget을 잠식 | Admission/pruning provenance, incumbent pin, size/RSS/model telemetry와 experiment gate |
| projection drift | Bound profile objective와 MIP coefficient의 의미 불일치 | Versioned projection capability, unsupported profile skip와 reconstructed full comparator |
| solver/native/runtime failure | No incumbent, native load/init 실패나 callback/session 오염이 전체 solve를 손상 | Optional OR-Tools adapter 격리, status gate, cancellation/callback cleanup와 ALNS incumbent fallback |
| cover conversion corruption | Alias, stale metric, pair/vehicle 중복 | Immutable reconstruction, atomic pair removal, full propagation과 independent verification |
| hybrid nondeterminism | Time-limited multi-thread MIP가 strong replay claim을 깨뜨림 | Reproducibility class, exact backend/version/order/budget lineage와 statistical evidence |

### 16.2 Migration

Legacy 문서와 현재 코드 구조는 replacement inventory와 characterization 대상일 뿐 목표 계약 준수 증거가 아니다. Migration은 다음 순서를 SHOULD 따른다.

1. 현재 input/output/state/error 의미를 read-only로 characterization한다.
2. 목표 계약과 semantic compatibility matrix를 만든다.
3. 새 normalized core, evaluation, algorithm과 verifier를 logical ports 뒤에서 격리한다.
4. 승인된 legacy meaning만 versioned adapter로 변환하고 provenance를 보존한다.
5. 동일 logical input에 대해 side-effect 없는 shadow comparison을 수행한다.
6. 두 publication verification gate와 동일 manifest를 통과한 결과만 publication/benchmark candidate로 사용한다.
7. Versioned cutover, retry/idempotency/cancellation과 rollback을 증거로 검증한다.
8. 구현이 설계를 바꿔야 하면 관련 결정 기록과 문서를 같은 변경 단위에서 갱신한다.

### 16.3 Deferred resume criteria

| 항목 | 현재 보존할 경계 | 재개 조건 |
|---|---|---|
| route pool/MIP production activation | §11.7~§11.10의 solver-neutral evaluated-artifact/projected-column types와 selected OR-Tools CP-SAT policy | `RM-9A`~`RM-9C`, verified ALNS/result baseline, measured value와 별도 OR-Tools version/config/native packaging/SBOM/security/operations/compute-cost/admission/fallback/rollback 승인 |
| optional variants | 현재 atomic pair, fixed terminal, bank와 matrix contract | [Q-VAR-01](master-design-open-questions.md#q-var-01)의 선택·시점 결정, 대표 fixture, hand result와 core-impact feasibility 승인 |
| AWS target implementation/cutover | 논리 ports, status/artifact/idempotency/cancellation와 local semantic parity | `RM-8` AWS adapter contract, two-gate publication, shadow/cutover/rollback, workload·security/access/retention/audit·retry/recovery·performance/cost 운영 승인 |
| AWS 이외 physical topology 채택 | AWS와 동일한 논리 ports와 semantic contract | workload·security/access/retention/audit·retry/recovery·performance/cost evidence, provider parity suite와 별도 승인 |
| academic benchmark expansion | Win manifest/card와 verifier 재사용 경계 | Win baseline, authoritative format/result, RPDPTW mapping과 separate manifest 승인 |
| multi-trip/rotation | 현재 `oneway`/single `roundtrip`; 후속 trip도 pair crossing 금지와 depot `duration` 경계 보존 | `multiRotation` 값·trip/reset/depot window/resource 계약, 예제와 domain/algorithm/verifier 영향의 별도 승인 |
| dynamic routing | immutable solve snapshot과 cancellation port | event/replanning, state continuity, conflict와 SLA 계약 승인 |

선택 변형을 현재 pair invariant 완화로 미리 구현하거나 OR-Tools dependency를 core에 선반영해서는 안 된다. Solver-neutral target type의 설계, selected CP-SAT adapter 구현과 production 활성화는 서로 별개다. AWS target은 선택됐지만 AWS implementation/cutover와 AWS 이외 topology 채택은 안정된 logical contracts, parity와 운영 evidence를 각각 요구한다.

## 17. Open questions와 traceability

### 17.1 중앙 질문

28개 질문의 상태와 결정 단일 등록부는 [Master Design open questions](master-design-open-questions.md)다. 세션 29의 24개 결정, `Q-ALG-01` 후속 사용자 결정과 2026-07-26 `Q-INFRA-01` AWS target 승인까지 26개가 해결됐고, `Q-BENCH-02`만 프로토콜이 확정된 `OPEN — EXPERIMENT_REQUIRED`다. `Q-VAR-01`만 `DEFERRED`다. 등록부는 exact decision, evidence, gate와 이 문서 반영 절을 보존한다. 새 질문 ID를 이 문서에서 만들지 않는다.

### 17.2 상세 통합 입력

| 상세 문서 | Master가 소비한 경계 |
|---|---|
| [세션 20 — domain/input](master-design-sessions/20-domain-input-draft.md) | canonical model, pair invariant, normalization, fixed point, values, matrix와 search bank |
| [세션 21 — policy/objective](master-design-sessions/21-policy-objective-draft.md) | hard/metric/score/objective/plan 분리, profile binding과 customer extension |
| [세션 22 — algorithm](master-design-sessions/22-algorithm-draft.md) | portfolio, pair evaluator, ALNS, termination, COW/cache/rollback과 apply/undo gate |
| [세션 23 — result/benchmark](master-design-sessions/23-result-benchmark-draft.md) | result partition, diagnostics, verifier, recovery와 Win PoC comparison |
| [세션 24 — roadmap](master-design-sessions/24-roadmap-draft.md) | logical ports, phase order/evidence, risk, migration와 deferred resume criteria |
| [세션 18 — governance](master-design-sessions/18-document-governance.md) | REVIEW 상태, 규범어, source hierarchy와 변경 관리 |
| [세션 29 — 사용자 인터뷰](master-design-sessions/29-open-question-interview.md) | 26개 interview 대상의 authoritative 사용자 답변과 두 실험 대기 protocol |
| [세션 30 — 질문 통합](master-design-sessions/30-open-question-integration.md) | Master/register/영향 세션 반영 범위와 validation evidence |
| [세션 31 — Domain Design 통합](master-design-sessions/31-domain-design-integration.md) | 세션 29 결정에 맞춘 상세 Domain Design 재구성 범위와 정합성 validation |
| [Architecture Design](architecture-design.md) | Java 25/Maven package DAG, solver-neutral route-selection SPI, optional OR-Tools CP-SAT adapter와 native/fallback boundary |
| [OGC 2024 DMS ALNS–MIP 분석](../../../optichallenge/2024/2024_algorithms_DMS_v1.0.0/OGC2024_DMS_ALNS_MIP_design_ko.md) | SHA-256 `fd71f3bf03eb04af970e84bf4df8b72d27679e482dd585ea430b165fd3481e84`; destroy/adaptation, promising repair, rejected-route pool과 Set Covering feedback의 non-normative transfer evidence. 원 환경의 동적 재실행을 완료한 자료가 아니며, all-pickups-first, 3 rider types, complete assignment, scalar cost와 확인된 코드 결함은 canonical 계약으로 채택하지 않음 |

[Domain Design](domain-design.md) v2는 이 Master의 domain/input/normalization/travel/state/evaluation/result 경계를 상세화한 `REVIEW` 문서다. 세션 31에서 legacy의 고정 numeric/time defaults, node-sized matrix, generic feature, multi-trip 기본 활성화와 mixed bank/result 의미를 제거했다. 두 문서가 여전히 충돌하면 현재 `REVIEW` 단계의 conflict 절차와 이 문서 §1의 authority 순서를 따른다.

연구 근거는 [문제 정의](arranged/01_problem_definition.md), [초기해 휴리스틱](arranged/02_initial_solution_heuristics.md), [ALNS](arranged/03_alns_metaheuristic.md), [local search](arranged/05_local_search_moves.md), [실무 확장](arranged/06_practical_extensions.md)과 [논문·benchmark](arranged/07_papers_and_benchmarks.md)에 있다. 연구 예시는 승인된 업무 계약을 대신하지 않는다.

### 17.3 Requirement-to-evidence map

| 규범 영역 | 결정 | 상세 입력 | Roadmap | 필수 evidence |
|---|---|---|---|---|
| 표준 모델과 입력 | `C-01`, `C-05`~`C-14` | 세션 20 | `RM-1` | normalization, pair/property, boundary/overflow와 directed-matrix evidence |
| 확장 평가 | `C-03`~`C-05`, `C-15`, `C-18` | 세션 21 | `RM-2` | profile isolation, bind rejection, full evaluation와 comparator/guard |
| 탐색과 상태 | `C-06`, `C-08`~`C-09`, `C-13`, `C-16`~`C-17`, `C-22` | 세션 22 | `RM-3`, `RM-4`, `RM-7` | portfolio traces, rollback/cache equivalence, step rerun와 measured state decision |
| ALNS operator/repair | `C-06`, `C-08`, `C-16`, `P-20` | OGC 분석 §3을 corrected adaptation | `RM-4` | Operator fixture, central pair edit, shortlist→exact gate, adaptive/acceptance trace |
| Route pool | `C-17`, `P-15`, `P-19` | OGC 분석 §3.9의 immutable adaptation | `RM-9A` | Rejected-route harvest, append/import merge, dominance oracle, deterministic snapshot와 memory |
| Route-selection/hybrid | `C-17`, `P-16`~`P-19` | Domain §12.5~§12.8, OGC 분석 §4 | `RM-9B`, `RM-9C` | OR-Tools CP-SAT tiny-model oracle, materialization/full evaluation, status/native-runtime fallback, adopted-only feedback와 hybrid provenance |
| 결과와 benchmark | `C-15`, `C-18`, `C-21`~`C-22` | 세션 23 | `RM-5`, `RM-6` | verifier independence, result partition, manifest/cards와 deterministic champion |
| 시스템과 migration | `C-02`, `C-19`~`C-20` | 세션 24 | `RM-0`, `RM-8`, `RM-9` | document audit, logical-port integration, compatibility/rollback와 resume approval |

이 문서의 변경이 문제 의미, invariant, input/output, objective, numeric/time, roadmap gate 또는 논리 책임을 바꾸면 승인된 결정 기록과 영향을 받는 상세 문서를 같은 변경 단위에서 갱신해야 한다. 구현 진행 기록은 이 설계를 대체할 수 없다.
