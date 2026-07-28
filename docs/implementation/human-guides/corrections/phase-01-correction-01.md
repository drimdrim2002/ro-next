# Phase 01 사람용 구현 가이드 correction 01

```yaml
phase: "01"
correction_round: "01"
correction_kind: INDEPENDENT_HUMAN_GUIDE_CORRECTION
correction_session_relation: AUTHOR_AND_REVIEWER_SESSIONS과_분리된_새_세션
target: docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md
finding_input: docs/implementation/human-guides/reviews/phase-01-review.md
correction_date: 2026-07-29
correction_snapshot_at: 2026-07-29T02:04:52+09:00
inventory_branch: codex-implementation
inventory_head: 7cc890ee1d0805df5ae14b633127fade4f978639
target_git_state_before: UNTRACKED
target_git_state_after: UNTRACKED
review_modified: false
implementation_or_build_modified: false
maven_or_test_executed: false
git_stage_commit_push_worktree_performed: false
correction_verdict: ALL_REQUIRED_FINDINGS_ADDRESSED_PENDING_RE_REVIEW
```

## 1. 범위와 결과

수정한 파일은 [target guide](../phases/phase-01-human-implementation-guide.md)와 이
correction report뿐이다. [finding input](../reviews/phase-01-review.md), human-guide
README/progress, 인접 guide/review/correction, canonical/implementation source,
Java/POM/test/deployment와 Git state는 수정하지 않았다.

Review의 required finding 5건을 모두 target의 contract, work package, test/evidence와
handoff에 연결했다. Phase 01 status는 계속 `NOT_STARTED / NOT_ACCEPTED`,
entry는 `BLOCKED_BY_PHASE_00_ACCEPTED_EVIDENCE`다. 문서 correction을 구현 또는
acceptance로 승격하지 않았다.

## 2. Hash와 authority baseline

### 2.1 Review/target before와 after

| Artifact | Before SHA-256 | After SHA-256 | 변경 |
|---|---|---|---|
| [Review](../reviews/phase-01-review.md) | `b5a873dcd991425d2b4c183e2ee87b42c850106547371b67ee2fb5fd67156218` | `b5a873dcd991425d2b4c183e2ee87b42c850106547371b67ee2fb5fd67156218` | 없음 |
| [Target](../phases/phase-01-human-implementation-guide.md) | `1eaeb50b98c65302360631e486e7048e0b8f717415b7c5d26b725c5540043bee` | `142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e` | correction 01 |

Target before hash는 review metadata와 correction 시작 시 직접 계산값이 일치했다.
After hash는 모든 target 교정 적용 뒤 계산했다.

### 2.2 Source 역할과 충돌 처리

| Source 층 | 이 correction에서 사용한 역할 |
|---|---|
| [Canonical Master](../../../master-design.md), [질문 등록부](../../../master-design-open-questions.md) | 현재 semantic decision, exact `Q-*` 상태, Phase/gate authority |
| 현재 상위 [Domain Design](../../../domain-design.md) | Domain 의미와 Phase owner 지도. `REVIEW`이며 구현 acceptance/public API 승인이 아님 |
| 현재 상위 [Architecture Design](../../../architecture-design.md) | `RECOMMENDED` Maven/package/DAG 지도. Accepted reactor 사실이 아님 |
| 사용자 고정 [2026-07-26 Domain](../../../2026-07-26-domain-design.md), [Architecture](../../../2026-07-26-architecture-design.md) | Dated source baseline과 regression cross-check. 오래된 질문 상태를 되살리지 않음 |
| [통합 구현 설계](../../../architecture-domain-implementation-design.md) | 15 Phase 배치와 responsibility cross-check |
| [Master Realization Plan](../../master-realization-plan.md), implementation README/original Phase/review | Entry/exit, evidence, Phase boundary와 실행 baseline |

현재 상위 문서, implementation baseline과 dated source의 역할을 target §2.1에
명시했다. 임의 public schema, coordinate precision, Great Circle 함수/version,
Maven coordinate version 또는 production default를 만들지 않았다.

## 3. Finding별 correction

### HG-P01-001 — HEAD baseline과 live drift 분리

**변경 위치**

- Target metadata와 [§1](../phases/phase-01-human-implementation-guide.md#1-이-가이드의-지위와-가장-먼저-알아야-할-결론)
- [§2.1 authority](../phases/phase-01-human-implementation-guide.md#21-충돌-해소-순서)와
  [§2.2 fingerprint](../phases/phase-01-human-implementation-guide.md#22-검증-가능한-source-fingerprint)
- [§5.2 re-inventory](../phases/phase-01-human-implementation-guide.md#52-구현-시작-전-read-only-확인)와
  [§5.4 overlap stop](../phases/phase-01-human-implementation-guide.md#54-entry-evidence가-생긴-뒤-재개-조건)
- [§6.1 HEAD baseline](../phases/phase-01-human-implementation-guide.md#61-head-baseline--historical-accepted-source-후보가-아닌-비교-기준),
  [§6.2 live snapshot](../phases/phase-01-human-implementation-guide.md#62-correction-live-snapshot--미커밋미승인-관찰),
  [§6.3 target](../phases/phase-01-human-implementation-guide.md#63-목표-상태)

**교정과 이유**

HEAD `7cc890e…`의 single-project `6/1` source 상태를 historical baseline으로
유지하되 current file처럼 보이던 삭제 path link를 제거했다. Correction live
snapshot은 timestamp와 root POM/registry/POM-set/source-set/status hash를 가진
미커밋·미승인 관찰로 분리했다.

Live snapshot에는 POM 13개, main/test Java `33/16`, wrapper/reactor, moved legacy,
Phase 01 production type 0개, `adapters/common` 부재, Phase 00 evidence directory
부재와 registry의 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`를 기록했다. Review
snapshot에 있던 mutable candidate evidence와 correction snapshot의 차이도 숨기지
않았다.

**Source 근거**

- Canonical execution registry §10.1
- Canonical Phase 00 §6, WP-00-3~5, §15
- Phase 00 human guide §15
- Review §3과 `HG-P01-001`

**보존한 blocker**

Live reactor/build/test는 accepted Phase 00 evidence가 아니다. Receipt가 없으므로
Phase 01 entry는 계속 blocked이고, implementation 직전 새 inventory와 overlap
owner 확인이 필요하다.

### HG-P01-002 — Adapter module introduction ownership

**변경 위치**

- [§3.3 producer-consumer contract](../phases/phase-01-human-implementation-guide.md#33-바로-앞과-바로-뒤의-producer-consumer-계약)
- [§6.3 target](../phases/phase-01-human-implementation-guide.md#63-목표-상태)과
  [§7.4 approval table](../phases/phase-01-human-implementation-guide.md#74-open-gated-deferred와-사람-승인)
- [WP-01.1A](../phases/phase-01-human-implementation-guide.md#wp-011a--adapter-placement와-module-introduction-gate)
- [§10.1 DAG](../phases/phase-01-human-implementation-guide.md#101-package와-dependency-방향),
  §14 checklist와 §16 traceability

**교정과 이유**

Phase 00의 explicit semantic handoff가 `rpdptw-core` 하나임을 고정하고
`adapters/common`을 선행 산출물로 가정한 문구를 제거했다. Architecture owner가
current recommended `adapters/common` 또는 approved alternate placement를 선택하는
별도 gate를 추가했다.

Recommended branch에는 root/`adapters` aggregator와 leaf POM, proposed artifact,
adapter→core compile edge, Jackson adapter-only scope, test-fixture
`test-jar/tests/test` wiring, architecture-rules dependency, reactor allowlist/rule,
positive/negative DAG test, focused red→green, rollback과 `E-P01-ERROR` evidence를
연결했다. Shared fixture가 adapter를 역의존하지 않게 하여 reactor cycle을 막았다.
Current Architecture §19가 `adapters/common` local integration을 later
`AR-6/RM-8-local`에 두는 시점 차이도 드러내고, Phase 01 branch가 input-only
mapping만 소유하며 local runtime/Phase 08을 선취하지 않는다는 승인 조건을 추가했다.

**Source 근거**

- Current Architecture §5~§7, §18~§19
- Current Domain §3.1
- Canonical Phase 00 §2.1, §6, WP-00-3~5, §15
- Master Realization Plan Phase 00~01
- Review `HG-P01-002`

**보존한 blocker**

Module/path/artifactId는 `PROPOSED INTERNAL/RECOMMENDED`다. Architecture approval
전 POM/production source를 만들 수 없고, Phase 00 acceptance가 module policy를
바꾸면 approved alternate branch로 다시 review한다.

### HG-P01-003 — Sparse travel/source handoff closure

**변경 위치**

- [§7.1 scope](../phases/phase-01-human-implementation-guide.md#71-이-phase가-반드시-하는-일),
  [§7.3 contract](../phases/phase-01-human-implementation-guide.md#73-확정된-결정),
  [§8.2 hand oracle](../phases/phase-01-human-implementation-guide.md#82-단계-b--작은-탐색과-hand-oracle)
- [WP-01.3A](../phases/phase-01-human-implementation-guide.md#wp-013a--sparse-travel-input와-source-fact-seal)
- [§11.3 test](../phases/phase-01-human-implementation-guide.md#113-test-class와-method-후보),
  [§11.4 fixture](../phases/phase-01-human-implementation-guide.md#114-필수-negativeboundary-fixture)
- [§15 handoff](../phases/phase-01-human-implementation-guide.md#15-phase-02-인계)와
  §16 `REQ-P01-TRAVEL-HANDOFF`

**교정과 이유**

Java 25 skeletal candidate로 typed directed key, generic provided metric
present/absent, sparse declaration, coordinate/speed present-valid/missing과 source
identity를 추가했다. Present-invalid는 success variant가 아니라 typed rejection이고,
legacy `C`는 raw/envelope provenance only로 고정했다.

Phase 01은 integer `D/U`, typed absence, duplicate/reference, canonical order와 source
fact만 봉인한다. Phase 02만 self `0/0`, Great Circle, `45 km/h`, missing generation,
`M²`, vehicle-resolved time, `PreparedTravel`과 dense problem을 만든다는 boundary를
work package·test·handoff에 반복 연결했다. Positive/negative oracle와
`E-P01-NUMERIC/ERROR` 배치도 명시했다.

**Source 근거**

- Master §8
- Current Domain §8, dated Domain §6
- Canonical Phase 01 §2.2, §3.1, §6.2
- Canonical Phase 02 §3~§4, §7.1, §13.1
- Review `HG-P01-003`

**보존한 blocker**

Great Circle function/version, coordinate precision과 typed source policy는 Phase 02
approval blocker다. Phase 01 candidate type이 이를 값이나 library default로 닫지
않는다.

### HG-P01-004 — Resolved window/depot/wait/resource semantics

**변경 위치**

- [§7.3 contract](../phases/phase-01-human-implementation-guide.md#73-확정된-결정)와
  [§7.4 OPEN 분리](../phases/phase-01-human-implementation-guide.md#74-open-gated-deferred와-사람-승인)
- [WP-01.4](../phases/phase-01-human-implementation-guide.md#95-wp-014--time-service-trip-wait와-route-resource-declaration)
- [§10.5 Java candidate](../phases/phase-01-human-implementation-guide.md#105-timetripresource-hierarchy-후보)
- §11 fixture, §12 meaning checkpoint, §14 checklist, §15 handoff와 §16 traceability

**교정과 이유**

`START_ONLY`/`COMPLETE_WITHIN_WINDOW`, depot `taskTime` 미적용,
`depot.duration` future rotation-only, exact `N/Y`, location-transition stop,
actual-traversed-arc drive resource와 route-total/no-reset 의미를 `CONTRACT`로
복원했다.

Phase 01은 explicit declaration/typed omission과 semantic policy identity를
보존한다. Phase 04가 window profile을 bind하고 Phase 03이 departure, wait,
stop/drive accumulation, vehicle/global `min`과 feasibility를 계산한다. OPEN은
versioned public field shape/requiredness/alias/unknown policy에만 남겼다. Mapping,
negative, absence와 no-early-calculation oracle를 추가했다.

**Source 근거**

- Master §6.2, §7.3
- Question register `Q-TIME-03`, `Q-IN-02`, `Q-BENCH-03`
- Current Domain §4.4, §6, §11
- Integrated design §5.4~§5.6
- Review `HG-P01-004`

**보존한 blocker**

Multi-trip/rotation은 `DEFERRED`다. Future depot-duration 실행, rotation reset과 public
wire shape를 구현하거나 확정하지 않았다.

### HG-P01-005 — Fail-closed Maven test report gate

**변경 위치**

- [WP-01.7](../phases/phase-01-human-implementation-guide.md#98-wp-017--integration-evidence-seal-independent-review와-phase-02-handoff)
- [§11.6 command/pass](../phases/phase-01-human-implementation-guide.md#116-명령과-pass-판정)
- §12 evidence bundle, §14.5 checklist와 §16 `REQ-P01-MAVEN`

**교정과 이유**

독립 세 줄 report audit를 `set -euo pipefail` manifest loop로 교체했다. `clean
verify` 뒤 owner module, engine, FQCN과 required method별 exact XML 존재,
positive test count, zero failure/error/skipped와 testcase record를 모두 확인한다.
Missing/zero/skipped/missing-method/unknown-engine/stale-report negative self-test와
verifier/manifest digest를 evidence로 요구한다.

Current live parent에 Failsafe가 없음을 드러내고 `*Test`/Surefire와
`*IT`/approved Failsafe `integration-test`+`verify` discovery를 분리했다.
Test-fixture classifier/scope, reactor dependency, focused/root clean verify도 pass
gate에 포함했다.

**Source 근거**

- Canonical Phase 01 review F-P01-003
- Canonical Phase 01 §9.4
- Current Architecture §5.2, §18~§19
- Master Realization Plan §8~§9
- Review `HG-P01-005`

**보존한 blocker**

Report FQCN/method와 Failsafe wiring은 accepted module/name에 맞춰 봉인해야 한다.
`failIfNoSpecifiedTests=false`는 non-owner `-am` module에만 허용되며 manifest verifier
없이는 owner pass가 아니다.

## 4. 보존한 gate와 last safe point

| Gate/blocker | Correction 후 상태 | Last safe point |
|---|---|---|
| Phase 00 independent review/evidence/receipt | `BLOCKED`; fix 01/review 재실행 대기 | Guide, contract, fixture/oracle와 approval 요청 |
| Adapter module placement | `OPEN — ARCHITECTURE APPROVAL REQUIRED` | Accepted core-only handoff |
| Public wire schema/version/alias/unknown | `OPEN` | Internal SPI와 TEST-ONLY fixture |
| Great Circle/coordinate/source policy | Phase 02 blocker | Typed source/absence handoff only |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Phase 01 unaffected |
| Phase 13 `C-17` | `GATED`; 06/07/08 + 14A receipt + owner approvals 필요 | ALNS-only path |
| `Q-VAR-01`, multi-trip | `DEFERRED` | Current pair/single-trip contract |
| Phase 14B production authority | `NOT_GRANTED / GATED` | Local/evidence path only |

Exact invariants, total rejection/sealed success, immutable evidence DAG, rollback,
security/redaction, Phase 13 optionality와 Phase 14A/14B authority를 삭제하거나
완화하지 않았다.

## 5. 검증

### 5.1 수행 결과

| 검사 | 결과 |
|---|---|
| Target H1/section/WP 구조 | `PASS`; H1 1개, §1~§17, WP-01.0~01.7 + explicit 01.1A/01.3A |
| Target fenced code block | `PASS`; fence 86개, balanced |
| Target local links | `PASS`; 62개 resolve, missing 0 |
| Target GFM/explicit fragment | `PASS`; missing 0 |
| Target trailing whitespace/conflict marker | `PASS`; diagnostic 0 |
| Target EOF newline | `PASS`; final byte `0a` |
| Target `git diff --no-index --check /dev/null <target>` | `PASS`; whitespace diagnostic 0, content 차이로 expected exit 1 |
| Correction report structure/fence/EOF | `PASS`; H1 1개, fence 2개 balanced, final byte `0a` |
| Correction report local links/fragments | `PASS`; 38개 resolve, missing 0 |
| Correction report whitespace/conflict marker | `PASS`; diagnostic 0 |
| Scoped `git diff --check -- <target> <report>` | `PASS`; exit 0 |
| Untracked 보완 `git diff --no-index --check` | 두 파일 모두 whitespace diagnostic 0, content 차이로 expected exit 1 |
| Maven/test | `NOT_RUN`; 다른 Phase 00 작업의 mutable `target/`과 build state를 변경하지 않음 |

두 scoped file에 대해 link/fragment/fence/whitespace/EOF 검사와
`git diff --check`/untracked 보완 검사를 최종 재실행했다.

### 5.2 Residual risk

1. Live Phase 00 작업은 snapshot 뒤에도 변할 수 있다. Phase 01 시작 직전 accepted
   manifest와 새 timestamp/hash inventory가 필요하다.
2. Phase 00 re-review가 module/test policy를 바꿀 수 있다. Adapter branch와 exact Maven
   selector/FQCN은 그 acceptance에 맞춰 재검토해야 한다.
3. Public schema, coordinate precision/source policy와 Great Circle function/version이
   미승인이라 production adapter/travel source type은 아직 확정할 수 없다.
4. Surefire/Failsafe XML layout이나 parameterized testcase naming이 달라지면 verifier는
   fail-closed로 멈추고 parser/manifest contract를 version-up해야 한다.
5. 이 correction은 구현·test evidence가 아니다. Independent re-review와 실제
   implementation acceptance가 여전히 필요하다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG-P01-001,HG-P01-002,HG-P01-003,HG-P01-004,HG-P01-005
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 1eaeb50b98c65302360631e486e7048e0b8f717415b7c5d26b725c5540043bee
TARGET_HASH_AFTER: 142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e
