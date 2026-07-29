# Phase 11 사람용 구현 가이드 correction 03

```yaml
phase: "11"
correction_round: "03"
correction_type: INDEPENDENT_HUMAN_GUIDE_RECHECK_CORRECTION
source_thread_id: 019fa957-eadc-74d1-ae44-6d2957482856
source_review: docs/implementation/human-guides/reviews/phase-11-review.md
source_correction_01: docs/implementation/human-guides/corrections/phase-11-correction-01.md
source_correction_02: docs/implementation/human-guides/corrections/phase-11-correction-02.md
canonical_phase: docs/implementation/phases/phase-11-aws-reference-distribution.md
target: docs/implementation/human-guides/phases/phase-11-human-implementation-guide.md
review_recheck_round_input: "02"
review_recheck_verdict_input: FURTHER_CORRECTION_REQUIRED
finding_scope:
  - HG11-R006
addressed_findings: 1
deferred_findings: 0
corrected_at: 2026-07-29T03:09:38+09:00
timezone: Asia/Seoul
head_observed: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
owner_role: RPDPTW Phase 11 human-guide correction 03 writer
owned_files:
  - docs/implementation/human-guides/phases/phase-11-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-11-correction-03.md
read_only_inputs:
  - docs/implementation/human-guides/reviews/phase-11-review.md
  - docs/implementation/human-guides/corrections/phase-11-correction-01.md
  - docs/implementation/human-guides/corrections/phase-11-correction-02.md
  - docs/implementation/phases/phase-11-aws-reference-distribution.md
implementation_or_aws_test_execution: NOT_RUN_BY_DESIGN
stage_commit_push_worktree: NOT_PERFORMED
target_status: CORRECTED_ROUND_03_AWAITING_INDEPENDENT_RECHECK
```

## 1. 결과와 소유 범위

[Round 02의 OPEN `HG11-R006`](../reviews/phase-11-review.md#hg11-r006--open-1),
[correction 02](phase-11-correction-02.md), correction 01, corrected Target의
§11.3.1/§11.5/§11.7/evidence/§14.5와
[Canonical Phase 11](../../phases/phase-11-aws-reference-distribution.md)을 읽고
입력 hash를 고정했다.

이 correction은 Target과 이 report만 썼다. Review/recheck, correction 01/02,
canonical/current architecture·implementation source, README/progress, 다른 guide,
Java/POM/test/IaC, AWS resource와 evidence는 읽기 전용으로 유지했다. Stage,
commit, push 또는 worktree 작업을 하지 않았다. 이 문서 계약은 Phase 11
implementation/provider evidence/acceptance/deployment/production authority를
만들지 않으며 상태는 계속
`BLOCKED_NOT_IMPLEMENTED / NOT_PRODUCED / NOT_DEPLOYED / NOT_GRANTED`다.

## 2. Frozen source와 before/after hash

SHA-256은 file bytes, Git blob은 `git hash-object` 결과다. Target before hash는
Round 02 recheck footer의 `TARGET_HASH_RECHECKED` 및 correction 02의
`TARGET_HASH_AFTER`와 일치한다.

| Artifact | Frozen/before SHA-256 | After SHA-256 | Git blob before → after | 역할 |
|---|---|---|---|---|
| Target guide | `4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9` | `802463c118c4cb3bb22dbc1d3232cc2a1e101caa70f8cb85a3940d31c53b09d4` | `28ee2c245a1cf4f167a98a28ed7ac0cdb8277711` → `782dbaeecb053378ec4941c1d1365998797c7fd4` | Intended correction |
| Human-guide review/recheck | `1bcdcf4f553297fe492de64a774afcd1999f67194a03f4831365d9a0a0a80dc5` | same | `37bdae0a25b1461551d44ed11b1ec277dd7801bb` → same | Read-only Round 02 input |
| Correction 01 | `211cc2558f18a21ac2b08b4d5e2fddab1958a574953d0d7f09da7fd7f152caac` | same | `fc6d192dee76b271e10d6e6346b74f15b1404b44` → same | Read-only history |
| Correction 02 | `431716418d76f93ee5f07bc8da0be529d9a60678f6a0c0ff0a07a98d68d1317e` | same | `d3d1c489f5fb996d379cb393f0ae8a1b8a6eed56` → same | Read-only previous correction |
| Canonical Phase 11 | `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728` | same | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` → same | Read-only authority |

## 3. Root cause와 corrected anchors

Correction 02는 supplementary case 29개 각각에 unique `sensitivityFaultId`와
sensitivity receipt 존재를 요구했다. 그러나 Target의 executable PASS 식은
`sensitivity-missing=∅`까지만 검사했다. Fault activation receipt가 있어도 mapped
case가 계속 green이거나 exit `0`인 `SURVIVED`, 다른 case/fault/report를 이어 붙인
wrong mapping을 계산·거부하지 않았다. 따라서 모든 receipt를 생성하고 normal
`29/29/29`를 만족하면서 faulty implementation 29개가 전부 살아남는 false-green이
가능했다.

수정한 Target anchor:

- [§8.2.1 lifecycle contract](../phases/phase-11-human-implementation-guide.md#821-maven-test-lifecycle-contract), WP11-2/7/8와 §10.1 command map
- [§11.3.1 supplementary catalog와 receipt schema](../phases/phase-11-human-implementation-guide.md#1131-supplementary-actual-s3-required-case-catalog)
- [§11.5 false-green 방지](../phases/phase-11-human-implementation-guide.md#115-false-green-방지)
- [§11.7 fresh auditor](../phases/phase-11-human-implementation-guide.md#117-maveniac-명령--현재와-미래를-구분하기)
- [§12.3 evidence schema](../phases/phase-11-human-implementation-guide.md#123-evidence-manifest-최소-항목)
- [§14.5 exit](../phases/phase-11-human-implementation-guide.md#145-evidence와-authority), §16 traceability와 §17 자문

## 4. Receipt schema와 29 activation↔red bijection

Canonical 64 manifest와 supplementary 29 JSON catalog의 exact bytes/digest를
보존하기 위해 case JSON을 수정하지 않았다. 대신 §11.3.1에 다음 normative
supplementary sensitivity receipt schema를 고정했다.

```text
caseId = catalog.caseId
reportId = "P11CASE:" + caseId
sensitivityFaultId = catalog.sensitivityFaultId
faultActivationEvidence = non-empty receipt + digest
expectedSensitivityDisposition = RED
normalDisposition = PASS
sensitivityDisposition = RED
sensitivityExitCode = non-zero
same sourceDigest/phase11RunId/effectivePomDigest/profileIdentity/deploymentIdentity
same canonicalManifestSha256/s3ActualCatalogSha256
```

`expectedSensitivityDisposition=RED`는 literal이며 provider output, profile 또는
환경 변수로 완화할 수 없다. `Phase11S3ActualCaseCatalogContractTest`는 provider
run 전에 29개 `caseId ↔ unique sensitivityFaultId ↔ reportId` bijection과 receipt
schema literal을 검사한다. Fresh auditor는 다음 집합과 PASS 식을 실행한다.

```text
activatedSensitivity =
  {caseId | exact catalog mapping의 fault activation evidence와 동일 identity가 있음}
observedRed =
  {caseId | normal PASS이고 mapped sensitivity가 expected RED이며 exit가 non-zero}
sensitivitySurvived =
  {caseId | fault activation receipt는 있으나 mapped case가 expected RED/non-zero가 아님}
wrong-sensitivity-mapping =
  {caseId | case/fault/report가 absent, duplicate, cross-mapped, catalog와 다르거나
            normal/sensitivity의 source/run/profile/deployment/two-digest가 다름}

PASS requires:
  |activatedSensitivity| = |observedRed| = 29
  sensitivity-missing = sensitivity-survived = wrong-sensitivity-mapping = ∅
```

각 receipt 안에서 case ID, unique fault ID, activation evidence, normal `PASS`,
sensitivity `RED`/non-zero disposition을 동일 source/run/profile/deployment 및
canonical/supplementary 두 manifest digest에 결합한다. Normal result와 fault
activation을 서로 다른 identity에서 조합하거나 fault A의 red를 case B에
재사용할 수 없다.

## 5. Gate 연결과 negative controls

| Gate | Required fail-closed 동작 |
|---|---|
| `Phase11S3ActualCaseCatalogContractTest` | 29 unique case/fault/report mapping 또는 literal `expectedSensitivityDisposition=RED`가 누락·중복·변경되면 provider run 전 non-zero |
| `Phase11FreshReportContractTest` | `29/29/29` 외에 activated/red `29/29`와 세 negative set empty를 다음 clean 전에 검사; survived/wrong disposition은 non-zero |
| §11.5/§11.7 | Same-run activation evidence와 disposition의 identity/digest 결합을 요구하고 receipt 존재만으로 green 금지 |
| §12.3 evidence schema | activated/red count가 29가 아니거나 missing/survived/wrong array가 non-empty면 validator non-zero |
| §14.5 exit | Catalog contract, fresh audit, evidence seal을 모두 요구하고 survived/wrong disposition 하나라도 있으면 exit non-zero |

문서식에 적용한 negative control:

| 주입/오염 | 계산 결과 | 판정 |
|---|---|---|
| Activation receipt 1개 누락 | `sensitivity-missing` non-empty, activated 28 | `FAIL` |
| 29개 fault가 모두 활성화됐지만 mapped case가 green 또는 exit `0` | `sensitivity-survived=29`, `observedRed=0` | `FAIL` |
| Fault A receipt를 case B/report B에 연결 | `wrong-sensitivity-mapping` non-empty | `FAIL` |
| 같은 fault ID를 둘 이상의 case에 재사용 | Catalog bijection 위반 | `FAIL` |
| Normal `PASS`와 sensitivity `RED`를 다른 source/run/profile/deployment 또는 digest에서 결합 | `wrong-sensitivity-mapping` 또는 stale identity | `FAIL` |
| Normal case가 실패했지만 sensitivity만 red | `normalDisposition=PASS` 위반 및 passed count 불일치 | `FAIL` |

따라서 receipt 29개가 존재해도 faulty implementation을 검출하지 못하면 PASS할 수
없다. Blind retry, stale-token reuse, last-write-wins, DENIED collapse,
timeout/unknown false-success, delete-marker auto-ABSENT, multipart upload ID
reuse/current-object cleanup, CopyObject fallback, partial stream replay와
ambient/shared credential fallback은 각 catalog fault가 자기 mapped case를
`RED`/non-zero로 만들 때만 sensitivity gate를 통과한다.

## 6. Digest, catalog와 authority 보존

- Canonical required block은 64줄/27 class이고 SHA-256
  `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1`로
  correction 전과 같다.
- Supplementary JSON은 version `phase11-s3-actual-cases-v1`, declared/actual
  `29/29`, unique case/fault/report `29/29/29`, bad report mapping 0이고 exact
  SHA-256
  `279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81`로
  correction 전과 같다.
- Canonical 64와 supplementary 29를 서로 count 대체할 수 없는 two-manifest AND
  gate를 유지했다.
- Exact key encoding, versioning mode, multipart threshold/part plan, retry
  count/backoff, credential mode와 production policy는 owner ADR 및 actual isolated
  AWS evidence 전 계속 `OPEN/ADR_REQUIRED`다. SDK/service/provider default로
  닫지 않았다.
- Evidence DAG `M → independent review R → receipt(M,R)`,
  `productionAuthority=false`, Phase 12/13/14 gate와 scheduler-only status
  authority를 보존했다.

## 7. Verification

| 검사 | 결과 | 근거 |
|---|---|---|
| Before/after hash | `PASS` | Target before가 Round 02 footer와 일치; after SHA/Git blob 고정 |
| Frozen read-only input | `PASS` | Review, correction 01/02, canonical SHA/Git blob 불변 |
| Canonical manifest | `PASS` | 64 lines, digest `98fa3535…e0a1` |
| Supplementary catalog | `PASS` | JSON parse, 29/29, unique case/fault/report 29, digest `279cbcf4…1d81` |
| Receipt schema | `PASS DOCUMENT` | Literal `RED`, normal `PASS`, sensitivity `RED`/non-zero, activation evidence와 same identity/two-digest binding |
| 29 activation↔red bijection | `PASS DOCUMENT` | PASS 식에 activated/red 29/29와 missing/survived/wrong empty가 모두 필요 |
| Negative controls | `PASS DOCUMENT` | Missing, all-survived, cross-mapping, duplicate fault, identity mismatch, non-PASS normal이 모두 non-zero |
| Target local link/GFM | `PASS` | Local 30, fragment 23, broken 0 |
| Correction 03 local link/GFM | `PASS` | Local 9, fragment 7, broken 0 |
| Heading/fence | `PASS` | Target H1/H2/H3/H4 `1/17/66/4`, report H1/H2 `1/8`; jump 0, fence marker target/report `100/6`, 모두 closed |
| Whitespace/encoding/EOF | `PASS` | 두 owned file trailing whitespace/tab/CRLF/NUL 0, UTF-8 read 성공, EOF LF |
| Scoped tracked diff check | `PASS` | `git diff --check -- <두 owned file>` diagnostic 0; 둘 다 untracked라 no-index 검사로 보완 |
| Untracked-aware target diff | `PASS` | Frozen target blob 대비 one-file `+111/-27`; content diff는 intended correction |
| Untracked-aware report diff | `PASS` | `/dev/null` 대비 one-file `+223`; no-index `--check` diagnostic 0, raw exit 1은 새 content |
| Scoped/untracked write set | `PASS` | Scoped status에서 두 owned file은 `??`; frozen target 대비 intended diff와 새 report만 write, 기존 untracked review/correction01/02 hash 불변 |
| Implementation/Maven/AWS | `NOT_RUN_BY_DESIGN` | Missing accepted Phase 11 implementation/provider environment; 문서 gate를 evidence로 승격하지 않음 |
| Stage/commit/push/worktree | `PASS` | 수행하지 않음 |

## 8. Residual과 판정

`HG11-R006`의 남은 sensitivity negative-disposition root cause를 Target의 case
contract, fresh audit, false-green gate, evidence schema와 exit에 연결했다.
Correction 03은 document contract만 교정했으며 실제 provider fault injection,
implementation, test/AWS evidence 또는 independent recheck를 합성하지 않는다.

남은 residual:

1. 실제 fault activator와 independent oracle가 각 29 case를 red로 만드는지는
   accepted implementation과 isolated actual-AWS run에서 증명해야 한다.
2. Phase 00/06~10 accepted implementation/handoff가 없고 Phase 11 entry는 닫혀
   있다.
3. Exact provider/production choice와 수치는 named owner ADR 전 open이다.
4. 별도 independent reviewer가 이 target/report hash와 `HG11-R006` closure를
   재검증해야 한다.

CORRECTION_ROUND: 03
ADDRESSED_FINDINGS: HG11-R006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9
TARGET_HASH_AFTER: 802463c118c4cb3bb22dbc1d3232cc2a1e101caa70f8cb85a3940d31c53b09d4
