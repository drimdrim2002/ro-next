# Phase 07 — 독립 검증과 최종 결과

```yaml
document_status: REVIEWED_WITH_CORRECTIONS
document_version: 1.2
document_workflow_status: INDEPENDENT_REVIEW_COMPLETE_CHANGES_REQUIRED
phase: "07"
phase_name: independent-verification-final-result
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
evidence_status: NOT_PRODUCED
review_status: COMPLETE
review_verdict: CHANGES_REQUIRED
review_document: ../reviews/phase-07-review.md
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Verification/Result owner role
  upstream_candidate: Phase 06 COW ALNS/Reproducibility owner role
  upstream_authority: Phase 02 Domain/Travel and Phase 04 Capability/Profile owner roles
  independent_oracle: Phase 07 independent verification-test owner role
  downstream_contract: Phase 08 Application/Local Runtime owner role
  downstream_benchmark: Phase 14A ALNS Benchmark Qualification owner role
  review: independent Phase 07 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture
  - Phase 01 accepted canonical normalization
  - Phase 02 accepted immutable ProblemInstance and complete PreparedTravel
  - Phase 03 accepted cache-free propagation/evaluation kernel
  - Phase 04 accepted exact BoundProfile and evaluation declaration
  - Phase 05 accepted pair insertion and initial portfolio contracts
  - Phase 06 accepted committed candidate and replay bundle
planned_evidence:
  - E-P07-CANDIDATE-VERIFY
  - E-P07-AUDIT
  - E-P07-RESULT-VERIFY
source_sections:
  canonical_master: "§2.2~2.4, §4.1~4.6, §6, §10, §12~14.1, §15.7, §16~17"
  final_domain: "§7~10, §15~17.5, §17.9, §18"
  final_architecture: "§2.1~2.7, §5.2~5.6, §6"
  integrated_design: "§3, §10~12, §19, §21~25, §27~28"
  open_questions: "Q-MTX-01~03, Q-OBJ-01~03, Q-RES-01~02, Q-BENCH-01~03, Q-INFRA-01, Q-VAR-01"
  master_realization_plan: "§2~4, Phase 06~08, §8~15"
  phase_02_actual: "§6, §13.2~13.3"
  phase_03_actual: "§7~9, §14.3"
  phase_04_actual: "§7.1, §7.4~7.5, §14.2"
  phase_06_actual: "§7.1, §7.6, §8.4~8.5, §16.2"
  phase_08_actual: "§3.2, §6.4~7.7, §11.5, §16.1"
source_fingerprints_sha256:
  README.md: 22eff4f63607db29bd4049344986109c680aa970d0865a3b859598e6b3b96c06
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
  docs/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d
  docs/implementation/README.md: 6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358
neighbor_validation_policy:
  rule: ADJACENT_PHASE_AND_REVIEW_DIGESTS_NOT_PERSISTED_OR_USED_FOR_ACCEPTANCE
  method: cited-section semantic comparison plus accepted artifact/evidence identity at entry
historical_cross_check:
  file: docs/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
  sha256: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
neighbor_phase_documents:
  phase_04: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_NOT_PRODUCED_NOT_ACCEPTED
  phase_06: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_NOT_PRODUCED
  phase_08: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_NOT_PRODUCED_NOT_READY
  phase_07_review: ACTUAL_COMPLETE_CHANGES_REQUIRED_BLOCKED_NOT_IMPLEMENTED
```

## 1. 문서 지위와 권위

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 권위 원문의 `REVIEW` metadata는 provenance로 보존하지만 이 상세 문서 작성을 중단하는 조건이 아니다. 반대로 이 문서, proposed Java 이름, future test 목록 또는 예시 manifest가 존재한다는 사실은 Phase 07 구현·evidence·독립 review가 완료되었다는 뜻이 아니다.

상태는 서로 섞지 않는다.

| 상태 축 | 현재 값 | 의미 |
|---|---|---|
| 문서 | `REVIEWED_WITH_CORRECTIONS` | 독립 review의 안전한 정정을 반영했으나 `CHANGES_REQUIRED` verdict와 residual blocker가 남음 |
| 구현 | `NOT_STARTED` | Target module/type/test는 아직 존재한다고 주장하지 않음 |
| evidence | `NOT_PRODUCED` | 아래 evidence key는 미래 bundle 요구 이름 |
| entry gate | `BLOCKED_BY_UNACCEPTED_PREDECESSORS` | Phase 00~06 accepted artifact가 없음 |
| review | `COMPLETE — CHANGES_REQUIRED` | 독립 document review는 완료됐으나 residual cross-phase blocker와 구현/evidence gate가 남음 |
| handoff | `NOT_READY` | Phase 08이 소비할 accepted output contract가 아직 없음 |

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 verification/result 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/package/DAG
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 15 Phase 배치
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 역사 자료로만 취급하며 현재 authority, API 이름 또는 evidence로 사용하지 않는다.

Final Domain/Architecture에 남은 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 최신 Canonical Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. 이는 Phase 07에 AWS 코드를 추가하거나 구현·배포·cutover를 완료했다고 주장하는 근거가 아니다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 07에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §2.2~§2.4, §4.1~§4.6, §6, §10, §12~§14.1, §15.7, §16~§17 | 두 verifier, route/bank와 result 분리, final audit, cache 비권위, provenance, `RM-5` gate |
| [Final Domain](../../2026-07-26-domain-design.md) | §7~§10, §15~§17.5, §17.9, §18 | Immutable authority, stable state, propagation/evaluation 의미, outcome/audit/error/evidence |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2.1~§2.7, §5.2~§5.6, §6 | `rpdptw-verification`, solver dependency 금지, immutable artifact, publication/test 경계 |
| [Integrated design](../../architecture-domain-implementation-design.md) | §3, §10~§12, §19, §21~§25, §27~§28 | Phase 06 input, Phase 07 sequence, Phase 08 output, provenance, corruption, anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-MTX-01~03`, `Q-OBJ-01~03`, `Q-RES-01~02`, `Q-BENCH-01~03`, `Q-INFRA-01`, `Q-VAR-01` | Prepared travel, objective, two-state outcome, audit, official/open/deferred 경계 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§4, Phase 06~08, §8~§15 | Current inventory, phase gate/evidence/DoD/blocker/handoff/traceability |
| [구현 문서 지도](../README.md) | §3~§7 | Authority, canonical filename, planned link와 scheduler/review 규칙 |
| [Root README](../../../README.md) | 기술 기준, 배포, placeholder 설명 | Java 25/Maven/GCP placeholder의 actual inventory와 target evidence 분리 |
| [Phase 02 상세](phase-02-prepared-travel-immutable-problem.md) | §6, §13.2~§13.3 | `ProblemInstanceRef`/`PreparedTravelRef`, equality proof, same identity/different bytes failure |
| [Phase 03 상세](phase-03-route-propagation-evaluation-kernel.md) | §7~§9, §14.3 | Cache-free kernel contract, `Feasible/Infeasible/Invalid`, Phase 07 consumer boundary |
| [Phase 04 actual 상세](phase-04-capabilities-customer-profiles.md) | §7.1, §7.4~§7.5, §14.2 | `BoundProfile` 안의 exact Phase 03 declaration, `SolvePlan`, problem/travel/profile equality와 Phase 07 handoff |
| [Phase 06 actual 상세](phase-06-cow-alns-reproducibility.md) | §7.1, §7.6, §8.4~§8.5, §16.2 | `CommittedCandidate`, `ReplayManifest`, `Phase06EvidenceManifest`, exact termination과 solver dependency 없는 handoff |
| [Phase 08 actual 상세](phase-08-application-ports-local-runtime.md) | §3.2, §6.4~§7.7, §11.5, §16.1 | `Phase07Output.Publishable/Rejected` exhaustive consumption, `GateIncomplete` lossless mapping/test와 both-gate handoff receipt |

Phase 04 v1.1은 actual `REVIEWED_CHANGES_REQUIRED`, Phase 06 v1.2는 actual `CHANGES_REQUIRED`, Phase 08 v1.3은 actual `REVIEWED_WITH_CORRECTIONS`이며 세 review 모두 `COMPLETE — CHANGES_REQUIRED`다. 구현/evidence는 모두 `NOT_STARTED`/`NOT_PRODUCED`이고 accepted implementation evidence가 없으므로 proposed cross-phase contract source이지 완료 authority가 아니다. 인접 Phase/review의 whole-file 또는 section digest를 metadata, authority나 acceptance 조건으로 저장하지 않는다. Entry에서는 위에 인용한 section의 의미와 accepted artifact/evidence identity만 다시 대조한다.

### 1.2 ALNS-first verification boundary

Phase 07은 ALNS-only candidate/result를 MIP solver, backend objective, solver license,
native runtime 또는 production authority 없이 독립 검증할 수 있어야 한다. Phase 07
`PASS`는 feasibility/correctness와 publishable result integrity를 뜻하며, quality,
performance 또는 variance threshold acceptance를 뜻하지 않는다.

Phase 07의 accepted both-gate evidence는 Phase 08 local run과 Phase 14A benchmark
qualification으로 직접 handoff한다. Phase 13은 Phase 14A가 별도의 immutable
benchmark bundle을 독립 review해 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`를 발행한
뒤에만 이를 소비할 수 있다. Raw MIP incumbent, backend objective 또는 selected
column은 이 verifier의 authority input이 아니다.

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 07은 Phase 06이 넘긴 committed candidate와 replay evidence를 **search와 독립된 module 및 authority path**에서 검증하고, 검증된 solution만 finalization/audit/result-integrity gate로 통과시켜 Phase 08이 소비할 immutable final result contract를 만든다.

핵심 완료 의미는 다음 AND gate다.

```text
same immutable problem/prepared-travel/profile authority
+ route/bank cache-free candidate verification PASS
+ exactly-one final outcomes
+ required final-solution insertion audit
+ outcome-derived summary and canonical payload
+ independent result-integrity verification PASS
= Phase 08가 받을 PublishableResult
```

Search와 candidate verifier는 같은 immutable `ProblemInstance`와 `PreparedTravel`의 exact fingerprint를 사용한다. 그러나 verifier는 search가 만든 feasibility flag, propagation cache, insertion table, metric/score/objective cache, candidate summary 또는 replay 성공 주장을 계산 권위로 신뢰하지 않는다. Candidate가 선언한 metric/objective는 독립 재계산 결과와 **사후 대조할 비권위 claim**일 뿐이다.

### 2.2 포함 범위

- Phase 06 committed route/bank와 replay bundle의 immutable handoff projection
- Problem/travel/profile/candidate/replay identity와 content-integrity 확인
- Solver/search/cache compile dependency가 없는 candidate verifier
- Route/bank exact partition, complete pair, same-vehicle, precedence, terminal/service pattern 검증
- Prepared directed travel을 사용한 load/time/window/resource와 모든 hard constraint 재계산
- Neutral metric, score breakdown, objective vector와 candidate claim 대조
- `FEASIBLE`, `INFEASIBLE`, `INVALID`, `CORRUPT`의 fail-closed 구분
- Candidate `PASS` 뒤 preliminary `ASSIGNED`/`UNASSIGNED` partition
- Static `PROVEN` 분리와 나머지 unassigned request의 final-solution exhaustive insertion audit
- Evidence 범위를 넘지 않는 unassigned reason/scope/confidence/source
- Exactly-one final outcome, ownership reference와 outcome-derived summary
- Versioned final-result manifest, semantic fingerprint, canonical payload digest와 replay/provenance
- Result-integrity verifier와 both-gate typed output
- Malicious/corruption/fingerprint/overflow/tampering/serialization 독립 oracle와 future-red test 설계
- Phase 08이 받을 pure Java contract와 handoff manifest

### 2.3 명시적 비범위

- Phase 06 ALNS, candidate 선택, COW mutation, search cache 또는 replay 실행 구현
- Invalid candidate 자동 수정, pair 복구, route repair, re-solve 또는 fallback candidate 선택
- Audit에서 찾은 feasible insertion 자동 적용이나 solver 재호출
- Phase 08 application use case, `ResultPublisher`, retrieval/status, local filesystem, same-process dispatcher
- Phase 09 object storage, digest locator, CAS pointer와 database/no-DB 구현
- Phase 10~12 coordinator, AWS S3/Step Functions/Lambda 또는 provider runtime/distribution
- Phase 13 route pool/MIP와 raw optimizer incumbent
- Phase 14 official manifest 수치, baseline, comparison, production cutover
- External public/wire schema, JSON field naming, media type 또는 long-term compatibility 승인
- `Q-BENCH-02`의 official step/worker/round/watchdog 수치 생성
- Multi-trip/rotation, MDVRP/OVRP/SDVRP 또는 운영자의 후속 외주/이월 상태
- Raw input 재파싱, travel 재생성, `latest` profile/config 조회

`PublishableResult`는 “정상 publication에 적격한 immutable 결과”라는 Phase 07 semantic type다. 실제 저장, CAS publication, 외부 status 전이와 retrieval은 Phase 08 이후의 책임이다.

## 3. Phase-local 결정, 불변조건과 판정 분류

이 절의 package/type/signature는 **proposed internal design**이다. 의미는 고정하지만 이름, visibility, canonical encoding과 public schema는 해당 ADR/review 전까지 compatibility 약속이 아니다.

### 3.1 결정 상태

| 항목 | 상태 | Phase 07 판단 |
|---|---|---|
| 두 gate 순서 | FIXED | Candidate verifier → finalization/audit → result verifier 순서를 바꾸거나 합치지 않음 |
| Authority | FIXED | Phase 02 problem/travel과 Phase 04 bound declaration의 exact immutable identity만 사용 |
| Search claim | FIXED | Cache/summary/feasibility는 권위가 아니며 재계산 결과와의 불일치는 거부 |
| Outcome | FIXED | `ASSIGNED`/`UNASSIGNED` 두 상태만 solver final outcome |
| Final audit | FIXED | Static `PROVEN` 외 모든 unassigned를 final routes 고정 상태에서 전수 검사 |
| Verifier disposition | PROPOSED INTERNAL | `FEASIBLE/INFEASIBLE/INVALID/CORRUPT` typed 구분 |
| Diagnostic Java enum 이름 | PROPOSED | Source/scope/confidence 의미만 고정; exact wire code는 public schema review 전 OPEN |
| Result canonical encoding | OPEN/PROPOSED | Versioned deterministic internal encoding 필요. External JSON/media type은 `ADR-002` 또는 동등 승인 전 미확정 |
| Fingerprint hash algorithm | OPEN/PROPOSED | Phase 02/serialization ADR의 algorithm/version을 그대로 소비. `SHA-256`을 새 public default로 확정하지 않음 |
| Official benchmark vector 사용 | GATED | Source formula는 파생 가능하지만 official 비교/baseline은 Phase 14와 `Q-BENCH-02` gate |

### 3.2 반드시 지킬 불변조건

1. **Exact authority:** Candidate bundle, `ProblemInstance`, `PreparedTravel`, `BoundProfile`의 declared/intrinsic/content identity가 모두 일치해야 계산을 시작한다.
2. **Same authority, independent claims:** Search와 verifier는 같은 immutable facts를 사용하지만 search-derived cache/evaluation claim을 verifier 계산 입력으로 사용하지 않는다.
3. **No raw reinterpretation:** Verifier는 raw DTO, coordinate, speed, reverse arc, customer name, `latest` registry와 provider metadata를 보지 않는다.
4. **Partition:** 모든 request는 candidate에서 exactly one complete same-route pair 또는 bank membership 중 하나다.
5. **Physical truth:** Route order, request ownership, vehicle/terminal binding과 bank가 source of truth이고 파생 cache는 버릴 수 있어야 한다.
6. **Checked arithmetic:** 재계산과 summary/audit count의 합·차·곱은 checked 수행한다. Overflow를 wrap, clamp, penalty 또는 `UNASSIGNED`로 바꾸지 않는다.
7. **Candidate PASS before finalization:** `VerifiedSolution`은 `FEASIBLE` 재계산과 모든 candidate claim parity가 통과한 경우에만 생성된다.
8. **Final outcomes:** 모든 input request는 정확히 하나의 `ASSIGNED` 또는 `UNASSIGNED` outcome을 가진다.
9. **Ownership:** `DIRECT`/`LEASE` route에 있으면 모두 `ASSIGNED`; fleet 밖 vehicle, `OUTSOURCED`, `DEFERRED`를 만들지 않는다.
10. **Audit completeness:** Static-proven skip 이외에는 eligible concrete vehicle과 모든 합법 pickup/delivery position pair를 검사해야 exhaustive confidence를 쓸 수 있다.
11. **No audit mutation:** Feasible insertion을 찾아도 verified route, outcome, candidate fingerprint와 audit input을 변경하지 않는다.
12. **Evidence ceiling:** Diagnostic confidence는 실제 static/audit/search-observation source의 범위를 넘지 않는다.
13. **Outcome-derived summary:** Count, ownership, route metric, objective와 reference metric은 verified routes/outcomes에서 다시 만든다.
14. **Two independent reports:** Candidate `PASS`는 result `PASS`를 대신하지 않고, result `PASS`는 candidate failure를 덮지 않는다.
15. **Deterministic identity:** Semantic identity는 stable ordering과 versioned canonical projection을 사용하며 map insertion, locale, timezone, thread completion, `toString()`에 의존하지 않는다.
16. **Observation separation:** Elapsed time, log order, provider locator와 attempt observation은 quality/result semantic fingerprint의 hidden input이 아니다.
17. **No normal payload on failure:** 어느 gate든 fail/incomplete이면 route/outcome/benchmark vector를 정상 payload로 만들지 않는다.
18. **Immutable handoff:** Phase 08은 `PublishableResult` 또는 typed rejection만 받고 mutable builder/cache/auditor 내부 상태를 받지 않는다.

### 3.3 `FEASIBLE` / `INFEASIBLE` / `INVALID` / `CORRUPT`

| 판정 | 정확한 의미 | 예 | 정상 outcome/publication인가 |
|---|---|---|---:|
| `FEASIBLE` | Authority/structure가 유효하고 cache-free hard/evaluation 재계산 및 candidate claim parity가 모두 통과 | Complete pair route, exact metric/objective | Candidate `PASS`로 승격 가능 |
| `INFEASIBLE` | Authority와 구조는 해석 가능하지만 authoritative hard constraint가 정상적으로 거절 | Capacity/time-window/resource 초과 | 아니오; unassigned reason으로 변환 금지 |
| `INVALID` | Canonical content는 읽혔으나 domain/state/contract가 성립하지 않거나 checked 계산을 완료할 수 없음 | Duplicate/missing task, split pair, route+bank XOR 위반, unknown ID, overflow | 아니오; defect/input investigation |
| `CORRUPT` | Identity, bytes, provenance 또는 비권위 claim이 authoritative content/recomputation과 불일치 | Fingerprint mismatch, same ID/different bytes, tampered score/result/payload digest | 아니오; integrity incident |

분류 우선순위는 fail-closed로 적용한다.

```text
content/digest/authority mismatch       → CORRUPT
well-digested domain/state violation    → INVALID
well-formed candidate hard rejection    → INFEASIBLE
all checks and claims exact              → FEASIBLE
```

`UNASSIGNED`는 위 네 판정 중 하나가 아니다. `UNASSIGNED`는 **이미 `FEASIBLE`로 검증된 solution 안에서 특정 request가 어느 verified route에도 없는 정상 final outcome**이다.

`INCOMPLETE`도 위 네 verification disposition 중 하나가 아니다. Audit cancellation/interruption, required work 미완료 또는 result gate가 끝까지 실행되지 못한 상태를 `INVALID`, `INFEASIBLE`이나 `CORRUPT`로 위장하지 않는다. Phase 07의 top-level output은 `Publishable` 또는 `Rejected`이고, `Rejected` 내부에서 completed semantic verdict인 `VerificationRejected(disposition, failure)`와 미완료인 `GateIncomplete(incompleteFailure)`를 분리한다. 둘 다 정상 route/outcome/payload를 운반하지 않는다.

## 4. Entry gate와 확인 evidence

문서 review와 corruption fixture 설계는 진행할 수 있지만 Phase 07 implementation/evidence 착수와 `ACCEPTED` 주장은 다음 gate가 모두 충족될 때까지 차단한다.

| Entry 항목 | 확인 방법 | 2026-07-28 local 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | `E-P00-ARCH`, accepted review, reactor와 verification/solver 분리 | Root 단일 Maven project, accepted evidence 없음 | BLOCKED |
| Phase 01 accepted | Canonical request universe, numeric/time/service fingerprints | Target artifact/evidence 없음 | BLOCKED |
| Phase 02 accepted | `ProblemInstanceRef`, `PreparedTravelRef`, exact fingerprint/content equality, `E-P02-*` | 상세/review는 actual이고 document review는 correction 후 pass지만 phase verdict는 `BLOCKED_NOT_IMPLEMENTED`; accepted artifact/evidence 없음 | BLOCKED |
| Phase 03 accepted | Cache-free route/solution kernel, typed failure, `E-P03-PROPAGATION/EVALUATION/COMPARATOR` | 상세/review는 actual이나 구현·evidence 없음; review verdict `CHANGES_REQUIRED`, solution-level evaluator owner/API gap이 residual | BLOCKED |
| Phase 04 accepted | Exact `BoundProfile`, dependency closure와 contract fingerprint | 상세/review actual, document verdict `CHANGES_REQUIRED`; implementation/evidence 없음 | BLOCKED |
| [actual Phase 05](phase-05-pair-insertion-initial-portfolio.md) accepted | Stable pair/route/bank snapshot과 side-effect-free exhaustive insertion evaluator | 상세/review actual, document verdict `CHANGES_REQUIRED`; implementation/evidence 없음 | BLOCKED |
| [actual Phase 06](phase-06-cow-alns-reproducibility.md) accepted | `CommittedCandidate`, `ReplayManifest`, `Phase06EvidenceManifest`, exact termination, cache-free handoff projection, `E-P06-*` | v1.2 상세/review actual, document verdict `CHANGES_REQUIRED`; implementation/evidence 없음 | BLOCKED |
| Independent oracle implementation review | Production evaluator/serializer를 호출하지 않는 source/bytecode와 seeded sensitivity evidence 승인 | v1.1 document design은 review됨; target oracle source/test/evidence는 없음 | REVIEW_REQUIRED_AT_IMPLEMENTATION |
| Canonical result encoding review | Version/order/inclusion/exclusion과 duplicate-key rule 승인 | External schema 미승인 | CONTRACT_GATE |
| Scheduler/owner | Exact task ID와 implementer/oracle/reviewer 역할 지정 | Task ID 미제공 | OWNER_GATE |
| Solution-level evaluation contract | Problem/travel/profile + ordered routes + bank의 exact aggregation/identity/failure/comparator owner와 API 승인 | Phase 03 review `F-P03-004` residual; Phase 07 pseudocode의 `checkedFullSolutionRecompute`를 구현할 accepted owner/API 없음 | CROSS_PHASE_BLOCKER |

Phase 06 handoff를 받을 때 최소 다음 equality를 재검증한다.

```text
candidate.problemFingerprint
  == ProblemInstance.fingerprint
  == phase06Replay.problemFingerprint

candidate.preparedTravelFingerprint
  == ProblemInstance.preparedTravelFingerprint
  == PreparedTravel.fingerprint
  == phase06Replay.preparedTravelFingerprint

candidate.boundProfileFingerprint
  == BoundProfile.fingerprint
  == phase06Replay.boundProfileFingerprint

candidate.evaluationDeclarationFingerprint
  == BoundProfile Phase03 declaration fingerprint
  == phase06Replay.evaluationDeclarationFingerprint

candidate.solvePlanFingerprint
  == BoundProfile.solvePlan.fingerprint
  == phase06Replay.solvePlanFingerprint

candidate content digest
  == digest(
       handoff contract/version
       + problem/travel/profile/evaluation/solve-plan authority refs
       + canonical committed routes
       + bank
       + declared claims and their fingerprint)

phase06 bundle accepted review/evidence refs
  == scheduler-declared predecessor refs
```

Mismatch가 있으면 raw input을 다시 읽거나 비슷한 snapshot을 찾아 보완하지 않는다. Phase 06 owner에게 typed `CORRUPT(AUTHORITY_MISMATCH)` receipt를 반환하며 마지막 안전 지점은 accepted Phase 06 bundle 원본과 이 문서/test fixture다.

## 5. 2026-07-28 current inventory

Code/toolchain inventory의 historical baseline은 branch `codex/domain-design`, commit `3424277c9c74`다. 이 문서를 만들기 전 `docs/implementation/` 전체가 Git 기준 untracked였다는 관찰과 당시 문서 개수는 historical snapshot으로만 보존한다. 인접 문서의 live review status는 metadata와 아래 별도 행에서 현재 파일의 명시적 status/verdict를 재대조했다.

| 항목 | 실제 관찰 | Phase 07 해석 |
|---|---|---|
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Java 25 목표와 일치하지만 Phase 07 evidence가 아님 |
| Maven | Root `pom.xml` 하나, `com.ronext:ro-next:0.1.0-SNAPSHOT`, release 25 | Target `rpdptw-verification`/reactor/architecture enforcement 없음 |
| Root dependencies | Google Workflow Executions, Google Cloud Storage, Jackson, JUnit가 한 classpath | Core/solver/verification/application/provider 격리 없음 |
| Main Java | `com.ronext.optimizer` 아래 6개 파일 | Candidate/result verifier, domain route, audit/result type 없음 |
| Placeholder solver | `AlnsBatchEngine`이 `double` 합성 objective와 `Map<String,Object>` candidate를 생성 | RPDPTW candidate/feasibility/replay evidence가 아님 |
| Placeholder finalization | Controller가 GCS prefix listing 후 최소 `objective` map을 결과로 저장 | Declared completeness, both-gate result와 독립 verification을 충족하지 않음 |
| Test | `AlnsBatchEngineTest` 한 개가 status/run number/positive objective만 검사 | Phase 07 corruption/audit/result evidence가 아님 |
| Phase 상세 — historical snapshot | 최초 inventory에서 00~14 canonical 상세 15개 actual | 당시 파일 개수 기록일 뿐 live acceptance 조건이 아님 |
| Review — historical snapshot | 최초 inventory에서 Phase 00~07 review 8개 actual | 이후 생성/변경 수를 authoritative snapshot으로 사용하지 않음 |
| Live reviewed neighbors | Phase 04 `REVIEWED_CHANGES_REQUIRED`, Phase 06 `CHANGES_REQUIRED`, Phase 08 `REVIEWED_WITH_CORRECTIONS`; 세 review `COMPLETE — CHANGES_REQUIRED` | 모두 `NOT_STARTED`/`NOT_PRODUCED`; Phase 04 `NOT_ACCEPTED`, Phase 08 `NOT_READY`; Phase 07 acceptance를 열지 않음 |

현재 source와 ignored `target/`을 검증 통과 evidence로 재사용하지 않는다. 이 문서 작성 turn에서는 `pom.xml`, `src`, `gcp`, 다른 Phase/review, 공용 README/plan/progress와 `docs/codex`를 수정하지 않는다.

Read-only inventory source fingerprint:

| File | SHA-256 |
|---|---|
| `pom.xml` | `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6` |
| `.sdkmanrc` | `25c276822911b813a58c317ee47b86608e51c79ba921c65706df9f9f74793be5` |
| `src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | `4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481` |
| `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` | `947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829` |

이 fingerprint는 source 존재/변경 감지용 inventory일 뿐 accepted build, test pass, deployment, candidate 또는 result evidence가 아니다. Metadata는 안정된 canonical source fingerprint만 보존하며 인접 Phase/review digest를 acceptance 조건으로 사용하지 않는다.

## 6. Proposed 변경 module/package/file tree

Phase 07 구현에서 허용할 target은 아래와 같다. 실제 경로는 Phase 00 accepted reactor/ADR과 일치해야 하며, 이 문서 작성 turn에서는 어느 Java/test/POM 파일도 만들지 않는다.

```text
rpdptw/verification/
├── pom.xml
├── src/main/java/com/ronext/rpdptw/
│   ├── verification/
│   │   ├── api/
│   │   │   ├── CandidateVerifier.java
│   │   │   ├── CandidateVerificationRequest.java
│   │   │   ├── CandidateVerification.java
│   │   │   ├── VerificationDisposition.java
│   │   │   ├── VerificationFailure.java
│   │   │   ├── CandidatePassReport.java
│   │   │   └── VerifiedSolution.java
│   │   ├── candidate/
│   │   │   └── internal/
│   │   │       ├── CacheFreeCandidateVerifier.java
│   │   │       ├── CandidateAuthorityValidator.java
│   │   │       ├── CandidatePartitionValidator.java
│   │   │       └── CandidateClaimComparator.java
│   │   └── result/
│   │       ├── ResultIntegrityVerifier.java
│   │       ├── ResultVerificationRequest.java
│   │       ├── ResultVerification.java
│   │       └── internal/
│   │           ├── CacheFreeResultIntegrityVerifier.java
│   │           ├── OutcomePartitionValidator.java
│   │           ├── ResultDerivationOracle.java
│   │           └── CanonicalResultVerifier.java
│   └── result/
│       ├── api/
│       │   ├── Phase07FinalResultService.java
│       │   ├── Phase07Request.java
│       │   ├── Phase07Output.java
│       │   ├── FinalOutcome.java
│       │   ├── UnassignedDiagnostic.java
│       │   ├── ResultSummary.java
│       │   ├── FinalResultManifest.java
│       │   ├── PublishableResult.java
│       │   └── FinalResultRejection.java
│       ├── finalization/
│       │   ├── FinalResultFinalizer.java
│       │   ├── FinalInsertionAuditor.java
│       │   ├── FinalInsertionAuditRecord.java
│       │   └── internal/
│       │       ├── ExhaustiveFinalInsertionAuditor.java
│       │       ├── EvidenceBoundedDiagnosticFactory.java
│       │       └── OutcomeDerivedSummaryFactory.java
│       └── encoding/
│           ├── CanonicalResultEncoder.java
│           ├── CanonicalResultPayload.java
│           └── internal/
│               └── VersionedCanonicalResultEncoder.java
└── src/test/java/com/ronext/rpdptw/
    ├── verification/candidate/...
    ├── result/finalization/...
    ├── verification/result/...
    └── result/encoding/...

build/test-fixtures/
└── src/test/java/com/ronext/rpdptw/testing/phase07/
    ├── Phase07AuthorityFixtureBuilder.java
    ├── CandidateArtifactBuilder.java
    ├── CorruptedCandidateBuilder.java
    ├── MaliciousCandidateBuilder.java
    ├── AuditScenarioBuilder.java
    ├── FinalResultDraftBuilder.java
    ├── ReferenceCandidateOracle.java
    ├── ReferenceInsertionOracle.java
    ├── ReferenceResultOracle.java
    ├── ReferenceCanonicalEncodingOracle.java
    ├── Phase07OracleIndependenceTest.java
    └── Phase07OracleSensitivityTest.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase07VerificationArchitectureTest.java
```

허용되는 Maven compile dependency는 다음뿐이다.

```text
rpdptw-verification → rpdptw-core

rpdptw-application  → rpdptw-core
rpdptw-application  → rpdptw-solver
rpdptw-application  → rpdptw-verification
```

`rpdptw-verification → rpdptw-solver`, search/cache package, application, adapter, cloud/HTTP/provider SDK, profile implementation JAR와 optimizer vendor API는 금지한다. Phase 06 solver-owned class를 직접 받지 않고 §7의 immutable handoff projection을 받는 이유가 이 의존 방향이다.

`build/test-fixtures`는 Phase 00이 정의한 Maven `src/test/java` attached test-jar 규칙을 따른다. `src/testFixtures/java` 같은 비표준 source root를 암묵적으로 가정하지 않는다. Verification production/main artifact는 fixture를 의존하지 않고, verification test만 `type=test-jar`, `classifier=tests`, `scope=test`로 소비한다. Fixture/oracle main-runtime 전이 또는 fixture → verification production dependency는 금지한다.

## 7. I/O artifact, contract, identity와 lifecycle

### 7.1 경계별 I/O 계약

| 경계 | 입력 | 출력 | 실패 |
|---|---|---|---|
| Phase 06 → Phase 07 | Accepted `CommittedCandidate` + `ReplayManifest` + `Phase06EvidenceManifest`의 immutable projection | `CandidateVerificationRequest` | Missing accepted evidence 또는 authority/content mismatch면 시작 전 `CORRUPT` |
| Candidate verification | Same `ProblemInstance`/`PreparedTravel`/`BoundProfile`, route order, bank, declared candidate claims | `CandidatePassReport` + `VerifiedSolution` 또는 typed rejection | No partial verified solution |
| Finalization | `VerifiedSolution`, immutable request universe, approved static/search evidence | Preliminary outcomes + audit request | Candidate `PASS` 없으면 호출 불가 |
| Final audit | Fixed verified routes, every non-static-proven unassigned request, pair evaluator | Complete audit record | Missing eligible vehicle/position, overflow, interruption이면 incomplete |
| Result draft | Verified routes, final outcomes, audit, replay/provenance | `FinalResultManifest` + proposed canonical payload | Summary/fingerprint는 파생 claim일 뿐 아직 publishable 아님 |
| Result verification | Candidate `PASS`, verified solution, outcomes/audit/summary/payload | Result `PASS` + `PublishableResult` 또는 rejection | No normal route/outcome payload on failure |
| Phase 07 → Phase 08 | `Phase07Output.Publishable` 또는 `Phase07Output.Rejected` | Provider-neutral application input | Phase 07은 저장/status/CAS를 수행하지 않음 |

Actual Phase 06의 handoff는 `CommittedCandidate`, `ReplayManifest`, `Phase06EvidenceManifest` 세 부류다. 이 문서에서 세 artifact를 함께 가리킬 때는 설명용 `Phase06Handoff`라고 부르지만 새 Java type이나 wire bundle을 확정하지 않는다. Phase 07의 `CandidateVerificationRequest`는 이 세 artifact의 immutable route/bank/authority/claim/replay/evidence projection이며 solver implementation class를 import하지 않는다.

### 7.2 Authority와 비권위 claim

| 분류 | 필드 | Verifier 사용 |
|---|---|---|
| Authority | `ProblemInstance`, `PreparedTravel`, `BoundProfile`와 intrinsic fingerprints | 물리/정책 계산의 유일한 facts |
| Authority | Candidate route/node order, vehicle/terminal binding, `SearchRequestBank` | Solution structure source of truth |
| Integrity claim | Candidate/problem/travel/profile/evaluation/SolvePlan/replay/content fingerprints | Authority/content/projection과 exact 대조 |
| Integrity claim | Candidate-declared metric/score/objective/solution fingerprint | 독립 재계산 뒤 exact 대조; 계산 입력으로 사용 금지 |
| Provenance | Build/runtime, algorithm/operator/state-strategy, seeds, requested/completed work, termination, trace digest | Replay/lineage 보존; feasibility 계산 입력 아님 |
| Bounded evidence | Static precheck와 approved search-observation record | Unassigned diagnostic source 후보; confidence ceiling 적용 |
| 금지 입력 | Search propagation/insertion cache, solver feasibility flag, mutable trial, summary map, raw `ObjVal` | API parameter와 dependency에서 제거 |
| 금지 입력 | Raw input, coordinate/speed, reverse arc, `latest` profile, provider locator | Reparse/fallback 없음 |
| 금지 입력 | Candidate/request가 제공한 evaluator/comparator/callback 또는 executable extension | Audit/verifier request type에서 제거; reviewed core contract/build에 bound된 internal dependency만 사용 |

Search cache를 “무시한다”는 것은 poisoned cache와 정상 cache가 같은 verifier 결과를 내야 한다는 뜻이다. Candidate가 외부에 선언한 score/objective claim은 무시하여 통과시키는 것이 아니라, 독립 재계산 결과와 다르면 `CORRUPT(CANDIDATE_CLAIM_MISMATCH)`로 거부한다.

### 7.3 Actual Phase 06 handoff 최소 필드

| 영역 | 최소 authoritative field | Identity/검증 |
|---|---|---|
| Contract | Handoff schema/contract version | Unknown version fail-closed |
| Authority refs | Problem, prepared travel, bound profile, Phase 03 evaluation declaration와 `SolvePlan` fingerprint + content digest | Intrinsic/`BoundProfile`/candidate/replay exact equality |
| Candidate | Stable route IDs, concrete vehicle IDs, ordered node/task IDs, bank membership | Defensive immutable copy와 canonical candidate fingerprint |
| Candidate claims | Route facts/metric/score/objective/solution fingerprints | Non-authoritative; cache-free result와 비교 |
| Replay | Build/runtime compatibility, algorithm/operator/state version, config fingerprint | Exact Phase 06 manifest equality |
| Work | Requested/completed screen/worker steps, stage/round/run ordinals | No official hidden default; actual explicit values |
| Randomness | Seed derivation version, base/derived actual seeds | Replay evidence |
| Termination | Exact normal/exceptional status, last committed safe point | Normal publication eligibility와 별도 보존 |
| Trace | Stable decision trace digest, warm-start/portfolio lineage | Replay claim integrity |
| Evidence | Phase 06 bundle/review/evidence refs | Accepted predecessor 확인 |

Exceptional termination의 last committed candidate도 이 입력 형식으로 전달할 수 있으나, candidate/result 두 gate를 통과해야만 **recovery-eligible semantic result**가 된다. 그것을 정상 completion, official benchmark 또는 production publication으로 표시할지는 Phase 08/14 product contract이며 Phase 07이 이름을 바꾸지 않는다.

### 7.4 `VerifiedSolution`, outcome와 audit artifact

| Artifact | Authoritative fields | Derived/checked fields | Lifecycle |
|---|---|---|---|
| `CandidatePassReport` | Verifier contract/version, exact input identities, verdict `PASS` | Recomputed route/evaluation fingerprints | Candidate verifier만 생성; immutable |
| `VerifiedSolution` | Exact verified routes, bank-derived membership, authority identities | Route facts, metrics, scores, objective, solution fingerprint | PASS 뒤 생성; finalization input |
| `FinalOutcome.Assigned` | Request ID, route ID, input vehicle ID, pickup/delivery references, ownership kind | None from search summary | Verified route에서만 생성 |
| `FinalOutcome.Unassigned` | Request ID, no route reference, evidence-bounded diagnostic | Audit disposition/source | Verified solution에서 route 미소유 |
| `FinalInsertionAuditRecord` | Contract/version, verified solution fingerprint, bound insertion-authority/evaluator-contract fingerprint, request별 enumeration/completion/rejection/found evidence | Work counts와 semantic audit fingerprint | Final routes 고정; no mutation |
| `ResultSummary` | 없음; 모든 값이 derived | Assigned/unassigned count, used vehicles, route metrics, objective/reference components | Result verifier가 다시 계산 |

Final audit의 observed elapsed는 internal observation/evidence bundle에 기록할 수 있지만 semantic audit/result fingerprint의 hidden input이 아니다. Canonical result에는 contract/version, completion, stable work count와 semantic evidence를 넣고 wall-clock observation은 별도 observation record로 둔다.

### 7.5 Unassigned diagnostic contract

Exact Java/wire enum 이름은 proposed지만 source/scope/confidence 의미는 다음과 같이 고정한다.

| Source | 허용 confidence | 허용 external 의미 | 금지 |
|---|---|---|---|
| Normalization/static precheck | `PROVEN` | 순서/탐색과 무관하게 호환 vehicle 없음 등 증명된 code | Search 실패를 static proof로 승격 |
| Complete final insertion audit | `EXHAUSTIVE_FOR_FINAL_SOLUTION` | Fixed final routes에서 모든 eligible vehicle/position이 실패 | 전역 재배치 불가능성 또는 optimality 주장 |
| Bounded search telemetry | `OBSERVED_DURING_SEARCH` | 실제 관측된 안정된 code/scope만 | Exhaustive/proven으로 승격 |
| 독립 evidence 없음 | `UNKNOWN` | 일반 unassigned | Bank의 last failure를 reason으로 직렬화 |
| Audit feasible insertion 발견 | Internal `FEASIBLE_INSERTION_FOUND` | Internal audit record에만 보존 | Auto-fix, re-solve, `NO_FEASIBLE_INSERTION` 외부 진단 |

Proposed internal diagnostic shape:

```text
code
scope
confidence
sourceKind
sourceContractVersion
evidenceFingerprint
safe subject/reference
```

Raw address/PII, secret, mutable cache dump, stack trace와 full candidate bytes는 external diagnostic에 넣지 않는다.

### 7.6 Final result/output manifest authoritative fields

`FinalResultManifest`는 다음 필드를 가진 versioned internal contract여야 한다.

| Group | 필수 field | Authority 규칙 |
|---|---|---|
| Contract | Result contract version, canonical encoding version, fingerprint algorithm/version | Unknown/duplicate version fail-closed |
| Authority | Problem, prepared travel, bound profile, Phase 03 evaluation declaration와 `SolvePlan` fingerprints/content digests | Candidate/replay/verified solution과 exact 일치 |
| Candidate | Candidate fingerprint, replay bundle fingerprint, verified solution fingerprint | Search cache는 포함하지 않음 |
| Routes | Stable route ID, concrete vehicle, ordered task/node references, terminal meaning, recomputed route facts/metric fingerprints | Visit order는 semantic이므로 정렬로 바꾸지 않음 |
| Outcomes | 모든 request의 exactly-one `Assigned`/`Unassigned`, assigned ownership reference, evidence-bounded diagnostic | Request universe stable order |
| Audit | Contract/version, bound insertion-authority/evaluator-contract fingerprint, completion, per-request semantic disposition/counts/rejection fingerprint, audit fingerprint | Caller callback와 observation elapsed 제외 |
| Summary | Assigned/unassigned count, used vehicle count, directed distance, route operational time, bound objective/metric breakdown | Verified routes/outcomes에서만 파생 |
| Replay/provenance | Input/adapter/problem/travel/profile/build/runtime/algorithm/config/seed/step/termination/trace lineage | Provider locator/clock/log order는 semantic identity 아님 |
| Candidate verification | Candidate verifier contract/version/report fingerprint/verdict | Exact `PASS` 필수 |
| Result identity | Final result semantic fingerprint | Self field를 제외한 canonical semantic projection에서 재계산 |
| Payload integrity | Payload schema/version, exact canonical bytes digest/length | Result verifier가 독립 재인코딩해 대조 |

`ResultIntegrityVerifier`의 `PASS` report는 위 draft manifest 내부에 넣어 circular digest를 만들지 않는다. 최종 `PublishableResult` envelope가 다음을 묶는다.

```text
FinalResultManifest
+ CanonicalResultPayload
+ CandidatePassReport reference
+ ResultPassReport
+ PublishableResultEnvelopeFingerprint
```

`FinalResultFingerprint`는 semantic field를 식별하고, `PayloadDigest`는 exact bytes를 식별한다. 관측 elapsed처럼 비결정적 운영 metadata는 별도 observation/evidence artifact에 두며 semantic fingerprint 또는 canonical payload에 섞지 않는다.

### 7.7 Lifecycle과 state transition

```text
Phase06Handoff(
  CommittedCandidate
  + ReplayManifest
  + Phase06EvidenceManifest)
  ── authority/content receipt ──▶ UnverifiedCandidate
  ── mismatch ──────────────────▶ Rejected(CORRUPT)

UnverifiedCandidate
  ── independent cache-free verify ──▶ CandidatePassReport + VerifiedSolution
  ├─ normal hard rejection ──────────▶ Rejected(INFEASIBLE)
  ├─ structural/overflow defect ─────▶ Rejected(INVALID)
  └─ claim/integrity mismatch ───────▶ Rejected(CORRUPT)

VerifiedSolution
  ── preliminary partition ──▶ AuditDraft
  ── required complete audit ─▶ FinalOutcomes + FinalInsertionAuditRecord
  └─ cancellation/interruption ─▶ Rejected(GateIncomplete)
  ── derive manifest/payload ─▶ FinalResultDraft

FinalResultDraft
  ── result-integrity PASS ──▶ PublishableResult
  ├─ outcome/audit invalid ──▶ Rejected(INVALID)
  ├─ fingerprint/payload tamper ─▶ Rejected(CORRUPT)
  └─ result gate incomplete ──▶ Rejected(GateIncomplete)
```

| Lifecycle | Mutable 여부 | Owner | 다음 단계에 넘기는 것 |
|---|---:|---|---|
| Candidate receipt draft | 생성 중에만 | Candidate verifier | Validated immutable request 또는 rejection |
| Verification scratch | Call-local only | Candidate verifier internal | 아무 scratch/cache도 전달하지 않음 |
| `VerifiedSolution` | Immutable | Candidate verifier | Recomputed facts/evaluation and identity |
| Audit enumeration | Call-local only | Final insertion auditor | Complete immutable audit record |
| `FinalResultDraft` | Immutable | Finalizer | Outcomes, audit, summary, manifest/payload |
| `PublishableResult` | Immutable | Result verifier | Phase 08 application input |
| `Rejected.VerificationRejected` | Immutable | Completed semantic gate that rejected | Stage/disposition/safe verification failure only |
| `Rejected.GateIncomplete` | Immutable | Gate that could not complete | Stage/safe incomplete reason/last safe identity only |

## 8. Proposed Java 25 contract

### 8.1 Candidate verifier input와 판정

```java
package com.ronext.rpdptw.verification.api;

public interface CandidateVerifier {
    CandidateVerification verify(CandidateVerificationRequest request);
}

public record CandidateVerificationRequest(
    ProblemInstance problem,
    PreparedTravel preparedTravel,
    BoundProfile boundProfile,
    CandidateSnapshot candidate,
    CandidateDeclaredClaims declaredClaims,
    ReplayEvidence replayEvidence,
    Phase06EvidenceReceipt phase06EvidenceReceipt,
    CandidateVerificationContract contract
) {
    // Defensive immutable copy; no cache/search/solver type is accepted.
}

public enum VerificationDisposition {
    FEASIBLE,
    INFEASIBLE,
    INVALID,
    CORRUPT
}

public sealed interface CandidateVerification
        permits CandidateVerification.Pass, CandidateVerification.Rejected {

    record Pass(
        CandidatePassReport report,
        VerifiedSolution solution
    ) implements CandidateVerification {}

    record Rejected(
        VerificationDisposition disposition,
        VerificationFailure failure
    ) implements CandidateVerification {}
}
```

`CandidateSnapshot`은 Phase 06 solver class가 아니라 route/bank source of truth를 표현하는 immutable handoff projection이다.

```java
public record CandidateSnapshot(
    ProblemFingerprint problemFingerprint,
    PreparedTravelFingerprint preparedTravelFingerprint,
    BoundProfileFingerprint boundProfileFingerprint,
    EvaluationDeclarationFingerprint evaluationDeclarationFingerprint,
    SolvePlanFingerprint solvePlanFingerprint,
    List<CandidateRoute> routes,
    List<RequestId> bankRequestIds,
    CandidateFingerprint fingerprint,
    ContentDigest contentDigest
) {}

public record CandidateDeclaredClaims(
    List<RouteEvaluationClaim> routeClaims,
    SolutionEvaluationClaim solutionClaim,
    CandidateClaimFingerprint fingerprint
) {}

public record ReplayEvidence(
    InputLineage inputLineage,
    ProblemFingerprint problemFingerprint,
    PreparedTravelFingerprint preparedTravelFingerprint,
    BoundProfileFingerprint boundProfileFingerprint,
    EvaluationDeclarationFingerprint evaluationDeclarationFingerprint,
    SolvePlanFingerprint solvePlanFingerprint,
    PortfolioLineage portfolioLineage,
    BuildRuntimeFingerprint buildRuntimeFingerprint,
    AlgorithmConfigFingerprint algorithmConfigFingerprint,
    OperatorStateStrategyFingerprint operatorStateStrategyFingerprint,
    SeedLineage seedLineage,
    WorkLineage workLineage,
    TerminationRecord termination,
    LastCommittedBoundary lastCommittedBoundary,
    DecisionTraceDigest decisionTraceDigest,
    ReplayEvidenceFingerprint fingerprint
) {}

public record Phase06EvidenceReceipt(
    EvidenceRef cowEvidence,
    EvidenceRef alnsEvidence,
    EvidenceRef replayEvidence,
    ReviewRef acceptedPhase06Review,
    Phase06EvidenceReceiptFingerprint fingerprint
) {}
```

`CandidateDeclaredClaims`는 검증할 claim이지 verifier가 재사용할 evaluation artifact가 아니다. `CandidateVerificationRequest`에는 `SearchCache`, `InsertionCache`, `SolverSummary`, `CommittedCandidate` implementation, provider handle 또는 `Map<String,Object>`가 존재하지 않는다.

### 8.2 Failure hierarchy와 verified solution

```java
public sealed interface VerificationFailure
        permits AuthorityCorruption,
                CandidateCorruption,
                StructuralInvalidity,
                ArithmeticInvalidity,
                HardInfeasibility,
                ClaimCorruption,
                ContractInvalidity {
    FailureCode code();
    SafeFailureLocation location();
}

public record VerifiedSolution(
    VerificationAuthority authority,
    List<VerifiedRoute> routes,
    List<RequestId> unassignedRequestIds,
    SolutionEvaluationArtifact evaluation,
    CandidateFingerprint sourceCandidateFingerprint,
    VerifiedSolutionFingerprint fingerprint
) {}

public record CandidatePassReport(
    CandidateVerifierContractVersion contractVersion,
    VerificationAuthority authority,
    CandidateFingerprint candidateFingerprint,
    ReplayEvidenceFingerprint replayEvidenceFingerprint,
    Phase06EvidenceReceiptFingerprint phase06EvidenceReceiptFingerprint,
    VerifiedSolutionFingerprint verifiedSolutionFingerprint,
    CandidateClaimFingerprint checkedClaimFingerprint,
    VerificationDisposition disposition,
    CandidatePassReportFingerprint fingerprint
) {
    // disposition must be FEASIBLE
}
```

`HardInfeasibility`만 `INFEASIBLE`, `StructuralInvalidity`/`ArithmeticInvalidity`/`ContractInvalidity`는 `INVALID`, authority/content/claim mismatch는 `CORRUPT`로 매핑한다. Failure code 순서는 deterministic하며 hash iteration 또는 최초 thread completion에 의존하지 않는다.

### 8.3 Final outcomes, audit와 diagnostic

```java
package com.ronext.rpdptw.result.api;

public sealed interface FinalOutcome
        permits FinalOutcome.Assigned, FinalOutcome.Unassigned {

    RequestId requestId();

    record Assigned(
        RequestId requestId,
        RouteId routeId,
        VehicleId vehicleId,
        RequestAssignmentEvidence assignmentEvidence,
        VehicleOwnership ownership
    ) implements FinalOutcome {}

    record Unassigned(
        RequestId requestId,
        UnassignedDiagnostic diagnostic
    ) implements FinalOutcome {}
}

public sealed interface RequestAssignmentEvidence
        permits RequestAssignmentEvidence.RealPickupDelivery,
                RequestAssignmentEvidence.DeliveryOnly {

    record RealPickupDelivery(
        SolverNodeId pickupNodeId,
        SolverNodeId deliveryNodeId
    ) implements RequestAssignmentEvidence {}

    record DeliveryOnly(
        SolverNodeId deliveryNodeId,
        InitialLoadOwnershipFingerprint initialLoadOwnershipFingerprint
    ) implements RequestAssignmentEvidence {}
}

public enum DiagnosticConfidence {
    PROVEN,
    EXHAUSTIVE_FOR_FINAL_SOLUTION,
    OBSERVED_DURING_SEARCH,
    UNKNOWN
}

public record UnassignedDiagnostic(
    DiagnosticCode code,
    DiagnosticScope scope,
    DiagnosticConfidence confidence,
    DiagnosticSource source,
    EvidenceFingerprint evidenceFingerprint
) {}
```

Delivery-only request의 logical pickup representation은 Phase 02/03/04 contract를 따른다. 위 `RequestAssignmentEvidence` 이름과 exact field는 proposed/open이지만 real pair와 delivery-only initial-load ownership을 구분하고 가짜 customer visit을 만들 수 없다는 의미는 고정한다.

```java
package com.ronext.rpdptw.result.finalization;

public interface FinalInsertionAuditor {
    FinalInsertionAuditResult audit(FinalInsertionAuditRequest request);
}

public sealed interface FinalInsertionAuditResult
        permits FinalInsertionAuditResult.Completed,
                FinalInsertionAuditResult.Incomplete,
                FinalInsertionAuditResult.Invalid {

    record Completed(FinalInsertionAuditRecord record)
        implements FinalInsertionAuditResult {}
    record Incomplete(AuditInterruption interruption)
        implements FinalInsertionAuditResult {}
    record Invalid(VerificationFailure failure)
        implements FinalInsertionAuditResult {}
}

public record FinalInsertionAuditRequest(
    VerifiedSolution verifiedSolution,
    List<RequestId> preliminaryUnassigned,
    StaticUnassignabilityEvidence staticEvidence,
    BoundInsertionAuthority insertionAuthority,
    AuditContractVersion contractVersion
) {}
```

`BoundInsertionAuthority`는 Phase 05의 accepted side-effect-free exact `AtomicPairInsertionEvaluator` contract/build/declaration identity를 가리키는 immutable data projection이다. Evaluator implementation은 reviewed composition root가 auditor internal constructor에 bind하고 candidate/facade request가 callback 객체를 주입하지 못하게 한다. Auditor는 route를 복제·commit하지 않으며 모든 option result를 count/evidence로만 기록한다.

```java
public enum AuditDisposition {
    STATIC_PROVEN,
    EXHAUSTED_NO_FEASIBLE_INSERTION,
    FEASIBLE_INSERTION_FOUND
}

public record RequestAuditRecord(
    RequestId requestId,
    AuditDisposition disposition,
    long eligibleVehicleCount,
    long legalPositionPairCount,
    long evaluatedOptionCount,
    SortedMap<ConstraintCode, Long> rejectionCounts,
    Optional<InsertionWitness> internalFeasibleWitness,
    RequestAuditFingerprint fingerprint
) {}

public record FinalInsertionAuditRecord(
    AuditContractVersion contractVersion,
    VerifiedSolutionFingerprint verifiedSolutionFingerprint,
    BoundInsertionAuthorityFingerprint insertionAuthorityFingerprint,
    List<RequestAuditRecord> requestRecords,
    boolean complete,
    AuditFingerprint fingerprint
) {}
```

`internalFeasibleWitness`는 external payload에 직접 노출하지 않는다. 모든 count는 checked `long`이며 `legalPositionPairCount`와 실제 enumeration count가 다르면 `INVALID(AUDIT_ENUMERATION_MISMATCH)`다.

### 8.4 Final result와 result-integrity verifier

```java
public interface FinalResultFinalizer {
    FinalResultDraft build(
        CandidatePassReport candidatePass,
        VerifiedSolution verifiedSolution,
        FinalInsertionAuditRecord audit,
        ReplayEvidence replayEvidence
    );
}

public record ResultSummary(
    long assignedRequestCount,
    long unassignedRequestCount,
    long dispatchedVehicleCount,
    long totalDirectedDistanceMeters,
    long totalRouteOperationalTimeSeconds,
    MetricSnapshot metrics,
    ScoreSnapshot scores,
    ObjectiveVector objective
) {}

public record FinalResultManifest(
    ResultContractVersion resultContractVersion,
    CanonicalEncodingVersion encodingVersion,
    VerificationAuthority authority,
    CandidateFingerprint sourceCandidateFingerprint,
    ReplayEvidence replayEvidence,
    VerifiedSolution verifiedSolution,
    List<FinalOutcome> outcomes,
    FinalInsertionAuditRecord audit,
    ResultSummary summary,
    CandidatePassReport candidatePassReport,
    FinalResultFingerprint fingerprint
) {}

public interface CanonicalResultEncoder {
    CanonicalResultPayload encode(FinalResultManifest manifest);
}

public record CanonicalResultPayload(
    CanonicalEncodingVersion encodingVersion,
    CanonicalBytes canonicalBytes,
    long contentLength,
    ContentDigest payloadDigest
) {}

public interface CanonicalBytes {
    int size();
    byte byteAt(int index);
    byte[] copy();
}
```

Internal implementation은 constructor에서 원본을 복사하고 `copy()`도 새 배열을 반환한다. Raw `byte[]` accessor나 mutable `ByteBuffer` view를 노출하지 않는다.

Exact external JSON schema와 media type은 Phase 07이 승인하지 않는다. Internal encoder는 stable version, ordered list/map, duplicate-key rejection, explicit absent representation, integer-only value와 length-delimited canonical projection을 지켜야 한다.

```java
package com.ronext.rpdptw.verification.result;

public interface ResultIntegrityVerifier {
    ResultVerification verify(ResultVerificationRequest request);
}

public record ResultVerificationRequest(
    CandidatePassReport candidatePass,
    VerifiedSolution verifiedSolution,
    FinalResultManifest manifest,
    CanonicalResultPayload payload,
    ResultVerifierContract contract
) {}

public sealed interface ResultVerification
        permits ResultVerification.Pass, ResultVerification.Rejected {

    record Pass(ResultPassReport report)
        implements ResultVerification {}

    record Rejected(
        VerificationDisposition disposition,
        VerificationFailure failure
    ) implements ResultVerification {}
}
```

Result stage의 정상 pass는 `FEASIBLE` candidate를 전제로 한다. Result stage에서 outcome/audit/summary 계약 위반은 `INVALID`, fingerprint/bytes/claim mismatch는 `CORRUPT`다. Result stage가 새로운 `INFEASIBLE` solution을 정상 결과로 만들 수 없다.

```java
public record PublishableResult(
    FinalResultManifest manifest,
    CanonicalResultPayload payload,
    CandidatePassReport candidatePass,
    ResultPassReport resultPass,
    PublishableResultFingerprint fingerprint
) {}

public sealed interface Phase07Output
        permits Phase07Output.Publishable, Phase07Output.Rejected {

    record Publishable(PublishableResult result)
        implements Phase07Output {}

    record Rejected(FinalResultRejection rejection)
        implements Phase07Output {}
}

public sealed interface FinalResultRejection
        permits FinalResultRejection.VerificationRejected,
                FinalResultRejection.GateIncomplete {

    RejectionStage stage();

    record VerificationRejected(
        RejectionStage stage,
        VerificationDisposition disposition,
        VerificationFailure failure,
        Optional<CandidatePassReportFingerprint> candidatePassFingerprint
    ) implements FinalResultRejection {}

    record GateIncomplete(
        RejectionStage stage,
        SafeIncompleteFailure incompleteFailure,
        Optional<CandidatePassReportFingerprint> candidatePassFingerprint,
        LastSafeIdentity lastSafeIdentity
    ) implements FinalResultRejection {}
}
```

`VerificationRejected`의 disposition은 completed semantic 판정인 `INFEASIBLE`, `INVALID` 또는 `CORRUPT`다. `GateIncomplete`에는 verification disposition을 합성하지 않는다. 두 `Rejected` variant 모두 정상 route/outcome/benchmark payload를 넣지 않고 safe identifiers와 bounded failure evidence만 포함하며 raw PII와 mutable state를 제외한다.

### 8.5 Phase 07 facade

```java
public interface Phase07FinalResultService {
    Phase07Output finalizeResult(Phase07Request request);
}

public record Phase07Request(
    CandidateVerificationRequest candidateRequest,
    StaticUnassignabilityEvidence staticEvidence,
    ResultContractVersion resultContractVersion,
    CanonicalEncodingVersion encodingVersion
) {}
```

Facade는 pure orchestration이다. `Clock`, `Path`, URI, S3/GCS client, transaction, repository, status state machine, thread pool 또는 network를 소유하지 않는다. Audit cancellation/interruption을 explicit input/token으로 받는 방안은 Phase 08 integration review 대상이며 incomplete를 normal result로 바꾸지 않는다.

## 9. Pseudocode와 상태 전이

### 9.1 Candidate verifier

```text
verify(request):
  require non-null immutable contract/version

  receipt = verifyContentAndAuthority(
      request.problem,
      request.preparedTravel,
      request.boundProfile,
      request.candidate,
      request.replayEvidence,
      request.phase06EvidenceReceipt)

  require exact equality for:
      problem/travel/profile/evaluation-declaration/solve-plan refs
      candidate projection digest
      accepted Phase 06 evidence/review refs

  if receipt mismatch:
      return Rejected(CORRUPT, receipt.failure)

  structural = independentlyCheckRequestPartition(
      problem.requestUniverse,
      candidate.routes,
      candidate.bank)

  if structural invalid:
      return Rejected(INVALID, structural.failure)

  verifiedRoutes = []
  for route in stableRouteIdOrder(candidate.routes):
      evaluation = cacheFreeKernel.evaluate(
          problem,
          preparedTravel,
          route.routePlan,
          boundProfile.propagationDeclaration,
          boundProfile.evaluationPlan)

      if evaluation is Infeasible:
          return Rejected(INFEASIBLE, exact first stable rejection)

      if evaluation is Invalid:
          return Rejected(INVALID, evaluation.failure)

      verifiedRoutes.add(evaluation.artifact)

  solutionEvaluation = checkedFullSolutionRecompute(verifiedRoutes, bank)
  if overflow/contract invalid:
      return Rejected(INVALID, failure)

  claimComparison = compareOnlyAfterRecompute(
      request.declaredClaims,
      verifiedRoutes,
      solutionEvaluation)

  if claimComparison differs:
      return Rejected(CORRUPT, CANDIDATE_CLAIM_MISMATCH)

  solution = immutableVerifiedSolution(verifiedRoutes, bank, identities)
  report = candidatePassReport(solution, checked claims, contract)
  return Pass(report, solution)
```

Verifier는 search claim과 cache를 “빠른 경로”로 사용하지 않는다. Candidate-declared claim이 비어 있거나 schema가 다르면 명시된 contract에 따라 `INVALID` 또는 `CORRUPT`이며 누락값을 재계산 결과로 조용히 채워 원본 claim을 정당화하지 않는다.

### 9.2 Preliminary outcomes와 final audit

```text
preliminaryOutcomes(verifiedSolution):
  for request in problem.requestUniverse stable order:
    if exactly one verified route owns complete request:
      Assigned(exact route/vehicle/pair refs)
    else:
      Unassigned(UNKNOWN placeholder for internal draft only)

audit(verifiedSolution, preliminaryUnassigned, staticEvidence):
  assert verifiedSolution fingerprint exact
  assert bound insertion authority/evaluator contract/build identity exact
  reject any caller-provided evaluator/callback shape before enumeration

  for request in stable request order:
    if staticEvidence independently proves unassignable:
      record STATIC_PROVEN
      continue

    eligibleVehicles = exact eligible concrete vehicles
    legalPositionPairs = exact pickup/delivery position enumeration
    counts = checked zero
    witness = empty

    for vehicle in stable vehicle order:
      for positionPair in stable legal order:
        counts.incrementChecked()
        result = exact side-effect-free pair evaluator(
            fixed final routes,
            request,
            vehicle,
            positionPair)

        if result feasible:
          keep first stable internal witness
        else:
          increment exact rejection code

    assert enumerated count == declared legal count

    if witness present:
      record FEASIBLE_INSERTION_FOUND
      # no route/bank/outcome mutation
    else:
      record EXHAUSTED_NO_FEASIBLE_INSERTION

  require all required requests completed
  return immutable audit
```

Feasible witness가 있어도 모든 required option을 끝까지 검사해야 하는지 여부는 audit work/cost review에서 **proposed**다. 최소 고정 계약은 `EXHAUSTIVE_FOR_FINAL_SOLUTION`을 주장할 때 전수 완료가 필요하고, feasible 발견 후 조기 종료하면 그 request는 external exhaustive confidence를 받을 수 없다는 것이다. Deterministic full audit baseline을 먼저 구현하며 성능 최적화는 별도 measured proposal로 둔다.

### 9.3 Final result와 integrity verifier

```text
finalize(candidatePass, verifiedSolution, audit, replay):
  require candidatePass references verifiedSolution exactly
  require audit complete and references same verifiedSolution

  outcomes = deriveExactlyOneOutcomes(verifiedSolution, audit, evidence)
  summary = checkedDeriveSummary(verifiedSolution, outcomes)
  manifest = canonicalSemanticManifest(
      authority,
      source candidate/replay,
      verified solution,
      outcomes,
      audit semantic fields,
      summary,
      candidate pass)
  payload = canonicalEncoder.encode(manifest)
  return FinalResultDraft(manifest, payload)

resultVerify(draft):
  verify candidate PASS and solution identity
  recompute request universe ↔ outcome exactly-one
  recompute every assigned route/vehicle/pair reference
  verify every unassigned diagnostic source/confidence ceiling
  verify audit required set, counts, completion and fingerprint
  independently derive summary from verified solution/outcomes
  recompute semantic result fingerprint
  independently canonicalize and compare exact payload bytes/digest

  if semantic contract invalid:
      return Rejected(INVALID)
  if identity/claim/bytes differ:
      return Rejected(CORRUPT)
  if required result-gate work cannot complete:
      return Rejected(GateIncomplete, exact last safe identity)
  return Pass(ResultPassReport)
```

Result verifier가 finalizer의 `summary()` helper나 encoder output digest를 그대로 신뢰해서는 안 된다. Production code sharing이 불가피한 primitive hash/value type은 사용할 수 있지만 결과 derivation, outcome enumeration과 canonical field traversal은 별도 경로로 작성하고 §10 independent oracle로 교차 검증한다.

## 10. Independent oracle과 exact future-red test plan

### 10.1 Oracle 독립성

Phase 07 expected result는 search가 만든 정상 candidate를 그대로 복사하지 않는다. Test oracle은 다음 규칙을 지킨다.

- `ReferenceCandidateOracle`은 production `RouteEvaluationKernel`, verifier, candidate cache와 claim comparator를 호출하지 않는다.
- Tiny fixture의 directed travel table과 service/load/window rule을 별도 immutable tuple로 읽고 `BigInteger` checked reference arithmetic을 사용한다.
- `ReferenceInsertionOracle`은 모든 eligible vehicle과 legal pickup/delivery position pair를 중첩 반복으로 직접 열거한다.
- `ReferenceResultOracle`은 request universe와 verified route ownership에서 outcome/summary를 독립 계산한다.
- `ReferenceCanonicalEncodingOracle`은 production encoder를 호출하지 않고 explicit field order와 length-prefix를 적용하거나 승인된 expected UTF-8/byte literal을 사용한다.
- Fixture builder와 production builder는 같은 mutable list/map instance를 공유하지 않는다.
- Oracle implementation과 expected bytes의 source digest를 evidence bundle에 기록한다.
- Production helper를 oracle 안에서 발견하면 해당 test/evidence를 폐기하고 red 상태로 되돌린다.
- `Phase07OracleIndependenceTest`가 test-jar source/bytecode graph에서 production verifier/evaluator/finalizer/encoder reference 0을 검사한다.
- Compile failure만으로 oracle 검출력을 주장하지 않는다. `Phase07OracleSensitivityTest`가 cache 신뢰, pair split 누락, checked-overflow wrap, finalizer summary 재사용과 unordered canonical traversal의 explicit faulty double을 각 oracle assertion으로 red로 만들고 최소 counterexample/ordinal을 보존해야 한다.

### 10.2 Test-only fixtures와 builders

| Fixture/builder | 용도 | 제약 |
|---|---|---|
| `P07_TINY_PD_3_TEST_ONLY` | Real pair, delivery-only, assigned/unassigned, DIRECT/LEASE를 포함한 작은 integer authority | Official/approved fixture 아님; 모든 값 explicit |
| `Phase07AuthorityFixtureBuilder` | Same immutable problem/travel/profile와 exact refs 구성 | Raw input/parser/provider 호출 금지 |
| `CandidateArtifactBuilder` | Baseline feasible routes/bank/claims/replay | Stable order와 defensive immutable copy |
| `CorruptedCandidateBuilder` | One-field fingerprint/bytes/claim/partition mutation | 각 test는 한 primary defect만 주입 |
| `MaliciousCandidateBuilder` | Extreme `long`, forged ID, aliased mutable list, unknown tag/field projection | Executable callback/reflection/script 없음 |
| `AuditScenarioBuilder` | Static proven, exhaustive fail, feasible witness cases | Fixed final routes와 exhaustive option count 제공 |
| `FinalResultDraftBuilder` | Outcome/audit/summary/payload one-field tamper | Candidate PASS와 verified solution은 별도 refs |
| `PermutationResultBuilder` | Map/set/source order, locale/timezone/thread permutation | Semantic visit order는 변경하지 않음 |

모든 수치, seed와 work count는 test method/config에서 `test-only`로 명시한다. `Q-BENCH-02` official default나 current decimal Win fixture를 사용하지 않는다.

### 10.3 Candidate authority, corruption와 malicious input test

| Exact test | Fixture/builder | Independent oracle | Expected |
|---|---|---|---|
| `CandidateVerifierAuthorityTest.passesOnlyWhenCandidateReplayProblemTravelAndProfileFingerprintsMatch()` | Baseline authority builder | Explicit fingerprint tuple | `Pass`, all refs exact |
| `CandidateVerifierAuthorityTest.rejectsProblemFingerprintMismatchAsCorrupt()` | One-field problem ref mutation | Expected authority literal | `CORRUPT(AUTHORITY_MISMATCH)` before evaluation |
| `CandidateVerifierAuthorityTest.rejectsPreparedTravelFingerprintMismatchAsCorrupt()` | Swapped travel ref | Prepared travel tuple oracle | `CORRUPT`, lookup 0 |
| `CandidateVerifierAuthorityTest.rejectsBoundProfileFingerprintMismatchAsCorrupt()` | Different profile ref | Profile declaration tuple | `CORRUPT`, fallback 0 |
| `CandidateVerifierAuthorityTest.rejectsEvaluationDeclarationOrSolvePlanFingerprintMismatchAsCorrupt()` | Candidate/replay의 declaration/plan one-field mutation | Bound profile snapshot tuple | `CORRUPT`, route evaluation 0 |
| `CandidateVerifierAuthorityTest.rejectsSameIdentityDifferentCanonicalBytesAsCorrupt()` | Content bytes mutation, unchanged ID | Independent content digest | `CORRUPT(CONTENT_DIGEST_MISMATCH)` |
| `CandidateVerifierAuthorityTest.candidateProjectionDigestCoversContractAuthoritiesRoutesBankAndDeclaredClaims()` | Projection field one-at-a-time mutation | Explicit inclusion manifest | Undetected mutation 0 |
| `CandidateVerifierIndependenceTest.requestContractContainsNoSearchCacheOrSolverSummaryType()` | Reflection/bytecode contract inspection | Forbidden-type list | Forbidden field/dependency 0 |
| `CandidateVerifierIndependenceTest.poisonedSearchCachesCannotChangeRecomputedVerdict()` | Same route/bank, two external poison holders | Reference candidate oracle | Both independent results exact equal |
| `CandidateClaimTamperingTest.rejectsTamperedMetricClaimAsCorrupt()` | Metric +1, route unchanged | Hand metric oracle | `CORRUPT(CANDIDATE_CLAIM_MISMATCH)` |
| `CandidateClaimTamperingTest.rejectsTamperedScoreClaimAsCorrupt()` | Score one-field mutation | Hand score oracle | `CORRUPT` |
| `CandidateClaimTamperingTest.rejectsTamperedObjectiveOrSolutionFingerprintAsCorrupt()` | Objective/fingerprint mutation | Reference result tuple | `CORRUPT` |
| `CandidateArtifactImmutabilityTest.defensivelyCopiesAdversarialRouteAndBankCollections()` | Mutable alias modified after construction | Baseline candidate bytes | Original canonical bytes/fingerprint unchanged |
| `CandidateArtifactImmutabilityTest.rejectsUnknownOrExecutableExtensionShape()` | Malicious unknown extension fixture | Allowed sealed-type set | `INVALID(CONTRACT_TYPE_NOT_ALLOWED)`; no callback invoked |

### 10.4 Partition, task/pair, route와 overflow test

| Exact test | Corruption | Oracle | Expected |
|---|---|---|---|
| `CandidatePartitionCorruptionTest.rejectsDuplicatedPickupTaskInOneRouteAsInvalid()` | Pickup visit duplicated | Request visit multiset oracle | `INVALID(DUPLICATE_TASK)` |
| `CandidatePartitionCorruptionTest.rejectsMissingDeliveryTaskAsInvalid()` | Delivery visit removed | Pair completeness oracle | `INVALID(MISSING_TASK)` |
| `CandidatePartitionCorruptionTest.rejectsPairSplitAcrossTwoVehicleRoutesAsInvalid()` | Pickup/delivery on different routes | Request owner oracle | `INVALID(PAIR_SPLIT)` |
| `CandidatePartitionCorruptionTest.rejectsDeliveryBeforePickupAsInvalid()` | Semantic order swapped | Ordered pair oracle | `INVALID(PRECEDENCE)` |
| `CandidatePartitionCorruptionTest.rejectsRouteAndBankDuplicateAsInvalid()` | Request in route and bank | XOR oracle | `INVALID(ROUTE_BANK_DUPLICATE)` |
| `CandidatePartitionCorruptionTest.rejectsRequestMissingFromRoutesAndBankAsInvalid()` | Request omitted everywhere | Universe partition oracle | `INVALID(REQUEST_OMISSION)` |
| `CandidatePartitionCorruptionTest.rejectsWholePairDuplicatedAcrossRoutesAsInvalid()` | Same pair twice | Exactly-once oracle | `INVALID(DUPLICATE_REQUEST)` |
| `CandidatePartitionCorruptionTest.rejectsUnknownTaskRequestOrVehicleIdAsInvalid()` | Forged dense/external ID | Authority ID set | `INVALID(UNKNOWN_REFERENCE)` |
| `CandidateRouteFeasibilityTest.classifiesCapacityTimeWindowAndResourceViolationsAsInfeasible()` | Three well-formed route variants | BigInteger hand propagation | Each `INFEASIBLE`, never unassigned |
| `CandidateRouteFeasibilityTest.classifiesWrongTerminalOrServicePatternAsInvalid()` | Structurally wrong policy | Terminal/pattern oracle | `INVALID` |
| `CandidateRouteFeasibilityTest.usesDirectedPreparedTravelWithoutReverseOrLazyFallback()` | Asymmetric arc; reverse absent in candidate path | Explicit directed table | Exact direction; fallback calls 0 |
| `CandidateArithmeticCorruptionTest.rejectsRouteMetricAndObjectiveOverflowAsInvalid()` | Near-`Long.MAX_VALUE` values | `BigInteger` reference | `INVALID(ARITHMETIC_OVERFLOW)` |
| `CandidateArithmeticCorruptionTest.neverWrapsClampsOrConvertsOverflowToUnassigned()` | Overflow variants | BigInteger range oracle | No `Pass`, no `UNASSIGNED` conversion |

### 10.5 Final audit, outcome와 diagnostic test

| Exact test | Fixture | Oracle | Expected |
|---|---|---|---|
| `FinalInsertionAuditTest.skipsOnlyRequestsWithIndependentStaticProvenEvidence()` | One static-proven, two ordinary unassigned | Required-audit set oracle | Only proven skipped |
| `FinalInsertionAuditAuthorityTest.requestContainsNoCallerProvidedEvaluatorComparatorOrCallback()` | Public signature/bytecode inspection | Forbidden executable-type list | Forbidden request field 0 |
| `FinalInsertionAuditAuthorityTest.rejectsInsertionAuthorityOrEvaluatorContractIdentityMismatchBeforeEnumeration()` | Authority/build/declaration one-field mutation | Accepted Phase 05 authority tuple | `CORRUPT` or typed contract `INVALID`; evaluator calls 0 |
| `FinalInsertionAuditTest.enumeratesEveryEligibleVehicleAndLegalPickupDeliveryPositionPair()` | Tiny two-vehicle routes | Exhaustive nested-loop oracle | Counts/fingerprint exact |
| `FinalInsertionAuditTest.usesExhaustiveForFinalSolutionOnlyWhenEveryOptionFails()` | All options rejected | Reference insertion oracle | Exact confidence/scope |
| `FinalInsertionAuditTest.recordsFeasibleInsertionWithoutMutatingRoutesOrOutcome()` | One feasible option | Pre/post fingerprint + oracle | Witness internal; route/outcome unchanged |
| `FinalInsertionAuditTest.doesNotClaimGlobalInfeasibilityFromFixedRouteAudit()` | Exhaustive final-route failure | Explicit confidence ceiling | No global/proven label |
| `FinalInsertionAuditTest.doesNotPromoteSearchFailureToProvenOrExhaustive()` | Bounded search telemetry only | Source/confidence matrix | `OBSERVED` or `UNKNOWN` |
| `FinalInsertionAuditCorruptionTest.rejectsMissingEligibleVehicleOrPositionAsInvalid()` | Enumeration omission | Exhaustive option set | `INVALID(AUDIT_INCOMPLETE)` |
| `FinalInsertionAuditCorruptionTest.rejectsAuditCountOverflowAsInvalid()` | Count overflow builder | BigInteger count | `INVALID(ARITHMETIC_OVERFLOW)` |
| `FinalOutcomeTest.assignsDirectAndLeaseRequestsAsAssigned()` | DIRECT/LEASE verified routes | Ownership oracle | Both `ASSIGNED` |
| `FinalOutcomeTest.createsExactlyOneOutcomeForEveryInputRequest()` | Baseline | Universe/outcome bijection oracle | Exact one-to-one |
| `FinalOutcomeTest.unassignedNeverContainsRouteOrVehicleReference()` | Baseline | Outcome schema oracle | No ownership ref |
| `FinalOutcomeTest.neverCreatesOutsourcedOrDeferredSolverOutcome()` | LEASE + unassigned | Allowed sealed-type set | Only two outcomes |

### 10.6 Result tampering와 deterministic serialization test

| Exact test | Tamper/permutation | Oracle | Expected |
|---|---|---|---|
| `ResultIntegrityVerifierTest.rejectsMissingOrDuplicatedOutcomeAsInvalid()` | Remove/duplicate request outcome | Reference result oracle | `INVALID(OUTCOME_PARTITION)` |
| `ResultIntegrityVerifierTest.rejectsAssignedRouteVehicleOrPairReferenceMismatchAsInvalid()` | One ownership ref changed | Verified route lookup oracle | `INVALID(OWNERSHIP_REFERENCE)` |
| `ResultIntegrityVerifierTest.rejectsIncompleteAuditAsInvalid()` | Required request record removed | Required-audit set | `INVALID(AUDIT_INCOMPLETE)` |
| `ResultIntegrityVerifierTest.rejectsOverstatedDiagnosticConfidenceAsInvalid()` | `OBSERVED` → `PROVEN` | Source/confidence matrix | `INVALID(CONFIDENCE_OVERCLAIM)` |
| `ResultIntegrityVerifierTest.rejectsTamperedSummaryAsCorrupt()` | Count/distance/time/objective +1 | Independent summary oracle | `CORRUPT(RESULT_DERIVATION_MISMATCH)` |
| `ResultIntegrityVerifierTest.rejectsTamperedResultFingerprintAsCorrupt()` | Self fingerprint one-field mutation | Independent semantic digest | `CORRUPT` |
| `ResultIntegrityVerifierTest.rejectsTamperedPayloadBytesLengthOrDigestAsCorrupt()` | One byte/length/digest mutation | Independent encoder/digest | `CORRUPT(PAYLOAD_INTEGRITY)` |
| `ResultIntegrityVerifierTest.rejectsMissingCandidatePassEvenWhenOutcomesAreComplete()` | PASS ref removed | Gate state oracle | Rejected; publication blocked |
| `BothGatePublicationTest.candidatePassCannotCoverResultFailure()` | Valid candidate, corrupt payload | Gate state oracle | `Rejected(RESULT_GATE)` |
| `BothGatePublicationTest.resultShapeCannotCoverCandidateFailure()` | Complete result draft, invalid candidate | Gate state oracle | Finalization not invoked |
| `BothGatePublicationTest.rejectedVariantCannotCarryFeasibleDisposition()` | `VerificationRejected(FEASIBLE, ...)` constructor corruption | Allowed disposition set | Construction/verification fail-closed |
| `BothGatePublicationTest.auditOrResultInterruptionProducesGateIncompleteWithoutVerificationDisposition()` | Interruption before complete audit/result report | Gate lifecycle oracle | Typed `GateIncomplete`; no fabricated `INVALID/INFEASIBLE/CORRUPT` |
| `BothGatePublicationTest.failureNeverCarriesNormalRouteOutcomeOrBenchmarkPayload()` | 모든 verification rejection과 gate-incomplete variant | Sealed output inspection | Safe rejection only |
| `CanonicalResultEncodingTest.sameSemanticResultProducesIdenticalBytesAcrossMapAndSourceInsertionOrders()` | Seeded permutations | Independent canonical encoder | Exact byte equality |
| `CanonicalResultEncodingTest.sameResultProducesIdenticalBytesAcrossLocaleTimezoneAndParallelCalls()` | Locale/timezone/parallel permutation | Expected byte literal | Exact byte/digest equality |
| `CanonicalResultEncodingTest.preservesSemanticVisitOrderWhileSortingOnlyUnorderedCollections()` | Visit swap vs map order swap | Encoding oracle | Visit swap changes digest; map order does not |
| `CanonicalResultEncodingTest.rejectsDuplicateKeysUnknownFieldsAndNonCanonicalAbsentValues()` | Encoding corruption set | Canonical grammar oracle | Fail-closed |
| `CanonicalResultEncodingTest.everyAuthoritativeFieldMutationChangesSemanticFingerprintOrPayloadDigest()` | One-field mutation generator | Field coverage manifest | Undetected mutation 0 |
| `CanonicalResultEncodingTest.observationElapsedDoesNotChangeSemanticResultFingerprint()` | Elapsed-only variants | Semantic inclusion manifest | Same result fingerprint |

### 10.7 Architecture, property와 replay test

| Exact test | 목적 | 통과 기준 |
|---|---|---|
| `Phase07VerificationArchitectureTest.verificationDependsOnCoreButNotSolverSearchOrCache()` | Module independence | Forbidden compile/bytecode edge 0 |
| `Phase07VerificationArchitectureTest.verificationContainsNoCloudProviderVendorOrCustomerBranch()` | Stable semantic boundary | Forbidden reference/branch 0 |
| `Phase07OracleIndependenceTest.referenceOraclesHaveNoProductionVerifierEvaluatorFinalizerOrEncoderDependency()` | Test-jar source/bytecode graph | Forbidden production-helper reference 0 |
| `Phase07OracleSensitivityTest.rejectsSeededCacheTrustPairSplitWrapSummaryReuseAndNoncanonicalOrderingDefects()` | Explicit faulty test doubles | 각 defect가 assertion red; minimal counterexample/ordinal 보존 |
| `CandidatePartitionPropertyTest.acceptsOnlyExactRouteBankPartition()` | Generated route/bank mutations | Counterexample 0; shrink record |
| `FinalInsertionAuditCompletenessPropertyTest.matchesIndependentExhaustiveEnumerator()` | Generated small routes/positions | Option set/count/rejection exact |
| `ResultOutcomePropertyTest.outcomesFormBijectionWithRequestUniverse()` | Generated verified solutions | Bijection always |
| `Phase07ReplayReproducibilityTest.sameFixedEnvelopeProducesSameVerifiedSolutionOutcomeAndResultFingerprint()` | Phase 06 replay envelope | Trace/result identity exact |
| `Phase07ReplayReproducibilityTest.changedAuthorityOrReplayFieldChangesCoveredFingerprint()` | One-field lineage mutation | Coverage gap 0 |

### 10.8 Red → green 순서와 applicable layer

모든 test는 현재 **future red**다. Source file과 target module이 없으므로 실행됐거나 green이라고 주장하지 않는다.

| 순서 | 먼저 red로 고정할 것 | 최소 green 조건 | Layer |
|---:|---|---|---|
| 1 | Architecture forbidden dependency와 request type contract | Solver/cache/provider edge 0 | Architecture |
| 2 | Authority/evaluation/SolvePlan/content/projection fingerprint mismatch | Evaluation 전 fail-closed | Contract/corruption |
| 3 | Duplicate/missing/split/XOR/unknown task | Exact `INVALID` code | Unit/property |
| 4 | Independent route feasibility/overflow | `INFEASIBLE`와 `INVALID` 구분 | Hand oracle/boundary |
| 5 | Claim tampering/cache independence | Recompute exact, mismatch `CORRUPT` | Corruption/equivalence |
| 6 | Candidate PASS/VerifiedSolution lifecycle | PASS 외 solution 생성 0 | Module contract |
| 7 | Static proof/bound evaluator authority/final exhaustive audit | Caller callback 0, required set와 option count exact | Exhaustive oracle |
| 8 | Outcomes/diagnostic confidence | Request bijection과 ceiling exact | Unit/property |
| 9 | Result manifest/summary | Independent derivation exact | Oracle |
| 10 | Canonical bytes/tampering/nondeterminism | Repeated/parallel exact bytes | Repro/corruption |
| 11 | Both-gate facade와 Phase 08 handoff | Publishable only after both PASS | Module contract |
| 12 | Independent-oracle sensitivity와 full module/reactor/evidence/review | Seeded defect detection, required failure/skipped 0 | Integration/review |

## 11. Ordered work packages

### WP-07.0 — Entry receipt, authority와 contract freeze

- **Precondition:** Phase 00~06 accepted bundle/review identities가 전달되고 independent oracle/result encoding reviewer가 지정됨.
- **Change target:** Phase 07 API skeleton, authority/equality receipt, architecture rule와 test-only fixture manifest.
- **Concrete tasks:** Phase 06 handoff field mapping, same immutable problem/travel/profile/evaluation declaration/SolvePlan equality와 full candidate projection digest, failure/incomplete classification, canonical inclusion/exclusion, public/non-public 상태를 review로 고정한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
    -Dtest=Phase07VerificationArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
    -Dtest=CandidateVerifierAuthorityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** §10.3의 problem/travel/profile/evaluation/SolvePlan/projection authority methods와 §10.7 architecture methods green; selected test discovery count가 manifest와 exact 일치하고 skipped required test 0.
- **Failure/rollback:** Upstream bundle이 solver class/cache를 요구하거나 authority identity가 불완전하면 code freeze를 중단한다. Test/contract draft만 보존하고 accepted Phase 06 artifact를 변경하지 않는다.
- **Handoff:** Approved `CandidateVerificationRequest`, disposition/failure taxonomy와 field inclusion manifest를 WP-07.1에 전달.

### WP-07.1 — Candidate structure와 cache-free verifier

- **Precondition:** WP-07.0 green, Phase 03 kernel/Phase 04 bound profile exact contract accepted.
- **Change target:** `verification.api`, `verification.candidate`, authority/partition/claim validators와 candidate tests.
- **Concrete tasks:** Defensive immutable request, route/bank exact partition, stable failure order, cache-free per-route/solution recomputation, claim parity, PASS-only `VerifiedSolution`을 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
    -Dtest=CandidateVerifierAuthorityTest,CandidateVerifierIndependenceTest,CandidatePartitionCorruptionTest,CandidateRouteFeasibilityTest,CandidateArithmeticCorruptionTest,CandidateClaimTamperingTest,CandidateArtifactImmutabilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Fingerprint mismatch/duplicate/missing/split/infeasible/overflow/tampered claim이 exact disposition으로 거부되고, poisoned cache variant와 baseline recomputation이 exact 일치한다.
- **Failure/rollback:** Search helper/cache/solver summary 없이 구현할 수 없거나 corruption이 `UNASSIGNED`/low score가 되면 WP를 revert한다. WP-07.0 contract와 predecessor digests가 last safe point다.
- **Handoff:** `CandidatePassReport`, `VerifiedSolution`, rejection contract와 `E-P07-CANDIDATE-VERIFY` 후보 report를 WP-07.2에 전달.

### WP-07.2 — Independent candidate oracle, malicious/property와 replay

- **Precondition:** WP-07.1 deterministic baseline green.
- **Change target:** `build/test-fixtures`의 Phase 07 independent reference, malicious builder, property/replay tests.
- **Concrete tasks:** Production kernel/verifier/finalizer/encoder를 쓰지 않는 BigInteger/reference traversal, one-field corruption, mutable alias/unknown extension, route-bank property, fixed-envelope repeat와 seeded faulty-double sensitivity를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/test-fixtures,rpdptw/verification -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
    -Dtest=CandidatePartitionPropertyTest,CandidateArtifactImmutabilityTest,Phase07ReplayReproducibilityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Oracle forbidden production-helper reference 0, seeded cache/pair/overflow/summary/ordering defect가 각각 assertion red, generated counterexample 0, shrinkable seed/ordinal 기록, malicious callback 실행 0, repeated/parallel verified-solution fingerprint exact equality.
- **Failure/rollback:** Oracle가 production verifier/evaluator/encoder를 공유하거나 fixture가 official label/default를 사용하면 evidence 전체를 폐기한다. WP-07.1 deterministic hand cases로 되돌린다.
- **Handoff:** Independent oracle digest/coverage와 stable verified solution fixture를 WP-07.3에 전달.

### WP-07.3 — Final partition, exhaustive insertion audit와 diagnostic

- **Precondition:** WP-07.1 PASS-only solution contract와 Phase 05 exact pair insertion evaluator accepted.
- **Change target:** `result.finalization` audit/outcome/diagnostic types, audit/reference/property tests.
- **Concrete tasks:** Preliminary partition, static `PROVEN`, bound insertion-authority/evaluator identity, caller callback이 없는 stable exhaustive vehicle/position enumeration, rejection/work counts, feasible witness no-auto-fix, confidence ceiling과 exact outcomes를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
    -Dtest=FinalInsertionAuditAuthorityTest,FinalInsertionAuditTest,FinalInsertionAuditCorruptionTest,FinalInsertionAuditCompletenessPropertyTest,FinalOutcomeTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Caller-provided evaluator/callback field 0, authority mismatch에서 evaluator call 0, static-proven 외 누락 0, option set/count와 reference oracle exact, feasible witness에서 pre/post route/outcome fingerprint 불변, DIRECT/LEASE 모두 assigned, outcome bijection exact.
- **Failure/rollback:** Audit가 route를 commit하거나 search failure를 proven/exhaustive로 승격하거나 incomplete를 complete로 표시하면 WP를 revert한다. `VerifiedSolution`이 last safe point다.
- **Handoff:** Complete `FinalInsertionAuditRecord`, final outcomes와 `E-P07-AUDIT` 후보 report를 WP-07.4에 전달.

### WP-07.4 — Final result manifest와 deterministic canonical payload

- **Precondition:** WP-07.3 complete audit/outcomes green, encoding inclusion/exclusion review 승인.
- **Change target:** `result.api`, `result.encoding`, summary/result identity와 independent encoding oracle.
- **Concrete tasks:** §7.6 authoritative fields, outcome-derived checked summary, semantic fingerprint, versioned canonical bytes, stable ordering, duplicate/unknown/absent rejection과 observation metadata 분리를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
    -Dtest=CanonicalResultEncodingTest,ResultOutcomePropertyTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Insertion order/locale/timezone/thread에 exact bytes 동일, semantic visit order mutation은 digest 변경, authoritative one-field mutation coverage 100%, overflow/duplicate/unknown field fail-closed.
- **Failure/rollback:** `toString()`, unordered map, default locale/timezone, wall-clock 또는 provider locator가 identity에 스며들면 encoder/result draft를 제거한다. WP-07.3 typed outcomes/audit가 last safe point다.
- **Handoff:** `FinalResultManifest`/payload draft와 independent expected bytes/digest를 WP-07.5에 전달.

### WP-07.5 — Result-integrity verifier와 both-gate output

- **Precondition:** WP-07.1 candidate PASS와 WP-07.4 immutable draft/independent oracle green.
- **Change target:** `verification.result`, `Phase07FinalResultService`, typed rejection와 result corruption/both-gate tests.
- **Concrete tasks:** Exactly-one/ownership/audit/confidence/summary/result fingerprint/payload를 별도 경로로 재계산하고 result PASS-only `PublishableResult`와 safe rejection을 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
    -Dtest=ResultIntegrityVerifierTest,BothGatePublicationTest,CanonicalResultEncodingTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected tests:** Tampered outcome/audit/summary/result/payload 전부 거부, audit/result interruption은 fabricated disposition 없이 `GateIncomplete`, 한 PASS로 다른 실패를 덮는 경로 0, rejection normal payload 0, both PASS에서만 publishable output 1.
- **Failure/rollback:** Result verifier가 finalizer helper/summary/digest를 무검증 신뢰하거나 candidate failure 뒤 finalization을 실행하면 facade/result verifier를 revert한다. WP-07.4 draft와 WP-07.1 PASS artifact는 publication 불가 상태로 보존한다.
- **Handoff:** `E-P07-RESULT-VERIFY` 후보 report와 Phase 08 input contract를 WP-07.6에 전달.

### WP-07.6 — Architecture, full bundle, independent review와 Phase 08 handoff

- **Precondition:** WP-07.0~5 required test 전체 green, failure/skipped 0.
- **Change target:** Architecture/evidence manifest, Phase 07 review input, handoff compatibility report. 공용 status/progress는 scheduler만 변경.
- **Concrete tasks:** Exact commands/toolchain/test count/source/build/input/output digests, corruption fixtures, oracle independence, known gate, rollback point, Phase 06/08 compatibility를 content-addressed bundle로 고정한다.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
    -Dtest=Phase07VerificationArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/test-fixtures,rpdptw/verification -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never clean verify
  ```

- **Expected:** OR-Tools-free ALNS-only full reactor green; required test failed/error/skipped 0; verification→solver/search/cache/provider/vendor edge 0; evidence bundle digest와 independent Phase 07 review `PASS`.
- **Failure/rollback:** Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`, publishable authority와 Phase 08 handoff를 주장하지 않는다. Last accepted predecessor bundle과 last green WP artifact를 유지한다.
- **Handoff:** §15.2의 exact `Phase07HandoffManifest`와 typed output contract를 actual-but-unaccepted Phase 08 owner에게 전달.

## 12. Verification command, pass criteria와 evidence

### 12.1 Layer별 future command

| Layer | Future command | 통과 판정 |
|---|---|---|
| Candidate unit/corruption | WP-07.1 command | §10.3~§10.4 exact methods 모두 발견/green |
| Oracle independence/sensitivity, property/malicious/replay | WP-07.2 commands | Forbidden production-helper ref 0, seeded defects assertion-red, counterexample 0, malicious callback 0, seed/ordinal/oracle digest 기록 |
| Audit/outcome oracle | WP-07.3 command | Required set/option set/count/outcome bijection exact |
| Encoding/reproducibility | WP-07.4 command | Repeated/parallel bytes/digest exact, authoritative mutation coverage gap 0 |
| Result/both-gate corruption | WP-07.5 command | Every corruption rejected, normal failure payload 0 |
| Architecture | WP-07.6 architecture command | Forbidden dependency/import/bytecode/customer branch 0 |
| Module | `./mvnw -B -ntp -Dstyle.color=never -pl build/test-fixtures,rpdptw/verification -am clean verify` | Fixture test-jar와 module tests/package green, required skips 0 |
| Reactor | `./mvnw -B -ntp -Dstyle.color=never clean verify` | OR-Tools-free ALNS-only full reactor green, unrelated required module skip 없음 |

Selected command는 `-am`을 제거하고 `-Dsurefire.failIfNoSpecifiedTests=true`를 사용해 upstream module의 nonmatching test 때문에 fail-closed 검사를 끄지 않는다. 각 selected run 전에 **같은 exact source commit/archive**를 root `./mvnw -B -ntp -Dstyle.color=never clean install`로 full-test 설치하고 dependency artifact digest를 기록한다. 이 preparation run의 green은 selected method report를 대신하지 않고, source/digest가 다르거나 dependency resolution이 안 되면 selected run을 시작하지 않는다. Test-fixtures/verification의 cross-module suite는 selected filter 없이 full `clean verify`를 실행한다. 각 command 직후 fresh Surefire XML을 content-addressed evidence 위치로 봉인한 뒤 다음 `clean`을 실행하며, expected class/method manifest에서 missing/duplicate/failed/error/skipped required test가 하나라도 있으면 실패다. `-DskipTests`, `-Dmaven.test.skip=true`, required test disable, `surefire.failIfNoSpecifiedTests=false`, stale `target/`, console summary 한 줄 또는 이전 run 혼합은 exit evidence가 아니다.

### 12.2 Planned evidence

| Key | 반드시 포함할 내용 | 현재 상태 |
|---|---|---|
| `E-P07-CANDIDATE-VERIFY` | Source/build/problem/travel/profile/evaluation/SolvePlan/candidate/replay fingerprints, full projection authority receipt, partition/feasibility/overflow/claim/cache corruption report, independent oracle digest/sensitivity, exact commands/toolchain/count/exit | NOT_PRODUCED |
| `E-P07-AUDIT` | Verified solution and bound insertion-authority/evaluator-contract fingerprints, static-proven set, required-audit set, eligible vehicle/legal position/evaluated count, rejection/witness/no-mutation/confidence evidence, caller callback absence와 oracle digest | NOT_PRODUCED |
| `E-P07-RESULT-VERIFY` | Outcome bijection, summary derivation, manifest field coverage, canonical bytes/digest reproducibility, tamper/both-gate/incomplete block, `VerificationRejected`/`GateIncomplete` schema identity | NOT_PRODUCED |

Bundle은 content-addressed immutable artifact 또는 digest-protected local equivalent여야 한다. 최소한 다음도 포함한다.

- Phase/document/review version과 source commit
- Exact source fingerprints와 upstream evidence refs
- Java/Maven/runtime compatibility
- Passed/failed/error/skipped/discovered test 수
- Independent oracle source/fixture/expected bytes digest
- Seeded faulty-double sensitivity red report와 최소 counterexample/ordinal
- Architecture dependency report
- OPEN/EXPERIMENT_REQUIRED/GATED/deferred/non-applicable 목록
- Reviewer/verdict/timestamp/approval record
- Phase 08 handoff identity와 rollback point

## 13. Exit gate, Definition of Done과 anti-pattern

### 13.1 Exit gate

다음 AND 조건을 모두 만족해야 independent reviewer가 Phase 07 `ACCEPTED`를 권고할 수 있다.

- Phase 00~06 accepted input/evidence와 exact fingerprints가 확인됨.
- Verification module의 solver/search/cache/cloud/provider/vendor compile/bytecode dependency가 0임.
- Candidate/replay/problem/travel/profile/evaluation declaration/SolvePlan의 same immutable authority/content/projection equality가 증명됨.
- Candidate verifier가 raw/cache/claim을 권위로 사용하지 않고 route/bank에서 cache-free 재계산함.
- Fingerprint mismatch, malicious alias/extension, duplicate/missing task, pair split, route-bank XOR, unknown reference를 exact typed failure로 거부함.
- Well-formed hard violation은 `INFEASIBLE`, structural/overflow는 `INVALID`, identity/claim tamper는 `CORRUPT`로 구분됨.
- Candidate PASS가 없으면 `VerifiedSolution`과 finalization call이 0임.
- Audit request에 caller-provided evaluator/comparator/callback이 없고 accepted bound insertion-authority/evaluator-contract identity가 확인됨.
- Static `PROVEN` 외 모든 unassigned request의 eligible vehicle/legal position 전수 audit가 complete함.
- Feasible insertion 발견이 route/outcome/candidate fingerprint를 변경하지 않음.
- 모든 input request에 exactly-one final outcome이 있고 DIRECT/LEASE 모두 assigned 의미를 보존함.
- Unassigned diagnostic source/scope/confidence가 evidence ceiling을 넘지 않음.
- Summary, objective, directed distance와 route operational time이 verified route/outcome에서 checked 파생됨.
- Result verifier가 candidate PASS, ownership, audit, confidence, summary, result fingerprint와 payload bytes/digest를 독립 검사함.
- Tampered score/result/payload와 nondeterministic serialization test가 통과함.
- 어느 gate failure/incomplete에도 정상 route/outcome/benchmark payload가 없음.
- Gate 미완료가 `INVALID`/`INFEASIBLE`/`CORRUPT`로 합성되지 않고 typed `GateIncomplete`로 보존됨.
- `FinalResultManifest` authoritative field coverage와 canonical encoding version review가 완료됨.
- Same fixed envelope의 verified solution/outcome/result fingerprint가 exact 동일함.
- `E-P07-*` bundle과 independent review가 immutable identity로 고정됨.
- Phase 08 handoff와 rollback point가 명시됨.
- OPEN/GATED/deferred/official 값을 hidden default로 넣지 않음.

### 13.2 Definition of Done

Phase 07 `ACCEPTED`는 다음을 뜻한다.

1. Phase 06 search implementation을 import하거나 신뢰하지 않고 같은 immutable authority에서 candidate의 사실을 다시 만들 수 있다.
2. 정상 hard infeasible, malformed state, arithmetic invalidity와 integrity corruption을 혼동하지 않는다.
3. Verified solution에서만 final outcome/audit/result가 시작된다.
4. 모든 unassigned reason이 실제 static/audit/observed evidence 범위를 넘지 않는다.
5. Final result의 authoritative field, derived field, semantic fingerprint와 exact payload digest가 구분된다.
6. Independent oracle가 production helper와 분리되고 seeded faulty double의 실제 defect를 assertion-red로 잡으며 malicious/corruption/property/replay test가 모두 green이다.
7. 두 verifier의 PASS가 모두 있어야 Phase 08에 publishable contract를 넘긴다.
8. Runtime/storage/distribution 구현 없이 provider-neutral pure contract로 완료된다.
9. Evidence와 independent review가 accepted identity로 고정된다.

### 13.3 금지 anti-pattern

- Verification module이 solver, search state 또는 cache package를 compile-depend
- Search와 verifier가 같은 mutable object/cache/summary를 공유
- Candidate `feasible=true`, cached arrival/load/metric/score 또는 solver objective를 권위화
- 다른 problem/travel/profile fingerprint인데 값이 비슷하다는 이유로 허용
- Missing prepared arc의 reverse/symmetry/coordinate/speed fallback
- Duplicate/missing/split pair를 normal infeasible 또는 unassignment reason으로 축소
- Checked overflow를 wrap/clamp/`Long.MAX_VALUE` sentinel/empty result로 변환
- Candidate verifier failure를 모든 request `UNASSIGNED`로 변환
- Candidate/request가 evaluator/comparator/callback을 audit/verifier에 주입
- Bank membership/last insertion failure를 final status/reason으로 직렬화
- Static proof 없이 `PROVEN`, incomplete audit로 `EXHAUSTIVE_FOR_FINAL_SOLUTION` 사용
- Feasible audit witness 자동 insert, route mutation, re-solve 또는 hidden retry
- `LEASE`를 `OUTSOURCED` outcome으로 바꾸거나 fleet 밖 vehicle 생성
- Finalizer가 만든 summary/fingerprint/digest를 result verifier가 그대로 신뢰
- Candidate PASS로 result failure를 덮거나 result shape로 candidate failure를 덮음
- Audit/result 미완료를 `INVALID`, `INFEASIBLE`, `CORRUPT` 또는 정상 `UNASSIGNED`로 합성
- Failure output에 정상 route/outcome/benchmark payload 포함
- `Map<String,Object>`, reflection expression, arbitrary script 또는 candidate-provided comparator/callback
- Unordered map/set, default locale/timezone, `toString()`, thread completion order로 canonical bytes 생성
- Observed elapsed/provider locator/log order를 semantic result fingerprint에 포함
- Same identity/different bytes overwrite/수용
- Phase 07에서 filesystem/S3/GCS/CAS/status/retrieval/workflow/HTTP/Lambda 구현
- `Q-BENCH-02` official 수치, Win decimal fixture 또는 C-17 hybrid를 Phase 07 default로 당김

## 14. Blocker, OPEN/GATED/deferred와 restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00~06 accepted evidence 부재 | BLOCKER | Architecture + Phase 01~06 owners | Phase 07 code/test/evidence와 accepted handoff | 이 detailed document와 independent fixture design | Accepted predecessor reviews/bundles/digests와 actual reactor artifacts |
| Phase 06 actual but unaccepted | BLOCKER | Phase 06 Algorithm owner + scheduler | Exact candidate/replay handoff acceptance | Actual Phase 06 §7/§16.2와 이 문서 §7.3의 compatible projection | Cross-phase review, `E-P06-COW/ALNS/REPLAY`, accepted Phase 06 verdict |
| Solution-level evaluation owner/API/identity | CROSS-PHASE BLOCKER | Core/Evaluation + Capability/Profile + Phase 05/07 | `checkedFullSolutionRecompute`, declared claim parity와 verified solution fingerprint | Accepted route-level kernel; ad hoc route-vector summing 금지 | Phase 03 review `F-P03-004`의 exact contract/API/failure/comparator와 Phase 03~05/07 corruption review 승인 |
| Scheduler task/owner 미지정 | BLOCKER | 총괄 scheduler | Authoritative status, implementer/oracle/reviewer assignment | `scheduler_task_id: TBD_NOT_SUPPLIED` | Exact task ID와 분리된 역할 지정 |
| Candidate/result Java names | PROPOSED/OPEN | Verification/Result + Architecture | Implementation/public compatibility freeze | §7~§8 semantic contract | Phase 07 review/ADR approval |
| Canonical result encoding/public schema | OPEN/PROPOSED | Product/API/Data + Verification | External bytes compatibility와 final implementation freeze | Versioned internal field inclusion/exclusion | `ADR-002` 또는 동등 schema/canonicalization/compatibility/security approval |
| Fingerprint algorithm/version | OPEN/PROPOSED | Architecture/Data Integrity | Public hash compatibility | Upstream declared algorithm/version 소비 | Phase 02/serialization ADR 승인; hidden SHA default 금지 |
| `GateIncomplete` Phase 08 mapping | RESOLVED_BY_PHASE08_V1_1 | Phase 08 Application + independent reviewer | 더 이상 document-contract blocker가 아님; Phase 08 implementation/evidence gate는 별도 유지 | Phase 08 v1.1의 three-variant receipt, `VERIFICATION_INCOMPLETE`와 no-payload mapping | Variant/field contract가 바뀌거나 future integration test가 실패할 때만 재개 |
| `Q-BENCH-02` official execution values | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 14 official manifest/baseline/cutover; generic Phase 07 test는 안 막음 | Explicit test-only values and value-free contract | Calibration corpus/protocol, measured review, explicit approval |
| Current Win fixture decimal `D/U` | BLOCKER FOR OFFICIAL USE | Input·Matrix + Benchmark | 해당 fixture official result; generic Phase 07 integer fixture는 안 막음 | `P07_TINY_PD_3_TEST_ONLY` | Compliant integer matrix 또는 explicit contract/migration approval |
| `C-17` route pool/MIP | GATED TARGET | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13와 production default | ALNS-only candidate/result contract | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope와 OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | Optional variant 질문/구현 | Current pair/terminal/bank contract | Representative fixture, core-impact feasibility와 별도 승인 |
| Multi-trip/rotation | DEFERRED FEATURE | Product·Domain·Algorithm | Single-trip 밖 verifier/result 의미 | Oneway + single roundtrip | Trip/reset/depot/resource/pair non-crossing contract와 승인 |
| Phase 08/09+ runtime/storage/provider | OUT OF SCOPE / FUTURE GATE | Application/Platform/Operations | Publication/retrieval/storage/distribution | Pure `Phase07Output` contract | Phase 08 accepted ports/local runtime부터 순서대로 |

`Q-INFRA-01`은 `RESOLVED`지만 Phase 07 restart 조건이 아니다. AWS S3 + Step Functions + Lambda 선택은 Phase 11 target/reference이며 verification/result dependency 방향과 output 의미를 바꾸지 않는다.

## 15. Previous/next handoff

### 15.1 Previous — actual but unaccepted Phase 06

[actual: Phase 06 — COW ALNS와 reproducibility](phase-06-cow-alns-reproducibility.md)에서 다음을 받아야 한다.

- Last committed immutable candidate route/bank source of truth
- Exact problem/prepared travel/bound profile/evaluation declaration/`SolvePlan` identities
- Candidate-declared route/solution metric/score/objective claims
- Candidate/content/replay fingerprints and digests
- Build/runtime, algorithm/operator/state-strategy/config versions
- Base/derived seed lineage, requested/completed work, stage/round/run/warm-start lineage
- Exact normal/exceptional termination과 stable trace digest
- Phase 06 accepted review와 `E-P06-COW`, `E-P06-ALNS`, `E-P06-REPLAY`

받으면 안 되는 것은 다음이다.

- Mutable `TrialDraft`, `current/stageBest/solveBest` alias
- Search/insertion/arrival/load/metric/score cache handle
- Solver feasibility flag나 summary만 있는 candidate
- Raw input, coordinate/speed fallback, partial travel
- Provider storage locator/client/credential
- Missing official value를 채운 hidden default

Actual Phase 06 §7.1/§8.5/§16.2와 위 목록을 직접 대조했다. Phase 06은 세 handoff 부류와 exact evidence key를 정의하지만 아직 accepted 상태가 아니다. Cross-phase review에서 Java projection 이름을 조정하더라도 Phase 07이 solver module을 import하거나 search cache를 authority로 받는 방향으로 맞추지 않는다.

### 15.2 Next — actual but unaccepted Phase 08

[actual: Phase 08 — Application ports와 local runtime](phase-08-application-ports-local-runtime.md)은 v1.3 `REVIEWED_WITH_CORRECTIONS`/`NOT_STARTED`이며 다음 stable contract만 소비한다.

```text
Phase07HandoffManifest
  phase07ContractVersion
  source/build/runtime fingerprints
  problem/travel/profile/evaluation/SolvePlan/candidate/replay identities
  candidatePassReport fingerprint or rejection
  verifiedSolution fingerprint when PASS
  audit/result/resultPass fingerprints when PASS
  canonical payload digest/length when PASS
  Phase07Output contract fingerprint
  E-P07-CANDIDATE-VERIFY / AUDIT / RESULT-VERIFY refs
  accepted Phase07 review ref
  rollback point

Phase07Output
  Publishable(PublishableResult)
  or Rejected(
       FinalResultRejection.VerificationRejected
       | FinalResultRejection.GateIncomplete)
```

Phase 08은 `Publishable`만 정상 success/result publication 입력으로 사용할 수 있다. `Rejected`는 typed status/error mapping 입력이며 정상 route/outcome payload로 저장하거나 조회하지 않는다.

Phase 08 v1.1 §3.2/§6.4~§7.7/§11.5/§16.1은 `Publishable`, `VerificationRejected`, `GateIncomplete`를 exhaustive하게 분리한다. `GateIncomplete`는 fabricated disposition이나 normal payload 없이 `VERIFICATION_INCOMPLETE`와 `Interrupted(VerificationGateIncomplete)`로 손실 없이 매핑한다. 따라서 이전 Phase 08 document-contract blocker는 `RESOLVED_BY_PHASE08_V1_1`이다. 다만 Phase 08 구현/evidence와 Phase 07 accepted output이 아직 없으므로 handoff/phase acceptance는 계속 `NOT_READY`다.

Phase 08이 소비하면 안 되는 것은 다음이다.

- Verification scratch/cache와 mutable builder
- Auditor iterator/internal feasible witness의 raw mutable object
- Search cache/solver summary
- Provider path/URI/bucket/key로 바뀐 domain identity
- Candidate PASS만 있고 result PASS가 없는 `FinalResultDraft`
- Result PASS report를 구현자가 합성한 self-claim

Phase 07은 `ArtifactStore`, `RunStateRepository`, `ResultPublisher`, local filesystem, CAS와 retrieval 구현을 만들지 않는다. Phase 08은 이 pure contract 뒤에서 해당 port를 구현하고 both-gate 의미를 보존한다.

### 15.3 Other downstream consumers

| Consumer | 소비할 것 | 소비하면 안 되는 것 | Handoff verification |
|---|---|---|---|
| Phase 10 coordinator | Phase 08 port 뒤의 publishable/rejected semantics | Verifier 내부, worker completion을 result PASS로 간주 | All-declared completion 뒤에도 each result both-gate 확인 |
| Phase 11 AWS reference | Provider-neutral result contract/digest | S3/Step Functions/Lambda가 verifier 의미 재구현 | Local/AWS semantic parity |
| Phase 14A ALNS benchmark | Publishable ALNS result, verifier reports와 objective vector | Phase 07 PASS를 quality/performance acceptance로 오인 | Approved corpus/protocol, complete repeat accounting와 independent acceptance |
| Phase 13 gated hybrid | Same candidate/result both-gate path | Raw MIP incumbent/ObjVal/selected columns | Phase 14A acceptance 뒤 materialize/full-evaluate 후 Phase 07 exact gate 재사용 |
| Phase 14B official | Publishable result + approved official manifest | Test-only fixture/value, different fingerprint quality compare | ALNS benchmark receipt, compliant authority, approved values, all-worker verified |

## 16. Source → requirement → test → evidence traceability

| Requirement | Source | Phase 07 contract | Exact test | Planned evidence |
|---|---|---|---|---|
| `REQ-ARCH-DAG` verification independent of solver/search/cache/provider | [Final Architecture §2](../../2026-07-26-architecture-design.md#2-module과-package-경계), [Plan §4.2](../master-realization-plan.md#42-compileruntime-invariants) | §6/§8.5 dependency | `Phase07VerificationArchitectureTest.*` | All P07 keys + architecture report |
| `REQ-TRAVEL` same complete prepared authority | [Master §4.2](../../master-design.md#42-단계별-데이터-계약), [Phase 02 §13.3](phase-02-prepared-travel-immutable-problem.md#133-이후-consumer) | §4/§7.2 authority equality | `CandidateVerifierAuthorityTest.*PreparedTravel*` | `E-P07-CANDIDATE-VERIFY` |
| `REQ-EVALUATION-AUTHORITY` exact bound declaration/SolvePlan projection | [Phase 04 §7.4](phase-04-capabilities-customer-profiles.md#74-proposed-java-25-contract), [Actual Phase 06 §16.2](phase-06-cow-alns-reproducibility.md#162-next--actual-but-unaccepted-phase-07) | §4/§7.3/§8.1 five-way authority projection | `CandidateVerifierAuthorityTest.*EvaluationDeclarationOrSolvePlan*`, projection coverage | `E-P07-CANDIDATE-VERIFY` |
| `REQ-PAIR` complete pair/same vehicle/precedence/route-bank XOR | [Master §6](../../master-design.md#6-핵심-불변조건과-atomic-mutation), `C-06` | §3.2/§9.1 partition | `CandidatePartitionCorruptionTest.*` | `E-P07-CANDIDATE-VERIFY` |
| `REQ-CACHE` cache-free recomputation | [Master §12](../../master-design.md#12-candidate-state-cache와-rollback), [Master §14.1](../../master-design.md#141-publication-gate) | Search claims non-authoritative | `CandidateVerifierIndependenceTest.*`, claim tamper tests | `E-P07-CANDIDATE-VERIFY` |
| `REQ-FAILURE` infeasible/invalid/corrupt fail-closed | [Final Domain §16](../../2026-07-26-domain-design.md#16-오류와-종료-모델), [Integrated §11.7](../../architecture-domain-implementation-design.md#117-error-categories) | §3.3 disposition | Route/partition/overflow/claim test matrices | `E-P07-CANDIDATE-VERIFY` |
| `REQ-RESULT` search bank/outcome separation | `C-15`, [Master §10](../../master-design.md#10-search-solution과-final-result), `Q-RES-01` | Two-state final outcome | `FinalOutcomeTest.*` | `E-P07-AUDIT` |
| `REQ-AUDIT` static proven + required final audit | `Q-RES-02`, [Master §10.2](../../master-design.md#102-finalization과-result) | §7.5/§9.2 exhaustive contract | `FinalInsertionAuditTest.*`, completeness property | `E-P07-AUDIT` |
| `REQ-DIAGNOSTIC` evidence-bounded confidence | [Master §10.2](../../master-design.md#102-finalization과-result), [Final Domain §15](../../2026-07-26-domain-design.md#15-verification-finalization과-result) | Source/scope/confidence matrix | Audit/diagnostic confidence tests | `E-P07-AUDIT`, `E-P07-RESULT-VERIFY` |
| `REQ-VERIFY` two independent verifier gates | `C-21`, [Master §14.1](../../master-design.md#141-publication-gate) | Candidate/result PASS separation | `BothGatePublicationTest.*` | `E-P07-CANDIDATE-VERIFY`, `E-P07-RESULT-VERIFY` |
| `REQ-INCOMPLETE` gate fail와 incomplete 분리 | [Master §10.2](../../master-design.md#102-finalization과-result), [Master §14.1](../../master-design.md#141-publication-gate) | `VerificationRejected` 대 `GateIncomplete`; 둘 다 normal payload 0 | `BothGatePublicationTest.*GateIncomplete*` | `E-P07-RESULT-VERIFY` |
| `REQ-SUMMARY` outcome/route-derived metric | `Q-BENCH-01`, [Final Domain §10/§15](../../2026-07-26-domain-design.md#15-verification-finalization과-result) | Checked derived summary | Tampered summary and result oracle tests | `E-P07-RESULT-VERIFY` |
| `REQ-PROVENANCE` input→replay→both-gate lineage | [Master §10.3](../../master-design.md#103-result-provenance), [Integrated §19](../../architecture-domain-implementation-design.md#19-configuration-provenance와-observability) | §7.3/§7.6 manifest | `Phase07ReplayReproducibilityTest.*` | All P07 keys |
| `REQ-REPRO` canonical result determinism | `C-22`, [Master §13.2](../../master-design.md#132-strong-reproducibility-envelope) | Stable semantic/payload identity | `CanonicalResultEncodingTest.*` | `E-P07-RESULT-VERIFY` |
| `REQ-CORRUPTION` independent malicious/tamper fixtures | [Final Architecture §5.6](../../2026-07-26-architecture-design.md#56-test와-evidence), [Integrated §22.4](../../architecture-domain-implementation-design.md#224-independent-corruption-fixtures) | §10 one-field corruption | Candidate/result corruption matrices | All P07 keys |
| `REQ-HANDOFF-P06` consume committed candidate/replay only | [Actual Phase 06 §16.2](phase-06-cow-alns-reproducibility.md#162-next--actual-but-unaccepted-phase-07), [Integrated §10~§11](../../architecture-domain-implementation-design.md#10-phase-6--cow-alns와-reproducibility), [Plan Phase 06~07](../master-realization-plan.md#phase-07--독립-검증과-최종-결과) | §7.3/§15.1 | Authority receipt/replay tests | Phase 06 receipt + `E-P07-CANDIDATE-VERIFY` |
| `REQ-HANDOFF-P08` output final result contract only | [Integrated §12](../../architecture-domain-implementation-design.md#12-phase-8--application-ports와-local-reference-runtime), [Plan Phase 08](../master-realization-plan.md#phase-08--application-interface와-local-실행) | §15.2 | Both-gate output + architecture tests | `E-P07-RESULT-VERIFY`, handoff manifest |
| `REQ-OPEN-GATE` no hidden official/default/gated scope | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, [Plan §14](../master-realization-plan.md#14-open-gated-deferred와-restart-condition) | §14 blocker table | Config/source label/architecture inspection | All P07 keys + known limitations |

새 result field, diagnostic confidence, verifier shortcut, serialization rule 또는 public schema 요구가 발견되면 이 표에 source/owner/test/evidence를 연결하고 관련 authority/ADR/Phase 06·08 compatibility와 review를 같은 변경 단위에서 갱신한다. 구현 편의를 위해 search claim을 권위화하거나 미확정 값을 hidden default로 채우지 않는다.
