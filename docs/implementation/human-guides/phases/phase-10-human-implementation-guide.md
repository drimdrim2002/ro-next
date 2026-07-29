# Phase 10 사람용 구현 가이드 — Provider-neutral coordinator

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 10 only
correction_status: CORRECTED_ROUND_01_AWAITING_INDEPENDENT_REVIEW
correction_round: "01"
canonical_phase_count: 15
phase: "10"
phase_name: provider-neutral-coordinator
canonical_phase_document: docs/implementation/phases/phase-10-provider-neutral-coordinator.md
canonical_phase_review: docs/implementation/reviews/phase-10-review.md
canonical_phase_document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS_AND_CROSS_PHASE_CONTRACTS
handoff_status_observed: NOT_READY
inventory_head_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_head_branch: codex-implementation
inventory_observed_date: 2026-07-29
live_inventory_snapshot_at: "2026-07-29T02:33:46+09:00"
live_inventory_status_sha256_porcelain_v1: da67223df253eb8e1a955e9bf9e884f59a65e85e4954210b002d79c9e9b2958b
head_baseline_shape: single-module-gcp-placeholder
live_drift_shape: concurrent-unaccepted-phase00-reactor-and-package-skeleton
live_drift_acceptance: NOT_ACCEPTED
canonical_fingerprint_scheme: git_hash_object_of_correction_snapshot
implementation_baseline_fingerprint_scheme: git_blob_at_inventory_head
live_inventory_fingerprint_scheme: git_hash_object_of_timestamped_worktree_file
source_sections_and_fingerprints:
  docs/master-design.md: "§1~4, §11.3, §13~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "§1~3, §10, §13~18; especially §14.3 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "§1~7, §10~20, §22; especially §11~14 and §17~18 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/architecture-domain-implementation-design.md: "§2~3, §12~16, §19~26, §29 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4; Q-BENCH-02, Q-INFRA-01, Q-VAR-01 | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
current_top_level_maps:
  README.md: "current repository/runtime map only | 6224b7c1bdac61e36f787a2e49bd36a00e6dab76"
  docs/README.md: "current design map and document hierarchy | 13f1b3b2dea038b8e0b466c138f5f59299413125"
historical_cross_check_only:
  docs/2026-07-26-domain-design.md: "dated predecessor; not canonical authority in this guide | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "dated predecessor; not canonical authority in this guide | d51339e251dee1e032e711144dc63d6d07d7323b"
implementation_baseline_sections_and_head_blobs:
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~6, Phase 06~12, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "HEAD §1~9 | 250aa90ae568a6b32ec905fa5ee456d430ff72cf"
  docs/implementation/phases/phase-08-application-ports-local-runtime.md: "§6~9, §16 | 2aff093a6f2728470a7ccbb22b7e1a1a71f5b963"
  docs/implementation/phases/phase-09-object-storage-no-database.md: "§3, §7~9, §15.2 | 99a5b0df5531a65964272f423cc0ccca4d4f1430"
  docs/implementation/phases/phase-10-provider-neutral-coordinator.md: "§1~15 | 2b909924008df6c6f34d7d4d4399c8fdf6b6830a"
  docs/implementation/reviews/phase-10-review.md: "§1~9 | 6c6e70a5c8e736787344176570a7f6f4be3f8186"
  docs/implementation/phases/phase-11-aws-reference-distribution.md: "§6.3, §9, §17~18 | 14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8"
live_worktree_fingerprints:
  docs/implementation/execution-progress-and-results.md: 0419f69199b3140dd44020f78278b1352e6517b8
  pom.xml: 1dc675ba17b7f2202f34a22131f152cc2868b075
  rpdptw/pom.xml: d02a560a0c661123d66046209c28eacb4ae5335c
  rpdptw/application/pom.xml: a580f6ca04ae661131bc820aeb4348b27aa5aadc
  build/pom.xml: a3afa3f4aa04d43c6654153f6e14a88276882871
  build/architecture-rules/pom.xml: cb14c68cd79c3db9636d0cd08b92016352dd1d3e
  build/test-fixtures/pom.xml: e033f9cda6de908a25431b6dfce5b0b0d8cd3ac1
expected_reader:
  - Java record, interface, sealed hierarchy와 Maven module dependency를 이해한다
  - CVRPTW route, time window, capacity propagation 경험이 있다
  - RPDPTW pair lifecycle, optimistic CAS와 distributed orchestration은 처음일 수 있다
owner_roles:
  implementation: RPDPTW Application/Coordinator owner
  upstream_worker: Phase 06 Solver/Search owner
  upstream_finalization: Phase 07 Verification/Result owner
  upstream_application: Phase 08 Application/Local Runtime owner
  upstream_storage: Phase 09 Object Storage owner
  independent_oracle: Phase 10 state-model/contract-test owner
  downstream_aws: Phase 11 AWS Reference owner
  security: Security/Tenant Boundary owner
  operations: Platform/Operations owner
  review: independent Phase 10 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P10-STATE
  - E-P10-COMPLETENESS
  - E-P10-RETRY
```

> 이 문서는 Java 기초와 CVRPTW 경험이 있는 사람이 Phase 10을 이해하고, entry gate가 열린 뒤 실제로 구현하며, 독립 evidence로 완료 여부를 판정하기 위한 교육형 지시서다. 현재 checkout에는 미승인 Phase 00 reactor/package skeleton이 보이지만 Phase 10 production type, test와 evidence는 없다. 아래 Java 이름·signature·file tree는 별도 표시가 없는 한 **제안 후보(PROPOSED INTERNAL)**이며, 현재 존재하는 API나 승인된 public/wire contract가 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 10은 solver worker가 만든 후보를 계산하는 단계가 아니라, immutable manifest와 exact state를 읽어 **“지금 허용되는 다음 동작 하나”**를 결정하고, 그 동작을 하나의 authoritative CAS로 기록한 뒤 provider-neutral action으로 내보내는 application coordinator 단계다.

핵심 성공 조건은 다음과 같다.

```text
provider 실행 순서, event 순서, process 수, crash/retry 횟수가 달라도
동일 logical manifest 아래에서는
  declared worker completeness
  champion lineage
  normal/exceptional termination
  publishable result identity
가 같아야 한다.
```

Coordinator는 AWS Step Functions도, GCP Workflows도, Kubernetes controller도 아니다. 그 실행 환경들이 호출하는 **portable state-transition brain**이다.

## 2. 큰 그림과 필요한 이유

### 2.1 Worker와 coordinator를 분리하는 이유

Worker는 한 assignment를 받아 ALNS search를 수행하고 candidate와 verifier report를 만든다. Coordinator는 여러 worker가 동일 manifest의 일부인지, 모두 끝났는지, retry가 같은 logical work인지, 어느 candidate가 다음 round champion인지, finalization과 publication을 시작해도 되는지를 판단한다.

```text
Worker가 답하는 질문
  “이 warm start와 seed/config로 요청된 search work를 수행한 결과는 무엇인가?”

Coordinator가 답하는 질문
  “선언된 모든 work가 정확히 완료·검증됐는가?”
  “이 round는 아직 기다려야 하는가, 더 진행할 길이 없어 INCOMPLETE인가?”
  “이 champion은 이전 champion보다 strictly better인가?”
  “취소·deadline·publication 중 어떤 terminal intent가 권위 있게 commit됐는가?”
```

둘을 합치면 다음 결함이 생기기 쉽다.

- 먼저 끝난 worker가 champion이 된다.
- 성공한 worker만 모아 partial round를 정상 완료로 처리한다.
- Retry가 seed나 warm start를 바꿔 다른 실험을 같은 work로 위장한다.
- Event payload의 `objective`가 exact stored candidate보다 더 강한 authority가 된다.
- Provider workflow가 comparator, verifier와 publication eligibility를 중복 구현한다.

### 2.2 CVRPTW의 병렬 실행 경험만으로 부족한 부분

CVRPTW batch runner에서도 여러 seed를 병렬 실행해 최솟값을 고를 수 있다. 그러나 이 프로젝트의 RPDPTW coordinator는 단순한 `min(objective)` 집계기가 아니다.

| 익숙한 batch pattern | Phase 10 계약 |
|---|---|
| 성공한 결과 중 최솟값 선택 | Manifest가 선언한 **모든** worker의 정상 완료와 candidate verifier `PASS`가 먼저 필요 |
| `double objective` 비교 | Upstream `BoundComparator`와 manifest-declared stable tie policy 사용 |
| 완료 event 수로 fan-in | Manifest의 exact assignment set과 exact committed outcome ref로 fan-in |
| 재시도 때 새 seed 허용 | 같은 `WorkerRunId`; `AttemptId`만 변경 |
| timeout이면 현재 best 반환 | `WATCHDOG_REACHED`/`PLATFORM_TIMEOUT`; normal quality termination과 분리 |
| 취소 flag가 보이면 끝 | Intent, same-run-state cancellation fence, 실제 worker 종료를 분리 |
| 결과 object가 있으면 success | Phase 07 both-gate `PublishableResultRef`와 Phase 09 publication pointer CAS 필요 |

### 2.3 Canonical 15 Phase에서의 위치

Canonical Phase는 **00~14, 총 15개**다. Phase 10만 이해하려 해도 producer와 consumer를 함께 알아야 한다.

| Phase | 주제 | 주요 producer → consumer 계약 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | Reactor, stable module, package ownership, dependency guard를 모든 후속 Phase에 제공 |
| 01 | Canonical input와 정규화 | Versioned normalized facts와 typed rejection을 Phase 02에 제공 |
| 02 | Prepared travel과 immutable problem | Complete directed travel, dense identity와 problem fingerprint를 Phase 03/05/07에 제공 |
| 03 | Route propagation과 evaluation | Cache-free facts, evaluation/comparator contract를 Phase 04/05/07/10에 제공 |
| 04 | Capability와 customer profile | Exact `BoundProfile`, evaluation declaration와 solve plan을 Phase 05/06/07에 제공 |
| 05 | Pair insertion과 initial portfolio | Stable route/bank, exact insertion, declared available portfolio slots를 Phase 06/10에 제공 |
| 06 | COW ALNS와 reproducibility | Worker assignment/result, committed candidate, termination/replay contract를 Phase 07/10에 제공 |
| 07 | Independent verification과 result | Candidate `PASS`, finalization facade와 `PublishableResult`를 Phase 08/10에 제공 |
| 08 | Application ports와 local runtime | Provider-neutral use case/port, logical identity, local reference를 Phase 09/10에 제공 |
| 09 | No-DB object storage | Immutable put/read, exact-key declared read, typed CAS/publication semantics를 **Phase 10**에 제공 |
| **10** | **Provider-neutral coordinator** | **State/action, all-declared fan-in, retry/cancel/deadline/publication orchestration을 Phase 11/12/14에 제공** |
| 11 | AWS reference distribution | Phase 10 action을 S3/Step Functions/Lambda에 얇게 mapping하고 parity evidence를 Phase 14B에 제공 |
| 12 | Provider substitution | 승인된 storage/workflow/compute 축만 같은 contract suite로 교체 |
| 13 | Optional hybrid route selection | **Phase 14A receipt와 C-17 승인 뒤에만** route pool/CP-SAT branch를 제공 |
| 14 | 14A benchmark / 14B official cutover | 14A ALNS benchmark receipt와 14B official/production authority를 별도 소유 |

ALNS-first 경로와 Phase 10의 위치를 분리해 읽는다.

```text
ALNS correctness/benchmark path:
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A

Provider-neutral production candidate path:
08 → 09 → [10] → 11
14A + 11 → 14B

Optional:
14A receipt + C-17 approvals → 13
```

Phase 13은 Phase 10의 선행조건이 아니다. Phase 10 baseline에 route pool, MIP, OR-Tools나 central selector를 미리 넣지 않는다. Phase 14A receipt도 Phase 14B production authority가 아니다.

### 2.4 Producer와 consumer 계약

```text
Phase 06 worker semantics ─┐
Phase 07 verifier/result ──┼─ exact identities and accepted contracts
Phase 08 application port ─┤
Phase 09 artifact/CAS ─────┘
             ↓
       Phase 10 coordinator
             ↓
  provider-neutral action/state/evidence
             ↓
Phase 11 AWS mapper / Phase 12 replacement / Phase 14 authority consumer
```

| 경계 | Producer가 보장해야 할 것 | Phase 10이 하면 안 되는 것 |
|---|---|---|
| Phase 06 → 10 | Stable `WorkerRunId`, requested/completed work, seed/warm-start/config, normal/exceptional termination | Search state, COW cache, seed derivation이나 candidate feasibility 재구현 |
| Phase 07 → 10 | Candidate PASS/rejection, comparator-ready verified identity, finalization output, both-gate publishable result | `verified=true` 합성, audit/result payload 생성, rejection 뒤 fallback candidate 자동 선택 |
| Phase 08 → 10 | `AdvanceSolve` seam, dispatcher/cancel/clock/telemetry port와 lossless application failure | Provider context나 ambient authority를 application 의미로 사용 |
| Phase 09 → 10 | Exact ref read, immutable put, one-pointer CAS, distinct typed preconditions와 typed storage failure | Prefix listing, ETag parsing, LWW, multi-object transaction, storage가 champion 결정 |
| Phase 10 → 11 | Stable action/state/failure/retry/cancel/deadline/publication contract와 evidence | ARN, S3 key, Lambda event 또는 ASL state를 generic contract에 추가 |
| Phase 10 → 14 | Complete verified normal terminal lineage | Test-only value, partial/recovery result를 official/production result로 승격 |

## 3. Source authority, fingerprint와 읽기 순서

### 3.1 Authority 역할과 시점을 먼저 분리한다

같은 문서도 역할과 관측 시점이 다르면 같은 종류의 authority가 아니다.

| 층 | 고정 경로/시점 | 이 가이드에서의 역할 | 하지 않는 일 |
|---|---|---|---|
| 사용자 고정 canonical 5문서 | `docs/master-design.md`, `docs/domain-design.md`, `docs/architecture-design.md`, `docs/architecture-domain-implementation-design.md`, `docs/master-design-open-questions.md`의 correction snapshot | 현재 domain/architecture/phase/question 의미 | dated predecessor가 이를 덮어쓰지 못함 |
| Current top-level maps | `README.md`, `docs/README.md`의 correction snapshot | 현재 repository/runtime 및 design hierarchy 경로 확인 | acceptance 판정 |
| Historical predecessor | `docs/2026-07-26-domain-design.md`, `docs/2026-07-26-architecture-design.md` | 변경 이유와 과거 표현 cross-check | current canonical authority |
| Implementation baseline | `inventory_head_commit`의 implementation README/plan/original Phase 10/review/인접 Phase | authoring 당시 계획·계약·review finding | live 구현 완료 판정 |
| HEAD snapshot | `7cc890ee1d0805df5ae14b633127fade4f978639` | tracked baseline 재현 | 동시 작성된 worktree 상태 설명 |
| Authoring/review snapshot | original Phase 10/review가 기록한 당시 inventory | finding이 왜 발생했는지 설명 | correction 시점 live inventory 대체 |
| Correction live snapshot | `2026-07-29T02:33:46+09:00` worktree와 metadata의 live fingerprint | module/POM/test/evidence가 지금 실제로 존재하는지 판정 | acceptance receipt 대체 |
| Accepted evidence | scheduler가 등록하고 independent reviewer가 승인한 receipt/evidence identity | gate와 구현 완료 판정 | HEAD, 파일 존재, green XML로 추정 |

`docs/implementation/README.md`와 realization plan이 dated Domain/Architecture를 “Final”로 가리키는 반면, 사용자 고정 5문서와 [현재 design map](../../../README.md)은 비날짜 경로를 canonical로 둔다. 이것은 `SOURCE_INDEX_CONFLICT`다. 이 guide는 사용자 고정 비날짜 문서를 requirement authority로 사용하고, dated 문서는 historical cross-check로만 읽는다. 구현 baseline 문서 자체의 source index를 바꾸거나 충돌을 조용히 숨기지 않는다.

### 3.2 충돌 해소 순서

```text
사용자 고정 지시
→ current top-level maps
→ canonical 5문서의 책임별 의미
→ 질문 등록부의 exact Q-* 상태
→ implementation baseline과 original Phase/review
→ 인접 Phase의 accepted contract
→ scheduler-owned accepted evidence
→ correction live inventory
→ dated/historical cross-check
```

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md), dated predecessor, deprecated 문서와 `docs/codex/*`는 역사 자료다. Phase 번호, 현재 API, provider 상태, evidence 또는 gate의 authority로 사용하지 않는다.

Canonical 문서 안에 남은 과거 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 [질문 등록부](../../../master-design-open-questions.md)의 최신 `Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. 이것은 Phase 10에 AWS code를 넣거나 Phase 11/14를 승인한다는 뜻이 아니다.

### 3.3 검증 가능한 source fingerprint와 semantic diff

Canonical/current/live 파일은 correction snapshot의 `git hash-object`, implementation baseline은 `inventory_head_commit`의 Git blob으로 고정했다. Hash가 바뀌면 숫자만 갱신하지 말고 인용 heading의 의미가 requirement, WP, test와 evidence에 미치는 영향을 다시 review한다.

| Source | 직접 읽을 heading/section | Fingerprint와 역할 |
|---|---|---|
| [Current repository map](../../../../README.md) | 전체 current repository/runtime map | correction snapshot `6224b7c1bdac61e36f787a2e49bd36a00e6dab76` |
| [Current design map](../../../README.md) | 전체 document hierarchy | correction snapshot `13f1b3b2dea038b8e0b466c138f5f59299413125` |
| [Canonical Master](../../../master-design.md) | §1, §4, §11.3, §13~17 | correction snapshot `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Canonical Domain](../../../domain-design.md) | §1~3, §10, §13~18, 특히 [§14.3](../../../domain-design.md#143-multi-round-execution-contract) | correction snapshot `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Canonical Architecture](../../../architecture-design.md) | §1~7, §10~20, §22, 특히 §11~14와 §17~18 | correction snapshot `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §2~3, §12~16, §19~26, §29 | correction snapshot `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~4와 exact `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | correction snapshot `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Dated Domain predecessor](../../../2026-07-26-domain-design.md) | 과거 용어/section만 비교 | historical `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Dated Architecture predecessor](../../../2026-07-26-architecture-design.md) | 과거 module/port 배치만 비교 | historical `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Implementation README](../../README.md) | §0~7 | HEAD baseline `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §2~6, Phase 06~12, §8~15 | HEAD baseline `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | HEAD §1~9와 live Phase 00 registry | HEAD `250aa90ae568a6b32ec905fa5ee456d430ff72cf`; correction live `0419f69199b3140dd44020f78278b1352e6517b8` |
| [Actual Phase 08](../../phases/phase-08-application-ports-local-runtime.md) | §6~9, §16 | HEAD baseline `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| [Actual Phase 09](../../phases/phase-09-object-storage-no-database.md) | §3, §7~9, §15.2 | HEAD baseline `99a5b0df5531a65964272f423cc0ccca4d4f1430` |
| [Canonical Phase 10](../../phases/phase-10-provider-neutral-coordinator.md) | 전체, 특히 §4, §7~10, §13~15 | HEAD baseline `2b909924008df6c6f34d7d4d4399c8fdf6b6830a` |
| [Phase 10 review](../../reviews/phase-10-review.md) | 전체, 특히 §4~9 | HEAD baseline `6c6e70a5c8e736787344176570a7f6f4be3f8186` |
| [Actual Phase 11](../../phases/phase-11-aws-reference-distribution.md) | §6.3, §9, §17~18 | HEAD baseline `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` |

Canonical-vs-dated semantic diff에서 Phase 10이 반드시 흡수할 변화는 다음과 같다.

- Canonical Domain §14.3은 manifest-declared **모든** worker의 정상 완료와 verifier 통과를 round gate로 둔다. 과거 “성공 결과 집계”로 축소하지 않는다.
- Canonical Architecture §11~14는 logical state/action/port와 provider adapter를 분리하고, §17은 tenant authorization과 lossless failure를 application 경계의 correctness로 둔다.
- Canonical Architecture §18~19는 test pyramid와 reactor/module 경계를 현재 build authority로 둔다. dated module sketch나 아직 없는 test module을 live 사실로 쓰지 않는다.
- 질문 등록부의 현재 상태는 canonical 본문 안의 오래된 숫자/상태보다 우선하지만, Phase 13/14 gate와 `EXPERIMENT_REQUIRED`를 자동으로 해제하지 않는다.

재검증 예:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/implementation/phases/phase-10-provider-neutral-coordinator.md
git hash-object docs/domain-design.md docs/architecture-design.md
git hash-object docs/implementation/execution-progress-and-results.md
```

인접 Phase/review의 whole-file digest, HEAD, test XML 또는 파일 존재는 acceptance receipt가 아니다. Entry에서는 인용 section의 의미와 scheduler-owned accepted artifact/evidence identity를 다시 확인한다.

### 3.4 구현 전 정확한 읽기 순서

| 순서 | 읽을 source/heading | 답해야 할 질문 | 확인할 module/file/evidence |
|---:|---|---|---|
| 1 | Current maps와 canonical 5문서 | 어느 경로가 current authority이며 tenant/failure/state 의미는 무엇인가? | `SOURCE_INDEX_CONFLICT`, current fingerprints |
| 2 | Master §4, §11.3, §13~14, §15.10 | Coordinator가 소유하는 의미와 publication AND gate는 무엇인가? | Logical fan-in, reproducibility, both-gate |
| 3 | Canonical Domain §2~3, §10, §13~18 | Pair, worker/round lineage와 정상·예외 종료의 domain 의미는 무엇인가? | Request pair, candidate/result authority |
| 4 | Canonical Architecture §1~7, §10~20 | Application module, authorized port, lossless failure, CAS, test/reactor는 어디에 배치되는가? | Package/POM/test lifecycle owner |
| 5 | Integrated §12~16, §19~26 | Phase 08/09 producer와 Phase 11 consumer가 Phase 10을 어떻게 둘러싸는가? | Port/state/artifact/action boundary |
| 6 | Question register exact rows | 공식 수치, AWS target, optional variant의 상태는 무엇인가? | `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` |
| 7 | Implementation README/plan | Entry, evidence DAG, DoD, rollback과 handoff는 무엇인가? | Baseline source-index conflict 포함 |
| 8 | Actual Phase 08 §6~9/§16 | Authorization, cancellation와 clock seam이 실제 accepted contract인가? | Accepted Phase 08 receipt |
| 9 | Actual Phase 09 §3/§7~9/§15.2 | Authorized storage scope, exact outcome, lossless failure와 typed CAS가 승인됐는가? | Accepted Phase 09 receipt |
| 10 | Canonical Phase 10와 review 전체 | Proposed state/action/WP/test와 correction finding은 무엇인가? | F-P10 및 HG10-R001~R005 |
| 11 | Actual Phase 11 §6.3/§9/§17~18 | AWS mapper와 local E2E가 어떤 exact contract/lifecycle을 기다리는가? | Handoff shape; acceptance가 아님 |
| 12 | Execution Progress의 HEAD/live | authoring status와 correction live status가 어떻게 다른가? | Scheduler-owned registry |
| 13 | Actual source/POM/test inventory | 기대 module/type/test가 Maven에서 정말 발견되는가? | dependency/profile/Surefire/Failsafe/effective POM |
| 14 | Accepted evidence/receipt | 구현·evidence가 독립 승인됐는가? | HEAD/live와 별도 identity |

### 3.5 Entry gate 판정 순서

다음 질문이 모두 `예`가 아니면 production implementation을 시작하지 않는다.

1. Phase 00 module/package/architecture가 accepted인가?
2. Phase 06 worker termination, retry-stable identity와 committed candidate가 accepted인가?
3. Phase 07 candidate/result verifier와 finalization facade가 accepted인가?
4. Phase 08 provider-neutral application port, cancellation/clock와 local E2E가 accepted인가?
5. Phase 09 exact committed-outcome authority, artifact/state/publication CAS가 accepted인가?
6. Caller-supplied `TenantId`가 아닌, 명시적 **authorized solve/storage scope**가 모든 lookup/read/decode/dispatch보다 먼저 승인·주입되는가?
7. 각 storage operation의 denied/not-found/conflict/corrupt/indeterminate/partial outcome을 손실 없이 보존하는 failure carrier가 승인됐는가?
8. Pending action authorization과 distinct publication-pointer precondition의 typed API가 공동 승인됐는가?
9. Same-run-state `CANCEL_REQUESTED` 대 `PUBLISHING` fence가 Phase 08/10/11에 정렬됐는가?
10. Restart-safe durable monotonic deadline representation/ADR가 승인됐는가?
11. Root/application/build POM owner, direct test dependency, module edge, Surefire/Failsafe lifecycle와 strict zero-test policy가 승인됐는가?
12. Scheduler task, implementer, independent model-oracle author, reviewer가 지정됐는가?
13. Test/experiment manifest가 값을 생략하지 않고 `TEST_ONLY` authority를 명시하는가?

현재 답은 대부분 `아니오`다. 따라서 가능한 작업은 이 가이드, contract review, pure reference model과 compile/dependency-closed future-red test 설계까지다. Fake predecessor, caller tenant claim, lossy adapter, zero-test green 또는 legacy GCP behavior로 gate를 넘지 않는다.

## 4. RPDPTW primer, 용어집, identity와 lifecycle

### 4.1 CVRPTW customer와 RPDPTW request

CVRPTW에서는 customer 하나를 route 위치 하나에 넣는 모델이 흔하다. RPDPTW에서는 request가 pickup과 delivery를 한 쌍으로 소유한다.

```text
Request R
  pickup visit P
  delivery visit D

stable state에서 반드시:
  same concrete vehicle
  P before D
  each required visit exactly once
  request가 route 하나 또는 bank에 정확히 하나
```

Delivery-only도 pair identity는 유지하지만 logical pickup이 physical visit, travel, stop이나 service를 만들지는 않는다. Coordinator는 이 domain 규칙을 다시 계산하지 않는다. 다만 worker outcome과 verified candidate가 **같은 problem/profile/manifest 계보**인지 확인하여 잘못된 pair solution이 fan-in으로 들어오지 못하게 한다.

### 4.2 용어집

| 용어 | 이 가이드의 뜻 | 흔한 오해 |
|---|---|---|
| `SolveSnapshot` | Problem, prepared travel와 bound profile을 고정한 immutable solve authority | Raw input나 provider object |
| `ExecutionManifest` | Snapshot/build/algorithm/seed/worker/round/tie/retry/deadline plan | 실행 중 조회하는 mutable config |
| `Round` | 같은 warm start를 선언된 독립 worker가 개선하고 완전한 fan-in으로 champion을 고르는 단위 | Worker 한 개 또는 workflow loop 한 번 |
| `WorkerRunId` | Round/ordinal/warm-start/config/requested work가 같은 logical worker | Lambda invocation ID |
| `AttemptId` | 같은 logical worker의 물리 재시도 ordinal | 새 seed를 허용하는 새 work |
| `ActionId` | Durable pending side effect의 semantic identity | CAS version 또는 provider request ID |
| `StateVersion` | Run-state CAS equality에만 쓰는 opaque token | 증가하는 business sequence |
| `transitionOrdinal` | Successful semantic transition lineage ordinal | CAS token 대체물 |
| `ArtifactRef` | Kind/schema/digest/opaque locator를 가진 immutable content reference | Raw S3/GCS/path |
| Declared completeness | Manifest가 선언한 모든 logical work가 exact refs로 완료·검증됨 | 성공 event 수가 worker 수와 같음 |
| Champion | Complete verified candidate set에서 stable comparator/tie로 고른 candidate | 먼저 끝나거나 raw scalar가 가장 작은 candidate |
| Strict adoption | Round champion이 이전 champion보다 `STRICTLY_BETTER`일 때만 lineage 교체 | `EQUAL` tie candidate로 교체 |
| Pending action | State CAS에 함께 저장되어 crash 후 같은 identity로 재실행할 외부 동작 | CAS 뒤 메모리에만 남는 callback |
| Cancellation intent | 취소 요청을 durable하게 기록한 사실 | Terminal fence 또는 실제 종료 |
| Publication fence | Run-state `PUBLISHING` CAS가 terminal intent를 publication으로 고정한 사실 | Result object 존재 |
| `INCOMPLETE` | 승인된 retry/wait 진행 경로 없이 declared work를 완결하지 못함 | 아직 기다리는 `ROUND_RUNNING` |

### 4.3 Identity 계층

Provider metadata가 semantic identity로 스며들지 않도록 계층을 먼저 그린다.

```text
TenantId
└─ SolveId + ManifestFingerprint = ExecutionRunId
   ├─ PhaseOneScreenRunId
   │  └─ AttemptId
   ├─ RoundId(roundOrdinal)
   │  └─ WorkerRunId(workerOrdinal, warmStart, config, requestedWork)
   │     └─ AttemptId
   └─ ActionId(actionOrdinal, kind, payloadFingerprint)
```

반드시 구분한다.

```text
logical identity:
  SolveId, ManifestFingerprint, RoundId, WorkerRunId, ActionId

physical observation:
  Step Functions execution ARN, Lambda request ID, pod UID,
  provider retry count, S3 version/ETag, event delivery ID
```

Physical observation은 telemetry와 runtime provenance에는 들어갈 수 있지만 candidate/result quality identity, seed, champion이나 action payload fingerprint를 바꾸면 안 된다.

### 4.4 Solve lifecycle

권위 lifecycle의 추천 shape:

```text
SUBMITTED
→ PREPARING
  → snapshot prepared
  → phase-1 declared screens dispatched
  → all available screens completed and verified
→ PREPARED(phase-one champion)
→ ROUND_DISPATCHING
→ ROUND_RUNNING
→ ROUND_VERIFYING
→ ROUND_AGGREGATING
├─ strictly better + next declared round → ROUND_DISPATCHING
├─ equal/worse → FINALIZING(NO_STRICT_IMPROVEMENT, previous champion)
└─ last round strict improvement → FINALIZING(MAX_ROUNDS_REACHED, round champion)
→ PUBLISHING
→ SUCCEEDED
```

Exceptional lifecycle은 정상 품질 종료와 분리한다.

```text
REJECTED_INPUT
BINDING_FAILED
CANCEL_REQUESTED → CANCELLED
WATCHDOG_REACHED
RESOURCE_LIMIT_REACHED
PLATFORM_TIMEOUT
FAILED
INCOMPLETE
PUBLICATION_REJECTED
```

Worker `MAX_STEPS_REACHED`는 worker candidate 자격이지 solve terminal reason이 아니다. `NO_STRICT_IMPROVEMENT`와 `MAX_ROUNDS_REACHED`만 complete verified batch 뒤의 정상 solve termination 후보다.

### 4.5 Coordinator invariant

구현 전에 다음 문장을 팀원이 자기 말로 설명할 수 있어야 한다.

1. Manifest는 solve 시작 뒤 변하지 않는다.
2. Every ref/state/action은 같은 tenant/solve/manifest lineage를 가진다.
3. Retry는 `AttemptId` 외 seed, warm start, config와 requested work를 바꾸지 않는다.
4. Immutable artifact를 쓰고 verified-read한 뒤 pointer/state 하나를 CAS한다.
5. 한 invocation은 authoritative CAS를 최대 한 번만 시도한다.
6. Correctness는 distributed lock, leader lease, singleton process에 의존하지 않는다.
7. CAS된 pending action은 crash 뒤 같은 `ActionId`로 재실행된다.
8. Action executor는 exact current state의 pending action identity/payload/ordinal을 다시 확인한다.
9. Event와 listing은 wake-up hint일 뿐 state, outcome과 completeness authority가 아니다.
10. Declared work 하나라도 missing/failed/abnormal/unverified/conflicting이면 champion과 finalization을 막는다.
11. Retry/wait가 남아 있으면 running/waiting이고, 승인된 진행 경로가 없을 때만 `INCOMPLETE`다.
12. Candidate verifier `PASS`가 없는 outcome은 comparator input이 아니다.
13. Reduction order는 stable worker ordinal이다.
14. `EQUAL`/`WORSE`이면 이전 champion을 finalization에 넘긴다.
15. Cancellation intent object는 request이고 같은 run-state `CANCEL_REQUESTED` CAS가 fence다.
16. `PUBLISHING` CAS가 이기면 late cancel은 too-late이며 publication reconciliation을 계속한다.
17. Deadline terminal CAS 뒤 late success는 normal result를 되살리지 못한다.
18. Coordinator는 Phase 07 outcome/audit/summary/payload/PASS를 합성하지 않는다.
19. `SUCCEEDED`는 both-gate exact result publication이 same digest로 성공·수렴한 뒤에만 가능하다.
20. Run-state, worker-commit, publication-pointer precondition token은 서로 다른 typed identity다.
21. Official numeric value가 없으면 omitted default가 아니라 binding failure다.
22. Cross-tenant ref는 artifact deserialization이나 dispatch 전에 거부한다.
23. Failure state에는 normal route/outcome/benchmark payload가 없다.
24. Phase 13 optional hybrid가 baseline state/action에 숨어 들어오지 않는다.
25. Request의 `TenantId`는 untrusted selector일 뿐 authorization proof가 아니다. 명시적 authorized scope가 없거나 tenant/solve와 불일치하면 backend lookup, existence check, read, decode, CAS와 dispatch를 모두 시작하지 않는다.
26. `ACCESS_DENIED`, `NOT_FOUND`, `CORRUPT`, `PRECONDITION_FAILED`, `VISIBILITY_INDETERMINATE`, `PARTIAL_WRITE_ABORTED` 같은 operation failure는 서로 치환하거나 `Optional.empty`, `false`, generic exception으로 축약하지 않는다.
27. Storage failure가 난 transition은 last safe point의 state/pointer를 유지하고 pending action을 새로 publish하지 않는다. Exact failure category에 대응하는 승인된 state transition이 있을 때만 그 transition을 별도 CAS한다.

## 5. 실제 repository inventory: HEAD와 live drift를 섞지 않기

| Resume 판단 층 | 확인할 것 | 현재 판정 |
|---|---|---|
| HEAD baseline | `inventory_head_commit`의 tracked source/POM | Single-module GCP placeholder |
| Authoring/review snapshot | Original Phase 10과 review가 관찰한 inventory/finding | Historical explanation; correction live 대체 불가 |
| Correction live snapshot | Timestamp, status digest, live file blob, current module/test count | Concurrent Phase 00 scaffold; Phase 10 type/test/evidence 0 |
| Accepted evidence | Scheduler registry + independent receipt + immutable evidence digest | Phase 10 receipt 없음; resume/implementation 시작 불가 |

`resume`는 live 파일이 늘었다는 이유로 열리지 않는다. Accepted predecessor receipt와 approved contract가 있고, 그 identity가 현재 source/effective POM/test evidence에 연결될 때만 열린다.

Phase 10 착수 직전에는 HEAD, timestamp/timezone, porcelain status digest, relevant POM/progress blob, wrapper, source/test/evidence count를 다시 snapshot한다. 이 문서의 live blob이 바뀌었다는 사실만으로 long-lived acceptance key를 갱신하거나 resume하지 않고, accepted commit/bundle/review/receipt identity를 새 entry evidence로 사용한다.

### 5.1 HEAD baseline

HEAD `7cc890ee1d0805df5ae14b633127fade4f978639`의 tracked baseline은 root 단일 Maven JAR와 `com.ronext.optimizer` GCP placeholder다.

| HEAD path | 관찰 | Git blob | Phase 10 의미 |
|---|---|---|---|
| `pom.xml` | Single implicit JAR, GCP SDK가 root dependency | `f8a411eadd4a5c01d8dd09fdea462738ca63d65f` | Target reactor/application boundary가 아님 |
| `AlnsBatchEngine.java` | Synthetic objective placeholder | `18bfb344faaefe9373e02b007446e7bf15c3443c` | Worker candidate/verification authority가 아님 |
| `OptimizationApiController.java` | GCP workflow/storage와 numeric fallback | `7889e7b6994b5655d04de329c9e6873fbcbe4efb` | Provider-neutral manifest/identity가 아님 |
| `OptimizationWorkerController.java` | Visible candidate listing과 raw objective selection | `0ccd699cc9bf178ccb51de488a899af166b82fa2` | Declared completeness/CAS/both-gate를 위반하는 migration inventory |
| `AlnsBatchEngineTest.java` | Placeholder test 1개 | `a1bbb8cc7397ce5a69dd78d2c07ca44b61dee67e` | Phase 10 test/evidence가 아님 |
| `gcp/workflows/optimization.yaml` | Parallel calls 뒤 finalize | `69629d072e36ce9a7229bc1fb6bea9d1ee2327b9` | Thin provider mapper의 target evidence가 아님 |

### 5.2 Live worktree drift

동일 checkout에는 다른 작업자가 만드는 **미승인 Phase 00 scaffold**가 보인다. 다음 inventory는 `2026-07-29T02:33:46+09:00`에 고정했고, 당시 `git status --porcelain=v1 -uall` SHA-256은 `da67223df253eb8e1a955e9bf9e884f59a65e85e4954210b002d79c9e9b2958b`다. 이 가이드는 그 변경을 소유하거나 수정하지 않는다.

| Live item | 관찰 | Live blob/상태 | 해석 |
|---|---|---|---|
| Root `pom.xml` | Parent/aggregator로 변경 중 | `1dc675ba17b7f2202f34a22131f152cc2868b075` | HEAD 대비 drift; 아직 accepted receipt 없음 |
| `rpdptw/pom.xml` | Core/solver/verification/application/capability/profile reactor | `d02a560a0c661123d66046209c28eacb4ae5335c` | Phase 00 target shape의 scaffold |
| `rpdptw/application/pom.xml` | Core/solver/verification dependency | `a580f6ca04ae661131bc820aeb4348b27aa5aadc` | Phase 10 예상 module은 존재하지만 type은 없음 |
| `build/pom.xml` | `test-fixtures`, `architecture-rules`만 aggregation | `a3afa3f4aa04d43c6654153f6e14a88276882871` | Port contract module/lifecycle은 없음 |
| `build/test-fixtures/pom.xml` | Core test dependency와 JUnit, test-jar attach | `e033f9cda6de908a25431b6dfce5b0b0d8cd3ac1` | Application coordinator fixture owner가 아님 |
| `build/architecture-rules/pom.xml` | Stable module test edges와 JUnit | `cb14c68cd79c3db9636d0cd08b92016352dd1d3e` | Phase 10 architecture test는 없음 |
| `application.execution/package-info.java` | Logical identity/lifecycle ownership 설명 | `d5c4b79a42a5d4e2a01bbc62ad26abd46254b2a5` | Package placeholder만 존재 |
| `application.port.in/package-info.java` | Provider-neutral inbound ownership 설명 | `3ca3b6788da6a87527dbfc1582cf8422b4ba9500` | `AdvanceSolve` type은 없음 |
| `application.port.out/package-info.java` | Provider SDK 금지 설명 | `28a34e23ace9bd968b89c7a1dad95374e75f911e` | Port interface 구현/acceptance가 아님 |
| Maven wrapper | `mvnw`, `mvnw.cmd`, wrapper properties 존재 | live path inventory | Future verification은 system Maven이 아니라 wrapper 사용 |
| Progress registry | Phase 00 review 02 `CHANGES_REQUIRED`, Fix 02 `IN_PROGRESS`, evidence `REJECTED_PENDING_FIX_02_REGENERATION`, receipt `NOT_PRODUCED` | live blob `0419f69199b3140dd44020f78278b1352e6517b8` | Phase 10은 계속 `BLOCKED_NOT_IMPLEMENTED` |

관찰 시점의 toolchain은 Java `25.0.3`, Maven `3.9.14`다. 이는 toolchain inventory일 뿐 Phase 10 acceptance가 아니다.

### 5.3 존재, placeholder와 부재

| 대상 | 현재 상태 | 목표 상태 |
|---|---|---|
| `rpdptw/application` Maven module | **존재하나 미승인 scaffold** | Accepted Phase 00/08 module |
| Application package ownership | `package-info.java` 4개 | 실제 versioned contracts + architecture enforcement |
| Phase 10 non-`package-info` main type | **0개** | Identity/state/action/reducer/coordinator |
| `rpdptw/application/src/test` | **부재/0개** | Unit/model/fault/replay/integration tests |
| `build/architecture-rules` | Java 9개, Surefire `*Test.java` 8개; Phase 10 named test 0개 | `Phase10CoordinatorArchitectureTest` |
| `build/test-fixtures` | Test Java 1개와 attached test-jar | Generic stable fixture만; application fixture를 역방향 의존시키지 않음 |
| `build/port-contract-tests` | **module/POM/source 모두 부재** | Approved POM + concrete `*Test` under `src/test/java` |
| Coordinator type name match | **0개** | `AdvanceSolve`, `SolveState`, `CoordinatorReducer`, `SolveCoordinator` 등 |
| Phase 10 named test match | **0개** | §10 test matrix |
| `E-P10-*` evidence path | **0개** | 세 immutable evidence bundle |
| Non-target `E-P10-*` 또는 Phase 10 acceptance receipt | **0개** | Independent review 뒤 scheduler-owned receipt |
| Root JUnit/Surefire | JUnit은 dependency management만, Surefire는 plugin management와 `failIfNoTests=false` | Phase owner가 strict selection/failure를 명시 |
| Failsafe/local IT profile | version/property/plugin/execution/profile **부재** | Explicit `phase10-local-it` lifecycle와 strict selected IT |
| AWS/provider adapter | Phase 10 target에는 없음 | 계속 없음; Phase 11 소유 |

현재 실제 확인에 쓸 수 있는 read-only 명령:

```bash
git rev-parse HEAD
git status --porcelain=v1 -uall

find rpdptw/application/src/main/java \
  -type f -name '*.java' ! -name 'package-info.java' -print

find rpdptw/application/src/test \
  -type f -name '*.java' -print 2>/dev/null

rg -n 'interface AdvanceSolve|sealed interface SolveState|class SolveCoordinator' \
  rpdptw build -g '*.java'

test ! -e build/port-contract-tests/pom.xml
```

Target test가 없는데 selected Maven command를 실행해 green이라고 주장하지 않는다. 현재 root `pom.xml`의 general `failIfNoTests=false`도 Phase 10 test 발견을 보장하지 않으며, 이전 `target/surefire-reports` XML은 current source의 증거가 아니다.

### 5.4 PROPOSED target tree

Accepted predecessor가 같은 의미를 다른 package/type으로 제공하면 중복 생성하지 않고 mapping을 먼저 review한다.

```text
pom.xml                                           # parent plugin/version policy owner
rpdptw/application/pom.xml                        # app dependency + unit/IT lifecycle owner

rpdptw/application/
├── src/main/java/com/ronext/rpdptw/application/
│   ├── port/in/
│   │   └── AdvanceSolve.java
│   ├── port/out/
│   │   ├── ArtifactStore.java
│   │   ├── RunArtifactRepository.java
│   │   ├── RunStateRepository.java
│   │   ├── ResultPublisher.java
│   │   ├── WorkerDispatcher.java
│   │   ├── CancellationPort.java
│   │   ├── FinalResultGateway.java
│   │   ├── MonotonicClock.java
│   │   └── TelemetryPort.java
│   ├── execution/
│   │   ├── ExecutionIdentity.java
│   │   ├── ExecutionManifest.java
│   │   ├── WorkerAssignment.java
│   │   └── WorkerCompletion.java
│   └── coordinator/
│       ├── SolveState.java
│       ├── CoordinatorAction.java
│       ├── CoordinatorDecision.java
│       ├── CoordinatorReducer.java
│       ├── StableRoundChampionSelector.java
│       └── SolveCoordinator.java
└── src/test/java/com/ronext/rpdptw/application/coordinator/
    ├── support/CoordinatorScenarioBuilder.java
    ├── support/IndependentCoordinatorModel.java
    ├── support/InMemoryCasStateRepository.java
    ├── support/ScriptedWorkerDispatcher.java
    ├── SolveCoordinatorStateMachineTest.java
    ├── CoordinatorIdempotencyConcurrencyTest.java
    ├── CoordinatorCrashResumeTest.java
    ├── CoordinatorEventOrderingTest.java
    ├── PhaseOneFanInTest.java
    ├── DeclaredRoundCompletenessTest.java
    ├── MultiRoundChampionTest.java
    ├── CoordinatorCancellationDeadlineTest.java
    ├── CoordinatorIntegrityTest.java
    ├── CoordinatorAuthorizationFailureContractTest.java
    ├── CoordinatorFinalizationPublicationTest.java
    ├── CoordinatorReplayTest.java
    ├── CoordinatorModelBasedPropertyTest.java
    └── Phase10CoordinatorIT.java

build/pom.xml                                      # build test-module aggregator owner

build/architecture-rules/
├── pom.xml
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase10CoordinatorArchitectureTest.java

build/port-contract-tests/                    # 현재 부재; predecessor-approved FUTURE
├── pom.xml
└── src/test/java/com/ronext/rpdptw/contract/coordinator/
    ├── CoordinatorPortContractSupport.java       # reusable support; test selector 아님
    └── CoordinatorPortContractTest.java          # concrete Surefire test
```

### 5.5 Maven compile/dependency/discovery closure

아래는 completed build가 아니라 **entry-gated proposed closure**다. POM owner가 승인하고 effective POM으로 확인되기 전에는 구현하지 않는다.

| Owner | 필요한 future 변경 | Compile/dependency 이유 | Discovery/실행 책임 |
|---|---|---|---|
| Root `pom.xml` | JUnit/Surefire/Failsafe version policy, 필요 시 Failsafe plugin management | Version drift 방지; execution을 자동 생성하지 않음 | General `failIfNoTests=false`를 Phase 10 green 근거로 사용 금지 |
| `rpdptw/application/pom.xml` | JUnit 직접 test dependency, 승인되어 실제 쓰는 property library만 직접 추가 | App-local test와 independent model이 compile됨 | `*Test`는 Surefire; `Phase10CoordinatorIT`는 explicit `phase10-local-it` Failsafe `integration-test`/`verify` |
| `build/port-contract-tests/pom.xml` | Application + JUnit test-scope dependency; build aggregator 등록 | Contract test가 실제 port/API를 compile함 | Concrete `CoordinatorPortContractTest`를 Surefire가 발견; main-source suite 금지 |
| `build/architecture-rules/pom.xml` | Application test-scope edge 유지/확인 | ArchUnit test가 Phase 10 package를 resolve함 | `Phase10CoordinatorArchitectureTest`를 Surefire가 발견 |
| Test fixture owner | Coordinator scenario/model/fake는 application test source가 기본 | `build/test-fixtures`가 application으로 역의존하는 cycle 방지 | Cross-module fixture 공개가 필요하면 별도 approved test artifact/edge 후 이동 |

```text
reactor dependency order:
rpdptw/core, solver, verification
  → rpdptw/application
    → build/port-contract-tests
    → build/architecture-rules

test lifecycle:
application *Test       → Surefire test
application *IT         → Failsafe integration-test + verify, profile phase10-local-it
port/architecture *Test → each module Surefire test
```

`phase10-local-it`는 future profile 이름 후보이며 숫자·timeout·backend를 발명하는 default가 아니다. Exact profile activation, dependency와 test class가 accepted되지 않으면 `OPEN/CROSS-PHASE BLOCKED`다. Property-based library는 manifest에 generator/seed/shrink evidence가 필요하고 실제 사용이 승인된 경우에만 추가한다.

Selected no-`-am` port/architecture run은 바로 앞 same-source `clean install`이 만든 application POM/JAR의 exact digest를 manifest에 기록해 소비한다. Local repository에 있던 미상/stale artifact를 재사용하거나 install source와 selected source가 다르면 discovery evidence는 실패다.

## 6. Scope, non-scope, 결정 상태와 사람 승인

### 6.1 Phase 10 in scope

- Manifest-bound solve/round/worker/attempt/action identity
- Immutable state/action/artifact schema와 legal transition
- Pure coordinator reducer
- Artifact-before-pointer, one-invocation/one-authoritative-CAS
- Durable pending action과 crash/resume reconciliation
- Phase-1 declared screen fan-out/fan-in
- Phase-2 declared worker fan-out/fan-in
- Duplicate/out-of-order/late/stale/lost hint 처리
- Retry에서 logical work equality 보존
- Stable champion과 strict improvement lineage
- Normal termination과 exceptional failure 분리
- Cancellation request/fence/actual termination 분리
- Deadline/watchdog observation과 late-success fence
- Phase 07 finalization과 Phase 09 publication orchestration
- Model-based, fault, corruption, replay, security와 architecture evidence

### 6.2 명시적 non-scope

- Phase 03/04 comparator, objective, tie-quality 의미 구현
- Phase 05/06 ALNS, COW, seed derivation, completed-step 계산 구현
- Phase 07 verifier, final audit, outcome, payload, PASS 생성
- Phase 08 public HTTP/wire schema와 local backend 구현
- Phase 09 object key encoding, storage backend, checksum algorithm, CAS primitive 구현
- AWS S3/Step Functions/Lambda SDK/event/IaC/deployment
- GCP/ECS/Kubernetes provider adapter
- Distributed lock, leader lease, database, multi-object transaction
- Phase 13 route pool/MIP/OR-Tools/central selector
- `Q-BENCH-02`의 공식 step/worker/round/watchdog 값
- Exceptional last best의 normal/official 자동 승격 정책
- Public Java API나 canonical wire schema의 최종 승인

### 6.3 확정, proposed, open, gated와 deferred

| 항목 | 상태 | 구현자가 할 수 있는 것 | 하면 안 되는 것 |
|---|---|---|---|
| Coordinator semantic owner | FIXED | Application에 state/completeness/retry/cancel/finalization sequencing 배치 | Provider workflow나 storage에 의미 분산 |
| Exact event authority | FIXED | Exact state/manifest/ref 재독해 | Event/listing payload로 state 대체 |
| Declared completeness | FIXED | 모든 declared normal completion + candidate PASS 요구 | Partial success champion |
| Retry identity | FIXED | `AttemptId`만 변경 | Seed/warm start/config/work 변경 |
| State/action Java name | PROPOSED INTERNAL | 의미 보존 아래 review·변경 | Public compatibility로 선언 |
| Single-CAS/pending action | PROPOSED SAFE BASELINE | Pure reducer + optimistic CAS 설계 | Lock/lease를 correctness 전제 |
| Authorized solve/storage scope | CROSS-PHASE BLOCKED | Security + Phase 08/09/10 owner가 non-ambient scope와 operation coverage 승인 | Command의 `TenantId`, thread-local, object prefix를 proof로 사용 |
| Lossless storage failure carrier | CROSS-PHASE BLOCKED | Operation별 typed failure와 state disposition review/red test | `Optional.empty`, boolean, generic retryable exception으로 축약 |
| Publication precondition API | CROSS-PHASE BLOCKED | Distinct typed API review/red contract test | Run-state token 재사용 |
| Cancellation/publication race | FIXED MEANING / API REVIEW | Same run-state CAS fence로 모델링 | Intent object write 순서로 winner 결정 |
| Durable deadline restart | CROSS-PHASE BLOCKED | `TEST_ONLY` virtual clock model | Wall clock/provider remaining time fallback |
| Maven test lifecycle | PROPOSED / BUILD-OWNER GATED | POM/dependency/profile/Surefire/Failsafe closure와 exact discovery manifest review | Zero-test green, stale XML, main-source suite |
| `Q-BENCH-02` | OPEN — EXPERIMENT_REQUIRED | Explicit test/experiment value | Official/production default 발명 |
| `Q-INFRA-01` | RESOLVED TARGET | Phase 11 handoff shape 검토 | Phase 10에 AWS SDK/type 추가 |
| Phase 13/C-17 | GATED TARGET | Baseline에서 disabled/absent 유지 | MIP를 Phase 10 prerequisite/default로 추가 |
| `Q-VAR-01` | DEFERRED | 현재 pair/terminal/bank 보존 | 질문·설계·활성화 |
| Phase 14A | NOT RUN/receipt absent | Phase 10 evidence와 구분 | Correctness test를 benchmark receipt로 승격 |
| Phase 14B | PRODUCTION AUTHORITY NOT GRANTED | Handoff contract만 준비 | Official/production cutover 주장 |

### 6.4 사람 checkpoint와 마지막 안전 지점

| Checkpoint | 반드시 승인할 사람 | 승인 전 마지막 안전 지점 | Stop 조건 |
|---|---|---|---|
| Entry receipt | Scheduler + Phase 06~09 owner/reviewer | 문서와 red model/test plan | Accepted predecessor/evidence 없음 |
| Identity/state/action freeze | Application/Coordinator + Architecture | Pure value/model branch | 같은 의미 type이 predecessor에 이미 있거나 token 역할 모호 |
| Tenant authorization | Security + Phase 08/09/10 owners | Command validation only; backend lookup/dispatch 0 | Explicit authorized scope가 없거나 operation coverage 불명 |
| Storage failure oracle | Phase 09/10 + independent oracle owner | Last safe state/pointer, side effect 0 | Failure category가 소실되거나 state disposition 불명 |
| Pending action/publication | Phase 08/09/10/11 owners | Pure reducer, publication 불가 | Distinct precondition/read-reconcile API 미승인 |
| Cancellation race | Phase 08/10/11 owners | Durable intent만 저장, side effect 없음 | Intent와 terminal fence를 같은 write로 볼 수 없음 |
| Deadline restart | Application/Operations | Virtual clock only, production deadline path off | JVM restart 뒤 tick 비교 규칙 없음 |
| Finalization integration | Phase 07/09/10 owners | Verified champion ref, unpublished | Coordinator가 result를 만들어야 진행 가능 |
| Maven discovery | Build + module/test owners | Test source와 manifest만; acceptance 주장 금지 | Effective POM, `-am clean`, strict selected run, fresh XML 불충족 |
| Evidence seal | Independent model/test owner + reviewer | Last green WP | Test discovery/count/digest 또는 independence 불명 |
| Phase 11 handoff | Phase 10 reviewer + AWS owner | Provider-neutral contract only | AWS field가 semantic type에 필요하다고 주장 |

질문이 막히면 넓은 기본값을 고르지 않는다. Blocker, owner, last safe point, restart evidence를 기록하고 멈춘다.

## 7. 학습 경로: 개념 → 작은 탐색 → 실제 변경 → 통합

### 7.1 1단계 — 개념을 말로 설명하기

종이에 worker 3개가 있는 round를 그린다.

```text
declared = [W0, W1, W2]

관찰 A: W0 success, W2 success
→ W1 missing
→ retry/wait 가능: ROUND_RUNNING + WaitForWorkers(W1)
→ 더 진행할 승인 경로 없음: INCOMPLETE
→ champion 없음

관찰 B: W0/W1/W2 모두 normal + candidate PASS
→ worker ordinal [0,1,2] 순서로 stable reduction
→ round champion
```

완료 신호:

- “Event 3개가 왔다”와 “declared set이 complete하다”의 차이를 설명할 수 있다.
- `WorkerRunId`와 `AttemptId`의 차이를 설명할 수 있다.
- Cancellation intent와 terminal fence의 차이를 설명할 수 있다.
- Candidate PASS와 publishable result의 차이를 설명할 수 있다.

자문 질문:

- W1의 첫 시도와 두 번째 시도 결과가 같은 digest이면 무엇이 달라지고 무엇이 같아야 하는가?
- W2 event가 먼저 왔다는 사실이 comparator input 순서에 영향을 주는가?
- Event를 잃어도 어떻게 진전할 수 있는가?

### 7.2 2단계 — 작은 손 탐색

다음 네 사례를 production code 없이 손으로 판정한다.

1. `W0=A`, `W1=B`, `W2=C`, 모두 PASS이고 `B`가 strictly best.
2. 다음 round champion `D`가 이전 `B`와 `EQUAL`.
3. Cancel intent가 저장됐지만 아직 `CANCEL_REQUESTED` CAS를 하지 않았다.
4. `PUBLISHING` CAS 뒤 result pointer 성공 응답이 유실됐다.

정답의 핵심:

```text
1 → B가 round champion
2 → NO_STRICT_IMPROVEMENT + final champion은 이전 B
3 → request일 뿐; exact state에서 cancel/publish fence 경쟁 필요
4 → exact published pointer를 읽어 same desired ref면 SUCCEEDED로 수렴
```

완료 신호:

- `EQUAL` candidate로 lineage를 바꾸지 않는다.
- Response loss를 unconditional 재publication으로 해결하지 않는다.
- 두 key의 write time으로 cancel/publish winner를 결정하지 않는다.

### 7.3 3단계 — 실제 변경을 vertical slice로 만들기

Entry gate가 열린 뒤에도 한 번에 cloud/E2E를 만들지 않는다.

```text
identity constructor
→ pure legal transition
→ one CAS + pending action
→ crash/reload
→ declared fan-in
→ stable champion
→ cancel/deadline
→ finalization/publication
→ architecture/contract/local integration
```

각 slice의 완료 신호는 production class 수가 아니라 해당 red test와 independent oracle이 green인지다.

### 7.4 4단계 — 통합과 handoff

마지막에만 accepted Phase 06/07/08/09 구현과 묶는다.

```text
same manifest
→ fake/local worker schedule permutation
→ duplicate/lost/out-of-order hints
→ crash at every durable boundary
→ same semantic terminal lineage/result
→ provider-specific reference 0
```

완료 신호:

- Required tests discovered/passed count가 expected manifest와 일치한다.
- Failed/error/skipped가 0이다.
- Model source/digest와 generated seed/minimized counterexample가 evidence에 있다.
- `E-P10-*`와 independent review가 있고 scheduler acceptance receipt 전에는 `ACCEPTED`라 하지 않는다.

## 8. PROPOSED Java 설계 안내

### 8.1 Dependency와 package ownership

```text
apps/provider adapter
  → rpdptw-application
    → rpdptw-core
    → rpdptw-solver의 exported worker/result projection
    → rpdptw-verification의 exported facade

rpdptw-application MUST NOT
  → AWS/GCP/Azure/Kubernetes SDK or event DTO
  → object backend implementation
  → solver COW/cache/operator internal
  → verifier scratch/finalizer internal
  → customer-name branch
  → OR-Tools/backend native API
```

Coordinator package는 application service의 한 책임이다. `manager`, `helper`, `util`로 ownership을 흐리지 않는다.

### 8.2 Inbound use case 후보

완성 코드를 복사하기보다 책임을 드러내는 최소 skeleton만 먼저 만든다.

```java
// PROPOSED INTERNAL — accepted Phase 08 contract에 맞춰 이름을 조정한다.
public interface AdvanceSolve {
    AdvanceSolveResult advance(AdvanceSolveCommand command);
}

public record AdvanceSolveCommand(
    TenantId tenantId,
    SolveId solveId,
    AdvanceCause cause,
    Optional<CoordinatorWakeUp> wakeUp
) {}
```

`AdvanceSolveCommand.tenantId`는 untrusted request claim/selector이며 권한 증명이 아니다. Accepted Phase 08/security boundary가 인증 주체에서 도출한 non-ambient `AuthorizedSolveScope` 또는 동등한 capability를 별도로 주입해야 한다. Exact public signature와 scope type은 **OPEN/CROSS-PHASE BLOCKED**이므로 위 skeleton에 임의 parameter를 완성해 넣지 않는다.

`CoordinatorWakeUp`은 safe correlation과 logical identity만 담는다. Candidate bytes, objective, replacement state나 authorization을 담지 않는다.

```java
public sealed interface AdvanceSolveResult
        permits ActionRequired, Waiting, Terminal, ReloadRequired, Rejected {}
```

`ReloadRequired`는 CAS conflict를 숨기지 않는다. Adapter가 무한 loop를 만드는 것도 금지한다. Retry scheduling은 explicit policy와 observation을 가져야 한다.

### 8.3 Identity record 후보

```java
public record ExecutionRunId(
    SolveId solveId,
    ManifestFingerprint manifestFingerprint
) {}

public record RoundId(
    ExecutionRunId runId,
    int roundOrdinal
) {
    // invariant: roundOrdinal >= 0
}

public record WorkerRunId(
    RoundId roundId,
    int workerOrdinal,
    CandidateFingerprint warmStart,
    WorkerConfigFingerprint config,
    RequestedWorkFingerprint requestedWork
) {}

public record AttemptId(
    WorkerRunId workerRunId,
    long attemptOrdinal
) {
    // invariant: attemptOrdinal >= 1
}
```

`WorkerRunId`에 provider execution ID나 `AttemptId`를 넣지 않는다.

```java
public record ActionId(
    ExecutionRunId runId,
    long actionOrdinal,
    CoordinatorActionKind kind,
    ActionPayloadFingerprint payloadFingerprint
) {}
```

`ActionId`와 action payload에는 opaque `StateVersion`을 넣지 않는다.

### 8.4 State hierarchy 후보

```java
public sealed interface SolveState
        permits Submitted,
                Preparing,
                Prepared,
                RoundDispatching,
                RoundRunning,
                RoundVerifying,
                RoundAggregating,
                Finalizing,
                Publishing,
                CancelRequested,
                SolveTerminalState {}

public record RunState(
    ExecutionRunId runId,
    long transitionOrdinal,
    SolveState state,
    Optional<CoordinatorAction> pendingAction,
    StateLineageFingerprint lineage
) {}
```

Opaque repository `StateVersion`은 `VersionedRunState` wrapper에 둔다. `transitionOrdinal`은 semantic lineage이고 CAS authority가 아니다.

```java
public record RoundRunning(
    RoundPlanRef plan,
    Set<WorkerRunId> declaredWorkers,
    Map<WorkerRunId, CommittedWorkerOutcomeRef> committedOutcomes
) implements SolveState {}

public record RoundAggregating(
    RoundPlanRef plan,
    DeclaredCompletenessReport completeness,
    List<CommittedWorkerOutcomeRef> outcomesInWorkerOrdinalOrder
) implements SolveState {}
```

Collection은 defensive immutable copy, stable equality와 duplicate rejection을 가져야 한다. `HashMap` iteration order를 comparator input으로 쓰지 않는다.

### 8.5 Action hierarchy 후보

```java
public sealed interface CoordinatorAction
        permits DispatchPhaseOneScreens,
                WaitForPhaseOneScreens,
                DispatchWorkers,
                WaitForWorkers,
                RequestWorkerStops,
                FinalizeResult,
                PublishResult,
                CompleteSolve,
                FailSolve {

    ActionId actionId();
    long authorizingTransitionOrdinal();
}
```

Action executor는 ordinal만 보고 실행하지 않는다.

```text
exact-read current VersionedRunState
require current.pendingAction.ActionId == requested.ActionId
require payload fingerprint exact
require authorizingTransitionOrdinal exact
then execute idempotently
```

Publication action은 별도 precondition type을 가진다.

```java
public record PublishResult(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    PublicationPrecondition expectedPublication,
    PublishableResultRef result
) implements CoordinatorAction {}
```

`PublicationPrecondition`의 exact shape는 **OPEN/CROSS-PHASE BLOCKED**다. Run-state `StateVersion`과 호환되거나 변환 가능하다고 가정하지 않는다.

### 8.6 Outbound port 후보

```java
public interface RunStateRepository {
    VersionedRunState get(SolveId solveId);

    StateUpdateResult compareAndSet(
        SolveId solveId,
        StateVersion expectedVersion,
        RunState nextState
    );
}

public interface WorkerDispatcher {
    DispatchReceipt dispatch(WorkerAssignment assignment);
    WorkerExecutionStatus getStatus(WorkerRunId workerRunId);
    StopReceipt requestStop(
        WorkerRunId workerRunId,
        CancellationId cancellationId
    );
}
```

```java
public interface ResultPublisher {
    VersionedPublishedResult get(SolveId solveId);

    PublicationResult compareAndSet(
        SolveId solveId,
        PublicationPrecondition expectedPublication,
        PublishableResultRef desired
    );
}
```

위 raw-`SolveId` signature는 기존 제안의 책임을 보여 줄 뿐, authorization/failure closure를 만족하는 최종 계약이 아니다. 모든 storage/dispatch operation은 승인된 `AuthorizedSolveScope`에 결박되고 다음 결과를 operation별로 손실 없이 운반해야 한다.

```text
PROPOSED SEMANTIC SHAPE — exact Java API는 OPEN
AuthorizedSolveScope + operation input
  → Success(exact value/receipt)
  | AccessDenied(non-revealing)
  | NotFound
  | PreconditionFailed(exact typed token domain)
  | CorruptArtifact
  | VisibilityIndeterminate
  | PartialWriteAborted
  | approved operation-specific failure
```

`Optional.empty`, boolean, null, catch-all `RuntimeException` 또는 무조건 retryable mapping으로 category를 잃지 않는다. Phase 08/09/security/10 owner가 exact scope와 failure carrier를 승인하기 전 구현·publication evidence로 사용하지 않는다.

### 8.7 Pure reducer와 orchestration 분리

Reducer:

```text
input:
  current semantic state
  exact verified manifest/artifacts
  cancellation observation
  deadline observation
  correlation-only wakeup

output:
  next semantic state
  immutable artifacts to write
  optional provider-neutral action
```

Reducer는 repository, dispatcher, clock, telemetry와 provider SDK를 호출하지 않는다.

Orchestrator:

```text
read exact state/version
→ read/verify manifest and referenced artifacts
→ call pure reducer
→ put immutable decision artifacts
→ verified read-back
→ CAS next state + pending action
→ success: return action
→ conflict: ReloadRequired; no side effect
```

### 8.8 One-transition pseudocode

```text
advance(command):
  scope = authorizationPort.bindAuthenticatedPrincipal(command.tenantId, command.solveId)
  if missing/denied/mismatch:
      return non-revealing rejection
      // state/storage lookup, existence observation, decode, CAS, dispatch count == 0

  versionedResult = stateRepository.get(scope)
  if storage failure:
      classify exact operation failure
      preserve last safe state/pointer; publish no action
      return approved lossless failure/result
  versioned = versionedResult.value

  if terminal(versioned.state):
      classify late/duplicate safely
      return same terminal

  manifest = readDeclaredAndVerified(scope, versioned.state.manifestRef)
  require intrinsic(manifest) == versioned.state.runId.manifestFingerprint
  require scope/tenant/solve/snapshot/build/config lineage equality

  wakeup = validateCorrelationOnly(command.wakeUp)
  cancel = cancellationPort.find(scope)
  deadline = observeApprovedDeadlineContract()

  decision = reducer.decide(
      versioned.state, manifest, exactArtifacts, cancel, deadline, wakeup)

  for each immutable artifact:
      putIfAbsent(scope, same key, same expected digest)
      readVerified(scope, exact ref)
      on failure: preserve exact category and last safe state; no CAS/action

  next = decision.nextState + durable pending action
  cas = compareAndSet(scope, versioned.version, next)

  if typed precondition conflict:
      return ReloadRequired
  if denied/corrupt/indeterminate/partial:
      preserve exact failure; no side effect
  return decision action/wait/terminal
```

한 invocation에서 conflict 뒤 내부 loop로 두 번째 business CAS를 시도하지 않는다.

### 8.9 Declared completeness와 champion pseudocode

```text
expected =
  roundPlan.assignments
    stable sort by workerOrdinal
    map exact WorkerRunId

for each expected worker:
  outcome = exact-read committed outcome

if missing and approved wait/retry remains:
  WaitForWorkers(missing)

if missing and no approved progress remains:
  INCOMPLETE(MISSING_DECLARED_WORKER)

require observed logical identity set == expected set

for outcome in expected order:
  require exact assignment fingerprint
  require normal MAX_STEPS_REACHED
  require completedSteps == requestedSteps
  require candidate verifier report PASS
  require exact VerifiedSolution ref

roundChampion =
  stable upstream comparator reduction
  + manifest-declared deterministic tie policy
```

Round transition:

```text
STRICTLY_BETTER + next declared round
  → round champion becomes exact common warm start

EQUAL or WORSE
  → NO_STRICT_IMPROVEMENT
  → finalization receives previous champion

STRICTLY_BETTER + max declared rounds consumed
  → MAX_ROUNDS_REACHED
  → finalization receives round champion
```

Tie policy는 같은 round의 후보를 안정적으로 하나 고르는 규칙이지 `EQUAL` candidate를 adoption하는 quality dimension이 아니다.

### 8.10 Cancellation, publication과 deadline state transition

Cancellation:

```text
record idempotent cancellation intent
→ coordinator exact-reads intent
→ same run-state version에서:
   CANCEL_REQUESTED CAS vs PUBLISHING CAS
→ exactly one successful run-state transition owns terminal intent
```

```text
CANCEL_REQUESTED wins:
  stop action
  no new dispatch/finalize/publish
  actual worker termination observations
  then CANCELLED

PUBLISHING wins:
  later cancel = TOO_LATE/no-op
  result pointer CAS/reconcile continues
```

Deadline:

```text
before approved boundary → running/waiting unchanged
at reached boundary → WATCHDOG_REACHED + stop fence
provider prevents durable record/stop → PLATFORM_TIMEOUT
late worker success → observation only; no normal publication revival
```

Process-local monotonic tick을 restart 뒤 직접 비교하지 않는다. Durable encoding/origin reconciliation/reserve/failure contract가 승인되기 전 production deadline path를 구현하지 않는다.

### 8.11 Authorization/failure state machine, oracle와 last safe point

Authorization은 controller decoration이 아니라 coordinator transition의 첫 gate다.

```text
UNBOUND REQUEST
  ├─ missing/mismatched/ambient-only authority
  │    → REJECTED_NON_REVEALING
  │    → backend observation 0, state transition 0, action 0
  └─ explicit authorized tenant+solve+operation scope
       → exact state read
       → exact artifact read/decode
       → pure decision
       → immutable write/read-back
       → one state CAS
       → only then action execution eligibility
```

Storage failure oracle는 operation과 last safe point를 함께 판정한다.

| Failure observation | 반드시 보존할 구분 | State/pointer와 side effect |
|---|---|---|
| `ACCESS_DENIED` | `NOT_FOUND`와 비구별 외부 응답이 필요해도 내부 typed audit는 보존 | Backend 추가 probe 금지; unchanged; action 0 |
| `NOT_FOUND` | Denied/corrupt와 구분; exact declared ref라면 completeness/failure rule에 전달 | 승인된 transition 전까지 unchanged |
| `CORRUPT_ARTIFACT` | Digest/schema/decode 단계와 exact ref | Deserialize/dispatch 금지; unchanged |
| `PRECONDITION_FAILED` | Run-state/worker/publication token domain | Reload/reconcile; loser side effect 0 |
| `VISIBILITY_INDETERMINATE` | Not-found/success로 추정 금지 | Retry/reconcile decision 전 unchanged |
| `PARTIAL_WRITE_ABORTED` | Operation/ref/verified-read 여부 | Pointer/state advance 금지; orphan cleanup은 별도 operation |

Independent oracle은 `(authorized scope, prior exact state/version, operation result)`에서 expected next state, returned failure, backend call trace와 action count를 계산한다. 실패 뒤 “가장 최근에 성공한 immutable put”이 아니라 **마지막 authoritative state/pointer CAS**가 resume 기준이다. Exact failure에 대응하는 승인된 durable failure transition이 있으면 다음 invocation이 그 transition을 한 번 CAS할 수 있지만, 현재 invocation이 failure를 generic `FAILED`로 바꾸어 숨겨서는 안 된다.

## 9. 순서 있는 work package

### WP-10.0 — Entry receipt, authority와 cross-phase contract freeze

**목적과 이유**

Upstream identity와 ownership이 없는 상태에서 coordinator type을 만들면 나중에 Phase 08/09 port를 복제하거나 잘못된 token을 semantic identity에 넣게 된다. 구현보다 먼저 exact handoff와 blocker를 봉인한다.

**사전조건**

- Scheduler task/implementer/oracle/reviewer 지정
- Phase 00/06/07/08/09 accepted receipt와 evidence ref
- Phase 08~10 application/storage ownership review
- Security owner가 승인한 explicit non-ambient authorized solve/storage scope
- Phase 09/10 owner가 승인한 operation별 lossless failure carrier와 last-safe-point disposition
- Build/application/test owner가 승인한 POM dependency/profile/Surefire/Failsafe/discovery map
- Explicit `TEST_ONLY` manifest authority

**예상 변경 위치/type**

- 구현 변경 없음 또는 reviewed contract map
- Test manifest와 authority projection
- Architecture red-test skeleton
- Root/application/build/port-contract POM change plan과 effective-POM oracle

**구체 행동**

1. Accepted predecessor receipt의 source/build/artifact/evidence identity를 대조한다.
2. Phase 06 worker projection, Phase 07 facade, Phase 08 ports, Phase 09 exact committed-outcome/publication operation을 표로 mapping한다.
3. Run-state/action/worker/publication token을 서로 다른 type으로 고정한다.
4. 모든 inbound/storage/dispatch operation을 authorized scope와 denied/not-found/corrupt/precondition/indeterminate/partial failure에 mapping한다.
5. Application-local fixture, direct test dependency, port/architecture module edge, Surefire/Failsafe profile와 reactor order를 mapping한다.
6. `Q-BENCH-02`, C-17, `Q-VAR-01`, Phase 14 authority snapshot을 기록한다.
7. Public/wire API와 provider adapter가 non-scope임을 review한다.

**근거**

Canonical Phase 10 §4, review F-P10-001/002/005/006, Phase 09 §3.1/§15.2.

**금지 shortcut**

- Missing predecessor를 fake `PASS`나 stub port로 대신
- 인접 문서 hash 일치만 acceptance로 사용
- Legacy GCP controller를 coordinator baseline으로 재사용
- Official numeric fallback 생성
- Caller `TenantId`, thread-local 또는 object prefix를 authorization proof로 사용
- Stub port/fake failure로 compile만 통과하거나 `failIfNoTests=false`로 discovery를 숨김

**현재 실행 가능한 검증**

```bash
test -s docs/implementation/phases/phase-06-cow-alns-reproducibility.md
test -s docs/implementation/phases/phase-07-independent-verification-final-result.md
test -s docs/implementation/phases/phase-08-application-ports-local-runtime.md
test -s docs/implementation/phases/phase-09-object-storage-no-database.md
test -s docs/implementation/phases/phase-10-provider-neutral-coordinator.md

rg -n 'Phase 10|BLOCKED_NOT_IMPLEMENTED|NOT_PRODUCED' \
  docs/implementation/execution-progress-and-results.md

find rpdptw/application build -name pom.xml -o -name '*Test.java' -o -name '*IT.java'
test ! -e build/port-contract-tests/pom.xml
```

**기대 결과와 실패 해석**

- 파일 존재는 확인되지만 accepted receipt/evidence가 없으므로 entry는 계속 blocked다.
- `TBD` owner/task가 남으면 implementation status를 올리지 않는다.
- Cross-phase signature가 다르거나 auth/failure/POM closure가 없으면 mapping review를 다시 하고 중복 interface나 green stub을 만들지 않는다.

**Rollback/마지막 안전 지점**

Reviewed 문서, source map과 future-red test matrix. Production source 변경 0.

**Handoff**

Approved identity/state/action/authorization/failure/token 및 Maven ownership map을 WP-10.1에 넘긴다.

### WP-10.1 — Pure identity, state model과 legal transition

**목적과 이유**

Provider나 storage 없이도 legal state machine을 독립적으로 설명하고 검증할 수 있어야 이후 CAS/fault test의 의미가 안정된다.

**사전조건**

WP-10.0 green, accepted Phase 08 state lifecycle와 explicit manifest fields.

**예상 file/package/type**

- `rpdptw/application/pom.xml`의 직접 test dependency
- `application.execution.ExecutionIdentity`
- `application.coordinator.SolveState`
- `CoordinatorAction`
- `CoordinatorDecision`
- `CoordinatorReducer`
- `IndependentCoordinatorModel`

**구체 행동**

1. Record constructor에서 null, negative ordinal, cross-run identity와 duplicate declared ID를 거부한다.
2. Legal/illegal transition table과 terminal monotonicity를 code와 독립 model에 별도로 표현한다.
3. Pure reducer가 I/O/clock/provider call을 하지 않게 한다.
4. Pending action을 semantic `ActionId`와 payload fingerprint로 만든다.
5. Independent model은 production state/reducer/encoder helper를 import하지 않는다.
6. Scenario/model fixture는 application test source에서 compile되게 하고, property library는 실제 사용·evidence가 승인된 경우에만 직접 선언한다.

**근거**

Integrated §14.1~14.2, canonical Phase 10 §6~8, review F-P10-001/003.

**금지 shortcut**

- `Map<String,Object>` state
- Enum 하나에 arbitrary payload
- Provider workflow state를 canonical state로 사용
- Model oracle가 production reducer를 호출
- `StateVersion`을 action identity에 포함

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
  -Dtest=SolveCoordinatorStateMachineTest,CoordinatorModelBasedPropertyTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

현재 target test가 0개이므로 이 명령은 **entry가 열린 뒤 생성된 test를 검출하는 future command**다. 지금 green evidence로 실행하지 않는다.

**기대 결과와 실패 해석**

- Expected class/method가 모두 discovered되고 failed/error/skipped 0.
- Illegal transition은 publication/dispatch 0.
- Model과 production semantic state/action diff 0.
- 0 tests는 성공이 아니라 source/test selection 오류다.
- Reducer가 port를 필요로 하면 책임 분리가 실패한 것이다.

**Rollback/마지막 안전 지점**

Production reducer/type을 폐기하고 WP-10.0 contract map으로 돌아간다.

**Handoff**

Pure `CoordinatorDecision`, legal table와 independent model을 WP-10.2에 넘긴다.

### WP-10.2 — Immutable artifact, one CAS와 crash/resume

**목적과 이유**

Artifact write, state commit과 외부 side effect 사이의 모든 crash window에서 같은 logical outcome으로 수렴하게 한다.

**사전조건**

WP-10.1 green, accepted explicit authorized solve/storage scope, Phase 09 operation별 lossless put/read/CAS failure와 exact current pending-action authorization contract.

**예상 file/package/type**

- `SolveCoordinator`
- `RunStateRepository` integration
- `InMemoryCasStateRepository`
- `CrashPointHarness`
- `CoordinatorIdempotencyConcurrencyTest`
- `CoordinatorCrashResumeTest`

**구체 행동**

1. Immutable artifact put-if-absent → readVerified → state CAS 순서를 구현한다.
2. Explicit scope를 backend lookup/read/decode/dispatch 전에 bind하고 operation마다 전달한다.
3. Invocation당 authoritative CAS를 최대 한 번만 호출한다.
4. Typed CAS conflict는 `ReloadRequired`이고 side effect는 0이다.
5. Denied/not-found/corrupt/indeterminate/partial failure를 lossless하게 반환하고 last authoritative state/pointer를 유지한다.
6. CAS 성공 state에 pending action을 함께 저장한다.
7. Executor는 exact current pending action과 authorized operation scope를 다시 확인한다.
8. Artifact put 전/후, CAS 전/후, action 전/후, receipt 전/후 crash를 주입한다.
9. Same key/same digest는 수렴하고 different digest는 integrity failure로 만든다.

**근거**

Phase 09 §7.5/§9, canonical Phase 10 §6.3/§8.2, review F-P10-001.

**금지 shortcut**

- Singleton/leader lease/distributed lock으로 race 숨김
- CAS conflict 자동 overwrite
- CAS 뒤 action을 메모리에만 보존
- Pre-CAS token으로 post-CAS action 승인
- Multi-object transaction 가정
- Caller tenant claim/ambient context를 authorized scope로 간주
- Failure를 generic retry/not-found/empty로 축약

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
  -Dtest=CoordinatorIdempotencyConcurrencyTest,CoordinatorCrashResumeTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Concurrent coordinator 수와 무관하게 semantic transition commit 1회.
- Losing coordinator side effect 0.
- Crash 뒤 동일 `ActionId` 재방출.
- Same identity/different digest는 fail-closed.
- Missing/mismatched authority는 backend call 0이고, storage failure 뒤 state/pointer/action은 last safe point에 남는다.
- Lock/lease가 없으면 test가 실패한다면 coordinator correctness 설계가 잘못된 것이다.

**Rollback/마지막 안전 지점**

I/O composition을 제거하고 WP-10.1 pure reducer/model을 보존한다.

**Handoff**

CAS-safe state/action executor를 WP-10.3에 전달한다.

### WP-10.3 — Phase-1/round dispatch, retry, event와 declared completeness

**목적과 이유**

도착한 결과가 아니라 **선언한 work 전체**를 기준으로 fan-in해야 partial success와 visibility 결함을 막을 수 있다.

**사전조건**

WP-10.2 green, accepted Phase 05/06 portfolio/worker semantics, Phase 07 candidate PASS, Phase 08 dispatcher, Phase 09 committed-outcome exact read.

**예상 file/package/type**

- `PhaseOnePlanRef`, `RoundPlanRef`
- `WorkerAssignment`, `CommittedWorkerOutcomeRef`
- `DeclaredCompletenessReport`
- `PhaseOneFanInTest`
- `DeclaredRoundCompletenessTest`
- `CoordinatorEventOrderingTest`

**구체 행동**

1. Phase-1 expected set은 declared `AVAILABLE` portfolio slot만 stable 4×2 order로 만든다.
2. Typed `UNAVAILABLE` slot을 missing screen으로 취급하지 않는다.
3. Assignment artifact를 모든 worker에 대해 만들고 verified-read한 뒤 dispatch state를 commit한다.
4. Retry에서 `AttemptId`만 바뀌는지 exact field equality로 검사한다.
5. Event는 correlation만 확인하고 exact committed outcome을 재독해한다.
6. Missing/failed/unverified/wrong-assignment/conflicting outcome을 champion에서 차단한다.
7. Extra unknown outcome은 expected set을 확장하지 않는다.
8. Wait/retry가 남은 상태와 `INCOMPLETE`를 명시적으로 구분한다.

**근거**

Master §11.3/§14.4, Integrated §13.7/§14.3~14.5, canonical Phase 10 §8.3~8.5.

**금지 shortcut**

- Prefix listing으로 worker set 생성
- Success count만 비교
- Event body objective/result 사용
- Failed worker를 성공 worker가 “상쇄”
- Retry 때 다른 seed/config/work

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
  -Dtest=PhaseOneFanInTest,DeclaredRoundCompletenessTest,CoordinatorEventOrderingTest,CoordinatorIdempotencyConcurrencyTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Available screen/declared worker 하나라도 incomplete면 champion 0.
- Completion permutation 모두 같은 ordered candidate list와 champion input.
- Lost wakeup에도 exact read로 진전.
- Same logical worker/different verified digest는 `FAILED(INTEGRITY_VIOLATION)`.
- Listing 호출이 보이면 Phase 09 contract를 우회한 것이다.

**Rollback/마지막 안전 지점**

Dispatcher/fan-in composition을 제거하고 WP-10.2 durable action/CAS를 보존한다.

**Handoff**

Complete ordered verified outcome set만 WP-10.4에 넘긴다.

### WP-10.4 — Stable champion, strict lineage와 multi-round termination

**목적과 이유**

같은 complete candidate set이면 completion order와 provider metadata가 달라도 같은 champion과 terminal lineage가 나와야 한다.

**사전조건**

WP-10.3 complete set green, accepted upstream comparator와 manifest tie policy identity.

**예상 file/package/type**

- `StableRoundChampionSelector`
- `RoundChampionRef`
- Next `RoundPlanRef`
- `MultiRoundChampionTest`
- `CoordinatorReplayTest`

**구체 행동**

1. Worker ordinal stable order로 upstream comparator reduction을 수행한다.
2. Tie policy와 quality comparator를 분리한다.
3. `STRICTLY_BETTER`일 때만 next-round common warm start를 바꾼다.
4. `EQUAL`/`WORSE`이면 이전 champion을 exact 보존한다.
5. Last declared round의 strict improvement만 `MAX_ROUNDS_REACHED`와 새 champion을 결합한다.
6. Lineage fingerprint에서 provider metadata, elapsed와 completion order를 제외한다.

**근거**

Canonical Master §14.4, Domain §16, canonical Phase 10 §8.4, review F-P10-003.

**금지 shortcut**

- Raw `double objective`
- First-completed winner
- Unordered map iteration
- Clock/thread ID tie-break
- `EQUAL` candidate adoption
- Missing tie policy hidden default

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
  -Dtest=MultiRoundChampionTest,CoordinatorReplayTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Equal/worse에서 previous champion ref exact equality.
- Strict improvement에서 모든 next assignment가 같은 warm start.
- Schedule/provider metadata permutation 뒤 lineage/result exact equality.
- Incomplete batch에서 normal termination 생성 0.

**Rollback/마지막 안전 지점**

Selector/round transition을 제거하고 WP-10.3 complete ordered set을 unpublished 상태로 보존한다.

**Handoff**

Final verified champion과 exact normal termination을 WP-10.5/10.6에 넘긴다.

### WP-10.5 — Cancellation, deadline와 failure taxonomy

**목적과 이유**

운영 중단을 정상 품질 종료로 오인하지 않고, cancel/publication race가 하나의 terminal meaning으로 수렴하게 한다.

**사전조건**

WP-10.2~4 green, accepted cancellation port, same-run-state fence, authorized operation scope, lossless storage failure contract와 durable deadline restart ADR. Deadline ADR가 없으면 deadline-enabled production path는 계속 blocked다.

**예상 file/package/type**

- `CancelRequested`, `SolveCancelled`
- `SolveWatchdogReached`, `SolvePlatformTimeout`
- `RequestWorkerStops`
- `CoordinatorFailure`
- `MutableTestClock`
- `CoordinatorCancellationDeadlineTest`
- `CoordinatorIntegrityTest`
- `CoordinatorAuthorizationFailureContractTest`

**구체 행동**

1. Cancellation intent record, `CANCEL_REQUESTED` state CAS, stop action, actual termination을 별도 artifact/state로 만든다.
2. `CANCEL_REQUESTED`와 `PUBLISHING`을 같은 run-state expected version에서 race시킨다.
3. Duplicate cancel과 late cancel을 idempotent하게 분류한다.
4. Deadline 직전/경계/직후를 virtual clock으로 검사한다.
5. `WATCHDOG_REACHED`, `RESOURCE_LIMIT_REACHED`, `PLATFORM_TIMEOUT`을 normal termination과 분리한다.
6. Terminal state 뒤 late worker success를 observation-only로 처리한다.
7. Public failure에는 raw provider exception, secret, PII와 artifact locator를 넣지 않는다.
8. Caller tenant claim만 있는 요청, missing/mismatched/ambient authority를 backend observation 전에 거부한다.
9. Denied/not-found/corrupt/indeterminate/partial failure의 state disposition과 last safe point를 independent oracle로 대조한다.

**근거**

Master §13.1, Architecture §3.6/§5.3, canonical Phase 10 §8.6~8.7, review F-P10-002/005.

**금지 shortcut**

- Intent object 자체를 terminal fence로 사용
- Cancel boolean을 success로 반환
- Deadline을 `MAX_STEPS_REACHED`나 plateau로 변환
- Real sleep/flaky timing test
- Restart 뒤 wall clock/provider remaining time fallback
- Late success로 terminal state 되돌리기
- Denied를 not-found로 내부 축약하거나 existence probe로 구별
- Partial/indeterminate write 뒤 pointer/state를 성공으로 전진

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
  -Dtest=CoordinatorCancellationDeadlineTest,CoordinatorIntegrityTest,CoordinatorAuthorizationFailureContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Cancel/deadline fence 뒤 new dispatch/finalize/publish 0.
- Intent-only 상태는 아직 terminal이 아님.
- `PUBLISHING` winner 뒤 late cancel은 too-late.
- Sleep/flaky test 0.
- Unauthorized request의 backend call/action/state transition 0.
- Operation별 failure category가 oracle과 exact 일치하고 실패 직후 state/pointer는 last safe point다.
- Durable deadline contract가 없으면 관련 production test를 억지로 green하지 않고 gate failure로 남긴다.

**Rollback/마지막 안전 지점**

Cancel/deadline integration을 제거하고 WP-10.4 committed lineage를 보존한다. Explicit `TEST_ONLY` virtual model까지만 유지한다.

**Handoff**

Typed terminal/failure와 stop evidence를 WP-10.6에 넘긴다.

### WP-10.6 — Phase 07 finalization과 Phase 09 publication orchestration

**목적과 이유**

Coordinator가 result를 만들지 않고, complete verified champion만 Phase 07에 넘기며, both-gate output만 distinct publication CAS로 공개하게 한다.

**사전조건**

WP-10.4 final champion 또는 WP-10.5 typed exceptional terminal, accepted Phase 07 facade, accepted Phase 09 distinct publication precondition/read-reconcile contract.

**예상 file/package/type**

- `FinalizationRequestRef`
- `FinalResultGateway`
- `Phase07Output`
- `PublishResult`
- `PublicationReceiptRef`
- `CoordinatorFinalizationPublicationTest`

**구체 행동**

1. Complete verified normal champion에서만 finalization request를 만든다.
2. Phase 07 `Publishable`과 `Rejected/GateIncomplete`를 exhaustively mapping한다.
3. Output identity/digest를 immutable artifact로 보존한다.
4. `PUBLISHING` state CAS로 publication terminal intent를 fence한다.
5. Distinct `PublicationPrecondition`으로 result pointer CAS를 실행한다.
6. Response loss 뒤 exact published pointer를 읽어 same desired ref면 수렴한다.
7. Different desired digest conflict는 `PUBLICATION_REJECTED`로 fail-closed한다.
8. Publication receipt 뒤에만 run state를 `SUCCEEDED`로 CAS한다.

**근거**

Master §14.1, Integrated §14/§21, Phase 09 §8.6, canonical Phase 10 §7.5/§8.1, review F-P10-001/002.

**금지 shortcut**

- Coordinator가 outcome/audit/summary/payload/PASS 생성
- Candidate PASS만으로 publish
- Run-state token을 publication token으로 사용
- Result object 존재를 publication으로 사용
- Same/different digest conflict를 모두 success 처리
- Exceptional recovery best 자동 publication

**Future 검증 명령**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
  -Dtest=CoordinatorFinalizationPublicationTest,CoordinatorIntegrityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

**기대 결과와 실패 해석**

- Phase 07 `Publishable` 외 publish action 0.
- Candidate PASS without result PASS publication 0.
- Same digest response-loss/conflict는 exact `SUCCEEDED` convergence.
- Different digest는 `PUBLICATION_REJECTED`.
- Coordinator 내부에 result builder가 생기면 authority 침범이다.

**Rollback/마지막 안전 지점**

Finalization/publication composition을 제거하고 verified champion ref를 **publication 불가** 상태로 보존한다.

**Handoff**

Provider-neutral action/port와 published lineage를 WP-10.7에 전달한다.

### WP-10.7 — Architecture, full evidence, independent review와 handoff

**목적과 이유**

개별 happy path가 아니라 provider-neutrality, confluence, security와 evidence DAG까지 검증해야 Phase 11이 안전하게 mapping할 수 있다.

**사전조건**

WP-10.1~6 required tests green, required skipped 0, cross-phase blocker 해소.

**예상 file/package/type**

- `Phase10CoordinatorArchitectureTest`
- `CoordinatorPortContractTest`
- `Phase10CoordinatorIT`
- Root/application/build/port-contract POM과 effective-POM evidence
- Pre-review evidence manifest
- Independent review report
- Post-review acceptance receipt
- `Phase10HandoffManifest`

**구체 행동**

1. Provider SDK/state/event DTO, storage implementation, solver/verifier internal forbidden edge를 검사한다.
2. Listing/lock/lease/LWW/hidden default semantic path를 negative test로 막는다.
3. Application direct test dependency, port/architecture module edge, explicit local-IT profile와 effective POM을 확인한다.
4. Required test class/method manifest와 fresh Surefire/Failsafe XML을 대조한다.
5. Model source/digest, generated seeds, minimized failures와 authorization/storage-fault matrix를 봉인한다.
6. Source/build/config/artifact/state/action/result digest와 OPEN/GATED/deferred snapshot을 기록한다.
7. Pre-review manifest → independent review → post-review receipt의 단방향 DAG를 유지한다.
8. Phase 11에는 accepted provider-neutral handoff만 넘긴다.

**근거**

Architecture §2/§5.6/§6.4, Integrated §22~26, Master Realization Plan §8~11, canonical Phase 10 §11~15.

**금지 shortcut**

- Root `verify` 한 번만으로 selected tests 발견 주장
- Stale `target/`/console summary를 evidence로 사용
- Review 결과를 pre-review manifest에 backfill
- AWS integration 성공을 Phase 10 semantic proof로 대체
- Missing semantic config에 production default
- `CoordinatorPortContractSuite`를 `src/main/java`에 두고 test라고 간주
- `Phase10CoordinatorIT`를 Failsafe execution/profile 없이 이름만 추가

**Future 검증 명령**

```bash
# 같은 exact source/dependency를 먼저 reactor에서 clean install한다.
./mvnw -B -ntp -Dstyle.color=never clean install

# 전체 Phase 10 compile/dependency/test closure. Filter 없는 -am clean이다.
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/application,build/port-contract-tests,build/architecture-rules \
  -am clean verify

# 아래 selected run은 위 install과 같은 source에서 -am 없이 실행한다.
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
  -Dtest=Phase10CoordinatorArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

# 현재 module은 부재한다. Accepted predecessor가 생성한 뒤에만 실행한다.
./mvnw -B -ntp -Dstyle.color=never -pl build/port-contract-tests \
  -Dtest=CoordinatorPortContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/application -Pphase10-local-it \
  -Dit.test=Phase10CoordinatorIT \
  -Dfailsafe.failIfNoSpecifiedTests=true clean verify

./mvnw -B -ntp -Dstyle.color=never -Pphase10-local-it clean verify
```

Selected command에는 `-am`을 붙이지 않고 해당 lifecycle의 strict selector를 쓴다. `-am`이 필요한 full dependency verification은 test filter 없이 `clean verify`로 별도 실행한다. `phase10-local-it` profile과 Failsafe execution이 effective POM에 없으면 IT command는 expected failure이며 exit gate를 열지 않는다.

**기대 결과와 실패 해석**

- Required discovered = expected, passed = discovered, failed/error/skipped = 0.
- Surefire와 Failsafe report `mtime`이 각 command 시작 뒤이고, exact suite/class/method가 XML에 있으며 missing/duplicate suite가 0.
- Provider/storage/solver/verifier internal forbidden edge 0.
- Same manifest의 schedule/crash/provider-metadata permutation terminal equality.
- Evidence/review/receipt 중 하나가 없으면 최대 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING`이다.

**Rollback/마지막 안전 지점**

Last accepted predecessor와 last green WP artifact를 보존한다. Evidence가 불완전하면 acceptance/handoff를 발행하지 않는다.

**Handoff**

§14의 provider-neutral `Phase10HandoffManifest`만 Phase 11/12/14에 전달한다.

## 10. 테스트 구현 안내

### 10.1 Fixture, builder와 oracle 분리

| Fixture/builder/oracle | 역할 | 독립성 규칙 |
|---|---|---|
| `CoordinatorScenarioBuilder` | Manifest/state/round/outcome/action을 작은 immutable fixture로 생성 | Expected state를 production reducer로 만들지 않음 |
| `TestOnlyExecutionManifestBuilder` | 모든 screen/worker/round/retry/deadline 값을 explicit하게 지정 | Omitted default와 official label 금지 |
| `DeclaredRoundBuilder` | Stable ordinal과 exact assignment set 생성 | Event/list order에서 set 추론 금지 |
| `VerifiedWorkerOutcomeBuilder` | Phase 06 termination + Phase 07 PASS ref 생성 | Raw objective/boolean verified 금지 |
| `TamperedArtifactBuilder` | 한 field/digest/schema/tenant만 변조 | 여러 결함을 한 fixture에 섞지 않음 |
| `InMemoryCasStateRepository` | Opaque version과 CAS success/conflict history | `synchronized` singleton을 correctness proof로 사용하지 않음 |
| `ScriptedWorkerDispatcher` | Dispatch/status/stop/duplicate/lost/out-of-order script | Thread timing/sleep 사용 금지 |
| `ScriptedFinalResultGateway` | Exact Publishable/Rejected/GateIncomplete 반환 | Verifier PASS를 자체 합성하지 않음 |
| `MutableTestClock` | Boundary 전/정확히/후 deterministic observation | Wall clock/real sleep 금지 |
| `CrashPointHarness` | Artifact/CAS/action/receipt boundary crash | Heap-only 상태로 durability 주장 금지 |
| `IndependentCoordinatorModel` | 단순 set/list와 transition relation으로 expected 계산 | Production state/reducer/codec/repository helper import 금지 |

Oracle input alphabet 예:

```text
SUBMIT
PREPARE_OK | PREPARE_REJECT | BIND_FAIL
DISPATCH_ACK(worker)
WORKER_SUCCESS(worker, attempt, digest)
WORKER_FAILURE(worker, attempt, kind)
RETRY(worker, newAttempt)
CANCEL_INTENT
STOP_ACK(worker)
DEADLINE_BEFORE | DEADLINE_REACHED
FINALIZE_PUBLISHABLE | FINALIZE_REJECTED | FINALIZE_INCOMPLETE
PUBLISH_OK | PUBLISH_CONFLICT_SAME | PUBLISH_CONFLICT_DIFFERENT
CRASH(point)
RESUME
STALE_EVENT(identity)
TAMPER(field)
CONCURRENT_ADVANCE(count)
AUTH_MISSING | AUTH_MISMATCH | AUTH_AMBIENT_ONLY | AUTHORIZED_SCOPE
STORE_DENIED | STORE_NOT_FOUND | STORE_CORRUPT
STORE_PRECONDITION_FAILED | STORE_VISIBILITY_INDETERMINATE
STORE_PARTIAL_WRITE_ABORTED
```

### 10.2 Exact test class/method 후보

이름은 proposed지만 defect detectability는 유지한다.

| Test class | Exact future method | Oracle/expected |
|---|---|---|
| `SolveCoordinatorStateMachineTest` | `submittedAdvancesOnlyToPreparing()` | Hand legal table |
|  | `illegalBackwardTransitionFailsClosed()` | `FAILED(ILLEGAL_TRANSITION)` |
|  | `terminalStateIgnoresLateWakeUp()` | Same terminal/no action |
|  | `oneAdvancePerformsAtMostOneAuthoritativeCas()` | CAS count `<= 1` |
| `PhaseOneFanInTest` | `requiresEveryDeclaredAvailableScreenBeforeChampion()` | Partial blocked |
|  | `typedUnavailableSlotIsNotMissingScreen()` | Exact available set |
|  | `failedScreenCannotBeHiddenBySuccessfulScreens()` | Champion 0 |
|  | `screenCompletionPermutationsSelectSameChampion()` | Same lineage |
| `DeclaredRoundCompletenessTest` | `missingDeclaredWorkerKeepsRoundOpenBeforeDeadline()` | `WaitForWorkers` |
|  | `missingDeclaredWorkerBecomesIncompleteWhenNoProgressRemains()` | `INCOMPLETE` |
|  | `failedWorkerCannotBeHiddenBySuccessfulWorkers()` | Champion 0 |
|  | `unverifiedWorkerCannotEnterChampionReduction()` | Comparator call 0 for invalid |
|  | `extraOutcomeNeverExpandsDeclaredSet()` | Expected set unchanged |
| `CoordinatorIdempotencyConcurrencyTest` | `duplicateDispatchReturnsSameActionIdentity()` | Same action/ref |
|  | `actionContainsNoOpaqueRepositoryVersion()` | Token leakage 0 |
|  | `executorRequiresExactCurrentPendingActionAndOrdinal()` | Stale side effect 0 |
|  | `sameWorkerSameDigestConverges()` | One committed outcome |
|  | `sameWorkerDifferentDigestFailsIntegrity()` | Typed failure |
|  | `twoCoordinatorsRaceAndExactlyOneCasWins()` | One transition |
|  | `losingCoordinatorReloadsWithoutSideEffect()` | `ReloadRequired` |
| `CoordinatorCrashResumeTest` | `crashBeforeCasLeavesNoAuthoritativeTransition()` | Old state |
|  | `crashAfterCasReemitsPendingAction()` | Same `ActionId` |
|  | `crashAfterDispatchBeforeAckReconciles()` | One logical work |
|  | `crashAfterArtifactBeforePointerUsesSameDigest()` | Idempotent artifact |
|  | `repeatedCrashesConvergeToSameTerminalFingerprint()` | Exact equality |
| `CoordinatorEventOrderingTest` | `outOfOrderFutureRoundEventIsNonAuthoritative()` | No state change |
|  | `latePreviousRoundSuccessCannotReplaceChampion()` | Champion unchanged |
|  | `lostWakeUpStillProgressesByExactRead()` | Same terminal |
|  | `allCompletionPermutationsSelectSameChampion()` | Same lineage |
|  | `staleAttemptSameDigestConvergesAfterRetry()` | No conflict |
| `MultiRoundChampionTest` | `strictlyBetterChampionBecomesCommonNextWarmStart()` | All refs exact |
|  | `equalChampionTerminatesAndRetainsPreviousChampion()` | Previous ref exact |
|  | `worseChampionTerminatesNoStrictImprovement()` | Previous ref exact |
|  | `lastStrictImprovementTerminatesMaxRoundsReached()` | New champion |
|  | `tiePolicyIsStableButNotQualityDimension()` | Stable same-round choice |
| `CoordinatorCancellationDeadlineTest` | `duplicateCancellationIntentConverges()` | One intent/action |
|  | `cancelIntentAndActualTerminationRemainDistinct()` | Intermediate visible |
|  | `intentRecordAloneIsRequestNotTerminalFence()` | No false authority |
|  | `cancelRequestedRunStateCasWinningFencesPublication()` | Publish 0 |
|  | `publicationFenceWinningCasMakesLaterCancelTooLate()` | Publication continues |
|  | `deadlineBeforeBoundaryDoesNotStopRound()` | Running |
|  | `deadlineAtBoundaryIsWatchdogNotQualityTermination()` | Watchdog |
|  | `platformTimeoutIsNeverMaxStepsReached()` | Platform failure |
|  | `lateSuccessAfterDeadlineCannotPublish()` | Terminal unchanged |
| `CoordinatorIntegrityTest` | `tamperedManifestIntrinsicFingerprintFailsClosed()` | Manifest mismatch |
|  | `sameManifestIdentityDifferentBytesFailsClosed()` | Integrity failure |
|  | `crossTenantArtifactReferenceIsRejected()` | Read/dispatch 0 |
|  | `workerAssignmentFingerprintMismatchIsRejected()` | Fan-in 0 |
|  | `resultPayloadEventCannotBypassExactArtifactRead()` | Exact ref authority |
| `CoordinatorAuthorizationFailureContractTest` | `callerSuppliedTenantIdWithoutAuthorizedContextIsRejectedBeforeLookup()` | Backend call 0 |
|  | `missingAuthorizedContextFailsBeforeBackendLookup()` | Non-revealing reject |
|  | `mismatchedAuthorizedTenantAndSolveFailsBeforeBackendLookup()` | Lookup/decode/dispatch 0 |
|  | `ambientThreadLocalOrGlobalAuthorityIsRejected()` | Explicit scope required |
|  | `sameSolveIdAcrossTenantsNeverSharesExistenceOrMetadata()` | Cross-tenant oracle separation |
|  | `accessDeniedRemainsDistinctAndDoesNotRevealExistence()` | Typed internal failure/redacted external |
|  | `notFoundRemainsDistinctFromAccessDeniedAndCorruption()` | Lossless classification |
|  | `corruptArtifactFailsBeforeDeserializeAndDispatch()` | State/action unchanged |
|  | `visibilityIndeterminateNeverBecomesNotFoundOrSuccess()` | Last safe point |
|  | `partialWriteAbortedNeverAdvancesPointerOrState()` | Pointer/state unchanged |
|  | `staleVersionProducesReloadWithoutSideEffect()` | Typed precondition loser |
| `CoordinatorFinalizationPublicationTest` | `onlyCompleteVerifiedChampionReachesFinalization()` | Exact call count |
|  | `phase07RejectedNeverCreatesPublishAction()` | Publish 0 |
|  | `candidatePassWithoutResultPassCannotPublish()` | Publish 0 |
|  | `runStateVersionCannotServeAsPublicationPrecondition()` | Compile/contract reject |
|  | `sameResultDigestPublicationConflictConverges()` | `SUCCEEDED` |
|  | `differentResultDigestPublicationConflictRejects()` | `PUBLICATION_REJECTED` |
|  | `crashAfterResultPointerBeforeSucceededReconciles()` | Exact success |
| `CoordinatorReplayTest` | `sameManifestWithDifferentSchedulesHasSameLineage()` | Exact state/result |
|  | `attemptIdChangesDoNotChangeSeedWarmStartOrWork()` | Field equality |
|  | `providerMetadataNeverChangesSemanticFingerprint()` | Fingerprint equality |
| `CoordinatorModelBasedPropertyTest` | `generatedLegalSequencesMatchIndependentModel()` | State/action equality |
|  | `duplicatesOrderingCrashesPreserveConfluence()` | Same terminal |
|  | `generatedIllegalSequencesNeverPublish()` | Publication 0 |
| `Phase10CoordinatorArchitectureTest` | `applicationHasNoProviderSdkProviderStateOrEventDtoDependency()` | Forbidden refs 0 |
|  | `coordinatorHasNoSolverInternalOrVerifierInternalDependency()` | Forbidden edges 0 |
|  | `coordinatorHasNoListingLockLeaseOrLastWriteWinsPath()` | Forbidden semantics 0 |
|  | `missingSemanticConfigCannotSelectProductionDefault()` | Binding reject |
| `CoordinatorPortContractTest` | `authorizedFakeAndLocalImplementSameStateActionFailureSemantics()` | Same contract trace |
|  | `deniedCorruptAndIndeterminateRemainLosslessAcrossPortBoundary()` | Same typed failure |
|  | `sameActionReplayAndDistinctPublicationPreconditionConverge()` | Same terminal/ref |
| `Phase10CoordinatorIT` | `submitAdvanceWorkerFinalizePublishRetrievePreservesExactLineage()` | Full exact lineage |
|  | `deniedCrossTenantFlowPublishesNothingAndLeaksNoExistence()` | Publish/probe 0 |
|  | `storageIndeterminateAndPartialWriteResumeFromLastSafePoint()` | Resume exact prior CAS |
|  | `sameManifestRerunProducesSameSemanticTerminalFingerprint()` | Exact fingerprint |

### 10.3 Red → green 순서

1. Invalid identity와 illegal transition을 red로 고정한다.
2. Pure state table과 independent model을 green으로 만든다.
3. One CAS, pending action과 crash window를 red→green 한다.
4. Dispatch/retry/duplicate/order/declared completeness를 green 한다.
5. Stable champion, strict improvement와 multi-round termination을 green 한다.
6. Authorization precheck, lossless storage failure, cancellation/deadline fence를 green 한다.
7. Phase 07 finalization과 Phase 09 publication을 green 한다.
8. Generated model/replay, architecture, port contract와 local integration을 마지막에 green 한다.

Production code를 먼저 완성한 뒤 test 이름을 맞추지 않는다. Red가 예상 defect 때문에 실패하는지 먼저 확인한다.

### 10.4 False-green 방지

- Selected `-Dtest`에는 `-Dsurefire.failIfNoSpecifiedTests=true`.
- Selected `-Dit.test`에는 `-Dfailsafe.failIfNoSpecifiedTests=true`와 approved local-IT profile.
- Selected run에는 `-am`을 쓰지 않는다.
- 같은 exact source를 먼저 full-test `clean install`; filter 없는 module closure는 reactor `-am clean verify`.
- Selected run 직후 fresh Surefire/Failsafe XML을 evidence로 봉인.
- Expected class/method manifest와 discovered/passed count를 대조.
- Report `mtime > command start`, suite/class/method exact match, duplicate/missing XML 0을 검사한다.
- `-DskipTests`, `maven.test.skip`, required test disable 금지.
- Stale `target/`, console summary 한 줄, 다른 commit의 dependency artifact 금지.
- Repeated flaky run 중 성공한 것만 선택 금지.
- General root `failIfNoTests=false`를 Phase 10 discovery proof로 사용 금지.

### 10.5 Test 종류별 적용성

| Test 종류 | Phase 10 적용 | Pass 판정 | 미적용/경계 이유 |
|---|---|---|---|
| Value/unit | 필수 | Invalid identity/config/default fail-closed | 없음 |
| State/model property | 필수 | Independent model diff 0, shrink seed 보존 | 없음 |
| Port contract | 필수 | Fake/local 구현이 same idempotency/CAS/failure semantics | Provider SDK 세부는 Phase 11 |
| Integration | 필수 | Accepted Phase 06/07/08/09 seam으로 full coordinator flow | Fake PASS만으로 acceptance 불가 |
| Local E2E | 필수 | Submit/advance/worker/finalize/publish/retrieve lineage exact | Public HTTP/wire는 Phase 08 별도 authority |
| Architecture | 필수 | Provider/storage/solver/verifier internal leakage 0 | 없음 |
| Fault/concurrency | 필수 | Every crash/CAS race converges or typed fail | Real race timing에 의존하지 않음 |
| Corruption | 필수 | Tamper/cross-tenant/same-ID-different-bytes fail-closed | 없음 |
| Reproducibility | 필수 | Schedule/duplicate/crash/provider metadata permutation equality | Wall-time quality equality는 대상 아님 |
| Security | 필수 | Tenant precheck, PII/secret/locator redaction | AWS IAM/KMS 실제 증명은 Phase 11 |
| Performance | 제한적 | Reducer bounded work, declared-set size/memory 측정과 regression 기록 | Official throughput/latency/worker 수는 `Q-BENCH-02`/Phase 14 |
| Provider integration | Phase 10 exit에 직접 미적용 | Phase 11 future | Phase 10 provider-neutrality를 cloud success로 대체하지 않음 |
| Hybrid/backend | 미적용 | Test/production dependency 0 | Phase 13 C-17 gated |

### 10.6 Performance와 resource 자문

Phase 10에서 할 수 있는 성능 검사는 semantic 값 발명이 아니라 구현 특성의 관찰이다.

- Declared worker 수에 따른 exact-read/CAS 횟수.
- Event permutation 수와 state transition 수.
- State/artifact payload size와 immutable copy allocation.
- Replay/crash 횟수 증가 시 convergence work.
- Reducer가 prefix listing이나 unbounded provider history scan을 하지 않는지.

공식 worker/round/watchdog/timeout threshold를 이 측정으로 자동 고르지 않는다. 측정 결과, hardware/runtime, manifest와 승인 authority를 Phase 14 calibration 입력으로 넘긴다.

## 11. 사람 checkpoint, evidence와 stop/resume

### 11.1 WP별 checkpoint

| WP | 사람이 확인할 질문 | Stop 신호 | Resume 조건 |
|---|---|---|---|
| 10.0 | Accepted predecessor, auth/failure/build exact owner가 있는가? | Fake/stub/neighbor hash 필요 | Receipt/evidence/owner 승인 |
| 10.1 | Model이 production helper와 독립인가? | Same reducer/codec import | Independent source review |
| 10.2 | Authorized scope/CAS 없이 lookup·side effect가 가능한가? | Ambient authority, lossy failure, lock/lease/LWW | Auth-first/one-CAS/failure model green |
| 10.3 | Expected set이 manifest에서만 오는가? | Listing/event count 사용 | Exact committed-outcome contract |
| 10.4 | Equal candidate가 lineage를 바꾸는가? | Raw scalar/timing tie | Comparator/tie contract 승인 |
| 10.5 | Cancel intent와 terminal fence를 섞었는가? | Two-key “먼저” 사용 | Same-state race test |
| 10.5 | Restart 뒤 deadline을 복원할 수 있는가? | Wall-clock fallback | Durable deadline ADR/test |
| 10.6 | Coordinator가 result를 만들고 있는가? | PASS/summary/payload builder | Phase 07 facade/Phase 09 port 승인 |
| 10.7 | POM/test/evidence가 compile-closed·fresh·independent한가? | Zero test/stale report/skipped/unselected IT | Effective POM + exact rerun + immutable seal |

### 11.2 Planned evidence bundle

| Key | 최소 내용 | 현재 |
|---|---|---|
| `E-P10-STATE` | Source/build/effective-POM/manifest/state/action schema, legal/illegal table, independent model digest, generated sequences, authorization-first/single-CAS/concurrency/crash history, terminal monotonicity | NOT_PRODUCED |
| `E-P10-COMPLETENESS` | Declared screen/worker set, exact outcome refs, missing/failed/unverified/extra cases, completion permutations, comparator/tie/strict lineage와 termination | NOT_PRODUCED |
| `E-P10-RETRY` | WorkerRun/Attempt/Action equality, duplicate/order/late/lost event, cancel/deadline, publication response-loss, tenant authorization와 operation별 denied/not-found/corrupt/precondition/indeterminate/partial failure | NOT_PRODUCED |

각 bundle에는 다음이 필요하다.

- Phase/guide/contract/review version
- Source commit와 exact source/build fingerprints
- Accepted Phase 06~09 receipts/evidence refs
- Java/Maven/runtime fingerprint와 exact command/exit code
- Effective POM, dependency tree, profile activation, Surefire/Failsafe provider/version
- Expected/discovered/passed/failed/error/skipped class/method 수
- Command start와 report `mtime`, exact XML path/digest, missing/duplicate/stale suite 판정
- Model source/digest, seed와 minimized failing sequence
- Artifact/state/action/publication before/after identity
- Crash/race/cancel/deadline/tamper/storage-failure와 last-safe-point matrix
- Authorized scope call trace, tenant/redaction/architecture result
- OPEN/GATED/deferred snapshot과 non-applicable 이유
- Handoff candidate와 rollback point

### 11.3 Evidence DAG

```text
immutable implementation/test artifacts
→ preReviewEvidenceManifest [M]
→ independentReviewReport [references M, digest R]
→ postReviewAcceptanceReceipt [references M + R]
→ scheduler ACCEPTED + handoff
```

Pre-review manifest에 reviewer, verdict, review ref나 acceptance receipt를 넣지 않는다. Review 뒤 manifest를 수정하지 않는다.

### 11.4 상태를 바꿀 권한

구현자와 reviewer는 evidence와 verdict를 제출할 수 있지만 [Execution Progress](../../execution-progress-and-results.md)의 authoritative status/result를 직접 승격하지 않는다. 총괄 스케줄러만 acceptance receipt를 확인해 registry를 갱신한다.

## 12. 흔한 오해와 anti-pattern

| Anti-pattern | 왜 위험한가 | 대신 할 것 |
|---|---|---|
| Visible worker object를 list해 최솟값 선택 | Missing/visibility/order false success | Manifest declared exact refs |
| Provider workflow에서 success count/comparator 계산 | Provider 교체 때 의미 drift | Application reducer/action |
| `StateVersion`을 `ActionId`에 포함 | CAS 뒤 즉시 stale | Semantic action + exact current pending check |
| Run-state token을 publication pointer token으로 사용 | 서로 다른 CAS authority 혼합 | Distinct `PublicationPrecondition` |
| Cancel intent object가 먼저 쓰였으니 cancel 승리 | 서로 다른 key write는 둘 다 성공 가능 | Same run-state CAS race |
| Equal round candidate로 previous champion 교체 | Strict lineage/replay 붕괴 | Equal/worse면 previous champion |
| Worker `MAX_STEPS_REACHED`를 solve success로 사용 | Worker와 solve termination 혼합 | Complete round + normal solve termination |
| Deadline을 plateau로 변환 | Platform failure를 quality로 위장 | Typed watchdog/platform failure |
| Retry 때 새 seed나 warm start | 다른 logical work를 retry로 위장 | Same `WorkerRunId`, new `AttemptId` |
| Event payload objective/PASS 신뢰 | Tamper/duplicate/out-of-order 취약 | Exact stored ref + verifier artifact |
| Same worker/different digest 중 best 선택 | Integrity defect 은폐 | Fail-closed incident |
| Coordinator가 final outcome/audit/payload 생성 | Phase 07 both-gate 우회 | Facade output만 consume |
| Result object가 있으니 success | Existence와 authority 혼동 | Publication pointer CAS/receipt |
| Distributed lock/leader lease 필수 | Crash/provider substitution 취약 | Idempotency + optimistic CAS |
| `HashMap`/completion order tie | Reproducibility 붕괴 | Stable ordinal order |
| Command의 `TenantId` 또는 thread-local을 authorization proof로 사용 | Cross-tenant lookup/existence leak | Explicit authenticated authorized scope before backend |
| Denied/not-found/corrupt/indeterminate를 empty/generic retry로 합침 | Retry와 state disposition이 왜곡됨 | Operation별 lossless typed failure |
| Partial write 뒤 pointer/state를 전진 | Resume가 비권위 artifact에서 시작 | Last authoritative CAS 유지 |
| AWS ARN/S3 key/ETag를 semantic identity로 사용 | Provider migration 시 result identity 변경 | Logical ID + opaque locator |
| Missing config를 legacy 값으로 채움 | `Q-BENCH-02` 우회 | Explicit TEST_ONLY/experiment or reject |
| Phase 13 type를 baseline state에 미리 추가 | C-17 gate 우회 | ALNS-only Phase 10 |
| Root build green만 evidence | Zero-test false green 가능 | Expected discovery + fresh XML |
| Main-source contract suite 또는 unbound `*IT` | Maven lifecycle가 test를 실행하지 않음 | `src/test/java` concrete `*Test`, Failsafe-bound `*IT` |

## 13. 실제 구현 exit checklist와 Definition of Done

### 13.1 Entry와 ownership

- [ ] Phase 00/06/07/08/09 accepted receipt와 exact evidence ref가 있다.
- [ ] Scheduler task, implementer, independent oracle author, reviewer가 지정됐다.
- [ ] State/action/worker/publication precondition owner가 공동 승인했다.
- [ ] Security/Phase 08~10 owner가 explicit non-ambient authorized solve/storage scope와 operation coverage를 승인했다.
- [ ] Phase 09/10 owner가 operation별 lossless failure carrier와 last-safe-point disposition을 승인했다.
- [ ] Build/application/test owner가 POM dependency/profile/Surefire/Failsafe lifecycle를 승인했다.
- [ ] Durable deadline restart ADR와 fault test 계약이 승인됐다.
- [ ] Proposed API를 public/wire authority로 표시하지 않았다.

### 13.2 Identity와 state

- [ ] Manifest-bound solve/round/worker/attempt/action identity가 immutable이다.
- [ ] Invalid/cross-tenant/cross-manifest identity가 constructor/entry에서 거부된다.
- [ ] Legal transition과 terminal monotonicity가 independent model과 일치한다.
- [ ] Opaque repository token이 semantic identity/action payload에 없다.
- [ ] Run-state/worker/publication token type이 분리됐다.
- [ ] Caller-supplied tenant/solve identity는 authorization proof로 사용되지 않는다.

### 13.3 CAS, action과 crash

- [ ] Immutable artifact before pointer와 verified read-back이 구현됐다.
- [ ] Invocation당 authoritative CAS 최대 1회다.
- [ ] CAS loser는 side effect 없이 reload한다.
- [ ] Pending action이 state에 durable하게 저장된다.
- [ ] Executor가 exact current pending action/payload/ordinal을 확인한다.
- [ ] 모든 crash window가 same identity로 수렴한다.
- [ ] Missing/mismatched/ambient authority는 모든 backend lookup/read/decode/dispatch 전에 거부된다.
- [ ] Denied/not-found/corrupt/precondition/indeterminate/partial failure가 lossless하고 state/pointer는 승인된 transition 전까지 last safe point다.
- [ ] Lock/lease/LWW/multi-object transaction correctness path가 0이다.

### 13.4 Completeness, retry와 champion

- [ ] Phase-1 available screen set이 exact declared authority다.
- [ ] Typed unavailable을 missing으로 세지 않는다.
- [ ] 모든 declared worker normal completion + candidate PASS 전 champion이 0회다.
- [ ] Wait/retry remaining과 `INCOMPLETE`가 구분된다.
- [ ] Retry는 `AttemptId` 외 field equality를 보존한다.
- [ ] Completion permutation이 champion/lineage를 바꾸지 않는다.
- [ ] Equal/worse는 previous champion을 보존한다.
- [ ] Normal termination은 complete verified batch 뒤에만 생긴다.

### 13.5 Cancellation, deadline와 publication

- [ ] Intent, cancellation fence와 actual termination이 분리됐다.
- [ ] `CANCEL_REQUESTED`/`PUBLISHING` same-state race가 exactly one meaning으로 수렴한다.
- [ ] Deadline/watchdog/resource/platform failure가 normal termination과 분리됐다.
- [ ] Late event/success가 terminal state를 후퇴시키지 않는다.
- [ ] Phase 07만 publishable result를 생성한다.
- [ ] Both-gate result 외 publish action이 0회다.
- [ ] Distinct publication precondition과 response-loss reconcile가 구현됐다.
- [ ] Same digest conflict만 수렴하고 different digest는 reject한다.

### 13.6 Test, security와 evidence

- [ ] Unit/model/contract/integration/E2E/architecture/fault/corruption/replay/security test가 적용 범위에서 green이다.
- [ ] Required discovered/passed가 expected와 같고 failed/error/skipped가 0이다.
- [ ] App test direct dependency, port/architecture module edge와 effective POM이 compile/dependency closure를 증명한다.
- [ ] `CoordinatorPortContractTest`는 test source에서 Surefire가, `Phase10CoordinatorIT`는 approved profile의 Failsafe가 실제 실행한다.
- [ ] Reactor `-am clean verify`와 strict selected run이 모두 통과하고 fresh XML `mtime`/suite/method/count가 manifest와 일치한다.
- [ ] Independent oracle가 production helper를 import하지 않는다.
- [ ] Provider SDK/state/event, storage implementation, solver/verifier internal edge가 0이다.
- [ ] Cross-tenant artifact는 deserialization/dispatch 전에 거부된다.
- [ ] Secret/PII/provider locator/raw exception leakage가 0이다.
- [ ] `E-P10-STATE`, `E-P10-COMPLETENESS`, `E-P10-RETRY`가 immutable하다.
- [ ] Independent review와 post-review acceptance receipt가 있다.
- [ ] Scheduler가 status를 갱신하기 전 `ACCEPTED`라고 주장하지 않았다.

### 13.7 DoD 문장

다음 문장을 evidence ref와 함께 참으로 말할 수 있을 때만 Phase 10 acceptance 후보가 된다.

> Accepted immutable manifest와 predecessor contracts 아래에서, coordinator는 explicit authorized scope 뒤 exact state/ref만 authority로 사용해 invocation당 하나의 CAS로 legal transition과 durable pending action을 commit한다. Storage failure는 operation별로 손실 없이 보존하고 last safe state/pointer에서 resume한다. 모든 declared worker가 정상 완료·독립 검증된 뒤 stable champion을 고르고 strict lineage, retry identity, cancellation/deadline fence와 Phase 07/09 publication authority를 보존한다. Duplicate/order/crash/provider metadata가 semantic terminal result를 바꾸지 않고, compile/dependency-closed Surefire/Failsafe suite와 세 evidence bundle 및 독립 acceptance receipt가 존재한다.

## 14. 다음 Phase handoff와 broken 증상

### 14.1 Phase 09에서 받아야 할 것

```text
Phase09StorageHandoff
  exact typed ArtifactKey/Ref and verified read
  immutable put-if-absent
  RunStateRepository opaque CAS
  exact committed-worker outcome authority
  distinct publication-pointer precondition/read reconcile
  tenant-scoped authorization binding
  lossless typed failure carrier
  E-P09-STORAGE-CONTRACT / E-P09-CAS / E-P09-TENANT
  accepted review/receipt
```

없어야 하는 것:

- Raw provider locator parsing
- Prefix listing completeness
- Storage-owned transition/champion
- Run-state token reuse
- Ambient authority

### 14.2 Phase 11에 넘길 것

```text
Phase10HandoffManifest
  contract/source/build/runtime fingerprints
  accepted Phase06/07/08/09 receipts
  ExecutionRunId/RoundId/WorkerRunId/AttemptId/ActionId contract
  SolveState/legal-transition/failure taxonomy
  CoordinatorAction schema
    no opaque repository token
    exact current pending authorization
    distinct publication precondition
  explicit AuthorizedSolveScope contract
  lossless operation-failure + last-safe-point disposition
  Artifact/State/Dispatcher/Cancel/Clock/Publisher ports
  declared completeness/comparator/tie/retry/cancel/deadline semantics
  effective POM/dependency/profile/Surefire/Failsafe manifest
  independent model + concrete port-contract/local-E2E suite refs
  E-P10-STATE / E-P10-COMPLETENESS / E-P10-RETRY
  independent review + acceptance receipt
  rollback point
```

Phase 11이 추가할 수 있지만 Phase 10이 넘기지 않는 것:

- S3 key/conditional header/SDK
- Step Functions ASL/type/retry/callback mode
- Lambda event/context/remaining time
- ARN/IAM/KMS/network/retention/quota/cost/IaC
- AWS integration/shadow/rollback evidence

### 14.3 Handoff가 깨졌다는 증상

| 증상 | 깨진 계약 | 돌아갈 곳 |
|---|---|---|
| Step Functions ASL이 worker success 수를 센다 | Declared completeness owner | Phase 10 exact fan-in/action |
| Lambda event에 objective와 PASS가 필수다 | Exact artifact/verifier authority | Phase 07/09 refs |
| S3 ETag가 `ActionId`나 result fingerprint에 들어간다 | Provider-neutral identity | Logical ID/opaque locator |
| Cancel intent가 저장되자 ASL이 execution을 abort하고 success/result pointer가 따로 남는다 | Same-run-state terminal fence | Phase 10 cancel/publish race |
| AWS에서만 wall-clock fallback을 쓴다 | Durable deadline parity | Deadline ADR/Phase 10 last safe |
| Local과 AWS의 same manifest champion이 다르다 | Stable order/retry/provider independence | Phase 10 replay/model |
| Missing worker인데 ASL Succeed로 끝난다 | All-declared gate | `INCOMPLETE` semantics |
| Candidate PASS만으로 result가 retrieval된다 | Both-gate publication | Phase 07/09/10 |
| Caller tenant field만으로 S3 key를 조회한다 | Explicit authorization-first boundary | Security/Phase 08~10 scope contract |
| S3 403/404/corruption/timeout을 모두 empty/retry로 바꾼다 | Lossless storage failure | Phase 09/10 operation oracle |
| Port contract가 main source에 있거나 `*IT` XML이 없다 | Maven discovery/lifecycle | Phase 10 POM/test owner |
| Phase 11 때문에 coordinator action에 ARN field가 추가된다 | Dependency/semantic boundary | Adapter mapping으로 되돌림 |

### 14.4 Phase 12/13/14 authority 보존

- Phase 12는 승인된 provider 축에서 같은 contract suite와 semantic parity를 증명한다. Provider 선택을 Phase 10이 대신하지 않는다.
- Phase 13은 Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17과 backend/legal/native/security/operations/cost 승인이 모두 있어야 시작한다. Phase 10 baseline에 필수가 아니다.
- Phase 14A는 ALNS benchmark qualification을 소유한다. Phase 10 correctness/replay evidence만으로 receipt를 만들지 않는다.
- Phase 14B는 official manifest, provider evidence와 production authority를 소유한다. Phase 10/11 acceptance가 자동 cutover 승인이 아니다.

## 15. Source → requirement → WP → test/evidence 추적성

| Requirement | Source heading | WP | Exact test/oracle | Planned evidence |
|---|---|---|---|---|
| `P10-STATE` Application-owned legal state machine | Integrated §14.1~14.2; Phase 10 §8.1 | 10.1 | `SolveCoordinatorStateMachineTest`, independent model | `E-P10-STATE` |
| `P10-IDENTITY` Manifest-bound run/round/worker/attempt/action | Architecture §3.6; Integrated §14.4 | 10.1 | Constructor/replay tests | `E-P10-STATE`, `E-P10-RETRY` |
| `P10-CAS` Artifact-before-pointer, one CAS, lease-free | Integrated §13.5~13.8; Phase 09 §9 | 10.2 | Idempotency/concurrency/crash | `E-P10-STATE` |
| `P10-ACTION` Exact current pending action authorization | Phase 10 review F-P10-001 | 10.2 | `executorRequiresExactCurrentPendingActionAndOrdinal()` | `E-P10-STATE` |
| `P10-AUTH` Explicit non-ambient authorized scope before lookup/read/decode/dispatch | Canonical Architecture §17; Phase 09 §15.2 | 10.0/2/5/7 | `CoordinatorAuthorizationFailureContractTest` | `E-P10-STATE`, `E-P10-RETRY` |
| `P10-FAILURE` Operation별 lossless failure와 last safe point | Canonical Architecture §17; Phase 09 §7~9 | 10.0/2/5/7 | Authorization/failure contract + independent operation oracle | `E-P10-RETRY` |
| `P10-PHASE1` Declared available screen fan-in | Master §11.3; Phase 10 §8.3 | 10.3 | `PhaseOneFanInTest` | `E-P10-COMPLETENESS` |
| `P10-COMPLETENESS` All declared verified worker | Master §14.4; Integrated §14.3 | 10.3 | `DeclaredRoundCompletenessTest` | `E-P10-COMPLETENESS` |
| `P10-EVENT` Event/listing hint-only | Integrated §13.7; Phase 10 §8.5 | 10.3 | Ordering/lost-wakeup tests | `E-P10-RETRY` |
| `P10-RETRY` Same logical work, new attempt only | Architecture §3.6; Integrated §14.4 | 10.3 | Assignment equality/replay | `E-P10-RETRY` |
| `P10-ORDER` Completion-order independence | Master §14.4 | 10.3~4 | Permutation/model oracle | `E-P10-COMPLETENESS` |
| `P10-STRICT` Equal/worse previous champion | Review F-P10-003; Domain §16 | 10.4 | `equalChampionTerminatesAndRetainsPreviousChampion()` | `E-P10-COMPLETENESS` |
| `P10-CANCEL` Intent/request, same-state fence, actual termination | Review F-P10-002; Phase 10 §8.6 | 10.5 | Cancel/publication race | `E-P10-RETRY` |
| `P10-DEADLINE` Watchdog/platform separation + restart-safe clock | Review F-P10-005; Architecture §5.3 | 10.5 | Virtual clock + crash origin | `E-P10-RETRY` |
| `P10-INTEGRITY` Same identity/different bytes and tamper rejection | Phase 09 §3.2; Integrated §22.4 | 10.2/3/5/6 | `CoordinatorIntegrityTest` | `E-P10-STATE`, `E-P10-RETRY` |
| `P10-FINAL` Phase 07 facade only | Master §14.1; Phase 10 §7.5 | 10.6 | Finalization/publication tests | `E-P10-COMPLETENESS` |
| `P10-PUBLISH` Distinct publication precondition/reconcile | Phase 09 §8.6; review F-P10-001 | 10.6 | Token separation/conflict/crash | `E-P10-RETRY` |
| `P10-PROVIDER` No provider semantic leakage | Architecture §2.2; Integrated §23 | 10.7 | Architecture/port suite | All `E-P10-*` |
| `P10-DISCOVERY` Compile/dependency-closed port contract와 local E2E lifecycle | Canonical Architecture §18~19; Phase 11 §9/§17 | 10.0/1/7 | `CoordinatorPortContractTest`, `Phase10CoordinatorIT`, fresh XML oracle | All `E-P10-*` |
| `P10-SECURITY` Tenant-first, non-revealing result와 redaction | Canonical Architecture §17; Integrated §20 | 10.5/7 | Cross-tenant/redaction tests | `E-P10-STATE`, `E-P10-RETRY` |
| `P10-OPEN` No hidden official value | `Q-BENCH-02`; realization plan §14 | 10.0/7 | Missing-config negative test | `E-P10-STATE` |
| `P10-GATED` Phase 13 not baseline | README §3/§6; C-17 | 10.0/7 | Dependency/module absence rule | Architecture report |
| `P10-HANDOFF` Thin Phase 11 mapping | Integrated §15; Phase 11 §18.1 | 10.7 | Local/provider action parity future | `Phase10HandoffManifest` |

## 16. 이 guide 자체와 현재 scope의 정적 검증

이 section의 명령은 Phase 10 구현 test가 아니라 **이 Markdown 산출물 하나의 범위·링크·공백 검증**이다.

```bash
# 대상 파일 존재와 제목
test -s docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md
test "$(rg -n '^# Phase 10 사람용 구현 가이드 — Provider-neutral coordinator$' \
  docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md | wc -l)" -eq 1

# 금지된 implementation/source 수정이 이 작업으로 생기지 않았는지는
# 시작 snapshot과 현재 status를 비교해 판단한다.
git status --short

# Trailing whitespace와 tracked diff
git diff --check

# 새 untracked guide 자체 whitespace
if rg -n '[[:blank:]]+$' \
  docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md; then
  exit 1
fi

# Gate와 canonical Phase 수 표현
rg -n '총 15개|OPEN — EXPERIMENT_REQUIRED|GATED TARGET|DEFERRED|Phase 13|Phase 14A|Phase 14B' \
  docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md

# Current/FUTURE Maven 경계
rg -n '현재.*0개|Future 검증 명령|현재 module은 부재|failIfNoSpecifiedTests=true' \
  docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md
```

상대 link는 target file 존재와 GFM heading slug를 함께 검사한다. Fragment가 깨지면 링크를 제거해 숨기지 말고 정확한 source heading으로 고친다.

## 17. 남아 있는 gate 요약

| Gate | 현재 | 마지막 안전 지점 | 해제 조건 |
|---|---|---|---|
| Phase 00/06/07/08/09 acceptance | BLOCKED | 이 guide + contract/model plan | Accepted evidence/review/receipt |
| `SOURCE_INDEX_CONFLICT` | OPEN DOCUMENTATION CONFLICT | User-fixed canonical 5 + dated historical only | Implementation index owner aligns maps without semantic regression |
| Scheduler/owner | NOT ASSIGNED FOR P10 | 문서만 | Exact task/roles registry |
| Tenant authorization | CROSS-PHASE BLOCKED | Backend lookup/dispatch 0 | Explicit non-ambient scope + negative contract tests |
| Lossless storage failure | CROSS-PHASE BLOCKED | Last authoritative state/pointer | Operation carrier + state disposition/oracle approval |
| Pending action/publication precondition | CROSS-PHASE BLOCKED | Pure reducer, unpublished result ref | Typed port/read/CAS + conformance |
| Cancellation/publication race | CROSS-PHASE BLOCKED | Intent request only | Same-state race/too-late/crash approval |
| Durable monotonic restart | CROSS-PHASE BLOCKED | Virtual `TEST_ONLY` clock | Durable ADR + restart tests |
| Maven port/E2E discovery | BUILD-OWNER GATED | Future-red source/manifest only | Effective POM + reactor/selected strict runs + fresh XML |
| `Q-BENCH-02` | OPEN — EXPERIMENT_REQUIRED | Explicit test/experiment manifest | Calibration result + approval |
| Phase 13/C-17 | GATED TARGET | ALNS-only coordinator | 14A receipt + separate approvals/evidence |
| `Q-VAR-01` | DEFERRED | Current pair/terminal/bank | Resume evidence + separate approval |
| Phase 11 AWS | DOWNSTREAM BLOCKED | Provider-neutral handoff only | Phase 10 accepted + AWS gates |
| Phase 14A | NOT RUN | Correctness evidence only | Approved benchmark bundle/receipt |
| Phase 14B production | AUTHORITY NOT GRANTED | No cutover | Official/provider/ops/security approval |

이 표의 gate를 hidden default, mock success, legacy GCP behavior, 부분 cloud 성공 또는 문서 hash로 닫지 않는다.
