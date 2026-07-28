# Phase 07 사람용 구현 가이드 교정 보고서 — Round 01

```yaml
phase: "07"
correction_round: "01"
correction_type: HUMAN_IMPLEMENTATION_GUIDE_CORRECTION
status: COMPLETED
target: docs/implementation/human-guides/phases/phase-07-human-implementation-guide.md
review: docs/implementation/human-guides/reviews/phase-07-review.md
correction_report: docs/implementation/human-guides/corrections/phase-07-correction-01.md
owner_write_scope:
  - docs/implementation/human-guides/phases/phase-07-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-07-correction-01.md
read_only_scope:
  - README/progress/canonical/current/dated design documents
  - implementation Phase/review documents
  - adjacent Phase 06/08 human guides
  - Java/POM/wrapper/test/target inventory
prohibited_actions_observed:
  code_change: false
  pom_or_test_change: false
  stage: false
  commit: false
  push: false
  worktree_operation: false
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
timezone: Asia/Seoul
correction_date: 2026-07-29
target_sha256_before: 43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d
target_git_hash_object_before: f5e4d82c1a0119ba9dfb56031ae470a2a897cfba
target_lines_before: 1878
target_sha256_after: 9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd
target_git_hash_object_after: 24d30f09b972a58657f07c640c98940011880cf1
target_lines_after: 2163
human_review_sha256_before_after: e673c4b9963e90f21432252560a15e176cbb175522467cb130b4779ba1bf8659
human_review_git_hash_object_before_after: 0184cceee9a8e1685bdc101fde234334d5b58345
addressed_findings:
  - HG07-R001
  - HG07-R002
  - HG07-R003
  - HG07-R004
  - HG07-R005
deferred_findings: []
```

## 1. 교정 결과와 ownership

[Target 가이드](../phases/phase-07-human-implementation-guide.md)의 review-required finding 5건을 모두 root cause 수준에서 교정했다. 이 보고서와 target 외 파일은 수정하지 않았다. Shared checkout의 Phase 00 및 인접 human-guide 변경은 다른 작업자의 소유로 보존했고, 존재·hash drift를 observation으로만 기록했다.

이번 교정이 닫은 것은 사람용 구현 지시의 source 선택, exact result/test 계약, live inventory와 compile-closure 결함이다. Phase 07 실제 구현 entry는 계속 blocked다. Phase 00~06 acceptance, full-solution evaluator/comparator, Phase 06→07 schema owner, public encoding/hash와 independent implementation task/review/receipt를 이 문서 교정으로 승인하지 않았다.

## 2. Source hash before/after와 authority 층

Target 수정 전 source set을 읽고 fingerprint를 고정했으며, 수정 후 다시 hash를 확인했다. Canonical/implementation source는 target correction 동안 변하지 않았다. Adjacent guide와 progress의 차이는 이 작업의 write가 아니라 shared checkout의 concurrent drift다.

| Layer / source | SHA-256 before 또는 frozen read | SHA-256 after/recheck | Git hash-object after/recheck | 판정 |
|---|---|---|---|---|
| Target | `43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d` | `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd` | `24d30f09b972a58657f07c640c98940011880cf1` | Intended change |
| Human review | `e673c4b9963e90f21432252560a15e176cbb175522467cb130b4779ba1bf8659` | same | `0184cceee9a8e1685bdc101fde234334d5b58345` | Unchanged |
| Repository design map | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | same | `13f1b3b2dea038b8e0b466c138f5f59299413125` | Current map |
| Canonical Master | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | same | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | Current canonical |
| Canonical Domain | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | same | `ace117c380466b733994a1fbb2a95d31e41b3959` | Current canonical |
| Canonical Architecture | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | same | `81495ff448d0e618ab3563e8ff80614fb1028acf` | Current canonical |
| Integrated design | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | same | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | Current canonical |
| Question register | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | same | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | Current canonical |
| Dated Domain input | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | same | `0a02ba4c77a402455e3d80b76969dca28831b1e6` | Dated detail cross-check |
| Dated Architecture input | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | same | `d51339e251dee1e032e711144dc63d6d07d7323b` | Dated detail cross-check |
| Implementation README | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | same | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` | Implementation baseline |
| Master realization plan | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | same | `d7f6be4fff0089204fbdb52f731b2348407f36eb` | Implementation baseline |
| Execution progress | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | `0419f69199b3140dd44020f78278b1352e6517b8` | Concurrent live drift; target은 02:22:50 snapshot을 보존, final recheck는 별도 |
| Canonical Phase 07 | `1b0d7d92d961c44605307a5a66bd659ac54b4cb0832d275aa10c8bfb855de727` | same | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` | Implementation baseline |
| Original Phase 07 review | `82da52bcd52724c44a5ffd913f3146b7017457ed0a209b85611b80cb8547c78f` | same | `156a994ef66d9c6f51785d96d2b327fe4539712e` | Implementation baseline |
| Phase 06 adjacent guide | Review-observed `fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f`; full correction read `3c3d9f1544621e14558f3570b353012c452caf9e5616298a606cfca6d14d9f66` | Final live snapshot은 §6에서 기록 | Final live snapshot은 §6에서 기록 | Concurrent correction drift; acceptance 아님 |
| Phase 08 adjacent guide | Full correction read `2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5` | Final live snapshot은 §6에서 기록 | Final live snapshot은 §6에서 기록 | Concurrent correction drift; acceptance 아님 |

적용한 authority 순서는 다음과 같다.

1. Current map과 사용자 고정 non-dated canonical five가 이 correction의 conflict resolver다.
2. Dated Domain/Architecture는 implementation 문서군의 dated authority input 및 상세 regression cross-check다.
3. Implementation README/plan/canonical Phase/review/HEAD는 구현 baseline이다.
4. POM/wrapper/source/test/target/progress는 timestamped live snapshot이며 acceptance가 아니다.

Implementation README와 human-guide README가 날짜 붙은 source index를 계속 가리키는 충돌은 소유 범위 밖 residual이다. Target은 이를 `KNOWN_SOURCE_INDEX_CONFLICT`로 드러내고 current canonical 의미를 WP/test/evidence/trace에 반영했다.

## 3. Finding별 correction과 검증

### HG07-R001 — Canonical source 정렬

- **Target anchor:** §3.1~§3.3, metadata `source_sections_and_fingerprints`, WP-07.0, `REQ-P07-SOURCE`.
- **Root cause:** Implementation README/human-guide index의 dated path를 current repository map 및 사용자 고정 canonical five와 재조정하지 않고 복제했다.
- **Source section:** Repository map; Canonical Domain §13~§16; Canonical Architecture §5~§7/§18~§20/§22; question register exact rows.
- **Correction:** Current map/current canonical, dated input, implementation baseline, live snapshot의 네 층을 분리했다. Domain/Architecture link·heading·hash를 non-dated current 문서로 교체하고 dated 문서를 historical/detail cross-check로 내렸다.
- **Gate preservation:** `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`, Phase 13 `C-17 GATED`, Phase 14A/14B 분리를 유지했다.
- **Verification:** Current canonical five SHA-256/Git hash-object before=after; source table link target 존재; source-index conflict를 residual로 기록.
- **Residual:** 다른 README/guide의 source index 정렬은 별도 owner/scheduler 작업이다.

### HG07-R002 — Exact result summary/comparator

- **Target anchor:** §9.6.1~§9.6.2, WP-07.4, §12.2 additional manifest, §13.2, §15, `REQ-P07-RESULT`/`REQ-P07-REFERENCE-COMPARATOR`.
- **Root cause:** 교육용 skeleton을 줄일 때 exact field manifest와 계산 공식을 함께 제거하고 추상적인 “summary/reference metric”만 남겼다.
- **Source section:** Canonical Domain §14.1~§14.2; Master §10.2/§14.3; Integrated §11.6; Canonical Phase 07 §7.6/§8.4/§10.6/§13.1.
- **Correction:** Exact 8-field `ResultSummary`, used-route operational-time five-component formula와 제외 항목, checked integer/unit contract를 복원했다. Reference vector를 `(unassigned, dispatched, directed distance, operational time)` lex ascending으로 고정하고 exact equality, `NOT_COMPARABLE`, customer objective와의 분리를 명시했다.
- **Boundary/failure:** Negative/missing/unit/schema/overflow와 legacy non-finite를 compare/encode 전에 typed invalid로 거부하고 declared mismatch는 corruption, incomplete work는 `GateIncomplete`로 보존한다.
- **Deterministic oracle:** Route time `150 + 210 = 360`, first/second/third/fourth-field win, exact equality, first-field loss와 invalid table을 production helper 없이 계산한다. One-field/component tamper와 last-safe/overflow/non-finite boundary를 additional 5-method manifest로 연결했다.
- **Gate preservation:** External wire field/media/hash algorithm과 public compatibility는 계속 OPEN/PROPOSED다.
- **Verification:** Additional count `5`, unique `5`, cross-duplicate `0`, LF SHA-256 `5374ef3ddf0aa9961c6b3974f036ff360f3f8f3ec8f7585134814dc059195502`.
- **Residual:** Exact Java spelling/public visibility와 external encoding/hash는 owner approval 전 확정되지 않는다.

### HG07-R003 — Canonical exact 69-test inventory

- **Target anchor:** WP-07.0~07.6, §12.2/§12.4, §13.1~§13.2, §15, `REQ-P07-TEST-MANIFEST`.
- **Root cause:** Canonical acceptance matrix를 교육용 후보 표 29개로 축약하면서 required set과 selected subset을 구분하지 않았다.
- **Source section:** Canonical Phase 07 §10.3~§10.7/§12.1; Canonical Architecture §18; original review F-P07-004/F-P07-006.
- **Correction:** Canonical ordered 69-method manifest 전체를 target에 넣고 WP/evidence별 non-overlapping slice로 배정했다. Target summary/comparator oracle 5개는 별도 additional manifest로 두어 canonical missing을 대신하지 못하게 했다.
- **Runtime validation contract:** Canonical↔target ordered/bidirectional comparison, JUnit `MethodSource` discovery, parameterized invocation aggregation, fresh Surefire/Failsafe XML, missing/extra/duplicate/ambiguous/failed/error/skipped/aborted/stale/zero-discovered fail-closed를 정의했다.
- **Gate preservation:** Method source 존재/compile/disabled/production-helper reuse를 PASS로 계산하지 않는다.
- **Verification:** Canonical extracted unique `69`, target count/unique `69/69`, ordered equality `true`, LF SHA-256 `aff4bae9269b37b09a8df9909cb9e5c8f060e3bb2f1f1cc166c29749e66ad339`; required/additional cross-duplicate `0`.
- **Residual:** 실제 Phase 07 test source는 0개이므로 runtime execution evidence는 implementation entry 뒤에만 생성한다.

### HG07-R004 — HEAD와 live inventory 분리

- **Target anchor:** metadata live timestamp/status, §5.1, §6.1~§6.3.
- **Root cause:** Concurrent Phase 00 상태를 한 번만 재검증해 final review 전에 wrapper/reactor/test/evidence/status drift를 놓쳤다.
- **Source section:** Live root/child POM, wrapper properties, Java/test inventory, execution progress current Phase 00 remediation section.
- **Correction:** HEAD baseline을 유지하고 `2026-07-29T02:22:50+09:00` live snapshot에 13 POM, executable wrapper, rpdptw main package-info 23, build test Java 11, Phase 07 named type/test 0, `target/phase-00-evidence` 102 files와 rejected/superseded status를 기록했다.
- **Acceptance separation:** Wrapper 실행 가능과 acceptance command 권위를 분리했다. Phase 00은 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING / NOT_ACCEPTED`, receipt `NOT_PRODUCED`; Phase 07은 `BLOCKED_NOT_IMPLEMENTED`다.
- **Verification:** Wrapper/POM/progress SHA-256과 Git hash-object, module/test count, root Surefire `failIfNoTests=false`, Failsafe 부재를 read-only로 확인했다.
- **Residual:** Shared checkout은 계속 drift한다. Hash/count가 달라지면 implementation entry를 멈추고 accepted commit/bundle/review/receipt와 분리한 새 snapshot을 만든다.

### HG07-R005 — Java/Maven compile closure

- **Target anchor:** §5.2, §6.3, §9.1~§9.2, WP-07.0/07.2/07.6, `REQ-P07-MAVEN-CLOSURE`/`REQ-P07-HANDOFF-SCHEMA`.
- **Root cause:** “proposed skeleton” 경고로 referenced type의 semantic owner와 compile-closed review skeleton을 구분하지 않았고 production dependency만 써서 fixture/JUnit/Failsafe test closure를 누락했다.
- **Source section:** Canonical Architecture §5~§7/§18~§19; current Phase 06 human guide §15.2; live verification/test-fixtures/architecture POM.
- **Correction:** Type family별 module/package/visibility/immutability/OPEN blocker 표를 추가했다. Verification future direct test dependencies를 test-fixtures `test-jar:tests`, JUnit Jupiter, 필요 시 JUnit Platform launcher로 고정하고 production→fixture reference 0을 요구했다.
- **Handoff owner:** Phase 06→07 schema는 core-owned immutable vocabulary 또는 neutral versioned artifact 중 review가 승인한 한 대안만 허용하며 default를 선택하지 않았다. Solver import와 Phase 07 consumer type pull-forward를 금지했다.
- **Fresh build closure:** Accepted wrapper와 fresh isolated repository에서 `-pl rpdptw/verification -am clean install`, same-artifact no-`-am` selected run, filter-free module/architecture/root `clean verify`를 연결했다. Surefire/Failsafe owner, zero-test policy, report sealing과 stale detection을 명시했다.
- **Gate preservation:** Full-solution evaluator/API, exact cross-Phase type spelling/public visibility, Phase 06→07 schema receipt와 external encoding/hash는 계속 OPEN/BLOCKED/PROPOSED다.
- **Verification:** Live verification POM은 core-only, test-fixtures는 tests classifier producer, root no-test 허용/Failsafe 부재임을 재현했다. 문서가 현재 source를 compile-ready라고 허위 주장하지 않는다.
- **Residual:** Accepted Phase 00 effective POM과 predecessor API/schema receipt가 들어오기 전 실제 compile/test/evidence는 시작할 수 없다.

## 4. Canonical manifest와 deterministic oracle 검증

| 검사 | 결과 |
|---|---|
| Canonical Phase 07 §10.3~§10.7 unique extraction | `69` |
| Target required count / unique / ordered equality | `69 / 69 / true` |
| Required LF SHA-256 | `aff4bae9269b37b09a8df9909cb9e5c8f060e3bb2f1f1cc166c29749e66ad339` |
| Additional summary/comparator count / unique | `5 / 5` |
| Additional LF SHA-256 | `5374ef3ddf0aa9961c6b3974f036ff360f3f8f3ec8f7585134814dc059195502` |
| Required↔additional duplicate | `0` |
| Evidence slice union/intersection | Required `69` exact cover / duplicate `0` |

Runtime method/XML 검증은 아직 실행하지 않았다. Phase 07 production/test가 0개이고 Phase 00/01~06 acceptance가 없으므로 green을 만들 수 없으며, target은 이를 exit gate로 명시한다.

## 5. OPEN/GATED/deferred preservation

| 항목 | 교정 후 상태 |
|---|---|
| `Q-BENCH-02` official benchmark values | `OPEN — EXPERIMENT_REQUIRED` |
| `Q-VAR-01` | `DEFERRED` |
| Canonical external JSON/media/hash/public compatibility | `OPEN/PROPOSED` |
| Full-solution evaluator/comparator API/identity | Cross-Phase `OPEN BLOCKER` |
| Phase 06→07 handoff schema owner | `CONTRACT GATE`, no default |
| Phase 13 route pool/MIP | `C-17 GATED TARGET`, valid Phase 14A receipt + separate approvals required |
| Phase 14A benchmark acceptance | `NOT_RUN / RECEIPT_NOT_PRODUCED` |
| Phase 14B production authority | `NOT GRANTED/GATED` |

Internal `ResultSummary` semantics와 four-field reference ordering을 exact하게 만든 것은 public wire/hash 또는 official benchmark threshold 승인과 다르다.

## 6. Final live drift snapshot

Shared checkout의 adjacent guides/progress는 correction 동안 다른 작업자가 변경했다. 이 작업은 그 파일을 수정하지 않았다. 다음 값은 `2026-07-29T02:34:50+09:00` final validation snapshot이며 frozen correction read 및 acceptance identity와 구분한다.

| File | Final live SHA-256 | Final live Git hash-object | Lines | 의미 |
|---|---|---|---:|---|
| Phase 06 adjacent guide | `719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3` | `45bee760233551432e9106f6b560bace653b4c8a` | `2400` | Concurrent, read-only, acceptance 아님 |
| Phase 08 adjacent guide | `610ac2e6ee3deb20a088b4948f6c644c4073956078bbce5a381dd9d016038fd9` | `be3704b7c0ccd8cac28da0aaf9cb2a223893902e` | `2831` | Concurrent, read-only, acceptance 아님 |
| Implementation progress | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | `0419f69199b3140dd44020f78278b1352e6517b8` | `470` | Scheduler live status snapshot |

## 7. 문서 정적 검증

Target과 correction report를 모두 대상으로 최종 재검증한다.

| 검사 | Target | Correction report |
|---|---|---|
| Local Markdown link target | `PASS`; local 32, broken 0 | `PASS`; local 1, broken 0 |
| GFM/local fragment | Fragment link 0, broken 0 | Fragment link 0, broken 0 |
| Heading/fence structure | H1 `1`, H2 `18`, H3 `63`, H4 `2`, fence closed | H1 `1`, H2 `8`, H3 `5`, fence closed |
| Trailing whitespace/tab/CRLF/NUL | 모두 `0` | 모두 `0` |
| EOF newline | `PASS` | `PASS` |
| Required/additional manifest | `PASS` | N/A |
| Scoped `git diff --check` | diagnostic `0` | diagnostic `0` |
| Untracked-aware `git diff --no-index --check` | diagnostic `0`; raw `1`은 file difference | diagnostic `0`; raw `1`은 file difference |

Maven/Java test는 실행하지 않았다. 이 correction은 문서 전용이고 actual Phase 07 test/type이 0이며 current reactor는 unaccepted Phase 00 concurrent work다. Test 0개의 build success를 evidence로 만드는 대신 exact future failure contract를 문서화했다.

## 8. 최종 판정

`HG07-R001`~`HG07-R005`는 모두 target 수정, source/WP/test/evidence/DoD/trace 연결과 정적 검증으로 교정됐다. 실제 구현 blocker와 OPEN/GATED/deferred 상태는 보존했다. Final recheck placeholder는 없고 target SHA-256은 metadata, hash table과 footer에서 모두 `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd`로 일치한다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG07-R001, HG07-R002, HG07-R003, HG07-R004, HG07-R005
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d
TARGET_HASH_AFTER: 9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd
