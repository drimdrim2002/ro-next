# Phase 03 사람용 구현 가이드 correction 01

```yaml
phase: "03"
correction_round: "01"
correction_type: INDEPENDENT_HUMAN_GUIDE_CORRECTION
source_review: docs/implementation/human-guides/reviews/phase-03-review.md
target: docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md
review_verdict_input: CHANGES_REQUIRED
finding_scope:
  - HG03-R001
  - HG03-R002
  - HG03-R003
  - HG03-R004
  - HG03-R005
addressed_findings: 5
deferred_findings: 0
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
live_inventory_snapshot_at: 2026-07-29T02:03:10+09:00
live_inventory_status_sha256: 7ff2b5b6c96f7a76c6b2e21e904ed1a7f1cfa3c18f11682221c63a537475a4fb
implementation_or_test_execution: NOT_RUN
edit_scope:
  - docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-03-correction-01.md
```

## 1. 교정 결과

[독립 review](../reviews/phase-03-review.md)가 요구한 5개 finding을 모두 [Target guide](../phases/phase-03-human-implementation-guide.md)의 구체 section에 반영했다. 이 correction은 구현, POM/test/deployment, scheduler status, canonical source 또는 인접 guide/review를 바꾸지 않았다.

핵심 결과는 다음과 같다.

1. HEAD baseline과 timestamp/hash가 있는 미커밋 live drift를 분리했다.
2. Fact/failure/facet package owner 충돌을 후보 A/B와 `CROSS-PHASE ARCHITECTURE BLOCKER`로 열어 두었다.
3. Canonical exact test 42개와 11개 hard-bound parameter row를 복원하고 false-green 판정을 강화했다.
4. Java fragment가 의도적으로 compile-closed가 아닌 review 입력임을 명시하고 모든 참조 type family의 owner/semantic closure를 추가했다.
5. 잘못된 내부 절 참조 4개를 실제 §11.2/§11.5로 교정했다.

## 2. Authority와 시점 구분

교정에는 다음 역할 구분을 적용했다.

| 입력 | 역할 | 적용한 section |
|---|---|---|
| [Canonical Master](../../../master-design.md) | 전체 의미, 불변조건, gate와 변경 절차 | Target §3, §7, §9, §14~17 |
| [Current Domain map](../../../domain-design.md) / [Current Architecture map](../../../architecture-design.md) | 현재 `REVIEW` 상위 의미·배치 지도 | Target §3.1~3.2, §9.1~9.4 |
| [User-fixed dated Domain](../../../2026-07-26-domain-design.md) / [dated Architecture](../../../2026-07-26-architecture-design.md) | 구현 문서 세트에 고정된 dated meaning/placement baseline | Target §3, §9 |
| [Integrated design](../../../architecture-domain-implementation-design.md) | 15 Phase와 capability/facet/evidence 연결 | Target §3, §7, §9, §16 |
| [Question register](../../../master-design-open-questions.md) | Exact OPEN/GATED/deferred 상태 | Target §3, §7, §13~17 |
| [Implementation README](../../README.md), [master plan](../../master-realization-plan.md), [canonical Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md), [original Phase review](../../reviews/phase-03-review.md) | 실행 baseline, exact test/evidence와 review correction history | Target §5~§16 |

Current upper map은 최신 검토 지도를 제공하지만 `REVIEW` 상태이고, dated source는 사용자 고정 baseline이다. Implementation baseline은 실행 계약을 제공하지만 승인 record 없이 상위 owner/package를 변경할 권위가 아니다. 이 세 층의 package-owner 충돌을 숨기거나 임의로 닫지 않았다.

## 3. Hash 기록

| Artifact | Before | After | 해석 |
|---|---|---|---|
| Review SHA-256 | `0184730a8503c825fb02eee55f7391a6c49951835c511922f9a05320528abfcc` | `0184730a8503c825fb02eee55f7391a6c49951835c511922f9a05320528abfcc` | Read-only, 변경 없음 |
| Review Git hash-object | `ad46c1dde7b7035ab074c464297c56c2675df36a` | `ad46c1dde7b7035ab074c464297c56c2675df36a` | Read-only, 변경 없음 |
| Target SHA-256 | `1611717ddd519cc581fe57cffc384ce6e43f7bd2185904c2aeb1fc68e26222f2` | `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3` | Finding 5개 교정 |
| Target Git hash-object | `6c5d7f1fa3cd3c574194396479dc20ececd131fd` | `f8c9db72ad5bc1a6b5c23d658b5d4e6280f96fae` | Git object identity, commit/stage 없음 |

## 4. Finding별 변경

### HG03-R001 — HEAD와 live inventory 분리

- **변경 위치:** Target metadata, [§3 Source authority](../phases/phase-03-human-implementation-guide.md#3-source-authority와-fingerprint), [§5.2 Entry gate](../phases/phase-03-human-implementation-guide.md#52-entry-gate를-확인하는-방법), [§6 inventory](../phases/phase-03-human-implementation-guide.md#6-실제-inventory-head-baseline-미커밋-live-snapshot과-목표), [§11.5 Maven](../phases/phase-03-human-implementation-guide.md#115-maven-명령과-fail-closed-판정)
- **변경 이유:** HEAD source fingerprint와 계속 변하는 working tree를 “현재 checkout” 한 상태로 표시하면 wrapper/reactor/link/command가 동시에 틀릴 수 있다.
- **Source 근거:** Master Realization Plan §3의 inventory 원칙, live `git status --short`, root POM, target tree, Execution Progress §5/§10.1.
- **구체 교정:** `7cc890e…` HEAD 표와 `2026-07-29T02:03:10+09:00` live 표를 분리했다. Live status SHA-256, root POM/progress blob, 13 POM, executable wrapper, 23 production package skeleton, Phase 00 test 9개와 Phase 03 test/evidence 0개를 기록했다. Legacy link는 live 이동 경로로 바꾸고 HEAD path는 `git show` snapshot으로만 설명했다.
- **보존 gate:** Live reactor/wrapper는 `UNCOMMITTED_UNAPPROVED_SNAPSHOT`; Phase 00 Fix 01/receipt와 Phase 01~02 receipt 전 Phase 03 source/evidence command로 승격하지 않는다.
- **Residual risk:** Live tree는 다시 바뀔 수 있다. 구현 착수 시 timestamp/HEAD/status digest/relevant blob/count를 재-snapshot해야 한다.

### HG03-R002 — package owner/DAG conflict

- **변경 위치:** Target [§3.1 역할 구분](../phases/phase-03-human-implementation-guide.md#31-충돌-해소-순서), [§7.4~7.5](../phases/phase-03-human-implementation-guide.md#74-opengateddeferred), [§9.1 package 후보](../phases/phase-03-human-implementation-guide.md#91-proposed-package와-file-배치), [§9.4 DAG](../phases/phase-03-human-implementation-guide.md#94-dependency-direction), WP-03.0, §12/§14~17
- **변경 이유:** Current/dated Domain·Architecture는 typed physical fact를 `evaluation.api`에 두지만 canonical Phase review F-P03-010은 propagation-owned fact/facet을 제시한다. 어느 쪽도 Target 단독으로 승인할 수 없다.
- **Source 근거:** Current Domain §3.1, Current Architecture §6.1, dated Final Domain §3, dated Final Architecture §2.3, Integrated §7.6, original Phase review F-P03-010.
- **구체 교정:** 후보 A(`evaluation.api` owner)와 후보 B(`propagation` owner)의 acyclic import DAG, owner-sensitive fact/failure/facet family, Phase 04/05/07 consumer 영향과 architecture-test 요구를 나란히 기록했다.
- **보존 gate:** `CROSS-PHASE ARCHITECTURE BLOCKER`; Core/Evaluation + Architecture owner가 관련 authority/ADR와 같은 변경 단위로 승인하기 전 package/file 생성 금지. Last safe point는 semantic contract, hand oracle과 import-graph review다.
- **Residual risk:** Type만 이동하고 rejection/fingerprint/facet/provider/verifier consumer를 함께 갱신하지 않으면 의미 cycle이나 duplicate authority가 남는다.

### HG03-R003 — exact test matrix와 false-green

- **변경 위치:** Target [§11.3 exact table/11 rows](../phases/phase-03-human-implementation-guide.md#113-test-class와-method-후보), [§11.5 manifest 판정](../phases/phase-03-human-implementation-guide.md#115-maven-명령과-fail-closed-판정), §12/§14/§16
- **변경 이유:** Target 37개와 canonical 42개의 차집합 5개, 축약된 boundary row는 반복/overnight, resource monotonicity, poisoned route fingerprint, neutral metric과 score-access 결함을 놓칠 수 있다.
- **Source 근거:** Canonical Phase 03 §9.3~9.4, original Phase review F-P03-003/F-P03-009, dated Final Domain §17.2/§17.5.
- **구체 교정:** 누락 5개 method를 원래 class/order에 복원했다. `rejectsOneUnitBeyondEveryHardBound()`를 weight/volume lower+upper, service close, due, exclusive plan end, full-arc work end, stop, drive time/distance의 11개 row로 열고 category/semantic code/call-count oracle을 명시했다.
- **검증 oracle:** Canonical과 Target의 table-order `Class#method` manifest는 각각 42줄이고 SHA-256 `79379c996379669725e70dd9577bce42d4fb1c5de4f00024d5415de4e75c6e1c`로 같다. Future report도 양방향 차집합, missing/duplicate/failed/error/skipped가 모두 0이어야 한다.
- **보존 gate:** Java enum token은 API review 전 `PROPOSED/OPEN`; 실제 test/report/evidence는 `NOT_PRODUCED`.
- **Residual risk:** 이름과 report row가 있어도 production helper가 oracle을 공유하거나 11개 parameter가 일부만 실행되면 sensitivity false-green이 남는다.

### HG03-R004 — Java contract closure

- **변경 위치:** Target [§9.1 owner-sensitive tree](../phases/phase-03-human-implementation-guide.md#91-proposed-package와-file-배치), [§9.2 fragment/closure](../phases/phase-03-human-implementation-guide.md#92-skeletal-contract-후보), [§11.1 fixture wiring](../phases/phase-03-human-implementation-guide.md#111-fixture-builder와-oracle-분리)
- **변경 이유:** `MetricDeclaration`, `MetricValue`, `ConstraintKey`, `ConstraintCheck`, `MetricSnapshot` 등 참조 type의 owner/visibility/unit/fingerprint가 없으면 구현자가 임의 package나 raw map으로 gap을 닫을 수 있다.
- **Source 근거:** Master §1.2, Current Architecture §6.1/§8, dated Final Architecture §2.3~2.5, canonical Phase §6~7.2.
- **구체 교정:** Snippet을 Java 문법의 의도적 non-compile-closed review fragment로 표기했다. 모든 참조 contract family에 proposed owner/visibility, semantic field, unit/fingerprint와 allowed edge를 기록했다. Live POM의 test-scope core dependency, test-JAR, core JUnit 부재와 Surefire/Failsafe discovery도 실제/future 조건으로 분리했다.
- **보존 gate:** §9.4 owner 승인 전 actual file 생성 금지; 빈 marker/raw `Map`/unit 없는 value/fingerprint 없는 snapshot으로 compile만 맞추는 행위 금지.
- **Residual risk:** Compile-closed skeleton은 아직 승인·생성되지 않았다. Phase 00 POM/source-set wiring과 owner decision 뒤 별도 compile review가 필요하다.

### HG03-R005 — 내부 절 참조

- **변경 위치:** Target §6.3, §8.2, WP-03.1, WP-03.2.
- **변경 이유:** 존재하지 않는 §10.2/§10.5는 hand fixture와 Maven precondition 탐색을 방해한다.
- **Source 근거:** Target heading inventory의 §11.2와 §11.5.
- **구체 교정:** 두 `§10.2`를 `§11.2`로, 두 `§10.5`를 `§11.5`로 교체했다.
- **보존 gate:** Historical source를 가리키는 Target §3.2의 “초안 §10.2”는 의도적 source section이므로 변경하지 않았다.
- **Residual risk:** 평문 절 번호는 heading 이동 시 다시 stale할 수 있으므로 heading-aware 검사에 포함해야 한다.

## 5. 보존한 exact contract와 gate

- Complete request pair, same concrete vehicle, exactly once, pickup-before-delivery와 모든 load prefix.
- Phase 02 exact `ProblemInstance`/`PreparedTravel` authority, directed lookup, raw/reverse/lazy fallback 금지.
- Full-arc next-window restart, inclusive capacity/window/resource와 exclusive `planEnd`, checked integer arithmetic.
- `Invalid`/`Infeasible` 분리와 physical hard → neutral metric → composed hard → score → objective 순서.
- Cache-free full recomputation authority, independent oracle, immutable evidence DAG와 rollback/last-safe point.
- Full-solution evaluator와 business equality/context tie의 기존 cross-Phase blocker.
- `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`, facet/public API/performance OPEN.
- Phase 13은 14A receipt + 별도 `C-17` 승인 전 GATED이며 backend dependency/API를 선반영하지 않는다.
- Phase 14A benchmark qualification과 Phase 14B official/provider/production authority를 분리한다.

## 6. 검증 결과

| 검사 | 결과 | 근거 |
|---|---|---|
| Finding coverage | PASS | HG03-R001~R005 모두 Target concrete section과 연결 |
| Review immutability | PASS | Review SHA-256/Git object before=after |
| Exact method matrix | PASS | Canonical=Target 42줄, SHA-256 `79379c…e1c`, diff 0 |
| Wrong section reference | PASS | Target의 stale §10.2/§10.5 실행 참조 0; historical source §10.2만 의도적으로 유지 |
| Implementation/build execution | NOT_RUN | Phase 03 구현 review가 아니며 live Phase 00 drift가 미승인 |
| Local links/GFM fragments | PASS | 두 파일의 relative file/fragment target 검사 |
| Heading/fence/whitespace/EOF | PASS | H1/heading 구조, fence parity, trailing whitespace/tab/CRLF, EOF LF 검사 |
| Scoped `git diff --check` | PASS | 두 허용 파일에 대해 실행; untracked-aware no-index whitespace diagnostic도 0 |
| Allowed write scope | PASS | Target guide와 이 correction report만 수정 |

검증은 문서 구조와 correction의 정합성만 뜻한다. Phase 03 code/test/evidence/acceptance 또는 live Phase 00 acceptance를 뜻하지 않는다.

## 7. 남은 blocker와 residual risk

1. Phase 00 Fix 01과 independent review/receipt, Phase 01~02 implementation/evidence/receipt가 없다.
2. 후보 A/B package owner는 Core/Evaluation + Architecture owner가 승인해야 한다.
3. Full-solution evaluator owner/API와 business equality/context tie boundary는 계속 cross-Phase blocker다.
4. Live Surefire `failIfNoTests=false`, Failsafe 부재, core JUnit 부재와 fixture source-set wiring은 Phase 00/POM owner의 accepted 변경 전 evidence 조건이 아니다.
5. Phase 03 source/test/`E-P03-*`, independent review와 post-review acceptance receipt는 모두 `NOT_PRODUCED`.
6. Live snapshot은 시점 자료다. 이후 working-tree drift를 이 correction의 hash로 권위화하면 안 된다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG03-R001,HG03-R002,HG03-R003,HG03-R004,HG03-R005
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 1611717ddd519cc581fe57cffc384ce6e43f7bd2185904c2aeb1fc68e26222f2
TARGET_HASH_AFTER: f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3
