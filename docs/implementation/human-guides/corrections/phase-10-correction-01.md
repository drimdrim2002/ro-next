# Phase 10 사람용 구현 가이드 correction 01

```yaml
phase: "10"
correction_round: "01"
correction_type: INDEPENDENT_HUMAN_GUIDE_CORRECTION
source_review: docs/implementation/human-guides/reviews/phase-10-review.md
target: docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md
review_verdict_input: CHANGES_REQUIRED
finding_scope:
  - HG10-R001
  - HG10-R002
  - HG10-R003
  - HG10-R004
  - HG10-R005
addressed_findings: 5
deferred_findings: 0
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
live_inventory_snapshot_at: "2026-07-29T02:33:46+09:00"
live_inventory_status_sha256_porcelain_v1: da67223df253eb8e1a955e9bf9e884f59a65e85e4954210b002d79c9e9b2958b
implementation_or_test_execution: NOT_RUN_BY_DESIGN
edit_scope:
  - docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-10-correction-01.md
```

## 1. 교정 결과와 ownership

[독립 review](../reviews/phase-10-review.md)가 요구한 `HG10-R001`~`HG10-R005`를 모두 [Target guide](../phases/phase-10-human-implementation-guide.md)의 concrete entry, execution, test, exit와 residual gate에 반영했다. 구현, Java, POM, test, evidence, scheduler status, canonical source, 인접 guide/review는 변경하지 않았다.

| 책임 | Owner | 이 correction의 처리 |
|---|---|---|
| Requirement authority | 사용자 고정 canonical 5문서 owner | Non-dated Domain/Architecture를 current authority로 복원 |
| Source-index conflict | Repository/Implementation Documentation owner | `SOURCE_INDEX_CONFLICT`로 기록; 이 correction이 타 문서를 임의 수정하지 않음 |
| Tenant authorization | Security + Phase 08/09/10 owners | Explicit non-ambient scope 의미와 stop rule만 고정; exact API는 blocked |
| Storage failure | Phase 09/10 + independent oracle owner | Operation별 lossless category, state disposition, last safe point 고정 |
| Application/POM | Application/Coordinator + Build owner | Direct test dependency와 Surefire/Failsafe ownership change map 제시 |
| Contract/E2E test | Application, Port Contract, Architecture test owners | Concrete test source/lifecycle/strict discovery manifest 제시 |
| Acceptance | Independent reviewer + total scheduler | Evidence/review/receipt 전 상태 승격 금지 |

핵심 결과는 다음과 같다.

1. Current maps, canonical 5문서, implementation baseline, HEAD, authoring/review snapshot, correction live snapshot과 accepted evidence를 역할·시점별로 분리했다.
2. Tenant authorization와 lossless storage failure를 entry, API 의미, state transition, failure oracle, WP, exact negative test, exit와 gate로 승격했다.
3. Port contract는 test source의 concrete `*Test`, local E2E는 explicit Failsafe `*IT`로 고정하고 strict discovery/fresh XML 판정을 추가했다.
4. Live Phase 00 상태를 Fix 02/rejected evidence/receipt 부재까지 다시 snapshot하고 acceptance와 분리했다.
5. Root/application/build test-module POM owner, dependency/source-set/profile/plugin/reactor closure를 future change plan에 포함했다.

## 2. Authority, source와 시점 분리

| Authority 층 | 고정 입력 | 적용 위치 |
|---|---|---|
| Current repository/design maps | [Repository map](../../../../README.md), [Design map](../../../README.md) | Target §3.1~§3.4 |
| Canonical 5문서 | [Master](../../../master-design.md), [Domain](../../../domain-design.md), [Architecture](../../../architecture-design.md), [Integrated](../../../architecture-domain-implementation-design.md), [Question register](../../../master-design-open-questions.md) | Target metadata, §3, §6, WP, §15~§17 |
| Historical cross-check | Dated Domain/Architecture predecessor | Target metadata와 §3에서 historical only |
| Implementation baseline | Implementation README/plan, original Phase 10/review, Phase 08/09/11 | Target §3, WP, traceability |
| HEAD baseline | Commit `7cc890ee1d0805df5ae14b633127fade4f978639` | Target §5.1 |
| Authoring/review snapshot | Original Target/review inventory | Finding root cause 설명에만 사용 |
| Correction live snapshot | `2026-07-29T02:33:46+09:00`의 POM/source/test/progress/status digest | Target metadata와 §5.2~§5.3 |
| Accepted evidence | Scheduler-owned immutable receipt identity | Entry/resume/acceptance의 유일한 완료 authority |

Canonical correction snapshot의 Git object는 Master `b507a5e7ba0b7e76475bc2d755493e814f4d053a`, Domain `ace117c380466b733994a1fbb2a95d31e41b3959`, Architecture `81495ff448d0e618ab3563e8ff80614fb1028acf`, Integrated `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0`, Question register `3fff4c583a54f02dea667e78c8e5187d65ec0e18`다.

Implementation source index가 dated Domain/Architecture를 Final로 부르는 반면 current design map과 사용자 고정 canonical 5문서는 non-dated 경로를 authority로 둔다. Target은 이 충돌을 `SOURCE_INDEX_CONFLICT`로 남겼다. 다른 문서의 경로를 이 correction 범위에서 고치지 않았고, dated source를 current requirement authority로 사용하지 않았다.

## 3. Hash before/after

| Artifact | Before | After | 판정 |
|---|---|---|---|
| Review SHA-256 | `27797bac5fd3515117e21bce788fb666158c270401c2cf47bba271d902949fa1` | `27797bac5fd3515117e21bce788fb666158c270401c2cf47bba271d902949fa1` | Read-only, unchanged |
| Review Git hash-object | `25694cac8bbd553aa600dd4583df1150b029196a` | `25694cac8bbd553aa600dd4583df1150b029196a` | Read-only, unchanged |
| Target SHA-256 | `c1649837bbb057ba3272a1c0964782477d32b764e2354ed2c06a94cf14839290` | `be79b5fe2049c739ee2bd0105267f65c08a25feb22539e9b8bddf4beff1d8584` | Five findings corrected |
| Target Git hash-object | `09f6f24e53167a4e320fc158640c13d1aa0cd29f` | `dbbf11dca3bb1dee85c482d590a2e161ffcd77c3` | Commit/stage 없음 |
| Target lines | `2,074` | `2,332` | Authority/gate/oracle/Maven closure 추가 |

## 4. Finding별 root-cause correction

### HG10-R001 — Current canonical authority 복원

- **Target anchor:** Metadata; Target §3.1 `Authority 역할과 시점`, §3.3 fingerprint/semantic diff, §3.4 reading order, §15 traceability, §17 gate.
- **Root cause:** Implementation README의 dated source index를 current repository design map과 사용자 고정 canonical 5문서보다 우선해 그대로 복제했다.
- **Source:** Current `docs/README.md`; Canonical Domain §14.3/§15~§18; Canonical Architecture §11~§14/§17~§20/§22; question register.
- **Correction:** Metadata와 source table을 non-dated Domain/Architecture object로 교체했다. Dated 문서는 historical only로 내리고, all-declared completion, logical state/port, failure/security, test/reactor semantic diff를 requirement/WP/test/evidence에 전파했다.
- **Gate/last safe point:** `SOURCE_INDEX_CONFLICT`는 documentation owner가 정렬할 때까지 open이다. Last safe point는 사용자 고정 canonical 5문서 + dated historical cross-check다.
- **Verification:** Non-dated current path/object가 metadata와 §3에 있고 dated path에는 `historical`/`not canonical` 표시가 있다. Current source anchor와 local fragment를 정적으로 확인했다.
- **Residual risk:** Implementation README와 다른 human guide는 계속 dated path를 사용할 수 있다. Scheduler/documentation 차원의 별도 source-index 정렬이 필요하다.

### HG10-R002 — Tenant authorization와 lossless storage failure

- **Target anchor:** Target §3.5 entry, §4.5 invariants, §6.3 decision, §6.4 checkpoint, §8.2/§8.6/§8.8, §8.11 state/failure oracle, WP-10.0/10.2/10.5/10.7, §10.2 exact tests, §11~§13, §15/§17.
- **Root cause:** Original Phase 10의 publication/cancel/deadline residual blocker를 중심으로 gate를 구성하고, 이후 Phase 08/09가 명시한 authorization/failure blocker를 handoff 문구에만 수동 병합했다.
- **Source:** Canonical Architecture §12/§17.2~§17.3; Actual Phase 08 §7.1/§7.3; Actual Phase 09 §8.3~§8.4/§13/§15.2; Phase 09 human guide의 authorization/failure gate.
- **Correction:** Caller `TenantId`를 untrusted selector로 명시했다. Explicit non-ambient authorized solve/storage scope가 lookup/read/decode/CAS/dispatch보다 먼저 결박되지 않으면 backend observation 0으로 멈춘다. Denied/not-found/corrupt/precondition/visibility-indeterminate/partial-write를 operation별로 lossless하게 보존하고 state/pointer/action의 last-safe-point 규칙을 추가했다.
- **State transition/failure oracle:** Unauthorized request는 `UNBOUND REQUEST → REJECTED_NON_REVEALING`이고 transition/action/backend call이 0이다. Storage failure는 prior exact state/version, operation result와 call trace를 independent oracle에 입력하며, 승인된 durable failure transition 전까지 마지막 authoritative state/pointer CAS를 유지한다.
- **Exact verification:** `CoordinatorAuthorizationFailureContractTest`에 caller tenant only, missing context, tenant/solve mismatch, ambient binding, same solve/cross tenant, denied, not-found, corrupt, indeterminate, partial write, stale precondition의 11개 method를 추가했다.
- **Gate/last safe point:** Exact `AuthorizedSolveScope`와 failure union은 `OPEN/CROSS-PHASE BLOCKED`다. Phase 08/09/security/10 공동 승인 전 pure reducer와 storage/publication side effect 0이 마지막 안전 지점이다.
- **Residual risk:** External non-revealing response와 internal typed audit의 exact Java/session/facade shape는 아직 승인되지 않았다. Target은 public signature나 retry default를 발명하지 않는다.

### HG10-R003 — Port contract와 local E2E Maven discovery

- **Target anchor:** Target §5.4 target tree, §5.5 Maven closure, WP-10.7, §10.2 exact manifest, §10.4 false-green, §13.6 exit.
- **Root cause:** Abstract reusable support, concrete contract test와 local integration test를 하나의 suite 이름으로 축약하고 source set/class name을 Maven lifecycle과 연결하지 않았다.
- **Source:** Canonical Architecture §18.1~§18.4; Integrated §22.1~§22.3; Phase 09 runnable contract pattern; Phase 11 human guide의 Failsafe `-Dit.test` pattern; Master Plan §8~§9.
- **Correction:** `CoordinatorPortContractSupport`는 support로만 두고 concrete `CoordinatorPortContractTest`를 `src/test/java`에서 Surefire로 실행하도록 했다. `Phase10CoordinatorIT`에 exact 4개 method/oracle을 추가하고 proposed `phase10-local-it` profile의 Failsafe `integration-test`/`verify`, `-Dit.test`, `failsafe.failIfNoSpecifiedTests=true`를 연결했다.
- **Discovery oracle:** Same-source full `clean install` 뒤 selected no-`-am` run, filter 없는 reactor `-am clean verify`, strict fail-if-none, command-start 뒤 fresh Surefire/Failsafe XML, exact suite/class/method/count, missing/duplicate/skipped 0을 모두 요구한다.
- **Gate/last safe point:** Port module과 local-IT profile은 현재 부재한 future target이며 `BUILD-OWNER GATED`다. Effective POM과 concrete test가 없으면 command failure가 정상이고 acceptance를 주장하지 않는다.
- **Verification:** Main-source suite를 selected test로 부르는 경로를 제거했고 port test 3개, local IT 4개를 required manifest에 추가했다. Original Phase 10 method 66개도 모두 보존했다.
- **Residual risk:** Concrete fake/local implementation이 production helper를 oracle로 공유하면 발견만 된 false-green이 남는다. Independent model과 implementation별 trace를 evidence에 봉인해야 한다.

### HG10-R004 — Live inventory와 resume authority

- **Target anchor:** Metadata; Target §3.1 authority time, §3.3 fingerprints, §5 resume table와 §5.2~§5.3 inventory.
- **Root cause:** Shared checkout의 concurrent Phase 00 진행을 authoring 중 한 시점만 fingerprint하고 handoff 직전에 다시 snapshot하지 않았다.
- **Source:** Live root/child POM, wrapper, application/architecture/test-fixture source inventory, Execution Progress §5/§8/§10.1.
- **Correction:** `2026-07-29T02:33:46+09:00` 기준 root/reactor/application/build POM blob, wrapper, app main/test 0, architecture Java 9/`*Test` 8, Phase 10 named test/evidence 0을 기록했다. Progress는 Phase 00 review 02 `CHANGES_REQUIRED`, Fix 02 `IN_PROGRESS`, evidence `REJECTED_PENDING_FIX_02_REGENERATION`, receipt `NOT_PRODUCED`로 갱신했다.
- **Gate/last safe point:** HEAD, authoring/review, correction live와 accepted evidence를 네 층으로 분리했다. Resume는 live file existence가 아니라 accepted commit/bundle/review/receipt identity가 현재 source/effective POM/evidence에 연결될 때만 가능하다.
- **Verification:** Live relevant blobs와 counts를 재계산했고 metadata/표가 snapshot과 일치한다. Toolchain은 Java `25.0.3`, Maven `3.9.14`로 확인했다.
- **Residual risk:** Shared checkout은 correction 뒤 다시 바뀔 수 있다. Phase 10 착수 직전에 timestamp/status digest/relevant blob/count를 재-snapshot하되 live blob 자체를 장기 acceptance key로 사용하지 않는다.

### HG10-R005 — Maven compile/dependency closure

- **Target anchor:** Target §5.4 tree, §5.5 closure, §8.1 ownership, WP-10.0/10.1/10.7, §10.1 fixture와 §10.4 false-green, §11.2 evidence, §13.6 exit.
- **Root cause:** Production compile DAG만 설명하고 required test source set, direct test dependency, fixture owner, consumer module edge와 Maven lifecycle을 구현 WP에서 제외했다.
- **Source:** Canonical Architecture §5.2/§7/§18~§19; Integrated §3.2/§3.5~§3.6/§22; Master Plan §4.1~§4.2/§8; live root/application/architecture/test-fixture POM.
- **Correction:** Root는 version/plugin policy, application POM은 direct JUnit 및 실제 승인·사용하는 property dependency와 unit/IT lifecycle, port/architecture POM은 application test-scope consumer edge, build POM은 module aggregation을 소유하도록 change map을 추가했다. Coordinator fixture는 application test source가 기본이며 shared artifact가 필요하면 approved acyclic edge 뒤 이동한다.
- **Compile/dependency oracle:** Effective POM, dependency tree, profile activation, plugin/provider version, same-source installed application POM/JAR digest, reactor order와 report를 evidence에 포함한다. Stale local artifact나 production-main fixture leakage는 실패다.
- **Gate/last safe point:** Exact library/version/fixture sharing/profile은 accepted Phase 00/08 build contract 전 `PROPOSED/BUILD-OWNER GATED`다. Last safe point는 reviewed POM/test map과 future-red manifest이며 임의 dependency/POM을 만들지 않는다.
- **Verification:** Required POM owner가 target tree/WP에 있고, Surefire/Failsafe 및 `-am clean`/selected no-`-am` 명령이 dependency order와 연결됐다.
- **Residual risk:** Actual POM/module/test는 아직 없으므로 compile/test evidence는 `NOT_PRODUCED`다. 승인된 build owner가 effective POM과 clean repository execution으로 닫아야 한다.

## 5. 보존한 계약과 gate

- Immutable manifest, exact ref/state authority, all-declared normal completion + candidate verifier `PASS`.
- Stable ordinal reduction, strict improvement, retry에서 `AttemptId`만 변경.
- Artifact-before-pointer, one invocation/one authoritative CAS, durable pending action, lease-free correctness.
- Event/listing hint-only, cancellation intent/same-state fence/actual termination 분리.
- Restart-safe deadline blocker와 watchdog/resource/platform failure의 normal quality termination 분리.
- Phase 07 both-gate result와 Phase 09 distinct publication precondition/reconcile authority.
- `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`.
- Phase 13은 Phase 14A receipt와 C-17 별도 승인 전 `GATED TARGET`.
- Phase 14A benchmark qualification과 Phase 14B official/provider/production authority 분리.
- 완성 Java/POM/public/wire API, cloud adapter와 numeric/default를 이 correction에서 발명하지 않음.

## 6. 검증 결과

| 검사 | 결과 | 근거 |
|---|---|---|
| Finding coverage | PASS | `HG10-R001`~`HG10-R005` 모두 concrete anchor/root cause/source/gate/oracle/residual과 연결 |
| Review immutability | PASS | Review SHA-256와 Git object before=after |
| Canonical source selection | PASS | Current non-dated Domain/Architecture가 authority, dated source는 historical only |
| Original exact method preservation | PASS | Original unique method 66개가 Target에 모두 존재; 차집합 0 |
| Added required methods | PASS | Authorization/failure 11 + port contract 3 + local E2E 4 = 18 |
| Maven discovery plan | PASS | Concrete test source, Surefire/Failsafe strict selection, reactor `-am clean`, fresh XML oracle |
| Compile/dependency plan | PASS | Root/application/build POM owner, direct dependency, fixture/module edge, profile/plugin/order 명시 |
| Live inventory | PASS | Timestamp/status digest/relevant blob/count와 acceptance 분리 |
| Implementation/Maven test | NOT_RUN_BY_DESIGN | Phase 10 source/test/module/evidence가 0이고 predecessor/build gate가 미승인 |
| Local links/GFM fragments | PASS | Target와 correction의 relative file/fragment target 검사 |
| Heading/fence/whitespace/EOF | PASS | H1/heading structure, fence parity, trailing whitespace/tab/CRLF/NUL, EOF LF 검사 |
| Scoped diff/allowed write | PASS | 두 허용 파일만 correction scope에서 수정; stage/commit/push/worktree 없음 |

두 허용 경로만 scoped 조회한 status는 다음과 같다.

```text
?? docs/implementation/human-guides/corrections/phase-10-correction-01.md
?? docs/implementation/human-guides/phases/phase-10-human-implementation-guide.md
```

Target은 review 시점에도 untracked였으므로 tracked diff가 아니라 review의 before hash와 correction after hash로 내용을 대조했다. 두 파일에 대한 `git diff --check`와 untracked-aware `git diff --no-index --check /dev/null <file>`의 whitespace diagnostic은 0이다. Existing unrelated working-tree change는 수정·삭제·stage하지 않았다.

검증은 guide correction의 정합성과 future verification contract를 뜻한다. Phase 10 implementation, test, evidence, predecessor acceptance 또는 Phase 00 acceptance를 뜻하지 않는다.

## 7. 남은 blocker와 residual risk

1. Phase 00 review 02는 `CHANGES_REQUIRED`, Fix 02는 `IN_PROGRESS`, evidence는 regeneration 전 rejected 상태이고 receipt가 없다.
2. Phase 06~09 accepted handoff와 Phase 10 scheduler task/role separation이 없다.
3. Authorized scope/failure carrier, pending action/publication precondition, cancellation/publication fence와 durable deadline ADR는 cross-Phase blocked다.
4. Port contract module, application test dependency, Failsafe profile와 모든 Phase 10 source/test/`E-P10-*`는 아직 없다.
5. `SOURCE_INDEX_CONFLICT`는 repository/implementation documentation owner의 별도 정렬이 필요하다.
6. `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`; Phase 13/14 gate와 production authority는 열리지 않았다.
7. Correction live snapshot은 시점 자료다. 이후 drift를 Target after hash나 status digest로 acceptance하지 않는다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG10-R001, HG10-R002, HG10-R003, HG10-R004, HG10-R005
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: c1649837bbb057ba3272a1c0964782477d32b764e2354ed2c06a94cf14839290
TARGET_HASH_AFTER: be79b5fe2049c739ee2bd0105267f65c08a25feb22539e9b8bddf4beff1d8584
