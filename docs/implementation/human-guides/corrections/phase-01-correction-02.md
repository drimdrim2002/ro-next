# Phase 01 사람용 구현 가이드 correction 02

```yaml
phase: "01"
correction_round: "02"
correction_kind: INDEPENDENT_HUMAN_GUIDE_CORRECTION
correction_session_relation: CORRECTION_01_AND_ORIGINAL_REVIEWER_RECHECK과_분리된_담당
target: docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md
finding_input: docs/implementation/human-guides/reviews/phase-01-review.md
previous_correction: docs/implementation/human-guides/corrections/phase-01-correction-01.md
correction_date: 2026-07-29
inventory_branch: codex-implementation
inventory_head: 7cc890ee1d0805df5ae14b633127fade4f978639
target_git_state_before: UNTRACKED
target_git_state_after: UNTRACKED
review_modified: false
previous_correction_modified: false
implementation_or_build_modified: false
maven_or_test_executed: false
git_stage_commit_push_worktree_performed: false
correction_verdict: HG_P01_005_AND_HG_P01_006_ADDRESSED_PENDING_INDEPENDENT_RE_REVIEW
```

## 1. 범위와 결과

수정한 파일은 [target guide](../phases/phase-01-human-implementation-guide.md)와 이
Correction 02 report뿐이다. [원 review와 Correction 01 재검증](../reviews/phase-01-review.md),
[Correction 01](phase-01-correction-01.md), scheduler/progress, canonical/original
implementation 문서, Java/POM/test/build와 Git state는 읽기 전용으로 유지했다.

재검증에서 `OPEN`인 `HG-P01-005`, `HG-P01-006`만 교정했다. Phase 01은 계속
`NOT_STARTED / NOT_ACCEPTED`이고 entry는
`BLOCKED_BY_PHASE_00_ACCEPTED_EVIDENCE`다. Catalog에 적은 module/FQCN/method도 전부
`PROPOSED`이며 acceptance 또는 public/stable default가 아니다.

## 2. Hash와 authority baseline

### 2.1 Correction 시작 전 고정값

| Artifact | SHA-256 before | Git blob | 역할/변경 |
|---|---|---|---|
| [Target](../phases/phase-01-human-implementation-guide.md) | `142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e` | `f2be351463309b97ffa35eb34367404b342013ef` | Correction 02의 유일한 기존 수정 대상 |
| [Correction 01](phase-01-correction-01.md) | `16cb6c7caa246a93a301cdbc74e796ed4122e92e24cd83a7f98217f398688792` | `e2b589e2782190a0eba52948f871094e2ee1f150` | Read-only correction history |
| [원 review + 재검증](../reviews/phase-01-review.md) | `5125ef9430f685686d4eba0656276e9198fc9f3a6fe430bb05215e92f1c2ae0a` | `01ba437b512723955ad5562df81b62ed513e0e79` | OPEN finding/root cause/required correction authority |

Before 값은 correction 작업 전에 직접 계산했다. Target before blob은 local object
database에도 존재해 after bytes와 no-index scoped diff를 다시 대조할 수 있었다.

### 2.2 Target before/after

| Artifact | SHA-256 before | SHA-256 after | 변경 |
|---|---|---|---|
| [Target](../phases/phase-01-human-implementation-guide.md) | `142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e` | `0afb6c603cb3f9ef3c507ec8b7ba6f76566d926e25ff2332ce178058ef86dee2` | `HG-P01-005/006` only |
| [Correction 01](phase-01-correction-01.md) | `16cb6c7caa246a93a301cdbc74e796ed4122e92e24cd83a7f98217f398688792` | same | 없음 |
| [원 review + 재검증](../reviews/phase-01-review.md) | `5125ef9430f685686d4eba0656276e9198fc9f3a6fe430bb05215e92f1c2ae0a` | same | 없음 |

## 3. Finding별 correction

### HG-P01-005 — Exit-required catalog와 fail-closed exact-set gate

**Target anchor**

- [WP-01.7](../phases/phase-01-human-implementation-guide.md#98-wp-017--integration-evidence-seal-independent-review와-phase-02-handoff)의 catalog/manifest/verifier/exit contract
- [§11.3 candidate와 유일 catalog](../phases/phase-01-human-implementation-guide.md#113-test-class와-method-후보)
- [§11.6 pass 판정](../phases/phase-01-human-implementation-guide.md#116-명령과-pass-판정)
- [§12.4 evidence](../phases/phase-01-human-implementation-guide.md#124-checkpoint-d--evidence-seal과-acceptance),
  [§14.5 exit checklist](../phases/phase-01-human-implementation-guide.md#145-testevidencereview)

**Root cause**

Correction 01은 report loop를 fail-fast로 만들었지만 required test inventory를
“class당 최소 한 method”로 축소했다. §9/§11에서 요구한 나머지 method가 manifest에서
빠져도 이를 판단할 별도 source-of-truth와 catalog↔manifest completeness oracle이
없었다. 설명은 Java identifier grammar와 duplicate rejection을 약속했지만 shell은
non-empty만 검사했다.

**Correction**

1. §11.3 표를 명시적으로 `CANDIDATE`로 낮추고, fence content
   `phase01-exit-required-tests-v1`만 유일한 exit-required catalog specification으로
   정했다.
2. Catalog는 51 unique `engine|module|fqcn|method` row / 13 class이며 모든 row가
   `PROPOSED — ENTRY/ARCHITECTURE/TEST IDENTITY APPROVAL REQUIRED`다. 승인된
   module/FQCN이 없는 현재 상태에서 이 block을 executable acceptance catalog로
   가장하거나 manifest를 봉인할 수 없다.
3. 승인 뒤 manifest는 catalog의 파생 set이고
   `catalog − manifest = ∅`, `manifest − catalog = ∅`를 모두 만족해야 한다.
   Missing class 전체, missing method 한 row, extra row와 wrong engine은 모두 gate를
   닫는다. 같은 class의 대표 method는 다른 required method를 대체하지 않는다.
4. Versioned verifier가 engine/module/FQCN/method grammar, exact/engine duplicate,
   unknown/wrong engine, fresh exact class XML, suite count, failure/error/skipped와
   base method record를 검사한다. Parameterized display는 base method 뒤의 `(` 또는
   `[` suffix만 허용한다.
5. `clean verify` 직전 external run marker보다 XML이 새로워야 하고 서로 다른 run의
   report를 합칠 수 없다.
6. Positive `0`과 negative `64..75`의 고정 exit contract를 만들고 malformed와
   omitted row를 별도 control로 분리했다.

**Source**

- [원 review 재검증 `HG-P01-005`](../reviews/phase-01-review.md#hg-p01-005--open)의
  finding/root cause/required correction
- [원 `HG-P01-005`](../reviews/phase-01-review.md#hg-p01-005--owner-surefire-report-확인-예시가-shell-전체-exit-code에서-false-green-가능)
- [Canonical Phase 01 review `F-P01-003`](../../reviews/phase-01-review.md)
- [Canonical Phase 01](../../phases/phase-01-canonical-input-normalization.md) §9.2/§9.4
- [Master Realization Plan](../../master-realization-plan.md) §8~§9

**Gate 보존**

Phase 00 receipt, adapter placement, engine/plugin, exact module/FQCN/method와 catalog
storage path가 승인되기 전에는 catalog/manifest/verifier evidence가 green일 수 없다.
Failsafe는 approved `integration-test`+`verify` execution이 생기기 전 계속 사용할 수
없다. Public schema, OPEN/GATED/DEFERRED, Phase 13 `C-17`과 Phase 14 authority는
변경하지 않았다.

**Verification**

- Catalog: `51` unique row, `13` class, duplicate `0`, unknown engine `0`,
  grammar defect `0`
- Catalog fence bytes SHA-256:
  `471183deffe508e429ffddf8c4c825bb5c25d4d7b103f4f8935f5e48122859b2`
- Verifier Python syntax: `PASS`
- Isolated positive/negative execution:
  `positive=0`, `unknown=64`, malformed 3/5 field·empty·module·FQCN·method `=65`,
  duplicate `=66`, omitted class/method와 extra `=67`, wrong engine `=68`,
  missing report/wrong classname `=69`, zero `=70`, failed `=71`, error `=72`,
  skipped `=73`, missing method `=74`, stale `=75`
- Static coverage: §9.8 exit table, executable verifier constants/branches,
  §11.6 pass gate, §12.4 evidence와 §14.5 checklist가 같은 vector와 artifact
  관계를 사용한다.

**Residual risk**

Approved module/FQCN/method, Failsafe 여부, parameterized report identity와 XML
contract가 바뀌면 catalog와 verifier를 같은 reviewed change에서 version-up하고
negative characterization을 다시 봉인해야 한다. 현재 proposed names는 구현 승인이나
stable API가 아니다.

### HG-P01-006 — 과거 §6 fragment compatibility

**Target anchor**

- [과거 `61-2026-07-29-실제-상태` compatibility
  anchor](../phases/phase-01-human-implementation-guide.md#61-2026-07-29-실제-상태)는
  현재 [§6.2 live snapshot](../phases/phase-01-human-implementation-guide.md#62-correction-live-snapshot--미커밋미승인-관찰) 바로 앞
- [과거 `62-목표-상태` compatibility
  anchor](../phases/phase-01-human-implementation-guide.md#62-목표-상태)는 현재
  [§6.3 목표 상태](../phases/phase-01-human-implementation-guide.md#63-목표-상태) 바로 앞

**Root cause**

Correction 01이 §6을 HEAD baseline/live/target 세 절로 분리하면서 이미 봉인된 원
review의 두 GFM slug를 external compatibility surface로 보존하지 않았다.

**Correction**

현재 heading과 의미를 바꾸지 않고 explicit `<a id>` 두 개를 과거 link가 의도한 새
위치에 추가했다. `61-...실제-상태`를 historical HEAD §6.1이 아니라 실제 live
snapshot §6.2에, `62-목표-상태`를 목표 §6.3에 연결했다. 원 review나 Correction 01의
link를 다시 쓰지 않았다.

**Source**

- [원 review 재검증 `HG-P01-006`](../reviews/phase-01-review.md#hg-p01-006--correction-01-heading-변경으로-원-review의-gfm-fragment-2개가-깨짐)
- 원 review `HG-P01-001/002`의 봉인된 target fragment

**Gate 보존**

Compatibility anchor는 navigation만 복구한다. HEAD baseline, timestamped unapproved
live snapshot과 target state의 authority/의미를 합치지 않고 Phase status나 gate를
바꾸지 않는다.

**Verification**

Target, Correction 01과 원 review를 함께 resolve한 결과 local target missing `0`,
GFM/explicit fragment missing `0`이다. 두 과거 fragment는 각각 정확히 한 번
존재하고 의도한 현재 heading 바로 앞에 있다.

**Residual risk**

향후 heading 재구성도 봉인된 review/correction link를 깨뜨릴 수 있다. 이후 correction은
target만 검사하지 말고 review와 모든 correction report의 cross-document fragment
set도 계속 검사해야 한다.

## 4. 보존한 authority, gate와 last safe point

| Gate/blocker | Correction 02 후 상태 | Last safe point |
|---|---|---|
| Phase 00 accepted evidence/receipt | `BLOCKED` | Guide, proposed catalog와 approval request |
| Adapter module placement/POM/DAG | `OPEN — ARCHITECTURE APPROVAL REQUIRED` | Accepted core-only handoff |
| Exit catalog module/FQCN/method | `PROPOSED — APPROVAL REQUIRED` | 51-row reviewed specification only |
| Failsafe execution/report identity | `NOT APPROVED` | Surefire proposal; IT 주장 금지 |
| Public wire schema/version/alias/unknown | `OPEN` | Internal candidate/test fixture |
| Great Circle/coordinate/source policy | Phase 02 approval blocker | Typed source/absence handoff only |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Phase 01 unaffected |
| Phase 13 `C-17` | `GATED` | ALNS-only path |
| `Q-VAR-01`, multi-trip | `DEFERRED` | Current pair/single-trip contract |
| Phase 14B production authority | `NOT_GRANTED / GATED` | Local/evidence path only |

Correction은 새 semantic default, runtime authority, test engine approval나 Phase
acceptance를 만들지 않았다.

## 5. 정적 검증과 scoped diff

### 5.1 Link, structure와 document hygiene

| 검사 | 결과 |
|---|---|
| Target + Correction 01 + 원 review local links/GFM | `127` links, `64` fragments; missing target/fragment `0/0` |
| Correction 02 local links/GFM | `24` links, `12` fragments; missing target/fragment `0/0` |
| Target compatibility anchors | 두 ID 각각 1개, intended §6.2/§6.3 바로 앞 |
| Target heading 구조 | `PASS`; H1 1개, canonical §1~§17 유지 |
| Target fenced code block | `PASS`; balanced |
| Target trailing whitespace/conflict marker/EOF | `PASS`; `0/0/final LF` |
| Correction 02 heading/fence/whitespace/EOF | `PASS` |

### 5.2 Catalog, verifier와 negative control

| 검사 | 결과 |
|---|---|
| Catalog exact inventory | `PASS`; 51 row / 13 class / duplicate 0 |
| Engine/module/FQCN/method grammar | `PASS`; invalid row 0 |
| Catalog static digest | `471183deffe508e429ffddf8c4c825bb5c25d4d7b103f4f8935f5e48122859b2` |
| Verifier syntax | `PASS` |
| Bidirectional set controls | omitted class, omitted method, extra row `67`; wrong engine `68` |
| Grammar/duplicate controls | unknown `64`; malformed variants `65`; duplicate `66` |
| Report controls | missing class `69`; zero/failed/error/skipped `70/71/72/73`; missing method `74`; stale `75` |
| Positive control | exact catalog=manifest + fresh reports `0` |
| Maven/project tests | `NOT_RUN`; documentation correction이고 unrelated mutable build state를 보존 |

### 5.3 Scoped/untracked diff

Target과 human-guide tree는 correction 시작/종료 모두 Git `UNTRACKED`라 일반
`git diff --check -- <path>`만으로 content scope를 증명할 수 없다. 이를 다음처럼
보완했다.

- Before blob `f2be3514…`와 current target의 no-index diff에서 변경은 metadata
  correction round/status, 두 compatibility anchor, exit catalog/verifier/evidence
  gate에 한정됨을 확인했다.
- `git diff --no-index --check`를 before target↔after target, `/dev/null`↔new report에
  적용해 whitespace diagnostic `0`을 확인했다.
- 최종 porcelain에서 두 owned file 외 기존 unrelated tracked/untracked 상태는
  그대로 보존했다. Stage/commit/push/worktree는 수행하지 않았다.

## 6. 최종 residual과 인계

1. Independent reviewer가 target에서 `HG-P01-005/006`을 다시 판정해야 한다.
2. Phase 00 acceptance와 architecture/test identity approval 전에는 proposed catalog를
   source tree에 복사하거나 exit evidence로 사용할 수 없다.
3. 승인 change는 catalog/verifier/self-test bytes, command/run marker/fresh XML과 exit
   vector를 `E-P01-ERROR`에 content-addressed evidence로 봉인해야 한다.
4. 이 correction은 구현, Maven green, Phase 01 acceptance나 Phase 02 entry evidence가
   아니다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG-P01-005, HG-P01-006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e
TARGET_HASH_AFTER: 0afb6c603cb3f9ef3c507ec8b7ba6f76566d926e25ff2332ce178058ef86dee2
