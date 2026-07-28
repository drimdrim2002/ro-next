# Phase 03 사람용 구현 가이드 — Route propagation과 evaluation kernel

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 03 only
canonical_phase_count: 15
phase: "03"
phase_name: route-propagation-evaluation-kernel
canonical_phase_document: docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md
canonical_phase_review: docs/implementation/reviews/phase-03-review.md
canonical_phase_document_status: REVIEWED_CHANGES_REQUIRED
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: NOT_STARTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_date: 2026-07-29
live_inventory_snapshot_at: 2026-07-29T02:03:10+09:00
live_inventory_status_command: "git status --short | sha256sum"
live_inventory_status_sha256: 7ff2b5b6c96f7a76c6b2e21e904ed1a7f1cfa3c18f11682221c63a537475a4fb
live_inventory_approval_status: UNCOMMITTED_UNAPPROVED_SNAPSHOT
prerequisite_phases:
  - "00"
  - "01"
  - "02"
source_fingerprint_scheme: git_blob_at_inventory_observed_commit
source_sections_and_fingerprints:
  docs/master-design.md: "§4.1~4.6, §5~9, §12~13, §15.4, §16~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "§1, §3, §11~12, §16~18 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "§1, §5~8, §18~22 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/2026-07-26-domain-design.md: "§2, §7~10, §17.5~17.6, §18 | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "§1.2~1.4, §2, §5.6, §6 | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/architecture-domain-implementation-design.md: "§1.7~1.9, §2~3, §6~8, §19~25 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-* rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§1, §3~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~4, Phase 02~04, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§2, §5~9 | 250aa90ae568a6b32ec905fa5ee456d430ff72cf"
  docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md: "§1~15 | 74098f7e15cf75bcc443ae009cc475a9b60d63a3"
  docs/implementation/reviews/phase-03-review.md: "§1~7 | e473887ffbbb165f18ea6aaa76d6fc3917fc4af2"
expected_reader:
  - Java의 record, interface, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, time window, capacity propagation을 경험했다
  - ro-next의 RPDPTW identity, lifecycle, module 경계는 처음 접한다
owner_roles:
  implementation: RPDPTW Core/Evaluation owner
  upstream: Phase 02 Domain/Travel owner
  downstream_binding: Phase 04 Capability/Profile owner
  downstream_search: Phase 05 Pair/Insertion owner
  downstream_verification: Phase 07 Verification owner
  architecture: Phase 00 Architecture owner
  review: independent Phase 03 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P03-PROPAGATION
  - E-P03-EVALUATION
  - E-P03-COMPARATOR
```

> 이 문서는 사람이 Phase 03을 학습하고, gate가 열린 뒤 구현하고, 스스로 판정하기 위한 작업 지시서다. `7cc890e…` HEAD에는 Phase 03 production code, target Maven module, test 또는 evidence가 없다. 별도의 미커밋 live tree에는 Phase 00 reactor/wrapper와 package skeleton이 있지만 아직 승인되지 않았고 Phase 03 production type/test/evidence도 없다. 따라서 아래 Java 이름과 signature는 별도 표시가 없는 한 **제안 후보(proposed)**이며, 파일 존재나 승인된 public API를 뜻하지 않는다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 03은 불변 방문 순서 하나를 처음부터 끝까지 다시 따라가며 도착 시각, 대기, 서비스, 적재량, 이동·정차 자원을 계산하고, 그 물리 사실을 고객 중립 metric과 평가 계약으로 바꾸는 **순수하고 결정적인 계산 kernel**을 만든다.

이 kernel은 “어떤 방문 순서를 시험할지”를 결정하지 않는다. 그 일은 Phase 05 이후 solver가 한다. Phase 03은 주어진 순서가 물리적으로 무엇을 의미하는지, 정상 hard 위반인지 손상된 입력인지, 그리고 평가 가능한 불변 artifact가 무엇인지를 판정한다.

## 2. 큰 그림

### 2.1 제품과 도메인 배경

이 제품은 배송 요청과 차량을 받아 RPDPTW 경로를 찾고, solver의 주장을 독립 검증한 뒤에만 결과를 게시하는 시스템을 목표로 한다. CVRPTW와 달리 실제 pickup-delivery 요청은 두 service node를 하나의 원자적 request로 소유한다. Pickup과 delivery는 같은 concrete vehicle route에 정확히 한 번씩 있어야 하고 pickup이 먼저여야 한다.

Phase 03이 필요한 이유는 방문 집합이 같아도 순서에 따라 다음이 모두 달라지기 때문이다.

- 모든 route prefix의 적재량
- directed arc별 이동 시간과 거리
- 고객 시간창의 대기와 서비스 시작·종료
- 차량 근무창 사이의 full-arc 재시작과 휴식
- stop, drive time, drive distance 같은 route 전체 자원
- hard feasibility와 이후 metric/score/objective

Solver cache나 insertion delta가 이 계산을 빠르게 보조할 수는 있지만, 정답의 소유자는 언제나 cache-free full recomputation이다.

### 2.2 Canonical 15 Phase 중 위치

이 저장소의 canonical 구현 단계는 **Phase 00~14, 총 15개**다.

| Phase | 주제 | Phase 03과의 관계 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | `rpdptw-core`, test-fixtures, architecture-rules와 build gate를 제공해야 한다 |
| 01 | 내부 표준 입력과 정규화 | 숫자·시간·서비스·호환성 의미를 정규화한다 |
| 02 | 이동 자료 준비와 immutable problem | Phase 03의 유일한 물리 authority인 `ProblemInstance`와 `PreparedTravel`을 만든다 |
| **03** | **경로 전파 계산과 평가 kernel** | **이 가이드의 소유 범위** |
| 04 | 재사용 기능과 고객 profile | Phase 03 SPI를 exact profile/capability에 bind한다 |
| 05 | Pair insertion과 초기 후보군 | Pure kernel과 comparator를 사용해 삽입과 최대 8개 초기 후보를 평가한다 |
| 06 | COW ALNS와 재현성 | Kernel을 authoritative full evaluation으로 사용한다 |
| 07 | 독립 검증과 최종 결과 | 같은 authority에서 cache 없이 다시 계산하되 solver를 신뢰하지 않는다 |
| 08 | Application interface와 local 실행 | 검증된 local end-to-end 경로를 조립한다 |
| 09 | DB 없는 object storage | Immutable artifact와 CAS 상태를 저장한다 |
| 10 | Provider-neutral coordinator | 선언 worker completeness와 deterministic champion을 관리한다 |
| 11 | AWS reference distribution | S3 + Step Functions + Lambda adapter를 붙인다 |
| 12 | Provider substitution | 승인된 provider 축만 교체한다 |
| 13 | Optional hybrid route selection | **14A acceptance 뒤에만** 열 수 있는 `C-17` gated branch다 |
| 14 | 14A ALNS benchmark / 14B official cutover | 14A와 14B는 서로 다른 evidence·authority gate다 |

현재 ALNS-first critical path는 다음과 같다.

```text
00 → 01 → 02 → [03] → 04 → 05 → 06 → 07 → 08 → 14A
```

Phase 13은 이 경로의 선행조건이 아니다. Phase 14A가 발행한 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 별도 `C-17` 승인이 있어야 검토할 수 있다. Phase 14B의 production authority가 없다는 이유로 14A를 막아서도 안 되고, 반대로 14A receipt가 13 또는 14B를 자동 승인하지도 않는다.

### 2.3 Producer와 consumer

```text
Phase 01 normalized meaning
        ↓
Phase 02 ProblemInstance + PreparedTravel
        ↓ exact identity/coverage gate
Phase 03 RoutePlan
        → structure validation
        → full propagation
        → neutral metrics
        → composed hard constraints
        → score
        → objective vector/comparator
        ↓
Phase 04 exact profile binding
Phase 05 insertion/portfolio
Phase 07 independent recomputation
```

핵심 producer-consumer 계약은 다음과 같다.

| 경계 | Producer가 보장할 것 | Consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 02 → 03 | Immutable problem/travel, complete directed lookup, dense ID, fingerprint equality | Raw `D/U`, 좌표, 속도, reverse arc 또는 provider를 다시 읽기 |
| Phase 03 → 04 | Customer-neutral SPI, facts, unit/type/fingerprint/failure 계약 | Propagation 복제, customer-name 분기, 숨은 default |
| Phase 03/04 → 05 | Pure evaluation, exact bound declaration, comparator 의미 | Ad hoc route-vector 합산을 solution authority로 만들기 |
| Phase 03 → 07 | Cache-free 재계산에 필요한 portable authority 계약 | Search cache, solver feasibility flag나 summary를 검증 authority로 사용하기 |

## 3. Source authority와 fingerprint

### 3.1 충돌 해소 순서

읽기와 구현 판단에는 다음 순서를 적용한다.

```text
사용자 고정 지시
→ canonical Master
→ exact Q-* question register
→ Final Domain 의미
→ Final Architecture 배치
→ Integrated 15-Phase design
→ implementation plan / phase document / review
→ historical cross-check
```

이 순서의 문서 역할을 시점별로 분리한다.

| 입력 층 | 역할 | 이 가이드의 적용 |
|---|---|---|
| 현재 상위 지도 | `docs/master-design.md`, `docs/domain-design.md`, `docs/architecture-design.md`의 현재 `REVIEW` 문서 | 최신 전체 의미와 package/module 배치 검토 입력이다. `REVIEW` 상태이므로 그 문장만으로 승인 record를 대신하지 않는다 |
| 사용자 고정 dated source | `docs/2026-07-26-domain-design.md`, `docs/2026-07-26-architecture-design.md` | 이 구현 문서 세트에 고정된 domain meaning과 architecture placement baseline이다 |
| 통합 설계 | `docs/architecture-domain-implementation-design.md` | 위 의미를 15 Phase와 구현 seam으로 연결한다. 오래된 나열 순서나 owner drift가 상위 계약을 자동 변경하지 않는다 |
| Implementation baseline | README, master plan, canonical Phase 03, original Phase review | 실행 순서, test/evidence와 교정 이력을 제공한다. 상위 owner/package를 바꿀 때는 승인 record와 관련 authority/architecture test를 같은 변경 단위로 갱신해야 한다 |

현재 상위 지도와 dated source는 모두 typed physical fact 계약을 `evaluation.api`에 두는 방향을 가리키지만, canonical Phase 03 review의 cycle 회피 교정은 propagation-owned fact/facet 방향을 제시한다. 이 충돌은 §7.4와 §9에서 `CROSS-PHASE ARCHITECTURE BLOCKER`로 보존한다. 이 가이드가 어느 한 방향을 승인된 사실로 승격하지 않는다.

특히 Final Domain과 Final Architecture의 일부 절에는 `Q-INFRA-01 DEFERRED`, 상태 `25/1/2`라는 오래된 문구가 남아 있다. 현재 적용값은 canonical Master와 질문 등록부의 **`Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`**이다. 이 해소는 Phase 03 core에 AWS dependency를 넣으라는 뜻이 아니다.

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md)과 `docs/codex/`는 누락·퇴행을 확인하는 역사 자료일 뿐 현재 결정을 바꾸는 authority가 아니다.

### 3.2 검증 가능한 source fingerprint

아래 값은 `inventory_observed_at_commit`에서 `git rev-parse HEAD:<path>`로 얻은 **Git blob hash**다. Whole-file SHA-256과 혼동하지 않는다. Source가 바뀌면 heading의 요구 의미와 hash를 함께 다시 대조한다.

| Source | 직접 읽을 section | Git blob |
|---|---|---|
| [Canonical Master](../../../master-design.md) | §4.1~4.6, §5~9, §12~13, §15.4, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Current Domain map](../../../domain-design.md) | §1, §3, §11~12, §16~18 | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Current Architecture map](../../../architecture-design.md) | §1, §5~8, §18~22 | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Final Domain](../../../2026-07-26-domain-design.md) | §2, §7~10, §17.5~17.6, §18 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Final Architecture](../../../2026-07-26-architecture-design.md) | §1.2~1.4, §2, §5.6, §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §1.7~1.9, §2~3, §6~8, §19~25 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~4, `Q-NUM`, `Q-TIME`, `Q-IN`, `Q-REQ`, `Q-OBJ`, `Q-BENCH-02`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | §1, §3~7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §2~4, Phase 02~04, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | §2, §5~9 | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Phase 02](../../phases/phase-02-prepared-travel-immutable-problem.md) | §4, §6, §11~14 | `8b5f115369ca2189c80de12868fb3a63104d9228` |
| [Canonical Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md) | 전체, 특히 §3~15 | `74098f7e15cf75bcc443ae009cc475a9b60d63a3` |
| [Phase 03 review](../../reviews/phase-03-review.md) | §1, §4~7 | `e473887ffbbb165f18ea6aaa76d6fc3917fc4af2` |
| [Phase 04](../../phases/phase-04-capabilities-customer-profiles.md) | §4, §7, §13~15 | `e0a68fd442a7db383e4a650e4a561234c323b1fe` |

Historical cross-check는 [2026-07-26 Master 초안](../../../2026-07-26-master-design.md)의
§3.4, §6.3, §10.2만 사용했으며 Git blob은
`d4f7fbf058711a02451de911bc86b1cd2f426218`이다. 이 값은 현재 authority
fingerprint가 아니다. `docs/codex/`는 현재 계약의 source로 소비하지 않았다.

재검증 명령:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md
git rev-parse HEAD:docs/implementation/reviews/phase-03-review.md
```

Hash가 다르면 “문서가 바뀌었다”에서 멈추지 말고 다음을 기록한다.

1. 바뀐 heading과 요구
2. 이 가이드의 영향 section
3. Java contract/test/evidence 영향
4. OPEN/GATED/deferred 상태 변화 여부와 승인 record

## 4. RPDPTW domain primer와 용어집

### 4.1 CVRPTW와 다른 request identity

CVRPTW의 흔한 단위는 customer visit 하나다. 이 프로젝트의 원자 단위는 `Request`다.

| 개념 | 정확한 의미 | 흔한 오해 |
|---|---|---|
| `Order` | 외부 business 입력 | Solver pair identity와 같지 않다 |
| `Request` | Pickup/delivery 의미를 소유한 원자 운송 업무 | Node 하나가 아니다 |
| `SolverNodeId` | Terminal/pickup/delivery service 정의 | Physical location과 같지 않다 |
| `PhysicalLocationId` | Directed travel table의 endpoint | 같은 location의 node들을 합치지 않는다 |
| `Visit` | Route 안에서 node가 나타난 한 occurrence | Node 정의 자체가 아니다 |
| `RoutePlan` | Concrete vehicle + terminal policy + ordered service sequence | Mutable search draft가 아니다 |

같은 건물에서 pickup과 delivery를 하더라도 node identity는 둘이고 location만 같을 수 있다. 이때 self travel은 `0/0`이지만 service와 load delta는 두 번 각각 적용한다.

### 4.2 두 service pattern

**Delivery-only**

- 화물은 route 출발 전에 이미 실려 있다.
- Logical pickup은 pair 소유권에는 참여하지만 physical visit이 아니다.
- 그 route의 모든 delivery-only demand 합이 initial load다.
- Delivery service에서 load가 감소한다.

**Real pickup-delivery**

- Pickup과 delivery 모두 실제 service visit이다.
- Pickup에서 load가 증가하고 delivery에서 감소한다.
- 같은 route, 같은 concrete vehicle, pickup-before-delivery가 필수다.

두 pattern은 한 single-trip route에 섞을 수 있다.

```text
initial load: delivery-only 4
depot → real pickup(+3) → delivery-only delivery(-4) → real delivery(-3)
load:              7                            3                    0
```

최종 load만 0이면 충분하지 않다. 모든 prefix에서 weight와 volume 각각 `0 <= load <= capacity`여야 한다.

### 4.3 Identity와 lifecycle

| 상태/artifact | Mutable인가 | Stable authority인가 |
|---|---:|---:|
| `ProblemInstance` | 아니오 | 예, Phase 02 authority |
| `PreparedTravel` | 아니오 | 예, Phase 02 authority |
| `RoutePlan` | 아니오 | 예, 한 kernel 호출의 경로 authority |
| `RouteFacts` | 아니오 | 예, exact input identity에 결합된 파생 fact |
| Search cache/insertion delta | 수명에 따라 다름 | 아니오, 재계산 가능한 최적화 |
| Phase 05 `TrialDraft` | 예 | 아니오 |
| Phase 05/06 `SearchSnapshot` | 아니오 | 구조와 full evaluation을 통과한 뒤에만 예 |
| Phase 07 `VerifiedSolution` | 아니오 | Candidate verifier `PASS` 뒤 예 |

Fingerprint는 “어디에 저장됐는가”가 아니라 “무슨 의미와 bytes에서 만들어졌는가”를 나타낸다. Object identity, `toString()`, unordered map iteration, file mtime, `latest`, thread ID와 wall clock을 semantic fingerprint에 넣지 않는다.

### 4.4 Stable route 불변조건

정상 route는 동시에 다음을 만족한다.

1. 정확히 한 concrete vehicle에 결합한다.
2. 첫 node는 그 vehicle의 start terminal이다.
3. Oneway는 마지막 customer에서 끝나고, single roundtrip은 같은 approved terminal로 복귀한다.
4. Current single-trip에서는 internal depot revisit와 foreign terminal이 없다.
5. Real pair가 같은 route에 각각 한 번 있고 pickup index가 더 작다.
6. Delivery-only logical pickup은 visit가 아니지만 route initial-load ownership에 포함된다.
7. 모든 weight/volume prefix가 capacity 범위 안이다.
8. Complete directed prepared travel과 정규화된 time/window/service 사실만 사용한다.
9. 완성 single-trip의 final load는 0이다.

Partial pair, duplicate pair, delivery-before-pickup, foreign terminal과 fingerprint mismatch는 “배정하기 어려움”이 아니라 손상 또는 구현 결함이다.

### 4.5 Propagation, metric, score, objective

| 층 | 질문 | 포함 | 금지 |
|---|---|---|---|
| Structure/static gate | Route 표현과 authority가 유효한가 | Pair, terminal, identity | 낮은 score로 숨기기 |
| Propagation | 실제 운행하면 무슨 일이 생기는가 | Time, load, travel, stop/resource, canonical hard rule | 가격, customer preference |
| Neutral metric | 얼마나 발생했는가 | 거리, 대기, 운행 시간 같은 단위 있는 양 | 좋고 나쁨, 단가 |
| Composed hard | Fact/metric 조합이 허용되는가 | Typed rejection | Finite penalty |
| Score | Feasible metric의 비용·soft penalty는 얼마인가 | Typed parameters와 metric snapshot | Raw route/input 재해석 |
| Objective | 어떤 차원을 어떤 방향·우선순위로 비교하는가 | Ordered lexicographic vector | Hidden Big-M |
| Stable tie | Business objective가 완전히 같을 때 어떤 total order인가 | 승인된 stable key | Non-tied objective 뒤집기 |

현재 적용할 순서는 Phase 03 review가 바로잡은 다음 흐름이다.

```text
identity/structure
→ physical propagation과 canonical hard gate
→ neutral metrics
→ composed hard constraints
→ scores
→ objective vector
→ comparator
```

Integrated design §7.5의 오래된 “composed hard → metric” 나열을 그대로 구현하지 않는다. Canonical Master §9.1, Final Domain §10과 Phase 03 review F-P03-001의 교정이 우선한다.

### 4.6 결과 삼분법

| 결과 | 의미 | 예 |
|---|---|---|
| `Feasible` | Authority·구조·계산이 정상이고 모든 hard rule 통과 | Inclusive capacity/window/resource boundary 만족 |
| `Infeasible` | Authority와 구조는 정상이며 계산도 신뢰할 수 있지만 hard bound 위반 | Prefix capacity 초과, service window 실패, full arc가 어느 work window에도 못 들어감 |
| `Invalid` | 입력 identity, route 구조, component contract 또는 arithmetic을 신뢰할 수 없음 | Partial pair, travel fingerprint mismatch, missing lookup, overflow, duplicate metric key, component exception |

`Invalid`를 insertion rejection, unassignment reason, 큰 비용 또는 maximum score로 바꾸지 않는다. `ConstraintRejection`도 Phase 07의 최종 diagnostic이 아니라 exact route/evaluation identity에 결합된 call-local machine fact다.

## 5. 시작 전 읽기 순서와 entry gate

### 5.1 읽기 순서

다음 순서를 지키면 “물리 의미”, “Java 배치”, “현재 status”를 섞지 않을 수 있다.

1. [Canonical Master §4](../../../master-design.md#4-구현-아키텍처와-책임-경계), [§6](../../../master-design.md#6-핵심-불변조건과-atomic-mutation), [§7.2~7.3](../../../master-design.md#72-fixed-point와-checked-arithmetic), [§8](../../../master-design.md#8-directed-distancetime-matrix-계약), [§9](../../../master-design.md#9-extensible-policy-evaluation과-profile-architecture)를 읽는다.
2. [Current Domain §3.1](../../../domain-design.md#31-architecture-package-mapping), [§11~12](../../../domain-design.md#11-route-propagation과-resources)와 [Current Architecture §5~8](../../../architecture-design.md#5-recommended-maven-multi-module-tree)을 최신 `REVIEW` 상위 지도로 읽는다.
3. 사용자 고정 dated source인 [Final Domain primer](../../../2026-07-26-domain-design.md#primer), [immutable model](../../../2026-07-26-domain-design.md#7-immutable-solver-model), [propagation](../../../2026-07-26-domain-design.md#9-route-propagation과-resource), [evaluation](../../../2026-07-26-domain-design.md#10-evaluation-profile과-objective)을 읽는다.
4. [Final Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계)와 [§5.6](../../../2026-07-26-architecture-design.md#56-test와-evidence)에서 dated module/package/test baseline을 확인한다.
5. [Integrated design Phase 02](../../../architecture-domain-implementation-design.md#6-phase-2--travel-preparation과-immutable-probleminstance), [Phase 03](../../../architecture-domain-implementation-design.md#7-phase-3--경로-전파-계산과-평가-kernel), [Phase 04](../../../architecture-domain-implementation-design.md#8-phase-4--capability와-data-driven-customer-profile)을 이어 읽는다.
6. [Implementation README §3](../../README.md#3-source-authority)와 [§6](../../README.md#6-phase-작업-순서)에서 최신 ALNS-first overlay와 gate를 확인한다.
7. [Master Plan Phase 03](../../master-realization-plan.md#phase-03--경로-전파-계산과-평가-kernel), [test 전략](../../master-realization-plan.md#8-공통-테스트-전략), [DoD](../../master-realization-plan.md#11-definition-of-done)를 읽는다.
8. [Canonical Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md) 전체와 [독립 review](../../reviews/phase-03-review.md)를 함께 읽는다.
9. [Phase 02 handoff](../../phases/phase-02-prepared-travel-immutable-problem.md#132-phase-02--phase-03)와 [Phase 04 handoff](../../phases/phase-04-capabilities-customer-profiles.md#141-previous--actual-phase-03-unaccepted)을 대조한다.
10. [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry)와 [§8](../../execution-progress-and-results.md#8-현재-blockers-open-gates와-남은-이슈)에서 live status를 확인한다.

### 5.2 Entry gate를 확인하는 방법

다음 조건은 모두 AND다.

| Gate | 받아야 할 evidence | 현재 관찰 | 행동 |
|---|---|---|---|
| Phase 00 accepted | Reactor, `rpdptw-core`, architecture rule, `E-P00-BUILD/ARCH`, receipt | 미커밋 reactor/wrapper/skeleton은 있으나 Fix 01 진행 중이고 receipt 없음 | Source 구현 시작 금지 |
| Phase 01 accepted | Immutable normalized numeric/time/service artifact와 `E-P01-*` | 없음 | Source 구현 시작 금지 |
| Phase 02 accepted | `ProblemInstance`, `PreparedTravel`, `E-P02-*`, independent review와 receipt | 문서만 actual, implementation/evidence 없음 | Source 구현 시작 금지 |
| Phase 03 contract review | Package/type/unit/failure triage 승인 | 의미는 상세하지만 이름은 proposed | Freeze 전 public API 약속 금지 |
| Fact/package owner review | `evaluation.api`-owned 대 propagation-owned fact/failure/facet, import DAG와 architecture test 승인 | 상위 지도/dataset source와 canonical Phase review가 충돌 | Package/file 생성 금지 |
| Cross-Phase owner | Solution-level evaluator와 tie boundary 공동 승인 | Review residual blocker 2건 | Route-only safe point 유지 |
| Scheduler identity | Exact task ID, implementer, independent reviewer | 제공되지 않음 | Authoritative 상태 전이 금지 |

Phase 02 handoff를 받으면 먼저 다음 equality와 completeness를 확인한다.

```text
ProblemInstance.preparedTravelFingerprint
  == PreparedTravel.fingerprint

all route-reachable SolverNodeId
  → exactly one PhysicalLocationId

all used VehicleId × all directed location pairs
  → exactly one resolved integer travel time

problem/travel policy identities
  == Phase02HandoffManifest declarations
```

Mismatch가 있으면 temporary lookup, reverse arc, 좌표 계산 또는 default로 보완하지 않는다.

### 5.3 마지막 안전 지점

현재처럼 entry gate가 닫힌 상태에서 허용되는 마지막 안전 지점은 다음이다.

- 이 가이드와 canonical 문서 읽기
- Hand-calculated fixture와 independent oracle 설계
- Proposed API와 두 package-owner 후보의 import DAG review
- Source fingerprint와 inventory 재확인

다음은 gate가 열리기 전 하지 않는다.

- `rpdptw/core` production source 생성
- Fact/failure/facet owner 승인 전 해당 package/file 생성
- Root POM/module 구조 변경
- Placeholder `com.ronext.optimizer.*`를 Phase 03 kernel로 개조
- 임의 test-only 값을 production default로 채우기
- Scheduler registry를 구현자가 직접 승격하기

<a id="6-실제-inventory-현재-상태와-목표-상태"></a>

## 6. 실제 inventory: HEAD baseline, 미커밋 live snapshot과 목표

### 6.1 재현 가능한 HEAD baseline

다음 baseline은 live filesystem이 아니라 commit `7cc890ee1d0805df5ae14b633127fade4f978639`의 Git tree다.

```bash
git rev-parse HEAD
git ls-tree -r --name-only 7cc890ee1d0805df5ae14b633127fade4f978639
git rev-parse 7cc890ee1d0805df5ae14b633127fade4f978639:pom.xml
```

| 항목 | HEAD baseline 관찰 | 해석 |
|---|---|---|
| Root POM | 단일 `com.ronext:ro-next` JAR, blob `f8a411eadd4a5c01d8dd09fdea462738ca63d65f` | Historical implementation baseline |
| Java | Root `src/main/java` 6개, `src/test/java` 1개 | RPDPTW kernel이 아닌 placeholder |
| Wrapper/reactor | `mvnw`, `.mvn/`, `rpdptw/`, `build/` 없음 | HEAD에는 Phase 00 target가 없다 |
| Phase 03 | Production type/test/evidence 없음 | `NOT_STARTED / NOT_ACCEPTED / NOT_PRODUCED` |

HEAD의 원래 placeholder 경로는 live filesystem link가 아니다. Snapshot 자체는 `git show 7cc890e:<path>`로 읽고, 현재 이동 중인 사본은 [live `AlnsBatchEngine`](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java)과 [live characterization test](../../../../legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java)로 구분한다. 어느 사본도 Phase 03 구현이나 evidence가 아니다.

### 6.2 미커밋·미승인 live drift snapshot

아래 표는 `2026-07-29T02:03:10+09:00`에 관찰한 working tree snapshot이다. `git status --short | sha256sum` 결과는 `7ff2b5b6c96f7a76c6b2e21e904ed1a7f1cfa3c18f11682221c63a537475a4fb`다. 이 digest는 그 시각의 short-status text를 식별할 뿐 untracked directory 내용 전체를 봉인하지 않으므로, root POM과 live progress blob을 함께 기록한다.

| 항목 | Live 관찰 | 승인/Phase 03 해석 |
|---|---|---|
| Root [`pom.xml`](../../../../pom.xml) | `packaging=pom`인 `ro-next-parent` reactor, blob `1dc675ba17b7f2202f34a22131f152cc2868b075` | 미커밋 Phase 00 drift; accepted parent가 아님 |
| Wrapper | Executable `./mvnw`, `.mvn/`, `mvnw.cmd` 존재 | 미승인 도구; receipt 전 authoritative evidence command가 아님 |
| Maven tree | Repository target 영역에 root 포함 13개 POM, `rpdptw/core`, `build/test-fixtures`, `build/architecture-rules` 존재 | 파일 존재만으로 predecessor acceptance 아님 |
| Target namespace | `src/main/java`의 `package-info.java` skeleton 23개, 그중 core 8개 | Package skeleton일 뿐 Phase 03 contract freeze/구현 아님 |
| Live tests | Architecture/fixture `*Test.java` 9개, Phase 03 required test 0개, `*IT.java` 0개 | Phase 00 remediation test이며 `E-P03-*`가 아님 |
| Legacy | HEAD root `src/**` 7개는 삭제 상태이고 `legacy/gcp-placeholder`로 이동·확장 중 | Snapshot과 live link를 섞지 않는다 |
| Live progress | Blob `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d`; Phase 00 Fix 01 진행 중, receipt `NOT_PRODUCED` | 총괄 scheduler 상태이며 Phase 00/03 acceptance 아님 |
| Phase 03 | `RoutePlan`, propagator, facts/evaluation type, required test와 `E-P03-*` 0 | 계속 `NOT_STARTED / NOT_ACCEPTED / NOT_PRODUCED` |

Live tree는 다른 작업 세션이 계속 바꿀 수 있다. 구현 착수 시 timestamp, HEAD, `git status --short` digest, relevant live POM/progress blob과 exact file/test count를 새로 계산한다. 이전 표를 “현재”라고 재사용하지 않는다.

### 6.3 Live diagnostic과 accepted future command 구분

현재 live wrapper로 다음을 실행할 수는 있지만, Phase 00 independent correction/review/receipt가 끝나지 않았으므로 결과는 **unaccepted diagnostic**일 뿐이다.

```bash
./mvnw -B -ntp -Dstyle.color=never clean verify
```

이 명령이 성공해도 live Phase 00 skeleton/test만 검사하며 Phase 03 required test는 0건이다. `E-P03-*`, Phase 03 exit 또는 package-owner 승인을 증명하지 않는다. 반대로 HEAD baseline을 재현할 때는 `./mvnw`가 존재한다고 가정해서도 안 된다.

Phase 03 evidence command로 승격하려면 다음이 모두 필요하다.

1. Phase 00 accepted wrapper/reactor와 post-review receipt
2. Phase 01~02 accepted artifacts/receipts
3. §9의 package-owner/DAG 승인
4. §11.5의 test discovery/wiring/zero-test precondition
5. 실제 Phase 03 source, exact 42-method suite와 fresh report

Future module command는 §11.5의 acceptance와 존재 precondition을 먼저 통과한 뒤에만 사용한다.

## 7. Scope, 결정 상태와 사람 승인

### 7.1 Phase 03 scope

- `RoutePlan`의 immutable 구조와 exact identity
- Route-local terminal/pair/precedence 구조 검사
- Delivery-only initial load와 real pair delta
- Complete directed travel을 통한 full propagation
- Arrival/wait/service/departure와 normalized window 소비
- Full arc가 한 vehicle work window 안에 들어가는지 검사
- Stop, distance, drive time, waiting, service, inter-work-window rest, operational time
- Inclusive route resource limit와 typed absence
- Checked `long` arithmetic와 overflow fail-closed
- Physical hard fact, neutral metric, composed hard, score, objective/comparator 층 분리
- Immutable facts/result/fingerprint와 cache-free equality
- Independent oracle, corruption, fault, reproducibility와 architecture evidence

### 7.2 Non-scope

- Raw input parsing과 numeric/time normalization
- Missing travel 생성, rounding과 provider lookup
- Customer/profile descriptor, registry, authorization과 production preset
- Pair insertion enumeration, route/bank mutation과 initial portfolio
- COW ALNS, adaptive state, acceptance와 execution budget
- Candidate/result verifier verdict, final diagnostic, audit와 publication
- Multi-trip/rotation과 optional variants
- Route pool, MIP, OR-Tools CP-SAT와 hybrid adoption
- AWS/GCP/storage/workflow/HTTP DTO와 SDK
- Public Java/wire API와 production numeric default 승인

### 7.3 확정된 의미

| 항목 | 상태 | 구현 의미 |
|---|---|---|
| Phase 02 authority only | FIXED | Raw travel/time을 재해석하지 않는다 |
| Checked integer math | FIXED | `double`, epsilon, saturation, sentinel 금지 |
| Full recomputation authority | FIXED | Cache hit/miss가 결과를 바꾸지 않는다 |
| Full-arc rule | FIXED | Arc 중간 pause/resume 없이 한 work window에서 전체 이동 |
| Service window rule | FIXED, explicit | `START_ONLY` 또는 `COMPLETE_WITHIN_WINDOW`; 숨은 선택 금지 |
| Layer order | FIXED | Physical hard → metric → composed hard → score → objective |
| Hard no-penalty | FIXED | Hard violation을 score/SA가 상쇄하지 못한다 |
| Objective priority | FIXED | Ordered lexicographic dimension, hidden Big-M 금지 |
| Failure boundary | FIXED meaning | Normal hard 위반과 invalid/corruption을 분리한다 |
| Cache | FIXED non-authority | 성능 seam은 downstream에서 소유할 수 있으나 full equality 필수 |

### 7.4 OPEN/GATED/deferred

| 항목 | 상태 | Phase 03 행동 | 해제 authority |
|---|---|---|---|
| Final internal type/signature | PROPOSED/OPEN | Skeletal contract만 사용 | Core/Evaluation + Architecture review |
| Physical fact/failure/facet owner와 package DAG | CROSS-PHASE ARCHITECTURE BLOCKER | 후보 A/B와 consumer import만 review; 실제 package/file 생성 금지 | Core/Evaluation + Architecture owner, 관련 authority/ADR 동시 변경 |
| Full-solution evaluator owner/API | CROSS-PHASE BLOCKER | Route artifact 합산 금지 | Phase 03~05 공동 review |
| Business equality 대 context tie | CROSS-PHASE BLOCKER | Non-tied objective 우선 의미만 고정 | Phase 03/05/06 공동 review |
| Typed facet SPI | OPEN/PROPOSED | Base acceptance는 empty facet set | `ADR-004`, Phase 04/07 parity |
| `Q-BENCH-02` 수치 | OPEN — EXPERIMENT_REQUIRED | Test-only 명시값만 허용, official default 없음 | Benchmark·Quality calibration/approval |
| `C-17` route pool/MIP | GATED | Dependency/API/backend 선반영 금지 | 14A receipt + 별도 scope/backend/운영 승인 |
| `Q-VAR-01` | DEFERRED | 질문·활성화 금지 | Product·Domain·Algorithm restart evidence |
| Multi-trip/rotation | DEFERRED FEATURE | Oneway/single roundtrip만 | 별도 trip/reset/resource 계약 승인 |
| Public API/schema | OPEN | Internal proposed contract로만 유지 | Product/API/Data review |
| Performance threshold | OPEN | Complexity와 측정법만 기록 | Performance/Operations workload 승인 |

### 7.5 사람 승인 checkpoint

| 시점 | 질문 | 승인 없을 때 마지막 안전 지점 |
|---|---|---|
| 구현 전 | Phase 00~02 acceptance receipt와 exact artifacts가 있는가 | 문서·fixture 설계 |
| API freeze 전 | Fact/failure/facet owner, 후보 A/B 중 하나의 import DAG와 proposed type hierarchy가 승인됐는가 | Package-private skeleton도 생성하지 않음 |
| Evaluation 구현 전 | Metric-before-composed-hard와 component failure 경계가 합의됐는가 | Propagation만 review |
| Solution ranking 전 | Full-solution evaluator owner/API가 승인됐는가 | Route-only artifact |
| Comparator total order 전 | Business equality와 insertion/solution tie가 분리됐는가 | Business vector compare만 |
| Facet 구현 전 | 실제 새 물리 상태와 `ADR-004`, verifier parity가 있는가 | Empty facet set |
| Evidence 봉인 전 | Required test/method가 fresh report에 모두 있는가 | `IMPLEMENTED_PENDING_EVIDENCE` |
| Handoff 전 | Independent review와 post-review receipt가 있는가 | Handoff candidate, not accepted handoff |

## 8. 학습 경로

### 8.1 1단계 — 개념

**목적:** CVRPTW의 “한 customer 삽입” 사고에서 RPDPTW의 request pair, location/node 분리와 prefix load 사고로 전환한다.

작은 실습:

1. Delivery-only 4, real pair 3, capacity 6인 route 두 개를 손으로 만든다.
2. Visit 집합은 같지만 pickup 위치만 바꿔 prefix load가 7이 되는 반례를 찾는다.
3. Same-location pickup/delivery가 travel과 stop은 늘리지 않지만 service와 load는 바꾸는 이유를 설명한다.

완료 신호:

- Logical pickup을 가짜 depot visit로 만들지 않는다.
- Final load만이 아니라 모든 prefix를 계산한다.
- Node identity와 physical location identity를 구분한다.

자문 질문:

- 이 위반은 정상 `Infeasible`인가, 구조 손상 `Invalid`인가?
- 지금 계산한 값은 물리 fact인가 고객 score인가?

### 8.2 2단계 — 작은 탐색과 손 계산

**목적:** Production helper 없이 propagation oracle을 만들 수 있을 만큼 시간·load·resource 의미를 이해한다.

§11.2의 mixed route를 종이에 event table로 계산한다. 각 arc마다 다음을 적는다.

```text
departure
→ selected work window
→ arcStart / arrival
→ service window
→ serviceStart / serviceEnd
→ load delta
→ stop/resource totals
```

완료 신호:

- A→B arc가 첫 work window에 못 들어가면 현재 위치에서 쉬고 다음 window start부터 전체 arc를 이동한다.
- Rest와 drive를 중복 계산하지 않는다.
- `routeOperationalTime`의 다섯 성분을 따로 합산한다.

자문 질문:

- `departure + U == workWindow.end`는 허용되는가?
- `serviceStart == close`와 `serviceEnd == close`는 어떤 rule에서 허용되는가?
- Event가 `planEnd`와 같으면 왜 허용되지 않는가?

### 8.3 3단계 — 실제 변경

**목적:** Gate가 열린 뒤 구조/propagation/evaluation을 red→green으로 구현한다.

진행 순서:

```text
identity/structure red
→ hand propagation red
→ boundary/corruption red
→ evaluation layering red
→ comparator/property red
→ architecture/reproducibility green
```

완료 신호:

- 각 work package가 자신의 exact test와 failure 해석을 가진다.
- Production result로 expected oracle를 만들지 않는다.
- `Invalid`와 `Infeasible`를 test가 구분한다.

자문 질문:

- 이 helper는 accepted upstream authority를 소비하는가, raw input을 재해석하는가?
- 이 collection은 constructor와 accessor 양쪽에서 immutable한가?
- 이 key/unit/fingerprint mismatch가 first-wins/last-wins로 숨겨지지 않는가?

### 8.4 4단계 — 통합과 handoff

**목적:** Phase 04/05/07이 같은 의미를 재구현하지 않고 소비할 수 있게 한다.

완료 신호:

- Phase 04 test-only binder가 exact Phase 03 declarations를 materialize한다.
- Phase 05는 invalid route를 ranking하지 않으며 failed trial의 pre-state를 보존한다.
- Phase 07은 solver/search/cache dependency 없이 같은 authority로 재계산할 수 있다.
- Evidence manifest → independent review → acceptance receipt가 단방향이다.

자문 질문:

- Consumer가 cache나 artifact를 authority로 오인할 여지가 있는가?
- Route-level evaluation을 solution-level authority로 몰래 승격했는가?
- Handoff가 OPEN/GATED 항목을 값으로 닫았는가?

## 9. Java 설계 안내

### 9.1 Proposed package와 file 배치

이 절은 `CROSS-PHASE ARCHITECTURE BLOCKER`의 review 입력이다. Phase 00 reactor가 승인돼도 아래 owner 결정이 끝나기 전에는 실제 file을 만들지 않는다.

```text
rpdptw/core/src/main/java/com/ronext/rpdptw/
├── domain/
│   ├── RoutePlan.java
│   └── RoutePlanFingerprint.java
├── propagation/
│   ├── RoutePropagator.java
│   ├── PropagationResult.java
│   ├── PropagationDeclaration.java
│   └── internal/
│       ├── ForwardRoutePropagator.java
│       ├── RouteStructureValidator.java
│       ├── WorkWindowArcResolver.java
│       └── CheckedRouteMath.java
└── evaluation/
    ├── api/
    │   ├── HardConstraint.java
    │   ├── MetricContributor.java
    │   ├── ScoreComponent.java
    │   ├── ObjectiveDimension.java
    │   ├── ObjectiveComparator.java
    │   ├── MetricDeclaration.java
    │   ├── MetricValue.java
    │   ├── MetricSnapshot.java
    │   ├── ConstraintKey.java
    │   ├── ConstraintCheck.java
    │   ├── ObjectiveVector.java
    │   └── EvaluationUnit.java
    └── runtime/
        ├── RouteEvaluationKernel.java
        ├── RouteEvaluationRequest.java
        ├── RouteEvaluationResult.java
        ├── RouteEvaluationArtifact.java
        ├── EvaluationPlan.java
        └── internal/DefaultRouteEvaluationKernel.java
```

위 tree에서 다음 owner-sensitive contract의 경로는 의도적으로 비워 두었다.

| Contract family | 최소 포함 type/책임 | 후보 A owner | 후보 B owner |
|---|---|---|---|
| Physical fact | `RouteFacts`, `VisitFacts`, `TravelLegFacts`, `RouteResourceTotals`, fact fingerprint | `evaluation.api` | `propagation` |
| Failure/rejection | `ConstraintRejection`, `ConstraintCode`, `EvaluationFailure`, exact input identity와 stable category/code | `evaluation.api` | `propagation` |
| Facet | `DomainFacetProvider`, facet declaration/snapshot/version; 계속 OPEN | `evaluation.api` | `propagation` |
| Propagation implementation | `RoutePropagator`, result, declaration, resolver와 checked math | `propagation` | `propagation` |
| Policy contract/runtime | Metric/hard/score/objective declarations와 values / binding·kernel | `evaluation.api` / `evaluation.runtime` | `evaluation.api` / `evaluation.runtime` |

- **후보 A — 상위 배치 후보:** Current Domain §3.1, Current Architecture §6.1, dated Final Domain §3, dated Final Architecture §2.3의 방향이다. `evaluation.api`가 shared physical fact/failure/facet contract를 소유하고 propagation이 이를 생성·소비한다.
- **후보 B — canonical Phase review 후보:** Original Phase review F-P03-010과 canonical Phase §7.3의 방향이다. Propagation이 fact/failure/facet을 소유하고 `evaluation.api`가 이를 소비한다.

`DomainFacetProvider`의 shape/version/positive implementation은 어느 후보에서도 `OPEN/PROPOSED`다. `ADR-004`와 Phase 04/07 parity 전 base acceptance는 empty facet set이며, 후보 선택 자체가 facet 활성화 승인이 아니다.

### 9.2 Skeletal contract 후보

아래는 Java 문법 형태로 의미를 논의하기 위한 **의도적으로 compile-closed가 아닌 review fragment**다. Package/import와 owner-sensitive type 경로를 생략했으므로 그대로 source file에 붙여 넣지 않는다. §9.1의 후보 하나가 승인되고 아래 closure table의 모든 edge·visibility·identity가 review된 뒤에만 compile-closed skeleton을 별도 변경으로 만든다.

```java
// PROPOSED/OPEN review fragment; package/import intentionally omitted
public interface RoutePropagator {
    PropagationResult propagate(
        ProblemInstance problem,
        PreparedTravel travel,
        RoutePlan route,
        PropagationDeclaration declaration
    );
}
```

```java
// PROPOSED/OPEN review fragment; owner-sensitive result values
public sealed interface PropagationResult {
    record Completed(RouteFacts facts) implements PropagationResult {}
    record Rejected(ConstraintRejection rejection) implements PropagationResult {}
    record Invalid(EvaluationFailure failure) implements PropagationResult {}
}
```

```java
// PROPOSED route-level kernel only
public interface RouteEvaluationKernel {
    RouteEvaluationResult evaluate(RouteEvaluationRequest request);
}
```

```java
// PROPOSED/OPEN review fragment; compile closure is the table below
public interface MetricContributor {
    MetricDeclaration declaration();
    MetricValue contribute(RouteFacts facts);
}

public interface HardConstraint {
    ConstraintKey key();
    ConstraintCheck check(
        ProblemInstance problem,
        RoutePlan route,
        RouteFacts facts,
        MetricSnapshot neutralMetrics
    );
}
```

Fragment가 참조하는 contract closure는 다음과 같다. “Semantic field”는 최소 의미이며 exact Java field/name은 계속 proposed다.

| Type/family | Proposed owner/visibility | 최소 semantic field·unit·fingerprint 책임 | 허용 dependency edge |
|---|---|---|---|
| `ProblemInstance` / `PreparedTravel` | Phase 02 `domain` / `travel`, intentional core contract | Dense identity, normalized facts / complete directed coverage, 서로 맞는 fingerprint | Propagation/runtime이 read-only 소비 |
| `RoutePlan` / `RoutePlanFingerprint` | `domain`, intentional core contract | Problem identity, concrete vehicle, ordered nodes, terminal meaning, canonical fingerprint | Propagation/runtime이 소비 |
| `PropagationDeclaration` | `propagation`, internal solve-bound contract 후보 | Explicit service-window rule, ordered facet declaration, contract/fingerprint | 후보 A면 `evaluation.api` facet contract만 inward import; 후보 B면 propagation-local |
| `RouteFacts` family | §9.1 후보 A 또는 B, immutable contract | Problem/travel/route/declaration identity, ordered visit/leg facts, typed units, totals와 canonical fingerprint | 선택한 DAG의 반대 import 금지 |
| `ConstraintRejection` / `EvaluationFailure` | §9.1 후보 A 또는 B, call-local sealed value | Exact input identities, category/code, safe subject, typed actual/bound/unit; 다른 route cache authority 금지 | `PropagationResult`와 `ConstraintCheck`가 같은 선택을 일관되게 소비 |
| `MetricDeclaration` | `evaluation.api`, profile implementation용 contract | Stable key/version, fact dependency, output value type/unit, declaration fingerprint | `MetricContributor`만 자신의 declaration을 반환 |
| `MetricValue` | `evaluation.api`, immutable value | Exact key, `EvaluationUnit`, checked `long` amount | Declaration과 exact key/type/unit 일치 |
| `MetricSnapshot` | `evaluation.api`, immutable ordered snapshot | Evaluation/fact identity, stable ordered unique values, schema/value fingerprint | Hard/score는 이 snapshot만 소비 |
| `ConstraintKey` / `ConstraintCheck` | `evaluation.api`, stable key + sealed call result | Key/version; `Satisfied`/`Rejected`/`Invalid`, rejection/failure identity 보존 | Owner-sensitive failure type으로 한 방향만 import |
| Score declaration/value/typed parameters | `evaluation.api`, profile SPI | Stable key/version, required metric keys, typed parameter schema, output unit/type/fingerprint | Raw route/problem access 금지 |
| Objective declaration/component/vector/direction | `evaluation.api`, policy comparison contract | Stable ordered dimensions, minimize/maximize, checked values, schema/value fingerprint | Comparator는 vector만 소비 |
| Evaluation plan/request/result/artifact fingerprints | `evaluation.runtime`, immutable internal contract | Problem/travel/route/propagation/plan identities와 fact/metric/score/objective schema/value digest | Runtime은 propagation과 API를 조립; 역방향 import 금지 |

`public`은 fragment를 읽기 쉽게 하기 위한 후보 표기다. 다른 Maven module이 실제로 소비해야 하는 contract만 export하고, 구현체와 조립 세부는 package-private 또는 `.internal`을 우선 검토한다. Compile 성공만을 위해 빈 marker, raw `Map`, unit 없는 `long`, fingerprint 없는 snapshot을 추가하면 semantic closure 실패다.

다음 contract는 의도적으로 확정하지 않는다.

- Full solution을 받는 evaluator signature
- Route artifact reuse/invalidation 정책
- Business equality와 solution/insertion tie API
- `DomainFacetProvider` shape/version
- Public serialization schema

이 gap을 `List<RouteEvaluationArtifact>` 합산 같은 편의 API로 메우면 안 된다.

### 9.3 Record와 immutable collection

Record라고 자동으로 deep immutable한 것은 아니다. 모든 proposed record는 다음을 만족해야 한다.

- Constructor에서 list/map/array를 defensive copy한다.
- Element 자체도 immutable value이거나 안전하게 freeze한다.
- Accessor가 mutable backing storage를 노출하지 않는다.
- Canonical fingerprint가 field meaning 전체와 일치하는지 생성 시 검증한다.
- `Optional`은 typed absence에만 쓰고 `null`과 섞지 않는다.
- Missing limit를 `Long.MAX_VALUE`로 바꾸지 않는다.

`RouteFacts`의 최소 identity:

```text
problem fingerprint
prepared travel fingerprint
route plan fingerprint
propagation declaration fingerprint
ordered visit/leg facts
resource totals
facet snapshot identity, if approved
route facts fingerprint
```

`RouteEvaluationArtifact`는 여기에 evaluation plan, metric/score/objective schema와 value fingerprint를 더한다.

### 9.4 Dependency direction

현재는 한 방향을 확정하지 않는다. Source conflict와 두 acyclic 후보를 review한다.

**후보 A — current upper map + user-fixed dated placement**

```text
domain + travel + evaluation.api
                    ↓ consumed by
                propagation

propagation + evaluation.api
                    ↓ consumed by
            evaluation.runtime
```

`evaluation.api`가 fact/failure/facet contract를 소유하고 propagation은 그 value를 생성한다. Phase 04 SPI implementation과 Phase 07 verifier도 같은 API contract를 소비한다.

**후보 B — canonical Phase review placement**

```text
domain + travel
        ↓ consumed by
propagation
        ↓ facts consumed by
evaluation.api

propagation + evaluation.api
        ↓ consumed by
evaluation.runtime
```

Propagation이 fact/failure/facet contract를 소유한다. 이 경우 propagation은 `evaluation.api`를 전혀 import하지 않아야 한다.

두 후보의 공통 consumer edge:

```text
Phase 04 capabilities/profile → approved Phase 03 API/runtime contracts
Phase 05 solver               → Phase 03 runtime
Phase 07 verification         → Phase 03 cache-free core contracts
build/test-fixtures           → rpdptw-core, test-only
```

Core/Evaluation + Architecture owner는 다음을 한 review record에서 승인해야 한다.

1. 후보 A/B 중 하나와 owner-sensitive type의 exact package/visibility
2. `RouteFacts`/failure/facet fingerprint와 Phase 04 provider import
3. Phase 05 runtime과 Phase 07 cache-free recomputation consumer compile impact
4. 선택한 방향을 강제하는 architecture test와 관련 authority/ADR 변경

그 전 last safe point는 semantic contract, hand oracle과 import graph review다. Type 이름만 옮기거나 양쪽에 adapter/duplicate fact를 만들어 cycle을 숨기지 않는다.

공통 금지:

- `rpdptw-core → solver/verification/application/adapter`
- `propagation → evaluation.runtime`
- 후보 A에서 `evaluation.api → propagation`
- 후보 B에서 `propagation → evaluation.api`
- `rpdptw-core → capabilities/profile-catalog`
- `verification → solver/search/cache`
- `rpdptw-core → build/test-fixtures`
- Cloud SDK, HTTP/Jackson DTO, customer name, OR-Tools vendor API의 core 침투

Test-fixtures module은 core를 test scope로 소비하는 단방향이어야 한다. Core test가 test-fixtures source를 역으로 의존해 cycle을 만들지 않는다. 실제 source-set wiring은 §11.1과 §11.5의 live/future 조건을 따른다.

### 9.5 State transition

```text
Unvalidated call
  → exact authority identity validation
  → route structure validation
      ├─ defect → Invalid
      └─ valid
          → physical propagation
              ├─ normal hard violation → Infeasible
              ├─ lookup/arithmetic corruption → Invalid
              └─ RouteFacts
                  → neutral metrics
                  → composed hard constraints
                      ├─ rejected → Infeasible
                      ├─ component invalid → Invalid
                      └─ scores → objective vector → Feasible artifact
```

여러 defect가 있으면 다음 stable precedence로 첫 failure를 고른다.

```text
authority identity
→ route structure
→ propagation lookup/arithmetic
→ component declaration
→ component execution
```

같은 bytes가 thread나 collection order에 따라 다른 first failure를 내면 재현성 실패다.

### 9.6 Corrected propagation pseudocode

```text
validate exact problem/travel/route/declaration identities
validate vehicle, terminal, service pattern, pair completeness and order

initialLoad = sum(delivery-only demand assigned to this route)
check each load dimension: 0 <= initialLoad <= vehicle capacity

earliestDeparture = max(vehicle work start, depot open)
if waitInDepot:
    departure = max(earliestDeparture, firstCustomerOpen - firstArcTravelTime)
    depotWaiting += departure - earliestDeparture
else:
    departure = earliestDeparture

for each actual arc current → next:
    D = exact prepared directed meter
    U = exact prepared vehicle-resolved second

    if a normalized work window contains departure
       and departure + U <= that window end:
        arcStart = departure
    else:
        choose the first future normalized window where
            window.start >= departure
            and window.start + U <= window.end
        if none exists inside the plan:
            return Infeasible(NO_SINGLE_WORK_WINDOW_FOR_FULL_ARC)
        rest += window.start - departure
        arcStart = window.start

    arrival = arcStart + U
    accumulate exact D and U

    choose the first expanded/clipped service window
      satisfying the explicit START_ONLY or COMPLETE_WITHIN_WINDOW rule
    serviceStart = max(arrival, window.open)
    serviceEnd = serviceStart + normalizedServiceTime
    require completion due date and every event < planEnd

    apply canonical load delta
    check every weight/volume prefix
    increase stop only on customer-location transition
    accumulate waiting, service and optional approved facet facts

if roundtrip:
    traverse final customer → approved terminal with the same full-arc rule
if oneway:
    do not invent a return arc

if final load != zero:
    return Invalid(FINAL_LOAD_AUTHORITY_CONTRADICTION)

check inclusive route-wide stop/drive limits
operational =
    drive + customerWait + depotWait + service + interWorkWindowRest
```

모든 합·차는 `Math.addExact`, `Math.subtractExact` 또는 동등한 checked operation을 사용한다. Overflow는 clamp, wrap, plan-end sentinel 또는 normal infeasible이 아니라 `Invalid`.

## 10. Ordered work packages

모든 work package는 앞 package의 green 결과와 evidence candidate를 선행조건으로 한다. Source 구현은 §5의 entry gate가 열리기 전 시작하지 않는다.

### WP-03.0 — Entry, authority와 contract freeze

| 항목 | 지시 |
|---|---|
| 목적/이유 | 잘못된 upstream artifact나 아직 열린 API를 code로 굳히지 않는다 |
| 사전조건 | Scheduler task/owner 지정, Phase 00~02 accepted receipt 후보 전달 |
| 예상 target | 이 Phase의 API review record와 red-test inventory; production source 없음 |
| 구체 행동 | Source blob/heading 재검증, Phase 02 handoff field inventory, identity equality, unit/failure taxonomy, §9.4 후보 A/B import graph와 Phase 04/05/07 consumer 영향을 기록한다 |
| 선택 근거 | Accepted artifact를 먼저 고정해야 propagation이 raw input을 재해석하지 않는다 |
| 금지 shortcut | Type 존재만으로 accepted 판단, historical hash를 current hash로 사용, open API를 public으로 선언 |
| 검증 | `git rev-parse "HEAD:path/to/source.md"`; actual link/heading check; Phase 02 receipt/digest 재계산 |
| 기대 결과 | Source drift 0 또는 영향 분석 완료, fact/failure/facet owner와 architecture test 승인, entry AND gate 전부 `READY` |
| 실패 해석 | 하나라도 없으면 implementation blocker이며 test failure를 고칠 단계가 아니다 |
| Rollback/last safe | 문서·fixture 설계로 돌아가고 source file을 만들지 않는다 |
| 다음 handoff | 승인된 internal semantic contract와 exact red-test 목록을 WP-03.1에 전달 |

사람 checkpoint:

- Full-solution evaluator와 tie boundary는 이 WP에서 승인되거나 residual blocker로 명시되어야 한다.
- Fact/failure/facet owner와 package DAG는 Core/Evaluation + Architecture owner가 승인하고 관련 authority/ADR와 같은 변경 단위로 반영해야 한다.
- 승인되지 않았다면 route-level kernel 범위만 유지한다.

### WP-03.1 — RoutePlan, structure validator와 failure triage

| 항목 | 지시 |
|---|---|
| 목적/이유 | 손상된 route와 정상 hard infeasible을 평가 전에 분리한다 |
| 사전조건 | WP-03.0 승인, Phase 02 IDs/terminal/service pattern contract |
| 예상 file/type | `RoutePlan`, fingerprint, `RouteStructureValidator`, 승인된 후보 package의 sealed result/failure types |
| 구체 행동 | Defensive ordered sequence, exact vehicle/start/end terminal, internal/foreign terminal 금지, real pair complete/exactly-once/precedence, delivery-only representation을 구현한다 |
| 선택 근거 | 구조 결함이 metric/score로 내려가면 solver가 defect를 탐색 후보로 취급한다 |
| 금지 shortcut | `Set`으로 visit 순서를 잃기, partial pair를 rejection으로 반환, external ID 문자열로 dense lookup |
| 검증 명령 | §11.5의 future core selected-test command |
| 기대 결과 | 모든 malformed route가 stable exact `Invalid` code, 정상 minimal route만 다음 단계로 이동 |
| 실패 해석 | `Infeasible`이면 taxonomy 위반, non-deterministic first failure면 reproducibility 위반 |
| Rollback/last safe | WP-03.1 변경만 제거하고 accepted Phase 02 artifact를 보존 |
| 다음 handoff | Validated immutable `RoutePlan`과 failure precedence를 WP-03.2에 전달 |

필수 red→green:

- `rejectsDeliveryBeforePickupAsInvalid()`
- `rejectsPartialDuplicateAndForeignTerminalRoutes()`
- `acceptsDeliveryOnlyLogicalPickupWithoutServiceVisit()`
- Constructor/accessor mutation probe

### WP-03.2 — Canonical full propagation

| 항목 | 지시 |
|---|---|
| 목적/이유 | Route의 물리 사실을 Phase 02 authority에서 완전히 재계산한다 |
| 사전조건 | WP-03.1 green, travel coverage/fingerprint 재확인 |
| 예상 file/type | `ForwardRoutePropagator`, `WorkWindowArcResolver`, `CheckedRouteMath`, visit/leg/resource facts |
| 구체 행동 | Initial load, prefix delta, directed arc, customer/depot wait, service rule, full-arc restart, stop/resource, oneway/roundtrip을 순서대로 구현한다 |
| 선택 근거 | 하나의 forward state가 이전 visit 결과를 다음 visit 입력으로 전달해야 stale cache가 authority가 되지 않는다 |
| 금지 shortcut | Reverse/symmetric travel, partial drive, raw time parsing, final-load-only capacity, limit sentinel |
| 검증 명령 | Core boundary selected test + test-fixtures full verify |
| 기대 결과 | §11.2 hand table exact 일치, boundary ±1과 corruption 분류 정확 |
| 실패 해석 | 값 mismatch는 fixture를 바꾸기 전에 production/independent oracle 중 어느 계약이 잘못됐는지 source까지 추적한다 |
| Rollback/last safe | WP-03.2 code를 제거하고 WP-03.1 immutable structure contract로 복귀 |
| 다음 handoff | Immutable facts와 hand-oracle report를 WP-03.3에 전달 |

완료 신호:

- Same-location service에서 travel/stop은 늘지 않고 service/load는 변한다.
- 다음 work window를 선택한 뒤 후속 arc가 stale 이전 window를 읽지 않는다.
- Roundtrip final arc만 drive/distance에 포함되고 depot stop은 세지 않는다.

### WP-03.3 — Neutral metric, composed hard와 evaluation runtime

| 항목 | 지시 |
|---|---|
| 목적/이유 | 물리 fact와 고객 정책을 섞지 않고 확장 가능한 단방향 평가를 만든다 |
| 사전조건 | WP-03.2 exact facts green, component key/unit/type contract review |
| 예상 file/type | `HardConstraint`, `MetricContributor`, `ScoreComponent`, `ObjectiveDimension`, `EvaluationPlan`, kernel/runtime |
| 구체 행동 | Declaration을 stable key order로 validate하고, metric → composed hard → score → objective 호출 순서와 short-circuit를 구현한다 |
| 선택 근거 | Composed hard는 neutral metric을 필요로 할 수 있지만 score/objective는 hard reject 뒤 호출되면 안 된다 |
| 금지 shortcut | Reflection/script/`Map<String,Object>`, duplicate first-wins, component exception을 0 또는 maximum으로 변환 |
| 검증 명령 | Evaluation layer/corruption/immutability selected tests |
| 기대 결과 | Physical reject 뒤 evaluation call 0; composed hard reject 뒤 score/objective call 0; contract violation은 `Invalid` |
| 실패 해석 | Customer-specific 정보가 필요하면 Phase 04 seam 질문이지 raw route access 허가가 아니다 |
| Rollback/last safe | Evaluation runtime만 제거하고 WP-03.2 physical facts를 유지 |
| 다음 handoff | Phase 04가 bind할 stable SPI/declaration 후보를 WP-03.4와 공동 review에 전달 |

### WP-03.4 — Comparator와 tie boundary

| 항목 | 지시 |
|---|---|
| 목적/이유 | Ordered business objective를 수학적으로 일관되게 비교한다 |
| 사전조건 | WP-03.3 green, ordered objective schema와 direction 명시 |
| 예상 file/type | `ObjectiveVector`, `ObjectiveComparator`, lexicographic implementation |
| 구체 행동 | Dimension별 minimize/maximize를 stable order로 비교하고 equality를 관찰 가능하게 한다 |
| 선택 근거 | Strict priority는 scalar Big-M보다 lexicographic vector로 보존한다 |
| 금지 shortcut | Tie key를 business dimension에 삽입, insertion position으로 non-tied objective 뒤집기, `double` epsilon |
| 검증 명령 | Test-fixtures comparator property/exhaustive suite |
| 기대 결과 | Antisymmetry, transitivity, equality와 non-tied priority 전수 통과 |
| 실패 해석 | Total order를 만들 API가 부족하면 cross-Phase blocker이며 임의 signature를 추가하지 않는다 |
| Rollback/last safe | Business vector comparator만 유지하고 context tie 적용을 제거 |
| 다음 handoff | Business equality 의미와 미해결 tie 질문을 WP-03.5/Phase 05 review에 전달 |

### WP-03.5 — Independent oracle, cache equality와 재현성

| 항목 | 지시 |
|---|---|
| 목적/이유 | Production 구현과 같은 결함을 반복하지 않는 독립 판정선을 만든다 |
| 사전조건 | WP-03.2~4 green |
| 예상 target | `build/test-fixtures`의 hand/exhaustive/BigInteger oracle와 defect doubles |
| 구체 행동 | Literal event table, bounded exhaustive window/reference calculation, `BigInteger` checked oracle, one-field corruption, faulty partial-arc/reverse/wrap/comparator double을 구현한다 |
| 선택 근거 | Compile red만으로 oracle가 실제 알고리즘 결함을 검출한다고 증명할 수 없다 |
| 금지 shortcut | Production propagator/helper로 expected 생성, stale Surefire XML 재사용, skipped property test |
| 검증 명령 | `mvn ... -pl build/test-fixtures -am clean verify` |
| 기대 결과 | Seeded defect마다 assertion red, fixed production에서 green, minimal counterexample와 ordinal/digest 기록 |
| 실패 해석 | Oracle와 production이 같은 helper를 공유하면 evidence 독립성 실패 |
| Rollback/last safe | Cache/최적화 seam을 제거하고 cache-free full path를 유지 |
| 다음 handoff | `E-P03-*` 후보 report와 exact method manifest를 WP-03.6에 전달 |

### WP-03.6 — Architecture, evidence, review와 handoff

| 항목 | 지시 |
|---|---|
| 목적/이유 | Code가 아니라 검증된 immutable 계약을 downstream에 전달한다 |
| 사전조건 | Required test failed/error/skipped 0, residual blocker 명시 |
| 예상 target | Architecture report, pre-review evidence manifest, independent review report, acceptance receipt |
| 구체 행동 | Forbidden dependency/bytecode/package cycle 검사, fresh root build, exact command/environment/report digest, redaction, rollback point와 OPEN snapshot을 봉인한다 |
| 선택 근거 | Source/test 존재와 console success는 acceptance authority가 아니다 |
| 금지 shortcut | Pre-review manifest에 reviewer/verdict/receipt backfill, 이전 run report 혼합, receipt 없이 `ACCEPTED` |
| 검증 명령 | Architecture module, core+fixtures module, root reactor full `clean verify` |
| 기대 결과 | Default build에 cloud/customer/vendor leakage 0, immutable manifest M → review R → receipt(M,R) |
| 실패 해석 | Review나 receipt가 없으면 최대 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING` |
| Rollback/last safe | Last accepted predecessor/build artifact와 WP-03.5 full path 유지 |
| 다음 handoff | Exact identities, evidence digests와 known blockers를 Phase 04/05/07에 전달 |

## 11. 테스트 구현 안내

### 11.1 Fixture, builder와 oracle 분리

| 역할 | 후보 | 규칙 |
|---|---|---|
| Core-local minimal builder | `CoreRouteTestData` | 정상/손상 route value를 작게 조립 |
| Literal hand fixture | `HandPropagationFixtureBuilder` | 사람이 계산한 expected event table 보존 |
| Exhaustive timeline oracle | `ExhaustiveRouteOracle` | 작은 window 조합을 전수 열거, production resolver 호출 금지 |
| Arithmetic oracle | `BigIntegerEvaluationOracle` | `long` wrap를 반복하지 않도록 `BigInteger` 사용 |
| Corruption builder | `CorruptAuthorityFixtureBuilder` | Fingerprint/mapping/key 한 field씩 변조 |
| Faulty sensitivity doubles | Test-fixtures only | Partial arc, reverse lookup, stale window, wrap, wrong comparator order를 의도적으로 주입 |

Live `build/test-fixtures/pom.xml`은 아직 미승인 Phase 00 snapshot이며 다음 wiring을 가진다.

| Live wiring | 관찰 | Phase 03 조건 |
|---|---|---|
| `rpdptw-test-fixtures → rpdptw-core` | `<scope>test</scope>` | 현재 wiring에서 core type을 참조하는 Phase 03 builder/oracle/test는 `src/test/java`에 둬야 한다 |
| Test JAR | `maven-jar-plugin:test-jar`를 `test` phase에 attach | 다른 test module이 shared test class를 소비할 때만 사용하며 core의 역의존은 금지 |
| `rpdptw-core` test dependency | JUnit dependency 없음 | Core-local Phase 03 test를 만들기 전 Phase 00/POM owner가 test dependency를 승인해야 한다 |
| Test discovery | Surefire `3.5.4`, global `failIfNoTests=false`; Failsafe binding 없음 | §11.3의 `*Test`는 Surefire 대상이나 zero-test는 기본 green 가능. `*IT`는 Failsafe를 pin/bind하기 전 전혀 실행되지 않는다 |

따라서 현재 last safe wiring은 independent builder/oracle와 그 test를 `build/test-fixtures/src/test/java`에 함께 두고 `-pl build/test-fixtures -am clean verify`로 실행하는 후보뿐이다. Shared production-like fixture library를 `src/main/java`로 옮기려면 core dependency scope, test-JAR 소비자와 module DAG를 Phase 00 owner가 별도 승인해야 하며 이 가이드가 POM 변경을 자동 지시하지 않는다.

Expected value에는 다음 provenance가 있어야 한다.

```text
fixture name/version
literal input digest
hand table or oracle implementation digest
expected result/category/code
seed or stable enumeration ordinal
source requirement reference
```

### 11.2 필수 hand-calculated fixture

다음 숫자는 **test-only**이며 official default나 calibration 값이 아니다.

```text
vehicle:
  roundtrip, capacity weight 10
  work windows [0,100], [200,400]
  waitInDepot = N

route:
  depot
  → A: delivery-only 4, open 20, service 10
  → B: real pickup 3, service 5
  → B: real delivery 3, service 5
  → depot

travel time:
  D→A 5, A→B 80, B→B 0, B→D 10
distance:
  D→A 100, A→B 800, B→B 0, B→D 120
```

Expected:

| Event | Arrival | Customer wait / work rest | Service start/end | Load after | Stop |
|---|---:|---:|---:|---:|---:|
| Start D | `0` | `0 / 0` | — | `4` | `0` |
| A delivery | `5` | `15 / 0` | `20 / 30` | `0` | `1` |
| B pickup | `280` | `0 / 170` | `280 / 285` | `3` | `2` |
| B delivery | `285` | `0 / 0` | `285 / 290` | `0` | `2` |
| Return D | `300` | `0 / 0` | — | `0` | `2` |

```text
distance = 1,020
drive = 95
customer wait = 15
depot wait = 0
service = 20
inter-work-window rest = 170
operational = 300
```

이 fixture 하나가 mixed service pattern, prefix load, same-location stop, full-arc restart, roundtrip final arc와 resource breakdown을 동시에 잡는다.

### 11.3 Test class와 method 후보

| Class | 필수 method 후보 | 핵심 판정 |
|---|---|---|
| `RouteStructureValidatorTest` | `rejectsDeliveryBeforePickupAsInvalid()` | `Invalid`, normal rejection 금지 |
|  | `rejectsPartialDuplicateAndForeignTerminalRoutes()` | Stable defect code |
|  | `acceptsDeliveryOnlyLogicalPickupWithoutServiceVisit()` | Fake visit/stop/travel 0 |
| `RoutePropagatorHandOracleTest` | `propagatesMixedDeliveryAndPickupAcrossAllPrefixes()` | §11.2 exact equality |
|  | `restartsWholeArcAtNextWorkWindow()` | Arc start 200, arrival 280, rest 170 |
|  | `usesAdvancedWorkWindowForFollowingArcs()` | Stale previous work-end 사용 금지 |
|  | `countsOnlyCustomerLocationTransitions()` | `A→A→B=2`, `A→B→A=3` |
|  | `includesFinalArcOnlyForRoundtrip()` | Oneway return 합성 금지 |
| `RoutePropagationBoundaryTest` | `acceptsInclusiveCapacityWindowAndResourceLimits()` | Exact upper boundary feasible |
|  | `rejectsOneUnitBeyondEveryHardBound()` | Weight/volume/time/stop/drive 각 row |
|  | `distinguishesStartOnlyFromCompleteWithinWindow()` | Explicit rule 차이 |
|  | `consumesExpandedRepeatingAndOvernightWindowsWithoutRawTimeReinterpretation()` | Phase 01/02 expanded order와 plan clipping exact |
|  | `rejectsEventAtPlanEnd()` | `[start,end)` |
|  | `reportsNonZeroFinalLoadAsInvalidCorruption()` | `Invalid`, not capacity rejection |
|  | `reportsAccumulationOverflowAsInvalid()` | Wrap/saturation 0 |
| `RoutePropagationPropertyTest` | `everyFeasibleGeneratedRouteRespectsAllLoadPrefixes()` | BigInteger oracle equality |
|  | `increasingCapacityCannotMakeAFeasibleRouteInfeasible()` | Monotonicity |
|  | `increasingAbsentOrPresentResourceLimitIsMonotone()` | Absent/present limit 완화 뒤 rejection 금지 |
|  | `constantTimeTranslationPreservesDurationsAndFeasibility()` | Safe `+k` translation |
| `RoutePropagationMetamorphicTest` | `movingEarlyWaitToDepotPreservesServiceAndOperationalTime()` | Wait breakdown만 이동 |
|  | `usesDirectedArcWithoutReverseOrSymmetryFallback()` | 방향별 exact 값 |
|  | `consecutiveSameLocationServiceAddsNoTravelOrStop()` | Service/load는 적용 |
| `RoutePropagationCorruptionTest` | `rejectsProblemTravelFingerprintMismatch()` | Lookup 전 invalid |
|  | `rejectsMissingVehicleResolvedTravelInsteadOfGenerating()` | Runtime generation 0 |
|  | `rejectsPoisonedRoutePlanFingerprint()` | Propagation 전 invalid |
| `RouteEvaluationLayerTest` | `doesNotInvokeAnyEvaluationComponentAfterPropagationRejection()` | All evaluation calls 0 |
|  | `computesNeutralMetricsBeforeComposedHardAndStopsBeforeScore()` | Correct call trace |
|  | `keepsNeutralMetricsFreeOfPriceAndPreference()` | Cost/preference unit declaration invalid |
|  | `scoreReadsOnlyMetricSnapshotAndTypedParameters()` | Raw route/problem access signature 0 |
|  | `reportsEvaluationArithmeticAndComponentFailureAsInvalid()` | Default/rejection 변환 금지 |
| `RouteEvaluationComparatorPropertyTest` | `lexicographicComparatorIsAntisymmetric()` | 모든 pair law |
|  | `lexicographicComparatorIsTransitive()` | 모든 triple law |
|  | `stableTieKeyCreatesDeterministicTotalOrderOnlyAfterObjectiveTie()` | Non-tied 우선 |
| `RouteEvaluationContractCorruptionTest` | `rejectsDuplicateMetricScoreAndObjectiveKeys()` | First/last wins 금지 |
|  | `rejectsUnitTypeAndDeclarationFingerprintMismatch()` | Invalid |
| `RouteEvaluationImmutabilityTest` | `defensivelyCopiesEveryRouteFactPlanSnapshotAndResultCollection()` | Mutable alias 0 |
| `RouteEvaluationCacheEquivalenceTest` | `cacheHitMissAndFullRecomputationAreExactlyEqual()` | Canonical bytes exact |
| `RoutePropagationReproducibilityTest` | `sameInputsProduceSameCanonicalResultAcrossRepeatedAndParallelCalls()` | Variant/bytes/fingerprint exact |
| `Phase03OracleSensitivityTest` | `independentOraclesRejectSeededPartialArcReverseLookupWrapAndComparatorOrderDefects()` | 각 defect가 실제 assertion red |
| `Phase03KernelArchitectureTest` | `kernelHasNoCloudSolverVerifierCustomerOrVendorDependency()` | Forbidden reference 0 |
|  | `propagationAndEvaluationPackagesAreAcyclic()` | Package cycle 0 |
|  | `coreDoesNotDependOnTestFixturesModule()` | Test edge 단방향 |

`rejectsOneUnitBeyondEveryHardBound()`는 이름 하나로 다음 11개 parameter row를 숨기지 않는다. 아래 semantic code는 required meaning이며 exact Java enum token은 WP-03.0 API review에서 `PROPOSED/OPEN` 상태로 freeze한다.

| Row | Feasible boundary | 최소 failing value | Expected category / semantic code | Evaluation call count |
|---|---|---|---|---|
| Weight lower | Prefix weight `0` | `-1` | `Infeasible / LOAD_WEIGHT_LOWER_BOUND` | Metric/hard/score/objective 모두 `0` |
| Weight upper | Prefix weight `capacityWeight` | `capacityWeight + 1` | `Infeasible / LOAD_WEIGHT_UPPER_BOUND` | 모두 `0` |
| Volume lower | Prefix volume `0` | `-1` | `Infeasible / LOAD_VOLUME_LOWER_BOUND` | 모두 `0` |
| Volume upper | Prefix volume `capacityVolume` | `capacityVolume + 1` | `Infeasible / LOAD_VOLUME_UPPER_BOUND` | 모두 `0` |
| Service close | 적용 rule이 허용하는 close exact boundary | 허용 boundary보다 `1`초 늦음 | `Infeasible / SERVICE_WINDOW_BOUND` | 모두 `0` |
| Completion due | `serviceEnd == due` | `due + 1` | `Infeasible / COMPLETION_DUE_BOUND` | 모두 `0` |
| Plan end | 모든 event `< planEnd` | `event == planEnd` | `Infeasible / PLAN_END_EXCLUSIVE_BOUND` | 모두 `0` |
| Full-arc work end | `arcStart + U == workWindow.end` | `workWindow.end + 1`, future fitting window 없음 | `Infeasible / NO_SINGLE_WORK_WINDOW_FOR_FULL_ARC` | 모두 `0` |
| Stop | `stopCount == effectiveMaxStop` | `effectiveMaxStop + 1` | `Infeasible / STOP_LIMIT` | 모두 `0` |
| Drive time | `driveTime == maxDriveTime` | `maxDriveTime + 1` | `Infeasible / DRIVE_TIME_LIMIT` | 모두 `0` |
| Drive distance | `driveDistance == maxDriveDistance` | `maxDriveDistance + 1` | `Infeasible / DRIVE_DISTANCE_LIMIT` | 모두 `0` |

각 row는 source requirement, fixture digest, expected category/semantic code와 actual invocation trace를 별도 parameter display name/report row로 남긴다. Production helper로 expected를 만들거나 11개 중 일부만 실행한 report는 method 이름이 존재해도 false-green이다.

### 11.4 Red → green 순서

| 순서 | 먼저 고정할 red | Red가 의미하는 것 | Green 판정 |
|---:|---|---|---|
| 1 | Structure/identity | Type 부재 또는 wrong-category defect | Corruption은 exact `Invalid` |
| 2 | Hand event table | Partial-arc/stale-window mismatch | 모든 event/load/resource exact |
| 3 | Boundary/directed travel | Off-by-one/reverse fallback | Inclusive/+1과 direction 정확 |
| 4 | Layer trace | Hard/metric/score 순서 오류 | Canonical trace와 short-circuit |
| 5 | Comparator laws | Pair/triple counterexample | Antisymmetry/transitivity/equality |
| 6 | Property/overflow | Translation/monotonicity/wrap 반례 | Minimal counterexample 0 |
| 7 | Cache/repro/architecture | Poisoned cache/order/dependency 누출 | Full equality와 forbidden ref 0 |

Compile failure는 TDD 시작 증거일 수 있지만 oracle sensitivity 증거는 아니다. Faulty test double을 사용한 실제 assertion red report를 보존한다.

### 11.5 Maven 명령과 fail-closed 판정

#### Live unaccepted snapshot

```bash
./mvnw -B -ntp -Dstyle.color=never clean verify
```

이 wrapper/reactor는 live filesystem에는 있지만 미커밋 Phase 00 Fix 01 상태다. Root POM은 Surefire `3.5.4`를 고정하면서 `failIfNoTests=false`를 상속하고, Failsafe plugin/binding은 없다. `rpdptw-core`에는 JUnit test dependency도 없고 Phase 03 test는 0개다. 따라서 이 명령은 실행 가능 diagnostic일 수 있어도 Phase 03 green 또는 accepted build command가 아니다.

현재와 future test discovery를 섞지 않는다.

| 이름/source set | 현재 discovery | Future evidence 조건 |
|---|---|---|
| §11.3의 `*Test.java` | Surefire default 대상 | Accepted effective POM과 JUnit provider/dependency 아래 실제 실행 |
| `*IT.java` | Failsafe가 없어 `verify`에서도 실행되지 않음 | IT가 생기면 pinned Failsafe의 `integration-test`/`verify` binding, include와 zero-test policy를 Phase 00/POM owner가 승인 |
| Test-fixtures `src/test/java` | Test-scope core dependency로 compile 가능 | `test-fixtures → core`; core 역의존 0, full module report manifest |
| Test-fixtures `src/main/java` | Test-scope core type을 참조하면 main compile 불가 | Compile-scope 변경과 소비자/test-JAR DAG를 별도 승인하기 전 사용 금지 |

#### Accepted future reactor에서만 실행할 command

파일 존재만이 아니라 acceptance와 effective wiring을 먼저 확인한다.

```bash
test -x ./mvnw
test -f rpdptw/core/pom.xml
test -f build/test-fixtures/pom.xml
test -f build/architecture-rules/pom.xml
```

모두 0이어도 충분하지 않다. Phase 00 receipt와 wrapper checksum/toolchain, core JUnit test dependency, §11.1 source-set wiring, chosen §9.4 package DAG, Surefire/Failsafe effective configuration을 함께 확인한다. 그 뒤 한 evidence run 전체에서 accepted `./mvnw`만 사용한다.

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core \
  -Dtest=RouteStructureValidatorTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core \
  -Dtest=RoutePropagationBoundaryTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core \
  -Dtest=RouteEvaluationLayerTest,RouteEvaluationContractCorruptionTest,RouteEvaluationImmutabilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/test-fixtures -am clean verify

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am clean verify

./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core,build/test-fixtures -am clean verify

./mvnw -B -ntp -Dstyle.color=never clean verify
```

Selected core command에는 `-am`을 붙이지 않는다. Core가 별도 reactor module dependency를 갖게 된다면 이 명령을 억지로 완화하지 말고 POM owner가 dependency/setup command와 selected-test command를 분리해 review한다. Test-fixtures와 architecture module은 reactor dependency를 포함하도록 `-am`을 쓰되, upstream의 nonmatching test 때문에 selected `-Dtest`를 전파하지 않고 full module `verify`로 판정한다.

각 `clean` command의 report는 다음 command가 지우기 전에 command line, start/end, module set, effective plugin/provider, XML digest와 manifest를 별도 evidence candidate로 봉인한다. 서로 다른 run이나 이전 `target/` report를 한 manifest에 합치지 않는다.

Pass는 exit code 0만 뜻하지 않는다.

1. Canonical Phase 03 §9.3의 table-order `Class#method` 42줄 manifest는 UTF-8/LF SHA-256 `79379c996379669725e70dd9577bce42d4fb1c5de4f00024d5415de4e75c6e1c`이다.
2. 이 가이드 §11.3에서 같은 방식으로 만든 manifest도 정확히 42줄·같은 SHA-256이어야 한다. Canonical↔Target 차집합이 하나라도 있으면 문서/test freeze 실패다.
3. `clean` 뒤 command start보다 새로 생성된 Surefire XML만 사용한다. Future IT가 승인되면 Failsafe XML을 별도 포함한다.
4. Report의 class + underlying method를 parameter display name과 분리해 normalize한 실제 manifest와 canonical 42개 set의 양방향 차집합이 0이어야 한다.
5. `rejectsOneUnitBeyondEveryHardBound()`는 method 존재 외에도 §11.3의 11개 parameter row, expected category/semantic code와 evaluation call-count가 모두 있어야 한다.
6. Required test의 failed/error/skipped, missing, duplicate가 모두 0이어야 한다.
7. Property seed/ordinal, fixture/oracle digest, chosen package-DAG architecture result와 toolchain이 기록돼야 한다.

빠른 보조 확인은 전체 manifest 판정을 대체하지 않는다.

```bash
find rpdptw build -path '*/target/surefire-reports/TEST-*.xml' -type f -print
rg -n 'failures="[1-9]|errors="[1-9]|skipped="[1-9]' \
  rpdptw/*/target/surefire-reports build/*/target/surefire-reports
rg -n 'name="restartsWholeArcAtNextWorkWindow"|name="lexicographicComparatorIsTransitive"' \
  rpdptw/*/target/surefire-reports build/*/target/surefire-reports
```

두 번째 `rg`가 match를 찾으면 실패다. Match가 없어도 report directory 자체가 없거나 42-method/11-row manifest가 비면 실패다. 세 번째는 두 method의 존재를 보는 예일 뿐 zero-test, stale-report 또는 exact inventory를 판정하지 못한다.

### 11.6 Test category별 적용성

| 종류 | Phase 03 적용 | 판정 |
|---|---|---|
| Unit/boundary | 필수 | Time/load/resource exact boundary |
| Contract | 필수 | Phase 02 identity, Phase 04 declaration, key/unit/type |
| Module integration | 필수 | Core + external test-fixtures + architecture |
| Full application E2E | 이 Phase exit에는 직접 적용 안 됨 | Phase 08 소유; Phase 03은 portable kernel/handoff만 제공 |
| Architecture | 필수 | SDK/customer/vendor/verifier 역의존 0 |
| Fault injection | 필수 | Component exception/overflow가 `Invalid`, partial output 없음 |
| Corruption | 필수 | Fingerprint/lookup/duplicate/unit/cache 변조 거부 |
| Reproducibility | 필수 | Sequential/parallel canonical equality |
| Security | 제한 적용 | Core 무 I/O/무 logger authority, safe failure/redaction field 검증 |
| Tenant authorization | 적용 안 됨 | Phase 03은 tenant/catalog를 읽지 않으며 Phase 04/08 이후 소유 |
| Performance | 구조 evidence만 | Linear forward traversal, lookup totality; 공식 시간/memory threshold는 OPEN |
| Provider integration | 적용 안 됨 | Core에 provider SDK가 없어야 통과 |
| Deployment/operations | 적용 안 됨 | Phase 11/14 소유 |

## 12. 사람 checkpoint와 evidence bundle

### 12.1 Checkpoint 산출물

| Checkpoint | 제출할 것 | Stop 조건 | Resume 조건 |
|---|---|---|---|
| C0 Entry | Source/inventory digest, predecessor receipts, owner/task, §9.4 package-DAG decision record | Receipt/identity mismatch 또는 owner conflict 미승인 | Correct exact artifact와 Core/Evaluation + Architecture 승인 |
| C1 Structure | Red/green reports, failure taxonomy, immutable probes | Corruption이 normal infeasible | Category/API review |
| C2 Propagation | Hand table, boundary rows, full-arc sensitivity | Oracle/production meaning 충돌 | Source-backed correction |
| C3 Evaluation | Call trace, declaration closure, invalid boundary | Customer/raw-route shortcut 필요 | Phase 04 seam review |
| C4 Comparator | Pair/triple exhaustive report, tie questions | Cross-Phase tie 미승인 | Joint review |
| C5 Integration | Chosen-DAG architecture/dependency report, canonical↔Target↔report 42-method manifest와 11 boundary rows | Missing/skipped/stale/duplicate 또는 set 차이 | Clean full rerun |
| C6 Acceptance | Manifest M, review R, receipt(M,R), rollback digest | Review fail 또는 receipt 없음 | Correction → new immutable chain |

### 12.2 Planned evidence 내용

| Key | 필수 내용 |
|---|---|
| `E-P03-PROPAGATION` | Source/build/problem/travel/route/declaration fingerprints, hand table, boundary/property/metamorphic/corruption/sensitivity result, exact commands와 fresh method manifest |
| `E-P03-EVALUATION` | Metric-before-composed-hard trace, unit/type/dependency closure, immutable artifact, arithmetic/component corruption와 failure triage |
| `E-P03-COMPARATOR` | Exhaustive pair/triple laws, business equality/tie boundary status, cache/full/repeated/parallel fingerprint equality |

공통 bundle:

```text
source commit and source blob fingerprints
toolchain/build/runtime fingerprint
input artifact digests
exact command + environment + exit code
fixture/oracle/test report digests
architecture/security report digests
OPEN/GATED/deferred snapshot
handoff candidate artifact digest
rollback point digest
```

### 12.3 단방향 evidence DAG

```text
immutable implementation/test evidence
  → preReviewEvidenceManifest [digest M]
  → independentReviewReport [references M, digest R]
  → postReviewAcceptanceReceipt [references M + R]
  → ACCEPTED handoff
```

Pre-review manifest에 reviewer identity, verdict, review digest나 acceptance receipt를 넣거나 나중에 backfill하지 않는다. Receipt가 없으면 reviewer가 `PASS`라고 해도 총괄 scheduler의 `ACCEPTED` 전이 조건을 충족하지 않는다.

### 12.4 Safe observability

허용 후보:

```text
problem/travel/route/propagation/evaluation/build fingerprint
result variant
stable constraint/failure/component code
safe dense route index
metric/objective key, unit, non-sensitive aggregate
fixture ordinal or property seed
```

금지:

- Raw external ID, 주소, 좌표, full route/input bytes
- Customer secret, credential, provider locator
- Arbitrary component exception message에 포함된 input
- Wall clock, thread ID, cache hit/completion order를 result fingerprint나 tie에 사용

Core는 logger/tracer/env/clock을 semantic input으로 읽지 않는다. 외부 telemetry adapter가 typed result에서 allowlisted field만 꺼낸다.

## 13. 흔한 오해와 anti-pattern

| 오해/shortcut | 왜 틀렸는가 | 올바른 경계 |
|---|---|---|
| “Record면 immutable이다” | 내부 list/array alias는 mutable하다 | Defensive copy + mutation probe |
| “최종 load가 0이면 capacity OK” | 중간 prefix가 초과할 수 있다 | 모든 prefix weight/volume 검사 |
| “Delivery-only pickup은 depot visit다” | Travel/stop/service를 오염시킨다 | Initial-load ownership only |
| “A→B가 없으면 B→A를 쓰자” | Directed authority 위반 | Phase 02 incomplete artifact를 `Invalid` |
| “근무 종료에서 이동을 pause하자” | Full-arc contract 위반 | 다음 window에서 arc 전체 재시작 |
| “Hard 위반도 큰 penalty면 된다” | Infeasible가 ranking/SA로 통과한다 | Typed `Infeasible`, downstream 호출 중단 |
| “Overflow는 `Long.MAX_VALUE`면 된다” | 정상 값/absence/failure가 섞인다 | Checked arithmetic → `Invalid` |
| “Metric에 km 단가를 넣자” | Neutral fact와 customer policy가 결합된다 | Metric amount, score에서 price 적용 |
| “Comparator가 route를 다시 계산하자” | Layer와 재현성 붕괴 | Ordered vector만 비교 |
| “HashMap iteration이 지금은 안정적이다” | JVM/input order에 따라 결과가 바뀔 수 있다 | Stable explicit key order |
| “Test exit 0이면 evidence다” | Missing test와 stale XML이 false green을 만든다 | Exact method manifest + fresh clean report |
| “Phase 03 route vectors를 합치면 solution score다” | Unassigned/used vehicle/global metric owner가 비어 있다 | Cross-Phase solution evaluator 승인 대기 |
| “Tie key를 마지막 objective로 넣자” | Business equality와 context tie가 섞인다 | Equality 뒤 별도 approved tie |
| “Facet hook을 미리 만들자” | Package cycle과 verifier gap을 만든다 | Empty set, `ADR-004` 뒤 구현 |
| “Phase 13 backend를 준비해 두자” | `C-17`/14A gate 우회다 | Vendor-neutral core only |
| “현재 root test가 green이니 시작 가능” | Placeholder 1건은 predecessor receipt가 아니다 | Phase 00~02 accepted evidence |

## 14. Phase exit checklist와 Definition of Done

### 14.1 구현 완료 checklist

- [ ] Phase 00~02 accepted receipts와 exact handoff fingerprints가 확인됐다.
- [ ] Internal API, unit, failure triage와 fact/failure/facet package owner/DAG가 review됐다.
- [ ] Route structure corruption이 모두 stable `Invalid`로 분류된다.
- [ ] §11.2 hand fixture의 모든 event/load/resource가 exact 일치한다.
- [ ] Mixed delivery-only/real pair의 모든 weight/volume prefix를 검사한다.
- [ ] Oneway/roundtrip, same-location, directed asymmetry와 final arc 의미가 정확하다.
- [ ] Full-arc restart와 advanced work-window state가 exact하다.
- [ ] `START_ONLY`/`COMPLETE_WITHIN_WINDOW`, inclusive close와 exclusive plan end가 검증됐다.
- [ ] Stop/drive/distance/operational breakdown과 inclusive limits가 검증됐다.
- [ ] 모든 arithmetic/component overflow와 corruption이 `Invalid`다.
- [ ] Physical hard rejection 뒤 evaluation call 0이다.
- [ ] Neutral metric → composed hard → score → objective 순서다.
- [ ] Duplicate key, unit/type/fingerprint mismatch가 fail-closed다.
- [ ] Comparator antisymmetry/transitivity/business equality가 통과한다.
- [ ] Context tie가 non-tied business objective를 뒤집지 않는다.
- [ ] Cache hit/miss/full result의 canonical bytes가 같다.
- [ ] Sequential/parallel repeat의 variant/bytes/fingerprint가 같다.
- [ ] Independent oracle가 seeded defects를 assertion red로 잡는다.
- [ ] Cloud/customer/vendor/solver/verifier/test-fixtures 역의존과 package cycle이 0이다.
- [ ] Safe failure/redaction와 wall-clock 비의미성이 검증됐다.
- [ ] Canonical↔Target↔fresh report의 exact 42-method 차집합과 duplicate가 0이고 11 boundary row가 모두 실행됐다.
- [ ] Required test failed/error/skipped/missing이 0이다.
- [ ] `E-P03-*` bundle이 immutable digest로 봉인됐다.
- [ ] Independent review와 유효한 post-review acceptance receipt가 있다.
- [ ] OPEN/GATED/deferred 항목을 값으로 닫지 않았다.
- [ ] Downstream handoff와 rollback point가 명시됐다.

### 14.2 Definition of Done

Phase 03 `ACCEPTED`는 다음을 모두 뜻한다.

1. Phase 02 authority만으로 route physical facts를 처음부터 다시 만들 수 있다.
2. 정상 hard infeasible과 authority/structure/arithmetic/component invalid를 구분한다.
3. Evaluation은 단방향이며 customer/profile/search/finalization 책임을 침범하지 않는다.
4. Independent oracle가 실제 결함을 검출하고 required suite가 green이다.
5. Cache는 source of truth가 아니며 재현성이 evidence로 확인됐다.
6. Phase 04/05/07이 raw input이나 kernel 내부 scratch 없이 contract를 소비할 수 있다.
7. Evidence와 review/receipt가 immutable identity로 연결됐다.

다음은 DoD가 아니다.

- Source/type/test file 존재
- Root placeholder test 성공
- Console의 `BUILD SUCCESS`
- Canonical Phase 문서가 reviewed됨
- Future command를 문서에 적음
- Independent review report만 있고 receipt는 없음

## 15. 다음 Phase와 downstream 인계

### 15.1 Phase 02에서 받아야 할 producer artifact

```text
ProblemInstanceRef
  schemaVersion
  problemFingerprint
  canonicalInputFingerprint
  denseMappingFingerprints
  preparedTravelFingerprint
  contentDigest

PreparedTravelRef
  schemaVersion
  preparedTravelFingerprint
  locationVehicleMappingFingerprints
  sourceGenerationPolicyFingerprints
  exact coverage proof
  contentDigest

Phase02HandoffManifest
  Phase01 identities
  source policy identities
  E-P02-* refs
  accepted review and receipt refs
```

Broken producer handoff 증상:

- Prepared fingerprint mismatch
- Missing vehicle-resolved time cell
- Raw coordinate/speed/provider function이 runtime fallback으로 노출
- Mutable collection/bitset
- Test-only artifact가 official label로 바뀜

### 15.2 Phase 04에 넘길 artifact

Phase 04가 소비할 것:

- `HardConstraint`, `MetricContributor`, `ScoreComponent`, `ObjectiveDimension`, comparator 계약
- 승인된 후보 A/B의 fact/failure/facet owner, import DAG와 architecture-test identity
- Typed key/version/parameter/unit/fact dependency declaration
- Immutable `PropagationDeclaration`, `EvaluationPlan`과 fingerprints
- Customer-neutral `RouteFacts`, metric/evaluation snapshot
- Duplicate/missing/unit/type mismatch가 pre-evaluation `Invalid`라는 계약
- Facet SPI의 accepted 또는 explicitly empty/deferred status
- `E-P03-*`와 acceptance receipt

Phase 04가 하면 안 되는 것:

- Propagation 복제
- Customer name으로 core 분기
- Profile descriptor를 route fact로 사용
- Solution-level evaluator gap을 route artifact 합으로 임의 해소

Broken Phase 04 handoff 증상:

- Binder가 `latest`나 비슷한 preset으로 fallback
- 같은 declaration identity인데 다른 ordered component bytes
- Metric/score unit mismatch가 runtime까지 내려옴
- Phase 04가 raw route/input을 다시 읽음

### 15.3 Phase 05와 Phase 07의 간접 소비

| Consumer | 소비할 것 | Broken handoff 증상 |
|---|---|---|
| Phase 05 | Pure route kernel, approved full-solution authority, business comparator/equality, exact identities | Invalid route ranking, failed insertion 뒤 state 변화, ad hoc route-vector sum, context tie가 objective를 뒤집음 |
| Phase 07 | Same problem/travel/declaration, cache-free core contracts | Solver/cache dependency, poisoned cache 통과, prepared fingerprint mismatch 무시 |

Phase 03은 Phase 05를 위해 mutable insertion delta API를 만들지 않는다. 성능 cache는 downstream이 수명을 소유하고 full kernel equality를 증명해야 한다.

### 15.4 인계 확인법

```text
consumer.problem == artifact.problem
consumer.travel == artifact.travel
consumer.route/declaration/evaluation identities == artifact identities
consumer compile dependencies obey module DAG
full recomputation == any reused/cache artifact
business comparison happens before any context tie
```

하나라도 다르면 accepted handoff가 아니다. Shim/default로 맞추지 말고 producer contract 또는 consumer integration을 수정한 뒤 새 evidence chain을 만든다.

## 16. Source → requirement → work package → test/evidence 추적성

| Requirement | Source | Work package | Test/oracle | Evidence |
|---|---|---|---|---|
| `REQ-P03-AUTHORITY` exact problem/travel only | [Master §4.2](../../../master-design.md#42-단계별-데이터-계약), [Phase 02 §13.2](../../phases/phase-02-prepared-travel-immutable-problem.md#132-phase-02--phase-03) | WP-03.0~2 | Fingerprint/missing lookup corruption | `E-P03-PROPAGATION` |
| `REQ-P03-PAIR` complete pair/precedence | [Master §6](../../../master-design.md#6-핵심-불변조건과-atomic-mutation), `Q-REQ-01~02` | WP-03.1 | `RouteStructureValidatorTest` | `E-P03-PROPAGATION` |
| `REQ-P03-NUMERIC` checked integer only | [Master §7.2](../../../master-design.md#72-fixed-point와-checked-arithmetic), `Q-NUM-01~03` | WP-03.2~3 | BigInteger overflow/corruption | `E-P03-PROPAGATION`, `E-P03-EVALUATION` |
| `REQ-P03-TIME` plan/window/service/full arc | [Master §7.3](../../../master-design.md#73-planning-period와-time), `Q-TIME-01~04`, `Q-IN-01~02` | WP-03.2 | Hand/boundary/metamorphic oracle | `E-P03-PROPAGATION` |
| `REQ-P03-TRAVEL` directed prepared lookup | [Master §8](../../../master-design.md#8-directed-distancetime-matrix-계약) | WP-03.0~2 | Asymmetry, missing-cell corruption | `E-P03-PROPAGATION` |
| `REQ-P03-LOAD` mixed service prefix | [Final Domain §9](../../../2026-07-26-domain-design.md#9-route-propagation과-resource) | WP-03.2 | Hand table + prefix property | `E-P03-PROPAGATION` |
| `REQ-P03-RESOURCE` stop/drive/operational | [Final Domain §9](../../../2026-07-26-domain-design.md#9-route-propagation과-resource), `Q-BENCH-01` | WP-03.2 | Stop/final arc/boundary tests | `E-P03-PROPAGATION` |
| `REQ-P03-LAYER` metric before composed hard | [Master §9.1](../../../master-design.md#91-단방향-평가-구조), [Review §4](../../reviews/phase-03-review.md#4-findings) | WP-03.3 | Invocation trace spies | `E-P03-EVALUATION` |
| `REQ-P03-COMPONENT` typed fail-closed | [Canonical Phase 03 §7](../../phases/phase-03-route-propagation-evaluation-kernel.md#7-io-artifact-contract-identity와-lifecycle) | WP-03.3 | Duplicate/unit/type/throwing component | `E-P03-EVALUATION` |
| `REQ-P03-OBJECTIVE` lexicographic priority | [Final Domain §10](../../../2026-07-26-domain-design.md#10-evaluation-profile과-objective), `Q-OBJ-01~03` | WP-03.4 | Comparator pair/triple exhaustive | `E-P03-COMPARATOR` |
| `REQ-P03-SOLUTION-EVAL` full solution authority | [Master RM-2](../../../master-design.md#154-rm-2--propagation-evaluation과-bound-profile), [Review residual](../../reviews/phase-03-review.md#62-residual-blockers--2) | WP-03.0, 4, 6 | Future cross-Phase compile/full-equality/corruption | Handoff review + `E-P03-EVALUATION` |
| `REQ-P03-TIE` business equality/context tie separation | [Review §6.2](../../reviews/phase-03-review.md#62-residual-blockers--2) | WP-03.4, 6 | Non-tied reversal and permutation tests | `E-P03-COMPARATOR` |
| `REQ-P03-CACHE` full recomputation authority | [Master §12](../../../master-design.md#12-candidate-state-cache와-rollback) | WP-03.5 | Poisoned cache/full equality | `E-P03-EVALUATION`, `E-P03-COMPARATOR` |
| `REQ-P03-REPRO` deterministic result | [Master §13](../../../master-design.md#13-termination-reproducibility와-execution-provenance) | WP-03.5~6 | Sequential/parallel canonical equality | `E-P03-COMPARATOR` |
| `REQ-P03-ORACLE` independent defect sensitivity | [Integrated §22](../../../architecture-domain-implementation-design.md#22-test와-evidence-matrix) | WP-03.5 | Seeded faulty doubles | All `E-P03-*` |
| `REQ-P03-ARCH` dependency isolation | [Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계) | WP-03.6 | `Phase03KernelArchitectureTest` | Architecture report + all keys |
| `REQ-P03-PACKAGE-OWNER` fact/failure/facet owner conflict | [Current Domain §3.1](../../../domain-design.md#31-architecture-package-mapping), [Current Architecture §6.1](../../../architecture-design.md#61-rpdptw-core), [dated Final Domain §3](../../../2026-07-26-domain-design.md#3-전체-처리-흐름과-책임-경계), [Original Review F-P03-010](../../reviews/phase-03-review.md#f-p03-010--open-facet-type-이름placement가-package-cycle을-암시했다) | WP-03.0, 6 | Approved 후보 DAG compile + cycle/consumer tests | Architecture decision record + all keys |
| `REQ-P03-EXACT-MANIFEST` canonical 42 methods + 11 boundary rows | [Canonical Phase 03 §9.3](../../phases/phase-03-route-propagation-evaluation-kernel.md#93-exact-test-classmethod-matrix) | WP-03.0~6 | Canonical↔Target↔fresh report set equality, boundary parameter report | All `E-P03-*` |
| `REQ-P03-SECURITY` safe failure/redaction | [Integrated §20](../../../architecture-domain-implementation-design.md#20-security와-tenant-boundary) | WP-03.3, 5, 6 | Redaction/exception payload/repro tests | Security report + all keys |
| `REQ-P03-HANDOFF` Phase 04/05/07 portable contract | [Integrated Phase 3](../../../architecture-domain-implementation-design.md#7-phase-3--경로-전파-계산과-평가-kernel) | WP-03.6 | Downstream compile/equality contract | Receipt + handoff manifest |

## 17. 구현자가 마지막으로 확인할 짧은 판정표

```text
아직 predecessor receipt가 없다
→ 멈춘다. 문서·oracle 설계까지만 한다.

fact/failure/facet package owner가 필요하다
→ 후보 A/B를 임의 선택하지 않는다. 승인된 DAG/ADR 전 file을 만들지 않는다.

route authority/structure가 손상됐다
→ Invalid. score나 unassignment로 보내지 않는다.

정상 값이 hard bound를 넘었다
→ Infeasible. downstream score/objective를 호출하지 않는다.

route facts가 완성됐다
→ neutral metric, composed hard, score, objective 순서로 간다.

solution evaluator/tie API가 필요하다
→ 임의 구현하지 않는다. cross-Phase review blocker를 연다.

test가 exit 0이다
→ fresh exact method manifest와 skipped 0까지 확인한다.

code와 test가 모두 있다
→ 아직 완료가 아니다. immutable evidence, independent review, receipt를 확인한다.

Phase 13/14 수치나 backend가 필요해 보인다
→ 이 Phase scope가 아니다. OPEN/GATED authority를 우회하지 않는다.
```
