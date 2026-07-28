# Phase 04 사람용 구현 가이드 correction 01

```yaml
correction_status: COMPLETE_SELF_VERIFIED
correction_round: "01"
source_thread_id: 019fa957-eadc-74d1-ae44-6d2957482856
corrected_at: "2026-07-29T02:26:54+09:00"
target: docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md
review: docs/implementation/human-guides/reviews/phase-04-review.md
target_status_after_correction: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
implementation_status_after_correction: NOT_STARTED
phase_acceptance_status_after_correction: NOT_ACCEPTED
evidence_status_after_correction: NOT_PRODUCED
target_sha256_before: 158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010
target_sha256_after: c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c
review_sha256_before: 4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e
review_sha256_after: 4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e
addressed_findings:
  - HG-P04-R01
  - HG-P04-R02
  - HG-P04-R03
  - HG-P04-R04
  - HG-P04-R05
  - HG-P04-R06
  - HG-P04-R07
deferred_findings: []
```

## 1. 결과와 correction 소유권

[Phase 04 사람용 구현 가이드](../phases/phase-04-human-implementation-guide.md)에
[독립 review](../reviews/phase-04-review.md)의 `HG-P04-R01`~`HG-P04-R07`을 모두
반영했다. 이 correction이 바꾼 것은 사람용 실행·판정 지침뿐이다. 실제 구현,
evidence, independent review PASS 또는 acceptance receipt를 만들지 않았고, target의
entry-gate 차단 상태도 해제하지 않았다.

이 작업의 write scope는 다음 두 파일로 제한했다.

1. 수정:
   `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md`
2. 신규:
   `docs/implementation/human-guides/corrections/phase-04-correction-01.md`

그 밖의 문서, 코드, POM, test, scheduler-owned README/progress, canonical Phase 문서와
review 문서는 읽기만 했다. staging, commit, push, branch/worktree 생성도 수행하지
않았다. 공유 checkout에서 관측한 다른 세션의 변경은 보존했으며 accepted
implementation evidence로 승격하지 않았다.

## 2. Authority와 시간축 처리

이번 correction은 source를 하나의 선형 권위 목록으로 합치지 않았다.

| Source class | 이 correction에서의 역할 | 충돌 또는 drift 처리 |
|---|---|---|
| Current top-level map | 현재 `REVIEW` provenance의 domain/architecture map | dated input이나 baseline을 자동 폐기하지 않음 |
| User-fixed dated/design input | 사용자가 고정한 설계 입력과 unresolved question | OPEN/GATED 상태를 임의로 닫지 않음 |
| Implementation baseline | canonical plan, Phase 문서/review, execution 규칙 | actual acceptance와 scheduler 상태의 기준 |
| Live working-tree snapshot | 현재 module/POM/test/status 관측 | 관측일 뿐 accepted evidence가 아님 |
| Historical cross-check | superseded 문맥 확인 | authority로 사용하지 않음 |

서로 다른 시간축의 source가 충돌하면 changed
heading → requirement → WP/test → evidence 영향을 기록하고 해당 owner의 re-baseline
승인을 요구하도록 target을 바꿨다. `PROPOSED API`, `OPEN`, `GATED`,
`EXPERIMENT_REQUIRED`, deferred 항목과 Phase 13 optional/C-17 gate, Phase 14의
official calibration/production authority는 그대로 보존했다.

## 3. 입력과 hash before/after

### 3.1 Target과 review

| Path | Before SHA-256 | After SHA-256 | 판정 |
|---|---|---|---|
| `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md` | `158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010` | `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` | 의도한 target 수정 |
| `docs/implementation/human-guides/reviews/phase-04-review.md` | `4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e` | `4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e` | 읽기 전용, 동일 |

### 3.2 권위 입력과 implementation baseline

`Before`는 correction 시작 시 읽은 working bytes, `After`는 correction 정적 검증
직전 다시 읽은 working bytes다.

| Path | Before SHA-256 | After SHA-256 | 판정 |
|---|---|---|---|
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 동일 |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 동일 |
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 동일 |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 동일 |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 동일 |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 동일 |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 동일 |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 동일 |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 동일 |
| `docs/implementation/phases/phase-04-capabilities-customer-profiles.md` | `2e05e8a08f0e6d7c3757aac80ccb195787b88a76339da943e0873b5010f93e71` | `2e05e8a08f0e6d7c3757aac80ccb195787b88a76339da943e0873b5010f93e71` | 동일 |
| `docs/implementation/reviews/phase-04-review.md` | `7cac66e259050347035249e622b3583faf538e1c3e0297c857ad4b2d693c460f` | `7cac66e259050347035249e622b3583faf538e1c3e0297c857ad4b2d693c460f` | 동일 |
| `docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md` | `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3` | `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3` | 동일 |

### 3.3 작업 중 관측한 live source drift

| Path | Before SHA-256 | After SHA-256 | 처리 |
|---|---|---|---|
| `docs/implementation/execution-progress-and-results.md` | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` | `5696065eb923c50c00e6fa7d5ed2e13c30894e77fcc7a8009fcd36b5d2d8e7cd` | 다른 세션의 Phase 00 Fix 01 진행 기록을 재독해했다. Phase 04 acceptance 근거로 사용하지 않았고 target source baseline도 임의 변경하지 않았다. |
| `docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md` | `be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d` | `87cac9333f646819700713d8b0ecd7198a7044b17a7e3cc23ad81d940bf0f1d9` | 다른 세션의 correction 결과를 전체 재독해했다. Phase 05도 Phase 04 accepted handoff 없이 BLOCKED라는 경계와 모순이 없음을 확인했다. |

위 두 drift는 이 correction이 만든 변경이 아니다. 공유 checkout의 live drift가
snapshot 이후에도 다시 발생할 수 있다는 `HG-P04-R06` residual risk의 실제
사례로만 기록한다.

### 3.4 Repository/Maven inventory snapshot

읽기 전용으로 root와 `rpdptw` reactor의 POM/module/test wiring을 확인했다.
관측 시점에 `rpdptw-core`, `rpdptw-capabilities`,
`rpdptw-profile-catalog`은 sibling이고, 두 leaf는 core에 의존하지만 서로에게
production 의존하지 않았다. `build/profile-validation`과 승인된 Phase 04
cross-module composition test는 없었다. 병렬 Phase 00의 uncommitted
module/package/test skeleton은 존재했지만 accepted receipt가 없으므로 target은 이를
`current observation`, `NOT_PRODUCED`로만 기록한다.

## 4. Finding별 correction

### HG-P04-R01 — Authorization pipeline과 failure precedence

- 변경 anchor:
  [§9.7](../phases/phase-04-human-implementation-guide.md#97-state-transition과-실패-precedence),
  [WP-04.1](../phases/phase-04-human-implementation-guide.md#wp-041--core-owned-selection-contract-exact-catalog와-two-stage-authorization),
  [§11.3](../phases/phase-04-human-implementation-guide.md#113-exact-test-class와-method-후보),
  [§12.4](../phases/phase-04-human-implementation-guide.md#124-safe-observability).
- 변경 이유/root cause: two-stage authorization을 pseudocode에만 반영하고 기존
  validation ordinal을 그대로 둬 unauthorized 요청이 catalog not-found/corruption과
  경쟁하도록 만든 모순을 제거했다.
- source 근거/section:
  `docs/implementation/reviews/phase-04-review.md` F-P04-004,
  canonical Phase 04 §8.1/§9.3, integrated design §20, human review
  `HG-P04-R01`.
- 교정 내용: cross-stage 순서를
  `syntax/exact-ref reject without lookup → requested-scope pre-read auth →
  tenant lookup/integrity/preset → verified descriptor+preset auth →
  registry/binder`로 고정했다. Pre-read denial은 read count 0이며
  not-found/corruption보다 먼저이고, post-verification authorization은 별도
  ordinal이다.
- 보존한 gate/blocker: 실제 backend timing/redaction과 Phase 08 outward API mapping은
  별도 security evidence가 필요하다.
- 검증/결과: multi-defect test matrix, zero-read assertion, exact ordinal과 safe
  observability 요구가 서로 같은 pipeline을 가리키는지 정적 대조했다. **PASS**.
- residual risk: provider/backend 수준 side channel은 Phase 04 generic contract만으로
  제거됐다고 볼 수 없다.

### HG-P04-R02 — Descriptor semantic order

- 변경 anchor:
  [§8.2](../phases/phase-04-human-implementation-guide.md#82-단계-b--작은-탐색과-test-only-실습),
  [§9.6](../phases/phase-04-human-implementation-guide.md#96-catalog-resolution과-pure-binder를-분리하는-pseudocode),
  [§11.3](../phases/phase-04-human-implementation-guide.md#113-exact-test-class와-method-후보).
- 변경 이유/root cause: keyed map, unordered declaration, dependency DAG,
  business-semantic objective/stage/tie sequence를 모두 “descriptor order”라는 한
  표현으로 합친 것이 원인이었다.
- source 근거/section: master design §9.1/§9.3, integrated design §8.3~§8.5,
  dated domain design §10, human review `HG-P04-R02`.
- 교정 내용: keyed map과 unordered set은 canonicalize하고 DAG는 stable
  topological order를 사용하되, objective dimension, SolvePlan stage, 승인된 tie
  sequence는 exact order를 fingerprint에 포함하도록 field-classification 표와
  same/different test를 추가했다. 미분류 field는 fail-closed다.
- 보존한 gate/blocker: exact wire schema와 canonical encoding은 `ADR-003` 승인 전
  `OPEN/PROPOSED`다.
- 검증/결과: map/set permutation은 same, objective/stage/tie swap은 different라는
  요구가 pseudocode와 test matrix 양쪽에 존재하는지 확인했다. **PASS**.
- residual risk: 승인 전 field-name/type을 public wire contract로 고정할 수 없다.

### HG-P04-R03 — Core-owned contract와 WP module DAG

- 변경 anchor:
  [§6.3](../phases/phase-04-human-implementation-guide.md#63-proposed-target-file-tree),
  [§9.1](../phases/phase-04-human-implementation-guide.md#91-package와-dependency-방향),
  [WP-04.1](../phases/phase-04-human-implementation-guide.md#wp-041--core-owned-selection-contract-exact-catalog와-two-stage-authorization).
- 변경 이유/root cause: 교육 순서는 core-first였지만 WP-04.1의 exact change target은
  catalog만 가리키고 core extension을 WP-04.2로 늦춘 불일치를 제거했다.
- source 근거/section: integrated design §3.4~§3.5, dated architecture design
  §2.2~§2.6, canonical Phase 04 §6/§7.5, live leaf POM dependency.
- 교정 내용: WP-04.1을 core-owned identity/selection/failure/binding skeletal
  contract와 core contract test를 먼저 만드는 substep, 그 API를
  profile-catalog가 구현하는 substep으로 닫았다. core→catalog edge와 duplicate
  authority type을 금지했다.
- 보존한 gate/blocker: exact Java 이름/signature는 `ADR-003` 및 Phase 03/04 owner
  review 전 `PROPOSED API`다. 제시 코드는 skeletal contract다.
- 검증/결과: `core → no catalog`, `catalog → core`, downstream core-only라는
  production DAG와 WP 순서를 정적 대조했다. **PASS**.
- residual risk: API owner 승인 전에는 실제 type 생성에 진입할 수 없다.

### HG-P04-R04 — Maven reactor freshness

- 변경 anchor:
  [WP-04.1](../phases/phase-04-human-implementation-guide.md#wp-041--core-owned-selection-contract-exact-catalog와-two-stage-authorization),
  [§11.6](../phases/phase-04-human-implementation-guide.md#116-false-green-방지와-maven-pass-판정),
  [§12.2](../phases/phase-04-human-implementation-guide.md#122-evidence-bundle-최소-내용).
- 변경 이유/root cause: upstream의 nonmatching `-Dtest` 문제를 피하려 leaf `-f`
  command를 택하면서 current sibling core materialization과 isolated local-repository
  provenance를 잃었다.
- source 근거/section: live `rpdptw/pom.xml`, core/capabilities/profile-catalog POM,
  integrated design §3.3, master realization plan §8~§9, human review
  `HG-P04-R04`.
- 교정 내용: Phase 00 승인 정책을 전제로 isolated Maven repository에서 current
  source graph를 `-am clean install`로 materialize하고, 같은 repository/source로
  target leaf `clean test`, 이어서 root `clean verify`를 수행하도록 바꿨다.
  exact method manifest, source commit, selected graph, repo path/digest와 fresh report
  timestamp를 evidence에 요구했다.
- 보존한 gate/blocker: Phase 00 independent review/receipt 전에는 최종
  wrapper/plugin command가 authority가 아니며, 승인 결과에 맞춰 치환해야 한다.
- 검증/결과: `-am`, isolated repository, clean leaf, root verify, zero/missing/skipped
  test, stale report/local snapshot 거부 조건을 정적 검사했다. **PASS**.
- residual risk: Phase 00가 확정할 module-specific Surefire 정책에 따라 exact
  selector 형태는 바뀔 수 있다.

### HG-P04-R05 — Cross-module composition과 downstream compile probe

- 변경 anchor:
  [§6.3](../phases/phase-04-human-implementation-guide.md#63-proposed-target-file-tree),
  [WP-04.5](../phases/phase-04-human-implementation-guide.md#wp-045--phase-03-equivalence와-downstream-contract),
  [§11.5](../phases/phase-04-human-implementation-guide.md#115-test-category별-적용성).
- 변경 이유/root cause: sibling production edge 금지와 실제 두 sibling을 한 test
  process에서 조립할 owner 필요를 구분하지 않고 full reactor build를 integration
  evidence처럼 취급했다.
- source 근거/section: integrated design §3.2/§3.5, dated architecture design
  §2.2/§2.5~§2.7, master realization plan §8.2, live inventory, human review
  `HG-P04-R05`.
- 교정 내용: Phase 00/Architecture 승인 뒤의 proposed test-only
  `build/profile-validation` owner에 actual provider registry → catalog binder →
  core `BoundProfile` composition과 restricted classpath의 Phase 05 core-only compile
  probe를 배치했다. production reverse edge 0도 함께 증명한다.
- 보존한 gate/blocker: module이 absent/unapproved이면 evidence는
  `NOT_PRODUCED`이고 sibling build green으로 대체할 수 없다.
- 검증/결과: file owner, test scope, exact composition 대상, restricted downstream
  probe, architecture edge check가 모두 WP/evidence/checklist에 연결되는지
  확인했다. **PASS**.
- residual risk: `build/profile-validation` 자체의 승인은 아직 없으며 Phase 00
  architecture decision이 다른 test-only owner를 선택할 수 있다.

### HG-P04-R06 — Fingerprint drift fail-closed

- 변경 anchor:
  [§4.1](../phases/phase-04-human-implementation-guide.md#41-충돌-해소-순서),
  [§4.2](../phases/phase-04-human-implementation-guide.md#42-검증-가능한-source-fingerprint),
  [WP-04.0](../phases/phase-04-human-implementation-guide.md#wp-040--entry-authority와-cross-phase-contract-freeze).
- 변경 이유/root cause: immutable expected `HEAD` blob과 mutable live working bytes를
  한 절차로 취급하고 일부 hash를 사람이 눈으로 비교한다고 가정했다.
- source 근거/section: master realization plan §9/§12.1,
  execution progress live status, human review `HG-P04-R06`.
- 교정 내용: current maps를 포함한 모든 expected source blob을 machine-readable
  TSV로 비교하고 missing/mismatch면 non-zero로 실패하도록 했다. 별도 exact-path
  loop가 status, cached/unstaged diff-name, live Git object와 SHA-256을 기록한다.
  implementation start와 pre-review seal 직전에 모두 재검사하고, drift는 impact
  review + owner re-baseline 없이는 진행하지 못한다.
- 보존한 gate/blocker: live bytes는 현재 관측일 뿐 authority/accepted evidence가
  아니다. 공유 checkout drift가 발생했다고 임의로 official baseline을 갱신하지
  않는다.
- 검증/결과: 작업 중 progress와 Phase 05 guide의 실제 concurrent drift를 탐지하고
  재독해했으며 §3.3에 분리 기록했다. target의 expected manifest와 live loop가
  서로 다른 역할을 갖는지 확인했다. **PASS**.
- residual risk: snapshot 직후의 TOCTOU drift는 없어지지 않으므로 두 시점 재검사가
  필수다.

### HG-P04-R07 — WP-04.6 acceptance/exit blocker branch

- 변경 anchor:
  [WP-04.6](../phases/phase-04-human-implementation-guide.md#wp-046--architecture-evidence-independent-review와-acceptance-handoff),
  [§12.3](../phases/phase-04-human-implementation-guide.md#123-단방향-evidence-dag),
  [§14.6](../phases/phase-04-human-implementation-guide.md#146-evidence와-acceptance),
  [§14.7](../phases/phase-04-human-implementation-guide.md#147-definition-of-done).
- 변경 이유/root cause: blocker가 있어도 가능한 evidence seal/review 준비와 blocker
  해제 뒤에만 가능한 receipt/accepted handoff를 한 expected path로 축약했다.
- source 근거/section: canonical Phase 04 review §6.2, master realization plan
  §9.2~§9.3/§10~§11, canonical Phase 04 §12.1/§13, human review
  `HG-P04-R07`.
- 교정 내용: Branch A는 unresolved blocker이면 immutable manifest와 independent
  review `CHANGES_REQUIRED/BLOCKED`까지만 허용하고 receipt/accepted handoff를
  금지한다. Branch B는 모든 exit blocker 해제와 test/evidence 완성 뒤
  review PASS → receipt `(M,R)` → accepted handoff만 허용한다.
- 보존한 gate/blocker: full-solution evaluator/equality/portfolio의 세 cross-Phase
  contract가 실제로 해결되지는 않았다. 현재는 Branch A다.
- 검증/결과: WP, evidence DAG, exit checklist, handoff envelope에 동일한 두 branch와
  receipt 금지 조건이 있는지 대조했다. **PASS**.
- residual risk: blocker owner/API approval 없이는 Branch B로 전이할 수 없다.

## 5. 보존한 설계 경계와 마지막 안전 지점

이 correction은 review finding을 닫았지만 다음 결정을 숨은 기본값으로 닫지 않았다.

- Capability/Profile Java 이름, signature, schema와 canonical wire encoding은
  `PROPOSED API`이며 owner/ADR 승인 전 skeletal contract다.
- Q-CAP-001~003과 관련 policy/facet 조합, precomputed extension,
  state-reset mechanism은 `OPEN/GATED/EXPERIMENT_REQUIRED/deferred` 상태를 유지한다.
- Phase 13 exact optimization은 optional이고 C-17 gate 통과 전 mandatory가 아니다.
- Phase 14만 official benchmark calibration과 production threshold authority를
  가진다. Phase 04 실험값은 official threshold가 아니다.
- Phase 00 receipt, Phase 03 evaluator/equality artifact, actual test-only composition
  owner, independent review PASS와 acceptance receipt가 없으면 구현 또는 accepted
  handoff로 진행하지 않는다.

따라서 사람이 멈춰야 할 마지막 안전 지점은 승인되지 않은 contract를 production
type/POM/dependency로 쓰기 직전이다. 그 시점에는 `BLOCKED` 또는
`EXPERIMENT_REQUIRED`를 기록하고 owner decision을 받아야 한다.

## 6. 정적 검증

검증 scope는 target과 이 correction report 두 파일뿐이다. 다른 dirty path의
상태나 diff는 성공 조건으로 소비하지 않았다.

| 검사 | 명령/방법 | 결과 |
|---|---|---|
| Target relative link | report의 target/review 상대 경로를 실제 파일로 resolve | PASS |
| GFM anchor | fenced code를 제외하고 GitHub-style heading slug를 생성해 두 파일의 local fragment 대조 | PASS |
| Heading | fence 밖 heading hierarchy, H1 개수, exact duplicate 확인 | PASS |
| Fence | 두 파일의 backtick/tilde fence open/close 짝 확인 | PASS |
| Whitespace | trailing whitespace와 CRLF 확인 | PASS |
| EOF | 두 파일 모두 정확히 하나의 final LF 확인 | PASS |
| Target hash | `shasum -a 256` 재실행 | PASS — `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` |
| Review immutability | review SHA-256 재실행 | PASS — `4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e` |
| Scoped diff check | `git diff --check -- <target> <report>` | PASS |
| Untracked-aware diff check | 각 파일을 `/dev/null`과 `git diff --no-index --check` | PASS — diff 존재를 뜻하는 expected exit 1, whitespace diagnostic 0 |

두 파일이 현재 untracked이므로 일반 `git diff --check`만으로는 content를 검사하지
못한다. 따라서 같은 두 파일에 한정한 `--no-index --check`를 함께 사용했다. 이는
staging을 의미하지 않는다. 이 mode의 exit 1은 `/dev/null`과 파일이 다르다는
뜻이고, 실제 check failure 여부는 whitespace diagnostic이 0인지로 판정했다.

## 7. Residual risk와 최종 판정

`HG-P04-R01`~`HG-P04-R07`은 사람용 guide 수준에서 모두 교정됐고 deferred finding은
없다. 그러나 guide의 `BLOCKED` 상태는 의도적으로 유지했다. 남은 위험은
documentation finding이 아니라 실제 owner approval, Phase 00/03 accepted producer,
test-only composition owner, security backend evidence, independent review와 receipt가
아직 없다는 implementation/acceptance blocker다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG-P04-R01, HG-P04-R02, HG-P04-R03, HG-P04-R04, HG-P04-R05, HG-P04-R06, HG-P04-R07
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010
TARGET_HASH_AFTER: c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c
