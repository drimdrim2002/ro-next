# Phase 09 사람용 구현 가이드 correction 01

```yaml
correction_round: "01"
correction_status: COMPLETE_AWAITING_INDEPENDENT_RECHECK
review_verdict_at_input: CHANGES_REQUIRED
addressed_findings: 6
deferred_findings: 0
implementation_status: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status: NOT_ACCEPTED
entry_gate_status: BLOCKED
source_thread_id: 019fa957-eadc-74d1-ae44-6d2957482856
correction_task_id: 019fa9be-5387-7002-913c-b95ead05af28
target: docs/implementation/human-guides/phases/phase-09-human-implementation-guide.md
review: docs/implementation/human-guides/reviews/phase-09-review.md
owned_writes:
  - docs/implementation/human-guides/phases/phase-09-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-09-correction-01.md
code_pom_test_write: NONE
git_stage_commit_push_worktree: NONE
validation_snapshot: 2026-07-29T02:33:10+09:00
timezone: Asia/Seoul
branch_observed: codex-implementation
head_observed: 7cc890ee1d0805df5ae14b633127fade4f978639
```

## 1. 결과와 소유권

[검토 대상 가이드](../phases/phase-09-human-implementation-guide.md)에
[사람용 검토](../reviews/phase-09-review.md)의 F-HG-P09-001~006을 모두 반영했다.
문서 계약의 빈틈은 닫았지만 Phase 09 entry, 구현, evidence와 acceptance는 열지 않았다.
구현자는 여전히 accepted Phase 00/07/08 receipt와 canonical Phase 09의 5개
cross-phase blocker resolution을 기다려야 한다.

이번 correction의 write ownership은 위 두 문서뿐이다. Java, POM, test, evidence,
progress registry와 다른 Phase 문서는 수정하지 않았고 stage/commit/push/worktree
operation도 수행하지 않았다. Shared checkout에서 관찰된 다른 변경은 concurrent
작업자의 것으로 보존했다.

역할 경계:

| 역할 | 책임 | 이 correction이 하지 않은 일 |
|---|---|---|
| Human-guide correction owner | Finding을 target의 contract/WP/oracle/DoD/trace에 연결 | 구현·acceptance 판정 |
| Architecture/Data Integrity | Encoding/digest/identity 승인 | 임의 codec 또는 checksum default 생성 |
| Platform Security | Non-ambient access, event forbidden field, maintenance principal 승인 | Production authorization 부여 |
| Records/Operations | Retention/purge policy와 restore authority 승인 | Retention day 또는 actual delete 활성화 |
| Phase 09/11 owner | S3 module/command/environment/evidence 경계 승인 | Provider owner 추정 |
| Independent reviewer/scheduler | Correction 재검토, evidence/acceptance/status 권위 | Correction author의 self-acceptance |

## 2. Hash manifest

### 2.1 Target와 review

Hash algorithm은 SHA-256이고 line count는 POSIX text line 기준이다.

| Artifact | Before/input | After/validation | Lines | 판정 |
|---|---|---|---:|---|
| Target guide | `67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54` | `09b32c3a821b6ad68ee4aa5a0322d01af07ee08390e3650b80c8de8339b46038` | 2727 | Correction 반영 |
| Human review | `a4c9f42905884f76a7729ff024c262f693697c05d3bd1af6eee4f9d0ca356d4d` | 동일 | 257 | Read-only, 불변 |

Target before hash는 review가 판정한 exact bytes와 일치한다. Target after hash는 이
report를 만들기 직전 validation snapshot에서 다시 계산했다.

### 2.2 Authority와 design source snapshot

사용자 고정 canonical 5문서:

| Source | SHA-256 | Lines |
|---|---|---:|
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 1648 |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 1886 |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 1019 |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 3822 |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 87 |

Current map, implementation map과 original Phase 09 source:

| Source | SHA-256 | Lines | 사용 |
|---|---|---:|---|
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 1607 | Current domain mapping cross-check |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 1469 | Current architecture mapping cross-check |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 240 | Authority/15-Phase/index |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 943 | Entry/exit/evidence/rollback |
| [Canonical Phase 09](../../phases/phase-09-object-storage-no-database.md) | `ac08d10a63f7d0bd86a74fa61c1d220b0619a9c16c7d50fe0017cfe9afc8bb3b` | 1967 | Original design/oracle/WP |
| [Canonical Phase 09 review](../../reviews/phase-09-review.md) | `e49a441d0ccedb9e13b5b5c0f2614bef710dc68a1d587a3c65a5e430bf7a0b5d` | 423 | F-P09 blockers/corrections |
| Human-guide README | `ad64de533a4e45984ae30be12c57d969a36efd4dc8453e949ec4efc992f6b18a` | 92 | Human-guide workflow |

인접 human guide와 mutable progress는 correction 중 concurrent drift가 있었다.
따라서 하나를 “현재 authority”로 합치지 않고 input/validation을 각각 고정한다.

| Source | Correction input SHA-256 | Validation SHA-256/시각 | 해석 |
|---|---|---|---|
| [Phase 08 human guide](../phases/phase-08-human-implementation-guide.md) | `2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5` | `610ac2e6ee3deb20a088b4948f6c644c4073956078bbce5a381dd9d016038fd9` at 02:33:10 | Input 의미를 읽은 뒤 concurrent drift; acceptance 아님 |
| [Phase 10 human guide](../phases/phase-10-human-implementation-guide.md) | `c1649837bbb057ba3272a1c0964782477d32b764e2354ed2c06a94cf14839290` | `908bca41eb3d4f2456d397abfde163b29b550c8c8380af8238eb9a74bb0c86a3` at 02:33:10 | Consumer contract cross-check, mutable guide |
| Implementation progress | `5696065eb923c50c00e6fa7d5ed2e13c30894e77fcc7a8009fcd36b5d2d8e7cd` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` at 02:33:10 | Scheduler/progress 관찰, source authority 아님 |
| Human-guide progress | `NOT_ADMISSIBLE_NO_EXACT_AUTHORING_HASH` | `069518a1639eb7d301f9fb08e0a8052efdbda887bb552386fb763a8218c21cc6` at 02:33:10 | Workflow 관찰, implementation evidence 아님 |

최초 human-guide progress authoring 관찰은 exact 64-hex와 timestamp가 함께 남지
않았으므로 hash authority로 사용하지 않았다. Target은 exact correction/validation
receipt만 시점 판정에 사용하고, 불완전한 역사 값은 superseded로 취급한다.

## 3. Authority 시점과 live inventory

Authority 적용 순서는 사용자 고정 지시 → canonical 5문서 → exact question register
상태 → current domain/architecture map cross-check → implementation plan/phase/review다.
Dated Domain/Architecture의 과거 `Q-INFRA-01 DEFERRED`와 exact question register의
`Q-INFRA-01 RESOLVED`가 충돌하면 register와 Canonical Master의 승인 record를
적용한다. 단, 이 resolution은 S3 implementation owner, provider adapter 또는 production
authority를 부여하지 않는다. 해소되지 않은 충돌은 `OPEN/GATED`,
`BLOCKED_PENDING_CONTRACT`, `deferred`, `EXPERIMENT_REQUIRED`로 남긴다.

Target은 inventory를 다음 네 층으로 분리했다.

| 층 | 내용 | Acceptance authority |
|---|---|---:|
| `AUTHORING_SNAPSHOT` | 최초 작성 중 관찰, exact timestamp가 없으면 superseded | 없음 |
| `HEAD_BASELINE` | Commit `7cc890ee1d0805df5ae14b633127fade4f978639` tree provenance | 없음 |
| `LIVE_OBSERVATION` | Exact timestamp의 worktree bytes/module/test/progress | 없음 |
| `ACCEPTED_EVIDENCE` | Scheduler가 승인한 immutable predecessor/implementation/test receipt | 있음 |

Validation 시점 live inventory:

| 항목 | `2026-07-29T02:32:21+09:00` 또는 바로 뒤 02:33 validation | 판정 |
|---|---|---|
| Branch/HEAD | `codex-implementation` / `7cc890ee1d0805df5ae14b633127fade4f978639` | Source provenance only |
| Root POM | Live Git object `1dc675ba17b7f2202f34a22131f152cc2868b075`, HEAD blob `f8a411eadd4a5c01d8dd09fdea462738ca63d65f` | Concurrent unaccepted Phase 00 drift |
| Reactor | Workspace-relevant POM 13개; root `rpdptw/build/legacy`; dependency-owned `node_modules` example POM 2개는 제외 | Accepted reactor 아님 |
| Stable scaffold | `rpdptw` 6 child modules, `build` 2, `legacy` 1 | Phase 00 scaffold |
| RPDPTW Java | Main Java 23개, 전부 `package-info.java`; test Java 0 | Phase 09 production/test 0 |
| Build tests | Test Java 11개 | Phase 00 architecture/fixture test; Phase 09 suite 0 |
| Phase 09 modules | `object-common/memory/filesystem/s3`, `port-contract-tests` 없음 | Not implemented |
| Phase 09 evidence | `E-P09-*` 없음 | Not produced |
| Maven property | Root `failIfNoTests=false` | Build exit 0을 Phase 09 green으로 사용 금지 |
| Accepted evidence | 없음 | Entry/exit 닫힘 |

02:24 snapshot의 11 POM 관찰과 02:32의 13 POM 관찰은 서로 다른 live receipt다.
Target은 전자를 지우지 않고 superseded/current 재사용 금지로 표시했으며, 지속 가능한
판정은 Phase 09 named production type/test/evidence 0뿐이다.

## 4. Finding별 correction

### F-HG-P09-001 — Canonical 필수 oracle와 false-green

- **Target anchor:** [§12.3 Canonical 필수 oracle manifest](../phases/phase-09-human-implementation-guide.md#123-canonical-필수-oracle-manifest), §13, §16, §18.
- **Root cause:** Target의 “후보” test 목록과 broad PASS/DoD가 canonical §10의 exact
  negative branch 전체를 exit gate에 강제하지 않아, 일부 test discovery와
  `failIfNoTests=false` Maven exit 0을 green으로 오인할 수 있었다.
- **Source:** Canonical Phase 09 §10/§12~13, canonical review와 human review
  F-HG-P09-001.
- **Correction:** Canonical method를 key/projection, put/read/streaming, state/worker/CAS,
  ordering/publication, fault, corruption, security/audit, profile, retention/purge,
  architecture/local/provider manifest로 전부 열거했다. 각 ID는 `REQUIRED`,
  `BLOCKED_PENDING_CONTRACT`, `NOT_APPLICABLE_WITH_APPROVAL` 중 정확히 하나이며
  기본값은 `REQUIRED`, 선승인 N/A는 0이다. Exact command/source/runtime/report digest,
  expected/discovered/executed ID set, pass/fail/error/skip, missing/duplicate/unexpected,
  waiver/blocker와 exit code를 evidence schema/DoD/trace에 연결했다.
- **Gate 보존:** Contract/API/provider blocker ID는 삭제·skip하지 않고 blocked red ID로
  남으며 exit에서는 blocked 0이어야 한다.
- **Verification:** Required ID set exact equality, required missing/skip/failure/error 0,
  root/slice reactor 동일 manifest, stale report/zero discovery 불합격 규칙을 확인했다.
- **Residual:** 실제 module/command/report는 아직 없으므로 구현 acceptance는
  `BLOCKED_NOT_IMPLEMENTED`; 문서 finding은 deferred하지 않았다.

### F-HG-P09-002 — Recursive locator-free protected projection

- **Target anchor:** [§9.3 Artifact key/ref](../phases/phase-09-human-implementation-guide.md#93-artifact-keyref-후보), WP-09.1, §12.3.1, §16, §18.
- **Root cause:** `List<ArtifactRef>`가 nested locator/provider/provenance를 protected
  projection으로 다시 끌어들이고 `createdByRun` observation과 semantic identity를
  혼합했다.
- **Source:** Canonical review F-P09-006, human review F-HG-P09-002, integrated design
  identity/security 경계.
- **Correction:** Physical locator나 observation field를 구조적으로 담지 못하는 recursive
  `ArtifactSemanticAuthorityRef`와 `ArtifactIdentityProjection` skeletal contract로
  분리했다. Output graph에서 `ArtifactRef`, `OpaqueLocator`, provider version/token,
  `createdByRun`, timestamp/attempt/trace를 금지하고 verified DAG/cycle rejection,
  allowlist serializer와 top-level+nested relocation byte/digest oracle를 추가했다.
- **Gate 보존:** Exact encoding/digest algorithm은 ADR 전 `OPEN`; type boundary와
  exclusion/relocation oracle만 `REQUIRED`다.
- **Verification:** Nested `ArtifactRef` 재포함, reflection/unknown-field serialization,
  locator/provenance 변화와 cycle을 exact method/DoD/trace에서 fail하도록 연결했다.
- **Residual:** Production codec/vector 승인은 남아 있으나 임의 default는 만들지 않았다.

### F-HG-P09-003 — Conditional purge operation closure

- **Target anchor:** [§9.10 backend contract](../phases/phase-09-human-implementation-guide.md#910-adapter-internal-backend-후보), WP-09.6, §12.3.9, §14, §16, §18.
- **Root cause:** Capability flag는 있었지만 누가 어떤 authority/version으로 delete하고,
  denial/stale/response-loss/confirmed-delete를 어떻게 판정·복구하는지 operation이
  없었다.
- **Source:** Canonical lifecycle/retention requirements, human review F-HG-P09-003.
- **Correction:** Normal repository와 분리된
  `RestrictedObjectMaintenanceBackend.deleteExactIfAuthorized` seam, exact object
  version/protected digest/purge authorization/policy/mark receipt command, exhaustive
  result hierarchy, Records/Operations owner와 Platform Security authorization,
  backend-call-0 denial, response-loss reconciliation와 immutable purge receipt를
  정의했다. Mark/new-ref/stale/quarantine/hold/indeterminate/confirmed-delete별 last
  safe authority와 rollback/restore 조건을 닫았다.
- **Gate 보존:** Production delete, retention day, Object Lock/lifecycle/provider semantics는
  `OPEN/GATED/DISABLED`; Phase 13/14 authority와 무관하다.
- **Verification:** Stale object/purge version, new reference, denied call, quarantine,
  indeterminate receipt와 normal-repository dependency oracle를 manifest/DoD에 연결했다.
- **Residual:** Actual deletion은 policy/provider/restore authority 승인 전 disabled다.

### F-HG-P09-004 — Observability completeness와 forbidden fields

- **Target anchor:** [§9.11 logical observability](../phases/phase-09-human-implementation-guide.md#911-logical-observability와-audit-contract), WP-09.2/09.5/09.6, §12.3.7, §16, §18.
- **Root cause:** Redaction 한 항목만으로 audit를 대표하여 operation별 terminal
  disposition 누락·중복, pointer outcome 불일치와 purge/quarantine receipt gap을
  판정할 수 없었다.
- **Source:** Final Architecture observability/security, integrated operations/evidence,
  human review F-HG-P09-004.
- **Correction:** Provider-neutral `StorageOperationEvent` skeletal contract, safe
  fingerprint/correlation/receipt field, success/same/conflict/denied/corrupt/
  indeterminate/stale/quarantine/hold/purge terminal disposition matrix와 logical
  operation당 terminal event 정확히 1 규칙을 추가했다. Raw tenant/PII/payload/secret/
  locator/object key/opaque token/provider-sensitive exception은 forbidden이다.
- **Gate 보존:** Sink/provider wire encoding과 audit-sink failure policy는 owner 승인 전
  open이나 logical completeness/redaction oracle는 필수다.
- **Verification:** Missing/duplicate/unknown disposition, result-event mismatch,
  pointer-before/after, receipt correlation과 forbidden-field 0을 exact method,
  `E-P09-TENANT`, DoD와 `REQ-OBSERVABILITY`에 연결했다.
- **Residual:** Exact sink encoding/policy decision은 남지만 finding의 logical gap은 없다.

### F-HG-P09-005 — WP-09.1 entry와 last safe point

- **Target anchor:** [§6.6 승인 checkpoint](../phases/phase-09-human-implementation-guide.md#66-코드-변경-전-사람-승인-checkpoint), WP-09.0, §14, §19.
- **Root cause:** “WP-09.1 이후 중단”이 WP-09.1까지 production code/test vector를
  만들어도 된다는 뜻으로 읽혔고 C0/WP-09.1 prerequisite와 충돌했다.
- **Source:** Canonical Phase 09 §4/WP-09.0~1, human review F-HG-P09-005.
- **Correction:** Gate 전에는 문서 읽기, 손 계산, pseudo-type/미실행 fixture 비교만
  `DESIGN_ONLY`로 허용한다. Java production/test, POM/module, API, persisted
  byte/digest vector와 backend operation은 WP-09.1 자체부터 금지한다.
  `BLOCKED_ENTRY_RECEIPT`와 `APPROVED_ENTRY_RECEIPT`, entry 전/구현 후 runtime last safe
  point를 분리해 §6.6/WP-09.0/C0/§19를 동일하게 맞췄다.
- **Gate 보존:** 5개 blocker, key/envelope/digest ADR와 predecessor receipts가 모두
  없으면 Phase 09는 시작하지 않는다.
- **Verification:** “WP-09.1 이후 중단” 원문은 모순을 설명하는 인용 외에는 stop
  instruction으로 남지 않았고 모든 실행 판정은 “WP-09.1 자체 착수 금지”다.
- **Residual:** Entry는 실제로 blocked이며 이것이 의도된 안전 상태다.

### F-HG-P09-006 — Authoring/HEAD/live/accepted evidence 분리

- **Target anchor:** [§5 repository inventory](../phases/phase-09-human-implementation-guide.md#5-실제-repository-inventory-authoring-head-live와-accepted-evidence-분리), metadata, §13.1, §18.
- **Root cause:** Date-only/stale Git object와 “현재” 표현이 authoring 관찰, HEAD,
  concurrent worktree와 accepted evidence를 한 snapshot처럼 보이게 했다.
- **Source:** Human-guide README/progress workflow, human review F-HG-P09-006, live
  Maven/module/test inventory.
- **Correction:** 네 inventory 층과 acceptance authority를 명시하고 02:24 correction
  receipt 및 02:32 validation drift receipt를 exact timestamp/branch/HEAD/object/count로
  분리했다. 과거 값은 superseded로 표기하고, live POM/progress 변화와 Phase 09
  production/test/evidence 0 판정을 별도로 기록했다.
- **Gate 보존:** HEAD/live hash, POM 존재, package-info/build test 성공은 accepted
  predecessor/Phase 09 evidence가 아니다.
- **Verification:** 02:24의 11 POM과 02:32의 workspace-relevant 13 POM drift를
  숨기지 않았고, `failIfNoTests=false`와 Maven reactor closure를 §13.2에서
  fail-closed로 판정했다.
- **Residual:** Shared checkout은 계속 변할 수 있다. 그래서 entry 시 새 exact receipt를
  다시 읽어야 하며 이 correction snapshot도 acceptance로 재사용할 수 없다.

## 5. Gate와 testing closure 보존 확인

| 항목 | Correction 후 상태 |
|---|---|
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED`; test-only fixture가 official 수치가 아님 |
| `Q-VAR-01` | `DEFERRED`; 질문·구현·자동 재개 금지 |
| Phase 13/C-17 | `GATED OPTIONAL`; Phase 14A accepted receipt 전 활성화 금지 |
| Phase 14A | Correctness/quality/performance acceptance owner; production authority 아님 |
| Phase 14B | Official/traffic/cutover authority 별도; Phase 09가 부여하지 않음 |
| Retention/purge | Safety seam/oracle 필수, production delete disabled |
| S3 Phase 09/11 boundary | Owner/module/environment/command/evidence decision 전 blocked |
| Maven reactor | Accepted module set + method-level manifest + slice/root 동일 fingerprint 필요 |
| Phase 09 status | `BLOCKED_NOT_IMPLEMENTED / NOT_ACCEPTED / EVIDENCE_NOT_PRODUCED` |

## 6. Validation record

문서만 수정했으므로 Maven test/build는 Phase 09 evidence로 실행하지 않았다. 현재
Phase 09 module/test가 없고 root `failIfNoTests=false`이므로 그런 실행은 오히려
F-HG-P09-001의 false-green을 재현할 뿐이다.

| 검사 | 명령/범위 | 결과 |
|---|---|---|
| Target/review hash | `shasum -a 256` | Before/review/after 값 일치 |
| Source hash manifest | Canonical 5, maps, README/plan, canonical P09/review, adjacent 08/10, progress | Input/validation snapshot 분리 완료 |
| Link target/fragment | 두 owned Markdown의 local link와 heading slug | PASS, broken 0 |
| GFM heading | Fence 밖 heading level와 duplicate slug | PASS, jump 0, duplicate 0 |
| Fence | Backtick/tilde fence state | PASS, unclosed 0 |
| Whitespace | Trailing whitespace/tab/CR/NUL | PASS, 0 |
| EOF | Final LF | PASS |
| Scoped diff | Owned two paths 상태와 unrelated status 재확인 | PASS, owned write 두 문서뿐 |
| Diff whitespace | Tracked/untracked-aware `git diff --check`/`--no-index --check` | PASS |
| Finding coverage | F-HG-P09-001~006 anchor/root cause/source/gate/verification/residual | PASS, 6/6 |
| Footer | Round/finding/deferred/before/after exact line | PASS |

독립 re-review는 아직 수행되지 않았으므로 correction status는
`COMPLETE_AWAITING_INDEPENDENT_RECHECK`다. 이것은 Phase 09 구현 또는 acceptance
완료 선언이 아니다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: F-HG-P09-001, F-HG-P09-002, F-HG-P09-003, F-HG-P09-004, F-HG-P09-005, F-HG-P09-006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54
TARGET_HASH_AFTER: 09b32c3a821b6ad68ee4aa5a0322d01af07ee08390e3650b80c8de8339b46038
