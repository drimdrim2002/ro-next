# Phase 00 사람용 구현 가이드 독립 리뷰

```yaml
phase: "00"
review_kind: INDEPENDENT_READ_ONLY_HUMAN_GUIDE_REVIEW
review_scope: DOCUMENT_CONTRACT_EXECUTABILITY_AND_LIVE_INVENTORY
review_status: COMPLETE
review_date: 2026-07-29
review_timezone: Asia/Seoul
reviewed_target: ../phases/phase-00-human-implementation-guide.md
target_sha256: bf6a35990f0554ffee7206e19f18213b601164ba11830938944c298c063d5c16
target_lines: 1687
target_git_state_at_review: UNTRACKED
target_modified_by_reviewer: false
output: docs/implementation/human-guides/reviews/phase-00-review.md
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 4
  medium: 2
  low: 0
inventory_snapshot:
  observed_at: 2026-07-29T01:28:25+09:00
  repository: /Users/brown/workspace/ro-next
  branch: codex-implementation
  head: 7cc890ee1d0805df5ae14b633127fade4f978639
  head_tree: a63fa3b93ca206298ac6d467e02e1f3503cee30e
  java: "Amazon Corretto 25.0.3"
  maven: "Apache Maven 3.9.14"
implementation_reviewed: false
implementation_acceptance_claimed: false
```

이 리뷰는 구현자 세션과 분리된 문서 리뷰다. Target guide, core Phase 문서, 코드, POM,
test, deployment, progress/README 또는 다른 human guide/review는 수정하지 않았다. 이 파일의
`CHANGES_REQUIRED`는 사람용 가이드의 교정 필요를 뜻하며 현재 live Phase 00 구현의 합격·
불합격 판정이나 구현 권한 부여가 아니다.

## 1. 리뷰 입력과 fingerprint

### 1.1 필수 권위·계획·인접 입력

| 입력 | 직접 대조한 section | Review-time SHA-256 | Target 기록과의 관계 |
|---|---|---|---|
| [`docs/master-design.md`](../../../master-design.md) | §1.1~§1.5, §2.4, §4.3~§4.7, §13, §15~§17 | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 일치 |
| [`docs/2026-07-26-domain-design.md`](../../../2026-07-26-domain-design.md) | §1~§3, §7~§8, §17~§18 | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 일치 |
| [`docs/2026-07-26-architecture-design.md`](../../../2026-07-26-architecture-design.md) | §2.1~§2.7, §5.6, §6 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 일치 |
| [`docs/architecture-domain-implementation-design.md`](../../../architecture-domain-implementation-design.md) | §2~§4, §19~§25, §27~§28 | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 일치 |
| [`docs/master-design-open-questions.md`](../../../master-design-open-questions.md) | §1~§4와 `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 일치 |
| [`docs/implementation/master-realization-plan.md`](../../master-realization-plan.md) | §1~§9, §11~§15 | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 일치 |
| [`docs/implementation/README.md`](../../README.md) | §0~§7 | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 일치 |
| [`docs/implementation/execution-progress-and-results.md`](../../execution-progress-and-results.md) | §1~§10, 특히 §10.1 Phase 00 실행 registry | `0ec2ee8c7a2ee3b79a2eb51ed9d1287af4f6031dd8b20fd3bb273f3248172c61` | Target의 `37f1...`은 `HEAD` blob이며 live bytes와 불일치 |
| [Phase 00 core plan](../../phases/phase-00-build-architecture-skeleton.md) | 전체, 특히 §3, §6~§12, §14~§17 | `0ad01e21a94ac543486a53c0ed0a256b4137e673bbae1be4d959ebc46dcefd27` | 일치 |
| [기존 Phase 00 core review](../../reviews/phase-00-review.md) | 전체, 특히 `P00-R-001`~`P00-R-006` | `db4f1f8c4d178c99d82597085155a1fbfac223659944f3b092e66ae5b7f6fed8` | 일치 |
| [Target guide](../phases/phase-00-human-implementation-guide.md) | 전체 | `bf6a35990f0554ffee7206e19f18213b601164ba11830938944c298c063d5c16` | 리뷰 대상 |
| [인접 Phase 01 human guide](../phases/phase-01-human-implementation-guide.md) | metadata, §1~§6, Phase 00 entry/handoff 언급 | `1eaeb50b98c65302360631e486e7048e0b8f717415b7c5d26b725c5540043bee` | Phase 00 accepted artifact를 기다리는 consumer |

Historical `docs/2026-07-26-master-design.md`는 Target이 선언한 대로 누락·퇴행
cross-check로만 취급했고 현재 결정의 authority로 사용하지 않았다.

### 1.2 HEAD baseline과 uncommitted live drift

Target metadata의 commit은 실제 `HEAD`와 일치한다. 그러나 commit identity만으로 dirty
worktree의 bytes를 식별할 수는 없다. 리뷰 시점의 두 층은 다음과 같다.

| 영역 | `HEAD` baseline | Review-time live worktree | 판정 |
|---|---|---|---|
| Maven | root `pom.xml` 1개, implicit JAR | POM 13개, root parent와 `build`/`rpdptw`/`legacy` reactor drift | live 구현 중이며 미승인 |
| Wrapper | 없음 | `mvnw`, `mvnw.cmd`, `.mvn/wrapper`가 untracked로 존재 | Target의 “현재 사용 불가”는 HEAD에만 참 |
| Production Java | root `src/main` 6개 | root 6개는 tracked deletion, live Java main 33개(legacy 10, stable package skeleton 23) | 이동/추가 drift |
| Test Java | root `src/test` 1개 | live test Java 14개(legacy 5, architecture 7, fixture 2) | test drift |
| Progress | Phase 00 `NOT_STARTED`인 HEAD blob | §10.1 `PREREQUISITE_REMEDIATION_IN_PROGRESS`, evidence/receipt는 여전히 미생성 | 공식 live status가 더 최신 |
| Git 상태 | tree `a63fa3...` | tracked modified 5, tracked deleted 7, untracked file 85 | clean snapshot 아님 |
| Human guides | `HEAD`에 없음 | human guide 17개가 untracked | Target과 이 review는 아직 Git blob이 아님 |

Live file 존재는 Phase 00 acceptance 증거가 아니다. 공식 progress에 따르면
`E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`, 독립 구현 review와 acceptance receipt는
아직 없다. 따라서 Phase 01은 계속 blocked다.

## 2. 리뷰 방법과 기준

1. Target의 모든 절을 line-by-line으로 읽고 source authority, 15-Phase DAG, core Phase 00
   plan과 기존 core review correction을 역추적했다.
2. Java/Maven 명령, 예상 path/type, test fixture와 oracle이 `HEAD` source에서 실제로 관찰되는
   동작과 맞는지 확인했다.
3. `HEAD` tree와 dirty live inventory를 따로 계산해 Target의 `CURRENT`, source link와
   progress fingerprint를 판정했다.
4. 각 WP에 사전조건, 구체 행동, 기대 결과, 실패 해석, rollback, handoff가 있는지와
   entry/exit/evidence 단방향 DAG를 확인했다.
5. OPEN/GATED/DEFERRED, Phase 13 `C-17`, Phase 14A receipt와 14B production authority가
   hidden default 또는 shortcut으로 닫히지 않았는지 검사했다.
6. Relative link target, GFM 구조, fence, trailing whitespace, EOF와 target-only
   `diff --no-index --check`, repository `git diff --check`를 실행했다.

Severity는 다음처럼 적용했다.

- `CRITICAL`: 그대로 실행하면 gate를 우회하거나 복구하기 어려운 손상을 만드는 결함
- `HIGH`: 잘못된 artifact/behavior/evidence를 정상으로 승인할 가능성이 큰 결함
- `MEDIUM`: false-green, handoff 지연 또는 재현성 저하를 만드는 결함
- `LOW`: 의미는 유지되지만 정확성·탐색성·정적 품질을 낮추는 결함

## 3. Finding summary

| ID | Severity | Finding | Target 수정 |
|---|---|---|---:|
| `P00-HG-R-001` | HIGH | 권위 package tree와 다른 `*.core`/plural capability skeleton 및 aggregator 누락 | YES |
| `P00-HG-R-002` | HIGH | Legacy HTTP failure oracle가 endpoint별 현재 동작 대신 새 status를 발명 | YES |
| `P00-HG-R-003` | HIGH | `target/` evidence 경로와 수동 seal이 clean·immutable evidence 계약을 위반 | YES |
| `P00-HG-R-004` | HIGH | Customer identity 허용 경계를 잃은 전 stable-module 전역 금지 | YES |
| `P00-HG-R-005` | MEDIUM | `HEAD` baseline과 live drift 미분리로 inventory/status/link가 현재 checkout과 불일치 | YES |
| `P00-HG-R-006` | MEDIUM | Core ambient nondeterminism 금지 rule과 negative fixture 누락 | YES |

## 4. Finding 상세

### P00-HG-R-001 — 권위 package/file skeleton과 다른 구현 지시

**Severity:** HIGH

**Finding:** Target은 `rpdptw-core`의 production source를
`com.ronext.rpdptw.core.package-info`, capability를
`com.ronext.rpdptw.capabilities.package-info`로 지시한다. 권위 tree는 Maven artifact
`rpdptw-core` 아래에 `com.ronext.rpdptw.input`, `domain`, `normalization`, `travel`,
`propagation`, `evaluation.*` package를 두며 capability Java package는 단수
`com.ronext.rpdptw.capability`다. Verification도 `verification.*`와 `result.*`,
application도 `port.in/out`, `service`, `execution`의 소유 경계를 예약한다. Target tree에는
`build/pom.xml`, `rpdptw/pom.xml`, `legacy/pom.xml` aggregator도 보이지 않는다.

**사람에게 미치는 영향:** 처음 온 구현자가 guide의 exact path를 따르면 권위 문서와 live
reactor 양쪽에 없는 새 package를 만든다. Phase 01 consumer가 canonical input의 실제 시작
package를 찾지 못하고, architecture rule이 잘못된 skeleton을 승인하거나 다음 Phase에서
대규모 rename을 요구할 수 있다.

**Target 위치/anchor:** §6.2 `PROPOSED target tree`, lines 356~402; §9.1 `Package ownership`,
lines 556~568; §15.1 Phase 01 handoff.

**Source evidence:**

- `docs/2026-07-26-architecture-design.md` §2.1 `Maven target tree`, §2.3 `Package 책임`
- `docs/architecture-domain-implementation-design.md` §3.2 `전체 directory tree`,
  §3.4 `Module 책임`
- `docs/implementation/phases/phase-00-build-architecture-skeleton.md` §6 `변경 대상
  module/package/file tree`, §6.1 `Package ownership`
- Review-time live inventory의 `rpdptw/**/package-info.java`와 세 하위 aggregator POM

**Root cause:** Artifact 이름과 Java package 이름을 동일하게 단순화했고, 교육용 축약 tree를
실행 가능한 expected file tree처럼 제시했다.

**Required correction:** §6.2와 §9.1을 core Phase 00의 reviewed tree에 맞춘다. 최소한
세 aggregator POM, core의 여섯 package 영역과 evaluation 하위 package, solver/application/
verification 하위 ownership, 단수 `capability`를 명시한다. 이름이 정말 proposal이면
accepted Phase 00 coordinate/package manifest가 source of truth이며 guide 예시를 그대로
생성하지 말라는 치환 절차와 검증 명령을 함께 둔다.

**Target 수정 필요 여부:** YES

**Residual risk:** Package 이름을 맞춰도 production type을 미리 만드는 문제는 별도다.
Phase 00에서는 reviewed `package-info.java`만 허용하고 Phase 01+ semantic type은 계속
금지해야 한다.

### P00-HG-R-002 — Legacy characterization의 false oracle

**Severity:** HIGH

**Finding:** Target은 `malformed JSON 또는 잘못된 request: 400`,
`지원하지 않는 method: 405`를 하나의 current contract로 고정하고
`returns400ForMalformedJson()`을 제안한다. `HEAD`의
`OptimizationApiController#handleOptimizations`는 `IllegalArgumentException`만 `400`으로
처리하고 Jackson parse exception을 generic `Exception`으로 받아 redacted `500`을 반환한다.
같은 controller의 unsupported method/path는 `404`다. `405`는 worker controller의 두 POST
context에서만 명시적으로 반환한다.

**사람에게 미치는 영향:** 이동 전 코드와 다른 expected value를 golden으로 만들면 legacy
보존 작업이 API 의미 변경을 정상 green으로 승인한다. 반대로 실제 behavior를 보존한
구현을 test failure로 오판해 불필요한 controller 변경을 유도한다.

**Target 위치/anchor:** §10 `WP00-1`, lines 790~799와 test 후보 lines 831~840;
§14.2 legacy checklist line 1522.

**Source evidence:**

- `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java`
  (`HEAD`) `handleOptimizations`, `submit`, `getResult`
- `src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java`
  (`HEAD`) `runBatch`, `finalizeResult`
- `docs/implementation/phases/phase-00-build-architecture-skeleton.md`
  `WP-00-1 — Legacy placeholder characterization과 격리`
- `docs/implementation/reviews/phase-00-review.md` `P00-R-005 — Legacy failure-path
  golden coverage`

**Root cause:** Existing review의 “400/404/405/500/202 경로를 모두 cover”를 “각 category의
고정 status”로 잘못 일반화했고 endpoint, method, parse 단계별 matrix를 만들지 않았다.

**Required correction:** 이동 전 exact bytes로 endpoint × method × path × failure stage의
characterization matrix를 먼저 생성한다. 최소한 malformed JSON과 parsed-invalid request,
API unsupported method/path와 worker unsupported method를 분리하고, expected value는
pre-move observed response에서 가져온다. 더 좋은 HTTP semantics로 바꾸려면 Phase 00
characterization과 분리된 승인 변경이어야 한다.

**Target 수정 필요 여부:** YES

**Residual risk:** 현재 controller는 client/provider를 field initialization에서 만들기 때문에
test seam 추가가 initialization timing과 failure mapping을 바꿀 수 있다. 이동 전/후 byte-level
fixture와 response golden을 모두 재실행해야 한다.

### P00-HG-R-003 — Evidence staging, sealing과 `clean` lifecycle 충돌

**Severity:** HIGH

**Finding:** WP00-0은 evidence root가 source tree와 분리돼야 한다고 하면서 바로 다음에
`target/phase-00-evidence/...`를 immutable baseline 후보로 준다. Maven `target/`은 이후
`clean verify`가 지우는 generated tree다. Master Realization Plan은 working-tree
`target/` 파일만 evidence로 인용하는 것을 명시적으로 금지한다. 또한 Target의 최종 seal은
수동 `shasum <exact-evidence-leaf-files...>`에 머물러 기존 core review가 요구한 canonical
relative path, byte length, nested completeness, symlink/missing/path mutation rejection을
실행 가능한 oracle로 보존하지 못했다.

**사람에게 미치는 영향:** WP00-0 baseline이 WP00-5 전에 삭제되거나 사람이 일부 log만
복사해 다른 snapshot의 evidence와 섞을 수 있다. `clean` 뒤에도 우연히 남은 manifest가
green이면 source/evidence 연결이 끊긴 acceptance receipt가 발행될 수 있다.

**Target 위치/anchor:** §10 `WP00-0` lines 694~705, 728~761; `WP00-2` lines 946~948;
`WP00-5` lines 1195~1280; §12.2 evidence bundle.

**Source evidence:**

- `docs/implementation/master-realization-plan.md` §9.1 `Pre-review evidence manifest`,
  §9.4 `금지`
- `docs/implementation/phases/phase-00-build-architecture-skeleton.md`
  `WP-00-2`, `WP-00-5`, §12.2
- `docs/implementation/reviews/phase-00-review.md`
  `P00-R-003 — Source와 evidence 재현성`

**Root cause:** Build output staging 위치와 immutable evidence 보관 위치를 구분하지 않았고,
기존 review correction의 recursive/corruption contract를 교육용 수동 hash 예로 축약했다.

**Required correction:** `target/`은 disposable staging으로만 표시하고 acceptance input으로
직접 인용하지 않는다. `clean` 영향 밖의 승인된 content-addressed/digest-protected evidence
root와 promotion 시점을 명시한다. Seal 명령은 canonical relative path + byte length +
SHA-256 stable manifest를 만들고 nested omission, symlink, missing file, same path/different
bytes, one-byte mutation과 self-reference를 non-zero로 거부해야 한다. Dirty/untracked
implementation bytes도 commit 또는 content-addressed source archive로 완전히 봉인한 뒤 두
clean workspace에 사용한다.

**Target 수정 필요 여부:** YES

**Residual risk:** Evidence store 자체의 retention/ACL 정책은 Phase 00 내부 이름만으로
결정할 수 없다. Build·Quality/Release owner가 위치와 보존 정책을 승인해야 한다.

### P00-HG-R-004 — Profile catalog까지 막는 과도한 customer rule

**Severity:** HIGH

**Finding:** Target pseudocode는 “각 stable production class”가 customer-specific namespace를
참조하면 실패시키고, WP00-4와 exit checklist도 stable module 전체에서 customer name/
reference 0을 요구한다. 권위 계약은 generic core/solver/verification의 customer branch를
금지하지만, exact customer/profile/version과 authorization metadata는
`rpdptw-profile-catalog` 및 승인된 adapter authorization 경계가 소유한다. 실행 가능한
customer 분기와 versioned customer identity data는 같은 것이 아니다.

**사람에게 미치는 영향:** Phase 04의 정당한 profile catalog data가 architecture violation이
된다. 구현자는 profile identity를 다른 임의 위치에 숨기거나 전체 customer rule을 broad
skip하는 두 가지 나쁜 선택으로 밀릴 수 있다.

**Target 위치/anchor:** §9.3 lines 642~656; `WP00-4` negative fixture 목록; §14.4
`Stable module의 ... customer-specific ... reference 0`.

**Source evidence:**

- `docs/2026-07-26-architecture-design.md` §2.6 `Profile과 고객 확장`, §2.7
  `Build와 architecture enforcement`
- `docs/architecture-domain-implementation-design.md` §3.4 `Module 책임`, §3.6
  `Architecture enforcement`
- `docs/implementation/phases/phase-00-build-architecture-skeleton.md`
  `WP-00-4` step 8
- `docs/implementation/reviews/phase-00-review.md`
  `P00-R-004 — Architecture scan oracle의 검출 한계`

**Root cause:** Customer identity, customer-specific executable behavior, Java package namespace와
generic-module conditional을 한 문자열 금지 규칙으로 합쳤다.

**Required correction:** 규칙을 다음처럼 분리한다.

1. Generic core/solver/verification과 승인 범위의 application code에는 customer-name
   conditional/implementation/package가 없어야 한다.
2. Profile catalog와 승인된 adapter authorization은 exact identity/version data를 가질 수
   있지만 임의 executable rule이나 solver branch를 소유하지 않는다.
3. Approved token manifest, 허용 위치, AST/bytecode/package rule과 conditional/string/switch
   negative fixture를 함께 쓰고 coverage 한계를 evidence에 남긴다.

**Target 수정 필요 여부:** YES

**Residual risk:** 정적 검사만으로 semantic synonym을 완전 검출할 수 없다. Capability/profile
변경에는 독립 change review가 계속 필요하다.

### P00-HG-R-005 — HEAD baseline과 live drift를 구분하지 않은 CURRENT

**Severity:** MEDIUM

**Finding:** Target metadata는 commit만 기록하고 dirty-state/content digest를 기록하지 않은 채
§6.1을 `CURRENT snapshot`이라 부른다. 리뷰 시점 live checkout은 이미 Phase 00 구현 drift가
있고 공식 progress도 `IN_PROGRESS`다. Target의 progress SHA와 POM/Docker fingerprint는
`HEAD` baseline에는 맞지만 live bytes에는 맞지 않는다. root `src/**`가 이동되어 §5.1의
Java source relative link 5개도 현재 checkout에서 깨진다. §17은 여전히
`implementation status = NOT_STARTED`를 현재 사실로 단정한다.

**사람에게 미치는 영향:** 구현자는 이미 존재하는 wrapper/reactor/module을 “없음”으로
판정해 중복 생성하거나, 현재 legacy source를 찾지 못하거나, 진행 중인 다른 작업과 겹칠 수
있다. 단순 commit ID만 기록하면 동일 `HEAD`의 서로 다른 dirty bytes를 재현할 수 없다.

**Target 위치/anchor:** metadata lines 9~16; §2.2 execution progress fingerprint line 100;
§5.1 source links lines 282~288; §6.1 lines 310~354; §17 lines 1672~1673.

**Source evidence:**

- `docs/implementation/execution-progress-and-results.md` metadata와 §10.1
  `Phase 00 prerequisite remediation`
- `docs/implementation/reviews/phase-00-review.md`
  `P00-R-006 — Stale adjacent document state`
- Review-time `git status --short --untracked-files=all`, `git ls-tree -r HEAD`,
  live POM/Java inventory와 SHA-256 결과

**Root cause:** Snapshot을 `HEAD` tree baseline과 dirty worktree 관측값으로 나누지 않았고
날짜만 기록했다. “공식 progress가 바뀌면 우선”이라는 주의문은 실행 경로와 broken link를
자동 교정하지 못한다.

**Required correction:** §6.1을 명시적인 `HEAD baseline at 7cc890e...`로 바꾸고,
별도 `live drift at <timestamp>` 또는 실행 직전 재분류 절차를 둔다. Branch/HEAD뿐 아니라
tracked diff/untracked content manifest digest를 기록한다. Source 읽기 명령은 baseline이면
`git show <commit>:<path>`, live면 re-inventory로 발견한 실제 path를 사용한다. Progress
fingerprint mismatch 시 단순 STOP뿐 아니라 scheduler status를 읽고 작업을 합류/중단하는
판정법을 적는다. 깨진 링크 5개도 snapshot-aware 방식으로 교정한다.

**Target 수정 필요 여부:** YES

**Residual risk:** Shared checkout drift는 문서 교정 뒤에도 계속 변할 수 있다. Implementation
owner가 immutable source snapshot을 봉인하기 전에는 live file 존재를 acceptance evidence로
사용하면 안 된다.

### P00-HG-R-006 — Core ambient nondeterminism guard의 false-green

**Severity:** MEDIUM

**Finding:** Core Phase 계약은 `core`가 environment variable, system clock, global random,
static mutable registry를 읽지 못하게 한다. Target의 dependency pseudocode, WP00-4
negative fixture 목록, test class 후보와 exit checklist에는 이 rule의 실행 가능한
negative control이 없다. Phase 00 package skeleton이 비어 있어 현재는 항상 green이며,
Phase 01 이후 코드가 ambient input을 추가해도 `E-P00-ARCH` 소비 계약만으로는 막지 못한다.

**사람에게 미치는 영향:** Build guard가 있다고 믿은 Phase 01+ 구현자가
`System.getenv`, wall-clock/default random 또는 static registry를 core에 넣어도 root verify가
통과할 수 있다. 같은 input/build의 semantic fingerprint와 재현성이 machine/run마다 달라지는
false-green이 된다.

**Target 위치/anchor:** §9.3 dependency evaluation; `WP00-4` lines 1087~1168;
§11.4 test 후보; §14.4 exit checklist.

**Source evidence:**

- `docs/implementation/phases/phase-00-build-architecture-skeleton.md` §3.2
  `MUST/MUST NOT` item 11, §8.5 `Build dependency pseudo-code`
- `docs/master-design.md` §13.2 `Strong reproducibility envelope`
- `docs/architecture-domain-implementation-design.md` §4.3 `Package 규칙`,
  §19.1 `Configuration layers`

**Root cause:** Phase 00 시점의 empty/package-info-only core만 검사하면 된다고 보고,
후속 Phase에도 계속 적용될 architecture policy의 negative fixture를 생략했다.

**Required correction:** Core ambient-access rule ID와 known-negative fixture를 추가한다.
`System.getenv`/system property semantic read, `System.currentTimeMillis`/`nanoTime`/ambient
clock, unseeded or global random, mutable static registry를 각각 검출하고 expected rule ID와
offending symbol을 assert한다. 허용할 clock/random/config는 후속 owner가 주입하는 명시적
contract여야 하며 Phase 00에서 fake production default를 만들지 않는다.

**Target 수정 필요 여부:** YES

**Residual risk:** Reflection이나 동적 service loading은 bytecode rule만으로 완전 검출되지
않는다. Source scan, dependency/bytecode rule과 독립 review를 함께 유지해야 한다.

## 5. 검사축별 판정과 finding 없는 영역의 근거

| 검사축 | 판정 | 근거와 residual risk |
|---|---|---|
| 원문 의미·불변조건·identity/lifecycle | `PASS_WITH_FINDINGS` | RPDPTW pair, stable route/bank authority, two verifier, document/implementation 상태 분리는 보존했다. Package/customer/reproducibility guard는 `001/004/006` 교정 필요. |
| 초심 Java/CVRPTW 구현자의 배경 이해 | `PASS` | CVRPTW와 RPDPTW 차이, delivery-only, pair atomicity, verifier 독립 이유와 학습 경로가 충분하다. Residual은 잘못된 exact package 지시다. |
| Command/WP 실행·판정 가능성 | `CHANGES_REQUIRED` | WP마다 사전조건/행동/기대/실패/rollback/handoff는 있다. Legacy false oracle과 evidence lifecycle이 `002/003`으로 실행 안전성을 깬다. |
| Java/Maven 실제 구조·type/signature | `CHANGES_REQUIRED` | Proposed test-only type 표기는 안전하지만 expected package/file tree가 권위 구조와 다르다(`001`). |
| 복붙 과잉/추상성 | `PASS` | Production 완성 코드를 제공하지 않고 test-only 후보를 proposed로 제한했다. WP 설명은 충분히 구체적이다. |
| Entry/exit/evidence/rollback/failure/security/observability/reproducibility | `CHANGES_REQUIRED` | AND gate, redaction, offline/two-build, 사람 checkpoint는 좋다. Evidence storage/seal과 ambient nondeterminism이 `003/006`으로 미완전하다. |
| OPEN/GATED/DEFERRED 보존 | `PASS` | `Q-BENCH-02`, public API/schema, `Q-VAR-01`, plugin/timestamp owner 결정을 hidden default로 닫지 않았다. |
| Phase 13 C-17/14A 및 Phase 14 authority | `PASS` | 14A receipt + C-17 승인 전 Phase 13을 막고, 14B production authority를 Phase 00 build와 분리했다. Shortcut은 발견하지 못했다. |
| 인접 Phase ownership/handoff | `PASS_WITH_FINDING` | Phase 01은 accepted Phase 00 build/arch/legacy evidence만 소비하고 domain type 선취를 금지한다. 잘못된 package handoff는 `001` 교정 필요. |
| Link/fragment/fingerprint/용어 | `CHANGES_REQUIRED` | Authority 용어와 15 Phase는 정확하다. Live progress fingerprint 1건 stale, relative link 5건 broken이며 fragment link는 0개다(`005`). |
| 사람 승인·마지막 안전 지점 | `PASS` | H0~H5와 승인 전 stop point를 구분하고 broad reset/clean을 금지한다. Evidence 위치 자체는 `003` 교정 필요. |
| 문서 완료 vs 구현 완료·legacy 11-Phase | `PASS_WITH_LIVE_DRIFT` | 00~14 총 15개, review/implementation/acceptance 분리는 정확하다. 다만 live status 표시는 `005`처럼 stale하다. Legacy 11-Phase 계획 복사는 없다. |
| Test fixture/builder/oracle/red→green/category/pass | `CHANGES_REQUIRED` | Negative rule ID, test count, fixture leakage와 category 표는 강하다. Legacy status oracle과 core ambient negative fixture가 `002/006`으로 부족하다. |

## 6. 링크·구조·whitespace 정적 검사

검사는 Target 파일 하나를 기준으로 수행했다.

| 검사 | 결과 |
|---|---|
| Target non-empty/hash/line | `PASS`; SHA-256 `bf6a...c16`, 1,687 lines |
| Markdown relative link | 39개 중 target 존재 34, broken 5 |
| Broken link | lines 284~288의 기존 root `src/main`/`src/test` Java 5개; live source 이동으로 부재 |
| Fragment | Target의 relative fragment link 0개; fragment target 오류는 해당 없음 |
| Heading hierarchy | Fenced bash의 `# FUTURE` 주석을 제외하면 skip 없음 |
| Code fence | 34개, 짝수/closed |
| Trailing whitespace | match 0 |
| EOF newline | 존재 |
| Target-only `git diff --no-index --check /dev/null <target>` | whitespace diagnostic 0; untracked added-file diff이므로 exit `1`은 expected |
| Repository `git diff --check` | exit `0`, output 0 |

Broken link 5건은 Target의 snapshot/live 분리 결함 `P00-HG-R-005`에 포함했다.

## 7. Residual implementation blocker — Target correction과 별도

아래는 이 guide의 문구를 고친다고 해결되는 문제가 아니며 이 리뷰는 구현 판정을 하지 않았다.

1. 공식 progress상 Phase 00 prerequisite remediation은 `IN_PROGRESS`이고 세 `E-P00-*`,
   독립 구현 review와 post-review acceptance receipt가 없다.
2. Dirty live reactor/module/test 파일은 아직 untracked/modified 상태다. 존재나 local green
   가능성만으로 immutable source/evidence가 되지 않는다.
3. Legacy Jackson convergence/Shade collision, exact plugin/version/checksum, archive timestamp
   derivation은 승인·compatibility evidence가 필요하다.
4. Phase 01은 Phase 00 acceptance receipt 전 계속 blocked다.
5. `Q-BENCH-02`, Phase 13 `C-17`, Phase 14B production authority와 `Q-VAR-01`은 각각
   OPEN/GATED/DEFERRED 상태를 유지한다.

## 8. 최종 verdict

**CHANGES_REQUIRED**

Target 수정이 필요한 finding이 6개다. 특히 package skeleton, legacy failure oracle,
evidence sealing과 customer/profile 허용 경계는 구현자가 잘못된 green을 만들 수 있으므로
교정 전 이 guide를 Phase 00 실행 지시의 단독 기준으로 사용하면 안 된다.

Reviewer는 Target guide를 포함한 다른 파일을 수정하지 않았다. 생성한 파일은 이 review
report 하나뿐이다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=4 MEDIUM=2 LOW=0
REQUIRED_CORRECTION_FINDINGS: P00-HG-R-001,P00-HG-R-002,P00-HG-R-003,P00-HG-R-004,P00-HG-R-005,P00-HG-R-006

## Correction 01 읽기 전용 재검증

### 재검증 범위와 고정 입력

```yaml
recheck_round: "01"
recheck_kind: ORIGINAL_REVIEWER_READ_ONLY_CORRECTION_RECHECK
recheck_started_at: 2026-07-29T02:41:42+09:00
inventory_observed_at: 2026-07-29T02:44:56+09:00
recheck_recorded_at: 2026-07-29T02:45:46+09:00
repository: /Users/brown/workspace/ro-next
branch: codex-implementation
head: 7cc890ee1d0805df5ae14b633127fade4f978639
head_tree: a63fa3b93ca206298ac6d467e02e1f3503cee30e
review_prefix_sha256_before_append: 63f733a5b6f61e41ea3befa4c8340dbd43f9755f4dc6fbb9a17377543e99d332
review_prefix_lines_before_append: 445
target_sha256_rechecked: c1d18c0f118f840c272c1737da678dc83769ed13ceca02227ec525edf4bd2f7c
target_lines_rechecked: 1969
correction_report_sha256_rechecked: b25a7e1ded51b22d694147f3ac511901febb534b47b03b0f47f2928e12b7889f
correction_report_lines_rechecked: 236
target_modified_by_reviewer: false
correction_report_modified_by_reviewer: false
implementation_reviewed_for_acceptance: false
```

`review_prefix_sha256_before_append`는 이 절을 붙이기 전 원 review 445행의 byte
fingerprint다. 이 재검증은 correction report의 `addressed_findings`를 성공 근거로 사용하지
않고, corrected target의 실제 anchor를 원 finding의 root cause·required correction과
source에 다시 대조했다.

읽은 문서의 재검증 시점 SHA-256은 다음과 같다.

| 입력 | SHA-256 | 재검증 역할 |
|---|---|---|
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 현재 invariant, reproducibility와 Phase 13/14 authority |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 현재 domain 책임 지도; acceptance authority로 사용하지 않음 |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 현재 recommended architecture 지도; H3 승인 대체 금지 |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 사용자 고정 dated cross-check |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | package/profile/build 경계 cross-check |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | module, evidence, configuration 경계 |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | OPEN/GATED/DEFERRED 상태 |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | §9 evidence lifecycle와 Phase/DoD |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | canonical index와 acceptance 의미 |
| `docs/implementation/execution-progress-and-results.md` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | live §10.1 scheduler 상태 |
| `docs/implementation/phases/phase-00-build-architecture-skeleton.md` | `0ad01e21a94ac543486a53c0ed0a256b4137e673bbae1be4d959ebc46dcefd27` | 원 Phase 00 실행 계약 |
| `docs/implementation/reviews/phase-00-review.md` | `db4f1f8c4d178c99d82597085155a1fbfac223659944f3b092e66ae5b7f6fed8` | 원 core review와 `P00-R-001~006` |
| `docs/implementation/phases/phase-01-canonical-input-normalization.md` | `67e078a058753335ae823bbec815b3628f212d4593dfce9ee0db39cc02000324` | Phase 01 consumer entry/handoff |
| `docs/implementation/reviews/phase-01-review.md` | `4644e7a7ef30f82368eea167e4f70197f4a1380a808c8066f37036608561f27f` | Phase 01 document review와 구현 acceptance 구분 |
| `docs/implementation/human-guides/phases/phase-01-human-implementation-guide.md` | `142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e` | 인접 human guide의 Phase 00 blocked entry |
| `docs/2026-07-26-master-design.md` | `5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a` | historical regression cross-check만 수행 |
| corrected target | `c1d18c0f118f840c272c1737da678dc83769ed13ceca02227ec525edf4bd2f7c` | 직접 재검증 대상 |
| correction report | `b25a7e1ded51b22d694147f3ac511901febb534b47b03b0f47f2928e12b7889f` | 교정 주장과 범위 확인; closure oracle로 사용하지 않음 |

Live inventory는 원 review와 correction 시점에서 다시 진행됐다. Append 직전 관측은 tracked
modified 5, tracked deleted 7, untracked file 128이며, porcelain status digest는
`bbf302340c2d9d18129926470df58fcf824cc30dc350d0cb6d77da5ea01648ab`,
tracked binary diff digest는
`a17293f3e23a7bebc1d6460c963ce4b3ec63a687e469e3b6034ecea92edf455b`,
sorted untracked `path + SHA-256` manifest digest는
`130167ea90aa461e3af77ebf7de60436bc03693481c3ae4bb90d76871daa4649`다.
Non-`target` inventory는 POM 13개, production Java 33개, test Java 17개다. 이 절을
append하면 마지막 untracked-content digest만 review file bytes 때문에 달라지는 것이
정상이며 target/correction report identity는 달라지지 않는다.

공식 progress는 correction 시점의 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`에서 현재
`REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS`,
`REJECTED_PENDING_FIX_02_REGENERATION`, receipt `NOT_PRODUCED`, Phase 01
`BLOCKED`로 더 진행됐다. `target/phase-00-evidence` directory도 재검증 시점에는 존재하지
않았다. 이것은 target §6.1.2가 correction-time 관측을 acceptance identity와 분리하고 실행
직전 live hash·status·owner를 다시 읽도록 한 절차가 필요한 실제 사례다. 현재 구현이나
evidence의 합격 여부는 이 문서 correction 재검증의 verdict에 포함하지 않는다.

### Finding별 closure

| Finding | Status | 확인한 target anchor와 source evidence | Closure 판정 | 남은 risk |
|---|---|---|---|---|
| `P00-HG-R-001` | **RESOLVED** | Target §6.2 `REVIEWED Phase 00 target tree — H3 전에는 PROPOSED`, §9.1 `Package ownership`, §15.1; Phase 00 원본 §6/§6.1, Final Architecture §2.1/§2.3, 통합 구현 설계 §3.2/§3.4 | §6.2가 `build/pom.xml`, `rpdptw/pom.xml`, `legacy/pom.xml` 세 aggregator와 core `input/domain/normalization/travel/propagation/evaluation.*`, solver/application/verification/result 세부 ownership, 단수 `com.ronext.rpdptw.capability`를 모두 제시한다. Production skeleton은 `package-info.java`까지만 허용하고, H3의 accepted coordinate/package manifest가 다르면 예시를 복사하지 말라고 명시한다. 원 root cause인 artifact/package 단순화와 실행 tree 축약이 닫혔다. | H3에서 current recommended profile module 지도와 reviewed capabilities/profile-catalog 배치를 실제로 승인해야 한다. 승인 전 이름은 계속 PROPOSED이며 semantic type 선생성은 금지다. |
| `P00-HG-R-002` | **RESOLVED** | Target WP00-1의 endpoint × method × path × failure-stage matrix, §11.4/§12.1/§14.2; `HEAD`의 `OptimizationApiController#handleOptimizations/submit/getResult`, `OptimizationWorkerController#runBatch/finalizeResult`; Phase 00 원본 WP-00-1과 core review `P00-R-005` | Target은 API malformed parse를 generic redacted `500`, parsed-invalid를 `400`, API fallback을 `404`, missing result를 `202`, worker non-POST를 `405`, worker parse/provider/empty-candidate failure를 endpoint별 `500`으로 분리한다. Expected status/content-type/body bytes는 이동 전 실제 응답 capture 뒤에만 확정하고 이동 전후 byte-level 비교하도록 한다. 원 root cause인 status category 일반화와 invented oracle이 닫혔다. | `HEAD` controller의 provider/client field initialization timing 때문에 test seam 자체가 동작을 바꿀 수 있다. Pre/post move exact bytes와 redaction을 실제 구현 evidence에서 다시 증명해야 한다. |
| `P00-HG-R-003` | **RESOLVED** | Target WP00-0, WP00-2, WP00-5, §11.4, §12.2, §14.5, §18; Master Realization Plan §9.1~§9.4; Phase 00 원본 WP-00-5와 core review `P00-R-003` | `target/`을 clean으로 삭제되는 disposable staging으로 한정하고 acceptance의 직접 참조를 금지했다. 승인된 clean-safe content-addressed/digest-protected root로 한 번 promotion하며, dirty source를 commit 또는 content-addressed archive로 봉인한다. Manifest는 recursive canonical relative path의 bytewise order, byte length, SHA-256을 기록하고 self-reference를 배제한다. Nested omission, symlink, missing, duplicate/path collision, same path/different bytes, one-byte mutation을 non-zero로 거부하며 verifier의 자동 재봉인도 금지한다. 원 root cause인 staging/immutable lifecycle 혼합과 수동 얕은 seal이 닫혔다. | Evidence root, retention, ACL은 OPEN 사람 승인이다. 현재 live evidence directory 부재와 공식 rejected status는 target correction 문제가 아니라 별도 구현 blocker다. |
| `P00-HG-R-004` | **RESOLVED** | Target §9.3 customer rule, WP00-4, §11.4, §12.1, §14.4; Final Architecture §2.6/§2.7, 통합 구현 설계 §3.4/§3.6, Phase 00 원본 WP-00-4와 core review `P00-R-004` | Generic core/solver/verification과 승인된 application 범위의 customer executable conditional/namespace 금지와, profile catalog/승인된 authorization boundary의 exact identity/version data 허용을 분리했다. Catalog의 arbitrary executable rule/solver branch는 다시 금지하며 approved token/location manifest, AST/bytecode/package 검사, conditional/string/switch negative fixture와 catalog positive fixture를 요구한다. 원 root cause인 identity data와 executable branch의 전역 문자열 금지 결합이 닫혔다. | 정적 scan은 customer synonym과 reflection을 완전 검출하지 못한다. Coverage manifest와 profile/capability 독립 change review가 계속 필요하다. |
| `P00-HG-R-005` | **RESOLVED** | Target §6.1 `HEAD baseline과 correction-time live drift`, WP00-0, §17; current progress §10.1과 인접 Phase 01 human guide의 blocked entry; core review `P00-R-006` | Target은 `HEAD 7cc890e…`와 correction-time dirty snapshot을 별도 표·digest로 고정하고, baseline source는 `git show`, live source는 재-inventory한 actual path로 읽게 한다. Branch/HEAD/tree, status, tracked diff, untracked bytes, implementation manifest를 다시 계산하고 progress hash/status/task/owner가 바뀌면 동일 task owner에게 합류하거나 overlap 해소까지 STOP한다. 기존 live source 상대 링크를 제거해 현재 link 검사도 0 broken이다. correction 뒤 실제 status가 FIX_02로 바뀌었어도 timestamped 관측을 현재 사실로 가장하지 않으므로 원 root cause가 닫혔다. | Shared checkout은 계속 변한다. 현재 status/test count/progress hash는 correction snapshot과 이미 다르므로 구현자는 §6.1.2 재분류를 반드시 실행하고 immutable source 전에는 live file을 evidence로 쓰면 안 된다. 인접 Phase 01도 receipt 전 계속 BLOCKED다. |
| `P00-HG-R-006` | **RESOLVED** | Target §9.3 core ambient rule, WP00-4 rule/fixture 목록, §11.1/§11.4, §12.1, §14.4; Phase 00 원본 §3.2/§8.5/WP-00-4, Master §13.2, 통합 구현 설계 §4.3/§19.1 | Core의 environment/system-property semantic read, system/ambient clock, unseeded/global random, mutable static registry를 각각 금지하고 각 category의 known-negative fixture와 `CoreAmbientAccessArchitectureTest` 후보를 둔다. Guard enabled 결과는 non-zero만 보지 않고 expected rule ID와 offending symbol을 assert하며, 허용 input은 후속 owner가 주입하는 explicit contract로 한정한다. Empty skeleton의 현재 green을 policy 존재로 오인하던 root cause가 닫혔다. | Reflection/dynamic loading과 의미상 동의어는 단일 bytecode rule로 완전 검출되지 않는다. Source/dependency/bytecode 조합과 독립 review를 유지해야 한다. |

6개 finding 모두 원 required correction의 필수 요소가 정확한 target anchor에 있고 source
계약과 일치한다. Correction 01이 이 finding 범위에서 만든 별도 `NEW` regression은 발견하지
않았다.

### 명령·test manifest와 정적 재검증

- WP00-2의 wrapper/root/offline/two-clean-build 명령은 `HEAD`와 live candidate를 구분하고
  `LIVE CANDIDATE / FUTURE ACCEPTED`로 표시한다. 현재 Maven path는 root, `rpdptw`,
  `build`, `legacy` aggregator를 포함한 POM 13개이며 target의 `-pl
  legacy/gcp-placeholder`, `-pl build/architecture-rules`, `-pl rpdptw/core` path와
  일치한다.
- Current live parent POM은 Surefire를 사용하고 `failIfNoTests=false`이며 Failsafe binding은
  없다. Target은 이 설정을 성공 oracle로 신뢰하지 않고, Surefire `*Test` discovery와
  Failsafe 사용 시 `*IT`의 `integration-test`/`verify` binding을 effective POM에서 확인하게
  한다. `clean` 뒤 fresh XML, expected class, non-zero suite/test count가 없거나 stale
  `target/*-reports`만 있으면 root exit `0`도 FAIL로 판정한다.
- Negative fixture는 guard-disabled compile 가능성, guard-enabled expected rule
  ID/offending symbol/non-zero, 위반 제거 뒤 green까지 요구한다. Dependency/network/syntax
  failure를 intended red로 오인하는 oracle과 zero-test green을 모두 차단한다.
- 이 재검증은 read-only document correction review이므로 Maven/test를 실행해 mutable
  `target/` evidence를 새로 만들지 않았다. 위 명령의 실제 구현 PASS나 Phase 00 acceptance를
  주장하지 않는다.
- Target 48개와 correction report 15개의 inline relative link/fragment를 GFM slug로
  대조한 결과 missing target/fragment는 0이다. Reference-style link definition은 두 파일
  모두 0이다.
- Fence-aware heading 검사는 target heading 67개, correction heading 14개에서 unclosed
  fence 0, heading-level jump 0을 확인했다. Code fence 안의 `#`는 heading으로 세지 않았다.
- Target과 correction report 모두 trailing whitespace 0, CR 0, EOF newline 존재다.
  Target-only와 correction-only
  `git diff --no-index --check /dev/null <file>`은 새 파일 전체 diff 때문에 exit `1`이지만
  whitespace diagnostic은 0줄이다.
- Repository `git diff --check`는 exit `0`이다. 이는 tracked live drift의 whitespace
  검사 결과이며 untracked target 검사를 대신하지 않아 위 target-only 검사를 별도로
  수행했다.
- Target source fingerprint는 재검증 직전 원 review hash를 포함해 고정 입력과 일치했다.
  Execution progress만 correction-time live hash에서 현재
  `9361ae89...bf08c`/FIX_02로 바뀌었으며 target은 이를 historical snapshot으로 표시하고
  재계산 절차를 제공한다. 이 review append 뒤 원 review 전체 hash가 바뀌는 것도
  `review_prefix_sha256_before_append`와 구분한다.

### 재검증 결론

Correction 01은 `P00-HG-R-001`부터 `P00-HG-R-006`까지의 root cause와 required
correction을 모두 닫았다. 따라서 사람용 guide correction의 재검증 verdict는
`ACCEPTED`다. 이는 Phase 00 구현, 현재 FIX_02 evidence, core Phase review 또는 Phase 01
entry의 acceptance가 아니다. 공식 구현 상태는 계속 rejected/not accepted이고 유효한
post-review receipt 전에는 Phase 01이 열리지 않는다.

RECHECK_ROUND: 01
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: P00-HG-R-001,P00-HG-R-002,P00-HG-R-003,P00-HG-R-004,P00-HG-R-005,P00-HG-R-006
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: c1d18c0f118f840c272c1737da678dc83769ed13ceca02227ec525edf4bd2f7c
CORRECTION_REPORT_HASH_RECHECKED: b25a7e1ded51b22d694147f3ac511901febb534b47b03b0f47f2928e12b7889f
