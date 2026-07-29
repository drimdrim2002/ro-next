# Phase 11 사람용 구현 가이드 correction 01

## 1. Metadata와 ownership

| 항목 | 값 |
|---|---|
| Correction round | `01` |
| Correction 시각 | `2026-07-29T02:36:43+09:00` (`Asia/Seoul`) |
| 역할 | Phase 11 사람용 guide correction 01 담당; original author/reviewer와 분리 |
| Target | [phase-11-human-implementation-guide.md](../phases/phase-11-human-implementation-guide.md) |
| Finding input | [phase-11-review.md](../reviews/phase-11-review.md) |
| 허용 변경 | Target guide와 이 correction report만 |
| 금지 범위 준수 | Canonical/current source, 다른 guide/review/correction, README/progress, Java/POM/test/deployment를 수정하지 않음 |
| 구현/acceptance 주장 | 없음. Phase 11은 `BLOCKED_NOT_IMPLEMENTED / NOT_DEPLOYED / NOT_PRODUCED / NOT_GRANTED` |
| Finding 결과 | `7/7 ADDRESSED`, deferred `NONE` |
| Target 문서 상태 | `CORRECTED_ROUND_01 / AWAITING_INDEPENDENT_REVIEW` |
| Status authority | 총괄 scheduler만 Phase 상태를 바꿀 수 있음 |

이 correction은 Phase 11 구현 계약을 사람이 판정할 수 있게 고친 문서 작업이다. AWS
resource를 생성·변경·삭제하지 않았고 Maven build, stage, commit, push 또는 worktree
작업도 수행하지 않았다. 다른 작업자의 Phase 00 candidate와 모든 unrelated 변경을
그대로 보존했다.

## 2. Hash와 correction baseline

SHA-256은 file bytes, Git blob은 `git hash-object` 결과다.

| File | SHA-256 before | SHA-256 after | Git blob before | Git blob after |
|---|---|---|---|---|
| Finding input review | `0ffdc4ce70364dae63dca266839a34692c1a821e1620614dec57ba9e9dcd29a6` | 동일 | `1dfdb093c3c436a9e2b4ec2d2d9874f94b38be41` | 동일 |
| Target guide | `b38705bc9289b9b5d1c9159ce20cc7a6b4112c05515dcbcfd3c04ee5182c94ee` | `d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657` | `1204cd4ed501921f5a4c0dc6edd1f8e70f6fa729` | `4c2bb22953eea363e9485167fd002d8e3fc2ba8b` |

Target before hash는 review metadata와 correction 시작 재계산값이 일치했다. Finding
input review는 read-only로 사용했고 bytes를 바꾸지 않았다.

### 2.1 Authority와 source snapshot

Correction source snapshot은 `HEAD
7cc890ee1d0805df5ae14b633127fade4f978639`, branch `codex-implementation`,
`2026-07-29T02:36:43+09:00`에 고정했다.

#### Current map과 canonical 5문서

| Source | Git blob | SHA-256 | 직접 읽은 범위 |
|---|---|---|---|
| [Current docs map](../../../README.md) | `13f1b3b2dea038b8e0b466c138f5f59299413125` | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | 권장 읽기 순서, 문서 계층 |
| [Canonical Master](../../../master-design.md) | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | §1~§4, §13~§17 |
| [Canonical Domain](../../../domain-design.md) | `ace117c380466b733994a1fbb2a95d31e41b3959` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | §1~§3, §9~§16, §18~§21 |
| [Canonical Architecture](../../../architecture-design.md) | `81495ff448d0e618ab3563e8ff80614fb1028acf` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | §1~§7, §10~§20, §22 |
| [Integrated design](../../../architecture-domain-implementation-design.md) | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | §1~§3, §12~§16, §19~§28 |
| [Question register](../../../master-design-open-questions.md) | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 전체와 exact `Q-BENCH-02/Q-INFRA-01/Q-VAR-01` |

날짜 문서도 source-governance semantic diff 대상으로 읽고 별도 고정했다.

| Historical/conflicting source | Git blob | SHA-256 | 해석 |
|---|---|---|---|
| [2026-07-26 Domain](../../../2026-07-26-domain-design.md) | `0a02ba4c77a402455e3d80b76969dca28831b1e6` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | Plain Domain supersession을 self-declare하지만 current map과 충돌 |
| [2026-07-26 Architecture](../../../2026-07-26-architecture-design.md) | `d51339e251dee1e032e711144dc63d6d07d7323b` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | Plain Architecture supersession과 stale `Q-INFRA-01` 상태가 current map/register와 충돌 |

이번 사용자 고정 canonical 5와 current top-level map을 따라 plain Domain/Architecture를
Phase 11 가이드 authority로 채택했다. 날짜 문서의 self-declared supersession은 삭제하거나
은폐하지 않고 source-governance residual blocker로 남겼다.

#### Implementation, original Phase/review와 handoff source

| Source | Git/live blob | SHA-256 | 직접 읽은 범위 |
|---|---|---|---|
| [Implementation README](../../README.md) | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | §0~§7 |
| [Master realization plan](../../master-realization-plan.md) | `d7f6be4fff0089204fbdb52f731b2348407f36eb` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | Phase 08~14, evidence/gates |
| [Execution progress](../../execution-progress-and-results.md) | Live `0419f69199b3140dd44020f78278b1352e6517b8`; HEAD `250aa90ae568a6b32ec905fa5ee456d430ff72cf` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | §1~§10, Phase 00 review 02/fix 02와 Phase 11 status |
| [Actual Phase 08](../../phases/phase-08-application-ports-local-runtime.md) | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` | `15dbcab53fc00fc4d3062c5bdb0eb1f072fd902bfbb59b322859f30b090a5f0f` | §7.3~§7.5, §9.1~§9.3, §16 |
| [Actual Phase 09](../../phases/phase-09-object-storage-no-database.md) | `99a5b0df5531a65964272f423cc0ccca4d4f1430` | `ac08d10a63f7d0bd86a74fa61c1d220b0619a9c16c7d50fe0017cfe9afc8bb3b` | §7.2, §7.5~§9.6, §14~§15 |
| [Actual Phase 10](../../phases/phase-10-provider-neutral-coordinator.md) | `2b909924008df6c6f34d7d4d4399c8fdf6b6830a` | `e7656b800020592731e07a1f321a870fc7bdabb021925ed3b13ebe311b9ba8cd` | §6.3~§8.6, §13~§14 |
| [Canonical Phase 11](../../phases/phase-11-aws-reference-distribution.md) | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` | `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728` | 전체, 특히 §6~§15 |
| [Original Phase 11 review](../../reviews/phase-11-review.md) | `74377517a4018da73bc0e0bc8bf033ff7a3b0832` | `52ad3f6803c301c00dd7191fe0e78777d9b7b7e1a54da6d1db3a87dcb35625bc` | `F-P11-001~008`, residual blocker |
| [Actual Phase 12](../../phases/phase-12-provider-substitution.md) | `63b9defb25d7771bce590d593db9485367724918` | `5f243b2900afe31801ab1c47b9c2467a6344c42b7b27f49ee5c5b51d1c21de1a` | Phase 11 evidence consumer와 reverse-edge 금지 |
| [Actual Phase 14](../../phases/phase-14-official-calibration-cutover.md) | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` | `c8f3b4fd2e48d35547d7e2fe31169a5e0a882730859ac97d9c5f60a165802eae` | 14A/14B와 production gate |
| [Human Phase 08](../phases/phase-08-human-implementation-guide.md) | Live untracked `2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223` | `2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5` | §9.4~§9.6와 direct handoff |
| [Human Phase 09](../phases/phase-09-human-implementation-guide.md) | Live untracked `1c31c4747362317c7f4ec8fb63c5f57627019769` | `67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54` | §9.5~§9.10와 §17 |
| [Human Phase 10](../phases/phase-10-human-implementation-guide.md) | Live untracked `09f6f24e53167a4e320fc158640c13d1aa0cd29f` | `c1649837bbb057ba3272a1c0964782477d32b764e2354ed2c06a94cf14839290` | §8~§10와 §14.2 |
| [Human Phase 12](../phases/phase-12-human-implementation-guide.md) | Live untracked `d0557e019ec71fbeb1e931d589945949f7ac0074` | `a0e29edfacc21d8a9f23e68b665db0eb62e862fec1f25d7a8031d0a96c2c6fdf` | §3.4, §9~§13와 §16.1 |

### 2.2 Live Maven/module/test inventory

| 항목 | Correction snapshot |
|---|---|
| Root POM | Live blob `1dc675ba17b7f2202f34a22131f152cc2868b075`; SHA-256 `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` |
| Reactor | `node_modules` 제외 POM 13; root `pom` + `rpdptw/build/legacy` |
| RPDPTW source | Main Java 23, 모두 `package-info.java`; concrete production type 0 |
| Tests | `node_modules` 제외 concrete test Java 15; build 10, legacy 5; build test `package-info` 1 별도 |
| Test lifecycle | Surefire `3.5.4`, `failIfNoTests=false`; Failsafe plugin/profile 0 |
| Phase 11 paths | `object-s3`, Step Functions/Lambda adapter, AWS distribution/deployment, port-contract module 모두 없음 |
| Phase 00 status | Review 02 `CHANGES_REQUIRED`; fix 02 `IN_PROGRESS`; evidence `REJECTED_PENDING_FIX_02_REGENERATION`; accepted receipt 없음 |
| Phase 11 status | `BLOCKED_NOT_IMPLEMENTED`, AWS/evidence/production authority 없음 |

Live state는 source authority나 acceptance가 아니다. 구현 시작 시 accepted receipt와 새
inventory snapshot이 다르면 Phase 11 entry를 다시 닫는다.

## 3. Finding별 correction

### HG11-R001 — Canonical authority 경로와 시점을 분리

**Target anchor**

- [§3.1 authority order](../phases/phase-11-human-implementation-guide.md#31-충돌-해소-순서)
- [§3.2 source fingerprint](../phases/phase-11-human-implementation-guide.md#32-검증-가능한-source-fingerprint)
- §3.3 reading order, §16 traceability와 metadata source map

**Root cause**

Implementation README와 original Phase 11의 날짜 문서 명칭을 current `docs/README.md`와
이번 사용자 고정 canonical 5보다 우선해 복제했다.

**Correction/source**

Current map과 Master §1.4에 따라 plain Domain/Architecture blob을 authority로 고정했다.
날짜 문서와 current 문서의 publication/port/test 차이를 target §3.1 semantic-impact
표로 연결하고, source owner가 반대 결정을 내리면 implementation을 중지해 source
index/section diff/WP/test/evidence를 함께 재승인하도록 했다.

**Gate/verification/residual**

`Q-INFRA-01 RESOLVED`는 AWS target 선택만 뜻하며 구현/evidence/production gate는
유지된다. Current 5문서 blob/SHA와 target source rows를 대조했다. Repository 다른
문서의 “Final dated” 표현과 self-declared supersession은 허용 범위 밖 residual이다.

### HG11-R002 — Phase 08/09 direct handoff와 entry check

**Target anchor**

- [§3.3 reading order](../phases/phase-11-human-implementation-guide.md#33-구현-전-정확한-읽기-순서)
- [§3.4 entry gate](../phases/phase-11-human-implementation-guide.md#34-entry-gate를-읽고-서명하는-순서)
- §2.4, WP11-0/2/3와 §16

**Root cause**

문서상 인접 Phase 10/12와 실제 storage/port semantic producer인 Phase 08/09를 혼동했다.

**Correction/source**

Actual/Human Phase 08의 port/access/failure/cancel/deadline와 Actual/Human Phase 09의
exact-key/verified-read/CAS/publication/S3-boundary section을 metadata, fingerprint,
reading order, entry와 traceability에 추가했다. Entry receipt는 source section semantic
diff와 accepted implementation artifact/evidence identity를 함께 확인하며 proposed
signature가 다르면 후보를 폐기하고 owner-approved signature 하나만 채택한다.

**Gate/verification/residual**

Whole-file reciprocal hash는 acceptance 조건이 아니다. Phase 08~10 implementation과
handoff가 아직 unaccepted이므로 source 추가 뒤에도 actual adapter entry는 blocked다.

### HG11-R003 — Canonical required 64 test manifest와 set oracle

**Target anchor**

- [§11.3 canonical manifest](../phases/phase-11-human-implementation-guide.md#113-canonical-required-test-manifest와-report-oracle)
- §10.1, §11.5, §12.3와 §14.5

**Root cause**

교육용 핵심 case 36개를 canonical acceptance inventory로 축약하고 expected set의
version/digest와 runtime report 차집합을 만들지 않았다.

**Correction/source**

Canonical Phase 11 §12.2~§12.4 blob에서 `Class#method()` 64개를 source order로
복원했다. Manifest는 `64 unique / 27 class`, UTF-8/LF/final newline, SHA-256
`98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1`로 고정했다.
Future case catalog가 fixture/digest, independent oracle/pass, owner, report mapping과
`sensitivityFaultId`를 64개 one-to-one으로 가져야 한다.

**Gate/verification/residual**

Expected/discovered/passed `64/64/64`와 missing/duplicate/failed/error/skipped/
renamed-unmapped/unexpected/stale/sensitivity-missing 0이 AND gate다. Embedded block을
canonical source 추출물과 byte 비교해 diff 0/hash 일치를 재현했다. Production helper와
oracle 공유 또는 AWS output golden-update 위험은 independent sensitivity review까지 남는다.

### HG11-R004 — Failsafe/JUnit/profile wiring과 `-am clean`

**Target anchor**

- [§8.2.1 Maven lifecycle](../phases/phase-11-human-implementation-guide.md#821-maven-test-lifecycle-contract)
- §10.1, §11.5와 [§11.7 future commands](../phases/phase-11-human-implementation-guide.md#117-maveniac-명령--현재와-미래를-구분하기)

**Root cause**

`-Dit.test`와 report count를 설명하면서 property를 소비할 Failsafe execution,
JUnit/fixture dependency, profile, effective-POM/report validator를 구현 범위에 넣지 않았다.

**Correction/source**

Future parent/module POM에 pinned Failsafe `3.5.4`, `integration-test`+`verify`, explicit
`*IT` include, JUnit Platform, approved fixture dependency, `aws-local-it`/
`aws-integration`, skip 금지와 effective-POM/profile checker를 요구했다. Same source의
isolated full install, selected no-`-am`, filter-free Phase 11 slice/profile
`-am clean verify`, same-run XML/P11CASE audit 순서를 고정했다.

**Gate/verification/residual**

Profile/plugin/summary/0-test/stale report 중 하나라도 없거나 다르면 non-zero다. Live
POM에는 Failsafe/profile이 실제로 0이므로 future gate는 현재 red다. Phase 00 owner가
다른 plugin version을 승인하면 source/effective-POM/manifest를 함께 재봉인해야 한다.

### HG11-R005 — Java concrete skeleton을 compile-shape로 교정

**Target anchor**

- [§9 Proposed/Open Java contract](../phases/phase-11-human-implementation-guide.md#9-proposedopen-java-계약과-상태-전이)
- §9.1, §9.3와 §9.4

**Root cause**

Interface signature 생략 문법을 concrete `final class`의 Java fence에 넣어 method body
없는 non-abstract declaration이 됐다.

**Correction/source**

각 concrete adapter에 package, provider SDK dependency, constructor injection,
`@Override`와 Java body를 두었다. Body는 의도적인 `UnsupportedOperationException`
contract skeleton이라 완성 adapter가 아니며 assembly에서 금지된다. Phase 09 backend의
`metadataExact`, Phase 08 worker dispatcher signature와 sealed event의 모든 permitted
subtype을 같은 compile-shape에 포함했다.

**Gate/verification/residual**

Java fence static 검사에서 bodyless concrete method line 0, required concrete/event
declaration missing 0을 확인했다. Exact public type/import/signature는 predecessor
acceptance 전 계속 `PROPOSED/OPEN`이며 실제 compile/build evidence는 만들지 않았다.

### HG11-R006 — S3 conditional/versioning/multipart operation contract

**Target anchor**

- [§9.1.1 S3 outcome table](../phases/phase-11-human-implementation-guide.md#911-s3-conditional-operation-versioning과-multipart-판정)
- §6.3~§6.4, WP11-2, §11.3와 §14.3

**Root cause**

Provider 세부를 ADR로 열어 두는 과정에서 actual AWS suite가 반드시 관측·분류할
409/412, current version/delete marker, multipart/copy/body replay와 tenant credential
source까지 제거했다.

**Correction/source**

Phase 09를 logical owner, Phase 11 adapter를 observation mapper, Storage/Security/Ops
ADR을 provider operation owner로 분리했다. Immutable create/state CAS/publication
각각에 success/409/412/deny/timeout/response-unknown, exact reread/revalidate,
retry identity, owner state, failure/rollback과 independent oracle를 고정했다.
`tenantCredentialProviderRef`, `versioningRequired`, multipart/copy/replay policy를
config 후보에 복원했다.

**Gate/verification/residual**

Original bytes/current accepted pointer 보존, exactly-one winner, orphan multipart만
bounded cleanup, provider-native deny가 pass 기준이다. S3를 semantic/default
authority로 승격하지 않았다. Exact versioning mode, key encoding, multipart threshold와
retry 수치는 계속 `OPEN/ADR_REQUIRED`다.

### HG11-R007 — Observability/cost schema의 completeness 판정

**Target anchor**

- [§11.6.1 required schema](../phases/phase-11-human-implementation-guide.md#1161-observability와-cost-required-schema)
- WP11-6, §12.3와 §14.4

**Root cause**

원본의 exact log/metric/alarm/cost catalog와 독립 owner를 추상 명사와 구현자 작성
fixture로 축약했다.

**Correction/source**

Canonical Phase 11 §10의 structured log field를 event-scope별 required set으로,
13개 metric을 exact unit/allowed dimension과 함께, alarm 8종과 usage/cost 8개
category를 normative table로 복원했다. Future schema path, source blob/section,
Ops/Security/FinOps/independent owner와 content digest requirement를 지정했다.

**Gate/verification/residual**

Expected-discovered, unexpected, duplicate key, wrong unit, forbidden field/dimension,
failure-run omission과 freshness를 모두 set으로 판정한다. Run/collection window와
`completeThrough >= usageWindow.end`가 충족되지 않으면 `PENDING_FRESHNESS`다. Exact
retention/evaluation delay/price/cap/affordability는 실제 workload와 owner 승인 전
`OPEN/OWNER_APPROVAL_REQUIRED`이고 production gate는 false다.

## 4. 보존한 invariant, scope와 gate

- AWS target 선택, implementation, provider evidence와 production authority를 별도 상태로 유지했다.
- No-DB, exact key, verified read, immutable create, one authoritative CAS, distinct
  publication precondition, same-state cancel/publish fence와 declared completeness를 유지했다.
- Provider observation, version/ETag/request ID와 completion order를 semantic identity에서 제외했다.
- Phase 12 evidence dependency는 Phase 11 standalone evidence → review → receipt →
  Phase 12-owned manifest 단방향이다.
- `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`, Phase 12 adoption
  gated, Phase 13 `C-17 GATED`, Phase 14A/14B 분리와 14B production authority를 유지했다.
- Complete adapter/source code, AWS deploy, Maven/POM/test implementation과 scheduler
  status 변경은 correction scope 밖으로 보존했다.

## 5. 검증 결과

| 검사 | 결과 | Evidence/해석 |
|---|---|---|
| Finding coverage | `PASS` | `HG11-R001~R007` 각각 target anchor/root cause/source/gate/verification/residual과 연결 |
| Review immutability | `PASS` | Finding input SHA-256/Git blob before/after 동일 |
| Target hash | `PASS` | Before/after SHA-256와 Git blob 기록 |
| Required manifest | `PASS` | Canonical 64/64, unique 64, class 27, source diff 0, SHA-256 `98fa...e0a1` |
| Authority/source snapshot | `PASS` | Current map/canonical 5/dated conflict/implementation/original/handoff/live hashes 고정 |
| Local Markdown link/GFM fragment | `PASS` | Target와 report의 local path/fragment missing 0 |
| Heading structure | `PASS` | 각 file H1 1, level jump 0, canonical title 일치 |
| Fence | `PASS` | 두 file opening/closing parity 정상 |
| Whitespace/EOF | `PASS` | Trailing whitespace/tab/NUL 0, 각 file final LF |
| Java skeleton static syntax | `PASS` | Java fence bodyless concrete method 0, required declaration/subtype missing 0 |
| Maven false-green contract | `PASS DOCUMENT` | Failsafe/effective-POM/profile/JUnit/fixture, selected no-`-am`, slice `-am clean`, fresh report set 명시 |
| S3/operations schema coverage | `PASS DOCUMENT` | 409/412/version/multipart/credential와 log/metric/alarm/cost completeness/unit/freshness anchor 존재 |
| Scoped diff/whitespace | `PASS` | 허용 두 path만 correction output; tracked `git diff --check`와 untracked-aware `--no-index --check` diagnostic 0 |
| Maven/AWS implementation test | `NOT_RUN_BY_DESIGN` | Phase 11 module/profile/Failsafe/test/resource가 없고 entry gate도 닫힘. Root/legacy green은 Phase 11 evidence가 아님 |
| Stage/commit/push/worktree | `PASS` | 수행하지 않음 |

## 6. Residual risk와 다음 판정

1. Repository의 dated/plain Domain/Architecture source governance는 이 두 문서 수정만으로
   완전히 해소되지 않는다. 상위 owner가 source set을 바꾸면 Phase 11 source index와
   semantic impact를 다시 review한다.
2. Phase 00 fix 02와 Phase 06~10 accepted implementation/handoff가 없으므로 Phase
   11 actual implementation은 계속 blocked다.
3. Same-state cancel/publish fence, distinct publication precondition과 durable deadline
   restart signature는 cross-Phase acceptance 전 `OPEN BLOCKER`다.
4. Live POM에는 Failsafe/profile이 없다. 이 correction의 command는 future contract이며
   effective-POM/report sensitivity evidence 전에는 실행 성공을 주장할 수 없다.
5. S3 versioning/key/multipart/retry/credential mode, network/IAM/KMS/quota/retention과
   actual isolated AWS evidence는 owner ADR와 provider test 전 open이다.
6. Observability collection delay, alarm evaluation window, retention, workload class,
   price와 cost cap은 Ops/Security/FinOps 승인 전 open이고 affordability/production은 false다.
7. 이 correction은 새 independent review, evidence manifest, post-review receipt와
   scheduler acceptance를 대체하지 않는다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG11-R001, HG11-R002, HG11-R003, HG11-R004, HG11-R005, HG11-R006, HG11-R007
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: b38705bc9289b9b5d1c9159ce20cc7a6b4112c05515dcbcfd3c04ee5182c94ee
TARGET_HASH_AFTER: d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657
