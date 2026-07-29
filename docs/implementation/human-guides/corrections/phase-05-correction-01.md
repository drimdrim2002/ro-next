# Phase 05 사람용 구현 가이드 correction 01

```yaml
phase: "05"
correction_round: "01"
correction_kind: INDEPENDENT_HUMAN_GUIDE_FINDING_CORRECTION
correction_status: COMPLETE_PENDING_INDEPENDENT_RECHECK
correction_date: 2026-07-29
correction_timezone: Asia/Seoul
target: ../phases/phase-05-human-implementation-guide.md
review_input: ../reviews/phase-05-review.md
review_verdict_input: CHANGES_REQUIRED
source_head: 7cc890ee1d0805df5ae14b633127fade4f978639
source_head_tree: a63fa3b93ca206298ac6d467e02e1f3503cee30e
live_snapshot_at: "2026-07-29T02:17:56+09:00"
live_recheck_at: "2026-07-29T02:23:50+09:00"
live_final_recheck_at: "2026-07-29T02:32:11+09:00"
concurrent_source_drift_detected: true
ownership:
  allowed:
    - docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md
    - docs/implementation/human-guides/corrections/phase-05-correction-01.md
  review_modified: false
  canonical_or_implementation_docs_modified: false
  code_pom_test_or_deployment_modified: false
  git_stage_commit_push_or_worktree: false
addressed_findings:
  - HG-P05-001
  - HG-P05-002
  - HG-P05-003
deferred_findings: []
target_sha256_before: be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d
target_sha256_after: 6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c
review_sha256_before: 69e97a55633b03448f8ff5e8a60b9824ecec5c93caaf62da1449a58209de6743
review_sha256_after: 69e97a55633b03448f8ff5e8a60b9824ecec5c93caaf62da1449a58209de6743
```

이 report는 [독립 human-guide review](../reviews/phase-05-review.md)의 required
finding 세 건을 [Phase 05 target guide](../phases/phase-05-human-implementation-guide.md)에
교정한 기록이다. 구현, Phase 00/05 acceptance, scheduler status 변경 또는 Phase 06
handoff authorization이 아니다.

## 1. 범위, before hash와 source 역할

### 1.1 소유권과 before 상태

Target은 correction 시작 시 untracked file이었고 SHA-256은 review metadata와 직접
계산값이 모두
`be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d`였다.
이 correction report는 시작 시 존재하지 않았다. 두 소유 파일 외의 existing tracked,
untracked와 ignored 변경은 보존했다.

### 1.2 Authority와 시간 층

| 입력 층 | 역할 | 교정 적용 |
|---|---|---|
| [Canonical Master](../../../master-design.md), [질문 등록부](../../../master-design-open-questions.md) | 현재 semantic decision, exact `Q-*`, gate와 변경 절차 | Stable pair, objective-first, 4×2, `Q-BENCH-02`, `C-17`, `Q-VAR-01` |
| 현재 top-level [Domain](../../../domain-design.md) / [Architecture](../../../architecture-design.md) | 최신 `REVIEW` 전체-product 의미·배치 cross-check | Dated 고정 입력이나 accepted reactor를 자동 대체하지 않음 |
| 사용자 고정 [2026-07-26 Domain](../../../2026-07-26-domain-design.md) / [Architecture](../../../2026-07-26-architecture-design.md) | 구현 문서 세트의 dated meaning/module baseline | Pair/service/DAG를 보존하고 오래된 질문 상태만 register로 판정 |
| [통합 구현 설계](../../../architecture-domain-implementation-design.md) | 15 Phase, Phase 5/6 lifecycle과 test/evidence 경계 | Phase 05 construction과 Phase 06 COW를 분리 |
| Implementation README/plan/progress와 original Phase/review | 실행 baseline, entry/exit/evidence와 residual blocker | Pinned HEAD와 live scheduler 상태를 분리 |
| HEAD baseline | 재현 가능한 tracked source 기준 | Commit/tree와 Git blob을 기록 |
| Timestamp가 있는 live snapshot | 현재 POM/wrapper/module/test/status 관찰 | Uncommitted/unapproved이며 acceptance authority 아님 |

현재 top-level map, user-fixed dated 입력, implementation baseline과 live snapshot은
시간이 다르다. 더 늦은 timestamp, 파일 존재 또는 Maven 성공을 conflict authority로
사용하지 않았다. 충돌은 path/heading, 두 후보, downstream 영향과 owner를 기록하고
승인 전 fail-closed하도록 target §3.1~§3.3에 명시했다.

### 1.3 Source SHA-256 before/after

아래 source는 모두 read-only다. After는 최종 검증에서 다시 계산한다.

| Source | Before SHA-256 | After SHA-256 |
|---|---|---|
| [Canonical Master](../../../master-design.md) | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| [Current Domain](../../../domain-design.md) | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| [Current Architecture](../../../architecture-design.md) | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| [2026-07-26 Domain](../../../2026-07-26-domain-design.md) | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| [2026-07-26 Architecture](../../../2026-07-26-architecture-design.md) | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| [2026-07-26 historical master](../../../2026-07-26-master-design.md) | `5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a` | `5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| [Question register](../../../master-design-open-questions.md) | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| [Implementation README](../../README.md) | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| [Master Realization Plan](../../master-realization-plan.md) | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| [Execution Progress](../../execution-progress-and-results.md) | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` | `5696065eb923c50c00e6fa7d5ed2e13c30894e77fcc7a8009fcd36b5d2d8e7cd` |
| [Original Phase 05](../../phases/phase-05-pair-insertion-initial-portfolio.md) | `9df624f8ba93d8de69f3f60e8cf3d7f74f7ac0e9c14fc7d99514bf6154595533` | `9df624f8ba93d8de69f3f60e8cf3d7f74f7ac0e9c14fc7d99514bf6154595533` |
| [Original Phase 05 review](../../reviews/phase-05-review.md) | `cef56504d6f42e2ec0ea3a9e69579ab50b6f999677007822ad292ef7d309bfd7` | `cef56504d6f42e2ec0ea3a9e69579ab50b6f999677007822ad292ef7d309bfd7` |
| [Human-guide README](../README.md) | `ad64de533a4e45984ae30be12c57d969a36efd4dc8453e949ec4efc992f6b18a` | `ad64de533a4e45984ae30be12c57d969a36efd4dc8453e949ec4efc992f6b18a` |
| [Human-guide progress](../execution-progress-and-results.md) | `00bd40b9802e34dbacfd21fc69615592a1df55eca71600d814b716ed4e856e0d` | `44ffb79b3938bf2e680ccf9718c696a70d7fc9cd8641c6f1352a2a8ee09c2724` |
| [Adjacent guide 04](../phases/phase-04-human-implementation-guide.md) | `158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010` | `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` |
| [Adjacent guide 06](../phases/phase-06-human-implementation-guide.md) | `fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f` | `31297a0595592619f254baa7f9e6845e30fea828578b40c362f695d814c33428` |
| [Human-guide review input](../reviews/phase-05-review.md) | `69e97a55633b03448f8ff5e8a60b9824ecec5c93caaf62da1449a58209de6743` | `69e97a55633b03448f8ff5e8a60b9824ecec5c93caaf62da1449a58209de6743` |

직접 읽은 전체 source set의 ordered `path + SHA-256` manifest는 before
`83d433e65bb4e2bd0b273b5c7fad3dcf7ed202183f9bb0ac234d75512dcd45b2`다.
최종 recheck의 after manifest는
`308ace7207a8c4c414e7212ec07df751a5abd099c31b337bd341c138d6c35d9a`다.
Implementation Execution Progress, human-guide progress와 인접 Phase 04/06 guide는
각 owner의 동시 갱신으로 바뀌었다. 관련 Phase 05 handoff, comparator/evaluator gate,
fresh Maven 경계를 다시 읽었고 이 correction은 그 파일들을 수정하지 않았다.

### 1.4 Live inventory snapshot

| Fingerprint/관찰 | 값 |
|---|---|
| HEAD commit/tree | `7cc890ee1d0805df5ae14b633127fade4f978639` / `a63fa3b93ca206298ac6d467e02e1f3503cee30e` |
| Status SHA-256 | `93ee89d679029252067311d3641dd41e4c33cb9a75780a7efbfccd21c2238a48` |
| Tracked binary diff SHA-256 | `500c4ef4e09165f685d0b446b97b4a5352e863dd1c08c6de82b89a1aab212836` |
| Sorted untracked content manifest | `9f65029bf10aca7dc785ec2dcb2bbee7a746ba26829627afea1eb1a20d395d69` |
| Non-`target` Phase 00 candidate manifest | `f8a7fd90f71bcc4459f1d3ea17de62ba7a703459bd3bcea932db9555dd1ff25c` |
| Root POM / progress live Git blob | `1dc675ba17b7f2202f34a22131f152cc2868b075` / `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d` |
| Project reactor inventory | Project POM 13, `rpdptw` main Java 23(package skeleton), Phase 00 build `*Test.java` 9, legacy `*Test.java` 5 |
| Phase 05 inventory | Production type 0, Phase 05 test class 0, evidence 0 |
| Scheduler state | Phase 00 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`; rejected/superseded bundle; acceptance receipt `NOT_PRODUCED` |

이 snapshot은 correction-pre-edit 관찰이며 acceptance evidence가 아니다. Shared checkout이
변하면 implementation 시작 시 같은 범위의 timestamp/status/content manifest를 다시
봉인하고 accepted receipt와 비교해야 한다.

`2026-07-29T02:23:50+09:00` recheck에서 Execution Progress blob은
`36afdfde...`에서 `f875edbd...`로 바뀌었고 scheduler state는
`FIX_01_IMPLEMENTED_REVIEW_02_PENDING`이 됐다. Non-`target` Phase 00 candidate
manifest는 `f8a7fd...25c`로 같았고 acceptance receipt는 계속 `NOT_PRODUCED`다.
이 동시 drift를 target과 report에 기록하고 Phase 05 entry를 계속 fail-closed했다.
최종 검증 시 tracked binary diff SHA-256은
`2ae737ce4a68f2820bd44e0d768bd12db3b3802ec805f6fdd803fe157a17300f`,
status SHA-256은
`f110797e7b56c992a2a996a9b8f8884eb88a8df02bf71aba7110c277728b97f4`였다.
이는 accepted baseline이 아니라 동시 drift와 correction files가 포함된 live fingerprint다.
전체 test class 14개는 Phase 00 build 9개와 legacy 5개의 합이며 초기 Phase 00
수치와 scope가 다르다. Phase 05 명명 production type/test는 계속 0개였다.

## 2. Finding별 correction

### HG-P05-001 — Fresh reactor dependency와 selected-test false green

- **변경 anchor:** Target [§6.4](../phases/phase-05-human-implementation-guide.md#64-현재와-future-maven-명령을-섞지-않는다), WP-05.0~WP-05.6, [§12.5](../phases/phase-05-human-implementation-guide.md#125-false-green-방지), §13.2.
- **Root cause:** 원본 0-test 교정에서 selected invocation의 upstream pattern mismatch를 피하려고 `-am`을 제거했지만, 별도 invocation이 fresh local repository에서 sibling reactor artifact/test-jar를 얻는 단계를 함께 설계하지 않았다.
- **Source section:** Dated Architecture §2.2/§6.1 module DAG, live `rpdptw/solver`, `build/test-fixtures`, `build/architecture-rules` POM dependency, human review HG-P05-001.
- **교정:** 각 target마다 새 isolated `maven.repo.local`을 만들고 `-pl <target> -am -DskipTests install`로 dependency를 준비한 뒤, 같은 repository에서 target-only `-pl <target> ... clean test`를 실행하도록 exact command를 교체했다. Prep report는 evidence에서 제외하고 target-owned fresh XML만 판정한다.
- **Zero/stale 판정:** `failIfNoSpecifiedTests=true`, start marker 이후 XML, non-empty sealed `Class#method` manifest와 양방향 equality, `tests>0`, failure/error/skipped 0, report/source/POM/test/config digest를 AND gate로 요구했다.
- **보존 gate:** Live wrapper/reactor는 Phase 00 `PRESENT_BUT_UNACCEPTED`; command는 accepted receipt 전 `BLOCKED TEMPLATE`다. `-DskipTests` prep 성공은 test/Phase acceptance가 아니다.
- **검증:** 모든 selected block에 wrapper, isolated repo, dependency-prep `-am install`, selected target-only `clean test`, exact `-Dtest`, fail-if-zero와 report owner가 있는지 정적 검사한다.
- **Residual risk:** Phase 00 Fix 01/review 02에서 selector, Surefire inheritance, test-jar classifier 또는 offline bootstrap이 바뀌면 accepted manifest에 맞춰 command를 다시 review해야 한다.

### HG-P05-002 — HEAD baseline과 live working-tree drift

- **변경 anchor:** Target metadata/서문, [§3.1](../phases/phase-05-human-implementation-guide.md#31-충돌-해소-순서), [§3.3](../phases/phase-05-human-implementation-guide.md#33-head-baseline과-live-drift의-검증-가능한-fingerprint), §5.1, [§6.1~§6.2](../phases/phase-05-human-implementation-guide.md#61-재현-명령), WP-05.0/§12.5.
- **Root cause:** Guide가 reproducible HEAD와 volatile live worktree를 말로 분리했지만 실제 source gate와 inventory는 HEAD blob만 사용했고, 병행 Phase 00 작업 뒤 final snapshot을 다시 반영하지 않았다.
- **Source section:** Live Execution Progress §5/§10.1, root/reactor POM과 wrapper/module/test inventory, human review HG-P05-002.
- **교정:** Commit/tree Git baseline, current-map role, timestamped status/tracked/untracked/content hashes, root POM/progress blob과 live counts를 별도 표로 기록했다. Reactor/wrapper/package skeleton은 `PRESENT_BUT_UNACCEPTED`, Phase 05 production/test/evidence는 `ABSENT`, Phase 00 initial `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`와 recheck `FIX_01_IMPLEMENTED_REVIEW_02_PENDING`을 모두 `NOT_ACCEPTED`로 판정했다.
- **Fail-closed:** `git status --short`, `git diff`, live `git hash-object`, source/POM/test count를 accepted receipt와 비교하고 source heading, build manifest, scheduler status 또는 post-report hash가 다르면 작업/report를 폐기하도록 했다.
- **Authority/time 보존:** Current top-level Domain/Architecture, user-fixed dated inputs, implementation HEAD baseline과 live snapshot의 역할을 구분했다. 최신 timestamp나 skeleton 존재로 API/default/acceptance를 만들지 않는다.
- **보존 gate:** Phase 00~04 receipt, cross-Phase evaluator/tie/editor owner와 scheduler owner gate는 계속 blocked다.
- **검증:** Target의 과거 “wrapper/module 없음”을 HEAD baseline 문맥 외에서 제거하고 live hash/status/count와 actual inventory를 대조한다.
- **Residual risk:** Shared checkout은 계속 변할 수 있다. Target의 snapshot은 시점 자료이며 실제 implementation source identity는 accepted receipt 또는 별도 content-addressed archive로 다시 봉인해야 한다.

### HG-P05-003 — Stable-tie hand oracle의 business equality

- **변경 anchor:** Target [§12.1](../phases/phase-05-human-implementation-guide.md#121-independent-fixture와-oracle), [§12.2](../phases/phase-05-human-implementation-guide.md#122-exact-test-classmethod-matrix), [§12.3](../phases/phase-05-human-implementation-guide.md#123-red--green-순서), WP-05.5.
- **Root cause:** Capacity 구분 fixture와 stable-tie comparator fixture를 한 expected row에 합치면서 physical travel equality를 full business equality로 가정하고 `(0,1)`을 unconditional selection pass로 만들었다.
- **Source section:** Master §9.1~§9.3/§11.1, Original Phase 05 §8.3/§10.2~§10.3, original review F-P05-005, human review HG-P05-003.
- **교정:** Terminal `T`와 `P1/D1/P2/D2`를 pairwise-distinct physical location으로 고정하고 diagonal `0/0`, 모든 off-diagonal `1m/1s`를 명시했다. Capacity oracle은 `6/6/2/4`와 feasible keys만 판정하고 selection을 주장하지 않는다.
- **Equality/tie oracle:** Test-only exact business authority spy가 두 complete cache-free candidate에 같은 ordered artifact/fingerprint를 반환함을 먼저 assertion한 뒤에만 stable `(0,1)`을 판정한다. 별도 non-tied control은 `(2,3)`이 strictly better일 때 stable-first `(0,1)`이 결과를 뒤집지 못하게 한다.
- **보존 gate:** Comparator/business-equality API는 계속 `OPEN CROSS-PHASE`; 미승인 시 selection 두 test는 `BLOCKED_UNTIL_COMPARATOR_CONTRACT`이고 capacity receipt만 independent green이다. `B_EQ/B_01/B_23`은 test-only이며 production dimension/default/API가 아니다.
- **검증:** Exact method matrix와 red→green 순서에서 capacity, equality-first stable tie와 non-reversal control을 세 method/판정으로 분리했다.
- **Residual risk:** Approved full-solution evaluator/comparator의 ordered vector와 identity가 정해지면 spy가 그 exact contract를 구현하도록 다시 pin해야 한다.

## 3. 보존한 contract, gate와 last safe point

- Complete pair, same concrete vehicle, exactly once, precedence, route-bank XOR,
  delivery-only initial-load ownership과 delegated feasibility를 바꾸지 않았다.
- Full-solution evaluator/ranking, business equality/solution/insertion tie와 Phase 05/06
  central editor ownership은 `RESIDUAL CROSS-PHASE BLOCKER`다.
- Traversal, CLOCK coordinate convention과 utilization missing/zero/tie는
  `OPEN/PROPOSED`이며 reference vector/typed policy/golden trace 전 production
  implementation을 freeze하지 않는다.
- 모든 Java type/signature는 별도 표시가 없는 한 `PROPOSED` skeletal contract다.
  완성 코드, POM/test 또는 evidence를 만들지 않았다.
- Phase 05는 Phase 06 seed까지만 소유한다. Phase 06 COW/ALNS와 Phase 07 verifier를
  앞당기지 않았다.
- `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01`은 `DEFERRED`, multi-trip과
  public schema는 미승인 상태다.
- Phase 13은 유효한 Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 별도 `C-17`
  승인 전 `GATED`; Phase 14B production authority는 별도 gate다.
- Phase 00~04 acceptance receipt와 Phase 05 evidence/review/receipt가 없으므로 상태는
  계속 `BLOCKED / NOT_ACCEPTED / NOT_PRODUCED`다.

## 4. 검증 결과

| 검사 | 결과 | Evidence/해석 |
|---|---|---|
| Finding coverage | PASS | HG-P05-001~003 각각 root cause/source/anchor/correction/preserved gate/validation/residual risk 기록 |
| Target/review/source hash | PASS | Target `be348...`→`6a0cc...`; review 불변 `69e97...`; source set `83d43...`→`308ac...`, 동시 drift 분리 |
| Target/report link target | PASS | 두 파일 local Markdown link 81개, missing path 0 |
| GFM fragment | PASS | GFM punctuation/space/duplicate 규칙으로 explicit fragment 0개 누락 |
| Heading hierarchy | PASS | Target/report H1 `1/1`, heading `82/13`, level jump `0/0` |
| Code fence | PASS | Target/report fence marker `100/2`, 모두 closed |
| Whitespace/EOF | PASS | 두 파일 trailing whitespace/tab/NUL/CR 모두 0, final LF 있음 |
| Maven command static closure | PASS | Selected invocation 10개 모두 wrapper, isolated repo, matching target `-am -DskipTests install`, start marker, target-only `clean test`, fail-if-zero 충족; false flag 0 |
| Oracle decisiveness | PASS | Capacity `6/6/2/4`, pairwise-distinct fixture, exact equality-first tie, non-tied reversal control, comparator 미승인 blocked를 각각 확인 |
| Scoped `git diff --check` | PASS | 두 소유 path로 실행, exit 0/output 0 |
| Untracked-aware whitespace | PASS | 각 파일 `git diff --no-index --check /dev/null <file>`의 whitespace diagnostic 0; exit 1은 expected content difference |
| Allowed write scope | PASS | Intentional write는 target/report 두 파일뿐; stage/commit/push/worktree 0 |
| Maven/test execution | NOT_RUN_BY_DESIGN | Phase 05 code/test 0, Phase 00 unaccepted; root/legacy green을 Phase 05 evidence로 사용하지 않음 |

## 5. Residual risk

1. Phase 00 Fix 01과 independent review/receipt가 진행 중이며 live POM/report contract가
   다시 바뀔 수 있다.
2. Phase 01~04 accepted handoff와 full-solution evaluator/comparator/editor owner가 없어
   Phase 05 production 구현은 계속 blocked다.
3. Comparator contract 승인 전 tie selection test는 의도적으로 blocked다. Capacity green을
   option-selection green으로 확대 해석하면 안 된다.
4. Fresh repository command는 accepted Phase 00 offline/bootstrap policy, classifier와
   plugin inheritance에 맞춰 재검증해야 한다.
5. 이 correction은 independent recheck와 scheduler acceptance를 대체하지 않는다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG-P05-001, HG-P05-002, HG-P05-003
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d
TARGET_HASH_AFTER: 6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c
