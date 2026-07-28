# Phase 09 사람용 구현 가이드 — DB 없는 object storage

```yaml
guide_status: CORRECTED_ROUND_01_AWAITING_INDEPENDENT_RECHECK
guide_scope: Phase 09 only
correction_round: "01"
correction_status: ALL_HUMAN_REVIEW_FINDINGS_ADDRESSED_IMPLEMENTATION_STILL_BLOCKED
human_review_document: docs/implementation/human-guides/reviews/phase-09-review.md
human_review_verdict: CHANGES_REQUIRED
human_review_sha256_at_correction_input: a4c9f42905884f76a7729ff024c262f693697c05d3bd1af6eee4f9d0ca356d4d
target_sha256_before_correction: 67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54
canonical_phase_count: 15
phase: "09"
phase_name: object-storage-no-database
canonical_phase_document: docs/implementation/phases/phase-09-object-storage-no-database.md
canonical_phase_review: docs/implementation/reviews/phase-09-review.md
canonical_phase_document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PHASE_08_AND_CROSS_PHASE_CONTRACTS
handoff_status_observed: NOT_READY
inventory_observed_at_head: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_observed_date: 2026-07-29
inventory_timezone: Asia/Seoul
correction_inventory_observed_at: 2026-07-29T02:24:56+09:00
correction_validation_inventory_observed_at: 2026-07-29T02:32:21+09:00
head_baseline_shape: SINGLE_GCP_PLACEHOLDER_PROJECT
live_worktree_shape: CONCURRENT_UNACCEPTED_PHASE00_REACTOR_REMEDIATION
live_worktree_rule: OBSERVE_ONLY_DO_NOT_CLAIM_OR_MODIFY
authoring_snapshot_status: AUTHORING_SNAPSHOT_SUPERSEDED_FOR_CURRENT_INVENTORY
correction_snapshot_status: TIMESTAMPED_OBSERVATION_NOT_ACCEPTANCE_AUTHORITY
accepted_inventory_evidence_status: NONE
source_fingerprint_scheme: git_blob_at_inventory_observed_head
prerequisite_phases:
  - "00"
  - "07"
  - "08"
direct_consumers:
  - "10"
  - "11"
later_consumers:
  - "12"
  - "13"
  - "14"
source_sections_and_head_blobs:
  docs/master-design.md: "§1~4.6, §10.3, §13~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/2026-07-26-domain-design.md: "§1~3, §7~8, §15~18 | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "§1~3.6, §5~6.5 | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/architecture-domain-implementation-design.md: "§2~3, §12~14, §19~26 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~5 and exact Q-BENCH-02/Q-INFRA-01/Q-VAR-01 rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~6, Phase 08~11, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "HEAD §2, §5~10 | 250aa90ae568a6b32ec905fa5ee456d430ff72cf"
  docs/implementation/phases/phase-08-application-ports-local-runtime.md: "§6.3~7.6, §8.3, §9.1~9.3, §16.2 | 2aff093a6f2728470a7ccbb22b7e1a1a71f5b963"
  docs/implementation/phases/phase-09-object-storage-no-database.md: "§1~16 | 99a5b0df5531a65964272f423cc0ccca4d4f1430"
  docs/implementation/phases/phase-10-provider-neutral-coordinator.md: "§6.1~7.6, §8, §14.1 | 2b909924008df6c6f34d7d4d4399c8fdf6b6830a"
  docs/implementation/reviews/phase-09-review.md: "§1~8 | 786a7bd3a396c7010204e3940c5d2f031c07af48"
live_drift_receipt:
  status: AUTHORING_SNAPSHOT_SUPERSEDED_FOR_CURRENT_INVENTORY
  observed_at: "exact authoring time was not recorded; date-only 2026-07-29"
  docs/implementation/execution-progress-and-results.md: "historical live blob 3e9dcd16621e459e4572a094cb71ab62a3978360"
  pom.xml: "HEAD blob f8a411eadd4a5c01d8dd09fdea462738ca63d65f; historical live sequence aea23a42057d42a22d10dec9d532f2f4b85913ec -> a7cdfb8baf5b8dfc77fdda8ac8fa56920f41c3a4 -> 8aeca338c772527c28a38c89270177cd5182caf1"
  rpdptw/pom.xml: "historical untracked live blob abd372e7cf00f50435605a933056bb0c169b7b05"
  build/pom.xml: "historical untracked live blob a3afa3f4aa04d43c6654153f6e14a88276882871"
correction_live_inventory_receipt:
  observed_at: 2026-07-29T02:24:56+09:00
  branch: codex-implementation
  head: 7cc890ee1d0805df5ae14b633127fade4f978639
  pom.xml: "live Git object 1dc675ba17b7f2202f34a22131f152cc2868b075; HEAD blob f8a411eadd4a5c01d8dd09fdea462738ca63d65f"
  rpdptw/pom.xml: "untracked live Git object d02a560a0c661123d66046209c28eacb4ae5335c"
  rpdptw/application/pom.xml: "untracked live Git object a580f6ca04ae661131bc820aeb4348b27aa5aadc"
  build/pom.xml: "untracked live Git object a3afa3f4aa04d43c6654153f6e14a88276882871"
  docs/implementation/execution-progress-and-results.md: "live Git object f875edbdeb79fb18710421b56e126462a4d6d38c; Phase 00 FIX_01_IMPLEMENTED_REVIEW_02_PENDING / NOT_ACCEPTED"
  docs/implementation/human-guides/execution-progress-and-results.md: "live Git object 0e1a12536f2f25182fd55cbdb3da96ad5b1e2850; Phase 09 correction 01 IN_PROGRESS"
  reactor: "11 POM files; root modules rpdptw/build/legacy; Phase 09 object modules absent"
  java_inventory: "rpdptw main Java 23, all package-info.java; rpdptw test Java 0; build test Java 11"
  phase09_inventory: "named production types 0; object-common/memory/filesystem/s3 and port-contract-tests modules absent; E-P09-* absent"
correction_validation_inventory_receipt:
  observed_at: 2026-07-29T02:32:21+09:00
  branch: codex-implementation
  head: 7cc890ee1d0805df5ae14b633127fade4f978639
  drift_from_correction_snapshot: "workspace-relevant POM count 11 -> 13; adjacent human guides and human progress changed concurrently"
  workspace_relevant_poms: "13, excluding 2 dependency-owned node_modules examples"
  docs/implementation/execution-progress-and-results.md: "live Git object f875edbdeb79fb18710421b56e126462a4d6d38c; unchanged from correction snapshot"
  docs/implementation/human-guides/execution-progress-and-results.md: "live Git object 8ab846f043cb93a826ad62d2b611c6606d558e74"
  java_inventory: "rpdptw main Java 23, all package-info.java; rpdptw test Java 0; build test Java 11"
  phase09_inventory: "named production types 0; object-common/memory/filesystem/s3 and port-contract-tests modules absent; E-P09-* absent"
expected_reader:
  - Java record, interface, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW route, time window, capacity propagation을 구현해 보았다
  - RPDPTW pair identity와 object-storage CAS/publication authority는 처음 접한다
owner_roles:
  implementation: RPDPTW Object Storage owner
  upstream_application: Phase 08 Application/Local Runtime owner
  upstream_result: Phase 07 Verification/Result owner
  data_integrity: Architecture/Data Integrity owner
  security: Platform Security owner
  retention: Records/Operations owner
  concurrency_oracle: Phase 09 independent storage-contract test owner
  downstream: Phase 10 Coordinator and Phase 11 AWS owners
  review: independent Phase 09 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P09-STORAGE-CONTRACT
  - E-P09-CAS
  - E-P09-TENANT
```

> 이 문서는 사람이 Phase 09를 이해하고, entry gate가 열린 뒤 구현하고, 독립 evidence로 완료를 판정하기 위한 교육형 작업 지시서다. 현재 shared checkout에는 다른 작업자가 만드는 미승인 Phase 00 reactor/POM/package-info scaffold가 보이지만 Phase 09 storage port, adapter, contract test 또는 evidence는 없다. 아래 Java 이름과 signature는 별도 표시가 없는 한 **제안 후보(proposed)**이며, 존재하는 API나 승인된 public/wire contract를 뜻하지 않는다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 09는 database 없이도 큰 immutable artifact를 exact key로 안전하게 저장하고 검증하며, 작은 state/result pointer 하나만 stale-detecting CAS로 바꾸어 **“object가 존재한다”와 “업무상 authoritative하다”를 분리하는 단계**다.

완료의 핵심은 S3나 filesystem에 파일을 올리는 것이 아니다. 같은 logical write의 중복이 같은 bytes일 때만 수렴하고, 다른 bytes·stale writer·부분 upload·목록 누락·tamper·tenant crossing이 authority를 만들지 못한다는 것을 reusable contract suite로 입증해야 한다.

## 2. 큰 그림과 필요한 이유

### 2.1 왜 database 없이도 state를 다룰 수 있는가

Database transaction 대신 다음 두 종류를 분리한다.

```text
큰 자료:
  input / problem / travel / manifest / assignment / worker outcome
  verifier report / final result / profile
  → immutable, create once, content digest로 검증

작은 권위:
  current solve state / worker committed pointer / published result pointer
  → 한 key의 opaque-version CAS
```

업무 transition은 여러 object가 동시에 써졌다는 사실로 확정하지 않는다.

```text
immutable payload 저장
→ exact read로 digest·length·schema 검증
→ immutable manifest/reference 저장
→ exact closure 검증
→ authoritative pointer 하나 CAS
```

CAS가 성공하기 전의 object는 staged 또는 orphan일 수 있다. 이 설계 덕분에 crash가 어느 지점에서 나도 이전 authoritative pointer는 보존된다.

### 2.2 object existence가 result authority가 아닌 이유

다음 관찰은 서로 다르다.

| 관찰 | 뜻 | 정상 result인가 |
|---|---|---:|
| Final-result bytes가 존재 | Bytes가 저장되었을 수 있음 | 아니오 |
| Candidate verifier report가 존재 | Report artifact가 저장되었을 수 있음 | 아니오 |
| Result verifier report가 존재 | Report artifact가 저장되었을 수 있음 | 아니오 |
| `PublishableResultRef` manifest가 존재 | Both-gate closure를 주장하는 immutable 자료가 있음 | 아직 아님 |
| Published pointer가 exact closure를 가리키도록 CAS됨 | 해당 solve의 현재 publication authority | 예 |
| Prefix list에서 result처럼 보이는 key 발견 | Discovery/maintenance hint | 아니오 |

정상 조회 경로는 반드시 다음과 같다.

```text
published pointer
→ PublishableResultRef
→ candidate PASS report
→ final result payload
→ result-integrity PASS report
→ same tenant / solve / execution authority 확인
```

Pointer가 없으면 `NOT_PUBLISHED`다. Pointer는 있지만 closure가 깨졌다면 `RUNNING`이나 빈 결과가 아니라 `CORRUPT_PUBLICATION` 계열의 typed failure다.

### 2.3 prefix listing을 authority로 쓰면 안 되는 이유

Object-storage listing은 지연되거나 누락·중복·재정렬될 수 있다. Event도 중복·유실·out-of-order가 가능하다. 따라서 다음 흐름은 금지한다.

```text
list workers/ prefix
→ 지금 보이는 worker만 모음
→ 그중 objective 최소를 champion으로 선택
```

권위 흐름은 manifest가 선언한 집합을 exact key로 읽는다.

```text
ExecutionManifest declares [W0, W1, W2]
→ W0 committed pointer exact read
→ W1 committed pointer exact read
→ W2 committed pointer exact read
→ 각 outcome exact verified read
→ Phase 10이 all-declared completeness 판단
```

Phase 09는 exact read와 pointer authority만 제공한다. “모든 worker가 모였는가”, “누가 champion인가”, “다음 state가 합법인가”는 Phase 10 책임이다.

### 2.4 Canonical 15 Phase 안의 위치

이 저장소의 canonical Phase는 **00~14, 총 15개**다. Phase 09만 떼어 구현하더라도 앞뒤 계약을 알아야 storage가 domain이나 coordinator 역할을 훔치지 않는다.

| Phase | 주제 | 주요 producer → consumer 계약 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | Reactor, stable module, test fixture와 dependency guard를 모든 Phase에 제공 |
| 01 | Canonical input와 정규화 | Versioned canonical facts와 typed rejection을 Phase 02에 제공 |
| 02 | Prepared travel과 immutable problem | Complete directed travel, dense identity와 immutable problem을 Phase 03 이후에 제공 |
| 03 | Propagation/evaluation kernel | Cache-free physical fact와 evaluation/comparator contract를 Phase 04~07에 제공 |
| 04 | Capability/profile | Exact profile/version/preset closure와 `BoundProfile`을 Phase 05 이후에 제공 |
| 05 | Pair insertion/portfolio | Stable route-bank partition, exact insertion과 최대 8개 후보를 Phase 06에 제공 |
| 06 | COW ALNS/reproducibility | `CommittedCandidate`, replay와 termination lineage를 Phase 07에 제공 |
| 07 | Independent verification/result | Both-gate `PublishableResult` 또는 typed non-success를 Phase 08에 제공 |
| 08 | Application ports/local runtime | `ArtifactStore`, state/publication port 의미와 local seam을 **Phase 09**에 제공 |
| **09** | **No-DB object storage** | **Immutable exact-key artifact, verified read, CAS pointer, tenant/fault contract를 Phase 10/11에 제공** |
| 10 | Provider-neutral coordinator | Manifest-declared completeness, state transition, retry/cancel action을 Phase 11에 제공 |
| 11 | AWS reference distribution | 승인된 Phase 09/11 경계에 따라 S3 + Step Functions + Lambda parity를 Phase 14B에 제공 |
| 12 | Provider substitution | 승인된 storage/workflow/compute 축만 동일 contract 아래 교체 |
| 13 | Optional hybrid | **Phase 14A receipt와 C-17 승인 뒤에만** route-pool/CP-SAT branch를 조건부 제공 |
| 14 | 14A benchmark / 14B official cutover | 14A는 ALNS benchmark acceptance, 14B는 official/production authority를 소유 |

ALNS-first 경로에서 Phase 09는 local solver 결과를 보여 주기 위한 Phase 14A의 선행조건은 아니다. 그러나 AWS target과 durable multi-round 경로에서는 Phase 09 storage contract가 Phase 10/11의 선행조건이다.

```text
local ALNS qualification:
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A

distributed AWS branch:
08 → [09] → 10 → 11 → 14B

optional hybrid:
14A acceptance receipt → 13 → official hybrid가 승인된 경우에만 14B
```

Phase 13을 Phase 09 편의상 미리 열지 않는다. Phase 14A correctness/quality/performance receipt는 Phase 13 entry의 일부일 뿐 Phase 14B production authority가 아니다.

### 2.5 Phase 09 producer와 consumer

```text
Phase 07: PublishableResult + both verifier reports
Phase 08: typed keys/refs, application ports, local state/publication meaning
      \                         /
       \ exact accepted contract
        ↓
Phase 09 object-common semantics
  ├─ immutable put-if-absent + verified read
  ├─ state/worker/publication pointer CAS
  ├─ exact profile resolution
  ├─ tenant/security/failure mapping
  └─ local/memory/provider backend conformance
        ↓
Phase 10: exact declared artifact read + state CAS
Phase 11: approved S3 boundary + same-suite parity
```

| 경계 | Producer가 보장할 것 | Phase 09 또는 consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 07 → 09 | Both-gate report/result identity와 immutable publishable closure | Storage가 PASS를 합성하거나 verifier를 재실행 |
| Phase 08 → 09 | Provider-neutral key/ref/port, local scope, submission/state/publication semantics | Bucket/path/URI나 ambient tenant context를 port authority로 사용 |
| Phase 09 → 10 | Exact verified artifact, opaque version, one-pointer CAS, typed failure | Storage가 legal transition/completeness/champion을 계산 |
| Phase 09 → 11 | Approved backend contract와 semantic suite | S3 SDK success를 application success로 승격 |
| Phase 09 → 12 | Locator-free semantic identity와 same contract suite | Provider별 약한 의미를 common contract로 내림 |
| Phase 09 → 13 | Generic immutable artifact/ref만 제공 | Route pool/MIP activation을 storage kind 존재로 추론 |
| Phase 09 → 14 | Published pointer와 verified lineage | Test-only 값, orphan object, list 결과를 official/cutover authority로 사용 |

## 3. Source authority, fingerprint와 읽기 순서

### 3.1 충돌 해소 순서

구현 판단에는 다음 순서를 적용한다.

```text
사용자 고정 지시
→ canonical docs/master-design.md
→ question register의 exact Q-* 상태
→ Final Domain의 domain 의미
→ Final Architecture의 module/package/port 배치
→ Integrated design의 15 Phase와 no-DB 구조
→ implementation map/plan
→ canonical Phase 08/09/10와 Phase 09 review
→ historical cross-check
```

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md), deprecated 문서와 과거 GCP 자료는 누락·퇴행을 확인하는 역사 자료일 뿐 현재 authority가 아니다. Final Domain/Architecture 일부의 오래된 `Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 [Canonical Master](../../../master-design.md)와 [질문 등록부](../../../master-design-open-questions.md)의 최신 `Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다.

AWS S3 + Step Functions + Lambda target 선택은 확정됐지만 실제 S3 adapter를 Phase 09와 Phase 11 중 어디서 구현·증명할지는 canonical realization 문서 사이에 충돌이 남아 있다. 이 경계는 owner 승인 전 임의로 닫지 않는다.

시점이 다른 문서를 다음처럼 구분한다.

- 사용자 고정 canonical 5문서는 `docs/master-design.md`,
  `docs/2026-07-26-domain-design.md`,
  `docs/2026-07-26-architecture-design.md`,
  `docs/architecture-domain-implementation-design.md`,
  `docs/master-design-open-questions.md`다.
- Current [Domain map](../../../domain-design.md)과
  [Architecture map](../../../architecture-design.md)은 최신 mapping/cross-check로
  읽되 사용자 고정 5문서의 의미나 exact 질문 상태를 조용히 대체하지 않는다.
- Dated Domain/Architecture의 오래된 `Q-INFRA-01 DEFERRED`와 현재 질문 등록부의
  exact `Q-INFRA-01 RESOLVED`가 충돌하면 등록부와 Canonical Master의 승인
  record를 적용한다. 이것은 S3 implementation/production authority를 부여하지 않는다.
- Progress/guide/review와 live POM은 관찰 시점이 다른 mutable inventory다. Source
  authority, implementation acceptance와 동일한 hash namespace로 합치지 않는다.
- Named source 의미가 서로 충돌하고 위 exact rule로 해소되지 않으면
  `OPEN/GATED/BLOCKED_PENDING_CONTRACT`로 남긴다. New default나 production
  authority를 만들지 않는다.

### 3.2 검증 가능한 source fingerprint

아래 Git blob은 metadata의 `inventory_observed_at_head`에서 `git rev-parse HEAD:<path>`로 얻은 값이다. Source가 바뀌면 hash만 바꾸지 말고 해당 heading의 의미, 이 가이드의 requirement·WP·test·evidence를 함께 review한다.

| Source | 직접 읽을 heading/section | HEAD Git blob |
|---|---|---|
| [Canonical Master](../../../master-design.md) | §1~4.6, [§10.3 Result provenance](../../../master-design.md#103-result-provenance), §13, [§14.1 Publication gate](../../../master-design.md#141-publication-gate), §15.10, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Final Domain](../../../2026-07-26-domain-design.md) | §1, [§2 primer](../../../2026-07-26-domain-design.md#2-cvrptw-개발자를-위한-rpdptw-입문), §3, §7~8, [§15 result](../../../2026-07-26-domain-design.md#15-verification-finalization과-result), §16~18 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Final Architecture](../../../2026-07-26-architecture-design.md) | §1~3.6, [§5 운영/검증/publication](../../../2026-07-26-architecture-design.md#5-운영-검증과-publication), §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | [§2 Phase overview](../../../architecture-domain-implementation-design.md#2-전체-구현-순서), §3, §12, [§13 Phase 9](../../../architecture-domain-implementation-design.md#13-phase-9--database-없는-object-storage-architecture), §14, §19~26 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~5, exact `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | §0~7, authority, canonical index와 ALNS-first DAG | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §2~6, [Phase 09](../../master-realization-plan.md#phase-09--db-없는-object-storage), §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | HEAD §2, §5~10; live Phase 00 remediation drift는 별도 receipt | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Actual Phase 08](../../phases/phase-08-application-ports-local-runtime.md) | §6.3~7.6, §8.3, §9.1~9.3, [§16.2 handoff](../../phases/phase-08-application-ports-local-runtime.md#162-next--actual-but-unaccepted-phase-09-storage-contract) | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| [Canonical Phase 09](../../phases/phase-09-object-storage-no-database.md) | 전체, 특히 §3~4, §7~14, §15~16 | `99a5b0df5531a65964272f423cc0ccca4d4f1430` |
| [Actual Phase 10](../../phases/phase-10-provider-neutral-coordinator.md) | §6.1~7.6, §8, [§14.1 handoff](../../phases/phase-10-provider-neutral-coordinator.md#141-previous--actual-but-unaccepted-phase-0609) | `2b909924008df6c6f34d7d4d4399c8fdf6b6830a` |
| [Phase 09 review](../../reviews/phase-09-review.md) | §1, [§4 findings](../../reviews/phase-09-review.md#4-findings), §5~8 | `786a7bd3a396c7010204e3940c5d2f031c07af48` |

Correction input의 current working-byte SHA-256은 아래처럼 별도 고정했다. Git HEAD
blob 표는 commit provenance이고 이 표는 `2026-07-29T02:24:56+09:00` correction
source snapshot이다. 둘 중 어느 것도 implementation acceptance receipt가 아니다.

| Source | Working-byte SHA-256 | Lines |
|---|---|---:|
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 1648 |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 1886 |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 1019 |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 3822 |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 87 |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 1607 |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 1469 |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 240 |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 943 |
| Canonical Phase 09 | `ac08d10a63f7d0bd86a74fa61c1d220b0619a9c16c7d50fe0017cfe9afc8bb3b` | 1967 |
| Canonical Phase 09 review | `e49a441d0ccedb9e13b5b5c0f2614bef710dc68a1d587a3c65a5e430bf7a0b5d` | 423 |
| Phase 08 human guide | `2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5` | 2599 |
| Phase 10 human guide | `c1649837bbb057ba3272a1c0964782477d32b764e2354ed2c06a94cf14839290` | 2074 |
| Phase 09 human review | `a4c9f42905884f76a7729ff024c262f693697c05d3bd1af6eee4f9d0ca356d4d` | 257 |
| Target before correction | `67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54` | 2079 |

재검증 예:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/master-design.md
git rev-parse HEAD:docs/implementation/phases/phase-09-object-storage-no-database.md
git rev-parse HEAD:docs/implementation/reviews/phase-09-review.md
git hash-object docs/implementation/execution-progress-and-results.md
```

마지막 명령은 명령을 실행한 그 시점의 live bytes만 확인한다. 최초 authoring 중
기록된 progress blob `3e9dcd16621e459e4572a094cb71ab62a3978360`은 superseded다.
Correction snapshot `2026-07-29T02:24:56+09:00`과 validation snapshot
`2026-07-29T02:32:21+09:00`의 implementation progress object는
`f875edbdeb79fb18710421b56e126462a4d6d38c`였다. 이 drift는 concurrent Phase 00
remediation 관찰이지 Phase 09 entry 승인이나 evidence가 아니다.

인접 Phase whole-file digest를 acceptance 조건으로 상호 저장하지 않는다. Entry 시 stable named section의 의미와 accepted artifact/evidence identity를 다시 확인한다.

### 3.3 구현 전 정확한 읽기 순서

| 순서 | 읽을 곳 | 답해야 할 질문 | 확인할 module/file/evidence |
|---:|---|---|---|
| 1 | Implementation README §0~§7 | Authority, 15 Phase, ALNS-first와 상태 변경 권한은 무엇인가? | Phase 09 canonical filename/review, `0/15 ACCEPTED` |
| 2 | Master §4.1~4.6, §10.3, §14.1 | Artifact lineage와 two-gate publication authority는 무엇인가? | Immutable provenance와 publication AND gate |
| 3 | Final Domain §2, §7~8, §15~17 | Request/pair identity, stable state, result authority와 error는 무엇인가? | `ProblemInstance`, `VerifiedSolution`, `PublishableResult` 의미 |
| 4 | Final Architecture §2, §3.5~3.6, §5 | Port owner, identity/idempotency, CAS/security/test 경계는 어디인가? | `rpdptw-application` outbound port ownership |
| 5 | Integrated design §2~3, §12~14, §19~26 | 15 Phase, target tree, no-DB exact key/CAS와 Phase 10 consumer는 무엇인가? | `adapters/object-common`, backend와 contract suite |
| 6 | Question register exact rows | 무엇이 resolved/open/gated/deferred인가? | `Q-INFRA-01`, `Q-BENCH-02`, `Q-VAR-01`, C-17 |
| 7 | Master Realization Plan Phase 08~11, §8~15 | Entry/exit/evidence/rollback과 S3 same-suite 요구는 무엇인가? | `E-P09-STORAGE-CONTRACT/CAS/TENANT` |
| 8 | Actual Phase 08 §6.3~7.6/§16.2 | 어떤 proposed port와 unresolved signature를 받는가? | Access binding, failure carrier, publication fence |
| 9 | Canonical Phase 09 전체 | Exact invariant, fixture, WP, command, blocker는 무엇인가? | §4 entry, §8 contract, §10 tests, §11 WP |
| 10 | Phase 09 review 전체 | 어떤 안전 수정과 5개 residual blocker가 남았는가? | F-P09-001~008 |
| 11 | Actual Phase 10 §6~8/§14.1 | 어떤 exact read/state/worker/publication operation을 소비하는가? | Worker commit과 distinct precondition |
| 12 | Execution Progress §5~10 | 누가 status를 바꾸며 current acceptance/evidence는 무엇인가? | Scheduler receipt; P09는 여전히 not implemented |
| 13 | 실제 POM/package inventory | Target module이 존재하는가, placeholder뿐인가? | Root/rpdptw/build/legacy POM, Phase 09 path 부재 |

문서가 detailed/reviewed 상태라는 사실, package-info가 있다는 사실, root build가 통과한다는 사실은 Phase 09 implementation/evidence가 있다는 뜻이 아니다.

## 4. RPDPTW primer, 용어집과 불변조건

### 4.1 CVRPTW의 customer와 RPDPTW의 request

CVRPTW에서는 customer visit 하나를 삽입·제거 단위로 보는 경우가 많다. RPDPTW에서는 `Request`가 pickup과 delivery의 pair ownership을 가진다.

| 용어 | 의미 | storage에서 보존할 것 |
|---|---|---|
| `Order` | 외부 business input | Canonicalization 전후 provenance |
| `Request` | 원자 운송 업무 | Stable request identity와 exactly-once outcome |
| `Node` | Pickup/delivery/terminal service 정의 | Immutable problem 안의 node identity |
| `PhysicalLocation` | Directed travel endpoint | Node와 분리된 travel identity |
| `Visit` | Route 안의 node occurrence | Ordered route artifact |
| `Route` | Concrete vehicle + terminal policy + ordered visits | Candidate/final artifact bytes와 digest |
| `SearchRequestBank` | Search 중 route 밖인 request ID 집합 | Candidate artifact 일부이지 final status가 아님 |
| `FinalOutcome` | Verifier/finalization 뒤 `ASSIGNED`/`UNASSIGNED` | Both-gate result closure 안의 authority |

Real pickup-delivery는 pickup에서 load가 늘고 delivery에서 줄어든다. Delivery-only는 route 출발 전 initial load에 참여하며 가짜 pickup visit/travel을 만들지 않는다. Storage는 이 domain 의미를 계산하지 않지만, artifact kind/schema/authority identity가 섞이지 않도록 보존해야 한다.

### 4.2 storage 용어집

| 용어 | 사람 말 | 절대 같은 것으로 보면 안 되는 것 |
|---|---|---|
| `ArtifactKey` | Application이 이해하는 typed logical address | S3 key, bucket, filesystem `Path` |
| `ArtifactRef` | Kind/schema/digest/length와 opaque locator가 있는 참조 | Bytes가 정상이라는 보증 |
| `OpaqueLocator` | Adapter만 해석하는 실제 위치 token | Semantic identity, tenant proof |
| `ContentDigest` | Algorithm/version이 명시된 content identity | Signature, encryption, authorization |
| `ArtifactEnvelope` | Protected metadata와 payload 검증 설명 | Arbitrary Java object serialization |
| `putIfAbsent` | Key가 비었을 때만 create-once | `exists` 확인 후 unconditional put |
| `StateVersion` | CAS equality에만 쓰는 opaque token | 증가하는 숫자, timestamp, business ordinal |
| CAS | Expected version일 때 pointer 하나 변경 | Multi-object transaction, distributed lock |
| Declared completeness | Manifest가 선언한 exact set을 모두 확인 | Prefix list에 현재 보이는 집합 |
| Orphan | 저장됐지만 authoritative pointer가 참조하지 않는 artifact | 정상 result나 자동 삭제 대상 |
| Quarantine | Corruption/tamper 조사 격리 | 정상 bytes로 overwrite하는 수리 |
| Publication pointer | Both-gate result closure를 권위화하는 한 key | Final-result bytes 자체 |
| Retention root | Current pointer/manifest/evidence가 도달 가능한 graph | Object age 또는 list 결과 |

### 4.3 identity의 층

Phase 09에서 가장 자주 생기는 오류는 서로 다른 identity를 한 문자열로 합치는 것이다.

```text
업무 identity:
  TenantId / SubmissionId / SolveId / RoundOrdinal
  WorkerOrdinal / WorkerRunId / AttemptId / ArtifactKind

content identity:
  schema version / algorithm id+version / checksum / length

storage identity:
  typed logical key / opaque provider locator / opaque object version

authority identity:
  execution manifest / run-state pointer / worker committed pointer
  publication pointer / verifier reports
```

고정 규칙:

1. 모든 logical key/access decision은 `TenantId`를 포함한다.
2. `OpaqueLocator`는 application이 parse·compare·fingerprint하지 않는다.
3. 같은 semantic content를 다른 physical locator로 복사해도 semantic identity는 같아야 한다.
4. `StateVersion`은 equality precondition 외에 정렬·증가·시간 추론에 쓰지 않는다.
5. Run-state token, worker pointer token과 publication pointer token은 서로 다른 authority다.
6. `AttemptId`가 바뀌어도 같은 `WorkerRunId`의 seed/warm-start/requested work는 바뀌지 않는다.

### 4.4 artifact lifecycle

```text
ABSENT
→ STAGING
→ IMMUTABLE_VERIFIED
→ DECLARED_REFERENCEABLE
→ COMMITTED_BY_POINTER
→ SUPERSEDED_BUT_RETAINED
→ PURGE_ELIGIBLE
→ PURGED

side branches:
STAGING → PARTIAL_ABORTED
verified/read step → QUARANTINED
CAS attempt → STALE_WITHOUT_AUTHORITY_CHANGE
```

| 상태 | authoritative한가 | 허용 행동 |
|---|---:|---|
| `STAGING` | 아니오 | Abort/cleanup candidate |
| `IMMUTABLE_VERIFIED` | 아직 아님 | Exact ref로 manifest가 참조 가능 |
| `DECLARED_REFERENCEABLE` | Artifact 자체만 verified | Phase 10의 worker/round success가 아님 |
| `COMMITTED_BY_POINTER` | 해당 pointer 의미에서만 예 | 다른 business state까지 자동 성공 아님 |
| `SUPERSEDED_BUT_RETAINED` | Current는 아니지만 replay/rollback root일 수 있음 | Policy와 hold 아래 보존 |
| `QUARANTINED` | 정상 사용 불가 | Incident evidence 보존 |
| `PURGE_ELIGIBLE` | Root/hold/grace 재검증 뒤에만 | Exact version conditional delete |

### 4.5 state와 result lifecycle

Storage는 Phase 08/10 lifecycle을 계산하지 않지만 다음 ordering을 보존한다.

```text
ExecutionManifest put + verify
→ assignment/outcome payload put + verify
→ worker committed reference put + verify
→ worker committed pointer CAS
→ Phase 10 declared completeness/champion 판단
→ solve/round state body put + verify
→ current state pointer CAS
→ final result + two verifier reports put + verify
→ PublishableResult manifest put + verify
→ published pointer CAS
```

각 화살표 사이에 crash가 발생할 수 있다. Recovery는 exact read와 same desired identity로 수렴해야 하며, 앞의 incomplete object를 정상 state로 해석하면 안 된다.

### 4.6 반드시 지킬 불변조건

1. **Tenant-first:** Key/access/closure가 같은 tenant인지 backend lookup 전에 검증한다.
2. **Create once:** Immutable final key의 첫 protected content를 덮어쓰지 않는다.
3. **Same/same only:** Same identity/same bytes·metadata만 duplicate success로 수렴한다.
4. **Different means conflict:** Same identity/different protected content는 integrity conflict다.
5. **Verify before decode:** Envelope/schema/length/digest/authenticator policy 전에는 deserialize하지 않는다.
6. **Payload before reference:** Referenced payload를 먼저 verified-read한다.
7. **One authority commit:** 한 business transition은 pointer 하나의 CAS로만 권위화한다.
8. **No multi-object transaction:** 여러 write 성공을 하나의 atomic commit으로 보지 않는다.
9. **No listing authority:** Manifest-declared exact ref만 normal completeness 입력이다.
10. **No implicit visibility:** Backend success만으로 committed-readable을 추정하지 않는다.
11. **CAS fail closed:** Stale/missing/indeterminate CAS를 정상 update로 바꾸지 않는다.
12. **No hidden index:** Idempotency/query/completeness를 DB-like table로 숨기지 않는다.
13. **Existence is not success:** Result/report bytes 존재는 publication이 아니다.
14. **Failure is typed:** Missing, denied, corrupt, stale, partial, unavailable, indeterminate를 합치지 않는다.
15. **Digest is not security:** Checksum은 authorization/encryption/signature를 대체하지 않는다.
16. **Retention preserves authority:** Active/held/quarantined graph를 listing/age로 지우지 않는다.
17. **Storage is not coordinator:** Legal transition, champion, comparator, cancel policy를 구현하지 않는다.
18. **Open stays open:** Encoding/checksum/retry/retention/provider ownership의 미승인 값을 default로 만들지 않는다.

## 5. 실제 repository inventory: authoring, HEAD, live와 accepted evidence 분리

### 5.1 재현 가능한 관찰 명령

```bash
date '+%Y-%m-%dT%H:%M:%S%z'
git rev-parse HEAD
git branch --show-current
git status --short
git ls-tree -r --name-only HEAD
rg --files --hidden --no-ignore rpdptw build legacy .mvn
rg --files rpdptw -g '*.java'
rg --files --hidden --no-ignore -g 'pom.xml'
git hash-object pom.xml rpdptw/pom.xml rpdptw/application/pom.xml build/pom.xml
git hash-object docs/implementation/execution-progress-and-results.md
git hash-object docs/implementation/human-guides/execution-progress-and-results.md
```

Inventory receipt는 다음 네 층을 절대로 합치지 않는다.

| 층 | 뜻 | Phase 09 entry/acceptance에 사용할 수 있는가 |
|---|---|---:|
| `AUTHORING_SNAPSHOT` | 최초 guide 작성 중 여러 시점에 관찰한 역사 값. Exact 시각이 없으므로 현재 inventory에는 superseded | 아니오 |
| `HEAD_BASELINE` | `7cc890e...` commit tree. Source provenance이며 현재 worktree나 accepted reactor가 아님 | 아니오 |
| `LIVE_OBSERVATION` | 한 timestamp에서 함께 읽은 working bytes/Git object/module/test/status | 아니오 |
| `ACCEPTED_EVIDENCE` | Scheduler가 승인한 predecessor receipt와 immutable implementation/test evidence | 예 |

Authoring snapshot의 date-only 값과 correction snapshot을 한 receipt처럼 이어 붙이지 않는다.
각 snapshot은 exact timestamp/timezone, branch, HEAD, live Git object set, progress registry
object와 module/test count를 함께 갖는다. 이후 하나라도 drift하면 이전 snapshot은
`AUTHORING_SNAPSHOT_SUPERSEDED_FOR_CURRENT_INVENTORY`이고 다시 exact-read한다.

Correction 관찰 기준:

- Branch: `codex-implementation`
- HEAD: `7cc890ee1d0805df5ae14b633127fade4f978639`
- HEAD는 root 단일 GCP placeholder project다.
- Correction live snapshot: `2026-07-29T02:24:56+09:00`.
- Live worktree에는 다른 Phase 00 작업의 root POM 수정과 `.mvn/`, `build/`, `legacy/`, `rpdptw/` untracked tree가 있다.
- Root POM은 11개 POM reactor를 선언하지만 Phase 09 `adapters/object-*`와 `build/port-contract-tests` module은 없다.
- `rpdptw` main Java 23개는 모두 `package-info.java`, RPDPTW test Java는 0, build test Java는 11개다.
- 작성과 correction 중 root/application/progress blob 및 test inventory가 바뀌었다. 따라서 아래 live 값은 acceptance baseline이 아니라 **한 시점의 관찰 영수증**이다.
- 이 가이드는 live drift를 읽기만 하며 acceptance나 stable baseline로 승격하지 않는다.

최종 document validation 중 `2026-07-29T02:32:21+09:00`에 다시 관찰하자
workspace-relevant POM은 13개로 늘었고 dependency-owned `node_modules` 예제 POM 2개가
별도로 보였다. Human-guide progress object도 `8ab846f...`로 바뀌었다. 반면 Phase 09
named production type/test/evidence는 계속 0이었다. 따라서 02:24 receipt도
`LIVE_OBSERVATION`으로 보존하되 “현재”라고 재사용하지 않으며, 02:32 receipt 역시
acceptance가 아니다.

### 5.2 Correction live receipt와 지속 가능한 판정

| 항목 | HEAD baseline | `2026-07-29T02:24:56+09:00` correction live | Phase 09 판정 |
|---|---|---|---|
| Root POM | 단일 `com.ronext:ro-next`, GCP/Jackson dependency 직접 보유; blob `f8a411e...` | `packaging=pom`, `rpdptw/build/legacy` aggregator, Git object `1dc675b...` | 외부 Phase 00 drift, unaccepted |
| Target namespace | 없음 | `com.ronext.rpdptw` package-info 23개 | 책임 설명만 있고 production type 없음 |
| Stable modules | 없음 | `core/solver/verification/application/capabilities/profile-catalog` POM | Phase 00 scaffold, Phase 09 code 아님 |
| Build support | 없음 | `build/test-fixtures`, `build/architecture-rules` test Java 11개 | 외부 Phase 00 architecture/fixture test이며 Phase 09 contract suite 아님 |
| Legacy | Root `src/**`의 GCP placeholder 6 main/1 test | `legacy/gcp-placeholder`로 이동 중 | Migration characterization, target 아님 |
| Phase 09 adapters | 없음 | `adapters/object-common`, `object-memory`, `object-filesystem`, `object-s3` 모두 없음 | 목표 대비 부재 |
| Phase 09 ports/types | 없음 | `ArtifactStore`, `ArtifactKey/Ref`, `RunStateRepository`, `ResultPublisher` Java type 0 | 목표 대비 부재 |
| Phase 09 tests | 없음 | RPDPTW test Java 0; build test Java 11개 중 Phase 09 storage contract 0 | Evidence 0 |
| Phase 09 evidence | 없음 | `E-P09-*` 없음 | `NOT_PRODUCED` |
| Implementation progress | P09 planned/not accepted | Git object `f875edb...`; Phase 00 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING / NOT_ACCEPTED` | P09 predecessor entry는 닫힘 |
| Human-guide progress | HEAD에 없음 | Git object `0e1a125...`; Phase 09 correction 01 `IN_PROGRESS` | 문서 workflow일 뿐 구현 evidence 아님 |

최초 authoring 값은 현재 inventory가 아니다. 아래처럼 명시적으로 superseded 상태로
보존하고 correction snapshot과 별도 표로 읽는다.

| Snapshot | Git object | 해석 |
|---|---|---|
| `AUTHORING_SNAPSHOT` root POM | `aea23a4... → a7cdfb8... → 8aeca33...` | `AUTHORING_SNAPSHOT_SUPERSEDED_FOR_CURRENT_INVENTORY`; exact authoring timestamp 미기록 |
| Correction [Root POM](../../../../pom.xml) | `1dc675ba17b7f2202f34a22131f152cc2868b075` | HEAD와 다른 live Phase 00 bytes |
| Correction [RPDPTW aggregator](../../../../rpdptw/pom.xml) | `d02a560a0c661123d66046209c28eacb4ae5335c` | Untracked Phase 00 scaffold |
| Correction [Application POM](../../../../rpdptw/application/pom.xml) | `a580f6ca04ae661131bc820aeb4348b27aa5aadc` | Phase 09 port type가 아니라 module dependency scaffold |
| Correction [Build aggregator](../../../../build/pom.xml) | `a3afa3f4aa04d43c6654153f6e14a88276882871` | `test-fixtures`/`architecture-rules`만 선언 |

여기서 지속 가능한 사실은 timestamp별 blob 값이 아니라 재현 명령과 **Phase 09 named
production type/test/evidence 0**이라는 판정이다. 구현 시작자는 implementation progress와
human-guide progress를 각각 exact-read하고, accepted predecessor receipt가 가리키는
source/build/test snapshot과 live bytes가 다르면 C0에서 멈춘다. Shared checkout의 live
drift를 자신의 오류로 보거나 acceptance로 승격하지 않는다.

### 5.3 현재 존재/placeholder/부재

| 분류 | 실제 항목 | 사람이 취할 태도 |
|---|---|---|
| 존재 | Authority 문서, Phase 08/09/10 상세와 review | 의미와 gate를 읽는다 |
| 존재하지만 unaccepted | Live Phase 00 reactor/POM/package-info | 변경하지 않고 drift로 기록한다 |
| Placeholder | `legacy/gcp-placeholder`의 direct GCS create/list/existence flow | Characterization만 하고 호환 요구로 보존하지 않는다 |
| 부재 | Phase 08 accepted ports/evidence | Phase 09 구현 시작 blocker |
| 부재 | `adapters/object-*`, `build/port-contract-tests` | Future target으로만 쓴다 |
| 부재 | Encoding/key/checksum/CAS/security/retention ADR | 사람 승인 전 production bytes/API를 고정하지 않는다 |
| 부재 | S3 ownership decision | Phase 09 exit를 열지 않는다 |
| 부재 | Phase 09 code/test/evidence/review acceptance receipt | 구현 완료를 주장하지 않는다 |

### 5.4 현재와 미래 Maven 명령 구분

현재 live reactor가 선택 가능하더라도 Phase 09 module/test가 없으므로 다음 명령은 Phase 09 evidence가 아니다.

```bash
# CURRENT-REACTOR-DIAGNOSTIC ONLY
./mvnw -pl rpdptw/application -am verify
./mvnw -pl build/architecture-rules -am test
./mvnw verify
```

Zero test, `failIfNoTests=false`, package-info compile 또는 legacy test 성공은 Phase 09 green이 아니다. 동시 Phase 00 작업이 안정되고 owner가 허용한 evidence session에서만 실행 결과를 기록한다.

아래는 module이 생긴 뒤의 **FUTURE-RED 후보**다.

```bash
./mvnw -pl adapters/object-common -am test
./mvnw -pl adapters/object-memory -am test
./mvnw -pl adapters/object-filesystem -am verify
./mvnw -pl build/port-contract-tests -am test
```

현재 이 path들은 존재하지 않는다. S3 command는 Phase 09/11 ownership 결정 전 `TBD_BY_APPROVED_PHASE_09_11_BOUNDARY`이며 명령을 발명하지 않는다.

## 6. Scope, non-scope와 결정 상태

### 6.1 Phase 09이 구현하는 것

- Tenant-scoped typed logical key와 alias/path traversal 방지
- Algorithm-tagged content digest, length, schema와 protected metadata 검증
- Immutable `putIfAbsent`와 exact-key verified read
- Opaque version을 사용하는 one-key CAS
- State, worker committed outcome와 publication authority 분리
- Payload → reference → pointer ordering과 response-loss recovery
- Manifest-declared exact reference read, listing 비권위
- Same/same idempotency와 same/different integrity conflict
- Partial write, visibility uncertainty, stale read와 duplicate writer의 typed 처리
- Corruption/tamper detection, quarantine와 no-overwrite recovery
- Tenant authorization, classification, redaction과 locator opacity
- Exact profile identity/version/preset resolution
- Retention root/hold/quarantine/grace와 conditional purge safety
- In-memory concurrent oracle와 local single-JVM reference adapter
- 모든 backend에 재사용할 storage contract suite
- Phase 10/11이 소비할 exact handoff와 rollback point

### 6.2 Phase 09이 구현하지 않는 것

- Phase 10 solve/round/worker legal transition, completeness, champion과 comparator
- Phase 07 verifier 계산이나 PASS report 합성
- Phase 08 public API/wire schema 재설계
- Database, hidden index, idempotency table, lock/lease service
- Recent solve/search/filter/pagination/aggregation API
- Prefix listing/event를 normal authority로 쓰는 API
- Multi-object transaction이나 filesystem rename/lock을 common guarantee로 승격
- AWS SDK, bucket/IAM/KMS/Object Lock/lifecycle/IaC를 owner 결정 없이 당김
- Step Functions/Lambda/ECS/GCP/Azure/Kubernetes runtime 구현
- Production retention day, retry count, timeout, object size와 concurrency 숫자 고정
- Phase 13 route pool/MIP 특화 최적화 또는 OR-Tools dependency
- Phase 14 official benchmark 수치, baseline, traffic/cutover authority
- 실제 production purge/delete 활성화

### 6.3 확정된 의미

| 항목 | 상태 | 구현자가 지킬 것 |
|---|---|---|
| Database 없음 | USER-CONSTRAINT / FIXED | Runtime authority를 DB/index/lock table로 보완하지 않음 |
| Immutable + one pointer CAS | FIXED | 여러 mutable object의 동시 commit을 가정하지 않음 |
| Exact key/reference | FIXED | Listing/event는 authority가 아님 |
| Result existence vs authority | FIXED | Published pointer 전에는 정상 result가 아님 |
| Same/same idempotency | FIXED | Different protected content를 overwrite하지 않음 |
| Verify before deserialize | FIXED | Corrupt/unknown schema를 decode하지 않음 |
| Tenant/provider separation | FIXED | Raw provider locator가 application identity가 아님 |
| AWS target | RESOLVED | S3 + Step Functions + Lambda 선택 자체만 확정 |

### 6.4 OPEN, GATED, deferred

| 항목 | 상태 | 이 Phase에서 할 수 있는 일 | 금지 |
|---|---|---|---|
| Key token/layout | OPEN/PROPOSED | Injective typed grammar와 test vector 설계 | Alphabet/escaping을 public compatibility로 확정 |
| Envelope encoding | OPEN/PROPOSED | Versioned deterministic projection 설계 | JSON/CBOR/protobuf를 hidden default로 선택 |
| Checksum algorithm/version | OPEN/PROPOSED | Algorithm-tagged abstraction과 TEST_ONLY vector | SHA-256 fixture를 production 승인으로 승격 |
| Authorization binding | CROSS-PHASE BLOCKER | Explicit non-ambient 후보 비교 | `ThreadLocal`/global/implicit request context |
| Failure carrier | CROSS-PHASE BLOCKER | Operation×failure matrix 설계 | Generic exception/not-found/conflict로 축소 |
| Worker commit operation | CROSS-PHASE BLOCKER | Create-once 또는 typed CAS 대안 검토 | Backend key/list/solve token으로 대체 |
| Publication precondition | CROSS-PHASE BLOCKER | Run-state fence와 pointer token 분리 | 한 `StateVersion`을 두 authority로 사용 |
| Phase 09/11 S3 ownership | BLOCKER | Owner/command/evidence decision 요청 | 어느 Phase든 임의 완료 claim |
| Local FS atomicity | ENVIRONMENT-GATED | Capability probe와 fail-closed test | Java API 존재만으로 모든 FS 지원 주장 |
| Retry/visibility budget | OPEN | Typed indeterminate와 explicit policy seam | Fixed hidden retry |
| Retention/grace/purge | OPEN | Preserve/dry-run/conditional safety | Production deletion 숫자/활성화 |
| `Q-BENCH-02` | OPEN — EXPERIMENT_REQUIRED | Storage TEST_ONLY fixture 사용 | Official worker/round/step/watchdog 수치 발명 |
| Phase 13/C-17 | GATED OPTIONAL | Generic artifact kind만 유지 | Route pool/CP-SAT activation |
| `Q-VAR-01` | DEFERRED | 현재 pair/terminal contract 보존 | 질문·구현·자동 재개 |
| Multi-trip/rotation | DEFERRED FEATURE | Opaque artifact로 보존 | Domain 의미 발명 |
| Phase 14 production | GATED / NOT GRANTED | Published lineage contract 제공 | Official/traffic/cutover authority 주장 |

### 6.5 독립 review가 남긴 5개 blocker

| Finding | 질문 | 코드 시작을 막는 범위 | 승인 전 last safe point |
|---|---|---|---|
| F-P09-001 | S3 adapter/same-suite를 Phase 09와 11 중 누가 소유하는가? | Provider module, command, Phase exit | Provider-neutral + memory/local plan |
| F-P09-002 | Tenant authorization을 outbound port에 어떻게 non-ambient 전달하는가? | 모든 storage operation | Backend lookup/call 0 |
| F-P09-003 | Internal failure를 caller에게 어떻게 lossless 전달하는가? | Port implementation/fault evidence | Failure 시 state/pointer 불변 |
| F-P09-004 | Worker committed outcome을 어떤 operation으로 권위화하는가? | Phase 10 exact fan-in | Verified orphan outcome, commit authority 없음 |
| F-P09-005 | Run-state fence와 publication pointer precondition을 어떻게 분리하는가? | Publication linearizability | Publishable closure, published pointer 없음 |

### 6.6 코드 변경 전 사람 승인 checkpoint

다음 질문에 문서화된 승인 답이 하나라도 없으면 **WP-09.1 production
code·POM·API·persisted byte/test-vector freeze를 시작하지 않는다.** “WP-09.1 이후
중단”은 WP-09.1까지 구현해도 된다는 뜻이 아니다.

1. Phase 08의 accepted `ArtifactStore`, `RunStateRepository`, `ResultPublisher` signature와 evidence ref는 무엇인가?
2. Tenant-scoped authority를 async/retry에도 유지하는 explicit session/facade 또는 동등한 binding은 무엇인가?
3. 각 operation의 missing/denied/corrupt/stale/partial/indeterminate/unsupported를 어떤 sealed/checked carrier로 손실 없이 반환하는가?
4. Worker committed outcome은 create-once인가, 별도 pointer CAS인가, 승인된 repository projection인가?
5. Run-state authorization fence와 publication pointer precondition의 distinct type/linearization은 무엇인가?
6. Canonical key/envelope/checksum version과 migration/rotation 정책은 무엇인가?
7. Phase 09/11 중 S3 module, isolated environment와 same-suite evidence owner는 누구인가?
8. Local target filesystem과 `LOCAL_SINGLE_JVM` capability evidence는 무엇인가?
9. Retention policy가 없을 때 production purge를 fail-closed로 막는가?
10. Implementer, contract-test owner, security reviewer와 independent reviewer가 분리됐는가?

승인 전 허용 범위는 다음과 같이 닫는다.

| 활동 | 승인 전 상태 | 허용/금지 |
|---|---|---|
| Authority/source 읽기, 손으로 상태 전이 계산, negative fixture 목록 작성 | `DESIGN_ONLY` | 허용. Working source/POM/API/wire bytes를 바꾸지 않음 |
| Review용 pseudo-type와 operation×failure matrix 비교 | `DESIGN_ONLY` | 허용. `PROPOSED/BLOCKED`를 유지하고 accepted contract/evidence로 인용하지 않음 |
| Java production/test source, POM/module, checked-in canonical byte/digest vector 생성 | `BLOCKED_PENDING_CONTRACT` | 금지 |
| Backend put/CAS/purge 또는 provider integration 실행 | `BLOCKED_PENDING_CONTRACT` | 금지 |
| `E-P09-*`, Phase handoff, `READY/IN_PROGRESS/ACCEPTED` 주장 | `BLOCKED_PENDING_ACCEPTED_ENTRY` | 금지 |

WP-09.0의 유일한 정상 handoff는 열 개 답, predecessor receipt, ADR와 owner가 모두
확인된 `APPROVED_ENTRY_RECEIPT`다. 하나라도 없으면 handoff는
`BLOCKED_ENTRY_RECEIPT`이고 last safe point는 이 문서/손 계산/미실행 fixture
proposal이다. 그 상태에서 WP-09.1을 호출하거나 production/test vector를 freeze하지
않는다.

## 7. 학습 경로

### 7.1 1단계 — 개념을 말로 설명하기

코드를 쓰기 전에 다음 질문에 답한다.

- 왜 immutable result bytes가 있어도 published result가 아닌가?
- `ArtifactKey`와 `OpaqueLocator`가 왜 다른가?
- `StateVersion`을 숫자로 증가시키면 왜 위험한가?
- Same key/same bytes와 same key/different bytes의 outcome은 왜 다른가?
- 왜 prefix listing은 worker completeness가 아닌가?
- 왜 checksum은 authorization이나 encryption이 아닌가?
- Crash가 payload 뒤/pointer 앞에서 나면 무엇이 authoritative한가?
- Storage와 Phase 10 coordinator의 책임 경계는 어디인가?

완료 신호: 동료에게 whiteboard로 `payload → verify → manifest → verify → pointer CAS`를 설명하고, 각 crash point의 last safe authority를 답할 수 있다.

### 7.2 2단계 — 작은 손 탐색

다음 최소 상태를 종이나 작은 test model로 계산한다.

```text
initial:
  current pointer = state-A @ version-v1

writer 1:
  expected v1, desired state-B

writer 2:
  expected v1, desired state-C
```

예상:

- Exactly one writer만 CAS success
- Loser는 `STALE_VERSION`
- State-B/C body는 둘 다 immutable orphan이 될 수 있음
- Current pointer가 가리키는 하나만 authoritative
- Loser가 current를 다시 읽지 않고 overwrite하면 결함

추가 손 탐색:

1. Same key/same bytes duplicate put
2. Same key/different protected metadata
3. Put commit 뒤 response loss
4. Result/report object는 있지만 published pointer 없음
5. Tenant A ref를 tenant B context로 읽기
6. Manifest는 W0/W1을 선언했는데 list는 W0/W9만 반환
7. Active pointer closure의 한 payload byte tamper

완료 신호: 각 case의 exact final pointer, returned disposition, unchanged state와 audit/quarantine 여부를 표로 쓸 수 있다.

### 7.3 3단계 — contract test부터 작은 vertical slice

추천 순서:

```text
typed key + canonical codec
→ in-memory putIfAbsent
→ verified read
→ opaque-version CAS
→ fault injection
→ artifact store semantic adapter
→ result publication closure
→ tenant/failure binding
→ exact profile catalog
→ retention
→ local filesystem
```

한 slice의 완료 신호:

- Test가 실제 target method를 호출한다.
- Independent expected bytes/digest가 있다.
- Same/same, same/different와 tamper negative case가 함께 있다.
- Pointer-before/after fingerprint를 비교한다.
- No-list/backend-call-count oracle가 있다.
- 실패 시 original bytes/current pointer가 그대로다.

### 7.4 4단계 — 실제 변경

각 변경은 다음 질문에 답해야 한다.

| 질문 | 올바른 답의 모양 |
|---|---|
| 어느 module/package가 의미를 소유하는가? | Port는 application, semantic adapter는 object-common, provider primitive는 backend |
| 어떤 accepted input을 소비하는가? | Phase 08 contract/evidence exact identity |
| 어떤 open 값을 사용했는가? | TEST_ONLY 또는 approved ADR identity |
| 실패하면 무엇이 남는가? | Previous pointer + immutable orphan/quarantine |
| 다음 consumer는 무엇을 받는가? | Exact contract fingerprint/evidence ref/rollback point |
| 무엇을 구현하지 않았는가? | Coordinator/provider/official scope 명시 |

### 7.5 5단계 — 통합과 handoff

Memory와 local adapter에 같은 applicable suite를 실행하고, 승인된 경계가 S3를 Phase 09에 배정했다면 동일 semantic suite를 isolated S3 environment에서 실행한다.

완료 신호:

- Required test skip 0
- Backend별 waiver는 exact non-applicability와 owner 승인이 있음
- Same logical operation의 final authority/failure가 backend 간 같음
- Provider locator가 semantic digest를 바꾸지 않음
- Phase 10 consumer contract test가 exact declared read/CAS만 사용
- Evidence manifest → independent review → acceptance receipt가 단방향임

### 7.6 스스로 묻는 자문 질문

- 이 code가 object 존재와 authority를 섞지 않는가?
- 실패를 `Optional.empty()`나 generic exception으로 지우지 않는가?
- 새로운 `list`, `latest`, `findAll`, `query` API가 hidden database 역할을 하지 않는가?
- CAS 전 check와 put 사이 race가 없는가?
- Same identity/different bytes를 “retry”로 덮지 않는가?
- Provider ETag/generation을 application이 해석하지 않는가?
- Test expected digest를 production codec의 결과에서 그대로 복사하지 않았는가?
- Local mutex가 distributed correctness 증거로 과장되지 않았는가?
- Phase 13/14 gate를 convenience default로 닫지 않았는가?

## 8. 예상 module, package, file과 dependency

### 8.1 Proposed target tree

아래는 canonical Phase 09의 proposed change tree를 사람이 읽기 쉽게 축약한 것이다. 실제 Phase 00 reactor와 Phase 08 contract가 승인된 뒤 이름을 다시 확인한다.

```text
rpdptw/application/
└── src/main/java/com/ronext/rpdptw/application/
    ├── storage/                         # proposed value/result/failure projections
    └── port/out/
        ├── ArtifactStore.java
        ├── RunArtifactRepository.java   # consumer need가 승인된 경우
        ├── RunStateRepository.java
        ├── ResultPublisher.java
        └── ProfileCatalogPort.java

adapters/
├── object-common/
│   └── src/main/java/com/ronext/rpdptw/adapter/object/common/
│       ├── backend/ObjectStorageBackend.java
│       ├── key/ObjectKeyLayout.java
│       ├── codec/ArtifactEnvelopeCodec.java
│       ├── integrity/VerifiedArtifactReader.java
│       ├── artifact/ObjectArtifactStore.java
│       ├── artifact/ObjectRunArtifactRepository.java
│       ├── profile/ObjectProfileCatalog.java
│       ├── state/ObjectRunStateRepository.java
│       ├── publication/ObjectResultPublisher.java
│       ├── security/AuthorizedStorageBinding.java  # name/shape OPEN
│       ├── lifecycle/StorageLifecyclePlanner.java
│       ├── lifecycle/RestrictedObjectMaintenanceBackend.java
│       └── audit/
│           ├── StorageAuditSink.java
│           └── StorageOperationEvent.java
├── object-memory/
│   └── .../InMemoryObjectStorageBackend.java
└── object-filesystem/
    └── .../
        ├── LocalDirectoryObjectStorageBackend.java
        ├── LocalAtomicCapabilityProbe.java
        └── SingleJvmCasCoordinator.java

build/port-contract-tests/
└── src/test/java/com/ronext/rpdptw/storage/contract/
    ├── ObjectStorageBackendContract.java
    ├── ArtifactStoreContract.java
    ├── ProfileCatalogContract.java
    ├── RunStateRepositoryContract.java
    ├── WorkerCommitContract.java
    ├── ResultPublisherContract.java
    ├── StorageFaultContract.java
    ├── StorageCorruptionContract.java
    ├── StorageConcurrencyContract.java
    ├── StorageSecurityContract.java
    └── StorageLifecycleContract.java
```

`AuthorizedStorageBinding`과 `WorkerCommitContract`는 requirement 이름이지 승인된 production type 이름이 아니다. Review가 선택한 exact public shape에 맞춘다.

### 8.2 Module 책임

| Module | 소유 | 소유하지 않음 |
|---|---|---|
| `rpdptw-application` | Provider-neutral port, typed identity/result/failure | Object key string, filesystem, cloud SDK, checksum 구현 |
| `adapters/object-common` | Key/envelope/integrity/closure/profile/state/publication/security/lifecycle semantics | Solve transition, champion, provider SDK |
| `adapters/object-memory` | Deterministic atomic/fault/concurrency reference | Durability/encryption/production claim |
| `adapters/object-filesystem` | Explicit workspace, single-JVM durable reference | Distributed CAS, 일반 POSIX 보장 |
| `build/port-contract-tests` | Reusable backend/semantic oracle | Production runtime class |
| Future approved S3 module | S3 conditional primitive/error mapping | Application/domain/coordinator 의미 |

### 8.3 Dependency 방향

```text
rpdptw-application
        ↑
adapters/object-common
        ↑                 ↑
object-memory      object-filesystem
        ↑                 ↑
        └── port-contract-tests (test scope)

approved future object-s3
        → object-common

apps/local
        → application + selected local adapter
```

금지:

- Application/core/solver/verification → `adapters/object-*`
- Stable module → `java.nio.file`, cloud SDK, provider locator
- Object-common → solver/coordinator internals
- Normal artifact/state/profile repository → restricted maintenance scan/delete port
- Contract test fixture → production runtime
- In-memory adapter → production silent default
- Filesystem adapter → capability probe 없는 distributed claim
- Phase 09 diff → Phase 10 state transition/champion implementation

## 9. Proposed Java skeletal contract

### 9.1 표기 원칙

- `PROPOSED`: 책임을 설명하는 후보다.
- `OPEN`: Exact representation/이름/algorithm은 승인 전 미정이다.
- `BLOCKED`: Cross-phase 결정을 기다려 구현하지 않는다.
- Code block은 복붙용 완성 구현이 아니다. Validation, null policy, codec, exception과 visibility를 실제 contract review에서 채운다.

### 9.2 Identity와 digest value 후보

```java
// PROPOSED internal/application value types
record TenantId(String canonicalValue) {}
record SolveId(String canonicalValue) {}
record WorkerRunId(String canonicalValue) {}
record AttemptId(String canonicalValue) {}
record RoundOrdinal(int value) {}
record WorkerOrdinal(int value) {}

record DigestAlgorithmId(String value) {}
record DigestAlgorithmVersion(String value) {}

record ContentDigest(
    DigestAlgorithmId algorithmId,
    DigestAlgorithmVersion algorithmVersion,
    byte[] bytes
) {
    ContentDigest {
        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }
}

record StateVersion(byte[] opaqueToken) {
    StateVersion {
        opaqueToken = opaqueToken.clone();
    }

    @Override
    public byte[] opaqueToken() {
        return opaqueToken.clone();
    }
}
```

자문:

- `byte[]` equality/hashCode를 record 기본 구현에 맡기면 되는가? 실제 구현은 value semantics를 명시해야 한다.
- `StateVersion`을 log/string으로 노출하는가? Safe token fingerprint 정책이 필요하다.
- Algorithm registry가 unknown/disabled version을 fail-closed하는가?
- `TenantId` canonicalization이 path sanitizer로 alias를 만들지 않는가?

### 9.3 Artifact key/ref 후보

```java
enum ArtifactKind {
    CANONICAL_INPUT,
    PROBLEM,
    PREPARED_TRAVEL,
    BOUND_PROFILE,
    SOLVE_SNAPSHOT,
    EXECUTION_MANIFEST,
    WORKER_ASSIGNMENT,
    WORKER_OUTCOME,
    CANDIDATE_VERIFIER_REPORT,
    FINAL_RESULT,
    RESULT_VERIFIER_REPORT,
    PUBLISHABLE_RESULT,
    PROFILE
    // ROUTE_POOL/HYBRID kinds are gated and must not activate Phase 13.
}

record ArtifactId(String canonicalValue) {}
record ArtifactSchemaVersion(String value) {}
record MediaType(String value) {}
record CompressionIdentity(String value) {}
record OpaqueLocator(String opaqueValue) {}
record EncryptionClassification(String value) {}
record RunIdentity(String canonicalValue) {}

record ArtifactKey(
    TenantId tenantId,
    ArtifactKind kind,
    ArtifactId artifactId
) {}

record ArtifactRef(
    ArtifactKind kind,
    ArtifactSchemaVersion schemaVersion,
    ContentDigest contentDigest,
    long contentLength,
    MediaType mediaType,
    OpaqueLocator opaqueLocator,
    EncryptionClassification classification,
    RunIdentity createdByRun
) {}
```

`ArtifactRef` 전체를 protected-metadata digest로 직렬화하지 않는다. `OpaqueLocator`,
provider generation/version, `createdByRun`, created-at와 observation metadata는 이
projection에서 제외한다. 현재 source가 요구하는 stable authority lineage는 별도의
locator-free `semanticAuthorityRefs`로 표현하고, `createdByRun`은 transport/audit
provenance로만 보존한다. 향후 owner가 `createdByRun`을 semantic field로 승격하려면
기존 encoding에 몰래 포함하지 않고 schema/version migration, authority decision과
relocation vector를 별도로 승인해야 한다.

```java
// PROPOSED/BLOCKED: these types structurally cannot carry physical location
// or observation provenance. Exact encoding/hash algorithm remains OPEN.
record ArtifactSemanticAuthorityRef(
    ArtifactKind kind,
    ArtifactSchemaVersion schemaVersion,
    ContentDigest contentDigest,
    long contentLength,
    MediaType mediaType,
    EncryptionClassification classification,
    List<ArtifactSemanticAuthorityRef> semanticAuthorityRefs
) {
    ArtifactSemanticAuthorityRef {
        semanticAuthorityRefs = List.copyOf(semanticAuthorityRefs);
    }
}

record ArtifactIdentityProjection(
    ArtifactKind kind,
    ArtifactSchemaVersion schemaVersion,
    ContentDigest contentDigest,
    long contentLength,
    MediaType mediaType,
    CompressionIdentity compressionIdentity,
    EncryptionClassification classification,
    List<ArtifactSemanticAuthorityRef> semanticAuthorityRefs
) {
    ArtifactIdentityProjection {
        semanticAuthorityRefs = List.copyOf(semanticAuthorityRefs);
    }
}
```

Identity/security boundary:

1. Projection builder의 입력은 verified `ArtifactRef` closure일 수 있지만 출력 field
   graph에는 `ArtifactRef`, `OpaqueLocator`, provider token/version/generation,
   `RunIdentity createdByRun`, timestamp, attempt, trace ID가 존재할 수 없다.
2. Nested authority ref도 매 level마다 `ArtifactSemanticAuthorityRef`로 다시 투영한다.
   Top-level만 locator-free이고 nested value가 locator-bearing이면 contract failure다.
3. Authority ref graph는 exact verified DAG여야 한다. Cycle은 reject하며 임의 depth
   default로 잘라 digest를 만들지 않는다.
4. Versioned serializer는 위 field allowlist와 명시적 canonical order만 받는다.
   Reflection/record-field iteration/full `ArtifactRef`/unknown-field ignore는 금지한다.
5. Exact canonical encoding과 digest algorithm은 ADR 전 `OPEN`이다. 그러나 field
   inclusion/exclusion, recursive type boundary와 relocation equality는 지금
   `REQUIRED` oracle로 고정할 수 있다.

필수 serialization/type oracle:

```text
ArtifactProjectionContract.projectionTypeCannotCarryLocatorProviderOrObservationFields
ArtifactProjectionContract.nestedAuthorityRefCannotReincludeArtifactRef
ArtifactProjectionContract.relocatingTopLevelAndNestedRefsPreservesCanonicalProjectionBytes
ArtifactProjectionContract.relocatingTopLevelAndNestedRefsPreservesProtectedDigest
ArtifactProjectionContract.createdByRunObservationDoesNotChangeProtectedDigest
ArtifactProjectionContract.semanticFieldChangeChangesProjectionOrFailsClosed
ArtifactProjectionContract.serializerRejectsFullArtifactRefReflectionAndUnknownFields
ArtifactProjectionContract.cyclicAuthorityReferenceGraphIsRejected
```

위 test의 exact bytes/digest equality는 approved test-only codec vector 또는 향후 ADR
vector에 대해 독립 serializer/calculator로 판정한다. Production codec이 만든 expected
bytes를 다시 oracle로 사용하지 않는다.

### 9.4 Run scope와 declared reference 후보

```java
sealed interface RunScope
    permits SolveScope, RoundScope, WorkerScope, AttemptScope {}

record SolveScope(SolveId solveId) implements RunScope {}

record RoundScope(
    SolveId solveId,
    RoundOrdinal roundOrdinal
) implements RunScope {}

record WorkerScope(
    SolveId solveId,
    RoundOrdinal roundOrdinal,
    WorkerOrdinal workerOrdinal,
    WorkerRunId workerRunId
) implements RunScope {}

record AttemptScope(
    WorkerScope workerScope,
    AttemptId attemptId
) implements RunScope {}

record RunArtifactKey(
    ArtifactKey artifactKey,
    RunScope scope,
    ArtifactRef executionManifestRef,
    ContentDigest expectedContentDigest
) {}
```

Phase 10이 requested identity를 execution manifest 안에서 exact resolve해야 할 때만 좁은 repository projection을 둔다. `listWorkers`, `findLatest`, `findByPrefix`는 추가하지 않는다.

### 9.5 Application-owned baseline port

```java
// PROPOSED baseline inherited from Phase 08; not yet accepted.
interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
        ArtifactKey key,
        ArtifactContent content,
        ContentDigest expectedDigest
    );

    ReadableArtifact readVerified(ArtifactRef reference);

    ArtifactMetadata metadata(ArtifactRef reference);
}

interface RunArtifactRepository {
    ReadableArtifact readDeclared(
        ArtifactRef executionManifestRef,
        DeclaredArtifactIdentity identity
    );
}

interface RunStateRepository {
    CreateStateResult createIfAbsent(
        SolveId solveId,
        RunState initialState
    );

    VersionedRunState get(SolveId solveId);

    StateUpdateResult compareAndSet(
        SolveId solveId,
        StateVersion expectedVersion,
        RunState nextState
    );
}
```

`RunState`는 accepted sealed/versioned codec만 받는다. `Map<String,Object>`, Java native serialization과 caller-supplied arbitrary class name을 받지 않는다.

### 9.6 Sealed put/failure 후보

```java
sealed interface ArtifactPutResult
    permits ArtifactPutResult.Created,
            ArtifactPutResult.AlreadyPresent,
            ArtifactPutResult.Conflict {

    record Created(ArtifactRef reference) implements ArtifactPutResult {}
    record AlreadyPresent(ArtifactRef reference) implements ArtifactPutResult {}
    record Conflict(ArtifactKey key, SafeConflictDetail detail)
        implements ArtifactPutResult {}
}

sealed interface StorageFailure
    permits ObjectNotFound,
            AccessDenied,
            IdentityConflict,
            CorruptObject,
            StaleVersion,
            VisibilityIndeterminate,
            PartialWriteAborted,
            UnsupportedCapability,
            QuarantinedObject,
            RetentionBlocked,
            TransientUnavailable {}
```

위 두 hierarchy 사이의 carrier가 F-P09-003 blocker다. 다음을 임의 선택하지 않는다.

```text
금지:
  throw RuntimeException(providerException)
  return Optional.empty()
  map denied/corrupt/indeterminate to Conflict

승인 필요:
  operation별 sealed result
  또는 checked application-failure carrier
  또는 동등한 compile-time exhaustive mapping
```

### 9.7 Authorization binding 후보와 blocker

Application `TenantId`는 authorization proof가 아니다.

```java
// PROPOSED concept only; exact public shape is BLOCKED.
record StorageAccessContext(
    PrincipalRef principal,
    TenantId tenantId,
    StorageOperation operation,
    AccessPurpose purpose
) {}
```

가능한 설계는 explicit tenant-scoped session/facade, operation context parameter 또는 동등한 closure binding이다. 승인 기준:

- Global/static/`ThreadLocal`/implicit request context가 아님
- Async dispatch/retry에서도 scope가 explicit
- Missing/mismatch가 backend lookup 전 fail-closed
- Cross-tenant denial이 existence/length/digest/locator를 노출하지 않음
- Test가 backend invocation count 0을 검증

### 9.8 Worker commit authority 대안

이 operation은 아직 승인되지 않았다.

```text
Option A — logical-key immutable create once
  worker committed key를 한 번만 생성
  same outcome digest는 converge
  different digest는 conflict

Option B — worker-record typed pointer CAS
  distinct WorkerCommitVersion 사용
  response loss와 stale writer reconciliation

Option C — approved repository projection
  solve-level state token을 재사용하지 않는 narrow operation
```

선택 기준:

- Exact `TenantId/SolveId/RoundOrdinal/WorkerOrdinal/WorkerRunId`
- Same/different digest 결과
- Response loss recovery
- Phase 10 `CommittedWorkerOutcomeRef` exact read
- Run-state/publication token과 distinct
- No listing/event/backend-key shortcut

### 9.9 Publication contract 후보와 blocker

```java
record StoredPublishableResultClosure(
    TenantId tenantId,
    SolveId solveId,
    ArtifactRef executionManifestRef,
    ArtifactRef candidateVerifierPassReportRef,
    ArtifactRef finalResultRef,
    ArtifactRef resultVerifierPassReportRef,
    ArtifactRef publishableManifestRef
) {}
```

두 precondition을 분리한다.

```text
run-state authorization fence
  → 이 solve state가 publication을 요청할 수 있는가

publication pointer precondition
  → published pointer가 absent/current expected value인가
```

Phase 08 baseline `compareAndSet(SolveId, StateVersion, PublishableResultRef)`는 둘을 충분히 표현하지 못한다. Phase 08/09/10 공동 승인이 exact signature와 linearization을 정하기 전 구현하지 않는다.

### 9.10 Adapter-internal backend 후보

```java
interface ObjectStorageBackend {
    BackendCapabilities capabilities();

    BackendGetResult getExact(ObjectKey key);

    BackendPutResult putIfAbsent(
        ObjectKey key,
        ObjectWriteSource source,
        ExpectedObjectIntegrity expected
    );

    BackendCasResult compareAndSet(
        ObjectKey key,
        ObjectVersionToken expectedVersion,
        ObjectWriteSource replacement,
        ExpectedObjectIntegrity expectedReplacement
    );

    BackendMetadataResult metadataExact(ObjectKey key);
}

record BackendCapabilities(
    boolean atomicCreateIfAbsent,
    boolean atomicCompareAndSet,
    boolean exactReadAfterCommittedResult,
    ConditionalDeleteCapability conditionalDelete,
    AdapterExecutionScope executionScope
) {}

enum ConditionalDeleteCapability {
    UNAVAILABLE,
    TEST_PROVEN,
    PROVIDER_SEMANTICS_PROVEN
}

enum AdapterExecutionScope {
    TEST_IN_MEMORY,
    LOCAL_SINGLE_JVM,
    DISTRIBUTED_PROVIDER
}
```

Required capability가 `UNAVAILABLE`이면 wrapper가 check-then-put, list-then-decide,
hidden lock로 conformant인 척하지 않는다. Local JVM serialization을 쓰면 scope를
`LOCAL_SINGLE_JVM`으로 노출한다.

`conditionalDelete`는 boolean 광고가 아니다. 아래 restricted operation의 object-version
조건, purge-authorization fence, response-loss reconciliation과 failure surface를 같은
backend/environment에서 contract suite로 증명한 수준이다. Normal artifact/state/profile
repository는 이 interface를 compile-depend하지 않는다.

```java
// PROPOSED restricted maintenance seam; not a normal application port.
interface RestrictedObjectMaintenanceBackend {
    ConditionalDeleteResult deleteExactIfAuthorized(
        AuthorizedMaintenanceAccess access,
        ConditionalDeleteCommand command
    );
}

record ConditionalDeleteCommand(
    MaintenanceOperationId operationId,
    ObjectKey exactKey,
    ObjectVersionToken expectedObjectVersion,
    ContentDigest expectedProtectedDigest,
    PurgeAuthorizationVersion expectedPurgeAuthorization,
    RetentionPolicyIdentity policyIdentity,
    PurgeMarkReceiptRef purgeMarkReceipt
) {}

sealed interface ConditionalDeleteResult
    permits ConditionalDeleteResult.Deleted,
            ConditionalDeleteResult.AlreadyDeletedBySameOperation,
            ConditionalDeleteResult.StaleObjectVersion,
            ConditionalDeleteResult.StalePurgeAuthorization,
            ConditionalDeleteResult.RetentionBlocked,
            ConditionalDeleteResult.QuarantineBlocked,
            ConditionalDeleteResult.AccessDenied,
            ConditionalDeleteResult.Unsupported,
            ConditionalDeleteResult.Indeterminate,
            ConditionalDeleteResult.Corrupt {

    record Deleted(PurgeAuditReceiptRef receipt) implements ConditionalDeleteResult {}
    record AlreadyDeletedBySameOperation(PurgeAuditReceiptRef receipt)
        implements ConditionalDeleteResult {}
    record StaleObjectVersion(SafeObjectFingerprint current)
        implements ConditionalDeleteResult {}
    record StalePurgeAuthorization(SafePurgeFingerprint current)
        implements ConditionalDeleteResult {}
    record RetentionBlocked(RetentionDisposition disposition)
        implements ConditionalDeleteResult {}
    record QuarantineBlocked(QuarantineReceiptRef receipt)
        implements ConditionalDeleteResult {}
    record AccessDenied() implements ConditionalDeleteResult {}
    record Unsupported() implements ConditionalDeleteResult {}
    record Indeterminate(ReconciliationRef reconciliation)
        implements ConditionalDeleteResult {}
    record Corrupt(QuarantineReceiptRef receipt)
        implements ConditionalDeleteResult {}
}
```

Operation/owner/authorization contract:

- Owner는 `Records/Operations` policy owner가 승인하고 `Platform Security`가
  maintenance principal·tenant·purpose·policy version을 검토한 restricted
  maintenance component다. Application status/cancel API와 normal repository는
  delete authority를 소유하지 않는다.
- `AuthorizedMaintenanceAccess`는 tenant, principal, `CONDITIONAL_PURGE`, purpose,
  policy/approval identity를 explicit하게 운반한다. Missing/mismatch는 exact
  metadata lookup과 backend call 전에 `AccessDenied`, call count 0이다.
- Planner는 active state/publication/evidence/rollback roots, hold, quarantine,
  grace와 새 reference를 exact recheck한다. New reference가 mark 뒤 생기면 purge
  authorization을 CAS-cancel하고 backend delete call은 0이다.
- Backend는 exact object version, protected digest와 still-current purge
  authorization을 함께 증명하지 못하면 delete하지 않는다. Provider가 이 semantics를
  증명할 수 없으면 `UNAVAILABLE`; check-then-delete나 unconditional delete로
  보완하지 않는다.
- `Indeterminate` 뒤에는 same operation ID로 exact read/receipt reconciliation만
  허용한다. New operation ID, blind retry 또는 “not found = deleted by us” 추정은
  금지한다.

Failure/rollback contract:

| 시점/결과 | Authority state | 허용 복구 |
|---|---|---|
| Mark 전/mark stale/new reference/hold/quarantine | Object와 기존 pointer 불변 | Mark 취소, preserve |
| Version/purge authorization mismatch | Replacement/current object 불변 | 새 snapshot부터 re-plan |
| Response indeterminate | 삭제 성공/실패 추정 금지 | Exact object+receipt reconcile; pointer 변경 0 |
| Confirmed delete | Purge receipt가 유일한 disposition evidence | Blind rollback 불가. 승인된 retained replica가 있을 때만 별도 authorized restore/create와 새 receipt |

Production delete는 여전히 `OPEN/GATED/DISABLED`다. Phase 09는 operation seam과
fail-closed contract를 요구할 뿐 retention day, provider Object Lock, lifecycle rule,
actual deletion authority를 발명하지 않는다.

Required lifecycle oracle:

```text
StorageLifecycleContract.newReferenceAfterMarkCancelsPurge
StorageLifecycleContract.staleDeleteVersionCannotDeleteReplacement
StorageLifecycleContract.stalePurgeAuthorizationCannotDelete
StorageLifecycleContract.quarantineBlocksAutomaticPurge
StorageLifecycleContract.deniedPurgeCallsBackendZeroTimes
StorageLifecycleContract.indeterminateDeleteRequiresExactReceiptReconciliation
StorageLifecycleContract.purgeProducesAuditReceipt
Phase09StorageArchitectureTest.normalRepositoryCannotDependOnRestrictedMaintenanceDelete
```

### 9.11 Logical observability와 audit contract

`StorageAuditSink`는 “redaction test 하나”가 아니다. Phase 09 logical operation의
완전한 disposition을 판정하기 위한 provider-neutral append-only event sink다. 아래
shape도 public wire schema가 아닌 `PROPOSED` contract이며, exact sink/provider
encoding은 Phase 11/운영 승인 전 열려 있다.

```java
record StorageOperationEvent(
    StorageEventSchemaVersion schemaVersion,
    StorageOperationId operationId,
    AttemptId attemptId,
    PseudonymousTenantRef tenantRef,
    Optional<SolveId> solveId,
    Optional<RunIdentity> runIdentity,
    StorageOperation operation,
    Optional<StorageFaultPoint> faultPoint,
    Optional<ArtifactKind> artifactKind,
    Optional<SafeDigestFingerprint> artifactFingerprint,
    StorageDisposition disposition,
    Optional<SafePointerFingerprint> pointerBefore,
    Optional<SafePointerFingerprint> pointerAfter,
    Optional<QuarantineReceiptRef> quarantineReceipt,
    Optional<HoldReceiptRef> holdReceipt,
    Optional<PurgeAuditReceiptRef> purgeReceipt,
    CorrelationRef correlation
) {}
```

필드 정책:

- Tenant는 승인된 pseudonymous ref만 기록한다. Raw tenant/customer name, principal,
  credential과 access token은 금지한다.
- Digest/token/pointer는 승인된 safe fingerprint policy ID와 bounded fingerprint만
  기록한다. Full digest/token/version bytes, object key, `OpaqueLocator`, bucket/path/URI는
  금지한다.
- Raw PII/address, full payload, input/result bytes, secret, provider exception message와
  stack field의 민감값은 금지한다.
- Event timestamp/provider sequence/completion order는 observation metadata일 수 있지만
  CAS, winner, quality, idempotency 또는 authority 판단 입력이 아니다.

Operation completeness:

| Branch | 필요한 terminal disposition | 추가 oracle |
|---|---|---|
| Create success | `CREATED` | Pointer-before/after는 해당 operation에 적용될 때만 안전 fingerprint로 일치 |
| Idempotent same | `ALREADY_PRESENT_SAME` / `ALREADY_AT_DESIRED` | Original identity와 same operation correlation |
| Identity conflict | `IDENTITY_CONFLICT` | Overwrite 0, safe conflict/quarantine receipt |
| Denied | `ACCESS_DENIED` | Backend lookup/call 0, existence/metadata leakage 0 |
| Corrupt | `CORRUPT` / `CORRUPT_PUBLICATION` | Decode/publish 0, quarantine receipt |
| Visibility/response unknown | `INDETERMINATE` | Success 합성 0, reconciliation ref |
| Stale CAS/delete | `STALE_VERSION` / `STALE_PURGE_AUTHORIZATION` | Pointer/object unchanged |
| Quarantine/hold | `QUARANTINED` / `RETENTION_BLOCKED` | Receipt와 automatic delete 0 |
| Purge | `DELETED` / same-operation reconciled equivalent | Immutable purge receipt |

각 logical operation은 승인된 branch에서 **정확히 하나의 terminal event**를 가져야 한다.
Start/fault-point detail event를 허용해도 terminal event 0/2+, unknown disposition,
operation/result 불일치와 orphan terminal event는 evidence failure다. Audit sink failure를
normal storage success로 숨길 수 있는지는 operation별 policy decision이며, Phase 09
acceptance 전 fail-closed matrix와 owner 승인이 필요하다.

Required audit oracle:

```text
StorageAuditContract.everyRequiredOperationHasExactlyOneTerminalDisposition
StorageAuditContract.createdSameConflictDeniedCorruptIndeterminateAndStaleAreComplete
StorageAuditContract.pointerBeforeAfterMatchesActualCasOutcome
StorageAuditContract.deniedAndCorruptEventsDoNotLeakExistenceOrLocator
StorageAuditContract.quarantineHoldAndPurgeCarryRequiredReceipt
StorageAuditContract.purgeReceiptCorrelatesToMaintenanceOperationAndExactVersion
StorageAuditContract.eventOrderNeverDeterminesAuthority
StorageAuditContract.forbidsRawTenantPiiPayloadSecretLocatorAndOpaqueToken
StorageAuditContract.unknownDispositionOrMissingRequiredFieldFailsEvidence
```

## 10. 상태 전이와 pseudocode

### 10.1 Immutable put

```text
putArtifact(access, key, envelope, content):
  authorizeBeforeLookup(access, key.tenant)
  requireCanonicalTypedKey(key)
  requireEnvelopeMatchesKey(envelope, key)
  requireApprovedExplicitDigestPolicy(envelope)
  requireClassificationPolicy(envelope)

  stream content to staging
  calculate digest and length independently

  if calculated != declared:
    abort staging
    return PARTIAL_WRITE_ABORTED or IDENTITY_CONFLICT

  result = backend.putIfAbsent(finalKey, staging, expectedIntegrity)

  CREATED:
    exact = backend.getExact(finalKey)
    if visibility cannot be confirmed:
      return VISIBILITY_INDETERMINATE
    verify envelope + protected metadata + length + digest
    if corrupt:
      quarantine without overwrite
      return CORRUPT
    return CREATED(reference)

  ALREADY_EXISTS:
    exact = verified exact read
    if exact protected bytes == desired:
      return ALREADY_PRESENT_SAME
    quarantine conflict evidence
    return IDENTITY_CONFLICT

  UNKNOWN/PARTIAL:
    return VISIBILITY_INDETERMINATE
```

### 10.2 Verified read

```text
readVerified(access, reference):
  authorize before existence lookup
  map typed reference to exact canonical object key
  get exact bytes + metadata
  validate envelope schema/kind/tenant/classification
  stream and recompute length/digest
  validate protected-metadata projection
  validate authenticator policy when required
  if mismatch:
    quarantine/incident without overwrite
    return CORRUPT
  only now select allow-listed codec and deserialize
```

### 10.3 State CAS

```text
compareAndSet(access, stateKey, expectedVersion, desiredRecord):
  authorize and canonicalize
  encode desired record with approved versioned codec
  put + verify immutable desired state body
  verify every desired reference closure

  result = backend.compareAndSet(
      currentPointerKey,
      expectedVersion,
      pointerTo(desiredBody),
      expectedPointerIntegrity
  )

  UPDATED:
    exact-read current pointer
    require exact desired body ref
    return UPDATED(newOpaqueVersion)

  VERSION_MISMATCH:
    exact-read current pointer
    if current exact desired ref:
      return ALREADY_AT_DESIRED
    return STALE_VERSION

  RESPONSE_UNKNOWN:
    exact-read current pointer
    if current exact desired ref:
      return ALREADY_AT_DESIRED
    if current still expected:
      return VISIBILITY_INDETERMINATE
    return STALE_VERSION or CORRUPT
```

### 10.4 Publication

```text
publish(access, solveId, runStateFence, publicationPrecondition, resultRef):
  authorize PUBLISH
  exact-read run state and verify runStateFence
  exact-read execution manifest
  exact-read candidate PASS report
  exact-read final result
  exact-read result PASS report
  exact-read publishable manifest
  require same tenant/solve/execution authority
  require no quarantine/unknown schema/digest/classification
  CAS one published pointer using publicationPrecondition
  exact-read pointer
  return CREATED / ALREADY_AT_DESIRED / typed rejection
```

### 10.5 Crash/fault 해석

| Fault point | 안전한 관찰 | Resume |
|---|---|---|
| Payload stream 중 실패 | Final ref 0, staging residue 가능 | Same key retry, residue maintenance |
| Put 뒤 verify 전 crash | Immutable orphan 가능 | Exact read+verify |
| Manifest 전 crash | Payload orphan, authority 없음 | Recreate same manifest or retain |
| Pointer CAS 전 crash | Previous pointer authoritative | Same expected/desired resume |
| CAS success 뒤 response loss | Desired pointer일 수 있음 | Exact read로 `ALREADY_AT_DESIRED` |
| CAS stale | 다른 current가 authoritative | Phase 10이 최신 state에서 판단 |
| Tamper 발견 | Pointer를 정상 반환하지 않음 | Quarantine + incident |

## 11. Ordered work packages

각 WP는 **목적/이유 → 사전조건 → 예상 변경 → 행동 → test/명령 → 기대 → 실패 해석 → rollback → handoff** 순서로 실행한다. 명령은 실제 module이 생긴 뒤 exact reactor path를 다시 확인한다.

### WP-09.0 — Entry receipt, drift와 contract freeze

목적: 코드가 아니라 authority를 먼저 고정하여 5개 cross-phase gap을 구현자가 임의로 메우지 못하게 한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | Phase 00/07/08 accepted receipts, scheduler task/role, stable source sections |
| 예상 file/evidence | Approved entry receipt, contract decision refs, rollback fingerprint; production Java 변경 없음 |
| 구체 행동 | HEAD/live inventory 분리, Phase 08/10 signature 대조, access/failure/worker/publication/S3 owner decision, encoding/key/checksum/CAS ADR 검토 |
| 근거 | Canonical Phase 09 §4, review F-P09-001~005 |
| 금지 shortcut | Neighbor file hash를 acceptance로 사용, pseudo-signature를 approved API로 선언 |
| 검증 | Link/anchor, source blob, owner, open-value scan, accepted receipt existence |
| 현재 명령 | `git status --short`, `git rev-parse HEAD:<path>`, `rg` inventory |
| Future command | `./mvnw -pl rpdptw/application -am verify`는 accepted Phase 08 contract가 생긴 뒤 |
| 기대 | Exact accepted refs, unresolved values, last safe point가 한 receipt에 분리됨 |
| 실패 해석 | `BLOCKED_ENTRY_RECEIPT`; WP-09.1 production/test source·POM·canonical vector freeze 시작 금지. §6.6의 `DESIGN_ONLY`만 허용 |
| Rollback | 문서/receipt proposal만 폐기; code/pointer 없음 |
| Handoff | 모든 gate가 닫힌 경우에만 `APPROVED_ENTRY_RECEIPT`; 아니면 `BLOCKED_ENTRY_RECEIPT` |

사람 checkpoint:

- [ ] Implementer와 independent reviewer가 다르다.
- [ ] Live Phase 00 drift를 accepted foundation으로 오인하지 않았다.
- [ ] S3 ownership decision에 module/environment/command/evidence owner가 있다.
- [ ] 5개 blocker 모두 decision ref가 있다.

### WP-09.1 — Typed identity, key namespace와 envelope

목적: Raw string/path가 tenant crossing과 identity alias를 만들지 못하게 하고 physical locator와 semantic identity를 분리한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.0, approved key/encoding/digest policy, non-ambient access binding |
| 예상 module/package | `rpdptw/application` storage values, `adapters/object-common/key`, `codec`, `integrity` |
| 후보 type | `ArtifactKey`, `ArtifactRef`, `ContentDigest`, `RunScope`, `ArtifactIdentityProjection`, `ObjectKeyLayout` |
| 구체 행동 | Tenant-first IDs, canonical segment encode/decode, locator-free recursive protected projection, allow-listed schema/digest registry |
| 근거 | Integrated §13.2/13.6, Phase 09 §7.2~7.3/§8.1~8.3 |
| 금지 shortcut | Raw concatenation, sanitize/truncate/hash fallback, full `ArtifactRef` hash |
| Exact test | `CanonicalKeyCodecTest.*`, `ArtifactProjectionContract.*`, property injectivity, top-level+nested relocation byte/digest identity |
| Future command | `./mvnw -pl adapters/object-common -am test` |
| 기대 | Collision/escape/alias/locator leakage 0 |
| 실패 해석 | Key/encoding contract가 불안정; persistence code 진행 금지 |
| Rollback | New codec/value를 assembly에서 제거; Phase 08 opaque ref 유지 |
| Handoff | Versioned key/envelope vectors와 contract fingerprint |

완료 신호:

- Valid generated identity에서 collision 0
- Decode→encode byte-exact
- Invalid `/`, `..`, percent escape, Unicode/case alias rejection
- Cross-tenant same raw suffix가 collision하지 않음
- Top-level/nested locator, provider metadata와 `createdByRun` observation이 protected bytes/digest를 바꾸지 않음
- Projection type/serializer가 `ArtifactRef` 재포함, reflection과 unknown field를 거부함

### WP-09.2 — Backend conditional primitive와 in-memory oracle

목적: Provider semantic adapter 전에 one-key atomic primitive와 deterministic fault model을 증명한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.1 |
| 예상 module/package | `adapters/object-common/backend`, `adapters/object-memory` |
| 후보 type | `ObjectStorageBackend`, capability/result/token types, `InMemoryObjectStorageBackend` |
| 구체 행동 | Atomic create, exact get/metadata, opaque CAS, defensive copy, deterministic visibility/partial/tamper/response-loss hook, operation/disposition event recorder |
| 근거 | Integrated §13.3~13.4, Phase 09 §8.7~8.8 |
| 금지 shortcut | Check-then-put, last-write-wins, mutable byte alias, silent production default |
| Exact test | Backend contract, initial same/different create, normal CAS success, CAS race, response loss, partial committed read, pre-state fingerprint, audit completeness |
| Future command | `./mvnw -pl adapters/object-memory -am test` |
| 기대 | Same/same convergence, same/different conflict, CAS exactly one winner |
| 실패 해석 | Backend capability `NOT_CONFORMANT` |
| Rollback | In-memory adapter를 assembly에서 제외; immutable test data만 폐기 |
| Handoff | Capability matrix와 concurrency/fault trace |

False-green 방지:

- `ConcurrentHashMap`을 사용했다는 사실만으로 통과하지 않는다.
- Barrier로 실제 race를 만들고 모든 result/exception/final bytes를 수집한다.
- Expected outcome은 completion order가 아니라 winner count와 final pointer set이다.

### WP-09.3 — Artifact repository, verified closure와 exact profile catalog

목적: Backend primitive를 application port 의미로 조립하고 read-before-decode와 declared exact lookup을 강제한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.2, accepted Phase 08 ports/failure carrier |
| 예상 module/package | `object-common/artifact`, `integrity`, `profile` |
| 후보 type | `ObjectArtifactStore`, `VerifiedArtifactReader`, `ObjectRunArtifactRepository`, `ObjectProfileCatalog` |
| 구체 행동 | Streaming checksum/length, post-write exact verify, failure total mapping, manifest-declared read, exact profile tenant/id/version/preset |
| 근거 | Phase 09 §8.5, review F-P09-003/F-P09-007 |
| 금지 shortcut | List, `latest`, default profile, process registry, digest 검증 전 decode |
| Exact test | `ArtifactStoreContract`, `StorageFailureSurfaceContract`, `ProfileCatalogContract` |
| Future command | `./mvnw -pl adapters/object-common,adapters/object-memory -am test` |
| 기대 | Verified commit 뒤에만 ref 반환, list invocation 0, corrupt decode count 0 |
| 실패 해석 | Semantic adapter non-conformant; ref/payload 권위 없음 |
| Rollback | Adapter 비활성화; backend immutable objects는 orphan retention |
| Handoff | Artifact/run/profile conformance receipt |

### WP-09.4 — Run state, worker commit와 publication CAS

목적: Immutable body와 authoritative pointer를 분리하고 state/worker/publication의 각 linearization point를 증명한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.3, approved worker commit operation, distinct publication preconditions, Phase 07 both-gate identities |
| 예상 module/package | `object-common/state`, `publication` |
| 후보 type | `ObjectRunStateRepository`, approved worker commit repository, `ObjectResultPublisher` |
| 구체 행동 | Desired body put+verify, exact closure, one pointer CAS, stale/lost-response reconciliation, both-gate publication |
| 근거 | Integrated §13.5/13.8, review F-P09-004/F-P09-005 |
| 금지 shortcut | Solve token을 worker/publication token으로 재사용, pointer absent를 hidden default로 가정 |
| Exact test | State/worker/publisher contract, same/different race, orphan result |
| Future command | `./mvnw -pl adapters/object-common,adapters/object-memory -am test` |
| 기대 | Premature pointer 0, different desired exactly one winner, result bytes만으로 publish 0 |
| 실패 해석 | Authority linearization 미증명; Phase 10/11 handoff 금지 |
| Rollback | Previous pointer 유지; desired bodies는 orphan/retention |
| Handoff | `E-P09-CAS` draft와 exact precondition fingerprints |

### WP-09.5 — Fault, corruption, tenant와 security

목적: 정상 happy path가 아니라 실제 storage 실패가 authority를 만들지 못함을 입증한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.4, approved access binding/failure carrier, Security policy stub |
| 예상 module/package | `object-common/security`, `integrity`, `audit`, contract fixtures |
| 구체 행동 | Delayed visibility, stale read, partial write, list noise, tamper, cross-tenant, missing/mismatched binding, classification/authenticator order, logical operation/disposition audit |
| 근거 | Phase 09 §7.8, §10.6~10.8, review F-P09-002/003/006 |
| 금지 shortcut | Denied/corrupt를 not-found로 축소, corrupt overwrite repair, locator/log leakage |
| Exact test | `StorageFaultContract`, `StorageCorruptionContract`, `StorageSecurityContract`, `StorageAuditContract` 전체 required manifest |
| Future command | `./mvnw -pl build/port-contract-tests,adapters/object-memory -am test` |
| 기대 | Corrupt deserialize/publish 0, cross-tenant backend lookup/leak 0, required logical operation terminal event missing/duplicate/forbidden-field 0 |
| 실패 해석 | Adapter `NOT_CONFORMANT`, identity quarantine |
| Rollback | Affected adapter disable; pointer unchanged; incident evidence 보존 |
| Handoff | Fault/corruption/security fixture digests와 `E-P09-TENANT` draft |

### WP-09.6 — Retention/lifecycle와 safe maintenance

목적: Orphan을 정리하면서 active publication/state/evidence/rollback graph를 지우지 않도록 한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.5, Records/Security/Operations policy interface |
| 예상 module/package | `object-common/lifecycle`, restricted maintenance scan |
| 후보 type | Root graph resolver, hold/quarantine marker, planner, `RestrictedObjectMaintenanceBackend`, conditional purge command/result/receipt |
| 구체 행동 | Non-authoritative scan 분리, exact root closure, mark/grace/new-reference cancellation/recheck, explicit maintenance authorization, exact object+purge-version conditional delete, response-loss reconciliation, manual clock, dry-run |
| 근거 | Phase 09 §7.7/§10.10, Master Plan §12~13 |
| 금지 shortcut | Listing/mtime/age-only delete, missing policy permissive default |
| Exact test | `StorageLifecycleContract.*`, denied/backend-call-0, indeterminate receipt reconciliation, normal repository maintenance dependency 금지 |
| Future command | `./mvnw -pl adapters/object-common,adapters/object-memory -am test` |
| 기대 | Active/held/quarantine/new-reference purge 0, stale object/purge-authorization delete 0, purge receipt completeness |
| 실패 해석 | Purge path unsafe 또는 capability `UNAVAILABLE`; Phase 09 exit blocked |
| Rollback | Purge disabled; mark 취소/preserve가 safe default. Confirmed delete는 blind rollback 불가하며 승인된 retained replica의 별도 restore만 허용 |
| Handoff | Retention safety report와 open production values |

### WP-09.7 — Local directory single-JVM reference

목적: Object-storage semantics를 명시적 local workspace에서 durable하게 연습하되 distributed/provider parity로 과장하지 않는다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.2~09.6, approved local filesystem/environment |
| 예상 module/package | `adapters/object-filesystem` |
| 후보 type | `LocalDirectoryObjectStorageBackend`, `LocalAtomicCapabilityProbe`, `SingleJvmCasCoordinator` |
| 구체 행동 | Root confinement, same-FS staging, no-replace publish, fsync/capability probe, single-JVM pointer serialization, restart recovery |
| 근거 | Phase 09 §8.8, Integrated §13.12 |
| 금지 shortcut | Symlink/path escape, OS lock를 distributed guarantee로 주장, unsupported FS에서 best effort |
| Exact test | Same abstract suite, crash snapshots, traversal/symlink/mount boundary |
| Future command | `./mvnw -pl adapters/object-filesystem -am verify` |
| 기대 | Memory와 applicable semantic parity, `LOCAL_SINGLE_JVM`, unsupported fail-closed |
| 실패 해석 | Local adapter exit 불충족; memory oracle만 남음 |
| Rollback | Local assembly/config 제거; workspace 보존/격리 |
| Handoff | Environment/capability fingerprint와 recovery report |

### WP-09.8 — Architecture, provider boundary, evidence와 Phase 10 handoff

목적: 모든 contract를 full reactor와 consumer 관점에서 확인하고 단방향 evidence를 봉인한다.

| 항목 | 내용 |
|---|---|
| 사전조건 | WP-09.0~09.7 green, approved S3 ownership decision |
| 예상 target | Full reactor, architecture rules, immutable evidence manifest |
| 구체 행동 | Forbidden dependency/list/default/provider scan, canonical §10 required-oracle manifest disposition, exact expected/discovered/executed/pass/fail/error/skip counts, backend parity, Phase 10 consumer test, independent review |
| 근거 | Phase 09 §12~16, Master Plan §8~15 |
| 금지 shortcut | Required skip, zero-test success, root BUILD SUCCESS 한 줄, unapproved S3 omission/pull-forward |
| Future command | Approved actual module paths로 Phase slice `verify`, 이후 root `./mvnw verify` |
| 기대 | Required test ID missing 0, required failure/error/skip 0, blocked contract ID 0 at exit, `E-P09-*` complete, accepted review/receipt |
| 실패 해석 | Phase 10 entry와 handoff는 `NOT_READY` |
| Rollback | Accepted Phase 08 local path와 previous pointer/adapter selection |
| Handoff | `Phase09StorageHandoff`, evidence refs, limitations, rollback |

## 12. 테스트 구현 안내

### 12.1 Fixture, builder와 oracle 분리

Production codec/backend가 만든 결과를 expected 값으로 다시 사용하면 같은 결함을 공유한다.

| Test component | 책임 | 금지 |
|---|---|---|
| `P09FixtureBuilder` | Typed identity와 canonical input 조립 | Production key codec 결과를 expected로 복사 |
| Checked-in byte vector | Exact bytes, schema, digest 기대값 | 실행마다 현재 codec으로 재생성 |
| Reference digest calculator | 최소 독립 algorithm으로 expected 계산 | Production registry/stream wrapper 재사용 |
| State model oracle | Pointer/version transition expected set | Backend implementation 호출 |
| Fault backend | Exact fault point/visibility/list noise 주입 | Wall-clock sleep/확률 fault |
| Corruption mutator | 한 byte/field만 변경 | 여러 결함을 동시에 섞어 원인 모호화 |
| Manual clock | Retention/grace 진행 | Filesystem mtime/wall-clock sleep |
| Barrier race harness | Writer 동시 시작과 전체 result 수집 | First completion만 assertion |

모든 fixture/config 값에는 `TEST_ONLY`를 넣는다. Test writer 수, payload size, SHA-256, retry/visibility step, retention duration은 production/official default가 아니다.

### 12.2 핵심 fixture catalog

| Fixture | 변형 | Oracle |
|---|---|---|
| `P09_ARTIFACT_A_TEST_ONLY` | 정상 bytes | Created, exact read |
| `P09_ARTIFACT_A_DUP_TEST_ONLY` | Same key/envelope/bytes | Already-present-same, original unchanged |
| `P09_ARTIFACT_A_CONFLICT_TEST_ONLY` | Same logical identity, different bytes | Identity conflict, overwrite 0 |
| `P09_PARTIAL_STREAM_TEST_ONLY` | N번째 chunk fault | Final ref 0, pointer 0 |
| `P09_DELAYED_VISIBILITY_TEST_ONLY` | Commit 뒤 get unavailable | Indeterminate, CAS call 0 |
| `P09_STALE_READ_TEST_ONLY` | v2인데 v1 반환 | Authority downgrade 0 |
| `P09_LIST_NOISE_TEST_ONLY` | W0 누락/W9 phantom/duplicate | Declared W0/W1 exact result 불변 |
| `P09_RESULT_ORPHAN_TEST_ONLY` | Result/reports 있고 pointer 없음 | Not published |
| `P09_RESULT_BOTH_GATE_TEST_ONLY` | Same authority both PASS closure | Publication success |
| `P09_RESULT_PASS_ONLY_TEST_ONLY` | Result PASS만 있고 candidate PASS 없음 | Publication reject, pointer unchanged |
| `P09_RESULT_CROSS_SOLVE_TEST_ONLY` | 다른 solve/report authority ref | Publication reject, disclosure 0 |
| `P09_RESULT_EXISTING_DIFFERENT_TEST_ONLY` | Published pointer가 다른 desired ref를 가리킴 | Conflict, overwrite 0 |
| `P09_TENANT_CROSS_TEST_ONLY` | Tenant A ref를 B access로 | Denied, leakage/backend call 0 |
| `P09_TAMPER_PAYLOAD_TEST_ONLY` | 한 byte flip | Corrupt, decode/publish 0 |
| `P09_TAMPER_POINTER_TEST_ONLY` | Pointer target/digest flip | Corrupt publication |
| `P09_CAS_RACE_SAME_TEST_ONLY` | Same expected/same desired | One update + convergence |
| `P09_CAS_RACE_DIFFERENT_TEST_ONLY` | Same expected/different desired | Exactly one winner |
| `P09_RETENTION_GRAPH_TEST_ONLY` | Active A, orphan B, held C, quarantine D | B만 grace/recheck 뒤 eligible |
| `P09_PURGE_RACE_TEST_ONLY` | Mark 뒤 새 ref, object replacement, response loss | Delete 0 또는 exact receipt reconciliation |
| `P09_AUDIT_COMPLETENESS_TEST_ONLY` | Success/same/conflict/denied/corrupt/indeterminate/stale/purge | 각 operation terminal 1, forbidden field 0 |
| `P09_LOCAL_RECOVERY_TEST_ONLY` | Body/pointer 전후 crash | Pointer 전 orphan, 뒤 committed |

### 12.3 Canonical 필수 oracle manifest

이 절은 후보 목록이 아니다. [Canonical Phase 09 §10](../../phases/phase-09-object-storage-no-database.md#10-exact-test-fixtures-oracle와-red--green-plan)의
모든 test ID를 Phase exit manifest에 넣는 최소 집합이다. 각 ID는 구현 시작 전 다음
세 disposition 중 정확히 하나를 가져야 한다.

| Disposition | 뜻 | Exit 처리 |
|---|---|---|
| `REQUIRED` | 현재 승인된 contract에서 실행 가능한 필수 oracle | Discovered/executed/pass exact, fail/error/skip 0 |
| `BLOCKED_PENDING_CONTRACT` | Cross-phase/API/ADR owner 결정 전 red test 또는 contract ID로 유지 | 삭제/skip/N/A 금지; Phase exit는 blocked |
| `NOT_APPLICABLE_WITH_APPROVAL` | 특정 backend/layer에 의미상 적용되지 않음을 owner+independent reviewer가 exact 사유로 승인 | Waiver ref와 대체 oracle 필요 |

기본값은 `REQUIRED`다. 현재 `NOT_APPLICABLE_WITH_APPROVAL`로 사전 승인된 ID는 0개다.
F-P09-002~005와 Phase 09/11 provider boundary에 의존하는 항목은 현재
`BLOCKED_PENDING_CONTRACT`이며 contract 승인 뒤 자동 `PASS`가 아니라 `REQUIRED`로
전환해 실제 실행한다. Test class가 일부만 발견됐거나 Maven exit가 0이어도 manifest
ID 누락, duplicate, unknown disposition 또는 required skip이 하나면 실패다.

#### 12.3.1 Key, identity와 recursive protected projection — `REQUIRED`

```text
CanonicalKeyCodecTest.encodesTenantFirstTypedSegments
CanonicalKeyCodecTest.decodeThenEncodeIsByteExact
CanonicalKeyCodecTest.rejectsSlashDotDotPercentAndReservedSegments
CanonicalKeyCodecTest.rejectsUnicodeAndCaseAliases
CanonicalKeyCodecTest.sameArtifactContentAcrossLocatorsKeepsSemanticIdentity
CanonicalKeyCodecTest.opaqueLocatorIsExcludedFromProtectedMetadataProjection
CanonicalKeyCodecTest.crossTenantKeyCannotCollide
CanonicalKeyCodecTest.oversizedSegmentFailsWithoutTruncationFallback
CanonicalKeyCodecPropertyTest.injectiveForGeneratedValidIdentities
ArtifactProjectionContract.projectionTypeCannotCarryLocatorProviderOrObservationFields
ArtifactProjectionContract.nestedAuthorityRefCannotReincludeArtifactRef
ArtifactProjectionContract.relocatingTopLevelAndNestedRefsPreservesCanonicalProjectionBytes
ArtifactProjectionContract.relocatingTopLevelAndNestedRefsPreservesProtectedDigest
ArtifactProjectionContract.createdByRunObservationDoesNotChangeProtectedDigest
ArtifactProjectionContract.semanticFieldChangeChangesProjectionOrFailsClosed
ArtifactProjectionContract.serializerRejectsFullArtifactRefReflectionAndUnknownFields
ArtifactProjectionContract.cyclicAuthorityReferenceGraphIsRejected
```

Encoding/digest ADR 전 exact production bytes green은 만들 수 없지만 ID를 삭제하지 않는다.
Approved `TEST_ONLY` vector로 type/field-exclusion red oracle를 유지하고 production vector는
ADR 승인 뒤 같은 ID에 추가한다.

#### 12.3.2 Put/read/checksum/streaming — `REQUIRED`

```text
ArtifactStoreContract.putAbsentCreatesVerifiedArtifact
ArtifactStoreContract.sameKeySameBytesIsIdempotent
ArtifactStoreContract.sameKeyDifferentBytesIsConflict
ArtifactStoreContract.sameChecksumDifferentProtectedMetadataIsConflict
ArtifactStoreContract.unknownChecksumAlgorithmIsRejected
ArtifactStoreContract.partialStreamReturnsNoReference
ArtifactStoreContract.readRecomputesLengthAndChecksumBeforeDecode
ArtifactStoreContract.copyToDifferentPhysicalLocatorPreservesSemanticIdentity
ArtifactStoreContract.postCommitResponseLossConvergesByExactRead
ArtifactStoreContract.largeStreamingArtifactNeverRequiresWholePayloadInApplicationHeap
StorageFailureSurfaceContract.exhaustivelyMapsEveryStorageFailure
StorageFailureSurfaceContract.indeterminateDeniedCorruptAndUnsupportedNeverCollapseToConflictOrNotFound
```

마지막 두 failure-surface ID는 F-P09-003 carrier 승인 전
`BLOCKED_PENDING_CONTRACT(F-P09-003)`다. Unknown/disabled algorithm은 fallback 0,
large streaming은 `P09_STREAM_SIZE_TEST_ONLY`에서 application whole-payload heap
materialization 0을 판정한다.

#### 12.3.3 State/worker/CAS/concurrency — mixed disposition

다음 state/backend/concurrency ID는 `REQUIRED`다.

```text
RunStateRepositoryContract.createAbsentSucceeds
RunStateRepositoryContract.createExistingSameRecordConverges
RunStateRepositoryContract.createExistingDifferentRecordConflicts
RunStateRepositoryContract.casExpectedVersionUpdates
RunStateRepositoryContract.casStaleVersionNeverOverwrites
RunStateRepositoryContract.casSameDesiredAfterLostResponseConverges
RunStateRepositoryContract.versionTokenIsOpaque
StorageConcurrencyContract.putSameContentHasOnePhysicalWinner
StorageConcurrencyContract.putDifferentContentNeverLastWriteWins
StorageConcurrencyContract.sameDesiredCasConverges
StorageConcurrencyContract.differentDesiredCasHasExactlyOneWinner
StorageConcurrencyContract.concurrentReadNeverReturnsPartialCommittedPayload
StorageConcurrencyContract.failurePreservesPreStateFingerprint
```

다음 worker ID는 승인 전 `BLOCKED_PENDING_CONTRACT(F-P09-004)`이고 승인 뒤
`REQUIRED`다.

```text
WorkerCommitContract.exactDeclaredWorkerCommitHasOneAuthorityPrimitive
WorkerCommitContract.sameWorkerSameDigestConvergesAndDifferentDigestConflicts
WorkerCommitContract.neverUsesSolveOrPublicationVersionAsWorkerPointerToken
```

#### 12.3.4 Manifest ordering와 result authority — mixed disposition

```text
ManifestOrderingContract.dependentArtifactRequiresVerifiedExecutionManifest
ManifestOrderingContract.payloadMustVerifyBeforeReferenceManifest
ManifestOrderingContract.referenceManifestMustVerifyBeforePointerCas
ManifestOrderingContract.pointerFailureLeavesOnlyUnreferencedArtifacts
ManifestOrderingContract.retryAfterPointerResponseLossConverges
ResultPublisherContract.resultBytesWithoutPointerAreNotPublished
ResultPublisherContract.candidatePassAloneCannotPublish
ResultPublisherContract.resultPassAloneCannotPublish
ResultPublisherContract.crossSolveReportCannotPublish
ResultPublisherContract.bothGateClosureThenSingleCasPublishes
ResultPublisherContract.existingDifferentPublishedResultIsConflict
ResultPublisherContract.tamperedPointerNeverReturnsNormalPayload
ResultPublisherContract.runStateFenceAndPublicationPointerUseDistinctPreconditions
ResultPublisherContract.staleRunStateFenceCannotAuthorizePublication
```

Manifest ordering five ID는 `REQUIRED`다. Publisher identity/precondition을 실제
operation에 연결하는 nine ID는 exact signature가 승인될 때까지
`BLOCKED_PENDING_CONTRACT(F-P09-005)`이며, result-PASS-only, cross-solve와
existing-different branch도 생략하지 않는다. Spy event trace는
`PUT → GET+VERIFY → PUT manifest → GET+VERIFY → CAS → GET+VERIFY`를 exact
판정하고 premature CAS/list/deserialize/delete/overwrite event 0을 요구한다.

#### 12.3.5 Listing, visibility와 partial fault — `REQUIRED`

```text
StorageFaultContract.missingListEntryDoesNotHideDeclaredExactArtifact
StorageFaultContract.phantomListEntryDoesNotCreateDeclaredArtifact
StorageFaultContract.duplicateOrReorderedListDoesNotAffectNormalRead
StorageFaultContract.delayedVisibilityReturnsIndeterminateAndSkipsCas
StorageFaultContract.explicitVisibilityReleaseAllowsSameKeyResume
StorageFaultContract.faultBeforeFinalCommitReturnsNoRef
StorageFaultContract.faultAfterCommitBeforeResponseUsesExactRead
StorageFaultContract.staleMetadataCannotAuthorizeCurrentPointer
StorageFaultContract.transientErrorNeverBecomesNotFound
StorageArchitectureTest.normalRepositoryHasNoListingDependency
```

#### 12.3.6 Corruption/tamper — `REQUIRED`

```text
StorageCorruptionContract.rejectsPayloadBitFlip
StorageCorruptionContract.rejectsLengthMismatch
StorageCorruptionContract.rejectsEnvelopeFieldTampering
StorageCorruptionContract.rejectsProtectedMetadataTampering
StorageCorruptionContract.rejectsAuthorityReferenceTampering
StorageCorruptionContract.rejectsUnknownChecksumWithoutFallback
StorageCorruptionContract.rejectsPointerTampering
StorageCorruptionContract.quarantinesWithoutOverwriting
StorageCorruptionContract.neverDeserializesCorruptBytes
```

#### 12.3.7 Security/tenant와 logical audit — mixed disposition

Classification, authenticator, path, scope와 redaction ID는 `REQUIRED`다.
Non-ambient authorization signature에 직접 의존하는 cross-tenant/missing-binding
operation ID는 승인 전 `BLOCKED_PENDING_CONTRACT(F-P09-002)`이고 승인 뒤
`REQUIRED`다.

```text
StorageSecurityContract.deniesCrossTenantPutGetMetadataCasPublish
StorageSecurityContract.missingAuthorizedBindingFailsBeforeBackendLookup
StorageSecurityContract.noGlobalStaticThreadLocalOrImplicitRequestAuthority
StorageSecurityContract.denialDoesNotRevealExistenceLengthChecksumOrLocator
StorageSecurityContract.rejectsMissingOrUnknownClassification
StorageSecurityContract.checksumDoesNotSatisfyEncryptionRequirement
StorageSecurityContract.requiredAuthenticatorFailureRejectsBeforeDecode
StorageSecurityContract.auditRedactsPayloadSecretAndOpaqueLocator
StorageSecurityContract.pathTraversalCannotEscapeLocalWorkspace
StorageSecurityContract.inMemoryAdapterCannotAdvertiseProductionSecurity
StorageSecurityContract.localAdapterDeclaresSingleJvmScope
StorageAuditContract.everyRequiredOperationHasExactlyOneTerminalDisposition
StorageAuditContract.createdSameConflictDeniedCorruptIndeterminateAndStaleAreComplete
StorageAuditContract.pointerBeforeAfterMatchesActualCasOutcome
StorageAuditContract.deniedAndCorruptEventsDoNotLeakExistenceOrLocator
StorageAuditContract.quarantineHoldAndPurgeCarryRequiredReceipt
StorageAuditContract.purgeReceiptCorrelatesToMaintenanceOperationAndExactVersion
StorageAuditContract.eventOrderNeverDeterminesAuthority
StorageAuditContract.forbidsRawTenantPiiPayloadSecretLocatorAndOpaqueToken
StorageAuditContract.unknownDispositionOrMissingRequiredFieldFailsEvidence
```

Audit sink/provider encoding은 open이어도 logical operation/disposition completeness와
forbidden-field 판정은 object-common recorder에서 필수다.

#### 12.3.8 Exact profile — `REQUIRED`

```text
ProfileCatalogContract.resolveExactTenantIdentityVersionAndPreset
ProfileCatalogContract.missingExactVersionDoesNotFallBackToLatestOrDefault
ProfileCatalogContract.sameIdentitySameBytesConverges
ProfileCatalogContract.sameIdentityDifferentProtectedBytesConflicts
ProfileCatalogContract.crossTenantProfileNeverFallsBackOrLeaksExistence
ProfileCatalogContract.listingOrEventIsNeverProfileAuthority
```

#### 12.3.9 Retention/purge — `REQUIRED`, production activation은 별도 `GATED`

```text
StorageLifecycleContract.activePublicationClosureIsNeverPurgeEligible
StorageLifecycleContract.activeStateClosureIsNeverPurgeEligible
StorageLifecycleContract.evidenceHoldBlocksPurge
StorageLifecycleContract.quarantineBlocksAutomaticPurge
StorageLifecycleContract.unreferencedArtifactNeedsExplicitGrace
StorageLifecycleContract.listCandidateIsRecheckedByExactKeyAndVersion
StorageLifecycleContract.newReferenceAfterMarkCancelsPurge
StorageLifecycleContract.staleDeleteVersionCannotDeleteReplacement
StorageLifecycleContract.purgeProducesAuditReceipt
StorageLifecycleContract.missingProductionRetentionPolicyFailsClosed
StorageLifecycleContract.stalePurgeAuthorizationCannotDelete
StorageLifecycleContract.deniedPurgeCallsBackendZeroTimes
StorageLifecycleContract.indeterminateDeleteRequiresExactReceiptReconciliation
Phase09StorageArchitectureTest.normalRepositoryCannotDependOnRestrictedMaintenanceDelete
```

Safety/dry-run/conditional operation oracle는 필수다. Actual production deletion은
provider semantics, policy와 authority가 승인되기 전 계속 disabled/gated다.

#### 12.3.10 Architecture/local/provider closure

```text
Phase09StorageArchitectureTest.applicationHasNoObjectAdapterDependency
Phase09StorageArchitectureTest.stableModulesHaveNoProviderDependency
Phase09StorageArchitectureTest.objectCommonHasNoCoordinatorDependency
Phase09StorageArchitectureTest.normalRepositoryHasNoListingDependency
Phase09StorageArchitectureTest.noRawPathOrUriInApplicationTypes
Phase09StorageArchitectureTest.noNativeJavaSerialization
Phase09StorageArchitectureTest.noMapStringObjectState
Phase09StorageArchitectureTest.noHiddenDefaults
Phase09StorageArchitectureTest.noAmbientStorageAuthority
Phase09StorageArchitectureTest.tokensAreDistinct
Phase09StorageArchitectureTest.inMemoryNotInProductionAssembly
Phase09StorageArchitectureTest.providerMatchesApprovedBoundary
LocalDirectoryObjectStorageBackendContractTest.rejectsWorkspaceEscape
LocalDirectoryObjectStorageBackendContractTest.recoversPointerCrashWindows
LocalDirectoryObjectStorageBackendContractTest.declaresSingleJvmScope
```

Local ID는 target filesystem 승인 전 `BLOCKED_PENDING_CONTRACT(ENVIRONMENT)`.
Provider boundary ID와 승인된 S3 same-suite row는 Phase 09/11 owner decision 전
`BLOCKED_PENDING_CONTRACT(F-P09-001)`이다. 나머지는 `REQUIRED`다.

#### 12.3.11 Machine-checkable 실행·판정 manifest

각 backend/layer command는 다음 row를 immutable evidence로 낸다.

```yaml
requiredOracleRun:
  schemaTemplate: true
  manifestVersion: APPROVED_EXPLICIT_VERSION
  contractFingerprint: APPROVED_EXACT_REF
  backendId: EXACT_BACKEND_AND_SCOPE
  command: EXACT_COMMAND
  environmentFingerprint: EXACT_ENVIRONMENT
  expectedTestIds: [...]
  dispositions:
    REQUIRED: [...]
    BLOCKED_PENDING_CONTRACT: []
    NOT_APPLICABLE_WITH_APPROVAL: []
  discoveredTestIds: [...]
  executedTestIds: [...]
  counts:
    expected: REQUIRED_INTEGER_NO_DEFAULT
    discovered: REQUIRED_INTEGER_NO_DEFAULT
    executed: REQUIRED_INTEGER_NO_DEFAULT
    passed: REQUIRED_INTEGER_NO_DEFAULT
    failed: REQUIRED_INTEGER_NO_DEFAULT
    errors: REQUIRED_INTEGER_NO_DEFAULT
    skipped: REQUIRED_INTEGER_NO_DEFAULT
  missingRequiredIds: []
  duplicateIds: []
  unexpectedIds: []
  waiverRefs: []
  blockedContractRefs: []
  reportDigests: [...]
  exitCode: 0
```

Phase exit oracle:

```text
schemaTemplate is replaced by a concrete immutable run receipt
expected == size(expectedTestIds) > 0
discovered == size(discoveredTestIds)
executed == size(executedTestIds)
set(discovered required IDs) == set(expected REQUIRED IDs)
set(executed required IDs) == set(expected REQUIRED IDs)
passed == executed
missingRequiredIds == []
duplicateIds == []
unexpectedIds == []
failed == 0
errors == 0
skipped == 0
BLOCKED_PENDING_CONTRACT == []
every NOT_APPLICABLE row has owner + reviewer + replacement oracle
command exitCode == 0
fresh report digest belongs to this exact source/build/runtime
```

`failIfNoTests=false`, stale report, class-level discovery만 있는 method 누락, blocked ID를
skip/N/A로 바꾸기와 broad `BUILD SUCCESS`는 모두 이 manifest를 통과하지 못한다.

### 12.4 Red → green 순서

| Red | 먼저 실패할 test | 최소 green | 다음 진입 기준 |
|---:|---|---|---|
| R1 | Key validation/property/projection type | Typed key + recursive semantic projection | Collision/escape/alias/locator-bearing nested ref 0 |
| R2 | Put/read/digest/unknown/stream/partial | Memory backend + artifact store | Same/same/conflict/verify/no-fallback/no-whole-heap exact |
| R3 | Initial create + normal/stale/race/lost-response CAS | Atomic create/CAS | Same/different create, normal success, exactly one + convergence |
| R4 | Ordering/result authority | Closure + publisher | Exact event order, PASS-only/cross-solve/existing-different/premature publication 0 |
| R5 | Visibility/list/fault | Deterministic fault backend | No list/implicit consistency |
| R6 | Corruption | Fail-closed reader/quarantine | Corrupt decode/publish 0 |
| R7 | Tenant/access/failure/audit | Approved binding + carrier + event recorder | Leakage/backend call/terminal-event gap/forbidden field 0 |
| R8 | Exact profile | Immutable object catalog | Latest/default/list 0 |
| R9 | Retention | Root graph + restricted conditional dry-run | Active/held/quarantine/new-ref/stale-version purge 0, receipt exact |
| R10 | Local recovery | Single-JVM filesystem adapter | Unsupported environment fail-closed |
| R11 | Architecture/handoff | Full suite/evidence | Required ID missing/blocked/skip 0, P10 receipt complete |

### 12.5 False-green 방지

- `failIfNoTests=false`나 zero discovered test를 green으로 세지 않는다.
- Canonical §10 ID를 broad test 한 건으로 대체하거나 “후보”라는 이유로 삭제하지 않는다.
- Expected/discovered/executed ID set과 pass/fail/error/skip count를 method 단위로 대조한다.
- `BLOCKED_PENDING_CONTRACT`를 skip/N/A로 바꾸지 않고 Phase exit blocker로 유지한다.
- `-DskipTests`, `maven.test.skip`, stale `target/`를 evidence로 쓰지 않는다.
- Source에 interface가 있다는 사실을 contract pass로 세지 않는다.
- Test가 production codec으로 expected digest를 만들지 않는다.
- Fault test에서 wall-clock sleep과 provider list order를 oracle로 쓰지 않는다.
- Race test에서 “exception이 없었다”만 확인하지 않는다.
- Same/different writer의 final bytes와 pointer winner count를 모두 검사한다.
- Corruption test는 decode/callback count 0을 확인한다.
- Cross-tenant test는 denial뿐 아니라 backend call/leak 0을 확인한다.
- Audit test는 redaction뿐 아니라 operation×disposition terminal completeness와 forbidden-field 0을 확인한다.
- Purge capability enum/boolean만 설정하고 restricted operation을 호출하지 않는 test는 실패다.
- Provider emulator 성공을 actual conditional-write/security evidence로 과장하지 않는다.
- Local adapter green을 distributed/S3 parity로 승격하지 않는다.
- Root build success를 Phase 09 evidence로 복사하지 않는다.

### 12.6 Test layer별 적용성

| Layer | 적용 | Phase 09에서 판정할 것 | 미적용/제한 이유 |
|---|---:|---|---|
| Unit | 필수 | Key, codec projection, digest, closure, retention graph | 없음 |
| Property | 필수 | Key injectivity, alias/collision, state transition invariant | 없음 |
| Contract | 필수 | 모든 backend/semantic adapter 동일 의미 | 핵심 exit evidence |
| Integration | 필수 | Memory/local, 승인된 owner의 S3 conditional behavior | S3 owner 미정이면 gate 유지 |
| E2E | 필수 | Phase 08 submit/artifact/state/publication → verified retrieval | Phase 08 accepted path가 선행 |
| Architecture | 필수 | Dependency, list/default/provider leakage | 없음 |
| Fault | 필수 | Partial, delayed, response loss, stale, duplicate | 없음 |
| Corruption | 필수 | Payload/envelope/ref/pointer one-field mutation | 없음 |
| Reproducibility | 필수 | Same fixture/operation → same semantic identity/outcome | Solver quality replay는 Phase 06/14 |
| Security | 필수 | Tenant denial, classification, redaction, path escape | Production IAM/KMS는 Phase 11 |
| Performance | 제한적 필수 | Streaming/no whole-payload application heap, test-only size | Official throughput/latency/sizing은 provider evidence/Phase 14 gate |
| Benchmark quality | 미적용 | Storage는 route quality를 평가하지 않음 | Phase 14A 소유 |
| MIP/native | 미적용 | Generic artifact kind 이상 하지 않음 | Phase 13 gated |

### 12.7 Pass 판정

| Area | PASS | FAIL |
|---|---|---|
| Put | Same/same exact convergence | Overwrite/key-exists-only idempotency |
| Projection | Top-level/nested locator-free typed bytes와 relocation equality | Nested `ArtifactRef`, provider/observation field 또는 reflection serialization |
| Read | Verify before decode | Decode first/corrupt→not-found |
| CAS | One commit, stale detect, response reconciliation | Check-then-write/stale overwrite |
| Ordering | Artifact verified before pointer | Multi-object success assumption |
| Authority | Pointer + exact both-gate closure | Result/list existence |
| Completeness input | Manifest exact refs | Prefix list/event |
| Visibility | Verified exact read 또는 typed indeterminate | Provider default assumption |
| Security | Tenant-first, classification, redaction | Metadata/locator/PII leakage |
| Observability | Operation별 terminal disposition 정확히 1, safe correlation/receipt와 forbidden field 0 | Redaction-only, missing/duplicate/unknown disposition |
| Lifecycle | Root/hold/quarantine/grace/new-ref/object+purge-version recheck와 receipt | Capability 광고뿐인 delete, age/list-only purge |
| Provider | Approved owner + same suite | Unapproved omission/pull-forward |
| Test execution | Required ID set exact, blocked 0, fail/error/skip 0 | Maven exit 0/class 일부 발견/stale report |

## 13. Verification command와 evidence

### 13.1 Current inventory/document command

현재 실행 가능한 것은 inventory와 document 검증뿐이다.

```bash
git status --short
git rev-parse HEAD
rg --files rpdptw build legacy
rg -n 'ArtifactStore|RunStateRepository|ResultPublisher|ObjectStorageBackend' rpdptw build
git diff --check
```

검색 결과 0은 Phase 09 implementation이 없다는 inventory evidence이지 test pass가 아니다.

### 13.2 Future command와 Maven reactor closure

실제 module path가 승인된 뒤 아래 후보를 갱신한다.

| Layer | Future command | Pass |
|---|---|---|
| Application contract | `./mvnw -pl rpdptw/application -am verify` | Approved port/value/failure/access contract ID 전부 발견·실행·green |
| Object-common | `./mvnw -pl adapters/object-common -am test` | Projection/key/envelope/closure/profile/state/publisher/audit/lifecycle required ID exact |
| Memory | `./mvnw -pl adapters/object-memory -am test` | Abstract suite+create/CAS/race/fault/tamper/purge/audit required ID exact |
| Filesystem | `./mvnw -pl adapters/object-filesystem -am verify` | Same applicable suite+capability/recovery/security; waiver는 owner 승인 exact |
| Contract bundle | `./mvnw -pl build/port-contract-tests -am test` | Canonical §10 + correction oracle manifest의 required abstract method 전부 발견 |
| S3 | `TBD_BY_APPROVED_PHASE_09_11_BOUNDARY` | Approved owner/environment/suite evidence |
| Phase slice | Approved module list with `-am verify` | Required ID missing/duplicate/unexpected/blocked/failure/error/skip 0 |
| Full reactor | `./mvnw verify` | Fresh reactor report와 Phase-slice manifest identity 일치; unrelated required module 포함 green |

현재 root POM의 `failIfNoTests=false`, 11개 POM과
`rpdptw`/`build`/`legacy` module 선언은 correction 시점의 **unaccepted live
inventory**일 뿐 future Phase 09 test closure가 아니다. Accepted Phase 00 reactor
receipt가 생기면 Phase 09 evidence command는 다음을 함께 증명해야 한다.

1. Root에서 실제 선택된 project list에 승인된 Phase 09 production adapter와
   contract-test module이 모두 들어 있다.
2. `-pl` slice와 root reactor가 같은 source/build/runtime 및 oracle manifest
   fingerprint를 사용한다.
3. Surefire/Failsafe가 method-level expected ID 전부를 discovered/executed report로
   냈고, `failIfNoTests`, include/exclude/tag/profile 때문에 빠진 ID가 0이다.
4. Phase slice가 green이어도 root에서 architecture/consumer/provider-boundary test가
   빠지면 exit는 실패다. 반대로 root `BUILD SUCCESS`도 Phase 09 slice의 exact manifest를
   대체하지 않는다.
5. Missing approved module, zero discovered test, stale `target/` report 또는 current
   untracked scaffold를 accepted reactor로 간주한 경우에는 command exit code와
   무관하게 evidence를 폐기한다.

명령마다 기록:

- Exact git/source/build/runtime fingerprint
- Java/Maven/OS/filesystem/provider environment
- Discovered class/method/test count
- Expected/discovered/executed exact test ID set과 disposition
- Passed/failed/error/skipped
- Missing/duplicate/unexpected ID, waiver/blocked-contract ref
- Fixture/config IDs
- Exit code와 start/end time
- Generated evidence digest

### 13.3 Planned evidence

| Evidence | 반드시 포함할 것 |
|---|---|
| `E-P09-STORAGE-CONTRACT` | Source/ADR/P08 receipt, key/envelope/digest vectors, put/read/list/visibility/partial results, memory/local/approved provider matrix |
| `E-P09-CAS` | Initial same/different create, normal/stale/same/different CAS, partial committed read, pre-state fingerprint, pointer crash windows, response loss, worker commit, ordering trace와 both-gate publication negative matrix |
| `E-P09-TENANT` | Access/classification policy, cross-tenant denial, operation/disposition audit completeness, forbidden-field redaction, traversal, authenticator order, tamper/quarantine, conditional purge authorization/failure/receipt와 retention safety |

공통 포함:

- Contract version/fingerprints
- Approved 5 blocker resolution refs
- Phase 09/11 provider boundary decision
- Adapter capability/execution scope
- Test-only value와 production-open 목록
- Canonical required-oracle manifest, exact ID disposition/count와 required missing/skip/blocked 0
- Fault schedule/barrier/corruption vector
- Safe operation event and purge receipt digests
- Architecture scan
- Last safe pointer/rollback ref
- Known limitation
- Independent review/acceptance refs

### 13.4 Evidence DAG

```text
immutable implementation/test evidence
→ preReviewEvidenceManifest [digest M]
→ independentReviewReport [references M, digest R]
→ postReviewAcceptanceReceipt [references M + R]
→ ACCEPTED / handoff
```

Pre-review manifest에 reviewer, verdict, review ref나 acceptance receipt를 넣지 않는다. Review 뒤 manifest를 backfill하지 않는다.

## 14. 사람 checkpoint, stop·resume와 rollback

### 14.1 WP별 checkpoint

| Checkpoint | 사람이 확인할 것 | Stop 조건 |
|---|---|---|
| C0 entry | Accepted P00/P07/P08, owner/task, 5 cross-phase decisions, key/envelope/digest ADR | 하나라도 없음 → `DESIGN_ONLY`; WP-09.1 production/test/POM/vector freeze 착수 금지 |
| C1 key | Versioned vectors, injectivity, locator-free projection | Collision/alias/open encoding 무단 확정 |
| C2 backend | Atomic capability, fault hooks, defensive copy | Check-then-put/LWW |
| C3 semantic | Failure total mapping, no-list, exact profile | Generic exception/latest/list |
| C4 authority | Worker/state/publication distinct CAS | Token 재사용/hidden precondition |
| C5 security | Non-ambient access, leakage 0 | Ambient context/backend call on deny |
| C6 lifecycle | Root/hold/quarantine, restricted operation/authorization/failure/receipt, production policy open | Capability 광고뿐인 delete, stale/new-ref purge, actual delete/default duration |
| C7 local | Capability probe, single-JVM scope | Unsupported FS best effort |
| C8 exit | Provider boundary, evidence/review/receipt | Required skip/evidence gap |

### 14.2 마지막 안전 지점

Entry 전 last safe point는 **문서/손 계산/미실행 fixture proposal뿐이고 production/test
source·POM·canonical byte vector가 없는 상태**다. Runtime implementation이 승인돼
존재한 뒤의 공통 last safe point는 **이전 accepted pointer와 그것이 가리키는
immutable artifact graph**다.

- New payload만 있으면 orphan이다.
- New desired state body만 있으면 orphan이다.
- CAS stale이면 current pointer가 authority다.
- Corrupt identity는 quarantine하며 overwrite하지 않는다.
- Purge mark 전/조건 실패 시 object와 pointer를 보존한다. Confirmed delete는 blind
  rollback할 수 없으므로 production purge는 restorable retained replica와 별도 restore
  authority가 승인되기 전 disabled다.
- Rollback은 파일 삭제나 blind overwrite가 아니다.
- 승인된 이전 immutable ref로 pointer 하나를 조건부 갱신하고 audit evidence를 남긴다.

### 14.3 Stop protocol

Blocker가 생기면:

1. New pointer/CAS를 중단한다.
2. Current pointer와 referenced closure를 exact read/verify한다.
3. New immutable objects를 orphan/quarantine/hold로 분류한다.
4. Backend/provider log에서 secret/locator를 redaction한다.
5. Failure disposition과 last safe ref를 immutable evidence로 남긴다.
6. Scheduler/owner에게 blocker와 required decision을 전달한다.
7. Acceptance/handoff/status를 승격하지 않는다.

### 14.4 Resume protocol

Resume 전:

- Original blocker owner와 approval ref 확인
- Source heading/blob drift 재검토
- Accepted predecessor contract/evidence 재검증
- Pointer/current closure health 확인
- Same logical operation identity 보존
- Test-only/open config 분리
- Previously failed exact test부터 red 재현
- Full applicable suite와 independent review 재실행

### 14.5 사람이 바꾸지 말아야 할 상태

구현자는 implementation proposal와 evidence를 만들 수 있지만 `READY`, `ACCEPTED`, registry와 result summary를 스스로 바꾸지 않는다. 총괄 scheduler만 authoritative status를 갱신한다.

## 15. 흔한 오해와 anti-pattern

- `if (!exists) put()`을 atomic put-if-absent라고 부른다.
- `get → modify → unconditional put`을 CAS라고 부른다.
- CAS conflict를 무한 retry해 latest를 덮어쓴다.
- Same key/different bytes를 latest-wins로 수용한다.
- S3 ETag/provider generation을 application identity나 숫자 version으로 사용한다.
- Run-state/worker/publication token을 재사용한다.
- Bucket/region/path/URI를 semantic fingerprint에 넣는다.
- Raw tenant/solve 문자열을 key에 붙인다.
- Invalid key를 sanitize/truncate/hash해서 다른 valid key로 바꾼다.
- Java native serialization이나 `Map<String,Object>`를 state authority로 사용한다.
- Digest 검증 전에 deserialize한다.
- Digest를 signature/encryption/authorization으로 간주한다.
- Unknown schema/digest/classification을 default로 허용한다.
- Authority를 global/static/`ThreadLocal`/implicit request context에서 가져온다.
- Denied/corrupt/indeterminate/partial/unsupported를 not-found/conflict/empty로 축소한다.
- SDK 2xx/upload receipt/object existence를 committed artifact로 간주한다.
- Candidate PASS 하나만으로 result를 publish한다.
- Published pointer 없이 final bytes를 `SUCCEEDED`로 조회한다.
- Prefix listing으로 worker completeness/champion을 정한다.
- Profile `latest`/default/list/hidden registry를 사용한다.
- Event arrival order를 commit/comparison order로 쓴다.
- Directory rename/file lock/local mutex를 common distributed contract로 광고한다.
- Fixed hidden retry로 delayed visibility를 숨긴다.
- Partial/indeterminate 뒤 새 key를 만들어 logical duplicate를 만든다.
- Corrupt object를 정상 bytes로 overwrite한다.
- Listing/mtime/age만으로 active/held/quarantined artifact를 삭제한다.
- In-memory adapter를 durability/encryption/production evidence로 사용한다.
- S3 ownership 결정을 기다리지 않고 Phase 09에서 넣거나 Phase 11로 미룬다.
- Phase 09에 Phase 10 transition/completeness/champion을 구현한다.
- `Q-BENCH-02`, retention, timeout, retry, size/concurrency 값을 임의 default로 고정한다.
- Phase 13 route pool/MIP를 generic artifact enum 존재로 활성화한다.
- Phase 14A receipt를 production/cutover authority로 해석한다.

## 16. 구현 exit checklist와 Definition of Done

### 16.1 실제 구현 exit checklist

- [ ] Phase 00/07/08 accepted evidence와 Phase 09 task/owners가 있다.
- [ ] 5개 review blocker에 cross-phase approval ref가 있다.
- [ ] Canonical key/envelope/digest/CAS/security/retention version 또는 explicit open boundary가 있다.
- [ ] Application type에 provider SDK/path/URI/list/event DTO가 없다.
- [ ] Non-ambient access binding이 모든 operation에서 lookup 전 강제된다.
- [ ] Operation×failure가 lossless/exhaustive carrier로 보존된다.
- [ ] Typed key가 injective하고 path/alias/collision test가 통과한다.
- [ ] Protected projection과 모든 nested authority ref가 locator-bearing `ArtifactRef`를 구조적으로 재포함하지 않는다.
- [ ] Top-level/nested relocation은 canonical projection bytes/protected digest를 보존하고 `createdByRun` observation은 semantic digest 입력이 아니다.
- [ ] Immutable put은 same/same만 수렴한다.
- [ ] Streaming write가 length/digest를 검증하고 partial final ref/pointer가 0이다.
- [ ] Exact read는 verify 전 deserialize하지 않는다.
- [ ] Visibility를 확인할 수 없으면 typed indeterminate이고 CAS가 0이다.
- [ ] Desired immutable body 뒤 pointer 하나만 CAS한다.
- [ ] Worker commit authority primitive가 approved되고 exact read 가능하다.
- [ ] State/worker/publication precondition identity가 distinct다.
- [ ] Stale CAS overwrite 0, lost response exact convergence가 통과한다.
- [ ] Concurrent different desired는 exactly one winner다.
- [ ] Initial state same/different create, normal CAS success, concurrent partial read와 failure pre-state fingerprint oracle가 통과한다.
- [ ] Payload/reference/pointer exact event ordering test가 통과한다.
- [ ] Normal path의 listing/event authority가 0이다.
- [ ] List noise가 declared exact result를 바꾸지 않는다.
- [ ] Result/report object 존재와 publication authority가 분리된다.
- [ ] Both-gate closure+pointer CAS만 정상 retrieval을 허용한다.
- [ ] Result-PASS-only, cross-solve report와 existing-different publication이 pointer를 바꾸지 않는다.
- [ ] One-field/one-byte tamper가 fail-closed다.
- [ ] Cross-tenant lookup/leak/path escape가 0이다.
- [ ] Exact profile catalog에 latest/default/list/index가 없다.
- [ ] Checksum/authentication/authorization/encryption이 서로 대체되지 않는다.
- [ ] Classification/encryption/authenticator call order와 unknown/disabled digest no-fallback이 통과한다.
- [ ] Logical storage operation마다 terminal disposition이 정확히 하나이고 success/same/conflict/denied/corrupt/indeterminate/stale/quarantine/hold/purge branch가 완전하다.
- [ ] Audit/event에 raw tenant/PII/payload/secret/locator/opaque token이 0이며 safe correlation/pointer/receipt가 결과와 일치한다.
- [ ] Restricted conditional-delete operation의 owner/access/object version/purge authorization/failure/reconciliation/receipt가 검증된다.
- [ ] Active/held/quarantine/new-reference graph purge와 stale object/purge-version delete가 0이다.
- [ ] Memory는 test/reference, local은 `LOCAL_SINGLE_JVM`이다.
- [ ] Required backend가 같은 applicable suite를 통과한다.
- [ ] Canonical §10와 correction oracle manifest의 expected/discovered/executed required ID set이 exact하고 missing/duplicate/unexpected/blocked/failure/error/skip이 0이다.
- [ ] Phase 09/11 S3 owner/module/environment/command/evidence가 승인됐다.
- [ ] Phase 10 coordinator code가 Phase 09 diff에 없다.
- [ ] `E-P09-*`, independent review와 post-review receipt가 있다.
- [ ] Phase 10 handoff와 rollback point가 있다.
- [ ] OPEN/GATED/deferred/test-only가 production default로 바뀌지 않았다.

### 16.2 Phase 09 Definition of Done

Phase 09 `ACCEPTED`는 다음을 모두 뜻한다.

1. DB/hidden index 없이 typed exact key로 immutable artifact를 저장·읽는다.
2. Duplicate는 exact same protected content일 때만 수렴한다.
3. Partial/crash/stale/response-loss에서도 previous authority가 모호해지지 않는다.
4. Object/list 존재는 result/worker/round authority가 아니다.
5. Phase 10은 exact manifest refs와 opaque CAS만 소비한다.
6. Canonical 필수 fault/corruption/concurrency/ordering/publication oracle가 exact
   manifest로 모두 발견·실행되고 false-green 없이 실제 검출된다.
7. Tenant/security/classification/quarantine/retention과 restricted conditional purge가 fail-closed다.
8. Exact profile catalog와 failure/access boundary가 hidden default 없이 구현된다.
9. Logical operation/disposition audit가 완전하고 forbidden field가 없으며 pointer/purge
   receipt가 실제 outcome과 일치한다.
10. Memory/local reference의 scope를 과장하지 않는다.
11. Approved S3 ownership과 Phase 10 책임을 보존한다.
12. Evidence, independent review와 acceptance receipt가 immutable identity로 고정된다.

다음은 DoD가 아니다.

- Object upload/read happy path
- Root build success
- Interface/record 파일 존재
- Memory adapter test만 green
- Local filesystem demo
- S3 SDK integration 2xx
- Planned evidence key나 review 문서 존재
- 일부 test class만 발견된 Maven exit 0 또는 `failIfNoTests=false`
- Conditional-delete capability 광고만 있고 restricted operation/receipt oracle가 없는 상태

## 17. 다음 Phase handoff와 broken 증상

### 17.1 Phase 08에서 받아야 할 것

```text
accepted ArtifactStore / RunStateRepository / ResultPublisher
ArtifactKey / ArtifactRef / ContentDigest identities
Phase 07 PublishableResult and non-success mapping
local workspace/capability scope
same/same and same/different semantics
state/publication CAS meaning
explicit tenant-scoped access binding
lossless failure carrier
approved worker commit authority
distinct publication preconditions
E-P08-PORT / LOCAL-E2E / IDEMPOTENCY
accepted review/receipt/rollback
```

받으면 안 되는 것:

- Filesystem `Path`, bucket/URI/provider SDK type
- Prefix listing result
- Candidate PASS만 있는 success
- `Map<String,Object>` state
- Hidden official worker/round/timeout default

### 17.2 Phase 10에 넘길 것

```text
Phase09StorageHandoff
  phase09ContractVersion
  phase08PortContractFingerprint
  keyLayoutContractFingerprint
  envelopeCodecContractFingerprint
  checksumPolicyIdentity
  backendConditionalContractFingerprint
  artifactStoreContractFingerprint
  runArtifactRepositoryContractFingerprint
  profileCatalogContractFingerprint
  runStateRepositoryContractFingerprint
  workerCommitAuthorityContractFingerprint
  resultPublisherContractFingerprint
  authorizationBindingContractFingerprint
  failureCarrierContractFingerprint
  publicationPreconditionContractFingerprint
  approvedProviderBoundaryDecisionRef
  tenantSecurityPolicyIdentity
  retentionPolicyStatus
  adapterCapabilityMatrix
  E-P09-STORAGE-CONTRACT ref
  E-P09-CAS ref
  E-P09-TENANT ref
  accepted review/receipt ref
  rollback pointer/ref
```

보장할 operation:

- Immutable execution/assignment/outcome/final artifact exact put/read
- Manifest-declared artifact exact resolve
- Exact profile tenant/identity/version/preset
- Solve/round record create/read/CAS
- Approved worker commit create/CAS
- Both-gate publication with distinct preconditions
- Missing/denied/corrupt/stale/indeterminate 구분

넘기면 안 되는 것:

- `listWorkers`, `findLatest`, `queryRunningSolves`
- Legal transition/completeness/champion 계산
- Provider version/path parsing
- Ambient authority/generic storage failure
- Stale CAS auto-overwrite
- AWS SDK/event/context 또는 local `Path`

### 17.3 Phase 11/12/13/14 handoff 제한

| Consumer | 받을 것 | 받으면 안 되는 것 |
|---|---|---|
| Phase 11 | Approved S3 boundary, conditional suite, key/ref mapping | Weaker CAS, unapproved omission/pull-forward |
| Phase 12 | Same semantic suite와 locator-free identity | Provider-specific application meaning |
| Phase 13 | Generic immutable artifact/ref | Route pool/MIP activation |
| Phase 14 | Published pointer와 verified lineage | Test-only values, orphan/list result |

### 17.4 Handoff가 깨졌다는 증상

- Phase 10이 `listWorkers()`를 요청한다.
- Phase 10이 `StateVersion`을 숫자로 증가시킨다.
- Worker commit을 solve state 또는 publication token으로 표현한다.
- Phase 10 action에 S3 key/ETag가 들어간다.
- Result bytes가 있으면 status를 `SUCCEEDED`로 반환한다.
- Missing worker가 empty outcome으로 읽힌다.
- Denied/corrupt가 not-found로 보인다.
- Phase 11 S3 adapter가 application port 의미를 바꾼다.
- Provider copy 뒤 semantic digest가 locator 때문에 달라진다.
- Phase 12 adapter가 별도 weaker contract suite를 가진다.
- Phase 13 artifact kind가 default route-selection을 켠다.
- Phase 14가 test-only retention/retry/benchmark 값을 official로 사용한다.

## 18. Source → requirement → WP → test/evidence 추적성(traceability)

| Requirement | Source | WP | Exact test/evidence |
|---|---|---|---|
| `REQ-NODB` DB/hidden index 없음 | Integrated §13.1, Plan Phase 09 | 09.0, 09.8 | Architecture no-DB/list/lock, `E-P09-STORAGE-CONTRACT` |
| `REQ-ARCH-DAG` provider isolation | Final Architecture §2, Integrated §3 | 09.0, 09.8 | `Phase09StorageArchitectureTest.*Dependency*` |
| `REQ-KEY` typed tenant key/locator separation | Integrated §13.2/13.6 | 09.1 | `CanonicalKeyCodec*`, `E-P09-TENANT` |
| `REQ-PROJECTION` recursive locator-free semantic identity | Canonical review F-P09-006, human review F-HG-P09-002 | 09.1 | `ArtifactProjectionContract.*`, top-level+nested relocation vector, `E-P09-STORAGE-CONTRACT` |
| `REQ-IMMUTABLE` create once | Integrated §13.5, Phase 09 §7.4 | 09.2~09.3 | `ArtifactStoreContract.*` |
| `REQ-CHECKSUM` verify before decode | Final Architecture §5.2 | 09.1~09.3 | Read/digest/corruption tests |
| `REQ-CAS` stale-detecting pointer | Integrated §13.3~13.5 | 09.2, 09.4 | State/concurrency, `E-P09-CAS` |
| `REQ-IDEMPOTENCY` same/same convergence | Final Architecture §3.6 | 09.2~09.4 | Duplicate/lost-response |
| `REQ-WORKER-COMMIT` exact worker authority | Integrated §13.5, Phase 10 §6.3 | 09.0, 09.4 | `WorkerCommitContract`, `E-P09-CAS` |
| `REQ-PRECONDITION` distinct tokens | Phase 08 §7.3, review F-P09-005 | 09.0, 09.4 | Distinct fence/pointer tests |
| `REQ-ORDERING` payload→reference→pointer | Integrated §13.5/13.8 | 09.3~09.4 | `ManifestOrderingContract` |
| `REQ-NOLIST` declared exact refs | Integrated §13.7 | 09.3, 09.5, 09.8 | List-noise/no-list architecture |
| `REQ-AUTHORITY` existence != publication | Master §14.1 | 09.4 | `ResultPublisherContract.*` |
| `REQ-PARTIAL` visibility/fault fail closed | Final Architecture §5.3 | 09.2, 09.5 | `StorageFaultContract`, E-P09-CAS |
| `REQ-CORRUPTION` tamper/quarantine | Integrated §22.4 | 09.3, 09.5 | `StorageCorruptionContract`, E-P09-TENANT |
| `REQ-TENANT` isolation/redaction | Integrated §20 | 09.1, 09.5 | `StorageSecurityContract`, E-P09-TENANT |
| `REQ-OBSERVABILITY` operation/disposition completeness와 forbidden-field redaction | Final Architecture §5.5~5.6, Integrated §19.2~§22, human review F-HG-P09-004 | 09.2, 09.5~09.6, 09.8 | `StorageAuditContract.*`, safe event/receipt digest, E-P09-TENANT |
| `REQ-ACCESS-BINDING` non-ambient authority | Review F-P09-002 | 09.0, 09.5 | Missing/mismatch/backend-call-0 |
| `REQ-FAILURE-CARRIER` lossless failures | Review F-P09-003 | 09.0, 09.3, 09.5 | `StorageFailureSurfaceContract` |
| `REQ-PROFILE` exact immutable catalog | Integrated §13.9, review F-P09-007 | 09.3 | `ProfileCatalogContract` |
| `REQ-RETENTION` safe lifecycle와 restricted conditional purge | Integrated §13.5/§19~20/§26, human review F-HG-P09-003 | 09.6 | `StorageLifecycleContract.*`, maintenance dependency rule, purge receipt와 retention report |
| `REQ-LOCAL` memory/local reference | Integrated §12~13 | 09.2, 09.7 | Backend suite/local recovery |
| `REQ-PROVIDER-BOUNDARY` S3 ownership | Plan Phase 09, Integrated §13.12 | 09.0, 09.8 | Decision ref + approved same-suite evidence |
| `REQ-HANDOFF-P08` consume accepted ports | Plan Phase 08 | 09.0~09.4 | Entry receipt + compatibility |
| `REQ-HANDOFF-P10` exact repository/CAS | Integrated §14 | 09.4, 09.8 | P10 consumer contract + handoff |
| `REQ-NO-PULLFORWARD` no coordinator/provider shortcut | Plan Phase 10~11 | 09.8 | Architecture/scope diff |
| `REQ-ORACLE-MANIFEST` canonical §10 exact coverage/no false-green | Canonical Phase 09 §10/§12~13, human review F-HG-P09-001 | 09.0~09.8 | Required disposition manifest, exact ID/count/report digest, E-P09-* |
| `REQ-ENTRY-LAST-SAFE` WP-09.1 production/test freeze 전 approved entry | Canonical Phase 09 §4/WP-09.0~1, human review F-HG-P09-005 | 09.0 | `APPROVED_ENTRY_RECEIPT` 또는 `BLOCKED_ENTRY_RECEIPT`; source/POM/vector diff 0 |
| `REQ-INVENTORY-LAYERS` authoring/HEAD/live/accepted evidence 분리 | Human-guide README §6, progress registry, human review F-HG-P09-006 | 09.0, 09.8 | Timestamped inventory receipt와 accepted receipt exact-read |
| `REQ-OPEN-GATE` no hidden official/default | Q-BENCH-02/C-17/Q-VAR-01 | 전 WP | Open/default scan + known limitations |

## 19. 구현자가 마지막으로 확인할 짧은 판정표

| 질문 | 예 | 아니오 |
|---|---|---|
| Accepted Phase 00/07/08 contract/evidence와 owner/task가 있는가? | 다음 질문 | `DESIGN_ONLY`; WP-09.1 production/test/POM/vector freeze 착수 금지 |
| 5개 review blocker와 key/envelope/digest ADR가 승인된 decision으로 닫혔는가? | 다음 질문 | `BLOCKED_ENTRY_RECEIPT`; WP-09.1 자체 착수 금지 |
| Same identity/different bytes를 overwrite하지 않는가? | 다음 질문 | 결함 |
| Verify-before-decode가 call order로 증명됐는가? | 다음 질문 | 결함 |
| Authority가 pointer 하나의 CAS에만 있는가? | 다음 질문 | 결함 |
| Listing/event가 normal decision input에서 0인가? | 다음 질문 | 결함 |
| Tenant denial이 backend lookup/leak 전에 일어나는가? | 다음 질문 | 보안 결함 |
| Worker/state/publication token이 distinct한가? | 다음 질문 | 동시성 결함 |
| Memory/local/approved provider suite가 실제 발견·green인가? | 다음 질문 | evidence 미완료 |
| Canonical required oracle ID가 모두 exact 실행되고 blocked/skip/missing 0인가? | 다음 질문 | false-green, exit 금지 |
| Operation/disposition audit와 conditional purge receipt가 완전하고 forbidden field가 0인가? | 다음 질문 | observability/lifecycle 결함 |
| Evidence M→R→receipt가 단방향인가? | 다음 질문 | acceptance 무효 |
| OPEN/GATED/deferred를 default로 닫지 않았는가? | Phase 10 handoff review | authority 위반 |

이 표가 모두 “예”여도 scheduler의 accepted receipt 전에는 Phase 09 상태를 `ACCEPTED`로 바꾸지 않는다.
