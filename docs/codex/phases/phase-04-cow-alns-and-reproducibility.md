# AR-4 / RM-4 — COW ALNS와 exact reproducibility 구현 계획

```yaml
phase: AR-4
rm_mapping: RM-4
status: BLOCKED
document_status: REVIEW_DRAFT
implementation_entry: BLOCKED_UNTIL_AR_0_THROUGH_AR_3_DONE
language: ko
repository: /Users/brown/workspace/ro-next
repository_baseline:
  branch: codex/domain-design
  head: 523c23e2e13410885b16e974efe40ffe598106ee
  inspected_at: 2026-07-24
source_baseline:
  implementation_plan:
    version: null
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  master_design:
    version: 3.2-review
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  architecture_design:
    version: 1.1-review
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  domain_design:
    version: 2.2-review
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
  question_register:
    version: 2.1-review
    status: REVIEW
    sha256: 3d6bc496b8df98a10534338828dd7e845b50ea967afa884642403405e613c088
prerequisite_documents:
  - path: docs/codex/implementation-plan.md
    required_sections: ["§5", "§8", "§9.5", "§10", "§11"]
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    phase: AR-0
    required_status: DONE
    validation_observation: PRESENT_NOT_STARTED
  - path: docs/codex/phases/phase-01-input-domain-and-travel.md
    phase: AR-1
    required_status: DONE
    validation_observation: PRESENT_BLOCKED
  - path: docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
    phase: AR-2
    required_status: DONE
    validation_observation: PRESENT_BLOCKED
  - path: docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
    phase: AR-3
    required_status: DONE
    validation_observation: PRESENT_BLOCKED
entry_gate: AR-0, AR-1, AR-2, AR-3 DONE evidence
owner_scope: rpdptw-solver의 COW state, ALNS worker, phase-1 screen, complete phase-2 batch, termination과 reproducibility
shared_build_baseline:
  source: phase-00 isolated execution observation supplied by the management session
  java: 25.0.3-amzn
  maven: 3.9.14
  legacy_test: PASS
  legacy_verify: PASS
  concurrent_target_failures_are_authoritative: false
```

문서 경로는
[`docs/codex/phases/phase-04-cow-alns-and-reproducibility.md`](phase-04-cow-alns-and-reproducibility.md)다.
이 문서는 구현 결과가 아니라 후속 구현 세션이 질문 없이 테스트부터 작성하도록 고정한
실행 명세다. 이 문서에서 새로 제시하는 Java 이름과 signature는 모두 **계획상 제안
API**이며 승인된 public API, wire schema 또는 저장 schema가 아니다.

## 1. 목표와 종료 산출물

AR-4는 AR-3이 넘긴 최대 8개 독립 initial candidate를 다음 순서로 처리하는
provider-neutral solver 구현을 만든다.

```text
available initial candidates
→ candidate별 exact phase-1 screen
→ 모든 available screen의 정상 완료와 cache-free 재평가 확인
→ stable phase-1 champion
→ champion을 공통 warm start로 쓰는 declared phase-2 worker batch
→ declared worker 전체의 정상 완료와 cache-free 재평가 확인
→ completion-order와 무관한 stable round champion
→ strictly-better면 다음 round, equal/worse면 NO_STRICT_IMPROVEMENT
→ configured round를 모두 마치면 MAX_ROUNDS_REACHED
→ last committed champion + exact lineage
```

한 ALNS step은 request-pair destroy, bank 기반 pair repair, 명시된 bounded
improvement, stable candidate 검증·평가, hard feasibility와 stage guard, acceptance,
commit/discard, current/best 갱신, adaptive state 갱신, completed-step 증가까지 모두
끝났을 때만 완료다. Candidate는 changed-route COW와 독립 bank를 사용한다. Reject,
예외, cancel, watchdog 또는 resource signal을 받은 미완료 step은 candidate 전체를
폐기하며 current, stageBest, solveBest, cache visibility, adaptive/acceptance state와
completed-step count를 바꾸지 않는다.

종료 산출물은 다음 네 가지다.

1. 모든 available initial candidate를 정상 screen한 `PhaseOneChampion`
2. 선언 worker를 모두 완료한 각 `PhaseTwoRoundResult`와 마지막
   `CommittedCandidate`
3. exact step/round/operator/seed/warm-start/termination lineage와 canonical trace
4. 같은 정상 reproducibility envelope를 반복했을 때 같은 trace와 solution
   fingerprint를 얻었다는 evidence

AR-4 산출물은 아직 `VerifiedSolution`이나 publishable result가 아니다. 독립 candidate
verification과 result-integrity verification은 AR-5의 책임이다.

## 2. Authority, 결정 상태와 충돌 처리

### 2.1 적용 authority

| Authority | 적용 절 | 이 phase가 상속하는 계약 | 상태 |
|---|---|---|---|
| [전체 구현 계획](../implementation-plan.md) | §5, §8, §9.5, §10~§11 | COW, exact step, test-first 순서, evidence bundle과 13개 phase 문서 계약 | `REVIEW` 성격의 상위 migration 계획 |
| [Master Design](../../master-design.md) | §6, §10~§13, §15.6, §16 | Atomic pair, search/result 분리, two-phase ALNS, COW/cache, termination, strong reproducibility, RM-4 gate | `3.2-review`, `REVIEW` |
| [Domain Design](../../domain-design.md) | §10~§12, §15~§16 | Stable partition, independent bank, derived cache, propagation/evaluation 소비 경계, search defect와 acceptance evidence | `2.2-review`, `REVIEW` |
| [Architecture Design](../../architecture-design.md) | §6~§7, §11, §14, §17~§19 | `rpdptw-solver` package 책임, dependency DAG, logical batch 의미, failure/observability와 build gate | `1.1-review`, `REVIEW` |
| [질문 등록부](../../master-design-open-questions.md) | `Q-ALG-01`, `Q-ALG-02`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | 최대 8개 portfolio와 phase-1/2 구조, `KEEP_COW`, 공식 수치 미확정, physical/variant deferred | `2.1-review`, `REVIEW` |

세 설계와 구현 계획은 현재 승인된 외부 API가 아니다. 이 문서의 package/type/API는
계획상 고정 제안이며, 구현 중 외부 계약이나 승인 ADR과 충돌하면 코드를 만들어
해결하지 않는다. 충돌 시 다음 순서를 적용한다.

1. 채택된 외부 계약과 승인 Decision Record
2. `APPROVED` Master
3. `APPROVED` 상세 설계
4. 현재 `REVIEW` Master/Architecture/Domain과 전체 구현 계획
5. 역사 세션, 연구 자료와 legacy 구현

의미 충돌은 blocker ID, 영향 절, 마지막 안전 commit/tree와 필요한 승인 문서를
evidence에 기록하고 `BLOCKED`로 중단한다. Architecture Design은 module/package
배치를, Domain Design은 domain 의미를 소유한다. 어느 쪽도 Master의 pair/COW/
termination/reproducibility 불변조건을 약화할 수 없다.

### 2.2 질문 상태와 AR-4 영향

| 질문/결정 | 현재 상태 | AR-4 판정 |
|---|---|---|
| `Q-ALG-01` | `RESOLVED` | 4×2 최대 8개 initial candidate, candidate별 phase-1 screen, 단일 champion을 phase-2 공통 warm start로 사용한다. |
| `Q-ALG-02` | `RESOLVED — KEEP_COW` | Changed-route COW와 independent bank가 유일한 production baseline이다. Apply/undo type, log, skeleton도 만들지 않는다. |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Generic AR-4와 explicit test-only config는 차단하지 않는다. `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`, watchdog의 공식값/default/benchmark manifest는 만들 수 없다. |
| `Q-INFRA-01` | `DEFERRED` | Physical dispatcher, cloud worker, storage, workflow와 provider SDK는 범위 밖이다. Solver는 in-process deterministic runner와 immutable value만 제공한다. |
| `Q-VAR-01` | `DEFERRED` | Optional variant, multi-trip/rotation과 pair/terminal/bank 완화는 금지한다. |

Test fixture의 작은 양의 step/round 값은 test method 또는 fixture builder에서
명시적으로 주입하고 이름과 evidence에 `TEST_ONLY`를 남긴다. README의
`parallelRuns=8`, `iterationsPerRun=5000`, 현재 workflow timeout과 seed 계산은 legacy
characterization일 뿐 이 phase의 default 또는 공식값이 아니다.

### 2.3 Entry blocker

현재 저장소에는 AR-0~AR-3 reactor/source/evidence가 없으므로 구현 관점의 AR-4는
`BLOCKED`다. 코드 구현은 시작되지 않았으며, 후속 구현 세션은 다음 AND gate를 먼저
검사한다.

| Blocker ID | 필요한 선행 evidence | 없을 때 판정 | 재개 조건 |
|---|---|---|---|
| `AR4-B01-REACTOR` | AR-0 `DONE`, `rpdptw/solver` module과 architecture rules | `BLOCKED` | Root reactor와 solver module `verify` evidence. 여러 문서 세션이 공유 `target/`에서 동시에 실행해 생긴 transient Maven/JAR replace 실패만으로는 blocker를 만들지 않음 |
| `AR4-B02-PROBLEM` | AR-1 `ProblemInstance`, `PreparedTravel`, test builders와 fingerprint | `BLOCKED` | Immutable input/travel handoff와 complete travel evidence |
| `AR4-B03-EVALUATION` | AR-2 `BoundProfile`, comparator, `SolvePlan`, cache-free full evaluation API | `BLOCKED` | Exact version/dependency closure와 comparator/stage guard evidence |
| `AR4-B04-PORTFOLIO` | AR-3 `CommittedCandidate`, `InitialPortfolio`, atomic pair insertion evaluator와 candidate isolation evidence | `BLOCKED` | 최대 8개 candidate, stable IDs/fingerprints와 AR-3 evidence digest |
| `AR4-B05-API-CONFLICT` | 이 문서의 계획상 API와 선행 phase 실제 API 간 mapping review | `BLOCKED` if 의미 충돌 | 이름만 다르면 mapping table; 의미가 다르면 영향 문서/ADR 승인 |

`Q-BENCH-02`는 `AR4-B*`가 아니다. AR-4의 generic correctness를 수행할 수 있지만
공식 수치, official Win run과 baseline을 주장할 수 없다는 별도 downstream gate다.

## 3. 작성 시점 실제 저장소 조사

조사 working directory는 `/Users/brown/workspace/ro-next`다. 작성 직전
`git status --short --untracked-files=all`, `find . -name pom.xml`,
`rg --files`, source/test/POM/README/GCP/data inspection과 source hash 계산을
수행했다.

### 3.1 Working tree와 source baseline

- Branch/HEAD는 `codex/domain-design` /
  `523c23e2e13410885b16e974efe40ffe598106ee`였다.
- 기존 tracked 변경은 `docs/master-design.md`, `docs/architecture-design.md`,
  `docs/domain-design.md`, 질문 등록부와 여러 역사/연구 문서에 존재했다.
- `data/`, `docs/codex/`, Obsidian 설정과 다수 설계 문서는 이미 untracked였다.
- 위 변경은 사용자 또는 다른 세션 소유이며 AR-4 문서 작성/구현 세션이 되돌리거나
  정리할 수 없다.
- 작성 시 `docs/codex/phases/`와 담당 파일은 존재하지 않았고,
  `target/codex-evidence/`도 존재하지 않았다.
- `docs/master-design.md`, `docs/architecture-design.md`,
  `docs/domain-design.md`의 실제 SHA-256은 전체 구현 계획 metadata와 정확히
  일치했다. 구현 계획 자체의 작성 시 SHA-256은 metadata에 별도 고정했다.
- `data/win_poc_case.json`의 실제 SHA-256은
  `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`였다.
  이 파일에는 문자열 소수 `D/U`가 있으므로 AR-4 test fixture, normalization 또는
  official benchmark input으로 변환·사용하지 않는다.

병렬 문서 작성 중 AR-0~AR-3 phase 문서가 새로 나타났다. 상태 정규화 기준에서
AR-0만 `NOT_STARTED`이고 AR-1~AR-3 및 이 AR-4는 필수 선행 evidence가 없으므로
`BLOCKED`다. 따라서 이 문서들은 계획상 predecessor 설명에는 반영하되 AR-4 entry
evidence로 승격하지 않는다. AR-3의
계획상 고정 경로인 `solver/state/CommittedCandidate`,
`solver/portfolio/InitialPortfolio`,
`evaluation/insertion/AtomicPairInsertionEvaluator`를 확인해 아래 예상 경로를
정합화했다. 다른 phase 문서의 내용은 수정하지 않았다.

### 3.2 실제 build/module graph

현재 repository POM은 root [`pom.xml`](../../../pom.xml) 하나뿐이며
`packaging`을 생략한 단일 `jar`다. Java release 25, Maven
`[3.9.14,)`, Java `[25,26)` Enforcer, Surefire와 Shade plugin이 있다. Google Cloud
Workflow/Storage, Jackson과 JUnit dependency가 root classpath에 직접 있다.
`rpdptw/core`, `rpdptw/solver`, `rpdptw/verification`, `rpdptw/application`,
`build/test-fixtures`와 `build/architecture-rules` module은 아직 없다.

작성 시 확인한 toolchain은 Maven `3.9.14`, Amazon Corretto Java `25.0.3`,
macOS aarch64다. 관리 세션이 전달한 phase-00 격리 관찰에서는 같은 Java/Maven
toolchain으로 기존 `mvn test`와 `mvn verify`가 성공했다. 이를 current legacy 공통
baseline으로 사용한다. 기존 `target/surefire-reports`에도 2026-07-24에 실행된 legacy
test 1건 성공 report가 있었지만, 이것만으로 AR-4 entry evidence나 향후 reactor 검증을
대체하지는 않는다.

여러 phase 문서 세션이 같은 working directory의 `target/`에서 Maven을 동시에
실행하면 Shade JAR replace 같은 transient 충돌이 생길 수 있다. 그런 동시 실행 실패는
repository baseline defect, AR-4 blocker 또는 test red evidence로 확정하지 않는다.
AR-4 구현 세션의 Maven 판정은 다른 writer와 겹치지 않는 격리/직렬 실행에서 재현된
결과만 사용한다. 이 문서 작성 세션은 관리 세션 정정에 따라 Maven을 다시 실행하지
않고 link/hash/diff/scope 검증으로 마무리한다.

### 3.3 실제 production/test/runtime inventory

| 영역 | 실제 파일/상태 | AR-4 해석 |
|---|---|---|
| Placeholder engine | `src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | `SplittableRandom`, `double objective`와 iteration 산술로 `Map<String,Object>`를 반환한다. Pair/COW/ALNS evidence가 아니다. |
| HTTP/API | `src/main/java/com/ronext/optimizer/adapter/in/http/*.java` 5개 | Controller가 SDK client, 시간/UUID/default와 result selection을 직접 소유한다. AR-4에서 수정하지 않는다. |
| Legacy test | `src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java` | status/run number/양수 objective만 검사한다. Characterization 외 AR-4 test로 재사용하지 않는다. |
| Root README | `README.md` | `parallelRuns=8`, `iterationsPerRun=5000`, seed 예시는 official 값이 아니다. |
| GCP 자료 | `gcp/README.md`, `gcp/workflows/optimization.yaml`, `gcp/cloudbuild.yaml` | 일부 batch 성공과 `double objective` 최소화를 전제로 하는 legacy physical topology다. 수정·소비하지 않는다. |
| Data | `data/win_poc_case.json`, `data/ro_input_json_spec.pdf` | Read-only/legacy fixture다. AR-4 synthetic in-memory fixture로 대체하며 값을 추측/변환하지 않는다. |

`OptimizationApiController`는 누락 config를 `parallelRuns=8`,
`iterationsPerRun=5000`, `System.nanoTime()` seed로 채운다.
`OptimizationWorkerController`와 workflow는 `seed + runNumber`로 batch를 실행하고,
prefix 아래 일부 candidate 중 `double objective`가 가장 작은 결과를 고른다. 이 동작은
strong reproducibility, complete declared worker batch, stable reduction, verifier 또는
normal/exceptional termination을 보장하지 않는다.

## 4. 현 상태 → 목표 상태 gap

| 관심사 | 현 상태 | AR-4 목표 |
|---|---|---|
| Module/namespace | 단일 `com.ronext.optimizer` JAR | `rpdptw-solver` / `com.ronext.rpdptw.solver.*`; solver→core 이외 compile dependency 없음 |
| Input state | `requestId`, URI, run number와 scalar iteration | AR-1~3 immutable problem/travel/profile/candidate와 explicit run config |
| Mutation | RPDPTW state가 없음 | Changed route만 first-write copy, independent bank, immutable current/stageBest/solveBest |
| Pair operation | 없음 | Destroy/remove와 repair/insert 모두 request pair atomic; partial pair 노출 0 |
| Cache | 없음 | Route/bank가 authority, derived cache/fingerprint invalidation, poisoned cache 무관성 |
| Acceptance | scalar objective formula뿐 | Hard feasibility와 AR-2 `StageGuard` 직접 호출 이후 config-selected policy; worse accepted current와 best 분리 |
| Adaptive update | 없음 | Stable operator IDs와 deterministic weighted selection; completed step 뒤 exact update |
| Phase 1 | 없음 | 모든 available initial candidate exact screen 정상 완료 뒤 stable champion |
| Phase 2 | workflow 일부 batch | Declared worker 전체 정상 완료·재평가 뒤 stable batch champion; partial success 금지 |
| Step/round | `iterations` 전달만 함 | Requested/completed step, round, operator와 inner work를 분리한 exact accounting |
| Seed | `seed + runNumber`, API fallback clock | Versioned canonical derivation, namespaced base/derived seed, global/clock random 금지 |
| Termination | result status `CANDIDATE`/`COMPLETED` | Normal `MAX_STEPS_REACHED`/`NO_STRICT_IMPROVEMENT`/`MAX_ROUNDS_REACHED`와 exceptional signal 분리 |
| Reproducibility | seed/run 일부만 보존 | Fixed envelope의 canonical trace와 solution fingerprint exact equality |
| Failure | Controller가 모든 예외를 500으로 축약 | Typed stable failure code, last committed boundary, incomplete mutation 0 |
| Verification | 없음 | AR-4 cache-free full evaluation; AR-5 독립 verifier가 소비할 authority artifact 제공 |

Legacy source, POM, README, GCP와 data는 AR-4에서 이동·삭제·수정하지 않는다.
AR-0 migration이 그것들을 `legacy/current-app`으로 이미 옮겼다면 AR-4는 그 결과도
수정하지 않는다.

## 5. Scope와 관통 불변조건

### 5.1 In scope

- `rpdptw-solver`의 `state`, `search`, `termination`, 기존 `portfolio` 연계
- Changed-route COW, independent copied bank, cache invalidation과 commit/discard
- Config-selected request-pair destroy와 pair repair
- Immutable bound `SolvePlan`이 가진 AR-2 `StageGuard` 직접 호출, acceptance와 current/stageBest/solveBest 분리
- Completed step 이후에만 하는 adaptive operator weight update
- Candidate별 phase-1 screen과 all-available stable champion selection
- Common warm start를 쓰는 complete phase-2 worker batch와 pure solver-side round loop
- Exact step/round/operator/seed/warm-start lineage, canonical trace/fingerprint
- Watchdog/cancel/resource/failure safe-point handling과 last committed boundary
- Hit/miss/stale/poisoned cache 대비 cache-free full evaluation equality

### 5.2 반드시 유지할 불변조건

1. Stable state마다 request는 same-route complete pair 또는 independent bank 중 정확히
   하나다.
2. Candidate가 직접 current, stageBest, solveBest의 route, bank, cache 또는
   fingerprint를 바꾸지 않는다.
3. Unchanged route는 immutable하게 공유할 수 있지만, changed route는 첫 write 전에
   복사한다.
4. Candidate bank는 base bank와 별도 value다. Pair remove/insert는 route와 bank를 한
   atomic operation으로 바꾼다.
5. Route/bank mutation은 영향받은 route/solution derived cache와 structural/result
   fingerprint를 무효화한다.
6. Reject, exception, cancel, watchdog, resource 또는 invariant failure는 candidate
   전체를 discard한다.
7. 미완료 step은 completed count, acceptance state, adaptive weight와 canonical
   completed-step trace를 전진시키지 않는다.
8. Hard-infeasible candidate는 acceptance policy에 전달하지 않는다. 완결된
   hard/stage rejection은 stable discard outcome으로만 기록한다.
9. Non-improving feasible candidate가 acceptance에 의해 current가 되어도 stageBest와
   solveBest는 악화되지 않는다.
10. Cache hit/miss/poison 여부가 feasibility, metric, score, objective, acceptance,
    best selection과 fingerprint를 바꾸지 않는다.
11. Candidate, operator, request, vehicle, route, position, worker와 reduction 순서는
    stable ID total order를 갖는다.
12. Elapsed time, thread scheduling, completion order와 cache hit는 quality/seed/tie의
    입력이 아니다.
13. Phase-1 champion은 모든 available candidate가 `MAX_STEPS_REACHED`하고 AR-4
    cache-free validation을 통과한 뒤에만 생긴다.
14. Phase-2 round champion은 선언 worker 전체가 `MAX_STEPS_REACHED`하고 AR-4
    cache-free validation을 통과한 뒤에만 생긴다.
15. AR-4의 “validation”은 AR-5의 독립 candidate verifier `PASS`가 아니다.
16. Official 수치, provider, optional variant, Win fixture 변환과 apply/undo를
    도입하지 않는다.

## 6. 정확한 예상 경로

아래 경로는 승인된 외부 surface가 아니라 **계획상 고정 제안**이다. 선행 phase가
동일 의미를 다른 내부 이름으로 이미 구현했다면 구현 시작 전에 mapping table을 이
phase evidence에 추가한다. 의미 변경은 이름 mapping으로 처리하지 않는다.

### 6.1 선행 phase에서 소비만 할 경로

| 예상 경로 | Owner phase | AR-4 사용 | AR-4 변경 |
|---|---|---|---|
| `rpdptw/core/src/main/java/com/ronext/rpdptw/domain/ProblemInstance.java` | AR-1 | Immutable problem authority | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/travel/PreparedTravel.java` | AR-1 | Complete directed travel authority | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/BoundProfile.java` | AR-2 | Comparator, `SolvePlan`, full evaluation binding | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/ObjectiveComparator.java` | AR-2 | Objective vector와 stable structural tie total order | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/SolvePlan.java` | AR-2 | Ordered stage, guard와 budget reference | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/StageGuard.java` | AR-2 | Candidate objective와 verified stage best의 protected prefix gate | 금지; AR-4가 import해 직접 호출 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/runtime/FullEvaluationEngine.java` | AR-2 | Cache-free route/solution recomputation | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/AtomicPairInsertionEvaluator.java` | AR-3/core | Repair option enumeration/evaluation | 금지 |
| `rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/insertion/FeasiblePairInsertionOption.java` | AR-3/core | Base-bound atomic pair insertion option | 금지 |
| `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CommittedCandidate.java` | AR-3 | Package `com.ronext.rpdptw.solver.state`의 immutable route/bank/evaluation/fingerprint handoff | 필요 시 호환 adapter만 별도 review; 원의미 수정 금지 |
| `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/AtomicPairMoveApplier.java` | AR-3 | Stale option check, atomic selected insertion과 cache-free equality | 금지; AR-4 insert path가 재사용 |
| `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/portfolio/InitialPortfolio.java` | AR-3 | Available candidate와 `UNAVAILABLE` provenance | 금지 |
| `build/test-fixtures/src/main/java/com/ronext/rpdptw/testfixture/**` | AR-1~3 | Synthetic problem/profile/portfolio builders와 hand oracle | production scope 변경 금지 |

### 6.2 테스트 파일 — 반드시 production보다 먼저 생성

| 작업 | 정확한 경로 |
|---|---|
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/state/CowCandidateStateTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/state/CowCandidateStateFaultTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/state/CacheInvalidationTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/PairDestroyRepairTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/AcceptanceAndBestIsolationTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/AdaptiveOperatorSelectorTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/AlnsStepAccountingTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/CacheFreeEvaluationEquivalenceTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/PhaseOneScreenTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/PhaseTwoCompleteBatchTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/CompletionOrderIndependenceTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/SeedLineageTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/RandomStreamTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/search/AlnsReproducibilityTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/termination/TerminationSemanticsTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/termination/RunControlFaultTest.java` |
| 신규 | `rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/architecture/SolverBoundaryArchitectureTest.java` |

Test-only fake는 각 test의 nested class 또는
`rpdptw/solver/src/test/java/com/ronext/rpdptw/solver/testing/` 아래에만 둔다.
Win fixture를 복사하거나 test resource JSON으로 변환하지 않는다. 작은 explicit
step/round 수는 synthetic Java fixture builder가 주입한다.

### 6.3 Production/POM 파일 — test red 뒤 생성 또는 변경

| 작업 | 정확한 경로 | 책임 |
|---|---|---|
| 검토만(예상 diff 0) | `rpdptw/solver/pom.xml` | AR-0/AR-3가 solver→core와 test-fixtures test-scope를 제공해야 한다. 누락 시 AR-4가 임시 수정하지 않고 owner phase blocker로 반환; 새 runtime/provider dependency 0 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/SearchIncumbents.java` | Immutable current/stageBest/solveBest triple과 strict best replacement |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CowCandidateState.java` | Changed-route first-write copy, independent bank, atomic pair mutation, freeze/discard |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/DerivedStateCache.java` | Problem/profile/run/structure-scoped derived values |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CacheInvalidation.java` | Route/bank/solution 영향 범위와 generation invalidation |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/state/CacheFreeCandidateEvaluator.java` | AR-2 `FullEvaluationEngine`으로 route/bank authority를 재구성·전체 평가하는 얇은 adapter |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PairDestroyOperator.java` | Stable pair destroy SPI |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PairRepairOperator.java` | Bank 기반 atomic pair repair SPI |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/UniformPairDestroyOperator.java` | Explicit count와 stable random stream을 쓰는 최소 generic destroy |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/GreedyPairRepairOperator.java` | 공통 insertion evaluator와 stable tie로 bank를 repair하는 최소 generic repair |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/AcceptancePolicy.java` | Feasible/stage-allowed candidate acceptance SPI와 immutable state transition |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/LexicographicGreedyAcceptance.java` | Explicitly selected, no-hidden-default reference acceptance |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/AdaptiveOperatorSelector.java` | Stable operator selection/update SPI |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/SegmentedAdaptiveOperatorSelector.java` | Versioned exact-integer weight selection/update |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/AlnsRunConfig.java` | Stage, operator, acceptance, adaptive, exact positive steps; default 없음 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/AlnsStepExecutor.java` | 한 step의 전체 전이와 safe point |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/WorkerRunRequest.java` | Warm start, exact config, seed lineage와 logical ordinal |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/WorkerRunResult.java` | Last committed best, exact worker termination/trace/work |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/AlnsWorkerRunner.java` | Exact max-step worker 실행 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PhaseOneScreenRunner.java` | All-available screen completeness와 champion selection |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PhaseOneChampion.java` | Candidate/result lineage가 있는 phase-1 output |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PhaseTwoBatchPlan.java` | Explicit declared worker specs; worker 수 default 없음 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PhaseTwoBatchRunner.java` | Complete worker execution/fan-in와 stable champion |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PhaseTwoRoundResult.java` | Completeness, previous/champion comparison과 lineage |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/TwoPhaseSearchRunner.java` | Explicit maxRounds loop와 normal coordinator termination |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/TwoPhaseRunConfig.java` | Explicit phase-1 config, declared phase-2 workers와 positive maxRounds; default 없음 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/TwoPhaseSearchResult.java` | Phase-1/round lineage, last champion과 coordinator termination |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/PhaseOneScreenResult.java` | Complete champion 또는 exact incomplete candidate set의 sealed result |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/CanonicalStepTrace.java` | Completed-step canonical fields와 trace fingerprint |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/SeedLineage.java` | Base/derived seed, namespace, derivation version, round/worker ordinal |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/SeedDeriver.java` | Versioned deterministic derivation SPI |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/Sha256SeedDeriverV1.java` | Canonical UTF-8 length-prefix input과 SHA-256 first signed 64-bit big-endian derivation |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/RandomStream.java` | Namespaced deterministic random contract와 draw accounting |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search/SplitMix64RandomStreamV1.java` | Versioned fixed algorithm implementation; JVM default/global random 비의존 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/TerminationReason.java` | Normal/exceptional 종료의 closed enum |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/RunControl.java` | Safe-point별 cooperative signal poll |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/RunControlFactory.java` | Candidate/worker별 독립 cooperative control 생성 |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/ControlSignal.java` | `CONTINUE`, `CANCEL`, `WATCHDOG`, `RESOURCE_LIMIT` |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/TerminationRecord.java` | Requested/completed steps, last boundary, cause와 rollback integrity |
| 신규 | `rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/termination/SearchFailure.java` | Stable failure code와 non-semantic diagnostic metadata |

새 resource, wire DTO, database schema와 provider file은 만들지 않는다. AR-4에서 file move나
legacy deletion도 없다.

## 7. 계획상 제안 type/API

### 7.1 API 표기 원칙

아래 signature는 구현 세션의 질문을 줄이기 위한 계획상 제안이다. `public`은 module
내 의도적 contract를 뜻할 뿐 HTTP/public serialization 승인이 아니다. COW mutation
surface와 operator 구현은 가능한 package-private로 두고 application이 solver internal
state를 조작하지 못하게 한다.

### 7.2 COW state

```java
public record SearchIncumbents(
        CommittedCandidate current,
        CommittedCandidate stageBest,
        CommittedCandidate solveBest) {
    public SearchIncumbents acceptCurrent(CommittedCandidate accepted);
    public SearchIncumbents updateBest(
            CommittedCandidate accepted,
            ObjectiveComparator comparator);
}
```

```java
final class CowCandidateState {
    static CowCandidateState begin(
            CommittedCandidate base,
            DerivedStateCache baseCache);

    PairMutation removePair(RequestId requestId);

    PairMutation insertPair(FeasiblePairInsertionOption insertion);

    CandidateFreezeResult validateAndFreeze(
            CacheFreeCandidateEvaluator evaluator);

    void discard();
}
```

불변조건:

- `begin`은 bank를 독립 value로 복사하고 route는 immutable reference로 공유한다.
- `removePair`와 `insertPair`만 route/bank membership을 바꾼다. Generic node editor를
  exported API로 제공하지 않는다.
- `insertPair`는 AR-3 `AtomicPairMoveApplier`의 stale/base identity와 full-evaluation
  계약을 재사용한다. Pair placement나 insertion feasibility를 AR-4에서 다시 구현하지
  않는다.
- 변경할 route는 첫 write 전에 한 번만 복사한다. 다른 route identity는 유지한다.
- Pair remove가 성공하면 두 node가 route에서 사라지고 bank에 request가 정확히 한
  번 생긴다. Pair insert가 성공하면 반대 전이가 한 번에 일어난다.
- 어느 atomic 전이든 구조 검증에 실패하면 candidate 내부도 전이 전 상태로 남아야
  한다. Committed state에는 어떤 경우에도 영향이 없다.
- `validateAndFreeze`는 cache를 권위화하지 않고 route/bank에서 full evaluation을
  수행한다. 성공한 immutable result만 `CommittedCandidate`가 된다.
- `discard` 뒤 mutation/freeze 호출은 `IllegalStateException("candidate already discarded")`.
- Partial/duplicate/split/route+bank 오류는
  `SearchFailure.Code.STATE_INVARIANT_VIOLATION`이며 unassignment나 infeasible option으로
  바꾸지 않는다.

`DerivedStateCache`는 `(problemFingerprint, profileFingerprint, runIdentity,
routeStructureGeneration, evaluatorVersion)` scope를 반드시 확인한다. Mismatch는 miss로
폐기하며 “가까운” cache로 fallback하지 않는다. Poison detection에서 full evaluation과
다르면 `CACHE_MISMATCH`로 worker를 실패시키고 last committed state를 보존한다.

### 7.3 Pair destroy/repair

```java
public interface PairDestroyOperator {
    OperatorId id();

    DestroyResult destroy(
            CowCandidateState candidate,
            int requestedPairCount,
            RandomStream random);
}
```

```java
public interface PairRepairOperator {
    OperatorId id();

    RepairResult repair(
            CowCandidateState candidate,
            AtomicPairInsertionEvaluator evaluator,
            RandomStream random);
}
```

`UniformPairDestroyOperator`는 explicit positive `requestedPairCount`를 받고, assigned
request를 stable request ID 순으로 만든 뒤 해당 operator의 namespaced random stream으로
without-replacement 선택한다. Assigned count가 더 작으면 전부 제거하고 actual count를
기록한다. Node count를 입력으로 받지 않는다.

`GreedyPairRepairOperator`는 stable bank request order로 모든 합법 vehicle/route/
pickup-position/delivery-position option을 공통 evaluator에 요청하고, bound comparator와
stable `(request, vehicle, route, pickupPosition, deliveryPosition)` tie order로 하나를
선택해 atomic insert한다. Feasible option이 없는 request는 complete pair가 route에 없는
상태로 bank에 남는다. Operator별 feasibility 재구현, raw matrix/coordinate 접근과
partial pair ranking은 금지한다.

Operator ID 목록, destroy count, repair order와 bounded improvement work는
`AlnsRunConfig`에 exact version과 함께 명시한다. Config에 없는 operator를 registry/classpath
순서로 발견하거나 기본 선택하지 않는다.

### 7.4 Acceptance, best와 adaptive update

AR-4는 `com.ronext.rpdptw.evaluation.api.StageGuard`를 import하고 immutable bound
`SolvePlan`의 현재 `SolveStage.guard()`를 직접 호출한다.

```java
StageGuardResult guardResult = solveStage.guard().evaluate(
        candidate.objectiveVector(),
        incumbents.stageBest().evaluation().objectiveVector());
```

AR-4는 solver module에 별도 `StageGuard`, wrapper, adapter 또는 같은 의미의 alias를
정의하지 않는다. Hard-feasible candidate만 위 guard에 전달하고, guard가 거부한
candidate는 acceptance policy에 전달하지 않는다.

```java
public interface AcceptancePolicy {
    AcceptancePolicyId id();

    AcceptanceDecision decide(
            SolutionEvaluation current,
            SolutionEvaluation candidate,
            AcceptanceState state,
            RandomStream random);
}
```

```java
public interface AdaptiveOperatorSelector {
    SelectedOperatorPair select(RandomStream random);

    AdaptiveOperatorSelector afterCompletedStep(
            SelectedOperatorPair selected,
            CompletedStepOutcome outcome);
}
```

순서는 반드시 다음이다.

```text
full candidate evaluation
→ hard feasibility
→ AR-2 StageGuard.evaluate(candidate, verified stage best)
→ acceptance decision
→ accepted면 current 교체
→ strict comparator로 stageBest/solveBest만 개선
→ adaptive update
→ acceptance state update
→ completed-step +1
```

`LexicographicGreedyAcceptance`는 config가 exact ID로 선택할 때만 사용하고
`candidate <= current`인 feasible/stage-allowed candidate를 accept한다. 이것을 hidden
default로 사용하지 않는다. Worse acceptance를 지원하는 다른 policy도 같은 SPI를
사용할 수 있으며, test fake `AlwaysAcceptFeasible`로 current/best isolation을 먼저
증명한다. Profile/customer 이름을 acceptance에 전달하지 않는다.

`SegmentedAdaptiveOperatorSelector`의 계획상 exact update는 nonnegative checked integer
arithmetic다.

```text
0 < reaction <= scale
oldWeight > 0
reward > 0
newWeight =
  FLOOR(((scale - reaction) * oldWeight + reaction * reward) / scale)
```

모든 곱/합은 checked arithmetic을 사용한다. Destroy/repair operator는 stable ID로
정렬하고, selection ticket은 해당 namespaced random stream의
`nextLong(totalWeight)`를 사용한다. Reward table, scale, reaction과 segment length는
explicit config이며 static default가 없다. Update는 완결된 step에 한 번만 수행한다.
Interrupted/failed step에는 selection을 되돌리거나 보상 update를 하지 않고 이전 immutable
selector state를 유지한다.

`CompletedStepOutcome`의 계획상 값은 `NEW_SOLVE_BEST`, `NEW_STAGE_BEST`,
`ACCEPTED_CURRENT`, `REJECTED_BY_ACCEPTANCE`, `REJECTED_BY_STAGE_GUARD`,
`REJECTED_HARD_INFEASIBLE`다. Hard/stage rejection은 acceptance policy를 호출하지
않지만 candidate를 안정적으로 discard하고 adaptive rejection outcome까지 기록했을 때는
completed step이 될 수 있다. Exception/control signal/invariant/cache mismatch는 이 enum이
아니며 completed step이 아니다.

### 7.5 Worker, phase-1과 phase-2

```java
public record WorkerRunRequest(
        WorkerOrdinal workerOrdinal,
        CommittedCandidate warmStart,
        AlnsRunConfig config,
        SeedLineage seedLineage) {}
```

```java
public final class AlnsWorkerRunner {
    public WorkerRunResult run(
            WorkerRunRequest request,
            RunControl control);
}
```

```java
public final class PhaseOneScreenRunner {
    public PhaseOneScreenResult screenAll(
            InitialPortfolio portfolio,
            AlnsRunConfig explicitScreenConfig,
            long baseSeed,
            RunControlFactory controls);
}
```

```java
public final class PhaseTwoBatchRunner {
    public PhaseTwoRoundResult runCompleteBatch(
            RoundOrdinal round,
            CommittedCandidate commonWarmStart,
            PhaseTwoBatchPlan declaredWorkers,
            RunControlFactory controls);
}
```

```java
public final class TwoPhaseSearchRunner {
    public TwoPhaseSearchResult run(
            InitialPortfolio portfolio,
            TwoPhaseRunConfig explicitConfig,
            RunControlFactory controls);
}
```

`AlnsRunConfig`는 적어도 algorithm/operator/acceptance/adaptive/state-strategy version,
ordered stage configs, 각 stage의 positive `maxSteps`, bounded improvement work,
cache contract version과 trace encoding version을 가진다. Null, empty stage, 0/negative
step과 unknown operator/policy는
`IllegalArgumentException("explicit positive stage maxSteps and registered policies are required")`
로 solve 전에 거부한다. Static factory `defaultConfig`, 환경변수 fallback과 README 값
읽기는 금지한다.

Phase-1은 `InitialPortfolio.availableCandidates()` 전체를 stable candidate ID 순으로
각각 독립 screen한다. 각 candidate는 별도 COW state, cache, adaptive state와 random
namespace를 가진다. 하나라도 `MAX_STEPS_REACHED`가 아니거나 cache-free validation이
실패하면 `PhaseOneIncomplete`를 반환하고 champion을 만들지 않는다. Completion order는
comparator input order가 아니다.

`PhaseTwoBatchPlan`은 ordered `WorkerRunRequest` template 목록을 직접 가진다. Worker
수는 목록 크기이며 생략/default가 없다. 각 worker는 같은 warm-start fingerprint,
problem/travel/profile/config contract를 가지되 declared derived seed/operator config가
다를 수 있다. 모든 declared worker가 `MAX_STEPS_REACHED`하고 AR-4 cache-free validation을
통과해야 champion을 만든다. Missing/duplicate/mismatched worker는 `INCOMPLETE`이며 일부
성공 worker를 비교하지 않는다.

`TwoPhaseSearchRunner`는 complete round champion이 previous champion보다 comparator상
strictly better일 때만 다음 round 공통 warm start로 사용한다. Equal 또는 worse는
`NO_STRICT_IMPROVEMENT`, explicit positive `maxRounds`를 모두 완료하면
`MAX_ROUNDS_REACHED`다. Worker 중간 plateau, elapsed deadline과 first-winner는 정상
종료 조건이 아니다. Retry/attempt/CAS/physical dispatch는 AR-7/application 책임이다.

### 7.6 Seed와 canonical trace

```java
public interface SeedDeriver {
    SeedLineage derive(
            long baseSeed,
            String namespace,
            int roundOrdinal,
            int workerOrCandidateOrdinal);
}
```

`Sha256SeedDeriverV1`은 version literal, base seed의 8-byte big-endian, 각 namespace
field의 UTF-8 byte length와 bytes, nonnegative ordinals를 canonical order로 SHA-256에
입력하고 첫 8 byte를 signed `long` big-endian으로 읽는다. `seed + ordinal`,
`hashCode()`, clock, thread ID, completion order와 global random을 사용하지 않는다.
Namespace는 적어도 `PHASE1/<candidateId>/<operatorPurpose>` 또는
`PHASE2/<round>/<worker>/<operatorPurpose>`를 구분한다. Base/derived seed와 derivation
version은 모두 provenance에 남긴다.

`SplitMix64RandomStreamV1`은 derived seed를 initial 64-bit state로 쓰고 draw마다
`state += 0x9E3779B97F4A7C15`, xor-shift/multiply
`0xBF58476D1CE4E5B9`, `0x94D049BB133111EB`, final xor-shift 순서의 고정 V1
algorithm으로 `long`을 만든다. Positive bound draw는 power-of-two mask 또는
nonnegative 63-bit rejection sampling을 사용해 modulo bias를 피하고, bound가
positive가 아니면 즉시 거부한다. Golden vector test가 seed별 raw/bounded draw와 draw
count를 고정한다. `java.util.Random`, `SplittableRandom`, default
`RandomGeneratorFactory`의 runtime 선택에 semantic result를 맡기지 않는다.

Completed-step canonical trace 항목은 다음 순서를 고정한다.

```text
traceEncodingVersion
stageOrdinal
completedStepOrdinal
destroyOperatorId
repairOperatorId
destroyed stable request IDs
repaired stable request IDs and chosen positions
candidate full-evaluation fingerprint
hard/stage/acceptance outcome
current fingerprint after decision
stageBest fingerprint
solveBest fingerprint
adaptive weight state after update
completed-step count
```

Timestamp, elapsed, memory, thread, cache hit count와 exception message는 canonical
fingerprint에서 제외하고 telemetry/evidence metadata로만 둔다. Normal
reproducibility 비교는 requested/completed step과 canonical trace/solution fingerprint를
exact byte equality로 비교한다.

### 7.7 Termination과 error

```java
public enum TerminationReason {
    MAX_STEPS_REACHED,
    NO_STRICT_IMPROVEMENT,
    MAX_ROUNDS_REACHED,
    WATCHDOG_REACHED,
    CANCELLED,
    RESOURCE_LIMIT_REACHED,
    PLATFORM_TIMEOUT,
    FAILED
}
```

Worker 정상 종료는 `MAX_STEPS_REACHED`만 사용한다. `NO_STRICT_IMPROVEMENT`와
`MAX_ROUNDS_REACHED`는 complete batch 뒤 coordinator 결과다. Solver `RunControl`이
직접 만들 수 있는 exceptional signal은 cancel/watchdog/resource뿐이다.
`PLATFORM_TIMEOUT`은 상위 application이 algorithm record를 완성하지 못한 경우 AR-7에서
mapping하며 AR-4 worker가 watchdog을 그 이름으로 바꾸지 않는다.

```java
@FunctionalInterface
public interface RunControl {
    ControlSignal poll(SafePoint point, StepBoundary boundary);
}
```

Safe point는 `BEFORE_STEP`, `AFTER_DESTROY`, `AFTER_REPAIR`,
`AFTER_BOUNDED_IMPROVEMENT`, `BEFORE_COMMIT`, `AFTER_COMMIT`이다.
`AFTER_COMMIT` signal은 이미 완료된 직전 step을 되돌리지 않고 다음 step 시작 전에
종료한다. 그보다 앞선 signal은 in-flight candidate를 폐기하고 해당 step을 세지 않는다.

Operator/evaluator의 `RuntimeException`은 worker boundary에서
`SearchFailure`의 stable code로 분류하고 `FAILED`를 반환한다. JVM `Error`를 정상
failure로 삼아 계속 실행하지 않는다. Stable code는 `OPERATOR_FAILURE`,
`EVALUATION_FAILURE`, `STATE_INVARIANT_VIOLATION`, `CACHE_MISMATCH`,
`ARITHMETIC_FAILURE`, `UNEXPECTED_FAILURE`다. Java exception class/message/stack은
진단 metadata이며 semantic/canonical fingerprint 입력이 아니다.

`TerminationRecord`는 requested/completed steps, completed stages, last safe point,
last committed candidate fingerprint, signal/failure code, candidate discard 여부와
rollback integrity를 가진다. Exceptional 종료의 last committed best는 AR-5 두
verification gate 전에는 publishable/recovery result가 아니다.

## 8. 세분화된 test case

모든 test는 production implementation보다 먼저 작성하고 아래 “첫 실패”를 실제로
관찰해야 한다. 새 API를 처음 도입하는 slice에서는 `cannot find symbol` compile red가
허용된다. 최소 type skeleton을 추가해 compile시킨 직후 같은 test가 표의 semantic
assertion으로 다시 red가 되는 것을 확인한 뒤에만 동작 구현을 시작한다. 환경 오류,
dependency download 실패, “test가 없음”과 unrelated test failure는 유효한 red가 아니다.

| Test class.method | 종류/권위 | Fixture | Expected | 첫 실패 관찰 | Green |
|---|---|---|---|---|---|
| `CowCandidateStateTest.copiesOnlyChangedRouteOnFirstWrite` | Unit/property; Master §12.1 | 3 immutable routes, request 1개 이동 | 변경 route만 새 identity, 나머지 2개 동일 identity | API red: `cannot find symbol CowCandidateState`; semantic red: `expected unchanged route instance to be shared` | Copy count 1, unchanged identity와 base fingerprint 유지 |
| `CowCandidateStateTest.usesIndependentBankAndAtomicPairMembership` | Property; Master §6, Domain §10 | Route pair 2개와 bank request 1개 | remove 뒤 complete pair가 bank 1회, insert 뒤 route 1회/bank 0 | `expected bank membership XOR route membership for request R1` | 모든 중간 observer에 stable XOR만 노출 |
| `CowCandidateStateTest.rejectLeavesCurrentBestAndFingerprintUntouched` | Property; 계획 §9.5 | Worse candidate를 reject하는 scripted policy | current/stageBest/solveBest/cache/fingerprint exact before equality | `expected current fingerprint <before> but was <candidate>` | Candidate discarded, incumbent triple byte-equal |
| `CowCandidateStateFaultTest.discardsOnExceptionCancellationAndWatchdog` | Fault; Master §12~§13 | Safe point별 exception/cancel/watchdog injection | In-flight state 0개 commit, completed/adaptive 0 증가 | `expected completedSteps=0 after AFTER_REPAIR cancellation but was 1` | 모든 safe-point parameterized case가 last committed와 같음 |
| `CacheInvalidationTest.invalidatesRouteBankSolutionAndFingerprintScopes` | Unit; Domain §10.5 | Route mutation, bank-only mutation, poisoned generations | 영향 scope는 invalid, 비영향 route cache만 reuse | `expected route R1 cache generation invalidated` | Full invalidation matrix와 generation key 일치 |
| `PairDestroyRepairTest.destroyRemovesWholePairsAndRepairUsesCommonEvaluator` | Unit/property; Master §11.1/§11.4 | 3 assigned pair, scripted insertion options | Destroy node가 아닌 pair count, repair가 common evaluator stable best 사용 | `expected destroyed request IDs [R1,R3], found partial node` | Pair/bank XOR, evaluator call set/순서와 chosen positions exact |
| `PairDestroyRepairTest.leavesUnrepairableRequestAsCompleteBankMembership` | Unit; Domain §10.3 | 모든 option hard-infeasible인 bank pair | Node 0개 route, bank request 1개, normal rejection | `expected R2 to remain once in bank` | Partial pair/diagnostic 저장 없이 stable candidate |
| `AcceptanceAndBestIsolationTest.acceptedWorseCandidateChangesOnlyCurrent` | Unit; Master §11.4 | Test-only always-accept feasible policy와 worse objective | current=candidate, stageBest/solveBest=before | `expected solveBest fingerprint to remain <best>` | Best 두 개 불변, acceptance lineage 기록 |
| `AcceptanceAndBestIsolationTest.hardOrStageRejectedCandidateNeverCallsAcceptance` | Unit; Master §9/§11.4 | hard fail 1개, AR-2 `NoWorseObjectivePrefixGuard` fail 1개, spy acceptance | Core `StageGuard` 호출 뒤 acceptance calls 0, stable discard outcome | `expected acceptance invocation count 0 but was 1` | Core guard result/rejection code, adaptive update 1회, completed step 1 |
| `AdaptiveOperatorSelectorTest.updatesExactWeightsOnlyAfterCompletedStep` | Unit; Master §11.4/§13.1 | scale=10, reaction=2, old=10, reward=20 test-only | exact new weight 12; interrupted step remains 10 | `expected weight 12 but was 10` 또는 interrupted `was 12` | Checked formula/overflow rejection과 stable order 통과 |
| `AlnsStepAccountingTest.countsOnlyFullyCompletedSteps` | Unit/fault; Master §11.4/§13.1 | 각 safe point 중단과 2개 complete outcomes | 완결 2개만 count/trace/adaptive update | `expected completedSteps=2 but was 3` | requested/completed/inner work 분리 exact |
| `CacheFreeEvaluationEquivalenceTest.matchesAcrossHitMissAndPoisonedCache` | Corruption/property; 구현 계획 §9.5 | 같은 candidate의 hit/miss/stale/poison cache | Hit/miss evaluation exact 같고 poison은 `CACHE_MISMATCH`, best 미변경 | `expected full objective <x> but cached was <y>` | 정상 두 경로 exact equality, poison typed failure/discard |
| `PhaseOneScreenTest.waitsForEveryAvailableCandidateBeforeChampion` | Integration; Master §11.3 | available 3, unavailable CLOCK 1, one delayed result | Available 3개 모두 normal+validated 뒤 stable champion | `expected no champion while candidate C3 incomplete` | Unavailable provenance 보존, incomplete면 champion absent |
| `PhaseOneScreenTest.rejectsAnyAbnormalOrUnvalidatedScreen` | Fault; 계획 §9.5 | worker watchdog 1개/validation fail 1개 | `PhaseOneIncomplete`, partial champion 없음 | `expected phase-one status INCOMPLETE but was COMPLETE` | Missing IDs와 actual termination exact |
| `PhaseTwoCompleteBatchTest.requiresEveryDeclaredWorkerNormalAndValidated` | Integration/fault; Master §11.3 | declared 4, success 3 + missing/fail/unvalidated permutation | 모든 case `INCOMPLETE`, champion/next round 없음 | `expected no round champion for missing worker W3` | Completeness set exact, 일부 성공 비교 0회 |
| `PhaseTwoCompleteBatchTest.usesStrictlyBetterChampionOrStopsPlateau` | Unit/integration; Master §11.3 | better/equal/worse hand comparator cases | Better만 next warm start; equal/worse `NO_STRICT_IMPROVEMENT` | `expected NO_STRICT_IMPROVEMENT for EQUAL` | Round lineage와 previous/common warm start exact |
| `CompletionOrderIndependenceTest.selectsSameChampionForEveryPermutation` | Property/reproducibility; Architecture §11/§14 | 4 worker result의 24 permutation | Champion ID/fingerprint와 reduction trace exact 동일 | `expected champion W2 but permutation selected W4` | 모든 permutation byte-equal |
| `SeedLineageTest.derivesStableNamespacedSeedsWithoutOrdinalAddition` | Unit; Master §13.2 | fixed base, phase/candidate/round/worker namespaces | Golden V1 long 값, namespace collision 0 | `expected seed <golden> but was <base+ordinal>` | UTF-8 length-prefix vector와 repeat equality |
| `RandomStreamTest.matchesSplitMix64V1GoldenRawAndBoundedDraws` | Unit/reproducibility; Master §13.2 | fixed derived seeds, raw and bounds 1/2/3/large | Golden long/bounded sequence와 draw count exact | `expected V1 draw[2]=<golden> but was <actual>` | 3회 repeat와 bound property/golden vector exact |
| `AlnsReproducibilityTest.repeatsTraceSolutionAndFingerprint` | Reproducibility; Master §13.2 | Synthetic fixed problem/travel/profile, 3 candidates, explicit test-only 5/7 step configs | 반복 및 worker completion permutation에서 trace/solution exact equality | `expected identical trace fingerprint, first=<a>, second=<b>` | 최소 3회 run과 permutation byte-equal |
| `TerminationSemanticsTest.separatesNormalAndExceptionalReasons` | Unit; Master §13.1 | max steps, plateau, max rounds, watchdog, cancel, resource, failure | 각 exact enum; 서로 alias 없음 | `expected WATCHDOG_REACHED but was MAX_STEPS_REACHED` | 요청/완료/last boundary와 reason matrix 통과 |
| `RunControlFaultTest.signalBeforeCommitDiscardsButAfterCommitPreservesCompletedStep` | Fault; Master §13.1 | BEFORE_COMMIT/AFTER_COMMIT signal | Before: count 0/base; after: count 1/new committed | `expected AFTER_COMMIT completedSteps=1 but was 0` | Boundary별 candidate/fingerprint exact |
| `SolverBoundaryArchitectureTest.forbidsProviderCustomerClockAndApplyUndoDependencies` | Architecture; Architecture §7/§20 | Source/bytecode scan | Provider/HTTP/application/verifier/customer-name/global-clock/apply-undo 참조 0 | Violation message가 exact offending class/package를 열거 | `mvn verify` architecture report violation 0 |
| `SolverBoundaryArchitectureTest.hasNoHiddenOfficialNumericDefaults` | Architecture/governance; `Q-BENCH-02` | Config factory/constant/source scan | Missing explicit config 거부, 공식 숫자/default 없음 | `expected missing screenMaxSteps to be rejected` | Test-only values만 fixture scope, production fallback 0 |

## 9. 테스트 우선 실행 순서

각 numbered slice는 독립 red→green evidence를 남긴다. 여러 production type을 먼저
한꺼번에 scaffold한 뒤 test를 쓰지 않는다.

1. **Baseline 고정**
   - `git status --short --untracked-files=all`, source hash, AR-0~3 evidence digest와
     `mvn -version`을 `commands.log`에 기록한다.
   - AR-3 module verify가 green이 아니면 AR-4 test를 쓰지 않고 `BLOCKED`다.
2. **COW API red**
   - `CowCandidateStateTest`와 `CowCandidateStateFaultTest`를 먼저 생성한다.
   - `cannot find symbol CowCandidateState`를 red report로 보존한다.
   - Compile만 되는 최소 type skeleton을 추가하고 semantic assertion red를 다시
     관찰한다.
   - Changed-route copy, independent bank, atomic mutation, freeze/discard의 최소
     동작만 구현한다.
3. **Cache red**
   - `CacheInvalidationTest`와 `CacheFreeEvaluationEquivalenceTest`를 작성한다.
   - Generation invalidation 또는 poison mismatch assertion red를 확인한 뒤
     `DerivedStateCache`와 `CacheInvalidation`을 구현한다.
4. **Pair operator red**
   - `PairDestroyRepairTest`를 먼저 작성하고 partial pair/evaluator call mismatch red를
     확인한다.
   - Operator SPI, uniform pair destroy와 common-evaluator greedy repair만 최소
     구현한다.
5. **Acceptance/best red**
   - `AcceptanceAndBestIsolationTest`를 작성해 worse accepted candidate가 best를
     오염시키는 red와 hard/stage fail이 acceptance를 호출하는 red를 확인한다.
   - `SearchIncumbents`, `AcceptancePolicy`와 explicitly selected greedy
     implementation만 구현한다. Stage gate는 AR-2 `SolveStage.guard()`를 직접
     호출하고 solver-local guard type을 만들지 않는다.
6. **Adaptive/step red**
   - `AdaptiveOperatorSelectorTest`, `AlnsStepAccountingTest`를 작성한다.
   - Exact weight 또는 interruption count red를 확인한 뒤 adaptive selector와
     `AlnsStepExecutor`를 구현한다.
7. **Termination red**
   - `TerminationSemanticsTest`, `RunControlFaultTest`를 작성한다.
   - Wrong alias/boundary red를 확인한 뒤 termination/control/failure types와 worker
     safe-point handling을 구현한다.
8. **Seed/trace red**
   - `SeedLineageTest`, `RandomStreamTest`를 먼저 작성하고 golden mismatch red를
     확인한다.
   - V1 derivation, fixed random stream과 canonical completed-step trace를 구현한다.
9. **Phase-1 red**
   - `PhaseOneScreenTest`를 작성해 일부 완료 champion 생성 red를 확인한다.
   - 모든 available candidate의 independent worker run과 stable fan-in을 구현한다.
10. **Phase-2 red**
    - `PhaseTwoCompleteBatchTest`와 `CompletionOrderIndependenceTest`를 작성한다.
    - Missing worker/first-winner red를 확인한 뒤 declared batch와 pure round loop를
      구현한다.
11. **Exact reproducibility red**
    - `AlnsReproducibilityTest`를 작성해 trace/solution mismatch를 확인한다.
    - Unordered iteration, shared random/cache/adaptive state와 noncanonical metadata
      원인을 하나씩 제거하는 최소 변경만 한다.
12. **Architecture red/green**
    - `SolverBoundaryArchitectureTest`를 작성한다. Test-only violating fixture를
      architecture-rules가 exact class/package로 찾는 red를 먼저 확인한다.
    - Violation fixture 제거 뒤 target green을 확인한다.
13. **Target green**
    - 각 slice의 exact `-Dtest` 명령이 green이고 대상 Surefire report에 test가 실제
      실행됐는지 확인한다.
14. **Module verify**
    - `rpdptw/solver`와 `-am` 선행 module 전체 `verify`를 실행한다.
15. **Reactor regression**
    - Root `mvn verify`, architecture/forbidden value scan, diff/link/scope check를
      실행한다.
16. **Evidence/handoff**
    - Red/green/regression/fault/reproducibility/diff 산출물을 evidence bundle에
      복사하고 digest를 계산한다. 모두 충족하기 전 progress를 `DONE`으로 바꾸지
      않는다.

각 slice에서 semantic red를 보지 않은 상태로 다음 production behavior를 구현하면
그 slice의 evidence는 무효다. 이미 우연히 behavior가 존재한다면 더 좁은 아직 실패하는
관찰을 먼저 추가하거나 characterization 결과와 이유를 기록한 뒤 change-specific red를
만든다.

## 10. Exact 명령

### 10.1 Working directory와 baseline

```bash
cd /Users/brown/workspace/ro-next
git status --short --untracked-files=all
git branch --show-current
git rev-parse --verify HEAD
sha256sum docs/codex/implementation-plan.md docs/master-design.md docs/architecture-design.md docs/domain-design.md docs/master-design-open-questions.md
mvn -version
find . -name pom.xml -not -path './target/*' -not -path './node_modules/*' -print | sort
rg --files rpdptw build adapters apps legacy 2>/dev/null | sort
find target/codex-evidence/AR-0 target/codex-evidence/AR-1 target/codex-evidence/AR-2 target/codex-evidence/AR-3 -type f -print | sort
```

Source hash가 metadata와 다르면 구현을 시작하지 않는다. Drift diff가 의미/계약을
바꾸지 않는지 review하고, 승인된 새 baseline으로 이 문서 metadata 또는 별도
implementation evidence를 갱신한 뒤 재개한다. 다른 세션의 uncommitted change를
hash를 맞추려고 되돌리면 안 된다.

현재 문서 세션은 shared `target/` 동시 충돌을 피하기 위해 아래 Maven test/verify를
실행하지 않는다. Phase-00의 격리 `test`/`verify` 성공을 legacy baseline으로 사용한다.
후속 AR-4 구현 세션은 Maven test를 다른 writer와 겹치지 않게 격리 또는 직렬 실행하고,
동시 실행에서만 나타난 Shade JAR replace/target rename 실패를 red evidence나 blocker로
기록하지 않는다.

### 10.2 선행 gate와 slice별 red/green

```bash
mvn -pl rpdptw/solver -am verify

mvn -pl rpdptw/solver -am -Dtest=CowCandidateStateTest,CowCandidateStateFaultTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=CacheInvalidationTest,CacheFreeEvaluationEquivalenceTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=PairDestroyRepairTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=AcceptanceAndBestIsolationTest,AdaptiveOperatorSelectorTest,AlnsStepAccountingTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=TerminationSemanticsTest,RunControlFaultTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=SeedLineageTest,RandomStreamTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=PhaseOneScreenTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=PhaseTwoCompleteBatchTest,CompletionOrderIndependenceTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=AlnsReproducibilityTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl rpdptw/solver -am -Dtest=SolverBoundaryArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
```

각 command 뒤 owner module에서 test가 실제 실행됐음을 다음으로 확인한다.

```bash
find rpdptw/solver/target/surefire-reports -type f -maxdepth 1 -print | sort
rg -n 'tests="[1-9]|Failures: [1-9]|Errors: [1-9]' rpdptw/solver/target/surefire-reports
```

`-Dsurefire.failIfNoSpecifiedTests=false`는 `-am` 선행 module에 같은 test name이 없는
경우만 허용한다. `rpdptw/solver` report에 대상 test가 없으면 red/green 모두 무효다.

### 10.3 Target/module/reactor green

```bash
mvn -pl rpdptw/solver -am test
mvn -pl rpdptw/solver -am verify
mvn verify
```

### 10.4 Architecture, 금지 shortcut과 hidden value scan

```bash
rg -n 'com\\.google|software\\.amazon|io\\.aws|jakarta\\.ws\\.rs|org\\.springframework|com\\.ronext\\.rpdptw\\.application|com\\.ronext\\.rpdptw\\.verification' rpdptw/solver/src/main rpdptw/solver/pom.xml
rg -n 'customer(Id|Name)|presetName|switch\\s*\\([^)]*customer|if\\s*\\([^)]*customer' rpdptw/solver/src/main/java
rg -n 'System\\.(nanoTime|currentTimeMillis)|Instant\\.now|LocalDateTime\\.now|ThreadLocalRandom|Math\\.random|SplittableRandom|RandomGeneratorFactory' rpdptw/solver/src/main/java
rg -n 'ApplyUndo|UndoLog|UndoStack|undo\\s*\\(' rpdptw/solver/src/main/java
rg -n '5_?000|parallelRuns|getOrDefault\\s*\\(|orElse\\s*\\([^)]*[0-9]' rpdptw/solver/src/main/java
rg -n 'latest|Latest' rpdptw/solver/src/main/java
test -f rpdptw/core/src/main/java/com/ronext/rpdptw/evaluation/api/StageGuard.java
test -z "$(find rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search -maxdepth 1 -type f -name '*Stage*Guard*.java' -print -quit)"
rg -n 'import com\\.ronext\\.rpdptw\\.evaluation\\.api\\.StageGuard;' rpdptw/solver/src/main/java/com/ronext/rpdptw/solver/search
```

앞의 여섯 `rg`는 production match 0건이어야 한다. Core guard file은 존재하고
solver-local guard file은 없어야 하며, 마지막 import 검색은 acceptance/search owner에서
한 건 이상이어야 한다. Numeric literal 일반 금지가 아니라 legacy
`5000`, fallback/default factory 패턴이 없는지를 확인한다. SHA-256 byte length,
enum ordinal validation 같은 계약 상수는 이 scan의 예외 사유를 evidence에 명시한다.
Test source의 작은 값은 `TEST_ONLY` fixture로만 허용한다.

### 10.5 문서 link/heading, diff와 범위

구현 세션은 이 문서를 소비하기 전에 다음을 실행한다.

```bash
test -f docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
test -f docs/codex/implementation-plan.md
test -f docs/master-design.md
test -f docs/architecture-design.md
test -f docs/domain-design.md
test -f docs/master-design-open-questions.md
rg -n '^## (1\\. 목표와 종료 산출물|2\\. Authority, 결정 상태와 충돌 처리|8\\. 세분화된 test case|9\\. 테스트 우선 실행 순서|11\\. Step-by-step implementation checklist|13\\. Rollback|14\\. DONE / BLOCKED 판정|15\\. 다음 phase handoff|16\\. Scope exclusions와 금지 shortcut)$' docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
git diff --check -- docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
git diff --name-only -- docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
git status --short --untracked-files=all
```

문서가 아직 untracked라 `git diff --check`가 내용을 검사하지 못할 때는 다음을 추가한다.

```bash
git diff --no-index --check /dev/null docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
test "$?" -eq 0 -o "$?" -eq 1
```

AR-4 구현 변경 범위는 §6 표의 POM/source/test와 evidence `target/`뿐이어야 한다.
`git status`의 기존 사용자 변경은 baseline snapshot과 대조하고, AR-4가 새로 바꾼
다른 설계/phase/legacy/GCP/data/progress 파일이 하나라도 있으면 scope failure다.

## 11. Step-by-step implementation checklist

- [ ] Baseline `git status`, branch/HEAD, source hashes와 toolchain을 기록했다.
- [ ] AR-0~AR-3가 각각 `DONE`이고 evidence digest가 있으며 선행 module verify가
      green임을 확인했다.
- [ ] 선행 실제 type과 §6.1 계획상 이름 mapping을 기록했고 의미 충돌이 0건이다.
- [ ] `CowCandidateStateTest`/fault test를 production보다 먼저 만들고 API compile red와
      semantic red를 모두 보존했다.
- [ ] Changed-route first-write copy와 independent bank만 최소 구현했다.
- [ ] Cache invalidation/equivalence test red를 먼저 보고 cache scope/generation을
      구현했다.
- [ ] Pair destroy/repair test red를 먼저 보고 complete pair operator와 common evaluator
      사용만 구현했다.
- [ ] Acceptance/current/best isolation test red를 먼저 보고 AR-2
      `SolveStage.guard()`를 직접 호출한 뒤 acceptance를 수행했다.
- [ ] Solver search package에 guard source 또는 동의어 wrapper를 만들지 않았다.
- [ ] Adaptive exact weight와 interrupted no-update red를 먼저 보고 immutable adaptive
      state를 구현했다.
- [ ] Step boundary/counter red를 먼저 보고 full transition 뒤에만 count하도록 했다.
- [ ] Watchdog/cancel/resource/exception safe-point red를 먼저 보고 candidate 전체
      discard와 exact termination을 구현했다.
- [ ] Seed golden red를 먼저 보고 versioned canonical derivation을 구현했다.
- [ ] Canonical trace에는 semantic completed-step field만 넣고 elapsed/thread/cache
      metadata를 제외했다.
- [ ] Phase-1 일부 완료 red를 먼저 보고 모든 available screen completeness를
      구현했다.
- [ ] Phase-2 missing/unverified worker red를 먼저 보고 complete batch fan-in을
      구현했다.
- [ ] 모든 completion permutation에서 같은 champion을 얻었다.
- [ ] Better/equal/worse round hand case가 각각 next round/
      `NO_STRICT_IMPROVEMENT`로 정확히 분기한다.
- [ ] Fixed envelope를 3회 이상 반복해 canonical trace와 solution fingerprint가 exact
      동일하다.
- [ ] Cache hit/miss가 exact 동일하고 poison은 typed failure이며 best를 바꾸지 않는다.
- [ ] Architecture test가 provider/application/verifier/customer/global clock/apply-undo
      침투를 차단한다.
- [ ] Production에 `Q-BENCH-02` 값/default, Win fixture 변환과 official manifest가 없다.
- [ ] Target test, solver `verify`, root reactor `verify`가 순서대로 green이다.
- [ ] Evidence bundle, rollback 정보와 AR-5/AR-7/AR-10 handoff digest를 만들었다.
- [ ] Diff check와 baseline 대비 changed-file scope check가 통과했다.

## 12. Deliverables와 evidence bundle

### 12.1 Deliverables

| Deliverable | 최소 내용 | Consumer |
|---|---|---|
| `PhaseOneChampion` | All-available screen completeness, initial candidate ID, normal worker result, comparator/tie lineage, candidate fingerprint | AR-5, AR-7 |
| `PhaseTwoRoundResult` | Declared/completed worker set, common warm start, stable champion, previous comparison, round status | AR-7 |
| `WorkerRunResult` | Last committed current/stageBest/solveBest, requested/completed steps, operator/adaptive trace, exact termination/failure | AR-5 recovery gate, AR-7 |
| `CanonicalStepTrace` | Versioned completed-step records와 SHA-256 fingerprint | AR-5 provenance, AR-9 rerun |
| `SeedLineage` | Base/derived seed, derivation version, phase/round/worker/candidate namespace | AR-7/AR-9 |
| COW/cache evidence | Route copy counts, independent bank, invalidation matrix, hit/miss/full equality, fault discard | AR-10 profiling baseline |
| Reproducibility record | Fixed envelope fingerprints, repeated commands/run IDs와 exact comparison | AR-5/AR-7/AR-10 |

### 12.2 Evidence 위치와 필수 파일

실행 산출물은 source control 대상이 아닌 다음 논리 경로를 사용한다.

```text
target/codex-evidence/AR-4/<evidence-id>/
├── evidence.json
├── commands.log
├── red/
├── green/
├── regression/
├── fingerprints/
├── faults/
│   ├── cow-discard-matrix.json
│   ├── termination-matrix.json
│   └── cache-corruption-matrix.json
├── reproducibility/
│   ├── envelope.json
│   ├── run-1/
│   ├── run-2/
│   ├── run-3/
│   └── exact-comparison.json
├── diff/
│   ├── changed-files.txt
│   ├── git-diff-check.txt
│   ├── dependency-scan.txt
│   └── source-hashes.txt
└── handoff.md
```

`evidence.json`은 상위 계획 §11의 공통 field에 더해
`initialCandidateIds`, `availableCandidateCount`, `declaredWorkerIds`,
`requestedStepsByStage`, `completedStepsByStage`, `roundsCompleted`,
`termination`, `traceFingerprint`, `solutionFingerprint`,
`seedDerivationVersion`, `cowStateStrategyVersion`,
`cacheContractVersion`, `redEvidenceIds`를 가진다.

Red report는 test class/method, 예상 실패, 실제 assertion/compile message와 해당 red
commit/tree를 연결한다. Green/regression report는 동일 test가 실제 owner module에서
실행됐다는 Surefire XML digest를 포함한다. Timestamp와 elapsed는 metadata이며 canonical
fingerprint에 넣지 않는다.

## 13. Rollback

Rollback 단위는 AR-4 source/test/POM과 AR-4 artifact version 전체다. 다른 phase,
legacy, 설계, GCP, data와 사용자 미커밋 변경을 rollback 범위에 넣지 않는다.

1. 구현 시작 전에 baseline branch/HEAD, `git status --short --untracked-files=all`,
   AR-3 handoff digest와 AR-3/전체 reactor green command를 evidence에 고정한다.
2. 각 vertical slice는 test와 그 test를 통과시키는 최소 production change를 같은
   reversible change unit으로 유지한다. Rollback은 해당 AR-4 commit의 revert 또는
   검토된 inverse patch를 사용한다. Broad `git reset --hard`, `git checkout -- .`,
   untracked clean과 다른 세션 파일 삭제는 금지한다.
3. `rpdptw/solver/pom.xml`은 AR-4 예상 diff가 0이다. Test-scope 또는 plugin convention이
   없으면 AR-4에서 임시 추가하지 않고 AR-0/AR-3 handoff를 `BLOCKED`로 돌려보낸다.
   별도 승인으로 POM 변경이 생긴 경우에도 해당 승인 단위만 되돌리고 AR-0
   parent/plugin management나 AR-3 dependency를 덮어쓰지 않는다.
4. COW/trace/seed/cache contract는 exact internal version을 가진다. AR-4 artifact를
   소비한 persisted test/evidence가 있다면 reader는 rollback 뒤 unknown/newer version을
   거부하고 재해석하지 않는다. AR-4는 external wire/storage schema와 logical cutover를
   만들지 않으므로 database migration, provider pointer와 production traffic rollback은
   해당 없음이다.
5. In-flight candidate는 durable authority가 아니므로 rollback 시 전부 폐기한다.
   Last committed AR-3 candidate가 안전 경계다. AR-4 phase-1/phase-2 결과는 AR-5
   verification/publication 전에는 외부 정상 result로 복구하지 않는다.
6. `target/codex-evidence/AR-4/<evidence-id>`는 generated evidence다. 다른 세션의
   `target/`을 지우지 말고, rollback record에 invalidated evidence ID를 남긴 뒤 새
   격리 실행 evidence ID를 만든다.
7. Rollback 확인은 AR-4 changed-file 목록이 baseline으로 돌아왔는지, AR-3 targeted
   `verify`와 root reactor `verify`가 격리/직렬 실행에서 green인지, user-owned status가
   baseline snapshot과 같은지를 확인한다.

Rollback 도중 AR-3 API를 바꾸거나 새 schema/version을 만들 필요가 생기면 마지막 안전
tree에서 중단하고 owner phase/ADR 승인을 요청한다.

## 14. DONE / BLOCKED 판정

### 14.1 DONE AND gate

다음을 모두 만족할 때만 `DONE`이다.

1. AR-0~AR-3 `DONE` evidence와 실제 authority artifact를 소비한다.
2. §6의 범위 내 source/test/POM deliverable이 존재하며 다른 owner 파일을 바꾸지 않았다.
3. §8 모든 behavior에 의도한 red와 동일 test의 green evidence가 있다.
4. Request pair/route-bank XOR, current/best immutability와 COW discard fault matrix가
   전부 통과한다.
5. Cache hit/miss/full recomputation exact equality와 poison failure가 통과한다.
6. 모든 available phase-1 screen과 declared phase-2 worker completeness를 요구하고
   partial champion이 0건이다.
7. Exact requested/completed step, round, operator, seed, warm-start와 termination lineage가
   있다.
8. Normal fixed envelope 3회 이상에서 canonical trace와 solution fingerprint가 exact
   동일하다.
9. Exceptional signal이 정상 종료로 alias되지 않고 미완료 mutation/adaptive/count가
   0이다.
10. Targeted test, solver `verify`, root `mvn verify`, architecture/rg/diff scope gate가
    모두 green이다.
11. Provider/customer/verification/application dependency, hidden official value,
    apply/undo, optional variant와 runtime travel fallback이 0건이다.
12. Evidence bundle digest와 재현 가능한 rollback/handoff가 있다.

Source/test/mock/demo 존재, legacy `AlnsBatchEngine` success, seed가 같은 한 번의 run,
일부 worker champion, cache가 정상인 happy path와 module package 성공만으로는
`DONE`이 아니다.

### 14.2 BLOCKED 조건

| 조건 | 판정과 마지막 안전 지점 | 재개 조건 |
|---|---|---|
| AR-0~3 artifact/evidence 없음 | Production/test 변경 전 `BLOCKED` | 선행 phase `DONE` |
| 선행 candidate/profile/comparator API 의미가 이 문서와 충돌 | Mapping 전 마지막 green tree에서 `BLOCKED` | 영향 설계/ADR/phase 문서 동시 승인 |
| COW로 pair atomicity/exception discard를 보장할 수 없음 | Apply/undo로 우회하지 않고 `BLOCKED` | COW defect 해결 또는 별도 승인 roadmap; AR-4에서는 전환 금지 |
| Cache-free full evaluation API가 없음 | Cache 구현 전 `BLOCKED` | AR-2/3 owner가 authority 재평가 API 제공 |
| Official 수치가 구현에 필요하다는 주장 | Generic explicit config까지 진행, official 부분만 `BLOCKED` | `Q-BENCH-02` calibration/approval; AR-9 책임 |
| Provider/product가 필요하다는 주장 | Solver pure runner까지 진행, physical 범위 `BLOCKED` | `Q-INFRA-01` resume evidence와 별도 scope |
| Source baseline 의미 drift | 다른 변경을 되돌리지 않고 `BLOCKED` | Drift review와 새 승인 baseline |
| Fixed envelope normal rerun 불일치 | 마지막 deterministic green slice에서 `BLOCKED`; flaky retry로 숨기지 않음 | Unordered/random/cache/shared-state 원인과 새 red→green evidence |

Shared `target/`에 여러 세션이 동시에 쓰는 동안만 발생한 Maven/JAR replace 오류는 위
표의 어떤 `BLOCKED` 조건에도 해당하지 않는다. 같은 revision을 격리/직렬 실행해
재현했을 때만 build/test defect로 분류한다.

## 15. 다음 phase handoff

### 15.1 AR-5 / RM-5

AR-5에 다음을 전달한다.

- Cache를 제외한 immutable route order, vehicle/terminal binding과 independent bank가
  authority인 `CommittedCandidate`
- Problem/travel/profile/config/build, candidate, trace와 solution fingerprint chain
- Exact termination과 last committed boundary
- AR-5가 search cache/solver summary를 받지 않고 독립 재계산할 수 있는 route/bank
  view
- Exceptional last committed best가 normal result가 아니라 recovery candidate일 뿐이라는
  flag/record

AR-5 entry는 AR-4 `DONE` evidence digest와 candidate authority serialization/encoding
version mapping을 확인해야 한다. AR-4 cache-free equality가 AR-5 candidate verifier
`PASS`를 대신하지 않는다.

### 15.2 AR-7 / RM-6-logical

AR-7에 다음 pure logical contract를 전달한다.

- Phase-1 all-available completeness와 stable champion reduction
- Phase-2 `PhaseTwoBatchPlan`, declared worker identity set, common warm start와 complete
  fan-in 의미
- Worker ordinal/config/seed/termination/result fingerprint
- Completion-order-independent reducer와 better/equal/worse round transition

AR-7은 이를 provider-neutral port, retry/attempt identity, duplicate digest, CAS와 durable
state에 연결한다. AR-4의 in-process runner는 physical dispatcher나 provider 승인 근거가
아니다.

### 15.3 AR-10 / RM-7

AR-10에는 COW strategy version, changed/unchanged route copy count hook, allocation/cache/
full-step work counters와 profiling on/off 전 semantic trace baseline을 넘긴다. 이 hook은
AR-4 correctness를 바꾸지 않는 read-only 관측 seam이어야 한다. AR-10은 COW 유지가
정상 완료이며 apply/undo 구현은 별도 승인 전 금지다.

## 16. Scope exclusions와 금지 shortcut

### 16.1 이 phase가 구현하지 않는 것

- AR-1 input/schema/normalization/travel과 Win fixture 변환
- AR-2 propagation/profile/objective 의미 변경
- AR-2 `evaluation.api.StageGuard`의 solver-local 재정의·wrapper·alias
- AR-3 construction policy, 최대 8개 candidate 생성 규칙과 pair insertion 의미 재구현
- AR-5 candidate verifier, finalization, result-integrity verifier와 publication
- AR-6 application port/local artifact/state/retrieval
- AR-7 durable logical orchestration, retry/attempt, duplicate/CAS와 physical dispatch
- AR-8 legacy adapter/cutover/rollback
- AR-9 official Win manifest, official 수치, baseline/challenger
- AR-10 profiling decision을 앞당긴 state strategy 전환
- Provider SDK, GCP/AWS topology, deployment/IaC
- Optional variant, multi-trip/rotation, route pool/MIP와 dynamic routing
- Public HTTP API, wire DTO, artifact/storage schema 승인

### 16.2 금지 shortcut

- `AlnsBatchEngine`의 `double objective`/iteration 공식을 새 solver에 옮기기
- README/GCP 값이나 임의 숫자를 `screenMaxSteps`, worker 수, phase-2 steps,
  `maxRounds`, watchdog default로 사용하기
- Seed에 ordinal을 더하거나 system clock/global/thread random을 사용하기
- Unordered collection iteration, completion first-winner 또는 cache hit 순서를 tie로
  사용하기
- Node destroy/repair, partial pair, route+bank 중복/누락을 일시 stable state로 노출하기
- Committed current/best를 in-place mutation하거나 reject 뒤 cache/fingerprint를
  “복원”하기
- Cache가 맞다고 가정해 full recomputation test를 생략하기
- Hard violation을 acceptance/temperature/penalty로 허용하기
- Worse accepted current로 stageBest/solveBest를 덮기
- 미완료 step의 adaptive/acceptance/count를 전진시키기
- 일부 phase-1 candidate 또는 일부 phase-2 worker 결과로 champion을 만들기
- Watchdog/cancel/resource/platform/failure를 `MAX_STEPS_REACHED`로 이름 바꾸기
- AR-4 cache-free validation을 독립 verifier라고 부르거나 result를 publish하기
- Apply/undo class, interface, undo log 또는 “향후용” skeleton 만들기
- Win fixture의 소수 `D/U`, provider/product 또는 optional variant를 추측하기
- 다른 세션의 미커밋 파일을 clean/reset/restore하거나 자기 scope에 포함하기

이 문서의 구현 세션은 위 exclusion이 불편하다는 이유로 scope를 넓히지 않는다.
새 authority, 외부 조정 또는 의미 변경이 필요하면 마지막 green boundary에서 중단하고
명시적 승인 뒤 재개한다.
