# Phase 04 사람용 구현 가이드 독립 review

```yaml
review_status: COMPLETE
review_kind: INDEPENDENT_READ_ONLY_HUMAN_GUIDE_REVIEW
phase: "04"
phase_name: capabilities-customer-profiles
reviewed_target: docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md
output: docs/implementation/human-guides/reviews/phase-04-review.md
reviewed_at: "2026-07-29T01:38:48+09:00"
review_timezone: Asia/Seoul
repository: /Users/brown/workspace/ro-next
branch: codex-implementation
head: 7cc890ee1d0805df5ae14b633127fade4f978639
target_sha256: 158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010
target_lines: 1980
target_bytes: 106470
target_head_state: ABSENT_UNTRACKED
target_modified_by_reviewer: false
implementation_reviewed: false
implementation_acceptance_claimed: false
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 4
  medium: 3
  low: 0
```

이 review는 작성자 세션과 분리된 새 작업에서 수행한 사람용 가이드 품질
review다. Target guide는 오탈자를 포함해 수정하지 않았고, 이 review 파일 외
README/progress, 다른 guide/review/correction, canonical/core Phase 문서,
Java/POM/test/deployment 파일은 수정하지 않았다.

`CHANGES_REQUIRED`는 Target guide에 교정이 필요하다는 뜻이다. 현재 live Phase 00
구현의 합격·불합격, Phase 04 구현 착수, evidence 또는 acceptance를 판정하거나
승인하지 않는다.

## 1. Review 입력, fingerprint와 inventory

### 1.1 Target fingerprint

| 항목 | Review snapshot |
|---|---|
| Path | `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md` |
| SHA-256 | `158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010` |
| Lines / bytes | `1,980` / `106,470` |
| Git state | `HEAD`에 없음, `human-guides/` 아래 untracked |
| Target 자체 조사 commit | `7cc890ee1d0805df5ae14b633127fade4f978639` |
| Target 자체 live cross-check 시각 | `2026-07-29T00:58:00+09:00` |
| Reviewer 수정 | 없음 |

Target hash는 review 시작과 종료 검증에서 같아야 한다. Target이 untracked이므로
일반 `git diff --check -- <target>`만으로 content whitespace를 검사했다고
간주하지 않고, 직접 검사와 `git diff --no-index --check`를 함께 사용했다.

### 1.2 대조 source fingerprint

아래 SHA-256은 `reviewed_at` 시점의 working-tree bytes다. `HEAD blob`은 Target이
기록한 commit-level fingerprint와의 대조값이다. Working-tree SHA-256과 `HEAD
blob`은 목적이 다르므로 서로 대신하지 않는다.

| Source | Lines | Review-time SHA-256 | `HEAD` blob / state | 직접 대조한 범위 |
|---|---:|---|---|---|
| `docs/master-design.md` | 1,648 | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | §2~§5, §7, §9, §13~§17 |
| `docs/2026-07-26-domain-design.md` | 1,886 | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | `0a02ba4c77a402455e3d80b76969dca28831b1e6` | §2~§3, §5.3, §7, §9~§10, §15~§18, §20~§21 |
| `docs/2026-07-26-architecture-design.md` | 1,019 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | `d51339e251dee1e032e711144dc63d6d07d7323b` | §1.2~§1.5, §2, §5~§6 |
| `docs/architecture-domain-implementation-design.md` | 3,822 | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | §1~§3, Phase 03~05, §19~§26, §28~§30 |
| `docs/master-design-open-questions.md` | 87 | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | exact 28개 `Q-*`, §3~§4 |
| `docs/implementation/master-realization-plan.md` | 943 | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | `d7f6be4fff0089204fbdb52f731b2348407f36eb` | §2~§4, Phase 03~05, §8~§15 |
| `docs/implementation/README.md` | 240 | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` | §0~§7 |
| `docs/implementation/execution-progress-and-results.md` | 418 | `27240b1a623d5becbdeaf415c2f483f53db39cbd65344487475423ec0f70bf45` | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` / modified | §2, §5~§10 |
| `docs/implementation/phases/phase-04-capabilities-customer-profiles.md` | 1,331 | `2e05e8a08f0e6d7c3757aac80ccb195787b88a76339da943e0873b5010f93e71` | `e0a68fd442a7db383e4a650e4a561234c323b1fe` | 전체 |
| `docs/implementation/reviews/phase-04-review.md` | 243 | `7cac66e259050347035249e622b3583faf538e1c3e0297c857ad4b2d693c460f` | `bd7d11d478f8afbd69c6336c3b653cd89ea69ac2` | 전체, 특히 F-P04-001~009와 §6 |
| `docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md` | 1,469 | `1611717ddd519cc581fe57cffc384ce6e43f7bd2185904c2aeb1fc68e26222f2` | `HEAD_ABSENT` | Phase 04 handoff, API/test/entry |
| Target Phase 04 human guide | 1,980 | `158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010` | `HEAD_ABSENT` | 전체 |
| `docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md` | 2,057 | `be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d` | `HEAD_ABSENT` | Phase 03/04 consumer, evaluator/tie/policy handoff |

Historical `docs/2026-07-26-master-design.md`는 SHA-256
`5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a`,
`HEAD` blob `d4f7fbf058711a02451de911bc86b1cd2f426218`인
`SUPERSEDED_NOT_AUTHORITY` 자료로만 cross-check했다. `docs/codex/`는 `HEAD`와
live worktree 모두에 없었다.

### 1.3 HEAD baseline과 review-time live drift

| 영역 | `HEAD` baseline | Review-time live worktree | 판정 |
|---|---|---|---|
| Maven | 단일 root application POM | POM 13개, root `packaging=pom`, `rpdptw`/`build`/`legacy` reactor | 미커밋 Phase 00 artifact, unaccepted |
| Wrapper | 없음 | executable `./mvnw`, `.mvn/wrapper` 존재 | 존재는 acceptance가 아님 |
| Stable Java | target namespace production type 없음 | `rpdptw` main Java 23개가 모두 `package-info.java`; test 0개 | Phase 01~04 의미 구현 없음 |
| Build test | 없음 | `build/architecture-rules` test Java 7개, `build/test-fixtures` test Java 2개 | Phase 00 review input |
| Legacy | root main/test 6/1 | root files는 삭제 상태, `legacy/gcp-placeholder` main/test 10/5 | 이동 중인 사용자 소유 drift |
| Phase 04 composition test owner | 없음 | `build/profile-validation` 없음 | Finding `HG-P04-R05`와 관련 |
| Phase 04 production/evidence | 없음 | non-`package-info` capability/profile/binding type 0, `E-P04-*` 0 | `NOT_STARTED/NOT_PRODUCED` |
| Progress | Phase 00 `NOT_STARTED` | Phase 00 `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW`, review `IN_PROGRESS`, receipt `NOT_PRODUCED` | Phase 00은 여전히 `NOT_ACCEPTED` |

Toolchain은 Amazon Corretto `25.0.3`, Maven `3.9.14`, macOS aarch64다. Target의
`00:58` snapshot 뒤 `execution-progress-and-results.md`가 갱신되어 Phase 00
implementation 제출과 독립 review 진행을 기록했고, architecture test source도 0개에서
7개로 늘었다. Target이 snapshot 시각과 재-inventory 규칙을 명시한 점은 옳다.
다만 Target의 실제 fingerprint 재검증 절차가 이 live drift를 검출하지 못하는 문제는
`HG-P04-R06`에서 다룬다.

## 2. Review 방법과 판정 기준

1. Target 1,980행 전체를 읽고 metadata, primer, authority, inventory, scope,
   proposed Java, WP, test, evidence, exit, handoff와 traceability를 section별로
   역추적했다.
2. Canonical Master, Final Domain/Architecture, Integrated Design, question
   register, realization plan, implementation README/progress와 canonical Phase
   04/review를 exact heading 단위로 대조했다.
3. Phase 03/05 human guide와 canonical handoff를 양방향으로 대조해 artifact
   ownership, lifecycle, full-solution evaluator, equality/tie와 portfolio policy
   gate를 확인했다.
4. `HEAD`, dirty worktree, POM/module dependency, Java/test inventory를 분리해
   Target의 current/proposed/future/accepted 표현을 확인했다.
5. Proposed Java type과 WP 순서가 core/profile-catalog/capabilities DAG에서 실제로
   생성·compile·test 가능한지 검토했다.
6. 각 WP의 entry, 행동, command, pass 판정, failure, rollback과 handoff가
   fail-closed인지 확인했다.
7. Fixture/builder/oracle independence, red→green sensitivity, fresh Surefire
   report와 missing/skipped/false-green 방지를 확인했다.
8. OPEN/GATED/deferred, Phase 13의 Phase 14A receipt + `C-17` gate와 Phase 14B
   production authority가 hidden default로 닫히지 않는지 확인했다.
9. Local link/GFM fragment, heading/fence/anchor, whitespace/tab/NUL/EOF와
   target-only diff check를 수행했다.

Severity 기준:

- `CRITICAL`: 그대로 따르면 authority/security/publication을 즉시 우회하거나
  회복 곤란한 corruption을 정상화하는 결함
- `HIGH`: core 의미, 보안 순서, module/API ownership 또는 test pass를 잘못 구현해
  중대한 재작업·false acceptance를 만들 수 있는 결함
- `MEDIUM`: 통합 evidence, fingerprint, 상태 전이 또는 재현 가능한 WP 판정을
  불완전하게 만드는 결함
- `LOW`: 의미 변경 가능성은 낮지만 링크·용어·추적성을 저하하는 결함

## 3. Finding summary

| ID | Severity | 요약 | Target 수정 |
|---|---|---|---:|
| `HG-P04-R01` | HIGH | Pre-read authorization과 failure precedence 표가 모순되어 catalog enumeration 방지 순서를 뒤집을 수 있음 | YES |
| `HG-P04-R02` | HIGH | 비의미적 descriptor 나열 순서와 의미적인 objective/stage 순서를 구분하지 않아 고객 우선순위를 canonicalization으로 지울 수 있음 | YES |
| `HG-P04-R03` | HIGH | WP-04.1의 core-owned API/type ownership과 WP-04.2의 core 변경 순서가 맞지 않아 compile 가능한 단방향 DAG가 닫히지 않음 | YES |
| `HG-P04-R04` | HIGH | Leaf `-f` Maven command가 현재 reactor의 변경된 core를 build하지 않아 fresh checkout failure 또는 stale local-repository false-green이 가능 | YES |
| `HG-P04-R05` | MEDIUM | Actual capability + catalog + bound runtime과 Phase 05 consumer를 함께 검증할 test-only composition owner/command가 없음 | YES |
| `HG-P04-R06` | MEDIUM | Source fingerprint 재확인 명령이 일부 `HEAD` blob만 출력해 모든 선언값 비교와 live drift 검출을 수행하지 않음 | YES |
| `HG-P04-R07` | MEDIUM | WP-04.6이 exit blocker가 남은 경로도 prerequisite로 허용하면서 valid receipt/handoff를 기대해 acceptance 분기가 모순됨 | YES |

Counts: `CRITICAL=0`, `HIGH=4`, `MEDIUM=3`, `LOW=0`.

## 4. Findings

### HG-P04-R01 — HIGH — Authorization pipeline과 failure precedence가 충돌한다

- **Finding:** Target §9.6은 requested scope authorization을 catalog read 전에
  수행하고 denial이면 read count 0으로 `ACCESS_DENIED`를 반환한다. 그러나 바로 뒤
  §9.7의 multi-defect precedence는 `AUTHORITY / CATALOG INTEGRITY`를
  `AUTHORIZATION / IDENTITY`보다 먼저 둔다. Unauthorized + absent/corrupt catalog
  fixture에서 이 표를 exact ordinal로 구현하면 authorization 결정보다 catalog
  existence/integrity를 먼저 읽어야 하므로 §9.6, WP-04.1과 security test의 핵심
  불변조건을 위반한다.
- **사람에게 미치는 영향:** 신규 구현자는 두 절 중 하나를 선택해야 한다. Precedence
  표를 따르면 unauthorized caller가 catalog read side effect, timing 또는 error
  category를 통해 profile 존재/schema/integrity를 열거할 수 있다. Pseudocode를
  따르면 oracle의 “expected exact code”와 production 결과가 달라져 올바른 보안
  구현을 test failure로 오판할 수 있다.
- **Target 위치:** §9.6 lines 891~909, §9.7 lines 946~960, WP-04.1 lines
  1050~1086, §11.3 `ProfileAuthorizationSecurityTest`, §12.4 safe observability.
- **Source evidence:**
  - `docs/implementation/reviews/phase-04-review.md` F-P04-004: requested
    tenant/customer scope는 catalog read 전에 authorize하고 outward denial은
    existence/schema/integrity를 노출하지 않아야 함
  - `docs/implementation/phases/phase-04-capabilities-customer-profiles.md`
    §8.1 catalog resolution과 §9.3 authorization test
  - `docs/architecture-domain-implementation-design.md` §20 security와 tenant boundary
  - Target 자체 §3.4 lifecycle: requested scope authorization → catalog lookup
- **Root cause:** Two-stage authorization correction을 pipeline에 반영하면서, 이전의
  단일 binding-validation precedence 표에는 pre-read scope authorization과
  post-verification descriptor/preset authorization을 별도 stage로 분리하지 않았다.
- **Required correction:** Cross-stage 순서를 명시적으로 분리한다. 최소한
  `request syntax/exact-reference rejection without lookup → pre-read requested-scope
  authorization → tenant-scoped lookup/integrity/preset resolution → exact verified
  descriptor+preset authorization → registry/binder validation`이어야 한다.
  Multi-defect oracle도 pre-read denial이 catalog corruption/not-found보다 항상 먼저이고
  catalog read count가 0임을 고정해야 한다. Post-verification authorization은 별도
  ordinal로 둔다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** Phase 08 이후 API mapping과 실제 catalog backend의 timing class는
  별도 security evidence가 필요하다. Phase 04 generic test만으로 provider-level
  side channel이 자동 해소되지는 않는다.

### HG-P04-R02 — HIGH — Descriptor order canonicalization이 semantic order를 지울 수 있다

- **Finding:** Target은 profile이 objective order와 stage declaration을 선택한다고
  가르치면서, §9.6에서 “descriptor 배열 순서를 authoritative order로 사용하지
  않는다”고 범위 제한 없이 선언한다. §8.2도 descriptor component order를 바꿔도
  closure/fingerprint가 같다고 요구한다. Capability/dependency의 unordered
  declaration에는 맞지만 `objective.order`, lexicographic dimension, SolvePlan stage
  order는 고객 의미 자체다. 현재 문구와 test 표에는 두 종류의 collection을
  구별하는 rule이 없다.
- **사람에게 미치는 영향:** 구현자가 모든 descriptor array를 key/version으로
  정렬하면 mandatory, total-unassigned, ownership과 remaining objective의 우선순위나
  stage 실행 의미가 바뀐다. 반대로 semantic array 순서를 fingerprint에서 제외하면
  서로 다른 profile이 같은 identity를 가질 수 있다. 결과는 deterministic해도
  잘못된 고객 목적을 최적화한다.
- **Target 위치:** §3.5 invariant 13~16, §8.2 descriptor-order 실습, §9.5
  `BoundProfile`, §9.6 마지막 문단 line 944, §11.3
  `canonicalFieldOrderDoesNotChangeDescriptorFingerprint()` 및 dependency-order tests.
- **Source evidence:**
  - `docs/master-design.md` §9.1: objective comparator는 ordered dimensions를
    소유하고 SolvePlan은 stage를 소유함
  - 같은 파일 §9.3: preset의 objective 순서와
    `mandatory → total unassigned → optional outsourced → regular → remaining`
    의미를 보존해야 함
  - `docs/architecture-domain-implementation-design.md` §8.3의
    `objective.order`, §8.4 item 8~11, §8.5 mandatory/ownership order
  - `docs/2026-07-26-domain-design.md` §10 evaluation/profile/objective
- **Root cause:** Canonical serialization의 map field order, registry insertion order,
  unordered component set과 business-semantic ordered vector/stage를 모두
  “descriptor order” 한 표현으로 합쳤다.
- **Required correction:** Descriptor schema에서 ordered와 unordered field를
  명시적으로 분류한다. Unordered capability/dependency declarations만 stable
  key/version으로 canonicalize하고, objective dimension order, stage order와 승인된
  tie order는 exact sequence를 보존하며 fingerprint에 포함한다. Map field reorder와
  unordered set permutation은 same fingerprint, objective/stage swap은 different
  fingerprint/plan/comparator가 되는 독립 tests를 추가한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** `ADR-003`이 최종 schema/canonical encoding을 승인하기 전에는
  ordered-field 목록을 production wire 약속으로 고정할 수 없다. 다만 semantic order를
  보존한다는 원칙은 지금도 FIXED다.

### HG-P04-R03 — HIGH — Core-owned contract와 WP module 순서가 닫히지 않는다

- **Finding:** Target §6.3은 `CustomerKey`, `ProfileLookupCoordinate`,
  `CapabilityRegistryView`, binding result/failure와 `BoundProfile`을
  `rpdptw-core/evaluation.api|runtime`에 둔다. §8.3도 실제 변경 순서를
  “Core-owned identity/value/failure skeletal contract → catalog”로 제시한다.
  그런데 WP-04.1의 예상 변경은 `rpdptw-profile-catalog`만 나열하면서 그 단계의
  handoff를 “Core-owned verified immutable selection contract”라고 부른다.
  WP-04.2에서야 “Core profile API extension”을 수행한다. 이 순서대로면 WP-04.1
  test가 아직 없는 core API에 의존하거나, profile-catalog에 임시/중복 type을 만든 뒤
  core로 이동해야 한다.
- **사람에게 미치는 영향:** 구현자는 core가 profile-catalog type을 import하는
  금지 dependency를 만들거나, 동일 identity/failure type을 두 module에 복제하거나,
  WP-04.1을 compile할 수 없어 WP 순서를 자의적으로 바꾸게 된다. Phase 05/07의
  core-only consumer handoff도 어떤 type이 authority인지 알 수 없다.
- **Target 위치:** §6.3 proposed tree lines 493~539, §8.3 lines 687~700,
  §9.1 dependency, WP-04.1 lines 1039~1094, WP-04.2 lines 1108~1161.
- **Source evidence:**
  - `docs/architecture-domain-implementation-design.md` §3.4:
    `rpdptw-core`가 evaluation SPI/runtime, profile-catalog가 versioned descriptor,
    schema, authorization metadata를 소유
  - 같은 파일 §3.5: capabilities와 profile-catalog는 각각 core에만 의존하며
    core의 역의존 금지
  - `docs/2026-07-26-architecture-design.md` §2.2~§2.6
  - `docs/implementation/phases/phase-04-capabilities-customer-profiles.md`
    §6 proposed file tree와 §7.5 dependency direction
  - Live `rpdptw/profile-catalog/pom.xml`: `rpdptw-core` compile dependency
- **Root cause:** Domain contract creation과 catalog implementation을 교육 단계에서는
  구분했지만 WP의 exact change target과 build order에 그 구분을 반영하지 않았다.
- **Required correction:** WP-04.1을 명시적 substep으로 나누거나 번호를 재정렬한다.
  먼저 core-owned identity/selection/failure/API를 `rpdptw-core`에 추가하고 core
  contract tests를 통과시킨 뒤, profile-catalog가 그 API를 구현·생성하게 해야 한다.
  WP별 exact module/file/type owner, compile dependency와 handoff를 일치시키고 임시
  duplicate type 또는 core→catalog edge를 금지한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** Exact type/name은 `ADR-003`, Phase 03/04 API review 전까지
  proposed다. 교정은 이름을 승인하는 것이 아니라 owner와 dependency 순서를
  실행 가능하게 만드는 것이다.

### HG-P04-R04 — HIGH — Leaf Maven command가 current core freshness를 증명하지 않는다

- **Finding:** WP-04.1/2/3/4/5의 selected command는
  `-f rpdptw/profile-catalog/pom.xml` 또는
  `-f rpdptw/capabilities/pom.xml`로 leaf project만 연다. 두 live POM은
  `${project.version}`의 `rpdptw-core`를 compile dependency로 가진다. `-f`는 parent
  POM을 `relativePath`로 읽어도 sibling core를 reactor project로 build하지 않는다.
  따라서 clean/isolated local repository에서는 core artifact 미해결로 실패하고,
  개발자 local repository에 snapshot이 있으면 현재 WP에서 수정한 core가 아니라
  stale installed core를 사용할 수 있다. Leaf의 `clean`은 core output도 지우지 않는다.
- **사람에게 미치는 영향:** 같은 source에서 fresh machine은 실패하고 오래 사용한
  machine은 통과할 수 있다. 더 위험하게는 WP-04.1/2의 새 core API와 profile/capability
  implementation이 서로 다른 snapshot에 대해 compile/test되어도 fresh Surefire
  report만 보고 green으로 오판할 수 있다.
- **Target 위치:** WP-04.1 command lines 1072~1079, WP-04.2 lines 1135~1147,
  WP-04.3 lines 1209~1216, WP-04.4 lines 1280~1287, WP-04.5 lines
  1346~1355, §11.6 false-green 판정.
- **Source evidence:**
  - Live `rpdptw/profile-catalog/pom.xml` lines 14~20:
    `com.ronext:rpdptw-core:${project.version}`
  - Live `rpdptw/capabilities/pom.xml` lines 14~20: 같은 core dependency
  - Live `rpdptw/pom.xml` lines 16~23: core/capabilities/profile-catalog sibling reactor
  - `docs/architecture-domain-implementation-design.md` §3.3:
    보통 root reactor에서 build하며 하위 POM은 dependency 경계를 선언
  - `docs/implementation/master-realization-plan.md` §8~§9:
    exact command/environment/toolchain과 reproducible evidence
- **Root cause:** Upstream module의 nonmatching `-Dtest` false failure를 피하려고
  leaf `-f`를 선택했지만, 그 과정에서 current reactor dependency materialization과
  isolated repository provenance를 잃었다.
- **Required correction:** Phase 00 accepted reactor/plugin 정책에 맞춰 current
  core를 반드시 같은 source snapshot에서 materialize하는 exact procedure를
  제시한다. 예를 들면 isolated Maven repository에 parent/core를 먼저 clean install한
  뒤 leaf exact-test command를 실행하거나, reactor에서 upstream을 함께 build하면서
  exact target-method 누락만 target module에서 fail-closed하는 module-specific
  Surefire 설정을 사용한다. 어느 방식이든 local repository path/digest, source
  commit, selected module graph와 exact method manifest를 evidence에 넣고 stale
  snapshot을 허용하지 않아야 한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** Phase 00 implementation review가 진행 중이므로 최종 wrapper/plugin
  command는 아직 authority가 아니다. Target correction은 현재 command를 확정값으로
  두지 말고 Phase 00 receipt에 따라 치환·검증하는 절차를 포함해야 한다.

### HG-P04-R05 — MEDIUM — Cross-module composition과 downstream compile probe의 test owner가 없다

- **Finding:** Target은 Module integration을 필수로 분류하고 actual
  capability registry, profile-catalog binder, `BoundProfile`과 Phase 05 core-only
  consumer를 함께 검증한다고 주장한다. 그러나 profile-local tests는
  `rpdptw-profile-catalog/src/test`에서 core interface와 test doubles만 사용할 수 있고,
  capabilities와 profile-catalog production POM은 서로 의존하지 않는다.
  WP-04.6의 sibling `clean verify`는 두 module을 각각 build할 뿐 실제
  capability+catalog composition을 호출하지 않는다. `Phase 05 core-only consumer
  compile probe`도 file owner와 command가 없다.
- **사람에게 미치는 영향:** 각 module unit test와 full reactor가 모두 green이어도
  실제 registry 구현을 catalog binder에 주입할 수 없는 signature/version/service
  mismatch, distribution assembly 오류 또는 downstream concrete dependency leakage가
  처음 Phase 05/08에서 발견될 수 있다.
- **Target 위치:** §6.3 proposed tree, §8.4 integration 완료 신호, WP-04.5
  lines 1317~1361, WP-04.6 lines 1383~1418, §11.5 Module integration,
  exit checklist §14.5.
- **Source evidence:**
  - `docs/architecture-domain-implementation-design.md` §3.2:
    `build/profile-validation`을 descriptor schema/fingerprint validation owner로 제안
  - 같은 파일 §3.5: production capabilities/profile-catalog sibling dependency 금지,
    distribution composition root만 둘을 조립
  - `docs/2026-07-26-architecture-design.md` §2.2와 §2.5~§2.7
  - Live inventory: `build/profile-validation` 없음,
    capabilities/profile-catalog는 각각 core만 의존
  - `docs/implementation/master-realization-plan.md` §8.2 Module/Architecture/Contract evidence
- **Root cause:** Production DAG를 지키는 것과 두 sibling implementation을 한 test
  process에서 조립하는 것을 구분하지 않았고, integration test를 “full reactor build”
  자체로 대체했다.
- **Required correction:** Production edge를 추가하지 않으면서 두 sibling을 조립할
  승인된 test-only owner를 명시한다. Integrated Design의
  `build/profile-validation`, 별도 test-only composition module 또는 approved
  distribution contract fixture 중 하나를 Phase 00/Architecture review에 따라
  선택하고, test-scope dependency와 exact command를 제시한다. Actual provider
  registry→catalog binder→core bound contract, semantic fingerprint equality와
  Phase 05 core-only compile probe를 그 owner에서 실행하며 production reverse edge
  0을 architecture test로 증명해야 한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** 새 test module 자체가 아직 approved/created되지 않았다. Approval
  전에는 이 integration evidence를 `NOT_PRODUCED`로 유지하고 sibling build green을
  대체 evidence로 사용하면 안 된다.

### HG-P04-R06 — MEDIUM — Fingerprint 절차가 live source drift를 fail-closed하지 않는다

- **Finding:** Target §4.2는 14개 source의 `HEAD` blob을 선언하지만 재확인 명령은
  `HEAD`와 Phase 04 상세/review 두 blob만 출력한다. 선언값과 actual을 비교해
  non-zero로 실패하지 않고, 나머지 source를 검사하지 않으며, `git rev-parse
  HEAD:<path>`는 working-tree modification을 볼 수 없다. WP-04.0의
  `git diff --check -- <human-guide>`도 whitespace 검사일 뿐 “Source drift 0”을
  판정하지 않는다.
- **사람에게 미치는 영향:** Canonical 의미, progress entry 상태 또는 인접 handoff가
  live 수정되어도 guide의 fingerprint check는 성공할 수 있다. Review 시점 실제로
  `execution-progress-and-results.md`의 `HEAD` blob은 Target 값 `250aa...`인 채
  working bytes가 SHA-256 `27240b...`로 바뀌고 Phase 00 상태도 진전했지만, Target의
  세 명령은 이 drift를 검출하지 않는다.
- **Target 위치:** metadata `source_fingerprint_scheme`,
  §4.2 lines 324~360, §5.1 item 12, WP-04.0 lines 985~1018,
  §12.2 evidence source fingerprint.
- **Source evidence:**
  - `docs/implementation/execution-progress-and-results.md` live §10.1:
    Phase 00 `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW`, review `IN_PROGRESS`,
    receipt `NOT_PRODUCED`
  - 해당 파일의 `HEAD` blob `250aa90...` 대 live Git blob
    `a6933848a0ac004d56bd4d23fb31d4084531784a`
  - `docs/implementation/master-realization-plan.md` §12.1 change control과
    §9 evidence provenance
  - Target §4.2 자체: hash가 바뀌면 heading/Java/test/evidence 영향을 review해야 함
- **Root cause:** Commit-level immutable authority fingerprint와 implementation-start
  live status/inventory fingerprint를 한 재확인 절차로 취급하고, 표를 사람이
  눈으로 비교할 것이라고 가정했다.
- **Required correction:** 모든 선언 source에 대해 expected `HEAD` blob을
  machine-readable하게 비교하고 mismatch면 non-zero로 끝나는 check를 제공한다.
  별도로 `git status`, `git diff --name-only`, `git hash-object <path>` 또는
  SHA-256으로 working bytes를 snapshot하여 `HEAD baseline`과 `live drift`를
  분리한다. Drift가 있으면 무조건 “0이어야 한다”고 끝내지 말고 changed
  heading→requirement→WP/test/evidence impact review와 re-baseline approval를
  기록한 뒤에만 진행한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** 공유 checkout의 병렬 작업은 snapshot 직후에도 바뀔 수 있다.
  Implementation start와 pre-review evidence seal 직전에 다시 검사해야 한다.

### HG-P04-R07 — MEDIUM — Exit blocker가 남은 WP-04.6 acceptance 경로가 모순된다

- **Finding:** WP-04.6 prerequisite는 residual blocker가 “승인되거나 명시적으로
  exit를 막는 상태”이면 진입할 수 있다고 한다. 그러나 같은 WP의 기대 결과는
  independent review와 valid receipt이고, 다음 handoff는 Phase 05/07 consumer
  manifest 제출이다. Full-solution evaluator/equality/portfolio blocker가
  exit-blocking으로 남으면 canonical DoD상 receipt와 accepted handoff는 발행할 수
  없으므로 한 WP 안에 `BLOCKED/CHANGES_REQUIRED` 경로와 `ACCEPTED` 경로가 섞여 있다.
- **사람에게 미치는 영향:** 구현자는 blocker를 manifest에 적었다는 사실만으로
  review/receipt 단계까지 정상 완료됐다고 해석하거나, 반대로 valid generic
  Phase 04 evidence까지 폐기해야 한다고 오해할 수 있다. Scheduler가
  `REVIEW_PENDING`, `BLOCKED`, `CHANGES_REQUIRED`, `ACCEPTED` 중 무엇을 기록할지도
  불명확하다.
- **Target 위치:** WP-04.6 prerequisite lines 1377~1381, concrete action
  lines 1391~1399, expected lines 1413~1418, rollback/handoff lines 1420~1426,
  §14.7 DoD.
- **Source evidence:**
  - `docs/implementation/reviews/phase-04-review.md` §6.2: 세 residual blocker는
    Phase 04 exit를 막는 cross-Phase contract
  - `docs/implementation/master-realization-plan.md` §9.2~§9.3:
    review가 exit를 통과하지 못하면 acceptance receipt를 발행하지 않음
  - 같은 파일 §10~§11: `REVIEW_PENDING/BLOCKED/FAILED`와 `ACCEPTED` 분리
  - `docs/implementation/phases/phase-04-capabilities-customer-profiles.md`
    §12.1 exit gate와 §13 blocker ledger
- **Root cause:** Evidence 봉인/독립 review 준비는 blocker가 남아도 할 수 있다는
  사실과, acceptance receipt/handoff authority는 blocker 해제 뒤에만 가능하다는
  상태 분기를 하나의 expected path로 축약했다.
- **Required correction:** WP-04.6을 최소 두 branch로 명시한다.
  `blocker unresolved`이면 immutable pre-review evidence와 review report까지만
  만들고 verdict/state를 `CHANGES_REQUIRED` 또는 `BLOCKED`로 남기며 receipt와
  accepted handoff를 금지한다. `all exit blockers resolved + tests complete`일 때만
  review PASS → receipt(M,R) → accepted handoff를 허용한다. 각 branch의 산출물,
  status, rollback과 resume condition을 따로 적는다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** 세 cross-Phase contract의 owner/API가 아직 승인되지 않았다.
  Target 문구를 고쳐도 그 blocker 자체가 해제되지는 않는다.

## 5. Finding 없는 검사 근거

| 검사 축 | 판정과 근거 | 남은 조건 |
|---|---|---|
| 원문 의미와 Phase 04 핵심 경계 | Capability code와 customer profile data, exact binding, core rule 보존, Phase 03 materialization-only 경계는 authority와 일치 | `R02`의 ordered field 구분 필요 |
| 신규 Java/CVRPTW 독자 배경 | Vehicle qualification/evaluation/operator namespace, descriptor/registry/binder/lifecycle과 작은 예가 충분히 설명됨 | Proposed type을 approved API로 승격하지 말 것 |
| Identity와 lifecycle | Lookup coordinate/schema/content, no overwrite/latest/fallback, problem-bound immutable runtime, no post-bind lookup이 명확 | Actual encoding은 `ADR-003` 필요 |
| Scope와 인접 Phase ownership | Phase 03 physical/evaluation, Phase 05 insertion/portfolio, Phase 07 verifier, Phase 08+ catalog/storage/runtime 경계를 대체로 보존 | `R03`, `R05`의 executable module/test owner 교정 필요 |
| OPEN/GATED/deferred | `ADR-003/004`, `P-04`, `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip과 production authority를 hidden default로 닫지 않음 | Gate owner 승인 전 구현/official claim 금지 |
| Phase 13/14 gate | Phase 14A ALNS receipt와 별도 `C-17`, Phase 14B production authority를 분리하며 OR-Tools-free Phase 04를 요구 | Receipt가 생겨도 Phase 13/14B 자동 승인 아님 |
| Entry/last safe point | Phase 00~03 receipt와 scheduler/ADR/cross-Phase gate를 AND로 두고, 현재 허용 범위를 문서/oracle/review 준비로 제한 | Live Phase 00은 아직 receipt 없음 |
| Failure/rollback | WP별 failure 해석과 last accepted Phase 03/green WP rollback, same-identity overwrite 금지가 있음 | `R07` state branch 교정 필요 |
| Security/observability | Two-stage auth, non-enumerating outward result, allowlist/redaction, telemetry 비의미성이 강함 | `R01` precedence 교정 필요 |
| Reproducibility | Stable closure, registry/backend/thread permutation, semantic fingerprint와 repeated/parallel equality를 요구 | `R02`, `R04`, `R06` 교정 필요 |
| Fixture/builder/oracle | Test-only literal builder, production helper 비사용 oracle, one-field corruption과 faulty-double sensitivity가 명확 | Cross-module actual composition은 `R05` |
| Red → green | Compile red와 assertion sensitivity red를 구분하고 minimal faulty implementation을 요구 | Actual test/evidence는 아직 0 |
| False-green/pass 판정 | Fresh `clean`, exact method manifest, failed/error/skipped/missing 0, stale report 금지가 명시됨 | `R04` leaf dependency freshness가 닫혀야 함 |
| Evidence DAG | Implementation evidence → pre-review manifest → independent review → receipt의 단방향 구조가 명확 | `R07` unresolved blocker branch 분리 필요 |
| 문서와 실제 구현 혼동 | Metadata와 전 절에서 proposed/future/test-only, `NOT_STARTED/NOT_PRODUCED/NOT_ACCEPTED`를 구분 | Snapshot 뒤 live drift는 acceptance가 아님 |
| Legacy 11-phase 혼동 | Canonical Phase 00~14 총 15개를 일관되게 사용하고 legacy 11-phase 표현 0건 | 없음 |
| 복붙 코드 과다/추상성 | Java는 skeletal/proposed로 표시되고 핵심 interface/record 수준에 한정됨. WP는 목적·행동·검증·rollback을 가짐 | 실행 계약 결함은 `R03~R05` |
| Local link/GFM | Local Markdown link 73개, fragment 44개를 actual path/heading/explicit anchor에 대조해 missing 0 | Source heading 변경 시 재검사 |
| 용어/traceability | Source→requirement→WP→test/evidence 표와 exact `Q-*`/`C-17` 용어가 있음 | Finding correction 뒤 trace table 영향 갱신 |

## 6. Target correction과 별개인 actual implementation blockers

아래 항목은 Target finding이 아니라 현재 실제 구현/권위 blocker다. 이 review가
수정하거나 해제하지 않는다.

| Blocker | Review-time 상태 | Last safe point | Restart condition |
|---|---|---|---|
| Phase 00 acceptance | Implementation 제출, independent review `IN_PROGRESS`, receipt 없음 | 보존된 live artifact + HEAD baseline | Independent review PASS, valid receipt, scheduler transition |
| Phase 01~03 accepted handoff | Production semantic type/test/evidence/receipt 없음 | Authority docs와 accepted Phase 00 artifact | 각 Phase evidence/review/receipt |
| `ADR-003` | OPEN | Exact/no-fallback semantic fields와 test-only schema design | Schema/encoding/registry/duplicate/auth approval |
| Full-solution evaluator | Cross-Phase blocker | Phase 03 route kernel + ordered declarations + immutable routes/bank | Exact API/identity/reuse/failure/comparator와 reciprocal evidence |
| Business equality/tie | Cross-Phase blocker | Ordered business vector, objective-first | Equality/tie owner/order/fingerprint와 law tests |
| Traversal/`CLOCK`/utilization | Cross-Phase authority blocker | Policy role only, missing `CLOCK → UNAVAILABLE` | Reference vector, typed edge policy, golden trace |
| `ADR-004` facet | OPEN, facet-only | Empty facet + unapproved rejection | Typed state와 Phase 03/07 recomputation approval |
| Production customer profile | Not supplied | Empty production catalog + test-only fixture | Exact Product/Security-approved descriptor/default/auth/parameter |
| Phase 13 | `C-17 GATED` | ALNS-only path | Phase 14A receipt + separate backend/legal/security/operations/cost approvals |
| Phase 14B production | Authority not granted | Benchmark/local evidence only | Official values, Phase 11, shadow/rollback/operations와 explicit authority |

Current wrapper/reactor, Phase 00 submission bundle, canonical Phase 04 document,
future commands 또는 이 document review는 위 blocker를 대신하지 않는다.

## 7. 링크·GFM·whitespace 정적 검사

Target 파일 하나를 대상으로 다음을 확인했다.

| 검사 | 결과 | Evidence |
|---|---|---|
| Relative Markdown path | PASS | 링크 73개, missing file 0 |
| GFM fragment/explicit anchor | PASS | fragment link 44개, missing 0 |
| Heading structure | PASS | heading 81개, level jump 0, duplicate base slug 0 |
| Fence parity | PASS | fence line 84개, 짝수 |
| Trailing whitespace | PASS | match 0 |
| Tab | PASS | match 0 |
| NUL | PASS | 0 byte |
| EOF newline | PASS | final byte LF |
| Legacy 11-phase pattern | PASS | match 0 |
| `git diff --check -- <target>` | PASS | exit 0; Target이 untracked라 content 열거에는 불충분 |
| `git diff --no-index --check /dev/null <target>` | PASS | whitespace diagnostic 0; 새 파일 diff 자체의 exit 1은 정상 |

## 8. 최종 verdict

`CHANGES_REQUIRED`

Target 수정이 필요한 finding은 7개다. 특히 pre-read authorization precedence,
semantic objective/stage order, core/profile API ownership과 Maven dependency
freshness를 바로잡기 전에는 신규 구현자가 안전하고 재현 가능한 Phase 04 구현을
수행할 수 없다.

Target guide는 수정하지 않았다. 이 review는 문서 품질 판정일 뿐 Phase 04
implementation/evidence/acceptance 판정이 아니다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=4 MEDIUM=3 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG-P04-R01,HG-P04-R02,HG-P04-R03,HG-P04-R04,HG-P04-R05,HG-P04-R06,HG-P04-R07

## Correction 01 읽기 전용 재검증

```yaml
recheck_round: "01"
rechecked_at: "2026-07-29T02:46:27+09:00"
recheck_mode: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
head: 7cc890ee1d0805df5ae14b633127fade4f978639
branch: codex-implementation
target_sha256_rechecked: c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c
target_lines_rechecked: 2259
correction_report_sha256_rechecked: fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c
correction_report_lines_rechecked: 335
original_review_sha256_before_append: 4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e
implementation_execution: NOT_RUN_READ_ONLY_RECHECK
```

### 재검증 범위, 입력과 hash

Correction report의 `COMPLETE_SELF_VERIFIED`와 각 `PASS` 자기주장을 판정 근거로
사용하지 않았다. 고정한 corrected target bytes에서 원 finding의 root cause와
required correction을 다시 추적하고 canonical/original/adjacent source, 현재 POM과
live inventory를 직접 대조했다.

핵심 artifact:

| Path | 재검증 SHA-256 | 상태 |
|---|---|---|
| `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md` | `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` | corrected target, read-only |
| `docs/implementation/human-guides/corrections/phase-04-correction-01.md` | `fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c` | correction report, read-only |
| `docs/implementation/human-guides/reviews/phase-04-review.md` | `4897e7717f9ea3e2e5cea19e2f1319892457a05d2056cdf0f9efe5cb4fb6316e` | 이 절 append 전 원 review bytes |

Canonical/original/implementation 입력의 재검증 시 working-byte SHA-256:

| Path | SHA-256 |
|---|---|
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| `docs/implementation/execution-progress-and-results.md` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` |
| `docs/implementation/phases/phase-04-capabilities-customer-profiles.md` | `2e05e8a08f0e6d7c3757aac80ccb195787b88a76339da943e0873b5010f93e71` |
| `docs/implementation/reviews/phase-04-review.md` | `7cac66e259050347035249e622b3583faf538e1c3e0297c857ad4b2d693c460f` |
| `docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md` | `46945bd0b4c2d65d43ea078d5562e048133986983d11c5b62ecfb8a9f3bd7b54` |
| `docs/implementation/reviews/phase-03-review.md` | `9bc1cd0a866f96971d40eed2b454e67a7626f53ad34ca5d6c66e9f7b9434961e` |
| `docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md` | `9df624f8ba93d8de69f3f60e8cf3d7f74f7ac0e9c14fc7d99514bf6154595533` |
| `docs/implementation/reviews/phase-05-review.md` | `cef56504d6f42e2ec0ea3a9e69579ab50b6f999677007822ad292ef7d309bfd7` |
| `docs/2026-07-26-master-design.md` | `5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a` |

Adjacent human guide와 live Maven 입력:

| Path | SHA-256 |
|---|---|
| `docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md` | `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3` |
| `docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md` | `6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c` |
| `pom.xml` | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` |
| `rpdptw/pom.xml` | `b778cf45360cc78674d3525c13ad2edeb6ceca97a467058e8f3cc09a069c03d0` |
| `rpdptw/core/pom.xml` | `d8b6634ca1a38c1486550e0935058490f96694efd895fc566bbb5b8fa4baad69` |
| `rpdptw/capabilities/pom.xml` | `f547a23f2d1c8be11fc71e93d7b5db9de7505b75c612b6ee2ff8d0cbcaab0a05` |
| `rpdptw/profile-catalog/pom.xml` | `dd3dbb064b06a99bb4896d72598abb7abc9da3420ea55044c98ffec5373de7b6` |
| `build/pom.xml` | `7b09571dbdaef2a25d33508f6154a6237e8ce07fc03f6b2d8a25d7abf70b3c49` |
| `build/architecture-rules/pom.xml` | `3f4ff694dc616651c739ef50b3b746ff11c23d1e9f23e3f5680d52eb6d5fbc14` |

Target §4.2가 선언한 expected `HEAD:<path>` Git blob 17개는 안전한
`source_file` 변수로 재실행했을 때 모두 현재 `HEAD`와 일치했다. 별도 live
snapshot에서는 `execution-progress-and-results.md`만 expected HEAD blob
`250aa90ae568a6b32ec905fa5ee456d430ff72cf`와 달랐고, working Git object는
`0419f69199b3140dd44020f78278b1352e6517b8`였다. 현재 progress는 Phase 00
`REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS / NOT_ACCEPTED`, acceptance receipt
`NOT_PRODUCED`다. 이는 target의 `02:22:42` snapshot 뒤 concurrent live drift이며
Phase 04 entry 또는 acceptance를 열지 않는다. 현재 Phase 05 human guide도 correction
report가 기록한 이전 hash 뒤 다시 바뀌었지만 여전히 Phase 04 accepted receipt를 AND
gate로 요구하고 `BLOCKED / NOT_ACCEPTED`다.

Live inventory는 root/rpdptw/build/legacy POM 13개, `rpdptw` main Java 23개 전부
`package-info.java`, `rpdptw` test Java 0개, `build` test Java 12개였다.
`build/profile-validation`은 여전히 없고 `./mvnw`는 executable이다.
`rpdptw-capabilities`와 `rpdptw-profile-catalog`은 각각
`${project.version}`의 `rpdptw-core`에 의존하고 서로 production 의존하지 않는다.
이 skeleton과 build test 증가는 미커밋 Phase 00 work이며 accepted Phase 04 구현,
test 또는 evidence가 아니다.

### Finding별 판정

#### HG-P04-R01 — RESOLVED

- **확인한 target anchor:** §9.6 lines 981~1,031, §9.7 lines 1,048~1,070,
  WP-04.1 lines 1,140~1,220, §11.3 lines 1,698~1,708, §12.4.
- **Source evidence:** canonical Phase 04 review F-P04-004, canonical Phase 04
  §8.1/§9.3, integrated design §20.
- **판정:** Target은
  `syntax/exact-reference no lookup → requested-scope pre-read authorization →
  tenant lookup/integrity/preset → verified descriptor+preset authorization →
  registry/binder`를 하나의 stable ordinal로 고정했다. Pre-read denial은
  not-found/corruption/missing preset보다 먼저이며 catalog read count 0이고,
  post-verification authorization은 별도 stage다. Multi-defect method와 safe
  observability도 같은 순서를 검사한다. 원 root cause와 required correction이
  실제 anchor에서 닫혔다.
- **남은 risk:** Provider backend의 timing class와 Phase 08 outward API mapping은
  실제 security evidence가 필요하다. 이는 target correction이 아니라 implementation
  blocker다.

#### HG-P04-R02 — RESOLVED

- **확인한 target anchor:** §8.2 lines 748~768, §9.6 lines 1,034~1,046,
  WP-04.2 lines 1,240~1,248, WP-04.3 lines 1,316~1,339, §11.3 lines
  1,705/1,717~1,719.
- **Source evidence:** Master §9.1/§9.3, integrated design §8.3~§8.5, dated
  Domain §10.
- **판정:** Keyed map, unordered declaration set, dependency DAG, ordered
  objective, ordered SolvePlan stage와 승인된 tie sequence를 서로 다른 semantic
  kind로 분리했다. Map/set permutation은 same fingerprint이고 objective/stage swap은
  different fingerprint/plan/comparator이며, 미분류 collection은 `ADR-003` 전
  fail-closed다. 원문의 semantic-order 소실 가능성이 제거됐다.
- **남은 risk:** Exact schema, ordered-field 이름과 canonical wire encoding은
  `ADR-003` 승인 전 `OPEN/PROPOSED`다.

#### HG-P04-R03 — RESOLVED

- **확인한 target anchor:** §6.3 lines 562~623, §8.3 lines 770~782, §9.1
  lines 810~834, WP-04.1 lines 1,140~1,220.
- **Source evidence:** integrated design §3.4~§3.5, dated Architecture
  §2.2~§2.6, canonical Phase 04 §6/§7.5, live core/capabilities/profile-catalog
  POM.
- **판정:** WP-04.1이 core-owned identity/selection/result/failure/binding
  skeletal contract와 core contract test를 먼저 만들고, 그 뒤 catalog가 동일
  contract를 생산하도록 exact substep을 갖는다. Duplicate catalog authority와
  `core → catalog` edge도 명시적으로 금지한다. Proposed tree, dependency 그림,
  action, test와 handoff가 같은 module DAG를 가리킨다.
- **남은 risk:** Exact Java type/signature는 Phase 03/04 owner와 `ADR-003` 승인 전
  public/accepted API가 아니다.

#### HG-P04-R04 — RESOLVED

- **확인한 target anchor:** 각 WP future command, 특히 WP-04.1 lines
  1,187~1,203, §11.6 lines 1,800~1,871, §12.2 Maven evidence manifest.
- **Source evidence:** live sibling POM dependency, integrated design §3.3,
  master realization plan §8~§9.
- **판정:** 새 isolated Maven repository에서 같은 source의
  `core,capabilities,profile-catalog -am clean install`을 먼저 수행하고, 같은
  repository/source에서 leaf exact `clean test`, approved composition verify와 root
  `clean verify`를 수행하도록 교정됐다. Local-repository path/inventory digest,
  selected graph, command별 fresh Surefire XML과 exact method manifest,
  failed/error/skipped/missing/zero-test/stale report 거부 조건도 있다. Leaf `-f`가
  current core를 임의의 기존 local snapshot에서 소비하던 원 결함은 닫혔다.
- **남은 risk:** Phase 00 receipt가 wrapper/reactor/plugin policy를 아직 승인하지
  않았으므로 명령은 올바르게 `future template`이다. 이 read-only recheck는 Maven
  build/test를 실행해 Phase 04 green을 주장하지 않았다.

#### HG-P04-R05 — RESOLVED

- **확인한 target anchor:** §6.3 lines 607~623, §9.1 lines 822~834,
  WP-04.5 lines 1,444~1,524, §11.3 lines 1,755~1,756, §11.5 lines
  1,783~1,798, §14.5.
- **Source evidence:** integrated design §3.2/§3.5, dated Architecture
  §2.2/§2.5~§2.7, master realization plan §8.2, live inventory.
- **판정:** Proposed test-only `build/profile-validation`이 actual
  registry → catalog provider → core binding engine → `BoundProfile` 조립과
  semantic fingerprint equality를 소유한다. 별도 fixture를 `rpdptw-core` only
  restricted classpath로 compile하는 Phase 05 probe와 production reverse-edge 0
  검사도 exact owner/method/command에 연결됐다. Owner가 absent/unapproved이면
  integration evidence를 `NOT_PRODUCED`로 두고 sibling build로 대체하지 않는다.
- **남은 risk:** Live module은 실제로 absent이고 Phase 00/Architecture 승인도 없다.
  이는 correction 미완료가 아니라 명시적으로 보존된 implementation blocker다.

#### HG-P04-R06 — OPEN

- **확인한 target anchor:** §4.2 lines 335~430, WP-04.0 lines
  1,095~1,134, §11.6 source/evidence fields, §14.1.
- **Source evidence:** master realization plan §9/§12.1, live progress의 HEAD
  blob 대 working-byte drift, current shell `zsh`.
- **확인된 개선:** 모든 expected source의 TSV와 mismatch/missing non-zero contract,
  별도 live status/diff/Git-object/SHA-256 snapshot, start와 pre-review seal의
  이중 검사, heading→requirement→WP/test/evidence impact review와 owner rebaseline은
  원 required correction의 의미를 반영한다. 안전한 변수명으로 재실행하면 expected
  HEAD blob 17개가 모두 일치하고 live progress drift도 검출된다.
- **남은 root cause:** 두 실제 code block 모두 loop 변수명으로 `path`를 사용한다.
  이 저장소의 현재 shell인 zsh에서 lowercase `path`는 `$PATH`와 연동된 특수
  array다. Target의 expected-manifest snippet을 그대로 `zsh -c`로 실행하면 첫
  `read ... path`가 명령 검색 경로를 덮어써 `git: command not found`, exit 41로
  끝난다. 같은 one-row snippet은 `bash -c`에서 exit 0이지만, target은 사용자가
  ambient zsh에 paste하지 못하도록 explicit `bash` invocation을 제공하지 않는다.
  Live snapshot의 `for path in ...`도 같은 문제를 가진다. 따라서 올바른 source
  set에서도 통과할 수 있는 재현 가능한 machine-readable procedure가 아직 아니다.
- **사람 영향:** macOS/zsh 구현자는 모든 hash가 일치해도 source mismatch로
  오판해 구현을 영구 차단하거나, 실패 원인을 피하려 manifest 검사를 건너뛸 수 있다.
- **Required correction:** 두 block의 변수명을 zsh/bash 모두에서 안전한
  `source_file`/`source_path`로 바꾸고 exact snippets를 supported shell에서
  success/missing/mismatch/live-drift fixture로 실행 검증한다. 대안으로 shell을
  명시적으로 고정한다면 copy-paste 가능한 `bash -eu -o pipefail` invocation,
  지원 shell 계약과 exit-code evidence를 함께 제공해야 한다. Target 수정 필요:
  **YES**.
- **남은 risk:** 변수 교정 뒤에도 공유 checkout의 TOCTOU drift는 남으므로
  implementation start와 evidence seal의 재검사는 계속 필요하다.

#### HG-P04-R07 — RESOLVED

- **확인한 target anchor:** WP-04.6 lines 1,530~1,622, §12.3 lines
  1,913~1,925, §14.6 lines 2,056~2,063, §14.7과 §15.2.
- **Source evidence:** canonical Phase 04 review §6.2, master realization plan
  §9.2~§9.3/§10~§11, canonical Phase 04 §12.1/§13.
- **판정:** Branch A는 unresolved blocker에서 immutable manifest와 independent
  `CHANGES_REQUIRED/BLOCKED` review까지만 허용하고 receipt, `ACCEPTED`와 accepted
  Phase 05/07 handoff를 모두 금지한다. Branch B는 모든 blocker 해소와 complete
  evidence 뒤에만 review `PASS → receipt(M,R) → ACCEPTED handoff`를 허용한다.
  두 branch의 artifact/status/rollback/resume와 다음 handoff가 각각 닫혔다.
- **남은 risk:** Full-solution evaluator, equality/tie와 portfolio policy의 owner/API가
  아직 승인되지 않아 현재 실제 경로는 Branch A다. Target correction과 별개다.

### Correction 01이 만든 NEW finding

#### HG-P04-C01-N01 — MEDIUM — Correction report가 존재하지 않는 question ID를 authority처럼 기록한다 — OPEN

- **Finding:** Correction report line 287은 `Q-CAP-001~003`의 상태를 보존했다고
  주장한다. Target, canonical Master와 exact question register 전체에는
  `Q-CAP-001`, `Q-CAP-002`, `Q-CAP-003`이 없고, register의 실제 집계는
  `RESOLVED 26 / OPEN — EXPERIMENT_REQUIRED 1 / DEFERRED 1`이다. Target 자체의
  관련 open contract는 `ADR-003`, `ADR-004`, `P-04`, `Q-BENCH-02`,
  `Q-VAR-01` 등으로 올바르게 기록돼 있다.
- **사람 영향:** Audit consumer가 존재하지 않는 세 질문에 owner, decision 또는
  resume evidence가 있다고 오해하고 exact register traceability를 깨뜨릴 수 있다.
- **Root cause:** 다른 capability 질문 식별자를 Phase 04 canonical question
  register와 대조하지 않고 correction report의 “보존한 경계” 목록에 복사했다.
- **Required correction:** Correction report line 287의 비존재 ID를 제거하고,
  실제 보존한 target 상태를 exact canonical ID/ADR로만 다시 기록한다. 새 question이
  정말 필요하다면 이 report에서 발명하지 말고 별도 authority 절차와 owner 승인을
  거쳐 register에 추가한다. Target 수정 필요: **NO**; correction report 수정 필요:
  **YES**.
- **Residual risk:** Corrected target의 실행 지침에는 이 ID가 없어 runtime/Phase
  boundary는 직접 변하지 않지만 correction audit의 신뢰성과 향후 재검증 자동화가
  손상된다.

### 정적 link/GFM/fence/whitespace/manifest 재검증

| 검사 | Target | Correction report | 판정 |
|---|---:|---:|---|
| Local Markdown link | 83, missing path 0 | 25, missing path 0 | PASS |
| Local fragment/GFM anchor | 52, missing 0 | 23, missing 0 | PASS |
| Fence 밖 heading | 81, level jump/duplicate base slug 0 | 19, 0/0 | PASS |
| Fence | 94 line, closed | 2 line, closed | PASS |
| Trailing whitespace/tab | fence 밖 0/0 | 0/0 | PASS |
| TSV용 tab | expected-source fenced TSV 안 17 | 0 | 의도된 delimiter |
| NUL/final LF | 0/LF | 0/LF | PASS |
| Legacy 11-phase pattern | 0 | 0 | PASS |
| `git diff --no-index --check /dev/null <file>` | whitespace diagnostic 0 | whitespace diagnostic 0 | PASS; untracked-file diff exit 1은 정상 |
| Expected HEAD manifest content | 안전한 변수명으로 17/17 match | N/A | 값과 범위 PASS |
| Target manifest exact execution | zsh exit 41, bash exit 0 | self-claim PASS | **FAIL — HG-P04-R06** |

Manifest/Maven 판정은 future command를 실행해 나온 green이 아니다. POM dependency와
reactor selector, isolated repository, `clean install/test/verify`, fresh report,
exact method, missing/zero/skipped/stale 판정 계약을 정적으로 대조했다. 현재
Phase 00~03 receipt, Phase 04 Java/test, approved composition owner가 없으므로 실제
Maven execution을 Phase 04 evidence로 만들 수 없다.

### 재검증 결론

원 finding 7개 중 `HG-P04-R01`, `R02`, `R03`, `R04`, `R05`, `R07`은 target의
정확한 anchor에서 root cause와 required correction이 닫혔다. `HG-P04-R06`은
HEAD/live 역할 분리와 manifest 범위는 교정됐지만 현재 지원 환경에서 exact command가
실행되지 않아 OPEN이다. Correction report의 비존재 `Q-CAP-001~003` 표기는 별도
NEW finding `HG-P04-C01-N01`로 OPEN이다.

Target guide, correction report, POM/code/test, README/progress와 다른 review는 수정하지
않았다. 이 append는 사람용 guide correction 판정이며 Phase 04 implementation,
evidence, review PASS 또는 acceptance를 뜻하지 않는다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG-P04-R01,HG-P04-R02,HG-P04-R03,HG-P04-R04,HG-P04-R05,HG-P04-R07
OPEN_FINDINGS: HG-P04-R06,HG-P04-C01-N01
TARGET_HASH_RECHECKED: c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c
CORRECTION_REPORT_HASH_RECHECKED: fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c

## Correction 02 읽기 전용 재검증

```yaml
recheck_round: "02"
rechecked_at: "2026-07-29T02:58:37+09:00"
recheck_mode: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
head: 7cc890ee1d0805df5ae14b633127fade4f978639
branch: codex-implementation
target_sha256_rechecked: a717d366bd2b54a16f1103b115df8d55f881252d1465fd9bf8cc5df0510ceb2b
target_lines_rechecked: 2283
correction_02_report_sha256_rechecked: 9b5ba92c4f4a0719c39b5600ce13093f0ce46ea8151917ffc8f881dae53545a2
correction_02_report_lines_rechecked: 307
review_sha256_before_append: 441905d73b9c50eed5beba7880af4c3ca44bf285a27e38e265b769ccb1b278d6
historical_correction_01_sha256_rechecked: fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c
question_register_sha256_rechecked: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
```

### 범위와 독립 검증 방법

Correction 02의 `COMPLETE_SELF_VERIFIED`, shell 결과표와 set-comparison 자기주장을
판정 근거로 그대로 사용하지 않았다. 고정한 target §4.2의 두 shell fence를 직접
추출해 bash/zsh에서 실행했고, canonical question register 28개 row와 target의
range-expanded Q-ID를 독립 set으로 다시 계산했다. Correction 01은 historical
read-only artifact로 유지하고 hash가 Round 01과 같은지 확인했다.

재검증 입력:

| Artifact | SHA-256 | 상태 |
|---|---|---|
| `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md` | `a717d366bd2b54a16f1103b115df8d55f881252d1465fd9bf8cc5df0510ceb2b` | corrected target, read-only |
| `docs/implementation/human-guides/corrections/phase-04-correction-02.md` | `9b5ba92c4f4a0719c39b5600ce13093f0ce46ea8151917ffc8f881dae53545a2` | correction 02 report, read-only |
| `docs/implementation/human-guides/corrections/phase-04-correction-01.md` | `fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c` | historical bytes unchanged |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | canonical register, read-only |
| `docs/implementation/human-guides/reviews/phase-04-review.md` | `441905d73b9c50eed5beba7880af4c3ca44bf285a27e38e265b769ccb1b278d6` | 이 절 append 전 bytes |

### HG-P04-R06 — RESOLVED

- **확인한 target anchor:** §4.2 lines 360~445, 특히 expected manifest
  lines 362~394와 live snapshot lines 399~445; WP-04.0과 §14.1의 start/seal
  fail-closed gate.
- **안전한 변수명:** 두 loop 모두 `source_file`을 사용한다. Target은 zsh의
  `$path` 특수 배열과 충돌하지 않는 이유를 명시하고, 실제 두 shell에서 command
  search path가 유지됐다. Lowercase `path` loop 변수는 남아 있지 않다.
- **독립 재현 결과:**

| Fixture | Target로부터 만든 입력 | bash | zsh | 판정 |
|---|---|---:|---:|---|
| Success | Expected manifest fence 원문 17행 | `0` | `0` | 17/17 `HEAD:<source_file>` match |
| Missing | 같은 fence의 첫 source path만 존재하지 않는 fixture로 변경 | `41` | `41` | missing/unresolvable HEAD source |
| Mismatch | 같은 fence의 첫 expected blob만 40자리 `0`으로 변경 | `42` | `42` | expected/actual inequality |
| Live drift | Live snapshot fence 원문 | `43` | `43` | working bytes와 expected HEAD 불일치 |

- **Live-drift evidence:** 두 shell 모두
  `docs/implementation/execution-progress-and-results.md`에서 expected HEAD blob
  `250aa90ae568a6b32ec905fa5ee456d430ff72cf`, working Git object
  `9ebfc9825931121bd12f65980653dc25263ab211`, working SHA-256
  `c98cddecc7eb0265b835bfa0ea76dfef2f414369c96276914b2d9ca6d1d8c395`와
  modified status를 출력하고 exit `43`으로 끝났다.
- **Root-cause closure:** Round 01의 원인은 올바른 manifest도 zsh에서 항상 exit
  `41`이 되던 특수변수 충돌이었다. 현재 target은 bash/zsh 양쪽의 success와
  missing/mismatch/live-drift를 서로 다른 exit로 재현하며, expected HEAD와 live
  working bytes 역할도 계속 분리한다. Required correction이 실제 anchor에서
  닫혔다.
- **Residual risk:** Snapshot 직후 concurrent TOCTOU drift는 남는다. Target이
  요구하는 implementation start와 pre-review evidence seal의 이중 검사와
  changed-heading impact review/rebaseline gate를 계속 적용해야 한다. 이는 OPEN
  correction finding이 아니다.

### HG-P04-C01-N01 — RESOLVED

- **확인한 anchor:** Historical correction 01 line 287/§5, correction 02
  lines 118~142와 machine-readable supersession lines 144~266, canonical question
  register §2~§4, target의 question/ADR references.
- **Historical immutability:** Correction 01 SHA-256은 Round 01과 같은
  `fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c`다.
  잘못된 과거 문장을 지우거나 소급 수정하지 않았다.
- **독립 register set 결과:** Canonical row는 정확히 28개이고 status는
  `RESOLVED 26`(`Q-ALG-02`의 `RESOLVED — KEEP_COW` 포함),
  `OPEN — EXPERIMENT_REQUIRED 1`(`Q-BENCH-02`),
  `DEFERRED 1`(`Q-VAR-01`)이다. Target의 range-expanded exact Q-ID는 11개이며
  모두 register subset으로 `target-minus-register=0`이다.
- **Invalid claim 확인:** `Q-CAP-001`, `Q-CAP-002`, `Q-CAP-003`은 register와
  target 양쪽 모두에 없다. Correction 02는 source file, line 287, exact historical
  hash와 claim을 지정하고 `INVALID_SUPERSEDED`,
  `HISTORICAL_AUDIT_CLAIM_ONLY`, owner/decision/resume evidence `null`,
  replacement ID 0으로 기록한다.
- **Root-cause closure:** 존재하지 않는 ID를 실제 authority처럼 읽게 하던 audit
  ambiguity가 immutable supersession record와 실제 authoritative ID/status list로
  닫혔다. 새 ID를 발명하거나 OPEN/GATED/deferred 상태를 승격하지 않았다.
- **Residual risk:** Correction 01만 고립해 읽는 사람은 과거 문장을 볼 수 있으므로
  audit consumer는 correction 02의 supersession chain을 함께 소비해야 한다. 역사
  artifact immutability를 보존한 결과이며 추가 correction을 요구하지 않는다.

### 정적 재검증

| 검사 | Target | Correction 01 | Correction 02 | Append 전 review |
|---|---:|---:|---:|---:|
| Local Markdown path / missing | 83 / 0 | 25 / 0 | 4 / 0 | 0 / 0 |
| GFM fragment / missing | 52 / 0 | 23 / 0 | 3 / 0 | 0 / 0 |
| Fence 밖 heading | 81 | 19 | 9 | 33 |
| Heading jump / duplicate base slug | 0 / 0 | 0 / 0 | 0 / 0 | 0 / 0 |
| Fence line / closed | 94 / yes | 2 / yes | 6 / yes | 4 / yes |
| Fence 밖 trailing whitespace/tab | 0 / 0 | 0 / 0 | 0 / 0 | 0 / 0 |
| CRLF / NUL / final LF | 0 / 0 / yes | 0 / 0 / yes | 0 / 0 / yes | 0 / 0 / yes |
| No-index whitespace diagnostic | 0 | 0 | 0 | 0 |

Target의 tab 17개는 expected-source fenced TSV delimiter이고 fence 밖 tab은 0이다.
`git diff --check -- <target> <correction-02> <review>`는 exit 0이었다. 네 파일은
untracked이므로 `/dev/null`과의 no-index raw exit 1은 content difference이며,
whitespace diagnostic은 모두 0이었다.

### Round 02 결론

`HG-P04-R06`과 `HG-P04-C01-N01`은 모두 **RESOLVED**다. 이 판정은 사람용 guide와
correction audit의 두 OPEN finding만 닫는다. Phase 00~03 receipt, cross-Phase
evaluator/equality/portfolio authority, ADR/owner 승인, 실제 Phase 04
implementation/test/evidence/review/acceptance는 여전히 별도 blocker이며 이
재검증이 해제하지 않는다.

Target, correction 01/02, question register, code/POM/test, README/progress와 다른
review는 수정하지 않았다.

RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG-P04-R06,HG-P04-C01-N01
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: a717d366bd2b54a16f1103b115df8d55f881252d1465fd9bf8cc5df0510ceb2b
CORRECTION_REPORT_HASH_RECHECKED: 9b5ba92c4f4a0719c39b5600ce13093f0ce46ea8151917ffc8f881dae53545a2
