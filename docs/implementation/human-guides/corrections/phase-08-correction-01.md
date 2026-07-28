# Phase 08 사람용 구현 가이드 교정 보고서 — Round 01

```yaml
correction_status: COMPLETE
correction_round: "01"
corrected_at: 2026-07-29T02:39:04+09:00
live_inventory_snapshot_at: 2026-07-29T02:35:33+09:00
adjacent_non_authoritative_recheck_at: 2026-07-29T02:39:04+09:00
head_commit_observed: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
target: docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md
review_input: docs/implementation/human-guides/reviews/phase-08-review.md
correction_report: docs/implementation/human-guides/corrections/phase-08-correction-01.md
correction_owner: RPDPTW Phase 08 human-guide correction owner
implementation_owner: RPDPTW Application/Local Runtime owner
architecture_owner: RPDPTW Architecture owner
test_artifact_owner: Phase 08 port-contract test owner
status_authority: total scheduler
independent_followup_owner: independent Phase 08 human-guide reviewer
allowed_write_scope:
  - docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-08-correction-01.md
code_pom_test_evidence_status_modified: false
git_stage_commit_push_worktree_status: NOT_PERFORMED
target_sha256_before: 2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5
target_sha256_after: ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326
target_git_object_before: 2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223
target_git_object_after: b3572fee2c687534cc41a581cb18f6f45e5328c6
review_sha256_before_after: 761a69a40ced5b2d7cff5d785b80db21da3e13a433b39ec44278ad78aa4d8a84
addressed_findings:
  - F-HG-P08-001
  - F-HG-P08-002
  - F-HG-P08-003
  - F-HG-P08-004
  - F-HG-P08-005
deferred_findings: []
```

## 1. 교정 결론과 ownership

F-HG-P08-001~005를 모두 target 본문에서 root cause 수준으로 교정했다. 이 교정은
문서의 source receipt, proposed module/test skeleton과 current observation만 바꾼다.
Phase 08 Java/POM/test/evidence를 구현하거나 Phase 00/08 acceptance를 승격하지 않았다.

Ownership은 다음처럼 유지한다.

| 항목 | Owner | 이 교정의 경계 |
|---|---|---|
| Current canonical path와 placement | 사용자 고정 입력 + Architecture owner | Current map을 반영했지만 새 ADR/public compatibility를 발행하지 않음 |
| Phase 07 output 의미/type owner | Phase 07 Verification/Result owner | Accepted hierarchy를 2단계로 소비; upstream 이름 변경 권한 없음 |
| CLI main/bootstrap/composition | Phase 08 `apps/cli` owner | Skeletal owner/DAG만 고정; exit code/schema/default는 `PROPOSED/OPEN` |
| Reusable contract Test JAR | Phase 08 test-artifact owner | Proposed test-only publication/consumption; production scope 유입 금지 |
| Implementation/evidence | Phase 08 implementer와 independent reviewer | 현재 `NOT_PRODUCED/NOT_ACCEPTED` 유지 |
| Authoritative status | Total scheduler | 본 교정자가 progress/status를 수정하지 않음 |

## 2. Target, review와 source hash receipt

SHA-256은 correction 시작과 최종 검증 시점의 file bytes를 기록한다. `unchanged`는 이
교정 scope가 source를 수정하지 않았다는 뜻이다. Mutable progress와 인접 human guide의
차이는 concurrent drift이며 이 작업이 만든 변경이 아니다. Current canonical/actual
Phase/review authority는 변하지 않았다. 인접 human guide의 마지막 bytes는
비권위 live observation으로만 기록하고 다시 따라가지 않는다.

### 2.1 Target와 correction input

| File | Before SHA-256 | After SHA-256 | 판정 |
|---|---|---|---|
| Target guide | `2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5` | `ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326` | Corrected |
| Human-guide review | `761a69a40ced5b2d7cff5d785b80db21da3e13a433b39ec44278ad78aa4d8a84` | `761a69a40ced5b2d7cff5d785b80db21da3e13a433b39ec44278ad78aa4d8a84` | Unchanged correction input |
| Canonical Phase 08 | `15dbcab53fc00fc4d3062c5bdb0eb1f072fd902bfbb59b322859f30b090a5f0f` | same | Unchanged implementation baseline |
| Canonical Phase 08 review | `95d0b005d6218af7c2e5e8d720419c6fd781d0f97f9abddff2fd0132b52264f7` | same | Unchanged F-P08-001~013 input |

### 2.2 Current canonical 5와 path map

| Source | Start/final SHA-256 | Role |
|---|---|---|
| `docs/README.md` | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | Current path/status map |
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | Current canonical system meaning/conflict order |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | Current domain/result/error meaning |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | Current module/app/test placement |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | Current integrated 15-Phase realization input |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | Exact OPEN/GATED/deferred state |

### 2.3 Dated, implementation와 adjacent inputs

| Source | Before/after SHA-256 | Role |
|---|---|---|
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | Historical implementation baseline only |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | Historical implementation baseline only |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | Implementation document map/authority protocol |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | Realization/evidence/DoD baseline |
| Actual Phase 07 | `1b0d7d92d961c44605307a5a66bd659ac54b4cb0832d275aa10c8bfb855de727` | Producer contract |
| Actual Phase 07 review | `82da52bcd52724c44a5ffd913f3146b7017457ed0a209b85611b80cb8547c78f` | Producer blocker/review |
| Phase 07 human guide | `43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d` → `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd` | Read snapshot 뒤 concurrent non-authoritative drift; exact hierarchy는 unchanged Actual Phase 07에서 고정 |
| Actual Phase 09 | `ac08d10a63f7d0bd86a74fa61c1d220b0619a9c16c7d50fe0017cfe9afc8bb3b` | Storage consumer contract |
| Actual Phase 09 review | `e49a441d0ccedb9e13b5b5c0f2614bef710dc68a1d587a3c65a5e430bf7a0b5d` | Consumer blocker/review |
| Phase 09 human guide | `67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54` → `09b32c3a821b6ad68ee4aa5a0322d01af07ee08390e3650b80c8de8339b46038` | Read snapshot 뒤 concurrent non-authoritative drift; reusable-suite authority는 current Architecture와 unchanged Actual Phase 09에서 고정 |

### 2.4 Mutable implementation observation

| Source | Start SHA-256/object | Final SHA-256/object | 의미 |
|---|---|---|---|
| Root `pom.xml` | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` / `1dc675ba17b7f2202f34a22131f152cc2868b075` | same | Live unaccepted Phase 00 reactor observation |
| Execution Progress | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` / `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` / `0419f69199b3140dd44020f78278b1352e6517b8` | Concurrent scheduler drift; review 02 completed `CHANGES_REQUIRED`, fix 02 started |

Final live status는 `REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS / NOT_ACCEPTED`,
replacement evidence는 `REJECTED_PENDING_FIX_02_REGENERATION`, acceptance receipt는
`NOT_PRODUCED`다. Phase 08 concrete production type/test와 `E-P08-*`는 0이다. 이
timestamped observation은 provenance이며 accepted evidence가 아니다.

## 3. Finding별 correction trace

### F-HG-P08-001 — Current canonical source receipt

- **Target anchor:** Metadata `source_sections_and_fingerprints`, §3.1~§3.3, §5, WP
  source references와 §17 traceability.
- **Root cause corrected:** Implementation README/Canonical Phase 08의 날짜 고정 hierarchy를
  복제해 current top-level map과 canonical 5를 재조정하지 않은 문제를 제거했다.
- **Source:** `docs/README.md`, Current Domain §1/§3/§10/§13~18, Current
  Architecture §5~7/§10/§12/§17~19.
- **Correction:** Current map/current canonical 5, dated historical inputs,
  implementation baseline, mutable live snapshot을 별도 authority/time layer로 분리했다.
  Current-vs-dated module/result/status semantic impact도 본문에 기록했다.
- **Gate:** Source receipt와 Architecture owner semantic-impact 승인이 없으면 Phase 08
  implementation 기준을 freeze하지 않는다.
- **Verification:** Current path links와 fragments 0 broken, source table/hash receipt,
  source→WP→test/evidence trace를 정적으로 확인했다.
- **Residual:** Upstream implementation README/Canonical Phase 08의 historical hierarchy는
  이 scope에서 수정하지 않았다. Target의 resolution을 owner가 구현 시작 전에 승인해야 한다.

### F-HG-P08-002 — Executable CLI composition direction

- **Target anchor:** §8.1~§8.3, WP-08.4/6/7, architecture/launcher tests, command
  matrix와 `REQ-P08-DAG`.
- **Root cause corrected:** Parser/presenter app과 downstream distribution에
  main/composition을 분리하면서 실제 bootstrap call direction을 닫지 않은 문제를 제거했다.
- **Source:** Current Architecture §6.6/§7.1/§10/§19.
- **Correction:** `apps/cli`가 `LocalCliMain`, `LocalRuntimeBootstrap`,
  `LocalCompositionRoot`, config, executable packaging을 함께 소유하고
  `adapters/common` local package와 accepted capability/profile을 주입하게 했다.
  `distributions/local`과 별도 filesystem module split은 새 승인 전 금지했다.
- **Gate:** Accepted Phase 00 naming과 Architecture/API owner approval 전 exact Java
  signature/public compatibility를 freeze하지 않는다.
- **Verification:** Acyclic compile DAG, reverse edge 0, packaged main resolution,
  downstream classpath 없는 launcher bootstrap smoke를 exact proposed tests에 연결했다.
- **Residual:** 실제 module/POM/JAR가 없으므로 launcher 결과는 future evidence이며 현재
  구현/acceptance가 아니다.

### F-HG-P08-003 — Phase 07 nested sealed hierarchy

- **Target anchor:** §4.5, §9.8, §10.1/§10.3, WP-08.0~2, §12.2, entry/exit
  checklist와 `REQ-P08-P07`.
- **Root cause corrected:** 의미상 세 outcome과 Java top-level subtype 수를 같은
  three-way 표현으로 축약한 문제를 제거했다.
- **Source:** Actual Phase 07 §7.6~§8.5/§15.2와 Phase 07 human guide의 exact contract.
- **Correction:** 첫 switch는 `Publishable | Rejected`, 둘째 switch는
  `VerificationRejected | GateIncomplete`로 작성했다. Application 외부의 세 의미와
  upstream 내부 2단계 type ownership을 구분하고 default/catch-all/reflection을 금지했다.
- **Gate:** Accepted Phase 07 artifact/contract와 two-level signature compatibility가
  없으면 mapping/real E2E를 시작하지 않는다.
- **Verification:** `phase07TopLevelOutputAndNestedRejectionAreExhaustive()`와 required-field
  fail-closed test, non-success publisher call 0 oracle에 연결했다.
- **Residual:** Upstream 이름은 proposed/unaccepted이므로 accepted type 이름이 바뀔 수
  있지만 two-level exhaustiveness와 field preservation은 유지해야 한다.

### F-HG-P08-004 — Reusable Test JAR와 clean-checkout selected tests

- **Target anchor:** §8.1~§8.2, §10.5, 모든 WP selected command, §12.1~§12.2와
  `E-P08-PORT`.
- **Root cause corrected:** Standard source set만 정하고 reusable artifact publication,
  consumer test dependency와 clean-checkout sibling resolution을 닫지 않은 문제를 제거했다.
- **Source:** Current Architecture §18.4/§19.1, live
  `build/test-fixtures/pom.xml` Test JAR convention, live architecture-rules consumer POM.
- **Correction:** `build/port-contract-tests`를 reusable `tests` classifier Test JAR
  owner로, `adapters/common`의 concrete `*ContractTest`를 behavior owner로 정했다.
  `type=test-jar/classifier=tests/scope=test`, no-production-leak rule과 reactor order를
  명시했다. Selected test는 explicit empty isolated repository에서
  `-pl owner -am clean install -DskipTests` dependency preparation 후 같은 repository의
  owner `-f ... clean test`로 실행한다.
- **Gate:** Phase 00 accepted reactor/POM, attached Test JAR, test-scope consumer edge와
  fresh exact report validator가 없으면 evidence를 만들지 않는다.
- **Verification:** Surefire 3.5.4와 Failsafe 미구성 상태를 분리하고, future `*IT`는
  explicit binding/`-Dit.test`/fail-if-none 전 discovered로 계산하지 않게 했다.
  Zero/missing/duplicate/failure/error/skipped/aborted/stale report를 모두 fail로 둔다.
- **Residual:** Test JAR과 planned modules는 아직 없고, production compile/runtime
  dependency report가 future evidence에 반드시 포함돼야 한다.

### F-HG-P08-005 — Authoring snapshot, live observation와 accepted evidence

- **Target anchor:** Metadata, §3.2, §5.1~§5.3, WP-08.0과 §12.1.
- **Root cause corrected:** Concurrent Phase 00 scheduler/reviewer 진행 뒤 final live
  receipt를 다시 봉인하지 않은 문제를 제거했다.
- **Source:** Live root POM, Execution Progress §5/§10.1과 actual Maven/source/test/evidence
  inventory.
- **Correction:** HEAD baseline, original authoring snapshot, final live observation,
  accepted evidence를 별도 layer로 나눴다. Root reactor 13 POM, `rpdptw` main Java
  23개 전부 `package-info`, concrete/test 0, application package-info 4/concrete 0,
  build test Java 11개와 Phase 08 module/test/evidence 부재를 반영했다.
- **Gate:** Review 02에서 거절된 replacement Phase 00 bundle과 진행 중인 fix 02는
  entry authority가 아니다. Regenerated evidence의 independent PASS와 scheduler
  acceptance receipt 전 Phase 08은 계속 blocked다.
- **Verification:** Final live progress/root object, Phase 00 status/evidence/review/receipt와
  Phase 08 zero inventory를 재조회했다.
- **Residual:** Shared checkout은 계속 변할 수 있다. Target timestamp/hash는 provenance로
  사용하고 구현 시작 때 scheduler receipt와 inventory를 다시 봉인해야 한다.

## 4. Preserved OPEN/GATED/deferred와 Phase 13/14 gates

- `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`; 공식 수치/default를 만들지 않았다.
- `C-17`과 Phase 13은 Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` 및 별도 승인
  전 `GATED`; route-selection dependency/option을 Phase 08에 넣지 않았다.
- `Q-VAR-01`은 `DEFERRED`; 질문·구현·config option을 추가하지 않았다.
- Phase 14A benchmark qualification과 Phase 14B production authority를 분리했다.
  Phase 08 local pass는 어느 authority도 자동 부여하지 않는다.
- Authorization/failure/publication/S3 ownership/worker commit/full-solution evaluation
  cross-Phase blocker는 임의 signature/default로 닫지 않았다.

## 5. Validation receipt

| 검사 | 방법 | 결과 |
|---|---|---|
| Target before/after | `shasum -a 256`, `git hash-object`, `wc -l` | Before 2,599줄; after 2,833줄; hash metadata와 일치 |
| Relative links/GFM fragments | Target/report link resolve + GitHub heading slug 검사 | `PASS`; missing/broken 0 |
| Heading hierarchy | Fence 밖 H1~H6 level transition 검사 | `PASS`; jump 0 |
| Fences | Line-anchored fence parity | `PASS`; target 142, report 2 |
| Whitespace/NUL/EOF | Trailing blank/tab, NUL, final LF 검사 | `PASS`; 위반 0 |
| Required wording/gates | Finding ID, current/datetime layers, 2+2 dispatch, CLI/Test JAR, OPEN/GATED/deferred scan | `PASS` |
| Scoped diff | Before-copy↔target와 `/dev/null`↔report `--no-index --check` | `PASS` |
| Scope | 두 허용 파일 외 before/final workspace status 비교 | `PASS`; unrelated pre-existing/concurrent 변경 보존 |
| Maven/Java tests | 실행하지 않음 | Phase 08 type/test 부재와 Phase 00 `NOT_ACCEPTED`; future command를 actual evidence로 오인하지 않음 |

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: F-HG-P08-001, F-HG-P08-002, F-HG-P08-003, F-HG-P08-004, F-HG-P08-005
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5
TARGET_HASH_AFTER: ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326
