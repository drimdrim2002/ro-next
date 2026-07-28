# Phase 10 — 여러 round를 조정하는 coordinator

```yaml
document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
document_version: 1.3
phase: "10"
phase_name: provider-neutral-coordinator
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
evidence_status: NOT_PRODUCED
review_status: COMPLETE_CHANGES_REQUIRED
review_document: ../reviews/phase-10-review.md
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Application/Coordinator owner role
  state_and_storage_contract: Phase 08 Application Port and Phase 09 Object Storage owner roles
  worker_semantics: Phase 06 Solver/Search owner role
  verification_and_finalization: Phase 07 Independent Verification owner role
  downstream_aws_mapping: Phase 11 AWS Reference Distribution owner role
  downstream_provider_substitution: Phase 12 Provider Substitution owner role
  quality_and_official_values: Benchmark/Quality owner role
  operations_and_cancellation: Platform/Operations owner role
  security: Security/Tenant Boundary owner role
  review: independent Phase 10 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture and architecture rules
  - Phase 06 accepted worker termination, retry-stable replay, and committed-candidate contract
  - Phase 07 accepted candidate/result verification and finalization contract
  - Phase 08 accepted provider-neutral application ports and deterministic local runtime
  - Phase 09 accepted immutable artifact, exact-key retrieval, and single-state/pointer CAS contract
  - exact ExecutionManifest with explicit test/experiment values; no omitted official numeric values
  - exact scheduler task ID, implementer, model-oracle author, and independent reviewer assignment
planned_evidence:
  - E-P10-STATE
  - E-P10-COMPLETENESS
  - E-P10-RETRY
source_sections:
  canonical_master: "§4.1~4.6, §11.3, §11.9~11.10, §13, §14.1, §14.4, §15.10, §16~17"
  final_domain: "§14~17, especially multi-round lineage, verification/finalization, and error/termination"
  final_architecture: "§2.4~2.5, §3.1~3.6, §5.1~5.6, §6"
  integrated_design: "§12~15, §19~25, §26.3~26.4, §27~28"
  open_questions: "Q-BENCH-02, Q-INFRA-01, Q-VAR-01; status summary and remaining gates"
  master_realization_plan: "§2~6, Phase 06~12, §8~15"
  implementation_readme: "§1~7"
  execution_progress: "§1~5, §8~9"
  phase_06_actual: "§7.1~7.6, §8.3~8.5, §13~17"
  phase_07_actual: "§7~9, §12~16"
  phase_08_actual: "§6.3~7.6, §9.1~9.3 and §16.3"
  phase_09_actual: "§7.2, §7.5~7.6, §8.1~8.6, §9.1~9.6 and §15.2"
  phase_11_actual: "linked expected-handoff document; re-read at Phase 11 entry; no fingerprint"
source_fingerprints_sha256:
  README.md: 22eff4f63607db29bd4049344986109c680aa970d0865a3b859598e6b3b96c06
  docs/README.md: 5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569
  docs/master-design.md: 58554334b9f27586c93a685adc0facf0fbd7e79576c18890f0ac13891b2f803b
  docs/2026-07-26-domain-design.md: 1870662f85a08cc9a1e48a1974b96278eccddfd1519721d71b356c56034ecaab
  docs/2026-07-26-architecture-design.md: 3d4dbbfc7e4cbdb2f3985378d84fd5f717db770b04131573c00ed354a9f41614
  docs/architecture-domain-implementation-design.md: ec513ac1b0bacd88149683bf48c36f7e6edcd53a9232498597e3b0d57c585875
  docs/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 5921213ae419b9398bde8c91c3d6ada5aa64bf22a9b823e5b3889e1642085c05
  docs/implementation/README.md: accf7758802c253ae47e3d0fe41e190728c507195d0b41f27f14a25804c8f23f
  docs/implementation/execution-progress-and-results.md: 75eac895fd3a3c930e5135a4ef57badb0c540bfa692d928188af9eeb2f73b803
adjacent_phase_documents:
  phase_08_09_11_batch: CONCURRENT_SCHEDULER_AUTHORIZED_REVIEW_BATCH_OBSERVED_NOT_ACCEPTED
  phase_10_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
neighbor_validation_policy:
  rule: ADJACENT_PHASE_AND_REVIEW_DIGESTS_NOT_PERSISTED_OR_USED_FOR_ACCEPTANCE
  method: cited-section semantic comparison plus accepted artifact/evidence identity at entry
historical_cross_check:
  superseded_master:
    file: docs/2026-07-26-master-design.md
    status: SUPERSEDED_NOT_AUTHORITY
    sha256: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
  codex_phase:
    file: docs/codex/phases/phase-07-logical-multi-round-coordinator.md
    status: HISTORICAL_NOT_AUTHORITY
    sha256: a68a4d9131e37e8f84b2197214d29b7edea36244f1fce7fff3be05e5989bb12d
```

**부제: Provider-neutral multi-round coordinator**

## 1. 문서 지위, 권위와 source 대조

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 권위 원문의 `REVIEW` metadata는 provenance로 보존하지만 이 상세 문서 작성을 중단하는 조건이 아니다. 반대로 이 문서, proposed Java type, future-red test 이름 또는 pseudocode가 존재한다는 사실은 Phase 10 구현, evidence, 독립 review 또는 production coordination이 완료되었다는 뜻이 아니다.

상태 축을 분리한다.

| 상태 축 | 현재 값 | 의미 |
|---|---|---|
| 문서 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS` | 독립 문서 review의 안전·명백한 수정이 반영됨 |
| 구현 | `NOT_STARTED` | Target module/type/test가 현재 있다고 주장하지 않음 |
| evidence | `NOT_PRODUCED` | `E-P10-*`은 미래 bundle의 요구 이름 |
| entry | `BLOCKED_BY_UNACCEPTED_PREDECESSORS` | Phase 06~09 accepted contract/evidence가 없음 |
| review | `COMPLETE — CHANGES_REQUIRED` | [독립 리뷰](../reviews/phase-10-review.md)는 완료됐으나 residual cross-phase blocker와 구현/evidence gate가 남음 |
| handoff | `NOT_READY` | Phase 11/12/14가 소비할 accepted contract가 없음 |

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 multi-round, verification/result와 termination 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module, port, distributed state와 identity 배치
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 15 Phase, no-DB storage와 provider boundary
6. [Master Realization Plan](../master-realization-plan.md), [구현 문서 지도](../README.md), [progress registry](../execution-progress-and-results.md)

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. [historical logical coordinator](../../codex/phases/phase-07-logical-multi-round-coordinator.md)를 포함한 `docs/codex/*`는 2026-07-24 역사 자료이며 current API, 상태, 질문 또는 evidence authority로 사용하지 않는다. Historical 문서의 `Q-INFRA-01 DEFERRED`, 과거 phase 번호와 source hash는 최신 등록부의 `Q-INFRA-01 RESOLVED`, canonical Phase 10 map으로 덮어쓴다. Historical 문서에서 current canonical sources가 독립적으로 확정하지 않은 이름·수치·경로는 이 문서로 복사하지 않는다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 10에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §4.1~§4.6, §11.3, §11.9~§11.10, §13, §14.1, §14.4, §15.10, §16~§17 | Complete worker fan-in, stable champion, normal/exceptional termination, both-gate publication, provider-neutrality |
| [Final Domain](../../2026-07-26-domain-design.md) | §14~§17 | Round/worker lineage, all-declared completeness, finalization/result authority, `INCOMPLETE`와 정상 종료 분리 |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §2.4~§2.5, §3.1~§3.6, §5.1~§5.6, §6 | Application-owned state, logical identities, retry/cancel, port/dependency와 failure 계약 |
| [Integrated design](../../architecture-domain-implementation-design.md) | §12~§15, §19~§25 | Phase 08 port, Phase 09 immutable artifact/CAS, Phase 10 action/state, Phase 11 thin mapping, test/anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01`, 상태 요약과 남은 gate | 공식 실행 수치 open, AWS target resolved, optional variant deferred |
| [Master Realization Plan](../master-realization-plan.md) | §2~§6, Phase 06~12, §8~§15 | Current inventory, Phase 10 entry/exit/evidence, DoD, blocker와 handoff |
| [구현 문서 지도](../README.md) | §1~§7 | Canonical filename, source authority, planned/actual와 review 규칙 |
| [Progress registry](../execution-progress-and-results.md) | §1~§5, §8~§9 | Scheduler task `TBD`, Phase 10 `PLANNED`, status 갱신 권한과 blockers |
| [Actual Phase 06](phase-06-cow-alns-reproducibility.md) | §7.1~§7.6, §8.3~§8.5, §13~§17 | `CommittedCandidate`, replay/termination, retry-stable seed/warm start, Phase 10 future-red 경계 |
| [Actual Phase 07](phase-07-independent-verification-final-result.md) | §7~§9, §12~§16 | Candidate/result `PASS`, `PublishableResult`, finalization authority와 provider-neutral handoff |
| [Actual Phase 08](phase-08-application-ports-local-runtime.md) | §6.3~§7.6, §9.1~§9.3 | `WorkerDispatcher`, `CancellationPort`, storage/publication seam, logical identity, monotonic deadline와 application failure |
| [Actual Phase 09](phase-09-object-storage-no-database.md) | §7.2, §7.5~§7.6, §8.1~§8.6, §9.1~§9.6, §15.2 | Opaque locator 비해석, run scope, verified read, canonical record, exact-key repository, CAS/publication과 Phase 10 storage handoff |
| [Actual Phase 11](phase-11-aws-reference-distribution.md) | 작성 시 expected handoff 대조; entry 재독해 필요 | Phase 11이 요구하는 `AdvanceSolve`, state/action, declared set, retry/cancel/publication handoff shape |
| [Root README](../../../README.md)와 actual source | 기술 기준·placeholder 설명, GCP controller/workflow | Current GCP demo를 target coordinator evidence로 오인하지 않는 inventory |

Phase 08, Phase 09와 Phase 11 상세는 shared checkout의 scheduler-authorized concurrent review batch로 관찰해 최신 contract/blocker section을 한 번 대조했다. 이들은 actual proposed contract source이되 accepted implementation authority는 아니다. Phase 08은 cancellation intent 뒤 legal run-state `CANCEL_REQUESTED` CAS를 명시하고, Phase 09는 run-state/publication precondition 분리를 여전히 cross-phase blocker로 둔다. Phase 11의 cancel-intent-first 표현은 §14.2 handoff와 재정렬이 필요하다. 인접 whole-file hash/status snapshot을 기록·반복 추적하지 않으며 Phase 11 entry에서 linked contract를 다시 읽는다.

인접 Phase/review의 whole-file 또는 section digest는 acceptance receipt가 아니다. Phase 10 entry에서는 위에 인용한 안정 section을 다시 읽고 accepted artifact/evidence identity와 semantic compatibility를 검증한다. Phase 11은 이 파일의 §14.2 expected handoff와 linked document를 자기 entry에서 재독해한다. Phase 10은 인접 문서 변경을 감시하거나 reciprocal hash를 기록하지 않는다.

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 10은 하나의 immutable `ExecutionManifest` 아래 phase-1과 여러 phase-2 round를 조정하는 **provider-neutral application coordinator**를 구현·검증한다. Coordinator는 exact solve state를 읽고 하나의 legal transition을 결정하며, immutable artifact를 만든 뒤 하나의 authoritative state/pointer를 CAS하고 provider-neutral action을 반환한다.

완료된 logical flow는 다음 의미를 보존해야 한다.

```text
immutable manifest and solve snapshot
→ phase-1 declared screens and stable champion
→ phase-2 declared worker assignments
→ all declared worker outcomes by exact reference
→ candidate-verifier PASS for every accepted worker
→ stable comparator + declared tie policy
→ strictly better next-round warm start
   or NO_STRICT_IMPROVEMENT
   or MAX_ROUNDS_REACHED
→ Phase 07 finalization/result verification
→ both-gate PublishableResult only
→ Phase 09 ResultPublisher CAS
→ SUCCEEDED
```

동일 logical input과 strong-replay envelope에서 physical dispatch 순서, event delivery 순서, coordinator process 수와 crash/restart 횟수가 달라도 committed state lineage, champion, termination과 published result identity가 같아야 한다.

### 2.2 포함 범위

- `SolveId`/manifest-bound run, `RoundId`, `WorkerRunId`, `AttemptId`와 action identity
- Immutable execution/round/assignment/completion/finalization/publication artifact reference
- Application-owned solve/round state와 legal transition table
- 한 invocation이 한 authoritative CAS만 수행하는 lease-free optimistic concurrency
- Durable pending action, action replay와 crash/restart reconciliation
- Phase-1/phase-2 declared fan-out, exact-key fan-in과 partial round 판정
- Retry에서 logical worker, seed, warm start, requested work 보존
- Duplicate, out-of-order, late, stale, lost wake-up/event의 idempotent 처리
- Stable comparator와 manifest-declared deterministic tie policy를 이용한 champion reduction
- Strict improvement, `NO_STRICT_IMPROVEMENT`, `MAX_ROUNDS_REACHED`
- Cooperative cancellation intent, stop request, actual termination과 last completed boundary 분리
- Explicit watchdog/deadline observation과 `WATCHDOG_REACHED`/`PLATFORM_TIMEOUT` 분리
- Manifest/artifact/state/version/fingerprint tampering의 fail-closed integrity 판정
- Phase 07 finalization/result-verification 호출과 Phase 09 publication CAS orchestration
- Fake/local port contract, state-transition/model-based oracle, fault/corruption/replay evidence
- Phase 11이 매핑할 provider-neutral action/port/failure contract

### 2.3 명시적 비범위

- Phase 03/04 objective, comparator, tie policy의 품질 의미 또는 verifier 계산 재구현
- Phase 05/06 initial construction, ALNS search state, COW mutation, seed derivation이나 completed-step 계산
- Phase 07 candidate/result verifier, exhaustive audit, final outcome, canonical payload 또는 `PASS` 생성
- Phase 08 public API/HTTP schema, local filesystem implementation과 external status compatibility 승인
- Phase 09 object key layout, digest/canonical encoding, CAS token 또는 object-storage backend 구현
- S3, Step Functions, Lambda, ARN, AWS event, IAM, retry policy, resource sizing과 deployment
- GCP Workflows/Cloud Run/GCS 또는 다른 provider adapter 구현
- Object prefix listing, event stream 또는 workflow history를 source of truth로 만드는 기능
- Distributed lock, coordinator leader lease, database, multi-object transaction 또는 last-write-wins
- Phase 13 route pool/MIP, cross-worker pool merge, central selector와 optimizer capacity lease
- `Q-BENCH-02`의 official `screenMaxSteps`, worker 수, `phase2MaxSteps`, `maxRounds`, watchdog 수치
- `Q-VAR-01`, multi-trip/rotation, dynamic routing과 실시간 replanning
- Exceptional last committed best를 normal success/official result로 승격하는 product 정책
- Public Java API, wire schema, canonical hash algorithm 또는 external compatibility의 최종 승인

Coordinator는 solver correctness, search state 또는 storage correctness의 소유자가 아니다. Phase 06/07/08/09의 accepted contract를 조립하고 그 결과를 검증된 reference와 typed verdict로만 소비한다.

## 3. Phase-local 결정, 미확정 항목과 불변조건

이 절의 package/type/signature는 모두 **PROPOSED INTERNAL**이다. Phase 08/09/10/11 cross-phase review에서 의미를 유지하는 범위로 이름과 visibility를 바꿀 수 있으며 public/wire compatibility 약속이 아니다.

### 3.1 결정 상태

| 항목 | 상태 | Phase 10 판단 |
|---|---|---|
| Semantic owner | FIXED | State/completeness/retry/cancel/finalization sequencing은 application coordinator가 소유 |
| Concurrency | PROPOSED SAFE BASELINE | Distributed lock/leader lease 없이 single authoritative state CAS가 linearization point |
| Side-effect recovery | PROPOSED SAFE BASELINE | CAS된 durable pending action을 재반환; dispatcher/publisher는 logical action key로 idempotent |
| Event authority | FIXED | Event는 wake-up hint. Exact state, manifest와 declared artifact ref가 authority |
| Worker completeness | FIXED | 모든 declared worker의 normal completion과 candidate verification `PASS`가 필요 |
| Champion semantics | FIXED/PROPOSED DETAIL | Bound comparator 의미는 upstream. Exact deterministic tie policy ID/version은 manifest에 필수이며 tuple 이름은 review 대상 |
| Retry identity | FIXED | `WorkerRunId`, seed, warm start, config, requested steps 고정; `AttemptId`만 변경 |
| Cancellation | FIXED | Intent, dispatch stop, actual termination과 last completed boundary 분리 |
| Deadline/watchdog | FIXED/PROPOSED DETAIL | Quality step/round 종료와 분리된 safety observation. Exact official duration은 open |
| Finalization authority | FIXED | Phase 07만 `PublishableResult` 또는 typed rejection을 생성 |
| Publication authority | FIXED MEANING / CROSS-PHASE TYPE BLOCKED | Both-gate result ref만 publication 가능. Run-state fence token과 publication-pointer precondition은 서로 다른 identity여야 하며 exact port shape는 Phase 08/09/10 review 대상 |
| AWS target | RESOLVED, DOWNSTREAM | `Q-INFRA-01`의 AWS 선택은 Phase 11 mapping 입력이며 Phase 10 코드에 AWS type을 허용하지 않음 |
| Official numeric plan | `OPEN — EXPERIMENT_REQUIRED` | `Q-BENCH-02`; test/experiment manifest만 explicit authority와 함께 허용 |
| Optional hybrid | `C-17 GATED TARGET` | Baseline Phase 10에서 활성화하지 않음; accepted Phase 13 manifest 확장만 future input |
| Optional variants | `Q-VAR-01 DEFERRED` | 질문·설계·활성화하지 않음 |

### 3.2 반드시 지킬 불변조건

1. **Manifest immutability:** Solve 시작 뒤 snapshot, profile, algorithm, seed, worker/round plan과 tie policy를 다시 조회하거나 바꾸지 않는다.
2. **Identity hierarchy:** 모든 round, worker, attempt, action과 artifact는 같은 `TenantId/SolveId/ManifestFingerprint` 계보를 가진다.
3. **Retry stability:** Retry는 `AttemptId` 외 seed, warm start, requested steps, worker config와 logical assignment fingerprint를 바꾸지 않는다.
4. **Artifact before pointer:** Immutable artifact를 put-if-absent하고 read-back digest를 검증한 뒤 하나의 state/pointer만 CAS한다.
5. **Single linearization:** 한 business transition의 권위 commit은 정확히 하나의 `RunStateRepository.compareAndSet` 또는 publication CAS다.
6. **Lease-free correctness:** Coordinator correctness는 leader election, lease expiry, file lock, process-local mutex 또는 provider workflow single-execution 보장에 의존하지 않는다.
7. **Durable action:** 외부 side effect가 필요한 state는 stable `ActionId`와 payload ref를 가진 pending action을 durable state에 포함한다.
8. **Idempotent replay:** Crash 후 같은 pending action을 다시 실행해도 same identity/same digest는 수렴하고 different digest는 integrity failure다.
9. **Declared completeness:** Expected worker set은 manifest/round plan에서만 나온다. Prefix list, 도착 event와 성공 worker 수로 추정하지 않는다.
10. **Partial is not success:** Missing, failed, abnormal, unverified, wrong-assignment 또는 conflicting worker가 하나라도 있으면 champion, next round와 finalization을 차단한다.
11. **Open versus incomplete:** 재시도/대기 가능성이 명시적으로 남아 있으면 `ROUND_RUNNING`과 `WaitForWorkers`; 더 진행할 승인된 경로가 없을 때만 `INCOMPLETE`.
12. **Verification first:** Worker candidate의 candidate-verifier `PASS`와 exact report/solution ref 없이는 champion 후보가 아니다.
13. **Order independence:** Comparator reduction input은 stable worker ordinal 순서다. Completion/event/list/hash iteration order를 쓰지 않는다.
14. **Strict lineage:** 다음 round warm start는 complete round champion이 이전 champion보다 `STRICTLY_BETTER`일 때만 바뀐다.
15. **Normal termination truth:** `NO_STRICT_IMPROVEMENT`와 `MAX_ROUNDS_REACHED`는 complete verified batch 뒤에만 coordinator가 생성한다.
16. **Exceptional truth:** Cancel, watchdog, resource, platform, verifier, integrity와 publication failure를 정상 품질 종료로 바꾸지 않는다.
17. **Cancellation fence:** Cancellation intent record는 durable request이며 authority state가 아니다. Same run-state pointer의 `CANCEL_REQUESTED` CAS가 이긴 뒤에는 dispatch, champion, finalization 또는 publication을 새로 commit하지 않는다.
18. **Publication fence:** `PUBLISHING` state CAS가 이기면 terminal intent는 publication으로 고정되고 result pointer CAS/response-loss를 reconcile한다. 뒤늦은 cancel로 의미를 바꾸지 않는다.
19. **Deadline fence:** Deadline/watchdog terminal CAS 뒤 late success가 normal result를 만들 수 없다.
20. **Late/stale isolation:** 과거 round/attempt 또는 terminal solve의 event는 state를 후퇴시키지 않는다. 같은 logical success의 different digest만 integrity incident로 승격한다.
21. **Finalization authority:** Coordinator는 `FinalOutcome`, audit, result summary, payload 또는 verifier `PASS`를 합성하지 않는다.
22. **Publication authority:** `SUCCEEDED`는 exact `PublishableResultRef`의 publication CAS가 동일 digest로 성공/수렴한 뒤에만 가능하다.
23. **No provider identity:** ARN, bucket/key, workflow execution ID, provider request ID와 runtime attempt는 semantic result/quality identity가 아니다.
24. **No hidden official value:** Test-only/experiment 수치는 manifest authority와 evidence에 남고 official/production default가 될 수 없다.
25. **Tenant safety:** 모든 exact ref와 command는 tenant scope를 가지며 cross-tenant ref는 deserialization/dispatch 전에 거부한다.
26. **No normal payload on failure:** `FAILED`, `INCOMPLETE`, `CANCELLED`, `PUBLICATION_REJECTED` 등에는 normal route/outcome/benchmark payload를 노출하지 않는다.
27. **Distinct CAS preconditions:** Run-state version, worker-commit version과 publication-pointer version을 서로 대체하지 않는다. Opaque repository token을 semantic `ActionId` 또는 action payload fingerprint에 포함하지 않는다.

## 4. Entry gate와 확인 evidence

문서 review와 test/model 설계는 가능하지만 production implementation 착수와 `ACCEPTED` 주장은 다음 gate가 모두 충족될 때까지 차단한다. 아래 review/status 관찰은 final audit 시점의 live 값이며, 최초 작성 당시 `READY_FOR_REVIEW`였다는 기록은 이 review의 historical finding에만 보존한다.

| Entry 항목 | 확인 방법 | 2026-07-28 live review/status 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Multi-module reactor, application/architecture modules, accepted review와 `E-P00-ARCH` | Root 단일 Maven JAR; accepted evidence 없음 | BLOCKED |
| [Actual Phase 06](phase-06-cow-alns-reproducibility.md) accepted | `CommittedCandidate`, `ReplayManifest`, exact worker termination/step/seed/warm-start와 `E-P06-*` | Review `COMPLETE — CHANGES_REQUIRED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, entry `BLOCKED` | BLOCKED |
| [Actual Phase 07](phase-07-independent-verification-final-result.md) accepted | Candidate/result verifier, Phase 07 facade, `PublishableResult`와 `E-P07-*` | Review `COMPLETE — CHANGES_REQUIRED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | BLOCKED |
| [Actual Phase 08](phase-08-application-ports-local-runtime.md) accepted | Application identity, artifact/state/dispatch/cancel/publication ports와 local E2E | Review `COMPLETE — CHANGES_REQUIRED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | BLOCKED |
| [Actual Phase 09](phase-09-object-storage-no-database.md) accepted | Exact-key immutable artifact, state/publication CAS suite, tenant isolation와 `E-P09-*` | Review `COMPLETE — CHANGES_REQUIRED`; phase acceptance `BLOCKED_NOT_IMPLEMENTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | BLOCKED |
| State/action/publication precondition contract | Action authorization은 exact pending state, publication은 distinct pointer precondition을 사용 | Phase 09 §3.1/§3.2가 cross-phase blocker로 명시; accepted signature 없음 | CROSS_PHASE_BLOCKED |
| Manifest contract review | Identity, explicit plan, tie/retry/deadline policy, canonical inclusion/exclusion | 이 문서의 proposed contract만 존재 | CONTRACT_GATE |
| Model oracle independence | Production reducer/serializer/ports를 import하지 않는 reference model review | 이 문서 §10의 future plan만 존재 | REVIEW_REQUIRED |
| Scheduler/owners | Exact task ID와 implementer/oracle/reviewer 배정 | Registry와 metadata 모두 `TBD` | OWNER_GATE |

`Q-BENCH-02`는 provider-neutral state machine과 명시적 `TEST_ONLY` fixture를 막지 않는다. 그러나 official manifest, official benchmark와 Phase 14 cutover는 계속 차단한다. Phase 11 AWS integration environment와 ADR은 Phase 10 entry가 아니라 downstream Phase 11 entry다.

Implementation 시작 전 최소 equality receipt:

```text
state.solveId == command.solveId
state.manifestFingerprint == ExecutionManifest.intrinsicFingerprint
manifestRef.contentDigest == readVerified(manifestRef).contentDigest
manifest.solveSnapshotRef == accepted Phase08/09 snapshot ref

for every declared worker:
  assignment.roundId.manifestFingerprint == state.manifestFingerprint
  assignment.warmStartFingerprint == round.commonWarmStartFingerprint
  assignment.configFingerprint == manifest.workerPlan[workerOrdinal].configFingerprint
  assignment.requestedSteps == manifest.workerPlan[workerOrdinal].requestedSteps
```

하나라도 다르면 일부를 고쳐 계속하지 않고 `FAILED(INTEGRITY_VIOLATION)` 또는 typed entry rejection으로 끝낸다.

## 5. Current inventory와 proposed change tree

### 5.1 Repository source/build snapshot와 live document status

Source/build 조사의 historical baseline은 branch `codex/domain-design`, commit `3424277c9c74f8151a83be056a07dd4659331beb`이다. 아래 code/build/runtime 사실은 이 snapshot을 보존한다. Document/review 상태 행만 final audit 시점의 live 관찰이며 historical snapshot과 섞지 않는다. `docs/implementation/`은 shared checkout에서 이미 untracked 문서 세트로 존재하며 이 Phase 작업은 대상 파일 외 다른 파일을 수정하지 않는다.

| 영역 | Actual 사실 | Phase 10 해석 |
|---|---|---|
| Build | Root `pom.xml` 하나의 `com.ronext:ro-next` JAR, Google SDK가 root dependency | Target application coordinator/module/CAS architecture 없음 |
| Application | `AlnsBatchEngine`이 seed/iterations로 synthetic `double objective` map 생성 | Solver/worker/finalization authority가 아님 |
| API | `OptimizationApiController`가 `gs://`와 GCP Workflows를 직접 사용하고 clock-derived seed/default 수치 적용 | Target identity/idempotency/explicit manifest와 불일치 |
| Worker/finalize | `OptimizationWorkerController`가 GCS prefix listing 후 보이는 objective 최솟값을 선택 | Declared completeness, verifier, CAS와 completion-order independence 위반 |
| Workflow | `gcp/workflows/optimization.yaml`이 parallel HTTP 호출 뒤 finalize | Historical migration inventory; target provider-neutral coordinator가 아님 |
| Test | Synthetic `AlnsBatchEngineTest` 한 개 | State/retry/crash/cancel/completeness evidence 없음 |
| Phase docs/reviews — live | Phase 06~09 및 Phase 11 review는 `COMPLETE — CHANGES_REQUIRED`이고 구현/evidence/phase acceptance는 미완료다. Phase 10도 `COMPLETE_CHANGES_REQUIRED / NOT_STARTED / NOT_PRODUCED / NOT_READY`이며 registry status authority는 별도다. | Completed document review를 accepted predecessor/implementation evidence로 승격하지 않음 |

Actual inventory fingerprint:

```text
pom.xml
  f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6
gcp/workflows/optimization.yaml
  65eeef9344a63b743af1684b915c9bdfa976454e560ebb8624d75ed00fd874c1
AlnsBatchEngine.java
  4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481
OptimizationApiController.java
  3dcab11fcac78bb994b1f77bedaf425601684e8b9b3eb1560c305dcf671f358c
OptimizationWorkerController.java
  846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c
```

이 hash는 read-only inventory provenance이지 target implementation/evidence가 아니다.

### 5.2 Proposed change tree

아래 tree는 Phase 00/08/09가 realization plan의 proposed module naming을 그대로 채택할 때의 **proposed internal target**이다. 실제 predecessor가 다른 동등 경로/API를 승인하면 중복 파일을 만들지 않고 Phase 08~11 cross-phase mapping을 먼저 review한다.

```text
rpdptw/application/
├── src/main/java/com/ronext/rpdptw/application/
│   ├── port/in/
│   │   └── AdvanceSolve.java
│   ├── port/out/
│   │   ├── ArtifactStore.java             # Phase 08/09 contract 소비
│   │   ├── RunStateRepository.java        # Phase 08/09 contract 소비
│   │   ├── ResultPublisher.java            # Phase 08/09 contract 소비
│   │   ├── WorkerDispatcher.java           # Phase 08 contract 소비
│   │   ├── CancellationPort.java           # Phase 08 contract 소비
│   │   ├── TelemetryPort.java              # Phase 08 contract 소비
│   │   └── MonotonicClock.java             # Phase 08 contract 소비
│   ├── execution/
│   │   ├── ExecutionIdentity.java
│   │   ├── ExecutionManifest.java
│   │   ├── WorkerAssignment.java
│   │   ├── WorkerCompletion.java
│   │   └── CoordinatorDeadlinePolicy.java
│   └── coordinator/
│       ├── SolveState.java
│       ├── RoundState.java
│       ├── CoordinatorEvent.java
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
    ├── CoordinatorFinalizationPublicationTest.java
    ├── CoordinatorReplayTest.java
    ├── CoordinatorModelBasedPropertyTest.java
    └── Phase10CoordinatorIT.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase10CoordinatorArchitectureTest.java

build/port-contract-tests/
└── src/main/java/com/ronext/rpdptw/contract/coordinator/
    └── CoordinatorPortContractSuite.java

apps/coordinator/
└── provider-neutral command adapter only
```

Phase 10은 Phase 08/09 port를 소비·필요 최소 확장할 수 있지만 backend를 구현하지 않는다. `apps/coordinator`는 application command를 호출하는 generic assembly 경계이고 AWS handler/event는 Phase 11의 adapter/distribution에만 둔다.

## 6. Artifact, identity와 lifecycle

### 6.1 Artifact와 authority

| Artifact/state | Owner | 최소 identity/내용 | Lifecycle/소비자 |
|---|---|---|---|
| `SolveSnapshotRef` | Phase 08 | Problem/travel/profile exact identities와 digest | Immutable solve authority |
| `ExecutionManifestRef` | Phase 08/10 contract | Snapshot/build/runtime/algorithm/seed/worker/round/tie/retry/deadline plan | Put-once; solve 전체에서 불변 |
| `RunState` | Phase 10 semantic, Phase 09 storage | Solve/manifest/transition ordinal, state payload, pending action, terminal/failure; opaque `StateVersion`은 wrapper | Single authoritative CAS object |
| `PhaseOnePlanRef` | Phase 06/10 orchestration | Stable available portfolio slots, screen config/identity와 expected screen set | Immutable; phase-1 fan-out authority |
| `PhaseOneScreenOutcomeRef` | Phase 06/08 handoff | Screen run identity, exact normal termination, cache-free validation과 candidate ref | Immutable; phase-1 fan-in input |
| `PhaseOneChampionRef` | Phase 06/10 | Complete available-screen set digest, stable reduction과 selected champion | Phase-2 first-round warm start |
| `RoundPlanRef` | Phase 10 | Round ID, common warm start, stable declared worker assignments | Immutable; dispatch/fan-in authority |
| `WorkerAssignmentRef` | Phase 10 | Worker logical identity, seed/warm start/config/requested work fingerprint | Put-once; every retry가 동일 logical content |
| `WorkerAttemptOutcomeRef` | Phase 06/08 application handoff | Attempt identity, exact termination/work, candidate/report refs 또는 failure | Immutable observation, worker authority 아님 |
| `CommittedWorkerOutcomeRef` | Phase 08/09 | WorkerRun ID, accepted same-digest outcome, candidate-verifier PASS refs | CAS/put-once; fan-in input |
| `RoundChampionRef` | Phase 10 | Complete declared set digest, ordered candidates, comparator/tie decision, champion | Complete round 뒤 immutable |
| `FinalizationRequestRef` | Phase 10 | Terminal champion and exact Phase 07 contract inputs | Immutable; Phase 07 facade input |
| `Phase07OutputRef` | Phase 07/08 | `PublishableResult` 또는 typed rejection | Coordinator가 내용 재구성 금지 |
| `PublishableResultRef` | Phase 07/09 | Both verifier reports, payload digest와 envelope fingerprint | Publication CAS의 유일한 normal input |
| `CancellationIntent` | Phase 08/10 | Solve, intent identity, requested-at observation, safe actor/reason code | Idempotent intent; result identity와 분리 |
| `CoordinatorObservation` | Phase 10 evidence | Event/action/provider correlation, elapsed, duplicate/stale classification | 비권위 observation; semantic fingerprint 제외 |

Actual Phase 09는 Phase 08의 `ArtifactRef` shape를 보존해 `OpaqueLocator`를 운반할 수 있게 한다. 그러나 coordinator는 이를 parse, concatenate, compare, exact-key derivation 또는 semantic fingerprint 입력으로 사용하지 않고 동일 ref를 port에 다시 전달만 한다. Physical mapping은 object adapter 책임이다. Provider execution ID와 event body도 semantic identity가 아니다.

### 6.2 Logical identity

```java
package com.ronext.rpdptw.application.execution;

public record ExecutionRunId(
    SolveId solveId,
    ManifestFingerprint manifestFingerprint
) {}

public record RoundId(
    ExecutionRunId runId,
    int roundOrdinal
) {
    // phase-2 outer roundOrdinal >= 0
}

public sealed interface LogicalWorkRunId
        permits PhaseOneScreenRunId, WorkerRunId {}

public record PhaseOneScreenRunId(
    ExecutionRunId runId,
    PortfolioSlotId portfolioSlot,
    CandidateFingerprint inputCandidateFingerprint,
    ScreenConfigFingerprint screenConfigFingerprint
) implements LogicalWorkRunId {}

public record WorkerRunId(
    RoundId roundId,
    int workerOrdinal,
    CandidateFingerprint warmStartFingerprint,
    WorkerConfigFingerprint configFingerprint
) implements LogicalWorkRunId {}

public record AttemptId(
    LogicalWorkRunId logicalWorkRunId,
    long attemptOrdinal
) {
    // attemptOrdinal >= 1
}

public record ActionId(
    ExecutionRunId runId,
    long actionOrdinal,
    CoordinatorActionKind kind,
    ActionPayloadFingerprint payloadFingerprint
) {}
```

`ExecutionRunId`는 source의 `SolveId + ManifestFingerprint` identity를 이름으로 묶은 proposed value다. Provider workflow execution ID가 아니다. `PhaseOneScreenRunId`는 Phase 06의 declared phase kind/portfolio-slot/ALNS run lineage를 lossless application identity로 감싼다. Coordinator가 Phase 06 seed derivation이나 `AlnsRunIdentity`를 다시 계산한다는 뜻은 아니다.

`WorkerRunId` canonical projection:

```text
solveId
+ manifestFingerprint
+ roundOrdinal
+ workerOrdinal
+ warmStartFingerprint
+ workerConfigFingerprint
+ requestedWorkFingerprint
```

`AttemptId`와 provider metadata는 `WorkerRunId`/assignment fingerprint에 들어가지 않는다. `ActionId`는 state에 저장되어 resume 시 같은 값으로 다시 반환된다.

Phase-1과 phase-2 모두 retry에서 logical work identity/config/input/requested steps를 보존하고 `AttemptId`만 바꾼다. Phase-1 expected set은 Phase 05/06 manifest에서 `AVAILABLE`로 선언된 portfolio slot만 포함한다. Typed `UNAVAILABLE` slot은 missing screen이 아니며 coordinator가 availability를 재판정하지 않는다.

### 6.3 Lifecycle과 state/action commit

```text
read VersionedRunState(stateVersion)
→ readVerified exact manifest/artifact refs
→ normalize wake-up as non-authoritative observation
→ pure CoordinatorReducer decision
→ putIfAbsent immutable decision artifacts
→ compareAndSet(solveId, stateVersion, nextState + pendingAction)
   ├─ success: transition linearized; return pendingAction
   └─ conflict: no side effect authority; return ReloadRequired
```

외부 action 실행과 ack:

```text
pending ActionId A
→ executor exact-reads current VersionedRunState
→ require current pending action has A + same payload fingerprint
          + same authorizingTransitionOrdinal
→ provider/local adapter executes A idempotently
→ crash may occur before acknowledgement
→ next AdvanceSolve reads same pending A
→ execute/reconcile A again
   ├─ same logical effect + same digest: converge
   └─ same identity + different digest: FAILED(INTEGRITY_VIOLATION)
→ CAS consumes receipt and clears/replaces pending action
```

Crash window별 last safe point:

| Crash 위치 | Durable 사실 | Resume |
|---|---|---|
| State CAS 전 | Old state only | 같은 decision을 다시 계산 |
| Artifact put 뒤 state CAS 전 | Unreferenced immutable artifact 가능 | Same key/digest 수렴 후 CAS 재시도 |
| State CAS 뒤 action 반환 전 | New state + pending action | 같은 `ActionId` 재반환 |
| Dispatch/publish 뒤 ack 전 | External effect, pending action 유지 | Idempotent inspect/re-execute 후 reconcile |
| Ack artifact 뒤 state CAS 전 | Immutable receipt 존재 | Exact receipt read 후 CAS 재시도 |
| Terminal CAS 뒤 | Terminal state | Late event/action은 terminal no-op; state 후퇴 금지 |

Coordinator correctness는 coordinator process 생존, singleton 실행, lease ownership 또는 event exactly-once에 의존하지 않는다.
Opaque `StateVersion`은 CAS 호출의 equality precondition과 CAS result에만 존재한다. Reducer가 알 수 없는 post-CAS token을 action에 예측·내장하지 않으며, pre-CAS token을 action authorization이나 semantic fingerprint로 재사용하지 않는다. Action executor는 exact current state의 pending action identity를 다시 확인한 뒤에만 side effect를 수행한다.

## 7. Proposed Java 25 contracts와 dependency

### 7.1 Inbound use case와 command

```java
package com.ronext.rpdptw.application.port.in;

public interface AdvanceSolve {
    AdvanceSolveResult advance(AdvanceSolveCommand command);
}

public record AdvanceSolveCommand(
    TenantId tenantId,
    SolveId solveId,
    AdvanceCause cause,
    Optional<CoordinatorWakeUp> wakeUp
) {}

public sealed interface AdvanceCause
        permits AdvanceCause.Submitted,
                AdvanceCause.Resumed,
                AdvanceCause.ActionCompleted,
                AdvanceCause.WorkerHint,
                AdvanceCause.CancellationObserved,
                AdvanceCause.DeadlineCheck {
    record Submitted() implements AdvanceCause {}
    record Resumed() implements AdvanceCause {}
    record ActionCompleted(ActionReceiptRef receipt) implements AdvanceCause {}
    record WorkerHint(WorkerRunId workerRunId) implements AdvanceCause {}
    record CancellationObserved(CancellationIntentRef intent) implements AdvanceCause {}
    record DeadlineCheck() implements AdvanceCause {}
}
```

`CoordinatorWakeUp`은 source of truth가 아니다. Payload는 safe correlation과 expected logical identity만 갖고, candidate bytes/objective/state replacement를 포함하지 않는다.

```java
public sealed interface AdvanceSolveResult
        permits AdvanceSolveResult.ActionRequired,
                AdvanceSolveResult.Waiting,
                AdvanceSolveResult.Terminal,
                AdvanceSolveResult.ReloadRequired,
                AdvanceSolveResult.Rejected {

    record ActionRequired(CoordinatorAction action)
        implements AdvanceSolveResult {}

    record Waiting(WaitCondition condition, StateVersion stateVersion)
        implements AdvanceSolveResult {}

    record Terminal(SolveTerminalState state)
        implements AdvanceSolveResult {}

    record ReloadRequired(StateVersion observedVersion)
        implements AdvanceSolveResult {}

    record Rejected(CoordinatorFailure failure)
        implements AdvanceSolveResult {}
}
```

한 invocation의 CAS conflict는 무시하거나 last-write-wins하지 않는다. `ReloadRequired`를 반환하고 다음 invocation이 최신 state에서 다시 결정한다. Provider adapter가 자체 무한 loop를 만들 수 없으며 retry scheduling은 explicit platform policy와 observation에 남는다.

### 7.2 State hierarchy

```java
package com.ronext.rpdptw.application.coordinator;

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
    StateLineageFingerprint lineageFingerprint
) {
    // transitionOrdinal >= 0; opaque StateVersion은 VersionedRunState에 별도 존재
}

public sealed interface PreparationProgress
        permits SnapshotPreparation,
                PhaseOneDispatching,
                PhaseOneRunning,
                PhaseOneAggregating,
                PhaseOneComplete {}

public record Preparing(
    PreparationProgress progress
) implements SolveState {}

public record SnapshotPreparation(
    SolveSnapshotRef snapshot
) implements PreparationProgress {}

public record PhaseOneDispatching(
    PhaseOnePlanRef plan
) implements PreparationProgress {}

public record PhaseOneRunning(
    PhaseOnePlanRef plan,
    Set<PhaseOneScreenRunId> declaredScreens,
    Map<PhaseOneScreenRunId, PhaseOneScreenOutcomeRef> committedOutcomes
) implements PreparationProgress {}

public record PhaseOneAggregating(
    PhaseOnePlanRef plan,
    DeclaredCompletenessReport completeness,
    List<PhaseOneScreenOutcomeRef> outcomesInPortfolioOrder
) implements PreparationProgress {}

public record PhaseOneComplete(
    PhaseOneChampionRef champion
) implements PreparationProgress {}

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

상태 이름은 source의 추천안을 보존한다. Phase 08/09 accepted state type이 다른 이름을 쓰면 의미를 mapping하고 top-level 중복 state repository를 만들지 않는다.

```java
public sealed interface SolveTerminalState extends SolveState
        permits SolveSucceeded,
                SolveRejectedInput,
                SolveBindingFailed,
                SolveCancelled,
                SolveWatchdogReached,
                SolveResourceLimitReached,
                SolvePlatformTimeout,
                SolveFailed,
                SolveIncomplete,
                SolvePublicationRejected {}

public record SolveSucceeded(
    NormalTermination termination,
    PublishableResultRef result,
    PublicationReceiptRef publication
) implements SolveTerminalState {}
```

`SolveSucceeded.termination`은 coordinator가 만든 `NO_STRICT_IMPROVEMENT` 또는 `MAX_ROUNDS_REACHED`다. Worker의 `MAX_STEPS_REACHED`를 solve termination과 혼합하지 않는다.

### 7.3 Provider-neutral action

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

public record DispatchPhaseOneScreens(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    PhaseOnePlanRef plan,
    List<PhaseOneScreenAssignmentRef> assignmentsInPortfolioOrder
) implements CoordinatorAction {}

public record WaitForPhaseOneScreens(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    Set<PhaseOneScreenRunId> missingScreens
) implements CoordinatorAction {}

public record DispatchWorkers(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    RoundId roundId,
    List<WorkerAssignmentRef> assignmentsInWorkerOrdinalOrder
) implements CoordinatorAction {}

public record WaitForWorkers(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    RoundId roundId,
    Set<WorkerRunId> missingWorkers
) implements CoordinatorAction {}

public record RequestWorkerStops(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    List<WorkerRunId> inFlightWorkers,
    StopReason reason
) implements CoordinatorAction {}

public record FinalizeResult(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    FinalizationRequestRef request
) implements CoordinatorAction {}

public record PublishResult(
    ActionId actionId,
    long authorizingTransitionOrdinal,
    PublicationPrecondition expectedPublication,
    PublishableResultRef result
) implements CoordinatorAction {}
```

`authorizingTransitionOrdinal`은 CAS 성공 시 저장되는 `RunState.transitionOrdinal`과 같은 semantic ordinal이다. 이는 stale action을 단독으로 승인하는 token이 아니다. Executor는 exact-read한 current pending action의 `ActionId`, payload fingerprint와 ordinal을 모두 대조한다. `PublicationPrecondition`은 Phase 09 publication pointer 전용 opaque precondition projection이며 run-state `StateVersion`과 호환·대체할 수 없다. Exact type/read method는 Phase 08/09/10 owner review 전 proposed blocker다.

Action에 S3 key, ARN, Lambda payload/context, Step Functions state, GCP URI, Kubernetes object 또는 provider repository version token을 넣지 않는다. Phase 11은 이 action을 AWS command/wait/wakeup에 매핑하지만 action 의미를 변경하지 않는다.

### 7.4 Outbound ports

Phase 10은 actual Phase 08/09가 제안한 계약을 다음 shape로 소비한다. Phase 10 action은 immutable assignment ref를 보존하지만 executor는 Phase 09 `readVerified` 뒤 allow-listed codec으로 Phase 08 `WorkerAssignment` value를 복원해 dispatcher에 넘긴다.

```java
public interface WorkerDispatcher {
    DispatchReceipt dispatch(WorkerAssignment assignment);
    WorkerExecutionStatus getStatus(WorkerRunId workerRunId);
    StopReceipt requestStop(
        WorkerRunId workerRunId,
        CancellationId cancellationId
    );
}

public interface CancellationPort {
    CancellationWriteResult recordIfAbsent(CancellationIntent intent);
    Optional<CancellationIntent> find(SolveId solveId);
}

public interface MonotonicClock {
    MonotonicTick now();
}

public interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
        ArtifactKey key,
        ArtifactContent content,
        ContentDigest expectedDigest
    );

    ReadableArtifact readVerified(ArtifactRef reference);
    ArtifactMetadata metadata(ArtifactRef reference);
}

public interface RunArtifactRepository {
    ReadableArtifact readDeclared(
        ArtifactRef executionManifestRef,
        DeclaredArtifactIdentity identity
    );
}

public interface RunStateRepository {
    CreateStateResult createIfAbsent(
        SolveId solveId,
        RunState initialState
    );

    VersionedRunState get(SolveId solveId);

    StateUpdateResult compareAndSet(
        SolveId solveId,
        StateVersion expectedVersion,
        RunState nextState
    );
}

public interface ResultPublisher {
    VersionedPublishedResult get(SolveId solveId);

    PublicationResult compareAndSet(
        SolveId solveId,
        PublicationPrecondition expectedPublication,
        PublishableResultRef result
    );
}
```

`ResultPublisher.get`과 `PublicationPrecondition`은 response-loss reconciliation과 distinct publication-pointer CAS precondition을 설명하기 위한 proposed refinement다. Phase 08/09 baseline signature를 Phase 10이 단독 승인하거나 구현하지 않는다. Accepted cross-phase signature가 정해질 때까지 WP-10.6과 Phase 10 exit는 차단된다.

Application `StateVersion`은 underlying Phase 09 run-state repository version의 opaque equality projection이다. 정렬, 숫자 증가, timestamp 비교 또는 provider ETag parsing을 하지 않는다. `RunState.transitionOrdinal`은 semantic lineage field이고 CAS authority가 아니다. `RunStateRepository`는 approved canonical codec/sealed state만 받아 arbitrary Java serialization과 `Map<String,Object>`를 금지한다.

Actual Phase 08의 `RunDeadline(MonotonicTick startedAt, WatchdogBudget explicitBudget)`를 사용하며 coordinator가 wall-clock `Instant`나 provider remaining-time을 semantic deadline으로 만들지 않는다. Phase 08/09 signature를 바꾸려면 양쪽 owner와 Phase 10/11 cross-phase review가 필요하다.

### 7.5 Finalization authority

```java
public interface FinalResultGateway {
    Phase07Output finalizeResult(Phase07Request request);
}

public sealed interface Phase07Output
        permits Phase07Output.Publishable,
                Phase07Output.Rejected {

    record Publishable(PublishableResult result)
        implements Phase07Output {}

    record Rejected(FinalResultRejection rejection)
        implements Phase07Output {}
}
```

이 signature는 actual Phase 07의 proposed facade 의미를 소비하는 application-side gateway다. Coordinator의 권한:

- Complete verified champion의 exact ref로 finalization request를 만든다.
- Phase 07 output identity/digest를 immutable artifact로 보존한다.
- `Publishable`일 때만 `PublishResult`를 만든다.
- `Rejected`를 normal result로 바꾸지 않고 `FAILED` 또는 `PUBLICATION_REJECTED`의 typed 원인을 보존한다.

Coordinator가 할 수 없는 일:

- Candidate verifier를 생략하거나 `verified=true` boolean을 합성
- Final outcomes/audit/summary/payload 생성·수정
- Candidate `PASS`를 result `PASS`로 간주
- Exceptional candidate를 normal/official result로 rename
- Result rejection 뒤 fallback candidate 자동 선택 또는 re-solve

### 7.6 Dependency direction

```text
apps/coordinator or provider adapter
  → rpdptw-application coordinator/use case
    → Phase 08/09 port interfaces
    → core comparator contract
    → Phase 06 worker request/result projection
    → Phase 07 verification/finalization facade

rpdptw-application MUST NOT
  → AWS/GCP/Azure/Kubernetes SDK or event DTO
  → object-storage backend implementation
  → solver internal COW/search/cache/operator package
  → verification internal scratch/cache/builder
  → customer profile implementation or customer-name branch
  → optimizer vendor/license/native API
```

Coordinator가 comparator interface를 호출할 수는 있지만 objective vector를 `double` 하나로 축약하거나 고객별 비교를 구현하지 않는다.

## 8. State transition, concurrency와 pseudocode

### 8.1 Legal transition table

| Current | Required authority | Next/action | Illegal shortcut |
|---|---|---|---|
| `SUBMITTED` | Exact submission/manifest ref | `PREPARING` | Raw provider payload에서 worker plan 생성 |
| `PREPARING` | Phase 08 prepared snapshot + all declared available phase-1 screens | Phase-1 dispatch/wait/aggregate 뒤 `PREPARED(PhaseOneChampionRef)` | Missing screen/fingerprint/default로 계속 |
| `PREPARED` | Phase-1/first round plan | `ROUND_DISPATCHING` + dispatch | Listing으로 assignment 생성 |
| `ROUND_DISPATCHING` | All immutable assignment refs | `ROUND_RUNNING` | 일부 assignment만 state commit |
| `ROUND_RUNNING` | Exact declared outcomes | `WaitForWorkers` 또는 `ROUND_VERIFYING` | 보이는 success만 fan-in |
| `ROUND_VERIFYING` | Every accepted candidate `PASS` | `ROUND_AGGREGATING` | Solver flag/summary를 `PASS`로 대체 |
| `ROUND_AGGREGATING` | Complete ordered set + comparator | Next round 또는 `FINALIZING` | Completion-order first winner |
| `FINALIZING` | Exact final champion + Phase 07 output | `PUBLISHING` 또는 typed failure | Coordinator가 result 생성 |
| `PUBLISHING` | `PublishableResultRef` + publication CAS | `SUCCEEDED` 또는 `PUBLICATION_REJECTED` | Unconditional overwrite |
| Any state before publication fence | Durable cancellation intent exact-read + same run-state CAS | `CANCEL_REQUESTED` + stop | Intent object만으로 terminal authority 주장 |
| `CANCEL_REQUESTED` | Actual stop/terminal observations | `CANCELLED` | Intent만으로 actual termination 주장 |
| Any nonterminal | Deadline/safety observation | Typed exceptional terminal/stop | `MAX_*` 정상 종료로 rename |
| Any terminal | Same command/event | Same terminal/no-op | State 후퇴 또는 재publication |

`REJECTED_INPUT`, `BINDING_FAILED`는 search 시작 전 terminal이다. `BACKEND_UNAVAILABLE`, `LICENSE_UNAVAILABLE`, `ROUTE_SELECTION_FAILED`는 Phase 13 future manifest가 허용할 때도 worker/phase typed outcome이며 baseline Phase 10이 정상 ALNS-only success로 재해석하지 않는다.

Actual Phase 09는 run-state pointer와 published-result pointer를 별도 one-key CAS로 둔다. Multi-object transaction 없이 cancellation/publication이 서로 다른 terminal meaning을 만들지 않도록 **run-state `PUBLISHING` CAS를 publication authorization fence**로 사용한다.

```text
FINALIZING + PublishableResultRef
  ├─ same run-state version에서 CANCEL_REQUESTED CAS wins first
  │    → CANCEL_REQUESTED; publication action never authorized
  └─ PUBLISHING + pending PublishResult CAS wins first
       → terminal intent is publication; later cancel is TOO_LATE/no-op
       → ResultPublisher CAS
       → response loss: accepted publication read contract exact desired ref and reconcile
       → run-state SUCCEEDED or PUBLICATION_REJECTED CAS
```

Cancellation intent object와 run-state pointer는 서로 다른 key이므로 두 write 사이의 “먼저”를 business linearization으로 사용하지 않는다. Intent는 exact-read하는 durable request이고, cancel/publish 우선순위는 동일 run-state expected version에 대한 `CANCEL_REQUESTED` 대 `PUBLISHING` CAS 중 하나의 성공으로만 정한다.

`PUBLISHING`은 normal success가 아니며 retrieval도 아직 금지한다. 다만 irreversible terminal-intent fence 뒤에는 cancellation으로 전환하지 않는다. 이 protocol은 actual Phase 08의 “한 terminal meaning”과 Phase 09의 별도 pointer 계약을 함께 만족시키기 위한 proposed cross-phase decision이며 Phase 08/09/10 review에서 승인되어야 한다.

### 8.2 One-transition coordinator pseudocode

```text
advance(command):
  authorize(command.tenantId, command.solveId)
  versioned = stateRepository.get(command.solveId)
  state = versioned.state
  stateVersion = versioned.stateVersion

  if state is terminal:
      classify command/wakeup as duplicate-or-late
      return Terminal(state)

  manifest = runArtifacts.readDeclared(
      state.manifestRef, declaredIdentity(EXECUTION_MANIFEST))
  require intrinsic(manifest) == state.runId.manifestFingerprint
  require tenant/snapshot/build/config identity equality

  wakeup = validateCorrelationOnly(command.wakeUp)

  cancellation = cancellationPort.readIntent(command.solveId)
  now = monotonicClock.now()

  decision = reducer.decide(
      state,
      manifest,
      exactReferencedArtifacts(state),
      cancellation,
      deadlineObservation(manifest, now),
      wakeup
  )

  for artifact in decision.immutableArtifacts:
      artifactStore.putIfAbsent(
          artifact.key, artifact.content, artifact.expectedDigest)
      artifactStore.readVerified(artifact.ref)

  next = decision.nextState.withPendingAction(decision.action)
  cas = stateRepository.compareAndSet(
      command.solveId,
      stateVersion,
      next
  )

  if cas.conflict:
      return ReloadRequired(cas.observedStateVersion)

  return decision.toAdvanceResult()
```

Reducer는 port를 호출하지 않는 pure function이다. CAS 뒤 action execution은 inbound adapter/application action executor가 수행하며 same `ActionId`로 재실행 가능해야 한다.

### 8.3 Phase-1 declared fan-out/fan-in

Phase 05/06 handoff가 선언한 최대 8개 portfolio slot을 그대로 사용한다.

```text
expectedScreens =
  phaseOnePlan.portfolioSlots in canonical 4×2 order
  where availability == AVAILABLE

typed UNAVAILABLE slot
  → evidence identity를 보존
  → missing screen으로 계산하지 않음
  → coordinator가 다른 policy/candidate로 대체하지 않음

for every expected screen:
  exact PhaseOneScreenAssignmentRef
  exact explicit screenMaxSteps
  Phase 06 declared seed/config/portfolio lineage
  normal MAX_STEPS_REACHED
  cache-free validation equality

all expected screens complete
  → stable upstream comparator + declared tie policy
  → PhaseOneChampionRef
  → first phase-2 RoundPlan common warm start
```

Available screen 하나라도 failed/cancelled/watchdog/platform/integrity/unverified이면 다른 screen만으로 champion을 만들지 않는다. Retry/wait가 남으면 `PhaseOneRunning`; 승인된 진행 경로가 없으면 solve `INCOMPLETE`다. Phase 10은 screen ALNS, seed derivation, cache-free validation과 portfolio availability를 재구현하지 않는다.

### 8.4 Declared round completeness와 champion

```text
expected = roundPlan.assignments
              .stableSortBy(workerOrdinal)
              .map(workerRunId)

observed = exact-read committed outcome for each expected workerRunId

if any expected ref missing and retry/wait remains:
    WaitForWorkers(missing)

if any expected ref missing and no approved progress remains:
    INCOMPLETE(MISSING_DECLARED_WORKER)

for each outcome in expected order:
    require outcome.assignmentFingerprint == expected assignment
    require outcome.normalTermination == MAX_STEPS_REACHED
    require outcome.completedSteps == outcome.requestedSteps
    require candidateVerifierReport == PASS
    require verifiedSolutionRef exact identity

require observed worker identity set == expected

champion = stable reduction(
    upstream BoundComparator,
    manifest.tiePolicy,
    verified candidates in workerOrdinal order
)
```

Extra/unrecognized outcome은 expected set에 추가하지 않는다. 같은 solve/round처럼 보이더라도 다른 manifest/assignment fingerprint면 integrity incident다. Unknown extra provider event만으로 solve를 실패시키지 않고 safe telemetry로 격리하되, expected logical identity를 사칭한 different digest는 `FAILED(INTEGRITY_VIOLATION)`다.

Round transition:

```text
comparison = comparator.compare(roundChampion, previousChampion)

STRICTLY_BETTER and another declared round exists
  → persist RoundChampionRef
  → next RoundPlan with exact common warm start
  → ROUND_DISPATCHING

EQUAL or WORSE
  → normal termination NO_STRICT_IMPROVEMENT
  → FINALIZING(previousChampion)

STRICTLY_BETTER and configured maxRounds reached
  → normal termination MAX_ROUNDS_REACHED
  → FINALIZING(roundChampion)
```

Tie-break는 같은 round 안의 worker candidate에서 안정된 단일 round champion을 고르는 재현성 규칙이며, `EQUAL` result가 previous champion을 대체하게 하는 adoption 규칙이 아니다. Exact stable tie policy ID/version이 manifest에 없으면 binding/manifest error다.

### 8.5 Duplicate, out-of-order, late와 stale event

| Event/observation | 처리 |
|---|---|
| Same `ActionId`, same receipt digest | Idempotent convergence |
| Same `WorkerRunId`, same verified success digest | Idempotent convergence |
| Same `WorkerRunId`, different verified success digest | `FAILED(INTEGRITY_VIOLATION)` |
| Recognized old attempt success before logical worker commit | Exact assignment/verification을 통과하면 attempt order와 무관하게 수렴 가능 |
| Old attempt success after same-digest commit | No-op duplicate |
| Old attempt success after different-digest commit | Integrity failure |
| Future round event before current round commit | Stale/out-of-order hint; exact current state 유지 |
| Previous round event during later round | Late no-op; previous state 재개 금지 |
| Worker event after cancel/deadline/terminal | Late observation only; publication/next round 금지 |
| Event lost entirely | Resume/poll이 exact declared refs를 읽어 진행 |
| Event tenant/manifest mismatch | Reject before artifact deserialization; security/integrity telemetry |
| Tampered event payload objective/result | Payload를 authority로 사용하지 않고 exact ref 재독해 |

### 8.6 Cancellation과 deadline

Cancellation race는 single state CAS로 linearize한다.

```text
RequestCancellation writes idempotent intent
→ AdvanceSolve observes intent
→ CAS CANCEL_REQUESTED with stop action
→ requestStop for every in-flight WorkerRunId
→ record actual worker termination separately
→ all required stop/terminal observations
→ CAS CANCELLED
```

Run-state `PUBLISHING` fence CAS가 먼저 이기면 뒤늦은 cancel은 `TOO_LATE` no-op이고, result pointer CAS/response-loss reconciliation을 계속한다. 같은 expected run-state version에서 `CANCEL_REQUESTED` CAS가 먼저 이기면 publication action은 생성되지 않는다. Intent record만 먼저 저장된 경우에는 아직 terminal authority가 아니며 coordinator가 exact intent를 읽어 same run-state CAS 경쟁에 참여해야 한다. 이전 pending `ActionId`/ordinal을 가진 executor는 current state 대조에서 stale로 거부된다. Result pointer만 성공하고 run-state terminal CAS 전에 crash한 경우 resume은 accepted publication read contract로 exact desired ref를 확인해 `SUCCEEDED`로 수렴하며 cancel로 의미를 바꾸지 않는다.

Deadline/watchdog:

- Deadline policy와 value는 manifest/runtime contract에 explicit해야 한다. Official value는 `Q-BENCH-02` 승인 전 없다.
- Test는 virtual/fake `Clock`을 사용하며 real sleep을 사용하지 않는다.
- Deadline은 quality comparator, seed, tie, step budget 또는 result fingerprint 입력이 아니다.
- Deadline 전에는 partial round가 `ROUND_RUNNING/WaitForWorkers`일 수 있다.
- Deadline 경계가 도달하면 미완료 work를 `NO_STRICT_IMPROVEMENT`나 `MAX_ROUNDS_REACHED`로 바꾸지 않는다.
- Application이 deadline을 관측하고 safe stop transition을 commit하면 `WATCHDOG_REACHED`; provider가 record/stop 경계를 완성하지 못한 timeout은 `PLATFORM_TIMEOUT`이다.
- Late worker success는 recovery observation일 수 있으나 해당 terminal solve의 normal publication을 재개하지 않는다.

Actual Phase 08이 제안한 `RunDeadline(MonotonicTick startedAt, WatchdogBudget explicitBudget)`와 `MonotonicClock`을 소비한다. Tick/budget의 durable encoding, process restart 간 monotonic origin reconciliation과 platform reserve는 Phase 08/10/11 ADR review 전 **미확정 API/운영 수치**다. Process-local monotonic tick을 crash 뒤 그대로 비교하거나 wall-clock/provider remaining-time fallback으로 보완하지 않는다. 이 계약이 승인되기 전 deadline-enabled crash-resume 구현과 Phase 10 exit는 차단된다. 어떤 선택도 normal quality termination 의미를 바꿀 수 없다.

### 8.7 Failure taxonomy

Source의 정상/예외 분리를 그대로 보존한다.

| State/termination | 정확한 의미 | Normal result 가능한가 |
|---|---|---:|
| `MAX_STEPS_REACHED` | Worker가 exact requested completed-step budget과 handoff를 완료 | Worker candidate 자격만 가능 |
| `NO_STRICT_IMPROVEMENT` | Complete verified round champion이 이전 champion보다 strictly better 아님 | Finalization/both-gate 뒤 가능 |
| `MAX_ROUNDS_REACHED` | Configured 모든 round를 complete verified batch로 완료 | Finalization/both-gate 뒤 가능 |
| `REJECTED_INPUT` | Schema/reference/input authority 시작 전 거절 | 아니오 |
| `BINDING_FAILED` | Profile/config/dependency binding 실패 | 아니오 |
| `CANCEL_REQUESTED` | Durable intent를 관측한 same run-state CAS가 cancellation fence를 commit했으나 실제 종료 미완료 | 아니오 |
| `CANCELLED` | Intent와 actual safe termination이 구분·기록됨 | 정상 success 아님 |
| `WATCHDOG_REACHED` | Application safety watchdog이 미완료 실행을 중단 | 정상/official 아님 |
| `RESOURCE_LIMIT_REACHED` | 명시적 안전 자원 한계 | 정상/official 아님 |
| `PLATFORM_TIMEOUT` | 상위 runtime이 algorithm/application record 완성을 막음 | 정상/official 아님 |
| `FAILED` | Execution/implementation/integrity/verifier defect | 아니오 |
| `INCOMPLETE` | Declared work/verification을 끝내지 못했고 승인된 진행 경로 없음 | 아니오 |
| `PUBLICATION_REJECTED` | Both-gate/identity/publication CAS 계약 미충족 | 아니오 |

`FAILED`의 stable safe failure code는 최소 `INTEGRITY_VIOLATION`, `ILLEGAL_TRANSITION`, `MANIFEST_MISMATCH`, `ARTIFACT_CORRUPT`, `VERIFIER_REJECTED`, `FINALIZATION_FAILED`를 구분할 수 있다. 이는 새 정상 state가 아니라 source의 `FAILED` 아래 조사 가능한 typed 원인이다. Raw provider exception/stack trace/PII를 public failure에 넣지 않는다.

## 9. Exact test fixtures, builders와 oracles

모든 아래 test는 **future red**다. 현재 target module/type이 없으므로 실행되거나 통과했다고 주장하지 않는다.

### 9.1 Fixture와 builder

| Fixture/builder | 역할 | 금지 |
|---|---|---|
| `CoordinatorScenarioBuilder` | Manifest, state, round, assignment, outcome, action/receipt를 immutable 생성 | Production reducer/serializer로 expected 생성 |
| `TestOnlyExecutionManifestBuilder` | Explicit positive screen/worker/round/retry/deadline 값과 `TEST_ONLY` authority | Omitted default, official label |
| `DeclaredRoundBuilder` | Stable worker ordinals와 exact assignment refs | Thread/completion/list order에서 worker set 추론 |
| `VerifiedWorkerOutcomeBuilder` | Phase 06 termination + Phase 07 candidate PASS refs | Boolean verified, raw solver objective |
| `TamperedArtifactBuilder` | 한 field/digest/schema/tenant/fingerprint만 변조 | Production validator를 expected oracle로 사용 |
| `InMemoryCasStateRepository` | Versioned CAS success/conflict와 state history | Synchronized singleton을 correctness proof로 사용 |
| `ScriptedWorkerDispatcher` | Dispatch/status/stop log, duplicates, lost/out-of-order completion | Real thread timing/flaky sleep |
| `ScriptedFinalResultGateway` | Exact `Publishable`/`Rejected` 반환 | Verifier PASS 합성 evidence |
| `MutableTestClock` | Deadline 직전/경계/직후 deterministic observation | Wall clock와 sleep |
| `CrashPointHarness` | Artifact/CAS/action/ack 각 boundary에서 process loss simulation | In-memory state만 보고 durability 주장 |

Test-only 값은 작은 양수일 수 있지만 fixture ID, authority와 manifest fingerprint에 포함한다. Production source/README/API fallback에 복사하지 않는다.

### 9.2 Independent model/state-transition oracle

`IndependentCoordinatorModel`은 production `CoordinatorReducer`, `RunState`, canonical encoder, CAS repository와 helper를 import하지 않는다. 단순 immutable mathematical state와 explicit set/list 연산으로 expected legal transitions를 계산한다.

Model input alphabet:

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
FINALIZE_PUBLISHABLE | FINALIZE_REJECTED
PUBLISH_OK | PUBLISH_CONFLICT_SAME | PUBLISH_CONFLICT_DIFFERENT
CRASH(point)
RESUME
STALE_EVENT(identity)
TAMPER(field)
CONCURRENT_ADVANCE(count)
```

Model oracle가 매 transition 뒤 검사할 invariant:

```text
transitionOrdinal increases by exactly one only on successful authoritative CAS
stateVersion is compared only as an opaque equality token
opaque repository version is absent from semantic action identity/payload
terminal state never transitions
committed outcomes keys subset of declared workers
round complete iff every declared worker has one accepted verified digest
next round warm start changes only after STRICTLY_BETTER complete champion
EQUAL/WORSE finalization retains the previous champion
pending action identity stable across crash/resume
current pending ActionId/payload/transition exact-read authorizes execution
CANCEL_REQUESTED run-state/deadline fence forbids new dispatch/finalize/publish
published result exists iff both-gate ref and publication receipt exist
same event sequence modulo duplicate/order/crash yields same semantic terminal state
```

Property runner는 generated sequence seed, minimized failing sequence, manifest fingerprint와 model/production state diff를 evidence에 보존한다.

### 9.3 Exact test class/method matrix

| Test class | Exact future method | Fixture/oracle | Expected |
|---|---|---|---|
| `SolveCoordinatorStateMachineTest` | `submittedAdvancesOnlyToPreparing()` | Hand transition table | Legal next state/action |
|  | `illegalBackwardTransitionFailsClosed()` | Hand table | `FAILED(ILLEGAL_TRANSITION)` |
|  | `terminalStateIgnoresLateWakeUp()` | Terminal fixture | Same state/no action |
|  | `oneAdvancePerformsAtMostOneAuthoritativeCas()` | CAS spy | CAS count `<=1` |
| `PhaseOneFanInTest` | `requiresEveryDeclaredAvailableScreenBeforeChampion()` | Portfolio set oracle | Partial screen fan-in blocked |
|  | `typedUnavailableSlotIsNotMissingScreen()` | 4×2 availability fixture | Expected set exact |
|  | `failedScreenCannotBeHiddenBySuccessfulScreens()` | Phase 06 outcome fake | Champion 0 |
|  | `screenCompletionPermutationsSelectSameChampion()` | Permutation oracle | Exact champion/lineage |
| `DeclaredRoundCompletenessTest` | `missingDeclaredWorkerKeepsRoundOpenBeforeDeadline()` | Declared set oracle | `WaitForWorkers` |
|  | `missingDeclaredWorkerBecomesIncompleteWhenNoProgressRemains()` | Hand oracle | `INCOMPLETE` |
|  | `failedWorkerCannotBeHiddenBySuccessfulWorkers()` | Set oracle | No champion |
|  | `unverifiedWorkerCannotEnterChampionReduction()` | Phase 07 fake | No champion |
|  | `extraOutcomeNeverExpandsDeclaredSet()` | Set oracle | Expected set unchanged |
| `CoordinatorIdempotencyConcurrencyTest` | `duplicateDispatchReturnsSameActionIdentity()` | Action log | Same action/ref |
|  | `actionContainsNoOpaqueRepositoryVersion()` | Distinct run/publication token fixture | Semantic payload token leakage 0 |
|  | `executorRequiresExactCurrentPendingActionAndOrdinal()` | Stale/current state fixture | Stale side effect 0 |
|  | `sameWorkerSameDigestConverges()` | Digest oracle | One committed outcome |
|  | `sameWorkerDifferentDigestFailsIntegrity()` | Digest oracle | `FAILED` |
|  | `twoCoordinatorsRaceAndExactlyOneCasWins()` | Barrier-free CAS fake | One transition commit; one opaque CAS winner |
|  | `losingCoordinatorReloadsWithoutSideEffect()` | CAS/action spy | `ReloadRequired` |
| `CoordinatorCrashResumeTest` | `crashBeforeCasLeavesNoAuthoritativeTransition()` | Crash harness | Old state |
|  | `crashAfterCasReemitsPendingAction()` | Crash harness | Same `ActionId` |
|  | `crashAfterDispatchBeforeAckReconciles()` | Dispatcher fake | One logical worker |
|  | `crashAfterArtifactBeforePointerUsesSameDigest()` | Artifact fake | Idempotent put |
|  | `repeatedCrashesConvergeToSameTerminalFingerprint()` | Model oracle | Exact equality |
| `CoordinatorEventOrderingTest` | `outOfOrderFutureRoundEventIsNonAuthoritative()` | Permutation oracle | No state change |
|  | `latePreviousRoundSuccessCannotReplaceChampion()` | Late-event fixture | Champion unchanged |
|  | `lostWakeUpStillProgressesByExactRead()` | No-event fixture | Same final state |
|  | `allCompletionPermutationsSelectSameChampion()` | Permutation oracle | Exact champion/lineage |
|  | `staleAttemptSameDigestConvergesAfterRetry()` | Attempt fixture | No conflict |
| `MultiRoundChampionTest` | `strictlyBetterChampionBecomesCommonNextWarmStart()` | Manual comparator | Next round refs exact |
|  | `equalChampionTerminatesAndRetainsPreviousChampion()` | Manual comparator | Normal termination + previous ref exact |
|  | `worseChampionTerminatesNoStrictImprovement()` | Manual comparator | Previous champion retained |
|  | `lastStrictImprovementTerminatesMaxRoundsReached()` | Round plan oracle | Normal termination |
|  | `tiePolicyIsStableButNotQualityDimension()` | Comparator/tie oracle | Same objective, stable ref |
| `CoordinatorCancellationDeadlineTest` | `duplicateCancellationIntentConverges()` | Cancel fake | One intent/action |
|  | `cancelIntentAndActualTerminationRemainDistinct()` | Stop observations | Intermediate state visible |
|  | `intentRecordAloneIsRequestNotTerminalFence()` | Two-key fixture | No false authority |
|  | `cancelRequestedRunStateCasWinningFencesPublication()` | Same-version CAS race | No publication |
|  | `publicationFenceWinningCasMakesLaterCancelTooLate()` | CAS race | Publication intent preserved |
|  | `deadlineBeforeBoundaryDoesNotStopRound()` | Mutable clock | Still running |
|  | `deadlineAtBoundaryIsWatchdogNotQualityTermination()` | Mutable clock | `WATCHDOG_REACHED` |
|  | `platformTimeoutIsNeverMaxStepsReached()` | Platform fixture | `PLATFORM_TIMEOUT` |
|  | `lateSuccessAfterDeadlineCannotPublish()` | Late outcome | Terminal unchanged |
| `CoordinatorIntegrityTest` | `tamperedManifestIntrinsicFingerprintFailsClosed()` | Tampered builder | `MANIFEST_MISMATCH` |
|  | `sameManifestIdentityDifferentBytesFailsClosed()` | Artifact fake | Integrity failure |
|  | `crossTenantArtifactReferenceIsRejected()` | Tenant fixture | No read/dispatch |
|  | `workerAssignmentFingerprintMismatchIsRejected()` | One-field tamper | No fan-in |
|  | `resultPayloadEventCannotBypassExactArtifactRead()` | Poison event | Exact ref wins |
| `CoordinatorFinalizationPublicationTest` | `onlyCompleteVerifiedChampionReachesFinalization()` | Phase 07 spy | Exact call count 1 |
|  | `phase07RejectedNeverCreatesPublishAction()` | Rejected fake | Typed failure |
|  | `candidatePassWithoutResultPassCannotPublish()` | Partial Phase 07 fixture | Publication 0 |
|  | `runStateVersionCannotServeAsPublicationPrecondition()` | Distinct token types | Compile/contract rejection |
|  | `sameResultDigestPublicationConflictConverges()` | Publisher fake | `SUCCEEDED` |
|  | `differentResultDigestPublicationConflictRejects()` | Publisher fake | `PUBLICATION_REJECTED` |
|  | `crashAfterResultPointerBeforeSucceededReconciles()` | Publisher/read fake | Exact `SUCCEEDED` |
| `CoordinatorReplayTest` | `sameManifestWithDifferentSchedulesHasSameLineage()` | Replay oracle | Exact state/champion/result |
|  | `attemptIdChangesDoNotChangeSeedWarmStartOrWork()` | Assignment oracle | Exact equality |
|  | `providerMetadataNeverChangesSemanticFingerprint()` | Metadata permutations | Exact equality |
| `CoordinatorModelBasedPropertyTest` | `generatedLegalSequencesMatchIndependentModel()` | Independent model | State/action equality |
|  | `duplicatesOrderingCrashesPreserveConfluence()` | Metamorphic oracle | Same terminal fingerprint |
|  | `generatedIllegalSequencesNeverPublish()` | Independent model | Publication count 0 |
| `Phase10CoordinatorArchitectureTest` | `applicationHasNoProviderSdkProviderStateOrEventDtoDependency()` | Bytecode/dependency scan | AWS/GCP/Azure/Kubernetes SDK/state/event refs 0 |
|  | `coordinatorHasNoSolverInternalOrVerifierInternalDependency()` | Architecture graph | Forbidden edges 0 |
|  | `coordinatorHasNoListingLockLeaseOrLastWriteWinsPath()` | Static/contract rule | Forbidden semantics 0 |
|  | `missingSemanticConfigCannotSelectProductionDefault()` | Assembly/config negative fixture | Startup/binding rejection |

### 9.4 Red → green와 test layer

1. Identity/manifest constructor와 illegal transition test를 red로 고정한다.
2. Pure transition table와 independent model을 만든다.
3. CAS/pending action과 crash boundary를 red→green 한다.
4. Dispatch/retry/duplicate/out-of-order/declared completeness를 green 한다.
5. Comparator invocation, strict improvement와 multi-round termination을 green 한다.
6. Cancellation/deadline/platform failure fence를 green 한다.
7. Phase 07 finalization과 Phase 09 publication contract를 green 한다.
8. Generated model/property/replay, architecture와 local integration을 마지막으로 green 한다.

| Layer | 필수 test | Exit evidence 역할 |
|---|---|---|
| Value/unit | Identity, manifest, state/action constructors | Invalid value/default/tamper 거부 |
| Pure model | State table, completeness, champion, termination | Semantic oracle |
| Property/model-based | Generated sequences와 metamorphic reorder/duplicate/crash | Transition completeness/confluence |
| Port contract | Artifact/state/dispatch/cancel/publication fakes | Provider-neutral semantics |
| Fault/corruption | CAS race, crash, tamper, missing worker | Fail-closed/last safe point |
| Integration | Local Phase 08/09 + Phase 06/07 doubles/accepted contracts | End-to-end orchestration |
| Architecture | Dependency/bytecode/static rule | Provider/solver/storage leakage 0 |
| Provider | 없음 — Phase 11 future red | Phase 10 completion에 AWS evidence를 위장하지 않음 |

## 10. Ordered work packages

### WP-10.0 — Entry receipt, authority와 cross-phase contract freeze

- **Prerequisite:** Scheduler task/owners 지정, Phase 06~09 accepted review/evidence와 exact artifact signatures.
- **Change target:** Phase 10 contract map, manifest/identity/failure inclusion-exclusion, test-only fixture authority.
- **Concrete tasks:** Upstream fingerprint equality, Phase 08/09 port ownership, Phase 06 worker/termination, Phase 07 finalization/both-gate projection을 대조하고 missing/open/gated 값을 확정하지 않은 채 기록한다.
- **Verification commands:**

  ```bash
  test -s docs/implementation/phases/phase-06-cow-alns-reproducibility.md
  test -s docs/implementation/phases/phase-07-independent-verification-final-result.md
  test -s docs/implementation/phases/phase-08-application-ports-local-runtime.md
  test -s docs/implementation/phases/phase-09-object-storage-no-database.md
  test -s docs/implementation/phases/phase-10-provider-neutral-coordinator.md
  ```

- **Expected tests/evidence:** Actual accepted predecessor refs와 artifact/evidence identity, 인용한 stable section의 semantic compatibility, 인접/reciprocal acceptance hash 0, scheduler/owner identity, explicit config status 표. Final inspection에서 다섯 상세 파일 existence command는 성공하지만 Phase 06~09가 모두 unaccepted이므로 entry는 계속 BLOCKED다.
- **Failure/rollback:** Stub port, fake `PASS`, copied legacy GCP controller 또는 hidden 수치로 진행하지 않는다. Last safe point는 이 detailed document와 red test matrix다.
- **Handoff:** Frozen identity/state/action/failure mapping을 WP-10.1에 전달한다.

### WP-10.1 — Pure state model, identity와 legal transition

- **Prerequisite:** WP-10.0 green, accepted Phase 08 state lifecycle.
- **Change target:** `execution` identity/manifest, `coordinator` state/event/action/decision/reducer, independent model.
- **Concrete tasks:** Constructor invariants, legal/illegal transition table, terminal monotonicity, pending action identity와 pure reducer를 구현한다. Independent model은 별도 test package에서 production helper를 쓰지 않는다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
    -Dtest=SolveCoordinatorStateMachineTest,CoordinatorModelBasedPropertyTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** §9.3의 state/model methods 발견·green, illegal transition publication 0, model diff 0.
- **Failure/rollback:** Reducer가 port/clock/provider SDK를 호출하거나 oracle가 production reducer를 공유하면 WP를 폐기한다. WP-10.0 contract가 last safe point다.
- **Handoff:** Pure `CoordinatorDecision`과 model transition table을 WP-10.2에 전달한다.

### WP-10.2 — Immutable artifact, single CAS와 lease-free crash/resume

- **Prerequisite:** WP-10.1 green, accepted Phase 09 put-if-absent/readVerified/CAS.
- **Change target:** `SolveCoordinator`, pending action lifecycle, CAS/action/crash fakes.
- **Concrete tasks:** Artifact-before-pointer, one-CAS invocation, conflict reload, opaque repository token이 없는 durable pending action, current pending identity/ordinal authorization, every crash window reconciliation과 same-key conflict를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
    -Dtest=CoordinatorIdempotencyConcurrencyTest,CoordinatorCrashResumeTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Concurrent coordinator count와 무관하게 transition ordinal commit 1회와 opaque CAS winner 1개, same action convergence, different digest rejection, lease/lock 사용 0.
- **Failure/rollback:** Correctness에 singleton, leader lease, process lock, multi-object transaction 또는 unconditional overwrite가 필요하면 중단한다. WP-10.1 pure reducer가 last safe point다.
- **Handoff:** CAS-safe state/action executor를 WP-10.3에 전달한다.

### WP-10.3 — Dispatch, event reconciliation, retry와 declared completeness

- **Prerequisite:** WP-10.2 green, accepted Phase 06 worker identity/termination과 Phase 08 dispatcher.
- **Change target:** Phase-1/round assignment/completion contracts, dispatcher integration, exact-ref fan-in.
- **Concrete tasks:** Available portfolio screen set과 typed unavailable evidence, phase-1 stable champion, round assignment refs, retry-only attempt change, duplicate/out-of-order/late/lost events, exact declared set, partial/wait/incomplete와 candidate PASS receipt를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
    -Dtest=PhaseOneFanInTest,DeclaredRoundCompletenessTest,CoordinatorEventOrderingTest,CoordinatorIdempotencyConcurrencyTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Available phase-1/declared phase-2 missing/failed/unverified 차단, typed unavailable 정확성, all completion permutations 동일, retry seed/warm start/work equality, prefix listing 호출 0.
- **Failure/rollback:** 일부 성공 worker champion, event payload authority 또는 retry config 변화가 발견되면 dispatcher composition을 제거하고 WP-10.2 pending action/CAS를 보존한다.
- **Handoff:** Complete ordered verified outcome set을 WP-10.4에 전달한다.

### WP-10.4 — Stable champion, multi-round lineage와 normal termination

- **Prerequisite:** WP-10.3 complete set green, accepted upstream comparator/tie contract.
- **Change target:** `StableRoundChampionSelector`, `RoundChampionRef`, next-round plan과 replay.
- **Concrete tasks:** Worker ordinal stable reduction, comparator/tie 분리, strict improvement warm start, plateau/max-round termination과 lineage fingerprint를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
    -Dtest=MultiRoundChampionTest,CoordinatorReplayTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Equal/worse에서 previous champion exact 보존, strict/max-round hand cases exact, schedule/provider metadata permutations 동일, incomplete batch normal termination 0.
- **Failure/rollback:** `double objective`, first-completed, unordered map 또는 hidden tie/default가 필요하면 selector를 제거한다. WP-10.3 complete ordered set이 last safe point다.
- **Handoff:** Final verified champion과 exact normal termination을 WP-10.5/10.6에 전달한다.

### WP-10.5 — Cancellation, deadline와 failure taxonomy

- **Prerequisite:** WP-10.2~4 green, accepted cancellation/clock port와 explicit test-only policy.
- **Change target:** Cancel/deadline state, stop actions, typed failures와 race/fault tests.
- **Concrete tasks:** Intent request/actual termination 분리, same run-state version의 `CANCEL_REQUESTED`/`PUBLISHING` CAS race, deadline boundary, late success fence, watchdog/resource/platform 구분과 safe public failure를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
    -Dtest=CoordinatorCancellationDeadlineTest,CoordinatorIntegrityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Real sleep/flaky test 0, cancel/deadline 뒤 dispatch/finalize/publish 0, 정상 종료 rename 0, raw provider/PII leakage 0.
- **Failure/rollback:** Cancel을 boolean success로 덮거나 deadline을 max-step/plateau로 변환하면 cancel/deadline layer를 제거한다. WP-10.4 committed round lineage가 last safe point다.
- **Handoff:** Typed terminal/failure와 stop evidence를 WP-10.6에 전달한다.

### WP-10.6 — Phase 07 finalization와 Phase 09 publication orchestration

- **Prerequisite:** WP-10.4 final champion 또는 WP-10.5 typed exceptional terminal, accepted Phase 07/09 contracts.
- **Change target:** `FinalizationRequestRef`, Phase 07 gateway, publish action/receipt와 both-gate tests.
- **Concrete tasks:** Complete champion만 finalization 호출, `Publishable/Rejected` 분기, exact result artifact, run-state token과 분리된 publication-pointer precondition CAS, same/different digest convergence와 terminal state를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application \
    -Dtest=CoordinatorFinalizationPublicationTest,CoordinatorIntegrityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Phase 07 `Publishable` 외 publish action 0, result synthesis 0, same digest success convergence, different digest `PUBLICATION_REJECTED`.
- **Failure/rollback:** Coordinator가 outcome/audit/summary/verifier를 구현하거나 unverified recovery result를 publish하면 integration을 제거한다. Final verified champion ref가 publication 불가 last safe point다.
- **Handoff:** Provider-neutral action/port contract와 published result lineage를 WP-10.7에 전달한다.

### WP-10.7 — Architecture, full evidence, independent review와 Phase 11 handoff

- **Prerequisite:** WP-10.1~6 required tests green, skipped required 0.
- **Change target:** Architecture/port contract, evidence bundle, review input, Phase 11/12/14 handoff manifest.
- **Concrete tasks:** Forbidden provider SDK/provider-state/event DTO/dependency/listing/lock/lease/default 검사, test count/model seed/crash matrix, source/build/artifact/state/result digests, limitations/rollback과 independent review input을 고정한다.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules \
    -Dtest=Phase10CoordinatorArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never -pl build/port-contract-tests \
    -Dtest=CoordinatorPortContractSuite \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/application -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never clean verify
  ```

- **Expected:** License-free reactor green, required failed/error/skipped 0, provider/storage/solver/verifier-internal forbidden edge 0, immutable `E-P10-*` bundle와 independent review `PASS`.
- **Failure/rollback:** Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`나 Phase 11 readiness를 주장하지 않는다. Last accepted predecessor와 last green WP artifact를 보존한다.
- **Handoff:** §14.2의 provider-neutral definition/ports만 Phase 11에 전달한다.

## 11. Verification commands, pass criteria와 evidence

### 11.1 Layer별 future command

| Layer | Future command | Pass criteria |
|---|---|---|
| State/model | WP-10.1 selected command | Exact methods discovered/green, model diff 0 |
| CAS/crash/concurrency | WP-10.2 | One authoritative CAS, same-action convergence, lock/lease 0 |
| Dispatch/completeness/events | WP-10.3 | All-declared set exact, partial/duplicate/order cases green |
| Champion/replay | WP-10.4 | Stable reduction, strict lineage, schedule/provider independence |
| Cancel/deadline/integrity | WP-10.5 | Fence and taxonomy exact, sleep/flaky 0 |
| Finalization/publication | WP-10.6 | Both-gate only, result synthesis 0, CAS convergence |
| Architecture/port | WP-10.7 | Forbidden dependency/semantic path 0 |
| Module | `./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application -am clean verify` | Application + required upstream tests/package green |
| Reactor | `./mvnw -B -ntp -Dstyle.color=never clean verify` | License-free full reactor green; unrelated required modules not skipped |

Selected command는 `-am`을 제거하고 `-Dsurefire.failIfNoSpecifiedTests=true`를 사용한다. 각 selected run 전에 **같은 exact source commit/archive**를 root `./mvnw -B -ntp -Dstyle.color=never clean install`로 full-test 설치하고 dependency artifact digest를 기록한다. 이 preparation run은 selected class/method report를 대신하지 않는다. 각 selected command 직후 fresh Surefire XML을 content-addressed evidence 위치에 봉인하고 expected class/method manifest와 discovered/passed/failed/error/skipped를 대조한 뒤 다음 `clean`을 실행한다.

`-DskipTests`, `-Dmaven.test.skip=true`, required test disable, zero-test selected command, `surefire.failIfNoSpecifiedTests=false`, stale `target/`, repeated flaky success만 선택, console summary 한 줄 또는 서로 다른 manifest/evidence 조합은 exit evidence가 아니다.

### 11.2 Planned evidence

| Key | 반드시 포함할 내용 | 현재 상태 |
|---|---|---|
| `E-P10-STATE` | Source/build/manifest/state/action contract, legal/illegal table, independent model digest, generated sequences/seeds, single-CAS/concurrency/crash history와 terminal monotonicity | NOT_PRODUCED |
| `E-P10-COMPLETENESS` | Declared assignment set, exact outcome refs, missing/failed/unverified/extra cases, completion permutations, comparator/tie/strict lineage와 normal termination records | NOT_PRODUCED |
| `E-P10-RETRY` | WorkerRun/Attempt/Action identities, seed/warm-start/config/work equality, duplicate/out-of-order/late/stale/lost event, cancel/deadline, publication convergence와 tamper rejection | NOT_PRODUCED |

각 bundle은 content-addressed immutable artifact 또는 digest-protected local equivalent여야 하며 다음을 포함한다.

- Phase/document/review version, source commit와 exact fingerprints
- Accepted Phase 06~09 evidence/review refs
- Java/Maven/runtime compatibility와 exact command/exit code
- Discovered/passed/failed/error/skipped class/method count
- Model oracle source/digest, generated seed와 minimized failure sequence
- Artifact/state/action/publication before/after version/digest
- Fault/crash/concurrency/cancel/deadline/tamper matrix
- Security/redaction/tenant/architecture result
- OPEN/GATED/deferred 항목과 non-applicable 근거
- Reviewer/verdict/timestamp, handoff와 rollback point

## 12. Exit gate, Definition of Done과 anti-pattern

### 12.1 Exit gate

Independent reviewer가 다음 AND 조건을 모두 확인해야 Phase 10 `ACCEPTED`를 권고할 수 있다.

- Phase 06~09 accepted authority/evidence와 exact fingerprints가 확인됨.
- Manifest가 worker/round/seed/warm-start/config/tie/retry/deadline 값을 explicit하게 가지며 official hidden default가 없음.
- Legal transition과 terminal monotonicity가 independent model과 일치함.
- Single CAS/lease-free concurrency, exact current pending-action authorization과 모든 crash window가 수렴함.
- Duplicate/out-of-order/late/stale/lost event가 exact-ref authority를 바꾸지 않음.
- All-declared normal completion + candidate `PASS` 없이는 champion/next round/finalization이 0회임.
- Completion permutation, concurrent coordinator와 retry attempt가 champion/lineage/result identity를 바꾸지 않음.
- `NO_STRICT_IMPROVEMENT`/`MAX_ROUNDS_REACHED`가 complete verified round 뒤에만 생성됨.
- Cancellation intent request/run-state cancellation fence/actual stop과 watchdog/resource/platform failure가 정확히 분리됨.
- Tampered manifest/artifact/state/assignment/result가 fail-closed 거부됨.
- Phase 07만 publishable result를 만들고 distinct publication-pointer precondition을 쓰는 Phase 09 publication CAS 뒤에만 `SUCCEEDED`.
- Provider/storage/solver/verifier internal dependency와 prefix listing/lock/lease/LWW 경로가 0.
- `E-P10-STATE`, `E-P10-COMPLETENESS`, `E-P10-RETRY`가 immutable identity와 independent review를 가짐.

### 12.2 Definition of Done

- [ ] Entry gate, owners와 scheduler task가 확정됨
- [ ] Proposed API가 actual Phase 08/09/11 contract와 cross-phase review됨
- [ ] State/action/failure/identity가 versioned immutable contract임
- [ ] One-transition/one-CAS와 durable pending action이 구현됨
- [ ] Partial/duplicate/retry/cancel/deadline/crash/concurrent path가 test됨
- [ ] Model-based oracle와 generated sequence evidence가 독립적임
- [ ] Declared completeness와 stable champion이 order-independent임
- [ ] Both-gate finalization/publication authority가 보존됨
- [ ] Provider-neutral architecture와 tenant/redaction rule이 green임
- [ ] Required tests failed/error/skipped 0, root reactor green임
- [ ] Three planned evidence bundles와 review verdict가 존재함
- [ ] Phase 11/12/14 handoff와 rollback point가 명시됨
- [ ] OPEN/GATED/deferred 항목이 그대로 보존됨

### 12.3 금지 anti-pattern

| Anti-pattern | 금지 이유 |
|---|---|
| Worker prefix listing 후 보이는 success 최솟값 선택 | Missing/eventual visibility/order에 따라 false success |
| Workflow definition에 comparator/completeness/verifier 작성 | Provider가 application 의미를 소유 |
| Coordinator가 solver state/cache를 읽어 objective 재계산 | Solver correctness/search authority 침범 |
| Candidate `PASS` boolean 또는 raw objective map | Independent verifier authority 붕괴 |
| Same worker different digest 중 하나를 last-write/lowest-objective로 선택 | Integrity failure 은폐 |
| Attempt retry에서 seed/warm start/config/steps 변경 | 다른 logical work를 retry로 위장 |
| State/object/result unconditional overwrite | Lost update와 double finalization |
| Distributed lock/leader lease를 correctness 전제 | Provider substitution과 crash safety 취약 |
| CAS 뒤 action identity를 저장하지 않음 | Crash window에서 lost dispatch/publication |
| Event payload를 state/result authority로 사용 | Duplicate/out-of-order/tamper에 취약 |
| Completion order/clock/thread ID tie-break | Reproducibility 붕괴 |
| Deadline을 `MAX_STEPS_REACHED`/plateau로 변환 | Quality와 platform failure 혼합 |
| Cancel intent만으로 actual termination 주장 | Operational truth 손실 |
| Exceptional last best 자동 publication | Normal/official completion overclaim |
| Coordinator에서 outcome/audit/payload 합성 | Phase 07 finalization/result verifier 우회 |
| AWS ARN/event/SDK를 application type에 노출 | Phase 11 세부가 logical contract 오염 |
| Test-only 수치를 production fallback으로 복사 | `Q-BENCH-02` gate 우회 |
| Phase 13 hybrid/lease를 baseline에 선반영 | `C-17` gate 우회 |

## 13. Blocker, owner, last safe point와 restart

| Blocker/gate | 상태 | Owner boundary | 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00 architecture | BLOCKED | Build/Architecture | Target module/code/test | 이 문서와 proposed tree | Accepted Phase 00 review + `E-P00-ARCH` |
| Phase 06 worker contract | BLOCKED | Solver/Search | Assignment/completion/retry integration | Red worker projection tests | Accepted `E-P06-COW/ALNS/REPLAY` |
| Phase 07 verification/finalization | BLOCKED | Verification | Champion eligibility/finalization/publication | Typed fake only, no PASS claim | Accepted `E-P07-*`와 facade |
| Phase 08 application ports | ACTUAL/UNACCEPTED | Application | Inbound/outbound/local integration | Section-projected proposed signature map | Accepted Phase 08 + local E2E |
| Phase 09 storage/CAS | ACTUAL/UNACCEPTED | Storage/Application | Durable state/artifact/publication | Pure reducer/model | Accepted `E-P09-*` contract suite |
| Run-state/action/publication precondition identity | CROSS-PHASE BLOCKED | Phase 08/09/10 Application/Storage/Coordinator owners | Pending action execution, WP-10.6 publication, Phase 10 exit | Pure reducer; no external action/publication authority | Exact current pending-action authorization과 distinct publication-pointer precondition port/signature/conformance test 승인 |
| Durable monotonic deadline across restart | CROSS-PHASE BLOCKED | Phase 08/10/11 Application/Coordinator/Operations owners | Deadline-enabled crash-resume와 Phase 10 exit | Explicit test-only virtual clock; no production deadline fallback | Durable clock/deadline encoding, restart-origin reconciliation, reserve/error mapping ADR와 crash tests 승인 |
| Phase 09 cited-section compatibility | CROSS-PHASE REVIEW | Storage/Coordinator owners | Signature freeze와 Phase 10 entry | 이 문서의 stable Phase 09 section mapping | Accepted artifact/evidence identity와 cited-section semantic diff review; neighbor digest 사용 0 |
| Phase 11 expected handoff drift | DOWNSTREAM RE-READ | Coordinator/AWS owners | Phase 11 entry와 handoff readiness | 이 문서 §14.2와 linked Phase 11 document | Phase 11 entry에서 문서 재독해; Phase 10 downstream digest/monitoring 0 |
| Scheduler task/owners | OWNER_GATE | Total scheduler | Implementation status 전이 | Reviewed Phase 10 v1.3 document | Exact task/assignee/reviewer 등록 |
| `Q-BENCH-02` official values | `OPEN — EXPERIMENT_REQUIRED` | Benchmark/Quality | Official manifest/baseline/Phase 14 | Explicit TEST_ONLY model tests | Calibration corpus/protocol/result review + approval |
| `C-17` route pool/MIP | `GATED TARGET` | Product/Algorithm/Architecture | Hybrid extension | ALNS-only coordinator | Phase 06/07 baseline + Phase 13 separate approval/evidence |
| `Q-VAR-01` | `DEFERRED` | Product/Domain/Algorithm | Optional variants | Current pair/terminal/bank | Resume evidence + separate approval |
| Phase 11 AWS integration | DOWNSTREAM GATE | Platform/Operations/Security | AWS adapter/deployment | Provider-neutral action/port only | Phase 10 accepted + ADR/environment/security approval |
| Phase 12 provider adoption | APPROVAL-GATED | Platform/Operations/Security | Specific future provider | Phase 10 contract suite | Provider selection/parity/security/cost approval |
| Production/public API authority | NOT GRANTED | Product/API/Release | External compatibility/cutover | Internal proposed contract | Versioned API/schema + Phase 11/14 evidence and approval |

Blocker를 hidden default, test double success 또는 legacy GCP behavior로 해소하지 않는다. Same blocker가 남아 있어도 문서와 independent model 설계는 보존하지만 implementation 완료를 주장하지 않는다.

## 14. Previous/next handoff

### 14.1 Previous — actual but unaccepted Phase 06~09

Phase 10이 받아야 할 최소 handoff:

```text
Phase 06:
  committed candidate ref
  exact requested/completed work and termination
  seed derivation, warm-start, config and replay fingerprints
  accepted E-P06-* and review ref

Phase 07:
  candidate verification PASS/rejection contract
  VerifiedSolution and comparator-ready objective identity
  finalization facade
  PublishableResult/rejection contract
  accepted E-P07-* and review ref

Phase 08:
  Phase08ExecutionSeamManifest
  Submit/Prepare/Advance/Execute/Complete/Cancel/Status use cases
  ArtifactStore/RunStateRepository/ResultPublisher
  WorkerDispatcher/CancellationPort/TelemetryPort/Clock
  deterministic local E2E and accepted E-P08-*

Phase 09:
  Phase09StorageHandoff
  ArtifactKey/Ref and canonical encoding identity
  putIfAbsent/readVerified
  state/publication compareAndSet and opaque version token
  declared exact-key retrieval and tenant isolation
  accepted E-P09-*
```

받으면 안 되는 것:

- Mutable search/COW/cache/builder/verifier scratch
- Provider URI/client/event/credential와 workflow state
- Prefix listing으로 만든 worker set
- Solver feasible flag/raw objective/summary
- Result `PASS` 없는 candidate/final result
- Hidden retry/deadline/official numeric default
- Same identity/different bytes를 덮어쓴 artifact

### 14.2 Next — actual but unaccepted Phase 11 AWS reference

[Actual Phase 11](phase-11-aws-reference-distribution.md)의 expected handoff와 작성 시 대조한 결과 Phase 11은 다음 provider-neutral definition/ports만 소비해야 한다. Phase 10은 이 downstream 파일의 section fingerprint나 현재 상태 snapshot을 기록하거나 이후 변경을 감시하지 않는다. Phase 11 entry에서 이 링크와 아래 handoff를 다시 읽고 semantic compatibility를 확인한다.

```text
Phase10HandoffManifest
  phase10ContractVersion
  accepted Phase06/07/08/09 review/evidence refs
  source/build/runtime/manifest fingerprints
  ExecutionRunId/RoundId/WorkerRunId/AttemptId/ActionId contract fingerprint
  SolveState/legal-transition/failure taxonomy fingerprint
  CoordinatorAction schema fingerprint
    no opaque repository version in semantic identity/payload
    exact current pending ActionId/payload/transition authorization
    distinct publication-pointer precondition
  Artifact/RunState/WorkerDispatcher/Cancellation/Clock/ResultPublisher port fingerprints
  declared-completeness/comparator-tie/retry/cancel/deadline semantics
    cancellation intent is request; CANCEL_REQUESTED run-state CAS is fence
    durable monotonic restart contract approval ref
  independent model and port-contract suite refs
  Phase10EvidenceManifest ref
  E-P10-STATE / E-P10-COMPLETENESS / E-P10-RETRY refs
  accepted Phase10 review ref
  rollback point

CoordinatorAction
  DispatchPhaseOneScreens
  WaitForPhaseOneScreens
  DispatchWorkers
  WaitForWorkers
  RequestWorkerStops
  FinalizeResult
  PublishResult
  CompleteSolve
  FailSolve

Phase10EvidenceManifest
  exact state/action schema and legal-transition fingerprint
  declared phase-1/round set and completion oracle refs
  retry/duplicate/order/crash/concurrency/cancel/deadline fault matrix
  publication CAS convergence record
  architecture/port-contract results
```

Phase 11이 구현할 수 있지만 Phase 10이 당기지 않는 것:

- S3 conditional request/version mapping
- Step Functions command/wait/wakeup/retry/cancel definition
- Lambda API/coordinator/worker handler와 remaining-time mapping
- ARN/resource/environment/IAM/encryption/network/retention/cost/IaC
- AWS SDK error/event DTO와 provider execution metadata
- Isolated AWS integration, shadow, security와 rollback rehearsal

Phase 11은 local↔AWS에서 같은 logical manifest/state/action/artifact/result/termination을 증명해야 한다. Step Functions/Lambda가 completeness, comparator, verifier 또는 publication eligibility를 다시 구현하면 handoff 위반이다.

### 14.3 Other downstream consumers

| Consumer | 소비할 것 | 소비하면 안 되는 것 | Handoff gate |
|---|---|---|---|
| Phase 12 | Same port/action/state contract와 parity suite | AWS locator/event를 generic API로 승격 | Provider별 adoption 승인 |
| Phase 14 | Complete verified terminal result, approved manifest lineage | Test-only values/incomplete/recovery result | `Q-BENCH-02`, compliant fixture, production authority |
| Phase 13 gated | Future accepted worker outcome extension | Cross-worker pool/lease/solver를 baseline coordinator에 추가 | `C-17` separate scope/evidence |

## 15. Source → requirement → test → evidence traceability

아래 evidence key는 planned requirement이며 현재 completion evidence가 아니다.

| Requirement | Source | Exact test/oracle | Planned evidence |
|---|---|---|---|
| `P10-STATE` Application-owned legal state machine | Master §4, Architecture §3.5, Integrated §14.1~14.2 | `SolveCoordinatorStateMachineTest`, model oracle | `E-P10-STATE` |
| `P10-IDENTITY` Manifest-bound run/round/worker/attempt/action | Architecture §3.6, Integrated §14.4 | Identity constructors, replay tests | `E-P10-STATE`, `E-P10-RETRY` |
| `P10-CAS` Immutable artifact + single pointer CAS, lease-free | Integrated §13.3~13.8 | Concurrency/crash tests | `E-P10-STATE` |
| `P10-COMPLETENESS` All declared worker exact fan-in | Master §11.3/§14.4, Domain §14, Integrated §14.3 | `DeclaredRoundCompletenessTest` | `E-P10-COMPLETENESS` |
| `P10-ORDER` Completion-order-independent champion | Master §11.3/§14.4 | Permutation + model oracle | `E-P10-COMPLETENESS` |
| `P10-STRICT` Strict improvement/plateau/max-round lineage | Master §11.3, Domain §16 | `MultiRoundChampionTest` | `E-P10-COMPLETENESS` |
| `P10-RETRY` Same logical work, new attempt only | Architecture §3.6, Integrated §14.4/§21 | Retry/replay tests | `E-P10-RETRY` |
| `P10-EVENT` Duplicate/out-of-order/late/stale/lost hint safety | Integrated §13.7, §22.3 | Event ordering/metamorphic tests | `E-P10-RETRY` |
| `P10-CANCEL` Intent request/run-state fence/actual termination separation | Master §13, Architecture §3.6, Integrated §13.3~§13.8 | Same-version cancellation/publication race tests | `E-P10-RETRY` |
| `P10-DEADLINE` Watchdog/platform vs quality separation and restart-safe clock contract | Master §13.1, Architecture §3.2/§5.3 | Virtual-clock boundary + crash/restart-origin tests | `E-P10-RETRY` |
| `P10-INTEGRITY` Manifest/artifact/same-ID-different-digest rejection | Integrated §13/§21/§22.4 | Tamper/corruption tests | `E-P10-STATE`, `E-P10-RETRY` |
| `P10-FINAL-AUTH` Phase 07 both-gate only | Master §14.1, Actual Phase 07 §7~§9 | Finalization/publication tests | `E-P10-COMPLETENESS` |
| `P10-PUBLISH` Distinct-precondition exact result publication CAS convergence | Integrated §13.3~§13.5/§14, Actual Phase 09 §3.1~§3.2/§8.5~§8.6 | Publication token separation/conflict tests | `E-P10-RETRY` |
| `P10-PROVIDER` No provider/storage/solver/verifier semantic leakage | Master §4.4, Integrated §14.1/§15.3 | Architecture/port suite | All `E-P10-*` |
| `P10-OPEN` No hidden official value | `Q-BENCH-02`, realization plan §14 | Manifest guard/model fixtures | `E-P10-STATE` |

Requirement 의미, state taxonomy, OPEN/GATED/deferred 상태 또는 evidence key를 바꾸려면 source/owner/approval과 Phase 08~11 영향 문서를 같은 변경 단위에서 갱신한다.
