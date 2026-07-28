# Phase 05 사람용 구현 가이드 독립 리뷰

```yaml
review_status: COMPLETE
review_type: INDEPENDENT_HUMAN_IMPLEMENTATION_GUIDE_REVIEW
review_date: 2026-07-29
reviewer_role: independent Phase 05 human-guide reviewer
target: docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md
target_sha256: be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d
target_lines: 2057
target_git_state_at_review: UNTRACKED
source_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
source_branch: codex-implementation
source_mode: PINNED_HEAD_BLOBS_PLUS_LIVE_WORKTREE_INVENTORY
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 1
  medium: 2
  low: 0
  total: 3
required_correction_findings:
  - HG-P05-001
  - HG-P05-002
  - HG-P05-003
implementation_blockers_are_target_findings: false
```

## 1. 결론

대상 가이드는 Phase 05의 핵심 의미를 대체로 정확하게 보존한다. Complete pair,
same concrete vehicle, exactly once, pickup-before-delivery, route-bank XOR,
delivery-only initial-load ownership, delegated feasibility, immutable construction,
최대 8개 독립 seed와 Phase 06 seed-only handoff가 canonical source와 정합한다.
원본 Phase 05 review의 residual blocker 세 개도 hidden default로 닫지 않았고,
Phase 13/14A/14B gate와 실제 구현·evidence·acceptance 상태를 분리했다.

그러나 구현자가 그대로 실행할 수 있어야 하는 사람용 가이드로서는 다음 세 교정이
필요하다.

1. Solver/test-fixtures/architecture-rules selected-test 명령이 fresh local repository에서
   sibling reactor artifact를 해소하지 못한다.
2. HEAD baseline만 보는 fingerprint와 현재 inventory 서술이 이미 존재하는 live
   Phase 00 reactor/wrapper 및 scheduler 상태를 놓친다.
3. Hand oracle이 두 feasible option의 business equality를 fixture로 고정하지 않고
   stable tie 선택을 unconditional pass 기준으로 둔다.

따라서 문서 verdict는 `CHANGES_REQUIRED`다. 이 verdict는 Phase 05 구현 blocker가
존재한다는 이유로 내린 것이 아니라, 대상 가이드 자체에서 고쳐야 할 실행성·현재성·
oracle 판정성 문제 세 건에 대한 판정이다. Phase 05의 구현 상태는 별도로 계속
`BLOCKED / NOT_ACCEPTED / NOT_PRODUCED`다.

## 2. 검토 기준과 metadata

### 2.1 Target와 source fingerprint

Target fingerprint는 review 시작 시 실제 bytes에 대한 SHA-256이다.

| 구분 | Path | Fingerprint |
|---|---|---|
| Target | `docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md` | SHA-256 `be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d`, 2,057 lines |
| Canonical Master | `docs/master-design.md` | HEAD blob `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| Final Domain | `docs/2026-07-26-domain-design.md` | HEAD blob `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| Final Architecture | `docs/2026-07-26-architecture-design.md` | HEAD blob `d51339e251dee1e032e711144dc63d6d07d7323b` |
| Integrated design | `docs/architecture-domain-implementation-design.md` | HEAD blob `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| Question register | `docs/master-design-open-questions.md` | HEAD blob `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| Master realization plan | `docs/implementation/master-realization-plan.md` | HEAD blob `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| Implementation README | `docs/implementation/README.md` | HEAD blob `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| Execution progress baseline | `docs/implementation/execution-progress-and-results.md` | HEAD blob `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| Canonical Phase 05 | `docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md` | HEAD blob `0ea8046142a9e53cc1b9cedb198a1bdae550378b` |
| Canonical Phase 05 review | `docs/implementation/reviews/phase-05-review.md` | HEAD blob `88ecf56c57556a0ae8d3578ec158561602222c14` |
| Adjacent human guide 04 | `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md` | SHA-256 `158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010` |
| Adjacent human guide 06 | `docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md` | SHA-256 `fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f` |

Historical `docs/2026-07-26-master-design.md`, `master-design-sessions`,
`arranged`, `orgin`과 legacy GCP 구현은 authority로 사용하지 않고 regression 및
placeholder 분류에만 사용했다.

### 2.2 HEAD와 live inventory

| 항목 | Review 시 관찰 |
|---|---|
| HEAD | `7cc890ee1d0805df5ae14b633127fade4f978639`, branch `codex-implementation` |
| Root POM baseline/live | HEAD `f8a411e...` 단일 JAR placeholder / live hash-object `1dc675ba...` reactor parent |
| Progress baseline/live | HEAD `250aa90...` / live hash-object `a6933848...` |
| Wrapper | Live worktree에 `mvnw`, `.mvn/wrapper/maven-wrapper.properties` 존재 |
| Stable module skeleton | Live worktree에 `rpdptw/core`, `solver`, `verification`, `application`, `capabilities`, `profile-catalog` POM과 package skeleton 존재 |
| Build modules | Live worktree에 `build/test-fixtures`, `build/architecture-rules` 존재 |
| Legacy move | 기존 root main 6개/test 1개는 deleted 표시, `legacy/gcp-placeholder`는 untracked |
| Phase 00 scheduler state | `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW`, acceptance receipt `NOT_PRODUCED`, Phase 01 handoff `BLOCKED` |
| Phase 05 code | `evaluation/insertion`, `solver/state`, `solver/portfolio`에는 `package-info.java`만 존재 |
| Phase 05 tests/evidence | Pair/insertion/portfolio production type·test·accepted evidence는 0 |

Live reactor는 다른 작업의 unaccepted 변경이다. 이 reviewer는 이를 수정·복구·
stage하지 않았다. Phase 00 acceptance가 없고 Phase 01~04도 accepted되지 않았으므로
Phase 05 entry verdict `BLOCKED`는 변하지 않는다.

## 3. 방법

1. Canonical 5문서에서 Phase 05의 pair/service/stable-state/evaluation/portfolio,
   module DAG, failure, security, reproducibility와 gate 의미를 추출했다.
2. Implementation master plan, README, tracked baseline과 live progress를 대조해
   문서 상태와 실제 implementation 상태를 분리했다.
3. 원본 Phase 05와 원본 independent review의 12개 finding, 특히 residual
   F-P05-001/F-P05-005/F-P05-011이 target에서 보존되는지 확인했다.
4. Phase 04/06 사람용 가이드의 producer/consumer, full-solution evaluator,
   comparator/tie, central pair editor와 seed/COW ownership을 상호 대조했다.
5. HEAD와 live POM/module/source/test inventory를 읽어 Java/Maven 제안과 command의
   실제 dependency 조건을 확인했다.
6. Work package별 prerequisite/action/test/pass/failure/rollback/handoff가 사람이
   판정 가능한지 추적했다.
7. Local Markdown link/fragment, fence parity, trailing whitespace, source fingerprint,
   forbidden hidden default와 status overclaim을 정적으로 검사했다.
8. Review 작업은 Markdown 한 파일만 작성했다. Maven/test는 Phase 05 type/test가
   없고 target output을 만들 수 있으며 review scope 밖 변경을 유발하므로 실행하지
   않았다.

## 4. Severity

| Severity | 판정 기준 |
|---|---|
| `CRITICAL` | Authority·안전·state를 회복 곤란하게 손상하거나 즉시 잘못 승인하게 함 |
| `HIGH` | 핵심 구현/evidence gate를 실행 불가능하게 하거나 false-green을 허용함 |
| `MEDIUM` | 현재성, 재현성, oracle 판정 또는 handoff를 불완전하게 하지만 핵심 invariant를 직접 완화하지는 않음 |
| `LOW` | 의미를 바꾸지 않는 오탈자, 링크, 표현 또는 경미한 추적성 결함 |

## 5. Findings

### HG-P05-001 — Selected-test 명령이 fresh reactor dependency를 재현 가능하게 해소하지 못한다

- **Severity:** `HIGH`
- **Finding:** Target은 fresh full-reactor `clean verify` 뒤 selected test를 target
  module에만 `-pl`로 실행하도록 요구한다. Core 자체를 고르는 명령은 독립 실행될 수
  있지만 `rpdptw/solver`, `build/test-fixtures`, `build/architecture-rules`는 sibling
  reactor artifact에 의존한다. `verify`는 artifact를 local repository에 install하지
  않으며, 다음 별도 Maven invocation에서 `-am`이나 명시적 dependency-preparation이
  없으면 fresh repository는 sibling artifact를 해소할 수 없다.
- **사람 영향:** 구현자는 올바른 코드와 test가 있어도 dependency-resolution
  failure를 WP 실패로 오인한다. 반대로 이미 오염된 local repository에서만 성공한
  명령을 재현 가능한 evidence로 봉인할 수 있다. WP-05.1/05.3/05.4/05.5와
  architecture gate가 모두 영향을 받는다.
- **Target 위치:** §6.4 lines 569~579, WP-05.1 lines 1268~1273, WP-05.2
  lines 1332~1343, WP-05.3 lines 1397~1402, WP-05.4 lines 1464~1469,
  WP-05.5 lines 1525~1542, WP-05.6 lines 1603~1609, §12.5 lines 1748~1752.
- **Source 경로 + section:** `docs/2026-07-26-architecture-design.md` §2.2/§6.1의
  reactor DAG; live `rpdptw/solver/pom.xml` dependencies lines 14~20;
  `build/test-fixtures/pom.xml` dependencies lines 14~20;
  `build/architecture-rules/pom.xml` dependencies lines 14~58.
- **Root cause:** 원본 review의 0-test false-green 교정에서 `-am`을 제거하고 target
  module만 선택했지만, separate invocation의 reactor dependency 공급 방법을 함께
  설계하지 않았다.
- **Required correction:** Phase 00 accepted reactor에 맞춰 exact command를 다시
  고정한다. 최소한 다음을 모두 만족해야 한다.
  1. Fresh isolated local repository에서도 required sibling artifact를 같은
     invocation의 reactor 또는 명시적 dependency-preparation 단계로 공급한다.
  2. Upstream module의 test-name mismatch가 target test gate를 거짓 실패시키지 않는다.
  3. Target module의 expected class/method count가 non-zero이고
     failure/error/skipped가 모두 0인지 fresh XML로 확인한다.
  4. Dependency-preparation과 selected-test invocation의 exact command, repository
     위치와 report ownership을 evidence에 남긴다.
  Accepted reactor가 결정되기 전에는 현재 command를 “exact executable command”가
  아니라 `BLOCKED TEMPLATE`로 표시한다.
- **Target 수정:** `YES`
- **Residual risk:** Phase 00 review에서 module selector, test-jar classifier 또는
  Surefire inheritance가 바뀌면 Phase 05 명령을 다시 검토해야 한다.

### HG-P05-002 — HEAD-only fingerprint와 inventory가 현재 live reactor/status를 놓친다

- **Severity:** `MEDIUM`
- **Finding:** Target은 source fingerprint를 `git rev-parse HEAD:<path>`로만
  재확인하고, 현재 checkout에 wrapper/reactor/module이 없다고 반복한다. Review 시점의
  live worktree에는 wrapper, reactor, core/solver/test/architecture module skeleton이
  존재하고 scheduler progress도 Phase 00을 `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW`로
  갱신했다. HEAD blob 조회는 이 uncommitted source/status drift를 검출하지 못한다.
- **사람 영향:** 새 구현자는 이미 존재하는 unaccepted Phase 00 skeleton을 부재로
  판단해 중복 생성하거나, 실제 POM/DAG와 다른 future tree/명령을 준비할 수 있다.
  반대로 skeleton 존재를 Phase 00 acceptance로 오인할 수도 있다.
- **Target 위치:** Intro line 61, §3.3 lines 206~240, entry table lines 345~356,
  inventory lines 433~479, Maven current/future 구분 lines 563~579,
  마지막 판정 line 2057.
- **Source 경로 + section:** `docs/implementation/execution-progress-and-results.md`
  metadata lines 11~19, §5 Phase 00 row, §10.1 lines 370~418; live root `pom.xml`
  `<packaging>pom</packaging>`/`<modules>`; `.mvn/wrapper/maven-wrapper.properties`;
  `rpdptw/pom.xml`; `build/pom.xml`.
- **Root cause:** Reproducible committed baseline과 volatile shared-worktree inventory를
  개념적으로 구분했지만, 실제 drift gate와 현재상태 표에는 HEAD blob만 사용했다.
  작성 뒤 병행 Phase 00 작업이 진행된 사실도 final target snapshot에 반영되지 않았다.
- **Required correction:** Target에 `HEAD baseline`과 `review/use-time live inventory`
  두 열을 명시한다. Live에는 reactor/wrapper/package skeleton이
  `PRESENT_BUT_UNACCEPTED`, Phase 05 production type/test/evidence는 `ABSENT`,
  Phase 00은 `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW / NOT_ACCEPTED`라고 기록한다.
  Source 재확인 절차에는 `git status --short -- <sources>`,
  `git diff -- <sources>`와 working-tree content hash를 포함해 uncommitted drift도
  포착한다. Entry verdict는 acceptance receipt가 없으므로 계속 `BLOCKED`로 유지한다.
- **Target 수정:** `YES`
- **Residual risk:** Shared checkout은 계속 변할 수 있으므로 실제 Phase 05 task는
  시작 시 동일한 이중 inventory를 다시 봉인해야 한다.

### HG-P05-003 — Hand oracle의 stable-tie 기대값이 business equality를 fixture로 고정하지 않는다

- **Severity:** `MEDIUM`
- **Finding:** §12.1은 “business vector가 동률이면” `(0,1)`을 선택한다고
  조건부로 설명하지만 exact test matrix와 red→green 표는
  `selected (0,1)`을 unconditional pass 기준으로 둔다. Capacity와 동일한 arc
  값만으로는 승인될 full-solution objective/extension score 전체의 equality를
  보장하지 않는다. Service physical-location distinctness도 fixture에 명시되지 않아
  self arc가 순서별 travel equality를 바꿀 수 있다.
- **사람 영향:** `(0,1)`이 business comparator 때문에 선택돼도 stable tie가
  검증됐다고 false-green 처리할 수 있다. 반대로 승인된 profile이 두 feasible
  option을 다르게 평가하면 올바른 구현이 fixture 기대와 충돌한다.
- **Target 위치:** §12.1 lines 1626~1659, `PairInsertionHandOracleTest` row
  lines 1671~1675, red group 3 line 1719.
- **Source 경로 + section:** `docs/master-design.md` §9.1~§9.3과 §11.1의
  evaluation/comparator 분리; `docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md`
  §8.3/§10.2~§10.3; `docs/implementation/reviews/phase-05-review.md`
  F-P05-005.
- **Root cause:** Capacity feasibility를 증명하는 literal fixture와 stable-tie
  comparator fixture를 한 표에 결합하면서 equality authority를 명시하지 않았다.
- **Required correction:** Hand fixture에 test-only exact profile/ranking artifact
  또는 authority spy를 명시하고 두 feasible option의 ordered business vector가
  exact equal임을 먼저 assertion한다. Terminal과 네 service의 physical location
  관계도 명시한다. 그 equality assertion 뒤에만 stable position tie `(0,1)`을
  판정하며, non-tied control case가 stable key에 의해 뒤집히지 않는지도 별도로
  실행한다. Comparator API가 여전히 OPEN이면 이 selection test는
  `BLOCKED_UNTIL_COMPARATOR_CONTRACT`, capacity receipt `6/2/4`만 독립 green으로
  분리한다.
- **Target 수정:** `YES`
- **Residual risk:** Full-solution evaluator/business-equality contract가 승인될 때
  fixture artifact와 comparator owner를 다시 pin해야 한다.

## 6. 구현 blocker와 target correction의 구분

다음은 target이 정확히 보존한 기존 implementation blocker이며 위 finding count에
포함하지 않는다.

| Blocker | 현재 판정 | Target 수정 여부 |
|---|---|---|
| Ordered routes + bank full-solution evaluator/ranking owner/API | `RESIDUAL CROSS-PHASE BLOCKER` | `NO`; 임의 API로 닫지 말고 승인 대기 |
| Business equality, solution tie와 insertion-context tie owner/order/version | `RESIDUAL CROSS-PHASE BLOCKER` | `NO`; HG-P05-003은 blocker 해소가 아니라 test 판정 조건 명료화 |
| Exact traversal, CLOCK convention, utilization missing/zero policy | `RESIDUAL AUTHORITY BLOCKER` | `NO`; golden trace/reference vector/typed policy 승인 대기 |
| Phase 05/06 central pair-removal editor ownership | `RESIDUAL CROSS-PHASE BLOCKER` | `NO`; construction transition과 destroy editor 분리 유지 |
| Phase 00~04 acceptance receipt 부재 | `ENTRY BLOCKER` | `NO`; Phase 00 skeleton 존재와 acceptance를 분리 |
| Phase 05 scheduler implementation task/owner 부재 | `OWNER GATE` | `NO`; 임의 task ID 생성 금지 |

HG-P05-001~003은 위 blocker의 의미를 임의로 해결하라는 요구가 아니다. 가이드의
명령, inventory와 test oracle을 blocker 상태에서도 판정 가능하고 안전하게 만들라는
문서 교정이다.

## 7. No-finding 근거

| Review axis | 판정 | 근거 |
|---|---|---|
| Phase 의미와 invariant | `PASS` | Complete pair, same vehicle, exactly once, precedence, route-bank XOR와 delivery-only initial load를 명시 |
| 신규 독자 배경/이유 | `PASS` | CVRPTW 대비 RPDPTW 차이, identity/service pattern/stable 의미와 학습 경로가 충분함 |
| Scope와 Phase 경계 | `PASS` | Phase 03/04 authority 재사용, Phase 06 COW/ALNS, Phase 07 verifier, Phase 13 MIP를 non-scope로 분리 |
| Adjacent ownership/lifecycle | `PASS` | Phase 04 bound artifact, Phase 05 construction seed, Phase 06 removal/COW/screen의 lifecycle이 교차 가이드와 정합 |
| Proposed Java contract 표시 | `PASS WITH OPEN GATE` | Record defensive copy, sealed result, identity와 dependency 방향을 제안으로 표시하고 unresolved solution API를 compile-freeze |
| WP 순서와 사람 checkpoint | `PASS EXCEPT HG-P05-001` | Prerequisite/action/shortcut/pass/failure/rollback/handoff가 WP별로 존재 |
| Entry/exit/evidence/rollback | `PASS` | Acceptance receipt 없는 status 승격 금지와 단방향 evidence DAG를 보존 |
| Failure/corruption | `PASS` | Rejected/Invalid/bounded miss/unavailable을 분리하고 partial publish·base mutation을 금지 |
| Security/observability | `PASS` | Safe aggregate와 raw address/input/coordinate/secret/provider locator redaction을 명시 |
| Reproducibility | `PASS EXCEPT HG-P05-001/002` | Stable order, no-alias, repeat/parallel/input permutation과 fingerprint equality를 요구 |
| OPEN/GATED/deferred | `PASS` | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip, public schema와 official 수치를 숨은 default로 닫지 않음 |
| Phase 13/14 gate | `PASS` | `05→06→07→08→14A`, receipt 뒤 Phase 13 optional, 14B production authority 분리 |
| 문서/실제 구현 분리 | `PASS EXCEPT LIVE DRIFT` | 구현/evidence/acceptance를 과장하지 않으며 Phase 05 type/test 0을 인정; live Phase 00 상태만 HG-P05-002 교정 필요 |
| Fixture/builder/oracle | `PASS EXCEPT HG-P05-003` | Independent permutation/BigInteger/faulty-double/corruption 방향과 red→green 순서가 명확 |
| Test category와 pass 판정 | `PASS EXCEPT HG-P05-001` | Unit/contract/integration/architecture/fault/corruption/repro/security의 적용성과 non-zero report 기준을 분리 |
| Traceability | `PASS` | Source → requirement → WP → exact test/evidence 표가 핵심 requirement를 모두 연결 |
| Historical/current 분리 | `PASS` | Superseded master와 legacy 자료를 비권위 cross-check로만 사용 |

## 8. 정적 검사

| 검사 | 결과 |
|---|---|
| Target non-empty/hash/line | `PASS`; SHA-256과 2,057 lines metadata 일치 |
| Local Markdown links | `PASS`; target의 local link 40개에서 missing file 0 |
| GFM fragments | `PASS`; explicit fragment의 missing target 0 |
| Heading/fence | `PASS`; fenced-code marker 98개, parity even |
| Target trailing whitespace | `PASS`; match 0 |
| Source fingerprint | `PASS FOR HEAD`; target 표의 pinned HEAD blob 일치 |
| Working-tree drift | `FAIL DOCUMENT CURRENTNESS`; progress/POM live hash가 pinned HEAD와 다름 — HG-P05-002 |
| Hidden default/gate bypass | `PASS`; forbidden production value/provider/backend activation 0 |
| Phase status overclaim | `PASS`; Phase 05 `BLOCKED / NOT_ACCEPTED / NOT_PRODUCED` 유지 |
| Phase 05 production/test inventory | `PASS`; target package 세 곳은 `package-info.java`만 있고 Phase 05 type/test 0 |
| Maven command static dependency closure | `FAIL`; selected solver/test-fixtures/architecture commands에 fresh sibling resolution 없음 — HG-P05-001 |
| Oracle decisive equality | `FAIL`; stable-tie hand fixture의 business equality가 condition-only — HG-P05-003 |
| Scoped edit | `PASS`; reviewer edit는 이 report 한 파일뿐 |
| Target modification | `PASS`; target bytes/hash 불변 |
| Stage/commit/push/worktree | `PASS`; 수행하지 않음 |
| Build/test/deployment | `NOT_RUN BY DESIGN`; review 범위 밖이며 Phase 05 test 부재 |
| Scoped whitespace + `git diff --check` | `PASS`; final validation에서 target/report whitespace와 repository diff check 오류 0 |

## 9. 최종 판정

대상은 핵심 도메인 계약, Phase 경계, 사람 checkpoint와 안전 gate를 잘 보존했지만
세 required correction이 남아 있으므로 `ACCEPTED`가 아니다. Correction 작업은
target만 수정하고 canonical 문서, progress, POM, Java/test/evidence를 건드리지
않아야 한다. 이후 독립 recheck는 target 새 hash에 대해 세 finding의 closure,
live/HEAD 이중 inventory, fresh-reactor command 설계와 tie fixture equality를 다시
확인해야 한다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=1 MEDIUM=2 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG-P05-001, HG-P05-002, HG-P05-003

## Correction 01 읽기 전용 재검증

```yaml
recheck_round: 01
recheck_type: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
recheck_at: "2026-07-29T02:44:25+09:00"
recheck_scope:
  - HG-P05-001
  - HG-P05-002
  - HG-P05-003
original_review_sha256_before_append: 69e97a55633b03448f8ff5e8a60b9824ecec5c93caaf62da1449a58209de6743
target_sha256_rechecked: 6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c
target_lines_rechecked: 2317
correction_report_sha256_rechecked: bad70b45d6089e5da19cd408637a28e57a3d016de3566cf4b3ef78522de8beeb
correction_report_lines_rechecked: 224
source_commit_rechecked: 7cc890ee1d0805df5ae14b633127fade4f978639
recheck_verdict: ACCEPTED
resolved_findings:
  - HG-P05-001
  - HG-P05-002
  - HG-P05-003
open_findings: []
new_findings: []
```

### 1. 읽은 파일과 fingerprint

Correction report의 closure 주장을 판정 근거로 사용하지 않고 corrected target, 원
finding의 source와 현재 live checkout을 다시 읽었다. Hash는 이 절을 append하기
직전에 계산한 실제 bytes의 SHA-256이다.

| 역할 | Path | SHA-256 |
|---|---|---|
| 원 review, append 전 | `docs/implementation/human-guides/reviews/phase-05-review.md` | `69e97a55633b03448f8ff5e8a60b9824ecec5c93caaf62da1449a58209de6743` |
| Corrected target | `docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md` | `6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c` |
| Correction report | `docs/implementation/human-guides/corrections/phase-05-correction-01.md` | `bad70b45d6089e5da19cd408637a28e57a3d016de3566cf4b3ef78522de8beeb` |
| Canonical Master | `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| 2026-07-26 Domain | `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| 2026-07-26 Architecture | `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| Integrated design | `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| Question register | `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| Master realization plan | `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| Implementation README | `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| Live execution progress | `docs/implementation/execution-progress-and-results.md` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` |
| Original Phase 05 | `docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md` | `9df624f8ba93d8de69f3f60e8cf3d7f74f7ac0e9c14fc7d99514bf6154595533` |
| Original Phase 05 review | `docs/implementation/reviews/phase-05-review.md` | `cef56504d6f42e2ec0ea3a9e69579ab50b6f999677007822ad292ef7d309bfd7` |
| Adjacent human guide 04 | `docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md` | `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` |
| Adjacent human guide 06 | `docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md` | `719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3` |

Live Maven reality는 `pom.xml`, wrapper properties, `rpdptw` parent/core/solver POM,
`build` parent/test-fixtures/architecture-rules POM의 ordered `path + SHA-256`
manifest `8507a57df835ffa112a7ed0cb9b41deb122e5b2d5ed24fbdec94f752cc706ecd`로
대조했다. 이 inventory는 acceptance authority가 아니다.

### 2. 재검증 방법과 범위

1. 원 finding의 finding/root cause/required correction/residual risk를 그대로
   판정 기준으로 삼고 corrected target의 새 anchor를 직접 찾았다.
2. HG-P05-001은 live reactor dependency와 wrapper/POM/plugin/test-jar 구조에
   command 두 invocation을 대입해 fresh dependency resolution, upstream
   test-name 격리, target report ownership과 zero/stale-test gate를 확인했다.
3. HG-P05-002는 pinned HEAD, timestamped live snapshot, 현재 progress/status와
   source/POM/test drift 재검출 절차를 분리해 확인했다.
4. HG-P05-003은 canonical objective-first 비교 의미와 original Phase/review의
   residual comparator blocker에 capacity/equality/tie fixture를 대조했다.
5. Correction이 건드린 범위에서 invariant, entry/exit, OPEN/GATED/deferred,
   Phase 04→05→06 handoff, failure/rollback/security/observability를 회귀
   검사했다. 이 범위를 넘어 새 broad review는 수행하지 않았다.
6. Phase 05 production type/test/evidence가 0이고 Phase 00 reactor도 accepted되지
   않았으므로 Maven/test는 실행하지 않았다. Existing Phase 00/legacy test 또는
   생성 가능한 `target/`을 Phase 05 evidence로 오인하지 않았다.

### 3. Finding별 판정

| Finding | Status | Target anchor | 핵심 판정 |
|---|---|---|---|
| `HG-P05-001` | `RESOLVED` | §6.4 lines 642~690; WP command blocks lines 1384~1400, 1460~1494, 1549~1565, 1628~1644, 1703~1755, 1818~1842; §12.5 lines 2004~2015 | Fresh isolated repository dependency prep와 target-only selected evidence가 분리되고 zero/stale/manifest AND gate가 닫힘 |
| `HG-P05-002` | `RESOLVED` | metadata lines 19~34; §3.3 lines 231~316; entry line 421; inventory lines 512~524; final line 2317 | HEAD baseline과 timestamped live inventory가 분리되고 correction 이후의 추가 status drift도 fail-closed로 검출됨 |
| `HG-P05-003` | `RESOLVED` | WP-05.5 lines 1685~1686; §12.1 lines 1857~1916; §12.2 lines 1927~1929; §12.3 line 1977 | Capacity, exact business equality 뒤 tie, non-tied control이 서로 다른 판정이며 미승인 comparator selection은 blocked |

#### HG-P05-001 — RESOLVED

- **원 root cause closure:** §6.4는 upstream test-name mismatch를 피하려고 `-am`을
  제거했던 원 문제에 dependency supply 단계를 추가했다. 각 gate는 새
  `mktemp` run root와 isolated `maven.repo.local`을 만들고, 같은 repository에서
  먼저 `-pl <target> -am -DskipTests install`, 다음에 `-am` 없는 target-only
  `-pl <target> -Dtest=... clean test`를 실행한다. Prep 성공/report는 test
  evidence가 아니라고 명시한다.
- **Maven/source evidence:** `docs/2026-07-26-architecture-design.md` §2.2/§6.1의
  DAG와 live `rpdptw/solver/pom.xml`의 core dependency,
  `build/test-fixtures/pom.xml`의 core test dependency 및 `test-jar` attachment,
  `build/architecture-rules/pom.xml`의 stable module/test-fixtures classifier
  dependency를 대조했다. Install prep가 sibling artifact를 같은 isolated
  repository에 공급하고 selected invocation은 target report만 소유하는 구조다.
- **실행·manifest 판정:** Example/WP의 selected command block 10개 모두
  isolated repository, matching `-am -DskipTests install`, start marker,
  target-only exact `-Dtest`, `clean test`,
  `surefire.failIfNoSpecifiedTests=true`를 함께 가진다. False flag는 0개다.
  §6.4/§12.5는 start marker 이후 target XML, non-empty sealed
  `Class#method` expected/actual 양방향 equality, duplicate 0, `tests > 0`,
  failure/error/skipped 0과 post-report source/POM/test/status hash 불변을
  하나의 AND gate로 요구한다.
- **Entry/failure/rollback:** 명령은 Phase 00 accepted wrapper/reactor/plugin/
  classifier/report contract 전 `BLOCKED TEMPLATE`다. Dependency failure,
  zero/missing/extra/stale report와 fingerprint drift는 green이 아니라 report
  폐기와 fresh restart다.
- **남은 risk:** Phase 00 Fix 02가 selector, plugin inheritance, test-jar
  classifier나 offline bootstrap을 바꾸면 accepted receipt 기준으로 명령을 다시
  pin해야 한다. 이는 target이 보존한 외부 entry risk이며 HG-P05-001을 OPEN으로
  둘 문서 결함은 아니다.

#### HG-P05-002 — RESOLVED

- **원 root cause closure:** Metadata와 §3.3은 reproducible HEAD commit/tree/blob,
  current top-level REVIEW map, timestamped live status/tracked/untracked/content
  hash를 서로 다른 authority/observation 층으로 둔다. Reactor/wrapper/package
  skeleton은 `PRESENT_BUT_UNACCEPTED`, Phase 05 type/test/evidence는 `ABSENT`,
  acceptance receipt가 없으면 entry는 `BLOCKED`라고 명시한다.
- **Source/live evidence:** `docs/implementation/execution-progress-and-results.md`
  metadata/§5/§10.1, live root/reactor POM, wrapper와 package/test inventory를
  직접 확인했다. Correction report 시점 뒤 progress는 다시 SHA-256
  `9361ae89...`/Git blob `0419f691...`로 바뀌었고 현재 상태는
  `PHASE_00_FIX_02_IN_PROGRESS`,
  `REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS / NOT_ACCEPTED`, acceptance
  receipt `NOT_PRODUCED`다. Phase 05 명명 production type/test/evidence는 계속
  0이다.
- **추가 drift 판정:** Target은 snapshot을 영구적인 “현재”로 재사용하지 말라고
  lines 307~314에 명시하고, scheduler status 또는 snapshot 뒤
  source/POM/test/hash 변화 시 바뀐 path/before-after hash/영향/owner를 기록한 뒤
  fail-closed하도록 한다. 따라서 위 Fix 02 drift는 target의 과거 Fix 01 snapshot을
  stale로 검출하는 실제 사례이며 acceptance나 구현 가능 상태로 오인되지 않는다.
- **Authority/entry/exit:** 더 늦은 timestamp와 파일 존재는 설계/acceptance
  authority가 아니다. Phase 00~04 receipt, Phase 05 pre-review manifest,
  independent review와 post-review acceptance receipt가 없으므로
  `BLOCKED / NOT_ACCEPTED / NOT_PRODUCED`와 Phase 06 handoff 금지가 유지된다.
- **남은 risk:** Shared checkout은 계속 변한다. 실제 구현자는 시작과 evidence
  sealing 뒤 동일 범위의 status/content/source/test manifest를 다시 봉인해야 한다.
  Target이 이 재봉인을 요구하므로 현재 drift는 required correction의 미이행이 아니다.

#### HG-P05-003 — RESOLVED

- **원 root cause closure:** §12.1은 terminal `T`, `P1/D1/P2/D2`를
  pairwise-distinct physical location으로 고정하고 diagonal `0/0`, 모든
  off-diagonal `1m/1s`를 명시한다. Capacity oracle은
  `legal/evaluated/feasible/rejected = 6/6/2/4`와 feasible key 두 개만
  판정하며 selected key를 주장하지 않는다.
- **Source evidence:** `docs/master-design.md` §9.1~§9.3/§11.1의 bound
  objective/comparator 우선 의미, original Phase 05 §8.3/§10.2~§10.3과
  original review `F-P05-005`의 unresolved business-equality/tie owner를
  대조했다. Adjacent Phase 04의 bound authority와 Phase 06의 evaluator/editor/
  COW/screen ownership도 앞당겨지지 않았다.
- **Equality/tie 판정:** Test-only authority spy가 두 complete cache-free
  candidate에 같은 exact ordered artifact `B_EQ`와 equality fingerprint를
  반환하고 `businessCompare == 0`을 먼저 assertion한 뒤에만 stable position
  `(0,1)`을 판정한다. 별도 non-tied control은 `B_23`이 strictly better일 때
  stable-first `(0,1)`이 `(2,3)` 선택을 뒤집지 못하게 한다. Exact method matrix와
  red→green 표도 capacity/equality-first tie/non-reversal을 분리한다.
- **OPEN/failure 보존:** Comparator/business-equality API가
  `OPEN CROSS-PHASE`이면 selection 두 test는
  `BLOCKED_UNTIL_COMPARATOR_CONTRACT`이고 capacity test만 independent green이다.
  Test-only artifact를 production objective/default/API로 승격하지 않으며 selection
  미실행을 capacity green으로 숨기지 않는다.
- **남은 risk:** Approved full-solution comparator의 ordered vector, owner와
  identity가 정해지면 spy를 그 exact contract에 다시 pin해야 한다. 이 blocker를
  target이 숨기지 않았으므로 HG-P05-003의 required oracle correction은 닫혔다.

### 4. 보존 gate와 regression 판정

- Complete same-route pair, exactly-once, pickup-before-delivery, route-bank XOR,
  concrete-vehicle uniqueness와 delivery-only initial-load ownership은 완화되지
  않았다.
- Phase 04 bound authority → Phase 05 immutable construction seed → Phase 06
  central removal/COW/ALNS/screen lifecycle을 유지한다. Phase 05가 champion,
  destroy/repair 또는 final verification을 선점하지 않는다.
- Full-solution evaluator, business equality/tie와 central editor owner는 계속
  cross-Phase blocker다. Traversal/CLOCK/utilization은 승인 전
  `OPEN/PROPOSED`다.
- `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01`은 `DEFERRED`,
  Phase 13은 Phase 14A benchmark receipt와 별도 `C-17` 전 `GATED`, Phase 14B
  production authority는 별도 gate다. Hidden default나 gate bypass는 없다.
- Invalid/rejected/exception/stale evidence는 partial publish 없이 last accepted
  authority/artifact로 rollback한다. Raw input/address/coordinate/secret/provider
  locator를 evidence/log에 넣지 않는 보안·관측 경계도 유지됐다.
- Correction 범위에서 새 regression finding은 발견하지 않았다.

### 5. 정적 재검증

| 검사 | 결과 |
|---|---|
| Target/report local links | `PASS`; 46 + 35 = 81개, missing path 0 |
| GFM fragment | `PASS`; explicit fragment missing 0 |
| Heading hierarchy | `PASS`; target/report H1 `1/1`, heading `82/13`, level jump `0/0` |
| Fence | `PASS`; target/report marker `100/2`, 모두 closed |
| Whitespace/encoding | `PASS`; target/report trailing whitespace, tab, NUL, CR 각 0; final LF 있음 |
| Maven selected command | `PASS STATIC`; command block 10개에서 prep/marker/target-only/exact test/clean/fail-if-zero 구성 일치, false flag 0 |
| Manifest/zero-test | `PASS FAIL-CLOSED`; expected `Class#method` non-empty 양방향 equality와 XML count gate가 있음. 현재 Phase 05 test/manifest/report/evidence는 0이므로 실행 green 주장 0 |
| Stale evidence | `PASS`; target-owned marker 이후 XML만 허용하고 prep/upstream/old/IDE/console/ignored `target/` report를 배제 |
| Current live drift | `PASS FAIL-CLOSED`; Fix 02 progress drift 검출, receipt `NOT_PRODUCED`, entry/exit/handoff 미승격 |
| Maven/test execution | `NOT_RUN_BY_DESIGN`; Phase 05 test 0, Phase 00 unaccepted, read-only recheck에서 build output을 만들지 않음 |
| Scoped whitespace/`git diff --check` | `PASS`; target, correction report와 appended review의 whitespace diagnostic 0, tracked repository `git diff --check` output 0 |
| Allowed write scope | `PASS`; 원 review의 첫 306 lines hash가 append 전 SHA-256과 같고 이 절만 뒤에 추가됨 |
| Target/correction immutability | `PASS`; 최종 재계산 hash가 위 pinned hash와 같음 |
| Stage/commit/push/worktree | `PASS`; 수행하지 않음 |

원 review의 `CHANGES_REQUIRED`와 finding 본문은 당시 target
`be3487...`에 대한 역사 기록으로 유지한다. Corrected target
`6a0cc...`에 대한 Correction 01 재검증에서는 세 required finding이 모두
`RESOLVED`이며 추가 target correction은 요구하지 않는다. 이는 Phase 05 구현
acceptance가 아니라 correction 문서 품질에 대한 판정이다.

RECHECK_ROUND: 01
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG-P05-001, HG-P05-002, HG-P05-003
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: 6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c
CORRECTION_REPORT_HASH_RECHECKED: bad70b45d6089e5da19cd408637a28e57a3d016de3566cf4b3ef78522de8beeb
