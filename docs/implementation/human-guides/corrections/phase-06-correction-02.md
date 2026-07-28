# Phase 06 사람용 구현 가이드 수정 보고서 — Round 02

```yaml
document_status: COMPLETE
report_type: HUMAN_IMPLEMENTATION_GUIDE_CORRECTION
correction_round: "02"
phase: "06"
correction_date: 2026-07-29
timezone: Asia/Seoul
correction_snapshot_observed_at: 2026-07-29T02:56:25+09:00
owner_role: RPDPTW 사람용 Phase 06 수정 02 담당자
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
target: docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md
review_recheck: docs/implementation/human-guides/reviews/phase-06-review.md
previous_correction: docs/implementation/human-guides/corrections/phase-06-correction-01.md
authorized_write_scope:
  - docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-06-correction-02.md
prohibited_actions:
  - code_or_pom_or_test_change
  - stage
  - commit
  - push
  - worktree_operation
findings_addressed:
  - HG06-R004
deferred_findings: NONE
target_sha256_before: 719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3
target_sha256_after: df232eebc269601f5a9964a5856a27c731c6fa23229aea5c9b318c899fe6df23
target_git_object_before: 45bee760233551432e9106f6b560bace653b4c8a
target_git_object_after: 39c647b9146eee2c3340ead62f29bcb8835e0d31
review_recheck_sha256: 988dae6f61a07d0b3d3216c3cc402987e850cd166759cebaba795c1aed888598
previous_correction_sha256: cbb22a9cb51401f728c038338089ae075079bc1cf433d4d34f84b22ef0d2b21c
canonical_phase_06_sha256: aec051d91795011c8a589926e14058f8f4943f2cdda873b97e2e4bf613ab6d08
original_phase_06_review_sha256: a33e998bfc9d28215cc2087d289fdb6980298b60d51793a0e817b77febbaf4ab
canonical_master_sha256: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
```

## 1. 수정 범위와 판정

수정한 파일은 이 보고서와
[Phase 06 사람용 구현 가이드](../phases/phase-06-human-implementation-guide.md)
두 개뿐이다. Review recheck, correction 01, canonical Phase 06, original Phase 06
review와 Canonical Master는 읽기 전용으로 유지했다. 코드, POM과 test를
구현하지 않았고 stage, commit, push 또는 worktree 작업을 수행하지 않았다.

Review recheck에서 유일하게 OPEN이던 `HG06-R004`의 closure 미완료를 target
skeletal contract 수준에서 교정했다. 이는 production Java API, concrete
`AtomicReference`/VarHandle/lock primitive 또는 Phase 05/06/07 cross-Phase
contract를 승인한 것이 아니다. Target의 implementation 상태와 entry gate는 계속
`BLOCKED/NOT_STARTED/NOT_ACCEPTED`다.

## 2. 읽은 source와 before/after hash

| Artifact | SHA-256 | 사용/판정 |
|---|---|---|
| Target before | `719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3` | Correction 01 end와 byte equality |
| Target after | `df232eebc269601f5a9964a5856a27c731c6fa23229aea5c9b318c899fe6df23` | 허용된 target 수정 |
| Target Git object before/after | `45bee760233551432e9106f6b560bace653b4c8a` / `39c647b9146eee2c3340ead62f29bcb8835e0d31` | Untracked worktree bytes 비교용; stage 아님 |
| [Human review/recheck](../reviews/phase-06-review.md) | `988dae6f61a07d0b3d3216c3cc402987e850cd166759cebaba795c1aed888598` | Original finding 전체와 correction 01 recheck `HG06-R004 — OPEN` |
| [Correction 01](phase-06-correction-01.md) | `cbb22a9cb51401f728c038338089ae075079bc1cf433d4d34f84b22ef0d2b21c` | 기존 교정 의도와 보존 gate |
| [Canonical Phase 06](../../phases/phase-06-cow-alns-reproducibility.md) | `aec051d91795011c8a589926e14058f8f4943f2cdda873b97e2e4bf613ab6d08` | §7.2, §8.1~§8.2, §8.4, §11, §14~§17 포함 전체 구조 |
| [Original Phase 06 review](../../reviews/phase-06-review.md) | `a33e998bfc9d28215cc2087d289fdb6980298b60d51793a0e817b77febbaf4ab` | `F-P06-006`, `F-P06-010`, `F-P06-011` |
| [Canonical Master](../../../master-design.md) | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | §11.4 completed step와 §13.1 termination/last committed |

## 3. HG06-R004 root cause와 source

- **Recheck root cause:** Executor 선언은 `execute(...)`인데 pseudocode는
  `deriveStep(...)`을 호출했다. `AlnsStepResult.Completed` 선언은
  `(outcome, after, evidence)`였지만 pseudocode가 없는 `unpublished=true`
  component를 만들고, publication 뒤에는 outcome/evidence 없는 같은 constructor를
  재사용했다.
- **Return root cause:** `AlnsEngine.run(...) → AlnsRunOutcome`과 pseudocode의
  `runOneStep` 사이에 exact return type과 termination fold가 없었다.
- **Conflict root cause:** `Conflict(actualIdentity)` 뒤에도 stale expected
  `before`를 `lastCommitted`라고 반환했다. Conflict를 가능한 경쟁으로 볼지
  exclusive-owner invariant defect로 볼지도 API/outcome/test에 닫히지 않았다.
- **Source requirement:** Canonical Master §11.4는 candidate/best/learning/
  acceptance/completed counter가 한 completed step임을, §13.1은 incomplete step
  non-advance와 exact exceptional termination/last committed를 요구한다.
  Canonical Phase 06 §7.2/§8.1~§8.2와 original review
  `F-P06-006/010/011`은 unpublished aggregate, distinct slot handles, final probe와
  single publication을 요구한다.

## 4. Target 교정 anchor

### 4.1 §8.1과 §8.3 — type와 owner 경계

[Target §8.3](../phases/phase-06-human-implementation-guide.md#83-proposed-state-types)에
다음을 반영했다.

- Executor의 유일한 method를
  `AlnsStepExecutor.execute(...) → AlnsStepDerivation`으로 고정했다.
- Unpublished pure result는
  `AlnsStepDerivation.Completed(outcome, unpublishedAfter, evidence)`로 선언했다.
  별도 `unpublished` boolean과 component 생략을 금지했다.
- Published engine step은 별도
  `AlnsEngineStepOutcome.Published(outcome, committed, evidence)`다.
- `AlnsEngine.runOneStep(...) → AlnsEngineStepOutcome`과
  `AlnsEngine.run(...) → AlnsRunOutcome`을 같은 interface에 선언했다.
- `VisibleAlnsState(state, identity, publicationCount)`와
  `AlnsStateSlot.readVisible()`로 세 값을 하나의 atomic visibility snapshot으로
  읽도록 했다.
- `StatePublishResult.Published/Conflict/Failed`가 각각 `visibleAfter` 또는
  `actualVisible`을 명시적으로 반환하게 했다.

### 4.2 §9.1~§9.2 — exact constructor, return과 termination mapping

[Target §9.2](../phases/phase-06-human-implementation-guide.md#92-alns-step-pseudocode)는
선언과 같은 이름/component를 사용한다.

```text
executor.execute(...)
  → AlnsStepDerivation
  → engineOwner.runOneStep(...)
  → AlnsEngineStepOutcome
  → engineOwner.run(...)
  → AlnsRunOutcome
```

Derivation `Completed`는 세 component를 모두 제공한다. Slot `Published` 뒤에만
별도 engine `Published`를 만들며 같은 `Completed`를 published 결과로 재사용하지
않는다. `runOneStep`의 published/exceptional/failed branch를 `run`의
normal/exceptional/failed branch로 fold하는 exact 표와 skeletal pseudocode를
추가했다. Phase 06 worker의 유일한 direct normal termination은 계속
`MAX_STEPS_REACHED`이고 Phase 10 소유 `NO_STRICT_IMPROVEMENT`/
`MAX_ROUNDS_REACHED`를 당기지 않았다.

### 4.3 WP-06.3, test, evidence, DoD와 추적성

[Target WP-06.3](../phases/phase-06-human-implementation-guide.md#wp-063--transactional-completed-step와-best-monotonicity),
§11.2, §12, §13, §14, §16과 §17을 같은 계약으로 갱신했다.

- File/type inventory에 derivation, engine-step, run outcome과 visible-state type을
  연결했다.
- 기존 canonical 89 method 이름은 바꾸거나 추가하지 않고
  `acceptedImprovementPublishesOneImmutableNextState()`의 fixture/pass oracle에
  type 분리와 truthful conflict를 명시했다.
- CP-3와 `E-P06-ALNS`에 declaration↔constructor↔return/termination mapping,
  atomic actual visibility와 publication-count oracle을 추가했다.
- Anti-pattern에 stale expected `before`를 conflict의 actual
  `lastCommitted`라고 보고하는 경우를 추가했다.
- Exit checklist와 `REQ-SINGLE-PUBLISH`가 같은 compile-semantic mapping과
  conflict oracle을 요구하게 했다.

## 5. Declaration ↔ pseudocode consistency 검사

| 선언 | Pseudocode 사용 | 결과 |
|---|---|---|
| `AlnsStepExecutor.execute(...) → AlnsStepDerivation` | `executor.execute(before.state(), config, random, interruption)` | `PASS`; `deriveStep` 0 |
| `AlnsStepDerivation.Completed(outcome, unpublishedAfter, evidence)` | `new AlnsStepDerivation.Completed(outcome, nextState, stepEvidence)` | `PASS`; component 3개 exact, `unpublished=true` 0 |
| `AlnsEngine.runOneStep(...) → AlnsEngineStepOutcome` | 모든 branch가 `Published`, `Exceptional` 또는 `Failed`의 전체 component를 생성 | `PASS`; derivation object 직접 반환 0 |
| `AlnsEngine.run(...) → AlnsRunOutcome` | exact budget→`Normal`, step exceptional→`Exceptional`, step failed→`Failed` | `PASS`; termination rename/cast 0 |
| `AlnsStateSlot.readVisible() → VisibleAlnsState` | Before와 conflict/failure actual을 state/identity/count 한 snapshot으로 소비 | `PASS`; 분리 getter 조합 0 |
| `StatePublishResult.Published(visibleAfter)` | Engine `Published(outcome, visibleAfter, evidence)` | `PASS`; count `before + 1` |
| `StatePublishResult.Conflict(actualVisible)` | Ownership invariant `Failed(..., actualVisible)` | `PASS`; stale `before` 반환 0 |
| `StatePublishResult.Failed(actualVisible, failure)` | Mapped publication `Failed(..., actualVisible)` | `PASS`; cause와 actual component 모두 소비 |

## 6. Conflict와 interruption oracle

Valid composition은 publish-capable slot reference를 한 `AlnsEngine` owner loop에만
준다. Executor/operator/observer에는 publish capability를 노출하지 않는다. 따라서
valid production run의 conflict는 불가능하고, 관찰되면
`PUBLICATION_OWNERSHIP_INVARIANT_VIOLATION`이다. 방어적 `Conflict` variant와 fault
double은 defect를 fail-closed 검증하기 위해 유지한다.

| Boundary/result | Visible/identity/count | Outcome과 retry |
|---|---|---|
| Pre-publication signal | Atomic `before` exact unchanged | Step `Exceptional`; publication 0 |
| `Published(visibleAfter)` | Entire derived state visible, total count `before + 1` | Engine `Published`; retry 0 |
| `Conflict(actualVisible)` | Actual state/identity/count를 atomic snapshot으로 보고; derived state는 unchanged-by-this-call; call-owned count delta 0 | Ownership invariant `Failed(lastCommitted=actualVisible)`; retry 0 |
| `Failed(actualVisible, cause)` | Actual snapshot truthful; derived state unchanged-by-this-call; call-owned count delta 0 | Publication `Failed(lastCommitted=actualVisible)`; retry 0 |
| Post-publication signal | Published step/count를 보존 | 현재 step은 `Published`; 다음 run boundary가 exact `Exceptional`; retry/rollback 0 |

Conflict에서 `actualVisible.publicationCount`는 unauthorized publisher가 owner
invariant를 이미 깼다면 expected `before`와 다를 수 있다. 따라서 “call 전 total
count와 항상 같다”는 거짓 oracle 대신 “이 call이 증가시킨 publication count는
0”과 actual atomic snapshot을 함께 검증한다.

## 7. 보존 gate와 residual risk

| Gate/risk | Correction 02 후 상태 |
|---|---|
| Proposed Java/publication API | `PROPOSED INTERNAL, REVIEW REQUIRED` |
| Concrete atomic/memory primitive | 미선택; Architecture/Solver review gate |
| Publish-capability exposure/composition root | 실제 Java owner/API approval 필요 |
| Phase 05 editor와 Phase 03/04 evaluator seam | Residual cross-Phase blocker 유지 |
| Phase 06→07 schema owner/mapping | `PHASE06_07_HANDOFF_SCHEMA_DECISION — BLOCKED` 유지 |
| Phase 08/09 durable/public result commit | Solver-local publication과 분리 유지 |
| Actual code/test/evidence | `NOT_STARTED` / `NOT_PRODUCED` |
| Official ALNS 수치, Phase 13/14, deferred scope | 기존 OPEN/GATED/deferred 상태 유지 |

## 8. 정적 검증 기록

Maven test는 실행하지 않았다. Phase 06 production/test가 없고 이 correction은
문서-only skeletal contract 교정이며 POM/Java/test 변경이 금지됐다.

| 검사 | 결과 | 판정 기준 |
|---|---|---|
| Target before/after hash | `PASS` | SHA-256/Git object가 metadata와 일치 |
| Declaration↔pseudocode symbols | `PASS` | `execute` 단일 이름, stale `deriveStep`/`AlnsStepResult` 0 |
| Constructor/components | `PASS` | `Completed` 3 component exact, `unpublished=true`와 incomplete reuse 0 |
| Return/termination mapping | `PASS` | Step/run sealed outcome과 mapping table/pseudocode 일치 |
| Conflict oracle | `PASS` | Atomic actual state/identity/count, invariant failure, unchanged-by-this-call/count, retry 0 |
| Canonical method manifest | `PASS` | 이름/count/hash 변경 없음: 89, `1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86` |
| Target/report H1과 heading | `PASS` | H1 각 1, duplicate generated anchor 0 |
| Fence parity | `PASS` | Unclosed fence 0 |
| Local Markdown link와 GFM fragment | `PASS` | Missing file/fragment 0 |
| Trailing whitespace/tab/CRLF | `PASS` | 각각 0 |
| EOF newline | `PASS` | 두 허용 파일 모두 LF |
| Scoped normal/untracked-aware diff | `PASS` | 허용된 target/report만; target before-object diff와 new-file no-index diff 확인 |
| Scoped `git diff --check`/no-index `--check` | `PASS` | Whitespace diagnostic 0 |
| Git mutation | `PASS` | Stage/commit/push/worktree operation 0 |

Shared checkout의 다른 기존 modified/untracked path는 이 correction의 산출물이
아니며 수정하거나 정리하지 않았다. 두 허용 파일은 untracked이므로 normal
tracked diff만으로 누락하지 않고 before Git object와 `/dev/null`을 기준으로
untracked-aware diff/check를 수행했다.

## 9. 최종 판정

`HG06-R004`의 correction 01 recheck root cause는 target의 reviewable skeletal
contract에서 닫혔다. Executor method/signature, unpublished derivation,
published engine step, run outcome/termination, `Completed` constructor와
conflict `lastCommitted`가 한 compile-semantic mapping을 이룬다. Concrete primitive,
actual API implementation, cross-Phase approval와 evidence는 의도대로 gate로
남겼다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG06-R004
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3
TARGET_HASH_AFTER: df232eebc269601f5a9964a5856a27c731c6fa23229aea5c9b318c899fe6df23
