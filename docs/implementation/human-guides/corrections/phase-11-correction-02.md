# Phase 11 사람용 구현 가이드 correction 02

```yaml
phase: "11"
correction_round: "02"
correction_type: INDEPENDENT_HUMAN_GUIDE_CORRECTION
source_review: docs/implementation/human-guides/reviews/phase-11-review.md
source_correction_01: docs/implementation/human-guides/corrections/phase-11-correction-01.md
canonical_phase: docs/implementation/phases/phase-11-aws-reference-distribution.md
target: docs/implementation/human-guides/phases/phase-11-human-implementation-guide.md
review_recheck_verdict_input: FURTHER_CORRECTION_REQUIRED
finding_scope:
  - HG11-R006
  - HG11-N001
addressed_findings: 2
deferred_findings: 0
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
corrected_at: 2026-07-29T02:56:11+09:00
timezone: Asia/Seoul
owner_role: RPDPTW Phase 11 human-guide correction 02 writer
owned_files:
  - docs/implementation/human-guides/phases/phase-11-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-11-correction-02.md
read_only_inputs:
  - docs/implementation/human-guides/reviews/phase-11-review.md
  - docs/implementation/human-guides/corrections/phase-11-correction-01.md
  - docs/implementation/phases/phase-11-aws-reference-distribution.md
  - AWS official conditional-write, policy-enforcement, SDK v2 PutObjectRequest documentation
prohibited_actions_observed:
  code_or_pom_or_test_change: false
  stage: false
  commit: false
  push: false
  worktree_operation: false
implementation_or_aws_test_execution: NOT_RUN
target_sha256_before: d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657
target_sha256_after: 4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9
```

## 1. 교정 결과와 소유 범위

[Correction 01 재검증의 OPEN finding](../reviews/phase-11-review.md#hg11-r006--open)과
[new regression finding](../reviews/phase-11-review.md#hg11-n001--113-heading-변경이-보존된-원-review의-fragment-link를-끊었다),
[correction 01](phase-11-correction-01.md), corrected Target 전체와
[Canonical Phase 11](../../phases/phase-11-aws-reference-distribution.md) 전체를
읽고 입력 hash를 고정했다. Target과 correction 01은 recheck footer의 SHA-256과
일치했다.

이 correction은 두 owned file만 수정한다. Review/recheck, correction 01,
canonical/current source, README/progress, 다른 guide/correction, Java/POM/test/IaC,
AWS resource와 evidence는 변경하지 않았다. Stage, commit, push와 worktree 작업도
수행하지 않았다. Phase 11의 구현/evidence/deployment/production 상태는 계속
`BLOCKED_NOT_IMPLEMENTED / NOT_PRODUCED / NOT_DEPLOYED / NOT_GRANTED`다.

결과:

1. Canonical 64-case manifest를 byte-exact 보존하고, 별도 versioned
   `phase11-s3-actual-cases-v1` **required** catalog 29개와 same-run
   Failsafe/P11CASE audit를 [Target §11.3.1](../phases/phase-11-human-implementation-guide.md#1131-supplementary-actual-s3-required-case-catalog)에 추가했다.
2. [Target §11.3 legacy alias](../phases/phase-11-human-implementation-guide.md#113-exact-test-classmethod-후보)를
   현재 normative heading 바로 앞에 추가하고 “후보”는 navigation compatibility
   wording일 뿐 현재 status가 아님을 명시했다.

## 2. Frozen input과 before/after hash

SHA-256은 file bytes, Git blob은 `git hash-object` 결과다. Read-only input의
before/after는 byte-identical이다.

| Artifact | Before / frozen SHA-256 | After SHA-256 | Git blob before → after | Lines after | 판정 |
|---|---|---|---|---:|---|
| Target guide | `d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657` | `4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9` | `4c2bb22953eea363e9485167fd002d8e3fc2ba8b` → `28ee2c245a1cf4f167a98a28ed7ac0cdb8277711` | 2,386 | Intended R006/N001 correction |
| Human-guide review/recheck | `3e0193a921cbff47a08d111fd2b83d59f6161938a3519e7a309065c95e7773b0` | same | `a19d6cb226bbbb8963632092edd194cea5bedd84` → same | 792 | Read-only |
| Correction 01 | `211cc2558f18a21ac2b08b4d5e2fddab1958a574953d0d7f09da7fd7f152caac` | same | `fc6d192dee76b271e10d6e6346b74f15b1404b44` → same | 342 | Read-only |
| Canonical Phase 11 | `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728` | same | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` → same | 1,833 | Read-only |

Target before object와 세 read-only input object를 Git object database에서 다시
읽을 수 있음을 확인했다. 따라서 untracked Target도 before blob 대 current file의
no-index diff/check로 검증할 수 있다.

### 2.1 AWS official source 의미

Review가 인용한 다음 primary source를 correction 시점에 다시 읽었다.

| Source | 이 correction에 고정한 provider observation | 고정하지 않은 것 |
|---|---|---|
| AWS S3 [conditional writes](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes.html) | `If-None-Match`/`If-Match`, 409/412, enabled current version/delete marker, `PutObject` 409 retry와 `CompleteMultipartUpload` 409 후 전체 re-init 차이 | Logical state/result, production versioning mode, retry 수치 |
| AWS S3 [bucket-policy enforcement](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes-enforce.html) | Multipart creation operation의 conditional-header 예외, conditional-write-enforced destination의 `CopyObject` no-header 403/header-present 501 | Copy enablement, migration/rollback policy |
| AWS SDK v2 [`PutObjectRequest`](https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/model/PutObjectRequest.html) | `ifMatch`/`ifNoneMatch` request surface와 409 후 fetch/retry 안내 | Body replayability 보장, application retry identity, hidden SDK/provider default 승인 |

AWS output은 subject observation일 뿐 expected semantic oracle가 아니다. Exact key
encoding, versioning mode, multipart threshold, retry count, SDK/service/provider
default와 production choice는 Storage/Security/Ops owner ADR 전 계속
`OPEN/ADR_REQUIRED`다.

## 3. Finding별 correction

### HG11-R006 — supplementary actual-S3 required catalog와 two-manifest audit

**Target anchors**

- [§11.3.1 supplementary catalog](../phases/phase-11-human-implementation-guide.md#1131-supplementary-actual-s3-required-case-catalog)
- §8.1 future tree, §8.2.1 report audit, WP11-2/7/8, §10.1 command map
- §11.5 false-green, §11.7 future command, §12.3 evidence, §14.3/§14.5 exit와 §16 traceability

**Root cause**

Correction 01은 §9.1.1 prose outcome table에 operation별 의미를 복원했지만 그 새
actual-AWS 필수 scenario를 canonical 64 expected set 밖의 versioned
machine-readable inventory로 만들지 않았다. Canonical
`S3ObjectStorageBackendAwsIT` 세 method가 green이면 current-version,
multipart/copy/replay/credential case 일부가 discovery되지 않아도 기존
`64/64/64` audit가 통과할 수 있었다.

**Source와 correction**

Canonical Phase 11 §6.1/§7.3/§12.2~§12.4, original Phase 11 review
`F-P11-002`, recheck `HG11-R006`와 위 AWS official source를 대조했다. Canonical
`phase11-required-tests-v1` 64 case/27 class/block digest를 그대로 두고, 다음
partition의 supplementary 29 case를 exact JSON bytes로 추가했다.

| Partition | Count | Stable case 범위 |
|---|---:|---|
| Immutable create/state CAS/publication CAS × 409/412/deny/timeout/response-unknown | 15 | `P11S3-IMM-*`, `P11S3-STATE-*`, `P11S3-PUB-*` |
| Enabled/suspended/delete-marker current state | 3 | `P11S3-VERSION-*` |
| PutObject retry 대 CompleteMultipartUpload re-init | 2 | `P11S3-RETRY-PUT-409`, `P11S3-REINIT-MPU-409` |
| Multipart completion/response-unknown/orphan cleanup | 3 | `P11S3-MPU-*` |
| Copy policy 403/501 | 2 | `P11S3-COPY-POLICY-*` |
| Replayable/non-replayable body | 2 | `P11S3-BODY-*` |
| Cross-tenant/missing-scope credential deny | 2 | `P11S3-CREDENTIAL-*` |
| **합계** | **29** | Unique case/report identity 29 |

각 case는 stable `caseId`, `fixtureRef`, `providerProbe`, `independentOracle`,
`passCriterion`, `sensitivityFaultId`, `P11CASE:<caseId>` report ID를 갖는다.
Catalog JSON exact bytes SHA-256은
`279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81`이다.

**Gate와 negative controls**

Same-run auditor는 canonical `C`와 supplementary `S`를 독립 multiset으로 읽는다.

```text
canonical:     expected/discovered/passed = 64/64/64
supplementary: expected/discovered/passed = 29/29/29

for both sets independently:
  missing = duplicate = unexpected = unmapped = failure = error = skipped = stale = sensitivity-missing = ∅
```

어느 count/set/digest/run/profile/effective-POM/deployment identity라도 다르면
Failsafe/P11CASE auditor는 non-zero다. Supplementary receipt는 두 manifest digest,
fixture bytes/probe/oracle receipt와 normal/sensitivity disposition을 함께 기록한다.
Canonical case로 supplementary case를 대체하거나 반대로 count를 채우지 않는다.

Negative controls는 29개 모두 unique `sensitivityFaultId`를 가지며 blind retry,
stale token reuse, last-write-wins, DENIED→NOT_FOUND collapse, timeout/unknown
false-success, delete-marker auto-ABSENT, multipart upload ID reuse/current deletion,
CopyObject fallback, partial stream replay와 ambient/shared credential fallback을
각각 red로 만든다. Sensitivity receipt 누락은 normal 29/29/29이어도 fail이다.

**Verification**

- Canonical block: 64줄, SHA-256
  `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1`,
  correction 전 digest와 동일.
- Supplementary JSON: parse 성공, declared/actual/unique case/unique report
  `29/29/29/29`; required field 누락 0; `P11CASE:` prefix 누락 0.
- Matrix partition `15+3+2+3+2+2+2=29`; operation matrix의 각
  immutable/state/publication × 다섯 observation 조합 누락·중복 0.
- Evidence schema, WP/command/report audit, exit checklist와 traceability가 두
  manifest digest/count를 함께 요구한다.

**Residual**

Exact key encoding, versioning mode, multipart threshold, part plan, retry
count/backoff, credential mode와 production policy는 계속 owner ADR와 actual
isolated AWS evidence 전 `OPEN`이다. 이 문서 correction은 AWS test를 실행하거나
구현/evidence/acceptance를 합성하지 않는다.

### HG11-N001 — legacy §11.3 inbound fragment compatibility

**Target anchor**

- [Legacy alias `113-exact-test-classmethod-후보`](../phases/phase-11-human-implementation-guide.md#113-exact-test-classmethod-후보)
- 바로 뒤 current normative
  [§11.3 heading](../phases/phase-11-human-implementation-guide.md#113-canonical-required-test-manifest와-report-oracle)

**Root cause**

Correction 01이 교육용 36-case “후보” heading을 canonical 64-case normative heading으로
교체하면서 preserved human-guide review line 269가 가리키는 generated fragment를
compatibility set에 남기지 않았다. Target/correction 01 outbound link만 검사해
immutable inbound review trace를 놓쳤다.

**Source와 correction**

Recheck `HG11-N001`, original human-guide review의 legacy link와 correction 01
`HG11-R003`를 대조했다. Current §11.3 heading 바로 앞에 다음 explicit HTML id를
정확히 한 번 추가했다.

```html
<a id="113-exact-test-classmethod-후보"></a>
```

바로 다음 visible compatibility note는 legacy “후보”가 navigation alias일 뿐 현재
normative status가 아니며 current §11.3 canonical manifest/report oracle이
authority임을 명시한다.

**Gate와 verification**

Target, correction 01과 preserved human-guide review의 모든 local path/fragment를
non-fenced GFM heading slug와 explicit HTML id에 대조했다. 기존 유일한 broken
legacy fragment는 alias에 resolve되고 missing file/fragment는 각각 0이다. Alias
ID는 target에 1개이며 current heading slug와 충돌하지 않는다.

**Residual**

Navigation compatibility가 복원된 뒤 의미 residual은 없다. Alias는 36-case
subset, old status 또는 canonical 64/supplementary 29를 대체하는 acceptance
manifest가 아니다.

## 4. 보존한 gate와 authority

- Canonical 64 manifest exact bytes/digest와 correction 01의 기존 finding closure를
  보존했다.
- AWS target 선택과 implementation/evidence/deployment/production authority를
  분리했다.
- Phase 09가 logical owner, Phase 11이 provider observation mapper,
  Storage/Security/Ops ADR이 operation policy owner라는 방향을 유지했다.
- Original bytes/current accepted state/publication pointer 보존, exactly-one CAS
  winner, orphan-only cleanup과 provider-native deny를 유지했다.
- AWS output golden-update, production helper와 oracle helper 공유, emulator-only
  actual evidence와 stale report를 금지했다.
- Evidence DAG는 standalone pre-review evidence `M → review R → receipt(M,R)`의
  단방향이며 Phase 12 manifest를 역참조하지 않는다.
- `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`, Phase 12/13/14
  gate와 `productionAuthority=false`를 유지했다.

## 5. 검증 결과

| 검사 | 결과 | 근거 |
|---|---|---|
| Before/after hash | `PASS` | Target before blob/SHA가 recheck와 일치; after SHA/Git blob/line 수 고정 |
| Canonical manifest 보존 | `PASS` | 64 unique, 27 class, SHA-256 `98fa3535…e0a1`; correction 전과 동일 |
| Supplementary catalog | `PASS` | JSON parse, version `phase11-s3-actual-cases-v1`, count/unique ID/unique report `29/29/29`, SHA-256 `279cbcf4…1d81` |
| Required per-case fields | `PASS` | 29개 모두 fixture/probe/independent oracle/pass/sensitivity/report ID present |
| Negative controls | `PASS DOCUMENT` | 29 unique sensitivity fault, sensitivity-missing non-zero; actual provider run은 entry gate 전 `NOT_RUN` |
| Two-manifest audit/evidence/exit | `PASS DOCUMENT` | `64/64/64`와 `29/29/29` 독립 AND, 두 digest 봉인, 모든 지정 failure set non-zero |
| Alias 위치/유일성/status | `PASS` | Current §11.3 바로 앞 id 1, duplicate 0, compatibility-only 설명 존재 |
| Target local path/GFM fragment | `PASS` | Local 30, fragment 23, broken 0 |
| Correction 01 local path/GFM fragment | `PASS` | Local 34, fragment 10, broken 0 |
| Human-guide original review/recheck local path/GFM fragment | `PASS` | Local 47, fragment 14, broken 0; legacy §11.3 resolve |
| Canonical original Phase 11 review local path/GFM fragment | `PASS` | Local 31, fragment 12, broken 0 |
| Correction 02 local path/GFM fragment | `PASS` | Local 9, fragment 7, broken 0 |
| GFM heading structure | `PASS` | Target H1/H2/H3/H4 `1/17/66/4`; correction 01 `1/6/9/2`; human review `1/10/18/8`; canonical review `1/9/13/0`; report `1/6/3/0`; level jump 0, explicit id duplicate 0 |
| Fence | `PASS` | Target/correction 01/human review/canonical review/report marker `98/0/2/2/6`; 모두 even/closed, language marker 정상 |
| Whitespace/encoding/EOF | `PASS` | Owned files trailing whitespace/tab/CRLF/NUL 0, UTF-8 read 성공, final LF |
| Scoped tracked diff check | `PASS` | `git diff --check -- <두 owned file>` whitespace diagnostic 0 |
| Untracked-aware target diff | `PASS` | Frozen before Git blob 대 current Target 1 file, `+440/-19`; no-index whitespace diagnostic 0, raw exit 1은 intended content difference |
| Untracked-aware new report | `PASS` | `/dev/null` 대 new report 1 file, `+291`; no-index whitespace diagnostic 0, raw exit 1은 new content |
| Scoped/untracked write set | `PASS` | Human-guide tree가 기존부터 untracked여서 scoped status는 Target/correction01/review/report를 `??`로 보지만, correction01/review frozen hash는 불변이고 intended Target + 새 correction 02만 write; unrelated working-tree 변경 보존 |
| Implementation/Maven/AWS test | `NOT_RUN_BY_DESIGN` | Phase 11 module/profile/Failsafe/test/resource와 accepted entry가 없으므로 문서 gate를 green evidence로 위장하지 않음 |
| Stage/commit/push/worktree | `PASS` | 수행하지 않음 |

Link 검사는 fenced code를 제외하고 local Markdown target을 실제 path로 resolve한 뒤
fragment를 대상 문서의 GFM heading slug 또는 explicit HTML id와 대조한다.
Target/correction 01/preserved review의 broken 합계는 0이며, 특히 recheck 전 유일한
broken legacy §11.3 inbound fragment가 복원됐다.

## 6. 판정과 residual

`HG11-R006`과 `HG11-N001`의 required document correction을 모두 반영했다. 이는
supplementary expected set과 navigation trace를 판정 가능하게 만든 것일 뿐
Phase 11 adapter/test/IaC 구현, AWS deployment/evidence, independent correction
recheck, post-review receipt 또는 scheduler acceptance가 아니다.

남은 의도적 residual:

1. Exact key encoding/versioning/multipart/retry/credential/network/KMS/quota/
   retention/cost/production 값은 named owner ADR와 actual evidence 전 open이다.
2. Phase 00/06~10 accepted implementation/handoff가 없어 Phase 11 entry는 닫혀 있다.
3. Live POM/module/profile/Failsafe/test/resource가 없으므로 두 manifest audit는
   future executable contract이며 현재 provider green을 주장하지 않는다.
4. 이 correction 뒤 별도 independent recheck가 두 finding closure와 target hash를
   승인해야 한다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG11-R006, HG11-N001
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657
TARGET_HASH_AFTER: 4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9
