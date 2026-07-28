# Phase 07 사람용 구현 가이드 — 독립 검증과 최종 결과

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 07 only
canonical_phase_count: 15
phase: "07"
phase_name: independent-verification-final-result
canonical_phase_document: docs/implementation/phases/phase-07-independent-verification-final-result.md
canonical_phase_review: docs/implementation/reviews/phase-07-review.md
canonical_phase_document_status: REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS_AND_CROSS_PHASE_CONTRACT
handoff_status_observed: NOT_READY
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_observed_date: 2026-07-29
inventory_live_snapshot_at: 2026-07-29T02:22:50+09:00
worktree_revalidation_status: PHASE00_FIX01_IN_PROGRESS_NOT_ACCEPTED
worktree_revalidation_scope: modified root POM/progress plus untracked executable wrapper/build/legacy/rpdptw and rejected-or-superseded Phase 00 evidence
source_fingerprint_scheme: current_working_bytes_git_hash_object_with_head_and_live_layers_separated
adjacent_guide_fingerprint_scheme: frozen_read_snapshot_not_acceptance_revalidate_on_drift
human_review: docs/implementation/human-guides/reviews/phase-07-review.md
human_review_sha256: e673c4b9963e90f21432252560a15e176cbb175522467cb130b4779ba1bf8659
correction_round: "01"
target_sha256_before_correction: 43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d
prerequisite_phases:
  - "00"
  - "01"
  - "02"
  - "03"
  - "04"
  - "05"
  - "06"
source_sections_and_fingerprints:
  docs/master-design.md: "§2.2~2.4, §4.1~4.6, §6, §10, §12~14.1, §15.7, §16~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "§1, §3, §10, §12~16 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "§1, §5~7, §18~20, §22 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/architecture-domain-implementation-design.md: "§1.7~1.9, §2~3, §10~12, §19~25, §27~28 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-MTX/Q-OBJ/Q-RES/Q-BENCH/Q-INFRA/Q-VAR rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/2026-07-26-domain-design.md: "dated implementation authority input / historical detail cross-check | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "dated implementation authority input / historical detail cross-check | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~4, Phase 05~08, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "live status snapshot only; current working bytes | f875edbdeb79fb18710421b56e126462a4d6d38c"
  docs/implementation/phases/phase-06-cow-alns-reproducibility.md: "§7.1, §7.6, §8.4~8.5, §16.2 | 984b6978981fffa662bcf4cf5b4f8a14c2287c09"
  docs/implementation/phases/phase-07-independent-verification-final-result.md: "§1~16 | 1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115"
  docs/implementation/phases/phase-08-application-ports-local-runtime.md: "§3.2, §6.4~7.7, §11.5, §16.1 | 2aff093a6f2728470a7ccbb22b7e1a1a71f5b963"
  docs/implementation/reviews/phase-07-review.md: "§1~8 | 156a994ef66d9c6f51785d96d2b327fe4539712e"
  docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md: "full correction read snapshot; Phase06→07 schema gate | e1a07397222a313752b1eb79fb7f414d25e6d0bf"
  docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md: "full correction read snapshot; three-variant consumer | 2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223"
expected_reader:
  - Java의 record, interface, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, time window, capacity propagation을 구현해 보았다
  - RPDPTW의 pair identity, final audit, two-gate publication은 처음 접한다
owner_roles:
  implementation: RPDPTW Verification/Result owner
  upstream_candidate: Phase 06 COW ALNS/Reproducibility owner
  upstream_authority: Phase 02 Domain/Travel and Phase 04 Capability/Profile owners
  upstream_evaluation: Phase 03 Core/Evaluation and Phase 05 insertion owners
  independent_oracle: Phase 07 independent verification-test owner
  downstream: Phase 08 Application/Local Runtime owner
  review: independent Phase 07 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P07-CANDIDATE-VERIFY
  - E-P07-AUDIT
  - E-P07-RESULT-VERIFY
```

> 이 문서는 사람이 Phase 07을 이해하고, entry gate가 열린 뒤 구현하고, 독립 evidence로 완료를 판정하기 위한 교육형 작업 지시서다. 현재 shared checkout에는 동시 작업 중인 미승인 Phase 00 reactor/POM/package-info scaffold가 보이지만 Phase 07 production type, test 또는 evidence는 없다. 아래 Java 이름과 signature는 별도 표시가 없는 한 **제안 후보(proposed)**이며, 존재하는 API나 승인된 public/wire contract를 뜻하지 않는다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 07은 solver가 내놓은 route와 request bank를 solver 밖에서 처음부터 다시 검증하고, 검증된 해에서만 최종 `ASSIGNED`/`UNASSIGNED` outcome과 audit·summary·payload를 만든 뒤, 그 결과를 다시 독립 검사하여 **두 gate가 모두 통과한 결과만 `PublishableResult`로 봉인하는 단계**다.

이 Phase의 `PASS`는 correctness와 result integrity를 뜻한다. 더 좋은 해인지, 충분히 빠른지, official baseline인지, production에 배포해도 되는지는 각각 Phase 14A benchmark acceptance와 Phase 14B production authority가 판단한다.

## 2. 큰 그림과 필요한 이유

### 2.1 Solver 결과를 그대로 믿으면 안 되는 이유

Search는 속도를 위해 propagation cache, insertion delta, metric aggregate와 objective cache를 사용한다. 이들 중 하나가 stale이거나 pair 편집 rollback이 잘못되어도 solver 내부의 `feasible=true`와 summary가 서로 같은 오류를 공유할 수 있다. Controller가 후보의 `objective` 숫자만 비교해 최소값을 고르는 현재 placeholder는 이 위험을 막지 못한다.

Phase 07은 같은 immutable facts를 보되 solver의 결론은 신뢰하지 않는다.

```text
같이 사용해도 되는 authority
  ProblemInstance
  PreparedTravel
  BoundProfile + evaluation declaration + SolvePlan
  candidate의 route order / vehicle binding / request bank

사용하면 안 되는 search claim
  cached arrival/load
  insertion table
  feasible flag
  cached metric/score/objective
  raw backend objective
```

검증기는 첫 번째 묶음에서 사실을 다시 계산한 뒤 두 번째 묶음의 선언과 사후 비교한다. 선언이 다르면 “검증기가 고쳐 준 값”으로 통과시키는 것이 아니라 integrity corruption으로 거부한다.

### 2.2 두 verifier 사이에 finalization이 있는 이유

Candidate가 물리적으로 맞는 것과 외부에 내보낼 결과가 완전한 것은 다른 문제다.

```text
Phase 06 committed candidate
  → candidate verifier
  → VerifiedSolution
  → preliminary outcomes
  → required final-solution insertion audit
  → final outcomes + summary + manifest + payload draft
  → result-integrity verifier
  → PublishableResult
```

첫 verifier는 route/bank, pair, terminal, directed travel, load/time/resource와 evaluation claim을 검사한다. 두 번째 verifier는 모든 input request의 exactly-one outcome, route ownership, audit completeness, diagnostic confidence, summary, semantic fingerprint와 exact payload bytes를 검사한다. 첫 gate 하나만 통과한 draft는 publishable하지 않다.

### 2.3 Canonical 15 Phase 안의 위치

이 저장소의 canonical Phase는 **00~14, 총 15개**다. 표의 “주요 producer → consumer 계약”은 한 Phase가 다음 단계에 넘겨야 하는 최소 의미다.

| Phase | 주제 | 주요 producer → consumer 계약 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | Reactor, `core/solver/verification/application`, test-fixtures와 dependency guard를 모든 Phase에 제공 |
| 01 | Canonical input와 정규화 | Versioned immutable normalized facts와 typed error를 Phase 02에 제공 |
| 02 | Prepared travel과 immutable problem | Complete directed `PreparedTravel`, `ProblemInstance`, dense identity와 fingerprint를 Phase 03/05/**07**에 제공 |
| 03 | Route propagation과 evaluation kernel | Cache-free propagation/evaluation/comparator 계약을 Phase 04/05/**07**에 제공 |
| 04 | Capability와 customer profile | Exact `BoundProfile`, evaluation declaration와 `SolvePlan` closure를 Phase 05/06/**07**에 제공 |
| 05 | Pair insertion과 initial portfolio | Stable route/bank, exact pair evaluator, independent initial candidates를 Phase 06과 Phase 07 audit에 제공 |
| 06 | COW ALNS와 reproducibility | `CommittedCandidate`, replay manifest와 accepted evidence를 **Phase 07**에 제공 |
| **07** | **Independent verification과 final result** | **두 verifier, final audit, typed rejection과 `PublishableResult`를 Phase 08/14A에 제공** |
| 08 | Application ports와 local runtime | Phase 07의 세 output variant를 손실 없이 소비하고 local E2E/artifact/state/publication port를 Phase 09/10에 제공 |
| 09 | No-DB object storage | Immutable artifact, exact-key read와 CAS semantics를 Phase 10/11에 제공 |
| 10 | Provider-neutral coordinator | Declared worker completeness, deterministic fan-in, retry identity를 Phase 11/14에 제공 |
| 11 | AWS reference distribution | S3 + Step Functions + Lambda parity/security/rollback evidence를 Phase 14B에 제공 |
| 12 | Provider substitution | **승인된 provider 축만** parity와 migration evidence 아래 교체하는 gated branch |
| 13 | Optional hybrid route selection | **Phase 14A receipt와 C-17 승인 뒤에만** route pool/CP-SAT/full-evaluation fallback을 Phase 14B에 조건부 제공 |
| 14 | 14A benchmark / 14B official cutover | 14A는 ALNS benchmark acceptance receipt, 14B는 official manifest와 production authority/cutover evidence를 소유 |

ALNS-first critical path는 다음과 같다.

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → [07] → 08 → 14A
```

Phase 13은 이 경로의 선행조건이 아니다. Phase 07 구현에 OR-Tools나 MIP backend를 넣어 미래 branch를 미리 열지 않는다. Phase 14A의 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 별도 `C-17` 승인이 있어야 Phase 13을 시작할 수 있다. Phase 14A receipt는 Phase 14B production authority도 아니다.

### 2.4 Phase 07의 producer와 consumer

```text
Phase 02  ProblemInstance + PreparedTravel
Phase 04  BoundProfile + evaluation declaration + SolvePlan
Phase 05  accepted bound exact pair-insertion authority
Phase 06  candidate + replay + evidence
     \       |       |       /
      \ exact identity/content equality
       ↓
Phase 07 candidate verifier
       ↓ PASS only
VerifiedSolution
       ↓
final outcome + insertion audit + deterministic result draft
       ↓
Phase 07 result-integrity verifier
       ↓
Publishable | VerificationRejected | GateIncomplete
       ↓
Phase 08 application mapping / Phase 14A benchmark input
```

| 경계 | Producer가 보장할 것 | Phase 07/consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 02 → 07 | Immutable problem/travel, complete directed lookup, intrinsic identity/content digest | Raw input, coordinate, speed, reverse/symmetric arc를 다시 해석 |
| Phase 03/04 → 07 | Accepted route/solution evaluation 의미, exact declaration/`SolvePlan`, comparator와 failure contract | Route vector를 ad hoc 합산해 solution authority를 발명 |
| Phase 05 → 07 | Side-effect-free exact pair insertion contract/build/declaration identity | Caller가 evaluator callback을 audit request에 주입 |
| Phase 06 → 07 | Immutable route/bank projection, declared claims, replay/work/termination/evidence lineage | Solver class, mutable COW state, cache와 final outcome을 넘김 |
| Phase 07 → 08 | `Publishable`, `VerificationRejected`, `GateIncomplete`의 exhaustive typed output | Candidate PASS만으로 success 처리하거나 rejection에 정상 payload를 넣음 |
| Phase 07 → 14A | Both-gate ALNS result와 immutable verifier evidence | Correctness PASS를 quality/performance/official acceptance로 승격 |

## 3. Source authority, fingerprint와 읽기 순서

### 3.1 충돌 해소 순서

이 가이드는 source의 역할과 시점을 섞지 않는다. 구현 판단에는 다음 네 층을 적용한다.

```text
1. CURRENT MAP / CURRENT CORRECTION AUTHORITY
   사용자 고정 지시
   → docs/README.md
   → non-dated canonical five:
      master-design.md
      domain-design.md
      architecture-design.md
      architecture-domain-implementation-design.md
      master-design-open-questions.md

2. DATED AUTHORITY INPUT
   docs/2026-07-26-domain-design.md
   docs/2026-07-26-architecture-design.md
   → implementation 문서군이 작성될 때 사용한 상세 입력
   → 현재 conflict resolver가 아니라 semantic regression cross-check

3. IMPLEMENTATION BASELINE
   implementation README / master realization plan
   canonical Phase 06~08 / original Phase 07 review
   HEAD commit과 accepted artifact/evidence/receipt

4. LIVE SNAPSHOT
   shared worktree POM/wrapper/source/test/target/progress의 관찰값
   → 시점이 붙은 진단 자료
   → HEAD나 acceptance를 덮지 않으며 receipt가 아니다
```

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md)과 deprecated 문서는 누락·퇴행을 확인하는 역사 자료일 뿐 현재 authority가 아니다. Non-dated [Canonical Domain](../../../domain-design.md) §13~§16의 finalization/result/metric/error/evidence와 [Canonical Architecture](../../../architecture-design.md) §5~§7, §18~§20, §22의 module/dependency/test/build/trace를 이 가이드의 현재 의미로 사용한다. Dated Domain §15~§18과 Architecture §2/§5~§6은 상세 대조 입력으로 남기되 현재 의미와 충돌하면 non-dated canonical five 및 사용자 고정 지시를 따른다.

현재 [Implementation README](../../README.md)와 [human-guide README](../README.md)가 날짜 붙은 Domain/Architecture를 진입점으로 가리키는 source-index 충돌은 이 두 파일의 소유 범위 밖이다. 구현자는 이를 숨은 동률 규칙이나 package owner로 사용하지 않고 entry receipt에 `KNOWN_SOURCE_INDEX_CONFLICT`로 기록한다. 별도 scheduler 정렬 전에도 이 Phase 07 target에서는 위 네 층이 conflict resolver다.

Current Domain 일부의 오래된 question-count/상태 표현과 충돌할 때는 canonical Master와 question register의 exact row를 따른다. 현재 register는 `RESOLVED=26`, `OPEN EXPERIMENT_REQUIRED=1`인 `Q-BENCH-02`, `DEFERRED=1`인 `Q-VAR-01`을 보존한다. 이것은 Phase 07에 official benchmark 값, AWS code, optional variant 또는 production authority를 넣었다는 뜻이 아니다.

### 3.2 검증 가능한 source fingerprint

아래 source fingerprint는 correction 입력을 읽은 **current working bytes의 `git hash-object`**다. Tracked canonical source는 관찰 시 HEAD blob과 같았고, live progress/POM은 HEAD와 다르다. Source가 바뀌면 hash만 갱신하지 말고 해당 heading의 의미, 이 가이드의 계약·WP·test·evidence 영향을 함께 review한다.

| 층 | Source | 직접 읽을 heading/section | Working-byte Git hash-object |
|---|---|---|---|
| Current map | [Repository design map](../../../README.md) | current top-level document entrypoints and status | `13f1b3b2dea038b8e0b466c138f5f59299413125` |
| Current canonical | [Canonical Master](../../../master-design.md) | §2.2~2.4, §4.1~4.6, §6, §10, §12~14.1, §15.7, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| Current canonical | [Canonical Domain](../../../domain-design.md) | §1, §3, §10, §12, §13~§16 | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| Current canonical | [Canonical Architecture](../../../architecture-design.md) | §1, §5~§7, §18~§20, §22 | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| Current canonical | [Integrated design](../../../architecture-domain-implementation-design.md) | §1.7~1.9, §2~§3, §10~§12, §19~§25, §27~§28 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| Current canonical | [Question register](../../../master-design-open-questions.md) | §1~§4와 exact `Q-MTX/Q-OBJ/Q-RES/Q-BENCH/Q-INFRA/Q-VAR` rows | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| Dated input | [2026-07-26 Domain](../../../2026-07-26-domain-design.md) | §2, §7~§10, §15~§18; historical/detail cross-check | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| Dated input | [2026-07-26 Architecture](../../../2026-07-26-architecture-design.md) | §1.2~§1.4, §2, §5.2~§5.6, §6; historical/detail cross-check | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| Implementation baseline | [Implementation README](../../README.md) | §0~§7, authority/DAG/index; known source-index conflict | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| Implementation baseline | [Master Realization Plan](../../master-realization-plan.md) | §2~§4, Phase 05~08, §8~§15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| Implementation baseline | [Actual Phase 06](../../phases/phase-06-cow-alns-reproducibility.md) | §7.1, §7.6, §8.4~§8.5, §16.2 handoff | `984b6978981fffa662bcf4cf5b4f8a14c2287c09` |
| Implementation baseline | [Canonical Phase 07](../../phases/phase-07-independent-verification-final-result.md) | 전체, 특히 §4, §7~§14, §15 | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` |
| Implementation baseline | [Original Phase 07 review](../../reviews/phase-07-review.md) | §1~§8, F-P07-001~008와 residual F-P07-005 | `156a994ef66d9c6f51785d96d2b327fe4539712e` |
| Implementation baseline | [Actual Phase 08](../../phases/phase-08-application-ports-local-runtime.md) | §3.2, §6.4~§7.7, §11.5, §16.1 handoff | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| Adjacent read snapshot | [Phase 06 human guide](phase-06-human-implementation-guide.md) | 전체; source conflict, exact 89, effective-POM과 Phase06→07 schema gate | `e1a07397222a313752b1eb79fb7f414d25e6d0bf` |
| Adjacent read snapshot | [Phase 08 human guide](phase-08-human-implementation-guide.md) | 전체; Phase 07 three-variant input/lossless mapping과 wrapper authority | `2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223` |
| Live snapshot | [Execution Progress](../../execution-progress-and-results.md) | current Phase 00 correction/status/blocker; acceptance source 아님 | `f875edbdeb79fb18710421b56e126462a4d6d38c` |

재검증 예:

```bash
git rev-parse HEAD
git hash-object docs/domain-design.md
git hash-object docs/architecture-design.md
git hash-object docs/implementation/phases/phase-07-independent-verification-final-result.md
git hash-object docs/implementation/reviews/phase-07-review.md
```

인접 Phase/review의 whole-file digest를 acceptance 조건으로 영구 저장하지 않는다. 위 adjacent hash는 실제로 전수 읽은 correction snapshot을 재현할 뿐이며, shared checkout에서 이후 drift할 수 있다. Entry에서는 인용 section의 의미와 accepted artifact/evidence identity를 다시 확인한다. Live hash가 달라지면 이 표를 acceptance에 맞추지 말고 새 timestamp/hash/status로 다시 snapshot한 뒤, accepted commit/bundle/receipt와 별도 열로 비교한다.

### 3.3 구현 전 정확한 읽기 순서

| 순서 | 읽을 곳 | 답해야 할 질문 | 확인할 module/file/evidence |
|---:|---|---|---|
| 1 | Implementation README §0~§7 | Authority, 15 Phase, 현재 ALNS-first gate는 무엇인가? | Phase 07 canonical filename과 review link |
| 2 | Master §10, §14.1, §15.7 | Search state와 final result, 두 verifier의 완료 의미는 무엇인가? | `RM-5`, publication AND gate |
| 3 | Canonical Domain §3, §10, §12~16 | Pair identity, stable state, outcome/audit/result metric/error/evidence는 무엇인가? | Exact operational metric와 reference comparator |
| 4 | Canonical Architecture §5~7, §18~20, §22 | Verification package/module/dependency/test/build/trace 경계는 어디인가? | Target `rpdptw-verification → rpdptw-core`; test-fixture test edge |
| 5 | Integrated design §11, §19~25 | Phase 07 sequence, provenance, security, failure, test는 무엇인가? | `verification.candidate`, `result.finalization`, `verification.result` |
| 6 | Question register exact rows | `ASSIGNED/UNASSIGNED`, audit, official values와 deferred 범위는 무엇인가? | `Q-RES-01/02`, `Q-BENCH-02`, `Q-VAR-01` |
| 7 | Master Realization Plan Phase 05~08, §8~15 | Entry/exit/evidence/DoD/rollback은 무엇인가? | `E-P07-CANDIDATE-VERIFY`, `E-P07-AUDIT`, `E-P07-RESULT-VERIFY` |
| 8 | Actual Phase 06 §16.2 + current Phase 06 human guide §15.2 | Candidate/replay/evidence handoff와 schema owner gate에 무엇이 있고 없어야 하는가? | `PHASE06_07_HANDOFF_SCHEMA_RECEIPT` + accepted Phase 06 receipt |
| 9 | Canonical Phase 07 전체 | Exact proposed types, WP, tests, commands와 blockers는 무엇인가? | §4 entry, §8 contracts, §10 tests, §11 WP |
| 10 | Phase 07 review 전체 | 어떤 correction이 적용됐고 무엇이 residual blocker인가? | F-P07-001~008, 특히 F-P07-005 |
| 11 | Actual Phase 08 §16.1 | 세 output variant가 손실 없이 소비되는가? | `Publishable`, `VerificationRejected`, `GateIncomplete` |
| 12 | Dated Domain/Architecture의 대응 section | Current canonical과 어떤 semantic 차이가 있는가? | 상세 누락만 cross-check; conflict resolver로 승격 금지 |
| 13 | Execution Progress current Phase 00 section | 누가 상태를 바꿀 수 있고 현재 구현 acceptance는 무엇인가? | Fix01 진행, receipt 부재; Phase 07 `BLOCKED_NOT_IMPLEMENTED` |

각 문서가 존재하거나 review됐다는 사실은 Java 구현과 evidence가 존재한다는 뜻이 아니다.

## 4. RPDPTW primer, 용어집과 불변조건

### 4.1 CVRPTW의 customer와 RPDPTW의 request

CVRPTW에서는 customer visit 하나가 제거·삽입 단위인 경우가 많다. RPDPTW에서는 `Request`가 pickup과 delivery 의미를 함께 소유한다.

| 용어 | 의미 | 섞으면 생기는 결함 |
|---|---|---|
| `Order` | 외부 business 입력 | Solver pair identity와 같다고 가정 |
| `Request` | 원자 운송 업무, pair ownership 단위 | Pickup node 하나로 축소 |
| `SolverNodeId` | Terminal/pickup/delivery service 정의 | Physical location과 합침 |
| `PhysicalLocationId` | Directed travel endpoint | 같은 location의 서로 다른 service node를 합침 |
| `Visit` | Route 안에서 node가 나타난 occurrence | Node 정의와 동일시 |
| `Route` | Concrete vehicle + terminal policy + ordered visits | Request 집합이나 vehicle class로 축소 |
| `SearchRequestBank` | Search 중 route 밖인 request ID 집합 | Final `UNASSIGNED` status/reason 저장소로 사용 |

Real pickup-delivery는 pickup에서 load가 증가하고 delivery에서 감소한다. Delivery-only는 route 출발 전 initial load를 소유하며 logical pickup이 가짜 travel/service visit을 만들지 않는다. 두 pattern은 같은 single-trip route에 섞일 수 있다.

### 4.2 Stable identity와 authority

Phase 07이 확인해야 할 identity는 “ID 문자열이 같다”보다 강하다.

```text
declared identity
+ intrinsic artifact fingerprint
+ content digest / canonical bytes
+ contract/version
= 같은 authority라고 판단할 최소 조건
```

Problem, travel, profile, evaluation declaration와 `SolvePlan`이 candidate와 replay에서 모두 같은지 계산 전에 확인한다. 같은 ID인데 bytes가 다르면 `CORRUPT`다. 값이 비슷한 다른 profile이나 “latest” snapshot을 찾아 보완하지 않는다.

Semantic fingerprint에는 stable field order와 version을 사용한다. Object identity, file path, provider locator, `toString()`, default locale/timezone, map insertion order, wall-clock과 thread completion order는 넣지 않는다.

### 4.3 Search lifecycle와 result lifecycle

| Artifact/state | Mutable인가 | Authority가 되는 시점 |
|---|---:|---|
| `TrialDraft` | 예 | 되지 않음; Phase 06 call-local |
| `SearchSnapshot` | 아니오 | Full evaluation과 stable invariants 뒤 search authority |
| `CommittedCandidate` projection | 아니오 | Phase 06 accepted handoff이지만 아직 검증되지 않음 |
| `VerifiedSolution` | 아니오 | Candidate verifier `PASS` 뒤에만 생성 |
| Preliminary outcome draft | 생성 중에만 | Final audit/result verifier 전에는 publishable하지 않음 |
| `FinalInsertionAuditRecord` | 아니오 | Required set의 complete audit와 authority check 뒤 |
| `FinalResultDraft` | 아니오 | 아직 result verifier 전 |
| `PublishableResult` | 아니오 | Candidate PASS와 result PASS 둘 다 존재할 때 |
| `VerificationRejected` | 아니오 | 완료된 semantic gate의 rejection |
| `GateIncomplete` | 아니오 | Required gate가 끝나지 못한 상태; disposition을 만들지 않음 |

`VerifiedSolution`이 final result와 같지 않고, `FinalResultDraft`가 publishable result와 같지 않다는 점을 type으로 드러내야 한다.

### 4.4 Stable route와 request partition 불변조건

모든 request는 candidate에서 다음 둘 중 정확히 하나다.

```text
완전한 same-vehicle route pair
XOR
SearchRequestBank membership
```

동시에 지켜야 할 조건:

1. Real pickup과 delivery는 같은 concrete input vehicle route에 있다.
2. 필요한 visit은 각각 정확히 한 번이다.
3. Pickup position은 delivery position보다 앞선다.
4. Delivery-only logical pickup은 initial-load ownership이며 physical visit을 만들지 않는다.
5. Route는 terminal/service pattern과 prepared directed travel을 지킨다.
6. 모든 load prefix가 weight/volume capacity 범위에 있다.
7. Time window, service, work-window, stop/drive/resource hard constraint를 지킨다.
8. Runtime travel fallback이나 fleet 밖 vehicle을 만들지 않는다.

Partial/duplicate/split pair, route+bank duplicate/omission은 정상 hard infeasibility나 `UNASSIGNED` 사유가 아니라 structural defect다.

### 4.5 Candidate disposition과 final outcome을 구분하기

| 개념 | 의미 | 정상 결과 payload인가 |
|---|---|---:|
| `FEASIBLE` | Authority/structure가 유효하고 재계산 및 claim parity 통과 | Candidate PASS로 갈 수 있음 |
| `INFEASIBLE` | Well-formed route가 authoritative hard constraint에 걸림 | 아니오 |
| `INVALID` | Domain/state/contract가 성립하지 않거나 checked 계산 불가 | 아니오 |
| `CORRUPT` | Identity, bytes, provenance, claim이 authority/recomputation과 불일치 | 아니오 |
| `ASSIGNED` | Verified input vehicle route가 request를 정확히 소유 | Final outcome |
| `UNASSIGNED` | Verified route 어느 곳에도 request가 없음 | Final outcome |
| `GateIncomplete` | Audit/result gate의 required work가 완료되지 않음 | 아니오; disposition 아님 |

Fail-closed 우선순위는 다음과 같다.

```text
content/digest/authority mismatch → CORRUPT
well-digested state violation     → INVALID
well-formed hard rejection        → INFEASIBLE
all checks and claim parity       → FEASIBLE
```

`INFEASIBLE` candidate를 모든 request의 `UNASSIGNED` 결과로 바꾸면 안 된다. `UNASSIGNED`는 이미 candidate 전체가 `FEASIBLE`로 검증된 solution 안의 정상 request outcome이다.

### 4.6 Final audit와 diagnostic confidence

Static precheck가 search와 무관하게 불가능성을 증명한 request만 `PROVEN`으로 audit를 생략할 수 있다. 나머지 unassigned request는 final routes를 고정하고 모든 eligible concrete vehicle과 모든 legal pickup/delivery position pair를 검사한다.

| Evidence source | 최대 confidence | 주장하면 안 되는 것 |
|---|---|---|
| Independent static precheck | `PROVEN` | Search 실패를 static proof로 승격 |
| Complete fixed-final-route audit | `EXHAUSTIVE_FOR_FINAL_SOLUTION` | 전역 재배치 불가능/optimality |
| Bounded search observation | `OBSERVED_DURING_SEARCH` | Exhaustive/proven |
| 독립 evidence 없음 | `UNKNOWN` | Bank의 last failure를 final reason으로 사용 |

Feasible insertion을 찾더라도 route를 수정하거나 solver를 재호출하지 않는다. Internal witness는 audit record에만 보존하고 outcome은 `UNASSIGNED`일 수 있다. 이것은 “좋은 해를 일부러 나쁘게 둔다”가 아니라 finalization을 검증 단계로 유지하고 hidden repair loop를 만들지 않는 계약이다.

## 5. 시작 전 entry gate와 사람 승인

### 5.1 현재 entry gate 판정

현재는 **구현 시작 차단**이다.

| Entry 항목 | 필요한 evidence | 현재 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Reactor, wrapper, verification/solver 분리, `E-P00-ARCH` | Executable wrapper/reactor와 Fix01 source/evidence path는 있으나 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING / NOT_ACCEPTED`, receipt `NOT_PRODUCED` | BLOCKED |
| Phase 01 accepted | Canonical request universe와 numeric/time/service identity | Target artifact/evidence 없음 | BLOCKED |
| Phase 02 accepted | `ProblemInstance`, `PreparedTravel`, exact identity/content equality | Target artifact/evidence 없음 | BLOCKED |
| Phase 03 accepted | Cache-free route **및 solution-level** evaluation/comparator/failure contract | Route-level 설계만 있고 review residual gap | BLOCKED |
| Phase 04 accepted | Exact `BoundProfile`, evaluation declaration와 `SolvePlan` | Target artifact/evidence 없음 | BLOCKED |
| Phase 05 accepted | Stable pair/bank와 bound exact pair insertion authority | Target artifact/evidence 없음 | BLOCKED |
| Phase 06 accepted | Candidate/replay/evidence projection과 termination lineage | `NOT_STARTED/NOT_PRODUCED` | BLOCKED |
| Phase 06→07 schema accepted | Core-owned immutable vocabulary 또는 neutral versioned artifact 중 승인된 한 owner와 producer/consumer mapper | Current Phase 06 guide에서 `PHASE06_07_HANDOFF_SCHEMA_DECISION — BLOCKED`; default 없음 | CONTRACT GATE |
| Independent oracle review | Production helper를 쓰지 않는 source/bytecode와 sensitivity plan | Test/oracle source 없음 | REVIEW REQUIRED |
| Canonical result encoding review | Version, field inclusion/exclusion, order, duplicate/absent rule | Public schema/hash policy open | CONTRACT GATE |
| Scheduler/roles | Exact implementation task, implementer/oracle/reviewer 분리 | Phase 07 실제 구현 task 미제공; Phase 00 Fix01 review와 분리 | OWNER GATE |

### 5.2 Residual cross-phase blocker

Phase 07 review의 남은 핵심 finding은 `checkedFullSolutionRecompute(verifiedRoutes, bank)`를 소유할 accepted solution-level evaluation 계약이 없다는 점이다. Current Phase 06 guide가 추가로 드러낸 Phase 06→07 handoff schema owner도 별도 cross-Phase gate다. 이 correction은 둘 중 어느 owner/API도 임의로 확정하지 않는다.

사람이 승인해야 할 최소 항목:

1. Owner module/package는 어디인가?
2. Input은 exact problem/travel/profile, ordered routes, bank 중 무엇인가?
3. Route artifact를 재사용할 수 있는 조건과 invalidation rule은 무엇인가?
4. Hard/metric/score/objective aggregation에서 checked arithmetic과 failure 우선순위는 무엇인가?
5. Solution fingerprint가 어떤 contract/version/authority를 포함하는가?
6. Comparator는 어떤 artifact를 받고 tie를 어떻게 처리하는가?
7. Phase 03~05/07이 같은 API/identity를 사용하는지 one-field corruption으로 어떻게 증명하는가?

Handoff schema는 다음 둘 중 review가 승인한 정확히 하나여야 한다.

1. `rpdptw-core`가 최소 versioned immutable candidate vocabulary를 소유하고 Phase 06 producer와 Phase 07 consumer mapper가 공유한다.
2. 승인된 neutral schema artifact가 canonical bytes/version/digest를 소유하고 Phase 06 exporter와 Phase 07 independent decoder가 연결된다.

두 대안 모두 owner/path, required/optional field, stable order, unknown/missing/duplicate policy, termination/last-safe mapping, claim 비권위성과 corruption test가 receipt에 있어야 한다. 답이 없으면 solver internal type을 import하거나 Phase 07 proposed type을 Phase 06으로 pull-forward하지 않고, route metric을 임의로 더해 `SolutionEvaluationArtifact`를 만들지도 않는다. Last safe point는 accepted route-level kernel과 publication 불가 상태다.

### 5.3 코드 변경 전 checkpoint

다음 receipt를 사람이 한 번에 읽을 수 있는 표로 준비하고 reviewer 승인을 받아야 한다.

```text
entry receipt
  accepted Phase 00~06 evidence/review/receipt refs
  accepted Phase06→07 schema owner/version/producer/consumer mapping receipt
  exact problem/travel/profile/evaluation/SolvePlan identities
  candidate projection schema and content coverage
  accepted solution-level evaluation contract identity
  accepted insertion evaluator authority identity
  oracle owner/reviewer independence
  canonical encoding inclusion/exclusion review
  explicit OPEN/GATED/deferred snapshot
  implementation task and rollback point
```

하나라도 없으면 할 수 있는 일은 문서 review, fixture 설계, interface proposal까지다. Production source, status 승격과 evidence claim은 시작하지 않는다.

### 5.4 Stop, resume와 마지막 안전 지점

| Stop 신호 | 즉시 할 일 | Last safe point | Resume 조건 |
|---|---|---|---|
| Authority ref/digest mismatch | Evaluation 전 중단, typed corruption receipt | Accepted Phase 06 bundle 원본 | Upstream이 새 accepted bundle 발행 |
| Solver/search/cache type가 verification API에 필요 | Dependency 설계 중단 | Core-only proposed API | Phase 00/03/06/07 cross-phase review |
| Solution evaluator owner/API 미정 | WP-07.1 중단 | Route-level kernel | F-P07-005 restart condition 승인 |
| Caller callback이 audit request에 필요 | Audit 구현 중단 | Bound authority data projection | Phase 05 composition/authority review |
| Canonical encoding rule 미승인 | WP-07.4 중단 | Typed outcomes + complete audit | ADR-002 또는 동등 approval |
| Audit interruption/누락 | Publish 차단 | `VerifiedSolution` + last safe audit identity | Complete rerun; disposition 합성 금지 |
| Result verifier incomplete/fail | Publish 차단 | Unpublished immutable draft | 원인 교정 후 새 complete gate |
| Evidence/review 불완전 | 최대 `IMPLEMENTED_PENDING_EVIDENCE` | Last green WP artifact | Immutable bundle + independent review + receipt |

Rollback은 upstream accepted artifact를 수정하거나 evidence를 삭제하는 일이 아니다. Phase-local unpublished artifact를 버리고 마지막 accepted predecessor/green WP identity로 돌아간다.

## 6. 실제 repository inventory: 현재와 목표

### 6.1 재현 가능한 관찰

권위 문서와 tracked baseline의 기준은 commit `7cc890ee1d0805df5ae14b633127fade4f978639`, branch `codex-implementation`이다. 아래 live snapshot은 `2026-07-29T02:22:50+09:00`에 current working bytes를 관찰한 것이다. Shared checkout의 Phase 00 Fix01이 계속 움직일 수 있으므로 **HEAD baseline**, **live unaccepted snapshot**, **accepted receipt**를 서로 다른 열로 읽는다. 기존 변경을 되돌리거나 live 파일/`target`을 Phase 07 완료 evidence로 사용하지 않는다.

```bash
git status --short
rg --files .mvn build legacy rpdptw
rg --files -g 'pom.xml'
rg -n '<module>' pom.xml build/**/pom.xml legacy/**/pom.xml rpdptw/**/pom.xml
rg -n 'CandidateVerifier|VerifiedSolution|PublishableResult|ResultIntegrityVerifier' \
  build legacy rpdptw pom.xml
shasum -a 256 mvnw .mvn/wrapper/maven-wrapper.properties pom.xml \
  rpdptw/verification/pom.xml build/test-fixtures/pom.xml \
  docs/implementation/execution-progress-and-results.md
java -version
mvn -version
```

초기 HEAD/tracked baseline 관찰:

- OpenJDK Corretto `25.0.3`
- Maven `3.9.14`
- Root `pom.xml` 하나, `<module>` 0개
- Main Java 6개, test Java 1개
- `rpdptw/verification` 경로와 Phase 07 named type 0개

현재 미승인 live worktree:

- Project POM은 root를 포함해 13개이고, root는 `packaging=pom`이며 `rpdptw`, `build`, `legacy`를 aggregate한다.
- Executable `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`가 존재한다. Wrapper properties는 `only-script`, Maven `3.9.14`, distribution SHA-256 `55fadd669532a3205d5db95f490bf13971d8b0843526f407f29db0e61f074ab3`을 선언한다.
- `rpdptw` main Java 23개는 모두 `package-info.java`다. Verification의 Phase 07 named production/test type은 0개다.
- `build` test Java는 11개다: architecture-rules 아래 9개와 test-fixtures 아래 2개다. 이 중 `Phase07VerificationArchitectureTest`와 Phase 07 fixture/oracle은 0개다.
- [`rpdptw/verification/pom.xml`](../../../../rpdptw/verification/pom.xml)은 `rpdptw-core` compile dependency만 선언한다. Test-fixtures test-jar, JUnit과 JUnit Platform direct test dependency는 아직 없다.
- [`build/test-fixtures/pom.xml`](../../../../build/test-fixtures/pom.xml)은 `src/test/java`를 `tests` classifier의 test-jar로 `test` phase에 attach한다. 이는 artifact wiring이지 Phase 07 fixture/oracle 존재나 acceptance가 아니다.
- Root Surefire `3.5.4`는 `failIfNoTests=false`다. Maven Failsafe plugin/version/execution은 없다. 이 live 기본값은 Phase 07 required-set의 zero-test를 막지 않는다.
- `legacy/gcp-placeholder`에는 main Java 10개, test Java 5개가 있다. 이 legacy inventory는 Phase 07 authority가 아니다.
- `target/phase-00-evidence`에 102개 파일이 보이지만 progress는 이전 defective bundle SHA-256 `acdac94dc2071744320b154ab88c2ab1ac1087858d9eabc39fe63e78759501ee`를 superseded로 표시한다. 현재 status는 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING / NOT_ACCEPTED`, acceptance receipt는 `NOT_PRODUCED`다.
- `E-P07-*`는 없고 Phase 07 status는 `BLOCKED_NOT_IMPLEMENTED`다.

Live fingerprint:

| Live file | SHA-256 | Git hash-object | 의미 |
|---|---|---|---|
| `mvnw` | `cae96cef89ebea3531221f4ae17c23cf8edf67d00eae8306d4186ae1bbed4d02` | N/A executable live file | 실행 가능, 미승인 |
| `.mvn/wrapper/maven-wrapper.properties` | `7613cd8a2f64216deb6c8f8443f2b339f2305d9b30fc3a4ea3088820a4788871` | N/A untracked live file | distribution declaration, 미승인 |
| `pom.xml` | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` | `1dc675ba17b7f2202f34a22131f152cc2868b075` | HEAD와 다른 live reactor |
| `rpdptw/verification/pom.xml` | `1a066e9bfa77f87ff5986e05e9847d5b5b1f8b30d2188de55e4913f6571e4ce7` | `4a61586ff0d40c566c802799c5d458f61f028d6e` | core-only dependency |
| `build/test-fixtures/pom.xml` | `65a9fae30174778ade49302b2e8525419955792437498a7dd5be6649888aad71` | `e033f9cda6de908a25431b6dfce5b0b0d8cd3ac1` | tests-classifier producer |
| `execution-progress-and-results.md` | `5696065eb923c50c00e6fa7d5ed2e13c30894e77fcc7a8009fcd36b5d2d8e7cd` | `f875edbdeb79fb18710421b56e126462a4d6d38c` | live scheduler status snapshot |

Live worktree는 검증 중에도 바뀔 수 있다. 위 timestamp 이후 POM/wrapper/progress/test count/hash 중 하나라도 다르면 Phase 07 entry를 중단하고 새 snapshot을 만든다. 새 snapshot도 accepted Phase 00 commit, immutable bundle, independent review와 valid receipt를 대신하지 않는다.

### 6.2 존재, placeholder와 부재

| 분류 | 실제 path/type | 현재 사실 | Phase 07 목표와 차이 |
|---|---|---|---|
| live 미승인 | [`pom.xml`](../../../../pom.xml) | Modified 13-POM parent/reactor, Surefire no-test 허용, Failsafe 없음 | Accepted Phase 00 receipt와 Phase 07 strict test policy 필요 |
| live 미승인 | `mvnw`, `mvnw.cmd`, wrapper properties | `mvnw` executable, exact hash 고정됨 | 파일 존재/실행 가능과 acceptance command 권위 분리 |
| live 미승인 | [`rpdptw/verification/pom.xml`](../../../../rpdptw/verification/pom.xml) | Core dependency와 package-info placeholder만 관찰 | Phase 07 production/test contract와 direct test deps 없음 |
| live 미승인 | [`build/test-fixtures/pom.xml`](../../../../build/test-fixtures/pom.xml) | `tests` classifier attach, Phase 07 fixture 0 | Phase 07 builder/oracle/independence test 없음 |
| live 미승인 | [`build/architecture-rules/pom.xml`](../../../../build/architecture-rules/pom.xml) | Architecture Java 9개와 fixture test-jar consumer | `Phase07VerificationArchitectureTest` 없음 |
| live 미승인 | `target/phase-00-evidence` | 102 files; prior bundle rejected/superseded, Fix01 review pending | Accepted receipt나 `E-P07-*`가 아님 |
| 존재/placeholder | [`AlnsBatchEngine.java`](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java) | `double` synthetic objective와 `Map<String,Object>` candidate | Pair/route/travel/profile/replay/verifier가 아님 |
| 존재/placeholder | [`OptimizationWorkerController.java`](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java) | GCS prefix list 뒤 최소 objective candidate를 고르고 바로 result 저장 | Declared completeness, candidate/result verifier, CAS/publication gate 없음 |
| 존재/placeholder | [`AlnsBatchEngineTest.java`](../../../../legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java) | Status/run number/positive objective만 검사 | Phase 07 corruption/audit/result evidence가 아님 |
| 부재 | `CandidateVerifier`/`VerifiedSolution` production type | Named type 없음 | WP-07.0~2 대상 |
| 부재 | `FinalOutcome`/audit/result verifier production type | Named type 없음 | WP-07.3~5 대상 |
| 부재 | Phase 07 oracle/architecture test | Named test 없음 | WP-07.2/6 대상 |
| 부재 | `E-P07-*` | Evidence 없음 | WP-07.6 뒤에만 생성 가능 |

HEAD baseline fingerprint:

| File | Git blob | SHA-256 |
|---|---|---|
| `pom.xml` | `f8a411eadd4a5c01d8dd09fdea462738ca63d65f` | `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6` |
| `AlnsBatchEngine.java` | `18bfb344faaefe9373e02b007446e7bf15c3443c` | `4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481` |
| `OptimizationWorkerController.java` | `0ccd699cc9bf178ccb51de488a899af166b82fa2` | `846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c` |
| `AlnsBatchEngineTest.java` | `a1bbb8cc7397ce5a69dd78d2c07ca44b61dee67e` | `947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829` |

위 blob은 HEAD의 원래 `src/...` path bytes다. 현재 `legacy/gcp-placeholder/...`로 이동 중인 세 placeholder bytes는 이 관찰 시 같은 hash였지만, untracked/modified concurrent scaffold의 acceptance identity가 아니다. Scaffold는 다른 작업자가 계속 변경할 수 있으므로 Phase 07 entry에서 accepted commit/blob/evidence를 새로 받아야 한다.

### 6.3 현재 가능한 명령과 future 명령

`./mvnw`는 현재 executable이므로 명령을 시작할 수 있다. 그러나 이 사실은 wrapper/reactor가 accepted됐거나 그 출력이 `E-P07-*`가 된다는 뜻이 아니다. Empty/package-info-only verification module은 root의 `failIfNoTests=false` 때문에 green일 수 있다. 이 correction은 code/POM/test를 소유하지 않으므로 live build를 Phase 07 acceptance 목적으로 실행하지 않는다.

Phase 00 acceptance와 Phase 07 source/test가 존재한 뒤에는 다음 **fresh-reactor 절차**를 exact source identity와 연결한다. `<isolated-repo>`와 `<sealed-report-dir>`은 run receipt에 절대경로와 digest를 기록하는 새 빈 위치다.

```bash
# 1. 필터 없이 upstream test-jar까지 same-source artifact로 설치한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-repo> \
  -pl rpdptw/verification -am clean install

# 2. 위 install artifact digest를 고정한 뒤 selected test는 -am 없이 실행한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-repo> \
  -f rpdptw/verification/pom.xml \
  -Dtest=CandidateVerifierAuthorityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

# 3. Module closure와 architecture/full reactor는 filter 없이 clean한다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-repo> \
  -pl rpdptw/verification -am clean verify
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-repo> \
  -pl build/architecture-rules -am clean verify
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=<isolated-repo> clean verify
```

Future POM은 verification module의 Surefire에 `failIfNoTests=true`를 명시하고 selected command에 `failIfNoSpecifiedTests=true`를 강제한다. Maven Failsafe plugin/version을 root policy에 pin하고 `integration-test`와 `verify` goal을 bind한다. Canonical 69개는 현재 모두 `*Test`이므로 required Failsafe method 수는 0이다. 승인된 target-additional `*IT` manifest가 생길 때만 `-Dit.test=<IT> -Dfailsafe.failIfNoSpecifiedTests=true`로 선택 실행하며, Failsafe report도 complete/fresh해야 한다.

각 `clean`은 이전 `target/surefire-reports`와 `target/failsafe-reports`를 지운다. 따라서 다음 clean 전에 report/XML/console/build-info/dependency tree를 `<sealed-report-dir>`에 content-addressed copy하고 source/build/run ID, 시작·종료 시각과 digest를 함께 봉인한다. Report source/run ID가 다르거나 run 시작 전 mtime, empty required set, missing XML, duplicate shard, zero discovered, failed/error/skipped/aborted가 하나라도 있으면 stale/zero-test false-green으로 거부한다. Selected filter와 `-am`을 함께 써 upstream module에 nonmatching filter를 전파하지 않는다.

## 7. Scope, non-scope와 결정 상태

### 7.1 Phase 07이 구현하는 것

- Phase 06 candidate/replay/evidence의 immutable core-only projection
- Problem/travel/profile/evaluation/`SolvePlan`/candidate/replay identity와 content equality
- Solver/search/cache dependency 없는 candidate verifier
- Pair/route/bank/terminal/travel/load/time/resource/evaluation의 cache-free 재계산
- `FEASIBLE/INFEASIBLE/INVALID/CORRUPT` fail-closed 분류
- Candidate PASS-only `VerifiedSolution`
- Exactly-one preliminary/final `ASSIGNED/UNASSIGNED`
- Static `PROVEN`을 제외한 final-route exhaustive insertion audit
- Evidence-bounded diagnostic
- Outcome-derived checked summary와 deterministic semantic/payload identity
- Result-integrity verifier
- `Publishable`/`VerificationRejected`/`GateIncomplete` typed output
- Independent oracle, malicious/corruption/property/replay/architecture evidence

### 7.2 Phase 07이 구현하지 않는 것

- ALNS, COW mutation, candidate 선택, replay 실행
- Invalid candidate repair, feasible audit witness auto-insert, re-solve, fallback candidate 선택
- Application use case, filesystem, object storage, CAS, result publication/retrieval/status
- Coordinator, AWS/GCP provider runtime, HTTP/wire API, deployment
- Phase 13 route pool/MIP/backend
- Phase 14 benchmark threshold, official manifest, baseline, cutover
- External JSON/media type와 long-term public compatibility
- Official step/worker/round/watchdog 값
- Multi-trip/rotation과 optional variants
- Raw input reparse, travel regeneration, `latest` profile/config lookup

### 7.3 확정된 의미

| 항목 | 상태 | 구현자가 지킬 것 |
|---|---|---|
| Candidate → finalization → result verifier 순서 | FIXED | 두 gate를 합치거나 건너뛰지 않음 |
| Same immutable authority | FIXED | Problem/travel/profile/evaluation/`SolvePlan` exact equality |
| Search cache/claim 비권위 | FIXED | 재계산 후 claim mismatch는 corruption |
| Solver final outcome | FIXED | `ASSIGNED`/`UNASSIGNED`만 |
| Required final audit | FIXED | Static `PROVEN` 외 전수 |
| Internal result summary/comparator | FIXED | Exact 8 fields, operational-time formula와 four-field lex order/equality |
| Failure normal payload | FIXED PROHIBITED | Rejection/incomplete에 route/outcome/benchmark payload 0 |
| Phase 07 PASS 의미 | FIXED | Correctness/integrity일 뿐 quality/performance/official 아님 |

### 7.4 Proposed, OPEN, GATED와 deferred

| 항목 | 상태 | 사람 승인 전 처리 |
|---|---|---|
| Java type/package 이름 | PROPOSED/OPEN | Semantic shape만 사용; public compatibility 약속 금지 |
| Verification disposition enum 이름 | PROPOSED INTERNAL | 의미와 fail-closed mapping을 우선 |
| Canonical result encoding/public schema | OPEN/PROPOSED | Version/inclusion/order/security ADR 전 WP-07.4 stop |
| Fingerprint algorithm/version | OPEN/PROPOSED | Upstream declared version 소비; hidden SHA default 금지 |
| `Q-BENCH-02` official 수치 | OPEN — EXPERIMENT_REQUIRED | Explicit test/experiment value만; Phase 14 gate 보존 |
| Phase 13 route pool/MIP | C-17 GATED TARGET | Phase 14A receipt와 별도 승인 전 dependency/activation 금지 |
| Phase 12 provider adoption | APPROVAL-GATED | Phase 07 pure contract에 provider를 끌어오지 않음 |
| `Q-VAR-01` | DEFERRED | 질문·구현·활성화 금지 |
| Multi-trip/rotation | DEFERRED FEATURE | Current single-trip contract 유지 |
| Phase 14B production authority | NOT GRANTED/GATED | Test/staging/Phase 07 PASS를 production으로 표시 금지 |

`Q-INFRA-01`의 AWS target은 `RESOLVED`지만 Phase 07 dependency나 restart 조건이 아니다.

### 7.5 사람 승인 질문

구현자는 첫 source change 전에 다음 질문에 답하고 reviewer에게 보여준다.

- 이 request type이 solver-owned class나 executable callback을 운반하는가?
- Solution-level evaluation owner/API가 accepted됐는가?
- Candidate projection digest가 contract, 다섯 authority ref, routes, bank, claims를 모두 덮는가?
- `INVALID`와 `INFEASIBLE`, semantic rejection과 incomplete가 분리되는가?
- Diagnostic confidence가 evidence보다 강한 말을 하지 않는가?
- Canonical field order와 제외 field가 승인됐는가?
- Phase 08이 세 output variant를 exhaustive하게 매핑하는가?
- 새 값이 `Q-BENCH-02`, C-17, public schema/hash policy를 몰래 닫지 않는가?

## 8. 학습 경로

### 8.1 1단계 — 개념을 말로 설명하기

먼저 코드 없이 다음 문장을 자신의 말로 설명한다.

1. Search bank와 final `UNASSIGNED`는 왜 다른가?
2. Candidate verifier와 result verifier는 각각 어떤 오류를 잡는가?
3. 같은 immutable authority를 쓰면서도 왜 independent verification인가?
4. `INFEASIBLE`, `INVALID`, `CORRUPT`, `GateIncomplete`는 어떻게 다른가?
5. Fixed-final-route exhaustive audit가 global infeasibility proof가 아닌 이유는 무엇인가?

완료 신호: 한 candidate corruption 예와 한 result corruption 예를 들고, 어느 gate에서 어떤 typed rejection이 나와야 하는지 설명할 수 있다.

자문 질문: “이 값을 solver cache 없이도 authority에서 다시 만들 수 있는가?”

### 8.2 2단계 — 작은 손 탐색

`P07_TINY_PD_3_TEST_ONLY` 같은 아주 작은 integer fixture를 종이에 만든다.

- Real pair 1개, delivery-only 1개, unassigned 1개
- `DIRECT` vehicle 1개, `LEASE` vehicle 1개
- Asymmetric directed travel
- Stable route 두 개와 bank 하나
- 한 request의 legal pickup/delivery position 전체

직접 계산할 것:

1. Request universe와 route/bank XOR
2. Delivery-only initial load와 real pair prefix load
3. Route별 distance/time/resource
4. Candidate claim과 독립 계산 tuple
5. Preliminary outcome
6. Eligible vehicle × legal position pair option set
7. Audit rejection count/feasible witness
8. Final outcome bijection과 summary
9. Stable field order의 expected bytes 또는 명시 tuple

완료 신호: Production builder/kernel/encoder 없이 expected 값을 설명하고 한 field tamper가 어느 fingerprint/digest를 바꾸는지 표시할 수 있다.

자문 질문: “Expected 값을 production helper로 만들고 있지 않은가?”

### 8.3 3단계 — 실제 변경을 작은 vertical slice로 만들기

Entry gate가 열린 뒤 다음 순서로만 넓힌다.

```text
architecture forbidden edge red
→ authority mismatch red
→ partition corruption red
→ cache-free feasible candidate green
→ PASS-only VerifiedSolution
→ complete audit
→ outcomes/summary/encoding
→ result corruption red
→ both-gate publishable green
```

각 단계에서 immutable input/output과 exact failure를 먼저 고정한다. Facade부터 만들어 내부를 mock해 green으로 만드는 방식은 false-green이다.

완료 신호: 각 vertical slice가 독립 oracle의 red를 먼저 만들고, 최소 production 구현으로 green이 되며, 이전 corruption test가 계속 green이다.

자문 질문: “이 green은 구현이 맞아서인가, test가 같은 helper를 공유해서인가?”

### 8.4 4단계 — 통합과 handoff

Phase 06 accepted candidate/replay/evidence를 실제 projection으로 받아 full module suite를 실행한다. Phase 08 compatibility test는 세 variant를 모두 소비하고 정상 payload가 failure 경로에 없는지 검사한다.

완료 신호:

- Solver/search/cache/provider dependency 0
- Candidate/result gate required tests의 missing/failed/error/skipped 0
- Oracle independence와 seeded defect sensitivity green
- Same fixed envelope의 verified solution/outcome/result fingerprint exact equality
- `E-P07-*` immutable bundle, independent review, acceptance receipt
- Phase 08 handoff manifest와 rollback point

자문 질문: “지금 가진 것은 implementation green인가, immutable accepted evidence인가?”

## 9. Proposed Java 설계 안내

### 9.1 예상 module, package와 file

아래는 Phase 00 reactor/ADR 승인 뒤의 **proposed target**이다.

```text
rpdptw/verification/
├── pom.xml
├── src/main/java/com/ronext/rpdptw/
│   ├── verification/api/
│   │   ├── CandidateVerifier.java
│   │   ├── CandidateVerificationRequest.java
│   │   ├── CandidateVerification.java
│   │   ├── VerificationDisposition.java
│   │   └── VerifiedSolution.java
│   ├── verification/candidate/internal/
│   │   ├── CacheFreeCandidateVerifier.java
│   │   ├── CandidateAuthorityValidator.java
│   │   ├── CandidatePartitionValidator.java
│   │   └── CandidateClaimComparator.java
│   ├── result/api/
│   │   ├── Phase07FinalResultService.java
│   │   ├── Phase07Output.java
│   │   ├── FinalOutcome.java
│   │   ├── FinalResultManifest.java
│   │   ├── PublishableResult.java
│   │   └── FinalResultRejection.java
│   ├── result/finalization/
│   │   ├── FinalInsertionAuditor.java
│   │   ├── FinalInsertionAuditRecord.java
│   │   └── internal/ExhaustiveFinalInsertionAuditor.java
│   ├── result/encoding/
│   │   ├── CanonicalResultEncoder.java
│   │   └── CanonicalResultPayload.java
│   └── verification/result/
│       ├── ResultIntegrityVerifier.java
│       └── ResultVerification.java
└── src/test/java/com/ronext/rpdptw/...

build/test-fixtures/src/test/java/com/ronext/rpdptw/testing/phase07/...
build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/
  Phase07VerificationArchitectureTest.java
```

이름은 proposed다. 위 tree는 완성 source 목록이 아니라 semantic placement다. `verification`, `finalization`, `result verification` owner와 dependency 방향은 보존하고, snippet에 나온 이름을 임의 package/raw map/solver type로 메우지 않는다. Review 단계의 compile closure는 다음 owner 표와 POM edge로 닫는다. Exact 이름이나 public visibility가 승인되지 않은 행은 `PROPOSED`/`OPEN BLOCKER`로 남고 production compile을 시작하지 않는다.

| Type family / snippet reference | Semantic owner와 proposed package | Visibility·immutability | Closure 상태 |
|---|---|---|---|
| `RequestId`, `RouteId`, `VehicleId`, `ProblemInstance`, `PreparedTravel`, route/pair/terminal/service facts | `rpdptw-core`: `domain`, `travel` | Cross-module immutable public vocabulary | Owner FIXED, exact type signature predecessor acceptance 필요 |
| `BoundProfile`, evaluation declaration, `SolvePlan`, `MetricSnapshot`, `ScoreSnapshot`, `ObjectiveVector`, `SolutionEvaluationArtifact` | `rpdptw-core`: `evaluation.api`/`evaluation.runtime` | Immutable evaluated facts; no profile implementation import | Full-solution evaluator/API/identity는 `OPEN BLOCKER` |
| Checked unit values, contract/version, fingerprint/digest/canonical-bytes value objects | `rpdptw-core` data-integrity/evaluation vocabulary 또는 reviewed verification result wrapper | Defensive immutable bytes; algorithm/version explicit | Exact package와 production hash algorithm `PROPOSED/OPEN` |
| `CandidateSnapshot`, `CandidateDeclaredClaims`, `ReplayEvidence`, `Phase06EvidenceReceipt`, `CandidateVerificationContract` | `CandidateSnapshot` 등은 `rpdptw-verification:verification.api`의 internal-consumer shape. Cross-module input owner는 accepted Phase06→07 receipt가 core-owned vocabulary 또는 neutral versioned artifact 중 하나로 결정 | Public immutable Phase 07 input; Phase 07 mapper/decoder 뒤에도 solver/search/cache type 0 | `PHASE06_07_HANDOFF_SCHEMA_DECISION`은 `OPEN BLOCKER`; 두 대안 중 default 없음 |
| `CandidateVerification`, `VerificationDisposition`, `CandidatePassReport`, `VerifiedSolution`, `VerificationFailure` | `rpdptw-verification`: `verification.api` | Downstream boundary만 public; concrete validator/failure construction internal | Semantic owner closed, exact hierarchy `PROPOSED` |
| `FinalOutcome`, `ResultSummary`, `FinalResultManifest`, `PublishableResult`, `Phase07Output`, `FinalResultRejection` | `rpdptw-verification`: `result.api` | Phase 08가 소비하는 immutable public output | Internal semantic fields fixed; external wire/media/hash `OPEN` |
| `FinalInsertionAuditRequest/Result/Record`, static evidence, `BoundInsertionAuthority` | `rpdptw-verification`: `result.finalization`; accepted insertion evaluator contract는 core `evaluation.insertion` | Auditor impl package-private; immutable audit identity/record만 result boundary가 참조 | Callback 없는 owner shape fixed; exact evaluator binding review 필요 |
| `CanonicalResultEncoder`, canonical payload draft | `rpdptw-verification`: `result.encoding` | Encoder internal; defensive immutable payload view만 public result가 참조 | Field/order semantic fixed; external encoding/hash `OPEN` |
| Candidate/result validators와 finalizer implementation | `verification.candidate.internal`, `verification.result.internal`, `result.finalization.internal` | Package-private/internal | Public exposure 금지 |
| Phase 07 builders, hand/exhaustive oracles, seeded faulty doubles | `build/test-fixtures/src/test/java/.../testing/phase07` | Test classifier only; production visibility 0 | Future test-jar edge 필요 |
| Phase 07 contract assertions | `rpdptw/verification/src/test/java` | Test only | Direct JUnit/fixture dependencies 필요 |
| `Phase07VerificationArchitectureTest` | `build/architecture-rules/src/test/java/.../architecture` | Test only, bytecode/dependency closure 검사 | Architecture module owns |

이 표가 닫는 것은 review 입력의 owner/dependency ambiguity다. 실제 production compile closure는 accepted predecessor API와 위 `OPEN BLOCKER` 승인이 들어온 뒤에만 성립한다. 미정 type을 같은 이름의 local stub, `Map<String,Object>`, `Object`, solver import 또는 nullable sentinel로 만들어 compile만 통과시키지 않는다.

### 9.2 Dependency 계약

Production dependency:

```text
rpdptw-verification(main) → rpdptw-core(main)

rpdptw-application(main) → rpdptw-core(main)
rpdptw-application(main) → rpdptw-solver(main)
rpdptw-application(main) → rpdptw-verification(main)
```

Verification의 future direct test dependencies:

```text
com.ronext:rpdptw-test-fixtures:${project.version}:test-jar:tests  scope=test
org.junit.jupiter:junit-jupiter                                  scope=test
org.junit.platform:junit-platform-launcher                      scope=test
```

Launcher를 exact discovery-manifest listener/checker가 쓰지 않는 최종 설계라면 승인된 dependency review로 제거할 수 있다. Local repository에 우연히 있는 fixture artifact를 사용하지 않는다. `rpdptw-verification`의 direct test-jar edge가 reactor에 `core → test-fixtures(test-jar attach/install) → verification test` 순서를 만들고, §6.3의 same-source `-pl rpdptw/verification -am clean install`이 exact artifact를 fresh isolated repository에 공급해야 한다.

Surefire는 `*Test`를, Failsafe는 승인된 `*IT`만 소유한다. Verification module은 required-set이 있으므로 `failIfNoTests=true`; selected run은 해당 plugin의 `failIfNoSpecifiedTests=true`다. Root placeholder module의 zero-test 허용은 Phase 07 test gate에 상속돼서는 안 된다.

금지 edge:

```text
rpdptw-verification -X→ rpdptw-solver
rpdptw-verification -X→ search/cache package
rpdptw-verification -X→ application/adapters/apps
rpdptw-verification -X→ cloud/HTTP/provider SDK
rpdptw-verification -X→ OR-Tools/vendor API
rpdptw-verification -X→ profile implementation JAR
production main       -X→ build/test-fixtures
```

Phase 06 solver type를 직접 import하지 않는 이유가 이 edge를 지키기 위해서다. Accepted schema가 core-owned이면 reviewed composition mapper가 immutable core vocabulary를 넘기고, neutral artifact이면 verification decoder가 version/digest/field policy를 검증한 뒤 internal input을 만든다. 어느 경우에도 solver internal class나 Phase 07 consumer type을 upstream으로 pull-forward하지 않는다. Architecture test는 production output에 fixture classifier reference가 0인지, verification production bytecode가 accepted core/schema edge 외 forbidden module을 참조하지 않는지, verification tests가 declared direct artifact만 쓰는지를 검사한다.

### 9.3 Candidate verifier skeletal contract

다음 코드는 완성 구현이 아니라 review할 shape다.

```java
// PROPOSED INTERNAL API — exact names/public visibility are OPEN.
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
    // Defensive immutable copy; no solver/cache/callback/provider field.
}

public enum VerificationDisposition {
    FEASIBLE, INFEASIBLE, INVALID, CORRUPT
}

public sealed interface CandidateVerification
        permits CandidateVerification.Pass, CandidateVerification.Rejected {
    record Pass(CandidatePassReport report, VerifiedSolution solution)
        implements CandidateVerification {}
    record Rejected(
        VerificationDisposition disposition,
        VerificationFailure failure
    ) implements CandidateVerification {}
}
```

`CandidateSnapshot` 후보는 다음 의미를 담는다.

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
```

`CandidateDeclaredClaims`는 계산 입력이 아니라 재계산 후 검증할 claim이다. 생성자가 모든 collection을 복사하고 순서/중복 규칙을 검증해야 한다.

### 9.4 Verified solution과 failure hierarchy

```java
// PROPOSED. SolutionEvaluationArtifact owner/API is currently BLOCKED.
public record VerifiedSolution(
    VerificationAuthority authority,
    List<VerifiedRoute> routes,
    List<RequestId> unassignedRequestIds,
    SolutionEvaluationArtifact evaluation,
    CandidateFingerprint sourceCandidateFingerprint,
    VerifiedSolutionFingerprint fingerprint
) {}

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
```

`HardInfeasibility`만 `INFEASIBLE`, structure/arithmetic/contract는 `INVALID`, authority/content/claim mismatch는 `CORRUPT`로 매핑한다. Failure 선택 순서는 stable ID/order에 따라 deterministic해야 한다.

### 9.5 Outcome, audit와 diagnostic contract

```java
public sealed interface FinalOutcome
        permits FinalOutcome.Assigned, FinalOutcome.Unassigned {
    RequestId requestId();

    record Assigned(
        RequestId requestId,
        RouteId routeId,
        VehicleId vehicleId,
        RequestAssignmentEvidence evidence,
        VehicleOwnership ownership
    ) implements FinalOutcome {}

    record Unassigned(
        RequestId requestId,
        UnassignedDiagnostic diagnostic
    ) implements FinalOutcome {}
}

public interface FinalInsertionAuditor {
    FinalInsertionAuditResult audit(FinalInsertionAuditRequest request);
}

public record FinalInsertionAuditRequest(
    VerifiedSolution verifiedSolution,
    List<RequestId> preliminaryUnassigned,
    StaticUnassignabilityEvidence staticEvidence,
    BoundInsertionAuthority insertionAuthority,
    AuditContractVersion contractVersion
) {}
```

Request에는 `AtomicPairInsertionEvaluator`, `Comparator`, `Supplier`, script 또는 callback을 넣지 않는다. `BoundInsertionAuthority`는 accepted contract/build/declaration identity를 나타내는 immutable data다. Reviewed composition root가 internal auditor에 exact evaluator implementation을 bind한다.

```java
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
```

Incomplete는 `INVALID`로 바꾸지 않는다.

### 9.6 Result와 top-level output contract

```java
public interface ResultIntegrityVerifier {
    ResultVerification verify(ResultVerificationRequest request);
}

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
        VerificationFailure failure
    ) implements FinalResultRejection {}

    record GateIncomplete(
        RejectionStage stage,
        SafeIncompleteFailure incompleteFailure,
        LastSafeIdentity lastSafeIdentity
    ) implements FinalResultRejection {}
}
```

`VerificationRejected`에는 `FEASIBLE` disposition을 허용하지 않는다. 두 rejection 모두 정상 route/outcome/benchmark payload를 운반하지 않는다.

#### 9.6.1 Exact `ResultSummary` semantic manifest

다음 record는 복사 가능한 완성 구현이 아니라 canonical internal field manifest다. Field 이름·순서·단위와 의미는 finalizer, independent result verifier, oracle, semantic fingerprint가 공통으로 **각자 파생해야 할 계약**이다.

```java
// CANONICAL INTERNAL SEMANTIC MANIFEST; constructor/implementation omitted.
ResultSummary(
    long assignedRequestCount,
    long unassignedRequestCount,
    long dispatchedVehicleCount,
    long totalDirectedDistanceMeters,
    long totalRouteOperationalTimeSeconds,
    MetricSnapshot metrics,
    ScoreSnapshot scores,
    ObjectiveVector objective
)
```

| Field | Exact derivation/invariant | Invalid/failure boundary |
|---|---|---|
| `assignedRequestCount` | Exactly-one final outcome 중 `ASSIGNED` 수 | 음수, request-universe 불일치, duplicate/missing outcome은 `INVALID` |
| `unassignedRequestCount` | Exactly-one final outcome 중 `UNASSIGNED` 수 | `assigned + unassigned`가 input request 수와 checked exact equality가 아니면 `INVALID` |
| `dispatchedVehicleCount` | Accepted input vehicle 중 하나 이상의 assigned request를 가진 used route의 unique concrete vehicle 수 | Unknown/duplicate vehicle ownership 또는 빈 route를 dispatch로 센 경우 `INVALID` |
| `totalDirectedDistanceMeters` | 모든 used route가 실제 순서로 조회한 prepared **directed** arc 거리의 checked sum | Missing/reverse/symmetric/lazy fallback, 음수 또는 overflow는 `INVALID`; clamp/wrap 금지 |
| `totalRouteOperationalTimeSeconds` | 아래 exact component의 used-route checked sum | 제외 항목 포함, missing/negative component, unit mismatch 또는 overflow는 `INVALID` |
| `metrics` | Accepted bound evaluation schema의 stable metric ID/unit/order/value를 routes/outcomes에서 재계산 | Unknown/missing/duplicate dimension, order/unit/value mismatch는 `INVALID` 또는 declared-claim `CORRUPT` |
| `scores` | Accepted bound profile/evaluation declaration의 stable score dimension/order/value | Schema/vector length/order mismatch는 `INVALID`; recompute 뒤 declared mismatch는 `CORRUPT` |
| `objective` | Accepted customer-bound objective vector를 exact ordered dimension으로 재계산 | Reference comparator를 대체하지 않음; schema/order/value mismatch는 위와 동일 |

Operational-time 공식은 다음 하나다.

```text
totalRouteOperationalTimeSeconds =
  checked Σ over used routes (
      driveSeconds
    + customerWaitingSeconds
    + depotWaitingSeconds
    + serviceSeconds
    + interWorkWindowRestSeconds
  )
```

`used route`는 하나 이상의 assigned request를 가진 final route다. Unused vehicle의 idle time, shift 시작 전 unrelated idle/pre-route time, solver/search/replay elapsed, verifier/audit/encoding elapsed, serialization/I/O/network time와 observation wall-clock은 포함하지 않는다. Component는 non-negative integer seconds이고 합산 순서와 무관한 checked exact addition을 사용한다. Internal summary numeric carrier는 integer 또는 승인된 unit-bearing fixed-point뿐이며 `double`/`float`는 semantic carrier가 아니다.

Legacy adaptor, parser 또는 임시 carrier에서 `NaN`, `+Infinity`, `-Infinity`가 들어오면 compare/encode 전에 typed `INVALID(NON_FINITE_NUMERIC)`으로 거부한다. Negative/missing/unit/schema mismatch와 checked overflow도 각각 typed `INVALID`이며 clamp, wrap, maximum sentinel, empty vector 또는 `UNASSIGNED`로 바꾸지 않는다. Accepted recomputation과 candidate/finalizer의 declared summary가 다르면 `CORRUPT(SUMMARY_CLAIM_MISMATCH)`다. Required calculation/audit work를 완료하지 못했으면 `GateIncomplete`이고 fabricated `INVALID` disposition이나 partial summary를 운반하지 않는다. 괄호 안 code spelling은 proposed internal 이름이지만 의미와 분류는 고정한다.

#### 9.6.2 Win/reference comparator와 exact equality

Customer-bound `objective`는 profile이 결정하는 solve-quality vector다. Win/reference comparator는 result/benchmark reference ordering이며 다음 four-tuple을 summary에서만 만든다.

```text
ReferenceVector =
  (
    unassignedRequestCount,
    dispatchedVehicleCount,
    totalDirectedDistanceMeters,
    totalRouteOperationalTimeSeconds
  )
```

두 valid result를 **왼쪽부터 첫 차이에서 ascending** 비교한다.

1. `unassignedRequestCount`가 작은 결과가 낫다.
2. 같으면 `dispatchedVehicleCount`가 작은 결과가 낫다.
3. 같으면 `totalDirectedDistanceMeters`가 작은 결과가 낫다.
4. 같으면 `totalRouteOperationalTimeSeconds`가 작은 결과가 낫다.
5. 네 field가 exact 같으면 `EQUAL`이다.

Epsilon/tolerance, weighted sum, subtraction 기반 comparator, locale/map iteration order 또는 fifth structural tie-break를 넣지 않는다. Stable result fingerprint/bytes로 구조적 순서를 만들 수는 있지만 그것은 quality 우열이 아니다. Invalid/non-finite/incomplete/corrupt input은 `NOT_COMPARABLE`이며 comparator가 정상 순서나 equality를 반환해서는 안 된다. Count/distance/time은 범위 검사를 마친 non-negative integer를 `Long.compare`와 동등한 exact ordering으로 비교하므로 subtraction overflow가 없다.

Independent hand oracle는 production summary/comparator helper를 import하지 않고 다음 표를 literal arithmetic과 field-by-field decision tree로 계산한다. Route A의 time은 `100 + 10 + 5 + 20 + 15 = 150`, Route B는 `120 + 0 + 10 + 70 + 10 = 210`, total은 `360`이다.

| Case | Reference vector | Baseline `(2, 2, 120, 360)` 대비 | 검출 목적 |
|---|---:|---|---|
| Earlier field wins | `(1, 99, 999, 999)` | BETTER | 뒤 field가 앞 field를 덮지 못함 |
| Second field wins | `(2, 1, 999, 999)` | BETTER | dispatched ordering |
| Third field wins | `(2, 2, 119, 999)` | BETTER | directed distance ordering |
| Fourth field wins | `(2, 2, 120, 359)` | BETTER | operational-time ordering |
| Exact equality | `(2, 2, 120, 360)` | EQUAL | four-field equality |
| First field loses | `(3, 0, 0, 0)` | WORSE | later minima가 unassigned를 덮지 못함 |
| Invalid boundary | negative/missing/overflow/non-finite | NOT_COMPARABLE | fail-closed before compare |

Oracle test는 각 summary field/component를 한 번에 하나씩 변조해 recomputation mismatch를 잡고, `0`, `Long.MAX_VALUE`, exact-last-safe sum, one-past-overflow, wrong unit/order/vector length, legacy non-finite input을 포함한다. `metrics`, `scores`, `objective`와 위 five scalar fields 중 권위 field 하나를 바꾸면 semantic fingerprint 또는 payload digest가 반드시 바뀌며, observation elapsed만 바꾸면 둘 다 바뀌지 않아야 한다.

### 9.7 Canonical payload의 open 경계

내부 contract가 반드시 지킬 의미:

- Versioned field set와 canonical encoding version
- Stable request/route order
- Unordered collection만 명시 규칙으로 정렬
- Semantic visit order 보존
- Duplicate/unknown field와 non-canonical absent 표현 거부
- Integer-only/checked length
- Semantic fingerprint와 exact payload digest 분리
- Elapsed/provider locator/log order 제외

아직 확정하지 않는 것:

- External JSON field 이름
- Media type
- Long-term wire compatibility
- Production hash algorithm/version

Raw `byte[]` 또는 mutable `ByteBuffer` view를 노출하지 않고 defensive copy abstraction을 사용한다.

## 10. 상태 전이와 pseudocode

### 10.1 Legal state transition

```text
Phase06 candidate/replay/evidence
  ─ authority/content receipt ─▶ UnverifiedCandidate
  └ mismatch ─────────────────▶ Rejected(CORRUPT)

UnverifiedCandidate
  ├ cache-free PASS ──────────▶ CandidatePassReport + VerifiedSolution
  ├ hard rejection ──────────▶ Rejected(INFEASIBLE)
  ├ structure/arithmetic ────▶ Rejected(INVALID)
  └ claim/integrity ─────────▶ Rejected(CORRUPT)

VerifiedSolution
  ─ preliminary outcomes ────▶ AuditDraft
  ├ complete required audit ─▶ FinalOutcomes + AuditRecord
  └ interrupted/incomplete ──▶ GateIncomplete

FinalResultDraft
  ├ result PASS ─────────────▶ PublishableResult
  ├ semantic invalid ────────▶ Rejected(INVALID)
  ├ fingerprint/bytes tamper ▶ Rejected(CORRUPT)
  └ incomplete ──────────────▶ GateIncomplete
```

금지 전이:

```text
Candidate rejected  -X→ VerifiedSolution
Candidate PASS only -X→ PublishableResult
Audit incomplete    -X→ FinalOutcome payload
Result fail         -X→ normal publication
```

### 10.2 Candidate verifier pseudocode

```text
verify(request):
  require known immutable contract/version
  defensively copy and validate allowed sealed shapes

  authority = compare intrinsic/content identities for:
      problem, travel, profile, evaluation declaration, SolvePlan
      candidate, replay, accepted Phase 06 receipt
  if mismatch:
      return CORRUPT before route evaluation

  partition = independently check request universe ↔ routes XOR bank
  if invalid:
      return INVALID in stable failure order

  for route in stable route-id order:
      artifact = cache-free core kernel evaluate(authority, route)
      if well-formed hard rejection:
          return INFEASIBLE
      if structure/arithmetic/contract invalid:
          return INVALID

  solutionEvaluation =
      accepted checked full-solution evaluator(routes, bank)
  # BLOCKED until owner/API/identity contract is approved.

  compare declared claims only after recomputation
  if any mismatch:
      return CORRUPT

  freeze VerifiedSolution and CandidatePassReport
  return FEASIBLE PASS
```

### 10.3 Audit pseudocode

```text
audit(verifiedSolution, preliminaryUnassigned, staticEvidence):
  verify solution identity and bound insertion authority identity
  reject any caller-provided executable shape

  for request in stable request order:
    if independent static evidence proves unassignable:
      record STATIC_PROVEN
      continue

    enumerate every eligible concrete vehicle in stable order
    enumerate every legal pickup/delivery position pair
    evaluate with reviewed exact side-effect-free pair evaluator
    checked-count every option and rejection code
    keep first stable feasible witness internally
    do not mutate route/outcome

    require declared legal count == evaluated count
    record EXHAUSTED or FEASIBLE_INSERTION_FOUND

  require every required request completed
  freeze audit record
```

`EXHAUSTIVE_FOR_FINAL_SOLUTION`을 쓸 때는 해당 request의 모든 required option이 완료돼야 한다. Feasible witness 뒤 early stop을 허용하는 성능 proposal은 exhaustive confidence를 주장할 수 없으며 별도 review 대상이다.

### 10.4 Result verifier pseudocode

```text
finalize:
  require candidate PASS references exact VerifiedSolution
  require complete audit references same solution
  derive exactly-one outcomes
  derive checked summary from routes/outcomes
  build versioned semantic manifest
  encode canonical payload

resultVerify:
  re-check candidate PASS and solution identity
  recompute request universe ↔ outcome bijection
  recompute assigned route/vehicle/pair ownership
  validate audit required set/count/completion
  enforce diagnostic source/confidence ceiling
  independently derive summary
  recompute semantic fingerprint
  independently canonicalize and compare bytes/length/digest

  incomplete work → GateIncomplete
  semantic defect → INVALID
  identity/claim/bytes mismatch → CORRUPT
  all exact → ResultPassReport + PublishableResult
```

Result verifier가 finalizer의 `summary()`나 encoder의 digest를 그대로 재사용하면 독립 gate가 아니다.

## 11. Ordered work packages

### WP-07.0 — Entry receipt, authority와 contract freeze

**목적과 이유**

구현 전에 upstream identity, failure taxonomy, canonical field coverage와 dependency를 고정한다. 이 단계가 없으면 뒤의 모든 test가 서로 다른 candidate/profile/encoding을 우연히 비교할 수 있다.

**사전조건**

- Phase 00~06 accepted receipt
- Solution-level evaluation owner/API 승인
- Independent oracle와 result encoding reviewer 지정
- Exact implementation task와 rollback point

**예상 변경 위치/type**

- `verification.api`의 request/result skeleton
- Authority/evidence data projection
- `Phase07VerificationArchitectureTest`
- Test-only fixture manifest

**구체 행동**

1. Phase 06 candidate/replay/evidence field를 core-only projection으로 mapping한다.
2. Problem/travel/profile/evaluation/`SolvePlan` equality와 content coverage를 표로 고정한다.
3. `FEASIBLE/INFEASIBLE/INVALID/CORRUPT`와 `GateIncomplete`를 분리한다.
4. Current canonical/datetime input/implementation baseline/live snapshot을 §3의 네 층으로 고정한다.
5. §9 owner/visibility 표, verification direct test dependencies와 fresh reactor build order를 review한다.
6. Canonical required manifest count `69`, duplicate `0`, digest `aff4bae...d339`를 test-only manifest로 먼저 고정한다.
7. Canonical semantic inclusion/exclusion과 public/non-public 상태를 review한다.
8. Verification forbidden dependency/import/bytecode rule을 먼저 red로 만든다.

**근거**

Canonical Phase 07 §4, §7.2~7.3, review F-P07-001/002/004/005.

**금지 shortcut**

- Upstream solver `CommittedCandidate` class 직접 import
- Missing field를 “나중에 채움”으로 nullable/`Map<String,Object>` 처리
- SHA-256을 public default로 임의 확정
- Entry blocker를 fake fixture로 acceptance했다고 표시

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
  -Dtest=Phase07VerificationArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
  -Dtest=CandidateVerifierAuthorityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- 기대: Forbidden edge 0, authority mismatch는 route evaluation 호출 0회로 `CORRUPT`.
- 0 tests: 성공이 아니라 command/manifest 오류.
- Solver type 필요: upstream projection/architecture contract가 아직 준비되지 않은 것.
- Solution evaluator 없음: WP-07.1 진행 금지.

**Rollback/마지막 안전 지점**

API skeleton과 fixture proposal만 폐기한다. Accepted Phase 06 bundle은 바꾸지 않는다.

**Handoff**

Approved request projection, failure taxonomy, field inclusion manifest, type/dependency closure와 required 69 digest를 WP-07.1에 넘긴다.

### WP-07.1 — Candidate structure와 cache-free verifier

**목적과 이유**

Candidate route/bank가 실제 RPDPTW stable solution인지 search claim 없이 다시 만든다.

**사전조건**

WP-07.0 green, accepted Phase 03 route/solution evaluation과 Phase 04 bound profile.

**예상 변경 위치/type**

- `verification.api`
- `verification.candidate.internal`
- `CandidateAuthorityValidator`
- `CandidatePartitionValidator`
- `CacheFreeCandidateVerifier`
- `CandidateClaimComparator`

**구체 행동**

1. Defensive immutable request construction과 unknown/executable shape rejection.
2. Request universe, complete pair, same vehicle, precedence, route-bank XOR 검사.
3. Stable route order의 cache-free propagation/evaluation.
4. Checked full-solution evaluation과 deterministic failure triage.
5. 모든 재계산 뒤 declared claims exact parity.
6. PASS-only `VerifiedSolution`/`CandidatePassReport` 생성.

**근거**

Master §6/§12/§14.1, Canonical Domain §3/§12~§16, Canonical Architecture §5~§7/§18, canonical Phase 07 §9.1.

**금지 shortcut**

- Poisoned cache가 정상 cache와 다른 verdict를 만들게 함
- Structural defect를 `INFEASIBLE`이나 `UNASSIGNED`로 축소
- Overflow wrap/clamp/sentinel
- Missing claim을 재계산 값으로 채워 통과
- Route evaluation만 더한 ad hoc solution aggregate

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
  -Dtest=CandidateVerifierAuthorityTest,CandidateVerifierIndependenceTest,CandidatePartitionCorruptionTest,CandidateRouteFeasibilityTest,CandidateArithmeticCorruptionTest,CandidateClaimTamperingTest,CandidateArtifactImmutabilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Exact mismatch/duplicate/missing/split/overflow/claim tamper가 정해진 disposition으로 거부.
- Poisoned cache 두 variant와 baseline 재계산 결과가 exact equal.
- Candidate rejection 뒤 `VerifiedSolution` 생성 0.
- Failure code가 hash/thread order에 따라 달라지면 deterministic contract 위반.

**Rollback/마지막 안전 지점**

Verifier implementation을 제거하고 WP-07.0 approved contract로 돌아간다.

**Handoff**

Immutable `VerifiedSolution`, `CandidatePassReport`, safe rejection과 `E-P07-CANDIDATE-VERIFY` 후보 report를 WP-07.2/3에 전달한다.

### WP-07.2 — Independent candidate oracle, malicious/property와 replay

**목적과 이유**

Production verifier와 같은 bug를 공유하지 않는 reference path가 실제 결함을 잡는지 증명한다.

**사전조건**

WP-07.1 deterministic hand case green.

**예상 변경 위치/type**

- `build/test-fixtures/.../phase07`
- `ReferenceCandidateOracle`
- `Phase07AuthorityFixtureBuilder`
- `CorruptedCandidateBuilder`
- `MaliciousCandidateBuilder`
- `Phase07OracleIndependenceTest`
- `Phase07OracleSensitivityTest`

**구체 행동**

1. Tiny directed table과 `BigInteger` reference arithmetic을 production kernel 없이 구현한다.
2. 한 번에 primary defect 하나만 주입하는 one-field corruption builder를 만든다.
3. Mutable alias, unknown tag, extreme `long`과 forged ID를 주입한다.
4. Generated route-bank property를 independent tuple과 비교한다.
5. Fixed envelope repeat/parallel fingerprint를 비교한다.
6. Cache trust, pair split, overflow wrap, summary reuse, unordered encoding faulty double이 각각 assertion-red가 되는지 확인한다.

**근거**

Canonical Phase 07 §10.1~10.4/§10.7, review F-P07-006.

**금지 shortcut**

- Oracle이 production evaluator/verifier/finalizer/encoder 호출
- Fixture와 production builder가 mutable collection 공유
- Compile failure를 defect sensitivity라고 주장
- Test-only 수치를 official/default로 표시

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/test-fixtures,rpdptw/verification -am clean verify

./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
  -Dtest=CandidatePartitionPropertyTest,CandidateArtifactImmutabilityTest,Phase07ReplayReproducibilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Oracle의 forbidden production-helper reference 0.
- 다섯 faulty double 각각 expected assertion red와 최소 counterexample/ordinal 보존.
- Generated counterexample 0, malicious callback 실행 0.
- Same envelope result exact equality.
- Oracle가 production helper를 공유하면 관련 evidence 전체를 폐기한다.

**Rollback/마지막 안전 지점**

독립성이 깨진 fixture/oracle evidence를 삭제 가능한 미승인 artifact로 취급하고 WP-07.1 hand cases로 돌아간다.

**Handoff**

Oracle source/fixture/expected digest와 stable verified fixture를 WP-07.3~5에 넘긴다.

### WP-07.3 — Final partition, exhaustive insertion audit와 diagnostic

**목적과 이유**

검증된 solution에서 모든 request의 final outcome을 만들고, unassigned 주장의 근거를 실제 evidence 범위에 가둔다.

**사전조건**

WP-07.1 PASS-only solution, accepted Phase 05 pair evaluator와 bound insertion authority.

**예상 변경 위치/type**

- `result.api.FinalOutcome`
- `result.finalization.FinalInsertionAuditor`
- `FinalInsertionAuditRecord`
- `EvidenceBoundedDiagnosticFactory`
- `OutcomeDerivedSummaryFactory`의 전 단계

**구체 행동**

1. Verified route ownership에서 preliminary outcome을 만든다.
2. Independent static `PROVEN` set만 skip한다.
3. Caller callback 없는 bound evaluator identity를 enumeration 전에 확인한다.
4. Every eligible vehicle × legal position pair를 stable order로 검사한다.
5. Checked work/rejection count와 internal feasible witness를 기록한다.
6. Route/outcome을 바꾸지 않는다.
7. Diagnostic source/scope/confidence ceiling과 exactly-one outcome을 고정한다.

**근거**

`Q-RES-01/02`, Master §10.2, canonical Phase 07 §7.5/§9.2, review F-P07-003.

**금지 shortcut**

- Bank last failure를 final reason으로 사용
- Search observation을 `PROVEN`으로 승격
- 일부 option만 검사하고 exhaustive 표시
- Feasible witness auto-insert/re-solve
- `LEASE`를 `OUTSOURCED`로 변환
- Count overflow 무시

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
  -Dtest=FinalInsertionAuditAuthorityTest,FinalInsertionAuditTest,FinalInsertionAuditCorruptionTest,FinalInsertionAuditCompletenessPropertyTest,FinalOutcomeTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Public request의 executable field 0.
- Authority mismatch 때 evaluator call 0.
- Static-proven 외 required request 누락 0.
- Option set/count가 reference enumerator와 exact.
- Feasible witness 전후 route/outcome fingerprint 불변.
- Audit interruption은 `GateIncomplete`, fabricated disposition 0.

**Rollback/마지막 안전 지점**

Incomplete audit/draft를 publish하지 않고 `VerifiedSolution`으로 돌아간다.

**Handoff**

Complete audit record, final outcomes와 `E-P07-AUDIT` 후보 report를 WP-07.4에 넘긴다.

### WP-07.4 — Result manifest와 deterministic canonical payload

**목적과 이유**

Verified facts에서 결과 identity를 결정적으로 만들고, semantic equality와 exact bytes integrity를 분리한다.

**사전조건**

WP-07.3 complete audit/outcomes, encoding inclusion/exclusion/ordering/security review 승인.

**예상 변경 위치/type**

- `result.api.ResultSummary`
- `FinalResultManifest`
- `result.encoding.CanonicalResultEncoder`
- `CanonicalResultPayload`
- Independent result/encoding oracle

**구체 행동**

1. Outcomes/routes에서 checked summary를 파생한다.
2. §9.6.1의 8-field manifest와 exact operational-time component를 독립 hand oracle로 대조한다.
3. §9.6.2 four-field reference vector를 customer objective와 분리하고 lex ascending/exact equality/`NOT_COMPARABLE`을 검사한다.
4. Negative/missing/unit/schema/non-finite/overflow를 compare/encode 전 fail-closed한다.
5. Authority/candidate/replay/audit/verifier provenance를 versioned manifest에 넣는다.
6. Self fingerprint를 제외한 semantic projection에서 fingerprint를 계산한다.
7. Stable canonical bytes와 exact length/digest를 만든다.
8. Observation elapsed/provider locator/log order를 semantic identity 밖에 둔다.
9. Permutation/locale/timezone/parallel과 every-authoritative-field/operational-component mutation coverage를 검사한다.

**근거**

Master §10.2/§14.1, Canonical Domain §14.1~§14.2, Integrated design §11.6, canonical Phase 07 §7.6/§8.4/§10.6/§13.1.

**금지 shortcut**

- `toString()` serialization
- Unordered map/set traversal
- Default locale/timezone
- Finalizer summary를 expected 값으로 재사용
- Semantic visit order 정렬
- Public schema/hash algorithm 임의 확정

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
  -Dtest=CanonicalResultEncodingTest,ResultOutcomePropertyTest,ResultSummaryContractTest,ReferenceResultComparatorTest,ResultSummaryOracleSensitivityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Map/source insertion order, locale/timezone/thread가 달라도 exact bytes 동일.
- Visit order mutation은 fingerprint/digest 변경.
- Authoritative one-field mutation undetected 0.
- Duplicate/unknown/non-canonical absent/overflow는 fail-closed.
- Elapsed-only 차이는 semantic fingerprint 불변.
- Hand time `150 + 210 = 360`, comparator first-difference/equality와 invalid `NOT_COMPARABLE`가 independent literal oracle와 exact.
- Customer objective를 바꿔도 reference ordering source가 바뀌지 않고, reference field 하나를 바꾸면 정해진 lex result만 바뀜.

**Rollback/마지막 안전 지점**

Encoder/draft를 제거하고 WP-07.3 typed outcomes/audit를 unpublished 상태로 보존한다.

**Handoff**

Manifest/payload draft와 independent expected bytes/digest를 WP-07.5에 넘긴다.

### WP-07.5 — Result-integrity verifier와 both-gate facade

**목적과 이유**

Finalizer 자체의 bug나 tampering을 두 번째 독립 경로가 잡고, 두 PASS 외 모든 경로를 publication에서 막는다.

**사전조건**

WP-07.1 candidate PASS, WP-07.4 immutable draft와 independent oracle.

**예상 변경 위치/type**

- `verification.result`
- `ResultIntegrityVerifier`
- `Phase07FinalResultService`
- `Phase07Output`
- `FinalResultRejection`

**구체 행동**

1. Candidate PASS와 solution identity를 재검사한다.
2. Outcome bijection/ownership/audit/confidence/summary를 별도 traversal로 재계산한다.
3. Semantic fingerprint와 canonical bytes/digest를 독립 대조한다.
4. Candidate fail 뒤 finalization 0회를 보장한다.
5. Result fail/incomplete에 normal payload 0을 보장한다.
6. `VerificationRejected`와 `GateIncomplete`를 exhaustively 분리한다.

**근거**

Master §14.1, canonical Phase 07 §9.3/§10.6, review F-P07-002/007.

**금지 shortcut**

- Candidate PASS로 result failure 덮기
- Complete-looking result로 candidate failure 덮기
- Incomplete를 `INVALID`로 합성
- Finalizer helper/digest 무검증 재사용
- Failure에 route/outcome/benchmark vector 포함

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification \
  -Dtest=ResultIntegrityVerifierTest,BothGatePublicationTest,CanonicalResultEncodingTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Outcome/audit/summary/fingerprint/payload tamper 전부 거부.
- Interruption은 disposition 없는 `GateIncomplete`.
- One-PASS partial publishable 0.
- Both PASS에서만 publishable 1.
- Rejection payload에 safe identifier/bounded failure만 존재.

**Rollback/마지막 안전 지점**

Facade/result verifier를 revert하고 candidate PASS와 result draft를 publication 불가 artifact로 보존한다.

**Handoff**

`E-P07-RESULT-VERIFY` 후보 report와 세 variant contract를 WP-07.6/Phase 08에 넘긴다.

### WP-07.6 — Architecture, evidence, independent review와 handoff

**목적과 이유**

Green working tree를 accepted Phase artifact로 바꾸려면 immutable evidence DAG와 독립 review가 필요하다.

**사전조건**

WP-07.0~5 required test green, missing/failed/error/skipped 0.

**예상 변경 위치/type**

- Architecture dependency report
- `E-P07-*` content-addressed bundles
- Pre-review evidence manifest
- Independent review input/report
- Phase 08 compatibility/handoff manifest

공용 progress/status는 total scheduler만 변경한다.

**구체 행동**

1. Source/build/toolchain/input/output/oracle/test report digest를 봉인한다.
2. Canonical required manifest 69/digest와 Target additional manifest 5/digest를 separately freeze한다.
3. JUnit `MethodSource` discovery manifest와 fresh Surefire/Failsafe XML을 ordered/bidirectional 대조한다.
4. Required/additional missing·duplicate·ambiguous·failed·error·skipped·aborted·stale·zero-discovered가 모두 0인지 확인한다.
5. Forbidden dependency/customer/provider/vendor와 production→fixture reference 0 report를 봉인한다.
6. OPEN/GATED/deferred snapshot과 last safe point를 기록한다.
7. Independent review를 받고 post-review acceptance receipt를 발행한다.
8. Phase 08 three-variant compatibility와 rollback identity를 확인한다.

**근거**

Master Realization Plan §9~11, canonical Phase 07 §12~15.

**금지 shortcut**

- `target/`/console 한 줄만 evidence로 사용
- 실패/skipped/이전 run report 혼합
- Pre-review manifest에 review/acceptance 역참조
- Receipt 없이 `ACCEPTED`
- Phase 07 PASS를 benchmark/production PASS로 표시

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
  -Dtest=Phase07VerificationArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/test-fixtures,rpdptw/verification -am clean verify

./mvnw -B -ntp -Dstyle.color=never clean verify
```

**기대 결과와 실패 해석**

- OR-Tools-free ALNS-only full reactor green.
- Required 69와 additional 5의 missing/duplicate/ambiguous/failed/error/skipped/aborted/stale 0, manifest digest exact.
- Verification→solver/search/cache/provider/vendor edge 0.
- Immutable bundle digest, independent review `PASS`, valid acceptance receipt.
- Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`.

**Rollback/마지막 안전 지점**

Last accepted predecessor bundle과 last green unpublished WP artifact를 유지한다.

**Handoff**

Accepted `Phase07HandoffManifest`, `Phase07Output` contract, evidence refs와 rollback point를 Phase 08/14A에 넘긴다.

## 12. 테스트 구현 안내

### 12.1 Fixture, builder와 oracle 분리

| 이름 후보 | 역할 | 독립성 규칙 |
|---|---|---|
| `P07_TINY_PD_3_TEST_ONLY` | Real pair/delivery-only/assigned/unassigned/DIRECT/LEASE tiny facts | Official fixture 아님, integer values explicit |
| `Phase07AuthorityFixtureBuilder` | Same problem/travel/profile/ref tuple | Parser/provider 호출 금지 |
| `CandidateArtifactBuilder` | Feasible baseline route/bank/claim/replay | Stable order, defensive copy |
| `CorruptedCandidateBuilder` | One-field identity/content/claim/partition tamper | 한 test에 primary defect 하나 |
| `MaliciousCandidateBuilder` | Extreme long, forged ID, mutable alias, unknown shape | Executable callback/script 없음 |
| `AuditScenarioBuilder` | Static proven/exhausted/feasible witness cases | Fixed final routes와 exact option set |
| `FinalResultDraftBuilder` | Outcome/audit/summary/payload tamper | Candidate PASS refs 별도 |
| `PermutationResultBuilder` | Map/source/locale/timezone/thread permutation | Semantic visit order 보존 |
| `ReferenceCandidateOracle` | Tiny propagation/evaluation | Production kernel/verifier 호출 금지 |
| `ReferenceInsertionOracle` | Exhaustive nested enumeration | Production auditor 호출 금지 |
| `ReferenceResultOracle` | Outcome/summary derivation | Finalizer helper 호출 금지 |
| `ReferenceCanonicalEncodingOracle` | Explicit field order/expected literal | Production encoder 호출 금지 |

Oracle arithmetic은 `BigInteger` 등 overflow-free reference로 range를 먼저 계산한 뒤 production `long` 허용 여부를 판정한다.

<a id="122-test-class와-exact-method-후보"></a>

### 12.2 Canonical required 69-method manifest

위 legacy “후보” ID는 기존 문서의 navigation compatibility만 위한 alias이며, 현재 normative status는 아래 canonical required 69-method manifest다.

아래 69개는 “후보 일부”가 아니라 [Canonical Phase 07](../../phases/phase-07-independent-verification-final-result.md) §10.3~§10.7에서 순서대로 추출한 required method manifest다. `BEGIN/END` marker 사이의 각 line만 UTF-8, LF, 앞뒤 whitespace 없음, 마지막 line 뒤 LF로 연결한 SHA-256은 `aff4bae9269b37b09a8df9909cb9e5c8f060e3bb2f1f1cc166c29749e66ad339`다.

```text
BEGIN PHASE07_REQUIRED_METHODS_V1
CandidateVerifierAuthorityTest.passesOnlyWhenCandidateReplayProblemTravelAndProfileFingerprintsMatch()
CandidateVerifierAuthorityTest.rejectsProblemFingerprintMismatchAsCorrupt()
CandidateVerifierAuthorityTest.rejectsPreparedTravelFingerprintMismatchAsCorrupt()
CandidateVerifierAuthorityTest.rejectsBoundProfileFingerprintMismatchAsCorrupt()
CandidateVerifierAuthorityTest.rejectsEvaluationDeclarationOrSolvePlanFingerprintMismatchAsCorrupt()
CandidateVerifierAuthorityTest.rejectsSameIdentityDifferentCanonicalBytesAsCorrupt()
CandidateVerifierAuthorityTest.candidateProjectionDigestCoversContractAuthoritiesRoutesBankAndDeclaredClaims()
CandidateVerifierIndependenceTest.requestContractContainsNoSearchCacheOrSolverSummaryType()
CandidateVerifierIndependenceTest.poisonedSearchCachesCannotChangeRecomputedVerdict()
CandidateClaimTamperingTest.rejectsTamperedMetricClaimAsCorrupt()
CandidateClaimTamperingTest.rejectsTamperedScoreClaimAsCorrupt()
CandidateClaimTamperingTest.rejectsTamperedObjectiveOrSolutionFingerprintAsCorrupt()
CandidateArtifactImmutabilityTest.defensivelyCopiesAdversarialRouteAndBankCollections()
CandidateArtifactImmutabilityTest.rejectsUnknownOrExecutableExtensionShape()
CandidatePartitionCorruptionTest.rejectsDuplicatedPickupTaskInOneRouteAsInvalid()
CandidatePartitionCorruptionTest.rejectsMissingDeliveryTaskAsInvalid()
CandidatePartitionCorruptionTest.rejectsPairSplitAcrossTwoVehicleRoutesAsInvalid()
CandidatePartitionCorruptionTest.rejectsDeliveryBeforePickupAsInvalid()
CandidatePartitionCorruptionTest.rejectsRouteAndBankDuplicateAsInvalid()
CandidatePartitionCorruptionTest.rejectsRequestMissingFromRoutesAndBankAsInvalid()
CandidatePartitionCorruptionTest.rejectsWholePairDuplicatedAcrossRoutesAsInvalid()
CandidatePartitionCorruptionTest.rejectsUnknownTaskRequestOrVehicleIdAsInvalid()
CandidateRouteFeasibilityTest.classifiesCapacityTimeWindowAndResourceViolationsAsInfeasible()
CandidateRouteFeasibilityTest.classifiesWrongTerminalOrServicePatternAsInvalid()
CandidateRouteFeasibilityTest.usesDirectedPreparedTravelWithoutReverseOrLazyFallback()
CandidateArithmeticCorruptionTest.rejectsRouteMetricAndObjectiveOverflowAsInvalid()
CandidateArithmeticCorruptionTest.neverWrapsClampsOrConvertsOverflowToUnassigned()
FinalInsertionAuditTest.skipsOnlyRequestsWithIndependentStaticProvenEvidence()
FinalInsertionAuditAuthorityTest.requestContainsNoCallerProvidedEvaluatorComparatorOrCallback()
FinalInsertionAuditAuthorityTest.rejectsInsertionAuthorityOrEvaluatorContractIdentityMismatchBeforeEnumeration()
FinalInsertionAuditTest.enumeratesEveryEligibleVehicleAndLegalPickupDeliveryPositionPair()
FinalInsertionAuditTest.usesExhaustiveForFinalSolutionOnlyWhenEveryOptionFails()
FinalInsertionAuditTest.recordsFeasibleInsertionWithoutMutatingRoutesOrOutcome()
FinalInsertionAuditTest.doesNotClaimGlobalInfeasibilityFromFixedRouteAudit()
FinalInsertionAuditTest.doesNotPromoteSearchFailureToProvenOrExhaustive()
FinalInsertionAuditCorruptionTest.rejectsMissingEligibleVehicleOrPositionAsInvalid()
FinalInsertionAuditCorruptionTest.rejectsAuditCountOverflowAsInvalid()
FinalOutcomeTest.assignsDirectAndLeaseRequestsAsAssigned()
FinalOutcomeTest.createsExactlyOneOutcomeForEveryInputRequest()
FinalOutcomeTest.unassignedNeverContainsRouteOrVehicleReference()
FinalOutcomeTest.neverCreatesOutsourcedOrDeferredSolverOutcome()
ResultIntegrityVerifierTest.rejectsMissingOrDuplicatedOutcomeAsInvalid()
ResultIntegrityVerifierTest.rejectsAssignedRouteVehicleOrPairReferenceMismatchAsInvalid()
ResultIntegrityVerifierTest.rejectsIncompleteAuditAsInvalid()
ResultIntegrityVerifierTest.rejectsOverstatedDiagnosticConfidenceAsInvalid()
ResultIntegrityVerifierTest.rejectsTamperedSummaryAsCorrupt()
ResultIntegrityVerifierTest.rejectsTamperedResultFingerprintAsCorrupt()
ResultIntegrityVerifierTest.rejectsTamperedPayloadBytesLengthOrDigestAsCorrupt()
ResultIntegrityVerifierTest.rejectsMissingCandidatePassEvenWhenOutcomesAreComplete()
BothGatePublicationTest.candidatePassCannotCoverResultFailure()
BothGatePublicationTest.resultShapeCannotCoverCandidateFailure()
BothGatePublicationTest.rejectedVariantCannotCarryFeasibleDisposition()
BothGatePublicationTest.auditOrResultInterruptionProducesGateIncompleteWithoutVerificationDisposition()
BothGatePublicationTest.failureNeverCarriesNormalRouteOutcomeOrBenchmarkPayload()
CanonicalResultEncodingTest.sameSemanticResultProducesIdenticalBytesAcrossMapAndSourceInsertionOrders()
CanonicalResultEncodingTest.sameResultProducesIdenticalBytesAcrossLocaleTimezoneAndParallelCalls()
CanonicalResultEncodingTest.preservesSemanticVisitOrderWhileSortingOnlyUnorderedCollections()
CanonicalResultEncodingTest.rejectsDuplicateKeysUnknownFieldsAndNonCanonicalAbsentValues()
CanonicalResultEncodingTest.everyAuthoritativeFieldMutationChangesSemanticFingerprintOrPayloadDigest()
CanonicalResultEncodingTest.observationElapsedDoesNotChangeSemanticResultFingerprint()
Phase07VerificationArchitectureTest.verificationDependsOnCoreButNotSolverSearchOrCache()
Phase07VerificationArchitectureTest.verificationContainsNoCloudProviderVendorOrCustomerBranch()
Phase07OracleIndependenceTest.referenceOraclesHaveNoProductionVerifierEvaluatorFinalizerOrEncoderDependency()
Phase07OracleSensitivityTest.rejectsSeededCacheTrustPairSplitWrapSummaryReuseAndNoncanonicalOrderingDefects()
CandidatePartitionPropertyTest.acceptsOnlyExactRouteBankPartition()
FinalInsertionAuditCompletenessPropertyTest.matchesIndependentExhaustiveEnumerator()
ResultOutcomePropertyTest.outcomesFormBijectionWithRequestUniverse()
Phase07ReplayReproducibilityTest.sameFixedEnvelopeProducesSameVerifiedSolutionOutcomeAndResultFingerprint()
Phase07ReplayReproducibilityTest.changedAuthorityOrReplayFieldChangesCoveredFingerprint()
END PHASE07_REQUIRED_METHODS_V1
```

Required grouping은 manifest order를 바꾸지 않는다.

| Lines | Count | Primary owner/oracle | WP / evidence |
|---:|---:|---|---|
| 1~14 | 14 | Authority/projection/cache/claim/immutability; literal authority + independent digest | 07.0~07.2 / `E-P07-CANDIDATE-VERIFY` |
| 15~27 | 13 | Pair/partition/route/directed travel/arithmetic; reference propagation + range oracle | 07.1~07.2 / `E-P07-CANDIDATE-VERIFY` |
| 28~41 | 14 | Audit authority/completeness/confidence and exactly-one outcome; exhaustive insertion + ownership oracle | 07.3 / `E-P07-AUDIT` |
| 42~60 | 19 | Result integrity, both-gate, exact summary/encoding; independent result/encoding oracle | 07.4~07.5 / `E-P07-RESULT-VERIFY` |
| 61~69 | 9 | Architecture, oracle independence/sensitivity, properties, replay | 07.2, 07.6 / all `E-P07-*` |

각 method의 expected disposition/code/call-count는 canonical §10.3~§10.7 row와 이 가이드의 exact semantic contract를 따른다. Class 존재, compile 성공, disabled test 또는 production helper를 그대로 호출하는 oracle은 해당 method PASS가 아니다. Target-only 추가 test는 별도 `PHASE07_ADDITIONAL_METHODS_V1` manifest/digest로 관리하며 required missing을 대신할 수 없다.

§9.6의 exact summary/comparator 교육 계약을 executable oracle에 연결하기 위해 다음 5개를 Target additional required set으로 둔다. 동일한 LF 규칙의 SHA-256은 `5374ef3ddf0aa9961c6b3974f036ff360f3f8f3ec8f7585134814dc059195502`다. 이 5개는 canonical 69를 줄이거나 대체하지 않는다.

```text
BEGIN PHASE07_ADDITIONAL_METHODS_V1
ResultSummaryContractTest.derivesExactCountsDistanceOperationalTimeMetricsScoresAndObjective()
ResultSummaryContractTest.rejectsNegativeMissingUnitMismatchNonFiniteAndOverflowBeforeComparisonOrEncoding()
ReferenceResultComparatorTest.ordersLexicographicallyByCanonicalFourFieldVectorAndUsesExactEquality()
ReferenceResultComparatorTest.isIndependentFromCustomerObjectiveAndReturnsNotComparableForInvalidInput()
ResultSummaryOracleSensitivityTest.rejectsEveryOneFieldAndOperationalComponentTamper()
END PHASE07_ADDITIONAL_METHODS_V1
```

Manifest verifier는 다음을 fail-closed로 판정한다.

1. Canonical §10.3~§10.7 추출 목록과 위 목록을 ordered/bidirectional 비교한다: count `69`, duplicate `0`, missing `0`, extra `0`, order mismatch `0`, SHA-256 exact equality.
2. JUnit Platform discovery listener가 display name이 아니라 exact `MethodSource(className, methodName)`를 기록한다. Required ID와 매핑되지 않는 dynamic source는 required coverage로 계산하지 않는다.
3. Parameterized/property invocation은 하나의 required method ID 아래 invocation ID로 모은다. Required method마다 successful invocation이 하나 이상이고 failed/error/skipped/aborted가 0이어야 한다.
4. Surefire/Failsafe XML은 listener run ID와 source/build digest를 포함한 sealed execution manifest에 연결한다. Required method missing, ambiguous mapping, duplicate engine/shard, unknown result, stale XML 또는 zero discovered가 하나라도 있으면 실패한다.
5. Additional method는 unknown-extra report에 명시적으로 나타나야 하며 별도 승인 manifest에 없으면 실패한다. Required와 additional의 duplicate ID는 실패다.

### 12.3 Red → green 순서

| 순서 | 먼저 red로 만들 것 | 최소 green |
|---:|---|---|
| 1 | Verification forbidden dependency | Core-only edge |
| 2 | Authority/content/evaluation/`SolvePlan` mismatch | Evaluation 전 fail-closed |
| 3 | Pair/route-bank corruption | Exact `INVALID` |
| 4 | Hard infeasibility/overflow | `INFEASIBLE` 대 `INVALID` |
| 5 | Cache/claim tamper | Recompute + exact `CORRUPT` |
| 6 | PASS-only verified solution | Non-PASS 생성 0 |
| 7 | Audit authority/completeness | Callback 0, exact option set |
| 8 | Outcome/diagnostic | Bijection + confidence ceiling |
| 9 | Summary/manifest | Independent derivation |
| 10 | Canonical bytes | Permutation deterministic |
| 11 | Result/both-gate | Both PASS only |
| 12 | Oracle sensitivity/full reactor | Required failure/skip 0 |

한 단계 green 전에 다음 단계 facade/mock으로 넘어가지 않는다.

### 12.4 False-green 방지

- Required manifest는 69 lines, duplicate 0, SHA-256 `aff4bae9269b37b09a8df9909cb9e5c8f060e3bb2f1f1cc166c29749e66ad339`이어야 한다.
- Selected Surefire command는 `-Dsurefire.failIfNoSpecifiedTests=true`; future selected Failsafe command는 `-Dfailsafe.failIfNoSpecifiedTests=true`다.
- Selected filter와 `-am`을 함께 사용하지 않는다. 먼저 same-source `-am clean install`, 그 다음 module POM에서 no-`-am` selected run을 실행한다.
- Cross-module/module-closure/root suite는 test filter 없이 `clean verify`한다.
- JUnit discovery manifest와 fresh Surefire/Failsafe XML을 exact source/run ID로 결합해 required missing/duplicate/ambiguous/failed/error/skipped/aborted/zero-discovered가 하나라도 있으면 fail한다.
- Additional set은 별도 manifest/digest다. Additional test가 required missing을 cover하거나 duplicate ID를 만들 수 없다.
- 각 후속 `clean` 전에 report를 content-addressed evidence에 봉인한다.
- Same source commit/archive의 full-test install, test-jar artifact와 dependency-tree digest를 선행 확인한다.
- Report mtime/run ID/source digest가 맞지 않는 stale `target/`, console summary, IDE green과 과거 report를 evidence로 쓰지 않는다.
- Oracle이 production helper를 참조하거나 seeded faulty double을 assertion-red로 만들지 못하면 관련 green을 무효화한다.

### 12.5 Test 종류별 적용성

| 종류 | 적용 | Phase 07에서의 내용/미적용 이유 | Pass 판정 |
|---|---:|---|---|
| Unit/boundary | 필수 | Failure mapping, checked count, outcome, canonical primitives | Positive/negative/boundary exact |
| Contract | 필수 | Candidate/result/audit sealed API와 Phase 06/08 compatibility | Unknown/executable shape fail-closed |
| Module integration | 필수 | Candidate→audit→result facade | Both PASS only publishable |
| Property | 필수 | Route-bank XOR, audit enumeration, outcome bijection | Shrinkable counterexample 0 |
| Hand/exhaustive oracle | 필수 | Candidate, insertion, result, encoding | Production-helper ref 0 |
| Architecture | 필수 | Verification→solver/provider/vendor 금지 | Forbidden edge 0 |
| Fault | 필수 | Audit/result interruption, overflow, mutable alias | Pre-state/identity 보존 |
| Corruption | 필수 | Authority/route/claim/outcome/audit/summary/payload | Exact typed rejection |
| Reproducibility | 필수 | Locale/timezone/order/parallel/fixed envelope | Exact result identity |
| Security | 필수 | Callback/extension, raw PII/secret/cache/locator 배제 | Forbidden field/log content 0 |
| Performance | 제한 적용 | Audit option/work count와 allocation 관측; official wall-clock threshold는 open | Correctness 불변, checked work accounting |
| Application local E2E | Phase 08 소유 | Phase 07에는 filesystem/status/retrieval이 없음 | Phase 08가 accepted contract로 수행 |
| Provider integration | 미적용 | Pure core verification에 provider SDK 없음 | Architecture상 dependency 0이 pass |
| Storage/CAS | 미적용 | Phase 08/09 책임 | Phase 07에서 구현하지 않는 것이 pass |
| Official benchmark | Phase 14A 소유 | Phase 07 PASS는 quality/performance acceptance 아님 | Approved protocol/receipt 전 N/A |

### 12.6 Security, observability와 performance 자문

Security:

- Failure/diagnostic에는 safe internal ID와 bounded code만 둔다.
- Raw address, full input bytes, secret, provider signed URL, stack trace와 cache dump를 external payload에 넣지 않는다.
- Candidate가 executable callback, reflection expression, comparator나 script를 주입할 수 없게 sealed/allowlisted type을 사용한다.

Observability:

- Requested/evaluated option count, rejection code count, verifier stage와 artifact digest를 기록한다.
- Elapsed, log order, provider locator는 observation metadata로만 둔다.
- Observation이 semantic fingerprint/quality objective를 바꾸면 실패다.

Performance:

- Audit baseline은 deterministic exhaustive enumeration이다.
- Wall-clock correctness gate나 hidden option cap을 만들지 않는다.
- 병목을 측정한 뒤에만 safe pruning/early stop proposal을 별도 review하며, exhaustive confidence와 결과 identity 영향부터 분석한다.

## 13. 사람 checkpoint와 evidence

### 13.1 WP별 checkpoint

| Checkpoint | 사람이 직접 확인할 것 | Stop 조건 |
|---|---|---|
| C0 entry freeze | Accepted refs, owner/API, field coverage, open snapshot | 하나라도 TBD |
| C1 candidate PASS | Cache-free facts, disposition, claim parity, no solver edge | Cache/helper 공유 |
| C2 oracle | Source/bytecode independence와 faulty-double sensitivity | Production helper 발견 |
| C3 audit | Bound authority, complete option set, no mutation/confidence overclaim | Callback/누락/auto-fix |
| C4 encoding | Version/order/include/exclude/security | Public/hash rule 미승인 |
| C5 both-gate | Rejection/incomplete normal payload 0 | One-PASS publication |
| C6 evidence | Canonical 69 + additional 5 manifests/digests, exact discovery/reports/review/receipt | Missing/duplicate/ambiguous/skip/stale/zero-test report |

### 13.2 Planned evidence 내용

| Key | 최소 내용 | 현재 |
|---|---|---|
| `E-P07-CANDIDATE-VERIFY` | Source/build/authority/candidate/replay fingerprints, projection coverage, canonical manifest lines 1~27/63~65/68~69, partition/feasibility/overflow/cache/claim corruption, oracle independence/sensitivity, exact discovery/XML counts | NOT_PRODUCED |
| `E-P07-AUDIT` | Verified solution, bound insertion authority, canonical manifest lines 28~41/66~67, static/required sets, vehicle/position/evaluated counts, rejection/witness/no-mutation/confidence, callback absence, oracle digest | NOT_PRODUCED |
| `E-P07-RESULT-VERIFY` | Canonical manifest lines 42~62와 additional 5, outcome bijection, exact 8-field summary/operational-time/reference comparator oracle, field coverage, canonical bytes, tamper/both-gate/incomplete blocking | NOT_PRODUCED |

세 bundle 모두 canonical manifest version/count `69`/digest `aff4bae...d339`, additional count `5`/digest `5374ef...5502`, assigned manifest slice, source commit/archive, accepted wrapper/toolchain, fresh isolated Maven-repo identity, installed dependency/test-jar digest, JUnit discovery manifest, fresh Surefire/Failsafe XML, discovered/success/failed/error/skipped/aborted counts, oracle/fixture/expected digest, OPEN/GATED/deferred snapshot, reviewer/verdict, Phase 08 handoff와 rollback point를 포함한다. 최종 pre-review evidence manifest는 세 bundle의 union이 canonical 69와 additional 5를 exact cover하고 intersection duplicate가 0임을 증명한다.

### 13.3 Evidence DAG

```text
immutable implementation/test evidence
  → preReviewEvidenceManifest [M]
  → independentReviewReport [references M, digest R]
  → postReviewAcceptanceReceipt [references M + R]
  → ACCEPTED / handoff
```

Pre-review manifest에 reviewer, review result/reference 또는 acceptance receipt를 backfill하지 않는다. Review가 changes required이면 receipt를 발행하지 않는다.

### 13.4 상태를 바꿀 권한

Implementer와 reviewer는 evidence/verdict proposal을 제출한다. `execution-progress-and-results.md`의 authoritative registry/status/result는 total scheduler만 갱신한다. Source가 생겼다는 이유로 `READY`, test 하나가 green이라는 이유로 `ACCEPTED`, Phase 07 PASS라는 이유로 `OFFICIAL/PRODUCTION`을 선언하지 않는다.

## 14. 흔한 오해와 anti-pattern

- Verification module이 solver/search/cache/provider/vendor를 compile-depend
- Search와 verifier가 같은 mutable candidate/cache/summary를 공유
- `feasible=true`, cached arrival/load/metric/objective를 authority로 사용
- 다른 problem/travel/profile인데 값이 비슷하다고 허용
- Missing arc를 reverse/symmetry/coordinate/speed로 보완
- Partial/duplicate/split pair를 normal infeasible/unassigned로 축소
- Overflow를 wrap/clamp/sentinel/empty result로 바꿈
- Candidate failure를 모든 request `UNASSIGNED`로 바꿈
- Caller-provided evaluator/comparator/callback을 audit에 주입
- Bank/last insertion failure를 final diagnostic으로 직렬화
- Static proof 없이 `PROVEN`
- Incomplete audit로 `EXHAUSTIVE_FOR_FINAL_SOLUTION`
- Feasible witness auto-insert, hidden retry나 re-solve
- `LEASE`를 `OUTSOURCED`로 바꾸거나 fleet 밖 vehicle 생성
- Finalizer summary/fingerprint/digest를 verifier가 그대로 믿음
- Candidate PASS로 result failure를 덮음
- Gate incomplete를 `INVALID/INFEASIBLE/CORRUPT`로 합성
- Failure output에 정상 route/outcome/benchmark payload 포함
- `Map<String,Object>`, arbitrary script/reflection/executable extension
- `toString()`, unordered map, locale/timezone/thread completion으로 bytes 생성
- Elapsed/provider locator/log order를 semantic identity에 포함
- Same identity/different bytes overwrite
- Phase 07에서 filesystem/S3/GCS/CAS/status/retrieval/workflow/HTTP 구현
- `Q-BENCH-02`, public schema/hash, Phase 13/14 gate를 hidden default로 닫음

## 15. 구현 exit checklist와 Definition of Done

### 15.1 실제 구현 exit checklist

- [ ] Phase 00~06 accepted receipt와 exact authority identities가 확인됐다.
- [ ] Solution-level evaluation owner/API/identity/failure/comparator blocker가 승인으로 닫혔다.
- [ ] Verification→solver/search/cache/cloud/provider/vendor edge가 0이다.
- [ ] Candidate/replay/problem/travel/profile/evaluation/`SolvePlan` equality가 evaluation 전에 확인된다.
- [ ] Pair/route-bank/terminal/travel/load/time/resource를 cache-free 재계산한다.
- [ ] Search claim은 재계산 뒤 exact 비교하며 mismatch를 `CORRUPT`로 거부한다.
- [ ] Hard infeasible, structural/arithmetic invalid, integrity corruption을 구분한다.
- [ ] Candidate PASS 외에는 `VerifiedSolution`이 생성되지 않는다.
- [ ] Audit request에 caller executable이 없고 bound insertion authority가 확인된다.
- [ ] Static `PROVEN` 외 모든 unassigned required option이 complete audit됐다.
- [ ] Feasible insertion이 route/outcome을 바꾸지 않는다.
- [ ] 모든 input request가 exactly-one `ASSIGNED/UNASSIGNED` outcome을 가진다.
- [ ] DIRECT/LEASE 모두 assigned 의미를 보존한다.
- [ ] Diagnostic confidence가 evidence ceiling을 넘지 않는다.
- [ ] Exact 8-field `ResultSummary`와 five operational-time component가 routes/outcomes에서 checked 파생된다.
- [ ] Reference comparator가 `(unassigned, dispatched, directed distance, operational time)` lex ascending/exact equality를 쓰고 customer objective와 분리된다.
- [ ] Negative/missing/unit/schema/non-finite/overflow는 compare/encode 전 typed invalid 또는 incomplete로 fail-closed한다.
- [ ] Canonical encoding version/field/order/security review가 끝났다.
- [ ] Result verifier가 outcome/audit/confidence/summary/fingerprint/payload를 독립 검사한다.
- [ ] Failure/incomplete에 normal payload가 없다.
- [ ] Same fixed envelope의 verified solution/outcome/result fingerprint가 exact 같다.
- [ ] Independent oracle의 production-helper reference가 0이고 faulty doubles를 모두 잡는다.
- [ ] Canonical required manifest가 exact 69/duplicate 0/SHA-256 `aff4bae...d339`이고 additional manifest가 exact 5/duplicate 0/SHA-256 `5374ef...5502`다.
- [ ] Required/additional test missing/duplicate/ambiguous/failed/error/skipped/aborted/stale/zero-discovered가 0이다.
- [ ] Fresh `-am clean install`, no-`-am` selected run, module/reactor clean verify의 source/artifact/report digest가 연결됐다.
- [ ] `E-P07-*`, independent review, acceptance receipt가 immutable identity로 존재한다.
- [ ] Phase 08 handoff와 rollback point가 검증됐다.
- [ ] OPEN/GATED/deferred/official 값을 발명하지 않았다.

### 15.2 Phase 07 Definition of Done

Phase 07 `ACCEPTED`는 다음 AND gate다.

1. Solver implementation을 import하거나 search cache를 믿지 않고 같은 immutable authority에서 candidate 사실을 다시 만든다.
2. Normal hard infeasibility, malformed state, arithmetic invalidity와 integrity corruption을 혼동하지 않는다.
3. Candidate PASS 뒤에만 finalization이 시작된다.
4. Final audit와 diagnostic이 실제 evidence 범위를 넘지 않는다.
5. Semantic result와 exact payload bytes의 identity가 분리되고 결정적이다.
6. Exact summary/operational-time/reference comparator의 hand oracle와 invalid/overflow/one-field sensitivity가 green이다.
7. Canonical required 69와 Target additional 5가 exact manifest/digest/discovery/XML 검증으로 모두 실행됐다.
8. Independent oracle가 실제 seeded defects를 assertion-red로 잡는다.
9. Candidate/result 두 PASS가 모두 있어야 Phase 08에 publishable output을 넘긴다.
10. Runtime/storage/provider 구현 없이 pure contract로 완료된다.
11. Immutable evidence, independent review와 valid acceptance receipt가 있다.

이 DoD는 Phase 14A benchmark acceptance, Phase 13 activation, Phase 14B production cutover를 완료했다는 뜻이 아니다.

## 16. 다음 Phase handoff와 broken 증상

### 16.1 Phase 06에서 받아야 할 것

```text
Candidate
  contract/version
  canonical immutable routes + bank
  problem/travel/profile/evaluation/SolvePlan refs and content digests
  non-authoritative route/solution claims
  candidate fingerprint/digest
  exact termination + last committed boundary

Replay
  build/runtime/algorithm/operator/state/config
  seed derivation and actual seeds
  requested/completed work
  stable trace and warm-start lineage

Evidence
  E-P06-COW / E-P06-ALNS / E-P06-REPLAY
  accepted Phase 06 review/receipt
```

받으면 안 되는 것: mutable trial/current/best alias, search cache handle, feasibility flag만 있는 summary, raw input/travel fallback, provider locator/client/credential, final outcome/diagnostic.

### 16.2 Phase 08에 넘길 것

```text
Phase07HandoffManifest
  contract/version/source/build/runtime
  problem/travel/profile/evaluation/SolvePlan/candidate/replay identities
  candidate pass or safe rejection
  verified solution/audit/result/result-pass identities when PASS
  payload digest/length when PASS
  Phase07Output contract fingerprint
  E-P07-* refs
  accepted review/receipt
  rollback point

Phase07Output
  Publishable(PublishableResult)
  or Rejected(VerificationRejected)
  or Rejected(GateIncomplete)
```

Phase 08은 `Publishable`만 normal success/result publication으로 mapping한다. `GateIncomplete`는 fabricated disposition 없이 `VERIFICATION_INCOMPLETE` 의미로 보존한다.

### 16.3 Handoff가 깨졌다는 증상

| 증상 | 깨진 계약 | 되돌아갈 owner/WP |
|---|---|---|
| Phase 07이 solver `CommittedCandidate` class를 import | Core-only projection 실패 | Phase 06/07, WP-07.0 |
| Evaluation declaration/`SolvePlan` mismatch가 route 실행 뒤 발견 | Authority precheck 순서 실패 | WP-07.0/1 |
| Same route/bank인데 cache에 따라 verdict가 다름 | Independent recomputation 실패 | WP-07.1/2 |
| Audit request가 evaluator callback을 받음 | Authority injection 가능 | Phase 05/07, WP-07.3 |
| Feasible witness 뒤 route가 바뀜 | No-audit-mutation 위반 | WP-07.3 |
| Outcome 수가 request universe와 다름 | Exactly-one final partition 실패 | WP-07.3/5 |
| Locale/timezone에 따라 payload digest가 다름 | Canonical encoding 실패 | WP-07.4 |
| Result fail인데 `COMPLETED`/payload가 보임 | Both-gate publication 실패 | WP-07.5/Phase 08 |
| Incomplete에 `INVALID` disposition이 붙음 | Failure/incomplete taxonomy 손실 | WP-07.5/Phase 08 |
| Phase 07 PASS를 benchmark accepted로 표시 | Authority overclaim | Phase 14A/scheduler |
| OR-Tools dependency가 Phase 07에 필요 | Phase 13 gate/architecture 침투 | Phase 00/13, WP-07.0 stop |

### 16.4 Downstream authority 보존

- Phase 10은 worker completion을 verifier PASS로 간주하지 않는다.
- Phase 11 AWS adapter는 verifier 의미를 재구현하지 않는다.
- Phase 14A는 approved corpus/protocol의 complete result bundle로 별도 quality/performance acceptance를 한다.
- Phase 13은 14A receipt와 C-17 승인이 있더라도 raw CP-SAT incumbent를 통과시키지 않고 materialize/full-evaluate/Phase 07 both-gate를 다시 사용한다.
- Phase 14B는 official manifest, Phase 11 evidence와 explicit production authority가 있어야 한다.

## 17. Source → requirement → WP → test/evidence 추적성

| Requirement | Source | WP | Exact test/evidence |
|---|---|---|---|
| `REQ-P07-SOURCE` current canonical, dated input, implementation baseline, live snapshot 분리 | Repository map; Canonical Domain/Architecture §1; user-fixed source set | Entry, 07.0, 07.6 | Source hash table; entry/live re-snapshot receipt; all `E-P07-*` |
| `REQ-P07-ARCH` verification은 solver/search/cache/provider와 독립 | Canonical Architecture §5~§7/§18, Integrated §3/§23 | 07.0, 07.6 | `Phase07VerificationArchitectureTest.*`; all `E-P07-*` |
| `REQ-P07-MAVEN-CLOSURE` fixture owner/direct test deps/reactor/Surefire/Failsafe fresh closure | Canonical Architecture §6~§7/§18~§19; live POM inventory | 07.0, 07.2, 07.6 | Dependency tree/artifact digest; `-am clean install`; no-`-am` selected; filter-free clean verify |
| `REQ-P07-AUTHORITY` problem/travel/profile/evaluation/`SolvePlan` exact equality | Master §4.2, Phase 04/06 handoff | 07.0, 07.1 | `CandidateVerifierAuthorityTest.*`; `E-P07-CANDIDATE-VERIFY` |
| `REQ-P07-SOLUTION-EVAL` accepted full-solution evaluator/identity | Phase 03 review F-P03-004, Phase 07 review F-P07-005 | Entry, 07.1 | Full/cache equality + corruption/comparator tests; blocker receipt |
| `REQ-P07-PAIR` pair/same vehicle/precedence/route-bank XOR | Master §6, Canonical Domain §3/§12 | 07.1, 07.2 | `CandidatePartitionCorruptionTest.*`; candidate evidence |
| `REQ-P07-CACHE` search cache/claim 비권위 | Master §12/§14.1 | 07.1, 07.2 | `CandidateVerifierIndependenceTest.*`, claim tamper |
| `REQ-P07-FAILURE` infeasible/invalid/corrupt/incomplete 분리 | Canonical Domain §15, Phase 07 review F-P07-002 | 07.0, 07.1, 07.5 | Route/partition/overflow/non-finite/both-gate tests |
| `REQ-P07-OUTCOME` solver final outcome은 assigned/unassigned만 | `Q-RES-01`, Master §10 | 07.3 | `FinalOutcomeTest.*`; `E-P07-AUDIT` |
| `REQ-P07-AUDIT` static proven 외 final-route exhaustive audit | `Q-RES-02`, Master §10.2/§14.1 | 07.3 | `FinalInsertionAuditTest.*`, completeness property |
| `REQ-P07-AUDIT-AUTH` caller callback 없는 bound evaluator authority | Phase 07 review F-P07-003 | 07.0, 07.3 | `FinalInsertionAuditAuthorityTest.*`; audit evidence |
| `REQ-P07-DIAGNOSTIC` evidence-bounded confidence | Canonical Domain §13/§16 | 07.3, 07.5 | Confidence overclaim tests; audit/result evidence |
| `REQ-P07-TWO-GATE` 두 PASS만 publication | Master §14.1, Integrated §11 | 07.5 | `BothGatePublicationTest.*`; result evidence |
| `REQ-P07-RESULT` exact 8-field summary와 operational-time checked derivation | Canonical Domain §14.1; Phase 07 §7.6/§8.4 | 07.4, 07.5 | `ResultSummaryContractTest.*`, sensitivity + tampered summary; result evidence |
| `REQ-P07-REFERENCE-COMPARATOR` four-field lex ascending/exact equality, customer objective와 분리 | Master §14.3; Canonical Domain §14.2; Integrated §11.6 | 07.4, 07.5 | `ReferenceResultComparatorTest.*`; hand oracle table; `E-P07-RESULT-VERIFY` |
| `REQ-P07-REPRO` deterministic semantic/payload identity | Master §13, `C-22` | 07.2, 07.4 | Encoding/replay tests; result evidence |
| `REQ-P07-ORACLE` independent corruption oracle와 defect sensitivity | Canonical Architecture §18.2~§18.3, Integrated §22.4, review F-P07-006 | 07.2, 07.6 | Oracle independence/sensitivity; all bundles |
| `REQ-P07-TEST-MANIFEST` canonical required 69와 additional 5 exact execution | Canonical Phase 07 §10.3~§10.7/§12.1; Canonical Architecture §18 | 07.0~07.6 | Required SHA-256 `aff4bae...d339`; additional `5374ef...5502`; JUnit discovery + fresh Surefire/Failsafe XML; all bundles |
| `REQ-P07-SECURITY` executable/PII/secret/provider locator 배제 | Integrated §20/§24 | 07.0, 07.3~6 | Contract/architecture/redaction inspection |
| `REQ-P07-HANDOFF-P06` candidate/replay/evidence만 소비 | Actual Phase 06 §16.2 | 07.0 | Authority/replay tests; predecessor receipt |
| `REQ-P07-HANDOFF-SCHEMA` one owner와 producer/consumer mapping, no solver import/pull-forward | Current Phase 06 human guide §15.2 | Entry, 07.0, 07.6 | `PHASE06_07_HANDOFF_SCHEMA_RECEIPT`; version/missing/unknown/digest compatibility tests |
| `REQ-P07-HANDOFF-P08` 세 variant lossless output | Actual Phase 08 §16.1 | 07.5, 07.6 | Both-gate + Phase 08 compatibility manifest |
| `REQ-P07-OPEN-GATE` official/default/gated scope 발명 금지 | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, Plan §14 | 전 WP | Config/source/architecture inspection; open snapshot |

새 field, diagnostic confidence, verifier shortcut, encoding rule 또는 public schema 요구가 나오면 source, owner, WP, exact test와 evidence를 이 표에 연결한다. 구현 편의를 위해 search claim을 authority로 만들거나 OPEN/GATED/deferred를 값으로 닫지 않는다.

## 18. 구현자가 마지막으로 확인할 짧은 판정표

| 질문 | 예라면 | 아니오라면 |
|---|---|---|
| Entry receipt와 solution evaluator contract가 accepted됐는가? | WP-07.0 시작 | 구현 중단 |
| Verification이 core만 의존하는가? | Authority test 진행 | Architecture 수정 |
| Candidate facts를 cache 없이 다시 만드는가? | Claim parity 진행 | Verifier 아님 |
| Candidate PASS 뒤에만 finalization하는가? | Audit 진행 | Lifecycle 결함 |
| Required audit가 complete하고 route를 안 바꾸는가? | Result draft 진행 | `GateIncomplete` |
| Result verifier가 별도 derivation/encoding을 하는가? | Both-gate 판단 | 독립성 결함 |
| 두 PASS가 모두 있는가? | `PublishableResult` 후보 | 정상 payload 금지 |
| Immutable evidence/review/receipt가 있는가? | Scheduler acceptance 제안 | 최대 pending evidence |
| Benchmark/production approval이 있는가? | 해당 Phase가 별도 판단 | Phase 07 PASS로 대신하지 않음 |

Phase 07의 가장 중요한 습관은 “solver가 맞다고 한 결과를 확인한다”가 아니라, **같은 immutable authority에서 solver의 주장을 버리고 다시 만들며, 그 뒤 만들어진 최종 결과까지 별도 gate로 다시 확인한다**는 것이다.
