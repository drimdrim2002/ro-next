# Phase 08 — Application interface와 local 실행

```yaml
document_status: REVIEWED_WITH_CORRECTIONS
document_version: 1.3
phase: "08"
phase_name: application-ports-local-runtime
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
evidence_status: NOT_PRODUCED
review_status: COMPLETE
review_verdict: CHANGES_REQUIRED
review_document: ../reviews/phase-08-review.md
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Application/Local Runtime owner role
  upstream_result: Phase 07 Verification/Result owner role
  upstream_semantics: Phase 01~06 Domain/Profile/Algorithm owner roles
  local_adapters: Local CLI/Filesystem adapter owner role
  legacy_characterization: Legacy HTTP/GCP compatibility owner role
  security_observability: Application Security/Operations owner role
  downstream_storage_contract: Phase 09 Object Storage owner role
  downstream_coordination_contract: Phase 10 Coordinator owner role
  downstream_benchmark_qualification: Phase 14A ALNS Benchmark Qualification owner role
  review: independent Phase 08 reviewer role
prerequisites:
  - Phase 00 accepted reactor/module/package architecture
  - Phase 01 accepted canonical input and normalization
  - Phase 02 accepted immutable ProblemInstance and complete PreparedTravel
  - Phase 03 accepted propagation/evaluation kernel
  - Phase 04 accepted exact BoundProfile binding
  - Phase 05 accepted initial portfolio
  - Phase 06 accepted committed candidate and deterministic replay envelope
  - Phase 07 accepted Phase07Output both-gate contract and evidence
planned_evidence:
  - E-P08-PORT
  - E-P08-LOCAL-E2E
  - E-P08-IDEMPOTENCY
source_sections:
  canonical_master: "§2.2~2.4, §4.1~4.7, §10, §13~14.1, §15.10, §16~17"
  final_domain: "§15~17, §18"
  final_architecture: "§2, §3.1~3.6, §5.1~5.6, §6"
  integrated_design: "§1.3~1.5, §3, §12, §13.1~13.4, §19~25, §27~28"
  open_questions: "Q-OBJ-01, Q-BENCH-02, Q-INFRA-01, Q-VAR-01 and status/gates"
  master_realization_plan: "§2~4, Phase 07~10, §8~15"
  phase_07_actual: "§7.6~7.7, §8.2~8.5, §13.2~15.2"
  phase_09_actual: "§4~5.3, §7.1~8.8, §15.1"
source_fingerprints_sha256:
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/deprecated/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/deprecated/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/deprecated/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
  docs/deprecated/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
neighbor_phase_documents:
  phase_07: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_BLOCKED_NOT_IMPLEMENTED_NOT_READY
  phase_08_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
  phase_09: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_BLOCKED_NOT_IMPLEMENTED_NOT_READY
  phase_10: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_BLOCKED_NOT_IMPLEMENTED_NOT_READY
historical_authoring_snapshot:
  authority: HISTORICAL_OBSERVATION_ONLY_NOT_CURRENT_STATUS
  phase_09: READY_FOR_REVIEW_NOT_STARTED_NOT_READY_WHEN_FIRST_OBSERVED
  phase_10: READY_FOR_REVIEW_NOT_STARTED_NOT_READY_WHEN_FIRST_OBSERVED
fingerprint_cycle_policy:
  rule: ADJACENT_PHASE_DOCUMENT_OR_SECTION_DIGEST_AS_ACCEPTANCE_GATE_FORBIDDEN
  method: canonical source fingerprints, stable section/contract citations, semantic impact review, and accepted implementation artifact/evidence refs only
```

## 1. 문서 지위, 권위와 source receipt

이 문서는 Phase 08 구현 전 실행 계약이다. 이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. Source 문서의 `REVIEW` metadata는 provenance로 보존하지만 문서 작성을 멈추는 조건이 아니다. 반대로 이 문서가 상세 Java signature, test 이름과 command를 제시한다는 사실은 구현, evidence, review 또는 handoff가 완료되었다는 뜻이 아니다.

상태 축을 섞지 않는다.

| 상태 축 | 현재 값 | 의미 |
|---|---|---|
| 문서 | `REVIEWED_WITH_CORRECTIONS` | 독립 review의 안전한 정정을 반영했으나 residual blocker가 있음 |
| 구현 | `NOT_STARTED` | Proposed module/type/test/runtime이 존재한다고 주장하지 않음 |
| evidence | `NOT_PRODUCED` | `E-P08-*`는 future bundle key이며 현재 evidence가 아님 |
| entry gate | `BLOCKED_BY_UNACCEPTED_PREDECESSORS` | Phase 00~07 accepted review/evidence가 없음 |
| review | `COMPLETE — CHANGES_REQUIRED` | [Phase 08 독립 review](../reviews/phase-08-review.md)의 residual blocker가 남음 |
| handoff | `NOT_READY` | Phase 09/10이 소비할 accepted application/storage contract가 아직 없음 |

권위 적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../deprecated/master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../deprecated/2026-07-26-domain-design.md)의 canonicalization, termination, verification/result 의미
4. [Final Architecture Design](../../deprecated/2026-07-26-architecture-design.md)의 Java/Maven/runtime/port 경계
5. [Integrated implementation design](../../deprecated/architecture-domain-implementation-design.md)의 15 Phase, no-DB storage와 provider substitution 배치
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../deprecated/2026-07-26-master-design.md)는 누락·퇴행 cross-check에만 사용했다. `docs/codex/*`는 2026-07-24 역사 자료이며 현재 authority, API 이름 또는 완료 evidence로 사용하지 않는다.

Final Domain/Architecture의 옛 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 최신 Canonical Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. 선택된 target/reference가 AWS S3 + Step Functions + Lambda라는 사실은 Phase 08에 AWS SDK, object storage, coordinator 또는 deployment를 넣으라는 뜻이 아니다. Phase 08은 provider-neutral semantics와 local reference까지만 만든다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 08에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §4.1~§4.7, §10, §13~§14.1, §15.10, §16~§17 | Submission→canonicalization→solve→both-gate publication, identity, termination, migration/rollback |
| [Final Domain](../../deprecated/2026-07-26-domain-design.md) | §15~§17, §18 | Phase 07 output 의미, pre-solve/domain failure와 정상/예외 termination 구분 |
| [Final Architecture](../../deprecated/2026-07-26-architecture-design.md) | §2, §3.1~§3.6, §5.1~§5.6, §6 | Java 25 module DAG, local reference, identities, retry/cancel, port/security/observability |
| [Integrated design](../../deprecated/architecture-domain-implementation-design.md) | §3, §12, §13.1~§13.4, §19~§25, §27~§28 | Phase 08 use case/ports/local runtime, Phase 09 storage seam, failure, anti-pattern |
| [질문 등록부](../../deprecated/master-design-open-questions.md) | `Q-OBJ-01`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01`, §3~§4 | Exact profile, explicit test-only values, AWS 선택과 구현 분리, deferred 보존 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§4, Phase 07~10, §8~§15 | Current inventory, Phase 08 exit evidence, DoD/blocker/handoff |
| [구현 문서 지도](../README.md) | §3~§7 | Authority, canonical filename, planned link와 scheduler/review 규칙 |
| [Root README](../../../README.md) | 기술 기준, 배포, placeholder 설명 | Java 25/Maven/GCP placeholder의 actual inventory와 target contract 분리 |
| [Actual Phase 07](phase-07-independent-verification-final-result.md) | §7.6~§7.7, §8.2~§8.5, §13.2~§15.2 | `Publishable`, `VerificationRejected`, `GateIncomplete`, exact safe fields and handoff/evidence |
| [Actual Phase 09](phase-09-object-storage-no-database.md) | §4~§5.3, §7.1~§8.8, §15.1 | Phase 08 three-port/checksum/local-scope receipt, exact-key/CAS/closure와 no-list consumer boundary |

Phase 07/09 같은 인접 문서의 whole-file 또는 발췌 section digest는 acceptance 조건으로
사용하지 않는다. Phase 08은 실제로 소비하는 Phase 07 §7.6~§8.5/§13.2~§15.2와
Phase 09 §7.2~§8.8/§15.1의 stable contract 이름, field와 의미를 인용한다. Entry 시에는
해당 section을 다시 읽어 semantic diff와 source→contract→test impact를 기록하고, accepted
implementation artifact/evidence ref만 단방향으로 pin한다. 인접 문서 digest mismatch,
reciprocal fingerprint 또는 “named-section hash 일치”만으로 entry를 열거나 막지 않는다.

같은 scheduler batch의 동시 Phase 09 independent review v1.2는 이 review에서 한 번만
관찰했다. 최신 Phase 09는 adjacent document/section digest acceptance를 제거하고 stable
named section과 accepted artifact/evidence identity로 trace하므로 해당 문서 blocker는
해소됐다. 그 상태나 hash를 반복 추적하지 않고 아래에는 잔존한 semantic blocker만 보존한다.

### 1.2 ALNS-first local benchmark seam

Phase 08 local reference는 Phase 06 ALNS와 Phase 07 both-gate 결과를 optimizer vendor,
MIP solver/license/native runtime, cloud provider 또는 production authority 없이
end-to-end 실행하는 기준 경로다. 이 경로는 Phase 14A benchmark qualification에
immutable run artifacts를 제공하지만 스스로 benchmark acceptance를 발행하지 않는다.

Phase 14A handoff에는 dataset/fixture digest, normalized problem/travel/profile,
algorithm/build/runtime/hardware fingerprints, actual seed와 repeat/run identity,
requested/completed work, timeout/resource observations, objective vector, candidate/result
verifier reports, canonical result/trace digest가 포함돼야 한다. 이 중 승인되지 않은
corpus, repeat 수, threshold, timeout/resource budget와 variance 기준은
`OPEN — EXPERIMENT_REQUIRED`다. Local E2E pass나 `win_poc_case_floor.json` 1회 성공을
Phase 13 open receipt로 바꾸지 않는다.

## 2. 목표, 범위, 비범위와 불변조건

### 2.1 Objective

Phase 08의 목표는 Phase 01~07의 provider-neutral semantic path를 application use case 뒤에 조립하고, 모든 입력과 실행값이 명시된 local reference runtime에서 다음 end-to-end 흐름을 재현하는 것이다.

```text
explicit local request/config/fixture
→ versioned inbound adapter
→ canonicalization
→ normalization + travel preparation + exact profile binding
→ immutable SolveSnapshot + explicit ExecutionManifest
→ deterministic portfolio/solve
→ committed candidate
→ independent candidate verification
→ finalization + exhaustive insertion audit
→ independent result verification
→ PublishableResult
→ atomic local publication
→ exact status/result reference
```

Phase 07이 `Rejected(VerificationRejected)`을 반환하면 application은 safe rejection만 기록하고,
`Rejected(GateIncomplete)`을 반환하면 incomplete reason과 last safe identity를 별도로 기록한다.
둘 다 정상 route/outcome/payload를 publish하지 않는다. Phase 07이
`Publishable(PublishableResult)`을 반환해도 application은 identity/digest와 publication CAS를
확인한 뒤에만 `SUCCEEDED`와 retrievable result를 노출한다.

Local runner는 cloud runtime의 임시 축소판이 아니라 provider semantics를 비교할 **reference execution**이다. 같은 manifest, semantic inputs, build/runtime compatibility와 정상 step completion에서는 completion order, thread count, filesystem locator, wall-clock과 environment에 따라 canonical result가 바뀌지 않아야 한다.

### 2.2 포함 범위

- Provider-neutral inbound application use case와 outbound port
- Submission, solve, manifest, run, attempt, artifact, publication identity
- `request → canonicalization → solve → Phase 07 both-gate → publication → retrieval` 순서
- Phase 07 typed `Publishable`, `VerificationRejected`, `GateIncomplete`의 exhaustive handling
- Explicit local configuration과 test fixture 기반 CLI reference entrypoint
- In-memory fake와 explicit-workspace filesystem local adapter
- Same-process single local worker execution
- Immutable artifact put/read/digest, versioned state CAS, atomic result pointer
- Idempotent submission/execution/publication semantics
- Cooperative cancellation intent, actual termination과 last safe point 분리
- Explicit watchdog/deadline 관측과 algorithm step budget 분리
- Typed application failure와 adapter-specific safe mapping
- Input/resource limit, tenant/path boundary, log redaction과 structured telemetry
- Existing Java/HTTP/GCP placeholder characterization, migration seam과 local rollback boundary
- Phase 09가 구현할 storage port/contract semantics와 Phase 10이 확장할 execution seam의 handoff

### 2.3 명시적 비범위

- S3/GCS/Azure object backend, bucket/key layout 또는 provider SDK
- AWS Step Functions/Lambda/ECS, GCP Workflows/Cloud Run, Kubernetes coordinator/worker
- Database, search index, 최근 실행 목록, arbitrary query 또는 hidden state store
- Multi-round declared-worker fan-out/fan-in, durable coordinator state machine와 champion selection; Phase 10 책임
- Object-storage `ObjectStorageBackend`와 provider parity; 승인될 Phase 09/11 책임
- AWS deployment/IaC, shadow production cutover, resource sizing와 운영 승인; Phase 11/14 책임
- Existing `/optimizations`, `/internal/batches`, `/internal/finalize` public compatibility 확정 또는 즉시 교체
- Exact public HTTP JSON schema, authentication product, public exit/status code freeze
- `Q-BENCH-02` 공식 worker/round/step/watchdog 수치
- Current decimal `D/U` Win fixture의 official 사용
- Route pool/MIP 또는 optimizer backend; `C-17 GATED TARGET`, Phase 13
- `Q-VAR-01` optional variant 질문·구현; `DEFERRED`
- Multi-trip/rotation, dynamic routing, realtime replanning
- Phase 07 canonical result를 다시 계산하거나 result 의미를 adapter에서 변경하는 일

### 2.4 Phase 08 불변조건

1. **Provider-neutral application:** `rpdptw-application`의 public type/signature/import에 bucket, URI scheme, cloud SDK DTO, HTTP exchange/context, `Path`, environment variable가 없다.
2. **Single semantic path:** CLI, future HTTP, fake/local adapter는 동일 application use case를 호출한다. Adapter가 solver/verifier를 직접 호출하지 않는다.
3. **Canonicalization first:** Raw input을 solver가 직접 읽지 않는다. Versioned adapter와 normalization/travel/profile binding 뒤 immutable snapshot만 solve한다.
4. **No hidden defaults:** Workspace, input, profile/version/preset, algorithm values, seed, limits와 watchdog은 explicit config/fixture에 있다. Environment, current directory, default provider, `System.nanoTime()` 또는 random UUID가 semantic 입력을 만들지 않는다.
5. **Both-gate publication:** Phase 07 `Publishable`만 result publication 후보가 된다. `Rejected`, exception, cancellation, watchdog과 partial candidate는 정상 result가 아니다.
6. **Typed Phase 07 non-success preservation:** `VerificationRejected`의
   stage/disposition/failure/optional candidate PASS와 `GateIncomplete`의
   stage/safe incomplete reason/optional candidate PASS/last-safe identity를 서로 다른
   application outcome으로 손실 없이 보존한다. `GateIncomplete`에 존재하지 않는 disposition이나
   normal payload를 합성하지 않는다.
7. **Identity before retry:** Same idempotency identity + same digest는 수렴하고, same identity + different digest는 conflict/integrity failure다.
8. **Cancellation separation:** 취소 intent, worker의 cooperative observation, actual terminal state와 last completed/committed boundary를 각각 기록한다. 취소를 `MAX_STEPS_REACHED`로 바꾸지 않는다.
9. **Deadline separation:** Wall-clock/transport/provider deadline은 quality budget이나 seed가 아니다. Watchdog/resource/platform timeout을 정상 algorithm termination으로 바꾸지 않는다.
10. **Immutable-before-pointer:** Content를 create-once 저장하고 digest를 검증한 뒤 하나의 authoritative state/result pointer만 CAS한다.
11. **Exact retrieval:** Status/result는 exact `SolveId`/reference로 조회한다. Prefix listing, directory scan 또는 event order가 completeness/authority가 아니다.
12. **No DB assumption:** Local state는 in-memory 또는 explicit versioned file일 수 있으나 relational query/transaction/lock semantics를 application contract로 요구하지 않는다.
13. **Stable reduction:** Same-process concurrency completion order, hash iteration과 log order는 champion/result identity가 아니다.
14. **Safe local boundary:** External path는 adapter에서 normalize/containment/symlink/regular-file/size 검사를 통과하며 core identity로 전달되지 않는다.
15. **Observability is not semantics:** Elapsed, absolute filesystem path, process ID, port와 thread count는 observation/runtime metadata일 뿐 semantic result fingerprint가 아니다.
16. **No premature infrastructure:** Phase 08이 Phase 09 object-common/S3, Phase 10 coordinator, Phase 11 AWS composition을 구현하거나 vendor type을 선반영하지 않는다.
17. **Explicit authorized scope:** Caller가 보낸 `TenantId` 자체는 authorization proof가 아니다.
    모든 command/query와 storage/state/publication operation은 승인된 non-ambient tenant-scoped
    access binding을 사용하고 key/ref의 tenant authority와 일치해야 한다. Global/static,
    `ThreadLocal`, process environment 또는 workspace 경로를 access context로 추론하지 않는다.

## 3. Entry gate와 required evidence receipt

### 3.1 Entry gate

| Gate | Required receipt | 현재 판정 |
|---|---|---|
| `G08-AUTHORITY` | Canonical source fingerprint와 cited adjacent contract semantic-impact review | 문서 review 시 확인, 구현 entry 시 재검증 필요 |
| `G08-P00` | Accepted reactor/module/DAG/architecture rules | `BLOCKED` |
| `G08-P01-P04` | Accepted canonicalization, travel, evaluation/profile artifacts and tests | `BLOCKED` |
| `G08-P05-P06` | Accepted portfolio/solver/replay bundle | `BLOCKED` |
| `G08-P07` | `Phase07HandoffManifest`, both-gate tests, three `E-P07-*`, accepted review | `BLOCKED` |
| `G08-OWNER` | Scheduler task ID, implementer, local/security owner, independent reviewer | `BLOCKED` |
| `G08-LEGACY` | Current HTTP/GCP characterization snapshot and rollback owner | `NOT_PRODUCED` |
| `G08-CONTRACT` | Proposed application/storage type review; public wire schema는 동결하지 않음 | `NOT_STARTED` |

Interface/test-double skeleton은 Phase 00 뒤 병행할 수 있지만 다음은 Phase 07 accepted gate를 우회할 수 없다.

- 정상 local `SUCCEEDED`
- 정상 result publication/retrieval
- `E-P08-LOCAL-E2E`
- Legacy/new result semantic comparison
- Phase 09/10 accepted handoff

### 3.2 Phase 07 entry receipt

Phase 08은 actual Phase 07 §15.2의 다음 최소 receipt를 받아야 한다.

```text
Phase07HandoffManifest
  phase07ContractVersion
  source/build/runtime fingerprints
  problem/travel/profile/evaluation declaration/SolvePlan/candidate/replay identities
  candidatePassReport fingerprint or rejection
  verifiedSolution fingerprint when PASS
  audit/result/resultPass fingerprints when PASS
  canonical payload digest/length when PASS
  Phase07Output contract fingerprint
  E-P07-CANDIDATE-VERIFY / E-P07-AUDIT / E-P07-RESULT-VERIFY refs
  accepted Phase07 review ref
  rollback point

Phase07Output
  Publishable(PublishableResult)
  or Rejected(FinalResultRejection.VerificationRejected)
  or Rejected(FinalResultRejection.GateIncomplete)

FinalResultRejection.VerificationRejected
  stage
  VerificationDisposition
  safe VerificationFailure
  optional CandidatePassReport fingerprint

FinalResultRejection.GateIncomplete
  stage
  SafeIncompleteFailure
  optional CandidatePassReport fingerprint
  LastSafeIdentity
```

받으면 안 되는 것은 다음이다.

- Mutable finalizer/builder/auditor state
- Search, insertion, route propagation 또는 metric/score cache handle
- Solver feasibility flag, summary 또는 raw objective만 있는 candidate
- Verifier 하나의 `PASS`만 있는 result
- Normal route/outcome/payload를 포함한 failure
- `GateIncomplete`를 `VerificationRejected`, generic exception 또는 fabricated disposition으로 바꾼 receipt
- Provider locator/client/credential
- `Q-BENCH-02` missing value를 채운 default

Phase 07의 live 상태는 review `COMPLETE/CHANGES_REQUIRED`, phase acceptance
`BLOCKED_NOT_IMPLEMENTED`, implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff
`NOT_READY`다. 작성 중 관찰한 옛 `READY_FOR_REVIEW` 값은 historical authoring snapshot일
뿐 current status가 아니다. 따라서 이 Phase 08 문서가 그 contract와 완료된 review를 직접
대조했다는 사실도 entry gate 통과나 accepted Phase 07 evidence를 뜻하지 않는다.

### 3.3 Planned Phase 08 evidence

| Evidence key | 필수 내용 | 현재 상태 |
|---|---|---|
| `E-P08-PORT` | Type signatures, dependency report, fake contract suite, no-provider/no-transport/no-path bytecode/import report, error/lifecycle tables | `NOT_PRODUCED` |
| `E-P08-LOCAL-E2E` | Explicit fixture/config, CLI invocation, request→canonicalization→solve→both-gate→publication/retrieval trace, deterministic rerun, security/limits/telemetry assertions | `NOT_PRODUCED` |
| `E-P08-IDEMPOTENCY` | Same/same convergence, same/different conflict, CAS race, duplicate publication, cancel/deadline retry and last-safe-point fault matrix | `NOT_PRODUCED` |

Evidence bundle에는 exact commit/source fingerprint, Java/Maven version, command, discovered test class/method count, failed/error/skipped, fixture/config/oracle digest, input/output/artifact/state fingerprints, structured event assertions, fault injection point, rollback result와 reviewer verdict가 있어야 한다. Existing `target/` report, synthetic placeholder test, console summary 한 줄과 문서의 expected 결과는 evidence가 아니다.

## 4. 2026-07-28 current inventory, gap와 migration boundary

조사 기준은 branch `codex/domain-design`, commit `3424277`의 local checkout이다. Implementation 문서들은 working tree에서 untracked 상태였으므로 “clean checkout”을 주장하지 않는다. 아래는 read-only source inspection이며 현재 cloud deployment, production traffic 또는 IAM 상태를 검증한 결과가 아니다.

Final consistency pass의 live adjacent document/review status는 다음과 같다. 이 표는
implementation/evidence/acceptance를 승격하지 않으며, 위 metadata의 historical authoring
snapshot을 current authority로 사용하지 않는다.

| Adjacent document/review | Live review verdict | Implementation/evidence | Acceptance/handoff |
|---|---|---|---|
| [Phase 07](phase-07-independent-verification-final-result.md) v1.1 / [review](../reviews/phase-07-review.md) | `COMPLETE / CHANGES_REQUIRED` | `NOT_STARTED / NOT_PRODUCED` | `BLOCKED_NOT_IMPLEMENTED / NOT_READY` |
| Phase 08 / [this review](../reviews/phase-08-review.md) | `COMPLETE / CHANGES_REQUIRED` | `NOT_STARTED / NOT_PRODUCED` | `BLOCKED_NOT_IMPLEMENTED / NOT_READY` |
| [Phase 09](phase-09-object-storage-no-database.md) v1.2 / [review](../reviews/phase-09-review.md) | `COMPLETE / CHANGES_REQUIRED` | `NOT_STARTED / NOT_PRODUCED` | `BLOCKED_NOT_IMPLEMENTED / NOT_READY` |
| [Phase 10](phase-10-provider-neutral-coordinator.md) v1.2 / [review](../reviews/phase-10-review.md) | `COMPLETE / CHANGES_REQUIRED` | `NOT_STARTED / NOT_PRODUCED` | `NOT_RECOMMENDED / NOT_READY` |

### 4.1 Build/runtime inventory

| 항목 | 현재 사실 | Phase 08 판단 |
|---|---|---|
| Maven | Root `pom.xml` 하나인 `com.ronext:ro-next:0.1.0-SNAPSHOT` 단일 project | Target reactor/application/local adapter 경계가 없음 |
| Java | Compiler release 25, `.sdkmanrc` Corretto `25.0.3-amzn`, Maven `3.9.14` | Java 25 type/record/sealed interface 설계 기준과 일치 |
| Dependencies | Google Workflow Executions, GCS, Jackson, JUnit가 root classpath에 직접 있음 | Application/provider dependency isolation이 없음 |
| Packaging | Shade main class가 `OptimizationHttpServer`인 단일 app JAR | Local reference distribution과 legacy GCP runtime이 분리되지 않음 |
| Container | Maven/Temurin 25 build 후 Corretto 25에서 shaded JAR 실행 | Reproducible local semantic distribution evidence가 아님 |
| CI/IaC | Tracked GitHub workflow/AWS IaC 없음 | Phase 08 범위 밖이며 completion evidence가 아님 |

### 4.2 Actual Java/HTTP placeholder

| File/endpoint | 현재 사실 | Characterization/migration 의미 |
|---|---|---|
| `AlnsBatchEngine` | `SplittableRandom`과 `double`로 합성 objective `Map<String,Object>` 생성 | Solver/application target이 아닌 deterministic-ish deployment placeholder |
| `OptimizationHttpServer` | `PORT` default `8080`, `SERVICE_MODE` default `api`, virtual-thread executor | Environment/hidden runtime default characterization 대상 |
| `POST /optimizations` | Raw `Map`, `gs://`만 허용, UUID request ID, default/clamp parameters, missing seed는 `System.nanoTime()` | Target idempotency/reproducibility/input 계약과 불일치 |
| `GET /optimizations/{id}` | `results/{requestId}.json` exact GCS object가 없으면 `RUNNING`, 있으면 raw bytes | Missing object를 running authority로 간주; result gate/status integrity 없음 |
| `POST /internal/batches` | Raw map을 synthetic engine에 전달하고 `candidates/{requestId}/{runNumber}.json` 생성 | Typed assignment/attempt/digest/cancel 없음 |
| `POST /internal/finalize` | GCS prefix listing 후 raw `double objective` 최소 후보 선택, `COMPLETED` 저장 | Declared completeness, stable comparator, candidate/result verifier, CAS 없음 |
| Error | `IllegalArgumentException`→400, 그 밖의 exception→generic 500 | Typed pre-solve/conflict/integrity/cancel/deadline mapping 없음 |
| Security/limits | Java layer에 body/depth/string/count limit, tenant/auth scope, cancellation, log-redaction contract가 없음 | Phase 08 target gate에서 명시적으로 추가할 영역 |
| Test | `AlnsBatchEngineTest.producesCandidateForIndependentAlnsBatch()` 하나 | Synthetic positive objective만 검사; target evidence 아님 |

현재 `OptimizationApiController`의 구체 fallback은 다음과 같다.

```text
parallelRuns: missing/non-number → 8, clamp 1..20
iterationsPerRun: missing/non-number → 5_000, clamp 100..250_000
seed: missing/non-number → System.nanoTime()
requestId: random UUID
```

이 값은 legacy characterization fact이지 Phase 08/official default가 아니다. 특히 `Q-BENCH-02` 공식 worker/step/watchdog 값으로 승격하지 않는다.

### 4.3 Actual deployment inventory

| File | 현재 사실 | Phase 08 경계 |
|---|---|---|
| `gcp/workflows/optimization.yaml` | Declared `parallelRuns` HTTP fan-out, worker timeout 900초, finalize timeout 300초, default HTTP retry | Historical/current migration input; timeout을 algorithm completion으로 간주 금지 |
| `gcp/cloudbuild.yaml` | Docker image build와 `latest` 기본 tag substitution | Immutable runtime identity/cutover evidence가 아님 |
| `gcp/README.md` | Cloud Run API/worker, Workflows, GCS, IAM 절차; API 배포 예시는 unauthenticated | 운영 사실/보안 승인 evidence가 아닌 가이드 |
| Root README | Java 25/GCP 시작점과 placeholder임을 명시 | Placeholder 판정과 일치; 현재 target authority는 canonical docs |

### 4.4 Gap과 migration/rollback boundary

| Legacy behavior | Target semantic | Phase 08 action | Rollback boundary |
|---|---|---|---|
| Random UUID + hidden seed/default | Explicit submission/idempotency/manifest identity | Read-only characterization 후 new local use case에 explicit values | Existing shaded JAR/GCP source untouched |
| Raw `Map<String,Object>` | Versioned sealed/record command와 typed failure | Versioned inbound translation seam 제안 | Legacy DTO/route는 local target contract가 아님 |
| Controller가 Workflow/GCS client 생성 | Application port + adapter composition root | New local distribution에서 dependency inversion 증명 | Legacy server와 new local distribution 병행 |
| Prefix listing으로 candidate completeness 추론 | Exact declared identity와 state/ref | Characterization test에서 위험 고정; target 경로는 listing 0 | Legacy finalize를 target publication에 연결하지 않음 |
| Raw objective minimum | Bound comparator + both verifier | Shadow comparison은 informational only | 불일치 시 legacy/new 중 임의 결과 publish 금지 |
| Result object 존재 여부가 status | Versioned state + result pointer CAS | Exact local repository contract | Migration/cutover 전 legacy status 유지 |
| Generic 400/500 | Typed application failure + safe adapter mapping | Proposed local mapping test | Public HTTP schema/status freeze는 별도 승인 |
| Fixed workflow timeout/retry | Explicit watchdog and logical retry identity | Timeout/cancel/attempt를 분리해 test | GCP workflow 수정/배포는 Phase 08 밖 |

Phase 08은 existing `src/main/java/com/ronext/optimizer/**`, `gcp/**`, Dockerfile 또는 root README를 삭제·대체하지 않는다. 미래 구현 시 new application/local reference가 완전한 evidence를 만들 때까지 legacy shaded artifact가 rollback point다. Shadow 실행은 side-effect-free input copy와 별도 namespace를 사용하고, legacy 결과를 target verifier `PASS`로 위장하거나 dual-write publication을 하지 않는다.

Public/GCP cutover는 Phase 08 exit가 아니다. Target local path가 실패하면 new local distribution과 its workspace/pointers만 사용 중단하고 legacy deployment source로 되돌릴 수 있어야 한다. 이미 생성한 immutable local artifacts는 result pointer가 없으면 정상 publication이 아니며 evidence/retention 정책에 따라 보존한다.

## 5. Proposed change tree와 ownership

아래 tree와 Java 이름은 **proposed internal design**이다. Phase 00/08 review에서 이름을 바꿀 수 있지만 module direction, semantics와 Phase 07/09 boundary는 바꾸지 않는다. 아직 존재하지 않으므로 모두 future red target이다.

```text
build/
├── architecture-rules/
│   └── .../Phase08ApplicationArchitectureTest.java
├── test-fixtures/
│   └── src/test/java/com/ronext/rpdptw/testing/phase08/
│       ├── Phase08LocalFixtureBuilder.java
│       ├── ExplicitLocalConfigBuilder.java
│       ├── Phase07OutputStubBuilder.java
│       ├── ApplicationStateOracle.java
│       ├── LocalArtifactOracle.java
│       └── TelemetryEventOracle.java
└── port-contract-tests/
    └── src/test/java/com/ronext/rpdptw/contract/application/
        ├── ArtifactStoreContract.java
        ├── RunStateRepositoryContract.java
        ├── ResultPublisherContract.java
        └── CancellationPortContract.java

rpdptw/application/
├── pom.xml
├── src/main/java/com/ronext/rpdptw/application/
│   ├── port/in/
│   │   ├── SubmitSolveUseCase.java
│   │   ├── ExecuteLocalSolveUseCase.java
│   │   ├── RequestCancellationUseCase.java
│   │   ├── GetSolveStatusQuery.java
│   │   └── GetVerifiedResultQuery.java
│   ├── port/out/
│   │   ├── ArtifactStore.java
│   │   ├── RunStateRepository.java
│   │   ├── ResultPublisher.java
│   │   ├── ProfileCatalogPort.java
│   │   ├── WorkerDispatcher.java
│   │   ├── WorkflowExecutionPort.java
│   │   ├── CancellationPort.java
│   │   ├── TelemetryPort.java
│   │   └── MonotonicClock.java
│   ├── api/
│   │   ├── SubmissionCommand.java
│   │   ├── ExecutionManifest.java
│   │   ├── ApplicationOutcome.java
│   │   ├── ApplicationFailure.java
│   │   └── identity/
│   ├── service/
│   │   ├── DefaultSubmitSolveService.java
│   │   ├── DefaultLocalSolveService.java
│   │   ├── DefaultCancellationService.java
│   │   └── DefaultRetrievalService.java
│   └── execution/
│       ├── SolveLifecycle.java
│       ├── SolveTransitionPolicy.java
│       ├── RunDeadline.java
│       └── LastSafePoint.java
└── src/test/java/com/ronext/rpdptw/application/...

adapters/common/
└── src/main/java/com/ronext/rpdptw/adapter/common/
    ├── VersionedInputDocumentAdapter.java
    ├── SafeFailurePresenter.java
    └── BoundedDocumentReader.java

adapters/object-filesystem/
├── src/main/java/com/ronext/rpdptw/adapter/local/storage/
│   ├── FileArtifactStore.java
│   ├── VersionedFileRunStateRepository.java
│   ├── AtomicFileResultPublisher.java
│   └── LocalWorkspaceLayout.java
└── src/test/java/com/ronext/rpdptw/adapter/local/storage/...

apps/cli/
├── src/main/java/com/ronext/rpdptw/app/cli/
│   ├── LocalCliMain.java
│   ├── LocalCliCommandParser.java
│   ├── LocalCliFailureMapper.java
│   └── LocalCliOutput.java
└── src/test/java/com/ronext/rpdptw/app/cli/...

distributions/local/
├── src/main/java/com/ronext/rpdptw/distribution/local/
│   ├── LocalCompositionRoot.java
│   ├── LocalRuntimeConfig.java
│   └── LocalRuntimeBootstrap.java
├── src/test/resources/phase08/
│   ├── P08_LOCAL_PD_3_TEST_ONLY.input.json
│   ├── P08_LOCAL_PD_3_TEST_ONLY.profile.json
│   └── P08_LOCAL_PD_3_TEST_ONLY.run.json
└── src/test/java/com/ronext/rpdptw/distribution/local/...
```

다음은 Phase 08에서 만들지 않는다.

```text
adapters/object-common/                  # Phase 09
adapters/object-s3/                      # Phase 09/11
apps/coordinator/ durable state machine  # Phase 10
adapters/workflow-aws-stepfunctions/     # Phase 11
adapters/compute-aws-lambda/             # Phase 11
deployment/aws/                          # Phase 11
```

### 5.1 Compile dependency

```text
rpdptw-core
rpdptw-solver          → rpdptw-core
rpdptw-verification    → rpdptw-core

rpdptw-application
  → rpdptw-core
  → rpdptw-solver
  → rpdptw-verification

adapters/common
  → rpdptw-core
  → rpdptw-application
  → rpdptw-verification

adapters/object-filesystem
  → rpdptw-application

apps/cli
  → rpdptw-application
  → adapters/common

distributions/local
  → apps/cli
  → rpdptw-capabilities
  → rpdptw-profile-catalog
  → adapters/object-filesystem
```

Application은 provider SDK, Jackson/HTTP server transport type, `java.nio.file.Path`, customer implementation 또는 route-selection vendor를 compile-depend하지 않는다. Local composition root만 concrete capabilities/profile catalog/filesystem adapter를 조립한다.

## 6. I/O artifact, contract, identity와 lifecycle

### 6.1 Boundary I/O

| Boundary | Authoritative input | Output | Failure boundary |
|---|---|---|---|
| Local inbound | Explicit local config, bounded input/profile/config documents | Provider-neutral `SubmissionCommand` | Path, bytes, schema, limit failure는 application start 전 safe rejection |
| Submission | Tenant/submission/idempotency identity + command fingerprint | `Created` or same-command `Existing` receipt | Same identity/different fingerprint is `CONFLICT` |
| Canonicalization | Versioned input document, exact adapter | Canonical business input + raw/adapter provenance | Unknown version, alias/reference/numeric/time error is `REJECTED_INPUT` |
| Preparation/binding | Canonical input + exact profile/version/preset/config | Immutable `SolveSnapshot` | Incomplete travel or binding failure cannot start solve |
| Solve | Snapshot + explicit manifest + cancellation/deadline probe | `CommittedCandidate` + replay/termination | Partial COW state never crosses boundary |
| Phase 07 | Accepted Phase 07 request/handoff authority | `PublishableResult`, `VerificationRejected` or `GateIncomplete` | Non-success never carries normal publication payload; incomplete is not fabricated as rejection |
| Artifact | Typed logical key + immutable bytes + expected digest | Verified `ArtifactRef` | Same key/different digest or read mismatch is integrity failure |
| State | Exact `SolveId`, expected opaque version, next legal state | CAS success/conflict | Stale version is reload/re-evaluate, not last-write-wins |
| Publication | `PublishableResultRef`, expected legal state | Published/already-same/conflict | Different digest or missing both-gate authority is reject |
| Retrieval | Exact `SolveId` or published ref | Safe status view or verified result | No list/search inference, no unverified partial payload |

### 6.2 Immutable application artifacts

| Artifact | Minimum identity/authority | Lifecycle |
|---|---|---|
| `SubmittedInputDocument` | schema version, content digest/length, adapter identity | Bounded inbound reader creates once |
| `CanonicalInput` | schema, raw digest, canonical adapter/version | Phase 01 authority |
| `SolveSnapshot` | problem + prepared travel + bound profile identities | Preparation creates once; solver/verifier share exact refs |
| `ExecutionManifest` | snapshot/build/runtime/algorithm/config/seed/work/watchdog declarations | Submission command fingerprint input; no hidden field |
| `CommittedCandidate` | stable routes/bank, replay, exact termination, last safe point | Phase 06 output |
| `PublishableResult` | final manifest/payload, candidate PASS, result PASS, envelope fingerprint | Phase 07 success output only |
| `FinalResultRejection.VerificationRejected` | stage/disposition/failure/optional candidate PASS ref | Phase 07 completed semantic rejection only |
| `FinalResultRejection.GateIncomplete` | stage/safe incomplete reason/optional candidate PASS ref/last-safe identity | Phase 07 incomplete gate only; no disposition synthesis |
| `ApplicationRunRecord` | logical identities, state version, termination, safe refs | CAS state; no mutable domain graph |
| `PublishedResultRef` | solve ID, result/payload digest, artifact ref, both-gate receipt | Result pointer only after publish CAS |
| `ObservationRecord` | monotonic elapsed, runtime/process/platform metadata, event counters | Separate from semantic result fingerprint |

`PublishableResult`의 canonical payload는 Phase 07이 소유한다. Application/local adapter가 JSON field를 추가·삭제·재정렬하거나 elapsed/path/provider ID를 넣어 payload digest를 다시 정의하지 않는다. Transport response가 필요하면 canonical payload 또는 exact ref와 별도의 versioned envelope를 사용한다.

### 6.3 Logical identity

```text
TenantId
SubmissionId
IdempotencyKey
SubmissionCommandFingerprint
SolveId
ManifestFingerprint
WorkerRunId
AttemptId
ArtifactKey
ArtifactDigest
StateVersion
PublishableResultFingerprint
PublishedResultRef
```

| Identity | Stable input | 바뀔 수 있는 것 | 금지 |
|---|---|---|---|
| Submission | tenant + client submission/idempotency key | 없음 | Same key/different command silently overwrite |
| Command fingerprint | input/profile/manifest identities와 versioned projection | fingerprint algorithm version은 migration contract로 변경 | Absolute path, mtime, env, clock, provider URI 포함 |
| Solve | Versioned derivation from accepted submission receipt | Proposed derivation/version review 전 이름 변경 가능 | Retry마다 random UUID 생성 |
| Worker run | solve/manifest/worker assignment/warm start | `AttemptId` only on retry | Seed/requested work/profile 변경 |
| Artifact | tenant + kind + logical artifact ID + content digest | Adapter locator | Locator를 semantic identity로 사용 |
| Publication | solve + publishable/result/payload identity | Same digest duplicate receipt | Different verified digest 임의 선택 |

`SolveId` 문자열 형식과 fingerprint algorithm은 **proposed/open**이다. 구현은 `SolveIdDerivationVersion`과 `FingerprintAlgorithmVersion`을 명시하고 hidden SHA/UUID/time fallback을 만들지 않는다. Phase 02/serialization ADR 또는 Phase 08 contract review가 선택하기 전 test는 explicit test-only derivation을 주입한다.

### 6.4 Lifecycle

```text
NEW
→ SUBMITTED
→ CANONICALIZING
→ PREPARING
→ PREPARED
→ SOLVING
→ VERIFYING_FINAL_RESULT
→ PUBLISHING
→ SUCCEEDED
```

Exceptional/pre-terminal branches:

```text
NEW | SUBMITTED | CANONICALIZING
  → REJECTED_INPUT

PREPARING
  → BINDING_FAILED

any non-terminal execution state
  → CANCEL_REQUESTED
  → CANCELLED

SOLVING | VERIFYING_FINAL_RESULT | PUBLISHING
  ├──▶ WATCHDOG_REACHED
  ├──▶ RESOURCE_LIMIT_REACHED
  ├──▶ PLATFORM_TIMEOUT
  └──▶ FAILED

VERIFYING_FINAL_RESULT
  ├──▶ PUBLICATION_REJECTED
  └──▶ VERIFICATION_INCOMPLETE

PUBLISHING
  → PUBLICATION_REJECTED
```

Phase 08 local single-worker state는 Phase 10의 full outer `ExecutionRound`/multi-worker state machine이 아니다. `ROUND_DISPATCHING`, declared worker completeness, multi-round champion과 durable wakeup/reconcile는 Phase 10이 이 seam 위에 추가한다. Phase 08은 그 future state를 미리 fake state로 만들어 완료를 주장하지 않는다.

Candidate verification, finalization과 result verification은 Phase 07 내부 lifecycle이며
`Phase07FinalResultService`가 하나의 `Phase07Output`으로만 반환하는 현재 handoff에서는
application이 각각의 완료 transition을 독립 관찰할 수 없다. 따라서 Phase 08은 내부 단계 이름을
authoritative outer state로 가장하지 않고 `VERIFYING_FINAL_RESULT` 하나를 소유한다. Exact 실패
stage와 last safe identity는 Phase 07 output variant 안에 보존한다.

### 6.5 Legal state transition rules

| From | Event | To | Authority/guard |
|---|---|---|---|
| `NEW` | accepted command | `SUBMITTED` | Submission create-if-absent and command digest |
| `SUBMITTED` | adapter selected | `CANONICALIZING` | Exact schema/adapter |
| `CANONICALIZING` | canonical input produced | `PREPARING` | Canonical input digest |
| `PREPARING` | snapshot complete | `PREPARED` | Problem/travel/profile fingerprints |
| `PREPARED` | worker assignment committed | `SOLVING` | Explicit manifest and cancellation clear |
| `SOLVING` | committed candidate receipt | `VERIFYING_FINAL_RESULT` | Phase 06 handoff/termination and exact Phase 07 request |
| `VERIFYING_FINAL_RESULT` | `Publishable(PublishableResult)` | `PUBLISHING` | Candidate/result PASS identities in Phase 07 output |
| `VERIFYING_FINAL_RESULT` | `Rejected(VerificationRejected)` | `PUBLICATION_REJECTED` | Exact stage/disposition/failure/optional candidate PASS preserved |
| `VERIFYING_FINAL_RESULT` | `Rejected(GateIncomplete)` | `VERIFICATION_INCOMPLETE` | Exact stage/safe incomplete reason/optional candidate PASS/last-safe identity preserved |
| `PUBLISHING` | same result CAS success | `SUCCEEDED` | Exact result/payload digest |
| Any non-terminal | first cancellation intent | `CANCEL_REQUESTED` | Idempotent intent record |
| `CANCEL_REQUESTED` | worker cleanup and actual stop | `CANCELLED` | No uncommitted candidate; last safe point |
| Publication | typed rejection/integrity failure | `PUBLICATION_REJECTED` | Safe rejection only |

Illegal transition은 `FAILED`로 억지 전환하거나 무시하지 않고 `IllegalStateTransition`을 반환한다. CAS failure 뒤에는 current state를 다시 읽고 transition guard를 재평가한다. Terminal `SUCCEEDED`에 같은 result digest publication/cancel retry가 오면 existing terminal view로 수렴할 수 있지만 다른 digest, 다른 command 또는 다른 terminal meaning은 conflict다.

## 7. Proposed Java 25 contracts

이 절의 package/type/method 이름은 **proposed**, wire schema가 아니라 internal Java projection이다. 실제 구현은 accepted predecessor type 이름에 맞춰 조정할 수 있다. 아래 signature가 존재하거나 compile된다고 주장하지 않는다.

### 7.1 Inbound use cases

```java
package com.ronext.rpdptw.application.port.in;

public interface SubmitSolveUseCase {
    SubmissionReceipt submit(SubmissionCommand command);
}

public interface ExecuteLocalSolveUseCase {
    LocalExecutionOutcome execute(ExecuteLocalSolveCommand command);
}

public interface RequestCancellationUseCase {
    CancellationReceipt requestCancellation(CancelSolveCommand command);
}

public interface GetSolveStatusQuery {
    SolveStatusView getStatus(GetSolveStatus query);
}

public interface GetVerifiedResultQuery {
    VerifiedResultView getResult(GetVerifiedResult query);
}
```

```java
public record SubmissionCommand(
    TenantId tenantId,
    SubmissionId submissionId,
    IdempotencyKey idempotencyKey,
    SubmittedInputDocument input,
    ProfileSelection profileSelection,
    ExecutionManifest executionManifest
) {}

public record ExecuteLocalSolveCommand(
    SubmissionCommand submission,
    LocalExecutionMode executionMode
) {}

public enum LocalExecutionMode {
    SAME_PROCESS_SINGLE_WORKER
}
```

`SubmittedInputDocument`는 defensive immutable bytes abstraction과 explicit length/digest/version을 가진다. `Path`, URL, bucket, stream, `HttpExchange`, Jackson node 또는 mutable `byte[]`를 application command에 넣지 않는다. Local adapter가 bounded read를 완료한 뒤 immutable document로 변환한다.

위 `TenantId`는 routing/key scope이지 caller authorization proof가 아니다. 모든 inbound
command/query에는 adapter가 인증·인가 후 만든 explicit provider-neutral authorized context가
추가되거나, 동등한 explicit tenant-scoped session/facade가 application service와 outbound
adapter를 묶어야 한다. Exact Java shape는 Phase 08/09 Security·Storage 공동 review 전
`BLOCKED/OPEN`이다. 이 결정을 하기 전에는 `TenantId`만 신뢰하거나 global/static,
`ThreadLocal`, environment 또는 workspace root에서 caller scope를 추론해 WP-08.1/3 tenant
isolation을 green으로 만들 수 없다.

### 7.2 Application outcome and receipt hierarchy

```java
public sealed interface SubmissionReceipt
        permits SubmissionReceipt.Created,
                SubmissionReceipt.Existing,
                SubmissionReceipt.Conflict {

    record Created(
        SolveId solveId,
        SubmissionCommandFingerprint commandFingerprint,
        StateVersion stateVersion
    ) implements SubmissionReceipt {}

    record Existing(
        SolveId solveId,
        SubmissionCommandFingerprint commandFingerprint,
        StateVersion stateVersion
    ) implements SubmissionReceipt {}

    record Conflict(
        SubmissionId submissionId,
        SafeConflictDetail detail
    ) implements SubmissionReceipt {}
}
```

```java
public sealed interface LocalExecutionOutcome
        permits LocalExecutionOutcome.Succeeded,
                LocalExecutionOutcome.Rejected,
                LocalExecutionOutcome.Cancelled,
                LocalExecutionOutcome.Interrupted {

    record Succeeded(
        SolveId solveId,
        PublishedResultRef publishedResult,
        SolveStatusView status
    ) implements LocalExecutionOutcome {}

    record Rejected(
        SolveId solveId,
        ApplicationFailure failure,
        SolveStatusView status
    ) implements LocalExecutionOutcome {}

    record Cancelled(
        SolveId solveId,
        CancellationReceipt receipt,
        LastSafePoint lastSafePoint
    ) implements LocalExecutionOutcome {}

    record Interrupted(
        SolveId solveId,
        ApplicationFailure failure,
        LastSafePoint lastSafePoint
    ) implements LocalExecutionOutcome {}
}
```

`Succeeded`에는 both-gate `PublishedResultRef`가 반드시 있어야 한다. `Rejected`, `Cancelled`, `Interrupted`에는 정상 canonical result payload, route/outcome 또는 benchmark vector를 넣지 않는다. Exceptional termination의 last committed candidate가 Phase 07 both-gate를 통과한 recovery-eligible artifact여도 정상 `Succeeded` 또는 official completion으로 이름을 바꾸지 않는다. 외부 노출 정책은 별도 product contract다.

### 7.3 Artifact/storage contracts produced for Phase 09

```java
package com.ronext.rpdptw.application.port.out;

public interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
        ArtifactKey key,
        ArtifactContent content,
        ContentDigest expectedDigest
    );

    ReadableArtifact readVerified(ArtifactRef reference);

    ArtifactMetadata metadata(ArtifactRef reference);
}
```

```java
public record ArtifactKey(
    TenantId tenantId,
    ArtifactKind kind,
    ArtifactId artifactId
) {}

public record ArtifactRef(
    ArtifactKind kind,
    ArtifactSchemaVersion schemaVersion,
    ContentDigest contentDigest,
    long contentLength,
    MediaType mediaType,
    OpaqueLocator opaqueLocator,
    EncryptionClassification encryptionClassification,
    RunIdentity createdByRun
) {}

public record ContentDigest(
    DigestAlgorithmId algorithmId,
    DigestAlgorithmVersion algorithmVersion,
    DigestBytes bytes
) {}
```

`OpaqueLocator`는 adapter만 생성/소비한다. Application은 값을 parse, concatenate, compare 또는 fingerprint input으로 사용하지 않는다. `readVerified`는 metadata/digest/length를 확인한 뒤에만 deserialization을 허용한다.

현재 proposed `ArtifactRef`는 logical `ArtifactKey`/tenant를 직접 보존하지 않으므로
`readVerified(ArtifactRef)`/`metadata(ArtifactRef)`만으로 cross-tenant access를 증명할 수 없다.
Phase 09 §8.3의 `StorageAccessContext` 제안도 public signature에 조용히 parameter를 추가하지
말라고 요구한다. 따라서 Phase 08/09 공동 review가 explicit non-ambient tenant-scoped
session/facade 또는 동등한 closure-binding contract와 caller-visible missing/mismatch failure를
승인하기 전에는 tenant-isolation port contract와 WP-08.3 구현을 시작하지 않는다. 이 문서는
`ArtifactRef`에 field를 임의 추가하거나 opaque locator/path를 tenant authority로 사용하지 않는다.

또한 현재 port projection은 missing, denied, stale, unavailable, visibility-indeterminate,
corrupt와 conflict를 operation별 typed carrier로 exhaustive하게 반환하는 방법을 고정하지
않았다. 아래 `ApplicationFailure` hierarchy는 application-level 의미 분류이지 adapter
exception이나 Phase 09 failure를 generic `AdapterUnavailable`로 축소할 권한이 아니다.
Phase 08/09 공동 review가 lossless carrier와 operation별 mapping을 승인하기 전에는
`ArtifactStore`, `RunStateRepository`, `ResultPublisher` 구현과 fault evidence를 시작하지
않는다.

Digest algorithm/version/byte representation은 **proposed/open**이고 approved fingerprint/checksum ADR 없이는 production default를 선택하지 않는다. Test-only SHA-256을 사용하면 fixture/config/evidence에 `TEST_ONLY`로 명시하며 production policy 승인으로 승격하지 않는다. Same textual digest라도 algorithm/version/kind/schema/length/protected metadata가 다르면 같은 artifact로 취급하지 않는다.

```java
public sealed interface ArtifactPutResult
        permits ArtifactPutResult.Created,
                ArtifactPutResult.AlreadyPresent,
                ArtifactPutResult.Conflict {

    record Created(ArtifactRef reference) implements ArtifactPutResult {}
    record AlreadyPresent(ArtifactRef reference) implements ArtifactPutResult {}
    record Conflict(ArtifactKey key, SafeConflictDetail detail)
        implements ArtifactPutResult {}
}
```

Same key/same digest는 `AlreadyPresent`로 수렴할 수 있다. Same key/different digest, declared digest/content mismatch와 existing bytes mismatch는 `Conflict` 또는 integrity incident이며 overwrite하지 않는다.

```java
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
    PublicationResult compareAndSet(
        SolveId solveId,
        StateVersion expectedState,
        PublishableResultRef result
    );
}
```

```java
public sealed interface PublicationResult
        permits PublicationResult.Published,
                PublicationResult.AlreadyPublished,
                PublicationResult.Conflict,
                PublicationResult.Rejected {

    record Published(PublishedResultRef reference) implements PublicationResult {}
    record AlreadyPublished(PublishedResultRef reference) implements PublicationResult {}
    record Conflict(SafeConflictDetail detail) implements PublicationResult {}
    record Rejected(ApplicationFailure failure) implements PublicationResult {}
}
```

`ResultPublisher`는 Phase 07의 두 PASS와 `PublishableResult` envelope identity를 검증하는 guard를 요구한다. CAS는 content upload를 대신하지 않는다.
현재 `expectedState`는 run-state authorization fence를 뜻하지만 published-result pointer의
absent/current precondition identity를 별도로 표현하지 않는다. 두 token을 같은 값으로
재사용하거나 pointer precondition을 adapter 내부 hidden default로 만들지 않는다. Phase
08/09 공동 review가 distinct run-state fence와 publication-pointer precondition, lost-response
idempotency를 승인하기 전에는 이 proposed signature를 구현 compatibility authority로
고정하지 않는다.

```text
1. PublishableResult canonical payload/artifacts putIfAbsent
2. artifact re-read + digest/length/schema verification
3. Phase 07 handoff/evidence identity match
4. Run state guard re-read
5. exactly one published-result pointer CAS
6. pointer re-read + result digest equality
7. only then SUCCEEDED
```

Phase 09는 위 application semantics를 object-common/backend conditional operation으로 구현한다. Phase 08은 bucket/container/object key, prefix listing, directory rename, multi-object transaction 또는 database transaction을 port 요구사항으로 넣지 않는다.

### 7.4 Profile, dispatch, workflow and cancellation ports

```java
public interface ProfileCatalogPort {
    ProfileDescriptorSnapshot resolveExact(
        TenantId tenantId,
        ProfileIdentity profileIdentity,
        ProfileVersion version,
        PresetIdentity presetIdentity
    );
}

public interface WorkerDispatcher {
    DispatchReceipt dispatch(WorkerAssignment assignment);
    WorkerExecutionStatus getStatus(WorkerRunId workerRunId);
    StopReceipt requestStop(WorkerRunId workerRunId, CancellationId cancellationId);
}

public interface WorkflowExecutionPort {
    WorkflowStartReceipt start(LogicalWorkflowRequest request);
    WorkflowExecutionStatus getStatus(LogicalWorkflowRunId runId);
    WorkflowCancelReceipt requestCancel(
        LogicalWorkflowRunId runId,
        CancellationId cancellationId
    );
}
```

Phase 08 local distribution은 `WorkerDispatcher`의 same-process single-worker implementation만 사용한다. `WorkflowExecutionPort`는 provider-neutral seam과 fake contract까지만 유지하며 durable workflow, round/worker completeness와 provider adapter는 Phase 10/11에 맡긴다. Local success가 workflow port call을 요구하도록 만들지 않는다.

```java
public interface CancellationPort {
    CancellationWriteResult recordIfAbsent(CancellationIntent intent);
    Optional<CancellationIntent> find(SolveId solveId);
}

public record CancellationIntent(
    CancellationId cancellationId,
    SolveId solveId,
    CancellationReasonCode reasonCode,
    ObservationInstant requestedAt
) {}

public interface CancellationProbe {
    CancellationObservation observe();
}
```

`requestedAt`은 observation/audit metadata이고 cancellation identity나 result fingerprint 입력이 아니다. 같은 `CancellationId`/solve/reason retry는 수렴하고 same ID/different solve는 conflict다. Cancellation reason은 safe enum/code이고 raw free text, PII, credential 또는 stack trace를 갖지 않는다.

### 7.5 Telemetry and monotonic observation

```java
public interface TelemetryPort {
    void emit(ApplicationEvent event);
    void record(ApplicationMetric metric);
}

public interface MonotonicClock {
    MonotonicTick now();
}

public record RunDeadline(
    MonotonicTick startedAt,
    WatchdogBudget explicitBudget
) {
    public DeadlineObservation observe(MonotonicClock clock);
}
```

`MonotonicClock`은 lease/elapsed/watchdog 관측에만 사용한다. Quality comparator, seed, stable ordering, semantic fingerprint 또는 `MAX_STEPS_REACHED` 판단에 사용하지 않는다. Wall-clock timestamp가 필요하면 별도 observation clock/record로 주입하고 canonical result에서 제외한다.

### 7.6 Application failure hierarchy

```java
public sealed interface ApplicationFailure
        permits InputRejected,
                BindingRejected,
                IdentityConflict,
                ArtifactIntegrityViolation,
                IllegalStateTransition,
                VerificationRejected,
                VerificationGateIncomplete,
                CancellationCompleted,
                WatchdogReached,
                ResourceLimitReached,
                PlatformTimeout,
                AdapterUnavailable,
                InternalApplicationFailure {

    ApplicationFailureCode code();
    FailureStage stage();
    SafeFailureDetail safeDetail();
    RetryDisposition retryDisposition();
}
```

| Failure | Source meaning | Retry | Normal result 가능 조건 |
|---|---|---|---|
| `InputRejected` | Schema/numeric/time/reference/preparation error | Same command retry 없음 | Corrected new submission |
| `BindingRejected` | Unknown/unauthorized/exact profile dependency error | Same command retry 없음 | Corrected exact config/version |
| `IdentityConflict` | Same key/different command/result digest | 자동 임의 선택 없음 | Caller resolves conflict |
| `ArtifactIntegrityViolation` | Digest/schema/bytes mismatch | Same artifact reuse 금지 | New valid artifact/incident resolution |
| `IllegalStateTransition` | Stale/illegal semantic transition | State reload 후 legal transition만 | Convergence evidence |
| `VerificationRejected` | Phase 07 `FinalResultRejection.VerificationRejected` | Normal retry 아님 | Defect/input investigation 후 new accepted run |
| `VerificationGateIncomplete` | Phase 07 `FinalResultRejection.GateIncomplete` | Same run을 성공으로 retry하지 않음 | Missing authority/operation completion 뒤 explicit resume/new run policy |
| `CancellationCompleted` | Cooperative cancellation actual completion | Idempotent status query only | 정상 success로 변환 불가 |
| `WatchdogReached` | Explicit monotonic safety budget | Manifest policy | Official completion 불가 |
| `ResourceLimitReached` | Explicit resource safety boundary | Manifest/product policy | Official completion 불가 |
| `PlatformTimeout` | Runtime boundary prevented complete record | Platform/operator | Official completion 불가 |
| `AdapterUnavailable` | Transient local/provider adapter failure | Same logical identity | Full completion/verification 뒤 가능 |
| `InternalApplicationFailure` | Unexpected defect | 자동 success/retry 없음 | Investigation and new evidence |

`VerificationRejected`는 Phase 07의 stage, `VerificationDisposition`, safe
`VerificationFailure`와 optional candidate PASS fingerprint를 손실 없이 보존한다.
`VerificationGateIncomplete`는 stage, `SafeIncompleteFailure`, optional candidate PASS
fingerprint와 `LastSafeIdentity`를 손실 없이 보존한다. `GateIncomplete`에는
`VerificationDisposition`/`VerificationFailure`를 합성하지 않으며 generic “failed
optimization”, normal `UNASSIGNED` 또는 completed rejection으로 축소하지 않는다.

Storage adapter failure도 같은 fail-closed 원칙을 따른다. Missing/denied/corrupt/stale/
indeterminate를 throw/catch 한 경로의 `AdapterUnavailable` 하나로 collapse하거나
not-found/success로 바꾸지 않는다. Exact result carrier, safe field와 retry disposition은
`CROSS-PHASE BLOCKER`이며 승인된 Phase 08/09 signature manifest가 유일한 재개 조건이다.

### 7.7 Internal service composition and pseudocode

Accepted upstream Java type 이름에 맞춰 조정할 proposed composition은 다음과 같다.

```java
public final class DefaultLocalSolveService
        implements ExecuteLocalSolveUseCase {

    private final SubmitSolveUseCase submit;
    private final VersionedCanonicalInputAdapter inputAdapter;
    private final ProblemPreparationService preparation;
    private final ProfileBindingService profileBinding;
    private final SolveExecutionService solver;
    private final Phase07FinalResultService finalResultService;
    private final ArtifactStore artifacts;
    private final RunStateRepository states;
    private final ResultPublisher publisher;
    private final CancellationPort cancellation;
    private final TelemetryPort telemetry;
    private final MonotonicClock monotonicClock;
}
```

Pseudocode:

```text
execute(command):
  validate command has explicit schema/profile/version/preset/manifest/seed/work/limits
  receipt = submit.submit(command.submission)
  if receipt is Conflict:
      return Rejected(IdentityConflict)

  current = states.get(receipt.solveId)
  if current is terminal:
      return exact existing terminal view after identity equality check

  transition CAS SUBMITTED → CANONICALIZING
  raw = command.submission.input
  canonical = inputAdapter.parseExact(raw)
  transition CAS CANONICALIZING → PREPARING

  problem, preparedTravel = preparation.prepare(canonical)
  boundProfile = profileBinding.bindExact(command.profileSelection)
  snapshot = SolveSnapshot(problem, preparedTravel, boundProfile)
  snapshotRef = artifacts.putIfAbsent(snapshotKey, encode(snapshot), snapshotDigest)
  manifestRef = artifacts.putIfAbsent(manifestKey, encode(manifest), manifestDigest)
  transition CAS PREPARING → PREPARED

  checkCancellationAndDeadline(lastSafe = PREPARED)
  transition CAS PREPARED → SOLVING
  committedCandidate = solver.solve(snapshot, manifest, cancellationProbe, deadline)
  persist/re-read candidate and replay refs
  check exact termination and last committed boundary

  transition CAS SOLVING → VERIFYING_FINAL_RESULT
  phase07 = finalResultService.finalizeResult(exact Phase07Request)

  switch phase07:
    Rejected(VerificationRejected rejection):
      persist safe rejection artifact
      CAS → PUBLICATION_REJECTED
      return Rejected(VerificationRejected preserving
          stage/disposition/failure/optional candidate PASS)

    Rejected(GateIncomplete incomplete):
      persist safe incomplete artifact
      CAS → VERIFICATION_INCOMPLETE
      return Interrupted(VerificationGateIncomplete preserving
          stage/safe incomplete reason/optional candidate PASS/last-safe identity)

    Publishable(result):
      require candidate PASS and result PASS identities
      persist result/payload/reports immutably; re-read and verify
      CAS VERIFYING_FINAL_RESULT → PUBLISHING
      publication = publisher.compareAndSet(solveId, expectedState, resultRef)
      if Published or exact AlreadyPublished:
          CAS/confirm SUCCEEDED
          return Succeeded(exact published ref)
      else:
          return typed conflict/rejection; never expose payload as success
```

모든 state change와 side effect 전에 cancellation/deadline safe point를 명시한다. 검증/immutable put처럼 이미 시작한 integrity-critical operation은 중간에 반쯤 commit하지 않고 완료 또는 discard한 뒤 actual cancellation state를 기록한다.

## 8. Explicit local reference runtime

### 8.1 Mandatory CLI reference

Phase 08 exit의 mandatory inbound adapter는 CLI다. Local HTTP는 §8.6의 optional migration adapter이며 Phase 08 exit 필수 경로가 아니다.

Proposed subcommands:

```text
ro-next-local solve  --config <explicit-config-file>
ro-next-local status --workspace <explicit-workspace> --tenant <id> --solve <id>
ro-next-local result --workspace <explicit-workspace> --tenant <id> --solve <id>
```

Phase 08 mandatory CLI cancellation은 blocking `solve` process 안에서 interrupt/control channel을 `RequestCancellationUseCase`로 변환하는 cooperative path다. 별도 process의 `cancel` subcommand는 cross-process state/CAS capability를 요구하므로 Phase 08 `LOCAL_SINGLE_JVM` reference의 완료 조건이 아니다. 이를 추가하려면 explicit supported execution scope와 concurrency/crash evidence가 필요하다.

`solve` config는 다음을 모두 explicit하게 가진다.

```text
contractVersion
tenantId
localAuthorizationBindingRef
submissionId
idempotencyKey
workspaceRoot
inputFile
inputSchemaVersion
profileFile or exact local catalog identity
profileIdentity/profileVersion/presetIdentity
algorithmConfigVersion
explicit construction/screen/worker step values
baseSeed and seedDerivationVersion
buildRuntimeCompatibilityFingerprint
watchdogBudget
input/resource limits
telemetry sink selection
local execution mode = SAME_PROCESS_SINGLE_WORKER
```

공식 수치가 없는 `Q-BENCH-02` 필드는 local test/experiment config에 `TEST_ONLY` 또는 `EXPERIMENT` classification과 explicit value로만 넣는다. 이를 production/official default로 직렬화하거나 생략 시 자동 채우지 않는다.

### 8.2 Local runtime config

```java
public record LocalRuntimeConfig(
    LocalRuntimeContractVersion contractVersion,
    WorkspaceRoot workspaceRoot,
    LocalAuthorizationBindingRef authorizationBinding,
    LocalInputSource inputSource,
    ExactProfileSource profileSource,
    SubmissionIdentity submissionIdentity,
    ExplicitExecutionConfig executionConfig,
    LocalInputLimits inputLimits,
    LocalResourceLimits resourceLimits,
    LocalTelemetryConfig telemetryConfig,
    LocalAdapterExecutionScope adapterExecutionScope
) {}

public enum LocalAdapterExecutionScope {
    LOCAL_SINGLE_JVM
}
```

Local bootstrap 규칙:

1. Config file 자체 path만 CLI에서 받고 모든 semantic/runtime field는 config에 명시한다.
2. Current working directory, user home, temp directory, environment variable, classpath “first profile”, `latest`, default region/provider를 사용하지 않는다.
3. Workspace가 없으면 explicit permission 하에서 exact directory만 생성한다. 예상과 다른 owner/permission/symlink/mount semantics면 fail closed한다.
4. Atomic move/create/lock semantics가 local contract를 지원하지 않으면 비원자 fallback이나 last-write-wins로 내려가지 않고 bootstrap을 거부한다.
5. Input/profile/config bytes와 content digest를 상태 전이 전에 기록한다.
6. Locale/timezone/charset은 parser/encoder contract가 explicit하게 소유하며 process default를 사용하지 않는다.
7. Secret이나 cloud credential을 local config에 요구하지 않는다.
8. Config/fixture의 exact bytes digest와 normalized semantic fingerprint를 evidence에 둘 다 기록한다.
9. `authorizationBinding`은 explicit tenant, principal/purpose와 policy version을 승인된
   non-ambient mechanism으로 묶는다. Exact shape/credential product는 Phase 08/09 security
   review 전 OPEN이며, test-only binding은 `TEST_ONLY`로 분류한다. Missing binding을
   `tenantId`, OS user, environment, workspace owner 또는 “local이므로 trusted”로 대체하지 않는다.

### 8.3 Local filesystem adapter

`FileArtifactStore`는 explicit workspace 아래 adapter-internal layout만 사용한다.

```text
<workspace>/
└── tenants/<safe-encoded-tenant>/
    └── solves/<safe-encoded-solve>/
        ├── artifacts/<kind>/<artifact-id>/<digest>
        ├── state/versions/<version>-<digest>
        ├── state/current
        ├── cancellation/<cancellation-id>
        └── results/
            ├── payloads/<digest>
            └── published
```

Layout 문자열은 **proposed adapter detail**이며 application identity가 아니다. Phase 09 object key layout approval과 혼동하지 않는다.

Required local semantics:

- External raw string을 relative path segment로 직접 이어 붙이지 않는다.
- Typed ID를 versioned safe encoding한 뒤 normalize하고 workspace containment를 다시 검사한다.
- Symlink를 follow하지 않으며 parent/target component가 symlink면 거부한다.
- Input은 regular file인지, declared/actual length가 limit 안인지 확인한다.
- Immutable object는 same-directory temporary file에 쓰고 flush/digest/length 검증 후 create-once atomic move한다.
- Existing same digest/bytes는 idempotent; different bytes/digest는 conflict다.
- State는 immutable version artifact를 먼저 쓴 뒤 opaque current pointer를 CAS한다.
- Published pointer는 create-once 또는 expected-version CAS이며 overwrite하지 않는다.
- Partial temp file, unreferenced artifact와 observation log는 normal result가 아니다.
- File lock을 사용하더라도 local adapter 내부 mechanism일 뿐 application/Phase 09 common contract가 file lock을 요구하지 않는다.
- Crash/fault injection에서 last pointer가 가리키는 object는 항상 완전하고 digest-verified여야 한다.

Phase 08 local mutable state/publication CAS의 declared support 범위는 `LOCAL_SINGLE_JVM`이다. Status/result exact read는 후속 process에서 가능할 수 있지만, cross-process writer/cancel/CAS와 distributed durability를 Phase 08 evidence로 주장하지 않는다. OS file lock이나 Java API 존재만으로 이 scope를 넓히지 않는다. Actual Phase 09가 common object semantics와 backend capability matrix에서 이 범위를 정제한다.

### 8.4 Same-process worker dispatcher

`LocalSameProcessWorkerDispatcher`는 하나의 explicit `WorkerAssignment`을 실행한다.

- Stable `WorkerRunId`와 assignment fingerprint를 받는다.
- Retry는 same assignment/seed/warm start/requested work, new `AttemptId`만 허용한다.
- Virtual thread 또는 platform thread 선택은 runtime metadata이며 result identity가 아니다.
- Completion callback 순서로 champion을 선택하지 않는다.
- Cancellation은 `CancellationProbe`를 solver safe point에 전달한다.
- Uncommitted COW trial은 cancel/deadline/failure 시 discard한다.
- Normal `MAX_STEPS_REACHED`는 explicit completed-step equality에서만 나온다.
- Dispatcher exception을 `CANCELLED`, `UNASSIGNED` 또는 normal termination으로 변환하지 않는다.

Phase 08 reference는 single worker다. Multi-worker stable assignment/reduction의 logical tests는 할 수 있지만 실제 fan-out/fan-in과 incomplete-round meaning은 Phase 10 gate다.

### 8.5 Deterministic local rerun

같은 local semantic run을 두 개의 빈 explicit workspace에서 실행해 다음을 비교한다.

```text
same:
  canonical input fingerprint
  problem/prepared travel/profile fingerprint
  execution manifest fingerprint
  committed candidate/replay fingerprint
  candidate PASS/result PASS fingerprints
  final semantic result fingerprint
  canonical payload digest/length
  terminal meaning

allowed to differ:
  absolute workspace path
  process/thread ID
  observation timestamp and elapsed
  temporary filename
  local opaque locator
  log/event order when semantic event order is not specified
```

Same workspace/idempotency retry도 새로운 artifact/result를 overwrite하지 않고 exact existing references로 수렴해야 한다.

### 8.6 Optional local HTTP and legacy seam

Phase 08에서 HTTP adapter를 구현하기로 별도 승인하면 다음 조건을 따른다.

- CLI와 동일 `SubmitSolveUseCase`, status/result/cancel query를 호출한다.
- Bind address, port, request/body/parser limits, auth mode와 deadline을 explicit config로 받는다. `8080`, `api`, public bind와 unauthenticated를 hidden default로 사용하지 않는다.
- Transport request timeout/client disconnect를 automatic solve cancellation으로 해석하지 않는다. 명시적 cancel command만 cancellation intent를 기록한다.
- Versioned transport DTO를 `SubmissionCommand`로 변환하고 `Map<String,Object>`를 application/core에 전달하지 않는다.
- Safe failure mapping과 correlation ID만 응답하며 stack trace, filesystem path, secret, raw input을 노출하지 않는다.
- Existing `/optimizations` compatibility는 characterization matrix와 별도 versioned adapter가 있을 때만 지원한다.

Phase 08 exit는 CLI reference로 충족할 수 있다. Existing GCP HTTP controller를 “local HTTP”라고 이름만 바꾸거나 current endpoints를 public target schema로 동결하지 않는다.

## 9. Idempotency, cancellation, deadline와 error mapping

### 9.1 Submission and execution idempotency

| Scenario | Required result |
|---|---|
| New submission key + valid command | `Created`, one `SolveId`, state `SUBMITTED` |
| Same key + byte/semantic-identical command | `Existing`, exact same solve/fingerprint/state |
| Same key + different input/profile/manifest digest | `Conflict`, no new solve/artifact/pointer |
| Duplicate worker dispatch same `WorkerRunId`/assignment | Same work identity; one committed outcome or same digest convergence |
| Retry after platform start failure | Same `WorkerRunId`, new `AttemptId`; seed/work unchanged |
| Duplicate publish same verified digest | `AlreadyPublished`, exact published ref |
| Duplicate publish different verified digest | Integrity conflict; no arbitrary winner |
| Strong-replay identity produces different verified digest | Integrity violation; neither chosen as “best” |

Idempotency key는 authentication/authorization을 대신하지 않는다. 다른 tenant의 같은 textual key는 서로 접근하거나 충돌하지 않아야 한다.

### 9.2 Cancellation

Cancellation sequence:

```text
request cancel
→ record cancellation intent idempotently
→ state CAS to CANCEL_REQUESTED when legal
→ dispatcher/worker observes intent cooperatively
→ current uncommitted COW trial discard
→ complete/close current integrity-critical operation
→ record actual termination + last completed/committed boundary
→ state CAS to CANCELLED
```

규칙:

- Intent 기록 성공은 worker가 이미 멈췄다는 뜻이 아니다.
- Repeated same intent는 기존 receipt로 수렴한다.
- Cancellation 전에 이미 `SUCCEEDED`이면 terminal result를 삭제/변경하지 않는다.
- Publication CAS와 cancellation race는 state/version guard로 정확히 한 terminal meaning만 허용한다.
- Candidate/result verifier가 실행 중이면 safe interruption contract가 없다면 중간 scratch를 publish하지 않고 단계 완료 또는 discard 후 cancel을 확정한다.
- `CANCELLED`는 normal success, `MAX_STEPS_REACHED`, `NO_STRICT_IMPROVEMENT` 또는 all-unassigned result가 아니다.
- Last committed best가 있어도 recovery result가 되려면 두 verifier가 필요하며 normal/official completion label은 금지한다.

### 9.3 Deadline and watchdog

세 deadline을 구분한다.

| Boundary | 의미 | Result/state mapping |
|---|---|---|
| Algorithm work budget | Explicit completed ALNS steps | Complete equality일 때만 normal termination |
| Application watchdog | Explicit monotonic safety budget | `WATCHDOG_REACHED`, exceptional |
| Transport/platform timeout | HTTP/client/process/provider boundary | `PLATFORM_TIMEOUT` or transport-only timeout; normal result 아님 |

`Q-BENCH-02`의 official watchdog 수치는 OPEN — EXPERIMENT_REQUIRED다. Test는 예를 들어 injected fake clock의 `TEST_ONLY` budget을 명시할 수 있지만 그 값은 production default가 아니다. Wall-clock watchdog retry가 step budget을 바꾸거나 seed를 새로 고르지 않는다.

Deadline observation race rules:

1. Completed semantic step commit이 먼저면 completed count/state를 기록한 뒤 next safe point에서 deadline을 관측한다.
2. Deadline/cancel이 incomplete step 중 관측되면 trial을 discard하고 completed count/adaptive/temperature를 전진시키지 않는다.
3. Publish pointer CAS가 성공한 뒤 transport response가 timeout되어도 retry는 exact published ref로 수렴한다.
4. Platform이 termination record completion 전에 process를 죽이면 `PLATFORM_TIMEOUT`/incomplete이며 normal success를 추론하지 않는다.

### 9.4 Proposed adapter error mapping

Application failure hierarchy는 고정 semantic이고, 다음 CLI/HTTP mapping은 **proposed local adapter contract**다. Public HTTP schema/status freeze가 아니다.

| Application outcome/failure | CLI class | Optional HTTP class | Payload rule |
|---|---:|---:|---|
| `Succeeded` | exit `0` | `200`/`201` according to versioned operation | Exact result ref or canonical payload |
| Accepted async submission | exit `0` | `202` | Solve/status ref only |
| `InputRejected`/`BindingRejected` | nonzero `USAGE_OR_INPUT` | `400`/`422` by versioned schema | Safe field/code; raw input 없음 |
| Unauthorized profile/tenant | nonzero `ACCESS` | `403` | Existence leak 최소화 |
| Not found exact ID | nonzero `NOT_FOUND` | `404` | Safe opaque ID only |
| `IdentityConflict`/publication conflict | nonzero `CONFLICT` | `409` | Existing/different digest raw values 노출 금지 |
| Input/resource limit exceeded | nonzero `LIMIT` | `413` or versioned `422` | Configured limit class, actual content 없음 |
| `VerificationRejected`/integrity | nonzero `INTEGRITY` | versioned `409`/`422`/`500` | Normal route/outcome/payload 없음 |
| `VerificationGateIncomplete` | nonzero `INCOMPLETE` | versioned terminal/incomplete response | Last safe identity only; disposition/normal payload 합성 없음 |
| `CancellationCompleted` | nonzero terminal class or status response | versioned terminal status | Last safe point, no success |
| `WatchdogReached`/`PlatformTimeout` | nonzero runtime class | `504`/terminal query | Exceptional termination preserved |
| Unexpected internal failure | nonzero `INTERNAL` | `500` | Correlation ID only |

Numeric exit codes, exact HTTP code, media type와 response schema는 test-only/proposed until adapter contract review다. 내부 `ApplicationFailureCode`, `FailureStage`, retry disposition과 Phase 07 rejection detail을 generic 문자열 하나로 잃지 않는 것이 고정 요구사항이다.

## 10. Security, explicit input limits와 observability

### 10.1 Security and tenant boundary

- Every command/artifact/state/result key is tenant-scoped.
- Caller-provided `TenantId` alone is not authorization. Approved explicit non-ambient
  tenant-scoped access binding/session must authorize before profile/catalog/artifact/state/result
  existence is read; missing/mismatch outward failure does not reveal existence.
- Local path는 adapter-only이며 typed tenant/solve/artifact ID의 versioned safe encoding으로만 만든다.
- `..`, absolute embedded path, separator injection, NUL/control character, invalid Unicode normalization과 symlink escape를 거부한다.
- Input/profile/result/audit artifact는 classification을 가진다.
- Raw address/PII, full input, canonical payload, secret, credential, stack trace와 absolute workspace path를 log/metric/exception safe detail에 넣지 않는다.
- Digest 검증과 encryption/access control은 서로 다른 통제다.
- Local runtime은 cloud credential, signed URL 또는 provider admin operation을 요구/노출하지 않는다.
- Profile identity는 exact tenant authorization과 version/preset binding을 통과한다. `latest`, other-tenant default와 classpath first-wins fallback이 없다.
- Result retrieval은 published pointer와 both-gate evidence를 확인한다. Artifact path를 안다는 사실만으로 읽을 수 없다.
- Optional HTTP는 public/unauthenticated bind를 Phase 08 default로 제공하지 않는다. Non-loopback exposure는 auth/TLS/gateway/threat review가 별도 필요하다.

### 10.2 Explicit input/resource limits

`LocalInputLimits`와 `LocalResourceLimits`는 config에 모든 값을 요구한다.

```java
public record LocalInputLimits(
    long maxConfigBytes,
    long maxInputBytes,
    long maxProfileBytes,
    int maxNestingDepth,
    int maxStringCodePoints,
    int maxCollectionEntries,
    int maxNumericTokenLength
) {}

public record LocalResourceLimits(
    long maxArtifactBytes,
    long maxWorkspaceBytes,
    int maxOpenFiles,
    int maxInProcessWorkers,
    WatchdogBudget watchdogBudget
) {}
```

위 field와 exact numeric values는 **proposed**다. Production 수치는 security/performance evidence와 approval 전까지 OPEN이다. Test는 method별 explicit boundary, 예를 들어 configured `maxInputBytes = 1024` 같은 `TEST_ONLY` 값을 사용할 수 있으나 default/official value로 문서화하지 않는다.

Limit enforcement order:

```text
filesystem metadata/regular-file/declared size
→ bounded streaming read
→ parser token/depth/string/number bounds
→ schema/semantic domain limits
→ canonicalization checked arithmetic
→ artifact/workspace/resource budget
```

초기 metadata length만 믿지 않고 streaming actual count도 검사한다. Limit 초과를 truncate, clamp, partial parse, empty request 또는 all-unassigned result로 바꾸지 않는다.

### 10.3 Structured observability

가능한 event correlation:

```text
tenantId (safe/opaque)
solveId
submissionCommandFingerprint
manifestFingerprint
workerRunId
attemptId
problemFingerprint
travelFingerprint
profileFingerprint
candidateFingerprint
candidateVerification
resultVerification
publishableResultFingerprint
artifactDigest
stateVersion
termination
lastSafePoint
```

Required event families:

```text
submission.received / existing / conflict
canonicalization.started / completed / rejected
preparation.started / completed / rejected
solve.started / completed / interrupted
candidate.verification.pass / rejected
result.verification.pass / rejected
verification.gate.incomplete
artifact.put.created / existing / conflict / corrupt
state.cas.success / conflict / illegal
cancellation.requested / observed / completed
deadline.observed
publication.published / existing / conflict / rejected
retrieval.succeeded / rejected
```

Metrics:

- Phase duration and separate observation elapsed
- Requested/completed algorithm work
- Artifact bytes/read/write and digest failures
- State/publication CAS conflicts
- Duplicate/retry/cancellation latency
- Candidate/result verification disposition
- Input/resource limit rejection
- Redaction policy violation count

Telemetry failure must not silently change quality, seed or result. Whether telemetry failure is fail-closed or best-effort is an explicit `LocalTelemetryConfig` policy; security audit events required by review cannot be dropped under a best-effort label. Event ordering is not result identity.

## 11. Independent fixtures, exact future-red tests와 red → green

### 11.1 Current test status

이 절의 target class/method는 모두 **future red**다. Proposed modules와 source/test files가 아직 없고 Phase 00~07 entry gate도 통과하지 않았으므로 실행됐거나 green이라고 주장하지 않는다. Current `AlnsBatchEngineTest`의 one-test pass나 ignored/stale `target/` output은 아래 test/evidence를 대체하지 않는다.

Test 구현 원칙:

- Application test는 fake port를 사용하지만 production application service를 실행한다.
- Port fake는 test가 요구한 exact call/result만 기록하며 success를 무조건 반환하지 않는다.
- State transition oracle은 production `SolveTransitionPolicy`를 호출하지 않는다.
- Artifact oracle은 production adapter의 digest/encoding 결과를 그대로 expected로 복사하지 않는다.
- Phase 07 success/rejection unit test는 `Phase07OutputStubBuilder`의 typed fixture를 쓰되, full E2E는 accepted real Phase 07 implementation을 사용한다.
- Full E2E oracle은 production result를 expected file로 즉석 갱신하지 않는다.
- Filesystem fault test는 one fault per case를 주입하고 last pointer/bytes를 independent reader로 검사한다.
- Environment/cwd/locale/timezone permutation은 semantic config를 바꾸지 않는다.
- Numeric limits, seed, work와 watchdog은 test method/config에 explicit `TEST_ONLY` 값으로 기록한다.
- Test/fake/oracle source digest와 fixture/config digest를 evidence bundle에 기록한다.

### 11.2 Test-only fixtures, builders and oracles

| Fixture/builder/oracle | Exact role | Independence/limit |
|---|---|---|
| `P08_LOCAL_PD_3_TEST_ONLY` | Versioned local input/profile/run config로 request→result full path 실행 | Official/approved benchmark 아님; integer travel과 모든 execution value explicit |
| `P08_APPLICATION_PUBLISHABLE_TEST_ONLY` | Known Phase 07 `PublishableResult` envelope for application unit tests | Production verifier/finalizer 호출 없이 safe typed fixture; accepted E2E evidence 아님 |
| `P08_APPLICATION_REJECTION_TEST_ONLY` | Candidate/result-stage `VerificationRejected` variants | Normal route/outcome/payload 없음 |
| `P08_APPLICATION_INCOMPLETE_TEST_ONLY` | Candidate/result-stage `GateIncomplete` variants | Safe incomplete reason/last-safe identity only; disposition 없음 |
| `Phase08LocalFixtureBuilder` | Fresh workspace-independent input/profile/config documents | CWD/env/home/temp default 금지 |
| `ExplicitLocalConfigBuilder` | Every required field, seed/work/limit/watchdog classification 설정 | Missing-field variants 생성, automatic default 없음 |
| `Phase07OutputStubBuilder` | Publishable/rejection/incomplete/tampered identity variants | Application unit layer only |
| `ApplicationStateOracle` | Allowed transition table와 terminal/race outcome 독립 계산 | Production transition policy import 금지 |
| `LocalArtifactOracle` | Exact key/content/digest/length and pointer target independent read | Production `FileArtifactStore` helper 공유 금지 |
| `TelemetryEventOracle` | Required event family, safe-field allowlist와 redaction denylist 검사 | Raw event serializer/helper 재사용 금지 |
| `LocalPathAttackBuilder` | Traversal, separator, control char, Unicode, symlink, hard-link/race fixtures | Test temp root 밖 쓰기/읽기 금지 |
| `FaultInjectingLocalIo` | Write/flush/move/pointer/CAS 단계별 one-shot failure | Production success path를 대체하지 않음 |
| `LegacyPlaceholderSourceOracle` | Current Java AST/import/call facts: hidden defaults, UUID/time seed, GCS listing | Runtime/cloud deployment evidence로 승격 금지 |

`P08_LOCAL_PD_3_TEST_ONLY`는 Phase 07 `P07_TINY_PD_3_TEST_ONLY`와 같은 semantic authority를 소비하도록 compatibility를 맞출 수 있지만, 별도 local external input/profile/run document와 digest를 갖는다. Approved Phase 07 fixture를 변경해 Phase 08 expected 결과에 맞추지 않는다.

Expected semantic oracle:

```text
real E2E:
  accepted Phase 01~06 fixture result
  → actual Phase 07 both-gate output
  → independent application state/pointer assertions

application unit:
  known typed Phase07Output
  → expected legal state sequence
  → exact artifact/result publication call set
  → exact safe external outcome
```

Full E2E에서 exact final result fingerprint/digest literal은 predecessor accepted fixture/evidence에서 전달받은 값을 사용한다. 현재 값이 없으므로 이 문서가 임의 digest, route 또는 objective를 만들지 않는다.

### 11.3 Architecture and public-contract tests

| Exact test method | Fixture/oracle | Expected |
|---|---|---|
| `Phase08ApplicationArchitectureTest.applicationDependsOnCoreSolverAndVerificationButNoProviderSdk()` | Maven/bytecode graph | AWS/GCP/Azure/Kubernetes/HTTP SDK ref 0 |
| `Phase08ApplicationArchitectureTest.applicationPortsExposeNoPathUriHttpExchangeEnvironmentOrMutableMap()` | Public signature reflection | Forbidden public type 0 |
| `Phase08ApplicationArchitectureTest.localAdaptersDependInwardAndApplicationNeverDependsOnAdapters()` | Module DAG | Reverse edge/cycle 0 |
| `Phase08ApplicationArchitectureTest.verificationStillDoesNotDependOnApplicationSolverOrAdapters()` | Full reactor graph | Phase 07 independence preserved |
| `Phase08ApplicationArchitectureTest.defaultLocalDistributionContainsNoCloudDatabaseOrRouteSelectionDependency()` | Dependency tree/jdeps | Cloud/DB/vendor ref 0 |
| `Phase08ApplicationArchitectureTest.applicationContainsNoCustomerNameBranchOrProviderLocatorParsing()` | AST/bytecode rules | Violations 0 |
| `ApplicationPortSignatureTest.artifactStatePublicationPortsMatchApprovedPhase09HandoffProjection()` | Approved signature manifest | Field/method semantic drift 0 |
| `ApplicationPortSignatureTest.phase07OutputHandlingIsExhaustiveForPublishableVerificationRejectedAndGateIncomplete()` | Sealed type inspection | Missing branch 0 |
| `ApplicationPortSignatureTest.allIdentityAndContractTypesCarryExplicitVersionWhereRequired()` | Type coverage manifest | Hidden/unversioned identity 0 |
| `ApplicationPortSignatureTest.tenantScopedAccessBindingIsExplicitAndNonAmbient()` | Approved Phase 08/09 access-binding manifest | Tenant-only/global/static/ThreadLocal binding 0 |
| `ApplicationPortSignatureTest.storageOperationFailureCarrierIsExhaustiveAndLossless()` | Approved Phase 08/09 operation/result manifest | Missing/denied/corrupt/stale/indeterminate collapse 0 |

### 11.4 Submission, identity and state tests

| Exact test method | Fixture/builder | Independent oracle | Expected |
|---|---|---|---|
| `SubmissionIdempotencyTest.newSubmissionCreatesOneSolveAndInitialState()` | Explicit command | State oracle | One `Created`, `SUBMITTED` v1 |
| `SubmissionIdempotencyTest.sameKeyAndSameCommandReturnsExistingSolve()` | Same bytes/semantic config | Call/state counts | Exact same solve/fingerprint; create count 1 |
| `SubmissionIdempotencyTest.sameKeyAndDifferentInputDigestConflicts()` | One-field input mutation | Command fingerprint oracle | Conflict, no new solve/artifact |
| `SubmissionIdempotencyTest.sameKeyAndDifferentProfileOrManifestConflicts()` | Profile/work/seed mutations | Inclusion manifest | Each conflicts |
| `SubmissionIdempotencyTest.absolutePathMtimeEnvironmentAndClockDoNotAffectCommandFingerprint()` | Two workspaces/process metadata | Semantic projection oracle | Same fingerprint |
| `SubmissionIdempotencyTest.missingExplicitSeedWorkLimitOrWatchdogIsRejectedWithoutDefault()` | Missing-field config variants | Required field manifest | Rejected before solve |
| `ApplicationStateTransitionTest.followsExactHappyPathToSucceeded()` | Publishable fixture | State oracle | Exact ordered states and CAS versions |
| `ApplicationStateTransitionTest.rejectsSkipFromSolvingDirectlyToPublishing()` | Illegal transition | State oracle | `IllegalStateTransition`; no pointer |
| `ApplicationStateTransitionTest.staleCasReloadsAndNeverFallsBackToLastWriteWins()` | Two contenders | Version oracle | One CAS, loser re-evaluates |
| `ApplicationStateTransitionTest.terminalDifferentMeaningOrDigestConflicts()` | Succeeded/cancelled/different result race | Terminal oracle | Exactly one terminal meaning |

### 11.5 Phase 07 consumption and publication tests

| Exact test method | Phase 07 fixture | Oracle | Expected |
|---|---|---|---|
| `Phase07ApplicationIntegrationTest.publishableResultIsPersistedVerifiedThenPublished()` | Publishable test-only envelope | Expected call/state sequence | Artifact re-read before one publication CAS |
| `Phase07ApplicationIntegrationTest.candidateStageRejectionNeverPublishesNormalPayload()` | Candidate rejection | Safe output allowlist | `PUBLICATION_REJECTED`, publish calls 0 |
| `Phase07ApplicationIntegrationTest.resultStageRejectionNeverPublishesNormalPayload()` | Result rejection | Safe output allowlist | `PUBLICATION_REJECTED`, publish calls 0 |
| `Phase07ApplicationIntegrationTest.rejectionPreservesStageDispositionAndSafeFailure()` | All Phase 07 rejection variants | Typed tuple oracle | No generic-loss mapping |
| `Phase07ApplicationIntegrationTest.candidateGateIncompleteMapsToInterruptedWithoutPublication()` | Candidate-stage `GateIncomplete` | State/outcome oracle | `VERIFICATION_INCOMPLETE`, publish calls 0 |
| `Phase07ApplicationIntegrationTest.resultGateIncompleteMapsToInterruptedWithoutPublication()` | Result-stage `GateIncomplete` | State/outcome oracle | `VERIFICATION_INCOMPLETE`, publish calls 0 |
| `Phase07ApplicationIntegrationTest.gateIncompletePreservesReasonCandidatePassAndLastSafeWithoutDisposition()` | All incomplete variants | Exact Phase 07 tuple oracle | No fabricated disposition/failure/payload |
| `Phase07ApplicationIntegrationTest.missingCandidateOrResultPassRejectsBeforePublication()` | Tampered publishable fixture | Gate oracle | Integrity rejection, publisher calls 0 |
| `Phase07ApplicationIntegrationTest.sameEnvelopeIdentityDifferentPayloadBytesIsIntegrityViolation()` | One-byte mutation | Independent bytes digest | No overwrite/publication |
| `Phase07ApplicationIntegrationTest.exceptionIsNeverConvertedToAllUnassignedOrSucceeded()` | Throwing Phase 07 fake | Outcome sealed inspection | Internal failure only |
| `ResultRetrievalTest.returnsResultOnlyThroughPublishedPointerAndVerifiedRef()` | Published/unreferenced artifacts | Exact pointer oracle | Unreferenced payload unavailable |
| `ResultRetrievalTest.neverUsesDirectoryListingOrNewestFileAsResultAuthority()` | Decoy newer files | Exact key oracle | Exact published digest only |

### 11.6 Artifact, state and publication port contract tests

모든 fake/local 구현은 같은 abstract contract suite를 실행한다. Phase 09 object backends가 이 suite를 재사용/확장한다.

| Exact test method | Fault/scenario | Expected |
|---|---|---|
| `ArtifactStoreContract.putIfAbsentCreatesAndReadVerifiedReturnsExactBytes()` | New key/content | Exact ref/digest/length/bytes |
| `ArtifactStoreContract.sameKeySameDigestIsIdempotent()` | Duplicate exact put | `AlreadyPresent`, overwrite 0 |
| `ArtifactStoreContract.sameKeyDifferentDigestConflicts()` | One-byte content change | Conflict, original bytes unchanged |
| `ArtifactStoreContract.declaredDigestDifferentFromContentIsRejected()` | Forged digest | Artifact absent |
| `ArtifactStoreContract.readVerifiedRejectsMetadataOrByteCorruption()` | Length/byte mutation | Integrity failure before deserialize |
| `ArtifactStoreContract.opaqueLocatorCannotChangeSemanticIdentity()` | Two adapter roots | Same logical/content identity |
| `ArtifactStoreContract.tenantScopeCannotCrossReadOrWrite()` | Same textual IDs, two tenants | Isolation exact |
| `ArtifactStoreContract.missingOrMismatchedAccessBindingFailsBeforeExistenceDisclosure()` | No/wrong tenant-scoped session | Denied, read/metadata/write 0 |
| `RunStateRepositoryContract.createIfAbsentIsIdempotentOnlyForSameInitialState()` | Same/different initial | Same converges, different conflicts |
| `RunStateRepositoryContract.compareAndSetAdvancesExactlyOneOpaqueVersion()` | Expected current | One success, exact next |
| `RunStateRepositoryContract.staleVersionNeverOverwritesCurrentState()` | Concurrent contenders | One winner, loser conflict |
| `RunStateRepositoryContract.rejectsIllegalTransitionEvenWithCurrentVersion()` | Valid CAS token, illegal state | No write |
| `ResultPublisherContract.publishesOnlyBothGateReferenceAtExpectedState()` | Valid publishable | One published pointer |
| `ResultPublisherContract.sameDigestDuplicateConverges()` | Duplicate call | `AlreadyPublished`, same ref |
| `ResultPublisherContract.differentDigestDuplicateConflicts()` | Different result | Existing pointer unchanged |
| `ResultPublisherContract.runStateFenceAndPublicationPreconditionAreDistinct()` | State/pointer preconditions independently varied | Token reuse/implicit precondition 0 |
| `ResultPublisherContract.cancellationPublicationRaceHasOneTerminalWinner()` | Controlled barrier | Succeeded XOR cancelled |

### 11.7 Filesystem, crash and security tests

| Exact test method | Fixture/fault | Oracle | Expected |
|---|---|---|---|
| `FileArtifactStoreContractTest.runsAllArtifactStoreContractCases()` | Fresh explicit workspace | Abstract contract | All inherited cases green |
| `VersionedFileRunStateRepositoryContractTest.runsAllStateContractCases()` | Fresh explicit workspace | Abstract contract | All inherited cases green |
| `AtomicFileResultPublisherContractTest.runsAllPublisherContractCases()` | Fresh explicit workspace | Abstract contract | All inherited cases green |
| `LocalFilesystemCrashSafetyTest.failureBeforeAtomicMoveLeavesNoReferencedPartialArtifact()` | Write/flush fault | Independent tree/pointer reader | No pointer to partial |
| `LocalFilesystemCrashSafetyTest.failureAfterArtifactBeforePointerLeavesUnreferencedArtifactOnly()` | Pointer fault | State oracle | Prior state/result authoritative |
| `LocalFilesystemCrashSafetyTest.retryAfterResponseLossConvergesToSamePublishedReference()` | Publish success then response loss | Exact pointer oracle | Same result, no overwrite |
| `LocalFilesystemCrashSafetyTest.bootstrapRejectsFilesystemWithoutRequiredAtomicSemantics()` | Capability fake | Bootstrap oracle | Fail closed, fallback 0 |
| `LocalFilesystemCrashSafetyTest.declaresLocalSingleJvmAndNeverClaimsCrossProcessOrDistributedCas()` | Capability/evidence manifest | Scope oracle | Exact `LOCAL_SINGLE_JVM` |
| `LocalWorkspaceSecurityTest.rejectsTraversalAbsoluteSeparatorControlAndUnicodeAmbiguity()` | Attack builder | Workspace containment oracle | Outside access/write 0 |
| `LocalWorkspaceSecurityTest.rejectsSymlinkEscapeAtEveryPathComponent()` | Symlink attack variants | Real path oracle | Outside access/write 0 |
| `LocalWorkspaceSecurityTest.tenantWithSameTextualArtifactIdCannotCrossRead()` | Two tenant trees | Exact typed key | Isolation exact |
| `LocalWorkspaceSecurityTest.tenantIdWithoutAuthorizedBindingCannotReadWriteOrQuery()` | Caller-asserted tenant only | Access oracle | Existence leak and port call 0 |
| `LocalWorkspaceSecurityTest.safeFailureContainsNoAbsolutePathInputSecretOrStackTrace()` | All failure paths | Denylist oracle | Leak count 0 |
| `LocalInputLimitTest.rejectsDeclaredOrStreamingBytesBeyondExplicitLimit()` | Limit-1/limit/limit+1 | Independent byte counter | Boundary exact, no truncate |
| `LocalInputLimitTest.rejectsDepthStringCollectionAndNumericTokenLimit()` | One boundary per input | Parser-independent token oracle | Typed limit rejection |
| `LocalInputLimitTest.missingLimitIsRejectedRatherThanDefaulted()` | Omitted config field | Required field manifest | Bootstrap fail |

### 11.8 Cancellation, deadline, retry and telemetry tests

| Exact test method | Fixture/fault | Expected |
|---|---|---|
| `CancellationSemanticsTest.sameCancellationIntentIsIdempotent()` | Duplicate same intent | Same receipt/terminal view |
| `CancellationSemanticsTest.intentAndActualWorkerTerminationAreDistinct()` | Barrier before worker observes | `CANCEL_REQUESTED` then `CANCELLED` |
| `CancellationSemanticsTest.cancellationDiscardsUncommittedCowTrialAndPreservesLastSafePoint()` | Cancel inside trial | Candidate fingerprint at last commit unchanged |
| `CancellationSemanticsTest.cancelAfterSucceededDoesNotDeleteOrRewriteResult()` | Terminal success | Same published ref |
| `CancellationSemanticsTest.cancelAndPublishRaceProducesExactlyOneTerminalMeaning()` | Deterministic barrier | XOR terminal; no partial payload |
| `DeadlineSemanticsTest.watchdogDoesNotBecomeMaxStepsReached()` | Fake monotonic clock | `WATCHDOG_REACHED`, normal termination false |
| `DeadlineSemanticsTest.incompleteStepDoesNotAdvanceCompletedWorkOrAdaptiveState()` | Deadline inside step | Counts/state unchanged |
| `DeadlineSemanticsTest.transportTimeoutAfterPublishRetryReturnsExistingPublishedResult()` | Response-loss fault | Same published ref |
| `DeadlineSemanticsTest.clockElapsedNeverChangesSeedComparatorOrResultFingerprint()` | Clock permutations | Exact semantic result equality |
| `WorkerRetryIdentityTest.retryChangesAttemptOnly()` | Platform start failure | Worker/seed/work/warm start unchanged |
| `WorkerRetryIdentityTest.sameStrongReplayIdentityDifferentVerifiedDigestIsIntegrityViolation()` | Divergent fake output | Arbitrary selection 0 |
| `TelemetryContractTest.emitsRequiredHappyPathEventsWithSafeCorrelation()` | Successful unit flow | Required set exact |
| `TelemetryContractTest.emitsConflictCancelDeadlineAndRejectionEvents()` | Failure matrix | Event/disposition exact |
| `TelemetryContractTest.redactsRawInputPiiSecretPathAndStackTrace()` | Malicious values | Leak count 0 |
| `TelemetryContractTest.elapsedAndEventOrderDoNotChangeSemanticResult()` | Two schedules | Result fingerprint/digest same |

### 11.9 CLI and real local E2E tests

| Exact test method | Fixture/config | Independent oracle | Expected |
|---|---|---|---|
| `LocalCliConfigurationTest.requiresConfigAuthorizationWorkspaceInputProfileVersionPresetSeedWorkLimitsWatchdogAndAdapterScope()` | Missing each field | Required field manifest | Every omission rejected |
| `LocalCliConfigurationTest.environmentCurrentDirectoryHomeLocaleTimezoneCannotSupplyMissingFields()` | Process permutations | Explicit config oracle | Missing stays failure |
| `LocalCliEndToEndTest.explicitFixtureRunsCanonicalizeSolveBothGatePublishAndRetrieve()` | `P08_LOCAL_PD_3_TEST_ONLY` | Accepted Phase 07 + state/pointer oracle | Exact stage lineage, `SUCCEEDED` |
| `LocalCliEndToEndTest.sameManifestInTwoFreshWorkspacesProducesSameCanonicalResultIdentity()` | Two workspace roots | Semantic equality projection | Result/payload exact; locators may differ |
| `LocalCliEndToEndTest.sameWorkspaceRetryConvergesWithoutOverwrite()` | Same idempotency command | Tree/call-count oracle | Existing refs, no duplicate mutation |
| `LocalCliEndToEndTest.differentInputWithSameSubmissionConflictsBeforeSolve()` | One-byte input mutation | Command oracle | Solver/Phase 07/publisher calls 0 |
| `LocalCliEndToEndTest.phase07RejectedFixtureReturnsNoNormalResult()` | Real accepted rejection path | Safe output oracle | Non-success, result pointer absent |
| `LocalCliEndToEndTest.statusAndResultUseExactSolveIdWithoutDirectoryScan()` | Decoy solve/artifact | Exact query oracle | Requested solve only |
| `LocalCliEndToEndTest.ctrlCRequestsCooperativeCancellationAndReportsLastSafePoint()` | Controlled interrupt | State oracle | Intent/actual termination separated |
| `LocalCliEndToEndTest.inputLimitFailureOccursBeforeCanonicalizationAndLeavesNoSolveResult()` | Explicit small limit | Call/tree oracle | Parser/solver/publisher calls 0 |
| `LocalCliEndToEndTest.structuredLogsContainRequiredEventsAndNoSensitiveData()` | Successful and failed flows | Telemetry oracle | Required events, leak 0 |

Full `LocalCliEndToEndTest`는 Phase 01~07 actual accepted implementation과 fixture가 있어야 green이 될 수 있다. Stub Phase 07을 사용한 test는 application integration이며 `E-P08-LOCAL-E2E`로 제출하지 않는다.

### 11.10 Legacy characterization and migration tests

| Exact test method | Authority | Expected |
|---|---|---|
| `LegacyPlaceholderSourceCharacterizationTest.recordsSingleRootPomAndDirectGcpDependencies()` | Maven model/source AST | §4 inventory exact |
| `LegacyPlaceholderSourceCharacterizationTest.recordsPortAndServiceModeDefaults()` | `OptimizationHttpServer` AST | `PORT=8080`, `SERVICE_MODE=api` legacy-only |
| `LegacyPlaceholderSourceCharacterizationTest.recordsUuidNanoTimeAndParameterClampDefaults()` | API controller AST | §4.2 tuple exact; target-default flag false |
| `LegacyPlaceholderSourceCharacterizationTest.recordsGsOnlyWorkflowAndGcsResultLookup()` | API controller AST | Current boundary exact |
| `LegacyPlaceholderSourceCharacterizationTest.recordsPrefixListingAndRawDoubleMinimumFinalize()` | Worker controller AST | Listing/raw comparator risk exact |
| `LegacyPlaceholderSourceCharacterizationTest.recordsMissingCancellationIdempotencyVerifierAndInputLimitSeams()` | Contract/AST inventory | Missing facts recorded, no completion claim |
| `LegacyMigrationBoundaryTest.newLocalDistributionDoesNotModifyOrInvokeLegacyGcpControllers()` | Module dependency/runtime spy | New path calls legacy 0 |
| `LegacyMigrationBoundaryTest.shadowComparisonCannotPublishLegacyOrMismatchedResultAsTarget()` | Side-effect-free comparison fixture | Publication calls 0 |
| `LegacyMigrationBoundaryTest.rollbackDisablesNewLocalEntrypointWithoutChangingLegacyArtifact()` | Isolated distributions/digests | Legacy source/artifact fingerprint unchanged |

AST/source characterization은 실제 deployment/runtime behavior evidence가 아니다. Black-box legacy HTTP/GCP test가 필요하면 isolated project/emulator, captured request/response와 no-production-side-effect approval을 별도 evidence로 추가한다. Cloud call을 fake success로 만든 unit test만으로 operational compatibility를 주장하지 않는다.

### 11.11 Red → green order and applicable layers

| 순서 | 먼저 red로 고정할 것 | 최소 green 조건 | Applicable layer |
|---:|---|---|---|
| 1 | Module DAG와 forbidden public type | Provider/transport/path edge 0 | Architecture |
| 2 | Identity/version/command projection | Same/same convergence, same/different conflict | Unit/contract |
| 3 | Legal state table와 typed failure | Illegal skip 0, rejection detail 보존 | Unit/property |
| 4 | Artifact/state/publication abstract ports | Put/read/digest/CAS/publish semantics exact | Port contract |
| 5 | Phase 07 exhaustive output handling | Publishable only after both PASS; rejection payload 0 | Module integration |
| 6 | Local filesystem/security/crash | Outside access 0, pointer-to-partial 0 | Adapter integration/fault |
| 7 | Cancel/deadline/retry/telemetry | Termination separation and safe events exact | Application/fault |
| 8 | Explicit CLI config and limits | Hidden source/default 0 | Adapter/security |
| 9 | Stubbed application end-to-end | Exact call/state/publication sequence | Application integration |
| 10 | Real Phase 01~07 local E2E | Request→both-gate→retrieval exact | Full local integration |
| 11 | Fresh-workspace/idempotent rerun | Canonical result/digest exact | Reproducibility |
| 12 | Legacy characterization/rollback | Snapshot exact, cross-call/write 0 | Migration |
| 13 | Full reactor/evidence/review | Required failure/error/skipped 0 | System/review |

Green 순서를 맞추려고 Phase 07 fake를 full E2E로 이름 바꾸거나, filesystem adapter를 in-memory fake로만 대체하거나, missing test를 `surefire.failIfNoSpecifiedTests=false`로 숨기지 않는다.

## 12. Ordered work packages

### WP-08.0 — Entry revalidation, predecessor receipt and legacy characterization

- **Prerequisites**
  - Scheduler task/owners/reviewer assigned.
  - Phase 00 architecture artifact가 inspect 가능.
  - §1 canonical source fingerprints와 cited adjacent contract semantic-impact review 실행 가능.
  - Legacy checkout/source가 read-only characterization 가능한 상태.
- **Change targets**
  - Phase 08 contract/architecture test skeleton.
  - Phase 07 handoff compatibility receipt.
  - `LegacyPlaceholderSourceOracle`와 characterization tests.
  - Entry evidence manifest; 공용 progress/status는 scheduler 소유.
- **Concrete tasks**
  1. Canonical 4문서, question register와 Master Plan fingerprint를 재검증한다.
  2. Phase 07/09 cited contract section을 다시 읽고 semantic diff와 source→contract→test impact를 review한다.
  3. `Phase07HandoffManifest`의 field/version/evidence/ref를 §3.2와 대조한다.
  4. Root POM, Java source 6개, test 1개, Docker/GCP files를 AST/Maven model로 characterization한다.
  5. Legacy facts를 target default/contract와 분리한 compatibility matrix와 rollback owner를 고정한다.
  6. Public wire schema, fingerprint algorithm, exact exit code 같은 proposed/open 항목을 decision manifest에 표시한다.
- **Verification commands**

  ```bash
  shasum -a 256 \
    docs/master-design.md \
    docs/deprecated/2026-07-26-domain-design.md \
    docs/deprecated/2026-07-26-architecture-design.md \
    docs/deprecated/architecture-domain-implementation-design.md \
    docs/deprecated/master-design-open-questions.md \
    docs/implementation/master-realization-plan.md

  ./mvnw -B -ntp -Dstyle.color=never \
    -f build/architecture-rules/pom.xml \
    -Dtest=LegacyPlaceholderSourceCharacterizationTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```
- **Exact tests / expected**
  - §11.10의 여섯 `LegacyPlaceholderSourceCharacterizationTest.*` method가 discovered/green.
  - Phase 07 exact variant/field contract가 accepted handoff/evidence receipt와 의미적으로 일치.
  - Inventory item은 actual/present 또는 missing으로만 판정하며 runtime/deployment success claim 0.
- **Failure and rollback**
  - Source snapshot drift, Phase 07 handoff missing field/evidence, unreviewed legacy change가 있으면 implementation code를 시작하지 않는다.
  - Last safe point는 이 document와 read-only inventory다.
  - Characterization이 runtime behavior를 추측하거나 hidden legacy value를 target default로 올리면 report/test를 폐기하고 AST/source facts부터 다시 작성한다.
- **Handoff**
  - Approved entry receipt, legacy compatibility matrix, proposed/open decision list를 WP-08.1에 전달한다.

### WP-08.1 — Provider-neutral types, ports, identity and state contract

- **Prerequisites**
  - WP-08.0 entry receipt green.
  - Phase 00 module/package conventions accepted.
  - Phase 07 `Phase07Output` public contract available.
- **Change targets**
  - `rpdptw/application` port/in, port/out, api, identity, execution packages.
  - `build/architecture-rules` Phase 08 rules.
  - `build/port-contract-tests` abstract contract skeleton.
- **Concrete tasks**
  1. §7.1~§7.6의 record/sealed interface/signature를 accepted predecessor type에 맞춰 구현한다.
  2. Submission/solve/worker/attempt/artifact/state/publication identity와 version projection을 고정한다.
  3. Legal state table와 `ApplicationFailure`/retry disposition을 explicit exhaustive switch로 구현한다. Phase 07 내부 substage를 application state로 가장하지 않는다.
  4. `ArtifactStore`, `RunStateRepository`, `ResultPublisher`, profile/dispatch/cancel/telemetry/clock port를 application 쪽에 정의한다.
  5. Public signatures에서 `Path`, URI, HTTP/Jackson, mutable map/bytes와 provider type을 제거한다.
  6. Phase 09 signature manifest와 Phase 10 reserved seam을 작성하되 backend/coordinator code를 추가하지 않는다.
  7. Phase 08/09 공동 review가 승인한 explicit non-ambient tenant-scoped access binding만 사용한다. 미승인 상태에서는 tenant-isolation port implementation을 시작하지 않는다.
  8. Phase 08/09가 승인한 operation별 lossless storage failure carrier만 구현한다. 미승인 상태에서는 generic exception 또는 `AdapterUnavailable` collapse로 진행하지 않는다.
- **Verification commands**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f build/architecture-rules/pom.xml \
    -Dtest=Phase08ApplicationArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/application/pom.xml \
    -Dtest=ApplicationPortSignatureTest,SubmissionIdempotencyTest,ApplicationStateTransitionTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```
- **Exact tests / expected**
  - §11.3의 11 methods와 §11.4의 10 methods 모두 discovered/green.
  - Provider/transport/path/mutable public edge 0.
  - Same/same exact convergence, same/different conflict, illegal transition write 0.
  - Required version/identity coverage gap 0.
- **Failure and rollback**
  - Application interface가 provider/transport/path를 요구하거나 Phase 07 output을 raw map으로 바꿔야 하면 WP 전체를 revert한다.
  - Phase 07 accepted contract와 WP-08.0 decision manifest가 last safe point다.
  - Java 이름 변경은 허용하되 signature manifest와 tests를 같은 change에서 갱신한다. Semantic guard 약화는 허용하지 않는다.
- **Handoff**
  - Approved application API, state/failure table, Phase 09 port signature projection을 WP-08.2/3에 전달한다.

### WP-08.2 — Application pipeline and exhaustive Phase 07 output handling

- **Prerequisites**
  - WP-08.1 port/state tests green.
  - Phase 01~06 service contracts available.
  - Phase 07 accepted implementation and handoff receipt available for integration; stub은 unit 시작에만 사용.
- **Change targets**
  - `DefaultSubmitSolveService`, `DefaultLocalSolveService`, retrieval service.
  - Versioned canonical input/preparation/profile/solver/Phase 07 composition.
  - Phase 07 application integration and retrieval tests.
- **Concrete tasks**
  1. §7.7 pseudocode 순서대로 request→canonicalization→preparation/binding→solve→Phase 07→publication을 조립한다.
  2. Snapshot/manifest/candidate/replay/result artifact를 immutable put + verified read로 연결한다.
  3. Phase 07 `Publishable`, `Rejected(VerificationRejected)`,
     `Rejected(GateIncomplete)` branches를 exhaustive하게 처리한다.
     `GateIncomplete`는 `VERIFICATION_INCOMPLETE`/`Interrupted`로 매핑하고 존재하지 않는
     disposition을 합성하지 않는다.
  4. Candidate/result 두 PASS와 exact identities가 없으면 publisher call 전에 reject한다.
  5. Retrieval은 exact published pointer/ref만 사용하고 unreferenced/decoy artifact를 무시한다.
  6. Search/domain/result meaning을 application에서 재해석하거나 normal failure payload를 만들지 않는다.
- **Verification command**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/application/pom.xml \
    -Dtest=Phase07ApplicationIntegrationTest,ResultRetrievalTest,ApplicationStateTransitionTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```
- **Exact tests / expected**
  - §11.5의 12 methods와 happy/illegal transition methods green.
  - Both-gate publishable path에서 publisher call exactly 1.
  - Candidate/result rejection와 GateIncomplete, missing PASS, corrupted bytes, exception path에서 publisher call 0과 normal payload 0.
  - Retrieval listing/newest-file call 0.
- **Failure and rollback**
  - Stub-only path가 real Phase 07 compatibility를 가리거나 application이 final result를 다시 encode/derive하면 service integration을 revert한다.
  - WP-08.1 pure types/ports와 accepted Phase 07 result artifact가 last safe point다.
  - Persisted immutable artifact가 생겨도 authoritative pointer가 없으면 unpublished로 남기고 정상 result로 복구 추론하지 않는다.
- **Handoff**
  - Exact application call graph, typed outcome, artifact/state transition sequence와 `E-P08-PORT` draft를 WP-08.3/6에 전달한다.

### WP-08.3 — Local artifact/state/publication adapters and crash safety

- **Prerequisites**
  - WP-08.1 approved storage semantics.
  - Explicit workspace capability requirements/security owner review.
  - Phase 08/09 approved non-ambient tenant-scoped access binding/session contract.
  - Phase 08/09 approved operation별 lossless storage failure carrier.
  - Accepted Phase 08 handoff 전 object-common/provider work를 시작하지 않음.
- **Change targets**
  - `adapters/object-filesystem`.
  - Abstract artifact/state/publisher contract implementations.
  - Path security, crash/fault injection tests.
- **Concrete tasks**
  1. Typed key safe encoding, containment, no-symlink, classification과 exact workspace bootstrap을 구현한다.
  2. Immutable temp→flush→digest→create-once atomic move와 verified read를 구현한다.
  3. Versioned immutable state + opaque current pointer CAS를 구현한다.
  4. Both-gate result artifact 이후 create-once/CAS published pointer를 구현한다.
  5. Same/same idempotency, same/different conflict와 stale-version reload를 구현한다.
  6. Write/flush/move/state/publish/response-loss fault injection에서 last safe pointer를 검증한다.
  7. File lock/atomic primitive는 adapter detail로 격리하고 지원하지 않는 filesystem을 fail closed한다.
- **Verification commands**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f adapters/object-filesystem/pom.xml \
    -Dtest=FileArtifactStoreContractTest,VersionedFileRunStateRepositoryContractTest,AtomicFileResultPublisherContractTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f adapters/object-filesystem/pom.xml \
    -Dtest=LocalFilesystemCrashSafetyTest,LocalWorkspaceSecurityTest,LocalInputLimitTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```
- **Exact tests / expected**
  - §11.6 abstract methods가 세 concrete suites에서 모두 실행된다.
  - §11.7 16 concrete methods discovered/green.
  - Pointer-to-partial count 0, outside-workspace read/write 0, stale overwrite 0.
  - Required atomic capability 없는 filesystem에서 fallback success 0.
- **Failure and rollback**
  - Atomic semantics를 증명할 수 없거나 symlink/race가 containment를 우회하면 local adapter를 supported로 표시하지 않는다.
  - In-memory fake와 WP-08.1 port contract가 last safe point이며 filesystem evidence를 폐기한다.
  - Partial/unreferenced files는 publish하지 않고 fault evidence로 보존/정리한다. Existing pointer를 되돌려 쓰지 않는다.
- **Handoff**
  - Local adapter contract report, crash matrix, filesystem assumptions와 Phase 09 extension gaps를 WP-08.4/7에 전달한다.

### WP-08.4 — Explicit CLI/config, limits, security and local composition

- **Prerequisites**
  - WP-08.2 application service와 WP-08.3 local adapters green.
  - Security/limit reviewer assigned.
  - Test-only execution/limit/watchdog values clearly classified.
- **Change targets**
  - `apps/cli`, `distributions/local`, `adapters/common` bounded input mapper.
  - Explicit config parser/validator, composition root, safe output/error mapper.
  - CLI configuration, input limits, path/redaction tests.
- **Concrete tasks**
  1. `solve/status/result` CLI와 blocking `solve`의 cooperative interrupt를 동일 inbound use cases에 연결한다. 별도-process `cancel` subcommand는 승인된 cross-process scope/evidence 전 구현하지 않는다.
  2. §8.1 required config와 approved local authorization binding field가 하나라도 없으면 bootstrap 전에 typed failure로 거부한다.
  3. File metadata + bounded stream + parser token/depth/string/numeric limits를 순서대로 구현한다.
  4. Local distribution에 filesystem adapter만 선택하고 cloud/DB/workflow/vendor dependency를 넣지 않는다.
  5. Process environment/CWD/home/locale/timezone에서 missing semantic/runtime config를 보완하지 않는다.
  6. Stdout/stderr, structured telemetry와 safe CLI failure mapping을 분리한다.
  7. Optional HTTP는 별도 scope 승인 없으면 구현하지 않는다.
- **Verification commands**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f apps/cli/pom.xml \
    -Dtest=LocalCliConfigurationTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f distributions/local/pom.xml \
    -Dtest=LocalInputLimitTest,LocalWorkspaceSecurityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f build/architecture-rules/pom.xml \
    -Dtest=Phase08ApplicationArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```
- **Exact tests / expected**
  - §11.9의 configuration 2 methods와 §11.7 limit/security methods green.
  - Required field omission variants 모두 failure, fallback/default source 0.
  - Local dependency tree에서 cloud/DB/route-selection SDK 0.
  - Safe failure leak count 0.
- **Failure and rollback**
  - Config omission이 default로 통과하거나 unbounded parser/read가 발견되면 CLI distribution을 release하지 않는다.
  - WP-08.2 application API와 WP-08.3 adapter contract가 last safe point다.
  - Misconfigured workspace에서 artifact/state write가 시작됐으면 pointer가 없는지 검사하고 incident evidence를 보존한다.
- **Handoff**
  - Explicit config schema/version, local composition manifest와 security/limit report를 WP-08.5/6에 전달한다.

### WP-08.5 — Cancellation, deadline, retry and telemetry fault semantics

- **Prerequisites**
  - WP-08.1 state/failure types, WP-08.2 service, WP-08.4 composition green.
  - Injected monotonic clock/cancellation probe/fault hooks available.
- **Change targets**
  - Cancellation service/port/local intent adapter.
  - `RunDeadline`, retry/attempt mapping, telemetry recorder.
  - Race/fault/redaction tests.
- **Concrete tasks**
  1. Intent→observation→actual termination→last-safe-point sequence를 구현한다.
  2. Cancellation/publish and stale CAS races를 deterministic barrier test로 고정한다.
  3. Algorithm completed steps, application watchdog, transport/platform timeout을 별도 type/state로 유지한다.
  4. Retry에서 `AttemptId`만 바뀌고 worker/seed/work/warm start가 보존되는지 guard한다.
  5. Required event/correlation/metric과 safe-field allowlist/redaction denylist를 구현한다.
  6. Telemetry failure policy를 explicit config로 적용하며 semantic result에 영향이 없음을 검증한다.
- **Verification command**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f rpdptw/application/pom.xml \
    -Dtest=CancellationSemanticsTest,DeadlineSemanticsTest,WorkerRetryIdentityTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f distributions/local/pom.xml \
    -Dtest=TelemetryContractTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```
- **Exact tests / expected**
  - §11.8의 15 methods discovered/green.
  - Cancelled/watchdog/platform timeout→normal termination conversion 0.
  - Incomplete-step count/adaptive mutation 0.
  - Race에서 terminal/published pointer winner exactly 1.
  - Sensitive telemetry leak 0; clock/event order에 따른 semantic digest 변화 0.
- **Failure and rollback**
  - Cooperative stop이 COW discard/last safe point를 증명하지 못하거나 process timeout을 정상 result로 추론하면 cancellation/deadline support를 incomplete로 표시한다.
  - Last green WP-08.2/4 state와 unpublished immutable artifact가 last safe point다.
  - Fault hook/test-only clock을 production runtime에 노출하지 않는다.
- **Handoff**
  - Idempotency/cancel/deadline/retry fault matrix와 telemetry/redaction report를 WP-08.6에 전달한다.

### WP-08.6 — Real local E2E, deterministic rerun and retrieval

- **Prerequisites**
  - WP-08.0~5 required tests green.
  - Phase 01~07 implementations/evidence/reviews accepted.
  - `P08_LOCAL_PD_3_TEST_ONLY` and predecessor expected oracle approved as test-only.
- **Change targets**
  - `LocalCliEndToEndTest`.
  - Fresh-workspace replay harness.
  - E2E evidence recorder and exact status/result retrieval.
- **Concrete tasks**
  1. Empty explicit workspace에서 full request→canonicalization→solve→both-gate→publish→retrieve를 실행한다.
  2. 모든 stage artifact/state/telemetry/ref가 exact lineage로 이어지는지 검사한다.
  3. 두 fresh workspace에서 same manifest를 실행해 semantic/result/payload identity를 비교한다.
  4. Same workspace retry, different input conflict, rejection, cancel, limit, decoy retrieval cases를 실행한다.
  5. Stubbed Phase 07과 real Phase 07 evidence를 분리하고 full evidence에는 real path만 사용한다.
  6. Observation/path/process differences가 semantic fingerprint에 포함되지 않는 inclusion manifest를 제출한다.
- **Verification commands**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f distributions/local/pom.xml \
    -Dtest=LocalCliEndToEndTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean verify

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/application,adapters/object-filesystem,apps/cli,distributions/local \
    -am clean verify
  ```
- **Exact tests / expected**
  - §11.9의 9 `LocalCliEndToEndTest.*` methods 모두 discovered/green; 2 configuration methods는 WP-08.4 report와 함께 bundle에 포함.
  - Full success trace에 candidate/result PASS 둘 다 존재.
  - Fresh-workspace canonical result fingerprint/payload digest/length exact equality.
  - Rejection/cancel/limit/conflict case에 published pointer와 normal payload 0.
  - Same workspace retry overwrite 0, exact existing reference.
  - Required test failure/error/skipped 0.
- **Failure and rollback**
  - Result differs without declared semantic input difference, real Phase 07 path is bypassed, or retrieval depends on directory scan이면 `E-P08-LOCAL-E2E`를 발행하지 않는다.
  - Last green application/adapter module artifacts and Phase 07 accepted bundle are last safe points.
  - Failed workspace는 immutable debug artifacts와 pointer 상태를 evidence로 보존하고 정상 result로 재label하지 않는다.
- **Handoff**
  - `E-P08-LOCAL-E2E` candidate bundle, exact config/fixture/oracle digest와 rerun comparison을 WP-08.7에 전달한다.

### WP-08.7 — Migration boundary, full evidence, independent review and Phase 09/10 handoff

- **Prerequisites**
  - WP-08.0~6 all required tests green.
  - Legacy/new distributions isolated.
  - Independent reviewer and Phase 09/10 consumers assigned.
- **Change targets**
  - Legacy migration/rollback tests.
  - Architecture/evidence manifests, review input.
  - Phase 09 storage contract and Phase 10 execution seam handoff.
  - 공용 README/plan/progress 갱신은 scheduler/해당 owner만 수행.
- **Concrete tasks**
  1. New local distribution이 legacy controllers/GCP clients를 invoke/modify하지 않음을 증명한다.
  2. Side-effect-free shadow comparison과 mismatch no-publication rule을 검증한다.
  3. New local entrypoint disable/rollback 후 legacy source/artifact fingerprint가 불변임을 검증한다.
  4. `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`를 content-addressed bundle로 고정한다.
  5. Exact command/toolchain/test count/failure/skipped/source/build/config/input/output digest와 last safe point를 기록한다.
  6. Phase 09/10 owner가 §16 handoff를 검토하고 compatibility receipt를 서명한다.
  7. Independent Phase 08 review가 `PASS`하기 전 status를 `ACCEPTED`로 승격하지 않는다.
- **Verification commands**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -f build/architecture-rules/pom.xml \
    -Dtest=Phase08ApplicationArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -f distributions/local/pom.xml \
    -Dtest=LegacyMigrationBoundaryTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/application,adapters/object-filesystem,apps/cli,distributions/local \
    -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never clean verify
  ```
- **Exact tests / expected**
  - §11.10의 3 `LegacyMigrationBoundaryTest.*` methods와 all architecture methods green.
  - OR-Tools-free ALNS-only full reactor green; required failed/error/skipped 0.
  - Cloud/DB/vendor dependency in default local/application 0.
  - Three evidence bundle digests complete and independent review `PASS`.
  - Phase 09/10 compatibility receipt present; AWS/object storage/coordinator implementation claim 0.
- **Failure and rollback**
  - Migration test, full reactor, evidence, consumer receipt 또는 independent review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`/handoff ready를 주장하지 않는다.
  - New local distribution을 disable하고 existing legacy source/artifact를 유지한다.
  - Immutable local artifacts는 published pointer 여부를 보존하며 destructive cleanup으로 evidence를 숨기지 않는다.
- **Handoff**
  - §16의 exact manifests와 three evidence bundles를 Phase 09/10 owner 및 scheduler에게 전달한다.

## 13. Verification commands, pass criteria and evidence bundle

### 13.1 Future command matrix

모든 command는 target modules가 구현되고 entry gate가 열린 뒤 실행할 **future command**다. 이 문서 작성 시 실행됐다고 주장하지 않는다.

```bash
java -version
./mvnw -version
```

Expected toolchain:

```text
Java feature release: 25
Maven: 3.9.14 or accepted compatible version under root Enforcer
```

| Layer | Future command | Required pass |
|---|---|---|
| Architecture/signature | WP-08.1 first command | §11.3 all methods; forbidden edge/type 0 |
| Identity/state | WP-08.1 second command | §11.4 all methods; conflict/transition exact |
| Phase 07 integration | WP-08.2 command | §11.5 all methods; failure publication 0 |
| Storage port/local adapter | WP-08.3 commands | Abstract suite + §11.7; corruption/outside write 0 |
| CLI/security/config | WP-08.4 commands | Missing/default fallback 0; leak 0 |
| Cancel/deadline/retry/telemetry | WP-08.5 command | §11.8 all methods; exceptional semantics preserved |
| Real local E2E | WP-08.6 commands | §11.9 all methods; real both-gate path |
| Migration | WP-08.7 first command | Legacy/new cross-call/write 0; rollback exact |
| Application/local module set | WP-08.7 second command | Required module verify green |
| Full reactor | `./mvnw -B -ntp -Dstyle.color=never clean verify` | OR-Tools-free ALNS-only required reactor green |

Selected test 전에 exact implementation source에서
`./mvnw -B -ntp -Dstyle.color=never clean install`을 통과시켜 reactor dependency를 설치한다.
Selected command는 test owner module의 `-f <module>/pom.xml`만 실행하고 `-am`을 사용하지 않으며
`surefire.failIfNoSpecifiedTests=true`로 fail closed한다. `-DskipTests`,
`-Dmaven.test.skip=true`, required class/method disable, `surefire.failIfNoSpecifiedTests=false`만으로
만든 zero-test success, stale `target/`, partial rerun, console summary 한 줄 또는 다른 commit의
report는 exit evidence가 아니다. Selected command마다 다음을 evidence manifest와 대조한다.

```text
requested test class/method set
discovered test class/method set
executed test class/method set
failed/error/skipped/aborted
test runtime classification (unit/contract/integration/fault/E2E)
fixture/config/oracle digest
source/build commit and dirty/untracked inventory
```

### 13.2 Exact pass criteria

Phase 08 exit는 다음 AND 조건이다.

1. All entry predecessor receipt/reviews accepted.
2. §11 exact required tests discovered and executed; failed/error/skipped 0.
3. `rpdptw-application` public/provider/transport/path/DB/vendor forbidden reference 0.
4. Local distribution cloud/DB/route-selection dependency 0.
5. Same submission + same command exact convergence.
6. Same submission + different input/profile/manifest exact conflict before solve.
7. Immutable artifact same/same idempotency and same/different rejection.
8. State/result CAS stale writer overwrite 0.
9. Phase 07 `Rejected` normal result publication/retrieval 0.
10. Missing/tampered candidate or result PASS publication 0.
11. Phase 07 `Publishable` only after artifact re-read/digest and pointer CAS becomes `SUCCEEDED`.
12. Cancellation intent/actual termination/last safe point separate.
13. Watchdog/platform/resource/cancellation normal termination conversion 0.
14. Input limits, path containment, tenant isolation and sensitive data leak failures 0.
15. Real Phase 01~07 local E2E full trace complete.
16. Two fresh workspace reruns have exact semantic result/payload identity.
17. Same workspace retry overwrite 0 and exact existing ref.
18. Legacy/new cross-invocation/write 0 and rollback rehearsal green.
19. Three evidence bundles complete.
20. Independent Phase 08 review `PASS`.

### 13.3 Planned evidence contents

#### `E-P08-PORT`

```text
canonical source receipt and cited-contract semantic-impact record
approved module/package/signature manifest
public API record/sealed hierarchy projection
state transition and failure/retry tables
dependency tree + jdeps/architecture report
fake/local abstract port contract results
Phase 07 output compatibility receipt
Phase 09/10 consumer compatibility receipt
proposed/open decision manifest
```

#### `E-P08-LOCAL-E2E`

```text
exact Java/Maven/build identity
P08 fixture/config/profile/input/oracle digests
full request→canonicalization→snapshot→solve lineage
committed candidate/replay identities
candidate PASS + result PASS identities
publishable/final result/payload fingerprints
artifact/state/publication refs and versions
status/retrieval evidence
fresh-workspace comparison
security/input-limit/telemetry/redaction assertions
exact test report inventory
```

#### `E-P08-IDEMPOTENCY`

```text
submission same/same and same/different cases
artifact put/read corruption cases
state CAS contenders and version history
duplicate publication same/different digest cases
worker retry identity and attempt lineage
cancellation/publish race
watchdog/transport response-loss cases
filesystem write/flush/move/pointer crash matrix
last safe point and rollback result per fault
```

Evidence artifact는 immutable/content-addressed여야 한다. Evidence key 이름만 있고 bytes/ref/digest가 없거나, expected table을 actual report로 복사했거나, test fake path만 실행한 full E2E claim은 `NOT_PRODUCED`로 판정한다.

## 14. Exit gate, Definition of Done and anti-patterns

### 14.1 Exit gate

| Gate | Pass condition | Evidence |
|---|---|---|
| `G08-PORT` | Provider-neutral inbound/outbound signatures, versioned identity/error/state and architecture tests pass | `E-P08-PORT` |
| `G08-P07` | Actual accepted Phase 07 Publishable/VerificationRejected/GateIncomplete branches exhaustively handled | `E-P08-PORT`, `E-P08-LOCAL-E2E` |
| `G08-ARTIFACT` | Exact key, immutable create, digest verified read, same/different semantics | `E-P08-PORT`, `E-P08-IDEMPOTENCY` |
| `G08-CAS` | State/publication stale writer/race and duplicate semantics exact | `E-P08-IDEMPOTENCY` |
| `G08-CANCEL-DEADLINE` | Intent/termination/last safe point and exceptional timeout meanings separate | `E-P08-IDEMPOTENCY` |
| `G08-LOCAL` | Explicit config CLI real E2E, result retrieval and deterministic rerun | `E-P08-LOCAL-E2E` |
| `G08-SECURITY` | Limits, tenant/path containment, redaction and local composition review | `E-P08-LOCAL-E2E` |
| `G08-MIGRATION` | Legacy characterization, isolation, no-target-publication and rollback rehearsal | All three |
| `G08-REVIEW` | Independent reviewer `PASS` and scheduler acceptance | Review artifact + all three |

Exit 전에 public HTTP cutover, S3 backend, coordinator, AWS deployment 또는 official benchmark를 요구하지 않는다. 반대로 그것들이 없다는 이유로 local reference semantics/test/evidence를 생략할 수 없다.

### 14.2 Definition of Done

Phase 08은 다음이 모두 참일 때만 `ACCEPTED` 후보다.

- [ ] Canonical source fingerprints and cited adjacent contracts revalidated by semantic-impact review; adjacent document/section digest gate 0.
- [ ] Phase 00~07 accepted receipts recorded.
- [ ] Application module owns use cases, identities, lifecycle, errors and ports.
- [ ] Application/provider/transport/path/DB/vendor dependency violations are zero.
- [ ] Request→canonicalization→solve→independent verification→result flow is one path.
- [ ] Phase 07 `PublishableResult`, `VerificationRejected` and `GateIncomplete` are exhaustively consumed without field loss or fabricated disposition.
- [ ] Failure output contains no normal route/outcome/benchmark payload.
- [ ] Explicit local config has no semantic/runtime hidden default.
- [ ] Caller tenant claim is bound to an approved explicit non-ambient access context/session; cross-tenant existence disclosure is zero.
- [ ] Local artifact/state/publication adapters meet digest/CAS/idempotency contracts.
- [ ] Cancellation, deadline, retry and error mapping preserve original meanings.
- [ ] Security/input limits/tenant/path/redaction/observability tests pass.
- [ ] Real local E2E and two-workspace deterministic rerun pass.
- [ ] Same-workspace idempotent retry and all fault/rollback cases pass.
- [ ] Legacy placeholder characterization and rollback boundary are evidence-backed.
- [ ] `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY` exist with digests.
- [ ] Phase 09/10 consumer compatibility receipt exists.
- [ ] Required test failure/error/skipped count is zero.
- [ ] Independent Phase 08 review is `PASS`.
- [ ] Scheduler, not implementer, updates authoritative progress/status.

### 14.3 Forbidden anti-patterns

- Controller/CLI/Lambda에서 solver/verifier를 직접 호출해 application use case 우회
- Application port를 adapter/provider가 정의하거나 provider DTO를 application이 구현
- `Map<String,Object>`, raw `byte[]`, `Path`, URI, HTTP exchange/context를 application/domain public contract에 노출
- `System.nanoTime()`, random UUID, current time, current directory, home, temp, environment로 seed/identity/config 생성
- Missing `Q-BENCH-02` 값에 legacy `8`, `5000`, workflow `900/300` 또는 다른 default 사용
- Profile/version/preset `latest`, classpath first-wins 또는 other-tenant fallback
- Raw input을 solver가 다시 parse하거나 prepared travel 뒤 coordinate/speed fallback
- Search candidate/cache/summary/raw objective를 normal result로 노출
- Phase 07 candidate PASS 하나로 result gate 대체
- Phase 07 rejection/exception을 all-unassigned, empty route 또는 HTTP success로 변환
- Canonical result payload를 application/transport가 다시 derive/encode하여 digest 의미 변경
- Result bytes를 먼저 응답하고 publication CAS를 나중에 수행
- Same key/different digest overwrite 또는 “latest write wins”
- CAS conflict 무시, stale retry without reload, multi-object transaction 가정
- Prefix/directory listing, newest mtime 또는 event arrival로 status/result/completeness 추론
- Filesystem absolute path/locator를 semantic artifact/result identity에 포함
- Atomic move/lock unsupported 시 non-atomic fallback
- Symlink follow, raw identifier path concatenation, tenant path traversal
- Caller-asserted `TenantId`, global/static/`ThreadLocal`, environment or workspace path를 authorization/access context로 신뢰
- Declared file length만 믿고 unbounded streaming/parser 실행
- Limit 초과를 truncate/clamp/partial parse/empty request로 변환
- Cancellation intent를 actual worker termination으로 간주
- Watchdog/platform timeout을 `MAX_STEPS_REACHED` 또는 `NO_STRICT_IMPROVEMENT`로 변경
- Retry에서 seed, warm start, work 또는 logical worker identity 변경
- Completion order/thread first-winner를 comparator input order로 사용
- Elapsed/path/process/event order를 semantic fingerprint 또는 quality objective에 포함
- Secret/raw address/PII/full input/path/stack trace를 log/metric/error payload에 노출
- Existing unauthenticated GCP deploy example를 target local security approval로 간주
- Legacy current endpoint behavior를 canonical public API로 자동 승인
- Stubbed Phase 07 path를 real `E-P08-LOCAL-E2E`로 제출
- Phase 08에서 object-common/S3/Step Functions/Lambda/coordinator/AWS IaC 구현
- Phase 13 route pool/MIP 또는 `Q-VAR-01` variant를 local option으로 미리 추가
- Document/test target 존재만으로 implementation/evidence/review accepted claim

## 15. Blockers, OPEN/GATED/deferred and restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00~07 accepted evidence 부재 | `BLOCKER` | Phase 00~07 owners + scheduler | Phase 08 real code/E2E/acceptance | 이 detailed document와 current inventory | Accepted review/bundle/digest and actual reactor artifacts |
| Upstream full-solution evaluation owner/API gap | `CROSS-PHASE BLOCKER` | Phase 03 Core/Evaluation + Phase 04 Profile + Phase 05/06 Algorithm | Phase 07 accepted candidate authority와 Phase 08 real solve/E2E | Route-level kernel, ordered routes/bank, no ad-hoc aggregate | F-P03-004/F-P04-001/F-P05-001/F-P06-004 exact API/identity/reuse/failure/comparator and cross-phase tests approved |
| Actual Phase 07 is unaccepted | `BLOCKER` | Verification/Result owner | Both-gate real integration/publication | Phase 07 cited §7.6~§8.5/§13.2~§15.2 contract + proposed typed adapter | `E-P07-*`, accepted Phase 07 verdict, compatible variant/field handoff |
| Scheduler task/owners 미지정 | `BLOCKER` | Scheduler | Authoritative implementation/review/status | `scheduler_task_id: TBD_NOT_SUPPLIED` | Exact task ID and separated roles |
| Phase 07/Application Java names | `PROPOSED/OPEN` | Architecture + P07/P08 owners | Public Java compatibility freeze | §6~§7 semantic projection | Cross-phase signature review/ADR |
| Explicit tenant access binding and `ArtifactRef` closure | `CROSS-PHASE SECURITY BLOCKER` | Phase 08 Application/Security + Phase 09 Storage | WP-08.1 tenant contract, WP-08.3 storage/state/publication implementation, `G08-SECURITY` | Caller `TenantId` is untrusted; no port implementation or existence disclosure | Explicit non-ambient tenant-scoped session/facade or equivalent closure binding, missing/mismatch failure and Phase 08/09 contract tests approved |
| Lossless storage operation failure carrier | `CROSS-PHASE DATA-INTEGRITY BLOCKER` | Phase 08 Application + Phase 09 Storage/Data Integrity | WP-08.1 port signature, WP-08.3 fault/corruption evidence | Proposed port semantics only; no adapter implementation or generic exception mapping | Operation별 exhaustive result/failure carrier, safe fields, retry disposition and no-collapse contract tests approved |
| Run-state fence vs publication precondition identity | `CROSS-PHASE LINEARIZABILITY BLOCKER` | Phase 08 Application + Phase 09 Storage + Phase 10 Coordinator | `ResultPublisher` implementation, lost-response/idempotency and publication evidence | Both-gate immutable result refs with no authoritative publication write | Distinct typed fence/precondition contract, stale/occupied/lost-response tests and consumer receipt approved |
| Worker committed-outcome authority primitive | `DOWNSTREAM CROSS-PHASE BLOCKER` | Phase 09 Storage + Phase 10 Coordinator | Phase 09→10 declared-worker commit/read handoff; Phase 08 single-worker core는 막지 않음 | Phase 08 single-worker assignment/outcome refs only; no inferred committed pointer | One typed create/CAS primitive, same/different digest oracle and Phase 09/10 compatibility approval |
| Fingerprint/SolveId derivation algorithm | `PROPOSED/OPEN` | Architecture/Data Integrity | Stable external identity bytes | Versioned injected test derivation | Serialization/fingerprint ADR and migration policy |
| CLI config/exit code schema | `PROPOSED/OPEN` | Application/Product/Operations | User-facing local compatibility | Required semantic field set | Versioned CLI contract review |
| Public/local HTTP schema/auth mapping | `OUT OF SCOPE/OPEN` | Product/API/Security | Optional HTTP adapter/cutover | CLI reference + safe failure hierarchy | Separate scope, schema/auth/TLS/threat review |
| Production input/resource limits | `OPEN` | Security/Performance/Operations | Production local/public limit values | Explicit test-only values and required fields | Measured workload/security review and approval |
| Local filesystem atomic/CAS support matrix | `PROPOSED/EXPERIMENT_REQUIRED` | Storage/Platform | Supported filesystem declaration | Fake/in-memory port semantics | Crash/concurrency/capability evidence per filesystem |
| Actual Phase 09 unaccepted | `CONSUMER REVIEW PENDING` | Phase 09 Storage owner | Cross-phase storage signature freeze와 object backend acceptance | Actual Phase 09 §7.2~§8.8/§15.1 semantic contract citations | Phase 08 accepted evidence/review and field/meaning compatibility receipt |
| Phase 09/11 S3 ownership boundary | `CROSS-PHASE BOUNDARY BLOCKER` | Architecture + Phase 08/09/11 owners + scheduler | Phase 09 exit backend suite와 Phase 11 entry/provider evidence | Provider-neutral Phase 08 ports + local single-JVM reference; no S3 implementation claim | Plan/Integrated/Phase 08/09/11 aligned decision assigns S3 implementation and same-suite evidence without weakening conditional semantics |
| Actual Phase 10 unaccepted | `FUTURE PHASE / CONSUMER REVIEW PENDING` | Phase 10 Coordinator owner | Multi-round/durable fan-out/fan-in | Single-worker dispatch/workflow seam | Phase 09 accepted + Phase 10 entry/review |
| `Q-BENCH-02` official values | `OPEN — EXPERIMENT_REQUIRED` | Benchmark/Quality | Official manifest/baseline/cutover | Explicit `TEST_ONLY`/experiment config | Calibration corpus/protocol, measured review, approval |
| Current Win fixture decimal `D/U` | `BLOCKER FOR OFFICIAL USE` | Input/Matrix + Benchmark | That fixture's official run | `P08_LOCAL_PD_3_TEST_ONLY` integer fixture | Compliant integer matrix or approved migration |
| AWS target implementation/cutover | `FUTURE GATE` | Phase 09~11/14 owners | S3/Step Functions/Lambda and production | Provider-neutral ports + local reference | Storage/coordinator/AWS parity, security, shadow, rollback evidence |
| `C-17` route pool/MIP | `GATED TARGET` | Product/Algorithm/Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13 and default activation | ALNS-only local path | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope와 OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` | `DEFERRED` | Product/Domain/Algorithm | Optional variant question/implementation | Current pair/terminal/bank contract | Representative fixture, feasibility and separate approval |
| Multi-trip/rotation/dynamic routing | `DEFERRED FEATURE` | Product/Domain/Algorithm | Extended runtime semantics | Oneway/single roundtrip immutable solve | Approved domain/runtime/replanning contract |

Blocker 때문에 이 문서 작성을 중단하지 않는다. 구현자는 blocker를 hidden default, fake evidence, unreviewed public API 또는 infrastructure shortcut으로 우회하지 않는다.

Restart procedure:

1. Owner/scheduler가 exact blocker receipt와 changed source sections를 제공한다.
2. §1 canonical source fingerprints를 다시 계산하고 cited adjacent contracts의 semantic diff/impact를 review한다.
3. Meaning drift를 source→contract→test matrix에 반영한다.
4. Last safe point artifacts/pointers가 immutable하고 unpublished인지 검사한다.
5. Affected WP의 first red test부터 재개한다.
6. Required downstream compatibility receipt를 다시 받는다.

## 16. Previous and next handoff

### 16.1 Previous — Actual but unaccepted Phase 07

[Actual Phase 07](phase-07-independent-verification-final-result.md)에서 다음을 받는다.

- `Phase07HandoffManifest` exact contract/version/source/build/runtime fingerprints
- Problem/prepared travel/profile/evaluation declaration/SolvePlan/candidate/replay identities
- Candidate verifier PASS/report or typed candidate-stage rejection
- Verified solution/audit/final result/result PASS identities when successful
- Canonical payload digest/length and `PublishableResult` envelope fingerprint
- Safe `FinalResultRejection.VerificationRejected` or `FinalResultRejection.GateIncomplete`
- `E-P07-CANDIDATE-VERIFY`, `E-P07-AUDIT`, `E-P07-RESULT-VERIFY`
- Accepted Phase 07 review ref and rollback point

Phase 08 receives exactly:

```text
Phase07Output.Publishable(PublishableResult)
or
Phase07Output.Rejected(FinalResultRejection.VerificationRejected)
or
Phase07Output.Rejected(FinalResultRejection.GateIncomplete)
```

Phase 08 does not receive mutable builder/cache/auditor/search state, provider locator/client, raw
route summary or one-PASS partial result. Phase 07 typed output 이름이 review에서 바뀌면 cited
contract semantic-impact review와 `ApplicationPortSignatureTest` compatibility manifest를 함께
갱신한다. 인접 section digest를 acceptance 대용으로 추가하지 않는다.

### 16.2 Next — Actual but unaccepted Phase 09 storage contract

[Actual Phase 09 — DB 없는 object storage](phase-09-object-storage-no-database.md)는 아직
unaccepted downstream contract이며 다음 stable application/storage contract를 소비한다.

```text
Phase08ApplicationStorageContractManifest
  contractVersion
  source and Phase08 accepted evidence refs
  TenantId / SolveId / ArtifactKey / ArtifactRef projections
  ArtifactKind and schema/version rules
  algorithm-tagged ContentDigest/contentLength/mediaType semantics
  OpaqueLocator non-semantic rule
  ArtifactStore putIfAbsent/readVerified/metadata signatures
  same-key same/different content semantics
  RunStateRepository create/get/compareAndSet signatures
  opaque StateVersion and legal transition guard
  ResultPublisher compareAndSet signature
  distinct run-state authorization fence and publication-pointer precondition identity
  both-gate publication precondition
  exact retrieval/no-list authority rule
  idempotency/conflict/retry disposition
  approved operation별 lossless storage failure carrier and mapping
  approved explicit tenant-scoped access binding/session and classification boundary
  missing/mismatched access-context failure contract
  local adapter execution scope = LOCAL_SINGLE_JVM
  abstract port contract suite source/digest
  E-P08-PORT / E-P08-IDEMPOTENCY refs
  independent Phase08 review ref
  last safe point and rollback boundary
```

승인된 Phase 09 범위에서 자유롭게 구현할 것:

- Object-common semantic adapter internal structure
- `ObjectStorageBackend`
- Optional exact-declared `RunArtifactRepository` only when Phase 10 requires it; no listing/mega repository
- Phase 10용 committed-outcome authority primitive only after Phase 09/10 cross-phase approval
- In-memory/local-directory conditional operations and recovery within declared capability
- S3 conditional operations only after the Phase 09/11 ownership decision assigns them
- Provider opaque locator/key layout
- Object metadata/version-token mapping
- Streaming/large-object adapter mechanics

Phase 09가 바꾸면 안 되는 것:

- Application logical key/content/state/publication meanings
- Same identity/different digest conflict
- Immutable artifact before one authoritative pointer CAS
- Both-gate result publication requirement
- Exact-key retrieval/no-list authority
- Tenant isolation and safe failure boundary
- No caller-asserted tenant trust and no ambient global/static/ThreadLocal access context
- Missing/denied/corrupt/stale/indeterminate failure distinctions and retry disposition
- Run-state fence와 publication-pointer precondition token의 identity 분리

Phase 08은 object storage, S3, bucket/container/key layout, multi-object transaction 또는 AWS code를 handoff에 선구현하지 않는다.

### 16.3 Later — Actual but unaccepted Phase 10 coordination seam

[Actual Phase 10 — Provider-neutral coordinator](phase-10-provider-neutral-coordinator.md)는
review `COMPLETE/CHANGES_REQUIRED`, implementation `NOT_STARTED`, evidence
`NOT_PRODUCED`, handoff `NOT_READY`인 unaccepted downstream contract이며 다음을 소비한다.

```text
Phase08ExecutionSeamManifest
  SolveId / ManifestFingerprint
  WorkerRunId / AttemptId
  WorkerAssignment and retry invariants
  WorkerDispatcher logical operations
  WorkflowExecutionPort logical operations
  CancellationPort intent semantics
  RunDeadline/termination separation
  RunStateRepository CAS semantics
  Publishable/VerificationRejected/GateIncomplete application outcomes
  Telemetry correlation/event contract
  deterministic single-worker local oracle
```

Phase 10이 추가로 소유한다.

- ExecutionRound and declared worker set
- Stable fan-out/fan-in completeness
- Multi-round champion and warm-start transition
- Durable state machine, wakeup/reconcile
- All-workers-normal-complete/verified gate
- Workflow/compute retry/cancel mapping

Phase 08 single-worker `SUCCEEDED`를 multi-worker round completion evidence로 재사용하지 않는다.

### 16.4 Local/legacy rollback handoff

```text
rollback point:
  existing single shaded GCP-oriented application source/artifact

new isolated unit:
  explicit local distribution + explicit workspace

rollback action:
  stop/disable new local entrypoint
  preserve published/unpublished pointer distinction
  leave legacy source/artifact unchanged
  record evidence and incompatibility

not authorized:
  GCP deploy/update/delete
  production traffic switch
  legacy result rewrite
  destructive evidence cleanup
```

Production cutover/rollback은 Phase 14 gate이며 이 local rehearsal이 대신하지 않는다.

### 16.5 Phase 14A ALNS benchmark qualification handoff

Phase 08 acceptance 뒤 Phase 14A는 local reference의 immutable execution manifest와
both-verifier result만 소비한다. Phase 08은 benchmark corpus/threshold를 정하거나
selected run만 전달하지 않는다. Declared run 전부의 success/failure/timeout/resource
상태와 artifact digest를 손실 없이 넘기며, Phase 14A의 independent review/acceptance
receipt가 없으면 Phase 13은 계속 `GATED`다.

## 17. Source → requirement → test → evidence traceability

| Requirement ID | Source authority | Phase 08 contract | Exact tests | Evidence |
|---|---|---|---|---|
| `REQ-P08-DAG` | Final Architecture §2, Integrated §3 | §5/§7 public dependency | `Phase08ApplicationArchitectureTest.*` | `E-P08-PORT` |
| `REQ-P08-FLOW` | Master §4.1~§4.2, Architecture §3.1 | §2.1/§6.1/§7.7 | `LocalCliEndToEndTest.explicitFixtureRunsCanonicalizeSolveBothGatePublishAndRetrieve()` | `E-P08-LOCAL-E2E` |
| `REQ-P08-CANON` | Master §4.1~§4.2, Domain input/error contract | Versioned adapter before solve | `LocalCliEndToEndTest.*`, `LocalCliConfigurationTest.*` | `E-P08-LOCAL-E2E` |
| `REQ-P08-P07` | Master §14.1, Actual Phase 07 §7.6~§8.5 | Publishable/VerificationRejected/GateIncomplete exhaustive | `Phase07ApplicationIntegrationTest.*` | `E-P08-PORT`, `E-P08-LOCAL-E2E` |
| `REQ-P08-NO-FAIL-PAYLOAD` | Phase 07 §7.7/§13.2~§13.3 | Safe rejection/incomplete only | `Phase07ApplicationIntegrationTest.*Rejection*`, `*GateIncomplete*`, `*exception*` | `E-P08-PORT` |
| `REQ-P08-IDENTITY` | Architecture §3.6, §5.2 | §6.3/§7.2 identities/version | `SubmissionIdempotencyTest.*` | `E-P08-IDEMPOTENCY` |
| `REQ-P08-ARTIFACT` | Architecture §5.1~§5.2, Integrated §12.3 | ArtifactStore exact/digest | `ArtifactStoreContract.*` | `E-P08-PORT`, `E-P08-IDEMPOTENCY` |
| `REQ-P08-CAS` | Integrated §12.4/§13.3, Plan Phase 08/09 | State/publication CAS | `RunStateRepositoryContract.*`, `ResultPublisherContract.*` | `E-P08-IDEMPOTENCY` |
| `REQ-P08-LOCAL` | Architecture §3.2, Integrated §12.4 | Explicit CLI/filesystem/same process | `LocalCliEndToEndTest.*`, filesystem suites | `E-P08-LOCAL-E2E` |
| `REQ-P08-NO-DEFAULT` | `Q-BENCH-02`, Architecture §5.2 | §2.4/§8.1~§8.2 | `LocalCliConfigurationTest.*`, `SubmissionIdempotencyTest.missingExplicit*` | `E-P08-PORT`, `E-P08-LOCAL-E2E` |
| `REQ-P08-REPRO` | Master §13.2, Architecture §3.2 | §8.5 stable/different fields | `LocalCliEndToEndTest.sameManifestInTwoFreshWorkspacesProducesSameCanonicalResultIdentity()` | `E-P08-LOCAL-E2E` |
| `REQ-P08-CANCEL` | Master §4.1/§13.1, Architecture §3.6 | §9.2 intent/actual/safe point | `CancellationSemanticsTest.*` | `E-P08-IDEMPOTENCY` |
| `REQ-P08-DEADLINE` | Master §13.1, Architecture §3.2/§5.3 | §9.3 three boundaries | `DeadlineSemanticsTest.*` | `E-P08-IDEMPOTENCY` |
| `REQ-P08-RETRY` | Architecture §3.6/§5.3 | Same logical ID, attempt-only | `WorkerRetryIdentityTest.*` | `E-P08-IDEMPOTENCY` |
| `REQ-P08-ERROR` | Domain §16, Architecture §5.3 | §7.6/§9.4 typed mapping | Phase 07 integration, deadline/cancel/limit tests | `E-P08-PORT` |
| `REQ-P08-SECURITY` | Architecture §5.5, Integrated §20, Actual Phase 09 §8.3 | §2.4/§7.1/§10.1 explicit non-ambient access binding, tenant/path/redaction | `ApplicationPortSignatureTest.tenantScopedAccessBindingIsExplicitAndNonAmbient()`, `LocalWorkspaceSecurityTest.*`, `TelemetryContractTest.redacts*` | `E-P08-PORT`, `E-P08-LOCAL-E2E` |
| `REQ-P08-LIMIT` | Integrated §20~§21 general failure boundary | §10.2 explicit limits | `LocalInputLimitTest.*` | `E-P08-LOCAL-E2E` |
| `REQ-P08-OBS` | Architecture §5.5, Integrated §19.3 | §10.3 events/metrics/non-semantic | `TelemetryContractTest.*` | `E-P08-LOCAL-E2E` |
| `REQ-P08-MIGRATION` | Master §15.10/§16.2, actual repository | §4.4/§8.6/§16.4 | `LegacyPlaceholderSourceCharacterizationTest.*`, `LegacyMigrationBoundaryTest.*` | All three |
| `REQ-P08-P09` | Integrated §13, Plan Phase 09 | §7.3/§16.2 storage handoff | `ApplicationPortSignatureTest.*Phase09*`, port contracts | `E-P08-PORT`, consumer receipt |
| `REQ-P08-P10` | Architecture §3.3~§3.6, Plan Phase 10 | §7.4/§16.3 seam only | Retry/cancel/state/telemetry tests | `E-P08-PORT`, consumer receipt |
| `REQ-P08-Q-INFRA` | `Q-INFRA-01 RESOLVED` | AWS selected but not pulled forward | Architecture/default-local dependency tests | `E-P08-PORT` |
| `REQ-P08-Q-BENCH` | `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED` | Explicit test-only; official hidden default 0 | Config/missing-field tests | `E-P08-PORT` |
| `REQ-P08-C17` | Canonical `C-17 GATED TARGET` | No route-selection dependency/option | `Phase08ApplicationArchitectureTest.defaultLocalDistributionContainsNoCloudDatabaseOrRouteSelectionDependency()` | `E-P08-PORT` |
| `REQ-P08-Q-VAR` | `Q-VAR-01 DEFERRED` | Current domain only; no activation | Dependency/config manifest inspection | `E-P08-PORT` |

Traceability row가 link나 이름만 있고 actual test report/evidence digest와 연결되지 않으면 coverage로 계산하지 않는다. Future Java/type 이름이 review에서 바뀌면 requirement ID와 semantic assertion을 유지하면서 exact test/signature manifest를 같은 change에서 갱신한다.

## 18. Implementation-start and review checklist

### Before first code change

- [ ] Canonical source fingerprints and cited Phase 07/09 contract semantic-impact review revalidated; adjacent document/section digest gate 0.
- [ ] Phase 00~07 accepted receipts and Phase 07 exact typed handoff present.
- [ ] Scheduler task ID and separate implementer/reviewer/security/storage owners assigned.
- [ ] Proposed/open API, fingerprint, CLI, limit and filesystem decisions recorded.
- [ ] Phase 08/09 explicit non-ambient tenant-scoped access binding/closure contract approved.
- [ ] Phase 08/09 operation별 lossless storage failure carrier and no-collapse mapping approved.
- [ ] Run-state authorization fence and publication-pointer precondition identity approved.
- [ ] Phase 09/10 worker committed-outcome authority primitive boundary approved before downstream use.
- [ ] Phase 09/11 S3 ownership and same-suite evidence boundary explicitly approved.
- [ ] Legacy characterization/rollback owner and no-production-side-effect boundary approved.
- [ ] First architecture/identity tests are red for the intended reason.

### Before claiming implementation complete

- [ ] All WP-08.0~7 handoffs complete in order.
- [ ] Required exact test methods discovered/executed with failed/error/skipped 0.
- [ ] Real Phase 01~07 local E2E, not stub-only, passes.
- [ ] Fresh and same-workspace rerun/idempotency evidence passes.
- [ ] Cancel/deadline/CAS/crash/security/redaction fault evidence passes.
- [ ] Legacy isolation and rollback rehearsal passes.
- [ ] No AWS/object-common/coordinator/DB/provider code pulled forward.
- [ ] Three immutable evidence bundles and consumer receipts exist.
- [ ] Independent Phase 08 review passes.
- [ ] Scheduler performs any authoritative status/progress update.

### Phase 08 final handoff summary

```text
input:
  explicit local submission/config
  accepted Phase 01~06 semantic pipeline
  accepted Phase07Output

output:
  provider-neutral application use cases/ports
  versioned identity/lifecycle/failure contracts
  explicit CLI local reference runtime
  local immutable artifact/state/publication adapters
  cancellation/deadline/idempotency/security/observability semantics
  real deterministic both-gate local E2E
  Phase 09 storage and Phase 10 coordination seam manifests

not output:
  object storage/S3
  AWS/GCP workflow or compute adapter
  durable coordinator/multi-round execution
  public HTTP compatibility/cutover
  official benchmark values
  C-17 hybrid or Q-VAR-01 variants
```
