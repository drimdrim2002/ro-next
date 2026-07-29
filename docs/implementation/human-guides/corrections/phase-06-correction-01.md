# Phase 06 사람용 구현 가이드 수정 보고서 — Round 01

```yaml
document_status: COMPLETE
report_type: HUMAN_IMPLEMENTATION_GUIDE_CORRECTION
correction_round: "01"
phase: "06"
correction_date: 2026-07-29
timezone: Asia/Seoul
source_end_snapshot_observed_at: 2026-07-29T02:37:22+09:00
source_end_snapshot_authority: LIVE_OBSERVATION_ONLY_NOT_ACCEPTANCE_EVIDENCE
owner_role: RPDPTW 사람용 Phase 06 수정 01 담당자
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
target: docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md
review: docs/implementation/human-guides/reviews/phase-06-review.md
authorized_write_scope:
  - docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-06-correction-01.md
prohibited_actions:
  - code_or_pom_or_test_change
  - scheduler_readme_or_progress_change
  - stage
  - commit
  - push
  - worktree_operation
findings_addressed:
  - HG06-R001
  - HG06-R002
  - HG06-R003
  - HG06-R004
  - HG06-R005
  - HG06-R006
deferred_findings: NONE
target_sha256_before: fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f
target_sha256_after: 719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3
target_git_object_before: 5fc02ade5df03dd6e697a2a9feb82c1c09f46b43
target_git_object_after: 45bee760233551432e9106f6b560bace653b4c8a
review_sha256_before: 68b46956abddf2acf13e18ef179d59c4370e05b26528a4a1fd062df8c682d832
review_sha256_after: 68b46956abddf2acf13e18ef179d59c4370e05b26528a4a1fd062df8c682d832
```

## 1. 수정 범위와 판정

수정한 파일은 이 보고서와
[Phase 06 사람용 구현 가이드](../phases/phase-06-human-implementation-guide.md)
두 개뿐이다. Scheduler README/progress, original/core 문서, POM, Java와 test는
읽기 전용으로 유지했다. Stage, commit, push와 worktree 작업도 수행하지 않았다.

여섯 finding은 모두 문서 root cause 수준에서 교정했다. 이는 실제 Phase 06
implementation gate를 연 것이 아니다. Source authority, accepted predecessor,
effective POM, central editor/evaluator, publication contract, handoff schema,
explicit config와 scheduler owner receipt는 계속 `BLOCKED` 또는
`REVIEW REQUIRED`다. `OPEN — EXPERIMENT_REQUIRED`, PROPOSED API,
Phase 13 `C-17`과 Phase 14A/14B gate, `Q-VAR-01`과 multi-trip deferred 상태도
보존했다.

## 2. Target, review와 source hash before/after

### 2.1 Target와 review

| Artifact | Initial read | Correction-end snapshot | 결과 |
|---|---|---|---|
| Target SHA-256 | `fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f` | `719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3` | 허용된 target 수정 |
| Target Git object | `5fc02ade5df03dd6e697a2a9feb82c1c09f46b43` | `45bee760233551432e9106f6b560bace653b4c8a` | Worktree bytes 변화, stage 아님 |
| Human review SHA-256 | `68b46956abddf2acf13e18ef179d59c4370e05b26528a4a1fd062df8c682d832` | 동일 | 읽기 전용 |
| Canonical Phase 06 SHA-256 | `aec051d91795011c8a589926e14058f8f4943f2cdda873b97e2e4bf613ab6d08` | 동일 | 읽기 전용 |
| Original Phase 06 review SHA-256 | `a33e998bfc9d28215cc2087d289fdb6980298b60d51793a0e817b77febbaf4ab` | 동일 | 읽기 전용 |

### 2.2 Authority와 implementation source

“동일”은 initial read와 correction-end snapshot의 SHA-256이 같다는 뜻이다.
Git blob은 Target [§3.3](../phases/phase-06-human-implementation-guide.md#33-검증-가능한-source-fingerprint)에
별도로 고정했다.

| Source | Initial SHA-256 | End SHA-256 | 지위 |
|---|---|---|---|
| `docs/README.md` | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | 동일 | Current top-level map |
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 동일 | Canonical Master |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 동일 | Current plain Domain |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 동일 | Current plain Architecture |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 동일 | User-fixed dated input |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 동일 | User-fixed dated input |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 동일 | Canonical five source |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 동일 | Canonical five source |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 동일 | Implementation authority input |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 동일 | Implementation baseline |
| Canonical Phase 05 | `9df624f8ba93d8de69f3f60e8cf3d7f74f7ac0e9c14fc7d99514bf6154595533` | 동일 | Adjacent producer source |
| Canonical Phase 07 | `1b0d7d92d961c44605307a5a66bd659ac54b4cb0832d275aa10c8bfb855de727` | 동일 | Adjacent consumer source |

### 2.3 Shared-worktree live drift

다음 파일은 correction 도중 다른 담당자가 갱신했다. 변경을 덮어쓰거나
acceptance evidence로 사용하지 않았다. Initial hash는 이 correction의 첫
inventory/read snapshot이고 end hash는
`2026-07-29T02:37:22+09:00`에 고정한 비권위 live observation이다. 이후
shared-checkout drift는 이 correction이 반복 추적하지 않으며, 최종 recheck가
안정된 상태를 별도로 검증한다.

| Read-only source | Initial SHA-256 | End snapshot SHA-256 | 해석 |
|---|---|---|---|
| Human Phase 05 guide | `be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d` | `6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c` | 인접 correction drift; 재대조, 비수정 |
| Human Phase 07 guide | `43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d` | `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd` | Handoff schema gate 정합성 재대조, 비수정 |
| Execution progress | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | Scheduler live drift; acceptance로 승격하지 않음 |

Live build input은 initial/end 모두 root POM
`ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`,
solver POM
`ac67af2ac73ab6a5d9f9418f4a5b54ed538c775fbad75a627f348885e3b0ad2e`,
wrapper properties
`7613cd8a2f64216deb6c8f8443f2b339f2305d9b30fc3a4ea3088820a4788871`
이었다. 이는 observation identity이며 accepted effective-POM receipt가 아니다.

## 3. Finding별 교정

### HG06-R001 — Source authority 충돌

- **변경 anchor:** Target
  [§3.1](../phases/phase-06-human-implementation-guide.md#31-충돌-해소-순서),
  [§3.2](../phases/phase-06-human-implementation-guide.md#32-처음-구현하는-사람의-정확한-읽기-순서),
  [§3.3](../phases/phase-06-human-implementation-guide.md#33-검증-가능한-source-fingerprint),
  §6.3~§6.4, §10 WP-06.0, §16.
- **이유/root cause:** 날짜 문서를 “Final”로 복제하면서 Canonical Master와
  `docs/README.md`의 plain current map에 대한 semantic diff와 owner approval를
  생략했다.
- **Source section:** Human review §6 `HG06-R001`; Canonical Master §1.4;
  `docs/README.md` current hierarchy; dated Domain/Architecture metadata;
  Implementation README §3와 Master Realization Plan §2.
- **교정:** Current top-level map, user-fixed dated input, implementation baseline,
  live snapshot을 네 identity layer로 분리했다. Plain 또는 dated pair를 조용히
  고르지 않는 `SOURCE_AUTHORITY_CONFLICT — BLOCKED`와
  `SOURCE_AUTHORITY_DECISION_RECEIPT` schema를 WP-06.0에 연결했다.
- **보존 gate:** Cross-document Master/Domain/Architecture owner approval 전
  production 구현과 accepted evidence 생성 금지. 이 correction은 canonical
  path를 임의 결정하지 않았다.
- **검증:** Plain/dates 양쪽 link와 HEAD blob, semantic/package/test diff 요구,
  traceability row 존재를 정적 대조했다.
- **Residual risk:** Target 밖의 authority map을 실제로 통일하는 owner 변경은
  아직 없다. Receipt 전 source conflict는 계속 blocker다.

### HG06-R002 — Canonical 89-test closure

- **변경 anchor:** Target
  [§11.2](../phases/phase-06-human-implementation-guide.md#112-핵심-test-class와-exact-method-후보),
  [Canonical required-method manifest](../phases/phase-06-human-implementation-guide.md#canonical-required-method-manifest),
  [§11.5](../phases/phase-06-human-implementation-guide.md#115-false-green-방지),
  §12, §14와 §16.
- **이유/root cause:** 교육용 “핵심 후보” 표를 66개로 줄이면서 canonical
  acceptance inventory와 분리하지 않아 누락 자체를 scanner가 찾을 수 없었다.
- **Source section:** Human review §6 `HG06-R002`; Canonical Phase 06 §11.1;
  original review `F-P06-002`, `F-P06-007~011`.
- **교정:** 누락 23개를 fixture/oracle/pass criterion과 함께 원래 class/order에
  복원했다. UTF-8/LF `Class#method()` 89행 manifest를
  `PHASE06-CANONICAL-REQUIRED-METHODS-V1`,
  SHA-256
  `1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86`
  로 고정하고 class→WP→evidence mapping을 추가했다.
- **보존 gate:** 이름은 여전히 future/proposed다. Rename/add/delete는 canonical
  source, manifest version/hash, WP/evidence와 independent review를 함께
  갱신해야 한다.
- **검증:** Target 표를 기계적으로 normalize한 결과 count 89, canonical temp
  manifest와 byte equality, hash equality를 확인했다. Fresh XML의
  missing/extra/duplicate/failed/error/skipped 0과 fault/corruption sensitivity
  receipt를 DoD에 연결했다.
- **Residual risk:** 실제 test source와 XML은 아직 0개다. Production helper로
  oracle expected를 생성하면 manifest 수가 맞아도 evidence는 실패다.

### HG06-R003 — Maven/POM과 selected command closure

- **변경 anchor:** Target §5.3, §6.4, §10의 모든 Future verification,
  [§11.5](../phases/phase-06-human-implementation-guide.md#115-false-green-방지),
  §12와 §14.
- **이유/root cause:** Selected-module filter와
  `failIfNoSpecifiedTests=true`만 강조하고 JUnit/test-fixture classpath,
  reactor dependency resolution, Failsafe lifecycle와 stale XML을 한 실행
  계약으로 닫지 않았다.
- **Source section:** Human review §6 `HG06-R003`; Current Architecture §5~§7;
  dated Architecture §2/§5~§6; Canonical Phase 06 §13.1; live root/solver POM.
- **교정:** `EFFECTIVE_POM_CONTRACT — BLOCKED`를 추가하고 accepted wrapper,
  JUnit, approved test-fixture artifact, pinned Surefire/Failsafe,
  `integration-test`/`verify`, zero-test policy를 receipt에 요구했다. Fresh
  isolated Maven repository에서 wrapper로 upstream을 `-am clean install`한 뒤
  target-only selected Surefire/Failsafe를 실행하고 마지막 root
  `clean verify`를 수행하는 절차를 제시했다.
- **보존 gate:** 현재 POM의 `failIfNoTests=false`, solver test dependency 부재와
  Failsafe 부재를 문서가 해결된 것으로 표시하지 않았다. Accepted effective-POM
  identity 전 test evidence 생성은 금지다.
- **검증:** 모든 selected command에 `clean`, correct target plugin property와
  isolated repo를 연결했다. `-am`은 upstream bootstrap에만 쓰고 selected
  filter는 target-only로 분리했다. Exact 89 XML scanner가 zero-test/stale
  success를 거부하도록 연결했다.
- **Residual risk:** 실제 POM 변경과 Maven 실행은 허용 범위 밖이며 아직
  수행되지 않았다.

### HG06-R004 — Single publication linearization

- **변경 anchor:** Target
  [§8.3](../phases/phase-06-human-implementation-guide.md#83-proposed-state-types),
  §9.1,
  [§9.2](../phases/phase-06-human-implementation-guide.md#92-alns-step-pseudocode),
  §10 WP-06.3, §12와 §14.
- **이유/root cause:** “aggregate 한 번 publish” 문장을 state machine에만
  옮기고 pure return API와 actual state owner/memory boundary를 연결하지 않았다.
  Pseudocode는 정의되지 않은 `accepted`도 사용했다.
- **Source section:** Human review §6 `HG06-R004`; Canonical Master §11.4/§13.1;
  Canonical Phase 06 §7.2/§8.1~§8.2; original review
  `F-P06-006`, `F-P06-010`, `F-P06-011`.
- **교정:** Executor를 unpublished immutable transition derivation으로 한정하고,
  proposed `AlnsEngine`, `AlnsStateSlot`, `StateIdentity`,
  `StatePublishResult`와 `AlnsRunOutcome` skeletal contract를 추가했다. Engine만
  final probe 뒤 `publishOnce(expectedBefore, after)`를 호출한다.
  `Published`만 completed state를 visible하게 만들며 `Conflict`/`Failed`는
  state bytes/identity와 publication count unchanged, no retry, typed failure다.
  Undefined `accepted`는 `decision.isAccepted()`로 교체했다.
- **보존 gate:** Contract는 `PROPOSED INTERNAL, REVIEW REQUIRED`다. Concrete
  `AtomicReference` 선택이나 durable application/storage commit을 선결정하지
  않았고 solver-local publication과 Phase 08/10 commit을 분리했다.
- **검증:** Skeletal signature의 current/identity/count/publish result와
  pseudocode call/return을 교차 대조했다. Published/conflict/failure fault
  double과 pre/post signal을 기존 canonical method matrix에 연결했다.
- **Residual risk:** Java memory primitive와 cross-Phase API review는 아직
  승인되지 않았다.

### HG06-R005 — Snapshot/live inventory

- **변경 anchor:** Target opening metadata/note,
  [§5.1](../phases/phase-06-human-implementation-guide.md#51-재현-가능한-read-only-inventory-명령),
  [§5.2](../phases/phase-06-human-implementation-guide.md#52-존재placeholder부재-구분),
  §5.3, §3.2와 §3.3.
- **이유/root cause:** 단일-module 시절 inventory command와 서로 다른 live
  관찰 시점을 “현재”로 합쳐 wrapper/module/progress 상태를 stale하게 만들었다.
- **Source section:** Human review §3/§6 `HG06-R005`; live POM/wrapper/Java/test
  inventory; execution progress §10.1; Master Realization Plan status/evidence rule.
- **교정:** HEAD baseline, human-review live, correction live와 future
  implementation-entry resnapshot을 분리했다. `rg --files` multi-root 명령으로
  13 POM, wrapper, `rpdptw/build/legacy` Java/test와 non-`package-info` Phase 06
  count를 확인한다. Correction observation에는 23 package-info, 14 test class,
  Phase 06 source/test 0, wrapper present, receipt absent를 기록했다.
- **보존 gate:** Live scaffold/wrapper/evidence 제출을 acceptance로 승격하지
  않았다. Timestamp/hash가 있는 observation일 뿐이며 entry에서 다시 봉인한다.
- **검증:** Correction 중 실제 progress와 인접 guide drift를 다시 감지해 §2.3에
  before/after hash로 기록했다. 두 파일 모두 수정하지 않았다.
- **Residual risk:** Shared working tree는 계속 바뀔 수 있다. 이 보고서의 end
  hash도 timestamped snapshot이며 scheduler authority가 아니다.

### HG06-R006 — Phase 06→07 handoff owner/mapping

- **변경 anchor:** Target §6.3~§6.4, §7.4, §8.1~§8.2, §10 WP-06.0/06.7,
  [§15.2](../phases/phase-06-human-implementation-guide.md#152-phase-07에-넘길-것),
  §15.3, §16과 §17.
- **이유/root cause:** Public/wire schema 최종 승인을 미루는 올바른 제한과 당장
  필요한 internal immutable handoff owner까지 함께 미뤘다.
- **Source section:** Human review §6 `HG06-R006`; canonical Architecture module
  DAG; Canonical Phase 06 §7.1/§16.2; Canonical/Human Phase 07 input와 no-solver
  dependency.
- **교정:** `PHASE06_07_HANDOFF_SCHEMA_DECISION — BLOCKED`를 추가했다. Review가
  core-owned minimal vocabulary 또는 neutral versioned artifact 중 하나를
  명시적으로 승인할 수 있지만 default는 없다. Receipt에 owner/module/package,
  producer/consumer mapper, required/optional/order/encoding, authority/content
  digest, exact termination/last-safe, unknown/missing/version failure,
  compatibility/corruption evidence를 요구했다.
- **보존 gate:** Solver→core only와 verification no-solver dependency,
  declared-claim 비권위성, no verifier/final/publication field를 보존했다.
  Phase 07 proposed type을 Phase 06으로 pull-forward하지 않았다.
- **검증:** Field group별 producer source, consumer use와 broken-handoff 증상을
  표로 대조했다. Correction 중 갱신된 Human Phase 07의 같은 two-option/no-default
  gate와 재대조했다.
- **Residual risk:** 실제 owner/path와 compatibility implementation은 아직
  cross-Phase receipt가 없어 blocker다.

## 4. 보존한 OPEN/GATED/deferred 경계

| 경계 | Correction 후 상태 |
|---|---|
| Source authority | `SOURCE_AUTHORITY_CONFLICT — BLOCKED` |
| Java/publication API | `PROPOSED INTERNAL, REVIEW REQUIRED` |
| Central pair-removal editor | Residual cross-Phase blocker |
| Full evaluator/comparator/tie seam | Residual cross-Phase blocker |
| Official ALNS numeric values | `OPEN — EXPERIMENT_REQUIRED` |
| Handoff schema owner | Cross-Phase contract gate, default 없음 |
| Phase 13 route pool/MIP/OR-Tools | `C-17 GATED TARGET`, Phase 14A receipt 후 별도 승인 |
| Phase 14A/14B | Benchmark acceptance와 production authority 분리, 둘 다 미획득 |
| `Q-VAR-01`, multi-trip/rotation | Deferred 보존 |

## 5. 정적 검증 기록

Maven test는 실행하지 않았다. Phase 06 source/test가 없고 실제 POM/test 수정이
금지됐으며, shared worktree의 다른 작업 산출물을 생성하는 것은 이 correction의
판정에 필요하지 않다.

| 검사 | 결과 | 판정 기준 |
|---|---|---|
| Target exact method manifest | `PASS` | 89행, canonical LF bytes와 equality, SHA-256 `1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86` |
| Target H1/section | `PASS` | H1 1개, §1~§17 존재 |
| Heading duplication | `PASS` | Duplicate generated anchor 0 |
| Fence parity | `PASS` | Unclosed fence 0 |
| Local Markdown links | `PASS` | Missing file 0 |
| GFM/explicit fragments | `PASS` | Missing fragment 0 |
| Trailing whitespace/tab/CRLF | `PASS` | 각각 0 |
| EOF newline | `PASS` | 두 허용 파일 모두 LF |
| Declared target/review/source hash | `PASS` | Correction-end snapshot과 일치 |
| Scoped normal `git diff --check` | `PASS` | Whitespace diagnostic 0 |
| Untracked-aware no-index `--check` | `PASS` | Raw exit 1은 새 파일 차이, whitespace diagnostic 0 |
| Write scope | `PASS` | 이 담당자가 수정한 path는 허용된 두 파일뿐 |
| Git mutation | `PASS` | Stage/commit/push/worktree operation 0 |

## 6. 최종 판정

`HG06-R001~HG06-R006`은 모두 Target correction에 반영됐다. 남은 risk는 finding
미교정이 아니라 source/POM/API/schema/implementation의 명시적 사람 승인과 실제
evidence가 아직 없다는 것이다. 따라서 Target은 corrected implementation guide지만
Phase 06 implementation/acceptance는 계속 `BLOCKED/NOT_STARTED/NOT_ACCEPTED`다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG06-R001, HG06-R002, HG06-R003, HG06-R004, HG06-R005, HG06-R006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f
TARGET_HASH_AFTER: 719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3
