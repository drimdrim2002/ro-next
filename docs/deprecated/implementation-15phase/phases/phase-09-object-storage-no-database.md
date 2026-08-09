# Phase 09 — DB 없는 object storage

> 부제: Database 없는 object storage

```yaml
document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
document_version: 1.3
phase: "09"
phase_name: object-storage-no-database
baseline_date: 2026-07-28
implementation_status: NOT_STARTED
evidence_status: NOT_PRODUCED
review_status: COMPLETE_CHANGES_REQUIRED
review_document: ../reviews/phase-09-review.md
entry_gate_status: BLOCKED_BY_UNACCEPTED_PHASE_08
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_status: ALNS_FIRST_GATE_OVERLAY_APPLIED_DOCUMENTATION_ONLY
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: RPDPTW Object Storage owner role
  upstream_contract: Phase 08 Application/Local Runtime owner role
  data_integrity: Architecture/Data Integrity owner role
  security_retention: Platform Security/Records owner roles
  concurrency_oracle: Phase 09 storage-conformance test owner role
  downstream_contract: Phase 10 Provider-neutral Coordinator owner role
  review: independent Phase 09 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture
  - Phase 07 accepted publishable-result and both-gate contract
  - Phase 08 accepted application storage ports and local runtime semantics
  - object canonical encoding/checksum/key-layout ADR review
planned_evidence:
  - E-P09-STORAGE-CONTRACT
  - E-P09-CAS
  - E-P09-TENANT
source_sections:
  canonical_master: "§1~4.6, §10.3, §13, §14, §15.10, §16~17"
  final_domain: "§1, §3, §7~8, §15~18"
  final_architecture: "§1.1~1.5, §2.1~2.7, §3.5~3.6, §5~6.5"
  integrated_design: "§1~3, §12~14, §19~25, §26.2~26.3, §27~28"
  open_questions: "Q-BENCH-02, Q-INFRA-01, Q-VAR-01 and §3~5"
  master_realization_plan: "§2~6, Phase 08~10, §8~15"
  phase_07_actual: "§7.6, §8.4~8.5, §13~16"
  phase_08_actual: "§6.3~7.6, §8.3, §9.1~9.3 and §16.2 handoff"
  phase_10_actual: "§6.1~7.4 and §14.1"
source_fingerprints_sha256:
  README.md: 22eff4f63607db29bd4049344986109c680aa970d0865a3b859598e6b3b96c06
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/deprecated/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/deprecated/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/deprecated/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
  docs/deprecated/master-design-open-questions.md: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
  docs/implementation/master-realization-plan.md: 940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d
  docs/implementation/README.md: 6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358
  docs/implementation/execution-progress-and-results.md: 37f1a8a0ffad1e9614bd54d2b2444739fb83d0951bff73f2a2465ab54a3e8895
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
  sha256: 5da9fd05a027e748b642517d33c0edab86ec818645d5b78c2a0d573fa968419a
neighbor_phase_documents:
  phase_07: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_NOT_PRODUCED_NOT_READY
  phase_08: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_NOT_PRODUCED_NOT_READY
  phase_10: ACTUAL_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_NOT_PRODUCED_NOT_READY
  phase_09_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
review_batch_status: 15_OF_15_PHASE_REVIEWS_COMPLETE
historical_authoring_snapshot:
  phase_08: READY_FOR_REVIEW_NOT_STARTED_NOT_READY_APPEARED_DURING_AUTHORING
  phase_10: READY_FOR_REVIEW_NOT_STARTED_NOT_READY_APPEARED_DURING_AUTHORING
fingerprint_cycle_policy:
  rule: ADJACENT_PHASE_DOCUMENT_FINGERPRINT_AS_ACCEPTANCE_FORBIDDEN
  method: use stable named source sections and accepted artifact/evidence identities
```

## 1. 문서 지위, 권위와 상태 분리

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. Canonical/Final 원문의 `REVIEW` metadata는 source provenance로 보존하지만 이 상세 문서 작성을 멈추는 조건이 아니다. 반대로 이 문서에 Java signature, test 이름과 future command가 있다는 사실은 Phase 09 code, evidence 또는 independent review가 존재한다는 뜻이 아니다.

상태 축은 다음처럼 분리한다.

| 상태 축 | 현재 값 | 의미 |
|---|---|---|
| 문서 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS` | 독립 문서 review의 안전·명백한 수정이 반영됨 |
| 구현 | `NOT_STARTED` | Proposed module/type/adapter/test를 실제 source로 주장하지 않음 |
| evidence | `NOT_PRODUCED` | `E-P09-*`는 미래 evidence bundle key |
| entry gate | `BLOCKED_BY_UNACCEPTED_PHASE_08` | Phase 08 accepted port/local semantics와 ADR evidence가 없음 |
| review | `COMPLETE_CHANGES_REQUIRED` | [독립 리뷰](../reviews/phase-09-review.md)가 교차 Phase blocker를 남김 |
| handoff | `NOT_READY` | Phase 10이 소비할 accepted repository contract가 아직 없음 |

적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../2026-07-31-phase-b-master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 identity/result 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/package/port 배치
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 Phase 09 no-DB 의미
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는 역사 cross-check에만 사용했다. `docs/codex/*`는 역사 자료이며 현재 contract, package 이름, 상태 또는 evidence로 사용하지 않는다.

Final Domain/Architecture에 남은 `Q-INFRA-01 DEFERRED`, `25/1/2` 표현은 최신 Canonical Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `26/1/1`로 해소한다. AWS S3 + Step Functions + Lambda가 target/reference로 선택되었어도 Phase 09는 AWS SDK, S3 adapter, 배포, IAM과 coordinator state machine을 구현하지 않는다. 그 책임은 Phase 11 이후의 명시적 gate에 남긴다.

### 1.1 직접 소비한 source section

| Source | 직접 소비한 section | Phase 09에 고정하는 내용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | §1~§4.6, §10.3, §13, §14, §15.10, §16~§17 | Immutable provenance, logical port, two-gate publication, idempotency/cancellation과 provider 격리 |
| [Final Domain](../../2026-07-26-domain-design.md) | §1, §3, §7~§8, §15~§18 | Immutable semantic identities, final result authority, failure와 evidence ceiling |
| [Final Architecture](../../2026-07-26-architecture-design.md) | §1.1~§1.5, §2.1~§2.7, §3.5~§3.6, §5~§6.5 | Application-owned port, artifact reference, CAS, identity, security, test와 ADR backlog |
| [Integrated design](../../architecture-domain-implementation-design.md) | §1~§3, §12~§14, §19~§25, §26.2~§26.3, §27~§28 | Phase 08 input, no-DB exact-key/CAS, Phase 10 consumer, lifecycle/failure/anti-pattern |
| [질문 등록부](../../master-design-open-questions.md) | `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01`, §3~§5 | Official 수치 open, AWS 선택과 구현 분리, deferred 범위 |
| [Master Realization Plan](../master-realization-plan.md) | §2~§6, Phase 08~10, §8~§15 | Current inventory, Phase DAG, `REQ-NODB`, evidence/DoD/blocker/handoff |
| [구현 문서 지도](../README.md) | §3~§7 | Authority, canonical filename, planned link, scheduler/review 규칙 |
| [Execution tracker](../execution-progress-and-results.md) | §2, §5~§9 | Phase 09 `PLANNED`, task `TBD`, scheduler-only status와 current blockers |
| [Root README](../../../../README.md) | 기술 기준, 배포, placeholder 설명 | Java 25/Maven과 actual GCP placeholder를 target storage evidence에서 분리 |
| [Actual Phase 07](phase-07-independent-verification-final-result.md) | §7.6, §8.4~§8.5, §13~§16 | `PublishableResult`/rejection, both-gate handoff와 provider-free result semantics |
| [Actual Phase 08](phase-08-application-ports-local-runtime.md) | §6.3~§7.6, §8.3, §9.1~§9.3, §16.2 | `ArtifactStore`, `RunStateRepository`, `ResultPublisher`, `OpaqueLocator`의 비해석 규칙, local semantics와 `Phase08ApplicationStorageContractManifest` |
| [Actual Phase 10](phase-10-provider-neutral-coordinator.md) | §6.1~§7.4, §14.1 | Manifest-declared exact run/round/worker refs, one-CAS reducer와 Phase 09 consumer shape |

Historical authoring snapshot에서는 Phase 08/10 상세가 shared checkout에 나타났을 때
`READY_FOR_REVIEW`/`NOT_STARTED`/`NOT_READY`였다. 그 snapshot은 작성 경위일 뿐 live status가
아니다. 2026-07-28 final consistency audit의 live inventory는 Phase 00~14 review
`15/15 COMPLETE`이며, Phase 07/08/10 review verdict는 모두 `CHANGES_REQUIRED`다.
세 target의 implementation은 `NOT_STARTED`, evidence는 `NOT_PRODUCED`, handoff는
`NOT_READY`이므로 accepted implementation authority는 여전히 없다. 인접 문서의 whole-file
또는 section digest를 acceptance 조건으로 상호 기록하지 않는다. 위 stable named section과
이후 accepted artifact/evidence identity로만 handoff를 trace한다.

Phase 08 §16.2는 filesystem/S3 conditional operation을 Phase 09로 넘기고, [Master Realization Plan — Phase 09](../master-realization-plan.md#phase-09--db-없는-object-storage)와 [Integrated design §13.12](../../architecture-domain-implementation-design.md#1312-phase-9-gate)는 filesystem reference와 S3 backend가 같은 abstract suite를 통과하는 출력을 요구한다. 반면 이 문서 v1.0은 local/in-memory만 포함하고 S3를 Phase 11로 미뤘다. 어느 경계를 따를지는 Phase 08/09/11 owner와 scheduler의 외부 권위 결정이 필요하므로 독립 review가 임의로 확정하지 않는다. 그 결정과 source 문서 정합성 수정 전까지 Phase 09 exit는 `BLOCKED`다.

## 2. 목표, 범위와 비범위

### 2.1 목표

Phase 09의 목표는 database 없이 다음 저장 의미를 제공하는 것이다.

```text
typed tenant/run/round identity
+ immutable or explicitly versioned artifact bytes
+ algorithm-tagged checksum and verified read
+ atomic put-if-absent
+ one authoritative state/pointer compare-and-set
+ exact declared references, never listing authority
+ idempotent duplicate convergence
+ explicit retention/security/corruption disposition
= Phase 10이 사용할 provider-neutral run/round storage contract
```

완료의 핵심은 object를 업로드할 수 있다는 사실이 아니다. **정상 결과 authority**는 두 verifier를 포함한 publishable reference가 단일 authoritative publication pointer에 성공적으로 CAS된 경우에만 생긴다. Final-result bytes, verifier report 또는 candidate object가 저장소에 홀로 존재하는 것은 orphan/staged artifact일 뿐 정상 result가 아니다.

### 2.2 포함 범위

- Tenant-scoped typed object-key namespace와 path traversal/alias 방지
- Content-addressed immutable payload와 명시적 schema/version identity
- Versioned state/pointer object와 opaque version token
- Provider-neutral `putIfAbsent`, exact-key verified read, metadata와 compare-and-set semantics
- Same identity/same bytes idempotent convergence와 same identity/different bytes integrity conflict
- Artifact first → verified reference/manifest → single state/pointer CAS ordering
- Execution manifest가 선언한 exact run/round/worker artifact reference 조회
- Prefix/object listing과 event notification을 authority로 쓰지 않는 contract
- Partial upload, delayed/inconsistent visibility, stale read, duplicate writer와 CAS conflict의 typed 처리
- Result object existence와 published-result authority 분리
- Streaming checksum, length/schema/media/classification envelope와 tamper detection
- Tenant authorization, locator opacity, log redaction, classification/encryption policy boundary
- Retention class, hold, quarantine, unreferenced artifact와 safe purge eligibility
- Provider-neutral application storage port refinement과 adapter-internal object backend
- In-memory concurrent conformance adapter와 명시적 single-JVM local directory reference adapter
- Fault/corruption/concurrency/contract fixture와 reusable abstract suite
- Phase 08 계약 receipt와 Phase 10 run/round repository handoff

### 2.3 명시적 비범위

- Phase 09/11 boundary decision 전 AWS S3 adapter, bucket policy, region, versioning, Object Lock, IAM/KMS, multipart tuning 또는 lifecycle rule을 임의로 Phase 09 완료 범위에 포함하거나 제외
- Step Functions, Lambda, ECS, GCP, Azure, Kubernetes 구현·배포·distribution
- Phase 10 solve/round/worker coordinator state machine, transition legality, champion 선택, retry/cancel 업무 판단
- Database, database-like hidden index, distributed lock service, lease table 또는 transaction log
- Prefix listing 기반 worker completeness, recent solve/query/search/pagination 또는 임의 집계
- Multi-object atomic transaction, directory rename, POSIX file lock 또는 default read-after-write consistency 가정
- Phase 08 use case와 both-gate 의미 재정의
- Phase 07 candidate/result verification 재실행 또는 verification report 합성
- External public/wire API, raw object URI, bucket/container/path 노출
- Provider production sizing, throughput, object size, retry count, timeout 또는 retention day의 공식값
- Actual data deletion, legal retention approval 또는 production purge activation
- Phase 13 route pool/MIP artifact 특화 저장 최적화
- Phase 14 official benchmark 수치, baseline 또는 cutover authority

`RunArtifactRepository`와 관련 port는 bytes/reference/state를 안전하게 보존한다. “이 worker가 완료되었는가”, “모든 declared worker가 모였는가”, “어떤 champion이 우수한가”, “다음 state 전이가 합법인가”는 Phase 10 application coordinator의 책임이다.

## 3. Phase-local 결정 상태와 불변조건

이 절의 Java 이름, namespace segment, checksum algorithm, canonical encoding과 test class 이름은 **proposed internal design**이다. 의미는 고정하지만 external compatibility와 production 기본값은 별도 ADR/review 전까지 열려 있다.

### 3.1 결정 상태

| 항목 | 상태 | Phase 09 판단 |
|---|---|---|
| Database 없음 | USER-CONSTRAINT / FIXED | Runtime authority를 DB/index/lock table로 보완하지 않음 |
| Immutable artifact + one CAS pointer | FIXED | 여러 mutable object의 동시 commit을 가정하지 않음 |
| Exact key/reference | FIXED | Listing/event는 authority가 아님 |
| Result existence vs authority | FIXED | Publication pointer 전에는 정상 result가 아님 |
| Conditional semantics | FIXED | Atomic create-if-absent와 stale-detecting CAS가 없으면 conformant backend가 아님 |
| Object backend interface 이름 | PROPOSED INTERNAL | 의미를 지키면 review에서 변경 가능 |
| Key string encoding | OPEN/PROPOSED | Typed identity와 tenant isolation은 고정, exact alphabet/escaping은 ADR 대상 |
| Canonical envelope encoding | OPEN/PROPOSED | Deterministic/versioned여야 하나 JSON/CBOR/protobuf 선택은 미확정 |
| Checksum algorithm/version | OPEN/PROPOSED | Algorithm-tagged여야 함. Fixture의 SHA-256은 test-only이며 hidden production default가 아님 |
| Retention duration/object size/retry count | OPEN | Explicit config/manifest 없이는 production operation을 만들지 않음 |
| In-memory adapter | TEST/REFERENCE | Contract/concurrency oracle이며 durability/security evidence가 아님 |
| Local directory adapter | LOCAL_SINGLE_JVM_REFERENCE | 명시적 capability probe와 single-process 범위; distributed/provider parity 증거가 아님 |
| Authorization context binding | CROSS-PHASE REVIEW REQUIRED | Phase 08 port signature를 조용히 바꾸거나 ambient/global context를 쓰지 않고 tenant-scoped authority를 전달하는 방식이 미확정 |
| Storage failure carrier | CROSS-PHASE REVIEW REQUIRED | Phase 09 typed failure를 Phase 08 `ApplicationFailure`/result에 lossless하게 전달하는 exhaustive mapping이 미확정 |
| Worker committed pointer operation | CROSS-PHASE REVIEW REQUIRED | Phase 10 exact committed-outcome read를 지원할 create/CAS authority primitive가 Phase 08/09 port에 없음 |
| Run-state fence vs publication precondition | CROSS-PHASE REVIEW REQUIRED | `ResultPublisher.expectedState`와 publication pointer 자체의 conditional token을 서로 다른 identity로 정의해야 함 |
| AWS S3 target | BOUNDARY BLOCKED | Phase 09 same-suite 요구와 Phase 11 provider integration 경계가 충돌하며 owner/scheduler 결정 전 어느 완료 claim도 금지 |

### 3.2 반드시 지킬 불변조건

1. **Tenant-first identity:** 모든 logical key와 access decision은 `TenantId`를 포함한다. Raw string concatenation으로 tenant scope를 바꾸지 않는다.
2. **Locator opacity:** Phase 08 `ArtifactRef`의 typed `OpaqueLocator`는 adapter가 생성하고 application은 운반만 한다. Raw bucket/container/path/URI와 local `Path`를 노출·parse하지 않고 locator를 semantic fingerprint에 포함하지 않는다.
3. **Create-once payload:** Immutable artifact key의 성공적 첫 content는 덮어쓰지 않는다.
4. **Same identity, same content:** Duplicate writer가 같은 canonical bytes, checksum, length와 protected metadata를 제시할 때만 idempotent success로 수렴한다.
5. **Conflict on difference:** 같은 logical/content address에 bytes, length, schema, kind 또는 protected metadata가 다르면 last-write-wins가 아니라 integrity conflict다.
6. **Verified read before use:** Checksum/length/envelope 검증 전에는 역직렬화, state transition input, result retrieval 또는 retention root로 사용하지 않는다.
7. **Payload before reference:** Referenced artifact를 먼저 완전히 쓰고 재독해 검증한 뒤 manifest/state/pointer가 이를 참조한다.
8. **One authoritative commit:** 하나의 business storage transition은 immutable prerequisites 뒤 하나의 state/pointer CAS로만 권위화한다.
9. **No multi-object transaction:** 여러 object write의 동시 성공을 원자적 business commit으로 취급하지 않는다.
10. **No listing authority:** Declared reference set은 manifest에서 읽고 exact key로 resolve한다. List 결과의 누락·중복·지연은 completeness 판정에 영향을 주지 않는다.
11. **No implicit visibility:** Backend 성공 응답만으로 read-after-write visibility를 추정하지 않는다. Phase-level `COMMITTED`는 exact read verification까지 완료한 경우만 뜻한다.
12. **Opaque version:** Version token은 equality precondition 외에 숫자 정렬, timestamp, generation 추론에 사용하지 않는다.
13. **CAS fail closed:** Stale/missing/indeterminate CAS를 정상 update로 바꾸거나 silently retry하여 다른 expected version을 쓰지 않는다.
14. **Idempotency without index:** Logical operation identity는 exact key, desired checksum와 expected version으로 표현한다. 숨은 idempotency table을 만들지 않는다.
15. **Authority closure:** Published pointer와 committed run/round pointer가 참조하는 manifest/report/payload는 same tenant/solve/manifest authority에서 모두 verified-readable해야 한다.
16. **Existence is not success:** Candidate/final result/verifier report object 존재, upload receipt 또는 list 발견은 completed/published authority가 아니다.
17. **Corruption is not not-found:** Missing, forbidden, stale, unavailable, malformed와 checksum/tamper를 typed failure로 분리한다.
18. **Checksum is not authenticity:** Content checksum은 우발/악의적 byte 변경 검출 수단이지만 signer trust, authorization 또는 encryption을 대신하지 않는다.
19. **Retention never invents authority:** Listing/age만으로 current pointer, evidence hold, referenced artifact 또는 quarantined incident evidence를 삭제하지 않는다.
20. **Security before existence disclosure:** Tenant/access 검증을 먼저 하며 다른 tenant object의 존재/metadata/locator를 error나 telemetry로 누설하지 않는다.
21. **No hidden default:** Checksum algorithm, retry/visibility budget, object size, retention/grace, concurrency와 encryption requirement는 explicit versioned policy 없이는 production value를 갖지 않는다.
22. **No coordinator pull-forward:** Storage layer는 state bytes/reference와 CAS를 보존할 뿐 legal transition, worker completeness, champion 또는 termination을 계산하지 않는다.
23. **No ambient authority:** Storage access authority를 global/static/`ThreadLocal`/implicit request state에서 조회하지 않는다. Phase 08/09 review가 승인한 tenant-scoped binding이 없으면 operation은 시작하지 않는다.
24. **Distinct preconditions:** Run-state authorization fence, worker commit pointer version과 publication pointer version은 서로 다른 typed identity다. 한 token을 다른 pointer의 CAS token으로 재사용·해석하지 않는다.
25. **Lossless failure surface:** Denied/corrupt/stale/indeterminate/partial/unsupported를 generic unchecked exception, `Conflict`, `NOT_FOUND` 또는 empty result로 축소하지 않는다.

### 3.3 Failure와 result 분류

| 분류 | 의미 | 예 | 자동 정상화 가능한가 |
|---|---|---|---:|
| `NOT_FOUND` | Exact authorized key에 object가 없음 | 아직 쓰지 않은 worker outcome | 아니오; Phase 10이 incomplete/wait 판단 |
| `ACCESS_DENIED` | Caller가 tenant/operation 권한 없음 | Cross-tenant read/write | 아니오; 존재 여부 비공개 |
| `IDENTITY_CONFLICT` | 같은 logical identity에 다른 protected content | Same key/different checksum | 아니오; integrity incident |
| `CORRUPT` | Stored bytes/envelope/checksum/reference closure 불일치 | Truncated payload, tampered pointer | 아니오; quarantine/incident |
| `STALE_VERSION` | CAS expected token이 current와 다름 | Concurrent writer가 먼저 commit | Expected state를 다시 읽고 업무 layer가 결정 |
| `VISIBILITY_INDETERMINATE` | Conditional write 결과를 exact verified read로 확인하지 못함 | Delayed read, timeout after write | 같은 logical key로 조사 가능, pointer commit 금지 |
| `PARTIAL_WRITE_ABORTED` | Stream/write가 commit 전 실패 | Fault after N bytes | Staging cleanup 가능, ref 반환 금지 |
| `UNSUPPORTED_CAPABILITY` | Required atomic conditional semantics를 증명하지 못함 | Filesystem atomic replace 없음 | Adapter startup/use 거부 |
| `QUARANTINED` | Integrity/security 조사 대상으로 격리 | Digest mismatch after prior success | 정상 read/publication 금지 |
| `RETENTION_BLOCKED` | Root/hold/grace/reference 때문에 purge 불가 | Published result, active evidence | 정상 보존 |
| `TRANSIENT_UNAVAILABLE` | Backend가 결과를 확정하지 못함 | I/O interruption | Explicit caller policy 안에서만 retry |

`ALREADY_PRESENT_SAME`와 `ALREADY_AT_DESIRED`는 failure가 아니라 idempotent success disposition이다. 반대로 bytes를 비교하지 않은 “key exists”, stale token을 무시한 “write accepted” 또는 timeout 뒤 추측한 success는 idempotent success가 아니다.

## 4. Entry gate와 확인 evidence

문서 review, fixture 정의와 test-double 설계는 진행할 수 있지만 Phase 09 구현·evidence 착수와 `ACCEPTED` 주장은 다음 gate가 모두 충족될 때까지 차단한다.

| Entry 항목 | 확인 방법 | 2026-07-28 local 관찰 | 판정 |
|---|---|---|---|
| Phase review batch | Phase 00~14 review 문서와 terminal document verdict | `15/15` review `COMPLETE`; Phase 07/08/09/10/11은 `CHANGES_REQUIRED` | COMPLETE — DOCUMENT ONLY |
| Phase 00 accepted architecture | `E-P00-ARCH`, multi-module DAG와 port/adapter dependency | Root 단일 Maven project, accepted evidence 없음 | BLOCKED |
| Phase 07 accepted both-gate output | `PublishableResult`/rejection, accepted `E-P07-*`와 review | Target v1.1 `REVIEWED_WITH_CORRECTIONS`; review `COMPLETE/CHANGES_REQUIRED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED` | BLOCKED |
| Phase 08 accepted port semantics | `ArtifactStore`, `RunStateRepository`, `ResultPublisher`, local E2E와 `E-P08-*` | Target v1.1 `REVIEWED_WITH_CORRECTIONS`; review `COMPLETE/CHANGES_REQUIRED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | BLOCKED |
| Canonical encoding ADR | Schema/version/protected field/order/length/checksum input 정의 | `ADR-ARCH-002/005` backlog만 존재 | BLOCKED |
| Key-layout ADR | Tenant/token encoding, namespace grammar, reserved segment와 migration | Proposed integrated layout만 존재 | BLOCKED |
| CAS capability contract review | Atomic create/CAS/visibility/indeterminate result와 local scope | Target code/test 없음 | BLOCKED |
| Authorization binding review | Phase 08 port에서 explicit tenant-scoped non-ambient authority 전달과 fail-closed missing-context oracle | Public port에는 access parameter가 없고 Phase 09 internal binding만 서술됨 | BLOCKED |
| Failure carrier review | 모든 §3.3 failure의 exhaustive `ApplicationFailure`/result mapping과 caller-visible oracle | `ArtifactPutResult` 3 variants와 internal `StorageFailure` 사이 carrier가 없음 | BLOCKED |
| Worker commit/publication precondition review | Worker committed pointer primitive와 run-state/publication token identity 분리 | Port signature/linearization point가 미정 | BLOCKED |
| Phase 09 S3 boundary decision | Plan/Integrated/Phase 08의 same-suite requirement와 Phase 11 provider integration 책임을 하나로 정렬 | Phase 09 v1.0은 S3를 제외함 | BLOCKED |
| Security/retention policy owner | Classification, authorization, encryption/hold/purge authority | Owner role만 계획됨, production value/approval 없음 | BLOCKED FOR PRODUCTION |
| Scheduler task/owner assignment | Actual task ID와 implementer/test/reviewer 분리 | `TBD_NOT_SUPPLIED` | BLOCKED |

Entry receipt는 최소 다음을 immutable evidence로 남겨야 한다.

- Phase 08 accepted contract version과 three-port signatures
- Phase 07 publishable/rejection contract fingerprint
- Canonical envelope/key/checksum/CAS ADR version
- Required backend capabilities와 local adapter execution scope
- Tenant/access/classification/retention policy identities
- Non-ambient authorization binding과 exhaustive failure carrier contract
- Worker committed pointer operation과 distinct run-state/publication precondition identities
- Phase 09/11 S3 implementation boundary를 승인한 decision record
- Explicit test-only values와 production-open values의 분리
- Source commit/build/toolchain fingerprints
- Rollback point와 independent reviewer

Phase 08이 실제화되면서 type 이름이 바뀌면 Phase 09는 의미를 맞추되 provider type, raw path, hidden default 또는 weaker CAS를 받아들이는 방식으로 호환하지 않는다.

## 5. 2026-07-28 current repository inventory

### 5.1 Actual source/build/test

| 영역 | 실제 상태 | Phase 09 해석 |
|---|---|---|
| Build | Root `pom.xml` 하나인 `com.ronext:ro-next`, Java release 25 | Proposed adapter/application modules와 contract-test module 없음 |
| Main source | `src/main/java` 6개, `com.ronext.optimizer` | Target `com.ronext.rpdptw` storage port/backend 없음 |
| Storage use | `OptimizationApiController`와 `OptimizationWorkerController`가 GCS client 직접 생성 | Provider-neutral port/tenant typed key/CAS 경계와 불일치 |
| Candidate write | `candidates/{requestId}/{runNumber}.json`에 unconditional create | Put-if-absent/digest/same-key conflict evidence 없음 |
| Fan-in | `candidates/{requestId}/` prefix listing 후 보이는 최소 objective 선택 | Declared completeness와 no-list authority에 정면 위배 |
| Result write | `results/{requestId}.json` unconditional create | Both-gate publication pointer/CAS/result authority 없음 |
| Result read | Object 존재 여부로 `RUNNING`/result 판단 | Storage existence와 result authority가 결합됨 |
| Payload | `Map<String,Object>`와 synthetic objective | Versioned envelope/canonical bytes/checksum이 아님 |
| Tests | `AlnsBatchEngineTest` 1개 | Storage/CAS/concurrency/corruption evidence가 아님 |

### 5.2 Actual deployment/history

`gcp/workflows/optimization.yaml`은 worker HTTP 호출 뒤 finalize endpoint를 호출하고, GCP guide는 Cloud Storage `objectAdmin` 권한을 예시로 든다. 이는 current migration characterization이지 Phase 09 target contract 또는 production deployment evidence가 아니다.

특히 다음 actual behavior를 유지해야 할 compatibility로 승격하지 않는다.

- `gs://` URI를 application/domain identity로 사용
- `requestId` raw string으로 object key 조합
- Prefix listing 결과를 complete worker set으로 간주
- Cloud provider SDK exception을 application failure model로 노출
- Unconditional overwrite 또는 provider default precondition
- Object existence를 completed/published result로 해석
- API가 fallback worker 수, iteration과 seed를 hidden/default로 생성

### 5.3 Current neighboring Phase/review status

| Neighbor | 파일 상태 | 계약 상태 | Phase 09 조치 |
|---|---|---|---|
| [Actual Phase 07](phase-07-independent-verification-final-result.md) + [review](../reviews/phase-07-review.md) | Target v1.2 reviewed; review `COMPLETE` | `CHANGES_REQUIRED` / `BLOCKED_NOT_IMPLEMENTED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | Both-gate semantic source로만 대조, accepted input으로 주장하지 않음 |
| [Actual Phase 08](phase-08-application-ports-local-runtime.md) + [review](../reviews/phase-08-review.md) | Target v1.3 reviewed; review `COMPLETE` | `CHANGES_REQUIRED` / `BLOCKED_NOT_IMPLEMENTED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | §6.3~§7.6, §8.3, §9.1~§9.3을 직접 소비하되 acceptance blocker 보존 |
| [Actual Phase 10](phase-10-provider-neutral-coordinator.md) + [review](../reviews/phase-10-review.md) | Target v1.3 reviewed; review `COMPLETE` | `CHANGES_REQUIRED` / acceptance `NOT_RECOMMENDED`; implementation `NOT_STARTED`, evidence `NOT_PRODUCED`, handoff `NOT_READY` | §6.1~§7.4, §14.1의 exact storage consumer를 대조하고 coordinator 의미는 당기지 않음 |

## 6. Proposed 변경 module/package/file tree

아래 tree는 future implementation target이며 현재 존재한다고 주장하지 않는다. Phase 08이 실제 port를 이미 정의하면 같은 책임의 중복 interface를 만들지 않고 그 contract를 소비·정제한다.

```text
ro-next/
├── rpdptw/
│   └── application/
│       └── src/main/java/com/ronext/rpdptw/application/
│           ├── port/out/storage/
│           │   ├── ArtifactStore.java                 # Phase 08 owner, consumed
│           │   ├── RunArtifactRepository.java         # Phase 10 consumer projection
│           │   ├── RunStateRepository.java            # Phase 08 owner, CAS
│           │   ├── ResultPublisher.java                # Phase 08 owner, publication CAS
│           │   └── ProfileCatalogPort.java             # exact immutable profile ref
│           └── storage/
│               ├── ArtifactKey.java
│               ├── ArtifactRef.java
│               ├── ArtifactEnvelope.java
│               ├── ContentDigest.java                 # Phase 08 type, algorithm-tagged refinement
│               ├── StateVersion.java                  # Phase 08 opaque CAS token
│               ├── RunArtifactKey.java
│               ├── RunRecordKey.java
│               ├── StorageAccessContext.java
│               ├── StorageFailure.java
│               └── result/
│                   ├── PutArtifactResult.java
│                   ├── ReadArtifactResult.java
│                   ├── CreateRecordResult.java
│                   ├── CompareAndSetResult.java
│                   └── PublicationResult.java
├── adapters/
│   ├── object-common/
│   │   └── src/main/java/com/ronext/rpdptw/adapter/object/common/
│   │       ├── backend/
│   │       │   ├── ObjectStorageBackend.java
│   │       │   ├── ObjectKey.java
│   │       │   ├── ObjectVersionToken.java
│   │       │   ├── BackendCapabilities.java
│   │       │   └── BackendFailure.java
│   │       ├── key/
│   │       │   ├── ObjectKeyLayout.java
│   │       │   └── CanonicalKeyCodec.java
│   │       ├── artifact/
│   │       │   ├── ObjectArtifactStore.java
│   │       │   ├── ObjectRunArtifactRepository.java
│   │       │   ├── ObjectProfileCatalog.java
│   │       │   └── VerifiedArtifactReader.java
│   │       ├── state/
│   │       │   ├── ObjectRunStateRepository.java
│   │       │   └── ObjectResultPublisher.java
│   │       ├── integrity/
│   │       │   ├── ChecksumRegistry.java
│   │       │   ├── EnvelopeCodec.java
│   │       │   └── ReferenceClosureVerifier.java
│   │       ├── lifecycle/
│   │       │   ├── RetentionPlanner.java
│   │       │   ├── RetentionClass.java
│   │       │   ├── HoldRecord.java
│   │       │   ├── QuarantineRecord.java
│   │       │   └── MaintenanceObjectScanner.java       # non-authoritative
│   │       └── security/
│   │           ├── StorageAuthorizer.java
│   │           ├── AuthorizedStorageBinding.java        # reviewed non-ambient binding only
│   │           ├── StorageSecurityPolicy.java
│   │           └── StorageAuditSink.java
│   ├── object-memory/
│   │   ├── src/main/java/.../adapter/object/memory/
│   │   │   └── InMemoryObjectStorageBackend.java
│   │   └── src/test/java/.../
│   │       └── InMemoryObjectStorageBackendContractTest.java
│   └── object-filesystem/
│       ├── src/main/java/.../adapter/object/filesystem/
│       │   ├── LocalDirectoryObjectStorageBackend.java
│       │   ├── LocalAtomicCapabilityProbe.java
│       │   └── SingleJvmCasCoordinator.java
│       └── src/test/java/.../
│           └── LocalDirectoryObjectStorageBackendContractTest.java
└── build/
    └── port-contract-tests/
        └── src/test/java/com/ronext/rpdptw/storage/contract/
            ├── ObjectStorageBackendContract.java
            ├── ArtifactStoreContract.java
            ├── ProfileCatalogContract.java
            ├── RunStateRepositoryContract.java
            ├── ResultPublisherContract.java
            ├── StorageFaultContract.java
            ├── StorageCorruptionContract.java
            ├── StorageConcurrencyContract.java
            ├── StorageSecurityContract.java
            └── StorageLifecycleContract.java
```

### 6.1 Module 책임과 변경 제한

| Module | 소유하는 것 | 소유하지 않는 것 |
|---|---|---|
| `rpdptw-application` | Provider-neutral port, typed identity/result/failure | Object key string, filesystem, provider SDK, checksum 구현 |
| `adapters/object-common` | Object backend를 application port 의미로 조립, exact immutable profile catalog, key codec, integrity/closure/lifecycle/security enforcement | Solve transition, worker completeness, champion, provider SDK |
| `adapters/object-memory` | Concurrent deterministic test/reference semantics, fault hook | Durability, encryption, multi-process 또는 production claim |
| `adapters/object-filesystem` | Explicit local workspace와 single-JVM durable reference | Distributed CAS, POSIX 일반 보장, cloud parity |
| `build/port-contract-tests` | 모든 backend/semantic adapter가 재사용할 abstract oracle | Production code, provider-specific expectation |

Phase 08의 `ArtifactStore`, `RunStateRepository`, `ResultPublisher` signature가 충분하면 Phase 09는 이를 수정하지 않는다. `RunArtifactRepository`는 Phase 10이 run/round/worker exact artifact를 읽고 쓰는 storage-only projection이 필요할 때만 추가한다. “편의를 위한 mega repository”나 port service locator를 만들지 않는다.

`AuthorizedStorageBinding`은 이름까지 확정된 구현 지시가 아니다. Phase 08/09 cross-phase review가 승인한 tenant-scoped authority 전달을 표시하는 placeholder다. 승인된 binding은 global/static/`ThreadLocal`/implicit request context를 사용하지 않으며, 누락·tenant mismatch를 backend lookup 전에 fail-closed로 거부해야 한다.

### 6.2 Dependency DAG

```text
rpdptw-application
        ↑
adapters/object-common
        ↑                 ↑
adapters/object-memory   adapters/object-filesystem
        ↑                 ↑
        └──── build/port-contract-tests (test scope only)

apps/local → rpdptw-application + selected local object adapter
```

금지 dependency:

- Application/core/solver/verification → `adapters/object-*`
- Application/core/solver/verification → `java.nio.file`, cloud SDK 또는 provider locator
- Object-common → solver, verification implementation 또는 coordinator package
- Contract-test fixture → production runtime classpath
- In-memory adapter → production distribution의 silent default
- Filesystem adapter → OS lock/atomicity를 capability probe 없이 일반 보장으로 주장

## 7. Artifact, contract, identity와 lifecycle

### 7.1 Storage existence와 result authority

Phase 09가 보존할 가장 중요한 분리는 다음이다.

| 관찰 | 의미 | 정상 result authority인가 |
|---|---|---:|
| Final result bytes key가 존재 | Immutable bytes가 한 번 저장되었을 수 있음 | 아니오 |
| Candidate/result verifier report가 존재 | Report artifact가 저장되었을 수 있음 | 아니오 |
| `PublishableResultRef` object가 존재 | Both-gate lineage를 주장하는 immutable manifest | 아직 아님 |
| Published pointer가 `PublishableResultRef`로 CAS되고 closure 재검증됨 | 해당 solve의 authoritative published result | 예 |
| Prefix list에서 result를 발견 | Discovery/maintenance hint | 아니오 |
| Upload/SDK success receipt만 있음 | Exact verified read 전에는 indeterminate | 아니오 |

정상 retrieval은 `published pointer → PublishableResultRef → candidate report + result report + payload`의 exact chain을 같은 tenant/solve authority에서 검증한다. Pointer가 없으면 result bytes가 있어도 `NOT_PUBLISHED`다. Pointer는 있는데 closure가 깨졌으면 `CORRUPT_PUBLICATION`이지 `RUNNING`이나 빈 result가 아니다.

### 7.2 Logical namespace와 key grammar

Application은 raw key string을 조립하지 않는다. Typed identity를 `ObjectKeyLayout`이 adapter-internal key로 encode한다.

```text
TenantId
  └─ SolveId
      ├─ ExecutionManifestRef
      ├─ RoundOrdinal
      ├─ WorkerOrdinal
      ├─ WorkerRunId
      ├─ AttemptId
      ├─ ArtifactKind
      └─ ContentDigest / logical record name
```

Proposed logical layout:

```text
tenants/{tenantToken}/
├── artifacts/{artifactKind}/{schemaVersion}/{algorithm}-{checksum}
├── profiles/{profileKey}/{profileVersion}/{profileChecksum}
├── submissions/{submissionToken}/pointer
└── solves/{solveToken}/
    ├── manifests/{manifestChecksum}
    ├── state/current
    ├── cancellation/intent
    ├── rounds/{roundOrdinal}/
    │   ├── state/current
    │   ├── assignments/{workerOrdinal}/{assignmentChecksum}
    │   ├── workers/{workerOrdinal}/
    │   │   ├── runs/{workerRunToken}/attempts/{attemptToken}/{outcomeChecksum}
    │   │   └── committed
    │   └── champion
    └── results/
        ├── manifests/{publishableChecksum}
        └── published
```

이 layout은 responsibility와 exact lookup을 설명하는 **proposed encoding**이다. 다음 규칙은 encoding 선택과 무관하게 고정한다.

1. `tenantToken`, `solveToken`, `workerRunToken`은 validated typed ID의 canonical encoding이다.
2. User-supplied `/`, `..`, percent escape, Unicode normalization alias, empty/reserved segment를 직접 삽입하지 않는다.
3. Decode 후 re-encode가 byte-exact canonical key와 같지 않으면 거부한다.
4. Artifact content address는 algorithm/version/kind/schema를 포함한다.
5. State/pointer의 logical name은 제한된 enum/typed key이고 arbitrary caller path가 아니다.
6. Bucket/container/region/account/project/absolute `Path`는 bootstrap config이며 logical identity에서 제외한다.
7. Key length/segment 제한은 adapter capability/config로 explicit 검증하며 truncate/hash fallback을 숨기지 않는다.
8. Cross-tenant reference, same string alias와 case folding을 금지한다.

### 7.3 Immutable artifact envelope

```text
ArtifactEnvelope
  envelopeSchemaVersion
  tenantId
  artifactKind
  artifactSchemaVersion
  contentChecksum { algorithmId, algorithmVersion, bytes }
  contentLength
  mediaType
  compressionIdentity or NONE
  encryptionClassification
  semanticAuthorityRefs
  createdByLogicalRun
  protectedMetadataChecksum
```

`createdAt`, provider locator, storage generation, upload attempt, observed latency와 diagnostic trace ID는 storage metadata가 될 수 있지만 semantic content identity의 hidden input으로 사용하지 않는다. 반대로 kind/schema/checksum/length/compression/classification/authority refs처럼 읽기 의미를 바꾸는 field는 protected metadata에 포함한다.

`ArtifactRef` 전체를 그대로 hash하는 것은 금지한다. Semantic identity/protected-metadata canonical projection은 `kind`, `schemaVersion`, algorithm-tagged `contentDigest`, `contentLength`, `mediaType`, `compressionIdentity`, `encryptionClassification`, stable `createdByRun`/authority refs만 포함한다. `opaqueLocator`, provider object version, storage generation과 observation metadata는 sidecar/reference transport metadata로 남고 projection에서 제외한다. 따라서 동일한 protected content를 다른 physical locator로 복사해도 semantic identity는 동일하며, locator가 바뀌었다는 이유로 checksum을 재정의하지 않는다.

Checksum 규칙:

- Algorithm과 version을 값에 포함한다.
- Unknown/disabled algorithm은 typed rejection이다.
- Caller가 선언한 checksum만 믿지 않고 streaming write와 verified read에서 재계산한다.
- Same checksum string이더라도 kind/schema/length/protected metadata가 다르면 같은 artifact로 취급하지 않는다.
- Checksum collision 또는 same address/different bytes가 관찰되면 자동 algorithm fallback이나 overwrite를 하지 않고 integrity incident로 격리한다.
- `SHA-256` fixture는 `P09_SHA256_TEST_ONLY`로만 사용한다. Production algorithm 승인으로 읽지 않는다.

### 7.4 Immutable, versioned와 pointer object 분류

| 분류 | 예 | Write rule | Authority |
|---|---|---|---|
| Content-addressed immutable | Snapshot, manifest, assignment, outcome, verifier report, final result | `putIfAbsent`, overwrite 금지 | 참조되기 전에는 staged/orphan 가능 |
| Logical-key immutable | Exact submission/profile version | `putIfAbsent`, same bytes만 수렴 | Exact identity로 조회 |
| Versioned state | Solve/round state record | New canonical version + one CAS pointer | Current pointer만 authoritative |
| Commit pointer | Worker committed outcome, round champion | Referenced closure 검증 뒤 CAS | CAS success token이 authority |
| Publication pointer | Published result | Both-gate closure 검증 뒤 CAS | 정상 result의 유일한 storage authority |
| Maintenance marker | Hold/quarantine/purge candidate | Versioned/CAS, business authority 아님 | Retention/security control |

Mutable state bytes를 같은 key에 blind overwrite하지 않는다. Adapter가 provider conditional replace를 사용하더라도 application에는 `expected StateVersion`이 있는 CAS로만 노출한다.

### 7.5 Manifest-before/after ordering

“Manifest를 먼저 쓴다”와 “payload 뒤에 manifest를 쓴다”는 서로 다른 manifest에 대한 규칙이다.

1. **Declaration before work:** `ExecutionManifest`는 run/round/worker expected identity와 exact assignment plan을 선언하므로 어떤 dependent assignment/outcome보다 먼저 immutable 저장·검증되어야 한다.
2. **Payload before reference:** Artifact payload는 그것을 참조하는 commit manifest/state/pointer보다 먼저 저장·verified-read되어야 한다.
3. **Verifier before publishable manifest:** Candidate report, result report와 payload가 먼저 verified-readable이어야 `PublishableResultRef`를 저장할 수 있다.
4. **Publishable manifest before pointer:** `PublishableResultRef`의 closure를 검증한 뒤 `results/published` 하나를 CAS한다.
5. **State transition artifact before state pointer:** Next-state body와 referenced artifacts를 먼저 쓰고 검증한 뒤 `state/current` 하나만 CAS한다.

```text
ExecutionManifest put+verify
→ declared assignment/outcome payload put+verify
→ committed outcome manifest put+verify
→ worker committed pointer CAS
→ [Phase 10 validates declared completeness/champion]
→ round/champion artifacts put+verify
→ round/solve state pointer CAS
→ final result + both verifier reports put+verify
→ PublishableResultRef put+verify
→ published result pointer CAS
```

Phase 09는 괄호 안의 coordinator 판단을 구현하지 않는다. Storage layer는 caller가 제시한 exact reference closure와 precondition을 검사하고 bytes/pointer를 commit할 뿐이다.

Crash/fault 결과:

| Fault point | Safe observable state | Recovery |
|---|---|---|
| Payload stream 중 실패 | Final key/ref 없음, staging residue 가능 | Same key 재시도, residue retention cleanup |
| Payload commit 뒤 verification 전 실패 | Immutable orphan 가능 | Exact key read+verify 후 idempotent resume |
| Manifest commit 전 실패 | Payload orphan, authority 없음 | Manifest 재생성 또는 retention |
| Pointer CAS 전 실패 | 모든 artifact 존재해도 authority 이전 상태 | Same expected/desired로 resume |
| Pointer CAS 성공 뒤 response 유실 | Current pointer read로 desired checksum 확인 | `ALREADY_AT_DESIRED` 수렴 |
| CAS stale | 다른 current version이 authoritative | Caller가 새 state를 읽고 Phase 10에서 판단 |

### 7.6 Declared completeness와 listing inconsistency

Normal run path:

```text
ExecutionManifest declares:
  round 2
  worker ordinals [0, 1, ... explicitly]
  exact WorkerRunId and AssignmentRef for each

Phase 10 asks repository:
  read manifest by exact ref
  for each declared worker in stable ordinal order
    read committed pointer by exact typed key
    read outcome by exact ref
    verify checksum/authority closure
```

금지 path:

```text
list "rounds/2/workers/"
→ currently visible objects만 success로 간주
→ best candidate/champion 선택
```

`MaintenanceObjectScanner`가 lifecycle discovery를 위해 listing을 사용할 수는 있다. 그러나 list는 누락, 중복, stale entry, reordered entry와 phantom metadata를 반환할 수 있는 **비권위 hint**다. Purge 전에는 exact key/version을 다시 읽고 current roots/holds/references를 재검증한다. Normal read/completeness API에는 list method를 두지 않는다.

### 7.7 Lifecycle, retention과 quarantine

```text
STAGING
→ IMMUTABLE_UNREFERENCED
→ REFERENCED_ACTIVE
→ REFERENCED_SUPERSEDED
→ RETENTION_HELD | QUARANTINED
→ PURGE_ELIGIBLE
→ PURGED
```

| Retention class | 의미 | 자동 purge 가능 조건 |
|---|---|---|
| `ACTIVE_AUTHORITY` | Current state/result pointer 또는 그 exact closure | 불가 |
| `REFERENCED_IMMUTABLE` | Active manifest/evidence가 참조 | 불가 |
| `SUPERSEDED_REFERENCED` | 이전 accepted version/replay/rollback point | Policy + hold + rollback owner 승인 |
| `UNREFERENCED_STAGING` | Pointer/manifest에 연결되지 않은 partial/orphan | Explicit grace와 exact unreferenced 재검증 |
| `EVIDENCE_HOLD` | Phase/review/security/legal evidence | Hold release authority 전 불가 |
| `SECURITY_QUARANTINE` | Corruption/tamper 조사 대상 | Incident owner disposition 전 불가 |
| `PURGE_ELIGIBLE` | 모든 root/hold/grace 재검증을 통과 | Exact version conditional delete일 때만 |

Production retention day, grace interval, evidence hold 기간과 purge batch size는 OPEN이다. Test는 manual clock과 `P09_RETENTION_TEST_ONLY` policy를 주입한다. Listing age, provider “last modified”, object count 또는 비용 압력만으로 authority artifact를 삭제하지 않는다.

Safe maintenance 순서:

```text
non-authoritative scan candidate
→ exact metadata/version read
→ tenant/policy/hold/quarantine check
→ current publication/state roots exact read
→ manifest reference closure exact traverse
→ proposed purge marker CAS
→ grace/recheck under same explicit policy
→ exact version conditional delete
→ immutable audit receipt
```

Actual production delete activation, legal retention과 provider lifecycle mapping은 Phase 11/14 운영 승인 범위다. Phase 09 local/in-memory test는 위 safety contract와 dry-run/conditional deletion만 검증한다.

### 7.8 Security boundary

1. `StorageAccessContext`는 authenticated principal reference, tenant scope, operation과 purpose를 갖는다. Raw credential/secret은 값 객체나 fingerprint에 넣지 않는다.
2. Authorization은 key resolution/read/write/metadata/list-like maintenance 전에 수행한다.
3. Cross-tenant denial은 object 존재, checksum, length, locator와 version을 노출하지 않는다.
4. `OpaqueLocator`는 Phase 08 `ArtifactRef` 안에서 port로 왕복할 수 있지만 application이 parse/compare/compose하지 않는다. Exception, normal result payload와 log에는 출력하지 않는다.
5. Customer input/result/audit는 explicit classification을 가져야 한다. Missing/unknown classification을 permissive default로 바꾸지 않는다.
6. Encryption requirement와 checksum을 분리한다. Local/in-memory adapter가 encryption-at-rest production evidence를 제공한다고 주장하지 않는다.
7. Signature/authenticator가 정책상 필요한 artifact는 trusted key/version과 protected envelope을 검증하기 전 읽지 않는다. 구체 KMS/provider 구현은 future adapter 책임이다.
8. Log/audit에는 tenant/solve의 approved pseudonymous IDs, operation, disposition, checksum prefix 정책만 기록하고 raw PII/full payload/secret/provider locator를 남기지 않는다.
9. Corruption 후 같은 identity를 정상 bytes로 overwrite해 “수리”하지 않는다. Quarantine하고 새 identity/approved recovery를 사용한다.
10. Denied/corrupt/indeterminate failure를 정상 not-found 또는 empty result로 축소하지 않는다.

## 8. Proposed Java 25 contract

아래 코드는 책임, 불변조건과 failure surface를 고정하기 위한 pseudo-signature다. Public API/wire compatibility 또는 구현 완료를 주장하지 않는다. 작성 중 실제화된 Phase 08 §7.3과 Phase 10 §7.4가 공통으로 사용하는 세 port가 baseline이다.

| Actual upstream/downstream baseline | Phase 09 적용 |
|---|---|
| `ArtifactKey(TenantId, ArtifactKind, ArtifactId)` | 그대로 소비. `ArtifactId`와 `ContentDigest` 일치 규칙을 key-layout/encoding ADR에서 명시 |
| `ArtifactRef(kind, schemaVersion, contentDigest, contentLength, mediaType, opaqueLocator, classification, createdByRun)` | 그대로 소비. `OpaqueLocator`는 운반만 가능하고 parse/compare/fingerprint 금지 |
| `ArtifactStore.putIfAbsent/readVerified/metadata` | Object-common이 conditional backend와 verified read로 구현 |
| `RunStateRepository.createIfAbsent/get/compareAndSet` | Same `SolveId`/`StateVersion` 의미를 versioned object + one pointer CAS에 매핑 |
| `ResultPublisher.compareAndSet(SolveId, StateVersion, PublishableResultRef)` | Both-gate closure를 확인한 뒤 one publication pointer CAS |
| Phase 10의 manifest-declared exact retrieval | 기존 port를 약화하지 않는 좁은 `RunArtifactRepository.readDeclared` projection만 proposed 추가 |

Phase 09는 위 port를 다른 public interface로 교체하지 않는다. 아래 `ArtifactEnvelope`, algorithm-tagged checksum, backend token, typed failure와 access policy는 object-common의 internal enforcement 또는 cross-phase review가 필요한 additive refinement다. 같은 이름의 중복 type을 새 package에 만들지 않는다.

### 8.1 Identity와 integrity value

```java
package com.ronext.rpdptw.application.storage;

public record TenantId(String canonicalValue) {
    public TenantId {
        // Proposed: validation is delegated to an approved canonical ID policy.
        // Raw path separators, aliases and blank/reserved values are rejected.
    }
}

public record SolveId(String canonicalValue) {}
public record SubmissionId(String canonicalValue) {}
public record WorkerRunId(String canonicalValue) {}
public record AttemptId(String canonicalValue) {}
public record RoundOrdinal(int value) {}
public record WorkerOrdinal(int value) {}

public record ChecksumAlgorithmId(String value) {}
public record ChecksumAlgorithmVersion(String value) {}

public record ContentDigest(
    ChecksumAlgorithmId algorithmId,
    ChecksumAlgorithmVersion algorithmVersion,
    byte[] value
) {
    public ContentDigest {
        value = value.clone();
    }

    @Override
    public byte[] value() {
        return value.clone();
    }
}

public record ContentLength(long bytes) {
    public ContentLength {
        if (bytes < 0L) throw new IllegalArgumentException("negative length");
    }
}

public record SchemaVersion(String value) {}
public record MediaType(String value) {}
public record EncryptionClassification(String value) {}
public record StateVersion(byte[] opaqueToken) {
    public StateVersion {
        opaqueToken = opaqueToken.clone();
    }

    @Override
    public byte[] opaqueToken() {
        return opaqueToken.clone();
    }
}
```

`ContentDigest`는 Phase 08 이름을 보존하면서 algorithm/version을 hidden default가 아닌 값으로 만든 proposed refinement다. `StateVersion`은 정렬하거나 increment하지 않는다. Defensive copy는 배열 alias를 막기 위한 proposed 구현이고 exact token representation은 adapter contract review 대상이다.

### 8.2 Artifact key, run/round key와 reference

```java
public enum ArtifactKind {
    CANONICAL_INPUT,
    PROBLEM,
    PREPARED_TRAVEL,
    BOUND_PROFILE,
    SOLVE_SNAPSHOT,
    EXECUTION_MANIFEST,
    WORKER_ASSIGNMENT,
    WARM_START,
    WORKER_CANDIDATE,
    CANDIDATE_VERIFIER_REPORT,
    WORKER_OUTCOME,
    ROUND_CHAMPION,
    FINAL_RESULT,
    RESULT_VERIFIER_REPORT,
    PUBLISHABLE_RESULT,
    PROFILE
}

public record ArtifactId(String canonicalValue) {}
public record ArtifactSchemaVersion(String value) {}
public record OpaqueLocator(String opaqueValue) {}
public record RunIdentity(String canonicalValue) {}

public record ArtifactKey(
    TenantId tenantId,
    ArtifactKind kind,
    ArtifactId artifactId
) {}

public sealed interface RunScope
    permits SolveScope, RoundScope, WorkerScope, AttemptScope {}

public record SolveScope(SolveId solveId) implements RunScope {}

public record RoundScope(
    SolveId solveId,
    RoundOrdinal roundOrdinal
) implements RunScope {}

public record WorkerScope(
    SolveId solveId,
    RoundOrdinal roundOrdinal,
    WorkerOrdinal workerOrdinal,
    WorkerRunId workerRunId
) implements RunScope {}

public record AttemptScope(
    WorkerScope workerScope,
    AttemptId attemptId
) implements RunScope {}

public record RunArtifactKey(
    ArtifactKey artifactKey,
    RunScope scope,
    ArtifactRef executionManifestRef,
    ContentDigest expectedContentDigest
) {}

public record ArtifactRef(
    ArtifactKind kind,
    ArtifactSchemaVersion schemaVersion,
    ContentDigest contentDigest,
    long contentLength,
    MediaType mediaType,
    OpaqueLocator opaqueLocator,
    EncryptionClassification encryptionClassification,
    RunIdentity createdByRun
) {}
```

이 shape는 actual Phase 08 §7.3을 보존한다. `OpaqueLocator`는 raw URI/path를 application 의미로 노출하는 허가가 아니다. Application/Phase 10은 값을 parse, concatenate, compare 또는 fingerprint하지 않고 exact `ArtifactRef`를 다시 port에 전달만 한다. Adapter가 checksum-preserving copy로 locator를 바꿔도 semantic artifact identity는 `kind/schema/contentDigest/length`와 authority refs로 유지된다. `executionManifestRef`는 dependent run artifact가 어느 declared execution authority에 속하는지 고정한다.

### 8.3 Envelope, content와 access context

```java
public record ArtifactEnvelope(
    SchemaVersion envelopeSchemaVersion,
    ArtifactRef artifactRef,
    CompressionIdentity compressionIdentity,
    List<ArtifactRef> semanticAuthorityRefs,
    LogicalRunIdentity createdByRun,
    ContentDigest protectedMetadataDigest
) {
    public ArtifactEnvelope {
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
    EncryptionClassification encryptionClassification,
    RunIdentity createdByRun,
    List<ArtifactRef> semanticAuthorityRefs
) {
    // opaqueLocator/provider version/createdAt/observation metadata are excluded.
}

public interface ArtifactContent extends AutoCloseable {
    InputStream openStream() throws IOException;
}

record StorageAccessContext(
    PrincipalRef principal,
    TenantId tenantId,
    StorageOperation operation,
    AccessPurpose purpose
) {}

public enum StorageOperation {
    PUT_IMMUTABLE,
    READ_CONTENT,
    READ_METADATA,
    CREATE_RECORD,
    COMPARE_AND_SET,
    PUBLISH,
    RETENTION_PLAN,
    CONDITIONAL_PURGE
}
```

`ArtifactIdentityProjection`은 canonical field inclusion/exclusion rule을 보이는 pseudo-type이다. `ArtifactEnvelope.artifactRef`를 포함하더라도 `protectedMetadataDigest`를 계산할 때 full `ArtifactRef` serialization을 사용하지 않고 이 locator-free projection을 사용한다.

`ArtifactEnvelope`와 `StorageAccessContext`는 object-common internal enforcement다. Phase 08 public port signature에 새 parameter를 조용히 추가하지 않는다. Tenant-scoped application service가 authorization을 끝낸 뒤 typed key와 adapter-scoped context를 함께 binding하고, object-common은 두 scope의 불일치를 거부한다. 그러나 그 binding이 global/static/`ThreadLocal` 또는 implicit request context이면 이 계약을 충족하지 못한다. Phase 08/09 cross-phase review가 explicit tenant-scoped session/facade 또는 동등한 non-ambient mechanism과 caller-visible missing-context failure를 승인하기 전에는 WP-09.1 이후로 진행하지 않는다. `ArtifactContent`가 repeatable인지 single-use인지는 signature/contract에 명시해야 한다. 구현이 verification을 위해 재독해해야 할 때 caller stream을 임의 재사용하지 않고 committed object를 backend에서 다시 읽는다.

### 8.4 Sealed result와 failure hierarchy

```java
public sealed interface ArtifactPutResult
    permits ArtifactPutResult.Created,
            ArtifactPutResult.AlreadyPresent,
            ArtifactPutResult.Conflict {

    record Created(ArtifactRef reference) implements ArtifactPutResult {}
    record AlreadyPresent(ArtifactRef reference) implements ArtifactPutResult {}
    record Conflict(ArtifactKey key, SafeConflictDetail detail)
        implements ArtifactPutResult {}
}

public sealed interface StorageFailure
    permits ObjectNotFound, AccessDenied, IdentityConflict, CorruptObject,
            StaleVersion, VisibilityIndeterminate, PartialWriteAborted,
            UnsupportedCapability, QuarantinedObject, RetentionBlocked,
            TransientUnavailable {}
```

`ArtifactPutResult`는 actual Phase 08 baseline이다. `StorageFailure`는 object-common이 provider faults를 typed application failure로 안전하게 mapping하기 위한 internal hierarchy다. Phase 08 `ApplicationFailure` review 없이 네 번째 public result variant를 추가하지 않는다. 동시에 internal hierarchy만 선언하고 전달 carrier를 생략해서도 안 된다. Phase 08/09 review는 각 public operation에 대해 §3.3의 모든 disposition을 exhaustive하게 표현하는 checked/sealed result 또는 approved application-failure carrier와 total mapping을 확정해야 한다. `ACCESS_DENIED`, `CORRUPT`, `VISIBILITY_INDETERMINATE`, `PARTIAL_WRITE_ABORTED`, `UNSUPPORTED_CAPABILITY`를 `Conflict`, `NOT_FOUND`, empty result, catch-all unchecked exception으로 축소하면 entry/exit gate 실패다. Failure record는 secret, raw payload, provider locator 또는 다른 tenant metadata를 포함하지 않는다. Provider SDK exception은 adapter-internal cause/audit로 보존할 수 있지만 application failure union을 대체하지 않는다.

### 8.5 Application-owned storage ports

```java
package com.ronext.rpdptw.application.port.out;

public interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
        ArtifactKey key,
        ArtifactContent content,
        ContentDigest expectedDigest
    );

    ReadableArtifact readVerified(ArtifactRef reference);

    ArtifactMetadata metadata(ArtifactRef reference);
}

// Proposed only if Phase 10 needs a named manifest projection.
// It does not replace ArtifactStore.
public interface RunArtifactRepository {
    ReadableArtifact readDeclared(
        ArtifactRef executionManifestRef,
        DeclaredArtifactIdentity identity
    );
}

public interface RunStateRepository {
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

public interface ResultPublisher {
    PublicationResult compareAndSet(
        SolveId solveId,
        StateVersion expectedState,
        PublishableResultRef result
    );
}
```

이 signature는 Phase 08 §7.3과 Phase 10 §7.4의 proposed baseline을 그대로 맞춘다. `RunState`는 Phase 08/10이 승인한 sealed state record와 schema registry만 받는다. Arbitrary Java serialization, `Map<String,Object>`와 caller-provided serializer는 금지한다.

`readDeclared`는 prefix를 list하지 않는다. Execution manifest에서 requested `DeclaredArtifactIdentity`의 exact ref를 찾고 그 key를 verified-read한다. Manifest에 선언되지 않은 artifact가 실제 저장돼 있어도 이 method는 정상 결과로 반환하지 않는다.

`ObjectProfileCatalog`는 Phase 08 §7.4의 `ProfileCatalogPort.resolveExact(tenantId, profileIdentity, version, presetIdentity)`를 exact immutable profile artifact/ref 조회로 구현한다. Mutable `latest`, prefix search, default profile/version 또는 cross-tenant fallback은 금지한다. Same profile identity/version/preset에 다른 protected bytes가 보이면 `IDENTITY_CONFLICT`/`CORRUPT`로 fail-closed한다.

한편 §7.4~§7.6과 Phase 10이 요구하는 worker committed-outcome pointer에는 위 baseline port 중 어느 것도 exact operation을 제공하지 않는다. `ArtifactStore`의 immutable logical key로 create-once할지, worker-record 전용 typed CAS를 추가할지, 승인된 `RunStateRepository` projection으로 표현할지는 Phase 08/09/10 cross-phase review가 결정해야 한다. Port 없이 backend key를 직접 사용하거나, prefix/list/event로 committed worker를 추론하거나, solve-level `StateVersion`을 worker pointer token으로 재사용해서는 안 된다.

### 8.6 Result publication contract

```java
record StoredPublishableResultClosure(
    TenantId tenantId,
    SolveId solveId,
    PublishableResultRef publishableResultRef,
    ArtifactRef executionManifestRef,
    ArtifactRef candidateVerifierPassReportRef,
    ArtifactRef finalResultRef,
    ArtifactRef resultVerifierPassReportRef,
    ArtifactRef publishableManifestRef
) {}

// PublicationResult is the sealed Phase 08 baseline:
// Published | AlreadyPublished | Conflict | Rejected.
```

`ResultPublisher.compareAndSet`은 다음을 storage-level로 확인한다.

1. 모든 ref가 same tenant/solve/execution manifest authority를 갖는다.
2. 각 object가 exact verified-readable하다.
3. `PublishableResultRef`와 internal `StoredPublishableResultClosure`의 protected digest/length/schema가 일치한다.
4. Candidate/result report가 Phase 07이 지정한 PASS artifact kind/schema다.
5. Phase 08 §7.3/§8.3의 `expectedState`는 publication pointer version이 아니라 current run-state authorization fence로 해석하고 exact guard re-read로 일치시킨다.
6. Published pointer 자체는 별도의 create-if-absent/conditional token으로 exactly one desired ref만 권위화하며, response loss 뒤 exact desired ref이면 `AlreadyPublished`로 수렴한다.

Run-state fence와 published-pointer conditional token은 type과 provenance가 다르며 서로 재사용하거나 provider generation에서 추론하지 않는다. 현재 public signature가 publication pointer token을 직접 받지 않으므로 exact internal precondition/linearization과 conflict mapping은 Phase 08/09/10 cross-phase review에서 고정해야 한다. 그 전 구현과 acceptance는 차단한다.

Publisher는 report 내용을 다시 계산하거나 PASS를 합성하지 않는다. Semantic verifier는 Phase 07의 책임이고 storage는 immutable report identity/closure만 검증한다.

### 8.7 Adapter-internal backend

```java
package com.ronext.rpdptw.adapter.object.common.backend;

public interface ObjectStorageBackend {
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

public record BackendCapabilities(
    boolean atomicCreateIfAbsent,
    boolean atomicCompareAndSet,
    boolean exactReadAfterCommittedResult,
    boolean exactConditionalDelete,
    AdapterExecutionScope executionScope
) {}

public enum AdapterExecutionScope {
    TEST_IN_MEMORY,
    LOCAL_SINGLE_JVM,
    DISTRIBUTED_PROVIDER
}
```

Required capability가 `false`인 backend를 wrapper가 last-write-wins, check-then-put, list-then-decide 또는 hidden lock service로 보완한 척해서는 안 된다. Wrapper가 명시적인 single-JVM serialization으로 구현할 때는 `LOCAL_SINGLE_JVM` 범위를 capability와 evidence에 노출한다.

`ObjectStorageBackend` normal runtime contract에는 list가 없다. Lifecycle scanner가 필요하면 별도 restricted maintenance interface를 사용하며 normal repository code가 compile-depend하지 않도록 architecture test로 막는다.

### 8.8 Local/in-memory conformance adapter

`InMemoryObjectStorageBackend`:

- `ConcurrentHashMap.compute` 또는 동등한 one-key atomic primitive로 put/CAS를 구현한다.
- Stored bytes와 metadata는 defensive copy한다.
- Deterministic fault hook로 partial write, post-commit response loss, delayed visibility, stale read, tamper와 transient failure를 주입한다.
- Multi-thread concurrency oracle를 제공하지만 process crash durability, encryption-at-rest와 provider parity를 주장하지 않는다.
- Production app이 adapter를 silent default로 선택할 수 없게 assembly/config에서 test/reference identity를 확인한다.

`LocalDirectoryObjectStorageBackend`:

- Explicit workspace root 아래 canonical relative key만 허용하고 normalized path가 root를 벗어나면 거부한다.
- Artifact payload는 same-filesystem staging에 쓰고 length/checksum/fsync 검증 뒤 no-replace atomic publish primitive를 사용한다.
- State는 immutable version body를 먼저 쓰고 single-JVM keyed coordinator 아래 current pointer를 atomic replace한다.
- Startup capability probe가 required atomic create/replace/fsync contract를 증명하지 못하면 `UNSUPPORTED_CAPABILITY`로 시작을 거부한다.
- `LOCAL_SINGLE_JVM`을 manifest/evidence에 기록하고 cross-process/distributed CAS를 주장하지 않는다.
- JVM crash 뒤 staging/orphan/current pointer closure를 exact recovery scan으로 검사하되 listing을 business authority로 사용하지 않는다.
- OS file lock을 provider-neutral contract 또는 crash-safe distributed lock으로 간주하지 않는다.

Local adapter의 exact atomic primitive 선택은 ADR/target filesystem 검증 대상이다. Java API가 존재한다는 이유만으로 모든 mount/filesystem에서 atomicity와 durability를 가정하지 않는다.

## 9. Pseudocode와 storage state transition

### 9.1 Immutable put-if-absent

```text
putArtifact(access, key, envelope, content):
  authorize(access, PUT_IMMUTABLE, key.tenant)
  requireCanonicalKey(key)
  requireEnvelopeMatchesKey(envelope, key)
  requireApprovedExplicitChecksumPolicy(envelope)
  requireClassificationPolicy(envelope)

  observedChecksum, observedLength =
      stream content into backend staging while calculating checksum/length

  if observed != envelope.expected:
      abort staging
      audit PARTIAL_WRITE_ABORTED or IDENTITY_CONFLICT
      return rejected

  backendResult = backend.putIfAbsent(finalKey, stagedBytes, expectedIntegrity)

  switch backendResult:
    CREATED:
      exact = backend.getExact(finalKey)
      if not visible or result indeterminate:
        return VISIBILITY_INDETERMINATE  // ref is not commit authority
      verified = verifyEnvelopeLengthChecksum(exact)
      if verified fails:
        quarantine(finalKey)
        return CORRUPT
      return ArtifactCreated(ref, exact.version)

    ALREADY_EXISTS:
      exact = backend.getExact(finalKey)
      if exact bytes + protected metadata exactly equal desired:
        return ArtifactAlreadyPresent(ref, exact.version)
      quarantineConflictEvidenceWithoutOverwrite()
      return IDENTITY_CONFLICT

    PARTIAL_OR_UNKNOWN:
      return VISIBILITY_INDETERMINATE

    UNSUPPORTED:
      return UNSUPPORTED_CAPABILITY
```

`VISIBILITY_INDETERMINATE` 뒤 caller는 same key와 checksum으로 exact read/resume할 수 있다. 다른 key를 생성하거나 state pointer를 commit해서 uncertainty를 숨기지 않는다.

### 9.2 Verified read

```text
readVerified(access, ref):
  authorize before existence lookup
  map typed ref to canonical exact object key
  exact = backend.getExact(key)
  if absent: NOT_FOUND
  validate envelope schema/kind/tenant/classification
  stream bytes and recompute length/checksum
  validate protected metadata checksum
  validate required authenticator policy when applicable
  if any mismatch:
    record quarantine/incident without overwriting object
    return CORRUPT
  return ArtifactRead(envelope, verified readable content, version)
```

Deserialization은 위 verified read 뒤 artifact-kind allow-listed codec에서만 시작한다. Envelope 안의 arbitrary class name, reflective type 또는 executable payload를 로드하지 않는다.

### 9.3 Compare-and-set

```text
compareAndSet(access, key, expectedVersion, desiredRecord):
  authorize and canonicalize key
  encode desired record with approved versioned codec
  put/verify immutable desired state body
  verify all desired references exact and same authority

  result = backend.compareAndSet(
      currentPointerKey,
      expectedVersion,
      pointerTo(desiredBodyRef),
      expectedPointerIntegrity
  )

  switch result:
    UPDATED:
      exactCurrent = backend.getExact(currentPointerKey)
      if exactCurrent does not equal desired pointer:
        return VISIBILITY_INDETERMINATE or CORRUPT
      return StateUpdated(exact desired record and new opaque version)

    VERSION_MISMATCH:
      exactCurrent = verified read current
      if exactCurrent.desiredBodyRef == desiredBodyRef:
        return StateAlreadyAtDesired
      return STALE_VERSION

    RESPONSE_UNKNOWN:
      exactCurrent = verified read current
      if exactCurrent.desiredBodyRef == desiredBodyRef:
        return StateAlreadyAtDesired
      if exactCurrent.version == expectedVersion:
        return VISIBILITY_INDETERMINATE
      return STALE_VERSION or CORRUPT according to verified state
```

`StateAlreadyAtDesired`는 current pointer가 exact desired body ref와 protected metadata를 가리킬 때만 반환한다. State enum/value 일부가 같거나 timestamp가 최신이라는 이유로 수렴하지 않는다.

### 9.4 Publication

```text
publish(access, publishedKey, expectedRunState, publishableRef):
  authorize PUBLISH for tenant/solve
  exact-read current run state and require expectedRunState authorization fence
  verify execution manifest exact
  verify candidate PASS report exact
  verify final result exact
  verify result PASS report exact
  verify publishable manifest exact
  verify same tenant/solve/manifest authority closure
  verify no quarantined/unknown algorithm/classification violation
  create/CAS the single published pointer under its own distinct precondition
  verified-read the pointer
  return CREATED / ALREADY_AT_DESIRED / typed rejection
```

Final-result artifact가 이미 존재해도 PASS report 하나가 없거나 pointer CAS가 없으면 `readPublished`는 정상 payload를 반환하지 않는다.

### 9.5 Run/round artifact lifecycle

```text
ABSENT
  → STAGING
  → IMMUTABLE_VERIFIED
  → DECLARED_REFERENCEABLE
  → COMMITTED_BY_POINTER
  → SUPERSEDED_BUT_RETAINED
  → PURGE_ELIGIBLE

side:
STAGING → PARTIAL_ABORTED
any verified/read step → QUARANTINED on corruption
CAS attempt → STALE without changing current authority
```

`DECLARED_REFERENCEABLE`는 storage state이며 worker/round completion이 아니다. `COMMITTED_BY_POINTER`도 해당 artifact pointer commit만 뜻한다. Phase 10이 manifest-declared set과 application transition을 검증하기 전에는 round success/champion을 뜻하지 않는다.

### 9.6 Duplicate writer cases

| Writer A | Writer B | Required outcome |
|---|---|---|
| Same key, same bytes | Same key, same bytes | One create; other `ALREADY_PRESENT_SAME` |
| Same key, different bytes/checksum | Concurrent | One create may succeed; other `IDENTITY_CONFLICT`, overwrite 0 |
| Same expected state, same desired ref | Concurrent CAS | One update; other `ALREADY_AT_DESIRED` or stale-read then exact convergence |
| Same expected state, different desired ref | Concurrent CAS | Exactly one update; loser `STALE_VERSION` |
| CAS success response lost | Retry same operation | Exact current read → `ALREADY_AT_DESIRED` |
| Partial writer leaves staging | Complete writer same final key | Complete writer may succeed; staging never shadows final exact key |

## 10. Exact test fixtures, oracle와 red → green plan

모든 test 값은 이름과 manifest에 `TEST_ONLY`를 포함한다. 아래 worker/thread/object-size/clock/checksum 선택은 production default, official benchmark 또는 provider sizing이 아니다.

### 10.1 Fixture catalog

| Fixture ID | 구성 | 주입/변형 | Expected oracle |
|---|---|---|---|
| `P09_ARTIFACT_A_TEST_ONLY` | Tenant A, solve A, kind `WORKER_OUTCOME`, canonical bytes A | 없음 | Put `CREATED`, read bytes/checksum/length exact |
| `P09_ARTIFACT_A_DUP_TEST_ONLY` | A와 key/envelope/bytes 동일 | Duplicate writer | `ALREADY_PRESENT_SAME`, stored version/bytes unchanged |
| `P09_ARTIFACT_A_CONFLICT_TEST_ONLY` | A와 logical identity 같고 bytes B | Same key/different protected content | `IDENTITY_CONFLICT`, original exact |
| `P09_PARTIAL_STREAM_TEST_ONLY` | Declared length보다 긴/짧은 fault stream | N번째 chunk fault | Final ref 0, pointer 0, staging만 cleanup 대상 |
| `P09_DELAYED_VISIBILITY_TEST_ONLY` | Put commit 뒤 configurable manual visibility gate | Immediate exact get unavailable | `VISIBILITY_INDETERMINATE`, downstream CAS call 0 |
| `P09_STALE_READ_TEST_ONLY` | Current v2인데 fake가 v1을 한 번 반환 | Stale exact read | Version/desired closure mismatch 검출, authority downgrade 0 |
| `P09_LIST_NOISE_TEST_ONLY` | Exact manifest refs W0/W1, list는 W0 누락·W9 phantom·duplicate | Inconsistent list | `readDeclared`는 W0/W1 exact, W9 무시; completeness storage 결과 불변 |
| `P09_RESULT_ORPHAN_TEST_ONLY` | Final result/report objects는 있으나 published pointer 없음 | Existence-only | `NOT_PUBLISHED`, normal result payload 0 |
| `P09_RESULT_BOTH_GATE_TEST_ONLY` | Same authority candidate PASS + result + result PASS + manifest | 정상 | Publication CAS와 verified retrieval 성공 |
| `P09_RESULT_MISSING_GATE_TEST_ONLY` | Result PASS ref 누락/다른 solve | Closure corruption | Publication rejected, pointer unchanged |
| `P09_TENANT_CROSS_TEST_ONLY` | Tenant A ref를 Tenant B access/key로 시도 | Read/write/metadata/publish | `ACCESS_DENIED`, A 존재/metadata leakage 0 |
| `P09_TAMPER_PAYLOAD_TEST_ONLY` | Stored content 한 byte flip | Payload corruption | `CORRUPT`, deserialize/publication 0 |
| `P09_TAMPER_ENVELOPE_TEST_ONLY` | Kind/schema/length/classification/ref 한 field flip | Metadata corruption | `CORRUPT` 또는 policy rejection |
| `P09_TAMPER_POINTER_TEST_ONLY` | Pointer body/ref/checksum flip | Authority corruption | `CORRUPT_PUBLICATION`, result payload 0 |
| `P09_CAS_RACE_SAME_TEST_ONLY` | 여러 test writer, same expected/same desired | Concurrent start barrier | One update + exact convergence, conflicting final 0 |
| `P09_CAS_RACE_DIFFERENT_TEST_ONLY` | Same expected/different desired refs | Concurrent start barrier | Exactly one update; all losers stale |
| `P09_RETENTION_GRAPH_TEST_ONLY` | Active pointer→manifest→A, unreferenced B, held C, quarantined D | Manual policy/clock | A/C/D purge 0; B만 grace+recheck 뒤 eligible |
| `P09_LOCAL_RECOVERY_TEST_ONLY` | Immutable body written, pointer 전/후 crash snapshots | Restart | Before pointer = orphan; after verified pointer = committed |

Fixture content는 checked-in test resource 또는 deterministic builder에서 exact bytes, envelope, key와 expected checksum을 함께 생성한다. Normal implementation이 만든 checksum을 expected oracle로 다시 복사하지 않는다. Expected bytes/checksum은 test resource의 independent calculation 또는 separate minimal reference calculator로 고정한다.

### 10.2 Key/identity oracle

Exact tests:

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
```

Oracle criteria:

- Valid typed identity 집합에서 encode collision 0
- Canonical decode/re-encode mismatch acceptance 0
- Workspace escape 0
- Locator/region/path가 semantic identity/fingerprint input에 등장 0
- Invalid key를 hash/truncate/sanitize해서 다른 valid key로 silently 매핑 0

### 10.3 Put/read/checksum contract

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

`largeStreamingArtifact`의 크기는 `P09_STREAM_SIZE_TEST_ONLY` explicit test config다. Production size limit이나 performance threshold가 아니다. Heap assertion은 application layer가 whole payload byte array를 요구하지 않는 contract를 확인하고 provider performance를 승인하지 않는다.

### 10.4 CAS와 concurrency contract

```text
RunStateRepositoryContract.createAbsentSucceeds
RunStateRepositoryContract.createExistingSameRecordConverges
RunStateRepositoryContract.createExistingDifferentRecordConflicts
RunStateRepositoryContract.casExpectedVersionUpdates
RunStateRepositoryContract.casStaleVersionNeverOverwrites
RunStateRepositoryContract.casSameDesiredAfterLostResponseConverges
RunStateRepositoryContract.versionTokenIsOpaque
WorkerCommitContract.exactDeclaredWorkerCommitHasOneAuthorityPrimitive
WorkerCommitContract.sameWorkerSameDigestConvergesAndDifferentDigestConflicts
WorkerCommitContract.neverUsesSolveOrPublicationVersionAsWorkerPointerToken
StorageConcurrencyContract.putSameContentHasOnePhysicalWinner
StorageConcurrencyContract.putDifferentContentNeverLastWriteWins
StorageConcurrencyContract.sameDesiredCasConverges
StorageConcurrencyContract.differentDesiredCasHasExactlyOneWinner
StorageConcurrencyContract.concurrentReadNeverReturnsPartialCommittedPayload
StorageConcurrencyContract.failurePreservesPreStateFingerprint
```

Concurrency fixture는 barrier로 writer를 동시에 release하고 result/exception/final object를 모두 수집한다. Test thread 수는 explicit `P09_CONCURRENCY_WRITERS_TEST_ONLY`이며 source contract나 runtime default에 박지 않는다. Oracle은 completion order가 아니라 final pointer/ref, winner count와 loser disposition의 set equality다.

### 10.5 Manifest ordering과 result authority

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

Oracle call order는 spy/fault backend의 immutable event trace로 검증한다.

```text
expected:
PUT artifact
GET+VERIFY artifact
PUT reference manifest
GET+VERIFY reference manifest
CAS pointer
GET+VERIFY pointer

forbidden:
CAS pointer before any prerequisite verified read
LIST prefix in normal path
DESERIALIZE before checksum verification
DELETE/OVERWRITE to resolve conflict
```

### 10.6 Listing/read-visibility/partial-fault contract

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

List inconsistency를 시험하기 위해 test backend/maintenance scanner는 list hook를 가질 수 있다. Production `ObjectStorageBackend`와 application port에 list를 추가하지 않는다.

### 10.7 Corruption/tamper contract

각 corruption test는 정상 fixture의 **한 field 또는 한 byte만** 바꾸고 나머지 identity를 유지한다.

| Mutation | Expected rejection | Must remain unchanged |
|---|---|---|
| Payload byte flip | `CORRUPT` checksum | Current pointers, original ref |
| Truncate/append | `CORRUPT` length/checksum | Deserialize count 0 |
| Envelope kind/schema flip | `CORRUPT`/schema reject | Application artifact type |
| Protected metadata checksum flip | `CORRUPT` | Pointer |
| Tenant/solve authority ref flip | `IDENTITY_CONFLICT`/`CORRUPT` | Cross-tenant disclosure 0 |
| Unknown/disabled checksum algorithm | Policy reject | Fallback algorithm 0 |
| Version token flip | `STALE_VERSION` or corrupt token | State |
| Published pointer target flip | `CORRUPT_PUBLICATION` | Normal retrieval 0 |
| PASS report ref → FAIL/wrong kind | Publication reject | Published pointer |
| Local file staged residue | Ignored by exact final key | Normal read |

Test methods:

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

### 10.8 Security and tenant contract

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
```

Local/in-memory tests는 encryption provider나 signer를 구현 완료했다고 주장하지 않는다. Policy stub가 `REQUIRED_BUT_UNAVAILABLE`을 fail-closed로 처리하는지와 classification/authenticator call order만 검증한다.

### 10.9 Exact profile catalog contract

```text
ProfileCatalogContract.resolveExactTenantIdentityVersionAndPreset
ProfileCatalogContract.missingExactVersionDoesNotFallBackToLatestOrDefault
ProfileCatalogContract.sameIdentitySameBytesConverges
ProfileCatalogContract.sameIdentityDifferentProtectedBytesConflicts
ProfileCatalogContract.crossTenantProfileNeverFallsBackOrLeaksExistence
ProfileCatalogContract.listingOrEventIsNeverProfileAuthority
```

Profile oracle은 Phase 08의 exact four-part lookup identity와 immutable descriptor bytes/ref를 독립 fixture로 고정한다. Catalog 구현이 storage list, mutable `latest` pointer, process-local registry 또는 database/index를 숨겨 사용하면 실패다.

### 10.10 Retention/lifecycle contract

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
```

Retention oracle는 manual clock, explicit root graph와 conditional delete outcome을 사용한다. Wall clock sleep, filesystem mtime와 provider list order를 authority로 사용하지 않는다.

### 10.11 Red → green 순서와 applicable layer

| Red stage | 먼저 실패시킬 test | 최소 green scope | Applicable layer | 다음 stage 진입 기준 |
|---:|---|---|---|---|
| R1 | Key/identity validation/property | Typed key + canonical codec | application/object-common | Collision/escape/alias 0 |
| R2 | Put/read/checksum/partial stream | In-memory backend + artifact store | backend/object-common | Same/same, conflict, verified read exact |
| R3 | CAS stale/concurrency/lost response | In-memory atomic CAS | backend/state adapter | Exactly-one and convergence oracle exact |
| R4 | Manifest ordering/result existence separation | Reference closure + publisher | object-common/application ports | Premature pointer/publication 0 |
| R5 | Delayed visibility/list inconsistency/fault | Fault-injecting backend | object-common contract | No implicit consistency/list authority |
| R6 | Tamper/corruption | Quarantine/fail-closed read | integrity/security | Deserialize/publish on corrupt 0 |
| R7 | Tenant/security/access binding | Authorizer/classification/redaction + reviewed non-ambient binding | security/key | Cross-tenant leakage 0, backend call without authority 0 |
| R8 | Exact profile catalog | `ObjectProfileCatalog` over immutable exact refs | object-common/application port | Latest/default/list/index fallback 0 |
| R9 | Retention/root graph | Planner + conditional purge | lifecycle/maintenance | Active/held/quarantine purge 0 |
| R10 | Local capability/recovery | Single-JVM local adapter | object-filesystem | Unsupported platform fail-closed, crash cases exact |
| R11 | Architecture/reactor/handoff | Dependency rules/full suite | all Phase 09 modules | Required skip 0, Phase 10 receipt complete |

Green 구현은 현재 red test가 요구하는 최소 semantic scope만 추가한다. Phase 09/11 boundary가 승인되기 전 S3/provider feature를 임의로 포함·제외하지 않고, coordinator transition, public JSON schema 또는 retention 숫자를 다음 red stage 편의를 위해 미리 넣지 않는다.

## 11. Ordered work packages

각 work package는 **선행 확인 → target → task → test/command → expected → failure/rollback → handoff** 순서로 실행한다. 명령은 future target reactor가 Phase 00에서 확정된 뒤 실제 경로와 일치하는지 review하고 실행한다. 현재 checkout에서 실행 가능한 증거 명령이 아니다.

### WP-09.0 — Entry receipt, drift와 ADR freeze

| 항목 | 내용 |
|---|---|
| Prerequisite | Phase 08 actual detailed contract와 accepted `E-P08-PORT/LOCAL-E2E/IDEMPOTENCY`; Phase 07 accepted output |
| Target | `docs/evidence/phase-09/entry-receipt` 또는 approved immutable equivalent |
| Tasks | Actual Phase 08/10 stable named handoff section 대조, port signature diff, authorization/failure/worker-commit/publication-precondition review, Phase 09/11 S3 boundary decision, canonical envelope/key/checksum/CAS ADR review, owner/task/rollback 고정 |
| Exact tests | Stable source section/link validation, no adjacent document digest as acceptance, port owner/dependency review, OPEN value scan |
| Future command | `mvn -pl rpdptw/application -am verify` |
| Expected | Entry receipt가 exact accepted refs와 unresolved policy를 분리; hidden default/provider type 0 |
| Failure/rollback | 구현 시작 금지; 이 detailed plan과 last accepted Phase 08 contract가 last safe point |
| Handoff | Approved Phase 09 contract version, source/ADR/evidence receipt |

### WP-09.1 — Typed identity, key namespace와 envelope

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.0, approved key/canonical encoding policy |
| Target | Application storage value types, object-common key codec/envelope codec |
| Tasks | Tenant-first typed IDs, run scope, content address, canonical key, protected envelope, allow-listed schema/algorithm registry |
| Exact tests | §10.2, `ArtifactStoreContract.sameChecksumDifferentProtectedMetadataIsConflict`, alias/path/property cases |
| Future command | `mvn -pl adapters/object-common -am test` |
| Expected | Valid identity injective encoding; escape/alias/cross-tenant collision 0; locator leakage 0 |
| Failure/rollback | New types/codecs 제거 또는 disabled assembly; Phase 08 opaque refs 보존 |
| Handoff | `P09-KeyContract`와 canonical test vector digest |

### WP-09.2 — Backend conditional primitive와 in-memory conformance

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.1 |
| Target | `ObjectStorageBackend`, capabilities, `InMemoryObjectStorageBackend`, backend abstract contract |
| Tasks | Atomic create, exact get/metadata, opaque version CAS, defensive copies, deterministic fault hooks |
| Exact tests | §10.3~§10.4의 backend/CAS/concurrency test, partial/response-loss cases |
| Future command | `mvn -pl adapters/object-memory -am test` |
| Expected | Same content convergence, different content conflict, exactly-one CAS, partial read 0 |
| Failure/rollback | In-memory adapter를 assembly에서 제외하고 backend contract를 `NOT_CONFORMANT`로 유지 |
| Handoff | Backend capability matrix와 concurrency oracle report |

### WP-09.3 — Artifact/run repository와 verified reference closure

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.2, Phase 08 port receipt |
| Target | `ObjectArtifactStore`, `ObjectRunArtifactRepository`, `ObjectProfileCatalog`, `VerifiedArtifactReader`, closure verifier |
| Tasks | Streaming checksum/length, exhaustive failure mapping, post-write exact verify, declared exact read, exact immutable profile resolution, corrupt/quarantine disposition |
| Exact tests | §10.3, §10.5~§10.9, no-list architecture test |
| Future command | `mvn -pl adapters/object-common,adapters/object-memory -am test` |
| Expected | Ref는 verified commit 뒤에만 반환; list invocation 0; corruption before decode |
| Failure/rollback | New object-common semantic adapter 비활성화; immutable backend objects는 orphan retention 대상으로 남김 |
| Handoff | `ArtifactStore`/`RunArtifactRepository`/`ProfileCatalogPort` conformance receipt |

### WP-09.4 — Run state와 publication CAS

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.3, Phase 07 both-gate schema identities |
| Target | `ObjectRunStateRepository`, `ObjectResultPublisher` |
| Tasks | Desired immutable state body, reviewed worker committed-pointer authority primitive, one current pointer CAS, distinct run-state/publication preconditions, stale/unknown response reconciliation, both-gate publication closure |
| Exact tests | §10.4~§10.6, especially worker commit, precondition identity, result existence-vs-authority and duplicate writers |
| Future command | `mvn -pl adapters/object-common,adapters/object-memory -am test` |
| Expected | Premature publication/state commit 0; same desired convergence; different desired exactly one winner |
| Failure/rollback | Current pointer는 이전 accepted ref 그대로; orphan desired bodies 보존/retention |
| Handoff | `E-P09-CAS` preliminary report와 publication fault trace |

### WP-09.5 — Fault, corruption, security와 tenant isolation

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.4, Security owner policy/stub, approved non-ambient access binding and exhaustive failure carrier |
| Target | Integrity/security enforcement, redacted audit, quarantine |
| Tasks | Delayed visibility, stale read, partial write, list noise, tamper, cross-tenant, missing/mismatched access binding, classification/authenticator call order |
| Exact tests | §10.6~§10.8 전체 one-field/one-fault matrix |
| Future command | `mvn -pl build/port-contract-tests,adapters/object-memory -am test` |
| Expected | Corrupt deserialize/publish 0; cross-tenant disclosure 0; implicit consistency/list authority 0 |
| Failure/rollback | Affected adapter `NOT_CONFORMANT`; compromised identity quarantine; overwrite repair 금지 |
| Handoff | Fault/corruption/security fixture digests와 `E-P09-TENANT` draft |

### WP-09.6 — Retention/lifecycle와 safe maintenance

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.5, Records/Security owner policy interface |
| Target | Retention planner, root graph, holds/quarantine, conditional purge dry-run |
| Tasks | Non-authoritative scan 분리, exact ref closure, mark/recheck, explicit test policy/manual clock |
| Exact tests | §10.10 전체 lifecycle contract |
| Future command | `mvn -pl adapters/object-common,adapters/object-memory -am test` |
| Expected | Active/held/quarantine delete 0; stale candidate delete 0; missing production policy fail-closed |
| Failure/rollback | Purge path disabled; objects 보존이 safe default; maintenance marker만 폐기 가능 |
| Handoff | Retention safety report, open production duration/authority 목록 |

### WP-09.7 — Local directory single-JVM reference

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.2~09.6, approved local workspace filesystem |
| Target | `LocalDirectoryObjectStorageBackend`, capability probe, recovery |
| Tasks | Root confinement, staged streaming, no-replace publish, single-JVM CAS serialization, fsync/atomic capability probe, restart recovery |
| Exact tests | Backend/semantic abstract suite + `P09_LOCAL_RECOVERY_TEST_ONLY` + traversal/symlink/mount boundary cases |
| Future command | `mvn -pl adapters/object-filesystem -am verify` |
| Expected | Same suite semantic parity with memory; unsupported filesystem fail-closed; multi-process claim 0 |
| Failure/rollback | Local adapter assembly/config 제거; in-memory conformance만 남기고 Phase exit BLOCKED |
| Handoff | Local capability/environment fingerprint와 recovery report |

### WP-09.8 — Architecture, full suite, evidence와 Phase 10 handoff

| 항목 | 내용 |
|---|---|
| Prerequisite | WP-09.0~09.7 green |
| Target | Full reactor, architecture rules, immutable evidence bundle와 consumer receipt |
| Tasks | Forbidden dependency/list/default/provider scan, exact test counts, cross-adapter parity, handoff/rollback/review |
| Exact tests | §10 전체 + Phase09StorageArchitectureTest + evidence schema validation |
| Future command | `mvn -pl rpdptw/application,adapters/object-common,adapters/object-memory,adapters/object-filesystem,build/port-contract-tests -am verify` then `mvn verify` |
| Expected | Required failures/skips 0, approved Phase 09/11 provider boundary exact, coordinator code 0, `E-P09-*` complete, independent review ready |
| Failure/rollback | Accepted Phase 08 local adapter/path로 복귀; Phase 10 entry를 열지 않음 |
| Handoff | Phase 10 run/round repository contract manifest, accepted refs, rollback point |

## 12. Verification command, pass criteria와 evidence

### 12.1 Future command matrix

현재 target reactor/module/test가 존재하지 않으므로 아래 명령은 **FUTURE-RED**다. 구현자가 실제 Phase 00 reactor와 맞는 module path를 확인한 뒤 exact command, toolchain, exit code와 discovered test count를 evidence에 기록한다.

| Layer | Future command | Planned tests | Pass criteria |
|---|---|---|---|
| Application contract | `mvn -pl rpdptw/application -am verify` | Storage value/port signature, sealed failure carrier, access binding, serialization allow-list | Provider/path/list/ambient-authority type leakage 0 |
| Object-common unit | `mvn -pl adapters/object-common -am test` | Key/envelope/checksum/closure/profile/state/worker-commit/publisher/lifecycle unit | Failed/error/skipped required test 0 |
| In-memory backend | `mvn -pl adapters/object-memory -am test` | Backend abstract + concurrency/fault/tamper | All abstract contract methods discovered/green |
| Filesystem backend | `mvn -pl adapters/object-filesystem -am verify` | Same abstract suite + capability/recovery/security | Scope `LOCAL_SINGLE_JVM`, unsupported platform fail-closed |
| S3/backend boundary | `TBD_BY_APPROVED_PHASE_09_11_BOUNDARY` — 현재 실행 명령으로 사용 금지 | Integrated §13.12/Plan Phase 09가 요구한 same abstract suite의 승인된 owning Phase | Owner/module/environment/evidence가 승인되기 전 Phase 09 exit BLOCKED |
| Contract bundle | `mvn -pl build/port-contract-tests -am test` | Artifact/state/publisher/fault/corruption/security/lifecycle | Backend-specific waiver 0 unless exact non-applicability approved |
| Phase slice | `mvn -pl rpdptw/application,adapters/object-common,adapters/object-memory,adapters/object-filesystem,build/port-contract-tests -am verify` | All P09 tests + architecture report | Required skip 0, dependency/list/default violations 0 |
| Full reactor | `mvn verify` | OR-Tools-free ALNS-only system regression | Reactor green; unrelated required module skip 없음 |

선택 test를 실행해야 할 때 `-Dtest=<exact class>`는 해당 module POM을 직접 대상으로 하고 discovered method count를 matrix와 대조한다. `-Dsurefire.failIfNoSpecifiedTests=false`, zero-test success, `-DskipTests`, `-Dmaven.test.skip=true`, stale `target/`, console 마지막 줄 또는 이전 run 결과 혼합은 evidence가 아니다.

### 12.2 Exact structural checks

Implementation review에서는 다음 정적 검사를 자동화한다.

```text
Phase09StorageArchitectureTest.applicationHasNoObjectAdapterDependency
Phase09StorageArchitectureTest.coreSolverVerificationHaveNoStorageProviderDependency
Phase09StorageArchitectureTest.objectCommonHasNoCoordinatorOrSolverDependency
Phase09StorageArchitectureTest.normalRepositoryHasNoListingMethodOrCall
Phase09StorageArchitectureTest.noCloudSdkOutsideFutureProviderAdapter
Phase09StorageArchitectureTest.noRawPathOrUriInApplicationStorageTypes
Phase09StorageArchitectureTest.noJavaNativeSerialization
Phase09StorageArchitectureTest.noMapStringObjectStatePayload
Phase09StorageArchitectureTest.noHiddenDefaultChecksumRetryRetentionOrConsistency
Phase09StorageArchitectureTest.noGlobalStaticThreadLocalOrImplicitStorageAuthority
Phase09StorageArchitectureTest.profileCatalogHasNoLatestDefaultListOrHiddenIndex
Phase09StorageArchitectureTest.runStateWorkerAndPublicationTokensAreDistinct
Phase09StorageArchitectureTest.inMemoryAdapterNotInProductionAssembly
Phase09StorageArchitectureTest.localAdapterDeclaresSingleJvmScope
Phase09StorageArchitectureTest.providerImplementationMatchesApprovedPhase09Boundary
Phase09StorageArchitectureTest.coordinatorImplementationIsAbsentFromPhase09Diff
```

보조 source scan은 semantic architecture test를 대신하지 않지만 다음 forbidden token의 위치를 확인한다.

```text
com.amazonaws
software.amazon.awssdk
com.google.cloud.storage
com.azure.storage
listObjects / listBlobs / prefix listing
FileLock
SELECT / JDBC / JPA
last-write-wins
latest
Map<String,Object>
ObjectInputStream
```

`FileLock`, `list` 또는 provider SDK가 역사/current placeholder에 이미 있는 것과 Phase 09 target module에 새로 들어온 것을 구분한다. Current placeholder source를 Phase 09가 수정하지 않으며, architecture evidence는 proposed target package/module scope를 검사한다.

### 12.3 Layer별 expected evidence

| Evidence key | 반드시 포함할 내용 | 현재 상태 |
|---|---|---|
| `E-P09-STORAGE-CONTRACT` | Source/ADR/Phase08 receipt, port/backend contract versions, key/envelope/checksum vectors, put/read/manifest/list/visibility/partial-fault results, memory/local conformance matrix, exact commands/toolchain/count/exit | NOT_PRODUCED |
| `E-P09-CAS` | Initial/current/desired refs와 versions, stale/same/different concurrent writer traces, pointer-before/after fault, response-loss convergence, result existence-vs-authority/both-gate publication report | NOT_PRODUCED |
| `E-P09-TENANT` | Tenant/access/classification policies, cross-tenant put/read/metadata/CAS/publish denials, redaction, traversal, tamper/quarantine, retention root/hold/purge safety report | NOT_PRODUCED |

각 bundle은 content-addressed immutable artifact 또는 digest-protected local equivalent로 다음도 포함한다.

- Phase/document/review version과 source commit
- Phase 07/08 accepted artifact/evidence refs와 contract fingerprints
- Canonical encoding/key/checksum/CAS/security/retention ADR refs
- Approved non-ambient access binding, exhaustive failure carrier와 operation별 mapping
- Worker committed pointer authority, distinct run-state/publication precondition contract와 Phase 09/11 provider-boundary decision
- Java/Maven/OS/filesystem/runtime fingerprint
- Adapter capability matrix와 declared execution scope
- Passed/failed/error/skipped/discovered test/class/method count
- Test-only fixture/config identities와 production-open value 목록
- Fault/corruption/concurrency seeds, schedules 또는 barrier trace
- Architecture/dependency/list/default/provider scan
- Quarantine/retention/rollback artifacts와 last safe pointer refs
- Reviewer/verdict/timestamp/approval record
- Phase 10 handoff contract identity

### 12.4 Pass/fail oracle summary

| Area | PASS | FAIL |
|---|---|---|
| Put | Same/same exact convergence; different content conflict | Overwrite, key-exists-only idempotency |
| Read | Envelope/length/checksum verified before decode | Decode first, corrupt→not-found |
| CAS | Exactly one desired commit; stale detectable; lost response reconcilable | Check-then-write, stale overwrite |
| Ordering | Artifact verified before reference/pointer | Multi-object success assumption |
| Authority | Published pointer + exact both-gate closure | Result object/list existence |
| Completeness input | Manifest-declared exact ref set | Prefix listing/event arrival |
| Visibility | Explicit verified read or typed indeterminate | Backend default assumption |
| Security | Tenant-first denial, classification, redaction | Locator/metadata/PII leakage |
| Lifecycle | Roots/holds/quarantine/grace/exact-version recheck | Age/list-only purge |
| Provider scope | Approved Phase 09/11 boundary and required same-suite backend evidence | Unapproved omission/pull-forward or provider-specific semantic weakening |

## 13. Exit gate, Definition of Done과 anti-pattern

### 13.1 Exit gate

다음 AND 조건을 모두 만족해야 independent reviewer가 Phase 09 `ACCEPTED`를 권고할 수 있다.

- Phase 00/07/08 accepted input, actual Phase 08 storage port contract와 exact fingerprints가 확인됨.
- Canonical envelope/key/checksum/CAS/security/retention policy의 approved version 또는 명시적 open boundary가 있음.
- Application port와 value type에 bucket/container/path/URI/provider SDK/event DTO가 없음.
- Approved tenant-scoped non-ambient authorization binding이 모든 operation에서 backend lookup보다 먼저 강제되고 missing/mismatch가 fail-closed함.
- §3.3의 모든 failure가 public application carrier에 exhaustively 보존되며 generic exception/`Conflict`/not-found/empty로 축소되지 않음.
- Typed tenant/run/round/worker/artifact identity가 canonical key로 injective하게 encode되고 traversal/alias/collision test가 통과함.
- Protected-metadata/semantic-identity projection에서 `OpaqueLocator`/provider version/observation metadata가 제외되고 physical relocation test가 통과함.
- Immutable artifact put-if-absent가 same/same만 수렴하고 same identity/different protected content를 거부함.
- Streaming write가 length/checksum을 검증하며 partial write에서 final ref/state/pointer가 0임.
- Exact read가 envelope/length/checksum/authenticator policy를 검증하기 전 deserialize하지 않음.
- Backend success 뒤 exact verified read가 없으면 `VISIBILITY_INDETERMINATE`이고 downstream CAS가 0임.
- State/publication transition은 immutable desired bodies 뒤 하나의 pointer CAS만 사용함.
- Worker committed outcome을 exact declared identity로 권위화하는 approved create/CAS primitive가 있고 list/event/solve token을 대신 쓰지 않음.
- Run-state authorization fence, worker pointer token과 publication pointer precondition이 distinct typed identity로 검증됨.
- Stale expected version이 overwrite하지 않고 lost response가 exact desired ref로 수렴함.
- Concurrent same/different writer oracle에서 exactly-one/conflict 규칙이 completion order와 무관하게 통과함.
- Execution manifest가 dependent run artifact보다 먼저 존재하고 payload/reference/pointer ordering test가 통과함.
- Normal run/completeness/publication code path의 prefix listing/event authority가 0임.
- List 누락/phantom/duplicate/reorder에도 declared exact read 결과가 동일함.
- Final result/report object 존재와 authoritative published result가 분리되고 both-gate closure + pointer CAS만 정상 retrieval을 허용함.
- Payload/envelope/reference/pointer의 one-field/one-byte tamper가 fail-closed로 검출되고 overwrite repair/normal payload가 0임.
- Tenant cross-access, path escape, existence/metadata/locator leakage가 0임.
- Exact profile catalog가 tenant/profile/version/preset identity만 조회하며 `latest`/default/list/hidden index fallback이 0임.
- Checksum, authenticity, authorization과 encryption control이 서로 대체되지 않음.
- Active/reference/hold/quarantine artifact가 purge되지 않고 unreferenced artifact도 explicit policy/grace/exact-version recheck를 통과해야 함.
- In-memory adapter는 test/reference, local adapter는 `LOCAL_SINGLE_JVM` 범위를 명시하고 capability 미지원 시 fail-closed함.
- Memory/local adapter가 같은 applicable abstract suite를 통과하며 waiver가 evidence에 명시됨.
- Phase 09/11 S3 boundary decision이 Plan/Integrated/Phase 08/09 문서와 정합하고 그 승인 범위의 same-suite evidence가 있으며, Phase 10 state machine/champion과 DB/index/lock service는 Phase 09 diff에 없음.
- `E-P09-STORAGE-CONTRACT`, `E-P09-CAS`, `E-P09-TENANT`와 independent review가 immutable identity로 고정됨.
- Phase 10 handoff가 exact repository contract, known limitation과 rollback point를 가짐.
- OPEN/GATED/deferred/test-only 값이 production default나 완료 claim으로 바뀌지 않음.

### 13.2 Definition of Done

Phase 09 `ACCEPTED`는 다음을 뜻한다.

1. Database나 hidden index 없이 typed exact key로 immutable run/round artifact를 안전하게 저장·읽을 수 있다.
2. Same logical write의 duplicate는 exact content일 때만 수렴하고 conflict/tamper는 원본을 덮지 않는다.
3. Artifact/reference/pointer의 순서와 one-key CAS가 crash/response-loss/stale concurrency에서도 authority를 모호하게 만들지 않는다.
4. Object/list 존재는 result, worker completion 또는 round champion authority가 아니다.
5. Phase 10은 manifest-declared exact run/round/worker refs와 opaque CAS version만 소비할 수 있다.
6. Partial upload, visibility uncertainty, list inconsistency, duplicate writer와 tampering이 reusable conformance suite에서 실제로 검출된다.
7. Tenant/security/classification/checksum/quarantine/retention 경계가 fail-closed다.
8. Exact profile catalog, tenant-scoped non-ambient access와 lossless typed failure surface가 hidden registry/default/index 없이 구현된다.
9. In-memory와 local single-JVM reference adapter가 provider-neutral semantic contract를 증명하지만 cloud/provider parity나 production durability를 과장하지 않는다.
10. Approved Phase 09/11 provider boundary와 Phase 10 coordinator 책임을 보존한다.
11. Evidence와 independent review가 accepted identity로 고정된다.

### 13.3 금지 anti-pattern

- `if exists then put` check-then-act를 atomic put-if-absent라고 부름
- State를 `get → modify → unconditional put`하고 CAS라고 부름
- CAS stale/version mismatch를 retry loop가 숨기고 새 current에 무조건 덮어씀
- Same key/different bytes를 overwrite 또는 “latest wins”로 수용
- Provider ETag/generation을 application이 파싱·정렬하거나 domain identity로 사용
- Run-state, worker pointer와 publication pointer의 token을 재사용하거나 한 token을 다른 pointer version으로 해석
- Bucket/container/region/path/URI를 `ArtifactRef`나 semantic fingerprint에 넣음
- Raw user/tenant/solve string을 object key에 concatenate
- Path sanitize/truncate/hash fallback으로 invalid identity를 silently 다른 key로 바꿈
- Java native serialization, class name reflection, arbitrary JSON map을 state/artifact authority로 사용
- Checksum 검증 전 deserialize
- Checksum을 signature, authorization 또는 encryption으로 간주
- Unknown checksum/schema/classification을 default로 허용
- Storage authority를 global/static/`ThreadLocal`/implicit request context에서 조회
- Denied/corrupt/indeterminate/partial/unsupported failure를 generic exception, conflict, not-found 또는 empty로 축소
- Upload/SDK 2xx/object existence/list result를 verified artifact 또는 normal result authority로 간주
- Candidate/result PASS report 일부만으로 publication pointer 생성
- Publication pointer 없이 final result bytes를 `COMPLETED`로 조회
- Prefix listing으로 worker set/completeness/champion을 결정
- Profile `latest`/default, prefix listing, event 또는 hidden registry/index로 exact profile identity를 대체
- Event notification 도착 순서를 commit/comparison order로 사용
- Directory rename, file lock, multi-file transaction 또는 default filesystem atomicity를 common contract로 가정
- Local JVM mutex를 distributed lock/evidence로 광고
- Delayed visibility를 not-found로 축소하거나 fixed hidden retry count로 덮음
- Partial/indeterminate write 뒤 다른 key로 retry해 logical duplicate를 만듦
- Corrupt object를 정상 bytes로 overwrite해서 incident evidence를 제거
- Listing/mtime/age만으로 active/reference/hold/quarantine artifact 삭제
- In-memory adapter를 durability/encryption/production evidence 또는 production silent default로 사용
- 승인된 Phase 09/11 boundary와 달리 AWS SDK/S3 adapter/IAM/KMS/distribution을 임의로 포함하거나 제외
- Phase 09에 solve/round legal transition, declared completeness 판정, champion/comparator/cancellation 업무 상태를 구현
- Hidden database, idempotency table, global lock, mutable “latest” catalog를 추가
- `Q-BENCH-02` official 수치, retention day, retry/timeout/object size/concurrency 값을 임의 default로 고정

## 14. Blocker, OPEN/GATED/deferred와 restart

| 항목 | 상태 | Owner | 현재 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|---|
| Phase 00 accepted architecture 부재 | BLOCKER | Architecture + scheduler | Target module/dependency implementation | 이 detailed document와 current placeholder characterization | `E-P00-ARCH`, accepted review/reactor |
| Phase 07 actual but unaccepted | BLOCKER | Verification/Result + scheduler | Authoritative both-gate artifact input | Actual Phase 07 named handoff semantics | Accepted `E-P07-*`와 review |
| Phase 08 actual but unaccepted | BLOCKER | Application/Local Runtime + scheduler | Phase 09 code/test/evidence | Actual §6.3~§7.6/§8.3/§9.1~§9.3 port semantics | `E-P08-*`, accepted review와 named handoff artifact identity |
| Phase 10 actual but unaccepted | CONSUMER REVIEW PENDING | Phase 10 owner | Cross-phase signature freeze; Phase 09 core semantics는 안 막음 | Actual §6.1~§7.4/§14.1 storage consumer | Cross-phase review, stable section citation과 accepted Phase 10 entry receipt |
| Scheduler task/roles 미지정 | BLOCKER | 총괄 scheduler | Authoritative implementation/status/review | `scheduler_task_id: TBD_NOT_SUPPLIED` | Exact task ID와 implementer/test/reviewer 분리 |
| Authorization binding 부재 | BLOCKER | Phase 08 Application + Phase 09 Security/Data Integrity | 모든 storage operation 구현 | Tenant-first invariant와 §7.8 policy | Explicit tenant-scoped non-ambient binding, missing/mismatch oracle와 cross-phase approval |
| Lossless failure carrier 부재 | BLOCKER | Phase 08 Application + Phase 09 Data Integrity | Port implementation과 fault evidence | §3.3 typed taxonomy | Operation별 exhaustive result/failure mapping, no-collapse tests와 cross-phase approval |
| Worker committed pointer operation 부재 | BLOCKER | Phase 08/09 Storage + Phase 10 Coordinator | Worker outcome commit/read와 fan-in | Immutable outcome/ref; no pointer authority | One approved typed create/CAS primitive, same/different digest oracle와 consumer receipt |
| Publication precondition identity 모호 | BLOCKER | Phase 08 Application + Phase 09 Storage + Phase 10 Coordinator | Result publisher implementation/linearizability | Phase 08 run-state guard + one published pointer ordering | Distinct run-state fence/publication precondition contract와 stale/lost-response tests |
| Phase 09/11 S3 경계 충돌 | BLOCKER | Architecture + Phase 08/09/11 owners + scheduler | Phase 09 scope, exit suite와 Phase 11 entry | Provider-neutral contract와 local/in-memory plan | Plan/Integrated/Phase 08/09/11 정합 decision; required S3 same-suite location/evidence 승인 |
| Canonical envelope/encoding | OPEN/PROPOSED | Architecture/Data Integrity + API/Data | External bytes compatibility와 implementation freeze | Versioned field inclusion/exclusion in §7/§8 | `ADR-ARCH-002/005` 또는 동등 approval |
| Key token/layout | OPEN/PROPOSED | Architecture/Platform/Security | Physical key compatibility | Typed namespace grammar | Collision/migration/security review와 approval |
| Checksum algorithm/version | OPEN/PROPOSED | Data Integrity/Security | Production content address | Algorithm-tagged contract; test-only SHA-256 | Approved algorithm/version/rotation/migration policy |
| Read visibility/retry budget | OPEN | Platform/Operations | Provider adapter tuning/official retry | Typed `VISIBILITY_INDETERMINATE`, no pointer | Provider evidence와 explicit versioned policy; hidden default 금지 |
| Retention/grace/purge value | OPEN | Records/Security/Operations | Production lifecycle/delete | Preserve/hold/dry-run fail-closed | Classification별 policy, legal/security review와 explicit approval |
| Local filesystem atomic capability | ENVIRONMENT-GATED | Local Runtime/Platform | Local adapter Phase 09 acceptance | In-memory reference contract | Approved target FS probe + recovery/atomicity evidence |
| `Q-BENCH-02` official 실행 수치 | OPEN — EXPERIMENT_REQUIRED | Benchmark·Quality | Phase 14 official manifest/baseline/cutover; generic storage test 안 막음 | Explicit `TEST_ONLY` fixture values | Calibration corpus/protocol, measured review, approval |
| Current Win decimal `D/U` | BLOCKER FOR OFFICIAL USE | Input·Matrix + Benchmark | 해당 fixture official use; storage contract 안 막음 | Storage fixtures with opaque bytes | Compliant integer matrix 또는 contract/migration approval |
| `C-17` route pool/MIP | GATED TARGET | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13/production default | Generic immutable artifact kind only | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope, OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` | DEFERRED | Product·Domain·Algorithm | Optional variant 질문/구현 | Generic artifact identity | Restart evidence와 별도 승인 |
| Multi-trip/rotation | DEFERRED FEATURE | Product·Domain·Algorithm | Domain/solver meaning; storage contract 안 막음 | Opaque artifact bytes | Trip/reset/depot/resource contract와 승인 |
| AWS S3 implementation | BOUNDARY DECISION REQUIRED | Architecture/Platform/Security/Operations + scheduler | Phase 09/11 provider adapter 위치와 evidence | Provider-neutral ports + local/memory conformance | 위 Phase 09/11 S3 경계 blocker 해제 뒤 승인된 Phase의 contract/parity/security evidence |
| Production cutover authority | GATED | Product/Operations/Security/Release | Actual traffic/pointer cutover | Local/reference only | Phase 11/14 evidence와 explicit production approval |

`Q-INFRA-01`의 AWS target 선택 자체는 OPEN이 아니지만, 그 구현/evidence를 Phase 09와 Phase 11 중 어디에서 소유할지는 현재 canonical realization 문서끼리 정렬되지 않았다. Target 선택을 구현 완료로 읽거나, 반대로 S3 same-suite 요구를 소유자 승인 없이 삭제하지 않는다.

Blocker가 발생하면 마지막 safe point는 **이전 accepted pointer와 immutable artifact graph**다. 새 artifact는 orphan으로 남을 수 있지만 accepted state/result pointer를 blind rollback하거나 artifact를 overwrite/delete하지 않는다. Rollback은 승인된 이전 immutable ref로 단일 pointer를 CAS하는 별도 authorized operation이며 evidence를 남긴다.

## 15. Previous/next handoff

### 15.1 Previous — actual but unaccepted Phase 08

[Actual Phase 08 — Application ports와 local runtime](phase-08-application-ports-local-runtime.md)에서 다음 accepted contract를 받아야 한다.

- Provider-neutral `ArtifactStore`, `RunStateRepository`, `ResultPublisher` exact signatures
- `ArtifactKey`/`ArtifactRef`, tenant/solve/execution identity와 checksum declaration
- Phase 07 `PublishableResult`/rejection을 application use case에 매핑한 contract
- Local workspace/config identity와 current local atomicity scope
- Same key/same content convergence, different content conflict semantics
- State/publication CAS와 cancellation/result retrieval separation
- Tenant-scoped non-ambient authorization binding과 operation별 lossless failure carrier
- Worker committed outcome authority primitive와 run-state/publication precondition identity 분리
- Deterministic local E2E manifest와 both-gate retrieval evidence
- `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`
- Phase 08 accepted review, build/source/contract fingerprints와 rollback point

받으면 안 되는 것은 다음이다.

- Filesystem `Path`, GCS/S3 URI, bucket/container 또는 provider SDK type
- `Map<String,Object>` state/result
- Prefix listing result 또는 object existence-based completion
- Provider default retry/consistency/overwrite behavior
- Candidate PASS만 있는 정상 publication
- Missing official worker/round/step/watchdog 값을 채운 local default

Phase 08 actual contract가 filesystem atomic rename/CAS를 가정하면 Phase 09는 그 가정을 common object contract로 승격하지 않는다. Explicit local execution scope/capability를 기록하고 in-memory/common semantics와 provider-neutral conditional requirements로 분리한다.

### 15.2 Next — actual but unaccepted Phase 10

[Actual Phase 10 — Provider-neutral coordinator](phase-10-provider-neutral-coordinator.md)는 다음 storage-only handoff를 소비한다.

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
  accepted Phase09 review ref
  rollback pointer/ref
```

Phase 10에 보장하는 operations:

```text
put immutable execution/assignment/outcome/champion/final artifacts by exact typed key
read verified artifact by exact ref
resolve manifest-declared artifact identity without listing
resolve exact profile tenant/identity/version/preset without latest/default/listing
create versioned solve/round record if absent
commit exact declared worker outcome through the approved typed authority primitive
read current record with opaque repository version
CAS one current record/pointer with stale conflict detection
publish both-gate result with distinct run-state authorization fence and published-pointer precondition
distinguish missing / denied / corrupt / stale / indeterminate
```

Phase 10이 storage에 넘겨야 하는 것:

- Exact `ExecutionManifestRef`
- Stable tenant/solve/round/worker/run/attempt identities
- Exact declared assignment/outcome/champion refs
- Desired canonical state record와 expected opaque repository version
- Both-gate `PublishableResultRef`
- Explicit retry/visibility/cancellation policy identity가 필요한 경우 그 ref

Phase 10이 기대하거나 Phase 09가 넘기면 안 되는 것:

- `listWorkers`, `findLatest`, `queryRunningSolves` 같은 database/index API
- Storage가 legal state transition/completeness/champion/comparator를 계산
- Provider ETag/generation/path/URI parsing
- Ambient storage authority or collapsed generic storage failure
- Reuse of run-state token as worker/publication pointer token
- Stale CAS 자동 overwrite 또는 distributed lock
- Partial worker set을 list해서 completion으로 판정
- Result object existence를 published status로 판정
- AWS SDK/event/context 또는 local `Path`

Phase 10 §6.1~§7.4/§14.1과 이 §15.2를 함께 review한다. 서로의 whole-file/section digest를 acceptance로 넣지 않고 stable named section citation과 accepted artifact/evidence identity로 trace한다.

### 15.3 Later consumers

| Consumer | 소비할 것 | 소비하면 안 되는 것 | Required later evidence |
|---|---|---|---|
| Phase 11 AWS reference | Approved boundary가 Phase 11에 배정한 backend conditional capability/semantic suite, key/envelope/ref mapping | Unapproved Phase 09 omission 또는 S3 shortcut, weaker CAS | Boundary decision + S3 contract/parity/security/operations |
| Phase 12 provider substitution | Same backend/application conformance suite | Provider-specific special semantics in application | Per-provider adoption/parity |
| Phase 13 gated hybrid | Generic immutable artifact/reference | Route pool/MIP activation or provider store assumption | C-17 and Phase 13 evidence |
| Phase 14 official/cutover | Published pointer + verified immutable lineage | Test-only values, orphan object/list result | Approved manifest/fixture/AWS/cutover authority |
| Retention operations | Root/hold/quarantine/purge safety contract | Listing/age-only delete | Legal/security/operations policy and rehearsal |

## 16. Source → requirement → test → evidence traceability

| Requirement | Source | Phase 09 contract | Exact test | Planned evidence |
|---|---|---|---|---|
| `REQ-ARCH-DAG` provider isolation | [Final Architecture §2](../../2026-07-26-architecture-design.md#2-module과-package-경계), [Plan §4.2](../master-realization-plan.md#42-compileruntime-invariants) | §6/§8 dependency | `Phase09StorageArchitectureTest.*Dependency*` | All P09 keys + architecture report |
| `REQ-NODB` no database/hidden index/lock | User constraint, [Integrated §13.1](../../architecture-domain-implementation-design.md#131-storage-전제), [Plan Phase 09](../master-realization-plan.md#phase-09--db-없는-object-storage) | §2.3/§3.2/§13.3 | No DB/list/lock architecture tests | `E-P09-STORAGE-CONTRACT` |
| `REQ-KEY` typed tenant namespace/locator separation | [Integrated §13.2](../../architecture-domain-implementation-design.md#132-logical-key와-provider-locator) | §7.2/§8.2 | `CanonicalKeyCodec*` | `E-P09-STORAGE-CONTRACT`, `E-P09-TENANT` |
| `REQ-IMMUTABLE` create-once artifact | [Final Architecture §5.2](../../2026-07-26-architecture-design.md#52-artifact-configuration과-provenance), [Integrated §13.5](../../architecture-domain-implementation-design.md#135-immutable-artifact와-mutable-pointer-분리) | §7.3~§7.4/§9.1 | `ArtifactStoreContract.*` | `E-P09-STORAGE-CONTRACT` |
| `REQ-CHECKSUM` verify before deserialize | [Final Architecture §5.2](../../2026-07-26-architecture-design.md#52-artifact-configuration과-provenance), ADR backlog | §7.3/§8.3/§9.2 | Read/checksum/corruption contract | `E-P09-STORAGE-CONTRACT`, `E-P09-TENANT` |
| `REQ-CAS` stale-detecting state/publication | [Integrated §13.3~§13.5](../../architecture-domain-implementation-design.md#133-application-level-storage-ports), [Plan §4.2](../master-realization-plan.md#42-compileruntime-invariants) | §8.5~§8.7/§9.3~§9.4 | State/publisher/concurrency contract | `E-P09-CAS` |
| `REQ-PRECONDITION-IDENTITY` distinct pointer/fence tokens | [Phase 08 §7.3](phase-08-application-ports-local-runtime.md#73-artifactstorage-contracts-produced-for-phase-09), [Integrated §13.5](../../architecture-domain-implementation-design.md#135-immutable-artifact와-mutable-pointer-분리) | §3.2/§8.5~§8.6 | Distinct run-state/worker/publication precondition tests | `E-P09-CAS` |
| `REQ-IDEMPOTENCY` duplicate exact convergence | [Final Architecture §3.6](../../2026-07-26-architecture-design.md#36-identity-idempotency-retry와-cancellation), [Integrated §13.8](../../architecture-domain-implementation-design.md#138-multi-object-transaction-금지) | §3.2/§9.1/§9.6 | Same/same, different, lost-response tests | `E-P09-CAS` |
| `REQ-WORKER-COMMIT` exact committed outcome authority | [Integrated §13.5](../../architecture-domain-implementation-design.md#135-immutable-artifact와-mutable-pointer-분리), [Phase 10 §6.3](phase-10-provider-neutral-coordinator.md#63-lifecycle과-stateaction-commit) | §7.4~§7.6/§8.5 | `WorkerCommitContract.*` | `E-P09-CAS` + Phase 10 handoff |
| `REQ-ORDERING` payload then one pointer | [Integrated §13.5~§13.8](../../architecture-domain-implementation-design.md#135-immutable-artifact와-mutable-pointer-분리) | §7.5/§9 | `ManifestOrderingContract.*` | `E-P09-STORAGE-CONTRACT`, `E-P09-CAS` |
| `REQ-NOLIST` declared exact completeness | [Integrated §13.7](../../architecture-domain-implementation-design.md#137-listing-금지와-declared-completeness), [Plan §4.2](../master-realization-plan.md#42-compileruntime-invariants) | §7.6/§8.5 | List-noise/fault/architecture tests | `E-P09-STORAGE-CONTRACT` |
| `REQ-AUTHORITY` result existence != publication | [Master §14.1](../../2026-07-31-phase-b-master-design.md#141-publication-gate), [Phase 07 §15.2](phase-07-independent-verification-final-result.md#152-next--actual-but-unaccepted-phase-08) | §7.1/§8.6/§9.4 | `ResultPublisherContract.*` | `E-P09-CAS` |
| `REQ-PARTIAL` partial/visibility fail closed | [Final Architecture §5.3](../../2026-07-26-architecture-design.md#53-상태와-failure-계약), user Phase 09 requirement | §3.3/§7.5/§9.1 | Partial/delayed/stale fault tests | `E-P09-STORAGE-CONTRACT`, `E-P09-CAS` |
| `REQ-CORRUPTION` tamper detection/quarantine | [Final Architecture §5.6](../../2026-07-26-architecture-design.md#56-test와-evidence), [Integrated §22.4](../../architecture-domain-implementation-design.md#224-independent-corruption-fixtures) | §7.3/§7.8/§10.7 | `StorageCorruptionContract.*` | All P09 keys |
| `REQ-TENANT` isolation/security/redaction | [Final Architecture §5.5](../../2026-07-26-architecture-design.md#55-observability와-security), [Integrated §20](../../architecture-domain-implementation-design.md#20-security와-tenant-boundary) | §7.8/§8.3 | `StorageSecurityContract.*` | `E-P09-TENANT` |
| `REQ-ACCESS-BINDING` explicit non-ambient authority | [Integrated §20](../../architecture-domain-implementation-design.md#20-security와-tenant-boundary), [Phase 08 §7.3](phase-08-application-ports-local-runtime.md#73-artifactstorage-contracts-produced-for-phase-09) | §3.1~§3.2/§7.8/§8.3 | Missing/mismatch/no-ambient authorization tests | `E-P09-TENANT` |
| `REQ-FAILURE-CARRIER` lossless typed storage failure | [Final Architecture §5.3](../../2026-07-26-architecture-design.md#53-상태와-failure-계약), [Integrated §21](../../architecture-domain-implementation-design.md#21-failure와-retry-matrix) | §3.3/§8.4 | `StorageFailureSurfaceContract.*` | All P09 keys |
| `REQ-PROFILE-CATALOG` exact immutable profile lookup | [Phase 08 §7.4](phase-08-application-ports-local-runtime.md#74-profile-dispatch-workflow-and-cancellation-ports), [Integrated §12.3/§13.4](../../architecture-domain-implementation-design.md#123-provider-neutral-outbound-ports) | §6/§8.5/§10.9 | `ProfileCatalogContract.*` | `E-P09-STORAGE-CONTRACT`, `E-P09-TENANT` |
| `REQ-RETENTION` safe lifecycle | [Master §16.3](../../2026-07-31-phase-b-master-design.md#163-deferred-resume-criteria), [Integrated §13.5/§20/§26](../../architecture-domain-implementation-design.md#135-immutable-artifact와-mutable-pointer-분리) | §7.7/§10.10 | `StorageLifecycleContract.*` | `E-P09-TENANT` + retention report |
| `REQ-LOCAL-PORT` local/memory reference contract | [Integrated §12.4~§13.4](../../architecture-domain-implementation-design.md#124-local-reference) | §6/§8.7~§8.8 | Backend abstract suite + local recovery | `E-P09-STORAGE-CONTRACT` |
| `REQ-PROVIDER-BOUNDARY` filesystem/S3 same-suite ownership | [Integrated §13.12](../../architecture-domain-implementation-design.md#1312-phase-9-gate), [Plan Phase 09](../master-realization-plan.md#phase-09--db-없는-object-storage), [Phase 08 §16.2](phase-08-application-ports-local-runtime.md#162-next--actual-but-unaccepted-phase-09-storage-contract) | §1.1/§4/§14 | Approved boundary + required backend suite | Decision ref + `E-P09-STORAGE-CONTRACT` or approved Phase 11 evidence |
| `REQ-HANDOFF-P08` consume application/storage contract | [Plan Phase 08](../master-realization-plan.md#phase-08--application-interface와-local-실행), [Integrated §12](../../architecture-domain-implementation-design.md#12-phase-8--application-ports와-local-reference-runtime) | §4/§15.1 | Contract receipt/compatibility tests | Entry receipt + all P09 keys |
| `REQ-HANDOFF-P10` storage-only run/round repository | [Plan Phase 10](../master-realization-plan.md#phase-10--여러-round를-조정하는-coordinator), [Integrated §14](../../architecture-domain-implementation-design.md#14-phase-10--provider-neutral-logical-coordinator) | §8.5/§15.2 | Declared exact read/state CAS consumer tests | Phase 09 handoff manifest |
| `REQ-NO-PULLFORWARD` no unapproved provider/coordinator scope | [Plan Phase 10~11](../master-realization-plan.md#phase-10--여러-round를-조정하는-coordinator) | §2.3/§13.3 | Approved-boundary architecture/diff scope tests | Review + all P09 keys |
| `REQ-OPEN-GATE` no hidden official/default | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, [Plan §14](../master-realization-plan.md#14-open-gated-deferred와-restart-condition) | §3.1/§14 | Config/source label scan | All P09 keys + known limitations |

새 storage operation, index/query, mutable pointer, checksum/encoding, retention/security policy 또는 provider shortcut이 필요하면 이 표에 source/owner/test/evidence를 연결하고 Phase 08/10 compatibility, ADR와 review를 같은 변경 단위에서 갱신한다. 편의를 위해 database/lock/listing/default consistency를 숨겨 넣거나 object existence를 authority로 승격하지 않는다.
