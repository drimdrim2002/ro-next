# Phase 01 사람용 구현 가이드 독립 리뷰

```yaml
phase: "01"
review_kind: INDEPENDENT_HUMAN_GUIDE_REVIEW
reviewer_task_character: AUTHOR_SESSION과_분리된_새_작업
target: docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md
target_git_state: UNTRACKED_AT_REVIEW_SNAPSHOT
target_git_blob: 326c99bbc3446fd6fda3f8ab52345fd34d69556c
target_sha256: 1eaeb50b98c65302360631e486e7048e0b8f717415b7c5d26b725c5540043bee
target_lines: 2054
review_date: 2026-07-29
inventory_snapshot_at: 2026-07-29T01:27:35+09:00
inventory_branch: codex-implementation
inventory_head: 7cc890ee1d0805df5ae14b633127fade4f978639
target_modified_by_reviewer: false
implementation_or_build_modified_by_reviewer: false
verdict: CHANGES_REQUIRED
target_changes_required: true
```

## 1. 결론

최종 verdict는 **`CHANGES_REQUIRED`**다.

가이드는 15개 canonical Phase, Phase 00 entry blocker, Phase 13 `C-17` optional
gate, Phase 14A/14B authority, 문서 완료와 구현 완료의 분리, immutable
evidence DAG를 전반적으로 잘 보존한다. 그러나 target 수정이 필요한 finding이
5건 있다.

- 현재 checkout을 HEAD baseline과 live drift로 분리하지 않아 실제 reactor,
  이동된 legacy source와 생성 중인 Phase 00 evidence를 모두 부재로 기록했다.
- Phase 00이 넘기는 유일한 semantic module은 `rpdptw/core`인데
  `adapters/common`을 누가 언제 reactor에 추가하는지 작업이 없다.
- Phase 02가 요구하는 sparse travel/source/typed-absence 계약이 scope와 handoff에만
  있고 구현·test·traceability에 닫혀 있지 않다.
- `Q-TIME-03`과 `Q-IN-02`의 이미 확정된 의미 일부가 빠졌거나 다시 승인 대기
  항목처럼 표현됐다.
- Surefire report 확인 예시는 앞선 실패가 마지막 성공 명령에 가려질 수 있다.

Phase 01 entry가 여전히 닫혀 있으므로 이 결함들이 곧바로 production 실행을
허용하지는 않는다. 그렇더라도 현재 형태로는 Phase 00 acceptance 뒤 사람이
실행 가능한 가이드가 아니며, target 변경이 필요하므로 `ACCEPTED`로 판정할 수
없다.

## 2. Review 입력과 fingerprint

### 2.1 권위·실행 문서

| 입력 | 직접 대조한 section | Review snapshot SHA-256 | 판정 |
|---|---|---|---|
| [`docs/master-design.md`](../../../master-design.md) | §1.5, §4~§8, §13~§17 | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | Target 기록과 일치 |
| [`docs/2026-07-26-domain-design.md`](../../../2026-07-26-domain-design.md) | §1~§7, §16~§18, §20~§21 | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | Target 기록과 일치 |
| [`docs/2026-07-26-architecture-design.md`](../../../2026-07-26-architecture-design.md) | §1~§2, §5.6, §6 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | Target 기록과 일치 |
| [`docs/architecture-domain-implementation-design.md`](../../../architecture-domain-implementation-design.md) | §1~§5, §19~§25, §27~§30 | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | Target 기록과 일치 |
| [`docs/master-design-open-questions.md`](../../../master-design-open-questions.md) | §1~§4와 exact `Q-*` rows | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | `26/1/1` 확인 |
| [`docs/implementation/master-realization-plan.md`](../../master-realization-plan.md) | §1~§15, 특히 Phase 00~02·test·evidence·gate | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | Target 기록과 일치 |
| [`docs/implementation/README.md`](../../README.md) | §1, §3~§7 | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | Target 기록과 일치 |
| [`docs/implementation/execution-progress-and-results.md`](../../execution-progress-and-results.md) | §1~§10, 특히 live execution registry | `0ec2ee8c7a2ee3b79a2eb51ed9d1287af4f6031dd8b20fd3bb273f3248172c61` | HEAD SHA `37f1a8…`에서 live drift 발생 |
| [`docs/implementation/phases/phase-01-canonical-input-normalization.md`](../../phases/phase-01-canonical-input-normalization.md) | 전체, 특히 §2~§15 | `67e078a058753335ae823bbec815b3628f212d4593dfce9ee0db39cc02000324` | Target 기록과 일치 |
| [`docs/implementation/reviews/phase-01-review.md`](../../reviews/phase-01-review.md) | 전체 finding·residual blocker | `4644e7a7ef30f82368eea167e4f70197f4a1380a808c8066f37036608561f27f` | Target 기록과 일치 |

Target이 기록한 `execution-progress-and-results.md`의 SHA-256
`37f1a8a0…`와 Git blob `250aa90…`은 HEAD
`7cc890ee1d0805df5ae14b633127fade4f978639`의 baseline에는 맞는다. 그러나
review snapshot의 working-tree bytes는 위 표의 `0ec2ee8c…`다. 이 차이를
현재 inventory와 분리하지 않은 것이 `HG-P01-001`의 일부다.

### 2.2 인접 guide와 사람용 guide registry

| 입력 | 직접 대조한 section | SHA-256 |
|---|---|---|
| [`phase-00-human-implementation-guide.md`](../phases/phase-00-human-implementation-guide.md) | metadata, §6~§7, WP-00-3~5, §15 Phase 01 handoff | `bf6a35990f0554ffee7206e19f18213b601164ba11830938944c298c063d5c16` |
| [`phase-02-human-implementation-guide.md`](../phases/phase-02-human-implementation-guide.md) | metadata, §4~§6, WP-02.0~02.6, §10~§15 | `93c53ea2055bea89df4cb70dddc95b66b035f01bc392764d59d1ec86dea87f3a` |
| [`human-guides/README.md`](../README.md) | §1~§7, review 역할·15 Phase index | `ad64de533a4e45984ae30be12c57d969a36efd4dc8453e949ec4efc992f6b18a` |
| [`human-guides/execution-progress-and-results.md`](../execution-progress-and-results.md) | §1~§7, Phase 01 review task registry | `7ddab7350f2ea4328f9340603b38c8bb26fd082a14aca07e4a617d38003880a7` |

Historical `docs/2026-07-26-master-design.md`는 누락·퇴행 확인에만 사용했고 현재
결정 authority로 사용하지 않았다. `docs/codex/`와 legacy 11-phase 계획은
요구 근거로 사용하지 않았다.

## 3. Repository inventory snapshot

### 3.1 HEAD baseline

HEAD `7cc890ee1d0805df5ae14b633127fade4f978639`에는 다음이 있다.

- root implicit-JAR POM 1개
- `src/main/java` Java 6개
- `src/test/java` Java 1개
- `com.ronext.optimizer` HTTP/GCP placeholder와 synthetic
  `AlnsBatchEngine`
- target reactor, wrapper, `rpdptw/core`, architecture rules와 Phase evidence는 없음

이 상태가 target §2.2의 baseline Git blob/SHA와 §6.1 inventory의 출처다.

### 3.2 미커밋 live drift

같은 checkout의 review snapshot에는 별도 Phase 00 구현 작업이 진행되어 다음
미커밋 상태가 존재했다.

- root POM은 `com.ronext:ro-next-parent`, `packaging=pom` reactor로 변경됨
- root 포함 POM 13개: `rpdptw` 6 leaf module, `build/test-fixtures`,
  `build/architecture-rules`, `legacy/gcp-placeholder` 및 aggregator
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper` 존재
- root `src/**`의 6 main/1 test는 삭제 상태이고 legacy source가
  `legacy/gcp-placeholder`로 이동·확장됨
- `rpdptw`, `build`, `legacy` 아래 live source는 main Java 33개, test Java 14개
- `rpdptw/core`의 `input`/`normalization`에는 `package-info.java`만 있고 Phase 01
  domain type 구현은 여전히 0개
- `adapters/common` module은 여전히 없음
- ignored `target/phase-00-evidence/`에는
  `E-P00-BUILD/ARCH/LEGACY` candidate와 34-test summary가 나타났고
  `pre-review-status.txt`는
  `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW`,
  `PHASE_01_ENTRY=BLOCKED_UNTIL_PHASE_00_INDEPENDENT_ACCEPTANCE`를 기록함
- canonical execution registry의 live working-tree 기록은
  `PREREQUISITE_REMEDIATION_IN_PROGRESS`, acceptance receipt
  `NOT_PRODUCED`이므로 Phase 00 또는 Phase 01을 accepted로 판정하지 않음

Live `pom.xml` SHA-256은
`ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`다.
Target이 기록한 `f61cab65…`는 HEAD baseline에만 해당한다.

이 drift는 다른 작업의 소유물이며 reviewer는 어떤 파일도 수정·이동·삭제하지
않았다. Candidate evidence는 mutable ignored artifact이고 independent review와
post-review receipt가 없으므로 Phase 01 entry를 열지 않는다.

## 4. Review 방법과 기준

1. Target 2,054줄 전체를 읽고 source authority, canonical Phase 01/review,
   Phase 00/02 원본과 인접 human guide를 section 단위로 대조했다.
2. `git rev-parse`, `git status`, `git diff`, `git ls-tree`, 실제 POM/source/evidence
   inventory로 HEAD baseline과 미커밋 live drift를 분리했다.
3. 13개 요청 축에 대해 의미·identity·lifecycle·Phase ownership, 학습 가능성,
   WP 실행 가능성, Java/Maven 배치, test oracle, evidence/rollback/security/
   observability/reproducibility와 gate 우회를 점검했다.
4. 모든 local Markdown link 59개와 fragment 25개를 target directory 기준으로
   resolve하고 GFM heading slug 또는 explicit anchor와 비교했다.
5. Target에 대해 trailing whitespace, conflict marker, fence balance, 필수
   heading/WP 구조와 EOF를 검사했다.
6. Maven/test는 실행하지 않았다. 이 review의 허용 출력은 review 파일 하나뿐이고,
   Maven 실행은 mutable `target/`을 추가 변경할 수 있으며 현재 별도 Phase 00
   작업이 같은 checkout에서 진행 중이다.

Severity는 다음처럼 적용했다.

- `CRITICAL`: gate 우회, data/security 손상 또는 즉시 잘못된 production 권한 부여
- `HIGH`: 구현/검증 계약을 잘못 만들거나 Phase handoff를 실질적으로 막는 결함
- `MEDIUM`: 실행·판정 신뢰성을 낮추지만 상위 gate가 즉시 위험 실행을 막는 결함
- `LOW`: 의미를 바꾸지 않는 국소 정확성·가독성·정적 품질 결함

## 5. Finding summary

| Severity | Count | Target 수정 필요 |
|---|---:|---:|
| `CRITICAL` | 0 | 0 |
| `HIGH` | 4 | 4 |
| `MEDIUM` | 1 | 1 |
| `LOW` | 0 | 0 |
| 합계 | 5 | 5 |

## 6. Findings

### HG-P01-001 — HEAD baseline을 현재 live inventory로 표시해 실제 reactor와 evidence drift를 숨김

| 필드 | 내용 |
|---|---|
| Severity | `HIGH` |
| Finding | Target metadata는 `inventory_checked_at: 2026-07-29`이고 §6.1 제목도 “2026-07-29 실제 상태”지만, 내용은 HEAD의 root POM 1개/main 6/test 1 상태다. Review 시점 live checkout에는 reactor/POM 13개, wrapper, target package skeleton, 이동된 legacy source와 Phase 00 candidate evidence가 있다. §1은 Phase 00 구현을 `NOT_STARTED`, §6.1은 target module/evidence를 모두 부재로 쓰고, 삭제된 root source 4개를 현재 file link로 제공한다. |
| 사람에게 미치는 영향 | 구현자가 현재 module을 새로 만들거나 이동된 legacy source를 삭제·복원 대상으로 오인할 수 있다. 동시 Phase 00 작업을 덮거나 accepted baseline과 mutable candidate evidence를 혼동할 수 있으며, “실제 inventory”를 믿고 실행한 link/file 검사가 실패한다. |
| Target 위치/anchor | metadata `inventory_checked_at`/`inventory_commit`; §1 “가장 먼저 알아야 할 결론”; [§2.2 source fingerprint](../phases/phase-01-human-implementation-guide.md#22-검증-가능한-source-fingerprint); [§5.2 read-only 확인](../phases/phase-01-human-implementation-guide.md#52-구현-시작-전-read-only-확인); [§6.1 실제 상태](../phases/phase-01-human-implementation-guide.md#61-2026-07-29-실제-상태), 특히 lines 437~450 |
| Source evidence | `docs/implementation/execution-progress-and-results.md` §10.1 “Phase 00 prerequisite remediation”; canonical Phase 00 §6 “변경 대상 module/package/file tree”, WP-00-3~5와 §15.1; `pom.xml` live reactor; `target/phase-00-evidence/pre-review-status.txt`와 `phase-01-handoff.txt`; `git status --short --untracked-files=all` |
| Root cause | Commit-scoped fingerprint table와 working-tree inventory를 같은 “현재” 표현으로 합쳤고, 동시 작업이 생긴 뒤 재-snapshot하지 않았다. |
| Required correction | Target에 `HEAD baseline`과 `live working-tree drift`를 별도 표로 둔다. Baseline blob/SHA는 historical snapshot으로 유지하되 live POM/source/evidence 상태, canonical registry 상태, candidate evidence의 비권위성, snapshot timestamp를 기록한다. 삭제된 root source link는 HEAD-only임을 명시하거나 현재 legacy 경로와 구분한다. Phase 01은 계속 blocked이며 Phase 00 candidate를 accepted로 올리지 않는다. 구현 직전 재-inventory 명령과 overlap stop rule을 명시한다. |
| Target 수정 필요 여부 | `YES` |
| Residual risk | 같은 checkout의 Phase 00 작업이 계속 변할 수 있다. 교정 후에도 inventory는 시점 snapshot이므로 Phase 01 착수 직전에 accepted receipt와 clean/dirty manifest를 다시 확인해야 한다. |

### HG-P01-002 — `adapters/common`의 생성·승인 ownership이 없어 WP-01.1 명령이 도달 불가능함

| 필드 | 내용 |
|---|---|
| Severity | `HIGH` |
| Finding | Target은 Phase 00가 `adapters/common`을 “실제로 생성·승인”한 뒤 WP-01.1을 실행한다고 가정한다. 그러나 Phase 00의 명시적 handoff는 `rpdptw/core`와 test-fixture/architecture seam뿐이고, 현재 live Phase 00 handoff도 `SOLE_SEMANTIC_PRODUCTION_START=rpdptw/core`다. Target에는 Phase 01이 `adapters/common/pom.xml`, parent/aggregator module entry, dependency edge와 architecture rule registration을 만드는 작업도 없다. |
| 사람에게 미치는 영향 | Phase 00가 올바르게 acceptance되어도 `mvn -pl adapters/common ...`은 module-not-found로 실패한다. 구현자는 Phase 00 scope를 소급 확장하거나, accepted reactor를 review 없이 바꾸거나, adapter를 core에 넣어 Jackson 경계를 깨는 세 가지 잘못된 선택 중 하나를 하게 된다. |
| Target 위치/anchor | [§6.2 목표 상태](../phases/phase-01-human-implementation-guide.md#62-목표-상태); [WP-01.1](../phases/phase-01-human-implementation-guide.md#92-wp-011--versioned-anti-corruption-adapter), 특히 lines 727~777; WP-01.6 lines 1203~1207; WP-01.7 lines 1273~1280; §10.1 dependency 방향 |
| Source evidence | canonical Phase 00 §2.1 목표 item 6, §6 tree/§6.1 package ownership, WP-00-3 item 1·8, WP-00-5 item 6, §15.1 “Accepted `rpdptw-core` module”; adjacent Phase 00 human guide §7.1과 WP-00-3~5; Master Realization Plan §4.1은 `adapters/common`을 proposed future module로만 둠; live `target/phase-00-evidence/phase-01-handoff.txt`; live reactor module list에는 `adapters/common` 없음 |
| Root cause | Canonical Phase 01의 proposed tree/명령을 복사하면서 predecessor가 실제로 소유하는 skeleton 범위와 Phase 01의 module-introduction 책임 사이 lifecycle 단계를 만들지 않았다. |
| Required correction | Phase 00 accepted handoff가 core-only임을 명시한다. Architecture owner 승인 아래 Phase 01이 `adapters/common`을 도입하는 별도 WP를 추가하거나, 승인된 다른 placement를 사용하는 조건부 branch를 정의한다. 새 module POM/aggregator edge, adapter→core compile edge, Jackson의 adapter-only scope, architecture rule update, reactor/focused red→green, rollback과 evidence를 포함해야 한다. “Phase 00가 생성할 것”이라는 사전조건은 제거하고 실제 accepted module contract에 맞춘다. |
| Target 수정 필요 여부 | `YES` |
| Residual risk | Phase 00 acceptance review가 module policy를 바꿀 수 있다. 최종 이름은 proposed internal로 남기되 module 생성 owner와 dependency invariant는 acceptance 전에 확정돼야 한다. |

### HG-P01-003 — Sparse travel/source handoff가 scope 문장뿐이고 구현·test·trace로 닫히지 않음

| 필드 | 내용 |
|---|---|
| Severity | `HIGH` |
| Finding | Target은 sparse provided travel과 coordinate/speed source fact를 scope 및 Phase 02 handoff에 넣었지만 이를 만드는 type, work package, positive/negative oracle와 requirement trace가 없다. 현재 test는 decimal `D/U`, duplicate key와 dangling reference 일부만 다룬다. Typed `D/U` presence/absence, meter/second authority, legacy `C`의 non-authority, raw/source provenance, coordinate/speed present-valid·missing 구분을 누가 봉인하는지 판정할 수 없다. |
| 사람에게 미치는 영향 | Phase 02가 요구하는 유일 source artifact가 불완전해 raw JSON을 다시 읽거나 detached travel input을 받게 된다. Missing과 invalid speed가 합쳐져 승인된 `45 km/h` fallback trigger가 오염되거나, `C`가 generation/semantic fingerprint에 들어가거나, 제공값과 생성값의 authority가 뒤섞일 수 있다. |
| Target 위치/anchor | [§7.1 scope](../phases/phase-01-human-implementation-guide.md#71-이-phase가-반드시-하는-일), lines 508~512; WP-01.2 lines 835~845; WP-01.3 lines 914~924; [§11.4 fixtures](../phases/phase-01-human-implementation-guide.md#114-필수-negativeboundary-fixture); [§15.1 handoff](../phases/phase-01-human-implementation-guide.md#151-producer-artifact); [§16 traceability](../phases/phase-01-human-implementation-guide.md#16-source--requirement--work-package--testevidence-traceability) |
| Source evidence | Master §8 “Directed distance/time matrix 계약”, 특히 provided `D/U`, `C`, self/missing/source/provenance rules; Final Domain §6 “Travel preparation”; canonical Phase 01 §2.2, §3.1 Travel handoff, §6.2 lines 341·356~359; canonical Phase 02 §3, §4 handoff validation, §7.1 source boundary와 §13.1; adjacent Phase 02 human guide §4~§6와 WP-02.2 |
| Root cause | Phase 02의 generation을 앞당기지 않으려다 Phase 01이 소유해야 하는 declaration validation까지 생략했다. Canonical Phase 01의 같은 추적성 공백도 그대로 상속했다. |
| Required correction | Phase 01 소유와 Phase 02 소유를 명시적으로 분리한 travel-input WP 또는 기존 WP의 완전한 하위 작업을 추가한다. Phase 01은 typed sparse key, integer meter/second provided value와 absence, `C` non-authority/raw provenance, source identity, coordinate/speed present-valid·missing/invalid와 canonical ordering을 봉인한다. Phase 02만 self `0/0` override, missing `D/U` 생성, `M²`, vehicle-resolved time과 `PreparedTravel`을 만든다고 고정한다. Proposed type, exact fixture/oracle, evidence key 배치와 `REQ-P01-TRAVEL-HANDOFF` trace를 추가한다. |
| Target 수정 필요 여부 | `YES` |
| Residual risk | Great Circle 함수/version, coordinate precision과 typed source policy는 Phase 02의 별도 approval blocker다. Phase 01 교정이 이 OPEN 항목을 값으로 닫아서는 안 된다. |

### HG-P01-004 — 확정된 window/depot/wait/resource 의미가 빠지거나 OPEN처럼 재표현됨

| 필드 | 내용 |
|---|---|
| Severity | `HIGH` |
| Finding | Target은 `Q-TIME-03`의 `START_ONLY`/`COMPLETE_WITHIN_WINDOW` ownership을 설명하지 않고, `Q-IN-02`의 depot `taskTime` 미적용, future rotation에서만 쓰는 depot `duration`, wait `Y/N` exact mapping, location-transition stop, actual-arc drive resource, route-total/no-reset 의미를 WP/test/handoff에 풀지 않았다. 오히려 WP-01.4 사전조건은 `waitInDepot wire meaning` 승인을 요구하고 §10.5는 Y/N mapping을 별도인 것처럼 써 이미 resolved된 의미와 아직 OPEN인 versioned wire shape를 혼합한다. |
| 사람에게 미치는 영향 | 구현자가 depot `taskTime`을 service에 더하거나, roundtrip의 최초 출발/최종 복귀에 depot `duration`을 적용하거나, wait Y/N을 반대로 mapping할 수 있다. Resource를 일/근무창마다 reset하거나 vehicle/global 값을 조기에 합치는 경우 Phase 03과 verifier가 다른 feasibility를 계산한다. 반대로 이미 resolved된 의미를 불필요한 승인 blocker로 기다릴 수 있다. |
| Target 위치/anchor | §7.3 확정 결정; [WP-01.4](../phases/phase-01-human-implementation-guide.md#95-wp-014--time-service-trip-wait와-route-resource-declaration), 특히 lines 978~1055; [§10.5](../phases/phase-01-human-implementation-guide.md#105-timetripresource-hierarchy-후보); §11.3~§11.5; §15.1; trace row `REQ-P01-TIME`/`REQ-P01-TRIP-RESOURCE` |
| Source evidence | Master §7.3 “Planning period와 time”, 특히 customer policy, lines 643~649 depot/trip/wait/resource; question register exact `Q-TIME-03`, `Q-IN-02`, `Q-BENCH-03`; Final Domain §5.2; Integrated §5.6; canonical Phase 01 §3.1, §6.2, §7.4와 §11.1 |
| Root cause | “Phase 01은 declaration만, Phase 03은 적용”이라는 책임 분리를 설명하면서 declaration이 보존해야 하는 resolved semantics와 owner를 함께 생략했다. Public schema OPEN과 semantic decision RESOLVED도 분리하지 않았다. |
| Required correction | 위 의미를 `CONTRACT`로 교육·scope·WP·test·handoff·trace에 추가한다. Phase 01은 exact Y/N→typed wait policy, depot task/duration provenance, vehicle/global route-limit declaration과 semantic policy identity를 보존하되 departure/min/reset/propagation은 계산하지 않는다고 명시한다. `START_ONLY`/`COMPLETE_WITHIN_WINDOW`는 Phase 04 binding과 Phase 03 적용 ownership을 설명하고 Phase 01이 hidden profile default를 채우지 않게 한다. OPEN은 versioned field shape/unknown/alias policy로만 남긴다. 각 항목에 mapping/negative/absence oracle를 추가한다. |
| Target 수정 필요 여부 | `YES` |
| Residual risk | Multi-trip/rotation은 계속 `DEFERRED`이며 future depot-duration 실행을 구현해서는 안 된다. Public schema version과 profile binding 형태도 별도 owner 승인 전 확정할 수 없다. |

### HG-P01-005 — Owner Surefire report 확인 예시가 shell 전체 exit code에서 false-green 가능

| 필드 | 내용 |
|---|---|
| Severity | `MEDIUM` |
| Finding | WP-01.7의 세 줄짜리 XML audit 예시는 `set -e`나 `&&`가 없다. `test -s`가 실패하고 positive test-count `rg`가 실패해도 shell은 다음 줄을 실행하며, 마지막 `! rg ... failures/errors/skipped`가 성공하면 전체 pasted block의 exit code가 0이 될 수 있다. Required method execution record 확인 명령도 예시에 없다. |
| 사람에게 미치는 영향 | Target이 canonical review F-P01-003에서 막으려 한 owner-test 0건/no-report false-green이 evidence command 자체에서 다시 생긴다. 사람이 마지막 exit code만 기록하면 누락된 test report로도 WP-01.7을 통과시킬 수 있다. |
| Target 위치/anchor | [WP-01.7](../phases/phase-01-human-implementation-guide.md#98-wp-017--integration-evidence-seal-independent-review와-phase-02-handoff), lines 1282~1293; [§11.6 명령과 pass 판정](../phases/phase-01-human-implementation-guide.md#116-명령과-pass-판정) |
| Source evidence | canonical Phase 01 review F-P01-003; canonical Phase 01 §9.4 “합격 판정”; Master Realization Plan §8 test와 §9 evidence 규칙 |
| Root cause | 사람용 예시를 독립 shell command 나열로 썼지만 evidence capture 단위와 fail-fast 동작을 고정하지 않았다. |
| Required correction | 예시를 `set -euo pipefail`을 사용하는 검증 script 또는 `test ... && rg ... && ! rg ...` 같은 하나의 fail-closed command로 바꾼다. 모든 required owner class와 required method/testcase가 정확히 존재하는지 loop/manifest로 검사하고, 그 verifier 자체의 negative self-test와 exit code를 evidence에 넣는다. |
| Target 수정 필요 여부 | `YES` |
| Residual risk | Surefire XML schema와 package/class 이름은 accepted module/name에 따라 바뀔 수 있다. 고정된 report contract나 검증 script를 Phase 01 evidence identity에 포함해야 한다. |

## 7. 13개 검사축 판정과 finding이 없는 부분의 근거

| 검사축 | 판정 | 근거 | Residual risk |
|---|---|---|---|
| 1. 원문 의미·불변조건·identity/lifecycle·Phase 경계 | `FAIL` | Raw/semantic/envelope identity, sealed/rejected lifecycle, pair와 no-prepared boundary는 보존. Travel과 time/depot/resource 의미는 `HG-P01-003/004` 미충족 | 교정 시 Phase 02/03/04 ownership을 앞당기지 않아야 함 |
| 2. 신규 Java/CVRPTW 구현자의 배경·이유·앞뒤 계약 이해 | `FAIL` | RPDPTW primer와 Phase map은 충분하지만 module creation과 travel/resource handoff를 독자가 스스로 추론해야 함 | Guide가 길어 교정 후에도 실행 경로 index가 필요할 수 있음 |
| 3. 명령/WP 실행·판정 가능성 | `FAIL` | 대부분 WP에 prerequisite/target/action/expected/failure/rollback/handoff가 있음. `HG-P01-002/003/004/005`가 실제 실행을 막음 | Accepted module 이름에 맞춘 최종 command 재검증 필요 |
| 4. Java/Maven 실제 구조와 proposed API/dependency | `FAIL` | Type은 proposed로 잘 표기했으나 actual inventory와 adapter module lifecycle가 `HG-P01-001/002`로 불일치 | 동시 Phase 00 acceptance 결과가 아직 미확정 |
| 5. 복붙 완성 코드 또는 과도한 추상화 | `PASS` | Java는 skeletal responsibility와 질문 중심이고 full implementation을 제공하지 않음 | Travel/resource type 보강도 완성 코드가 아니라 계약 수준이어야 함 |
| 6. Entry/exit/evidence/rollback/failure/security/observability/reproducibility | `PARTIAL` | Four-key evidence, no-partial, PII canary, immutable manifest→review→receipt, rollback은 강함. Report verifier false-green은 `HG-P01-005` | End-to-end transport/storage telemetry는 later Phase 소유 |
| 7. OPEN/GATED/deferred/EXPERIMENT_REQUIRED hidden default | `PASS_WITH_CORRECTION` | `Q-BENCH-02`, public schema, `C-17`, `Q-VAR-01`, multi-trip을 값으로 닫지 않음. `HG-P01-004`는 resolved semantic과 OPEN wire shape 분리 필요 | 교정 중 wait/profile 의미를 새 public schema로 확정하면 안 됨 |
| 8. Phase 13 optional/14A/14B 우회 | `PASS` | Phase 06/07/08 accepted + 14A receipt + C-17 approvals와 14B production authority를 명시적으로 보존 | 실제 receipt/authority는 모두 미생성 |
| 9. 인접 Phase artifact ownership/dependency/lifecycle | `FAIL` | Dense ID/prepared travel은 Phase 02, propagation은 Phase 03, profile binding은 Phase 04로 둠. Adapter module과 travel declaration owner는 `HG-P01-002/003` 미완 | Phase 00/02 guide도 live status refresh가 필요하지만 이 target correction과 분리 |
| 10. 상대 링크/GFM fragment/fingerprint/traceability/용어 | `FAIL` | Fragment 25개는 모두 유효. Live 삭제 path 4개가 missing target이고 travel trace가 없음. `HG-P01-001/003` | Baseline link와 live path를 동시에 표현하는 규칙 필요 |
| 11. 사람 결정·승인과 마지막 안전 지점 | `PASS_WITH_CORRECTION` | Checkpoint A~D, stop/resume, last safe point가 있음. `HG-P01-004`에서 resolved 의미를 다시 승인 대상으로 보이게 함 | Public schema/ADR owner가 실제 assignee로 지정돼야 함 |
| 12. 문서 완성과 실제 구현/legacy 11-phase 혼동 | `PASS` | `implementation_status: NOT_STARTED`, `implementation_claim: NONE`, 00~14 15개 Phase를 유지하며 review verdict를 implementation acceptance로 승격하지 않음 | Human-guide registry 자체는 live Phase 00 진행을 반영하지 않지만 reviewer 수정 범위 밖 |
| 13. Test fixture/builder/oracle/red→green/false-green/category pass | `FAIL` | Independent oracle, negative fixture, category matrix와 owner-report gate는 강함. Travel/resource coverage와 shell false-green이 `HG-P01-003/004/005` | Future target test는 구현 뒤 실제 red→green evidence가 필요 |

## 8. 링크·구조·whitespace 정적 검사

### 8.1 Target 구조

| 검사 | 결과 |
|---|---|
| 파일 존재/non-empty | `PASS` |
| H1 | 1개 |
| Top-level `##` section | 17개, §1~§17 |
| Ordered WP | WP-01.0~WP-01.7, 8개 |
| Required metadata | Phase/status/entry/public schema/implementation/evidence/acceptance/roles/handoff 존재 |
| Conflict marker | 0개 |
| Fenced code block balance | `PASS` |
| EOF newline | `PASS` |

### 8.2 Link와 fragment

Target의 local Markdown link 59개와 fragment 25개를 검사했다.

- GFM fragment missing: 0
- Relative target missing: 4

Missing 4개는 모두 §6.1이 현재 file처럼 가리키는, live working tree에서 삭제·이동된
HEAD baseline source다.

```text
src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java
src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java
src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java
src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java
```

현재 대응 source는 `legacy/gcp-placeholder/src/**`에 있다. 이를 단순 링크 치환하면
HEAD baseline 설명을 잃으므로 `HG-P01-001`의 baseline/live 분리와 함께 고쳐야 한다.

### 8.3 Whitespace

Target 하나만 대상으로 다음을 검사했다.

```bash
rg -n '[[:blank:]]+$' \
  docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md

git diff --check -- \
  docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md

git diff --no-index --check /dev/null \
  docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md
```

Trailing whitespace diagnostic은 0개다. Standard `git diff --check -- <target>`는
exit 0이지만 target이 untracked라 내용을 검사하지 않는 한계가 있다. 이를 보완한
`--no-index --check`도 whitespace diagnostic 0개였고, content difference 때문에
예상 exit 1이었다.

## 9. 구현 blocker와 target correction의 구분

다음은 target 문구 교정만으로 해제되지 않는 실제 blocker다.

| 실제 blocker | 현재 판정 | Target finding과의 관계 |
|---|---|---|
| Phase 00 independent implementation review/acceptance receipt | 미생성, Phase 01 entry 계속 차단 | `HG-P01-001`은 상태 표현 교정이며 gate 해제가 아님 |
| `adapters/common` placement/module approval | 미확정 | `HG-P01-002`가 실행 가능한 ownership 절차를 요구 |
| Public wire schema/version/unknown/alias policy | `OPEN` | Internal contract/test-only fixture까지만 가능 |
| Great Circle/source policy approval | Phase 02 blocker | `HG-P01-003` 교정이 함수·수치를 발명하면 안 됨 |
| `Q-BENCH-02`/14A corpus·criteria·receipt | `OPEN — EXPERIMENT_REQUIRED`/미생성 | Phase 13과 official quality 주장 차단 |
| `C-17` 나머지 승인 | `GATED` | Phase 01은 OR-Tools/backend field를 추가하지 않음 |
| Phase 14B production authority | `NOT_GRANTED` | Local/document/test 성공으로 대체 불가 |

Target correction은 위 blocker를 숨기거나 status를 올리는 작업이 아니다.

## 10. 최종 verdict

**`CHANGES_REQUIRED`**

Target 변경이 필요한 finding이 5건 있으므로 `ACCEPTED` 조건을 충족하지 않는다.
Reviewer는 target guide, README/progress, 다른 guide/review/correction,
canonical 문서, Java/POM/test/deployment와 git state를 수정하지 않았다. 이
파일만 생성했다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=4 MEDIUM=1 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG-P01-001,HG-P01-002,HG-P01-003,HG-P01-004,HG-P01-005

## Correction 01 읽기 전용 재검증

```yaml
recheck_round: "01"
recheck_kind: ORIGINAL_REVIEWER_READ_ONLY_CORRECTION_RECHECK
recheck_at: 2026-07-29T02:45:04+09:00
scope: HG-P01-001_THROUGH_HG-P01-005_AND_CORRECTION_REGRESSIONS_ONLY
target_sha256_rechecked: 142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e
target_git_blob_rechecked: f2be351463309b97ffa35eb34367404b342013ef
target_lines_rechecked: 2620
correction_report_sha256_rechecked: 16cb6c7caa246a93a301cdbc74e796ed4122e92e24cd83a7f98217f398688792
correction_report_git_blob_rechecked: e2b589e2782190a0eba52948f871094e2ee1f150
correction_report_lines_rechecked: 312
review_sha256_before_append: b5a873dcd991425d2b4c183e2ee87b42c850106547371b67ee2fb5fd67156218
inventory_branch: codex-implementation
inventory_head: 7cc890ee1d0805df5ae14b633127fade4f978639
target_modified_by_reviewer: false
correction_report_modified_by_reviewer: false
implementation_or_build_modified_by_reviewer: false
maven_or_test_executed: false
```

### 재검증 입력과 fingerprint

Correction report의 자기 판정은 evidence가 아니라 수정 위치를 찾는 index로만
사용했다. 아래 파일의 현재 bytes를 직접 읽고 target의 해당 anchor와 다시 대조했다.

| 읽은 파일 | 재검증 SHA-256 | 직접 대조한 범위 |
|---|---|---|
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | §1.5, §4~§8, Phase/gate·travel/time 불변조건 |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | §1~§7, travel·service·trip cross-check |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | §1~§2, §5.6, §6; dated architecture cross-check |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | §3.1, §4.4, §6, §8, §11 |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | §5~§7, §18~§19의 recommended module/DAG/test map |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | §3, §5, §19~§25, §27~§30 |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | `Q-TIME-03`, `Q-IN-02`, `Q-BENCH-03`, OPEN/GATED 상태 |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | §1~§15, 특히 Phase 00~02·test·evidence |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | §3 authority, §6~§7 execution/review |
| `docs/implementation/execution-progress-and-results.md` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | §10.1 live execution registry |
| `docs/implementation/phases/phase-00-build-architecture-skeleton.md` | `0ad01e21a94ac543486a53c0ed0a256b4137e673bbae1be4d959ebc46dcefd27` | Phase 00 module/evidence/handoff |
| `docs/implementation/phases/phase-01-canonical-input-normalization.md` | `67e078a058753335ae823bbec815b3628f212d4593dfce9ee0db39cc02000324` | §2~§15 |
| `docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md` | `51d8491a701f88b218609ea32e9ec714710ff88291760c1dc63bfc27bba9f686` | Phase 01 input, source policy, preparation owner와 entry |
| `docs/implementation/reviews/phase-01-review.md` | `4644e7a7ef30f82368eea167e4f70197f4a1380a808c8066f37036608561f27f` | `F-P01-003`, residual command risk |
| `docs/implementation/human-guides/phases/phase-00-human-implementation-guide.md` | `c1d18c0f118f840c272c1737da678dc83769ed13ceca02227ec525edf4bd2f7c` | §6, WP00-3~5, §15.1 core-only handoff |
| `docs/implementation/human-guides/phases/phase-02-human-implementation-guide.md` | `123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace` | §4~§6, WP-02.0~2.3, test manifest, §14~§15 |
| 이 원 review의 append 전 bytes | `b5a873dcd991425d2b4c183e2ee87b42c850106547371b67ee2fb5fd67156218` | 원 finding `HG-P01-001`~`005`와 required correction |
| Corrected target | `142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e` | 수정 anchor 전체, 2,620줄 |
| Correction report | `16cb6c7caa246a93a301cdbc74e796ed4122e92e24cd83a7f98217f398688792` | 수정 자기주장과 before/after fingerprint, 312줄 |

Historical/datetime source는 regression cross-check로만 사용했고 현재 authority를
대체하지 않았다.

### 재검증 방법과 live inventory

1. 원 finding의 root cause와 required correction을 문장 단위로 다시 고정했다.
2. Correction report가 지시한 target 위치를 직접 읽고, 각 계약이 scope 문장뿐 아니라
   WP, Java 책임, fixture/oracle, pass gate, rollback과 인접 handoff까지 닫히는지
   확인했다.
3. 실제 POM/module/source/evidence와 execution registry를 다시 조사해 correction
   snapshot과 이후 live drift를 분리했다.
4. Target/correction/review의 local link와 GFM fragment, fence, conflict marker,
   trailing whitespace와 EOF를 정적으로 검사했다. Target의 report verifier shell은
   `bash -n`으로 syntax를 확인하고 실제 POM의 Surefire/Failsafe discovery와 대조했다.
5. Maven/test는 실행하지 않았다. Phase 01 entry는 닫혀 있고 실행은 다른 Phase 00
   작업의 mutable `target/`을 바꿀 수 있으므로 문서 재검증 범위를 벗어난다.

Recheck 시점 live checkout은 root 포함 POM 13개, main Java 33개, test Java 17개이며
`adapters/common`과 Phase 01 production type/evidence는 없다. Root POM SHA-256은
`ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`다.
Phase 00 candidate evidence directory는 다시 존재하지만 registry §10.1은
`REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS`,
`REJECTED_PENDING_FIX_02_REGENERATION`, acceptance receipt `NOT_PRODUCED`,
handoff `PHASE_01_BLOCKED`다.

이는 target §6.2의 `2026-07-29T02:04:52+09:00` correction snapshot 이후 발생한
live drift다. Target은 그 snapshot을 미커밋·미승인 관찰로 표시하고 WP-01.0에서
재-inventory를 강제하므로 이 변화 자체는 `HG-P01-001` 재개방 사유가 아니다.

### Finding 재판정 요약

| Finding | 원 severity | 재검증 status | Target 수정 필요 |
|---|---:|---:|---:|
| `HG-P01-001` | `HIGH` | `RESOLVED` | `NO` |
| `HG-P01-002` | `HIGH` | `RESOLVED` | `NO` |
| `HG-P01-003` | `HIGH` | `RESOLVED` | `NO` |
| `HG-P01-004` | `HIGH` | `RESOLVED` | `NO` |
| `HG-P01-005` | `MEDIUM` | `OPEN` | `YES` |
| `HG-P01-006` — correction regression | `LOW` | `OPEN` | `YES` |

### HG-P01-001 — RESOLVED

- **확인한 target anchor:** metadata와 §1; §2.2 fingerprint; §5.2
  re-inventory; §5.4 overlap stop; §6.1 HEAD baseline, §6.2 timestamped correction
  live snapshot, §6.3 목표 상태; WP-01.0과 §12.1.
- **Source evidence:** execution registry §10.1; canonical Phase 00의 module/evidence
  handoff; Phase 00 human guide §6과 §15.1; 실제 root POM/source/evidence/status.
- **판정 근거:** HEAD `6/1` 상태는 historical comparison으로, live POM/source/status는
  timestamped unaccepted observation으로 분리됐다. 삭제된 root source를 current link로
  제시하지 않으며, candidate evidence와 root green을 acceptance로 쓰지 않는다.
  구현 직전 새 hash/status와 path ownership을 다시 확인하고 overlap이면 멈추는
  절차가 WP-01.0에 연결됐다.
- **남은 risk:** 같은 checkout의 Phase 00 fix 02가 계속 변한다. Accepted
  manifest/receipt가 생긴 뒤 새 inventory를 봉인해야 하며 현재 candidate를 재사용할
  수 없다.

### HG-P01-002 — RESOLVED

- **확인한 target anchor:** §3.3 producer-consumer contract; §6.3 목표 상태; §7.4
  `Adapter module placement`; WP-01.1A; WP-01.1의 conditional command; §10.1 DAG;
  §12.1, §14.1~§14.2와 §16 `REQ-P01-ADAPTER/ARCH/MAVEN`.
- **Source evidence:** Phase 00 human guide §15.1의 accepted
  `rpdptw-core`/fixture/architecture seam; current Architecture §5~§7, §18~§19의
  `adapters/common` recommended/later placement; canonical Phase 00/01과 Master
  Realization Plan Phase 00~01.
- **판정 근거:** Phase 00이 adapter module을 준다고 더는 가정하지 않는다.
  Architecture owner가 Branch A 또는 approved alternate를 고른 뒤에만
  parent/aggregator/leaf POM, adapter→core, Jackson adapter-only, test-jar
  `tests/test`, architecture rule와 positive/negative DAG를 도입한다. 승인 전
  production source를 만들지 않는 last safe point, exact conditional command,
  rollback patch와 `E-P01-ERROR` handoff도 있다.
- **남은 risk:** Exact module name/coordinate와 parent policy는 Phase 00 acceptance와
  architecture approval 뒤에야 확정된다. Branch B면 selector, report path와 rule
  target을 다시 봉인해야 한다.

### HG-P01-003 — RESOLVED

- **확인한 target anchor:** §7.1~§7.4; WP-01.3A; §10.8 pseudocode; §11.2~§11.6;
  §12.2/§12.4; §14.3; §15 전체; §16 `REQ-P01-TRAVEL-HANDOFF`.
- **Source evidence:** Master §8; current Domain §8과 dated Domain §6; canonical
  Phase 01 §2.2/§3.1/§6.2; canonical Phase 02의 Phase 01 input/source preparation
  contract; adjacent Phase 02 human guide §4~§6, WP-02.2~2.3과 §14~§15.
- **판정 근거:** Typed directed key, `D/U` 각각의 present/absent와 integer
  meter/second, source identity, `C` non-authority, coordinate/speed
  present-valid/missing/invalid와 canonical ordering이 Phase 01 WP/type/oracle로
  닫혔다. Self `0/0`, Great Circle, `45 km/h`, missing generation, `M²`,
  vehicle-resolved time, `PreparedTravel`과 dense problem은 Phase 02에만 남는다.
  Exact positive/negative fixtures, independent tuple oracle, evidence key와 consumer
  check가 모두 연결됐다.
- **남은 risk:** Coordinate representation/precision, Great Circle function/version과
  source policy는 여전히 Phase 02 approval blocker다. Proposed Java 이름을 승인된
  public/production contract로 읽어서는 안 된다.

### HG-P01-004 — RESOLVED

- **확인한 target anchor:** §7.3 `CONTRACT`; §7.4의 semantic/wire-shape 분리;
  WP-01.4; §10.5; §11.3~§11.5; §12.2; §14.3; §15와 §16
  `REQ-P01-TIME/TRIP-RESOURCE`.
- **Source evidence:** question register `Q-TIME-03`, `Q-IN-02`,
  `Q-BENCH-03`; Master §6.2/§7.3; current Domain §4.4, §6.3/§6.6와 §11;
  integrated design §5.4~§5.6.
- **판정 근거:** `START_ONLY`/`COMPLETE_WITHIN_WINDOW` declaration→Phase 04
  binding→Phase 03 application ownership, depot `taskTime` 미적용,
  `depot.duration` future rotation-only, exact `N/Y`, location-transition stop,
  actual-arc drive와 route-total/no-reset 의미가 WP/type/fixture/handoff에
  일치한다. OPEN은 public field shape/requiredness/alias/unknown policy에만
  한정되고 resolved semantic을 재승인 대상으로 되돌리지 않는다.
- **남은 risk:** Multi-trip/rotation은 `DEFERRED`다. Future rotation reset과
  public wire shape는 이 Phase 구현자가 확정할 수 없다.

### HG-P01-005 — OPEN

- **Severity:** `MEDIUM`
- **Finding:** WP-01.7의 `set -euo pipefail` loop는 각 manifest row의 report,
  positive test count, zero failure/error/skipped와 method testcase를 fail-closed로
  확인한다. 그러나 target lines 1681~1688은 manifest가 “각 required class의
  **최소 한** required method”만 명시하면 된다고 정한다. 원 required correction은
  **모든** required owner class와 **모든 required method/testcase**의 존재를
  요구했다. 현재 규칙이면 §9와 §11.3~§11.4에서 required behavior로 열거한 method가
  빠져도 같은 class의 한 method와 `tests>0`만으로 gate를 통과할 수 있다. 또한
  lines 1691~1719는 FQCN/method를 Java identifier로 제한한다고 설명하지만 script는
  non-empty만 검사하고 그 문법을 실제 검증하지 않는다.
- **사람에게 미치는 영향:** 구현자가 필수 boundary/negative/security method를
  삭제하거나 이름을 틀려도 같은 owner class의 다른 test 한 건이 실행되면 Phase
  evidence가 green으로 봉인될 수 있다. Correction이 없애려던 method-level
  false-green이 manifest completeness 단계에 남는다.
- **Target 위치/anchor:** WP-01.7 `Required test manifest`와 verifier
  (lines 1648~1726); §11.3 test class/method table; §11.4 required fixture;
  §11.6 pass items 4~8; §14.5.
- **Source evidence:** 이 review의 원 `HG-P01-005` required correction;
  canonical Phase 01 review `F-P01-003`; canonical Phase 01 §9.2/§9.4;
  Master Realization Plan §8~§9.
- **Root cause:** Report loop의 fail-fast 동작은 고쳤지만 required test catalog와
  manifest 사이의 완전성 계약을 “class당 대표 method 하나”로 축소했다.
  사람이 작성한 manifest 자체가 required catalog를 빠뜨렸는지 판정하는
  source-of-truth가 없다.
- **Required correction:** Target에서 required test의 유일한 catalog를 정하고,
  manifest가 그 catalog의 모든 required class/method row를 정확히 포함하도록
  생성 또는 양방향 set-equality 검사를 요구한다. §9/§11에서 단순 candidate와
  exit-required method를 명확히 나누고, exit-required method는 하나도 대표
  method로 대체하지 않는다. Verifier가 engine/module/FQCN/method row grammar,
  duplicate와 unknown engine을 실제 검사하게 하고 missing class, missing method,
  zero, failed/error/skipped, wrong engine, stale report와 malformed/omitted manifest
  row 각각의 negative self-test 및 exit code를 evidence에 봉인한다. Target은
  reviewer가 수정하지 않는다.
- **Target 수정 필요 여부:** `YES`
- **남은 risk:** Accepted module/FQCN, parameterized testcase naming과
  Surefire/Failsafe XML contract가 바뀌면 catalog/verifier를 같은 identity로
  version-up하고 다시 negative-characterize해야 한다.

### HG-P01-006 — Correction 01 heading 변경으로 원 review의 GFM fragment 2개가 깨짐

- **Severity:** `LOW`
- **Finding:** Correction 뒤 target 자체와 correction report의 link는 유효하지만,
  이 원 review에 이미 봉인된 target link 중
  `#61-2026-07-29-실제-상태`와 `#62-목표-상태` 두 fragment가 더는 target heading에
  존재하지 않는다. Correction이 §6을 HEAD baseline/live/target 세 절로 나누며
  heading slug를 바꿨지만 이전 slug compatibility anchor를 남기지 않았다.
- **사람에게 미치는 영향:** 원 finding의 정확한 inventory/목표 위치로 이동하는
  traceability link가 404 fragment가 되어 correction 전후 비교와 후속 감사가
  불필요하게 어려워진다. 의미나 runtime gate를 바꾸지는 않는다.
- **Target 위치/anchor:** 현재 target §6.1 `HEAD baseline ...`, §6.2
  `Correction live snapshot ...`, §6.3 `목표 상태`; 이 원 review의
  `HG-P01-001/002` Target 위치 링크.
- **Source evidence:** 원 review의 상대 link 27개/GFM fragment 13개에 대한 current
  resolver 결과 `missing target=0`, `missing fragment=2`; correction report §5.1은
  target과 correction report link만 검사했다.
- **Root cause:** Section 삽입·재번호화 때 이미 다른 immutable review가 참조하는
  기존 GFM slug를 external compatibility surface로 다루지 않았다.
- **Required correction:** Target의 의미와 현재 heading은 유지하되, 과거 link가
  의도한 새 위치 바로 앞에 중복되지 않는 explicit compatibility anchor
  `61-2026-07-29-실제-상태`와 `62-목표-상태`를 추가한다. 이후 target,
  correction report와 이 원 review를 함께 link/fragment 검사한다. 기존 review
  본문은 덮어쓰지 않는다.
- **Target 수정 필요 여부:** `YES`
- **남은 risk:** 향후 heading 재구성도 이미 봉인된 review/correction의 fragment를
  깨뜨릴 수 있다. Cross-document link set을 correction 정적 검사 범위에 계속
  포함해야 한다.

### 정적 link/GFM/fence/whitespace/manifest 재검증

| 검사 | 결과 |
|---|---|
| Corrected target local link/GFM | 62 links, 25 fragments; missing target/fragment `0/0` |
| Correction report local link/GFM | 38 links, 26 fragments; missing target/fragment `0/0` |
| 원 review append 전 local link/GFM | 27 links, 13 fragments; missing target `0`, missing fragment `2` — `HG-P01-006` |
| Target structure/fence | H1 1개, §1~§17, WP-01.0~01.7 + 01.1A/01.3A; fence 86개 balanced |
| Target whitespace/conflict/EOF | trailing whitespace 0, conflict marker 0, EOF newline `PASS` |
| Target `git diff --check -- <target>` | exit 0. Target이 untracked인 한계를 아래 no-index 검사로 보완 |
| Target `git diff --no-index --check /dev/null <target>` | whitespace diagnostic 0; content difference의 expected exit 1 |
| Manifest shell syntax | target lines 1696~1719을 `bash -n`으로 검사해 `PASS` |
| Actual Maven discovery | root parent는 Surefire `3.5.4`, `failIfNoTests=false`; Failsafe 설정 없음. Target의 `*Test`/Surefire와 approved `*IT`/Failsafe 조건 분리는 실제 상태와 일치 |
| Manifest completeness | `FAIL`; 모든 required method와 manifest row의 set-equality가 없어 `HG-P01-005` OPEN |
| Maven/test execution | `NOT_RUN`; entry blocked이고 이 read-only review의 evidence가 아님 |

Whitespace 검사는 target 하나만 대상으로 다시 수행했고 target, correction report,
코드/POM/test와 다른 문서는 수정하지 않았다. 이 절 append 뒤 review 파일 자체의
trailing whitespace, fence, EOF와 scoped `git diff --check`도 별도로 재검사한다.

### Recheck verdict

원 finding 5건 중 `HG-P01-001`~`004`는 실제 target에서 닫혔다.
`HG-P01-005`의 required-method manifest completeness가 남고 correction이 만든
GFM traceability regression `HG-P01-006`도 target 수정이 필요하므로 최종
재검증 verdict는 **`FURTHER_CORRECTION_REQUIRED`**다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG-P01-001,HG-P01-002,HG-P01-003,HG-P01-004
OPEN_FINDINGS: HG-P01-005,HG-P01-006
TARGET_HASH_RECHECKED: 142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e
CORRECTION_REPORT_HASH_RECHECKED: 16cb6c7caa246a93a301cdbc74e796ed4122e92e24cd83a7f98217f398688792

## Correction 02 읽기 전용 재검증

```yaml
recheck_round: "02"
recheck_kind: ORIGINAL_REVIEWER_READ_ONLY_CORRECTION_RECHECK
recheck_at: 2026-07-29T03:01:04+09:00
scope: OPEN_HG-P01-005_AND_HG-P01-006_ONLY
target_sha256_rechecked: 0afb6c603cb3f9ef3c507ec8b7ba6f76566d926e25ff2332ce178058ef86dee2
target_git_blob_rechecked: 17331fec983df8edcfb581be7c075f40489b5b4e
target_lines_rechecked: 2892
correction_report_sha256_rechecked: 17575034724655d800e55e441d9a185822ca3a680d86c78e60e3585220ad475b
correction_report_git_blob_rechecked: fc4006ec1ba470f816c1024713d44e9056197eb6
correction_report_lines_rechecked: 259
review_sha256_before_append: 5125ef9430f685686d4eba0656276e9198fc9f3a6fe430bb05215e92f1c2ae0a
inventory_branch: codex-implementation
inventory_head: 7cc890ee1d0805df5ae14b633127fade4f978639
target_modified_by_reviewer: false
correction_report_modified_by_reviewer: false
implementation_or_build_modified_by_reviewer: false
maven_or_project_test_executed: false
```

### 재검증 입력과 방법

직접 읽은 고정 입력은 다음과 같다.

| 입력 | 재검증 SHA-256 | 사용 범위 |
|---|---|---|
| Corrected target | `0afb6c603cb3f9ef3c507ec8b7ba6f76566d926e25ff2332ce178058ef86dee2` | WP-01.7, §11.3, §11.6, §12.4, §14.5와 §6 compatibility anchor |
| Correction 02 report | `17575034724655d800e55e441d9a185822ca3a680d86c78e60e3585220ad475b` | 수정 위치를 찾는 index로만 사용; 자기 판정은 evidence로 채택하지 않음 |
| 이 원 review의 round 02 append 전 bytes | `5125ef9430f685686d4eba0656276e9198fc9f3a6fe430bb05215e92f1c2ae0a` | Round 01의 `HG-P01-005/006` root cause와 required correction |
| Correction 01 report | `16cb6c7caa246a93a301cdbc74e796ed4122e92e24cd83a7f98217f398688792` | 이전 correction과 round 01 판정의 traceability |

검증은 다음 순서로 수행했다.

1. Target의 marker 사이 catalog와 Python verifier block을 bytes 그대로 추출했다.
2. Catalog row/class/duplicate/engine/grammar와 명시 digest를 독립 계산했다.
3. `/tmp` 격리 directory에서 추출 verifier를 직접 실행해 exact positive/negative
   exit vector를 재현했다. Repository의 Java/POM/test와 `target/`은 사용하거나
   변경하지 않았다.
4. 실제 reactor에서 `adapters/common`, 13개 proposed test class와 Failsafe
   configuration이 아직 없는지 확인하고, target이 이를 승인된 identity처럼
   사용하는지 점검했다.
5. Target, Correction 01/02와 이 원 review의 local link/GFM fragment를 다시
   resolve해 legacy §6 link가 실제 복구됐는지 확인했다.

### Finding 재판정 요약

| Finding | Severity | Round 01 status | Round 02 status | Target 추가 수정 |
|---|---:|---:|---:|---:|
| `HG-P01-005` | `MEDIUM` | `OPEN` | `RESOLVED` | `NO` |
| `HG-P01-006` | `LOW` | `OPEN` | `RESOLVED` | `NO` |

### HG-P01-005 — RESOLVED

**확인한 target anchor**

- WP-01.7 lines 1651~1905의 catalog/manifest/verifier/exit contract
- §11.3 lines 2262~2359의 candidate 분리와 유일
  `phase01-exit-required-tests-v1` catalog
- §11.6 lines 2416~2455의 final pass gate
- §12.4 `E-P01-ERROR` evidence와 §14.5 exit checklist

**Catalog 독립 판정**

Marker와 fence delimiter를 제외한 catalog content를 직접 추출한 결과는 다음과 같다.

| 항목 | 실제 결과 | 판정 |
|---|---:|---:|
| Row | `51` | 명시값과 일치 |
| Distinct `(module,FQCN)` class | `13` | 명시값과 일치 |
| Exact duplicate row | `0` | `PASS` |
| Engine | `SUREFIRE` only | 현재 proposed catalog와 일치 |
| Grammar defect | `0` | 추출 verifier의 positive parse로 확인 |
| Catalog SHA-256 | `471183deffe508e429ffddf8c4c825bb5c25d4d7b103f4f8935f5e48122859b2` | Target 명시 digest와 일치 |

§11.3의 표와 fixture 이름은 명시적으로 `CANDIDATE`로 분리됐고 marker 사이 51개
row만 유일한 exit-required catalog다. Catalog row 하나를 같은 class의 대표 method로
대체할 수 없으며, 승인 뒤 manifest는 이 catalog에서 파생되어야 한다.

**양방향 exact-set과 verifier 독립 실행**

Verifier는 catalog와 manifest 각각의 duplicate/grammar를 먼저 닫고,
공통 `(module,FQCN,method)`의 engine mismatch를 별도 판정한 뒤
`set(catalog) == set(manifest)`를 요구한다. 따라서
`catalog − manifest`와 `manifest − catalog`가 모두 비어야 한다.

추출한 verifier를 새 process로 실행한 실제 결과는 다음과 같다.

| Control | 실제 exit | 기대 |
|---|---:|---:|
| Exact catalog=manifest + fresh reports | `0` | `0` |
| Unknown engine `JUNIT` | `64` | `64` |
| 3-field row | `65` | `65` |
| 5-field row | `65` | `65` |
| Empty field | `65` | `65` |
| Illegal module | `65` | `65` |
| Illegal FQCN | `65` | `65` |
| Illegal method | `65` | `65` |
| Missing final LF | `65` | `65` |
| Malformed XML | `65` | `65` |
| Duplicate row | `66` | `66` |
| Omitted required class | `67` | `67` |
| Omitted required method row | `67` | `67` |
| Extra manifest row | `67` | `67` |
| Wrong engine for same identity | `68` | `68` |
| Missing class report | `69` | `69` |
| Wrong testcase `classname` | `69` | `69` |
| Suite tests `0` | `70` | `70` |
| Failure count `1` | `71` | `71` |
| Error count `1` | `72` | `72` |
| Skipped count `1` | `73` | `73` |
| Missing required base method | `74` | `74` |
| Report not newer than run marker | `75` | `75` |

모든 control이 target의 고정 vector와 일치했다. Report path는 engine별
Surefire/Failsafe directory와 exact FQCN filename을 사용하고, XML 내부
`classname`, suite count, failure/error/skipped와 catalog의 모든 base method를
검사한다. 한 class의 다른 test가 required method를 대신하는 round 01
false-green은 닫혔다.

**PROPOSED module/FQCN gate**

Current reactor에는 POM 13개와 Surefire `3.5.4`/`failIfNoTests=false`만 있고
Failsafe configuration은 없다. `adapters/common`은 없으며 catalog의 13개 proposed
test class source도 모두 0개다. Target은 이를 구현 사실로 쓰지 않고 다음 gate를
명시한다.

- 전체 catalog는
  `PROPOSED — ENTRY/ARCHITECTURE/TEST IDENTITY APPROVAL REQUIRED`
- Phase 00 receipt, adapter placement와 engine/module/FQCN/method approval 전에는
  manifest 봉인과 verifier green 주장 금지
- 승인 결과가 다르면 같은 reviewed change에서 catalog version-up
- `*IT`는 approved Failsafe `integration-test`+`verify` 전에는 evidence로 사용 금지
- Catalog/verifier/self-test bytes, marker와 실제 exit vector를
  `E-P01-ERROR`에 content-addressed evidence로 봉인

따라서 없는 module/API/test를 현재 evidence처럼 쓰지 않으며 correction 문서 완료를
Phase 01 구현 또는 acceptance로 승격하지 않는다.

**남은 risk**

실제 승인 때 module/FQCN/method, parameterized testcase naming, engine 또는 report
format이 바뀔 수 있다. 이는 현재 target 결함이 아니라 아직 열리지 않은
implementation gate다. 변경 시 catalog/verifier를 같은 reviewed identity로
version-up하고 모든 control을 다시 봉인해야 한다.

### HG-P01-006 — RESOLVED

**확인한 target anchor**

- `<a id="61-2026-07-29-실제-상태"></a>`: target line 519, 현재 §6.2
  correction live snapshot 바로 앞
- `<a id="62-목표-상태"></a>`: target line 559, 현재 §6.3 목표 상태 바로 앞

두 ID는 target에 각각 정확히 한 번 존재한다. 첫 anchor는 historical HEAD §6.1이
아니라 원 link가 의도한 live inventory §6.2로, 둘째는 목표 상태 §6.3으로 이동한다.
HEAD/live/target authority 의미도 합쳐지지 않았다.

원 review를 현재 target에 대해 다시 resolve한 결과 local link 27개와 fragment
13개 모두 유효하며 missing target/fragment는 `0/0`이다. Round 01에서 관찰한 두
missing fragment가 실제로 복구됐으므로 finding을 닫는다.

**남은 risk**

향후 heading 변경도 봉인된 review/correction link를 깨뜨릴 수 있다. Target뿐 아니라
기존 review와 모든 correction report를 cross-document link 검사에 계속 포함해야 한다.

### 정적 검사 결과

| 대상/검사 | 결과 |
|---|---|
| Target local link/GFM | 62 links, 25 fragments; missing `0/0` |
| Correction 01 local link/GFM | 38 links, 26 fragments; missing `0/0` |
| Correction 02 local link/GFM | 24 links, 12 fragments; missing `0/0` |
| 원 review round 02 append 전 local link/GFM | 27 links, 13 fragments; missing `0/0` |
| Target compatibility anchor count | 두 legacy ID 각각 `1` |
| Target fence | 86개, balanced |
| Correction 02 fence | 2개, balanced |
| Target/Correction 02 whitespace·conflict·EOF | trailing whitespace `0`, conflict marker `0`, final LF `PASS` |
| Scoped `git diff --check` | exit `0` |
| Untracked 보완 `git diff --no-index --check` | 두 파일 모두 whitespace diagnostic `0`, content difference의 expected exit `1` |
| Embedded verifier syntax/execution | Python compile `PASS`; isolated vector `0,64..75` 일치 |
| Maven/project test | `NOT_RUN`; Phase 01 entry가 닫힌 문서 재검증이며 implementation evidence가 아님 |

### Round 02 verdict

`HG-P01-005`의 모든 required method catalog, manifest 양방향 exact-set,
grammar/duplicate/engine/report/staleness gate와 negative control이 실행 가능하게
닫혔고, proposed identity approval gate도 유지됐다. `HG-P01-006`의 두 legacy
fragment도 원 review link 기준으로 복구됐다.

이 `ACCEPTED`는 Correction 02가 남은 review finding을 해결했다는 뜻일 뿐
Phase 01 구현, Maven green, evidence 또는 acceptance receipt를 뜻하지 않는다.

RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG-P01-005,HG-P01-006
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: 0afb6c603cb3f9ef3c507ec8b7ba6f76566d926e25ff2332ce178058ef86dee2
CORRECTION_REPORT_HASH_RECHECKED: 17575034724655d800e55e441d9a185822ca3a680d86c78e60e3585220ad475b
