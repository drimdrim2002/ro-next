# Phase 06 — 복사 후 변경 방식의 ALNS

```yaml
document_status: CHANGES_REQUIRED
document_version: 1.2
phase: "06"
phase_name: cow-alns-reproducibility
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
evidence_status: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Solver/Search owner role
  upstream_state_and_portfolio: Phase 05 Pair/Insertion/Portfolio owner role
  upstream_evaluation: Phase 03 Core/Evaluation and Phase 04 Profile/Binding owner roles
  downstream_verification: Phase 07 Independent Verification owner role
  downstream_coordination: Phase 10 Coordinator owner role
  review: independent Phase 06 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture
  - Phase 03 accepted cache-free propagation/evaluation/comparator contract
  - Phase 04 accepted immutable BoundProfile and SolvePlan
  - Phase 05 accepted SearchSnapshot, PairInsertionEvaluator, SeedPortfolio, and handoff manifest
  - explicit algorithm/operator/acceptance/termination config with no omitted official numeric values
planned_evidence:
  - E-P06-COW
  - E-P06-ALNS
  - E-P06-REPLAY
source_fingerprints_sha256:
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/deprecated/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/deprecated/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/deprecated/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
  docs/deprecated/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d
  docs/implementation/README.md: 6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358
  docs/README.md: 5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569
  docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md: 46945bd0b4c2d65d43ea078d5562e048133986983d11c5b62ecfb8a9f3bd7b54
adjacent_phase_sources_read_only:
  phase_05:
    file: docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
    sections: "§7, §9, §15.2~§15.3"
    whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
  phase_07:
    file: docs/implementation/phases/phase-07-independent-verification-final-result.md
    sections: "§4, §7.1~§7.3, §15.1"
    whole_file_hash: OMITTED_TO_AVOID_RECIPROCAL_DOCUMENT_HASH
adjacent_phase_documents:
  phase_05: ACTUAL_DIRECTLY_READ_NOT_STARTED_NOT_ACCEPTED
  phase_07: ACTUAL_DIRECTLY_READ_NOT_STARTED_NOT_ACCEPTED
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
  sha256: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
```

## 1. 문서 지위와 권위

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. 권위 원문의 `REVIEW` metadata는 provenance로 보존하지만 이 상세 문서 작성을 중단하는 조건이 아니다. 반대로 이 문서, proposed type, future test 이름 또는 pseudocode의 존재는 Phase 06 구현·evidence·review 완료를 뜻하지 않는다.

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../master-design.md)
2. [질문 등록부](../../deprecated/master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../deprecated/2026-07-26-domain-design.md)의 stable state, COW와 ALNS 의미
4. [Final Architecture Design](../../deprecated/2026-07-26-architecture-design.md)의 Java 25/Maven/package/runtime 배치
5. [Integrated implementation design](../../deprecated/architecture-domain-implementation-design.md)의 15 Phase 순서
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../deprecated/2026-07-26-master-design.md)는 historical cross-check에만 사용했다. `docs/codex/*`는 현재 권위 입력으로 사용하거나 복사하지 않았고 이 작업에서 수정하지 않는다. Final Domain/Architecture 안의 과거 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 최신 Canonical Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. 이 drift는 Phase 06 solver 의미를 바꾸지 않는다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 06에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../master-design.md) | §4.1~§4.6, §9~§13, §15.5~§15.7, §16~§17 | Phase 05 portfolio 소비, ALNS step, destroy/repair/adaptive/acceptance, COW/cache, termination, strong reproducibility, `RM-4` gate |
| [Final Domain](../../deprecated/2026-07-26-domain-design.md) | §8, §10~§11, §16, §17.5~§17.6, §17.9, §18 | Stable route/bank partition, search type lifecycle, state/outcome, operator result, cache-free equality, fault와 replay evidence |
| [Final Architecture](../../deprecated/2026-07-26-architecture-design.md) | §2.1~§2.7, §3.1~§3.6, §5.2~§5.6, §6 | `rpdptw-solver` package, core-only dependency, `WorkerRun`/`AlnsRun`, completed-step budget, retry/seed identity, build/evidence |
| [Integrated design](../../deprecated/architecture-domain-implementation-design.md) | §2~§3, §9~§10, §19~§25 | Phase 05→06→07 순서, COW ALNS gate, configuration/provenance, fault/test/anti-pattern |
| [질문 등록부](../../deprecated/master-design-open-questions.md) | `Q-ALG-01`, `Q-ALG-02`, `Q-BENCH-02`, `Q-VAR-01` | 최대 8개 portfolio, `KEEP_COW`, 공식 execution 수치 calibration gate, optional variant deferred |
| [Master Realization Plan](../master-realization-plan.md) | §2~§6, Phase 03~07, §8~§15 | 입력 권위, actual inventory, Phase 06 entry/exit/evidence, test/DoD/blocker/handoff |
| [구현 문서 지도](../README.md) | §1~§7 | Canonical filename, planned/actual link, status와 review 규칙 |
| [Phase 03 actual 상세](phase-03-route-propagation-evaluation-kernel.md) | §7~§15 | Cache-free evaluator/comparator는 소비 계약이며 Phase 06이 재구현하지 않는다는 경계 |
| [Phase 05 actual 상세](phase-05-pair-insertion-initial-portfolio.md) | §7, §9, §15.2~§15.3 | `SearchSnapshot`, `ConstructionCandidate`, `SeedPortfolio`, exact `Phase05SeedPortfolioHandoff`, Phase 06이 별도 소유할 screen/RNG/operator/COW 경계 |
| [Phase 07 actual 상세](phase-07-independent-verification-final-result.md) | §4, §7.1~§7.3, §15.1 | Candidate/replay authority equality, non-authoritative declared claims, accepted `E-P06-*` receipt와 solver dependency 금지 |

Phase 05와 Phase 07 상세는 이 문서 작성 중 actual 파일로 전환되어 양쪽 handoff를 직접 대조했다. 이 독립 리뷰의 final validation 중에도 두 인접 문서는 각 owner에 의해 v1.1/review workflow로 갱신되었으므로 다시 read-only 대조했다. 둘 다 implementation `NOT_STARTED`, phase `NOT_ACCEPTED`이고 accepted evidence는 없으므로 implementation entry는 계속 차단된다. Proposed signature와 named-section receipt는 Phase 05/06/07 cross-phase review에서 함께 정합화하되 whole-file reciprocal hash를 만들지 않는다.

### 1.2 ALNS-first 독립 실행·benchmark handoff

Phase 06의 required build/run은 OR-Tools를 포함한 MIP solver, optimizer license,
solver service/token, native packaging, Phase 13 scope 또는 production authority 없이
완결돼야 한다. MIP 결과를 ALNS correctness oracle, acceptance 입력, termination
조건 또는 quality baseline으로 사용할 수 없다.

Phase 06은 Phase 07/08/14A가 소비할 다음 측정 가능 artifact를 남긴다.

- Immutable problem/travel/profile/build/algorithm/config fingerprint
- Seed derivation/version과 actual seed
- Requested/completed ALNS work, normal/exceptional termination과 trace digest
- Candidate/result fingerprint와 objective vector
- CPU time, elapsed time, peak/allocated memory 등 측정 schema와 runtime identity
- Replay outcome과 mismatch report

구체 corpus, seed 목록, repeat 수, timeout/resource budget, quality/performance threshold와
허용 variance는 Phase 14A 승인 전 `OPEN — EXPERIMENT_REQUIRED`다. Phase 06 test-only
값은 explicit하게 주입하되 official default나 Phase 13 activation evidence로 승격하지
않는다.

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 06은 Phase 05의 cache-free validated `SeedPortfolio`, stable `SearchSnapshot`과 pure `PairInsertionEvaluator` 계약을 받아 다음을 구현·검증한다.

1. Changed route만 first-write copy하고 bank는 독립 value로 유지하는 COW trial.
2. Request-pair destroy → repair → configured bounded improvement → full evaluation → guard → acceptance → immutable state replacement의 completed-step.
3. Explicit operator selection, adaptive learning, acceptance/temperature policy와 exact completed-step termination.
4. Phase-1 per-candidate screen과 completion-order-independent stable champion selection.
5. Namespaced seeded RNG ownership, deterministic tie-break, immutable trace와 independent replay oracle.
6. Accept/reject/invalid/fault/cancel/watchdog/resource/platform 종료에서 parent/current/best 보존과 exact work accounting.

동일한 strong-replay envelope에서는 canonical attempt/step decision trace, phase-1 champion, committed candidate와 replay fingerprint가 정확히 같아야 한다.

### 2.2 포함 범위

- `SearchSnapshot` top-level no-alias와 immutable route structural sharing
- Changed-route copy-on-first-write, independent bank, cache/fingerprint invalidation
- Cross-phase review가 승인한 central pair-removal editor를 통한 destroy 적용과 Phase 06 소비 contract test
- Phase 05 side-effect-free exact insertion evaluator를 통한 repair
- Random, related/Shaw, route, historical edge/action, worst/semi-worst, location-oriented removal family의 versioned proposal 계약
- Explicit repair scope와 cheap shortlist → authoritative exact insertion composition
- Explicit bounded in-step improvement family와 per-family attempt/work budget
- Ordered operator registry, seeded selection, immutable call/reward/weight/probability snapshot
- `GLOBAL_BEST_IMPROVED`, `CURRENT_IMPROVED`, `ACCEPTED_NON_IMPROVING`, `REJECTED`, `INVALID_CANDIDATE`, `INTERRUPTED`
- Hard/stage guard 뒤의 explicit acceptance policy와 completed-step-based temperature update
- `MAX_STEPS_REACHED`와 exceptional termination 분리
- `NO_STRICT_IMPROVEMENT`/`MAX_ROUNDS_REACHED`를 위한 typed handoff 의미와 future coordinator test contract
- Phase-1 screen, cache-free validation, stable champion reduction
- Seed derivation/version/namespace, replay manifest와 deterministic trace digest
- Unit/contract/integration/architecture/fault/corruption/replay/security/performance-counter evidence

### 2.3 명시적 비범위

- Phase 03 propagation/evaluation/comparator의 복제 또는 Phase 04 profile/preset 재해석
- Phase 05 insertion feasibility, functional construction transition과 initial construction policy의 재구현
- Phase 07 candidate/result verifier, `PASS`, `VerifiedSolution`, final audit/outcome/summary와 publication
- Phase 10 declared-worker fan-out/fan-in, outer round champion, retry/CAS와 official multi-round completion
- Phase 13 route pool/MIP/selector/hybrid 구현 또는 vendor dependency
- Apply/undo, automatic state-strategy switch와 profiling 결과 없는 최적화
- Multi-trip/rotation, optional variants, cross-worker pool fan-in
- Official `screenMaxSteps`, `phase2MaxSteps`, worker count, `maxRounds`, watchdog 또는 새 안전 budget 수치
- Raw input/travel generation, customer name, cloud SDK, storage/workflow/HTTP DTO
- Public/wire API, trace serialization, hash algorithm 또는 external compatibility의 최종 승인

Phase 06이 만드는 `CommittedCandidate`는 **search-time committed candidate**다. 독립 verifier의 `PASS`나 publishable result가 아니며 그 용어를 대신 사용할 수 없다.

## 3. Phase-local 결정, 수치 상태와 불변조건

이 절의 Java 이름과 signature는 모두 **PROPOSED INTERNAL**이다. Phase 05/06/07 review에서 의미를 보존하는 범위에서 이름과 visibility를 바꿀 수 있으며 public compatibility 약속이 아니다.

| 항목 | 상태 | Phase 06 판단 |
|---|---|---|
| COW state strategy | FIXED | `Q-ALG-02 RESOLVED — KEEP_COW`; apply/undo를 구현하거나 자동 선택하지 않는다. |
| Cache-free evaluator/comparator | FIXED | Phase 03/04의 authoritative contract를 주입받아 소비한다. Cache/incremental 값은 equality가 증명된 최적화일 뿐이다. |
| Pair mutation/insertion semantics | FIXED | Destroy는 ordered proposal만 만들고 중앙 pair-removal editor가 pair/bank invariant를 보존하며 repair는 Phase 05 `PairInsertionEvaluator` 의미를 소비한다. Operator 직접 mutation과 feasibility 재구현은 금지한다. |
| Central pair-removal editor implementation owner | CROSS-PHASE OWNER CONFLICT — BLOCKED | Master Realization Plan Phase 05와 Integrated §9.7은 Phase 05에 중앙 atomic editor를 두지만 actual Phase 05 §2.3/§15.2와 이 문서 v1.0은 Phase 06 소유로 분리했다. Phase 05/06/Architecture review가 module/API/evidence owner를 승인하기 전 어느 쪽도 구현 완료를 주장하지 않는다. |
| State publication | PROPOSED | 한 step의 모든 next value를 local immutable aggregate로 만든 뒤 마지막에 한 번 교체한다. 중간 current/best publish는 금지한다. |
| RNG derivation | PROPOSED/REVIEW_REQUIRED | Versioned seed derivation과 purpose별 stateless substream을 사용한다. Algorithm/version 확정 전 external replay compatibility를 주장하지 않는다. |
| Invalid/operator defect policy | PROPOSED SAFE BASELINE | Structural corruption, malformed proposal 또는 operator exception은 candidate discard 후 `FAILED`; 내부에서 무한 재시도하지 않는다. Continue-on-invalid는 explicit bounded policy와 별도 approval 없이는 금지한다. |
| Acceptance policy | EXPLICIT CONFIG REQUIRED | Hill Climbing, SA 또는 다른 approved policy를 ID/version/config로 선택한다. Omitted policy나 phase별 hidden switch는 bind error다. |
| Official ALNS numeric values | OPEN — EXPERIMENT_REQUIRED | `Q-BENCH-02` calibration 전 없음. Test-only/experiment config만 이름과 manifest에 명시한다. |
| Legacy replay numeric values | COMPATIBILITY/TEST ONLY | `OGC2024_LEGACY_REPLAY` preset의 값은 differential replay 대상이며 general production default가 아니다. |
| Round termination | FUTURE PHASE 10 | Phase 06은 worker/candidate record를 제공한다. `NO_STRICT_IMPROVEMENT`/`MAX_ROUNDS_REACHED`의 outer decision은 Phase 10 future red다. |

반드시 지킬 불변조건은 다음과 같다.

1. **Stable partition:** 모든 committed/current/stageBest/solveBest는 complete pair와 route/bank XOR을 만족한다.
2. **Top-level no alias:** `current`, `stageBest`, `solveBest`는 서로 다른 immutable snapshot value/handle이며 어느 슬롯의 교체도 다른 슬롯을 변경하지 않는다.
3. **Safe structural sharing:** Unchanged route value는 immutable일 때만 공유할 수 있다. 한 route의 첫 write 전에 trial-owned copy를 만들며 같은 trial에서 두 번 copy하지 않는다.
4. **Independent bank:** Trial bank는 base bank와 mutable alias를 공유하지 않는다.
5. **No parent mutation:** Reject, invalid, exception, cancellation, watchdog와 resource signal 뒤 base/current/stageBest/solveBest canonical bytes와 fingerprint가 모두 동일하다.
6. **Single state publication:** Candidate commit, best selection, adaptive/acceptance update와 completed-step increment가 모두 성공한 next `AlnsSearchState`만 한 번 publish한다.
7. **Completed-step exactness:** Destroy, repair, bounded improvement, structural/full evaluation, guard, acceptance, current/best decision, adaptive/acceptance update가 모두 끝나야 `completedStep + 1`이다.
8. **Non-advance:** `INVALID_CANDIDATE`와 `INTERRUPTED`는 completed-step, reward, operator call count, adaptive weight와 temperature를 전진시키지 않는다.
9. **Rejected is completed:** Hard-feasible/stage-guarded candidate의 정상 `REJECTED`는 completed step이며 configured reject reward와 temperature update를 적용한다.
10. **Hard guard first:** Structural/hard invalid candidate는 acceptance random draw를 소비하거나 temperature로 살아남을 수 없다. SolvePlan stage guard도 acceptance보다 먼저다.
11. **Best eligibility와 monotonicity:** SolvePlan stage guard를 통과한 candidate만 `current`, `stageBest`, `solveBest` 후보가 될 수 있다. Non-improving acceptance는 `current`만 바꿀 수 있고 cache-free comparator 기준 `stageBest`와 `solveBest`는 악화되지 않는다.
12. **Explicit numeric config:** Removal count, shortlist, local improvement, reward/update/reaction/floor, temperature/cooling/scale, step와 watchdog 관련 값은 typed config에 없으면 실행하지 않는다.
13. **RNG ownership:** Global/static/thread-local random을 금지한다. Base seed와 namespace/version/context에서 purpose별 stream을 만들고 retry `AttemptId`, thread ID, clock와 completion order를 seed input으로 쓰지 않는다.
14. **Stable order:** Request/vehicle/route/position/operator/candidate reduction은 canonical total order를 사용한다. Hash iteration, first-completed와 clock tie-break를 금지한다.
15. **Evaluator reuse:** Phase 06은 prepared travel, propagation, hard feasibility, objective/comparator를 다시 구현하지 않는다.
16. **Termination truth:** Watchdog/cancel/resource/platform/failure를 `MAX_STEPS_REACHED` 또는 정상 round 종료로 이름 변경하지 않는다.
17. **No verifier pull-forward:** Candidate manifest는 verifier input일 뿐 `PASS`, final outcome 또는 publication eligibility를 포함하지 않는다.
18. **No official overclaim:** Test-only 수치, legacy preset과 local repeat가 official benchmark/calibration evidence가 아니다.

## 4. Entry gate와 확인 방법

문서 작성은 완료할 수 있지만 Phase 06 implementation 착수는 다음 gate가 모두 충족될 때까지 `BLOCKED`다.

| Entry 항목 | 확인 방법 | 2026-07-28 checkout 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Accepted review, `E-P00-ARCH`, target reactor와 architecture rule | 상세 `REVIEWED_WITH_CORRECTIONS`; review `COMPLETE`/`PASS_WITH_RESIDUAL_BLOCKERS`; implementation `NOT_STARTED`, phase acceptance `PLANNED`, evidence `NOT_PRODUCED` | BLOCKED |
| Phase 03 accepted | Actual Phase 03 상세 + accepted review, `E-P03-*`, cache-free kernel artifact digest | 상세 `REVIEWED_CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_PRODUCED`; review `COMPLETE`/`CHANGES_REQUIRED`; accepted artifact/evidence 없음 | BLOCKED |
| [Phase 04 actual 상세](phase-04-capabilities-customer-profiles.md) accepted | Accepted review, immutable `BoundProfile`/`SolvePlan`, dependency closure와 `E-P04-*` | 상세 `REVIEWED_CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_PRODUCED`; review `COMPLETE`/`CHANGES_REQUIRED`; accepted artifact/evidence 없음 | BLOCKED |
| [Phase 05 actual 상세](phase-05-pair-insertion-initial-portfolio.md) accepted | `Phase05SeedPortfolioHandoff`, `SearchSnapshot`, `SeedPortfolio`, `PairInsertionEvaluator`, 최대 8 validated candidates와 `E-P05-*` | 상세 `REVIEWED_CHANGES_REQUIRED`/`NOT_STARTED`/`NOT_PRODUCED`; review `COMPLETE`/`CHANGES_REQUIRED`; accepted artifact/evidence 없음 | BLOCKED |
| Central pair-removal editor owner | Master Realization/Integrated와 actual Phase 05/06의 owner/API/evidence 배치를 Phase 05/06/Architecture가 공동 승인 | 상위 실행 계획은 Phase 05, actual 상세는 Phase 06으로 충돌 | CONTRACT_GATE |
| Full solution evaluation/comparator tie seam | Phase 03 review `F-P03-004/006`의 solution-level evaluator와 business equality/context tie API가 Phase 03~06 compatible contract로 승인됨 | Phase 03 review가 residual cross-phase blocker로 기록 | CONTRACT_GATE |
| Explicit Phase 06 config | Algorithm/operator/acceptance/termination IDs, versions, ordered registry와 모든 numeric values | Proposed §7/§9만 존재 | REVIEW_REQUIRED |
| Seed/replay contract review | Derivation version, namespace, canonical encoding과 independent reference oracle 승인 | Proposed §7.5/§10만 존재 | REVIEW_REQUIRED |
| Owner/scheduler identity | Exact scheduler task ID와 구현/독립 reviewer 배정 | Task ID 미제공 | OWNER_GATE |

Phase 05 handoff를 받을 때 다음을 전부 대조한다.

```text
each initial candidate:
  cacheFreeValidation == PASS_BY_PHASE_05_AUTHORITY
  problem/travel/profile/evaluation fingerprints == solve snapshot declaration
  every RequestId is exactly one of route ownership or SearchRequestBank
  current/stageBest/solveBest construction can be made without mutable alias
  source growth/vehicle-order/candidate fingerprint has a stable total order
```

여기서 `PASS_BY_PHASE_05_AUTHORITY`는 Phase 07 verifier `PASS`가 아니다. Phase 05의 cache-free construction validation evidence를 뜻한다. 하나라도 다르면 임시 candidate, empty bank, default profile 또는 placeholder objective로 보완하지 않는다.

## 5. 2026-07-28 current Java 25/Maven inventory

Read-only inventory 기준 commit은 `3424277c9c74f8151a83be056a07dd4659331beb`이다. 이 문서를 만들기 전부터 `docs/implementation/` 전체는 Git 기준 untracked였으며 기존 사용자 작업으로 취급한다.

| 항목 | 실제 관찰 | Phase 06 해석 |
|---|---|---|
| Toolchain | Corretto OpenJDK `25.0.3`, Maven `3.9.14`, macOS aarch64 | Java 25 target과 일치하지만 Phase 06 build/replay evidence는 아님 |
| Maven | Root `pom.xml` 한 개, `com.ronext:ro-next:0.1.0-SNAPSHOT`, module 목록 없음 | `rpdptw-solver`와 architecture boundary가 아직 없음 |
| Root dependencies | Google Workflow Executions/Storage, Jackson, JUnit가 같은 classpath | Target core/solver/provider 격리 전 placeholder inventory |
| Main Java | `com.ronext.optimizer` 아래 6개 파일 | Target namespace/package가 없음 |
| Placeholder engine | `AlnsBatchEngine`이 `SplittableRandom(seed)`로 `double` 합성 objective `Map<String,Object>`를 반환 | RPDPTW ALNS/COW/pair/evaluation/replay evidence로 재사용 금지 |
| Test | `AlnsBatchEngineTest` 1개가 status/run number/objective 양수만 확인 | Phase 06의 isolation/step/replay test가 아님 |
| Actual Phase 상세/review | **Live:** Phase 00~14 상세 15개와 독립 review 15/15 actual; review lifecycle은 모두 `COMPLETE` 또는 `FINAL`. Document verdict는 `CHANGES_REQUIRED` 11개(Phase 03~12, 14), `PASS_WITH_RESIDUAL_BLOCKERS` 2개(Phase 00, 13), `ACCEPTED_WITH_APPLIED_CORRECTIONS` 1개(Phase 01), `PASS_AFTER_APPLIED_CORRECTIONS` 1개(Phase 02). **Historical authoring snapshot:** review 00~03은 review 시작 전 존재했고 `APPEARED_DURING_AUTHORING`은 review 04~07이었다. | Historical snapshot은 provenance일 뿐 live count가 아니다. Document review verdict와 implementation/evidence/Phase acceptance를 분리하며 Phase 04/05/07 handoff는 unaccepted이고 Phase 06 accepted entry artifact는 없음 |
| Accepted evidence/review | Phase 00~14 accepted bundle 없음 | Target implementation accepted completion은 여전히 0 |

Inventory fingerprint:

| File | SHA-256 |
|---|---|
| `pom.xml` | `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6` |
| `.sdkmanrc` | `25c276822911b813a58c317ee47b86608e51c79ba921c65706df9f9f74793be5` |
| `AlnsBatchEngine.java` | `4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481` |
| `AlnsBatchEngineTest.java` | `947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829` |

이 문서 작업은 `src`, test, `pom.xml`, `.sdkmanrc`, README/plan/progress, 다른 Phase/review와 `docs/codex/*`를 변경하지 않는다.

## 6. Proposed 변경 module/package/file tree

아래 tree는 **future implementation target**이다. 현재 파일 존재를 주장하지 않는다. Phase 00/05/06 review에서 이름을 바꿀 수 있지만 owner와 dependency 의미는 보존해야 한다.

```text
rpdptw/solver/
├── pom.xml
├── src/main/java/com/ronext/rpdptw/solver/
│   ├── state/
│   │   ├── SearchSnapshotRef.java
│   │   ├── TrialDraft.java
│   │   ├── CowTrialFactory.java
│   │   ├── CowRouteStore.java
│   │   ├── CentralPairRemovalEditor.java        # only if cross-phase review assigns Phase 06
│   │   ├── TrialCommitter.java
│   │   └── StateIsolationFailure.java
│   ├── search/
│   │   ├── AlnsEngine.java
│   │   ├── AlnsSearchState.java
│   │   ├── AlnsStepExecutor.java
│   │   ├── AlnsStepResult.java
│   │   ├── IterationOutcome.java
│   │   ├── StageGuard.java
│   │   ├── Phase1Screener.java
│   │   └── StableChampionReducer.java
│   ├── search/destroy/
│   │   ├── DestroyOperator.java
│   │   ├── DestroyProposal.java
│   │   ├── DestroyOperatorRegistry.java
│   │   └── internal/<versioned-family-implementations>.java
│   ├── search/repair/
│   │   ├── RepairOperator.java
│   │   ├── RepairPlan.java
│   │   ├── RepairResult.java
│   │   └── RepairPipeline.java
│   ├── search/improvement/
│   │   ├── InStepImprovement.java
│   │   ├── ImprovementBudget.java
│   │   └── ImprovementRegistry.java
│   ├── search/adaptive/
│   │   ├── AdaptiveOperatorSelector.java
│   │   ├── OperatorLearningSnapshot.java
│   │   ├── OperatorRewardPolicy.java
│   │   └── ProbabilityVector.java
│   ├── search/acceptance/
│   │   ├── AcceptancePolicy.java
│   │   ├── AcceptanceDecision.java
│   │   ├── AcceptanceStateSnapshot.java
│   │   ├── HillClimbingAcceptance.java
│   │   └── SimulatedAnnealingAcceptance.java
│   ├── random/
│   │   ├── SeedDeriver.java
│   │   ├── SeedDerivationVersion.java
│   │   ├── RngNamespace.java
│   │   ├── RngPurpose.java
│   │   └── StepRandomStreams.java
│   ├── termination/
│   │   ├── Termination.java
│   │   ├── WorkBudget.java
│   │   ├── InterruptionProbe.java
│   │   └── TerminationController.java
│   └── replay/
│       ├── DecisionTrace.java
│       ├── DecisionTraceEvent.java
│       ├── ReplayManifest.java
│       ├── ReproducibilityEnvelope.java
│       └── TraceCanonicalizer.java
└── src/test/java/com/ronext/rpdptw/solver/
    ├── fixture/
    │   ├── AlnsFixtureBuilder.java
    │   ├── TestAlnsConfigBuilder.java
    │   ├── ReplayFixtureBuilder.java
    │   └── ManualInterruptionProbe.java
    ├── oracle/
    │   ├── IndependentSeedDerivationReference.java
    │   ├── FullCopyStepReference.java
    │   ├── IndependentTraceCanonicalizer.java
    │   └── IndependentReplayOracle.java
    ├── state/CowTrialIsolationTest.java
    ├── search/AlnsStepStateMachineTest.java
    ├── search/Phase1ScreenerTest.java
    ├── search/AlnsWorkerRunIT.java
    ├── search/destroy/DestroyOperatorContractTest.java
    ├── search/repair/RepairPipelineContractTest.java
    ├── search/adaptive/AdaptiveOperatorSelectorTest.java
    ├── search/acceptance/AcceptancePolicyTest.java
    ├── random/SeedStreamOwnershipTest.java
    ├── termination/TerminationFaultTest.java
    ├── replay/ReplayDeterminismTest.java
    ├── replay/ReplayCorruptionTest.java
    ├── security/SolverTraceRedactionTest.java
    └── performance/CowCopyWorkAccountingTest.java

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase06SolverArchitectureTest.java
```

Phase 06은 `rpdptw-core`에 search state를 밀어 넣거나 Phase 05 evaluator를 `solver.search` 안에 복사하지 않는다. Shared public contract가 필요하면 semantic owner와 Phase 00/05 architecture review를 먼저 갱신한다.

## 7. Artifact, Java contract, identity와 lifecycle

### 7.1 Artifact와 owner

| Artifact | Owner/입력 | 최소 identity | Lifecycle/소비자 |
|---|---|---|---|
| `SearchSnapshot` | Phase 05 | Problem/travel/profile/evaluation, routes-by-concrete-vehicle, bank, authoritative evaluation, structural/content fingerprint | Immutable; Phase 06 current/best와 warm start |
| `Phase05SeedPortfolioHandoff` / `SeedPortfolio` | Phase 05 | Authority/config/enumeration/tie fingerprints, ordered available `ConstructionCandidate` refs/`SearchSnapshot` fingerprints, unavailable records, validation/evidence/rollback refs | Immutable Phase-1 input; screen/RNG/operator state는 포함하지 않음 |
| `AlnsExecutionConfig` | Phase 06 bind input | Algorithm/operator/repair/improvement/adaptive/acceptance/state strategy/step/termination versions와 explicit values | Run 시작 전 immutable; omitted required field는 bind failure |
| `AlnsSearchState` | Phase 06 | Run/stage, distinct current/stageBest/solveBest, progress, learning, acceptance와 RNG lineage | Completed step 경계에서만 immutable replacement |
| `TrialDraft` | Phase 06 state | Base snapshot fingerprint, trial/run/step identity, copied-route set, independent bank, invalidation generation | Step-local mutable; commit/discard 뒤 참조 금지 |
| `CompletedTrial` | Phase 06 semantic over Phase 05 values | Full structural/evaluation artifact, changed routes, repair/improvement record와 candidate fingerprint | Commit 전 immutable value; stable snapshot으로 freeze하거나 폐기 |
| `DecisionTrace` | Phase 06 | Ordered canonical event list/digest, logical run/step/completed-step 경계. Platform `AttemptId`, thread와 elapsed는 제외 | Immutable append-by-new-value; replay/evidence input |
| `CommittedCandidate` | Phase 06 | Canonical route/bank payload/ref, problem/travel/profile/evaluation declaration/`SolvePlan` authority fingerprint **및 content digest**, non-authoritative declared metric/score/objective claims, candidate/termination/trace/replay identity | Phase 07 candidate verifier input; `PASS` 아님 |
| `ReplayManifest` | Phase 06 | Strong envelope, base/derived seeds, namespace version, stable orders, requested/completed work, warm-start/candidate/trace fingerprints | Independent replay와 retry identity |
| `Phase06EvidenceManifest` | Evidence bundle | Source/build/runtime/config/test/oracle/artifact digests와 rollback point | Reviewer와 Phase 07/10 handoff |

Cache와 temperature, operator weights는 canonical solution source of truth가 아니다. 그러나 replay 결과에 영향을 주므로 exact config/state version과 trace identity에는 포함한다.

### 7.2 Proposed Java 25 type hierarchy와 signatures

```java
package com.ronext.rpdptw.solver.search;

public record AlnsSearchState(
    SearchSnapshotRef current,
    SearchSnapshotRef stageBest,
    SearchSnapshotRef solveBest,
    SearchProgress progress,
    OperatorLearningSnapshot operatorLearning,
    AcceptanceStateSnapshot acceptanceState,
    RngLineage rngLineage,
    DecisionTraceRef trace
) {
    // compact constructor: distinct immutable refs/values and authority equality
}

public record SearchProgress(
    int stageOrdinal,
    long completedStep,
    long requestedCompletedSteps,
    AlnsRunIdentity alnsRun
) {}
```

`SearchSnapshotRef`는 mutable object reference나 provider URI가 아니다. Immutable candidate artifact identity와 안전한 in-process value를 묶는 proposed solver-local handle이다. Top-level 세 슬롯은 서로 다른 handle이어야 하며 동일 content fingerprint를 가질 수는 있다.

```java
public interface AlnsEngine {
    AlnsRunOutcome run(
        SearchSnapshot warmStart,
        AlnsExecutionConfig config,
        AlnsRunIdentity runIdentity,
        long baseSeed,
        InterruptionProbe interruption
    );
}

public interface AlnsStepExecutor {
    AlnsStepResult execute(
        AlnsSearchState before,
        AlnsExecutionConfig config,
        StepRandomStreams random,
        InterruptionProbe interruption
    );
}

public sealed interface AlnsStepResult
        permits AlnsStepResult.Completed,
                AlnsStepResult.Invalid,
                AlnsStepResult.Interrupted,
                AlnsStepResult.Failed {

    record Completed(
        IterationOutcome outcome,
        AlnsSearchState after,
        StepEvidence evidence
    ) implements AlnsStepResult {}

    record Invalid(
        InvalidCandidateEvidence evidence,
        AlnsSearchState unchanged
    ) implements AlnsStepResult {}

    record Interrupted(
        ExceptionalTermination termination,
        InterruptionEvidence evidence,
        AlnsSearchState unchanged
    ) implements AlnsStepResult {}

    record Failed(
        SearchFailure failure,
        AlnsSearchState unchanged
    ) implements AlnsStepResult {}
}
```

Baseline에서 `Invalid`와 `Failed`는 run을 fail-closed 종료한다. `unchanged`는 input state와 canonical equality뿐 아니라 top-level current/best bytes/fingerprint equality를 만족해야 한다.

```java
package com.ronext.rpdptw.solver.search.destroy;

public interface DestroyOperator {
    OperatorVersion version();

    DestroyProposal propose(
        SearchSnapshot current,
        DestroyConfig config,
        RandomStream random
    );
}

public record DestroyProposal(
    OperatorVersion operator,
    int requestedRemovalCount,
    List<RequestId> orderedUniqueRequests,
    Set<RouteId> sourceRoutes,
    ScoreAndTieEvidence evidence,
    Digest decisionTraceDigest
) {}
```

Destroy operator는 `SearchSnapshot`을 mutate하지 않는다. Proposal 적용은 Phase 06 `CentralPairRemovalEditor`만 수행하며, editor는 Phase 05의 stable pair/route-bank identity와 functional immutable transition 의미를 보존한다.

```java
package com.ronext.rpdptw.solver.search.repair;

public interface RepairOperator {
    OperatorVersion version();

    RepairResult repair(
        TrialDraft postDestroy,
        RepairPlan plan,
        PairInsertionEvaluator phase05Evaluator,
        RandomStream random
    );
}

public record RepairPlan(
    RepairScope scope,
    ShortlistPolicy shortlist,
    NewRoutePolicy newRoute,
    StableTiePolicy tie,
    RepairBudget budget
) {}
```

`PairInsertionEvaluator`의 실제 proposed signature와 exhaustive/bounded honesty는 actual Phase 05 §6~§7이 소유한다. Phase 06 central removal editor와 COW apply는 Phase 06 책임이며 Phase 05 construction-only `ConstructionTransition`을 destroy 구현으로 cast하지 않는다.

```java
package com.ronext.rpdptw.solver.search.adaptive;

public interface AdaptiveOperatorSelector {
    SelectedOperator select(
        OrderedOperatorRegistry registry,
        OperatorLearningSnapshot state,
        RandomStream random
    );

    OperatorLearningSnapshot update(
        OrderedOperatorRegistry registry,
        OperatorLearningSnapshot before,
        IterationOutcome completedOutcome,
        AdaptiveConfig config
    );
}

public record OperatorLearningSnapshot(
    List<OperatorLearningEntry> entriesInCanonicalOrder,
    long completedUpdates,
    Fingerprint fingerprint
) {}
```

```java
package com.ronext.rpdptw.solver.search.acceptance;

public interface AcceptancePolicy {
    AcceptanceDecision decide(
        EvaluatedCandidate candidate,
        SearchSnapshot current,
        BoundComparator comparator,
        AcceptanceStateSnapshot state,
        RandomStream random
    );

    AcceptanceStateSnapshot advanceAfterCompletedStep(
        AcceptanceStateSnapshot before,
        IterationOutcome outcome
    );
}

public sealed interface AcceptanceStateSnapshot
        permits HillClimbingState, SimulatedAnnealingState {}
```

Hill Climbing config에는 temperature가 없고 non-worse/equal 처리와 tie behavior가 explicit해야 한다. SA config는 scalar energy projection ID/version, initial temperature, completed-step cooling schedule, scale와 numeric contract가 모두 있어야 한다. Lexicographic objective를 근거 없는 Big-M 합으로 바꾸지 않는다.

```java
package com.ronext.rpdptw.solver.termination;

public sealed interface Termination
        permits NormalTermination, ExceptionalTermination {}

public enum NormalTermination implements Termination {
    MAX_STEPS_REACHED,
    NO_STRICT_IMPROVEMENT, // Phase 10 owner; Phase 06 does not emit for a single worker
    MAX_ROUNDS_REACHED     // Phase 10 owner; Phase 06 does not emit for a single worker
}

public enum ExceptionalTermination implements Termination {
    WATCHDOG_REACHED,
    CANCELLED,
    RESOURCE_LIMIT_REACHED,
    PLATFORM_TIMEOUT,
    FAILED,
    INCOMPLETE
}
```

### 7.3 COW identity와 lifecycle

```text
SearchSnapshot(base)
  ├─ immutable route values R1, R2, R3
  ├─ immutable bank B0
  └─ authoritative fingerprint F0

open TrialDraft
  ├─ route refs initially point only to immutable R1, R2, R3
  ├─ independent mutable/trial-owned bank B1
  └─ derived cache generation invalid

first write R2
  ├─ copy R2 → trial-owned R2'
  ├─ R1/R3 remain shared immutable
  └─ copiedRouteIds = [R2]

freeze after full evaluation
  └─ CompletedTrial C1

accept + all state updates succeed
  └─ new immutable SearchSnapshot F1 and new AlnsSearchState published once

reject/invalid/fault/cancel/watchdog
  └─ discard draft; F0/current/stageBest/solveBest unchanged
```

Copy counter와 alias audit는 evidence field다.

```text
uniqueMutatedRouteIds == copiedRouteIds
parentMutableAliasCount == 0
bankMutableAliasCount == 0
discardedDraftReachableReferenceCount == 0
```

Java garbage collection 자체를 correctness oracle로 사용하지 않는다. `discardedDraftReachableReferenceCount`는 explicit owner graph/test instrumentation으로 검증한다.

### 7.4 Explicit config와 미확정 수치 gate

`AlnsExecutionConfig`는 다음 nested config를 반드시 exact ID/version과 함께 가진다.

```text
algorithmVersion
stateStrategy = CHANGED_ROUTE_COW_V1
ordered destroy operator registry + per-operator config
ordered repair operator registry + per-operator config
bounded improvement registry + explicit NONE or budgets
adaptive selection/reward/update config
acceptance policy + state lifecycle + all numeric parameters
seed derivation version + namespace mapping
stable request/vehicle/route/position/operator/candidate tie policies
requested completed steps
watchdog/resource/interruption policy
trace schema/canonicalization version
```

다음 omission은 bind error다.

- Removal lower/upper/count rounding
- Repair scope와 shortlist/top-N/power bias/exact-evaluation budget
- Bounded improvement operator별 attempt/work budget
- Initial weight, reward mapping, update period, reaction factor, exploration floor
- Acceptance policy; SA일 때 energy projection, initial temperature, cooling, scale
- `screenMaxSteps`/worker requested steps와 watchdog policy
- Seed derivation/version/namespace와 stable tie policy

수치 상태를 다음처럼 분리한다.

| Config | 허용 목적 | 지위 |
|---|---|---|
| `TEST_ONLY_COW_THREE_STEP_V1` | §10의 작은 fixture와 red→green test | Explicit test-only; official 사용 금지 |
| `TEST_ONLY_SA_BOUNDARY_V1` | Acceptance 확률/temperature 손 계산 | Explicit test-only; official 사용 금지 |
| `OGC2024_LEGACY_REPLAY` | Differential compatibility replay | Legacy test/experiment only; general default 금지 |
| `EXPERIMENT_<id/version>` | Calibration corpus 측정 | Experiment manifest와 approval 전 official 금지 |
| Official manifest values | Phase 14 official run | `Q-BENCH-02` calibration/승인 전 생성 불가 |

`OGC2024_LEGACY_REPLAY`의 원문 값은 다음과 같이 격리한다.

```text
9 operator selection slots
request removal ratio 5%..15%
maxDestroy = 1000
Shaw weights = 9 / 3 / 2
worst rank exponent = 3
reward = 20 / 10 / 2 / 0
update period = 100
reaction factor = 0.5
exploration floor = 0.01
repair shortlist N = min(max(12, floor(2K/100)), candidateRouteCount)
optional power-biased sample within top 5N
```

이 값은 legacy preset의 differential oracle에만 적용한다. 일반 RPDPTW operator 수, removal 비율, shortlist, reward, temperature 또는 official step의 hidden default로 복사하지 않는다.

### 7.5 Seeded RNG stream ownership

RNG 설계 목표는 “같은 global generator를 같은 순서로 우연히 호출”하는 것이 아니라 **결정 목적별 random ownership을 고정**하는 것이다.

Proposed derivation context:

```text
baseSeed
+ seedDerivationVersion
+ seedContextFingerprint
+ phaseKind                 // PHASE1_SCREEN | PHASE2_WORKER
+ portfolioSlot?           // phase-1 only
+ roundOrdinal?            // Phase 10 supplies when applicable
+ workerOrdinal?           // worker run only
+ alnsRunOrdinal
+ completedStepOrdinal
+ purpose
+ operatorId/version?      // operator-owned stream only
→ derived stream seed
```

`seedContextFingerprint`는 problem/travel/profile/build/algorithm config와 logical run identity의 canonical pre-run projection이며 base/derived seed, trace와 candidate output을 포함하지 않는다. 따라서 manifest self-reference 없이 먼저 계산할 수 있다. `AttemptId`, OS thread, executor index, current time, elapsed time, cache hit/miss와 completion order는 derivation input이 아니다. Logical worker retry는 같은 stream을 재생한다.

`RngPurpose` 최소 namespace:

```text
DESTROY_OPERATOR_SELECTION
REPAIR_OPERATOR_SELECTION
DESTROY_OPERATOR_INTERNAL
REPAIR_SHORTLIST_SAMPLING
IN_STEP_IMPROVEMENT_SELECTION
ACCEPTANCE_DRAW
```

규칙:

1. 한 purpose의 draw 수가 달라져도 다른 purpose의 stream은 이동하지 않는다.
2. Operator internal stream은 operator ID/version까지 namespace에 포함한다.
3. SA draw를 사용하지 않는 strictly-better/equal step도 다음 step/operator stream을 바꾸지 않는다.
4. Invalid/interrupted step은 mutable RNG state를 남기지 않는다. 재시도 정책이 승인된 경우 같은 logical step은 같은 stream에서 시작해야 한다.
5. Random removal은 stable assigned request universe를 canonical order로 만든 뒤 dedicated stream으로 선택한다.
6. Weighted operator selection은 ordered registry의 stable cumulative scan 또는 review-approved equivalent를 사용한다.
7. Derived seed와 draw count/value digest는 replay trace에 기록하되 seed를 quality 선별 대상으로 사용하지 않는다.
8. Canonical decision trace와 candidate fingerprint에는 logical run/step identity만 넣는다. Infrastructure `AttemptId`, thread/executor ID, elapsed와 provider execution ID는 별도 observation/evidence에 둘 수 있지만 canonical replay trace를 바꾸지 않는다.

Seed mixing/canonical byte encoding의 구체 algorithm은 Phase 06 review에서 version으로 확정한다. Production implementation과 독립 reference oracle은 코드를 공유하지 않아야 한다.

### 7.6 Dependency direction

```text
rpdptw-solver
  → rpdptw-core

rpdptw-solver MUST NOT
  → rpdptw-verification
  → rpdptw-application
  → profile implementation/catalog
  → provider/cloud/HTTP/storage SDK
  → optimizer vendor API
```

Phase 06은 다음 upstream public/internal-reviewed contract만 소비한다.

```text
Phase 03/04:
  ProblemInstance / PreparedTravel / BoundProfile / SolvePlan
  RouteEvaluationKernel / BoundComparator / authoritative fingerprints

Phase 05:
  SearchSnapshot / SearchRequestBank
  BoundInsertionAuthority / PairInsertionEvaluator / FeasibleInsertionOption
  ConstructionCandidate / SeedPortfolio
  Phase05SeedPortfolioHandoff
```

Phase 07은 solver module을 compile-depend하지 않는다. Phase 06 handoff의 route/bank canonical values와 authority fingerprints를 core/verification-owned input으로 변환하는 boundary는 Phase 07/Application review가 소유한다.

## 8. State transition, completed-step pseudocode와 termination

### 8.1 Trial과 step state transition

```text
STEP_READY
→ OPERATOR_SELECTED
→ TRIAL_OPEN
→ DESTROY_PROPOSED
→ PAIR_DESTROY_APPLIED
→ REPAIR_COMPLETED
→ BOUNDED_IMPROVEMENT_COMPLETED
→ STRUCTURE_VALIDATED
→ FULL_EVALUATED
→ STAGE_GUARD_EVALUATED
→ ACCEPTED | REJECTED
→ NEXT_CURRENT_AND_BEST_DERIVED
→ ADAPTIVE_AND_ACCEPTANCE_DERIVED
→ NEXT_STATE_DERIVED_WITH_COMPLETED_STEP_RECORDED
→ NEXT_STATE_PUBLISHED_ONCE
→ COMPLETED
```

다음 side transition은 `NEXT_STATE_PUBLISHED` 전에만 허용한다.

```text
malformed proposal / pair editor defect / repair DEFECT
  → INVALID_CANDIDATE or FAILED
  → TRIAL_DISCARDED
  → RUN_FAILED (baseline)

cancellation / watchdog / resource / platform interruption
  → INTERRUPTED
  → TRIAL_DISCARDED
  → exact exceptional termination

hard-infeasible full candidate
  → INVALID_CANDIDATE
  → TRIAL_DISCARDED
  → RUN_FAILED (baseline fail-closed)
```

Normal `REJECTED`는 `current`를 보존하지만 completed-step/adaptive/temperature update를 포함한다. Completed-step과 trace는 immutable `nextState` 안에서 먼저 파생되고 그 aggregate와 별도로 publish되지 않는다. Publish 뒤에 실패 가능한 state update나 후속 counter write를 두지 않는다.

Cancellation/watchdog/resource/platform signal은 위 named boundary마다 cooperative probe한다. Final pre-publication probe와 `NEXT_STATE_PUBLISHED_ONCE` 사이가 step의 linearization boundary다. Probe가 publish 전에 signal을 관찰하면 draft/next-state local value를 폐기하고 input state를 그대로 반환한다. Single publication이 끝난 뒤 도착한 signal은 완료된 step을 소급 취소하지 않고 다음 run boundary에서 exact exceptional termination으로 기록한다.

### 8.2 Canonical ALNS step pseudocode

```text
executeStep(before, config, interruption):
  require authority fingerprints and before-state invariants
  require explicit config closure
  random := derive purpose streams from logical completedStep identity

  if interruption.present():
      return Interrupted(exact signal, unchanged=before)

  selectedDestroy := adaptive.select(
      ordered destroy registry,
      before.operatorLearning,
      random.DESTROY_OPERATOR_SELECTION)

  selectedRepair := adaptive.select(
      ordered repair registry,
      before.operatorLearning,
      random.REPAIR_OPERATOR_SELECTION)

  if interruption.presentAt(OPERATOR_SELECTED):
      return Interrupted(exact signal, unchanged=before)

  trial := cowTrialFactory.open(before.current)
  // independent bank; routes copied only on first write

  try:
      proposal := selectedDestroy.propose(
          before.current,
          explicit destroy config,
          random.forDestroyOperator(selectedDestroy.version))

      if interruption.presentAt(DESTROY_PROPOSED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      validate ordered-unique proposal against stable assigned universe
      destroyResult := centralPairRemovalEditor.removePairs(trial, proposal)
      assert exact route/bank partition after removal

      if interruption.presentAt(PAIR_DESTROY_APPLIED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      repairResult := selectedRepair.repair(
          destroyResult.trial,
          explicit repair plan,
          phase05PairInsertionEvaluator,
          random.REPAIR_SHORTLIST_SAMPLING)

      if repairResult == DEFECT:
          discard trial
          return Failed(OPERATOR_OR_PAIR_EDIT_DEFECT, unchanged=before)

      if interruption.presentAt(REPAIR_COMPLETED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      improvedTrial := run configured bounded improvement with a probe
          before/after every configured attempt/work unit
      // explicit NONE is allowed; omitted config is not

      if interruption.presentAt(BOUNDED_IMPROVEMENT_COMPLETED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      evaluated := phase03_04Kernel.fullEvaluate(improvedTrial)

      if interruption.presentAt(FULL_EVALUATED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      if evaluated is structurally invalid or hard-infeasible:
          discard trial
          return Invalid(evidence, unchanged=before)

      guardPassed := solvePlanStageGuard.permits(evaluated, before.stageBest)
      if interruption.presentAt(STAGE_GUARD_EVALUATED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      if not guardPassed:
          decision := REJECT
      else:
          decision := acceptance.decide(
              evaluated,
              before.current,
              boundComparator,
              before.acceptanceState,
              random.ACCEPTANCE_DRAW)

      if interruption.presentAt(ACCEPTANCE_DECIDED):
          discard trial
          return Interrupted(exact signal, unchanged=before)

      completedTrial := freeze evaluated trial
      candidateSnapshot := freezeAsImmutableSnapshotValue(completedTrial)
      // No state slot is published here.
      nextCurrentContent := decision.accept
          ? candidateSnapshot
          : immutableContentOf(before.current)
      nextStageBestContent := guardPassed
          ? strictlyBestContent(immutableContentOf(before.stageBest), candidateSnapshot)
          : immutableContentOf(before.stageBest)
      nextSolveBestContent := guardPassed
          ? strictlyBestContent(immutableContentOf(before.solveBest), candidateSnapshot)
          : immutableContentOf(before.solveBest)

      // The same immutable content/artifact may win multiple slots, but handles may not alias.
      nextCurrent := distinctSlotRef(CURRENT, nextCurrentContent)
      nextStageBest := distinctSlotRef(STAGE_BEST, nextStageBestContent)
      nextSolveBest := distinctSlotRef(SOLVE_BEST, nextSolveBestContent)
      assert pairwiseDistinctHandles(nextCurrent, nextStageBest, nextSolveBest)
      outcome := classify using cache-free comparator and decision

      if interruption.presentAt(NEXT_CURRENT_AND_BEST_DERIVED):
          discard trial and unpublished slot refs
          return Interrupted(exact signal, unchanged=before)

      nextLearning := adaptive.update(before.operatorLearning, outcome, config)
      nextAcceptance := acceptance.advanceAfterCompletedStep(
          before.acceptanceState, outcome)
      nextTrace := append canonical completed-step events

      if interruption.presentAt(ADAPTIVE_AND_ACCEPTANCE_DERIVED):
          discard trial and unpublished next values
          return Interrupted(exact signal, unchanged=before)

      nextState := construct entirely immutable state with:
          completedStep = before.completedStep + 1
          current/stageBest/solveBest distinct handles
          nextLearning / nextAcceptance / next RNG lineage / nextTrace

      if interruption.presentAt(PRE_PUBLICATION):
          discard trial and unpublished nextState
          return Interrupted(exact signal, unchanged=before)

      atomically publish nextState once
      return Completed(outcome, nextState, evidence)

  catch operator/config/arithmetic/identity exception:
      discard trial
      return Failed(typed failure, unchanged=before)
```

여기서 `guardPassed`는 `solvePlanStageGuard.permits(...)`의 결과다. Guard를 통과하지 못한 candidate는 정상 `REJECTED` completed step일 수는 있지만 `current`, `stageBest`, `solveBest` 어느 슬롯의 content도 바꾸지 않는다. `strictlyBestContent`는 Phase 03/04 bound comparator를 사용한다. Equal candidate는 best content를 바꾸지 않고 canonical tie evidence만 기록한다. Candidate identity나 completion order로 business objective equality를 “improvement”로 바꾸지 않는다. 한 global-best candidate가 세 slot의 동일 immutable content가 되더라도 top-level `SearchSnapshotRef` handle은 slot별로 새로 만들고 pairwise distinct해야 한다.

### 8.3 Phase-1 screen과 stable champion

```text
available portfolio slots in canonical 4×2 order
→ derive independent screen run namespace per slot
→ run exact explicit test/experiment screenMaxSteps
→ require MAX_STEPS_REACHED
→ require Phase 05/03/04 cache-free validation equality
→ collect screen best in slot order, not completion order
→ reduce with bound comparator
→ on EQUAL use stable tuple:
     growthPolicyOrdinal
     vehicleOrderPolicyOrdinal
     candidateContentFingerprint
→ phase-1 champion
```

모든 available candidate가 정상 step budget을 완료해야 champion을 확정한다. `CLOCK UNAVAILABLE`은 Phase 05 manifest의 unavailable slot이며 실패 screen으로 둔갑시키지 않는다. 한 screen의 fault/cancel/watchdog/invalid outcome을 무시하고 나머지로 champion을 만드는 정책은 별도 explicit product/quality approval 없이는 금지한다.

### 8.4 Termination과 last safe point

Single `AlnsRun`:

| Condition | Termination | completed-step 처리 | Candidate status |
|---|---|---|---|
| Exact requested steps 전체 완료 | `MAX_STEPS_REACHED` | requested와 exact equality | Last committed `solveBest`를 candidate payload로 만들 수 있음 |
| Cooperative cancel | `CANCELLED` | 마지막 completed boundary까지만 | Draft discard; last committed best는 recovery input일 수 있으나 정상 완료 아님 |
| Monotonic watchdog | `WATCHDOG_REACHED` | 미완료 step 미계수 | 동일 |
| Resource guard | `RESOURCE_LIMIT_REACHED` | 미완료 step 미계수 | 동일 |
| Platform boundary가 record 완성을 막음 | `PLATFORM_TIMEOUT` | 추정/보정 금지 | 정상 candidate 완료 주장 금지 |
| Operator/state/evaluation defect | `FAILED` | 미완료 step 미계수 | Draft discard; defect evidence |
| Upstream/declared work 불완전 | `INCOMPLETE` | 누락 work 명시 | 정상 완료 아님 |

Exceptional last committed best를 Phase 07에 recovery candidate로 전달할지 여부는 future product/error contract다. 전달하더라도 exact exceptional termination을 유지하고 Phase 07 두 verifier 없이는 정상 result가 아니다.

Phase 10 future coordinator가 소유하는 상태:

```text
all declared workers complete and verified
→ stable round champion
→ compare with previous champion
→ EQUAL/WORSE: NO_STRICT_IMPROVEMENT
→ strictly better and maxRounds reached: MAX_ROUNDS_REACHED
```

이 Phase 10 test는 현재 **future red**이며 Phase 06 exit test로 green을 위장하지 않는다.

### 8.5 Replay manifest와 mismatch

`ReplayManifest` 최소 필드:

```text
schema/version
problem/travel/numeric/time/adapter fingerprints
bound profile/evaluation/objective/SolvePlan fingerprints
algorithm/operator/repair/improvement/adaptive/acceptance/state-strategy versions
build/runtime compatibility fingerprint
base seed, derivation version, all actual derived seed identities
phase kind, portfolio slot, round/worker/alns-run ordinals
warm-start candidate fingerprint
requested/completed steps
stable ordering/tie policy versions
exact termination
last completed step and rollback/discard evidence
canonical decision trace digest
committed current/stageBest/solveBest fingerprints
candidate payload fingerprint
```

Mismatch classification:

| Mismatch | 판정 |
|---|---|
| Manifest identity 또는 authority fingerprint 다름 | `NOT_COMPARABLE`, replay 시작 금지 |
| 같은 strong envelope인데 trace event/derived seed/draw digest 다름 | `REPLAY_MISMATCH`, integrity failure |
| Trace 같고 candidate bytes/fingerprint 다름 | `REPLAY_MISMATCH`, state/COW/canonicalization defect |
| Candidate 같고 trace digest 다름 | `REPLAY_MISMATCH`, hidden nondeterminism 또는 trace defect |
| Exceptional termination 또는 다른 requested work | Strong-result equality 주장 금지; typed comparison record만 |
| Parallel schedule만 다르고 envelope 동일 | Trace/candidate exact equality가 필요 |

## 9. Destroy, repair, adaptive selection과 acceptance contract

### 9.1 Destroy operator family

모든 family는 ordered unique `RequestId` proposal만 만들고 route/bank를 직접 수정하지 않는다.

| Family | 입력/선택 의미 | Deterministic/RNG contract | 필수 failure test |
|---|---|---|---|
| Random removal | Assigned request universe의 seeded subset | Stable request order + operator-owned stream | Duplicate/unknown/unassigned ID proposal 거부 |
| Related/Shaw | Distance, window, demand/resource similarity | Prepared/bound facts만; score/tie 순서와 seed explicit | Raw customer field/route index 사용 탐지 |
| Route removal | 선택 route의 complete requests | Route ID stable order; nonempty에서 configured min/max/rounding | 0개 또는 upper bound 초과 거부 |
| Historical edge/action | 낮은 품질 adjacency/transition history | Immutable service identity와 versioned history | Mutable route index/history alias 거부 |
| Worst/Semi-worst | Removal improvement ranking bias | Phase 03/04 comparator facts + explicit rank bias | Operator 직접 mutation/finite hard penalty 거부 |
| Location-oriented | Pickup/delivery/service-entry relation | Delivery-only와 real pair entry 의미 분리 | Logical pickup을 physical location으로 위조하는 경우 거부 |

Active family/slot 수와 각 수치는 config가 소유한다. “9 operator family”를 일반 기본으로 해석하지 않는다. Legacy의 9 selection slots은 동일 family의 여러 configured slot을 포함할 수 있는 compatibility preset 의미다.

### 9.2 Repair pipeline

```text
ordered repair request scope
→ static compatibility/capacity/time-overlap fast gate
→ all existing routes cheap promising score
→ explicit stable top-N or seeded power-biased shortlist
→ shortlist + every legal NEW_ROUTE/unused concrete vehicle option
→ Phase 05 common atomic pair evaluator
→ Phase 03/04 bound comparator + stable request/vehicle/route/position tie
→ Phase 06 COW apply of the Phase 05 feasible option or keep request in bank
→ COMPLETE / PARTIAL / NO_FEASIBLE / DEFECT
```

규칙:

- Cheap score, shortlist와 seeded sample은 feasibility/objective authority가 아니다.
- Phase 06은 pickup/delivery position, prepared travel와 hard feasibility를 자체 계산하지 않는다.
- `NEW_ROUTE`는 real unused `VehicleId`를 소비하며 type/count sentinel을 만들지 않는다.
- `PARTIAL_REINSERTION`과 `NO_FEASIBLE_INSERTION`은 route/bank exact partition과 hard feasibility를 만족하면 full-evaluated trial이 될 수 있다.
- `REMOVED_ONLY`/`REMOVED_PLUS_EXISTING_BANK`를 trace와 config에 기록한다.
- Existing route ranking과 new-route option enumeration의 tie order를 explicit하게 고정한다.

### 9.3 Adaptive selection

`OperatorLearningSnapshot`은 canonical operator order로 다음을 보존한다.

```text
operator ID/version
selection count
completed outcome counts
reward accumulator
current weight
selection probability/mass
last update completed-step
```

Gate:

1. 활성 operator가 0이면 config bind failure다.
2. 분모 0을 안전하게 처리하고 NaN/Infinity/negative/zero-active probability를 거부한다.
3. 모든 active probability는 finite·positive이고 합이 review-approved exact tolerance/representation에서 1이다.
4. 같은 pre-state/outcome/config는 같은 next snapshot/fingerprint를 만든다.
5. `INVALID_CANDIDATE`/`INTERRUPTED`는 count/reward/weight/update period를 움직이지 않는다.
6. State lifecycle `RESET_EACH_ALNS_PHASE` 또는 `CARRY_ACROSS_HYBRID_PHASES`는 explicit하다. Phase 06 ALNS-only baseline은 route pool lifecycle을 구현하지 않는다.
7. Registry discovery/classpath iteration order는 selection order가 아니다.

### 9.4 Acceptance와 temperature/policy

Acceptance 전 순서:

```text
structural exactness
→ authoritative full hard feasibility
→ SolvePlan stage guard
→ bound comparator relation to current
→ configured acceptance policy
```

Hill Climbing:

- Explicit equal-candidate policy와 accepted relation을 config로 고정한다.
- Temperature/random draw가 없다.
- 첫 phase 또는 후속 phase의 hidden default가 아니다.

Simulated Annealing:

- Scalar energy projection은 profile/SolvePlan에 승인된 exact ID/version이어야 한다.
- Lexicographic vector를 임의 Big-M, `cost/K`, raw backend objective로 합치지 않는다.
- `initialTemperature`, step-based `cooling`, `scale`, min/max numeric validation과 state lifecycle을 explicit하게 제공한다.
- Worse candidate의 draw는 `ACCEPTANCE_DRAW` stream만 사용한다.
- Cooling은 completed step 뒤에만 적용한다.
- Invalid/interrupted step은 temperature와 draw lineage를 전진시키지 않는다.
- Same candidate/current/state/draw는 same decision을 만든다.

Acceptance policy가 없거나 temperature parameter 일부가 누락되면 “보수적 reject”로 fallback하지 않고 pre-run config binding을 실패시킨다.

## 10. Independent replay oracle와 fixture

### 10.1 Oracle 독립성

Production code를 두 번 호출하는 것만으로 independent replay evidence를 만들 수 없다. Test source의 `IndependentReplayOracle`은 다음 규칙을 지킨다.

1. `solver.state`, `solver.search`, `solver.random`, `solver.replay` production implementation을 import하지 않는다.
2. Phase 03/05의 immutable semantic contract와 canonical fixture data만 소비한다.
3. COW 대신 모든 route/bank를 deep-copy하는 `FullCopyStepReference`를 사용한다.
4. Seed derivation을 production helper와 공유하지 않고 spec/version에 따라 별도 구현한다.
5. Stable sorting과 trace canonicalization도 별도 reference implementation을 사용한다.
6. Expected outcome/trace/fingerprint는 hand table 또는 committed test resource digest로 고정한다.
7. Production trace를 expected input으로 다시 echo하지 않는다.

### 10.2 Exact test-only fixture

`ReplayFixtureBuilder.oneVehicleFourRequestPairsV1()`:

```text
Problem: test-only integer units, complete directed PreparedTravel
Vehicles: V01
Requests: R01, R02, R03, R04
Initial route: R01 pickup/delivery, R02 pickup/delivery
Initial bank: R03, R04
All route/request/position orders: ascending stable ID
Bound comparator: test-only lexicographic
  totalUnassignedCount
  totalDistance
  stable tie key only after objective equality
```

`TestAlnsConfigBuilder.threeCompletedStepsHillClimbingV1()`:

```text
config ID: TEST_ONLY_COW_THREE_STEP_V1
requested completed steps: 3
destroy registry: one RANDOM_REMOVAL_TEST_V1 slot
removal count: exactly 1
repair registry: one EXHAUSTIVE_REPAIR_TEST_V1 slot
repair scope: REMOVED_PLUS_EXISTING_BANK
shortlist: all routes, no approximation
NEW_ROUTE: disabled because no unused vehicle
bounded improvement: explicit NONE_V1
adaptive update: explicit deterministic test-only values in fixture resource
acceptance: HILL_CLIMBING_TEST_V1 with explicit equal policy
base seed: explicit fixture value
watchdog: manual probe, no wall-clock sleep
```

`TestAlnsConfigBuilder.saBoundaryV1()`은 SA probability/temperature만 검증하는 별도 explicit config다. 이 수치와 3 step은 official `Q-BENCH-02` 값이 아니다.

### 10.3 Oracle outputs

Oracle은 step별 다음 exact row를 만든다.

```text
logical step ordinal / completedStep ordinal
  (platform AttemptId is excluded from the canonical row)
purpose namespace + derived seed
selected destroy/repair operator
ordered removal proposal
ordered repair attempts and selected insertion
pre/post route-bank structural fingerprint
full objective vector
guard and acceptance relation/draw
iteration outcome
current/stageBest/solveBest fingerprints
adaptive/acceptance fingerprints
trace prefix digest
```

Production과 oracle row 하나라도 다르면 `REPLAY_MISMATCH`다. 최종 fingerprint만 같다고 중간 mismatch를 허용하지 않는다.

## 11. Exact test plan

모든 class/method는 현재 **future red**다. Source 파일, module과 test가 아직 없으므로 이 표 자체를 pass evidence로 사용하지 않는다.

### 11.1 Exact class/method/fixture/oracle matrix

| Test class | Exact method | Fixture/builder | Oracle | Pass criterion |
|---|---|---|---|---|
| `CowTrialIsolationTest` | `copiesChangedRouteOnlyOnFirstWrite()` | `AlnsFixtureBuilder.twoRoutesOneMovedPair()` | Owner-graph/copy counter | Copied route IDs가 unique mutated route와 exact equality |
|  | `sharesOnlyImmutableUnchangedRoutes()` | Same | Mutability probe | Unchanged route는 immutable share, writable alias 0 |
|  | `copiesBankIndependentlyBeforeMutation()` | Same | Identity/mutation probe | Base bank bytes/fingerprint 불변 |
|  | `keepsCurrentStageBestAndSolveBestTopLevelRefsDistinct()` | `threeSnapshotSlotsSameContent()` | Reference identity + content | `assertNotSame` handles, content equality allowed |
|  | `rejectDiscardLeavesParentAndBestFingerprintsUnchanged()` | `rejectableCompletedTrial()` | Pre-state deep canonical bytes | 모든 pre/post parent/best bytes exact equality |
|  | `exceptionDiscardLeavesNoReachableTrialMutation()` | `throwingPairEditor()` | Explicit owner graph | Trial-owned mutable reachable ref 0 |
|  | `cancelDiscardLeavesParentUnchanged()` | Manual cancellation probe | Pre-state bytes | Candidate/bank/cache/fingerprint unchanged |
| `AlnsStepStateMachineTest` | `acceptedImprovementPublishesOneImmutableNextState()` | `improvingTrial()` | State transition table | 한 번 publish, current/best 모두 correct |
|  | `acceptedNonImprovingChangesCurrentButNeverBest()` | `saAcceptedWorseTrial()` | Comparator hand relation | current만 변경, best fingerprints 유지 |
|  | `rejectedFeasibleTrialCountsOneCompletedStep()` | `hillClimbingRejectedTrial()` | State transition table | completed +1, reject reward/cooling once |
|  | `invalidCandidateAdvancesNoStateAndFailsClosed()` | `hardInvalidTrial()` | Pre-state bytes | progress/reward/temperature/count unchanged, `FAILED` |
|  | `interruptedStepAdvancesNoState()` | Probe at each lifecycle boundary | Pre-state bytes | 모든 boundary에서 exact unchanged |
|  | `interruptionRaceUsesPublicationAsSingleLinearizationPoint()` | Signal immediately before vs after publication | State/publication recorder | Before: unchanged/incomplete; after: step completed once and signal applies at next boundary |
|  | `adaptiveFailureBeforePublicationRollsBackWholeStep()` | Throwing adaptive updater | Pre-state bytes | Candidate도 commit되지 않고 completed 0 |
|  | `stageGuardRunsBeforeAcceptanceDraw()` | Guard-failing candidate + draw counter | Call-order recorder | Acceptance calls/draws 0 |
|  | `stageGuardFailureCannotUpdateCurrentStageBestOrSolveBest()` | Guard-failing candidate with attractive lower-stage objective | Pre-state bytes + comparator relation | 세 snapshot slot과 fingerprint가 모두 unchanged; reject update만 completed |
|  | `globalBestImprovementPublishesDistinctSlotHandlesForSameContent()` | One candidate improves current/stage/solve best | Reference identity + content fingerprint | 세 handle pairwise distinct, 세 content fingerprint는 candidate와 equality 가능 |
|  | `hardInfeasibleCandidateNeverReachesAcceptance()` | Phase 03 rejected evaluation | Call-order recorder | Acceptance calls 0 |
| `DestroyOperatorContractTest` | `randomRemovalUsesStableUniverseAndDedicatedStream()` | Shuffled assigned map variants | Independent selection reference | Same ordered request proposal |
|  | `relatedRemovalUsesPreparedAndBoundFactsOnly()` | Relatedness hand matrix | Hand score table | Exact score/order, raw field access 0 |
|  | `routeRemovalHonorsExplicitNonZeroBoundsAndRounding()` | One/two-route boundaries | Hand count oracle | Exact requested count, nonempty result |
|  | `historicalRemovalUsesStableServiceIdentityNotRouteIndex()` | Reindexed same routes | Identity metamorphic oracle | Same RequestIds |
|  | `worstRemovalOnlyProposesAndNeverMutates()` | Removal delta table | Parent bytes + comparator | Proposal exact, parent unchanged |
|  | `locationRemovalDistinguishesLogicalAndPhysicalPickup()` | Mixed delivery-only/real pair | Entry-location hand table | No fake physical location |
|  | `rejectsDuplicateUnknownOrUnassignedProposal()` | Corrupt proposals | Contract validator | Typed defect for each corruption |
|  | `legacyReplayPresetMatchesVersionedDifferentialTrace()` | `OGC2024_LEGACY_REPLAY` fixture | Independent legacy reference | Exact slot/config/trace; no default leakage |
| `RepairPipelineContractTest` | `usesCheapScoreOnlyForShortlist()` | Misleading cheap score fixture | Exhaustive Phase 05 oracle | Final insertion follows exact evaluator |
|  | `delegatesEveryShortlistedOptionToPhase05ExactEvaluator()` | Spy evaluator | Expected call set | Call set equals legal shortlist/options |
|  | `includesEveryLegalUnusedVehicleNewRouteOption()` | Two unused vehicles | Stable vehicle-order oracle | Both options evaluated in order |
|  | `partialReinsertionPreservesRouteBankExactPartition()` | Two removed, one feasible | XOR oracle | Stable completed trial and one bank member |
|  | `noFeasibleInsertionPreservesHardFeasibleSnapshot()` | No-option fixture | Phase 03 full evaluation | Stable candidate, all requests partitioned |
|  | `defectNeverReachesAcceptance()` | Corrupt insertion result | Call-order recorder | Typed failure, acceptance 0 |
|  | `doesNotReimplementPropagationOrComparator()` | Architecture/package inspection | Forbidden reference list | Duplicate evaluator implementation 0 |
| `AdaptiveOperatorSelectorTest` | `probabilitiesAreFinitePositiveAndSumToOne()` | Zero-use/mixed-use snapshots | Independent decimal/rational calculation | All conditions exact within declared representation |
|  | `zeroUseDenominatorNeverProducesNaNOrInfinity()` | All zero counts | Same | Finite positive vector |
|  | `sameSnapshotOutcomeAndConfigProduceSameNextFingerprint()` | Fixed outcome classes | Reference updater | Byte/fingerprint equality |
|  | `invalidAndInterruptedDoNotAdvanceLearning()` | Two non-completed outcomes | Pre-state bytes | Exact unchanged |
|  | `selectionIsIndependentOfRegistryInsertionOrder()` | Permuted registry source | Canonical registry oracle | Same operator and trace |
|  | `updatePeriodUsesCompletedStepsOnly()` | Reject/invalid/interrupted mix | Hand counter | Update only at expected completed boundary |
| `AcceptancePolicyTest` | `hillClimbingRequiresExplicitEqualPolicy()` | Missing/equal config | Config validator | Missing rejected; explicit decision exact |
|  | `saRejectsMissingEnergyTemperatureCoolingOrScale()` | One-field omissions | Config validator | Every omission bind failure |
|  | `saUsesApprovedEnergyProjectionNotLexicographicBigM()` | Conflicting vector/scalar fixture | Projection declaration | Only declared projection used |
|  | `saUsesAcceptancePurposeStreamOnly()` | Draw-count perturbation | Namespace oracle | Other streams unchanged |
|  | `temperatureAdvancesOnceAfterCompletedRejectedStep()` | SA reject fixture | Hand cooling table | Exact next state |
|  | `temperatureDoesNotAdvanceAfterInvalidOrInterruptedStep()` | Invalid/interrupted fixtures | Pre-state bytes | Exact unchanged |
|  | `sameStateCandidateAndDrawProduceSameDecision()` | Fixed draw boundary | Hand probability relation | Same decision/fingerprint |
| `SeedStreamOwnershipTest` | `derivesDistinctSeedsForEveryPurposeNamespace()` | Replay fixture | Independent seed reference | No unintended collision in fixture; exact reference values |
|  | `retryAttemptIdDoesNotChangeLogicalStreams()` | Two attempt IDs | Independent reference | All derived seeds equal |
|  | `unrelatedDestroyDrawsDoNotShiftAcceptanceStream()` | Variable destroy draw count | Purpose seed/draw oracle | Acceptance seed/draw equal |
|  | `unusedAcceptanceDrawDoesNotShiftNextStepOperators()` | Better vs worse prior candidate | Step namespace oracle | Next step operator streams equal |
|  | `threadAndCompletionOrderDoNotEnterDerivation()` | Executor permutations | Independent reference | Same derived seeds |
|  | `detectsSeedDerivationVersionMismatch()` | Corrupted manifest | Replay classifier | `NOT_COMPARABLE` |
| `Phase1ScreenerTest` | `screensEveryAvailablePortfolioCandidateToExactStepBudget()` | 7 available + CLOCK unavailable | Per-run work counters | Each available exact steps, unavailable not run |
|  | `requiresNormalCompletionForEveryAvailableScreen()` | One watchdog screen | Completion oracle | No champion, typed incomplete |
|  | `selectsChampionIndependentOfCompletionOrder()` | Delayed fake runs | Stable reducer | Same champion/trace in all schedules |
|  | `usesPolicyOrderThenFingerprintForEqualObjectiveTie()` | Equal objective candidates | Hand total order | Exact champion |
|  | `doesNotTreatClockUnavailableAsFailedCandidate()` | Missing-coordinate Phase 05 manifest | Manifest oracle | Slot remains unavailable |
| `TerminationFaultTest` | `returnsMaxStepsOnlyAtExactCompletedCount()` | Requested 3, mixed rejects | Work counter | requested=completed=3 |
|  | `watchdogDuringEveryStepBoundaryDiscardsDraft()` | Manual probe parameterized | Pre-state bytes | `WATCHDOG_REACHED`, unchanged incomplete step |
|  | `cancelDuringEveryStepBoundaryDiscardsDraft()` | Manual probe parameterized | Pre-state bytes | `CANCELLED`, unchanged incomplete step |
|  | `resourceSignalIsNotRenamedWatchdogOrMaxSteps()` | Resource probe | Enum oracle | Exact termination |
|  | `platformTimeoutIsNotAlgorithmCompletion()` | Platform signal fake | Enum oracle | `PLATFORM_TIMEOUT`, no synthetic record |
|  | `operatorExceptionReturnsFailedAndPreservesLastCommittedBest()` | Throwing operator | Pre-state bytes | `FAILED`, draft discarded, best exact |
|  | `malformedProposalFailsClosedWithoutInternalRetryLoop()` | Duplicate proposal | Call counter | One failure path, no hidden retry |
| `ReplayDeterminismTest` | `matchesIndependentThreeStepReferenceRowByRow()` | §10.2 builders | `IndependentReplayOracle` | Every row and final fingerprint exact |
|  | `sameEnvelopeProducesSameTraceAndCandidateOnRepeatedRuns()` | Same manifest repeated | Independent canonicalizer | Exact bytes/digests |
|  | `retryAttemptIdDoesNotChangeCanonicalTraceOrCandidate()` | Same logical run with two platform attempt IDs | Independent canonicalizer | Canonical trace/candidate exact equality; observation metadata만 다름 |
|  | `sameEnvelopeProducesSameTraceUnderParallelScreenSchedules()` | Schedule permutations | Same | Exact trace/champion |
|  | `cacheHitMissPatternDoesNotChangeReplay()` | Poison-free cache toggle | Phase 03 full oracle | Same trace/candidate |
|  | `unorderedSourceCollectionsDoNotChangeReplay()` | Permuted map/set creation | Stable reference | Same trace/candidate |
| `ReplayCorruptionTest` | `detectsChangedWarmStartFingerprint()` | One-field manifest corruption | Independent classifier | `NOT_COMPARABLE` |
|  | `detectsChangedOperatorVersionOrTiePolicy()` | One-field corruption | Independent classifier | `NOT_COMPARABLE` |
|  | `detectsDerivedSeedOrDrawDigestCorruption()` | One trace field changed | Independent canonicalizer | `REPLAY_MISMATCH` |
|  | `detectsStepEventReorderOmissionAndDuplication()` | Three corrupt traces | Independent canonicalizer | Each rejected |
|  | `detectsSameTraceDifferentCandidateFingerprint()` | Candidate corruption | Full-copy reference | `REPLAY_MISMATCH` |
|  | `detectsSameCandidateDifferentTraceDigest()` | Trace corruption | Independent canonicalizer | `REPLAY_MISMATCH` |
| `AlnsWorkerRunIT` | `runsPhase05WarmStartThroughPhase03EvaluationWithoutDuplicateEvaluator()` | Cross-module fixture | Call graph + full-copy oracle | Exact trace/candidate, one evaluator authority |
|  | `handsOffCandidateEvidenceAndReplayManifestOnly()` | Completed worker | Artifact schema oracle | No verifier/final outcome fields |
|  | `exceptionalRunPreservesExactTerminationAndLastSafePoint()` | Cancel/watchdog/operator fault | Pre-state/evidence oracle | No normal completion overclaim |
| `SolverTraceRedactionTest` | `traceContainsIdsDigestsAndVersionsButNoRawAddressInputOrSecret()` | Sensitive-string fixture | Denylist + schema allowlist | Sensitive values 0, required IDs present |
|  | `traceUsesSafeInternalIdsAndExcludesRawExternalIdentifiers()` | Raw external request/vehicle/customer ID fixture | Schema allowlist + mapping probe | Canonical trace에는 dense/safe identity만 존재 |
|  | `solverModuleHasNoTenantAuthorizationOrProviderCredentialLogic()` | Architecture inspection | Forbidden package/type list | References 0 |
| `CowCopyWorkAccountingTest` | `copiesExactlyDistinctMutatedRoutesAcrossRepeatedWrites()` | Repeated writes to two routes | Instrumented copy counter | Copy count exactly 2 |
|  | `recordsCheapExactFeasibleAndCompletedWorkSeparately()` | Repair work fixture | Hand counter table | Every counter exact |
|  | `doesNotUseWallClockAsQualityOrTieInput()` | Varying manual elapsed | Trace/candidate oracle | Quality trace/candidate unchanged |
| `Phase06SolverArchitectureTest` | `solverDependsOnlyOnCoreAmongSemanticModules()` | Compiled graph | Maven/bytecode rule | Forbidden module edge 0 |
|  | `solverHasNoCloudHttpCustomerVerifierOrVendorReferences()` | Source + bytecode | Denylist | Reference 0 |
|  | `searchDoesNotImplementPropagationTravelOrFinalization()` | Package ownership rule | Allowed owner list | Duplicate semantic owner 0 |
|  | `productionHasNoStaticGlobalOrThreadLocalRandom()` | Source/bytecode inspection | Random owner rule | Violations 0 |

Canonical solver trace의 identifier는 dense/internal safe ID, logical run ordinal, version과 digest로 제한한다. Raw external request/vehicle/customer ID, 주소/좌표, full input, provider locator, credential/secret와 arbitrary exception payload는 trace에 넣지 않는다. `solveId`/manifest/round/worker/run/termination, requested/completed work와 artifact digest 같은 correlation은 typed observation record에 둘 수 있지만 platform `AttemptId`, elapsed와 provider execution ID는 semantic trace/candidate fingerprint 밖에 둔다. Fingerprint는 correlation/integrity 수단이며 authorization이나 encryption을 대신하지 않는다.

### 11.2 Test 종류별 적용 판정

| Test 종류 | 적용 | Phase 06 판정 |
|---|---:|---|
| Unit/boundary | Yes | COW, outcome, adaptive, acceptance, seed와 termination의 exact methods |
| Module contract | Yes | Phase 03/04 evaluator/comparator와 Phase 05 `SearchSnapshot`/`PairInsertionEvaluator`/`SeedPortfolio` 소비 |
| Integration | Yes | `AlnsWorkerRunIT`; provider/application/Phase 07 verifier E2E는 future |
| Architecture | Yes | Module DAG, semantic owner, random/provider/customer/verifier/vendor leakage |
| Fault injection | Yes | Operator exception, malformed proposal, adaptive failure, cancel/watchdog/resource/platform |
| Corruption | Yes | Route/bank/cache/manifest/trace/seed/candidate fingerprint one-field mutation |
| Reproducibility | Yes | Independent oracle, repeated/parallel/cache/order permutations |
| Security | Limited but applicable | Solver trace data minimization과 dependency boundary; IAM/tenant/provider rehearsal은 Phase 11/14 |
| Performance | Work-accounting applicable | Copy/evaluation/work counters와 bounded config를 검증; absolute wall-clock SLA와 apply/undo profiling은 `RM-7`/Phase 14 |

### 11.3 Red → green 순서

1. **Red A — COW isolation:** `CowTrialIsolationTest` 전부 red인 상태에서 state primitive만 구현한다.
2. **Green A:** First-write copy, bank independence, discard와 top-level no-alias를 green으로 만든다.
3. **Red B — Pair pipeline:** Destroy/repair contract와 Phase 05 integration을 red로 추가한다.
4. **Green B:** Proposal-only destroy, 승인된 owner의 central pair edit와 exact insertion delegation만 구현한다.
5. **Red C — Step transaction:** Accept/reject/invalid/interrupted/fault state transition을 red로 만든다.
6. **Green C:** Next state를 local에서 완성한 뒤 한 번 publish하여 progress/best/learning/temperature를 원자적으로 전이한다.
7. **Red D — Adaptive/acceptance/RNG:** Operator probability, temperature와 purpose stream tests를 red로 만든다.
8. **Green D:** Explicit versioned config와 stateless namespaced derivation으로 green을 만든다.
9. **Red E — Screen/termination:** Phase-1 champion과 exact completed-step/fault tests를 red로 만든다.
10. **Green E:** Completion-order-independent reducer와 typed termination을 구현한다.
11. **Red F — Independent replay/corruption:** Production helper를 import하지 않는 reference oracle과 mismatch tests를 red로 만든다.
12. **Green F:** Row-by-row replay, canonical trace와 manifest identity를 green으로 만든다.
13. **Green G — Architecture/security/performance counters:** Root/module build, forbidden reference, redaction와 work accounting을 모두 green으로 만든다.

Green이 된 뒤 test를 지우거나 expected value를 production output으로 덮어써서 맞추지 않는다. Oracle 수정은 source decision/spec drift 근거와 independent review가 필요하다.

## 12. Ordered work packages

### WP-06.0 — Entry, authority와 config freeze

- **Prerequisite:** Scheduler task/owner 배정, Phase 03~05 accepted handoff bundle 제공.
- **Change target:** Phase 06 internal contract review, source/evidence manifest skeleton, explicit config schema.
- **Concrete tasks:** Upstream fingerprint equality, Phase 05 candidate inventory, operator/repair/improvement/adaptive/acceptance/termination config closure, proposed seed derivation/trace schema와 all numeric status를 review한다.
- **Verification commands:**

  ```bash
  test -s docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
  test -s docs/implementation/phases/phase-06-cow-alns-reproducibility.md
  test -s docs/implementation/phases/phase-07-independent-verification-final-result.md
  ```

- **Expected tests/evidence:** Actual Phase 05/07 links, accepted predecessor reviews/digests, omitted config rejection table와 task identity. 현재 final inspection에서는 두 상세 파일 command가 성공하지만 모두 unaccepted이므로 entry는 계속 BLOCKED다.
- **Failure/rollback:** Stub candidate/default numeric/placeholder evaluator로 진행하지 않는다. Last safe point는 이 detailed document와 independent fixture 설계다.
- **Handoff:** Frozen semantic contract와 exact upstream identities를 WP-06.1에 전달한다.

### WP-06.1 — COW state, identity와 discard isolation

- **Prerequisite:** WP-06.0 gate green, Phase 05 immutable `SearchSnapshot`/bank contract.
- **Change target:** `solver.state`, COW fixture, owner-graph/copy instrumentation.
- **Concrete tasks:** Distinct top-level refs, immutable route structural sharing, first-write copy, independent bank, derived-state invalidation, freeze/commit/discard, pre/post canonical bytes를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=CowTrialIsolationTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** §11.1의 7개 COW methods green; parent/best mutation 0; copy set exact.
- **Failure/rollback:** Mutable alias, whole-solution untracked copy, apply/undo 또는 cache authority가 필요하면 WP를 중단한다. Phase 05 immutable candidate digest가 last safe point다.
- **Handoff:** Audited `TrialDraft`/freeze/discard primitive를 WP-06.2에 전달한다.

### WP-06.2 — Destroy/repair와 bounded improvement composition

- **Prerequisite:** WP-06.1 green, actual Phase 05 `PairInsertionEvaluator`/`SeedPortfolio` integration fixture와 accepted handoff, 중앙 pair-removal editor의 Phase 05/06 owner/API/evidence review 승인.
- **Change target:** `solver.search.destroy`, `repair`, `improvement`; 승인된 owner의 central editor contract와 operator/repair contract tests.
- **Concrete tasks:** Ordered proposal-only family, 승인된 중앙 pair-removal editor 소비/구현, explicit repair scope/shortlist/NEW_ROUTE/Phase 05 exact evaluator delegation, explicit bounded improvement 또는 `NONE_V1`을 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=DestroyOperatorContractTest,RepairPipelineContractTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Family/legacy differential, duplicate proposal fault, cheap→exact, partial/no-feasible partition과 evaluator ownership methods green.
- **Failure/rollback:** Operator 직접 mutation, local feasibility/comparator 복제, hidden shortlist/removal budget가 발견되면 해당 operator registry를 제거하고 WP-06.1로 돌아간다.
- **Handoff:** Versioned operator/repair result와 exact call trace를 WP-06.3에 전달한다.

### WP-06.3 — Transactional completed-step와 best monotonicity

- **Prerequisite:** WP-06.2 green, Phase 03/04 full evaluation/comparator available.
- **Change target:** `AlnsStepExecutor`, step result/outcome, state transition tests.
- **Concrete tasks:** Full lifecycle, guard-before-acceptance, accept/reject/invalid/interrupted/fault classification, local next-state construction, one-time publication과 best monotonicity를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=AlnsStepStateMachineTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** §11.1의 11개 state-machine methods green; publication race의 before/after linearization exact, global-best improvement에서도 slot handle pairwise distinct, stage-guard failure에서 세 solution slot unchanged, invalid/interrupted/adaptive failure에서 entire state unchanged.
- **Failure/rollback:** Candidate commit 뒤 adaptive failure가 rollback 불가능하거나 mid-step current/best가 노출되면 설계를 중단한다. WP-06.2의 pure proposal/repair + WP-06.1 COW가 last safe point다.
- **Handoff:** Completed-step state transition을 WP-06.4에 전달한다.

### WP-06.4 — Adaptive selection, acceptance와 namespaced RNG

- **Prerequisite:** WP-06.3 green, config/seed derivation version review 승인.
- **Change target:** `adaptive`, `acceptance`, `random`; exact reference calculations.
- **Concrete tasks:** Ordered selection, finite-positive/sum-one state, completed-only update, HC/SA explicit config, purpose/operator substreams, retry/order independence와 lineage를 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=AdaptiveOperatorSelectorTest,AcceptancePolicyTest,SeedStreamOwnershipTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** NaN/Infinity/zero-active 0, invalid/interrupted non-advance, acceptance stream isolation, independent seed reference equality.
- **Failure/rollback:** Global RNG, registry/classpath order, wall clock, omitted acceptance/default temperature가 하나라도 result에 들어가면 WP 코드를 제거하고 deterministic explicit single-operator/HC test contract로 돌아가 review한다.
- **Handoff:** Immutable learning/acceptance/RNG lineage를 WP-06.5에 전달한다.

### WP-06.5 — Phase-1 screen과 exact termination/fault

- **Prerequisite:** WP-06.4 green, Phase 05 portfolio stable order.
- **Change target:** `Phase1Screener`, `StableChampionReducer`, `termination`; manual probes와 integration test.
- **Concrete tasks:** 각 available slot의 exact screen, all-screen completeness, stable reduction, requested/completed work, cancel/watchdog/resource/platform/operator fault와 last safe point를 구현한다.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=Phase1ScreenerTest,TerminationFaultTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dit.test=AlnsWorkerRunIT \
    -Dfailsafe.failIfNoSpecifiedTests=true clean verify
  ```

- **Expected:** Completion schedule permutations 모두 같은 champion; exact completed count; every lifecycle boundary fault에서 draft discard와 exact termination.
- **Failure/rollback:** 일부 성공 screen champion, real sleep/flaky timeout, `PLATFORM_TIMEOUT→MAX_STEPS` 변환이 있으면 screener/termination layer만 제거하고 WP-06.4 single-step engine을 보존한다.
- **Handoff:** Phase-1 champion/worker candidate/termination record를 WP-06.6에 전달한다.

### WP-06.6 — Independent replay, corruption와 parallel determinism

- **Prerequisite:** WP-06.5 green, trace/manifest canonical schema review.
- **Change target:** `solver.replay`, independent test oracle, replay/corruption tests.
- **Concrete tasks:** Strong envelope, canonical event/digest, separate full-copy reference, separate seed/canonicalizer implementation, repeated/parallel/cache/order permutation과 mismatch classification을 구현한다.
- **Verification command:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=ReplayDeterminismTest,ReplayCorruptionTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **Expected:** Three-step fixture row-by-row exact equality; schedule/cache/source-order permutation exact equality; 모든 one-field corruption 거부.
- **Failure/rollback:** Oracle가 production helper를 import하거나 expected를 production output으로 생성하면 `E-P06-REPLAY` 전체를 폐기한다. WP-06.5 trace 미승인 상태가 last safe point다.
- **Handoff:** `ReplayManifest`, independent report와 candidate fingerprint를 WP-06.7에 전달한다.

### WP-06.7 — Architecture, security, performance counters, bundle와 review

- **Prerequisite:** WP-06.1~6 green, required test skipped 0.
- **Change target:** Architecture rule, trace redaction, work accounting, evidence bundle와 Phase 07/10 handoff.
- **Concrete tasks:** Forbidden dependency/random/customer/provider/verifier/vendor 검사, copy/work counters, sensitive data allowlist, exact command/toolchain/test count/digests, known limitation/blocker/rollback과 independent review input을 만든다.
- **Verification commands:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver \
    -Dtest=SolverTraceRedactionTest,CowCopyWorkAccountingTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/architecture-rules -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/solver -am clean verify

  ./mvnw -B -ntp -Dstyle.color=never clean verify
  ```

- **Expected:** OR-Tools-free ALNS-only root green, failed/error/skipped required 0, forbidden reference 0, no sensitive raw value, copy/work exact, immutable evidence bundle와 independent review `PASS`.
- **Failure/rollback:** Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE`; `ACCEPTED`, verified 또는 downstream authority를 주장하지 않는다. Last accepted predecessor와 last green WP artifact를 보존한다.
- **Handoff:** §16에 명시한 candidate/evidence/replay manifest만 Phase 07/10에 전달한다.

## 13. Verification commands, evidence와 pass criteria

### 13.1 Layer별 future command

| Layer | Command | Pass criteria |
|---|---|---|
| COW/state | WP-06.1 selected test | Exact methods green, alias/parent mutation 0 |
| Operator/repair | WP-06.2 selected test | Proposal-only/central edit/exact evaluator ownership과 all outcome partition green |
| Step transaction | WP-06.3 selected test | Single publication, non-advance와 best monotonicity green |
| Adaptive/acceptance/RNG | WP-06.4 selected test | Finite-positive/sum-one, completed-only state, independent seed equality |
| Screen/termination | WP-06.5 unit + Failsafe | All candidate completeness, exact work, typed fault, handoff schema |
| Replay/corruption | WP-06.6 selected test | Row-by-row oracle, repeated/parallel exact equality, corruptions rejected |
| Security/performance | WP-06.7 selected test | Allowlisted trace only, copy/work counters exact, no wall-clock quality input |
| Architecture | WP-06.7 architecture command | Forbidden dependency/reference/owner/global random 0 |
| Module | `./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/solver -am clean verify` | Solver + required upstream tests/package green |
| Reactor | `./mvnw -B -ntp -Dstyle.color=never clean verify` | OR-Tools-free ALNS-only full reactor green; unrelated required modules not skipped |

Selected solver command는 `-Dsurefire.failIfNoSpecifiedTests=true` 또는 `-Dfailsafe.failIfNoSpecifiedTests=true`를 사용하고 `-am`을 제거한다. Upstream nonmatching test 때문에 fail-closed 검사를 끄지 않으며, accepted predecessor artifact를 해소하지 못하면 command 자체가 실패하는 것이 맞다. Architecture는 selected name을 느슨하게 적용하지 않고 module 전체 `clean verify`를 실행한다. 각 command는 해당 report directory를 clean한 뒤 §11.1의 exact class/method를 fresh Surefire/Failsafe XML manifest와 대조한다. Missing/duplicate report나 method, failed/error/skipped required test는 Maven exit code가 0이어도 evidence failure다.

`-DskipTests`, `-Dmaven.test.skip=true`, `failIfNoSpecifiedTests=false`, required test disable, stale `target/` report, console summary 한 줄, flaky test 재실행 성공만 선택, 서로 다른 manifest 결과 혼합은 exit evidence가 아니다.

### 13.2 Planned evidence

| Key | 반드시 포함할 내용 | 현재 상태 |
|---|---|---|
| `E-P06-COW` | Upstream/state/config identities, route-copy/bank/alias audit, accept/reject/fault/cancel pre/post canonical fingerprints, exact command/toolchain/exit code와 fresh required-method report manifest | NOT_PRODUCED |
| `E-P06-ALNS` | Operator/repair/improvement/adaptive/acceptance versions, exact requested/completed work, phase-1 screen/champion trace, termination/fault matrix, cache-free equality와 fresh required-method report manifest | NOT_PRODUCED |
| `E-P06-REPLAY` | Strong envelope, base/derived seed lineage, independent oracle implementation/digest, row-by-row trace, repeated/parallel/cache/order/attempt-ID-separation results, corruption rejection과 fresh required-method report manifest | NOT_PRODUCED |

모든 bundle은 content-addressed immutable artifact 또는 digest-protected local equivalent여야 한다. Reviewer/verdict, test count와 evidence digest를 구현자가 임의 합성하지 않는다.

## 14. Exit gate, Definition of Done과 anti-pattern

### 14.1 Exit gate

다음 AND 조건을 모두 만족해야 independent reviewer가 Phase 06 `ACCEPTED`를 권고할 수 있다.

- Phase 03~05 accepted authority/evidence와 exact input fingerprints가 확인됨.
- Central pair-removal editor와 solution-evaluation/comparator seam의 cross-phase owner/API/evidence 계약이 승인되어 중복 구현·공백이 없음.
- Required numeric/operator/acceptance/termination/seed/tie config가 explicit하고 omitted value binding이 실패함.
- Changed-route first-write copy, independent bank, distinct current/stageBest/solveBest와 reject/fault/cancel parent preservation이 검증됨.
- Destroy가 proposal-only이고 승인된 owner의 central pair editor가 pair 전체를 이동하며 repair가 Phase 05 exact evaluator를 재사용함.
- Complete/partial/no-feasible repair가 route/bank exact partition을 지키고 `DEFECT`는 acceptance에 도달하지 않음.
- Completed-step 전체 lifecycle, `REJECTED` completed 처리와 invalid/interrupted non-advance가 exact함.
- Named interruption boundary와 final pre-publication probe가 single publication linearization을 보존하고, publish 이후 signal이 완료 step을 소급 취소하지 않음.
- Hard/stage guard가 acceptance/temperature보다 먼저 실행되고 stage-guard failure가 current/stageBest/solveBest를 바꾸지 않으며 best가 cache-free comparator 기준 악화되지 않음.
- Adaptive probability가 finite·positive·sum-one이고 registry insertion/classpath order에 독립적임.
- Acceptance/temperature가 explicit policy이며 completed step에만 전진함.
- Purpose/operator namespaced seed가 independent reference와 일치하고 retry/thread/completion order에 독립적임.
- 모든 available Phase-1 screen이 exact test/experiment steps로 정상 완료되고 stable champion이 completion order와 무관함.
- Normal/exceptional termination과 requested/completed step가 정확하며 모든 fault boundary에서 draft가 폐기됨.
- Independent full-copy replay oracle가 step별 trace와 최종 candidate에 일치하고 corruption/mismatch를 거부함.
- Cache hit/miss, unordered source construction, parallel schedule과 platform `AttemptId`가 canonical trace/candidate fingerprint를 바꾸지 않음.
- Solver의 cloud/customer/verifier/application/vendor dependency와 global/static/thread-local random reference가 0임.
- Trace에 raw address/input/secret이 없고 COW copy/work counters가 exact함.
- OR-Tools-free ALNS-only module/root build, immutable `E-P06-*` bundle과 independent Phase 06 review가 통과함.
- Phase 07에 candidate/evidence/replay manifest만 전달하고 verifier/final result/publication을 당기지 않음.
- OPEN/GATED/deferred/official 미확정 값을 default나 완료 상태로 채우지 않음.

### 14.2 Definition of Done

Phase 06 `ACCEPTED`는 다음을 뜻한다.

1. Phase 05 stable candidate에서 parent/best를 오염시키지 않고 COW ALNS step을 반복할 수 있다.
2. Request-pair operator, exact evaluator, acceptance와 learning 책임이 분리되고 upstream evaluator를 재사용한다.
3. Exact completed-step과 fault/termination 의미가 trace와 candidate lineage에 보존된다.
4. Phase-1 champion과 worker candidate가 same envelope에서 decision trace까지 재현된다.
5. Independent reference가 COW/RNG/trace를 별도 계산해 생산 구현의 replay claim을 검증한다.
6. 공식 미확정 수치 없이도 explicit test-only/experiment config로 논리 계약을 검증하며 official 값을 주장하지 않는다.
7. Phase 07과 Phase 10이 의미를 재구현하지 않고 candidate/evidence/replay artifact를 소비할 수 있다.
8. Evidence와 review가 immutable identity로 고정되고 rollback point가 있다.

### 14.3 금지 anti-pattern

- `current`, `stageBest`, `solveBest` 또는 parent route/bank/cache 직접 mutation
- Changed route first write 전 mutable list/map 공유
- Apply/undo skeleton, automatic COW threshold 또는 성능 근거 없는 state switch
- Destroy operator가 route를 수정하고 별도 ID list를 반환
- Pickup/delivery node를 독립 removal/insertion 단위로 사용
- Phase 03 propagation/evaluation/comparator 또는 Phase 05 insertion evaluator 복제
- Cheap shortlist score를 feasibility/objective/final diagnostic으로 사용
- Missing `NEW_ROUTE`, concrete vehicle 대신 type/count sentinel
- Partial/no-feasible repair를 defect로 오인하거나 bank를 final unassignment로 직렬화
- Hard violation을 SA/penalty/Big-M/scalar surrogate로 acceptance
- Raw objective/double placeholder를 bound comparator 대신 사용
- Hidden operator list/removal ratio/top-N/reward/temperature/cooling/step/watchdog default
- Legacy preset 수치를 general production default로 사용
- Invalid/interrupted를 `REJECTED`로 바꿔 reward/cooling/completed step을 전진
- Rejected completed step을 미완료로 처리해 exact budget을 왜곡
- Candidate commit 뒤 adaptive failure를 부분 rollback
- Global `Random`, static RNG, thread-local RNG 또는 unordered collection iteration
- Seed derivation에 attempt/thread/time/elapsed/completion order 포함
- Completion-first Phase-1 champion 또는 equal objective에서 random/clock tie-break
- Wall-clock 1분을 phase-1 quality termination으로 사용
- Watchdog/cancel/resource/platform failure를 `MAX_STEPS_REACHED`로 변환
- 같은 strong replay identity의 다른 digest 중 더 좋은 것을 선택
- Production implementation을 호출해 expected replay output 생성
- Trace에 raw input/address/PII/secret 값 저장
- Phase 07 verifier `PASS`, final outcomes/result/publication eligibility를 Phase 06에서 생성
- Gated route pool/MIP/vendor/backend type을 선반영

## 15. Blocker, OPEN/GATED/deferred와 restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00/03/04/05 accepted evidence 부재 | BLOCKER | Architecture + Core/Evaluation/Profile + Pair/Portfolio | Phase 06 code/test/evidence 착수 | 이 detailed document와 independent fixture 설계 | Accepted review/bundle/digest와 actual artifacts |
| Central pair-removal editor owner conflict | RESIDUAL CROSS-PHASE BLOCKER | Phase 05/06 Pair/Algorithm + Architecture reviewers | WP-06.2 implementation owner, package/API와 `E-P05-PAIR`/`E-P06-*` evidence attribution | Ordered proposal + central pair edit semantics; 어느 Phase도 중복 구현하지 않음 | Master Realization/Integrated Phase 05와 actual Phase 05/06 사이 owner/API/evidence를 공동 승인 |
| Solution evaluation과 comparator/tie seam | RESIDUAL UPSTREAM CONTRACT BLOCKER | Phase 03 Core/Evaluation + Phase 04 Profile + Phase 05/06 Algorithm | Candidate full evaluation, guard/current/best compare와 replay identity | Route kernel + ordered business objective first; ad hoc aggregation/Big-M/context tie reversal 금지 | Phase 03 review `F-P03-004/006`와 Phase 05 review `F-P05-001/005`의 solution API, equality/context tie owner/order/fingerprint와 cross-phase tests 승인 |
| Phase 05/07 actual detailed contracts unaccepted | BLOCKER FOR CONTRACT FREEZE | Phase 05/07 owner + scheduler | Exact adjacent signature와 accepted handoff contract | Actual Phase 05 §7/§15.2 + Phase 07 §7.3/§15.1 대조 | Phase 05/06/07 cross-phase review와 compatible immutable projection 승인 |
| Scheduler task/owner 미지정 | BLOCKER | 총괄 scheduler | Authoritative status와 review assignment | `TBD_NOT_SUPPLIED` | Exact task ID, implementer/reviewer 배정 |
| Proposed Java API/type/trace schema | PROPOSED/OPEN | Solver + Architecture + Phase 05/07 reviewers | Implementation/public compatibility freeze | §7 semantics | Cross-phase review/ADR와 version approval |
| Seed derivation/canonical encoding version | PROPOSED/OPEN | Algorithm + Reproducibility + Architecture | External replay compatibility | Purpose/context contract, test-only oracle | Exact version/spec, independent oracle와 review |
| Invalid-candidate continuation policy | PROPOSED SAFE BASELINE | Algorithm + Quality | Continue-on-invalid behavior | Fail-closed, no hidden retry | Explicit bounded policy/evidence와 approval |
| `Q-BENCH-02` official screen/worker/round/watchdog | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 14 official manifest/baseline/cutover | Explicit test-only/experiment config only | Calibration corpus/protocol, measured review, explicit approval |
| Additional attempt/resource numeric limits | OPEN/PROPOSED | Algorithm + Quality + Operations | 해당 safety policy의 official use | Fail-closed + external typed resource signal | Explicit config, measurement와 approval |
| Current Win decimal `D/U` | BLOCKER FOR OFFICIAL USE | Input·Matrix + Benchmark | 해당 fixture official run | Test-only compliant integer fixture | Compliant integer matrix 또는 contract/migration approval |
| `C-17` route pool/MIP | GATED TARGET | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13와 production default | Phase 06 ALNS-only candidate/replay | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope, OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | Optional variant 질문/구현 | Current pair/fixed-terminal contract | Representative fixture, feasibility와 별도 승인 |
| Multi-trip/rotation | DEFERRED FEATURE | Product·Domain·Algorithm | Trip/reset/depot resource 의미 | Oneway + single roundtrip | Exact contract/example/verifier impact와 승인 |
| Apply/undo | GATED BY MEASUREMENT/APPROVAL | Algorithm + Performance + Review | State strategy 변경 | `CHANGED_ROUTE_COW_V1` | `RM-7` profiling, round-trip/fault/trace/verifier equality와 별도 승인 |

`Q-INFRA-01`은 `RESOLVED`지만 Phase 06 solver에 AWS dependency를 추가하는 근거가 아니다. AWS S3/Step Functions/Lambda target은 Phase 11 distribution 책임이며 strong replay의 semantic owner가 아니다.

## 16. Previous/next handoff

### 16.1 Previous — actual but unaccepted Phase 05

[actual: Phase 05 — Pickup-delivery pair, 삽입과 초기 후보군](phase-05-pair-insertion-initial-portfolio.md)에서 exact `Phase05SeedPortfolioHandoff`를 받아야 한다.

- Source commit와 Phase 05 detailed/review/evidence digests
- Problem/PreparedTravel/BoundProfile/SolvePlan과 portfolio/enumeration/tie contract fingerprints
- Ordered available `ConstructionCandidate` refs와 각 immutable `SearchSnapshot`/`SolutionFingerprint`
- Complete pair/same-vehicle/precedence/route-bank XOR과 cache-free validation report refs
- 각 member policy/trace/fingerprint와 unavailable member records
- `E-P05-PAIR`, `E-P05-INSERTION`, `E-P05-PORTFOLIO` refs와 rollback point
- Pure `PairInsertionEvaluator`/`FeasibleInsertionOption` contract; Phase 06 repair의 feasibility authority

Phase 05는 phase-1 champion, screen 수치, destroy/repair proposal, `TrialDraft/current/stageBest/solveBest`, acceptance/adaptive/RNG/termination, verifier/final outcome을 넘기지 않는다. Phase 06은 candidate를 직접 mutate하지 않고 자기 COW state로 가져오며, screen/RNG/operator/acceptance/adaptive/step config를 별도 manifest로 제공한다. Actual Phase 05 v1.3 document review는 `CHANGES_REQUIRED`이고 implementation/evidence는 `NOT_STARTED`/`NOT_PRODUCED`이므로 implementation entry는 여전히 차단된다.

### 16.2 Next — actual but unaccepted Phase 07

[actual: Phase 07 — 독립 검증과 최종 결과](phase-07-independent-verification-final-result.md)에 다음 **세 부류만** 전달한다.

1. **Candidate**
   - Handoff schema/contract version
   - Immutable route order와 bank membership의 canonical payload/ref
   - Problem/travel/profile/evaluation/SolvePlan fingerprints와 authority artifact content digests
   - Route/solution metric·score·objective와 solution fingerprint의 **비권위 declared claims**; Phase 07이 cache-free 재계산 뒤 exact 대조
   - Canonical routes + bank + declared claims를 덮는 candidate content digest/fingerprint
   - Exact termination과 last committed boundary
2. **Evidence**
   - `E-P06-COW`, `E-P06-ALNS` references/digests
   - Requested/completed step, operator/config/build/runtime와 fault/rollback lineage
   - 승인된 경우에만 bounded search-observation record; final reason/confidence/outcome은 포함하지 않음
   - Scheduler가 연결한 accepted independent Phase 06 review receipt/reference. Review는 evidence digest를 검증할 수 있지만 Phase 상세/evidence manifest가 review whole-file hash를 역참조하지 않아 reciprocal digest를 만들지 않음
3. **Replay manifest**
   - Strong envelope, seed derivation/actual seeds, stable order/tie versions
   - Authority fingerprints/content digests, warm-start, trace와 candidate fingerprints
   - `E-P06-REPLAY` independent oracle report

다음은 전달하거나 생성하지 않는다.

- Candidate verifier `PASS`/`FAIL` 또는 `VerifiedSolution`
- Search cache를 authority로 한 feasibility/metric/objective summary
- Final `ASSIGNED/UNASSIGNED`, audit, diagnostic, confidence와 summary
- Result verifier report, `PublishableResult`, publication eligibility
- Exceptional last best를 정상 `MAX_STEPS_REACHED`로 바꾼 상태

Phase 07은 `rpdptw-solver`를 compile-depend하지 않고 core authority에서 candidate를 cache-free 재계산해야 한다. Actual Phase 07 §7.3/§15.1의 authority/content equality, requested/completed work, seed lineage, termination, trace와 evidence receipt를 이 handoff가 모두 제공함을 대조했다. Phase 07 document workflow가 review 완료로 갱신됐더라도 implementation/evidence는 `NOT_STARTED`/`NOT_PRODUCED`이고 phase handoff는 `NOT_READY`이므로 accepted 상태가 아니다.

### 16.3 Other downstream consumers

| Consumer | 소비할 것 | 소비하면 안 되는 것 | Handoff verification |
|---|---|---|---|
| Phase 10 Coordinator | Worker identity, warm start, requested/completed steps, exact termination, candidate/replay/evidence | Operator internals, completion-first winner, partial worker champion | Retry seed/config identity, all-worker completeness와 stable reducer future tests |
| Phase 14A ALNS benchmark | Accepted ALNS candidate/trace/replay와 measurement schema | Test-only 값을 official threshold로 승격, MIP 비교를 correctness oracle로 사용 | Phase 07/08 accepted + approved benchmark protocol |
| Phase 13 Optional hybrid — GATED | Accepted Phase 06 COW baseline, ALNS candidate/replay, explicit state lifecycle seam | Vendor API, raw selector incumbent, apply/undo | Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` + `C-17` 별도 승인 |
| `RM-7` COW profiling | Copy/allocation/GC/work counters와 representative accepted path | Automatic switch threshold | Measured bottleneck attribution; no-switch is valid result |

## 17. Source → requirement → test → evidence traceability

| Requirement | Source | Phase 06 contract | Exact test | Planned evidence |
|---|---|---|---|---|
| `REQ-COW-ALNS` changed-route COW/independent bank | [Master §12](../../master-design.md#12-candidate-state-cache와-rollback), `Q-ALG-02` | §7.3/§8 first-write copy, discard, no alias | `CowTrialIsolationTest.*` | `E-P06-COW` |
| `REQ-PAIR` request-level destroy/repair | [Master §11.4~§11.6](../../master-design.md#114-alns-step), [Final Domain §11](../../deprecated/2026-07-26-domain-design.md#11-alns-state와-operator) | Proposal-only destroy, cross-phase-approved central pair edit, exact repair delegation | `DestroyOperatorContractTest.*`, `RepairPipelineContractTest.*` | `E-P06-ALNS` |
| `REQ-EVAL` no duplicated evaluator/comparator | [Master §9](../../master-design.md#9-extensible-policy-evaluation과-profile-architecture), Phase 03 actual | Phase 03/04 injected authority, guard-before-acceptance | State/repair/integration + architecture methods | `E-P06-ALNS` |
| `REQ-STEP` completed-step lifecycle | [Master §11.4](../../master-design.md#114-alns-step), [Final Architecture §3.4](../../deprecated/2026-07-26-architecture-design.md#34-alns-step-budget-계약) | Single publication, rejected completed, invalid/interrupted non-advance | `AlnsStepStateMachineTest.*`, `TerminationFaultTest.*` | `E-P06-ALNS` |
| `REQ-ADAPTIVE` outcome/reward/probability | [Master §11.5](../../master-design.md#115-destroy-repair와-adaptive-selection) | Ordered finite-positive/sum-one immutable update | `AdaptiveOperatorSelectorTest.*` | `E-P06-ALNS` |
| `REQ-ACCEPTANCE` guard/explicit SA or policy | [Master §11.4~§11.5](../../master-design.md#114-alns-step) | Explicit policy/config, completed-only temperature, no Big-M | `AcceptancePolicyTest.*` | `E-P06-ALNS` |
| `REQ-PORTFOLIO` phase-1 4×2 screen | `Q-ALG-01`, [Master §11.2~§11.3](../../master-design.md#112-현재-범위의-initial-solution-portfolio) | Every available candidate exact screen + stable champion | `Phase1ScreenerTest.*` | `E-P06-ALNS`, `E-P06-REPLAY` |
| `REQ-TERMINATION` normal/exception separation | [Master §13.1](../../master-design.md#131-종료-의미), Final Domain §16 | Exact count, typed cancel/watchdog/resource/platform/failure | `TerminationFaultTest.*` | `E-P06-ALNS` |
| `REQ-REPRO` seeded strong replay | [Master §13.2~§13.3](../../master-design.md#132-strong-reproducibility-envelope), Final Domain §17.9 | Namespaced stateless streams, stable order, platform `AttemptId` 밖의 canonical manifest/trace | `SeedStreamOwnershipTest.*`, `ReplayDeterminismTest.*` | `E-P06-REPLAY` |
| `REQ-REPLAY-INDEPENDENCE` independent oracle/corruption | Master §15.6/§16.1, realization plan §8 | Full-copy/separate seed/canonicalizer reference, one-field mismatch | `ReplayDeterminismTest.matchesIndependentThreeStepReferenceRowByRow()`, `ReplayCorruptionTest.*` | `E-P06-REPLAY` |
| `REQ-NUMERIC-GATE` no hidden official value | `Q-BENCH-02`, realization plan §14 | Explicit test/experiment/legacy status; omission bind failure | Config omission methods across acceptance/operator/screen tests | All evidence manifests |
| `REQ-ARCH-DAG` solver→core only | [Final Architecture §2](../../deprecated/2026-07-26-architecture-design.md#2-module과-package-경계) | §7.6 dependency/owner/random rules | `Phase06SolverArchitectureTest.*` | All keys + architecture report |
| `REQ-SECURITY` trace minimization | Integrated §20, realization plan §13 | Safe internal IDs/digests/version only, raw external ID/input/PII/secret absent; observation metadata 분리 | `SolverTraceRedactionTest.*` | `E-P06-REPLAY` + security report |
| `REQ-PERFORMANCE` evidence before apply/undo | `Q-ALG-02`, Master §12.3/§15.9 | Exact copy/work counters, no wall-clock correctness | `CowCopyWorkAccountingTest.*` | `E-P06-COW`, `E-P06-ALNS` |
| `REQ-HANDOFF` verifier responsibility not pulled forward | Master §14.1/§15.7, realization Phase 07 | Candidate/evidence/replay only | `AlnsWorkerRunIT.handsOffCandidateEvidenceAndReplayManifestOnly()` | Phase 06 review handoff record |

새 algorithm family, state policy, random source, acceptance projection, numeric default, termination 또는 public artifact field가 필요하면 source/owner/test/evidence와 approval을 이 표에 연결한다. Phase 06 구현 편의를 위해 `OPEN — EXPERIMENT_REQUIRED`, `GATED` 또는 `DEFERRED`를 임의 값이나 완료 상태로 채우지 않는다.
